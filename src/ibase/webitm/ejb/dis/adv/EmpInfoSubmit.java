/**
 DEVELOPED BY RITESH TIWARI ON 10/04/14 
 PURPOSE: WS3LSUN003 (StarClub Employee details.)
 */
package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.Stateless;

@Stateless 
public class EmpInfoSubmit extends ActionHandlerEJB implements EmpInfoSubmitRemote, EmpInfoSubmitLocal
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String retString = "";
		Connection conn = null;
		boolean isConn= false;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn, isConn);
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [StockTransferConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}
	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus ) throws RemoteException,ITMException
	{
		String retString = "",status="";
		String sql = "";
		int count=0,upd=0;
		String refSer = "";
		String confirmed = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ibase.utility.E12GenericUtility genericUtility= null;
		ITMDBAccessEJB itmDBAccess = null;
		try
		{
			genericUtility = new  ibase.utility.E12GenericUtility();
			itmDBAccess = new ITMDBAccessEJB();
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			connStatus = true;
			
			sql = " SELECT ref_ser FROM TRANSETUP WHERE TRAN_WINDOW = 'w_emp_travel_info' ";
			pstmt = conn.prepareStatement( sql );
			System.out.println( "@@@@@ sqlgetdata :: [[" + sql+"]]" );
			rs = pstmt.executeQuery(  );
			if(rs.next())
			{
				refSer = rs.getString("ref_ser");
			}
			
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println( "@@@@@ refSer :: [[" + refSer+"]]" );
			
			sql = " select count(*)  from doc_contents where doc_id in (select doc_id from doc_transaction_link where ref_id  =  ? and ref_ser = ? ) ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2, refSer);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(count == 0)
			{
				System.out.println("No attachment found !");
				retString = itmDBAccess.getErrorString("","VTNOATCH1","","",conn);
				return retString;
			}
			//Changes done by Chandrashekar on 15-may-2014
			sql = " select status,confirmed from emp_travel_info where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				status = rs.getString("status");
				confirmed = rs.getString("confirmed");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			if("X".equalsIgnoreCase(status))
			{	System.out.println("Travel information Cancelled");
				retString = itmDBAccess.getErrorString("","VTNOTSUB","","",conn);
				return retString;
			}else if("Y".equalsIgnoreCase(confirmed))
			{	System.out.println("Travel information already confirmed");
				retString = itmDBAccess.getErrorString("","VTCNF","","",conn);
				return retString;
			}
			else
			{
				sql = " select count(*) from emp_travel_info where tran_id = ?  and (case when status is null then 'N' else status end = 'S') ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				if(count > 0 )
				{
					System.out.println("The transaction already submited");
					retString= "VTCONF8";
				}
				else
				{
					sql = " update emp_travel_info set status = 'S' , status_date = ?  where tran_id  =  ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, new java.sql.Timestamp( System.currentTimeMillis() ) );
					pstmt.setString(2, tranId);
					upd =  pstmt.executeUpdate();
					pstmt.close();pstmt = null;
					System.out.println(" status updated:: "+upd);
					if(upd > 0)
					{
						retString = "submited";
						System.out.println("  information submited::");
					}
				}
				
			}
			//End Changes done by Chandrashekar on 15-may-2014
		
		}
		catch( Exception e )
		{
			try
			{
				conn.rollback();
				retString = e.getMessage();
				e.printStackTrace();
			}
			catch (Exception e1)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
			}
			throw new ITMException(e);
		}
		finally
		{
			if(retString != null && retString.indexOf("submited") > -1)
			{
				try {
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				retString = itmDBAccess.getErrorString("","VTSUB1","","",conn);
			}
			else if(retString != null && retString.indexOf("VTCONF8") > -1)
			{
				retString = itmDBAccess.getErrorString("","VTALSUB1","","",conn);
			}
			else
			{
				retString = itmDBAccess.getErrorString("","VTERRSUB","","",conn);
			}
			try{
				
			if(rs != null)
			{
				rs.close();rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();pstmt = null;
			}
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
			}
			catch(Exception e)
			{System.out.println("Exception : "+e);e.printStackTrace();}
			
		}

		
		return retString ;
	}
	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
}
