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
import java.util.StringTokenizer;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.jmx.snmp.Timestamp;

@Stateless
public class PODProcess extends ProcessEJB implements PODProcessLocal,
		PODProcessRemote {
	String siteCodeG = "", invTypeG = "", xtraParamsG = "", schemeCodeG = "",
			multipleScheme = "";
	Map<String, Double> partialInvoiceMapG = new HashMap<String, Double>();
	Map<String, Double> partialInvMap = new HashMap<String, Double>();
	int invdoneCnt = 0;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	// in that map only partial invoice id,line_no_trace added with actual qty
	// less partial qty
	public String process(String xmlString, String xmlString2,
			String windowName, String xtraParams) throws RemoteException,
			ITMException {

		System.out.println("enter in process(21....................");
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
			/*retStr = e.getMessage();*/ // Commented By Mukesh Chauhan on 05/08/19
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
		int cnt = 0;

		double ExchRate = 0;
		String siteCode = "", custCodeFrm = "", custCodeTo = "", invType = "", errorString = "", errCode = "", userId = "", custCode = "";
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
			userId = vdt.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			siteCode = genericUtility.getColumnValue("site_code", dom);
			custCodeFrm = genericUtility.getColumnValue("cust_code_from", dom);
			custCodeTo = genericUtility.getColumnValue("cust_code_to", dom);
			invType = genericUtility.getColumnValue("inv_type", dom);

			siteCode = siteCode == null ? "" : siteCode.trim();
			custCodeFrm = custCodeFrm == null ? "" : custCodeFrm.trim();
			custCodeTo = custCodeTo == null ? "" : custCodeTo.trim();
			invType = invType == null ? "" : invType.trim();
			System.out.println("SiteCode--133-->>[" + siteCode + "]");
			System.out.println("custCodeFrm--->>[" + custCodeFrm + "]");
			System.out.println("custCodeTo--->>[" + custCodeTo + "]");
			System.out.println("invType--->>[" + invType + "]");

			// Validation for Site code
			if (siteCode.length() == 0) {
				errCode = "VTSITECNE";
				errorString = vdt.getErrorString("site_code", errCode, userId);
				return errorString;

			} else {
				cnt = podObject.getDBRowCount(conn, "site", "site_code",
						siteCode);
				if (cnt == 0) {
					System.out.println("site_code not exist validation fire");
					errCode = "VTSITENEX";
					errorString = vdt.getErrorString("site_code", errCode,
							userId);
					return errorString;
				}
			}
			// Validation for Customer Code From
			if (custCodeFrm.length() == 0) {
				errCode = "VTCUSTCNE";
				errorString = vdt.getErrorString("cust_code_from", errCode,
						userId);
				return errorString;

			} else {
				System.out.println("custCodeFrm--->>[" + custCodeFrm + "]");
				if (!("00".equalsIgnoreCase(custCodeFrm))) {
					cnt = podObject.getDBRowCount(conn, "customer",
							"cust_code", custCodeFrm);
					if (cnt == 0) {
						System.out
								.println("custCodeFrom not exist validation fire");
						errCode = "VTCUSTNEX";
						errorString = vdt.getErrorString("cust_code_from",
								errCode, userId);
						return errorString;
					}
				}

			}

			// Validation for Customer Code To
			if (custCodeTo.length() == 0) {
				errCode = "VTCUSTCNE";
				errorString = vdt.getErrorString("cust_code_to", errCode,
						userId);
				return errorString;

			} else {
				System.out.println("custCodeTo --->>[" + custCodeTo + "]");
				if (!("ZZ".equalsIgnoreCase(custCodeTo))) {
					cnt = podObject.getDBRowCount(conn, "customer",
							"cust_code", custCodeTo);
					if (cnt == 0) {
						System.out
								.println("custCodeFrom not exist validation fire");
						errCode = "VTCUSTNEX";
						errorString = vdt.getErrorString("cust_code_to",
								errCode, userId);
						return errorString;
					}
				}
			}
			// Validation for invoice type. only DM or IS invoice type allowed.
			if (invType.length() == 0) {
				errCode = "VTINVTNN";
				errorString = vdt.getErrorString("inv_type", errCode, userId);
				return errorString;

			} else {
				if (!("DM".equalsIgnoreCase(invType) || "IS"
						.equalsIgnoreCase(invType))) {
					errCode = "VTINVTI";
					errorString = vdt.getErrorString("inv_type", errCode,
							userId);
					return errorString;
				}
			}
			if (custCodeFrm.length() > 0 && custCodeTo.length() > 0
					&& siteCode.length() > 0) {
				siteCodeG = siteCode;
				invTypeG = invType;

				// [START] Code added by Sanket Girme [13-jul-15]
				String disDate = "";

				String sqlPODDate = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE='999999' AND VAR_NAME='POD_PRD_DATE'";
				pstmt = conn.prepareStatement(sqlPODDate);
				rs = pstmt.executeQuery();
				while (rs.next()) 
				{
					disDate = rs.getString(1);
				}
				// [END] Code added by Sanket Girme [13-jul-15]
				
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
				
				if ("00".equalsIgnoreCase(custCodeFrm)
						&& "ZZ".equalsIgnoreCase(custCodeTo)) {

					// sql = "select invoice_id from invoice where site_code = ?
					// and confirmed = ?"
					// + " and sysdate - conf_date > 45 and inv_type = ?";base'

					sql = "select invoice_id from invoice where site_code = ? and confirmed = ?"
							+ " and conf_date > '"
							+ disDate
							+ "' and inv_type = ? and sysdate - conf_date > 45";

					// [13-JUL-15] CHANGED BY
					// SANKET GIRME TO GET THE
					// INVOICE_DATE GRETTER THAN
					// 01-NOV-14
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, "Y");
					pstmt.setString(3, invType);
				} else {
					// sql = "select invoice_id from invoice where
					// cust_code__bil between ? and ? and site_code = ? and
					// confirmed = ?"
					// + " and sysdate - conf_date > 45 and inv_type =?";

					sql = "select invoice_id from invoice where cust_code__bil between ? and  ? and site_code = ? and confirmed = ?"
							+ " and conf_date > '"
							+ disDate
							+ "' and inv_type = ? and sysdate - conf_date > 45";
					// [13-JUL-15] CHANGED BY
					// SANKET GIRME TO GET THE
					// INVOICE_DATE GRETTER THAN
					// 01-NOV-15
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCodeFrm);
					pstmt.setString(2, custCodeTo);
					pstmt.setString(3, siteCode);
					pstmt.setString(4, "Y");
					pstmt.setString(5, invType);
				}
				// ADD CONDITION TO FILTER INVOICE ID WHICH HAS SALE RETURN  GENERATED ON 25/FEB/2016.
				rs = pstmt.executeQuery();
				while (rs.next()) {
					invoiceId = rs.getString(1);
					invoiceId = invoiceId == null ? "" : invoiceId.trim();
					inv45dayList.add(invoiceId); 
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Size of arrayList inv45dayList----->>["
						+ inv45dayList.size() + "]");
				// if(inv45dayList.size() > 0){
				System.out.println("Data found for process---------------");

				// inv45dayList.clear();
				// inv45dayList.add("618ID00047");
				// inv45dayList.add("618ID00032");

				System.out.println("inv45dayList-->>[" + inv45dayList + "]");
				miscDrCrInvList = getDoneInvoiceIDFromMiscDrCrRcp(conn,
						inv45dayList);
				System.out.println("miscDrCrInvList11121---->>["
						+ miscDrCrInvList + "]");
				if (miscDrCrInvList.contains("Error")) {
					errCode = "VTPRCNCP";
					errorString = itmdbAccess.getErrorString("", errCode, "",
							"", conn);
					return errorString;
				}

				PodHdrInvList = getDoneInvoiceIDFromPODHdr(conn,
						miscDrCrInvList);
				System.out
						.println("POD header123---->>[" + PodHdrInvList + "]");

				if (PodHdrInvList.size() > 0) {
					podProcessInvIDList.addAll(PodHdrInvList);
				}

				System.out.println("Final partialInvoiceMapG---->>["
						+ partialInvoiceMapG + "]");
				System.out.println("final podProcessInvIDList---->>["
						+ podProcessInvIDList + "]");

				if (podProcessInvIDList.size() <= 0
						&& partialInvoiceMapG.size() <= 0) {
					errCode = "VTDNFIN"; // Data not found
					errorString = itmdbAccess.getErrorString("", errCode, "",
							"", conn);
					return errorString;
				}

				// podProcessInvIDList.clear();
				// podProcessInvIDList.add("618ID00047");

				System.out
						.println("Final invoice Id ready for debit note12---->>["
								+ podProcessInvIDList + "]");

				priceList = podObject.getNameOrDescrForCode(conn, "site",
						"price_list", "site_code", siteCode);
				priceList = priceList == null ? "" : priceList.trim();
				System.out.println("priceList----->>[" + priceList + "]");
				if (priceList.length() <= 0) {
					errCode = "VTPRCLND"; // Price list not define in site
					// master.
					errorString = itmdbAccess.getErrorString("", errCode, "",
							"", conn);
					return errorString;
				}
				// Check in invoice_trace if item_code_ord (scheme_code) is
				// available or not.
				// if yes then checked in bomdet.if scheme code found then for
				// that particular item scheme is apply.
				errorString = generateDebitNote(conn, podProcessInvIDList,
						priceList, xtraParams, siteCode, invType);
				System.out
						.println("Return string from generateDebitNote----->>["
								+ errorString + "]");
				if (errorString.equalsIgnoreCase("Error")) {
					errCode = "VTPRCNCP"; // process failed
					errorString = itmdbAccess.getErrorString("", errCode, "",
							"", conn);
					return errorString;
				}

				/*
				 * } else{ errCode="VTDNFIN"; //Data not found errorString =
				 * itmdbAccess.getErrorString("", errCode, "", "", conn); return
				 * errorString; }// inv45dayList.size end
				 */
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

	private String generateDebitNote(Connection conn,
			ArrayList<String> podProcessInvIDListL, String priceListL,
			String xtraParams, String siteCode, String invType) {
		invdoneCnt = 0;
		ResultSet rs = null, rs1 = null, rs2 = null;
		Connection conn1 = null;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ProofOfDelivery pod = null;
		ProofOfDeliveryConf podC = null;
		Map<String, Object> invoiceDetailsMap = null;
		HashMap<String, String> podTraceMap = new HashMap();
		HashMap<String, String> schemeMap = new HashMap();
		HashMap<String, String> partialMap = new HashMap();
		Set<String> splitPartial = new HashSet<String>();
		Map<String, Double> schemeQty = null;
		String partialError = "";
		String sql = "", lotNo = "", lotSl = "", itemCode = "", itemCodeOrd = "", itemActive = "", detail2xmlString = "", errString = "";
		String custCode = "", currCode = "", partial = "N", QtyInvTrace = "", scheme = "N", errCode = "", sreturnTranID = "", sql1 = "";
		double invRate = 0, invQty = 0, discount = 0, priceListRate = 0, detail3Amt = 0, detail3AmtT = 0, debitNoteAmtDetail = 0;
		double debitNoteAmtHdr = 0, ExchRate = 0, actualRate = 0, chargQty = 0, freeQty = 0, netPTS = 0, sreturnQuantity = 0, partialQty = 0;
		int lineNotrace = 0, lineNo = 0, noOfInvoicePrc = 0, totalInvoice = 0, sumDebitNote = 0;
		pod = ProofOfDelivery.getInstance();
		podC = ProofOfDeliveryConf.getInstance();
		ITMDBAccessEJB itmdbAccess1 = new ITMDBAccessEJB();
		ConnDriver connDriver1 = new ConnDriver();
		DecimalFormat df = new DecimalFormat("#.###");
		boolean invstatus;

		try {
			 //Changes and Commented By Bhushan on 13-06-2016 :START
			 //conn1 = connDriver1.getConnectDB("DriverITM");
			 conn1 = getConnection();
			 //Changes and Commented By Bhushan on 13-06-2016 :END
			for (String key : partialInvMap.keySet()) {
				String id[] = key.split(":");
				splitPartial.add(id[0]);
			}
			podProcessInvIDListL.addAll(splitPartial);
			connDriver1 = null;
			System.out.println("priceListL123------>>[" + priceListL + "]");
			totalInvoice = podProcessInvIDListL.size();
			System.out.println("total invoice for process------>>["
					+ totalInvoice + "]");
			System.out.println("total partialInvoiceMapG for process------>>["
					+ partialInvoiceMapG.size() + "]");
			if (totalInvoice > 0 || partialInvoiceMapG.size() > 0) {
				if (totalInvoice > 0) {
					podProcessInvIDListL = getInvFromSalesReturn(podProcessInvIDListL, conn);
					for (String invoiceId : podProcessInvIDListL) {
						detail2xmlString = "";
						lineNo = 0;
						System.out.println("invoice ID in for loop------->>["
								+ invoiceId + "]");

						sql = " select rate,quantity,lot_no,lot_sl,item_code,inv_line_no,discount,item_code__ord,invoice_id "
								+ "from invoice_trace where invoice_id = ? order by inv_line_no";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, invoiceId);
						rs = pstmt.executeQuery();

						while (rs.next()) {

							invRate = rs.getDouble(1);
							invQty = rs.getDouble(2);
							lotNo = rs.getString(3);
							lotSl = rs.getString(4);
							itemCode = rs.getString(5);
							lineNotrace = rs.getInt(6);
							discount = rs.getDouble(7);
							itemCodeOrd = rs.getString(8);

							lotNo = lotNo == null ? "" : lotNo.trim();
							lotSl = lotSl == null ? "" : lotSl.trim();
							itemCode = itemCode == null ? "" : itemCode.trim();
							itemCodeOrd = itemCodeOrd == null ? ""
									: itemCodeOrd.trim();

							// Discount need discussion
							if (discount > 0) {
								invRate = invRate - (invRate * discount / 100);
								System.out
										.println("Discount for invoice id--->>["
												+ invoiceId
												+ "]"
												+ "inv Rate-->>["
												+ invRate
												+ "]");
							}
							// check if any sales return
							sreturnTranID = pod.getSalesReturnTranID(conn,
									"sreturn", "tran_id", "invoice_id",
									invoiceId);
							sreturnTranID = sreturnTranID == null ? ""
									: sreturnTranID.trim();
							if (sreturnTranID.length() > 0) {
								System.out
										.println("-----Got sales return tran id-----------");
								sql1 = "select item_code,quantity from sreturndet where tran_id = ? and item_code = ? ";
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setString(1, sreturnTranID);
								pstmt1.setString(2, itemCode);
								rs1 = pstmt1.executeQuery();
								if (rs1.next()) {
									sreturnQuantity = rs1.getDouble(2);
								}
								if (rs1 != null) {
									rs1.close();
									rs1 = null;
								}
								if (pstmt != null) {
									pstmt1.close();
									pstmt1 = null;
								}
								System.out.println("Sales return------>>["
										+ sreturnQuantity + "]");
								invQty = invQty - sreturnQuantity;
							}
							partial = "N";
							scheme = "N";
							/*
							 * if(pod.isPartialyInvoiceChk(conn,
							 * invoiceId,"PRC")){
							 * System.out.println("@#@#partial condition found
							 * for invoice id----->>["+invoiceId+"]");
							 * System.out.println("Actual inv qty in inv
							 * trace---->>["+invQty+"]");
							 * System.out.println("inv. qty done in POD
							 * screen---->>["+pod.partialQty+"]"); invQty =
							 * invQty - pod.partialQty; partial="Y";
							 * partialMap.put(invoiceId+","+itemCode, scheme);
							 * System.out.println("Actual inv
							 * qty---->>["+invQty+"]"); }
							 */

							// partialQty=actualQtyPODProcessPartial(conn,invoiceId,itemCode,lineNotrace);
							// System.out.println("partial
							// qty---->>["+partialQty+"]");
							// partialMap.put(invoiceId+","+itemCode,
							// partial);
							// Check if item code Active or not."N" or
							// null then
							// return error message.
							itemActive = pod.getColumnDescr(conn, "active",
									"item", "item_code", itemCode);
							itemActive = itemActive == null ? "" : itemActive
									.trim();
							if (itemActive.equalsIgnoreCase("N")
									|| itemActive.length() <= 0) {
								errCode = "VTITMCNAC"; // Item code not
								// active
								errString = itmdbAccess1.getErrorString("",
										errCode, "", "", conn);
								errString = newErrorMessage(errString,
										invoiceId);
								return errString; // item code not
								// active
							}
							java.util.Date tranDate = new Date();
							priceListRate = getPriceListRate(priceListL,
									tranDate, itemCode, lotNo, "", conn);
							System.out.println("priceListRate----->>["
									+ priceListRate + "]");
							if (priceListRate <= 0) {
								errCode = "VTPRCLNE";
								errString = itmdbAccess1.getErrorString("",
										errCode, "", "", conn);
								errString = newErrorMessage(errString,
										invoiceId);
								return errString;
							}

							// check if scheme is apply or not
							if (isSchemeApply(conn, itemCode, invoiceId)) {
								// java code if scheme is apply
								System.out
										.println("-------Scheme is apply -----------");
								System.out.println("multipleScheme--->>["
										+ multipleScheme + "]");
								if ("FOUND".equalsIgnoreCase(multipleScheme)) {
									errCode = "VTITEM10"; // Multiple
									// Scheme
									// Code !
									errString = itmdbAccess1.getErrorString("",
											errCode, "", "", conn);
									errString = newErrorMessage(errString,
											invoiceId);
									return errString;
								}
								schemeQty = schemeApply(conn, invoiceId,
										itemCode);
								System.out.println("schemeQty map---->>["
										+ schemeQty + "]");
								if (schemeQty.size() > 0) {
									if (schemeQty.get("success") < 0) {
										errCode = "VTPRCNCP"; // Scheme
										// related error
										// or sql error.
										errString = itmdbAccess1
												.getErrorString("", errCode,
														"", "", conn);
										errString = newErrorMessage(errString,
												invoiceId);
										return errString;
									} else if (schemeQty.get("success") == 1) {
										scheme = "Y";
										schemeMap.put(invoiceId + ","
												+ itemCode, scheme);
										try {
											chargQty = schemeQty
													.get("charge_qty");
										} catch (Exception E) {
											chargQty = 0;
											System.out
													.println("Excpetion In charge_qty"
															+ E);
											// E.printStackTrace();
										}

										try {
											freeQty = schemeQty.get("free_qty");
										} catch (Exception e) {
											freeQty = 0;
											System.out
													.println("Excpetion In free_qty"
															+ e);
										}

										netPTS = chargQty
												/ (chargQty + freeQty)
												* priceListRate;
										System.out.println("chargQty ---> ["
												+ chargQty + "]");
										System.out.println("freeQty ----> ["
												+ freeQty + "]");
										System.out.println("priceListRate>["
												+ priceListRate + "]");
										System.out
												.println("Equation ==> netPTS = ("
														+ chargQty
														+ "/ ("
														+ chargQty
														+ " + "
														+ freeQty
														+ ") *"
														+ priceListRate + " )");

										System.out.println("Net PTS rate----->>["
														+ netPTS + "]");
										actualRate = netPTS - invRate;

										System.out.println("invRate == > "
												+ invRate);
										System.out
												.println("Second Equation ==> actualRate = "
														+ netPTS
														+ " - "
														+ invRate + " ");
										System.out
												.println("actualRate-------->>["
														+ actualRate + "]");
										if (actualRate <= 0) {
											errCode = "VTNGAMTE"; // negative
											// rate
											errString = itmdbAccess1
													.getErrorString("",
															errCode, "", "",
															conn);
											errString = newErrorMessage(
													errString, invoiceId);
											return errString;
										}
										detail3Amt = actualRate * invQty;
										System.out
												.println("Debit note amount-->>["
														+ detail3Amt + "]");

										detail3AmtT = detail3AmtT + detail3Amt;

										/*
										 * [START][17-JUL-15] Sanket Girme To
										 * Rounding off the Debit Note Example
										 * if Debit Note Amount is 0.49 then
										 * Round off to 0.490
										 * 
										 */
										String debitnt = df.format(detail3Amt) == null ? "0"
												: df.format(detail3Amt);
										detail3Amt = Double
												.parseDouble(debitnt);
										System.out
												.println("Formated Approved Rate---->>["
														+ detail3Amt + "]");

										/*
										 * [END] [17-JUL-15] Sanket Girme
										 */

										lineNo++;
										detail2xmlString = detail2xmlString
												+ podC.getDetailsXmlString(
														conn, invoiceId,
														lineNotrace, lineNo,
														actualRate, invQty,
														lotNo, lotSl, itemCode,
														detail3Amt);
									}
								}
							} else {
								System.out
										.println("-------Scheme not apply -----------");
								schemeMap.put(invoiceId + "," + itemCode,
										scheme);
								scheme = "N";
								actualRate = priceListRate - invRate;
								System.out.println("actualRate-------->>["
										+ actualRate + "]");
								if (actualRate <= 0) {
									errCode = "VTNGAMTE"; // negative
									// rate
									errString = itmdbAccess1.getErrorString("",
											errCode, "", "", conn);
									errString = newErrorMessage(errString,
											invoiceId);
									return errString;
								}
								detail3Amt = actualRate * invQty;

								System.out
										.println("Debit note amount-------->>["
												+ detail3Amt + "]");
								System.out
										.println("Actual rate after deducting inv rate-------->>["
												+ detail3Amt + "]");
								detail3AmtT = detail3AmtT + detail3Amt;

								/*
								 * [START][17-JUL-15] Sanket Girme To ROunding
								 * of the Debit Note Example if Debit Note
								 * Amount is 0.49 then Round off to 0.490
								 * 
								 * 
								 */
								String debitnt = df.format(detail3Amt) == null ? "0"
										: df.format(detail3Amt);
								detail3Amt = Double.parseDouble(debitnt);
								System.out
										.println("Formated Approved Rate---->>["
												+ detail3Amt + "]");

								/* [END] [17-JUL-15] Sanket Girme */

								lineNo++;
								detail2xmlString = detail2xmlString
										+ podC.getDetailsXmlString(conn,
												invoiceId, lineNotrace, lineNo,
												actualRate, invQty, lotNo,
												lotSl, itemCode, detail3Amt);

							}
						}// End while Loop rs2
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}

						System.out.println("Detail2 xml-------->>[["
								+ detail2xmlString + "]");
						if (detail3AmtT > 0) {
							invoiceDetailsMap = pod.getDetailsFromInvoice(conn,
									invoiceId);
							custCode = invoiceDetailsMap.get("cust_code")
									.toString();
							currCode = pod.checkNull(invoiceDetailsMap.get(
									"curr_code").toString()); // xmlStringAll
							ExchRate = Double.parseDouble(invoiceDetailsMap
									.get("exch_rate").toString());
							// END OF IF DB AMT CONDTION START

							/*invstatus = checkDebitAmt(invoiceId, conn);

							System.out.println("For Invoce ID = > " + invoiceId
									+ " Invoice status =>" + invstatus);
							if (detail3AmtT>=100) {*/
							
//							boolean invSreturn = checkSalesReturn(invoiceId, conn);
//							System.out.println("inv Sreturn invoiceId status =>"
//									+ invSreturn);
//							System.out.println("InvoiceId "+invoiceId+" Sales retrun status:"+invSreturn);
//							if (invSreturn) {
									errString = podC.genDebitNote("PRC", invoiceId,
											custCode, siteCode, xtraParams,
											currCode, ExchRate, detail2xmlString,
											detail3AmtT, conn);
									System.out.println("------Error string after DN save--->>["
													+ errString + "]");
//							 }else{
//								 errcode = "vtngamte"; //invalid debit
//									errstring = itmdbaccess1.geterrorstring("",
//											errcode, "", "", conn);
//								}

								if (errString.indexOf("Success") > -1) {
									System.out
											.println("Tran id for Misc. debit note------->>["
													+ (podC.debitNoteTranID)
															.trim() + "]");
									MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
									errString = confDebitNote.confirm(
											(podC.debitNoteTranID).trim(),
											xtraParams, "", conn);
									System.out
											.println("Error String on DB Confirmation: "
													+ errString);
									if ((errString != null)
											&& errString.indexOf("CONFSUCCES") > -1) {
										int cnt = insertDataInPodTrace(conn1,
												invoiceId, podC.xmlStringAll,
												scheme, "N", "Y");
										noOfInvoicePrc++;
										if (noOfInvoicePrc == totalInvoice) {
											invdoneCnt = 1;
											/*
											 * errCode="VTPRCCSF"; errString =
											 * itmdbAccess1.getErrorString("",
											 * errCode, "", "", conn); return
											 * errString;
											 */
										}
										if (cnt > 0) {
											conn1.commit();
										}

										// conn.commit();------------commited
										// when
										// partial invoice end

									} else {
										conn.rollback();
										/*
										 * int cnt=insertDataInPodTrace(conn1,
										 * invoiceId, podC.xmlStringAll,
										 * schemeMap, partialMap,"N"); if(cnt >
										 * 1) conn1.commit();
										 */
										errString = newErrorMessage(errString,
												invoiceId);
										return errString;
									}
								} else {
									conn.rollback();
									/*
									 * int cnt=insertDataInPodTrace(conn1,
									 * invoiceId, podC.xmlStringAll, schemeMap,
									 * partialMap,"N"); if(cnt > 1)
									 * conn1.commit();
									 */
									errString = newErrorMessage(errString,
											invoiceId);
									return errString;
								}
//							}
//							else {
//								
//								System.out
//										.println("The debit Note Not generated for the Invoice Id=> "
//												+ invoiceId+" Due to total debit amount below then 100!");
//								errCode = "VTNGAMTE"; // negative amountbase
//								
//								errString = itmdbAccess1.getErrorString("",
//										errCode, "", "", conn);
//								errString = newErrorMessage(errString, invoiceId);
//								return errString;
//							}

						} else {// end detail3AmtT > 0 condition
							errCode = "VTNGAMTE"; // negative amount
							errString = itmdbAccess1.getErrorString("",
									errCode, "", "", conn);
							errString = newErrorMessage(errString, invoiceId);
							return errString;
						}

					} // end invoice id for loop
				}

			} else {
				errCode = "VTDNFFP"; // No data found for processing
				errString = itmdbAccess1.getErrorString("", errCode, "", "",
						conn);
				conn.rollback();
				return errString;
			}
			System.out.println("errString before partial method--->>["
					+ errString + "]");
			System.out.println("partialInvoiceMapG before partial mrthod--->>["
					+ partialInvoiceMapG + "]");
			if (partialInvoiceMapG.size() > 0) {
				errString = actualQtyPODProcessPartial(conn); // partialInvoiceMapV
				System.out
						.println("Returing STring from actualQtyPODProcessPartial-->>["
								+ errString + "]");
				if ((errString != null) && errString.indexOf("CONFSUCCES") > -1) {
					conn.commit();
					errCode = "VTPRCCSF";
					errString = itmdbAccess1.getErrorString("", errCode, "",
							"", conn);
					return errString;
				} else if (errString.equalsIgnoreCase("Error"))
					errCode = "VTPRCNCP"; // process failed
				errString = itmdbAccess1.getErrorString("", errCode, "", "",
						conn);
				return errString;
			}
			if ((errString != null) && errString.indexOf("CONFSUCCES") > -1) {
				conn.commit();
				errCode = "VTPRCCSF";
				errString = itmdbAccess1.getErrorString("", errCode, "", "",
						conn);
				return errString;
			}

		} catch (Exception e) {
			System.out.println("Exception : ");
			e.printStackTrace();
			return "Error";
		} finally {
			if (partialInvoiceMapG != null) {
				partialInvoiceMapG.clear();
				System.out
						.println("IN finally partialInvoiceMapG clear121...........");
			}
			if (conn1 != null) {
				try {
					conn1.close();
					conn1 = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return errString;
	}

	private int checkPODDoneAlready(Connection conn, String invoiceIdL) {
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", remarks = "", invID = "", itemCodeMisc = "", itemCodeInv;
		double invQty = 0, miscQty = 0;
		int count = 0, lineInvT = 0, lineMiscInvT = 0;

		try {
			sql = "SELECT d.item_code,d.quantity,h.remarks,d.line_no__invtrace FROM  misc_drcr_rcp h, misc_drcr_rdet d  "
					+ "where remarks like 'Auto%POD%' and h.tran_id=d.tran_id";
			// sql="SELECT remarks FROM misc_drcr_rcp where remarks like
			// 'Auto%POD%'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				itemCodeMisc = rs.getString(1);
				miscQty = rs.getDouble(2);
				remarks = rs.getString(3).trim();
				lineMiscInvT = rs.getInt(4);
				itemCodeMisc = itemCodeMisc == null ? "" : itemCodeMisc.trim();
				invID = remarks.substring(46, remarks.length());
				System.out.println("Invoice id after subString----->>[" + invID
						+ "]");

				if (invID.equalsIgnoreCase(invoiceIdL.trim())) {
					sql = "select invoice_id,inv_line_no,item_code,quantity from invoice_trace where invoice_id = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, invID.trim());
					rs1 = pstmt1.executeQuery();
					while (rs1.next()) {
						lineInvT = rs1.getInt(2);
						itemCodeInv = rs1.getString(3);
						invQty = rs1.getDouble(4);
						itemCodeInv = itemCodeInv == null ? "" : itemCodeInv
								.trim();
						System.out.println("itemCodeMisc--->>[" + itemCodeMisc
								+ "  itemCodeInv-->>[" + itemCodeInv + "]");
						System.out.println("lineMiscInvT--->>[" + lineMiscInvT
								+ "  lineInvT-->>[" + lineInvT + "]");
						if (itemCodeMisc.equalsIgnoreCase(itemCodeInv)
								&& lineMiscInvT == lineInvT) {
							if (invQty == miscQty) {
								return 1;
							}

						}

					}

				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		} catch (Exception e) {
			System.out.println("----Exception in checkPODDoneAlready----");
			e.printStackTrace();
			return -1;
		}
		return count;
	}

	// Return array list whoes invoice id not confirmed in spl_sales_por_hdr
	// table.
	private ArrayList<String> getNotConfirmedInvIdFromHdr(Connection conn) throws ITMException {
		ArrayList<String> podNotConfListL = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ProofOfDelivery podL = ProofOfDelivery.getInstance();
		ResultSet rs = null, rs1 = null;
		String sql = "", invID = "";
		try {
			sql = "select invoice_id from spl_sales_por_hdr where confirmed = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "N");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				invID = rs.getString(1);
				invID = invID == null ? "" : invID.trim();
				podNotConfListL.add(invID);
			}
			System.out.println("Invoice id Not confirmed in HDR table--->>[["
					+ podNotConfListL + "]]");
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			System.out
					.println("-----Exception in getNotConfirmedInvIdFromHdr method-----");
			podNotConfListL.add("Error");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return podNotConfListL;
	}

	private int updatePodTraceSuccess(Connection conn, String success,
			String invID) {
		String sql = "";
		int lineNo = 0, count = 0;
		double dbAmt = 0.0, debitNoteHeaderL = 0;
		PreparedStatement pstmt = null;

		try {
			sql = "update podtrace set success = ? where invoice_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, success);
			pstmt.setString(2, invID);
			count = pstmt.executeUpdate();
		}// end try
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		System.out.println("update updatePodTraceSuccess@@@----->>[" + count
				+ "]");
		return count;

	}

	// [31-JUL-2015]Created By Sanket Girme to Check the debit note amount is greater than
	// 100 or not
	public boolean checkDebitAmt(String InvId, Connection conn) throws ITMException {
		PreparedStatement pstmt2 = null;
		ResultSet rs2 = null;
		int sumDebitNote = 0;
		boolean errFlag = false;
		String sql2 = "SELECT NVL(SUM(D.DEBIT_NOTE_AMT),0) from spl_sales_por_hdr h,spl_sales_por_det d WHERE H.TRAN_ID = D.TRAN_ID AND H.INVOICE_ID =?";
		try {
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setString(1, InvId);
			rs2 = pstmt2.executeQuery();
			if (rs2.next()) {

				System.out
						.println("**** Inside checkDebitAmt If loop ****");
				sumDebitNote = rs2.getInt(1);
				System.out.println("For invoice ID ==> " + InvId);
				System.out.println("Debit Note Amount is ==> " + sumDebitNote);
				if (sumDebitNote >= 100) {
					errFlag  = true;
				}
			}
			

		} catch (Exception e) {
			System.out.println("Exception in checkDebitAmt!!> " + e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
			if (rs2 != null) {
				rs2.close();
				rs2 = null;
			}
			if (pstmt2 != null) {
				pstmt2.close();
				pstmt2 = null;
			}
			}
			catch(Exception e)
			{
				System.out.println("Exception to close the resultset! in checkDebitAmt Method..!!!");
			}
		}
		return errFlag;

	}

	private ArrayList<String> getDoneInvoiceIdHDR(Connection conn,
			ArrayList<String> inv45dayListL) throws ITMException {
		System.out
				.println("--------------getDoneInvoiceIdHDR method------------------");
		System.out.println("ArrayList invIdList--->>[[" + inv45dayListL + "]]");
		ProofOfDelivery podObjL = ProofOfDelivery.getInstance();
		ArrayList<String> podDoneInvIDList = new ArrayList<String>();
		ArrayList<String> podProcessInvIDListL = new ArrayList<String>();

		String sql = "", invoiceIdL = "", tranIdL = "";
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;

		try {
			// Get all records whoes POD is already generated.
			sql = "select tran_id,invoice_id from spl_sales_por_hdr where confirmed = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "Y");
			rs = pstmt.executeQuery();

			while (rs.next()) {
				tranIdL = rs.getString(1);
				invoiceIdL = rs.getString(2);
				invoiceIdL = invoiceIdL == null ? "" : invoiceIdL.trim();
				tranIdL = tranIdL == null ? "" : tranIdL.trim();
				podDoneInvIDList.add(invoiceIdL);

			}
			System.out.println("ArrayList podDoneInvIDList--->>[["
					+ podDoneInvIDList + "]]");
			System.out.println("ArrayList podDoneInvIDList size--->>[["
					+ podDoneInvIDList.size() + "]]");
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (inv45dayListL.size() > 0 && podDoneInvIDList.size() > 0) {
				System.out
						.println("---both arraylist contains elements-----------");
				for (String invId : inv45dayListL) {

					if (!(podDoneInvIDList.contains(invId))) {
						podProcessInvIDListL.add(invId);

					} else { // partial check
						if (podObjL.isPartialyInvoiceChk(conn, invId, "PRC")) {
							// partialInvMap.put(invId+":"+podObjL.lineNoTrace,
							// podObjL.remainQty);

						}
					}
					/*
					 * System.out.println("size of Map
					 * partialInvMap-->>["+partialInvMap.size()+"]");
					 * if(partialInvMap.size() > 0){ for(String key
					 * :partialInvMap.keySet()) { String invID[]=key.split(":");
					 * podProcessInvIDListL.add(invID[0]); } }
					 */

				}// end for loop

			} else {
				return inv45dayListL;
			}

			System.out.println("podProcessInvIDList----->>[["
					+ podProcessInvIDListL + "]");
			System.out.println("podProcessInvIDList----->>[["
					+ podProcessInvIDListL.size() + "]");
		} catch (Exception e) {
			System.out
					.println("---------Exception in checkPODDoneOrNot method------------------");
			podProcessInvIDListL.add("Error");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19

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
				System.out
						.println("Exception in finally checkPODDoneOrNot method-------");
				e.printStackTrace();
			}
		}
		return inv45dayListL; // podProcessInvIDListL;

	}

	private boolean isSchemeApply(Connection conn, String itemCodeL,
			String invoiceId) throws ITMException {
		String sql = "", custCodeBil = "", stateCode = "", countCode = "", schemeCode = "", applyCustList = "", noApplyCustList = "", schemeOrderType = "", varValue = "", sql1 = "";
		schemeCodeG = "";
		multipleScheme = "NOTFOUND";
		ResultSet rsL = null, rs1 = null;
		PreparedStatement pstmtL = null, pstmt1 = null;
		int countL = 0;
		ProofOfDelivery podObject = null;
		boolean schemeApply = false, checkBom = false;
		ArrayList<String> schemeCodeList = new ArrayList<String>();
		try {
			/*
			 * sql="select count(*) from bomdet where item_code= ? and eff_from <=
			 * sysdate and " + "valid_upto >= sysdate order by nature";
			 * pstmtL=conn.prepareStatement(sql); pstmtL.setString(1,
			 * itemCodeL); rsL=pstmtL.executeQuery(); if(rsL.next()){
			 * countL=rsL.getInt(1); } rsL.close(); rsL=null; pstmtL.close();
			 * pstmtL=null; if(countL > 0) return true;
			 */
			podObject = ProofOfDelivery.getInstance(); // siteCodeG
			custCodeBil = podObject.getColumnDescr(conn, "cust_code__bil",
					"invoice", "invoice_id", invoiceId);
			custCodeBil = custCodeBil == null ? "" : custCodeBil.trim();
			sql = "select state_code,count_code from customer where cust_code= ? ";
			pstmtL = conn.prepareStatement(sql);
			pstmtL.setString(1, custCodeBil);
			rsL = pstmtL.executeQuery();
			if (rsL.next()) {
				stateCode = rsL.getString(1) == null ? "" : rsL.getString(1)
						.trim();
				countCode = rsL.getString(2) == null ? "" : rsL.getString(2)
						.trim();
			}
			rsL.close();
			rsL = null;
			pstmtL.close();
			pstmtL = null;
			System.out.println("StateCode---->>[" + stateCode + "]");
			System.out.println("countCode---->>[" + countCode + "]");

			sql = " select a.scheme_code,a.apply_cust_list,a.noapply_cust_list "
					+ " from scheme_applicability a, scheme_applicability_det b"
					+ " where a.scheme_code = b.scheme_code"
					+ " and a.item_code = ?"
					+ " and a.app_from <= ?"
					+ " and a.valid_upto >= ?"
					+ " and ( b.site_code = ? or b.state_code = ? or b.count_code = ? )";// stateCode
			// value
			// not
			// fetched

			pstmtL = conn.prepareStatement(sql);
			pstmtL.setString(1, itemCodeL);
			pstmtL.setTimestamp(2, new java.sql.Timestamp(System
					.currentTimeMillis()));
			pstmtL.setTimestamp(3, new java.sql.Timestamp(System
					.currentTimeMillis()));
			pstmtL.setString(4, siteCodeG);
			pstmtL.setString(5, stateCode);
			pstmtL.setString(6, countCode);

			rsL = pstmtL.executeQuery();
			while (rsL.next()) {
				schemeCodeList.add(rsL.getString(1));
				applyCustList = rsL.getString(2);
				noApplyCustList = rsL.getString(3);
			}
			applyCustList = applyCustList == null ? "" : applyCustList.trim();
			noApplyCustList = noApplyCustList == null ? "" : noApplyCustList
					.trim();
			rsL.close();
			rsL = null;
			pstmtL.close();
			pstmtL = null;
			System.out.println("schemeCodeList size---->>["
					+ schemeCodeList.size() + "]");
			System.out
					.println("noApplyCustList---->>[" + noApplyCustList + "]");
			System.out.println("applyCustList---->>[" + applyCustList + "]");
			if (schemeCodeList.size() == 0) {
				return false;
			} else if (schemeCodeList.size() > 1) {
				multipleScheme = "FOUND";
				return true;

			}
			/*
			 * If no apply customer list is not null then POD cust code will be
			 * checked in this list. If match is found scheme won't be
			 * applicable If match not found then system will check for apply
			 * customer list
			 * 
			 * If no apply customer list is null system will check for apply
			 * customer list If apply customer list is null then scheme will be
			 * applicable If apply customer list is not null then system will
			 * check for POD cust code in this list. If no match found then
			 * scheme wont be applicable If match found then scheme will be
			 * applicable
			 */

			else if (schemeCodeList.size() == 1) {
				schemeOrderType = podObject.getColumnDescr(conn, "order_type",
						"scheme_applicability", "scheme_code", schemeCodeList
								.get(0));
				schemeOrderType = schemeOrderType == null ? ""
						: schemeOrderType.trim();
				System.out.println("schemeOrderType11-------->>["
						+ schemeOrderType + "]");

				if (noApplyCustList.length() == 0
						&& applyCustList.length() == 0
						&& schemeOrderType.length() >= 0) {
					schemeApply = true;
					checkBom = true;
				}
				if (noApplyCustList.length() > 0) { // if no apply customer list
					// not null then checked
					String[] custList = noApplyCustList.split(",");
					for (int i = 0; i < custList.length; i++) {
						System.out.println("Customer in noApplyCustList --->>["
								+ custList[i] + "]");
						if (custCodeBil.equalsIgnoreCase(custList[i].trim())) {
							System.out.println("scheme not apply111--->>["
									+ custList[i] + "]");
							return false;
						}
					}
				}
				if (applyCustList.length() > 0) {// if apply customer list
					// not null then checked
					String[] custList = applyCustList.split(",");
					for (int i = 0; i < custList.length; i++) {
						System.out.println("Customer in applyCustList --->>["
								+ custList[i] + "]");
						if (custCodeBil.equalsIgnoreCase(custList[i].trim())) {
							System.out.println("scheme apply222--->>["
									+ custList[i] + "]");
							schemeApply = true;
						}
					}
				}

			}
			System.out.println("Scheme apply in isSchemeApply------>>["
					+ schemeApply + "]");
			if (schemeApply) {
				sql1 = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, "999999");
				pstmt1.setString(2, "POD_SCHEME_APPLY");
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					varValue = rs1.getString(1);
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
				if ( pstmt1 != null )
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if ( rs1 != null )
				{
					rs1.close();
					rs1 = null;
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
				
				System.out.println("var value from diparm----->>[" + varValue + "]");
				varValue = varValue == null ? "" : varValue.trim();

				boolean valueFound = false;
				if (schemeOrderType.length() > 0) {
					//Commented by Tajuddin Mahadi for checking in multiple values in schemeOrderType - START
					/*if (schemeOrderType.equalsIgnoreCase(varValue))
						checkBom = true;
					else {
						System.out.println("schemeOrderType and varvalue not same..............");
						return false;
					}*/
					//Commented by Tajuddin Mahadi for checking in multiple values in schemeOrderType - END
					//Added by Tajuddin Mahadi for multivalues in schemeOrderType - START
					StringTokenizer strToken = new StringTokenizer(schemeOrderType, ",");
					while (strToken.hasMoreTokens()) {
						String schemeOrder = (String) strToken.nextElement();
						if(schemeOrder.equalsIgnoreCase(varValue)) {
							valueFound = true;
							checkBom = true;
							break;
						}
					}
					//Added by Tajuddin Mahadi for multivalues in schemeOrderType - END
				}
				
				//Added by Tajuddin Mahadi for multivalues in schemeOrderType - START
				if(!valueFound){
					return false;
				}
				//Added by Tajuddin Mahadi for multivalues in schemeOrderType - END

				if (checkBom) {
					int count = 0;
					sql = "select count(*) from bomdet where item_code= ? and bom_code = ?";
					pstmtL = conn.prepareStatement(sql);
					pstmtL.setString(1, itemCodeL);
					pstmtL.setString(2, schemeCodeList.get(0));
					rsL = pstmtL.executeQuery();
					if (rsL.next()) {
						count = rsL.getInt(1);
					}
					System.out.println("count in bomdet----->>[" + count + "]");
					if (count > 0) {
						schemeCodeG = schemeCodeList.get(0);
						return true;
					} else
						return false;
				}

			}

		} catch (Exception e) {
			System.out
					.println("----Exception : in isSchemeApply method-------");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return false;

	}

	private Map<String, Double> schemeApply(Connection conn, String invoiceID,
			String itemCode) {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		HashMap<String, Double> schemeQtyMap = new HashMap<String, Double>();

		String sql = "", nature = "";
		int count = 0;
		double quantity = 0;

		try {
			sql = "select qty_per,nature from bomdet where item_code = ? and bom_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, schemeCodeG);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				quantity = rs.getDouble(1);
				nature = rs.getString(2);
				if ("C".equalsIgnoreCase(nature.trim())) {
					schemeQtyMap.put("charge_qty", quantity);
					System.out.println("charge qty added in map------>>["
							+ schemeQtyMap.get("charge_qty") + "]");
				}
				if ("F".equalsIgnoreCase(nature.trim())) {
					schemeQtyMap.put("free_qty", quantity);
					System.out.println("free qty added in map------>>["
							+ schemeQtyMap.get("free_qty") + "]");
				} else {
					schemeQtyMap.put("free_qty", 0.0);
				}
			} // end while
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}

			/*
			 * else{ System.out.println("----data not found in
			 * bomdet---------"); schemeQtyMap.put("success", new Double(0));
			 * return schemeQtyMap; }
			 */

			schemeQtyMap.put("success", new Double(1));
		} catch (Exception e) {
			System.out.println("Exception in scheme apply ---------");
			e.printStackTrace();
			schemeQtyMap.put("success", new Double(-1));
			return schemeQtyMap;

		}
		return schemeQtyMap;
	}

	private int insertDataInPodTrace(Connection conn1, String invID,
			String xmlDataAll, String scheme, String partial, String success) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", itemCode = "", quantity = "", rate = "", amount = "", lineNoS = "";
		int count = 0;
		long tranID = 0;
		invID = invID == null ? "" : invID.trim();
		try {
			// DocumentBuilderFactory dbFactory =
			// DocumentBuilderFactory.newInstance();
			// DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			System.out.println("xmlDataAll---->>[[" + xmlDataAll + "]]");
			Document doc = new ValidatorEJB().parseString(xmlDataAll);
			doc.getDocumentElement().normalize();
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("Detail2");
			System.out.println("in continue----------------"
					+ nList.getLength());
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					itemCode = eElement.getElementsByTagName("item_code").item(
							0).getTextContent();
					quantity = eElement.getElementsByTagName("quantity")
							.item(0).getTextContent();
					rate = eElement.getElementsByTagName("rate").item(0)
							.getTextContent();
					amount = eElement.getElementsByTagName("amount").item(0)
							.getTextContent();
					lineNoS = eElement.getElementsByTagName("line_no").item(0)
							.getTextContent();
					// scheme=schemeMapL.get(invID+","+itemCode.trim());
					// partial=partialMapL.get(invID+","+itemCode.trim());
					System.out.println("scheme in insert pod trace---->>["
							+ scheme + "]");

					sql = "select tran_id from podtrace order by tran_id DESC";
					pstmt = conn1.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						tranID = Long.parseLong(rs.getString(1).trim());
					} else {
						tranID = 10000;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = "insert into podtrace (tran_id,line_no,tran_date,invoice_id,site_code,item_code,inv_type,db_qty,"
							+ "db_rate,debit_note_amt,partial,scheme,chg_date,chg_user,chg_term,success) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstmt = conn1.prepareStatement(sql);
					pstmt.setString(1, Long.toString(++tranID));
					pstmt.setInt(2, Integer.parseInt(lineNoS));
					pstmt.setTimestamp(3, new java.sql.Timestamp(
							new java.util.Date().getTime()));
					pstmt.setString(4, invID);
					pstmt.setString(5, siteCodeG);
					pstmt.setString(6, itemCode);
					pstmt.setString(7, invTypeG);
					pstmt.setDouble(8, Double.parseDouble(quantity));
					pstmt.setDouble(9, Double.parseDouble(rate));

					pstmt.setDouble(10, Double.parseDouble(amount));
					pstmt.setString(11, partial);
					pstmt.setString(12, scheme);
					pstmt.setTimestamp(13, new java.sql.Timestamp(
							new java.util.Date().getTime()));
					pstmt.setString(14, "BASE");
					pstmt.setString(15, "System");
					pstmt.setString(16, success);

					count = pstmt.executeUpdate();

					pstmt.close();

				}// end if
			} // end for

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in insertDataInPodTrace method----");
			return -1;
		}
		return count;
	}

	private ArrayList<String> getDoneInvoiceIDFromPodTrace(Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		HashSet<String> unqInvID = new HashSet<String>();
		ArrayList<String> InvIDList = new ArrayList<String>();
		try {
			sql = "select invoice_id from podtrace where success = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "Y");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				InvIDList.add(rs.getString(1).trim());
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (InvIDList.size() > 0) {
				unqInvID.addAll(InvIDList);
				InvIDList.clear();
				InvIDList.addAll(unqInvID);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return InvIDList;
	}

	private ArrayList<String> getDoneInvoiceIDFromPODHdr(Connection conn,
			ArrayList<String> inv45dayListL) throws ITMException {
		System.out.println("------in getDoneInvoiceIDFromPODHdr------");
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String sql = "", remarks = "", subInvId = "", TranId = "", sql1 = "", itemCode = "", itemCodeM = "", confirmed = "", lotNo = "", lotSl = "", lotNoM = "", lotSlM = "", lineNoTraceI = "";
		int lineNoTrace = 0, lineNoTraceM = 0, deleteId = 0, sumDebitNote = 0;
		double sumQuantityMisc = 0, sumQuantityInv = 0, qty = 0, qtyM = 0;
		HashSet<String> unqInvID = new HashSet<String>();
		ArrayList<String> tranIDList = new ArrayList<String>();
		ArrayList<String> InvIDList = new ArrayList<String>();
		ArrayList<String> InvIDTemp = new ArrayList<String>();
		ProofOfDelivery podL = ProofOfDelivery.getInstance();
		try {
			Iterator<String> it = inv45dayListL.iterator();
			while (it.hasNext()) {
				deleteId = 1;
				String id = it.next();
				confirmed = podL.getColumnDescr(conn, "confirmed",
						"spl_sales_por_hdr", "invoice_id", id);
				confirmed = confirmed == null ? "N" : confirmed.trim();
				System.out.println("confirmed-->[" + confirmed + "] invID-->["
						+ id + "]");
				if ("Y".equalsIgnoreCase(confirmed)) {
					sql = "select sum(quantity),item_code,lot_no,lot_sl,inv_line_no from invoice_trace where invoice_id = ? "
							+ "group by item_code,lot_no, lot_sl,inv_line_no";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, id);
					rs = pstmt.executeQuery();
					while (rs.next()) {

						System.out
								.println("Inside 1st while Loop of generateDebitNote!!!");
						// lineNoTrace=rs.getInt(1);
						qty = rs.getDouble(1);
						itemCode = rs.getString(2);
						lotNo = rs.getString(3);
						lotSl = rs.getString(4);
						lineNoTraceI = rs.getString(5);
						itemCode = itemCode == null ? "" : itemCode.trim();
						lotNo = lotNo == null ? "" : lotNo.trim();
						lotSl = lotSl == null ? "" : lotSl.trim();

						/*
						 * sql1="seleline_no__invtracect
						 * d.,d.item_code,d.quantity,h.remarks from " +
						 * "misc_drcr_rcp h,misc_drcr_rdet d where
						 * h.tran_id=d.tran_id and h.remarks " + "like ? order
						 * by line_no__invtrace";
						 */
						sql1 = "select sum(nvl(d.quantity__resale,0)),d.item_code,d.lot_no,d.lot_sl,d.line_no__trace "
								+ "from spl_sales_por_hdr h,spl_sales_por_det d "
								+ "where h.tran_id = d.tran_id and h.invoice_id = ? "
								+ "group by d.item_code,d.lot_no,d.lot_sl,d.line_no__trace";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, id);
						rs1 = pstmt1.executeQuery();
						while (rs1.next()) {
							// lineNoTraceM=rs1.getInt(1);

							// START ID DR_aMT < 100
							// Added By Sanket Girme [16-JUL-15]
							// to generate the Debite Note For Invoice id having
							// Total Amount Greter than 100
							// Ingnore the invoice having Debit Note less than
							// 100

							System.out
									.println("*** Generating Debit Note for Invoice having debit Amount greater than 100/-!");

							qtyM = rs1.getDouble(1);
							itemCodeM = rs1.getString(2);
							lotNoM = rs1.getString(3);
							lotSlM = rs1.getString(4);
							itemCodeM = itemCodeM == null ? "" : itemCodeM
									.trim();
							lotNoM = lotNoM == null ? "" : lotNoM.trim();
							lotSlM = lotSlM == null ? "" : lotSlM.trim();

							System.out.println("itemCode-->>[" + itemCode
									+ "] itemCodeM-->[" + itemCodeM + "]");
							System.out.println("lotNo-->>[" + lotNo
									+ "] lotNoM-->[" + lotNoM + "]");
							System.out.println("lotSl-->>[" + lotSl
									+ "] lotSlM-->[" + lotSlM + "]");
							System.out.println("qty-->>[" + qty + "] qtyM-->["+ qtyM + "]");
							if (qtyM < qty
									&& itemCode.equalsIgnoreCase(itemCodeM)
									&& lotNoM.equalsIgnoreCase(lotNo)
									&& lotSlM.equalsIgnoreCase(lotSl)) {
								InvIDTemp.add(id);
								partialInvoiceMapG.put(id + ":" + itemCode
										+ ":" + lotNoM + ":" + lotSlM, qty
										- qtyM);
								System.out.println("partial map11111----->>["
										+ partialInvoiceMapG + "]");
							} else {
								System.out.println("deleted ID--------->>["
										+ deleteId + "]");
								if (deleteId == 1) {
									System.out
											.println("----in remove iterator condition--------");
									it.remove();
									// break;
								}
								deleteId++;
								// inv45dayListL.remove(id);

							}
							// ENF OF IF DR_AMT<100

						} // end while 3
						if (rs1 != null) {
							rs1.close();
							rs1 = null;
						}

					} // end while 2
					System.out.println("InvTemp --->>[" + InvIDTemp + "]");
				} // confirmed id cond. end

			} // end while 1
			System.out.println("unqInvID addAll--->>[" + unqInvID + "]");
			System.out.println("InvIDTemp --->>[" + InvIDTemp + "]");
			if (InvIDTemp.size() > 0) {
				unqInvID.addAll(InvIDTemp);
				InvIDList.clear();
				InvIDList.addAll(unqInvID);
				System.out.println("InvIDList addAll--->>[" + InvIDList + "]");
				// partialInvoice.addAll(InvIDList);

			}
			System.out.println("partialInvoiceMapG --->>[" + partialInvoiceMapG
					+ "]");
			if (inv45dayListL.size() > 0) {
				Iterator<String> it1 = InvIDList.iterator();
				while (it1.hasNext()) {
					String podPrcId = it1.next();
					if (inv45dayListL.contains(podPrcId))
						System.out.println("---in misc arrayList------"
								+ podPrcId);
					it1.remove();
					inv45dayListL.remove(podPrcId);

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			inv45dayListL.add("Error");
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("inv45dayListL addAll--->>[" + inv45dayListL + "]");
		return inv45dayListL;
	}

	private ArrayList<String> getDoneInvoiceIDFromMiscDrCrRcp(Connection conn,
			ArrayList<String> inv45dayListL) throws ITMException {
		System.out
				.println("------in getDoneInvoiceIDFromMiscDrCrRcp12345678------");
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", sql1 = "", itemCodeT = "", lotNoT = "", lotSlT = "", lineNoT = "", itemCodeM = "", lotNoM = "", lotSlM = "", lineNoM = "";
		String invoiceId = "";
		double quantityT = 0, quantityM = 0;
		int listSize = 0, count = 0, noOfRecordMatched = 0, noOfRecords = 0;
		HashSet<String> partialInvID = new HashSet<String>();
		try {
			for (int i = 0; i < inv45dayListL.size(); i++) {
				invoiceId = inv45dayListL.get(i);
				System.out.println("invoiceId in for---->>[" + invoiceId + "]");
				sql = "select sum(quantity),item_code,lot_no,lot_sl,inv_line_no from invoice_trace where invoice_id = ? "
						+ "group by item_code,lot_no,lot_sl,inv_line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invoiceId);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					count++;
					quantityT = rs.getDouble(1);
					itemCodeT = rs.getString(2) == null ? "" : rs.getString(2)
							.trim();
					lotNoT = rs.getString(3) == null ? "" : rs.getString(3)
							.trim();
					lotSlT = rs.getString(4) == null ? "" : rs.getString(4)
							.trim();
					lineNoT = rs.getString(5) == null ? "" : rs.getString(5)
							.trim();

					sql1 =  " select sum(d.quantity) as Quantity,d.item_code,d.lot_no,d.lot_sl,d.line_no__invtrace from misc_drcr_rcp h,misc_drcr_rdet d "
							+ "where h.tran_id = d.tran_id  and h.remarks like ? and quantity is not null "
							+ "group by d.item_code,d.lot_no,d.lot_sl,d.line_no__invtrace";

					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, "%POD%" + invoiceId.trim());
					rs1 = pstmt1.executeQuery();
					while (rs1.next()) {
						quantityM = rs1.getDouble(1);
						itemCodeM = rs1.getString(2) == null ? "" : rs1.getString(2).trim();
						lotNoM = rs1.getString(3) == null ? "" : rs1.getString(3).trim();
						lotSlM = rs1.getString(4) == null ? "" : rs1.getString(4).trim();
						lineNoM = rs1.getString(5)== null? "" : rs1.getString(5).trim();
						System.out.println("count1--------------->>[" + count+ "]");
						System.out.println("quantityT--->>[" + quantityT+ "] quantityM-->[" + quantityM + "]");
						System.out.println("itemCodeT--->>[" + itemCodeT+ "] itemCodeM-->[" + itemCodeM + "]");
						System.out.println("lotNoT--->>[" + lotNoT+ "] lotNoM-->[" + lotNoM + "]");
						System.out.println("lotSlT--->>[" + lotSlT+ "] lotSlM-->[" + lotSlM + "]");
						System.out.println("lineNoT--->>[" + lineNoT+ "] lineNoM-->[" + lineNoM + "]");
						if (itemCodeT.equalsIgnoreCase(itemCodeM)
								&& lotNoT.equalsIgnoreCase(lotNoM)) {
							if (lotSlT.equalsIgnoreCase(lotSlM)
									&& lineNoT.equalsIgnoreCase(lineNoM)) {
								if (quantityM < quantityT) {
									System.out.println("record matched........at count "+ count);
									partialInvID.add(invoiceId);
									partialInvoiceMapG.put(invoiceId + ":"+ itemCodeT + ":" + lotNoT + ":"
											+ lotSlT, quantityT - quantityM);
									noOfRecordMatched++;
								}
								if (quantityM == quantityT) {
									System.out.println("Quantity matched for inv id....."
													+ invoiceId);
									partialInvID.add(invoiceId);
								}
							}
						}

					} // end while misc
					if (rs1 != null) {
						rs1.close();
						rs1 = null;
					}
					if (pstmt1 != null) {
						pstmt1.close();
						pstmt1 = null;
					}

				} // end while trace
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}

			}// end for loop

			System.out.println("outside for partialInvoiceMapG-->>["
					+ partialInvoiceMapG + "]");
			System.out.println("outside for partialInvID-->>[" + partialInvID
					+ "]");
			if (inv45dayListL.size() > 0) {
				Iterator<String> itr = inv45dayListL.iterator();
				while (itr.hasNext()) {
					String id = itr.next();
					if (partialInvID.contains(id)) {
						System.out
								.println("Invoice id removed--->[" + id + "]");
						itr.remove();
						inv45dayListL.remove(id);
						continue;
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			inv45dayListL.clear();
			inv45dayListL.add("Error");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19

		}
		return inv45dayListL;
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

	public String actualQtyPODProcessPartial(Connection conn) // partialInvoiceMap
	{
		System.out
				.println("-------for partial invoice id Dr note1233 -----------");
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", tranIdL = "", sql1 = "", itemCodeP = "";
		double actQty = 0, doneQty = 0, invQtyP = 0;
		ITMDBAccessEJB itmdbAccess1 = new ITMDBAccessEJB();
		Map<String, Double> partialInvItem = new HashMap();
		Map<String, String> partialMap = new HashMap();
		Set<String> partialID = new HashSet();
		ArrayList<String> partialItem = new ArrayList();
		ArrayList<String> partialLotNo = new ArrayList();
		ArrayList<String> partialLotSl = new ArrayList();
		Map<String, Object> invoiceDetailsMap = null;
		HashMap<String, String> podTraceMap = new HashMap();
		HashMap<String, String> schemeMap = new HashMap();
		Set<String> splitPartial = new HashSet<String>();
		Map<String, Double> schemeQty = null;
		String lotNo = "", lotSl = "", itemCode = "", itemCodeOrd = "", itemActive = "", detail2xmlString = "", errString = "", itemCodeInvTrace = "";
		String custCode = "", currCode = "", partial = "N", QtyInvTrace = "", scheme = "N", errCode = "", sreturnTranID = "", priceList = "";
		double invRate = 0, invQty = 0, discount = 0, priceListRate = 0, detail3Amt = 0, detail3AmtT = 0, debitNoteAmtDetail = 0;
		double debitNoteAmtHdr = 0, ExchRate = 0, actualRate = 0, chargQty = 0, freeQty = 0, netPTS = 0, sreturnQuantity = 0, partialQty = 0;
		int lineNotrace = 0, lineNo = 0, noOfInvoicePrc = 0, totalInvoice = 0;
		ProofOfDelivery pod = ProofOfDelivery.getInstance();
		ProofOfDelivery podObject = ProofOfDelivery.getInstance();
		ProofOfDeliveryConf podC = ProofOfDeliveryConf.getInstance();
		int count = 0, cnt1 = 0, cnt2 = 0, itemCnt = 0;
		Connection conn1 = null;
		boolean invstatus;
		boolean invSreturn = false;
		ConnDriver connDriver1 = new ConnDriver();
		try {
			//Changes and Commented By Bhushan on 13-06-2016 :START
			  //conn1 = connDriver1.getConnectDB("DriverITM");
			  conn1 = getConnection();
			  //Changes and Commented By Bhushan on 13-06-2016 :END
			/*
			 * sql="select sum(d.quantity__resale),d.item_code,d.quantity__inv
			 * from spl_sales_por_hdr h,spl_sales_por_det d" +"where h.TRAN_ID =
			 * d.TRAN_ID and h.invoice_id= ? GROUP BY d.item_code,
			 * d.quantity__inv";
			 */
			System.out.println("partialInvoiceMapG--->>" + partialInvoiceMapG);
			if (partialInvoiceMapG.size() > 0) {
				for (String id : partialInvoiceMapG.keySet()) {
					String[] invID = id.split(":");
					partialID.add(invID[cnt1]);
					cnt1++;
					partialItem.add(invID[cnt1]);
					cnt1++;
					partialLotNo.add(invID[cnt1]);
					cnt1++;
					partialLotSl.add(invID[cnt1]);
					cnt1 = 0;
				}
			}
			System.out.println("partialID set--->>[" + partialID + "]");
			System.out.println("partialItem--->>[" + partialItem + "]");
			System.out.println("partialLotNO--->>[" + partialLotNo + "]");
			System.out.println("partialLotSl--->>[" + partialLotSl + "]");
			totalInvoice = partialID.size();
			System.out.println("totalInvoice partial--->>[" + totalInvoice
					+ "]");
			//partialID = getInvFromSalesReturn(partialID, conn);

			for (String invIdPartial : partialID) {
				// itemCode=partialItem.get(itemCnt)==null ? ""
				// :partialItem.get(itemCnt).trim();
				// invQty=partialInvoiceMapG.get(invIdPartial+":"+itemCode);
				detail2xmlString = "";
				sql1 = "select rate,lot_no,lot_sl,discount,item_code,inv_line_no from invoice_trace "
						+ "where invoice_id = ?";

				pstmt = conn.prepareStatement(sql1);
				pstmt.setString(1, invIdPartial);
				// pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					invRate = rs.getDouble(1);
					lotNo = rs.getString(2);
					lotSl = rs.getString(3);
					discount = rs.getDouble(4);
					itemCode = rs.getString(5);
					lineNotrace = rs.getInt(6);

					itemCode = itemCode == null ? "" : itemCode.trim();
					lotNo = lotNo == null ? "" : lotNo.trim();
					lotSl = lotSl == null ? "" : lotSl.trim();
					System.out.println("end getting data...................");
					// Discount need discussion
					if (discount > 0) {
						invRate = invRate - (invRate * discount / 100);
						System.out.println("Discount for invoice id--->>["
								+ invIdPartial + "]" + "inv Rate-->>["
								+ invRate + "]");
					}
					// if(partialItem.contains(itemCode) &&
					// partialLotNo.contains(lotNo) &&
					// partialLotSl.contains(lotSl)){
					if (partialInvoiceMapG.containsKey(invIdPartial + ":"
							+ itemCode + ":" + lotNo + ":" + lotSl)) {
						System.out.println("invIdPartial--->[" + invIdPartial
								+ "]");
						System.out.println("itemCode111--->[" + itemCode + "]");
						System.out.println("lotNo1--->[" + lotNo + "]");
						System.out.println("lotSl22--->[" + lotSl + "]");
						invQty = partialInvoiceMapG.get(invIdPartial + ":"
								+ itemCode + ":" + lotNo + ":" + lotSl);
						System.out.println("Remain111 inv qty--->>[" + invQty
								+ "] inv id--->[" + invIdPartial + "]");
					} else {
						System.out
								.println("Key not fount so next iteration......");
						continue;
					}
					// check if any sales return
					sreturnTranID = pod.getSalesReturnTranID(conn, "sreturn",
							"tran_id", "invoice_id", invIdPartial);
					sreturnTranID = sreturnTranID == null ? "" : sreturnTranID
							.trim();
					if (sreturnTranID.length() > 0) {
						System.out
								.println("-----Got sales return tran id-----------");
						sql1 = "select item_code,quantity from sreturndet where tran_id = ? and item_code = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, sreturnTranID);
						pstmt1.setString(2, itemCode);
						rs1 = pstmt1.executeQuery();

						if (rs1.next()) {
							sreturnQuantity = rs1.getDouble(2);
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
					itemActive = pod.getColumnDescr(conn, "active", "item",
							"item_code", itemCode);
					itemActive = itemActive == null ? "" : itemActive.trim();
					if (itemActive.equalsIgnoreCase("N")
							|| itemActive.length() <= 0) {
						errCode = "VTITMCNAC"; // Item code not active
						errString = itmdbAccess1.getErrorString("", errCode,
								"", "", conn);
						errString = newErrorMessage(errString, invIdPartial);
						return errString; // item code not active
					}
					java.util.Date tranDate = new Date();
					priceList = podObject.getNameOrDescrForCode(conn, "site",
							"price_list", "site_code", siteCodeG);
					priceListRate = getPriceListRate(priceList, tranDate,
							itemCode, lotNo, "", conn);
					System.out.println("priceListRate----->>[" + priceListRate
							+ "]");
					if (priceListRate <= 0) {
						errCode = "VTPRCLNE";
						errString = itmdbAccess1.getErrorString("", errCode,
								"", "", conn);
						errString = newErrorMessage(errString, invIdPartial);
						return errString;
					}

					if (isSchemeApply(conn, itemCode, invIdPartial)) {
						// java code if scheme is apply
						System.out
								.println("-------Scheme is apply -----------");
						schemeQty = schemeApply(conn, invIdPartial, itemCode);
						System.out.println("schemeQty map---->>[" + schemeQty
								+ "]");
						if (schemeQty.size() > 0) {
							if (schemeQty.get("success") < 0) {
								errCode = "VTPRCNCP"; // Scheme related error
								// or sql error.
								errString = itmdbAccess1.getErrorString("",
										errCode, "", "", conn);
								errString = newErrorMessage(errString,
										invIdPartial);
								return errString;
							} else if (schemeQty.get("success") == 1) {
								scheme = "Y";
								schemeMap.put(invIdPartial + "," + itemCode,
										scheme);
								chargQty = schemeQty.get("charge_qty");
								freeQty = schemeQty.get("free_qty");

								netPTS = chargQty / (chargQty + freeQty)
										* priceListRate;

								System.out
										.println("Net PchangeEditable(this)TS rate----->>["
												+ netPTS + "]");
								actualRate = netPTS - invRate;
								detail3Amt = actualRate * invQty;
								detail3AmtT = detail3AmtT + detail3Amt;
								lineNo++;
								detail2xmlString = detail2xmlString
										+ podC.getDetailsXmlString(conn,
												invIdPartial, lineNotrace,
												lineNo, actualRate, invQty,
												lotNo, lotSl, itemCode,
												detail3Amt);
							}
						}
					} else {
						System.out
								.println("-------Scheme not apply -----------");
						schemeMap.put(invIdPartial + "," + itemCode, scheme);
						scheme = "N";
						actualRate = priceListRate - invRate;
						detail3Amt = actualRate * invQty;
						System.out.println("invQty-------->>[" + invQty + "]");
						System.out
								.println("invRate-------->>[" + invRate + "]");
						System.out.println("Debit note amount-------->>["
								+ detail3Amt + "]");
						System.out
								.println("Actual rate after deducting inv rate-------->>["
										+ detail3Amt + "]");
						detail3AmtT = detail3AmtT + detail3Amt;
						lineNo++;
						detail2xmlString = detail2xmlString
								+ podC.getDetailsXmlString(conn, invIdPartial,
										lineNotrace, lineNo, actualRate,
										invQty, lotNo, lotSl, itemCode,
										detail3Amt);

					} // end isSchemeApply

				} // end invoice trace while
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}

				System.out.println("Detail2Xml for partial--->>["
						+ detail2xmlString + "]");
				System.out.println("detail3AmtT for partial--->>["
						+ detail3AmtT + "]");
				if (detail3AmtT > 0) {
					invoiceDetailsMap = pod.getDetailsFromInvoice(conn,
							invIdPartial);
					custCode = invoiceDetailsMap.get("cust_code").toString();
					currCode = pod.checkNull(invoiceDetailsMap.get("curr_code")
							.toString()); // xmlStringAll
					ExchRate = Double.parseDouble(invoiceDetailsMap.get(
							"exch_rate").toString());

					// check the debit note is greater than 100 or not added by sanket on Nov 2015
					invstatus = checkDebitAmt(invIdPartial, conn);
					// check wheather the sreturn is generated for all qty. added by ritesh on 29 feb 16
					invSreturn = checkSalesReturn(invIdPartial, conn);
					System.out.println("invstatus invIdPartial =>"
							+ invIdPartial);
					System.out.println("Debit Amt status:"+invSreturn+":"+"Slae retrun status:"+invSreturn);
					if (invstatus && invSreturn) {
						errString = podC.genDebitNote("PRC", invIdPartial,
								custCode, siteCodeG, xtraParamsG, currCode,
								ExchRate, detail2xmlString, detail3AmtT, conn);
						System.out
								.println("------Error string after DN save--->>["
										+ errString + "]");
					} else {
						errString = "Invalid Debit";
					}
					if (errString.indexOf("Success") > -1) {
						System.out
								.println("Tran id for Misc. debit note------->>["
										+ (podC.debitNoteTranID).trim() + "]");
						MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
						errString = confDebitNote.confirm(
								(podC.debitNoteTranID).trim(), xtraParamsG, "",
								conn);
						// System.out.println("Error String on DB Confirmation:
						// "+errString);
						if ((errString != null)
								&& errString.indexOf("CONFSUCCES") > -1) {
							int cnt = insertDataInPodTrace(conn1, invIdPartial,
									podC.xmlStringAll, scheme, "Y", "Y");
							noOfInvoicePrc++;
							System.out.println("noOfInvoicePrc in partial-->>["
									+ noOfInvoicePrc + "]");
							/*
							 * if(noOfInvoicePrc==totalInvoice){
							 * if(invdoneCnt==1){ errCode="VTPRCCSF"; errString =
							 * itmdbAccess1.getErrorString("", errCode, "", "",
							 * conn); conn.commit(); return errString; } }else
							 * if(invdoneCnt==1){
							 * System.out.println("invdoneCnt1----true");
							 * errCode="VTPPRCSF"; errString =
							 * itmdbAccess1.getErrorString("", errCode, "", "",
							 * conn); conn.commit(); return errString; }
							 */

							if (cnt > 0) {
								conn1.commit();
							}

							/*
							 * errCode="VTPRCCSF"; errString =
							 * itmdbAccess1.getErrorString("", errCode, "", "",
							 * conn); return errString;
							 */

							// return "Success";
						} else {
							conn.rollback();
							/*
							 * int cnt=insertDataInPodTrace(conn1, invIdPartial,
							 * podC.xmlStringAll, schemeMap, partialMap,"N");
							 * if(cnt > 1) conn1.commit();
							 */
							errString = newErrorMessage(errString, invIdPartial);
							return errString;
						}
					} else {
						conn.rollback();
						/*
						 * int cnt=insertDataInPodTrace(conn1, invIdPartial,
						 * podC.xmlStringAll, schemeMap, partialMap,"N"); if(cnt >
						 * 1) conn1.commit();
						 */
						errString = newErrorMessage(errString, invIdPartial);
						return errString;
					}

				} else {
					errCode = "VTNGAMTE"; // negative amount
					errString = itmdbAccess1.getErrorString("", errCode, "",
							"", conn);
					errString = newErrorMessage(errString, invIdPartial);
					conn.rollback();
					return errString;
				}

				itemCnt++;
			} // end for loop for partial

		}// end try
		catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}

		System.out.println("Done quantity---->>[" + doneQty + "]");
		return errString;

	}

	private boolean checkSalesReturn(String invIdPartial, Connection conn) throws SQLException, ITMException {
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", sql1 = "";
		double  invQty = 0d,sreturnQuantity=0d;
		boolean isInvoice = true;
//		ProofOfDelivery pod = ProofOfDelivery.getInstance();
		try {
			
				sql = "select sum(quantity) from invoice_trace where invoice_id = ? order by inv_line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invIdPartial);
				rs = pstmt.executeQuery();

				while (rs.next())
				{
					invQty = rs.getDouble(1);
				}
				System.out.println("-----Got sales return tran id-----------");
				sql1 = "select nvl(sum(d.quantity),0) from sreturndet d ,sreturn h where d.tran_id = h.tran_id  and  h.invoice_id = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, invIdPartial);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					sreturnQuantity = rs1.getDouble(1);
				}
				System.out.println("Sales return------>>["
						+ sreturnQuantity + "]");
				invQty = invQty - sreturnQuantity;
				if(invQty <= 0)
				{
					isInvoice = false;   // Sale return generated for all qty.
				}

				
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
	
		finally
		{
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
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
		
		
		return isInvoice;
	}
	private ArrayList<String> getInvFromSalesReturn(ArrayList<String> invoiceList, Connection conn) throws SQLException, ITMException {
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", sql1 = "";
		double  invQty = 0d,sreturnQuantity=0d;
		boolean isInvoice = true;
		ArrayList<String> invoiceRmvList =  new ArrayList();
		try {
			
			   for(String invIdPartial : invoiceList)
			   {
				sql = "select sum(quantity) from invoice_trace where invoice_id = ? order by inv_line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invIdPartial);
				rs = pstmt.executeQuery();

				while (rs.next())
				{
					invQty = rs.getDouble(1);
				}
				System.out.println("-----Got sales return tran id-----------");
				sql1 = "select nvl(sum(d.quantity),0) from sreturndet d ,sreturn h where d.tran_id = h.tran_id  and  h.invoice_id = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, invIdPartial);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					sreturnQuantity = rs1.getDouble(1);
				}
				System.out.println("Sales return------>>["
						+ sreturnQuantity + "]");
				invQty = invQty - sreturnQuantity;
				if(invQty <= 0)
				{
					isInvoice = false;   // Sale return generated for all qty.
					invoiceRmvList.add(invIdPartial);
					System.out.println("Removed invoice id from sreturn ::"+invIdPartial);
				}

			   }
			   invoiceList.removeAll(invoiceRmvList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
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
		
		System.out.println("Returning invoice list from sreturn::"+invoiceList.toString());
		return invoiceList;
	}

	public void partialInvDebitNote(Connection conn,
			ArrayList<String> invIdPartial) throws ITMException {
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", tranIdL = "", sql1 = "";
		String lotNo = "", lotSl = "", itemCode = "", itemCodeOrd = "", itemActive = "", detail2xmlString = "", errString = "";
		String custCode = "", currCode = "", partial = "N", QtyInvTrace = "", scheme = "N", errCode = "", sreturnTranID = "";
		double invRate = 0, invQty = 0, discount = 0, priceListRate = 0, detail3Amt = 0, detail3AmtT = 0, debitNoteAmtDetail = 0, sreturnQuantity = 0;
		int lineNotrace = 0;
		ProofOfDelivery pod = ProofOfDelivery.getInstance();
		try {
			for (String invId : invIdPartial) {
				sql = "select rate,quantity,lot_no,lot_sl,item_code,inv_line_no,discount,item_code__ord,invoice_id "
						+ "from invoice_trace where invoice_id = ? order by inv_line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invId);
				rs = pstmt.executeQuery();

				while (rs.next()) {
					invRate = rs.getDouble(1);
					invQty = rs.getDouble(2);
					lotNo = rs.getString(3);
					lotSl = rs.getString(4);
					itemCode = rs.getString(5);
					lineNotrace = rs.getInt(6);
					discount = rs.getDouble(7);
					itemCodeOrd = rs.getString(8);

					lotNo = lotNo == null ? "" : lotNo.trim();
					lotSl = lotSl == null ? "" : lotSl.trim();
					itemCode = itemCode == null ? "" : itemCode.trim();
					itemCodeOrd = itemCodeOrd == null ? "" : itemCodeOrd.trim();

					// Discount need discussion
					if (discount > 0) {
						invRate = invRate - (invRate * discount / 100);
						System.out
								.println("Discount for invoice id--->>["
										+ invId + "]" + "inv Rate-->>["
										+ invRate + "]");
					}
					// check if any sales return
					sreturnTranID = pod.getSalesReturnTranID(conn, "sreturn",
							"tran_id", "invoice_id", invId);
					sreturnTranID = sreturnTranID == null ? "" : sreturnTranID
							.trim();
					if (sreturnTranID.length() > 0) {
						System.out
								.println("-----Got sales return tran id-----------");
						sql1 = "select item_code,quantity from sreturndet where tran_id = ? and item_code = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, sreturnTranID);
						pstmt1.setString(2, itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) {
							sreturnQuantity = rs1.getDouble(2);
						}
						if (rs1 != null) {
							rs1.close();
							rs1 = null;
						}
						if (pstmt1 != null) {
							pstmt1.close();
							pstmt1 = null;
						}
						System.out.println("Sales return------>>["
								+ sreturnQuantity + "]");
						invQty = invQty - sreturnQuantity;
					}

				} // end while
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
	}

	public double getPriceListRate(String priceList, java.util.Date tranDate,
			String itemCode, String lotNo, String type,
			Connection connectionObject) throws RemoteException, ITMException {
		double rate = 0;
		String siteCode = "";
		String locCode = "";
		String lotSl = "";

		String errCode = "";
		Statement stmt;
		ResultSet rs = null;
		String sql = "";

		// boolean connectionState = false;
		try {
			System.out.println("in getPriceListRate method-------------------");

			stmt = connectionObject.createStatement();
			sql = "SELECT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = '"
					+ priceList + "'";
			rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return -1;
			} else {
				type = rs.getString(1);
			}
			if (type.equalsIgnoreCase("L")) // List Price
			{

				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"
						+ priceList
						+ "' AND ITEM_CODE = '"
						+ itemCode
						+ "' AND LIST_TYPE = 'L' AND EFF_FROM <= sysdate AND VALID_UPTO >= sysdate";
				System.out.println("sql in list type L--->>[[" + sql + "]]");
				rs = stmt.executeQuery(sql);
				if (!rs.next()) {
					return -1; // Denotes Error
				} else {
					rate = rs.getDouble(1);
				}
			} else if (type.equalsIgnoreCase("D")) // Despatch
			/*
			 * Selecting rate from pricelist for L, if not found picking up from
			 * batch
			 */
			{
				try {
					sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"
							+ priceList
							+ "' AND ITEM_CODE = '"
							+ itemCode
							+ "' AND LIST_TYPE = 'L'	AND EFF_FROM <= sysdate AND VALID_UPTO >= sysdate";
					rs = stmt.executeQuery(sql);
					if (!rs.next()) {
						rate = 0;
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"
								+ priceList
								+ "' AND ITEM_CODE = '"
								+ itemCode
								+ "' AND LIST_TYPE = 'B' AND LOT_NO__FROM <= '"
								+ lotNo
								+ "' AND LOT_NO__TO >= '"
								+ lotNo
								+ "' AND EFF_FROM <= sysdate AND VALID_UPTO >= sysdate";
						rs = stmt.executeQuery(sql);
						if (!rs.next()) {
							return -1; // Denotes Error
						} else {
							rate = rs.getDouble(1);
						}
					} else {
						rate = rs.getDouble(1);
					}
				} catch (SQLException ie) {
					System.out
							.println("Exception: PODProcess: getPriceListRate: type D: ==>"
									+ ie);
					ie.printStackTrace();
					return -1;
				}
			} else if (type.equalsIgnoreCase("B")) // Batch Price
			{
				rate = 0;
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"
						+ priceList + "' AND ITEM_CODE = '" + itemCode
						+ "' AND LIST_TYPE = 'B' AND LOT_NO__FROM <= '" + lotNo
						+ "' AND LOT_NO__TO >= '" + lotNo
						+ "' AND EFF_FROM <= sysdate AND VALID_UPTO >= sysdate";
				System.out.println("price list sql---->>[" + sql + "]");
				rs = stmt.executeQuery(sql);
				System.out.println("After excution-----------");
				if (!rs.next()) {
					return -1; // Denotes Error
				} else {
					rate = rs.getDouble(1);
				}
			} else if (type.equalsIgnoreCase("M") || type.equalsIgnoreCase("N")) // Discount
			// Price
			{
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"
						+ priceList + "' AND ITEM_CODE = '" + itemCode
						+ "' AND LIST_TYPE = '" + type
						+ "' AND EFF_FROM <= sysdate AND VALID_UPTO >= sysdate";
				rs = stmt.executeQuery(sql);
				if (!rs.next()) {
					return -1; // Denotes Error
				} else {
					rate = rs.getDouble(1);
				}
			} else if (type.equalsIgnoreCase("I")) // Inventory
			{

				rate = 0;
				// To check is lot no is null or not and fetch accordingly
				if (lotSl.trim().length() == 0 || lotSl.length() == 0) {
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = '"
							+ itemCode + "' AND SITE_CODE = '" + siteCode
							+ "' AND LOC_CODE = '" + locCode
							+ "' AND LOT_NO = '" + lotNo + "'";
					rs = stmt.executeQuery(sql);
					if (!rs.next()) {
						return -1;
					} else {
						rate = rs.getDouble(1);
					}
				} else {
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = '"
							+ itemCode + "' AND SITE_CODE = '" + siteCode
							+ "' AND LOC_CODE = '" + locCode
							+ "' AND LOT_NO = '" + lotNo + "' AND LOT_SL '"
							+ lotSl + "'";
					rs = stmt.executeQuery(sql);
					if (rs.next()) {
						rate = rs.getDouble(1);
					} else {
						return -1;
					}
				}
			}
			if (rs != null) {
				rs.close();
			}

		} catch (Exception e) {
			System.out.println("Exception :PODProcess :getPriceListRate:"
					+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return rate;
	}

}
