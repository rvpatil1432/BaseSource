package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;




public class CustomerMasterWF {

	
	public String escalate(String tranId, String empCodeAprv, String xmlDataAll, String processId, String roleCode, String activityId) throws ITMException, Exception {
		System.out.println("CustomerMasterWF Parameters ::: tranId["+tranId+"],empCodeAprv["+empCodeAprv+"],xmlDataAll["+xmlDataAll+"],processId["+processId+"],roleCode["+roleCode+"],activityId["+activityId+"]");
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
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection() ;
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat  = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
			currDate          = simpleDateFormat.format( Calendar.getInstance().getTime() );
			currDate          = genericUtility.getValidDateTimeString( currDate, genericUtility.getApplDateTimeFormat(),genericUtility.getDBDateTimeFormat() ) ;

			sql = "select SIGN_STATUS from OBJ_SIGN_TRANS "
				+ "WHERE ltrim(rtrim(REF_SER)) = 'CU_ID' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
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
						+ "WHERE ltrim(rtrim(REF_SER)) = 'CU_ID' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
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
						+ "WHERE ltrim(rtrim(REF_SER)) = 'CU_ID' AND REF_ID = ? AND PROCESS_ID = ? AND INSTANCE_ID = ? AND ACTIVITY_ID = ? AND PROCESS_STATUS != 3";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(currDate));
				pstmt.setString(2, tranId);
				pstmt.setString(3, processInfo[0]);
				pstmt.setString(4, processInfo[1]);
				pstmt.setString(5, activityId);
				resultCnt += pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("Update Successfully:"+resultCnt);
			} 
			else 
			{
				retString = "N";
			}
		}
		catch (Exception e) {
			System.out.println("Exception: CustomerMasterWF escalate method ..> " + e.getMessage());
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
						System.out.println("CustomerMasterWF escalate: Connection Rollback");
					} else {
						conn.commit();
						retString = "Y";
						System.out.println("CustomerMasterWF escalate: Connection Commit");
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
	
	public String approverSignStatusUpdate(String tranId, String empCodeAprv, String xmlDataAll, String processId, String roleCode, String activityId, String signStatusFlag) throws ITMException, Exception {
		System.out.println("Parameters ::: tranId["+tranId+"],empCodeAprv["+empCodeAprv+"],xmlDataAll["+xmlDataAll+"],processId["+processId+"],roleCode["+roleCode+"],signStatusFlag["+signStatusFlag+"]");
		String retString = "",signRemarks = "",empName = "";
		Boolean isError = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		E12GenericUtility genericUtility = null;
		SimpleDateFormat  simpleDateFormat = null;
		String currDate = "";
		String userCode = "",processInfo[] = null;
		int resultCnt = 0;
		Connection conn = null;
		ConnDriver connDriver = null;
		empCodeAprv = checkNull(empCodeAprv);
		tranId = checkNull(tranId);
		processId = checkNull(processId);
		signStatusFlag = checkNull(signStatusFlag);
		roleCode = checkNull(roleCode);
		
		try
		{
			connDriver = new ConnDriver();
			genericUtility = new E12GenericUtility();
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection() ;
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat  = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
			currDate          = simpleDateFormat.format( Calendar.getInstance().getTime() );
			currDate          = genericUtility.getValidDateTimeString( currDate, genericUtility.getApplDateTimeFormat(),genericUtility.getDBDateTimeFormat() ) ;
			System.out.println(" approverSignStatusUpdate :: currDate :: ["+currDate+"]");
				
			sql = "select u.code,e.emp_fname || ' ' || e.emp_lname as emp_name from users u inner join employee e on e.emp_code = u.emp_code where e.emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCodeAprv);
			rs = pstmt.executeQuery();
			
			if (rs.next()) 
			{
				userCode = checkNull(rs.getString("code"));
				empName  = checkNull(rs.getString("emp_name"));
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if("S".equalsIgnoreCase(signStatusFlag)){
				signRemarks = "Transaction Approved by "+empName+".";
			}
			else if("R".equalsIgnoreCase(signStatusFlag)){
				signRemarks = "Transaction Rejected by "+empName+".";
			}
			else{
				signRemarks = "Transaction Approved by "+empName+".";
			}
			
			sql = "UPDATE OBJ_SIGN_TRANS "
					+ "SET SIGN_DATE = ?, USER_ID__SIGN = ?, SIGN_REMARKS = '"+signRemarks+"', SIGN_STATUS = 'V', EMP_CODE = ? "
					+ "WHERE ltrim(rtrim(REF_SER)) = 'CU_ID' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
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
			sql = "UPDATE WF_PRC_STATUS SET STATUS_DATE = ?, PROCESS_STATUS = 4, ERR_STATUS = '0', STATUS_REMARKS = '"+signRemarks+"' " +
				  "WHERE ltrim(rtrim(REF_SER)) = 'CU_ID' AND ltrim(rtrim(REF_ID)) = ? AND PROCESS_ID = ? AND INSTANCE_ID = ? AND ACTIVITY_ID = ? AND PROCESS_STATUS != 3";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(currDate));
			pstmt.setString(2, tranId);
			pstmt.setString(3, processInfo[0]);
			pstmt.setString(4, processInfo[1]);
			pstmt.setString(5, activityId);
			resultCnt += pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			System.out.println("Update Successfully:"+resultCnt);
		}
		catch (Exception e) {
			System.out.println("Exception: approverSignStatusUpdate method ..> " + e.getMessage());
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
						System.out.println("approverSignStatusUpdate: Connection Rollback");
					} else {
						conn.commit();
						retString = "Y";
						System.out.println("approverSignStatusUpdate: Connection Commit");
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
	public String checkEscalationStatus(String tranId, String empCodeAprv, String xmlDataAll, String processId, String roleCode, String activityId) throws ITMException, Exception {
		System.out.println("checkEscalationStatus Parameters ::: tranId["+tranId+"],empCodeAprv["+empCodeAprv+"],xmlDataAll["+xmlDataAll+"],processId["+processId+"]");
		String retString = "";
		Boolean isError = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "",prcId = "", instanceId = "",processInfo[] = null;
		int escalationCnt = 0, finAprvObjTransCnt = 0;
		Connection conn = null;
		ConnDriver connDriver = null;
		empCodeAprv = checkNull(empCodeAprv);
		tranId = checkNull(tranId);
		processId = checkNull(processId);
		roleCode = checkNull(roleCode);
		
		try
		{
			connDriver     = new ConnDriver();
			conn           = connDriver.getConnectDB("DriverITM");
			//conn = getConnection() ;
			conn.setAutoCommit(false);
			connDriver     = null;
			processInfo    = processId.split(":");
			prcId          = checkNull(processInfo[0]);
			instanceId     = checkNull(processInfo[1]);
			System.out.println(" checkEscalationStatus :: prcId :: ["+prcId+"]"+" instanceId: "+instanceId);
				
			sql = "SELECT COUNT(DISTINCT(W.ACTIVITY_ID)) AS COUNT FROM OBJ_SIGN_TRANS O INNER JOIN WF_PRC_STATUS W " +
				  "ON O.REF_ID = W.REF_ID WHERE O.SIGN_STATUS='V' AND W.PROCESS_STATUS = 4 AND O.REF_ID ='"+tranId+"' AND W.INSTANCE_ID  = "+instanceId+" ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			if (rs.next()) 
			{
				escalationCnt = rs.getInt("COUNT");
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = "SELECT COUNT(W.ACTIVITY_ID) AS COUNT FROM WF_PRC_STATUS W WHERE W.ACTIVITY_ID = '"+activityId+"' AND W.REF_ID ='"+tranId+"' AND W.INSTANCE_ID  = "+instanceId+" ";
		
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			if (rs.next()) 
			{
				finAprvObjTransCnt = rs.getInt("COUNT");
			}
	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("escalationCnt :"+escalationCnt+" finAprvObjTransCnt:"+finAprvObjTransCnt);
			
		}
		catch (Exception e) {
			System.out.println("Exception: checkEscalationStatus method ..> " + e.getMessage());
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
					if (isError || !(finAprvObjTransCnt < 1)) {
						retString = "N";
					} else {
						retString = "Y";
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
	public String checkWorkflowStatus(String tranId, String empCode, String transInfoXml, String processInstancetWf) throws ITMException, Exception {
		System.out.println("Parameters ::: tranId["+tranId+"],empCode["+empCode+"],transInfoXml["+transInfoXml+"],processInstancetWf["+processInstancetWf);
		String retString = "", sql = "";
		Boolean isError = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = null;
		int custCount = 0;
		tranId = checkNull(tranId);
		
		try
		{
			connDriver         = new ConnDriver();
			conn               = connDriver.getConnectDB("DriverITM");
			//conn = getConnection() ;
			connDriver         = null;
			
			sql = "select count(cust_code) as count from customer where cust_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			
			while (rs.next()) 
			{
				custCount = rs.getInt("count");
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(custCount > 0){
				retString = "N";
			}
			else {
				retString = "Y";
			}
		}
		catch (Exception e) {
			System.out.println("Exception: CustomerMasterWF checkWorkflowStatus method ..> " + e.getMessage());
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
					if (isError) {
						retString = "N";
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
	private String checkNull(String str) {
		if(str == null){
			str = "";
		}
		else{
			str = str.trim();
		}
		return str;
	}
}
