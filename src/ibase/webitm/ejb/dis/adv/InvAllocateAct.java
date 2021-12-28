/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :19/11/2005
*/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.utility.CommonConstants;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class InvAllocateAct extends ActionHandlerEJB implements InvAllocateActLocal, InvAllocateActRemote
{
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}
	public String actionHandler(String actionType,String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("InvAllocate called");
		Document dom1 = null;
		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if(actionType.equalsIgnoreCase("SelStock"))
			{
				resString = actionStock(dom1,objContext,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :InvAllocate :actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		System.out.println("returning from action[Stock] actionHandler"+resString);
	    return resString;
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			System.out.println("xmlString ::"+xmlString);
			System.out.println("xmlString1 ::"+xmlString1);
			System.out.println("selDataStr ::"+selDataStr);

			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString);			
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			if(selDataStr != null && selDataStr.trim().length()!=0)
			{
				selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);				
			}
			System.out.println("actionType:"+actionType+":");
						
			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = stockTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :InvAllocateAct :actionHandlerTransform(String xmlString):" +e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from InvAllocateAct : actionHandlerTransform"+retString);
		return retString;
	}

	private String actionStock(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "",finalSql = "";
		String siteCode="", tranId="", itemCode="", qty="", siteCodeQry ="",errCode = "",errString = "";
		String qcReqd = "", qcReqdFn = "", autoQcReqdFn = "", useInvStatus = "", trackShelfLife = "", itemSer = "";
		String itemCodeQry= "", unit ="", locCode ="", lotNo = "", lotSl = "", effDateStr = "", retestDateStr = "";
		String lotSlQry = "",lotSl2 = "", strDimension = "";
		double quantityQry =0;
		double quantity = 0;
		double dimension = 0;
		double rate =0;
		int cnt = 0, itemCnt =0;
		java.sql.Date creaDate = null;
		java.sql.Date ldtDate = null;
		java.sql.Date retestDate = null;
		java.sql.Date retestDateQry = null;
		java.sql.Date effDate = null;
		java.sql.Date expDate = null;		
		boolean ctrFlag1 = false;
		boolean ctrFlag2 = false;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");

		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{		
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			CommonConstants.setIBASEHOME();
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			itemCode = genericUtility.getColumnValue("item_code",dom1);
			qty = genericUtility.getColumnValue("quantity",dom1);
			tranId = genericUtility.getColumnValue("tran_id",dom1);
			ldtDate = new java.sql.Date(System.currentTimeMillis());

			System.out.println("siteCode :"+siteCode+":: itemCode :"+itemCode+":");
			System.out.println("qty :"+qty+":: tranId :"+tranId+":: ldtDate :"+ldtDate+":");
			if(qty != null && qty.trim().length() > 0)
			{
				quantity = Double.parseDouble(qty);
			}
			if(siteCode == null || siteCode.trim().length()== 0)
			{
				errCode = "VTSITECD1";
			}
			else if(itemCode == null || itemCode.trim().length()== 0)
			{
				errCode = "VTITEM1";
			}
			else 
			{
				// check whether any record exists in Header
				// for the same item code
				sql = "SELECT COUNT(*) FROM INV_ALLOC_DET "+
					"WHERE TRAN_ID = '"+tranId+"' "+	
					"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("SQL ::"+sql);
				rs = stmt.executeQuery(sql);
				
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				stmt.close();
				System.out.println("cnt:"+cnt);
				if(cnt == 0)
				{
					
					stmt = conn.createStatement();
					sql="SELECT (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE QC_REQD END) FROM ITEM "+
						" WHERE ITEM_CODE ='"+itemCode+"'" ;
					System.out.println("ITEM :sql:"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
					  qcReqd = rs.getString(1);
					}
					System.out.println("qcReqd :"+qcReqd+":");
					stmt.close();
					if(qcReqd.equalsIgnoreCase("Y"))
					{
						sql ="SELECT COUNT(*) FROM STOCK A, INVSTAT B "+
							 "WHERE A.INV_STAT  = B.INV_STAT "+
							 "AND A.ITEM_CODE ='"+itemCode+"' "+
							 "AND A.SITE_CODE ='"+siteCode+"' "+
							 "AND B.AVAILABLE = 'Y' "+
							 "AND (CASE WHEN ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END)) IS NULL THEN 0 ELSE "+
							 "((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END))END)  > 0 "+
							 "AND ( A.EXP_DATE > ?  OR A.EXP_DATE IS NULL) "+
							 "AND A.RETEST_DATE > ? " ;
						pstmt = conn.prepareStatement(sql);
						System.out.println("SQL ::"+sql);
						System.out.println("Current Date:"+new java.sql.Date(System.currentTimeMillis())+":");
						pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
						pstmt.setDate(2,new java.sql.Date(System.currentTimeMillis()));
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemCnt = rs.getInt(1);
						}
						System.out.println("IF:itemCnt:"+itemCnt);
						pstmt.close();
					}// (qcReqd==Y) if
					else
					{
						stmt = conn.createStatement();
						ldtDate = java.sql.Date.valueOf("1910-01-01");
						sql="SELECT COUNT(*) FROM STOCK A, INVSTAT B"+
							" WHERE A.INV_STAT  = B.INV_STAT"+
							" AND A.ITEM_CODE = '"+itemCode+"'"+
							" AND A.SITE_CODE = '"+siteCode+"'"+
							" AND B.AVAILABLE = 'Y' "+
							" AND (CASE WHEN ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END)) IS NULL THEN 0 ELSE ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END))END)  > 0 ";
						rs = stmt.executeQuery(sql);
						System.out.println("SQL ::"+sql);
						if(rs.next())
						{
							itemCnt = rs.getInt(1);
						}
						System.out.println("ELSE:itemCnt:"+itemCnt);
						stmt.close();

					}//else	(qcReqd==Y)
					if(itemCnt == 0)
					{
						errCode ="VTSTOCK1";						
					}					
					rs.close();
					System.out.println("errCode:"+errCode);
					if(errCode == null || errCode.trim().length() == 0)
					{
						if(itemCode != null && siteCode != null)//if not isnull(ls_dimension) and len(trim(ls_dimension)) > 0 then 
						{
							stmt = conn.createStatement();
							effDate = ldtDate;
							useInvStatus = "";
							retestDate = effDate;
							//qcReqdFn = itmDBAccess.getQcReqd(siteCode,itemCode);
							qcReqdFn = getQcReqd(siteCode,itemCode);
							//autoQcReqdFn = itmDBAccess.getAutoQcReqd(siteCode,itemCode);
							autoQcReqdFn = getAutoQcReqd(siteCode,itemCode);
							System.out.println("FROM Called Functions:qcReqdFn:"+qcReqdFn+":autoQcReqdFn:"+autoQcReqdFn);
							sql="SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END "+
								"FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
							System.out.println("TRACK_SHELF_LIFE:sql:"+sql);
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								trackShelfLife = rs.getString(1);
							}
							System.out.println("trackShelfLife:"+trackShelfLife+":");
							if(qcReqdFn.equalsIgnoreCase("N") || autoQcReqdFn.equalsIgnoreCase("N"))
							{
								retestDate = java.sql.Date.valueOf("1910-01-01");
							}
							if(trackShelfLife.equalsIgnoreCase("N"))
							{
								effDate = java.sql.Date.valueOf("1910-01-01");
							}
							if(CommonConstants.DB_NAME.equals("db2"))
							{
								//effDate = String(effDate,'yyyy-mm-dd') //converting to String
								//ls_retest_date = String(retestDate,'yyyy-mm-dd')
							}
							else
							{
								//ls_effdate = String(effDate,'dd-mmm-yyyy')
								//ls_retest_date = String(retestDate,'dd-mmm-yyyy')
							}
							if(useInvStatus == null || useInvStatus.trim().length() == 0) 
							{
								useInvStatus = "U";
							}
							stmt.close();
							rs.close();
							if(useInvStatus == null || useInvStatus.trim().length() == 0 || useInvStatus.equalsIgnoreCase("U"))
							{
								finalSql="SELECT STOCK.ITEM_SER,STOCK.ITEM_CODE,STOCK.UNIT, " + 
									" STOCK.LOC_CODE,STOCK.LOT_NO,"+
									" STOCK.LOT_SL, " + 
									" STOCK.QUANTITY - STOCK.ALLOC_QTY AS QUANTITY, " + 
									" STOCK.SITE_CODE,STOCK.EXP_DATE,STOCK.RETEST_DATE, " + 
									" STOCK.DIMENSION,STOCK.RATE,STOCK.CREA_DATE, " +
									" CASE WHEN LENGTH(RTRIM(STOCK.LOT_SL)) = 1 THEN '0' || RTRIM(STOCK.LOT_SL) ELSE RTRIM(STOCK.LOT_SL) END "+
									" FROM STOCK,INVSTAT,LOCATION " + 
									" WHERE ( STOCK.LOC_CODE = LOCATION.LOC_CODE ) AND " +
									" ( LOCATION.INV_STAT = INVSTAT.INV_STAT ) AND " + 
									" ( STOCK.ITEM_CODE = '" + itemCode + "' ) AND " + 
									" ( STOCK.SITE_CODE = '" +siteCode + "' ) AND " + 
									" ( STOCK.QUANTITY - STOCK.ALLOC_QTY > 0 ) AND " + 
									" ( INVSTAT.AVAILABLE = 'Y' ) AND" + 
									" ( INVSTAT.STAT_TYPE <> 'S' ) ";
								if(qcReqdFn.equalsIgnoreCase("Y") && autoQcReqdFn.equalsIgnoreCase("Y"))
								{
									retestDateStr = sdf.format(retestDate);
									System.out.println("retestDateStr:"+retestDateStr);
									ctrFlag1 =true; 
									finalSql = finalSql +  " AND ( STOCK.RETEST_DATE IS NULL OR STOCK.RETEST_DATE > ?) " ;
								}
							}//	  useInvStatus==B
							else if(useInvStatus.equalsIgnoreCase("B"))
							{
								finalSql = "SELECT STOCK.ITEM_SER,STOCK.ITEM_CODE,STOCK.UNIT, " + 
										" STOCK.LOC_CODE,STOCK.LOT_NO,STOCK.LOT_SL, "+
										" STOCK.QUANTITY - STOCK.ALLOC_QTY AS QUANTITY, " +
										" STOCK.SITE_CODE,STOCK.EXP_DATE,STOCK.RETEST_DATE, " +
										" STOCK.DIMENSION,STOCK.RATE,STOCK.CREA_DATE, " + 
										" CASE WHEN LENGTH(RTRIM(STOCK.LOT_SL)) = 1 THEN '0' || RTRIM(STOCK.LOT_SL) ELSE RTRIM(STOCK.LOT_SL) END  "+
										" FROM STOCK,INVSTAT "+
										" WHERE ( STOCK.INV_STAT = INVSTAT.INV_STAT ) AND " +
										"( STOCK.ITEM_CODE = '" +itemCode + "' ) AND " +
										"( STOCK.SITE_CODE = '" +siteCode + "' ) AND " + 
										"( STOCK.QUANTITY - STOCK.ALLOC_QTY > 0 ) AND " + 
										"( INVSTAT.STAT_TYPE <> 'S' ) AND " + 
										"  NOT EXISTS ( SELECT 	1 FROM 	QC_ORDER " +
										"	 WHERE 	STOCK.SITE_CODE = QC_ORDER.SITE_CODE " + 
										"	 AND STOCK.ITEM_CODE = QC_ORDER.ITEM_CODE " + 
										"	 AND STOCK.LOT_NO = QC_ORDER.LOT_NO	" + 
										"	 AND STOCK.LOC_CODE = QC_ORDER.LOC_CODE " +
										"	 AND CASE WHEN QC_ORDER.STATUS IS NULL THEN 'U' ELSE QC_ORDER.STATUS END = 'U'"+
										"	 AND (STOCK.LOT_SL = QC_ORDER.LOT_SL OR QC_ORDER.LOT_SL IS NULL )) ";
							}//useInvStatus = "B"
							if(trackShelfLife.equalsIgnoreCase("Y"))
							{
								effDateStr = sdf.format(effDate);
								System.out.println("effDateStr:"+effDateStr);
								ctrFlag2 = true;
								finalSql = finalSql + " AND (STOCK.EXP_DATE IS NULL or STOCK.EXP_DATE > ? ) " ;
							}
							finalSql = finalSql + " ORDER BY NVL(STOCK.EXP_DATE, STOCK.CREA_DATE) ASC, STOCK.LOT_NO ASC, " +
								"STOCK.LOT_SL ASC ";
							
							pstmt = conn.prepareStatement(finalSql);
							
							if(ctrFlag1 == true)
							{
								pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(retestDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							}
							if(ctrFlag2 == true)
							{
								if(ctrFlag1 == true)
								{
									pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(effDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
								}
								else
								{
									pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(effDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
								}
							}
							rs = pstmt.executeQuery();
							System.out.println("Final Sql :: "+finalSql);
							while(rs.next())
							{
								itemSer =rs.getString(1);
								itemCodeQry =rs.getString(2);
								unit =rs.getString(3);
								locCode =rs.getString(4);
								lotNo =rs.getString(5);
								lotSl =rs.getString(6);
								quantityQry =rs.getDouble(7);
								siteCodeQry =rs.getString(8);
								expDate =rs.getDate(9);
								retestDateQry =rs.getDate(10);
								strDimension = rs.getString(11);
								//dimension = rs.getDouble(11);
								rate =rs.getDouble(12);
								creaDate =rs.getDate(13);
								lotSl2 = rs.getString(14);
								System.out.println("itemSer:"+itemSer+":itemCodeQry:"+itemCodeQry+":unit:"+unit+":");
								System.out.println("lotNo:"+lotNo+":lotSl:"+lotSl+":quantityQry:"+quantityQry+":");
								System.out.println("siteCodeQry:"+siteCodeQry+":expDate:"+expDate+":retestDateQry:"+retestDateQry+":");
								System.out.println("strDimension:"+strDimension+":rate:"+rate+":creaDate:"+creaDate+":");
								valueXmlString.append("<Detail>");
								valueXmlString.append("<item_ser>").append("<![CDATA[").append(itemSer== null?"":itemSer).append("]]>").append("</item_ser>");
								valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCodeQry==null?"":itemCodeQry).append("]]>").append("</item_code>");
								valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>");
								valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode == null?"":locCode).append("]]>").append("</loc_code>");
								valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo== null?"":lotNo).append("]]>").append("</lot_no>");
								valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>");
								valueXmlString.append("<quantity>").append("<![CDATA[").append(quantityQry).append("]]>").append("</quantity>");
								valueXmlString.append("<exp_date>").append("<![CDATA[").append((expDate == null) ? "":sdf.format(expDate)).append("]]>").append("</exp_date>");
								valueXmlString.append("<retest_date>").append("<![CDATA[").append((retestDateQry == null) ? "":sdf.format(retestDateQry)).append("]]>").append("</retest_date>");
								valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>");
								valueXmlString.append("<dimension>").append("<![CDATA[").append(strDimension).append("]]>").append("</dimension>");
								valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCodeQry).append("]]>").append("</site_code>");
								valueXmlString.append("<crea_date>").append("<![CDATA[").append((creaDate == null) ? "":sdf.format(creaDate)).append("]]>").append("</crea_date>");
								//valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl== null?"":lotSl.trim()).append("]]>").append("</lot_sl>");
								valueXmlString.append("</Detail>");
							}
						}//dimension 
					}//errCode
				}//cnt
				else
				{
					errCode = "VTITMALOC";
				}				
			}
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("InvAllocate:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : InvAllocate : actionStock " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : InvAllocate : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	public String getQcReqd(String siteCode,String itemCode)throws RemoteException,ITMException
	{
		System.out.println("InvAllocateAct:getQcReqd():"+itemCode+":siteCode:"+siteCode+":");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String qcReqd = "";
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			sql="SELECT CASE WHEN QC_REQD IS NULL THEN 'N' ELSE QC_REQD END FROM SITEITEM "+
				" WHERE ITEM_CODE = '"+itemCode+"'"+
				" AND SITE_CODE ='"+siteCode+"'";
			System.out.println("InvAllocateAct :getQcReqd:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				qcReqd=rs.getString(1);
			}
			else
			{
				sql="SELECT CASE WHEN QC_REQD IS NULL THEN 'N' ELSE QC_REQD END FROM ITEM"+
					" WHERE	ITEM_CODE ='"+itemCode+"'";
				System.out.println("InvAllocateAct :getQcReqd:sql:"+sql);
				rs = stmt.executeQuery(sql); 
				if(rs.next())
				{
					qcReqd=rs.getString(1);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception :InvAllocateAct :getQcReqd:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("InvAllocateAct :getQcReqd:qcReqd:"+qcReqd+":");
		return qcReqd;
	}

	public String getAutoQcReqd(String siteCode,String itemCode)throws RemoteException,ITMException
	{
		System.out.println("InvAllocateAct:getAutoQcReqd():"+itemCode+":siteCode:"+siteCode+":");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String autoReqc = "";
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();	
			sql="SELECT CASE WHEN AUTO_REQC IS NULL THEN 'Y' ELSE AUTO_REQC END FROM SITEITEM"+
				" WHERE SITE_CODE = '"+siteCode+"'  AND ITEM_CODE ='"+itemCode+"'";
			System.out.println("InvAllocateAct :getAutoQcReqd:sql:"+sql);
			rs= stmt.executeQuery(sql);
			if(rs.next())
			{
				autoReqc = rs.getString(1);
			}
			else
			{
				sql=" SELECT CASE WHEN  AUTO_REQC IS NULL THEN 'Y' ELSE AUTO_REQC END FROM ITEM "+
					"  WHERE ITEM_CODE ='"+itemCode+"'";	
				System.out.println("InvAllocateAct :getAutoQcReqd:sql:"+sql);
				rs= stmt.executeQuery(sql);
				if(rs.next())
				{
					autoReqc = rs.getString(1);
				}
			}
	 	}
		catch (Exception e)
		{
			System.out.println("Exception :InvAllocateAct :getAutoQcReqd:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println(":InvAllocateAct :getAutoQcReqd:autoReqc:"+autoReqc+":");
		return autoReqc;
	}

	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		NodeList detailList = null;
		Node currentNode = null;
		int detailListLength = 0;
		String tranId = "",tranDate = "",lineNo = "",siteCode = "",itemCode = "",quantity = "",itemDescr = "";
		String detailQty = "",selLocCode = "",selLotNo = "",selLotSl = "",selquantity = "",errCode = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		double qty = 0,sQuantity = 0;
		double stockQty = 0;
		try
		{
			tranId = new  ibase.utility.E12GenericUtility().getColumnValue("tran_id",dom1);
			tranDate = new  ibase.utility.E12GenericUtility().getColumnValue("tran_date",dom1);

			lineNo = new  ibase.utility.E12GenericUtility().getColumnValue("line_no",dom);
			siteCode = new  ibase.utility.E12GenericUtility().getColumnValue("site_code",dom);
			itemCode = new  ibase.utility.E12GenericUtility().getColumnValue("item_code",dom);
			quantity = new  ibase.utility.E12GenericUtility().getColumnValue("quantity",dom);
			itemDescr = new  ibase.utility.E12GenericUtility().getColumnValue("item_descr",dom);
			if (quantity != null && quantity.trim().length() > 0)
			{
				qty = Double.parseDouble(quantity);
			}
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			for (int ctr = 0;ctr < detailListLength;ctr++)	// Check whether sufficient stock is selected for allocation
			{
				currentNode = detailList.item(ctr);
				detailQty = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity",currentNode);
				if (detailQty != null)
				{
					stockQty = stockQty + Double.parseDouble(detailQty);
				}
			}
			if (stockQty < qty)
			{
				errCode = "VTSTOCK1";  // stock selected is not sufficient for allocation
			}
			// process each selected row
			if (errCode.trim().length() == 0)
			{
				for (int ctr = 0;ctr < detailListLength;ctr++)	// ctr < detailListLength && qty > 0   // qty > 0 commented in PB
				{
					currentNode = detailList.item(ctr);
					selLocCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code",currentNode);	
					selLotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no",currentNode);	
					selLotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl",currentNode);	
					selquantity = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity",currentNode);	
					valueXmlString.append("<Detail>");
					valueXmlString.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>");
					valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(selLocCode).append("]]>").append("</loc_code>");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(selLotNo).append("]]>").append("</lot_no>");
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(selLotSl).append("]]>").append("</lot_sl>");
					sQuantity = Double.parseDouble(selquantity);
					if (sQuantity <= qty)
					{
						// stock quantity in this lot less than required quantity
						valueXmlString.append("<quantity>").append("<![CDATA[").append(sQuantity).append("]]>").append("</quantity>");
					}
					else
					{
						// stock quantity in this lot is more than required quantity
						// allocate the whole lot
						valueXmlString.append("<quantity>").append("<![CDATA[").append(sQuantity).append("]]>").append("</quantity>");
					}
					if (sQuantity <= qty)
					{
						// stock quantity in this lot is less than required quantity
						qty = qty - sQuantity;
					}
					else
					{
						qty = 0;
					}
					valueXmlString.append("</Detail>");
				}
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch (Exception se)
		{
			System.out.println("Exception in InvAllocateAct :: stockTransform ::"+se);
			se.printStackTrace();
			throw new ITMException(se);
		}
		return valueXmlString.toString();
	}
}