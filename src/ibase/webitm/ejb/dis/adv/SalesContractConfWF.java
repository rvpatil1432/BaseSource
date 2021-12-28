package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SalesContractConfWF 
{
	public String confirm(String contractNo, String empCodeCon, String xmlDataAll, String processId, String keyFlag, String userInfoStr) throws RemoteException, ITMException
	{
		
	    String retString = "";
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    String sql = "";
	    ConnDriver connDriver = new ConnDriver();

	    int updcnt = 0;
	    
	    try {
	    	
	    	System.out.println("Inside Sales contracted confirm()");
	    	//conn = connDriver.getConnectDB("DriverITM");
	    	System.out.println("Inside SalesContractConfWF confirm section: "+userInfoStr);
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
	    	String transDB = userInfo.getTransDB();
	    	System.out.println("get TransDB connetion in SalesContractConfWF : "+transDB);
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
	    	
	    	empCodeCon = empCodeCon == null ? "" : empCodeCon.trim();
	    	
	    	if ((xmlDataAll != null) && (xmlDataAll.trim().length() != 0)) 
	    	{
	    		sql = "update scontract set confirmed = 'Y', conf_date = sysdate where contract_no = ?";
	    		pstmt = conn.prepareStatement(sql);

	    		pstmt.setString(1, contractNo);
	    		updcnt = pstmt.executeUpdate();
		        pstmt.close();
		        pstmt = null;
		        System.out.println("Updated Successfully:" + updcnt);

		        if (updcnt > 0) 
		        {
		          conn.commit();
		          retString = "Y";
		        }
		        else
		        {
		        	conn.rollback();
		        	retString = "N";
		        }
		    }
	    	
		} 
	    catch (Exception e) {
	    	try
		      {
		        conn.rollback();
		      }
		      catch (SQLException e1)
		      {
		        e1.printStackTrace();
		      }
		      e.printStackTrace();
		      System.out.println("Exception occurred in catch block of SalesContractConfWF.confirm()");
		      
		      throw new ITMException(e);
	    }
	    finally
	    {
		      if (conn != null)
		      {
		        try
		        {
		          conn.close();
		          conn = null;
		        }
		        catch (SQLException e)
		        {
		          e.printStackTrace();
		          System.out.println("Exception occurred in finally block of SalesContractConfWF.confirm()");
		        }
		      }
		}
		
	    return retString;
		
	}
}
