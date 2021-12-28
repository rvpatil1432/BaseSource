package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

@Stateless
public class ProofOfDeliveryConf extends ActionHandlerEJB implements
		ProofOfDeliveryConfLocal, ProofOfDeliveryConfRemote {
	String debitNoteTranID = "", xmlStringAll = "";
	E12GenericUtility genericUtility= new  E12GenericUtility();
	boolean isRegPriceNotFound = false;
	boolean isError = false;

	public static ProofOfDeliveryConf getInstance() {
		return new ProofOfDeliveryConf();
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag)
			throws RemoteException, ITMException {
		System.out
				.println("--------------confirm method of ProofOfDeliveryConf ------------- : ");
		String retString = "";
		try {

			retString = confirmPOD(tranId, xtraParams, forcedFlag);

		} catch (Exception e) {
			System.out.println("Exception :ProofOfDeliveryConf :confirm():"
					+ e.getMessage() + ":");
			retString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return retString;
	}

	private String confirmPOD(String tranId, String xtraParams,
			String forcedFlag) throws ITMException {
		System.out
				.println("---------Class : ProofOfDeliveryConf-->> confirm method called-----------");
		isError = false;
		tranId = tranId == null ? "" : tranId.trim();
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver ConnDriver = new ConnDriver();
		PreparedStatement pstmt = null;
		String errString = "", sql = "", loginSite = "", wfStatus = "", itemCode = "", regulatedPrice = "", itemActive = "";
		String itmCodeDt2 = "", lotNo = "", lotSl = "", detail2xmlString = "", invoiceID = "";
		double aprvRate = 0.0, PODrate = 0.0, PODqty = 0.0, allowedAmt = 0.0, stockistAmt = 0.0, debitNoteAmtDetail = 0.0;
		double debitNoteAmtHdr = 0;
		int updCnt = 0, lineNo = 0, lineNotrace = 0;
		ITMDBAccessEJB itmdbAccess = new ITMDBAccessEJB();
		ProofOfDelivery podObj = null;
		HashMap<Integer, Double> mapDebitNoteAmt = new HashMap<Integer, Double>();
		try {
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverValidator");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			conn.setAutoCommit(false);
			String confirmed = "";
			sql = "select confirmed,wf_status,invoice_id from spl_sales_por_hdr WHERE tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				confirmed = rs.getString(1) == null ? "N" : rs.getString(1);
				wfStatus = rs.getString(2) == null ? "O" : rs.getString(2);
				invoiceID = rs.getString(3);
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (wfStatus != null && wfStatus.equalsIgnoreCase("O")) {
				// Opened transaction can not confirm
				errString = itmdbAccess.getErrorString("", "VTOPCCONF", "", "",
						conn);
				return errString;
			}
			if (wfStatus != null && wfStatus.equalsIgnoreCase("R")) {
				// Rejected transaction can not confirm
				errString = itmdbAccess.getErrorString("", "VTRJCCONF", "", "",
						conn);
				return errString;
			}
			if (confirmed != null && confirmed.equalsIgnoreCase("Y")) {
				// if already confirm then show error
				errString = itmdbAccess.getErrorString("", "VTALCONF", "", "",
						conn);
				return errString;
			}

			// check in misc_drcr_rcp POD already received or not
			podObj = new ProofOfDelivery();
			if (podObj.isRecordPresentInMiscDrCrRcp(conn, invoiceID)) {
				errString = itmdbAccess.getErrorString("", "VTPODARI", "", "",
						conn);
				return errString;
			}
			System.out.println("-------wfStatus before confirm -------"
					+ wfStatus);
			// Only submitted transaction allowed to confirm
			if (wfStatus.trim().equalsIgnoreCase("S")) {
				String invoiceId = "", custCode = "", siteCode = "", currCode = "", loginCode = "";
				double ExchRate = 0.0, actualDBamount = 0.0, rateInv = 0, qtyInv = 0, detail3Amt = 0, detail3AmtT = 0;
				loginCode = genericUtility
						.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
				System.out.println("Login Code ------->> " + loginCode);

				sql = "";
				sql = "select invoice_id,cust_code,site_code,curr_code,exch_rate from spl_sales_por_hdr where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					invoiceId = rs.getString(1);
					custCode = rs.getString(2) == null ? "" : rs.getString(2);
					siteCode = rs.getString(3) == null ? "" : rs.getString(3);
					currCode = rs.getString(4) == null ? "" : rs.getString(4);
					ExchRate = rs.getDouble(5);
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}

				sql = "";
				sql = "select item_code,rate__resale,quantity__resale,aprv_rate,line_no,lot_no,lot_sl,rate__inv,quantity__inv,line_no__trace "
						+ "from spl_sales_por_det where tran_id = ? order by line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					itemCode = rs.getString(1);
					PODrate = rs.getDouble(2);
					PODqty = rs.getDouble(3);
					aprvRate = rs.getDouble(4);
					lineNo = rs.getInt(5);
					lotNo = rs.getString(6);
					lotSl = rs.getString(7);
					rateInv = rs.getDouble(8);
					qtyInv = rs.getDouble(9);
					lineNotrace = rs.getInt(10);

					itemCode = itemCode == null ? "" : itemCode.trim();
					lotNo = lotNo == null ? "" : lotNo.trim();
					lotSl = lotSl == null ? "" : lotSl.trim();

					// debitNoteAmtDetail=getDebitNoteAmount(conn, tranId,
					// lineNo,aprvRate,PODrate,PODqty);
					debitNoteAmtDetail = getDebitNoteAmount(rateInv, qtyInv);
					debitNoteAmtHdr = debitNoteAmtHdr + debitNoteAmtDetail;
					detail3Amt = (PODrate - aprvRate) * PODqty;
					if (detail3Amt < 0) {
						detail3Amt = 0;
					}
					if (detail3Amt < 0) {
						System.out
								.println("debit note amount less than 0--------");
						detail3Amt = 0;
					}
					detail3AmtT = detail3AmtT + detail3Amt;
					mapDebitNoteAmt.put(lineNo, detail3Amt);
					// detail2xmlString=getDetailsXmlString(conn, xtraParams,
					// invoiceId, aprvRate, debitNoteAmtDetail);
					detail2xmlString = detail2xmlString
							+ getDetailsXmlString(conn, invoiceId, lineNotrace,
									lineNo, aprvRate, PODqty, lotNo, lotSl,
									itemCode, detail3Amt);

				} // end while
				System.out.println("detail2xmlString---->>[["
						+ detail2xmlString + "]]");
				System.out
						.println("Final detail3AmtT in debit note---------->> : "
								+ detail3AmtT);

				// Debit note amount cannot less than hundred.
				// [31-JUL-2015] Sanket Girme
				if (detail3AmtT >= 100) {
					errString = genDebitNote("POD", invoiceId, custCode,
							siteCode, xtraParams, currCode, ExchRate,
							detail2xmlString, detail3AmtT, conn);
					System.out.println("------Error string after DN save--->>["
							+ errString + "]");
					if (errString.indexOf("Success") > -1) {
						System.out
								.println("Confirmation of Debit note----------");
						MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
						errString = confDebitNote.confirm(debitNoteTranID,
								xtraParams, "", conn);
						// errString =
						// confirmDebitNote("drcrrcp_dr",debitNoteTranID,xtraParams,"",conn);
						System.out.println("Error String on DB Confirmation: "
								+ errString);
						if ((errString != null)
								&& errString.indexOf("CONFSUCCES") > -1) {
							updCnt = updatePodHeader(conn, tranId, loginCode);
							System.out
									.println("return count from updateDebitNoteAmounts--->["
											+ updCnt + "]");
							if (updCnt > 0) {
								conn.commit();
								errString = itmdbAccess.getErrorString("",
										"VTCONSUCF", "", "", conn);
								System.out
										.println("------@@@Debit Note Generated successfully for amount : "
												+ actualDBamount);
								System.out
										.println("Confirmation of Debit note done successfully----------");
							} else {
								errString = itmdbAccess.getErrorString("",
										"VTNCONFT", "", "", conn);
								conn.rollback();
								System.out
										.println("transaction not confirmed----------");
							}
						} else {
							errString = itmdbAccess.getErrorString("",
									"VTNCONFT", "", "", conn);
							conn.rollback();
						}
					} else {
						// errString = itmdbAccess.getErrorString("",
						// "VTNCONFT", "", "", conn);
						conn.rollback();
						return errString;

					}
				} else {
					errString = itmdbAccess.getErrorString("", "VTCOSUCFT", "",
							"", conn);
					System.out
							.println("Debit note generated zero for invoice ID-->>["
									+ invoiceId + "]");
					debitNoteTranID = "";
					updCnt = updatePodHeader(conn, tranId, loginCode);
					if (updCnt > 0)
						conn.commit();
				}

			} // end condition for wfstatus ='S'
			else {
				conn.rollback();
				errString = itmdbAccess.getErrorString("", "VTNSUBCON", "", "",
						conn);
			}

		} // end try
		catch (SQLException se) {
			System.out.println("SQLException : class ProofOfDeliveryConf : ");
			se.printStackTrace();
			try {
				isError = true;
				conn.rollback();
			} catch (Exception e) {
				System.out
						.println("Exception : Occure during rollback........");
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
		} catch (Exception e) {
			try {
				isError = true;
				conn.rollback();
			} catch (Exception e1) {
				e.printStackTrace();
				System.out.println("Exception Class [ProofOfDeliveryConf]::"
						+ e.getMessage());
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
				if (isError) {
					errString = itmdbAccess.getErrorString("", "VTNCONFT", "",
							"", conn);
				}

			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();

			}
		}
		System.out.println("[ProofOfDeliveryConf]errstring :" + errString);
		return errString;
	}

	public java.sql.Timestamp getInvoiceDate(Connection conn, String invID) throws ITMException {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String sql = "";
		java.sql.Timestamp invDate = null;
		try {
			sql = "select conf_date from invoice where invoice_id= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				invDate = rs.getTimestamp(1);
			}

		} catch (Exception e) {
			System.out.println("Exception in getInvoiceDate-------");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return invDate;
	}

	protected String genDebitNote(String process, String invoiceId,
			String custCode, String siteCode, String xtraParams,
			String currencyCode, double exchRate, String detail2xmlString,
			double detail3AmtT, Connection conn) throws ITMException {
		System.out.println("<--------Inside genDebitNote----------->");
		StringBuffer xmlBuffer = new StringBuffer();
		String xmlString = "", sql = "", refNo = "", remarks = "";
		String retString = "";
		//GenericUtility genutility = null;
		SimpleDateFormat sdf = null;
		String currAppdate = "", invoiceDate = "";
		java.sql.Timestamp currDate = null;
		String itemSer = "";
		String finEntity = "", acctCode = "", cctrCode = "", acctCodetotAR = "", cctrCodetotAR = "", dbTranID = "";
		String crTerm = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;		
		String userId = "";//Added By Pavan R 27/DEC/17

		try {
			//genutility = GenericUtility.getInstance();
			E12GenericUtility genutility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genutility.getApplDateFormat());
			
			//added by Pavan R on 27/DEC/17 userId passwed to savData() and processRequest()
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("userId::["+userId+"]");
			
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			currAppdate = sdf.format(currDate);
			java.sql.Timestamp invDate = getInvoiceDate(conn, invoiceId);
			if (invDate != null) {
				invoiceDate = sdf.format(invDate);
			}
			System.out.println("Process------>>[" + process + "]");
			if (process.equalsIgnoreCase("PRC"))
				remarks = "Auto Gen. Debit Note-POD Process-Invoice ID - "
						+ invoiceId;
			else
				remarks = "Auto Generated Debit Note - POD-Invoice ID - "
						+ invoiceId;
			itemSer = getColumnDescr(conn, "item_ser", "invoice", "invoice_id",
					invoiceId);
			finEntity = getColumnDescr(conn, "fin_entity", "site", "site_code",
					siteCode);
			acctCode = getColumnDescr(conn, "acct_code__ar", "customer",
					"cust_code", custCode);
			cctrCode = getColumnDescr(conn, "cctr_code__ar", "customer",
					"cust_code", custCode);
			// acctCodetotAR=getColumnDescr(conn,"ACCT_CODE__TOT_AR",
			// "item_acct_detr", "item_ser", itemSer);
			// cctrCodetotAR=getColumnDescr(conn,"CCTR_CODE__TOT_AR",
			// "item_acct_detr", "item_ser", itemSer);
			cctrCodetotAR = cctrCodetotAR == null ? " " : cctrCodetotAR;
			xmlBuffer.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuffer.append("<DocumentRoot>");
			xmlBuffer.append("<description>").append("Datawindow Root").append(
					"</description>");
			xmlBuffer.append("<group0>");
			xmlBuffer.append("<description>").append("Group0 description")
					.append("</description>");
			xmlBuffer.append("<Header0>");
			xmlBuffer.append("<objName><![CDATA[").append("misc_drcr_rcp_dr")
					.append("]]></objName>");
			xmlBuffer.append("<pageContext><![CDATA[").append("1").append(
					"]]></pageContext>");
			xmlBuffer.append("<objContext><![CDATA[").append("1").append(
					"]]></objContext>");
			xmlBuffer.append("<editFlag><![CDATA[").append("A").append(
					"]]></editFlag>");
			xmlBuffer.append("<focusedColumn><![CDATA[").append("").append(
					"]]></focusedColumn>");
			xmlBuffer.append("<action><![CDATA[").append("SAVE").append(
					"]]></action>");
			xmlBuffer.append("<elementName><![CDATA[").append("").append(
					"]]></elementName>");
			xmlBuffer.append("<keyValue><![CDATA[").append("1").append(
					"]]></keyValue>");
			xmlBuffer.append("<taxKeyValue><![CDATA[").append("	").append(
					"]]></taxKeyValue>");
			xmlBuffer.append("<saveLevel><![CDATA[").append("1").append(
					"]]></saveLevel>");
			xmlBuffer.append("<forcedSave><![CDATA[").append(true).append(
					"]]></forcedSave>");
			xmlBuffer.append("<taxInFocus><![CDATA[").append(false).append(
					"]]></taxInFocus>");
			xmlBuffer.append("<description>").append("Header0 members").append(
					"</description>");

			xmlBuffer.append("<Detail1 objContext =\"1\"").append(
					" objName=\"misc_drcr_rcp_dr\" domID=\"1\" dbID=\"\">");
			xmlBuffer
					.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuffer.append("<tran_id/>");
			xmlBuffer.append("<tran_type>").append("<![CDATA[").append("HO")
					.append("]]></tran_type>\r\n");
			xmlBuffer.append("<tran_ser>").append("<![CDATA[").append("MDRCRD")
					.append("]]></tran_ser>\r\n");
			xmlBuffer.append("<tran_date>").append("<![CDATA[").append(
					currAppdate).append("]]></tran_date>\r\n");
			xmlBuffer.append("<eff_date>").append("<![CDATA[").append(
					currAppdate).append("]]></eff_date>\r\n");
			xmlBuffer.append("<due_date>").append("<![CDATA[").append(
					currAppdate).append("]]></due_date>\r\n");
			xmlBuffer.append("<fin_entity>").append("<![CDATA[").append(
					finEntity).append("]]></fin_entity>\r\n");
			xmlBuffer.append("<site_code>").append("<![CDATA[")
					.append(siteCode).append("]]></site_code>\r\n");
			xmlBuffer.append("<cctr_code>").append("<![CDATA[")
					.append(cctrCode).append("]]></cctr_code>\r\n");
			xmlBuffer.append("<sundry_type>").append("<![CDATA[").append("C")
					.append("]]></sundry_type>\r\n");
			xmlBuffer.append("<sundry_code>").append("<![CDATA[").append(
					custCode).append("]]></sundry_code>\r\n");
			xmlBuffer.append("<acct_code>").append("<![CDATA[")
					.append(acctCode).append("]]></acct_code>\r\n");
			xmlBuffer.append("<curr_code>").append(
					"<![CDATA[" + currencyCode + "]]>").append(
					"</curr_code>\r\n");
			xmlBuffer.append("<amount>").append("<![CDATA[")
					.append(detail3AmtT).append("]]></amount>\r\n");
			xmlBuffer.append("<drcr_flag>").append("<![CDATA[").append("D")
					.append("]]></drcr_flag>\r\n");
			xmlBuffer.append("<item_ser>").append("<![CDATA[").append(itemSer)
					.append("]]></item_ser>\r\n");
			xmlBuffer.append("<exch_rate>").append("<![CDATA[")
					.append(exchRate).append("]]></exch_rate>\r\n");
			xmlBuffer.append("<confirmed>").append("<![CDATA[").append("N")
					.append("]]></confirmed>\r\n");
			xmlBuffer.append("<acct_code__tot_ar>").append("<![CDATA[").append(
					acctCodetotAR).append("]]></acct_code__tot_ar>\r\n");
			xmlBuffer.append("<cctr_code__tot_ar>").append("<![CDATA[").append(
					cctrCodetotAR).append("]]></cctr_code__tot_ar>\r\n");

			xmlBuffer.append("<reas_code>").append("<![CDATA[").append("PODDB")
					.append("]]></reas_code>\r\n");
			// xmlBuffer.append("<cust_ref_no>").append("<![CDATA[").append(invoiceId).append("]]></cust_ref_no>\r\n");
			// xmlBuffer.append("<cust_ref_date>").append("<![CDATA[").append(invoiceDate).append("]]></cust_ref_date>\r\n");

			xmlBuffer.append("<remarks>").append("<![CDATA[").append(
					remarks.trim()).append("]]></remarks>\r\n");
			xmlBuffer.append("<chg_user/>\r\n");
			xmlBuffer.append("<chg_term/>\r\n");
			xmlBuffer.append("<chg_date>").append("<![CDATA[").append(
					currAppdate).append("]]></chg_date>\r\n");
			xmlBuffer.append("</Detail1>\r\n");
			System.out.println("Detail 2 xml String ---->>[[ "
					+ detail2xmlString + " ]]");
			xmlBuffer.append(detail2xmlString);

			xmlBuffer.append("</Header0>");
			xmlBuffer.append("</group0>");
			xmlBuffer.append("</DocumentRoot>");

			xmlString = xmlBuffer.toString();

			System.out.println("All XML String --->>[[" + xmlBuffer.toString()
					+ "]]");
			xmlStringAll = xmlBuffer.toString(); // xmlStringAll access in
													// POD Process.

			System.out
					.println("------ debit Note Data saving------------------");
			retString = saveData(siteCode, xmlString,userId, conn);
			System.out
					.println("debit Note Data saved successfully. retString : [["
							+ retString + "]]");
			if (retString.indexOf("Success") > -1) {
				System.out.println("retString.indexOf(Success) > -1)");
				String[] arrayForTranId = retString.split("<TranID>");
				int endIndex = arrayForTranId[1].indexOf("</TranID>");

				dbTranID = arrayForTranId[1].substring(0, endIndex);
				// retString ="";
				System.out.println("@@@@@@3: retString" + retString);
				System.out
						.println("DebitNoteTranID------>>>[" + dbTranID + "]");
				if (dbTranID != null && dbTranID.trim().length() > 0) {

					System.out
							.println("tran_id__drn generated successfully----------");
					debitNoteTranID = dbTranID;
				}

			}

		} catch (Exception e) {
			isError = true;
			retString = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}

		return retString;

	}

	protected String getDetailsXmlString(Connection conn, String invoiceId,
			int lineNoTrace, int lineNo, double rateInv, double qtyInv,
			String lotNo, String lotSl, String itemCode,
			double debitNoteAmtDetail) {
		invoiceId = invoiceId == null ? "" : invoiceId.trim();
		System.out.println("in getDetail2xmlString -->> itemCode: " + itemCode
				+ " LineNo : " + lineNo);
		StringBuffer detail2xml = new StringBuffer();
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String acctCodeUDF1 = "", acctCodeUDF2 = "", sql = "", itemUnit = "";

		try {
			sql = "select UDF_STR1,UDF_STR2 from gencodes where FLD_NAME = ? AND MOD_NAME = ? AND FLD_VALUE = ? ";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, "REAS_CODE");
			pstmt1.setString(2, "W_MISC_DRCR_RCP_DR");
			pstmt1.setString(3, "PODDB");
			rs1 = pstmt1.executeQuery();
			if (rs1.next()) {
				acctCodeUDF1 = rs1.getString(1) == null ? "" : rs1.getString(1);
				acctCodeUDF2 = rs1.getString(2) == null ? "" : rs1.getString(2);
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			System.out.println("acctCodeUDF1 : " + acctCodeUDF1
					+ "  acctCodeUDF2 : " + acctCodeUDF2);

			itemUnit = getColumnDescr(conn, "unit", "item", "item_code",
					itemCode);
			System.out.println("Item Code : " + itemCode + " itemCode Unit : "
					+ itemUnit);
			detail2xml.append("<Detail2 objContext =\"2\"").append(
					" objName=\"misc_drcr_rcp_dr\" domID=\"1\" dbID=\"\">");
			detail2xml
					.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			detail2xml.append("<tran_id/>");
			detail2xml.append("<line_no>").append("<![CDATA[").append(lineNo)
					.append("]]></line_no>");
			detail2xml.append("<line_no__invtrace>").append("<![CDATA[")
					.append(lineNoTrace).append("]]></line_no__invtrace>");
			detail2xml.append("<net_amt>").append("<![CDATA[").append(
					debitNoteAmtDetail).append("]]></net_amt>");
			detail2xml.append("<amount>").append("<![CDATA[").append(
					debitNoteAmtDetail).append("]]></amount>");
			detail2xml.append("<acct_code>").append("<![CDATA[").append(
					acctCodeUDF1).append("]]></acct_code>");
			detail2xml.append("<cctr_code>").append("<![CDATA[").append(
					acctCodeUDF2).append("]]></cctr_code>");
			detail2xml.append("<sundry_type>").append("<![CDATA[").append("C")
					.append("]]></sundry_type>\r\n");

			detail2xml.append("<item_code>").append("<![CDATA[").append(
					itemCode).append("]]></item_code>\r\n");
			detail2xml.append("<rate>").append("<![CDATA[").append(rateInv)
					.append("]]></rate>\r\n");
			detail2xml.append("<quantity>").append("<![CDATA[").append(qtyInv)
					.append("]]></quantity>\r\n");
			detail2xml.append("<lot_no>").append("<![CDATA[").append(lotNo)
					.append("]]></lot_no>\r\n");
			detail2xml.append("<lot_sl>").append("<![CDATA[").append(lotSl)
					.append("]]></lot_sl>\r\n");
			detail2xml.append("<unit>").append("<![CDATA[").append(itemUnit)
					.append("]]></unit>\r\n");

			detail2xml.append("<reas_code>").append("<![CDATA[")
					.append("PODDB").append("]]></reas_code>\r\n");

			detail2xml.append("</Detail2>");
			return detail2xml.toString();

		} catch (Exception e) {
			System.out.println("Exception :getDetail2xmlString method  :==>");
			isError = true;
			e.printStackTrace();
		}

		System.out.println("@@@@@2: xmlString detail2--->>[["
				+ detail2xml.toString() + "]]");
		return detail2xml.toString();
	}

	private int updatePodHeader(Connection conn, String tranId, String loginCode) {
		String sql = "";
		int lineNo = 0, count = 0;
		double dbAmt = 0.0, debitNoteHeaderL = 0;
		PreparedStatement pstmt = null;

		try {

			System.out
					.println("spl_sales_por_det updated successfully but not commit yet------");

			System.out.println("Final debitNote amt Header----->>["
					+ debitNoteHeaderL + "]");

			sql = "update spl_sales_por_hdr set tran_id__drn = ? where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, debitNoteTranID);
			pstmt.setString(2, tranId);
			count = pstmt.executeUpdate();

			sql = "update spl_sales_por_hdr set confirmed = ?,conf_date = ?, emp_code__aprv = ? where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "Y");
			pstmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));
			pstmt.setString(3, loginCode); // loginCode //"E10808"
			pstmt.setString(4, tranId);
			count = pstmt.executeUpdate();
			System.out
					.println("spl_sales_por_hdr(confirmed) updated successfully but not commit yet------");
		}// end try
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		System.out.println("update count@@@----->>[" + count + "]");
		return count;

	}

	private int updateDrCrRcpHdrAmt(String tranId, double actualDebitNote,
			Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		int cnt = 0;
		double drCrAcctAmt = 0, drCrAcctAmtM = 0, hdrDbAmt = 0, finalHdrAmt = 0, exchRate = 0, amountBc = 0;

		try {
			sql = "select amount from drcr_racct where tran_id= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				drCrAcctAmt = rs.getDouble(1);

				if (drCrAcctAmt < 0) {
					drCrAcctAmtM = drCrAcctAmtM + drCrAcctAmt;
				}

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("Final amount drcr_racct------>>["
					+ drCrAcctAmtM + "]");
			sql = "select amount,exch_rate from DRCR_RCP where tran_id= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				// hdrDbAmt=rs.getDouble(1);
				exchRate = rs.getDouble(2);
			}
			System.out.println("Header amount in DRCR_RCP------>>[" + hdrDbAmt
					+ "]");

			finalHdrAmt = hdrDbAmt + drCrAcctAmtM;
			amountBc = actualDebitNote * exchRate;
			System.out.println("Final Header amount in DRCR_RCP------>>["
					+ finalHdrAmt + "]");
			System.out.println("Final Header amount_bc in DRCR_RCP------>>["
					+ amountBc + "]");

			sql = "update DRCR_RCP set amount = ?,amount__bc = ? where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, actualDebitNote);
			pstmt.setDouble(2, amountBc);
			pstmt.setString(3, tranId);
			cnt = pstmt.executeUpdate();

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		} catch (Exception e) {
			isError = true;
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("cnt in updateDrCrRcpHdrAmt---->[" + cnt + "]");
		return cnt;
	}

	private String saveData(String siteCode, String xmlString, String userId,Connection conn)
			throws ITMException {

		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try {
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx
					.lookup("ibase/MasterStatefulEJB/local");

			String[] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString in masterStateful :::: " + xmlString);
			retString = masterStateful.processRequest(authencate, siteCode,
					true, xmlString, true, conn);
			masterStateful = null;

		} catch (ITMException itme) {
			System.out.println("ITMException :PosConfirm :saveData :==>");
			throw itme;
		} catch (Exception e) {
			isError = true;
			System.out.println("Exception :PosConfirm :saveData :==>");
			throw new ITMException(e);
		}
		return retString;

	}

	private String getColumnDescr(Connection conn, String columnName,
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

	protected double getDebitNoteAmount(double rateInv, double qtyInv) {
		System.out
				.println("-------- in getDebitNoteAmount method -------------");
		double debitNoteAmount = 0, aprvRate = 0, dbResale = 0, dbAmt = 0;
		System.out.println("rateInv-->>[" + rateInv + "]");
		System.out.println("qtyInv-->>[" + qtyInv + "]");
		// debitNoteAmount= (podRate - apprvRate) * podQty;
		// dbResale= podRate * podQty;

		debitNoteAmount = rateInv * qtyInv;
		// System.out.println("dbResale-->>["+podQty+"]");
		System.out.println("dbAmt-->>[" + dbAmt + "]");
		/*
		 * if(dbResale > dbAmt){ debitNoteAmount=dbResale - dbAmt; }
		 */
		System.out.println("debitNoteAmount-->>[" + debitNoteAmount + "]");
		return debitNoteAmount;
	}

	private boolean isRegulatePriceNull(Connection conn, String tranId) throws ITMException {
		boolean isNull = false;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String sql = "", itemCode = "", regulatedPrice = "";
		sql = "select item_code from spl_sales_por_det where tran_id = ?";
		// getColumnDescr(conn,"regulated_price","item","item_code",itemCode);
		try {
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, tranId);
			rs1 = pstmt1.executeQuery();

			while (rs1.next()) {
				itemCode = rs1.getString(1) == null ? "" : rs1.getString(1);
				regulatedPrice = getColumnDescr(conn, "regulated_price",
						"item", "item_code", itemCode);
				System.out
						.println("--regulatedPrice in isRegulatePriceNull method -->>"
								+ regulatedPrice);
				if (regulatedPrice == null
						|| regulatedPrice.trim().length() == 0)
					return true;

			}
		} catch (Exception e) {
			System.out.println("Exception in isRegulatePriceNull method ");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return false;
	}

	public String confirmDebitNote(String businessObj, String tranIdFr,
			String xtraParams, String forcedFlag, Connection conn)
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
		try {
			// ConnDriver connDriver = new ConnDriver();
			// //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out
					.println("-------in confirmDebitNote mothod----------------");
			tranIdFr = tranIdFr == null ? "" : tranIdFr.trim();
			System.out.println("Tran id for debit note--->>" + tranIdFr + "]");
			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, businessObj);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			compName = compName == null ? "" : compName.trim();
			System.out.println("serviceCode = [" + serviceCode
					+ "]   compName [" + compName + "]");
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, serviceCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serviceURI = rs.getString("SERVICE_URI");
			}
			serviceURI = serviceURI == null ? "" : serviceURI.trim();
			System.out.println("serviceURI = [" + serviceURI
					+ "]---> compName = [" + compName + "]");
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "component_name"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING,
					ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "xtra_params"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "forced_flag"),
					XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String(forcedFlag);
			// System.out.println("@@@@@@@@@@loginEmpCode:"
			// +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out
					.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);

			retString = (String) call.invoke(aobj);

			System.out
					.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>["
							+ retString + "]");

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
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try {
					conn.rollback();

				} catch (Exception s) {
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}

		}
		return retString;
	}// end confirmCreditNote

	public String confirmConsOrder(String businessObj, String tranIdFr,
			String xtraParams, Connection conn) throws ITMException {

		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out
				.println("confirmSaleOrder(String businessObj, String tranIdFr,String	xtraParams, String forcedFlag, Connection conn) called >>><!@#>");

		try {
			// ConnDriver connDriver = new ConnDriver();
			// //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out
					.println("enter in confirm method [[[[[[[[[[[]]]]]]]]]]] >>>>>");
			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = " SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND	EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, businessObj);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = " + serviceCode + " compName "
					+ compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, serviceCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println("serviceURI = " + serviceURI + " compName = "
					+ compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "component_name"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING,
					ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "xtra_params"),
					XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName(
					"http://NvoServiceurl.org", "forced_flag"),
					XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			// aobj[3] = new String(forcedFlag);
			// System.out.println("@@@@@@@@@@loginEmpCode:"+genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out
					.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String) call.invoke(aobj);
			System.out
					.println("Confirm Complete @@@@@@@@@@@ Return string from NVO	is:==>["
							+ retString + "]");

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
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try {
					conn.rollback();

				} catch (Exception s) {
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}
		}
		return retString;

	}

}
