package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
@Stateless
public class POrderAmdIC extends ValidatorEJB implements POrderAmdICLocal, POrderAmdICRemote {
	// GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon discommon = new DistCommon();//Modified by Rohini T on 15/04/2021
	// method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		System.out.println("wfValdata() called for POrderAmdIC");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				System.out.println("xmlString[" + xmlString + "]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1[" + xmlString1 + "]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2[" + xmlString2 + "]");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int cnt = 0;
		int ctr = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		int currentFormNo = 0, recCnt = 0;
		FinCommon fincommon = new FinCommon();
		DistCommon discommon = new DistCommon();
		Timestamp amdDate = null, dlvDate = null, reqDate = null, ordDate = null;
		String siteCodeOrd = "", purcOrder = "", status = "", amdStatus = "", confirmed = "", modName = "",
				siteCodeDlv = "", siteCodeBill = "", suppCode = "";
		String suppCodeO = "", deptCode = "", empCode = "", itemSer = "", crTerm = "", purcOrderHdr = "";
		String currCode = "", suppCurrCode = "", baseCurrCode = "", exchRate = "", amdDateStr = "";
		String taxEnv = "", taxEnvpt = "", taxChap = "", taxChappt = "", taxClass = "", taxClasspt = "", projCode = "",
				quotNo = "", salesPers = "", varValue = "";
		String siteCode = "", siteCodept = "", itemCode = "", ordDateStr = "", amdNo = "", lineNo = "",
				stopBusiness = "", lineNoOrd = "";
		String unit = "", unitStd = "", packCode = "", locCode = "", cctrCodeCr = "", dlvDateStr = "", reqDateStr = "";
		String convRtuomStduomStr = "", unitRate = "", quantityStr = "", convQtyStduomStr = "", workOrder = "",
				Status = "", acctCodeDr = "", acctCodeCr = "";
		double convRtuomStduom = 0, quantity = 0, convQtyStduom = 0, rate1 = 0, purcRate = 0, maxRate = 0;
		String indNo = "", cctrCodeDr = "";
		String discount = "";
		double preQty = 0, indQty = 0, qtybrow = 0, ordQty = 0, dlvQty = 0, rate = 0, rateO = 0, quantityStduom = 0;
		String rateStr = "", rateOStr = "", quantityStduomStr = "";
		String curlineNo = "", tempindno = "", templinenoord = "", templineno = "", curlineno = "", qtybrowStr = "";
		String rateStduomStr = "", empCodeQcaprv = "", qcReqd = "", withHeld = "";
		Timestamp resigDate = null, duedate = null;
		String pordType = "", sql1 = "";
		int cntproj = 0, cntporder = 0, cntamd = 0, cntind = 0, cntindhdr = 0;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String dutyPaid = "", eou = "", lopReqd = "", formNo = "", dutyPaidO = "", formNoO = "", quantityO = "",
				termCode = "";
		String lineNoBrow = "", amdNoTemp = "", formNoTemp = "", lineNoTemp = "", quantitybrow = "", totalQuantity = "";
		double totQty = 0, ct3Qty = 0, qtyUsed = 0;
		double dis = 0;
		double Disrate = 0;
		String lnNo = "", lnNoStr = "";
		int noOfParent = 0, ctr1 = 0;
		int count11 = 0;
		int count21 = 0;
		String projCodeO = "";
		String projindNo = "";
		String ProjStatus = "";
		String linnoprev = "";
		String poStatus = "";
		String vouchcreated = "";
		String linepayterm = "";
		String linepurmil = "";
		ArrayList Linetmp = new ArrayList();
		double lineord1 = 0;
		double rel_amt = 0, ord_amt = 0, tot_amt = 0, amount = 0, adv_amt = 0;
		int lineNoo = 0, count = 0, row = 0;
		String type = "", task_code = "", taskcodeparent = "", rel_agnst = "", val = "", val1 = "", retval = "";
		double mmin_day = 0, mmax_day = 0, mmin_amt = 0, mmax_amt = 0;
		boolean isLine = false;
		boolean excedAmtFlag = true;
		String pordNo = "", linenoOrdBrow = "";
		Timestamp pordDate = null;
		double indBalance = 0;
		double totAmtIndent = 0;
		double totAmtDetpordertemp = 0, totAmtDetpordertot = 0.0, totAmtDetpordertotNtc = 0.0,
				totAmtDetpordertotPorcpConf = 0.0, totAmtDetpordertotPorcpSer = 0.0;
		;
		double totAmtDetPoamdDetbrow = 0, totAmtDetIndent = 0, totAmtDetporder = 0, approxCost = 0, totAmtProj = 0;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String linearr[];
		String temparr = "", sql2 = "";
		int a = 0;
		ArrayList line = new ArrayList();
		double quantitypoamddet = 0, ratepoamddet = 0, totAmtDetPoamdDetbrowtemp = 0, exRate = 0, dlvQuty = 0, qty = 0;
		String line1 = "";
		String startStr = "", endStr = "", descrStr = "", descrStart = "", descrEnd = "", exchRateStr = "", value = "";
		double discountpoamddet = 0, taxpoamddet = 0, totAmtpoamd = 0;
		boolean ValueType = false, ValueProjectType = false, firstNull = true;
		int cntindamdhdr = 0, itrArr = 0, maxLine = 0;
		int pos;
		String totAmtDetpordertempstr = "", quantitypoamd = "", ratepoamd = "", acctCodeCrporder = "", exchate = "";
		String acctCodeCrbrow = "", taxpoamd = "";
		String discountpoamd = "", varValueprojectType = "", invAcctSer = "N";
		String rateOld = "", qtyOld = "", sumqty = "", totAmtDetPoamdDetbrow1 = "", invAcctPorcp = "", invAcctQc = "";
		double rateOldamd = 0, qtyOldamd = 0, sumQtyamd = 0, sumQtyamdNew = 0, excedAmt = 0;

