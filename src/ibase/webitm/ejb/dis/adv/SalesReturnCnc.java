/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 31/10/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;

import javax.ejb.*;

import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SalesReturnCnc extends ActionHandlerEJB implements SalesReturnCncLocal, SalesReturnCncRemote
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

	public String actionHandler(String tranID, String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("SalesReturnCancel ,actionHandler is called");
		String  retString = null;
		try
		{
			retString = actionCancel(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :SalesReturnCancel :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionCancel(String tranID ,String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt =null;
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
			sql="SELECT STATUS, CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END FROM SRETURN WHERE TRAN_ID = '"+tranID+"'";
			System.out.println("SalesReturnCancel:actionCancel:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				status = rs.getString(1);
				conf = rs.getString(2);
			}
			System.out.println("SalesReturnCancel :actionCancel:status:"+status+":conf:"+conf+":");
			if(status != null && status.trim().length()>0)
			{
				if(status.trim().equalsIgnoreCase("X"))
				{
					errCode ="VTSRET18";
				}
				else if(status.trim().equalsIgnoreCase("S"))
				{
					errCode = "VTALSUB";
				}
				else if(conf.trim().equalsIgnoreCase("Y"))
				{
					errCode ="VTSRET19";
				}	
				else
				{
					updSql="UPDATE SRETURN SET STATUS = 'X'	WHERE TRAN_ID ='"+tranID+"'";
					rows =stmt.executeUpdate(updSql);
					if(rows != 1)
					{
						errCode = "VTSRET20";
						conn.rollback();
						System.out.println("<======= SRETURN ROLLBACK ====>");
					}
					else
					{
						conn.commit();
						System.out.println("<======= SRETURN Updated Successfully ====>");
						errCode ="VTSRET21";
					}
				}//update else end
			}//status != null end if
			else
			{
				errCode ="VTACTSTS";
			}
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("SalesReturnCancel:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SalesReturnCancel :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
			}catch(Exception e){}
		}
		return errString;
	}
 }		