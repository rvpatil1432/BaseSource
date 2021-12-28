package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.GenericWorkflowClass;
import ibase.webitm.utility.ITMException;

@Stateless
public class ConsumpIssueConfWfEJB extends ActionHandlerEJB implements ConsumpIssueConfWfEJBLocal, ConsumpIssueConfWfEJBRemote 
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		String userInfoStr = "";
		String errString = "";
		try
		{
			errString = confirm(tranId, xtraParams, forcedFlag, userInfoStr);
			System.out.println("userInfoStr of confirm::::: " +userInfoStr);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [ConsumpIssueConfWfEJB] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
		
	}
	public String confirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException 
	{
		System.out.println("@@@@@@@@@@@@@@@@@@@@ ConsumpIssueConfWfEJB : confirm @@@@@@@@@@@@@@@@");
		String userId = "";
		String errString = "";
		String transDB = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String status = "",confirmed = "",retString = "",wf_status = "";
		String[] authencate = new String[2];
		authencate[0] = "";
		authencate[1] = "";
		boolean isError = false;
		
		try {
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			//conn = getConnection();
			if(userInfoStr != null && userInfoStr.trim().length() > 0)
			{
				UserInfoBean userInfo = new UserInfoBean(userInfoStr);
		    	transDB       = userInfo.getTransDB();
			}
	    	if (transDB != null && transDB.trim().length() > 0)
	    	{
	    		conn = connDriver.getConnectDB(transDB);
		    }
			
	    	else
	    	{
	    		conn = connDriver.getConnectDB("DriverITM");
	    	}
				
	    	//connDriver = null;
	    	conn.setAutoCommit(false);
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			System.out.println("tran_id ..> " + tranId+" userId: "+userId+" xtraParams: "+xtraParams+" forcedFlag: "+forcedFlag);
			tranId = checkNull(tranId);

			sql = " SELECT status, confirmed, wf_status FROM consume_iss WHERE cons_issue = ? ";

			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranId);
			rs = pStmt.executeQuery();
			
			if (rs.next()) {
				status     = checkNull(rs.getString("status"));
				confirmed  = checkNull(rs.getString("confirmed"));
				wf_status  = checkNull(rs.getString("wf_status"));
			}
			
			closePstmtRs(pStmt, rs);

			if(!"Y".equalsIgnoreCase(confirmed))
			{
				if("X".equalsIgnoreCase(status) || "C".equalsIgnoreCase(status))
				{
					errString = new ITMDBAccessEJB().getErrorString("", "VTWFCLOCAN", "","",conn);
				}
				
				else if ("O".equalsIgnoreCase(status) || "".equalsIgnoreCase(status)) 
				{
					if("O".equalsIgnoreCase(wf_status) || "R".equalsIgnoreCase(wf_status) || "".equalsIgnoreCase(wf_status))
					{
						GenericWorkflowClass wfGenericClass = new GenericWorkflowClass();
						retString = wfGenericClass.invokeWorkflow(conn, tranId, xtraParams, "w_consume_issue", "consume_issue");
						if ("success".equalsIgnoreCase(retString)) {
							
							sql = "UPDATE CONSUME_ISS SET wf_status = 'S' WHERE CONS_ISSUE = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, tranId);
							int updCount = pStmt.executeUpdate();
							closePstmtRs(pStmt, rs);
							
							if (updCount > 0) {
								errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWS", "","",conn);
							}
							else{
								errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWF", "","",conn);
	    						isError=true;
							}
						} else {
							errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWF", "","",conn);
						}
					}
					else
					{
						errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALINT", "","",conn);
					}
				} 
				else {
					errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWF", "","",conn);
				}
			}
			else{
				errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALCNF", "","",conn);
			}
		} catch (Exception e) {
			isError = true;
			System.out.println("Exception: confirm method ..> " + e.getMessage());
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VFMWRKFLWF", userId,"",conn);
			throw new ITMException(e);
		} finally {
			if (conn != null) {
				try {
					if (isError) {
						conn.rollback();
						System.out.println("ConsumpIssueConfWfEJB: confirm: Connection Rollback");
					} else {
						conn.commit();
						System.out.println("ConsumpIssueConfWfEJB: confirm: Connection Commit");
					}
					conn.close();
					conn = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return errString;
	}
	
	
	
	public String rejection(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String userInfoStr = "";
		String errString = "";
		try
		{
			userInfoStr = rejection(tranId, xtraParams, forcedFlag, userInfoStr);
			System.out.println("userInfoStr of rejection::::: " +userInfoStr);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [ConsumpIssueConfWfEJB] rejection " + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
	}
	
	public String rejection(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException
	{
		System.out.println("================ ConsumpIssueConfWfEJB : rejection ================");
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		Connection conn = null;
		ConnDriver connDriver  = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String transDB = "";
		String status = "",confirmed = "",retString = "", wf_status = "";
		String[] authencate = new String[2];
		authencate[0] = "";
		authencate[1] = "";
		boolean isError = false;
		
		try {
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			//conn = getConnection();
			if(userInfoStr != null && userInfoStr.trim().length() > 0)
			{
				UserInfoBean userInfo = new UserInfoBean(userInfoStr);
		    	transDB       = userInfo.getTransDB();
			}
	    	if (transDB != null && transDB.trim().length() > 0)
	    	{
	    		conn = connDriver.getConnectDB(transDB);
		    }
			
	    	else
	    	{
	    		conn = connDriver.getConnectDB("DriverITM");
	    	}
				
	    	//connDriver = null;
	    	conn.setAutoCommit(false);
			
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			System.out.println("tran_id ..> " + tranId+" userId: "+userId+" xtraParams: "+xtraParams+" forcedFlag: "+forcedFlag);
			tranId = checkNull(tranId);

			sql = "SELECT status, confirmed, wf_status FROM consume_iss WHERE cons_issue = ? ";

			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranId);
			rs = pStmt.executeQuery();
			
			if (rs.next()) 
			{
				confirmed  = checkNull(rs.getString("confirmed"));
				status     = checkNull(rs.getString("status"));
				wf_status  = checkNull(rs.getString("wf_status"));
			}
			
			closePstmtRs(pStmt, rs);

			if(!"Y".equalsIgnoreCase(confirmed))
			{
				/*if("X".equalsIgnoreCase(status) || "C".equalsIgnoreCase(status))
				{
					errString = new ITMDBAccessEJB().getErrorString("", "VTWFCLOCAN", "");
				}*/
				if ("O".equalsIgnoreCase(wf_status) || "R".equalsIgnoreCase(wf_status) || "S".equalsIgnoreCase(wf_status) || "".equalsIgnoreCase(wf_status)) 
				{
					sql = "UPDATE CONSUME_ISS SET WF_STATUS = 'O' WHERE CONS_ISSUE = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, tranId);
					int updCount = pStmt.executeUpdate();
					closePstmtRs(pStmt, rs);
						
					if (updCount > 0) 
					{
						retString="Y";
						errString = new ITMDBAccessEJB().getErrorString("", "VFMWFOPN", "","",conn);
					}
					else
					{
						errString = new ITMDBAccessEJB().getErrorString("", "VFMWFFAIL", "","",conn);
	    				isError=true;
					}
				} 
				else
				{
					errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALINT", "","",conn);
				}
			}
			else
			{
				errString = new ITMDBAccessEJB().getErrorString("", "VFMWFCONFN", "","",conn);
			}
			
		} 
		catch (Exception e) 
		{
			isError = true;
			System.out.println("Exception: rejection method ..> " + e.getMessage());
			e.printStackTrace();
			//errString = itmDBAccessEJB.getErrorString("", "VFMWRKFLWF", userId,"",conn);
			throw new ITMException(e);
		} 
		finally 
		{
			if (conn != null) 
			{
				try 
				{
					if (isError) 
					{
						conn.rollback();
						System.out.println("ConsumpIssueConfWfEJB: rejection: Connection Rollback");
					} 
					else 
					{
						conn.commit();
						System.out.println("ConsumpIssueConfWfEJB: rejection: Connection Commit");
					}
					conn.close();
					conn = null;
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
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



