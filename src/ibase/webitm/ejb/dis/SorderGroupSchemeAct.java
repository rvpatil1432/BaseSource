package ibase.webitm.ejb.dis;

//import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.ejb.Stateless;

@Stateless
public class SorderGroupSchemeAct extends ActionHandlerEJB implements SorderGroupSchemeActRemote, SorderGroupSchemeActLocal 
{	
	E12GenericUtility genericUtility= new  E12GenericUtility();
	UtilMethods utilMethod = new UtilMethods();
	public String actionHandler(String saleOrder, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		StringBuffer msgAppStrinrg = new StringBuffer("");		
		StringBuffer msgNotAppString = new StringBuffer("");
		System.out.println(">>>>>>>>>>>>>>>>>>SorderGroupSchemeAct called>>>>>>>>>>>>>>>>>>>");
		Connection conn = null;
		String sql="", sql1="" , sql2="", sql3="";
		String sql4="", sql5="";		
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		PreparedStatement pstmt3 = null, pstmt4 = null, pstmt5 = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null; // rs4 = null;
		int count = 0, count1 = 0, count2 = 0, count3 = 0;
		String errString = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		double qty = 0.0, rate=0.0;
		
		String schLineNo = "", schItemCode = "", schNature = "", schScheme = "", schFalg = ""; 
		double schQty = 0.0, schRate=0.0;
		String itemCodeOrd = "" ,nature = "" , schAttr1 = "", ordType = "", custCode = "", schmDesc = "", lineNo = "";
		String ordtype = "", schemeCode="", schemeCode1="",itemCodeParent="" , curScheme = "", prevScheme = "", custSchemeCode = "";
		String siteCode="", StateCodeDlv = "", CountCodeDlv="", lstype = "";
		Timestamp ordDate = null;	
		String applyCustList = "", noApplyCustList = "" , applicableOrdTypes = "", mainStr="", msgApplied="", msgNotApplied="", itemCode1="";
		boolean proceed = false;
		String lineNo1 = "", nature1="";
		double qty1 = 0.0, totalQty = 0.0, chargeAmt = 0.0 , freeDiscount = 0.0;
		int no=0;
		
		ArrayList<HashMap> itemList =new ArrayList<HashMap>();
		
		String schemeType = "";
		int PurcBase = 0, SchAllowence = 0;
		double freeQty = 0.0;
		try 
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			
			System.out.println("saleOrder...["+saleOrder+"]");
			if (saleOrder != null && saleOrder.trim().length() > 0) 
			{
				sql = "SELECT B.LINE_NO, B.ITEM_CODE__ORD, B.QUANTITY, B.RATE, B.NATURE, B.SCH_ATTR, A.ORDER_TYPE, A.ORDER_DATE, B.SITE_CODE, A.STATE_CODE__DLV, A.COUNT_CODE__DLV, A.CUST_CODE " +
						"FROM SORDER A, SORDDET B " +
						"WHERE A.SALE_ORDER = B.SALE_ORDER AND B.SALE_ORDER = ? AND B.SCH_ATTR = 'Y' "; 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				while (rs.next()) 
				{
					lineNo = checkNull(rs.getString(1));
					itemCodeOrd = checkNull(rs.getString(2));
					qty = rs.getDouble(3);
					rate  = rs.getDouble(4);
					nature = checkNull(rs.getString(5));
					schAttr1 = checkNull(rs.getString(6));
					ordtype = checkNull(rs.getString(7));
					ordDate = rs.getTimestamp(8);
					siteCode = checkNull(rs.getString(9));
					StateCodeDlv = checkNull(rs.getString(10));
					CountCodeDlv = checkNull(rs.getString(11));
					custCode = checkNull(rs.getString(12));					
					count3 = 0;
					System.out.println("lineNo["+lineNo+"]");
					System.out.println("itemCodeOrd["+itemCodeOrd+"]");
					System.out.println("qty["+qty+"]");
					System.out.println("rate["+rate+"]");
					System.out.println("nature["+nature+"]");
					System.out.println("schAttr1["+schAttr1+"]");
					System.out.println("ordtype["+ordtype+"]");
					System.out.println("ordDate["+ordDate+"]");
					System.out.println("siteCode["+siteCode+"]");
					System.out.println("StateCodeDlv["+StateCodeDlv+"]");
					System.out.println("CountCodeDlv["+CountCodeDlv+"]");
					System.out.println("custCode["+custCode+"]");
					
					//checks the schemes of items from sorddet lineNo wise.
					sql1 = "SELECT A.SCHEME_CODE FROM SCHEME_APPLICABILITY A,  SCHEME_APPLICABILITY_DET B " +
							"WHERE A.SCHEME_CODE = B.SCHEME_CODE AND (A.ITEM_CODE    = ? OR A.PROD_SCH = 'Y') " +
							"AND A.APP_FROM <= ? AND A.VALID_UPTO >= ? " +
							"AND (B.SITE_CODE = ? OR B.STATE_CODE = ? OR B.COUNT_CODE = ? ) ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, itemCodeOrd);
					pstmt1.setTimestamp(2, ordDate);
					pstmt1.setTimestamp(3, ordDate);
					pstmt1.setString(4, siteCode);
					pstmt1.setString(5, StateCodeDlv);
					pstmt1.setString(6, CountCodeDlv);
					rs1 = pstmt1.executeQuery();
					while (rs1.next())
					{
						schemeCode = checkNull(rs1.getString("scheme_code"));
						System.out.println("schemeCode...["+schemeCode+"]");
						
						if (schemeCode != null && schemeCode.trim().length() > 0)
						{	
//-----------------------------------------------------------------------------------							
/*							sql2 = "select item_code__parent from item where item_code =?";
							pstmt2 = conn.prepareStatement(sql2);
							pstmt2.setString(1, itemCodeOrd);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								itemCodeParent = checkNull(rs2.getString("item_code__parent"));
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;
							int no12 = 0;
							System.out.println("itemCodeParent::["+itemCodeParent+"]");
							if (itemCodeParent == null || itemCodeParent.trim().length() == 0)
							{
								sql3 = "select count(1) as cnt from item where item_code__parent =?";
								pstmt3 = conn.prepareStatement(sql3);
								pstmt3.setString(1, itemCodeOrd);
								
								rs3 = pstmt3.executeQuery();
								if (rs3.next())
								{
									no12 = rs3.getInt("cnt");
								}
								rs3.close();
								rs3 = null;
								pstmt3.close();
								pstmt3 = null;
							}
							System.out.println("no12 is..["+no12+"]");
							if (no12 > 0)
							{
								break;
							}

						}
						int no12 = 0 ;
						sql2 = "Select count(1) as cnt From scheme_applicability A, bom b Where A.scheme_code = b.bom_code And B.bom_code= ?" + " And(? between case when b.min_qty is null then 0 else b.min_qty end" + " And case when b.max_qty is null then 0 else b.max_qty end) and B.promo_term is null";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, schemeCode);
						pstmt2.setDouble(2, qty);
						rs2 = pstmt2.executeQuery();
						if (rs1.next())
						{
							no12 = rs1.getInt("cnt");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
							
						System.out.println("no12 from scheme_applicability..["+no12+"]");
						if(no12 == 0)
							continue;*/
						
						sql2 = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as ls_apply_cust_list, "
							+ " (case when noapply_cust_list is null then ' ' else noapply_cust_list end) as ls_noapply_cust_list, order_type "
							+ " from scheme_applicability where scheme_code = ? ";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, schemeCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							applyCustList = checkNull(rs2.getString("ls_apply_cust_list"));
							noApplyCustList = checkNull(rs2.getString("ls_noapply_cust_list"));
							applicableOrdTypes = checkNull(rs2.getString("order_type"));
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						System.out.println("applyCustList["+applyCustList+"]noApplyCustList["+noApplyCustList+"][applicableOrdTypes["+applicableOrdTypes+"]");
						System.out.println("ordType..["+ordType+"]");

						if ("NE".equalsIgnoreCase(ordtype) && (applicableOrdTypes == null || applicableOrdTypes.trim().length() == 0))
						{
							// goto Nextrec
						} 
						else if (applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
						{	
							System.out.println("PAVAN R Inside If *****applicableOrdTypes***");
							proceed = false;
							String applicableOrdTypesArr[] = applicableOrdTypes.split(",");
							System.out.println("applicableOrdTypesArr Length::["+applicableOrdTypesArr.length+"]");
							ArrayList<String> appliOrdTyplist= new ArrayList<String>(Arrays.asList(applicableOrdTypesArr));
							
							if(appliOrdTyplist.contains(ordType))
							{
								System.out.println("lbProceed"+proceed);
								proceed = true;
								//break;
							}
							if (!proceed)
							{
								System.out.println("Inside lbproceed");
								continue;
								// goto Nextrec
							}
						}			
						schemeCode1 = schemeCode;
						schemeCode = schemeCode1;
						System.out.println("250@@@ schemeCode["+schemeCode+"]");
						if (applyCustList.trim().length() > 0)
						{
							schemeCode = null;
							System.out.println("schemeCode:::::::1"+schemeCode);
							//lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
							System.out.println("custCode"+custCode);
							String noapplyCustListArr[] = applyCustList.split(",");
							ArrayList<String> applyCustList1 = new ArrayList<String>(Arrays.asList(noapplyCustListArr));
							
							if(applyCustList1.contains(custCode.trim()))
							{
								System.out.println("Inside applycustList1.........");
								schemeCode = curScheme;
								custSchemeCode = curScheme;
								System.out.println("schemeCode::"+schemeCode+" custSchemeCode::"+curScheme);

								//					break;
							}
							System.out.println("applyCustList1...["+applyCustList1+"]");
						}
						if (noApplyCustList.trim().length() > 0 && schemeCode != null)
						{
							//custCode12 = checkNull(genericUtility.getColumnValue("cust_code", dom));
							String noApplyCustListArr[] = noApplyCustList.split(",");
							ArrayList<String> noapplyCustList= new ArrayList<String>(Arrays.asList(noApplyCustListArr));
							if(noapplyCustList.contains(custCode))//cusstCode12 from dom
							{
								schemeCode = "";
								break;
							}
						}	
						if ( schemeCode != null)
						{
							count3 ++;
						} else if (count3 == 1)
						{
							schemeCode = schemeCode1;
							System.out.println("lsSchemeCode264:::::::"+schemeCode);
						}
						// Nextrec:
						// fetch next curscheme into :ls_curscheme;
						
//-----------------------------------------------------
								
						}else
						{
							System.out.println("Scheme not found for Item::["+itemCodeOrd+"]");
						}
						
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("outside while..schemeCode..["+schemeCode+"]");
					
					if (count3 > 1)
					{
						schemeCode = "";
					} 
					
					//---IF SCHEME CODE IS GREATER THAN 0 START
					
					if(schemeCode!= null && schemeCode.trim().length() > 0)
					{
						//To check free item against SchemeCode
						sql2 = "SELECT COUNT(*) FROM SCH_OFFER_ITEMS WHERE SCHEME_CODE = ? and ITEM_CODE = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, schemeCode);
						pstmt2.setString(2, itemCodeOrd);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							count2 = rs2.getInt(1);
							System.out.println("sch _off-1 count2--["+count2+"]");
						}
						System.out.println("sch _off-2 count2--["+count2+"]");
						if (count2 > 0) 
						{
							HashMap schemItemMap = new HashMap();
							
							schemItemMap.put("lineNo", lineNo);
							schemItemMap.put("itemCode", itemCodeOrd);
							schemItemMap.put("scheme", schemeCode);
							schemItemMap.put("qty", qty);
							schemItemMap.put("rate", rate);
							schemItemMap.put("nature", nature);
							schemItemMap.put("schemeNature", "F");								
							itemList.add(schemItemMap);
						
						}
						count2 = 0;
						sql2 = "SELECT COUNT(*) FROM SCH_PUR_ITEMS WHERE SCHEME_CODE = ? AND ITEM_CODE = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, schemeCode);
						pstmt2.setString(2, itemCodeOrd);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							count2 = rs2.getInt(1);
							System.out.println("sch_pur--1 count2--["+count2+"]");
						}
						System.out.println("sch_pur--2 count2--["+count2+"]");
						if (count2 > 0) 
						{
							HashMap schemItemMap = new HashMap();
							
							schemItemMap.put("lineNo", lineNo);
							schemItemMap.put("itemCode", itemCodeOrd);
							schemItemMap.put("scheme", schemeCode);
							schemItemMap.put("qty", qty);
							schemItemMap.put("rate", rate);
							schemItemMap.put("nature", nature);
							schemItemMap.put("schemeNature", "C");								
							itemList.add(schemItemMap);
							
						}						
						
					}
					//--------------------------------------
					
						/*//To Get Offer aginst scheme
						sql3 = "SELECT DESCR, SCHEME_TYPE, PURC_BASE, SCH_ALLOWENCE FROM SCH_GROUP_DEF where SCHEME_CODE= ?";
						pstmt3 = conn.prepareStatement(sql3);
						pstmt3.setString(1, schemeCode);
						rs3 = pstmt3.executeQuery();
						if (rs3.next()) 
						{
							schmDesc = checkNull(rs3.getString(1));
							schemeType = checkNull(rs3.getString(2));
							PurcBase = rs3.getInt(3);
							SchAllowence = rs3.getInt(4);
						}
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;
						System.out.println("schmDesc"+schmDesc+"]");	*/	
					//abouve sql added in if schNature='F'
						
					/*	sql3 ="select count (*) as cnt from sch_pur_items  where SCHEME_CODE =? and item_code=?";
						pstmt3 = conn.prepareStatement(sql3);
						pstmt3.setString(1, schemeCode);
						pstmt3.setString(2, itemCodeOrd);
						int count5 = 0;
						rs3 = pstmt3.executeQuery();

						if (rs3.next())
						{
							count5 = rs3.getInt(1);
						}
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;	
						if (count5 == 0)
						{
							schemeCode = "";
							
						}*/
					/*
					if(!("Y".equalsIgnoreCase(schAttr)))
					{
						errString = itmDBAccessLocal.getErrorString("", "VTMCONF20", "");
						return errString;
					}else if(!"P".equalsIgnoreCase(""))
					{
						errString = itmDBAccessLocal.getErrorString("", "VTSOSTAT", "Sales order is not in pending status");
						return errString;
					}	*/
				}	//sorder/sorddet while end
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//ArrayList<String>
				System.out.println("itemList::[~"+itemList+"~]");				
				double freeTotQty = 0.0, chrgbleTotQty = 0.0;
				
				for (HashMap  schemItemMap: itemList) 
				{						  
					schLineNo = (String)schemItemMap.get("lineNo");
					schItemCode = (String)schemItemMap.get("itemCode");
					schScheme = (String)schemItemMap.get("scheme");
					schQty = (Double)schemItemMap.get("qty");
					schRate = (Double)schemItemMap.get("rate");
					schNature = (String)schemItemMap.get("nature");
					schFalg = (String)schemItemMap.get("schemeNature");		
					System.out.println("schemItemMap[-"+schLineNo+"-"+schFalg+"-"+schItemCode+"-"+schScheme+"-"+schQty+"-"+schRate+"-"+schNature);					

					if ("F".equalsIgnoreCase(schFalg))
					{
						
						freeTotQty = freeTotQty+schQty;
						
					}else if("C".equalsIgnoreCase(schFalg)){
						chrgbleTotQty = chrgbleTotQty + schQty;
					}
					System.out.println("freeTotQty["+freeTotQty+"]--chrgbleTotQty["+chrgbleTotQty+"]");
				}
				
				//Check If Items are chargable but offer item not selected then Error Msg
				if(chrgbleTotQty > 0 && freeTotQty== 0)
				{
					for (HashMap  schemItemMap: itemList) 
					{						  
						schLineNo = (String)schemItemMap.get("lineNo");
						schItemCode = (String)schemItemMap.get("itemCode");
						schScheme = (String)schemItemMap.get("scheme");
						schQty = (Double)schemItemMap.get("qty");
						schRate = (Double)schemItemMap.get("rate");
						schNature = (String)schemItemMap.get("nature");
						schFalg = (String)schemItemMap.get("schemeNature");		
						System.out.println("schemItemMap[-"+schLineNo+"-"+schFalg+"-"+schItemCode+"-"+schScheme+"-"+schQty+"-"+schRate+"-"+schNature);					

						//Scheme applied msg
						mainStr="Line No:"+schLineNo+" Item Code:"+schItemCode+" Scheme:"+schScheme+"\n";
						msgNotAppString = msgNotAppString.append(mainStr);
						System.out.println("--msgNotAppString--"+msgNotAppString);

					}
				}
					//Checks scheme type and description
					sql3 = "SELECT DESCR, SCHEME_TYPE, PURC_BASE, SCH_ALLOWENCE FROM SCH_GROUP_DEF where SCHEME_CODE= ?";
					pstmt3 = conn.prepareStatement(sql3);
					pstmt3.setString(1, schScheme);
					rs3 = pstmt3.executeQuery();
					if (rs3.next()) 
					{
						schmDesc = checkNull(rs3.getString(1));
						schemeType = checkNull(rs3.getString(2));
						PurcBase = rs3.getInt(3);
						SchAllowence = rs3.getInt(4);
					}
					rs3.close();
					rs3 = null;
					pstmt3.close();
					pstmt3 = null;
					System.out.println("schmDesc"+schmDesc+"]");	
					
					//update value from map
					for (HashMap  schemItemMap: itemList) 
					{						  
						schLineNo = (String)schemItemMap.get("lineNo");
						schItemCode = (String)schemItemMap.get("itemCode");
						schScheme = (String)schemItemMap.get("scheme");
						schQty = (Double)schemItemMap.get("qty");
						schRate = (Double)schemItemMap.get("rate");
						schNature = (String)schemItemMap.get("nature");
						schFalg = (String)schemItemMap.get("schemeNature");		
						System.out.println("schemItemMap[-"+schLineNo+"-"+schFalg+"-"+schItemCode+"-"+schScheme+"-"+schQty+"-"+schRate+"-"+schNature);					

						if ("F".equalsIgnoreCase(schFalg))
						{		
							//freeTotQty = freeTotQty+schQty;
							if("0".equalsIgnoreCase(schemeType))
							{
								//freeQty = ((qty * SchAllowence) / PurcBase);
								freeQty = ((chrgbleTotQty * SchAllowence) / PurcBase);
								if(freeQty > freeTotQty && freeTotQty > 0 )
								{
									//Scheme applied msg
									mainStr="\n\n Some of the Scheme Items, Offer not selected.\n" +
											"Eligible Free Quantity:"+freeQty+" only.\n"+
											"Total Free Quantity:"+freeTotQty+"\n";
									msgAppStrinrg = msgAppStrinrg.append(mainStr);
									System.out.println("--msgNotAppString--"+msgAppStrinrg);
								}
							}
							else if("1".equalsIgnoreCase(schemeType))
							{
								chargeAmt = (chrgbleTotQty * rate);
								if(chargeAmt >= PurcBase)
								{
									freeQty = roundValue(((((chrgbleTotQty * rate)* SchAllowence) / PurcBase)), 0);
								}
										
							}else if("2".equalsIgnoreCase(schemeType))
							{
								chargeAmt = (chrgbleTotQty * rate);
								if(chargeAmt >= PurcBase)
								{
									freeDiscount = SchAllowence;
								}		
							}
								if("2".equalsIgnoreCase(schemeType))
								{
									sql4 = "UPDATE SORDDET SET DISCOUNT = ? WHERE SALE_ORDER = ? AND LINE_NO = ?";
									 pstmt4 = conn.prepareStatement(sql4);
									 pstmt4.setDouble(1, freeDiscount);
									 pstmt4.setString(2, saleOrder);
									 pstmt4.setString(3, lineNo1);
									 no = pstmt4.executeUpdate();
									 System.out.println("419...no...1..["+no+"]");
									 pstmt4.close();
									 pstmt4 = null;
									 
									 if(no > 0)
									 {
										 sql5 = "UPDATE SORDDET SET SPEC_REASON='P' WHERE SALE_ORDER = ? AND LINE_NO = ?";
										 pstmt5 = conn.prepareStatement(sql5);
										 pstmt5.setString(1, saleOrder);
										 pstmt5.setString(2, lineNo);
										 int nos2 = pstmt5.executeUpdate();
										 System.out.println("452...nos2...["+nos2+"]");
										 pstmt5.close();
										 pstmt5 = null;
										 
										 System.out.println("totalQty::["+totalQty+"]");
											//Scheme applied msg
											mainStr="\nLine No"+lineNo+" Item Code:"+itemCodeOrd+" Scheme:"+schScheme; //+" Description:"+schmDesc;
											msgAppStrinrg = msgAppStrinrg.append(mainStr);
											System.out.println("--msgString--"+msgAppStrinrg);
									 }									 
								}
								System.out.println("schQty:["+schQty+"]<= freeQty["+freeQty+"]");
								System.out.println("totalQty:["+totalQty+"]<= freeQty["+freeQty+"]");
								if(schQty <= freeQty && totalQty < freeQty)
								{
									 sql4 = "UPDATE SORDDET SET NATURE = 'F', RATE = 0, RATE__STDUOM = 0 WHERE SALE_ORDER = ? AND LINE_NO = ?";
									 pstmt4 = conn.prepareStatement(sql4);
									 pstmt4.setString(1, saleOrder);
									 pstmt4.setString(2, schLineNo);
									 no = pstmt4.executeUpdate();
									 System.out.println("471...no...["+no+"]");
									 pstmt4.close();
									 pstmt4 = null;									 
								}
								totalQty = totalQty + schQty;
								System.out.println("@@@516 ToatlQty:"+totalQty);
								if(totalQty == freeQty)
								{
									//break;
								}
								System.out.println("totalQty::["+totalQty+"]");
							
								/*//Scheme applied msg
								String mainStr="Line No:"+lineNo+" Item Code:"+itemCodeOrd+" Scheme:"+schemeCode+" Description:"+schmDesc;
								msgString = msgString.append(mainStr);
								System.out.println("--msgString--"+msgString);
	*/								
								if(totalQty == freeQty)
								{
									//continue;
								}								
						
							
						}else if("C".equalsIgnoreCase(schFalg)){
							chrgbleTotQty = chrgbleTotQty + schQty;
						}
						
					}
					//upadate value from map end
						//To Check offer againts Scheme code...
						/*sql3 = "SELECT count(*) FROM SORDDET WHERE ITEM_CODE IN " +
								"(SELECT ITEM_CODE FROM SCH_OFFER_ITEMS WHERE SCHEME_CODE = ?) AND SCH_ATTR = 'Y' AND RATE != 0 AND SALE_ORDER = ? ";
						pstmt3 = conn.prepareStatement(sql3);
						pstmt3.setString(1, schScheme);
						pstmt3.setString(2, saleOrder);
						rs3 = pstmt3.executeQuery();
						if (rs3.next()) 
						{
							count = rs3.getInt(1);
						}						
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;
						System.out.println("schemeType::417::["+schemeType+"]");
						System.out.println("count...["+count+"]");
						if (count == 0) 
						{
							mainStr="Line No:"+lineNo+" Item Code:"+itemCodeOrd+" Scheme:"+schScheme;
							msgNotAppString = msgNotAppString.append(mainStr);
							System.out.println("--msgNotAppString--"+msgNotAppString);						
						}
						else
						{*/						
							
							
														
							//errString = msgString+"\n~~~Scheme Item But Offer Not Selected.~~~\n"+msgString;
						
					

					
					//errString = msgString+"\n~~~Scheme Item But Offer Not Selected.~~~\n"+msgString;
				//mainStr="Scheme Not Applied On Following item:\nLine No:"+lineNo+" Item Code:"+itemCodeOrd+" Scheme:"+schemeCode+" Description:"+schmDesc;
				errString = itmDBAccessLocal.getErrorString("", "VTNOOFSCH", "");
				String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
				String endPart = errString.substring( errString.indexOf("</trace>"));
				//mainStr="Line NO: "+lineNo+"\n Item Code: "+itemCodeOrd+"\n Scheme: "+schemeCode+"\n Description: "+schmDesc;
				//mainStr=begPart+msgString;
				if(msgNotAppString != null && msgNotAppString.length() > 0)
				{
					msgNotApplied="Following Scheme Item, Offer item not selected :";
				}
				else
				{
					msgNotApplied = " ";
				}
				if(msgAppStrinrg != null && msgAppStrinrg.length() > 0)
				{
					msgApplied="Scheme Applied on items :";
				}else
				{
					msgApplied = " ";
				}
				System.out.println("C"+msgNotAppString+"]");
				System.out.println("msgAppStrinrg-----["+msgAppStrinrg+"]");
				String mainString = begPart+"\n"+msgNotApplied+"\n"+checkNull(msgNotAppString.toString())+"\n"+msgApplied+"\n"+checkNull(msgAppStrinrg.toString());
				if(mainString.trim().length()==0)
				{
					mainStr = begPart;
				}
				mainString = mainString +  endPart;	
				System.out.println("mainString...["+mainString+"]");
				errString = mainString;
				System.out.println("----------["+errString+"]");
				return errString;
			}//end if saleOrder != null
			
		} catch (Exception e) 
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{		
			try
			{	
				conn.commit();
				System.out.println("-- commited--");
					
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				if(rs != null) 
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}
	private double roundValue(double round, int scale)
	{
		return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
	}
	
}


