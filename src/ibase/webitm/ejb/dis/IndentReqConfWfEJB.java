package ibase.webitm.ejb.dis;

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
import ibase.webitm.utility.ITMException;


@Stateless
public class IndentReqConfWfEJB extends ActionHandlerEJB implements IndentReqConfWfEJBLocal, IndentReqConfWfEJBRemote
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("================ IndentReqConfWfEJB : confirm ================");
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String status = "",confirmed = "",retString = "", ind_no= "", wf_status = "";
		String[] authencate = new String[2];
		authencate[0] = "";
		authencate[1] = "";
		boolean isError = false;
		
		try {
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			conn = getConnection();
			conn.setAutoCommit(false);
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			System.out.println("tran_id ..> " + tranID+" userId: "+userId+" xtraParams: "+xtraParams+" forcedFlag: "+forcedFlag);
			ind_no = checkNull(tranID);

			sql = "SELECT CONFIRMED,STATUS,WF_STATUS FROM INDENT_HDR WHERE IND_NO = ? ";

			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, ind_no);
			rs = pStmt.executeQuery();
			
			if (rs.next()) 
			{
				confirmed  = rs.getString("confirmed");
				status     = rs.getString("STATUS");
				wf_status  = rs.getString("WF_STATUS");
			}
			
			confirmed = checkNull(confirmed);
			status = checkNull(status);
			wf_status = checkNull(wf_status);
			
			closePstmtRs(pStmt, rs);

			if(!"Y".equalsIgnoreCase(confirmed))
			{
				if("X".equalsIgnoreCase(status) || "C".equalsIgnoreCase(status))
				{
					errString = new ITMDBAccessEJB().getErrorString("", "VTWFCLOCAN", "","",conn);
				}
				else if ("O".equalsIgnoreCase(wf_status) || "R".equalsIgnoreCase(wf_status) || "".equalsIgnoreCase(wf_status)) 
				{
					GenericWorkflowClass wfGenericClass = new GenericWorkflowClass();
					retString = wfGenericClass.invokeWorkflow(conn, tranID, xtraParams, "w_indent_req", "indent_req");
					if ("success".equalsIgnoreCase(retString)) 
					{
						sql = "UPDATE INDENT_HDR SET WF_STATUS = 'S' WHERE IND_NO = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, tranID);
						int updCount = pStmt.executeUpdate();
							
						if (updCount > 0) 
						{
							errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWS", "","",conn);
						}
						else
						{
							errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWF", "","",conn);
	    					isError=true;
						}
					} 
					else 
					{
						errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWF", "","",conn);
					}
				} 
				else
				{
					errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALINT", "","",conn);
				}
			}
			else
			{
				errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALCNF", "","",conn);
			}
		} 
		catch (Exception e) 
		{
			isError = true;
			System.out.println("Exception: confirm method ..> " + e.getMessage());
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VFMWRKFLWF", userId,"",conn);
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
						System.out.println("IndentReqConfWfEJB: confirm: Connection Rollback");
					} 
					else 
					{
						conn.commit();
						System.out.println("IndentReqConfWfEJB: confirm: Connection Commit");
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
		return errString;
	}
	
	public String rejection(String tranID, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException
	{
		System.out.println("================ IndentReqConfWfEJB : rejection ================");
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		Connection conn = null;
		ConnDriver connDriver  = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String status = "",confirmed = "",retString = "", ind_no= "", wf_status = "";
		String[] authencate = new String[2];
		authencate[0] = "";
		authencate[1] = "";
		boolean isError = false;
		
		try {
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			//conn = getConnection();
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
	    	String transDB       = userInfo.getTransDB();
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
			
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			System.out.println("tran_id ..> " + tranID+" userId: "+userId+" xtraParams: "+xtraParams+" forcedFlag: "+forcedFlag);
			ind_no = checkNull(tranID);

			sql = "SELECT CONFIRMED,STATUS,WF_STATUS FROM INDENT_HDR WHERE IND_NO = ? ";

			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, ind_no);
			rs = pStmt.executeQuery();
			
			if (rs.next()) 
			{
				confirmed  = rs.getString("confirmed");
				status     = rs.getString("STATUS");
				wf_status  = rs.getString("WF_STATUS");
			}
			
			confirmed = checkNull(confirmed);
			status = checkNull(status);
			wf_status = checkNull(wf_status);
			
			closePstmtRs(pStmt, rs);

			if(!"Y".equalsIgnoreCase(confirmed))
			{
				/*if("X".equalsIgnoreCase(status) || "C".equalsIgnoreCase(status))
				{
					errString = new ITMDBAccessEJB().getErrorString("", "VTWFCLOCAN", "","",conn);
				}*/
				if ("O".equalsIgnoreCase(wf_status) || "R".equalsIgnoreCase(wf_status) || "S".equalsIgnoreCase(wf_status)) 
				{
					sql = "UPDATE INDENT_HDR SET WF_STATUS = 'O' WHERE IND_NO = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ind_no);
					int updCount = pStmt.executeUpdate();
					closePstmtRs(pStmt, rs);
						
					if (updCount > 0) 
					{
						retString="Y";
						//errString = new ITMDBAccessEJB().getErrorString("", "VFMWFOPN", "","",conn);
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
						System.out.println("IndentReqConfWfEJB: rejection: Connection Rollback");
					} 
					else 
					{
						conn.commit();
						System.out.println("IndentReqConfWfEJB: rejection: Connection Commit");
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

	private void closePstmtRs(PreparedStatement pStmt, ResultSet rs) 
	{
		if (pStmt != null) 
		{
			try 
			{
				pStmt.close();
			} catch (SQLException localSQLException1) {
			}
			pStmt = null;
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
	
		
	}
}
