package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdjustmentReceiptConfWF 
{
	public String confirm(String tranId, String empCodeAprv, String xmlDataAll, String processId, String keyFlag, String userInfoStr) throws RemoteException, ITMException
	{
		
	    String retString = "";
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    String sql = "";
	    ConnDriver connDriver = new ConnDriver();

	    int updcnt = 0;
	    
	    try {
			System.out.println("Exception in Try block of AdjustmentReceiptWF.confirm()");
			//conn = connDriver.getConnectDB("DriverITM");
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			String transDB       = userInfo.getTransDB();
			if (transDB != null && transDB.trim().length() > 0)
	           {
	               conn = connDriver.getConnectDB(transDB);
	           }
			
			connDriver = null;
	    	conn.setAutoCommit(false);
			
	    	empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
	    	
	    	if ((xmlDataAll != null) && (xmlDataAll.trim().length() != 0)) 
	    	{
	    		sql = "update sample_rcp set confirmed = 'Y', conf_date = sysdate where tran_id = ?";
	    		pstmt = conn.prepareStatement(sql);

	    		pstmt.setString(1, tranId);
	    		updcnt = pstmt.executeUpdate();
		        pstmt.close();
		        pstmt = null;
		        System.out.println("Updated Successfully:" + updcnt);

		        if (updcnt > 0) 
		        {
		          conn.commit();
		          retString = "Y";
		        }
		        
		        conn.rollback();
		        retString = "N";
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
		      System.out.println("Exception occurred in catch block of AdjustmentReceiptWF.confirm()");
		      
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
		          System.out.println("Exception occurred in finally block of AdjustmentReceiptWF.confirm()");
		        }
		      }
		}
		
	    return retString;
		
	}
}
