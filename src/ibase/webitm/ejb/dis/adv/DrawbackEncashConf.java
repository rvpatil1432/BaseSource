/*	
		Developed by	: Hatim Laxmidhar
		Started On		: 31/12/2005
		Purpose  			: This  update confimed to 'Y' in drwaback_encash
		Window				: w_drawback_encash

*/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DrawbackEncashConf extends ActionHandlerEJB implements DrawbackEncashConfLocal, DrawbackEncashConfRemote
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
			retString = actionConfirm(tranId, xtraParams, forcedFlag);
		}
		catch(Exception e)
		{
			System.out.println("Exception :DrawbackEncashCnf :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}

	public String actionConfirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String errCode = "", errString = "";
		String confirmed = "", sql="";
		String tranIdIns = "", refId = "", refSer = "", certNo = "";
		Connection conn=  null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		int cnt = 0;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			sql = "SELECT CONFIRMED " +
					"FROM DRAWBACK_ENCASH WHERE TRAN_ID = '" + tranId + "'";

			System.out.println(sql);

			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				confirmed = rs.getString(1);
				if (confirmed == null || confirmed.trim().length() == 0)
				{
					confirmed = "N";
				}
				System.out.println("confirmed : " + confirmed);
			}
			if (confirmed.trim().equalsIgnoreCase("Y"))
			{
				errCode = "VTCONF1";
			}
			if (errCode.length() == 0)
			{
				sql = "UPDATE DRAWBACK_ENCASH SET CONFIRMED = 'Y', CONF_DATE = ? " +
							"WHERE TRAN_ID = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
				pstmt.setString(2,tranId);
				cnt = pstmt.executeUpdate();
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
			System.out.println("The sqlException occure in DrawbackEncashCnf :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in DrawbackEncashCnf :"+e);
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
		System.out.println("returning String from DrawbackEncashCnf :actionConfirm :" + errString);
		return errString; 
	}
}