package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsumpIssueConfWF 
{
	public String confirm(String consIssue, String empCodeAprv, String xmlDataAll, String processId, String keyFlag, String userInfoStr) throws RemoteException, ITMException
	{
		String retString = "";
	    String errString = "";
	    Connection conn = null;
	    ITMDBAccessEJB itmDBAccessEJB = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    String status = "", confirmed = "", wf_status = "", line_no = "";
	    String sql = "";
	    String loginCode = "", loginSiteCode = "";
	    ConnDriver connDriver = new ConnDriver();
	    java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());

	    int updcnt = 0;
	    
	    try {
			System.out.println("Inside Try block of ConsumpIssueConfWF.confirm()");
			//conn = connDriver.getConnectDB("DriverITM");
			System.out.println("Inside PContractConfWF confirm section: "+userInfoStr);
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
	    	String transDB = userInfo.getTransDB();
	    	System.out.println("get TransDB connection in ConsumpIssueConfWF : "+transDB);
	    	
		    if (transDB != null && transDB.trim().length() > 0)
		    {
		        conn = connDriver.getConnectDB(transDB);
		    }
		    else
		    {
		    	conn = connDriver.getConnectDB("DriverITM");
		    }
		    connDriver = null;
			conn.setAutoCommit(false);
	    	
			empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
			
			sql = " select site_code__def,code from users where emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCodeAprv);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				loginCode     = checkNull(rs.getString("code"));
				loginSiteCode = checkNull(rs.getString("site_code__def"));
			}
			
			closePstmtRs(pstmt, rs);
			
			
			sql = " SELECT select status, confirmed, wf_status from consume_iss WHERE cons_issue = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, consIssue);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				status     = checkNull(rs.getString("status"));
				confirmed  = checkNull(rs.getString("confirmed"));
				wf_status  = checkNull(rs.getString("wf_status"));
			}
			
			closePstmtRs(pstmt, rs);
			
			
			if("Y".equalsIgnoreCase(confirmed))
			{
				errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALCNF", "","",conn);
				return errString;
				
			}
			else if("X".equalsIgnoreCase(status) || "C".equalsIgnoreCase(status))
			{
				errString = new ITMDBAccessEJB().getErrorString("", "VTWFCLOCAN", "","",conn);
				return errString;
			}
	    	
			else
			{
	    	
				if((xmlDataAll != null) && (xmlDataAll.trim().length() != 0)) 
				{
					sql = "update consume_iss set confirmed = 'Y', conf_date = ?, status = 'O' where cons_issue =  ?";
					pstmt = conn.prepareStatement(sql);

					pstmt.setDate(1, currentDate);
					pstmt.setString(2, consIssue);
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
		      System.out.println("Catch block of ConsumpIssueConfWF.confirm()");
		      
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
		          System.out.println("Finally block of ConsumpIssueConfWF.confirm()");
		        }
		      }
		}
	    System.out.println("Return string from ConsumpIssueConfWF confirm method is: "+retString);
	    return retString;
		
	}
	
	private String checkNull(String str) 
	{
		if(str == null){
			str = "";
		}
		else{
			str = str.trim();
		}
		return str;
	}
	
	private void closePstmtRs(PreparedStatement pStmt, ResultSet rs) 
	{
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
		
		if (pStmt != null) {
			try {
				pStmt.close();
			} catch (SQLException localSQLException1) {
			}
			pStmt = null;
		}
		
	}
}
