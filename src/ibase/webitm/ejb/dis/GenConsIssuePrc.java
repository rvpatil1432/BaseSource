package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessLocal;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless // added for ejb3
public class GenConsIssuePrc extends ProcessEJB implements GenConsIssuePrcLocal, GenConsIssuePrcRemote {
	// GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	ValidatorEJB valejb = new ValidatorEJB();
	StringBuffer failMsg = null;
	FinCommon fCommon = new FinCommon();
	int countl, countitm, countlf, countgrp, NOL, countqty, countld, countYN, countitmm, countitm1, countheat,
			countitmval, COUNTSTK1;
	List<String> YNadd = new ArrayList<String>();
	List<String> nameliststs = new ArrayList<String>();

	String sql = "";

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException, ITMException {

		BaseLogger.log("3", null, null, "enter in process >>efrtttt>>>>>>>>><>ASASASSS>>>jkdfjkhgfhgjgh>>>>>>");
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;

		// GenericUtility genericUtility = GenericUtility.getInstance();
		BaseLogger.log("3", null, null, "below genericUtility--------------->>>>>>>>>");
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				headerDom = genericUtility.parseString(xmlString);
				BaseLogger.log("9", null, null, "xmlString d" + xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				detailDom = genericUtility.parseString(xmlString2);
				BaseLogger.log("9", null, null, "xmlString2 f" + xmlString2);
			}

			retStr = process(headerDom, detailDom, windowName, xtraParams);

		} catch (Exception e) {
			BaseLogger.log("0", null, null, 
					"Exception :StockAllocationPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):"
							+ e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
		}
		return retStr;

	}

	public String process() throws RemoteException, ITMException {
		return "";
	}

	public String process(Document dom, Document dom2, String windowName, String xtraParams)
			throws RemoteException, ITMException {
		BaseLogger.log("2", null, null, "enter in process(dom)");

		String chgTerm = "", chgUser = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		java.sql.Timestamp currDate = null;
		String currAppdate = "";
		String excpMsg = null;

		String sql1 = "";
		String retString = null;
		String errMsgStr = null;
		String consOrdFrom = null;
		String consOrdTo = null;
		String fromDateStr = null;
		String toDateStr = null;
		// GenericUtility genutility = null;
		SimpleDateFormat sdf = null;
		Timestamp fromDate = null;
		Timestamp toDate = null;
		ArrayList consOrderList = null;
		Connection conn = null;
		// String siteCode = "";
		String SiteCodeReq = "";
		String SiteCode = "", LocGrp = "";
		HashMap<String, ArrayList<DistOrderClubBean>> hm = new HashMap<String, ArrayList<DistOrderClubBean>>();
		StringBuffer xmlBuffer = new StringBuffer();
		StringBuffer xmlBuffer1 = new StringBuffer();
		StringBuffer xmlBufferD = new StringBuffer();
		String itemChgRetStr = "";
		String conorder = "";
		String sqldet = "";
		String allocateStr = "";
		String actionType = "";
		String xmlString = "";
		ArrayList<Integer> lineNo = new ArrayList();
		String lineNoStr = null;
		String errCode = "", errString = "", varValue = "";
		String available_yn = null;
		DistCommon dComm = new DistCommon();

		try {
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chg_termr");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chg_user");
			// genutility=GenericUtility.getInstance();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			currAppdate = sdf.format(currDate);

			ConnDriver connDriver = new ConnDriver();
			// conn = connDriver.getConnectDB( "DriverITM" );//Commented by Nandkumar
			// Gadkari on 03/07/18
			connDriver = null;
			conn = getConnection();// changes by Nandkumar Gadkari on 03/07/18
			conn.setAutoCommit(false);

			failMsg = new StringBuffer("");

			consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
			consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
			fromDateStr = genericUtility.getColumnValue("order_date__fr", dom);
			toDateStr = genericUtility.getColumnValue("order_date__to", dom);
			SiteCode = genericUtility.getColumnValue("site_code", dom);
			LocGrp = genericUtility.getColumnValue("loc_group", dom);
			fromDateStr = genericUtility.getValidDateString(fromDateStr, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			fromDate = java.sql.Timestamp.valueOf(fromDateStr + " 00:00:00.0");

			toDateStr = genericUtility.getValidDateString(toDateStr, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			toDate = java.sql.Timestamp.valueOf(toDateStr + " 00:00:00.0");

			// if(fromDate.compareTo(toDate) > 0)
			// BaseLogger.log("9", null, null, "Second Date is initialized before First Date");
			// else if(fromDate.compareTo(toDate) < 0)
			// BaseLogger.log("9", null, null, "Second Date is initialized after First Date");
			// else
			// BaseLogger.log("9", null, null, "First Date and Second Date are equal");

			int CNT = 0;
			sql = "";
			sql = "SELECT STATUS FROM CONSUME_ORD_DET WHERE CONS_ORDER BETWEEN ? AND ?";
			PreparedStatement pstmtl = conn.prepareStatement(sql);
			pstmtl.setString(1, consOrdFrom);
			pstmtl.setString(2, consOrdTo);

			ResultSet rsl = pstmtl.executeQuery();
			BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
			while (rsl.next()) {
				BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
				nameliststs.add(rsl.getString(1));
			}
			BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
			pstmtl.close();
			rsl.close();
			pstmtl = null;
			rsl = null;

			BaseLogger.log("9", null, null, ")))%%%%%%%%%%%%%%%%%" + nameliststs);
			sql = "";
			sql = "SELECT CONS_ORDER,SITE_CODE__ORD,DEPT_CODE,EMP_CODE,ITEM_SER,"
					+ "SITE_CODE__REQ,REF_NO,REF_DATE,TRAN_CODE,REMARKS,REAS_CODE,AMOUNT,NET_AMT,"
					+ "TAX_AMT,CURR_CODE,EXCH_RATE,NET_AMT__BC,CONFIRMED,CONF_DATE,"
					+ "EMP_CODE__APRV,AVAILABLE_YN,STATUS,LOC_CODE FROM CONSUME_ORD"
					+ " WHERE SITE_CODE__ORD = ? AND CONS_ORDER between ? AND ?" + "AND ORDER_DATE between ? AND ?";

			BaseLogger.log("9", null, null, sql + "1st sql");
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, SiteCode);
			pstmt.setString(2, consOrdFrom);
			pstmt.setString(3, consOrdTo);
			pstmt.setTimestamp(4, fromDate);
			pstmt.setTimestamp(5, toDate);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				countgrp = 0;
				countitm = 0;
				countl = 0;
				countlf = 0;
				NOL = 0;
				countld = 0;
				countYN = 0;
				countitmm = 0;
				countitm1 = 0;
				countheat = 0;
				countitmval = 0;
				COUNTSTK1 = 0;
				BaseLogger.log("9", null, null, "dhhd" + xmlString);
				conorder = rs.getString("cons_order");
				available_yn = rs.getString("available_yn");
				BaseLogger.log("9", null, null, conorder + "here");
				BaseLogger.log("9", null, null, "enter in 1st while");
				SiteCodeReq = rs.getString("SITE_CODE__REQ").trim();
				xmlBuffer.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuffer.append("<DocumentRoot>");
				xmlBuffer.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuffer.append("<group0>");
				xmlBuffer.append("<description>").append("Group0 description").append("</description>");
				xmlBuffer.append("<Header0>");
				xmlBuffer.append("<objName><![CDATA[").append("consumption_issue").append("]]></objName>");
				xmlBuffer.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuffer.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuffer.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuffer.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuffer.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuffer.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuffer.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuffer.append("<taxKeyValue><![CDATA[").append("	").append("]]></taxKeyValue>");
				xmlBuffer.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuffer.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuffer.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuffer.append("<description>").append("Header0 members").append("</description>");
				xmlBuffer1.append("<Detail1 objContext =\"1\"")
						.append(" objName=\"consumption_issue\" domID=\"1\" dbID=\"\">");
				xmlBuffer1.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuffer1.append("<cons_issue/>");
				xmlBuffer1.append("<issue_date>").append("<![CDATA[").append(currAppdate)
						.append("]]></issue_date>\r\n");
				xmlBuffer1.append("<eff_date>").append("<![CDATA[").append(currAppdate).append("]]></eff_date>\r\n");
				xmlBuffer1.append("<cons_order>").append("<![CDATA[").append(rs.getString("cons_order"))
						.append("]]></cons_order>\r\n");
				xmlBuffer1.append("<site_code__ord>").append("<![CDATA[").append(rs.getString("site_code__ord"))
						.append("]]></site_code__ord>\r\n");
				xmlBuffer1.append("<dept_code>").append("<![CDATA[").append(rs.getString("dept_code"))
						.append("]]></dept_code>\r\n");
				xmlBuffer1.append("<emp_code>").append("<![CDATA[").append(rs.getString("emp_code"))
						.append("]]></emp_code>\r\n");
				xmlBuffer1.append("<ITEM_SER>").append("<![CDATA[").append(rs.getString("ITEM_SER"))
						.append("]]></ITEM_SER>\r\n");
				xmlBuffer1.append("<SITE_CODE__REQ>").append("<![CDATA[").append(rs.getString("SITE_CODE__REQ"))
						.append("]]></SITE_CODE__REQ>\r\n");
				xmlBuffer1.append("<REF_NO>").append("<![CDATA[")
						.append((rs.getString("REF_NO") == null) ? "" : rs.getString("REF_NO"))
						.append("]]></REF_NO>\r\n");
				xmlBuffer1.append("<REF_DATE>").append("<![CDATA[")
						.append((rs.getDate("REF_DATE") == null) ? "" : rs.getString("REF_DATE"))
						.append("]]></REF_DATE>\r\n");
				xmlBuffer1.append("<TRAN_CODE>").append("<![CDATA[")
						.append((rs.getString("TRAN_CODE") == null) ? "" : rs.getString("TRAN_CODE"))
						.append("]]></TRAN_CODE>\r\n");
				xmlBuffer1.append("<REAS_CODE>").append("<![CDATA[").append(rs.getString("REAS_CODE"))
						.append("]]></REAS_CODE>\r\n");
				xmlBuffer1.append("<AMOUNT>").append("<![CDATA[").append(rs.getDouble("AMOUNT"))
						.append("]]></AMOUNT>\r\n");
				xmlBuffer1.append("<NET_AMT>").append("<![CDATA[").append(rs.getDouble("NET_AMT"))
						.append("]]></NET_AMT>\r\n");
				xmlBuffer1.append("<TAX_AMT>").append("<![CDATA[").append(rs.getDouble("TAX_AMT"))
						.append("]]></TAX_AMT>\r\n");
				xmlBuffer1.append("<CURR_CODE>").append("<![CDATA[").append(rs.getString("CURR_CODE"))
						.append("]]></CURR_CODE>\r\n");
				xmlBuffer1.append("<EXCH_RATE>").append("<![CDATA[").append(rs.getDouble("EXCH_RATE"))
						.append("]]></EXCH_RATE>\r\n");
				xmlBuffer1.append("<NET_AMT__BC>").append("<![CDATA[").append(rs.getDouble("NET_AMT__BC"))
						.append("]]></NET_AMT__BC>\r\n");
				xmlBuffer1.append("<available_yn>").append("<![CDATA[").append(rs.getString("available_yn"))
						.append("]]></available_yn>\r\n");
				xmlBuffer1.append("<REMARKS>").append("<![CDATA[")
						.append((rs.getString("REMARKS") == null) ? "" : rs.getString("REMARKS"))
						.append("]]></REMARKS>\r\n");
				xmlBuffer1.append("<chg_user>").append("<![CDATA[").append(chgUser).append("]]></chg_user>\r\n");
				xmlBuffer1.append("<chg_term>").append("<![CDATA[").append(chgTerm).append("]]></chg_term>\r\n");
				xmlBuffer1.append("<chg_date>").append("<![CDATA[").append(currAppdate.trim())
						.append("]]></chg_date>\r\n");
				xmlBuffer1.append("<EMP_CODE__APRV>").append("<![CDATA[").append(rs.getString("EMP_CODE__APRV"))
						.append("]]></EMP_CODE__APRV>\r\n");
				xmlBuffer1.append("<STATUS>").append("<![CDATA[").append(rs.getString("STATUS"))
						.append("]]></STATUS>\r\n");
				xmlBuffer1.append("<LOC_CODE>").append("<![CDATA[")
						.append((rs.getString("LOC_CODE") == null) ? "" : rs.getString("LOC_CODE"))
						.append("]]></LOC_CODE>\r\n");

				xmlBuffer1.append("</Detail1>\r\n");

				xmlBuffer.append(xmlBuffer1);

				if (xmlBuffer1.toString() != null && xmlBuffer1.toString().trim().length() != 0) {
					BaseLogger.log("9", null, null, "After ItemChange Of Detail [xmlBuffer] :" + xmlBuffer1.toString());
					dom2 = genericUtility.parseString(xmlBuffer1.toString());
					BaseLogger.log("9", null, null, "\n dom2 ************* " + dom2.toString());
				}

				BaseLogger.log("3", null, null, conorder + "here11");
				sqldet = "SELECT LINE_NO FROM CONSUME_ORD_DET WHERE CONS_ORDER = '" + conorder + "'";
				BaseLogger.log("9", null, null, sqldet);
				PreparedStatement pstmtm = conn.prepareStatement(sqldet);
				ResultSet rsd = pstmtm.executeQuery();
				BaseLogger.log("9", null, null, "under isd");
				while (rsd.next()) {
					lineNo.add(new Integer(rsd.getInt("LINE_NO")));
				}
				rsd.close();rsd = null;
				pstmtm.close(); pstmtm = null;
				BaseLogger.log("9", null, null, "The size of ArrayList :" + lineNo.size());
				// itemChgXmlString.append(xmlBuffer.toString());
				NOL = lineNo.size();
				for (int i = 0; i < lineNo.size(); i++) {

					BaseLogger.log("9", null, null, "line size" + i);
					BaseLogger.log("9", null, null, "getItemChangedDetail2 now call");
					lineNoStr = lineNo.get(i).toString();

					itemChgRetStr = itemChgRetStr + getItemChangedDetail2(dom, dom2, SiteCodeReq, available_yn,
							xtraParams, actionType, conorder, lineNoStr, conn);

					if (itemChgRetStr.indexOf("DETERROR") > -1) {
						continue;
					}

					BaseLogger.log("9", null, null, "itemChgRetStrVCVCVVCV---->>[" + itemChgRetStr + "]");
					BaseLogger.log("9", null, null, "in continue >>>>VVVVV[" + itemChgRetStr + "]");

				}

				if (lineNo.size() == 0) {

					BaseLogger.log("9", null, null, "new logic for det");
					consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
					consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
					BaseLogger.log("9", null, null, ">NNNNNhkJKKK::JKK" + consOrdFrom + "" + consOrdTo + "");
					CNT = 0;
					sql = "";
					sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
					PreparedStatement pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, consOrdFrom);
					pstmt1.setString(2, consOrdTo);

					ResultSet rs11 = pstmt1.executeQuery();
					BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
					if (rs11.next()) {
						BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
						CNT = rs11.getInt(1);
					}
					BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
					pstmt1.close();
					rs11.close();
					pstmt1 = null;
					rs11 = null;

					if (CNT == 1) {
						// return "ERRORMSG";
						errString = itmDBAccessEJB.getErrorString("", "CONSCOMBVR", "", "", conn);
						BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
						return errString;
					} else {
						xmlBuffer1 = new StringBuffer();
						xmlBuffer = new StringBuffer();
						xmlString = "";
						continue;
					}

				}

				xmlBuffer.append(itemChgRetStr);

				// xmlBuffer.append(itemChgXmlString.toString());

				// xmlBuffer.append("</Header0>");
				// xmlBuffer.append("</group0>");
				// xmlBuffer.append("</DocumentRoot>");
				lineNo = new ArrayList<Integer>(0);
				// lineNo = new ArrayList<Integer>(0);
				BaseLogger.log("3", null, null, "out of for loop line size" + lineNo.size());
				xmlBuffer.append("</Header0>");
				xmlBuffer.append("</group0>");
				xmlBuffer.append("</DocumentRoot>");
				BaseLogger.log("3", null, null, "allocated check here " + allocateStr + "allocated check here");

				BaseLogger.log("3", null, null, "^^^^^^After document root endffff^^^^^^^^");

				// xmlString=itemChgXmlString.toString();
				//BaseLogger.log("9", null, null, "xml beff here" + xmlBuffer + "xml beff here");
				//BaseLogger.log("9", null, null, "xml ceff here" + xmlBuffer.toString() + "xml ceff here");
				//BaseLogger.log("9", null, null, "xml string here" + xmlString + "xml string here");
				BaseLogger.log("3", null, null, SiteCodeReq + "code codefff");
				xmlString = xmlBuffer.toString();

				if (xmlString.indexOf("POCONSTRA") > -1) {
					BaseLogger.log("9", null, null, "IN 1 TIME SGDSGHDHF");
					ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
					errCode = "POCONSTRAG";
					errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
					BaseLogger.log("9", null, null, "errString :>>kkk" + errString);
					return errString;
				}

				if (xmlString.indexOf("LOCERROR") > -1) {
					BaseLogger.log("9", null, null, "error through");
					ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
					errCode = "LOCERRSTR";
					errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
					BaseLogger.log("9", null, null, "errString :>>kkk" + errString);
					return errString;
				}

				if (xmlString.indexOf("ERRORMSG") > -1) {
					BaseLogger.log("9", null, null, "IN 1 TIME SGDSGHDHF");
					ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
					errCode = "ERRORSTR";
					errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
					BaseLogger.log("9", null, null, "errString :>>kkk" + errString);
					return errString;
				}

				if (xmlString.indexOf("Errors") > -1) {
					xmlBuffer1 = new StringBuffer();
					xmlBuffer = new StringBuffer();

					itemChgRetStr = "";
					xmlString = "";
					continue;
				}
				BaseLogger.log("3", null, null, "saveData(SiteCodeReq, xmlString, conn, xtraParams)"+xmlString+"]");
				xmlString = xmlString.replaceAll("DETERROR", "");
				BaseLogger.log("3", null, null, "out continue >>>>FFFFFF[" + xmlString + "]");
				retString = saveData(SiteCodeReq, xmlString, xtraParams, conn);
				BaseLogger.log("3", null, null, "@@@@@@3: retString from despatch" + retString);
				conn.commit();
				BaseLogger.log("3", null, null, "retstring check here" + retString);
				if (retString.indexOf("Success") > -1) {
					BaseLogger.log("3", null, null, "@@@@@@3: retString from despatch" + retString);
					varValue = dComm.getDisparams("999999", "AUTO_CONF_DESP", conn);
					BaseLogger.log("9", null, null, "varValue --" + varValue);
					if ("Y".equalsIgnoreCase(varValue.trim())) {
						String[] arrayForTranId = retString.split("<TranID>");
						int endIndex = arrayForTranId[1].indexOf("</TranID>");
						String tranIdForDesp = arrayForTranId[1].substring(0, endIndex);
						BaseLogger.log("9", null, null, "-tranIdForDesp-" + tranIdForDesp);
						retString = confirmConsOrder("consumption_issue", tranIdForDesp, xtraParams, conn);
						BaseLogger.log("9", null, null, "retString from conf from despatch ::" + retString);
					}
					BaseLogger.log("9", null, null, "record successfully saved");
				}

				xmlBuffer1 = new StringBuffer("");
				xmlBufferD = new StringBuffer("");
				xmlBuffer = new StringBuffer("");
				xmlString = "";
				itemChgRetStr = "";
				BaseLogger.log("9", null, null, "xmlbuffer1" + xmlBuffer1 + "xmlBufferD" + xmlBufferD + "xmlBuffer" + xmlBuffer
						+ "xmlString" + xmlString + "end here");

			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		} catch (ITMException itme) {
			BaseLogger.log("0", null, null, "Process :: returning in itme ::  " + retString);
			
			retString = "ERROR";
			excpMsg = itme.getMessage();

			itme.printStackTrace();
		} catch (Exception e) {

			
			retString = "ERROR";
			excpMsg = e.getMessage();

			e.printStackTrace();
		} finally {
			try {

				if (consOrderList != null) {

					consOrderList = null;
				}
				BaseLogger.log("3", null, null, "finally called retString.. :: " + retString);
				if (retString == null || retString.trim().length() == 0
						|| retString.toLowerCase().indexOf("success") > -1) {
					conn.commit();

					BaseLogger.log("3", null, null, "LAST POP UP");
					// retString = itmDBAccessEJB.getErrorString("","CONSCOMBVR","BASE");
					retString = itmDBAccessEJB.getErrorString("", "PROCSUCC", "");// Change by chandrashekar on
																					// 27-Apr-2015

					/*
					 * if( failMsg != null && failMsg.toString().trim().length() > 0 ) { String
					 * begPart = retString.substring( 0, retString.indexOf("<trace>") + 7 ); String
					 * endPart = retString.substring( retString.indexOf("</trace>")); String mainStr
					 * = begPart +
					 * "Following Dist Orders can not be processded due to \n unavailability of stock \n or unconfirmed Distribution Issue already exist :\n"
					 * + failMsg.toString() + endPart; retString = mainStr; begPart =null; endPart
					 * =null; mainStr =null; }
					 */
				} else {
					BaseLogger.log("3", null, null, "finally called rollback.. :: " + retString);
					conn.rollback();

					retString = itmDBAccessEJB.getErrorString("", "VTDESNCONF", "BASE");

				}
				if (rs != null) {
					rs.close();
				}
				rs = null;
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {

				excpMsg = e.getMessage();
				e.printStackTrace();
			}
		}
		BaseLogger.log("3", null, null, "last...returning in itme ::  " + retString);
		return (retString);

	}

