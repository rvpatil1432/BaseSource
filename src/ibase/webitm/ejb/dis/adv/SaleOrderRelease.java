package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;

import org.w3c.dom.*;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.system.config.*;

import javax.ejb.Stateless; 

@Stateless
public class SaleOrderRelease extends ActionHandlerEJB implements SaleOrderReleaseLocal, SaleOrderReleaseRemote
{
	static
	{
		System.out.println("-- SalesReturnFormClose called -- ");
	}
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;

		System.out.println(".......tranId......."+tranId);
		System.out.println(".......xtraParams..."+xtraParams);
		System.out.println(".......forcedFlag..."+forcedFlag);
		if(tranId!=null && tranId.trim().length()>0)
		{
			returnString = releaseSaleOrder(tranId,xtraParams,forcedFlag);
		}
		return returnString;
	}	
	public String releaseSaleOrder(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("releaseSaleOrder called........");
		String sql = "";
		String errString = "" ;
		String status = "",conf = "",userId;
		int rowUpdate  =0;
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		System.out.println("tran id = "+tranId);
		ibase.utility.E12GenericUtility genericUtility= null;
		Timestamp sysdate = null;
		String chgTerm = "", chgUser = "";

		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			genericUtility =new  ibase.utility.E12GenericUtility();
			//userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			sysdate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");
			//sysdate =  java.sql.Timestamp.valueOf(sdf.format(new java.util.Date()) + " 00:00:00.0");
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_TERM" );
			chgUser =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_USER" );	
			
			if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				//Changes and Commented By Ajay on 22-12-2017:START
				//sql =  " SELECT confirmed, status FROM sorder WHERE sale_order = ? for update ";
				sql = "SELECT confirmed,(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status FROM sorder WHERE sale_order = ? for update ";
				
			}
			else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				//sql =  " SELECT confirmed, status FROM sorder (updlock) WHERE sale_order = ? ";
				  sql =  "SELECT confirmed,(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status FROM sorder (updlock) WHERE sale_order = ? ";
			}
			else
			{
				//sql = " SELECT confirmed, status FROM sorder WHERE sale_order = ? for update nowait ";
				  sql = "SELECT confirmed,(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status FROM sorder WHERE sale_order = ? for update nowait";
				//Changes and Commented By Ajay on 22-12-2017:END
			}
			pstmt  = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				conf = rs.getString("confirmed");System.out.println("--conf--"+conf);
				status = rs.getString("status");System.out.println("--status--"+status);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			if("C".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already closed--");
				errString = itmDBAccessEJB.getErrorString("","VTCLSREL","","",conn);
			}
			else if("X".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already cancelled--");
				errString = itmDBAccessEJB.getErrorString("","VTCANREL","","",conn);

			}
