/********************************************************
Title : SchemeConf []
Date  : 27/12/16
Developer: Pankaj R
Purpose: To conf scheme
********************************************************/
package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.adv.GenericWorkflowClass;
import ibase.webitm.utility.ITMException;

@Stateless
public class SchemeConf extends ActionHandlerEJB 
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("SchemeConf.confirm() :: ");
		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
		}
		catch(Exception e)
		{
			System.out.println("SchemeConf.confirm() : "+e);
			throw new ITMException(e);
		}
		return retString;
	}
	
	public String confirm(String tranID,String xtraParams, String forcedFlag,String userInfoStr) throws RemoteException,ITMException
	{
		System.out.println("SchemeConf.confirm() userInfo : ");
		String retString = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
		   	String transDB = userInfo.getTransDB();
		   	System.out.println("#### TransDB connection in : "+transDB);
		   	
		   	if (transDB != null && transDB.trim().length() > 0)
		   	{
		   		conn = connDriver.getConnectDB(transDB);
		   	}
		   	else
		   	{
		   		conn = connDriver.getConnectDB("DriverITM");
		   	}
		   	conn.setAutoCommit(false);
		   	connDriver = null;
		 
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
			//return retString;
			return "1";
		}
		catch(Exception e)
		{
			System.out.println("Exception in [SchemeConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		
	}
	
	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
		System.out.println("SchemeConf.confirm() connectuion ");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String isConfirmed = "", returnStr = "", loginEmpCode = "";
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		E12GenericUtility genericUtility = new E12GenericUtility();
		boolean isError = false;
		try
		{
			Date currentDate = new java.sql.Date(new java.util.Date().getTime());
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			if ( conn == null )
			{
				conn = getConnection();
			}
			String checkConf = "SELECT CONFIRMED FROM SCHEME_APPRV WHERE TRAN_ID = ?";
			preparedStatement = conn.prepareStatement(checkConf);
			preparedStatement.setString(1, tranId);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				isConfirmed = E12GenericUtility.checkNull(resultSet.getString("CONFIRMED"));
			}
			resultSet.close();
			resultSet = null;
			preparedStatement.close();
			preparedStatement = null;
			
			if(isConfirmed.length() > 0 && isConfirmed.equalsIgnoreCase("Y"))
			{
				returnStr = itmDBAccess.getErrorString("","VTINDCONF","","",conn);
				return returnStr;
			}
			else
			{
				String updateScheme = "UPDATE SCHEME_APPRV SET CONFIRMED = ?, CONF_DATE = ? WHERE TRAN_ID = ?";
				preparedStatement = conn.prepareStatement(updateScheme);
				preparedStatement.setString(1, "Y");
				preparedStatement.setDate(2, currentDate);
				preparedStatement.setString(3, tranId);
				int updateCount = preparedStatement.executeUpdate();
				if(updateCount > 0)
				{
					returnStr = itmDBAccess.getErrorString("CONFIRMED", "VTMCONF2", loginEmpCode);
					conn.commit();
				}
			}
		}
		catch(Exception e)
		{
			isError = true;
			System.out.println("Exception in SchemeConf.confirm(4 param) : "+e);
		}
		finally
		{
			try
			{	
				System.out.println("isError in Finally SchemeConf="+isError);
				if( !isError  )
				{
					System.out.println("----------commmit-----------");
					conn.commit(); 
				}
				else if ( isError )
				{
					System.out.println("--------------rollback------------");
					conn.rollback();
				}
				
				if ( preparedStatement != null )
				{
					preparedStatement.close();
					preparedStatement= null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return returnStr;
	}
}
