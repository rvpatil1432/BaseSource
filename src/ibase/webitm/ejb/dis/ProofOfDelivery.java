package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

@Stateless
public class ProofOfDelivery extends ValidatorEJB implements
		ProofOfDeliveryLocal, ProofOfDeliveryRemote {
	String str, confirmedInv = "";
	boolean isError = false;
	double partialQty = 0, actaulInvQty = 0, remainQty = 0;
	int lineNoTrace = 0;
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public static ProofOfDelivery getInstance() {
		return new ProofOfDelivery();
	}

	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		System.out
				.println("------------ wfvalData method called-----------------");
		System.out.println("xtraParams --->>> [[" + xtraParams + " ]]");
		System.out.println("xmlString --->>>  [[" + xmlString + "  ]]");
		System.out.println("xmlString1 --->>> [[" + xmlString1 + " ]]");
		System.out.println("xmlString2 --->>> [[" + xmlString2 + " ]]");
		System.out.println("editFlag  --->>>  [[" + editFlag + "   ]]");

		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);

		} catch (Exception e) {
			System.out
					.println("Exception : ProofOfDelivery.java : wfValData(String xmlString) : ==>\n"
							+ e.getMessage());
			throw new ITMException(e);
		}

		return errString;
	} // end of wfValData

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
		isError = false;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String childNodeName = null;
		String errString = "", errorType = "", loginSite = "", status = "", invoiceID = "", sreturnTranID = "", sql1 = "", userId = "";
		String errCode = "", updateStatus = "", lineNoTrace = "", lineNoTrace1 = "";
		Connection conn = null;
		PreparedStatement pstmt;
		ResultSet rs = null;
		int cnt = 0, cntItem = 0;
		int currentFormNo = 0, miscCount = 0;
		int childNodeListLength;
		ConnDriver connDriver = new ConnDriver();
		// ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		StringBuffer errStringXml = new StringBuffer(
				"<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date newBillDate = new java.util.Date();
		java.util.Date newInvDate = new java.util.Date();
		// java.sql.Date newInvDate=null;
		// java.sql.Date wDate=null;
		try {
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,
					"loginCode");
			System.out.println("user ID form XtraParam : " + userId
					+ "Edit Flag -->>: " + editFlag);

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
					System.out.println("Child name --->> " + childNodeName);

					if (childNodeName.equalsIgnoreCase("invoice_id")) {
						String invoiceConf = "";
						invoiceID = genericUtility.getColumnValue("invoice_id",
								dom);
						str = invoiceID;
						System.out.println("-----invoice id : " + invoiceID
								+ " str--->" + str);
						if (invoiceID == null || invoiceID.trim().length() == 0) {
							errCode = "VTINVIDNN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							cnt = getDBRowCount(conn, "invoice", "invoice_id",
									invoiceID);
							int cnt1 = getDBRowCount(conn, "spl_sales_por_hdr",
									"invoice_id", invoiceID);
							invoiceConf = getNameOrDescrForCode(conn,
									"invoice", "confirmed", "invoice_id",
									invoiceID);
							System.out
									.println("invoice Confirmed Status--------->> : "
											+ invoiceConf);
							String conf = getNameOrDescrForCode(conn,
									"spl_sales_por_hdr", "confirmed",
									"invoice_id", invoiceID);
							conf = conf == null ? "" : conf.trim();
							if (invoiceConf.trim().equalsIgnoreCase("N")) {
								// If invoice Id not confirmed in invoice master
								// then show error
								errCode = "VTINVNCON";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							status = getNameOrDescrForCode(conn,
									"spl_sales_por_hdr", "wf_status",
									"invoice_id", invoiceID);
							status = status == null ? "" : status.trim();
							System.out.println("Status for reject2 ---->>$$$$["
									+ status + "]");
							// check if selected invoice id already generated
							// debit note
							if (isRecordPresentInMiscDrCrRcp(conn, invoiceID)) {
								errCode = "VTPODARI"; // Proof of Delivery
								// already received for
								// this invoice ID
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// end check

							if (!(editFlag.equals("E"))) {
								if (!(status.equals("R"))) {

									if (cnt1 > 0
											&& (!isPartialyInvoiceChk(conn,
													invoiceID, "POD"))) {
										// If POD for invoiceID already exist in
										// spl_sales_por_hdr then show error
										errCode = "VTINVIDAC";
										errList.add(errCode);
										errFields.add(childNodeName
												.toLowerCase());
									} else if (isPartialyInvoiceChkHdr(conn,
											invoiceID)) {
										// system has unconfirmed POD
										// transaction for this invoice.
										System.out
												.println("--------Unconfirmed POD transaction--------");
										errCode = "VTINVDUNF";
										errList.add(errCode);
										errFields.add(childNodeName
												.toLowerCase());
									}
								}
							}

							if (cnt == 0) {
								errCode = "VTINVIDNE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("site_code")) {
						String siteCode = "", invId = "", invSite = "";
						int count = 0;
						siteCode = genericUtility.getColumnValue("site_code",
								dom);
						invId = genericUtility
								.getColumnValue("invoice_id", dom);
						siteCode = siteCode == null ? "" : siteCode.trim();
						invId = invId == null ? "" : invId.trim();
						if (siteCode.length() == 0) {
							errCode = "VTSITECNE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							System.out
									.println("----in else part of site_code-----------");
							count = getDBRowCount(conn, "site", "site_code",
									siteCode);
							if (count == 0) {
								errCode = "VTSITENEX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// check if entered site code not in invoice
							invSite = getColumnDescr(conn, "site_code",
									"invoice", "invoice_id", invId);
							invSite = invSite == null ? "" : invSite.trim();
							System.out.println("Site_code ----->>[" + siteCode
									+ "]");
							System.out.println("Site_code invID TRUE----->>["
									+ invSite + "]");
							if (!(invSite.equalsIgnoreCase(siteCode))) {
								errCode = "VTSITENEI"; // site code not match
								// in
								// invoice;
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					if (childNodeName.equalsIgnoreCase("cust_code")) {
						String custCode = "", invId = "", custCodeInv = "";
						int count = 0;
						custCode = genericUtility.getColumnValue("cust_code",
								dom);
						invId = genericUtility
								.getColumnValue("invoice_id", dom);
						custCode = custCode == null ? "" : custCode.trim();
						invId = invId == null ? "" : invId.trim();
						if (custCode.length() == 0) {
							errCode = "VTCUSTCNE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							count = getDBRowCount(conn, "customer",
									"cust_code", custCode);
							if (count == 0) {
								errCode = "VTCUSTNEX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// check if entered customer code bill not in
							// invoice
							custCodeInv = getColumnDescr(conn,
									"cust_code__bil", "invoice", "invoice_id",
									invId);
							custCodeInv = custCodeInv == null ? ""
									: custCodeInv.trim();
							System.out.println("cust_code bill invID----->>["
									+ custCodeInv + "]");
							if (!(custCodeInv.equalsIgnoreCase(custCode))) {
								errCode = "VTCUSTNEI"; // Customer code bill
								// not
								// match in invoice;
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					/*
					 * if (childNodeName.equalsIgnoreCase("cust_code__solto")) {
					 * String custCodeSolTo = "", invId = "", custCodeInv = "";
					 * int count = 0; custCodeSolTo =
					 * genericUtility.getColumnValue( "cust_code__solto", dom);
					 * custCodeSolTo = custCodeSolTo == null ? "" :
					 * custCodeSolTo.trim(); invId = invId == null ? "" :
					 * invId.trim(); if (custCodeSolTo.length() == 0) { errCode =
					 * "VTCUSTCNE"; errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase()); } else {
					 * count = getDBRowCount(conn, "customer", "cust_code",
					 * custCodeSolTo); if (count == 0) { errCode = "VTCUSTNEX";
					 * errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase()); } } }
					 */

					if (childNodeName.equalsIgnoreCase("wf_status")) {

						String wfStatus = "";
						String rejectRemarkStr = "";
						wfStatus = genericUtility.getColumnValue("wf_status",
								dom);
						rejectRemarkStr = genericUtility.getColumnValue(
								"spl_sales_por_hdr_remarks", dom);
						rejectRemarkStr = rejectRemarkStr == null ? ""
								: rejectRemarkStr.trim();
						System.out.println("Rejection Remark in wf_status>"
								+ rejectRemarkStr + "<==!");
						wfStatus = wfStatus == null ? "" : wfStatus.trim();
						if (wfStatus.equalsIgnoreCase("R")
								&& editFlag.equalsIgnoreCase("A")) {
							errCode = "VTREJNOTA";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (wfStatus.equalsIgnoreCase("R")
								&& (rejectRemarkStr == null || rejectRemarkStr
										.isEmpty())) {
							errCode = "VTREJNOTAB";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						} else if (!wfStatus.equalsIgnoreCase("R")
								&& !(rejectRemarkStr == null || rejectRemarkStr
										.isEmpty())) { // (rejectRemarkStr.isEmpty()
							// ||
							// rejectRemarkStr.length()>0
							// ||
							// rejectRemarkStr==""
							// ||
							// rejectRemarkStr==null)){
							System.out.println("in else-if Rejection =>"
									+ rejectRemarkStr + "<==!");
							errCode = "VTREJNOTAC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} // end for loop
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("quantity__resale")) {
						String qtyResale = "", qtyInv = "", invoiceId = "", itemCodeSret = "", pndQty = "";
						double qtyResalD = 0.0;
						qtyResale = genericUtility.getColumnValue(
								"quantity__resale", dom);
						qtyInv = genericUtility.getColumnValue("quantity__inv",
								dom);
						pndQty = genericUtility.getColumnValue("pend_qty", dom);

						itemCodeSret = genericUtility.getColumnValue(
								"item_code", dom);
						itemCodeSret = itemCodeSret == null ? "" : itemCodeSret
								.trim();
						System.out.println("itemCodeSret--->>[" + itemCodeSret
								+ "]");

						try {
							qtyInv = qtyInv == null ? "0.0" : qtyInv;
							pndQty = pndQty == null ? "0.0" : pndQty;

							System.out.println("quantity__resale---->>"
									+ qtyResale);
							qtyResale = checkSpaceNull(qtyResale); // qtyResale
							// == null ?
							// "0.0" :
							// qtyResale;
							qtyResalD = Double
									.parseDouble(checkSpaceNull(qtyResale));
							double pndQtyVal = Double
									.parseDouble(checkSpaceNull(pndQty));

							System.out
									.println("qtyInv---->>" + qtyInv
											+ " qtyResaled after parsing :"
											+ qtyResalD);
							System.out.println("pndQtyVal----->>"+pndQtyVal);

							/*
							 * [17-JUL-15] qtyResalD <= 0 Added By Sanket Girme
							 * [CCF#CQ15/CS1019] Point #8
							 */

							if (qtyResale == null
									|| qtyResale.trim().length() == 0
									|| qtyResalD <= 0 || qtyResalD > pndQtyVal) {
								errCode = "VTPODQNN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								System.out.println("------IN ELSE-----------");
								String tranID = "";
								double quantyInvD = 0.0, podInvD = 0.0, totalInvQty = 0.0;
								double qtyInvD = 0.0, qtyResaleD = 0.0;
								qtyInvD = Double
										.parseDouble(checkSpaceNull(qtyInv));
								qtyResaleD = Double.parseDouble(qtyResale);
								PreparedStatement pstmt1 = null;
								ResultSet rs1 = null;
								System.out
										.println("invoice Id dom---->>" + str);
								sreturnTranID = getSalesReturnTranID(conn,
										"sreturn", "tran_id", "invoice_id", str);
								sreturnTranID = sreturnTranID == null ? ""
										: sreturnTranID.trim();
								System.out.println("sreturnTranID --------->>["
										+ sreturnTranID + "]");
								if (sreturnTranID.length() > 0) {
									String sreturnItemCode = "";
									double sreturnQuantity = 0.0;
									sql1 = "select item_code,quantity from sreturndet where tran_id = ?";
									pstmt = conn.prepareStatement(sql1);
									pstmt.setString(1, sreturnTranID);
									rs = pstmt.executeQuery();
									while (rs.next()) {
										sreturnItemCode = rs.getString(1);
										sreturnQuantity = rs.getDouble(2);
										System.out
												.println("sreturnItemCode -->["
														+ sreturnItemCode
														+ "] sreturnQuantity -->["
														+ sreturnQuantity + "]");
										if (sreturnItemCode.trim()
												.equalsIgnoreCase(itemCodeSret)) {
											qtyInvD = qtyInvD - sreturnQuantity;
											// qtyResalD=qtyResalD+isTotalAmountPodQtyGrt(conn,str,qtyInvD);
											System.out
													.println("POD qty with new entry value---------->>"
															+ qtyResalD);
											System.out
													.println("Actual quantity after sales return---->>["
															+ qtyInvD + "]");
											if (isTotalAmountPodQtyGrt(conn,
													str, qtyInvD, qtyResalD,
													itemCodeSret)) // VTSLRETER
											{
												// Invoice quantity for item(s)
												// is returned.Please check
												// sales return.
												errCode = "VTSLRETER";
												errList.add(errCode);
												errFields.add(childNodeName
														.toLowerCase());
											}
										}
									} // end while

								}
								System.out
										.println("in isPartialyInvoiceChk condition------------ ");
								// sql1="select tran_id from spl_sales_por_hdr
								// where invoice_id = ?";
								// totalInvQty=isTotalAmountPodQtyGrt(conn,str,qtyInvD);
								// totalInvQty = totalInvQty+ qtyResalD;
								System.out.println("qtyInvD---->> " + qtyInvD
										+ "  qtyResaleD : " + qtyResaleD);
								System.out.println("Final totalInvQty ----->"
										+ totalInvQty);
								if (isTotalAmountPodQtyGrt(conn, str, qtyInvD,
										qtyResaleD, itemCodeSret)) {
									System.out
											.println("the total qty condition true---------");
									errCode = "VTPODGRT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if (qtyResaleD > qtyInvD) {
									// POD quantity can not be greater than
									// invoice quantity
									errCode = "VTPODGRT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						} catch (Exception e) {
							System.out
									.println("Exception : quantity__resale column (Detail) ");
							e.printStackTrace();
						}
					}
					if (childNodeName.equalsIgnoreCase("rate__resale")) {
						String rateResale = "";
						double rateResaleD = 0.0;
						rateResale = genericUtility.getColumnValue(
								"rate__resale", dom);
						rateResale = rateResale == null ? "0.0" : rateResale
								.trim();
						rateResaleD = Double
								.parseDouble(checkSpaceNull(rateResale));
						System.out.println("rateResaleD------->> "
								+ rateResaleD);
						if (rateResale == null || rateResale.length() == 0
								|| rateResaleD <= 0) {
							errCode = "VTRSALENN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("item_code")) {
						String itemCode = "", regulatedPrice = "", itemActive = "";
						itemCode = genericUtility.getColumnValue("item_code",
								dom);
						itemCode = itemCode == null ? "" : itemCode.trim();
						System.out.println("itemCode------->> " + itemCode);
						if (itemCode.length() > 0) {
							regulatedPrice = getColumnDescr(conn,
									"regulated_price", "item", "item_code",
									itemCode);
							itemActive = getColumnDescr(conn, "active", "item",
									"item_code", itemCode);
							itemActive = itemActive == null ? "" : itemActive
									.trim();
							regulatedPrice = regulatedPrice == null ? ""
									: regulatedPrice.trim();

							if (regulatedPrice.length() < 0) {
								errCode = "VTREGPRCN"; // regulated price not
								// define in item master
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if (itemActive.length() < 0
									|| itemActive.equalsIgnoreCase("N")) {
								errCode = "VTITMCNAC"; // item code not active
								// in item master
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("aprv_rate")) {
						String apprvRate = "", rateResale = "";
						double aprvRateD = 0, rateResaleD = 0;
						apprvRate = genericUtility.getColumnValue("aprv_rate",
								dom);
						rateResale = genericUtility.getColumnValue(
								"rate__resale", dom);
						apprvRate = apprvRate == null ? "0" : apprvRate.trim();
						rateResale = rateResale == null ? "0" : rateResale
								.trim();

						aprvRateD = Double.parseDouble(apprvRate);
						rateResaleD = Double.parseDouble(rateResale);
						System.out.println("Approve rate123---->>>["
								+ aprvRateD + "]");
						System.out.println("rateResaleD---->>>[" + rateResaleD
								+ "]");

						/*
						 * if(aprvRateD > rateResaleD) { errCode = "VTAPRNGRR"; //
						 * approved rate should not be greater than End sale
						 * rate. errList.add( errCode ); errFields.add(
						 * childNodeName.toLowerCase() ); }
						 */

						if (aprvRateD <= 0) {
							errCode = "VTAPRRIDN"; // Approved rate not
							// available.Please check
							errList.add(errCode);// regulated rate in item
							// master or var name in
							// disparm
							errFields.add(childNodeName.toLowerCase());
						}

					}
					if (childNodeName.equalsIgnoreCase("line_no__trace")) {
						lineNoTrace = genericUtility.getColumnValue(
								"line_no__trace", dom2);
						System.out.println("Current Line no trace &&&&:-["
								+ lineNoTrace + "]");
						if (lineNoTrace == null
								|| lineNoTrace.trim().length() <= 0) {
							errCode = "VTBLNKLNTR";// Duplicate line_no__trace
							// for Sales Return.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							int length = dom2.getElementsByTagName("Detail2")
									.getLength();
							System.out.println("length  $$$$$$$ =" + length);
							for (int i = 0; i < dom2.getElementsByTagName(
									"Detail2").getLength() - 1; i++) {
								updateStatus = checkNull(getCurrentUpdateFlag(dom2
										.getElementsByTagName("Detail2")
										.item(i)));
								System.out
										.println("Update Status $$$$$$$$$$:-["
												+ updateStatus + "]");
								if (!updateStatus.equalsIgnoreCase("D")
										&& length > 1) {
									System.out.println("Enter in IF ");
									lineNoTrace1 = checkNull(
											genericUtility
													.getColumnValueFromNode(
															"line_no__trace",
															dom2
																	.getElementsByTagName(
																			"Detail2")
																	.item(i)))
											.trim();
									System.out
											.println("Current Line no trace In loop &&&&:-["
													+ lineNoTrace1 + "]");
									System.out.println("itemCode1======>"
											+ lineNoTrace1);
									if (lineNoTrace
											.equalsIgnoreCase(lineNoTrace1)) {
										cntItem++;
										System.out.println("cntItem=="
												+ cntItem);
									}
								}
								if (cntItem > 0) {
									errCode = "VTDUPILNTR";// Duplicate
									// line_no__trace for
									// Sales Return.
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("bill_no")) {
						String billNo = "";
						billNo = genericUtility.getColumnValue("bill_no", dom);
						billNo = billNo == null ? "" : billNo.trim();
						System.out
								.println("Bill no--------->>[" + billNo + "]");
						if (billNo.length() == 0) {
							errCode = "VTBILNNN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("remarks")) {
						String remarks = "";
						remarks = checkNull(genericUtility.getColumnValue(
								"remarks", dom));
						System.out
								.println("remarks-------->>[" + remarks + "]");
						if (remarks.length() > 100) {
							errCode = "VTRMKSG"; // Remarks length exceed
							// than
							// specified.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					if (childNodeName.equalsIgnoreCase("bil_date")) {
						String billDate = "", billNo = "", sql = "", invID = "", custCodeCurr = "", invoiceDate = "", tranId = "";
						boolean isDateAft = false;
						Timestamp invDateT = null;
						int diffDays = 0;
						invID = genericUtility.getColumnValue("invoice_id",
								dom1);
						custCodeCurr = genericUtility.getColumnValue(
								"cust_code", dom1);
						billDate = genericUtility.getColumnValue("bil_date",
								dom);
						billNo = genericUtility.getColumnValue("bill_no", dom);
						invID = invID == null ? "" : invID.trim();
						billDate = billDate == null ? "" : billDate.trim();
						billNo = billNo == null ? "" : billNo.trim();
						custCodeCurr = custCodeCurr == null ? "" : custCodeCurr
								.trim();

						System.out.println("customer Code Current--------->>["
								+ custCodeCurr + "]");
						if (billDate.length() == 0) {
							errCode = "VTBILDNN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							if (invID.length() > 0 && billDate.length() > 0) {
								sql = "select conf_date from invoice where invoice_id = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, invID);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									invDateT = rs.getTimestamp(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (invDateT != null) {
									SimpleDateFormat sdf = new SimpleDateFormat(
											genericUtility.getApplDateFormat());
									invoiceDate = sdf.format(invDateT);
									// newInvDate=new
									// java.sql.Date(invDateT.getTime());;
									// billDate=sdf.format(billDate);
									newInvDate = sourceFormat
											.parse(invoiceDate);
									newBillDate = sourceFormat.parse(billDate);
									System.out
											.print("stkBillDate121------->>>>>>>>>>>["
													+ billDate + "]");
									System.out
											.print("invoice confDate ------->>>>>>>>>>>["
													+ invoiceDate + "]");

									// sql = "select to_date( ?,'dd-mm-yyyy') -
									// to_date( ?,'dd-mm-yyyy') from dual";
									// pstmt = conn.prepareStatement(sql);
									// pstmt.setString(1, billDate);
									// pstmt.setString(2, invoiceDate);
									// rs = pstmt.executeQuery();
									// if (rs.next())
									// {
									// diffDays = rs.getInt(1);
									// }
									// rs.close();
									// rs = null;
									// pstmt.close();
									// pstmt = null;

									// diffDays=(int)countDaysBetween(wDate,todayDate);
									// diffDays=(int)countDaysBetween(newInvDate,newBillDate);
									// //VALLABH KADAM 24/DEC/14 get diffrence
									// between DAYS
									diffDays = (int) countDaysBetween(
											newInvDate, newBillDate); // VALLABH
									// KADAM
									// 24/DEC/14
									// get
									// diffrence
									// between
									// DAYS

									System.out
											.println("Difference in days for validation--->>["
													+ diffDays + "]");
									editFlag = editFlag == null ? "" : editFlag
											.trim();
									String currTranId = genericUtility
											.getColumnValue("tran_id", dom1);
									currTranId = currTranId == null ? ""
											: currTranId.trim();
									System.out.println("currTranId--->>["
											+ currTranId + "]");
									System.out.println("Edit flag133BD--->>["
											+ editFlag + "]");

									/*
									 * //comment on 26/06/15 by cpatil for
									 * remove detail 2 validation of bill no if
									 * (isBillNoRepeatedInDetail2(dom, dom2,
									 * billNo, billDate, currTranId,conn)) {
									 * System.out.println("------bill no
									 * repeated in detail2---------"); errCode =
									 * "VTBLBDNRP"; errList.add(errCode);
									 * errFields.add(childNodeName.toLowerCase()); }
									 */

									if (isBillNoRepeatedInYear(conn, billNo,
											billDate, custCodeCurr, currTranId,
											xtraParams)) {
										System.out
												.println("------bill and stockist and year matched-----------");
										errCode = "VTBLBDNRP";
										errList.add(errCode);
										errFields.add(childNodeName
												.toLowerCase());
									}
									if (diffDays < 0) {
										errCode = "VTINDLSTD";
										errList.add(errCode);
										errFields.add(childNodeName
												.toLowerCase());
									}
								}
							}
						} // end else

					}

				}
				break;

			} // end switch

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
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString
								.indexOf("<Errors>") + 8, errString
								.indexOf("<trace>"));
						bifurErrString = bifurErrString
								+ errString.substring(errString
										.indexOf("</trace>") + 8, errString
										.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."
								+ errStringXml);
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

				errStringXml.append("</Errors></Root>\r\n");
			} else {
				errStringXml = new StringBuffer("");
			}

		} // end try
		catch (SQLException se) {
			System.out.println("SQLException ::" + se);
			se.printStackTrace();
			throw new ITMException(se);
		} catch (Exception e) {
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
				if (connDriver != null) {
					connDriver = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (isError) {
					return itmDBAccessEJB.getErrorString("", "VTTNSVD", "", "",
							conn);
				}

			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		System.out.println("ErrString ::[ " + errStringXml.toString() + " ]");
		return errStringXml.toString();

	}

	private boolean isBillNoRepeatedInDetail2(Document dom, Document dom2,
			String billNo, String billDate, String currTranId, Connection conn)
			throws Exception {
		System.out
				.println("@@@@@@ isBillNoRepeatedInDetail2 () called.........");
		String errString = "", lineNoCurrent = "", updateStatus = "";
		int ctr1 = 0, k = 0, l = 1, noOfParent = 0;

		NodeList detail2List = dom2.getElementsByTagName("Detail2");
		NodeList Currentdetail2List = dom.getElementsByTagName("Detail2");

		// parentNodeList = dom.getElementsByTagName("Detail1");
		Node parentNode = Currentdetail2List.item(0);
		NodeList childNodeList = parentNode.getChildNodes();
		int childNodeListLength = childNodeList.getLength();

		for (int ctr = 0; ctr < childNodeListLength; ctr++) {
			Node childNode = childNodeList.item(ctr);
			String childNodeName = childNode.getNodeName();
			// System.out.println("@@@@@@@@ for line_no
			// childNodeName["+childNodeName+"]");
			if (childNodeName.equalsIgnoreCase("line_no")) {
				lineNoCurrent = genericUtility.getColumnValue("line_no", dom);
			}
		}

		System.out.println("@@@@ billNo:[" + billNo + "]::billDate:["
				+ billDate + "]lineNoCurrent[" + lineNoCurrent + "]");
		if (detail2List != null && detail2List.getLength() > 0) {
			noOfParent = detail2List.getLength();
			System.out.println("@@@@@@@@ noOfParent [[" + noOfParent + "]]");

			for (ctr1 = 0; ctr1 < noOfParent; ctr1++) // Loop for each node of
			// current detail
			{
				String lineNoDet2 = genericUtility.getColumnValueFromNode(
						"line_no", detail2List.item(ctr1));
				String billNoDet2 = genericUtility.getColumnValueFromNode(
						"bill_no", detail2List.item(ctr1));
				String billDateDet2 = genericUtility.getColumnValueFromNode(
						"bil_date", detail2List.item(ctr1));

				updateStatus = checkNull(getCurrentUpdateFlag(dom2
						.getElementsByTagName("Detail2").item(ctr1)));

				/*
				 * if
				 * ("Attribute".equalsIgnoreCase(detail2List.item(ctr1).getNodeName()) ) {
				 * updateStatus
				 * =detail2List.item(ctr1).getAttributes().getNamedItem("updateFlag").getNodeValue(); }
				 */
				// String editFlag = genericUtility.getColumnValueFromNode(
				// "edit_flag", detail2List.item(ctr1) );
				// updateStatus =
				// checkNull(getCurrentUpdateFlag(dom2.getElementsByTagName("Detail2").item(i)));
				System.out.println("@@@@ ctr1[" + ctr1 + "]updateStatus["
						+ updateStatus + "]billNoDet2:[" + billNoDet2
						+ "]billDateDet2[" + billDateDet2 + "]lineNoDet2["
						+ lineNoDet2 + "]");

				if (("A".equalsIgnoreCase(updateStatus)
						|| "E".equalsIgnoreCase(updateStatus) || "N"
						.equalsIgnoreCase(updateStatus))
						&& billNo.equalsIgnoreCase(billNoDet2)
						&& (!lineNoCurrent.equalsIgnoreCase(lineNoDet2))) {
					System.out.println("@@@@ duplicate data found......");
					return true;
				}
			}
		}
		System.out.println("@@@@ duplicate data not found......");
		return false;
	}

	// public long countDaysBetween(java.sql.Date startdate, java.util.Date
	// enddate)
	public long countDaysBetween(java.util.Date startdate,
			java.util.Date enddate) {

		// reset all hours mins and secs to zero on start date
		Calendar startCal = GregorianCalendar.getInstance();
		startCal.setTime(startdate);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		long startTime = startCal.getTimeInMillis();

		// reset all hours mins and secs to zero on end date
		Calendar endCal = GregorianCalendar.getInstance();
		endCal.setTime(enddate);
		endCal.set(Calendar.HOUR_OF_DAY, 0);
		endCal.set(Calendar.MINUTE, 0);
		endCal.set(Calendar.SECOND, 0);
		long endTime = endCal.getTimeInMillis();

		return (endTime - startTime) / (1000 * 60 * 60 * 24);
	}

	private boolean isBillNoRepeatedInYear(Connection conn, String billNoL,
			String stockistDateL, String currCustCode, String currTranIdL,
			String xtraParams) throws ITMException {
		System.out.println("--------in isBillNoRepeatedInYear() method-------");
		billNoL = billNoL == null ? "" : billNoL.trim();
		System.out.println("billNoL-------->>" + billNoL);
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		int currStkYear = 0, actStkYear = 0;
		ArrayList<String> tranIDList = new ArrayList<String>();
		String sql = "", sql1 = "", billNo = "", custCode = "", tranId = "";
		Timestamp stockistDate = null;
		Timestamp frDate = null, toDate = null;
		String currDate = "", frDateStr = "", traDateStr = "", toDateStr = "";

		try {
			SimpleDateFormat sdf = new SimpleDateFormat((genericUtility
					.getApplDateFormat()));
			java.util.Date date = sdf.parse(stockistDateL);
			java.sql.Timestamp stkTimest = new java.sql.Timestamp(date
					.getTime());
			System.out.println("Stockist Date current---->>" + stkTimest);
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
			String CurrYear = sdf1.format(date);
			System.out.println("Current stk Year------>>[" + CurrYear + "]");
			currStkYear = Integer.parseInt(CurrYear);

			int cnt = 0;
			// String prdCode="";
			// String siteCode =
			// checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,
			// "loginSiteCode"));

			sql1 = "select fr_date,to_date from acctprd where ?  between fr_date and to_date";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setTimestamp(1, stkTimest);
			rs1 = pstmt1.executeQuery();
			if (rs1.next()) {
				frDate = rs1.getTimestamp("fr_date");
				toDate = rs1.getTimestamp("to_date");
			}
			frDateStr = sdf.format(frDate).toString();
			toDateStr = sdf.format(toDate).toString();

			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;

			System.out.println("From Date[" + frDateStr + "]To Date[" + toDate
					+ "]tranID:[" + currTranIdL + "]");

			if (currTranIdL == null || currTranIdL.trim().length() == 0) {
				currTranIdL = "@@@@@@@@@@";
			}
			System.out.println("tranID:[" + currTranIdL + "]");

			sql = " select count(1) from spl_sales_por_hdr h, spl_sales_por_det d "
					+ " where h.tran_id = d.tran_id  and d.bill_no = ?  "
					+ " and d.bil_date  BETWEEN  ?  AND  ?  "
					+ " and d.tran_id <> ?  and h.wf_status <> 'R'  ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, billNoL);
			pstmt.setTimestamp(2, frDate);
			pstmt.setTimestamp(3, toDate);
			pstmt.setString(4, currTranIdL);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;

			System.out.println("cnt" + cnt);

			if (cnt > 0) {
				System.out
						.println("@@@@@@@@ bill_no and bill_date already exist...........");
				return true;
			}

			// commented by cpatil on 18/06/15 for execution time issue.
			/*
			 * sql = "select tran_id,cust_code from spl_sales_por_hdr"; pstmt =
			 * conn.prepareStatement(sql); rs = pstmt.executeQuery(); while
			 * (rs.next()) { tranId = rs.getString(1); tranId = tranId == null ? "" :
			 * tranId.trim(); tranIDList.add(tranId); } rs.close(); rs = null;
			 * pstmt.close(); pstmt = null; System.out.println("Tran ID--->>[" +
			 * tranIDList + "]"); if (tranIDList.contains(currTranIdL)) {
			 * tranIDList.remove(currTranIdL); } System.out.println("Tran ID
			 * after removeing--->>[" + tranIDList + "]"); for (String tranIdL :
			 * tranIDList) { sql = "select tran_id,cust_code from
			 * spl_sales_por_hdr where tran_id = ?"; pstmt =
			 * conn.prepareStatement(sql); pstmt.setString(1, tranIdL); rs =
			 * pstmt.executeQuery(); while (rs.next()) { tranId =
			 * rs.getString(1); custCode = rs.getString(2);
			 * 
			 * tranId = tranId == null ? "" : tranId.trim(); custCode = custCode ==
			 * null ? "" : custCode.trim(); sql1 = "select bill_no,bil_date from
			 * spl_sales_por_det where tran_id = ?"; pstmt1 =
			 * conn.prepareStatement(sql1); pstmt1.setString(1, tranId); rs1 =
			 * pstmt1.executeQuery(); while (rs1.next()) { billNo =
			 * rs1.getString(1); stockistDate = rs1.getTimestamp(2); billNo =
			 * billNo == null ? "" : billNo.trim(); if (stockistDate != null) {
			 * if (custCode.equalsIgnoreCase(currCustCode)) {
			 * System.out.println("currCustCode Condition true--->>[" + billNo +
			 * "][" + currCustCode + "]"); if (billNo.equalsIgnoreCase(billNoL)) {
			 * System.out.println("billNo Condition true--->>333[" + billNo +
			 * "]"); String year1 = sdf1.format(stockistDate);
			 * System.out.println("stkDate Sting-------->>[" + year1 + "]");
			 * actStkYear = Integer.parseInt(year1); if (currStkYear ==
			 * actStkYear) { System.out.println("$$$$---Return true from
			 * isBillNoRepeatedInYear method--%%%%"); return true; } } } } }
			 * rs1.close(); rs1 = null; pstmt1.close(); pstmt1 = null; } if (rs !=
			 * null) { rs.close(); rs = null; } if (pstmt != null) {
			 * pstmt.close(); pstmt = null; } }
			 */

		} catch (Exception e) {
			System.out
					.println("--------Exception : in isBillNoRepeated method----------");
			isError = true;
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return false;
	}

	protected int getDBRowCount(Connection conn, String table_name,
			String whrCondCol, String whrCondVal) throws ITMException {
		int count = -1;

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
			} catch (SQLException e) {
				System.out
						.println("SQL Exception In getDBRowCount method of ProofOfDelivery Class : "
								+ e.getMessage());
				e.printStackTrace();
			} catch (Exception ex) {
				System.out
						.println("Exception In getDBRowCount method of ProofOfDelivery Class : "
								+ ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
			} finally {
				try {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		} else {
			try {
				throw new SQLException(
						"Connection passed to ProofOfDelivery.getDBRowCount() method is null");
			} catch (SQLException e) {
				e.printStackTrace();
				isError = true;
			}
		}
		return count;
	}

	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		System.out
				.println("------------------ itemChanged called------------------");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception : [ProofOFDelivery ][itemChanged(String,String)] :==>\n"
							+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		isError = false;
		System.out.println("@@@@@@@ itemChanged called");
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0;
		double reSaleQty = 0;
		String columnValue = null, updateStatus = "", tranId = "", lineNo = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null, currDetail = null;
		int ctr = 0;
		// 03/03/15 manoharan taken from inside case
		String lineNoTrace = "";
		String despachID = "", approveRate = "";
		String sql1 = "", invoiceID1 = "", lotNo = "", lotSl = "", itemSerPorm = "", itemCode = "", itemCodeDesc = "", locCode = "";
		int invLineNo = 0;
		double rate = 0.0, quantity = 0.0, discount = 0.0, pendQty = 0;
		HashMap<String, String> domDetail = null;
		String rateResale = "", podQty = "", aprvRate = "", invQtyS = "", pendQtyS = "", pendTempS = "";
		double rateResaleD = 0.0, podQtyD = 0.0, debitNoteAmt = 0, aprvRateD = 0, invQtyD = 0, pendQtyD = 0, tempQty = 0, pendTempD = 0;
		String id1 = "";
		String invoiceID = "", siteCode = "", custCode = "", sql = "", siteDescr = "", custName = "";
		String confirm = "N", wfStatus = "O", loginSite = "", siteDescr1 = "";
		String custName1 = "", custName2 = "", custCodeSoledTo = "", invRemarks = "";
		String stkBillDate = "", invoiceDate = "", despLineNo = "";
		Timestamp invDateT = null;
		int diffDays = 0;
		// end 03/03/15 manoharan taken from inside case

		try {
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer(
					"<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("-------- currentFormNo : " + currentFormNo);

			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				System.out.println("[" + currentColumn + "] ==> '"
						+ columnValue + "'");
				System.out.println("editFlag =>" + editFlag);

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					System.out.println("------------in itm_default--------->");
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(
							genericUtility.getApplDateFormat());
					String tranDate = simpleDateFormatObj.format(currentDate
							.getTime());
					valueXmlString.append("<tran_date>").append(
							"<![CDATA[" + tranDate + "]]>").append(
							"</tran_date>");
					valueXmlString.append("<confirmed>").append(
							"<![CDATA[" + confirm + "]]>").append(
							"</confirmed>");
					valueXmlString.append("<wf_status>").append(
							"<![CDATA[" + wfStatus + "]]>").append(
							"</wf_status>");

					// [START] Added by Sanket Girme

					String chgUser = this.genericUtility
							.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
					String chgTerm = this.genericUtility
							.getValueFromXTRA_PARAMS(xtraParams, "termId");
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility
							.getApplDateFormat());
					String sysDate = sdf.format(new Date());
					System.out.println("SYstem Date :: ===>" + sysDate);
					valueXmlString.append("<chg_user>").append(
							"<![CDATA[" + chgUser + "]]>")
							.append("</chg_user>");

					valueXmlString.append("<chg_term>").append(
							"<![CDATA[" + chgTerm + "]]>")
							.append("</chg_term>");

					valueXmlString.append("<chg_date>").append(
							"<![CDATA[" + sysDate + "]]>")
							.append("</chg_date>");

					// [END] ADDED bY SANKET GIRME

					loginSite = getValueFromXTRA_PARAMS(xtraParams,
							"loginSiteCode");
					loginSite = loginSite == null ? "" : loginSite.trim();
					System.out.println("loginSite ----->>[" + loginSite + "]");
					if (loginSite.length() > 0) {
						siteDescr1 = getNameOrDescrForCode(conn, "SITE",
								"DESCR", "SITE_CODE", loginSite);
					}
					valueXmlString.append("<site_code>").append(
							"<![CDATA[" + loginSite + "]]>").append(
							"</site_code>");
					valueXmlString.append("<site_descr>").append(
							"<![CDATA[" + siteDescr1 + "]]>").append(
							"</site_descr>");
				}

				else if ((currentColumn.trim().equalsIgnoreCase("invoice_id"))) {
					// double amount=0.0,exchRate=0.0;
					
					String invDate = "";
					Date inDateFrmt;
					
					Map<String, Object> invDetailsMap = null;
					invoiceID = genericUtility
							.getColumnValue("invoice_id", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom);
					custCode = genericUtility.getColumnValue("cust_code", dom);
					invDate = genericUtility.getColumnValue(
							"invoice_tran_date", dom);

					invoiceID = invoiceID == null ? "" : invoiceID.trim();
					siteCode = siteCode == null ? "" : siteCode.trim();
					custCode = custCode == null ? "" : custCode.trim();
					invDate = invDate == null ? "" : invDate.trim();
					System.out.println("invoiceID -----   :->> " + invoiceID);
					System.out.println("siteCode -----   :->> " + siteCode);
					System.out.println("custCode -----   :->> " + custCode);
					System.out.println("invDate ----     :->> " + invDate);

					if (invoiceID.length() > 0) {
						invDetailsMap = getDetailsFromInvoice(conn, invoiceID);
						System.out.println("Map in invoice---->>["
								+ invDetailsMap + "]");

						if (invDetailsMap != null) {
							if (siteCode.length() <= 0) {
								siteCode = invDetailsMap.get("site_code")
										.toString();
								siteCode = siteCode == null ? "" : siteCode
										.trim();
								System.out.println("siteCode -----  map :->> "
										+ siteCode);
							}
							//if (custCode.length() <= 0) {
								custCode = invDetailsMap.get("cust_code__bill")
										.toString();
								custCode = custCode == null ? "" : custCode
										.trim();
								System.out.println("custCode -----  map :->> "
										+ custCode);
							//}
							
							// [START] Added by Sanket Girme
							// To get the Invoice date [13-JUL-15]
							inDateFrmt = (Date) invDetailsMap.get("tran_date");
							custCodeSoledTo = (String) invDetailsMap
									.get("cust_code");
							invRemarks = (String) invDetailsMap.get("remarks");
							invRemarks = invRemarks == null ? "" : invRemarks;
							invRemarks = invRemarks == "null" ? "" : invRemarks;

							// [END] Added by Sanket Girme to get the Invoice
							// date [13-JUL-15]
							
							if (siteCode.length() > 0) {
								siteDescr = getNameOrDescrForCode(conn, "SITE",
										"DESCR", "SITE_CODE", siteCode);
							}

							if (custCode.length() > 0) {
								custName = getNameOrDescrForCode(conn,
										"customer", "cust_name", "cust_code",
										custCode);
							}

							if (invDate.length() > 0) {
								invDate = getNameOrDescrForCode(conn,
										"invoice", "tran_date", "invoice_id",
										invoiceID);
							}
							SimpleDateFormat spf = new SimpleDateFormat(
									genericUtility.getDispDateFormat());

							System.out.println("period code--->>"
									+ invDetailsMap.get("prd_code"));
							valueXmlString.append("<site_code>").append(
									"<![CDATA[" + siteCode + "]]>").append(
									"</site_code>");
							valueXmlString.append("<cust_code>").append(
									"<![CDATA[" + custCode + "]]>").append(
									"</cust_code>");
							valueXmlString.append("<amount>").append(
									"<![CDATA[" + invDetailsMap.get("amount")
											+ "]]>").append("</amount>");
							valueXmlString.append("<exch_rate>").append(
									"<![CDATA["
											+ invDetailsMap.get("exch_rate")
											+ "]]>").append("</exch_rate>");
							valueXmlString.append("<curr_code>").append(
									"<![CDATA["
											+ checkNull(invDetailsMap
													.get("curr_code")
													+ "]]>")).append(
									"</curr_code>");
							valueXmlString.append("<site_descr>").append(
									"<![CDATA[" + siteDescr + "]]>").append(
									"</site_descr>");
							valueXmlString.append("<cust_name>").append(
									"<![CDATA[" + custName + "]]>").append(
									"</cust_name>");

			      	 // [START] Added By Sanket Girme [30-JUL-2015]
							// Invoice Date
							valueXmlString.append("<invoice_tran_date>")
									.append(
											"<![CDATA["
													+ spf.format(inDateFrmt)
													+ "]]>").append(
											"</invoice_tran_date>");

							// Sold to Customer Code
							valueXmlString.append("<invoice_cust_code>")
									.append(
											"<![CDATA[" + custCodeSoledTo
													+ "]]>").append(
											"</invoice_cust_code>");

							if (custCodeSoledTo.length() > 0) {
								custName2 = getNameOrDescrForCode(conn,
										"customer", "cust_name", "cust_code",
										custCodeSoledTo);
							}
							valueXmlString.append("<cust_name_1>").append(
									"<![CDATA[" + custName2 + "]]>").append(
									"</cust_name_1>");

							// Invoice Remark
							valueXmlString.append("<invoice_remarks>").append(
									"<![CDATA[" + invRemarks + "]]>").append(
									"</invoice_remarks>");

					// [END] Added By Sanket Girme [30-JUL-2015]

							valueXmlString.append("<prd_code>").append(
									"<![CDATA["
											+ checkNull(invDetailsMap
													.get("prd_code")) + "]]>")
									.append("</prd_code>");
						}
					} else {
						System.out.println("------invoice id null-----");
						// valueXmlString.append("<project_descr>").append("").append("</project_descr>");
						// valueXmlString.append("<site_code>").append("").append("</site_code>");
						// valueXmlString.append("<cust_code>").append("").append("</cust_code>");
						valueXmlString.append("<amount>").append("").append(
								"</amount>");
						valueXmlString.append("<exch_rate>").append("").append(
								"</exch_rate>");
						valueXmlString.append("<curr_code>").append("").append(
								"</curr_code>");
						valueXmlString.append("<invoice_tran_date>").append("")
								.append("</invoice_tran_date>");
						// valueXmlString.append("<site_descr>").append("").append("</site_descr>");
						// valueXmlString.append("<cust_name>").append("").append("</cust_name>");
						valueXmlString.append("<prd_code>").append("").append(
								"</prd_code>");
					}
				}

				else if (currentColumn.trim().equalsIgnoreCase("site_code")) {
					siteCode = genericUtility.getColumnValue("site_code", dom);
					siteCode = siteCode == null ? "" : siteCode.trim();
					System.out.println("siteCode ----->>[" + siteCode + "]");
					if (siteCode.length() > 0) {
						siteDescr1 = getNameOrDescrForCode(conn, "SITE",
								"DESCR", "SITE_CODE", siteCode);
					}
					valueXmlString.append("<site_descr>").append(
							"<![CDATA[" + siteDescr1 + "]]>").append(
							"</site_descr>");

				} else if (currentColumn.trim().equalsIgnoreCase("cust_code")) {
					custCode = genericUtility.getColumnValue("cust_code", dom);
					custCode = custCode == null ? "" : custCode.trim();
					System.out.println("custCode ----->>[" + custCode + "]");
					if (custCode.length() > 0) {
						custName1 = getNameOrDescrForCode(conn, "customer",
								"cust_name", "cust_code", custCode);
					}
					valueXmlString.append("<cust_name>").append(
							"<![CDATA[" + custName1 + "]]>").append(
							"</cust_name>");
				}
				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				int childListLength = childNodeList.getLength();
				if (currentColumn.trim().equals("itm_default")) {
					String invID = "";
					System.out
							.println("itm_default item change called details 2................");
					invID = checkNull(genericUtility.getColumnValue(
							"invoice_id", dom1));
					valueXmlString.append("<inv_id>").append(
							"<![CDATA[" + invID + "]]>").append("</inv_id>");
				}
				if (currentColumn.trim().equals("line_no__trace")) {
					domDetail = new HashMap<String, String>();
					System.out.println("line_no__trace item change........");
					lineNoTrace = checkNull(genericUtility.getColumnValue(
							"line_no__trace", dom));
					invoiceID1 = checkNull(genericUtility.getColumnValue(
							"invoice_id", dom1));
					id1 = checkNull(genericUtility
							.getColumnValue("inv_id", dom));
					System.out.println("line no trace----->[" + lineNoTrace
							+ "]");
					System.out.println("invoiceID----->[" + invoiceID1 + "]");
					System.out.println("id1----->[" + id1 + "]");

					if (lineNoTrace.length() > 0 && invoiceID1.length() > 0) {
						// sql1="select
						// rate,quantity,lot_no,lot_sl,item_ser__prom,item_code,inv_line_no,desp_id,discount
						// "
						// +
						// "from invoice_trace where invoice_id = ? and
						// inv_line_no = ?";
						// VALLABH KADAM 23/DES/14
						// Change [inv_line_no] to [line_no]
						// to get proper data in Pop help
						/*sql1 = "select rate,quantity,lot_no,lot_sl,item_ser__prom,item_code,inv_line_no,desp_id,discount, desp_line_no "
								+ "from invoice_trace where invoice_id = ? and line_no = ?";*/
						sql1 = "select rate,quantity__stduom,lot_no,lot_sl,item_ser__prom,item_code,inv_line_no,desp_id,discount, desp_line_no "
							+ "from invoice_trace where invoice_id = ? and line_no = ?";
						pstmt = conn.prepareStatement(sql1);
						pstmt.setString(1, invoiceID1);
						pstmt.setString(2, lineNoTrace);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							rate = rs.getDouble(1);
							quantity = rs.getDouble(2);
							lotNo = checkNull(rs.getString(3));
							lotSl = checkNull(rs.getString(4));
							itemSerPorm = checkNull(rs.getString(5));
							itemCode = checkNull(rs.getString(6));
							invLineNo = rs.getInt(7);
							despachID = checkNull(rs.getString(8));
							discount = rs.getDouble(9);
							despLineNo = rs.getString(10);
						}
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
						System.out.println("despachID ------>> " + despachID);
						System.out.println("discount ------>> " + discount);
						if (discount > 0) {
							rate = rate - (rate * discount / 100);
							System.out
									.println("New rate after discount------>> "
											+ rate + "]");
						}
						valueXmlString.append("<rate__inv>").append(
								"<![CDATA[" + rate + "]]>").append(
								"</rate__inv>");
						approveRate = ProofOfDeliveryDefault.getInstance()
								.getApprovedRate(conn, itemCode, rate);// Call
						// another
						// method
						System.out.println("approveRate final ------>> "
								+ approveRate);
						valueXmlString.append("<aprv_rate>").append(
								"<![CDATA[" + approveRate + "]]>").append(
								"</aprv_rate>");
						valueXmlString.append("<quantity__inv>").append(
								"<![CDATA[" + quantity + "]]>").append(
								"</quantity__inv>");

						valueXmlString.append("<lot_no>").append(
								"<![CDATA[" + lotNo + "]]>")
								.append("</lot_no>");
						valueXmlString.append("<lot_sl>").append(
								"<![CDATA[" + lotSl + "]]>")
								.append("</lot_sl>");
						valueXmlString.append("<item_ser__prom>").append(
								"<![CDATA[" + itemSerPorm + "]]>").append(
								"</item_ser__prom>");
						valueXmlString.append("<item_code>").append(
								"<![CDATA[" + itemCode + "]]>").append(
								"</item_code>");
						itemCodeDesc = getNameOrDescrForCode(conn, "item",
								"descr", "item_code", itemCode);
						System.out.println("---------EXECUTE query12--------");
						// sql1 = "select loc_code from despatchdet where
						// desp_id = ? and item_code = ?";
						sql1 = "select loc_code from despatchdet where desp_id = ? and line_no = ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, despachID);
						// pstmt1.setString(2, itemCode);
						pstmt1.setString(2, despLineNo);

						rs1 = pstmt1.executeQuery();
						System.out.println("-----query executed12345-----");
						if (rs1.next()) {
							locCode = checkNull(rs1.getString(1));
						}
						if (rs1 != null) {
							rs1.close();
							rs1 = null;
						}
						if (pstmt1 != null) {
							pstmt1.close();
							pstmt1 = null;
						}
						System.out.println("Location code -------->> : "
								+ locCode);

						valueXmlString.append("<descr>").append(
								"<![CDATA[" + itemCodeDesc + "]]>").append(
								"</descr>");
						valueXmlString.append("<loc_code>").append(
								"<![CDATA[" + locCode + "]]>").append(
								"</loc_code>");

						domDetail
								.put("quantity__inv", String.valueOf(quantity));
						domDetail.put("lot_no", lotNo == null ? null : lotNo
								.trim());
						domDetail.put("lot_sl", lotSl == null ? null : lotSl
								.trim());
						domDetail.put("invoice_id", invoiceID1);
						domDetail.put("line_no__trace", lineNoTrace); // String.valueOf(invLineNo));
						domDetail.put("item_code", itemCode == null ? ""
								: itemCode.trim());
						domDetail.put("loc_code", locCode);
						pendQty = (ProofOfDeliveryDefault.getInstance())
								.getPendingQty(conn, domDetail);
						// 16/02/15 manoharan in edit mode not to consider the
						// old quantity
						NodeList links = dom.getElementsByTagName("attribute");
						for (int i = 0; i < links.getLength(); i++) {
							Element link = (Element) links.item(i);
							updateStatus = link.getAttribute("updateFlag");
							System.out.println("attribute value = "
									+ updateStatus);
						}
						if ("E".equalsIgnoreCase(editFlag)
								&& "E".equalsIgnoreCase(updateStatus)) {
							tranId = checkNull(genericUtility.getColumnValue(
									"tran_id", dom));
							if (tranId == null || "null".equals(tranId)
									|| tranId.trim().length() == 0) {
								tranId = "@@@@@@@@@@";
							}
							lineNo = checkNull(genericUtility.getColumnValue(
									"line_no", dom));
							if (lineNo == null || "null".equals(lineNo)
									|| lineNo.trim().length() == 0) {
								lineNo = "0";
							}
							sql1 = "select quantity__resale from spl_sales_por_det where tran_id = ? and line_no = ?";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, tranId);
							pstmt1.setInt(2, Integer.parseInt(lineNo));
							rs1 = pstmt1.executeQuery();
							System.out.println("-----query executed12345-----");
							if (rs1.next()) {
								reSaleQty = rs1.getDouble(1);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							pendQty = pendQty + reSaleQty;

						}
						System.out.println("Pending quantity----->>[" + pendQty
								+ "]");
						valueXmlString.append("<pend_qty>").append(
								"<![CDATA[" + pendQty + "]]>").append(
								"</pend_qty>");
						valueXmlString.append("<pend_temp>").append(
								"<![CDATA[" + pendQty + "]]>").append(
								"</pend_temp>");
					}
				} // end line no trace

				else if (currentColumn.trim().equals("quantity__resale")) {
					System.out
							.println("-------item chnage quantity__resale-------------");
					domDetail = new HashMap<String, String>();

					aprvRate = genericUtility.getColumnValue("aprv_rate", dom);
					rateResale = genericUtility.getColumnValue("rate__resale",
							dom);
					podQty = genericUtility.getColumnValue("quantity__resale",
							dom);
					invQtyS = genericUtility.getColumnValue("quantity__inv",
							dom);
					pendQtyS = genericUtility.getColumnValue("pend_qty", dom);

					pendTempS = genericUtility.getColumnValue("pend_temp", dom);

					rateResaleD = Double
							.parseDouble(checkSpaceNull(rateResale));
					podQtyD = Double.parseDouble(checkSpaceNull(podQty));
					aprvRateD = Double.parseDouble(checkSpaceNull(aprvRate));
					invQtyD = Double.parseDouble(checkSpaceNull(invQtyS));
					pendQtyD = Double.parseDouble(checkSpaceNull(pendQtyS));

					pendTempD = Double.parseDouble(checkSpaceNull(pendTempS));
					System.out.println("aprvRate ------>>" + aprvRateD);
					System.out.println("rateResaleD ------>>" + rateResaleD);

					System.out.println("Invoice Qty ------>>" + invQtyD);
					System.out.println("qty resale(POD) ------>>" + podQtyD);
					System.out.println("pending Qty------>>" + pendQtyD);
					System.out.println("qty pend_temp ------>>" + pendTempD);

					/*
					 * tempQty =invQtyD - pendQtyD;// 200 - 80 =120 // 0
					 * tempQty=tempQty + podQtyD;//120 + 30 = 150//120
					 * 
					 * 
					 * pendQtyD=invQtyD - tempQty; //200 -150 =50
					 */

					// 03/03/15 manoharan commented old logic of pending
					// quantity and new logic added
					/*
					 * if (pendQtyD > 0) { System.out.println("Pending qty
					 * greater than zero.........."); tempQty = invQtyD -
					 * pendTempD; // 200 -80 =120 tempQty = tempQty + podQtyD; //
					 * 120 +30 =150
					 * 
					 * pendQtyD = invQtyD - tempQty; // 200 -150 =50 } else {
					 * System.out.println("Pending qty less than
					 * zero.........."); pendQtyD = invQtyD - podQtyD; // 200
					 * -150 =50 }
					 */

					lineNoTrace = checkNull(genericUtility.getColumnValue(
							"line_no__trace", dom));
					invoiceID1 = checkNull(genericUtility.getColumnValue(
							"invoice_id", dom1));
					id1 = checkNull(genericUtility
							.getColumnValue("inv_id", dom));
					if (lineNoTrace.length() > 0 && invoiceID1.length() > 0) {
						// sql1="select
						// rate,quantity,lot_no,lot_sl,item_ser__prom,item_code,inv_line_no,desp_id,discount
						// "
						// +
						// "from invoice_trace where invoice_id = ? and
						// inv_line_no = ?";
						// VALLABH KADAM 23/DES/14
						// Change [inv_line_no] to [line_no]
						// to get proper data in Pop help DESP_LINE_NO NOT NULL
						// CHAR(3)
						/*sql1 = "select quantity,lot_no,lot_sl,item_code,inv_line_no,desp_id, desp_line_no "
								+ "from invoice_trace where invoice_id = ? and line_no = ?";*/
						sql1 = "select quantity__stduom,lot_no,lot_sl,item_code,inv_line_no,desp_id, desp_line_no "
							+ "from invoice_trace where invoice_id = ? and line_no = ?";
						pstmt = conn.prepareStatement(sql1);
						pstmt.setString(1, invoiceID1);
						pstmt.setString(2, lineNoTrace);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							quantity = rs.getDouble(1);
							lotNo = checkNull(rs.getString(2));
							lotSl = checkNull(rs.getString(3));
							itemCode = checkNull(rs.getString(4));
							invLineNo = rs.getInt(5);
							despachID = checkNull(rs.getString(6));
							despLineNo = rs.getString(7);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						// sql1 = "select loc_code from despatchdet where
						// desp_id = ? and item_code = ?";
						sql1 = "select loc_code from despatchdet where desp_id = ? and line_no = ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, despachID);
						// pstmt1.setString(2, itemCode);
						pstmt1.setString(2, despLineNo);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) {
							locCode = checkNull(rs1.getString(1));
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					domDetail.put("quantity__inv", String.valueOf(quantity));
					domDetail
							.put("lot_no", lotNo == null ? null : lotNo.trim());
					domDetail
							.put("lot_sl", lotSl == null ? null : lotSl.trim());
					domDetail.put("invoice_id", invoiceID1);
					domDetail.put("line_no__trace", lineNoTrace);// String.valueOf(invLineNo));
					domDetail.put("item_code", itemCode == null ? "" : itemCode
							.trim());
					domDetail.put("loc_code", locCode);
					pendQty = (ProofOfDeliveryDefault.getInstance())
							.getPendingQty(conn, domDetail);
					// 16/02/15 manoharan in edit mode not to consider the old
					// quantity
					System.out.println("invoice qty [" + quantity
							+ "] pendQty before [" + pendQty + "]");
					reSaleQty = 0;
					NodeList links = dom.getElementsByTagName("attribute");
					for (int i = 0; i < links.getLength(); i++) {
						Element link = (Element) links.item(i);
						updateStatus = link.getAttribute("updateFlag");
						System.out.println("attribute value = " + updateStatus);
					}
					if ("E".equalsIgnoreCase(editFlag)
							&& "E".equalsIgnoreCase(updateStatus)) {
						tranId = checkNull(genericUtility.getColumnValue(
								"tran_id", dom));
						if (tranId == null || "null".equals(tranId)
								|| tranId.trim().length() == 0) {
							tranId = "@@@@@@@@@@";
						}
						lineNo = checkNull(genericUtility.getColumnValue(
								"line_no", dom));
						if (lineNo == null || "null".equals(lineNo)
								|| lineNo.trim().length() == 0) {
							lineNo = "0";
						}
						sql1 = "select quantity__resale from spl_sales_por_det where tran_id = ? and line_no = ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, tranId);
						pstmt1.setInt(2, Integer.parseInt(lineNo));
						rs1 = pstmt1.executeQuery();
						System.out.println("-----query executed12345-----");
						if (rs1.next()) {
							reSaleQty = rs1.getDouble(1);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						pendQty = pendQty + reSaleQty;

					}
					System.out.println("invoice qty [" + quantity
							+ "] pendQty after [" + pendQty + "] reSaleQty ["
							+ reSaleQty + "]");
					// end 03/03/15 manoharan
					if (rateResaleD > 0.0 && podQtyD > 0.0) {
						debitNoteAmt = (rateResaleD - aprvRateD) * podQtyD;
					}
					valueXmlString.append("<pend_qty>").append(
							"<![CDATA[" + pendQtyD + "]]>").append(
							"</pend_qty>");
					System.out.println("debitNoteAmt ------>>" + debitNoteAmt);
					if (debitNoteAmt > 0)
						valueXmlString.append("<debit_note_amt>").append(
								"<![CDATA[" + debitNoteAmt + "]]>").append(
								"</debit_note_amt>");
					else
						valueXmlString.append("<debit_note_amt>").append(
								"<![CDATA[]]>").append("</debit_note_amt>");
				} else if (currentColumn.trim().equals("rate__resale")) {
					System.out
							.println("-------item chnage rate__resale-------------");

					aprvRate = genericUtility.getColumnValue("aprv_rate", dom);

					rateResale = genericUtility.getColumnValue("rate__resale",
							dom);
					podQty = genericUtility.getColumnValue("quantity__resale",
							dom);

					rateResaleD = Double
							.parseDouble(checkSpaceNull(rateResale));
					podQtyD = Double.parseDouble(checkSpaceNull(podQty));
					aprvRateD = Double.parseDouble(checkSpaceNull(aprvRate));

					System.out.println("aprvRate  rate__resale------>>"
							+ aprvRate);
					System.out.println("rateResaleD ------>>" + rateResaleD);
					System.out.println("qty resale ------>>" + podQtyD);

					if (rateResaleD > 0.0 && podQtyD > 0.0) {
						System.out
								.println("------Inside condition 12121----------");
						debitNoteAmt = (rateResaleD - aprvRateD) * podQtyD;
						System.out.println("debitNoteAmt asasdd------>>"
								+ debitNoteAmt);
					}
					System.out.println("debitNoteAmt ------>>" + debitNoteAmt);
					if (debitNoteAmt > 0)
						valueXmlString.append("<debit_note_amt>").append(
								"<![CDATA[" + debitNoteAmt + "]]>").append(
								"</debit_note_amt>");
					else
						valueXmlString.append("<debit_note_amt>").append(
								"<![CDATA[]]>").append("</debit_note_amt>");
				} else if (currentColumn.trim().equals("bil_date")) {
					invoiceID = genericUtility.getColumnValue("invoice_id",
							dom1);
					stkBillDate = genericUtility
							.getColumnValue("bil_date", dom);
					invoiceID = invoiceID == null ? "" : invoiceID.trim();
					stkBillDate = stkBillDate == null ? "" : stkBillDate.trim();
					System.out
							.println("Invoice Id in bill date item change ------>>["
									+ invoiceID + "]");
					System.out
							.println("Stockist bill date item change ------>>["
									+ stkBillDate + "]");
					if (invoiceID.length() > 0 && stkBillDate.length() > 0) {
						sql = "select conf_date from invoice where invoice_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, invoiceID);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							invDateT = rs.getTimestamp(1);
						}
						System.out.println("conf date invoice--->>[" + invDateT
								+ "]");

						if (invDateT != null) {
							SimpleDateFormat format = new SimpleDateFormat(
									genericUtility.getApplDateFormat());
							invoiceDate = format.format(invDateT);
							System.out.println("confDate 139------>>["
									+ invoiceDate + "]");

							System.out.print("stkBillDate ------->>>>>>>>>>>["
									+ stkBillDate + "]");
							System.out
									.print("invoice confDate ------->>>>>>>>>>>["
											+ invoiceDate + "]");

							sql = "select  to_date( ?,'dd-mm-yyyy') -  to_date( ?,'dd-mm-yyyy') from dual";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stkBillDate);
							pstmt.setString(2, invoiceDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								diffDays = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Difference between ----->>["
									+ diffDays + "]");

							valueXmlString.append("<transit_day>").append(
									"<![CDATA[" + diffDays + "]]>").append(
									"</transit_day>");
						}
					}
				}
				valueXmlString.append("</Detail2>");
			}// end switch
			valueXmlString.append("</Root>");

		} // end
		// try-------------------------------------------------------------
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			isError = true;
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}

	private String checkSpaceNull(String str) {
		if (str == null || str.trim().length() == 0) {
			str = "0.0";
		}
		return str;
	}

	public Map<String, Object> getDetailsFromInvoice(Connection conn,
			String invoiceId) throws ITMException {
		HashMap<String, Object> invDetailsMap = new HashMap<String, Object>();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String sql = "";
		try {
			sql = "select site_code,cust_code__bil,net_amt,curr_code,exch_rate,prd_code,tran_date,cust_code,remarks from invoice where invoice_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				invDetailsMap.put("site_code", rs.getString(1));
				invDetailsMap.put("cust_code__bill", rs.getString(2));
				
				invDetailsMap.put("amount", Double.toString((rs.getDouble(3))));
				invDetailsMap.put("curr_code", rs.getString(4));
				invDetailsMap
						.put("exch_rate", Double.toString(rs.getDouble(5)));
				invDetailsMap.put("prd_code", rs.getString(6));
				invDetailsMap.put("tran_date", rs.getDate(7));
				invDetailsMap.put("cust_code", rs.getString(8));
				invDetailsMap.put("remarks", rs.getString(9));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			System.out.println("Exception in getDetailsFromInvoice----------");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return invDetailsMap;
	}

	private boolean isTotalAmountPodQtyGrt(Connection conn, String invID,
			double actualInvQty, double currentPodQty, String itemCodeSret) throws ITMException {
		System.out
				.println("---in method isTotalAmountPodQtyGrt@@@@@@@ ------invoice ID :["
						+ invID + "]");
		System.out.println("actualInvQty----->>" + actualInvQty
				+ " currentPodQty----->>" + currentPodQty);
		Map<String, Double> itemMap = new HashMap<String, Double>();
		PreparedStatement pstmt2 = null, pstmt1 = null, pstmt3 = null;
		ResultSet rs2 = null, rs1 = null, rs3 = null;
		String sql1 = "", tranID = "", confirmed = "", itemCode = "";
		int lineNo = 0, count = 0;
		Double totalParsingQty = 0.0;
		double qtyInv = 0.0, podQty = 0.0, totalPodQty = 0.0, consumeQty = 0.0, finalPodqty = 0.0;
		boolean isItemCodeMatch = false;
		sql1 = "select tran_id,confirmed from spl_sales_por_hdr where invoice_id = ? and confirmed = ? and wf_status <> 'R'";
		try {
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setString(1, invID);
			pstmt2.setString(2, "Y");
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				tranID = rs2.getString(1);
				confirmed = rs2.getString(2);
				tranID = tranID == null ? "" : tranID.trim();
				System.out.println("Confirmed POD---->[" + confirmed + "]");

				System.out.println("tran id counter ------->" + count);
				if (tranID.length() > 0) {
					sql1 = "";
					sql1 = "select line_no,item_code,quantity__inv,quantity__resale from spl_sales_por_det where tran_id = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, tranID);
					rs1 = pstmt1.executeQuery();

					while (rs1.next()) {
						lineNo = rs1.getInt(1);
						itemCode = rs1.getString(2);
						qtyInv = rs1.getDouble(3);
						podQty = rs1.getDouble(4);
						System.out.println("Line no @@@@ ---->" + lineNo
								+ " qtyInv-->>" + qtyInv + " podQty->>"
								+ podQty + " itemCode =[" + itemCode + "]");

						itemCode = itemCode == null ? "" : itemCode.trim();
						sql1 = "";
						/*
						 * sql1= "select sum(quantity__resale) from
						 * spl_sales_por_det where item_code = ? and tran_id = ?" ;
						 * pstmt3=conn.prepareStatement(sql1);
						 * pstmt3.setString(1, itemCode); pstmt3.setString(2,
						 * tranID); rs3=pstmt3.executeQuery(); if(rs3.next()) {
						 * totalPodQty=rs3.getDouble(1); }
						 */

						if (itemMap != null) {
							for (String key : itemMap.keySet()) {
								System.out.println("(In map$$$) Item Code--->>"
										+ key + " itemCode--->>"
										+ itemMap.get(key));
								if (key.equalsIgnoreCase(itemCode)) {
									System.out
											.println("item code match---------");
									// totalPodQty=itemMap.get(key);
									itemMap.put(itemCode,
											(itemMap.get(key) + podQty));
									System.out.println("item code=" + itemCode
											+ " TotalPODqty="
											+ (itemMap.get(key) + podQty));
									isItemCodeMatch = true;
								}
							}
						}
						if (!(isItemCodeMatch))
							itemMap.put(itemCode, podQty);
						isItemCodeMatch = false;
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
			}
			System.out.println("Item code in hashMap ---------->>");
			for (String key : itemMap.keySet()) {
				System.out.println("Item code -->> " + key);
			}
			System.out.println("PODqty FINAL in hashMap ---------->>");
			for (Double key : itemMap.values()) {
				System.out.println("f values  = " + key);
				// itemMap.get(itemCodeSret);
				finalPodqty = itemMap.get(itemCodeSret) + currentPodQty;
				System.out.println("Final pod Quantity------>>" + finalPodqty
						+ " Actual POD qty--->>" + actualInvQty);
				if (finalPodqty > actualInvQty) {
					System.out
							.println("-------------------POD quantity is greater--------------------------------------------------------");
					return true;
				}
			}

			// System.out.println("TranID in getTotalAmountPodQty
			// --->>["+tranID+"] TotalPODqty -->["+totalPodqty+"]");

			/*
			 * if (confirmed.equalsIgnoreCase("Y")) return false;
			 */

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (rs2 != null) {
					rs2.close();
					rs2 = null;
				}
				if (pstmt2 != null) {
					pstmt2.close();
					pstmt2 = null;
				}
			} catch (Exception e) {
				System.out.println("Exception : In finally block ");
				e.printStackTrace();
				isError = true;
			}
		}
		return false;
	}

	protected boolean isPartialyInvoiceChk(Connection conn, String invID,
			String transaction) throws ITMException {
		System.out
				.println("---in method isPartialyInvoiceChk ------invoice ID :["
						+ invID + "]");
		PreparedStatement pstmt2 = null, pstmt1 = null;
		ResultSet rs2 = null, rs1 = null;
		String sql1 = "", tranID = "", confirmed = "";
		int lineNoTraceL = 0, count = 1, invTraceCount = 0, podCount = 0;

		double qtyInv = 0.0, podQty = 0.0, totalPodQty = 0.0;

		// sql1 = "select tran_id,confirmed from spl_sales_por_hdr where
		// invoice_id = ? and wf_status <> ?"; // 12/02/15 manoharan commented
		// and moved below
		try {
			// 12/02/15 manoharan check whether all invoice_trace line are not
			// existing is pod
			sql1 = "select count(1) from invoice_trace where invoice_id = ?";
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setString(1, invID);
			rs2 = pstmt2.executeQuery();
			if (rs2.next()) {
				invTraceCount = rs2.getInt(1);
			}
			rs2.close();
			rs2 = null;
			pstmt2.close();
			pstmt2 = null;

			sql1 = "select count( distinct d.line_no__trace) from spl_sales_por_det d, spl_sales_por_hdr h "
					+ " where h.tran_id = d.tran_id " + " and h.invoice_id = ?";
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setString(1, invID);
			rs2 = pstmt2.executeQuery();
			if (rs2.next()) {
				podCount = rs2.getInt(1);
			}
			rs2.close();
			rs2 = null;
			pstmt2.close();
			pstmt2 = null;
			System.out.println("IN isPartialyInvoiceChk invTraceCount ["
					+ invTraceCount + "] podCount [" + podCount + "]");
			if (podCount < invTraceCount) {
				return true; // Pod for all invoice_trace is not entered
			}

			sql1 = "select d.line_no__trace, d.quantity__inv,sum(d.quantity__resale) from spl_sales_por_det d, spl_sales_por_hdr h "
					+ " where h.tran_id = d.tran_id "
					+ " and h.invoice_id = ? "
					+ " group by d.line_no__trace, d.quantity__inv";
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setString(1, invID);
			rs2 = pstmt2.executeQuery();
			invTraceCount = 0;
			while (rs2.next()) {
				if (rs2.getDouble(2) > rs2.getDouble(3)) {
					invTraceCount++;
					break;
				}
			}
			rs2.close();
			rs2 = null;
			pstmt2.close();
			pstmt2 = null;
			if (invTraceCount > 0) {
				return true; // pod quantity is < invoice quantity for some
				// existing POD
			}

			System.out.println("IN isPartialyInvoiceChk invTraceCount ["
					+ invTraceCount + "] podCount [" + podCount + "]");
			if (podCount < invTraceCount) {
				return true; // Pod for all invoice_trace is not entered
			}

			// end 12/02/15 manoharan check whether all invoice_trace line are
			// not existing is pod

			// 12/02/15 manoharan moved from above
			sql1 = "select tran_id,confirmed from spl_sales_por_hdr where invoice_id = ? and wf_status <> ?";
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setString(1, invID);
			pstmt2.setString(2, "R");
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				tranID = rs2.getString(1);
				confirmed = rs2.getString(2);
				tranID = tranID == null ? "" : tranID.trim();
				confirmed = confirmed == null ? "" : confirmed.trim();

				System.out
						.println("In isPartialyInvoiceChk tran id counter ------->"
								+ count);
				if (tranID.length() > 0 && confirmed.equalsIgnoreCase("Y")) {
					count++;
					sql1 = "";
					sql1 = "select line_no__trace,item_code,quantity__inv,quantity__resale from spl_sales_por_det where tran_id = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, tranID);

					rs1 = pstmt1.executeQuery();
					while (rs1.next()) {
						lineNoTraceL = rs1.getInt(1);
						qtyInv = rs1.getDouble(3);
						podQty = rs1.getDouble(4);
						System.out
								.println("IN isPartialyInvoiceChk lineNoTraceL ["
										+ lineNoTraceL
										+ "] qtyInv ["
										+ qtyInv
										+ "] podQty  [" + podQty + "]");
						totalPodQty = totalPodQty + podQty;
						if (podQty < qtyInv) {
							partialQty = podQty;
							actaulInvQty = qtyInv;
							lineNoTrace = lineNoTraceL;
							remainQty = actaulInvQty - partialQty;
							System.out
									.println("In isPartialyInvoiceChk partialQty ["
											+ partialQty
											+ "] actaulInvQty ["
											+ actaulInvQty
											+ "] remainQty ["
											+ remainQty
											+ "] lineNoTrace ["
											+ lineNoTrace + "]");
							return true;
						}
					}
					System.out.println("Total POD quantity --------->>"
							+ totalPodQty);
					System.out.println("Confirmed POD---->[" + confirmed + "]");

					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
				if ("POD".equalsIgnoreCase(transaction)) {
					if (confirmed.equalsIgnoreCase("N"))
						return true;
				}
			}

			System.out.println("TranID in isPartialyInvoiceChk --->>[" + tranID
					+ "] confirmed -->[" + confirmed + "]");

			/*
			 * if (confirmed.equalsIgnoreCase("Y")) return false;
			 */

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (rs2 != null) {
					rs2.close();
					rs2 = null;
				}
				if (pstmt2 != null) {
					pstmt2.close();
					pstmt2 = null;
				}
			} catch (Exception e) {
				System.out.println("Exception : In finally block ");
				e.printStackTrace();
				isError = true;
			}
		}
		return false;
	}

	private String errorType(Connection conn, String errorCode) throws ITMException {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, checkNull(errorCode));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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

	public String checkNull(Object input) {
		if (input == null) {
			return "";
		} else {
			String input1 = input.toString();

			return input1.trim();
		}
	}

	protected String getNameOrDescrForCode(Connection conn, String table_name,
			String descr_col_name, String whrCondCol, String whrCondVal) throws ITMException {
		String descr = null;

		if (conn != null) {

			ResultSet rs = null;
			PreparedStatement pstmt = null;

			String sql = "SELECT " + descr_col_name + " FROM " + table_name
					+ " WHERE " + whrCondCol + " = ?";

			System.out.println("SQL in getNameOrDescrForCode method : " + sql);
			try {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, whrCondVal);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					descr = rs.getString(descr_col_name);
				}

				descr = descr == null ? "" : descr;
			} catch (SQLException e) {
				System.out
						.println("SQL Exception In getNameOrDescrForCode method of ProofOfDelivery Class : "
								+ e.getMessage());
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			} catch (Exception ex) {
				System.out
						.println("Exception In getNameOrDescrForCode method of ProofOfDelivery Class : "
								+ ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
			} finally {
				try {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		} else {
			try {
				throw new SQLException(
						"Connection passed to ProofOfDelivery.getNameOrDescrForCode() method is null");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return descr;
	}

	private String getCurrentUpdateFlag(Node currDetail) {
		NodeList currDetailList = null;
		String updateStatus = "", nodeName = "";
		int currDetailListLength = 0;

		currDetailList = currDetail.getChildNodes();
		currDetailListLength = currDetailList.getLength();
		for (int i = 0; i < currDetailListLength; i++) {
			nodeName = currDetailList.item(i).getNodeName();
			System.out.println("@@@@@@@@@@@@@@ nodeName[" + nodeName + "]");
			if (nodeName.equalsIgnoreCase("Attribute")) {
				updateStatus = currDetailList.item(i).getAttributes()
						.getNamedItem("updateFlag").getNodeValue();
				System.out.println("@@@@@@@@@@@@@@ inside if i[" + i
						+ "]updateStatus[" + updateStatus + "]");
				break;
			}
		}
		System.out.println("@@@@@@@@@@@@@@ updateStatus[" + updateStatus + "]");
		return updateStatus;
	}

	private boolean isPartialyInvoiceChkHdr(Connection conn, String invID) throws ITMException {
		System.out
				.println("---in method isPartialyInvoiceChkHdr ------invoice ID :["
						+ invID + "]");
		PreparedStatement pstmt2 = null, pstmt1 = null;
		ResultSet rs2 = null, rs1 = null;
		String sql1 = "", tranID = "", confirmed = "";
		int lineNo = 0, count = 1;
		invID = invID == null ? "" : invID.trim();

		if (invID.length() <= 0) {
			return false;
		}
		double qtyInv = 0.0, podQty = 0.0;

		sql1 = "select tran_id,confirmed from spl_sales_por_hdr where invoice_id = ? and wf_status <> 'R' order by confirmed";
		try {
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setString(1, invID);
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				tranID = rs2.getString(1);
				confirmed = rs2.getString(2);
				tranID = tranID == null ? "" : tranID.trim();
				confirmed = confirmed == null ? "" : confirmed.trim();
				System.out.println("Confirmed POD---->[" + confirmed + "]");

				if (confirmed.equalsIgnoreCase("N"))
					return true;

				System.out.println("tran id counter ------->" + count);
			}
			System.out.println("TranID in isPartialyInvoiceChk --->>[" + tranID
					+ "] confirmed -->[" + confirmed + "]");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (rs2 != null) {
					rs2.close();
					rs2 = null;
				}
				if (pstmt2 != null) {
					pstmt2.close();
					pstmt2 = null;
				}
			} catch (Exception e) {
				System.out.println("Exception : In finally block ");
				e.printStackTrace();
			}
		}
		return false;
	}

	protected String getColumnDescr(Connection conn, String columnName,
			String tableName, String columnName2, String value) throws ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String findValue = "";
		try {
			sql = "SELECT " + columnName + " from " + tableName + " where "
					+ columnName2 + "= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			System.out.println("Exception in getColumnDescr ");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("returning String from getColumnDescr " + findValue);
		return findValue;
	}

	protected boolean isRecordPresentInMiscDrCrRcp(Connection conn,
			String invoiceID) throws ITMException {
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", itemCodeT = "", lotNoT = "", lotSlT = "", lineNoT = "", itemCodeM = "", lotNoM = "", lotSlM = "", lineNoM = "";
		double quantityT = 0, quantityM = 0;
		int count = 0, noOfRecordMatched = 0, noOfRecords = 0;
		try {
			sql = "select count(*) from invoice_trace where invoice_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invoiceID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				noOfRecords = rs.getInt(1);
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			System.out.println("NoOfRecords---->>[" + noOfRecords + "]");

			sql = "select sum(quantity),item_code,lot_no,lot_sl,inv_line_no from invoice_trace "
					+ "where invoice_id = ? group by item_code,lot_no,lot_sl,inv_line_no";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invoiceID);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				count++;
				quantityT = rs.getDouble(1);
				itemCodeT = rs.getString(2);
				lotNoT = rs.getString(3);
				lotSlT = rs.getString(4);
				lineNoT = rs.getString(5);

				itemCodeT = itemCodeT == null ? "" : itemCodeT.trim();
				lotNoT = lotNoT == null ? "" : lotNoT.trim();
				lotSlT = lotSlT == null ? "" : lotSlT.trim();
				lineNoT = lineNoT == null ? "" : lineNoT.trim();
				System.out.println("count--------------->>[" + count + "]");
				System.out.println("itemCodeT-->[" + itemCodeT + "] lotNoT->["
						+ lotNoT + "]");
				System.out.println("lotSlT-->[" + lotSlT + "] lineNoT->["
						+ lineNoT + "]");

				// checked in misc_drcr_rcp

				sql = "select sum(d.quantity) as Quantity,d.item_code,d.lot_no,d.lot_sl,d.line_no__invtrace from misc_drcr_rcp h,misc_drcr_rdet d "
						+ "where h.tran_id = d.tran_id  and h.remarks like ? and quantity is not null "
						+ "group by d.item_code,d.lot_no,d.lot_sl,d.line_no__invtrace";

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, "%POD%" + invoiceID);
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {
					quantityM = rs1.getDouble(1);
					itemCodeM = rs1.getString(2);
					lotNoM = rs1.getString(3);
					lotSlM = rs1.getString(4);
					lineNoM = rs1.getString(5);

					itemCodeM = itemCodeT == null ? "" : itemCodeT.trim();
					lotNoM = lotNoM == null ? "" : lotNoM.trim();
					lotSlM = lotSlM == null ? "" : lotSlM.trim();
					lineNoM = lineNoM == null ? "" : lineNoM.trim();

					System.out.println("QuantityT----->>[" + quantityT + "]");
					System.out.println("QuantityM----->>[" + quantityM + "]");
					System.out.println("itemCodeM-->[" + itemCodeM
							+ "] lotNoM-->[" + lotNoM + "]");
					System.out.println("lotSlM-->[" + lotSlM + "] lineNoM-->["
							+ lineNoM + "]");

					if (itemCodeT.equalsIgnoreCase(itemCodeM)
							&& lotNoT.equalsIgnoreCase(lotNoM)) {
						if (lotSlT.equalsIgnoreCase(lotSlM)
								&& lineNoT.equalsIgnoreCase(lineNoM)) {
							if (quantityT == quantityM) {
								System.out
										.println("record matched........at count "
												+ count);
								noOfRecordMatched++;
							}
						}
					}
				} // inner while end
				if (rs1 != null) {
					rs1.close();
					rs1 = null;
				}
				if (pstmt1 != null) {
					pstmt1.close();
					pstmt1 = null;
				}
			}// end main while
			System.out.println("noOfRecords End----->>[" + noOfRecords + "]");
			System.out.println("noOfRecordMatched----->>[" + noOfRecordMatched
					+ "]");

			if (noOfRecords == noOfRecordMatched) {
				return true;
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
			System.out.println("Exception : checkInMiscDrCrRcp : "
					+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return false;
	}

	protected String getSalesReturnTranID(Connection conn, String table_name,
			String tranID, String whrCondCol, String whrCondVal) throws ITMException {
		String descr = null;

		if (conn != null) {

			ResultSet rs = null;
			PreparedStatement pstmt = null;
			// getSalesReturnTranID(conn, "sreturn", "tran_id", "invoice_id",
			// str);

			String sql = "SELECT " + tranID + " FROM " + table_name + " WHERE "
					+ whrCondCol + " = ? and confirmed = ?";
			// select tran_id from sreturn where invoice_id = '10DS086300' and
			// confirmed ='Y';
			System.out.println("SQL in getSalesReturnTranID method : " + sql);
			try {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, whrCondVal);
				pstmt.setString(2, "Y");
				rs = pstmt.executeQuery();
				if (rs.next()) {
					descr = rs.getString(tranID);
				}
				descr = descr == null ? "" : descr;
			} catch (SQLException e) {
				System.out
						.println("SQL Exception In getNameOrDescrForCode method of ProofOfDelivery Class : "
								+ e.getMessage());
				e.printStackTrace();
			} catch (Exception ex) {
				System.out
						.println("Exception In getNameOrDescrForCode method of ProofOfDelivery Class : "
								+ ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
			} finally {
				try {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		} else {
			try {
				throw new SQLException(
						"Connection passed to ProofOfDelivery.getNameOrDescrForCode() method is null");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return descr;
	}

}
