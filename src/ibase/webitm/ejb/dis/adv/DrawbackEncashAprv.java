/*	
		Developed by	: Hatim Laxmidhar
		Started On		: 31/12/2005
		Purpose  			: This  updates DRAWBACK_ENCASH sets EMP_CODE__APRV to the empCode of the 
										Current User
		Window				: w_drawback_encash

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
public class DrawbackEncashAprv extends ActionHandlerEJB implements DrawbackEncashAprvLocal, DrawbackEncashAprvRemote
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
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String  retString = null;
		try
		{
			retString = actionApprove(tranId, xtraParams, forcedFlag);
		}
	  catch(Exception e)
		{
			System.out.println("Exception :DrawbackEncashApv :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}

	public String actionApprove(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String errCode = "", errString = "";
		String confirmed="", approved = "", sql="";
		String empCode = "", userId = "", termId = "";
		Connection conn=  null;
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

			sql =	"SELECT CONFIRMED,EMP_CODE__APRV " + 
						"FROM DRAWBACK_ENCASH WHERE TRAN_ID = '" + tranId + "'";

			System.out.println(sql);

			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			if (rs.next())
			{
				confirmed = rs.getString(1);
				approved = rs.getString(2);
				if (confirmed == null || confirmed.trim().length() == 0)
				{
					confirmed = "N";
				}
				System.out.println("confirmed : " + confirmed);
				System.out.println("approved : " + approved);
			}

			if (confirmed.trim().equalsIgnoreCase("N"))
			{
				errCode = "VTCONF2";
			}
			if (errCode.length() == 0)
			{
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
				sql = "SELECT EMP_CODE FROM USERS " +
							"WHERE CODE = '" + userId + "'";				
				System.out.println(sql);
				rs = stmt.executeQuery(sql);

				if (rs.next())
				{
					empCode = rs.getString(1);
				}

				if (approved == null || approved.trim().length() == 0)
				{
					sql = "UPDATE DRAWBACK_ENCASH SET EMP_CODE__APRV = ?, CHG_DATE = ?, " +
								"CHG_USER = ?, CHG_TERM = ? " +
								"WHERE TRAN_ID = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,empCode);
					pstmt.setDate(2,new java.sql.Date(System.currentTimeMillis()));
					pstmt.setString(3,userId);
					pstmt.setString(4,termId);
					pstmt.setString(5,tranId);
					cnt = pstmt.executeUpdate();
				}
				if (cnt > 0)
				{
					errCode = "VTMCONF2";
					conn.commit();
				}
			}
			if (errCode.trim().length() > 0)
			{
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
			}
		}
		catch (SQLException sqx)
		{
			System.out.println("The sqlException occure in DrawbackEncashApv :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in DrawbackEncashApv :"+e);
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
			catch (Exception e)
			{
				System.out.println(e);
			}
		}
		System.out.println("returning String from DrawbackEncashApv :actionApprove :" + errString);
		return errString;
	}
}