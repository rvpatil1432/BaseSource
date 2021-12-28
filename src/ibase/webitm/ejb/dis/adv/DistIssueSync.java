package ibase.webitm.ejb.dis.adv;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import javax.ejb.Stateless;
/*import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;*/
/*import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;*/
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

@Stateless
public class DistIssueSync extends ActionHandlerEJB implements DistIssueSyncLocal, DistIssueSyncRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon Fcommon = new FinCommon();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	FinCommon finCommon = new FinCommon();
	DistCommon disCommon = new DistCommon();

	public String confirm(String tranId, String xtraParams, String forcedFlag) throws ITMException {
		/*
		 * //492 505 519 404 603 604 605
		 */
		System.out.println(" -- DistIssueSync Called --");
		String retString = "";
		Connection conn = null;
		try {
			conn = getConnection();
			retString = confirm(tranId, xtraParams, forcedFlag, conn);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.out.println("Exception in [DistOrderConf] " + exception.getMessage());
			throw new ITMException(exception);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				conn = null;
			}
		}
		System.out.println("Returnstring :::: [" + retString + "]");
		return retString;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn)
			throws RemoteException, ITMException {
		String loginCode = "", sql = "", sql1 = "", sql2 = "";
		String errString = "";
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String isConfirmed = "", syncStatus = "", distIssueTranId = "", transactionMode = "", distIssueConfrmd = "",
				requesterLocation = "", issueLocation = "";
		String siteCodeIss = "", siteCodeRcp = "";
		String itemCodeIssue = "", lotNoIssue = "", lotSlIssue = "";
		int cntIssData = 0, cntSrlContentData = 0;
		StringBuffer valueXmlStringSrlContent = new StringBuffer();
		StringBuffer valueXmlStringDistIssue = new StringBuffer();
		String srlContainerSrlNo = "", srlContentsItemCode = "", srlContentsLotSl = "";
		double srlContentsQty = 0.0;
		StringBuffer finalString = new StringBuffer(
				"<![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		String errMsg = "", errorMsg = "", errorNo = "", transactionNo = "", shippedBy = "", requester = "",
				requestTo = "";
		String soapEndpointUrl = "", soapAction = "", sysDateStr = "";
		ArrayList<String> itemGrpList = new ArrayList<String>();
		HashMap<String, String> errorInformation = new HashMap<String, String>();
		int updCount = 0, disRcpCount = 0;
		String itemGrpCode = "";
		try {
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			// Getting Current Date in format[dd/MM/yyyy] [START]
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Transaction Id is [" + tranId + "]\n" + "Current Date String is [" + sysDateStr + "]");
			// Getting Current Date in format[dd/MM/yyyy] [END]

			// Checking Confirmed and sync_status of Selected Distribution Issue [START]
			sql = "select confirmed , sync_status , loc_group__from , loc_group__to from distord_iss where tran_id=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				isConfirmed = checkNull(rs.getString("confirmed"));
				syncStatus = checkNull(rs.getString("sync_status"));
				// Commented and added by sarita because data of requesterLocation &
				// issueLocation is setting wrong[START]
				/*
				 * Om Mail Dtd : 23/12/19 IssueLocation should be set as loc_group__from instead
				 * of loc_group__to. Issue location is location from where stock is transferred
				 * to other location. requesterLocation =
				 * checkNull(rs.getString("loc_group__from")); issueLocation =
				 * checkNull(rs.getString("loc_group__to"));
				 */
				issueLocation = checkNull(rs.getString("loc_group__from"));
				requesterLocation = checkNull(rs.getString("loc_group__to"));
				// Commented and added by sarita because data of requesterLocation &
				// issueLocation is setting wrong[END]
				System.out.println("isConfirmed [" + isConfirmed + "]\t syncStatus [" + syncStatus
						+ "] \t locGroupFrom [" + requesterLocation + "] \t locGroupTo [" + issueLocation + "]");
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if ((requesterLocation == null || requesterLocation.trim().length() == 0)
					&& (issueLocation == null || issueLocation.trim().length() == 0)) {
				errString = itmDBAccess.getErrorString("", "VTINVCDLOC", loginCode, "", conn);
				return errString;
			}
			if (!isConfirmed.equalsIgnoreCase("Y")) {
				errString = itmDBAccess.getErrorString("", "VTISSTTRAN", loginCode, "", conn);

				return errString;
			} else if (("Y").equalsIgnoreCase(syncStatus)) {
				errString = itmDBAccess.getErrorString("", "VTISSSYNC", loginCode, "", conn); // Transaction has already
																								// Integrated.
				return errString;
			}

			// Checking Confirmed and sync_status of Selected Distribution Issue [END]
			shippedBy = checkNull(getShippedByUsingGencode(transactionMode, conn));

			// Get Item Code from DISTISS_SYNC_LIST from disparm [START]
			String ItemForCustomization = checkNull(disCommon.getDisparams("999999", "DISTISS_SYNC_LIST", conn));
			System.out.println("distIssSyncList : " + ItemForCustomization);
			StringTokenizer stringTokenizer = new StringTokenizer(ItemForCustomization, ":");
			while (stringTokenizer.hasMoreTokens()) {
				itemGrpList.add(stringTokenizer.nextToken());
			}
			System.out.println("Item Code in DISTISS_SYNC_LIST are " + itemGrpList);
			// Get Item Group Code from DISTISS_SYNC_LIST from disparm [END]
			requester = getGencodesData("LOC_GROUP__FROM", requesterLocation, conn);
			requestTo = getGencodesData("LOC_GROUP__TO", issueLocation, conn);
			System.out.println(
					"Unit[Requester] Code From [" + requester + "] \t Unit[RequestTo] Code To [" + requestTo + "]");

			finalString.append("<REQUESTINFO>\r\n");
			finalString.append("<MATERIALREQUISITION>\r\n");
			finalString.append("<MTLREQUISITIONINFO>\r\n");

			finalString.append("<ISSUENUMBER>").append("").append("</ISSUENUMBER>\r\n");
			finalString.append("<MQNDATE>").append(sysDateStr).append("</MQNDATE>\r\n");
			finalString.append("<MQNNUMBER>").append("").append("</MQNNUMBER>\r\n");
			finalString.append("<REQUESTER>").append(requester).append("</REQUESTER>\r\n");
			finalString.append("<REQUESTERLOCATION>").append(requesterLocation).append("</REQUESTERLOCATION>\r\n");
			finalString.append("<REQUESTTO>").append(requestTo).append("</REQUESTTO>\r\n");
			finalString.append("<ISSUELOCATION>").append(issueLocation).append("</ISSUELOCATION>\r\n");
			finalString.append("<ISSUESUBLOCATION>").append("").append("</ISSUESUBLOCATION>\r\n");
			finalString.append("<ISSUINGENTITY>").append("").append("</ISSUINGENTITY>\r\n");
			finalString.append("<SHIPPEDBY>").append(shippedBy).append("</SHIPPEDBY>\r\n");
			finalString.append("<SHIPMENTDATE>").append(sysDateStr).append("</SHIPMENTDATE>\r\n");
			finalString.append("<CONSNUMBER>").append("").append("</CONSNUMBER>\r\n");
			finalString.append("<ITEMINFO>\r\n");

			errorInformation.put("ISSUENUMBER", "");
			errorInformation.put("MQNDATE", sysDateStr);
			errorInformation.put("MQNNUMBER", "");
			errorInformation.put("REQUESTER", requester);
			errorInformation.put("REQUESTERLOCATION", requesterLocation);
			errorInformation.put("REQUESTTO", requestTo);
			errorInformation.put("ISSUELOCATION", issueLocation);
			errorInformation.put("ISSUESUBLOCATION", "");
			errorInformation.put("ISSUINGENTITY", "");
			errorInformation.put("SHIPMENTDATE", sysDateStr);

			sql = "select issdet.item_code as item_code , " + "issdet.lot_no as lot_no , "
					+ "issdet.lot_sl as lot_sl , " + "item.grp_code as item_grp_code " + "from distord_iss iss , "
					+ "distord_issdet issdet , " + "Item item  " + "where iss.tran_id = issdet.tran_id "
					+ "AND iss.dist_order = issdet.dist_order " + "AND issdet.item_code = item.item_code "
					+ "AND issdet.tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				itemCodeIssue = checkNull(rs.getString("item_code"));
				lotNoIssue = checkNull(rs.getString("lot_no"));
				lotSlIssue = checkNull(rs.getString("lot_sl"));
				itemGrpCode = checkNull(rs.getString("item_grp_code"));
				System.out.println("cntIssData[" + cntIssData + "]\titemCodeIssue[" + itemCodeIssue + "]\t"
						+ "lotNoIssue[" + lotNoIssue + "]\tlotSlIssue[" + lotSlIssue + "] \t itemGroupCode["
						+ itemGrpCode + "]");

				if (itemGrpList != null && (itemGrpList.contains(itemGrpCode) == false)) {
					continue;
				}

				sql1 = "select serial_no from srl_container " + "where item_code = ? " + "AND lot_no = ? "
						+ "AND lot_sl = ?"
						// Added and Commented by sarita to check status for Confirmed Transactions on
						// 14MARCH2019 [START]
						// + "AND status = 'C'";
						+ "AND status = 'A'";
				// Added and Commented by sarita to check status for Confirmed Transactions on
				// 14MARCH2019 [END]
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCodeIssue);
				pstmt1.setString(2, lotNoIssue);
				pstmt1.setString(3, lotSlIssue);
				rs1 = pstmt1.executeQuery();

				if (rs1.next()) {
					srlContainerSrlNo = checkNull(rs1.getString("serial_no"));
					System.out.println("SRLCONTAINER SERIAL_NO --" + srlContainerSrlNo);

					sql2 = "select item_code ," + " lot_sl " + " from srl_contents "
							+ " where serial_no = ? order by line_no";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, srlContainerSrlNo);
					rs2 = pstmt2.executeQuery();
					while (rs2.next()) {
						srlContentsItemCode = checkNull(rs2.getString("item_code"));
						srlContentsLotSl = checkNull(rs2.getString("lot_sl"));

						finalString.append("<ITEM>");
						finalString.append("<ITEMCODE>").append(srlContentsItemCode).append("</ITEMCODE>\r\n");
						finalString.append("<SERIALNO>").append(srlContentsLotSl).append("</SERIALNO>\r\n");
						finalString.append("<CARTONCODE>").append("").append("</CARTONCODE>\r\n");
						finalString.append("</ITEM>");

						errorInformation.put("ITEMCODE", srlContentsItemCode);
						errorInformation.put("SERIALNO", srlContentsLotSl);
					}
					if (rs2 != null) {
						rs2.close();
						rs2 = null;
					}
					if (pstmt2 != null) {
						pstmt2.close();
						pstmt2 = null;
					}
				} else {
					errorInformation.put("ITEMCODE", itemCodeIssue);
					errorInformation.put("SERIALNO", lotSlIssue);

					finalString.append("<ITEM>");
					finalString.append("<ITEMCODE>").append(itemCodeIssue).append("</ITEMCODE>\r\n");
					finalString.append("<SERIALNO>").append(lotSlIssue).append("</SERIALNO>\r\n");
					finalString.append("<CARTONCODE>").append("").append("</CARTONCODE>\r\n");
					finalString.append("</ITEM>");
				}
				if (rs1 != null) {
					rs1.close();
					rs1 = null;
				}
				if (pstmt1 != null) {
					pstmt1.close();
					pstmt1 = null;
				}
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}

			finalString.append("</ITEMINFO>\r\n");
			finalString.append("</MTLREQUISITIONINFO>\r\n");
			finalString.append("</MATERIALREQUISITION>\r\n");
			finalString.append("</REQUESTINFO>]\r\n");
			System.out.println("Final String is ==== [" + finalString + "]");

			/*
			 * soapEndpointUrl = disCommon.getDisparams("999999", "DISTISSUE_PUSH_URL",
			 * conn); // http://192.168.100.29/NEWAPISVC/service.asmx soapAction =
			 * disCommon.getDisparams("999999", "DISTISSUE_PUSH_SOAPACTION", conn); //
			 * http://tempuri.org/MaterialRequisition
			 * 
			 * SOAPMessage responseData = callSoapWebService(soapEndpointUrl, soapAction,
			 * finalString.toString(), conn); // JSONObject jsonObject =
			 * retResponseValues(responseData); errorMsg = jsonObject.getString("MESSAGE");
			 * errorNo = jsonObject.getString("ERRORNO"); transactionNo =
			 * jsonObject.getString("TRANSACTIONNO");
			 */System.out.println("1.. [" + transactionNo + "] \t 2.. [" + errorNo + "] \t 3.. [" + errorMsg + "]");

			if ("FAILED".equalsIgnoreCase(errorMsg)) {
				errMsg = "\n Transaction No : " + transactionNo + " \n Error No : " + errorNo + "\n Status :"
						+ errorMsg;
				errString = getError(errMsg, errorNo, errorInformation, conn);
			} else {
				sql = "update distord_iss set sync_status = 'Y' where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				updCount = pstmt.executeUpdate();
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}

				System.out.println("updCount ..[" + updCount + "]");

				errMsg = "\n Transaction No : " + transactionNo + " \n Error No : " + errorNo + "\n Status :"
						+ errorMsg;
				errString = getError(errMsg, errorNo, errorInformation, conn);
			}
			System.out.println("errString is" + errString);
		} catch (Exception e) {
			System.out.println("Error in method confirm() " + e);
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				System.out.println("Exception--[" + e.getMessage() + "]");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input.trim();
	}

	// Method to get udfString for SiteCodeTo and SiteCodeFrom from distorder
	private String getGencodesData(String fldName, String fldVal, Connection conn) throws ITMException {
		String udfStr = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			sql = "select udf_str2 from gencodes where fld_name = ? and mod_name = 'W_DIST_ORDER' and fld_value = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, fldName);
			pstmt.setString(2, fldVal);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				udfStr = checkNull(rs.getString("udf_str2"));
				System.out.println("UDF String Against This Site is == [" + udfStr + "]");
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
			System.out.println("In [getGencodesData] method :: [" + e + "]");
			throw new ITMException(e);
		}
		return udfStr;
	}

	private String getShippedByUsingGencode(String transMode, Connection conn) throws ITMException {
		String shippedBy = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			sql = "select descr from gencodes where fld_name = 'TRANS_MODE' and mod_name = 'W_DIST_ISSUE' and fld_value = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, transMode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				shippedBy = checkNull(rs.getString("descr")).toUpperCase();
				System.out.println("Transaction Mode is == [" + shippedBy + "]");
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
			System.out.println("In [getGencodesData] method :: [" + e + "]");
			throw new ITMException(e);
		}
		return shippedBy;
	}

	// Method to create Soap Request
	/*
	 * private SOAPMessage createSOAPRequest(String soapAction,String finalString
	 * ,Connection conn) throws SOAPException, ITMException { String userId = "" ,
	 * password = "",externalPartyName="",uniqId = ""; SOAPMessage soapMessage =
	 * null; try { userId = checkNull(disCommon.getDisparams("999999",
	 * "DISTISSUE_PUSH_USERID", conn)); password =
	 * checkNull(disCommon.getDisparams("999999", "DISTISSUE_PUSH_PASSWORD", conn));
	 * externalPartyName = checkNull(disCommon.getDisparams("999999",
	 * "DISTISSUE_PUSH_EXTPARTYNAME", conn));
	 * System.out.println("userId ["+userId+"] \t password ["
	 * +password+"] \t externalPartyName ["+externalPartyName+"]");
	 * 
	 * 
	 * MessageFactory messageFactory = MessageFactory.newInstance(); soapMessage =
	 * messageFactory.createMessage();
	 * soapMessage.getSOAPHeader().setPrefix("soap");
	 * 
	 * 
	 * System.out.println("**** Inside createSoapEnvelope Method ****"); uniqId =
	 * generateRandomString();
	 * 
	 * SOAPPart soapPart = soapMessage.getSOAPPart(); String myNamespace = "xsi";
	 * String myNamespaceURI = "http://www.w3.org/2001/XMLSchema-instance";
	 * 
	 * String myNamespace1 = "xsd"; String myNamespaceURI1 =
	 * "http://www.w3.org/2001/XMLSchema";
	 * 
	 * SOAPEnvelope envelope = soapPart.getEnvelope(); envelope.setPrefix("soap");
	 * envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
	 * envelope.addNamespaceDeclaration(myNamespace1, myNamespaceURI1);
	 * 
	 * SOAPHeader header = envelope.getHeader(); SOAPElement security =
	 * header.addChildElement("MQUserNameToken", "", "http://tempuri.org/");
	 * SOAPElement userName = security.addChildElement("User_id", "");
	 * userName.addTextNode(userId); SOAPElement passwrd =
	 * security.addChildElement("Password", ""); passwrd.addTextNode(password);
	 * SOAPElement token = security.addChildElement("ExternalPartyName", "");
	 * token.addTextNode(externalPartyName);
	 * 
	 * SOAPBody soapBody = envelope.getBody(); soapBody.setPrefix("soap");
	 * SOAPElement security1 = soapBody.addChildElement("MaterialRequisition", "",
	 * "http://tempuri.org/"); SOAPElement xmlInfo =
	 * security1.addChildElement("MaterialRequisitionXML", "");
	 * xmlInfo.addTextNode(finalString); SOAPElement xmlInfo1 =
	 * security1.addChildElement("ReferenceNo", ""); xmlInfo1.addTextNode(uniqId);
	 * 
	 * MimeHeaders headers = soapMessage.getMimeHeaders();
	 * 
	 * headers.addHeader("Content-Type", "text/xml; charset=utf-8");
	 * headers.addHeader("SOAPAction", soapAction); soapMessage.saveChanges();
	 * 
	 * System.out.println("Request SOAP Message:"); soapMessage.writeTo(System.out);
	 * System.out.println("\n"); } catch(Exception e) {
	 * System.out.println("Error In createSoapEnvelope ::]" +e);
	 * e.printStackTrace(); throw new ITMException(e); } return soapMessage; }
	 */

	/*
	 * private SOAPMessage callSoapWebService(String soapEndpointUrl, String
	 * soapAction, String finalString, Connection conn) throws ITMException {
	 * SOAPMessage soapResponse = null; try {
	 * System.out.println("**** Inside callSoapWebService Method ****");
	 * 
	 * SOAPConnectionFactory soapConnectionFactory =
	 * SOAPConnectionFactory.newInstance(); SOAPConnection soapConnection =
	 * soapConnectionFactory.createConnection();
	 * 
	 * //SOAPMessage soapRequest = createSOAPRequest(soapAction, finalString, conn);
	 * soapResponse = soapConnection.call(soapRequest, soapEndpointUrl);
	 * soapConnection.close();
	 * 
	 * } catch (Exception e) { System.out.println("Error In createSoapEnvelope ::]"
	 * + e); e.printStackTrace(); throw new ITMException(e); } return soapResponse;
	 * }
	 */

	// Creted Random String for Reference Number
	private String generateRandomString() throws ITMException {
		String uniqueStr = "";
		try {
			Random random = new Random();
			long randomInt = random.nextInt(9999);
			uniqueStr = "ERP_" + randomInt;
			System.out.println("uniqueStr" + uniqueStr);
		} catch (Exception e) {
			System.out.println("Error In generateRandomString ::]" + e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return uniqueStr;
	}

	/*
	 * // Method to get Return Values[TransactionId , ErrorNo, Message] private
	 * JSONObject retResponseValues(SOAPMessage soapResponse) throws ITMException {
	 * String responseString = ""; String childNodeName = ""; NodeList
	 * parentNodeList = null; NodeList childNodeList = null; Node parentNode = null;
	 * Node childNode = null; int childNodeListLength = 0, ctr = 0; String
	 * transactionNo = "", errorNo = "", message = ""; JSONObject dataObj = new
	 * JSONObject(); final StringWriter stringWriter = new StringWriter(); try {
	 * TransformerFactory.newInstance().newTransformer().transform(new
	 * DOMSource(soapResponse.getSOAPPart()), new StreamResult(stringWriter));
	 * responseString = stringWriter.toString(); responseString =
	 * responseString.replace("&lt;", "<"); responseString =
	 * responseString.replace("&gt;", ">"); System.out.println("responseString is "
	 * + responseString);
	 * 
	 * Document dom = genericUtility.parseString(responseString); parentNodeList =
	 * dom.getElementsByTagName("STATUS"); System.out.println("***" +
	 * parentNodeList.getLength()); for (int rowCnt = 0; rowCnt <
	 * parentNodeList.getLength(); rowCnt++) { parentNode =
	 * parentNodeList.item(rowCnt); childNodeList = parentNode.getChildNodes();
	 * childNodeListLength = childNodeList.getLength();
	 * 
	 * for (ctr = 0; ctr < childNodeListLength; ctr++) { childNode =
	 * childNodeList.item(ctr); childNodeName = childNode.getNodeName(); if
	 * ("TRANSACTIONNO".equalsIgnoreCase(childNodeName)) { transactionNo =
	 * checkNull(genericUtility.getColumnValue("TRANSACTIONNO", dom)); } else if
	 * ("ERRORNO".equalsIgnoreCase(childNodeName)) { errorNo =
	 * checkNull(genericUtility.getColumnValue("ERRORNO", dom)); } else if
	 * ("MESSAGE".equalsIgnoreCase(childNodeName)) { message =
	 * checkNull(genericUtility.getColumnValue("MESSAGE", dom)); }
	 * dataObj.put("TRANSACTIONNO", transactionNo); dataObj.put("ERRORNO", errorNo);
	 * dataObj.put("MESSAGE", message); } } } catch (Exception e) {
	 * System.out.println("Error In retResponseValues ::]" + e);
	 * e.printStackTrace(); throw new ITMException(e); } return dataObj; }
	 */

	// getError method to show Error Message contains TransactionNo , ErrorNo And
	// Message
	private String getError(String errMsg, String Code, HashMap<String, String> erroData, Connection conn)
			throws ITMException, Exception {
		String mainStr = "";
		try {
			System.out.println("Error Data is ==== [" + erroData + "]");
			String errString = "";
			errString = new ITMDBAccessEJB().getErrorString("", Code, "", "", conn);
			System.out.println("Origional ErrorString is =====> <" + errString + ">");
			String begPart = errString.substring(0, errString.indexOf("</description>"));
			if (begPart.contains("%ISSUENUMBER%")) {
				begPart = begPart.replace("%ISSUENUMBER%", erroData.get("ISSUENUMBER"));
			} else if (begPart.contains("%MQNDATE%")) {
				begPart = begPart.replace("%MQNDATE%", erroData.get("MQNDATE"));
			} else if (begPart.contains("%MQNNUMBER%")) {
				begPart = begPart.replace("%MQNNUMBER%", erroData.get("MQNNUMBER"));
			} else if (begPart.contains("%REQUESTER%")) {
				begPart = begPart.replace("%REQUESTER%", erroData.get("REQUESTER"));
			} else if (begPart.contains("%REQUESTERLOCATION%")) {
				begPart = begPart.replace("%REQUESTERLOCATION%", erroData.get("REQUESTERLOCATION"));
			} else if (begPart.contains("%REQUESTTO%")) {
				begPart = begPart.replace("%REQUESTTO%", erroData.get("REQUESTTO"));
			} else if (begPart.contains("%ISSUELOCATION%")) {
				begPart = begPart.replace("%ISSUELOCATION%", erroData.get("ISSUELOCATION"));
			} else if (begPart.contains("%ISSUESUBLOCATION%")) {
				begPart = begPart.replace("%ISSUESUBLOCATION%", erroData.get("ISSUESUBLOCATION"));
			} else if (begPart.contains("%ISSUINGENTITY%")) {
				begPart = begPart.replace("%ISSUINGENTITY%", erroData.get("ISSUINGENTITY"));
			} else if (begPart.contains("%SHIPMENTDATE%")) {
				begPart = begPart.replace("%SHIPMENTDATE%", erroData.get("SHIPMENTDATE"));
			} else if (begPart.contains("%ITEMCODE%")) {
				begPart = begPart.replace("%ITEMCODE%", erroData.get("ITEMCODE"));
			} else if (begPart.contains("%SERIALNO%")) {
				begPart = begPart.replace("%SERIALNO%", erroData.get("SERIALNO"));
			}
			String endDesc = errString.substring(errString.indexOf("</description>"), errString.length());
			System.out.println("begPart [" + begPart + "] \t endDesc [" + endDesc + "]");
			mainStr = checkNull(begPart) + errMsg + checkNull(endDesc);
			System.out.println("mainStr:::::::::::::::::: " + mainStr);
		} catch (Exception e) {
			System.out.println("Error In getError ::]" + e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return mainStr;
	}
}
