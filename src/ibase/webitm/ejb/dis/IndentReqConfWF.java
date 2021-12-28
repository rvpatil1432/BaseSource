package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

public class IndentReqConfWF 
{
	public String confirm(String tran_id, String empCodeAprv, String xmlDataAll, String processId, String keyFlag, String userInfoStr) throws RemoteException, ITMException 
	{
		System.out.println("confirm workflow invocation");
		System.out.println("Parameters ::: tranId[" + tran_id + "], empCodeAprv[" + empCodeAprv + "], xmlDataAll[" + xmlDataAll + "],processId["+processId+"],keyFlag["+keyFlag+"]");
		String loginCode = "";
		String loginSiteCode = "";
		String loginEmpCode = "";
		String xtraParams = "loginCode=" + loginCode + "~~" + "loginSiteCode=" + loginSiteCode + "~~" + "loginEmpCode=" + loginEmpCode;
		String retString = "";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String confirmed = "", wf_status = "", status = "";
		ConnDriver connDriver = new ConnDriver();
		E12GenericUtility genericUtility = null;
		
		if("C".equalsIgnoreCase(keyFlag) || "R".equalsIgnoreCase(keyFlag)){
			retString = updateWfStatus(tran_id, empCodeAprv, loginSiteCode, keyFlag);
		}
		else{
		      try {
		    	  System.out.println("Try block of IndentReqConfWF.confirm()");
		    	  //conn = connDriver.getConnectDB("DriverITM");
		    	  //conn = getConnection();
		    	  System.out.println("Inside Indent Requisition confirm section: "+userInfoStr);
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
				  
				  empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
			  
			  
				  String methodName = "";
				  String compName = "", compType = "";
				  String businessObj = "indent_req";
				  String eventCode = "pre_confirm";
				  String serviceCode = "";
				  String serviceURI = "";
				  String actionURI = "";
				  
				  sql = " select site_code__ori, emp_code__req, chg_user from indent_hdr where ind_no = ? ";
				  pstmt = conn.prepareStatement(sql);
				  pstmt.setString(1, tran_id);
				  rs = pstmt.executeQuery();
				  if (rs.next()) {
					  loginSiteCode = rs.getString(1);
				  }
				  loginSiteCode = loginSiteCode == null ? "" : loginSiteCode.trim();
				  rs.close();
				  rs = null;
				  pstmt.close();
				  pstmt = null;
	  
				  empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
				  
				  sql = " select code from users where emp_code = ? ";
				  pstmt = conn.prepareStatement(sql);
				  pstmt.setString(1, empCodeAprv);
				  rs = pstmt.executeQuery();
				  if (rs.next()) {
					  loginCode = rs.getString(1);
				  }
	  
				  loginCode = loginCode == null ? "" : loginCode.trim();
				  rs.close();
				  rs = null;
				  pstmt.close();
				  pstmt = null;
	  
				  xtraParams = "loginCode=" + loginCode + "~~" + "loginSiteCode=" + loginSiteCode + "~~" + "loginEmpCode=" + empCodeAprv;
	  
				  genericUtility = new E12GenericUtility();
				  //genericUtility = GenericUtility.getInstance();
				  methodName = "gbf_post";
				  actionURI = "http://NvoServiceurl.org/" + methodName;
	  
				  sql = "SELECT SERVICE_CODE,COMP_NAME,COMP_TYPE FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = ? ";
				  pstmt = conn.prepareStatement(sql);
				  pstmt.setString(1, businessObj);
				  pstmt.setString(2, eventCode);
				  rs = pstmt.executeQuery();
				  if (rs.next()) {
					  serviceCode = rs.getString("SERVICE_CODE");
					  compName = rs.getString("COMP_NAME");
					  compType = rs.getString("COMP_TYPE");
				  }
				  System.out.println("serviceCode = " + serviceCode + " compName " + compName + " compType " +compType);
	  
				  rs.close();
				  rs = null;
				  pstmt.close();
				  pstmt = null;
	  
				  sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
				  pstmt = conn.prepareStatement(sql);
				  pstmt.setString(1, serviceCode);
				  rs = pstmt.executeQuery();
				  if (rs.next()) {
					  serviceURI = rs.getString("SERVICE_URI");
				  }
				  System.out.println("serviceURI = " + serviceURI + " compName = " + compName);
	  
				  rs.close();
				  rs = null;
				  pstmt.close();
				  pstmt = null;
	  
				  if("EJB".equalsIgnoreCase(compType))
				  {
					  IndentReqConf indentReqConf = new IndentReqConf();
					  indentReqConf.confirm(tran_id, xtraParams, "Y", conn);
				  }
				  else if("WSR".equalsIgnoreCase(compType))
				  {
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
					  System.out.println("Confirm Complete @@@@@@@@@@@Return string from NVO is:==>[" + retString + "]");
	  
					  if ((retString.indexOf("success") > -1) || (retString.indexOf("Success") > -1) || (retString.indexOf("Sucess") > -1)) {
						  System.out.println("Transaction Successfull");
						  retString = "Y";
					  } else {
						  System.out.println("Exception while calling WSR component");
					  }
				  }
				  
				  sql = "SELECT CONFIRMED,STATUS,WF_STATUS FROM INDENT_HDR WHERE IND_NO = ? ";

				  pstmt = conn.prepareStatement(sql);
				  pstmt.setString(1, tran_id);
				  rs = pstmt.executeQuery();
					
				  if (rs.next()) 
				  {
					  confirmed  = rs.getString("confirmed");
					  status     = rs.getString("STATUS");
					  wf_status  = rs.getString("WF_STATUS");
				  }
					
				  confirmed = checkNull(confirmed);
				  status = checkNull(status);
				  wf_status = checkNull(wf_status);
					
				  rs.close();
				  rs = null;
				  pstmt.close();
				  pstmt = null;
					
				  if("N".equalsIgnoreCase(confirmed))
				  {
					  sql = "UPDATE INDENT_HDR SET CONFIRMED = 'Y' WHERE IND_NO = ? ";
					  pstmt = conn.prepareStatement(sql);
					  pstmt.setString(1, tran_id);
					  int cnt = pstmt.executeUpdate();
					  pstmt.close();
					  pstmt = null;
					  
					  if (cnt > 0) {
						  conn.commit();
						  retString = "Y";
					  }
					  else{
						  retString = "N";
					  }

				  }
				  
			}
		catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			System.out.println("Exception occured");

			throw new ITMException(e);
		} finally {
			if (conn != null) {
				try {
					conn.commit();
					conn.close();
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Exception in Finally");
				}
			}
			}
		}
		System.out.println("Return string from IndentReqConf confirm method is: "+retString);
		return retString;
	
	}
	
	public String updateWfStatus(String tran_id, String empCodeAprv, String loginSiteCode, String wfStatus) throws ITMException {
		String  retString = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		
		try
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			if ("C".equalsIgnoreCase(wfStatus)) {
				sql = "UPDATE INDENT_HDR SET WF_STATUS = 'C' WHERE TRAN_ID = ?";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tran_id);
				int cnt = pStmt.executeUpdate();
				if (cnt > 0) {
					conn.commit();
					retString = "Y";
				}
				else{
					retString = "N";
				}
			} else if("R".equalsIgnoreCase(wfStatus)){
				sql = "UPDATE INDENT_HDR SET WF_STATUS = 'R' WHERE TRAN_ID = ?";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tran_id);
				int cnt = pStmt.executeUpdate();
				if (cnt > 0) {
					conn.commit();
					retString = "Y";
				}
				else{
					retString = "N";
				}
			}
			else{
				System.out.println("Workflow In Progress");
			}
		}
		catch (Exception e)
		{
			try {
					conn.rollback();
				} 
			catch (SQLException sqle) 
				{
					sqle.printStackTrace();
				}
			System.out.println("Exception : IndentReqConf : updateWfStatus() : ==>\n" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
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
				if(conn!=null)
				{
					conn.close();
					conn=null;
				}
			}
			catch(SQLException sqlEx)
			{
				System.out.println("Exception in updateWfStatus() Finally "+sqlEx.getMessage());
				sqlEx.printStackTrace();
			}
		}
		System.out.println("Return string from IndentReqConf updateWfStatus method is: "+retString);
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
