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
public class InsuranceCnc extends ActionHandlerEJB implements InsuranceCncLocal, InsuranceCncRemote
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
			System.out.println("Exception :InsuranceCnc :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from InsuranceCnc :actionHandler :"+retString);
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
			
			sql = "SELECT STATUS,CLOSING_REMARKS FROM INSURANCE WHERE TRAN_ID = '" + tranID + "'";
			
			System.out.println("sql :\n" + sql +"\n");
			
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				status = rs.getString(1);
				closingRem = rs.getString(2);
			}
			
			System.out.println("Status :"+status);
			System.out.println("Closing Remarks :"+closingRem);
			
			if (!status.equalsIgnoreCase("O"))
			{
				errCode = "VTCX";
				errString  = itmDBAccess.getErrorString("",errCode,"","",conn);
			}
			else if (closingRem == null || closingRem.trim().length() == 0)
			{
				errCode = "VTNLREM";
				errString  = itmDBAccess.getErrorString("",errCode,"","",conn);
			}
			
			if (errCode.trim().length() == 0 )
			{
				sql = "UPDATE INSURANCE SET STATUS = ?, CHG_DATE = ?, CHG_USER = ?, CHG_TERM = ? " +
							"WHERE TRAN_ID = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,"X");
				pstmt.setDate(2,new java.sql.Date(System.currentTimeMillis()));
				pstmt.setString(3,genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
				pstmt.setString(4,genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
				pstmt.setString(5,tranID);
				cnt = pstmt.executeUpdate();
				if (cnt > 0)
				{
					errCode = "VTCANCLD";
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					conn.commit();
				} 
			}
		}
		catch (SQLException sqx)
		{
			System.out.println("The sqlException occure in InsuranceCnc :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InsuranceCnc :"+e);
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
				System.out.println("");
			}
		}
		return errString;
	}
}