//			else if(!"Y".equalsIgnoreCase(conf.trim()))
//			{
//				System.out.println("-- unconfirmed order --");
//				errString = itmDBAccessEJB.getErrorString("","VTCOCONF3","","",conn);
//
//			}
			else if("P".equalsIgnoreCase(status.trim()))
			{
				System.out.println("-- ALREADY RELEASED --");
				errString = itmDBAccessEJB.getErrorString("","VTALREL","","",conn);

			}
			else if("H".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--order  on hold currently--");
				sql = " update sorder set status = 'P',status_date = ? where sale_order =  ? ";
				pstmt  = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,sysdate);
				pstmt.setString(2,tranId);
				rowUpdate = pstmt.executeUpdate();
				if(rowUpdate > 0 )
				{
					double lcstatus = 4;
					errString = sorderStatusLog(tranId ,sysdate ,lcstatus,xtraParams,"","","","",conn);
					errString = itmDBAccessEJB.getErrorString("","VTREL","","",conn);
				}
			}
		} 
		catch( Exception e)
		{			
				try 
				{
					conn.rollback(); 
					System.out.println("Exception.. "+e.getMessage());
					e.printStackTrace();	
					errString=e.getMessage();
					throw new ITMException(e);

				} 
				catch (SQLException ex) 
				{
					ex.printStackTrace();
					errString=ex.getMessage();
					throw new ITMException(ex);
				}
		}
		finally
		{		
				try
				{
					if(rowUpdate > 0)
					{
						conn.commit();
						System.out.println("--transaction commited--"+rowUpdate);
							
					}else
					{
						conn.rollback();
						System.out.println("--transaction rollback--");
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
					if(conn != null)
					{
						conn.close();
						conn = null;
					}
				}
				catch(Exception e)
				{
					System.out.println("Exception : "+e);
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
		return errString;
	} //end of  method
	
	public String sorderStatusLog(String tranId, Timestamp tod, double evtype, String xtraParams,String lineno,String explev,String reascode,String refdescr,Connection conn) throws  RemoteException,ITMException
	{
		int lskey = 0;
		Timestamp  todayDate = null ;
		String sql = "",siteCode= "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ibase.utility.E12GenericUtility genericUtility= null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = "" ;
		String generatedtranId = "";
		String chgTerm = "",chgUser = "";
		int rows = 0;
		String edioption = "";
		try
		{
			System.out.println("@@@@@ :: sorderStatusLog :::: called :::: ");
			genericUtility = new  ibase.utility.E12GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_TERM" );
			chgUser =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_USER" );
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			todayDate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");
			sql = "select site_code  from sorder where sale_order = ?" ; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteCode = rs.getString(1);//into :ls_site_code
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			sql = "select count(key_string)  from transetup where tran_window = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"w_sorder_stat_log");
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				lskey = rs.getInt(1) ;
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs =null;
			if(lskey == 0)
			{
				sql = "select count(key_string) from transetup where tran_window = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,"GENERAL");
				rs = pstmt.executeQuery();
				if(rs.next())
				{
				   if(rs.getInt(1) > 0);
				   {
					   System.out.println("-- key_string found in general --");
					   errString = itmDBAccessEJB.getErrorString("","DS000","","",conn);
				   }
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs =null;
			}
			generatedtranId = generateTranId( "w_sorder_stat_log",siteCode, conn );
			sql = " insert into sorder_stat_log values(?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,generatedtranId);
			pstmt.setString(2,tranId);
			pstmt.setTimestamp(3,tod);
			pstmt.setDouble(4,evtype);
			pstmt.setString(5,lineno);
			pstmt.setString(6,explev);
			pstmt.setString(7,reascode);
			pstmt.setString(8,refdescr);
			pstmt.setTimestamp(9,todayDate);
			pstmt.setString(10,chgUser);
			pstmt.setString(11,chgTerm);
		    rows = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
		    if(rows > 0)
		    {
		    	if(tranId != null)
				{
					sql = " select edi_option from transetup where tran_window = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,"w_sorder_stat_log");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						edioption = rs.getString(1);
					    String dataStr = "";
					    if("1".equalsIgnoreCase(edioption.trim()))
					    {
					    	CreateRCPXML createRCPXML = new CreateRCPXML("w_sorder_stat_log","tran_id");
							dataStr = createRCPXML.getTranXML( tranId, conn );
							System.out.println( "dataStr =[ "+ dataStr + "]" );
							Document ediDataDom = genericUtility.parseString(dataStr);
							E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
							String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_sorder_stat_log", "0", xtraParams, conn );
							createRCPXML = null;
							e12CreateBatchLoad = null;
							if( retString != null && retString.indexOf("SUCCESS") > -1 )
							{
								System.out.println("retString from batchload ["+retString+"]");
							}
					    }

					}
					else
					{
						System.out.println(" edi option !found in transetup for w_sorder_stat_log ");
						errString = itmDBAccessEJB.getErrorString("","DS000","","",conn);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
		    }
			else
			{
				System.out.println("-- not insert into sorder_stat_log --");
				errString = itmDBAccessEJB.getErrorString("","DS000","","",conn);
			}
		}
		catch(Exception e)
		{
			try
			{
				if( errString != null && errString.trim().length() >  0 )
				{
					conn.rollback();
					System.out.println("--Transaction rollback in catch--");
				}
				System.out.println("Exception.. "+e.getMessage());
				e.printStackTrace();	
				errString=e.getMessage();
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				errString=e.getMessage();
				throw new ITMException(e1);
			}			
		}
		finally
		{
			try
			{
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
				if(errString != null && errString.trim().length() >  0)
				{
					conn.rollback();
				}else
				{
					conn.commit();
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				errString=e1.getMessage();
				throw new ITMException(e1);
			}	
		}
		
		return errString;
	}
	private String generateTranId( String windowName,String siteCode, Connection conn )throws  RemoteException,ITMException
    {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
	    {

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
					keyString = rs.getString("KEY_STRING");
					keyCol = rs.getString("TRAN_ID_COL");
					tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
         }
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +selSql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
						rs.close();
						rs = null;
				}
				if (pstmt != null)
				{
						pstmt.close();
						pstmt = null;
				}
			}
			catch(Exception e){}
		}
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@tranId[[[[[[[[[[["+tranId+"]]]]]]]]]]]]]]]");
        return tranId;
     }//end of generateTranTd()
}