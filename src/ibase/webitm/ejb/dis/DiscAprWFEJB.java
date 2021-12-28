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
public class DiscAprWFEJB extends ActionHandlerEJB implements DiscAprWFEJBLocal, DiscAprWFEJBRemote
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
	public String confirm(String tranID, String xtraParams, String forcedFlag,String userInfoStr) throws RemoteException,ITMException
	{
		
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		ConnDriver connDriver  = new ConnDriver();
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String confirmed = "",retString = "", tran_id= "", wf_status = "";
		boolean isError = false;
		
		try {
//			conn = getConnection();// commented by kailasg on 16-1pril-21 table orview does exit error occur on STD
					/*UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			    	String transDB       = userInfo.getTransDB();
			    	if(userInfoStr != null && userInfoStr.trim().length() > 0)
					{
				    	transDB       = userInfo.getTransDB();
					}
			    	if (transDB != null && transDB.trim().length() > 0)
			    	{
			    		conn = connDriver.getConnectDB(transDB);
				    }
					
			    	else
			    	{
			    		conn = connDriver.getConnectDB("DriverITM");
			    	}*/
					/*conn.setAutoCommit(false);
					itmDBAccessEJB = new ITMDBAccessEJB();
					validatorEJB = new ValidatorEJB();
				//	userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
					userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");*/
					tran_id = checkNull((tranID));
					itmDBAccessEJB = new ITMDBAccessEJB();
					validatorEJB = new ValidatorEJB();
					conn = getConnection();
					conn.setAutoCommit(false);
					userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			tran_id = checkNull((tranID));
			sql = "SELECT CONFIRMED,WF_STATUS FROM DISC_APR_STRG WHERE TRAN_ID = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tran_id);
			rs = pStmt.executeQuery();
			if (rs.next()) 
			{
				confirmed = checkNull(rs.getString("confirmed"));
				wf_status = checkNull(rs.getString("WF_STATUS"));
			}
			/*rs.close();
			rs = null;
			pStmt.close();
			pStmt = null;*/
			closePstmtRs(pStmt, rs);
			
			
			if(!"Y".equalsIgnoreCase(confirmed))
			{
				 if ("E".equalsIgnoreCase(wf_status) ||  "".equalsIgnoreCase(wf_status)) 
				{
					GenericWorkflowClass wfGenericClass = new GenericWorkflowClass();
					retString = wfGenericClass.invokeWorkflow(conn, tranID, xtraParams, "w_cust_disc_apprv", "cust_disc_apprv");
					if ("success".equalsIgnoreCase(retString)) 
					{
						sql = "UPDATE DISC_APR_STRG SET WF_STATUS = 'S' WHERE TRAN_ID = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, tranID);
						int updCount = pStmt.executeUpdate();
					    System.out.println("updCount000 : " + updCount);

							
						if (updCount > 0) 
						{
							errString = new ITMDBAccessEJB().getErrorString("", "VFMWRKFLWS", "","",conn);
							conn.commit();
						}
						else
						{
							errString = new ITMDBAccessEJB().getErrorString("", "VFWFINTERR", "","",conn);
	    					isError=true;
	    					conn.rollback();
						}
					} 
					else 
					{
						errString = new ITMDBAccessEJB().getErrorString("", "VFWFINTERR", "","",conn);
					}
				} 
				else
				{
					errString = new ITMDBAccessEJB().getErrorString("", "WFSTATUS", "","",conn);
				}
			}
			else
			{
				errString = new ITMDBAccessEJB().getErrorString("", "VTINVSUB3", "","",conn);
			}
		} 

		catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pStmt != null) {
						pStmt.close();
						pStmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		
		return errString;
	}
	public String rejection(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String userInfoStr = "";
		String errString = "";
		try
		{
			userInfoStr = rejection(tranID, xtraParams, forcedFlag, userInfoStr);
			System.out.println("userInfoStr of rejection:::::11 " +userInfoStr);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [ConsumpIssueConfWfEJB] rejection " + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
	}
	
	public String rejection(String tranID, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException
	{
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		ConnDriver connDriver  = new ConnDriver();
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String confirmed = "",retString = "", tran_id= "", wf_status = "";
		boolean isError = false;
		
		try {
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			//conn = getConnection();
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
	    	String transDB       = userInfo.getTransDB();
	    	if(userInfoStr != null && userInfoStr.trim().length() > 0)
			{
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
			conn.setAutoCommit(false);
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			tran_id = checkNull((tranID));
			//userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
		//	tran_id = checkNull((tranID));
			/*itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			conn = getConnection();
			conn.setAutoCommit(false);
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");*/
			tran_id = checkNull((tranID));

			sql ="SELECT CONFIRMED,WF_STATUS FROM DISC_APR_STRG WHERE TRAN_ID = ? ";

			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tran_id);
			rs = pStmt.executeQuery();
			
			if (rs.next()) 
			{
				confirmed = checkNull(rs.getString("confirmed"));
				wf_status = checkNull(rs.getString("WF_STATUS"));
			}
			rs.close();
			rs = null;
			pStmt.close();
			pStmt = null;

			if(!"Y".equalsIgnoreCase(confirmed))
			{
				if ( "E".equalsIgnoreCase(wf_status) || "S".equalsIgnoreCase(wf_status))
				{
					sql = "UPDATE DISC_APR_STRG SET WF_STATUS = 'E' WHERE TRAN_ID = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, tran_id);
					int updCount = pStmt.executeUpdate();
					pStmt.close();
					pStmt = null;
						
					if (updCount > 0) 
					{
						retString="Y";
						conn.commit();
						
					}
					else
					{
						errString = new ITMDBAccessEJB().getErrorString("", "VFMWFFAIL", "","",conn);
	    				isError=true;
	    				conn.rollback();
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
		
		
		catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pStmt != null) {
						pStmt.close();
						pStmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
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
	
	private void closePstmtRs(PreparedStatement pStmt, ResultSet rs) {
		if (pStmt != null) {
			try {
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
