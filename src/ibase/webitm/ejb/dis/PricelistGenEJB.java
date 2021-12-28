package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.ejb.Stateless;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
@Stateless 
public class PricelistGenEJB extends ValidatorEJB
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	DistCommon distCommon = new DistCommon();
	DecimalFormat df = new DecimalFormat("0.00");
	
	public Object generateAndInsertPriceList(String productCode,String grpCode, ArrayList<HashMap<String, String>> targetPlist, String effFrom, String validUpto, String xtraParams, boolean isPreview, Connection conn) throws RemoteException,ITMException
	{
		String  trDate = "";
		boolean isError = false;
		
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		ArrayList<HashMap<String,String>> itemCodeList = new ArrayList<HashMap<String,String>>();
		HashMap<String, ArrayList<HashMap<String, String>>> itemWiseInsPlistHMap = new HashMap<String, ArrayList<HashMap<String, String>>>();

		try
		{
			System.out.println("PriceListGenEJB.generateAndInsertPriceList() : productCode["+productCode+"] effFrom["+effFrom+"] validUpto["+validUpto+"] isPreview["+isPreview+"] grpCode["+grpCode+"]");

			trDate = genericUtility.getValidDateString(new Date(), genericUtility.getApplDateFormat());
			
			sql = "SELECT ITEM_CODE, UNIT FROM ITEM WHERE PRODUCT_CODE = ? ";
			
			if(grpCode!=null && grpCode.trim().length()>0)
			{
				sql+="AND GRP_CODE = '"+grpCode+"' " ;
			}
				
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, productCode);
			
			rs = pstmt.executeQuery();
			
			while (rs.next())
			{
				String itemCode = "", unit = "";
				HashMap<String, String> tempMap = new HashMap<String, String>();
				
				itemCode = E12GenericUtility.checkNull(rs.getString("ITEM_CODE"));
				unit = E12GenericUtility.checkNull(rs.getString("UNIT"));
				
				tempMap.put("item_code", itemCode);
				tempMap.put("unit", unit);
				
				itemCodeList.add(tempMap);
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs!=null)
			{
				rs.close();
				rs=null;
			}
			
			if(itemCodeList != null && itemCodeList.size() > 0)
			{
				for(HashMap<String, String> eachItemHMap : itemCodeList)
				{
					ArrayList<HashMap<String, String>> itemWisePlist = new ArrayList<HashMap<String,String>>();
					String itemCode = eachItemHMap.get("item_code");
					String unit = eachItemHMap.get("unit");
					
					if(targetPlist != null && targetPlist.size() > 0)
					{
						double masterRate = 0.0;
						String  finalRate = "0.0";
						String priceListMRP1=targetPlist.get(0).get("price_list_mrp");
						masterRate = distCommon.pickRate(priceListMRP1, trDate, itemCode, "", conn);
						
						System.out.println("priceListMRP1["+priceListMRP1+"masterRate["+masterRate+"]");
						for(HashMap<String, String> eachPlistHMap : targetPlist)
						{
							
							ScriptEngineManager manager = new ScriptEngineManager();
						    ScriptEngine exprsEngine = manager.getEngineByName("js");
							
							String priceListMRP = eachPlistHMap.get("price_list_mrp");
							String priceListTar = eachPlistHMap.get("price_list_tar");
							String priceListParnt = eachPlistHMap.get("price_list_parent");
							String calcMethod = eachPlistHMap.get("calc_method");
							String freeQty = eachPlistHMap.get("free_qty");
							String freeOn = eachPlistHMap.get("free_on_qty");
							String discPerc = eachPlistHMap.get("disc_perc");
							String listType = eachPlistHMap.get("list_type");
							
							exprsEngine.put("FREE_QTY", getDoubleValue(freeQty));
							exprsEngine.put("ORDER_QTY", getDoubleValue(freeOn));
							exprsEngine.put("DISC_PERC", getDoubleValue(discPerc));
							exprsEngine.put("LOT_NO__FROM", "00");
							exprsEngine.put("LOT_NO__TO", "ZZ");
							
							
							exprsEngine.put("RATE", masterRate);
							
							sql = "SELECT CALC_METHOD, LINE_NO, CALC_SEQ, VAR_NAME, VAR_EXPR, VAR_SOURCE, VAR_INPUT FROM CALC_METHOD_VAR WHERE CALC_METHOD = ? ORDER BY CALC_SEQ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, calcMethod);
							
							rs = pstmt.executeQuery();
							
							while (rs.next())
							{
								PreparedStatement exprPsmt = null;
								ResultSet exprRs = null;
								String[] exprsSqlInputArry = null;
								HashMap<String,String> sqlInputDataMap = new HashMap<String, String>();
								
								sqlInputDataMap.put("ITEM_CODE", itemCode);
								sqlInputDataMap.put("EFF_FROM", effFrom);
								sqlInputDataMap.put("VALID_UPTO", validUpto);
								sqlInputDataMap.put("LOT_NO__FROM", "00");
								sqlInputDataMap.put("LOT_NO__TO", "ZZ");
								sqlInputDataMap.put("FREE_QTY", freeQty);
								sqlInputDataMap.put("ORDER_QTY", freeOn);
								sqlInputDataMap.put("DISC_PERC", discPerc);
								
								String varName = E12GenericUtility.checkNull(rs.getString("VAR_NAME"));
								String varExpr = E12GenericUtility.checkNull(rs.getString("VAR_EXPR"));
								String varSrc = E12GenericUtility.checkNull(rs.getString("VAR_SOURCE"));
								String varInput = E12GenericUtility.checkNull(rs.getString("VAR_INPUT"));
								
								System.out.println("varName["+varName+"]varSrc["+varSrc+"]varInput["+varInput+"]");
								
								if("S".equalsIgnoreCase(varSrc))
								{
									if( varExpr.length() > 0)
									{
										exprPsmt = conn.prepareStatement(varExpr);
										
										if(varInput.length()>0)
										{
											int index = 1;
											exprsSqlInputArry = varInput.split(",");
											
											for(String eachSqlInput : exprsSqlInputArry)
											{
												String var=eachSqlInput.toUpperCase().trim();
												System.out.println("eachSqlInput.toUpperCase()["+var+"]");
												if(sqlInputDataMap.containsKey(var))
												{
													if( "EFF_FROM".equalsIgnoreCase(var) || "VALID_UPTO".equalsIgnoreCase(var) )
													{
														exprPsmt.setTimestamp(index++, getTimeStamp(sqlInputDataMap.get(var)));
													}
													else
													{
														exprPsmt.setString(index++, sqlInputDataMap.get(var));
													}
												}
												else
												{
													exprPsmt.setString(index++, "");
												}
											}
										}
										
										exprRs = exprPsmt.executeQuery();
										
										if(exprRs.next())
										{
											finalRate = E12GenericUtility.checkNull(exprRs.getString(1));
											exprsEngine.put(varName, getDoubleValue(finalRate));
										}
										else
										{
											exprsEngine.put(varName, 0);
										}
										if(exprPsmt != null)
										{
											exprPsmt.close();
											exprPsmt = null;
										}
										if(exprRs!=null)
										{
											exprRs.close();
											exprRs=null;
										}
									}
								}
								else if("E".equalsIgnoreCase(varSrc))
								{
									if( varExpr.indexOf("ROUND(") != -1 )
									{
										varExpr = varExpr.replaceAll("ROUND\\(", "Math.round(");
										System.out.println("in 1st if updated varExpr["+varExpr+"]");
									}
									else if(  varExpr.indexOf("ROUND (") != -1 )
									{
										varExpr = varExpr.replaceAll("ROUND \\(", "Math.round(");
										System.out.println("in 2nd if updated varExpr["+varExpr+"]");
									}
									finalRate = String.valueOf(exprsEngine.eval(varExpr));
									
									System.out.println("inside E :::"+varName+"=["+finalRate+"]");
									
									if("NaN".equalsIgnoreCase(finalRate))
									{
										exprsEngine.put(varName, 0);
									}
									else
									{
										exprsEngine.put(varName, getDoubleValue(finalRate));
									}
								}
								
								System.out.println("varName["+varName+"]finalRate["+finalRate+"]");
							}
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							if(rs!=null)
							{
								rs.close();
								rs=null;
							}
							System.out.println("itemCode ["+itemCode+"] priceList["+priceListTar+"] finalRate["+finalRate+"]");
							
							finalRate = df.format(Double.valueOf(finalRate));
							
							System.out.println("finalRate after formatting["+finalRate+"]");
							
							HashMap<String, String> eachPlistInsHMap = new HashMap<String, String>();
							eachPlistInsHMap.put("price_list", priceListTar);
							eachPlistInsHMap.put("price_list_parent", priceListParnt);
							eachPlistInsHMap.put("item_code", itemCode);
							eachPlistInsHMap.put("unit", unit);
							eachPlistInsHMap.put("list_type", listType);
							eachPlistInsHMap.put("eff_from", effFrom);
							eachPlistInsHMap.put("valid_upto", validUpto);
							eachPlistInsHMap.put("rate", finalRate);
							
							itemWisePlist.add(eachPlistInsHMap);
						}
						itemWiseInsPlistHMap.put(itemCode, itemWisePlist);
					}
				}
			}
			
			if(!isPreview)
			{
				return insertPriceList(itemWiseInsPlistHMap, xtraParams, conn);
			}
			else
			{
				return itemWiseInsPlistHMap;
			}
		}
		catch(Exception e)
		{
			isError = true;
			System.out.println("PriceListGenEJB.generateAndInsertPriceList()["+e.getMessage()+"]");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(isError)
				{
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs!=null)
					{
						rs.close();
						rs=null;
					}
				}
			}
			catch(SQLException se)
			{
				System.out.println("PriceListGenEJB.generateAndInsertPriceList()["+se.getMessage()+"]");
				throw new ITMException(se);
			}
		}
	}
	
	public String insertPriceList(HashMap<String, ArrayList<HashMap<String, String>>> insertListHMap, String xtraParams, Connection conn) throws ITMException
	{
		String insertSql = "", sql = "", loginCode = "", chgTerm = "", retString = "";
		PreparedStatement insertPstmt = null, pstmt = null;
		ResultSet rs = null;
		int insertCnt[] = null;
		boolean isError = false,insert=false;
		
		try
		{
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			
			if(insertListHMap != null && insertListHMap.size() > 0)
			{
				Set<String> keySet = insertListHMap.keySet();
				
				insertSql = " INSERT INTO PRICELIST (PRICE_LIST, ITEM_CODE, UNIT, LIST_TYPE, SLAB_NO, EFF_FROM, VALID_UPTO, LOT_NO__FROM, LOT_NO__TO, MIN_QTY, MAX_QTY, RATE, RATE_TYPE, MIN_RATE, CHG_DATE, CHG_USER, CHG_TERM, MAX_RATE, ORDER_TYPE, CHG_REF_NO, PRICE_LIST__PARENT, CALC_BASIS, REF_NO, REF_NO_OLD) "
						  + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				
				insertPstmt = conn.prepareStatement(insertSql);
				
				for(String itemCode : keySet)
				{
					ArrayList<HashMap<String, String>> eachInsPlist = insertListHMap.get(itemCode);
					
					
					for( HashMap<String, String> eachInsHMap : eachInsPlist )
					{
						int slabNo = 1;
						sql="SELECT MAX(SLAB_NO) FROM PRICELIST WHERE PRICE_LIST = ? AND ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, eachInsHMap.get("price_list"));
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if ( rs.next() )
						{
							slabNo = rs.getInt(1)+1;
						}
						System.out.println("slabNo@@@@@@@"+slabNo);
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						
						insert=updatePricelist(itemCode,(String) eachInsHMap.get("price_list"), getTimeStamp(eachInsHMap.get("eff_from")),getTimeStamp(eachInsHMap.get("valid_upto")),eachInsHMap.get("rate"),conn);
						System.out.println("Insert Flag"+insert);
						if(insert)
						{
							insertPstmt.setString(1, eachInsHMap.get("price_list"));
							insertPstmt.setString(2, eachInsHMap.get("item_code"));
							insertPstmt.setString(3, eachInsHMap.get("unit"));
							insertPstmt.setString(4, eachInsHMap.get("list_type"));
							insertPstmt.setInt(5, slabNo);
							insertPstmt.setTimestamp(6, getTimeStamp(eachInsHMap.get("eff_from")));
							insertPstmt.setTimestamp(7, getTimeStamp(eachInsHMap.get("valid_upto")));
							insertPstmt.setString(8, "00");
							insertPstmt.setString(9, "ZZ");
							insertPstmt.setDouble(10, 0);
							insertPstmt.setDouble(11, 999999999);
							insertPstmt.setDouble(12, getDoubleValue(eachInsHMap.get("rate")));
							insertPstmt.setString(13, "F");
							insertPstmt.setDouble(14, getDoubleValue(eachInsHMap.get("rate")));
							insertPstmt.setTimestamp(15, java.sql.Timestamp.valueOf(genericUtility.getValidDateString(new Date(), genericUtility.getDBDateTimeFormat())));
							insertPstmt.setString(16, loginCode);
							insertPstmt.setString(17, chgTerm);
							insertPstmt.setDouble(18, getDoubleValue(eachInsHMap.get("rate")));
							insertPstmt.setString(19, null);
							insertPstmt.setString(20, null);
							insertPstmt.setString(21, eachInsHMap.get("price_list_parent"));
							insertPstmt.setString(22, null);
							insertPstmt.setString(23, null);
							insertPstmt.setString(24, null);
	
							insertPstmt.addBatch();
						}
							insertPstmt.clearParameters();
					}
				}
				
				insertCnt = insertPstmt.executeBatch();
				
				if(insertCnt!=null)
				{
					for(int eachRowCnt : insertCnt)
					{
						System.out.println("PriceList insert cnt ["+eachRowCnt+"]");
					}
				}
				
				retString = "SUCCESS";
			}
		}
		catch(SQLException se)
		{
			isError = true;
			System.out.println("PriceListGenEJB.insertPriceList().sqlException["+se.getMessage()+"]");
			throw new ITMException(se);
		}
		catch(Exception e)
		{
			isError = true;
			System.out.println("PriceListGenEJB.insertPriceList()["+e.getMessage()+"]");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(isError)
				{
					retString = "FAILURE";
					if(insertPstmt != null)
					{
						insertPstmt.close();
						insertPstmt = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
				}
			}
			catch(SQLException se)
			{
				System.out.println("PriceListGenEJB.insertPriceList().finally["+se.getMessage()+"]");
				throw new ITMException(se);
			}
		}
		System.out.println("FINAL MESSAGE["+retString+"]");
		return retString;
	}

	private java.sql.Timestamp getTimeStamp(String dateStr) throws ITMException, Exception 
	{ 
		String dbDateStr = "";
		if(dateStr != null && !dateStr.equals(""))
		{
			if(dateStr.indexOf(":") != -1)
			{
				System.out.println("inside logic"+dateStr);
				return java.sql.Timestamp.valueOf(dateStr);      
			}
			else
			{
				System.out.println("inside ");
				dbDateStr = genericUtility.getValidDateTimeString(dateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateTimeFormat());
				return java.sql.Timestamp.valueOf(dbDateStr); 
			}
		}
		else
		{
			return null;
		}
	}
	private double getDoubleValue(String input) throws Exception
	{
		double value = 0.0;
		try
		{
			if(input == null)
			{
				value = 0.0;
			}
			else
			{
				if(input.trim().length() > 0)
				{	
					value = Double.valueOf(E12GenericUtility.checkNull(input));
					/*String roundValue = df.format(value);
					System.out.println("formatted value = ["+roundValue+"]");
					value = Double.parseDouble(roundValue);*/
				}
			}
			System.out.println("retutn value["+value+"]");
		}
		catch(Exception e)
		{
			System.out.println("PriceListGenEJB.getDoubleValue()["+e.getMessage()+"]");
			throw e;
		}
		return value;
	}
	public boolean updatePricelist(String itemCode,String pricelist,Timestamp effFrom,Timestamp validUpto,String rate,Connection conn) throws ITMException
	{
		boolean retFlag = false;
		PreparedStatement pstmt,pstmt1;
		ResultSet rs,rs1;
		Timestamp dbEffrom=null,dbValidUpto=null,updValidUpto=null;
		String dbPricelist=null,dbitemCode=null,dbUnit=null,dblistType=null,dbslabNo=null;
		try
		{
			
		String sql="select * from PRICELIST where ITEM_CODE = ? and PRICE_LIST = ? "
				+ "and ( EFF_FROM between ? and ? or "
				+ "VALID_UPTO between ? and ? or "
				+ "EFF_FROM <= ? AND VALID_UPTO >= ?)";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setString(2, pricelist);
				pstmt.setTimestamp(3, effFrom);
				pstmt.setTimestamp(4, validUpto);
				pstmt.setTimestamp(5, effFrom);
				pstmt.setTimestamp(6, validUpto);
				pstmt.setTimestamp(7, effFrom);
				pstmt.setTimestamp(8, validUpto);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					dbEffrom=rs.getTimestamp("eff_from");
					dbValidUpto=rs.getTimestamp("valid_upto");
					dbPricelist=rs.getString("price_list");
					dbitemCode=rs.getString("item_code");
					dbUnit=rs.getString("unit");
					dblistType=rs.getString("list_type");
					dbslabNo=rs.getString("slab_no");
					
					
					//updValidUpto=getPreviousDay(effFrom);
					if(effFrom.after(dbEffrom) && (validUpto.equals(dbValidUpto)|| validUpto.before(dbValidUpto) || validUpto.after(dbValidUpto))  )
					{
						
						updValidUpto=getNextPreviousDay(effFrom,0);
						
						String sqlUpd="Update pricelist set valid_upto = ? where price_list= ? and item_code= ? and unit = ? and list_type = ? and slab_no = ?";
						pstmt1=conn.prepareStatement(sqlUpd);
						pstmt1.setTimestamp(1, updValidUpto);
						pstmt1.setString(2, pricelist);
						pstmt1.setString(3, itemCode);
						pstmt1.setString(4, dbUnit);
						pstmt1.setString(5, dblistType);
						pstmt1.setString(6, dbslabNo);
						int updcnt=pstmt1.executeUpdate();
						
						pstmt1.close();
						pstmt1=null;
						
						if(updcnt>0)
						{
							retFlag = true;
						}
					}
					else if( effFrom.equals(dbEffrom) && (validUpto.equals(dbValidUpto)|| validUpto.before(dbValidUpto)))
					{
						Timestamp updeffFrom=getNextPreviousDay(effFrom,1);
						String sqlUpd="Update pricelist set eff_from = ?  where price_list= ? and item_code= ? and unit = ? and list_type = ? and slab_no = ?";
						pstmt1=conn.prepareStatement(sqlUpd);
						pstmt1.setTimestamp(1, updeffFrom);
						pstmt1.setString(2, pricelist);
						pstmt1.setString(3, itemCode);
						pstmt1.setString(4, dbUnit);
						pstmt1.setString(5, dblistType);
						pstmt1.setString(6, dbslabNo);
						int updcnt=pstmt1.executeUpdate();
						
						pstmt1.close();
						pstmt1=null;
						
						if(updcnt>0)
						{
							retFlag = true;
						}
					}
					else if( effFrom.equals(dbEffrom) && validUpto.after(dbValidUpto) )
					{
						String sqlUpd="Update pricelist set valid_upto = ?, rate = ? where price_list= ? and item_code= ? and unit = ? and list_type = ? and slab_no = ?";
						pstmt1=conn.prepareStatement(sqlUpd);
						pstmt1.setTimestamp(1, validUpto);
						pstmt1.setDouble(2, getDoubleValue(rate));
						pstmt1.setString(3, pricelist);
						pstmt1.setString(4, itemCode);
						pstmt1.setString(5, dbUnit);
						pstmt1.setString(6, dblistType);
						pstmt1.setString(7, dbslabNo);
						int updcnt=pstmt1.executeUpdate();
						
						pstmt1.close();
						pstmt1=null;
						
						if(updcnt>0)
						{
							retFlag = false;
						}
					}
					
				}
				else
				{
					retFlag = true;
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;
				
	
		}
		catch(Exception e)
		{
			e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("retFlag["+retFlag+"]");
		return retFlag;
	}
	
	public Timestamp getNextPreviousDay(Timestamp d1,int pn) throws ITMException
	{
		Timestamp prevDate=null;
		String transformDt;
		try
		{
			SimpleDateFormat appdtfr = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat dbDtfr = new SimpleDateFormat(genericUtility.getDBDateFormat());
			transformDt=appdtfr.format(d1);
			
			Calendar preCalc = Calendar.getInstance();	
			System.out.println("preCalc::"+preCalc);
			preCalc.setTime( getDateObject( transformDt ) );
			if(pn==0)
			{
				preCalc.add( Calendar.DAY_OF_MONTH , -1);
			}
			else
			{
				preCalc.add( Calendar.DAY_OF_MONTH , +1);
			}
			java.util.Date prvDate = preCalc.getTime();
			prevDate=Timestamp.valueOf(dbDtfr.format(prvDate) + " 00:00:00.00");
			
			System.out.println("Source Date["+d1+"] Formatted Date["+prevDate+"]");
			
			
		}
		catch(Exception e)
		{
			e.getMessage();
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return prevDate;
	}

}