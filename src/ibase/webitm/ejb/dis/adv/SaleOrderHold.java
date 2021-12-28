package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;

import org.w3c.dom.*;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless; 
import ibase.webitm.ejb.dis.adv.SaleOrderRelease;;

@Stateless
public class SaleOrderHold extends ActionHandlerEJB implements SaleOrderHoldLocal, SaleOrderHoldRemote
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
		if(tranId!=null && tranId.trim().length() > 0 )
		{
			returnString = holdSaleOrder(tranId,xtraParams,forcedFlag);
		}
		return returnString;
	}	
	public String holdSaleOrder(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("holdSaleOrder called........");
		String sql = "";
		String errString = "" ;
		String status = "",conf = "";
		int rowUpdate  =0;
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		System.out.println("tran id = "+tranId);
		ibase.utility.E12GenericUtility genericUtility= null;
		Timestamp sysdate = null;
		SaleOrderRelease sordRel = null;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			sordRel = new SaleOrderRelease();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			genericUtility =new  ibase.utility.E12GenericUtility();
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			sysdate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");
			//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//sysdate =  java.sql.Timestamp.valueOf(sdf.format(new java.util.Date()).toString() + " 00:00:00.0");
				
			
			if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				//Changes and Commented By Ajay on 22-12-2017:START
				//sql =  " SELECT confirmed, status FROM sorder WHERE sale_order = ? for update ";
			      sql = "SELECT confirmed,(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status FROM sorder WHERE sale_order = ? for update ";  
			}
			else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				//sql =  " SELECT confirmed, status FROM sorder (updlock) WHERE sale_order = ? ";
				sql = "SELECT confirmed,(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status FROM sorder (updlock) WHERE sale_order = ?";
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
				errString = itmDBAccessEJB.getErrorString("","VTCLSHLD","","",conn);
			}
			else if("X".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already cancelled--");
				errString = itmDBAccessEJB.getErrorString("","VTCANHLD","","",conn);
			}
//			else if(!"Y".equalsIgnoreCase(conf.trim()))
//			{
//				System.out.println("-- unconfirmed order --");
//				errString = itmDBAccessEJB.getErrorString("","VTCOCONF3","","",conn);
//			}
			else if("H".equalsIgnoreCase(status.trim()))
			{
				System.out.println("-- ALREADY ON HOLED --");
				errString = itmDBAccessEJB.getErrorString("","VTALHOLD","","",conn);
			}
			else if("P".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--order already on hold--");
				sql = " update sorder set status = 'H',status_date = ? where sale_order =  ? ";
				pstmt  = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,sysdate);
				pstmt.setString(2,tranId);
				rowUpdate = pstmt.executeUpdate(); 
				if(rowUpdate > 0 )
				{
					double lcstatus = 3;
					try
					{
						errString = sordRel.sorderStatusLog(tranId ,sysdate ,lcstatus,xtraParams,"","","","",conn);
						errString = itmDBAccessEJB.getErrorString("","VTHOLD","","",conn);
					}
					catch(Exception ex)
					{
						System.out.println("@@@@@ Exception to calling sorderStatusLog(..)");
						ex.printStackTrace();
					}
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
	
}