package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class PcontractCnc extends ActionHandlerEJB implements PcontractCncLocal, PcontractCncRemote
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
		try
		{
			System.out.println("The tranID :"+tranID);
			retString = actionCancel(tranID, xtraParams);
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Pcontract:actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		System.out.println("returning String from Pcontract : actionHandler"+retString);
	    return retString;
	}

	private String actionCancel(String tranID, String xtraParams) throws RemoteException , ITMException
	{
		int update = 0;
		String sql = "";
		String errCode = "";
		String errString = "";
		String status = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			System.out.println("tranId :"+tranID);
			sql = "SELECT STATUS FROM PCONTRACT_HDR WHERE CONTRACT_NO ='"+ tranID +"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				status = rs.getString(1);
				System.out.println("Status : "+status);
				if (status != null && status.trim().length() > 0 && status.equalsIgnoreCase("X"))
				{
					errCode = "VTPCCLOSED";
				}
				else
				{
					sql = "UPDATE PCONTRACT_HDR SET STATUS='X' WHERE CONTRACT_NO ='"+tranID+"'";
					System.out.println("UpdateSql :"+sql);
					update = stmt.executeUpdate(sql);
					errCode = "VTSUCC";
					System.out.println("The Number of Records updated :"+update);
				}
			}
			if (errCode != null && errCode.trim().length() > 0)
			{
				System.out.println("errCode :"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
			}
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Pcontract : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Pcontract : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection........");
				conn.close();
				conn = null;
			}
			catch(Exception e){}
		}
		System.out.println("errString: "+errString);
		return errString;
	}
}