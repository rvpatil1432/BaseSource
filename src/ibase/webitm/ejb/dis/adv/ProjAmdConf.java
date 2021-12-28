package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import javax.ejb.Stateless;

@Stateless
public class ProjAmdConf extends ActionHandlerEJB implements ProjAmdConfRemote,
		ProjAmdConfLocal {

	public ProjAmdConf() {
	}

	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		String projCode = "";
		String confirmed = "";
		Timestamp endDate = null,extEndDate = null;
		Double approxCost = 0.0;
		try {
			System.out.println("helloconfirm*****ProjAmdConf2******************");
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			ibase.webitm.ejb.ValidatorEJB valicateEjb = new ibase.webitm.ejb.ValidatorEJB();

			String userId = valicateEjb.getValueFromXTRA_PARAMS(xtraParams, "loginCode");System.out.println("login emp code::"+userId);

			sql = "select confirmed from project_amd where amd_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				confirmed = rs.getString(1);
			}
			confirmed = confirmed == null ? "" : confirmed.trim();

			if (confirmed.length() > 0 && confirmed.equalsIgnoreCase("Y")) {
				errString = itmDBAccessEJB.getErrorString("", "VTCONFMD", "", "", conn);// Already Confirmed
			} else {
				if (errString == null || errString.trim().length() == 0) {
					int updCol = 0;
					sql = "select project_amd.proj_code, project_amd.end_date, project_amd.approx_cost,project_amd.ext_end_date from project project, project_amd project_amd where project_amd.proj_code = project.proj_code and project_amd.amd_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranID);
					rs = pstmt.executeQuery();

					while (rs.next()) {
						projCode = rs.getString("proj_code");
						endDate = rs.getTimestamp("end_date");
						approxCost = rs.getDouble("approx_cost");
						extEndDate = rs.getTimestamp("ext_end_date");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					
					//Update Project table with end_date and approx_cost
					sql = "update project set end_date = ?,approx_cost = ?, ext_end_date = ? where proj_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, endDate);
					pstmt.setDouble(2, approxCost);
					pstmt.setTimestamp(3, extEndDate);
					pstmt.setString(4, projCode);
					updCol = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					
					//If any project gets updated set confirm for amd_no in project_amd
					if(updCol > 0){
						sql = "update project_amd set confirmed = 'Y',appr_by = ? , appr_date = ? where amd_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, userId);
						pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
						pstmt.setString(3, tranID);
						pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
					}
				}
			}
			if (errString != null && errString.trim().length() > 0) {
				conn.rollback();
				return errString;
			} else {
				conn.commit();
				errString = itmDBAccessEJB.getErrorString("", "VTCNFSUCC", "","",conn);
			}
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception t) {
			}
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTDESNCONF", "", "", conn);
			throw new ITMException(e);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				pstmt = null;
				if (conn != null) {
					conn.close();
				}
				conn = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return errString;
	}

	
}
