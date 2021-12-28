/**
 * @author : Akhilesh Sikarwar 
 * @Version : 1.0
 * Date : 03/10/12
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import javax.ejb.Stateless;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;


@Stateless

public class BillofQuantityConf extends ActionHandlerEJB implements BillofQuantityConfLocal,BillofQuantityConfRemote //SessionBean
{
	public String confirm(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		ConnDriver connDriver = null;
		String loginEmpCode = "";
		String confirm = "" ;
		String status = "";
		String errString = "" ; 
		ValidatorEJB validatorEJB = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = null;
		try
		{

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

			sql = "select confirmed ,status from boqhdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirm = rs.getString("confirmed");;
				status = rs.getString("status");
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(confirm != null && confirm.equalsIgnoreCase("Y"))
			{
				System.out.println("The Selected transaction is already confirmed");
				errString = itmDBAccessEJB.getErrorString("","VTMCONF1","","",conn);
				return errString;
			}
			else if(status != null && status.equalsIgnoreCase("X"))				
			{
			
					System.out.println("The Selected transaction is cancle");
					errString = itmDBAccessEJB.getErrorString("","VTINDCONF1","","",conn);
					return errString;
			}		
			
			else /*(confirm != null  && confirm.equalsIgnoreCase("N") && !status.equalsIgnoreCase("C"))*/
			{

					sql = "update boqhdr set confirmed = 'Y', conf_date = ?,emp_code__aprv = ? where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
					pstmt.setString(2, loginEmpCode);
					pstmt.setString(3, tranId);
					int updateCoount = pstmt.executeUpdate();
					System.out.println("no of row update = "+updateCoount);
					pstmt.close();
					pstmt = null;
					if(updateCoount > 0)
					{
						errString = itmDBAccessEJB.getErrorString("","VTCICONF3 ","","",conn);
					}
				
					System.out.println("115 err String from confirm method.....");
			}
		}
		
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			//errString = GenericUtility.getInstance().createErrorString(e);
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(errString != null && errString.trim().length() > 0)
				{
					if(errString.indexOf("VTCICONF3") > -1)
					{
						conn.commit();
					}
					else
					{
						conn.rollback();
					}
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
				conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}//end of confirm method

} //class
