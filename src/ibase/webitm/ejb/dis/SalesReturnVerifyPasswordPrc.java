/* Developed By Pavan Rane.
 * Date : 12-JUN-2019
 * Component Name : SalesReturnVerifyPasswordPrc
 */
package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.adv.SalesReturnConfirm;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.ejb.Stateless;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;

/**
 * Session Bean implementation class EmpMaritalStatusConfPrc
 */
@Stateless

public class SalesReturnVerifyPasswordPrc extends ProcessEJB implements SalesReturnVerifyPasswordPrcRemote, SalesReturnVerifyPasswordPrcLocal 
{

	E12GenericUtility genericUtility = new E12GenericUtility();
	public String process() throws RemoteException, ITMException {
		return "";
	}

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException {		
		String retString = "";
		Document detailDom = null;
		Document headerDom = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				headerDom = genericUtility.parseString(xmlString);
				System.out.println("headerDom" + headerDom);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) 
			{
				detailDom = genericUtility.parseString(xmlString2);
				System.out.println("detailDom" + detailDom);
			}
			retString = process(headerDom, detailDom, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception :SalesReturnVerifyPasswordPrc :process(String):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams)	throws RemoteException, ITMException 
	{
		System.out.println("-----------Calling SalesReturnVerifyPasswordIC----process(Documents)----------");
		String returnString = "", tranId = "", sql = "", lsPasswd = "", lsConfirm = "", lsEntPasswd = "";
		String lsPwdVerified = "N";
		boolean isError = false;
		Connection conn = null;
		ITMDBAccessEJB itmDBAccess = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String objName = "", compType = "";
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			itmDBAccess = new ITMDBAccessEJB();
			objName = "salesreturn_retn";
			tranId = genericUtility.getColumnValue("tran_id", detailDom);
			System.out.println("SalesReturn No: # "+tranId+"]");
			if (tranId == null || tranId.trim().length() <= 0) 
			{
				returnString = itmDBAccess.getErrorString("", "VTTRNNULL", "", "", conn);
				return returnString;
			} else 
			{
				sql = "select conf_passwd, confirmed from sreturn where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsPasswd = checkNull(rs.getString("conf_passwd"));
					lsConfirm = checkNull(rs.getString("confirmed"));					
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				System.out.println("@V@ PAVAN lsPasswd :- [" + lsPasswd + "]");
				System.out.println("@V@ PAVAN lsConfirm :- [" + lsConfirm + "]");
				if ("Y".equalsIgnoreCase(lsConfirm)) {
					returnString = itmDBAccess.getErrorString("", "VTMCONF1", "", "", conn);
					return returnString;
				} 
				else 
				{
					if (lsPasswd != null && lsPasswd.trim().length() > 0) 
					{
						lsEntPasswd = checkNull(genericUtility.getColumnValue("conf_passwd", detailDom));
						System.out.println("@V@ PAVAN lsEntPasswd :- [" + lsEntPasswd + "]");
						if (lsEntPasswd == null || lsEntPasswd.trim().length() <= 0	|| !(lsEntPasswd.equals(lsPasswd))) 
						{
							returnString = itmDBAccess.getErrorString("", "VPINVDPSWD", "", "", conn);
							return returnString;
						} else 
						{
							if(lsEntPasswd.equals(lsPasswd))
							{
								lsPwdVerified = "Y";
							}
						}
					} /*else {
						lsPwdVerified = "Y";
					}*/
					if ("Y".equalsIgnoreCase(lsPwdVerified)) {

						sql = "select comp_type from system_events where obj_name = ? and EVENT_CODE = 'pre_confirm'";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, objName);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							compType = checkNull(rs.getString("comp_type"));
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						// conn.commit();
						System.out.println("@V@ Commiting befor SalesReturnConfirm 144");
						if ("JB".equalsIgnoreCase(compType)) {
							SalesReturnConfirm sReturnConf= new SalesReturnConfirm();
							returnString = sReturnConf.confirm(tranId,xtraParams,"N",conn);
						} else 
						{
							returnString = sReturnConfirm(tranId, objName, xtraParams, conn);
						}
					}
				} 
			} // TRAN_ID NOT NULL

			if (returnString.length() > 0 && (returnString.indexOf("VTCONF") > -1 || returnString.indexOf("VTSRTRNCMP") > -1 )) 
			{
				//returnString = itmDBAccess.getErrorString("", "PRCSUCES", "", "", conn);
			} else 
			{
				isError = true;
				System.out.println("SalesReturnVerifyPasswordPrc isError ::[" + isError + "]");
				// returnString = itmDBAccess.getErrorString("", "PRCFAIL", "", "", connection);
			}
		} catch (Exception e) {
			isError = true;
			System.out.println("Exception :SalesReturnVerifyPasswordPrc :process(Documnet):"+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			System.out.println("Finally Closing Connection....");
			try {
				if (isError) {
					conn.rollback();
					System.out.println("SalesReturnVerifyPasswordPrc connection rollback");
				} else {
					conn.commit();
					System.out.println("SalesReturnVerifyPasswordPrc connection committed");
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				returnString = e.getMessage();
				e.printStackTrace();
			}
		}
		System.out.println("errString...:: " + returnString);
		return returnString;
	}

	private String checkNull(String input) {
		if (input == null || ("null").equalsIgnoreCase(input.trim())) {
			input = "";
		} else {
			input = input.trim();
		}
		return input;
	}

	public String sReturnConfirm(String tranId, String objName, String xtraParams, Connection conn)
			throws ITMException {
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			System.out.println("sReturnConfirm tranId:[" + tranId + "] objName:["+objName+"]xtraParams: ["+xtraParams+"]");

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, objName);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			rs.close();
			rs = null;
			pStmt.close();
			pStmt = null;
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, serviceCode);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				serviceURI = rs.getString("SERVICE_URI");
			}
			rs.close();
			rs = null;
			pStmt.close();
			pStmt = null;
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			/*call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tab_xml_data_1"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tab_xml_data__all"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"),
					XMLType.XSD_STRING, ParameterMode.IN);*/
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);
			String nullString = "";
			if (tranId == null) {
				tranId = "";
			}
			System.out.println("compName [" + compName + "]tab_xml_data_1 [" + tranId.trim() + "tab_xml_data__all ["
					+ nullString + "\n]");
			aobj[0] = new String(compName);
			aobj[1] = new String(tranId.trim());
			aobj[2] = new String(xtraParams);
			aobj[3] = new String("N");// forcedflag
			
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String) call.invoke(aobj);
			System.out.println("Return string from NVO is:==>[" + retString + "]");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (Exception e) {
			}
		}
		return retString;
	}
}