		DistCommon distCommon = new DistCommon();
		try {
			System.out.println("@@@@@@@@ wfvaldata called");
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("amd_date")) {
						amdDateStr = checkNull(genericUtility.getColumnValue("amd_date", dom));
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
						System.out.println("@@@@ amd_date[" + amdDateStr + "]");
						if (amdDateStr == null || amdDateStr.length() == 0) {
							amdDate = Timestamp.valueOf(
									genericUtility.getValidDateString(amdDateStr, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
							// Changes and Commented By Ajay on 20-12-2017 :START
							// errCode = this.nfCheckPeriod("PUR", amdDate,siteCodeOrd);
							errCode = finCommon.nfCheckPeriod("PUR", amdDate, siteCodeOrd, conn);
							// Changes and Commented By Ajay on 20-12-2017 :END
							if (errCode != null && errCode.trim().length() > 0) {
								// errCode = "VMORDTYNUL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("purc_order")) {
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
						System.out.println("@@@@ purc_order[" + purcOrder + "]");
						if (purcOrder != null && purcOrder.length() > 0) {
							sql = "	select status, confirmed from porder where purc_order = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								recCnt++;
								status = rs.getString("status");
								confirmed = rs.getString("confirmed");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (recCnt == 0) {
								errCode = "VTPORD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("C".equalsIgnoreCase(status) || "X".equalsIgnoreCase(status)) {
								errCode = "VTCLOSE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!("Y".equalsIgnoreCase(confirmed))) {
								errCode = "VTNOTCONF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							// Start Added by chandrashekar on 06-may-2016
							if (!ispurcOrderDom(dom2, purcOrder)) {
								errCode = "VTNOTMATCH";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// End Added by chandrashekar on 06-may-2016

						}
					}

					if (childNodeName.equalsIgnoreCase("site_code__dlv")) {
						siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
						System.out.println("@@@@ site_code__dlv[" + siteCodeDlv + "]");
						errCode = this.isSiteCode(siteCodeDlv, modName);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("site_code__ord")) {
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
						System.out.println("@@@@ site_code__ord[" + siteCodeOrd + "]");
						errCode = this.isSiteCode(siteCodeOrd, modName);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("site_code__bill")) {

						siteCodeBill = checkNull(genericUtility.getColumnValue("site_code__bill", dom));
						System.out.println("@@@@ site_code__bill[" + siteCodeBill + "]");
						errCode = this.isSiteCode(siteCodeBill, modName);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("dept_code")) {
						deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));
						System.out.println("@@@@ dept_code[" + deptCode + "]");
						errCode = isExist("department", "dept_code", deptCode, conn);
						if ("FALSE".equalsIgnoreCase(errCode)) {
							errCode = "VTDEPT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("emp_code")) {
						empCode = checkNull(genericUtility.getColumnValue("emp_code", dom));
						System.out.println("@@@@ emp_code[" + empCode + "]");
						errCode = isExist("employee", "emp_code", empCode, conn);
						if ("FALSE".equalsIgnoreCase(errCode)) {
							errCode = "VMEMP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("emp_code__aprv")) {
						empCode = checkNull(genericUtility.getColumnValue("emp_code__aprv", dom));
						System.out.println("@@@@ emp_code[" + empCode + "]");
						errCode = isExist("employee", "emp_code", empCode, conn);
						if ("FALSE".equalsIgnoreCase(errCode)) {
							errCode = "VMEMP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("item_ser")) {

						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
						System.out.println("@@@@ item_ser[" + itemSer + "]");
						errCode = isExist("itemser", "item_ser", itemSer, conn);
						if ("FALSE".equalsIgnoreCase(errCode)) {
							errCode = "VTITEMSER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("supp_code")) {

						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						suppCodeO = checkNull(genericUtility.getColumnValue("supp_code__o", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));
						System.out.println("@@@@ supp_code[" + suppCode + "]");

						if (!(suppCode.equalsIgnoreCase(suppCodeO))) {
							errCode = isExist("supplier", "supp_code", suppCode, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTINVSUPCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							errCode = isExist("voucher", "purc_order", purcOrder, conn);
							if ("TRUE".equalsIgnoreCase(errCode)) {
								errCode = "VTVOUCHEXT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							errCode = isExist("porcp", "purc_order", purcOrder, conn);
							if ("TRUE".equalsIgnoreCase(errCode)) {
								errCode = "VTPORCPEXT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("cr_term")) {
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						System.out.println("@@@@ cr_term[" + crTerm + "]");

						if (crTerm != null && crTerm.length() > 0) {
							errCode = isExist("crterm", "cr_term", crTerm, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTCRTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("curr_code")) {

						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						System.out.println("@@@@ curr_code[" + currCode + "]::::supp_code[" + suppCode + "]");
						if (currCode != null && currCode.trim().length() > 0) {
							errCode = isExist("currency", "curr_code", currCode, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTCURRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								suppCurrCode = setDescription("curr_code", "supplier", "supp_code", suppCode, conn);
								if (suppCurrCode != null && suppCurrCode.length() > 0) {
									sql = "select curr_code from parameter ";
									pstmt = conn.prepareStatement(sql);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										baseCurrCode = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if ((!(currCode.equalsIgnoreCase(suppCurrCode)))
											&& (!(currCode.equalsIgnoreCase(baseCurrCode)))) {
										errCode = "VTCURR2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						} // end if
					}
					if (childNodeName.equalsIgnoreCase("exch_rate")) {

						exchRate = checkNull(genericUtility.getColumnValue("exch_rate", dom));
						System.out.println("@@@@ exch_rate[" + exchRate + "]");
						if (exchRate != null && exchRate.length() > 0) {
							if (Double.parseDouble(exchRate) <= 0) {
								errCode = "VTEXCHRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							errCode = "VTEXCHRATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						System.out.println("@@@@ tax_chap[" + taxChap + "]");
						if (taxChap != null && taxChap.length() > 0) {
							errCode = isExist("taxchap", "tax_chap", taxChap, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						System.out.println("@@@@ tax_class[" + taxClass + "]");
						if (taxClass != null && taxClass.length() > 0) {
							errCode = isExist("taxclass", "tax_class", taxClass, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("tax_env")) {
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						amdDateStr = genericUtility.getColumnValue("amd_date", dom);
						System.out.println("@@@@ tax_env[" + taxEnv + "]");
						if (taxEnv != null && taxEnv.length() > 0) {
							errCode = isExist("taxenv", "tax_env", taxEnv, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTAXENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								// Pavan R 17sept19 start[to validate tax environment]
								if (amdDateStr != null && amdDateStr.length() > 0) {
									amdDate = Timestamp.valueOf(genericUtility.getValidDateString(amdDateStr,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}
								// errCode = fincommon.checkTaxEnvStatus(taxEnv,amdDate, conn);
								errCode = discommon.getCheckTaxEnvStatus(taxEnv, amdDate, "P", conn);
								// Pavan R 17sept19 end[to validate tax environment]
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("proj_code")) {
						projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
						System.out.println("@@@@ proj_code[" + projCode + "]");
						if (projCode != null && projCode.length() > 0) {
							errCode = isExist("project", "proj_code", projCode, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTPROJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("quot_no")) {
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						System.out.println("@@@@ quot_no[" + quotNo + "]");
						if (quotNo != null && quotNo.length() > 0) {
							sql = "select count(*) from pquot_hdr where quot_no = ? and status = 'A' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, quotNo);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTPQUOT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("sales_pers")) {
						salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));
						siteCodeOrd = checkNull(genericUtility.getColumnValue("site_code__ord", dom));

						System.out.println("@@@@ sales_pers[" + salesPers + "]");
						if (salesPers != null && salesPers.length() > 0 && siteCodeOrd != null
								&& siteCodeOrd.length() > 0) {
							varValue = this.getEnvDis("999999", "SITE_SPECIFIC_SPERS", conn);
							if ("Y".equalsIgnoreCase(varValue)) // mvar_value =
																// 'Y' then
							{
								sql = "	select count(*) from site_sales_pers where site_code = ? and sales_pers = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCodeOrd);
								pstmt.setString(2, salesPers);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									errCode = "VTSLPERS2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

							errCode = isExist("sales_pers", "sales_pers", salesPers, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				} // end for
				break; // case 1 end

			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("@@@@@@@@@@@@childNodeListLength[" + childNodeListLength + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("purc_order")) {
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						purcOrderHdr = checkNull(genericUtility.getColumnValue("purc_order", dom1));
						System.out.println("@@@@ purc_order[" + purcOrder + "]::::purcOrderHdr[" + purcOrderHdr + "]");
						if (!(purcOrder.equalsIgnoreCase(purcOrderHdr))) {
							errCode = "VTPOHDR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("site_code")) {
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						purcOrderHdr = checkNull(genericUtility.getColumnValue("purc_order", dom1));
						System.out.println("@@@@ siteCode[" + siteCode + "]");
						errCode = this.isSiteCode(siteCode, modName);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("line_no__ord")) {
						lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						if (lineNoOrd != null && lineNoOrd.length() > 0) {
							if ((lineNoOrd.trim().equalsIgnoreCase("0")))// VALLABH KADAM Add validation for
																			// line_no__ord==0 20/NOV/14
							{
								errCode = "VTINVPOLIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
								lineNoOrd = "    " + lineNoOrd;
								lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3, lineNoOrd.length());
								amdStatus = checkNull(genericUtility.getColumnValue("status", dom));
								sql = " select status,dlv_qty,quantity from porddet where purc_order = ? and line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								pstmt.setString(2, lineNoOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) {

									status = rs.getString("status");
									dlvQuty = rs.getDouble("dlv_qty");
									qty = rs.getDouble("quantity");

								}
								System.out.println("STATUS@@" + status);
								System.out.println("amdStatus@@" + amdStatus);

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("C".equalsIgnoreCase(status)) {
									// Condition used for allow close to open PO
									System.out.println("STATUS@@" + status);
									if ((qty - dlvQuty) > 0 && amdStatus.equals("O")) {
										System.out.println("status@(qty-dlvQuty)>0@" + status);
										System.out.println("entered in@@::[(qty-dlvQuty)>0]::");
										break;

									}
									System.out.println("checking staus 'C'::[" + status + "]::");
									errCode = "VTAMDDET";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

						if (errCode == null || errCode.trim().length() == 0) {
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
							amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));

							sql = " select count(1) from poamd_det where  amd_no = ? and line_no  <> ? "
									+ " and purc_order   = ? and	 line_no__ord = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, amdNo);
							pstmt.setString(2, lineNo);
							pstmt.setString(3, purcOrder);
							pstmt.setString(4, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								errCode = "VMPROCUSG";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("item_code")) {
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						ordDateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));

						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						{
							sql = " select case when stop_business is null then 'N' else stop_business end From Item Where  item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								stopBusiness = rs.getString(1);
							}
							System.out.println("stopBusiness@@" + stopBusiness);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						sql = " select status,line_no From porddet Where purc_order = ? and line_no=? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNo);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							poStatus = rs.getString(1);

						}
						System.out.println("STATUS OF PO-->" + poStatus);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ("N".equalsIgnoreCase(stopBusiness)) {
							errCode = this.isItem(siteCode, itemCode, modName, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							System.out.println("STOPBUSINESSS_FLAG='Y'@@@");
							if ((poStatus.equals("O")) && ("Y".equalsIgnoreCase(stopBusiness))) {
								System.out.println("@@allow to close with item stopflag=yes");

								errCode = this.isItem(siteCode, itemCode, modName, conn);
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								break;

							}

							errCode = "VTIIC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}

					}

					if (childNodeName.equalsIgnoreCase("unit")) {
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						errCode = isExist("uom", "unit", unit, conn);
						if ("FALSE".equalsIgnoreCase(errCode)) {
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!(unit.equalsIgnoreCase(unitStd))) {
							String conv__qty_stduomStr = checkNull(
									genericUtility.getColumnValue("conv__qty_stduom", dom));
							if (conv__qty_stduomStr == null || "0".equalsIgnoreCase(conv__qty_stduomStr)) // isnull(lc_conv)
																											// or
																											// lc_conv =
																											// 0 then

							{
								sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unit);
								pstmt.setString(2, unitStd);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									sql = "select count(*)  from uomconv where unit__to = ? and unit__fr = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, unit);
									pstmt.setString(2, unitStd);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt == 0) {
										errCode = "VTUNIT3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("loc_code")) {
						locCode = checkNull(genericUtility.getColumnValue("loc_code", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						errCode = isExist("location", "loc_code", locCode, conn);
						if ("FALSE".equalsIgnoreCase(errCode)) {
							errCode = "VTLOC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("work_order")) {
						workOrder = checkNull(genericUtility.getColumnValue("work_order", dom));
						if (workOrder != null && workOrder.length() > 0) {
							Status = setDescription("status", "workorder", "work_order", workOrder, conn);
							if (Status == null || Status.length() == 0) {
								errCode = "VTWORD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("C".equalsIgnoreCase(status) || "X".equalsIgnoreCase(status)) {
								errCode = "VTWORDER2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("pack_code")) {
						packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
						if (packCode != null && packCode.length() > 0) {
							errCode = isExist("packing", "pack_code", packCode, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTPKCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("acct_code__dr")) {
						acctCodeDr = genericUtility.getColumnValue("acct_code__dr", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						System.out.println("@@@@@ acctCodeDr [" + acctCodeDr + "]");
						if (acctCodeDr != null && acctCodeDr.length() > 0) {
							errCode = fincommon.isAcctCode(siteCode, acctCodeDr, modName, conn);
							// errCode = this.isAcctCode(siteCode, acctCodeDr,
							// modName);
							if (errCode != null && errCode.trim().length() > 0) {
								// errCode = "VTPKCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if (errCode == null || errCode.trim().length() == 0) {

								errCode = finCommon.isAcctType(acctCodeDr, "", "O", conn);
								// ----End---------
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

						} else {
							errCode = "VMACCTDRNU";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("acct_code__cr")) {

						acctCodeCr = genericUtility.getColumnValue("acct_code__cr", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						System.out.println("@@@@@ acctCodeCr [" + acctCodeCr + "]");
						if (acctCodeCr != null && acctCodeCr.length() > 0) {
							System.out.println("@@@@@ acctCodeCr [" + acctCodeCr.trim() + "]");

							errCode = fincommon.isAcctCode(siteCode, acctCodeCr, modName, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								invAcctPorcp = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

								if (!"ERROR".equalsIgnoreCase(invAcctPorcp)) {
									if (invAcctPorcp == null || "NULLFOUND".equalsIgnoreCase(invAcctPorcp)
											|| invAcctPorcp.trim().length() == 0) {
										invAcctPorcp = "N";
									}

								}
								// 19-dec-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
								if ("S".equals(invAcctPorcp)) {
									itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
									sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										invAcctSer = checkNull(rs.getString("inv_acct"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if (invAcctSer == null || invAcctSer.trim().length() == 0) {
										invAcctSer = "N";
									}
									invAcctPorcp = invAcctSer;
								}
								// end 19-dec-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the

								invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

								if (!"ERROR".equalsIgnoreCase(invAcctQc)) {
									invAcctQc = "N";
								}

								if ("N".equalsIgnoreCase(invAcctPorcp) || "N".equalsIgnoreCase(invAcctQc)) {
									if ("Y".equalsIgnoreCase(invAcctPorcp)) {

										errCode = finCommon.isAcctType(acctCodeCr, "", "O", conn);
										if (errCode != null && errCode.trim().length() > 0) {
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									} else {

										errCode = finCommon.isAcctType(acctCodeCr, suppCode, "S", conn);
										if (errCode != null && errCode.trim().length() > 0) {
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
							if (errCode == null || errCode.trim().length() == 0) {
								// added by Priyanka Das for acctcodecr validation
								// request id -d15esun002
								// sql ="select max(line_no) from porddet where purc_order = ? ";
								sql = "select trim(max(cast(line_no as number))) from porddet where purc_order = ?";// Changed
																													// by
																													// Jaffar
																													// S.
																													// on
																													// 22-11-18
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									maxLine = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								/*
								 * if(maxLine > 1) {
								 */

								NodeList detail2List = dom2.getElementsByTagName("Detail2");
								noOfParent = detail2List.getLength();
								acctCodeCr = genericUtility.getColumnValue("acct_code__cr", dom);
								acctCodeCr = acctCodeCr == null ? "" : acctCodeCr.trim();
								System.out.println("<acctCodeCr >@@@@@@ " + acctCodeCr);
								System.out.println("noOfParent@@@@@@@@@@@" + noOfParent);
								a = 0;
								// commented by monika-22-may-2019
								/*
								 * for (a = noOfParent - 1; a >= 0; a--) {
								 */
								// commented by-MOnika-21-may-2019
								/*
								 * //String lineNoBrowacct =
								 * genericUtility.getColumnValueFromNode("line_no",detail2List.item(a));
								 * //lineNoBrowacct = lineNoBrowacct == null ? "": lineNoBrowacct.trim();
								 * //System.out.println("linenoBrowacct>>>>"+lineNoBrowacct); sql1
								 * ="select acct_code__cr from porddet where purc_order = ?"; pstmt =
								 * conn.prepareStatement(sql1); pstmt.setString(1, purcOrder);
								 * //pstmt.setString(2,lineNoBrowacct); rs = pstmt.executeQuery(); if
								 * (rs.next()) { acctCodeCrporder = rs.getString(1); } rs.close(); rs = null;
								 * pstmt.close(); pstmt = null; System.out.println("noOfParent@@@@@@@@@@@"+
								 * noOfParent); System.out.println("a@@@@ value" + a); //
								 * System.out.println("ctr1@@@@@@@@"+ctr1);
								 * 
								 * 
								 * acctCodeCrbrow =
								 * genericUtility.getColumnValueFromNode("acct_code__cr",detail2List.item(a));
								 * acctCodeCrbrow = acctCodeCrbrow == null ? "": acctCodeCrbrow.trim();
								 * System.out.println("@@@@acctCodeCrBrow------"+ acctCodeCrbrow);
								 * System.out.println("@@@@acctCodeCr------"+ acctCodeCr);
								 * System.out.println("AcctCodeCrPorder>>>>>"+acctCodeCrporder);
								 * if(!(acctCodeCr.trim().equals(acctCodeCrporder.trim()))) {
								 * System.out.println("ACCTCODECR IN LOOP>>>"+acctCodeCr);
								 * System.out.println("aCCTCODEPORDER IN LOOP???"+acctCodeCrporder); errCode =
								 * "VTACCTCODE"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase());
								 * System.out.println("in error for vtacctcode with existing porder"); break;
								 * 
								 * }
								 * 
								 * System.out.println("!(acctCodeCr.equals(acctCodeCrbrow)"+
								 * !(acctCodeCr.equals(acctCodeCrbrow))); if
								 * (!(acctCodeCr.equals(acctCodeCrbrow))) {
								 * 
								 * errCode = "VTACCTCODE"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase());
								 * System.out.println("in error for vtacctcode"); break; }
								 */
								// Changes-by-monika-on-21-may-2019

								for (a = noOfParent - 1; a >= 0; a--) {
									childNode = childNodeList.item(ctr1);
									childNodeName = childNode.getNodeName();
									System.out.println("childNodeName[" + childNodeName + "]");
									if (childNodeName.equalsIgnoreCase("acct_code__cr")) {
										acctCodeCr = checkNull(genericUtility.getColumnValue("acct_code__cr", dom2))
												.trim();
										lineNoTemp = checkNull(genericUtility.getColumnValue("line_no", dom2)).trim();
										// Added By PRiyankaC on 04JAn18[START]
										if (acctCodeCr == null || acctCodeCr.trim().length() == 0) {
											errList.add("VMACCODE1 ");
											errFields.add(childNodeName.toLowerCase());
										}
										// Added By PRiyankaC on 04JAn18[END]
										else if (!acctCodeCr.equalsIgnoreCase(acctCodeCr.trim())
												&& (!lineNoTemp.equalsIgnoreCase(lineNo.trim()))) {
											errCode = "VTACCTCODE";
											// +
											// "~t In a Single PO two account code credit is not allowed";
											if (errCode != null && errCode.trim().length() > 0) {
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}

									}

								} // end
							}

						} else {
							errCode = "VMACCTCRNU";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("cctr_code__dr")) {
						cctrCodeDr = genericUtility.getColumnValue("cctr_code__dr", dom);
						System.out.println("@@@@@ cctrCodeDr [" + cctrCodeDr + "]");
						if (cctrCodeDr != null && cctrCodeDr.length() > 0) {
							acctCodeDr = genericUtility.getColumnValue("acct_code__dr", dom);
							// errCode = this.isCctrCode(acctCodeDr, cctrCodeDr,
							// modName);
							System.out.println("@@@@@ cctrCodeDr acctCodeDr [" + acctCodeDr + "]");
							errCode = fincommon.isCctrCode(acctCodeDr, cctrCodeDr, modName, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						} else {
							errCode = "VMCCTRDRNU";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}

					if (childNodeName.equalsIgnoreCase("cctr_code__cr")) {
						cctrCodeCr = genericUtility.getColumnValue("cctr_code__cr", dom);
						System.out.println("@@@@@ cctrCodeCr [" + cctrCodeCr + "]");
						if (cctrCodeCr != null && cctrCodeCr.length() > 0) {
							acctCodeCr = genericUtility.getColumnValue("acct_code__cr", dom);
							// errCode = this.isCctrCode(acctCodeCr, cctrCodeCr,
							// modName);
							System.out.println("@@@@@ cctrCodeCr acctCodeCr [" + acctCodeCr + "]");
							errCode = fincommon.isCctrCode(acctCodeCr, cctrCodeCr, modName, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							errCode = "VMCCTRCRNU";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}

					if (childNodeName.equalsIgnoreCase("proj_code")) {
						String ordType = "";
						String projType = "";
						projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
						projCodeO = checkNull(genericUtility.getColumnValue("proj_code__o", dom));
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						ordType = checkNull(genericUtility.getColumnValue("PORD_TYPE", dom1));
						System.out.println("Proj Code" + projCode);
						// if (projCode.trim().length() > 0 || projCode != null)
						if (projCode.trim().length() > 0 && projCode != null) // Changed by Pankaj.R on 30-APR-15

						{

							sql = "select case when proj_status is null then 'X' else proj_status end proj_status,ind_no "
									+ "from project where proj_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ProjStatus = checkNull(rs.getString("proj_status"));
								projindNo = checkNull(rs.getString("ind_no"));

								System.out.println("Project Status inside loop is " + ProjStatus);
								System.out.println("Indent no is " + projindNo);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							System.out.println("Project Status outside loop is " + ProjStatus);
							if (!("A".equalsIgnoreCase(ProjStatus))) {
								System.out.println("Project Status2 outside loop is " + ProjStatus);
								System.out.println("In VTPROJ2");
								errCode = "VTPROJ2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if (ordType.equals("P")) {
								if (!(indNo.trim().equalsIgnoreCase(projindNo.trim()))) {
									System.out.println("Indentno is " + indNo);
									System.out.println("ProjIndentno is " + projindNo);
									errCode = "VINDPJMM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						// Added by Priyanka Das Request ID -D15ESUN002

						pordNo = checkNull(genericUtility.getColumnValue("purc_order", dom));
						lineNoOrd = genericUtility.getColumnValue("line_no__ord", dom);
						lineNoOrd = lineNoOrd == null || lineNoOrd.trim().length() == 0 ? " " : lineNoOrd;
						amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						sql = "select pord_type from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							pordType = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Amdtype.........." + pordType);
						varValue = discommon.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn);
						System.out.println(">>>>>>>>>In for loop afer schemeKey varValue:" + varValue);
						varValue = varValue.trim();
						if (varValue != null && varValue.trim().length() > 0) {
							String varValArr[] = varValue.split(",");
							ArrayList varValList = new ArrayList<String>(Arrays.asList(varValArr));

							System.out.println("varValList ::" + varValList);
							System.out.println("varValList.contains(pordType) ::" + varValList.contains(pordType));

							if (varValList.contains(pordType)) {
								ValueType = true;
							} else {
								ValueType = false;
							}

						}

						System.out.println("ValueType11111>>>>" + ValueType);
						if (varValue != null && varValue.trim().length() > 0 && varValue != "NULLFOUND"
								&& ValueType == true) {
							System.out.println("ValueType2222222>>>>" + ValueType);

							System.out.println("Project Code>>>> [" + projCode);

							if (projCode == null || projCode.trim().length() == 0) {
								errCode = "VEPRJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if (projCode != null && projCode.trim().length() > 0) {
								errCode = isExist("project", "proj_code", projCode, conn);
								if ("FALSE".equalsIgnoreCase(errCode)) {
									errCode = "VTPROJ1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								ordDateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
								System.out.println("orderdate>>>>" + ordDateStr);
								pordDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDateStr,
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
										+ " 00:00:00.0");

								System.out.println("pordDate>>>>" + pordDate);
								sql = "select count(*) from project where proj_code = ? and ? between start_date"
										+ " and end_date OR ? between start_date and ext_end_date";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								pstmt.setTimestamp(2, pordDate);
								pstmt.setTimestamp(3, pordDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cntproj = rs.getInt(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Count for project not between order date>>>> + " + cntproj);
								if (cntproj == 0) {
									errCode = "VTINVORDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

								sql1 = "select approx_cost from project where proj_code = ?";
								projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setString(1, projCode);
								System.out.println("Project code is....." + projCode);
								rs1 = pstmt1.executeQuery();
								if (rs1.next()) {
									approxCost = rs1.getDouble(1);
									System.out.println("approxCost for Project Code>>>>>>>> " + approxCost);

								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								/*
								 * sql = "select count(*) from porder,porddet" +
								 * " where (porddet.purc_order = porder.purc_order)" +
								 * "and porder.confirmed = 'N' and porddet.proj_code = ?"; pstmt =
								 * conn.prepareStatement(sql); pstmt.setString(1, projCode); rs =
								 * pstmt.executeQuery(); if (rs.next()) { cntporder = rs.getInt(1); }
								 * rs.close(); rs = null; pstmt.close(); pstmt = null;
								 * System.out.println("Count for porder>>>> + "+ cntporder); if (cntporder >= 1)
								 * { errCode = "VTDUPROJ1"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase());
								 * 
								 * }
								 */
								System.out.println("AmdNo>>>>>" + amdNo);
								if (amdNo == null || amdNo.trim().length() == 0) {
									amdNo = "@@@@@";
								}
								/*
								 * sql = "select count(*) from poamd_hdr,poamd_det" +
								 * " where (poamd_det.amd_no = poamd_hdr.amd_no)" +
								 * "and poamd_hdr.confirmed = 'N' and poamd_det.proj_code = ? " +
								 * "and poamd_det.amd_no ! = ?"; pstmt = conn.prepareStatement(sql);
								 * pstmt.setString(1, projCode); pstmt.setString(2, amdNo); rs =
								 * pstmt.executeQuery(); if (rs.next()) { cntamd = rs.getInt(1); } rs.close();
								 * rs = null; pstmt.close(); pstmt = null;
								 * System.out.println("Count for poamd>>>> + "+ cntamd); if (cntamd >= 1) {
								 * errCode = "VTDUPRO3"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase()); }
								 * 
								 */ /*
									 * CODE FOR INDENT AND INDENT REQ VALIDATION COUNT
									 */

								/*
								 * sql = "select count(*) from indent where status = 'U' and proj_code = ?";
								 * pstmt = conn.prepareStatement(sql); pstmt.setString(1, projCode); rs =
								 * pstmt.executeQuery(); if (rs.next()) { cntind = rs.getInt(1); } rs.close();
								 * rs = null; pstmt.close(); pstmt = null;
								 * System.out.println("Count for indent>>>> + "+ cntind); if (cntind >= 1) {
								 * errCode = "VTDUPROJ2"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase());
								 * 
								 * } sql =
								 * "select count(*) from indent_hdr where confirmed = 'N' and proj_code =  ? ";
								 * pstmt = conn.prepareStatement(sql); pstmt.setString(1, projCode); rs =
								 * pstmt.executeQuery(); if (rs.next()) { cntindhdr = rs.getInt(1); }
								 * rs.close(); rs = null; pstmt.close(); pstmt = null;
								 * System.out.println("Count for indent where unconfirmed indent>>>> + "+
								 * cntindhdr); if (cntindhdr >= 1) { errCode = "VTDUPROJ"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase()); } sql1 =
								 * "select count(*) from indent_amd where confirmed = 'N' and proj_code =  ? ";
								 * pstmt = conn.prepareStatement(sql1); pstmt.setString(1, projCode); rs =
								 * pstmt.executeQuery(); if (rs.next()) { cntindamdhdr = rs.getInt(1); }
								 * rs.close(); rs = null; pstmt.close(); pstmt = null;
								 * System.out.println("Count for indent where unconfirmed indent>>>> + "+
								 * cntindhdr); if (cntindamdhdr >= 1) { errCode = "VTDUPROJ3";
								 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); }
								 * 
								 */ /*
									 * sql1 =" select sum(quantity * purc_rate) " +
									 * "from indent where proj_code = ? " + "and status NOT IN ('C','X')"; projCode
									 * =checkNull (genericUtility.getColumnValue("proj_code" ,dom )); pstmt =
									 * conn.prepareStatement(sql1); pstmt.setString(1, projCode); System.out.println
									 * ("Project code IN (quantity * purc_rate) is....." +projCode); rs =
									 * pstmt.executeQuery(); while (rs.next()) { totAmtIndent =rs.getDouble(1);
									 * System.out.println( "totAmtDet for Indent with quant* rate>>>>>>>> " +
									 * totAmtIndent); } rs.close(); rs = null; pstmt.close(); pstmt = null;
									 * System.out.println ("totAmtDet for Indent with quant* rate>>>>>>>> " +
									 * totAmtIndent); sql =
									 * "select sum(ord_qty * purc_rate) from indent where proj_code = ?" +
									 * "and status NOT IN ('C','X') "; projCode =checkNull
									 * (genericUtility.getColumnValue("proj_code" ,dom )); pstmt =
									 * conn.prepareStatement(sql); pstmt.setString(1, projCode); System.out.println
									 * ("Project code in (ord_qty * purc_rate)is....." +projCode); rs =
									 * pstmt.executeQuery(); while (rs.next()) { indBalance =rs.getDouble(1);
									 * System.out.println( "totAmtDet for Indent with ordqty * purcrate>>>>>>>> "
									 * +indBalance); } rs.close(); rs = null; pstmt.close(); pstmt = null;
									 * totAmtDetIndent =totAmtIndent - indBalance; System.out.println
									 * ("totAmtDet for Indent with quant* rate>>>>>>>> " + totAmtIndent);
									 * System.out.println( "totAmtDet for Indent with ordqty * purcrate>>>>>>>> "
									 * +indBalance); System.out.println(
									 * "totAmtDet for Indent with totAmtDetIndent>>>>>>>> " +totAmtDetIndent);
									 */

								/*
								 * sql = "select sum( a.tot_amt ) from porddet a, porder b" +
								 * " where ( a.purc_order = b.purc_order ) and b.confirmed = ''" +
								 * " and a.proj_code = ? and a.purc_order! = ? and b.status! = 'X'";
								 * 
								 * projCode = checkNull(genericUtility.getColumnValue("proj_code", dom)); pstmt
								 * = conn.prepareStatement(sql); pstmt.setString(1, projCode);
								 * pstmt.setString(2, pordNo); rs = pstmt.executeQuery(); if (rs.next()) {
								 * totAmtDetpordertot = rs.getDouble(1);
								 * System.out.println("totAmtDet for Porder without  in  loop>>>>>>>> "+
								 * totAmtDetpordertot);
								 * 
								 * } rs.close(); rs = null; pstmt.close(); pstmt = null;
								 * System.out.println("totAmtDet for Porder outside loop>>>>>>>> "+
								 * totAmtDetpordertot);
								 */
								System.out.println("@@@@@@@ In @@@@@@@@@");
								sql = "select sum(a.net_amt * b.exch_rate) from " + "porcpdet a, porcp b ,porddet c "
										+ " where ( a.purc_order = c.purc_order ) "
										+ " and (a.tran_id = b.tran_id ) and a.line_no__ord = c.line_no "
										+ " and b.confirmed = 'Y' and c.proj_code = ?  " + " and b.status ! = 'X'"
										+ " and c.status = 'C'  and  b.tran_ser='P-RCP'";
								projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								// pstmt.setString(2, pordNo);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									totAmtDetpordertotPorcpConf = rs.getDouble(1);
									System.out.println(" POrcp VALUE>>>>>>>> " + totAmtDetpordertotPorcpConf);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select sum(a.tot_amt * b.exch_rate) from porddet a, porder b"
										+ " where (a.purc_order = b.purc_order ) and b.confirmed = 'Y' and a.proj_code = ? and b.status! = 'X' and a.status! ='C' and a.purc_order! = ?";

								projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								pstmt.setString(2, pordNo);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									totAmtDetpordertotNtc = rs.getDouble(1);
									System.out.println(
											"totAmtDet for Porder without  in  loop and status pordet is not C>>>>>>>> "
													+ totAmtDetpordertotNtc);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("totAmtDet for Porder outside loop and status pordet is C>>>>>>>> "
										+ totAmtDetpordertotNtc);
								sql = "select sum(a.net_amt * b.exch_rate) from porcpdet a, porcp b ,porddet c "
										+ " where ( a.purc_order = c.purc_order )" + "and (a.tran_id = b.tran_id )"
										+ "and b.confirmed = 'Y' and a.line_no__ord = c.line_no and  c.proj_code = ?"
										+ " and b.status ! = 'X'and c.status = 'C' and b.tran_ser= 'P-RET'";
								projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								// pstmt.setString(2, pordNo);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									totAmtDetpordertotPorcpSer = rs.getDouble(1);
									System.out.println(" return value is>>>>>>>> " + totAmtDetpordertotPorcpSer);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println(
										"totAmtDet for Porder without  in  loop and status pordet is C and POrcp confirmed and return >>>>>>>> "
												+ totAmtDetpordertotPorcpSer);

								System.out.println("totAmtDet for Porder return " + totAmtDetpordertotPorcpSer);
								System.out.println(
										"totAmtDet for Porder and status pordet is C and POrcp confirmed and return"
												+ totAmtDetpordertotPorcpConf);
								System.out.println(
										"totAmtDet for Porder and status pordet is not C" + totAmtDetpordertotNtc);
								System.out.println("totAmtDet for Porder" + totAmtDetpordertot);
								totAmtDetpordertot = totAmtDetpordertotNtc + totAmtDetpordertotPorcpConf
										- totAmtDetpordertotPorcpSer;
								System.out.println(totAmtDetpordertotNtc + "-" + totAmtDetpordertotPorcpConf + "-"
										+ totAmtDetpordertotPorcpSer);
								System.out.println("Consumed Amount " + totAmtDetpordertot);
								System.out.println("dom@@@@" + genericUtility.serializeDom(dom));
								System.out.println("dom1111@@@@" + genericUtility.serializeDom(dom1));
								System.out.println("dom2222222222@@@@" + genericUtility.serializeDom(dom2));
								NodeList detail2List = dom2.getElementsByTagName("Detail2");
								System.out.println("ChildNOde" + childNodeName);
								System.out.println("detail2List" + detail2List);
								noOfParent = detail2List.getLength();
								System.out.println("NO OF PARENT@@@@" + noOfParent);
								int countqty = 0;
								a = 0;
								String excedAmt1 = "";
								double chekAMt = 0;
								String chekAMt1 = "";
								projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
								double tottemp = 0.0;
								String exchRateOld = "";
								double exchRateOldamd = 0;
								System.out.println("projCode>>>>" + projCode);
								for (a = 0; a < noOfParent; a++) {
									System.out.println(" detail2List.item(a)" + detail2List.item(a));
									System.out.println(
											genericUtility.getColumnValueFromNode("proj_code", detail2List.item(a))
													+ "in genericutility");
									String projcodeBrow = checkNull(
											genericUtility.getColumnValueFromNode("proj_code", detail2List.item(a)));
									System.out.println("a[" + a + "]projcodebrow>>>" + projcodeBrow);
									System.out.println("projcode>>" + projCode);
									if (projcodeBrow != null && projcodeBrow.trim().equalsIgnoreCase(projCode.trim())) {
										// EXCH_RATE
										System.out.println("in condition!!!");

										exchate = (genericUtility.getColumnValue("exch_rate", dom1));
										System.out.println("exchate" + exchate);
										exRate = exchate == null ? 0 : Double.parseDouble(exchate);
										System.out.println("exRate" + exRate);
										quantitypoamd = checkNull(genericUtility
												.getColumnValueFromNode("quantity__stduom", detail2List.item(a)));
										quantitypoamddet = quantitypoamd == null ? 0
												: Double.parseDouble(quantitypoamd);
										System.out.println("<quantitySTDUOMpoamd >@@@@@@ " + quantitypoamd);
										ratepoamd = checkNull(genericUtility.getColumnValueFromNode("rate__stduom",
												detail2List.item(a)));
										ratepoamd = ratepoamd != null && ratepoamd.trim().length() > 0
												? ratepoamd.trim()
												: "0";
										System.out.println("<rateSTDUOMpoamd > @@@@@@@@@@@@@@@@@" + ratepoamd);
										ratepoamddet = ratepoamd == null ? 0 : Double.parseDouble(ratepoamd);
										discountpoamd = checkNull(
												genericUtility.getColumnValueFromNode("discount", detail2List.item(a)));
										discountpoamd = discountpoamd != null && discountpoamd.trim().length() > 0
												? discountpoamd.trim()
												: "0";
										discountpoamddet = discountpoamd == null ? 0
												: Double.parseDouble(discountpoamd);
										System.out.println("<discountpoamd > @@@@@@@@@@@@@@@@@" + discountpoamd);
										System.out.println("<discountpoamdDET > @@@@@@@@@@@@@@@@@" + discountpoamddet);
										taxpoamd = checkNull(
												genericUtility.getColumnValueFromNode("tax_amt", detail2List.item(a)));
										taxpoamd = taxpoamd != null && taxpoamd.trim().length() > 0 ? taxpoamd.trim()
												: "0";
										taxpoamddet = taxpoamd == null ? 0 : Double.parseDouble(taxpoamd);
										System.out.println("<taxpoamd > @@@@@@@@@@@@@@@@@" + taxpoamd);
										System.out.println("<taxpoamddet > @@@@@@@@@@@@@@@@@" + taxpoamddet);
										// totAmtDetPoamdDetbrowtemp = quantitypoamddet* ratepoamddet;
										// lc_tot_amt = lc_tot_amt +((((mqty_brow * lc_rate_brow)-((mqty_brow *
										// lc_rate_brow *lc_disc)/100)) + lc_tax_amt)) * lc_exch_rate
										totAmtDetPoamdDetbrowtemp = ((quantitypoamddet * ratepoamddet)
												- ((quantitypoamddet * ratepoamddet * discountpoamddet) / 100)
												+ taxpoamddet) * exRate;
										System.out.println("totAmtDetPoamdDetbrowtemp" + totAmtDetPoamdDetbrowtemp);
										System.out.println("<totAmtDetPoamdDetbrowtemp > @@@@@@@@@@@@@@@@@"
												+ totAmtDetPoamdDetbrowtemp);
										pordNo = checkNull(genericUtility.getColumnValueFromNode("purc_order",
												detail2List.item(a)));
										linenoOrdBrow = checkNull(genericUtility.getColumnValueFromNode("line_no__ord",
												detail2List.item(a)));
										// line_no__ord
										System.out.println("linenoOrdBrow>>>" + linenoOrdBrow);
										if (linenoOrdBrow == null || linenoOrdBrow.trim().length() == 0) {
											linenoOrdBrow = "@";
										}
										System.out
												.println("totAmtDetPoamdDetbrowtemp @@@ " + totAmtDetPoamdDetbrowtemp);
										totAmtDetPoamdDetbrow = totAmtDetPoamdDetbrowtemp + totAmtDetPoamdDetbrow;
										temparr = temparr + linenoOrdBrow + ",";
										rateOld = checkNull(
												genericUtility.getColumnValueFromNode("rate__o", detail2List.item(a)));
										rateOld = rateOld != null && rateOld.trim().length() > 0 ? rateOld.trim() : "0";
										System.out.println("*********rateOLDpoamd*************" + rateOld);
										rateOldamd = rateOld == null ? 0 : Double.parseDouble(rateOld);

										qtyOld = checkNull(genericUtility.getColumnValueFromNode("quantity__o",
												detail2List.item(a)));
										qtyOld = qtyOld != null && qtyOld.trim().length() > 0 ? qtyOld.trim() : "0";
										System.out.println("************QTY OLD poamd **********" + qtyOld);
										qtyOldamd = qtyOld == null ? 0 : Double.parseDouble(qtyOld);
										// EXCH_RATE__O

										exchRateOld = (genericUtility.getColumnValue("exch_rate__o", dom1));
										System.out.println("************QTY OLD poamd **********" + exchRateOld);
										exchRateOld = exchRateOld != null && exchRateOld.trim().length() > 0
												? exchRateOld.trim()
												: "0";
										System.out.println("************QTY OLD poamd **********" + exchRateOld);
										exchRateOldamd = exchRateOld == null ? 0 : Double.parseDouble(exchRateOld);

										sumQtyamd = rateOldamd * qtyOldamd * exchRateOldamd;
										sumqty = String.valueOf(sumQtyamd);

										System.out.println("Sum Quamtity String" + sumqty);
										// sumQtyamd = totAmtDetPoamdDetbrow - sumQtyamd;
										System.out.println("sumQtyamd" + sumQtyamd);
										sumqty = String.valueOf(sumQtyamd);
										System.out.println("ABhi testing add old qty" + sumQtyamd);
										System.out.println("linenoOrdBrow>>>[" + linenoOrdBrow + "]a[" + a + "]");

										if (linenoOrdBrow == null || linenoOrdBrow.trim().length() == 0) {
											linenoOrdBrow = "@";
										}
										/*
										 * if((linenoOrdBrow == null || "@".equalsIgnoreCase(linenoOrdBrow) ||
										 * "".equalsIgnoreCase(linenoOrdBrow) || linenoOrdBrow.trim().length()==0 ) &&
										 * firstNull== true) { firstNull=false;
										 * System.out.println("totAmtDetPoamdDetbrowtemp@@@@@@@@"
										 * +totAmtDetPoamdDetbrowtemp); tottemp+=totAmtDetPoamdDetbrowtemp;
										 * 
										 * System.out.println("tottemp Null @@@@@" + tottemp);
										 * 
										 * } else { System.out.println("totAmtDetPoamdDetbrowtemp else @@@@@@@@"
										 * +totAmtDetPoamdDetbrowtemp); System.out.println("sumQtyamd else @@@@@@@@" +
										 * sumQtyamd); tottemp+= totAmtDetPoamdDetbrowtemp - sumQtyamd; if(tottemp < 0)
										 * { System.out.println(" IF tottemp"); tottemp=0; }
										 * System.out.println("tottemp else @@@@@@@@" + tottemp); }
										 * System.out.println("@@@@@@@@@@ after excedAmt["+excedAmt+"]");
										 * System.out.println("excedAmt is "+excedAmt); /* if(excedAmt <=0) {
										 * excedAmtFlag=false; }
										 */
										System.out.println("@@@@@@@@@@ linenoOrdBrow[" + linenoOrdBrow + "]");
										temparr = temparr + linenoOrdBrow + ",";

									}
								}
								System.out.println("temparr" + temparr);
								if (temparr.length() > 0) {
									String tempArray[] = temparr.split(",");
									System.out.println("tempArray[].length :-[" + tempArray.length + "]");

									System.out.println("line!!!!" + line);
									line1 = "";

									for (itrArr = 0; itrArr < tempArray.length; itrArr++) {
										line1 = line1 + "'" + tempArray[itrArr] + "',";

									}
									System.out.println("line1 before sql:- [" + line1 + "]");
									line1 = line1.substring(0, line1.length() - 1);

									sql1 = "select sum(a.tot_amt * b.exch_rate)  " + "from porddet a, porder b"
											+ " where ( a.purc_order = b.purc_order ) and b.confirmed = 'Y'"
											+ " and a.proj_code = ? and a.purc_order = ? and line_no NOT IN ( " + line1
											+ ") and a.status! ='C'";

									projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
									pstmt = conn.prepareStatement(sql1);
									pstmt.setString(1, projCode);
									pstmt.setString(2, pordNo);

									System.out.println("pordNo is....." + pordNo);
									System.out.println("Project code is....." + projCode);
									rs = pstmt.executeQuery();
									while (rs.next()) {
										totAmtDetpordertempstr = rs.getString(1);
										totAmtDetpordertempstr = totAmtDetpordertempstr != null
												&& totAmtDetpordertempstr.trim().length() > 0
														? totAmtDetpordertempstr.trim()
														: "0.0";
										totAmtDetpordertemp = Double.parseDouble(totAmtDetpordertempstr);
										totAmtDetporder = totAmtDetporder + totAmtDetpordertemp;
										System.out.println("totAmtDet for Porder in  loop>>>>>>>> " + totAmtDetporder);

									}
								}

								System.out.println("totAmtDetpordertot>>>>>>>>  " + totAmtDetpordertot);
								System.out.println("totAmtDetPoamdDetbrow >>>>>>>>  " + totAmtDetPoamdDetbrow);
								System.out.println("totAmtDetpordertemp>>>>>>>>  " + totAmtDetpordertemp);
								totAmtProj = totAmtDetpordertot + +totAmtDetPoamdDetbrow + totAmtDetpordertemp;
								System.out.println("totAmtProj>>>>> " + totAmtProj);
								System.out.println("@@@@@@@@@@ excedAmtFlag222[" + excedAmtFlag + "]");
								if (totAmtProj > approxCost) {
									errCode = "VTPROJCOST";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									// TODO

									/*
									 * errString = getErrorString("proj_code",errCode, userId);
									 * System.out.println("::: errorstring ::: "+ errString); startStr =
									 * errString.substring(0,errString.indexOf("<description>") + 13); endStr =
									 * errString.substring(errString.indexOf("</description>"),errString.length());
									 * descrStr = errString.substring(errString.indexOf("<description>") +
									 * 13,errString.indexOf("</description>")); descrStart = descrStr.substring(0,
									 * descrStr.indexOf("]")); descrEnd =
									 * descrStr.substring(descrStr.indexOf("]"),descrStr.length()); value =
									 * "Approx Cost Of budget : "+ BigDecimal.valueOf(approxCost).toPlainString() +
									 * ", Exceeded Amount : "+ BigDecimal.valueOf(totAmtProj).toPlainString();
									 * System.out.println("Value ::: "+ value); descrStart =
									 * descrStart.concat(value).concat(descrEnd); errString =
									 * startStr.concat(descrStart).concat(endStr); errorType = errorType(conn,
									 * errCode); if (errString.length() > 0) { String bifurErrString =
									 * errString.substring(errString.indexOf("<Errors>") +
									 * 8,errString.indexOf("<trace>")); bifurErrString = bifurErrString +
									 * errString.substring(errString.indexOf("</trace>") + 8,
									 * errString.indexOf("</Errors>")); errStringXml.append(bifurErrString);
									 * errString = ""; System.out.println("In errorString Length"); }
									 * System.out.println("ErrorType>>>>>>"+ errorType); if
									 * (errorType.equalsIgnoreCase("E")) {
									 * System.out.println("ErrorType in loop>>>>>>"+ errorType); break;
									 * 
									 * }
									 */

								}

							}
						} else if (ValueType == false) {
							projCode = checkNull(genericUtility.getColumnValue("proj_code", dom));
							if (projCode != null && projCode.trim().length() > 0) {
								sql1 = "select proj_type from project where proj_code = ?";
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setString(1, projCode);
								System.out.println("Project code is....." + projCode);
								rs1 = pstmt1.executeQuery();
								if (rs1.next()) {
									projType = checkNull(rs1.getString(1));

								}
								System.out.println("ProjectType>>>" + projType);
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								varValueprojectType = discommon.getDisparams("999999", "PROJECT_TYPE_OPT", conn);
								System.out.println(">>>>>>>>>In for loop afer schemeKey varValue for project type : "
										+ varValueprojectType);
								varValueprojectType = varValueprojectType.trim();
								if (varValueprojectType != null && varValueprojectType.trim().length() > 0
										&& varValueprojectType != "NULLFOUND") {
									String varValprojecttypeArr[] = varValueprojectType.split(",");
									ArrayList varValListprojectType = new ArrayList<String>(
											Arrays.asList(varValprojecttypeArr));
									System.out.println("varValList in project Type ::" + varValListprojectType);
									System.out.println("varValListIN PROJECT TYPE.contains(projectType) ::"
											+ varValListprojectType.contains(projType));

									if ((varValListprojectType.contains(projType.trim()))) {
										System.out.println("In error for project type");
										errCode = "VTINVPROJ2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}
								}

							}

						}
					} // Ended by Priyanka Das Request ID -D15ESUN002

					if (childNodeName.equalsIgnoreCase("req_date")) {
						reqDateStr = genericUtility.getColumnValue("req_date", dom);
						ordDateStr = genericUtility.getColumnValue("ord_date", dom1);
						if ((reqDateStr != null && reqDateStr.trim().length() > 0)
								&& (ordDateStr != null && ordDateStr.length() > 0)) {
							reqDate = Timestamp.valueOf(
									genericUtility.getValidDateString(reqDateStr, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
							ordDate = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDateStr, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if (reqDate.before(ordDate)) {
								errCode = "VTPOREQDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("dlv_date")) {
						dlvDateStr = checkNull(genericUtility.getColumnValue("dlv_date", dom));
						ordDateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						if ((dlvDateStr != null && dlvDateStr.length() > 0)
								&& (ordDateStr != null && ordDateStr.length() > 0)) {
							dlvDate = Timestamp.valueOf(
									genericUtility.getValidDateString(dlvDateStr, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
							ordDate = Timestamp.valueOf(
									genericUtility.getValidDateString(ordDateStr, genericUtility.getApplDateFormat(),
											genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if (dlvDate.before(ordDate)) {
								errCode = "VTPOREQDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("rate")) {
						System.out.println("InsideRate :" + rate);
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						rateStr = checkNull(genericUtility.getColumnValue("rate", dom));
						rateOStr = checkNull(genericUtility.getColumnValue("rate__o", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						ordDateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						System.out.println("@@@@@@@@@@@@ ordDateStr from dom1[" + ordDateStr + "]:::::::::::: from dom["
								+ genericUtility.getColumnValue("ord_date", dom) + "]");
						quantityStduomStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));
						lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						quantityStduom = quantityStduomStr == null ? 0 : Double.parseDouble(quantityStduomStr);
						rate = rateStr == null ? 0 : Double.parseDouble(rateStr);
						rateO = rateOStr == null ? 0 : Double.parseDouble(rateOStr);
						System.out.println("Rate is @@@@@ " + rate);
						System.out.println("Rate old  is @@@@@ " + rateO);
						varValue = this.getEnvDis("999999", "REGULATED_PRICE_LIST", conn);

						// Added By PriyankaC on 07JUNE18.[START]
						// Pavan R on 14nov18 [system should not allow any PO line for which there is
						// pending receipt exist] start
						// sql = " SELECT COUNT(*) FROM PORCP WHERE PURC_ORDER = ? and confirmed ='N'";
						sql = "select COUNT(*) from PORCP, PORCPDET" + " WHERE PORCP.TRAN_ID = PORCPDET.TRAN_ID"
								+ " AND PORCPDET.PURC_ORDER = ?" + " AND	PORCP.CONFIRMED ='N'"
								+ " AND LINE_NO__ORD = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();

						if (rs.next()) {
							count = rs.getInt(1);
						}
						System.out.println("Result in Rate " + count);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (count >= 1) {

							sql = " select rate from PORCPDET  WHERE " + " PURC_ORDER = ?  and LINE_NO__ORD = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								purcRate = rs.getDouble(1);
							}

							System.out.println("Result of purcRate Rate : " + purcRate + "current Rate : " + rateStr);
							if (rate != purcRate) {
								System.out.println("Result of purcRate Rate1 : " + purcRate + "current Rate 1: "
										+ rateStr + " VTCHINRATE");
								errCode = "VTCHINRATE";// Changed In rate not applicable when PORCP is unconfirmed
								// errCode = "VTRCPLNEXS";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							// Pavan R on 14nov18 end
						}
						// Added By PriyankaC on 07JUNE18.[END]

						if (varValue != null && varValue.length() > 0) {
							rate1 = discommon.pickRate(varValue, ordDateStr, itemCode, "", "L", quantityStduom, conn);
							System.out.println("Rate in Pic rate " + rate1);
							if (rate > rate1 && rate1 > 0) {
								errCode = "VTDPCORT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						if (rate == rateO) {
							lineNoOrd = "    " + lineNoOrd;
							lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3, lineNoOrd.length());
							sql = "	select case when dlv_qty is null then 0 end from porddet "
									+ "where	purc_order = ? and line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								dlvQty = rs.getDouble(1);
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (!(dlvQty == 0)) {
								errCode = "VTRCPRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						System.out.println("Error Code" + errCode);
						// if( errCode == null || errCode.trim().length() == 0 )
						// {
						if (rate > 0) {
							indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
							discount = (genericUtility.getColumnValue("discount", dom));
							/*
							 * Added by Abhijit Gaikwad Request ID -D15GSUN005 FOR VALIDATE ENTERED Discount
							 * START
							 */
							System.out.println("Discount in dom: " + discount);
							dis = Double.parseDouble(discount);
							boolean falgporate = true;
							String pordTyperate = "";
							if (indNo != null && indNo.length() > 0) {
								// -----------------
								String typeallowporatelist = "";
								sql = "select pord_type from porder where purc_order = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									pordTyperate = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Pordtype.........." + pordTyperate);
								typeallowporatelist = discommon.getDisparams("999999", "TYPE_ALLOW_PURC_RATE", conn);
								System.out.println(">>>>>>>>>In   varValue for  type allow purchase rate : "
										+ typeallowporatelist);
								typeallowporatelist = typeallowporatelist.trim();
								if (typeallowporatelist != null && typeallowporatelist.trim().length() > 0
										&& typeallowporatelist != "NULLFOUND") {
									String typeallowporatelistArr[] = typeallowporatelist.split(",");
									ArrayList typeallowporate = new ArrayList<String>(
											Arrays.asList(typeallowporatelistArr));
									if (typeallowporate.contains(pordTyperate)) {
										falgporate = false;
										System.out.println("In Flag False " + falgporate);
									} else {
										falgporate = true;
										System.out.println("In Flag False " + falgporate);
									}
								}

								System.out.println("Rate is" + rate);
								System.out.println("falgporate" + falgporate);
								if (falgporate) {
									System.out.println("Enter If Flag COndition");
									if (dis > 0) {
										Disrate = (rate * dis) / 100;
										System.out.println("Discount Rate " + Disrate);
										rate = rate - Disrate;
										System.out.println("Rate discount condition is" + rate);
									}

									sql = " select case when purc_rate is null then 0 else purc_rate end ,"
											+ "case when max_rate is null then 0 else max_rate end "
											+ " from	 indent where  ind_no = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, indNo);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										purcRate = rs.getDouble(1);
										maxRate = rs.getDouble(2);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("Maximum rate:" + maxRate);
									System.out.println("rate>>>>>>>>>:" + rate);
									if (maxRate != 0)
										if (rate > maxRate) {
											errCode = "VTLESMXRT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
								}
								// Ended by Abhijit Gaikwad Request
								// ID-D15GSUN005
								/*
								 * if (( rate > maxRate ) || ( rate < purcRate )) {
								 * 
								 * errCode = "VNOTBETIND"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase()); }
								 */
							}
						}
						// }
					}

					if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						if (taxClass != null && taxClass.length() > 0) {
							errCode = isExist("taxclass", "tax_class", taxClass, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						if (taxChap != null && taxChap.length() > 0) {
							errCode = isExist("taxchap", "tax_chap", taxChap, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("tax_env")) {
						// taxEnv = genericUtility.getColumnValue("tax_env", dom);
						taxEnv = discommon.getParentColumnValue("tax_env", dom, "2");
						System.out.println("POAmd 2 TaxEnv:" + taxEnv + "]");
						amdDateStr = checkNull(genericUtility.getColumnValue("amd_date", dom1));
						if (taxEnv != null && taxEnv.length() > 0) {
							errCode = isExist("taxenv", "tax_env", taxEnv, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else // Pavan R 17sept19 start[to validate tax environment]
							{/*
								 * // //getCheckTaxEnvStatus sql =
								 * " select (case when status is null then 'A' else status end) " +
								 * "from   taxenv where  tax_env      =  ?  and    status_date  <= ? "; pstmt =
								 * conn.prepareStatement(sql); pstmt.setString(1, taxEnv); pstmt.setTimestamp(2,
								 * amdDate); rs = pstmt.executeQuery(); if (rs.next()) { status =
								 * rs.getString(1);
								 * 
								 * }
								 * 
								 * rs.close(); rs = null; pstmt.close(); pstmt = null; if
								 * ("C".equalsIgnoreCase(status)) { errCode = "VTTAXENVCL";
								 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); }
								 */
								if (amdDateStr != null && amdDateStr.length() > 0) {
									amdDate = Timestamp.valueOf(genericUtility.getValidDateString(amdDateStr,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}
								errCode = distCommon.getCheckTaxEnvStatus(taxEnv, amdDate, "P", conn);
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								// Pavan R 17sept19 end[to validate tax environment]
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("conv__qty_stduom")) {
						System.out.println("@@@@@ validation of conv__qty_stduom executed......");
						convQtyStduomStr = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						convQtyStduom = convQtyStduomStr == null ? 0 : Double.parseDouble(convQtyStduomStr);
						if (unitStd == null || unitStd.length() == 0) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							unit = setDescription("unit", "item", "item_code", itemCode, conn);
						}
						unit = unit == null ? "" : unit.trim();
						unitStd = unitStd == null ? "" : unitStd.trim();
						System.out.println("@@@@@3 unit[" + unit + "]::unitStd[" + unitStd + "]::convQtyStduom["
								+ convQtyStduom + "]");
						if ((unit.equalsIgnoreCase(unitStd)) && (convQtyStduom != 1)) {
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!(unit.equalsIgnoreCase(unitStd))) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(mitemcode, mval,
							// mval1, lc_conv,conn);
							errCode = gf_check_conv_fact(itemCode, unit, unitStd, convQtyStduom, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("conv__rtuom_stduom")) //
					{
						System.out.println("@@@@@ validation of conv__rtuom_stduom executed......");
						convRtuomStduomStr = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						convRtuomStduom = convRtuomStduomStr == null ? 0 : Double.parseDouble(convRtuomStduomStr);
						if (unitStd == null || unitStd.length() == 0) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							unit = setDescription("unit", "item", "item_code", itemCode, conn);
						}
						unitRate = unitRate == null ? "" : unitRate.trim();
						unitStd = unitStd == null ? "" : unitStd.trim();
						System.out.println("@@@@@1 unitRate[" + unitRate + "]::unitStd[" + unitStd
								+ "]::convRtuomStduom[" + convRtuomStduom + "]");
						if ((unitRate.equalsIgnoreCase(unitStd)) && (convRtuomStduom != 1)) {
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (!(unitRate.equalsIgnoreCase(unitStd))) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(mitemcode, mval,
							// mval1, lc_conv,conn);
							errCode = gf_check_conv_fact(itemCode, unitStd, unitRate, convRtuomStduom, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("quantity")) {
						// added by RITESH ON 27/02/14 FOR VALIDATE ENTERED QTY
						// START
						String quantityDlvStr = checkNull(genericUtility.getColumnValue("dlv_qty", dom));
						double quantityDlv = quantityDlvStr == null ? 0 : Double.parseDouble(quantityDlvStr);
						System.out.println("<dlv_qty > " + quantityDlv);
						// added by RITESH ON 27/02/14 FOR VALIDATE ENTERED QTY
						// END

						quantityStr = checkNull(genericUtility.getColumnValue("quantity", dom));
						System.out.println("@@@@@@@@@@@quantityStr [" + quantityStr + "]");
						quantity = quantityStr == null ? 0 : Double.parseDouble(quantityStr);
						indNo = checkNull(genericUtility.getColumnValue("ind_no", dom));
						preQty = 0;
						recCnt = 0;
						lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						curlineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						System.out.println("@@@@@@@@@@@quantity [" + quantity + "]");
						if (quantity < 0) {
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							if (indNo != null && indNo.length() > 0 && (!"null".equalsIgnoreCase(indNo))) {
								sql = " select (case when quantity__stduom is null then 0 else quantity__stduom end) "
										+ "	from indent where ind_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, indNo);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									indQty = rs.getDouble(1);
									// recCnt++;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (indQty >= 0) {
									NodeList detail2ListTemp = dom2.getElementsByTagName("Detail2");
									NodeList detail1ListTemp = dom2.getElementsByTagName("Detail1");
									ArrayList linenoords = new ArrayList();
									noOfParent = detail2ListTemp.getLength();
									System.out.println("@@@@@@noOfParent[" + noOfParent + "]");
									for (int k = 0; k < noOfParent; k++) {
										tempindno = genericUtility.getColumnValueFromNode("ind_no",
												detail2ListTemp.item(k));
										System.out.println("@@@@@@tempindno[" + tempindno + "]");
										templinenoord = genericUtility.getColumnValueFromNode("line_no__ord",
												detail2ListTemp.item(k));
										System.out.println("@@@@@@templinenoord[" + templinenoord + "]");
										templineno = genericUtility.getColumnValueFromNode("line_no",
												detail2ListTemp.item(k));
										System.out.println("@@@@@@templineno[" + templineno + "]");

										System.out.println("@@@@@tempindno[" + tempindno + "]::templinenoord["
												+ templinenoord + "]::templineno[" + templineno + "]");
										// if indent no is empty then skip the
										// row
										if (templinenoord != null && templinenoord.length() > 0) {
											linenoords.add(linenoords.size(), templinenoord);
										}

										if (indNo.equalsIgnoreCase(templineno)) {
											qtybrowStr = genericUtility.getColumnValueFromNode("quantity__stduom",
													detail2ListTemp.item(ctr1));
											qtybrow = qtybrowStr == null ? 0 : Double.parseDouble(qtybrowStr);
											quantity = quantity + qtybrow;
										}
									}
									recCnt = 0;
									sql = "select sum(quantity__stduom) from porddet where purc_order = '" + purcOrder
											+ "' ";
									// lnNoStr = "'";
									if (linenoords.size() > 0) {
										for (int k = 0; k < linenoords.size(); k++) {
											lnNoStr = lnNoStr + "'" + linenoords.get(k) + "',";
										}
										lnNoStr = lnNoStr.substring(0, lnNoStr.length() - 1);
										sql = sql + " and line_no not in (" + lnNoStr + ") ";
									}
									if (indNo != null) {
										sql = sql + " and ind_no = '" + indNo + "'";
									}
									pstmt = conn.prepareStatement(sql);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										ordQty = rs.getDouble(1);
										recCnt++;
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if (recCnt >= 0) {
										sql = "	select sum(porddet.quantity__stduom)  " + " from porddet, porder "
												+ " where (porddet.purc_order = porder.purc_order ) and "
												+ " (((case when porder.status is null then 'O' else porder.status end) <> 'X' ) "
												+ " and ((case when porder.confirmed is null then 'N' else porder.confirmed end) <> 'Y'"
												+ " and (case when porder.status is null then 'O' else porder.status end) <> 'C') )  "
												+ " and (porddet.ind_no = ? ) and ((porddet.purc_order <> ? ) ) ";

										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, indNo);
										pstmt.setString(2, purcOrder);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											preQty = rs.getDouble(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

									}

									if (quantity + ordQty + preQty > indQty) {
										errCode = "VTPIQTY1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}

								}
							} // indent number is not expty
						} // quantity < 0
							// added by RITESH ON 27/02/14 FOR VALIDATE ENTERED
							// QTY
							// START
						if (quantity < quantityDlv) {
							errCode = "VTQTYDLV1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} // added by RITESH ON 27/02/14 FOR VALIDATE ENTERED
							// QTY END
					}

					if (childNodeName.equalsIgnoreCase("quantity__stduom")) //
					{
						quantityStduomStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));
						// convRtuomStduom = convRtuomStduomStr== null?
						// 0:Double.parseDouble(convRtuomStduomStr);

						if ("-999999999".equalsIgnoreCase(quantityStduomStr)) {
							errCode = "VTPOQTY3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("rate__stduom")) //
					{
						rateStduomStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));
						if ("-999999999".equalsIgnoreCase(rateStduomStr)) {
							errCode = "VTPORATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("emp_code__qcaprv")) //
					{
						empCodeQcaprv = checkNull(genericUtility.getColumnValue("emp_code__qcaprv", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						qcReqd = setDescription("qc_reqd", "item", "item_code", itemCode, conn);
						if ("Y".equalsIgnoreCase(qcReqd)) {
							errCode = isExist("employee", "emp_code", empCodeQcaprv, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VMEMP1";
							} else {
								// Done changes by sarita as [errCode setting 'TRUE' in isExist method if
								// employee exists and below(VERREMPL) error is getting] on 04 SEP 2018
								errCode = "";
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
									errCode = "VMEMP7";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} else if ("Y".equalsIgnoreCase(withHeld)) {
									errCode = "VMEMP9";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if (errCode != null && errCode.trim().length() > 0) {
								errCode = "VERREMPL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("duty_paid")) {
						dutyPaid = checkNull(genericUtility.getColumnValue("duty_paid", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						sql = "Select case when eou is null then 'N' else eou end  From site Where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							eou = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ("Y".equalsIgnoreCase(eou)) {
							if (dutyPaid == null || dutyPaid.length() == 0) {
								errCode = "VTDUTYBK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if ("N".equalsIgnoreCase(dutyPaid)) {
								itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
								ordDateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
								sql = " Select b.lop_reqd From item a, itemser b "
										+ " Where a.item_ser = b.item_ser And a.item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lopReqd = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (ordDateStr != null) {
									ordDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDateStr,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}

								if ("Y".equalsIgnoreCase(lopReqd)) {
									sql = " Select count(1) From lop_hdr a, lop_det b "
											+ "	Where a.lop_ref_no = b.lop_ref_no And a.site_code = ? And a.confirmed = 'Y' And "
											+ " b.item_code = ? And b.item_status ='A' And  ? >= a.valid_from And "
											+ " ? <= a.valid_to And b.buy_sell_flag In ('P','B') ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, siteCode);
									pstmt.setString(2, itemCode);
									pstmt.setTimestamp(3, ordDate);
									pstmt.setTimestamp(4, ordDate);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if (cnt == 0) {
										errCode = "VTLOPITEM1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					} // /end

					if (childNodeName.equalsIgnoreCase("form_no")) //
					{
						formNo = checkNull(genericUtility.getColumnValue("form_no", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						sql = "Select case when eou is null then 'N' else eou end  From site Where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							eou = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("Y".equalsIgnoreCase(eou)) {
							ordDateStr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
							quantityStr = checkNull(genericUtility.getColumnValue("quantity", dom));
							quantityO = checkNull(genericUtility.getColumnValue("quantity__o", dom));
							dutyPaid = checkNull(genericUtility.getColumnValue("duty_paid", dom));
							dutyPaidO = checkNull(genericUtility.getColumnValue("duty_paid__o", dom));
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
							lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							formNoO = checkNull(genericUtility.getColumnValue("form_no__o", dom));
							suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
							amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
							quantity = quantityStr == null ? 0 : Double.parseDouble(quantityStr);
							if (lineNoOrd == null) {
								lineNoOrd = "   ";
							} else {
								lineNoOrd = "    " + lineNoOrd;
								lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3, lineNoOrd.length());
							}

							// If New Item is Inserted than check the form Qty
							sql = " select count(1) from  porddet where purc_order = ? and  line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt > 0) {
								if (!(quantity == quantity)) {
									errCode = "VTPOAMDQTY"; // CT3 Quantity amend not allowed!
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} else if (!(dutyPaidO == dutyPaid) || !(formNoO == formNo)) {
									errCode = "VTPOAMDDUT"; // Not allow to change Duty paid and Form No
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else if (cnt == 0) // Means New Order Line
							{
								if ("Y".equalsIgnoreCase(dutyPaid) && (formNo != null && formNo.length() > 0)) {
									errCode = "VTCT3DUTY"; // Has to be Blank
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} else if ("N".equalsIgnoreCase(dutyPaid) && (formNo != null && formNo.length() > 0)) {

									sql = " select sum(case when b.quantity is null then 0 else b.quantity end) - sum(case when b.dlv_qty is null then 0 else b.dlv_qty end) "
											+ " from porder a, porddet b where a.purc_order = b.purc_order and	b.form_no = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, formNo);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										preQty = rs.getDouble(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									NodeList detail2List = dom2.getElementsByTagName("Detail2");
									NodeList detail1List = dom2.getElementsByTagName("Detail1");
									noOfParent = detail2List.getLength();
									for (ctr = 1; ctr <= noOfParent; ctr++) {
										System.out.println("noOfParent@@@@@@@@@@@@" + noOfParent);
										lineNoBrow = genericUtility.getColumnValueFromNode("line_no__ord",
												detail2List.item(ctr1));
										amdNoTemp = genericUtility.getColumnValueFromNode("amd_no",
												detail2List.item(ctr1));
										formNoTemp = genericUtility.getColumnValueFromNode("form_no",
												detail2List.item(ctr1));
										lineNoTemp = genericUtility.getColumnValueFromNode("line_no",
												detail2List.item(ctr1));
										if (amdNo.equalsIgnoreCase(amdNoTemp) && formNo.equalsIgnoreCase(formNoTemp)
												&& (lineNoBrow == null || lineNoBrow.length() == 0)
												&& (!(lineNo == lineNoTemp))) {
											quantitybrow = genericUtility.getColumnValueFromNode("quantity",
													detail2List.item(ctr1));
											qtybrow = quantitybrow == null ? 0 : Double.parseDouble(quantitybrow);
											totQty = totalQuantity == null ? 0 : Double.parseDouble(totalQuantity);
											totQty = totQty + qtybrow;
										}
									}

									recCnt = 0;
									sql = "	Select  sum(case when b.quantity is null then 0 else b.quantity end),"
											+ " sum(case when b.qty_used is null then 0 else b.qty_used end) "
											+ " From ct3form_hdr a , ct3form_det b Where a.form_no = b.form_no "
											+ " And a.form_no = ? " + " And a.site_code = ? " + " And b.supp_code = ? "
											+ " And b.item_code = ? " + " And ? >= a.eff_from "
											+ " And ? <= a.valid_upto " + " And b.purc_order = ? "
											+ " And a.status = 'O' "
											+ " And case when a.confirmed is null then 'N' else a.confirmed end = 'Y'";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, formNo);
									pstmt.setString(2, siteCode);
									pstmt.setString(3, suppCode);
									pstmt.setString(4, itemCode);
									pstmt.setTimestamp(5, ordDate);
									pstmt.setTimestamp(6, ordDate);
									pstmt.setString(7, purcOrder);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										recCnt++;
										ct3Qty = rs.getDouble(1);
										qtyUsed = rs.getDouble(2);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt == 0) {
										errCode = "VTCT3FORM1"; // Invalid CT3 Form no
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									} else if ((preQty + totQty + quantity) > (ct3Qty - qtyUsed)) {
										errCode = "VTCT3QTY"; // Quantity exceeds the balance quantity of CT3 Form
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								} // End If //fORM NO
							}
						}
					} // end form_no
				} // end for
				break; // case 1 end

			case 3:
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("line_no_ord")) {
						lineNoOrd = checkNull(genericUtility.getColumnValue("line_no_ord", dom));
						System.out.println("@@@@@1lineNoOrd[" + lineNoOrd + "]");
						if (lineNoOrd != null && lineNoOrd.length() > 0) {
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
							System.out.println("@@@@@2purcOrder[" + purcOrder + "]");
							sql = " select count(1) from pord_term where purc_order = ? and line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) {
								errCode = "VTNOLINE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}

					if (childNodeName.equalsIgnoreCase("term_code")) {
						termCode = checkNull(genericUtility.getColumnValue("term_code", dom));
						System.out.println("@@@@@3termCode[" + termCode + "]");
						errCode = isExist("pur_term", "term_code", termCode, conn);
						if ("FALSE".equalsIgnoreCase(empCode)) {
							errCode = "VTTERM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							lineNoOrd = checkNull(genericUtility.getColumnValue("line_no_ord", dom));
							System.out.println("@@@@@4lineNoOrd[" + lineNoOrd + "]");
							if (lineNoOrd == null || lineNoOrd.length() == 0) {
								purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
								System.out.println("@@@@@5purcOrder[" + purcOrder + "]");
								sql = "select count(1) from pord_term where purc_order = ? and term_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								pstmt.setString(2, termCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt > 0) {
									errCode = "VTDUPTERM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					} //

				} // end for case 3
				break; // case 3 end

			case 4:
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("ChildNOde" + childNodeName);
					if (childNodeName.equalsIgnoreCase("line_no__ord")) {
						int cnt11 = 0, cnt12 = 0;
						String lineOrder = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						System.out.println("Line no ord is======" + lineord1);
						System.out.println("Pur order is:" + purcOrder);
						sql = "select line_no from pord_pay_term where purc_order= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							linepayterm = rs.getString("line_no");
							System.out.println("Purchase Order Pay Term:" + linepayterm);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select line_no__ord from pur_milstn where purc_order= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							linepurmil = rs.getString("line_no__ord");
							System.out.println("purchase milestone:" + linepurmil);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (linepayterm.equalsIgnoreCase(lineOrder) && linepurmil.equalsIgnoreCase(lineOrder)) {
							System.out.println("ENter the if condition");
							errCode = "VTPOAMPTRM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					} else if (childNodeName.equalsIgnoreCase("line_no__prev")) {

						System.out.println("Inside line_no__prev");
						linnoprev = checkNull(genericUtility.getColumnValue("line_no__prev", dom));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom).trim());
						String lineOrder1 = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						System.out.println("Line no order is:" + lineOrder1);
						System.out.println("Amend Line NO Prev is:" + linnoprev);
						if (linnoprev != null && linnoprev.trim().length() > 0 && !linnoprev.equals("null")) {
							sql = "select line_no from pord_pay_term where purc_order= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								String linenoprev = rs.getString("line_no");
								System.out.println("Po Pay term line no is :" + linenoprev);
								if (linenoprev.equalsIgnoreCase(linnoprev)) {
									isLine = true;
									break;
								}
							}
							if (isLine == false || linnoprev.equalsIgnoreCase(lineOrder1)) {
								System.out.println("Inside Errrr");
								errCode = "VTPRVLINE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								rs.close();
							}
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						System.out.println("Amend@@@@@ Line NO Prev is:" + linnoprev);
					} else if (childNodeName.equalsIgnoreCase("rel_amt")) {

						String rel_amount = checkNull(genericUtility.getColumnValue("rel_amt", dom));
						rel_amount = rel_amount != null && rel_amount.trim().length() > 0 ? rel_amount.trim() : "0";
						rel_amt = Double.parseDouble(rel_amount);

						System.out.println("<rel_amt > " + rel_amt);
						if (rel_amt == 0 || rel_amt <= 0) {
							errCode = "VTAMOUNT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						NodeList detail4List = dom.getElementsByTagName("Detail4");
						noOfParent = detail4List.getLength();
						String ordamt1 = checkNull(genericUtility.getColumnValue("ord_amt", dom1));
						System.out.println("<ord_amt >@@@@@@ " + ordamt1);
						ordamt1 = ordamt1 != null && ordamt1.trim().length() > 0 ? ordamt1.trim() : "0";
						ord_amt = Double.parseDouble(ordamt1);
						System.out.println("<ord_amt > " + ord_amt);
						String totamt1 = checkNull(genericUtility.getColumnValue("tot_amt", dom1));
						totamt1 = totamt1 != null && totamt1.trim().length() > 0 ? totamt1.trim() : "0";
						System.out.println("<tot_amt > @@@@@@@@@@@@@@@@@" + totamt1);
						System.out.println("Total Amount" + totamt1);
						tot_amt = Double.parseDouble(totamt1);
						System.out.println("<tot_amt > " + tot_amt);
						if ((lineNoBrow == null || lineNoBrow.length() == 0) && (!(lineNo == lineNoTemp))) {
							String lineno = checkNull(
									genericUtility.getColumnValueFromNode("line_no", detail4List.item(ctr1)));
							// lineno = lineno == null ? "0" : lineno.trim();
							lineno = lineno != null && lineno.trim().length() > 0 ? lineno.trim() : "0";

							lineNoo = Integer.parseInt(lineno);
							System.out.println("Line Number" + lineNoo);
							// line_no
							// =Integer.parseInt(genericUtility.getColumnValue("line_no",
							// dom));
							System.out.println("Line Number" + lineno);
						}
						System.out.println("noOfParent@@@@@@@@@@@" + noOfParent);
						a = 0;
						for (a = 1; a <= noOfParent; a++) {

							System.out.println("noOfParent@@@@@@@@@@@" + noOfParent);
							System.out.println("a@@@@ value" + a);
							System.out.println("ctr1@@@@@@@@" + ctr1);
							type = genericUtility.getColumnValueFromNode("amt_type", detail4List.item(ctr1));
							retval = Double.toString(rel_amt);
							retval = genericUtility.getColumnValueFromNode("rel_amt", detail4List.item(ctr1));
							System.out.println("Amount " + amount);
							System.out.println("OrderAmount " + ord_amt);
							System.out.println("TotalAmount " + tot_amt);
							if (a != lineNoo) {
								if (type.equalsIgnoreCase("01"))

								{
									adv_amt = ord_amt * (rel_amt / 100);
								} else if (type.equalsIgnoreCase("02")) {
									adv_amt = tot_amt * (rel_amt / 100);
								} else if (type.equalsIgnoreCase("03")) {
									adv_amt = rel_amt;
								}
								System.out.println("Amount%%%%%%%" + amount);
								amount = amount + adv_amt;
								System.out.println("Amount  allllll@@@@@@@@@ " + adv_amt);
								System.out.println("Amount  allllll@@@@@@@@@ " + amount);

							}
						}

						type = genericUtility.getColumnValueFromNode("amt_type", dom);
						retval = genericUtility.getColumnValueFromNode("rel_amt", dom);
						type = checkNull(genericUtility.getColumnValue("amt_type", dom));
						System.out.println("Amount%%%%%%%" + amount);
						System.out.println("Amount%%%%%%%" + tot_amt);
						if (amount > tot_amt) {
							errCode = "POADVMIS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} else if (childNodeName.equalsIgnoreCase("task_code")) {
						rel_agnst = genericUtility.getColumnValue("rel_agnst", dom);
						task_code = checkNull(genericUtility.getColumnValue("task_code", dom));
						System.out.println("Task code is" + task_code);
						if (rel_agnst.equalsIgnoreCase("05")) {
							System.out.println("Enter the Project task ");

							if (task_code == null || task_code.trim().length() == 0) {
								System.out.println("Project Task is null");
								errCode = "VTNULTASK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							} else if (task_code != null && task_code.trim().length() > 0) {
								sql = "select count(*)  from proj_task where task_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, task_code.trim());
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									errCode = "VTTASK";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}

						else if (rel_agnst.equalsIgnoreCase("06")) {
							System.out.println("Enter the user Defined Milestone");

							if (task_code == null || task_code.trim().length() == 0) {
								System.out.println("user Defined Milestone is nulll");
								errCode = "VTNULTASK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							} else if (task_code != null && task_code.trim().length() > 0) {
								sql = "select count(*) from gencodes where fld_Name = 'TASK_CODE' AND fld_value = ? AND mod_name = 'W_PORDER'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, task_code.trim());
								rs = pstmt.executeQuery();
								if (rs.next()) {

									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0) {
									errCode = "VTTASKCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					} else if (childNodeName.equalsIgnoreCase("task_code__parent")) {
						rel_agnst = genericUtility.getColumnValue("rel_agnst", dom);
						taskcodeparent = checkNull(genericUtility.getColumnValue("task_code__parent", dom));
						if (rel_agnst.equalsIgnoreCase("05")) {
							System.out.println("Task codeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee1111");
							System.out.println("Task codeeeeeeeeeeeee Parent is" + taskcodeparent);
							if (taskcodeparent != null && taskcodeparent.trim().length() > 0) {
								sql = "select count(*)  from proj_task where task_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taskcodeparent.trim());
								System.out.println("Task parent trim is" + taskcodeparent.trim());
								rs = pstmt.executeQuery();
								if (rs.next()) {
									count11 = rs.getInt(1);
									System.out.println("@@@@@@@@Count is:@@@@@@" + count11);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (count11 == 0) {
									errCode = "VTPTASK";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						} else if (rel_agnst.equalsIgnoreCase("06")) {

							if (taskcodeparent != null && taskcodeparent.trim().length() > 0) {
								sql = "select count(*) from gencodes where fld_Name = 'TASK_CODE' AND fld_value = ? AND mod_name = 'W_PORDER'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taskcodeparent.trim());
								rs = pstmt.executeQuery();
								if (rs.next()) {
									count21 = rs.getInt(1);
									System.out.println("#######Count is:#####" + count21);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (count21 == 0) {
									errCode = "VTPTASKCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					} else if (childNodeName.equalsIgnoreCase("site_code__adv")) {
						siteCodept = checkNull(genericUtility.getColumnValue("site_code__adv", dom));
						System.out.println("@@@@ siteCode[" + siteCodept + "]");
						if (siteCodept != null && siteCodept.trim().length() > 0) {
							errCode = this.isSiteCode(siteCodept, modName);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChappt = checkNull(genericUtility.getColumnValue("tax_chap", dom));
						System.out.println("@@@@ tax_chap[" + taxChappt + "]");
						if (taxChappt != null && taxChappt.length() > 0) {
							errCode = isExist("taxchap", "tax_chap", taxChappt, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClasspt = checkNull(genericUtility.getColumnValue("tax_class", dom));
						System.out.println("############ tax_class[" + taxClasspt + "]");
						if (taxClasspt != null && taxClasspt.length() > 0) {
							errCode = isExist("taxclass", "tax_class", taxClasspt, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("tax_env")) {
						/*
						 * taxEnvpt = checkNull(genericUtility.getColumnValue( "tax_env", dom));
						 */
						taxEnvpt = discommon.getParentColumnValue("tax_env", dom, "4");
						System.out.println("POAmd 4 TaxEnv:" + taxEnv + "]");

						amdDateStr = genericUtility.getColumnValue("amd_date", dom1);
						System.out.println("##############tax_env[" + taxEnvpt + "]");
						System.out.println("##############Amd Date is:[" + taxEnvpt + "]");
						if (taxEnvpt != null && taxEnvpt.length() > 0) {
							errCode = isExist("taxenv", "tax_env", taxEnvpt, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) {
								errCode = "VTTAXENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								// Pavan R 17sept19 start[to validate tax environment]
								/*
								 * errCode = fincommon.checkTaxEnvStatus(taxEnvpt, amdDate, conn);
								 */
								if (amdDateStr != null && amdDateStr.length() > 0) {
									amdDate = Timestamp.valueOf(genericUtility.getValidDateString(amdDateStr,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}
								errCode = distCommon.getCheckTaxEnvStatus(taxEnvpt, amdDate, "P", conn);
								// Pavan R 17sept19 end[to validate tax environment]
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
				}
				break;

			case 5:
				parentNodeList = dom.getElementsByTagName("Detail5");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("min_day")) {
						System.out.println("Minnimum day");

						val = genericUtility.getColumnValue("purc_order", dom);
						val1 = genericUtility.getColumnValue("line_no", dom);
						String min_day = genericUtility.getColumnValue("min_day", dom);
						min_day = min_day == null ? "0" : min_day.trim();
						mmin_day = Double.parseDouble(min_day);
						String max_day = genericUtility.getColumnValue("max_day", dom);
						max_day = max_day == null ? "0" : max_day.trim();
						mmax_day = Double.parseDouble(max_day);
						String min_amt = genericUtility.getColumnValue("min_amt", dom);
						min_amt = min_amt == null ? "0" : min_amt.trim();
						mmin_amt = Double.parseDouble(min_amt);
						String max_amt = genericUtility.getColumnValue("max_amt", dom);
						max_amt = max_amt == null ? "0" : max_amt.trim();
						mmax_amt = Double.parseDouble(max_amt);
						System.out.println("Maximum day" + mmax_day);
						System.out.println("Minnimum day" + mmin_day);

						if (mmax_day < mmin_day) {
							System.out.println("test in max if::::::::::::::::::::::::::");
							errCode = "VMMINDAY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							System.out.println("test in max else::::::::::::::::::::::::::");
							sql = "select count(*) from pord_dlv_term where purc_order = ? and ? between min_day and max_day  and line_no !=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, val);
							pstmt.setDouble(2, mmin_day);
							pstmt.setString(3, val1);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt > 0) {
								errCode = "VTDLTERM2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					} else if (childNodeName.equalsIgnoreCase("min_amt")) {
						String min_amt1 = genericUtility.getColumnValue("min_amt", dom);
						min_amt1 = min_amt1 == null ? "0" : min_amt1.trim();
						mmin_amt = Double.parseDouble(min_amt1);
						String max_amt1 = genericUtility.getColumnValue("max_amt", dom);
						max_amt1 = max_amt1 == null ? "0" : max_amt1.trim();
						mmax_amt = Double.parseDouble(max_amt1);
						System.out.println("Maximum amount @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + mmax_amt);
						System.out.println("Minnimum amount@@@@@@@@@@@@@@@@@@@@@@@@@@" + mmin_amt);
						if (mmin_amt < 0) {
							System.out.println("1111111@@@@@@@@@@@@@@@@@@@@@@@@@@222222");
							errCode = "VMMINAMT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (mmax_amt < mmin_amt) {
							System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@");
							errCode = "VMMINAMT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}

					}

				}

			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					// TODO
					if ("VTPROJCOST".equalsIgnoreCase(errCode)) {
						startStr = errString.substring(0, errString.indexOf("<description>") + 13);
						endStr = errString.substring(errString.indexOf("</description>"), errString.length());
						descrStr = errString.substring(errString.indexOf("<description>") + 13,
								errString.indexOf("</description>"));
						descrStart = descrStr.substring(0, descrStr.indexOf("]"));
						descrEnd = descrStr.substring(descrStr.indexOf("]"), descrStr.length());
						System.out.println("<approxCost IN ERROR MESSAGE> @@@@@@@@@@@@@@@@@" + approxCost);
						System.out.println(
								"<totAmtDetpordertot IN ERROR MESSAGE > @@@@@@@@@@@@@@@@@" + totAmtDetpordertot);
						System.out.println("<excedAmt @@@@@@@@@@@@@@@@@" + excedAmt);
						System.out.println("<totAmtDetporder @@@@@@@@@@@@@@@@@" + totAmtDetporder);
						System.out.println(
								"<sumQtyamd + totAmtDetporder - approxCost IN ERROR MESSAGE > @@@@@@@@@@@@@@@@@"
										+ sumQtyamd + totAmtDetporder);
						System.out.println("<sumQtyamd + totAmtDetpordertot IN ERROR MESSAGE > @@@@@@@@@@@@@@@@@"
								+ totAmtDetPoamdDetbrow + totAmtDetporder);
						value = " ;    Amount Exceeded  Project Code :      " + projCode
								+ ";Project Aprroved Amount :  " + BigDecimal.valueOf(approxCost).toPlainString()
								// +" ;Consumed Amount : "+
								// BigDecimal.valueOf(totAmtDetpordertot).toPlainString()
								+ " ;Consumed Amount :  " + BigDecimal.valueOf(totAmtProj).toPlainString()
								// totAmtProj
								// +" ; Current Purchase Amendment Amount : "+
								// BigDecimal.valueOf(totAmtDetPoamdDetbrow + totAmtDetporder ).toPlainString()
								+ " ;   Current Purchase Amendment Amount :  "
								+ BigDecimal.valueOf(totAmtDetPoamdDetbrow).toPlainString()
								// +" ; Exceeded Amount : "+ BigDecimal.valueOf( sumQtyamd + totAmtDetpordertot
								// - approxCost).toPlainString();
								// +" ; Exceeded Amount : "+ BigDecimal.valueOf(totAmtDetpordertot -
								// approxCost).toPlainString();
								// if( excedAmt > 0.0)
								// {
								+ " ; Exceeded Amount :  "
								+ BigDecimal.valueOf(totAmtProj - approxCost).toPlainString();
						// }
						System.out.println("Value ::: " + value);
						descrStart = descrStart.concat(value).concat(descrEnd);
						errString = startStr.concat(descrStart).concat(endStr);
					}
					if (errString.length() > 0) {
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
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}// end of validation

	private String gf_check_conv_fact(String itemCode, String unitfrom, String unitto, Double convfact, Connection conn)
			throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int recCnt = 0;
		String errCode = "", variencetype = "", order = "NOTFOUND", sql = "";
		double varience = 0, mastfact = 0;

		System.out.println("@@@@@2 unitfrom[" + unitfrom + "]::unitto[" + unitto + "]::convfact[" + convfact + "]");
		if (unitfrom.equalsIgnoreCase(unitto) && (!(convfact == 1))) {
			errCode = "VTUCON1";
			return errCode;
		}

		sql = " select fact, varience_type, varience_value " + " from uomconv  where ( uomconv.unit__fr = ? ) and"
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
			sql = " select fact, varience_type, varience_value "
					+ " from uomconv  where ( uomconv.unit__fr = ? ) and ( uomconv.unit__to = ? ) "
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
			errCode = "VTUOMCONV";
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
				errCode = "VTUOMVAR";
			}
		}

		return errCode;

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

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext,
			String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("itemChanged() called for POrderAmdIC");
		String valueXmlString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [POrderAmdIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException {
		String childNodeName = null;
		String sql = "", purcOrder = "", amdNo = "", invAcctQc = "", invAcct = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0, cnt1 = 0, cnt = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();

		String designCode = "", fname = "", mname = "", lname = "", colname = "", code = "", descr = "", descr1 = "",
				crTerm = "", crdescr = "";
		String commPercOn = "", ccode = "", empCode = "", descr2 = "", descr3 = "", descr4 = "";
		String po = "", pordType = "", supp = "", siteCodeDlv = "", siteCodeOrd = "", siteCodeBill = "", status = "",
				statusdt = "";
		String deptCode = "", orderDb = "", itemSer = "", taxOpt = "", amddt = "";
		String currCode = "", taxChap = "", taxClass = "", taxEnv = "", remarks = "", projCode = "", salesPers = "",
				common = "", curr_comm = "";
		String quotNo = "", confirmed = "", tranCode = "", currCodeFrt = "", frtTerm = "", dlvTerm = "",
				currCodeIns = "", purc_order = "", empCodeAprv = "";
		String empname = "", dlvqty = "", amd_no = "";

		double Num = 0, perc = 0, ordAmt = 0, taxAmt = 0, totAmt = 0, commPerc = 0, qty_stduom = 0, discount = 0,
				frtAmt = 0, insuranceAmt = 0, qty = 0;
		double pendqty = 0, conv_qty_stduom = 0, conv_rtuom_stduom = 0, convtemp = 0, exchRate = 0, clgRate = 0,
				rate_stduom = 0, rate = 0;

		String itemcode = "", loc = "", Val = "", Val1 = "", val2 = "", acct = "", cctr = "", specialInstr = "",
				form_no = "";
		String duty_paid = "", site = "", indNo = "", item = "", unit = "", rem = "", work = "", locCode = "",
				unit_std = "";
		String pack = "", packInstr = "", unitrate = "", acct_dr = "", cctr_dr = "", acct_cr = "", cctr_cr = "",
				disc_type = "";
		String qc_name = "", qc_name_o = "", packinstr = "", unitPur = "", empCodeQcAprv = "", pack1 = "",
				packinstr1 = "";
		String benefit_type = "", suppCode = "", siteCode = "", stationFr = "", stationTo = "", supp_code_mnfr = "";
		String specific_instr = "", ld_no_art = "", term = "", pono = "", term_code_o = "", retval = "",
				licence_no = "", eou = "";
		String currCodeComm = "", analCode = "";
		Timestamp reqdate = null, reqdt = null, dlvdt = null, refDate = null, ordDate = null, taxdt = null,
				confdt = null;
		Timestamp statusDate = null, taxDate = null, confDate = null;
		String suppName = "", suppAdd1 = "", suppAdd2 = "", suppCity = "", suppStanCode = "", empCodePur = "";
		String siteDescr = "", siteAdd1 = "", siteAdd2 = "", siteCity = "", siteStanCode = "", suppCodeO = "",
				lineNoOrd = "";
		int currentFormNo = 0;
		String columnValue = "", chgUser = "", chgTerm = "", itemCode = "", itemDescr = "", fSysDate = "",
				channelPartner = "";
		// /// detail
		double quantity = 0, dlvQty = 0, quantityStduom = 0, noArt = 0, rateClg = 0;
		Timestamp dlvDate = null, reqDate = null, duedate = null;
		String WorkOrder = "", packCode = "", discountType = "", specificInstr = "", benefitType = "", licenceNo = "",
				formNo = "", dutyPaid = "";
		DistCommon disscommon = new DistCommon();
		FinCommon fincommon = new FinCommon();
		String unitRate = "", unitStd = "", termCode = "", termDescr = "", termCodeO = "", inputNos = "", printOpt = "";
		// ldt_ref
		ArrayList qty_stduomList = null, rate_stduomList = null, quantityList = null, quantityStduomList = null;
		// long ll_lineno ="", ll_row ="", ll_row1
		String frtType = "";
		double frtRate = 0, frtAmtQty = 0, frtAmtFixed = 0;
		// SimpleDateFormat sdf = new
		// SimpleDateFormat(genericUtility.getApplDateFormat());
		String frtRateStr = "", frtAmtQtyStr = "", frtAmtFixedStr = "", conv_rtuom_stduomStr = "";
		String ordDateHdr = "";
		double lastPurcRate = 0d;
		String lastPurcPo = "";
		long line_no = 0;
		String value = "", value1 = "", task_code = "", amdno = "", sitecodeadv = "";
		double relamt = 0, relafter = 0, adjustmentperc = 0;
		String adjmet = "", acctcode = "", cctrcode = "", relagnst = "", amttype = "";
		String type = "", refcode = "", fchgtype = "";
		double retperc = 0, minday = 0, maxday = 0, finchg = 0, minamt = 0, maxamt = 0;
		String linenoPrev = "";
		String allowover = "";
		String taskcodeParent = "";
		double apprvlead = 0;
		String remark = "";
		String analcode = "", cctrcode1 = "", acctcode1 = "", deptcode = "", sitecode = "";
		String BudgetStr = "", CONSStr = "", totStr = "";
		String refDateStr = "";
		String dlvDateStr = "";
		String reqDateStr = "";
		String taxDateStr = "";
		String ordDateStr = "";
		String contractNo = "", invAcctSer = "";
		double deliveryQty = 0.0;
		String dimension="";
		try {
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDate);

			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo + "**************");
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					System.out.println("@@@@@@@@ itm_default called @@@@@@@@");
					valueXmlString.append("<amd_date>").append("<![CDATA[" + sysDate + "]]>").append("</amd_date>");
					valueXmlString.append("<conf_date>").append("<![CDATA[" + sysDate + "]]>").append("</conf_date>");
					valueXmlString.append("<tax_date>").append("<![CDATA[" + sysDate + "]]>").append("</tax_date>");

					valueXmlString.append("<frt_type protect = \"0\">").append("<![CDATA[" + "Q" + "]]>")
							.append("</frt_type>");
					valueXmlString.append("<frt_amt__fixed protect = \"1\">").append("<![CDATA[" + "0" + "]]>")
							.append("</frt_amt__fixed>");
					valueXmlString.append("<frt_rate protect = \"0\">").append("<![CDATA[" + "0" + "]]>")
							.append("</frt_rate>");
					valueXmlString.append("<frt_amt__qty protect = \"1\">").append("<![CDATA[" + "0" + "]]>")
							.append("</frt_amt__qty>");

				} else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) {
					System.out.println("@@@@@@@@ itm_defaultedit called @@@@@@@@");

					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
					amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));

					sql = "select count(1) from poamd_det where amd_no = ? and purc_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, amdNo);
					pstmt.setString(2, purcOrder);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = " select count(1) from poamd_term where  purc_order = ? and amd_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					pstmt.setString(2, amdNo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt1 = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("@@@@@@ cnt[" + cnt + "]:::: cnt1[" + cnt1 + "]");
					if (cnt > 0 || cnt1 > 0) { // 1-- protect
						valueXmlString.append("<purc_order protect = \"1\" >").append("<![CDATA[" + purcOrder + "]]>")
								.append("</purc_order>");
					} else {
						valueXmlString.append("<purc_order protect = \"0\" >").append("<![CDATA[" + purcOrder + "]]>")
								.append("</purc_order>");
					}

					// / for item change frt_type

					System.out.println("@@@@@@@@ item change frt_type " + currentColumn);
					frtType = checkNull(genericUtility.getColumnValue("frt_type", dom));
					frtRateStr = checkNull(genericUtility.getColumnValue("frt_rate", dom));
					frtAmtQtyStr = checkNull(genericUtility.getColumnValue("frt_amt__qty", dom));
					frtAmtFixedStr = checkNull(genericUtility.getColumnValue("frt_amt__fixed", dom));
					System.out.println("frtType[" + frtType + "]:::frtAmtFixed[" + frtAmtFixedStr + "]");

					frtRateStr = frtRateStr == null ? "0" : frtRateStr;
					frtAmtQtyStr = frtAmtQtyStr == null ? "0" : frtAmtQtyStr;
					frtAmtFixedStr = frtAmtFixedStr == null ? "0" : frtAmtFixedStr;

					if ("Q".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_amt__fixed protect = \"1\">")
								.append("<![CDATA[" + frtAmtFixedStr + "]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"0\">").append("<![CDATA[" + frtRateStr + "]]>")
								// .append("</frt_rate__o>");
								.append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"0\">")
								.append("<![CDATA[" + frtAmtQtyStr + "]]>").append("</frt_amt__qty>");
					} else if ("F".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_amt__fixed  protect = \"0\">")
								.append("<![CDATA[" + frtAmtFixedStr + "]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"1\">").append("<![CDATA[" + frtRateStr + "]]>")
								// .append("</frt_rate__o>");
								.append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"1\">")
								.append("<![CDATA[" + frtAmtQtyStr + "]]>").append("</frt_amt__qty>");
					}
					// /

				} else if (currentColumn.trim().equalsIgnoreCase("purc_order")) {
					System.out.println("@@@@@@@@ purc_order called @@@@@@@@");

					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
					amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));

					sql = " select purc_order,ord_date,pord_type,supp_code,site_code__dlv,site_code__ord,site_code__bill,"
							+ "  status,status_date,dept_code,emp_code,order_db,item_ser,tax_opt,cr_term,ord_amt,tax_amt,tot_amt,"
							+ "  curr_code,exch_rate,tax_chap,tax_class,tax_env,remarks,tax_date,proj_code,sales_pers,comm_perc,"
							+ "  comm_perc__on,curr_code__comm,quot_no,confirmed,conf_date,tran_code,frt_amt,curr_code__frt,frt_term,"
							+ "  dlv_term,insurance_amt,curr_code__ins,emp_code__aprv,ref_date ,anal_code,"
							+ "  FRT_TYPE, FRT_RATE,FRT_AMT__QTY, FRT_AMT__FIXED  "
							+ "  from porder where purc_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						purcOrder = checkNull(rs.getString("purc_order"));
						ordDate = rs.getTimestamp("ord_date");
						pordType = checkNull(rs.getString("pord_type"));
						suppCode = checkNull(rs.getString("supp_code"));
						siteCodeDlv = checkNull(rs.getString("site_code__dlv"));
						siteCodeOrd = checkNull(rs.getString("site_code__ord"));
						siteCodeBill = checkNull(rs.getString("site_code__bill"));
						status = checkNull(rs.getString("status"));
						statusDate = rs.getTimestamp("status_date");
						deptCode = checkNull(rs.getString("dept_code"));
						empCode = checkNull(rs.getString("emp_code"));
						orderDb = checkNull(rs.getString("order_db"));
						itemSer = checkNull(rs.getString("item_ser"));
						taxOpt = checkNull(rs.getString("tax_opt"));
						crTerm = checkNull(rs.getString("cr_term"));
						ordAmt = rs.getDouble("ord_amt");
						taxAmt = rs.getDouble("tax_amt");
						totAmt = rs.getDouble("tot_amt");
						currCode = checkNull(rs.getString("curr_code"));
						exchRate = rs.getDouble("exch_rate");
						taxChap = checkNull(rs.getString("tax_chap"));
						taxClass = checkNull(rs.getString("tax_class"));
						taxEnv = checkNull(rs.getString("tax_env"));
						remarks = checkNull(rs.getString("remarks"));
						taxDate = rs.getTimestamp("tax_date");
						projCode = checkNull(rs.getString("proj_code"));
						salesPers = checkNull(rs.getString("sales_pers"));
						commPerc = rs.getDouble("comm_perc");
						commPercOn = checkNull(rs.getString("comm_perc__on"));
						currCodeComm = checkNull(rs.getString("curr_code__comm"));
						quotNo = checkNull(rs.getString("quot_no"));
						confirmed = checkNull(rs.getString("confirmed"));
						confDate = rs.getTimestamp("conf_date");
						tranCode = checkNull(rs.getString("tran_code"));
						frtAmt = rs.getDouble("frt_amt");
						currCodeFrt = checkNull(rs.getString("curr_code__frt"));
						frtTerm = checkNull(rs.getString("frt_term"));
						dlvTerm = checkNull(rs.getString("dlv_term"));
						insuranceAmt = rs.getDouble("insurance_amt");
						currCodeIns = checkNull(rs.getString("curr_code__ins"));
						empCodeAprv = checkNull(rs.getString("emp_code__aprv"));
						refDate = rs.getTimestamp("ref_date");
						analCode = checkNull(rs.getString("anal_code"));
						// added by cpatil
						frtType = checkNull(rs.getString("FRT_TYPE"));
						frtRate = rs.getDouble("FRT_RATE");
						frtAmtQty = rs.getDouble("FRT_AMT__QTY");
						frtAmtFixed = rs.getDouble("FRT_AMT__FIXED");
						// end
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// frtRate = frtRate==0?0:frtRate;
					// frtAmtQty = frtAmtQty==0?0:frtAmtQty;
					// frtAmtFixed = frtAmtFixed==0?0:frtAmtFixed;

					if (ordDate != null) {
						ordDateStr = sdf.format(ordDate.getTime());
					}

					System.out.println("@@@@@ordDateStr" + ordDateStr);
					if (ordDateStr != null && ordDateStr.trim().length() > 0) {
						valueXmlString.append("<ord_date>").append("<![CDATA[" + ordDateStr + "]]>")
								.append("</ord_date>");
					} else {
						valueXmlString.append("<ord_date>").append("<![CDATA[]]>").append("</ord_date>");
					}
					valueXmlString.append("<pord_type>").append("<![CDATA[" + pordType + "]]>").append("</pord_type>");
					valueXmlString.append("<pord_type>").append("<![CDATA[" + pordType + "]]>").append("</pord_type>");
					valueXmlString.append("<supp_code>").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");
					valueXmlString.append("<supp_code__o>").append("<![CDATA[" + suppCode + "]]>")
							.append("</supp_code__o>");
					valueXmlString.append("<site_code__dlv>").append("<![CDATA[" + siteCodeDlv + "]]>")
							.append("</site_code__dlv>");
					valueXmlString.append("<site_code__dlv__o>").append("<![CDATA[" + siteCodeDlv + "]]>")
							.append("</site_code__dlv__o>");
					valueXmlString.append("<site_code__ord>").append("<![CDATA[" + siteCodeOrd + "]]>")
							.append("</site_code__ord>");
					valueXmlString.append("<site_code__ord__o>").append("<![CDATA[" + siteCodeOrd + "]]>")
							.append("</site_code__ord__o>");
					valueXmlString.append("<site_code__bill>").append("<![CDATA[" + siteCodeBill + "]]>")
							.append("</site_code__bill>");
					valueXmlString.append("<site_code__bill__o>").append("<![CDATA[" + siteCodeBill + "]]>")
							.append("</site_code__bill__o>");
					valueXmlString.append("<dept_code>").append("<![CDATA[" + deptCode + "]]>").append("</dept_code>");
					valueXmlString.append("<dept_code__o>").append("<![CDATA[" + deptCode + "]]>")
							.append("</dept_code__o>");
					valueXmlString.append("<emp_code>").append("<![CDATA[" + empCode + "]]>").append("</emp_code>");
					valueXmlString.append("<emp_code__o>").append("<![CDATA[" + empCode + "]]>")
							.append("</emp_code__o>");
					valueXmlString.append("<anal_code>").append("<![CDATA[" + analCode + "]]>").append("</anal_code>");

					sql = " select case when employee.emp_fname is null then ' ' else employee.emp_fname end as fname,"
							+ " case when employee.emp_mname is null then ' ' else employee.emp_mname end as mname,"
							+ " case when employee.emp_lname is null then ' ' else employee.emp_lname end as lname"
							+ " from employee where emp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, empCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						fname = checkNull(rs.getString(1));
						mname = checkNull(rs.getString(2));
						lname = checkNull(rs.getString(3));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<emp_fname>").append("<![CDATA[" + fname + "]]>").append("</emp_fname>");
					valueXmlString.append("<emp_mname>").append("<![CDATA[" + mname + "]]>").append("</emp_mname>");
					valueXmlString.append("<emp_lname>").append("<![CDATA[" + lname + "]]>").append("</emp_lname>");

					valueXmlString.append("<order_db>").append("<![CDATA[" + orderDb + "]]>").append("</order_db>");
					valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
					valueXmlString.append("<item_ser__o>").append("<![CDATA[" + itemSer + "]]>")
							.append("</item_ser__o>");
					valueXmlString.append("<tax_opt>").append("<![CDATA[" + taxOpt + "]]>").append("</tax_opt>");
					valueXmlString.append("<tax_opt__o>").append("<![CDATA[" + taxOpt + "]]>").append("</tax_opt__o>");
					valueXmlString.append("<cr_term>").append("<![CDATA[" + crTerm + "]]>").append("</cr_term>");
					valueXmlString.append("<cr_term__o>").append("<![CDATA[" + crTerm + "]]>").append("</cr_term__o>");
					valueXmlString.append("<ord_amt__o>").append("<![CDATA[" + ordAmt + "]]>").append("</ord_amt__o>");
					valueXmlString.append("<tax_amt__o>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt__o>");
					valueXmlString.append("<tot_amt__o>").append("<![CDATA[" + totAmt + "]]>").append("</tot_amt__o>");
					valueXmlString.append("<ord_amt>").append("<![CDATA[" + ordAmt + "]]>").append("</ord_amt>");
					valueXmlString.append("<tax_amt>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt>");
					valueXmlString.append("<tot_amt>").append("<![CDATA[" + totAmt + "]]>").append("</tot_amt>");
					valueXmlString.append("<curr_code>").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");
					valueXmlString.append("<curr_code__o>").append("<![CDATA[" + currCode + "]]>")
							.append("</curr_code__o>");
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
					valueXmlString.append("<exch_rate__o>").append("<![CDATA[" + exchRate + "]]>")
							.append("</exch_rate__o>");
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
					valueXmlString.append("<tax_chap__o>").append("<![CDATA[" + taxChap + "]]>")
							.append("</tax_chap__o>");
					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
					valueXmlString.append("<tax_class__o>").append("<![CDATA[" + taxClass + "]]>")
							.append("</tax_class__o>");
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
					valueXmlString.append("<tax_env__o>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env__o>");
					valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");

					System.out.println("Taxdate>>>" + taxDate);

					if (taxDate != null) {
						taxDateStr = sdf.format(taxDate.getTime());
						System.out.println("Taxdate>>>" + taxDateStr);
						valueXmlString.append("<tax_date__o>").append("<![CDATA[" + taxDateStr + "]]>")
								.append("</tax_date__o>");
					} else {
						valueXmlString.append("<tax_date__o>").append("<![CDATA[]]>").append("</tax_date__o>");
					}
					valueXmlString.append("<proj_code>").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
					valueXmlString.append("<proj_code__o>").append("<![CDATA[" + projCode + "]]>")
							.append("</proj_code__o>");
					valueXmlString.append("<sales_pers>").append("<![CDATA[" + salesPers + "]]>")
							.append("</sales_pers>");
					valueXmlString.append("<sales_pers__o>").append("<![CDATA[" + salesPers + "]]>")
							.append("</sales_pers__o>");
					valueXmlString.append("<comm_perc>").append("<![CDATA[" + commPerc + "]]>").append("</comm_perc>");
					valueXmlString.append("<comm_perc__o>").append("<![CDATA[" + commPerc + "]]>")
							.append("</comm_perc__o>");
					valueXmlString.append("<comm_perc__on>").append("<![CDATA[" + common + "]]>")
							.append("</comm_perc__on>");
					valueXmlString.append("<comm_perc__on__o>").append("<![CDATA[" + common + "]]>")
							.append("</comm_perc__on__o>");
					valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + currCodeComm + "]]>")
							.append("</curr_code__comm>");
					valueXmlString.append("<curr_code__comm__o>").append("<![CDATA[" + currCodeComm + "]]>")
							.append("</curr_code__comm__o>");
					valueXmlString.append("<quot_no>").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
					valueXmlString.append("<quot_no__o>").append("<![CDATA[" + quotNo + "]]>").append("</quot_no__o>");
					valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
					valueXmlString.append("<tran_code__o>").append("<![CDATA[" + tranCode + "]]>")
							.append("</tran_code__o>");
					// valueXmlString.append("<frt_amt>").append("<![CDATA["+frtAmt+"]]>").append("</frt_amt>");
					// // commented by cpatil
					valueXmlString.append("<frt_amt__o>").append("<![CDATA[" + frtAmt + "]]>").append("</frt_amt__o>");
					valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + currCodeFrt + "]]>")
							.append("</curr_code__frt>");
					valueXmlString.append("<curr_code__frt__o>").append("<![CDATA[" + currCodeFrt + "]]>")
							.append("</curr_code__frt__o>");
					valueXmlString.append("<frt_term>").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");
					valueXmlString.append("<frt_term__o>").append("<![CDATA[" + frtTerm + "]]>")
							.append("</frt_term__o>");
					valueXmlString.append("<dlv_term>").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term>");
					valueXmlString.append("<dlv_term__o>").append("<![CDATA[" + dlvTerm + "]]>")
							.append("</dlv_term__o>");
					valueXmlString.append("<insurance_amt>").append("<![CDATA[" + insuranceAmt + "]]>")
							.append("</insurance_amt>");
					valueXmlString.append("<insurance_amt__o>").append("<![CDATA[" + insuranceAmt + "]]>")
							.append("</insurance_amt__o>");
					valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + currCodeIns + "]]>")
							.append("</curr_code__ins>");
					valueXmlString.append("<curr_code__ins__o>").append("<![CDATA[" + currCodeIns + "]]>")
							.append("</curr_code__ins__o>");
					valueXmlString.append("<emp_code__aprv>").append("<![CDATA[" + empCodeAprv + "]]>")
							.append("</emp_code__aprv>");
					valueXmlString.append("<emp_code__aprv__o>").append("<![CDATA[" + empCodeAprv + "]]>")
							.append("</emp_code__aprv__o>");
					if (refDate != null) {
						refDateStr = sdf.format(refDate.getTime());
					}

					valueXmlString.append("<ref_date>").append("<![CDATA[" + refDateStr + "]]>").append("</ref_date>");
					valueXmlString.append("<ref_date__o>").append("<![CDATA[" + refDateStr + "]]>")
							.append("</ref_date__o>");

					if (suppCode != null && suppCode.length() > 0) {
						sql = "select supp_name, addr1, addr2, city, stan_code from supplier where supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							suppName = checkNull(rs.getString("supp_name"));
							suppAdd1 = checkNull(rs.getString("addr1"));
							suppAdd2 = checkNull(rs.getString("addr2"));
							suppCity = checkNull(rs.getString("city"));
							suppStanCode = checkNull(rs.getString("stan_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<supp_name>").append("<![CDATA[" + checkNull(suppName) + "]]>")
							.append("</supp_name>");
					valueXmlString.append("<supplier_addr1>").append("<![CDATA[" + checkNull(suppAdd1) + "]]>")
							.append("</supplier_addr1>");
					valueXmlString.append("<supplier_addr2>").append("<![CDATA[" + checkNull(suppAdd2) + "]]>")
							.append("</supplier_addr2>");
					valueXmlString.append("<supplier_city>").append("<![CDATA[" + checkNull(suppCity) + "]]>")
							.append("</supplier_city>");
					valueXmlString.append("<supplier_stan_code>").append("<![CDATA[" + checkNull(suppStanCode) + "]]>")
							.append("</supplier_stan_code>");

					// added by cpatil
					valueXmlString.append("<frt_type>").append("<![CDATA[" + checkNull(frtType) + "]]>")
							.append("</frt_type>");
					valueXmlString.append("<frt_type__o>").append("<![CDATA[" + checkNull(frtType) + "]]>")
							.append("</frt_type__o>");

					if ("Q".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_amt__fixed protect = \"1\">")
								.append("<![CDATA[" + (frtAmtFixed) + "]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"0\">").append("<![CDATA[" + frtRate + "]]>")
								.append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"0\">").append("<![CDATA[" + frtAmtQty + "]]>")
								.append("</frt_amt__qty>");
					} else if ("F".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_amt__fixed  protect = \"0\">")
								.append("<![CDATA[" + frtAmtFixed + "]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"1\">").append("<![CDATA[" + frtRate + "]]>")
								.append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"1\">").append("<![CDATA[" + frtAmtQty + "]]>")
								.append("</frt_amt__qty>");
					}

					// valueXmlString.append("<frt_rate>").append("<![CDATA["+frtRate+"]]>").append("</frt_rate>");
					valueXmlString.append("<frt_rate__o>").append("<![CDATA[" + frtRate + "]]>")
							.append("</frt_rate__o>");
					// valueXmlString.append("<frt_amt__qty>").append("<![CDATA["+frtAmtQty+"]]>").append("</frt_amt__qty>");
					valueXmlString.append("<frt_amt__qty__o>").append("<![CDATA[" + frtAmtQty + "]]>")
							.append("</frt_amt__qty__o>");
					// valueXmlString.append("<frt_amt__fixed>").append("<![CDATA["+frtAmtFixed+"]]>").append("</frt_amt__fixed>");
					valueXmlString.append("<frt_amt__fixed__o>").append("<![CDATA[" + frtAmtFixed + "]]>")
							.append("</frt_amt__fixed__o>");

					// end

				}

				// cpatil
				else if (currentColumn.trim().equalsIgnoreCase("frt_type")) {
					System.out.println("@@@@@@@@ item change call for " + currentColumn);
					frtType = checkNull(genericUtility.getColumnValue("frt_type", dom));
					frtRateStr = checkNull(genericUtility.getColumnValue("frt_rate", dom));
					frtAmtQtyStr = checkNull(genericUtility.getColumnValue("frt_amt__qty", dom));
					frtAmtFixedStr = checkNull(genericUtility.getColumnValue("frt_amt__fixed", dom));
					System.out.println("frtType[" + frtType + "]:::frtAmtFixed[" + frtAmtFixedStr + "]");

					frtAmtQtyStr = frtAmtQtyStr == null ? "0" : frtAmtQtyStr;
					frtAmtFixedStr = frtAmtFixedStr == null ? "0" : frtAmtFixedStr;

					if ("Q".equalsIgnoreCase(frtType)) {
						// valueXmlString.append("<frt_amt__fixed protect =
						// \"1\">").append("<![CDATA["+(
						// frtAmtFixedStr )+"]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_amt__fixed protect = \"1\">").append("<![CDATA[0]]>")
								.append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"0\">").append("<![CDATA[" + frtRateStr + "]]>")
								.append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"1\">")
								.append("<![CDATA[" + frtAmtQtyStr + "]]>").append("</frt_amt__qty>");
					} else if ("F".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_amt__fixed  protect = \"0\">")
								.append("<![CDATA[" + frtAmtFixedStr + "]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"1\">").append("<![CDATA[0]]>")
								.append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"1\">").append("<![CDATA[0]]>")
								.append("</frt_amt__qty>");
					}
				}

				// end

				else if (currentColumn.trim().equalsIgnoreCase("item_ser")) {
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					if (itemSer != null && itemSer.trim().length() > 0) {
						empCodePur = setDescription("emp_code__pur", "itemser", "item_ser", itemSer, conn);
					}
					valueXmlString.append("<emp_code>").append("<![CDATA[" + checkNull(empCodePur) + "]]>")
							.append("</emp_code>");
				} else if (currentColumn.trim().equalsIgnoreCase("site_code__dlv")) {
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));
					if (siteCodeDlv != null && siteCodeDlv.trim().length() > 0) {
						sql = " select 	descr, add1, add2, city, stan_code from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeDlv);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							siteDescr = checkNull(rs.getString("descr"));
							siteAdd1 = checkNull(rs.getString("add1"));
							siteAdd2 = checkNull(rs.getString("add2"));
							siteCity = checkNull(rs.getString("city"));
							siteStanCode = checkNull(rs.getString("stan_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<site_descr>").append("<![CDATA[" + checkNull(siteDescr) + "]]>")
							.append("</site_descr>");
					valueXmlString.append("<site_add1>").append("<![CDATA[" + checkNull(siteAdd1) + "]]>")
							.append("</site_add1>");
					valueXmlString.append("<site_add2>").append("<![CDATA[" + checkNull(siteAdd2) + "]]>")
							.append("</site_add2>");
					valueXmlString.append("<site_city>").append("<![CDATA[" + checkNull(siteCity) + "]]>")
							.append("</site_city>");
					valueXmlString.append("<site_stan_code>").append("<![CDATA[" + checkNull(siteStanCode) + "]]>")
							.append("</site_stan_code>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("supp_code")) {
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					suppCodeO = checkNull(genericUtility.getColumnValue("supp_code__o", dom));
					if ((suppCode != null && suppCode.trim().length() > 0) && !(suppCode.equalsIgnoreCase(suppCodeO))) {
						sql = "select supp_name, addr1, addr2, city, stan_code from supplier where supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							suppName = checkNull(rs.getString("supp_name"));
							suppAdd1 = checkNull(rs.getString("addr1"));
							suppAdd2 = checkNull(rs.getString("addr2"));
							suppCity = checkNull(rs.getString("city"));
							suppStanCode = checkNull(rs.getString("stan_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<supp_name>").append("<![CDATA[" + checkNull(suppName) + "]]>")
							.append("</supp_name>");
					valueXmlString.append("<supplier_addr1>").append("<![CDATA[" + checkNull(suppAdd1) + "]]>")
							.append("</supplier_addr1>");
					valueXmlString.append("<supplier_addr2>").append("<![CDATA[" + checkNull(suppAdd2) + "]]>")
							.append("</supplier_addr2>");
					valueXmlString.append("<supplier_city>").append("<![CDATA[" + checkNull(suppCity) + "]]>")
							.append("</supplier_city>");
					valueXmlString.append("<supplier_stan_code>").append("<![CDATA[" + checkNull(suppStanCode) + "]]>")
							.append("</supplier_stan_code>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("dept_code")) {
					deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));
					if (deptCode != null && deptCode.trim().length() > 0) {
						descr = setDescription("descr", "department", "dept_code", deptCode, conn);
					}
					valueXmlString.append("<department_descr>").append("<![CDATA[" + checkNull(descr) + "]]>")
							.append("</department_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("cr_term")) {
					crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
					if (crTerm != null && crTerm.trim().length() > 0) {
						descr = setDescription("descr", "crterm", "cr_term", crTerm, conn);
					}
					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + checkNull(descr) + "]]>")
							.append("</crterm_descr>");
				} else if (currentColumn.trim().equalsIgnoreCase("curr_code")) {
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					if (crTerm != null && crTerm.trim().length() > 0) {
						descr = setDescription("std_exrt", "currency", "curr_code", currCode, conn);
					}
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + checkNull(descr) + "]]>")
							.append("</exch_rate>");
				} else if (currentColumn.trim().equalsIgnoreCase("sales_pers")) {
					salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));
					if (salesPers != null && salesPers.trim().length() > 0) {
						sql = "select comm_perc, comm_perc__on, curr_code from sales_pers where sales_pers = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, salesPers);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							commPerc = rs.getDouble("comm_perc");
							commPercOn = rs.getString("comm_perc__on");
							currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

					}
					valueXmlString.append("<comm_perc>").append("<![CDATA[" + commPerc + "]]>").append("</comm_perc>");
					valueXmlString.append("<comm_perc__on>").append("<![CDATA[" + checkNull(commPercOn) + "]]>")
							.append("</comm_perc__on>");
					valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + checkNull(currCode) + "]]>")
							.append("</curr_code__comm>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("emp_code")) {
					empCode = checkNull(genericUtility.getColumnValue("emp_code", dom));
					if (empCode != null && empCode.trim().length() > 0) {
						sql = " select case when employee.emp_fname is null then ' ' else employee.emp_fname end as fname,"
								+ " case when employee.emp_mname is null then ' ' else employee.emp_mname end as mname,"
								+ " case when employee.emp_lname is null then ' ' else employee.emp_lname end as lname"
								+ " from employee where emp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							fname = rs.getString(1);
							mname = rs.getString(2);
							lname = rs.getString(3);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<emp_fname>").append("<![CDATA[" + fname + "]]>").append("</emp_fname>");
						valueXmlString.append("<emp_mname>").append("<![CDATA[" + mname + "]]>").append("</emp_mname>");
						valueXmlString.append("<emp_lname>").append("<![CDATA[" + lname + "]]>").append("</emp_lname>");

					}
				} // case 1 end
				valueXmlString.append("</Detail1>");
				break;
			// case 2 start
			case 2:
				System.out.println("**********************In case 2 ***********************8");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
				System.out.println(
						"IN DETAILS column name is %%%%%%%%%%%%%[" + currentColumn + "] ==> '" + columnValue + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) {
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom));

					if ((suppCode != null && suppCode.trim().length() > 0)
							&& (siteCodeDlv != null && siteCodeDlv.trim().length() > 0)) {
						sql = "select channel_partner from site_supplier  where site_code__ch = ? and supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeDlv);
						pstmt.setString(2, suppCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							channelPartner = rs.getString("channel_partner");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (channelPartner != null && channelPartner.trim().length() > 0) {
						sql = " select case when channel_partner is null then 'N' else channel_partner end as channel_partner	"
								+ " from supplier where supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							channelPartner = rs.getString("channel_partner");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					if ("Y".equalsIgnoreCase(channelPartner)) {
						valueXmlString.append("<status protect = \"0\" >").append("<![CDATA[" + status + "]]>")
								.append("</status>");
					} else {
						valueXmlString.append("<status protect = \"1\" >").append("<![CDATA[" + status + "]]>")
								.append("</status>");
					}

				}

				else if (currentColumn.trim().equalsIgnoreCase("itm_default")) {

					amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom1));
					System.out.println("@@@@@@@@@@@@@***** dom 1 *******[amdNo]::::::[" + amdNo + "]");

					valueXmlString.append("<amd_no >").append("<![CDATA[" + amdNo + "]]>").append("</amd_no>");
					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));
					valueXmlString.append("<purc_order >").append("<![CDATA[" + purcOrder + "]]>")
							.append("</purc_order>");
					siteCodeDlv = checkNull(genericUtility.getColumnValue("site_code__dlv", dom1));
					valueXmlString.append("<site_code >").append("<![CDATA[" + siteCodeDlv + "]]>")
							.append("</site_code>");
					amddt = checkNull(genericUtility.getColumnValue("amd_date", dom1));
					if (amddt != null) {
						valueXmlString.append("<req_date >").append("<![CDATA[" + amddt + "]]>").append("</req_date>");
						valueXmlString.append("<dlv_date >").append("<![CDATA[" + amddt + "]]>").append("</dlv_date>");
					}
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
					sql = " select channel_partner from site_supplier where  site_code__ch = ? and supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeDlv);
					pstmt.setString(2, suppCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						channelPartner = rs.getString("channel_partner");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (channelPartner == null || channelPartner.trim().length() == 0) {
						sql = "select case when channel_partner is null then 'N' else channel_partner end from supplier where supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							channelPartner = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if ("Y".equalsIgnoreCase(channelPartner)) {
						valueXmlString.append("<status protect = \"0\">").append("<![CDATA[" + status + "]]>")
								.append("</status>");
					} else {
						valueXmlString.append("<status protect = \"1\">").append("<![CDATA[" + status + "]]>")
								.append("</status>");
					}

					valueXmlString.append("<line_no__ord protect = \"0\">").append("<![CDATA[" + lineNoOrd + "]]>")
							.append("</line_no__ord>");
					valueXmlString.append("<ind_no protect = \"0\">").append("<![CDATA[" + indNo + "]]>")
							.append("</ind_no>");
					valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + itemCode + "]]>")
							.append("</item_code>");

					sql = " select case when eou is null then 'N' else eou end From site Where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeDlv);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						eou = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if ("Y".equalsIgnoreCase(eou)) {
						valueXmlString.append("<duty_paid >").append("N").append("</duty_paid>");
					} else {
						valueXmlString.append("<duty_paid >").append("Y").append("</duty_paid>");
					}

				} else if (currentColumn.trim().equalsIgnoreCase("line_no__ord")) {
					lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
					sitecode = checkNull(genericUtility.getColumnValue("site_code", dom));
					// acctcode1=checkNull(genericUtility.getColumnValue("acct_code__dr",
					// dom));
					// cctrcode1=checkNull(genericUtility.getColumnValue("cctr_code__dr",
					// dom));
					// deptcode=checkNull(genericUtility.getColumnValue("dept_code",
					// dom));
					analcode = checkNull(genericUtility.getColumnValue("anal_code", dom1));
					System.out.println("anal code is:" + analcode);

					if (lineNoOrd != null && lineNoOrd.trim().length() > 0 && purcOrder != null
							&& purcOrder.trim().length() > 0) {

						lineNoOrd = "    " + lineNoOrd;
						lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3, lineNoOrd.length());
						valueXmlString.append("<line_no__ord >").append("<![CDATA[" + lineNoOrd + "]]>")
								.append("</line_no__ord>");

						sql = " select site_code,ind_no,item_code,quantity,unit,rate,discount,tax_amt,tot_amt,loc_code,req_date,"
								+ " dlv_date,dlv_qty,status,status_date,tax_class,tax_chap,tax_env,remarks,work_order,unit__rate,"
								+ "	conv__qty_stduom,conv__rtuom_stduom,unit__std,quantity__stduom,RATE__STDUOM,pack_code,no_art,pack_instr,"
								+ "	acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,discount_type,status,supp_code__mnfr,specific_instr,"
								//+ "	RATE__CLG,special_instr,benefit_type,licence_no,form_no,duty_paid,dept_code,proj_code"//Modified by Rohini T on 16/04/2021
								+ "	RATE__CLG,special_instr,benefit_type,licence_no,form_no,duty_paid,dept_code,proj_code,dimension"
								+ "   from  porddet where purc_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							siteCode = checkNull(rs.getString("site_code"));
							indNo = checkNull(rs.getString("ind_no"));
							itemCode = checkNull(rs.getString("item_code"));
							quantity = rs.getDouble("quantity");
							unit = checkNull(rs.getString("unit"));
							rate = rs.getDouble("rate");
							discount = rs.getDouble("discount");
							taxAmt = rs.getDouble("tax_amt");
							totAmt = rs.getDouble("tot_amt");
							locCode = checkNull(rs.getString("loc_code"));
							reqDate = rs.getTimestamp("req_date");
							dlvDate = rs.getTimestamp("dlv_date");
							dlvQty = rs.getDouble("dlv_qty");
							status = checkNull(rs.getString("status"));
							statusDate = rs.getTimestamp("status_date");
							taxClass = checkNull(rs.getString("tax_class"));
							taxChap = checkNull(rs.getString("tax_chap"));
							taxEnv = checkNull(rs.getString("tax_env"));
							remarks = checkNull(rs.getString("remarks"));
							WorkOrder = checkNull(rs.getString("work_order"));
							unitRate = checkNull(rs.getString("unit__rate"));
							conv_qty_stduom = rs.getDouble("conv__qty_stduom");
							conv_rtuom_stduom = rs.getDouble("conv__rtuom_stduom");
							unitStd = checkNull(rs.getString("unit__std"));
							quantityStduom = rs.getDouble("quantity__stduom");
							rate_stduom = rs.getDouble("RATE__STDUOM");
							packCode = checkNull(rs.getString("pack_code"));
							noArt = rs.getDouble("no_art");
							packInstr = checkNull(rs.getString("pack_instr"));
							acct_dr = (rs.getString("acct_code__dr"));
							cctr_dr = (rs.getString("cctr_code__dr"));
							acct_cr = (rs.getString("acct_code__cr"));
							cctr_cr = (rs.getString("cctr_code__cr"));
							discountType = checkNull(rs.getString("discount_type"));
							status = checkNull(rs.getString("status"));
							supp_code_mnfr = checkNull(rs.getString("supp_code__mnfr"));
							specificInstr = checkNull(rs.getString("specific_instr"));
							rateClg = rs.getDouble("RATE__CLG");
							specialInstr = checkNull(rs.getString("special_instr"));
							benefitType = checkNull(rs.getString("benefit_type"));
							licenceNo = checkNull(rs.getString("licence_no"));
							formNo = checkNull(rs.getString("form_no"));
							dutyPaid = checkNull(rs.getString("duty_paid"));
							deptCode = checkNull(rs.getString("dept_code"));
							projCode = checkNull(rs.getString("proj_code"));
							dimension = checkNull(rs.getString("dimension"));//Modified by Rohini T on 16/04/2021
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("Site code is" + siteCode);
						System.out.println("Site code is" + sitecode);

						// Added by Abhijit on 22-7-2015
						sql = "select acct_code__dr,cctr_code__dr, dept_code from porddet where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							acctcode1 = checkNull(rs.getString("acct_code__dr"));
							cctrcode1 = checkNull(rs.getString("cctr_code__dr"));
							deptcode = checkNull(rs.getString("dept_code"));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("account code is:" + acctcode1);
						System.out.println("cctr code is:" + acctcode1);
						System.out.println("dept code is:" + deptcode);

						// sql="select
						// FN_GET_BUDGET_AMT('P-AMD','"+sitecode+"','"+acctcode1+"','"+cctrcode1+"','"+analcode+"','"+deptcode+"','A')
						// from dual";
						sql = "select FN_GET_BUDGET_AMT('P-AMD','" + siteCode + "','" + acctcode1 + "','" + cctrcode1
								+ "','" + analcode + "','" + deptcode + "','A') from dual";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							BudgetStr = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						System.out.println("budget amount.................. ![" + BudgetStr + "]");

						// ('P-AMD','"+sitecode+"','"+acctcode1+"','"+cctrcode1+"','"+analcode+"','"+deptcode+"','A')

						sql = "select FN_GET_CONS_AMT('P-AMD','" + siteCode + "','" + acctcode1 + "','" + cctrcode1
								+ "','" + analcode + "','" + deptcode + "','A') from dual";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							CONSStr = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						System.out.println("consumed amount.................. ![" + CONSStr + "]");

						sql = "select  (FN_GET_BUDGET_AMT('P-AMD','" + siteCode + "','" + acctcode1 + "','" + cctrcode1
								+ "','" + analcode + "','" + deptcode + "','A'))- (FN_GET_CONS_AMT('P-AMD','" + sitecode
								+ "','" + acctcode1 + "','" + cctrcode1 + "','" + analcode + "','" + deptcode
								+ "','A')) from dual";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							totStr = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						System.out.println("total amount.................. ![" + totStr + "]");
						// Added by Varsha V on 30-11-18 to take delivered quantity and contract no and
						// unconfirmed receipt count
						sql = "select contract_no, dlv_qty from porddet where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							contractNo = checkNull(rs.getString("contract_no"));
							deliveryQty = rs.getDouble("dlv_qty");
						}
						System.out.println(
								"contractNo in Porder :: " + contractNo + " deliveryQty in porder :: " + deliveryQty);
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}

						int count = 0;
						sql = "select COUNT(1) as cnt from PORCP, PORCPDET" + " WHERE PORCP.TRAN_ID = PORCPDET.TRAN_ID"
								+ " AND PORCPDET.PURC_ORDER = ?" + " AND	PORCP.CONFIRMED ='N'"
								+ " AND LINE_NO__ORD = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt("cnt");
						}
						System.out.println("Unconfirm PORCP count" + count);
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
						// Ended by Varsha V on 30-11-18 to take delivered quantity and contract no and
						// unconfirmed receipt count
						// Added by chandrashekar on 22-04-2014
						descr = setDescription("descr", "item", "item_code", itemCode, conn);
						valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>")
								.append("</item_descr>");
						// End by chandrashekar

						valueXmlString.append("<status >").append("<![CDATA[" + status + "]]>").append("</status>");
						// Commented and Added by Varsha V 30-11-18 to make site_code disabled if
						// purchase order is made against purchase contract
						/*
						 * valueXmlString.append("<site_code >") .append("<![CDATA[" + siteCode + "]]>")
						 * .append("</site_code>");
						 */
						if ((contractNo != null && contractNo.trim().length() > 0) || deliveryQty > 0 || count > 0) {
							valueXmlString.append("<site_code  protect = \"1\">").append("<![CDATA[" + siteCode + "]]>")
									.append("</site_code>");
						} else {
							valueXmlString.append("<site_code  protect = \"0\">").append("<![CDATA[" + siteCode + "]]>")
									.append("</site_code>");
						}
						// Commented and Ended by Varsha V 30-11-18 to make site_code disabled if
						// purchase order is made against purchase contract

						valueXmlString.append("<site_code__o >").append("<![CDATA[" + siteCode + "]]>")
								.append("</site_code__o>");
						valueXmlString.append("<ind_no>").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
						valueXmlString.append("<item_code >").append("<![CDATA[" + itemCode + "]]>")
								.append("</item_code>");
						valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>")
								.append("</quantity>");
						valueXmlString.append("<quantity__o >").append("<![CDATA[" + quantity + "]]>")
								.append("</quantity__o>");
						valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
						valueXmlString.append("<rate >").append("<![CDATA[" + rate + "]]>").append("</rate>");
						valueXmlString.append("<rate__o >").append("<![CDATA[" + rate + "]]>").append("</rate__o>");
						valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>")
								.append("</discount>");
						valueXmlString.append("<tax_amt >").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt>");
						valueXmlString.append("<tot_amt >").append("<![CDATA[" + totAmt + "]]>").append("</tot_amt>");
						valueXmlString.append("<loc_code >").append("<![CDATA[" + locCode + "]]>")
								.append("</loc_code>");

						if (reqDate != null) {
							reqDateStr = sdf.format(reqDate.getTime());
							valueXmlString.append("<req_date >").append("<![CDATA[" + reqDateStr + "]]>")
									.append("</req_date>");

							valueXmlString.append("<req_date__o>").append("<![CDATA[" + reqDateStr + "]]>")
									.append("</req_date__o>");
						} else {
							valueXmlString.append("<req_date >").append("<![CDATA[]]>").append("</req_date>");

							valueXmlString.append("<req_date__o>").append("<![CDATA[]]>").append("</req_date__o>");

						}
						if (dlvDate != null) {
							dlvDateStr = sdf.format(dlvDate.getTime());
							valueXmlString.append("<dlv_date >").append("<![CDATA[" + dlvDateStr + "]]>")
									.append("</dlv_date>");
							valueXmlString.append("<dlv_date__o>").append("<![CDATA[" + dlvDateStr + "]]>")
									.append("</dlv_date__o>");
						} else {
							valueXmlString.append("<dlv_date >").append("<![CDATA[]]>").append("</dlv_date>");
							valueXmlString.append("<dlv_date__o>").append("<![CDATA[]]>").append("</dlv_date__o>");
						}
						valueXmlString.append("<dlv_qty>").append("<![CDATA[" + dlvQty + "]]>").append("</dlv_qty>");
						valueXmlString.append("<tax_class__o>").append("<![CDATA[" + taxClass + "]]>")
								.append("</tax_class__o>");
						valueXmlString.append("<tax_chap__o >").append("<![CDATA[" + taxChap + "]]>")
								.append("</tax_chap__o>");
						valueXmlString.append("<tax_env__o >").append("<![CDATA[" + taxEnv + "]]>")
								.append("</tax_env__o>");
						valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>")
								.append("</tax_class>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");
						valueXmlString.append("<work_order>").append("<![CDATA[" + WorkOrder + "]]>")
								.append("</work_order>");
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>")
								.append("</unit__rate>");
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv_qty_stduom + "]]>")
								.append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv_rtuom_stduom + "]]>")
								.append("</conv__rtuom_stduom>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
								.append("</unit__std>");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + quantityStduom + "]]>")
								.append("</quantity__stduom>");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate_stduom + "]]>")
								.append("</rate__stduom>");
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>")
								.append("</rate__clg>");// Manoj dtd 05/11/14 to
														// set
														// Rate__clg from
														// porddet
						valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>")
								.append("</pack_code>");
						valueXmlString.append("<no_art>").append("<![CDATA[" + noArt + "]]>").append("</no_art>");
						valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>")
								.append("</pack_instr>");
						valueXmlString.append("<acct_code__dr_o>").append("<![CDATA[" + acct_dr + "]]>")
								.append("</acct_code__dr_o>");
						valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + acct_dr + "]]>")
								.append("</acct_code__dr>");
						valueXmlString.append("<cctr_code__dr__o>").append("<![CDATA[" + cctr_dr + "]]>")
								.append("</cctr_code__dr__o>");
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + cctr_dr + "]]>")
								.append("</cctr_code__dr>");
						valueXmlString.append("<acct_code__cr__o>").append("<![CDATA[" + acct_cr + "]]>")
								.append("</acct_code__cr__o>");
						valueXmlString.append("<acct_code__cr>").append("<![CDATA[" + acct_cr + "]]>")
								.append("</acct_code__cr>");
						valueXmlString.append("<cctr_code__cr__o>").append("<![CDATA[" + cctr_cr + "]]>")
								.append("</cctr_code__cr__o>");
						valueXmlString.append("<cctr_code__cr >").append("<![CDATA[" + cctr_cr + "]]>")
								.append("</cctr_code__cr>");
						valueXmlString.append("<discount_type >").append("<![CDATA[" + discountType + "]]>")
								.append("</discount_type>");
						valueXmlString.append("<supp_code_mnfr__O>").append("<![CDATA[" + supp_code_mnfr + "]]>")
								.append("</supp_code_mnfr__O>");
						valueXmlString.append("<supp_code_mnfr >").append("<![CDATA[" + supp_code_mnfr + "]]>")
								.append("</supp_code_mnfr>");
						valueXmlString.append("<specific_instr__O>").append("<![CDATA[" + specificInstr + "]]>")
								.append("</specific_instr__O>");
						valueXmlString.append("<specific_instr >").append("<![CDATA[" + specificInstr + "]]>")
								.append("</specific_instr>");
						valueXmlString.append("<special_instr__O>").append("<![CDATA[" + specialInstr + "]]>")
								.append("</special_instr__O>");
						valueXmlString.append("<special_instr >").append("<![CDATA[" + specialInstr + "]]>")
								.append("</special_instr>");
						valueXmlString.append("<benefit_type >").append("<![CDATA[" + benefitType + "]]>")
								.append("</benefit_type>");
						valueXmlString.append("<benefit_type__o>").append("<![CDATA[" + benefitType + "]]>")
								.append("</benefit_type__o>");
						valueXmlString.append("<licence_no >").append("<![CDATA[" + licenceNo + "]]>")
								.append("</licence_no>");
						valueXmlString.append("<licence_no__o >").append("<![CDATA[" + licenceNo + "]]>")
								.append("</licence_no__o>");
						valueXmlString.append("<form_no >").append("<![CDATA[" + formNo + "]]>").append("</form_no>");
						valueXmlString.append("<form_no__o >").append("<![CDATA[" + formNo + "]]>")
								.append("</form_no__o>");
						valueXmlString.append("<duty_paid >").append("<![CDATA[" + dutyPaid + "]]>")
								.append("</duty_paid>");
						valueXmlString.append("<duty_paid__o >").append("<![CDATA[" + dutyPaid + "]]>")
								.append("</duty_paid__o>");
						valueXmlString.append("<dept_code >").append("<![CDATA[" + deptCode + "]]>")
								.append("</dept_code>");
						valueXmlString.append("<proj_code >").append("<![CDATA[" + projCode + "]]>")
								.append("</proj_code>");
						valueXmlString.append("<proj_code__o >").append("<![CDATA[" + projCode + "]]>")
								.append("</proj_code__o>");
						valueXmlString.append("<budget_amt_anal >").append("<![CDATA[" + BudgetStr + "]]>")
								.append("</budget_amt_anal>");
						valueXmlString.append("<consumed_amt_anal >").append("<![CDATA[" + CONSStr + "]]>")
								.append("</consumed_amt_anal>");
						valueXmlString.append("<budget_amt >").append("<![CDATA[" + totStr + "]]>")
								.append("</budget_amt>");

						sql = " select emp_code__qcaprv from  porddet where purc_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							empCodeQcAprv = checkNull(rs.getString("emp_code__qcaprv"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (empCodeQcAprv != null && empCodeQcAprv.trim().length() > 0) {
							sql = " select case when employee.emp_fname is null then ' ' else employee.emp_fname end as fname,"
									+ " case when employee.emp_mname is null then ' ' else employee.emp_mname end as mname,"
									+ " case when employee.emp_lname is null then ' ' else employee.emp_lname end as lname"
									+ " from employee where emp_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCodeQcAprv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								fname = checkNull(rs.getString(1));
								mname = checkNull(rs.getString(2));
								lname = checkNull(rs.getString(3));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							valueXmlString.append("<emp_fname>").append("<![CDATA[" + fname + "]]>")
									.append("</emp_fname>");
							valueXmlString.append("<emp_mname>").append("<![CDATA[" + mname + "]]>")
									.append("</emp_mname>");
							valueXmlString.append("<emp_lname>").append("<![CDATA[" + lname + "]]>")
									.append("</emp_lname>");

							valueXmlString.append("<emp_fname_1>").append("<![CDATA[" + fname + "]]>")
									.append("</emp_fname_1>");
							valueXmlString.append("<emp_mname_1>").append("<![CDATA[" + mname + "]]>")
									.append("</emp_mname_1>");
							valueXmlString.append("<emp_lname_1>").append("<![CDATA[" + lname + "]]>")
									.append("</emp_lname_1>");

							valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeQcAprv + "]]>")
									.append("</emp_code__qcaprv>");
							valueXmlString.append("<emp_code__qcaprv__o>").append("<![CDATA[" + empCodeQcAprv + "]]>")
									.append("</emp_code__qcaprv__o>");

							descr = setDescription("descr", "item", "item_code", itemCode, conn);

							valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>")
									.append("</item_descr>");

							valueXmlString.append("<ind_no protect = \"1\">").append("<![CDATA[" + indNo + "]]>")
									.append("</ind_no>");
							valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + itemCode + "]]>")
									.append("</item_code>");

						} else {
							valueXmlString.append("<line_no__ord protect = \"0\">")
									.append("<![CDATA[" + lineNoOrd + "]]>").append("</line_no__ord>");
							valueXmlString.append("<ind_no protect = \"0\">").append("<![CDATA[" + indNo + "]]>")
									.append("</ind_no>");
							valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + itemCode + "]]>")
									.append("</item_code>");

						}
						// added by ritesh on 02/feb/2015 for request S14ISUN007
						// start
						lastPurcRate = 0d;
						lastPurcPo = "";
						// itemCode = genericUtility.getColumnValue("item_code",
						// dom); //RITESH
						ordDateHdr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
						ordDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDateHdr,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

						lastPurcRate = getLastPurcRate(itemCode, ordDate, siteCode, conn);
						System.out.println("lastPurcRate ::[ " + lastPurcRate + " ]");
						lastPurcPo = getLastPurcPo(itemCode, ordDate, siteCode, conn);
						System.out.println("lastPurcPo ::[ " + lastPurcPo + " ]");
						valueXmlString.append("<last_purc_rate>").append("<![CDATA[" + lastPurcRate + "]]>")
								.append("</last_purc_rate>");
						valueXmlString.append("<last_purc_po>").append("<![CDATA[" + lastPurcPo + "]]>")
								.append("</last_purc_po>");
						// added by ritesh on 02/feb/2015 for request S14ISUN007
						// end
						
						//Modified by Rohini T on 15/04/2021[Start]
						
						System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);

						if(dimension!=null && dimension.trim().length()>0)
						{
							valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>");
							valueXmlString.append("<dimension__o>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension__o>"); 
						}
						if(noArt!=0)
						{
							valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>");
							valueXmlString.append("<no_art__o>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art__o>");
						}
						//Modified by Rohini T on 15/04/2021[End]
					}

				}

				else if (currentColumn.trim().equalsIgnoreCase("ind_no")) {
					lineNoOrd = genericUtility.getColumnValue("line_no__ord", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);

					if (lineNoOrd == null || lineNoOrd.trim().length() == 0) {
						indNo = genericUtility.getColumnValue("ind_no", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						if (indNo != null && indNo.trim().length() > 0) {
							valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + itemCode + "]]>")
									.append("</item_code>");
							sql = " Select item_code,unit__ind,(quantity__stduom - ord_qty)	as pendqty ,req_date,site_code__dlv,conv__qty_stduom,"
									+ " acct_code, cctr_code ,remarks from indent where ind_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, indNo);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								itemCode = checkNull(rs.getString("item_code"));
								unit = checkNull(rs.getString("unit__ind"));
								pendqty = rs.getDouble("pendqty");
								reqDate = rs.getTimestamp("req_date");
								siteCode = checkNull(rs.getString("site_code__dlv"));
								conv_qty_stduom = rs.getDouble("conv__qty_stduom");
								acct_dr = checkNull(rs.getString("acct_code"));
								cctr_dr = checkNull(rs.getString("cctr_code"));
								remarks = checkNull(rs.getString("remarks"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							valueXmlString.append("<item_code >").append("<![CDATA[" + itemCode + "]]>")
									.append("</item_code>");
							valueXmlString.append("<unit >").append("<![CDATA[" + unit + "]]>").append("</unit>");
							valueXmlString.append("<site_code >").append("<![CDATA[" + siteCode + "]]>")
									.append("</site_code>");
							valueXmlString.append("<pack_instr >").append("<![CDATA[" + packInstr + "]]>")
									.append("</pack_instr>");
							valueXmlString.append("<remarks >").append("<![CDATA[" + remarks + "]]>")
									.append("</remarks>");
							valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + quantityStduom + "]]>")
									.append("</quantity__stduom>");
							valueXmlString.append("<acct_code__dr >").append("<![CDATA[" + acct_dr + "]]>")
									.append("</acct_code__dr>");
							valueXmlString.append("<cctr_code__dr >").append("<![CDATA[" + cctr_dr + "]]>")
									.append("</cctr_code__dr>");

							sql = " select rate__ref from supplieritem where supp_code = ? and item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, suppCode);
							pstmt.setString(2, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								rate = rs.getDouble("rate__ref");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							valueXmlString.append("<rate >").append("<![CDATA[" + rate + "]]>").append("</rate>");

							// sql = " Select descr, loc_code, unit, unit__pur into :mdescr, :mloc,
							// :ls_unit, :ls_unitpur from item where item_code = :itemcode ";
							sql = "	Select descr, loc_code, unit, unit__pur from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								itemDescr = checkNull(rs.getString("descr"));
								locCode = checkNull(rs.getString("loc_code"));
								unit = checkNull(rs.getString("unit"));
								unitPur = checkNull(rs.getString("unit__pur"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (unitPur == null || unitPur.trim().length() == 0) {
								unitPur = unit;
							}
							valueXmlString.append("<unit >").append("<![CDATA[" + unitPur + "]]>").append("</unit>");

							if (conv_qty_stduom == 0) {
								conv_qty_stduom = 1;
							}
							valueXmlString.append("<unit__std >").append("<![CDATA[" + unit + "]]>")
									.append("</unit__std>");
							valueXmlString.append("<unit__rate >").append("<![CDATA[" + unitPur + "]]>")
									.append("</unit__rate>");

							if (unit.equalsIgnoreCase(unitPur)) {
								valueXmlString.append("<conv__qty_stduom >")
										.append("<![CDATA[" + conv_qty_stduom + "]]>").append("</conv__qty_stduom>");
								valueXmlString.append("<conv__rtuom_stduom >")
										.append("<![CDATA[" + conv_rtuom_stduom + "]]>")
										.append("</conv__rtuom_stduom>");
								valueXmlString.append("<quantity__o >").append("<![CDATA[" + pendqty + "]]>")
										.append("</quantity__o>");
							} else {
								quantity = pendqty;

								quantityList = disscommon.convQtyFactor(unitPur, unit, itemCode, quantity,
										conv_qty_stduom, conn);
								quantity = Double.parseDouble(quantityList.get(1).toString());
								valueXmlString.append("<conv__qty_stduom >")
										.append("<![CDATA[" + conv_qty_stduom + "]]>").append("</conv__qty_stduom>");
								if (conv_qty_stduom == 0) {
									conv_qty_stduom = 1;
								}
								valueXmlString.append("<conv__rtuom_stduom >")
										.append("<![CDATA[" + 1 / conv_qty_stduom + "]]>")
										.append("</conv__rtuom_stduom>");
								quantityList = disscommon.convQtyFactor(unit, unitPur, itemCode, pendqty,
										conv_qty_stduom, conn);
								quantity = Double.parseDouble(quantityList.get(1).toString());
								valueXmlString.append("<quantity__o >").append("<![CDATA[" + quantity + "]]>")
										.append("</quantity__o>");
							}
							valueXmlString.append("<item_descr >").append("<![CDATA[" + itemDescr + "]]>")
									.append("</item_descr>");
							valueXmlString.append("<loc_code >").append("<![CDATA[" + locCode + "]]>")
									.append("</loc_code>");
							valueXmlString.append("<rate__stduom >").append("<![CDATA[" + rate + "]]>")
									.append("</rate__stduom>");

							if (reqDate != null) {
								reqDateStr = sdf.format(reqDate.getTime());
								valueXmlString.append("<req_date >").append("<![CDATA[" + reqDateStr + "]]>")
										.append("</req_date>");
								valueXmlString.append("<dlv_date >").append("<![CDATA[" + reqDateStr + "]]>")
										.append("</dlv_date>");
							} else {
								valueXmlString.append("<req_date >").append("<![CDATA[]]>").append("</req_date>");
								valueXmlString.append("<dlv_date >").append("<![CDATA[]]>").append("</dlv_date>");
							}
							valueXmlString.append("<pack_code >").append("<![CDATA[" + packCode + "]]>")
									.append("</pack_code>");
							valueXmlString.append("<pack_instr >").append("<![CDATA[" + packInstr + "]]>")
									.append("</pack_instr>");

							itemSer = genericUtility.getColumnValue("item_ser", dom1);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							pordType = checkNull(genericUtility.getColumnValue("pord_type", dom1));

							if (acct_dr == null || acct_dr.trim().length() == 0) {
								// changes-by monika-17-may-2019

								if (pordType == null || pordType.trim().length() > 0) {

									sql = "select pord_type  from porder where purc_order = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, purcOrder);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										pordType = checkNull(rs.getString("pord_type"));

									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								cctr_dr = fincommon.getAcctDetrTtype(itemCode, itemSer, "IN", pordType, siteCode, conn);
								// commented-by-monika
								/*
								 * cctr_dr = fincommon.getFromAcctDetr(itemCode, itemSer, "IN", conn);
								 */

								/*
								 * String[] cctr_drStr = cctr_dr.split(","); int len = cctr_drStr.length - 1;
								 * System.out.println("@@@cr len[" + len + "]"); if (len > -1) { acct_dr =
								 * cctr_drStr[0]; } else { acct_dr = ""; }
								 */

								String mcctrArray[] = cctr_dr.split(",");
								System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
								if (mcctrArray.length > 0) {
									acct_dr = mcctrArray[0];
									cctr_dr = "";
								}
								if (mcctrArray.length > 1) {
									acct_dr = mcctrArray[0];
									cctr_dr = mcctrArray[1];
								}
							}
							// dw_detedit[ii_currformno].setitem(1,"acct_code__dr",macct)
							valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + acct_dr + "]]>")
									.append("</acct_code__dr>");
							// dw_detedit[ii_currformno].setitem(1,"cctr_code__dr",mcctr)
							valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + cctr_dr + "]]>")
									.append("</cctr_code__dr>");

						}

						cctr_cr = genericUtility.getColumnValue("cctr_code__cr", dom);
						acct_cr = genericUtility.getColumnValue("acct_code__cr", dom);
						// end

						if (acct_cr == null || acct_cr.trim().length() == 0) {
							// changes-by monika-17-may-2019
							if (pordType == null || pordType.trim().length() > 0) {

								sql = "select pord_type  from porder where purc_order = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									pordType = checkNull(rs.getString("pord_type"));
									System.out.println("hello product__type:");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							cctr_cr = fincommon.getAcctDetrTtype(itemCode, itemSer, "IN", pordType, siteCode, conn);
							/*
							 * cctr_dr = fincommon.getFromAcctDetr(itemCode, itemSer, "IN", conn);
							 */

							// end

							/*
							 * String[] acct_crStr = cctr_dr.split(","); int len = acct_crStr.length - 1;
							 * System.out.println("@@@cr len[" + len + "]"); if (len > -1) { acct_cr =
							 * acct_crStr[0]; } else { acct_cr = ""; }
							 */
							// changes -made-by monika-22-may-2019
							if (cctr_cr != null && cctr_cr.trim().length() > 0) {
								String ls_cctr_crArray[] = cctr_cr.split(",");
								System.out.println("@@@@@ cctr_drArray.length[" + ls_cctr_crArray.length + "]");
								if (ls_cctr_crArray.length > 0) {
									acct_cr = ls_cctr_crArray[0];
									cctr_cr = "";
								}
								if (ls_cctr_crArray.length > 1) {
									acct_cr = ls_cctr_crArray[0];
									cctr_cr = ls_cctr_crArray[1];
								}

							} // end
						}
						/*
						 * valueXmlString.append("<acct_code__dr >") .append("<![CDATA[" + acct_dr +
						 * "]]>") .append("</acct_code__dr>"); valueXmlString.append("<cctr_code__dr >")
						 * .append("<![CDATA[" + cctr_dr + "]]>") .append("</cctr_code__dr>");
						 */
						valueXmlString.append("<acct_code__cr >").append("<![CDATA[" + acct_cr + "]]>")
								.append("</acct_code__cr>");
						valueXmlString.append("<cctr_code__cr >").append("<![CDATA[" + cctr_cr + "]]>")
								.append("</cctr_code__cr>");
					} else {
						valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + itemCode + "]]>")
								.append("</item_code>");
					}
				} else if (currentColumn.trim().equalsIgnoreCase("item_code")) {
					itemCode = genericUtility.getColumnValue("item_code", dom);
					sql = " select descr,unit,loc_code,pack_code,pack_instr,unit__pur from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						itemDescr = checkNull(rs.getString("descr"));
						unit = checkNull(rs.getString("unit"));
						locCode = checkNull(rs.getString("loc_code"));
						packCode = checkNull(rs.getString("pack_code"));
						packInstr = checkNull(rs.getString("pack_instr"));
						unitPur = checkNull(rs.getString("unit__pur"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_descr >").append("<![CDATA[" + itemDescr + "]]>")
							.append("</item_descr>");
					valueXmlString.append("<unit__std >").append("<![CDATA[" + unit + "]]>").append("</unit__std>");
					valueXmlString.append("<loc_code >").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
					valueXmlString.append("<pack_code >").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
					valueXmlString.append("<pack_instr >").append("<![CDATA[" + packInstr + "]]>")
							.append("</pack_instr>");

					itemSer = genericUtility.getColumnValue("item_ser", dom);

					if (unitPur != null && unitPur.trim().length() > 0) {
						valueXmlString.append("<unit >").append("<![CDATA[" + unitPur + "]]>").append("</unit>");
						valueXmlString.append("<unit__rate >").append("<![CDATA[" + unitPur + "]]>")
								.append("</unit__rate>");
					} else {

						valueXmlString.append("<unit >").append("<![CDATA[" + unit + "]]>").append("</unit>");
						valueXmlString.append("<unit__rate >").append("<![CDATA[" + unit + "]]>")
								.append("</unit__rate>");
						valueXmlString.append("<conv__qty_stduom >").append("1").append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rtuom_stduom >").append("1").append("</conv__rtuom_stduom>");
					}
					valueXmlString.append("<unit__std >").append("<![CDATA[" + unit + "]]>").append("</unit__std>");
					valueXmlString.append("<unit__rate >").append("<![CDATA[" + unit + "]]>").append("</unit__rate>");
					if (unit.equalsIgnoreCase(unitPur))// trim(ls_unit) =
														// trim(ls_unitpur) then
					{
						valueXmlString.append("<conv__qty_stduom >").append("1").append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rtuom_stduom >").append("1").append("</conv__rtuom_stduom>");
					} else {
						quantityList = disscommon.convQtyFactor(unitPur, unit, itemCode, pendqty, conv_qty_stduom,
								conn);
						quantity = Double.parseDouble(quantityList.get(1).toString());
						valueXmlString.append("<conv__rtuom_stduom >").append("1").append("</conv__rtuom_stduom>");

						if (conv_qty_stduom == 0) {
							conv_qty_stduom = 1;
						}
						valueXmlString.append("<conv__rtuom_stduom >")
								.append("<![CDATA[" + (1 / conv_qty_stduom) + "]]>").append("</conv__rtuom_stduom>");
					}
					packCode = genericUtility.getColumnValue("pack_code", dom);

					if (packCode == null || packCode.trim().length() > 0) // isnull(ls_pack)
																			// or
																			// len(trim(ls_pack))
																			// =
																			// 0
																			// then
					{
						valueXmlString.append("<pack_code >").append("<![CDATA[" + packCode + "]]>")
								.append("</pack_code>");
					}
					packInstr = genericUtility.getColumnValue("pack_instr", dom);

					if (packInstr == null || packInstr.trim().length() > 0) // isnull(ls_packinstr)
																			// or
																			// len(trim(ls_packinstr))
																			// =
																			// 0
																			// then
					{
						valueXmlString.append("<pack_instr >").append("<![CDATA[" + packInstr + "]]>")
								.append("</pack_instr>");
					}

					acct_dr = genericUtility.getColumnValue("acct_code__dr", dom);
					cctr_dr = genericUtility.getColumnValue("cctr_code__dr", dom);
					indNo = genericUtility.getColumnValue("ind_no", dom);

					if (indNo == null || indNo.trim().length() == 0) {
						// changes-by-monika-17-may-2019
						pordType = checkNull(genericUtility.getColumnValue("pord_type", dom1));
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom1));
						if (pordType == null || pordType.trim().length() > 0) {

							sql = "select pord_type  from porder where purc_order = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								pordType = checkNull(rs.getString("pord_type"));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						cctr_dr = fincommon.getAcctDetrTtype(itemCode, itemSer, "IN", pordType, siteCode, conn);
						System.out.println("tuesday:" + cctr_dr);
						// COMMENTED BY-Monika
						/*
						 * cctr_dr = fincommon.getFromAcctDetr(itemCode, itemSer, "IN", conn);
						 */
						// end
						String[] cctr_drStr = cctr_dr.split(",");
						int len = cctr_drStr.length - 1;
						System.out.println("@@@cr len[" + len + "]");
						/*
						 * if (len > -1) { acct_dr = cctr_drStr[0]; } else { acct_dr = ""; }
						 * 
						 * if (len > 0) { cctr_dr = cctr_drStr[1]; } else { cctr_dr = ""; }
						 */
						// changes made -by-monika-22-may-2019

						String mcctrArray[] = cctr_dr.split(",");
						System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
						if (mcctrArray.length > 0) {
							acct_dr = mcctrArray[0];
							cctr_dr = "";
						}
						if (mcctrArray.length > 1) {
							acct_dr = mcctrArray[0];
							cctr_dr = mcctrArray[1];
						}

						valueXmlString.append("<acct_code__dr >").append("<![CDATA[" + acct_dr + "]]>")
								.append("</acct_code__dr>");
						valueXmlString.append("<cctr_code__dr >").append("<![CDATA[" + cctr_dr + "]]>")
								.append("</cctr_code__dr>");
					} else {
						sql = " Select acct_code, cctr_code from indent where ind_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, indNo);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							acct_dr = checkNull(rs.getString("acct_code"));
							cctr_dr = checkNull(rs.getString("cctr_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<acct_code__dr >").append("<![CDATA[" + acct_dr + "]]>")
								.append("</acct_code__dr>");
						valueXmlString.append("<cctr_code__dr >").append("<![CDATA[" + cctr_dr + "]]>")
								.append("</cctr_code__dr>");

					}

					itemSer = genericUtility.getColumnValue("item_ser", dom1);
					suppCode = genericUtility.getColumnValue("supp_code", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);

					cctr_cr = genericUtility.getColumnValue("cctr_code__cr", dom);
					acct_cr = genericUtility.getColumnValue("acct_code__cr", dom);

					invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);

					if (invAcct == null || "NULLFOUND".equalsIgnoreCase(invAcct) || invAcct.trim().length() == 0) {
						invAcct = "N";
					}
					// 20-dec-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the
					if ("S".equals(invAcct)) {
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						sql = " select inv_acct from itemser where item_ser = (select item_ser from item where item_code = ?)  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							invAcctSer = checkNull(rs.getString("inv_acct"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (invAcctSer == null || invAcctSer.trim().length() == 0) {
							invAcctSer = "N";
						}
						invAcct = invAcctSer;
					}
					// end 20-dec-2019 manoharan in case INV_ACCT_PORCP = 'S' then to consider the

					invAcctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);

					if (invAcctQc == null || "NULLFOUND".equalsIgnoreCase(invAcctQc)
							|| invAcctQc.trim().length() == 0) {
						invAcctQc = "N";
					}
					if (acct_cr == null || acct_cr.trim().length() == 0) {
						// changes made by monika-21-may-2019

						// changes-by-monika-17-may-2019
						if (pordType == null || pordType.trim().length() > 0) {

							sql = "select pord_type  from porder where purc_order = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								pordType = checkNull(rs.getString("pord_type"));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if ("Y".equalsIgnoreCase(invAcct) && !"Y".equalsIgnoreCase(invAcctQc)) {

							cctr_cr = fincommon.getAcctDetrTtype(itemCode, itemSer, "PORCP", pordType, siteCode, conn);
							System.out.println("tuesday:" + cctr_dr);
							// COMMENTED BY-Monika
							/*
							 * cctr_cr = fincommon.getFromAcctDetr(itemCode, itemSer, "PO", conn);
							 */
							// end

							/*
							 * String[] cctr_crStr = cctr_cr.split(","); int len = cctr_crStr.length - 1;
							 * System.out.println("@@@cr len[" + len + "]");
							 */
							/*
							 * if (len > -1) { acct_cr = cctr_crStr[0]; } else { acct_cr = ""; }
							 * 
							 * if (len > 0) { cctr_cr = cctr_crStr[1]; } else { cctr_cr = ""; }
							 */
							// changes made -by-monika-22-may-2019

							String mcctrArray[] = cctr_cr.split(",");
							System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
							if (mcctrArray.length > 0) {
								acct_cr = mcctrArray[0];
								cctr_cr = "";
							}
							if (mcctrArray.length > 1) {
								acct_cr = mcctrArray[0];
								cctr_cr = mcctrArray[1];
							}
							// end
						} else {
							cctr_cr = fincommon.getAcctDetrTtype(itemCode, itemSer, "PO", pordType, siteCode, conn);
							System.out.println("tuesday:" + cctr_dr);

							String[] cctr_crStr = cctr_cr.split(",");
							int len = cctr_crStr.length - 1;
							System.out.println("@@@cr len[" + len + "]");

							String mcctrArray[] = cctr_cr.split(",");
							System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
							if (mcctrArray.length > 0) {
								acct_cr = mcctrArray[0];
								cctr_cr = "";
							}
							if (mcctrArray.length > 1) {
								acct_cr = mcctrArray[0];
								cctr_cr = mcctrArray[1];
							}
						}
					}

					/*
					 * valueXmlString.append("<acct_code__dr >") .append("<![CDATA[" + acct_dr +
					 * "]]>") .append("</acct_code__dr>"); valueXmlString.append("<cctr_code__dr >")
					 * .append("<![CDATA[" + cctr_dr + "]]>") .append("</cctr_code__dr>");
					 */// commented by MOnika
					valueXmlString.append("<acct_code__cr >").append("<![CDATA[" + acct_cr + "]]>")
							.append("</acct_code__cr>");
					valueXmlString.append("<cctr_code__cr >").append("<![CDATA[" + cctr_cr + "]]>")
							.append("</cctr_code__cr>");

					itemCode = genericUtility.getColumnValue("item_code", dom);
					taxChap = genericUtility.getColumnValue("tax_chap", dom);
					taxClass = genericUtility.getColumnValue("tax_class", dom);
					taxEnv = genericUtility.getColumnValue("tax_env", dom);
					suppCode = genericUtility.getColumnValue("supp_code", dom1);
					siteCode = genericUtility.getColumnValue("site_code", dom);

					if (taxChap == null || taxChap.trim().length() == 0) {
						taxChap = disscommon.getTaxChap(itemCode, itemSer, "S", suppCode, siteCode, conn);
						valueXmlString.append("<tax_chap >").append("<![CDATA[" + taxChap + "]]>")
								.append("</tax_chap>");
					}
					if (taxClass == null || taxClass.trim().length() == 0) // isnull(ls_taxclass)
																			// or
																			// len(trim(ls_taxclass))
																			// =
																			// 0
																			// then
					{
						taxClass = disscommon.getTaxClass("S", suppCode, itemCode, siteCode, conn);
						valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>")
								.append("</tax_class>");
					}
					if (taxEnv == null || taxEnv.trim().length() == 0) {
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						stationFr = genericUtility.getColumnValue("supplier_stan_code", dom);
						stationTo = setDescription("stan_code", "site", "site_code", siteCode, conn);
						taxEnv = disscommon.getTaxEnv(stationFr, stationTo, taxChap, taxClass, siteCode, conn);
						valueXmlString.append("<tax_env >").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");

					}
					// added by ritesh on 02/feb/2015 for request S14ISUN007
					// start
					lastPurcRate = 0d;
					lastPurcPo = "";
					// itemCode = genericUtility.getColumnValue("item_code",
					// dom); //RITESH
					ordDateHdr = checkNull(genericUtility.getColumnValue("ord_date", dom1));
					ordDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDateHdr,
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

					lastPurcRate = getLastPurcRate(itemCode, ordDate, siteCode, conn);
					System.out.println("lastPurcRate ::[ " + lastPurcRate + " ]");
					lastPurcPo = getLastPurcPo(itemCode, ordDate, siteCode, conn);
					System.out.println("lastPurcPo ::[ " + lastPurcPo + " ]");
					valueXmlString.append("<last_purc_rate>").append("<![CDATA[" + lastPurcRate + "]]>")
							.append("</last_purc_rate>");
					valueXmlString.append("<last_purc_po>").append("<![CDATA[" + lastPurcPo + "]]>")
							.append("</last_purc_po>");
					// added by ritesh on 02/feb/2015 for request S14ISUN007 end
				} else if (currentColumn.trim().equalsIgnoreCase("quantity")) {
					String quantityStr = genericUtility.getColumnValue("quantity", dom);
					quantity = quantityStr == null ? 0 : Double.parseDouble(quantityStr);
					unit = genericUtility.getColumnValue("unit", dom);
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);

					String conv_qty_stduomStr = genericUtility.getColumnValue("conv__qty_stduom", dom);
					System.out.println(
							"1 Itemchanged of quantity [" + quantity + "] unit [" + unit + "] unitStd [" + unitStd
									+ "] itemCode [" + itemCode + "] conv_qty_stduomStr [" + conv_qty_stduomStr + "]");
					conv_qty_stduom = conv_qty_stduomStr == null ? 0 : Double.parseDouble(conv_qty_stduomStr);
					convtemp = conv_qty_stduom;
					if ((unitStd == null || unitStd.trim().length() == 0)
							&& (unit == null || unit.trim().length() == 0)) // len(trim(mVal1))
																			// =
																			// 0
																			// then
					{
						unitStd = setDescription("unit", "item", "item_code", itemCode, conn);
						System.out.println("2 Itemchanged of quantity [" + quantity + "] unit [" + unit + "] unitStd ["
								+ unitStd + "] itemCode [" + itemCode + "] conv_qty_stduomStr [" + conv_qty_stduomStr
								+ "]");
						quantityStduomList = disscommon.convQtyFactor(unit, unitStd, itemCode, quantity,
								conv_qty_stduom, conn);
						quantityStduom = Double.parseDouble(quantityStduomList.get(1).toString());
						valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
								.append("</unit__std>");
					} else {
						quantityStduomList = disscommon.convQtyFactor(unit, unitStd, itemCode, quantity,
								conv_qty_stduom, conn);
						quantityStduom = Double.parseDouble(quantityStduomList.get(1).toString());
					}

					if (convtemp == 0) {
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv_qty_stduom + "]]>")
								.append("</conv__qty_stduom>");
					}
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + quantityStduom + "]]>")
							.append("</quantity__stduom>");

				} else if (currentColumn.trim().equalsIgnoreCase("rate")) {

					String rateStr = genericUtility.getColumnValue("rate", dom);
					System.out.println("In Rate itemchange Rate value by dom [" + rateStr + "]");
					rate = rateStr == null ? 0 : Double.parseDouble(rateStr);
					unitRate = genericUtility.getColumnValue("unit__rate", dom);
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					unit = genericUtility.getColumnValue("unit", dom);
					conv_rtuom_stduomStr = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
					conv_rtuom_stduom = conv_rtuom_stduomStr == null ? 0 : Double.parseDouble(conv_rtuom_stduomStr);
					convtemp = conv_rtuom_stduom;
					if (unitRate == null || unitRate.trim().length() == 0) {
						unitStd = setDescription("unit", "item", "item_code", itemCode, conn);
						quantityStduomList = disscommon.convQtyFactor(unitStd, unitRate, itemCode, rate,
								conv_rtuom_stduom, conn);
						rate_stduom = Double.parseDouble(quantityStduomList.get(1).toString());
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>")
								.append("</unit__rate>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
								.append("</unit__std>");
					} else {
						rate_stduomList = disscommon.convQtyFactor(unitStd, unitRate, itemCode, rate, conv_rtuom_stduom,
								conn);
						rate_stduom = Double.parseDouble(rate_stduomList.get(1).toString());
					}
					if (convtemp == 0) {
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv_rtuom_stduom + "]]>")
								.append("</conv__rtuom_stduom>");
					}
					valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate_stduom + "]]>")
							.append("</rate__stduom>");
					valueXmlString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
				} else if (currentColumn.trim().equalsIgnoreCase("conv__rtuom_stduom")) {
					conv_rtuom_stduomStr = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
					unit = genericUtility.getColumnValue("unit", dom);
					unitRate = genericUtility.getColumnValue("unit__rate", dom);
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					String rateStr = genericUtility.getColumnValue("rate", dom);
					rate = rateStr == null ? 0 : Double.parseDouble(rateStr);
					conv_rtuom_stduom = conv_rtuom_stduomStr == null ? 0 : Double.parseDouble(conv_rtuom_stduomStr);
					if (unitRate == null || unitRate.trim().length() == 0) // isnull(mval)
																			// or
																			// len(trim(mVal))
																			// =
																			// 0
																			// then
					{
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + unit + "]]>")
								.append("</unit__rate>");
						unitRate = unit;
					}
					if (unitStd == null || unitStd.trim().length() == 0) {
						unitStd = setDescription("unit", "item", "item_code", itemCode, conn);
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
								.append("</unit__std>");
					}
					rate_stduomList = disscommon.convQtyFactor(unitStd, unitRate, itemCode, rate, conv_rtuom_stduom,
							conn);
					rate_stduom = Double.parseDouble(rate_stduomList.get(1).toString());
					valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate_stduom + "]]>")
							.append("</rate__stduom>");
				} else if (currentColumn.trim().equalsIgnoreCase("unit")) {
					unit = genericUtility.getColumnValue("unit", dom);
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					String quantityStr = genericUtility.getColumnValue("quantity", dom);
					quantity = quantityStr == null ? 0 : Double.parseDouble(quantityStr);
					conv_qty_stduom = 0;
					if (unitStd == null || unitStd.trim().length() == 0) {
						unitStd = setDescription("unit", "item", "item_code", itemCode, conn);
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
								.append("</unit__std>");
					}

					// ld_qty_stduom = gf_conv_qty_fact(mcode, mVal1, itemcode,
					// ld_qty, ld_conv_qty_stduom)
					qty_stduomList = disscommon.convQtyFactor(unit, unitStd, itemCode, quantity, conv_qty_stduom, conn);
					qty_stduom = Double.parseDouble(qty_stduomList.get(1).toString());
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv_qty_stduom + "]]>")
							.append("</conv__qty_stduom>");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qty_stduom + "]]>")
							.append("</quantity__stduom>");
				} else if (currentColumn.trim().equalsIgnoreCase("unit__rate")) {
					unitRate = genericUtility.getColumnValue("unit__rate", dom);
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					String rateStr = genericUtility.getColumnValue("rate", dom);
					rate = rateStr == null ? 0 : Double.parseDouble(rateStr);
					if (unitStd == null || unitStd.trim().length() == 0) {
						unitStd = setDescription("unit", "item", "item_code", itemCode, conn);
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>")
								.append("</unit__std>");
					}
					conv_qty_stduom = 0;
					rate_stduomList = disscommon.convQtyFactor(unitStd, unitRate, itemCode, rate, conv_qty_stduom,
							conn);
					rate_stduom = Double.parseDouble(rate_stduomList.get(1).toString());
					valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv_qty_stduom + "]]>")
							.append("</conv__rtuom_stduom>");
					valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate_stduom + "]]>")
							.append("</rate__stduom>");
				}
				if (currentColumn.trim().equalsIgnoreCase("emp_code__qcaprv")) {
					empCodeQcAprv = genericUtility.getColumnValue("emp_code__qcaprv", dom);
					if (empCodeQcAprv != null && empCodeQcAprv.trim().length() > 0) {
						sql = " select case when employee.emp_fname is null then ' ' else employee.emp_fname end as fname,"
								+ " case when employee.emp_mname is null then ' ' else employee.emp_mname end as mname,"
								+ " case when employee.emp_lname is null then ' ' else employee.emp_lname end as lname"
								+ " from employee where emp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCodeQcAprv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							fname = checkNull(rs.getString(1));
							mname = checkNull(rs.getString(2));
							lname = checkNull(rs.getString(3));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<emp_fname>").append("<![CDATA[" + fname + "]]>").append("</emp_fname>");
						valueXmlString.append("<emp_mname>").append("<![CDATA[" + mname + "]]>").append("</emp_mname>");
						valueXmlString.append("<emp_lname>").append("<![CDATA[" + lname + "]]>").append("</emp_lname>");
					}
				}
				//Modified by Rohini T on 15/04/2021[Start]
				else if(currentColumn.trim().equals("no_art") || currentColumn.trim().equals("dimension"))
				{
					System.out.println("Inside no_art block or dimension block");
					String noArtStr="";

					String reStr="";
					int pos=0;

					itemCode= genericUtility.getColumnValue("item_code", dom);
					dimension=genericUtility.getColumnValue("dimension", dom);
					noArtStr= genericUtility.getColumnValue("no_art", dom);

					System.out.println("item code>>"+itemCode+"\ndimension>>"+dimension+"\nno of articles>>"+noArtStr);


					if(dimension!=null && dimension.trim().length()>0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							unit = rs.getString("UNIT");
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
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
						System.out.println("dimension>>@@"+dimension+"\n no of articles>>"+noArt);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							quantity=discommon.getQuantity(dimension,noArt,unit,conn);

							System.out.println("quantity in dimension block>>"+quantity);
							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity)));
							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("after quantity itemchanged 1877.......@@");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
					}
				}
				//Modified by Rohini T on 15/04/2021[End]
				valueXmlString.append("</Detail2>");
				break;

			// ///////
			case 3:
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail3>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) {
					lineNoOrd = genericUtility.getColumnValue("line_no_ord", dom);
					System.out.println("@@@@@1 lineNoOrd [" + lineNoOrd + "]");
					valueXmlString.append("<line_no_ord protect = \"1\">").append("<![CDATA[" + lineNoOrd + "]]>")
							.append("</line_no_ord>");
				} else if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					amdNo = genericUtility.getColumnValue("amd_no", dom1);
					System.out.println("@@@@@2 amdNo [" + amdNo + "]");
					valueXmlString.append("<amd_no protect = \"1\">").append("<![CDATA[" + amdNo + "]]>")
							.append("</amd_no>");
					purcOrder = genericUtility.getColumnValue("purc_order", dom1);
					System.out.println("@@@@@3 purcOrder [" + purcOrder + "]");
					valueXmlString.append("<purc_order protect = \"1\">").append("<![CDATA[" + purcOrder + "]]>")
							.append("</purc_order>");

				} else if (currentColumn.trim().equalsIgnoreCase("line_no_ord")) {

					lineNoOrd = genericUtility.getColumnValue("line_no_ord", dom);
					System.out.println("@@@@@4 lineNoOrd [" + lineNoOrd + "]");
					if (lineNoOrd != null && lineNoOrd.trim().length() > 0) {
						purcOrder = genericUtility.getColumnValue("purc_order", dom1);
						System.out.println("@@@@@5 purcOrder [" + purcOrder + "]");
						sql = " select term_code, descr, print_opt from pord_term where purc_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							termCode = checkNull(rs.getString("term_code"));
							termDescr = checkNull(rs.getString("descr"));
							printOpt = checkNull(rs.getString("print_opt"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<term_code >").append("<![CDATA[" + termCode + "]]>")
								.append("</term_code>");
						valueXmlString.append("<term_code__o>").append("<![CDATA[" + termCode + "]]>")
								.append("</term_code__o>");
						valueXmlString.append("<descr>").append("<![CDATA[" + termDescr + "]]>").append("</descr>");
						valueXmlString.append("<print_opt>").append("<![CDATA[" + printOpt + "]]>")
								.append("</print_opt>");

					}
				} else if (currentColumn.trim().equalsIgnoreCase("term_code")) {
					purcOrder = genericUtility.getColumnValue("purc_order", dom1);
					termCode = genericUtility.getColumnValue("term_code", dom);
					termCodeO = genericUtility.getColumnValue("term_code__o", dom);
					lineNoOrd = genericUtility.getColumnValue("line_no_ord", dom);
					System.out.println("@@@@@6termCode[" + termCode + "]::termCodeO[" + termCodeO + "]::purcOrder["
							+ purcOrder + "]::lineNoOrd [" + lineNoOrd + "]");
					// Changed by Rupesh on[09/11/20017][Term Description should set from
					// Transaction Table if Term code is same as previouse term code][Start].
					/* if (lineNoOrd != null && lineNoOrd.trim().length() > 0) */
					if ((termCodeO != null || termCodeO.trim().length() > 0)
							&& (termCode != null || termCode.trim().length() > 0) && (termCodeO.equals(termCode)))
					// Changed by Rupesh on[09/11/20017][Term Description should set from
					// Transaction Table if Term code is same as previouse term code][End].
					{
						sql = "	select descr from pord_term where purc_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<descr>").append("<![CDATA[" + descr + "]]>").append("</descr>");
					} else {
						sql = "select descr,input_nos from pur_term where term_code = ? ";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, termCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr = checkNull(rs.getString("descr"));
							inputNos = checkNull(rs.getString("input_nos"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<descr>").append("<![CDATA[" + descr + "]]>").append("</descr>");
					}
				}

				valueXmlString.append("</Detail3>");
				break;

			// case 3 end
			// //

			case 4:

				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail4>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					purcOrder = genericUtility.getColumnValue("purc_order", dom1);
					System.out.println("@@@@@3 purcOrder [" + purcOrder + "]");
					valueXmlString.append("<purc_order>").append("<![CDATA[" + purcOrder + "]]>")
							.append("</purc_order>");

				} else if (currentColumn.trim().equalsIgnoreCase("line_no__ord")) {
					lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
					purcOrder = genericUtility.getColumnValue("purc_order", dom1);
					// value1 = genericUtility.getColumnValue("rel_agnst", dom);
					// System.out.println("LIne Noooooooooo111111111111111 order release
					// against@@@@@@");
					System.out.println("line order@@@@@@@@@@@@@@@@@@@@@@@@@@22" + lineNoOrd);

					// type,relase_amt,release_after,adj_method,acct_code,cctr_code,tax_class,tax_chap,tax_env,rentention_perc,site_code_adv,adjustment_perc,task_code
					System.out.println("purchase order@@@@@@@@@@@@@@@@@@@@@@@@@@22" + purcOrder);
					System.out.println("line order@@@@@@@@@@@@@@@@@@@@@@@@@@22" + lineNoOrd);
					sql = "select line_no__prev,type,rel_agnst,amt_type,rel_amt,rel_after,adj_method,acct_code,cctr_code,tax_class,tax_chap,tax_env,retention_perc,site_code__adv,adj_perc,task_code,allow_override,task_code__parent,apprv_lead_time,remarks from pord_pay_term where purc_order = ? and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					pstmt.setString(2, lineNoOrd);
					rs = pstmt.executeQuery();
					boolean flag = false;
					if (rs.next()) {
						flag = true;
						linenoPrev = checkNull(rs.getString("line_no__prev"));
						type = rs.getString("type");
						relagnst = rs.getString("rel_agnst");
						amttype = rs.getString("amt_type");
						relamt = rs.getDouble("rel_amt");
						relafter = rs.getDouble("rel_after");
						adjmet = rs.getString("adj_method");
						// acctcode =checkNull(rs.getString("acct_code"));
						cctrcode = checkNull(rs.getString("cctr_code"));
						taxClass = checkNull(rs.getString("tax_class"));
						taxChap = checkNull(rs.getString("tax_chap"));
						taxEnv = checkNull(rs.getString("tax_env"));
						retperc = rs.getDouble("retention_perc");
						siteCode = checkNull(rs.getString("site_code__adv"));
						adjustmentperc = rs.getDouble("adj_perc");
						task_code = checkNull(rs.getString("task_code"));
						allowover = checkNull(rs.getString("allow_override"));
						taskcodeParent = checkNull(rs.getString("task_code__parent"));
						apprvlead = rs.getDouble("apprv_lead_time");
						remark = rs.getString("remarks");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (flag) {
						System.out.println("Taskcode trim is:" + task_code.trim());
						System.out.println("Taskcode trim is:" + taskcodeParent.trim());
						valueXmlString.append("<line_no__ord >").append("<![CDATA[" + lineNoOrd + "]]>")
								.append("</line_no__ord>");
						valueXmlString.append("<type >").append("<![CDATA[" + type + "]]>").append("</type>");
						valueXmlString.append("<line_no__prev >").append("<![CDATA[" + linenoPrev + "]]>")
								.append("</line_no__prev>");
						valueXmlString.append("<line_no__prev__o >").append("<![CDATA[" + linenoPrev + "]]>")
								.append("</line_no__prev__o>");
						valueXmlString.append("<amt_type>").append("<![CDATA[" + amttype + "]]>").append("</amt_type>");
						valueXmlString.append("<amt_type__o>").append("<![CDATA[" + amttype + "]]>")
								.append("</amt_type__o>");
						valueXmlString.append("<task_code>").append("<![CDATA[" + task_code.trim() + "]]>")
								.append("</task_code>");
						/*
						 * if (relagnst.equalsIgnoreCase("05")|| relagnst.equalsIgnoreCase("06")) {
						 * System.out.println("Release against@@@@@@"); /*
						 * valueXmlString.append("<item_code protect = \"0\">"
						 * ).append("<![CDATA["+itemCode+"]]>").append( "</item_code>");
						 * 
						 * valueXmlString.append("<task_code protect = \"0\">"). append("<![CDATA[" +
						 * task_code + "]]>").append("</task_code>"); } else {
						 * 
						 * valueXmlString.append("<task_code protect = \"1\">").
						 * append("<![CDATA[]]>").append("</task_code>"); }
						 */
						valueXmlString.append("<rel_agnst >").append("<![CDATA[" + relagnst + "]]>")
								.append("</rel_agnst>");
						// valueXmlString.append("<item_code protect =
						// \"0\">").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
						valueXmlString.append("<rel_agnst__o>").append("<![CDATA[" + relagnst + "]]>")
								.append("</rel_agnst__o>");
						valueXmlString.append("<rel_amt >").append("<![CDATA[" + relamt + "]]>").append("</rel_amt>");
						valueXmlString.append("<rel_amt__o>").append("<![CDATA[" + relamt + "]]>")
								.append("</rel_amt__o>");
						valueXmlString.append("<rel_after>").append("<![CDATA[" + relafter + "]]>")
								.append("</rel_after>");
						valueXmlString.append("<rel_after__o>").append("<![CDATA[" + relafter + "]]>")
								.append("</rel_after__o>");
						valueXmlString.append("<adj_method >").append("<![CDATA[" + adjmet + "]]>")
								.append("</adj_method>");
						valueXmlString.append("<adj_method__o>").append("<![CDATA[" + adjmet + "]]>")
								.append("</adj_method__o>");
						valueXmlString.append("<acct_code >").append("<![CDATA[" + acct_dr + "]]>")
								.append("</acct_code>");
						valueXmlString.append("<acct_code__o>").append("<![CDATA[" + acct_dr + "]]>")
								.append("</acct_code__o>");
						valueXmlString.append("<cctr_code >").append("<![CDATA[" + cctrcode + "]]>")
								.append("</cctr_code>");
						valueXmlString.append("<cctr_code__o>").append("<![CDATA[" + cctrcode + "]]>")
								.append("</cctr_code__o>");
						valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>")
								.append("</tax_class>");
						valueXmlString.append("<tax_class__o>").append("<![CDATA[" + taxClass + "]]>")
								.append("</tax_class__o>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_chap__o>").append("<![CDATA[" + taxChap + "]]>")
								.append("</tax_chap__o>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						valueXmlString.append("<tax_env__o>").append("<![CDATA[" + taxEnv + "]]>")
								.append("</tax_env__o>");
						valueXmlString.append("<retention_perc>").append("<![CDATA[" + retperc + "]]>")
								.append("</retention_perc>");
						valueXmlString.append("<retention_perc__o>").append("<![CDATA[" + retperc + "]]>")
								.append("</retention_perc__o>");
						valueXmlString.append("<site_code__adv >").append("<![CDATA[" + siteCode + "]]>")
								.append("</site_code__adv>");
						valueXmlString.append("<site_code__adv__o>").append("<![CDATA[" + siteCode + "]]>")
								.append("</site_code__adv__o>");
						valueXmlString.append("<adj_perc >").append("<![CDATA[" + adjustmentperc + "]]>")
								.append("</adj_perc>");
						valueXmlString.append("<adj_perc__o >").append("<![CDATA[" + adjustmentperc + "]]>")
								.append("</adj_perc__o>");
						// valueXmlString.append("<task_code >").append("<![CDATA["
						// + task_code + "]]>").append("</task_code>");

						valueXmlString.append("<task_code__o >").append("<![CDATA[" + task_code.trim() + "]]>")
								.append("</task_code__o>");
						valueXmlString.append("<task_code__parent >")
								.append("<![CDATA[" + taskcodeParent.trim() + "]]>").append("</task_code__parent>");
						valueXmlString.append("<task_code__parent__o >")
								.append("<![CDATA[" + taskcodeParent.trim() + "]]>").append("</task_code__parent__o>");
						valueXmlString.append("<apprv_lead_time >").append("<![CDATA[" + apprvlead + "]]>")
								.append("</apprv_lead_time>");
						valueXmlString.append("<apprv_lead_time__o >").append("<![CDATA[" + apprvlead + "]]>")
								.append("</apprv_lead_time__o>");
						valueXmlString.append("<allow_override >").append("<![CDATA[" + allowover + "]]>")
								.append("</allow_override>");
						valueXmlString.append("<allow_override__o >").append("<![CDATA[" + allowover + "]]>")
								.append("</allow_override__o>");

					}
				}
				/*
				 * else if (currentColumn.trim().equalsIgnoreCase("rel_agnst")) {
				 * 
				 * value = genericUtility.getColumnValue("rel_agnst", dom);
				 * task_code=genericUtility.getColumnValue("task_code", dom);
				 * System.out.println("Release against#####@@@@@@"+value); System
				 * .out.println("Task code Release against!!!!!!!!!!@@@@@@" +task_code);
				 * 
				 * if (value.equalsIgnoreCase("05")|| value.equalsIgnoreCase("06")) {
				 * System.out.println("Release against@@@@@@"); /*valueXmlString.
				 * append("<item_code protect = \"0\">").append(
				 * "<![CDATA["+itemCode+"]]>").append("</item_code>"); valueXmlString
				 * .append("<task_code protect = \"0\">").append("<![CDATA[" + task_code +
				 * "]]>").append("</task_code>"); } else {
				 * 
				 * valueXmlString.append("<task_code protect = \"1\">").append(
				 * "<![CDATA[]]>").append("</task_code>"); } } else if
				 * (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) { value =
				 * genericUtility.getColumnValue("rel_agnst", dom);
				 * task_code=genericUtility.getColumnValue("task_code", dom); System
				 * .out.println("Release against item default edit#####@@@@@@" +value);
				 * System.out.println("Task code itm default edit!!!!!!!!!!@@@@@@" +task_code);
				 * if (value.equalsIgnoreCase("05")|| value.equalsIgnoreCase("06")) {
				 * valueXmlString.append("<task_code protect = \"0\">" ).append("<![CDATA[" +
				 * task_code + "]]>").append("</task_code>"); } else { valueXmlString.append(
				 * "<task_code protect = \"1\">").append("<![CDATA[]]>"
				 * ).append("</task_code>"); } }
				 */
				valueXmlString.append("</Detail4>");
				break;
			// //
			case 5:
				/*
				 * parentNodeList = dom.getElementsByTagName("Detail3"); parentNode =
				 * parentNodeList.item(0); childNodeList = parentNode.getChildNodes(); ctr = 0;
				 * valueXmlString.append("<Detail3>"); childNodeListLength =
				 * childNodeList.getLength(); do { childNode = childNodeList.item(ctr);
				 * childNodeName = childNode.getNodeName();
				 * if(childNodeName.equals(currentColumn)) { if (childNode.getFirstChild()!=
				 * null) { columnValue = childNode.getFirstChild().getNodeValue().trim(); } }
				 * ctr++; } while(ctr < childNodeListLength &&
				 * !childNodeName.equals(currentColumn)); System.out.println("[" + currentColumn
				 * + "] ==> '" + columnValue + "'");
				 */
				parentNodeList = dom.getElementsByTagName("Detail5");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail5>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					purcOrder = genericUtility.getColumnValue("purc_order", dom1);
					System.out.println("@@@@@3 purcOrder [" + purcOrder + "]");
					valueXmlString.append("<purc_order>").append("<![CDATA[" + purcOrder + "]]>")
							.append("</purc_order>");

				} else if (currentColumn.trim().equalsIgnoreCase("line_no__ord")) {
					lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
					System.out.println("line order@@@@@@@@@@@@@@@@@@@@@@@@@@22" + lineNoOrd);
					purcOrder = genericUtility.getColumnValue("purc_order", dom1);
					System.out.println("purcOrder@@@@@@@@@@@" + purcOrder);
					sql = "select ref_code, min_day,max_day,fin_chg,fchg_type,min_amt,max_amt from pord_dlv_term where purc_order = ? and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					pstmt.setString(2, lineNoOrd);
					rs = pstmt.executeQuery();

					if (rs.next()) {
						refcode = rs.getString("ref_code");
						minday = rs.getDouble("min_day");
						maxday = rs.getDouble("max_day");
						finchg = rs.getDouble("fin_chg");
						fchgtype = rs.getString("fchg_type");
						minamt = rs.getDouble("min_amt");
						maxamt = rs.getDouble("max_amt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<line_no__ord >").append("<![CDATA[" + lineNoOrd + "]]>")
							.append("</line_no__ord>");
					valueXmlString.append("<ref_code >").append("<![CDATA[" + refcode + "]]>").append("</ref_code>");
					valueXmlString.append("<min_day >").append("<![CDATA[" + minday + "]]>").append("</min_day>");
					valueXmlString.append("<min_day__o >").append("<![CDATA[" + minday + "]]>").append("</min_day__o>");
					valueXmlString.append("<max_day >").append("<![CDATA[" + maxday + "]]>").append("</max_day>");
					valueXmlString.append("<max_day__o >").append("<![CDATA[" + maxday + "]]>").append("</max_day__o>");
					valueXmlString.append("<fin_chg >").append("<![CDATA[" + finchg + "]]>").append("</fin_chg>");
					valueXmlString.append("<fin_chg__o>").append("<![CDATA[" + finchg + "]]>").append("</fin_chg__o>");
					valueXmlString.append("<fchg_type >").append("<![CDATA[" + fchgtype + "]]>").append("</fchg_type>");
					valueXmlString.append("<fchg_type__o>").append("<![CDATA[" + fchgtype + "]]>")
							.append("</fchg_type__o>");
					valueXmlString.append("<min_amt >").append("<![CDATA[" + minamt + "]]>").append("</min_amt>");
					valueXmlString.append("<min_amt__o>").append("<![CDATA[" + minamt + "]]>").append("</min_amt__o>");
					valueXmlString.append("<max_amt >").append("<![CDATA[" + maxamt + "]]>").append("</max_amt>");
					valueXmlString.append("<max_amt__o>").append("<![CDATA[" + maxamt + "]]>").append("</max_amt__o>");

				}
				valueXmlString.append("</Detail5>");
				break;
			}
			valueXmlString.append("</Root>");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;

					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}

	private String setDescription(String descr, String table, String field, String value, Connection conn)
			throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		System.out.println("@@@@@@@@table[" + table + "]:::field[" + field + "]::value[" + value + "]");
		sql = "select " + descr + " from " + table + " where " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			descr = checkNull(rs.getString(1));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		System.out.print("========>::descr[" + descr + "]");
		return descr;
	}

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input;
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
				msgType = checkNull(rs.getString("MSG_TYPE"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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

	private double getLastPurcRate(String itemCode, Timestamp ordDate, String siteCode, Connection conn) {
		double lastPurRate = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		try {
			sql = "SELECT DDF_GET_LASTPURRATE(?,?,?) FROM DUAL ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setTimestamp(2, ordDate);
			pstmt.setString(3, siteCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				lastPurRate = rs.getDouble(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
		return lastPurRate;
	}

	private String getLastPurcPo(String itemCode, Timestamp ordDate, String siteCode, Connection conn) {
		String lastPurPo = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		try {
			sql = "SELECT DDF_GET_PO(?,?,?) FROM DUAL ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setTimestamp(2, ordDate);
			pstmt.setString(3, siteCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				lastPurPo = rs.getString(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
		return lastPurPo;
	}

	private boolean ispurcOrderDom(Document dom, String porderNo) throws ITMException {
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;

		String porderNoDom = "";
		boolean isporderExist = false;
		String refNoDom = "", refSerDom = "";
		System.out.println("---inside ispurcOrderDom--");
		try {

			parentList = dom.getElementsByTagName("Detail2");
			int parentNodeListLength = parentList.getLength();
			System.out.println("parentNodeListLength>>>>" + parentNodeListLength);
			if (parentNodeListLength == 0) {
				isporderExist = true;
			}
			for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr--) {
				parentNode = parentList.item(prntCtr - 1);
				childList = parentNode.getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++) {
					childNode = childList.item(ctr);

					if (childNode != null && childNode.getNodeName().equalsIgnoreCase("attribute")) {
						String updateFlag = "";
						updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
						System.out.println("updateFlag>>>>" + updateFlag);
						if (updateFlag.equalsIgnoreCase("D")) {
							System.out.println("Break from here as the record is deleted");
							isporderExist = true;
							// break;
						}
					}

					if (childNode != null && childNode.getFirstChild() != null
							&& childNode.getNodeName().equalsIgnoreCase("purc_order")) {
						porderNoDom = childNode.getFirstChild().getNodeValue().trim();
					}

				}
				if (porderNo.trim().equalsIgnoreCase(porderNoDom.trim())) {
					isporderExist = true;
					break;
				}

			} // for loop

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("isporderExist>>>>>> [" + isporderExist + "]");
		return isporderExist;
	}
	//Modified by Rohini T on 15/04/2021[Start]
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
	//Modified by Rohini T on 15/04/2021[End]
}