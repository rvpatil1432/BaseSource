/********************************************************
	Title : POrderIC []
	Date  : 27 - APR - 2016

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.webitm.ejb.sys.UtilMethods;

@Stateless
public class POrderIC extends ValidatorEJB implements POrderICRemote,
POrderICLocal {

	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = new FinCommon();

	public String wfValData() throws RemoteException, ITMException {
		return "";
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		System.out.println("Validation Start.....xmlString[" + xmlString + "]]]]]xmlString1[[[[[" + xmlString1
				+ "]]]]]][[[[[[[" + xmlString2 + "]]]]]]].....");
		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
			.println("Exception : StarclubEligWorkIC() : wfValData(String xmlString) : ==>\n" + e.getMessage());
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;

		Node parentNode = null;
		Node childNode = null;
		NodeList parentNodeList1 = null;
		NodeList childNodeList1 = null;
		Node parentNode1 = null;
		Node childNode1 = null;
		// String columnValue = null;
		String childNodeName = null;
		String childNodeName1 = null;
		// ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		// String errcode = "";
		String userId = null;
		int cnt = 0;
		int ctr = 0;
		int ctr1 = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		int childNodeListLength1 = 0;
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		ConnDriver connDriver = new ConnDriver();

		String empCode = "";
		String siteCode = "";
		String errcode = "";
		Timestamp sysDate = null;
		int count = 0, projCnt = 0;
		String errorType = "";
		java.util.Date today = new java.util.Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		today = cal.getTime();
		SimpleDateFormat sdf = null;
		// ValidatorEJB ValidatorEJB = new ValidatorEJB();//Changed By PriyankaC on
		// 04Jan18
		FinCommon FinCommon = new FinCommon();
		DistCommon DistCommon = new DistCommon();

		// sysDate=sdf.format(today);
		/*
		 * Date toDt = null; Date fromDt = null; Date indDate = null; Date fmDate =
		 * null; Date vUpto = null;
		 */

		Timestamp toDt = null;

		Timestamp fromDt = null;
		Timestamp indDate = null;
		Timestamp fmDate = null;
		Timestamp vUpto = null;

		String ordDate = "", siteCodeord = "", contractNo = "", pordType = "", bomCode = "", suppCode = "",
				siteCodeDlv = "", siteCodeBill = "", deptCode = "", siteCodeOrd = "", relieveDate = "", termTable = "",
				tranCode = "";
		String itemSer = "", crTerm = "", currCode = "", msuppcurr = "", taxChap = "", taxClass = "", taxEnv = "",
				indNo = "", projCode = "", status = "", projIndno = "", quotNo = "", salesPers = "", dlvTerm = "",
				currCodefrt = "";
		String frtAmt = "", insuranceAmt = "", invAcctSer = "", currCodeins = "", saleOrder = "", conf = "",
				tranIdBoq = "", itemCode = "", qty = "", enqNo = "", proviTranid = "", itemSeries = "", mindOpt = "",
				acceptCriteria = "", policyNo = "", bankCodepay = "", payMode = "", active = "", locGroupjwiss = "",
				taskCode = "", termCode = "", relAmt = "", ordAmt = "", totAmt = "", lineNo = "", amtType = "",
				relAgnst = "", typeAllowProjbudgtBudgt = "", dueDate = "";
		String taskCodeParent = "", lineNoPrev = "", siteCodeAdv = "", purcOrder = "", minDay = "", maxDay = "",
				refCode = "";
		// String itemCode = "",
		String siteCodeBillPo = "", exchRate = "", rate = "", qtyStduom = "", rateStduom = "", discount = "",
				typeAllowProjbudgtList = "", projType = "", projectTypeOpt = "", projectTypeOptList = "", taxAmt = "",
				stopBusi = "", cp = "", mval1 = "", mval3 = "", itemSerCrpolicy = "", quotOpt = "", analCode = "",
				acctCodeDr = "", quantityFc = "", qtystdStr = "", qtyStr = "", rateStr = "", ratestdStr = "",
				unitRate = "", unit = "", varValue = "", lsitemCode = "", unitStd = "", convRtuomStduom = "",
				priceList = "", itemCodels = "", typeAllowPorateList = "", typeAllowPorate = "", convQtyStduom = "",
				locCode = "", workOrder = "", packCode = "", acctCodeApadv = "", cctrCodeApadv = "", cctrCodeDr = "",
				cctrCodeCr = "", reqDate = "", dlvDate = "", invAcctPorcp = "", acctCodeCr = "", invAcctQc = "",
				projCodeTemp = "", lineNoTemp = "", disc = "", mqtyBrow = "", rateBrow = "", opReason = "", qcReqd = "",
				itemCodeMfg = "", empCodeQcaprv = "", specRef = "", specreqd = "", qcreqd = "", acctCodeProvDr = "",
				cctrCodeProvDr = "", acctCodeProvCr = "", cctrCodeProvCr = "", prdCodeRfc = "", eou = "", dutyPaid = "",
				formNo = "", lopReqd = "", purcOrderTemp = "", formNoTemp = "", qtyBrow = "", itemCodeTemp = "",
				indNoTemp = "", quantityFcTemp = "", financialCharge="";

		String jobWorkType="",subContractType="";
		String crType = "";
		boolean flagLine = false;
		boolean ordflag = false;
		boolean lbflag = false;
		double lcAmount = 0, advAmt = 0, pordQuantity = 0, quantityStduom = 0, pretAmt = 0, poamount = 0, totAmt1 = 0,
				totAmtProj = 0, exceedAmt = 0, approxcost = 0, lcqty = 0, pordeQtyDb = 0, indentQtylc = 0,
				totalQtyDb = 0, projEstQtyDb = 0, estRate = 0, maxRateDb = 0, preQty = 0, totQty = 0, totQty1 = 0,
				ct3Qty = 0, qtyUsed = 0, totFc1, totFc = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();

		double lc_porcp_amt = 0, lc_unconf_poamount = 0, lc_poamount1 = 0;
		double stdRate = 0, pRate = 0, qtyConvStdUom = 0, convRateuomStduom = 0; // Added by Mahesh Saggam on 04/07/2019

		NodeList parentList4 = null;
		NodeList childList4 = null;
		Node parentNode4 = null;
		Node childNode4 = null;

		Timestamp ordDate2 = null, ordDated2 = null;

		Timestamp reqDateTm = null, ordDateTm = null, dlvDateTm = null;

		String queryStdoum = "";// Added by Anjali R. on[13/03/2018]

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try {
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			DistCommon distComm = new DistCommon();
			FinCommon finCommon = new FinCommon();
			// userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			// loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			java.util.Date currDate = sdf.parse(sdf.format(timestamp).toString());
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1: {
				System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				int cnt1 = 0;
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("ord_date")) {
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));
						siteCodeord = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
						if (ordDate != null && ordDate.trim().length() > 0) {
							sysDate = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");

							// Modified by Anjali R. on [31/07/2018][ System should not allow to enter
							// future date in PO Date][Start]
							Date date = new Date();
							System.out.println("date--["
									+ new SimpleDateFormat(genericUtility.getDBDateTimeFormat()).format(date));

							if (sysDate.after(date)) {
								System.out.println("Error purchase order date greater than sysdate");
								errList.add("VTDATEINVD");
								errFields.add(childNodeName.toLowerCase());
							}
							// Modified by Anjali R. on [31/07/2018][ System should not allow to enter
							// future date in PO Date][End]

							// sdf = new
							// SimpleDateFormat(genericUtility.getApplDateFormat());
							// sysDate=sdf.parse(ordDate);
						}
						System.out.println("sysDate[" + sysDate + "]siteCodeord[" + siteCodeord + "]");
						// errcode =
						// nvo_functions_adv.nf_check_period('PUR',mdate1,mval);
						// Changes and Commented By Ajay on 20-12-2017 :START
						// errcode = ValidatorEJB.nfCheckPeriod("PUR", sysDate,siteCodeord);
						errcode = finCommon.nfCheckPeriod("PUR", sysDate, siteCodeord, conn);
						// Changes and Commented By Ajay on 20-12-2017 :END
						System.out.println("errcode[" + errcode + "]");
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					if (childNodeName.equalsIgnoreCase("contract_no")) {
						System.out.println("Inside wfval contract_no>>>");

						contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
						bomCode = checkNull(genericUtility.getColumnValue("bom_code", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));
						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						if (contractNo.trim().length() > 0) {
							cnt1 = 0;
							sql = "select contract_fromdate,contract_todate from pcontract_hdr where contract_no = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, contractNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								fromDt = rs.getTimestamp("contract_fromdate");
								toDt = rs.getTimestamp("contract_todate");
								cnt1++;

								// Added and commented by sarita to correct if condition as ordDateTm should
								// check with both fromDt & toDt on 17 JULY 18 [START]
								/*
								 * if ((ordDateTm.before(fromDt)) || (ordDateTm.after(fromDt))) { errcode =
								 * "VTVLD";
								 */
								if ((ordDateTm.before(fromDt)) || (ordDateTm.after(toDt))) {
									errcode = "VTPORD0001";
									// Added and commented by sarita to correct if condition as ordDateTm should
									// check with both fromDt & toDt on 17 JULY 18 [END]

									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt1 == 0) {
								errcode = "VTINCONT";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("pord_type")) {
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
						System.out.println("Inside wfval pord_type>>>" + pordType);
						if (pordType == null || pordType.trim().length() == 0) {
							errcode = "VTPOTYBL";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						// if (errcode == null || errcode.trim().length() == 0) {

						sql = "select count(1) as cnt from pordertype where ORDER_TYPE  = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, pordType);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (count == 0) {
							errcode = "VTPOTYPE";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						// }
					}
					if (childNodeName.equalsIgnoreCase("purc_order")) {
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						System.out.println("Inside wfval purcOrder>>>" + purcOrder);
						String keyFlag = "";

						sql = "select key_flag from transetup where tran_window='w_porder' ";
						pStmt = conn.prepareStatement(sql);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							keyFlag = rs.getString("key_flag");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						System.out.println("Key Flag>>>>>>>>>" + keyFlag);
						if (keyFlag.equalsIgnoreCase("M")) {
							System.out.println("purcOrder[" + purcOrder + "]");
							if (purcOrder == null || purcOrder.length() == 0) {
								errList.add("VTPURCEMP");
								errFields.add(childNodeName.toLowerCase());
							}
							System.out.println("Edit Flag>>>>" + editFlag);
							if ("A".equalsIgnoreCase(editFlag)) {
								sql = " SELECT COUNT(1) FROM porder " + "WHERE purc_order = ?  ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, purcOrder);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt > 0) {
									errList.add("INVPONOES");
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("supp_code")) {
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						siteCodeord = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
						System.out.println("Inside wfval supp_code>>>" + suppCode + "siteCodeord" + siteCodeord);
						errcode = FinCommon.isSupplier(siteCodeord, suppCode, "P-ORD", conn);

						System.out.println("Inside wfval supp_code errcode>>> " + errcode);
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("site_code__dlv")) {
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));

						errcode = isSiteCode(siteCodeord, "P-ORD"); // Changed By PriyankaC on 04Jan18
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("site_code__ord")) {
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));

						errcode = isSiteCode(siteCodeord, "P-ORD"); // Changed By PriyankaC on 04Jan18
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("site_code__bill")) {
						siteCodeBill = checkNull(genericUtility.getColumnValue("site_code__bill", dom));

						if (siteCodeBill.length() > 0) {
							errcode = isSiteCode(siteCodeBill, "P-ORD");// Changed By PriyankaC on 04Jan18
							System.out.println("@@@@@@308 errcode[" + errcode + "]");
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							errcode = "VMSITECD1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("dept_code")) {
						deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));
						cnt = 0;
						sql = "SELECT COUNT(1) FROM DEPARTMENT WHERE DEPT_CODE = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, deptCode);
						rs = pStmt.executeQuery();

						if (rs.next()) {
							cnt = rs.getInt(1);
						}

						if (cnt == 0) {
							errcode = "VTDEPT1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}
					if (childNodeName.equalsIgnoreCase("emp_code")) {
						empCode = checkNull(genericUtility.getColumnValue("emp_code", dom));
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));

						errcode = FinCommon.isEmployee(siteCodeOrd, empCode, "P-ORD", conn);
						System.out.println("errcode errcode" + errcode);
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						cnt1 = 0;
						if (errcode != null && errcode.trim().length() == 0) {
							sql = "select relieve_date from employee where emp_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, empCode);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								relieveDate = rs.getString("relieve_date");
								cnt1++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt1 == 0) {
								errcode = "VMEMP1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (relieveDate == null) {
								errcode = "VMEMP2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

						}

					}
					if (childNodeName.equalsIgnoreCase("term_table")) {
						cnt1 = 0;
						termTable = checkNull(genericUtility.getColumnValue("term_table", dom));

						sql = "select Count(*) from pur_term_table where term_table = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, termTable);
						rs = pStmt.executeQuery();

						if (rs.next()) {
							cnt = rs.getInt(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (cnt == 0) {
							errcode = "VTTERM";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("tran_code")) {
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						// Pavan R on 28aug18 [to validate blank transporter].
						if (tranCode == null || tranCode.trim().length() == 0) {
							errcode = "VMTRANCD1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());

						} else if (tranCode.length() > 0) {
							sql = "Select Count(*) from transporter where tran_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, tranCode);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VMTRAN1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("item_ser")) {
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						// errcode = DistCommon.getItemSer(itemCode,
						// siteCodeOrd,timestamp, "", "", conn);
						int cnt5 = isItemSer("itemser", "item_ser", itemSer, conn);
						// errcode = nvo_dis.gbf_itemser(itemSer,transer);
						if (cnt5 == 0) {
							errcode = "VTITEMSER1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					if (childNodeName.equalsIgnoreCase("cr_term")) {  //change by mukesh on 17/09/2020 Start
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));

						sql = "Select cr_type from crterm where cr_term = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, crTerm);
						rs = pStmt.executeQuery();

						if (rs.next()) {
							//cnt = rs.getInt(1);
							crType = rs.getString("cr_type");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						/*if (cnt == 0) {                //commented by mukesh
									errcode = "VTCRTERM1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
                                }*/
						if (!"P".equalsIgnoreCase(crType)) 
						{
							errcode = "VMCRTEMPO";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

					}//END
					if (childNodeName.equalsIgnoreCase("curr_code")) {
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));

						if (currCode == null || currCode.trim().length() == 0) {
							errcode = "VTCURRCD1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							sql = "Select Count(*) from currency where curr_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, currCode);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTCURRCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
								sql = "Select curr_code from supplier where supp_code = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, suppCode);
								rs = pStmt.executeQuery();

								if (rs.next()) {
									msuppcurr = rs.getString("curr_code");

								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
								System.out.println("@@@@@@ currCode[" + currCode + "]msuppcurr[" + msuppcurr + "]");
								if (!currCode.trim().equalsIgnoreCase(msuppcurr.trim())) {
									errcode = "VTCURR2";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));

						if (taxChap.trim().length() > 0) {
							sql = "Select Count(*) from taxchap where tax_chap = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxChap);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTTAXCHAP1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));

						if (taxClass.trim().length() > 0) {
							sql = "Select Count(*) from taxclass where tax_class = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxClass);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTTAXCLA1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_env")) {
						taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));
						if (taxEnv.trim().length() > 0) {
							sql = "Select Count(*) from taxenv where tax_env = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxEnv);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTTAXENV1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								// Pavan R 17sept19 start[to validate tax environment]
								/*
								 * Date parsedDate = sdf.parse(ordDate); timestamp = new java.sql.Timestamp(
								 * parsedDate.getTime());
								 */
								/*
								 * errcode = DistCommon.getCheckTaxEnvStatus( taxEnv, timestamp, conn);
								 */
								if (ordDate != null && ordDate.trim().length() > 0) {
									sysDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDate,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}
								errcode = DistCommon.getCheckTaxEnvStatus(taxEnv, sysDate, "P", conn);
								// Pavan R 17sept19 end[to validate tax environment]
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("proj_code")) {
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));

						if (projCode == null || projCode.trim().length() == 0) {
							if ("P".equalsIgnoreCase(pordType.trim())) {
								errcode = "VTPROVI";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							sql = "select case when proj_status is null then 'X' else proj_status end,ind_no from project where proj_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								status = rs.getString(1);
								projIndno = rs.getString(2);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (!"A".equalsIgnoreCase(status)) {
								errcode = "VTPROJ2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("P".equalsIgnoreCase(pordType.trim())) {
								if (!indNo.trim().equalsIgnoreCase(projIndno.trim())) {
									errcode = "VINDPJMM";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("quot_no")) {
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));

						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						cnt1 = 0;
						if (quotNo.length() > 0) {
							sql = "select status,valid_upto from pquot_hdr where quot_no = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, quotNo);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								status = rs.getString(1);
								toDt = rs.getTimestamp(2);
								cnt1++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							System.out.println("toDt: "+toDt+" ordDateTm: "+ordDateTm);
							if (cnt1 == 0) {
								errcode = "VTPQUOT2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("U".equalsIgnoreCase(status)) {
								errcode = "VTPQUOT1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}// else if (ordDateTm.before(toDt)) {//Commented by Anagha R on 10/08/2020 for Condition to be changed (Shreeji Woodcraft)
							else if (toDt.before(ordDateTm)) { //Added by Anagha R on 10/08/2020 for Condition to be changed (Shreeji Woodcraft)
								errcode = "VTQDTNV";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("sales_pers")) {
						salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));

						if (salesPers.length() > 0) {
							errcode = FinCommon.isSalesPerson(siteCodeOrd, salesPers, "P-ORD", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							// nvo_dis.gbf_sperson(mval1,mval,transer)
						}
					}
					if (childNodeName.equalsIgnoreCase("dlv_term")) {
						dlvTerm = checkNull(genericUtility.getColumnValue("dlv_term", dom));

						if (dlvTerm != null && dlvTerm.trim().length() > 0) {
							sql = "select count(*) from delivery_term where dlv_term = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, dlvTerm);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTDLV1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							errcode = "VMDLVTERM2";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("curr_code__frt")) {
						currCodefrt = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));
						frtAmt = checkNull(genericUtility.getColumnValue("frt_amt", dom));

						if (frtAmt != null && frtAmt.trim().length() > 0) {
							sql = "select count(*) from currency where curr_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, currCodefrt);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTCURRCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("curr_code__ins")) {
						insuranceAmt = checkNull(genericUtility.getColumnValue("insurance_amt", dom));
						currCodeins = checkNull(genericUtility.getColumnValue("curr_code__ins", dom));

						if ((insuranceAmt != null && insuranceAmt.trim().length() > 0
								&& Double.parseDouble(insuranceAmt) > 0)
								|| (currCodeins != null && currCodeins.trim().length() > 0)) {
							sql = "select count(*) from currency where curr_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, currCodeins);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTCURRCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("sale_order")) {
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));

						if (saleOrder.trim().length() > 0) {
							cnt1 = 0;
							sql = "select confirmed,status from sorder where sale_order = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, saleOrder);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								conf = rs.getString(1);
								status = rs.getString(1);
								cnt1++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt1 == 0) {
								errcode = "VTSORD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("N".equalsIgnoreCase(conf)) {
								errcode = "VTSORD2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("C".equalsIgnoreCase(status)) {
								errcode = "VTSORDCX";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("ind_no")) {
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
						tranIdBoq = checkNull(genericUtility.getColumnValue("TRAN_ID__BOQ", dom));

						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						sql = "select confirmed , status from boqhdr where TRAN_ID = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, tranIdBoq);
						rs = pStmt.executeQuery();

						if (rs.next()) {
							conf = rs.getString(1);
							status = rs.getString(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ("Y".equalsIgnoreCase(conf) && "X".equalsIgnoreCase(status)) {
							errcode = "VTBOQSTAT";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						if (indNo.length() > 0 && indNo.trim().length() == 0) {
							errcode = "VTINDINV1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (indNo.length() > 0) {
							cnt = 0;
							sql = "select item_code, status,quantity,ord_qty,site_code__bil,ind_date from indent where ind_no = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, indNo);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								itemCode = rs.getString(1);
								status = rs.getString(2);
								qty = rs.getString(3);
								indDate = rs.getTimestamp(6);
								cnt++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								// errcode = "VTINDENT1"

								errcode = "VTINDENT1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());

							}
							if ("U".equalsIgnoreCase(status)) {
								errcode = "VTINDENT3";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("X".equalsIgnoreCase(status)) {
								errcode = "VTINDENT6";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("L".equalsIgnoreCase(status)) {
								proviTranid = checkNull(genericUtility.getColumnValue("provi_tran_id", dom, "1"));

								if (!"P".equalsIgnoreCase(pordType)) {
									if (proviTranid.length() == 0 || proviTranid == null) {
										errcode = "VTINDENT4";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								} else if ("C".equalsIgnoreCase(status)) {
									errcode = "VTINDCL";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								} // else if (ordDateTm.after(indDate)) {
								else if (ordDateTm.before(indDate)) {
									errcode = "VTPOINDT";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}

								// if (errcode.length() == 0) {

								if ("P".equalsIgnoreCase(pordType)) {
									sql = "select	count(*) from	porder where pord_type = 'P' and ind_no = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, indNo);
									rs = pStmt.executeQuery();

									if (rs.next()) {
										cnt = rs.getInt(1);

									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;
									if (cnt == 0) {
										errcode = "VTDUPINDPP";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

								// }
							}
							// if (errcode.length() == 0) {

							quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));

							if (quotNo.length() > 0) {
								sql = "select enq_no from pquot_hdr where quot_no = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, quotNo);
								rs = pStmt.executeQuery();

								if (rs.next()) {
									enqNo = rs.getString(1);

								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (enqNo.length() > 0) {
									sql = "select status from enq_det where enq_no = ? and ind_no = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, enqNo);
									pStmt.setString(2, indNo);
									rs = pStmt.executeQuery();

									if (rs.next()) {
										status = rs.getString(1);

									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;

									if (status == null) {
										errcode = "VTINDENT5";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

							}

							// }

							// if (errcode.trim().length() == 0) {

							itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));

							sql = "select item_ser from item where item_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								itemSeries = rs.getString(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (!itemSer.equalsIgnoreCase(itemSeries)) {
								errcode = "VTITEMSER";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

							// }
						}
						// if (errcode == null || errcode.trim().length() == 0) {

						sql = "select indent_opt from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();

						if (rs.next()) {
							mindOpt = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ("M".equalsIgnoreCase(mindOpt)) {
							if (indNo == null || indNo.trim().length() == 0) {
								errcode = "VTINDNO";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else if ("N".equalsIgnoreCase(mindOpt)) {
							if (indNo.trim().length() > 0) {
								errcode = "VTINDNO";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// }
					}
					if (childNodeName.equalsIgnoreCase("accept_criteria")) {
						acceptCriteria = checkNull(genericUtility.getColumnValue("accept_criteria", dom));

						if (acceptCriteria.trim().length() == 0 || acceptCriteria == null) {
							// errcode = "VTNULCRT" +
							// "Accept Criteria must be entered. " ;
							errcode = "VTNULCRT";// +
							// "Accept Criteria must be entered. "
							// ;
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("policy_no")) {
						policyNo = checkNull(genericUtility.getColumnValue("policy_no", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));

						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if (policyNo != null && policyNo.trim().length() > 0) {
							sql = "select count(*) from insurance where  policy_no = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, policyNo);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTPOLI1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

							// if (errcode == null || errcode.trim().length() == 0) {

							sql = "select status, from_date , valid_upto from insurance where policy_no = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, policyNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								status = rs.getString("status");
								fmDate = rs.getTimestamp("from_date");
								vUpto = rs.getTimestamp("valid_upto");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if ("C".equalsIgnoreCase(status) || "X".equalsIgnoreCase(status)) {
								errcode = "VTCX";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (fmDate.after(ordDateTm) || vUpto.before(ordDateTm)) {
								errcode = "VTCX";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

							// }
						}
					}
					if (childNodeName.equalsIgnoreCase("bank_code__pay")) {
						bankCodepay = checkNull(genericUtility.getColumnValue("bank_code__pay", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));

						if (bankCodepay != null && bankCodepay.trim().length() > 0) {
							sql = "select pay_mode from supplier where supp_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, suppCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								payMode = rs.getString("pay_mode");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (payMode == null || payMode.trim().length() == 0 || !"E".equalsIgnoreCase(payMode)) {
								errcode = "VTBENBCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								cnt1 = 0;
								sql = "select case when confirmed is null then 'N' else confirmed end,case when active_yn is null then 'Y' else active_yn end"
										+ " from supplier_bank where supp_code = ? and and	bank_code__ben = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, suppCode);
								pStmt.setString(2, bankCodepay);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									conf = rs.getString(1);
									active = rs.getString(2);
									cnt1++;
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt1 == 0) {
									errcode = "VTBENBCD2";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								} else if ("N".equalsIgnoreCase(active)) {
									errcode = "VTBENBCD4";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								} else if ("N".equalsIgnoreCase(conf)) {
									errcode = "VTBENBCD5";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("loc_group__jwiss")) {
						locGroupjwiss = checkNull(genericUtility.getColumnValue("loc_group__jwiss", dom));
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));

						if (locGroupjwiss.trim().length() > 0 && locGroupjwiss != null) {
							sql = "select count(*) from pordertype where loc_group__jwiss = ? and ORDER_TYPE = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, locGroupjwiss);
							pStmt.setString(2, pordType);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTPOLOCGRP";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("task_code")) {
						taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));

						if (taskCode != null && taskCode.trim().length() > 0) {
							sql = "select count(*) from proj_est_milestone where task_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taskCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTASK1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("curr_code__comm")) {
						String currCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm", dom));

						/*
						 * if (currCode == null || currCode.trim().length() == 0) { errcode =
						 * "VTCURRCD1"; errList.add(errcode);
						 * errFields.add(childNodeName.toLowerCase()); } else
						 */
						if (currCodeComm != null && currCodeComm.trim().length() > 0) {
							sql = "Select Count(1) from currency where curr_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, currCodeComm);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTCURRCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							/*
							 * else { suppCode = checkNull(genericUtility .getColumnValue("supp_code",
							 * dom)); sql = "Select curr_code from supplier where supp_code = ?" ; pStmt =
							 * conn.prepareStatement(sql); pStmt.setString(1, suppCode); rs =
							 * pStmt.executeQuery();
							 * 
							 * if (rs.next()) { msuppcurr = rs.getString("curr_code");
							 * 
							 * } rs.close(); rs = null; pStmt.close(); pStmt = null;
							 * System.out.println("@@@@@@ currCode[" + currCode + "]msuppcurr[" + msuppcurr
							 * + "]"); if (!currCode.trim().equalsIgnoreCase( msuppcurr.trim())) { errcode =
							 * "VTCURR2"; errList.add(errcode); errFields.add(childNodeName.toLowerCase());
							 * } }
							 */
						}
					}

					if (childNodeName.equalsIgnoreCase("frt_term")) {
						String frtTerm = checkNull(genericUtility.getColumnValue("frt_term", dom));
						cnt = 0;
						if (frtTerm != null && frtTerm.trim().length() > 0) {
							sql = " select count(1) from gencodes where fld_name = 'FRT_TERM' and fld_value = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, frtTerm);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "UV_FRTERM";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
			}
			break;

			case 2: {
				System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				// Variables added by Mahesh Saggam on 24-June-2019 [Start]
				NodeList itemNodeList = null, lineNoList = null, detail2List = null, childDetilList = null;
				Node itemNode = null, lineNoNode = null, detailNode = null, chidDetailNode = null;
				String uniqueItem = "", itemCode1 = "", pOrderType = "";
				// Variables added by Mahesh Saggam on 24-June-2019 [End]
				String lineValue = "", updateFlag = "";
				int lineNoInt = 0, lineValueInt = 0;
				int cnt1 = 0;
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("site_code")) {
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom1));

						if (!siteCode.equalsIgnoreCase(siteCodeDlv)) {
							// if (errcode == null || errcode.trim().length() == 0) {
							// errcode = nvo_dis.gbf_site(mval,transer)
							errcode = isSiteCode(siteCode, "P-ORD"); // Changed By PriyankaC on 04Jan18
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							// }
						}
					}
					if (childNodeName.equalsIgnoreCase("ind_no")) {
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						siteCodeBill = checkNull(genericUtility.getColumnValue("site_code__bill", dom1));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if (indNo != null && indNo.length() > 0 && indNo.trim().length() == 0) {
							errcode = "VTINDINV1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (indNo != null && indNo.trim().length() > 0) {
							String itemCodeInd = "";
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							qty = checkNull(genericUtility.getColumnValue("quantity", dom));
							int cnt9 = 0;
							sql = "select item_code, status,quantity,ord_qty,site_code__dlv,"
									+ "site_code__bil,ind_date from indent where ind_no = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, indNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								itemCodeInd = rs.getString(1);
								status = rs.getString(2);
								siteCodeDlv = rs.getString(5);
								siteCodeBillPo = rs.getString(6);
								indDate = rs.getTimestamp(7);
								cnt9++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							System.out.println("@@@@1491 itemCode[" + itemCode + "]itemCodeInd[" + itemCodeInd + "]");
							if (cnt9 == 0) {
								errcode = "VTINDENT1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} // else if (!itemCode.equalsIgnoreCase(itemCodeInd)) { commented and added trim
							// by Varsha V on 25-05-18 for avoiding missmatch in item code
							else if (!itemCode.trim().equalsIgnoreCase(itemCodeInd.trim())) {
								errcode = "VTINDENT2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("U".equalsIgnoreCase(status)) {
								errcode = "VTINDENT3";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("L".equalsIgnoreCase(status)) {
								errcode = "VTINDENT4";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("C".equalsIgnoreCase(status)) {
								errcode = "VTINDCL";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("X".equalsIgnoreCase(status)) {
								errcode = "VTINDENT6";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (ordDateTm.before(indDate)) {
								errcode = "VTPOINDT";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!siteCodeBill.trim().equalsIgnoreCase(siteCodeBillPo.trim())) {
								errcode = "VTINDBILL";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!siteCodeDlv.trim().equalsIgnoreCase(siteCode.trim())) {
								errcode = "VTINDSITE";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

							// }
							// if (errcode.length() == 0) {

							quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom1));

							if (quotNo.length() > 0) {
								sql = "select enq_no from pquot_hdr where quot_no = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, itemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									enqNo = rs.getString(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (enqNo.length() > 0) {
									sql = "select status from enq_det where enq_no = ? and ind_no = ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, enqNo);
									pStmt.setString(2, indNo);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										status = rs.getString(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;

									if ("0".equalsIgnoreCase(status)) {
										errcode = "VTINDENT5";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}

							// }
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));
							sql = "select quantity__stduom from indent where ind_no = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, indNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								quantityStduom = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							sql = "select sum(a.quantity__stduom) from porddet a , porder b where a.purc_order = b.purc_order and "
									+ " a.purc_order <> ? and  b.pord_type <> 'P' and  b.status <> 'X' and b.status <> 'C' and  a.ind_no = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, purcOrder);
							pStmt.setString(2, indNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								pordQuantity = rs.getDouble(1);
							}

							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							System.out.println("@@@@@1374 pordQuantity[" + pordQuantity + "] >= quantityStduom["
									+ quantityStduom + "]");
							if (pordQuantity >= quantityStduom) {
								errcode = "VTQPO";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("proj_code")) {
						projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						exchRate = checkNull(genericUtility.getColumnValue("exch_rate", dom));
						qtyStduom = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));
						rateStduom = checkNull(genericUtility.getColumnValue("rate__stduom", dom));
						discount = checkNull(genericUtility.getColumnValue("discount", dom));
						taxAmt = genericUtility.getColumnValue("tax_amt", dom);

						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDate2 = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if (purcOrder == null || purcOrder.trim().length() == 0) {
							purcOrder = "@@@";
						}

						if (discount == null) {
							discount = "0";
						}

						if (taxAmt == null) {
							taxAmt = "0";
						}

						if (projCode != null && projCode.trim().length() > 0) {
							cnt1 = 0;
							sql = "select case when proj_status is null then 'X' else proj_status end ,ind_no from project where proj_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								status = rs.getString(1);
								projIndno = rs.getString(2) == null || rs.getString(2).trim().length() == 0 ? ""
										: rs.getString(2).trim();
								cnt1++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt1 == 0) {
								errcode = "VTPROJ1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!"A".equalsIgnoreCase(status)) {
								errcode = "VTPROJ2";
							} else if (indNo != null && indNo.trim().length() > 0) {
								System.out.println("indNo[" + indNo + "]projIndno[" + projIndno + "]");
								indNo = indNo.trim();
								if (!indNo.equalsIgnoreCase(projIndno)) {
									errcode = "VINDPJMM";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

						// if (errcode == null || errcode.trim().length() == 0) {

						typeAllowProjbudgtList = distComm.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn);

						if ("NULLFOUND".equalsIgnoreCase(typeAllowProjbudgtList)) {
							typeAllowProjbudgtList = "";
						}

						sql = "select approx_cost from   project where  proj_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, projCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							approxcost = rs.getDouble("approx_cost");

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						ordflag = false;
						/*
						 * do { typeAllowProjbudgtBudgt = distComm.getToken( typeAllowProjbudgtList,
						 * ",");
						 * 
						 * if (pordType!=null && pordType.trim() == typeAllowProjbudgtBudgt .trim()) {
						 * ordflag = true; }
						 * 
						 * } while (typeAllowProjbudgtList.trim().length() > 0);
						 */

						System.out.println("@@@@@@@ typeAllowProjbudgtList[" + typeAllowProjbudgtList + "]");
						String typeAllowProjbudgtListArray[] = typeAllowProjbudgtList.split(",");
						System.out.println(
								"@@@@@ typeAllowProjbudgtListArray.length[" + typeAllowProjbudgtListArray.length + "]");
						for (int k = 0; k < typeAllowProjbudgtListArray.length; k++) {
							if (pordType != null
									&& pordType.trim().equalsIgnoreCase(typeAllowProjbudgtListArray[k].trim())) {
								ordflag = true;
							}
						}

						// }

						if (ordflag == true) {
							if (projCode == null || projCode.trim().length() == 0) {
								errcode = "VEPRJ1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							// if (errcode == null || errcode.trim().length() == 0) {

							sql = "select sum(a.tot_amt * b.exch_rate) from   porddet a, porder b where  ( a.purc_order = b.purc_order )"
									+ "and  b.confirmed = 'Y'  and a.proj_code = ? and a.purc_order <> ? and b.status <> 'X' and a.status <> 'C' ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							pStmt.setString(2, purcOrder);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								lc_poamount1 = rs.getDouble(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							sql = "select sum(a.net_amt * b.exch_rate) from  porcpdet a, porcp b ,porddet c where  ( a.purc_order = c.purc_order )"
									+ "and (a.tran_id = b.tran_id ) and 	a.line_no__ord = c.line_no and b.confirmed = 'Y'  and c.proj_code = ?"
									+ "and a.purc_order <> ? and b.status <> 'X' and c.status = 'C' and b.tran_ser = 'P-RCP'";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							pStmt.setString(2, purcOrder);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								lc_porcp_amt = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							sql = "select sum(a.net_amt * b.exch_rate ) from   porcpdet a, porcp b ,porddet c where ( a.purc_order = c.purc_order )"
									+ "and (a.tran_id = b.tran_id ) and  b.confirmed = 'Y'  and a.proj_code = ?  and b.status <> 'X' and a.status <> 'C' and b.tran_ser = 'P-RET'";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								pretAmt = rs.getDouble(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							sql = " select sum(a.tot_amt *  b.exch_rate) " +
									// " into :lc_unconf_poamount " +
									" from porddet a, porder b " + " where ( a.purc_order = b.purc_order ) "
									+ " and b.confirmed = 'N' " + " and a.proj_code = ? "
									+ " and a.purc_order <> ?    ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							pStmt.setString(2, purcOrder);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								lc_unconf_poamount = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							poamount = lc_poamount1 + lc_unconf_poamount + lc_porcp_amt - pretAmt;

							System.out.println("@@@@@@1556 poamount[" + poamount + "]");

							parentNodeList1 = dom2.getElementsByTagName("Detail2");
							parentNode1 = parentNodeList1.item(0);
							childNodeList1 = parentNode1.getChildNodes();
							childNodeListLength1 = childNodeList1.getLength();
							for (ctr1 = 0; ctr1 < childNodeListLength1; ctr1++) {
								childNode1 = childNodeList1.item(ctr1);
								childNodeName1 = childNode1.getNodeName();
								System.out.println("childNodeName[" + childNodeName1 + "]");
								if (childNodeName1.equalsIgnoreCase("proj_code")) {
									projCodeTemp = checkNull(genericUtility.getColumnValue("proj_code", dom2)).trim();
									lineNoTemp = checkNull(genericUtility.getColumnValue("line_no", dom2)).trim();

									if (projCodeTemp.equalsIgnoreCase(projCode.trim())
											&& !lineNoTemp.equalsIgnoreCase(lineNo)) {
										mqtyBrow = checkNull(genericUtility.getColumnValue("quantity__stduom", dom2))
												.trim();

										rateBrow = checkNull(genericUtility.getColumnValue("rate__stduom", dom2))
												.trim();
										//Change by Varsha V on 08-06-2020
										//disc = checkNull(genericUtility.getColumnValue("discount", dom2)).trim();
										disc = checkInt(genericUtility.getColumnValue("discount", dom2)).trim();
										//taxAmt = checkNull(genericUtility.getColumnValue("tax_amt", dom2)).trim();
										taxAmt = checkInt(genericUtility.getColumnValue("tax_amt", dom2)).trim();

										if (disc == null) {
											disc = "0";
										}
										if (taxAmt == null) {
											taxAmt = "0";
										}

										totAmt1 = totAmt1
												+ ((Double.parseDouble(mqtyBrow) * Double.parseDouble(rateBrow))
														- ((Double.parseDouble(mqtyBrow) * Double.parseDouble(rateBrow)
																* Double.parseDouble(disc))) / 100)
												+ Double.parseDouble(totAmt);
									}
								}
								totAmt1 = totAmt1 + (((Double.parseDouble(qtyStduom) * Double.parseDouble(rateStduom))
										- ((Double.parseDouble(qtyStduom) * Double.parseDouble(rateStduom)
												* Double.parseDouble(discount)) / 100))
										+ Double.parseDouble(taxAmt));
								totAmtProj = (poamount + totAmt1);
								exceedAmt = (totAmtProj - approxcost);

								System.out.println(
										"@@@@@1635 totAmtProj[" + totAmtProj + "] > approxcost[" + approxcost + "]");

								if (totAmtProj > approxcost) {
									errcode = "VTPROJCOST";
									/*
									 * + " Exceeded Project Code: " + projCode + " Project Approved Amount: " +
									 * approxcost + " Consumed Amount: " + poamount + " Current Porder Amount : " +
									 * totAmt1 + " Exceeded Amount: " + exceedAmt;
									 */
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

							// }
							// if (errcode == null || errcode.trim().length() == 0) {

							sql = "select count(*) from project where proj_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								projCnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (projCnt == 0) {
								errcode = "VTPROJ1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							// if (errcode == null || errcode.trim().length() == 0) {

							sql = "select count(*) from project where proj_code = ? "
									+ " and ( ? between start_date and end_date "
									+ " or ? between start_date and ext_end_date )";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, projCode);
							pStmt.setTimestamp(2, ordDate2);
							pStmt.setTimestamp(3, ordDate2);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTINVORDT";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

							// }

							// }

							else if (ordflag = false) {
								if (projCode != null && projCode.trim().length() > 0) {
									sql = "select proj_type from project where proj_code =  ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, projCode);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										projType = rs.getString("proj_type");

									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;
								} else {

									projectTypeOpt = distComm.getDisparams("999999", "PROJECT_TYPE_OPT", conn);

									if ("NULLFOUND".equalsIgnoreCase(projectTypeOpt)) {
										projectTypeOpt = "";
									}

									/*
									 * do { // ls_proj_type_opt_list = // f_get_token(ls_proj_type_opt,',')
									 * projectTypeOptList = DistCommon .getToken(projectTypeOpt, ",");
									 * 
									 * if (projType.trim() == projectTypeOptList .trim()) { errcode = "VTINVPROJ2";
									 * errList.add(errcode); errFields.add(childNodeName .toLowerCase()); }
									 * 
									 * } while (projectTypeOpt.trim().length() > 0);
									 */

									System.out.println("@@@@@@@ projectTypeOpt[" + projectTypeOpt + "]");
									String projectTypeOptArray[] = projectTypeOpt.split(",");
									System.out.println(
											"@@@@@ projectTypeOptArray.length[" + projectTypeOptArray.length + "]");
									for (int k = 0; k < projectTypeOptArray.length; k++) {
										if (pordType != null
												&& pordType.trim().equalsIgnoreCase(projectTypeOptArray[k].trim())) {
											errcode = "VTINVPROJ2";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}

								}

							}
						}
					}
					if (childNodeName.equalsIgnoreCase("item_code")) {
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));

						sql = "Select count(1) from item where item_code =  ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (cnt == 0) {
							errcode = "VTITMCNM";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						sql = "Select case when stop_business is null then 'N' else stop_business end from Item where item_code =  ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							stopBusi = rs.getString(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ("N".equalsIgnoreCase(stopBusi)) {
							errcode = isItem(siteCode, itemCode, "P-ORD", conn);
							// errcode = nvo_dis.gbf_item(mval1,mval,transer)
							System.out.println("@@@@@1769 errcode[" + errcode + "]");
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							errcode = "VTIIC";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						// if (errcode == null || errcode.trim().length() == 0) {
						cnt1 = 0;
						sql = "Select channel_partner,site_code__ch from site_supplier where site_code =  ? and supp_code = ?  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, siteCode);
						pStmt.setString(2, suppCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							cp = rs.getString("channel_partner");
							cnt1++;
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (cnt == 0 || cp == null) {
							sql = "select channel_partner,site_code from supplier where supp_code = ?  ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, suppCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cp = rs.getString("channel_partner");
								siteCode = rs.getString("site_code");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}

						// if (errcode == null || errcode.trim().length() == 0) {
						if (cp != null && "Y".equalsIgnoreCase(cp.trim().toUpperCase())) {
							mval1 = cp;
						}

						// mval3 =
						// gf_get_item_ser(mval,mval1,mdate1,msupp,'S');
						Date parsedDate = sdf.parse(ordDate);
						timestamp = new java.sql.Timestamp(parsedDate.getTime());
						mval3 = DistCommon.getItemSer(itemCode, siteCode, timestamp, suppCode, "S", conn);
						mval3 = checkNull(mval3);// added by nandkumar gadkari on 06/09/19
						System.out.println("@@@@@1828 mval3[" + mval3 + "]");
						/*
						 * if( mval3 != null && mval3.trim().length() > 0 ) { errcode =
						 * DistCommon.getToken(mval3, " ");
						 * 
						 * }
						 */
						// }

						// if (errcode.length() == 0) {
						sql = "select oth_series from itemser where item_ser = ?  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemSer);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							status = rs.getString("oth_series");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (!itemSer.trim().equalsIgnoreCase(mval3.trim())
								&& "N".equalsIgnoreCase(status.toUpperCase())) {
							errcode = "VTITEM2";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!itemSer.trim().equalsIgnoreCase(mval3.trim())
								&& "G".equalsIgnoreCase(status.toUpperCase())) {
							sql = "Select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) from "
									+ "itemser where item_ser =  ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, mval1);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								itemSerCrpolicy = rs.getString(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (!itemSerCrpolicy.trim().equalsIgnoreCase(itemSerCrpolicy.trim())) {
								// errcode =
								// "VTITEM2"+"Item Does Not Belong To Group"
								// ;
								errcode = "VTITEM2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// }
						// if (errcode.length() == 0) {
						//quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom1));

						sql = "select quot_opt from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							quotOpt = rs.getString("quot_opt");

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ("M".equalsIgnoreCase(quotOpt)) {
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));

							sql = "select  case when ind_val_to_raise_enq is null then 0 else ind_val_to_raise_enq end from item where item_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						// }
						// if (errcode.length() == 0) {
						//quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom1));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));

						if (quotNo.trim() != null && quotNo.trim().length() > 0) {
							cnt1 = 0;
							sql = "select status from pquot_det where quot_no = ? and item_code = ? and status = 'A' ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, quotNo);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								status = rs.getString("status");
								cnt1++;
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt1 == 0) {
								errcode = "VTNOTITMQT";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// }
						// if (errcode == null || errcode.length() == 0) {
						proviTranid = checkNull(genericUtility.getColumnValue("provi_tran_id", dom));

						if (proviTranid.trim() != null && proviTranid.trim().length() > 0) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							cnt = 0;
							sql = "select  count(*) from porder a,porddet b where a.purc_order = b.purc_order and a.provi_tran_id = ?"
									+ "and a.purc_order <> ? and    b.item_code = ?    ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, proviTranid);
							pStmt.setString(2, proviTranid);
							pStmt.setString(3, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt > 0) {
								// errcode =
								// "VTSTDPOCR~tStandard PO already created for item "+
								// itemCode ;
								errcode = "VTSTDPOCR";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// }
						// if (errcode.length() == 0 || errcode == null) {
						sql = "select indent_opt from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mindOpt = rs.getString("indent_opt");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ("M".equalsIgnoreCase(mindOpt)) {
							if (indNo == null || indNo.trim().length() == 0) {
								errcode = "VTINDNO";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else if ("N".equalsIgnoreCase(mindOpt)) {
							if (indNo.trim().length() > 0) {
								errcode = "VTINDRQ";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// }

						// if (errcode.length() == 0 || errcode == null) {
						sql = "select quot_opt from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							quotOpt = rs.getString("quot_opt");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						// if (errcode == null || errcode.trim().length() == 0) {
						if ("M".equalsIgnoreCase(quotOpt)) {
							if (quotNo == null || quotNo.trim().length() == 0) {
								errcode = "VTQUOTM";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// }

						// }
						// if (errcode.length() == 0 || errcode == null) {
						taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));
						if (taskCode != null && taskCode.trim().length() > 0) {
							cnt = 0;
							sql = "select  count(*) from proj_est_bsl_item where task_code = ? and    item_code = ?    ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taskCode);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTASK4";
							}
						}
						// }
						// }

						// Added by Mahesh Saggam on 24-June-2019 [Start]
						detail2List = dom2.getElementsByTagName("Detail2");

						itemCode1 = genericUtility.getColumnValue("item_code", dom);
						pOrderType = checkNull(genericUtility.getColumnValue("pord_type", dom1));
						lineNo = genericUtility.getColumnValue("line_no", dom);

						sql = "select unique_item from pordertype where order_type = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, pOrderType);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							uniqueItem = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						uniqueItem = uniqueItem == null ? "N" : uniqueItem.trim();
						System.out.println("Unique item = " + uniqueItem);
						System.out.println("Detail 2 List " + detail2List.getLength());

						if (lineNo != null && lineNo.trim().length() > 0) {
							lineNoInt = Integer.parseInt(lineNo.trim());
						}

						if ("Y".equalsIgnoreCase(uniqueItem)) {
							for (int t = 0; t < detail2List.getLength(); t++) {
								detailNode = detail2List.item(t);
								childDetilList = detailNode.getChildNodes();
								for (int p = 0; p < childDetilList.getLength(); p++) {
									chidDetailNode = childDetilList.item(p);
									System.out.println("current child node>>>>>>>>>> " + chidDetailNode.getNodeName());

									if (chidDetailNode.getNodeName().equalsIgnoreCase("line_no")) {
										System.out.println("line node found >>>>>" + chidDetailNode.getNodeName());
										if (chidDetailNode.getFirstChild() != null) {
											lineValue = chidDetailNode.getFirstChild().getNodeValue();
											if (lineValue != null && lineValue.trim().length() > 0) {
												lineValueInt = Integer.parseInt(lineValue.trim());
											}
										}
									}

									if (chidDetailNode.getNodeName().equalsIgnoreCase("attribute")) {
										System.out.println("operation node found >>>>>" + chidDetailNode.getNodeName());
										updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag")
												.getNodeValue();
										System.out.println("Update flag is......." + updateFlag);

									}

									if (chidDetailNode.getNodeName().equalsIgnoreCase("item_code")) {
										if (chidDetailNode.getFirstChild() != null) {
											itemCode = chidDetailNode.getFirstChild().getNodeValue();
											if (lineNoInt != lineValueInt && !updateFlag.equalsIgnoreCase("D")
													&& itemCode.trim().equalsIgnoreCase(itemCode1.trim())) {
												System.out.println("Item is unique it cannot be repeated");
												errcode = "VTDUPITEM ";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
							}
						}

						// Added by Mahesh Saggam on 24-June-2019 [End]
					}
					if (childNodeName.equalsIgnoreCase("quantity")) {
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						//quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom1));

						qty = qty == "" ? "0" : qty;

						if (qty == null || Double.parseDouble(qty) <= 0) {
							errcode = "VTQTY";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
							queryStdoum = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));// Added by
							// Anjali R.
							// on[13/03/2018][To
							// pass
							// current
							// line
							// query_stdoum
							// value]

							if (indNo != null && indNo.trim().length() > 0) {
								// Added by Anjali R. on[13/03/2018][To pass current line query_stdoum
								// value][Start]
								// if (Isreasonrequired(dom, currentFormNo, indNo,lineNo,conn))
								if (Isreasonrequired(dom, currentFormNo, indNo, lineNo, queryStdoum, conn))
									// Added by Anjali R. on[13/03/2018][To pass current line query_stdoum
									// value][End]
								{
									errcode = "VTPIQTY1";
									if (errcode != null && errcode.trim().length() > 0) {
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							if (quotNo.length() > 0) {

								itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));

								sql = "select sum(quantity) from pquot_det where quot_no = ? and item_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, quotNo);
								pStmt.setString(2, itemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									lcqty = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (Double.parseDouble(qty) > lcqty) {
									errcode = "VTPQQTY1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}

								qtyStr = checkNull(genericUtility.getColumnValue("quantity", dom));
								qtystdStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

								rateStr = checkNull(genericUtility.getColumnValue("rate", dom));
								ratestdStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));
								unit = checkNull(genericUtility.getColumnValue("unit", dom));
								unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));

								if (rateStr == null || rateStr.trim().length() == 0) {
									rateStr = "0";
								}
								if (ratestdStr == null || ratestdStr.trim().length() == 0) {
									ratestdStr = "0";
								}
								if (qtyStr == null || qtyStr.trim().length() == 0) {
									qtyStr = "0";
								}
								if (qtystdStr == null || qtystdStr.trim().length() == 0) {
									qtystdStr = "0";
								}
								varValue = distComm.getDisparams("999999", "RCP_UOM_VARIANCE", conn);
								// lsvalue = gf_getenv_dis('999999',
								// 'RCP_UOM_VARIANCE')

								if ("NULLFOUND".equalsIgnoreCase(varValue)) {
									//errcode = "TUOMVARPARM";
									errcode = "TUOMPARM";
									// errcode = errcode +
									// "Variabe RCP_UOM_VARIANCE not defined under Distribution Environment
									// Variables "
									// ;
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}

								//if (isNumeric(varValue)) 
								if (!isNumeric(varValue))
								{
									//errcode = "VTUOMVARPARM1";
									errcode = "VTUOMPARM1";
									// errcode = errcode +
									// "Please set a numeric value for Distribution environment Variabe
									// RCP_UOM_VARIANCE "
									// ;
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}

								if (unitRate.trim().equalsIgnoreCase(unit.trim())) {
									if (Math.abs(Double.parseDouble(qtyStr == "" ? "0" : qtyStr)
											* Double.parseDouble(rateStr)
											- Double.parseDouble(qtystdStr) * Double.parseDouble(ratestdStr)) > Double
											.parseDouble((varValue))) {
										errcode = "VTCONV";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

							}

							indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
							taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));

							// if (errcode == null || errcode.trim().length() == 0) {

							if (taskCode != null && taskCode.trim().length() > 0) {
								lsitemCode = checkNull(genericUtility.getColumnValue("item_code", dom));

								sql = "select sum(b.quantity) from porder a ,porddet b where a.purc_order=b.purc_order and a.confirmed='Y'"
										+ "and a.task_code= ? and b.item_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taskCode);
								pStmt.setString(2, lsitemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									pordeQtyDb = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								/*
								 * sql = "select temp  from dual "; pStmt = conn.prepareStatement(sql);
								 * pStmt.setString(1, taskCode); pStmt.setString(2, lsitemCode); rs =
								 * pStmt.executeQuery(); if (rs.next()) { } rs.close(); rs = null;
								 * pStmt.close(); pStmt = null;
								 */

								if (indNo != null && indNo.trim().length() > 0) {
									indentQtylc = 0;
								} else {
									sql = "select sum(quantity) from indent where task_code = ? and status='A' and item_code= ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, taskCode);
									pStmt.setString(2, lsitemCode);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										indentQtylc = rs.getDouble(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;
								}

								sql = "select temp  from dual ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taskCode);
								pStmt.setString(2, lsitemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								totalQtyDb = indentQtylc + pordeQtyDb + Double.parseDouble(qty);

								sql = "select sum(quantity) from proj_est_bsl_item where task_code = ? and item_code= ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taskCode);
								pStmt.setString(2, lsitemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									projEstQtyDb = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								sql = "select temp  from dual ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taskCode);
								pStmt.setString(2, lsitemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (totalQtyDb > projEstQtyDb) {
									errcode = "VTTASK2";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

							// }
						}
					}
					if (childNodeName.equalsIgnoreCase("unit")) {
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						cnt = 0;
						sql = "Select Count(*) from uom where unit = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, unit);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (cnt == 0) {
							errcode = "VTUNIT1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!unit.equalsIgnoreCase(unitStd)) {
							convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));

							convQtyStduom = convQtyStduom == "" ? "0" : convQtyStduom;

							if (convQtyStduom == null || Double.parseDouble(convQtyStduom) == 0) {
								cnt = 0;
								sql = "Select Count(*) from uomconv where unit__fr = ? and unit__to = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, unit);
								pStmt.setString(2, unitStd);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									errcode = "VTUNIT3";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("unit__rate")) {
						unit = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						cnt = 0;
						sql = "Select Count(*) from uom where unit = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, unit);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (cnt == 0) {
							errcode = "VTUNIT1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!unit.equalsIgnoreCase(unitStd)) {
							convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
							System.out.println("@@@@@@ convQtyStduom[" + convQtyStduom + "]");
							if (convQtyStduom == null || convQtyStduom.trim().length() == 0
									|| (convQtyStduom != null && Double.parseDouble(convQtyStduom) == 0)) {
								cnt = 0;
								sql = "Select Count(*) from uomconv where unit__fr = ? and unit__to = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, unit);
								pStmt.setString(2, unitStd);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									errcode = "VTUNIT3";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("rate")) {
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom));
						qtyStduom = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

						discount = checkNull(genericUtility.getColumnValue("discount", dom));
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
						taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));

						if (discount == null) {
							discount = "0";
						}

						// if (errcode == null || errcode.trim().length() == 0) {

						if (taskCode != null && taskCode.trim().length() > 0) {
							itemCodels = checkNull(genericUtility.getColumnValue("item_code", dom));

							sql = " select avg(rate) from proj_est_bsl_item where task_code= ? and item_code= ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taskCode);
							pStmt.setString(2, itemCodels);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								estRate = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							rate = rate == "" ? "0" : rate;

							if (Double.parseDouble(rate) > estRate) {
								errcode = "VTTASK5";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// }
						rate = rate == "" ? "0" : rate;
						if (rate != null && Double.parseDouble(rate) > 0) {
							indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));

							if (indNo != null && indNo.trim().length() > 0) {
								// ls_type_allow_porate_list =
								// trim(gf_getenv_dis('999999','TYPE_ALLOW_PURC_RATE'))
								typeAllowPorateList = distComm.getDisparams("999999", "TYPE_ALLOW_PURC_RATE", conn);

								if ("NULLFOUND".equalsIgnoreCase(typeAllowPorateList)) {
									typeAllowPorateList = "";
								}
								/*
								 * do { typeAllowPorate = DistCommon.getToken( typeAllowPorateList, ""); if
								 * (pordType.trim() == typeAllowPorate .trim()) { lbflag = true; }
								 * 
								 * } while (typeAllowPorateList.trim() .length() > 0);
								 */

								System.out.println("@@@@@@@ typeAllowPorateList[" + typeAllowPorateList + "]");
								String typeAllowPorateListArray[] = typeAllowPorateList.split(",");
								System.out.println("@@@@@ typeAllowPorateListArray.length["
										+ typeAllowPorateListArray.length + "]");
								for (int k = 0; k < typeAllowPorateListArray.length; k++) {
									if (pordType != null
											&& pordType.trim().equalsIgnoreCase(typeAllowPorateListArray[k].trim())) {
										lbflag = true;
									}
								}
								discount = discount == "" ? "0" : discount;
								if (lbflag = false) {
									if (discount != null && Double.parseDouble(discount) > 0) {
									} else {
									}

									sql = "select case when purc_rate is null then 0 else purc_rate end,case when max_rate is null then 0 else max_rate end"
											+ " from indent where  ind_no= ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, indNo);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										maxRateDb = rs.getDouble(2);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;

									if (Double.parseDouble(rate) > maxRateDb) {
										errcode = "VTLESMXRT";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

							}
							// if (errcode == null || errcode.trim().length() == 0) {

							if (priceList.trim().length() == 0 || priceList == null) {
								sql = "select var_value from disparm where prd_code = '999999' and var_name = 'REGULATED_PRICE_LIST'";
								pStmt = conn.prepareStatement(sql);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									priceList = checkNull(rs.getString("var_value"));
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								System.out.println("REGULATED_PRICE_LIST-->PriceList[" + priceList + "]");

								if (checkNull(priceList).trim().length() > 0) {
								}

								errcode = checkrate(dom, conn);
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

							// }

						}
					}
					if (childNodeName.equalsIgnoreCase("conv__qty_stduom")) {
						convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));

						if (convQtyStduom == null || convQtyStduom.trim().length() == 0) {
							convQtyStduom = "0";
						}
						// Added by Mahesh Saggam on 04/07/2019 Start [if conv_qty__stduom is 1 then
						// quantity and quantity__stduom should be same]

						qtyConvStdUom = Double.parseDouble(convQtyStduom);

						if (qtyConvStdUom == 1) {

							qtyStr = checkNull(genericUtility.getColumnValue("quantity", dom));
							qtystdStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

							System.out.println("quantity = " + qtyStr + " std quantity = " + qtystdStr);

							if (qtyStr != null && qtyStr.trim().length() > 0) {
								pordQuantity = Double.parseDouble(qtyStr);
							}
							if (qtystdStr != null && qtystdStr.trim().length() > 0) {
								quantityStduom = Double.parseDouble(qtystdStr);
							}
							if (pordQuantity != quantityStduom) {
								System.out.println("Error occured in validating quantity");
								errcode = "VTINVQTTY";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// if (unit.trim().equalsIgnoreCase(unitStd.trim()) &&
						// Integer.parseInt(convQtyStduom) != 1)
						if (unit.trim().equalsIgnoreCase(unitStd.trim()) && Double.parseDouble(convQtyStduom) != 1)
							// Added by Mahesh Saggam on 04/07/2019 end
						{
							errcode = "VTUCON1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!unit.trim().equalsIgnoreCase(unitStd.trim())) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(ls_item_code,
							// mval, mval1, lc_convqtystduom)
							errcode = checkConvfact(itemCode, unit, unitStd, Double.parseDouble(convQtyStduom), conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("conv__rtuom_stduom")) {
						convRtuomStduom = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						unit = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));

						if (convRtuomStduom == null || convRtuomStduom.trim().length() == 0) {
							convRtuomStduom = "0";
						}
						// Added by Mahesh Saggam on 04/07/2019 Start [if conv_rate__stduom is 1 then
						// rate and rate__stduom should be same]

						convRateuomStduom = Double.parseDouble(convRtuomStduom);

						if (convRateuomStduom == 1) {

							rateStr = checkNull(genericUtility.getColumnValue("rate", dom));
							ratestdStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));
							System.out.println("rate = " + rateStr + " std rate = " + ratestdStr);

							if (rateStr != null && rateStr.trim().length() > 0) {
								pRate = Double.parseDouble(rateStr);
							}
							if (ratestdStr != null && ratestdStr.trim().length() > 0) {
								stdRate = Double.parseDouble(ratestdStr);
							}
							if (pRate != stdRate) {
								System.out.println("Error occured in validating rate");
								errcode = "VTINVRTE";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// if (unit.trim().equalsIgnoreCase(unitStd.trim()) &&
						// Integer.parseInt(convRtuomStduom) != 1) Commented by Mahesh Saggam
						if (unit.trim().equalsIgnoreCase(unitStd.trim()) && Double.parseDouble(convRtuomStduom) != 1)
							// Added by Mahesh Saggam on 04/07/2019 end
						{
							errcode = "VTUCON1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!unit.trim().equalsIgnoreCase(unitStd.trim())) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(ls_item_code,
							// mval, mval1, lc_convqtystduom)
							errcode = checkConvfact(itemCode, unit, unitStd, Double.parseDouble(convRtuomStduom), conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("loc_code")) {
						locCode = checkNull(genericUtility.getColumnValue("loc_code", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));

						if (locCode == null || locCode.trim().length() == 0) {
							errcode = "VMLOCBK";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							cnt = 0;
							sql = "Select Count(*) from location  where loc_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, locCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTLOC1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("work_order")) {

						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom1));

						if (!"O".equalsIgnoreCase(pordType.trim())) {
							workOrder = checkNull(genericUtility.getColumnValue("work_order", dom));

							if (workOrder.length() > 0) {
								// cnt1++;
								cnt1 = 0;
								sql = "select status from workorder  where work_order = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, workOrder);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									status = rs.getString("status");
									cnt1++;
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
								System.out.println("@@@@@@ cnt1[" + cnt1 + "]status[" + status + "]");
								if (cnt1 == 0) {
									errcode = "VTWORD1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								} else if (status.equalsIgnoreCase("C") || status.equalsIgnoreCase("X")) {
									errcode = "VTWORDER2";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("pack_code")) {

						packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
						if (packCode.length() > 0) {
							cnt = 0;
							sql = "Select Count(*) from packing  where pack_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, packCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt == 0) {
								errcode = "VTPKCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					if (childNodeName.equalsIgnoreCase("quantity__stduom")) {
						qtyStduom = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

						if ("-999999999".equalsIgnoreCase(qtyStduom)) {
							errcode = "VTPOQTY3";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("rate__stduom")) {
						rateStduom = checkNull(genericUtility.getColumnValue("rate__stduom", dom));

						if ("-999999999".equalsIgnoreCase(rateStduom)) {
							errcode = "VTPORATE";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("acct_code__dr")) {
						acctCodeDr = checkNull(genericUtility.getColumnValue("acct_code__dr", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));

						// errcode =
						// nvo_dis.gbf_acct(siteCode,acctCodeDr,"P-ORD")
						errcode = finCommon.isAcctCode(siteCode, acctCodeDr, "P-ORD", conn);
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						// if (errcode == null || errcode.trim().length() == 0) {

						// errcode =
						// nvo_dis.gbf_acct_type(acctCodeDr,"", "O")

						// ----Changed and commented by Jaffar S on 05 March 19[Start]
						/*
						 * errcode = finCommon.isAcctType(siteCode, "", "O", conn);
						 */
						errcode = finCommon.isAcctType(acctCodeDr, "", "O", conn);
						// ----End---------
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						// }
					}
					if (childNodeName.equalsIgnoreCase("acct_code__ap_adv")) {
						acctCodeApadv = checkNull(genericUtility.getColumnValue("acct_code__ap_adv", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if (acctCodeApadv != null && acctCodeApadv.trim().length() != 0) {
							// errcode =
							// nvo_dis.gbf_acct(mval1,mval,transer)
							errcode = finCommon.isAcctCode(siteCode, acctCodeApadv, "P-ORD", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("cctr_code__ap_adv")) {
						cctrCodeApadv = checkNull(genericUtility.getColumnValue("cctr_code__ap_adv", dom));
						acctCodeApadv = checkNull(genericUtility.getColumnValue("acct_code__ap_adv", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if (cctrCodeApadv.length() > 0 && cctrCodeApadv != null) {
							// errcode =
							// nvo_dis.gbf_acct(mval1,mval,transer)
							errcode = finCommon.isCctrCode(acctCodeApadv, cctrCodeApadv, "P-ORD", conn);
							// errcode = finCommon.isAcctCode(siteCode,
							// cctrCodeApadv, "P-ORD", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("acct_code__cr")) {
						acctCodeCr = checkNull(genericUtility.getColumnValue("acct_code__cr", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						System.out.println("@@@@@@ purcOrder[" + purcOrder + "]");
						// if (purcOrder == null || purcOrder.trim().length() == 0)
						// {
						// errcode =
						// nvo_dis.gbf_acct(mval1,mval,transer)
						errcode = finCommon.isAcctCode(siteCode.trim(), acctCodeCr.trim(), "P-ORD", conn);
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						// if (errcode == null || errcode.trim().length() == 0) {

						invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
						// invAcctPorcp =
						// trim(gf_getfinparm('999999','INV_ACCT_PORCP'))
						if (!"ERROR".equalsIgnoreCase(invAcctPorcp)) {
							if (invAcctPorcp == null || "NULLFOUND".equalsIgnoreCase(invAcctPorcp)
									|| invAcctPorcp.trim().length() == 0) {
								invAcctPorcp = "N";
							}

						}
						// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						if ("S".equals(invAcctPorcp)) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								invAcctSer = checkNull(rs.getString("inv_acct"));
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (invAcctSer == null || invAcctSer.trim().length() == 0) {
								invAcctSer = "N";
							}
							invAcctPorcp = invAcctSer;
						}
						// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item

						invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

						if (!"ERROR".equalsIgnoreCase(invAcctQc)) {
							invAcctQc = "N";
						}

						if ("N".equalsIgnoreCase(invAcctPorcp) || "N".equalsIgnoreCase(invAcctQc)) {

							if ("Y".equalsIgnoreCase(invAcctPorcp)) {
								// errcode =
								// nvo_dis.gbf_acct_type(acctCodeCr,"","O")
								errcode = finCommon.isAcctType(acctCodeCr, "", "O", conn);
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else {
								// errcode =
								// nvo_dis.gbf_acct_type(mVal,ls_supp_code,
								// "S")
								errcode = finCommon.isAcctType(acctCodeCr, suppCode, "S", conn);
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

						// }
						// if (errcode == null || errcode.trim().length() == 0) {

						String acctCodeCrTemp = "";

						parentNodeList1 = dom2.getElementsByTagName("Detail2");
						parentNode1 = parentNodeList1.item(0);
						childNodeList1 = parentNode1.getChildNodes();
						childNodeListLength1 = childNodeList1.getLength();
						for (ctr1 = 1; ctr1 < childNodeListLength1; ctr1++) {
							childNode1 = childNodeList1.item(ctr1);
							childNodeName1 = childNode1.getNodeName();
							System.out.println("childNodeName[" + childNodeName1 + "]");
							if (childNodeName1.equalsIgnoreCase("acct_code__cr")) {
								acctCodeCrTemp = checkNull(genericUtility.getColumnValue("acct_code__cr", dom2)).trim();
								lineNoTemp = checkNull(genericUtility.getColumnValue("line_no", dom2)).trim();
								// Added By PRiyankaC on 04JAn18[START]
								if (acctCodeCrTemp == null || acctCodeCrTemp.trim().length() == 0) {
									errList.add("VMACCODE1 ");
									errFields.add(childNodeName.toLowerCase());
								}
								// Added By PRiyankaC on 04JAn18[END]
								else if (!acctCodeCrTemp.equalsIgnoreCase(acctCodeCrTemp.trim())
										&& (!lineNoTemp.equalsIgnoreCase(lineNo.trim()))) {
									errcode = "VTACCTCODE";
									// +
									// "~t In a Single PO two account code credit is not allowed";
									if (errcode != null && errcode.trim().length() > 0) {
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

							}

						}

						// }
					}
					if (childNodeName.equalsIgnoreCase("cctr_code__dr")) {
						cctrCodeDr = checkNull(genericUtility.getColumnValue("cctr_code__dr", dom));
						acctCodeDr = checkNull(genericUtility.getColumnValue("acct_code__dr", dom));
						if (cctrCodeDr == null || cctrCodeDr.trim().length() == 0) {
							errList.add("VMCCTRDRNU");
							errFields.add(childNodeName.toLowerCase());
						} else {
							// errcode = nvo_dis.gbf_cctr(mval1,mval,transer)
							errcode = finCommon.isCctrCode(acctCodeDr, cctrCodeDr, "P-ORD", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("cctr_code__cr")) {
						cctrCodeCr = checkNull(genericUtility.getColumnValue("cctr_code__cr", dom));
						acctCodeCr = checkNull(genericUtility.getColumnValue("acct_code__cr", dom));

						System.out.println("cctrCodeCr[" + cctrCodeCr + "]acctCodeCr[" + acctCodeCr + "]");

						if (cctrCodeCr == null || cctrCodeCr.trim().length() == 0) {
							errList.add("VMCCTRCRNU");
							errFields.add(childNodeName.toLowerCase());
						} else {
							// errcode = nvo_dis.gbf_cctr(mval1,mval,transer)
							errcode = finCommon.isCctrCode(acctCodeCr, cctrCodeCr, "P-ORD", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("dept_code")) {
						deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));
						acctCodeDr = checkNull(genericUtility.getColumnValue("acct_code__dr", dom));

						if (deptCode != null && deptCode.trim().length() > 0) {
							// errcode =
							// gbf_acctdept(ls_acct_code__dr,ls_dept_code,transer)\
							errcode = isAcctdept(acctCodeDr, deptCode, "P-ORD", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("req_date")) {

						reqDate = checkNull(genericUtility.getColumnValue("req_date", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						dlvDate = checkNull(genericUtility.getColumnValue("dlv_date", dom));

						System.out.println(
								"@@@@@@ reqDate[" + reqDate + "]ordDate[" + ordDate + "]dlvDate[" + dlvDate + "]");

						if (reqDate != null && reqDate.trim().length() > 0) {
							reqDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(reqDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						if (dlvDate != null && dlvDate.trim().length() > 0) {
							dlvDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(dlvDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if (reqDateTm.before(ordDateTm) || reqDateTm.before(dlvDateTm)
								|| reqDate.trim().length() == 0) {
							errcode = "VTPOREQDT";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("dlv_date")) {

						dlvDate = checkNull(genericUtility.getColumnValue("dlv_date", dom));
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
						System.out.println(
								"@@@@@@ reqDate[" + reqDate + "]ordDate[" + ordDate + "]dlvDate[" + dlvDate + "]");

						if (ordDate != null && ordDate.trim().length() > 0) {
							ordDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						if (dlvDate != null && dlvDate.trim().length() > 0) {
							dlvDateTm = Timestamp.valueOf(
									genericUtility.getValidDateString(dlvDate, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if (!"P".equalsIgnoreCase(pordType)) {
							if (dlvDateTm.before(ordDateTm)) {
								errcode = "VTPODLVDT";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// if (errcode.trim().length() == 0) {

						if (contractNo.trim().length() > 0) {
							sql = "select contract_fromdate,contract_todate "
									+ " from pcontract_hdr where contract_no = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, contractNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								fromDt = rs.getTimestamp("contract_fromdate");
								toDt = rs.getTimestamp("contract_todate");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							// Added and commented by sarita to correct if condition as dlvDateTm should
							// check with both fromDt & toDt on 17 JULY 18 [START]
							/*
							 * if (dlvDateTm.before(fromDt) || dlvDateTm.after(fromDt)) {
							 */
							if (dlvDateTm.before(fromDt) || dlvDateTm.after(toDt)) {
								// Added and commented by sarita to correct if condition as dlvDateTm should
								// check with both fromDt & toDt on 17 JULY 18 [END]
								errcode = "VTCONVAL";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// }
					}
					if (childNodeName.equalsIgnoreCase("bom_code")) {
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom1));
						System.out.println("@@@@@@ pordType[" + pordType + "]");
						if (!"O".equalsIgnoreCase(pordType)) {
							bomCode = checkNull(genericUtility.getColumnValue("bom_code", dom));
							pordType = checkNull(genericUtility.getColumnValue("pord_type", dom1));

							System.out.println("@@@@@@ pordType[" + pordType + "]bomCode[" + bomCode + "]");
							//commented by monika to get value of jobWorkType and  subContractType from disparm  
							/*if (("C".equalsIgnoreCase(pordType.trim()) || "J".equalsIgnoreCase(pordType.trim()))
									&& bomCode.trim().length() == 0 || bomCode == null) {
							 */	
							//added by monika salla on 31 july 2020 to check the value of disparm 

							jobWorkType = DistCommon.getDisparams("999999", "JOBWORK_TYPE", conn);
							if (jobWorkType == null)
							{
								jobWorkType = "";
							}
							subContractType = DistCommon.getDisparams("999999", "SUBCONTRACT_TYPE", conn);
							if (subContractType == null)
							{
								subContractType = "";
							}

							if ((subContractType.trim().equalsIgnoreCase(pordType.trim())|| jobWorkType.trim().equalsIgnoreCase(pordType.trim()))
									&& bomCode.trim().length() == 0 || bomCode == null) {//end
								errcode = "VTBOMCJ";
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							} 
							//commented by monika to get value of jobWorkType and  subContractType from disparm  
							/*else if (("C".equalsIgnoreCase(pordType.trim()) || "J".equalsIgnoreCase(pordType.trim()))
											&& bomCode.trim().length() > 0) {*/
							else if ((subContractType.trim().equalsIgnoreCase(pordType.trim()) || jobWorkType.trim().equalsIgnoreCase(pordType.trim()))
									&& bomCode.trim().length() > 0) {
								cnt = 0;
								sql = "Select Count(*) from bom  where bom_code = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, bomCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
								if (cnt == 0) {
									errcode = "VMBOM1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else if (bomCode.trim().length() > 0) {
								errcode = "VTBOMNCJ";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					/*
					 * if (childNodeName.equalsIgnoreCase("discount")) { discount =
					 * checkNull(genericUtility.getColumnValue( "discount", dom));
					 * 
					 * discount = discount== "" ? "0" :discount ;
					 * 
					 * if (discount == null || discount.trim().length() == 0) { errcode = "VTDISC";
					 * errList.add(errcode); errFields.add(childNodeName.toLowerCase()); } else if
					 * (Double.parseDouble(discount) > 100) { errcode = "VTDISC1";
					 * errList.add(errcode); errFields.add(childNodeName.toLowerCase()); } }
					 */
					if (childNodeName.equalsIgnoreCase("op_reason")) {
						opReason = checkNull(genericUtility.getColumnValue("op_reason", dom));
						queryStdoum = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

						if (opReason == null || opReason.trim().length() == 0) {

							// Added by Anjali R. on[13/03/2018][To pass current line query_stdoum
							// value][Start]
							// if (Isreasonrequired(dom, currentFormNo, indNo,lineNo,conn))
							if (Isreasonrequired(dom, currentFormNo, indNo, lineNo, queryStdoum, conn))
								// Added by Anjali R. on[13/03/2018][To pass current line query_stdoum
								// value][End]
							{
								errcode = "VTPORESN2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));

						if (taxChap != null && taxChap.trim().length() > 0) {
							cnt = 0;
							sql = "Select Count(*) from taxchap  where tax_chap = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxChap);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTAXCHAP1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));

						if (taxClass != null && taxClass.trim().length() > 0) {
							cnt = 0;
							sql = "Select Count(*) from taxclass  where tax_class = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxClass);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTAXCLA1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_env")) {
						// taxEnv = checkNull(genericUtility.getColumnValue(
						// "tax_env", dom));
						taxEnv = checkNull(distComm.getParentColumnValue("tax_env", dom, "2"));
						System.out.println("PO 2 TaxEnv:" + taxEnv + "]");
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						if (taxEnv != null && taxEnv.trim().length() > 0) {
							cnt = 0;
							sql = "Select Count(*) from taxenv  where tax_env = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxEnv);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTAXENV1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								// Pavan R 17sept19 start[to validate tax environment]
								// errcode =
								// gf_check_taxenv_status(mVAL,ordDate)
								/*
								 * Date parsedDate = sdf.parse(ordDate); timestamp = new java.sql.Timestamp(
								 * parsedDate.getTime()); errcode = DistCommon.getCheckTaxEnvStatus( taxEnv,
								 * timestamp, conn);
								 */
								if (ordDate != null && ordDate.trim().length() > 0) {
									sysDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDate,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}
								// errcode = DistCommon.getCheckTaxEnvStatus(taxEnv, timestamp, conn);
								errcode = DistCommon.getCheckTaxEnvStatus(taxEnv, sysDate, "P", conn);
								// Pavan R 17sept19 end[to validate tax environment]
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("emp_code__qcaprv")) {
						empCodeQcaprv = checkNull(genericUtility.getColumnValue("emp_code__qcaprv", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));

						sql = "select qc_reqd from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							qcReqd = rs.getString("qc_reqd");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						System.out.println("@@@@@@3430 qcReqd[" + qcReqd + "]empCodeQcaprv[" + empCodeQcaprv + "]");
						if ("Y".equalsIgnoreCase(qcReqd)) {

							if (empCodeQcaprv == null || empCodeQcaprv.trim().length() == 0) {
								errcode = "VERREMPL";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

							// errcode =
							// nvo_dis.gbf_employee_resig('',ls_emp,'')
							errcode = isEmployeeResign(empCodeQcaprv, childNodeName, conn);
							System.out.println("@@@@@@3430 errcode[" + errcode + "]");
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("item_code__mfg")) {
						itemCodeMfg = checkNull(genericUtility.getColumnValue("item_code__mfg", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						if (itemCodeMfg.length() > 0) {
							cnt = 0;
							sql = "Select Count(*) from item  where item_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCodeMfg);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VMITEM1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("spec_ref")) {
						specRef = checkNull(genericUtility.getColumnValue("spec_ref", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
						specreqd = isSpecReqd(siteCodeDlv, itemCode, conn);
						qcreqd = isQcReqd(siteCodeDlv, itemCode, conn);

						if ("Y".equalsIgnoreCase(qcreqd)) {
							if ("Y".equalsIgnoreCase(specreqd)) {
								if (specRef == null || specRef.trim().length() == 0) {
									errcode = "VTSPEC"; // ~t Specification
									// required for this
									// item SPECIFICATION
									// REFERENCE cannot be
									// blank";
									if (errcode != null && errcode.trim().length() > 0) {
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								} else {
									cnt = 0;
									sql = "select count(1) from qcitem_spec_det where item_code = ? and spec_ref = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, itemCode);
									pStmt.setString(2, specRef);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;

									if (cnt == 0) {
										// errcode =
										// "VTINVSPEC~t Invalid Specification Reference! not specified in item QC
										// specification master"
										// ;
										errcode = "VTINVSPEC";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("acct_code__prov_dr")) {
						acctCodeProvDr = checkNull(genericUtility.getColumnValue("acct_code__prov_dr", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));

						invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

						if (!"ERROR".equalsIgnoreCase(invAcctPorcp)) {
							if (invAcctPorcp == null || "NULLFOUND".equalsIgnoreCase(invAcctPorcp)
									|| invAcctPorcp.trim().length() == 0) {
								invAcctPorcp = "N";
							}
							// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
							// inv_acct of the itemser of the item
							if ("S".equals(invAcctPorcp)) {
								itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
								sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, itemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									invAcctSer = checkNull(rs.getString("inv_acct"));
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
								if (invAcctSer == null || invAcctSer.trim().length() == 0) {
									invAcctSer = "N";
								}
								invAcctPorcp = invAcctSer;
							}
							// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
							// inv_acct of the itemser of the item
							// invAcctQc =
							// trim(gf_getfinparm("999999","INV_ACCT_PORCP"));
							invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

							if (!"ERROR".equalsIgnoreCase(invAcctQc)) {
								if (invAcctQc == null || "NULLFOUND".equalsIgnoreCase(invAcctQc)
										|| invAcctQc.trim().length() == 0) {
									invAcctQc = "N";
								}
								// invAcctQc =
								// finCommon.getFinparams("999999","INV_ACCT_QCORDER",
								// conn);

								if ("Y".equalsIgnoreCase(invAcctPorcp) && "Y".equalsIgnoreCase(invAcctQc)) {
									errcode = finCommon.isAcctCode(siteCode, acctCodeProvDr, "PORDER", conn);
									if (errcode != null && errcode.trim().length() > 0) {
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								// if (errcode == null || errcode.trim().length() == 0) {

								errcode = finCommon.isAcctType(acctCodeProvDr, suppCode, "O", conn);
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}

								/*
								 * } else { errcode = "VTPROVACCT "; }
								 */
							}
						}
					}

				}
				if (childNodeName.equalsIgnoreCase("acct_code__prov_cr")) {
					acctCodeProvCr = checkNull(genericUtility.getColumnValue("acct_code__prov_cr", dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));

					invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

					if (!"ERROR".equalsIgnoreCase(invAcctPorcp)) {
						if (invAcctPorcp == null || "NULLFOUND".equalsIgnoreCase(invAcctPorcp)
								|| invAcctPorcp.trim().length() == 0) {
							invAcctPorcp = "N";
						}
						// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						if ("S".equals(invAcctPorcp)) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								invAcctSer = checkNull(rs.getString("inv_acct"));
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (invAcctSer == null || invAcctSer.trim().length() == 0) {
								invAcctSer = "N";
							}
							invAcctPorcp = invAcctSer;
						}
						// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						// invAcctQc =
						// finCommon.getFinparams("999999","INV_ACCT_QCORDER",
						// conn);
						// invAcctQc =
						// trim(gf_getfinparm("999999","INV_ACCT_PORCP"));
						invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);
						if (!"ERROR".equalsIgnoreCase(invAcctQc)) {
							if (invAcctQc == null || "NULLFOUND".equalsIgnoreCase(invAcctQc)
									|| invAcctQc.trim().length() == 0) {
								invAcctQc = "N";
							}
							// invAcctQc =
							// finCommon.getFinparams("999999","INV_ACCT_QCORDER",
							// conn);
							if ("Y".equalsIgnoreCase(invAcctPorcp) && "Y".equalsIgnoreCase(invAcctQc)) {
								errcode = finCommon.isAcctCode(siteCode, acctCodeProvCr, "PORDER", conn);
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							// if (errcode == null || errcode.trim().length() == 0) {
							// errcode =
							// nvo_dis.gbf_acct_type(mVal,ls_supp_code, "S")
							errcode = finCommon.isAcctType(acctCodeProvCr, suppCode, "O", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							/*
							 * } else { errcode = "VTPROVACCT "; errList.add(errcode);
							 * errFields.add(childNodeName.toLowerCase()); }
							 */
						}
					}
				}
				if (childNodeName.equalsIgnoreCase("cctr_code__prov_dr")) {
					cctrCodeProvDr = checkNull(genericUtility.getColumnValue("cctr_code__prov_dr", dom));
					acctCodeProvDr = checkNull(genericUtility.getColumnValue("acct_code__prov_dr", dom));

					// invAcctQc =
					// trim(gf_getfinparm("999999","INV_ACCT_PORCP"));
					invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
					if (!"ERROR".equalsIgnoreCase(invAcctPorcp)) {
						if (invAcctPorcp == null || "NULLFOUND".equalsIgnoreCase(invAcctPorcp)
								|| invAcctPorcp.trim().length() == 0) {
							invAcctPorcp = "N";
						}
						// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						if ("S".equals(invAcctPorcp)) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								invAcctSer = checkNull(rs.getString("inv_acct"));
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (invAcctSer == null || invAcctSer.trim().length() == 0) {
								invAcctSer = "N";
							}
							invAcctPorcp = invAcctSer;
						}
						// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

						// invAcctQc =
						// trim(gf_getfinparm("999999","INV_ACCT_QCORDER"));
						// invAcctQc =
						// finCommon.getFinparams("999999","INV_ACCT_QCORDER",
						// conn);
						if (!"ERROR".equalsIgnoreCase(invAcctQc)) {
							if (invAcctQc == null || "NULLFOUND".equalsIgnoreCase(invAcctQc)
									|| invAcctQc.trim().length() == 0) {
								invAcctQc = "N";
							}

						}
						if ("Y".equalsIgnoreCase(invAcctPorcp) && "Y".equalsIgnoreCase(invAcctQc)) {
							errcode = finCommon.isCctrCode(acctCodeProvDr, cctrCodeProvDr, "PORDER", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				if (childNodeName.equalsIgnoreCase("cctr_code__prov_cr")) {
					cctrCodeProvCr = checkNull(genericUtility.getColumnValue("cctr_code__prov_cr", dom));
					acctCodeProvCr = checkNull(genericUtility.getColumnValue("acct_code__prov_cr", dom));

					invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
					if (!"ERROR".equalsIgnoreCase(invAcctPorcp)) {
						if (invAcctPorcp == null || "NULLFOUND".equalsIgnoreCase(invAcctPorcp)
								|| invAcctPorcp.trim().length() == 0) {
							invAcctPorcp = "N";
						}
						// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						if ("S".equals(invAcctPorcp)) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								invAcctSer = checkNull(rs.getString("inv_acct"));
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (invAcctSer == null || invAcctSer.trim().length() == 0) {
								invAcctSer = "N";
							}
							invAcctPorcp = invAcctSer;
						}
						// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

						// invAcctQc =
						// finCommon.getFinparams("999999","INV_ACCT_QCORDER",
						// conn);
						if (!"ERROR".equalsIgnoreCase(invAcctQc)) {
							if (invAcctQc == null || "NULLFOUND".equalsIgnoreCase(invAcctQc)
									|| invAcctQc.trim().length() == 0) {
								invAcctQc = "N";
							}
						}
						if ("Y".equalsIgnoreCase(invAcctPorcp) && "Y".equalsIgnoreCase(invAcctQc)) {
							errcode = finCommon.isCctrCode(acctCodeProvCr, cctrCodeProvCr, "PORDER", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				if (childNodeName.equalsIgnoreCase("prd_code__rfc")) {
					prdCodeRfc = checkNull(genericUtility.getColumnValue("prd_code__rfc", dom));

					if (prdCodeRfc != null && prdCodeRfc.trim().length() > 0) {
						cnt = 0;
						sql = "Select count(1) from period where code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, prdCodeRfc);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (cnt == 0) {
							errcode = "VTRFCDATE";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							if (sdf.parse(prdCodeRfc).before(currDate)) {
								errcode = "VTRFCDATE2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				if (childNodeName.equalsIgnoreCase("duty_paid")) {
					dutyPaid = checkNull(genericUtility.getColumnValue("duty_paid", dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));

					sql = "Select case when eou is null then 'N' else eou end From site Where site_code =  ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						eou = rs.getString(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("Y".equalsIgnoreCase(eou)) {
						if (dutyPaid.trim().length() == 0 || dutyPaid == null) {
							errcode = "VTDUTYBK";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						} else if ("N".equalsIgnoreCase(dutyPaid)) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));

							sql = "Select b.lop_reqd  From item a, itemser b Where a.item_ser = b.item_ser And a.item_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();

							if (rs.next()) {
								lopReqd = rs.getString(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							// java.sql.Date ordDated = (java.sql.Date)
							// sdf.parse(ordDate);
							if (ordDate != null && ordDate.trim().length() > 0) {
								ordDated2 = Timestamp.valueOf(
										genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
												genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}

							if ("Y".equalsIgnoreCase(lopReqd)) {
								cnt = 0;
								sql = "Select count(1) From lop_hdr a, lop_det b Where a.lop_ref_no = b.lop_ref_no And"
										+ "a.site_code = :ls_site_code And a.confirmed = 'Y' And b.item_code = :ls_itemCode And  b.item_status ='A' And"
										+ "? >= a.valid_from And ? <= a.valid_to And b.buy_sell_flag In ('P','B');";
								pStmt = conn.prepareStatement(sql);
								pStmt.setTimestamp(1, ordDated2);
								pStmt.setTimestamp(2, ordDated2);
								// pStmt.setDate(1, ordDated);
								// pStmt.setDate(2, ordDated);
								rs = pStmt.executeQuery();

								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									errcode = "VTLOPITEM1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}

				}
				if (childNodeName.equalsIgnoreCase("form_no")) {
					formNo = checkNull(genericUtility.getColumnValue("form_no", dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					dutyPaid = checkNull(genericUtility.getColumnValue("duty_paid", dom));

					sql = "Select case when eou is null then 'N' else eou end From site Where site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						eou = rs.getString(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("Y".equalsIgnoreCase(eou) && "N".equalsIgnoreCase(dutyPaid)) {
						if (formNo != null && formNo.trim().length() > 0) {
							suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							qty = checkNull(genericUtility.getColumnValue("quantity", dom));

							if (qty == null || qty.trim().length() == 0) {
								qty = "0";
							}

							sql = "select sum(case when b.quantity is null then 0 else b.quantity end) - sum(case when b.dlv_qty is null then 0 else b.dlv_qty end)"
									+ " From  porder a, porddet b Where a.purc_order = b.purc_order and a.purc_order <> :ls_purc_order  and    b.form_no = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, formNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								preQty = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							parentNodeList1 = dom2.getElementsByTagName("Detail2");
							parentNode1 = parentNodeList1.item(0);
							childNodeList1 = parentNode1.getChildNodes();
							childNodeListLength1 = childNodeList1.getLength();
							for (ctr1 = 0; ctr1 < childNodeListLength1; ctr1++) {

								purcOrderTemp = checkNull(genericUtility.getColumnValue("purc_order", dom2)).trim();
								lineNoTemp = checkNull(genericUtility.getColumnValue("line_no", dom2)).trim();
								formNoTemp = checkNull(genericUtility.getColumnValue("form_no", dom2)).trim();

								if (purcOrderTemp.equalsIgnoreCase(purcOrder.trim())
										&& formNoTemp.equalsIgnoreCase(formNo)
										&& !lineNoTemp.equalsIgnoreCase(lineNo)) {
									qtyBrow = checkNull(genericUtility.getColumnValue("quantity", dom2)).trim();

									if (qtyBrow == null || qtyBrow.trim().length() == 0) {
										qtyBrow = "0";
									}

									totQty1 = totQty + Double.parseDouble(qtyBrow);
								}

							}
							// java.sql.Date ordDated = (java.sql.Date)
							// sdf.parse(ordDate);
							// java.sql.Date purcOrderd = (java.sql.Date)
							// sdf.parse(purcOrder);

							if (ordDate != null && ordDate.trim().length() > 0) {
								ordDated2 = Timestamp.valueOf(
										genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
												genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}

							if (ordDate != null && ordDate.trim().length() > 0) {
								ordDated2 = Timestamp.valueOf(
										genericUtility.getValidDateString(ordDate, genericUtility.getApplDateFormat(),
												genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}
							sql = "select a.status, case when b.quantity is null then 0 else b.quantity end,case when b.qty_used is null then 0 else b.qty_used end"
									+ "From  ct3form_hdr a , ct3form_det b Where a.form_no = b.form_no And a.site_code = ? And b.supp_code = ? And b.item_code = ?"
									+ "And ? >= a.eff_from And ? <= a.valid_upto And b.purc_order = ? And b.line_no = ? And a.status = 'O'"
									+ "And case when a.confirmed is null then 'N' else a.confirmed end = 'Y'";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, siteCode);
							pStmt.setString(2, suppCode);
							pStmt.setString(3, itemCode);
							pStmt.setTimestamp(3, ordDated2);
							pStmt.setTimestamp(4, ordDated2);
							// pStmt.setDate(3, ordDated);
							// pStmt.setDate(4, ordDated);
							// pStmt.setDate(5, purcOrderd);
							pStmt.setString(5, purcOrder);
							pStmt.setString(6, lineNo);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								status = rs.getString(1);
								ct3Qty = rs.getDouble(2);
								qtyUsed = rs.getDouble(3);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt1 == 0) {
								errcode = "VTCT3FORM1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!"O".equalsIgnoreCase(status)) {
								errcode = "VTCT3FORM2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if ((preQty + totQty + Double.parseDouble(qty)) > (ct3Qty - qtyUsed)) {
								errcode = "VTCT3QTY";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					} else if ("Y".equalsIgnoreCase(eou) && "Y".equalsIgnoreCase(dutyPaid)) {
						if (formNo != null && formNo.trim().length() > 0) {
							errcode = "VTCT3DUTY";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

					}

				}
				if (childNodeName.equalsIgnoreCase("anal_code")) {
					analCode = checkNull(genericUtility.getColumnValue("anal_code", dom));

					if (analCode.trim().length() > 0) {
						acctCodeDr = checkNull(genericUtility.getColumnValue("acct_code__dr", dom));
						// errcode =
						// nvo_dis.gbf_analysis(mval1,mval,transer) ;
						errcode = FinCommon.isAnalysis(acctCodeDr, analCode, "P-ORD", conn);
						if (errcode != null && errcode.trim().length() > 0) {
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				if (childNodeName.equalsIgnoreCase("quantity__fc")) {
					pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));

					if ("WO".equalsIgnoreCase(pordType)) {
						quantityFc = checkNull(genericUtility.getColumnValue("quantity__fc", dom));

						if (quantityFc == null || quantityFc.trim().length() == 0) {
							quantityFc = "0";
						}
						if ("0".equalsIgnoreCase(quantityFc)) {
							errcode = "VTFC1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						parentNodeList1 = dom2.getElementsByTagName("Detail2");
						parentNode1 = parentNodeList1.item(0);
						childNodeList1 = parentNode1.getChildNodes();
						childNodeListLength1 = childNodeList1.getLength();
						for (int ctr2 = 0; ctr2 < childNodeListLength1; ctr2++) {

							itemCodeTemp = checkNull(genericUtility.getColumnValue("item_code", dom2)).trim();
							indNoTemp = checkNull(genericUtility.getColumnValue("ind_no", dom2)).trim();

							if (itemCodeTemp.equalsIgnoreCase(itemCode) && indNoTemp.equalsIgnoreCase(indNo)) {
								quantityFcTemp = checkNull(genericUtility.getColumnValue("quantity__fc", dom2)).trim();

								if (quantityFcTemp == null || quantityFcTemp.trim().length() == 0) {
									quantityFcTemp = "0";
								}
								totFc1 = totFc + Double.parseDouble(quantityFcTemp) + Double.parseDouble(quantityFc);

								/*
								 * sql = "select totFc1 from dual"; pStmt = conn.prepareStatement(sql);
								 * pStmt.executeQuery(); if (rs.next()) {
								 * 
								 * } rs.close(); rs = null; pStmt.close(); pStmt = null;
								 */

								if (totFc1 < 100) {
									errcode = "VTFC2";
									if (errcode != null && errcode.trim().length() > 0) {
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

							}

						}

					}

				}

				/*
				 * if (childNodeName.equalsIgnoreCase("supp_code__mnfr")) { String suppCodeMnfr
				 * = checkNull(genericUtility.getColumnValue( "supp_code__mnfr", dom)); siteCode
				 * = checkNull(genericUtility.getColumnValue( "site_code", dom)); if (
				 * suppCodeMnfr != null && suppCodeMnfr.trim().length() > 0) { cnt=0; sql =
				 * " select count(1) " +
				 * " from site_supplier,supplier  where  site_supplier.site_code= ? " +
				 * " and site_supplier.supp_code = ? " +
				 * " and site_supplier.supp_code=supplier.supp_code "; pStmt =
				 * conn.prepareStatement(sql); pStmt.setString(1, siteCode); pStmt.setString(2,
				 * suppCodeMnfr); rs = pStmt.executeQuery(); if (rs.next()) { cnt =
				 * rs.getInt(1); } rs.close(); rs = null; pStmt.close(); pStmt = null;
				 * 
				 * if (cnt == 0) { errcode = "INVSUPPCOD"; errList.add(errcode);
				 * errFields.add(childNodeName.toLowerCase()); } } }
				 */

				// }
			}
			break;
			case 3: {
				System.out.println("VALIDATION FOR DETAIL [3 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("term_code")) {
						termCode = checkNull(genericUtility.getColumnValue("term_code", dom));

						sql = " select count(*) from pur_term where term_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, termCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (cnt == 0) {
							errcode = "VTTERM1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}

			}
			break;
			case 4: {
				System.out.println("VALIDATION FOR DETAIL [4 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("rel_amt") || childNodeName.equalsIgnoreCase("amt_type")) {
						relAmt = checkNull(genericUtility.getColumnValue("rel_amt", dom));

						relAmt = relAmt == "" ? "0" : relAmt;

						if (relAmt == null || relAmt.trim().length() == 0 || Double.parseDouble(relAmt) == 0) {
							errcode = "VTAMOUNT1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}

						ordAmt = checkNull(genericUtility.getColumnValue("ord_amt", dom1));
						totAmt = checkNull(genericUtility.getColumnValue("tot_amt", dom1));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						lcAmount = 0;

						if (ordAmt == null || ordAmt.trim().length() == 0) {
							ordAmt = "0";
						}
						if (totAmt == null || totAmt.trim().length() == 0) {
							totAmt = "0";
						}
						if (relAmt == null || relAmt.trim().length() == 0) {
							relAmt = "0";
						}
						/*
						 * ll_count = dw_detbrow[ii_currformno].RowCount() For ll_row = 1 to ll_count
						 * ls_type = dw_detbrow[ii_currformno ].GetItemString(ll_row,"amt_type")
						 * //'01','02','03' lc_rel_amt = dw_detbrow[ii_currformno].GetItemDecimal(
						 * ll_row,"rel_amt") if ll_row <> ll_line_no then //If line no is being edited.
						 * if ls_type = '01' then //Base lc_adv_amt = lc_ord_amt * (lc_rel_amt/100)
						 * elseif ls_type = '02' then //Net lc_adv_amt = lc_tot_amt * (lc_rel_amt/100)
						 * elseif ls_type = '03' then //Fix lc_adv_amt = lc_rel_amt end if lc_amount =
						 * lc_amount + lc_adv_amt end if Next
						 */

						amtType = checkNull(genericUtility.getColumnValue("amt_type", dom));
						/*
						 * relAmt = checkNull(genericUtility.getColumnValue( "rel_amt", dom, "1"));
						 */
						System.out.println("@@@@@@@3928 amtType[" + amtType + "]");
						if ("01".equalsIgnoreCase(amtType)) {
							advAmt = Double.parseDouble(ordAmt) * (Double.parseDouble(relAmt) / 100);
						} else if ("02".equalsIgnoreCase(amtType)) {
							advAmt = Double.parseDouble(totAmt) * (Double.parseDouble(relAmt) / 100);
						} else if ("03".equalsIgnoreCase(amtType)) {
							advAmt = Double.parseDouble(relAmt);
						}
						lcAmount = lcAmount + advAmt;

						System.out.println("@@@@@@@3942 lcAmount[" + lcAmount + "] > totAmt[" + totAmt + "]");

						// commented by sarita to provide validation on post save as total amount
						// updated on save on 11 JUN 2018 [START]
						/*
						 * if (lcAmount > Double.parseDouble(totAmt)) { errcode = "POADVMIS";
						 * errList.add(errcode); errFields.add(childNodeName.toLowerCase()); }
						 */
						// commented by sarita to provide validation on post save as total amount
						// updated on save on 11 JUN 2018 [END]
					}
					/*
					 * if (childNodeName.equalsIgnoreCase("amt_type")) {
					 * 
					 * relAmt = checkNull(genericUtility.getColumnValue("rel_amt", dom, "1"));
					 * 
					 * if (relAmt == null || Double.parseDouble(relAmt) == 0) { errcode =
					 * "VTAMOUNT1"; errList.add(errcode);
					 * errFields.add(childNodeName.toLowerCase()); }
					 * 
					 * ordAmt = checkNull(genericUtility.getColumnValue( "ord_amt", dom, "1"));
					 * totAmt = checkNull(genericUtility.getColumnValue( "tot_amt", dom, "1"));
					 * lineNo = checkNull(genericUtility.getColumnValue( "line_no", dom, "1"));
					 * lcAmount = 0;
					 * 
					 * 
					 * amtType = checkNull(genericUtility.getColumnValue( "amt_type", dom, "1"));
					 * relAmt = checkNull(genericUtility.getColumnValue( "rel_amt", dom, "1"));
					 * 
					 * if (amtType == "01") { advAmt = Double.parseDouble(ordAmt)
					 * (Double.parseDouble(relAmt) / 100); } else if (amtType == "02") { advAmt =
					 * Double.parseDouble(totAmt) (Double.parseDouble(relAmt) / 100); } else if
					 * (amtType == "03") { advAmt = Double.parseDouble(relAmt); } lcAmount =
					 * lcAmount + advAmt;
					 * 
					 * if (lcAmount > Double.parseDouble(totAmt)) { errcode = "POADVMIS";
					 * errList.add(errcode); errFields.add(childNodeName.toLowerCase()); }
					 * 
					 * }
					 */
					if (childNodeName.equalsIgnoreCase("task_code")) {
						taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));
						relAgnst = checkNull(genericUtility.getColumnValue("rel_agnst", dom));
						System.out.println("@@@@@@ taskCode[" + taskCode + "]relAgnst[" + relAgnst + "]");
						if ("05".equalsIgnoreCase(relAgnst)) {
							if (taskCode == null || taskCode.trim().length() == 0) {
								errcode = "VTNULTASK";
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else if (taskCode != null || taskCode.trim().length() > 0) {
								sql = " select count(*) from proj_task where task_code = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taskCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									errcode = "VTTASK";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						} else if ("06".equalsIgnoreCase(relAgnst)) {
							if (taskCode == null || taskCode.trim().length() == 0) {
								errcode = "VTNULTASK";
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else if (taskCode != null || taskCode.trim().length() > 0) {
								sql = " select count(*) from gencodes where  fld_Name = 'TASK_CODE' AND mod_name = 'W_PORDER'";
								pStmt = conn.prepareStatement(sql);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									sql = " select count(*) from gencodes where  fld_Name = 'TASK_CODE' AND fld_value = ? and mod_name = 'X'";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, taskCode);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;
									if (cnt == 0) {
										errcode = "VTTASKCD";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("due_date")) {
						dueDate = checkNull(genericUtility.getColumnValue("due_date", dom));
						relAgnst = checkNull(genericUtility.getColumnValue("rel_agnst", dom));

						if ("05".equalsIgnoreCase(relAgnst) || "06".equalsIgnoreCase(relAgnst)) {
							// if (dueDate == null ||
							// sdf.parse(dueDate).equals("01/01/00"))
							if (dueDate == null || dueDate.trim().length() == 0) {
								errcode = "INDUEDATE0";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else if (sdf.parse(dueDate).before(currDate)) {
								errcode = "VTDUE";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("task_code__parent")) {

						taskCodeParent = checkNull(genericUtility.getColumnValue("task_code__parent", dom));
						relAgnst = checkNull(genericUtility.getColumnValue("rel_agnst", dom));

						if ("05".equalsIgnoreCase(relAgnst)) {
							if (taskCodeParent != null && taskCodeParent.trim().length() > 0) {
								sql = " select count(*) from proj_task where task_code = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taskCodeParent);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									errcode = "VTPTASK";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

						} else if ("06".equalsIgnoreCase(relAgnst)) {
							if (taskCodeParent != null && taskCodeParent.trim().length() > 0) {
								sql = " select count(*) from gencodes where  fld_Name = 'TASK_CODE' AND mod_name = 'W_PORDER'";
								pStmt = conn.prepareStatement(sql);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (cnt == 0) {
									sql = " select count(*) from gencodes where  fld_Name = 'TASK_CODE' AND fld_value = ? and mod_name = 'X'";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, taskCodeParent);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;
									if (cnt == 0) {
										errcode = "VTTASKCD";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("line_no__prev")) {

						String ll_line_no = "";

						lineNoPrev = checkNull(genericUtility.getColumnValue("line_no__prev", dom));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						// ll_count = dw_detbrow[ii_currformno].RowCount()

						if (lineNoPrev != null && lineNoPrev.trim().length() > 0) {
							flagLine = false;
							/*
							 * For ll_row = 1 to ll_count ll_line_no = dw_detbrow
							 * [ii_currformno].GetItemNumber(ll_row,"line_no")
							 * 
							 * ls_temp ="In loop line no from brow"+string(ll_line_no
							 * )+"previous line no"+string(ll_line_no_prev) select :ls_temp into :ls_temp
							 * from dual;
							 * 
							 * if ll_line_no <> ll_line_num then
							 * 
							 * if ll_line_no_prev=ll_line_no then lb_flag_line=true ls_temp
							 * ="In condition true loop line no from brow"
							 * +string(ll_line_no)+"previous line no"+string( ll_line_no_prev)
							 * 
							 * select :ls_temp into :ls_temp from dual; end if
							 * 
							 * end if
							 * 
							 * Next
							 */

							System.out
							.println("current line ===> lineNo[" + lineNo + "]lineNoPrev[" + lineNoPrev + "]");
							parentList4 = dom2.getElementsByTagName("Detail4");
							int parentNodeListLength4 = parentList4.getLength();
							for (int prntCtr = parentNodeListLength4; prntCtr > 0; prntCtr--) {
								parentNode4 = parentList4.item(prntCtr - 1);
								childList4 = parentNode4.getChildNodes();
								for (int ctr4 = 0; ctr4 < childList4.getLength(); ctr4++) {
									childNode4 = childList4.item(ctr4);

									if (childNode4 != null && childNode4.getFirstChild() != null
											&& childNode4.getNodeName().equalsIgnoreCase("line_no")) {

										ll_line_no = childNode4.getFirstChild().getNodeValue().trim();
										System.out.println("ll_line_no[" + ll_line_no + "]prntCtr[" + prntCtr + "]");

										if (!ll_line_no.equalsIgnoreCase(lineNo)) {
											System.out.println(
													"lineNoPrev[" + lineNoPrev + "]ll_line_no[" + ll_line_no + "]");
											if (lineNoPrev.equalsIgnoreCase(ll_line_no)) {
												flagLine = true;
											} // end if

										} // end if
									}
								}
							}

							if (flagLine == false) {
								errcode = "VTPRVLINE";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("site_code__adv")) {
						siteCodeAdv = checkNull(genericUtility.getColumnValue("site_code__adv", dom));

						if (siteCodeAdv.trim().length() > 0) {
							// errcode = nvo_dis.gbf_site(mval,transer) ;
							errcode = isSiteCode(siteCodeAdv, "P-ORD");// Changed By PriyankaC on 04Jan18
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));

						if (taxChap.trim().length() > 0) {
							sql = " select count(*) from taxchap where  tax_chap = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxChap);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTAXCHAP1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));

						if (taxClass.trim().length() > 0) {
							sql = " select count(*) from taxclass where  tax_class = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxClass);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTAXCLA1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_env")) {
						/*
						 * taxEnv = checkNull(genericUtility.getColumnValue( "tax_env", dom));
						 */
						taxEnv = distComm.getParentColumnValue("tax_env", dom, "4");
						System.out.println("PO 4 TaxEnv:" + taxEnv + "]");
						ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));

						// if (taxEnv.trim().length() > 0) {
						if (taxEnv != null && taxEnv.trim().length() > 0) { // added by manish mhatre [taxEnv!=null]
							sql = " select count(*) from taxenv where  tax_env = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxEnv);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt == 0) {
								errcode = "VTTAXENV1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								Date parsedDate = sdf.parse(ordDate);
								timestamp = new java.sql.Timestamp(parsedDate.getTime());
								// Pavan R 17sept19 start[to validate tax environment]
								/*
								 * errcode = FinCommon.checkTaxEnvStatus(taxEnv, timestamp, conn);
								 */
								errcode = distComm.getCheckTaxEnvStatus(taxEnv, timestamp, "P", conn);
								// Pavan R 17sept19 end[to validate tax environment]
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
				}

			}
			break;
			case 5: {
				System.out.println("FOR DETAIL [ 5]..........");
				parentNodeList = dom.getElementsByTagName("Detail5");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("min_day")) {
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						minDay = genericUtility.getColumnValue("min_day", dom);
						maxDay = genericUtility.getColumnValue("max_day", dom);
						refCode = checkNull(genericUtility.getColumnValue("ref_code", dom));

						/*
						 * ll_count = dw_detbrow[ii_currformno].RowCount() For ll_row = 1 to ll_count if
						 * trim(dw_detbrow[ii_currformno ].GetItemString(ll_count,"purc_order")) =
						 * trim(ls_val) and & dw_detbrow[ii_currformno].GetItemnumber
						 * (ll_count,"min_day") = mmin_day and & dw_detbrow[ii_currformno
						 * ].GetItemnumber(ll_count,"max_day") = mmax_day and & trim
						 * (dw_detbrow[ii_currformno].GetItemString(ll_count, "ref_code")) =
						 * trim(ls_ref_code) then errcode = 'VTDLTERM2' end if Next
						 */
						System.out.println(
								"childNodeName[" + childNodeName + "]" + "minDay" + minDay + "maxDay" + maxDay);

						if (minDay == null || minDay == "") {
							minDay = "0";
						}

						if (maxDay == null || maxDay == "") {
							maxDay = "0";
						}
						System.out.println("Integer.parseInt(maxDay)[" + Integer.parseInt(maxDay) + "]"
								+ "Integer.parseInt(minDay)[" + Integer.parseInt(minDay) + "]");
						if (Integer.parseInt(maxDay) < Integer.parseInt(minDay)) {
							errcode = "VMMINDAY";
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							sql = " select count(*) from pord_dlv_term where  purc_order = ? "
									+ " and ? between min_day and max_day and " + "line_no <> ? and ref_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, purcOrder);
							pStmt.setString(2, minDay);
							pStmt.setString(3, lineNo);
							pStmt.setString(4, refCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt > 0) {
								errcode = "VTDLTERM2";
								if (errcode != null && errcode.trim().length() > 0) {
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("min_amt")) {

						String minAmt = genericUtility.getColumnValue("min_amt", dom);
						String maxAmt = genericUtility.getColumnValue("max_amt", dom);

						if (minAmt != null && Integer.parseInt(minAmt) < 0) {
							errcode = "VMMINAMT1";
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if (minAmt != null && maxAmt != null && Integer.parseInt(maxAmt) < Integer.parseInt(minAmt)) {
							errcode = "VMMINAMT";
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// START added by kailasg On 19-dec-2019 [:Data application error while taking purchase order for new division ]

					if(childNodeName.equalsIgnoreCase("fin_chg"))
					{
						financialCharge = checkNull(genericUtility.getColumnValue("fin_chg", dom));
						System.out.println("352fIN CH"+financialCharge);
						if(financialCharge == null || financialCharge.trim().length()==0)
						{
							errcode = "VMFICHNT1";
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// END added by kailasg On 19-dec-2019 [Data application error while taking purchase order for new division ]

				}

			}
			break;

			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			System.out.println("errListSize [" + errListSize + "] errFields size [" + errFields.size() + "]");
			if ((errList != null) && (errListSize > 0)) {
				System.out.println("Inside errList>" + errList);
				for (cnt = 0; cnt < errListSize; cnt++) {
					errcode = (String) errList.get(cnt);
					System.out.println("errcode :" + errcode);
					int pos = errcode.indexOf("~");
					System.out.println("pos :" + pos);
					if (pos > -1) {
						errcode = errcode.substring(0, pos);
					}

					System.out.println("error code is :" + errcode);
					errFldName = (String) errFields.get(cnt);
					System.out.println(" cnt [" + cnt + "] errcode [" + errcode + "] errFldName [" + errFldName + "]");
					if (errcode != null && errcode.trim().length() > 0) {
						errString = getErrorString(errFldName, errcode, userId);
						errorType = errorType(conn, errcode);
					}
					System.out.println("errorType :[" + errorType + "]errString[" + errString + "]");
					if (errString != null && errString.trim().length() > 0) {
						// if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) {
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}

		} catch (Exception e) {
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (pStmt != null) {
						pStmt.close();
						pStmt = null;
					}

					if (rs != null) {
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
			System.out.println(" < StarclubEligWorkIC > CONNECTION IS CLOSED");
		}

		System.out.println("ErrString ::[ " + errStringXml.toString() + " ]");
		return errStringXml.toString();
	}

	private int isItemSer(String table_name, String whrCondCol,
			String whrCondVal, Connection conn) {
		int count = 0;

		if (conn != null) {

			ResultSet rs = null;
			PreparedStatement pstmt = null;

			String sql = "select count(1) from " + table_name + " where "
					+ whrCondCol + " = ?";
			System.out.println("SQL in getDBRowCount method : " + sql);
			try {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, whrCondVal);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} catch (SQLException e) {
				System.out
				.println("SQL Exception In getDBRowCount method of ItemSerIC Class : "
						+ e.getMessage());
				e.printStackTrace();
			} catch (Exception ex) {
				System.out
				.println("Exception In getDBRowCount method of ItemSerIC Class : "
						+ ex.getMessage());
				ex.printStackTrace();
			}
		}

		return count;
	}

	private String checkrate(Document dom, Connection conn) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Inside checkrate ........");
		String errcode = "";
		String rate = "";
		String unitrate = "";
		String unitstd = "";
		String convrtuomstduom = "";
		String itemCode = "";
		String sitecode = "";
		String orddate = "";
		String lspurcorder = "";
		Date ldtorddate = null;
		int cnt = 0;
		double lctemprate = 0;
		String sql = null;
		String lsValue = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DistCommon DistCommon = new DistCommon();
		ArrayList ratestduomArr = new ArrayList();

		Timestamp orddate2 = null;

		try {

			lsValue = DistCommon.getDisparams("999999", "UOM_ROUND", conn);

			if ("NULLFOUND".equalsIgnoreCase(lsValue)) {
				errcode = "VTUOMVARPARM";
				// errcode = errcode +
				// "Variable UOM_ROUND not defined under Distribution Environment Variables";
				return errcode;
			}

			rate = checkNull(genericUtility.getColumnValue("rate", dom));
			unitrate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
			unitstd = checkNull(genericUtility.getColumnValue("unit__std", dom));
			itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
			convrtuomstduom = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
			sitecode = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
			orddate = checkNull(genericUtility.getColumnValue("ord_date", dom));

			System.out.println("lsValue" + lsValue + "rate" + rate + "unitrate" + unitrate + "unitstd" + unitstd
					+ "itemCode" + itemCode + "convrtuomstduom" + convrtuomstduom + "sitecode" + sitecode + "orddate"
					+ orddate);
			if (rate == null) {
				rate = "0";
			}
			if (convrtuomstduom == null) {
				convrtuomstduom = "0";
			}
			if (unitrate == null) {
				sql = "Select unit from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					unitrate = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			if ("R".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
				ratestduomArr = DistCommon.getConvQuantityFact(unitstd, unitrate, itemCode, Double.parseDouble(rate),
						Double.parseDouble(convrtuomstduom), conn);
			} else {
				ratestduomArr = DistCommon.getConvQuantityFact(unitstd, unitrate, itemCode, Double.parseDouble(rate),
						Double.parseDouble(convrtuomstduom), conn);
			}

			if (orddate != null && orddate.trim().length() > 0) {
				orddate2 = Timestamp.valueOf(genericUtility.getValidDateString(orddate,
						genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}

			sql = "select porddet.purc_order, porder.ord_date, porddet.rate__stduom  from porddet, porder  "
					+ " where ( porddet.purc_order = porder.purc_order ) and  ( ( porddet.site_code = ? ) and "
					+ " ( porddet.item_code = ? ) and ( porder.ord_date < ? ) ) order by porder.ord_date desc ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, sitecode);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, orddate2);
			// pstmt.setString(3, orddate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				lspurcorder = rs.getString(1);
				ldtorddate = rs.getDate(2);
				lctemprate = rs.getDouble(3);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			for (int i = 0; i < ratestduomArr.size(); ++i) {
				if (lctemprate < Double.parseDouble((String) ratestduomArr.get(i))) {
					//errcode = "VTPORATE1"; //commented by rupali on 03/03/2021 because of issue in process
					/*
					 * ~tLast Purchase Order : " + lspurcorder + " dated " + ldtorddate + "Item " +
					 * itemCode + "~r~nhas Rate/Std.UOM " + lctemprate + "~r~nin place of " +
					 * ratestduomArr;
					 */
				}
			}

		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return errcode;
	}

	private boolean isNumeric(String varValue) {
		// TODO Auto-generated method stub
		return varValue.matches("-?\\d+(\\.\\d+)?");

	}

	public String itemChanged() throws RemoteException, ITMException {
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext,
			String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try {
			System.out.println("xmlString::" + xmlString);
			dom = parseString(xmlString);
			System.out.println("xmlString1::" + xmlString1);
			dom1 = parseString(xmlString1);

			if (xmlString2.trim().length() > 0) {
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [StarclubEligWorkIC][itemChanged] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException {
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		PreparedStatement pStmt1 = null;
		PreparedStatement pStmtNew = null;
		ResultSet rs = null,rs1=null;
		ResultSet rsNew = null;
		String sql, lsnull = "";
		String loginSite = "";
		int currentFormNo = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = "";
		NodeList parentNodeList = null;
		Node parentNode = null;
		NodeList childNodeList = null;
		int childNodeListLength = 0;
		int ctr = 0;
		SimpleDateFormat sdf = null;
		Timestamp timestamp = null;
		String chguser = "";
		String emp = "";
		String mCurrency = "";
		String exchRate = "", exchRateSp = "";
		String taxEnv = "";
		String sysDate = "";
		String mcode = "", qtyStr = "", trandt = "";
		String sdescr = "";
		String sadd1 = "";
		String sadd2 = "";
		String scity = "";
		String sstancode = "";
		String dept = "";
		String descr1 = "";
		String descr2 = "";
		String descr3 = "";
		String descr4 = "";
		String suppCodePref = "";
		String empCodePur = "";
		String analCode = "";
		String taskCode = "";
		String saleOrder = "";
		String mDlvsite = "";
		String indNo = "";
		String pordType = "";
		String defSer = "";
		String locGroupJwiss = "";
		String tranboqId = "";
		String itemSer = "";
		String siteCodeDlv = "";
		String sitecodeBil = "";
		String errString = "";
		String descr = "";
		String lineNo = "";
		String mcrterm = "";
		String mcrdescr = "";
		String mcpercon = "";
		String mccode = "";
		String memp = "";
		String mval1 = "";
		String reStr = "";
		String payMode = "";
		String quotNo = "";
		String suppCode = "";
		String advPerc = "";
		String advType = "";
		String frtamtFixed = "";
		String frtamtQty = "";
		String frtamtTot = "";
		String bname = "";
		String acctNo = "";
		String taskDesc = "";
		String projCode = "";
		String projDescr = "";
		String mval2 = "";
		String preAssignLot = "";
		String preAssignExp = "";
		String bankCodeben = "";
		String ordDate = "";
		String commPerc = "";
		String frtType = "";
		String ratestduom = "";
		String cctrCode = "";
		String approxCost = "";
		String eou = "";
		String tranCode = "";
		String taxChap = "";
		String frtTerm = "";
		String dlvTerm = "";
		String crTerm = "";
		String frtAmt = "";
		String contractType = "";
		String priceList = "";
		String policyNo = "";
		String date = "";
		String purcOrder = "";
		String mfgitemDesc = "";
		String uom = "";
		String unitStd = "";
		String mVal1 = "";
		String lsValue = "";
		String lcConv = "";
		String contractNo = "";
		String empCode = "";
		String taxClass = "";
		String stationstanCode = "";
		String taxclassDes = "";
		String taxchapDes = "";
		String taxenvDes = "";
		String stationCodeto = "";
		String termTable = "";
		String currCode = "";
		String currCodeFr = ""; // Added by Nandkumar Gadkari on 17MAR2018
		String priceListClg = "";
		String ccode = "";
		String currCodefrt = "";
		String currCodefrtBase = "";
		String transMode = "";
		String tranName = "";
		String mhdrtot = "";
		String mhdrord = "";
		String frtRate = "";
		String bankcdPay = "";
		String acctCodeApAdv = "";
		String cctrCodeApAdv = "";
		String codeDescr = "";
		String invAcctPorcp = "";
		String mval = "";
		String rowCount = "";
		String itemCode = "";
		// String itemCode ="" ;
		String pendqty = "";
		String reqdate = "";
		String sitecodeDlv = "";
		String convQtystd = "";
		String specialInstr = "";
		String indRemarks = "";
		String workOrder = "";
		String acctCode = "";
		String specificInstr = "";
		String packInstr = "";
		String deptCode = "";
		String empCodeqcaprv = "";
		String cctr_code = "";
		String siteCode = "";
		String mdiscount = "";
		String mrate = "";
		String mdescr = "";
		String qty = "";
		String portType = "";
		String quantityFc = "";
		String rate = "";
		String mVal = "";
		String budgetAmtanal = "";
		String consumedAmtanal = "";
		String enqNo = "";
		String locCode = "";
		String packCode = "";
		String suppcodeMnfr = "";
		String saleItem = "";
		String saleQty = "";
		String saleUnit = "";
		String itemDescr = "";
		String itemLoc = "";
		String despDate = "";
		String lastPurcRate = "";
		String lastPurcPo = "";
		String convQtystduom = "";
		String stdRate = "";
		String actualCost = "";
		String costctrAsloccode = "";
		String costctr = "";
		String qcReqd = "";
		String CctrLoccode = "";
		String invAcctQcorder = "";
		String varValue = "";
		String unitRate = "";
		String unit = "";
		String ratestd = "";
		String qtystd = "";
		String Value = "";
		String taskCodeDescr = "", convRtuomStduom = "";
		// Modified by Rohini T on[08/05/19][start]
		String tranType = "";
		// Modified by Rohini T on[08/05/19][end]
		// Pavan R on 28aug18
		String quantityStduomStr = "";
		String rateStduomStr = "";
		String discStr = "";
		String taxAmtStr = "";
		double quantityStduom = 0;
		double rateStduom = 0;
		double disc = 0;
		double taxAmt = 0;
		double totAmount = 0;

		double exchRate1 = 0, advPercInt = 0, advance = 0, totAmt = 0, ordAmt = 0, frtRatedouble = 0, exchRatelc = 0;
		double convQtystdDob = 0, pendqtyDouble = 0, mrateDou = 0, budgetAmt = 0, convTemp = 0;
		double budgetAmtanal1 = 0, consumedAmtanal1 = 0, qtyTot = 0, stdCost = 0, stdCode = 0, varie = 0;
		double amt = 0, amtStd = 0, qtystdlc = 0, lcConvlc = 0, ratestdlc = 0, Ratelc = 0;
		double rateLc = 0.0, qtyLc = 0.0, qtystdLc = 0.0, ratestdLc = 0.0, ValueLc = 0.0, convQtystduomLc = 0.0,
				stdRateLc = 0.0, mrateLc = 0.0;
		ArrayList pendqtyArr = new ArrayList();
		ArrayList ratestduomArr = new ArrayList();
		ArrayList qtystduomArr = new ArrayList();

		int cnt = 0;
		int row = 0;
		int rowCountInt = 0;
		int pos = 0;
		int qty1 = 0;
		int quantityFc1 = 0;
		int totQtyperc = 0;
		int temp = 0;

		// cpatil start

		String ls_cont = "", ls_type = "", ls_sitecode = "", ls_ind_no = "", ls_item_ser = "", item_code__mfg = "";
		String mloc = "", ls_invacct = "", errstr = "", ls_qcreqd = "", mloc__aprv = "", mloc__insp = "";
		String ls_unitpur = "", ls_pack1 = "", ls_packinstr1 = "", ls_itemser = "", ls_rate_unit = "",
				ls_emp_code__qcaprv = "";
		String ls_empfname = "", ls_empmname = "", ls_emplname = "", ls_supp_code__mnfr = "", ls_item_code = "",
				ls_taxclasshdr = "", ls_taxchaphdr = "";
		String ldt_orddateStr = "", ls_pricelist = "", ls_unitstd = "";
		ArrayList lc_qtystduom = null;
		// Change by Pavan R 1/DEC/17 Start
		// ArrayList<String> lc_ratestduomArrayList = new ArrayList<String>();
		ArrayList lc_ratestduomArrayList = new ArrayList();
		// Change by Pavan R 1/DEC/17 End
		double mNum = 0, mNum1 = 0, mNum2 = 0, mNum3 = 0, lc_stdrate = 0;
		double lc_disc = 0, lc_rate__clg = 0;
		double lc_qty = 0, lc_sale_qty = 0, lc_temp = 0, lc_qtystd = 0, ld_std_code = 0, lc_actual_cost = 0;
		double lc_rate = 0, lc_ratestduom = 0, lc_amt = 0, lc_amt_std = 0, lc_ratestd = 0, mVal5 = 0;
		double lc_conv = 0, lc_convtemp = 0;
		double lc_exch_rate = 0;

		String macct = "", mcctr = "", ls_enq = "", ls_pack = "", ls_supp_mnfr = "";
		String ls_eou = "", ls_site = "", ls_taxclass = "", ls_taxchap = "", ls_taxenv = "", ls_disc_type = "",
				ls_acct_dr = "", ls_cctr_dr = "";
		String ls_acct_cr = "", ls_cctr_cr = "", ls_bom = "", ls_site_dlv = "", ls_special_instr = "",
				ls_specific_instr = "";
		String ls_item = "", ls_sale_unit = "", ls_item_descr = "", ls_loc = "", ls_ind_remarks = "", ls_descr = "";
		String ls_acct_ap = "", ls_ind_val = "", ls_packinstr = "", ls_code = "";
		String ls_projcode = "", ls_itemCode = "", ls_suppcode = "", ls_itemref = "";
		String ls_stationfr = "", ls_stationto = "";
		String ls_qc_aprv_name = "", ls_mfg_item_cd = "", ls_mfg_item_des = "", ls_acct_prov = "";
		String ls_work_order = "", ls_price_list__clg = "", ls_dept_code = "", ls_var_value = "", ls_invacct_qc = "";
		String ls_taxenvhdr = "", ls_proj_code = "", mval3 = "", ls_indno = "", ls_costctr = "",
				ls_costctr_as_loccode = "", ls_qc_reqd = "", ls_cctr_loccode = "";

		double ll_cnt = 0, ll_row = 0, ll_findstr = 0;
		Timestamp ld_desp_date = null, ldt_orddate = null, ld_date = null;
		String ld_desp_dateStr = "", Uom = "";

		String ls_Cctr_Loccode = "";
		double li_line_no = 0;
		String ls_null = "";
		String ls_line_no = "", invAcctSer = "";

		// cpatil end

		Timestamp ordDate2 = null;

		String queryStdoum = "";// Added by Anjali R. on[13/03/2018]
		UtilMethods utilMethods = UtilMethods.getInstance();

		try {
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			DistCommon distComm = new DistCommon();
			FinCommon finCommon = new FinCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			java.util.Date today = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			today = cal.getTime();
			DecimalFormat df = new DecimalFormat("#.#####");
			lsValue = distComm.getDisparams("999999", "UOM_ROUND", conn);
			System.out.println("@@@@@@@@ lsValue[" + lsValue + "]");
			if (lsValue.equals("NULLFOUND")) {
				errString = itmDBAccessLocal.getErrorString("", "VTUOMVARPARM", "", "", conn);
				return errString;
			}

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(today);

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			chguser = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			empCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");

			// chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());
			date = (sdf.format(timestamp).toString()).trim();
			System.out.println("loginSite[" + loginSite + "][chguserhdr " + chguser + "][ld_date" + date + "]");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");

			System.out.println("Current Form No [" + currentFormNo + "]");
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equals("itm_default")) {
					System.out.println("Inside itm_default...");

					sql = "select descr, add1, add2, city, stan_code from site where site_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, loginSite);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						sdescr = checkNull(rs.getString("descr"));
						sadd1 = checkNull(rs.getString("add1"));
						sadd2 = checkNull(rs.getString("add2"));
						scity = checkNull(rs.getString("city"));
						sstancode = checkNull(rs.getString("stan_code"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = "select emp_code from users where code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, chguser);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						emp = checkNull(rs.getString("emp_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = "select dept_code from employee where emp_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, emp);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						dept = checkNull(rs.getString("dept_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = "select descr from department where dept_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, dept);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					/*
					 * sql =
					 * "select Site_code__dlv,Site_code__ord, Status, Curr_code__purc, Tax_opt, Single_Ser from PurcCtrl"
					 * ; pStmt = conn.prepareStatement(sql); rs = pStmt.executeQuery(); if
					 * (rs.next()) { mDlvsite = checkNull(rs.getString("Site_code__dlv")); mCurrency
					 * = checkNull(rs.getString("Curr_code__purc")); } rs.close(); rs = null;
					 * pStmt.close(); pStmt = null;
					 */

					/*
					 * sql = "select std_exrt from Currency where Curr_code = ?"; pStmt =
					 * conn.prepareStatement(sql); pStmt.setString(1, mCurrency); rs =
					 * pStmt.executeQuery(); if (rs.next()) {
					 */
					// exchRate = checkNull(rs.getString("std_exrt"));
					// exchRate = fincommon.getDailyExchRateSellBuy(currcode, lscurrcodebase,
					// sitecode, trandt, "B", conn);

					/*
					 * } rs.close(); rs = null; pStmt.close(); pStmt = null;
					 */

					sql = "select count(1) as cnt from gencodes where fld_name = 'PORD_TYPE' "
							+ "and ltrim(rtrim(fld_value)) = 'R'";
					pStmt = conn.prepareStatement(sql);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");

					}
					if (cnt > 0) {
						valueXmlString.append("<pord_type>").append("<![CDATA[" + "R" + "]]>").append("</pord_type>");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<dept_code>").append("<![CDATA[" + dept + "]]>").append("</dept_code>");
					valueXmlString.append("<department_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</department_descr>");
					valueXmlString.append("<site_code__dlv>").append("<![CDATA[" + loginSite + "]]>")
					.append("</site_code__dlv>");
					valueXmlString.append("<site_code__ord>").append("<![CDATA[" + loginSite + "]]>")
					.append("</site_code__ord>");
					valueXmlString.append("<site_code__bill>").append("<![CDATA[" + loginSite + "]]>")
					.append("</site_code__bill>");
					valueXmlString.append("<curr_code>").append("<![CDATA[" + mCurrency + "]]>").append("</curr_code>");
					valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + mCurrency + "]]>")
					.append("</curr_code__comm>");
					valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + mCurrency + "]]>")
					.append("</curr_code__frt>");

					// itemChanged(dom,dom1,dom2,objContext,"Curr_code__frt",editFlag,xtraParams);

					valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + mCurrency + "]]>")
					.append("</curr_code__ins>");
					valueXmlString.append("<tax_Opt>").append("<![CDATA[" + "L" + "]]>").append("</tax_Opt>");
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
					valueXmlString.append("<ord_date>").append("<![CDATA[" + date + "]]>").append("</ord_date>");
					valueXmlString.append("<status_date>").append("<![CDATA[" + date + "]]>").append("</status_date>");
					valueXmlString.append("<tax_date>").append("<![CDATA[" + date + "]]>").append("</tax_date>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + sdescr + "]]>").append("</site_descr>");
					valueXmlString.append("<site_add1>").append("<![CDATA[" + sadd1 + "]]>").append("</site_add1>");
					valueXmlString.append("<site_add2>").append("<![CDATA[" + sadd2 + "]]>").append("</site_add2>");
					valueXmlString.append("<city__site>").append("<![CDATA[" + scity + "]]>").append("</city__site>");
					valueXmlString.append("<site_stan_code>").append("<![CDATA[" + sstancode + "]]>")
					.append("</site_stan_code>");
					valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + mCurrency + "]]>")
					.append("</curr_code__ins>");
					valueXmlString.append("<ref_date>").append("<![CDATA[" + date + "]]>").append("</ref_date>");
					valueXmlString.append("<emp_code>").append("<![CDATA[" + empCode + "]]>").append("</emp_code>");
					valueXmlString.append("<adv_perc protect = '1'>").append("").append("</adv_perc>");
					valueXmlString.append("<ind_no protect = '0'>").append("").append("</ind_no>");
					valueXmlString.append("<site_code__dlv protect = '0'>").append("<![CDATA[" + loginSite + "]]>")
					.append("</site_code__dlv>");
					valueXmlString.append("<proj_code protect = '0'>").append("").append("</proj_code>");
					valueXmlString.append("<adv_perc>").append("0.000").append("</adv_perc>");
					valueXmlString.append("<bank_code__pay protect = '1'>").append("").append("</bank_code__pay>");

					valueXmlString.append("<status protect = '0'>").append("O").append("</status>");

					cnt = 0;
					sql = "select count(1) as cnt from proj_est_milestone";
					pStmt = conn.prepareStatement(sql);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");

					}
					if (cnt > 0) {
						valueXmlString.append("<task_code protect = '0'>").append("").append("</task_code>");
					} else {
						valueXmlString.append("<task_code protect = '1'>").append("").append("</task_code>");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
				}
				if (currentColumn.trim().equals("itm_defaultedit")) {

					itemSer = genericUtility.getColumnValue("item_ser", dom);
					purcOrder = genericUtility.getColumnValue("purc_order", dom);

					valueXmlString.append("<item_ser protect = '1'>").append("<![CDATA[" + itemSer + "]]>")
					.append("</item_ser>");
					sql = "select count(*) from porddet where purc_order = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, purcOrder);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						row = rs.getInt(1);

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					System.out.println("row>>>" + row);
					if (row > 0) {
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));

						valueXmlString.append("<ind_no protect = '1'>").append("<![CDATA[" + indNo + "]]>")
						.append("</ind_no>");
						valueXmlString.append("<quot_no protect = '1'>").append("<![CDATA[" + quotNo + "]]>")
						.append("</quot_no>");
						valueXmlString.append("<contract_no protect = '1'>").append("<![CDATA[" + contractNo + "]]>")
						.append("</contract_no>");
						// valueXmlString.append("<proj_code protect = '1'>") //Pavan R 23Jul19[to made
						// proj_code editable]
						valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");
						valueXmlString.append("<site_code__dlv protect = '1'>")
						.append("<![CDATA[" + siteCodeDlv + "]]>").append("</site_code__dlv>");

					} else {
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));

						valueXmlString.append("<site_code__dlv protect = '0'>")
						.append("<![CDATA[" + siteCodeDlv + "]]>").append("</site_code__dlv>");

						if (!(indNo == null || indNo.trim().length() == 0)) {
							valueXmlString.append("<ind_no protect = '0'>").append("<![CDATA[" + indNo + "]]>")
							.append("</ind_no>");
							valueXmlString.append("<quot_no protect = '1'>").append("<![CDATA[" + quotNo + "]]>")
							.append("</quot_no>");
							valueXmlString.append("<contract_no protect = '1'>")
							.append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							// valueXmlString.append("<proj_code protect = '1'>") //Pavan R 23Jul19[to made
							// proj_code editable]
							valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code>");
						} else if (!(quotNo == null || quotNo.trim().length() == 0)) {
							valueXmlString.append("<ind_no protect = '1'>").append("<![CDATA[" + indNo + "]]>")
							.append("</ind_no>");
							valueXmlString.append("<quot_no protect = '0'>").append("<![CDATA[" + quotNo + "]]>")
							.append("</quot_no>");
							valueXmlString.append("<contract_no protect = '1'>")
							.append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							// valueXmlString.append("<proj_code protect = '1'>") //Pavan R 23Jul19[to made
							// proj_code editable]
							valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code>");
						} else if (!(contractNo == null || contractNo.trim().length() == 0)) {
							valueXmlString.append("<ind_no protect = '1'>").append("<![CDATA[" + indNo + "]]>")
							.append("</ind_no>");
							valueXmlString.append("<quot_no protect = '1'>").append("<![CDATA[" + quotNo + "]]>")
							.append("</quot_no>");
							valueXmlString.append("<contract_no protect = '0'>")
							.append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							// valueXmlString.append("<proj_code protect = '1'>") //Pavan R 23Jul19[to made
							// proj_code editable]
							valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code>");
						} else if (!(projCode == null || projCode.trim().length() == 0)) {
							valueXmlString.append("<ind_no protect = '1'>").append("<![CDATA[" + indNo + "]]>")
							.append("</ind_no>");
							valueXmlString.append("<quot_no protect = '1'>").append("<![CDATA[" + quotNo + "]]>")
							.append("</quot_no>");
							valueXmlString.append("<contract_no protect = '1'>")
							.append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code>");
						} else {
							valueXmlString.append("<ind_no protect = '0'>").append("<![CDATA[" + indNo + "]]>")
							.append("</ind_no>");
							valueXmlString.append("<quot_no protect = '0'>").append("<![CDATA[" + quotNo + "]]>")
							.append("</quot_no>");
							valueXmlString.append("<contract_no protect = '0'>")
							.append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code>");
						}
					}

					exchRate = checkNull(genericUtility.getColumnValue("exch_rate", dom));
					exchRateSp = checkNull(genericUtility.getColumnValue("exch_rate__sp", dom));
					frtRate = checkNull(genericUtility.getColumnValue("frt_rate", dom));

					valueXmlString.append("<exch_rate protect = '1'>").append("<![CDATA[" + exchRate + "]]>")
					.append("</exch_rate>");
					valueXmlString.append("<exch_rate__sp protect = '1'>").append("<![CDATA[" + exchRateSp + "]]>")
					.append("</exch_rate__sp>");
					valueXmlString.append("<frt_rate protect = '1'>").append("<![CDATA[" + frtRate + "]]>")
					.append("</frt_rate>");

					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));

					sql = "select pay_mode from supplier where supp_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, suppCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						payMode = rs.getString("pay_mode");

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (payMode == null || payMode.trim().length() == 0 || (!"E".equalsIgnoreCase(payMode)
							&& !"N".equalsIgnoreCase(payMode) && !"G".equalsIgnoreCase(payMode))) {
						valueXmlString.append("<bank_code__pay protect = '1'>").append("").append("</bank_code__pay>");
					}

					sql = "select count(*) as cnt from proj_est_milestone ";
					pStmt = conn.prepareStatement(sql);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");

					}
					if (cnt > 0) {
						valueXmlString.append("<task_code protect = '0'>").append("").append("</task_code>");
					} else {
						valueXmlString.append("<task_code protect = '0'>").append("").append("</task_code>");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

				}
				if (currentColumn.trim().equals("pord_type")) {
					pordType = checkNull(genericUtility.getColumnValue("pord_type", dom));

					if (pordType != null && "A".equalsIgnoreCase(pordType.trim())) {
						sql = "select udf_str1  from gencodes where fld_name = 'PORD_TYPE' AND fld_value ='A' ";
						pStmt = conn.prepareStatement(sql);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							defSer = rs.getString("udf_str1");
							valueXmlString.append("<item_ser>").append("<![CDATA[" + defSer + "]]>")
							.append("</item_ser>");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					} else {
						// valueXmlString.append("<item_ser>").append("<![CDATA["
						// + lsnull + "]]>").append("</item_ser>");
						valueXmlString.append("<item_ser>").append("").append("</item_ser>");
					}

					sql = "select loc_group__jwiss  from pordertype where ORDER_TYPE = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, pordType);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						locGroupJwiss = checkNull(rs.getString("loc_group__jwiss"));
						valueXmlString.append("<loc_group__jwiss>").append("<![CDATA[" + locGroupJwiss + "]]>")
						.append("</loc_group__jwiss>");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
				}
				if (currentColumn.trim().equals("ind_no")) {
					mcode = checkNull(genericUtility.getColumnValue("ind_no", dom));
					contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
					quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
					projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));

					if ((mcode != null && mcode.trim().length() > 0)) {
						sql = "select count(*) as cnt from boqhdr where  indent_no = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							row = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (row > 0) {
							sql = "select tran_id from boqhdr where  indent_no = ? and status <> 'X'";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, mcode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								tranboqId = rs.getString("tran_id");
								valueXmlString.append("<tran_id__boq>").append("<![CDATA[" + tranboqId + "]]>")
								.append("</tran_id__boq>");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}

						valueXmlString.append("<contract_no protect = '1'>").append("<![CDATA[" + contractNo + "]]>")
						.append("</contract_no>");
						valueXmlString.append("<quot_no protect = '1'>").append("<![CDATA[" + quotNo + "]]>")
						.append("</quot_no>");
						valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");// unprotecte by nandkumar gadkari on 14/05/19

						sql = "select  b.item_ser item_ser,a.site_code__dlv site_code__dlv, a.supp_code__pref supp_code__pref, "
								+ "a.emp_code__pur emp_code__pur, a.SITE_CODE__BIL SITE_CODE__BIL, proj_code proj_code, a.anal_code anal_code"
								+ " from indent a, item b where a.item_code = b.item_code and a.ind_no = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							itemSer = checkNull(rs.getString("item_ser"));
							siteCodeDlv = checkNull(rs.getString("site_code__dlv"));
							suppCodePref = checkNull(rs.getString("supp_code__pref"));
							empCodePur = checkNull(rs.getString("emp_code__pur"));
							sitecodeBil = checkNull(rs.getString("SITE_CODE__BIL"));
							projCode = checkNull(rs.getString("proj_code"));
							analCode = checkNull(rs.getString("anal_code"));
						}

						valueXmlString.append("<proj_code>").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");
						valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
						valueXmlString.append("<site_code__dlv>").append("<![CDATA[" + siteCodeDlv + "]]>")
						.append("</site_code__dlv>");
						// dw_edit.SetColumn("site_code__dlv")
						// gbf_itemchangedhdr("site_code__dlv")
						valueXmlString.append("<anal_code>").append("<![CDATA[" + analCode + "]]>")
						.append("</anal_code>");

						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (suppCodePref != null && suppCodePref.trim().length() > 0) {
							valueXmlString.append("<supp_code>").append("<![CDATA[" + suppCodePref + "]]>")
							.append("</supp_code>");
							// gbf_itemchangedhdr("supp_code");
						}
						if (empCodePur != null && empCodePur.trim().length() > 0) {
							valueXmlString.append("<emp_code>").append("<![CDATA[" + empCodePur + "]]>")
							.append("</emp_code>");
						}
						if (sitecodeBil != null && sitecodeBil.trim().length() > 0) {
							valueXmlString.append("<site_code__bill>").append("<![CDATA[" + sitecodeBil + "]]>")
							.append("</site_code__bill>");
						}
						// dw_edit.setcolumn("ref_date")
					} else {
						valueXmlString.append("<contract_no protect = '0'>").append("<![CDATA[" + contractNo + "]]>")
						.append("</contract_no>");
						valueXmlString.append("<quot_no protect = '0'>").append("<![CDATA[" + quotNo + "]]>")
						.append("</quot_no>");
						valueXmlString.append("<proj_code >").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");
						// valueXmlString.append("<proj_code protect =
						// '0'>").append("").append("</proj_code>");

					}
					if ((mcode != null && mcode.trim().length() > 0)) {
						sql = "select task_code from indent where ind_no = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							taskCode = rs.getString("task_code");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ((taskCode != null && taskCode.trim().length() > 0)) {
							sql = "select task_desc from proj_est_milestone where task_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taskCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								taskDesc = rs.getString("task_code");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							valueXmlString.append("<task_code>").append("<![CDATA[" + taskCode + "]]>")
							.append("</task_code>");
							valueXmlString.append("<task_desc>").append("<![CDATA[" + taskDesc + "]]>")
							.append("</task_desc>");
						}
						valueXmlString.append("<task_code protect = '1'>").append("").append("</task_code>");
					}
				}
				if (currentColumn.trim().equals("item_ser")) {
					mcode = genericUtility.getColumnValue("item_ser", dom);
					empCode = genericUtility.getColumnValue("emp_code", dom, "1");

					//if (empCode.trim().length() == 0 || empCode == null)
					if (empCode == null || empCode.trim().length() == 0 )
					{
						sql = "select emp_code__pur from itemser where item_ser = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							memp = rs.getString("emp_code__pur");
						}
						if (memp != null && memp.trim().length() > 0) {
							valueXmlString.append("<emp_code>").append("<![CDATA[" + memp + "]]>")
							.append("</emp_code>");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}

					taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
					taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					stationstanCode = checkNull(genericUtility.getColumnValue("station_stan_code", dom));

					sql = "select stan_code  from site where site_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						stationCodeto = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("A".equalsIgnoreCase(editFlag) && suppCode.trim().length() > 0) {
						if (taxClass == null || taxClass.trim().length() == 0) {
							// taxclassDes =
							// gf_get_taxclass('S',suppCode,"",siteCodeDlv)
							taxclassDes = distComm.getTaxClass("S", suppCode, "", siteCodeDlv, conn);
						} else {
							taxclassDes = taxClass;
						}
						if (taxChap == null || taxChap.trim().length() == 0) {
							// taxchapDes = gf_get_taxchap("", itemSer, 'S',
							// suppCode,siteCodeDlv)
							taxchapDes = distComm.getTaxChap("", itemSer, "S", suppCode, siteCodeDlv, conn);
						} else {
							taxchapDes = taxChap;
						}
						if (taxEnv == null || taxEnv.trim().length() == 0) {
							// taxenvDes = gf_get_taxenv(stationstanCode,
							// stationCodeto, taxchapDes,
							// taxclassDes,siteCodeDlv)
							taxenvDes = distComm.getTaxEnv(stationstanCode, stationCodeto, taxchapDes, taxclassDes,
									siteCodeDlv, conn);
						} else {
							taxenvDes = taxEnv;
						}

						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxclassDes + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxchapDes + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxenvDes + "]]>").append("</tax_env>");
					}

					sql = "select term_table from itemser where item_ser = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						// Chananged By Nasruddin checkNull condition 14-10-16
						// termTable = rs.getString("term_table");
						termTable = checkNull(rs.getString("term_table"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					System.out.println("termTable===>> [" + termTable + "]");
					// Chananged By Nasruddin 14-10-16
					// if (termTable.length() > 0 || termTable != null)
					if (termTable.trim().length() > 0 && termTable != null) {
						valueXmlString.append("<term_table>").append("<![CDATA[" + termTable + "]]>")
						.append("</term_table>");
					}
				}
				if (currentColumn.trim().equals("supp_code")) {
					mcode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					trandt = genericUtility.getColumnValue("ord_date", dom);

					sql = "select cr_term, curr_code from site_supplier " + " where supp_code = ? and site_code =  ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					pStmt.setString(2, siteCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						mcrterm = checkNull(rs.getString("cr_term"));
						currCode = checkNull(rs.getString("curr_code"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					/*
					 * Changed By PriyankaC on 29DEC2018.. sql =
					 * "select supp_name, addr1, addr2, city, stan_code, tran_code , price_list, price_list__clg, cr_term, curr_code "
					 * + "from supplier where supp_code = ? ";
					 */
					sql = "select supp_name, addr1, addr2, city, stan_code, tran_code , price_list, price_list__clg, cr_term, curr_code , dlv_term "
							+ " from supplier where supp_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString("supp_name"));
						descr1 = checkNull(rs.getString("addr1"));
						descr2 = checkNull(rs.getString("addr2"));
						descr3 = checkNull(rs.getString("city"));
						descr4 = checkNull(rs.getString("stan_code"));
						tranCode = checkNull(rs.getString("tran_code"));
						priceList = checkNull(rs.getString("price_list"));
						priceListClg = checkNull(rs.getString("price_list__clg"));
						crTerm = checkNull(rs.getString("cr_term"));
						ccode = checkNull(rs.getString("curr_code"));
						dlvTerm = checkNull(rs.getString("dlv_term"));

					}
					System.out.println("dlvTerm : " + dlvTerm);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (mcrterm == null || mcrterm.trim().length() == 0) {
						mcrterm = crTerm;
					}
					if (currCode == null || currCode.trim().length() == 0) {
						currCode = ccode;
					}

					sql = "select descr from crterm where cr_term = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcrterm);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						mcrdescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (tranCode != null && tranCode.trim().length() > 0) {
						sql = "select frt_term, curr_code from transporter where tran_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, tranCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							frtTerm = checkNull(rs.getString("frt_term"));
							currCodeFr = checkNull(rs.getString("curr_code"));// Changes by Nandkumar Gadkari on
							// 17MAR2018
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						/*
						 * exchRatelc = finCommon.getDailyExchRateSellBuy(currCode, "", siteCodeDlv,
						 * trandt, "B", conn);
						 */
						valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + getAbsString(currCodeFr) + "]]>")
						.append("</curr_code__frt>"); // Changes by Nandkumar Gadkari on 17MAR2018
						setNodeValue(dom, "cust_code__dlv", getAbsString(currCode));
						reStr = itemChanged(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangedhdr("Curr_code__frt")
						valueXmlString.append("<frt_term>").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");
					} else {
						valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + getAbsString(currCode) + "]]>")
						.append("</curr_code__frt>");
						setNodeValue(dom, "cust_code__dlv", getAbsString(currCode));
						reStr = itemChanged(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangedhdr("Curr_code__frt")
						valueXmlString.append("<frt_term>").append("B").append("</frt_term>");
					}
					valueXmlString.append("<supp_name>").append("<![CDATA[" + descr + "]]>").append("</supp_name>");
					valueXmlString.append("<supplier_addr1>").append("<![CDATA[" + descr1 + "]]>")
					.append("</supplier_addr1>");
					valueXmlString.append("<supplier_addr2>").append("<![CDATA[" + descr2 + "]]>")
					.append("</supplier_addr2>");
					valueXmlString.append("<supplier_city>").append("<![CDATA[" + descr3 + "]]>")
					.append("</supplier_city>");
					valueXmlString.append("<station_stan_code>").append("<![CDATA[" + descr4 + "]]>")
					.append("</station_stan_code>");
					valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrterm + "]]>").append("</cr_term>");
					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + mcrdescr + "]]>")
					.append("</crterm_descr>");
					valueXmlString.append("<tran_code>").append("<![CDATA[" + getAbsString(tranCode) + "]]>")
					.append("</tran_code>");
					// Commented by Nandkumar Gadkari on 17MAR2018
					/*
					 * valueXmlString.append("<exch_rate>") .append("<![CDATA[" + exchRatelc +
					 * "]]>") .append("</exch_rate>");
					 */
					// Added By PriyankaC on 29JAN2018..[START]
					valueXmlString.append("<dlv_term>").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term>");
					// Added By PriyankaC on 29JAN2018..[END]

					setNodeValue(dom, "tran_code", getAbsString(tranCode));
					reStr = itemChanged(dom, dom1, dom2, objContext, "tran_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					// gbf_itemchangedhdr("tran_code")

					valueXmlString.append("<curr_code>").append("<![CDATA[" + getAbsString(currCode) + "]]>")
					.append("</curr_code>");
					setNodeValue(dom, "curr_code", getAbsString(currCode));
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					// gbf_itemchangedhdr("curr_code")
					//// added by Nandkumar Gadkari on 17MAR2018------------start-------
					exchRatelc = finCommon.getDailyExchRateSellBuy(currCode, "", siteCodeDlv, trandt, "B", conn);

					valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRatelc + "]]>")
					.append("</exch_rate>");
					System.out.println("exchRatelc::: : " + exchRatelc);
					// added by Nandkumar Gadkari on 16MAR2018------------end-------

					valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + getAbsString(currCode) + "]]>")
					.append("</curr_code__comm>");
					setNodeValue(dom, "curr_code__comm", getAbsString(currCode));
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__comm", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					// gbf_itemchangedhdr("curr_code__comm")

					if (genericUtility.getColumnValue("price_list", dom) == null
							|| (genericUtility.getColumnValue("price_list", dom)).trim().length() == 0) {
						valueXmlString.append("<price_list>").append("<![CDATA[" + priceList + "]]>")
						.append("</price_list>");
					}
					taxClass = genericUtility.getColumnValue("tax_class", dom);
					taxChap = genericUtility.getColumnValue("tax_chap", dom);
					taxEnv = genericUtility.getColumnValue("tax_env", dom);
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					suppCode = genericUtility.getColumnValue("supp_code", dom);
					siteCodeDlv = genericUtility.getColumnValue("site_code__dlv", dom);
					stationstanCode = genericUtility.getColumnValue("station_stan_code", dom);

					// Changes by mayur on 16-May-2018---[start]
					sql = "select tax_class from site_supplier where site_code = ?" + " and supp_code = ? ";

					System.out.println("SQL::" + sql);
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					pStmt.setString(2, suppCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						taxclassDes = rs.getString("tax_class") == null ? "" : rs.getString("tax_class");
						System.out.println("@@taxclassDes in site_supplier@@" + taxclassDes);

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (taxclassDes == null || taxclassDes.trim().length() == 0) {
						sql = "select tax_class from supplier where supp_code = ? ";

						System.out.println("SQL::" + sql);
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, suppCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							taxclassDes = rs.getString("tax_class") == null ? "" : rs.getString("tax_class");
							System.out.println("@@taxclassDes in supplier@@" + taxclassDes);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}

					sql = "select tax_env from site_supplier where site_code = ?" + " and supp_code = ? ";

					System.out.println("SQL::" + sql);
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					pStmt.setString(2, suppCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						taxenvDes = rs.getString("tax_env") == null ? "" : rs.getString("tax_env");
						System.out.println("@@taxenvDes in site_supplier@@" + taxenvDes);

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (taxenvDes == null || taxenvDes.trim().length() == 0) {
						sql = "select tax_env from supplier where supp_code = ? ";

						System.out.println("SQL::" + sql);
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, suppCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {

							taxenvDes = rs.getString("tax_env") == null ? "" : rs.getString("tax_env");
							System.out.println("@@taxenvDes in supplier@@" + taxenvDes);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}
					// Changes by mayur on 16-May-2018---[end]

					sql = "select stan_code from site where site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						stationCodeto = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("A".equalsIgnoreCase(editFlag)) {

						if (taxclassDes == null || taxclassDes.trim().length() == 0) {
							// taxclassDes =
							// gf_get_taxclass('S',suppCode,"",siteCodeDlv)
							taxclassDes = checkNull(distComm.getTaxClass("S", suppCode, "", siteCodeDlv, conn));
							System.out.println("@@taxclassDes from distComm@@" + taxclassDes);
						}

						if (taxChap == null || taxChap.trim().length() == 0) {
							// taxchapDes = gf_get_taxchap("", itemSer, 'S',
							// suppCode,siteCodeDlv)
							taxchapDes = distComm.getTaxChap("", itemSer, "S", suppCode, siteCodeDlv, conn);
						} else {
							taxchapDes = taxChap;
						}

						if (taxenvDes == null || taxenvDes.trim().length() == 0) {
							// taxenvDes = gf_get_taxenv(stationstanCode,
							// stationCodeto, taxchapDes,
							// taxclassDes,siteCodeDlv)
							taxenvDes = checkNull(distComm.getTaxEnv(stationstanCode, stationCodeto, taxchapDes,
									taxclassDes, siteCodeDlv, conn));
							System.out.println("@@taxenvDes from distComm@@" + taxenvDes);
						}

						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxclassDes + "]]>")
						.append("</tax_class>");

						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxchapDes + "]]>")
						.append("</tax_chap>");

						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxenvDes + "]]>").append("</tax_env>");
					}

					valueXmlString.append("<price_list__clg>").append("<![CDATA[" + priceListClg + "]]>")
					.append("</price_list__clg>");

					sql = "select pay_mode from supplier where supp_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						payMode = rs.getString("pay_mode");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (payMode != null && payMode.trim().length() > 0 && ("E".equalsIgnoreCase(payMode)
							|| "G".equalsIgnoreCase(payMode) || "N".equalsIgnoreCase(payMode))) {
						valueXmlString.append("<bank_code__pay protect = '0'>").append("").append("</bank_code__pay>");
					} else {
						valueXmlString.append("<bank_code__pay>").append("").append("</bank_code__pay>");
						valueXmlString.append("<bank_name__ben>").append("").append("</bank_name__ben>");
						valueXmlString.append("<bank_acct_no__ben>").append("").append("</bank_acct_no__ben>");
						valueXmlString.append("<bank_code__pay protect = '1'>").append("").append("</bank_code__pay>");
					}

					sql = "select count(*) as cnt from supplier_bank where curr_code = ? and supp_code= ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, currCode);
					pStmt.setString(2, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (cnt == 1) {
						sql = "select bank_code__ben from supplier_bank where curr_code = ? and supp_code= ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, currCode);
						pStmt.setString(2, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							bankCodeben = rs.getString("bank_code__ben");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					} else {
						bankCodeben = "";
					}

					valueXmlString.append("<bank_code__ben>").append("<![CDATA[" + bankCodeben + "]]>")
					.append("</bank_code__ben>");

				}
				if (currentColumn.trim().equals("site_code__dlv")) {
					mcode = genericUtility.getColumnValue("site_code__dlv", dom);

					System.out.println("site_code__dlv mcode" + mcode);

					sql = "select descr, add1, add2, city, stan_code from site where site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString("descr"));
						descr1 = checkNull(rs.getString("add1"));
						descr2 = checkNull(rs.getString("add2"));
						descr3 = checkNull(rs.getString("city"));
						descr4 = checkNull(rs.getString("stan_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<site_descr>").append("<![CDATA[" + descr + "]]>").append("</site_descr>");
					valueXmlString.append("<site_add1>").append("<![CDATA[" + descr1 + "]]>").append("</site_add1>");
					valueXmlString.append("<site_add2>").append("<![CDATA[" + descr2 + "]]>").append("</site_add2>");
					valueXmlString.append("<city__site>").append("<![CDATA[" + descr3 + "]]>").append("</city__site>");
					valueXmlString.append("<site_stan_code>").append("<![CDATA[" + descr4 + "]]>")
					.append("</site_stan_code>");
					valueXmlString.append("<site_code__bill>").append("<![CDATA[" + mcode + "]]>")
					.append("</site_code__bill>");

					taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
					taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					stationstanCode = checkNull(genericUtility.getColumnValue("station_stan_code", dom));

					sql = "select stan_code from site where site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						stationCodeto = checkNull(rs.getString("stan_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("A".equalsIgnoreCase(editFlag)) {
						if (taxClass == null || taxClass.trim().length() == 0) {
							// taxclassDes =
							// gf_get_taxclass('S',suppCode,"",siteCodeDlv)
							taxclassDes = distComm.getTaxClass("S", suppCode, "", siteCodeDlv, conn);
						} else {
							taxclassDes = taxClass;
						}
						if (taxChap == null || taxChap.trim().length() == 0) {
							// taxchapDes = gf_get_taxchap('', itemSer, 'S',
							// suppCode,siteCodeDlv)
							taxchapDes = distComm.getTaxChap("", itemSer, "S", suppCode, siteCodeDlv, conn);
						} else {
							taxchapDes = taxChap;
						}
						if (taxEnv == null || taxEnv.trim().length() == 0) {
							// taxenvDes = gf_get_taxenv(stationstanCode,
							// stationCodeto, taxchapDes,
							// taxclassDes,siteCodeDlv)
							taxenvDes = distComm.getTaxEnv(stationstanCode, stationCodeto, taxchapDes, taxclassDes,
									siteCodeDlv, conn);
						} else {
							taxenvDes = taxEnv;
						}

						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxclassDes + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxchapDes + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxenvDes + "]]>").append("</tax_env>");
					}

				}
				if (currentColumn.trim().equals("dept_code")) {
					mcode = checkNull(genericUtility.getColumnValue("dept_code", dom));

					sql = "select descr from department where dept_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					valueXmlString.append("<department_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</department_descr>");

				}
				if (currentColumn.trim().equals("cr_term")) {
					mcode = checkNull(genericUtility.getColumnValue("cr_term", dom));

					sql = "select descr from crterm where cr_term = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</crterm_descr>");
				}
				if (currentColumn.trim().equals("curr_code__frt")) {
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));
					frtRate = checkNull(genericUtility.getColumnValue("frt_rate", dom));

					currCodefrt = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));
					sql = "select a.curr_code curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						currCodefrtBase = checkNull(rs.getString("curr_code"));
						if (currCodefrt != null && currCodefrt.trim().length() > 0) {
							// lc_exchrate =
							// gf_get_daily_exch_rate_sell_buy(currCodefrt,
							// currCodefrtBase, siteCodeDlv, ordDate, 'B')
							exchRate1 = finCommon.getDailyExchRateSellBuy(currCodefrt, currCodefrtBase, siteCodeDlv,
									ordDate, "B", conn);
							valueXmlString.append("<frt_rate>").append("<![CDATA[" + exchRate1 + "]]>")
							.append("</frt_rate>");
						}
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					frtType = genericUtility.getColumnValue("frt_type", dom);

					if ("Q".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_rate protect = '0'>").append("<![CDATA[" + exchRate1 + "]]>")
						.append("</frt_rate>");
					} else {
						valueXmlString.append("<frt_rate protect = '1'>").append("<![CDATA[" + exchRate1 + "]]>")
						.append("</frt_rate>");
					}

					setNodeValue(dom, "frt_rate", exchRate1);
					reStr = itemChanged(dom, dom1, dom2, objContext, "frt_rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					// gbf_itemchangedhdr("frt_rate")

				}
				if (currentColumn.trim().equals("curr_code")) {
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));

					sql = "select a.curr_code curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					currCodefrt = genericUtility.getColumnValue("curr_code", dom);
					if (rs.next()) {
						currCodefrtBase = checkNull(rs.getString("curr_code"));
						if (currCodefrt != null && currCodefrt.trim().length() > 0) {
							// lc_exchrate =
							// gf_get_daily_exch_rate_sell_buy(currCodefrt,
							// currCodefrtBase, siteCodeDlv, ordDate, 'B' )
							exchRate1 = finCommon.getDailyExchRateSellBuy(currCodefrt, currCodefrtBase, siteCodeDlv,
									ordDate, "B", conn);
							System.out.println("@@@@@6149 exchRate1[" + exchRate1 + "]");
							valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate1 + "]]>")
							.append("</exch_rate>");
						}
						valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + currCodefrt + "]]>")
						.append("</curr_code__comm>");
						valueXmlString.append("<exch_rate__sp>").append("<![CDATA[" + exchRate1 + "]]>")
						.append("</exch_rate__sp>");

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (!currCodefrtBase.trim().equalsIgnoreCase(currCodefrt.trim())) {
						valueXmlString.append("<exch_rate protect='0'>").append("<![CDATA[" + exchRate1 + "]]>")
						.append("</exch_rate>");
					} else {
						valueXmlString.append("<exch_rate protect='1'>").append("<![CDATA[" + exchRate1 + "]]>")
						.append("</exch_rate>");
						valueXmlString.append("<frt_rate>").append("1").append("</frt_rate>");
					}

					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));

					sql = "select count(*) as cnt from supplier_bank where curr_code= ? and supp_code= ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, currCodefrt);
					pStmt.setString(2, suppCode);
					rs = pStmt.executeQuery();
					currCodefrt = genericUtility.getColumnValue("curr_code", dom);
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (cnt == 1) {

						sql = "select bank_code__ben from supplier_bank where curr_code= ? and supp_code= ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, currCodefrt);
						pStmt.setString(2, suppCode);
						rs = pStmt.executeQuery();
						currCodefrt = genericUtility.getColumnValue("curr_code", dom);
						if (rs.next()) {
							bankCodeben = checkNull(rs.getString("bank_code__ben"));
						}
					} else {
						bankCodeben = "";
					}
					valueXmlString.append("<bank_code__ben>").append("<![CDATA[" + bankCodeben + "]]>")
					.append("</bank_code__ben>");

				}
				if (currentColumn.trim().equals("curr_code__comm")) {
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom));

					sql = "select a.curr_code curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					currCodefrt = genericUtility.getColumnValue("curr_code__comm", dom);
					if (rs.next()) {
						currCodefrtBase = rs.getString("curr_code");
						if (currCodefrt != null && currCodefrt.trim().length() > 0) {
							// lc_exchrate =
							// gf_get_daily_exch_rate_sell_buy(currCodefrt,
							// currCodefrtBase, siteCodeDlv, ordDate, 'B' )
							exchRate1 = finCommon.getDailyExchRateSellBuy(currCodefrt, currCodefrtBase, siteCodeDlv,
									ordDate, "B", conn);
							valueXmlString.append("<exch_rate__sp>").append("<![CDATA[" + exchRate1 + "]]>")
							.append("</exch_rate__sp>");
						}

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (!currCodefrtBase.trim().equalsIgnoreCase(currCodefrt.trim())) {
						valueXmlString.append("<exch_rate__sp protect='0'>").append("").append("</exch_rate__sp>");
					} else {
						valueXmlString.append("<exch_rate__sp protect='1'>").append("").append("</exch_rate__sp>");
						valueXmlString.append("<exch_rate__sp>").append("1").append("</exch_rate__sp>");
					}
				}
				if (currentColumn.trim().equals("sales_pers")) {
					mcode = checkNull(genericUtility.getColumnValue("sales_pers", dom)).trim();

					sql = "select comm_perc, comm_perc__on, curr_code from sales_pers where sales_pers = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					currCodefrt = checkNull(genericUtility.getColumnValue("curr_code__comm", dom));
					if (rs.next()) {
						commPerc = rs.getString("comm_perc");
						mcpercon = rs.getString("comm_perc__on");
						mccode = rs.getString("curr_code");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<comm_perc>").append("<![CDATA[" + commPerc + "]]>").append("</comm_perc>");
					valueXmlString.append("<comm_perc__on>").append("<![CDATA[" + mcpercon + "]]>")
					.append("</comm_perc__on>");
					valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + getAbsString(mccode) + "]]>")
					.append("</curr_code__comm>");
					valueXmlString.append("<exch_rate__sp>").append("<![CDATA[" + exchRate1 + "]]>")
					.append("</exch_rate__sp>");
					// gbf_itemchangedhdr('curr_code__comm')

					setNodeValue(dom, "curr_code__comm", getAbsString(mccode));
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__comm", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

				}
				if (currentColumn.trim().equals("tran_code")) {
					mcode = checkNull(genericUtility.getColumnValue("tran_code", dom));

					if (mcode != null && mcode.trim().length() > 0) {
						sql = "select trans_mode from transporter_mode where tran_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							transMode = checkNull(rs.getString("trans_mode"));

							if (transMode != null && transMode.trim().length() > 0) {
								valueXmlString.append("<trans_mode>").append("<![CDATA[" + transMode + "]]>")
								.append("</trans_mode>");
							} else {
								valueXmlString.append("<trans_mode>").append("R").append("</trans_mode>");
							}

							sql = "select tran_name,frt_term,curr_code from transporter where tran_code = ?";
							pStmt1 = conn.prepareStatement(sql);
							pStmt1.setString(1, mcode);
							rsNew = pStmt1.executeQuery();
							currCodefrt = genericUtility.getColumnValue("curr_code__comm", dom);
							if (rsNew.next()) {
								descr = rsNew.getString("tran_name");
								frtTerm = rsNew.getString("frt_term");
								currCode = rsNew.getString("curr_code");
							}
							valueXmlString.append("<tran_name>").append("<![CDATA[" + descr + "]]>")
							.append("</tran_name>");
							valueXmlString.append("<frt_term>").append("<![CDATA[" + frtTerm + "]]>")
							.append("</frt_term>");
							valueXmlString.append("<curr_code__frt>")
							.append("<![CDATA[" + getAbsString(currCode) + "]]>").append("</curr_code__frt>");

							setNodeValue(dom, "curr_code__frt", getAbsString(currCode));
							reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__frt", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);

							// gbf_itemchangedhdr("curr_code__frt")

							rsNew.close();
							rsNew = null;
							pStmt1.close();
							pStmt1 = null;
						} else {
							valueXmlString.append("<trans_mode>").append("").append("</trans_mode>");
							valueXmlString.append("<tran_name>").append("").append("</tran_name>");

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					}
				}
				if (currentColumn.trim().equals("dlv_term")) {
					mcode = checkNull(genericUtility.getColumnValue("dlv_term", dom));

					sql = "select descr,policy_no from delivery_term where dlv_term = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString("descr"));
						policyNo = checkNull(rs.getString("policy_no"));
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<delivery_term_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</delivery_term_descr>");
					valueXmlString.append("<policy_no>").append("<![CDATA[" + policyNo + "]]>").append("</policy_no>");
				}
				if (currentColumn.trim().equals("contract_no")) {
					mcode = checkNull(genericUtility.getColumnValue("contract_no", dom));
					indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
					quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
					projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));

					if (mcode != null && mcode.trim().length() > 0) {

						valueXmlString.append("<ind_no protect='1'>").append("<![CDATA[" + indNo + "]]>")
						.append("</ind_no>");
						valueXmlString.append("<quot_no protect='1'>").append("<![CDATA[" + quotNo + "]]>")
						.append("</quot_no>");
						// valueXmlString.append("<proj_code protect='1'>") //Pavan R 23Jul19[to made
						// proj_code editable]
						valueXmlString.append("<proj_code protect='0'>").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");

						sql = "select supp_code,proj_code,tran_code,tax_class,tax_chap,tax_env,frt_term,curr_code__frt,dlv_term,cr_term,"
								+ "frt_amt,contract_type, price_list from pcontract_hdr where contract_no = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							suppCode = rs.getString("supp_code");
							projCode = rs.getString("proj_code");
							tranCode = rs.getString("tran_code");
							taxClass = rs.getString("tax_class");
							taxChap = rs.getString("tax_chap");
							taxEnv = rs.getString("tax_env");
							frtTerm = rs.getString("frt_term");
							dlvTerm = rs.getString("dlv_term");
							crTerm = rs.getString("cr_term");
							frtAmt = rs.getString("frt_amt");
							contractType = rs.getString("contract_type");
							priceList = rs.getString("price_list");

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						valueXmlString.append("<supp_code>").append("<![CDATA[" + suppCode + "]]>")
						.append("</supp_code>");
						valueXmlString.append("<proj_code>").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>")
						.append("</tran_code>");
						valueXmlString.append("<tran_name>").append("<![CDATA[" + tranName + "]]>")
						.append("</tran_name>");
						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						valueXmlString.append("<dlv_term>").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term>");
						valueXmlString.append("<frt_amt>").append("<![CDATA[" + frtAmt + "]]>").append("</frt_amt>");
						valueXmlString.append("<pord_type>").append("<![CDATA[" + contractType + "]]>")
						.append("</pord_type>");

						setNodeValue(dom, "supp_code", getAbsString(suppCode));
						reStr = itemChanged(dom, dom1, dom2, objContext, "supp_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);

						// gbf_itemchangedhdr("supp_code")

					} else {
						valueXmlString.append("<ind_no protect='0'>").append("<![CDATA[" + indNo + "]]>")
						.append("</ind_no>");
						valueXmlString.append("<quot_no protect='0'>").append("<![CDATA[" + quotNo + "]]>")
						.append("</quot_no>");
						valueXmlString.append("<proj_code protect='0'>").append("<![CDATA[" + projCode + "]]>")
						.append("</proj_code>");

					}

				}
				if (currentColumn.trim().equals("quot_no")) {
					quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));

					if (quotNo == null || quotNo.trim().length() == 0) {
						valueXmlString.append("<ind_no protect = '0'>").append("").append("</ind_no>");
						valueXmlString.append("<contract_no protect = '0'>").append("").append("</contract_no>");
						valueXmlString.append("<proj_code protect = '0'>").append("").append("</proj_code>");
					} else {
						valueXmlString.append("<ind_no protect = '1'>").append("").append("</ind_no>");
						valueXmlString.append("<contract_no protect = '1'>").append("").append("</contract_no>");
						// valueXmlString.append("<proj_code protect = '1'>") //Pavan R 23Jul19[to made
						// proj_code editable]
						valueXmlString.append("<proj_code protect = '0'>").append("").append("</proj_code>");
					}

					sql = "select supp_code, item_ser from pquot_hdr where quot_no = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, quotNo);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						suppCode = rs.getString("supp_code");
						itemSer = rs.getString("item_ser");

						valueXmlString.append("<supp_code>").append(getAbsString(suppCode)).append("</supp_code>");
						setNodeValue(dom, "supp_code", getAbsString(suppCode));
						reStr = itemChanged(dom, dom1, dom2, objContext, "supp_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangedhdr("supp_code")
						valueXmlString.append("<item_ser>").append(itemSer).append("</item_ser>");
					}

				}
				if (currentColumn.trim().equals("adv_type")) {
					mcode = checkNull(genericUtility.getColumnValue("adv_type", dom));
					mhdrtot = checkNull(genericUtility.getColumnValue("tot_amt", dom));
					mhdrord = checkNull(genericUtility.getColumnValue("ord_amt", dom));
					advPerc = checkNull(genericUtility.getColumnValue("adv_perc", dom));

					totAmt = Double.parseDouble(mhdrtot);
					ordAmt = Double.parseDouble(mhdrord);
					advPercInt = Double.parseDouble(advPerc);
					if ("F".equalsIgnoreCase(mcode)) {
						advance = totAmt;
						valueXmlString.append("<advance>").append(advPercInt).append("</advance>");
					} else if ("P".equalsIgnoreCase(mcode)) {
						advance = (advPercInt / 100) * totAmt;
						valueXmlString.append("<advance>").append(advance).append("</advance>");
					} else {
						advance = (advPercInt / 100) * ordAmt;
						valueXmlString.append("<advance>").append(advance).append("</advance>");
					}

				}
				if (currentColumn.trim().equals("adv_perc")) {
					mhdrtot = checkNull(genericUtility.getColumnValue("tot_amt", dom));
					mhdrord = checkNull(genericUtility.getColumnValue("ord_amt", dom));
					advPerc = checkNull(genericUtility.getColumnValue("adv_perc", dom));
					advType = checkNull(genericUtility.getColumnValue("adv_type", dom));

					totAmt = Double.parseDouble(mhdrtot);
					ordAmt = Double.parseDouble(mhdrord);
					advPercInt = Double.parseDouble(advPerc);
					if ("F".equalsIgnoreCase(advType)) {
						advance = totAmt;
						valueXmlString.append("<advance>").append(advPercInt).append("</advance>");
					} else if ("P".equalsIgnoreCase(advType)) {
						advance = (advPercInt / 100) * totAmt;
						valueXmlString.append("<advance>").append(advance).append("</advance>");
					} else {
						advance = (advPercInt / 100) * ordAmt;
						valueXmlString.append("<advance>").append(advance).append("</advance>");
					}
				}
				if (currentColumn.trim().equals("frt_type")) {
					frtType = checkNull(genericUtility.getColumnValue("frt_type", dom));
					frtRate = checkNull(genericUtility.getColumnValue("frt_rate", dom));

					if ("F".equalsIgnoreCase(frtType)) {
						// valueXmlString.append("<frt_rate>").append("0").append("</frt_rate>");
						valueXmlString.append("<frt_rate protect= '1'>").append("<![CDATA[" + frtRate + "]]>")
						.append("</frt_rate>");

						// setNodeValue( dom, "frt_rate", 0 );
						reStr = itemChanged(dom, dom1, dom2, objContext, "frt_rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangedhdr("frt_rate")
					} else if ("Q".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_rate protect= '0'>").append("<![CDATA[" + frtRate + "]]>")
						.append("</frt_rate>");

						// setNodeValue( dom, "curr_code__frt", "" );
						reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__frt", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangedhdr("curr_code__frt")
					}
				}
				if (currentColumn.trim().equals("frt_rate")) {
					frtType = checkNull(genericUtility.getColumnValue("frt_type", dom));

					if ("Q".equalsIgnoreCase(frtType)) {
						frtRate = genericUtility.getColumnValue("frt_rate", dom);

						frtRatedouble = Double.parseDouble(frtRate);
						if (!(frtRate == null || frtRatedouble == 0)) {
							purcOrder = genericUtility.getColumnValue("purc_order", dom);

							qtyTot = 0;
							/*
							 * for ll_rowcnt = 1 to dw_detbrow[1].RowCount()
							 * 
							 * 
							 * 
							 * ldec_qty = dw_detbrow[1].getitemdecimal(ll_rowcnt, "quantity") if
							 * isnull(ldec_qty) then ldec_qty = 0
							 * 
							 * ld_qty_tot = ld_qty_tot + ldec_qty next ldec_frtamt_qty = ldec_frtrate *
							 * ld_qty_tot dw_edit.setitem(1, "frt_amt__qty",ldec_frtamt_qty)
							 * 
							 * ld_frtamt_fixed = dw_edit.getitemdecimal(1,"frt_amt__fixed") if
							 * isnull(ld_frtamt_fixed) then ld_frtamt_fixed = 0
							 * 
							 * ld_frtamt_tot = ldec_frtamt_qty + ld_frtamt_fixed dw_edit.setitem(1,
							 * "frt_amt",ld_frtamt_tot) else ld_frtamt_fixed =
							 * dw_edit.getitemdecimal(1,"frt_amt__fixed") dw_edit.setitem(1,
							 * "frt_amt__qty",0) dw_edit.setitem(1, "frt_amt",ld_frtamt_fixed)
							 */
						}
					}

				}
				if (currentColumn.trim().equals("frt_amt__fixed")) {
					frtamtFixed = checkNull(genericUtility.getColumnValue("frt_amt__fixed", dom));
					frtamtQty = checkNull(genericUtility.getColumnValue("frt_amt__qty", dom));

					if (frtamtFixed == null) {
						frtamtFixed = "0";
					}
					if (frtamtQty == null) {
						frtamtQty = "0";
					}

					frtamtTot = frtamtFixed + frtamtQty;
					valueXmlString.append("<frt_amt>").append(frtamtTot).append("</frt_amt>");
				}
				if (currentColumn.trim().equals("bank_code__pay")) {
					bankcdPay = checkNull(genericUtility.getColumnValue("bank_code__pay", dom));
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));

					sql = "select bank_name__ben, bank_acct_no__ben from supplier_bank where supp_code = ? and bank_code__ben = ?"
							+ "and case when confirmed is null then 'N' else confirmed end = 'Y' and	case when active_yn is null then 'Y' else active_yn end = 'Y' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, suppCode);
					pStmt.setString(2, bankcdPay);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						bname = rs.getString("bank_name__ben");
						acctNo = rs.getString("bank_acct_no__ben");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<bank_name__ben>").append(bname).append("</bank_name__ben>");
					valueXmlString.append("<bank_acct_no__ben>").append(acctNo).append("</bank_acct_no__ben>");

				}
				if (currentColumn.trim().equals("task_code")) {
					taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));

					sql = "select task_desc from proj_est_milestone where task_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, taskCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						taskDesc = checkNull(rs.getString("task_desc"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<task_desc>").append(taskDesc).append("</task_desc>");
				}
				if (currentColumn.trim().equals("proj_code")) {
					projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));

					sql = "select descr ,cctr_code ,approx_cost from project where proj_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, projCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						projDescr = checkNull(rs.getString("descr"));
						cctrCode = checkNull(rs.getString("cctr_code"));
						approxCost = checkNull(rs.getString("approx_cost"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					valueXmlString.append("<proj_name>").append(projDescr).append("</proj_name>");
					valueXmlString.append("<budget_code>").append(cctrCode).append("</budget_code>");
					valueXmlString.append("<budget_amt>").append(approxCost).append("</budget_amt>");
				}
				valueXmlString.append("</Detail1>");
				break;

			case 2:

				reStr = itemChanged(dom1, dom1, dom2, "1", "itm_defaultedit", editFlag, xtraParams);

				reStr = reStr.substring(reStr.indexOf("<Detail1>"), reStr.indexOf("</Detail1>"));
				System.out.println("Detail 1String" + reStr);
				valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
				valueXmlString.append(editFlag).append("</editFlag> </header>");
				valueXmlString.append(reStr);

				indNo = checkNull(genericUtility.getColumnValue("ind_no", dom1));
				quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom1));
				contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
				projCode = checkNull(genericUtility.getColumnValue("proj_code", dom1));
				siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom1));
				itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));

				valueXmlString.append("<ind_no protect = '1'>").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
				valueXmlString.append("<quot_no protect = '1'>").append("<![CDATA[" + quotNo + "]]>")
				.append("</quot_no>");
				valueXmlString.append("<contract_no protect = '1'>").append("<![CDATA[" + contractNo + "]]>")
				.append("</contract_no>");
				valueXmlString.append("<proj_code protect = '0'>") // //Pavan R 23Jul19[to made proj_code editable]
				.append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
				valueXmlString.append("<site_code__dlv protect = '1'>").append("<![CDATA[" + siteCodeDlv + "]]>")
				.append("</site_code__dlv>");
				valueXmlString.append("<item_ser protect = '1'>").append("<![CDATA[" + itemSer + "]]>")
				.append("</item_ser>");
				valueXmlString.append("</Detail1>");

				valueXmlString.append("<Detail2>");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equals("itm_default")) {
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString .append("<purc_order>")
					 * .append(checkNull(genericUtility.getColumnValue( "purc_order", dom)))
					 * .append("</purc_order>"); siteCodeDlv =
					 * checkNull(genericUtility.getColumnValue( "site_code__dlv", dom1));
					 * valueXmlString.append("<site_code  protect='0'>")
					 * .append(siteCodeDlv).append("</site_code>"); tranboqId =
					 * checkNull(genericUtility.getColumnValue( "tran_id__boq", dom));
					 * 
					 * valueXmlString.append("<status_date>").append(sysDate)
					 * .append("</status_date>");
					 * valueXmlString.append("<req_date>").append(sysDate) .append("</req_date>");
					 * valueXmlString.append("<dlv_date>").append(sysDate) .append("</dlv_date>");
					 */
					valueXmlString.append("<purc_order>")
					.append("<![CDATA[" + checkNull(genericUtility.getColumnValue("purc_order", dom)) + "]]>")
					.append("</purc_order>");
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom1));
					valueXmlString.append("<site_code  protect='0'>").append("<![CDATA[" + siteCodeDlv + "]]>")
					.append("</site_code>");
					// Added by Rohini on 08-05-19 to set site code in detail dom [to fetch below
					// for itemAcctDetrTtype acct_code__ap_adv]
					setNodeValue(dom, "site_code", siteCodeDlv);
					// Ended by Rohini on 08-05-19 to set site code in detail dom [to fetch below
					// for itemAcctDetrTtype acct_code__ap_adv]
					tranboqId = checkNull(genericUtility.getColumnValue("tran_id__boq", dom));

					valueXmlString.append("<status_date>").append("<![CDATA[" + sysDate + "]]>")
					.append("</status_date>");
					valueXmlString.append("<req_date>").append("<![CDATA[" + sysDate + "]]>").append("</req_date>");
					valueXmlString.append("<dlv_date>").append("<![CDATA[" + sysDate + "]]>").append("</dlv_date>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					sql = " Select case when eou is null then 'N' else eou end From site Where site_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						eou = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("Y".equalsIgnoreCase(eou)) {
						valueXmlString.append("<duty_paid>").append("N").append("</duty_paid>");
					} else {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<duty_paid>").append(lsnull) .append("</duty_paid>");
						 */
						valueXmlString.append("<duty_paid>").append("<![CDATA[" + lsnull + "]]>")
						.append("</duty_paid>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}

					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));

					if (saleOrder.trim().length() > 0) {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<line_no__sord protect = '0'>")
						 * .append(lsnull).append("</line_no__sord>");
						 */
						valueXmlString.append("<line_no__sord protect = '0'>").append("<![CDATA[" + lsnull + "]]>")
						.append("</line_no__sord>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}

					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));

					if (lineNo != null && lineNo.trim().length() > 0) {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<line_no>").append(lineNo) .append("</line_no>");
						 */
						valueXmlString.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}
					// valueXmlString.append("<site_code protect =
					// '0'>").append("").append("</site_code>");
					// valueXmlString.append("<ind_no protect =
					// '0'>").append("").append("</ind_no>");
					// valueXmlString.append("<item_code protect =
					// '0'>").append("").append("</item_code>");

					valueXmlString.append("<conv__rtuom_stduom>").append(1).append("</conv__rtuom_stduom>");
					indNo = genericUtility.getColumnValue("ind_no", dom1);

					if (tranboqId != null && tranboqId.trim().length() > 0) {
						// valueXmlString.append("<ind_no protect='0'>").append("").append("</ind_no>");
					} else {
						if (indNo != null && indNo.trim().length() > 0) {
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<ind_no protect = '1'>") .append(getAbsString(indNo))
							 * .append("</ind_no>");
							 */
							valueXmlString.append("<ind_no protect = '1'>")
							.append("<![CDATA[" + getAbsString(indNo) + "]]>").append("</ind_no>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
							// valueXmlString.append("<ind_no protect='1'>").append("").append("</ind_no>");

							setNodeValue(dom, "ind_no", getAbsString(indNo));
							reStr = itemChanged(dom, dom1, dom2, objContext, "ind_no", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
							// gbf_itemchangeddet("ind_no");
						} else {
							valueXmlString.append("<ind_no protect='0'>").append("").append("</ind_no>");
						}
					}
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString .append("<proj_code>")
					 * .append(checkNull(genericUtility.getColumnValue( "proj_code",
					 * dom1))).append("</proj_code>");
					 */
					valueXmlString.append("<proj_code>")
					.append("<![CDATA[" + checkNull(genericUtility.getColumnValue("proj_code", dom1)) + "]]>")
					.append("</proj_code>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					exchRate = genericUtility.getColumnValue("exch_rate", dom1);
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<exch_rate>").append(exchRate)
					 * .append("</exch_rate>");
					 */
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					mval2 = checkNull(genericUtility.getColumnValue("supp_code", dom1));

					// Modified by Rohini T on[08/05/19][start]
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					System.out.println("itemCode::::::::" + itemCode);
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
					System.out.println("itemSer::::::::" + itemSer);
					tranType = checkNull(genericUtility.getColumnValue("pord_type", dom1));
					System.out.println("tranType::::::::" + tranType);
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					System.out.println("siteCode::::::::" + siteCode);
					cctrCodeApAdv = finCommon.getAcctDetrTtype(itemCode, itemSer, "APADV", tranType, siteCode, conn);

					System.out.println("@@@@@@@ mcctr[" + cctrCodeApAdv + "]");
					String mcctrArray[] = cctrCodeApAdv.split(",");
					System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
					if (mcctrArray.length > 0) {
						acctCodeApAdv = mcctrArray[0];
						cctrCodeApAdv = "";
					}
					if (mcctrArray.length > 1) {
						acctCodeApAdv = mcctrArray[0];
						cctrCodeApAdv = mcctrArray[1];
					}
					if (acctCodeApAdv == null || acctCodeApAdv.length() == 0) {
						sql = " Select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV From supplier Where supp_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mval2);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							acctCodeApAdv = checkNull(rs.getString("ACCT_CODE__AP_ADV"));
							cctrCodeApAdv = checkNull(rs.getString("CCTR_CODE__AP_ADV"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}
					// Modified by Rohini T on[08/05/19][end]

					// valueXmlString.append("<acct_code__ap_adv>").append(acctCodeApAdv).append("</acct_code__ap_adv>");
					// valueXmlString.append("<cctr_code__ap_adv>").append(cctrCodeApAdv).append("</cctr_code__ap_adv>");

					// invAcctPorcp=
					// trim(gf_getfinparm('999999','INV_ACCT_PORCP'))

					invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

					if (invAcctPorcp == null || invAcctPorcp.equalsIgnoreCase("null")
							|| invAcctPorcp.trim().length() == 0) {
						invAcctPorcp = "N";
					}
					// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					// inv_acct of the itemser of the item
					if ("S".equals(invAcctPorcp)) {
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							invAcctSer = checkNull(rs.getString("inv_acct"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (invAcctSer == null || invAcctSer.trim().length() == 0) {
							invAcctSer = "N";
						}
						invAcctPorcp = invAcctSer;
					}
					// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					// inv_acct of the itemser of the item
					if ("Y".equalsIgnoreCase(invAcctPorcp)) {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString .append("<acct_code__ap_adv protect='0'>")
						 * .append(acctCodeApAdv) .append("</acct_code__ap_adv>"); valueXmlString
						 * .append("<cctr_code__ap_adv protect='0'>") .append(cctrCodeApAdv)
						 * .append("</cctr_code__ap_adv>");
						 */
						valueXmlString.append("<acct_code__ap_adv protect='0'>")
						.append("<![CDATA[" + acctCodeApAdv + "]]>").append("</acct_code__ap_adv>");
						valueXmlString.append("<cctr_code__ap_adv protect='0'>")
						.append("<![CDATA[" + cctrCodeApAdv + "]]>").append("</cctr_code__ap_adv>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					} else {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString .append("<acct_code__ap_adv protect='1'>")
						 * .append(acctCodeApAdv) .append("</acct_code__ap_adv>"); valueXmlString
						 * .append("<cctr_code__ap_adv protect='1'>") .append(cctrCodeApAdv)
						 * .append("</cctr_code__ap_adv>");
						 */
						valueXmlString.append("<acct_code__ap_adv protect='1'>")
						.append("<![CDATA[" + acctCodeApAdv + "]]>").append("</acct_code__ap_adv>");
						valueXmlString.append("<cctr_code__ap_adv protect='1'>")
						.append("<![CDATA[" + cctrCodeApAdv + "]]>").append("</cctr_code__ap_adv>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}

					mval = checkNull(genericUtility.getColumnValue("pord_type", dom));
					sql = " Select PRE_ASSIGN_LOT,PRE_ASSIGN_EXP From pordertype Where order_type = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mval);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						preAssignLot = rs.getString("PRE_ASSIGN_LOT");
						preAssignExp = rs.getString("PRE_ASSIGN_EXP");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("Y".equalsIgnoreCase(preAssignLot.trim())) {
						// valueXmlString.append("<lot_no__passign
						// protect='0'>").append(cctrCodeApAdv).append("</lot_no__passign>");
						valueXmlString.append("<lot_no__passign protect='0'>").append("").append("</lot_no__passign>");
					} else {
						// valueXmlString.append("<lot_no__passign
						// protect='1'>").append(cctrCodeApAdv).append("</lot_no__passign>");
						valueXmlString.append("<lot_no__passign protect='1'>").append("").append("</lot_no__passign>");
					}

					if ("Y".equalsIgnoreCase(preAssignExp.trim())) {
						// valueXmlString.append("<exp_date__passign
						// protect='0'>").append(cctrCodeApAdv).append("</exp_date__passign>");
						valueXmlString.append("<exp_date__passign protect='0'>").append("")
						.append("</exp_date__passign>");
					} else {
						// valueXmlString.append("<exp_date__passign
						// protect='1'>").append(cctrCodeApAdv).append("</exp_date__passign>");
						valueXmlString.append("<exp_date__passign protect='1'>").append("")
						.append("</exp_date__passign>");
					}

					// valueXmlString.append("<item_code>").append("EF0150").append("</item_code>");
				}
				if (currentColumn.trim().equals("itm_defaultedit")) {

					// rowCount =
					// checkNull(genericUtility.getColumnValue("row_count",
					// dom)) ;
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));

					// Added by Rupesh on [07/11/2017][Instructed by Piyush sir][Start]
					acctCodeApAdv = checkNull(genericUtility.getColumnValue("acct_code__ap_adv", dom));
					System.out.println("itm_defaultedit acctCodeApAdv--" + acctCodeApAdv);
					cctrCodeApAdv = checkNull(genericUtility.getColumnValue("cctr_code__ap_adv", dom));
					System.out.println("itm_defaultedit cctr_code__ap_adv--" + cctrCodeApAdv);
					// Added by Rupesh on [07/11/2017][Instructed by Piyush sir][End]

					sql = " select count(1) from porddet where purc_order = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, purcOrder);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						rowCountInt = rs.getInt(1);

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					System.out.println("@@@@ rowCountInt[" + rowCountInt + "]");
					// rowCountInt =
					// Integer.parseInt(rowCount==null?"0":rowCount) ;

					if (rowCountInt > 0) {
						codeDescr = getDescrSpeces(itemCode, conn);

						if (indNo != null && indNo.trim().length() > 0) {
							valueXmlString.append("<item_code__mfg protect='1'>").append("")
							.append("</item_code__mfg>");
						} else {
							valueXmlString.append("<item_code__mfg protect='0'>").append("")
							.append("</item_code__mfg>");
						}
					} else {
						valueXmlString.append("<item_code__mfg protect='0'>").append("").append("</item_code__mfg>");
						codeDescr = "";
					}
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<spl_instr>").append(codeDescr)
					 * .append("</spl_instr>");
					 */
					valueXmlString.append("<spl_instr>").append("<![CDATA[" + codeDescr + "]]>").append("</spl_instr>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					mval = genericUtility.getColumnValue("order_type", dom);
					sql = " Select PRE_ASSIGN_LOT,PRE_ASSIGN_EXP From pordertype Where order_type = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mval);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						preAssignLot = checkNull(rs.getString("PRE_ASSIGN_LOT"));
						preAssignExp = checkNull(rs.getString("PRE_ASSIGN_EXP"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if ("Y".equalsIgnoreCase(preAssignLot.trim())) {
						// valueXmlString.append("<lot_no__passign
						// protect='0'>").append(cctrCodeApAdv).append("</lot_no__passign>");
						valueXmlString.append("<lot_no__passign protect='0'>").append("").append("</lot_no__passign>");
					} else {
						// valueXmlString.append("<lot_no__passign
						// protect='1'>").append(cctrCodeApAdv).append("</lot_no__passign>");
						valueXmlString.append("<lot_no__passign protect='1'>").append("").append("</lot_no__passign>");
					}

					if ("Y".equalsIgnoreCase(preAssignExp.trim())) {
						// valueXmlString.append("<exp_date__passign
						// protect='0'>").append(cctrCodeApAdv).append("</exp_date__passign>");
						valueXmlString.append("<exp_date__passign protect='0'>").append("")
						.append("</exp_date__passign>");
					} else {
						// valueXmlString.append("<exp_date__passign
						// protect='1'>").append(cctrCodeApAdv).append("</exp_date__passign>");
						valueXmlString.append("<exp_date__passign protect='1'>").append("")
						.append("</exp_date__passign>");
					}

					// invAcctPorcp =
					// trim(gf_getfinparm('999999','INV_ACCT_PORCP'))
					invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

					if (invAcctPorcp == null || invAcctPorcp.equalsIgnoreCase("null")
							|| invAcctPorcp.trim().length() == 0) {
						invAcctPorcp = "N";
					}
					// 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					// inv_acct of the itemser of the item
					if ("S".equals(invAcctPorcp)) {
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							invAcctSer = checkNull(rs.getString("inv_acct"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (invAcctSer == null || invAcctSer.trim().length() == 0) {
							invAcctSer = "N";
						}
						invAcctPorcp = invAcctSer;
					}
					// end 15-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					// inv_acct of the itemser of the item
					if ("Y".equalsIgnoreCase(invAcctPorcp)) {
						// Modified by Rupesh [07/11/2017][Instructed by Piyush sir][Start]
						/*
						 * valueXmlString. append("<acct_code__ap_adv protect='0'>").append("").
						 * append("</acct_code__ap_adv>"); valueXmlString.
						 * append("<cctr_code__ap_adv protect='0'>").append("").
						 * append("</cctr_code__ap_adv>");
						 */
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<acct_code__ap_adv protect='0'>").append(
						 * acctCodeApAdv) .append("</acct_code__ap_adv>");
						 * valueXmlString.append("<cctr_code__ap_adv protect='0'>").append(
						 * cctrCodeApAdv) .append("</cctr_code__ap_adv>");
						 */
						valueXmlString.append("<acct_code__ap_adv protect='0'>")
						.append("<![CDATA[" + acctCodeApAdv + "]]>").append("</acct_code__ap_adv>");
						valueXmlString.append("<cctr_code__ap_adv protect='0'>")
						.append("<![CDATA[" + cctrCodeApAdv + "]]>").append("</cctr_code__ap_adv>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						// Modified by Rupesh [07/11/2017][Instructed by Piyush sir][End]
					} else {
						// Modified by Rupesh [07/11/2017][Instructed by Piyush sir][Start]
						/*
						 * valueXmlString. append("<acct_code__ap_adv protect='1'>").append("").
						 * append("</acct_code__ap_adv>"); valueXmlString.
						 * append("<cctr_code__ap_adv protect='1'>").append("").
						 * append("</cctr_code__ap_adv>");
						 */
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<acct_code__ap_adv protect='1'>").append(
						 * acctCodeApAdv) .append("</acct_code__ap_adv>");
						 * valueXmlString.append("<cctr_code__ap_adv protect='1'>").append(
						 * cctrCodeApAdv) .append("</cctr_code__ap_adv>");
						 */
						valueXmlString.append("<acct_code__ap_adv protect='1'>")
						.append("<![CDATA[" + acctCodeApAdv + "]]>").append("</acct_code__ap_adv>");
						valueXmlString.append("<cctr_code__ap_adv protect='1'>")
						.append("<![CDATA[" + cctrCodeApAdv + "]]>").append("</cctr_code__ap_adv>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						// Modified by Rupesh [07/11/2017][Instructed by Piyush sir][End]
					}
				}
				if (currentColumn.trim().equals("ind_no")) {

					mcode = checkNull(genericUtility.getColumnValue("ind_no", dom));
					mval = checkNull(genericUtility.getColumnValue("item_ser", dom1));
					mval1 = checkNull(genericUtility.getColumnValue("quot_no", dom1));
					mval2 = checkNull(genericUtility.getColumnValue("supp_code", dom1));
					projCode = checkNull(genericUtility.getColumnValue("proj_code", dom1));
					sitecodeDlv= checkNull(genericUtility.getColumnValue("site_code__dlv", dom1)); // 08-dec-2020 manoharan added  as used below

					System.out.println("supp_code>>" + mval2 + "item_ser>>" + mval + "quot_no>>" + mval1 + "ind_no>>"
							+ mcode + "projCode>>" + projCode);
					if (mcode != null && mcode.trim().length() > 0) {
						sql = " Select item_code, unit__ind, case when quantity__stduom is null then 0 else quantity__stduom end  - case when ord_qty is null then 0 else ord_qty end as quantity__stduom,req_date,"
								+ "  site_code__dlv,conv__qty_stduom,special_instr,specific_instr,remarks, pack_instr, acct_code, cctr_code, unit__std, "
								+ " emp_code__qcaprv, item_code__mfg,WORK_ORDER,dept_code,supp_code__mnfr,proj_code,anal_code"
								+ " From indent Where ind_no = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							itemCode = checkNull(rs.getString("item_code"));
							uom = checkNull(rs.getString("unit__ind"));
							pendqty = checkNull(rs.getString("quantity__stduom"));                            
							reqdate = checkNull(rs.getString("req_date"));
							sitecodeDlv = checkNull(rs.getString("site_code__dlv"));
							convQtystd = checkNull(rs.getString("conv__qty_stduom"));
							specialInstr = checkNull(rs.getString("special_instr"));
							specificInstr = checkNull(rs.getString("specific_instr"));
							indRemarks = checkNull(rs.getString("remarks"));
							packInstr = checkNull(rs.getString("pack_instr"));
							acctCode = checkNull(rs.getString("acct_code"));
							cctrCode = checkNull(rs.getString("cctr_code"));
							unitStd = checkNull(rs.getString("unit__std"));
							empCodeqcaprv = checkNull(rs.getString("emp_code__qcaprv"));
							workOrder = checkNull(rs.getString("WORK_ORDER"));
							deptCode = checkNull(rs.getString("dept_code"));
							projCode = checkNull(rs.getString("proj_code"));
							analCode = checkNull(rs.getString("anal_code"));

							System.out.println("@@@@@@@6838 itemCode[" + itemCode + "]");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						System.out.println("pendqty 7453>>"+pendqty);

						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<remarks>").append(indRemarks) .append("</remarks>");
						 * valueXmlString.append("<item_code>").append(itemCode)
						 * .append("</item_code>"); valueXmlString.append("<acct_code__dr>")
						 * .append(acctCode).append("</acct_code__dr>");
						 * valueXmlString.append("<cctr_code__dr>")
						 * .append(cctr_code).append("</cctr_code__dr>");
						 * valueXmlString.append("<site_code  protect='0'>")
						 * .append(sitecodeDlv).append("</site_code>");
						 * valueXmlString.append("<special_instr>") .append(specialInstr)
						 * .append("</special_instr>"); valueXmlString.append("<specific_instr>")
						 * .append(specificInstr) .append("</specific_instr>");
						 * valueXmlString.append("<pack_instr>").append(packInstr)
						 * .append("</pack_instr>"); valueXmlString.append("<unit>").append(uom)
						 * .append("</unit>"); valueXmlString.append("<unit__std>").append(unitStd)
						 * .append("</unit__std>"); valueXmlString.append("<unit__rate>").append(uom)
						 * .append("</unit__rate>"); valueXmlString.append("<conv__qty_stduom>")
						 * .append(convQtystd) .append("</conv__qty_stduom>");
						 * valueXmlString.append("<emp_code__qcaprv>") .append(empCodeqcaprv)
						 * .append("</emp_code__qcaprv>");
						 * valueXmlString.append("<work_order>").append(workOrder)
						 * .append("</work_order>");
						 * valueXmlString.append("<dept_code>").append(deptCode)
						 * .append("</dept_code>"); valueXmlString.append("<anal_code>")
						 * .append(getAbsString(analCode)) .append("</anal_code>");
						 */
						valueXmlString.append("<remarks>").append("<![CDATA[" + indRemarks + "]]>")
						.append("</remarks>");
						valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>")
						.append("</item_code>");
						valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + acctCode + "]]>")
						.append("</acct_code__dr>");
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + cctr_code + "]]>")
						.append("</cctr_code__dr>");
						valueXmlString.append("<site_code  protect='0'>").append("<![CDATA[" + sitecodeDlv + "]]>")
						.append("</site_code>");
						valueXmlString.append("<special_instr>").append("<![CDATA[" + specialInstr + "]]>")
						.append("</special_instr>");
						valueXmlString.append("<specific_instr>").append("<![CDATA[" + specificInstr + "]]>")
						.append("</specific_instr>");
						valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>")
						.append("</pack_instr>");
						valueXmlString.append("<unit>").append("<![CDATA[" + uom + "]]>").append("</unit>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
						.append("</unit__std>");
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + uom + "]]>").append("</unit__rate>");
						System.out.println("convQtystd 7502>> " + convQtystd);  //added by manish mhatre on 26-03-21
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + convQtystd + "]]>")
						.append("</conv__qty_stduom>");
						valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeqcaprv + "]]>")
						.append("</emp_code__qcaprv>");
						valueXmlString.append("<work_order>").append("<![CDATA[" + workOrder + "]]>")
						.append("</work_order>");
						valueXmlString.append("<dept_code>").append("<![CDATA[" + deptCode + "]]>")
						.append("</dept_code>");
						valueXmlString.append("<anal_code>").append("<![CDATA[" + getAbsString(analCode) + "]]>")
						.append("</anal_code>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

						setNodeValue(dom, "anal_code", getAbsString(analCode));
						reStr = itemChanged(dom, dom1, dom2, objContext, "anal_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangeddet("anal_code")

						setNodeValue(dom, "acct_code__dr", getAbsString(acctCode));
						reStr = itemChanged(dom, dom1, dom2, objContext, "acct_code__dr", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangeddet("acct_code__dr")

						setNodeValue(dom, "cctr_code__dr", getAbsString(cctr_code));
						reStr = itemChanged(dom, dom1, dom2, objContext, "cctr_code__dr", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangeddet("cctr_code__dr")

						valueXmlString.append("<proj_code>").append(projCode).append("</proj_code>");

						if (projCode != null && projCode.trim().length() > 0) {
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<proj_code protect = '1'>")
							 * .append(projCode).append("</proj_code>");
							 */
							valueXmlString.append("<proj_code protect = '0'>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code>");//// UNPROTECTED BY NANDKUMAR GADKARI ON 14/05/19
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						}



						sql = " Select descr from item Where item_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mfgitemDesc = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<item_code__mfg>")
						 * .append(itemCode).append("</item_code__mfg>");
						 * valueXmlString.append("<mfg_item_descr>") .append(mfgitemDesc)
						 * .append("</mfg_item_descr>");
						 */
						valueXmlString.append("<item_code__mfg>").append("<![CDATA[" + itemCode + "]]>")
						.append("</item_code__mfg>");
						valueXmlString.append("<mfg_item_descr>").append("<![CDATA[" + mfgitemDesc + "]]>")
						.append("</mfg_item_descr>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

						// valueXmlString.append("<item_code__mfg protect =
						// '1'>").append("").append("</item_code__mfg>");

						analCode = checkNull(genericUtility.getColumnValue("anal_code", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));

						if (mval1.length() > 0) {
							sql = " Select rate,discount from pquot_det Where quot_no = ? and item_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, mval1);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								mrate = rs.getString("rate");
								mdiscount = rs.getString("discount");
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
							} else {
								sql = " Select  purc_rate from item Where item_code = ?";
								pStmtNew = conn.prepareStatement(sql);
								pStmtNew.setString(1, itemCode);
								rsNew = pStmt.executeQuery();
								if (rsNew.next()) {
									mrate = rs.getString("purc_rate");
								}
								rsNew.close();
								rsNew = null;
								pStmtNew.close();
								pStmtNew = null;
							}
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<rate>").append(mrate) .append("</rate>");
							 * valueXmlString.append("<rate__clg>").append(mrate) .append("</rate__clg>");
							 * valueXmlString.append("<discount>") .append(mdiscount).append("</discount>");
							 */
							valueXmlString.append("<rate>").append("<![CDATA[" + mrate + "]]>").append("</rate>");
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + mrate + "]]>")
							.append("</rate__clg>");
							valueXmlString.append("<discount>").append("<![CDATA[" + mdiscount + "]]>")
							.append("</discount>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						} else {
							sql = " Select rate__ref from supplieritem Where supp_code = ? and item_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, mval2);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								mrate = rs.getString("rate__ref");
								// Commented by sarita on 15NOV2017 for open cursor issue [start]
								/*
								 * rs.close(); rs = null; pStmt.close(); pStmt = null;
								 */
								// Commented by sarita on 15NOV2017 for open cursor issue [end]
							} else {
								sql = " Select  purc_rate from item Where item_code = ?";
								pStmtNew = conn.prepareStatement(sql);
								pStmtNew.setString(1, itemCode);
								rsNew = pStmt.executeQuery();
								if (rsNew.next()) {

									mrate = rs.getString("purc_rate");
									System.out.println("mrate" + mrate);
								}
								rsNew.close();
								rsNew = null;
								pStmtNew.close();
								pStmtNew = null;
							}
							// Added by sarita on 15NOV2017 for open cursor issue[start]
							if (rs != null) {
								rs.close();
								rs = null;
							}
							if (pStmt != null) {
								pStmt.close();
								pStmt = null;
							}
							// Added by sarita on 15NOV2017 for open cursor issue[end]
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<rate>").append(mrate) .append("</rate>");
							 * valueXmlString.append("<rate__clg>").append(mrate) .append("</rate__clg>");
							 */
							valueXmlString.append("<rate>").append("<![CDATA[" + mrate + "]]>").append("</rate>");
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + mrate + "]]>")
							.append("</rate__clg>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

						}
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<quantity__stduom>")
						 * .append(pendqty).append("</quantity__stduom>");
						 */
						System.out.println("quantity stduom 7681>> " + utilMethods.getReqDecString(Double.parseDouble(pendqty),3));  //added by manish mhatre on 26-03-21
						System.out.println("quantity stduom 7682>> " +pendqty);  //added by manish mhatre on 26-03-21
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(Double.parseDouble(pendqty),3) + "]]>")
						.append("</quantity__stduom>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

						if (mrate == null || mrate.trim().length() == 0) {
							mrate = "0";
						}

						if (convQtystd == null || convQtystd.trim().length() == 0) {
							convQtystd = "0";
						}
						System.out.println("@@@@@ convQtystd[" + convQtystd + "]mrate[" + mrate + "]");
						convQtystdDob = Double.parseDouble(convQtystd == null ? "0" : convQtystd);
						mrateDou = Double.parseDouble(mrate);
						System.out.println("@@@@@7070 uom[" + uom + "]unitStd[" + unitStd + "]");
						if (uom != null && unitStd != null && !uom.trim().equalsIgnoreCase(unitStd.trim())) {

							System.out.println("inside if 7855");
							convQtystdDob = 0;
							// pendqty = gf_conv_qty_fact(unitStd, uom,
							// itemCode, pendqty, convQtystd)
							pendqtyDouble = Double.parseDouble(pendqty);
							System.out.println("@@@@@7070 uom[" + uom + "]unitStd[" + unitStd + "]itemCode[" + itemCode
									+ "]pendqtyDouble[" + pendqtyDouble + "]convQtystdDob[" + convQtystdDob + "]");
							pendqtyArr = distComm.getConvQuantityFact(unitStd, uom, itemCode, pendqtyDouble,
									convQtystdDob, conn);
							// Modified by Anjali R. on 15/10/2017 [To resolve the issue of item change
							// reported from Pitambari.As instructed by Piyush sir][Start]
							/*
							 * if (pendqtyArr.size() > 1) { valueXmlString.append("<quantity>")
							 * .append(pendqtyArr.get(1)) .append("</quantity>"); } else {
							 * valueXmlString.append("<quantity>").append("0") .append("</quantity>");
							 * 
							 * } } else { valueXmlString.append("<quantity>").append(pendqty)
							 * .append("</quantity>");
							 */
							if (pendqtyArr.size() > 1) {
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
								/*
								 * valueXmlString.append("<quantity>") .append(pendqtyArr.get(1))
								 * .append("</quantity>");
								 */
								valueXmlString.append("<quantity>").append("<![CDATA[" + pendqtyArr.get(1) + "]]>")
								.append("</quantity>");
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
								setNodeValue(dom, "quantity", getAbsString((String) pendqtyArr.get(1)));

							} else {
								valueXmlString.append("<quantity>").append("0").append("</quantity>");
								setNodeValue(dom, "quantity", getAbsString("0"));
							}
						} else {
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<quantity>").append(pendqty) .append("</quantity>");
							 */
							valueXmlString.append("<quantity>").append("<![CDATA[" + pendqty + "]]>")  
							.append("</quantity>");  
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
							setNodeValue(dom, "quantity", getAbsString(pendqty));
							System.out.println("inside if pendqty 7806"+pendqty);

							// Modified by Anjali R. on 15/10/2017 [To resolve the issue of item change
							// reported from Pitambari.As instructed by Piyush sir][End]]
						}

						System.out.println("@@@@@7180 pendqtyArr[" + pendqtyArr + "]");
						System.out.println("inside if pendqty 7814"+pendqty);  //addded by manish mhatre on 25-03-21
						System.out.println("inside if pendqty utilMethods.getReqDecString(Double.parseDouble(pendqty),3) 7815"+utilMethods.getReqDecString(Double.parseDouble(pendqty),3)); //addded by manish mhatre on 25-03-21

						if (uom != null && unitStd != null && !uom.trim().equalsIgnoreCase(unitStd.trim())) {
							convQtystdDob = 0;
							if ("R".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
								// lc_ratestduom = gf_conv_qty_fact1(ls_unitstd,
								// uom, itemCode, mrate, lc_conv,'Y')
								ratestduomArr = distComm.getConvQuantityFact(unitStd, uom, itemCode, mrateDou,
										convQtystdDob, conn);

							} else {
								// ratestduom = gf_conv_qty_fact1(ls_unitstd,
								// uom, itemCode, mrate, lc_conv,'N')
								ratestduomArr = distComm.convQtyFactor(unitStd, uom, itemCode, mrateDou, convQtystdDob,
										conn);
							}
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<conv__rtuom_stduom>") .append(convQtystd)
							 * .append("</conv__rtuom_stduom>"); valueXmlString.append("<rate__stduom>")
							 * .append(ratestduom) .append("</rate__stduom>");
							 */
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + convQtystd + "]]>")
							.append("</conv__rtuom_stduom>");
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + ratestduom + "]]>")
							.append("</rate__stduom>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						}

						sql = " Select descr, loc_code, unit, unit__pur from item Where item_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mdescr = checkNull(rs.getString("descr"));
							locCode = checkNull(rs.getString("loc_code"));
							uom = checkNull(rs.getString("unit"));

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (mval1 != null && mval1.trim().length() > 0) {
							sql = " Select enq_no from pquot_det Where quot_no = ? and item_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, mval1);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								enqNo = checkNull(rs.getString("enq_no"));

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (enqNo != null && enqNo.trim().length() > 0) {
								sql = " Select pack_code,pack_instr,supp_code__mnfr from enq_det "
										+ "Where enq_no = ? and item_code = ? and status = 'O'";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, enqNo);
								pStmt.setString(2, itemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									packCode = checkNull(rs.getString("pack_code"));
									packInstr = checkNull(rs.getString("pack_instr"));
									suppcodeMnfr = checkNull(rs.getString("supp_code__mnfr"));

								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
							}
						}

						if (reqdate != null && reqdate.trim().length() > 0) {
							reqdate = genericUtility.getValidDateString(reqdate, genericUtility.getDBDateFormat(),
									genericUtility.getApplDateFormat());
						}
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<item_descr>").append(mdescr)
						 * .append("</item_descr>"); valueXmlString.append("<loc_code>").append(locCode)
						 * .append("</loc_code>"); valueXmlString.append("<req_date>").append(reqdate)
						 * .append("</req_date>"); valueXmlString.append("<dlv_date>").append(reqdate)
						 * .append("</dlv_date>"); valueXmlString.append("<pack_code>").append(packCode)
						 * .append("</pack_code>");
						 * valueXmlString.append("<pack_instr>").append(packInstr)
						 * .append("</pack_instr>"); valueXmlString.append("<supp_code__mnfr>")
						 * .append(suppcodeMnfr) .append("</supp_code__mnfr>");
						 */
						valueXmlString.append("<item_descr>").append("<![CDATA[" + mdescr + "]]>")
						.append("</item_descr>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
						valueXmlString.append("<req_date>").append("<![CDATA[" + reqdate + "]]>").append("</req_date>");
						valueXmlString.append("<dlv_date>").append("<![CDATA[" + reqdate + "]]>").append("</dlv_date>");
						valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>")
						.append("</pack_code>");
						valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>")
						.append("</pack_instr>");
						valueXmlString.append("<supp_code__mnfr>").append("<![CDATA[" + suppcodeMnfr + "]]>")
						.append("</supp_code__mnfr>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						System.out.println("@@@@@@7001 itemCode[" + itemCode + "]");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<item_code>").append(itemCode)
						 * .append("</item_code>");
						 */
						valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>")
						.append("</item_code>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						setNodeValue(dom, "item_code", getAbsString(itemCode));

						// Modified by Anjali R. on 15/10/2017 [To resolve the issue of item change
						// reported from Pitambari as instructed by Piyush sir].Start

						setNodeValue(dom, "site_code", getAbsString(sitecodeDlv));
						setNodeValue(dom, "ind_no", getAbsString(mcode));
						setNodeValue(dom, "item_code__mfg", getAbsString(itemCode));
						setNodeValue(dom, "unit__std", getAbsString(unitStd));
						setNodeValue(dom, "rate", getAbsString(mrate));
						setNodeValue(dom, "unit__rate", getAbsString(uom));
						setNodeValue(dom, "conv__rtuom_stduom", getAbsString(convQtystd));
						// Modified by Anjali R. on 15/10/2017 [To resolve the issue of item change
						// reported from Pitambari as instructed by Piyush sir].End

						reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangeddet('item_code')

						//commented by manish mhatre on 16-4-21
						/*//added by manish mhatre on 25-03-2021
						//start manish

						int lineNoInddet=0;
						mcode=mcode.trim();
						System.out.println("ind No 7557>>>"+mcode);

						System.out.println("ind No lngth7559>> "+mcode.length()); 

						String indentNo=mcode.substring(0,mcode.length()-2);
						System.out.println("indentNo 7562>> "+indentNo); 

						String lineNoStr=mcode.substring(mcode.length()-2);
						System.out.println("line no str in int "+lineNoStr);     

						//int indentNoLength=indNo.length();

						if(lineNoStr!=null && lineNoStr.trim().length()>0)
						{
							lineNoInddet=Integer.parseInt(lineNoStr);
							System.out.println("line no in int after parse 7554 >>  "+lineNoInddet);

							lineNoInddet=lineNoInddet+1;
							System.out.println("line no in int after addition "+lineNoInddet);
						}
						System.out.println("indent no from indent det"+indentNo+"\n line nostr from indent det"+lineNo);
						System.out.println("line no in int from indent det"+lineNoInddet);


						String dimension="";
						double noArt=0;
						sql="select dimension,no_art From indent_det Where ind_no = ? and Line_no    =? ";
						pStmt1 = conn.prepareStatement(sql);
						pStmt1.setString(1, indentNo);
						pStmt1.setInt(2, lineNoInddet);
						rs1 = pStmt1.executeQuery();
						if (rs1.next()) 
						{
							dimension=rs1.getString("dimension");
							noArt=rs1.getDouble("no_art");
							//quantity=rs.getDouble("quantity");
						}
						rs1.close();
						rs1 = null;
						pStmt1.close();
						pStmt1 = null;
						System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);


						if(dimension!=null && dimension.trim().length()>0)
						{
							valueXmlString.append("<dimension>").append("<![CDATA[" + dimension + "]]>").append("</dimension>");
							setNodeValue(dom, "dimension", getAbsString(dimension));
						}
						if(noArt!=0)
						{
							valueXmlString.append("<no_art>").append("<![CDATA[" + noArt + "]]>").append("</no_art>");
							setNodeValue(dom, "no_art", noArt);
						}
						//end manish
						 */

						//added by manish mhatre on 16-4-21
						//start manish

						String dimension="";
						double noArt=0;

						sql = " Select dimension, no_art From indent Where ind_no = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							dimension = rs.getString("dimension");
							noArt = rs.getDouble("no_art");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);

						if(dimension!=null && dimension.trim().length()>0)
						{
							valueXmlString.append("<dimension>").append("<![CDATA[" + dimension + "]]>").append("</dimension>");
							setNodeValue(dom, "dimension", getAbsString(dimension));
						}
						if(noArt!=0)
						{
							valueXmlString.append("<no_art>").append("<![CDATA[" + noArt + "]]>").append("</no_art>");
							setNodeValue(dom, "no_art", noArt);
						}

						//end manish

					} else {
						valueXmlString.append("<item_code__mfg protect='0'>").append("").append("</item_code__mfg>");
					}

					if (mcode == null || mcode.trim().length() == 0) {
						valueXmlString.append("<proj_code>").append(" ").append("</proj_code>");
					}
					if (sitecodeDlv == null || sitecodeDlv.trim().length() == 0) {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<site_code>").append(siteCode)
						 * .append("</site_code>");
						 */
						valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>")
						.append("</site_code>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}

					/*
					 * sql =
					 * "select fn_get_budget_amt('P-ORD',:siteCode,:acctDr,:cctrDr,:analCode,:deptCode,'A'),"
					 * +
					 * " fn_get_cons_amt('P-ORD',:siteCode,:acctDr,:cctrDr,:analCode,:deptCode,'A') "
					 * + "from dual";
					 */
					sql = " select fn_get_budget_amt('P-ORD',?,?,?,?,?,'A'), "
							+ " fn_get_cons_amt('P-ORD',?,?,?,?,?,'A') " + " from dual";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCode);
					pStmt.setString(2, acctCode);
					pStmt.setString(3, cctr_code);
					pStmt.setString(4, analCode);
					pStmt.setString(5, deptCode);
					pStmt.setString(6, siteCode);
					pStmt.setString(7, acctCode);
					pStmt.setString(8, cctr_code);
					pStmt.setString(9, analCode);
					pStmt.setString(10, deptCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						budgetAmtanal = checkNull(rs.getString(1));
						consumedAmtanal = checkNull(rs.getString(2));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (budgetAmtanal == null || budgetAmtanal.trim().length() == 0) {
						budgetAmtanal = "0";
					}
					if (consumedAmtanal == null || consumedAmtanal.trim().length() == 0) {
						consumedAmtanal = "0";
					}
					System.out.println(
							"@@@@@7062 budgetAmtanal[" + budgetAmtanal + "]consumedAmtanal[" + consumedAmtanal + "]");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<budget_amt_anal>")
					 * .append(budgetAmtanal).append("</budget_amt_anal>");
					 * valueXmlString.append("<consumed_amt_anal>") .append(consumedAmtanal)
					 * .append("</consumed_amt_anal>");
					 */
					valueXmlString.append("<budget_amt_anal>").append("<![CDATA[" + budgetAmtanal + "]]>")
					.append("</budget_amt_anal>");
					valueXmlString.append("<consumed_amt_anal>").append("<![CDATA[" + consumedAmtanal + "]]>")
					.append("</consumed_amt_anal>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					budgetAmtanal1 = Double.parseDouble(budgetAmtanal);
					consumedAmtanal1 = Double.parseDouble(consumedAmtanal);

					budgetAmt = budgetAmtanal1 - consumedAmtanal1;
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<budget_amt>") .append(df.format(budgetAmt))
					 * .append("</budget_amt>");
					 */
					valueXmlString.append("<budget_amt>").append("<![CDATA[" + df.format(budgetAmt) + "]]>")
					.append("</budget_amt>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					System.out.println("@@@@@@@7083 itemCode[" + itemCode + "]");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<item_code>").append(itemCode)
					 * .append("</item_code>");
					 */
					valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
				}
				if (currentColumn.trim().equals("line_no__sord")) {
					mval = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					mval1 = checkNull(genericUtility.getColumnValue("sale_order", dom));
					// mval = Right(' '+ trim(mval),3) ;

					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<line_no__sord>").append(mval)
					 * .append("</line_no__sord>");
					 */
					valueXmlString.append("<line_no__sord>").append("<![CDATA[" + mval + "]]>")
					.append("</line_no__sord>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					sql = "select  item_code,quantity,unit from sorditem where sale_order = ? and line_no = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mval1);
					pStmt.setString(2, mval);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						saleItem = checkNull(rs.getString("item_code"));
						saleQty = checkNull(rs.getString("quantity"));
						saleUnit = checkNull(rs.getString("unit"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<item_code>").append(saleItem)
					 * .append("</item_code>");
					 */
					valueXmlString.append("<item_code>").append("<![CDATA[" + saleItem + "]]>").append("</item_code>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					sql = "select  descr,loc_code,item_ser from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, saleItem);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						itemDescr = rs.getString("descr");
						itemLoc = rs.getString("loc_code");
						itemSer = rs.getString("item_ser");

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = "select  dsp_date from sorddet where sale_order = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mval1);
					pStmt.setString(2, mval);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						despDate = rs.getString("dsp_date");

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					System.out.println("@@@@@7241 saleQty[" + saleQty + "]");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<item_descr>").append(itemDescr)
					 * .append("</item_descr>");
					 * valueXmlString.append("<req_date>").append(despDate) .append("</req_date>");
					 * valueXmlString.append("<dlv_date>").append(despDate) .append("</dlv_date>");
					 * valueXmlString.append("<loc_code>").append(itemLoc) .append("</loc_code>");
					 * valueXmlString.append("<quantity>").append(saleQty) .append("</quantity>");
					 * valueXmlString.append("<quantity__stduom>").append(saleQty)
					 * .append("</quantity__stduom>");
					 * valueXmlString.append("<unit>").append(saleUnit) .append("</unit>");
					 * valueXmlString.append("<unit__std>").append(saleUnit)
					 * .append("</unit__std>");
					 * valueXmlString.append("<unit__rate>").append(saleUnit)
					 * .append("</unit__rate>");
					 * valueXmlString.append("<conv__qty_stduom>").append("1")
					 * .append("</conv__qty_stduom>");
					 * valueXmlString.append("<conv__rtuom_stduom>").append("1")
					 * .append("</conv__rtuom_stduom>");
					 */
					valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr + "]]>")
					.append("</item_descr>");
					valueXmlString.append("<req_date>").append("<![CDATA[" + despDate + "]]>").append("</req_date>");
					valueXmlString.append("<dlv_date>").append("<![CDATA[" + despDate + "]]>").append("</dlv_date>");
					valueXmlString.append("<loc_code>").append("<![CDATA[" + itemLoc + "]]>").append("</loc_code>");
					valueXmlString.append("<quantity>").append("<![CDATA[" + saleQty + "]]>").append("</quantity>");
					//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + saleQty + "]]>").append("</quantity__stduom>");
					System.out.println("quantity stduom 8149>> " +saleQty);  //added by manish mhatre on 26-03-21
					System.out.println("quantity stduom 8150>> " +utilMethods.getReqDecString(Double.parseDouble(saleQty),3));  //added by manish mhatre on 26-03-21
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(Double.parseDouble(saleQty),3) + "]]>").append("</quantity__stduom>");
					valueXmlString.append("<unit>").append("<![CDATA[" + saleUnit + "]]>").append("</unit>");
					valueXmlString.append("<unit__std>").append("<![CDATA[" + saleUnit + "]]>").append("</unit__std>");
					valueXmlString.append("<unit__rate>").append("<![CDATA[" + saleUnit + "]]>")
					.append("</unit__rate>");
					valueXmlString.append("<conv__qty_stduom>").append("1").append("</conv__qty_stduom>");
					valueXmlString.append("<conv__rtuom_stduom>").append("1").append("</conv__rtuom_stduom>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					// gbf_itemchangeddet("item_code")
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<item_code>") .append(getAbsString(saleItem))
					 * .append("</item_code>");
					 */
					valueXmlString.append("<item_code>").append("<![CDATA[" + getAbsString(saleItem) + "]]>")
					.append("</item_code>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					setNodeValue(dom, "item_code", getAbsString(saleItem));
					reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);


				}
				if (currentColumn.trim().equals("item_code")) {
					mval = checkNull(genericUtility.getColumnValue("item_code", dom));

					// valueXmlString.append("<item_code>").append(getAbsString(mval)).append("</item_code>");
					// setNodeValue( dom, "item_code", getAbsString(mval) );

					/*
					 * reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag,
					 * xtraParams); pos = reStr.indexOf("<Detail1>"); reStr = reStr.substring(pos +
					 * 9); pos = reStr.indexOf("</Detail1>"); reStr = reStr.substring(0,pos);
					 * valueXmlString.append(reStr);
					 */
					// gbf_itemchangeddet2(as_fldname)

					// cpatil start

					/*
					 * setNull(ls_null) //ls_null added by Amit for DB2
					 * 
					 * 
					 * nvo_business_object_dist nvo_dis_obj if itm_structure.pur_comp > 0 then
					 * itm_app_server[itm_structure .pur_comp].createinstance(nvo_dis_obj) else
					 * nvo_dis_obj = create nvo_business_object_dist end if
					 * 
					 * ls_value = gf_getenv_dis('999999', 'UOM_ROUND') if ls_value = 'NULLFOUND'
					 * then populateerror(9999,'populateerror') errstr = 'VTUOMVARPARM' errstr =
					 * errstr +' ~t'+'Variable UOM_ROUND not defined under Distribution Environment
					 * Variables'
					 * 
					 * errstr = gf_error_location(errstr) end if
					 * 
					 * //END SHARON
					 * 
					 * choose case lower(trim(as_fldname))
					 */
					// Case "item_code"
					// gbf_itemchangeddet2(as_fldname)
					mcode = checkNull(genericUtility.getColumnValue("item_code", dom));
					// mcode =
					// dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].getrow(),
					// as_fldname)
					// ls_cont = dw_header.getitemstring(1,"contract_no")
					ls_cont = checkNull(genericUtility.getColumnValue("contract_no", dom1));
					// ls_item_ser = dw_header.getitemstring(1,"item_ser")
					ls_item_ser = checkNull(genericUtility.getColumnValue("contract_no", dom1));
					// ls_type = dw_header.getitemstring(1, "pord_type")
					ls_type = checkNull(genericUtility.getColumnValue("pord_type", dom1));
					// ls_sitecode =
					// dw_detedit[ii_currformno].Getitemstring(1,"site_code")//added
					// by prajakta 16/06/06
					ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom));

					// ls_ind_no =
					// dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].getrow(),
					// "ind_no")
					ls_ind_no = checkNull(genericUtility.getColumnValue("ind_no", dom));

					item_code__mfg = checkNull(genericUtility.getColumnValue("item_code__mfg", dom));

					if (ls_ind_no != null && ls_ind_no.trim().length() > 0) {
						// gbf_itemchg_modifier_ds
						// (dw_detedit[ii_currformno],"item_code__mfg","protect","1")
						valueXmlString.append("<item_code__mfg protect = \"1\">")
						.append("<![CDATA[" + item_code__mfg + "]]>").append("</item_code__mfg>");
					} else {
						valueXmlString.append("<item_code__mfg protect = \"0\">")
						.append("<![CDATA[" + item_code__mfg + "]]>").append("</item_code__mfg>");
						// gbf_itemchg_modifier_ds
						// (dw_detedit[ii_currformno],"item_code__mfg","protect","0")
					}

					// emp_code__qcaprv populated from item
					sql = "	Select descr, loc_code, unit, unit__pur, pack_code,pack_instr, item_ser,unit__rate " +
							// " into :mdescr, :mloc, :uom ,:ls_unitpur, :ls_pack1,:ls_packinstr1,
							// :ls_itemser,:ls_rate_unit "
							// +
							" from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						mdescr = checkNull(rs.getString("descr"));
						mloc = checkNull(rs.getString("loc_code"));
						uom = checkNull(rs.getString("unit"));
						ls_unitpur = checkNull(rs.getString("unit__pur"));
						ls_pack1 = checkNull(rs.getString("pack_code"));
						ls_packinstr1 = checkNull(rs.getString("pack_instr"));
						ls_itemser = checkNull(rs.getString("item_ser"));
						ls_rate_unit = checkNull(rs.getString("unit__rate"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = " select loc_code__aprv,loc_code__insp" +
							// " into :mloc__aprv, :mloc__insp " +
							" from   siteitem  " + "	where  site_code = ? " + " and    item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_sitecode);
					pStmt.setString(2, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						mloc__aprv = checkNull(rs.getString("loc_code__aprv"));
						mloc__insp = checkNull(rs.getString("loc_code__insp"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = " select case when qc_reqd is null then 'N' else qc_reqd end   " +
							// " into :ls_qc_reqd " +
							" from    siteitem " + " where    item_code = ? " + " and    site_code =  ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					pStmt.setString(2, ls_sitecode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						ls_qcreqd = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (ls_qcreqd == null || ls_qcreqd.trim().length() == 0) {
						sql = " select case when qc_reqd is null then 'N' else qc_reqd end " +
								// " into:ls_qc_reqd " +
								" from    item   where    item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							ls_qcreqd = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					}

					if ("Y".equalsIgnoreCase(ls_qcreqd) && mloc__insp != null && mloc__insp.trim().length() > 0) {
						// dw_detedit[ii_currformno].setitem(1,"loc_code",mloc__insp)
						valueXmlString.append("<loc_code protect = \"0\">").append("<![CDATA[" + mloc__insp + "]]>")
						.append("</loc_code>");
					} else if ("N".equalsIgnoreCase(ls_qcreqd) && mloc__aprv != null
							&& mloc__aprv.trim().length() > 0) {
						// dw_detedit[ii_currformno].setitem(1,"loc_code",mloc__aprv)
						valueXmlString.append("<loc_code protect = \"0\">").append("<![CDATA[" + mloc__aprv + "]]>")
						.append("</loc_code>");
					} else {
						valueXmlString.append("<loc_code protect = \"0\">").append("<![CDATA[" + mloc + "]]>")
						.append("</loc_code>");
						// dw_detedit[ii_currformno].Setitem(1,"loc_code", mloc)
					}
					// end if
					// end added by prajakta 16/06/06

					sql = " select emp_code__qcaprv	" +
							// " into :ls_emp_code__qcaprv " +
							" from  item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						ls_emp_code__qcaprv = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = " select emp_fname,emp_mname,emp_lname " +
							// " into :ls_empfname,:ls_empmname,:ls_emplname " +
							" from employee 	where emp_code= ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_emp_code__qcaprv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						ls_empfname = checkNull(rs.getString("emp_fname"));
						ls_empmname = checkNull(rs.getString("emp_mname"));
						ls_emplname = checkNull(rs.getString("emp_lname"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					ldt_orddateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
					// ldt_orddate = dw_header.getitemdatetime(1,"ord_date")
					// //zahid 26/02/04
					// ls_pricelist = gf_getenv_dis('999999','STD_PO_PL')
					ls_pricelist = distComm.getDisparams("999999", "STD_PO_PL", conn);

					if (ls_pricelist == null || "NULLFOUND".equalsIgnoreCase(ls_pricelist)) {
						// ls_pricelist =
						// dw_header.getitemString(1,"price_list")
						ls_pricelist = checkNull(genericUtility.getColumnValue("price_list", dom));
						// ls_unitstd =
						// dw_detedit[ii_currformno].getitemString(1,"unit__std")
						ls_unitstd = checkNull(genericUtility.getColumnValue("unit__std", dom));

						sql = "	select rate " +
								// " into :lc_stdrate " +
								" from pricelist " + " where price_list= ? and item_code= ? and unit= ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_pricelist);
						pStmt.setString(2, mcode);
						pStmt.setString(3, ls_unitstd);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							lc_stdrate = rs.getDouble("rate");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					} else // zahid 26/02/04
					{
						// lc_stdrate = nvo_dis_obj.gbf_pick_rate
						// (ls_pricelist,ldt_orddate,mcode,' ','L');
						lc_stdrate = distComm.pickRate(ls_pricelist, ldt_orddateStr, mcode, " ", "L", conn);
						// Added by Pavan R 1/DEC/17 Start
						System.out.println("ls_pricelist [" + ls_pricelist + "]");
						System.out.println("ldt_orddateStr [" + ldt_orddateStr + "]");
						System.out.println("mcode [" + mcode + "]");
						System.out.println("lc_stdrate [" + lc_stdrate + "]");
						// Added by Pavan R 1/DEC/17 End

					} // end if

					// dw_detedit[ii_currformno].setitem(1,"std_rate",lc_stdrate)
					// //zahid 26/02/04
					valueXmlString.append("<std_rate>").append("<![CDATA[" + lc_stdrate + "]]>").append("</std_rate>");

					// dw_detedit[ii_currformno].setitem(1,"emp_fname",ls_empfname)
					valueXmlString.append("<emp_fname>").append("<![CDATA[" + ls_empfname + "]]>")
					.append("</emp_fname>");
					// dw_detedit[ii_currformno].setitem(1,"emp_mname",ls_empmname)
					valueXmlString.append("<emp_mname>").append("<![CDATA[" + ls_empmname + "]]>")
					.append("</emp_mname>");
					// dw_detedit[ii_currformno].setitem(1,"emp_lname",ls_emplname)
					valueXmlString.append("<emp_lname>").append("<![CDATA[" + ls_emplname + "]]>")
					.append("</emp_lname>");
					// ended by prince james

					// dw_detedit[ii_currformno].SetItem(1,"item_descr", mdescr)
					valueXmlString.append("<item_descr>").append("<![CDATA[" + mdescr + "]]>").append("</item_descr>");
					// dw_detedit[ii_currformno].SetItem(1,"loc_code", mloc)
					// //commented by prajakta 16/06/06
					// dw_detedit[ii_currformno].setitem(1,"emp_code__qcaprv",ls_emp_code__qcaprv)
					valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + ls_emp_code__qcaprv + "]]>")
					.append("</emp_code__qcaprv>");

					if (ls_unitpur == null || ls_unitpur.trim().length() == 0) {
						ls_unitpur = uom;
					}

					if (ls_rate_unit == null || ls_rate_unit.trim().length() == 0
							|| "0".equalsIgnoreCase(ls_rate_unit.trim())) {
						ls_rate_unit = ls_unitpur;
					}

					// dw_detedit[ii_currformno].SetItem(1,"unit__std", uom)
					valueXmlString.append("<unit__std>").append("<![CDATA[" + uom + "]]>").append("</unit__std>");
					// Added by Pavan R 1/DEC/17 Start
					setNodeValue(dom, "unit__std", uom);
					// Added by Pavan R 1/DEC/17 End
					// dw_detedit[ii_currformno].SetItem(1,"unit", ls_unitpur)
					valueXmlString.append("<unit>").append("<![CDATA[" + ls_unitpur + "]]>").append("</unit>");
					// dw_detedit[ii_currformno].SetItem(1,"unit__rate",
					// ls_rate_unit)
					valueXmlString.append("<unit__rate>").append("<![CDATA[" + ls_rate_unit + "]]>")
					.append("</unit__rate>");
					// Added by Pavan R 1/DEC/17 Start
					setNodeValue(dom, "unit__rate", ls_rate_unit);
					// Added by Pavan R 1/DEC/17 End
					if (!uom.trim().equalsIgnoreCase(ls_unitpur.trim())) {
						// lc_qty = dw_detedit[ii_currformno].GetItemNumber(1,
						// "quantity")
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						if (qty != null && qty.trim().length() > 0) {
							lc_qty = Double.parseDouble(qty);
						}
						lc_temp = lc_qty;
						/*
						 * if(lc_qty == null ) { lc_qty = 1; } if( lc_rate == null ) { lc_rate = 1; }
						 */
						lc_conv = 0;

						if ("Q".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
							// lc_qtystduom = gf_conv_qty_fact1(ls_unitpur, uom,
							// mcode, lc_qty, lc_conv,'Y')
							lc_qtystduom = distComm.convQtyFactor(ls_unitpur, uom, mcode, lc_qty, lc_conv, conn);
						} else {
							// lc_qtystduom = gf_conv_qty_fact1(ls_unitpur, uom,
							// mcode, lc_qty, lc_conv,'N')
							lc_qtystduom = distComm.convQtyFactor(ls_unitpur, uom, mcode, lc_qty, lc_conv, conn);
						}
						// End If
						// End Sharon
						// lc_qtystduom = gf_conv_qty_fact(ls_unitpur, uom,
						// mcode, lc_qty, lc_conv)
						// dw_detedit[ii_currformno].SetItem(1,"conv__qty_stduom",
						// lc_conv)
						System.out.println("convQtystd 8483>> " + lc_conv);  //added by manish mhatre on 26-03-21
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + lc_conv + "]]>")
						.append("</conv__qty_stduom>");

						if (lc_temp != 0) {
							// dw_detedit[ii_currformno].setitem(1,"quantity__stduom",lc_qtystduom)
							//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + lc_qtystduom + "]]>").append("</quantity__stduom>");
							System.out.println("quantity stduom 8494>> " +lc_qtystduom);  //added by manish mhatre on 26-03-21
							System.out.println("quantity stduom 8495>> " + utilMethods.getReqDecString(Double.parseDouble(lc_qtystduom.toString()),3));  //added by manish mhatre on 26-03-21
							valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(Double.parseDouble(lc_qtystduom.toString()),3) + "]]>").append("</quantity__stduom>");
						}
						// end if
						rate = checkNull(genericUtility.getColumnValue("rate", dom));

						if (rate != null && rate.trim().length() > 0) {
							lc_rate = Double.parseDouble(rate);
						}
						// lc_rate = dw_detedit[ii_currformno].GetItemNumber(1,
						// "rate")
						lc_temp = lc_rate;
						lc_conv = 0;
						// SHARON 21-Aug-2003

						if ("R".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {// modification
							// done by
							// rahul
							// replacing
							// ls_unitpur
							// by
							// ls_rate_unit
							// lc_ratestduom
							// =
							// gf_conv_qty_fact1(uom,ls_rate_unit,
							// mcode,
							// lc_rate,
							// lc_conv,'Y')
							lc_ratestduomArrayList = distComm.convQtyFactor(uom, ls_rate_unit, mcode, lc_rate, lc_conv,
									conn);
						} else {// modification done by rahul replacing
							// ls_unitpur by ls_rate_unit
							// lc_ratestduom =
							// gf_conv_qty_fact1(uom,ls_rate_unit, mcode,
							// lc_rate, lc_conv,'N')
							lc_ratestduomArrayList = distComm.convQtyFactor(uom, ls_rate_unit, mcode, lc_rate, lc_conv,
									conn);
						} // End If
						// End Sharon
						// lc_ratestduom = gf_conv_qty_fact(uom,ls_unitpur,
						// mcode, lc_rate, lc_conv)

						// dw_detedit[ii_currformno].SetItem(1,"conv__rtuom_stduom",
						// lc_conv)
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lc_conv + "]]>")
						.append("</conv__rtuom_stduom>");

						if (lc_temp != 0) {
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + lc_ratestduomArrayList.get(0) + "]]>")
							.append("</rate__stduom>");
							// dw_detedit[ii_currformno].setitem(1,"rate__stduom",lc_ratestduom)
						}
						// end if

					} else {
						// dw_detedit[ii_currformno].setitem(1,"conv__qty_stduom",1)
						valueXmlString.append("<conv__qty_stduom>").append("1").append("</conv__qty_stduom>");
						// dw_detedit[ii_currformno].SetItem(1,"conv__rtuom_stduom",
						// 1)
						valueXmlString.append("<conv__rtuom_stduom>").append("1").append("</conv__rtuom_stduom>");
						// lc_rate = dw_detedit[ii_currformno].GetItemNumber(1,
						// "rate")
						String lc_rateStr = genericUtility.getColumnValue("rate", dom) == null ? "0"
								: genericUtility.getColumnValue("rate", dom);
						System.out.println("@@@7514 lc_rateStr[" + lc_rateStr + "]");

						if (lc_rateStr != null && lc_rateStr.trim().length() > 0) {
							lc_rate = Double.parseDouble(lc_rateStr);
						} else {
							lc_qty = 0;
						}

						// lc_qty = dw_detedit[ii_currformno].GetItemNumber(1,
						// "quantity")

						String lc_qtyStr = genericUtility.getColumnValue("quantity", dom) == null ? "0"
								: genericUtility.getColumnValue("quantity", dom);
						System.out.println("@@@7514 lc_qtyStr[" + lc_qtyStr + "]");

						if (lc_qtyStr != null && lc_qtyStr.trim().length() > 0) {
							lc_qty = Double.parseDouble(lc_qtyStr);
						} else {
							lc_qty = 0;
						}
						/*
						 * if isnull(lc_qty) { lc_qty = 1 } if isnull(lc_rate) { lc_rate = 1 }
						 */

						// dw_detedit[ii_currformno].setitem(1,"quantity__stduom",lc_qty)
						// dw_detedit[ii_currformno].setitem(1,"rate__stduom",lc_rate)

						System.out.println("Quantity__stduom15:: "+lc_qty);
						//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + lc_qty + "]]>").append("</quantity__stduom>");
						System.out.println("quantity stduom 8589>> " +lc_qty);  //added by manish mhatre on 26-03-21
						System.out.println("quantity stduom 848590>> " + utilMethods.getReqDecString((lc_qty),3));  //added by manish mhatre on 26-03-21
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString((lc_qty),3) + "]]>").append("</quantity__stduom>");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lc_rate + "]]>")
						.append("</rate__stduom>");

					} // end if

					// dw_detedit[ii_currformno].setitem(1,"pack_code",ls_pack)
					valueXmlString.append("<pack_code>").append("<![CDATA[" + ls_pack + "]]>").append("</pack_code>");
					// ls_pack =
					// dw_detedit[ii_currformno].getitemstring(1,"pack_code")
					ls_pack = checkNull(genericUtility.getColumnValue("pack_code", dom));

					if (ls_pack == null || ls_pack.trim().length() == 0) {
						valueXmlString.append("<pack_code>").append("<![CDATA[" + ls_pack1 + "]]>")
						.append("</pack_code>");
						// dw_detedit[ii_currformno].setitem(1,"pack_code",ls_pack1)
					}
					ls_packinstr = checkNull(genericUtility.getColumnValue("pack_instr", dom));
					// ls_packinstr =
					// dw_detedit[ii_currformno].getitemstring(1,"pack_instr")
					if (ls_packinstr == null || ls_packinstr.trim().length() == 0) {
						// dw_detedit[ii_currformno].setitem(1,"pack_instr",ls_packinstr1)
						valueXmlString.append("<pack_instr>").append("<![CDATA[" + ls_packinstr1 + "]]>")
						.append("</pack_instr>");
					}
					ls_ind_val = checkNull(genericUtility.getColumnValue("ind_no", dom));
					// ls_ind_val =
					// dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].getrow(),
					// "ind_no")

					if (ls_ind_val == null || ls_ind_val.trim().length() == 0) {
						// mcctr =
						// nvo_dis_obj.gbf_acct_detr_ttype(mcode,ls_itemser,'IN',
						// ls_type)

						if (mcode != null && mcode.trim().length() > 0) {
							mcctr = finCommon.getAcctDetrTtype(mcode, ls_itemser, "IN", ls_type, conn);

							System.out.println("@@@@@@@ mcctr[" + mcctr + "]");
							String mcctrArray[] = mcctr.split(",");
							System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
							if (mcctrArray.length > 0) {
								macct = mcctrArray[0];
								mcctr = "";
							}
							if (mcctrArray.length > 1) {
								macct = mcctrArray[0];
								mcctr = mcctrArray[1];
							}
						}
						// dw_detedit[ii_currformno].setitem(1,"acct_code__dr",macct)
						valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + macct + "]]>")
						.append("</acct_code__dr>");
						// dw_detedit[ii_currformno].setitem(1,"cctr_code__dr",mcctr)
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + mcctr + "]]>")
						.append("</cctr_code__dr>");
					}

					if (ls_cont.trim().length() > 0) {
						sql = " select max_qty,rate,unit,loc_code,tax_class,tax_chap,tax_env,"
								+ "	discount_type,discount,acct_code__dr,cctr_code__dr, "
								+ "	acct_code__cr,cctr_code__cr,bom_code" +
								// " into :lc_qty,:lc_rate,:uom,:mloc,:ls_taxclass,:ls_taxchap,:ls_taxenv,"
								// +
								// " :ls_disc_type,:lc_disc,:ls_acct_dr,:ls_cctr_dr,"
								// +
								// " :ls_acct_cr,:ls_cctr_cr,:ls_bom" +
								"	from pcontract_det" + "	where contract_no = ? and item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_cont);
						pStmt.setString(2, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							lc_qty = rs.getDouble("max_qty");
							lc_rate = rs.getDouble("rate");
							uom = rs.getString("unit");
							mloc = rs.getString("loc_code");
							ls_taxclass = rs.getString("tax_class");
							ls_taxchap = rs.getString("tax_chap");
							ls_taxenv = rs.getString("tax_env");
							ls_disc_type = rs.getString("discount_type");
							lc_disc = rs.getDouble("discount");
							ls_acct_dr = rs.getString("acct_code__dr");
							ls_cctr_dr = rs.getString("cctr_code__dr");
							ls_acct_cr = rs.getString("acct_code__cr");
							ls_cctr_cr = rs.getString("cctr_code__cr");
							ls_bom = rs.getString("bom_code");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						System.out.println("@@@@@7749 lc_qty[" + lc_qty + "]");
						// dw_detedit[ii_currformno].Setitem(1,"loc_code", mloc)
						valueXmlString.append("<loc_code>").append("<![CDATA[" + mloc + "]]>").append("</loc_code>");
						// dw_detedit[ii_currformno].Setitem(1,"unit__rate",
						// Uom)
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + Uom + "]]>").append("</unit__rate>");
						// dw_detedit[ii_currformno].setitem(1,"quantity",lc_qty)
						valueXmlString.append("<quantity>").append("<![CDATA[" + lc_qty + "]]>").append("</quantity>");
						// dw_detedit[ii_currformno].setitem(1,"quantity__stduom",lc_qty)

						//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + lc_qty + "]]>").append("</quantity__stduom>");
						System.out.println("quantity stduom 8695>> " +lc_qty);  //added by manish mhatre on 26-03-21
						System.out.println("quantity stduom 8696>> " + utilMethods.getReqDecString(lc_qty,3));  //added by manish mhatre on 26-03-21
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(lc_qty,3) + "]]>").append("</quantity__stduom>");
						// dw_detedit[ii_currformno].setitem(1,"rate__stduom",lc_rate)
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lc_rate + "]]>")
						.append("</rate__stduom>");
						// dw_detedit[ii_currformno].setitem(1,"conv__rtuom_stduom",1)
						valueXmlString.append("<conv__rtuom_stduom>").append("1").append("</conv__rtuom_stduom>");
						// dw_detedit[ii_currformno].setitem(1,"tax_class",ls_taxclass)
						valueXmlString.append("<tax_class>").append("<![CDATA[" + ls_taxclass + "]]>")
						.append("</tax_class>");
						// dw_detedit[ii_currformno].setitem(1,"tax_chap",ls_taxchap)
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + ls_taxchap + "]]>")
						.append("</tax_chap>");
						// dw_detedit[ii_currformno].setitem(1,"tax_env",ls_taxenv)
						valueXmlString.append("<tax_env>").append("<![CDATA[" + ls_taxenv + "]]>").append("</tax_env>");
						// dw_detedit[ii_currformno].setitem(1,"discount_type",ls_disc_type)
						valueXmlString.append("<discount_type>").append("<![CDATA[" + ls_disc_type + "]]>")
						.append("</discount_type>");
						// dw_detedit[ii_currformno].setitem(1,"discount",lc_disc)
						valueXmlString.append("<discount>").append("<![CDATA[" + lc_disc + "]]>").append("</discount>");
						// dw_detedit[ii_currformno].setitem(1,"bom_code",ls_bom)
						valueXmlString.append("<bom_code>").append("<![CDATA[" + ls_bom + "]]>").append("</bom_code>");
					} // end if
					// dw_detedit[ii_currformno].setitem(1,"contract_detail",ls_null)
					valueXmlString.append("<contract_detail>").append("<![CDATA[" + ls_null + "]]>")
					.append("</contract_detail>");

					ld_desp_dateStr = checkNull(genericUtility.getColumnValue("dlv_date", dom));
					if (ld_desp_dateStr != null && ld_desp_dateStr.trim().length() > 0) {
						ld_desp_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_desp_dateStr,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					// ld_desp_date =
					// dw_detedit[ii_currformno].getitemdatetime(1,"dlv_date")
					ls_cont = "";

					// declare contract_cur cursor for
					sql = " select pcontract_det.contract_no from pcontract_det, pcontract_hdr"
							+ "	where ( pcontract_det.contract_no = pcontract_hdr.contract_no ) "
							+ " and ( ( pcontract_det.item_code = ? ) " + " and ( pcontract_hdr.status = 'O' ) "
							+ " and ( pcontract_hdr.contract_fromdate <= ? ) "
							+ " and ( pcontract_hdr.contract_todate >= ? ) ) ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					pStmt.setTimestamp(2, ld_desp_date);
					pStmt.setTimestamp(3, ld_desp_date);
					rs = pStmt.executeQuery();
					while (rs.next()) {
						mVal1 = checkNull(rs.getString(1));
						ls_cont += mVal1.trim() + ", ";

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (ls_cont.trim().length() > 0) {
						ls_cont = ls_cont.substring(0, ls_cont.trim().length() - 1);
					}
					// beep(2)

					// dw_detedit[ii_currformno].setitem(1,"contract_detail",ls_cont)
					valueXmlString.append("<contract_detail>").append("<![CDATA[" + ls_cont + "]]>")
					.append("</contract_detail>");

					String lcRateStr = checkNull(genericUtility.getColumnValue("rate", dom)) == "" ? "0"
							: checkNull(genericUtility.getColumnValue("rate", dom));
					// lc_rate =
					// dw_detedit[ii_currformno].getitemdecimal(1,"rate")
					lc_rate = Double.parseDouble(lcRateStr);
					System.out.println("@@@@@@@@8000 lc_rate[" + lc_rate + "]");
					if (lc_rate == 0) {
						// ls_pricelist =
						// dw_header.getitemstring(1,"price_list")
						ls_pricelist = checkNull(genericUtility.getColumnValue("price_list", dom1));
						// ldt_orddate = dw_header.getitemdatetime(1,"ord_date")
						ldt_orddateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						// lc_qty =
						// dw_detedit[ii_currformno].getitemdecimal(1,"quantity")
						qtyStr = checkNull(genericUtility.getColumnValue("quantity", dom));

						if (qtyStr == null || qtyStr.trim().length() == 0) {
							qtyStr = "0";
							lc_qty = Double.parseDouble(qtyStr);
						}
						// ls_unitpur =
						// dw_detedit[ii_currformno].getitemstring(1,"unit__rate")
						ls_unitpur = genericUtility.getColumnValue("unit__rate", dom);
						System.out.println("Unit::[" + ls_unitpur + "]");
						if (ls_pricelist != null && ls_pricelist.trim().length() > 0) {
							// lc_rate = nvo_dis_obj.gbf_pick_rate(ls_pricelist,
							// ldt_orddate, mcode, ' ','L',lc_qty,ls_unitpur)
							System.out.println("POrderIc pickRate() called....");
							lc_rate = distComm.pickRate(ls_pricelist, ldt_orddateStr, mcode, " ", "L", lc_qty,
									ls_unitpur, conn);
							System.out.println("@9035 ls_pricelist [" + ls_pricelist + "]ldt_orddateStr ["
									+ ldt_orddateStr + "]mcode [" + mcode + "]lc_rate [" + lc_rate + "]");
							System.out.println("@@@@@@@7452 lc_rate[" + lc_rate + "] ");
							if (lc_rate > 0) {
								// dw_detedit[ii_currformno].setitem(1,"rate",lc_rate)
								valueXmlString.append("<rate>").append("<![CDATA[" + lc_rate + "]]>").append("</rate>");
								// dw_detedit[ii_currformno].setitem(1,"rate__clg",lc_rate)
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + lc_rate + "]]>")
								.append("</rate__clg>");
								// mVal1 =
								// dw_detedit[ii_currformno].GetItemString(1,
								// "unit__std")
								mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
								// lc_conv =
								// dw_detedit[ii_currformno].GetItemNumber(1,
								// "conv__rtuom_stduom")

								convRtuomStduom = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
								System.out.println("mVal1:-[" + mVal1 + "]");
								System.out.println("lsValue:-[" + lsValue + "]");

								if (convRtuomStduom != null && convRtuomStduom.trim().length() > 0) {
									lc_conv = Double.parseDouble(convRtuomStduom);
								}
								lc_convtemp = lc_conv;
								// SHARON 21-Aug-2003
								if ("R".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
									// lc_ratestduom = gf_conv_qty_fact1(mVal1,
									// ls_unitpur, mcode, lc_rate, lc_conv,'Y')
									lc_ratestduomArrayList = distComm.convQtyFactor(mVal1, ls_unitpur, mcode, lc_rate,
											lc_conv, conn);
								} else {
									// lc_ratestduom = gf_conv_qty_fact1(mVal1,
									// ls_unitpur, mcode, lc_rate, lc_conv,'N')
									System.out.println("lsValue != R OR B then convQtyFactor called at 9075.........");
									System.out.println("mVal1:-" + mVal1 + "|ls_unitpur:-" + ls_unitpur + "|lc_rate:-"
											+ lc_rate + "|lc_conv:-" + lc_conv);
									lc_ratestduomArrayList = distComm.convQtyFactor(mVal1, ls_unitpur, mcode, lc_rate,
											lc_conv, conn);
									System.out.println("lc_ratestduomArrayList....:-" + lc_ratestduomArrayList);
								}
								// End If
								// End Sharon
								// lc_ratestduom = gf_conv_qty_fact(mVal1,
								// ls_unitpur, mcode, lc_rate, lc_conv)

								// If Conversion factor is not entered change
								// it.
								System.out.println("lc_convtemp:--" + lc_convtemp);
								if (lc_convtemp == 0) {
									// dw_detedit[ii_currformno].SetItem(1,"conv__rtuom_stduom",
									// lc_conv)
									System.out.println("inside if in ls_convtemp...");
									valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lc_conv + "]]>")
									.append("</conv__rtuom_stduom>");
								}
								// end if
								// dw_detedit[ii_currformno].SetItem(1,"rate__stduom",
								// lc_ratestduom)
								System.out.println("lc_ratestduomArra--" + lc_ratestduomArrayList.get(1));
								System.out.println("lc_ratestduomArrayList.get(0)----:[1");
								System.out.println(
										"lc_ratestduomArrayList.get(0)----:[" + lc_ratestduomArrayList.get(0) + "]");
								System.out.println("lc_ratestduomArrayList.get(0)----:[2");

								valueXmlString.append("<rate__stduom>")
								.append("<![CDATA["
										+ Double.parseDouble(lc_ratestduomArrayList.get(1).toString()) + "]]>")
								.append("</rate__stduom>");
							} else {
								lc_rate = -1;
								// dw_detedit[ii_currformno].setitem(1,"rate",lc_rate)
								valueXmlString.append("<rate>").append("<![CDATA[" + lc_rate + "]]>").append("</rate>");
								// dw_detedit[ii_currformno].setitem(1,"rate__clg",lc_rate)
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + lc_rate + "]]>")
								.append("</rate__clg>");
							} // end if
							System.out.println("@@@@@@@7497 lc_rate[" + lc_rate + "] ");
						} // end if
					} // end if

					// mcode = dw_detedit[ii_currformno].GetItemString(1,
					// "ind_no")
					mcode = checkNull(genericUtility.getColumnValue("ind_no", dom));
					// mval = dw_header.getitemstring(1,"item_ser")
					mval = checkNull(genericUtility.getColumnValue("item_ser", dom1));
					// mval1 = dw_header.getitemstring(1,"quot_no")
					mval1 = checkNull(genericUtility.getColumnValue("quot_no", dom1));
					// mval2 = dw_header.getitemstring(1,"supp_code")
					mval2 = checkNull(genericUtility.getColumnValue("supp_code", dom1));
					// ls_projcode = dw_header.getitemstring(1,"proj_code")
					ls_projcode = checkNull(genericUtility.getColumnValue("proj_code", dom1));
					// itemCode = dw_detedit[ii_currformno].GetItemString(1,
					// "item_code")
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					// ls_cont = dw_header.getitemstring(1,"contract_no")
					ls_cont = checkNull(genericUtility.getColumnValue("contract_no", dom1));

					// get the existing cr a/c
					// in case pcontract is there will be taken from there

					// ls_cctr_cr =
					// dw_detedit[ii_currformno].getitemstring(1,"cctr_code__cr")
					ls_cctr_cr = checkNull(genericUtility.getColumnValue("cctr_code__cr", dom));
					// ls_acct_cr =
					// dw_detedit[ii_currformno].getitemstring(1,"acct_code__cr")
					ls_acct_cr = checkNull(genericUtility.getColumnValue("acct_code__cr", dom));

					if (mcode != null && mcode.trim().length() > 0) {
						sql = " Select acct_code, cctr_code" +
								// " into :ls_acct_dr, :ls_cctr_dr " +
								" from indent where ind_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							ls_acct_dr = checkNull(rs.getString("acct_code"));
							ls_cctr_dr = rs.getString("cctr_code");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					} // end if

					if (ls_acct_dr == null || ls_acct_dr.trim().length() == 0) {
						// indent acct_code blank
						// check in pcontract if neccessary
						if (ls_cont != null && ls_cont.trim().length() > 0) {
							sql = " select acct_code__dr,cctr_code__dr,	acct_code__cr,cctr_code__cr" +
									// " into :ls_acct_dr,:ls_cctr_dr, :ls_acct_cr,:ls_cctr_cr"
									// +
									"	from pcontract_det " + " where contract_no = ? and item_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, ls_cont);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								ls_acct_dr = checkNull(rs.getString("acct_code__dr"));
								ls_cctr_dr = rs.getString("cctr_code__dr");
								ls_acct_cr = checkNull(rs.getString("acct_code__cr"));
								ls_cctr_cr = rs.getString("cctr_code__cr");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

						} // end if

						if (ls_acct_dr == null || ls_acct_dr.trim().length() == 0) {// indent/pcontract
							// acct_code
							// blank
							// check
							// in
							// project
							// if
							// neccessary
							if (ls_projcode != null && ls_projcode.trim().length() > 0) {
								sql = " select acct_code,cctr_code " +
										// " into :ls_acct_dr,:ls_cctr_dr " +
										" from project " + " where proj_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, ls_projcode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									ls_acct_dr = checkNull(rs.getString("acct_code"));
									ls_cctr_dr = rs.getString("cctr_code");
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

							} // end if
						} // end if
						//
						if (ls_acct_dr == null || ls_acct_dr.trim().length() == 0) {
							// ls_cctr_dr =
							// nvo_dis_obj.gbf_acct_detr_ttype(itemCode,mval,
							// 'IN', ls_type)
							// ls_cctr_dr =
							// nvo_dis_obj.gbf_acct_detr_ttype(itemCode,ls_itemser,
							// 'IN', ls_type)

							ls_cctr_dr = finCommon.getAcctDetrTtype(itemCode, ls_itemser, "IN", ls_type, conn);
							System.out.println("@@@@@@@ ls_cctr_dr[" + ls_cctr_dr + "]");
							if (ls_cctr_dr != null && ls_cctr_dr.trim().length() > 0) {
								String ls_cctr_drArray[] = ls_cctr_dr.split(",");
								System.out.println("@@@@@ ls_cctr_drArray.length[" + ls_cctr_drArray.length + "]");
								if (ls_cctr_drArray.length > 0) {
									ls_acct_dr = ls_cctr_drArray[0];
									ls_cctr_dr = "";
								}
								if (ls_cctr_drArray.length > 1) {
									ls_acct_dr = ls_cctr_drArray[0];
									ls_cctr_dr = ls_cctr_drArray[1];
								}

							}
							// ls_acct_dr = f_get_token(ls_cctr_dr,'~t')
						} // end if
					} // end if
					// ls_invacct =
					// trim(gf_getfinparm('999999','INV_ACCT_PORCP'))
					ls_invacct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
					if (ls_invacct == null || "NULLFOUND".equalsIgnoreCase(ls_invacct)
							|| ls_invacct.trim().length() == 0) {
						ls_invacct = "N";
					}
					// 14-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					// inv_acct of the itemser of the item
					if ("S".equals(ls_invacct)) {
						sql = " select inv_acct from itemser where item_ser = ?  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_itemser);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							invAcctSer = checkNull(rs.getString("inv_acct"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (invAcctSer == null || invAcctSer.trim().length() == 0) {
							invAcctSer = "N";
						}
						ls_invacct = invAcctSer;
					}
					// end 14-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					// inv_acct of the itemser of the item

					// ls_invacct_qc =
					// trim(gf_getfinparm('999999','INV_ACCT_QCORDER'))
					ls_invacct_qc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

					if (ls_invacct_qc == null || "NULLFOUND".equalsIgnoreCase(ls_invacct_qc)
							|| ls_invacct_qc.trim().length() == 0) {
						ls_invacct_qc = "N";
					}
					// end if

					if (ls_acct_cr == null || ls_acct_cr.trim().length() == 0) {
						if ("Y".equalsIgnoreCase(ls_invacct) && !"Y".equalsIgnoreCase(ls_invacct_qc)) {
							// ls_cctr_cr =
							// nvo_object.gbf_acct_detr_ttype(itemCode,ls_itemser,'PORCP',
							// ls_type)

							ls_cctr_cr = finCommon.getAcctDetrTtype(itemCode, ls_itemser, "PORCP", ls_type, conn);
							System.out.println("@@@@@@@ ls_cctr_cr[" + ls_cctr_cr + "]");
							String ls_cctr_crArray[] = ls_cctr_cr.split(",");
							System.out.println("@@@@@ ls_cctr_crArray.length[" + ls_cctr_crArray.length + "]");
							if (ls_cctr_crArray.length > 0) {
								ls_acct_cr = ls_cctr_crArray[0];
								ls_cctr_cr = "";
							}
							if (ls_cctr_crArray.length > 1) {
								ls_acct_cr = ls_cctr_crArray[0];
								ls_cctr_cr = ls_cctr_crArray[1];
							}

							// ls_acct_cr = f_get_token(ls_cctr_cr,'~t')
						} else {
							// ls_cctr_cr =
							// nvo_object.gbf_acct_detr_ttype(itemCode,mval,'PO',
							// ls_type)
							// ls_cctr_cr =
							// nvo_object.gbf_acct_detr_ttype(itemCode,ls_itemser,'PO',
							// ls_type)

							ls_cctr_cr = finCommon.getAcctDetrTtype(itemCode, ls_itemser, "PO", ls_type, conn);
							System.out.println("@@@@@@@ ls_cctr_cr[" + ls_cctr_cr + "]");
							String ls_cctr_crArray[] = ls_cctr_cr.split(",");
							System.out.println("@@@@@ ls_cctr_crArray.length[" + ls_cctr_crArray.length + "]");
							if (ls_cctr_crArray.length > 0) {
								ls_acct_cr = ls_cctr_crArray[0];
								ls_cctr_cr = "";
							}
							if (ls_cctr_crArray.length > 1) {
								ls_acct_cr = ls_cctr_crArray[0];
								ls_cctr_cr = ls_cctr_crArray[1];
							}

							// ls_acct_cr = f_get_token(ls_cctr_cr,'~t')

							if (ls_acct_cr == null || ls_acct_cr.trim().length() == 0) {
								sql = "	select acct_code__ap , 	cctr_code__ap " +
										// " into :ls_acct_cr , :ls_cctr_cr "
										// +
										" from   supplier where  supp_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, mval2);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									ls_acct_cr = checkNull(rs.getString("acct_code__ap"));
									ls_cctr_cr = rs.getString("cctr_code__ap");
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

							} // End if
							// //Added to set acct_code__cr, cctr_code__cr
							// from supplier master, Ruchira
							// 10/05/2k7(DI78DIS010).
						} // end if
					} // end if

					// dw_detedit[ii_currformno].setitem(1,"acct_code__dr",ls_acct_dr)
					valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + ls_acct_dr + "]]>")
					.append("</acct_code__dr>");
					// dw_detedit[ii_currformno].setitem(1,"cctr_code__dr",ls_cctr_dr)
					valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + ls_cctr_dr + "]]>")
					.append("</cctr_code__dr>");
					// dw_detedit[ii_currformno].setitem(1,"acct_code__cr",ls_acct_cr)
					valueXmlString.append("<acct_code__cr>").append("<![CDATA[" + ls_acct_cr + "]]>")
					.append("</acct_code__cr>");
					// dw_detedit[ii_currformno].setitem(1,"cctr_code__cr",ls_cctr_cr)
					valueXmlString.append("<cctr_code__cr>").append("<![CDATA[" + ls_cctr_cr + "]]>")
					.append("</cctr_code__cr>");
					// Added by fatema - 31/03/2007 - DI7SUN0018

					// gbf_itemchangeddet("acct_code__dr")
					reStr = itemChanged(dom, dom1, dom2, objContext, "acct_code__dr", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					// gbf_itemchangeddet("cctr_code__dr")
					reStr = itemChanged(dom, dom1, dom2, objContext, "cctr_code__dr", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					// gbf_itemchangeddet("acct_code__cr")
					reStr = itemChanged(dom, dom1, dom2, objContext, "acct_code__cr", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					// gbf_itemchangeddet("cctr_code__cr")
					reStr = itemChanged(dom, dom1, dom2, objContext, "cctr_code__cr", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					// ls_suppcode = dw_header.getitemstring(1,"supp_code")
					ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
					// ls_sitecode =
					// dw_detedit[ii_currformno].Getitemstring(1,"site_code")
					ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom));

					sql = " select tax_class, tax_env " +
							// " into :ls_taxclass, :ls_taxenv " +
							" from site_supplier " + "	where site_code = ? and supp_code = ?  ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_sitecode);
					pStmt.setString(2, ls_suppcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						ls_taxclass = checkNull(rs.getString("tax_class"));
						ls_taxenv = rs.getString("tax_env");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (ls_taxclass != null && ls_taxclass.trim().length() > 0) {
						// dw_detedit[ii_currformno].setitem(1, "tax_class",
						// ls_taxclass)
						valueXmlString.append("<tax_class>").append("<![CDATA[" + ls_taxclass + "]]>")
						.append("</tax_class>");
					}

					if (ls_taxenv != null && ls_taxenv.trim().length() > 0) {
						// dw_detedit[ii_currformno].setitem(1, "tax_env",
						// ls_taxenv)
						valueXmlString.append("<tax_env>").append("<![CDATA[" + ls_taxenv + "]]>").append("</tax_env>");
					}
					// Modified by Anjali R.[To set columnValue in current dom][21/11/2017][Start]
					updateNodeValue(dom, "tax_class", ls_taxclass, "2");
					updateNodeValue(dom, "tax_env", ls_taxenv, "2");
					// Modified by Anjali R.[To set columnValue in current dom][21/11/2017][End]

					// ls_itemCode =
					// dw_detedit[ii_currformno].Getitemstring(1,"item_code")
					ls_itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					// ls_taxchap =
					// dw_detedit[ii_currformno].Getitemstring(1,"tax_chap")
					ls_taxchap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					// ls_taxclass =
					// dw_detedit[ii_currformno].Getitemstring(1,"tax_class")
					ls_taxclass = checkNull(genericUtility.getColumnValue("tax_class", dom));
					// ls_taxenv =
					// dw_detedit[ii_currformno].Getitemstring(1,"tax_env")
					ls_taxenv = checkNull(genericUtility.getColumnValue("tax_env", dom));
					// ls_taxclasshdr = dw_header.getitemstring(1,"tax_class")
					ls_taxclasshdr = checkNull(genericUtility.getColumnValue("tax_class", dom1));
					// ls_taxchaphdr = dw_header.getitemstring(1,"tax_chap")
					ls_taxchaphdr = checkNull(genericUtility.getColumnValue("tax_chap", dom1));
					// ls_taxenvhdr = dw_header.getitemstring(1,"tax_env")
					ls_taxenvhdr = checkNull(genericUtility.getColumnValue("tax_env", dom1));

					/*if (ls_taxchap == null || ls_taxchap.trim().length() == 0) {
						if (ls_taxchaphdr == null || ls_taxchaphdr.trim().length() == 0) {
							// dw_detedit[ii_currformno].setitem(1, "tax_chap",
							// gf_get_taxchap(ls_itemCode, ls_itemser, 'S',
							// ls_suppcode,ls_sitecode))
							valueXmlString.append("<tax_chap>").append("<![CDATA[" + checkNull(
									distComm.getTaxChap(ls_itemCode, ls_itemser, "S", ls_suppcode, ls_sitecode, conn))
									+ "]]>").append("</tax_chap>");
						} else {
							// dw_detedit[ii_currformno].setitem(1, "tax_chap",
							// ls_taxchaphdr)
							valueXmlString.append("<tax_chap>").append("<![CDATA[" + ls_taxchaphdr + "]]>")
									.append("</tax_chap>");
						} // end if
					} // end if //commented by nandkumar gadkari on 02/01/20
					 */
					// taxchap  condition added by  by nandkumar gadkari on 02/01/20
					ls_taxchap=checkNull(distComm.getTaxChap(ls_itemCode, ls_itemser, "S", ls_suppcode, ls_sitecode, conn));
					if (ls_taxchap == null || ls_taxchap.trim().length() == 0) 
					{
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + ls_taxchaphdr + "]]>").append("</tax_chap>");
					}
					else
					{
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + ls_taxchap + "]]>").append("</tax_chap>");
					}

					if (ls_taxclass == null || ls_taxclass.trim().length() == 0) {
						if (ls_taxclasshdr == null || ls_taxclasshdr.trim().length() == 0) {
							// dw_detedit[ii_currformno].setitem(1, "tax_class",
							// gf_get_taxclass('S',ls_suppcode,ls_itemCode,ls_sitecode))
							// Commented and added by Varsha v on 24-05-18 to resolve wrong tax_class
							// setting issue
							/*
							 * valueXmlString .append("<tax_chap>") .append("<![CDATA[" +
							 * checkNull(distComm.getTaxClass( "S", ls_suppcode, ls_itemCode, ls_sitecode,
							 * conn)) + "]]>") .append("</tax_chap>");
							 */
							valueXmlString.append("<tax_class>")
							.append("<![CDATA[" + checkNull(
									distComm.getTaxClass("S", ls_suppcode, ls_itemCode, ls_sitecode, conn))
							+ "]]>")
							.append("</tax_class>");
							// ended by Varsha v on 24-05-18 to resolve wrong tax_class setting issue
						} else {
							// dw_detedit[ii_currformno].setitem(1,
							// "tax_class",ls_taxclasshdr)
							valueXmlString.append("<tax_class>").append("<![CDATA[" + ls_taxclasshdr + "]]>")
							.append("</tax_class>");
						} // end if
					} // end if

					if (ls_taxenv == null || ls_taxenv.trim().length() == 0) {
						if (ls_taxenvhdr == null || ls_taxenvhdr.trim().length() == 0) {
							// ls_suppcode = dw_header.Getitemstring(1,
							// "supp_code")
							ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));

							sql = " select tax_env " +
									// " into :ls_taxenv " +
									" from supplieritem " + " where supp_code = ? and item_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, ls_suppcode);
							pStmt.setString(2, ls_itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								ls_taxenv = rs.getString("tax_env");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							// //Added to pick up column tax env from supplier
							// master also, Ruchira 23/09/2k6(DI6MJB0008).
							if (ls_taxenv != null && ls_taxenv.trim().length() > 0) {
								// dw_detedit[ii_currformno].setitem(1,
								// "tax_env", ls_taxenv)
								valueXmlString.append("<tax_env>").append("<![CDATA[" + ls_taxenv + "]]>")
								.append("</tax_env>");
							} else {
								sql = " select tax_env " +
										// " into :ls_taxenv " +
										" from supplier where supp_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, ls_suppcode);
								rs = pStmt.executeQuery();
								if (rs.next()) {
									ls_taxenv = rs.getString("tax_env");
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (ls_taxenv != null && ls_taxenv.trim().length() > 0) {
									valueXmlString.append("<tax_env>").append("<![CDATA[" + ls_taxenv + "]]>")
									.append("</tax_env>");
								} else {
									// ls_taxchap =
									// dw_detedit[ii_currformno].Getitemstring(1,
									// "tax_chap")
									ls_taxchap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
									// ls_taxclass =
									// dw_detedit[ii_currformno].Getitemstring(1,
									// "tax_class")
									ls_taxclass = checkNull(genericUtility.getColumnValue("tax_class", dom));
									// ls_stationfr = dw_header.Getitemstring(1,
									// "station_stan_code")
									ls_stationfr = checkNull(genericUtility.getColumnValue("station_stan_code", dom1));

									sql = "	select stan_code " +
											// " into :ls_stationto " +
											"	from site where site_code = ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, ls_sitecode);
									rs = pStmt.executeQuery();
									if (rs.next()) {
										ls_stationto = rs.getString("stan_code");
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;

									// dw_detedit[ii_currformno].setitem(1,
									// "tax_env", gf_get_taxenv(ls_stationfr,
									// ls_stationto, ls_taxchap,
									// ls_taxclass,ls_sitecode))
									valueXmlString.append("<tax_env>")
									.append("<![CDATA[" + distComm.getTaxEnv(ls_stationfr, ls_stationto,
											ls_taxchap, ls_taxclass, ls_sitecode, conn) + "]]>")
									.append("</tax_env>");
								} // end if
							} // end if
							// //End Added to pick up column tax env from
							// supplier master also, Ruchira
							// 23/09/2k6(DI6MJB0008).

						} else {
							// dw_detedit[ii_currformno].setitem(1, "tax_env",
							// ls_taxenvhdr)
							valueXmlString.append("<tax_env>").append("<![CDATA[" + ls_taxenvhdr + "]]>")
							.append("</tax_env>");
						}
						// end if
					} // end if

					// ls_costctr =
					// dw_detedit[ii_currformno].GetitemString(1,"cctr_code__dr")
					ls_costctr = checkNull(genericUtility.getColumnValue("cctr_code__dr", dom));

					if (ls_costctr == null || ls_costctr.trim().length() == 0) {
						// ls_costctr =
						// dw_detedit[ii_currformno].GetitemString(1,"cctr_code__cr")
						ls_costctr = checkNull(genericUtility.getColumnValue("cctr_code__cr", dom));
					}
					// end if
					// ls_costctr_as_loccode =
					// gf_getenv_dis('999999','CCENTER_AS_LOCATION')
					ls_costctr_as_loccode = distComm.getDisparams("999999", "CCENTER_AS_LOCATION", conn);

					if (!"NULLFOUND".equalsIgnoreCase(ls_costctr_as_loccode)
							&& "Y".equalsIgnoreCase(ls_costctr_as_loccode)) {
						// mcode =
						// dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].getrow(),
						// "item_code")
						mcode = checkNull(genericUtility.getColumnValue("item_code", dom));
						// select nvl(qc_reqd,'N') into :ls_qc_reqd from item
						// where item_code = :mcode;
						sql = "	select (case when qc_reqd is null then 'N' else qc_reqd end) " +
								// " into :ls_qc_reqd " +
								" from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							ls_qc_reqd = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if ("Y".equalsIgnoreCase(ls_qc_reqd)) {
							ls_Cctr_Loccode = ls_costctr.trim() + "Q";
							// dw_detedit[ii_currformno].Setitem(1,"loc_code",ls_Cctr_Loccode)
							valueXmlString.append("<loc_code>").append("<![CDATA[" + ls_Cctr_Loccode + "]]>")
							.append("</loc_code>");
						} else {
							ls_Cctr_Loccode = ls_costctr;
							// dw_detedit[ii_currformno].Setitem(1,"loc_code",ls_Cctr_Loccode)
							valueXmlString.append("<loc_code>").append("<![CDATA[" + ls_Cctr_Loccode + "]]>")
							.append("</loc_code>");
						} // end if
						// Jiten 18-Aug-04 (Changed the protect keyword)
						// dw_detedit[ii_currformno].modify("loc_code.Protect=1")
						// gbf_itemchg_modifier_ds
						// (dw_detedit[ii_currformno],"loc_code","protect","1")
						valueXmlString.append("<loc_code protect = \"1\">")
						.append("<![CDATA[" + ls_Cctr_Loccode + "]]>").append("</loc_code>");
						// dw_detedit[ii_currformno].modify("loc_code.background.color="
						// + String(RGB(192,192,192)) )
						// dw_detedit[ii_currformno].modify("loc_code.color=" +
						// String(RGB(255,0,0)) )
					} // end if

					// gbf_pricelistitemcheck(ls_itemCode)
					pricelistitemcheck(ls_itemCode, editFlag, dom, conn);

					// 15/11/02
					// mcode = dw_detedit[ii_currformno].GetItemString(1,
					// "item_code")
					mcode = checkNull(genericUtility.getColumnValue("item_code", dom));

					// mdescr = gf_get_desc_specs(mcode)
					mdescr = getDescrSpeces(mcode, conn);

					// dw_detedit[ii_currformno].setitem(1,"spl_instr",mdescr)
					valueXmlString.append("<spl_instr>").append("<![CDATA[" + mdescr + "]]>").append("</spl_instr>");
					// 15/11/02 end
					// rahul 03/03/04
					// wf_itemchangeddet("unit__rate")
					// end rahul
					// ////////////added by prajakta 29/03/06/////////////////
					// ls_price_list__clg =
					// dw_header.getitemString(1,"price_list__clg")
					ls_price_list__clg = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));

					// lc_rate__clg = nvo_dis_obj.gbf_pick_rate
					// (ls_price_list__clg,ldt_orddate,mcode,' ','L')
					lc_rate__clg = distComm.pickRate(ls_price_list__clg, ldt_orddateStr, mcode, " ", "L", conn);
					System.out.println("@@9755 lc_rate__clg [" + lc_rate__clg + "]");

					// added by prajakta 06/05/06
					if (lc_rate__clg == -1) {
						// gbf_pricelistitemcheck(ls_itemCode)
						pricelistitemcheck(ls_itemCode, editFlag, dom, conn);
					} else {
						// dw_detedit[ii_currformno].setitem(1,"rate__clg",lc_rate__clg)
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + lc_rate__clg + "]]>")
						.append("</rate__clg>");
					} // end if
					// added by darshan 28-09-07 di78sun089
					// ls_ind_no = dw_detedit[1].Getitemstring(1,"ind_no")
					ls_ind_no = checkNull(genericUtility.getColumnValue("ind_no", dom));
					// ls_item_code = dw_detedit[1].Getitemstring(1,"item_code")
					ls_item_code = checkNull(genericUtility.getColumnValue("item_code", dom));

					sql = " select supp_code__mnfr " +
							// " into :ls_supp_code__mnfr " +
							" from indent  where " + " ind_no 	= ? and item_code= ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_ind_no);
					pStmt.setString(2, ls_item_code);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						ls_supp_code__mnfr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					// ll_findstr= pos(ls_supp_code__mnfr,',')
					// ll_findstr= ls_supp_code__mnfr.contains(",")
					if (ls_supp_code__mnfr.contains(",")) {
						ll_findstr = 1;
					} else {
						ll_findstr = 0;
					}

					if (ll_findstr > 0) {
						ls_supp_code__mnfr = "";
					}
					// dw_detedit[ii_currformno].setitem(1,"supp_code__mnfr",trim(ls_supp_code__mnfr))///trim
					// added by jasmina 14/01/2008 -di78sun089
					valueXmlString.append("<supp_code__mnfr>").append("<![CDATA[" + ls_supp_code__mnfr.trim() + "]]>")
					.append("</supp_code__mnfr>");

					sql = " select item_code__ref " +
							// " into :ls_itemref " +
							" from supplieritem " + " where supp_code = ? and	item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_suppcode);
					pStmt.setString(2, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						ls_itemref = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (ls_itemref == null || ls_itemref.trim().length() == 0) {
						ls_itemref = mcode;
					}
					// dw_detedit[ii_currformno].setitem(1,"supp_item__ref",trim(ls_itemref))
					valueXmlString.append("<supp_item__ref>").append("<![CDATA[" + ls_itemref.trim() + "]]>")
					.append("</supp_item__ref>");

					ld_std_code = lc_qty * lc_stdrate;
					// lc_actual_cost =
					// dw_detedit[ii_currformno].GetItemNumber(1, "actual_cost")
					lc_actual_cost = Double.parseDouble(genericUtility.getColumnValue("actual_cost", dom) == null ? "0"
							: genericUtility.getColumnValue("actual_cost", dom));
					// dw_detedit[ii_currformno].setitem(1,"std_cost",ld_std_code)
					valueXmlString.append("<std_cost>").append("<![CDATA[" + ld_std_code + "]]>").append("</std_cost>");
					// dw_detedit[ii_currformno].setitem(1,"varience",ld_std_code
					// - lc_actual_cost)
					valueXmlString.append("<varience>").append("<![CDATA[" + (ld_std_code - lc_actual_cost) + "]]>")
					.append("</varience>");

					// end choose

					// cpatil end

					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					// siteCode =
					// checkNull(genericUtility.getColumnValue("site_code",
					// dom));

					if (ordDate != null && ordDate.trim().length() > 0) {
						ordDate2 = Timestamp.valueOf(genericUtility.getValidDateString(ordDate,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}

					sql = "select DDF_GET_LASTPURRATE(?,?,?) from dual";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCode);
					// pStmt.setString(2, ordDate);
					pStmt.setTimestamp(2, ordDate2);
					pStmt.setString(3, siteCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						lastPurcRate = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					sql = "select DDF_GET_PO(?,?,?) from dual";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCode);
					// pStmt.setString(2, ordDate);
					pStmt.setTimestamp(2, ordDate2);
					pStmt.setString(3, siteCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						lastPurcPo = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<last_purc_rate>")
					 * .append(lastPurcRate).append("</last_purc_rate>");
					 * valueXmlString.append("<last_purc_po>").append(lastPurcPo)
					 * .append("</last_purc_po>");
					 * 
					 * valueXmlString.append("<item_code>").append(itemCode)
					 * .append("</item_code>");
					 */
					valueXmlString.append("<last_purc_rate>").append("<![CDATA[" + lastPurcRate + "]]>")
					.append("</last_purc_rate>");
					valueXmlString.append("<last_purc_po>").append("<![CDATA[" + lastPurcPo + "]]>")
					.append("</last_purc_po>");

					valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
				}
				if (currentColumn.trim().equals("cctr_code__dr")) {
					costctr = checkNull(genericUtility.getColumnValue("cctr_code__dr", dom));
					analCode = checkNull(genericUtility.getColumnValue("anal_code", dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));
					costctrAsloccode = checkNull(distComm.getDisparams("999999", "CCENTER_AS_LOCATION", conn));

					if (!"NULLFOUND".equalsIgnoreCase(costctrAsloccode) && "Y".equalsIgnoreCase(costctrAsloccode)) {
						mcode = genericUtility.getColumnValue("item_code", dom);
						sql = " select case when qc_reqd is null then 'N' else qc_reqd end from item where item_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mcode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							qcReqd = rs.getString(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if ("Y".equalsIgnoreCase(qcReqd)) {
							CctrLoccode = costctr.trim() + "Q";
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<loc_code>")
							 * .append(CctrLoccode).append("</loc_code>");
							 */
							valueXmlString.append("<loc_code>").append("<![CDATA[" + CctrLoccode + "]]>")
							.append("</loc_code>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						} else {
							CctrLoccode = costctr.trim();
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<loc_code>")
							 * .append(CctrLoccode).append("</loc_code>");
							 */
							valueXmlString.append("<loc_code>").append("<![CDATA[" + CctrLoccode + "]]>")
							.append("</loc_code>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						}
						valueXmlString.append("<loc_code protect='1'>").append("").append("</loc_code>");

						// invAcctPorcp =
						// trim(gf_getfinparm('999999','INV_ACCT_PORCP'))
						invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

						if (invAcctPorcp == null || invAcctPorcp.equalsIgnoreCase("NULLFOUND")
								|| invAcctPorcp.trim().length() == 0) {
							invAcctPorcp = "N";
						}
						// 14-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item
						if ("S".equals(invAcctPorcp)) {
							sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code ? ) ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, mcode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								invAcctSer = checkNull(rs.getString("inv_acct"));
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (invAcctSer == null || invAcctSer.trim().length() == 0) {
								invAcctSer = "N";
							}
							invAcctPorcp = invAcctSer;
						}
						// end 14-nov-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
						// inv_acct of the itemser of the item

						invAcctQcorder = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);
						if (invAcctQcorder == null || invAcctQcorder.equalsIgnoreCase("NULLFOUND")
								|| invAcctQcorder.trim().length() == 0) {
							invAcctQcorder = "N";
						}

						if ("Y".equalsIgnoreCase(invAcctPorcp) && "Y".equalsIgnoreCase(invAcctQcorder)) {

						} else {
							costctr = "";
						}

						if (currentColumn.trim().equals("cctr_code__dr")) {
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<cctr_code__prov_dr>") .append(costctr)
							 * .append("</cctr_code__prov_dr>");
							 */
							valueXmlString.append("<cctr_code__prov_dr>").append("<![CDATA[" + costctr + "]]>")
							.append("</cctr_code__prov_dr>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						} else {
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<cctr_code__prov_cr>") .append(costctr)
							 * .append("</cctr_code__prov_cr>");
							 */
							valueXmlString.append("<cctr_code__prov_cr>").append("<![CDATA[" + costctr + "]]>")
							.append("</cctr_code__prov_cr>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						}

						/*
						 * sql =
						 * " select fn_get_budget_amt('P-ORD',:siteCode,:acctDr,:cctrDr,:analCode,:deptCode,'A'),"
						 * +
						 * " fn_get_cons_amt('P-ORD',:siteCode,:acctDr,:cctrDr,:analCode,:deptCode,'A') "
						 * + " from dual";
						 */
						sql = " select fn_get_budget_amt('P-ORD',?,?,?,?,?,'A'), "
								+ " fn_get_cons_amt(('P-ORD',?,?,?,?,?,'A') " + " from dual";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, siteCode);
						pStmt.setString(2, acctCode);
						pStmt.setString(3, cctr_code);
						pStmt.setString(4, analCode);
						pStmt.setString(5, deptCode);
						pStmt.setString(6, siteCode);
						pStmt.setString(7, acctCode);
						pStmt.setString(8, cctr_code);
						pStmt.setString(9, analCode);
						pStmt.setString(10, deptCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							budgetAmtanal = rs.getString(1);
							consumedAmtanal = rs.getString(2);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<budget_amt_anal>") .append(df.format(budgetAmtanal))
						 * .append("</budget_amt_anal>"); valueXmlString.append("<consumed_amt_anal>")
						 * .append(df.format(consumedAmtanal)) .append("</consumed_amt_anal>");
						 */
						valueXmlString.append("<budget_amt_anal>")
						.append("<![CDATA[" + df.format(budgetAmtanal) + "]]>").append("</budget_amt_anal>");
						valueXmlString.append("<consumed_amt_anal>")
						.append("<![CDATA[" + df.format(consumedAmtanal) + "]]>")
						.append("</consumed_amt_anal>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

						budgetAmtanal1 = Double.parseDouble(budgetAmtanal);
						consumedAmtanal1 = Double.parseDouble(consumedAmtanal);

						budgetAmt = budgetAmtanal1 - consumedAmtanal1;
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<budget_amt>") .append(df.format(budgetAmt))
						 * .append("</budget_amt>");
						 */
						valueXmlString.append("<budget_amt>").append("<![CDATA[" + df.format(budgetAmt) + "]]>")
						.append("</budget_amt>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					}

				}

				if (currentColumn.trim().equals("emp_code__qcaprv")) {
					String empFname = "", empMname = "", empLname = "";
					empCodeqcaprv = checkNull(genericUtility.getColumnValue("emp_code__qcaprv", dom));
					sql = " select emp_fname,emp_mname,emp_lname from employee where emp_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, empCodeqcaprv);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						empFname = checkNull(rs.getString(1));
						empMname = checkNull(rs.getString(2));
						empLname = checkNull(rs.getString(3));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<emp_fname>").append("<![CDATA[" + empFname + "]]>").append("</emp_fname>");
					valueXmlString.append("<emp_mname>").append("<![CDATA[" + empMname + "]]>").append("</emp_mname>");
					valueXmlString.append("<emp_lname>").append("<![CDATA[" + empLname + "]]>").append("</emp_lname>");
				}

				if (currentColumn.trim().equals("quantity")) {
					qty = checkNull(genericUtility.getColumnValue("quantity", dom));
					unit = checkNull(genericUtility.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					lcConv = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
					stdRate = checkNull(genericUtility.getColumnValue("std_rate", dom));
					lcConv = lcConv == "" ? "0" : lcConv;
					convTemp = Double.parseDouble(lcConv);
					System.out.println(" in quantity qty>>"+qty+"\nunit"+unit+"\nmVal1"+mVal1+"\nitemCode"+itemCode+"\nlcConv"+lcConv+"\nstdRate"+stdRate+"\nconvTemp"+convTemp);

					if (qty != null && qty.trim().length() > 0) {
						qtyLc = Double.parseDouble(qty);
					}
					if (mVal1.trim().length() == 0) {
						sql = " select unit	from item where item_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mVal1 = rs.getString("unit");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if ("O".equalsIgnoreCase(Value) || "B".equalsIgnoreCase(Value)) {
							// lc_qtystduom = gf_conv_qty_fact1(unit, mVal1,
							// itemCode, qty, lcConv,'Y') ;
							qtystduomArr = distComm.convQtyFactor(unit, mVal1, itemCode, qtyLc,
									Double.parseDouble(lcConv), conn);
						} else {
							// lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1,
							// itemCode, lc_qty, lc_conv,'N')
							qtystduomArr = distComm.convQtyFactor(unit, mVal1, itemCode, qtyLc,
									Double.parseDouble(lcConv), conn);
						}
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<unit__std>") .append(df.format(mVal1))
						 * .append("</unit__std>");
						 */
						valueXmlString.append("<unit__std>").append("<![CDATA[" + df.format(mVal1) + "]]>")
						.append("</unit__std>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					} else {
						if ("O".equalsIgnoreCase(Value) || "B".equalsIgnoreCase(Value)) {
							// lc_qtystduom = gf_conv_qty_fact1(unit, mVal1,
							// itemCode, qty, lcConv,'Y') ;
							qtystduomArr = distComm.convQtyFactor(unit, mVal1, itemCode, Double.parseDouble(qty),
									Double.parseDouble(lcConv), conn);
						} else {
							// lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1,
							// itemCode, lc_qty, lc_conv,'N')
							qtystduomArr = distComm.convQtyFactor(unit, mVal1, itemCode, Double.parseDouble(qty),
									Double.parseDouble(lcConv), conn);
						}
					}

					if (convTemp == 0) {
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<conv__qty_stduom>")
						 * .append(lcConv).append("</conv__qty_stduom>");
						 */
						System.out.println("convQtystd 9827>> " + lcConv);  //added by manish mhatre on 26-03-21
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + lcConv + "]]>")
						.append("</conv__qty_stduom>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<quantity__stduom>") .append(qtystduomArr.get(1))
					 * .append("</quantity__stduom>");
					 */

					//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtystduomArr.get(1) + "]]>").append("</quantity__stduom>");
					System.out.println("quantity stduom 9849>> " +qtystduomArr.get(1));  //added by manish mhatre on 26-03-21
					System.out.println("quantity stduom 9850>> " +utilMethods.getReqDecString(Double.parseDouble(qtystduomArr.get(1).toString()),3));  //added by manish mhatre on 26-03-21
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(Double.parseDouble(qtystduomArr.get(1).toString()),3) + "]]>").append("</quantity__stduom>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

					// gbf_isreasonrequired()

					// Added by Anjali R. on[13/03/2018][To pass query_stdoum value for current line
					// number][Start]
					queryStdoum = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

					// Isreasonrequired(dom, currentFormNo, indNo, lineNo, conn);
					Isreasonrequired(dom, currentFormNo, indNo, lineNo, queryStdoum, conn);
					// Added by Anjali R. on[13/03/2018][To passquery_stdoum value for current line
					// number][End]

					priceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
					ordDate = checkNull(genericUtility.getColumnValue("ord_date", dom1));
					rate = checkNull(genericUtility.getColumnValue("rate", dom));

					rate = rate == "" ? "0" : rate;
					System.out.println("rate is....[" + rate + "]");
					System.out
					.println("PriceList is...[" + priceList + "]ordDate[" + ordDate + "]qtyLc[" + qtyLc + "]");
					if (rate == null || Double.parseDouble(rate) <= 0) {
						System.out.println("inside rate is null or leass than eq 0");
						if (priceList != null && priceList.trim().length() > 0) {
							Ratelc = distComm.pickRate(priceList, ordDate, itemCode, "", "L", qtyLc, unit, conn);
							System.out.println("@@@@@@@@@@@8286 Ratelc[" + Ratelc + "] ");
							if (Ratelc == 0) {
								Ratelc = -1;
							}

							sql = " select count(*) as cnt from pricelist where  price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B')";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, priceList);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							System.out.println("@@@@@@@@@@@8305 Ratelc[" + Ratelc + " rate is set ] ");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<rate>").append(Ratelc) .append("</rate>");
							 * valueXmlString.append("<rate__clg>").append(Ratelc) .append("</rate__clg>");
							 * valueXmlString.append("<unit__rate>").append(unit) .append("</unit__rate>");
							 */
							valueXmlString.append("<rate>").append("<![CDATA[" + Ratelc + "]]>").append("</rate>");
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + Ratelc + "]]>")
							.append("</rate__clg>");
							valueXmlString.append("<unit__rate>").append("<![CDATA[" + unit + "]]>")
							.append("</unit__rate>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
							mval1 = genericUtility.getColumnValue("unit__std", dom);
							lcConv = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
							lcConv = lcConv == "" ? "0" : lcConv;
							convTemp = Double.parseDouble(lcConv);

							if ("R".equalsIgnoreCase(Value) || "B".equalsIgnoreCase(Value)) {
								// lc_ratestduom = gf_conv_qty_fact1(mVal1,
								// mVal, itemCode, lc_rate, lc_conv,'Y')
								ratestduomArr = distComm.convQtyFactor(mVal1, unit, itemCode, Ratelc,
										Double.parseDouble(lcConv), conn);
							} else {
								// lc_ratestduom = gf_conv_qty_fact1(mVal1,
								// mVal, itemCode, lc_rate, lc_conv,'N')
								ratestduomArr = distComm.convQtyFactor(mVal1, unit, itemCode, Ratelc,
										Double.parseDouble(lcConv), conn);
							}

							if (convTemp == 0) {
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
								/*
								 * valueXmlString.append("<conv__rtuom_stduom>") .append(lcConv)
								 * .append("</conv__rtuom_stduom>");
								 */
								valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lcConv + "]]>")
								.append("</conv__rtuom_stduom>");
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
							}
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
							/*
							 * valueXmlString.append("<rate__stduom>") .append(ratestduomArr.get(0))
							 * .append("</rate__stduom>");
							 */
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + ratestduomArr.get(0) + "]]>")
							.append("</rate__stduom>");
							// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
						}
					}
					// Pavan R on 28aug18 [to set Tot_amt based on qty and rate][start]
					System.out.println("On quantity.......values from dom ");
					rateStduomStr = genericUtility.getColumnValue("rate__stduom", dom).trim();
					//Changed by Varsha V on 08-06-2020
					/*discStr = genericUtility.getColumnValue("discount", dom).trim();
					taxAmtStr = genericUtility.getColumnValue("tax_amt", dom).trim();*/
					discStr = checkInt(genericUtility.getColumnValue("discount", dom)).trim();
					taxAmtStr = checkInt(genericUtility.getColumnValue("tax_amt", dom)).trim();
					System.out.println("...quantity__stduom[" + quantityStduomStr + "]rate__stduom[" + rateStduomStr
							+ "]discount[" + discStr + "]tax_amt[" + taxAmtStr + "]");
					// rate__stduom
					if (rateStduomStr != null && rateStduomStr.trim().length() > 0) {
						rateStduom = Double.parseDouble(rateStduomStr);
					} else {
						rateStduom = 0;
					}
					if (discStr != null && discStr.trim().length() > 0) {
						disc = Double.parseDouble(discStr);
					} else {
						disc = 0;
					}
					if (taxAmtStr != null && taxAmtStr.trim().length() > 0) {
						taxAmt = Double.parseDouble(taxAmtStr);
					} else {
						taxAmt = 0;
					}
					System.out.println("quantity__stduom:[" + Double.parseDouble(qtystduomArr.get(1).toString()) + "]");
					totAmount = (Double.parseDouble(qtystduomArr.get(1).toString()) * rateStduom)
							- ((Double.parseDouble(qtystduomArr.get(1).toString()) * rateStduom * disc) / 100) + taxAmt;
					System.out.println("totAmount[" + totAmount + "]");
					valueXmlString.append("<tot_amt>").append(totAmount).append("</tot_amt>");
					setNodeValue(dom1, "tot_amt", totAmount);
					// Pavan R on 28aug18 [to set Tot_amt based on qty and rate][end]
					// gbf_pricelistitemcheck(itemCode) ;

					pricelistitemcheck(itemCode, editFlag, dom, conn);
					// gbf_itemchangeddet("adj_variance")

					// commented
					// valueXmlString.append("<adj_variance>").append("").append("</adj_variance>");
					// setNodeValue( dom, "adj_variance","" );
					reStr = itemChanged(dom, dom1, dom2, objContext, "adj_variance", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					actualCost = genericUtility.getColumnValue("actual_cost", dom) == null ? "0"
							: genericUtility.getColumnValue("actual_cost", dom);

					stdCode = Double.parseDouble(qty == "" ? "0" : qty)
							* Double.parseDouble(stdRate == "" ? "0" : stdRate);

					varie = (stdCode - Double.parseDouble(actualCost == "" ? "0" : actualCost));
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
					/*
					 * valueXmlString.append("<std_cost>").append(stdCode) .append("</std_cost>");
					 * valueXmlString.append("<varience>").append(varie) .append("</varience>");
					 */
					valueXmlString.append("<std_cost>").append("<![CDATA[" + stdCode + "]]>").append("</std_cost>");
					valueXmlString.append("<varience>").append("<![CDATA[" + varie + "]]>").append("</varience>");
					// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]

				}
				if (currentColumn.trim().equals("adj_variance")) {
					varValue = distComm.getDisparams("999999", "PO_ADJ_VARIANCE", conn);
					// gf_getenv_dis('999999', 'PO_ADJ_VARIANCE')
					if ("Y".equalsIgnoreCase(varValue)) {
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						qtystd = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));
						ratestd = checkNull(genericUtility.getColumnValue("rate__stduom", dom));
						System.out.println(" in adj variance unit>>"+unit+"\nunitrate"+unitRate+"\nunitStd"+unitStd+"\nqty"+qty+"rate"+rate+"\nqtyStd"+qtystd+"\nratestd"+ratestd);

						Value = distComm.getDisparams("999999", "RCP_UOM_VARIANCE", conn);
						// ls_value = gf_getenv_dis('999999',
						// 'RCP_UOM_VARIANCE')

						if (qty != null && qty.trim().length() > 0) {
							qtyLc = Double.parseDouble(qty);
						}
						if (rate != null && rate.trim().length() > 0) {
							rateLc = Double.parseDouble(rate);
						}
						if (qtystd != null && qtystd.trim().length() > 0) {
							qtystdLc = Double.parseDouble(qtystd);
						}
						if (ratestd != null && ratestd.trim().length() > 0) {
							ratestdLc = Double.parseDouble(ratestd);
						}
						if (Value != null && Value.trim().length() > 0) {
							// Added and changes by sarita on 08FEB2018
							// ValueLc = Double.parseDouble(ratestd) ;
							ValueLc = Double.parseDouble(Value);
						}

						if (qtyLc > 0 && rateLc > 0) {
							amt = qtyLc * rateLc;
							amtStd = qtystdLc * ratestdLc;

							if ((amt - amtStd) > ValueLc && !unit.trim().equalsIgnoreCase(unitRate.trim())) {
								qtystdlc = (amt / ratestdLc);
								lcConvlc = qtystdLc / qtyLc;
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
								/*
								 * valueXmlString.append("<conv__qty_stduom>") .append(df.format(lcConvlc))
								 * .append("</conv__qty_stduom>"); valueXmlString.append("<quantity__stduom>")
								 * .append(df.format(qtystdlc)) .append("</quantity__stduom>");
								 */
								System.out.println("convQtystd 10045>> " + lcConvlc);  //added by manish mhatre on 26-03-21
								valueXmlString.append("<conv__qty_stduom>")
								.append("<![CDATA[" + df.format(lcConvlc) + "]]>")
								.append("</conv__qty_stduom>");

								//valueXmlString.append("<quantity__stduom>")		.append("<![CDATA[" + df.format(qtystdlc) + "]]>")			.append("</quantity__stduom>");

								System.out.println("quantity stduom 10064>> " +qtystdlc);  //added by manish mhatre on 26-03-21
								System.out.println("quantity stduom 10065>> " +utilMethods.getReqDecString((qtystdlc),3));  //added by manish mhatre on 26-03-21
								valueXmlString.append("<quantity__stduom>")		.append("<![CDATA[" + df.format(utilMethods.getReqDecString((qtystdlc),3) + "]]>"))			.append("</quantity__stduom>");
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
							}

							amtStd = qtystdLc * ratestdLc;
							if ((amt - amtStd) > ValueLc && !unit.trim().equalsIgnoreCase(unitRate.trim())) {
								ratestdlc = (amt / qtystdLc);
								lcConvlc = ratestdLc / rateLc;
								// Commented and Added by sarita on 01 OCT 2018 to change value of
								// conv__rtuom_stduom & rate__stduom [START]
								/*
								 * valueXmlString.append("<conv__rtuom_stduom>") .append(df.format(lcConv))
								 * .append("</conv__rtuom_stduom>"); valueXmlString.append("<rate__stduom>")
								 * .append(df.format(ratestd)) .append("</rate__stduom>");
								 */
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
								/*
								 * valueXmlString.append("<conv__rtuom_stduom>") .append(df.format(lcConvlc))
								 * .append("</conv__rtuom_stduom>"); valueXmlString.append("<rate__stduom>")
								 * .append(df.format(ratestdlc)) .append("</rate__stduom>");
								 */
								valueXmlString.append("<conv__rtuom_stduom>")
								.append("<![CDATA[" + df.format(lcConvlc) + "]]>")
								.append("</conv__rtuom_stduom>");
								valueXmlString.append("<rate__stduom>")
								.append("<![CDATA[" + df.format(ratestdlc) + "]]>").append("</rate__stduom>");
								// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
								// Commented and Added by sarita on 01 OCT 2018 to change value of
								// conv__rtuom_stduom & rate__stduom [END]
							}
						}
					}

				}
				if (currentColumn.trim().equals("unit")) {

					System.out.println("inside unit block");
					mcode = checkNull(genericUtility.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					qty = checkNull(genericUtility.getColumnValue("quantity", dom));
					System.out.println("inside unit>"+mcode+"\nunitstd"+mVal1+"\nitemCode"+itemCode+"\nqty"+qty);

					if (qty != null && qty.trim().length() > 0) {
						qtyLc = Double.parseDouble(qty);
					}

					// convQtystdDob = Double.parseDouble(lcConv);
					if (mval1 == null || mval1.trim().length() == 0) {
						sql = "select unit from item where item_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mVal1 = rs.getString("unit");

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<unit__std>").append(mVal1) .append("</unit__std>");
						 */
						valueXmlString.append("<unit__std>").append("<![CDATA[" + mVal1 + "]]>").append("</unit__std>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}

					convQtystdDob = 0;
					// 20-oct-2020 manoharan check both unit and unit__std are same
					if (mcode != null && mVal1 != null && !mcode.trim().equalsIgnoreCase(mVal1.trim())) {
						if ("Q".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
							// lc_qtystduom = gf_conv_qty_fact1(mcode, mVal1,
							// itemCode, lc_qty, lc_conv,'Y')
							qtystduomArr = distComm.convQtyFactor(mcode, mVal1, itemCode, qtyLc, convQtystdDob, conn);
						} else {
							// lc_qtystduom = gf_conv_qty_fact1(mcode, mVal1,
							// itemCode, lc_qty, lc_conv,'N')
							qtystduomArr = distComm.convQtyFactor(mcode, mVal1, itemCode, qtyLc, convQtystdDob, conn);
						}
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [START]
						/*
						 * valueXmlString.append("<conv__qty_stduom>").append(lcConv)
						 * .append("</conv__qty_stduom>"); valueXmlString.append("<quantity__stduom>")
						 * .append(qtystduomArr.get(1)) .append("</quantity__stduom>");
						 */
						System.out.println("convQtystd 10136>> " + lcConv);  //added by manish mhatre on 26-03-21
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + lcConv + "]]>")
						.append("</conv__qty_stduom>");
						double qtystduomArrVal =0.0;
						qtystduomArrVal = Double.parseDouble(qtystduomArr.get(1).toString());

						//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" +qtystduomArr.get(1) + "]]>").append("</quantity__stduom>");
						System.out.println("quantity stduom 10157>> " +qtystduomArrVal);  //added by manish mhatre on 26-03-21
						System.out.println("quantity stduom 10158>> " +utilMethods.getReqDecString(qtystduomArrVal,3));  //added by manish mhatre on 26-03-21
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(qtystduomArrVal,3) + "]]>").append("</quantity__stduom>");
						// Commented and Added by sarita to add CDATA on 01 FEBRUARY 2019 [END]
					}
					else //// 20-oct-2020 manoharan else condition added check both unit and unit__std are same
					{
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[1]]>").append("</conv__qty_stduom>");

						//valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qty + "]]>").append("</quantity__stduom>");
						System.out.println("quantity stduom 10167>> " +qty);  //added by manish mhatre on 26-03-21
						System.out.println("quantity stduom 10168>> " +utilMethods.getReqDecString(Double.parseDouble(qty),3));  //added by manish mhatre on 26-03-21
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + utilMethods.getReqDecString(Double.parseDouble(qty),3) + "]]>").append("</quantity__stduom>");
					}

				}
				if (currentColumn.trim().equals("conv__qty_stduom")) {
					convQtystduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
					mVal = checkNull(genericUtility.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					qty = checkNull(genericUtility.getColumnValue("quantity", dom));
					if (qty != null && qty.trim().length() > 0) {
						qtyLc = Double.parseDouble(qty);
					}
					stdRate = checkNull(genericUtility.getColumnValue("std_rate", dom));

					if (convQtystduom != null && convQtystduom.trim().length() > 0) {
						convQtystduomLc = Double.parseDouble(convQtystduom);
					}

					if (stdRate != null && stdRate.trim().length() > 0) {
						stdRateLc = Double.parseDouble(stdRate);
					}

					valueXmlString.append("<std_cost>").append(stdCost).append("</std_cost>");
					stdCost = qtyLc * stdRateLc;

					valueXmlString.append("<std_cost>").append(stdCost).append("</std_cost>");

					valueXmlString.append("<std_cost>").append(stdCost).append("</std_cost>");

					actualCost = genericUtility.getColumnValue("actual_cost", dom);

					if (actualCost == null || actualCost.trim().length() == 0) {
						actualCost = "0";
					}
					valueXmlString.append("<varience>")
					.append((stdCost - Double.parseDouble(actualCost == "" ? "0" : actualCost)))
					.append("</varience>");

					if (mval1 == null || mval1.trim().length() == 0) {
						sql = "select unit from item where item_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mVal1 = rs.getString("unit");

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<unit__std>").append(mVal1).append("</unit__std>");
					}

					if ("Q".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
						// lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1,
						// itemCode, lc_qty, lc_conv,'Y')
						qtystduomArr = distComm.convQtyFactor(mVal, mVal1, itemCode, qtyLc, convQtystduomLc, conn);
					} else {
						qtystduomArr = distComm.convQtyFactor(mVal, mVal1, itemCode, qtyLc, convQtystduomLc, conn);
					}
				}
				if (currentColumn.trim().equals("rate")) {
					double lcrtconv = 0, inputQty = 0;
					rate = checkNull(genericUtility.getColumnValue("rate", dom));
					mVal = checkNull(genericUtility.getColumnValue("unit__rate", dom));
					mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					lcConv = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
					lc_convtemp = Double.parseDouble(lcConv == "" ? "0" : lcConv);
					System.out.println(
							"@@@@@@@@@8511 mVal1[" + mVal1 + "]lc_convtemp[" + lc_convtemp + "]rate[" + rate + "]");
					if (rate != null && rate.trim().length() > 0) {
						rateLc = Double.parseDouble(rate);
					}
					if (mVal1 == null || mVal1.trim().length() == 0) {
						sql = "Select unit from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mVal1 = rs.getString("unit");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<unit__std>").append(mVal1).append("</unit__std>");
					}
					System.out.println("@@@@@@@@@8528 mVal[" + mVal + "]");

					if (mVal == null || mVal.trim().length() == 0) {
						sql = "Select unit from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mVal = rs.getString("unit");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						convQtystdDob = Double.parseDouble(lcConv);// conv__rtuom_stduom
						ratestduomArr = null;
						ratestduomArr = new ArrayList();
						ratestduomArr = distComm.getConvQuantityFact(mVal1, mVal, itemCode, rateLc, convQtystdDob,
								conn);
						if (ratestduomArr != null) {
							inputQty = Double.parseDouble(ratestduomArr.get(1).toString());
							lcrtconv = Double.parseDouble(ratestduomArr.get(0).toString());
							System.out.println("inputQty[" + inputQty + "]");
							System.out.println("lcrtconv[" + lcrtconv + "]");
						}
						valueXmlString.append("<unit__rate>").append(mVal).append("</unit__rate>");
					} else {
						ratestduomArr = null;
						ratestduomArr = new ArrayList();
						ratestduomArr = distComm.getConvQuantityFact(mVal1, mVal, itemCode, rateLc, convQtystdDob,
								conn);
						if (ratestduomArr != null) {
							inputQty = Double.parseDouble(ratestduomArr.get(1).toString());
							lcrtconv = Double.parseDouble(ratestduomArr.get(0).toString());
							System.out.println("inputQty[" + inputQty + "]");
							System.out.println("lcrtconv[" + lcrtconv + "]");
						}
					}

					System.out.println("@@@@@@@@@@@8573 inputQty[" + inputQty + "]lcrtconv[" + lcrtconv + "] ");
					if (lc_convtemp == 0) {
						valueXmlString.append("<conv__rtuom_stduom>").append(lcrtconv).append("</conv__rtuom_stduom>");
					}
					valueXmlString.append("<rate__stduom>").append(inputQty).append("</rate__stduom>");
					mrate = checkNull(genericUtility.getColumnValue("rate__clg", dom));
					// Pavan R on 28aug18 [to set Tot_amt based on qty and rate][start]
					System.out.println("PO On Rate.......values from dom ");
					quantityStduomStr = genericUtility.getColumnValue("quantity__stduom", dom).trim();
					//discStr = genericUtility.getColumnValue("discount", dom).trim();
					discStr = checkInt(genericUtility.getColumnValue("discount", dom)).trim();
					//taxAmtStr = genericUtility.getColumnValue("tax_amt", dom).trim();
					taxAmtStr = checkInt(genericUtility.getColumnValue("tax_amt", dom)).trim();
					System.out.println("...quantity__stduom[" + quantityStduomStr + "]rate__stduom[" + rateStduomStr
							+ "]discount[" + discStr + "]tax_amt[" + taxAmtStr + "]");
					if (quantityStduomStr != null && quantityStduomStr.trim().length() > 0) {
						quantityStduom = Double.parseDouble(quantityStduomStr);
					} else {
						quantityStduom = 0;
					}
					// discount
					if (discStr != null && discStr.trim().length() > 0) {
						disc = Double.parseDouble(discStr);
					} else {
						disc = 0;
					}
					// tax_amt
					if (taxAmtStr != null && taxAmtStr.trim().length() > 0) {
						taxAmt = Double.parseDouble(taxAmtStr);
					} else {
						taxAmt = 0;
					}
					totAmount = (quantityStduom * inputQty) - ((quantityStduom * inputQty * disc) / 100) + taxAmt;
					System.out.println("totAmount[" + totAmount + "]");
					valueXmlString.append("<tot_amt>").append(totAmount).append("</tot_amt>");
					setNodeValue(dom1, "tot_amt", totAmount);
					// Pavan R on 28aug18 [to set Tot_amt based on qty and rate][end]
					System.out.println("@@@@@@8583 rate[" + rate + "]mrate[" + mrate + "]");
					if (mrate != null && mrate.trim().length() > 0) {
						mrateLc = Double.parseDouble(mrate);
					}
					if (mrate == null || mrate.trim().length() == 0 || mrateLc <= 0) {
						valueXmlString.append("<rate__clg>").append(rate).append("</rate__clg>");
					} else {
						ls_price_list__clg = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));

						System.out
						.println("@@@@@@8593 ls_price_list__clg[" + ls_price_list__clg + "]rate[" + rate + "]");
						if (ls_price_list__clg == null || ls_price_list__clg.trim().length() == 0) {

							valueXmlString.append("<rate__clg>").append(rate).append("</rate__clg>");
						}

					} // end if

					// gbf_itemchangeddet("adj_variance")
					reStr = itemChanged(dom, dom1, dom2, objContext, "adj_variance", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

				}
				if (currentColumn.trim().equals("unit__rate")) {
					mcode = checkNull(genericUtility.getColumnValue("unit__rate", dom));
					mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					rate = checkNull(genericUtility.getColumnValue("rate", dom));
					lcConv = "0";

					if (mVal1 == null || mVal1.trim().length() == 0) {
						sql = "Select unit from item where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							mVal1 = rs.getString("unit");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<unit__std>").append(mVal1).append("</unit__std>");
					}

					if ("R".equalsIgnoreCase(lsValue) || "B".equalsIgnoreCase(lsValue)) {
						// ratestduom = gf_conv_qty_fact1(mVal1, mcode,
						// itemCode, lc_rate, lc_conv,'Y')
						ratestduomArr = distComm.convQtyFactor(mVal1, mcode, itemCode,
								Double.parseDouble(rate == "" ? "0" : rate), convQtystdDob, conn);
					} else {
						// ratestduom = gf_conv_qty_fact1(mVal1, mcode,
						// itemCode, lc_rate, lc_conv,'N')
						ratestduomArr = distComm.convQtyFactor(mVal1, mcode, itemCode,
								Double.parseDouble(rate == "" ? "0" : rate), convQtystdDob, conn);
					}
					System.out.println("@@@@@@@8647 ratestduomArr[" + ratestduomArr + "]");
					valueXmlString.append("<conv__rtuom_stduom>").append(lcConv).append("</conv__rtuom_stduom>");
					// valueXmlString.append("<rate__stduom>").append(ratestduom).append("</rate__stduom>");
					valueXmlString.append("<rate__stduom>").append(ratestduomArr.get(0)).append("</rate__stduom>");

				}
				if (currentColumn.trim().equals("anal_code")) {
					mcode = genericUtility.getColumnValue("anal_code", dom);

					sql = " Select descr from analysis where anal_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mcode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						mdescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					valueXmlString.append("<analysis_descr>").append(mdescr).append("</analysis_descr>");
				}
				if (currentColumn.trim().equals("quantity__fc")) {
					portType = checkNull(genericUtility.getColumnValue("pord_type", dom));
					quantityFc = checkNull(genericUtility.getColumnValue("quantity__fc", dom));

					if ("WO".equalsIgnoreCase(portType)) {
						indNo = genericUtility.getColumnValue("ind_no", dom).trim();
						if (indNo == null || indNo.trim().length() == 0) {
							indNo = genericUtility.getColumnValue("ind_no", dom).trim();
						}

						sql = " Select quantity from indent where ind_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, indNo);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							qty = rs.getString("quantity");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (qty == null) {
							qty = "0";
						}
						qty1 = Integer.parseInt(qty);
						quantityFc1 = Integer.parseInt(quantityFc);
						totQtyperc = (qty1 * quantityFc1) / 100;
						temp = totQtyperc + totQtyperc;

						/*
						 * sql = "select :ls_temp from dual"; pStmt= conn.prepareStatement(sql); rs =
						 * pStmt.executeQuery(); if(rs.next()) { } rs.close(); rs = null; pStmt.close();
						 * pStmt = null;
						 */
						System.out.println("@@@@9117 totQtyperc[" + totQtyperc + "]");
						valueXmlString.append("<quantity>").append(totQtyperc).append("</quantity>");

						setNodeValue(dom, "quantity", (totQtyperc));
						reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						// gbf_itemchangeddet("quantity") ;
					}
				}


				//added by manish mhatre on 17-03-2021
				//start manish
				if(currentColumn.trim().equals( "no_art" ) || currentColumn.trim().equals( "dimension" ) )
				{
					System.out.println("Inside porder no_art block");
					String dimension="",noArtStr="";
					double quantity=0,noArt=0;

					itemCode= genericUtility.getColumnValue("item_code", dom);
					dimension=genericUtility.getColumnValue("dimension", dom);
					noArtStr= genericUtility.getColumnValue("no_art", dom);

					System.out.println("item code>>"+itemCode+"\ndimension>>"+dimension+"\nno of articles>>"+noArtStr);

					if(dimension!=null && dimension.trim().length()>0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if(rs.next())
						{
							unit = rs.getString("UNIT");
						}
						if(pStmt != null)
						{
							pStmt.close();
							pStmt = null;
						}
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						System.out.println("unit>>"+unit);

						if(noArtStr!=null && noArtStr.trim().length()>0)
						{
							noArt=Double.parseDouble(noArtStr);
						}
						else
						{
							noArt=1;
						}
						System.out.println("dimension>>"+dimension+"\n no of articles>>"+noArt);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							System.out.println("Inside if dimension not null in no art");
							quantity=distComm.getQuantity(dimension,noArt,unit,conn);
							System.out.println("quantity in dimension block>>"+quantity);
							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(dom, "quantity", quantity);

							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("after quantity itemchanged 10416.......");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
					}
				}
				//end manish


				valueXmlString.append("</Detail2>");
				break;
			case 3:
				valueXmlString.append("<Detail3>");
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equals("itm_default")) {
					System.out.println("Inside itm_default case 3...");
					// lineNo = long(gbf_get_argval(is_extra_arg, "line_no"));
					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					if (lineNo != null && lineNo.trim().length() > 0) {
						valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
					}
					valueXmlString.append("<purc_order>").append(genericUtility.getColumnValue("purc_order", dom))
					.append("</purc_order>");

				}
				if (currentColumn.trim().equals("term_code")) {
					mval = checkNull(genericUtility.getColumnValue("term_code", dom));

					sql = "select descr,input_nos from pur_term where term_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mval);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						mval1 = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					valueXmlString.append("<descr>").append(mval1).append("</descr>");

				}
				valueXmlString.append("</Detail3>");
				break;
			case 4:
				valueXmlString.append("<Detail4>");
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equals("itm_default")) {
					System.out.println("Inside itm_default case 4...");
					// lineNo = long(gbf_get_argval(is_extra_arg, "line_no"));
					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					if (lineNo != null && lineNo.trim().length() > 0) {
						valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
					}
					valueXmlString.append("<purc_order>").append(genericUtility.getColumnValue("purc_order", dom))
					.append("</purc_order>");

				}
				if (currentColumn.trim().equals("acct_code")) {
					acctCode = checkNull(genericUtility.getColumnValue("acct_code", dom));

					sql = "select descr from Accounts where acct_code = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, acctCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					valueXmlString.append("<accounts_descr>").append(descr).append("</accounts_descr>");

				}
				if (currentColumn.trim().equals("rel_agnst")) {
					Value = checkNull(genericUtility.getColumnValue("rel_agnst", dom));
				}
				if (currentColumn.trim().equals("itm_defaultedit")) {
					Value = checkNull(genericUtility.getColumnValue("rel_agnst", dom));
				}
				if (currentColumn.trim().equals("task_code")) {
					Value = checkNull(genericUtility.getColumnValue("rel_agnst", dom));
					taskCode = checkNull(genericUtility.getColumnValue("task_code", dom));
					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
					projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));

					if ("05".equalsIgnoreCase(Value)) {
						sql = "select FN_GET_TASK_DESC(?,?,?) from dual";
						pStmt = conn.prepareStatement(sql);

						pStmt.setString(2, purcOrder);
						pStmt.setString(3, projCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							taskCodeDescr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					} else {
						sql = "select FN_GET_GEN_DESCR ('TASK_CODE','W_PORDER' ,?,'D') from dual";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, taskCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							taskCodeDescr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}
					valueXmlString.append("<task_code_descr>").append(taskCodeDescr).append("</task_code_descr>");
				}
				valueXmlString.append("</Detail4>");
				break;
			case 5:

				valueXmlString.append("<Detail5>");
				parentNodeList = dom.getElementsByTagName("Detail5");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equals("itm_default")) {
					valueXmlString.append("<purc_order>").append(genericUtility.getColumnValue("purc_order", dom))
					.append("</purc_order>");
					// lineNo = long(gbf_get_argval(is_extra_arg, "line_no"));
					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					if (lineNo != null && lineNo.trim().length() > 0) {
						valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
					}
				}
				valueXmlString.append("</Detail5>");
				break;

			}
			valueXmlString.append("</Root>");

		} catch (Exception e) {
			System.out.println("POrderIC Exception ::" + e.getMessage());
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
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}
		}
		System.out.println("valueXmlString @@@@@@@@@ [" + valueXmlString + "]");
		return valueXmlString.toString();
	}

	//Modified by Anjali R. on[13/02/2018][Added new parameter as query_stdoum value for current line number][Start]
	/*private boolean Isreasonrequired(Document dom, int currentFormNo,
			String indNo, String lineNo, Connection conn) {*/
	private boolean Isreasonrequired(Document dom, int currentFormNo, String indNo, String lineNo, String qtyStduom,
			Connection conn) {
		// Modified by Anjali R. on[13/02/2018][Added new parameter as query_stdoum
		// value for current line number][End]
		String purcOrder, qtystdTemp = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		int childNodeListLength = 0;
		String indNoTemp = "";
		String lineNoTemp = "";
		double indQty = 0;
		double orderedQty = 0;
		double pendordqty = 0;
		double curordqty = 0;
		boolean enable = false;
		try {
			purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));

			// Modified by Anjali R. on[13/03/2018][use parameter values][Start]
			/*
			 * indNo = checkNull(genericUtility.getColumnValue("ind_no", dom)); lineNo =
			 * checkNull(genericUtility.getColumnValue("line_no", dom)); qtyStduom =
			 * checkNull(genericUtility.getColumnValue( "quantity__stduom", dom));
			 */
			// Modified by Anjali R. on[13/03/2018][use parameter values][End]

			if (qtyStduom == null) {
				qtyStduom = "0";
			}

			if (purcOrder == null) {
				purcOrder = "@@@";
			}
			curordqty = Double.parseDouble(qtyStduom == "" ? "0" : qtyStduom);
			if (indNo != null && indNo.trim().length() > 0) {
				parentNodeList = dom.getElementsByTagName("Detail" + currentFormNo);
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (int ctr1 = 0; ctr1 < childNodeListLength; ctr1++) {

					// Modified by Anjali R. on[16/03/2018][TO take current line number
					// values][Start]
					/*
					 * indNoTemp = checkNull( genericUtility.getColumnValue("ind_no", dom)) .trim();
					 * lineNoTemp = checkNull( genericUtility.getColumnValue("line_no", dom))
					 * .trim();
					 * 
					 * if (indNoTemp != null && indNoTemp.equalsIgnoreCase(indNo.trim()) && !
					 * lineNoTemp.equalsIgnoreCase(lineNo)) { qtystdTemp = checkNull(
					 * genericUtility.getColumnValue( "quantity__stduom", dom)).trim();
					 */
					qtystdTemp = "0";
					indNoTemp = checkNull(genericUtility.getColumnValueFromNode("ind_no", parentNode));
					System.out.println("current indent number --[" + indNoTemp + "]");

					lineNoTemp = checkNull(genericUtility.getColumnValueFromNode("line_no", parentNode));
					System.out.println("current line_no --[" + lineNoTemp + "]");

					if (indNoTemp != null && indNoTemp.trim().equalsIgnoreCase(indNo.trim())
							&& !lineNoTemp.trim().equalsIgnoreCase(lineNo.trim())) {
						qtystdTemp = checkNull(genericUtility.getColumnValueFromNode("quantity__stduom", parentNode));

						System.out.println("current qtystdTemp----[" + qtystdTemp + "]");
					}

					/*
					 * if (qtystdTemp == null) { qtystdTemp = "0"; } curordqty =
					 * Double.parseDouble(qtyStduom == "" ? "0" : qtyStduom) +
					 * Double.parseDouble(qtystdTemp == "" ? "0" : qtystdTemp);
					 */
					System.out.println("curordqty--[" + curordqty + "]qtystdTemp--[" + qtystdTemp + "]");
					curordqty = curordqty + Double.parseDouble(qtystdTemp == "" ? "0" : qtystdTemp);
					// Modified by Anjali R. on[16/03/2018][TO take current line number values][End]
				}

				sql = " select (case when quantity__stduom is null then 0 else quantity__stduom end),(case when ord_qty is null then 0 else ord_qty end)"
						+ " from indent where ind_no = ?";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, indNo);
				rs = pStmt.executeQuery();
				if (rs.next()) {
					indQty = rs.getDouble(1);
					orderedQty = rs.getDouble(2);
				}
				rs.close();
				rs = null;
				pStmt.close();
				pStmt = null;

				sql = "select case when sum(case when porddet.quantity__stduom is null then 0 else porddet.quantity__stduom end) is null then 0 else sum(case when porddet.quantity__stduom is null then 0 else porddet.quantity__stduom end)end"
						+ " from porddet, porder" + " where (porddet.purc_order = porder.purc_order ) and"
						+ " (((case when porder.status is null then 'O' else porder.status end) <> 'X' ) and"
						+ " ((case when porder.confirmed is null then 'N' else porder.confirmed end) <> 'Y' and (case when porder.status is null then 'O' else porder.status end) <> 'C') )  and "
						+ " (porddet.ind_no =  ? ) and " + " ((porddet.purc_order <> ? ) )";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, indNo);
				pStmt.setString(2, purcOrder);
				rs = pStmt.executeQuery();
				if (rs.next()) {
					pendordqty = rs.getDouble(1);
				}

				rs.close();
				rs = null;
				pStmt.close();
				pStmt = null;

				System.out.println("curordqty--[" + curordqty + "]orderedQty--[" + orderedQty + "]pendordqty--["
						+ pendordqty + "]indQty--[" + indQty + "]");
				if ((curordqty + orderedQty + pendordqty) > indQty) {
					enable = true;
				}
			}
		} catch (Exception e) {
			System.out.println("POrderIC Exception ::" + e.getMessage());
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return enable;

	}

	private void pricelistitemcheck(String itemCode, String editFlag, Document dom, Connection conn) {
		// TODO Auto-generated method stub
		String plist, priceListParent = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		StringBuffer valueXmlString = new StringBuffer();
		int cnt = 0;
		System.out.println("editFlag >>" + editFlag);
		editFlag = editFlag == null ? "" : editFlag;
		try {
			if (!editFlag.equalsIgnoreCase("V")) {
				plist = checkNull(genericUtility.getColumnValue("price_list", dom));
				if (!(plist == null || plist.trim().length() == 0)) {
					sql = "select	count(*) as cnt from pricelist where	price_list = ? and	item_code = ?	(list_type = 'F' or list_type = 'B')";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, plist);
					pStmt.setString(2, itemCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if (cnt > 0) {
						valueXmlString.append("<rate protect ='1'>").append("").append("</rate>");
						valueXmlString.append("<rate__clg protect ='1'>").append("").append("</rate__clg>");
						valueXmlString.append("<rate>").append(0).append("</rate>");
						valueXmlString.append("<rate__clg>").append(0).append("</rate__clg>");
						valueXmlString.append("<rate__stduom>").append(0).append("</rate__stduom>");

					} else {

						sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) from pricelist where price_list = ? and list_type = 'B'";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, plist);
						pStmt.setString(2, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (!(priceListParent == null) && priceListParent.trim().length() > 0) {
							sql = "select count(*) as cnt from pricelist where price_list = ? and	item_code = ? and	(list_type = 'F' or list_type = 'B')";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, priceListParent);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							if (cnt > 0) {
								valueXmlString.append("<rate protect ='1'>").append("").append("</rate>");
								valueXmlString.append("<rate__clg protect ='1'>").append("").append("</rate__clg>");
							} else {
								valueXmlString.append("<rate>").append(0).append("</rate>");
								valueXmlString.append("<rate__clg>").append(0).append("</rate__clg>");
								valueXmlString.append("<rate__stduom>").append(0).append("</rate__stduom>");
							}
						}
					}

				}
			} else {
				valueXmlString.append("<rate protect = '0'>").append("").append("</rate>");
			}
		} catch (Exception e) {
			System.out.println("POrderIC Exception ::" + e.getMessage());
			e.printStackTrace();
		}
	}

	private String errorType(Connection conn, String errorCode) {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
				e.printStackTrace();
			}
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
				e.printStackTrace();
			}
		}
		return msgType;
	}

	public String checkNull(String inputVal) {
		if (inputVal == null) {
			inputVal = "";
		}
		return inputVal;
	}

	private static String getAbsString(String str) {
		return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}

	private static void setNodeValue(Document dom, String nodeName, double nodeVal) throws Exception {
		setNodeValue(dom, nodeName, Double.toString(nodeVal));
	}

	private static void setNodeValue(Document dom, String nodeName, String nodeVal) throws Exception {
		Node tempNode = dom.getElementsByTagName(nodeName).item(0);

		if (tempNode != null) {
			if (tempNode.getFirstChild() == null) {
				CDATASection cDataSection = dom.createCDATASection(nodeVal);
				tempNode.appendChild(cDataSection);
			} else {
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}

	private static String getDescrSpeces(String itemCode, Connection conn) throws Exception {
		String specs = "";
		// Connection conn = null;

		// ConnDriver connDriver = new ConnDriver();
		// conn = connDriver.getConnectDB("DriverITM");
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		String itemType = "";
		String attrib1 = "";
		String attrib2 = "";
		String attrib3 = "";
		String attrib4 = "";
		String attrib5 = "";
		String attrib6 = "";
		String attrib7 = "";
		String attrib8 = "";
		String attrib9 = "";
		String attrib10 = "";
		String attrib11 = "";
		String attrib12 = "";
		String spec1 = "";
		String spec2 = "";
		String spec3 = "";
		String spec4 = "";
		String spec5 = "";
		String spec6 = "";
		String spec7 = "";
		String spec8 = "";
		String spec9 = "";
		String spec10 = "";
		String spec11 = "";
		String spec12 = "";

		sql = " select a.item_type,a.phy_attrib_1,a.phy_attrib_2,a.phy_attrib_3,a.phy_attrib_4,a.phy_attrib_5,a.phy_attrib_6,a.phy_attrib_7,a.phy_attrib_8, "
				+ " a.phy_attrib_9,a.phy_attrib_10,a.phy_attrib_11,a.phy_attrib_12,b.phy_attrib_1,b.phy_attrib_2,b.phy_attrib_3,b.phy_attrib_4, "
				+ " b.phy_attrib_5,b.phy_attrib_6,b.phy_attrib_7,b.phy_attrib_8,b.phy_attrib_9,b.phy_attrib_10,b.phy_attrib_11,b.phy_attrib_12 "
				+ " from item a, item_type b where a.item_type = b.item_type and a.item_code = ? ";

		pStmt = conn.prepareStatement(sql);
		pStmt.setString(1, itemCode);
		rs = pStmt.executeQuery();
		if (rs.next()) {
			itemType = rs.getString(1);
			spec1 = rs.getString(2);
			spec2 = rs.getString(3);
			spec3 = rs.getString(4);
			spec4 = rs.getString(5);
			spec5 = rs.getString(6);
			spec6 = rs.getString(7);
			spec7 = rs.getString(8);
			spec8 = rs.getString(9);
			spec9 = rs.getString(10);
			spec10 = rs.getString(11);
			spec11 = rs.getString(12);
			spec12 = rs.getString(13);
			attrib1 = rs.getString(14);
			attrib2 = rs.getString(15);
			attrib3 = rs.getString(16);
			attrib4 = rs.getString(17);
			attrib5 = rs.getString(18);
			attrib6 = rs.getString(19);
			attrib7 = rs.getString(20);
			attrib8 = rs.getString(21);
			attrib9 = rs.getString(22);
			attrib10 = rs.getString(23);
			attrib11 = rs.getString(24);
			attrib12 = rs.getString(25);

		}
		rs.close();
		rs = null;
		pStmt.close();
		pStmt = null;

		if (attrib1 != null && attrib1.trim().length() > 0 && spec1 != null && spec1.trim().length() > 0) {
			specs = specs + " " + attrib1.trim() + " : " + spec1.trim();
		}
		if (attrib2 != null && attrib2.trim().length() > 0 && spec2 != null && spec2.trim().length() > 0) {
			specs = specs + " " + attrib2.trim() + " : " + spec2.trim();
		}
		if (attrib3 != null && attrib3.trim().length() > 0 && spec3 != null && spec3.trim().length() > 0) {
			specs = specs + " " + attrib3.trim() + " : " + spec3.trim();
		}
		if (attrib4 != null && attrib4.trim().length() > 0 && spec4 != null && spec4.trim().length() > 0) {
			specs = specs + " " + attrib4.trim() + " : " + spec4.trim();
		}
		if (attrib5 != null && attrib5.trim().length() > 0 && spec5 != null && spec5.trim().length() > 0) {
			specs = specs + " " + attrib5.trim() + " : " + spec5.trim();
		}
		if (attrib6 != null && attrib6.trim().length() > 0 && spec6 != null && spec6.trim().length() > 0) {
			specs = specs + " " + attrib6.trim() + " : " + spec6.trim();
		}
		if (attrib7 != null && attrib7.trim().length() > 0 && spec7 != null && spec7.trim().length() > 0) {
			specs = specs + " " + attrib7.trim() + " : " + spec7.trim();
		}
		if (attrib8 != null && attrib8.trim().length() > 0 && spec8 != null && spec8.trim().length() > 0) {
			specs = specs + " " + attrib8.trim() + " : " + spec8.trim();
		}
		if (attrib9 != null && attrib9.trim().length() > 0 && spec9 != null && spec9.trim().length() > 0) {
			specs = specs + " " + attrib9.trim() + " : " + spec9.trim();
		}
		if (attrib10 != null && attrib10.trim().length() > 0 && spec10 != null && spec10.trim().length() > 0) {
			specs = specs + " " + attrib10.trim() + " : " + spec10.trim();
		}
		if (attrib11 != null && attrib11.trim().length() > 0 && spec11 != null && spec11.trim().length() > 0) {
			specs = specs + " " + attrib11.trim() + " : " + spec11.trim();
		}
		if (attrib12 != null && attrib12.trim().length() > 0 && spec12 != null && spec12.trim().length() > 0) {
			specs = specs + " " + attrib12.trim() + " : " + spec12.trim();
		}

		specs = specs.trim();

		return specs;

	}

	private String checkConvfact(String itemCode, String unitfrom, String unitto, Double convfact, Connection conn)
			throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int recCnt = 0;
		String errcode = "", variencetype = "", order = "NOTFOUND", sql = "";
		double varience = 0, mastfact = 0;

		System.out.println("@@@@@2 unitfrom[" + unitfrom + "]::unitto[" + unitto + "]::convfact[" + convfact + "]");
		if (unitfrom.equalsIgnoreCase(unitto) && (!(convfact == 1))) {
			errcode = "VTUCON1";
			return errcode;
		}

		sql = " select fact, varience_type, varience_value " + " from uomconv  where ( uomconv.unit__fr = ? ) or " // and
				+ " ( uomconv.unit__to = ? ) and ( uomconv.item_code = ? )   ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, unitfrom);
		pstmt.setString(2, unitto);
		pstmt.setString(3, itemCode);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			mastfact = rs.getDouble(1);
			variencetype = rs.getString(1);
			varience = rs.getDouble(1);
			recCnt++;
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if (recCnt == 0) {
			// 2. Check in the reverse order (TO - FROM) for the item
			sql = " select fact, varience_type, varience_value " // and
					+ " from uomconv  where ( uomconv.unit__fr = ? ) or ( uomconv.unit__to = ? ) "
					+ " and ( uomconv.item_code = ? )  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, unitto);
			pstmt.setString(2, unitfrom);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				mastfact = rs.getDouble(1);
				variencetype = rs.getString(1);
				varience = rs.getDouble(1);
				recCnt++;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (recCnt == 0) {
				sql = " select fact, varience_type, varience_value "
						+ " from uomconv  where ( uomconv.unit__fr = ? ) and ( uomconv.unit__to = ? ) "
						+ " and ( uomconv.item_code = 'X' )  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, unitfrom);
				pstmt.setString(2, unitto);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mastfact = rs.getDouble(1);
					variencetype = rs.getString(1);
					varience = rs.getDouble(1);
					recCnt++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (recCnt == 0) {
					// 4. Check in the reverse order (TO - FROM) for 'X' item
					sql = " select fact, varience_type, varience_value "
							+ " from uomconv  where ( uomconv.unit__fr = ? ) and ( uomconv.unit__to = ? ) "
							+ " and ( uomconv.item_code = 'X' ) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitto);
					pstmt.setString(2, unitfrom);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mastfact = rs.getDouble(1);
						variencetype = rs.getString(1);
						varience = rs.getDouble(1);
						recCnt++;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (recCnt == 0) {
						order = "NOTFOUND";
					} else // found reverse order for 'X' item
					{
						order = "REVORD";
					}
				} else {
					// found actual order for 'X' item
					order = "ACTORD";
				}
			} else { // found reverse order for item
				order = "REVORD";
			}
		} else { // found actual order for item
			order = "ACTORD";
		}

		// isnull(ld_mastfact) then ld_mastfact = 0

		if (variencetype == null || variencetype.length() == 0) {
			variencetype = "";
		}

		if ("NOTFOUND".equalsIgnoreCase(order)) // ls_order = "NOTFOUND" then
		{
			errcode = "VTUOMCONV";
		} else {
			if ("REVORD".equalsIgnoreCase(order)) // ls_order = "REVORD" then
			{
				if (!(mastfact == 0)) {
					mastfact = 1 / mastfact;
				}
				if (!(varience == 0)) {
					varience = 1 / varience;
				}
			}

			if ("P".equalsIgnoreCase(variencetype)) // ls_variencetype = 'P'
				// then
			{
				varience = mastfact * varience / 100;
			} else if ("F".equalsIgnoreCase(variencetype)) // ls_variencetype =
				// 'F' then
			{// ld_varience
			}
			if (convfact > mastfact + varience) {
				errcode = "VTUOMVAR";
			}
		}

		return errcode;

	}

	private String isQcReqd(String siteCodeDlv, String itemCode, Connection conn) throws SQLException {
		String qcReqd = "";
		int cnt2 = 0;
		String sql = "select case when qc_reqd is null then 'N' else qc_reqd end from	siteitem where	item_code = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, itemCode);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			qcReqd = rs.getString(1);
			cnt2++;
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if (cnt2 == 0) {
			sql = "select case when qc_reqd is null then 'N' else qc_reqd end from	item where	item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				qcReqd = rs.getString(1);
				cnt2++;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		return qcReqd;
	}

	private String isSpecReqd(String siteCodeDlv, String itemCode, Connection conn) throws SQLException {
		String spqcReqd = "";

		int cnt2 = 0;
		String sql = "select spec_reqd from	siteitem where	item_code = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, itemCode);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			spqcReqd = rs.getString(1);
			cnt2++;
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if (cnt2 == 0) {
			sql = "select nvl(spec_reqd,'N')  from	item where	item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				spqcReqd = rs.getString(1);
				cnt2++;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		return spqcReqd;
	}

	private String isAcctdept(String acctCodeDr, String deptCode, String transer, Connection conn) throws SQLException {
		String errcode = "";
		String acctSpec = "";
		int cnt = 0;
		int cnt1 = 0;
		int cnt2 = 0;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if (deptCode.trim().length() > 0 && deptCode != null) {
			sql = "select count(1)  from department where dept_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, deptCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (cnt == 0) {
				errcode = "VMDEPT1";
			} else {
				sql = "select var_value from finparm where prd_code = '999999' and var_name = 'ACCT_SPECIFIC_DEPT' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					acctSpec = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (acctSpec != null && "Y".equalsIgnoreCase(acctSpec)) {
					sql = "select count(*) from finparm where acct_code = ? and dept_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
					pstmt.setString(2, acctCodeDr);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt1 = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (cnt1 == 0) {
						sql = "select count(1) from accounts_dept where acct_code = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, acctCodeDr);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt2 = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt2 > 0) {
							errcode = "VMDEPT2";
						} else if (cnt2 == 0) {
							if (deptCode != null && deptCode.trim().length() > 0) {
								errcode = "VMDEPT2";
							}
						}
					}
				}
			}
		}

		return errcode;
	}

	private String isEmployeeResign(String empCodeQcaprv, String childNodeName, Connection conn) throws SQLException {
		String errcode = "";
		String sql = null;
		String withHeld = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Timestamp resigDate = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		System.out.println("@@@@@@@@ isEmployeeResign empCodeQcaprv[" + empCodeQcaprv + "]");
		if (empCodeQcaprv != null && empCodeQcaprv.trim().length() > 0) {
			errcode = isExist("employee", "emp_code", empCodeQcaprv, conn);
		}
		System.out.println("@@@@@@@@ isEmployeeResign errcode[" + errcode + "]");

		if ("FALSE".equalsIgnoreCase(errcode)) {
			errcode = "VMEMP1";
		} else {

			errcode = "";
			System.out.println("@@@@@@@@ errcode[" + errcode + "]");
			sql = " select resi_date, with_held from employee where emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCodeQcaprv);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				resigDate = rs.getTimestamp(1);
				withHeld = rs.getString(2);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (resigDate != null) {
				errcode = "VMEMP7";
				errList.add(errcode);
				errFields.add(childNodeName.toLowerCase());
			} else if ("Y".equalsIgnoreCase(withHeld)) {
				errcode = "VMEMP9";
				errList.add(errcode);
				errFields.add(childNodeName.toLowerCase());
			}

		}
		System.out.println("@@@@@@@@ return isEmployeeResign errcode[" + errcode + "]");

		return errcode;
	}

	private String isExist(String table, String field, String value, Connection conn) throws SQLException {
		String sql = "", retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if (cnt > 0) {
			retStr = "TRUE";
		}
		if (cnt == 0) {
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::[" + cnt + "]");
		return retStr;
	}

	//Added BY Anjali R. to set node value in dom on[22/11/2017][Start]
	private static void updateNodeValue(Document doc, String columnName, String columnValue, String formNo) {
		NodeList nodeList = doc.getElementsByTagName("Detail" + formNo);
		Node node = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);

			if (node != null && node.getNodeName().equalsIgnoreCase(columnName)) {
				node.setNodeValue(columnValue);
			}
		}
	}
	//Added BY Anjali R. to set node value in dom on[22/11/2017][End]
	//Added by Varsha V on 08-06-2020 
	public String checkInt(String inputVal) {
		if (inputVal == null) {
			inputVal = "0";
		}
		return inputVal;
	}
	//Ended by Varsha V on 08-06-2020 
}