	public String confirmConsOrder(String businessObj, String tranIdFr, String xtraParams, Connection conn)
			throws ITMException {

		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		BaseLogger.log("3", null, null, 
				"confirmSaleOrder(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");

		try {
			// ConnDriver connDriver = new ConnDriver();
			// conn = connDriver.getConnectDB("DriverITM");

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;
			sql = "";
			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, businessObj);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
			BaseLogger.log("3", null, null, "serviceCode = " + serviceCode + " compName " + compName);
			sql = "";
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, serviceCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serviceURI = rs.getString("SERVICE_URI");
			}
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
			BaseLogger.log("3", null, null, "serviceURI = " + serviceURI + " compName = " + compName);
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING,
					ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"),
					XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			// aobj[3] = new String(forcedFlag);
			// BaseLogger.log("9", null, null, "@@@@@@@@@@loginEmpCode:"
			// +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			BaseLogger.log("3", null, null, "@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String) call.invoke(aobj);
			BaseLogger.log("3", null, null, "Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>[" + retString + "]");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {

				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				/*
				 * if( conn != null ){ conn.close(); conn = null; }
				 */
			} catch (Exception e) {
				BaseLogger.log("0", null, null, "Exception inCalling confirmed");
				e.printStackTrace();
				try {
					conn.rollback();

				} catch (Exception s) {
					BaseLogger.log("0", null, null, "Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}
		}
		return retString;

	}
	private String saveData(String siteCode, String xmlString, String xtraParams, Connection conn) throws ITMException
	{
		BaseLogger.log("3", null, null, "calling saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		ibase.utility.UserInfoBean userInfo = new UserInfoBean();
		String chgUser = "", chgTerm = "";
		String loginCode = "", loginEmpCode = "", loginSiteCode = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			BaseLogger.log("3", null, null, "xtraParams>>>>" + xtraParams);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userInfo.setEmpCode(loginEmpCode);
			userInfo.setRemoteHost(chgTerm);
			userInfo.setSiteCode(loginSiteCode);
			userInfo.setLoginCode(loginCode);
			userInfo.setEntityCode(loginEmpCode);
			String[] authencate = new String[2];
			authencate[0] = loginCode;
			authencate[1] = "";
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
			BaseLogger.log("3", null, null, "SaveData: retString ::"+ retString);
		} catch (ITMException e)
		{
			BaseLogger.log("0", null, null, "SaveData:masterStateful:: "+retString+" ITMException::"+ e.getMessage());
			e.printStackTrace();			 
			throw new ITMException(e); 
		} catch (Exception e)
		{
			BaseLogger.log("0", null, null, "SaveData:: Exception ::"+ e.getMessage());
			e.printStackTrace();			
			throw new ITMException(e);
		}
		return retString;
	}
	

	/*private String saveData(String siteCode, String xmlString, Connection conn) throws ITMException {
		BaseLogger.log("0", null, null, siteCode + "enter in save data in sitecode" + siteCode);
		BaseLogger.log("0", null, null, "saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null;
		try {
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");

			String[] authencate = new String[2];
			authencate[0] = "";
			authencate[1] = "";
			BaseLogger.log("9", null, null, "xmlString :::: " + xmlString);

			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
			masterStateful = null;
		} catch (ITMException itme) {
			BaseLogger.log("0", null, null, "ITMException :SaveData :saveData :==>");
			throw itme;
		} catch (Exception e) {
			BaseLogger.log("0", null, null, "Exception :SaveData :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}

	private String saveData(String siteCode, String xmlString, Connection conn, String xtraParams) throws ITMException {
		BaseLogger.log("9", null, null, siteCode + "enter in save data xtra params in sitecode" + siteCode);
		BaseLogger.log("9", null, null, "saving data...........");
		InitialContext ctx = null;
		String retString = null;
		String userInfo = "";
		MasterStatefulLocal masterStateful = null;
		try {
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			//masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			String[] authencate = new String[2];
			authencate[0] = loginCode;
			authencate[1] = "";
			BaseLogger.log("9", null, null, "xmlString :::: " + xmlString);

			ValidatorEJB validatorEJB = new ValidatorEJB();
			String userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			// siteCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			DBAccessLocal dbAccess = (DBAccessLocal) ctx.lookup("ibase/DBAccessEJB/local");
			// Changes and Commented By Ajay on 29/01/2018:START
			// String[] userInfoArray = dbAccess.getEmpInfo(userId, siteCode);
			UserInfoBean userInfoBean = dbAccess.createUserInfo(userId);

			userInfo = userInfoBean.toString();

			BaseLogger.log("0", null, null, "^^^^^^^ UserInfo ..> " + userInfo);
			// UserInfoBean userInfoBean = new UserInfoBean();
			
			 * userInfoBean.setLoginCode(userId); userInfoBean.setEmpCode(userInfoArray[0]);
			 * userInfoBean.setEmpName(userInfoArray[1]);
			 * userInfoBean.setReportTo(userInfoArray[2]);
			 * userInfoBean.setDeptCode(userInfoArray[3]);
			 * userInfoBean.setDeptDescr(userInfoArray[4]);
			 * userInfoBean.setSiteCode(siteCode);
			 * userInfoBean.setSiteDescr(userInfoArray[6]);
			 * userInfoBean.setUserLevel(userInfoArray[7]);
			 * userInfoBean.setUserType(userInfoArray[8]);
			 * userInfoBean.setEntityCode(userInfoArray[9]);
			 * userInfoBean.setProfileId(userInfoArray[10]);
			 * userInfoBean.setItemSer(userInfoArray[11]);
			 * userInfoBean.setEmailIdOff(userInfoArray[12]);
			 * userInfoBean.setLoggerType(userInfoArray[13]);
			 * userInfoBean.setStanCode(userInfoArray[14]);
			 * userInfoBean.setDesignation(userInfoArray[15]);
			 * userInfoBean.setDateJoin(userInfoArray[16]);
			 * userInfoBean.setDivision(userInfoArray[17]);
			 * userInfoBean.setUserLanguage(userInfoArray[18]);
			 * userInfoBean.setUserCountry(userInfoArray[19]);
			 
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			// retString = masterStateful.processRequest(userInfoBean, xmlString, true,
			// conn);
			//retString = masterStateful.processRequest(userInfo, xmlString.toString(), true, conn);
			//retString = masterStateful.processRequest(userInfo, xmlString.toString(), true, conn);
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
			// Changes and Commented By Ajay On 29/01/2018:END
			BaseLogger.log("0", null, null, "Returns String for:"+retString);
			BaseLogger.log("0", null, null, "masterstatefull,dbaccess and ctx will be null here["+retString+"]");
			// retString = masterStateful.processRequest(authencate, siteCode, true,
			// xmlString, true, conn);
			masterStateful = null;
			// dbAccess = null;
			// ctx = null;

		} catch (ITMException itme) {
			BaseLogger.log("0", null, null, "ITMException ::GenConsIssue:: SaveData :saveData :==>"+itme.getMessage());
			throw itme;
		} catch (Exception e) {
			BaseLogger.log("0", null, null, "Exception ::GenConsIssue:: SaveData :saveData :==>"+e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}*/

	private String getItemChangedDetail2(Document dom1, Document dom2, String siteCode, String available_yn,
			String xtraParams, String actionType, String consumeOrder, String lineNoStr, Connection conn)
			throws Exception {

		String allocateStr1 = "";
		BaseLogger.log("2", null, null, "enter in getItemChangedDetail2");

		PreparedStatement pstmt;

		ResultSet rs = null;
		String tranType = "", sql = "";
		String itemCode = "", unit = "", taxChap = "", taxClass = "", taxEnv = "";
		String acctCode = "", cctrCode = "", locCode = "";
		String itemDescr = "", qcReqd = "", acctCodeInv = "", cctrCodeInv = "";
		String consIssue = "", tranIDIssue = "";
		double qtyIssue = 0, quantity = 0, qtyStd = 0, qtyStr = 0, rate = 0;

		StringBuffer xmlBuffer = new StringBuffer("");
		// GenericUtility genericUtility = GenericUtility.getInstance();
		try {
			tranType = genericUtility.getColumnValue("tran_type", dom1);
			consIssue = genericUtility.getColumnValue("cons_issue", dom1);
			tranIDIssue = genericUtility.getColumnValue("tran_id__iss", dom1);

			BaseLogger.log("3", null, null, "3 fields" + tranType + "" + consIssue + "" + tranIDIssue);
			if (consIssue == null) {
				consIssue = "@@";
			}
			sql = "";
			sql = "select item_code,unit,quantity,rate,tax_chap,tax_class,tax_env,acct_code,cctr_code,loc_code from consume_ord_det "
					+ "where cons_order = ? and line_no = ?";
			BaseLogger.log("9", null, null, "SQL ::" + sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, consumeOrder);
			pstmt.setString(2, lineNoStr);

			rs = pstmt.executeQuery();
			if (rs.next()) {

				itemCode = rs.getString("item_code");
				unit = rs.getString("unit");
				qtyStr = rs.getDouble("quantity");
				rate = rs.getDouble("rate");
				taxChap = rs.getString("tax_chap") == null ? "" : rs.getString("tax_chap");
				taxClass = rs.getString("tax_class") == null ? "" : rs.getString("tax_class");
				taxEnv = rs.getString("tax_env") == null ? "" : rs.getString("tax_env");
				acctCode = rs.getString("acct_code") == null ? "" : rs.getString("acct_code");
				cctrCode = rs.getString("cctr_code") == null ? "" : rs.getString("cctr_code");
				locCode = rs.getString("loc_code") == null ? "" : rs.getString("loc_code");
			}
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
			BaseLogger.log("9", null, null, "itemCode ::" + itemCode);
			BaseLogger.log("9", null, null, "unit     ::" + unit);
			BaseLogger.log("9", null, null, "qtyStr	 ::" + qtyStr);
			BaseLogger.log("9", null, null, "rate	 ::" + rate);
			BaseLogger.log("9", null, null, "taxChap  ::" + taxChap);
			BaseLogger.log("9", null, null, "taxClass ::" + taxClass);
			BaseLogger.log("9", null, null, "taxEnv   ::" + taxEnv);
			BaseLogger.log("9", null, null, "acctCode ::" + acctCode);
			BaseLogger.log("9", null, null, "cctrCode ::" + cctrCode);
			BaseLogger.log("9", null, null, "locCode  ::" + locCode);
			// (itemCode,unit,qtyStr,rate,taxChap,taxClass,taxEnv,acctCode,cctrCode,locCode)
			sql = "";
			sql = "select sum(case when b.tran_type = 'I' then a.quantity else (-1 * a.quantity) end) from consume_iss_det a, consume_iss b "
					+ " where a.cons_issue = b.cons_issue " + " and b.cons_issue <> ?" + " and a.cons_order = ?"
					+ " and a.line_no__ord = ?";
			BaseLogger.log("9", null, null, "SQL ::" + sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, consIssue);
			pstmt.setString(2, consumeOrder);
			pstmt.setString(3, lineNoStr);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				qtyIssue = rs.getDouble(1);
			}
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
			quantity = qtyStr - qtyIssue;
			sql = "";
			sql = "select descr,qc_reqd from item where item_code = ?";
			BaseLogger.log("9", null, null, "SQL ::" + sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				itemDescr = rs.getString("descr");
				qcReqd = rs.getString("qc_reqd");
			}
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
			if (tranType != null && tranType == "R") {
				if (tranIDIssue != null && tranIDIssue.trim().length() > 0) {
					sql = "";
					sql = "SELECT SUM(CASE WHEN B.TRAN_TYPE = 'I' THEN A.QUANTITY ELSE (-1 * A.QUANTITY) END)"
							+ " FROM CONSUME_ISS_DET A, CONSUME_ISS B " + " WHERE A.CONS_ISSUE = B.CONS_ISSUE "
							+ " AND B.CONS_ISSUE <> ?" + " AND (B.CONS_ISSUE = ? OR B.TRAN_ID__ISS = ?)"
							+ " AND A.CONS_ORDER =  ?" + " AND A.LINE_NO__ORD = ?";
					BaseLogger.log("9", null, null, "SQL ::" + sql);

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, consIssue);
					pstmt.setString(2, tranIDIssue);
					pstmt.setString(3, tranIDIssue);
					pstmt.setString(4, consumeOrder);
					pstmt.setString(5, lineNoStr);
					rs = pstmt.executeQuery();

					if (rs.next()) {
						qtyIssue = rs.getDouble(1);
					}
					rs.close();rs = null;
					pstmt.close(); pstmt = null;
					quantity = qtyIssue;
				}
				sql = "";
				sql = "select a.acct_code,a.cctr_code,a.acct_code__inv,a.cctr_code__inv	, a.rate "
						+ "from consume_iss_det a,consume_iss b where  a.cons_issue = b.cons_issue "
						+ "and b.tran_type = 'I' and a.cons_order = ? and " + "a.line_no__ord = ?";
				BaseLogger.log("9", null, null, "SQL ::" + sql);

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, consumeOrder);
				pstmt.setString(2, lineNoStr);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					acctCode = rs.getString(1);
					cctrCode = rs.getString(2);
					acctCodeInv = rs.getString(3);
					cctrCodeInv = rs.getString(4);
					rate = rs.getDouble(5);
				}
				rs.close();rs = null;
				pstmt.close(); pstmt = null;
			}
			// BaseLogger.log("9", null, null, "isSrvCallOnChg is set ---");
			//
			// xmlBuffer.append("<Detail2 objContext =\"2\"").append("
			// objName=\"consumption_issue\" domID=\"1\" dbID=\"\">");
			// xmlBuffer.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\"
			// pkNames=\"\"/>");
			// xmlBuffer.append("<cons_issue/>");
			//
			//
			// xmlBuffer.append("<cons_order
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((consumeOrder == null) ? "
			// ":consumeOrder).append("]]>").append("</cons_order>\r\n");
			// xmlBuffer.append("<line_no__ord
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lineNoStr).append("]]>").append("</line_no__ord>\r\n");
			// xmlBuffer.append("<item_code
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
			// xmlBuffer.append("<item_descr
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
			// xmlBuffer.append("<unit
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
			// xmlBuffer.append("<unit__std
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(unit).append("]]>").append("</unit__std>\r\n");
			// xmlBuffer.append("<conv_qty_stduom
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append("1").append("]]>").append("</conv_qty_stduom>\r\n");
			// xmlBuffer.append("<quantity
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
			// xmlBuffer.append("<quantity__std
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(quantity).append("]]>").append("</quantity__std>\r\n");
			// xmlBuffer.append("<rate
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
			// xmlBuffer.append("<tax_chap
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxChap == null) ? "
			// ":taxChap).append("]]>").append("</tax_chap>\r\n");
			// xmlBuffer.append("<tax_class
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxClass == null) ? "
			// ":taxClass).append("]]>").append("</tax_class>\r\n");
			// xmlBuffer.append("<tax_env
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxEnv == null) ? "
			// ":taxEnv).append("]]>").append("</tax_env>\r\n");
			// xmlBuffer.append("<acct_code
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((acctCode == null) ? "
			// ":acctCode).append("]]>").append("</acct_code>\r\n");
			// xmlBuffer.append("<cctr_code
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((cctrCode == null) ? "
			// ":cctrCode).append("]]>").append("</cctr_code>\r\n");
			// xmlBuffer.append("<loc_code
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((locCode == null) ? "
			// ":locCode).append("]]>").append("</loc_code>\r\n");
			// xmlBuffer.append("<qc_reqd
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(qcReqd).append("]]>").append("</qc_reqd>\r\n");
			// xmlBuffer.append("<acct_code__inv
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((acctCodeInv == null) ? "
			// ":acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
			// xmlBuffer.append("<cctr_code__inv
			// isSrvCallOnChg=\"0\">").append("<![CDATA[").append((cctrCodeInv == null) ? "
			// ":cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
			// xmlBuffer.append("</Detail2>");

			BaseLogger.log("9", null, null, "before insert in action allocate" + quantity);
			allocateStr1 = actionAllocate(dom1, dom2, consumeOrder, lineNoStr, siteCode, available_yn, xtraParams,
					actionType, itemCode, unit, qtyStr, rate, quantity, taxChap, taxClass, taxEnv, acctCode, cctrCode,
					locCode, conn);

		} catch (Exception e) {
			BaseLogger.log("0", null, null, "Exception [ConsumeIssueAct][getItemChanged] :" + e);
			e.printStackTrace();
			throw e;
		}
		BaseLogger.log("0", null, null, "return string from actionAllocate allocateStr1 ------>>[" + allocateStr1 + "]");
		if (allocateStr1.equals("ERROR")) {
			return allocateStr1;
		} else {
			xmlBuffer.append(allocateStr1);
			return xmlBuffer.toString();
		}

	}

	private String actionAllocate(Document dom, Document dom2, String consOrder, String lineOrd, String siteCodeReq,
			String availableYn, String xtraParams, String actionType, String itemCode, String unit, double qtyStr,
			double rate, double quantity, String taxChap, String taxClass, String taxEnv, String acctCode,
			String cctrCode, String locCode, Connection conn) throws RemoteException, ITMException {

		String tranType = "", consIss = "";
		String itemDescr = "", lotNo = "", lotSl = "";

		String allocDate = "", partQuantity = "", errCode = "", errString = "", sql = "", retResult = "";
		String acctCodeInv = "", cctrCodeInv = "", sql1 = "";
		double remainingQty = 0d, inputQty = 0d;
		int minShelfLife = 0;
		String trackShelfLife = "", chkDate = "";
		ArrayList acctCodeInvArrLst = new ArrayList();
		java.util.Date chkDate1 = null, expDate1 = null;
		;
		java.sql.Date expDate = null;
		double stockQuantity = 0d;
		String availableinv = "", usableloc = "", invst = "", usableinv = "";
		String lotNum = "", locCode1 = "";
		double hmQty = 0d;
		NodeList detailList = null;
		Node currDetail = null;
		int detailListLength = 0;
		HashMap hm = new HashMap();
		String LocGrp = "";
		Statement stmt = null, stmt1 = null;
		// Connection conn = null;
		ResultSet rs = null, rs1 = null;
		String statType = "";
		// ConnDriver connDriver = new ConnDriver();

		StringBuffer valueXmlString = new StringBuffer("");
		boolean stkExpFlag = false;
		LocGrp = genericUtility.getColumnValue("loc_group", dom);
		BaseLogger.log("9", null, null, "loccccc" + LocGrp);
		if (dom == null) {
			BaseLogger.log("9", null, null, "openclose root here");
			valueXmlString.append("<Root/>\r\n");
			BaseLogger.log("9", null, null, "openclose root here");
			return valueXmlString.toString();
		}

		// GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();

		try {
			// conn = connDriver.getConnectDB("DriverITM");
			BaseLogger.log("3", null, null, "[Validating .....]");

			AppConnectParm appConnect = new AppConnectParm();
			java.util.Properties p = appConnect.getProperty();
			InitialContext ctx = new InitialContext(p);

			detailList = dom.getElementsByTagName("Detail2");
			detailListLength = detailList.getLength();
			BaseLogger.log("3", null, null, "detailListLength : " + detailListLength);
			BaseLogger.log("3", null, null, "actionType : " + actionType);
			// if (actionType.equalsIgnoreCase("Allocate"))
			// {
			// detailListLength = 1;
			// }
			stmt = conn.createStatement();
			// currDetail = detailList.item(ctr);

			String varValue = "", stkExpLoc = "";

			varValue = itmDBAccess.getEnvDis("999999", "NEAREXP_LOC", conn);
			if (varValue != null && varValue.trim().length() > 0 && !(varValue.equalsIgnoreCase("NULLFOUND"))) {
				stkExpLoc = varValue;
			}

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			// tranType = genericUtility.getColumnValue("tran_type", dom2);
			// consIss = genericUtility.getColumnValue("cons_issue", dom2);
			// consOrd = genericUtility.getColumnValueFromNode("cons_order", currDetail);
			// lineOrd = genericUtility.getColumnValueFromNode("line_no__ord", currDetail);
			// //siteCodeReq = genericUtility.getColumnValueFromNode("site_code__req",
			// currDetail);
			//
			// BaseLogger.log("9", null, null, "siteCodeReq--------"+siteCodeReq);
			// itemCode = genericUtility.getColumnValueFromNode("item_code", currDetail);
			// itemDescr = genericUtility.getColumnValueFromNode("item_descr", currDetail);
			// locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
			// lotNo = genericUtility.getColumnValueFromNode("lot_no", currDetail);
			// lotSl = genericUtility.getColumnValueFromNode("lot_sl", currDetail);
			// quantity = genericUtility.getColumnValueFromNode("quantity", currDetail);
			// acctCode = genericUtility.getColumnValueFromNode("acct_code", currDetail);
			// cctrCode = genericUtility.getColumnValueFromNode("cctr_code", currDetail);
			// taxChap = genericUtility.getColumnValueFromNode("tax_chap", currDetail);
			// taxClass = genericUtility.getColumnValueFromNode("tax_class", currDetail);
			// taxEnv = genericUtility.getColumnValueFromNode("tax_env", currDetail);
			// availableYn = genericUtility.getColumnValueFromNode("available_yn",
			// currDetail);

			String sqlpt = "select issue_date,part_qty from consume_iss" + " where cons_order = ?";

			PreparedStatement pstmtt = conn.prepareStatement(sqlpt);
			pstmtt.setString(1, consOrder);

			ResultSet rst = pstmtt.executeQuery();
			if (rst.next()) {
				allocDate = rst.getString("issue_date") == null ? "" : rst.getString("issue_date");
				partQuantity = rst.getString("part_qty") == null ? "" : rst.getString("part_qty");

			}

			pstmtt.close();
			pstmtt = null;
			sqlpt = null;
			rst.close();
			rst = null;

			BaseLogger.log("3", null, null, "locCode :" + locCode);
			if (partQuantity == null || partQuantity.trim().length() == 0) {
				partQuantity = " ";
			}

			// if (locCode == null || "null".equalsIgnoreCase(locCode) ||
			// locCode.trim().length() == 0)
			// {
			// BaseLogger.log("9", null, null, "If locCode = null || length() == 0");
			// locCode = "%";
			// BaseLogger.log("9", null, null, "If locCodelocCode is null then [%] :" +locCode);
			// }

			// added

			// generate found code

			if (locCode != null && locCode.trim().length() > 0) {

				sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

				pstmtt = conn.prepareStatement(sqlpt);
				pstmtt.setString(1, locCode);

				rst = pstmtt.executeQuery();
				if (rst.next()) {
					invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");

				}

				pstmtt.close();
				pstmtt = null;
				sqlpt = null;
				rst.close();
				rst = null;

				BaseLogger.log("9", null, null, "CHECK OUT");
				sqlpt = "select available,stat_type from invstat where inv_stat = ?";

				pstmtt = conn.prepareStatement(sqlpt);
				pstmtt.setString(1, invst);

				rst = pstmtt.executeQuery();
				if (rst.next()) {
					availableinv = rst.getString("available") == null ? "" : rst.getString("available");
					statType = rst.getString("stat_type");
					YNadd.add(availableinv);
				}

				pstmtt.close();
				pstmtt = null;
				sqlpt = null;
				rst.close();
				rst = null;

				sqlpt = "select usable from location where loc_code = ?";

				pstmtt = conn.prepareStatement(sqlpt);
				pstmtt.setString(1, locCode);

				rst = pstmtt.executeQuery();
				if (rst.next()) {
					usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
					YNadd.add(usableloc);
				}

				pstmtt.close();
				pstmtt = null;
				sqlpt = null;
				rst.close();
				rst = null;

				if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")) {

				} else {

					countitmm++;

					BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + countitm + ">>>>>>>><<<<" + NOL);
					if (NOL == countitmm) {

						BaseLogger.log("9", null, null, "NOL gets match");

						BaseLogger.log("9", null, null, "enter in false condition))");
						BaseLogger.log("9", null, null, "dsdddd");
						String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
						String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
						BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
						BaseLogger.log("9", null, null, "enter in elsssee");
						int CNT = 0;
						sql = "";
						sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
						PreparedStatement pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, consOrdFrom);
						pstmt1.setString(2, consOrdTo);

						ResultSet rs11 = pstmt1.executeQuery();
						BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
						if (rs11.next()) {
							BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
							CNT = rs11.getInt(1);
						}
						BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
						pstmt1.close();
						rs11.close();
						pstmt1 = null;
						rs11 = null;

						//if (CNT == 1) {
						if (CNT == 0) {
							return "ERRORMSG";
						} else {
							CNT = 0;
							sql = "";
							sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
									+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? " + " AND B.AVAILABLE = ? "
									+ " AND A.LOC_CODE = ? " + " AND B.STAT_TYPE<>'S' " + // Condition added by Manoj
																							// dtd 03/06/2016 to exclude
																							// intransit location
									" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))  > 0";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, siteCodeReq);
							pstmt1.setString(3, availableYn);
							pstmt1.setString(4, locCode);

							rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								CNT = rs11.getInt(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;
							if (CNT == 0) {

								errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
								BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
								return errString;
							}
						}

					}

					//return "DETERROR";

				}

			}

			// END HERE

			int cnt = 0;
			sql = "";
			sql = "select count(1) from consume_ord where cons_order = ? ";
			PreparedStatement pstmtl = conn.prepareStatement(sql);
			pstmtl.setString(1, consOrder);
			ResultSet rsl = pstmtl.executeQuery();
			if (rsl.next()) {
				cnt = rsl.getInt(1);
			}
			pstmtl.close();
			rsl.close();
			pstmtl = null;
			rsl = null;
			if (cnt > 0) {

				int quantord = 0;
				sql = "";
				sql = "select quantity from consume_ord_det where cons_order = ? and line_no = ?";
				pstmtl = conn.prepareStatement(sql);
				pstmtl.setString(1, consOrder);
				pstmtl.setString(2, lineOrd);
				rsl = pstmtl.executeQuery();
				if (rsl.next()) {
					quantord = rsl.getInt(1);
				}
				pstmtl.close();
				rsl.close();
				pstmtl = null;
				rsl = null;

				int quantiss = 0;
				sql = "";
				sql = "SELECT SUM(QUANTITY) FROM CONSUME_ISS_DET WHERE CONS_ORDER = ? AND LINE_NO__ORD = ?";
				PreparedStatement pstmtlQ = conn.prepareStatement(sql);
				pstmtlQ.setString(1, consOrder);
				pstmtlQ.setString(2, lineOrd);
				ResultSet rslQ = pstmtlQ.executeQuery();
				if (rslQ.next()) {

					quantiss = rslQ.getInt(1);
				}

				pstmtlQ.close();
				rslQ.close();
				pstmtlQ = null;
				rslQ = null;

				if (quantord <= quantiss) {
					List<String> nameList = new ArrayList<String>();
					BaseLogger.log("9", null, null, "check----------->>>>>>1");
					BaseLogger.log("9", null, null, "enter for check fine");
					BaseLogger.log("9", null, null, "dsdddd");
					String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
					String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
					BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
					BaseLogger.log("9", null, null, "enter in elsssee");
					int CNT = 0;
					sql = "";
					sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
					pstmtl = conn.prepareStatement(sql);
					pstmtl.setString(1, consOrdFrom);
					pstmtl.setString(2, consOrdTo);

					rsl = pstmtl.executeQuery();
					BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
					if (rsl.next()) {
						BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
						CNT = rsl.getInt(1);
					}
					BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
					pstmtl.close();
					rsl.close();
					pstmtl = null;
					rsl = null;

					if (CNT == 1) {
						BaseLogger.log("9", null, null, "check----------->>>>>>2");
						CNT = 0;
						sql = "";
						sql = "SELECT COUNT(*) FROM CONSUME_ORD_DET WHERE CONS_ORDER = ?";
						pstmtl = conn.prepareStatement(sql);
						pstmtl.setString(1, consOrder);

						rsl = pstmtl.executeQuery();
						BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
						if (rsl.next()) {
							BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
							CNT = rsl.getInt(1);
						}
						BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
						pstmtl.close();
						rsl.close();
						pstmtl = null;
						rsl = null;

						if (CNT == 1) {
							BaseLogger.log("9", null, null, "check----------->>>>>>3");
							String status = "";
							sql = "";
							sql = "select status from consume_ord_det where cons_order = ? and line_no = ?";
							PreparedStatement pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, consOrder);
							pstmt1.setString(2, lineOrd);
							ResultSet rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								status = rs11.getString(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;
							if (status.equalsIgnoreCase("P")) {
								sql = "";
								BaseLogger.log("9", null, null, "check----------->>>>>>18");
								sql = "select item_code,loc_code from consume_ord_det "
										+ "where cons_order = ? and status = ? order by line_no";
								BaseLogger.log("9", null, null, "SQL ::xx" + sql);

								PreparedStatement pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, consOrder);
								pstmt.setString(2, "P");
								rs = pstmt.executeQuery();
								if (rs.next()) {
									BaseLogger.log("9", null, null, "check----------->>>>>>20");
									itemCode = rs.getString("item_code");
									locCode = rs.getString("loc_code") == null ? "" : rs.getString("loc_code");
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (locCode == null || locCode.trim().length() == 0) {
									String locCoden = "";
									BaseLogger.log("9", null, null, ">>>>>>>>>>>...........1");
									sql = "";
									sql = "SELECT LOC_CODE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND (QUANTITY - (ALLOC_QTY + (CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END))) > 0 ORDER BY EXP_DATE";
									BaseLogger.log("9", null, null, "SQL ::xx" + sql);

									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setString(2, siteCodeReq);
									rs = pstmt.executeQuery();
									while (rs.next()) {
										locCoden = rs.getString(1) == null ? "" : rs.getString(1);

										sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select available,stat_type from invstat where inv_stat = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, invst);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											availableinv = rst.getString("available") == null ? ""
													: rst.getString("available");
											statType = rst.getString("stat_type");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select usable from location where loc_code = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;
										BaseLogger.log("9", null, null, "avalable>>>>>" + availableinv + "->>>>>" + usableloc);

										if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")
												&& !"S".equalsIgnoreCase(statType)) {
											if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCoden)) // changes
																														// dadaso
																														// deepak
											{
												countheat++;
												BaseLogger.log("9", null, null, "aaaaaaa____________1" + countheat + "---" + NOL);

												if (NOL == countheat) {
													BaseLogger.log("9", null, null, "check----------->>>>>>21");
													BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
													return "ERRORMSG";
												}
											}
										} else {
											continue;
										}

									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

								} else {
									if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCode)) // changes
																												// dadaso
																												// deepak
									{

										countheat++;
										BaseLogger.log("9", null, null, "aaaaaaa____________2" + countheat + "---" + NOL);
										if (NOL == countheat) {
											BaseLogger.log("9", null, null, "check----------->>>>>>21");
											BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
											return "ERRORMSG";
										}
									}
								}

							}
							if (!nameliststs.contains("P")) {
								BaseLogger.log("9", null, null, "check----------->>>>>>11");
								BaseLogger.log("9", null, null, "called here ++++++ 2");
								return "POCONSTRA";
							}
						} else {
							BaseLogger.log("9", null, null, "check----------->>>>>>6");
							sql = "";
							sql = "select status from consume_ord_det where cons_order = ?";
							pstmtl = conn.prepareStatement(sql);
							pstmtl.setString(1, consOrder);
							rsl = pstmtl.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							while (rsl.next()) {
								BaseLogger.log("9", null, null, "check----------->>>>>>7");
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								String status = rsl.getString(1) == null ? "" : rsl.getString(1).trim();
								nameList.add(status);
							}
							pstmtl.close();
							rsl.close();
							pstmtl = null;
							rsl = null;
							if (nameList.contains("P")) {
								sql = "";
								BaseLogger.log("9", null, null, "check----------->>>>>>8");
								sql = "select item_code,loc_code from consume_ord_det "
										+ "where cons_order = ? and status = ? order by line_no";
								BaseLogger.log("9", null, null, "SQL ::xx" + sql);

								PreparedStatement pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, consOrder);
								pstmt.setString(2, "P");
								rs = pstmt.executeQuery();
								if (rs.next()) {
									BaseLogger.log("9", null, null, "check----------->>>>>>9");
									itemCode = rs.getString("item_code");
									locCode = rs.getString("loc_code") == null ? "" : rs.getString("loc_code");
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (locCode == null || locCode.trim().length() == 0) {
									String locCoden = "";
									BaseLogger.log("9", null, null, ">>>>>>>>>>>...........1");
									sql = "";
									sql = "SELECT LOC_CODE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND (QUANTITY - (ALLOC_QTY + (CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END))) > 0 ORDER BY EXP_DATE";
									BaseLogger.log("9", null, null, "SQL ::xx" + sql);

									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setString(2, siteCodeReq);
									rs = pstmt.executeQuery();
									while (rs.next()) {
										locCoden = rs.getString(1) == null ? "" : rs.getString(1);

										sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select available,stat_type from invstat where inv_stat = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, invst);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											availableinv = rst.getString("available") == null ? ""
													: rst.getString("available");
											statType = rst.getString("stat_type");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select usable from location where loc_code = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;
										BaseLogger.log("9", null, null, "avalable>>>>>" + availableinv + "->>>>>" + usableloc);

										if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")
												&& !"S".equalsIgnoreCase(statType)) {
											if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCoden)) // changes
																														// dadaso
																														// deepak
											{
												countheat++;
												BaseLogger.log("9", null, null, "aaaaaaa____________3" + countheat + "---" + NOL);
												if (NOL == countheat) {
													BaseLogger.log("9", null, null, "check----------->>>>>>21");
													BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
													return "ERRORMSG";
												}
											}
										} else {
											continue;
										}

									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

								} else {
									if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCode)) // changes
																												// dadaso
																												// deepak
									{
										countheat++;
										BaseLogger.log("9", null, null, "aaaaaaa____________4" + countheat + "---" + NOL);
										if (NOL == countheat) {
											BaseLogger.log("9", null, null, "check----------->>>>>>10");
											BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
											return "ERRORMSG";
										}
									}
								}

							}
							if (!nameliststs.contains("P")) {
								BaseLogger.log("9", null, null, "check----------->>>>>>11");
								BaseLogger.log("9", null, null, "called here ++++++ 2");
								return "POCONSTRA";
							}
						}

					} else // main else

					{

						BaseLogger.log("9", null, null, "check----------->>>>>>2");
						CNT = 0;
						sql = "";
						sql = "SELECT COUNT(*) FROM CONSUME_ORD_DET WHERE CONS_ORDER = ?";
						pstmtl = conn.prepareStatement(sql);
						pstmtl.setString(1, consOrder);

						rsl = pstmtl.executeQuery();
						BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
						if (rsl.next()) {
							BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
							CNT = rsl.getInt(1);
						}
						BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
						pstmtl.close();
						rsl.close();
						pstmtl = null;
						rsl = null;

						if (CNT == 1) {
							BaseLogger.log("9", null, null, "check----------->>>>>>3");
							String status = "";
							sql = "";
							sql = "select status from consume_ord_det where cons_order = ? and line_no = ?";
							PreparedStatement pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, consOrder);
							pstmt1.setString(2, lineOrd);
							ResultSet rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								status = rs11.getString(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;
							if (status.equalsIgnoreCase("P")) {

								BaseLogger.log("9", null, null, "check----------->>>>>>18");
								sql = "";
								sql = "select item_code,loc_code from consume_ord_det "
										+ "where cons_order = ? and status = ? order by line_no";
								BaseLogger.log("9", null, null, "SQL ::xx" + sql);

								PreparedStatement pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, consOrder);
								pstmt.setString(2, "P");
								rs = pstmt.executeQuery();
								if (rs.next()) {
									BaseLogger.log("9", null, null, "check----------->>>>>>20");
									itemCode = rs.getString("item_code");
									locCode = rs.getString("loc_code") == null ? "" : rs.getString("loc_code");
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (locCode == null || locCode.trim().length() == 0) {
									String locCoden = "";
									BaseLogger.log("9", null, null, ">>>>>>>>>>>...........1");
									sql = "";
									sql = "SELECT LOC_CODE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND (QUANTITY - (ALLOC_QTY + (CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END))) > 0 ORDER BY EXP_DATE";
									BaseLogger.log("9", null, null, "SQL ::xx" + sql);

									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setString(2, siteCodeReq);
									rs = pstmt.executeQuery();
									while (rs.next()) {
										locCoden = rs.getString(1) == null ? "" : rs.getString(1);

										sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select available,stat_type from invstat where inv_stat = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, invst);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											availableinv = rst.getString("available") == null ? ""
													: rst.getString("available");
											statType = rst.getString("stat_type");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select usable from location where loc_code = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;
										BaseLogger.log("9", null, null, "avalable>>>>>" + availableinv + "->>>>>" + usableloc);

										if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")
												&& !"S".equalsIgnoreCase(statType)) {
											if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCoden)) // changes
																														// dadaso
																														// deepak
											{
												countheat++;
												BaseLogger.log("9", null, null, "aaaaaaa____________5" + countheat + "---" + NOL);
												if (NOL == countheat) {
													BaseLogger.log("9", null, null, "check----------->>>>>>21");
													BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
													return "ERRORMSG";
												}
											}
										} else {
											continue;
										}

									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

								} else {
									if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCode)) // changes
																												// dadaso
																												// deepak
									{
										countheat++;
										BaseLogger.log("9", null, null, "aaaaaaa____________6" + countheat + "---" + NOL);
										if (NOL == countheat) {
											BaseLogger.log("9", null, null, "check----------->>>>>>21");
											BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
											return "ERRORMSG";
										}
									}
								}

							}
							if (!nameliststs.contains("P")) {
								BaseLogger.log("9", null, null, "check----------->>>>>>11");
								BaseLogger.log("9", null, null, "called here ++++++ 2");
								return "POCONSTRA";
							}
						} else {
							BaseLogger.log("9", null, null, "check----------->>>>>>6");
							sql = "";
							sql = "select status from consume_ord_det where cons_order = ?";
							pstmtl = conn.prepareStatement(sql);
							pstmtl.setString(1, consOrder);
							rsl = pstmtl.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							while (rsl.next()) {
								BaseLogger.log("9", null, null, "check----------->>>>>>7");
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								String status = rsl.getString(1) == null ? "" : rsl.getString(1).trim();
								nameList.add(status);
							}
							pstmtl.close();
							rsl.close();
							pstmtl = null;
							rsl = null;
							if (nameList.contains("P")) {
								BaseLogger.log("9", null, null, "check----------->>>>>>8");
								sql = "";
								sql = "select item_code,loc_code from consume_ord_det "
										+ "where cons_order = ? and status = ? order by line_no";
								BaseLogger.log("9", null, null, "SQL ::xx" + sql);

								PreparedStatement pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, consOrder);
								pstmt.setString(2, "P");
								rs = pstmt.executeQuery();
								if (rs.next()) {
									BaseLogger.log("9", null, null, "check----------->>>>>>9");
									itemCode = rs.getString("item_code");
									locCode = rs.getString("loc_code") == null ? "" : rs.getString("loc_code");
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (locCode == null || locCode.trim().length() == 0) {
									String locCoden = "";
									BaseLogger.log("9", null, null, ">>>>>>>>>>>...........1");
									sql = "";
									sql = "SELECT LOC_CODE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND (QUANTITY - (ALLOC_QTY + (CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END))) > 0 ORDER BY EXP_DATE";
									BaseLogger.log("9", null, null, "SQL ::xx" + sql);

									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setString(2, siteCodeReq);
									rs = pstmt.executeQuery();
									while (rs.next()) {
										locCoden = rs.getString(1) == null ? "" : rs.getString(1);

										sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select available,stat_type from invstat where inv_stat = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, invst);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											availableinv = rst.getString("available") == null ? ""
													: rst.getString("available");
											statType = rst.getString("stat_type");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;

										sqlpt = "select usable from location where loc_code = ?";

										pstmtt = conn.prepareStatement(sqlpt);
										pstmtt.setString(1, locCoden);

										rst = pstmtt.executeQuery();
										if (rst.next()) {
											usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
										}

										pstmtt.close();
										pstmtt = null;
										sqlpt = null;
										rst.close();
										rst = null;
										BaseLogger.log("9", null, null, "avalable>>>>>" + availableinv + "->>>>>" + usableloc);

										if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")
												&& !"S".equalsIgnoreCase(statType)) {
											if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCoden)) // changes
																														// dadaso
																														// deepak
											{
												countheat++;
												BaseLogger.log("9", null, null, "aaaaaaa____________7" + countheat + "---" + NOL);
												if (NOL == countheat) {
													BaseLogger.log("9", null, null, "check----------->>>>>>21");
													BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
													return "ERRORMSG";
												}
											}
										} else {
											continue;
										}

									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

								}

								else {
									if (!isStockAvailable(conn, itemCode, siteCodeReq, availableYn, locCode)) // changes
																												// dadaso
																												// deepak
									{
										countheat++;
										BaseLogger.log("9", null, null, "aaaaaaa____________8" + countheat + "---" + NOL);
										if (NOL == countheat) {
											BaseLogger.log("9", null, null, "check----------->>>>>>10");
											BaseLogger.log("9", null, null, "called here ++++++bbbbbb 1");
											return "ERRORMSG";
										}
									}
								}

							}
							if (!nameliststs.contains("P")) {
								BaseLogger.log("9", null, null, "check----------->>>>>>11");
								BaseLogger.log("9", null, null, "called here ++++++ 2");
								return "POCONSTRA";
							}
						}

					}

				}

			}

			sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

			pstmtt = conn.prepareStatement(sqlpt);
			pstmtt.setString(1, locCode);

			rst = pstmtt.executeQuery();
			if (rst.next()) {
				invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");
			}

			pstmtt.close();
			pstmtt = null;
			sqlpt = null;
			rst.close();
			rst = null;

			sqlpt = "select available,stat_type from invstat where inv_stat = ?";

			pstmtt = conn.prepareStatement(sqlpt);
			pstmtt.setString(1, invst);

			rst = pstmtt.executeQuery();
			if (rst.next()) {
				availableinv = rst.getString("available") == null ? "" : rst.getString("available");
				statType = rst.getString("stat_type");
			}

			pstmtt.close();
			pstmtt = null;
			sqlpt = null;
			rst.close();
			rst = null;

			sqlpt = "select usable from location where loc_code = ?";

			pstmtt = conn.prepareStatement(sqlpt);
			pstmtt.setString(1, locCode);

			rst = pstmtt.executeQuery();
			if (rst.next()) {
				usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
			}

			pstmtt.close();
			pstmtt = null;
			sqlpt = null;
			rst.close();
			rst = null;
			BaseLogger.log("9", null, null, "avalable>>>>>" + availableinv + "->>>>>" + usableloc);
			// IF STARTED
			if ((LocGrp != null && LocGrp.trim().length() > 0) && (locCode != null && locCode.trim().length() > 0)) {
				BaseLogger.log("9", null, null, "ENTER WHEN LOCGRP AND LOCCODE NOT NULL");
				int vLocCode = 0;

				sql = "";
				sql = "SELECT COUNT(*) FROM STOCK WHERE LOC_CODE = ? AND ITEM_CODE = ?";
				PreparedStatement pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, locCode);
				pstmt1.setString(2, itemCode);
				ResultSet rs11 = pstmt1.executeQuery();

				if (rs11.next()) {

					vLocCode = rs11.getInt(1);
					BaseLogger.log("9", null, null, ">>cc>v>v>v>v" + vLocCode + "jdhudf" + locCode);

					if (vLocCode == 0) {

						countitm++;

						BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + countitm + ">>>>>>>><<<<" + NOL);
						if (NOL == countitm) {

							BaseLogger.log("9", null, null, "NOL gets match");

							BaseLogger.log("9", null, null, "enter in false condition))");
							BaseLogger.log("9", null, null, "dsdddd");
							String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
							String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
							BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
							BaseLogger.log("9", null, null, "enter in elsssee");
							int CNT = 0;
							sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, consOrdFrom);
							pstmt1.setString(2, consOrdTo);

							rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								CNT = rs11.getInt(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;

							if (CNT == 1) {
								return "ERRORMSG";
							} else {
								CNT = 0;
								sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
										+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? " + " AND B.AVAILABLE = ? "
										+ " AND A.LOC_CODE = ? " + " AND B.STAT_TYPE<>'S' " + // Condition added by
																								// Manoj dtd 03/06/2016
																								// to exclude intransit
																								// location
										" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) > 0";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, itemCode);
								pstmt1.setString(2, siteCodeReq);
								pstmt1.setString(3, availableYn);
								pstmt1.setString(4, locCode);

								rs11 = pstmt1.executeQuery();
								BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
								if (rs11.next()) {
									BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
									CNT = rs11.getInt(1);
								}
								BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
								pstmt1.close();
								rs11.close();
								pstmt1 = null;
								rs11 = null;
								if (CNT == 0) {

									errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
									BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
									return errString;
								}
							}

						} else {
							return "DETERROR";
						}

					}

				}

				pstmt1.close();
				rs11.close();
				pstmt1 = null;
				rs11 = null;

				if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")) {

					BaseLogger.log("9", null, null, "ENTER WHEN AVAILABLE AND USABLE ARE PRESENT");
					BaseLogger.log("9", null, null, "enter in stock");
					// added

					if (locCode != null && locCode.trim().length() > 0) {
						BaseLogger.log("9", null, null, "ENTER FOR CHECK LOC GROUP MATCHING WITH DOM ENTRY");
						String lcgrp = "";
						sql = "";
						sql = "select loc_group from location where loc_code = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, locCode);
						rs11 = pstmt1.executeQuery();

						while (rs11.next()) {
							lcgrp = rs11.getString(1) == null ? "" : rs11.getString(1);
							lcgrp = lcgrp.trim();
							LocGrp = LocGrp.trim();
							BaseLogger.log("9", null, null, "loc grp value <<<<<<<>>>" + lcgrp + "<<<<" + LocGrp + "???????");
							if (!LocGrp.equalsIgnoreCase(lcgrp)) {

								countgrp++;

								BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + countgrp + ">>>>>>>><<<<" + NOL);
								if (NOL == countgrp) {

									BaseLogger.log("9", null, null, "NOL gets match");

									BaseLogger.log("9", null, null, "enter in false condition))");
									BaseLogger.log("9", null, null, "dsdddd");
									String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
									String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
									BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
									BaseLogger.log("9", null, null, "enter in elsssee");
									int CNT = 0;
									sql = "";
									sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, consOrdFrom);
									pstmt1.setString(2, consOrdTo);

									rs11 = pstmt1.executeQuery();
									BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
									if (rs11.next()) {
										BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
										CNT = rs11.getInt(1);
									}
									BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
									pstmt1.close();
									rs11.close();
									pstmt1 = null;
									rs11 = null;

									if (CNT == 1) {
										return "LOCERROR";
									} else {

										errString = itmDBAccess.getErrorString("", "LOCERRSTR", "", "", conn);
										BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
										return errString;

									}

								}

								return "DETERROR";

							}

						}

						pstmt1.close();
						rs11.close();
						pstmt1 = null;
						rs11 = null;
					}

					if (lotNo == null || lotNo.trim().length() == 0)

					{
						BaseLogger.log("9", null, null, "ENTER LOTNO IS BLANK");
						sql = "";
						sql = "SELECT (CASE WHEN SUM((A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))) IS NULL THEN 0 ELSE SUM((A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))) END) "
								+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
								+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
								+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND B.AVAILABLE = '" + availableYn
								+ "' " + " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd 03/06/2016 to exclude
																	// intransit location
								+ "AND A.QUANTITY  > 0 " + "AND A.LOC_CODE  LIKE '" + locCode + "' "
								+ "AND L.LOC_GROUP = '" + LocGrp + "' ";
						BaseLogger.log("9", null, null, "locCode in query ===" + locCode);

					}

					else {
						sql = "";
						BaseLogger.log("9", null, null, "ENTER LOTNO IS NOT BLANK");
						sql = "SELECT (CASE WHEN SUM((A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))) IS NULL THEN 0 ELSE SUM((A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))) END) "
								+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
								+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
								+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND A.LOC_CODE  = '" + locCode + "' "
								+ "AND A.LOT_NO    = '" + lotNo + "' " + "AND A.LOT_SL	 = '" + lotSl + "' "
								+ " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd 03/06/2016 to exclude
															// intransit location
								+ "AND (A.QUANTITY - A.ALLOC_QTY) >=" + quantity + "'" + " AND B.AVAILABLE = '"
								+ availableYn + "'" + "AND L.LOC_GROUP = '" + LocGrp + "' ";
					} //
					BaseLogger.log("9", null, null, "sql :" + sql);
					rs = stmt.executeQuery(sql);
					if (rs.next()) {
						stockQuantity = rs.getDouble(1);
						BaseLogger.log("9", null, null, "stockQuantity :" + stockQuantity);

					}

					if (stockQuantity == 0 && "X".equals(partQuantity)) {
						errCode = "VTNOSTK";
						errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
						BaseLogger.log("9", null, null, "errString :" + errString);
						return errString;
					} else if (stockQuantity == 0) {
						BaseLogger.log("9", null, null, "zerooo q");
					}

					stmt.close();

					if (lotNo == null || lotNo.trim().length() == 0) {
						BaseLogger.log("9", null, null, "ENTER LOTNO IS 2BLANK");
						sql = "";
						sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
								+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
								+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
								+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
								+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
								+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND B.AVAILABLE = '" + availableYn
								+ "' " + " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd 03/06/2016 to exclude
																	// intransit location
								+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) > 0 "
								+ "AND A.LOC_CODE  LIKE '" + locCode + "' " + "AND L.LOC_GROUP = '" + LocGrp + "' "
								+ "ORDER BY A.EXP_DATE";

						BaseLogger.log("9", null, null, "check here SQLLLL");
					}

					else {
						sql = "";
						BaseLogger.log("9", null, null, "ENTER LOTNO IS NOT 2BLANK");
						sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
								+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
								+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
								+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
								+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
								+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND A.LOC_CODE  = '" + locCode + "' "
								+ "AND A.LOT_NO    = '" + lotNo + "' " + "AND A.LOT_SL	 = '" + lotSl + "' "
								+ " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd 03/06/2016 to exclude
															// intransit location
								+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) >="
								+ quantity + " AND B.AVAILABLE = '" + availableYn + "'" + "AND L.LOC_GROUP = '" + LocGrp
								+ "' ";
					} //
					BaseLogger.log("9", null, null, "sql :" + sql);

					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					while (rs.next()) {
						lotNum = rs.getString(1);
						locCode1 = rs.getString(12);
						BaseLogger.log("9", null, null, "lotNum :" + lotNum);
						BaseLogger.log("9", null, null, "locCode1 :" + locCode1);
						if (!hm.containsKey(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum)) {
							hm.put(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum,
									new Double(rs.getDouble(3)));
						}
					}

					BaseLogger.log("9", null, null, "Hashmap :" + hm);
					remainingQty = quantity;
					BaseLogger.log("9", null, null, "remainingQty :" + remainingQty);

					rs.close();
					rs = null;
					stmt.close();
					stmt = null;

					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);

					int count = 0;

					while (rs.next()) {
						count++;
						BaseLogger.log("9", null, null, "ENTER IN FIRST MAIN WHILE CONDITION FOR CHECKING RS");

						lotNum = rs.getString(1);
						locCode1 = rs.getString(12);
						System.out.print(
								"Combination Key :  " + itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum);
						hmQty = Double.parseDouble(
								(hm.get(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum)).toString());
						BaseLogger.log("9", null, null, " ::Value [hmQty] :: " + hmQty);
						if (hmQty == 0) {
							continue;
						}
						if (availableYn.equals("Y")) {
							sql1 = "SELECT MIN_SHELF_LIFE, (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) "
									+ "FROM ITEM WHERE ITEM_CODE = '" + itemCode + "' ";
							BaseLogger.log("9", null, null, "sql1 :" + sql1);
							stmt1 = conn.createStatement();
							rs1 = stmt1.executeQuery(sql1);
							if (rs1.next()) {
								minShelfLife = rs1.getInt(1);
								trackShelfLife = rs1.getString(2);
							}
							stmt1.close();
							stmt1 = null;
							stkExpFlag = false;
							String token = "";
							StringTokenizer stToken = new StringTokenizer(stkExpLoc, ",");
							while (stToken.hasMoreTokens()) {
								token = stToken.nextToken();
								if (locCode.equalsIgnoreCase(token)) {
									stkExpFlag = true;
									break;
								}
							}
							if (stkExpFlag == false) {
								if (minShelfLife == 0) {
									minShelfLife = 1;
								}
								// if (trackShelfLife.equals("Y"))
								// {
								// chkDate = calcExpiry(allocDate,minShelfLife);
								// BaseLogger.log("9", null, null, "chkDate :"+chkDate);
								// chkDate1 = sdf.parse(chkDate);
								// BaseLogger.log("9", null, null, "chkDate1 :"+chkDate1);
								// expDate = rs.getDate(4);
								// BaseLogger.log("9", null, null, "expDate :"+expDate);
								// if(expDate != null)
								// {
								// expDate1 = new java.util.Date(expDate.getTime());
								// BaseLogger.log("9", null, null, "expDate1 :"+expDate1);
								// if (chkDate1.compareTo(expDate1) > 0)
								// {
								// continue;
								// }
								// }
								// }
							}
						}
						BaseLogger.log("9", null, null, "remainingQty before if :" + remainingQty);
						BaseLogger.log("9", null, null, "rs.getDouble(3) :" + rs.getDouble(3));

						if (remainingQty == 0) {
							break;
						}

						else if (hmQty >= remainingQty) {
							inputQty = remainingQty;
							BaseLogger.log("9", null, null, "inputQty :" + inputQty);
							remainingQty = 0;
							hm.put(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum,
									new Double(hmQty - inputQty));

							BaseLogger.log("9", null, null, "hmQty - remainingQty :" + (hmQty - inputQty));
							BaseLogger.log("9", null, null, "hm if [hmQty >= remainingQty] :" + hm);
						}

						else if (hmQty < remainingQty) {

							inputQty = hmQty;
							BaseLogger.log("9", null, null, "inputQty :" + inputQty);

							remainingQty = remainingQty - inputQty;
							BaseLogger.log("9", null, null, "remainingQty :" + remainingQty);
							hm.put(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum, new Double(0));
						}
						BaseLogger.log("9", null, null, "Hashmap :" + hm);

						BaseLogger.log("9", null, null, lineOrd);
						valueXmlString.append("<Detail2 objContext =\"2\"")
								.append(" objName=\"consumption_issue\" domID=\"1\" dbID=\"\">");
						valueXmlString.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
						valueXmlString.append("<cons_issue/>");
						// valueXmlString.append("<line_no
						// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(line).append("]]>").append("</line_no>\r\n");
						BaseLogger.log("9", null, null, "rs.getDouble(15) :" + rs.getDouble(15));
						if (rs.getDouble(15) > 0) {
							valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(15)).append("]]>")
									.append("</rate>\r\n");
						} else {
							sql1 = "SELECT RATE	FROM CONSUME_ORD_DET " + "WHERE CONS_ORDER = '" + consOrder + "' "
									+ "AND LINE_NO = " + lineOrd + " ";
							BaseLogger.log("9", null, null, "sql1 :" + sql1);
							stmt1 = conn.createStatement();
							rs1 = stmt1.executeQuery(sql1);
							if (rs1.next()) {
								valueXmlString.append("<rate>").append("<![CDATA[").append(rs1.getDouble(1))
										.append("]]>").append("</rate>\r\n");
							}
							stmt1.close();
							stmt1 = null;
						}

						valueXmlString.append("<cons_order isSrvCallOnChg=\"0\">").append("<![CDATA[").append(consOrder)
								.append("]]>").append("</cons_order>\r\n");
						valueXmlString.append("<line_no__ord isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lineOrd)
								.append("]]>").append("</line_no__ord>\r\n");
						valueXmlString.append("<item_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemCode)
								.append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<item_descr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemDescr)
								.append("]]>").append("</item_descr>\r\n");
						valueXmlString.append("<quantity isSrvCallOnChg=\"0\">").append("<![CDATA[").append(inputQty)
								.append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity__std isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(inputQty).append("]]>").append("</quantity__std>\r\n");
						valueXmlString.append("<unit isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(5))
								.append("]]>").append("</unit>\r\n");
						valueXmlString.append("<unit__std isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(rs.getString(5)).append("]]>").append("</unit__std>\r\n");
						valueXmlString.append("<loc_code isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(rs.getString(12)).append("]]>").append("</loc_code>\r\n");

						valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(rs.getString(1))
								.append("]]>").append("</lot_no>\r\n");
						valueXmlString.append("<lot_sl isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(rs.getString(2)).append("]]>").append("</lot_sl>\r\n");
						valueXmlString.append("<conv_qty_stduom isSrvCallOnChg=\"0\">").append("<![CDATA[").append(1)
								.append("]]>").append("</conv_qty_stduom>\r\n");
						valueXmlString.append("<acct_code isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append((acctCode == null) ? "" : acctCode).append("]]>").append("</acct_code>\r\n");
						valueXmlString.append("<cctr_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(cctrCode)
								.append("]]>").append("</cctr_code>\r\n");
						valueXmlString.append("<tax_chap isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append((taxChap == null) ? "" : taxChap).append("]]>").append("</tax_chap>\r\n");
						valueXmlString.append("<tax_class isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append((taxClass == null) ? "" : taxClass).append("]]>").append("</tax_class>\r\n");
						valueXmlString.append("<tax_env isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append((taxEnv == null) ? "" : taxEnv).append("]]>").append("</tax_env>\r\n");
						BaseLogger.log("9", null, null, 
								"rs.getString(13) :" + rs.getString(13) + " \nrs.getString(14) :" + rs.getString(14));
						if ((rs.getString(13) == null || rs.getString(13).trim().length() == 0)
								|| (rs.getString(14) == null || rs.getString(14).trim().length() == 0)) {
							retResult = acctDetrTType(itemCode, rs.getString(6), "IN", tranType);
							// retResult =
							// fCommon.getAcctDetrDistTtype("","",rs.getString(6),"",itemCode,"IN",tranType,conn);

							// public String getAcctDetrDistTtype(String siteCodeFrom,String siteCodeTo,
							// String itemSer,String grpCode,String itemCode, String purpose, String
							// tranType, Connection conn) throws ITMException
							// {

							BaseLogger.log("9", null, null, "retResult :" + retResult);
							if (retResult.substring(retResult.length() - 5).equals("DS000")) {
								acctCodeInv = " ";
								cctrCodeInv = " ";
							} else {
								acctCodeInvArrLst = genericUtility.getTokenList(retResult, "\t");
								BaseLogger.log("9", null, null, "acctCodeInvArrLst.size :" + acctCodeInvArrLst.size());
								BaseLogger.log("9", null, null, "acctCodeInvArrLst.get(1) :" + (String) acctCodeInvArrLst.get(1));
								acctCodeInv = (String) acctCodeInvArrLst.get(0);
								BaseLogger.log("9", null, null, "acctCodeInv :" + acctCodeInv);
								cctrCodeInv = (String) acctCodeInvArrLst.get(1);
								BaseLogger.log("9", null, null, "cctrCodeInv :" + cctrCodeInv);
							}
						}
						if (rs.getString(13) == null) {
							valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
									.append(acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
						} else {
							valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
									.append(rs.getString(13)).append("]]>").append("</acct_code__inv>\r\n");
						}
						if (rs.getString(14) == null) {
							valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
									.append(cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
						} else {
							valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
									.append(rs.getString(14)).append("]]>").append("</cctr_code__inv>\r\n");
						}
						valueXmlString.append("</Detail2>\r\n");
					}

					if (count == 0) {

						countl++;

						BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + countl + ">>>>>>>><<<<" + NOL);
						if (NOL == countl) {

							BaseLogger.log("9", null, null, "NOL gets match");

							BaseLogger.log("9", null, null, "enter in false condition))");
							BaseLogger.log("9", null, null, "dsdddd");
							String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
							String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
							BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
							BaseLogger.log("9", null, null, "enter in elsssee");
							int CNT = 0;
							sql = "";
							sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, consOrdFrom);
							pstmt1.setString(2, consOrdTo);

							rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								CNT = rs11.getInt(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;

							if (CNT == 1) {
								return "ERRORMSG";
							} else {
								CNT = 0;
								sql = "";
								sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
										+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? " + " AND B.AVAILABLE = ? "
										+ " AND A.LOC_CODE = ? " + " AND B.STAT_TYPE<>'S' " + // Condition added by
																								// Manoj dtd 03/06/2016
																								// to exclude intransit
																								// location
										" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))  > 0";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, itemCode);
								pstmt1.setString(2, siteCodeReq);
								pstmt1.setString(3, availableYn);
								pstmt1.setString(4, locCode);

								rs11 = pstmt1.executeQuery();
								BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
								if (rs11.next()) {
									BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
									CNT = rs11.getInt(1);
								}
								BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
								pstmt1.close();
								rs11.close();
								pstmt1 = null;
								rs11 = null;
								if (CNT == 0) {

									errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
									BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
									return errString;
								}
							}

						}

						return "DETERROR";

					}

					if (remainingQty > 0) {
						if (partQuantity.equals("X")) {
							errCode = "VTSTOCK1";
							errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
							BaseLogger.log("9", null, null, "errString :" + errString);
							return errString;

						}
					}

					stmt = null;
					if (errCode != null && errCode.trim().length() > 0) {
						BaseLogger.log("9", null, null, "errCode :" + errCode);
						errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
						BaseLogger.log("9", null, null, "errString :" + errString);
						return errString;
					}

				}

			} else {

				BaseLogger.log("9", null, null, "ENTER IN WHEN SOMETHING IS NULL");
				sql = "";
				PreparedStatement pstmt1 = null;
				if (LocGrp == null || LocGrp.trim().length() == 0) {
					sql = "SELECT LOC_CODE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND  (QUANTITY - (ALLOC_QTY + (CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END))) > 0 ORDER BY EXP_DATE";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					pstmt1.setString(2, siteCodeReq);
					BaseLogger.log("9", null, null, "><><><><" + itemCode + "SDSDSD" + siteCodeReq);

				} else {
					sql = "select loc_code from stock where item_code = ? and site_code = ? and  loc_code in  (select loc_code from location   where  loc_group = ?) and (QUANTITY - (ALLOC_QTY + (CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END))) > 0 order by exp_date";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					pstmt1.setString(2, siteCodeReq);
					pstmt1.setString(3, LocGrp);
				}

				ResultSet rs11 = pstmt1.executeQuery();

				String LOCC = "";
				BaseLogger.log("9", null, null, "rs11 not null ------->>-----");

				while (rs11.next()) {

					BaseLogger.log("9", null, null, "rs11 is true................");
					BaseLogger.log("9", null, null, "ENTER IN WHEN LOC  COMBINATON IS NOT NULL");
					LOCC = rs11.getString(1) == null ? "" : rs11.getString(1);
					sqlpt = "";
					sqlpt = "SELECT inv_stat FROM LOCATION WHERE LOC_CODE = ?";

					pstmtt = conn.prepareStatement(sqlpt);
					pstmtt.setString(1, LOCC);

					rst = pstmtt.executeQuery();
					if (rst.next()) {
						invst = rst.getString("inv_stat") == null ? "" : rst.getString("inv_stat");
					}

					pstmtt.close();
					pstmtt = null;
					sqlpt = null;
					rst.close();
					rst = null;

					sqlpt = "";
					sqlpt = "select available,usable,stat_type from invstat where inv_stat = ?";

					pstmtt = conn.prepareStatement(sqlpt);
					pstmtt.setString(1, invst);

					rst = pstmtt.executeQuery();
					if (rst.next()) {
						availableinv = rst.getString("available") == null ? "" : rst.getString("available");
						usableinv = rst.getString("usable") == null ? "" : rst.getString("usable");
						statType = rst.getString("stat_type");
					}

					pstmtt.close();
					pstmtt = null;
					sqlpt = null;
					rst.close();
					rst = null;

					sqlpt = "";
					sqlpt = "select usable from location where loc_code = ?";

					pstmtt = conn.prepareStatement(sqlpt);
					pstmtt.setString(1, LOCC);

					rst = pstmtt.executeQuery();
					if (rst.next()) {
						usableloc = rst.getString("usable") == null ? "" : rst.getString("usable");
					}

					pstmtt.close();
					pstmtt = null;
					sqlpt = null;
					rst.close();
					rst = null;
					BaseLogger.log("9", null, null, "dadaso--------------------");
					BaseLogger.log("9", null, null, "avalable>>>>>" + availableinv + "->>>>>" + usableloc
							+ ">>>>>>>>>>fdfdfdfdfd>>>>>>" + usableinv);

					if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")
							&& usableinv.equalsIgnoreCase("Y") && !"S".equalsIgnoreCase(statType)) {
						BaseLogger.log("9", null, null, "enter in available >>>>>>>>????" + LocGrp + "LLFLDLF" + locCode);

						if (LocGrp == null && ((locCode == "" || locCode == null) && locCode.trim().length() == 0)) {
							BaseLogger.log("9", null, null, "both null");
							sql = "";
							if (lotNo == null || lotNo.trim().length() == 0) {
								BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>6");
								sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
										+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
										+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
										+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
										+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
										+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND B.AVAILABLE = '"
										+ availableYn + "' " + " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd
																						// 03/06/2016 to exclude
																						// intransit location
										+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) > 0 "
										+ "ORDER BY A.EXP_DATE";

								BaseLogger.log("9", null, null, "check here SS");
							}

							else {

								BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>10");
								sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
										+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
										+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
										+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
										+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
										+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND A.LOT_NO    = '" + lotNo
										+ "' " + "AND A.LOT_SL	 = '" + lotSl + "' " + " AND B.STAT_TYPE<>'S' "// Condition
																												// added
																												// by
																												// Manoj
																												// dtd
																												// 03/06/2016
																												// to
																												// exclude
																												// intransit
																												// location
										+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) >="
										+ quantity + " AND B.AVAILABLE = '" + availableYn + "'" + "ORDER BY A.EXP_DATE";
							} //
							BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>7");
							break;
						}

						if ((locCode != null && locCode.trim().length() > 0) && LocGrp == null) {
							sql = "";
							BaseLogger.log("9", null, null, "LOCGRP NULL AND LOC CODE NOT NULL");
							int vLocCode = 0;
							sql = "";
							sql = "SELECT COUNT(*) FROM STOCK WHERE LOC_CODE = ? AND ITEM_CODE = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, locCode);
							pstmt1.setString(2, itemCode);
							rs11 = pstmt1.executeQuery();

							if (rs11.next()) {
								vLocCode = rs11.getInt(1);
								BaseLogger.log("9", null, null, ">>cc>v>v>v>v" + vLocCode + "jdhudf" + locCode);

								if (vLocCode == 0) {

									countitm++;

									BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + countitm + ">>>>>>>><<<<" + NOL);
									if (NOL == countitm) {

										BaseLogger.log("9", null, null, "NOL gets match");

										BaseLogger.log("9", null, null, "enter in false condition))");
										BaseLogger.log("9", null, null, "dsdddd");
										String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
										String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
										BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
										BaseLogger.log("9", null, null, "enter in elsssee");
										int CNT = 0;
										sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, consOrdFrom);
										pstmt1.setString(2, consOrdTo);

										rs11 = pstmt1.executeQuery();
										BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
										if (rs11.next()) {
											BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
											CNT = rs11.getInt(1);
										}
										BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
										pstmt1.close();
										rs11.close();
										pstmt1 = null;
										rs11 = null;

										if (CNT == 1) {
											return "ERRORMSG";
										} else {
											CNT = 0;
											sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
													+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? "
													+ " AND B.AVAILABLE = ? " + " AND A.LOC_CODE = ? "
													+ " AND B.STAT_TYPE<>'S' " + // Condition added by Manoj dtd
																					// 03/06/2016 to exclude intransit
																					// location
													" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) > 0";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, itemCode);
											pstmt1.setString(2, siteCodeReq);
											pstmt1.setString(3, availableYn);
											pstmt1.setString(4, locCode);

											rs11 = pstmt1.executeQuery();
											BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
											if (rs11.next()) {
												BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
												CNT = rs11.getInt(1);
											}
											BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
											pstmt1.close();
											rs11.close();
											pstmt1 = null;
											rs11 = null;
											if (CNT == 0) {

												errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
												BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
												return errString;
											}
										}

									} else {
										return "DETERROR";
									}

								}

							}

							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;

							BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>2");
							BaseLogger.log("9", null, null, "locgrp null but loccode is not null");
							sql = "";
							if (lotNo == null || lotNo.trim().length() == 0) {

								BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>");
								sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
										+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
										+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
										+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
										+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
										+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND B.AVAILABLE = '"
										+ availableYn + "' " + " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd
																						// 03/06/2016 to exclude
																						// intransit location
										+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) > 0 "
										+ "AND A.LOC_CODE  LIKE '" + locCode + "' " + "ORDER BY A.EXP_DATE";

								BaseLogger.log("9", null, null, "check here ass");
							}

							else {

								sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
										+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
										+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
										+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
										+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
										+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND A.LOC_CODE  = '" + locCode
										+ "' " + "AND A.LOT_NO    = '" + lotNo + "' " + "AND A.LOT_SL	 = '" + lotSl
										+ "' " + " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd 03/06/2016 to
																			// exclude intransit location
										+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) >="
										+ quantity + " AND B.AVAILABLE = '" + availableYn + "'" + "ORDER BY A.EXP_DATE";

							} //
							break;
						}

						if ((LocGrp != null && LocGrp.trim().length() > 0)
								&& ((locCode == "" || locCode == null) && locCode.trim().length() == 0)) {
							String chk = checkLocgrp(dom, consOrder, itemCode, locCode, LocGrp, conn);
							if (chk.length() > 0) {
								BaseLogger.log("9", null, null, chk + "duifwedt");
								return chk;

							}

							int lgrp = 0;
							sqlpt = "SELECT COUNT(*) FROM STOCK A,LOCATION L WHERE L.LOC_CODE = A.LOC_CODE AND A.ITEM_CODE = ? AND L.LOC_GROUP = ?";

							pstmtt = conn.prepareStatement(sqlpt);
							pstmtt.setString(1, itemCode);
							pstmtt.setString(2, LocGrp);

							rst = pstmtt.executeQuery();
							while (rst.next()) {
								BaseLogger.log("9", null, null, "continueee parsingdd");
								lgrp = rst.getInt(1);

								if (lgrp == 0) {

									countitm1++;

									BaseLogger.log("9", null, null, 
											">>>>>>>>>>>>dskkkdsdsd><<<<<<<<<VAL" + countitm1 + ">>>>>>>><<<<" + NOL);
									if (NOL == countitm1) {

										BaseLogger.log("9", null, null, "shddddjfgggfghd))");
										BaseLogger.log("9", null, null, "dsdddd");
										String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
										String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
										BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
										BaseLogger.log("9", null, null, "enter in elsssee");
										int CNT = 0;
										sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, consOrdFrom);
										pstmt1.setString(2, consOrdTo);

										rs11 = pstmt1.executeQuery();
										BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
										if (rs11.next()) {
											BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
											CNT = rs11.getInt(1);
										}
										BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
										pstmt1.close();
										rs11.close();
										pstmt1 = null;
										rs11 = null;

										if (CNT == 1) {
											return "ERRORMSG";
										} else {
											CNT = 0;
											sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
													+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? "
													+ " AND B.AVAILABLE = ? " + " AND A.LOC_CODE = ? "
													+ " AND B.STAT_TYPE<>'S' " + // Condition added by Manoj dtd
																					// 03/06/2016 to exclude intransit
																					// location
													" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))  > 0";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, itemCode);
											pstmt1.setString(2, siteCodeReq);
											pstmt1.setString(3, availableYn);
											pstmt1.setString(4, locCode);

											rs11 = pstmt1.executeQuery();
											BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
											if (rs11.next()) {
												BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
												CNT = rs11.getInt(1);
											}
											BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
											pstmt1.close();
											rs11.close();
											pstmt1 = null;
											rs11 = null;
											if (CNT == 0) {

												errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
												BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
												return errString;
											}
										}
									} else {
										return "DETERROR";
									}

								} else {
									continue;
								}

							}

							pstmtt.close();
							pstmtt = null;
							sqlpt = null;

							rst.close();
							rst = null;

							BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>3");
							BaseLogger.log("9", null, null, "locgrp not null but loccode is null");
							sql = "";
							if (lotNo == null || lotNo.trim().length() == 0) {
								BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>4");
								sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
										+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
										+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
										+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
										+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
										+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND B.AVAILABLE = '"
										+ availableYn + "' " + " AND B.STAT_TYPE<>'S' "// Condition added by Manoj dtd
																						// 03/06/2016 to exclude
																						// intransit location
										+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) > 0 "
										+ "AND L.LOC_GROUP = '" + LocGrp + "' " + "ORDER BY A.EXP_DATE";

								BaseLogger.log("9", null, null, "check here ass");
							}

							else {
								BaseLogger.log("9", null, null, "ADDED IN vDFGHDFFGTFDDV>>>>>>> >>>>>>>5");
								sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))), A.EXP_DATE, A.UNIT, "
										+ "A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
										+ "A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE "
										+ "FROM STOCK A, LOCATION L, INVSTAT B " + "WHERE L.LOC_CODE = A.LOC_CODE "
										+ " AND L.INV_STAT  = B.INV_STAT " + "AND A.ITEM_CODE = '" + itemCode + "' "
										+ "AND A.SITE_CODE = '" + siteCodeReq + "' " + "AND A.LOT_NO    = '" + lotNo
										+ "' " + "AND A.LOT_SL	 = '" + lotSl + "' " + " AND B.STAT_TYPE<>'S' "// Condition
																												// added
																												// by
																												// Manoj
																												// dtd
																												// 03/06/2016
																												// to
																												// exclude
																												// intransit
																												// location
										+ "AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END))) >="
										+ quantity + " AND B.AVAILABLE = '" + availableYn + "'" + "AND L.LOC_GROUP = '"
										+ LocGrp + "' " + "ORDER BY A.EXP_DATE";
							} //
							break;
						}

						BaseLogger.log("9", null, null, "enter in salable >>>>>>>>????");
					} else {
						BaseLogger.log("9", null, null, "in continue..............");
						continue;
					}

				}
				BaseLogger.log("9", null, null, ">>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<SDSDSDS");

				if (availableinv.equalsIgnoreCase("Y") && usableloc.equalsIgnoreCase("Y")
						&& usableinv.equalsIgnoreCase("Y") && !"S".equalsIgnoreCase(statType)) {

				} else {
					if (rs11.next() == false) {
						COUNTSTK1++;

						BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + COUNTSTK1 + ">>>>>>>><<<<" + NOL);
						if (NOL == COUNTSTK1) {

							BaseLogger.log("9", null, null, "NOL gets match");

							BaseLogger.log("9", null, null, "enter in false condition))");
							BaseLogger.log("9", null, null, "dsdddd");
							String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
							String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
							BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
							BaseLogger.log("9", null, null, "enter in elsssee");
							int CNT = 0;
							sql = "";
							sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, consOrdFrom);
							pstmt1.setString(2, consOrdTo);

							rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								CNT = rs11.getInt(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;

							if (CNT == 1) {
								return "ERRORMSG";
							} else {
								CNT = 0;
								sql = "";
								sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
										+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? " + " AND B.AVAILABLE = ? "
										+ " AND A.LOC_CODE = ? " + " AND B.STAT_TYPE<>'S' " + // Condition added by
																								// Manoj dtd 03/06/2016
																								// to exclude intransit
																								// location
										" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))  > 0";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, itemCode);
								pstmt1.setString(2, siteCodeReq);
								pstmt1.setString(3, availableYn);
								pstmt1.setString(4, locCode);

								rs11 = pstmt1.executeQuery();
								BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
								if (rs11.next()) {
									BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
									CNT = rs11.getInt(1);
								}
								BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
								pstmt1.close();
								rs11.close();
								pstmt1 = null;
								rs11 = null;
								if (CNT == 0) {

									errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
									BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
									return errString;
								}
							}

						} else {
							return "DETERROR";
						}

					}
				}

				BaseLogger.log("9", null, null, "?????>>>>>>>>>>>>>>>>>????");

				BaseLogger.log("9", null, null, "OUT OF THE LOOP");

				BaseLogger.log("9", null, null, "CHECKING SQL HERE FROM LOOP >>>>" + sql);

				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					lotNum = rs.getString(1);
					locCode1 = rs.getString(12);
					BaseLogger.log("9", null, null, "lotNum :" + lotNum);
					BaseLogger.log("9", null, null, "locCode1 :" + locCode1);
					if (!hm.containsKey(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum)) {
						hm.put(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum,
								new Double(rs.getDouble(3)));
					}
				}
				BaseLogger.log("9", null, null, "Hashmap :" + hm);
				remainingQty = quantity;
				BaseLogger.log("9", null, null, "remainingQty :" + remainingQty);

				rs.close();
				rs = null;
				stmt.close();
				stmt = null;

				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);

				int count = 0;

				while (rs.next()) {
					count++;
					BaseLogger.log("9", null, null, "ENTER IN 2ND WHILE LOOP");

					lotNum = rs.getString(1);
					locCode1 = rs.getString(12);
					System.out.print(
							"Combination Key :  " + itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum);
					hmQty = Double.parseDouble(
							(hm.get(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum)).toString());
					BaseLogger.log("9", null, null, " ::Value [hmQty] :: " + hmQty);
					if (hmQty == 0) {
						continue;
					}
					if (availableYn.equals("Y")) {
						sql1 = "SELECT MIN_SHELF_LIFE, (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) "
								+ "FROM ITEM WHERE ITEM_CODE = '" + itemCode + "' ";
						BaseLogger.log("9", null, null, "sql1 :" + sql1);
						stmt1 = conn.createStatement();
						rs1 = stmt1.executeQuery(sql1);
						if (rs1.next()) {
							minShelfLife = rs1.getInt(1);
							trackShelfLife = rs1.getString(2);
						}
						stmt1.close();
						stmt1 = null;
						stkExpFlag = false;
						String token = "";
						StringTokenizer stToken = new StringTokenizer(stkExpLoc, ",");
						while (stToken.hasMoreTokens()) {
							token = stToken.nextToken();
							if (locCode.equalsIgnoreCase(token)) {
								stkExpFlag = true;
								break;
							}
						}
						if (stkExpFlag == false) {
							if (minShelfLife == 0) {
								minShelfLife = 1;
							}
							// if (trackShelfLife.equals("Y"))
							// {
							// chkDate = calcExpiry(allocDate,minShelfLife);
							// BaseLogger.log("9", null, null, "chkDate :"+chkDate);
							// chkDate1 = sdf.parse(chkDate);
							// BaseLogger.log("9", null, null, "chkDate1 :"+chkDate1);
							// expDate = rs.getDate(4);
							// BaseLogger.log("9", null, null, "expDate :"+expDate);
							// if(expDate != null)
							// {
							// expDate1 = new java.util.Date(expDate.getTime());
							// BaseLogger.log("9", null, null, "expDate1 :"+expDate1);
							// if (chkDate1.compareTo(expDate1) > 0)
							// {
							// continue;
							// }
							// }
							// }
						}
					}
					BaseLogger.log("9", null, null, "remainingQty before if :" + remainingQty);
					BaseLogger.log("9", null, null, "rs.getDouble(3) :" + rs.getDouble(3));

					if (remainingQty == 0) {
						break;
					}

					else if (hmQty >= remainingQty) {
						inputQty = remainingQty;
						BaseLogger.log("9", null, null, "inputQty :" + inputQty);
						remainingQty = 0;
						hm.put(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum,
								new Double(hmQty - inputQty));

						BaseLogger.log("9", null, null, "hmQty - remainingQty :" + (hmQty - inputQty));
						BaseLogger.log("9", null, null, "hm if [hmQty >= remainingQty] :" + hm);
					}

					else if (hmQty < remainingQty) {

						inputQty = hmQty;
						BaseLogger.log("9", null, null, "inputQty :" + inputQty);

						remainingQty = remainingQty - inputQty;
						BaseLogger.log("9", null, null, "remainingQty :" + remainingQty);
						hm.put(itemCode + "~" + siteCodeReq + "~" + locCode1 + "~" + lotNum, new Double(0));
					}
					BaseLogger.log("9", null, null, "Hashmap :" + hm);

					BaseLogger.log("9", null, null, lineOrd);
					valueXmlString.append("<Detail2 objContext =\"2\"")
							.append(" objName=\"consumption_issue\" domID=\"1\" dbID=\"\">");
					valueXmlString.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
					valueXmlString.append("<cons_issue/>");
					// valueXmlString.append("<line_no
					// isSrvCallOnChg=\"0\">").append("<![CDATA[").append(line).append("]]>").append("</line_no>\r\n");
					BaseLogger.log("9", null, null, "rs.getDouble(15) :" + rs.getDouble(15));
					if (rs.getDouble(15) > 0) {
						valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(15)).append("]]>")
								.append("</rate>\r\n");
					} else {
						sql1 = "SELECT RATE	FROM CONSUME_ORD_DET " + "WHERE CONS_ORDER = '" + consOrder + "' "
								+ "AND LINE_NO = " + lineOrd + " ";
						BaseLogger.log("9", null, null, "sql1 :" + sql1);
						stmt1 = conn.createStatement();
						rs1 = stmt1.executeQuery(sql1);
						if (rs1.next()) {
							valueXmlString.append("<rate>").append("<![CDATA[").append(rs1.getDouble(1)).append("]]>")
									.append("</rate>\r\n");
						}
						stmt1.close();
						stmt1 = null;
					}

					valueXmlString.append("<cons_order isSrvCallOnChg=\"0\">").append("<![CDATA[").append(consOrder)
							.append("]]>").append("</cons_order>\r\n");
					valueXmlString.append("<line_no__ord isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lineOrd)
							.append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemCode)
							.append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemDescr)
							.append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg=\"0\">").append("<![CDATA[").append(inputQty)
							.append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<quantity__std isSrvCallOnChg=\"0\">").append("<![CDATA[").append(inputQty)
							.append("]]>").append("</quantity__std>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(5))
							.append("]]>").append("</unit>\r\n");
					valueXmlString.append("<unit__std isSrvCallOnChg=\"0\">").append("<![CDATA[")
							.append(rs.getString(5)).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<loc_code isSrvCallOnChg=\"0\">").append("<![CDATA[")
							.append(rs.getString(12)).append("]]>").append("</loc_code>\r\n");

					valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(rs.getString(1))
							.append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(2))
							.append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<conv_qty_stduom isSrvCallOnChg=\"0\">").append("<![CDATA[").append(1)
							.append("]]>").append("</conv_qty_stduom>\r\n");
					valueXmlString.append("<acct_code isSrvCallOnChg=\"0\">").append("<![CDATA[")
							.append((acctCode == null) ? "" : acctCode).append("]]>").append("</acct_code>\r\n");
					valueXmlString.append("<cctr_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(cctrCode)
							.append("]]>").append("</cctr_code>\r\n");
					valueXmlString.append("<tax_chap isSrvCallOnChg=\"0\">").append("<![CDATA[")
							.append((taxChap == null) ? "" : taxChap).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_class isSrvCallOnChg=\"0\">").append("<![CDATA[")
							.append((taxClass == null) ? "" : taxClass).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_env isSrvCallOnChg=\"0\">").append("<![CDATA[")
							.append((taxEnv == null) ? "" : taxEnv).append("]]>").append("</tax_env>\r\n");
					BaseLogger.log("9", null, null, 
							"rs.getString(13) :" + rs.getString(13) + " \nrs.getString(14) :" + rs.getString(14));
					if ((rs.getString(13) == null || rs.getString(13).trim().length() == 0)
							|| (rs.getString(14) == null || rs.getString(14).trim().length() == 0)) {
						retResult = acctDetrTType(itemCode, rs.getString(6), "IN", tranType);
						// retResult =
						// fCommon.getAcctDetrDistTtype("","",rs.getString(6),"",itemCode,"IN",tranType,conn);

						// public String getAcctDetrDistTtype(String siteCodeFrom,String siteCodeTo,
						// String itemSer,String grpCode,String itemCode, String purpose, String
						// tranType, Connection conn) throws ITMException
						// {

						BaseLogger.log("9", null, null, "retResult :" + retResult);
						if (retResult.substring(retResult.length() - 5).equals("DS000")) {
							acctCodeInv = " ";
							cctrCodeInv = " ";
						} else {
							acctCodeInvArrLst = genericUtility.getTokenList(retResult, "\t");
							BaseLogger.log("9", null, null, "acctCodeInvArrLst.size :" + acctCodeInvArrLst.size());
							BaseLogger.log("9", null, null, "acctCodeInvArrLst.get(1) :" + (String) acctCodeInvArrLst.get(1));
							acctCodeInv = (String) acctCodeInvArrLst.get(0);
							BaseLogger.log("9", null, null, "acctCodeInv :" + acctCodeInv);
							cctrCodeInv = (String) acctCodeInvArrLst.get(1);
							BaseLogger.log("9", null, null, "cctrCodeInv :" + cctrCodeInv);
						}
					}
					if (rs.getString(13) == null) {
						valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
					} else {
						valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(rs.getString(13)).append("]]>").append("</acct_code__inv>\r\n");
					}
					if (rs.getString(14) == null) {
						valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
					} else {
						valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[")
								.append(rs.getString(14)).append("]]>").append("</cctr_code__inv>\r\n");
					}
					valueXmlString.append("</Detail2>\r\n");
				}

				if (count == 0) {

					countlf++;

					BaseLogger.log("9", null, null, ">>>>>>>>>>>>><<<<<<<<<VAL" + countlf + ">>>>>>>><<<<" + NOL);
					if (NOL == countlf) {

						BaseLogger.log("9", null, null, "NOL gets match");

						BaseLogger.log("9", null, null, "enter in false condition))");
						BaseLogger.log("9", null, null, "dsdddd");
						String consOrdFrom = genericUtility.getColumnValue("cons_order__fr", dom);
						String consOrdTo = genericUtility.getColumnValue("cons_order__to", dom);
						BaseLogger.log("9", null, null, "<<<>>::" + consOrdFrom + "+consOrdTo+" + consOrdTo);
						BaseLogger.log("9", null, null, "enter in elsssee");
						int CNT = 0;
						sql = "SELECT COUNT(*) FROM CONSUME_ORD WHERE CONS_ORDER BETWEEN ? AND ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, consOrdFrom);
						pstmt1.setString(2, consOrdTo);

						rs11 = pstmt1.executeQuery();
						BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
						if (rs11.next()) {
							BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
							CNT = rs11.getInt(1);
						}
						BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
						pstmt1.close();
						rs11.close();
						pstmt1 = null;
						rs11 = null;

						if (CNT == 1) {
							return "ERRORMSG";
						} else {
							CNT = 0;
							sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND"
									+ " A.ITEM_CODE = ? " + " AND A.SITE_CODE = ? " + " AND B.AVAILABLE = ? "
									+ " AND A.LOC_CODE = ? " + " AND B.STAT_TYPE<>'S' " + // Condition added by Manoj
																							// dtd 03/06/2016 to exclude
																							// intransit location
									" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))  > 0";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, siteCodeReq);
							pstmt1.setString(3, availableYn);
							pstmt1.setString(4, locCode);

							rs11 = pstmt1.executeQuery();
							BaseLogger.log("9", null, null, "CNT CNT ----->>" + CNT);
							if (rs11.next()) {
								BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
								CNT = rs11.getInt(1);
							}
							BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
							pstmt1.close();
							rs11.close();
							pstmt1 = null;
							rs11 = null;
							if (CNT == 0) {

								errString = itmDBAccess.getErrorString("", "CONSCOMBVR", "", "", conn);
								BaseLogger.log("9", null, null, "CNT CNT -CV VBBGB---1wewewe->>" + errString);
								return errString;
							}
						}

					}

					return "DETERROR";

				}

				if (remainingQty > 0) {
					if (partQuantity.equals("X")) {
						errCode = "VTSTOCK1";
						errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
						BaseLogger.log("9", null, null, "errString :" + errString);
						return errString;

					}
				}

				stmt = null;
				if (errCode != null && errCode.trim().length() > 0) {
					BaseLogger.log("9", null, null, "errCode :" + errCode);
					errString = itmDBAccess.getErrorString("", errCode, "", "", conn);
					BaseLogger.log("9", null, null, "errString :" + errString);
					return errString;
				}

			}

		} catch (SQLException sqx) {
			BaseLogger.log("0", null, null, "The SQLException occurs in ConsumeIssueAct :(Allocate) Button :" + sqx);
			throw new ITMException(sqx);
		} catch (Exception e) {
			BaseLogger.log("0", null, null, "The Exception occurs in ConsumeIssueAct : (Allocate) Button :" + e);
			throw new ITMException(e);
		} finally {
			// Commeted by manoj dtd 03/06/2016 to use single connection object
			/**
			 * try { conn.close(); conn = null; } catch (Exception e){}
			 */
		}
		BaseLogger.log("0", null, null, "valueXmlString return from ConsumeIssueAct[actionAllocate] :" + valueXmlString.toString());
		return valueXmlString.toString();

	}

	public boolean isStockAvailable(Connection conn, String itemCode, String siteCodeReq, String availableYn,
			String locCode) {
		BaseLogger.log("9", null, null, "ENTER IN METOD");
		PreparedStatement pstmt1 = null;
		ResultSet rs11 = null;
		String sql = "";

		int CNT = 0;

		try {
			sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND" + " A.ITEM_CODE = ? "
					+ " AND A.SITE_CODE = ? " + " AND B.AVAILABLE = ? " + " AND A.LOC_CODE = ? "
					+ " AND B.STAT_TYPE<>'S' " + // Condition added by Manoj dtd 03/06/2016 to exclude intransit
													// location
					" AND (A.QUANTITY - (A.ALLOC_QTY + (CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END)))  > 0";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, itemCode);
			pstmt1.setString(2, siteCodeReq);
			pstmt1.setString(3, availableYn);
			pstmt1.setString(4, locCode);

			rs11 = pstmt1.executeQuery();
			BaseLogger.log("3", null, null, "CNT CNT ----->>" + CNT);
			if (rs11.next()) {
				BaseLogger.log("9", null, null, "CNT CNT --IN-->>" + CNT);
				CNT = rs11.getInt(1);
				BaseLogger.log("9", null, null, "checkkkkkk ______DD" + CNT);
			}
			pstmt1.close();
			rs11.close();
			pstmt1 = null;
			rs11 = null;
			BaseLogger.log("9", null, null, "CNT CNT ----1->>" + CNT);
			if (CNT > 0) {
				return true;
			}			
		} catch (Exception E) {
			BaseLogger.log("0", null, null, "Exception in is StockAvailable");
			E.printStackTrace();
		}

		return false;
	}

	private String getErrorString(String string, String errCode, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String calcExpiry(String tranDate, int months) {

		java.util.Date expDate = new java.util.Date();
		java.util.Date retDate = new java.util.Date();
		String retStrInDate = "";
		BaseLogger.log("3", null, null, "tranDate :" + tranDate + "\nmonths :" + months);
		try {
			// GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (months > 0) {
				Calendar cal = Calendar.getInstance();
				expDate = sdf.parse(tranDate);
				BaseLogger.log("3", null, null, "expDate :" + expDate);
				cal.setTime(expDate);
				cal.add(Calendar.MONTH, months);

				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DATE, 0);

				retDate = cal.getTime();
				retStrInDate = sdf.format(retDate);
			} else {
				retStrInDate = tranDate;
			}
		} catch (Exception e) {
			BaseLogger.log("0", null, null, "The Exception occurs in calcExpiry :" + e);
		}
		BaseLogger.log("3", null, null, "retStrInDate :" + retStrInDate);
		return retStrInDate;

	}

	private String acctDetrTType(String itemCode, String itemSer, String purpose, String tranType) throws Exception {

		BaseLogger.log("2", null, null, "acctDetrTType Calling................");
		BaseLogger.log("3", null, null, "The values of parameters are :\n itemCode :" + itemCode + " \n itemSer :" + itemSer
				+ " \n purpose :" + purpose + " \n tranType :" + tranType);
		String sql = "", stkOption = "", acctCode = "", cctrCode = "", itemSer1 = "", retStr = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		// GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try {
			// Changes and Commented By Poonam on 08-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Poonam on 08-06-2016 :END

			stmt = conn.createStatement();
			if (purpose.equals("IN")) { // if 1
				sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
				BaseLogger.log("9", null, null, "sql :" + sql);
				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					stkOption = rs.getString(1);
					BaseLogger.log("9", null, null, "stkOption :" + stkOption);
				}
				if (stkOption.equals("0")) { // if II
					sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR " + "WHERE ITEM_CODE = '" + itemCode
							+ "' " + "AND ITEM_SER = '" + itemSer + "' " + "AND TRAN_TYPE = '" + tranType + "'";
					BaseLogger.log("9", null, null, "sql from if part :" + sql);
					rs = stmt.executeQuery(sql);
					if (rs.next()) {
						acctCode = rs.getString(1);
						BaseLogger.log("9", null, null, "acctCode :" + acctCode);
						cctrCode = rs.getString(2);
						BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
					}
					// if (acctCode == null || acctCode.equals(""))
					// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
					if (acctCode == null || acctCode.trim().length() == 0) {
						sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
								+ "WHERE ITEM_SER = ' ' AND ITEM_CODE = '" + itemCode + "' " + "AND TRAN_TYPE = '"
								+ tranType + "'";
						BaseLogger.log("9", null, null, "sql :" + sql);
						rs = stmt.executeQuery(sql);
						if (rs.next()) {
							acctCode = rs.getString(1);
							BaseLogger.log("9", null, null, "acctCode :" + acctCode);
							cctrCode = rs.getString(2);
							BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
						}
						// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
						if (acctCode == null || acctCode.trim().length() == 0) {
							sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
									+ "WHERE ITEM_SER = ' ' AND ITEM_CODE = '" + itemCode + "' "
									+ "AND TRAN_TYPE = ' '";
							BaseLogger.log("9", null, null, "sql :" + sql);
							rs = stmt.executeQuery(sql);
							if (rs.next()) {
								acctCode = rs.getString(1);
								BaseLogger.log("9", null, null, "acctCode :" + acctCode);
								cctrCode = rs.getString(2);
								BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
							}
							// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
							if (acctCode == null || acctCode.trim().length() == 0) {// if III
								if (itemSer == null && itemSer.trim().length() == 0) {
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
									BaseLogger.log("9", null, null, "sql :" + sql);
									rs = stmt.executeQuery(sql);
									if (rs.next()) {
										itemSer1 = rs.getString(1);
										BaseLogger.log("9", null, null, "itemSer1 :" + itemSer1);
									}
								} else {
									itemSer1 = itemSer;
									BaseLogger.log("9", null, null, "itemSer1 :" + itemSer1);
								}
								sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR " + "WHERE ITEM_SER = '"
										+ itemSer1 + "' " + "AND ITEM_CODE = ' ' AND TRAN_TYPE = '" + tranType + "'";
								BaseLogger.log("9", null, null, "sql :" + sql);
								rs = stmt.executeQuery(sql);
								if (rs.next()) {
									acctCode = rs.getString(1);
									BaseLogger.log("9", null, null, "acctCode :" + acctCode);
									cctrCode = rs.getString(2);
									BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
								}
								// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
								if (acctCode == null || acctCode.trim().length() == 0) {
									sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
											+ "WHERE ITEM_SER = '" + itemSer1 + "' "
											+ "AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									BaseLogger.log("9", null, null, "sql :" + sql);
									rs = stmt.executeQuery(sql);
									if (rs.next()) {
										acctCode = rs.getString(1);
										BaseLogger.log("9", null, null, "acctCode :" + acctCode);
										cctrCode = rs.getString(2);
										BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
									}
									// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
									if (acctCode == null || acctCode.trim().length() == 0) {// if IV
										sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEMSER " + "WHERE ITEM_SER = '"
												+ itemSer;
										BaseLogger.log("9", null, null, "sql :" + sql);
										rs = stmt.executeQuery(sql);
										if (rs.next()) {
											acctCode = rs.getString(1);
											BaseLogger.log("9", null, null, "acctCode :" + acctCode);
											cctrCode = rs.getString(2);
											BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
										}
									} // end if IV
								}
							}
						}
					} // end if III
				} // end if II
				else {
					sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR " + "WHERE ITEM_CODE = '" + itemCode
							+ "' " + "AND ITEM_SER = '" + itemSer + "' " + "AND TRAN_TYPE = '" + tranType + "'";
					BaseLogger.log("9", null, null, "sql from else part :" + sql);
					rs = stmt.executeQuery(sql);
					if (rs.next()) {
						acctCode = rs.getString(1);
						BaseLogger.log("9", null, null, "acctCode :" + acctCode);
						cctrCode = rs.getString(2);
						BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
					}
					// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
					if (acctCode == null || acctCode.trim().length() == 0) {// if I
						sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
								+ "WHERE ITEM_SER = ' ' AND ITEM_CODE = '" + itemCode + "' " + "AND TRAN_TYPE = '"
								+ tranType + "'";
						BaseLogger.log("9", null, null, "sql from else part :" + sql);
						rs = stmt.executeQuery(sql);
						if (rs.next()) {
							acctCode = rs.getString(1);
							BaseLogger.log("9", null, null, "acctCode :" + acctCode);
							cctrCode = rs.getString(2);
							BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
						}
						// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
						if (acctCode == null || acctCode.trim().length() == 0) {// if II
							sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
									+ "WHERE ITEM_SER = ' ' AND ITEM_CODE = '" + itemCode + "' "
									+ "AND TRAN_TYPE = ' '";
							BaseLogger.log("9", null, null, "sql from else part :" + sql);
							rs = stmt.executeQuery(sql);
							if (rs.next()) {
								acctCode = rs.getString(1);
								BaseLogger.log("9", null, null, "acctCode :" + acctCode);
								cctrCode = rs.getString(2);
								BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
							}
							// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
							if (acctCode == null || acctCode.trim().length() == 0) {// if III
								if (itemSer == null && itemSer.trim().length() == 0) {
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
									BaseLogger.log("9", null, null, "sql :" + sql);
									rs = stmt.executeQuery(sql);
									if (rs.next()) {
										itemSer1 = rs.getString(1);
										BaseLogger.log("9", null, null, "itemSer1 :" + itemSer1);
									}
								} else {
									itemSer1 = itemSer;
									BaseLogger.log("9", null, null, "itemSer1 :" + itemSer1);
								}
								sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR " + "WHERE ITEM_SER = '"
										+ itemSer1 + "' " + "AND ITEM_CODE = ' ' AND TRAN_TYPE = '" + tranType + "'";
								BaseLogger.log("9", null, null, "sql from else part :" + sql);
								rs = stmt.executeQuery(sql);
								if (rs.next()) {
									acctCode = rs.getString(1);
									BaseLogger.log("9", null, null, "acctCode :" + acctCode);
									cctrCode = rs.getString(2);
									BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
								}
								// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
								if (acctCode == null || acctCode.trim().length() == 0) {// if IV
									sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
											+ "WHERE ITEM_SER = '" + itemSer1 + "' "
											+ "AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									BaseLogger.log("9", null, null, "sql :" + sql);
									rs = stmt.executeQuery(sql);
									if (rs.next()) {
										acctCode = rs.getString(1);
										BaseLogger.log("9", null, null, "acctCode :" + acctCode);
										cctrCode = rs.getString(2);
										BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
									}
									// Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
									if (acctCode == null || acctCode.trim().length() == 0) {// IF V
										sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEMSER " + "WHERE ITEM_SER = '"
												+ itemSer + "'";
										BaseLogger.log("9", null, null, "sql :" + sql);
										rs = stmt.executeQuery(sql);
										if (rs.next()) {
											acctCode = rs.getString(1);
											BaseLogger.log("9", null, null, "acctCode :" + acctCode);
											cctrCode = rs.getString(2);
											BaseLogger.log("9", null, null, "cctrCode :" + cctrCode);
										}
									} // end if V
								} // end if IV
							} // end if III
						} // end if II
					} // end if I
				} // end else
			} // end if I
		} // try end
		catch (SQLException sqx) {
			BaseLogger.log("0", null, null, "The exception occurs in acctDetrTType() :" + sqx);
			throw new ITMException(sqx);
		} catch (Exception e) {
			BaseLogger.log("0", null, null, "The exception occurs in acctDetrTType() :" + e);
			throw new ITMException(e);
		} finally {
			try {
				conn.close();
				conn = null;
			} catch (Exception e) {
			}
		}
		if (acctCode == null) {
			acctCode = "";
		}
		if (cctrCode == null) {
			cctrCode = "";
		}
		retStr = acctCode + "\t" + cctrCode;
		BaseLogger.log("9", null, null, "retStr :" + retStr);
		return retStr;

	}

	private String checkLocgrp(Document dom, String consOrder, String itemCode, String locCode, String LocGrp,
			Connection conn) {

		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		PreparedStatement pstmt1 = null;
		ResultSet rs11 = null;
		int cntcont = 0;
		String errString = "";
		int getint = 0;

		List<String> itemadd = new ArrayList<String>();
		List<Integer> getint1 = new ArrayList();

		try {
			sql = "";
			sql = "SELECT DISTINCT item_code FROM CONSUME_ORD_DET WHERE CONS_ORDER = ?";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, consOrder);

			rs11 = pstmt1.executeQuery();

			while (rs11.next()) {

				itemadd.add(rs11.getString(1));
			}

			pstmt1.close();
			rs11.close();
			pstmt1 = null;
			rs11 = null;

			int ccnt = itemadd.size();
			BaseLogger.log("9", null, null, "sdsdsdsdsdsd" + ccnt);

			sql = "";
			sql = "select count(1) from location where loc_group = ? AND ITEM_CODE = ?";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, LocGrp);
			pstmt1.setString(2, itemCode);
			rs11 = pstmt1.executeQuery();
			if (rs11.next()) {
				getint = rs11.getInt(1);
			}
			pstmt1.close();
			rs11.close();
			pstmt1 = null;
			rs11 = null;
			BaseLogger.log("9", null, null, ">>>>>>>>>>>>>>>>>>>>WSDWD" + getint);
			if (getint == 0) {
				countitmval++;
				BaseLogger.log("9", null, null, "cccccccccccccccc" + countitmval);
				if (ccnt == countitmval) {
					int val = 0;
					int COUNT = 0;
					for (int i = 0; i < itemadd.size(); i++) {

						BaseLogger.log("9", null, null, "_+__)_)_)_)_+" + i);
						sql = "";
						sql = "SELECT COUNT(*) FROM STOCK A,LOCATION L WHERE L.LOC_CODE = A.LOC_CODE AND A.ITEM_CODE = ? AND L.LOC_GROUP = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemadd.get(i));
						pstmt1.setString(2, LocGrp);
						rs11 = pstmt1.executeQuery();
						if (rs11.next()) {
							getint1.add(rs11.getInt(1));

							if (rs11.getInt(1) > 0) {
								COUNT++;
							}

						}
						pstmt1.close();
						rs11.close();
						pstmt1 = null;
						rs11 = null;

					}

					BaseLogger.log("9", null, null, ">>>>>>>>>SSSSSSSs" + getint1);
					BaseLogger.log("9", null, null, ">>>>>>DSDSD>>>SSSSSSSs" + COUNT);

					if (COUNT == 0) {
						errString = "LOCERROR";
						return errString;
					}

				}
			}

		} catch (Exception e) {
			BaseLogger.log("0", null, null, "message from above code" + e);
			errString = "Process not continued";
			return errString;
		}

		return errString;

	}

}
