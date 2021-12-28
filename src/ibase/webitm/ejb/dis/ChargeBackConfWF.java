package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChargeBackConfWF 
{
	public String confirm(String tranId, String empCodeAprv, String xmlDataAll, String processId, String keyFlag, String userInfoStr) throws RemoteException, ITMException
	{
		
	    String retString = "";
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    String sql = "";
	    String tran_id="";
	    ConnDriver connDriver = new ConnDriver();

	    int updcnt = 0;
	    
	    try {
	    	
	    	System.out.println("Inside ChargeBackConfWF confirm()");
	  
	    	System.out.println("Inside ChargeBackConfWF confirm section: "+userInfoStr);
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
	    	String transDB = userInfo.getTransDB();
	    	System.out.println("get TransDB connetion in ChargeBackConfWF : "+transDB);
	    	tran_id = checkNull((tranId));
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
	    	
			empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
	    	
	    	if ((xmlDataAll != null) && (xmlDataAll.trim().length() != 0)) 
	    	{
	    		sql = "update charge_back set confirmed = 'Y', conf_date = sysdate where tran_id = ?";
	    		pstmt = conn.prepareStatement(sql);

	    		pstmt.setString(1, tran_id);
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
		      System.out.println("Exception occurred in catch block of DiscAprWF.confirm()");
		      
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
		          System.out.println("Exception occurred in finally block of DiscAprWF.confirm()");
		        }
		      }
		}
		
	    return retString;
		
	} 
	
	public String escalate (String tranId, String empCodeAprv, String xmlDataAll, String processId, String roleCode, String activityId,String userInfoStr) throws ITMException, Exception {
		System.out.println("Parameters ::: tranId["+tranId+"],empCodeAprv["+empCodeAprv+"],xmlDataAll["+xmlDataAll+"],processId["+processId+"],roleCode["+roleCode+"],activityId["+activityId+"]");
		String retString = "";
		Boolean isError = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		E12GenericUtility genericUtility = null;
		SimpleDateFormat  simpleDateFormat = null;
		String currDate = "";
		String signStatus = "", userCode = "",processInfo[] = null;
		int resultCnt = 0;
		Connection conn = null;
		ConnDriver connDriver = null;
		
		
		try
		{
			connDriver = new ConnDriver();
			genericUtility = new E12GenericUtility();
			System.out.println("Inside ChargeBackConfWF confirm section: "+userInfoStr);
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			String transDB = userInfo.getTransDB();
			if (transDB != null && transDB.trim().length() > 0)
		    {
		    	conn = connDriver.getConnectDB(transDB);
		    }
		    else
		    {
		    	conn = connDriver.getConnectDB("DriverITM");
		    }
			//conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat  = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
			currDate          = simpleDateFormat.format( Calendar.getInstance().getTime() );
			currDate          = genericUtility.getValidDateTimeString( currDate, genericUtility.getApplDateTimeFormat(),genericUtility.getDBDateTimeFormat() ) ;

			sql = "select SIGN_STATUS from OBJ_SIGN_TRANS "
				+ "WHERE ltrim(rtrim(REF_SER)) = 'S-CBK' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
				+ "AND SIGN_STATUS = 'U'";
				
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2, roleCode);
			rs = pstmt.executeQuery();
			
			if (rs.next()) 
			{
				signStatus = rs.getString("SIGN_STATUS");
			}
			System.out.println("signStatus = " + signStatus);

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if("U".equalsIgnoreCase(signStatus)){
				
				sql = "select code from users where emp_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, empCodeAprv);
				rs = pstmt.executeQuery();
				
				if (rs.next()) 
				{
					userCode = rs.getString("code");
				}
				userCode = userCode == null ? "" : userCode.trim();
	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				sql = "UPDATE OBJ_SIGN_TRANS "
						+ "SET SIGN_DATE = ?, USER_ID__SIGN = ?, SIGN_REMARKS = 'HR_SPAN is over', SIGN_STATUS = 'V', EMP_CODE = ? "
						+ "WHERE ltrim(rtrim(REF_SER)) = 'S-CBK' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
						+ "AND SIGN_STATUS = 'U'";
						
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(currDate));
				pstmt.setString(2, userCode);
				pstmt.setString(3, empCodeAprv);
				pstmt.setString(4, tranId);
				pstmt.setString(5, roleCode);
				
				resultCnt += pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("Update Successfully:"+resultCnt);
				
				processInfo = processId.split(":");
				sql = "UPDATE WF_PRC_STATUS "
						+ "SET STATUS_DATE = ?, PROCESS_STATUS = 4, ERR_STATUS = '0', "
						+ "STATUS_REMARKS='Escalated -> Completing :"+processId+" Invoked :"+processId+"' "
						+ "WHERE ltrim(rtrim(REF_SER)) = 'S-CBK' AND REF_ID = ? AND PROCESS_ID = ? AND INSTANCE_ID = ? AND ACTIVITY_ID = ? AND PROCESS_STATUS != 3";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(currDate));
				pstmt.setString(2, tranId);
				pstmt.setString(3, processInfo[0]);
				pstmt.setString(4, processInfo[1]);
				pstmt.setString(5, activityId);
				resultCnt += pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("Update ChargeBackConfWF Successfully:"+resultCnt);
			} 
			else 
			{
				retString = "N";
			}
		}
		catch (Exception e) {
			System.out.println("Exception: ChargeBackConfWF escalate method ..> " + e.getMessage());
			isError = true;
			throw new ITMException(e);
		} finally {
			if(rs !=null)
			{
				rs.close();rs=null;
			}
			if(pstmt != null)
            {
				pstmt.close();pstmt = null;
            }
			if (conn != null) {
				try {
					if (isError || !(resultCnt > 0)) {
						conn.rollback();
						retString = "N";
						System.out.println("ChargeBackConfWF escalate: Connection Rollback");
					} else {
						conn.commit();
						retString = "Y";
						System.out.println("ChargeBackConfWF escalate: Connection Commit");
					}
					conn.close();
					conn = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return retString;
	}
	
	    
	private String checkNull(String str) 
	{
		if(str == null)
		{
			str = "";
		}
		else
		{
			str = str.trim();
		}
		return str;
	} 
	    
	    
	
}