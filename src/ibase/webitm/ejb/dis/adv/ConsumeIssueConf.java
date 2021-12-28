package ibase.webitm.ejb.dis.adv;

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
import java.util.Date;
import java.util.HashMap;
import ibase.webitm.ejb.fin.InvAcct;
import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.InvHoldGen;
import ibase.webitm.ejb.dis.StockUpdate;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

@Stateless
public class ConsumeIssueConf extends ActionHandlerEJB implements ConsumeIssueConfLocal, ConsumeIssueConfRemote {

	E12GenericUtility genericUtility = new E12GenericUtility();
	String userId = null;
	String termId = null;
	InvAcct invacct = new InvAcct();

	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException {

		String errString = "";
		
		try {
			errString = confirmConIss(tranId, xtraParams, forcedFlag);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in confirm() ==>" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			System.out.println("errString in cofirm() ==>" + errString);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String confirmConIss(String tranId, String xtraParams, String forcedFlag)
			throws RemoteException, ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String confirm = "";
		String errString = "";
		String consIssue = "", status = "", consOrd = "";
		String gpno = "", siteCodeReq = "", orderTypes = "", orderType = "", key = "", inString = "", exciseCodes = "";
		String sitecdExc = "", ledGPostConf = "", gpSer = "";

		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp gpDate = null;
		java.sql.Timestamp today = null;
		FinCommon finCommon = null;
		DistCommon distCommon = null;
		distCommon = new DistCommon();
		Connection conn = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		boolean isLocalConn = false;
		int ll_cnt = 0, cnt = 0, lineNoDet = 0,count=0;
		SimpleDateFormat sdf = null;
		try {
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			sdf = new SimpleDateFormat(new E12GenericUtility().getApplDateFormat());
			today = getCurrtDate();
			finCommon = new FinCommon();
			itmDBAccessEJB = new ITMDBAccessEJB();
			conn = getConnection();
			consIssue = tranId;
			sql = " select confirmed,cons_order from consume_iss  where cons_issue = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				confirm = rs.getString("confirmed");
				consOrd = rs.getString("cons_order");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (confirm != null && "Y".equalsIgnoreCase(confirm)) {
				System.out.println("The Selected transaction is already confirmed");
				errString = itmDBAccessEJB.getErrorString("", "VTMCONF1", "", "", conn);
				return errString;
			} else {
				sql = "select status from consume_ord where cons_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, consOrd);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					status = rs.getString("status");

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (status != null && "C".equalsIgnoreCase(status.trim())) {
					System.out.println("Consumption Order Cancelled. Cannot confirm the consumption issue.");
					errString = itmDBAccessEJB.getErrorString("", "VTCANCEL4", "", "", conn);
					return errString;
				}
				if (status != null && "D".equalsIgnoreCase(status.trim())) {

					System.out.println("Consumption Order Closed. Cannot edit the consumption order.");
					errString = itmDBAccessEJB.getErrorString("", "VTCLOSED4", "", "", conn);
					return errString;

				} else {
					sql = "select (case when LEDG_POST_CONF is null then 'N' else LEDG_POST_CONF end ) as ledgpostconf from transetup where lower(tran_window) = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "w_consume_issue");
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ledGPostConf = rs.getString("ledgpostconf");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (ledGPostConf == null && ledGPostConf.trim().length() < 0) {
						ledGPostConf = "N";
					}
					if (ledGPostConf != null && "Y".equalsIgnoreCase(ledGPostConf.trim())) {

						sql = "update consume_iss set ISSUE_DATE = ? where cons_issue = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, today);
						pstmt.setString(2, consIssue);
						cnt = pstmt.executeUpdate();
						if (cnt < 0) {
							errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
							return errString;
						}

						pstmt.close();
						pstmt = null;

					}

					sql = "select gp_no,site_code__req,cons_order, issue_date, gp_ser, gp_date from consume_iss where cons_issue = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, consIssue);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						gpno = rs.getString("gp_no");
						siteCodeReq = rs.getString("site_code__req");
						consOrd = rs.getString("cons_order");
						tranDate = rs.getTimestamp("issue_date");
						gpSer = rs.getString("gp_ser");
						gpDate = rs.getTimestamp("gp_date");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select order_type from consume_ord where cons_order  = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, consOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						orderType = rs.getString("order_type");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					exciseCodes = distCommon.getDisparams("999999","EXC_TAX_CODE", conn);// gf_getenv_dis('999999','EXC_TAX_CODE')

					String lsexcisecodeArr[] = exciseCodes.split(",");
					for (String excisecode : lsexcisecodeArr) {
						inString = inString + "'" + excisecode + "'" + ",";
					}
					inString = inString.substring(0, inString.length() - 1);

					sql = "select	count(*) from taxtran where	tran_code = 'C-ISS'  and tran_id = '" + consIssue + "' "
							+ "and tax_code in (" + inString + ") ";

					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1, "w_consume_issue");
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ll_cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (cnt < 0) {
						errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
						return errString;
					}

					orderTypes = distCommon.getDisparams("999999", "GP_NO", conn);// gf_getenv_dis('999999', 'GP_NO')

					String ordertypesArr[] = orderTypes.split(",");
					for(String ordertp:ordertypesArr)
					 {
						 if(ordertp.equalsIgnoreCase(orderType))
							 count++;
					 }
					if (count==0) {

						if ((gpno == null || gpno.trim().length() < 0) && ll_cnt > 0) {

							sql = "select key_string from transetup	where lower(tran_window) = 'gpno' ";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								key = rs.getString("key_string");

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (key == null && key.trim().length() == 0) {
								sql = "select key_string from transetup	where tran_window = 'GENERAL'";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									key = rs.getString("key_string");

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}

							sql = "select site_code__exc from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeReq);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								sitecdExc = rs.getString("site_code__exc");

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (sitecdExc != null && sitecdExc.trim().length() > 0) {
								siteCodeReq = sitecdExc;
							}

							String xmlValues = "";
							xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>";
							xmlValues = xmlValues + "<Detail1>";
							xmlValues = xmlValues + "<tran_id/>";
							xmlValues = xmlValues + "<site_code>" + siteCodeReq + "</site_code>";
							xmlValues = xmlValues + "<tran_date>" + tranDate + "</tran_date>";
							xmlValues = xmlValues + "<gp_ser>" + gpSer + "</gp_ser>";
							xmlValues = xmlValues + "<gp_date>" + sdf.format(gpDate) + "</gp_date>";
							xmlValues = xmlValues + "</Detail1></Root>";

							TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
							gpno = tg.generateTranSeqID(siteCodeReq, "tran_id", key, conn);
							System.out.println("GP NO Generated :::::::::" + gpno);

							if (gpno == null && gpno.trim().length() == 0) {
								System.out.println("GP NO is not Generated");
								errString = itmDBAccessEJB.getErrorString("", "GPNOERR", "", "", conn);
								return errString;
							} else {
								gpDate = new java.sql.Timestamp(System.currentTimeMillis());
								sql = "update consume_iss set gp_no = ?, gp_date = ? where cons_issue = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, gpno);
								pstmt.setTimestamp(2, gpDate);
								pstmt.setString(3, consIssue);
								cnt = pstmt.executeUpdate();
								if (cnt < 0) {
									errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
									return errString;
								}

								pstmt.close();
								pstmt = null;

							}

						}
					}

					errString = gbfRetrieveConsIss(consIssue, "C-ISS", xtraParams, conn);

					if (errString != null && errString.trim().length() > 0) {
						return errString;
					}
					else
					{
						System.out.println("@@@@@@@@@@@118:::::::::::confirm record successfully........");
						errString = itmDBAccessEJB.getErrorString("","CONFSUCC","");
						
					}

				}
			}
		} // end of try
		catch (Exception e) {
			System.out.println("Exception ::" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (Exception e1) {
				System.out.println("Exception while rollbacking222.......");
			}
			throw new ITMException(e);
		} finally {
			System.out.println("IN fINALLY ConsumeIssueConf @#......>>[" + isLocalConn + "]");
			System.out.println("IN fINALLY ConsumeIssueConf errString@#......>>[" + errString + "]");
			try {
				if (errString != null && errString.trim().length() > 0) {
					if (errString.indexOf("CONFSUCC") > -1) {
						
							System.out.println("Transaction commited111.............from ConsumeIssueConf");
							conn.commit();
						
					} else {
						System.out.println("CONNECTION Rollbacking......................................");
						conn.rollback();
						System.out.println("CONNECTION Rollback......................................");
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
				if (isLocalConn)
					conn.close();
			} catch (Exception e) {
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	} // end of confirm method

	private String gbfRetrieveConsIss(String consIssue, String tranSer, String xtraParams, Connection conn)
			throws RemoteException, ITMException {
		String consIss[], keyFld = "", tranId = "", ediOption = "", tranType = "", cConsIss = "", unit = "",
				acctCode = "", cctrCode = "", locCode = "", lotNo = "";
		String detconsIssue = "", deptCode = "", itemSer = "", siteCodeReq = "", currCode = "", mtranType = "",avail="",
				hdrconsIssue = "", consOrder = "", itemCode = "",lineNo="";
		String lotSl = "", unitStd = "", qcReqd = "", acctCodeInv = "", cctrCodeInv = "", errString = "";
		double quantityStd = 0, quantity = 0, rate = 0;
		int ll_cnt = 0, cnt = 0, noArt = 0, lineNoOrd = 0;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "", sql1 = "", sqlInner = "";
		String loginEmpCode = "";
		ITMDBAccessEJB itmDBAccessEJB = null;
		FinCommon Fcommon = new FinCommon();
		DistCommon Dcommon = new DistCommon();

		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp gpDate = null;
		java.sql.Timestamp today = null;
		java.sql.Timestamp issueDate = null;

		HashMap consIssHdrMap = null, consIssDetMap = null;

		ArrayList consIssHdrList = new ArrayList();
		ArrayList consIssDetList = new ArrayList();

		SimpleDateFormat sdf = null;
		try {

			sql = "select cons_issue from consume_iss where cons_issue = ? and confirmed = 'N'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, consIssue);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				consIssue = rs.getString("cons_issue");
				System.out.println("cons_issue...gbfRetrieveConsIss:[" + consIssue + "]");
				sql1 = "select cons_issue from consume_iss where cons_issue =? for update nowait";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, consIssue);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					keyFld = rs1.getString("cons_issue");
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTLCKERR","","",conn);
					return errString;
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select cons_issue,  issue_date,  dept_code,   item_ser,site_code__req,available_yn,"
					+ "curr_code,tran_type from consume_iss where cons_issue= ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, consIssue);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				hdrconsIssue = rs.getString("cons_issue");
				issueDate = rs.getTimestamp("issue_date");
				deptCode = rs.getString("dept_code");
				itemSer = rs.getString("item_ser");
				siteCodeReq = rs.getString("site_code__req");
				currCode = rs.getString("curr_code");
				mtranType = rs.getString("tran_type");
				avail = rs.getString("available_yn");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			consIssHdrMap = new HashMap();
			consIssHdrMap.put("cons_issue", hdrconsIssue);
			consIssHdrMap.put("issue_date", issueDate);
			consIssHdrMap.put("dept_code", deptCode);
			consIssHdrMap.put("item_ser", itemSer);
			consIssHdrMap.put("site_code__req", siteCodeReq);
			consIssHdrMap.put("curr_code", currCode);
			consIssHdrMap.put("tran_type", mtranType);
			consIssHdrMap.put("available_yn", avail);

	

			sql = " select cons_issue,line_no,cons_order,line_no__ord,item_code,quantity,unit,rate,"
					+ "acct_code,cctr_code,loc_code,lot_no,lot_sl,quantity__std,unit__std,"
					+ "qc_reqd,acct_code__inv,cctr_code__inv,no_art from consume_iss_det where ( cons_issue = ?  )";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, consIssue);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				detconsIssue = rs.getString("cons_issue");
				lineNo = rs.getString("line_no");
				consOrder = rs.getString("cons_order");
				lineNoOrd = rs.getInt("line_no__ord");
				itemCode = rs.getString("item_code");
				quantity = rs.getDouble("quantity");
				unit = rs.getString("unit");
				rate = rs.getDouble("rate");
				acctCode = rs.getString("acct_code");
				cctrCode = rs.getString("cctr_code");
				locCode = rs.getString("loc_code");
				lotNo = rs.getString("lot_no");
				lotSl = rs.getString("lot_sl");
				quantityStd = rs.getDouble("quantity__std");
				unitStd = rs.getString("unit__std");
				qcReqd = rs.getString("qc_reqd");
				acctCodeInv = rs.getString("acct_code__inv");
				cctrCodeInv = rs.getString("cctr_code__inv");
				noArt = rs.getInt("no_art");
				
				consIssDetMap = new HashMap();
				consIssDetMap.put("cons_issue", detconsIssue);
				consIssDetMap.put("line_no", lineNo);
				consIssDetMap.put("cons_order", consOrder);
				consIssDetMap.put("line_no__ord", lineNoOrd);
				consIssDetMap.put("item_code", itemCode);
				consIssDetMap.put("quantity", quantity);
				consIssDetMap.put("unit", unit);
				consIssDetMap.put("rate", rate);
				consIssDetMap.put("acct_code", acctCode);
				consIssDetMap.put("cctr_code", cctrCode);
				consIssDetMap.put("loc_code", locCode);
				consIssDetMap.put("lot_no", lotNo);
				consIssDetMap.put("lot_sl", lotSl);
				consIssDetMap.put("quantity__std", quantityStd);
				consIssDetMap.put("unit__std", unitStd);
				consIssDetMap.put("qc_reqd", qcReqd);
				consIssDetMap.put("acct_code__inv", acctCodeInv);
				consIssDetMap.put("cctr_code__inv", cctrCodeInv);
				consIssDetMap.put("no_art", noArt);

				consIssDetList.add(consIssDetMap);

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			

			errString = gbfPostConsIss(consIssHdrMap, consIssDetList, tranSer, xtraParams, conn);
			if (errString != null && errString.trim().length() > 0) {
				return errString;
			}

		} // try

		catch (Exception e) {
			System.out.println("Exception :ConsumeIssueConf :ConsumeIssueConf :==>" + e.getMessage());
			errString = genericUtility.createErrorString(e);
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
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;

	}

	private String gbfPostConsIss(HashMap consIssHdrMap, ArrayList consIssDetList, String tranSer, String xtraParams,
			Connection conn) throws RemoteException, ITMException {
		String consIss = "", tranType = "",tranTypeStk="", unit = "", acctCodeOrd = "", cctrCodeOrd = "", locCode = "", lotNo = "",locCodeIss="",
				temp = "", ret = "", invlink = "", invStat = "", sundryType = "",qOrderType="";
		String detconsIssue = "", sundryCode = "", itemSer = "", siteCodeReq = "", currCode = "", hdrconsIssue = "",
				consOrder = "", itemCode = "",tranId="",avail="";
		String lotSl = "", unitStd = "", qcReqd = "", acctCodeInv = "", cctrCodeInv = "", errString = "",
				 costctrAsLocCode = "", invOnline = "",gsRunMode="";
		String packinStr = "", dimension = "", ppCodeMfg = "", acctcodeCR = "", cctrCodeCR = "", acctCodeOh = "",
				cctrCodeOh = "", suppCodeMfg = "",suppCode = "",qSuppCodeMfg = "";
		String packCode = "", siteCodeMfg = "", grade = "", batchNo = "", unitAlt = "", potencyperc = "",lineNo = "",quarLockCode="";
		double quantityStd = 0, quantity = 0, rate = 0, grossRate = 0.0, rateoh = 0.0, conv = 0.0, allocQty = 0.0,actualRate=0.0,stkQty=0.0,totOrder =0.0,totIssue=0.0;
		double oldRate=0,totAmt=0.0;
		int ll_cnt = 0, cnt = 0, noArt = 0,  lineNoOrd = 0;

		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "", sql1 = "", sqlInner = "";
		String loginEmpCode = "",empCodeAprv="";
		ITMDBAccessEJB itmDBAccessEJB = null;
		FinCommon finCommon = new FinCommon();
		DistCommon distCommon = new DistCommon();

		java.sql.Timestamp tranDate = null,qMfgDate =null,qExpDate=null,issueDate=null;
		java.sql.Timestamp gpDate = null,qOrderDate= null;
		java.sql.Timestamp today = null, mfgDate = null, expDate = null, retestDt = null;

		ArrayList acctCodeCRArrLst = new ArrayList();
		StockUpdate	stkUpd ;
		InvHoldGen invHoldGen = new InvHoldGen();

		HashMap  consIssDetMap = null, updConsiss = null,lstrQOrd = null,stkUpdMap= null;
		HashMap  stockQtyMap = new HashMap();
		ArrayList<HashMap<String, String>> stockList = new ArrayList<HashMap<String, String>>();
		boolean rateChanged = false;
		SimpleDateFormat sdf = null;
		try {
			today = getCurrtDate();
			empCodeAprv  = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			/*
			 * temp="confirmed object"; sql ="select :ls_temp into :ls_temp from dual";
			 */
				//header Data
				hdrconsIssue = (String) consIssHdrMap.get("cons_issue");
				issueDate = (Timestamp)consIssHdrMap.get("issue_date");
				sundryCode = (String) consIssHdrMap.get("dept_code");
				itemSer = (String) consIssHdrMap.get("item_ser");
				siteCodeReq = (String) consIssHdrMap.get("site_code__req");
				currCode = (String) consIssHdrMap.get("curr_code");
				tranType = (String) consIssHdrMap.get("tran_type");
				avail=(String) consIssHdrMap.get("available_yn");
				/* taxAmt =Double.parseDouble(detListMap.get("tax_amt").toString()); */
			

			for (int ctr = 0; ctr < consIssDetList.size(); ctr++) {
				consIssDetMap = new HashMap();
				consIssDetMap = (HashMap) consIssDetList.get(ctr);

				detconsIssue = (String) consIssDetMap.get("cons_issue");
				lineNo =(String) (consIssDetMap.get("line_no"));
				consOrder = (String) consIssDetMap.get("cons_order");
				lineNoOrd = Integer.parseInt(consIssDetMap.get("line_no__ord").toString());
				itemCode = (String) consIssDetMap.get("item_code");
				quantity = Double.parseDouble(consIssDetMap.get("quantity").toString());
				unit = (String) consIssDetMap.get("unit");
				rate = Double.parseDouble(consIssDetMap.get("rate").toString());
				oldRate = Double.parseDouble(consIssDetMap.get("rate").toString());
				acctCodeOrd = (String) consIssDetMap.get("acct_code");
				cctrCodeOrd = (String) consIssDetMap.get("cctr_code");
				locCode = (String) consIssDetMap.get("loc_code");
				lotNo = (String) consIssDetMap.get("lot_no");
				lotSl = (String) consIssDetMap.get("lot_sl");
				quantityStd = Double.parseDouble(consIssDetMap.get("quantity__std").toString());
				unitStd = (String) consIssDetMap.get("unit__std");
				qcReqd = (String) consIssDetMap.get("qc_reqd");
				acctCodeInv = (String) consIssDetMap.get("acct_code__inv");
				cctrCodeInv = (String) consIssDetMap.get("cctr_code__inv");
				noArt = Integer.parseInt(consIssDetMap.get("no_art").toString());
				
				
				
				// pb Code is not migrated
				/*
				 * ls_costctr_as_loccode = gf_getenv_dis('999999','CCENTER_AS_LOCATION') if
				 * ls_costctr_as_loccode <> "NULLFOUND" and ls_costctr_as_loccode = 'Y' THEN if
				 * ls_trantype = 'I' then ls_sitecode =
				 * lds_hdr.getitemstring(1,"site_code__req") for ll_ctr = 1 to ll_rows
				 * ls_cons_iss = lds_det.getitemstring(ll_ctr,"cons_issue") ll_cons_line =
				 * lds_det.getitemnumber(ll_ctr,"line_no") ls_co =
				 * lds_det.getitemstring(ll_ctr,"cons_order") ll_co_line =
				 * lds_det.getitemnumber(ll_ctr,"line_no__ord") ls_itemcode =
				 * lds_det.getitemstring(ll_ctr,"item_code") ls_loccodeiss =
				 * lds_det.getitemstring(ll_ctr,"loc_code") ls_lotno =
				 * lds_det.getitemstring(ll_ctr,"lot_no") ls_lotsl =
				 * lds_det.getitemstring(ll_ctr,"lot_sl") lc_quantity =
				 * lds_det.getitemnumber(ll_ctr,"quantity") ls_acct_ord =
				 * lds_det.getitemstring(ll_ctr,"acct_code") ls_cctr_ord =
				 * lds_det.getitemstring(ll_ctr,"cctr_code") ls_acct_iss =
				 * lds_det.getitemstring(ll_ctr,"acct_code__inv") ls_cctr_iss =
				 * lds_det.getitemstring(ll_ctr,"cctr_code__inv") if isnull(ls_acct_iss) or
				 * len(trim(ls_acct_iss)) = 0 then //prince 30-03-05 convert in case when //
				 * select nvl(acct_code__inv,' '), nvl(cctr_code__inv,' ') // into :ls_acct_iss,
				 * :ls_cctr_iss // from stock // where item_code = :ls_itemcode and // site_code
				 * = :ls_sitecode and // loc_code = :ls_loccodeiss and // lot_no = :ls_lotno and
				 * // lot_sl = :ls_lotsl ; select  when acct_code__inv is null then ' '
				 * else acct_code__inv end), (case when cctr_code__inv is null then ' ' else
				 * cctr_code__inv end) into :ls_acct_iss, :ls_cctr_iss from stock where
				 * item_code = :ls_itemcode and site_code = :ls_sitecode and loc_code =
				 * :ls_loccodeiss and lot_no = :ls_lotno and lot_sl = :ls_lotsl ; end if
				 * 
				 * //// Added on 16/04/2k5 by Ruchira if ( ( isnull(ls_acct_iss) or
				 * trim(ls_acct_iss) = '' ) OR & ( isnull(ls_cctr_iss) or trim(ls_cctr_iss) = ''
				 * ) ) then ls_ret =
				 * nvo_acct_code_inv.gbf_acct_detr_ttype(ls_itemcode,ls_item_ser,'IN',
				 * ls_trantype) if left(ls_ret,5) = 'DS000' then ls_acct_iss = ' ' ls_cctr_iss =
				 * ' ' else ls_acct_iss = f_get_token(ls_ret,'~t') ls_cctr_iss = ls_ret end if
				 * end if //// End of Added on 16/04/2k5 by Ruchira
				 * 
				 * if trim(ls_cctr_ord) <> trim(ls_cctr_iss) then // create a stock transfer
				 * entry
				 * 
				 * if isnull(ls_acct_ord) or len(trim(ls_acct_ord)) = 0 then ls_invlink =
				 * gf_getfinparm('999999', 'INV_ACCT_CISS') // if ls_invlink = 'NULLFOUND' then
				 * // ls_errcode = 'VTFINPARM~tVariabe INV_ACCT_CISS not defined under Financial
				 * Variables' // goto errfound // end if // // ls_invlink =
				 * left(trim(ls_invlink), 1) // if pos('Y_N', ls_inv_online) = 0 then //
				 * ls_errcode = 'VTFINPARM1~tVariabe INV_ACCT_CISS value should be Y or N under
				 * Financial Variables' // goto errfound // end if // ls_invlink =
				 * trim(ls_invlink)
				 * 
				 * if ls_inv_online = 'Y' then ls_errcode = "INVACCT~tInventory A/c not defined"
				 * goto errfound end if end if
				 * 
				 * lds_transfer.reset() ll_newrow = lds_transfer.insertrow(0) //prince 30-03-05
				 * convert in case when Select (case when rate is null then 0 else rate end),
				 * (case when gross_rate is null then 0 else gross_rate end),pack_instr,
				 * dimension,supp_code__mfg,acct_code__inv,
				 * cctr_code__inv,rate__oh,acct_code__oh,cctr_code__oh,
				 * pack_code,site_code__mfg, grade, batch_no, unit__alt, potency_perc, mfg_date,
				 * exp_date, retest_date, conv__qty_stduom, alloc_qty into :lc_rate,
				 * :lc_grossrate,:ls_packinstr,:ls_dimension,:ls_suppcodemfg,:s_update.
				 * acctcodecr, :s_update.cctrcodecr,:ld_rateoh,:ls_acctcodeoh,:ls_cctrcodeoh,
				 * :ls_packcode,:ls_sitecodemfg, :ls_grade, :ls_batchno, :ls_unitalt,
				 * :lc_potencyperc,:ldt_mfgdate, :ldt_expdate, :ldt_retestdt, :lc_conv,
				 * :lc_allocqty from stock where item_code = :ls_itemcode and site_code =
				 * :ls_sitecode and loc_code = :ls_loccodeiss and lot_no = :ls_lotno and lot_sl
				 * = :ls_lotsl ; if get_sqlcode() < 0 then ls_errcode = "DS000" +
				 * string(sqlca.sqldbcode) + "~t" + sqlca.sqlerrtext goto errfound elseif
				 * get_sqlcode() = 100 then ls_errcode = "~tInvalid Stock, not exist" goto
				 * errfound end if // rahul changed on 18-07-03 for stock allocation trace //
				 * update stock set alloc_qty = alloc_qty - :lc_allocqty // where item_code =
				 * :ls_itemcode and // site_code = :ls_sitecode and // loc_code = :ls_loccodeiss
				 * and // lot_no = :ls_lotno and // lot_sl = :ls_lotsl ; // if get_sqlcode() < 0
				 * then // ls_errcode = "DS000" + string(sqlca.sqldbcode) + "~t" +
				 * sqlca.sqlerrtext // goto errfound // end if ls_temp='inv allocate start
				 * '+string(lc_allocqty) select :ls_temp into :ls_temp from dual;
				 * 
				 * 
				 * if isnull(lc_allocqty) then lc_allocqty = 0.000 lstr_allocate.tran_date =
				 * datetime(today()) lstr_allocate.ref_ser = as_transer lstr_allocate.ref_id =
				 * ls_cons_iss lstr_allocate.ref_line = string(ll_cons_line)
				 * lstr_allocate.site_code = ls_sitecode lstr_allocate.item_code = ls_itemcode
				 * lstr_allocate.loc_code = ls_loccodeiss lstr_allocate.lot_no = ls_lotno
				 * lstr_allocate.lot_sl = ls_lotsl lstr_allocate.alloc_qty = -1 * lc_allocqty
				 * lstr_allocate.chg_user = userid lstr_allocate.chg_term = termid
				 * lstr_allocate.chg_win = "W_CONSUME_ISSUE" ls_errcode =
				 * lnvo_allocate.gbf_upd_alloc_trace(lstr_allocate)
				 * 
				 * ls_temp='inv allocate 1 error code '+ls_errcode select :ls_temp into :ls_temp
				 * from dual;
				 * 
				 * if (not isnull(ls_errcode)) and (len(trim(ls_errcode)) > 0) then ls_temp='inv
				 * allocate 1 exit got error code '+ls_errcode select :ls_temp into :ls_temp
				 * from dual; exit end if ls_temp='transfer set quantity '+string(lc_quantity)
				 * select :ls_temp into :ls_temp from dual;
				 * 
				 * 
				 * //18-07-03 lds_transfer.setitem(ll_newrow,"ref_id",ls_cons_iss)
				 * lds_transfer.setitem(ll_newrow,"ref_ser",'XFRX')
				 * lds_transfer.setitem(ll_newrow,"item_code",ls_itemcode)
				 * lds_transfer.setitem(ll_newrow,"site_code__fr",ls_sitecode)
				 * lds_transfer.setitem(ll_newrow,"site_code__to",ls_sitecode)
				 * lds_transfer.setitem(ll_newrow,"tran_date",lds_hdr.getitemdatetime(1,
				 * "issue_date")) lds_transfer.setitem(ll_newrow,"reas_code","XFRX")
				 * lds_transfer.setitem(ll_newrow,"remarks","Auto transfer C-ISS")
				 * lds_transfer.setitem(ll_newrow,"ref_ser__for",'XFRX')
				 * lds_transfer.setitem(ll_newrow,"ref_id__for",ls_cons_iss)
				 * lds_transfer.setitem(ll_newrow,"loc_code__fr",ls_loccodeiss)
				 * lds_transfer.setitem(ll_newrow,"lot_no__fr",ls_lotno)
				 * lds_transfer.setitem(ll_newrow,"lot_no__to",ls_lotno)
				 * lds_transfer.setitem(ll_newrow,"lot_sl__fr",ls_lotsl)
				 * lds_transfer.setitem(ll_newrow,"lot_sl__to",ls_lotsl)
				 * 
				 * // Ashok on 30/11/02 The loc needs to changes so that the cctr to which //
				 * the material is issued is inserted in stock table // This will be reqd. while
				 * CITN // lds_transfer.setitem(ll_newrow,"loc_code__to",ls_loccodeiss) // //
				 * check the variable if required set cost center code as loc. code
				 * 
				 * if ls_costctr_as_loccode <> "NULLFOUND" and ls_costctr_as_loccode = 'Y' THEN
				 * lds_transfer.setitem(ll_newrow,"loc_code__to",ls_cctr_ord) else
				 * lds_transfer.setitem(ll_newrow,"loc_code__to",ls_loccodeiss) end if //
				 * 30/11/02
				 * lds_transfer.setitem(ll_newrow,"unit",lds_det.getitemstring(ll_ctr,"unit"))
				 * lds_transfer.setitem(ll_newrow,"quantity",lc_quantity )
				 * lds_transfer.setitem(ll_newrow,"mfg_date",ldt_mfgdate)
				 * lds_transfer.setitem(ll_newrow,"site_code__mfg",ls_sitecodemfg)
				 * lds_transfer.setitem(ll_newrow,"potency_perc",lc_potencyperc)
				 * lds_transfer.setitem(ll_newrow,"pack_code",ls_packcode)
				 * lds_transfer.setitem(ll_newrow,"acct_code__dr",ls_acct_iss)
				 * lds_transfer.setitem(ll_newrow,"cctr_code__dr",ls_cctr_ord)
				 * lds_transfer.setitem(ll_newrow,"acct_code__cr",ls_acct_iss)
				 * lds_transfer.setitem(ll_newrow,"cctr_code__cr",ls_cctr_iss)
				 * 
				 * ls_errcode =
				 * lnvo_trf.gbf_transfer_stock(lds_transfer.describe("datawindow.data"))
				 * ls_temp='describe data transfer
				 * data'+string(lds_transfer.describe("datawindow.data")) select :ls_temp into
				 * :ls_temp from dual; if not isnull(ls_errcode) and len(trim(ls_errcode)) > 0
				 * then goto errfound end if // Ashok alloc_qty needs to be increased from loc
				 * where stock is transferred // it will be reduced from stock update function
				 * // rahul on 18-07-03 for stock allocation trace // update stock set alloc_qty
				 * = alloc_qty + :lc_allocqty // where item_code = :ls_itemcode and // site_code
				 * = :ls_sitecode and // loc_code = :ls_cctr_ord and // lot_no = :ls_lotno and
				 * // lot_sl = :ls_lotsl ; // if get_sqlcode() < 0 then // ls_errcode = "DS000"
				 * + string(sqlca.sqldbcode) + "~t" + sqlca.sqlerrtext // goto errfound // end
				 * if if isnull(lc_allocqty) then lc_allocqty = 0.000 lstr_allocate.tran_date =
				 * datetime(today()) lstr_allocate.ref_ser = as_transer lstr_allocate.ref_id =
				 * ls_cons_iss lstr_allocate.ref_line = string(ll_cons_line)
				 * lstr_allocate.site_code = ls_sitecode lstr_allocate.item_code = ls_itemcode
				 * lstr_allocate.loc_code = ls_loccodeiss lstr_allocate.lot_no = ls_lotno
				 * lstr_allocate.lot_sl = ls_lotsl lstr_allocate.alloc_qty = lc_allocqty
				 * lstr_allocate.chg_user = userid lstr_allocate.chg_term = termid
				 * lstr_allocate.chg_win = "W_CONSUME_ISSUE"
				 * 
				 * ls_temp='inv allocate before code '+string(lstr_allocate.alloc_qty) select
				 * :ls_temp into :ls_temp from dual;
				 * 
				 * ls_errcode = lnvo_allocate.gbf_upd_alloc_trace(lstr_allocate)
				 * 
				 * ls_temp='inv allocate 2 error code '+ls_errcode select :ls_temp into :ls_temp
				 * from dual;
				 * 
				 * 
				 * if (not isnull(ls_errcode)) and (len(trim(ls_errcode)) > 0) then ls_temp='inv
				 * allocate 2 got exit error code '+ls_errcode select :ls_temp into :ls_temp
				 * from dual; exit end if
				 * 
				 * //18-07-03 // if ls_invlink = 'Y' then // inventory accounting posting
				 * ls_errcode =
				 * lnvo_fin.gbf_stk_transfer_post(lds_transfer.describe("datawindow.data"))
				 * 
				 * ls_temp='stock transfer describe
				 * '+string(lds_transfer.describe("datawindow.data")) select :ls_temp into
				 * :ls_temp from dual;
				 * 
				 * if not isnull(ls_errcode) and len(trim(ls_errcode)) > 0 then goto errfound
				 * end if // end if // update consumption issue detail with the new // Ashok on
				 * 30/11 the loc_code should be updated to the cost center to which the //
				 * material is issued otherwise the stock will be removed twice from the cctr
				 * issuing the // material update consume_iss_det set cctr_code__inv =
				 * :ls_cctr_ord,loc_code = :ls_cctr_ord where cons_issue = :ls_cons_iss and
				 * line_no = :ll_cons_line ; if get_sqlcode() <> 0 then errcode = "DS000" +
				 * string(sqlca.sqldbcode) +
				 * "~t Error updating issue detail (gbf_post_cons_iss()) " + sqlca.sqlerrtext
				 * goto errfound end if end if
				 * 
				 * next ////////////////////////////////////////// // retrieve the data again
				 * because the inventory A/c , cctr // might be changed now lds_hdr.reset()
				 * lds_det.reset() lds_hdr.retrieve(cons_iss) ll_rows =
				 * lds_det.retrieve(cons_iss) end if end if
				 */
				///////// 18/03/02 transfer of stock ////////////////////////

				rateChanged = false;
				tranId=detconsIssue;
				if ("R".equalsIgnoreCase(tranType.trim())) {
					tranSer = "C-IRTN";
				} else {
					tranSer = "C-ISS";
				}
				if ("R".equalsIgnoreCase(tranType.trim())) {
					tranTypeStk = tranType;
				} else {
					tranTypeStk = "ID";
				}
				sql = "select inv_stat from location where loc_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, locCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					invStat = rs.getString("inv_stat");

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (lotSl == null || lotSl.trim().length() == 0) {
					lotSl = "  ";
				}
				if (lotNo == null || lotNo.trim().length() == 0) {
					lotNo = "  ";
				}
				sundryType = "D";
				if ("R".equalsIgnoreCase(tranType.trim())) 
				{
					sql = "select a.loc_code  from consume_iss_det a, consume_iss b  where a.cons_issue = b.cons_issue "
							+ " and b.cons_order = ?   and a.line_no__ord = ?   and b.tran_type = ? "
							+ " and b.confirmed = ?   and a.lot_no = ?  and a.lot_sl = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, consOrder);
					pstmt.setInt(2, lineNoOrd);
					pstmt.setString(3, "I");
					pstmt.setString(4, "Y");
					pstmt.setString(5, lotNo);
					pstmt.setString(5, lotSl);

					rs = pstmt.executeQuery();
					if (rs.next()) {
						locCodeIss = rs.getString("loc_code");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				else
				{
					locCodeIss=locCode;
				}
				sql = "Select (case when rate is null then 0 else rate end) as rate,(case when gross_rate is null then 0 else gross_rate end) as gross_rate ,"
						+ "pack_instr, dimension,supp_code__mfg,acct_code__inv, "
						+ "cctr_code__inv,rate__oh,acct_code__oh,cctr_code__oh,	pack_code,site_code__mfg, grade,mfg_date,exp_date,retest_date,"
						+ "conv__qty_stduom,unit__alt,actual_rate from stock where item_code = ? and site_code = ? and loc_code  =? "
						+ "and lot_no = ? and lot_sl    = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCodeReq);
				pstmt.setString(3,locCodeIss);
				pstmt.setString(4,lotNo);
				pstmt.setString(5,lotSl);

				rs = pstmt.executeQuery();
				if (rs.next()) {
					 rate= rs.getDouble("rate");
					 grossRate=rs.getDouble("gross_rate");
					 packinStr=rs.getString("pack_instr");
					 dimension=rs.getString("dimension");
					 suppCodeMfg=rs.getString("supp_code__mfg");
					 acctcodeCR=rs.getString("acct_code__inv");
					 cctrCodeCR=rs.getString("cctr_code__inv");
						rateoh=rs.getDouble("rate__oh");
						acctCodeOh=rs.getString("acct_code__oh");
						cctrCodeOh=rs.getString("cctr_code__oh");
						packCode=rs.getString("pack_code");
						siteCodeMfg=rs.getString("site_code__mfg");
						grade =rs.getString("grade");
						mfgDate=rs.getTimestamp("mfg_date");
						expDate =rs.getTimestamp("exp_date");
						retestDt =rs.getTimestamp("retest_date");
						conv =rs.getDouble("conv__qty_stduom");
						unitAlt =rs.getString("unit__alt");
						actualRate=rs.getDouble("actual_rate");

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if((acctcodeCR== null ||acctcodeCR.trim().length()==0) ||(cctrCodeCR== null ||cctrCodeCR.trim().length()==0 ) )	
				{
					ret=finCommon.getAcctDetrTtype(itemCode, itemSer, "IN", tranType,conn);
					System.out.println("retResult :"+ret);
					if (ret.substring(ret.length()-5).equals("DS000")) 
					{
						acctcodeCR = " ";
						cctrCodeCR = " ";
					}
					else
					{
						acctCodeCRArrLst = genericUtility.getTokenList(ret, ",");
						System.out.println("acctCodeCRArrLst.size :"+acctCodeCRArrLst.size());
						System.out.println("acctCodeCRArrLst.get(1) :"+(String)acctCodeCRArrLst.get(1));
						acctcodeCR = (String)acctCodeCRArrLst.get(0); 
						System.out.println("acctCodeCR :"+acctcodeCR);
						cctrCodeCR = (String)acctCodeCRArrLst.get(1);
						System.out.println("cctrCodeCR :"+cctrCodeCR);
					}
				}
				if(acctcodeCR== null ||acctcodeCR.trim().length()==0)
				{
				
					invOnline = finCommon.getFinparams("999999","INV_ACCT_CISS",conn);
					if("NULLFOUND".equalsIgnoreCase(invOnline))
					{
						errString = itmDBAccessEJB.getErrorString("","VTFINPARM","","",conn);
						return errString;
					}
					invOnline = invOnline.trim();
					
					if("Y".equalsIgnoreCase(invOnline))
					{
						errString = itmDBAccessEJB.getErrorString("","INVACCT","","",conn);
						return errString;
					}
					
				}
				acctCodeInv=acctcodeCR;
				cctrCodeInv=cctrCodeCR;
				acctcodeCR = acctCodeOrd;
				cctrCodeCR	 = cctrCodeOrd;
				
				if ("R".equalsIgnoreCase(tranType.trim())) 
				{		
					gsRunMode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "gs_run_mode");
					if ("R".equalsIgnoreCase(qcReqd.trim()) && "I".equalsIgnoreCase(gsRunMode))
					{	
						lstrQOrd = new HashMap();
						
						qOrderDate = getCurrtDate();
						tranSer="C-IRTN";
						qOrderType= "I";
						tranId=detconsIssue;
						sql = "	select supp_code,supp_code__mfg, mfg_date,exp_date from item_lot_info "
								+ "where item_code = ? and lot_no = ?"; 
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, lotNo);
						

						rs = pstmt.executeQuery();
						if (rs.next()) {
							suppCode = rs.getString("supp_code");
							qSuppCodeMfg = rs.getString("supp_code__mfg");
							qMfgDate = rs.getTimestamp("mfg_date");
							qExpDate = rs.getTimestamp("exp_date");
							
							sql1 = "select c.exp_date ,	c.mfg_date, c.supp_code__mfg from consume_iss_det a, consume_iss b, stock c	"
									+ "where a.cons_issue = b.cons_issue and c.item_code = ? and c.item_code = a.item_code "
									+ "and c.lot_no = ?	and c.site_code = ?	and a.cons_order =  ? and a.line_no__ord =?	"
									+ "and b.confirmed = 'Y' 	and b.tran_type = 'I' 	and c.exp_date is not null";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, lotNo);
							pstmt1.setString(3, siteCodeReq);
							pstmt1.setString(4, consOrder);
							pstmt1.setInt(5, lineNoOrd);
							rs1 = pstmt1.executeQuery();
							if (rs1.next()) {
								qExpDate = rs1.getTimestamp("exp_date");
								qMfgDate = rs1.getTimestamp("mfg_date");
								suppCodeMfg = rs1.getString("supp_code__mfg");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						String expdate = (qExpDate == null) ? null : sdf.format(qExpDate);
						String mfgdate = (qMfgDate == null) ? null : sdf.format(qMfgDate);
						
						lstrQOrd.put("supp_code", suppCode);	
						lstrQOrd.put("supp_code__mfg", qSuppCodeMfg);	
						lstrQOrd.put("qorder_type", qOrderType);	
						lstrQOrd.put("qorder_date", qOrderDate);	
						lstrQOrd.put("site_code", siteCodeReq);	
						lstrQOrd.put("item_code", itemCode);	
						lstrQOrd.put("quantity", quantity);
						lstrQOrd.put("lot_no", lotNo);
						lstrQOrd.put("lot_sl", lotSl);
						lstrQOrd.put("loc_code",locCode);
						lstrQOrd.put("tran_id", tranId);
						lstrQOrd.put("batch_no",lotNo);
						lstrQOrd.put("qc_create_type", "A");
						lineNo= "   "+lineNo;
						lineNo = lineNo.substring(lineNo.length()-3, lineNo.length());
						lstrQOrd.put("line_no", lineNo);
						lstrQOrd.put("unit", unit);
						lstrQOrd.put("qc_create_type", "A");
						lstrQOrd.put("transer", tranSer);
						lstrQOrd.put("expiry_date", qExpDate);
						lstrQOrd.put("mfg_date", qMfgDate);
						lstrQOrd.put("route_code", null);
						
						errString = createQc(lstrQOrd, xtraParams, conn) ;
						if (errString != null && errString.trim().length() > 0)
						{
							return errString;
						}
						//ls_qcord_no[upperbound(ls_qcord_no) + 1] =lstr_qord.qcorder_no ------PB code not migrated
					}
				}
				if ("I".equalsIgnoreCase(tranType.trim())) 
				{
					sql = "select (case when a.quantity is null then 0 else a.quantity end)as quantity  from stock a, invstat b  where a.inv_stat = b.inv_stat and"
						+ "	a.site_code = ?  and a.item_code =? and a.loc_code = ? and a.lot_no = ? and	a.lot_sl = ? and b.available = ? "	; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeReq);
					pstmt.setString(2, itemCode);
					pstmt.setString(3, locCode);
					pstmt.setString(4, lotNo);
					pstmt.setString(5, lotSl);
					pstmt.setString(6, avail);
					
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stkQty = checkDoubleNull(rs.getString("quantity"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if (stkQty < quantity )
					{
						errString = itmDBAccessEJB.getErrorString("", "VTSTOCK1", "", "", conn);
						return errString;
					}
				}
				stkUpdMap = new HashMap(); 
				stkUpdMap.put("gross_rate", Double.toString(grossRate));
				stkUpdMap.put("qty_stduom", Double.toString(quantityStd));
				stkUpdMap.put("item_code", itemCode);
				stkUpdMap.put("no_art", Double.toString(noArt));
				stkUpdMap.put("site_code", siteCodeReq);
				stkUpdMap.put("loc_code", locCode);
				stkUpdMap.put("lot_no", lotNo);
				stkUpdMap.put("lot_sl", lotSl);
				stkUpdMap.put("unit", unit);
				stkUpdMap.put("unit__alt", unitAlt);
				stkUpdMap.put("tran_type", tranTypeStk);
				tranDate=getCurrtDate();
				stkUpdMap.put("tran_date", tranDate);
				stkUpdMap.put("tran_ser", tranSer);
				stkUpdMap.put("tran_id", tranId);
				stkUpdMap.put("acct_code__dr", acctCodeOrd);
				stkUpdMap.put("cctr_code__dr", cctrCodeOrd);
				stkUpdMap.put("acct_code__cr", acctcodeCR);
				stkUpdMap.put("cctr_code__cr", cctrCodeCR);
				stkUpdMap.put("acct_code_inv", acctcodeCR);
				stkUpdMap.put("cctr_code_inv", cctrCodeCR);
				stkUpdMap.put("line_no", lineNo);
				stkUpdMap.put("rate", Double.toString(rate));
				stkUpdMap.put("actual_rate", Double.toString(actualRate));
				stkUpdMap.put("site_code__mfg", siteCodeMfg);
				stkUpdMap.put("supp_code__mfg", suppCodeMfg);
				stkUpdMap.put("pack_code", packCode);
				stkUpdMap.put("mfg_date", mfgDate);
				stkUpdMap.put("exp_date", expDate);
				stkUpdMap.put("inv_stat", invStat);
				stkUpdMap.put("retest_date", retestDt);
				stkUpdMap.put("grade", grade);
				stkUpdMap.put("conv__qty_stduom", Double.toString(conv));
				stkUpdMap.put("batch_no", lotNo);
				 
				stkUpd = new StockUpdate();
				errString = stkUpd.updateStock(stkUpdMap, xtraParams, conn);
				if (errString == null || errString.trim().length() == 0)
				{
					if("Y".equalsIgnoreCase(qcReqd))
					{
						quarLockCode=distCommon.getDisparams("999999", "QUARNTINE_LOCKCODE", conn);
						System.out.println("quarLockCode:::::::"+quarLockCode);
						if("NULLFOUND".equalsIgnoreCase(quarLockCode))
						{
							quarLockCode="  ";
						}
						if (quarLockCode != null && quarLockCode.trim().length() > 0)
						{
							sql = "select count(*) from consume_iss_det  where cons_issue = ? and line_no = ?  "	; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, detconsIssue);
								pstmt.setString(2, lineNo);
								
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									sql = "select b.site_code__req,a.item_code,a.loc_code,a.lot_no,a.lot_sl from  consume_iss b,  consume_iss_det a"
											+"where a.cons_issue = b.cons_issue and b.cons_order = ? "
											+ "and a.line_no__ord = ? and b.cons_issue = ?"	; 
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, consOrder);
									pstmt.setInt(2, lineNoOrd);
									pstmt.setString(3, detconsIssue);
									
									
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										siteCodeReq = rs.getString("site_code__req");
										itemCode = rs.getString("item_code");
										locCode = rs.getString("loc_code");
										lotNo = rs.getString("lot_no");
										lotSl = rs.getString("lot_sl");
										
										
										stockQtyMap.put( "site_code", siteCodeReq );
										stockQtyMap.put( "item_code", itemCode );
										stockQtyMap.put( "loc_code", locCode );
										stockQtyMap.put( "lot_no", lotNo );
										stockQtyMap.put( "lot_sl", lotSl );
										stockList.add( stockQtyMap );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								errString = invHoldGen.generateHoldTrans(quarLockCode, detconsIssue, "C-IRTN", siteCodeReq, stockList, xtraParams, conn);
						}
						
					}
				}
				if ("I".equalsIgnoreCase(tranType.trim())) 
				{
					sql = "update consume_ord_det set issue_qty = (case when issue_qty is null then 0 else issue_qty end) + ? where cons_order = ? and line_no  = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, quantity);
					pstmt.setString(2, consOrder);
					pstmt.setInt(3, lineNoOrd);
					cnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
				}
				else 
				{
					sql = "update consume_ord_det set issue_qty = (case when issue_qty is null then 0 else issue_qty end) - ? where cons_order = ? and line_no  = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, quantity);
					pstmt.setString(2, consOrder);
					pstmt.setInt(3, lineNoOrd);
					cnt = pstmt.executeUpdate();
					
					pstmt.close();
					pstmt = null;
				}
				if (cnt < 0) {
					errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
					return errString;
				}
				if ("I".equalsIgnoreCase(tranType.trim())) 
				{
					sql = "select sum(case when quantity is null then 0 else quantity end) as quantity,SUM(case when issue_qty is null then 0 else issue_qty end) as issue_qty " + 
							" from consume_ord_det where cons_order = ? and line_no = ?	  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, consOrder);
					pstmt.setInt(2, lineNoOrd);
					
					
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						totOrder = rs.getDouble("quantity");
						totIssue = rs.getDouble("issue_qty");
		
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(totOrder > 0 && totOrder == totIssue )
					{
						sql = "update consume_ord_det set status = 'D',status_date = ? where cons_order  =? and line_no = ?	 ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, tranDate);
						pstmt.setString(2, consOrder);
						pstmt.setInt(3, lineNoOrd);
						cnt = pstmt.executeUpdate();
						if (cnt < 0) {
							errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
							return errString;
						}
						pstmt.close();
						pstmt = null;
					}
				}
				if (oldRate != rate)
				{
					rateChanged=true;
				/*	gs_taxstring = gf_batch_tax_data('C-ISS',ls_cons_iss,ldw_dummy,right('     ' + string(ll_cons_line),3), 1)
				lc_taxamt = gf_calc_tax_ds(lds_detedit,lds_tax,'C-ISS',ls_cons_iss,ldt_taxdate,"rate","quantity__std",0,ls_curr_code,'2')
				
					if lc_taxamt = -999999999 then 
							ls_errcode = "DSTAXERR"
							goto errfound
						end if	
					lc_qty    = lds_det.getitemnumber(ll_ctr,"quantity")    
							lc_totamt = (lc_qty * lc_rate)
										 
							Update consume_iss_det 
							set 	 rate 		= :lc_rate, 
									 net_amt 	= :lc_totamt + :lc_taxamt,
									 amount 		= :lc_totamt, 
									 tax_amt 	= :lc_taxamt
							where  cons_issue = :ls_cons_iss 
							and 	 line_no 	= :ll_cons_line;
							if get_sqlcode() <> 0 then
								ls_errcode = 'DS000' + trim(string(sqlca.sqldbcode))+ '~t'+ sqlca.sqlerrtext
								goto errfound
							end if*/
					//pb code not migrated
					
					/*totAmt=quantity * rate;
					
					sql = "Update consume_iss_det set rate= ?, net_amt 	= ?, amount =?, tax_amt = ? where cons_issue =?	and line_no=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, rate);
					pstmt.setString(2, consOrder);
					pstmt.setInt(3, lineNoOrd);
					cnt = pstmt.executeUpdate();
					if (cnt < 0) {
						errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
						return errString;
					}
					pstmt.close();
					pstmt = null;*/
				}
				if ("I".equalsIgnoreCase(tranType.trim())&& errString.trim().length() == 0) 
				{
					sql = "select count(*) from consume_ord_det where cons_order = ? and "
							+ "(case when status is null then 'P' else status end)  <> 'D'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, consOrder);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
							cnt = rs.getInt(1);
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (cnt == 0) 
					{
						sql = "update consume_ord set status = 'D',status_date = ? where  cons_order  =?";
								
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, today);
						pstmt.setString(2, consOrder);
						cnt = pstmt.executeUpdate();
						if (cnt < 0) {
							errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
							return errString;
						}
						pstmt.close();
						pstmt = null;
					}
				}
				/*if lb_ratechanged then
				//prince 30-03-05 convert in case when
//					select sum(nvl(tax_amt,0)), sum(nvl(amount,0)), sum(nvl(net_amt,0))
//					into :lc_taxamt, :lc_totamt, :lc_netamt
//					from consume_iss_det where cons_issue = :ls_cons_iss;
					select sum(case when tax_amt is null then 0 else tax_amt end), 
					sum(case when amount is null then 0 else amount end), 
					sum(case when net_amt is null then 0 else net_amt end)
					into :lc_taxamt, :lc_totamt, :lc_netamt
					from consume_iss_det where cons_issue = :ls_cons_iss;
						
					if get_sqlcode() <> 0 then
						ls_errcode = "DS000" + string(sqlca.sqldbcode) + "~t" + sqlca.sqlerrtext
					else
//						select nvl(exch_rate,0) into :lc_exchrate 
//						from consume_iss where cons_issue = :ls_cons_iss;
						//prince 30-03-05 convert in case when
						select (case when exch_rate is null then 0 else exch_rate end) 
						into :lc_exchrate 
						from consume_iss 
						where cons_issue = :ls_cons_iss;
								
						update consume_iss set amount = :lc_totamt, tax_amt = :lc_taxamt, net_amt = :lc_netamt,
						net_amt__bc = :lc_netamt * :lc_exchrate
						where cons_issue = :ls_cons_iss;
						if get_sqlcode() <> 0 then
							ls_errcode = "DS000" + string(sqlca.sqldbcode) + "~t" + sqlca.sqlerrtext
						end if
					end if
				end if*/ //pb code no migrated
				System.out.println("errString after  : "+errString);
				
				if (errString == null || errString.trim().length() == 0)
				{
					sql = "update consume_iss set confirmed = 'Y', conf_date =?, emp_code__aprv = ?	where cons_issue =?";
					System.out.println("SQL : "+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, today);
					pstmt.setString(2, empCodeAprv);
					pstmt.setString(3, detconsIssue);
					cnt = pstmt.executeUpdate();
					if (cnt < 0) {
						errString = itmDBAccessEJB.getErrorString("", "DS000", "", "", conn);
						return errString;
					}
					pstmt.close();
					pstmt = null;
								/*ls_errcode  = nvo_inv_acct.gbf_ud_accountposting('w_consume_issue', 'C-ISS', '', 2 , &
										 lds_hdr.describe("datawindow.syntax") &
										 + '~r' + lds_hdr.describe("datawindow.syntax.data"), &
										 lds_det.describe("datawindow.syntax") + &
										 '~r' + lds_det.describe("datawindow.syntax.data"), '','', '', '', '')
										 
						ls_acctmethod = f_get_token(ls_errcode,',')							 
						
						
							
						if len(trim(ls_errcode)) <>  0 then
							goto errfound
						end if				
						
				*/ //pb code not migrated 			
					
					errString = invacct.consumeIssPost(detconsIssue, tranSer,conn);
					if (errString != null && errString.trim().length() > 0)
					{
						return errString;
					}
				}
				
			}

		} // try

		catch (Exception e) {
			System.out.println("Exception :ConsumeIssueConf :gbfPostConsIss :==>" + e.getMessage());
			errString = genericUtility.createErrorString(e);
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
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;

	}
	public String createQc(HashMap qcOrd,String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String errcode = "", key = "", win = "", qcNo = "", genLotAuto = "", suppCode = "",geneLotNo = "";
		String tranSer = "",keyCol = "",userId = "",chgTerm = "",chgUser = "",sql = "";
		Date ldtDueDate = null;
		String aprv = "", rej = "", empCode = "", itemSer = "", lotNo = "", suppCodeMfg = "", procMth ="";
		String qcCycleTime = "", qcLeadTime = "", qcLeadTimeItem = "", qcLeadTimeSiteItem = "", qtySample = "";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ValidatorEJB vdt=new ValidatorEJB();
		DistCommon distCommon = new DistCommon();
		Timestamp currDate = null;
		UtilMethods utilMethods = UtilMethods.getInstance();
		java.sql.Date startDate = null, dueDate = null, retestDate = null,qcOrderDate = null;
		try
		{
			System.out.println("----------- Inside createQc -------------");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			sql = "select qc_cycle_time, qc_lead_time from item where item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,qcOrd.get("item_code").toString() );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				qcCycleTime = checkNullAndTrim(rs.getString(1));
				qcLeadTimeItem = checkNullAndTrim(rs.getString(2));
			}
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			if (rs != null )
			{
				rs.close();
				rs = null;
			}			

			sql = "select qc_lead_time,item_ser from siteitem where site_code = ? and item_code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,qcOrd.get("site_code").toString() );
			pstmt.setString(2,qcOrd.get("item_code").toString() );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				qcLeadTimeSiteItem = checkNullAndTrim(rs.getString(1));
				itemSer = checkNullAndTrim(rs.getString(2));
			}
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			if (rs != null )
			{
				rs.close();
				rs = null;
			}			

			if(itemSer == null || itemSer.trim().length() == 0 )
			{
				sql = "select item_ser from item where item_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,qcOrd.get("item_code").toString() );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					itemSer = checkNullAndTrim(rs.getString(1));
				}
				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}				
				if (rs != null )
				{
					rs.close();
					rs = null;
				}			
			}

			if(qcCycleTime == null || qcCycleTime.length() == 0)
			{
				qcCycleTime = "0";
			}

			if(qcLeadTimeSiteItem == null || qcLeadTimeSiteItem.length() == 0)
			{
				qcLeadTime = qcLeadTimeItem;
			}
			else
			{
				qcLeadTime = qcLeadTimeSiteItem;
			}

			if(qcLeadTime == null || qcLeadTime.length() == 0)
			{
				qcLeadTime = "0"; 
			}

			ldtDueDate =  utilMethods.RelativeDate(new Date(currDate.getTime()),(Integer.parseInt(qcCycleTime) + Integer.parseInt(qcLeadTime)));

			qcOrd.put("qorder_date", currDateStr);
			qcOrd.put("start_date", currDateStr);
			qcOrd.put("due_date", sdfAppl.format(ldtDueDate));
			
			sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"w_qcorder_new" );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				key = checkNullAndTrim(rs.getString(1));
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			if (rs != null )
			{
				rs.close();
				rs = null;
			}	
			
			if(key == null || key.length() == 0)
			{
				sql = " select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					key  = rs.getString("key_string");
					keyCol = rs.getString("TRAN_ID_COL");
					tranSer = rs.getString("REF_SER");
				}
				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}				
				if (rs != null )
				{
					rs.close();
					rs = null;
				}
			}

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"user_id");
			String XMLString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>"+
			"\r\n</header><Detail1><site_code>"+qcOrd.get("site_code").toString()+"</site_code><qorder_type>"+qcOrd.get("qorder_type").toString()+"</qorder_type>"+
			" <qorder_date>"+new java.sql.Date(System.currentTimeMillis())+"</qorder_date><lot_no>"+qcOrd.get("lot_no").toString()+"</lot_no><item_ser>"+itemSer+"</item_ser></Detail1></Root>";
			TransIDGenerator tg = new TransIDGenerator(XMLString, userId, "");
			
			qcNo = tg.generateTranSeqID(tranSer, keyCol, key, conn);
			if ("ERROR".equals(qcNo))
			{
				errcode = new ITMDBAccessEJB().getErrorString("", "VTTRANID", "","",conn);
			}

			sql = "select loc_code__aprv,loc_code__rej,PROC_MTH,  QTY_SAMPLE  from siteitem where site_code = ? and item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,qcOrd.get("site_code").toString() );
			pstmt.setString(2,qcOrd.get("item_code").toString() );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				aprv = checkNullAndTrim(rs.getString(1));
				rej = checkNullAndTrim(rs.getString(2));
				procMth = checkNullAndTrim(rs.getString(3));
				qtySample = checkNullAndTrim(rs.getString(4));
			}
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			if (rs != null )
			{
				rs.close();
				rs = null;
			}

			if(procMth == null || procMth.length() == 0)
			{
				if(qtySample == null || qtySample.length() == 0)
				{
					qtySample = "0";
				}
				else if(qtySample != null || qtySample.length() > 0)
				{
					qcOrd.put("qty_sample", qtySample);
				}
			}
			else
			{
				qcOrd.put("qty_sample", "0");
			}

			if(qcOrd.get("lot_no").toString() == null || qcOrd.get("lot_no").toString().length() == 0)
			{
				qcOrd.put("lot_no", qcNo);
				sql = "update workorder_receipt set lot_no = ? where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,qcOrd.get("lot_no").toString() );
				pstmt.setString(2,qcOrd.get("tran_id").toString() );
				int rowcnt = pstmt.executeUpdate();
				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}	
			}
			sql = "select generate_lot_no from siteitem where site_code= ? and item_code= ?"; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,qcOrd.get("site_code").toString() );
			pstmt.setString(2,qcOrd.get("item_code").toString() );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				geneLotNo = checkNull(rs.getString(1));
			}
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			if (rs != null )
			{
				rs.close();
				rs = null;
			}

			if(geneLotNo == null || geneLotNo.length() == 0)
			{
				geneLotNo = "1";
			}
			genLotAuto = distCommon.getDisparams("999999","GENERATE_LOT_NO_AUTO",conn) ;

			if("D-RCP".equalsIgnoreCase(tranSer))
			{
				if(("Y".equalsIgnoreCase(genLotAuto) || "M".equalsIgnoreCase(genLotAuto)) && "1".equalsIgnoreCase(genLotAuto))
				{
					lotNo = qcOrd.get("lot_no").toString();
					qcOrd.put("lot_no",qcNo);
				}
			}
			sql = "select emp_code from users where code = ?"; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,userId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				empCode = checkNullAndTrim(rs.getString(1));
			}
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			if (rs != null )
			{
				rs.close();
				rs = null;
			}
			
			if(qcOrd.get("route_code") == null || qcOrd.get("route_code").toString().trim().length() == 0)
			{
				qcOrd.put("route_code", null);
			}
			String route_code = (qcOrd.get("route_code") == null) ? "" : qcOrd.get("route_code").toString();
			qcOrd.put("qcorder_no", qcNo);

			double qtyPass = Double.parseDouble(qcOrd.get("quantity").toString())-Double.parseDouble(qcOrd.get("qty_sample").toString());
			
			if(qcOrd.get("due_date") != null)
			{
				dueDate = java.sql.Date.valueOf(genericUtility.getValidDateString(qcOrd.get("due_date").toString(), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}
			System.out.println("---------- dueDate -----------------"+dueDate);
			
			
			if(qcOrd.get("qorder_date") != null)
			{
				qcOrderDate = java.sql.Date.valueOf(genericUtility.getValidDateString(qcOrd.get("qorder_date").toString(), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}
			

			sql = "insert into qc_order (qorder_no,qorder_type,qorder_date,site_code,item_code,route_code,quantity,qty_passed,qty_rejected,start_date,due_date,rel_date,porcp_no,porcp_line_no," +
			" lot_no,lot_sl,chg_date,chg_user,chg_term,loc_code,qty_sample,status, unit,qc_create_type,unit__sample,loc_code__aprv,loc_code__rej,lot_no__new, batch_no,expiry_date, proj_code," +
			" emp_code,item_code__new,mfg_date,spec_ref,supp_code,supp_code__mfg,retest_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,qcNo);	
			pstmt.setString(2,qcOrd.get("qorder_type").toString());	
			pstmt.setDate(3,qcOrderDate);
			pstmt.setString(4,qcOrd.get("site_code").toString());	
			pstmt.setString(5,qcOrd.get("item_code").toString());	
			pstmt.setString(6,route_code);	
			pstmt.setDouble(7,Double.parseDouble(qcOrd.get("quantity").toString()));	
			pstmt.setDouble(8,qtyPass);	
			pstmt.setString(9,"0");	
			pstmt.setDate(10, new java.sql.Date(new java.util.Date().getTime()));
			pstmt.setDate(11, dueDate);
			pstmt.setDate(12, new java.sql.Date(new java.util.Date().getTime()));
			pstmt.setString(13,qcOrd.get("tran_id").toString());	
			pstmt.setString(14,qcOrd.get("line_no").toString());	
			pstmt.setString(15,qcOrd.get("lot_no").toString());	
			pstmt.setString(16,qcOrd.get("lot_sl").toString());	
			pstmt.setDate(17,new java.sql.Date(new java.util.Date().getTime()));
			pstmt.setString(18,userId);	
			pstmt.setString(19,chgTerm);	
			pstmt.setString(20,qcOrd.get("loc_code").toString());	
			pstmt.setDouble(21,Double.parseDouble(qcOrd.get("qty_sample").toString()));	
			pstmt.setString(22,"U");
			pstmt.setString(23,qcOrd.get("unit").toString());
			pstmt.setString(24,qcOrd.get("qc_create_type").toString());
			pstmt.setString(25,qcOrd.get("unit").toString());
			pstmt.setString(26,aprv);
			pstmt.setString(27,rej);
			pstmt.setString(28,qcOrd.get("lot_no").toString());
			pstmt.setString(29,qcOrd.get("batch_no").toString());
			pstmt.setDate(30, (java.sql.Date) qcOrd.get("expiry_date"));
			pstmt.setString(31,null);
			
			pstmt.setString(32,empCode);
			pstmt.setString(33,qcOrd.get("item_code").toString());
			pstmt.setDate(34, (java.sql.Date) qcOrd.get("mfg_date"));
			pstmt.setString(35,null);
			pstmt.setString(36,qcOrd.get("supp_code").toString());
			pstmt.setString(37,qcOrd.get("supp_code__mfg").toString());
			//pstmt.setString(38,qcOrd.get("retest_date").toString());			// retest_date is not put into map
			pstmt.setDate(38, null);
			
			pstmt.executeUpdate(); 
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
		}
		catch(Exception e)
		{			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{		
			try{
				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}				
				if (rs != null )
				{
					rs.close();
					rs = null;
				}				
			}
			catch(Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try{
					conn.rollback();

				}catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}

		}
		return errcode;
	}
	public String checkNullAndTrim( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}
	private String checkNull(String input)
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private java.sql.Timestamp getCurrtDate() throws ITMException {
		String currAppdate = "";
		java.sql.Timestamp currDate = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		try {
			Object date = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (currDate);
	}
	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}

}
