/* 
 * Created 31 December 2005
 * @Author Hatim Laxmidhar
 * Perpose : This  Cancel the Created Epcg
 */

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class EpcgCnc extends ActionHandlerEJB implements EpcgCncLocal, EpcgCncRemote
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
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String  retString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			retString = actionCancel(tranID, xtraParams, forcedFlag);
		}
		catch(Exception e)
		{
			System.out.println("Exception :EpcgCnc :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from EpcgCnc :actionHandler :"+retString);
		return retString;
	}

	private String actionCancel(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String status = "", closingRem = "", sql = "";
		String errCode = "", errString = "";
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		int cnt = 0;
		
		try

		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();

			System.out.println("########### Inside actionCancel ###########");
			System.out.println("Tran ID : "+ tranID);
			
			sql = "SELECT STATUS FROM EPCG_HDR WHERE TRAN_ID = '" + tranID + "'";
			
			System.out.println("sql :\n" + sql +"\n");
			
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				status = rs.getString(1);
			}
			
			System.out.println("Status :"+status);
			
			if (!status.equalsIgnoreCase("O"))
			{
				errCode = "VTCX";
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
			}
			
			if (errCode.trim().length() == 0 )
			{
				sql = "UPDATE EPCG_HDR SET STATUS = 'X', CHG_DATE = ?, CHG_USER = ?, CHG_TERM = ? " +
						"WHERE TRAN_ID = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
				pstmt.setString(2,genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
				pstmt.setString(3,genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
				pstmt.setString(4,tranID);
				cnt = pstmt.executeUpdate();
				if (cnt > 0)
				{
					errCode = "VTCANC1";
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					System.out.println("ErrString : " + errString);
					conn.commit();
					System.out.println("Update Successfull!!");					
				}
			}
		}
		catch (SQLException sqx)
		{ 
			System.out.println("The sqlException occure in EpcgCnc :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in EpcgCnc :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{	
				if (stmt!=null)
				{
					stmt.close();
					stmt=null;
				}
				if (pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				if (conn!=null)
				{
					conn.close();
					conn = null;
				}
			}
			catch (Exception e){}
		}
		System.out.println(errString);
		return errString;
	}
}