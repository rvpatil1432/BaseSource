/**
 * PURPOSE : To cancel of bill ofquantity 
 * AUTHOR : Done  by akhilesh on 22/04/13
 */

package ibase.webitm.ejb.dis;


import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.*;

import javax.ejb.Stateless;


@Stateless
public class BoqCancel extends ActionHandlerEJB implements BoqCancelLocal, BoqCancelRemote
{
	/**
	 * The public method is used for splitting the sales return form
	 * Returns splitted message on successfull splitting otherwise returns error message
	 * @param tranId is the transaction id to be splitted
	 * @param xtraParams contais additional information such as loginEmpCode,loginCode,chgTerm etc
	 * @param forcedFlag (true or false)
	 */
	String userId = "", termId = "";
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		System.out.println("Cancel  called........");
		String sql = "";
		String status = "";
		String postatus = "";
		String siteCode = "";
		String errString = "" ;
		String loginEmpCode = "";
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0,cntOrder=0;

		//GenericUtility genericUtility = null;
		E12GenericUtility genericUtility= null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		ValidatorEJB validatorEJB = null;
		System.out.println("tran id = "+tranId);

		try
		{
			
			 genericUtility= new  E12GenericUtility();
			//genericUtility = new GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			loginEmpCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");

			
			sql = "select count(*) from porder where TRAN_ID__BOQ  = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
		     cnt = rs.getInt(1);
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
			
		System.out.println("cnt value "+cnt);
			
		if(cnt > 0)
			{
			sql = "select count(*) from porder where TRAN_ID__BOQ  = ? and case when status is null then 'O' else status end ='X'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
		     cntOrder = rs.getInt(1);
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cntOrder==0)
			{
				errString = itmDBAccessEJB.getErrorString("","VMBOQCNL","","",conn);
				return errString;
			}
			
				/*sql = "select status from porder where TRAN_ID__BOQ  = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					postatus = rs.getString("status");
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(postatus == null || !postatus.equalsIgnoreCase("X"))
				{*/
					
				//}
				
			}
		
		
			System.out.println("postatus value "+postatus);
			
			
						
			
			sql = "select status from boqhdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				status = rs.getString("status");
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(status != null  && status.equalsIgnoreCase("X"))
			{
				System.out.println("The Selected transaction is already Cancel");
				errString = itmDBAccessEJB.getErrorString("","VMBOQCAN","","",conn);
				return errString;
			}
			else
			{
					sql = "update boqhdr set status = 'X', status_date = ? where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
					pstmt.setString(2, tranId);
					int updateCoount = pstmt.executeUpdate();
					System.out.println("no of row update = "+updateCoount);
					pstmt.close();
					pstmt = null;
					if(updateCoount > 0)
					{
					if(cntOrder>0)//Condition Added by Manoj dtd 09/03/2016 to make tran_id__boq blank in porder
					{
						sql="update porder set TRAN_ID__BOQ='' where TRAN_ID__BOQ=?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,tranId);
						pstmt.executeUpdate();
						pstmt.close();
						pstmt=null;
						
					}
						errString = itmDBAccessEJB.getErrorString("","VTCANC1","","",conn);
					}
			}
			
			System.out.println("115 err String from Cancel method.....");	
					
		}

		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch (Exception e1)
			{
				throw new ITMException(e1);
			}
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{

				conn.commit();

				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::"+errString);
		return errString;
	}
}

	