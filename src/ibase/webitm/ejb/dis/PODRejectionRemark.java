//OLD
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorEJB;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.jmx.snmp.Timestamp;

@Stateless
public class PODRejectionRemark extends ProcessEJB implements PODRejectionRemarkLocal,
PODRejectionRemarkRemote {
	String siteCodeG = "", invTypeG = "", xtraParamsG = "", schemeCodeG = "",
			multipleScheme = "";
	Map<String, Double> partialInvoiceMapG = new HashMap<String, Double>();
	Map<String, Double> partialInvMap = new HashMap<String, Double>();
	int invdoneCnt = 0;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	// Process Window Created by Sanket Girme [10-Aug-2015]
	
	
	public String process(String xmlString, String xmlString2,
			String windowName, String xtraParams) throws RemoteException,
			ITMException {

		System.out.println("enter in process(212....................");
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;

		//GenericUtility genericUtility = GenericUtility.getInstance();
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				headerDom = genericUtility.parseString(xmlString);
				System.out.println("xmlString--->>" + xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				detailDom = genericUtility.parseString(xmlString2);
				System.out.println("xmlString2 --->>" + xmlString2);
			}
			xtraParamsG = xtraParams;
			retStr = process(headerDom, detailDom, windowName, xtraParams);

		} catch (Exception e) {
			System.out
					.println("Exception :PODProcess :process(String xmlString, String xmlString2, String windowName, String xtraParams):"
							+ e.getMessage() + ":");
			e.printStackTrace();
			/*retStr = e.getMessage();*/ //Commented By Mukesh Chauhan on 05/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return retStr;

	}

	public String process() throws RemoteException, ITMException {
		return "";
	}

	public String process(Document dom, Document dom2, String windowName,
			String xtraParams) throws RemoteException, ITMException {
		System.out.println("enter in process(dom) ");
		ArrayList<String> inv45dayList = new ArrayList<String>();
		ArrayList<String> podProcessInvIDList = new ArrayList<String>();
		ArrayList<String> podNotConfList = new ArrayList<String>();
		ArrayList<String> podTraceInvList = new ArrayList<String>();
		ArrayList<String> miscDrCrInvList = new ArrayList<String>();
		ArrayList<String> PodHdrInvList = new ArrayList<String>();
		Connection conn = null;
		ConnDriver connDriver = null;
		//GenericUtility genericUtility = null;
		ProofOfDelivery podObject = null;
		ITMDBAccessEJB itmdbAccess = null;
		ValidatorEJB vdt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String rsStatus="", resConfirmed="";
		int cnt = 0,updateRows=0;
		ProofOfDeliveryReject rjct;

		double ExchRate = 0;
		String statusOfTran = "", CurrRemark = "", custCodeTo = "", invType = "", errorString = "", errCode = "", tranId = "", custCode = "";
		String sql = "", invoiceId = "", priceList = "", currCode = "";
		try {
			//genericUtility = GenericUtility.getInstance();
			itmdbAccess = new ITMDBAccessEJB();
			podObject = ProofOfDelivery.getInstance();
			vdt = new ValidatorEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			String succ="";
			//tranId = vdt.getValueFromXTRA_PARAMS(xmlString, "tran_id");
			tranId = genericUtility.getColumnValue("tran_id", dom);
			statusOfTran = genericUtility.getColumnValue("wf_status", dom);
			CurrRemark = genericUtility.getColumnValue("rej_remark", dom);
			
			statusOfTran = statusOfTran == null ? "" : statusOfTran.trim();
			CurrRemark = CurrRemark == null ? "" : CurrRemark.trim();
			custCodeTo = custCodeTo == null ? "" : custCodeTo.trim();
			invType = invType == null ? "" : invType.trim();
			System.out.println("dom-->>[" + dom + "]");
			System.out.println("dom2->>[" + dom2 + "]");
			System.out.println("windowName--->>[" + windowName + "]");
			System.out.println("xtraParams--->>[" + xtraParams + "]");
			
			System.out.println("Tran id ==================> "+tranId);
			System.out.println("Status of the Transaction=> "+statusOfTran);
			System.out.println("Cusrrent Remark ==========> "+CurrRemark);
			
			
			sql = "SELECT WF_STATUS, CONFIRMED FROM SPL_SALES_POR_HDR WHERE TRAN_ID= ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while(rs.next()){
				rsStatus= rs.getString(1);
				resConfirmed = rs.getString(2);
			}
			if(rsStatus== null) {
				//status cannnot be null
				System.out.println("Status is NUll for tran_id =>"+tranId);
				errCode = "VTSTANUL";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}else if (rsStatus.equalsIgnoreCase("O")) {
				//status cannnot be O 
				System.out.println("Status is 'O' for tran_id =>"+tranId+" is Not allowed, for Rejection..!" );
				errCode = "VTOPTCRJ";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}else if ( rsStatus.equalsIgnoreCase("R")){
				//status cannnot be R
				System.out.println("Status is 'R' for tran_id =>"+tranId+" is Not allowed, for Rejection..!" );
				errCode = "VTALREJ";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}else if (resConfirmed.equalsIgnoreCase("Y")){
				//Transaction should not be Confirmed
				System.out.println("Status is 'R' for tran_id =>"+tranId+" is Not allowed, for Rejection..!" );
				errCode = "VTCONFCRJ";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}
			
			
			
			if(tranId==null || tranId.isEmpty()){
				System.out.println("Transaction Id is empty or getting null");
				errCode = "VTTRANUL";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}if(statusOfTran==null || statusOfTran.isEmpty() || statusOfTran.length()<=0){
				System.out.println("Status is empty or getting null");
				errCode = "VTSTANUL";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}if(CurrRemark==null || CurrRemark.isEmpty() || CurrRemark.length()<=0){
				System.out.println("Remark is empty or getting null");
				errCode = "VTREJNOTAC";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}
			
			if(CurrRemark!=null && statusOfTran.equalsIgnoreCase("S") && resConfirmed.equalsIgnoreCase("N") ){
				System.out.println("Updating the remark status of the tran_id =>"+tranId);
				
				sql = "UPDATE SPL_SALES_POR_HDR SET REMARKS = ? WHERE TRAN_ID= ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, CurrRemark);
				pstmt.setString(2, tranId);
				updateRows = pstmt.executeUpdate();
				if(updateRows>=0){
					conn.commit();
					rjct = new ProofOfDeliveryReject();
					
					// call to Reject EJB of the ProofOfDeliveryReject.
					succ = rjct.actionHandler(tranId,xtraParams,xtraParams) ;
					if(!succ.isEmpty()){
						System.out.println("Remark Updated Successfully! Of Tran_id => "+tranId);
						
						errCode = "VTREJSUCF";
						errorString = itmdbAccess.getErrorString("", errCode, "",
								"", conn);
						return errorString;
					}else{
						
					}
					
					
				}else{
					conn.rollback();
					System.out.println("Remark Not Updated!!! \n Somthing went wrong!!");
					errCode = "VTALREJ";
					errorString = itmdbAccess.getErrorString("", errCode, "",
							"", conn);
					return errorString;
				}
			}else{
				System.out.println("Remark Not Updated, Because the Status is =>"+statusOfTran+"<= of the tran_id =>"+tranId);
				errCode = "VTALREJ";
				errorString = itmdbAccess.getErrorString("", errCode, "",
						"", conn);
				return errorString;
			}
			
		}// end try
		catch (Exception e) {
			System.out.println("Exception in PODProcess Class---------");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return errorString;

	}

	


	private String newErrorMessage(String errString, String invid) throws ITMException {
		System.out.println("In newErrorMessage method--->>[" + errString + "]");
		String trace = "", message = "", desc = "", redirect = "", domID = "";
		Node parentNode = null;
		Node childNode = null;
		NodeList childNodeList = null;
		StringBuffer newStr = new StringBuffer();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = new ValidatorEJB().parseString(errString);
			doc.getDocumentElement().normalize();
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());
			NodeList parentNodeList = doc.getElementsByTagName("error");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			String demo = genericUtility.serializeDom(doc);
			System.out.println("childNodeList length----------------"
					+ parentNodeList.getLength());
			for (int i = 0; i < childNodeList.getLength(); i++) {
				childNode = childNodeList.item(i);
				if (childNode == null) {
					System.out.println("child Node is null..............");
				}
				// System.out.println("ChildNode
				// Name--->>"+childNode.getFirstChild().getTextContent());
				if ("message".equalsIgnoreCase(childNode.getNodeName())) {
					message = childNode.getFirstChild() == null ? ""
							: childNode.getFirstChild().getNodeValue();
				} else if ("description".equalsIgnoreCase(childNode
						.getNodeName())) {
					desc = childNode.getFirstChild() == null ? "" : childNode
							.getFirstChild().getNodeValue();
				} else if ("trace".equalsIgnoreCase(childNode.getNodeName())) {
					trace = childNode.getFirstChild() == null ? "" : childNode
							.getFirstChild().getNodeValue();
				} else if ("redirect".equalsIgnoreCase(childNode.getNodeName())) {
					redirect = childNode.getFirstChild() == null ? ""
							: childNode.getFirstChild().getNodeValue();
				} else if ("detailDomId".equalsIgnoreCase(childNode
						.getNodeName())) {
					domID = childNode.getFirstChild() == null ? "" : childNode
							.getFirstChild().getNodeValue();
				}

			}
			trace = trace + " Invoice Id : " + invid;

			newStr.append(errString.substring(0,
					errString.indexOf("message") - 1));
			newStr.append("<message><![CDATA[" + message + "]]></message>"
					+ "<description><![CDATA[" + desc + "]]></description>");
			newStr.append("<trace><![CDATA[" + trace + "]]></trace>");
			newStr.append("<redirect><![CDATA[" + redirect + "]]></redirect>");
			newStr.append("<detailDomId><![CDATA[" + domID
					+ "]]></detailDomId>");

			newStr.append("</error></Errors>");
			if (errString.contains("Root")) {
				newStr.append("</Root>");
			}

			System.out.println("Final xml in newErrorMessage--->>[["
					+ newStr.toString() + "]]");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return newStr.toString();
	}
}
