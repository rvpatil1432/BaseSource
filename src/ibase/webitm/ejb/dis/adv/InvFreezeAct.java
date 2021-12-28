/* 
	Window Name : w_inv_freeze
	Button Name : (Detail,Alldue)
    Action      : Adding Records  for Detail Count,Alldue. 
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Random;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class InvFreezeAct extends ActionHandlerEJB implements InvFreezeActLocal, InvFreezeActRemote
{
	/* public void ejbCreate() throws RemoteException, CreateException 
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
	} */
	Random rnd = new Random();
    public String confirm() throws RemoteException,ITMException
	{
		return "";
	}

	public String confirm(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		String  resString = null;
		try
		{
			if (actionType.equalsIgnoreCase("AllItem"))
			{
				if(xmlString != null && xmlString.trim().length()!=0)
				{
					ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
					System.out.println("XML String :"+xmlString);
					dom = genericUtility.parseString(xmlString); 
				}
				resString = actionAllItem(dom,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("AllDue"))
			{
				if (xmlString != null && xmlString.trim().length()!=0)
				{
					ibase.utility.E12GenericUtility genericUtlity= new  ibase.utility.E12GenericUtility();
					System.out.println("XML String :"+ xmlString);
					dom = genericUtlity.parseString(xmlString);
				}
				resString = actionAllDue(dom,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("Random"))
			{
				if(xmlString != null && xmlString.trim().length()!=0)
				{
					ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
					System.out.println("XML String :"+xmlString);
					dom = genericUtility.parseString(xmlString); 
				}
				resString = actionRandom(dom,objContext,xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :InvFreeze :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionDetail actionHandler"+resString);
		return resString;
	}

	private String actionAllItem(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String siteCode = "", locCodeFrom = "", locCodeTo = "";
		String itemSerFrom = "", itemSerTo = "";
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Connection conn = null;
		PreparedStatement pStmt1 = null;
		PreparedStatement pStmt2 = null;
		ResultSet rs = null, rs1 = null;
		String errCode = "" ,errString = "", returnValue1 = "";
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			siteCode= genericUtility.getColumnValue("site_code",dom);
			locCodeFrom = genericUtility.getColumnValue("loc_code__from",dom);
			locCodeTo = genericUtility.getColumnValue("loc_code__to",dom);
			itemSerFrom = genericUtility.getColumnValue("item_ser__from",dom);
			itemSerTo = genericUtility.getColumnValue("item_ser__to",dom);
			System.out.println("siteCode ="+siteCode);
			System.out.println("locCodeFrom ="+locCodeFrom);
			System.out.println("locCodeTo ="+locCodeTo);
			System.out.println("ItemSerFrom ="+itemSerFrom);
			System.out.println("ItemSerTo ="+itemSerTo);
			sql = "SELECT A.ITEM_CODE,A.LOC_CODE,A.LOT_NO,A.LOT_SL,A.QUANTITY,A.UNIT "
				 +" FROM STOCK A,INVSTAT B "
				 +" WHERE A.SITE_CODE = ? "
				 +" AND LOC_CODE >= ? "
				 +" AND LOC_CODE <= ? "
				 +" AND ITEM_SER >= ? "
				 +" AND ITEM_SER <= ? "
				 +" AND A.INV_STAT=B.INV_STAT "
				 +" AND B.AVAILABLE='Y' ";
			System.out.println("sql "+sql);
			pStmt1 = conn.prepareStatement(sql);
			pStmt1.setString( 1, siteCode );
			pStmt1.setString( 2, locCodeFrom );
			pStmt1.setString( 3, locCodeTo );
			pStmt1.setString( 4, itemSerFrom );
			pStmt1.setString( 5, itemSerTo );
			rs = pStmt1.executeQuery();
			while (rs.next())
			{
				String itemCode = rs.getString(1);
				
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
				sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = ?";
				pStmt2 = conn.prepareStatement(sql);
				pStmt2.setString( 1, itemCode );
				rs1 = pStmt2.executeQuery();
				if(rs1.next())
				{
					returnValue1 = rs1.getString(1);					
				}
				pStmt2.close();
				pStmt2 = null;
				rs1.close();
				rs1 = null;
				
				valueXmlString.append("<item_descr>").append(returnValue1).append("</item_descr>\r\n");
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(2)).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(4)).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(6)).append("]]>").append("</unit>\r\n");
				valueXmlString.append("</Detail>\r\n");
			}
			pStmt1.close();
			pStmt1 = null;	
			valueXmlString.append("</Root>\r\n");			
		}
		catch(SQLException e)
		{
			System.out.println("Exception : InvFreeze : actionAllRecords " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : InvFreeze : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection........");
				conn.close();
				conn = null;
			}catch(SQLException se){}
		}
		System.out.println("valueXmlString.toString() :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionAllDue(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String siteCode = "";
		String locCodeFrom = "";
		String locCodeTo = "";
		String itemSerFrom = "";
		String itemSerTo = "";
		String tranDate= "";
		String sql = "";
		String returnValue1 = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Connection conn = null;
		PreparedStatement pStmt1 = null;
		PreparedStatement pStmt2 = null;
		ResultSet rs = null, rs2 = null;
		String errCode = "" ,errString = "";
		String cycleCount = null;
		int cycleCnt = 0;
		java.sql.Timestamp phycDate = null;
		java.sql.Timestamp tranDateTs = null;
		//java.util.Date tempDate = null;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		UtilMethods utlMtds = new UtilMethods();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			siteCode= genericUtility.getColumnValue("site_code",dom);
			locCodeFrom = genericUtility.getColumnValue("loc_code__from",dom);
			locCodeTo = genericUtility.getColumnValue("loc_code__to",dom);
			itemSerFrom = genericUtility.getColumnValue("item_ser__from",dom);
			itemSerTo = genericUtility.getColumnValue("item_ser__to",dom);
			tranDate = genericUtility.getColumnValue("tran_date",dom);
			tranDateTs = Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("siteCode ="+siteCode);
			System.out.println("locCodeFrom ="+locCodeFrom);
			System.out.println("locCodeTo ="+locCodeTo);
			System.out.println("ItemSerFrom ="+itemSerFrom);
			System.out.println("ItemSerTo ="+itemSerTo);
			System.out.println("TranDate="+tranDate);
			System.out.println("tranDateTs="+tranDateTs);
			
			sql = "SELECT A.ITEM_CODE,A.LOC_CODE,A.LOT_NO,A.LOT_SL,A.QUANTITY,A.UNIT,A.LAST_PHYC_DATE "
				 +" FROM STOCK A,INVSTAT B "
				 +" WHERE A.SITE_CODE = ? "
				 +" AND LOC_CODE >= ? "
				 +" AND LOC_CODE <= ? "
				 +" AND ITEM_SER >= ? "
				 +" AND ITEM_SER <= ? "
				 +" AND A.INV_STAT=B.INV_STAT "
				 +" AND B.AVAILABLE='Y'";
			System.out.println("sql "+sql);
			pStmt1 = conn.prepareStatement(sql);
			pStmt1.setString( 1, siteCode );
			pStmt1.setString( 2, locCodeFrom );
			pStmt1.setString( 3, locCodeTo );
			pStmt1.setString( 4, itemSerFrom );
			pStmt1.setString( 5, itemSerTo );
			rs = pStmt1.executeQuery();
			while (rs.next())
			{
				String itemCode = rs.getString("ITEM_CODE");
				phycDate = rs.getTimestamp("LAST_PHYC_DATE");
				
				System.out.println("phycDate from rs.getDate is ==>"+phycDate);
				if( phycDate != null )
				{
					sql = "SELECT CYCLE_COUNT FROM ITEM WHERE ITEM_CODE = ?";
					pStmt2 = conn.prepareStatement(sql);
					pStmt2.setString( 1, itemCode );
					rs2 = pStmt2.executeQuery();
					if (rs2.next())
					{
						cycleCount = rs2.getString("CYCLE_COUNT");
						cycleCnt = rs2.getInt("CYCLE_COUNT");
					}
					pStmt2.close();
					pStmt2 = null;
					rs2.close();
					rs2 = null;
					
					if( cycleCount != null && cycleCount.trim().length() > 0 )
					{
						phycDate = utlMtds.RelativeDate( phycDate, cycleCnt );
						System.out.println("phycDate after calculation is ==>"+phycDate);
						if( phycDate.compareTo(tranDateTs) <= 0 )
						{
							valueXmlString.append("<Detail>\r\n");
							valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
							valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
							sql = "SELECT  DESCR FROM ITEM WHERE ITEM_CODE= ?";
							pStmt2 = conn.prepareStatement(sql);
							pStmt2.setString( 1, itemCode );
							rs2 = pStmt2.executeQuery();
							if(rs2.next())
							{
								returnValue1 = rs2.getString(1);
							}
							pStmt2.close();
							pStmt2 = null;
							rs2.close();
							rs2 = null;
							
							valueXmlString.append("<item_descr>").append(returnValue1).append("</item_descr>\r\n");
							valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(2)).append("]]>").append("</loc_code>\r\n");
							valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</lot_no>\r\n");
							valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(4)).append("]]>").append("</lot_sl>\r\n");
							valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</quantity>\r\n");
							valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(6)).append("]]>").append("</unit>\r\n");
							valueXmlString.append("</Detail>\r\n");
						}
					}
				}
			}
			pStmt1.close();
			pStmt1 = null;
			rs.close();
			rs = null;
			
			valueXmlString.append("</Root>\r\n");						
		}
		catch(SQLException e)
		{
			System.out.println("Exception : InvFreeze : actionAllRecords " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : InvFreeze : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection........");
				conn.close();
				conn = null;
			}catch(SQLException se){}
		}
		System.out.println("valueXmlString.toString() :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionRandom(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{	
		String siteItem = "", status= "", itemCode= "";
		String siteCode = "", locCodeFrom = "", locCodeTo = "";
		String ItemSerFrom = "", ItemSerTo = "", tranId = "";
		String lineNo = "", sql1 = "", sql2= "", sql3 = "", sql4 = "", sql5 = "", sql6 = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Connection conn = null;
		Statement stmt1 = null, stmt2 = null, stmt3 = null, stmt4 = null;
		Statement stmt5 = null, stmt6 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null, rs4 = null, rs5 = null, rs6 = null;
		String returnValue1 = "", returnValue2 = "";
		String errCode = "" ,errString = "";
		String detCnt = "0",varValue="";
		PreparedStatement pStmt1 = null;
		boolean bool=false;
		double retries=0;
		int xx,zz,cnt=0, rowId = 0, ItmCount = 0, maxItems=400;
		String stackOptions[]={"1","2"};
		ArrayList site= new ArrayList();
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon dstCmn = new DistCommon();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt1= conn.createStatement();
			tranId= genericUtility.getColumnValue("tran_id",dom);
			System.out.println("tranId :"+tranId);
			lineNo= genericUtility.getColumnValue("line_no",dom);
			System.out.println("lineNo :"+lineNo);
			status= genericUtility.getColumnValue("status",dom);
			System.out.println("status :"+status);
			siteCode= genericUtility.getColumnValue("site_code",dom);
			System.out.println("siteCode :"+siteCode);
			locCodeFrom = genericUtility.getColumnValue("loc_code__from",dom);
			System.out.println("locCodeFrom :"+locCodeFrom);
			locCodeTo = genericUtility.getColumnValue("loc_code__to",dom);
			System.out.println("locCodeTo :"+locCodeTo);
			ItemSerFrom = genericUtility.getColumnValue("item_ser__from",dom);
			System.out.println("ItemSerFrom :"+ItemSerFrom);
			ItemSerTo = genericUtility.getColumnValue("item_ser__to",dom);
			System.out.println("ItemSerTo :"+ItemSerTo);
			if (status.equals("F") || status.equals("U") || status.equals("V"))
			{
				errCode = "";
				errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
				System.out.println("errString :"+errString);
				return errString;
			}
			
			varValue = dstCmn.getDisparams("999999", "PHYCNTITEMS", conn);
			System.out.println("varValue :"+varValue);
			if( varValue != null && varValue.trim().length() > 0 )
			{
				maxItems = Integer.parseInt(varValue);
				System.out.println("maxItems  :"+maxItems);
			}
			
			if (varValue == null || varValue.equals("0"))
			{
				varValue = "0";
				maxItems= 10;
			}
			
			sql1 = "SELECT SITEITEM.SITE_CODE,SITEITEM.ITEM_CODE,ITEM.ITEM_SER FROM SITEITEM,ITEM "
				+" WHERE  SITEITEM.ITEM_CODE = ITEM.ITEM_CODE "
				+" AND SITEITEM.SITE_CODE ='"+siteCode+"' "
				+" AND ITEM.ITEM_SER >='"+ItemSerFrom+"' "
				+" AND ITEM.ITEM_SER <='"+ItemSerTo+"' "
				+" AND ITEM.STK_OPT IN ('1','2')"; 
			System.out.println("sql1="+sql1);
			
			stmt2= conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			rs2=stmt2.executeQuery(sql1);
			int noOfRows = 0;
			while(rs2.next())
			{
				noOfRows++;
			}
			
			//System.out.println("noOfRows :"+noOfRows);
			noOfRows = noOfRows + 1;
			System.out.println("noOfRows + 1 :"+noOfRows);
			for (xx = 1; xx <= maxItems; xx++)
			{
				//retries++;
				//System.out.println("Calling getCount function ==>"+retries);
				getCount( retries, rs2, siteCode, locCodeFrom, locCodeTo, site, noOfRows, conn );
				retries = 0;
			}//end for	
			stmt2.close();
			rs2.close();
			
			xx = site.size();
			System.out.println("Size of ArrayList XX:"+xx);
			if (xx == 0)
			{
				errCode = "VTDETCNT";
				errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
				System.out.println("errString :"+errString);
				return errString;
			}
			for (xx = 0;xx <= maxItems; xx++)
			{
				//System.out.println("Inside for :"+site.size());
				if (xx >= site.size())
				{
					continue;
				}
				itemCode = (String)site.get(xx);
				System.out.println("itemCode :"+itemCode);
				sql4="SELECT STOCK.SITE_CODE,STOCK.QUANTITY,STOCK.LOT_NO,STOCK.LOT_SL,STOCK.ITEM_SER, "
					+"STOCK.LOC_CODE,STOCK.ITEM_CODE,STOCK.UNIT FROM STOCK,ITEM,LOCATION,INVSTAT "
					+"WHERE ITEM.ITEM_CODE = STOCK.ITEM_CODE "
					+"AND LOCATION.LOC_CODE=STOCK.LOC_CODE "
					+"AND LOCATION.INV_STAT = INVSTAT.INV_STAT "
					+"AND INVSTAT.AVAILABLE= 'Y' "
					+"AND STOCK.SITE_CODE='"+siteCode+"' "
					+"AND STOCK.ITEM_CODE='" +itemCode+ "' "
					+"AND STOCK.LOC_CODE >='"+ locCodeFrom +"' "
					+"AND  STOCK.LOC_CODE<= '"+locCodeTo +"'";
				System.out.println("sql "+sql4);
				
				stmt4 = conn.createStatement();
				rs4 = stmt4.executeQuery(sql4);
				while (rs4.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
					valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
					
					sql5 = "SELECT  DESCR FROM ITEM WHERE ITEM_CODE= ?";
					System.out.println("sql5 :"+sql5);
					pStmt1 = conn.prepareStatement(sql5);
					pStmt1.setString( 1, itemCode );
					rs5 = pStmt1.executeQuery();
					//rs5 = stmt1.executeQuery(sql5);
					if(rs5.next())
					{
						returnValue1 = rs5.getString(1);
						System.out.println("returnValue1 :"+returnValue1);
					}
					pStmt1.close();
					pStmt1 = null;
					rs5.close();
					rs5 = null;
					
					valueXmlString.append("<site_descr>").append(returnValue1).append("</site_descr>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(rs4.getDouble(2)).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs4.getString(3)).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs4.getString(4)).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<item_ser>").append("<![CDATA[").append(rs4.getString(5)).append("]]>").append("</item_ser>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs4.getString(6)).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					
					sql6 = "SELECT  DESCR FROM SITE WHERE SITE_CODE= ?";
					System.out.println("sql6 :"+sql6); 
					pStmt1 = conn.prepareStatement(sql6);
					pStmt1.setString( 1, siteCode );
					rs6 = pStmt1.executeQuery();
					//rs6 = stmt1.executeQuery(sql6);
					if(rs6.next()  )
					{
						returnValue2 = rs6.getString(1);
					}
					//stmt1.close();
					pStmt1.close();
					pStmt1 = null;
					rs6.close();
					rs6 = null;
					
					valueXmlString.append("<item_descr>").append(returnValue2).append("</item_descr>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(rs4.getString(8)).append("]]>").append("</unit>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				stmt4.close();
				rs4.close();
				rs4 = null;
			}//for
			
			valueXmlString.append("</Root>\r\n");
		}//try
		catch(SQLException e)
		{
			System.out.println("Exception : InvFreeze : actionAllRecords " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : InvFreeze : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection........");
				conn.close();
				conn = null;
			}catch(SQLException se){}
		}
		System.out.println("valueXmlString.toString() :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	
	private void getCount( double retries, ResultSet rs2, String siteCode, String locCodeFrom, String locCodeTo, ArrayList site, int noOfRows, Connection conn ) throws RemoteException , ITMException
	{
		String itemCode = "";
		String sql = "";
		int cnt = 0;
		ResultSet rs1 = null;
		PreparedStatement pStmt1 = null;
		try
		{
			retries++;
			//System.out.println("retries inside function ==>"+retries);
			
			int rowId = rnd.nextInt(noOfRows);
			//System.out.println("rowId  ==>"+rowId);
			if (rowId == 0)
			{
				rowId = 1;
			}
			rs2.absolute(rowId);
			itemCode = rs2.getString(2);
			//System.out.println("itemCode  ==>"+itemCode);
			boolean itemSelected = false;
			for (int zz = 0; zz < site.size(); zz++)
			{   
				if (site.get(zz).toString().equalsIgnoreCase(itemCode))
				{
					itemSelected = true;
					cnt = 0;
				}
			} 
			
			//System.out.println("itemSelected  ==>"+itemSelected);
			if (!itemSelected)
			{
				sql = "SELECT COUNT(*) FROM STOCK S,INVSTAT ISTAT "
					  +"WHERE S.ITEM_CODE= ? AND S.SITE_CODE= ? "
					  +"AND S.LOC_CODE>= ? AND S.LOC_CODE<= ? "
					  +"AND S.INV_STAT = ISTAT.INV_STAT AND ISTAT.AVAILABLE ='Y'";
				pStmt1 = conn.prepareStatement(sql);
				pStmt1.setString( 1, itemCode );
				pStmt1.setString( 2, siteCode );
				pStmt1.setString( 3, locCodeFrom );
				pStmt1.setString( 4, locCodeTo );
				rs1 = pStmt1.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
					System.out.println("cnt :"+cnt);
				}
				pStmt1.close();
				pStmt1 = null;
				rs1.close();
				rs1 = null;
			}
			//System.out.println("cnt  ==>"+cnt);
			if (cnt >= 1)
			{
				site.add(itemCode);
			}
			else
			{
				//System.out.println("Inside else");
				if (retries > 4)
				{
					//System.out.println("Inside retries > 4");
					site.add("");
				}
				else
				{
					//retries++;
					//System.out.println("Calling getCount from same function");
					getCount( retries, rs2, siteCode, locCodeFrom, locCodeTo, site, noOfRows, conn );
					//System.out.println("After getCount from same function");
				}
			}
			/* for( int a = 0; a < site.size(); a++ )
			{
				System.out.println("Total Site array is ==>"+(String)site.get(a));
			} */
		}
		catch(Exception e )
		{
			System.out.println("Exception : InvFreeze : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);		
		}
	}
}