/**
 DEVELOPED BY CHANDRASHEKAR ON 14/05/14 
 PURPOSE: W14BSUN003 (StarClub Employee details.)
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ejb.Stateless;

@Stateless 
public class EmpTravelInfoVisa extends ActionHandlerEJB implements EmpTravelInfoVisaRemote, EmpTravelInfoVisaLocal
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
			System.out.println("Exception in [EmpTravelInfoVisa] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}
	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus ) throws RemoteException,ITMException
	{
		String retString = "",chgUser="";
		String sql = "";
		int count=0,upd=0,count1=0;
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
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
			sql = " select count(*)  from doc_contents where chg_user=? and doc_id in (select doc_id from doc_transaction_link where ref_id  =  ? ) ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, chgUser);
			pstmt.setString(2, tranId);
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
			
			sql = " select count(*) from emp_travel_info where tran_id = ?  and (case when confirmed is null then 'N' else confirmed end = 'Y') ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			if(count == 0 )
			{
				System.out.println("The transaction not confirmed ");
				retString = itmDBAccess.getErrorString("","VTNOTCONF2","","",conn);
				return retString;				
			}
			else
			{	sql = " select count(*) from emp_travel_info where tran_id = ?  and (case when visa_status is null then 'N' else visa_status end = 'Y') ";
				pstmt = conn.prepareStatement(sql);				
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count1 = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				if(count1 > 0 )
				{
					System.out.println("The visa status already submited");
					retString = itmDBAccess.getErrorString("","VTVISACONF","","",conn);
					return retString;	
					
				}
				else
				{					
					sql = " update emp_travel_info set visa_status = 'Y',visa_stat_date= ?  where tran_id  =  ? ";
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
				retString = itmDBAccess.getErrorString("","VTVISASUC","","",conn);
			}			
			else
			{
				retString = itmDBAccess.getErrorString("","VTERRVISA","","",conn);
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
	
}
