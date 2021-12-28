/**
 * VALLABH KADAM 
 * ChargeBackLocCancel.java
 * Req Id:-[D15BSUN003]
 * */

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless;

import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless


public class ChargeBackLocCancel extends ActionHandlerEJB implements ChargeBackLocCancelLocal,ChargeBackLocCancelRemote
{
	int updateCount=0;

	@Override
    public String cancel(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException
    {
	    // TODO Auto-generated method stub
		System.out.println("Inside ChargeBackLocCancel cancel() method");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", sql1 = "";
		ConnDriver connDriver = null;		
		connDriver = null;
		String errString = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		int cnt=0;
		Timestamp sysDate = null;
		E12GenericUtility genericUtility=new E12GenericUtility();
		
		try
        {
	        connDriver = new ConnDriver();
	        //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
	        connDriver = null;
	        conn.setAutoCommit(false);
	        
	        System.out.println("Tran Id fornd by transaction :- ["+tranID+"]");
	        sql = " select  count(*) as cnt from charge_back where tran_id = ? and confirmed = 'Y'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID.trim());
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				cnt  = rs.getInt("cnt");
			}
			rs.close();
			rs = null;			
			pstmt.close();
			pstmt=null;
			if(cnt > 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTALRACNFM","","",conn);
			    return errString;
			}			
//			else
//			{
				/**
				 * Hence the Transaction is not Accepted
				 * Cancel the transaction
				 * Set STATUS='X'
				 * */
				System.out.println("Tran Id fornd by transaction :- ["+tranID+"]");
		        sql = " select  count(*) as cnt from charge_back where tran_id = ? and STATUS = 'X'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID.trim());
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					cnt  = rs.getInt("cnt");
				}
				rs.close();
				rs = null;			
				pstmt.close();
				pstmt=null;
				if(cnt > 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VTALRACNCL","","",conn);
				    return errString;
				}
				else
				{				
				Calendar currentDate = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			    String sysDateStr = sdf.format(currentDate.getTime());
			    System.out.println(">>>>>>>Now sysDateStr :=>  " + sysDateStr);	
			    sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			    System.out.println(">>>>>>>>sysDate:"+sysDate);	
				
				sql = "UPDATE charge_back set status='X',STATUS_DATE=? where tran_id=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, tranID);
				updateCount = pstmt.executeUpdate();				
				pstmt.close();
				pstmt = null;				
				}
				if(updateCount>0)
				{
					if(errString==null || errString.trim().length()<=0)
					{
						errString = itmDBAccessEJB.getErrorString("","VTACNLSUCS","","",conn);
					}					
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTACNLFAIL","","",conn);
				}
//			}
        }
		catch (SQLException e)
        {
	        e.printStackTrace();
	        throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
        } 
		catch (Exception e)
        {
	        e.printStackTrace();
	        throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
        }
		finally 
		{
			try
			{
				if(!conn.isClosed())
				{
					if(updateCount>0)
					{
						conn.commit();
					}
					else
					{
						conn.rollback();
					}
				conn.close();
				}				
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
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		
	    return errString;
    }
}
