/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 24/10/2005
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
public class IndentReqCnc extends ActionHandlerEJB implements IndentReqCncLocal, IndentReqCncRemote
//public class IndentReqCnc extends ActionHandlerEJB implements SessionBean
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
			retString = actionCancel(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :IndentReq :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionCancel(String indNo,  String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String errCode="";
		String errString="";
		String status = "";
		String conf = "";
		int rows=0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			sql = "SELECT CONFIRMED,STATUS FROM INDENT_HDR WHERE IND_NO = '"+indNo+"'";
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				conf = rs.getString("CONFIRMED");
				status = rs.getString("STATUS");
				System.out.println("indNo:"+indNo+":status:"+status+":conf:"+conf+":");
				System.out.println(" calling IndentReqCnc2::170415"+errCode);

			}
			if(conf != null && conf.equalsIgnoreCase("Y"))
			{
				errCode = "VTINDCANC1";
			}
			else if(status != null && status.equalsIgnoreCase("C"))
			{
				errCode = "VTINDCANC2";
			}
			// ADDED BY RITESH ON 17-APR-2015 START
			else if(status != null && status.equalsIgnoreCase("S"))
			{
				errCode = "VTINDCANC3";
				System.out.println(" errCode::170415"+errCode);
			}
			// ADDED BY RITESH ON 17-APR-2015 END

			if (errCode.equals(""))
			{
				updSql = "UPDATE INDENT_HDR SET STATUS = 'C' WHERE IND_NO ='"+indNo+"'";
				rows = stmt.executeUpdate(updSql) ;
				if(rows > 0)
				{
					conn.commit();
					errCode = "VTSUCC";
					System.out.println("\n <==== Transaction Updated Successfully ====>");
				}					
			}
			if (errCode != null  && errCode.trim().length() > 0)
			{
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :IndentReq :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
	 	}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		return errString;
	}
}

