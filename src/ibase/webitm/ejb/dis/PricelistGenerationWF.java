package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;


public class PricelistGenerationWF {

	public String confirm(String tran_id, String empCodeAprv, String loginSiteCode, String keyFlag, String remarks) throws RemoteException, ITMException {
		System.out.println("PricelistGenerationWF confirm ::: tran_id["+tran_id+"],empCodeAprv["+empCodeAprv+"],keyFlag["+keyFlag+"]");
		String loginCode = "",compType = "",xtraParams = "", retString = "",sql = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		E12GenericUtility genericUtility = null;
		loginSiteCode = checkNull(loginSiteCode);
		PriceListConf priceListConf = null;
		boolean connStatus= false;
		
		if("C".equalsIgnoreCase(keyFlag) || "R".equalsIgnoreCase(keyFlag)){
			retString = updateWfStatus(tran_id, empCodeAprv, loginSiteCode, keyFlag, remarks);
		}
		else{
		   try {
				conn = connDriver.getConnectDB("DriverITM");
			    //conn = getConnection();
				connDriver = null;
				conn.setAutoCommit(false);
				empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
				
				String methodName = "";
				String compName = "";
				String businessObj = "pricelist_tran";
				String eventCode = "pre_confirm";
				String serviceCode = "";
				String serviceURI = "";
				String actionURI = "";
	
				sql = " select site_code__def,code from users where emp_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, empCodeAprv);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					loginCode     = checkNull(rs.getString("code"));
					loginSiteCode = checkNull(rs.getString("site_code__def"));
				}
	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
	
				xtraParams = "loginCode=" + loginCode + "~~" + "loginSiteCode=" + loginSiteCode + "~~" + "loginEmpCode=" + empCodeAprv;
	
				genericUtility = new E12GenericUtility();
				methodName = "gbf_post";
				actionURI = "http://NvoServiceurl.org/" + methodName;
	
				sql = "SELECT SERVICE_CODE,COMP_NAME,COMP_TYPE FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, businessObj);
				pstmt.setString(2, eventCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					serviceCode = checkNull(rs.getString("SERVICE_CODE"));
					compName    = checkNull(rs.getString("COMP_NAME"));
					compType    = checkNull(rs.getString("COMP_TYPE"));
				}
				System.out.println("serviceCode = " + serviceCode + " compName " + compName +"compType : "+compType);
	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
	
				if("WSR".equalsIgnoreCase(compType)){
					sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, serviceCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						serviceURI = rs.getString("SERVICE_URI");
					}
					serviceURI = serviceURI == null ? "" : serviceURI.trim();
					System.out.println("serviceURI = " + serviceURI);
					
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
		
					Service service = new Service();
					Call call = (Call) service.createCall();
					call.setTargetEndpointAddress(new URL(serviceURI));
					call.setOperationName(new QName("http://NvoServiceurl.org", methodName));
					call.setUseSOAPAction(true);
					call.setSOAPActionURI(actionURI);
					Object[] aobj = new Object[4];
		
					call.addParameter(new QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
					call.addParameter(new QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
					call.addParameter(new QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
					call.addParameter(new QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);
		
					aobj[0] = new String(compName);
					aobj[1] = new String(tran_id);
					aobj[2] = new String(xtraParams);
					System.out.println("@@@@@@@@@@loginEmpCode:" + genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode") + ":");
					call.setReturnType(XMLType.XSD_STRING);
					retString = (String) call.invoke(aobj);
					System.out.println("PricelistGenerationWF Confirm Complete @@@@@@@@@@@Return string from NVO is:==>[" + retString + "]");
				}
				else{
					priceListConf = new PriceListConf();
					retString = priceListConf.confirm(tran_id, xtraParams, "", conn, connStatus);
					System.out.println("PricelistGenerationWF Confirm Complete @@@@@@@@@@@Return string from EJB is:==>[" + retString + "]");

				}
				if ((retString.indexOf("success") > -1) || (retString.indexOf("Success") > -1) || (retString.indexOf("VTSUCC1") > -1) || (retString.indexOf("VTCICONF3") > -1)) {
					System.out.println("Transaction Successful");
					retString = "Y";
				} else {
					retString = "N";
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception in PricelistGenerationWF in confirm()");
				throw new ITMException(e);
			} finally {
				if (conn != null) {
					try {
						conn.close();
						conn = null;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Return string from PricelistGenerationWF confirm method is: "+retString);
		return retString;
	}

	public String updateWfStatus(String tran_id,String empCodeAprv, String loginSiteCode, String wfStatus, String remarks) throws ITMException {
		String  retString = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		int cnt = 0;
		boolean isError = false;
		wfStatus = checkNull(wfStatus);
		remarks = checkNull(remarks);
		try
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			if ("C".equalsIgnoreCase(wfStatus) || "R".equalsIgnoreCase(wfStatus)) {
				sql = "UPDATE PRICELIST_HDR SET WF_STATUS = '"+wfStatus+"',REMARKS = '"+remarks+"' WHERE TRAN_ID = ?";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tran_id);
				cnt = pStmt.executeUpdate();
			} 
			else{
				System.out.println("Workflow In Progress");
			}
		}
		catch (Exception e)
		{
			isError = true;
			System.out.println("Exception : PricelistGenerationWF : updateWfStatus() : ==>\n" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs !=null)
    			{
    				rs.close();rs=null;
    			}
    			if(pStmt != null)
                {
                	pStmt.close();pStmt = null;
                }
    			if (conn != null) {
    				try {
    					if (isError || !(cnt > 0)) {
    						conn.rollback();
    						retString = "N";
    						System.out.println("PricelistGenerationWF updateWfStatus: Connection Rollback");
    					} else {
    						conn.commit();
    						retString = "Y";
    						System.out.println("PricelistGenerationWF updateWfStatus: Connection Commit");
    					}
    					conn.close();
    					conn = null;
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
			}
			catch(SQLException sqlEx)
			{
				System.out.println("Exception in updateWfStatus() Finally "+sqlEx.getMessage());
				sqlEx.printStackTrace();
			}
		}
		return retString;
	}
	
	public String escalate(String tranId, String empCodeAprv, String xmlDataAll, String processId, String roleCode, String activityId) throws ITMException, Exception {
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
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat  = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
			currDate          = simpleDateFormat.format( Calendar.getInstance().getTime() );
			currDate          = genericUtility.getValidDateTimeString( currDate, genericUtility.getApplDateTimeFormat(),genericUtility.getDBDateTimeFormat() ) ;

			sql = "select SIGN_STATUS from OBJ_SIGN_TRANS "
				+ "WHERE ltrim(rtrim(REF_SER)) = 'P-LST' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
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
						+ "WHERE ltrim(rtrim(REF_SER)) = 'P-LST' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
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
						+ "WHERE ltrim(rtrim(REF_SER)) = 'P-LST' AND REF_ID = ? AND PROCESS_ID = ? AND INSTANCE_ID = ? AND ACTIVITY_ID = ? AND PROCESS_STATUS != 3";
				
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
			System.out.println("Exception: PricelistGenerationWF escalate method ..> " + e.getMessage());
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
						System.out.println("PricelistGenerationWF escalate: Connection Rollback");
					} else {
						conn.commit();
						retString = "Y";
						System.out.println("PricelistGenerationWF escalate: Connection Commit");
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
			//conn = getConnection();
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
					+ "WHERE ltrim(rtrim(REF_SER)) = 'P-LST' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
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
				  "WHERE ltrim(rtrim(REF_SER)) = 'P-LST' AND REF_ID = ? AND PROCESS_ID = ? AND INSTANCE_ID = ? AND ACTIVITY_ID = ? AND PROCESS_STATUS != 3";
			
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
			//conn = getConnection();
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
