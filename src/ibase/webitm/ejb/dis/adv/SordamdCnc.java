/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :15/11/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.CommonConstants;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SordamdCnc extends ActionHandlerEJB implements SordamdCncLocal, SordamdCncRemote
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
		System.out.println("Sordamd is called");
		String  retString = null;
		try
		{
			retString = actionCancel(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Sordamd :actionHandler:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionCancel(String amdNo ,String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String errCode = "";
		String errString = "";
		String conf = "";
		String status = "";
		int rows = 0;
		CommonConstants commonConstants = new CommonConstants();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			CommonConstants.setIBASEHOME();
			if(CommonConstants.DB_NAME.equals("mssql"))
			{
				sql="SELECT CONFIRMED, STATUS FROM SORDAMD (UPDLOCK) WHERE AMD_NO ='"+amdNo+"'" ;
			}
			else if(CommonConstants.DB_NAME.equals("db2"))
			{
				sql="SELECT CONFIRMED, STATUS FROM SORDAMD WHERE AMD_NO = '"+amdNo+"' FOR UPDATE";
			}
			else
			{
				sql="SELECT CONFIRMED, STATUS FROM SORDAMD WHERE AMD_NO ='"+amdNo+"'";// FOR UPDATE NOWAIT";
			}
			System.out.println("Sordamd:actionCancel:SORDAMD:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				conf = rs.getString(1);
				status = rs.getString(2);
			}
			System.out.println("Sordamd:actionCancel:conf:"+conf+":status:"+status);
			//if get_sqlcode() <> 0 then
			//start -commented by rajendra on 02/11/07
			//if(status != null && status.trim().length() > 0)
			//{
				//if(status.equalsIgnoreCase("X"))
			//end by rajendra on 02/11/07
				if(("X").equalsIgnoreCase(status))
				{
					errCode ="VTCANC2";
				}
				else if(conf != null && conf.equalsIgnoreCase("Y"))
				{
					errCode = "VTSACONF1";
				}
				else
				{
					updSql =" UPDATE SORDAMD SET STATUS= 'X',STATUS_DATE = ?"+
							//,"REMARKS = ?"
							" WHERE AMD_NO 	= ?";
					System.out.println("Sordamd:actionCancel:SORDAMD:updSql:"+updSql);
					pstmt = conn.prepareStatement(updSql);
					pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
					//pstmt.setString(2,"Cancelling sales order amendment");
					pstmt.setString(2,amdNo);
					rows = pstmt.executeUpdate();
					System.out.println("ordamd:actionCancel:rows:"+rows+":");
					if(rows > 0)
					{
						conn.commit();
						errCode = "VTCANC1";
						System.out.println("\n <==== SORDAMD Updated Successfully ====>");
					}
				}
			//}// status != null(End If)   //start commented by rajendra on 02/11/07
//			else
//			{
//				System.out.println("\n Status is null");
//				errCode ="VTNULSTAT";
//			}
//end commented by rajendra on 02/11/07
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("Sordamd:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);				
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Sordamd :" + e.getMessage() + ":");
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