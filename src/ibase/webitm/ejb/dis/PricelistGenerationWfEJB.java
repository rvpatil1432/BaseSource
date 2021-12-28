package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.Stateless;

@Stateless
public class PricelistGenerationWfEJB extends ActionHandlerEJB implements PricelistGenerationWfEJBLocal, PricelistGenerationWfEJBRemote {
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException {
		System.out.println("@@@@@@@@@@@@@@@@@@@@ PricelistGenerationWfEJB @@@@@@@@@@@@@@@@");
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String status = "",confirmed = "",retString = "";
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

			System.out.println("tran_id ..> " + tranId+" userId: "+userId+" xtraParams: "+xtraParams+" forcedFlag: "+forcedFlag);
			tranId = checkNull(tranId);

			sql = " select wf_status,confirmed from pricelist_hdr where tran_id = ? ";

			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranId);
			rs = pStmt.executeQuery();
			
			if (rs.next()) {
				status     = rs.getString("wf_status");
				confirmed  = rs.getString("confirmed");
			}
			
			status    = checkNull(status);
			confirmed = checkNull(confirmed);
			closePstmtRs(pStmt, rs);

			if(!"Y".equalsIgnoreCase(confirmed)){
				if ("O".equalsIgnoreCase(status) || "R".equalsIgnoreCase(status) || "".equalsIgnoreCase(status)) {
						GenericWorkflowClass wfGenericClass = new GenericWorkflowClass();
						retString = wfGenericClass.invokeWorkflow(conn, tranId, xtraParams, "w_pricelist_tran", "pricelist_tran");
						if ("success".equalsIgnoreCase(retString)) {
							
							sql = "UPDATE PRICELIST_HDR SET WF_STATUS = 'S' WHERE TRAN_ID = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, tranId);
							int updCount = pStmt.executeUpdate();
							
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
				else {
					errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALINT", "","",conn);
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
			if (conn != null) {
				try {
					if (isError) {
						conn.rollback();
						System.out.println("PricelistGenerationWfEJB: confirm: Connection Rollback");
					} else {
						conn.commit();
						System.out.println("PricelistGenerationWfEJB: confirm: Connection Commit");
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


	private String checkNull(String str) {
		if(str == null){
			str = "";
		}
		else{
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
