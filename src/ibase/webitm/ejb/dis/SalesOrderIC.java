/**
 * VALLABH KADAM
 * SalesOrderIC.java
 * for item change and validation
 * [09/MAY/16]
 * */
package ibase.webitm.ejb.dis;

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
import java.util.Date;

import javax.ejb.Stateless; // added for ejb3

import org.apache.poi.hssf.record.cont.ContinuableRecord;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

@Stateless
// added for ejb3
public class SalesOrderIC extends ValidatorEJB implements SalesOrderICLocal, SalesOrderICRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	String winName = null;
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon = new DistCommon();
	//Commented by Varsha V because its not required as ValidatorEJB is extended
	//ValidatorEJB validator = new ValidatorEJB();
	UtilMethods utlMethods = new UtilMethods();
	String isClassName = "";
	String contractLineNo = "", contractNo = "";
	double adRate = 0;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("Priyanka testing : Inside wfValData 0 ");
		System.out.println("Priyanka testing : xmlString :" + xmlString);
		System.out.println("Priyanka testing : xmlString1 :" + xmlString1);
		System.out.println("Priyanka testing : xmlString2 :" + xmlString2);
		System.out.println("Priyanka testing : objContext :" + objContext);
		System.out.println("Priyanka testing : editFlag :" + editFlag);
		System.out.println("Priyanka testing : xtraParams :" + xtraParams);
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
			System.out.println("Before calling  1212  function wfvalData****");
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);

			System.out.println("After calling method wfVAlData Error String====" + errString);
		} catch (Exception e) {
			System.out.println("Exception : [SalesOrderIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	// gbf_valdata_logic

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String bankCode = "", custCode = "", itemSer = "", terrCode = "", stanCode = "", stanCodeInit = "";
		String siteCode = "", siteCodeShip = "", contractNo = "", blackListed = "", stopBuss = "", countCodeDlv = "",
				countCodeNotify = "";
		String taxClass = "", taxChap = "", taxEnv = "", crTerm = "", currCode = "", currCodeFrt = "", currCodeIns = "",
				transMode = "", salesPers = "", distRoute = "";
		String cofirmed = "", status = "", stanCodeTo = "", salesPers1 = "", salesPers2 = "", tranCode = "",
				quotNo = "", custCode1 = "", dlvTerm = "", notifyTerm = "";
		String saleOrder = "", custPord = "", channelPartner = "", disLink = "", pordDate = "", acctCodeSal = "", lsPosttype = "",
				priceList = "", termCode = "";
		String snCode = "", hazardYn = "", commPercOn = "", commPercOn1 = "", commPercOn2 = "", custCodeNotify = "",
				stateCodeNotify = "", stanCodeNotify = "", priceListClg = "";
		String listType = "", priceListParent = "", salesPerYn = "", priceListDisc = "", modName = "";
		String ordDate = "", contractNoHeader = "", lineNoContr = "", itemCode = "", itemFlg = "", stateCodeDlv = "",
				itemCodeOrd = "";
		String custCodedlv = "", controlledYN = "", drugLicNo = "", itemCodeParent = "", apporderType = "";
		String eou = "", exportOrderType = "", orderType = "", lopReqd = "", unit = "", unitStd = "", unitRate = "",
				packCode = "", mfgCode = "";
		String nature = "", custItemRef = "", prdCodeRfc = "", noAppCustList = "", appCustList = "", schemeCode = "",
				schemeFlag = "", contractYN = "";
		String lsToken = "", lsAppCust = "", lsNoAppCustList = "", itemSerHrd = "", mVal1 = "", othSer = "",
				itemSerProm = "", itemSerCrPer = "", itemSerCrPolicy = "", itemSerCrPolicyHrd = "";
		String salesOrder = "", lsCommPercOnStr = "";
		String remark = "";
		double quantityStduom = 0, rateStduom = 0, discount = 0;
		String stopBusiness = "", saleOption = "", contractReq = "", saleOptionItem = "", transer = "", specRef = "";
		double advPerc = 0, total = 0, totalStd = 0, convQtyStduom = 0, convRtuomStduom = 0, commPerc1 = 0,
				quantity = 0, priceLst = 0, modQty = 0, minQty = 0, maxQty = 0, rate = 0, mrate = 0;
		String isContractYn = "N";
		String lsOrderType = "";
		double totOrdValue = 0, maxOrderValue = 0, orderValue = 0, orderValueO = 0;
		Date dueDate1 = null, drugDateUpto = null, drugLicNoUpto = null;
		Timestamp orderDate = null, dueDate = null, pDate = null, appFrom = null, validUpto = null, plDate = null,
				restUpto = null;
		Timestamp dspDate = null;
		boolean lbProceed = false;
		java.sql.Timestamp today = null;
		double commPerc = 0, commPerc2 = 0;
		Date porddate = null;
		int currentFormNo = 0;
		int childNodeListLength;
		int ctr = 0;
		int cnt = 0, llcnt = 0;
		String chkActive ="";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
        String crType = "";
		String schAttr = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		// Calendar currentDate = Calendar.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		ConnDriver connDriver = new ConnDriver();

		FinCommon finCommon = new FinCommon();
		double rateClg = 0, commPer1 = 0, commPerOn1 = 0, commPer2 = 0, commPer3 = 0, commPerOn3 = 0;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		System.out.println("Priyanka testing : editFlag=====>> :" + editFlag);
		String schemeStkChk=""; //added by nandkumar gadkari on 21/09/19
		double availQty=0.0;//added by nandkumar gadkari on 21/09/19
		String cctrCodeSal="";
		try {
			System.out.println("In empty try block");

			System.out.println("try1");
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			System.out.println("try2");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			today = java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			System.out.println("try3");
			modName = ("w_" + getValueFromXTRA_PARAMS(xtraParams, "obj_name")).toUpperCase();
			System.out.println("transer=============================" + modName);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("Priyanka testing : currentFormNo :" + currentFormNo);
			}
			switch (currentFormNo) {
			case 1:

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("@V@ Priyanka testing :parentNode : " + parentNode);
				System.out.println("@V@ Priyanka testing :childNodeListLength : " + childNodeListLength);
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("@V@ Priyanka testing :childNodeName : " + childNodeName);

					if (childNodeName.equalsIgnoreCase("sale_order")) {

						salesOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						System.out.println("Inside wfval purcOrder>>>" + salesOrder);
						String keyFlag = "";

						sql = "select key_flag from transetup where tran_window='w_sorder' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							keyFlag = rs.getString("key_flag");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("Key Flag>>>>>>>>>" + keyFlag);
						if (keyFlag.equalsIgnoreCase("M")) {
							if (salesOrder == null || salesOrder.length() == 0) {
								errList.add("VTSORDEMP");
								errFields.add(childNodeName.toLowerCase());
							}
							System.out.println("Edit Flag>>>>" + editFlag);
							if ("A".equalsIgnoreCase(editFlag)) {
								sql = " SELECT COUNT(1) FROM sorder WHERE sale_order = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, salesOrder);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt > 0) {
									errList.add("INVSONOES");
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("order_date")) {
						String orderDateStr = "", despDateNew = "", currentDateStr = "", lsContractNo = "";
						orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom));
						//changes-by-monika-23-05-2019-
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						//end
						System.out.println("@V@ orderDateStr====[" + orderDateStr + "]");
						if (orderDateStr == null || orderDateStr.length() == 0) {
							errCode = "VTORDDT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//changes made-by-MOnika-on-23-05-2019
						else 
						{

							orderDate= Timestamp.valueOf(genericUtility.getValidDateString(orderDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							errCode=finCommon.nfCheckPeriod("SAL",orderDate,siteCode, conn);
							System.out.println("@@@@ orderDate["+orderDate+"]errCode["+errCode+"]");
							if (errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						//end
						//end
						lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						if (lsContractNo != null && lsContractNo.trim().length() > 0) {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							//Changes and commented by Ajay on 05/04/18:START
							// sql = "select count(*) into as cnt from   scontract where  contract_no = ? and ? between eff_from and valid_upto";
							sql = "select count(*)  as cnt from   scontract where  contract_no = ? and ? between eff_from and valid_upto";
							//Changes and commented by Ajay on 05/04/18:END
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsContractNo);
							pstmt.setTimestamp(2, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTIORDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					} else if (childNodeName.equalsIgnoreCase("order_type")) {

						System.out.println(">>>>>>>>>>Inside Order_Type>>>>>>>>>>>>>");
						lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
						if (lsOrderType == null || lsOrderType.trim().length() == 0) {
							errCode = "VMORTYBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (lsOrderType != null && lsOrderType.trim().length() > 0) {
							sql = "select count(*) as cnt from sordertype where order_type=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsOrderType);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							if (rs != null) {
								rs.close();
								rs = null;
							}
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
							if (cnt == 0) {
								errCode = "VTINVORD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("contract_no")) {
						String lsContractNo = "", lsConf = "", lsStatus = "";
						lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						if (lsContractNo != null && lsContractNo.length() > 0) {
							sql = "select confirmed, status from scontract where contract_no =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsContractNo);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsConf = checkNull(rs.getString("confirmed"));
								lsStatus = checkNull(rs.getString("status"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (lsConf.equalsIgnoreCase("N")) {
								errCode = "VTISCNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if (lsStatus.equalsIgnoreCase("X")) {
								errCode = "VSCCAN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("bank_code")) {
						bankCode = checkNull(genericUtility.getColumnValue("bank_code", dom));
						if (bankCode != null && bankCode.trim().length() > 0) {
							if (!(isExist(conn, "bank", "bank_code", bankCode))) {
								errCode = "VMBANK1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					//Added by Shubham.S.B on 12-03-2021
					// remarks Validation for not allowing single quote in string
					else if (childNodeName.equalsIgnoreCase("remarks")) {
						remark = checkNull(genericUtility.getColumnValue("remarks", dom));
						if (remark != null && remark.trim().length() > 0) {
							if (remark.contains("'")) {
								errCode = "VTDNALSPLC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// remarks Validation ends
					
					if (childNodeName.equalsIgnoreCase("cust_code")) {
						System.out.println("Inside cust code>>>>>>>>>>>>>>>>");
						String mSiteCode = "", orderDateStr = "", lsItemser = "", blackListedYn = "",
								lsStopBusiness = "", lsAvailableYn = "", lsChannelPartner = "";
						String blackListedCust = "";
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						mSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom));
						lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
						siteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
						System.out.println("sitecode>>>>>>>>>"+mSiteCode);
						System.out.println("sitecodeship>>>>>>>>>"+siteCodeShip);
						sql = "select black_listed from customer_series where cust_code=? and item_ser =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							blackListedYn = checkNull(rs.getString("black_listed"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (blackListedYn.equalsIgnoreCase("Y")) {
							errCode = "VTCUSTCD3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							//removed validator. by Varsha V because its not required as ValidatorEJB is extended
							errCode = finCommon.isCustomer(mSiteCode, custCode, modName, conn);//mukesh chauhan on 23/03/2020
							errCode = finCommon.isCustomer(siteCodeShip, custCode, modName, conn);//mukesh chauhan on 23/03/2020
							System.out.println("mSiteCode>>>>>"+mSiteCode);
							System.out.println("siteCodeShip>>>>>"+siteCodeShip);
							if (errCode.trim().length() == 0) {
								/*
								 * sql = "select stop_business   from customer where cust_code = ?"; pstmt =
								 * conn.prepareStatement(sql); pstmt.setString(1, custCode); rs =
								 * pstmt.executeQuery(); if (rs.next()) { lsStopBusiness =
								 * checkNull(rs.getString("stop_business")); } pstmt.close(); pstmt = null;
								 * rs.close(); rs = null;
								 */
								// Added by Abhijit on 21/03/2017
								sql = "select stop_business ,black_listed  from customer where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsStopBusiness = checkNull(rs.getString("stop_business"));
									blackListedCust = checkNull(rs.getString("black_listed"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								// ended by Abhijit on 21/03/2017
								if (lsStopBusiness.equalsIgnoreCase("Y")) {
									errCode = "VTICC";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								// Added by Abhijit on 21/03/2017
								if ("Y".equalsIgnoreCase(blackListedCust)) {
									errCode = "VTCUSTCD3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								// ended by Abhijit on 21/03/2017
							} else {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if (errCode == null || errCode.trim().length() == 0) {
							lsAvailableYn = checkNull(genericUtility.getColumnValue("available_yn", dom));
							if (lsAvailableYn.equalsIgnoreCase("N")) {
								sql = "select channel_partner from site_customer where cust_code=? and site_code=? and available_yn='N'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, mSiteCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsChannelPartner = checkNull(rs.getString("channel_partner"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (lsChannelPartner == null || lsChannelPartner.trim().length() == 0) {
									sql = "select channel_partner  from customer where cust_code=? and available_yn='N'";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										lsChannelPartner = checkNull(rs.getString("channel_partner"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								if (lsChannelPartner == null || lsChannelPartner.trim().length() == 0) {
									lsChannelPartner = "N";
								}
							}
							if (lsChannelPartner.equalsIgnoreCase("N")) {
								errCode = "VTCUSTCD4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("cust_code__bil")) {
						System.out.println("@V@ In cust code bill to validation");
						String mSiteCode = "", orderDateStr = "", lsItemser = "", blackListedYn = "",
								lsStopBusiness = "", lsAvailableYn = "", lsChannelPartner = "";
						custCode = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
						mSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom));
						lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));

						sql = "select black_listed from customer_series where cust_code=? and item_ser =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							blackListedYn = checkNull(rs.getString("black_listed"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (blackListedYn.equalsIgnoreCase("Y")) {
							errCode = "VTCUSTCD3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							//removed validator. by Varsha V because its not required as ValidatorEJB is extended
							errCode = isCustomer(mSiteCode, custCode, modName, conn);
							if (errCode.trim().length() == 0) {
								sql = "select stop_business from customer where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsStopBusiness = checkNull(rs.getString("stop_business"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (lsStopBusiness.equalsIgnoreCase("Y")) {
									errCode = "VTICC";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if (errCode == null || errCode.trim().length() == 0) {
							lsAvailableYn = checkNull(genericUtility.getColumnValue("available_yn", dom));
							if (lsAvailableYn.equalsIgnoreCase("N")) {
								sql = "select channel_partner from site_customer where cust_code=? and site_code=? and available_yn='N'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, mSiteCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsChannelPartner = checkNull(rs.getString("channel_partner"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (lsChannelPartner == null || lsChannelPartner.trim().length() == 0) {
									sql = "select channel_partner  from customer where cust_code=? and available_yn='N'";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										lsChannelPartner = checkNull(rs.getString("channel_partner"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								if (lsChannelPartner == null || lsChannelPartner.trim().length() == 0) {
									lsChannelPartner = "N";
								}
							}
							if (lsChannelPartner.equalsIgnoreCase("N")) {
								errCode = "VTCUSTCD4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("terr_code")) {
						String lsTerrCode = "";
						// 28-dec-2019 manoharan terr_code is varchar2(5) so trim should not be done
						//lsTerrCode = checkNull(genericUtility.getColumnValue("terr_code", dom));
						lsTerrCode = genericUtility.getColumnValue("terr_code", dom);
						if (lsTerrCode != null && lsTerrCode.trim().length() > 0) {
							sql = "select count(*) as cnt from territory where terr_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsTerrCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTTERRCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("site_code")) {
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						System.out.println("siteCode: " + siteCode);
						if (siteCode == null || siteCode.trim().length() == 0) {
							errCode = "VMSITECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Site Code can not be blank!!");
						} else {
							if (siteCode != null && siteCode.trim().length() > 0) {
								if (!(isExist(conn, "site", "site_code", siteCode))) {
									errCode = "VMSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					// site_code__ship
					else if (childNodeName.equalsIgnoreCase("site_code__ship")) {
						siteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
						System.out.println("siteCodeShip: " + siteCodeShip);

						if (siteCodeShip == null || siteCodeShip.trim().length() == 0) {
							errCode = "VMSITECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Site Code can not be blank!!");
						}
						if (siteCodeShip != null && siteCodeShip.trim().length() > 0) {
							if (!(isExist(conn, "site", "site_code", siteCodeShip))) {
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// count_code__dlv
					else if (childNodeName.equalsIgnoreCase("count_code__dlv")) {
						countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom));
						System.out.println("countCodeDlv: " + countCodeDlv);

						if (countCodeDlv != null && countCodeDlv.trim().length() != 0) {
							if (!(isExist(conn, "country", "count_code", countCodeDlv))) {
								errCode = "VTCONT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());// **pending
								System.out.println("The country entered does not exist in the site master");
							}
						}
					}
					// count_code__notify
					else if (childNodeName.equalsIgnoreCase("count_code__notify")) {
						countCodeNotify = checkNull(genericUtility.getColumnValue("count_code__notify", dom));
						System.out.println("countCodeNotify: " + countCodeNotify);
						if (countCodeNotify != null && countCodeNotify.trim().length() != 0) {
							if (!(isExist(conn, "country", "count_code", countCodeNotify))) {
								errCode = "VMCOUNT1  ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());// **pending
								System.out.println("The country entered does not exist in the site master");
							}
						}
					}
					// stan_code
					else if (childNodeName.equalsIgnoreCase("stan_code")) {
						stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
						System.out.println("stanCode: " + stanCode);
						// if (stanCode != null && stanCode.trim().length() > 0)
						// {
						if (!(isExist(conn, "station", "stan_code", stanCode))) {
							errCode = "VTSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// }
					} else if (childNodeName.equalsIgnoreCase("stan_code__init")) {
						stanCode = checkNull(genericUtility.getColumnValue("stan_code__init", dom));
						System.out.println("stanCode: " + stanCode);
						if (stanCode != null && stanCode.trim().length() > 0) {
							if (!(isExist(conn, "station", "stan_code", stanCode))) {
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("item_ser")) {
						System.out.println("@V@ Validating HDR item_ser");
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
						System.out.println("itemSer: " + itemSer);

						if (itemSer == null || itemSer.trim().length() == 0) {
							errCode = "VTITEMSER5";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (itemSer != null && itemSer.trim().length() > 0) {
							if (!(isExist(conn, "itemser", "item_ser", itemSer))) {
								errCode = "VTITMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("DIVISION CODE LEFT BLANK OR NOT PROPER.");
							} else {
								//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][Start]
								String serSpecificCust = "";
								serSpecificCust = distCommon.getDisparams("999999", "SER_SPECIFIC_CUST", conn);
								if("Y".equalsIgnoreCase(serSpecificCust)) 
								{
									//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][End]
									custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
									sql = "select count(*)  from customer_series where cust_code = ? and item_ser = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, itemSer);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt == 0) {
										errCode = "VTITEMSER4";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}
					// tax_class
					else if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
						System.out.println("taxClass: " + taxClass);
						if (taxClass.trim().length() != 0) {
							if (!(isExist(conn, "taxclass", "tax_class", taxClass))) {
								errCode = "VTTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Tax class not define in tax class master.");
							}
						}
					}
					// tax_chap
					else if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
						System.out.println("taxChap: " + taxChap);
						if (taxChap.trim().length() > 0) {
							if (!(isExist(conn, "taxchap", "tax_chap", taxChap))) {
								errCode = "VTTCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Tax chapter not define in tax chapter master");
							}
						}
					}
					// tax_env
					else if (childNodeName.equalsIgnoreCase("tax_env")) {
						taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");

						if (taxEnv.trim().length() != 0) {
							if (!(isExist(conn, "taxenv", "tax_env", taxEnv))) {
								errCode = "VTTENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("TAX ENVIRONMENT NOT DEFINED");
							} else {
								//Pavan R 17sept19 start[to validate tax environment]
								//errCode = gfCheckTaxenvStatus(taxEnv, orderDate, conn);															
								//errCode = gfCheckTaxenvStatus(taxEnv, orderDate, "S", conn);
								errCode = distCommon.getCheckTaxEnvStatus(taxEnv, orderDate, "S", conn);
								//Pavan R 17sept19 end[to validate tax environment]
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("TAX ENVIRONMENT NOT DEFINED");
								}
							}
						}
					}
					// cr_term
					else if (childNodeName.equalsIgnoreCase("cr_term")) {
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						System.out.println("crTerm: " + crTerm);

						if (crTerm == null || crTerm.trim().length() == 0) {
							errCode = "VTCRTERM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if (crTerm != null && crTerm.trim().length() > 0) {

                            sql = "Select cr_type from crterm where cr_term = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();

							if (rs.next()) {
								//cnt = rs.getInt(1);
                                crType = rs.getString("cr_type");
                            }
                            rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
                            if (!"R".equalsIgnoreCase(crType)) 
                            {
                                errCode = "VMCRTEMSO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
                            }
							if (!(isExist(conn, "crterm", "cr_term", crTerm))) {
								errCode = "VTCRTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// curr_code
					else if (childNodeName.equalsIgnoreCase("curr_code")) {
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						currCode = currCode == null ? "" : currCode.trim();
						System.out.println("currCode: " + currCode);
						if (currCode.length() == 0) {
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if (!(isExist(conn, "currency", "curr_code", currCode))) {
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					// curr_code__frt
					else if (childNodeName.equalsIgnoreCase("curr_code__frt")) {
						currCodeFrt = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));
						System.out.println("currCodeFrt: " + currCodeFrt);

						if (!(isExist(conn, "currency", "curr_code", currCodeFrt))) {
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					// curr_code__ins
					else if (childNodeName.equalsIgnoreCase("curr_code__ins")) {
						currCodeIns = checkNull(genericUtility.getColumnValue("curr_code__ins", dom));
						System.out.println("currCodeIns: " + currCodeIns);

						if (!(isExist(conn, "currency", "curr_code", currCodeIns))) {
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out
							.println("The currency code you have entered is not found in the currency master.");
						}

					}
					// trans_mode
					else if (childNodeName.equalsIgnoreCase("trans_mode")) {
						transMode = checkNull(genericUtility.getColumnValue("trans_mode", dom));
						System.out.println("transMode: " + transMode);
						if (transMode == null || transMode.trim().length() == 0) {
							errCode = "VMTRMOD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Transportation mode cannot be null or blank");
						}
					}
					// sales_pers
					else if (childNodeName.equalsIgnoreCase("sales_pers")) {
                        System.out.println("In Sales_person::");
                        String mItemSer = "", orderDateStr = "", lsSalesPersYn = "";
                        //salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));
                        salesPers = genericUtility.getColumnValue("sales_pers", dom);//Changed by Anagha R
						mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
                        System.out.println("SalesPerson:: "+salesPers);
                        //System.out.println("SalesPerson.length:: "+salesPers.length());
                        //Added by Anagha R on 04/01/2021 for Integrity constraint (APPVIS.FK_SORDER_SALES_PERS) violated - parent key not found(Spaces) START                            
                        //if(salesPers.length() > 0 && salesPers.trim().length() == 0){
                          if(salesPers != null && salesPers.trim().length() == 0){  
                            System.out.println("Spaces not alllowed for Sales Person");
                                errCode = "VTSALPRN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
                        }
                        //Added by Anagha R on 04/01/2021 for Integrity constraint (APPVIS.FK_SORDER_SALES_PERS) violated - parent key not found START                            

						if (salesPers != null && salesPers.trim().length() > 0) {

                            //Added by Anagha R on 29/12/2020 for Integrity constraint (APPVIS.FK_SORDER_SALES_PERS) violated - parent key not found START                            
                            int count1 = 0;
							sql = "select count(*) from sales_pers where sales_pers= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, salesPers);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count1 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
                            System.out.println("count1: :"+count1);
							if (count1 == 0) {
								errCode = "VTSALPRN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
                            }                          

                //Added by Anagha R on 29/12/2020 for Integrity constraint (APPVIS.FK_SORDER_SALES_PERS) violated - parent key not found END
				
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom));
                            errCode = finCommon.isSalesPerson(siteCode, salesPers, modName, conn);
                		} else {
							sql = "select (case when sales_pers_yn is null then 'N' else sales_pers_yn end) as sales_pers_yn from itemser where  item_ser =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mItemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsSalesPersYn = rs.getString("sales_pers_yn");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (lsSalesPersYn.equalsIgnoreCase("Y")) {
								errCode = "VMSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// dist_route
					else if (childNodeName.equalsIgnoreCase("dist_route")) {
						distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom));
						stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
						if (distRoute != null && distRoute.trim().length() > 0) {
							if (!(isExist(conn, "distroute", "dist_route", distRoute))) {
								errCode = "VTDISTRT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								sql = "select stan_code__to from distroute where dist_route =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, distRoute);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									stanCodeTo = rs.getString("stan_code__to");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!stanCodeTo.equalsIgnoreCase(stanCode)) {
									errCode = "VMSTANMIS1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}

					// sales_pers__1
					else if (childNodeName.equalsIgnoreCase("sales_pers__1")) {
						salesPers1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom));
						if (salesPers1 != null && salesPers1.trim().length() > 0) {

							if (!(isExist(conn, "sales_pers", "sales_pers", salesPers1))) {
								errCode = "VMSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// sales_pers__2
					else if (childNodeName.equalsIgnoreCase("sales_pers__2")) {
						salesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom));
						if (salesPers2 != null && salesPers2.trim().length() > 0) {

							if (!(isExist(conn, "sales_pers", "sales_pers", salesPers2))) {
								errCode = "VMSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// adv_perc
					else if (childNodeName.equalsIgnoreCase("adv_perc")) {
						//advPerc = Integer.parseInt(genericUtility.getColumnValue("adv_perc", dom));
						String advPercStr = genericUtility.getColumnValue("adv_perc", dom);
						if (advPercStr == null || "null".equals(advPercStr) || advPercStr.trim().length() == 0)
						{
							advPercStr = "0.00";
						}
						//advPerc = Double.parseDouble(genericUtility.getColumnValue("adv_perc", dom));
						advPerc = Double.parseDouble(advPercStr);
						if (advPerc > 100) {
							errCode = "VMADVPERC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					// tran_code
					else if (childNodeName.equalsIgnoreCase("tran_code")) {
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						System.out.println("tranCode: " + tranCode);
						if (tranCode.trim().length() != 0) {

							if (!(isExist(conn, "transporter", "tran_code", tranCode))) {
								errCode = "VMTRAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// quot_no
					else if (childNodeName.equalsIgnoreCase("quot_no")) {
						String mCustCode = "";
						quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						System.out.println("quotNo..."+quotNo+"custCode....."+custCode);
						if (quotNo != null && quotNo.trim().length() > 0) {
							sql = "select cust_code from sales_quot where quot_no = ?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, distRoute);//Changes done by Rohini T on [01/02/2021]
							pstmt.setString(1, quotNo);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mCustCode = rs.getString("cust_code");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (mCustCode == null || mCustCode.trim().length() == 0) {
								errCode = "VTQUOT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} //else if (!mCustCode.equalsIgnoreCase(custCode)) {//Commented and added by Rohini T on[04/02/2021]
							else if (!mCustCode.trim().equalsIgnoreCase(custCode.trim())) {
								errCode = "VTQUOT2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// dlv_term
					else if (childNodeName.equalsIgnoreCase("dlv_term")) {
						dlvTerm = checkNull(genericUtility.getColumnValue("dlv_term", dom));
						System.out.println("quotNo: " + dlvTerm);
						if (dlvTerm.trim().length() != 0) {
							if (!(isExist(conn, "delivery_term", "dlv_term", dlvTerm))) {
								errCode = "VMDLVTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// notify_term
					else if (childNodeName.equalsIgnoreCase("notify_term")) {
						dlvTerm = checkNull(genericUtility.getColumnValue("notify_term", dom));
						System.out.println("Notify Term: " + dlvTerm);
						if (dlvTerm.trim().length() != 0) {
							if (!(isExist(conn, "delivery_term", "dlv_term", dlvTerm))) {
								errCode = "VMDLVTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("emp_code__ord")) {
						String empCodeOrd = "";
						empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord", dom));
						if (empCodeOrd != null && empCodeOrd.trim().length() > 0) {
							if (!(isExist(conn, "employee", "emp_code", empCodeOrd))) {
								errCode = "VMEMPORD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("emp_code__ord1")) {
						String empCodeOrd = "";
						empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord1", dom));
						System.out.println("Inside wfvaldata 982"+empCodeOrd);
						if (empCodeOrd != null && empCodeOrd.trim().length() > 0) {
							System.out.println("Inside if condition 982 "+empCodeOrd);
							if (!(isExist(conn, "employee", "emp_code", empCodeOrd))) {
								errCode = "VMEMPORD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// due_date
					else if (childNodeName.equalsIgnoreCase("due_date")) {
						String empCodeOrd = "";
						// dueDate = Timestamp.valueOf(genericUtility.getColumnValue("due_date", dom));
						dueDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("due_date", dom),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						// orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom));
						orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						if (dueDate.before(orderDate)) {
							errCode = "VTSCH1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					// cust_pord
					else if (childNodeName.equalsIgnoreCase("cust_pord")) {
						Timestamp mPordDate = null, ldtFrdt = null, ldtTodt = null;
						custPord = checkNull(genericUtility.getColumnValue("cust_pord", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						// orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom));
						orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						// mPordDate = Timestamp.valueOf(genericUtility.getColumnValue("pord_date",
						// dom));
						// mPordDate =
						// Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("pord_date",
						// dom), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) +
						// " 00:00:00.0");
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if (custPord != null && custPord.trim().length() > 0) {
							sql = "select count(*) as cnt from sorder where cust_code =? and cust_pord =? and sale_order <>?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, custPord);
							pstmt.setString(3, saleOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								errCode = "VTCUSTPODT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						sql = "select channel_partner,dis_link from site_customer where cust_code=? and site_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							channelPartner = rs.getString("channel_partner");
							disLink = rs.getString("dis_link");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (channelPartner == null && errCode.trim().length() == 0) {
							sql = "select channel_partner,dis_link from customer where cust_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								channelPartner = rs.getString("channel_partner");
								disLink = rs.getString("dis_link");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if ("A".equalsIgnoreCase(disLink)
								|| "S".equalsIgnoreCase(disLink) && "Y".equalsIgnoreCase(channelPartner)) {
							if (custPord != null && custPord.trim().length() > 0) {
								sql = "select status from porder where purc_order = ? and	confirmed  ='Y'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custPord);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									status = rs.getString("status");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!status.equalsIgnoreCase("O")) {
									errCode = "VTPONF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						sql = "select fr_date,to_date from acctprd where ? between fr_date and to_date";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, orderDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ldtFrdt = rs.getTimestamp("fr_date");
							ldtTodt = rs.getTimestamp("to_date");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select count(*) as ll_cnt from sorder where cust_pord = ? and status <> 'X'";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custPord);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("ll_cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt > 0) {
							sql = "select count(*) as ll_cnt from sorder where CUST_CODE= ? and order_date BETWEEN  ? AND ?"
									+ " AND cust_pord = ? and status <> 'X' and sale_order <>?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setTimestamp(2, ldtFrdt);
							pstmt.setTimestamp(3, ldtTodt);
							pstmt.setString(4, custPord);
							pstmt.setString(5, saleOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("ll_cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								errCode = "VTCUSPOFIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("pord_date")) {
						Timestamp pordDt = null;
						String poDtStr = "";
						poDtStr = genericUtility.getColumnValue("pord_date", dom);
						if (poDtStr != null && poDtStr.trim().length() > 0) {
							// pordDt = Timestamp.valueOf(genericUtility.getColumnValue("pord_date", dom));
							pordDt = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("pord_date", dom),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							// orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date",
							// dom));
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");

							if (pordDt.after(orderDate)) {
								errCode = "VTPODATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// acct_code__sal
					else if (childNodeName.equalsIgnoreCase("acct_code__sal")) 
					{
						acctCodeSal = checkNull(genericUtility.getColumnValue("acct_code__sal", dom));
						lsPosttype =  checkNull(finCommon.getFinparams("999999", "SALES_INV_POST_HDR", conn)); //Added by Dipesh p on[16/06/2020]
						System.out.println("lsPosttype::::::"+lsPosttype);
						System.out.println("acctCodeSal: " + acctCodeSal);
						if (acctCodeSal != null && acctCodeSal.trim().length() > 0) 
						{
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							errCode = finCommon.isAcctCode(siteCode, acctCodeSal, "S-INV", conn);
							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Added by Dipesh p on[16/06/2020][Start]
						else if("H".equalsIgnoreCase(lsPosttype) && (acctCodeSal == null || acctCodeSal.trim().length() == 0))
						{
							errCode = "VTACTSAL02";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Added by Dipesh p on[16/06/2020][End]
						
					}
					// price_list
					else if (childNodeName.equalsIgnoreCase("price_list")) {
						String lsPriceListParent = "", lsOrdtypeSample = "";
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom));
						if (priceList != null && priceList.trim().length() > 0) {
							sql = "select distinct list_type from pricelist_mst where price_list =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								listType = rs.getString("list_type");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (listType == null || listType.trim().length() == 0) {
								sql = "select (case when price_list__parent is null  then '' else price_list__parent end )"
										+ " from pricelist_mst where price_list =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsPriceListParent = rs.getString("price_list__parent");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}

							if (lsPriceListParent != null && lsPriceListParent.trim().length() > 0) {
								sql = "select distinct list_type  from pricelist_mst where price_list =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsPriceListParent);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									listType = rs.getString("list_type");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}

							if (listType.equalsIgnoreCase("B") || listType.equalsIgnoreCase("L")
									|| listType.equalsIgnoreCase("I") || listType.equalsIgnoreCase("F")) {

							} else {
								errCode = "VPLSTYPE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						orderType = checkNull(genericUtility.getColumnValue("order_type", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

						sql = "SELECT  price_list FROM cust_plist WHERE cust_code = ? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, orderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceList = checkNull(rs.getString("price_list"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);
						if (priceList == null || priceList.trim().length() == 0) {
							if (orderType.equalsIgnoreCase(lsOrdtypeSample)) {
								errCode = "VMPLIST20";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					// price_list__clg
					else if (childNodeName.equalsIgnoreCase("price_list__clg")) {
						priceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom));
						if (priceListClg != null && priceListClg.trim().length() > 0) {
							sql = "select distinct list_type from pricelist_mst where price_list = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceListClg);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								listType = rs.getString("list_type");
								if (listType == null) {
									do {
										sql = "select (case when price_list__parent is null then '' else price_list__parent end ) as result"
												+ " from pricelist_mst where price_list =?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, priceListClg);
										rs1 = pstmt.executeQuery();
										if (rs.next()) {
											priceListParent = rs.getString("price_list__parent");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if (priceListParent == null || priceListParent.trim().length() == 0) {
											break;
										}
										if (priceListParent.trim().length() > 0) {
											sql = " select distinct list_type  from pricelist_mst	where price_list =?";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, priceListParent);
											rs1 = pstmt1.executeQuery();
											while (rs1.next()) {
												listType = rs1.getString("list_type");
												if (listType.trim().length() > 0) {
													listType = priceListParent;
													break;
												}
												if (listType == null) {
													listType = priceListParent;
													continue;
												}
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
										}
									} while (true);
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (listType.equalsIgnoreCase("B") || listType.equalsIgnoreCase("L")
									|| listType.equalsIgnoreCase("I") || listType.equalsIgnoreCase("F")) {

							} else {
								errCode = "VPLSTYPE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Price List Type is invalid !!!. ");
							}
						}
					}
					// price_list__disc
					else if (childNodeName.equalsIgnoreCase("price_list__disc")) {
						priceListDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom));
						// Changed By pragyan 19/Mar/17 wrong veriable check
						// if (priceListClg != null && priceListClg.trim().length() > 0)
						if (priceListDisc != null && priceListDisc.trim().length() > 0) {
							sql = "select distinct list_type from pricelist_mst where price_list = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceListDisc);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								listType = rs.getString("list_type");
								if (listType == null) {
									do {
										sql = "select (case when price_list__parent is null then '' else price_list__parent end ) as result"
												+ " from pricelist_mst where price_list =?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, priceListDisc);
										rs1 = pstmt.executeQuery();
										if (rs.next()) {
											priceListParent = rs.getString("price_list__parent");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										pstmt1.close();
										pstmt1 = null;
										if (priceListParent == null || priceListParent.trim().length() == 0) {
											break;
										}
										if (priceListParent.trim().length() > 0) {
											sql = " select distinct list_type  from pricelist_mst	where price_list =?";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, priceListParent);
											rs1 = pstmt1.executeQuery();
											while (rs1.next()) {
												listType = rs1.getString("list_type");
												if (listType.trim().length() > 0) {
													listType = priceListParent;
													break;
												}
												if (listType == null) {
													listType = priceListParent;
													continue;
												}
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
										}
									} while (true);
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (listType.equalsIgnoreCase("M") || listType.equalsIgnoreCase("N")) {

							} else {
								errCode = "VPLSTYPE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}

					}
					// sn_code
					else if (childNodeName.equalsIgnoreCase("sn_code")) {
						snCode = checkNull(genericUtility.getColumnValue("sn_code", dom));
						hazardYn = checkNull(genericUtility.getColumnValue("hazard_yn", dom));

						if (hazardYn == "Y") {
							if (!(isExist(conn, "safety_norm", "sn_code", snCode))) {
								errCode = "VTSNCODE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" Safety Norm Code Not Defined In Master. ");
							}
						} else if (snCode != null && snCode.trim().length() > 0) {
							if (!(isExist(conn, "safety_norm", "sn_code", snCode))) {
								errCode = "VTSNCODE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" Safety Norm Code Not Defined In Master. ");
							}
						}

					}
					// comm_perc
					// Commented by Santosh on 24/03/2017 need to compare business logic with
					// orginal component [Start]
					/*
					 * else if (childNodeName.equalsIgnoreCase("comm_perc"))// doubt { commPerc =
					 * checkDoubleNull((genericUtility.getColumnValue("comm_perc", dom)));
					 * System.out.println(); commPercOn =
					 * checkNull(genericUtility.getColumnValue("comm_perc__on", dom)); if (commPerc
					 * != 0 && commPerc > 0) { if (commPercOn == null || commPercOn.trim().length()
					 * == 0) { errCode = "VTSOCOMM1"; errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase()); System.out.
					 * println("Commission On Cannot Be Blank If Commission Perc Is Specified "); }
					 * } else if (commPercOn != null && commPercOn.trim().length() > 0 &&
					 * !"N".equalsIgnoreCase(commPercOn)) { errCode = "VTSOCOMM2";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); }
					 * 
					 * } // comm_perc_1 else if (childNodeName.equalsIgnoreCase("comm_perc_1"))//
					 * doubt { commPerc1 =
					 * checkDoubleNull((genericUtility.getColumnValue("comm_perc_1", dom)));
					 * commPercOn = checkNull(genericUtility.getColumnValue("comm_perc_on_1", dom));
					 * if (commPerc1 != 0 && commPerc1 > 0) { if (commPercOn == null ||
					 * commPercOn.trim().length() == 0) { errCode = "VTSOCOMM1";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); System.out.
					 * println("Commission On Cannot Be Blank If Commission Perc Is Specified "); }
					 * } else if (commPercOn != null && commPercOn.trim().length() > 0 &&
					 * !"N".equalsIgnoreCase(commPercOn)) { errCode = "VTSOCOMM2";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); }
					 * 
					 * } // comm_perc_2 else if (childNodeName.equalsIgnoreCase("comm_perc_2"))//
					 * doubt { commPerc2 =
					 * checkDoubleNull((genericUtility.getColumnValue("comm_perc_2", dom)));
					 * commPercOn = checkNull(genericUtility.getColumnValue("comm_perc_on_2", dom));
					 * if (commPerc2 != 0 && commPerc2 > 0) { if (commPercOn == null ||
					 * commPercOn.trim().length() == 0) { errCode = "VTSOCOMM1";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); System.out.
					 * println("Commission On Cannot Be Blank If Commission Perc Is Specified "); }
					 * } else if (commPercOn != null && commPercOn.trim().length() > 0 &&
					 * !"N".equalsIgnoreCase(commPercOn)) { errCode = "VTSOCOMM2";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); }
					 * 
					 * }
					 */
					// Commented by Santosh on 24/03/2017 need to compare business logic with
					// orginal component [End]
					// cust_code__notify
					else if (childNodeName.equalsIgnoreCase("cust_code__notify"))// doubt
					{
						custCodeNotify = checkNull((genericUtility.getColumnValue("cust_code__notify", dom)));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						if (genericUtility.getColumnValue("order_date", dom) == null) {
							orderDate = null;
						} else {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));

						if (custCodeNotify.trim().length() > 0) {
							sql = "select black_listed  from customer_series where cust_code=? and item_ser =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								blackListed = rs.getString("black_listed");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if ("Y".equalsIgnoreCase(blackListed)) {
								errCode = "VTCUSTCD3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" Safety Norm Code Not Defined In Master. ");
							} else {
								errCode = this.isCustomer(siteCode, custCodeNotify, modName, conn);
								if (errCode.trim().length() == 0) {
									sql = "select stop_business from customer where cust_code = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										stopBuss = rs.getString("stop_business");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if ("Y".equalsIgnoreCase(stopBuss)) {
										errCode = "VTICC";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

					}
					// state_code__notify
					else if (childNodeName.equalsIgnoreCase("state_code__notify")) {
						stateCodeNotify = checkNull((genericUtility.getColumnValue("state_code__notify", dom)));
						if (stateCodeNotify.trim().length() > 0) {
							if (!(isExist(conn, "state", "state_code", stateCodeNotify))) {
								errCode = "VMSTATE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"State code does not exists in master. Please enter valid state code.");
							}
						}
					}
					// stan_code__notify
					else if (childNodeName.equalsIgnoreCase("stan_code__notify")) {
						stanCodeNotify = checkNull((genericUtility.getColumnValue("stan_code__notify", dom)));
						if (stanCodeNotify.trim().length() > 0) {
							if (!(isExist(conn, "station", "stan_code", stanCodeNotify))) {
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("prom_date")) {
						Timestamp promDate = null;
						String prmDtStr = "", ordDtStr = "";
						prmDtStr = genericUtility.getColumnValue("prom_date", dom);
						ordDtStr = genericUtility.getColumnValue("order_date", dom);
						if (prmDtStr != null && prmDtStr.trim().length() > 0) {
							// promDate = Timestamp.valueOf((genericUtility.getColumnValue("prom_date",
							// dom))); comment by Abhijit
							promDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("prom_date", dom),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");// Added By Abhijit Gaikwad
							System.out.println("promDate[" + promDate + "]");
							// orderDate = Timestamp.valueOf((genericUtility.getColumnValue("order_date",
							// dom)));
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							System.out.println("orderDate[" + orderDate + "]");
						}
						if (((prmDtStr != null && prmDtStr.trim().length() > 0)
								&& (ordDtStr != null && ordDtStr.trim().length() > 0)) && promDate.before(orderDate)) {
							errCode = "VTPROM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					//added by manish mhatre on 15-oct-2019
					//start manish
					else if (childNodeName.equalsIgnoreCase("cust_code__dlv")) {

						custCode = genericUtility.getColumnValue("cust_code__dlv", dom);
						if(custCode!=null && custCode.trim().length()>0)
						{
							sql="select count(*) as cnt from customer where cust_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt("cnt");
							}
							if(cnt==0)
							{
								errCode = "VTCUSTDLV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// 007-sep-2020 manoharan close the statement and result set
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
					}
					//end manish

					if (childNodeName.equalsIgnoreCase("cctr_code__sal")) 
					{
						cctrCodeSal = checkNull(genericUtility.getColumnValue("cctr_code__sal", dom));
						acctCodeSal = checkNull(genericUtility.getColumnValue("acct_code__sal", dom));
						lsPosttype =  checkNull(finCommon.getFinparams("999999", "SALES_INV_POST_HDR", conn)); //Added by Dipesh p on[16/06/2020]
						System.out.println("lsPosttype::::::"+lsPosttype);
						if(cctrCodeSal != null && cctrCodeSal.trim().length() > 0)
						{
							errCode = finCommon.isCctrCode(acctCodeSal, cctrCodeSal," " , conn);
							if( errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Added by Dipesh p on[16/06/2020][Start]
						// 04-dec-2020 manoharan trim length should not be done
						//else if("H".equalsIgnoreCase(lsPosttype) && (cctrCodeSal == null || cctrCodeSal.trim().length() == 0))
						else if("H".equalsIgnoreCase(lsPosttype) && cctrCodeSal == null )
						{
							errCode = "VTCCTSAL02";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Added by Dipesh p on[16/06/2020][End]
					}
				} // end of else if loop
				// end of for
				break;// end of switch
				// case2
			case 2:
				System.out.println("---------------in detail2 validation------------------------");
				System.out.println("dom@@@@------->>" + genericUtility.serializeDom(dom));
				System.out.println("dom@@@@111------->>" + genericUtility.serializeDom(dom1));
				System.out.println("dom@@@@222------->>" + genericUtility.serializeDom(dom2));
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				String contractNoDet = "";

				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("item_code__ord")) {
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						if (itemCodeOrd != null && itemCodeOrd.trim().length() > 0) {
							cnt = 0;
							sql = "select count(*) from item where item_code= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}else{//Added by Anagha Rane 11/03/2020 To check item is active or not and add validation for the same Start

								sql = "select Active from item where item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									chkActive = rs.getString("Active");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("N".equalsIgnoreCase(chkActive)) {
									errCode = "VTITEM4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//Added by Anagha Rane 11/03/2020 To check item is active or not and add validation for the same End
						}
					} else if (childNodeName.equalsIgnoreCase("contract_no")) {
						contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
						if (contractNo != null && contractNo.trim().length() > 0) {
							contractNoDet = checkNull(genericUtility.getColumnValue("contract_no", dom));
							// contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
							if (contractNoDet == null || contractNoDet.trim().length() == 0) {
								errCode = "VCONERR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// site_code
					else if (childNodeName.equalsIgnoreCase("site_code")) {
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						if (!(isExist(conn, "site", "site_code", siteCode))) {
							errCode = "VTSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					// line_no__contr
					else if (childNodeName.equalsIgnoreCase("line_no__contr")) {
						//Changes by mayur on 17-May-2018--[start]
						lineNoContr = checkNull(genericUtility.getColumnValue("line_no__contr", dom));	//Changes by Pratiksha A on 09-03-21				
						//lineNoContr = genericUtility.getColumnValue("line_no__contr", dom);
						contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						//Changes by mayur on 17-May-2018--[end]
						
						//added by  Pratiksha A on 09-03-21-- end--
						System.out.println("line before["+ lineNoContr+"]");
						if(lineNoContr != null && lineNoContr.trim().length() > 0)
						{
							lineNoContr = getLineNewNo(lineNoContr);
						}	
						System.out.println("1820 line after["+ lineNoContr+"]");
						//added by  Pratiksha A on 09-03-21-- end--
						
						if (contractNo != null && contractNo.trim().length() > 0) {
							sql = "select count(*) as cnt from scontractdet where contract_no =? and line_no =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, contractNo);
							pstmt.setString(2, lineNoContr);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTCONTR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							if (lineNoContr != null && lineNoContr.trim().length() > 0) {
								errCode = "VTCONTR2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// item_code
					else if (childNodeName.equalsIgnoreCase("item_code")) {
						String itemFlag = "", ststeCodeDlv = "", controlledYn = "", custCodeDlv = "",
								lsExportOrderType = "", lsDisPobOrdtypeList = "", applyCustList = "";
						String noapplyCustList = "", lsApplicableOrdTypes = "", active = "", itemSerHdr = "",
								mothSer = "", lsItemSerCrPerc = "", lsItemSerCrpolicyHdr = "";
						String lsItemSerCrpolicy = "", lsStopBusiness = "";
						Timestamp drugLicnoUpto = null;
						boolean lbOrdFlg = false;
						int cntSer = 0;

						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemCode = itemCode == null ? "" : itemCode.trim();
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if (checkNull(genericUtility.getColumnValue("due_date", dom1)).trim().length() > 0) {
							dueDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("due_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						if (checkNull(genericUtility.getColumnValue("order_date", dom1)).trim().length() > 0) {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						// dueDate=Timestamp.valueOf(genericUtility.getColumnValue("due_date", dom1));
						// orderDate=Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom1));

						itemFlag = checkNull(genericUtility.getColumnValue("item_flg", dom));
						ststeCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
						countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));
						custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom1));
						stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));

						sql = "select controlled_yn   from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							controlledYn = rs.getString("controlled_yn");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("Y".equalsIgnoreCase(controlledYn)) {
							sql = "select drug_lic_no,drug_licno_upto FROM customer where cust_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCodeDlv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								drugLicNo = rs.getString("drug_lic_no");
								drugLicnoUpto = rs.getTimestamp("drug_licno_upto");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (drugLicNo != null && drugLicNo.trim().length() > 0) {
								if (drugLicnoUpto == null || drugLicnoUpto.before(dueDate)) {
									errCode = "VTDLNDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						sql = "select dis_link,channel_partner from customer where cust_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							disLink = rs.getString("dis_link");
							channelPartner = rs.getString("channel_partner");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("A".equalsIgnoreCase(disLink) && "Y".equalsIgnoreCase(channelPartner)) {
							custPord = checkNull(genericUtility.getColumnValue("cust_pord", dom1));

							if (custPord != null && custPord.trim().length() > 0) {
								sql = "select count(*) as cnt from porddet where purc_order = ? and	item_code  = ? and	status = 'O'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custPord);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									errCode = "VTPODET";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}

						//	if (errCode == null || errCode.trim().length() == 0) {Commented condition by nandkumar gadkai on 03/01/20
						cnt = 0;
						sql = "Select case when eou is null then 'N' else eou end as eou From site Where site_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							eou = rs.getString("eou");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("Y".equalsIgnoreCase(eou)) {
							lsExportOrderType = distCommon.getDisparams("999999", "EXPORT_DESPATCH_ORDER_TYPE",
									conn);
							orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
							if (lsExportOrderType != null && lsExportOrderType.trim().length() > 0
									&& lsExportOrderType.equalsIgnoreCase(orderType)) {
								sql = "select lop_reqd from itemser where item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lopReqd = rs.getString("lop_reqd");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("Y".equalsIgnoreCase(lopReqd)) {
									sql = "Select count(1) as cnt from lop_hdr a, lop_det b where a.lop_ref_no = b.lop_ref_no"
											+ " and a.site_code = ? and a.confirmed = 'Y' and b.item_code = ?"
											+ " and b.item_status ='A' and ? >= a.valid_from and ? <= a.valid_to and b.buy_sell_flag in ('S','B')";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, siteCode);
									pstmt.setString(2, itemCode);
									pstmt.setTimestamp(3, orderDate);
									pstmt.setTimestamp(4, orderDate);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt("cnt");
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
						//} Commented condition by nandkumar gadkai on 03/01/20
						orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
						//lsDisPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
						lsDisPobOrdtypeList = ""; //commentted and added by rupali on 02/04/2021
						
						lbOrdFlg = false;
						if (lsDisPobOrdtypeList != null && lsDisPobOrdtypeList.trim().length() > 0) 
						{
							String lsDisPobOrdtypeListArr[] = lsDisPobOrdtypeList.split(",");
							ArrayList<String> disPobOrdtypeList = new ArrayList<String>(Arrays.asList(lsDisPobOrdtypeListArr));
							//if (disPobOrdtypeList.contains(orderType)) { 
							for( String pobOrdType : disPobOrdtypeList )
							{
								if(checkNull(pobOrdType).equalsIgnoreCase(orderType))
								{
									lbOrdFlg = true;
								}
							}
						}
						//if (errCode == null || errCode.trim().length() == 0) {
						schAttr = checkNull(genericUtility.getColumnValue("sch_attr", dom));
						System.out.println("schAttr::[" + schAttr + "]");
						nature = checkNull(genericUtility.getColumnValue("nature", dom));
						//------Added by Jaffar S. on 29-01-19
						System.out.println("lbOrdFlg ----- "+lbOrdFlg);
						System.out.println("nature ----- "+nature);
						if(lbOrdFlg == true)
						{

						}
						else if (("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature)
								|| "S".equalsIgnoreCase(nature)) && !lbOrdFlg && (!"Y".equalsIgnoreCase(schAttr))) {
							sql = "select item_code__parent from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								itemCodeParent = checkNull(rs.getString("item_code__parent"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (itemCodeParent == null || itemCodeParent.trim().length() == 0) {
								errCode = "VTSCHITEM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						//}Commented condition by nandkumar gadkai on 03/01/20

						//	if (errCode == null || errCode.trim().length() == 0) {Commented condition by nandkumar gadkai on 03/01/20
						if ("B".equalsIgnoreCase(itemFlag) && (!lbOrdFlg)) {
							if (itemCode == null || itemCode.length() == 0) {
								System.out.println("VTITEM81");
								errCode = "VTITEM8";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								quantity = Double
										.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
								sql = "select count(1) as cnt from bom where bom_code =  ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									System.out.println("VTITEM82");
									errCode = "VTITEM8";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

								sql = "select app_from,	valid_upto,	apply_cust_list, noapply_cust_list,	order_type"
										+ " from scheme_applicability where scheme_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									appFrom = rs.getTimestamp("app_from");
									validUpto = rs.getTimestamp("valid_upto");
									applyCustList = checkNull(rs.getString("apply_cust_list"));
									noapplyCustList = checkNull(rs.getString("noapply_cust_list"));
									lsApplicableOrdTypes = checkNull(rs.getString("order_type"));
									if (dueDate.before(appFrom) || dueDate.after(validUpto)) {
										System.out.println("VTITEM83");
										errCode = "VTITEM8";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								} else {
									System.out.println("VTITEM84");
									errCode = "VTITEM8";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("NE".equalsIgnoreCase(orderType) && ((lsApplicableOrdTypes == null
										|| lsApplicableOrdTypes.trim().length() == 0))) {
									System.out.println("VTITEM85");
									errCode = "VTITEM8";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} else if (lsApplicableOrdTypes != null
										&& lsApplicableOrdTypes.trim().length() > 0) {
									lbProceed = false;
									String lsApplicableOrdTypesArr[] = lsApplicableOrdTypes.split(",");
									ArrayList<String> applicableOrdTypes = new ArrayList<String>(
											Arrays.asList(lsApplicableOrdTypesArr));
									if (applicableOrdTypes.contains(orderType.trim())) {
										lbProceed = true;
										// break;
									}
									if (!lbProceed) {
										System.out.println("VTITEM86");
										errCode = "VTITEM8";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

								if (applyCustList.trim().length() > 0) {
									lbProceed = false;
									String applyCustListArr[] = applyCustList.split(",");
									System.out.println("Custcode111" + custCode);
									ArrayList<String> ApplyCustList = new ArrayList<String>(
											Arrays.asList(applyCustListArr));
									if (ApplyCustList.contains(custCode.trim())) {
										lbProceed = true;
										//break;Commented  by nandkumar gadkai on 03/01/20
									}
									if (!lbProceed) {
										System.out.println("VTITEM87");
										errCode = "VTITEM8";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								if (noapplyCustList != null && noapplyCustList.trim().length() > 0) {
									lbProceed = true;
									String noapplyCustListArr[] = noapplyCustList.split(",");
									ArrayList<String> NoapplyCustListArr = new ArrayList<String>(
											Arrays.asList(noapplyCustListArr));
									if (NoapplyCustListArr.contains(custCode)) {
										lbProceed = false;
										//break;Commented  by nandkumar gadkai on 03/01/20
									}
									if (!lbProceed) {
										System.out.println("VTITEM88");
										errCode = "VTITEM8";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								nature = checkNull(genericUtility.getColumnValue("nature", dom));
								if ("C".equalsIgnoreCase(nature)) {
									sql = "Select count(1) as cnt From scheme_applicability A, bom b Where  A.scheme_code = b.bom_code And	 B.bom_code= ?"
											+ " And	 ? between case when b.min_qty is null then 0 else b.min_qty end"
											+ " And case when b.max_qty is null then 0 else b.max_qty end";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setDouble(2, quantity);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								} else {
									sql = "Select count(1) as cnt From scheme_applicability A, bom	b Where  A.scheme_code = b.bom_code"
											+ " And	 B.bom_code =?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								if (cnt == 0) {
									System.out.println("VTITEM89");
									errCode = "VTITEM8";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

								sql = "select count(*) as cnt from   scheme_applicability_det where  scheme_code = ? and site_code = ?"
										+ " and state_code is null and count_code is null";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, siteCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									sql = "select count(*) as cnt from scheme_applicability_det where scheme_code = ? and site_code is null and state_code = ?"
											+ " and count_code is null";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setString(2, stateCodeDlv);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt == 0) {
										sql = "select count(*) as cnt from scheme_applicability_det where scheme_code = ? and site_code is null"
												+ " and state_code is null and count_code = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, itemCode);
										// pstmt.setString(1, countCodeDlv); changed by Nasruddin khan 05-01-17
										pstmt.setString(2, countCodeDlv);

										rs = pstmt.executeQuery();
										if (rs.next()) {
											cnt = rs.getInt("cnt");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}

									if (cnt == 0) {
										errCode = "VTITEM6";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								sql = "select item_code from scheme_applicability where  scheme_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									itemCode = rs.getString("item_code");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								itemSer = distCommon.getItemSer(itemCode, siteCode, orderDate, custCode, "C", conn);

								//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][Start]
								String serSpecificCust = "";
								serSpecificCust = distCommon.getDisparams("999999", "SER_SPECIFIC_CUST", conn);
								if("Y".equalsIgnoreCase(serSpecificCust)) 
								{
									//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][End]
									sql = "select count(1) as cnt from customer_series where cust_code = ? and item_ser =?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, itemSer);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cntSer = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cntSer == 0 && cnt == 0) {
										errCode = "VTITEM7";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

						if ("I".equalsIgnoreCase(itemFlag)) {
							//removed validator. by Varsha V because its not required as ValidatorEJB is extended
							errCode = isItem(siteCode, itemCode, modName, conn);

							if (errCode != null && errCode.trim().length() > 0) {
								sql = "select active from item where item_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									active = checkNull(rs.getString("active"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("D".equalsIgnoreCase(active)) {
									errCode = "VTITMONALR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

							//if (errCode == null || errCode.trim().length() == 0) {Commented condition by nandkumar gadkai on 03/01/20
							itemSer = distCommon.getItemSer(itemCode, siteCode, orderDate, custCode, "C", conn);

							itemSerHdr = checkNullandTrim(genericUtility.getColumnValue("item_ser", dom1));
							sql = "select oth_series from itemser where item_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSerHdr);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mothSer = checkNullandTrim(rs.getString("oth_series"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (mothSer == null || mothSer.trim().length() == 0) {
								mothSer = "N";
							}
							itemSerProm = checkNullandTrim(genericUtility.getColumnValue("item_ser__prom", dom));
							sql = "select item_ser from item_credit_perc where item_code = ? and item_ser"
									+ " in ( select item_ser from customer_series where cust_code = ? and item_ser = item_credit_perc.item_ser)";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, custCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsItemSerCrPerc = checkNullandTrim(rs.getString("item_ser"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("itemSerHdr[" + itemSerHdr + "] itemSer[" + itemSer
									+ "] mothSer[" + mothSer + "] lsItemSerCrPerc[" + lsItemSerCrPerc
									+ "] itemSerProm[" + itemSerProm + "]");
							if (!itemSerHdr.equalsIgnoreCase(itemSer.trim()) && "N".equalsIgnoreCase(mothSer)
									&& !(lsItemSerCrPerc.equalsIgnoreCase(itemSerProm))) {
								errCode = "VTITEM2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!itemSerHdr.equalsIgnoreCase(itemSer.trim())
									&& "G".equalsIgnoreCase(mothSer)) {
								sql = "select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) as item_ser__crpolicy"
										+ " from itemser where  item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSerHdr);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsItemSerCrpolicyHdr = rs.getString("item_ser__crpolicy");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) as item_ser__crpolicy"
										+ " from itemser where  item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsItemSerCrpolicy = checkNullandTrim(rs.getString("item_ser__crpolicy"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!lsItemSerCrpolicy.equalsIgnoreCase(lsItemSerCrpolicyHdr)) {
									errCode = "VTITEM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//}Commented condition by nandkumar gadkai on 03/01/20
						}
						else// else condition added by nandkumar gadkari on 03/01/20 for scheme check item code ord ----------start----------------
						{
							itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
							errCode = isItem(siteCode, itemCodeOrd, modName, conn);

							if (errCode != null && errCode.trim().length() > 0) {
								sql = "select active from item where item_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									active = checkNull(rs.getString("active"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("D".equalsIgnoreCase(active)) {
									errCode = "VTITMONALR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

							//if (errCode == null || errCode.trim().length() == 0) {Commented condition by nandkumar gadkai on 03/01/20
							itemSer = distCommon.getItemSer(itemCodeOrd, siteCode, orderDate, custCode, "C", conn);

							itemSerHdr = checkNullandTrim(genericUtility.getColumnValue("item_ser", dom1));
							sql = "select oth_series from itemser where item_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSerHdr);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mothSer = checkNullandTrim(rs.getString("oth_series"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (mothSer == null || mothSer.trim().length() == 0) {
								mothSer = "N";
							}
							itemSerProm = checkNullandTrim(genericUtility.getColumnValue("item_ser__prom", dom));
							sql = "select item_ser from item_credit_perc where item_code = ? and item_ser"
									+ " in ( select item_ser from customer_series where cust_code = ? and item_ser = item_credit_perc.item_ser)";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							pstmt.setString(2, custCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsItemSerCrPerc = checkNullandTrim(rs.getString("item_ser"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("itemSerHdr[" + itemSerHdr + "] itemSer[" + itemSer
									+ "] mothSer[" + mothSer + "] lsItemSerCrPerc[" + lsItemSerCrPerc
									+ "] itemSerProm[" + itemSerProm + "]");
							if (!itemSerHdr.equalsIgnoreCase(itemSer.trim()) && "N".equalsIgnoreCase(mothSer)
									&& !(lsItemSerCrPerc.equalsIgnoreCase(itemSerProm))) {
								errCode = "VTITEM2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else if (!itemSerHdr.equalsIgnoreCase(itemSer.trim())
									&& "G".equalsIgnoreCase(mothSer)) {
								sql = "select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) as item_ser__crpolicy"
										+ " from itemser where  item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSerHdr);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsItemSerCrpolicyHdr = rs.getString("item_ser__crpolicy");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) as item_ser__crpolicy"
										+ " from itemser where  item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsItemSerCrpolicy = checkNullandTrim(rs.getString("item_ser__crpolicy"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!lsItemSerCrpolicy.equalsIgnoreCase(lsItemSerCrpolicyHdr)) {
									errCode = "VTITEM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//}Commented condition by nandkumar gadkai on 03/01/20

						}
						// else condition added by nandkumar gadkari on 03/01/20 for scheme check item code ord ----------end----------------
						sql = "select stop_business,  (case when sale_option is null then 'A' else sale_option end) as sale_option, contract_req"
								+ " from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsStopBusiness = checkNullandTrim(rs.getString("stop_business"));
							saleOptionItem = checkNullandTrim(rs.getString("sale_option"));
							contractReq = checkNullandTrim(rs.getString("contract_req"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("Y".equalsIgnoreCase(lsStopBusiness)) {
							errCode = "VTIIC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						sql = "select sales_option from customer where cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							saleOption = checkNullandTrim(rs.getString("sales_option"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("S".equalsIgnoreCase(saleOptionItem)) {
							contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
							if (contractNo == null
									|| contractNo.trim().length() == 0 && "Y".equalsIgnoreCase(contractReq)) {
								errCode = "VTRESITEM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								sql = "select count(*) as cnt from customeritem where cust_code = ? and item_code = ?"
										+ " and ((? between restrict_upto and valid_upto) or (restrict_upto is null and valid_upto is null))";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemCode);
								pstmt.setTimestamp(3, orderDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									errCode = "VTICI";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						//}Commented condition by nandkumar gadkai on 03/01/20
					}
					// quantity
					else if (childNodeName.equalsIgnoreCase("quantity")) {
						double mintQty = 0.00, mmOdQty = 0.00;
						String lsDisPobOrdtypeList = "";
						boolean lbOrdFlag = false;
						isContractYn = "N";

						quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						siteCodeShip = checkNull(genericUtility.getColumnValue("site_code", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						// orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom1));
						if (checkNull(genericUtility.getColumnValue("order_date", dom1)).trim().length() > 0) {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						if (quantity <= 0 || quantity == 0) {
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							sql = "select count(*) as cnt from customeritem where cust_code =? and item_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								sql = "select integral_qty, restrict_upto from customeritem where cust_code =? and item_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									mintQty = rs.getDouble("integral_qty");
									restUpto = rs.getTimestamp("restrict_upto");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (restUpto != null) {
									if (orderDate.before(restUpto)) {
										errCode = "VTRESDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

								if (minQty > 0) {
									sql = "Select mod(?,?) as mmOdQty from dual?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setDouble(1, quantity);
									pstmt.setDouble(2, minQty);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										mmOdQty = rs.getDouble("mmOdQty");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (mmOdQty > 0) {
										errCode = "VTINTQTY";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							} else if (cnt == 0) {
								if (siteCodeShip != null) {
									minQty = distCommon.getIntegralQty("", itemCode, siteCodeShip, conn);
								} else {
									minQty = distCommon.getIntegralQty("", itemCode, siteCode, conn);
								}

								if (minQty > 0) {
									nature = checkNullandTrim(genericUtility.getColumnValue("nature", dom));
									System.out.println("Nature" + nature);

									if ("C".equalsIgnoreCase(nature)) {
										if ((CommonConstants.DB_NAME).equalsIgnoreCase("db2")) {
											if (quantity < minQty || (quantity % minQty) > 0) {
												errCode = "VTINTQTY1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										} else {
											sql = "Select mod(?,?) as mmOdQty from dual";
											pstmt = conn.prepareStatement(sql);
											pstmt.setDouble(1, quantity);
											pstmt.setDouble(2, minQty);
											rs = pstmt.executeQuery();
											if (rs.next()) {
												mmOdQty = rs.getDouble("mmOdQty");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;

											if (quantity < minQty || mmOdQty > 0) {
												errCode = "VTINTQTY1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
							}
							orderType = checkNullandTrim(genericUtility.getColumnValue("order_type", dom1));
							//lsDisPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
							lsDisPobOrdtypeList = ""; //commentted and added by rupali on 02/04/2021
							lbOrdFlag = false;
							if (lsDisPobOrdtypeList != null && lsDisPobOrdtypeList.trim().length() > 0) 
							{
								String lsDisPobOrdtypeListArr[] = lsDisPobOrdtypeList.split(",");
								ArrayList<String> disPobOrdtypeList = new ArrayList<String>(Arrays.asList(lsDisPobOrdtypeListArr));
								//for (int i = 0; i < lsDisPobOrdtypeListArr.length; i++) {  
								//if (orderType.equalsIgnoreCase(lsDisPobOrdtypeListArr[i])) {
								for( String pobOrdType : disPobOrdtypeList ) {
									if(checkNull(pobOrdType).equalsIgnoreCase(orderType))	{									
										lbOrdFlag = true;
									}
								}
							}
							System.out.println("lbOrdFlag[" + lbOrdFlag + "]");
							System.out.println("errCode<<<<<<" + errCode + "]" + "schemeCode" + schemeCode + "]"
									+ "nature" + nature + "]");

							if (errCode == null || errCode.trim().length() == 0) {
								nature = checkNullandTrim(genericUtility.getColumnValue("nature", dom));
								// NANDKUMAR GADKARI----------------------------------------START ------------------
								discount = Double.parseDouble(checkDouble(genericUtility.getColumnValue("discount", dom)));

								System.out.println(" after get nature" + nature + "]");
								System.out.println(" after get discount" + discount + "]");

								if (("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature)
										|| "S".equalsIgnoreCase(nature)) || (schAttr.trim().equalsIgnoreCase("Y") && discount > 0) && (!lbOrdFlag)) {
									schemeCode = getSchemeCode(dom, dom1, dom2, "quantity", conn);

									//------Commented and added alias to get proper data by Jaffar S on 07-02-2019
									//sql = "select CASE when scheme_flag is null then 'Q' ELSE scheme_flag END from bom where bom_code = ?";
									sql = "select CASE when scheme_flag is null then 'Q' ELSE scheme_flag END as scheme_flag from bom where bom_code = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										schemeFlag = checkNullandTrim(rs.getString("scheme_flag"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									orderType = checkNullandTrim(genericUtility.getColumnValue("order_type", dom1));
									if ("Q".equalsIgnoreCase(schemeFlag)) {
										System.out.println("Insude");
										if(!"PB".equalsIgnoreCase(orderType))//For generating POB sales order transaction
										{
											errCode = valDataGroupScheme(dom, dom1, dom2, "quantity", objContext, editFlag,
													nature, conn);
											System.out.println("Quantity AFTER errCode1111111" + errCode + "]");
											if (errCode != null && errCode.trim().length() > 0) {
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									} else {
										System.out.println("Insude else");
										if(!"PB".equalsIgnoreCase(orderType))
										{
											errCode = valDataGroupScheme(dom, dom1, dom2, "quantity", objContext, editFlag,
													nature, conn);
											System.out.println("Value AFTER errCode1111111" + errCode + "]");
											if (errCode != null && errCode.trim().length() > 0) {
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
							}
							if ("Y".equalsIgnoreCase(isContractYn)) {
								contractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
								if (contractNo != null && contractNo.trim().length() > 0) {
									errCode = gbfScItemQtyVal(itemCode, contractNo, conn);
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}

					}
					//	---------------------------- Nandkumar Gadkari -------------on 03/10/18---------start--------------------
					else if (childNodeName.equalsIgnoreCase("nature")) {

						//	-------------------------------------------------------------------------------------------

						double  totFreeQty = 0,	unConfTotFreeQty = 0 , freeQty = 0,	prvFreeQty = 0,  rate1 = 0,qty=0.0,mRate = 0.00,value=0.0,valueAmount=0.0;
						String  lineNo = "", browItemCode = "",	currLineNo = "",ldtDateStr = "",lsPriceList = "",retlSchmRateBase="",lsUnit="",lsListType="",lsRefNo="",schemeCode1="";
						Timestamp  ldtPlDate = null,ldPlistDate = null;
						int llPlcount=0;

						nature = checkNullandTrim(genericUtility.getColumnValue("nature", dom));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom2));
						if(saleOrder.trim().length() == 0)
						{
							saleOrder=" ";
						}
						freeQty=0.0;
						qty = checkDoubleNull(genericUtility.getColumnValue("quantity", dom));
						System.out.println("inside scheme balance..............." +nature);
						System.out.println("itemCodeOrd==" + itemCodeOrd);
						System.out.println("siteCode==" + siteCode);
						System.out.println("custCode==" + custCode);
						System.out.println("quantity==" + qty);
						System.out.println("orderDate==" + orderDate);



						if ("V".equalsIgnoreCase(nature.trim()))
						{	
							retlSchmRateBase = checkNullandTrim(distCommon.getDisparams( "999999", "RETL_SCHM_RATE_BASE", conn ));	


							//----------------------------------------------------
							if (genericUtility.getColumnValue("pl_date", dom1) != null
									&& genericUtility.getColumnValue("pl_date", dom1).trim().length() > 0) {
								ldtPlDate = Timestamp
										.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
												genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

							}
							if (ldtPlDate != null) {

								ldtDateStr = genericUtility.getColumnValue("order_date", dom1);

							} else {

								ldtDateStr = genericUtility.getColumnValue("pl_date", dom1);
							}
							ldPlistDate = orderDate;

							if("M".equalsIgnoreCase(retlSchmRateBase))
							{
								lsPriceList = checkNullandTrim(distCommon.getDisparams( "999999", "MRP", conn ));
								lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));
								lsListType = distCommon.getPriceListType(lsPriceList, conn);

								sql = "select count(1)  as llPlcount from pricelist where price_list=?"
										+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
										+ " and (ref_no is not null)";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsPriceList);
								pstmt.setString(2, itemCodeOrd);
								pstmt.setString(3, lsUnit);
								pstmt.setString(4, lsListType);
								pstmt.setTimestamp(5, orderDate);
								pstmt.setTimestamp(6, orderDate);
								pstmt.setDouble(7, qty);
								pstmt.setDouble(8, qty);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									llPlcount = rs.getInt("llPlcount");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (llPlcount >= 1) {
									sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
											+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, lsPriceList);
									pstmt.setString(2, itemCodeOrd);
									pstmt.setString(3, lsUnit);
									pstmt.setString(4, lsListType);
									pstmt.setTimestamp(5, orderDate);
									pstmt.setTimestamp(6, orderDate);
									pstmt.setDouble(7, qty);
									pstmt.setDouble(8, qty);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										lsRefNo = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCodeOrd, lsRefNo, lsListType, qty,
											conn);
								}
								if (mRate <= 0) {
									/*mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCodeOrd, lsRefNo, "L", qty,
											conn);*/
									mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCodeOrd, "", "L", qty, conn);
								}

							}
							else
							{
								lsPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
								if (lsPriceList != null || lsPriceList.trim().length() > 0)
								{
									mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCodeOrd, "", "L", qty, conn);
									System.out.print("mRate gbfICquantity++++++++" + mRate);
									System.out.print("mqty++++++++" + qty);
								}

							}

							valueAmount= qty * mRate;

							//---------------------------------------------------



							sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
									+ " AND EFF_FROM <= ? AND VALID_UPTO >=?  AND CUST_CODE = ?  AND ITEM_CODE= ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, orderDate);
							pstmt.setTimestamp(2, orderDate);
							pstmt.setString(3,custCode);
							pstmt.setString(4,"X");
							rs1 = pstmt.executeQuery();

							if (rs1.next()) 
							{
								freeQty = rs1.getDouble(1);
							}
							rs1.close();
							rs1 = null;
							pstmt.close();
							pstmt = null;
							if(freeQty > 0)
							{
								sql = " select  a.price_list,a.pl_date,b.quantity , a.order_date,b.unit " +								
										" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
										+ " and a.cust_code = ?  and a.sale_order <> ? and a.order_date between ? and ?"
										+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('V')";

								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, siteCode);
								pstmt1.setString(2, custCode);
								pstmt1.setString(3, saleOrder);
								pstmt1.setTimestamp(4, orderDate);
								pstmt1.setTimestamp(5, orderDate);
								rs1 = pstmt1.executeQuery();
								while (rs1.next()) {

									lsPriceList = rs1.getString(1);
									ldtPlDate = rs1.getTimestamp(2);
									qty = rs1.getDouble(3);
									orderDate = rs1.getTimestamp(4);
									lsUnit = rs1.getString(5);



									if (ldtPlDate != null) {

										ldtDateStr = sdf.format(orderDate);

									} else {

										ldtDateStr = sdf.format(ldtPlDate);
									}
									ldPlistDate = orderDate;


									if("M".equalsIgnoreCase(retlSchmRateBase))
									{
										lsPriceList = checkNullandTrim(distCommon.getDisparams( "999999", "MRP", conn ));
										lsListType = distCommon.getPriceListType(lsPriceList, conn);

										sql = "select count(1)  as llPlcount from pricelist where price_list=?"
												+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
												+ " and (ref_no is not null)";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, lsPriceList);
										pstmt.setString(2, itemCodeOrd);
										pstmt.setString(3, lsUnit);
										pstmt.setString(4, lsListType);
										pstmt.setTimestamp(5, orderDate);
										pstmt.setTimestamp(6, orderDate);
										pstmt.setDouble(7, qty);
										pstmt.setDouble(8, qty);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											llPlcount = rs.getInt("llPlcount");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if (llPlcount >= 1) {
											sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
													+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, lsPriceList);
											pstmt.setString(2, itemCodeOrd);
											pstmt.setString(3, lsUnit);
											pstmt.setString(4, lsListType);
											pstmt.setTimestamp(5, orderDate);
											pstmt.setTimestamp(6, orderDate);
											pstmt.setDouble(7, qty);
											pstmt.setDouble(8, qty);
											rs = pstmt.executeQuery();
											if (rs.next()) {
												lsRefNo = rs.getString(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;

											mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCodeOrd, lsRefNo, lsListType, qty,
													conn);
										}
										if (mRate <= 0) {
											mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCodeOrd, "", "L", qty, conn);
										}
									}
									else
									{

										if (lsPriceList != null || lsPriceList.trim().length() > 0)
										{
											mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCodeOrd, "", "L", qty, conn);
											System.out.print("mRate gbfICquantity++++++++" + mRate);
											System.out.print("mqty++++++++" + qty);
										}

									}
									value= qty * mRate;
									unConfTotFreeQty= unConfTotFreeQty + value;
									System.out.println("unConfTotFreeQty separte free" + lsPriceList +ldtPlDate+ qty + unConfTotFreeQty + mRate);


								}
								pstmt1.close();
								rs1.close();
								pstmt1 = null;
								rs1 = null;

								//-----------------------------

								//---------------------------
								Node currDetail1 = null;
								prvFreeQty = 0;
								int count = 0;
								currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
								NodeList detailList1 = dom2.getElementsByTagName("Detail2");


								int noOfDetails = detailList1.getLength();

								lsPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
								orderDate = Timestamp.valueOf(
										genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
												genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
										+ " 00:00:00.0");
								if (genericUtility.getColumnValue("pl_date", dom1) != null
										&& genericUtility.getColumnValue("pl_date", dom1).trim().length() > 0) {
									ldtPlDate = Timestamp
											.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
													genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

								}
								if (ldtPlDate != null) {

									ldtDateStr = genericUtility.getColumnValue("order_date", dom1);

								} else {

									ldtDateStr = genericUtility.getColumnValue("pl_date", dom1);
								}
								ldPlistDate = orderDate;
								for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

									currDetail1 = detailList1.item(ctr1);

									lineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", currDetail1));
									nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
									browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
									quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("quantity", currDetail1));
									lsUnit = checkNullandTrim(genericUtility.getColumnValueFromNode("unit", currDetail1));
									System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);

									System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");

									if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
										System.out.println("Insideif00000forSCHEME_BALANCE ");
										if (nature.equals("V")) {

											if("M".equalsIgnoreCase(retlSchmRateBase))
											{
												lsPriceList = checkNullandTrim(distCommon.getDisparams( "999999", "MRP", conn ));
												lsListType = distCommon.getPriceListType(lsPriceList, conn);

												sql = "select count(1)  as llPlcount from pricelist where price_list=?"
														+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
														+ " and (ref_no is not null)";
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, lsPriceList);
												pstmt.setString(2, browItemCode);
												pstmt.setString(3, lsUnit);
												pstmt.setString(4, lsListType);
												pstmt.setTimestamp(5, orderDate);
												pstmt.setTimestamp(6, orderDate);
												pstmt.setDouble(7, quantity);
												pstmt.setDouble(8, quantity);
												rs = pstmt.executeQuery();
												if (rs.next()) {
													llPlcount = rs.getInt("llPlcount");
												}
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;

												if (llPlcount >= 1) {
													sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
															+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, lsPriceList);
													pstmt.setString(2, browItemCode);
													pstmt.setString(3, lsUnit);
													pstmt.setString(4, lsListType);
													pstmt.setTimestamp(5, orderDate);
													pstmt.setTimestamp(6, orderDate);
													pstmt.setDouble(7, quantity);
													pstmt.setDouble(8, quantity);
													rs = pstmt.executeQuery();
													if (rs.next()) {
														lsRefNo = rs.getString(1);
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;

													mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, browItemCode, lsRefNo, lsListType, quantity,
															conn);
												}
												if (mRate <= 0) {
													mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCodeOrd, "", "L", qty, conn);
												}
											}
											else
											{

												if (lsPriceList != null || lsPriceList.trim().length() > 0)
												{
													mRate = distCommon.pickRate(lsPriceList, ldtDateStr, browItemCode, "", "L", quantity, conn);
													System.out.print("mRate gbfICquantity++++++++" + mRate);
													System.out.print("mqty++++++++" + quantity);
												}

											}
											value =quantity * mRate;
											prvFreeQty =prvFreeQty + value;
										}
										System.out.println(
												"prvFreeQty insdie V[" + prvFreeQty+ "]");

									}	
								}	
								System.out.println(
										"valueAmount insdie V[" + valueAmount+ "]"+unConfTotFreeQty + prvFreeQty);
								if ((valueAmount +unConfTotFreeQty + prvFreeQty ) > freeQty) {
									errCode = "VTFREEQTY1";// Entered free quantity is
									// greater than scheme's free
									// quantity
									System.out.println("before ....errList.." + errList);
									errList.add(errCode);
									System.out.println("after ....errList.." + errList);
									System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
									errFields.add(checkNull(" ".toLowerCase()));
									// errFields.add(checkNull(childNodeName.toLowerCase()));
									System.out.println(
											"Entered free quantity is greater than sCHEME_BALANCE quantity" + errFields);
								}

							}
							else
							{
								errCode = "VTQTYSCBAL";// Entered free quantity is
								// greater than scheme's free
								// quantity
								System.out.println("before ....errList.." + errList);
								errList.add(errCode);
								System.out.println("after ....errList.." + errList);
								System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
								errFields.add(checkNull(" ".toLowerCase()));
								// errFields.add(checkNull(childNodeName.toLowerCase()));
								System.out.println(
										"Entered free quantity is greater than sCHEME_BALANCE quantity" + errFields);
							}

						}
						if ("I".equalsIgnoreCase(nature.trim()))
						{
							sql = " SELECT BALANCE_FREE_QTY - USED_FREE_QTY  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_QTY - USED_FREE_QTY > 0 "
									+ " AND EFF_FROM <= ? AND VALID_UPTO >=? AND CUST_CODE = ?  AND ITEM_CODE= ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, orderDate);
							pstmt.setTimestamp(2, orderDate);
							pstmt.setString(3,custCode);
							pstmt.setString(4,itemCodeOrd);
							rs1 = pstmt.executeQuery();

							if (rs1.next()) 
							{
								freeQty = rs1.getDouble(1);
							}
							rs1.close();
							rs1 = null;
							pstmt.close();
							pstmt = null;
							if(freeQty > 0)
							{
								sql = " select sum(case when nature ='I' then quantity else 0 end) as unconfirmFreeQty " +								
										" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
										+ " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
										+ "	and b.item_code__ord = ?"
										+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('I')";

								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, siteCode);
								pstmt1.setString(2, custCode);
								pstmt1.setString(3, saleOrder);
								pstmt1.setTimestamp(4, orderDate);
								pstmt1.setTimestamp(5, orderDate);
								pstmt1.setString(6, itemCodeOrd);
								rs1 = pstmt1.executeQuery();
								if (rs1.next()) {

									unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
									System.out.println("unConfTotFreeQty separte free" + unConfTotFreeQty);

								}
								pstmt1.close();
								rs1.close();
								pstmt1 = null;
								rs1 = null;

								//---------------------------
								Node currDetail1 = null;
								prvFreeQty = 0;
								int count = 0;
								currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
								NodeList detailList1 = dom2.getElementsByTagName("Detail2");

								int noOfDetails = detailList1.getLength();

								for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

									currDetail1 = detailList1.item(ctr1);

									lineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", currDetail1));
									nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
									browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
									quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("quantity", currDetail1));

									System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);

									System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");

									if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
										System.out.println("Insideif00000forSCHEME_BALANCE ");
										if (nature.equals("I") && (browItemCode.equalsIgnoreCase(itemCodeOrd))) {
											prvFreeQty = prvFreeQty + quantity;

										}
										System.out.println(
												"prvFreeQty insdie V[" + prvFreeQty+ "]");

									}	
								}	

								if ((qty +unConfTotFreeQty + prvFreeQty ) > freeQty) {
									errCode = "VTFREEQTY1";// Entered free quantity is
									// greater than scheme's free
									// quantity
									System.out.println("before ....errList.." + errList);
									errList.add(errCode);
									System.out.println("after ....errList.." + errList);
									System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
									errFields.add(checkNull(" ".toLowerCase()));
									// errFields.add(checkNull(childNodeName.toLowerCase()));
									System.out.println(
											"Entered free quantity is greater than sCHEME_BALANCE quantity" + errFields);
								}
							}
							else
							{
								errCode = "VTQTYSCBAL";// Entered free quantity is
								// greater than scheme's free
								// quantity
								System.out.println("before ....errList.." + errList);
								errList.add(errCode);
								System.out.println("after ....errList.." + errList);
								System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
								errFields.add(checkNull(" ".toLowerCase()));
								// errFields.add(checkNull(childNodeName.toLowerCase()));
								System.out.println(
										"Entered free quantity is greater than sCHEME_BALANCE quantity" + errFields);
							}



						}
						//added by nandkumar gadkari on 28/05/19-----------------------start------------------------------
						if ("P".equalsIgnoreCase(nature.trim()))
						{
							int cnt1=0;
							double reqPoints=0,freePoints=0,unconfreqPoints=0,unConfFreeQty=0,unConfTotFreePoints=0,prvFreePoints=0,currentPoints=0;
							stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
							countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));

							if (itemCodeOrd.trim().length() > 0) {
								if (orderType == null) {
									orderType = "";
								}
								if (siteCode == null) {
									siteCode = "";
								}
								if (stateCodeDlv == null) {
									stateCodeDlv = "";
								}
								if (countCodeDlv == null) {
									countCodeDlv = "";
								}
								sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code= b.scheme_code  and a.app_from <= ? and a.valid_upto>= ? and (b.site_code= ? or b.state_code = ?  or b.count_code= ?) and PROD_SCH = ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setTimestamp(1, orderDate);
								pstmt1.setTimestamp(2, orderDate);
								pstmt1.setString(3, siteCode);
								pstmt1.setString(4, stateCodeDlv);
								pstmt1.setString(5, countCodeDlv);
								pstmt1.setString(6, "Y");
								rs1 = pstmt1.executeQuery();
								while (rs1.next()) {
									schemeCode1 = rs1.getString("scheme_code");

									if(schemeCode1 !=null && schemeCode1.trim().length() > 0)
									{
										sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, schemeCode1);
										pstmt.setString(2, itemCodeOrd);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt = rs.getInt("cnt");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if(cnt==0)
										{
											continue;
										}
										if (cnt > 1 )
										{
											errCode = "VTITEM10";
											errList.add(errCode);
											errFields.add(checkNull(" ".toLowerCase()));
										}
										if(cnt== 1)
										{
											cnt1++;
											schemeCode=schemeCode1;
										}


									}		

								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1= null;
								if (cnt1 == 0) {
									errCode = "VTINFEEQTY";// Scheme is not applicable for the
									errList.add(errCode);
									System.out.println("errList" + errList + "]");
									errFields.add(checkNull(" ".toLowerCase()));
									System.out.println("invalid free quantity for this item code ");
								}

								sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									reqPoints = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(reqPoints > 0)
								{
									currentPoints = qty * reqPoints;
								}

								sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
										+ "  AND CUST_CODE = ?  AND ITEM_CODE= ?   AND EFF_FROM <= ? AND VALID_UPTO >=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,"X");
								pstmt.setTimestamp(3, orderDate);
								pstmt.setTimestamp(4, orderDate);
								rs = pstmt.executeQuery();

								if (rs.next()) 
								{
									freePoints = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(freePoints > 0)
								{
									sql = " select b.item_code__ord ,b.quantity " +								
											" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
											+ " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
											+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('P')";

									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, siteCode);
									pstmt1.setString(2, custCode);
									pstmt1.setString(3, saleOrder);
									pstmt1.setTimestamp(4, orderDate);
									pstmt1.setTimestamp(5, orderDate);

									rs1 = pstmt1.executeQuery();
									while (rs1.next()) {
										itemCode=rs1.getString(1);
										unConfFreeQty = rs1.getDouble(2);
										System.out.println("unConfFreeQty" + unConfFreeQty);
										sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, schemeCode);
										pstmt.setString(2, itemCode);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt = rs.getInt("cnt");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if(cnt > 0)
										{
											sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, schemeCode);
											pstmt.setString(2, itemCode);
											rs = pstmt.executeQuery();
											if (rs.next()) 
											{
												unconfreqPoints = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;


											unConfTotFreePoints = unConfTotFreePoints + unConfFreeQty * unconfreqPoints;

										}

									}
									pstmt1.close();
									rs1.close();
									pstmt1 = null;
									rs1 = null;
									System.out.println("unConfTotFreePoints" + unConfTotFreePoints+ "]");
									//---------------------------
									Node currDetail1 = null;
									prvFreeQty = 0;
									int count = 0;
									currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
									NodeList detailList1 = dom2.getElementsByTagName("Detail2");

									int noOfDetails = detailList1.getLength();

									for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

										currDetail1 = detailList1.item(ctr1);

										lineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", currDetail1));
										nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
										browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code__ord", currDetail1));
										quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("quantity", currDetail1));

										System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);

										System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");

										if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
											System.out.println("Insideif00000forSCHEME_BALANCE ");
											if (nature.equals("P")) {

												sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, schemeCode);
												pstmt.setString(2, browItemCode);
												rs = pstmt.executeQuery();
												if (rs.next()) 
												{
													cnt = rs.getInt("cnt");
												}
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;

												if(cnt > 0)
												{
													sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, schemeCode);
													pstmt.setString(2, browItemCode);
													rs = pstmt.executeQuery();
													if (rs.next()) 
													{
														unconfreqPoints = rs.getDouble(1);
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;


													prvFreePoints = prvFreePoints + quantity * unconfreqPoints;

												}


											}
											System.out.println("prvFreePoints insdie P[" + prvFreePoints+ "]");

										}	
									}	

									if ((currentPoints +unConfTotFreePoints + prvFreePoints ) > freePoints) {
										errCode = "VTFREEQTY1";// Entered free POINTS is
										// greater than scheme's free
										// POINTS
										System.out.println("before ....errList.." + errList);
										errList.add(errCode);
										System.out.println("after ....errList.." + errList);
										System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
										errFields.add(checkNull(" ".toLowerCase()));
										// errFields.add(checkNull(childNodeName.toLowerCase()));
										System.out.println(
												"Entered free quantity is POINTS than sCHEME_BALANCE POINTS" + errFields);
									}
								}	
								else
								{
									errCode = "VTQTYSCBAL";// Entered free POINTS is
									// greater than scheme's free
									// POINTS
									System.out.println("before ....errList.." + errList);
									errList.add(errCode);
									System.out.println("after ....errList.." + errList);
									System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
									errFields.add(checkNull(" ".toLowerCase()));
									// errFields.add(checkNull(childNodeName.toLowerCase()));
									System.out.println(
											"Entered free POINTS is greater than sCHEME_BALANCE POINTS" + errFields);
								}

								// added by nandkumar gadkari on 21/09/19----------------------start-----------------
								schemeStkChk = distCommon.getDisparams( "999999", "SCHEME_STOCK_CHECK", conn );
								if(schemeStkChk==null || schemeStkChk.trim().length() ==0 || schemeStkChk.equalsIgnoreCase("NULLFOUND"))
								{
									schemeStkChk="Y";
								}
								if("Y".equalsIgnoreCase(schemeStkChk.trim()))
								{
									sql="SELECT SUM(a.QUANTITY - a.ALLOC_QTY - CASE WHEN a.HOLD_QTY IS NULL THEN 0 ELSE a.HOLD_QTY END ) AVAIL_QTY "
											+" FROM STOCK A, "
											+"LOCATION B, "
											+"INVSTAT C "
											+"WHERE A.LOC_CODE = B.LOC_CODE "
											+"AND B.INV_STAT = C.INV_STAT "
											+"AND A.ITEM_CODE = ? "
											+"AND A.SITE_CODE = ? "
											+"AND C.AVAILABLE = 'Y' "
											+"AND C.STAT_TYPE = 'M' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCodeOrd);
									pstmt.setString(2, siteCode);

									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										availQty = rs.getDouble(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if(availQty <= 0)
									{
										errCode = "VTWOIN01";
										errList.add(errCode);
										errFields.add(checkNull(" ".toLowerCase()));
									}
								}

								// added by nandkumar gadkari on 21/09/19----------------------end-----------------

							}


						}
						//added by nandkumar gadkari on 28/05/19-----------------------end------------------------------
					}
					//					---------------------------- Nandkumar Gadkari -------------on 03/10/18----------end----------------------
					// unit
					else if (childNodeName.equalsIgnoreCase("unit")) {
						unit = checkNullandTrim(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNullandTrim(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNullandTrim(genericUtility.getColumnValue("item_code", dom));

						if (!(isExist(conn, "uom", "unit", unit))) {
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("The unit code you have entered is not existing in the unit master.");
						} else if (unit.equalsIgnoreCase(unitStd))// changes by mayur on 26/10/17
						{
							convQtyStduom = checkIntNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
							System.out.println("MANISH convQtyStduom-------->>[" + convQtyStduom + "]");
							if (convQtyStduom != 1) {
								errCode = "VTUCON1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Conversion factor entered is invalid.");
							}
						} else if (!unit.equalsIgnoreCase(unitStd)) {
							sql = "select count(*) as cnt from uomconv where (item_code = ? or item_code = 'X') and ((unit__fr = ? and unit__to = ?)"
									+ " or (unit__to = ? and unit__fr = ?)) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, unit);
							pstmt.setString(3, unitStd);
							pstmt.setString(4, unit);
							pstmt.setString(5, unitStd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTUOMCONV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					// unit__rate
					else if (childNodeName.equalsIgnoreCase("unit__rate")) {
						unitRate = checkNullandTrim(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNullandTrim(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNullandTrim(genericUtility.getColumnValue("item_code", dom));
						if (!(isExist(conn, "uom", "unit", unitRate))) {
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("The unit code you have entered is not existing in the unit master.");
						} else if (unitRate.equalsIgnoreCase(unitStd)) {
							convRtuomStduom = Double
									.parseDouble(checkDouble(genericUtility.getColumnValue("conv__rtuom_stduom", dom)));
							if (convRtuomStduom != 1) {
								errCode = "VTUCON1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// Changed by Santosh on 24/03/2017
						// else if(!unitRate.equalsIgnoreCase(unitStd))
						else if (!(unitRate.trim()).equalsIgnoreCase(unitStd.trim())) {
							sql = "select count(*) as cnt from uomconv where (item_code = ? or item_code = 'X') and ((unit__fr = ? and unit__to = ?)"
									+ " or (unit__to = ? and unit__fr = ?)) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, unit);
							pstmt.setString(3, unitStd);
							pstmt.setString(4, unit);
							pstmt.setString(5, unitStd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTUNIT2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("rate")) {
						Timestamp ldtDate = null;
						double priceListD = 0.00, lcMinRate = 0.00, lcMaxRate = 0.00, mRate = 0.00;
						String lsListType = "";

						rate = Double.parseDouble(checkDouble(genericUtility.getColumnValue("rate", dom)));

						if (rate < 0) //add condition by kailasG on 18 june 2019
						{								
							errCode = "VTNERATE";	
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						

						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
						if (checkNull(genericUtility.getColumnValue("order_date", dom1)).trim().length() > 0) {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						// orderDate=Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom1));
						if (genericUtility.getColumnValue("quantity", dom) != null
								&& genericUtility.getColumnValue("quantity", dom).trim().length() > 0) {
							quantity = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
						}
						nature = checkNullandTrim(genericUtility.getColumnValue("nature", dom));
						if (checkNull(genericUtility.getColumnValue("pl_date", dom1)).trim().length() > 0) {
							plDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						// plDate=Timestamp.valueOf(genericUtility.getColumnValue("pl_date", dom1));

						if (plDate != null) {
							ldtDate = plDate;
						} else {
							ldtDate = orderDate;
						}

						//if (quantity == 0) {
						if ("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature)
								|| "S".equalsIgnoreCase(nature)
								|| "I".equalsIgnoreCase(nature)  || "V".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature))// ADDED BY NANDKUMAR GADKARI ON 17/09/19 
						{
							if (rate > 0) {
								errCode = "VTFREEITEM";

								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else {
							if ("I".equalsIgnoreCase(itemFlg)) {
								/*
								 * if(priceList==null || priceList.trim().length()==0) { //ls_price_list =
								 * lvo_dist.gbf_pick_pricelist(ls_price_list,ldt_date,ls_item_code,'','L',
								 * lc_qty) priceListD=distCommon.pickRate(priceList,ldtDate.toString() ,
								 * itemCode,"","L",quantity, conn); }
								 */
								if (priceList != null && priceList.trim().length() > 0) {
									lsListType = checkNullandTrim(distCommon.getPriceListType(priceList, conn));
								}
								if ((priceList != null && priceList.length() > 0)
										&& !"B".equalsIgnoreCase(lsListType)) {
									sql = "select max(case when min_rate is null then 0 else min_rate end) as lc_min_rate,"
											+ "max(case when max_rate is null then 0 else max_rate end)as lc_max_rate"
											+ " from pricelist where price_list = ?"
											+ " and item_code = ? and list_type = ? and eff_from <= ?"
											+ " and valid_upto >= ? and min_qty <= ? and max_qty >=?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, priceList);
									pstmt.setString(2, itemCode);
									pstmt.setString(3, lsListType);
									pstmt.setTimestamp(4, ldtDate);
									pstmt.setTimestamp(5, ldtDate);
									pstmt.setDouble(6, quantity);
									pstmt.setDouble(7, quantity);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										lcMinRate = rs.getDouble("lc_min_rate");
										lcMaxRate = rs.getDouble("lc_max_rate");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (rate < lcMinRate && ( !"B".equalsIgnoreCase(lsListType) && ! "I".equalsIgnoreCase(lsListType))) {
										errCode = "VTRATE3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										//System.out.println("There are no contracts pending for release,as per the specified parameters");
										System.out.println("Rate is less Than MinRate of Pricelist["+rate+"]<["+lcMinRate+"]");
									}
									if (rate > lcMaxRate && ( !"B".equalsIgnoreCase(lsListType) && ! "I".equalsIgnoreCase(lsListType))) {
										errCode = "VTRATE8";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										//System.out.println("There are no contracts pending for release,as per the specified parameters");											
										System.out.println("Rate is less Than MinRate of Pricelist["+rate+"]<["+lcMaxRate+"]");
									}
								}
							}																								
							//Pavan Rane 01aug19 Start[change in rate validation]
							//String lsDisPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
							String lsDisPobOrdtypeList = ""; //commentted and added by rupali on 02/04/2021
							orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
							boolean lbOrdFlg = false;
							if (lsDisPobOrdtypeList != null && lsDisPobOrdtypeList.trim().length() > 0) {
								String lsDisPobOrdtypeListArr[] = lsDisPobOrdtypeList.split(",");
								ArrayList<String> disPobOrdtypeList = new ArrayList<String>(Arrays.asList(lsDisPobOrdtypeListArr));									
								//if (disPobOrdtypeList.contains(orderType)) 
								for(String pobOrdType : disPobOrdtypeList)
								{
									if(checkNull(pobOrdType).equals(orderType))
									{
										lbOrdFlg = true;
									}
								}
							}
							//Pavan Rane 01aug19 End[change in rate validation]
							lsListType = distCommon.getPriceListType(priceList, conn);
							if (errCode == null || errCode.trim().length() == 0) {
								//if (priceList == null || priceList.trim().length() == 0 && rate <= 0) { //Pavan Rane 01aug19 End[change in rate validation]							
								if ((!lbOrdFlg && ((priceList == null || priceList.trim().length() == 0) && rate <= 0 )) && (lbOrdFlg && rate < 0 )) 
								{										
									errCode = "VTRATE2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									//System.out.println("There are no contracts pending for release,as per the specified parameters");
									System.out.println("There are no PriceList defined...");
								}
							}

							if (errCode == null || errCode.trim().length() == 0) {									
								//Pavan Rane 1aug19[to change if condtion]
								//if (priceList == null || priceList.trim().length() == 0 && rate <= 0 && !"B".equalsIgnoreCase(lsListType)) {
								if (priceList != null && priceList.trim().length() > 0 && rate <= 0 && !"B".equalsIgnoreCase(lsListType)) {
									if (rate == 0) {
										String lrdateStr = "";
										if (ldtDate != null) {
											Date date = new Date(ldtDate.getTime());
											SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
													genericUtility.getApplDateFormat());
											lrdateStr = simpleDateFormat.format(date);
										}
										mRate = distCommon.pickRate(priceList, lrdateStr, itemCode, "", lsListType,
												quantity, conn);

										if (mRate != 0) {
											errCode = "VTRATE6";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											System.out.println(
													"There are no contracts pending for release,as per the specified parameters");
										}
									} else {
										errCode = "VTRATE1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println(
												"There are no contracts pending for release,as per the specified parameters");
									}
								}
							}
						}
						//}
					}

					// tax_class
					else if (childNodeName.equalsIgnoreCase("tax_class")) {
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
						System.out.println("taxClass:  " + taxClass);
						if (taxClass.trim().length() != 0 && taxClass != null) {
							sql = "select count(*) as cnt from taxclass where tax_class = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) {
								errCode = "VTTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							}

						}
					}
					// pack_code
					else if (childNodeName.equalsIgnoreCase("pack_code")) {
						packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
						if (packCode != null && packCode.trim().length() > 0) {
							sql = "select count(*) as cnt from packing where pack_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, packCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) {
								errCode = "VTPKCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							}
						}
					}

					// tax_chap
					else if (childNodeName.equalsIgnoreCase("tax_chap")) {
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
						System.out.println("taxChap:  " + taxChap);
						if (taxChap != null && taxChap.trim().length() != 0) {
							if (!(isExist(conn, "taxchap", "tax_chap", taxChap))) {
								errCode = "VTTCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Tax chapter not define in tax chapter master!");
							}

						}
					}
					// tax_env
					else if (childNodeName.equalsIgnoreCase("tax_env")) {
						//taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						taxEnv = distCommon.getParentColumnValue("tax_env", dom, "2");
						if (checkNull(genericUtility.getColumnValue("order_date", dom1)).trim().length() > 0) {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						// orderDate=Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom1));
						if (taxEnv != null && taxEnv.trim().length() != 0) {

							if (!(isExist(conn, "taxenv", "tax_env", taxEnv))) {
								errCode = "VTTENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("TAX ENVIRONMENT NOT DEFINED");
							} else// Added by chandrashekar on 10-aug-2016
							{
								//Pavan R 17sept19 start[to validate tax environment]
								//errCode = gfCheckTaxenvStatus(taxEnv, orderDate, conn);
								//errCode = gfCheckTaxenvStatus(taxEnv, orderDate, "S", conn);
								errCode = distCommon.getCheckTaxEnvStatus(taxEnv, orderDate, "S", conn);
								//Pavan R 17sept19 end[to validate tax environment]
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}
					}
					// item_ser
					else if (childNodeName.equalsIgnoreCase("item_ser")) {
						String lsSer = "", lsCust = "", lsItemFlag = "";

						lsSer = checkNullandTrim(genericUtility.getColumnValue("item_ser", dom1));
						lsCust = checkNullandTrim(genericUtility.getColumnValue("cust_code", dom1));
						lsItemFlag = checkNullandTrim(genericUtility.getColumnValue("item_flg", dom));

						if ("I".equalsIgnoreCase(lsItemFlag)) {
							//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][Start]
							String serSpecificCust = "";
							serSpecificCust = distCommon.getDisparams("999999", "SER_SPECIFIC_CUST", conn);
							if("Y".equalsIgnoreCase(serSpecificCust)) 
							{
								//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][End]
								sql = "select count(*) as cnt from customer_series where cust_code = ? and item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCust);
								pstmt.setString(2, lsSer);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0) {
									errCode = "VTITEM7";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println(
											"There are no contracts pending for release,as per the specified parameters");
								}
							}
						}
					}
					// mfg_code
					else if (childNodeName.equalsIgnoreCase("mfg_code")) {
						String lsMfgCode = "";

						lsMfgCode = checkNull(genericUtility.getColumnValue("mfg_code", dom));
						if (lsMfgCode != null && lsMfgCode.trim().length() > 0) {
							sql = "select count(*) as cnt from mfg_note where mfg_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsMfgCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) {
								errCode = "VTIMC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							}
						}
					}
					// rate__clg
					else if (childNodeName.equalsIgnoreCase("rate__clg")) {
						double lcRateClg = 0.00, lcMinRate = 0.00, lcMaxRate = 0.00, mRate = 0.00;
						;
						String lsListType = "";
						Timestamp ldtDate = null;

						lcRateClg = Double.parseDouble(checkDouble(genericUtility.getColumnValue("rate__clg", dom)));
						rate = Double.parseDouble(checkDouble(genericUtility.getColumnValue("rate", dom)));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
						lsListType = checkNull(genericUtility.getColumnValue("price_list", dom1));
						priceList = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));
						if (checkNull(genericUtility.getColumnValue("order_date", dom1)).trim().length() > 0) {
							orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						// orderDate=Timestamp.valueOf(genericUtility.getColumnValue("order_date",
						// dom1));
						quantity = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
						nature = checkNullandTrim(genericUtility.getColumnValue("nature", dom));
						if (checkNull(genericUtility.getColumnValue("pl_date", dom1)).trim().length() > 0) {
							plDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}
						// plDate=Timestamp.valueOf(genericUtility.getColumnValue("pl_date", dom1));

						if (plDate != null) {
							ldtDate = plDate;
						} else {
							ldtDate = orderDate;
						}
						if (nature == null || nature.trim().length() == 0) {
							nature = "C";
						}

						if (priceList != null && priceList.trim().length() > 0) {
							lsListType = checkNullandTrim(distCommon.getPriceListType(priceList, conn));
							if (rate > 0) {
								if (lcRateClg <= 0 && !"B".equalsIgnoreCase(lsListType)) {
									errCode = "VTECRNZ";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("The Dlv date cannot be less than Today's date .");
								}
							}
						}
						if ("C".equalsIgnoreCase(nature) && "I".equalsIgnoreCase(itemFlg)) {
							/*
							 * if(priceList==null || priceList.trim().length()==0) {
							 * //priceList=distCommon.gbf_pick_pricelist(ls_price_list,ldt_date,ls_item_code
							 * ,'','L',lc_qty) }
							 */
							if (priceList != null && priceList.trim().length() > 0) {
								lsListType = checkNullandTrim(distCommon.getPriceListType(priceList, conn));
								sql = "select max(case when min_rate is null then 0 else min_rate end) as lc_min_rate,"
										+ "max(case when max_rate is null then 0 else max_rate end) as lc_max_rate"
										+ " from pricelist where price_list = ? and item_code = ? and list_type = ? and eff_from <= ?"
										+ "	and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								pstmt.setString(2, itemCode);
								pstmt.setString(3, lsListType);
								pstmt.setTimestamp(4, ldtDate);
								pstmt.setTimestamp(5, ldtDate);
								pstmt.setDouble(6, quantity);
								pstmt.setDouble(7, quantity);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lcMinRate = rs.getDouble("lc_min_rate");
									lcMaxRate = rs.getDouble("lc_max_rate");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("lsListType : "+lsListType);
								System.out.println("lcMaxRate P : "+lcMaxRate);
								System.out.println("lcMinRate  P : "+lcMinRate);

								if(! lsListType.equalsIgnoreCase("B") && ! lsListType.equalsIgnoreCase("I"))
								{
									System.out.println("Inside wfvaldata of rate__clg");
									if (lcRateClg < lcMinRate) {
										errCode = "VTRATE9";// Tax class not define in
										// tax class master!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										//System.out.println("There are no contracts pending for release,as per the specified parameters");
										System.out.println("Clearing Rate is less Than MinRate of Pricelist["+lcRateClg+"]<["+lcMinRate+"]");
									}
									if (lcRateClg > lcMaxRate) {
										errCode = "VTRATE10";// Tax class not define in
										// tax class master!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										//System.out.println("There are no contracts pending for release,as per the specified parameters");
										System.out.println("Clearing Rate is greater Than MaxRate of Pricelist["+lcRateClg+"]>["+lcMaxRate+"]");
									}
								}
							}
						}
					}

					// dsp_date
					else if (childNodeName.equalsIgnoreCase("dsp_date")) {
						String lsContractNo = "";
						if (genericUtility.getColumnValue("dsp_date", dom) == null) {
							dspDate = getCurrtDate();
							today = dspDate;
						} else {
							if (checkNull(genericUtility.getColumnValue("dsp_date", dom1)).trim().length() > 0) {
								dspDate = Timestamp.valueOf(genericUtility.getValidDateString(
										genericUtility.getColumnValue("dsp_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
										+ " 00:00:00.0");
							}

							today = java.sql.Timestamp
									.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");

						}
						if (dspDate != null && dspDate.compareTo(today) < 0) {
							errCode = "VTIDLVDATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("The Dlv date cannot be less than Today's date .");
						}
						if ("Y".equalsIgnoreCase(isContractYn) && errCode.trim().length() == 0) {
							lsContractNo = genericUtility.getColumnValue("contract_no", dom1);
							if (lsContractNo != null && lsContractNo.trim().length() > 0) {
								sql = "select count(*) as cnt from   scontract where  contract_no = ? and ? between eff_from and valid_upto";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsContractNo);
								pstmt.setTimestamp(2, dspDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0) {
									errCode = "VDLVDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println(
											"There are no contracts pending for release,as per the specified parameters");
								}
							}
						}
					}
					// quantity__stduom
					else if (childNodeName.equalsIgnoreCase("quantity__stduom")) {
						String lsUnitQuantity = "", lsUnitRate = "";
						double lcQuantity = 0.00, lcStdQuantity = 0.00, lcRate = 0.00, lcStdRate = 0.00, lcTot = 0.00,
								lcStdTot = 0.00;

						lcQuantity = Double.parseDouble(genericUtility.getColumnValue("quantity", dom) == null ? "0.00"
								: genericUtility.getColumnValue("quantity", dom));
						lcStdQuantity = Double
								.parseDouble(genericUtility.getColumnValue("quantity__stduom", dom) == null ? "0.00"
										: genericUtility.getColumnValue("quantity__stduom", dom));
						lcRate = Double.parseDouble(genericUtility.getColumnValue("rate", dom) == null ? "0.00"
								: genericUtility.getColumnValue("rate", dom));
						lcStdRate = Double
								.parseDouble(genericUtility.getColumnValue("rate__stduom", dom) == null ? "0.00"
										: genericUtility.getColumnValue("rate__stduom", dom));

						lcTot = lcQuantity * lcRate;
						lcStdTot = lcStdQuantity * lcStdRate;
						lsUnitQuantity = genericUtility.getColumnValue("unit", dom) == null ? ""
								: genericUtility.getColumnValue("unit", dom);
						lsUnitRate = genericUtility.getColumnValue("unit__rate", dom) == null ? "0.00"
								: genericUtility.getColumnValue("unit__rate", dom);

						// lsUnitQuantity=Double.parseDouble(genericUtility.getColumnValue("unit",
						// dom)==null?"0.00":genericUtility.getColumnValue("unit", dom));
						// lsUnitRate=Double.parseDouble(genericUtility.getColumnValue("unit__rate",
						// dom)==null?"0.00":genericUtility.getColumnValue("unit__rate", dom));

						if (lsUnitQuantity.equalsIgnoreCase(lsUnitRate)) {
							if (Math.abs(lcTot - lcStdTot) > 1) {
								errCode = "VTCONV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							}
						}
					}
					// ord_value
					else if (childNodeName.equalsIgnoreCase("ord_value")) {
						double lcTotOrdVal = 0.00, lcOrdValue = 0.00, lcMaxOrderValue = 0.00, lcOrdValueo = 0.00;

						lcOrdValue = Double.parseDouble(checkDouble(genericUtility.getColumnValue("ord_value", dom)));
						lcTotOrdVal = Double
								.parseDouble(checkDouble(genericUtility.getColumnValue("tot_ord_value", dom1)));
						lcMaxOrderValue = Double
								.parseDouble(checkDouble(genericUtility.getColumnValue("max_order_value", dom1)));

						if (lcMaxOrderValue != 0 && (lcOrdValue + lcTotOrdVal - lcOrdValueo) > lcMaxOrderValue) {
							errCode = "VTEXMAX";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(
									"There are no contracts pending for release,as per the specified parameters");
						}
					} else if (childNodeName.equalsIgnoreCase("spec_ref")) {
						String mval = "", mval1 = "";
						mval = genericUtility.getColumnValue("spec_ref", dom);
						mval1 = genericUtility.getColumnValue("item_code", dom);

						if (mval != null && mval.trim().length() > 0) {
							sql = "select count(*) as cnt from specification where spec_ref =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VMINVSPEC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							} else if (cnt > 0) {
								sql = "select count(*) as cnt from qcitem_spec where item_code = ? and spec_ref =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mval1);
								pstmt.setString(2, mval);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									errCode = "VMNOQCSPE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println(
											"There are no contracts pending for release,as per the specified parameters");
								}
							}
						}
					}

					/*
					 * // conv__rtuom_stduom else if
					 * (childNodeName.equalsIgnoreCase("conv__rtuom_stduom")) { double
					 * lcConvRtuom=0.00;
					 * lcConvRtuom=Double.parseDouble(genericUtility.getColumnValue(
					 * "conv__rtuom_stduom",
					 * dom)==null?"0.00":genericUtility.getColumnValue("conv__rtuom_stduom", dom));
					 * if(lcConvRtuom<=0) { errCode = "VTUNIT3"; errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase()); System.out.
					 * println("There are no contracts pending for release,as per the specified parameters"
					 * ); } }
					 */
					if (childNodeName.equalsIgnoreCase("conv__rtuom_stduom")) //
					{
						// String unit="",unitStd="",convRtuomStduomStr="",itemCode="",
						String lcconvqtystduom1 = "", convRtuomStduomStr = "";
						// double convRtuomStduom=0;
						String rateStr = "", ratestdStr = "";  // added by mahesh saggam on 04/07/2019
						double sordRate = 0, stdRate = 0;
						System.out.println("@@@@@ validation of conv__rtuom_stduom executed......");
						convRtuomStduomStr = checkNullandTrim(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						unit = checkNullandTrim(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNullandTrim(genericUtility.getColumnValue("unit__std", dom));
						convRtuomStduom = convRtuomStduomStr == null || convRtuomStduomStr.trim().length() == 0 ? 0 : Double.parseDouble(convRtuomStduomStr);
						if (unitStd == null || unitStd.length() == 0) {
							itemCode = checkNullandTrim(genericUtility.getColumnValue("item_code", dom));
							unit = setDescription("unit", "item", "item_code", itemCode, conn);
						}
						unit = unit == null ? "" : unit.trim();
						unitStd = unitStd == null ? "" : unitStd.trim();
						System.out.println("@@@@@1 unitRate[" + unit + "]::unitStd[" + unitStd + "]::convRtuomStduom["
								+ convRtuomStduom + "]");
						if ((unit.equalsIgnoreCase(unitStd)) && (convRtuomStduom != 1)) {
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						} else if (!(unit.equalsIgnoreCase(unitStd))) {
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(mitemcode, mval,
							// mval1, lc_conv,conn);
							errCode = gf_check_conv_fact(itemCode, unitStd, unit, convRtuomStduom, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								// errCode = "VTUCON1"; //16-12-17
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// Added by Mahesh Saggam on 05/07/2019 start  [if conv_rate__stduom is 1 then rate and rate__stduom should be same]

						if(convRtuomStduom == 1)
						{

							rateStr = checkNull(genericUtility.getColumnValue("rate", dom));
							ratestdStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));	
							System.out.println("rate = "+rateStr+" std rate = "+ratestdStr);

							if (rateStr != null && rateStr.trim().length() > 0) 
							{
								sordRate = Double.parseDouble(rateStr);	
							}
							if (ratestdStr != null && ratestdStr.trim().length() > 0) 
							{
								stdRate = Double.parseDouble(ratestdStr);
							}
							if(sordRate != stdRate)
							{
								System.out.println("Error occured in validating rate");
								errCode = "VTINVRTE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// Added by Mahesh Saggam on 05/0/2019 end

						if(convRtuomStduom <= 0 )//Added by Mukesh Chauhan on  20/11/19
						{
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//END by Mukesh


					} // end case convRtuomStduom
					// nv__qty_stduom
					else if (childNodeName.equalsIgnoreCase("conv__qty_stduom")) {
						String convQtyStduomStr = "", qtyStr = "", qtyStdStr = "";  //added by mahesh saggam on 04/07/2019
						double dqty = 0, dqtyStduom = 0;
						double lcConvQtyuom;
						lcConvQtyuom = Double
								.parseDouble(genericUtility.getColumnValue("conv__qty_stduom", dom) == null ? "0.00"
										: genericUtility.getColumnValue("conv__qty_stduom", dom));

						if (lcConvQtyuom <= 0) {
							errCode = "VMUCNV1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(
									"There are no contracts pending for release,as per the specified parameters");
						}

						// Added by Mahesh Saggam on 05/07/2019 start  [if conv_qty__stduom is 1 then quantiy and quantity__stduom should be same]

						if(lcConvQtyuom == 1)
						{
							qtyStr = checkNull(genericUtility.getColumnValue("quantity", dom));
							qtyStdStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));	
							System.out.println("quantity = "+qtyStr+" std quantity = "+qtyStdStr);

							if (qtyStr != null && qtyStr.trim().length() > 0) 
							{
								dqty = Double.parseDouble(qtyStr);	
							}
							if (qtyStdStr != null && qtyStdStr.trim().length() > 0) 
							{
								dqtyStduom = Double.parseDouble(qtyStdStr);
							}
							if(dqty != dqtyStduom)
							{
								System.out.println("Error occured in validating quantity");
								errCode = "VTINVQTTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						// Added by Mahesh Saggam on 05/07/2019 end
					}

					// comm_perc_1
					// Commented by Santosh on 24/03/2017 need to compare business logic with
					// orginal component [Start]
					/*
					 * else if (childNodeName.equalsIgnoreCase("comm_perc_1")) { String
					 * lsCommpercOn=""; double lcCommPerc=0.0;
					 * lcCommPerc=checkDoubleNull(genericUtility.getColumnValue("comm_perc_1",
					 * dom)); lsCommpercOn=genericUtility.getColumnValue("comm_perc_on_1", dom);
					 * System.out.println("lsCommpercOn comm_perc_1>>>>>"+lsCommpercOn);
					 * if(lcCommPerc>0) { if(lsCommpercOn==null || lsCommpercOn.trim().length()==0)
					 * { errCode = "VTSOCOMM1"; errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase());
					 * System.out.println("comm_perc_1 the specified parameters"); } } else {
					 * if(lsCommpercOn !=null && lsCommpercOn.trim().length()>0 &&
					 * !"N".equalsIgnoreCase(lsCommpercOn)) { errCode = "VTSOCOMM2";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase());
					 * System.out.println("comm_perc_1 specified parameters"); } } }
					 * 
					 * // comm_perc_2 else if (childNodeName.equalsIgnoreCase("comm_perc_2")) {
					 * String lsCommpercOn=""; double lcCommPerc=0.0;
					 * lcCommPerc=checkDoubleNull(genericUtility.getColumnValue("comm_perc_2",
					 * dom)); lsCommpercOn=genericUtility.getColumnValue("comm_perc_on_2", dom);
					 * System.out.println("lsCommpercOn comm_perc_2>>>>>"+lsCommpercOn);
					 * if(lcCommPerc>0) { if(lsCommpercOn==null || lsCommpercOn.trim().length()==0)
					 * { errCode = "VTSOCOMM1"; errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase());
					 * System.out.println("comm_perc_2  specified parameters"); } } else {
					 * if(lsCommpercOn !=null && lsCommpercOn.trim().length()>0 &&
					 * !"N".equalsIgnoreCase(lsCommpercOn)) { errCode = "VTSOCOMM2";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase());
					 * System.out.println("comm_perc_2 parameters"); } } }
					 * 
					 * // comm_perc_3 else if (childNodeName.equalsIgnoreCase("comm_perc_3")) {
					 * String lsCommpercOn=""; double lcCommPerc=0.0;
					 * lcCommPerc=checkDoubleNull(genericUtility.getColumnValue("comm_perc_3",
					 * dom)); lsCommpercOn=genericUtility.getColumnValue("comm_perc_on_3", dom);
					 * System.out.println("lsCommpercOn comm_perc_3>>>>>"+lsCommpercOn);
					 * if(lcCommPerc > 0) { if(lsCommpercOn==null ||
					 * lsCommpercOn.trim().length()==0) { errCode = "VTSOCOMM1";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase());
					 * System.out.println("comm_perc_2  specified parameters"); } } else {
					 * if(lsCommpercOn!=null && lsCommpercOn.trim().length()>0 &&
					 * !"N".equalsIgnoreCase(lsCommpercOn)) { errCode = "VTSOCOMM2";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase());
					 * System.out.println("comm_perc_3 parameters"); } } }
					 */
					// Commented by Santosh on 24/03/2017 need to compare business logic with
					// orginal component [End]
					// cust_item__ref
					else if (childNodeName.equalsIgnoreCase("cust_item__ref")) {
						String lsCustItemCodeRef = "", lsItemCode = "", lsCustCode = "";

						lsCustItemCodeRef = genericUtility.getColumnValue("cust_item__ref", dom);
						lsItemCode = genericUtility.getColumnValue("item_code__ord", dom);
						lsCustCode = genericUtility.getColumnValue("cust_code", dom1);

						if (lsCustItemCodeRef != null && lsCustItemCodeRef.trim().length() > 0) {
							sql = "select count(*)as cnt from customeritem where cust_code = ? and item_code = ? and item_code__ref = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							pstmt.setString(2, lsItemCode);
							pstmt.setString(3, lsCustItemCodeRef);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTCUSTITM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							}
						}
					}

					// prd_code__rfc
					else if (childNodeName.equalsIgnoreCase("prd_code__rfc")) {
						String mVal = "";

						mVal = genericUtility.getColumnValue("prd_code__rfc", dom1);
						if (mVal != null && mVal.trim().length() > 0) {
							sql = "Select count(*) as cnt from period where code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mVal);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VTRFCDATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(
										"There are no contracts pending for release,as per the specified parameters");
							}
						}
					}

				}
				break;
			case 3:
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName in details !" + childNodeName);
					if (childNodeName.equalsIgnoreCase("term_code")) {
						termCode = checkNull(genericUtility.getColumnValue("term_code", dom1));
						sql = "select count(*) as cnt  from sale_term where term_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, termCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
							System.out.println("Count of termCode: ====" + cnt);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						if (cnt == 0) {
							errCode = "VTTERM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Term Code you have entered does not exist in purchase term master.");
						}
					}
				}
				break;
			}

			int errListSize = errList.size();
			int count = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (count = 0; count < errListSize; count++) {
					errCode = errList.get(count);
					errFldName = errFields.get(count);
					System.out.println(" testing :errCode .:" + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
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

		} // end of try
		catch (Exception e) {
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
		System.out.println("testing : final errString : " + errString);
		return errString;

	}

	//Commented by Pavan R 18oct19 start [to called common method from distcommon]
	/*private String gfCheckTaxenvStatus(String taxEnv, Timestamp orderDate, String busiType, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String errorCode = "", sql = "", lsStatus = "", busiProcUse="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean errFlag = false;
		try {
			sql = "select (case when status is null then 'A' else status end) as ls_status from   taxenv where  tax_env      = ? and    status_date  <=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, taxEnv);
			pstmt.setTimestamp(2, orderDate);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsStatus = checkNullandTrim(rs.getString("ls_status"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if ("C".equalsIgnoreCase(lsStatus)) {
				errorCode = "VTTAXENVCL";
				return errorCode;
			}
			//Pavan R 17sept19 start[to validate tax environment]
			sql = "select busi_proc_use from taxenv where tax_env = ? ";			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, taxEnv);
			rs = pstmt.executeQuery();
			if (rs.next()) {				
				busiProcUse = rs.getString("busi_proc_use") != null ? rs.getString("busi_proc_use") : "";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	
			System.out.println("SaleOrder::TaxEnv::busiProcUse [" + busiProcUse + "]");
			String busiProcUseArr[] = busiProcUse.split(",");
			if (busiProcUseArr.length > 0) {
				for (int i = 0; i < busiProcUseArr.length; i++) {
					if (busiType.trim().equalsIgnoreCase(busiProcUseArr[i].trim())) {
						errFlag = true;
					}
				}
			}
			if (!errFlag) {
				errorCode = "VTTAXENVVL";
				return errorCode;
			}
			//Pavan R 17sept19 end[to validate tax environment]
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return errorCode;
	}*/
	//Commented by Pavan R 18oct19 start [to called common method from distcommon]

	private String gbfScItemQtyVal(String itemCode, String contractNo, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String errCode = "", sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double ldScItemQty = 0.00, ldScItemQtyUsedOnSo = 0.00, totalQty = 0.00, curQty = 0.00;

		try {
			sql = "select sum(quantity) as quantity from scontract sc, scontractdet scd where sc.contract_no=scd.contract_no"
					+ " and sc.contract_no=? and scd.item_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, contractNo);
			pstmt.setString(2, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				ldScItemQty = rs.getDouble("quantity");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "SELECT sum(sorddet.quantity) as quantity FROM sorddet, sorder WHERE sorder.sale_order = sorddet.sale_order"
					+ " and sorder.contract_no= ? and sorddet.item_code = ? and sorder.status <> 'X'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, contractNo);
			pstmt.setString(2, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				ldScItemQtyUsedOnSo = rs.getDouble("quantity");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			totalQty = ldScItemQtyUsedOnSo + curQty;

			if (totalQty > ldScItemQty) {
				errCode = "VSOSCQTY";
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return errCode;
	}

	// itemChange
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext,
			String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";

		System.out.println("hELLO PRINT");
		try {
			System.out.println("xmlString@@@@@@@" + xmlString);

			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			System.out.println("dom@@@@@@@" + dom);

			System.out.println("xmlString1@@@@@@@" + xmlString1);

			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}

			System.out.println("xmlString2@@@@@@@" + xmlString2);

			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			// valueXmlString = itemChangedHdr(dom, dom1, dom2, objContext, currentColumn,
			// editFlag, xtraParams);
			if ("1".equals(objContext.trim())) {
				valueXmlString = itemChangedHdr(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			} else {
				valueXmlString = itemChangedDet(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			}
			System.out.println("VALUE HELLO PRINT[" + valueXmlString + "]");
		} catch (Exception e) {
			System.out.println("Exception : [FreightRateIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA@@[" + valueXmlString + "]");
		return valueXmlString;
	}

	public String itemChangedHdr(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException, SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0, childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String sql = "";
		String lsDeptcode = "", mDlvsite = "", mOrdSite = "", mStatus = "", mCurrency = "", mTaxOpt = "",
				mSingleSer = "", lsDueDays = "", mkeyval = "", lsOrderType = "", lsOtypeDescr = "", lsPlist = "",
				lsPlist1 = "", lsPlist2 = "", lsSiteCode = "", lsCustCode = "", lsSalesPers = "", lsSalesPers1 = "",
				lsSalesPers2 = "", lsContractNo = "", lsOrdtypeSample = "";
		String lsCustCodeDlv = "", lsCustCodeBil = "", mtaxopt = "", mItemSer = "", lsCustPord = "", lcCommPerc = "",
				lsTaxClass = "", lsTaxChap = "", lsTaxEnv = "", ldTaxDate = "";
		String lsPriceList = "", lsCrTerm = "", lsQuotNo = "", lsCurrCode = "", lcExchRate = "", lsRemarks = "",
				lsDlvAdd1 = "", lsDlvAdd2 = "", lsDlvCity = "", lsCountCodeDlv = "";
		String lsDlvPin = "", lsStanCode = "", lsPartQty = "", lsStatus = "", ldStatusDate = "", lsTranCode = "",
				lsUdfStr1 = "", lsUdfStr2 = "", lcUdfNum1 = "", lcUdfNum2 = "";
		String lcCommAmt = "", lsStatusRemarks = "", lsDlvTerm = "", lcFrtAmt = "", lsCurrCodeFrt = "",
				lcExchRateFrt = "", lsFrtTerm = "";
		String lcAdvPerc = "", lsDistRoute = "", lsCurrCodeComm = "", lcCommPerc1 = "", lsCommPercOn1 = "",
				lsCurrCodeComm1 = "", lcCommPerc2 = "", lsCommPercOn2 = "", lsCurrCodeComm2 = "";
		String lsEmpCodeCon = "", lsDlvAdd3 = "", lsStateCodeDlv = "", lsTransMode = "", lsSpecReason = "",
				lsOffshoreInvoice = "", lsLabelType = "", lsOutsideInspection = "";
		String lsRemarks2 = "", lsRemarks3 = "", lsStanCodeInit = "", lsCurrCodeIns = "", lcExchRateIns = "",
				lcInsAmt = "", lsShipStatus = "", lsDlvTo = "", lsAcctCodeSal = "";
		String lsCctrCodeSal = "", tel1 = "", tel2 = "", tel3 = "", fax = "", lcExchRateComm = "", lcExchRateComm1 = "",
				lcExchRateComm2 = "", lsPriceListDisc = "", lsMarketReg = "";
		String lsProjCode = "", lsContractType = "", lsCustnameBil = "", lsDisIndOrdtypeList = "", lsPlistClg = "",
				lsLocGroup = "", lsTermTable = "";
		String ldtOrderDate = "", lsItemser = "", lsPlistDisc = "", mget1 = "", lsTerrdescr = "", descr = "",
				lsAvailableYn = "", lsCctrcodeSal = "", lsAcctcodeSal = "", mcrTermNp = "";
		String mcrdescr = "", mcrterm = "", lsPlistOrderType = "", lsTypeAllowCrLmtList = "", lcCreditLmt = "",
				lcOsAmt = "", lcOvosAmt = "", mTranDate = "", mCode = "", lsCurrcodeBase = "";
		String lsEmpCode = "", lsSordCommCal = "", lsCommTable1 = "", mCcurr = "", lsStateCode = "", lsCommTable3 = "",
				lsSpName = "", lsStanCodeNotify = "";
		String lsItemCode = "", lcSaleRate = "", lsShDescr = "", lsItemCodeOrd = "", lsSaleOrd = "", lsListType = "",
				mPriceListClg = "", lsCommPerc1 = "", lsCommPerc2 = "";
		String mPriceList = "", lsFinscheme = "", lsCommTable2 = "", lsDisLink = "", lsChannelPartner = "";
		String ContractNo = "", lsCommPercOn = "";
		String taxClassHdr="", taxChapHdr="",taxEnvHdr="", siteCodeShip="",stanCodeFr="",stanCodeTo="",custCodeDlv="", custTaxOpt="",custCodeTax="",orderType="",custCodeBill1="";
		//Added by mayur on 26-July-2018---[start] 
		String billAddr1 = "",billAddr2 = "",billAddr3 = "",billCity = "",stateCode1 = "",billPin = "",
				cstNoBill = "",drugLicNo = "",drugLicNo1 = "",drugLicNo2 = "",lstNoBill = ""; 
		//Added by mayur on 26-July-2018---[end] 
		//added by manish mhatre on 28-aug-2019[start]
		String ordTypePrd="";
		boolean ordTypeFlag = false;
		//added by manish mhatre on 28-aug-2019[End]
		Timestamp ldPromDate = null, ldPordDate = null, ldUdfDate1 = null, ldPorderDate = null;
		Timestamp TranDateDet = null;
		String dlvCity = "", state = "", countryCode = ""; // Added By PriyankaC on 27 JUNE 2017
		double mExcRate = 0.0, lcStdrate = 0.00, lsCommPerc = 0.00;
		int cnt = 0, ll_schcnt = 0, cnt1 = 0;
		boolean rootFlag = true;
		String priceLst="",custCodeBilPl="";   //added by manish mhatre on 24-sep-2019
		String priceBasis="";	 //added by manish mhatre on 24-sep-2019
		int pos = 0;
		String reStr = "";

		Timestamp ldDueDate = null;
		String crTermSource = ""; // Added by sarita on 15 NOV 2018
		try {
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("-------- currentFormNo : " + currentFormNo);

			switch (currentFormNo) {
			case 1:
				System.out.println("Sales Order itemchanged case 1");
				System.out.println("---------DOM------[" + genericUtility.serializeDom(dom) + "]");
				System.out.println("---------DOM1------[" + genericUtility.serializeDom(dom1) + "]");
				System.out.println("---------DOM2------[" + genericUtility.serializeDom(dom2) + "]");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				String orderStatus = "O", empCode = "", loginSite = "";
				System.out.println("currentColumn-------->>[" + currentColumn + "]");
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					// valueXmlString.append(itmDefaultHdr(valueXmlString, dom, dom1, dom2,
					// editFlag, xtraParams, objContext, conn));
					valueXmlString = (itmDefaultHdr(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				} else if ((currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))) {
					// valueXmlString.append(itmDefaultEdit(valueXmlString, dom, dom1, dom2,
					// editFlag, xtraParams, objContext, conn));
					valueXmlString = (itmDefaultEdit(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				} else if ((currentColumn.trim().equalsIgnoreCase("contract_no"))) {
					// valueXmlString.append(itmContractNo(valueXmlString, dom, dom1, dom2,
					// editFlag, xtraParams, objContext, conn));
					//Modified by Anjali R. on [14/12/2018][Contract no itemChange should be perform if contract no is not empty][Start]
					String contractNo = "";
					contractNo = genericUtility.getColumnValue("contract_no", dom);
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
					if(contractNo != null && "null".equalsIgnoreCase(contractNo.trim()))
					{
						contractNo = null;
					}
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
					if(contractNo != null && contractNo.trim().length() > 0)
					{
						//Modified by Anjali R. on [14/12/2018][Contract no itemChange should be perform if contract no is not empty][End]
						valueXmlString = (itmContractNo(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
								conn));
					}
				} else if ((currentColumn.trim().equalsIgnoreCase("site_code"))) {
					System.out.println("@V@ Ship site Itemchange ..");
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					lsCustCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));   
					// ldtOrderDate = checkNull(genericUtility.getColumnValue("order_date", dom));
					ldtOrderDate = genericUtility.getColumnValue("order_date", dom);   // changes made by mahesh saggam on 09/aug/2019   [Removed checknull()]
					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));

					lsPlistDisc = priceListDiscount(lsSiteCode, lsCustCode, conn);
					valueXmlString.append("<price_list__disc protect = \"1\">").append("<![CDATA[" + lsPlistDisc + "]]>")
					.append("</price_list__disc>");   //protected by manish mhatre on 15-apr-2020

					lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
					lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);

					if(ldtOrderDate != null)  // if condition added by mahesh saggam on 09/aug/2019 
					{
						Timestamp ldtOrdDt = Timestamp.valueOf(genericUtility.getValidDateString(ldtOrderDate,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						System.out.println("@V@ ldtOrdDt :-[" + ldtOrdDt + "]");
						lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));						
						// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
						if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
						{
							lsContractNo = null;
						}
						// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
						
						lsContractNo = getContractHrd(lsSiteCode, lsCustCode, ldtOrdDt, lsItemser, conn);
					}


					if (lsContractNo != null && lsContractNo.trim().length() > 0) {
						valueXmlString.append("<contract_no>").append("<![CDATA[" + lsContractNo + "]]>")
						.append("</contract_no>");
					} else {
						valueXmlString.append("<contract_no>").append("<![CDATA[]]>").append("</contract_no>");
					}

					lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
					boolean lbOrdFlag = false;
					String lsDisIndOrdtypeListArr[] = lsDisIndOrdtypeList.split(",");
					if (lsDisIndOrdtypeListArr.length > 0) {
						for (int i = 0; i < lsDisIndOrdtypeListArr.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsDisIndOrdtypeListArr[i])) {
								lbOrdFlag = true;
							}
						}
					}
					//added by manish mhatre on 24-sep-2019
					//start manish
					sql="SELECT PRICE_BASIS FROM SORDERTYPE where ORDER_TYPE= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,lsOrderType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						priceBasis = checkNullandTrim(rs.getString("PRICE_BASIS"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if((priceBasis.length()>0))
					{
						sql=" SELECT CUST_CODE__BIL,CUST_CODE__DLV FROM CUSTOMER WHERE CUST_CODE = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,lsCustCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							custCodeBilPl = rs.getString("CUST_CODE__BIL");
							custCodeDlv = rs.getString("CUST_CODE__DLV");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(("B").equalsIgnoreCase(priceBasis))
						{
							lsCustCode=custCodeBilPl;
						}
						if(("D").equalsIgnoreCase(priceBasis))
						{
							lsCustCode=custCodeDlv;
						}
					}

					//end manish

					if (lbOrdFlag) {
						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date", dom1));
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
						}
					} else {
						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code = ?  AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							System.out.println(
									"@V@ Order Date :- [" + genericUtility.getColumnValue("order_date", dom1) + "]");
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							// Timestamp orderDate =
							// Timestamp.valueOf(genericUtility.getColumnValue("order_date", dom1));as
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCodeDlv, conn);
						}
					}
					if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
						valueXmlString.append("<price_list>").append("<![CDATA[" + lsPriceListDisc + "]]>")
						.append("</price_list>");
					} 
					else
					{
						//Modified by Anjali R. on [17/10/2018][Set actual price list value][Start]
						//valueXmlString.append("<price_list>").append("<![CDATA[" + lsPriceListDisc + "]]>")
						//		.append("</price_list>");
						valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + lsPriceList + "]]>")
						.append("</price_list>");   //protected by manish mhatre on 15-apr-2020
						valueXmlString.append("<price_list__clg>").append("<![CDATA[" + lsPlistClg + "]]>")
						.append("</price_list__clg>");
						//Modified by Anjali R. on [17/10/2018][Set actual price list value][End]
					}
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));   //added by manish mhatre on 24-sep-2019
					valueXmlString.append("<site_code__ship>").append("<![CDATA[" + lsSiteCode + "]]>")
					.append("</site_code__ship>");
				}

				// itmchange on dvl_city --Added By PriyankaC on 27/JUNE/2017 [--Start--]

				else if ("dlv_city".equalsIgnoreCase(currentColumn.trim())) {
					String custCode="";
					dlvCity = checkNull(genericUtility.getColumnValue("dlv_city", dom));
					System.out.println("city from hdr : [" + dlvCity + "]");
					dlvCity = dlvCity.toUpperCase().trim();
					if (dlvCity != null && dlvCity.trim().length() > 0) {
						sql = " select s.state_code,st.count_code from station s,state st  where UPPER(city)= ? and s.state_code=st.state_code";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, dlvCity);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							state = checkNull(rs.getString("state_code"));
							countryCode = checkNull(rs.getString("count_code"));

						}

						if (rs != null) {
							rs.close();
						}
						rs = null;

						if (pstmt != null) {
							pstmt.close();
						}
						pstmt = null;

						valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + state + "]]>")
						.append("</state_code__dlv>");
						valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countryCode + "]]>")
						.append("</count_code__dlv>");
					}
				} // Added By PriyankaC on 27/JUNE/2017 [--END--]

				else if ((currentColumn.trim().equalsIgnoreCase("terr_code"))) {
					mget1 = checkNull(genericUtility.getColumnValue("terr_code", dom));
					System.out.println("@V@ terr_code 1:- [" + mget1 + "]");
					if (mget1 != null && mget1.trim().length() > 0) {
						System.out.println("@V@ terr_code 2:- [" + mget1 + "]");
						sql = "select descr from territory where terr_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mget1);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsTerrdescr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<territory_descr>").append("<![CDATA[" + lsTerrdescr + "]]>")
						.append("</territory_descr>");
					}
				}
				// Changes done by Mayur.K.Nair on 07/JUNE/17-----[START]

				else if ((currentColumn.trim().equalsIgnoreCase("order_type"))) {

					lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					lsCustCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					custCodeBill1 = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));

					sql = "select descr,available_yn from sordertype where order_type = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsOrderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString("descr"));
						lsAvailableYn = checkNull(rs.getString("available_yn"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<descr>").append("<![CDATA[" + descr + "]]>").append("</descr>");
					setNodeValue(dom, "descr", getAbsString(descr));

					valueXmlString.append("<available_yn>").append("<![CDATA[" + lsAvailableYn + "]]>")
					.append("</available_yn>");
					setNodeValue(dom, "available_yn", getAbsString(lsAvailableYn));

					lsCctrcodeSal = finCommon.getAcctDetrTtype("", lsItemser, "SAL", lsOrderType, conn);
					String lsAcctcodeSalArr[] = lsCctrcodeSal.split(",");
					/*
					 * if(lsAcctcodeSalArr.length>0) { lsCctrcodeSal=lsAcctcodeSalArr[0]; } else {
					 * lsCctrcodeSal=""; } if(lsAcctcodeSalArr.length>0) { lsAcctcodeSal =
					 * lsAcctcodeSalArr[1]; } else { lsAcctcodeSal=""; }
					 */
					if (lsAcctcodeSalArr.length > 0) {
						lsAcctcodeSal = lsAcctcodeSalArr[0];
					} else {
						lsAcctcodeSal = "";
					}
					if (lsAcctcodeSalArr.length > 0) {
						lsCctrcodeSal = lsAcctcodeSalArr[1];
					} else {
						lsCctrcodeSal = "";
					} // change by chandrashekar on 12-Aug-2016
					valueXmlString.append("<acct_code__sal>").append("<![CDATA[" + lsAcctcodeSal + "]]>")
					.append("</acct_code__sal>");
					setNodeValue(dom, "acct_code__sal", getAbsString(lsAcctcodeSal));
					valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + lsCctrcodeSal + "]]>")
					.append("</cctr_code__sal>");
					setNodeValue(dom, "cctr_code__sal", getAbsString(lsCctrcodeSal));

					if (lsCctrcodeSal == null || lsCctrcodeSal.trim().length() == 0) {
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);

						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__1", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);

						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__2", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);

					}
					//Added and commented by sarita to set cr_term_source on 15 NOV 2018 [START]
					//sql = "select cust_tax_opt from sordertype where order_type = ? ";
					sql = "select cust_tax_opt,cr_term_source from sordertype where order_type = ? ";
					//Added and commented by sarita to set cr_term_source on 15 NOV 2018 [END]
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsOrderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
						//Added by sarita on 15 NOV 2018 [START]
						crTermSource = checkNull(rs.getString("cr_term_source"));
						//Added by sarita on 15 NOV 2018 [END]
					}
					//Added and commented by sarita on 15 NOV 2018 [START]
					/*pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	*/
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					//Added and commented by sarita on 15 NOV 2018 [END]
					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}
					//added by manish mhatre on 28-aug-2019[For set cr_term_np when enter order type ]
					//start manish
					ordTypePrd = distCommon.getDisparams("999999","ORD_TYPE_NEWPRD",conn);
					ordTypePrd = ordTypePrd == null || ordTypePrd.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(ordTypePrd) ? " " :ordTypePrd.trim();

					if (ordTypePrd.trim().length() > 0 )
					{
						String ordTypeArr[] = ordTypePrd.split(",");
						for(int i = 0; i<ordTypeArr.length; i++)
						{
							if (lsOrderType.trim().equals(ordTypeArr[i].trim()))
							{
								ordTypeFlag=true;
							}
						}
					}
					System.out.println("Inside OrderType itemchanged..."+lsOrderType);
					//					if (lsOrderType != null && lsOrderType.equalsIgnoreCase("NP")) {     //commented by manish mhatre on 28-aug-2019
					if(lsOrderType != null && ordTypeFlag) {     //end manish
						sql = "select cr_term__np from customer_series where cust_code =? and item_ser  = ? ";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1, lsCustCode);  //changed by Pavan R on 16aug18 [to set cr_term on billto/dlvto]
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
						/*if("1".equals(custTaxOpt))	{
							pstmt.setString(1, custCodeBill1);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, lsCustCodeDlv);							
						}*/
						if((crTermSource != null ) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeBill1);
						}
						else if((crTermSource != null ) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, lsCustCodeDlv);
						}
						else 
						{
							pstmt.setString(1, lsCustCode);
						}
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTermNp = rs.getString("cr_term__np");
						}						
						//Added and commented by sarita on 15 NOV 2018 [START]
						/*rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	*/
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						//Added and commented by sarita on 15 NOV 2018 [END]

						if (mcrTermNp == null || mcrTermNp.trim().length() == 0) {
							sql = "select cr_term__np from customer where cust_code = ? ";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, lsCustCode);
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
							/*if("1".equals(custTaxOpt))	{
								pstmt.setString(1, custCodeBill1);								
							}else if("0".equals(custTaxOpt)) {						
								pstmt.setString(1, lsCustCodeDlv);								
							}*/

							if((crTermSource != null ) && ("B".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeBill1);
							}
							else if((crTermSource != null ) && ("D".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, lsCustCodeDlv);
							}
							else 
							{
								pstmt.setString(1, lsCustCode);
							}

							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mcrTermNp = rs.getString("cr_term__np");
							}
							//Added and commented by sarita on 15 NOV 2018 [START]
							/*rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	*/
							if(rs != null)
							{
								rs.close();
								rs = null;
							}
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							//Added and commented by sarita on 15 NOV 2018 [END]
						}
						valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrTermNp + "]]>").append("</cr_term>");
						setNodeValue(dom, "cr_term", getAbsString(mcrTermNp));
						sql = "select descr from crterm where cr_term =  ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcrTermNp);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrdescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<crterm_descr>").append("<![CDATA[" + mcrdescr + "]]>")
						.append("</crterm_descr>");
						setNodeValue(dom, "crterm_descr", getAbsString(mcrdescr));
					} else {						
						mcrterm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						sql = "SELECT CR_TERM_MAPPING.CR_TERM_MAP FROM CR_TERM_MAPPING"
								+ " WHERE ( CR_TERM_MAPPING.CR_TERM =?) AND ( CR_TERM_MAPPING.ORD_TYPE = ?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcrterm);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrterm = rs.getString("CR_TERM_MAP");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (mcrterm == null || mcrterm.trim().length() == 0) {
							sql = "select cr_term from customer_series where cust_code =? and item_ser  =?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, lsCustCode);
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
							/*if("1".equals(custTaxOpt))	{
								pstmt.setString(1, custCodeBill1);								
							}else if("0".equals(custTaxOpt)) {						
								pstmt.setString(1, lsCustCodeDlv);								
							}*/
							if((crTermSource != null ) && ("B".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeBill1);
							}
							else if((crTermSource != null ) && ("D".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, lsCustCodeDlv);
							}
							else 
							{
								pstmt.setString(1, lsCustCode);
							}

							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
							pstmt.setString(2, lsItemser);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mcrterm = rs.getString("cr_term");
							}
							//Added and commented by sarita on 15 NOV 2018 [START]
							/*rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	*/
							if(rs != null)
							{
								rs.close();
								rs = null;
							}
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							//Added and commented by sarita on 15 NOV 2018 [END]
						}
						valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrterm + "]]>").append("</cr_term>");
						setNodeValue(dom, "cr_term", getAbsString(mcrterm));

						sql = "select descr from crterm where cr_term =  ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcrterm);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrdescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<crterm_descr>").append("<![CDATA[" + mcrdescr + "]]>")
						.append("</crterm_descr>");
						setNodeValue(dom, "crterm_descr", getAbsString(mcrdescr));
					}

					lsPlistDisc = priceListDiscount(lsSiteCode, lsCustCodeDlv, conn);
					sql = "select order_type from pricelist where price_list =  ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsPlistDisc);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsPlistOrderType = checkNull(rs.getString("order_type"));
						System.out.println("lsPlistOrderType["+lsPlistOrderType+"]");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if ((lsOrderType.equalsIgnoreCase("NE") && lsPlistOrderType.equalsIgnoreCase("NE"))
							|| (!lsOrderType.equalsIgnoreCase("NE") && lsPlistOrderType == null)) {
						valueXmlString.append("<price_list__disc protect = \"1\">").append("<![CDATA[" + lsPlistDisc + "]]>")
						.append("</price_list__disc>");  //protected by manish mhatre on 15-apr-2020
						setNodeValue(dom, "price_list__disc", getAbsString(lsPlistDisc));
					} else {
						valueXmlString.append("<price_list__disc>").append("<![CDATA[]]>")
						.append("</price_list__disc>");
						setNodeValue(dom, "price_list__disc", getAbsString(""));
					}
					ldtOrderDate = checkNull(genericUtility.getColumnValue("order_date", dom));
					lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
					boolean lbOrdFlag = false;
					if (lsDisIndOrdtypeList.trim().length() > 0) {
						String lsDisIndOrdTypeArr[] = lsDisIndOrdtypeList.split(",");
						for (int i = 0; i < lsDisIndOrdTypeArr.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsDisIndOrdTypeArr[i])) {
								lbOrdFlag = true;
							}
						}
					}

					//added by manish mhatre on 24-sep-2019
					//start manish
					sql="SELECT PRICE_BASIS FROM SORDERTYPE where ORDER_TYPE= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,lsOrderType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						priceBasis = checkNullandTrim(rs.getString("PRICE_BASIS"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if((priceBasis.length()>0))
					{
						sql=" SELECT CUST_CODE__BIL,CUST_CODE__DLV FROM CUSTOMER WHERE CUST_CODE = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,lsCustCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							custCodeBilPl = rs.getString("CUST_CODE__BIL");
							custCodeDlv = rs.getString("CUST_CODE__DLV");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(("B").equalsIgnoreCase(priceBasis)) 
						{
							lsCustCode=custCodeBilPl;
							lsCustCodeDlv=custCodeBilPl;
						}
						if(("D").equalsIgnoreCase(priceBasis))
						{
							lsCustCode=custCodeDlv;
							lsCustCodeDlv=custCodeDlv;
						}
					}
					//end manish

					if (lbOrdFlag) {
						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
						}
					} else {
						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code = ?  AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCodeDlv, conn);
						}
					}
					lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);
					if (lbOrdFlag) {
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							pstmt.setString(2, lsSiteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from customer where  cust_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					} else {
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCodeDlv);
							pstmt.setString(2, lsSiteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from customer where  cust_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCodeDlv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
						valueXmlString.append("<price_list>").append("<![CDATA[]]>").append("</price_list>");
						setNodeValue(dom, "price_list", getAbsString(""));
						valueXmlString.append("<price_list__clg>").append("<![CDATA[]]>").append("</price_list__clg>");
						setNodeValue(dom, "price_list__clg", getAbsString(""));
					} else {
						valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + lsPriceList + "]]>")
						.append("</price_list>");   //protected by manish mhatre on 15-apr-2020
						setNodeValue(dom, "price_list", getAbsString(lsPriceList));
						valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + lsPlistClg + "]]>")
						.append("</price_list__clg>");  //protected by manish mhatre on 15-apr-2020
						setNodeValue(dom, "price_list__clg", getAbsString(lsPlistClg));
					}
					lsCustCodeDlv=checkNull(genericUtility.getColumnValue("cust_code__dlv", dom)); //added  by manish mhatre on 04-oct-2019
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));   //added by manish mhatre on 24-sep-2019

					lsTypeAllowCrLmtList = distCommon.getDisparams("999999", "TYPE_ALLOW_CR_LMT", conn);
					if (lsTypeAllowCrLmtList == null || lsTypeAllowCrLmtList.trim().length() == 0
							|| lsTypeAllowCrLmtList.equalsIgnoreCase("NULLFOUND")) {
						lbOrdFlag = false;
					} else {
						String lsTypeAllowCrLmt[] = lsTypeAllowCrLmtList.split(",");
						for (int i = 0; i < lsTypeAllowCrLmt.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsTypeAllowCrLmt[i])) {
								lbOrdFlag = true;
								break;
							}
						}
					}
					if (lbOrdFlag) {
						sql = "select CREDIT_LMT from customer_series where cust_code = ? and 	item_ser  = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcCreditLmt = rs.getString("CREDIT_LMT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select fn_get_cust_series(?,?,?,'T')as lc_os_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						pstmt.setString(3, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOsAmt = rs.getString("lc_os_amt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select fn_get_cust_series(?,?,?,'O') as lc_ovos_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						pstmt.setString(3, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOvosAmt = rs.getString("lc_ovos_amt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						sql = "select credit_lmt from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcCreditLmt = rs.getString("credit_lmt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select fn_get_custos(?,?,'T')as lc_os_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOsAmt = rs.getString("lc_os_amt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select fn_get_custos(?,?,'O') as lc_ovos_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOvosAmt = rs.getString("lc_ovos_amt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<os_amt>").append("<![CDATA[" + lcOsAmt + "]]>").append("</os_amt>");
					setNodeValue(dom, "os_amt", getAbsString(lcOsAmt));
					valueXmlString.append("<cr_lmt>").append("<![CDATA[" + lcCreditLmt + "]]>").append("</cr_lmt>");
					setNodeValue(dom, "cr_lmt", getAbsString(lcCreditLmt));
					valueXmlString.append("<ovos_amt>").append("<![CDATA[" + lcOvosAmt + "]]>").append("</ovos_amt>");
					setNodeValue(dom, "ovos_amt", getAbsString(lcOvosAmt));
				}
				// Changes done by Mayur.K.Nair on 07/JUNE/17-----[END]
				else if (currentColumn.trim().equalsIgnoreCase("item_ser")) {

					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
					//Add by Ajay on 06/02/18:START
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					//Add by Ajay on 06/02/18:END
					sql = "select descr from itemser where item_ser =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<itemser_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</itemser_descr>");
					setNodeValue(dom, "itemser_descr", getAbsString(descr));
					lsCctrcodeSal = finCommon.getAcctDetrTtype("", lsItemser, "SAL", lsOrderType, conn);

					// String lsAcctcodeSalArr[] = lsCctrcodeSal.split("\t");
					String lsAcctcodeSalArr[] = lsCctrcodeSal.split(",");
					/*
					 * if(lsAcctcodeSalArr.length>0) { lsCctrcodeSal=lsAcctcodeSalArr[0]; } else {
					 * lsCctrcodeSal=""; } if(lsAcctcodeSalArr.length>0) { lsAcctcodeSal =
					 * lsAcctcodeSalArr[1]; } else { lsAcctcodeSal="";
					 * 
					 * }
					 */
					if (lsAcctcodeSalArr.length > 0) {
						lsAcctcodeSal = lsAcctcodeSalArr[0];
					} else {
						lsAcctcodeSal = "";
					}
					if (lsAcctcodeSalArr.length > 0) {
						lsCctrcodeSal = lsAcctcodeSalArr[1];
					} else {
						lsCctrcodeSal = "";

					} // Change by chandrashekar on 12-aug-2016

					System.out.println("@V@ lsAcctcodeSal :-[" + lsAcctcodeSal + "]");

					valueXmlString.append("<acct_code__sal>").append("<![CDATA[" + lsAcctcodeSal + "]]>")
					.append("</acct_code__sal>");
					setNodeValue(dom, "acct_code__sal", getAbsString(lsAcctcodeSal));
					valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + lsCctrcodeSal + "]]>")
					.append("</cctr_code__sal>");
					setNodeValue(dom, "cctr_code__sal", getAbsString(lsCctrcodeSal));

					if (lsCctrcodeSal == null || lsCctrcodeSal.trim().length() == 0) {
						System.out.println(
								"valueXmlString before internal item change :-[" + valueXmlString.toString() + "]");
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						System.out.println("Rest string after [sales_pers] item change :- [" + reStr + "]");
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						System.out.println("valueXmlString after internal item change [sales_pers]:-["
								+ valueXmlString.toString() + "]");

						System.out.println("valueXmlString before internal item change sales_pers__1:-["
								+ valueXmlString.toString() + "]");
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__1", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						System.out.println("Rest string after [sales_pers1] item change :- [" + reStr + "]");
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						System.out.println("valueXmlString after internal item change [sales_pers1]:-["
								+ valueXmlString.toString() + "]");

						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__2", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					//Commented by sarita on 04 APR 2019 to set values of customerSerIC [START]
					/*if (lsCustCode != null && lsCustCode.trim().length() > 0) {
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}*/
					valueXmlString = customerSerIC(valueXmlString, dom, dom1, dom2,
							editFlag, xtraParams, objContext, conn);
					System.out.println("valueXmlString is ["+valueXmlString+"]");
					//Commented by sarita on 04 APR 2019 to set values of customerSerIC [END]

					System.out.println("Final item change [sales_pers1]:-[" + valueXmlString.toString() + "]");
				} else if (currentColumn.trim().equalsIgnoreCase("order_date")) {
					mTranDate = checkNull(genericUtility.getColumnValue("order_date", dom));
					valueXmlString.append("<tax_date>").append("<![CDATA[" + mTranDate + "]]>").append("</tax_date>");
					setNodeValue(dom, "tax_date", getAbsString(mTranDate));
					valueXmlString.append("<pl_date>").append("<![CDATA[" + mTranDate + "]]>").append("</pl_date>");
					setNodeValue(dom, "pl_date", getAbsString(mTranDate));
					valueXmlString.append("<prom_date>").append("<![CDATA[" + mTranDate + "]]>").append("</prom_date>");
					setNodeValue(dom, "prom_date", getAbsString(mTranDate));
					valueXmlString.append("<due_date>").append("<![CDATA[" + mTranDate + "]]>").append("</due_date>");
					setNodeValue(dom, "due_date", getAbsString(mTranDate));
				} else if (currentColumn.trim().equalsIgnoreCase("dlv_term")) {
					mCode = checkNull(genericUtility.getColumnValue("dlv_term", dom));
					if (mCode != null && mCode.trim().length() > 0) {
						sql = "select freight from delivery_term where dlv_term = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsFrtTerm = checkNull(rs.getString("freight"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsFrtTerm != null && lsFrtTerm.trim().length() > 0) {
							valueXmlString.append("<frt_term protect = \"1\">").append("<![CDATA[" + lsFrtTerm + "]]>")
							.append("</frt_term>");
						} else {
							valueXmlString.append("<frt_term protect = \"0\">").append("<![CDATA[]]>")
							.append("</frt_term>");
						}
					}
				} else if (currentColumn.trim().equalsIgnoreCase("cr_term")) {
					String crTerm = "", crDescr = "";
					crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
					sql = "select descr  from crterm where cr_term = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, crTerm);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						crDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + crDescr + "]]>")
					.append("</crterm_descr>");
				} else if (currentColumn.trim().equalsIgnoreCase("stan_code")) {
					String stanCode = "", stanDescr = "";
					stanCode = genericUtility.getColumnValue("stan_code", dom);
					sql = "select descr from station where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						stanDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<station_descr>").append("<![CDATA[" + stanDescr + "]]>")
					.append("</station_descr>");
				} else if (currentColumn.trim().equalsIgnoreCase("curr_code")) {
					lsCurrCode = genericUtility.getColumnValue("curr_code", dom);
					lsSiteCode = genericUtility.getColumnValue("site_code", dom);
					ldtOrderDate = genericUtility.getColumnValue("order_date", dom);

					lcExchRate = String.valueOf(
							finCommon.getDailyExchRateSellBuy(lsCurrCode, "", lsSiteCode, ldtOrderDate, "S", conn));

					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCurrCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<currency_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</currency_descr>");
					setNodeValue(dom, "currency_descr", getAbsString(descr));
					valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + lsCurrCode + "]]>")
					.append("</curr_code__frt>");
					setNodeValue(dom, "curr_code__frt", getAbsString(lsCurrCode));
					valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + lsCurrCode + "]]>")
					.append("</curr_code__ins>");
					setNodeValue(dom, "curr_code__ins", getAbsString(lsCurrCode));
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + lcExchRate + "]]>")
					.append("</exch_rate>");
					setNodeValue(dom, "exch_rate", getAbsString(lcExchRate));
					valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + lcExchRate + "]]>")
					.append("</exch_rate__frt>");
					setNodeValue(dom, "exch_rate__frt", getAbsString(lcExchRate));
					valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + lcExchRate + "]]>")
					.append("</exch_rate__ins>");
					setNodeValue(dom, "exch_rate__ins", getAbsString(lcExchRate));

					if (lsCurrCode != null && lsCurrCode.trim().length() > 0) {
						sql = "select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCurrcodeBase = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
						//if (lsCurrcodeBase != null && lsCurrcodeBase.trim().length() > 0) {
						System.out.println("lsCurrcodeBase--["+lsCurrcodeBase+"]lsCurrCode--["+lsCurrCode+"]");
						if ((lsCurrcodeBase != null && lsCurrcodeBase.trim().length() > 0) && lsCurrCode.equalsIgnoreCase(lsCurrcodeBase)) {
							//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
							valueXmlString.append("<exch_rate protect = \"1\">")
							.append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate>");
							valueXmlString.append("<exch_rate__frt protect = \"1\">")
							.append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate__frt>");
							valueXmlString.append("<exch_rate__ins protect = \"1\">")
							.append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate__ins>");

							setNodeValue(dom, "exch_rate", getAbsString(lcExchRate));
							setNodeValue(dom, "exch_rate__frt", getAbsString(lcExchRate));
							setNodeValue(dom, "exch_rate__ins", getAbsString(lcExchRate));
						} else {
							valueXmlString.append("<exch_rate protect = \"0\">")
							.append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate>");
							valueXmlString.append("<exch_rate__frt protect = \"0\">")
							.append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate__frt>");
							valueXmlString.append("<exch_rate__ins protect = \"0\">")
							.append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate__ins>");

							setNodeValue(dom, "exch_rate", getAbsString(lcExchRate));
							setNodeValue(dom, "exch_rate__frt", getAbsString(lcExchRate));
							setNodeValue(dom, "exch_rate__ins", getAbsString(lcExchRate));
						}
					}
					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "exch_rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				} else if (currentColumn.trim().equalsIgnoreCase("sales_pers")) {
					String commPerc = "", commPercOn = "", currCodeComm = "";
					double exchRateComm = 0.00;

					mCode = checkNull(genericUtility.getColumnValue("sales_pers", dom));

					sql = "select  (case when emp_code is null then '' else emp_code end) as emp_code from sales_pers where sales_pers = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsEmpCode = rs.getString("emp_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsEmpCode != null && lsEmpCode.trim().length() > 0) {
						lsCctrCodeSal = checkNull(genericUtility.getColumnValue("cctr_code__sal", dom));
						// Modified by Anjali R. [To get cctr_code__sal if it is null in
						// dom]on[24/11/2017][Start]
						// if (lsCctrCodeSal != null && lsCctrCodeSal.trim().length() > 0)
						if (lsCctrCodeSal == null || lsCctrCodeSal.trim().length() == 0)
							// Modified by Anjali R. [To get cctr_code__sal if it is null in
							// dom]on[24/11/2017][End]
						{
							sql = "select (case when cctr_code__sal is null then '' else cctr_code__sal end) as cctr_code__sal from employee where emp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsEmpCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCctrCodeSal = rs.getString("cctr_code__sal");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + lsCctrCodeSal + "]]>")
							.append("</cctr_code__sal>");
						}
					}

					commPerc = checkNull(genericUtility.getColumnValue("comm_perc", dom));
					commPercOn = checkNull(genericUtility.getColumnValue("comm_perc__on", dom));
					currCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm", dom));
					exchRateComm = Double
							.parseDouble(genericUtility.getColumnValue("exch_rate__comm", dom) == null ? "0.00"
									: genericUtility.getColumnValue("exch_rate__comm", dom));

					valueXmlString.append("<comm_perc protect = \"0\">").append("<![CDATA[" + commPerc + "]]>")
					.append("</comm_perc>");
					setNodeValue(dom, "comm_perc", getAbsString(commPerc));

					valueXmlString.append("<comm_perc__on protect = \"0\">").append("<![CDATA[" + commPercOn + "]]>")
					.append("</comm_perc__on>");
					setNodeValue(dom, "comm_perc__on", getAbsString(commPercOn));

					valueXmlString.append("<curr_code__comm protect = \"0\">")
					.append("<![CDATA[" + currCodeComm + "]]>").append("</curr_code__comm>");
					setNodeValue(dom, "curr_code__comm", getAbsString(currCodeComm));

					valueXmlString.append("<exch_rate__comm protect = \"0\">")
					.append("<![CDATA[" + exchRateComm + "]]>").append("</exch_rate__comm>");
					setNodeValue(dom, "exch_rate__comm", getAbsString(String.valueOf(exchRateComm)));

					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));

					lsSordCommCal = distCommon.getDisparams("999999", "SORD_COMM_CAL", conn);
					if (lsSordCommCal == null || lsSordCommCal.trim().length() == 0
							|| lsSordCommCal.equalsIgnoreCase("NULLFOUND")) {
						lsSordCommCal = "H";
					}
					if (lsSordCommCal.equalsIgnoreCase("H")) {
						Timestamp orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						sql = "select comm_table__1 from   customer_series where  cust_code = ? and item_ser  = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable1 = rs.getString("comm_table__1");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsCommTable1 == null || lsCommTable1.trim().length() == 0) {
							sql = "select comm_table__1 from sales_pers where  sales_pers =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommTable1 = rs.getString("comm_table__1");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsCommTable1 == null || lsCommTable1.trim().length() == 0) {
							sql = "select comm_table__1 from customer where  cust_code =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommTable1 = rs.getString("comm_table__1");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsCommTable1 != null && lsCommTable1.trim().length() > 0) {
							sql = "select count(*) as cnt from	comm_det where  comm_table = ? and item_ser	= ? and	eff_date   <= ? and valid_upto >= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable1);
							pstmt.setString(2, lsItemser);
							pstmt.setTimestamp(3, orderDate);
							pstmt.setTimestamp(4, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								sql = "select (case when comm_perc is null then 0 else comm_perc end) , comm_perc__on"
										+ " from comm_det where comm_table = ? and item_ser= ? and eff_date   <= ? and valid_upto >=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCommTable1);
								pstmt.setString(2, lsItemser);
								pstmt.setTimestamp(3, orderDate);
								pstmt.setTimestamp(4, orderDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsCommPerc = rs.getDouble("comm_perc");
									lsCommPercOn = rs.getString("comm_perc__on");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						if ((lsCommPerc == 0) || lsCommPercOn.trim().length() == 0) {
							sql = "select (case when comm_perc is null then 0 else comm_perc end) as comm_perc, comm_perc__on from customer_series where  cust_code = ? and item_ser  =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							pstmt.setString(2, lsItemser);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommPerc = rs.getDouble("comm_perc");
								lsCommPercOn = rs.getString("comm_perc__on");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if ((lsCommPerc) == 0) {
								sql = "select comm_perc,comm_perc__on from   sales_pers where  sales_pers =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsCommPerc = rs.getDouble("comm_perc");
									lsCommPercOn = rs.getString("comm_perc__on");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}
					sql = "select sp_name, curr_code from sales_pers where sales_pers =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("sp_name");
						mCcurr = rs.getString("curr_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					//valueXmlString.append("<sp_name>").append("<![CDATA[" + descr + "]]>").append("</sp_name>");
					//setNodeValue(dom, "sp_name", getAbsString(descr));
					valueXmlString.append("<sp_name1>").append("<![CDATA[" + descr + "]]>").append("</sp_name1>");
					setNodeValue(dom, "sp_name1", getAbsString(descr));

					valueXmlString.append("<comm_perc>").append("<![CDATA[" + lsCommPerc + "]]>")
					.append("</comm_perc>");
					setNodeValue(dom, "comm_perc", getAbsString(String.valueOf(lsCommPerc)));

					if ((lsCommPerc) > 0) {
						valueXmlString.append("<comm_perc__on>").append("<![CDATA[" + lsCommPercOn + "]]>")
						.append("</comm_perc__on>");
						setNodeValue(dom, "comm_perc__on", getAbsString(String.valueOf(lsCommPercOn)));
					}
					valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + mCcurr + "]]>")
					.append("</curr_code__comm>");
					setNodeValue(dom, "curr_code__comm", getAbsString(mCcurr));

					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "curr_code__comm", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					if (mCode == null || mCode.trim().length() == 0) {
						valueXmlString.append("<comm_perc protect = \"1\">").append("<![CDATA[" + commPerc + "]]>")
						.append("</comm_perc>");
						setNodeValue(dom, "comm_perc", getAbsString(commPerc));

						valueXmlString.append("<comm_perc__on protect = \"1\">")
						.append("<![CDATA[" + commPercOn + "]]>").append("</comm_perc__on>");
						setNodeValue(dom, "comm_perc__on", getAbsString(commPercOn));

						valueXmlString.append("<curr_code__comm protect = \"1\">")
						.append("<![CDATA[" + currCodeComm + "]]>").append("</curr_code__comm>");
						setNodeValue(dom, "curr_code__comm", getAbsString(currCodeComm));

						valueXmlString.append("<exch_rate__comm protect = \"1\">")
						.append("<![CDATA[" + exchRateComm + "]]>").append("</exch_rate__comm>");
						setNodeValue(dom, "exch_rate__comm", getAbsString(String.valueOf(exchRateComm)));
					}
				} else if (currentColumn.trim().equalsIgnoreCase("sales_pers__1")) {
					String commPerc = "", commPercOn = "", currCodeComm = "";
					double exchRateComm = 0.00;

					mCode = checkNull(genericUtility.getColumnValue("sales_pers__1", dom));

					sql = "select  (case when emp_code is null then '' else emp_code end) as emp_code from sales_pers where sales_pers = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsEmpCode = rs.getString("emp_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsEmpCode != null && lsEmpCode.trim().length() > 0) {
						lsCctrCodeSal = checkNull(genericUtility.getColumnValue("cctr_code__sal", dom));
						// Modified by Anjali R. [To get cctr_code__sal if it is null in
						// dom]on[24/11/2017][Start]
						// if (lsCctrCodeSal != null && lsCctrCodeSal.trim().length() > 0)
						if (lsCctrCodeSal == null || lsCctrCodeSal.trim().length() == 0)
							// Modified by Anjali R. [To get cctr_code__sal if it is null in
							// dom]on[24/11/2017][End]
						{
							sql = "select (case when cctr_code__sal is null then '' else cctr_code__sal end) as cctr_code__sal from employee where emp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsEmpCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCctrCodeSal = rs.getString("cctr_code__sal");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + lsCctrCodeSal + "]]>")
							.append("</cctr_code__sal>");
							setNodeValue(dom, "cctr_code__sal", getAbsString(lsCctrCodeSal));
						}
					}
					sql = "select count(*) as cnt from employee where emp_code = ? and resi_date is null";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsEmpCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					commPerc = checkNull(genericUtility.getColumnValue("comm_perc_1", dom));
					commPercOn = checkNull(genericUtility.getColumnValue("comm_perc__on_1", dom));
					currCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm_1", dom));
					exchRateComm = Double
							.parseDouble(genericUtility.getColumnValue("exch_rate__comm_1", dom) == null ? "0.00"
									: genericUtility.getColumnValue("exch_rate__comm_1", dom));

					valueXmlString.append("<comm_perc_1 protect = \"0\">").append("<![CDATA[" + commPerc + "]]>")
					.append("</comm_perc_1>");
					setNodeValue(dom, "comm_perc_1", getAbsString(commPerc));

					valueXmlString.append("<comm_perc__on_1 protect = \"0\">").append("<![CDATA[" + commPercOn + "]]>")
					.append("</comm_perc__on_1>");
					setNodeValue(dom, "comm_perc__on_1", getAbsString(commPercOn));

					valueXmlString.append("<curr_code__comm_1 protect = \"0\">")
					.append("<![CDATA[" + currCodeComm + "]]>").append("</curr_code__comm_1>");
					setNodeValue(dom, "curr_code__comm_1", getAbsString(currCodeComm));

					valueXmlString.append("<exch_rate__comm_1 protect = \"0\">")
					.append("<![CDATA[" + exchRateComm + "]]>").append("</exch_rate__comm_1>");
					setNodeValue(dom, "exch_rate__comm_1", getAbsString(String.valueOf(exchRateComm)));

					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));

					lsSordCommCal = distCommon.getDisparams("999999", "SORD_COMM_CAL", conn);
					if (lsSordCommCal == null || lsSordCommCal.trim().length() == 0
							|| lsSordCommCal.equalsIgnoreCase("NULLFOUND")) {
						lsSordCommCal = "H";
					}

					if (lsSordCommCal.equalsIgnoreCase("H")) {
						Timestamp orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						sql = "select comm_table__1 from   customer_series where  cust_code = ? and item_ser  = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable1 = rs.getString("comm_table__1");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsCommTable1 == null || lsCommTable1.trim().length() == 0) {
							sql = "select comm_table__1 from sales_pers where  sales_pers =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommTable1 = rs.getString("comm_table__1");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsCommTable1 == null || lsCommTable1.trim().length() == 0) {
							sql = "select comm_table__1 from customer where  cust_code =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommTable1 = rs.getString("comm_table__1");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsCommTable1 != null && lsCommTable1.trim().length() > 0) {
							sql = "select count(*) as cnt from	comm_det where  comm_table = ? and item_ser	= ? and	eff_date   <= ? and valid_upto >= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable1);
							pstmt.setString(2, lsItemser);
							pstmt.setTimestamp(3, orderDate);
							pstmt.setTimestamp(4, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								sql = "select (case when comm_perc is null then 0 else comm_perc end) as comm_perc , comm_perc__on"
										+ " from comm_det where comm_table = ? and item_ser= ? and eff_date   <= ? and valid_upto >=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCommTable1);
								pstmt.setString(2, lsItemser);
								pstmt.setTimestamp(3, orderDate);
								pstmt.setTimestamp(4, orderDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsCommPerc = rs.getDouble("comm_perc");
									lsCommPercOn = rs.getString("comm_perc__on");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						if (((lsCommPerc) == 0) || (lsCommPercOn.trim().length() == 0)) {
							sql = "select (case when comm_perc is null then 0 else comm_perc end) as comm_perc, comm_perc__on from customer_series where  cust_code = ? and item_ser  =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							pstmt.setString(2, lsItemser);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommPerc = rs.getDouble("comm_perc");
								lsCommPercOn = rs.getString("comm_perc__on");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (lsCommPerc == 0) {
								sql = "select comm_perc,comm_perc__on from   sales_pers where  sales_pers =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsCommPerc = rs.getDouble("comm_perc");
									lsCommPercOn = rs.getString("comm_perc__on");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}
					sql = "select sp_name, curr_code from sales_pers where sales_pers =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("sp_name");
						mCcurr = rs.getString("curr_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					//valueXmlString.append("<sp_name>").append("<![CDATA[" + descr + "]]>").append("</sp_name>");
					//setNodeValue(dom, "sp_name", getAbsString(descr));

					// start here..Modified by kailasG on 21-aug-2019 [set blank name in sales person 2 & sales person 3]
					valueXmlString.append("<sp_name2>").append("<![CDATA[" + descr + "]]>").append("</sp_name2>");
					setNodeValue(dom, "sp_name2", getAbsString(descr));

					// End here..Modified by kailasG on 21-aug-2019 [set blank name in sales person 2 & sales person 3]

					valueXmlString.append("<comm_perc_1>").append("<![CDATA[" + lsCommPerc + "]]>")
					.append("</comm_perc_1>");
					setNodeValue(dom, "comm_perc_1", getAbsString(String.valueOf(lsCommPerc)));

					if ((lsCommPerc) > 0) {
						valueXmlString.append("<comm_perc__on_1>").append("<![CDATA[" + lsCommPercOn + "]]>")
						.append("</comm_perc__on_1>");
						setNodeValue(dom, "comm_perc__on_1", getAbsString(String.valueOf(lsCommPercOn)));
					}
					valueXmlString.append("<curr_code__comm_1>").append("<![CDATA[" + mCcurr + "]]>")
					.append("</curr_code__comm_1>");
					setNodeValue(dom, "curr_code__comm_1", getAbsString(mCcurr));

					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "curr_code__comm", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					if (mCode == null || mCode.trim().length() == 0) {
						valueXmlString.append("<comm_perc_1 protect = \"1\">").append("<![CDATA[" + commPerc + "]]>")
						.append("</comm_perc_1>");
						setNodeValue(dom, "comm_perc_1", getAbsString(commPerc));

						valueXmlString.append("<comm_perc__on_1 protect = \"1\">")
						.append("<![CDATA[" + commPercOn + "]]>").append("</comm_perc__on_1>");
						setNodeValue(dom, "comm_perc__on_1", getAbsString(commPercOn));

						valueXmlString.append("<curr_code__comm_1 protect = \"1\">")
						.append("<![CDATA[" + currCodeComm + "]]>").append("</curr_code__comm_1>");
						setNodeValue(dom, "curr_code__comm_1", getAbsString(currCodeComm));

						valueXmlString.append("<exch_rate__comm_1 protect = \"1\">")
						.append("<![CDATA[" + exchRateComm + "]]>").append("</exch_rate__comm_1>");
						setNodeValue(dom, "exch_rate__comm_1", getAbsString(String.valueOf(exchRateComm)));
					}
				} else if (currentColumn.trim().equalsIgnoreCase("sales_pers__2")) {
					String descr1 = "";
					double mNum = 0.00;
					String commPerc = "", commPercOn = "", currCodeComm = "";
					double exchRateComm = 0.00;

					lsSalesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom));

					sql = "select (case when emp_code is null then '' else emp_code end) as ls_emp_code from sales_pers where sales_pers = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSalesPers2);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsEmpCode = rs.getString("ls_emp_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsEmpCode != null && lsEmpCode.trim().length() > 0) {
						lsCctrCodeSal = checkNull(genericUtility.getColumnValue("cctr_code__sal", dom));
						// Modified by Anjali R. [To get cctr_code__sal if it is null in
						// dom]on[24/11/2017][Start]
						// if (lsCctrCodeSal != null && lsCctrCodeSal.trim().length() > 0)
						if (lsCctrCodeSal == null || lsCctrCodeSal.trim().length() == 0)
							// Modified by Anjali R. [To get cctr_code__sal if it is null in
							// dom]on[24/11/2017][End]
						{
							sql = "select (case when cctr_code__sal is null then '' else cctr_code__sal end) as ls_cctr_code__sal"
									+ " from employee where emp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsEmpCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCctrCodeSal = rs.getString("ls_cctr_code__sal");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + lsCctrCodeSal + "]]>")
							.append("</cctr_code__sal>");
							setNodeValue(dom, "cctr_code__sal", getAbsString(lsCctrCodeSal));
						}
					}

					commPerc = checkNull(genericUtility.getColumnValue("comm_perc_2", dom));
					commPercOn = checkNull(genericUtility.getColumnValue("comm_perc_on_2", dom));
					currCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm_2", dom));
					exchRateComm = Double
							.parseDouble(genericUtility.getColumnValue("exch_rate__comm_2", dom) == null ? "0.00"
									: genericUtility.getColumnValue("exch_rate__comm_2", dom));

					valueXmlString.append("<comm_perc_2 protect = \"0\">").append("<![CDATA[" + commPerc + "]]>")
					.append("</comm_perc_2>");
					setNodeValue(dom, "comm_perc_2", getAbsString(commPerc));

					valueXmlString.append("<comm_perc_on_2 protect = \"0\">").append("<![CDATA[" + commPercOn + "]]>")
					.append("</comm_perc_on_2>");
					setNodeValue(dom, "comm_perc_on_2", getAbsString(commPercOn));

					valueXmlString.append("<curr_code__comm_2 protect = \"0\">")
					.append("<![CDATA[" + currCodeComm + "]]>").append("</curr_code__comm_2>");
					setNodeValue(dom, "curr_code__comm_2", getAbsString(currCodeComm));

					valueXmlString.append("<exch_rate__comm_2 protect = \"0\">")
					.append("<![CDATA[" + exchRateComm + "]]>").append("</exch_rate__comm_2>");
					setNodeValue(dom, "exch_rate__comm_2", getAbsString(String.valueOf(exchRateComm)));

					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));

					lsSordCommCal = distCommon.getDisparams("999999", "SORD_COMM_CAL", conn);
					if (lsSordCommCal == null || lsSordCommCal.trim().length() == 0
							|| lsSordCommCal.equalsIgnoreCase("NULLFOUND")) {
						lsSordCommCal = "H";
					}
					if (lsSordCommCal.equalsIgnoreCase("H")) {
						Timestamp orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");

						sql = "select comm_table__3  from customer_series where  cust_code = ?and 	 item_ser  =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable3 = rs.getString("comm_table__3");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsCommTable3 == null || lsCommTable3.trim().length() == 0) {
							sql = "select comm_table__3 from   sales_pers where  sales_pers = ?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, lsSalesPers1);
							pstmt.setString(1, lsSalesPers2);// Modified by kailasG on 21-aug-2019 [set blank name in sales person 2]

							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsCommTable3 = rs.getString("comm_table__3");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (lsCommTable3 == null || lsCommTable3.trim().length() == 0) {
								sql = "select comm_table__3 from   customer where  cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCustCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsCommTable3 = rs.getString("comm_table__3");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						} else if (lsCommTable3 != null || lsCommTable3.trim().length() > 0) {
							sql = "select count(*) as cnt from comm_det where comm_table = ? and item_ser= ? and eff_date <= ? and valid_upto >= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable3);
							pstmt.setString(2, mItemSer);
							pstmt.setTimestamp(3, orderDate);
							pstmt.setTimestamp(4, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0) {
								sql = "select (case when comm_perc is null then 0 else comm_perc end)as comm_perc , comm_perc__on"
										+ " from comm_det where comm_table = ? and item_ser	= ? and eff_date <= ? and valid_upto >=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCommTable3);
								pstmt.setString(2, mItemSer);
								pstmt.setTimestamp(3, orderDate);
								pstmt.setTimestamp(4, orderDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									mNum = rs.getDouble("comm_perc");
									lsCommPercOn = rs.getString("comm_perc__on");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						if ((mNum) == 0) {
							sql = "select (case when comm_perc__2 is null then 0 else comm_perc__2 end)as comm_perc__2 , comm_perc__on_2 from   customer_series where  cust_code = ? and    item_ser  =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable3);
							pstmt.setString(2, mItemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mNum = rs.getDouble("comm_perc__2");
								descr1 = rs.getString("comm_perc__on_2");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if ((mNum) == 0 || (descr1 == null || descr1.trim().length() == 0)) {
								sql = "select comm_perc,comm_perc__on from   sales_pers where  sales_pers =?";
								pstmt = conn.prepareStatement(sql);
								//pstmt.setString(1, lsSalesPers1);
								pstmt.setString(1, lsSalesPers2);// Modified by kailasG on 21-aug-2019 [set blank name in sales person 2]
								rs = pstmt.executeQuery();
								if (rs.next()) {
									mNum = rs.getDouble("comm_perc");
									descr1 = rs.getString("comm_perc__on");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}

					sql = "select sp_name, curr_code from sales_pers where sales_pers =?";
					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1, lsSalesPers1);
					pstmt.setString(1, lsSalesPers2);// Modified by kailasG on 21-aug-2019 [set blank name in sales person 2]
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsSpName = rs.getString("sp_name");
						mCcurr = rs.getString("curr_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					/*valueXmlString.append("<sales_pers_sp_name_1>").append("<![CDATA[" + lsSpName + "]]>")
							.append("</sales_pers_sp_name_1>");
					setNodeValue(dom, "sales_pers_sp_name_1", getAbsString(lsSpName));*/
					valueXmlString.append("<sp_name3>").append("<![CDATA[" + lsSpName + "]]>").append("</sp_name3>");
					setNodeValue(dom, "sp_name3", getAbsString(lsSpName));
					valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + mCcurr + "]]>")
					.append("</comm_perc_2>");
					setNodeValue(dom, "comm_perc_2", getAbsString(mCcurr));

					if ((mNum) > 0) {
						valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + descr1 + "]]>")
						.append("</comm_perc_2>");
						setNodeValue(dom, "comm_perc_2", getAbsString(descr1));
					}
					valueXmlString.append("<curr_code__comm_2>").append("<![CDATA[" + mCcurr + "]]>")
					.append("</curr_code__comm_2>");
					setNodeValue(dom, "curr_code__comm_2", getAbsString(mCcurr));

					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "curr_code__comm2", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					if (mCode == null || mCode.trim().length() == 0) {
						valueXmlString.append("<comm_perc_2 protect = \"1\">").append("<![CDATA[" + commPerc + "]]>")
						.append("</comm_perc_2>");
						setNodeValue(dom, "comm_perc_2", getAbsString(commPerc));

						valueXmlString.append("<comm_perc_on_2 protect = \"1\">")
						.append("<![CDATA[" + commPercOn + "]]>").append("</comm_perc_on_2>");
						setNodeValue(dom, "comm_perc_on_2", getAbsString(commPercOn));

						valueXmlString.append("<curr_code__comm_2 protect = \"1\">")
						.append("<![CDATA[" + currCodeComm + "]]>").append("</curr_code__comm_2>");
						setNodeValue(dom, "curr_code__comm_2", getAbsString(currCodeComm));

						valueXmlString.append("<exch_rate__comm_2 protect = \"1\">")
						.append("<![CDATA[" + exchRateComm + "]]>").append("</exch_rate__comm_2>");
						setNodeValue(dom, "exch_rate__comm_2", getAbsString(String.valueOf(exchRateComm)));
					}
				} else if (currentColumn.trim().equalsIgnoreCase("cust_code__bil")) {
					String custCodeBill = "", custName = "", mcrTerm = "", mCurr = "", mBankCode = "", mRcpMode = "",
							lsChqName = "", mCurrDescr = "", lsDescr1 = "", lsDescr2 = "";
					String lsDescr3 = "", lsDescr4 = "", lsMaddr3 = "";
					double mNum = 0.00;
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String chgDtStr = "";
					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
					System.out.println("custCodeBill getting from dom["+custCodeBill+"]");
					//Added by mayur on 26-July-2018 -- [start]
					//sql = "select cust_name from customer where cust_code = ?";
					sql = "select cust_name,addr1,addr2,addr3,city,state_code,pin," +
							"cst_no,lst_no,drug_lic_no,drug_lic_no_1,drug_lic_no_2 from customer where cust_code =? ";
					System.out.println("sql ["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCodeBill);													
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						custName = rs.getString("cust_name");		
						billAddr1 = rs.getString("addr1");   
						billAddr2 = rs.getString("addr2");
						billAddr3 = rs.getString("addr3");
						billCity = rs.getString("city");
						stateCode1 = rs.getString("state_code");
						billPin = rs.getString("pin");
						cstNoBill = rs.getString("cst_no");
						lstNoBill = rs.getString("lst_no");
						drugLicNo = rs.getString("drug_lic_no");
						drugLicNo1 = rs.getString("drug_lic_no_1");
						drugLicNo2 = rs.getString("drug_lic_no_2");

						System.out.println("custName["+custName+"]");
						//System.out.println("billAddr1["+billAddr1+"]");
						//System.out.println("billAddr2["+billAddr2+"]");
						//System.out.println("billAddr3["+billAddr3+"]");
						System.out.println("billCity["+billCity+"]");
						System.out.println("stateCode["+stateCode1+"]");
						System.out.println("billPin["+billPin+"]");
						//System.out.println("cstNoBill["+cstNoBill+"]");
						//System.out.println("lstNoBill["+lstNoBill+"]");
						//System.out.println("drugLicNo["+drugLicNo+"]");
						//System.out.println("drugLicNo1["+drugLicNo1+"]");
						//System.out.println("drugLicNo2["+drugLicNo2+"]");							
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					
					//added by Pavan R on 11jun2k18 [to set taxes based on a cust_tax_opt parameter in order type]										
					custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					taxClassHdr = checkNull(genericUtility.getColumnValue("tax_class", dom));
					taxChapHdr = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					taxEnvHdr = checkNull(genericUtility.getColumnValue("tax_env", dom));					
					siteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
					mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));						
					orderType = checkNull(genericUtility.getColumnValue("order_type",dom));										
					//Commented and Added by sarita to set cr_term_source on 15 NOV 2018 [START]
					//sql = "select cust_tax_opt from sordertype where order_type = ? ";
					sql = "select cust_tax_opt,cr_term_source from sordertype where order_type = ? ";
					//Commented and Added by sarita to set cr_term_source on 15 NOV 2018 [END]
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
						//Added by sarita on 15 NOV 2018 [START]
						crTermSource = checkNull(rs.getString("cr_term_source"));
						//Added by sarita on 15 NOV 2018 [END]
					}				
					//Added and commented by sarita on 15 NOV 2018 [START]
					/*pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;		*/
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					//Added and commented by sarita on 15 NOV 2018 [END]
					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}
					System.out.println("cust_code__bil::custTaxOpt["+custTaxOpt+"]custCodeDlv["+custCodeDlv+"]custCodeBill["+custCodeBill+"]orderType["+orderType+"]");					
					sql = "select stan_code from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeShip);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						stanCodeFr = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;										
					sql = "select stan_code from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);				 
					if("1".equals(custTaxOpt))	{
						pstmt.setString(1, custCodeBill);
						custCodeTax = custCodeBill;
					}else if("0".equals(custTaxOpt)) {						
						pstmt.setString(1, custCodeDlv);
						custCodeTax = custCodeDlv;
					}	
					rs = pstmt.executeQuery();
					if (rs.next()) 	{						
						stanCodeTo = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					
					//if (taxChapHdr == null || taxChapHdr.trim().length() == 0) {
					taxChapHdr = distCommon.getTaxChap("", mItemSer, "C", custCodeTax, siteCodeShip, conn);
					//}
					//if (taxClassHdr == null || taxClassHdr.trim().length() == 0) {					
					taxClassHdr = distCommon.getTaxClass("C", custCodeTax, "", siteCodeShip, conn);
					//}
					//if (taxEnvHdr == null || taxEnvHdr.trim().length() == 0) {
					taxEnvHdr = distCommon.getTaxEnv(stanCodeFr, stanCodeTo, taxChapHdr, taxClassHdr, siteCodeShip, conn);
					//}										
					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClassHdr + "]]>").append("</tax_class>");
					setNodeValue(dom, "tax_class", getAbsString(taxClassHdr));					
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChapHdr + "]]>").append("</tax_chap>");
					setNodeValue(dom, "tax_chap", getAbsString(taxChapHdr));					
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnvHdr + "]]>").append("</tax_env>");
					setNodeValue(dom, "tax_env", getAbsString(taxEnvHdr));		
					System.out.println("cust_code__bil::taxChapHdr["+taxChapHdr+"]taxClassHdr["+taxClassHdr+"]taxEnvHdr["+taxEnvHdr+"]");
					//added by Pavan R on 11jun2k18 End					
					valueXmlString.append("<cust_name__bil>").append("<![CDATA[" + custName + "]]>")
					.append("</cust_name__bil>");
					setNodeValue(dom, "cust_name__bil", getAbsString(custName));

					//Added by mayur on 26-July-2018 -- [start]--to get the billing details populated when the cust code bill is manually set 
					valueXmlString.append("<bill_addr1>").append("<![CDATA[" + billAddr1 + "]]>")
					.append("</bill_addr1>");
					setNodeValue(dom, "bill_addr1", getAbsString(billAddr1));

					valueXmlString.append("<bill_addr2>").append("<![CDATA[" + billAddr2 + "]]>")
					.append("</bill_addr2>");
					setNodeValue(dom, "bill_addr2", getAbsString(billAddr2));

					valueXmlString.append("<bill_addr3>").append("<![CDATA[" + billAddr3 + "]]>")
					.append("</bill_addr3>");
					setNodeValue(dom, "bill_addr3", getAbsString(billAddr3));

					valueXmlString.append("<bill_city>").append("<![CDATA[" + billCity + "]]>")
					.append("</bill_city>");
					setNodeValue(dom, "bill_city", getAbsString(billCity));

					valueXmlString.append("<state_code__bill>").append("<![CDATA[" + stateCode1 + "]]>")
					.append("</state_code__bill>");
					setNodeValue(dom, "state_code__bill", getAbsString(stateCode1));

					valueXmlString.append("<bill_pin>").append("<![CDATA[" + billPin + "]]>")
					.append("</bill_pin>");
					setNodeValue(dom, "bill_pin", getAbsString(billPin));

					valueXmlString.append("<cst_no__bill>").append("<![CDATA[" + cstNoBill + "]]>")
					.append("</cst_no__bill>");
					setNodeValue(dom, "cst_no__bill", getAbsString(cstNoBill));

					valueXmlString.append("<lst_no__bill>").append("<![CDATA[" + lstNoBill + "]]>")
					.append("</lst_no__bill>");
					setNodeValue(dom, "lst_no__bill", getAbsString(lstNoBill));

					valueXmlString.append("<drug_lic_no__bill>").append("<![CDATA[" + drugLicNo + "]]>")
					.append("</drug_lic_no__bill>");
					setNodeValue(dom, "drug_lic_no__bill", getAbsString(drugLicNo));		

					valueXmlString.append("<drug_lic_no_1__bill>").append("<![CDATA[" + drugLicNo1 + "]]>")
					.append("</drug_lic_no_1__bill>");
					setNodeValue(dom, "drug_lic_no_1__bill", getAbsString(drugLicNo1));

					valueXmlString.append("<drug_lic_no_2__bill>").append("<![CDATA[" + drugLicNo2 + "]]>")
					.append("</drug_lic_no_2__bill>");
					setNodeValue(dom, "drug_lic_no_2__bill", getAbsString(drugLicNo2));			        			           
					//Added by mayur on 26-July-2018 -- [end]

					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
					lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);

					if (lsOrderType == null || lsOrderType.trim().length() == 0) {
						valueXmlString.append("<order_type>").append("<![CDATA[]]>").append("</order_type>");
						setNodeValue(dom, "order_type", getAbsString(""));
						valueXmlString.append("<descr>").append("<![CDATA[]]>").append("</descr>");
						setNodeValue(dom, "descr", getAbsString(""));
					}

					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

					lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
					boolean lbOrdFlag = false;
					String lsDisIndOrdtypeListArr[] = lsDisIndOrdtypeList.split(",");
					if (lsDisIndOrdtypeListArr.length > 0) {
						for (int i = 0; i < lsDisIndOrdtypeListArr.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsDisIndOrdtypeListArr[i])) {
								lbOrdFlag = true;
							}
						}
					}
					System.out.println("Inside cust_code__bil....lbOrdFlag["+lbOrdFlag+"]");
					if (lbOrdFlag) {
						sql = "select cr_term from customer_series where cust_code = ? and item_ser= ?";
						pstmt = conn.prepareStatement(sql);
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
						//pstmt.setString(1, lsCustCode); //changed by Pavan R on 16aug18 [to set cr_term on billto/dlvto]
						/*if("1".equals(custTaxOpt))	{
							pstmt.setString(1, custCodeBill);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, custCodeDlv);							
						}*/

						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeBill1);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else 
						{
							pstmt.setString(1, lsCustCode);
						}
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						//Added and commented by sarita on 15 NOV 2018 [START]
						/*pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;		*/
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						//Added and commented by sarita on 15 NOV 2018 [END]

						if (mcrTerm == null || mcrTerm.trim().length() == 0) {
							sql = "select cr_term from customer where cust_code =  ?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, lsCustCode);
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START]
							/*if("1".equals(custTaxOpt))	{
								pstmt.setString(1, custCodeBill);								
							}else if("0".equals(custTaxOpt)) {						
								pstmt.setString(1, custCodeDlv);								
							}*/

							if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeBill1);
							}
							else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeDlv);
							}
							else 
							{
								pstmt.setString(1, lsCustCode);
							}

							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END]
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mcrTerm = rs.getString("cr_term");
							}
							//Added and commented by sarita on 15 NOV 2018 [START]
							/*pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;		*/
							if(rs != null)
							{
								rs.close();
								rs = null;
							}
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							//Added and commented by sarita on 15 NOV 2018 [END]
						}
					} else {
						sql = "select cr_term from customer_series where cust_code = ? and item_ser= ?";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1, lsCustCode);
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
						/*if("1".equals(custTaxOpt))	{
							pstmt.setString(1, custCodeBill);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, custCodeDlv);							
						}*/
						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeBill1);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else 
						{
							pstmt.setString(1, lsCustCode);
						}
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						//Added and commented by sarita on 15 NOV 2018 [START]
						/*pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;		*/
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						//Added and commented by sarita on 15 NOV 2018 [END]

						if (mcrTerm == null || mcrTerm.trim().length() == 0) {
							sql = "select cr_term from customer where cust_code =  ?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, lsCustCode);
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
							/*if("1".equals(custTaxOpt))	{
								pstmt.setString(1, custCodeBill);								
							}else if("0".equals(custTaxOpt)) {						
								pstmt.setString(1, custCodeDlv);								
							}*/
							if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeBill);
							}
							else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeDlv);
							}
							else 
							{
								pstmt.setString(1, lsCustCode);
							}		
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mcrTerm = rs.getString("cr_term");
							}
							//Added and commented by sarita on 15 NOV 2018 [START]
							/*pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;		*/
							if(rs != null)
							{
								rs.close();
								rs = null;
							}
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							//Added and commented by sarita on 15 NOV 2018 [END]
						}
					}
					if (!lbOrdFlag) {
						sql = "select cr_term from customer where cust_code =  ?";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1, custCodeBill);
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
						/*if("1".equals(custTaxOpt))	{
							pstmt.setString(1, custCodeBill);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, custCodeDlv);							
						}*/
						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeBill);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else 
						{
							pstmt.setString(1, lsCustCode);
						}					
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [END] 
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						//Added and commented by sarita on 15 NOV 2018 [START]
						/*pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;		*/
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						//Added and commented by sarita on 15 NOV 2018 [END]
					}

					if (lsOrderType != null && lsOrderType.trim().length() > 0) {
						valueXmlString.append("<order_type>").append("<![CDATA[" + lsOrderType + "]]>")
						.append("</order_type>");
						setNodeValue(dom, "order_type", getAbsString(lsOrderType));
					}
					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "order_type", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					sql = "SELECT CR_TERM_MAPPING.CR_TERM_MAP FROM CR_TERM_MAPPING"
							+ " WHERE ( CR_TERM_MAPPING.CR_TERM = ? ) AND  (CR_TERM_MAPPING.ORD_TYPE = ?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcrTerm);
					pstmt.setString(2, lsOrderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsCrTerm = rs.getString("CR_TERM_MAP");
						mcrTerm = lsCrTerm;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrTerm + "]]>").append("</cr_term>");

					sql = "select descr from crterm where cr_term =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcrTerm);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + descr + "]]>").append("</crterm_descr>");
					//commented as cr_trem desc set instead of cr_trerm 
					/*sql = "select descr from crterm where cr_term =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcrTerm);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					 */
					//valueXmlString.append("<cr_term>").append("<![CDATA[" + descr + "]]>").append("</cr_term>");

					sql = "select curr_code , bank_code , rcp_mode, chq_name from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCodeBill);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mCurr = rs.getString("curr_code");
						mBankCode = checkNull(rs.getString("bank_code"));
						mRcpMode = checkNull(rs.getString("rcp_mode"));
						lsChqName = checkNull(rs.getString("chq_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					lsTypeAllowCrLmtList = distCommon.getDisparams("999999", "TYPE_ALLOW_CR_LMT", conn);
					if (lsTypeAllowCrLmtList == null || lsTypeAllowCrLmtList.trim().length() == 0
							|| lsTypeAllowCrLmtList.equalsIgnoreCase("NULLFOUND")) {
						lbOrdFlag = false;
					} else {
						String lsTypeAllowCrLmt[] = lsTypeAllowCrLmtList.split(",");
						for (int i = 0; i < lsTypeAllowCrLmt.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsTypeAllowCrLmt[i])) {
								lbOrdFlag = true;
								break;
							}
						}
					}
					if (lbOrdFlag) {
						sql = "select CREDIT_LMT from customer_series where cust_code = ? and 	item_ser  = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcCreditLmt = rs.getString("CREDIT_LMT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						sql = "select credit_lmt from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcCreditLmt = rs.getString("credit_lmt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<curr_code>").append("<![CDATA[" + mCurr + "]]>").append("</curr_code>");
					valueXmlString.append("<bank_code>").append("<![CDATA[" + mBankCode + "]]>").append("</bank_code>");
					valueXmlString.append("<rcp_mode>").append("<![CDATA[" + mRcpMode + "]]>").append("</rcp_mode>");
					valueXmlString.append("<chq_name>").append("<![CDATA[" + lsChqName + "]]>").append("</chq_name>");
					valueXmlString.append("<cr_lmt>").append("<![CDATA[" + lcCreditLmt + "]]>").append("</cr_lmt>");

					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCurr);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mCurrDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					Timestamp orderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
					//String chgDtStr=genericUtility.getValidDateString(orderDate, genericUtility.getDBDateFormat());
					chgDtStr = sdf.format(orderDate);					
					mNum = finCommon.getDailyExchRateSellBuy(mCurr, "", lsSiteCode, chgDtStr, "S", conn);
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + mNum + "]]>").append("</exch_rate>");
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
					//exchangeRateProtect(mCurr, lsSiteCode, "exch_rate", conn);
					String retVal = exchangeRateProtect(mCurr, lsSiteCode, "exch_rate", conn);
					System.out.println("retVal--455["+retVal+"]");
					valueXmlString.append(retVal);
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]

					if (lbOrdFlag) {
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + custCodeBill + "]]>")
						.append("</cust_code__dlv>");

						sql = "select	cust_name, addr1, addr2,addr3, city, pin ,stan_code,state_code from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCodeBill);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							custName = rs.getString("cust_name");
							lsDescr1 = rs.getString("addr1");
							lsDescr2 = rs.getString("addr2");
							lsMaddr3 = rs.getString("addr3");
							lsDescr3 = rs.getString("city");
							lsDescr4 = rs.getString("pin");
							lsStanCode = rs.getString("stan_code");
							lsStateCode = rs.getString("state_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<dlv_to>").append("<![CDATA[" + custName + "]]>").append("</dlv_to>");
						valueXmlString.append("<dlv_add1>").append("<![CDATA[" + lsDescr1 + "]]>")
						.append("</dlv_add1>");
						valueXmlString.append("<dlv_add2>").append("<![CDATA[" + lsDescr2 + "]]>")
						.append("</dlv_add2>");
						valueXmlString.append("<dlv_add3>").append("<![CDATA[" + lsMaddr3 + "]]>")
						.append("</dlv_add3>");
						valueXmlString.append("<dlv_city>").append("<![CDATA[" + lsDescr3 + "]]>")
						.append("</dlv_city>");
						valueXmlString.append("<dlv_pin>").append("<![CDATA[" + lsDescr4 + "]]>").append("</dlv_pin>");
						valueXmlString.append("<stan_code>").append("<![CDATA[" + lsStanCode + "]]>")
						.append("</stan_code>");
						valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + lsStateCode + "]]>")
						.append("</state_code__dlv>");
					}
				} else if (currentColumn.trim().equalsIgnoreCase("emp_code__ord")) {
					String empCodeOrd = "", fname = "", lname = "", deptCode = "";
					empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord", dom));

					if (empCodeOrd != null && empCodeOrd.trim().length() > 0) {
						sql = "select emp_fname, emp_lname , dept_code from employee where emp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							fname = rs.getString("emp_fname");
							lname = rs.getString("emp_lname");
							deptCode = rs.getString("dept_code");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						fname = "";
						lname = "";
						// deptCode= deptCode;
					}
					valueXmlString.append("<emp_fname>").append("<![CDATA[" + fname + "]]>").append("</emp_fname>");
					valueXmlString.append("<emp_lname>").append("<![CDATA[" + lname + "]]>").append("</emp_lname>");
					valueXmlString.append("<dept_code>").append("<![CDATA[" + deptCode + "]]>").append("</dept_code>");

				} else if (currentColumn.trim().equalsIgnoreCase("emp_code__ord1")) {
					String empCodeOrd = "", fname = "", lname = "", deptCode = "";
					empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord1", dom));
					if (empCodeOrd != null && empCodeOrd.trim().length() > 0) {
						sql = "select emp_fname, emp_lname , dept_code from employee where emp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							fname = rs.getString("emp_fname");
							lname = rs.getString("emp_lname");
							// deptCode = rs.getString("dept_code");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						fname = "";
						lname = "";
						// deptCode= deptCode;
					}

					valueXmlString.append("<employee_emp_fname>").append("<![CDATA[" + fname + "]]>")
					.append("</employee_emp_fname>");
					valueXmlString.append("<employee_emp_lname>").append("<![CDATA[" + lname + "]]>")
					.append("</employee_emp_lname>");
				} else if (currentColumn.trim().equalsIgnoreCase("curr_code__frt")) {
					double mNum = 0.00;
					lsCurrCodeFrt = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					Timestamp orderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String ordDate = sdf.format(orderDate);					
					mNum = finCommon.getDailyExchRateSellBuy(lsCurrCodeFrt, "", lsSiteCode, ordDate, "S",
							conn);
					valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + mNum + "]]>")
					.append("</exch_rate__frt>");
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
					//exchangeRateProtect(lsCurrCodeFrt, lsSiteCode, "exch_rate__frt", conn);
					String retVal = exchangeRateProtect(lsCurrCodeFrt, lsSiteCode, "exch_rate__frt", conn);
					System.out.println("retVal--455["+retVal+"]");
					valueXmlString.append(retVal);
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
				} else if (currentColumn.trim().equalsIgnoreCase("cust_code__dlv")) {
					System.out.println("Inside Cust Code DLV Validation::"+lsCustCodeDlv);
					String custName = "", addr1 = "", addr2 = "", addr3 = "", city = "", pin = "", countCode = "",
							stanCode = "", tranCode = "", stateCode = "", tele1 = "", tele2 = "", tele3 = "", fax1 = "",
							tranName = "", frtTerm = "", lsEmpCodeOrd = "", lsEmpCodeOrd1 = "";
					String lsFname = "", lsLname = "", lsPendingOrder = "", custCodeBill="";
					lsCustCodeDlv = genericUtility.getColumnValue("cust_code__dlv", dom);
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					System.out.println("Inside Cust Code DLV Validation::"+lsCustCodeDlv);
					System.out.println("cust_code__dlv -lsSiteCode[" + lsSiteCode + "]");
					lsItemser = genericUtility.getColumnValue("item_ser", dom);
					sql = "select	cust_name, addr1, addr2,addr3, city, pin, count_code, stan_code,tran_code, state_code, "
							+ "tele1, tele2, tele3,fax, term_table__no,emp_code__ord , emp_code__ord1 from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCodeDlv);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custName = checkNull(rs.getString("cust_name"));
						addr1 = checkNull(rs.getString("addr1"));
						addr2 = checkNull(rs.getString("addr2"));
						addr3 = checkNull(rs.getString("addr3"));
						city = checkNull(rs.getString("city"));
						pin = checkNull(rs.getString("pin"));
						countCode = checkNull(rs.getString("count_code"));
						stanCode = checkNull(rs.getString("stan_code"));
						tranCode = checkNull(rs.getString("tran_code"));
						stateCode = checkNull(rs.getString("state_code"));
						tele1 = checkNull(rs.getString("tele1"));
						tele2 = checkNull(rs.getString("tele2"));
						tele3 = checkNull(rs.getString("tele3"));
						fax1 = checkNull(rs.getString("fax"));
						lsTermTable = checkNull(rs.getString("term_table__no"));
						lsEmpCodeOrd = checkNull(rs.getString("emp_code__ord"));
						lsEmpCodeOrd1 = checkNull(rs.getString("emp_code__ord1"));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<dlv_to>").append("<![CDATA[" + custName + "]]>").append("</dlv_to>");
					valueXmlString.append("<dlv_add1>").append("<![CDATA[" + addr1 + "]]>").append("</dlv_add1>");
					valueXmlString.append("<dlv_add2>").append("<![CDATA[" + addr2 + "]]>").append("</dlv_add2>");
					valueXmlString.append("<dlv_add3>").append("<![CDATA[" + addr3 + "]]>").append("</dlv_add3>");
					valueXmlString.append("<dlv_city>").append("<![CDATA[" + city + "]]>").append("</dlv_city>");
					valueXmlString.append("<dlv_pin>").append("<![CDATA[" + pin + "]]>").append("</dlv_pin>");
					valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countCode + "]]>")
					.append("</count_code__dlv>");
					valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
					valueXmlString.append("<stan_code>").append("<![CDATA[" + stanCode + "]]>").append("</stan_code>");
					valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + stateCode + "]]>")
					.append("</state_code__dlv>");
					valueXmlString.append("<tel1__dlv>").append("<![CDATA[" + tele1 + "]]>").append("</tel1__dlv>");
					valueXmlString.append("<tel2__dlv>").append("<![CDATA[" + tele2 + "]]>").append("</tel2__dlv>");
					valueXmlString.append("<tel3__dlv>").append("<![CDATA[" + tele3 + "]]>").append("</tel3__dlv>");
					valueXmlString.append("<fax__dlv>").append("<![CDATA[" + fax1 + "]]>").append("</fax__dlv>");
					valueXmlString.append("<term_table__no>").append("<![CDATA[" + lsTermTable + "]]>")
					.append("</term_table__no>");

					if (lsEmpCodeOrd == null || lsEmpCodeOrd.trim().length() == 0) {

						valueXmlString
						.append("<emp_code__ord>").append("<![CDATA["
								+ genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode") + "]]>")
						.append("</emp_code__ord>");

						valueXmlString.append("<emp_fname>").append("<![CDATA[]]>").append("</emp_fname>");
						valueXmlString.append("<emp_lname>").append("<![CDATA[]]>").append("</emp_lname>");
					} else {

						valueXmlString.append("<emp_code__ord>").append("<![CDATA[" + lsEmpCodeOrd + "]]>")
						.append("</emp_code__ord>");

						sql = "select emp_fname, emp_lname  from employee where emp_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsEmpCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsFname = rs.getString("emp_fname");
							lsLname = rs.getString("emp_fname");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<emp_fname>").append("<![CDATA[" + lsFname + "]]>")
						.append("</emp_fname>");
						valueXmlString.append("<emp_lname>").append("<![CDATA[" + lsLname + "]]>")
						.append("</emp_lname>");
					}
					//commented by mayur
					//					valueXmlString.append("<emp_code__ord1>").append("<![CDATA[" + lsEmpCodeOrd1 + "]]>")
					//							.append("</emp_code__ord1>");
					if (lsEmpCodeOrd1 == null || lsEmpCodeOrd1.trim().length() == 0) {
						//added by mayur on 23-May-18 --start
						System.out.println("mayur nair456"+lsEmpCodeOrd1);
						valueXmlString.append("<emp_code__ord1><![CDATA[]]></emp_code__ord1>");
						valueXmlString.append("<employee_emp_fname><![CDATA[]]></employee_emp_fname>");
						valueXmlString.append("<employee_emp_lname><![CDATA[]]></employee_emp_lname>");
						//added by mayur on 23-May-18 ---end
						//						valueXmlString.append("<employee_emp_fname>").append("<![CDATA[]]>")
						//						.append("</employee_emp_fname>");
						//				         valueXmlString.append("<employee_emp_lname>").append("<![CDATA[]]>")
						//						.append("</employee_emp_lname>");
					} else {
						System.out.println("mayur nair654"+lsEmpCodeOrd1);
						//added by mayur on 23-May-18 --start
						valueXmlString.append("<emp_code__ord1>").append("<![CDATA[" + lsEmpCodeOrd1 + "]]>")
						.append("</emp_code__ord1>");
						//added by mayur on 23-May-18 --end
						sql = "select emp_fname, emp_lname  from employee where emp_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsEmpCodeOrd1);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsFname = rs.getString("emp_fname");
							lsLname = rs.getString("emp_fname");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<employee_emp_fname>").append("<![CDATA[" + lsFname + "]]>")
						.append("</employee_emp_fname>");
						valueXmlString.append("<employee_emp_lname>").append("<![CDATA[" + lsLname + "]]>")
						.append("</employee_emp_lname>");
					}
					sql = "select descr from station 	where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<station_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</station_descr>");
					//Pavan Rane 10jul19 start [to Tax_env on Cust_code__dlv]
					siteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
					orderType = checkNull(genericUtility.getColumnValue("order_type",dom));					
					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));

					sql = "select cust_tax_opt from sordertype where order_type = ? ";					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));						
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;					
					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}					
					System.out.println("cust_code__dlv::custTaxOpt["+custTaxOpt+"]custCodeDlv["+lsCustCodeDlv+"]custCodeBill["+custCodeBill+"]orderType["+orderType+"]");

					sql = "select stan_code from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeShip);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						stanCodeFr = rs.getString("stan_code");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;										
					sql = "select stan_code from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);				 
					if("1".equals(custTaxOpt))	{
						pstmt.setString(1, custCodeBill);
						custCodeTax = custCodeBill;
					}else if("0".equals(custTaxOpt)) {						
						pstmt.setString(1, lsCustCodeDlv);
						custCodeTax = lsCustCodeDlv;
					}	
					rs = pstmt.executeQuery();
					if (rs.next()) 	{						
						stanCodeTo = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					

					taxChapHdr = distCommon.getTaxChap("", lsItemser, "C", custCodeTax, siteCodeShip, conn);
					taxClassHdr = distCommon.getTaxClass("C", custCodeTax, "", siteCodeShip, conn);
					taxEnvHdr = distCommon.getTaxEnv(stanCodeFr, stanCodeTo, taxChapHdr, taxClassHdr, siteCodeShip, conn);
					System.out.println("cust_code__dlv::taxChapHdr["+taxChapHdr+"]taxClassHdr["+taxClassHdr+"]taxEnvHdr["+taxEnvHdr+"]");															
					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClassHdr + "]]>").append("</tax_class>");
					setNodeValue(dom, "tax_class", getAbsString(taxClassHdr));					
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChapHdr + "]]>").append("</tax_chap>");
					setNodeValue(dom, "tax_chap", getAbsString(taxChapHdr));					
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnvHdr + "]]>").append("</tax_env>");
					setNodeValue(dom, "tax_env", getAbsString(taxEnvHdr));						
					//Pavan Rane 10jul19 end
					sql = "select tran_name, (case when frt_term is null then 'B' else frt_term end) as frt_term	from transporter where tran_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						tranName = rs.getString("tran_name");
						frtTerm = rs.getString("frt_term");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<tran_name>").append("<![CDATA[" + tranName + "]]>").append("</tran_name>");
					valueXmlString.append("<frt_term>").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");

					sql = "select count(*) as cnt from customer_series where cust_code =? and item_ser  =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCodeDlv);
					pstmt.setString(2, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (cnt > 0) {
						sql = "select pending_order from customer_series where cust_code =? and item_ser  =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPendingOrder = rs.getString("pending_order");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsPendingOrder == null || lsPendingOrder.trim().length() == 0) {
						sql = "select pending_order from customer where cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPendingOrder = rs.getString("pending_order");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					//Pavan R 04FEB2019 [if pending_order in customer master is null then consider as 'N'].
					if(lsPendingOrder == null || lsPendingOrder.trim().length() == 0 || "null".equals(lsPendingOrder) )
					{
						lsPendingOrder = "N";
					}					
					valueXmlString.append("<pending_order>").append("<![CDATA[" + lsPendingOrder + "]]>")
					.append("</pending_order>");
					lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);
					lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));


					//Nandkumar Gadkari 29/01/18 Start ------------- CONDITION ADDED FOR  SET PRICE LIST CUST_CODE 

					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

					lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
					boolean lbOrdFlag = false;
					String lsDisIndOrdtypeListArr[] = lsDisIndOrdtypeList.split(",");
					if (lsDisIndOrdtypeListArr.length > 0) {
						for (int i = 0; i < lsDisIndOrdtypeListArr.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsDisIndOrdtypeListArr[i])) {
								lbOrdFlag = true;
							}
						}
					}
					//added by manish mhatre on 24-sep-2019
					//start manish
					sql="SELECT PRICE_BASIS FROM SORDERTYPE where ORDER_TYPE= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,lsOrderType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						priceBasis = checkNullandTrim(rs.getString("PRICE_BASIS"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if((priceBasis.length()>0))
					{
						sql=" SELECT CUST_CODE__BIL,CUST_CODE__DLV FROM CUSTOMER WHERE CUST_CODE = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,lsCustCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							custCodeBilPl = rs.getString("CUST_CODE__BIL");
							custCodeDlv = rs.getString("CUST_CODE__DLV");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(("B").equalsIgnoreCase(priceBasis))
						{
							lsCustCode=custCodeBilPl;
						}
						if(("D").equalsIgnoreCase(priceBasis))
						{
							lsCustCode=custCodeDlv;
						}

						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
						}
					}
					//end manish


					if (lbOrdFlag) {

						System.out.println("insideIfprice_listnandkumar"+lsCustCode);
						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
						}
					} else {
						sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Site Code is" + lsSiteCode);
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							System.out.println("if Price list is null then Site Code is" + lsSiteCode);
							lsPriceList = priceListSite(lsSiteCode, lsCustCodeDlv, conn);
						}
					}

					if (lbOrdFlag) {
						System.out.println("insideIfprice_listnandkumar2price_list__clg"+lsCustCode);
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							pstmt.setString(2, lsSiteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from customer where  cust_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

					}
					else {
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCodeDlv);
							pstmt.setString(2, lsSiteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
							sql = "select price_list__clg from customer where  cust_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCodeDlv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPlistClg = checkNull(rs.getString("price_list__clg"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}



					/*	sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCodeDlv);
					pstmt.setString(2, lsOrderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsPriceList = checkNull(rs.getString("price_list"));
						lsPlistClg = checkNull(rs.getString("price_list__clg"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					System.out.println("Site Code is" + lsSiteCode);
					if (lsPriceList == null || lsPriceList.trim().length() == 0) {
						Timestamp orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
										+ " 00:00:00.0");
						lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
					}
					if (lsPriceList == null || lsPriceList.trim().length() == 0) {
						System.out.println("if Price list is null then Site Code is" + lsSiteCode);
						lsPriceList = priceListSite(lsSiteCode, lsCustCodeDlv, conn);
					}
					if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
						sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}
					if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
						sql = "select price_list__clg from customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCodeDlv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}*/

					//---------------------Nandkumar Gadkari 29/01/18 end  -------------
					if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
						valueXmlString.append("<price_list>").append("<![CDATA[]]>").append("</price_list>");
					} else {
						valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + lsPriceList + "]]>")
						.append("</price_list>");    //protected by manish mhatre on 15-apr-2020
						valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + lsPlistClg + "]]>")
						.append("</price_list__clg>");   //protecetd by manish mhatre on 15-apr-2020
					}
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));   //added by manish mhatre on 24-sep-2019

				} else if (currentColumn.trim().equalsIgnoreCase("cust_code__notify")) {
					valueXmlString.append(
							itmCustCodeNotify(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext, conn));

				} else if (currentColumn.trim().equalsIgnoreCase("cust_code")) 
				{
					System.out.println("--------------Inside itemchange of cust_code------------");					
					String custCode = "", custName="", custNameBill = "", tranMode = "", mbillto = "", lsTerrcode = "", mslpers = "",
							mslPers1 = "", mslPers2 = "", mcrTerm = "";
					String lsTaxclasshdr = "", lsTaxchaphdr = "", lsTaxenvhdr = "", lsSiteCodeShip = "",
							lsStationfr = "", lsStationto = "", mCrdescr = "", lsPendingOrder = "";
					String descr1 = "", descr2 = "", descr3 = "", descr4 = "", mcountry = "", mstan = "", mtran = "",
							maddr3 = "", mTransMode = "", mstate = "", lsSingleLot = "";
					String mcurr = "", mbankCode = "", mrcpMode = "", lsChqName = "", lsCurrcdFrt = "",
							lsCurrcdIns = "", mcurrdescr = "", lsEmailAddr = "", lsFrtterm = "";
					String lsDescrCl = "", orderTypeLs="";
					double mNum = 0.00;

					String retVal = "";//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same]


					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					System.out.println("custCode getting from dom["+custCode+"]");
					lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					//added by Pavan Rane 10jul19 start [On edit taxes are not set based on cust_code__dlv]
					//custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					custCodeDlv = checkNull(getCustCodeDlv(custCode, conn));
					System.out.println("custCode ::::::: "+custCode + "\t" + "custCodeDlv ::::::"+custCodeDlv); 
					if(custCodeDlv != null && custCodeDlv.trim().length() > 0)
					{
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA["+custCodeDlv+"]]>").append("</cust_code__dlv>");						
						setNodeValue(dom, "cust_code__dlv", getAbsString(custCodeDlv));
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					else
					{
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA["+custCode+"]]>").append("</cust_code__dlv>");
						setNodeValue(dom, "cust_code__dlv", getAbsString(custCode));
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					//Pavan R 10jul19 end
					lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);		
					//changes to get cust_code in  
					sql = "select cust_code__bil, cust_name, order_type from customer where cust_code =? ";
					System.out.println("sql["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {						
						mbillto = checkNull(rs.getString("cust_code__bil"));
						custName = checkNull(rs.getString("cust_name"));
						orderTypeLs = checkNull(rs.getString("order_type"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Added by mayur on 26-July-2018---[start]
					//sql = "select cust_name, cust_code__bil from customer where cust_code =? ";
					sql = "select cust_name,addr1,addr2,addr3,city,state_code,pin," +
							"cst_no,lst_no,drug_lic_no,drug_lic_no_1,drug_lic_no_2 from customer where cust_code =? ";

					System.out.println("sql["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mbillto);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custNameBill = checkNull(rs.getString("cust_name"));
						//mbillto = checkNull(rs.getString("cust_code__bil"));	
						billAddr1 = checkNull(rs.getString("addr1"));   
						billAddr2 = checkNull(rs.getString("addr2"));
						billAddr3 = checkNull(rs.getString("addr3"));
						billCity = checkNull(rs.getString("city"));
						stateCode1 = checkNull(rs.getString("state_code"));
						billPin = checkNull(rs.getString("pin"));
						cstNoBill = checkNull(rs.getString("cst_no"));
						lstNoBill = checkNull(rs.getString("lst_no"));
						drugLicNo = checkNull(rs.getString("drug_lic_no"));
						drugLicNo1 = checkNull(rs.getString("drug_lic_no_1"));
						drugLicNo2 = checkNull(rs.getString("drug_lic_no_2"));

						System.out.println("custNameBill["+custNameBill+"]");
						System.out.println("mbillto["+mbillto+"]");
						//System.out.println("billAddr1["+billAddr1+"]");
						//System.out.println("billAddr2["+billAddr2+"]");
						//System.out.println("billAddr3["+billAddr3+"]");
						System.out.println("billCity["+billCity+"]");
						System.out.println("stateCode["+stateCode1+"]");
						System.out.println("billPin["+billPin+"]");
						//System.out.println("cstNoBill["+cstNoBill+"]");
						//System.out.println("lstNoBill["+lstNoBill+"]");
						//System.out.println("drugLicNo["+drugLicNo+"]");
						//System.out.println("drugLicNo1["+drugLicNo1+"]");
						//System.out.println("drugLicNo2["+drugLicNo2+"]");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					orderType = checkNull(genericUtility.getColumnValue("order_type",dom));
					//if condition added to check if initially null from dom

					if(orderType == null || orderType.trim().length() == 0)
					{
						orderType = orderTypeLs;						
					}
					if(custCodeDlv == null || custCodeDlv.trim().length() == 0)
					{
						custCodeDlv = custCode;
					}
					System.out.println("6244orderType::["+orderType+"]custCodeDlv["+custCodeDlv+"]");


					//Commented and Added by sarita to add cr_term_source in sql on 15 NOV 2018 [START]
					//sql = "select cust_tax_opt from sordertype where order_type = ? ";

					sql = "select cust_tax_opt,cr_term_source from sordertype where order_type = ? ";
					//Commented and Added by sarita to add cr_term_source in sql on 15 NOV 2018 [END]
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
						//Added by sarita on 15 NOV 2018 [START]
						crTermSource = checkNull(rs.getString("cr_term_source"));
						//Added by sarita on 15 NOV 2018 [END]
					}
					//Added and commented by sarita on 15 NOV 2018 [START]
					/*pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;		*/
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}

					//Added and commented by sarita on 15 NOV 2018 [END]
					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}
					System.out.println("inside cust_code itemchanged::6251");
					sql = "select terr_code from customer_series where cust_code =? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsTerrcode = rs.getString("terr_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (lsTerrcode == null || lsTerrcode.trim().length() == 0) {
						sql = "select  terr_code from  site_customer where cust_code =? and site_code =? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsTerrcode = rs.getString("terr_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsTerrcode == null || lsTerrcode.trim().length() == 0) {
						sql = "select terr_code from  customer where cust_code =? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsTerrcode = rs.getString("terr_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsTerrcode != null && lsTerrcode.trim().length() > 0) {
						sql = "select descr from territory where terr_code =? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsTerrcode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsTerrdescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<terr_code>").append("<![CDATA[" + lsTerrcode + "]]>")
						.append("</terr_code>");
						setNodeValue(dom, "terr_code", getAbsString(lsTerrcode));
						valueXmlString.append("<territory_descr>").append("<![CDATA[" + lsTerrdescr + "]]>")
						.append("</territory_descr>");
						setNodeValue(dom, "territory_descr", getAbsString(lsTerrdescr));
					}

					sql = "select sales_pers , sales_pers__1 	, sales_pers__2 from customer_series where cust_code =? and item_ser =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mslpers = checkNull(rs.getString("sales_pers"));
						mslPers1 = checkNull(rs.getString("sales_pers__1"));
						mslPers2 = checkNull(rs.getString("sales_pers__2"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;



					sql = "select order_type from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsOrderType = checkNull(rs.getString("order_type"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					if (lsOrderType != null && lsOrderType.trim().length() > 0) {
						valueXmlString.append("<order_type>").append("<![CDATA[" + lsOrderType + "]]>")
						.append("</order_type>");
						setNodeValue(dom, "order_type", getAbsString(lsOrderType));
						valueXmlString.append("<descr>").append("<![CDATA[" + lsDescrCl + "]]>").append("</descr>");
						setNodeValue(dom, "descr", getAbsString(lsDescrCl));
					}
					lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
					boolean lbOrdFlag = false;
					String lsDisIndOrdtypeListArr[] = lsDisIndOrdtypeList.split(",");
					if (lsDisIndOrdtypeListArr.length > 0) {
						for (int i = 0; i < lsDisIndOrdtypeListArr.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsDisIndOrdtypeListArr[i])) {
								lbOrdFlag = true;
							}
						}
					}

					System.out.println("lbOrdFlag...6353["+lbOrdFlag+"]");
					if (lbOrdFlag) {
						sql = "select cr_term from customer_series where cust_code = ? and item_ser= ?";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1, custCode); //changed by Pavan R on 16aug18 start [to set cr_term on billto/dlvto] 
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
						/*if("1".equals(custTaxOpt))	{
							pstmt.setString(1, mbillto);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, custCodeDlv);							
						}*/
						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, mbillto);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else
						{
							pstmt.setString(1, custCode);
						}

						//Commented and Added by sarita to set customer on basis of cr_term_source 15 NOV 2018 [END] 
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						//Added and commented by sarita on 15 NOV 2018 [START]
						/*pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;		*/
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						//Added and commented by sarita on 15 NOV 2018 [END]

						if (mcrTerm == null || mcrTerm.trim().length() == 0) {
							sql = "select cr_term from customer where cust_code =  ?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, custCode); 
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
							/*if("1".equals(custTaxOpt))	{
								pstmt.setString(1, mbillto);							
							}else if("0".equals(custTaxOpt)) {						
								pstmt.setString(1, custCodeDlv);							
							}*/
							if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, mbillto);
							}
							else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeDlv);
							}
							else
							{
								pstmt.setString(1, custCode);
							}							
							//Commented and Added by sarita to set customer on basis of cr_term_source 15 NOV 2018 [END] 							
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mcrTerm = rs.getString("cr_term");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					} else {						
						sql = "select cr_term from customer_series where cust_code = ? and item_ser= ?";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1, custCode);
						//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
						/*if("1".equals(custTaxOpt))	{
							pstmt.setString(1, mbillto);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, custCodeDlv);							
						}*/
						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, mbillto);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else
						{
							pstmt.setString(1, custCode);
						}						
						//Commented and Added by sarita to set customer on basis of cr_term_source 15 NOV 2018 [END] 
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (mcrTerm == null || mcrTerm.trim().length() == 0) {
							sql = "select cr_term from customer where cust_code =  ?";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, custCode);
							//Commented and Added by sarita to set customer on on basis of cr_term_source 15 NOV 2018 [START] 
							/*if("1".equals(custTaxOpt))	{
								pstmt.setString(1, mbillto);							
							}else if("0".equals(custTaxOpt)) {						
								pstmt.setString(1, custCodeDlv);							
							}*/
							if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, mbillto);
							}
							else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
							{
								pstmt.setString(1, custCodeDlv);
							}
							else
							{
								pstmt.setString(1, custCode);
							}							
							//Commented and Added by sarita to set customer on basis of cr_term_source 15 NOV 2018 [END] 
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mcrTerm = rs.getString("cr_term");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (mslpers == null || mslpers.trim().length() == 0) {
						sql = "select sales_pers from customer where cust_code =  ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mslpers = checkNull(rs.getString("sales_pers"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (mslPers1 == null || mslPers1.trim().length() == 0) {
						sql = "select sales_pers__1 from customer where cust_code =   ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mslPers1 = checkNull(rs.getString("sales_pers__1"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (mslPers2 == null || mslPers2.trim().length() == 0) {
						sql = "select sales_pers__2 from customer where cust_code =   ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mslPers2 = checkNull(rs.getString("sales_pers__2"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					sql = "select order_type, loc_group, part_qty from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsOrderType = checkNull(rs.getString("order_type"));
						lsLocGroup = checkNull(rs.getString("loc_group"));
						lsPartQty = checkNull(rs.getString("part_qty"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Pavan Rane 04FEB2019 [if part_qty in customer master is null then consider as '2'].
					if(lsPartQty == null || lsPartQty.trim().length() == 0 || "null".equals(lsPartQty))
					{
						lsPartQty = "2";
					}
					valueXmlString.append("<loc_group>").append("<![CDATA[" + lsLocGroup + "]]>")
					.append("</loc_group>");
					setNodeValue(dom, "loc_group", getAbsString(lsLocGroup));
					valueXmlString.append("<part_qty>").append("<![CDATA[" + lsPartQty + "]]>").append("</part_qty>");
					setNodeValue(dom, "part_qty", getAbsString(lsPartQty));

					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "order_type", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					// lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom1));

					System.out.println("lsCrTerm"+lsCrTerm);
					sql = "SELECT CR_TERM_MAPPING.CR_TERM_MAP FROM CR_TERM_MAPPING"
							+ " WHERE ( CR_TERM_MAPPING.CR_TERM = ? ) AND ( CR_TERM_MAPPING.ORD_TYPE = ?) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcrTerm);
					pstmt.setString(2, lsOrderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsCrTerm = rs.getString("CR_TERM_MAP");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsCrTerm != null && lsCrTerm.trim().length() > 0) {
						mcrTerm = lsCrTerm;
					}
					valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrTerm + "]]>").append("</cr_term>");
					setNodeValue(dom, "cr_term", getAbsString(mcrTerm));

					sql = "select descr from crterm where cr_term =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcrTerm);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</crterm_descr>");
					setNodeValue(dom, "crterm_descr", getAbsString(descr));

					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					ldtOrderDate = checkNull(genericUtility.getColumnValue("order_date", dom));

					//commented by manish mhatre on 10-sep-2019[for set dlv cust code in customer master ]
					//start manish
					//					valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + custCode + "]]>")
					//							.append("</cust_code__dlv>");
					//					setNodeValue(dom, "cust_code__dlv", getAbsString(custCode));
					//end manish 												// Start added by chandrashekar
					// 10-aug-2016

					//lsOrderType=checkNull(genericUtility.getColumnValue("order_type",dom));

					//added by manish mhatre on 24-sep-2019
					//start manish

					sql="SELECT PRICE_BASIS FROM SORDERTYPE where ORDER_TYPE= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,lsOrderType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						priceBasis = checkNullandTrim(rs.getString("PRICE_BASIS"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if((priceBasis.length()>0))
					{
						sql=" SELECT CUST_CODE__BIL,CUST_CODE__DLV FROM CUSTOMER WHERE CUST_CODE = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,lsCustCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							custCodeBilPl = rs.getString("CUST_CODE__BIL");
							custCodeDlv = rs.getString("CUST_CODE__DLV");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(("B").equalsIgnoreCase(priceBasis)) {

							lsCustCode=custCodeBilPl;
							custCode=custCodeBilPl;
						}
						if(("D").equalsIgnoreCase(priceBasis))
						{
							lsCustCode=custCodeDlv;
							custCode=custCodeDlv;
						}
					}
					//end manish

					sql = "SELECT  price_list, price_list__clg FROM cust_plist WHERE cust_code = ? AND order_type =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, lsOrderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsPriceList = checkNull(rs.getString("price_list"));
						lsPlistClg = checkNull(rs.getString("price_list__clg"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsPriceList == null || lsPriceList.trim().length() == 0) {
						Timestamp orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
					}
					System.out.println("lsSiteCode[" + lsSiteCode + "]");
					if (lsPriceList == null || lsPriceList.trim().length() == 0) {
						String lsSiteCode1 = checkNull(genericUtility.getColumnValue("site_code", dom));
						System.out.println("lsSiteCode1[" + lsSiteCode1 + "]");
						lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
					}
					if (lbOrdFlag) {
						sql = "SELECT  price_list, price_list__clg FROM cust_plist WHERE cust_code = ? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
					} else {
						sql = "SELECT  price_list, price_list__clg FROM cust_plist WHERE cust_code = ? AND order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceList = checkNull(rs.getString("price_list"));
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							Timestamp orderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
						}
						if (lsPriceList == null || lsPriceList.trim().length() == 0) {
							lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
						}
					}
					if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
						valueXmlString.append("<price_list>").append("<![CDATA[]]>").append("</price_list>");
					} else {
						valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + lsPriceList + "]]>")
						.append("</price_list>");  //protected by manish mhatre on 15-apr-2020
						setNodeValue(dom, "price_list", getAbsString(""));
					}
					if (lsPriceList == null || lsPriceList.trim().length() == 0) {
						valueXmlString.append("<price_list protect = \"0\">").append("<![CDATA[]]>")
						.append("</price_list>");
						valueXmlString.append("<price_list__clg protect = \"0\">").append("<![CDATA[]]>")
						.append("</price_list__clg>");
					} else {
						valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + lsPriceList + "]]>")
						.append("</price_list>");
						setNodeValue(dom, "price_list", getAbsString(lsPriceList));
						valueXmlString.append("<price_list__clg protect = \"1\">")
						.append("<![CDATA[" + lsPlistClg + "]]>").append("</price_list__clg>");
						setNodeValue(dom, "price_list__clg", getAbsString(lsPlistClg));
					}
					lsTaxclasshdr = checkNull(genericUtility.getColumnValue("tax_class", dom));
					lsTaxchaphdr = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					lsTaxenvhdr = checkNull(genericUtility.getColumnValue("tax_env", dom));
					lsSiteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));   //added by manish mhatre on 24-sep-2019
					custCode=checkNull(genericUtility.getColumnValue("cust_code", dom));   //added by manish mhatre on 24-sep-2019

					sql = "select stan_code from site where site_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCodeShip);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsStationfr = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select stan_code from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsStationto = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select descr from crterm where cr_term =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcrTerm);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mCrdescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					//added by Pavan R on 11jun2k18 [to set taxes based on a cust_tax_opt parameter in order type]					
					//custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					taxClassHdr = checkNull(genericUtility.getColumnValue("tax_class", dom));
					taxChapHdr = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					taxEnvHdr = checkNull(genericUtility.getColumnValue("tax_env", dom));
					mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));															
					//orderType = checkNull(genericUtility.getColumnValue("order_type",dom));										
					/*sql = "select cust_tax_opt from sordertype where order_type = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	
					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}
					 */
					System.out.println("cust_code::custTaxOpt["+custTaxOpt+"]custCodeDlv["+custCodeDlv+"]mbillto["+mbillto+"]orderType["+orderType+"]");										
					sql = "select stan_code from customer where cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					if("1".equals(custTaxOpt))	{
						pstmt.setString(1, mbillto);
						custCodeTax = mbillto;
					}else if("0".equals(custTaxOpt)) {						
						pstmt.setString(1, custCodeDlv);
						custCodeTax = custCodeDlv;
					}																				
					rs = pstmt.executeQuery();
					if (rs.next()) {						
						stanCodeTo = rs.getString("stan_code");					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					
					//if (taxChapHdr == null || taxChapHdr.trim().length() == 0) {
					taxChapHdr = distCommon.getTaxChap("", mItemSer, "C", custCodeTax, lsSiteCodeShip, conn);
					//}
					//if (taxClassHdr == null || taxClassHdr.trim().length() == 0) {					
					taxClassHdr = distCommon.getTaxClass("C", custCodeTax, "", lsSiteCodeShip, conn);
					//}
					//if (taxEnvHdr == null || taxEnvHdr.trim().length() == 0) {
					taxEnvHdr = distCommon.getTaxEnv(lsStationfr, stanCodeTo, taxChapHdr, taxClassHdr, lsSiteCodeShip, conn);
					//}
					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClassHdr + "]]>").append("</tax_class>");
					setNodeValue(dom, "tax_class", getAbsString(taxClassHdr));					
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChapHdr + "]]>").append("</tax_chap>");
					setNodeValue(dom, "tax_chap", getAbsString(taxChapHdr));					
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnvHdr + "]]>").append("</tax_env>");
					setNodeValue(dom, "tax_env", getAbsString(taxEnvHdr));					
					System.out.println("cust_code::taxChapHdr["+taxChapHdr+"]taxClassHdr["+taxClassHdr+"]taxEnvHdr["+taxEnvHdr+"]");
					//added by Pavan R on 11jun2k18 End					
					valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");
					setNodeValue(dom, "cust_name", getAbsString(custName));
					valueXmlString.append("<cust_code__bil>").append("<![CDATA[" + mbillto + "]]>")
					.append("</cust_code__bil>");
					setNodeValue(dom, "cust_code__bil", getAbsString(mbillto));

					sql = "select cust_name from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsDlvTo = rs.getString("cust_name");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<cust_name__bil>").append("<![CDATA[" + custNameBill + "]]>")
					.append("</cust_name__bil>");
					setNodeValue(dom, "cust_name__bil", getAbsString(lsCustnameBil));

					//Added by mayur on 26-July-2018 -- [start]--to get the billing details populated when the cust code is set 
					valueXmlString.append("<bill_addr1>").append("<![CDATA[" + billAddr1 + "]]>")
					.append("</bill_addr1>");
					setNodeValue(dom, "bill_addr1", getAbsString(billAddr1));

					valueXmlString.append("<bill_addr2>").append("<![CDATA[" + billAddr2 + "]]>")
					.append("</bill_addr2>");
					setNodeValue(dom, "bill_addr2", getAbsString(billAddr2));

					valueXmlString.append("<bill_addr3>").append("<![CDATA[" + billAddr3 + "]]>")
					.append("</bill_addr3>");
					setNodeValue(dom, "bill_addr3", getAbsString(billAddr3));

					valueXmlString.append("<bill_city>").append("<![CDATA[" + billCity + "]]>")
					.append("</bill_city>");
					setNodeValue(dom, "bill_city", getAbsString(billCity));

					valueXmlString.append("<state_code__bill>").append("<![CDATA[" + stateCode1 + "]]>")
					.append("</state_code__bill>");
					setNodeValue(dom, "state_code__bill", getAbsString(stateCode1));

					valueXmlString.append("<bill_pin>").append("<![CDATA[" + billPin + "]]>")
					.append("</bill_pin>");
					setNodeValue(dom, "bill_pin", getAbsString(billPin));

					valueXmlString.append("<cst_no__bill>").append("<![CDATA[" + cstNoBill + "]]>")
					.append("</cst_no__bill>");
					setNodeValue(dom, "cst_no__bill", getAbsString(cstNoBill));

					valueXmlString.append("<lst_no__bill>").append("<![CDATA[" + lstNoBill + "]]>")
					.append("</lst_no__bill>");
					setNodeValue(dom, "lst_no__bill", getAbsString(lstNoBill));

					valueXmlString.append("<drug_lic_no__bill>").append("<![CDATA[" + drugLicNo + "]]>")
					.append("</drug_lic_no__bill>");
					setNodeValue(dom, "drug_lic_no__bill", getAbsString(drugLicNo));		

					valueXmlString.append("<drug_lic_no_1__bill>").append("<![CDATA[" + drugLicNo1 + "]]>")
					.append("</drug_lic_no_1__bill>");
					setNodeValue(dom, "drug_lic_no_1__bill", getAbsString(drugLicNo1));

					valueXmlString.append("<drug_lic_no_2__bill>").append("<![CDATA[" + drugLicNo2 + "]]>")
					.append("</drug_lic_no_2__bill>");
					setNodeValue(dom, "drug_lic_no_2__bill", getAbsString(drugLicNo2));			        			           
					//Added by mayur on 26-July-2018 -- [end]


					valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrTerm + "]]>").append("</cr_term>");
					setNodeValue(dom, "cr_term", getAbsString(mcrTerm));

					valueXmlString.append("<crterm_descr>").append("<![CDATA[" + mCrdescr + "]]>")
					.append("</crterm_descr>");
					setNodeValue(dom, "crterm_descr", getAbsString(mCrdescr));

					valueXmlString.append("<dlv_to>").append("<![CDATA[" + lsDlvTo + "]]>").append("</dlv_to>");
					setNodeValue(dom, "dlv_to", getAbsString(lsDlvTo));


					valueXmlString.append("<sales_pers>").append("<![CDATA[" + mslpers + "]]>").append("</sales_pers>");
					setNodeValue(dom, "sales_pers", getAbsString(mslpers));

					if (mslpers != null && mslpers.trim().length() > 0) {
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					valueXmlString.append("<sales_pers__1>").append("<![CDATA[" + mslPers1 + "]]>")
					.append("</sales_pers__1>");
					setNodeValue(dom, "sales_pers__1", getAbsString(mslPers1));

					if (mslPers1 != null && mslPers1.trim().length() > 0) {
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__1", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					valueXmlString.append("<sales_pers__2>").append("<![CDATA[" + mslPers2 + "]]>")
					.append("</sales_pers__2>");
					setNodeValue(dom, "sales_pers__2", getAbsString(mslPers2));

					if (mslPers2 != null && mslPers2.trim().length() > 0) {
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__2", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					sql = "select count(*)as cnt from customer_series where cust_code = ? and 	item_ser  =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (cnt > 0) {
						sql = "select pending_order from customer_series where cust_code =? and 	item_ser  =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPendingOrder = rs.getString("pending_order");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsPendingOrder == null || lsPendingOrder.trim().length() == 0) {
						sql = "select pending_order from customer where cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPendingOrder = rs.getString("pending_order");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					//Pavan R 04FEB2019 [if pending_order in customer master is null then consider as 'N'].
					if(lsPendingOrder == null || lsPendingOrder.trim().length() == 0 || "null".equals(lsPendingOrder) )
					{
						lsPendingOrder = "N";
					}

					sql = "select cust_name, addr1, addr2, city, pin, count_code, stan_code, tran_code, addr3, trans_mode, state_code,"
							+ "tele1,tele2,tele3, fax,loc_group,curr_code__frt, curr_code__ins, term_table__no,single_lot"
							+ " from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("cust_name");
						descr1 = rs.getString("addr1");
						descr2 = rs.getString("addr2");
						descr3 = rs.getString("city");
						descr4 = rs.getString("pin");
						mcountry = rs.getString("count_code");
						mstan = rs.getString("stan_code");
						mtran = rs.getString("tran_code");
						maddr3 = rs.getString("addr3");
						mTransMode = rs.getString("trans_mode");
						mstate = rs.getString("state_code");
						tel1 = rs.getString("tele1");
						tel2 = rs.getString("tele2");
						tel3 = rs.getString("tele3");
						fax = rs.getString("fax");
						lsLocGroup = checkNull(rs.getString("loc_group"));
						lsCurrCodeFrt = rs.getString("curr_code__frt");
						lsCurrCodeIns = rs.getString("curr_code__ins");
						lsTermTable = rs.getString("term_table__no");
						lsSingleLot = checkNull(rs.getString("single_lot"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select curr_code , bank_code , rcp_mode, chq_name from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mcurr = checkNull(rs.getString("curr_code"));
						mbankCode = checkNull(rs.getString("bank_code"));
						mrcpMode = checkNull(rs.getString("rcp_mode"));
						lsChqName = checkNull(rs.getString("chq_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = "select curr_code__frt, curr_code__ins from site_customer where site_code = ? and cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCode);
					pstmt.setString(2, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsCurrcdFrt = rs.getString("curr_code__frt");
						lsCurrcdIns = rs.getString("curr_code__ins");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsCurrcdFrt == null || lsCurrcdFrt.trim().length() == 0) {
						lsCurrcdFrt = lsCurrCodeFrt;
					}
					if (lsCurrcdIns == null || lsCurrcdIns.trim().length() == 0) {
						lsCurrcdIns = lsCurrCodeIns;
					}
					if (lsCurrcdFrt == null || lsCurrcdFrt.trim().length() == 0) {
						lsCurrcdFrt = mcurr;
					}
					if (lsCurrcdIns == null || lsCurrcdIns.trim().length() == 0) {
						lsCurrcdIns = mcurr;
					}


					lsTypeAllowCrLmtList = distCommon.getDisparams("999999", "TYPE_ALLOW_CR_LMT", conn);
					if (lsTypeAllowCrLmtList == null || lsTypeAllowCrLmtList.trim().length() == 0
							|| lsTypeAllowCrLmtList.equalsIgnoreCase("NULLFOUND")) {
						lbOrdFlag = false;
					} else {
						String lsTypeAllowCrLmt[] = lsTypeAllowCrLmtList.split(",");
						for (int i = 0; i < lsTypeAllowCrLmt.length; i++) {
							if (lsOrderType.equalsIgnoreCase(lsTypeAllowCrLmt[i])) {
								lbOrdFlag = true;
								break;
							}
						}
					}
					if (lbOrdFlag) {
						sql = "select CREDIT_LMT from customer_series where cust_code = ? and 	item_ser  = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcCreditLmt = rs.getString("CREDIT_LMT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						sql = "select credit_lmt from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcCreditLmt = rs.getString("credit_lmt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}


					valueXmlString.append("<dlv_add1>").append("<![CDATA[" + descr1 + "]]>").append("</dlv_add1>");
					setNodeValue(dom, "dlv_add1", getAbsString(descr1));

					valueXmlString.append("<dlv_add2>").append("<![CDATA[" + descr2 + "]]>").append("</dlv_add2>");
					setNodeValue(dom, "dlv_add2", getAbsString(descr2));

					valueXmlString.append("<dlv_city>").append("<![CDATA[" + descr3 + "]]>").append("</dlv_city>");
					setNodeValue(dom, "dlv_city", getAbsString(descr3));

					valueXmlString.append("<dlv_pin>").append("<![CDATA[" + descr4 + "]]>").append("</dlv_pin>");
					setNodeValue(dom, "dlv_pin", getAbsString(descr4));

					valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + mcountry + "]]>")
					.append("</count_code__dlv>");
					setNodeValue(dom, "count_code__dlv", getAbsString(mcountry));

					valueXmlString.append("<tran_code>").append("<![CDATA[" + mtran + "]]>").append("</tran_code>");
					setNodeValue(dom, "tran_code", getAbsString(mtran));

					valueXmlString.append("<stan_code>").append("<![CDATA[" + mstan + "]]>").append("</stan_code>");
					setNodeValue(dom, "stan_code", getAbsString(mstan));

					valueXmlString.append("<curr_code>").append("<![CDATA[" + mcurr + "]]>").append("</curr_code>");
					setNodeValue(dom, "curr_code", getAbsString(mcurr));

					valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + lsCurrcdFrt + "]]>")
					.append("</curr_code__frt>");
					setNodeValue(dom, "curr_code__frt", getAbsString(lsCurrcdFrt));

					valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + lsCurrcdIns + "]]>")
					.append("</curr_code__ins>");
					setNodeValue(dom, "curr_code__ins", getAbsString(lsCurrcdIns));

					valueXmlString.append("<dlv_add3>").append("<![CDATA[" + maddr3 + "]]>").append("</dlv_add3>");
					setNodeValue(dom, "dlv_add3", getAbsString(maddr3));

					valueXmlString.append("<bank_code>").append("<![CDATA[" + mbankCode + "]]>").append("</bank_code>");
					setNodeValue(dom, "bank_code", getAbsString(mbankCode));

					valueXmlString.append("<trans_mode>").append("<![CDATA[" + mTransMode + "]]>")
					.append("</trans_mode>");
					setNodeValue(dom, "trans_mode", getAbsString(mTransMode));

					valueXmlString.append("<rcp_mode>").append("<![CDATA[" + mrcpMode + "]]>").append("</rcp_mode>");
					setNodeValue(dom, "rcp_mode", getAbsString(mrcpMode));

					valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + mstate + "]]>")
					.append("</state_code__dlv>");
					setNodeValue(dom, "state_code__dlv", getAbsString(mstate));

					valueXmlString.append("<tel1__dlv>").append("<![CDATA[" + tel1 + "]]>").append("</tel1__dlv>");
					setNodeValue(dom, "tel1__dlv", getAbsString(tel1));

					valueXmlString.append("<tel2__dlv>").append("<![CDATA[" + tel2 + "]]>").append("</tel2__dlv>");
					setNodeValue(dom, "tel2__dlv", getAbsString(tel2));

					valueXmlString.append("<tel3__dlv>").append("<![CDATA[" + tel3 + "]]>").append("</tel3__dlv>");
					setNodeValue(dom, "tel3__dlv", getAbsString(tel3));

					valueXmlString.append("<fax__dlv>").append("<![CDATA[" + fax + "]]>").append("</fax__dlv>");
					setNodeValue(dom, "fax__dlv", getAbsString(fax));

					System.out.println("lsLocGroup customer Id [" + lsLocGroup + "]");
					valueXmlString.append("<loc_group>").append("<![CDATA[" + lsLocGroup + "]]>")
					.append("</loc_group>");
					setNodeValue(dom, "loc_group", getAbsString(lsLocGroup));

					valueXmlString.append("<chq_name>").append("<![CDATA[" + lsChqName + "]]>").append("</chq_name>");
					setNodeValue(dom, "chq_name", getAbsString(lsChqName));


					valueXmlString.append("<cr_lmt>").append("<![CDATA[" + lcCreditLmt + "]]>").append("</cr_lmt>");
					setNodeValue(dom, "cr_lmt", getAbsString(lcCreditLmt));


					valueXmlString.append("<term_table__no>").append("<![CDATA[" + lsTermTable + "]]>")
					.append("</term_table__no>");
					setNodeValue(dom, "term_table__no", getAbsString(lsTermTable));


					valueXmlString.append("<pending_order>").append("<![CDATA[" + lsPendingOrder + "]]>")
					.append("</pending_order>");
					setNodeValue(dom, "pending_order", getAbsString(lsPendingOrder));


					valueXmlString.append("<single_lot>").append("<![CDATA[" + lsSingleLot + "]]>")
					.append("</single_lot>");
					setNodeValue(dom, "single_lot", getAbsString(lsSingleLot));

					reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcurr);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mcurrdescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// Timestamp orderDate =
					// Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date",
					// dom1), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
					// + " 00:00:00.0");
					String ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));

					// mNum = finCommon.getDailyExchRateSellBuy(mcurr, "", lsSiteCode,
					// orderDate.toString(), "S", conn);
					mNum = finCommon.getDailyExchRateSellBuy(mcurr, "", lsSiteCode, ordDate, "S", conn);
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + mNum + "]]>").append("</exch_rate>");
					setNodeValue(dom, "exch_rate", getAbsString(String.valueOf(mNum)));
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
					//exchangeRateProtect(mcurr, lsSiteCode, "exch_rate", conn);
					retVal =  exchangeRateProtect(mcurr, lsSiteCode, "exch_rate", conn);
					System.out.println("retVal--["+retVal+"]");
					valueXmlString.append(retVal);
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
					// mNum = finCommon.getDailyExchRateSellBuy(lsCurrcdFrt, "", lsSiteCode,
					// orderDate.toString(), "S", conn);
					mNum = finCommon.getDailyExchRateSellBuy(lsCurrcdFrt, "", lsSiteCode, ordDate.toString(), "S",
							conn);
					valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + mNum + "]]>")
					.append("</exch_rate__frt>");
					setNodeValue(dom, "exch_rate__frt", getAbsString(String.valueOf(mNum)));
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
					//exchangeRateProtect(mcurr, lsSiteCode, "exch_rate", conn);
					retVal = exchangeRateProtect(lsCurrcdFrt, lsSiteCode, "exch_rate__frt", conn);
					System.out.println("retVal--111["+retVal+"]");
					valueXmlString.append(retVal);
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]

					// mNum = finCommon.getDailyExchRateSellBuy(lsCurrcdIns, "", lsSiteCode,
					// orderDate.toString(), "S", conn);
					mNum = finCommon.getDailyExchRateSellBuy(lsCurrcdIns, "", lsSiteCode, ordDate.toString(), "S",
							conn);
					valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + mNum + "]]>")
					.append("</exch_rate__ins>");
					setNodeValue(dom, "exch_rate__ins", getAbsString(String.valueOf(mNum)));
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
					//exchangeRateProtect(lsCurrcdIns, lsSiteCode, "exch_rate__ins", conn);
					retVal = exchangeRateProtect(lsCurrcdIns, lsSiteCode, "exch_rate__ins", conn);
					System.out.println("retVal--125["+retVal+"]");
					valueXmlString.append(retVal);
					//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]

					valueXmlString.append("<currency_descr>").append("<![CDATA[" + mcurrdescr + "]]>")
					.append("</currency_descr>");
					setNodeValue(dom, "currency_descr", getAbsString(mcurrdescr));

					sql = "select descr from station where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mstan);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<station_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</station_descr>");
					setNodeValue(dom, "station_descr", getAbsString(descr));

					sql = "select tran_name, (case when frt_term is null then 'B' else frt_term end)as descr1 ,curr_code as mcurr"
							+ " from transporter where tran_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mtran);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("tran_name");
						descr1 = rs.getString("descr1");
						mcurr = rs.getString("mcurr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<tran_name>").append("<![CDATA[" + descr + "]]>").append("</tran_name>");
					setNodeValue(dom, "tran_name", getAbsString(descr));
					valueXmlString.append("<frt_term>").append("<![CDATA[" + descr1 + "]]>").append("</frt_term>");
					setNodeValue(dom, "frt_term", getAbsString(descr1));

					if (lsCurrcdFrt == null || lsCurrcdFrt.trim().length() == 0) {
						valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + mcurr + "]]>")
						.append("</curr_code__frt>");
						setNodeValue(dom, "curr_code__frt", getAbsString(mcurr));

						// mNum = finCommon.getDailyExchRateSellBuy(mcurr, "", lsSiteCode,
						// orderDate.toString(), "S", conn);
						mNum = finCommon.getDailyExchRateSellBuy(mcurr, "", lsSiteCode, ordDate.toString(), "S", conn);
						valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + mNum + "]]>")
						.append("</exch_rate__frt>");
						setNodeValue(dom, "exch_rate__frt", getAbsString(String.valueOf(mNum)));
						//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
						//exchangeRateProtect(mcurr, lsSiteCode, "exch_rate__frt", conn);
						retVal = exchangeRateProtect(mcurr, lsSiteCode, "exch_rate__frt", conn);
						System.out.println("retVal--451["+retVal+"]");
						valueXmlString.append(retVal);
						//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
					}

					lsPlistDisc = priceListDiscount(lsSiteCode, lsCustCode, conn);
					sql = "select ltrim(rtrim(order_type)) as ls_plist_ordertype from pricelist where price_list = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsPlistDisc);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsPlistOrderType = rs.getString("ls_plist_ordertype");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if ((lsOrderType.equalsIgnoreCase("NE") && lsPlistOrderType.equalsIgnoreCase("NE"))
							|| (!lsOrderType.equalsIgnoreCase("NE") && lsPlistOrderType == null)) {
						valueXmlString.append("<price_list__disc protect = \"1\">").append("<![CDATA[" + lsPlistDisc + "]]>")
						.append("</price_list__disc>");   //protected by manish mhatre on 15-apr-2020
						setNodeValue(dom, "price_list__disc", getAbsString(lsPlistDisc));
					}
					if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
						sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
						sql = "select price_list__clg from customer where  cust_code =  ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPlistClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					if (lsPlistClg == null || lsPlistClg.trim().length() == 0) {
						lsPlistClg = distCommon.getDisparams("999999", "PRICE_LIST__CLG", conn);
						if (lsPlistClg != null && lsPlistClg.trim().length() > 0
								&& !lsPlistClg.equalsIgnoreCase("NULLFOUND")) {
							if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
								valueXmlString.append("<price_list__clg>").append("<![CDATA[]]>")
								.append("</price_list__clg>");
							} else {
								valueXmlString.append("<price_list__clg>").append("<![CDATA[" + lsPlistClg + "]]>")
								.append("</price_list__clg>");
								setNodeValue(dom, "price_list__clg", getAbsString(lsPlistClg));
							}
						}
					} else {
						if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
							valueXmlString.append("<price_list__clg>").append("<![CDATA[]]>")
							.append("</price_list__clg>");
						} else {
							valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + lsPlistClg + "]]>")
							.append("</price_list__clg>");   //protected by manish mhatre on 15-apr-2020
							setNodeValue(dom, "price_list__clg", getAbsString(lsPlistClg));
						}
					}

					sql = "select market_reg from site_customer where  cust_code = ? and    site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, lsSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsMarketReg = rs.getString("market_reg");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (lsMarketReg == null || lsMarketReg.trim().length() == 0) {
						sql = "select market_reg from customer where  cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsMarketReg = rs.getString("market_reg");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsMarketReg != null && lsMarketReg.trim().length() > 0) {
						valueXmlString.append("<market_reg>").append("<![CDATA[" + lsMarketReg + "]]>")
						.append("</market_reg>");
						setNodeValue(dom, "market_reg", getAbsString(lsMarketReg));
					}

					sql = "select email_addr from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsEmailAddr = rs.getString("email_addr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<email_addr>").append("<![CDATA[" + lsEmailAddr + "]]>")
					.append("</email_addr>");
					setNodeValue(dom, "email_addr", getAbsString(lsEmailAddr));


					sql = "select dlv_term from   customer_series where  cust_code = ? and    item_ser = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsDlvTerm = rs.getString("dlv_term");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (lsDlvTerm == null || lsDlvTerm.trim().length() == 0) {
						sql = "select dlv_term from   customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsDlvTerm = rs.getString("dlv_term");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsDlvTerm == null || lsDlvTerm.trim().length() == 0) {
						lsDlvTerm = "NA";
					}
					valueXmlString.append("<dlv_term>").append("<![CDATA[" + lsDlvTerm + "]]>").append("</dlv_term>");
					setNodeValue(dom, "dlv_term", getAbsString(lsDlvTerm));

					Timestamp orderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
					if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
					{
						lsContractNo = null;
					}
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
					System.out.println("inside customer code itemchange 9434..............");
					if (lsContractNo == null || lsContractNo.trim().length() == 0) {
						if (lsContractNo == null || lsContractNo.trim().length() == 0) 
						{
						lsContractNo = getContractHrd(lsSiteCode, lsCustCode, orderDate, lsItemser, conn);//Modified by Pratiksha on 01-03-21
						valueXmlString.append("<contract_no>").append("<![CDATA[]]>").append("</contract_no>");
					    }
					}
					valueXmlString.append("<site_code__ship>").append("<![CDATA[" + lsSiteCode + "]]>")
					.append("</site_code__ship>");
					setNodeValue(dom, "site_code__ship", getAbsString(lsSiteCode));

					if (lbOrdFlag) {
						sql = "select fn_get_cust_series(?, ?,?, 'T') as lc_os_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						pstmt.setString(3, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOsAmt = rs.getString("lc_os_amt");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						valueXmlString.append("<os_amt>").append("<![CDATA[" + lcOsAmt + "]]>").append("</os_amt>");
						setNodeValue(dom, "os_amt", getAbsString(lcOsAmt));

						sql = "select fn_get_cust_series(?,?,?, 'O') as lc_ovos_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						pstmt.setString(3, lsItemser);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOvosAmt = rs.getString("lc_ovos_amt");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					} else {
						sql = "select fn_get_custos(?,?, 'T') as lc_os_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOsAmt = rs.getString("lc_os_amt");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						valueXmlString.append("<os_amt>").append("<![CDATA[" + lcOsAmt + "]]>").append("</os_amt>");
						setNodeValue(dom, "os_amt", getAbsString(lcOsAmt));

						sql = "select fn_get_custos(?,?, 'O') as lc_ovos_amt from dual";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcOvosAmt = rs.getString("lc_ovos_amt");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}
					valueXmlString.append("<ovos_amt>").append("<![CDATA[" + lcOvosAmt + "]]>").append("</ovos_amt>");
					setNodeValue(dom, "ovos_amt", getAbsString(lcOvosAmt));

					sql = "select frt_term from site_customer where cust_code = ? and site_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, lsSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsFrtterm = checkNull(rs.getString("frt_term"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (lsFrtterm == null || lsFrtterm.trim().length() == 0) {
						sql = "select frt_term from customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsFrtterm = checkNull(rs.getString("frt_term"));
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}
					if (lsFrtterm == null || lsFrtterm.trim().length() == 0) {
						lsTranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						if (lsTranCode != null && lsTranCode.trim().length() > 0) {
							sql = "select frt_term from transporter where  tran_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsTranCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsFrtterm = checkNull(rs.getString("frt_term"));
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
						}
					}
					valueXmlString.append("<frt_term>").append("<![CDATA[" + lsFrtterm + "]]>").append("</frt_term>");
					setNodeValue(dom, "frt_term", getAbsString(lsFrtterm));
					/* Pavan Rane 10jul19 [below commented code added on initially on cust_code itemchnaged]
					//Added by sarita to set cust_code__dlv from customer if matches customer.cust_code = sorder.cust_code on 04 APR 2019 [START]					
					custCodeDlv = checkNull(getCustCodeDlv(custCode, conn));
					System.out.println("custCode ::::::: "+custCode + "\t" + "custCodeDlv ::::::"+custCodeDlv); 
					if(custCodeDlv != null && custCodeDlv.trim().length() > 0)
					{
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA["+custCodeDlv+"]]>").append("</cust_code__dlv>");
						//Added by sarita on 08 APR 2019 to perform cust_code__dlv itemchange [START]
						setNodeValue(dom, "cust_code__dlv", getAbsString(custCodeDlv));
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						//Added by sarita on 08 APR 2019 to perform cust_code__dlv itemchange [END]
					}
					else
					{
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA["+custCode+"]]>").append("</cust_code__dlv>");
						setNodeValue(dom, "cust_code__dlv", getAbsString(custCode));
						//Added by sarita on 08 APR 2019 to perform cust_code__dlv itemchange [START]
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						//Added by sarita on 08 APR 2019 to perform cust_code__dlv itemchange [END]
					}
					//Added by sarita to set cust_code__dlv from customer if matches customer.cust_code = sorder.cust_code on 04 APR 2019 [END]
					 */
				} else if (currentColumn.trim().equalsIgnoreCase("curr_code__ins")) {
					valueXmlString = (itmCurrCodeIns(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));

				} else if (currentColumn.trim().equalsIgnoreCase("curr_code__comm")) {
					valueXmlString = (itmCurrCodeComm(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));

				} else if (currentColumn.trim().equalsIgnoreCase("curr_code__comm1")) {
					valueXmlString = (itmCurrCodeComm1(valueXmlString, dom, dom1, dom2, editFlag, xtraParams,
							objContext, conn));

				} else if (currentColumn.trim().equalsIgnoreCase("curr_code__comm2")) {
					valueXmlString = (itmCurrCodeComm2(valueXmlString, dom, dom1, dom2, editFlag, xtraParams,
							objContext, conn));

				} else if (currentColumn.trim().equalsIgnoreCase("cust_pord")) {
					lsCustPord = checkNull(genericUtility.getColumnValue("cust_pord", dom));
					if (lsCustPord != null && lsCustPord.trim().length() > 0) {
						sql = "select ord_date from porder where purc_order=? and 	confirmed ='Y' and status ='O' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustPord);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ldPorderDate = rs.getTimestamp("ord_date");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (ldPorderDate != null) {
							String ldPorDtStr = genericUtility.getValidDateString(ldPorderDate.toString(),
									genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
							valueXmlString.append("<pord_date>").append("<![CDATA[" + ldPorDtStr + "]]>")
							.append("</pord_date>");
						} else {
							String ldPorDtStr = "";
							valueXmlString.append("<pord_date>").append("<![CDATA[" + ldPorDtStr + "]]>")
							.append("</pord_date>");
						}
					}
				} else if (currentColumn.trim().equalsIgnoreCase("quot_no")) {
					lsQuotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));

					if (lsQuotNo != null && lsQuotNo.trim().length() > 0) {
						sql = "select cust_code, sales_pers from sales_quot where quot_no =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsQuotNo);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCustCode = checkNull(rs.getString("cust_code"));
							lsSalesPers = checkNull(rs.getString("sales_pers"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<cust_code>").append("<![CDATA[" + lsCustCode + "]]>")
						.append("</cust_code>");
						setNodeValue(dom, "cust_code", getAbsString(lsCustCode));
						reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);

						if (lsSalesPers != null && lsSalesPers.trim().length() > 0) {
							valueXmlString.append("<sales_pers>").append("<![CDATA[" + lsSalesPers + "]]>")
							.append("</sales_pers>");
							setNodeValue(dom, "sales_pers", getAbsString(lsSalesPers));
							reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
						}
					}
				} else if (currentColumn.trim().equalsIgnoreCase("exch_rate")) {
					lcExchRate = checkNull(genericUtility.getColumnValue("exch_rate", dom));
					lsCurrCode = checkNullandTrim(genericUtility.getColumnValue("curr_code", dom));
					lsCurrCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm", dom));
					lsCurrCodeComm1 = checkNull(genericUtility.getColumnValue("curr_code__comm1", dom));
					lsCurrCodeComm2 = checkNull(genericUtility.getColumnValue("curr_code__comm2", dom));

					if (lsCurrCodeComm.equalsIgnoreCase(lsCurrCode)) {
						valueXmlString.append("<exch_rate__comm>").append("<![CDATA[" + lcExchRate + "]]>")
						.append("</exch_rate__comm>");
					}
					if (lsCurrCodeComm1.equalsIgnoreCase(lsCurrCode)) {
						valueXmlString.append("<exch_rate__comm1>").append("<![CDATA[" + lcExchRate + "]]>")
						.append("</exch_rate__comm1>");
					}
					if (lsCurrCodeComm2.equalsIgnoreCase(lsCurrCode)) {
						valueXmlString.append("<exch_rate__comm2>").append("<![CDATA[" + lcExchRate + "]]>")
						.append("</exch_rate__comm2>");
					}
				} else if (currentColumn.trim().equalsIgnoreCase("stan_code__notify")) {
					lsStanCodeNotify = checkNull(genericUtility.getColumnValue("stan_code__notify", dom));

					sql = "select descr from station where stan_code =? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsStanCodeNotify);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<stan_descr__notify>").append("<![CDATA[" + descr + "]]>")
					.append("</stan_descr__notify>");
				} else if (currentColumn.trim().equalsIgnoreCase("tran_code")) {
					String tranCode = "", custCode = "", siteCode = "", frtTerm = "", trasMode = "";
					tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));

					sql = "select tran_name  from transporter where tran_code =? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("tran_name");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<tran_name>").append("<![CDATA[" + descr + "]]>").append("</tran_name>");

					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					sql = "select frt_term from site_customer where cust_code = ? and site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						frtTerm = checkNull(rs.getString("frt_term"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (frtTerm == null || frtTerm.trim().length() == 0) {
						sql = "select frt_term  from customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							frtTerm = checkNull(rs.getString("frt_term"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (frtTerm == null || frtTerm.trim().length() == 0) {
						sql = "select frt_term  from transporter where  tran_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							frtTerm = checkNull(rs.getString("frt_term"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<frt_term>").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");
					if (tranCode != null && tranCode.trim().length() > 0) {
						sql = "select trans_mode  from transporter_mode where tran_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							trasMode = rs.getString("trans_mode");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (trasMode != null && trasMode.trim().length() > 0) {
							valueXmlString.append("<trans_mode>").append("<![CDATA[" + trasMode + "]]>")
							.append("</trans_mode>");
						} else {
							valueXmlString.append("<trans_mode>").append("<![CDATA[]]>").append("</trans_mode>");
						}
					}
				}
				// Added By Priyankac on 27JUNE2017.[--START--]
				else if ("dlv_geo_pos".equalsIgnoreCase(currentColumn.trim())) {
					dlvCity = checkNull(genericUtility.getColumnValue("dlv_city", dom));
					System.out.println("city from hdr : [" + dlvCity + "]");
					dlvCity = dlvCity.toUpperCase().trim();
					if (dlvCity != null && dlvCity.trim().length() > 0) {
						dlvCity = dlvCity.trim();
						sql = " select s.state_code,st.count_code from station s,state st  where UPPER(city)= (?) and s.state_code=st.state_code";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, dlvCity);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							state = rs.getString("state_code");
							countryCode = rs.getString("count_code");

						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}

						if (rs != null) {
							rs.close();
							rs = null;

						}

						valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + state + "]]>")
						.append("</state_code__dlv>");
						valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countryCode + "]]>")
						.append("</count_code__dlv>");
					}

				}

				// Added By Priyankac on 27JUNE2017.[--END--]

				//site_code__ship column itemchange added by nandkumar gadkari on 25/10/19--------start---------------------
				else if ("site_code__ship".equalsIgnoreCase(currentColumn.trim())) {
					String custCodeBill="";
					siteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
					mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
					custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));

					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
					orderType = checkNull(genericUtility.getColumnValue("order_type",dom));

					sql = "select cust_tax_opt from sordertype where order_type = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	

					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}

					sql = "select stan_code from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeShip);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						stanCodeFr = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;										
					sql = "select stan_code from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);				 
					if("1".equals(custTaxOpt))	{
						pstmt.setString(1, custCodeBill);
						custCodeTax = custCodeBill;
					}else if("0".equals(custTaxOpt)) {						
						pstmt.setString(1, custCodeDlv);
						custCodeTax = custCodeDlv;
					}	
					rs = pstmt.executeQuery();
					if (rs.next()) 	{						
						stanCodeTo = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					
					taxChapHdr = distCommon.getTaxChap("", mItemSer, "C", custCodeTax, siteCodeShip, conn);
					taxClassHdr = distCommon.getTaxClass("C", custCodeTax, "", siteCodeShip, conn);
					taxEnvHdr = distCommon.getTaxEnv(stanCodeFr, stanCodeTo, taxChapHdr, taxClassHdr, siteCodeShip, conn);

					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClassHdr + "]]>").append("</tax_class>");
					setNodeValue(dom, "tax_class", getAbsString(taxClassHdr));					
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChapHdr + "]]>").append("</tax_chap>");
					setNodeValue(dom, "tax_chap", getAbsString(taxChapHdr));					
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnvHdr + "]]>").append("</tax_env>");
					setNodeValue(dom, "tax_env", getAbsString(taxEnvHdr));	

				}
				//added by manish mhatre on 31-oct-2019[For set the cust_name_end]
				//start manish
				else if (currentColumn.trim().equalsIgnoreCase("cust_code__end")){
					String custName="",custCode="";

					custCode = checkNull(genericUtility.getColumnValue("cust_code__end",dom));			
					sql="select cust_name from customer where cust_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						custName =rs.getString("cust_name");
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					valueXmlString.append("<cust_name__end>").append("<![CDATA[" + custName + "]]>").append("</cust_name__end>");
				}
				//end manish

				valueXmlString.append("</Detail1>");
				break;
			}
			valueXmlString.append("</Root>");
			System.out.println("valueXmlString[" + valueXmlString.toString() + "]");

			return valueXmlString.toString();

		} catch (Exception e) {
			System.out.println("Exception : [itemChangedNew] :==>\n" + e.getMessage());
			throw new ITMException(e);
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	public String itemChangedDet(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException, SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0, childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String sql = "";
		String lsDeptcode = "", mDlvsite = "", mOrdSite = "", mStatus = "", mCurrency = "", mTaxOpt = "",
				mSingleSer = "", lsDueDays = "", mkeyval = "", lsOrderType = "", lsOtypeDescr = "", lsPlist = "",
				lsPlist1 = "", lsPlist2 = "", lsSiteCode = "", lsCustCode = "", lsSalesPers = "", lsSalesPers1 = "",
				lsSalesPers2 = "", lsContractNo = "", lsOrdtypeSample = "";
		String lsCustCodeDlv = "", lsCustCodeBil = "", mtaxopt = "", mItemSer = "", lsCustPord = "", lcCommPerc = "",
				lsTaxClass = "", lsTaxChap = "", lsTaxEnv = "", ldTaxDate = "";
		String lsPriceList = "", lsCrTerm = "", lsQuotNo = "", lsCurrCode = "", lcExchRate = "", lsRemarks = "",
				lsDlvAdd1 = "", lsDlvAdd2 = "", lsDlvCity = "", lsCountCodeDlv = "";
		String lsDlvPin = "", lsStanCode = "", lsPartQty = "", lsStatus = "", ldStatusDate = "", lsTranCode = "",
				lsUdfStr1 = "", lsUdfStr2 = "", lcUdfNum1 = "", lcUdfNum2 = "";
		String lcCommAmt = "", lsStatusRemarks = "", lsDlvTerm = "", lcFrtAmt = "", lsCurrCodeFrt = "",
				lcExchRateFrt = "", lsFrtTerm = "";
		String lcAdvPerc = "", lsDistRoute = "", lsCurrCodeComm = "", lcCommPerc1 = "", lsCommPercOn1 = "",
				lsCurrCodeComm1 = "", lcCommPerc2 = "", lsCommPercOn2 = "", lsCurrCodeComm2 = "";
		String lsEmpCodeCon = "", lsDlvAdd3 = "", lsStateCodeDlv = "", lsTransMode = "", lsSpecReason = "",
				lsOffshoreInvoice = "", lsLabelType = "", lsOutsideInspection = "";
		String lsRemarks2 = "", lsRemarks3 = "", lsStanCodeInit = "", lsCurrCodeIns = "", lcExchRateIns = "",
				lcInsAmt = "", lsShipStatus = "", lsDlvTo = "", lsAcctCodeSal = "";
		String lsCctrCodeSal = "", tel1 = "", tel2 = "", tel3 = "", fax = "", lcExchRateComm = "", lcExchRateComm1 = "",
				lcExchRateComm2 = "", lsPriceListDisc = "", lsMarketReg = "";
		String lsProjCode = "", lsContractType = "", lsCustnameBil = "", lsDisIndOrdtypeList = "", lsPlistClg = "",
				lsLocGroup = "", lsTermTable = "";
		String ldtOrderDate = "", lsItemser = "", lsPlistDisc = "", mget1 = "", lsTerrdescr = "", descr = "",
				lsAvailableYn = "", lsCctrcodeSal = "", lsAcctcodeSal = "", mcrTermNp = "";
		String mcrdescr = "", mcrterm = "", lsPlistOrderType = "", lsTypeAllowCrLmtList = "", lcCreditLmt = "",
				lcOsAmt = "", lcOvosAmt = "", mTranDate = "", mCode = "", lsCurrcodeBase = "";
		String lsEmpCode = "", lsSordCommCal = "", lsCommTable1 = "", mCcurr = "", lsStateCode = "", lsCommTable3 = "",
				lsSpName = "", lsStanCodeNotify = "";
		String lsItemCode = "", lcSaleRate = "", lsShDescr = "", lsItemCodeOrd = "", lsSaleOrd = "", lsListType = "",
				mPriceListClg = "", lsCommPerc1 = "", lsCommPerc2 = "";
		String mPriceList = "", lsFinscheme = "", lsCommTable2 = "", lsDisLink = "", lsChannelPartner = "";
		String ContractNo = "", lsCommPercOn = "";
		Timestamp ldPromDate = null, ldPordDate = null, ldUdfDate1 = null, ldPorderDate = null;
		Timestamp TranDateDet = null;
		double mExcRate = 0.0, lcStdrate = 0.00, lsCommPerc = 0.00;
		int cnt = 0, ll_schcnt = 0, cnt1 = 0;
		boolean rootFlag = true;
		String itemSer="",custCodeBil="",priceListDisc="",orderType="",priceList="",priceListClg="";

		int pos = 0;
		String reStr = "";

		Timestamp ldDueDate = null;
		try {
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("-------- currentFormNo : " + currentFormNo);

			switch (currentFormNo) {
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();


				reStr = itemChangedHdr(dom1, dom1, dom2, "1","itm_defaultedit", editFlag, xtraParams);
				reStr=reStr.substring(reStr.indexOf("<Detail1>"), reStr.indexOf("</Detail1>"));
				System.out.println("Detail 1String"+reStr);

				valueXmlString = new StringBuffer(
						"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
				valueXmlString.append(editFlag).append("</editFlag></header>");
				valueXmlString.append(reStr);

				custCodeBil = checkNull(genericUtility.getColumnValue("cust_code__bil", dom1));
				itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
				priceListDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
				orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
				priceList= checkNull(genericUtility.getColumnValue("price_list", dom1));
				priceListClg= checkNull(genericUtility.getColumnValue("price_list__clg", dom1));
				lsCustCode= checkNull(genericUtility.getColumnValue("cust_code", dom1));

				valueXmlString.append("<cust_code protect = \"1\">").append("<![CDATA[" + lsCustCode + "]]>")
				.append("</cust_code>");
				valueXmlString.append("<cust_code__bil protect = \"1\">").append("<![CDATA[" + custCodeBil + "]]>")
				.append("</cust_code__bil>");
				valueXmlString.append("<item_ser protect = \"1\">").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
				valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + priceList + "]]>")
				.append("</price_list>");
				valueXmlString.append("<price_list__disc protect = \"1\">").append("<![CDATA[" + priceListDisc + "]]>")
				.append("</price_list__disc>");
				valueXmlString.append("<order_type protect = \"1\">").append("<![CDATA[" + orderType + "]]>")
				.append("</order_type>");
				valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + priceListClg + "]]>")
				.append("</price_list__clg>");
				valueXmlString.append("</Detail1>");

				valueXmlString.append("<Detail2>");
				int childListLength = childNodeList.getLength();

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) {
					String lsSpecs = "", lcRate = "",rateClg="";//rateClg added by nandkumar gadkari on 06/05/19
					double lsCommPerc1D = 0.00, lsCommPerc2D = 0.00;
					String lsCommPercOn2D = "", lsCommPercOn1D = "";
					lsItemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					String unit="",unitStd="",unitRate="",conveQtyStduom="",convRtuomStduom="";

					sql = "select (case when sale_rate is null then 0 else sale_rate end) as lc_sale_rate,sh_descr from item where item_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsItemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcSaleRate = rs.getString("lc_sale_rate");
						lsShDescr = rs.getString("sh_descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select rtrim(ltrim(item_parnt)) ||' , '|| item_type.phy_attrib_1 || ' : ' "
							+ "||(case when item.phy_attrib_1 is null then '' else item.phy_attrib_1 end) || ' , '||item_type.phy_attrib_2 || ' : ' "
							+ "||(case when item.phy_attrib_2 is null then '' else item.phy_attrib_2 end) ||' , ' ||item_type.phy_attrib_3 || ' : ' "
							+ "||(case when item.phy_attrib_3 is null then '' else item.phy_attrib_3 end) || ' , '||item_type.phy_attrib_4 || ' : '"
							+ "||(case when item.phy_attrib_4 is null then '' else item.phy_attrib_4 end) ||' , ' ||item_type.phy_attrib_5 || ' : '"
							+ "||(case when item.phy_attrib_5 is null then '' else item.phy_attrib_5 end) as ls_specs"
							+ " from item, item_type where item.item_type = item_type.item_type"
							+ " and item.item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsItemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsSpecs = rs.getString("ls_specs");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_specs>").append("<![CDATA[" + lsSpecs + " " + "M.R.P. = " + lcSaleRate
							+ " " + " Short Descr : " + lsShDescr + "]]>").append("</item_specs>");

					lsPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
					lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
					Timestamp orderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					lsSaleOrd = checkNull(genericUtility.getColumnValue("sale_order", dom1));

					sql = "select  quot_no from sorder where sale_order =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSaleOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsQuotNo = rs.getString("quot_no");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					lsListType = distCommon.getPriceListType(lsPriceList, conn);

					lcRate = checkNull(genericUtility.getColumnValue("rate", dom));
					rateClg = checkNull(genericUtility.getColumnValue("rate__clg", dom));//added by nandkumar gadkari on 06/05/19

					if (lsListType == null || lsListType.trim().length() == 0) {
						lsListType = "L";
					}
					if (lsQuotNo != null && lsQuotNo.trim().length() > 0) {
						valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + lcRate + "]]>")
						.append("</rate>");
					} else {
						if (lsListType.equalsIgnoreCase("B") || lsListType.equalsIgnoreCase("I")
								|| lsListType.equalsIgnoreCase("F")) {
							valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + lcRate + "]]>")
							.append("</rate>");
						} else {
							valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + lcRate + "]]>")
							.append("</rate>");
						}
					}
					mPriceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));
					lsListType = distCommon.getPriceListType(mPriceListClg, conn);
					if (lsListType == null || lsListType.trim().length() == 0) {
						lsListType = "L";
					}
					if (lsListType.equalsIgnoreCase("B") || lsListType.equalsIgnoreCase("F")
							|| lsListType.equalsIgnoreCase("I")) {
						//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [START]
						/*valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + lcRate + "]]>")
								.append("</rate>");*/
						valueXmlString.append("<rate__clg protect = \"1\">").append("<![CDATA[" + rateClg + "]]>")
						.append("</rate__clg>");
						//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [END]
					} else {
						//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [START]
						/*valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + lcRate + "]]>")
								.append("</rate>");*/
						valueXmlString.append("<rate__clg protect = \"0\">").append("<![CDATA[" + rateClg + "]]>")
						.append("</rate__clg>");
						//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [END]
					}
					// Values to be retrived from Header dom manoj
					lsCommPerc = Double.parseDouble(genericUtility.getColumnValue("comm_perc", dom1) == null ? "0.00"
							: genericUtility.getColumnValue("comm_perc", dom1));
					lsCommPercOn = genericUtility.getColumnValue("comm_perc__on", dom1) == null ? " "
							: genericUtility.getColumnValue("comm_perc__on", dom1);
					lsCommPerc1D = Double
							.parseDouble(genericUtility.getColumnValue("comm_perc_1", dom1) == null ? "0.00"
									: genericUtility.getColumnValue("comm_perc_1", dom1));
					lsCommPercOn1D = genericUtility.getColumnValue("comm_perc_on_1", dom1) == null ? " "
							: genericUtility.getColumnValue("comm_perc_on_1", dom1);
					lsCommPerc2D = Double
							.parseDouble(genericUtility.getColumnValue("comm_perc_2", dom1) == null ? "0.00"
									: genericUtility.getColumnValue("comm_perc_2", dom1));
					lsCommPercOn2D = genericUtility.getColumnValue("comm_perc_on_2", dom1) == null ? " "
							: genericUtility.getColumnValue("comm_perc_on_2", dom1);

					if ((lsCommPerc > 0) || (lsCommPerc1 != null || lsCommPerc1.trim().length() > 0)
							|| (lsCommPerc2 != null || lsCommPerc2.trim().length() > 0)) {
						valueXmlString.append("<comm_perc_1 protect = \"1\">").append("<![CDATA[" + lsCommPerc + "]]>")
						.append("</comm_perc_1>");
						valueXmlString.append("<comm_perc_2 protect = \"1\">")
						.append("<![CDATA[" + lsCommPerc1D + "]]>").append("</comm_perc_2>");
						valueXmlString.append("<comm_perc_3 protect = \"1\">")
						.append("<![CDATA[" + lsCommPerc2D + "]]>").append("</comm_perc_3>");
						valueXmlString.append("<comm_perc_on_1 protect = \"1\">")
						.append("<![CDATA[" + lsCommPercOn + "]]>").append("</comm_perc_on_1>");
						valueXmlString.append("<comm_perc_on_2 protect = \"1\">")
						.append("<![CDATA[" + lsCommPercOn1D + "]]>").append("</comm_perc_on_2>");
						valueXmlString.append("<comm_perc_on_3 protect = \"1\">")
						.append("<![CDATA[" + lsCommPercOn2D + "]]>").append("</comm_perc_on_3>");
					} else {
						lsSalesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom1));
						lsSalesPers1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom1));
						lsSalesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom1));

						if (lsSalesPers == null || lsSalesPers.trim().length() == 0) {
							valueXmlString.append("<comm_perc_1 protect = \"1\">")
							.append("<![CDATA[" + lsCommPerc + "]]>").append("</comm_perc_1>");
							valueXmlString.append("<comm_perc_on_1 protect = \"1\">")
							.append("<![CDATA[" + lsCommPercOn + "]]>").append("</comm_perc_on_1>");
						} else {
							valueXmlString.append("<comm_perc_1 protect = \"0\">")
							.append("<![CDATA[" + lsCommPerc + "]]>").append("</comm_perc_1>");
							valueXmlString.append("<comm_perc_on_1 protect = \"0\">")
							.append("<![CDATA[" + lsCommPercOn + "]]>").append("</comm_perc_on_1>");
						}
						if (lsSalesPers1 == null || lsSalesPers1.trim().length() == 0) {
							valueXmlString.append("<comm_perc_2 protect = \"1\">")
							.append("<![CDATA[" + lsCommPerc1D + "]]>").append("</comm_perc_2>");
							valueXmlString.append("<comm_perc_on_2 protect = \"1\">")
							.append("<![CDATA[" + lsCommPercOn1D + "]]>").append("</comm_perc_on_2>");
						} else {
							valueXmlString.append("<comm_perc_2 protect = \"0\">")
							.append("<![CDATA[" + lsCommPerc1D + "]]>").append("</comm_perc_2>");
							valueXmlString.append("<comm_perc_on_2 protect = \"0\">")
							.append("<![CDATA[" + lsCommPercOn1D + "]]>").append("</comm_perc_on_2>");
						}
						if (lsSalesPers2 == null || lsSalesPers2.trim().length() == 0) {
							valueXmlString.append("<comm_perc_3 protect = \"1\">")
							.append("<![CDATA[" + lsCommPerc2D + "]]>").append("</comm_perc_3>");
							valueXmlString.append("<comm_perc_on_3 protect = \"1\">")
							.append("<![CDATA[" + lsCommPercOn2D + "]]>").append("</comm_perc_on_3>");
						} else {
							valueXmlString.append("<comm_perc_3 protect = \"0\">")
							.append("<![CDATA[" + lsCommPerc2D + "]]>").append("</comm_perc_3>");
							valueXmlString.append("<comm_perc_on_3 protect = \"0\">")
							.append("<![CDATA[" + lsCommPercOn2D + "]]>").append("</comm_perc_on_3>");
						}
					}

					//added by nandkumar gadkari on 22/07/19-----start-----------------------
					unit = checkNull(genericUtility.getColumnValue("unit", dom));
					unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
					conveQtyStduom = genericUtility.getColumnValue("conv__qty_stduom", dom);
					unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
					convRtuomStduom = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
					if (unit.trim().equalsIgnoreCase(unitStd.trim())) {
						valueXmlString.append("<conv__qty_stduom protect = \"1\">").append("<![CDATA[" + conveQtyStduom + "]]>").append("</conv__qty_stduom>");
					} 
					else
					{
						valueXmlString.append("<conv__qty_stduom protect = \"0\">").append("<![CDATA[" + conveQtyStduom + "]]>").append("</conv__qty_stduom>");
					}
					if(unitStd.trim().equalsIgnoreCase(unitRate.trim()))//if condition added by nandkumar gadkari on 22/07/19
					{
						valueXmlString.append("<conv__rtuom_stduom protect = \"1\">").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
					}
					else
					{
						valueXmlString.append("<conv__rtuom_stduom protect = \"0\">").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
					}
					//added by nandkumar gadkari on 22/07/19-----end-----------------------

				} else if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					String lsSiteCodeShip = "", dueDateStr = "";
					String lsCommPercOnStr = "";
					String custNameEnd="",custCodeEnd="";    //added by manish mhatre on 31-oct-2019[for cust code end]

					lsSaleOrd = checkNull(genericUtility.getColumnValue("sale_order", dom1));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					lsSiteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom1));
					System.out.println("Ship site code in dtl" +lsSiteCodeShip);
					lsPlistDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
					custCodeEnd = checkNull(genericUtility.getColumnValue("cust_code__end",dom1));    //added by manish mhatre on 31-oct-2019[for cust code end]     
					custNameEnd=checkNull(genericUtility.getColumnValue("cust_name__end",dom1));    //added by manish mhatre on 31-oct-2019[for cust name end]
					//added by manish mhatre on 31-oct-2019
					//start manish
					valueXmlString.append("<cust_code__end>").append("<![CDATA[" + custCodeEnd + "]]>").append("</cust_code__end>");
					valueXmlString.append("<cust_name__end>").append("<![CDATA[" + custNameEnd + "]]>").append("</cust_name__end>");
					//end manish
					valueXmlString.append("<sale_order>").append("<![CDATA[" + lsSaleOrd + "]]>")
					.append("</sale_order>");
					//Changed By PriyankaC on 28SEP2018.
					/*valueXmlString.append("<site_code>").append("<![CDATA[" + lsSiteCode + "]]>")
							.append("</site_code>");
					valueXmlString.append("<site_code>").append("<![CDATA[" + lsSiteCodeShip + "]]>")
							.append("</site_code>");
					//Changed By PriyankaC on 28SEP2018.[END]
					 valueXmlString.append("<site_code__ship>").append("<![CDATA[" + lsSiteCodeShip +
					 "]]>").append("</site_code__ship>");*/


					valueXmlString.append("<price_list__disc>").append("<![CDATA[" + lsPlistDisc + "]]>")
					.append("</price_list__disc>");

					sql = "select  quot_no from sorder where sale_order =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSaleOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsQuotNo = rs.getString("quot_no");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					mPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
					lsListType = distCommon.getPriceListType(mPriceList, conn);

					if (lsListType == null || lsListType.trim().length() == 0) {
						lsListType = "L";
					}
					if (lsQuotNo != null && lsQuotNo.trim().length() > 0) {
						valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[]]>").append("</rate>");
					} else {
						if ("B".equalsIgnoreCase(lsListType) || "F".equalsIgnoreCase(lsListType)
								|| "I".equalsIgnoreCase(lsListType)) {
							valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[]]>").append("</rate>");
						} else {
							valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[]]>").append("</rate>");
						}
					}
					dueDateStr = this.genericUtility.getColumnValue("due_date", dom1);
					// Timestamp ldtDueDate =
					// Timestamp.valueOf(genericUtility.getColumnValue("due_date", dom1));
					// valueXmlString.append("<dsp_date>").append("<![CDATA[" + ldtDueDate +
					// "]]>").append("</dsp_date>");
					valueXmlString.append("<dsp_date>").append("<![CDATA[" + dueDateStr + "]]>").append("</dsp_date>");
					//Changed By PriyankaC on 25SEP2018[START]
					//  valueXmlString.append("<site_code protect = \"0\">").append("<![CDATA[]]>").append("</site_code>");
					valueXmlString.append("<site_code protect = \"0\">").append("<![CDATA["+lsSiteCodeShip+"]]>").append("</site_code>");
					//Changed By PriyankaC on 25SEP2018[END]

					valueXmlString.append("<item_specs>").append("<![CDATA[]]>").append("</item_specs>");

					lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
					if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
					{
						lsContractNo = null;
					}
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
					if (lsContractNo != null && lsContractNo.trim().length() > 0) {
						valueXmlString.append("<contract_no protect = \"1\">")
						.append("<![CDATA[" + lsContractNo + "]]>").append("</contract_no>");
					}
					lsFinscheme = checkNull(genericUtility.getColumnValue("fin_scheme", dom1));
					valueXmlString.append("<fin_scheme>").append("<![CDATA[" + lsFinscheme + "]]>")
					.append("</fin_scheme>");

					lsCommPerc = Double.parseDouble(genericUtility.getColumnValue("comm_perc", dom) == null ? "0.00"
							: genericUtility.getColumnValue("comm_perc", dom));
					lsCommPercOnStr = checkNull(genericUtility.getColumnValue("comm_perc_on_1", dom1));
					lsCommPerc1 = checkNull(genericUtility.getColumnValue("comm_perc_1", dom1));
					lsCommPercOn1 = checkNull(genericUtility.getColumnValue("comm_perc_on_1", dom1));
					lsCommPerc2 = checkNull(genericUtility.getColumnValue("comm_perc_2", dom1));
					lsCommPercOn2 = checkNull(genericUtility.getColumnValue("comm_perc_on_2", dom1));

					valueXmlString.append("<comm_perc_1>").append("<![CDATA[" + lsCommPerc + "]]>")
					.append("</comm_perc_1>");
					valueXmlString.append("<comm_perc_on_1>").append("<![CDATA[" + lsCommPercOnStr + "]]>")
					.append("</comm_perc_on_1>");
					valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + lsCommPerc1 + "]]>")
					.append("</comm_perc_2>");
					valueXmlString.append("<comm_perc_on_2>").append("<![CDATA[" + lsCommPercOn1 + "]]>")
					.append("</comm_perc_on_2>");
					valueXmlString.append("<comm_perc_3>").append("<![CDATA[" + lsCommPerc2 + "]]>")
					.append("</comm_perc_3>");
					valueXmlString.append("<comm_perc_on_3>").append("<![CDATA[" + lsCommPercOn2 + "]]>")
					.append("</comm_perc_on_3>");

					if ((lsCommPerc > 0) || (lsCommPerc1 != null || lsCommPerc1.trim().length() > 0)
							|| (lsCommPerc2 != null || lsCommPerc2.trim().length() > 0)) {
						valueXmlString.append("<comm_perc_1 protect = \"1\">").append("<![CDATA[" + lsCommPerc + "]]>")
						.append("</comm_perc_1>");
						valueXmlString.append("<comm_perc_on_1 protect = \"1\">")
						.append("<![CDATA[" + lsCommPercOnStr + "]]>").append("</comm_perc_on_1>");
						valueXmlString.append("<comm_perc_2 protect = \"1\">").append("<![CDATA[" + lsCommPerc1 + "]]>")
						.append("</comm_perc_2>");
						valueXmlString.append("<comm_perc_on_2 protect = \"1\">")
						.append("<![CDATA[" + lsCommPercOn1 + "]]>").append("</comm_perc_on_2>");
						valueXmlString.append("<comm_perc_3 protect = \"1\">").append("<![CDATA[" + lsCommPerc2 + "]]>")
						.append("</comm_perc_3>");
						valueXmlString.append("<comm_perc_on_3 protect = \"1\">")
						.append("<![CDATA[" + lsCommPercOn2 + "]]>").append("</comm_perc_on_3>");
					} else {
						lsSalesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom1));
						lsSalesPers1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom1));
						lsSalesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom1));

						if (lsSalesPers == null || lsSalesPers.trim().length() == 0) {
							valueXmlString.append("<comm_perc_1 protect = \"1\">")
							.append("<![CDATA[" + lsCommPerc + "]]>").append("</comm_perc_1>");
							valueXmlString.append("<comm_perc_on_1 protect = \"1\">")
							.append("<![CDATA[" + lsCommPercOnStr + "]]>").append("</comm_perc_on_1>");
						} else {
							valueXmlString.append("<comm_perc_1 protect = \"0\">")
							.append("<![CDATA[" + lsCommPerc + "]]>").append("</comm_perc_1>");
							valueXmlString.append("<comm_perc_on_1 protect = \"0\">")
							.append("<![CDATA[" + lsCommPercOnStr + "]]>").append("</comm_perc_on_1>");
						}
						if (lsSalesPers1 == null || lsSalesPers1.trim().length() == 0) {
							valueXmlString.append("<comm_perc_2 protect = \"1\">")
							.append("<![CDATA[" + lsCommPerc1 + "]]>").append("</comm_perc_2>");
							valueXmlString.append("<comm_perc_on_2 protect = \"1\">")
							.append("<![CDATA[" + lsCommPercOn1 + "]]>").append("</comm_perc_on_2>");
						} else {
							valueXmlString.append("<comm_perc_2 protect = \"0\">")
							.append("<![CDATA[" + lsCommPerc1 + "]]>").append("</comm_perc_2>");
							valueXmlString.append("<comm_perc_on_2 protect = \"0\">")
							.append("<![CDATA[" + lsCommPercOn1 + "]]>").append("</comm_perc_on_2>");
						}
						if (lsSalesPers2 == null || lsSalesPers2.trim().length() == 0) {
							valueXmlString.append("<comm_perc_3 protect = \"1\">")
							.append("<![CDATA[" + lsCommPerc2 + "]]>").append("</comm_perc_3>");
							valueXmlString.append("<comm_perc_on_3 protect = \"1\">")
							.append("<![CDATA[" + lsCommPercOn2 + "]]>").append("</comm_perc_on_3>");
						} else {
							valueXmlString.append("<comm_perc_3 protect = \"0\">")
							.append("<![CDATA[" + lsCommPerc2 + "]]>").append("</comm_perc_3>");
							valueXmlString.append("<comm_perc_on_3 protect = \"0\">")
							.append("<![CDATA[" + lsCommPercOn2 + "]]>").append("</comm_perc_on_3>");
						}
					}
					valueXmlString.append("<nature>").append("<![CDATA[" + 'C' + "]]>").append("</nature>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("item_code__ord")) 
				{
					String itemCodeOrd = "", unit = "", unitStd = "", unitRate = "", packCode = "", taxClass = "     ",
							taxChap = "          ";
					String siteCodeShip = "", loginSiteCode = "", itemFlg = "I", convQtyStdUom = "1", lcQty = "",
							lcRate = "", lsUnit = "", lsSiteCodeShip = "";
					String mDescr = "", lsLocType = "", lsSiteCodeMfg = "", lsSiteCodeOwn = "", lsSitecodeshipItem = "",
							lsSitecodeshipSiteitem = "", city = "", mStateCd = "";
					String mQty = "", lsSchemeCode = "", lsType = "", lsCurscheme = "", lsApplyCustList = "",
							lsNoapplyCustList = "", lsApplicableordtypes = "", lsPrevscheme = "";
					String lsCustSchemeCode = "", lcIntegralQty = "", lsItemStru = "", lsDisPobOrdtypeList = "",
							lsPackCode = "", toStation = "", frStation = "", lsTaxChapHdr = "";
					//Pavan Rane 31jul19 start [Sales order near expiry logic - corrected]
					String lsTaxClassHdr = "", lsTaxEnvHdr = "";// lcMinShelfLife = "", llMaxShelfLifeDet = "",										
					String lsSorder = "";//, lcMinLifePerc = "", llShelfLife = "", llMaxShelfLife = "";
					double lcMinShelfLife = 0, llMaxShelfLifeDet = 0, lcMinLifePerc = 0, llShelfLife = 0, llMaxShelfLife = 0;
					//Pavan Rane 31jul19 end [Sales order near expiry logic - corrected]
					String lsSalesOrderType = "", lsVarvalue = "", lcStkQty = "", lsApplseg = "";
					String ldtDateStr = "";
					boolean lbProceed = false, lbOrdFlag = false;
					Timestamp ldtPldate = null, orderDate = null;
					String lsItemSer = "", custCodeBill = "", custTaxOpt = "", stanCodeTo="", custCodeTax="", lsSiteCodeDet="",itemCodeDet="";
					itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
					lsQuotNo = checkNull(genericUtility.getColumnValue("quot_no", dom1));

					if (lsQuotNo != null && lsQuotNo.trim().length() > 0) {
						sql = "select quantity, rate, unit, remarks from   sales_quotdet where  quot_no = ? and item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsQuotNo);
						pstmt.setString(2, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcQty = rs.getString("quantity");
							lcRate = rs.getString("rate");
							lsUnit = rs.getString("unit");
							lsRemarks = rs.getString("remarks");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<quantity>").append("<![CDATA[" + lcQty + "]]>").append("</quantity>");
						setNodeValue(dom, "quantity", getAbsString(lcQty));

						valueXmlString.append("<rate>").append("<![CDATA[" + lcRate + "]]>").append("</rate>");
						setNodeValue(dom, "rate", getAbsString(lcRate));

						valueXmlString.append("<unit>").append("<![CDATA[" + lsUnit + "]]>").append("</unit>");
						setNodeValue(dom, "unit", getAbsString(lsUnit));

						valueXmlString.append("<remarks>").append("<![CDATA[" + lsRemarks + "]]>").append("</remarks>");
						setNodeValue(dom, "remarks", getAbsString(lsRemarks));
					}

					lsSiteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom1));
					valueXmlString.append("<site_code>").append("<![CDATA[" + lsSiteCodeShip + "]]>")
					.append("</site_code>");
					setNodeValue(dom, "site_code", getAbsString(lsSiteCodeShip));

					sql = "select count(*) as cnt from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (cnt == 0) {
						valueXmlString.append("<item_code>").append("<![CDATA[]]>").append("</item_code>");
					}
					valueXmlString.append("<st_scheme>").append("<![CDATA[]]>").append("</st_scheme>");
					if (cnt > 0) {
						sql = "select descr , loc_type , site_code , site_code__own, site_code__ship from item where  item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mDescr = rs.getString("descr");
							lsLocType = rs.getString("loc_type");
							lsSiteCodeMfg = rs.getString("site_code");
							lsSiteCodeOwn = rs.getString("site_code__own");
							lsSitecodeshipItem = rs.getString("site_code__ship");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));

						sql = "select site_code__ship from siteitem where site_code = ? and    item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSiteCode);
						pstmt.setString(2, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsSitecodeshipSiteitem = rs.getString("site_code__ship");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsSitecodeshipSiteitem != null && lsSitecodeshipSiteitem.trim().length() > 0) {
							valueXmlString.append("<site_code>").append("<![CDATA[" + lsSitecodeshipSiteitem + "]]>")
							.append("</site_code>");
							setNodeValue(dom, "site_code", getAbsString(lsSitecodeshipSiteitem));
						} else if (lsSitecodeshipItem != null && lsSitecodeshipItem.trim().length() > 0) {
							valueXmlString.append("<site_code>").append("<![CDATA[" + lsSitecodeshipItem + "]]>")
							.append("</site_code>");
							setNodeValue(dom, "site_code", getAbsString(lsSitecodeshipItem));
						}

						valueXmlString.append("<item_site_code>").append("<![CDATA[" + lsSiteCodeMfg + "]]>")
						.append("</item_site_code>");
						setNodeValue(dom, "item_site_code", getAbsString(lsSiteCodeMfg));

						valueXmlString.append("<item_site_code__own>").append("<![CDATA[" + lsSiteCodeOwn + "]]>")
						.append("</item_site_code__own>");
						setNodeValue(dom, "item_site_code__own", getAbsString(lsSiteCodeOwn));

						sql = "select descr , city from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSiteCodeMfg);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr = rs.getString("descr");
							city = rs.getString("city");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<site_descr>").append("<![CDATA[" + descr + "]]>")
						.append("</site_descr>");
						setNodeValue(dom, "site_descr", getAbsString(descr));

						valueXmlString.append("<site_city>").append("<![CDATA[" + city + "]]>").append("</site_city>");
						setNodeValue(dom, "site_city", getAbsString(city));

						sql = "select descr , city from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSiteCodeOwn);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr = rs.getString("descr");
							city = rs.getString("city");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<site_descr_1>").append("<![CDATA[" + descr + "]]>")
						.append("</site_descr_1>");
						setNodeValue(dom, "site_descr_1", getAbsString(descr));

						valueXmlString.append("<site_city_1>").append("<![CDATA[" + city + "]]>")
						.append("</site_city_1>");
						setNodeValue(dom, "site_city_1", getAbsString(city));

						valueXmlString.append("<item_descr>").append("<![CDATA[" + mDescr + "]]>")
						.append("</item_descr>");
						setNodeValue(dom, "item_descr", getAbsString(descr));

						valueXmlString.append("<loc_type>").append("<![CDATA[" + lsLocType + "]]>")
						.append("</loc_type>");
						setNodeValue(dom, "loc_type", getAbsString(lsLocType));

						// Timestamp mOrderDate =
						// Timestamp.valueOf(genericUtility.getColumnValue("order_date", dom1));
						Timestamp mOrderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						lsItemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						mStateCd = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
						lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
						mTranDate = checkNull(genericUtility.getColumnValue("due_date", dom1));

						TranDateDet = Timestamp.valueOf(genericUtility.getValidDateString(mTranDate,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						mQty = checkNull(genericUtility.getColumnValue("quantity", dom));
						lsCountCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));

						sql = "select bom_code, item_stru from item where  item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsSchemeCode = checkNull(rs.getString("bom_code"));
							lsType = checkNullandTrim(rs.getString("item_stru"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						lsType = lsType.trim();
						if (!"C".equalsIgnoreCase(lsType)) {
							/*
							 * if isnull(ls_order_type) then ls_order_type = " " if isnull(ls_site_code)
							 * then ls_site_code = " " if isnull(mstate_cd) then mstate_cd = " " if
							 * isnull(ls_count_code__dlv) then ls_count_code__dlv = " "
							 */
							sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det b"
									+ " where a.scheme_code	= b.scheme_code" + " and a.item_code=?"
									+ " and a.app_from<= ?" + " and a.valid_upto>= ?" + " and (b.site_code=?"
									+ " or b.state_code = ?" + " or b.count_code= ?)";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCodeOrd);
							pstmt1.setTimestamp(2, TranDateDet);
							pstmt1.setTimestamp(3, TranDateDet);
							pstmt1.setString(4, lsSiteCode);
							pstmt1.setString(5, mStateCd);
							pstmt1.setString(6, lsCountCodeDlv);
							rs1 = pstmt1.executeQuery();
							while (rs1.next()) {
								lsCurscheme = rs1.getString("scheme_code");

								sql = "Select count(*) as cnt From	scheme_applicability A,bom b "
										+ "Where A.scheme_code = b.bom_code And B.bom_code= ?"
										+ " And (? between case when b.min_qty is null then 0 else b.min_qty end"
										+ " And case when b.max_qty is null then 0 else b.max_qty end)"
										+ " and B.promo_term is null";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCurscheme);
								pstmt.setString(2, mQty);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0)
									continue;
								sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as ls_apply_cust_list,"
										+ " (case when noapply_cust_list is null then ' ' else noapply_cust_list end) as ls_noapply_cust_list,order_type"
										+ " from scheme_applicability where scheme_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsCurscheme);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									lsApplyCustList = rs.getString("ls_apply_cust_list");
									lsNoapplyCustList = rs.getString("ls_noapply_cust_list");
									lsApplicableordtypes = rs.getString("order_type");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ("NE".equalsIgnoreCase(lsOrderType) && (lsApplicableordtypes == null
										|| lsApplicableordtypes.trim().length() == 0)) {
									// goto Nextrec
									continue;
								} else if (lsApplicableordtypes != null && lsApplicableordtypes.trim().length() > 0) {
									System.out.println("lsApplicableordtypes" + lsApplicableordtypes + ":::");
									lbProceed = false;
									String lsApplicableordtypesArr[] = lsApplicableordtypes.split(",");
									ArrayList<String> applicableordtypesList = new ArrayList<String>(
											Arrays.asList(lsApplicableordtypesArr));
									if (applicableordtypesList.contains(lsOrderType.trim())) {
										System.out.println("Inside lbProceeed" + lbProceed);
										lbProceed = true;
										// break;
									}
									if (!lbProceed) {

										System.out.println("Inside continue");
										lsSchemeCode = "";
										// goto Nextrec
										continue;
									}
								}
								lsPrevscheme = lsSchemeCode;
								lsSchemeCode = lsCurscheme;

								if (lsApplyCustList.trim().length() > 0) {
									lsSchemeCode = null;
									lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
									String lsApplyCustListArr[] = lsApplyCustList.split(",");
									ArrayList<String> applyCustList = new ArrayList<String>(
											Arrays.asList(lsApplyCustListArr));
									if (applyCustList.contains(lsCustCode.trim())) {
										lsSchemeCode = lsCurscheme;
										lsCustSchemeCode = lsCurscheme;
										// break;
									}
								}
								if (lsNoapplyCustList != null && lsNoapplyCustList.trim().length() > 0) {
									String lsNoapplyCustListArr[] = lsNoapplyCustList.split(",");
									ArrayList<String> noapplyCustList = new ArrayList<String>(
											Arrays.asList(lsNoapplyCustListArr));
									if (noapplyCustList.contains(lsCustCode)) {
										lsSchemeCode = "";
										break;
									}
								}
								if (lsSchemeCode != null) {
									ll_schcnt++;
								} else if (ll_schcnt == 1) {
									System.out.println("lsPrevscheme[" + lsPrevscheme + "]");
									lsSchemeCode = lsPrevscheme;
									System.out.println("lsSchemeCode[" + lsSchemeCode + "]");

								}
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							if (ll_schcnt > 1) {
								lsSchemeCode = "";
							} else if (lsCustSchemeCode.trim().length() > 0) {
								lsSchemeCode = lsCustSchemeCode;

							}
						} else {
							valueXmlString.append("<item_code>").append("<![CDATA[" + lsSchemeCode + "]]>")
							.append("</item_code>");
							setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
							// gbf_ic_item_code("item_code")//Need to Add manoj
							// itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag,
							// xtraParams);
							reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);

						}
						sql = "select batch_qty from bom where bom_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSchemeCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcIntegralQty = rs.getString("batch_qty");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (Double.parseDouble(checkDouble(lcIntegralQty)) > 0) {
							if (Double.parseDouble(checkDouble(mQty)) < Double
									.parseDouble(checkDouble(lcIntegralQty))) {
								lsSchemeCode = "";
							}
						}
						System.out.println("lsSchemeCode[" + lsSchemeCode + "]");
						sql = "select (case when item_stru is null then 'S' else item_stru end) as item_stru from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsItemStru = checkNullandTrim(rs.getString("item_stru"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						String itemCodeOrd1 = itemCodeOrd;
						lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom1));

						//lsDisPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
						lsDisPobOrdtypeList = ""; //commentted and added by rupali on 02/04/2021
						System.out.println("===lsDisPobOrdtypeList[" + lsDisPobOrdtypeList + "]");

						lbOrdFlag = false;
						System.out.println("===lsOrderType[" + lsDisPobOrdtypeList + "]");
						System.out.println("===lsDisPobOrdtypeList.length[" + lsDisPobOrdtypeList.length() + "]");
						if (lsDisPobOrdtypeList != null && lsDisPobOrdtypeList.trim().length() > 0) 
						{
							String lsDisPobOrdtypeListArr[] = lsDisPobOrdtypeList.split(",");
							ArrayList<String> disPobOrdtypeList = new ArrayList<String>(Arrays.asList(lsDisPobOrdtypeListArr));
							System.out.println("disPobOrdtypeList[" + disPobOrdtypeList + "]");
							//if (disPobOrdtypeList.contains(lsOrderType)) { 
							for( String pobOrdType : disPobOrdtypeList )
							{
								if(checkNull(pobOrdType).equalsIgnoreCase(lsOrderType)) {
									lbOrdFlag = true;
								}
							}
						}
						System.out.println("lsItemStru[" + lsItemStru + "] lsSchemeCode[" + lsSchemeCode + "]lbOrdFlag["
								+ lbOrdFlag + "]");
						if (lbOrdFlag == true) {
							System.out.println("lbOrdFlag inside[" + lbOrdFlag + "");
							valueXmlString.append("<item_flg>").append("<![CDATA[" + 'I' + "]]>").append("</item_flg>");
							setNodeValue(dom, "item_flg", getAbsString("I"));
							valueXmlString.append("<item_code>").append("<![CDATA[" + itemCodeOrd + "]]>")
							.append("</item_code>");
							// valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" +
							// itemCodeOrd + "]]>").append("</item_code>");
							setNodeValue(dom, "item_code", getAbsString(itemCodeOrd));
							reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
						} else if ("F".equalsIgnoreCase(lsItemStru)
								&& (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)) {
							sql = "select count(*) as cnt from scheme_applicability where item_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("scheme_applicability cnt[" + cnt + "]");
							if (cnt > 1) {
								valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>")
								.append("</item_flg>");
								setNodeValue(dom, "item_flg", getAbsString("B"));
								valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[]]>")
								.append("</item_code>");
							} else {
								System.out.println("Else scheme_applicability");
								valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>")
								.append("</item_flg>");
								setNodeValue(dom, "item_flg", getAbsString("B"));
								System.out.println(
										"Else (F.equalsIgnoreCase(lsItemStru) lsSchemeCode[" + lsSchemeCode + "]");
								valueXmlString.append("<item_code protect = \"0\">")
								.append("<![CDATA[" + lsSchemeCode + "]]>").append("</item_code>");
								setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
								reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0, pos);
								valueXmlString.append(reStr);
							}
						} else if (!"F".equalsIgnoreCase(lsItemStru)
								&& (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)) {
							valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>").append("</item_flg>");
							setNodeValue(dom, "item_flg", getAbsString("B"));
							System.out.println("(! F.equalsIgnoreCase(lsItemStru) lsSchemeCode[" + lsSchemeCode + "]");
							valueXmlString.append("<item_code protect = \"0\">")
							.append("<![CDATA[" + lsSchemeCode + "]]>").append("</item_code>");
							setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
							reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
						} else if (!"F".equalsIgnoreCase(lsItemStru)
								&& (lsSchemeCode == null || lsSchemeCode.trim().length() == 0)) {
							valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[]]>")
							.append("</item_code>");
							System.out.println("ll_schcnt[" + ll_schcnt + "]");
							if (ll_schcnt >= 1) {
								valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>")
								.append("</item_flg>");
								setNodeValue(dom, "item_flg", getAbsString("B"));
								System.out.println(" (! F.equalsIgnoreCase(lsItemStru) && (lsSchemeCode == null["
										+ lsSchemeCode + "]");
								valueXmlString.append("<item_code>").append("<![CDATA[" + lsSchemeCode + "]]>")
								.append("</item_code>");
								setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
							} else {
								valueXmlString.append("<item_flg>").append("<![CDATA[" + 'I' + "]]>")
								.append("</item_flg>");
								setNodeValue(dom, "item_flg", getAbsString("I"));
								System.out.println("(itemCodeOrd1 == null[" + itemCodeOrd + "]");
								System.out.println(" (Else ! F.equalsIgnoreCase(lsItemStru) && (itemCodeOrd1 == null["
										+ itemCodeOrd1 + "]");
								valueXmlString.append("<item_code protect = \"1\">")
								.append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code>");
								setNodeValue(dom, "item_code", getAbsString(itemCodeOrd1));
							}
							reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
						}
						System.out.println("after change item code " + valueXmlString.toString());
						System.out.println("after itemchange itemCodeOrd" + itemCodeOrd);
						sql = "select pack_code from siteitem where  site_code = ? and    item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSiteCode);
						pstmt.setString(2, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPackCode = rs.getString("pack_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPackCode == null || lsPackCode.trim().length() <= 0) {
							sql = "select pack_code from item where item_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsPackCode = rs.getString("pack_code");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						valueXmlString.append("<pack_code>").append("<![CDATA[" + lsPackCode + "]]>")
						.append("</pack_code>");
						setNodeValue(dom, "pack_code", getAbsString(lsPackCode));

						sql = "select descr from packing where pack_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsPackCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<packing_descr>").append("<![CDATA[" + descr + "]]>")
						.append("</packing_descr>");
					}

					if (genericUtility.getColumnValue("order_date", dom1) != null
							&& genericUtility.getColumnValue("order_date", dom1).trim().length() > 0) {
						orderDate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
					}
					if (genericUtility.getColumnValue("pl_date", dom1) != null
							&& genericUtility.getColumnValue("pl_date", dom1).trim().length() > 0) {
						ldtPldate = Timestamp.valueOf(
								genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
										genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
					}
					Timestamp ldtDate = null;
					if (ldtPldate != null) {
						ldtDate = ldtPldate;
						ldtDateStr = genericUtility.getColumnValue("order_date", dom1);
					} else {
						ldtDate = orderDate;
						ldtDateStr = genericUtility.getColumnValue("pl_date", dom1);
					}
					lsPriceList = distCommon.getDisparams("999999", "STD_SO_PL", conn);
					if (lsPriceList != null || lsPriceList.trim().length() > 0) {
						lcStdrate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCodeOrd, "", "L", conn);
					}
					valueXmlString.append("<rate__std>").append("<![CDATA[" + lcStdrate + "]]>").append("</rate__std>");
					setNodeValue(dom, "rate__std", getAbsString(String.valueOf(lcStdrate)));

					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));

					lsSordCommCal = distCommon.getDisparams("999999", "SORD_COMM_CAL", conn);

					if (lsSordCommCal == null || lsSordCommCal.trim().length() == 0
							|| lsSordCommCal.equalsIgnoreCase("NULLFOUND")) {
						lsSordCommCal = "H";
					}
					if ("H".equalsIgnoreCase(lsSordCommCal)) {
						lcCommPerc = checkNull(genericUtility.getColumnValue("comm_perc", dom1));
						if (lcCommPerc != null && lcCommPerc.trim().length() > 0) {
							valueXmlString.append("<comm_perc_1>").append("<![CDATA[" + lcCommPerc + "]]>")
							.append("</comm_perc_1>");
							setNodeValue(dom, "comm_perc_1", getAbsString(lcCommPerc));
						}
						lcCommPerc1 = checkNull(genericUtility.getColumnValue("comm_perc_1", dom1));
						if (lcCommPerc1 != null && lcCommPerc1.trim().length() > 0) {
							valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + lcCommPerc1 + "]]>")
							.append("</comm_perc_2>");
							setNodeValue(dom, "comm_perc_2", getAbsString(lcCommPerc1));
						}
						lcCommPerc2 = checkNull(genericUtility.getColumnValue("comm_perc_2", dom1));
						if (lcCommPerc2 != null && lcCommPerc2.trim().length() > 0) {
							valueXmlString.append("<comm_perc_3>").append("<![CDATA[" + lcCommPerc2 + "]]>")
							.append("</comm_perc_3>");
							setNodeValue(dom, "comm_perc_3", getAbsString(lcCommPerc2));
						}
						lsCommPercOn = genericUtility.getColumnValue("comm_perc__on", dom1) == null ? " "
								: genericUtility.getColumnValue("comm_perc__on", dom1);
						if (lsCommPercOn.trim().length() > 0) {
							valueXmlString.append("<comm_perc_on_1>").append("<![CDATA[" + lsCommPercOn + "]]>")
							.append("</comm_perc_on_1>");
							setNodeValue(dom, "comm_perc_on_1", getAbsString(String.valueOf(lsCommPercOn)));
						}
						lsCommPercOn1 = checkNull(genericUtility.getColumnValue("comm_perc_on_1", dom1));
						if (lsCommPercOn1 != null && lsCommPercOn1.trim().length() > 0) {
							valueXmlString.append("<comm_perc_on_2>").append("<![CDATA[" + lsCommPercOn1 + "]]>")
							.append("</comm_perc_on_2>");
							setNodeValue(dom, "comm_perc_on_2", getAbsString(lsCommPercOn1));
						}
						lsCommPercOn2 = checkNull(genericUtility.getColumnValue("comm_perc_on_2", dom1));
						if (lsCommPercOn2 != null && lsCommPercOn2.trim().length() > 0) {
							valueXmlString.append("<comm_perc_on_3>").append("<![CDATA[" + lsCommPercOn2 + "]]>")
							.append("</comm_perc_on_3>");
							setNodeValue(dom, "comm_perc_on_3", getAbsString(lsCommPercOn2));
						}
					}
					mItemSer = distCommon.getItemSer(itemCodeOrd, lsSiteCode, orderDate, lsCustCode, "C", conn);
					toStation = checkNull(genericUtility.getColumnValue("stan_code", dom1));
					lsCustCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom1));
					lsSiteCodeDet = checkNull(genericUtility.getColumnValue("site_code", dom));	
					sql = "Select stan_code From site Where  site_code =?";
					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1, lsSiteCode);
					pstmt.setString(1, lsSiteCodeDet);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						frStation = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					lsTaxChapHdr = checkNull(genericUtility.getColumnValue("tax_chap", dom1));
					lsTaxClassHdr = checkNull(genericUtility.getColumnValue("tax_class", dom1));
					lsTaxEnvHdr = checkNull(genericUtility.getColumnValue("tax_env", dom1));

					//Changed by Pavan Rane on 06AUG18 [to set taxes based on a cust_tax_opt parameter in order type]									
					itemCodeDet = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
					lsItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));									
					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom1));
					orderType = checkNull(genericUtility.getColumnValue("order_type",dom1));
					sql = "select cust_tax_opt from sordertype where order_type = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	

					System.out.println("orderType:["+orderType+"]  custTaxOpt["+custTaxOpt+"]");
					//for default customer as dlv_cust if null found
					if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
					{
						custTaxOpt = "0";
					}
					sql = "select stan_code from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql); 
					if("1".equals(custTaxOpt))	{
						pstmt.setString(1, custCodeBill);
						custCodeTax = custCodeBill;
					}else if("0".equals(custTaxOpt)) {						
						pstmt.setString(1, lsCustCodeDlv);
						custCodeTax = lsCustCodeDlv;
					}										
					rs = pstmt.executeQuery();
					if (rs.next()) 	{						
						stanCodeTo = rs.getString("stan_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println(">> 8650 stanCodeTo ["+stanCodeTo+"]");			


					//if (lsTaxChapHdr == null || lsTaxChapHdr.trim().length() == 0) {
					lsTaxChap = distCommon.getTaxChap(itemCodeDet, lsItemSer, "C", custCodeTax, lsSiteCodeDet, conn);
					//} else {
					//lsTaxChap = lsTaxChapHdr;
					//}
					//if (lsTaxClassHdr == null || lsTaxChapHdr.trim().length() == 0) {
					lsTaxClass = distCommon.getTaxClass("C", custCodeTax, itemCodeDet, lsSiteCodeDet, conn);
					//} else {
					//lsTaxClass = lsTaxClassHdr;
					//}
					if (lsTaxEnvHdr == null || lsTaxEnvHdr.trim().length() == 0) {
						sql = "select tax_env from customeritem where cust_code = ? and item_code =?";
						pstmt = conn.prepareStatement(sql);
						if("1".equals(custTaxOpt))	{
							pstmt.setString(1, custCodeBill);							
						}else if("0".equals(custTaxOpt)) {						
							pstmt.setString(1, lsCustCodeDlv);					
						}											
						pstmt.setString(2, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsTaxEnv = rs.getString("tax_env");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsTaxEnv == null || lsTaxEnv.trim().length() == 0) {
							lsTaxEnv = distCommon.getTaxEnv(frStation, stanCodeTo, lsTaxChap, lsTaxClass, lsSiteCodeDet,conn);
						}
					} else {
						lsTaxEnv = lsTaxEnvHdr;
					}
					//Changed by Pavan Rane on 06AUG18 End 
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + lsTaxChap + "]]>").append("</tax_chap>");
					setNodeValue(dom, "tax_chap", getAbsString(lsTaxChap));
					valueXmlString.append("<tax_class>").append("<![CDATA[" + lsTaxClass + "]]>")
					.append("</tax_class>");
					setNodeValue(dom, "tax_class", getAbsString(lsTaxClass));
					valueXmlString.append("<tax_env>").append("<![CDATA[" + lsTaxEnv + "]]>").append("</tax_env>");
					setNodeValue(dom, "tax_env", getAbsString(lsTaxEnv));

					//Pavan Rane 31jul19 Start [Changes for Sales order near expiry logic to be corrected]
					//lcMinShelfLife = checkNull(genericUtility.getColumnValue("min_shelf_life", dom1));
					//llMaxShelfLifeDet = checkNull(genericUtility.getColumnValue("max_shelf_life", dom1));					
					lcMinShelfLife = checkDoubleNull(genericUtility.getColumnValue("min_shelf_life", dom));
					llMaxShelfLifeDet = checkDoubleNull(genericUtility.getColumnValue("max_shelf_life", dom));
					lsSorder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
					lsPlistDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
					if (lcMinShelfLife == 0) //if (lcMinShelfLife == null || lcMinShelfLife.trim().length() == 0) {
					{
						sql = "select min_shelf_life ,max_shelf_life from sordertype where order_type =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsOrderType);
						rs = pstmt.executeQuery();
						if (rs.next())	{													
							lcMinShelfLife = rs.getDouble("min_shelf_life"); //rs.getString("min_shelf_life");
							llMaxShelfLifeDet = rs.getDouble("max_shelf_life"); //rs.getString("max_shelf_life");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lcMinShelfLife == 0) //if (lcMinShelfLife == null || lcMinShelfLife.trim().length() == 0) {
					{
						sql = "select min_shelf_life from customeritem where cust_code = ? and item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {														
							lcMinShelfLife = rs.getDouble("min_shelf_life"); //lcMinShelfLife = rs.getString("min_shelf_life"); 
							// llMaxShelfLifeDet = rs.getString("max_shelf_life");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lcMinShelfLife == 0) //if (lcMinShelfLife == null || lcMinShelfLife.trim().length() == 0) {
					{
						sql = "select case when min_shelf_perc is null then 0 else min_shelf_perc end as min_shelf_perc "
								+ " from customer_series where cust_code = ? and item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {							
							lcMinLifePerc = rs.getDouble("min_shelf_perc"); //lcMinLifePerc = rs.getString("min_shelf_perc");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lcMinLifePerc > 0) //if (lcMinLifePerc != null || lcMinLifePerc.trim().length() > 0) {					 
					{
						sql = "select (case when shelf_life is null then 0 else shelf_life end ) as shelf_life from item where  item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {							
							llShelfLife = rs.getDouble("shelf_life"); //llShelfLife = rs.getString("shelf_life");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;						
						if (llShelfLife > 0) 
						{							
							lcMinShelfLife = Math.round((lcMinLifePerc / 100) * llShelfLife);
							llMaxShelfLife = llShelfLife;
						}
					}
					/*llShelfLife = (llShelfLife == null || llShelfLife.trim().length() == 0) ? "0" : llShelfLife;
					if (Integer.parseInt(llShelfLife) > 0)
					if (llShelfLife > 0) 
					{
						/*lcMinShelfLife = String.valueOf(Math.round((Double.parseDouble(checkDouble(lcMinLifePerc)) / 100)* Double.parseDouble(checkDouble(llShelfLife))));
						lcMinShelfLife = Math.round((lcMinLifePerc / 100) * llShelfLife);
						llMaxShelfLife = llShelfLife;
					} else {
						lcMinShelfLife = 0; //"0";
						llMaxShelfLife = 0;	//"0";
					}*/
					//if (lcMinShelfLife == null || lcMinShelfLife.trim().length() == 0) {
					if (lcMinShelfLife == 0) 
					{
						sql = "select min_shelf_life from customer where cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {							
							lcMinShelfLife = rs.getDouble("min_shelf_life");  //lcMinShelfLife = rs.getString("min_shelf_life");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lcMinShelfLife == 0)//if (lcMinShelfLife == null || lcMinShelfLife.trim().length() == 0) {
					{
						sql = "select min_shelf_life from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							//lcMinShelfLife = rs.getString("min_shelf_life");
							lcMinShelfLife = rs.getDouble("min_shelf_life");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					//---Commented conditions by Jaffar S as suggested by SM Sir on 29-01-2019
					//if (lsPriceListDisc != null && lsPriceListDisc.trim().length() > 0) {
					/*sql = "select order_type from sorder where sale_order =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSaleOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsSalesOrderType = rs.getString("order_type");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;*/						
					//if ("NE".equalsIgnoreCase(lsSalesOrderType)) {
					if ("NE".equalsIgnoreCase(lsOrderType)) 
					{
						sql = "select (case when no_sales_month is null then 0 else no_sales_month end) as no_sales_month from item where  item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSaleOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {								
							llMaxShelfLife = rs.getInt("no_sales_month"); //llMaxShelfLife = rs.getString("no_sales_month");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;							 
						if (llMaxShelfLife == 0)  //if (Double.parseDouble(checkDouble(llMaxShelfLife)) == 0)
						{
							lsVarvalue = distCommon.getDisparams("999999", "NEAR_EXP_SHELF_LIFE", conn);
							//if (lsVarvalue == null || lsVarvalue.trim().length() == 0 || lsVarvalue.equalsIgnoreCase("NULLFOUND")) {
							if (lsVarvalue != null && lsVarvalue.trim().length() > 0 ) 
							{									
								llMaxShelfLife = checkDoubleNull(lsVarvalue); //llMaxShelfLife = lsVarvalue;
							} 
						}						
						double llTemp = llMaxShelfLife;
						llMaxShelfLife = lcMinShelfLife;
						lcMinShelfLife = llTemp;
					} else 
					{							
						llMaxShelfLife = 0;
					}
					//}
					//if (Double.parseDouble(checkDouble(llMaxShelfLifeDet)) > 0) {
					//Pavan Rane 31jul19 End [Changes for Sales order near expiry logic to be corrected]
					if (llMaxShelfLifeDet > 0) {
						llMaxShelfLife = llMaxShelfLifeDet;
					}
					valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + lcMinShelfLife + "]]>").append("</min_shelf_life>");
					setNodeValue(dom, "min_shelf_life", String.valueOf(lcMinShelfLife));
					valueXmlString.append("<max_shelf_life>").append("<![CDATA[" + llMaxShelfLife + "]]>").append("</max_shelf_life>");
					setNodeValue(dom, "max_shelf_life", String.valueOf(llMaxShelfLife));

					if ((CommonConstants.DB_NAME).equalsIgnoreCase("mssql")) {
						sql = "select [dbo].fn_get_itmstk(?, ?) as lc_stk_qty from dual";
					} else {
						sql = "select fn_get_itmstk(?,?) as lc_stk_qty from dual";
					}
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					pstmt.setString(2, lsSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcStkQty = rs.getString("lc_stk_qty");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<stk_qty>").append("<![CDATA[" + lcStkQty + "]]>").append("</stk_qty>");
					//setNodeValue(dom, "stk_qty", getAbsString(String.valueOf(llMaxShelfLife)));
					setNodeValue(dom, "stk_qty", getAbsString(String.valueOf(lcStkQty)));
					sql = "select appl_seg from customeritem where cust_code = ? and item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, itemCodeOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsApplseg = rs.getString("appl_seg");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsApplseg == null || lsApplseg.trim().length() == 0) {
						sql = "select appl_seg from item where  item_code = ?";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsApplseg = rs.getString("appl_seg");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					valueXmlString.append("<appl_seg>").append("<![CDATA[" + checkNull(lsApplseg) + "]]>")
					.append("</appl_seg>");
					setNodeValue(dom, "appl_seg", getAbsString(lsApplseg));

					lsSordCommCal = distCommon.getDisparams("999999", "SORD_COMM_CAL", conn);

					if (lsSordCommCal == null || lsSordCommCal.trim().length() == 0
							|| lsSordCommCal.equalsIgnoreCase("NULLFOUND")) {
						lsSordCommCal = "H";
					}
					if ("D".equalsIgnoreCase(lsSordCommCal)) {
						lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						lsSalesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom1));
						lsSalesPers1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom1));
						lsSalesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom1));

						sql = "select comm_table__1 from customer_series where cust_code =? and item_ser  =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable1 = rs.getString("comm_table__1");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsCommTable1 == null || lsCommTable1.trim().length() == 0) {
						sql = "select comm_table__1 from   sales_pers where  sales_pers =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSalesPers);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable1 = rs.getString("comm_table__1");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					if (lsCommTable1 == null || lsCommTable1.trim().length() == 0) {
						sql = "select comm_table__1 from   customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable1 = rs.getString("comm_table__1");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsCommTable1 != null && lsCommTable1.trim().length() > 0) {
						sql = "select count(*) as cnt from comm_det where comm_table = ? and item_code= ? and eff_date   <= ? and valid_upto >=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCommTable1);
						pstmt.setString(2, itemCodeOrd);
						pstmt.setTimestamp(3, orderDate);
						pstmt.setTimestamp(4, orderDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 1) {
							sql = "select (case when comm_perc is null then 0 else comm_perc end) as cnt,"
									+ " comm_perc__on from comm_det where comm_table = ? and item_code	= ? and	 eff_date   <= ? and valid_upto >=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable1);
							pstmt.setString(2, itemCodeOrd);
							pstmt.setTimestamp(3, orderDate);
							pstmt.setTimestamp(4, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
								descr = rs.getString("comm_perc__on");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (cnt == 0 || descr.trim().length() == 0) {
						sql = "select (case when comm_perc is null then 0 else comm_perc end) as cnt,"
								+ " comm_perc__on from customer_series where cust_code = ? and item_ser  =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
							descr = rs.getString("comm_perc__on");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 0) {
							sql = "select comm_perc,comm_perc__on from sales_pers where sales_pers =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsSalesPers);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt1 = rs.getInt("comm_perc");
								descr = rs.getString("comm_perc__on");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					valueXmlString.append("<comm_perc_1>").append("<![CDATA[" + cnt1 + "]]>").append("</comm_perc_1>");
					setNodeValue(dom, "comm_perc_1", getAbsString(String.valueOf(cnt1)));
					if (cnt1 > 0) {
						valueXmlString.append("<comm_perc_on_1>").append("<![CDATA[" + descr + "]]>")
						.append("</comm_perc_on_1>");
						setNodeValue(dom, "comm_perc_on_1", getAbsString(descr));
					}

					sql = "select comm_table__2 from customer_series where  cust_code = ? and item_ser  =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, mItemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsCommTable2 = rs.getString("comm_table__2");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsCommTable2 == null || lsCommTable2.trim().length() == 0) {
						sql = "select comm_table__2 from sales_pers where sales_pers =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSalesPers);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable2 = rs.getString("comm_table__2");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsCommTable2 == null || lsCommTable2.trim().length() == 0) {
						sql = "select comm_table__2 from customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable2 = rs.getString("comm_table__2");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsCommTable2 != null && lsCommTable2.trim().length() > 0) {
						sql = "select count(*) as cnt from comm_det where comm_table = ? and item_code= ? and eff_date <= ? and valid_upto >= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCommTable2);
						pstmt.setString(2, itemCodeOrd);
						pstmt.setTimestamp(3, orderDate);
						pstmt.setTimestamp(4, orderDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 1) {
							sql = "select (case when comm_perc is null then 0 else comm_perc end) as cnt, comm_perc__on from comm_det where comm_table = ? and item_code= ? and eff_date <= ? and valid_upto >=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable2);
							pstmt.setString(2, itemCodeOrd);
							pstmt.setTimestamp(3, orderDate);
							pstmt.setTimestamp(4, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
								descr = rs.getString("comm_perc__on");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (cnt == 0 || (descr == null || descr.trim().length() == 0)) {
						sql = "select (case when comm_perc__1 is null then 0 else comm_perc__1 end) as cnt , comm_perc__on_1 from customer_series where cust_code = ? and item_ser=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
							descr = rs.getString("comm_perc__on_1");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (cnt == 0 || (descr == null || descr.trim().length() == 0)) {
						sql = "select comm_perc,comm_perc__on from sales_pers where sales_pers =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSalesPers);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt1 = rs.getInt("comm_perc");
							descr = rs.getString("comm_perc__on");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + cnt1 + "]]>").append("</comm_perc_2>");
					setNodeValue(dom, "comm_perc_2", getAbsString(String.valueOf(cnt1)));
					if (cnt1 > 0) {
						valueXmlString.append("<comm_perc_on_2>").append("<![CDATA[" + descr + "]]>")
						.append("</comm_perc_on_2>");
						setNodeValue(dom, "comm_perc_on_2", getAbsString(descr));
					}

					sql = "select comm_table__3 from customer_series where cust_code = ? and item_ser=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, mItemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsCommTable3 = rs.getString("comm_table__3");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsCommTable3 == null || lsCommTable3.trim().length() == 0) {
						sql = "select comm_table__3 from sales_pers where sales_pers =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSalesPers);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable3 = rs.getString("comm_table__3");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsCommTable3 == null || lsCommTable3.trim().length() == 0) {
						sql = "select comm_table__3 from customer where  cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsCommTable3 = rs.getString("comm_table__3");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (lsCommTable3 != null && lsCommTable3.trim().length() > 0) {
						sql = "select count(*) as cnt from comm_det where comm_table = ? and item_code= ? and eff_date<= ? and valid_upto >= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCommTable3);
						pstmt.setString(2, itemCodeOrd);
						pstmt.setTimestamp(3, orderDate);
						pstmt.setTimestamp(4, orderDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 1) {
							sql = "select (case when comm_perc is null then 0 else comm_perc end)as cnt , comm_perc__on from comm_det where comm_table = ? and item_code= ? and eff_date <= ? and valid_upto >= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCommTable3);
							pstmt.setString(2, itemCodeOrd);
							pstmt.setTimestamp(3, orderDate);
							pstmt.setTimestamp(4, orderDate);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
								descr = rs.getString("comm_perc__on");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (cnt == 0 || (descr == null || descr.trim().length() == 0)) {
						sql = "select (case when comm_perc__2 is null then 0 else comm_perc__2 end) as cnt , comm_perc__on_2 from customer_series where cust_code = ? and item_ser  =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
							descr = rs.getString("comm_perc__on_2");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (cnt == 0 || (descr == null || descr.trim().length() == 0)) {
						sql = "select comm_perc,comm_perc__on from   sales_pers where  sales_pers =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSalesPers);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt1 = rs.getInt("comm_perc");
							descr = rs.getString("comm_perc__on");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<comm_perc_3>").append("<![CDATA[" + cnt1 + "]]>").append("</comm_perc_3>");
					setNodeValue(dom, "comm_perc_3", getAbsString(String.valueOf(cnt1)));
					if (cnt1 > 0) {
						valueXmlString.append("<comm_perc_on_3>").append("<![CDATA[" + descr + "]]>")
						.append("</comm_perc_on_3>");
						setNodeValue(dom, "comm_perc_on_3", getAbsString(descr));
					}
					if ((CommonConstants.DB_NAME).equalsIgnoreCase("mssql")) {
						sql = "select [dbo].fn_get_availstk(?, ?) as lc_stk_qty from dual";
					} else {
						sql = "select fn_get_availstk(?,?) as lc_stk_qty from dual";
					}
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					pstmt.setString(2, lsSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcStkQty = rs.getString("lc_stk_qty");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<avail_qty>").append("<![CDATA[" + lcStkQty + "]]>").append("</avail_qty>");
					setNodeValue(dom, "avail_qty", getAbsString(lcStkQty));

					reStr = itemChangedDet(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

				}
				if (currentColumn.trim().equalsIgnoreCase("item_code")) {
					System.out.println("======item code=========");
					String toStation = "", lsItemSerProm = "", lsItemSerCrPerc = "", lsSpecs = "", lsLineNoContr = "",
							mDescr = "", uom = "", uomr = "", mType = "", mPackinStr = "";
					String lsUnitSal = "", lsItemFlg = "", lsItemDesc = "", mDescr1 = "", lsNature = "",
							lsSaleOrder = "", unit = "", lsUnit = "", lsItemDescr = "", lsUnitRate = "";
					String lsPackCode = "", lsPackInstr = "", llNoArt = "", lsUnitStd = "", lsDiscPriceList = "",
							lsSpecId = "";
					String lrdateStr = "";
					double lsQty = 0.00, mRate = 0.00, idRateWtDiscount = 0.00, lcRate = 0.00, mRateClg = 0.00,
							ldRate = 0.00, lcQuantity = 0.00, lcDiscount = 0.00, lcTaxAmt = 0.00;
					double lcNetAmt = 0.00, ldQtyStdum = 0.00, lcRateStduom = 0.00, ldConveQtyStduom = 0.00,
							lcConvRtuomStduom = 0.00, mQty = 0.00, lcPlistDisc = 0.00;
					double lcQtyStd = 0.00, acShipperQty = 0.00, acIntQty = 0.00;
					Date ldDespDate = null;
					boolean lsDiscFlag = false;

					lsItemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					Timestamp orderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
					lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));

					Timestamp ldtPldate = Timestamp
							.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
					Timestamp ldtDate = null;

					if (ldtPldate != null) {
						ldtDate = ldtPldate;
					} else {
						ldtDate = orderDate;
					}
					// Start Added by chandrshekar on 16-aug-2016
					if (ldtDate != null) {
						Date date = new Date(ldtDate.getTime());
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
						lrdateStr = simpleDateFormat.format(date);
					}
					// End Added by chandrshekar on 16-aug-2016
					toStation = checkNull(genericUtility.getColumnValue("stan_code", dom1));
					System.out.println("lsItemCodeOrd[" + lsItemCodeOrd + "]");
					System.out.println("lsSiteCode[" + lsSiteCode + "]");
					System.out.println("orderDate[" + orderDate + "]");
					System.out.println("lsCustCode[" + lsCustCode + "]");
					if (lsItemCodeOrd != null && lsItemCodeOrd.trim().length() > 0) {

						mItemSer = distCommon.getItemSer(lsItemCodeOrd, lsSiteCode, orderDate, lsCustCode, "C", conn);
						System.out.println("lsItemCodeOrd is not null mItemSer" + mItemSer);
					} else {
						mItemSer = distCommon.getItemSer(lsItemCode, lsSiteCode, orderDate, lsCustCode, "C", conn);
						System.out.println("lsItemCodeOrd is not null mItemSer" + mItemSer);
					}

					lsCustPord = checkNull(genericUtility.getColumnValue("cust_pord", dom1));

					sql = "select dis_link,channel_partner from site_customer where cust_code=? and site_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, lsSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsDisLink = rs.getString("dis_link");
						lsChannelPartner = rs.getString("channel_partner");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsChannelPartner == null || lsChannelPartner.trim().length() == 0) {
						sql = "select dis_link,channel_partner from customer where cust_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsDisLink = rs.getString("dis_link");
							lsChannelPartner = rs.getString("channel_partner");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					if ("A".equalsIgnoreCase(lsDisLink) && "Y".equalsIgnoreCase(lsChannelPartner)) {
						if (lsCustPord != null && lsCustPord.trim().length() > 0) {
							sql = "select count(*) as cnt from porddet where purc_order = ? and item_code  =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustPord);
							pstmt.setString(2, lsItemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								// errcode = "VTPODET" //Invalid PO
							}
						}
					}

					lsQty = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));

					if (lsItemCodeOrd != null && lsItemCodeOrd.trim().length() > 0) {
						lsItemSerProm = distCommon.getItemSer(lsItemCodeOrd, lsSiteCode, orderDate, lsCustCode, "O",
								conn);
					} else {
						lsItemSerProm = distCommon.getItemSer(lsItemCode, lsSiteCode, orderDate, lsCustCode, "O", conn);
					}

					sql = "select count(*) as cnt from customer_series where cust_code = ? and item_ser =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, lsItemSerProm);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("cnt=========[" + cnt + "]");
					if (cnt == 0) {
						System.out.println("cnt=========000");
						sql = "select item_ser from item_credit_perc where item_code = ?"
								+ " and item_ser in ( select item_ser from customer_series where cust_code = ?"
								+ " and item_ser  = item_credit_perc.item_ser)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsItemCode);
						pstmt.setString(2, lsCustCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsItemSerCrPerc = rs.getString("item_ser");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsItemSerCrPerc != null && lsItemSerCrPerc.trim().length() > 0) {
							System.out.println("cnt=========111");
							valueXmlString.append("<item_ser__prom>").append("<![CDATA[" + lsItemSerCrPerc + "]]>")
							.append("</item_ser__prom>");
							setNodeValue(dom, "item_ser__prom", getAbsString(lsItemSerCrPerc));

							sql = "select item_ser__inv from customer_series where cust_code = ? and item_ser =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCustCode);
							pstmt.setString(2, lsItemSerCrPerc);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsItemser = rs.getString("item_ser__inv");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("lsItemser cnt=========111[" + lsItemser + "]");
							valueXmlString.append("<item_ser>").append("<![CDATA[" + lsItemser + "]]>")
							.append("</item_ser>");
							setNodeValue(dom, "item_ser", getAbsString(lsItemser));
						} else {
							System.out.println("lsItemser cnt=========111 e;[" + lsItemSerProm + "]");
							valueXmlString.append("<item_ser__prom>").append("<![CDATA[" + lsItemSerProm + "]]>")
							.append("</item_ser__prom>");
							setNodeValue(dom, "item_ser__prom", getAbsString(lsItemSerProm));
						}
					} else {
						System.out.println("lsItemser cnt=========111 e;[" + lsItemSerProm + "]");
						valueXmlString.append("<item_ser__prom>").append("<![CDATA[" + lsItemSerProm + "]]>")
						.append("</item_ser__prom>");
						setNodeValue(dom, "item_ser__prom", getAbsString(lsItemSerProm));
					}

					if ((CommonConstants.DB_NAME).equalsIgnoreCase("mssql")) {
						sql = "select ltrim(rtrim(item_parnt)) + ' , ' + item_type.phy_attrib_1 + ' : '"
								+ "  (case when item.phy_attrib_1 is null then '' else item.phy_attrib_1 end) + ' , ' + item_type.phy_attrib_2 + ' : '"
								+ " (case when item.phy_attrib_2 is null then '' else item.phy_attrib_2 end) + ' , ' + item_type.phy_attrib_3 + ' : '"
								+ "  (case when item.phy_attrib_3 is null then '' else item.phy_attrib_3 end) + ' , ' + item_type.phy_attrib_4 + ' : '"
								+ " (case when item.phy_attrib_4 is null then '' else item.phy_attrib_4 end) + ' , ' + item_type.phy_attrib_5 + ' : '"
								+ " (case when item.phy_attrib_5 is null then '' else item.phy_attrib_5 end) as ls_specs"
								+ " from item, item_type where item.item_type = item_type.item_type and item.item_code =?";

					} else {
						sql = "select ltrim(rtrim(item_parnt)) ||' , '|| item_type.phy_attrib_1 || ' : '"
								+ " ||(case when item.phy_attrib_1 is null then '' else item.phy_attrib_1 end) || ' , '||item_type.phy_attrib_2 || ' : '"
								+ "||(case when item.phy_attrib_2 is null then '' else item.phy_attrib_2 end) ||' , ' || item_type.phy_attrib_3|| ' : '"
								+ "||(case when item.phy_attrib_3 is null then '' else item.phy_attrib_3 end) || ' , '||item_type.phy_attrib_4 || ' : '"
								+ "||(case when item.phy_attrib_4 is null then '' else item.phy_attrib_4 end) ||' , ' ||item_type.phy_attrib_5 || ' : '"
								+ "||(case when item.phy_attrib_5 is null then '' else item.phy_attrib_5 end) as ls_specs"
								+ " from item, item_type where item.item_type = item_type.item_type and item.item_code =?";
					}
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsItemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsSpecs = rs.getString("ls_specs");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsSpecs != null && lsSpecs.trim().length() > 0) {
						valueXmlString.append("<item_specs>").append("<![CDATA[" + lsSpecs + "]]>")
						.append("</item_specs>");
						setNodeValue(dom, "item_specs", getAbsString(lsSpecs));
					} else {
						valueXmlString.append("<item_specs>").append("<![CDATA[]]>").append("</item_specs>");
						setNodeValue(dom, "item_specs", getAbsString(""));
					}

					// 08-sep-2020 manoharan contract is already there use the same
					//ContractNo = "";
					//lsContractNo = "";
					ContractNo = genericUtility.getColumnValue("contract_no", dom);
					lsContractNo = genericUtility.getColumnValue("contract_no", dom);
					lsLineNoContr = genericUtility.getColumnValue("line_no__contr", dom);
					if(ContractNo == null ||ContractNo.trim().length() == 0 || lsLineNoContr == null ||lsLineNoContr.trim().length() == 0 )	
					{
						//Modified by Yashwant S. on 19-10-2020[START][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
						lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
						lsLineNoContr = checkNull(genericUtility.getColumnValue("line_no__contr", dom));
						// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
						if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
						{
							lsContractNo = null;
						}
						if(lsLineNoContr != null && "null".equalsIgnoreCase(lsLineNoContr.trim()))
						{
							lsLineNoContr = null;
						}
						// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
						//Modified by Yashwant S. on 19-10-2020[END][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]					
					
						ContractNo = getContract(lsSiteCode, lsCustCode, orderDate, lsItemCode, lsContractNo, lsLineNoContr,
								conn);// Change by chandrashekar on 10-o8-2016
                        System.out.println("ContractNo=====[" + ContractNo + "]");
                        // 18-sep-2020 manoharan take care of null
						//if (ContractNo.trim().length() > 0) {
                        if (ContractNo != null && ContractNo.trim().length() > 0) {
							String[] arrStr = ContractNo.split("@");
							if (arrStr.length > 0) {
								lsContractNo = arrStr[0];
							}
							if (arrStr.length > 1) {
								lsLineNoContr = arrStr[1];
							}
						}
					}
					// Modified by Sana  S on 23-11-2020
					// if (lsContractNo == null || lsContractNo.trim().length() == 0) {
					if (lsContractNo == null || lsContractNo.trim().length() == 0 || "null".equalsIgnoreCase(lsContractNo.trim())) {
                        System.out.println("ContractNo== NUll");
                        //added by Sana S on 23-11-2020 [start]
                        lsLineNoContr="";
                        //added by Sana S on 23-11-2020 [end]
						valueXmlString.append("<contract_no>").append("<![CDATA[" + lsContractNo + "]]>")
						.append("</contract_no>");
						setNodeValue(dom, "contract_no", getAbsString(lsContractNo));
						valueXmlString.append("<line_no__contr>").append("<![CDATA[" + lsLineNoContr + "]]>")
						.append("</line_no__contr>");
						setNodeValue(dom, "line_no__contr", getAbsString(lsLineNoContr));
						System.out.println("lsItemCode" + lsItemCode);
						sql = "Select descr,unit,unit__rate,item_stru,pack_instr,unit__sal from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsItemCode);
						// pstmt.setString(2, lsItemSerCrPerc);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mDescr = rs.getString("descr");
							uom = rs.getString("unit");
							uomr = rs.getString("unit__rate");
							mType = rs.getString("item_stru");
							mPackinStr = rs.getString("pack_instr");
							lsUnitSal = rs.getString("unit__sal");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ((uom != null && uom.trim().length() > 0) && (uomr != null && uomr.trim().length() > 0)) {
							valueXmlString.append("<item_ser>").append("<![CDATA[" + mItemSer + "]]>")
							.append("</item_ser>");
							setNodeValue(dom, "item_ser", getAbsString(mItemSer));
							System.out.println("lsItemser uom != null[" + mItemSer);

							lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));

							sql = "select count(*) as cnt from item where item_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("cnt[" + cnt + "]");
							if (cnt > 0) {
								sql = "select descr from bom where bom_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsItemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									descr = rs.getString("descr");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("descr[" + descr + "]");
							}
							lsItemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
							System.out.println("descr for bom ::["+descr+"] lsItemFlg :::["+lsItemFlg+"]");
							if ("B".equalsIgnoreCase(lsItemFlg)) {
								lsItemDesc = mDescr + " " + descr;
							} else {
								lsItemDesc = mDescr;
							}
							// setNodeValue( dom, "sch_attr", getAbsString("Y"));
							String schAttr = checkNull(genericUtility.getColumnValue("sch_attr", dom));
							System.out.println("9750::schAttr[" + schAttr + "]");
							if (schAttr != null && "N".equalsIgnoreCase(schAttr)) {
								System.out.println("lsItemDesc[" + lsItemDesc + "]");
								valueXmlString.append("<item_descr>").append("<![CDATA[" + lsItemDesc + "]]>")
								.append("</item_descr>");
								setNodeValue(dom, "item_descr", getAbsString(lsItemDesc));
							}
							/*
							 * System.out.println("lsItemDesc["+lsItemDesc+"]");
							 * valueXmlString.append("<item_descr>").append("<![CDATA[" + lsItemDesc +
							 * "]]>").append("</item_descr>"); setNodeValue( dom, "item_descr",
							 * getAbsString( lsItemDesc));
							 */

							if (lsUnitSal == null || lsUnitSal.trim().length() == 0) {
								lsUnitSal = uom;
							}
							System.out.println("lsUnitSal[" + lsUnitSal + "]");
							valueXmlString.append("<unit>").append("<![CDATA[" + lsUnitSal + "]]>").append("</unit>");
							setNodeValue(dom, "unit", getAbsString(lsUnitSal));
							reStr = itemChangedDet(dom, dom1, dom2, objContext, "unit", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
							System.out.println("Unit ItemChangeD Called[" + valueXmlString.toString() + "]");
							valueXmlString.append("<unit__std>").append("<![CDATA[" + uom + "]]>")
							.append("</unit__std>");
							setNodeValue(dom, "unit__std", getAbsString(uom));
							valueXmlString.append("<unit__rate>").append("<![CDATA[" + uomr + "]]>")
							.append("</unit__rate>");
							setNodeValue(dom, "unit__rate", getAbsString(uomr));
							valueXmlString.append("<pack_instr>").append("<![CDATA[" + mPackinStr + "]]>")
							.append("</pack_instr>");
							setNodeValue(dom, "pack_instr", getAbsString(mPackinStr));
							if ("F".equalsIgnoreCase(mType)) {
								valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>")
								.append("</item_flg>");
								setNodeValue(dom, "item_flg", getAbsString("B"));
							} else {
								valueXmlString.append("<item_flg>").append("<![CDATA[" + 'I' + "]]>")
								.append("</item_flg>");
								setNodeValue(dom, "item_flg", getAbsString("I"));
							}
						} else {
							System.out.println("UOM else");
							lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
							System.out.println("item_code__ord[" + lsItemCodeOrd + "]");
							sql = "select count(*) as cnt from item where item_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("cnt[" + cnt);
							if (cnt > 0) {
								sql = "select descr from item where item_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsItemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									mDescr1 = rs.getString("descr");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							System.out.println("lsItemCode[" + lsItemCode + "]");
							System.out.println("lsItemCodeOrd[" + lsItemCodeOrd + "]");
							sql = "Select descr, unit from bom where bom_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								descr = rs.getString("descr");
								uom = rs.getString("unit");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//Added by Varsha V on 07-09-18 for resolving Koye issue 7[bom description was not setting in item description]
							lsItemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
							System.out.println("descr for bom ::["+descr+"] lsItemFlg :::["+lsItemFlg+"]");
							//Ended by Varsha V on 07-09-18 for resolving Koye issue 7[bom description was not setting in item description
							if ("B".equalsIgnoreCase(lsItemFlg)) {
								lsItemDesc = mDescr1 + " " + descr;
							} else {
								lsItemDesc = mDescr1;
							}
							System.out.println("lsItemDesc ::["+lsItemDesc+"] ");
							////////////// added by aru pal 13-12-17
							String schAttr = checkNull(genericUtility.getColumnValue("sch_attr", dom));

							// if(!("Y").equalsIgnoreCase(schAttr))
							// {
							valueXmlString.append("<item_descr>").append("<![CDATA[" + lsItemDesc + "]]>")
							.append("</item_descr>");
							setNodeValue(dom, "item_descr", getAbsString(lsItemDesc));
							valueXmlString.append("<unit>").append("<![CDATA[" + uom + "]]>").append("</unit>");
							setNodeValue(dom, "unit", getAbsString(uom));

							valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>").append("</item_flg>");
							setNodeValue(dom, "item_flg", getAbsString("B"));
							valueXmlString.append("<unit__std>").append("<![CDATA[" + uom + "]]>")
							.append("</unit__std>");
							setNodeValue(dom, "unit__std", getAbsString(uom));
							valueXmlString.append("<unit__rate>").append("<![CDATA[" + uom + "]]>")
							.append("</unit__rate>");
							setNodeValue(dom, "unit__rate", getAbsString(uom));
							// }
							reStr = itemChangedDet(dom, dom1, dom2, objContext, "unit", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
						}
						mPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
						lsItemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
						lsNature = checkNull(genericUtility.getColumnValue("nature", dom));
						Double quantity = Double
								.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
						System.out.print("Quantity1++++++++" + quantity);

						if ("F".equalsIgnoreCase(lsNature) || "B".equalsIgnoreCase(lsNature)
								|| "S".equalsIgnoreCase(lsNature)) {
							mRate = 0.00;
						} else {
							if (mPriceList != null && mPriceList.trim().length() > 0) {
								mRate = distCommon.pickRate(mPriceList, lrdateStr, lsItemCodeOrd, "", "L", quantity,
										conn);
								System.out.print("mrate inside item 752++++++++" + mRate);
							}
							if (mPriceList != null && mPriceList.trim().length() > 0) {
								lsListType = distCommon.getPriceListType(mPriceList, conn);

								if ("B".equalsIgnoreCase(lsListType) && mRate < 0) {
									mRate = 0.00;
								}
							}
						}
						idRateWtDiscount = mRate;

						lcRate = Double
								.parseDouble(checkDouble(checkDouble(genericUtility.getColumnValue("rate", dom))));

						if (lcRate == 0) {
							valueXmlString.append("<rate>").append("<![CDATA[" + mRate + "]]>").append("</rate>");
							setNodeValue(dom, "rate", getAbsString(String.valueOf(mRate)));
						}
						System.out.println("Rate is:" + mRate);
						mPriceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));
						System.out.println("mPriceListClg is:" + mPriceListClg);
						if (mPriceListClg != null && mPriceListClg.trim().length() > 0) {
							mRateClg = distCommon.pickRate(mPriceListClg, lrdateStr, lsItemCodeOrd, "", "L", lsQty,
									conn);
							System.out.println("mRateClg" + mRateClg);
						}
						System.out.println("Pass mRateClg" + mRateClg);
						lsListType = distCommon.getPriceListType(mPriceListClg, conn);
						System.out.println("lsListType" + lsListType);
						System.out.println("mRateClg" + mRateClg);
						if ("B".equalsIgnoreCase(lsListType) && mRateClg < 0) {
							mRateClg = 0.00;
						}
						if (mRateClg <= 0) {
							System.out.println("mRateClg <=0 mPriceListClg" + mPriceListClg);
							if (mPriceListClg != null && mPriceListClg.trim().length() > 0) {
								System.out.println("mRateClg <=0 mRateClg" + mRateClg);
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>")
								.append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
							} else {
								System.out.println("mRateClg <=0 ldRate" + ldRate);
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + ldRate + "]]>")
								.append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(ldRate)));
							}
						} else {
							System.out.println("mRateClg <=0 else mRateClg" + mRateClg);
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>")
							.append("</rate__clg>");
							setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
						}
						reStr = itemChangedDet(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						System.out.println("rate itemChanged Called" + valueXmlString.toString());
						lsSaleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));

						sql = "select  quot_no from sorder where sale_order =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSaleOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsQuotNo = rs.getString("quot_no");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsListType == null || lsListType.trim().length() == 0) {
							lsListType = "L";
						}
						double rate1 = Double.parseDouble(genericUtility.getColumnValue("rate", dom1) == null ? "0.00"
								: genericUtility.getColumnValue("rate", dom1));
						double mRateClg1 = Double
								.parseDouble(genericUtility.getColumnValue("rate__clg", dom1) == null ? "0.00"
										: genericUtility.getColumnValue("rate__clg", dom1));
						System.out.println("dom1 rate :" + rate1);
						System.out.println("dom1 mRateClg1 :" + mRateClg1);
						double rate = Double.parseDouble(genericUtility.getColumnValue("rate", dom) == null ? "0.00"
								: genericUtility.getColumnValue("rate", dom));
						mRateClg = Double.parseDouble(genericUtility.getColumnValue("rate__clg", dom) == null ? "0.00"
								: genericUtility.getColumnValue("rate__clg", dom));
						System.out.println("dom rate :" + rate);
						System.out.println("dom mRateClg1 :" + mRateClg);
						if ("B".equalsIgnoreCase(lsListType) || "I".equalsIgnoreCase(lsListType)
								|| "F".equalsIgnoreCase(lsListType)) {
							//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [START]
							/*valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rate + "]]>")
									.append("</rate>");*/
							valueXmlString.append("<rate__clg protect = \"1\">").append("<![CDATA[" + rate + "]]>")
							.append("</rate__clg>");
							//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [END]
							setNodeValue(dom, "rate", getAbsString(String.valueOf(rate)));
						} else if (lsQuotNo != null && lsQuotNo.trim().length() > 0) {
							//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [START]
							/*valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rate + "]]>")
									.append("</rate>");*/
							valueXmlString.append("<rate__clg protect = \"1\">").append("<![CDATA[" + rate + "]]>")
							.append("</rate__clg>");		
							//Commented and Added by sarita to set rate__clg instead rate as checking for price_list__clg on 09 APR 2019 [END]
							setNodeValue(dom, "rate", getAbsString(String.valueOf(rate)));
						} else {
							valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + rate + "]]>")
							.append("</rate>");
							setNodeValue(dom, "rate", getAbsString(String.valueOf(rate)));
						}
						if (mPriceListClg != null && mPriceListClg.trim().length() > 0) {
							if (lsListType.trim().length() == 0) {
								lsListType = "L";
							}
							if ("B".equalsIgnoreCase(lsListType)) {
								valueXmlString.append("<rate__clg protect = \"1\">")
								.append("<![CDATA[" + mRateClg + "]]>").append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
							} else {
								valueXmlString.append("<rate__clg protect = \"0\">")
								.append("<![CDATA[" + mRateClg + "]]>").append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
							}
						} else {
							valueXmlString.append("<rate__clg protect = \"0\">").append("<![CDATA[" + mRateClg + "]]>")
							.append("</rate__clg>");
							setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
						}

						lsPlistDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
						lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						lsItemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						if (checkNull(genericUtility.getColumnValue("pl_date", dom1)).trim().length() > 0) {
							Timestamp ldPlistDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
						}

					} else {
						// 03/AUG/16 check up to here
						System.out.println("ContractNo!=NUll");
						lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						sql = "select count(*) as cnt from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsItemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt > 0) {
							sql = "select descr from bom where bom_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mDescr1 = rs.getString("descr");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						// 08-sep-2020 manoharan contract is already there use the same
						//ContractNo = ""; 
						//lsContractNo = "";
						ContractNo = genericUtility.getColumnValue("contract_no", dom);
						lsContractNo = genericUtility.getColumnValue("contract_no", dom);
						lsLineNoContr = genericUtility.getColumnValue("line_no__contr", dom);
						if(ContractNo == null ||ContractNo.trim().length() == 0 || lsLineNoContr == null ||lsLineNoContr.trim().length() == 0 )	
						{
							//Modified by Yashwant S. on 19-10-2020[START][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
							lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
							lsLineNoContr = checkNull(genericUtility.getColumnValue("line_no__contr", dom));
							// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
							if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
							{
								lsContractNo = null;
							}
							if(lsLineNoContr != null && "null".equalsIgnoreCase(lsLineNoContr.trim()))
							{
								lsLineNoContr = null;
							}
							// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
							//Modified by Yashwant S. on 19-10-2020[END][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
							
							ContractNo = getContract(lsSiteCode, lsCustCode, orderDate, lsItemCode, lsContractNo,
							lsLineNoContr, conn);// Change by chandrashekar on 10-o8-2016
                            // 18-sep-2020 manoharan take care of null
							//if (ContractNo.trim().length() > 0) {
                            if (ContractNo != null && ContractNo.trim().length() > 0) {
								String[] arrStr = ContractNo.split("@");
								if (arrStr.length > 0) {
									lsContractNo = arrStr[0];
								}
								if (arrStr.length > 1) {
									lsLineNoContr = arrStr[1];
								}
							}
						}
						
						//added by  Pratiksha A on 09-03-21--start--
						System.out.println("line before=====["+ lsLineNoContr+"]");							
						if(lsLineNoContr != null && lsLineNoContr.trim().length() > 0)
						{
							lsLineNoContr = getLineNewNo(lsLineNoContr);
						}
						System.out.println("line after====["+ lsLineNoContr+"]");
						//added by  Pratiksha A on 09-03-21-- end--
						
						sql = "select site_code,item_code,item_flg,quantity,unit,dsp_date,"
								+ " rate,discount,tax_amt,tax_class,tax_chap,"
								+ "tax_env, net_amt,remarks,item_descr,unit__rate,"
								+ "pack_code, pack_instr,no_art,quantity__stduom,rate__stduom,"
								+ " unit__std, conv__qty_stduom, conv__rtuom_stduom"
								+ " from scontractdet where  contract_no= ? and line_no =  ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsContractNo);
						pstmt.setString(2, lsLineNoContr);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsSiteCode = checkNull(rs.getString("site_code"));
							lsItemCode = checkNull(rs.getString("item_code"));
							lsItemFlg = checkNull(rs.getString("item_flg"));
							lcQuantity = rs.getDouble("quantity");
							lsUnit = checkNull(rs.getString("unit"));
							ldDespDate = rs.getDate("dsp_date");
							lcRate = rs.getDouble("rate");
							lcDiscount = rs.getDouble("discount");
							lcTaxAmt = rs.getDouble("tax_amt");
							lsTaxClass = checkNull(rs.getString("tax_class"));
							lsTaxChap = checkNull(rs.getString("tax_chap"));
							lsTaxEnv = checkNull(rs.getString("tax_env"));
							lcNetAmt = rs.getDouble("net_amt");
							lsRemarks = checkNull(rs.getString("remarks"));
							lsItemDescr = checkNull(rs.getString("item_descr"));
							lsUnitRate = checkNull(rs.getString("unit__rate"));
							lsPackCode = checkNull(rs.getString("pack_code"));
							lsPackInstr = checkNull(rs.getString("pack_instr"));
							llNoArt = checkNull(rs.getString("no_art"));
							ldQtyStdum = rs.getDouble("quantity__stduom");
							lcRateStduom = rs.getDouble("rate__stduom");
							lsUnitStd = checkNull(rs.getString("unit__std"));
							ldConveQtyStduom = rs.getDouble("conv__qty_stduom");
							lcConvRtuomStduom = rs.getDouble("conv__rtuom_stduom");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_code >").append("<![CDATA[" + lsSiteCode + "]]>")
						.append("</site_code>");
						setNodeValue(dom, "site_code", getAbsString(lsSiteCode));

						valueXmlString.append("<item_code >").append("<![CDATA[" + lsItemCode + "]]>")
						.append("</item_code>");
						setNodeValue(dom, "item_code", getAbsString(lsItemCode));

						valueXmlString.append("<item_flg >").append("<![CDATA[" + lsItemFlg + "]]>")
						.append("</item_flg>");
						setNodeValue(dom, "item_flg", getAbsString(lsItemFlg));

						valueXmlString.append("<unit >").append("<![CDATA[" + lsUnit + "]]>").append("</unit>");
						setNodeValue(dom, "unit", getAbsString(lsUnit));

						valueXmlString.append("<rate >").append("<![CDATA[" + lcRate + "]]>").append("</rate>");
						setNodeValue(dom, "rate", getAbsString(String.valueOf(lcRate)));

						//////// Nandkumar Gadkari-----start
						/* if (lsContractNo != null && lsContractNo.trim().length() > 0) { */
						valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + lcDiscount + "]]>")
						.append("</discount>");//Changed by PavanR 04oct18[to protect discount if discount auto-populated]

						setNodeValue(dom, "discount", getAbsString(String.valueOf(lcDiscount)));
						/* } */
						//////// Nandkumar Gadkari-----end
						valueXmlString.append("<tax_class >").append("<![CDATA[" + lsTaxClass + "]]>")
						.append("</tax_class>");
						setNodeValue(dom, "tax_class", getAbsString(lsTaxClass));

						valueXmlString.append("<tax_chap >").append("<![CDATA[" + lsTaxChap + "]]>")
						.append("</tax_chap>");
						setNodeValue(dom, "tax_chap", getAbsString(lsTaxChap));

						valueXmlString.append("<tax_env >").append("<![CDATA[" + lsTaxEnv + "]]>").append("</tax_env>");
						setNodeValue(dom, "tax_env", getAbsString(lsTaxEnv));

						valueXmlString.append("<remarks >").append("<![CDATA[" + lsRemarks + "]]>")
						.append("</remarks>");
						setNodeValue(dom, "remarks", getAbsString(lsRemarks));

						if (lsItemFlg.equalsIgnoreCase("B")) {
							lsItemDesc = lsItemDescr + " " + mDescr1;
						} else {
							lsItemDesc = lsItemDescr;
						}

						valueXmlString.append("<item_descr >").append("<![CDATA[" + lsItemDescr + "]]>")
						.append("</item_descr>");
						setNodeValue(dom, "item_descr", getAbsString(lsItemDescr));

						valueXmlString.append("<unit__rate >").append("<![CDATA[" + lsUnitRate + "]]>")
						.append("</unit__rate>");
						setNodeValue(dom, "unit__rate", getAbsString(lsUnitRate));

						valueXmlString.append("<pack_code >").append("<![CDATA[" + lsPackCode + "]]>")
						.append("</pack_code>");
						setNodeValue(dom, "pack_code", getAbsString(lsPackCode));

						valueXmlString.append("<pack_instr >").append("<![CDATA[" + lsPackInstr + "]]>")
						.append("</pack_instr>");
						setNodeValue(dom, "pack_instr", getAbsString(lsPackInstr));

						valueXmlString.append("<no_art >").append("<![CDATA[" + llNoArt + "]]>").append("</no_art>");
						setNodeValue(dom, "no_art", getAbsString(llNoArt));

						valueXmlString.append("<rate__stduom >").append("<![CDATA[" + lcRateStduom + "]]>")
						.append("</rate__stduom>");
						setNodeValue(dom, "rate__stduom", getAbsString(String.valueOf(lcRateStduom)));

						valueXmlString.append("<unit__std >").append("<![CDATA[" + lsUnitStd + "]]>")
						.append("</unit__std>");
						setNodeValue(dom, "unit__std", getAbsString(lsUnitStd));
						String ldDespDateStr = "";
						if (ldDespDate != null) {
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
									genericUtility.getApplDateFormat());
							ldDespDateStr = simpleDateFormat.format(ldDespDate);
							System.out.println("=========ldDespDate[" + ldDespDateStr + "]");
						} else {
							ldDespDateStr = "";
						}

						valueXmlString.append("<dsp_date>").append("<![CDATA[" + ldDespDateStr + "]]>")
						.append("</dsp_date>");
						//setNodeValue(dom, "dsp_date", getAbsString(ldDespDate.toString())); COMMENTED AND ADDED BY NANDKUMAR GADKARI ON 17/09/19
						setNodeValue(dom, "dsp_date", getAbsString(ldDespDateStr));
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + ldConveQtyStduom + "]]>")
						.append("</conv__qty_stduom>");
						setNodeValue(dom, "conv__qty_stduom", getAbsString(String.valueOf(ldConveQtyStduom)));

						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lcConvRtuomStduom + "]]>")
						.append("</conv__rtuom_stduom>");
						setNodeValue(dom, "conv__rtuom_stduom", getAbsString(String.valueOf(lcConvRtuomStduom)));

						valueXmlString.append("<contract_no>").append("<![CDATA[" + lsContractNo + "]]>")
						.append("</contract_no>");
						setNodeValue(dom, "contract_no", getAbsString(lsContractNo));

                        //added by Sana S on 23-11-2020 [start]
                        if(lsLineNoContr == null || lsLineNoContr.trim().length() == 0 || "null".equalsIgnoreCase(lsLineNoContr.trim()))
                        {
                            lsLineNoContr="";
                        }
                        //added by Sana S on 23-11-2020 [end]

						valueXmlString.append("<line_no__contr>").append("<![CDATA[" + lsLineNoContr + "]]>")
						.append("</line_no__contr>");
						setNodeValue(dom, "line_no__contr", getAbsString(lsLineNoContr));

						mPriceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));

						if (lsItemCodeOrd != null && lsItemCodeOrd.trim().length() > 0) {
							mItemSer = distCommon.getItemSer(lsItemCode, lsSiteCode, orderDate, lsCustCode, "C", conn);
						} else {
							mItemSer = distCommon.getItemSer(lsItemCode, lsSiteCode, orderDate, lsCustCode, "C", conn);
						}
						valueXmlString.append("<item_ser>").append("<![CDATA[" + mItemSer + "]]>")
						.append("</item_ser>");
						setNodeValue(dom, "item_ser", getAbsString(mItemSer));

						Double quantity = Double
								.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
						System.out.print("Quantity2 mrateClg++++++++" + quantity);

						mRateClg = distCommon.pickRate(mPriceList, lrdateStr, lsItemCode, "", "L", quantity, conn);
						System.out.print("mRateClg++++++++" + mRateClg);

						lsListType = distCommon.getPriceListType(mPriceListClg, conn);

						if ("B".equalsIgnoreCase(lsListType) && mRateClg < 0) {
							mRateClg = 0.00;
						}

						if (mRateClg == 0) {
							if (mPriceListClg != null && mPriceListClg.trim().length() > 0) {
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>")
								.append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
							} else {
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + lcRateStduom + "]]>")
								.append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(lcRateStduom)));
							}
						} else {
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>")
							.append("</rate__clg>");
							setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
						}

						reStr = itemChangedDet(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);

						idRateWtDiscount = lcRate;
					}
					lsItemCode = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
					lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));
					mQty = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));

					lcPlistDisc = getDiscount(lsItemCode, lsUnit, mQty, dom, dom1, dom2, conn);
					System.out.println("lcPlistDisc in itemcode IC" + lcPlistDisc);
					// nandkumar Gadkari start---------
					lcDiscount = Double.parseDouble(checkDouble(genericUtility.getColumnValue("discount", dom)));/// chnages
					/// dom1
					/// to
					/// dom

					System.out.println("lcDiscount:::::::::" + lcDiscount);
					// nandkumar Gadkari end---------
					//Changed by PavanR 04oct18[to protect discount if discount auto-populated]
					lsDiscPriceList = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
					System.out.println("lsDiscPriceList" + lsDiscPriceList);
					if (lcDiscount == 0 && (lsContractNo == null || lsContractNo.trim().length() == 0)) {
						System.out.println("PavanRane :: 10295 [" +lcPlistDisc+"]");
						if(lsDiscPriceList != null && lsDiscPriceList.trim().length() > 0)
						{
							valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + lcPlistDisc + "]]>").append("</discount>");
						}else
						{
							valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + lcPlistDisc + "]]>").append("</discount>");
						}
						setNodeValue(dom, "discount", getAbsString(String.valueOf(lcPlistDisc)));
					}
					//lsDiscPriceList = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
					//System.out.println("lsDiscPriceList" + lsDiscPriceList);
					lcRate = Double.parseDouble(genericUtility.getColumnValue("rate", dom));
					System.out.println("rate in item code IC" + lcRate);
					if ("M".equalsIgnoreCase(distCommon.getPriceListType(lsDiscPriceList, conn))) {
						ldRate = lcRate;
						lsDiscFlag = true;
						ldRate = calRate(lcPlistDisc, ldRate);
						System.out.println("RATE calcrate" + ldRate);
						lsUnitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						lsUnitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));

						System.out.println("lsUnitRate [" + lsUnitRate + "]:::lcRateStduom[" + lcRateStduom + "]");

						lcConvRtuomStduom = Double
								.parseDouble(checkDouble(genericUtility.getColumnValue("conv__rtuom_stduom", dom)));
						lcRateStduom = distCommon.convQtyFactor(lsUnitStd, lsUnitRate, lsItemCode, ldRate, conn);
						System.out.println("Before lcConvRtuomStduom [" + lcConvRtuomStduom + "]:::lcRateStduom["
								+ lcRateStduom + "]");

						ArrayList ratestduomArr = null;
						ratestduomArr = distCommon.getConvQuantityFact(lsUnitStd, lsUnitRate, lsItemCode, ldRate,
								lcConvRtuomStduom, conn);
						// valueXmlString.append("<unit__rate>").append("<![CDATA[" + mVal +
						// "]]>").append("</unit__rate>");

						lcConvRtuomStduom = Double.parseDouble(ratestduomArr.get(0).toString());
						lcRateStduom = Double.parseDouble(ratestduomArr.get(1).toString());

						System.out.println("After lcConvRtuomStduom [" + lcConvRtuomStduom + "]:::lcRateStduom["
								+ lcRateStduom + "]");

						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lcConvRtuomStduom + "]]>")
						.append("</conv__rtuom_stduom>");
						setNodeValue(dom, "conv__rtuom_stduom", getAbsString(String.valueOf(lcConvRtuomStduom)));
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lcRateStduom + "]]>")
						.append("</rate__stduom>");
						setNodeValue(dom, "rate__stduom", getAbsString(String.valueOf(lcRateStduom)));
						valueXmlString.append("<rate>").append("<![CDATA[" + ldRate + "]]>").append("</rate>");
						setNodeValue(dom, "rate", getAbsString(String.valueOf(ldRate)));
						//////// Nandkumar Gadkari-----start
						// setNodeValue(dom, "discount", getAbsString(String.valueOf("0")));
						//////// Nandkumar Gadkari-----end
						reStr = itemChangedDet(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						System.out.println("rate itemChanged Called IC" + valueXmlString.toString());

						//////// Nandkumar Gadkari-----start COMMENTED
						//Changed by PavanR 04oct18[to protect discount if discount auto-populated]
						if(lsDiscPriceList != null && lsDiscPriceList.trim().length() > 0)
						{
							valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + 0 + "]]>").append("</discount>");
							//////// Nandkumar Gadkari-----end
						}else{
							valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + 0 + "]]>").append("</discount>");
						}
					}

					lcQtyStd = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity__stduom", dom1)));
					lsPackCode = genericUtility.getColumnValue("pack_code", dom1);
					llNoArt = String.valueOf(distCommon.getNoArt(lsSiteCode, lsCustCode, lsItemCode, lsPackCode,
							lcQtyStd, 'I', acShipperQty, acIntQty, conn));

					valueXmlString.append("<no_art>").append("<![CDATA[" + llNoArt + "]]>").append("</no_art>");
					setNodeValue(dom, "no_art", getAbsString(llNoArt));

					sql = "select spec_id from siteitem where site_code = ? and item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCode);
					pstmt.setString(2, lsItemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsSpecId = rs.getString("spec_id");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (lsSpecId == null || lsSpecId.trim().length() == 0) {
						sql = "select spec_id from customeritem where cust_code = ? and item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCustCode);
						pstmt.setString(2, lsItemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsSpecId = rs.getString("spec_id");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					valueXmlString.append("<spec_id>").append("<![CDATA[" + lsSpecId + "]]>").append("</spec_id>");
					setNodeValue(dom, "spec_id", getAbsString(lsSpecId));
					// valueXmlString.append("<quantity>").append("<![CDATA[" + mQty +
					// "]]>").append("</quantity>");
					// setNodeValue( dom, "quantity", getAbsString( String.valueOf(mQty)));

					reStr = itemChangedDet(dom, dom1, dom2, objContext, "unit", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}

				if (currentColumn.trim().equalsIgnoreCase("line_no__contr")) {
					String lsLineNoContr = "", mDescr1 = "", lsItemFlg = "", lsUnit = "", lsItemDescr = "",
							lsUnitRate = "", lsPackCode = "", lsPackInstr = "", llNoArt = "";
					String lsUnitStd = "", lsItemDesc = "";
					double lcQuantity = 0.00, lcRate = 0.00, lcDiscount = 0.00, lcTaxAmt = 0.00, lcNetAmt = 0.00,
							ldQtyStduom = 0.00, lcRateStduom = 0.00, ldConvQtyStduom = 0.00, lcConvRtuomStduom = 0.00;
					double ldRate = 0.00, idRateWtDiscount = 0.00;
					Timestamp ldDspDate = null;
					lsLineNoContr = checkNull(genericUtility.getColumnValue("line_no__contr", dom1));
					lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
					if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
					{
						lsContractNo = null;
					}
					if(lsLineNoContr != null && "null".equalsIgnoreCase(lsLineNoContr.trim()))
					{
						lsLineNoContr = null;
					}
					// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
					
					if (lsContractNo != null && lsContractNo.trim().length() > 0) {
						lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom1));

						sql = "select count(*) as cnt from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsItemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt > 0) {
							sql = "select descr from bom where bom_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								mDescr1 = rs.getString("descr");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						sql = "select site_code,item_code,item_flg,quantity,unit,dsp_date,"
								+ " rate,discount,tax_amt,tax_class,tax_chap,tax_env,"
								+ " net_amt,remarks,item_descr,unit__rate,pack_code,"
								+ " pack_instr,no_art,quantity__stduom,rate__stduom,unit__std,"
								+ " conv__qty_stduom,conv__rtuom_stduom"
								+ " from scontractdet where contract_no= ? and line_no =  ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsContractNo);
						pstmt.setString(2, lsLineNoContr);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsSiteCode = rs.getString("site_code");
							lsItemCode = rs.getString("item_code");
							lsItemFlg = rs.getString("item_flg");
							lcQuantity = rs.getDouble("quantity");
							lsUnit = rs.getString("unit");
							ldDspDate = rs.getTimestamp("dsp_date");
							lcRate = rs.getDouble("rate");
							lcDiscount = rs.getDouble("discount");
							lcTaxAmt = rs.getDouble("tax_amt");
							lsTaxClass = rs.getString("tax_class");
							lsTaxChap = rs.getString("tax_chap");
							lsTaxEnv = rs.getString("tax_env");
							lcNetAmt = rs.getDouble("net_amt");
							lsRemarks = rs.getString("remarks");
							lsItemDescr = rs.getString("item_descr");
							lsUnitRate = rs.getString("unit__rate");
							lsPackCode = rs.getString("pack_code");
							lsPackInstr = rs.getString("pack_instr");
							llNoArt = rs.getString("no_art");
							ldQtyStduom = rs.getDouble("quantity__stduom");
							lcRateStduom = rs.getDouble("rate__stduom");
							lsUnitStd = rs.getString("unit__std");
							ldConvQtyStduom = rs.getDouble("conv__qty_stduom");
							lcConvRtuomStduom = rs.getDouble("conv__rtuom_stduom");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<site_code>").append("<![CDATA[" + lsSiteCode + "]]>")
						.append("</site_code>");
						valueXmlString.append("<item_code>").append("<![CDATA[" + lsItemCode + "]]>")
						.append("</item_code>");
						valueXmlString.append("<item_flg>").append("<![CDATA[" + lsItemFlg + "]]>")
						.append("</item_flg>");
						valueXmlString.append("<quantity>").append("<![CDATA[" + lcQuantity + "]]>")
						.append("</quantity>");
						valueXmlString.append("<unit>").append("<![CDATA[" + lsUnit + "]]>").append("</unit>");
						valueXmlString.append("<rate>").append("<![CDATA[" + lcRate + "]]>").append("</rate>");

						System.out.println("lsContractNo::::::::::" + lsContractNo);
						//Changed by PavanR 04oct18[to protect discount if discount auto-populated]
						valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + lcDiscount + "]]>")
						.append("</discount>");

						valueXmlString.append("<tax_class>").append("<![CDATA[" + lsTaxClass + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + lsTaxChap + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + lsTaxEnv + "]]>").append("</tax_env>");
						valueXmlString.append("<remarks>").append("<![CDATA[" + lsRemarks + "]]>").append("</remarks>");
						if ("B".equalsIgnoreCase(lsItemFlg)) {
							lsItemDesc = lsItemDescr + " " + mDescr1;
						} else {
							lsItemDesc = lsItemDescr;
						}
						valueXmlString.append("<item_descr>").append("<![CDATA[" + lsItemDesc + "]]>")
						.append("</item_descr>");
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + lsUnitRate + "]]>")
						.append("</unit__rate>");
						valueXmlString.append("<pack_code>").append("<![CDATA[" + lsPackCode + "]]>")
						.append("</pack_code>");
						valueXmlString.append("<pack_instr>").append("<![CDATA[" + lsPackInstr + "]]>")
						.append("</pack_instr>");
						valueXmlString.append("<no_art>").append("<![CDATA[" + llNoArt + "]]>").append("</no_art>");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + ldQtyStduom + "]]>")
						.append("</quantity__stduom>");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lcRateStduom + "]]>")
						.append("</rate__stduom>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + lsUnitStd + "]]>")
						.append("</unit__std>");
						valueXmlString.append("<dsp_date>").append("<![CDATA[" + ldDspDate + "]]>")
						.append("</dsp_date>");
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + ldConvQtyStduom + "]]>")
						.append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lcConvRtuomStduom + "]]>")
						.append("</conv__rtuom_stduom>");
					}
				}
				if (currentColumn.trim().equalsIgnoreCase("quantity")) {
					valueXmlString = (gbfIcQuantity(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
					//added by  Nandkumar Gadkari on 23/10/18---------------- start------------------
					reStr = itemChangedDet(dom, dom1, dom2, objContext, "nature", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					//added by  Nandkumar Gadkari on 23/10/18---------------- end ------------------
					
					//added by  Pratiksha A on 09-03-21---------------- start------------------
					System.out.println("12917 Rate itemchanged called=====");
					reStr = itemChangedDet(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					//added by  Pratiksha A on 09-03-21---------------- end------------------
				}
				if (currentColumn.trim().equalsIgnoreCase("unit")) {
					// gbfIcUnit(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
					// conn);
					valueXmlString = (gbfIcUnit(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("conv__qty_stduom")) {
					valueXmlString = (gbfIcconvQtyStduom(valueXmlString, dom, dom1, dom2, editFlag, xtraParams,
							objContext, conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("rate")) {
					valueXmlString = (gbfIcRate(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
					valueXmlString = (gbfIcConvRtuomStduom(valueXmlString, dom, dom1, dom2, editFlag, xtraParams,
							objContext, conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("conv__rtuom_stduom")) {
					valueXmlString = (gbfIcConvRtuomStduom(valueXmlString, dom, dom1, dom2, editFlag, xtraParams,
							objContext, conn));//removed .append by nandkumar gadkari on 18/04/19
				}
				if (currentColumn.trim().equalsIgnoreCase("unit__rate")) {
					valueXmlString = (gbfIcUnitRate(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("pack_code")) {
					valueXmlString = (gbfIcPackCode(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("site_code")) {
					valueXmlString = (gbfIcSiteCode(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("cust_item__ref")) {
					valueXmlString = (gbfIcCustItemRef(valueXmlString, dom, dom1, dom2, editFlag, xtraParams,
							objContext, conn));
				}
				if (currentColumn.trim().equalsIgnoreCase("nature")) {
					valueXmlString = (gbfIcNature(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));

					//Nandkumar Gadkari on 04/10/18----------------------
					double mVal= 0.0;
					String nature="";
					nature = checkNull(genericUtility.getColumnValue("nature", dom));
					if ("V".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature))// type p added by nandkumar gadkari on 28/05/19
					{


						valueXmlString.append("<rate>").append("<![CDATA[" + mVal + "]]>").append("</rate>");
						setNodeValue(dom, "rate", getAbsString(String.valueOf(mVal)));
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + mVal + "]]>")
						.append("</rate__stduom>");
						valueXmlString.append("<amount>").append("<![CDATA[" + mVal + "]]>")
						.append("</amount>");
						/*valueXmlString.append("<ord_value>").append("<![CDATA[" + mVal + "]]>")
						.append("</ord_value>");*/
					}
					else 
					{
						valueXmlString = (gbfIcQuantity(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
								conn));
					}


					//Nandkumar Gadkari on 04/10/18----------------------
				}
				if (currentColumn.trim().equalsIgnoreCase("price_list__disc")) {
					// valueXmlString.append(gbfIcSiteCode(valueXmlString,dom,dom1,dom2,editFlag,
					// xtraParams,objContext,conn));
					reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}
				//added by manish mhatre on 31-oct-2019[For set the cust_name_end]
				//start manish
				else if (currentColumn.trim().equalsIgnoreCase("cust_code__end")){
					String custName="",custCode="";

					custCode = checkNull(genericUtility.getColumnValue("cust_code__end",dom));			
					sql="select cust_name from customer where cust_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						custName =rs.getString("cust_name");
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					valueXmlString.append("<cust_name__end>").append("<![CDATA[" + custName + "]]>").append("</cust_name__end>");
				}
				//end manish
				valueXmlString.append("</Detail2>");
				break;
			case 3:
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail3>");
				int childListLength3 = childNodeList.getLength();
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					String termId = "", userId = "", lsSalesOrd = "";

					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
					String sysDate = sdf.format(date);
					termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
					userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

					valueXmlString.append("<chg_user>").append("<![CDATA[" + userId + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>");
					valueXmlString.append("<chg_date>").append("<![CDATA[" + sysDate + "]]>").append("</chg_date>");

					lsSaleOrd = checkNull(genericUtility.getColumnValue("sale_order", dom1));
					valueXmlString.append("<sale_order>").append("<![CDATA[" + lsSaleOrd + "]]>")
					.append("</sale_order>");
				} else if (currentColumn.trim().equalsIgnoreCase("cr_type")) {
					valueXmlString.append(
							gbfIcCrType(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext, conn));
				}
				valueXmlString.append("</Detail3>");
				break;
			case 4:
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail4>");
				int childListLength4 = childNodeList.getLength();
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					String lsSalesOrd = "";

					lsSaleOrd = checkNull(genericUtility.getColumnValue("sale_order", dom1));
					valueXmlString.append("<sale_order>").append("<![CDATA[" + lsSaleOrd + "]]>")
					.append("</sale_order>");
				} else if (currentColumn.trim().equalsIgnoreCase("term_code")) {
					valueXmlString = (gbfIcTermCode(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
				}
				valueXmlString.append("</Detail4>");
				break;
			}
			valueXmlString.append("</Root>");
			System.out.println("valueXmlString[" + valueXmlString.toString() + "]");

			return valueXmlString.toString();

		} catch (Exception e) {
			System.out.println("Exception : [itemChangedDet] :==>\n" + e.getMessage());
			throw new ITMException(e);
		} finally 
		{
			if (conn != null) 
			{
				conn.close();
				conn = null;
			}
		}
	}

	private StringBuffer itmContractNo(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsOrderType = "", lsContractNo = "", lsOrdtypeSample = "", lsCustCode = "", lsCustCodeDlv = "",
				lsCustCodeBil = "", mtaxopt = "", mItemSer = "", lsCustPord = "";
		String lsSalesPers = "", lcCommPerc = "", lsTaxClass = "", lsTaxChap = "", lsTaxEnv = "", ldTaxDate = "",
				lsPriceList = "", lsCrTerm = "", lsSiteCode = "", lsQuotNo = "";
		String lsCurrCode = "", lcExchRate = "", lsRemarks = "", lsDlvAdd1 = "", lsDlvAdd2 = "", lsDlvCity = "",
				lsCountCodeDlv = "", lsDlvPin = "", lsStanCode = "", lsPartQty = "";
		String lsStatus = "", ldStatusDate = "", lsTranCode = "", lsUdfStr1 = "", lsUdfStr2 = "", lcUdfNum1 = "",
				lcUdfNum2 = "", lcCommAmt = "", lsStatusRemarks = "", lsDlvTerm = "";
		String lcFrtAmt = "", lsCurrCodeFrt = "", lcExchRateFrt = "", lsFrtTerm = "", lcAdvPerc = "", lsDistRoute = "",
				lsCurrCodeComm = "", lsSalesPers1 = "", lcCommPerc1 = "", lsCommPercOn1 = "";
		String lsCurrCodeComm1 = "", lsSalesPers2 = "", lcCommPerc2 = "", lsCommPercOn2 = "", lsCurrCodeComm2 = "",
				lsEmpCodeCon = "", lsDlvAdd3 = "", lsStateCodeDlv = "";
		String lsTransMode = "", lsSpecReason = "", lsOffshoreInvoice = "", lsLabelType = "", lsOutsideInspection = "",
				lsRemarks2 = "", lsRemarks3 = "", lsStanCodeInit = "";
		String lsCurrCodeIns = "", lcExchRateIns = "", lcInsAmt = "", lsShipStatus = "", lsDlvTo = "",
				lsAcctCodeSal = "", lsCctrCodeSal = "", tel1 = "", tel2 = "", tel3 = "", fax = "";
		String lcExchRateComm = "", lcExchRateComm1 = "", lcExchRateComm2 = "", lsPriceListDisc = "", lsMarketReg = "",
				lsProjCode = "", lsContractType = "", lsCustnameBil = "";
		String lsDisIndOrdtypeList = "", lsPlistClg = "", lsOtypeDescr = "", lsLocGroup = "", lsTermTable = "";
		String reStr = "";
		int pos = 0;
		//double lsCommPercOn = 0.00;
		String lsCommPercOn = "";
		Timestamp ldDueDate = null, ldPromDate = null, ldPordDate = null, ldUdfDate1 = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
			lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));//Modified by Pratiksha on 01-03-21
			mItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));//Modified by Pratiksha on 01-03-21
			
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
			if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
			{
				lsContractNo = null;
			}
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
            SimpleDateFormat sdfnew  = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdfOutputnew = new SimpleDateFormat(genericUtility.getApplDateFormat());
			lsOrdtypeSample = distCommon.getDisparams("999999", "SAMPLE_ORDER_TYPE", conn);

			sql = "select  cust_code,cust_code__dlv,cust_code__bil,tax_opt,item_ser,"
					+ "cust_pord,sales_pers,comm_perc,tax_class,tax_chap,"
					+ "tax_env,tax_date,price_list,cr_term,site_code,"
					+ "quot_no,curr_code,exch_rate,due_date,prom_date,"
					+ "remarks,dlv_add1,dlv_add2,dlv_city,count_code__dlv,"
					+ "dlv_pin,stan_code,part_qty,status,status_date,"
					+ "tran_code,udf__str1,udf__str2,udf__num1,udf__num2,"
					+ "comm_perc__on,comm_amt,status_remarks,dlv_term,frt_amt,"
					+ "curr_code__frt,exch_rate__frt,frt_term,pord_date,adv_perc,"
					+ "dist_route,curr_code__comm,sales_pers__1,comm_perc_1,comm_perc__on_1,"
					+ "curr_code__comm_1,sales_pers__2,comm_perc_2,comm_perc_on_2,curr_code__comm_2,"
					+ "emp_code__con,udf__date1,dlv_add3,state_code__dlv,trans_mode,"
					+ "spec_reason,offshore_invoice,label_type,outside_inspection,remarks2,"
					+ "remarks3,stan_code__init,curr_code__ins,exch_rate__ins,ins_amt,"
					+ "ship_status,dlv_to,acct_code__sal,cctr_code__sal,tel1__dlv,tel2__dlv,"
					+ "tel3__dlv,fax__dlv,exch_rate__comm,exch_rate__comm_1,exch_rate__comm_2,"
					+ "price_list__disc,market_reg,proj_code,tax_class, contract_type" + " from  scontract"
					+ " where contract_no =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsContractNo);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsCustCode = checkNull(rs.getString("cust_code"));
				lsCustCodeDlv = checkNull(rs.getString("cust_code__dlv"));
				lsCustCodeBil = checkNull(rs.getString("cust_code__bil"));
				mtaxopt = checkNull(rs.getString("tax_opt"));
				mItemSer = checkNull(rs.getString("item_ser"));
				lsCustPord = checkNull(rs.getString("cust_pord"));
				lsSalesPers = checkNull(rs.getString("sales_pers"));
				lcCommPerc = checkNull(rs.getString("comm_perc"));
				lsTaxClass = checkNull(rs.getString("tax_class"));
				lsTaxChap = checkNull(rs.getString("tax_chap"));
				lsTaxEnv = checkNull(rs.getString("tax_env"));
				ldTaxDate = checkNull(rs.getString("tax_date"));
				lsPriceList = checkNull(rs.getString("price_list"));
				lsCrTerm = checkNull(rs.getString("cr_term"));
				lsSiteCode = checkNull(rs.getString("site_code"));
				lsQuotNo = checkNull(rs.getString("quot_no"));
				lsCurrCode = checkNull(rs.getString("curr_code"));
				lcExchRate = checkNull(rs.getString("exch_rate"));
				ldDueDate = rs.getTimestamp("due_date");
				ldPromDate = rs.getTimestamp("prom_date");
				lsRemarks = checkNull(rs.getString("remarks"));
				lsDlvAdd1 = checkNull(rs.getString("dlv_add1"));
				lsDlvAdd2 = checkNull(rs.getString("dlv_add2"));
				lsDlvCity = checkNull(rs.getString("dlv_city"));
				lsCountCodeDlv = checkNull(rs.getString("count_code__dlv"));
				lsDlvPin = checkNull(rs.getString("dlv_pin"));
				lsStanCode = checkNull(rs.getString("stan_code"));
				lsPartQty = checkNull(rs.getString("part_qty"));
				lsStatus = checkNull(rs.getString("status"));
				ldStatusDate = rs.getString("status_date");
				lsTranCode = checkNull(rs.getString("tran_code"));
				lsUdfStr1 = checkNull(rs.getString("udf__str1"));
				lsUdfStr2 = checkNull(rs.getString("udf__str2"));
				lcUdfNum1 = checkNull(rs.getString("udf__num1"));
				lcUdfNum2 = checkNull(rs.getString("udf__num2"));
				lsCommPercOn = checkNull(rs.getString("comm_perc__on"));
				lcCommAmt = checkNull(rs.getString("comm_amt"));
				lsStatusRemarks = checkNull(rs.getString("status_remarks"));
				lsDlvTerm = checkNull(rs.getString("dlv_term"));
				lcFrtAmt = checkNull(rs.getString("frt_amt"));
				lsCurrCodeFrt = checkNull(rs.getString("curr_code__frt"));
				lcExchRateFrt = checkNull(rs.getString("exch_rate__frt"));
				lsFrtTerm = checkNull(rs.getString("frt_term"));
				ldPordDate = rs.getTimestamp("pord_date");
				lcAdvPerc = checkNull(rs.getString("adv_perc"));
				lsDistRoute = checkNull(rs.getString("dist_route"));
				lsCurrCodeComm = checkNull(rs.getString("curr_code__comm"));
				lsSalesPers1 = checkNull(rs.getString("sales_pers__1"));
				lcCommPerc1 = checkNull(rs.getString("comm_perc_1"));
				lsCommPercOn1 = checkNull(rs.getString("comm_perc__on_1"));
				lsCurrCodeComm1 = checkNull(rs.getString("curr_code__comm_1"));
				lsSalesPers2 = checkNull(rs.getString("sales_pers__2"));
				lcCommPerc2 = checkNull(rs.getString("comm_perc_2"));
				lsCommPercOn2 = checkNull(rs.getString("comm_perc_on_2"));
				lsCurrCodeComm2 = checkNull(rs.getString("curr_code__comm_2"));
				lsEmpCodeCon = checkNull(rs.getString("emp_code__con"));
				ldUdfDate1 = rs.getTimestamp("udf__date1");
				lsDlvAdd3 = checkNull(rs.getString("dlv_add3"));
				lsStateCodeDlv = checkNull(rs.getString("state_code__dlv"));
				lsTransMode = checkNull(rs.getString("trans_mode"));
				lsSpecReason = checkNull(rs.getString("spec_reason"));
				lsOffshoreInvoice = checkNull(rs.getString("offshore_invoice"));
				lsLabelType = checkNull(rs.getString("label_type"));
				lsOutsideInspection = checkNull(rs.getString("outside_inspection"));
				lsRemarks2 = checkNull(rs.getString("remarks2"));
				lsRemarks3 = checkNull(rs.getString("remarks3"));
				lsStanCodeInit = checkNull(rs.getString("stan_code__init"));
				lsCurrCodeIns = checkNull(rs.getString("curr_code__ins"));
				lcExchRateIns = checkNull(rs.getString("exch_rate__ins"));
				lcInsAmt = checkNull(rs.getString("ins_amt"));
				lsShipStatus = checkNull(rs.getString("ship_status"));
				lsDlvTo = checkNull(rs.getString("dlv_to"));
				lsAcctCodeSal = checkNull(rs.getString("acct_code__sal"));
				lsCctrCodeSal = checkNull(rs.getString("cctr_code__sal"));
				tel1 = checkNull(rs.getString("tel1__dlv"));
				tel2 = checkNull(rs.getString("tel2__dlv"));
				tel3 = checkNull(rs.getString("tel3__dlv"));
				fax = checkNull(rs.getString("fax__dlv"));
				lcExchRateComm = checkNull(rs.getString("exch_rate__comm"));
				lcExchRateComm1 = checkNull(rs.getString("exch_rate__comm_1"));
				lcExchRateComm2 = checkNull(rs.getString("exch_rate__comm_2"));
				lsPriceListDisc = checkNull(rs.getString("price_list__disc"));
				lsMarketReg = checkNull(rs.getString("market_reg"));
				lsProjCode = checkNull(rs.getString("proj_code"));
				lsTaxClass = checkNull(rs.getString("tax_class"));
				lsContractType = checkNull(rs.getString("contract_type"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "select curr_code__frt, curr_code__ins from site_customer where site_code = ? and cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSiteCode);
			pstmt.setString(2, lsCustCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsCurrCodeFrt = rs.getString("curr_code__frt");
				lsCurrCodeIns = rs.getString("curr_code__ins");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			if ((lsCurrCodeFrt == null || lsCurrCodeFrt.trim().length() == 0)
					&& (lsCurrCodeIns == null || lsCurrCodeIns.trim().length() == 0)) {
				sql = "select curr_code__frt, curr_code__ins from customer where cust_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsCurrCodeFrt = rs.getString("curr_code__frt");
					lsCurrCodeIns = rs.getString("curr_code__ins");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				if (lsCurrCodeFrt == null || lsCurrCodeFrt.trim().length() == 0) {
					lsCurrCodeFrt = lsCurrCode;
				}
				if (lsCurrCodeIns == null || lsCurrCodeIns.trim().length() == 0) {
					lsCurrCodeIns = lsCurrCode;
				}
			}

            valueXmlString.append("<cust_code>").append("<![CDATA[" + lsCustCode + "]]>").append("</cust_code>");//cust_code
			setNodeValue(dom, "cust_code", getAbsString(lsCustCode));
            reStr = itemChangedHdr(dom, dom1, dom2, objContext, "cust_code", editFlag, xtraParams);//Modified by Pratiksha on 01-03-21
			pos = reStr.indexOf("<Detail1>");
			reStr = reStr.substring(pos + 9);
			pos = reStr.indexOf("</Detail1>");
			reStr = reStr.substring(0, pos);
			valueXmlString.append(reStr);

			valueXmlString.append("<item_ser>").append("<![CDATA[" + mItemSer + "]]>").append("</item_ser>");
			setNodeValue(dom, "item_ser", getAbsString(mItemSer));
            reStr = itemChangedHdr(dom, dom1, dom2, objContext, "item_ser", editFlag, xtraParams);//Modified by Pratiksha on 01-03-21
			pos = reStr.indexOf("<Detail1>");
			reStr = reStr.substring(pos + 9);
			pos = reStr.indexOf("</Detail1>");
			reStr = reStr.substring(0, pos);
			valueXmlString.append(reStr);

			valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + lsCustCodeDlv + "]]>")
			.append("</cust_code__dlv>");
			setNodeValue(dom, "cust_code__dlv", getAbsString(lsCustCodeDlv));

			valueXmlString.append("<cust_code__bil>").append("<![CDATA[" + lsCustCodeBil + "]]>")
			.append("</cust_code__bil>");
			setNodeValue(dom, "cust_code__bil", getAbsString(lsCustCodeBil));

			sql = "select cust_name from customer where cust_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsCustCodeBil);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsCustnameBil = rs.getString("cust_name");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			valueXmlString.append("<cust_name__bil>").append("<![CDATA[" + lsCustnameBil + "]]>")
			.append("</cust_name__bil>");
			setNodeValue(dom, "cust_name__bil", getAbsString(lsCustnameBil));

			valueXmlString.append("<tax_opt>").append("<![CDATA[" + mtaxopt + "]]>").append("</tax_opt>");
			setNodeValue(dom, "tax_opt", getAbsString(mtaxopt));

			valueXmlString.append("<cust_pord>").append("<![CDATA[" + lsCustPord + "]]>").append("</cust_pord>");
			setNodeValue(dom, "cust_pord", getAbsString(lsCustPord));

			valueXmlString.append("<sales_pers>").append("<![CDATA[" + lsSalesPers + "]]>").append("</sales_pers>");
			setNodeValue(dom, "sales_pers", getAbsString(lsSalesPers));

			valueXmlString.append("<comm_perc>").append("<![CDATA[" + lcCommPerc + "]]>").append("</comm_perc>");
			setNodeValue(dom, "comm_perc", getAbsString(lcCommPerc));

			valueXmlString.append("<tax_class>").append("<![CDATA[" + lsTaxClass + "]]>").append("</tax_class>");
			setNodeValue(dom, "tax_class", getAbsString(lsTaxClass));

			valueXmlString.append("<tax_chap>").append("<![CDATA[" + lsTaxChap + "]]>").append("</tax_chap>");
			setNodeValue(dom, "tax_chap", getAbsString(lsTaxChap));

			valueXmlString.append("<tax_env>").append("<![CDATA[" + lsTaxEnv + "]]>").append("</tax_env>");
			setNodeValue(dom, "tax_env", getAbsString(lsTaxEnv));

			//valueXmlString.append("<tax_date>").append("<![CDATA[" + ldTaxDate + "]]>").append("</tax_date>");
			//setNodeValue(dom, "tax_date", getAbsString(ldTaxDate));

			lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
			boolean lbOrdFlag = false;
			String lsDisIndOrdtypeListArr[] = lsDisIndOrdtypeList.split(",");
			if (lsDisIndOrdtypeListArr.length > 0) {
				for (int i = 0; i < lsDisIndOrdtypeListArr.length; i++) {
					if (lsOrderType.equalsIgnoreCase(lsDisIndOrdtypeListArr[i])) {
						lbOrdFlag = true;
					}
				}
			}

			if (lbOrdFlag) {
				sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code =? AND order_type =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				pstmt.setString(2, lsOrderType);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsPriceList = checkNull(rs.getString("price_list"));
					lsPlistClg = checkNull(rs.getString("price_list__clg"));
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				if (lsPriceList == null || lsPriceList.trim().length() == 0) {
					Timestamp orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date", dom1));
					lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
				}
				if (lsPriceList == null || lsPriceList.trim().length() == 0) {
					lsPriceList = priceListSite(lsSiteCode, lsCustCode, conn);
				}
			} else {
				sql = "SELECT  price_list,price_list__clg FROM cust_plist WHERE cust_code = ?  AND order_type =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCodeDlv);
				pstmt.setString(2, lsOrderType);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsPriceList = checkNull(rs.getString("price_list"));
					lsPlistClg = checkNull(rs.getString("price_list__clg"));
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				if (lsPriceList == null || lsPriceList.trim().length() == 0) {
                   // Timestamp orderDate = Timestamp.valueOf(genericUtility.getColumnValue("order_date", dom1));//commented by manish mhatre on 21-oct-20
                     
                    Timestamp orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
                            genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");  //added by manish mhatre on 21-oct-20
                            System.out.println("order date in manish:"+orderDate);
			//orderDate = sdf.parse(ordDate);
					lsPriceList = getPriceListOrderType(orderDate, dom, dom1, dom2, conn);
				}
				if (lsPriceList == null || lsPriceList.trim().length() == 0) {
					lsPriceList = priceListSite(lsSiteCode, lsCustCodeDlv, conn);
				}
			}
			if (lsOrderType.equalsIgnoreCase(lsOrdtypeSample)) {
				valueXmlString.append("<price_list>").append("<![CDATA[" + lsPriceListDisc + "]]>")
				.append("</price_list>");
				setNodeValue(dom, "price_list", getAbsString(lsPriceListDisc));
			} else {
				valueXmlString.append("<price_list>").append("<![CDATA[" + lsPriceListDisc + "]]>")
				.append("</price_list>");
				setNodeValue(dom, "price_list", getAbsString(lsPriceListDisc));

				valueXmlString.append("<price_list__clg>").append("<![CDATA[" + lsPlistClg + "]]>")
				.append("</price_list__clg>");
				setNodeValue(dom, "price_list__clg", getAbsString(lsPlistClg));
			}

			valueXmlString.append("<cr_term>").append("<![CDATA[" + lsCrTerm + "]]>").append("</cr_term>");
			setNodeValue(dom, "cr_term", getAbsString(lsCrTerm));

			valueXmlString.append("<site_code>").append("<![CDATA[" + lsSiteCode + "]]>").append("</site_code>");
			setNodeValue(dom, "site_code", getAbsString(lsSiteCode));

			valueXmlString.append("<quot_no>").append("<![CDATA[" + lsQuotNo + "]]>").append("</quot_no>");
			setNodeValue(dom, "quot_no", getAbsString(lsQuotNo));

			valueXmlString.append("<curr_code>").append("<![CDATA[" + lsCurrCode + "]]>").append("</curr_code>");
			setNodeValue(dom, "curr_code", getAbsString(lsCurrCode));

			valueXmlString.append("<exch_rate>").append("<![CDATA[" + lcExchRate + "]]>").append("</exch_rate>");
			setNodeValue(dom, "exch_rate", getAbsString(lcExchRate));

             //addded by manish mhatre on 21-oct-20
             //start manish
			
			
			if (ldDueDate != null) {
				System.out.println("manish ldDueDate before[" + ldDueDate + "]");
				String dueDatestr = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(ldDueDate);
				/*
				 * String dueDatestr= genericUtility.getValidDateString(ldDueDate.toString(),
				 * genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
				 * //addded by manish mhatre on 21-oct-20
				 */
				System.out.println("manish dueDatestr after [" + dueDatestr + "]");
				/*
				 * valueXmlString.append("<due_date>").append("<![CDATA[" + ldDueDate +
				 * "]]>").append("</due_date>"); setNodeValue(dom, "due_date",
				 * getAbsString(ldDueDate.toString()));
				 */ // commented by manish mhatre on 21-oct-20

				if (dueDatestr != null && dueDatestr.trim().length() > 0) {
					valueXmlString.append("<due_date>").append("<![CDATA[" + dueDatestr + "]]>")
							.append("</due_date>");
					setNodeValue(dom, "due_date", getAbsString(dueDatestr));
				}
			}
			// end manish

			if (ldPromDate != null) {

				System.out.println("manish ldPromDate before[" + ldPromDate + "]");
				String promDatestr = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(ldPromDate);

				/*
				 * String promDatestr= genericUtility.getValidDateString(ldPromDate.toString(),
				 * genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
				 * //addded by manish mhatre on 21-oct-20
				 */
				System.out.println("manish promDatestr after [" + promDatestr + "]");
				/*
				 * valueXmlString.append("<prom_date>").append("<![CDATA[" + ldPromDate +
				 * "]]>").append("</prom_date>"); setNodeValue(dom, "prom_date",
				 * getAbsString(ldPromDate.toString()));
				 */ // commented by manish mhatre on 21-oct-20
				// addded by manish mhatre on 21-oct-20
				// start manish
				if (promDatestr != null && promDatestr.trim().length() > 0) {
					valueXmlString.append("<prom_date>").append("<![CDATA[" + promDatestr + "]]>")
							.append("</prom_date>");
					setNodeValue(dom, "prom_date", getAbsString(promDatestr));

				}
			}
            //end manish

			valueXmlString.append("<remarks>").append("<![CDATA[" + lsRemarks + "]]>").append("</remarks>");
			setNodeValue(dom, "remarks", getAbsString(lsRemarks));

			valueXmlString.append("<dlv_add1>").append("<![CDATA[" + lsDlvAdd1 + "]]>").append("</dlv_add1>");
			setNodeValue(dom, "dlv_add1", getAbsString(lsDlvAdd1));

			valueXmlString.append("<dlv_add2>").append("<![CDATA[" + lsDlvAdd2 + "]]>").append("</dlv_add2>");
			setNodeValue(dom, "dlv_add2", getAbsString(lsDlvAdd2));

			valueXmlString.append("<dlv_city>").append("<![CDATA[" + lsDlvCity + "]]>").append("</dlv_city>");
			setNodeValue(dom, "dlv_city", getAbsString(lsDlvCity));

			valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + lsCountCodeDlv + "]]>")
			.append("</count_code__dlv>");
			setNodeValue(dom, "count_code__dlv", getAbsString(lsCountCodeDlv));

			valueXmlString.append("<dlv_pin>").append("<![CDATA[" + lsDlvPin + "]]>").append("</dlv_pin>");
			setNodeValue(dom, "dlv_pin", getAbsString(lsDlvPin));

			valueXmlString.append("<stan_code>").append("<![CDATA[" + lsStanCode + "]]>").append("</stan_code>");
			setNodeValue(dom, "stan_code", getAbsString(lsStanCode));

			valueXmlString.append("<part_qty>").append("<![CDATA[" + lsPartQty + "]]>").append("</part_qty>");
			setNodeValue(dom, "part_qty", getAbsString(lsPartQty));

			valueXmlString.append("<tran_code>").append("<![CDATA[" + lsTranCode + "]]>").append("</tran_code>");
			setNodeValue(dom, "tran_code", getAbsString(lsTranCode));

			valueXmlString.append("<udf__str1>").append("<![CDATA[" + lsUdfStr1 + "]]>").append("</udf__str1>");
			setNodeValue(dom, "udf__str1", getAbsString(lsUdfStr1));

			valueXmlString.append("<udf__str2>").append("<![CDATA[" + lsUdfStr2 + "]]>").append("</udf__str2>");
			setNodeValue(dom, "udf__str2", getAbsString(lsUdfStr2));

			valueXmlString.append("<udf__num1>").append("<![CDATA[" + lcUdfNum1 + "]]>").append("</udf__num1>");
			setNodeValue(dom, "udf__num1", getAbsString(lcUdfNum1));

			valueXmlString.append("<udf__num2>").append("<![CDATA[" + lcUdfNum2 + "]]>").append("</udf__num2>");
			setNodeValue(dom, "udf__num2", getAbsString(lcUdfNum2));

			valueXmlString.append("<comm_perc__on>").append("<![CDATA[" + lsCommPercOn + "]]>")
			.append("</comm_perc__on>");
			setNodeValue(dom, "comm_perc__on", getAbsString(String.valueOf(lsCommPercOn)));

			valueXmlString.append("<comm_amt>").append("<![CDATA[" + lcCommAmt + "]]>").append("</comm_amt>");
			setNodeValue(dom, "comm_amt", getAbsString(lcCommAmt));

			valueXmlString.append("<status_remarks>").append("<![CDATA[" + lsStatusRemarks + "]]>")
			.append("</status_remarks>");
			setNodeValue(dom, "status_remarks", getAbsString(lsStatusRemarks));

			valueXmlString.append("<dlv_term>").append("<![CDATA[" + lsDlvTerm + "]]>").append("</dlv_term>");
			setNodeValue(dom, "dlv_term", getAbsString(lsDlvTerm));

			valueXmlString.append("<frt_amt>").append("<![CDATA[" + lcFrtAmt + "]]>").append("</frt_amt>");
			setNodeValue(dom, "frt_amt", getAbsString(lcFrtAmt));

			valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + lsCurrCodeFrt + "]]>")
			.append("</curr_code__frt>");
			setNodeValue(dom, "curr_code__frt", getAbsString(lsCurrCodeFrt));

			valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + lcExchRateFrt + "]]>")
			.append("</exch_rate__frt>");
			setNodeValue(dom, "exch_rate__frt", getAbsString(lcExchRateFrt));

			/*valueXmlString.append("<pord_date>").append("<![CDATA[" + ldPordDate + "]]>").append("</pord_date>");
            setNodeValue(dom, "pord_date", getAbsString(ldPordDate.toString()));*/
            
             //addded by manish mhatre on 22-oct-20
             //start manish
				if (ldPordDate != null) {
					System.out.println("manish ldPordDate before[" + ldPordDate + "]");
					String prodDatestr = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(ldPordDate);
					/*
					 * String prodDatestr= genericUtility.getValidDateString(ldPordDate.toString(),
					 * genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
					 * //addded by manish mhatre on 22-oct-20
					 */
					System.out.println("manish prodDatestr  after [" + prodDatestr + "]");

					if (prodDatestr != null && prodDatestr.trim().length() > 0) {
						valueXmlString.append("<pord_date>").append("<![CDATA[" + prodDatestr + "]]>")
								.append("</pord_date>");
						setNodeValue(dom, "pord_date", getAbsString(prodDatestr));
					}
				}
				// end manish

			valueXmlString.append("<adv_perc>").append("<![CDATA[" + lcAdvPerc + "]]>").append("</adv_perc>");
			setNodeValue(dom, "adv_perc", getAbsString(lcAdvPerc));

			valueXmlString.append("<dist_route>").append("<![CDATA[" + lsDistRoute + "]]>").append("</dist_route>");
			setNodeValue(dom, "dist_route", getAbsString(lsDistRoute));

			valueXmlString.append("<curr_code__comm>").append("<![CDATA[" + lsCurrCodeComm + "]]>")
			.append("</curr_code__comm>");
			setNodeValue(dom, "curr_code__comm", getAbsString(lsCurrCodeComm));

			valueXmlString.append("<sales_pers__1>").append("<![CDATA[" + lsSalesPers1 + "]]>")
			.append("</sales_pers__1>");
			setNodeValue(dom, "sales_pers__1", getAbsString(lsSalesPers1));

			valueXmlString.append("<comm_perc_1>").append("<![CDATA[" + lcCommPerc1 + "]]>").append("</comm_perc_1>");
			setNodeValue(dom, "comm_perc_1", getAbsString(lcCommPerc1));

			valueXmlString.append("<comm_perc_on_1>").append("<![CDATA[" + lsCommPercOn1 + "]]>")
			.append("</comm_perc_on_1>");
			setNodeValue(dom, "comm_perc_on_1", getAbsString(lsCommPercOn1));

			valueXmlString.append("<curr_code__comm_1>").append("<![CDATA[" + lsCurrCodeComm1 + "]]>")
			.append("</curr_code__comm_1>");
			setNodeValue(dom, "curr_code__comm_1", getAbsString(lsCurrCodeComm1));

			valueXmlString.append("<sales_pers__2>").append("<![CDATA[" + lsSalesPers2 + "]]>")
			.append("</sales_pers__2>");
			setNodeValue(dom, "sales_pers__2", getAbsString(lsSalesPers2));

			valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + lcCommPerc2 + "]]>").append("</comm_perc_2>");
			setNodeValue(dom, "comm_perc_2", getAbsString(lcCommPerc2));

			valueXmlString.append("<comm_perc_on_2>").append("<![CDATA[" + lsCommPercOn2 + "]]>")
			.append("</comm_perc_on_2>");
			setNodeValue(dom, "comm_perc_on_2", getAbsString(lsCommPercOn2));

			valueXmlString.append("<curr_code__comm_2>").append("<![CDATA[" + lsCurrCodeComm2 + "]]>")
			.append("</curr_code__comm_2>");
			setNodeValue(dom, "curr_code__comm_2", getAbsString(lsCurrCodeComm2));

			valueXmlString.append("<emp_code__ord>").append("<![CDATA[" + lsEmpCodeCon + "]]>")
			.append("</emp_code__ord>");
			setNodeValue(dom, "emp_code__ord", getAbsString(lsEmpCodeCon));

			/*valueXmlString.append("<udf__date1>").append("<![CDATA[" + ldUdfDate1 + "]]>").append("</udf__date1>");
            setNodeValue(dom, "udf__date1", getAbsString(ldUdfDate1.toString()));*/
            
             //addded by manish mhatre on 21-oct-20
             //start manish
            if(ldUdfDate1!=null)
            {
                System.out.println("manish ldUdfDate1 before[" + ldUdfDate1 + "]");
             /*String udfDatestr= genericUtility.getValidDateString(ldUdfDate1.toString(),
								genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());  //addded by manish mhatre on 22-oct-20*/
             String udfDatestr = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(ldUdfDate1);
                                System.out.println("manish udfDatestr>>>> "+udfDatestr);
             
            
            if(udfDatestr!=null && udfDatestr.trim().length()>0)
            {
           
            valueXmlString.append("<udf__date1>").append("<![CDATA[" + udfDatestr + "]]>").append("</udf__date1>");
            setNodeValue(dom, "udf__date1", getAbsString(udfDatestr));
            }
            }
            //end manish
          
				
			valueXmlString.append("<dlv_add3>").append("<![CDATA[" + lsDlvAdd3 + "]]>").append("</dlv_add3>");
			setNodeValue(dom, "dlv_add3", getAbsString(lsDlvAdd3));

			valueXmlString.append("<proj_code>").append("<![CDATA[" + lsProjCode + "]]>").append("</proj_code>");
			setNodeValue(dom, "proj_code", getAbsString(lsProjCode));

			valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + lsStateCodeDlv + "]]>")
			.append("</state_code__dlv>");
			setNodeValue(dom, "state_code__dlv", getAbsString(lsStateCodeDlv));

			valueXmlString.append("<trans_mode>").append("<![CDATA[" + lsTransMode + "]]>").append("</trans_mode>");
			setNodeValue(dom, "trans_mode", getAbsString(lsTransMode));

			valueXmlString.append("<label_type>").append("<![CDATA[" + lsLabelType + "]]>").append("</label_type>");
			setNodeValue(dom, "label_type", getAbsString(lsLabelType));

			valueXmlString.append("<outside_inspection>").append("<![CDATA[" + lsOutsideInspection + "]]>")
			.append("</outside_inspection>");
			setNodeValue(dom, "outside_inspection", getAbsString(lsOutsideInspection));

			valueXmlString.append("<remarks2>").append("<![CDATA[" + lsRemarks2 + "]]>").append("</remarks2>");
			setNodeValue(dom, "remarks2", getAbsString(lsRemarks2));

			valueXmlString.append("<remarks3>").append("<![CDATA[" + lsRemarks3 + "]]>").append("</remarks3>");
			setNodeValue(dom, "remarks3", getAbsString(lsRemarks3));

			valueXmlString.append("<stan_code__init>").append("<![CDATA[" + lsStanCodeInit + "]]>")
			.append("</stan_code__init>");
			setNodeValue(dom, "stan_code__init", getAbsString(lsStanCodeInit));

			valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + lsCurrCodeIns + "]]>")
			.append("</curr_code__ins>");
			setNodeValue(dom, "curr_code__ins", getAbsString(lsCurrCodeIns));

			valueXmlString.append("<ins_amt>").append("<![CDATA[" + lcInsAmt + "]]>").append("</ins_amt>");
			setNodeValue(dom, "ins_amt", getAbsString(lcInsAmt));

			valueXmlString.append("<dlv_to>").append("<![CDATA[" + lsDlvTo + "]]>").append("</dlv_to>");
			setNodeValue(dom, "dlv_to", getAbsString(lsDlvTo));

			valueXmlString.append("<acct_code__sal>").append("<![CDATA[" + lsAcctCodeSal + "]]>")
			.append("</acct_code__sal>");
			setNodeValue(dom, "acct_code__sal", getAbsString(lsAcctCodeSal));

			valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + lsCctrCodeSal + "]]>")
			.append("</cctr_code__sal>");
			setNodeValue(dom, "cctr_code__sal", getAbsString(lsCctrCodeSal));

			valueXmlString.append("<tel1__dlv>").append("<![CDATA[" + tel1 + "]]>").append("</tel1__dlv>");
			setNodeValue(dom, "tel1__dlv", getAbsString(tel1));

			valueXmlString.append("<tel2__dlv>").append("<![CDATA[" + tel2 + "]]>").append("</tel2__dlv>");
			setNodeValue(dom, "tel2__dlv", getAbsString(tel2));

			valueXmlString.append("<tel3__dlv>").append("<![CDATA[" + tel3 + "]]>").append("</tel3__dlv>");
			setNodeValue(dom, "tel3__dlv", getAbsString(tel3));

			valueXmlString.append("<fax__dlv>").append("<![CDATA[" + fax + "]]>").append("</fax__dlv>");
			setNodeValue(dom, "fax__dlv", getAbsString(fax));

			valueXmlString.append("<exch_rate__comm>").append("<![CDATA[" + lcExchRateComm + "]]>")
			.append("</exch_rate__comm>");
			setNodeValue(dom, "exch_rate__comm", getAbsString(lcExchRateComm));

			valueXmlString.append("<exch_rate__comm_1>").append("<![CDATA[" + lcExchRateComm1 + "]]>")
			.append("</exch_rate__comm_1>");
			setNodeValue(dom, "exch_rate__comm_1", getAbsString(lcExchRateComm1));

			valueXmlString.append("<exch_rate__comm_2>").append("<![CDATA[" + lcExchRateComm2 + "]]>")
			.append("</exch_rate__comm_2>");
			setNodeValue(dom, "exch_rate__comm_2", getAbsString(lcExchRateComm2));

			valueXmlString.append("<price_list__disc>").append("<![CDATA[" + lsPriceListDisc + "]]>")
			.append("</price_list__disc>");
			setNodeValue(dom, "price_list__disc", getAbsString(lsPriceListDisc));

			valueXmlString.append("<market_reg>").append("<![CDATA[" + lsMarketReg + "]]>").append("</market_reg>");
			setNodeValue(dom, "market_reg", getAbsString(lsMarketReg));

			valueXmlString.append("<order_type>").append("<![CDATA[" + lsContractType + "]]>").append("</order_type>");
			setNodeValue(dom, "order_type", getAbsString(lsContractType));

			sql = "select descr from sordertype where order_type =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsOrderType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsOtypeDescr = rs.getString("descr");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "select loc_group from customer where cust_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsCustCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsLocGroup = checkNull(rs.getString("loc_group"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			System.out.println("lsLocGroup ====>>[" + lsLocGroup + "]");
			valueXmlString.append("<loc_group>").append("<![CDATA[" + lsLocGroup + "]]>").append("</loc_group>");
			setNodeValue(dom, "loc_group", getAbsString(lsLocGroup));
			if (lsSalesPers != null && !"null".contentEquals(lsSalesPers) && lsSalesPers.trim().length() > 0) {
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
				if (reStr.indexOf("Detail1>") > -1) {
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}
			}
			if (lsSalesPers1 != null && !"null".contentEquals(lsSalesPers1) && lsSalesPers1.trim().length() > 0) {
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "sales_pers__1", editFlag, xtraParams);
				if (reStr.indexOf("Detail1>") > -1) {
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}
			}

			if (lsSalesPers2 != null && !"null".contentEquals(lsSalesPers2) && lsSalesPers2.trim().length() > 0) {
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "sales_pers__2", editFlag, xtraParams);
				if (reStr.indexOf("Detail1>") > -1) {
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}
			}

			if (lsEmpCodeCon != null && !"null".contentEquals(lsEmpCodeCon) && lsEmpCodeCon.trim().length() > 0) {
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "emp_code__ord", editFlag, xtraParams);
				if (reStr.indexOf("Detail1>") > -1) {
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}
			}
			/*
			reStr = itemChangedDet(dom, dom1, dom2, objContext, "emp_code__ord1", editFlag, xtraParams);
			pos = reStr.indexOf("<Detail1>");
			reStr = reStr.substring(pos + 9);
			pos = reStr.indexOf("</Detail1>");
			reStr = reStr.substring(0, pos);
			valueXmlString.append(reStr);
			*/
			sql = "select term_table__no from customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsCustCodeDlv);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsTermTable = rs.getString("term_table__no");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			valueXmlString.append("<term_table__no>").append("<![CDATA[" + lsTermTable + "]]>")
			.append("</term_table__no>");
			setNodeValue(dom, "term_table__no", getAbsString(lsTermTable));
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer itmDefaultHdr(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", empCode = "", loginSite = "", lsDeptcode = "", mDlvsite = "", mOrdSite = "", mStatus = "",
				mCurrency = "", orderStatus = "", mTaxOpt = "", mSingleSer = "";
		String lsDueDays = "";
		double mExcRate = 0.00;
		Timestamp ldDueDate = null;
		String reStr = "";
		int pos = 0;

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		System.out.println("Calling Item default HDR:- @@@");
		try {
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String tranDate = simpleDateFormatObj.format(currentDate.getTime());
			empCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSite");

			/**
			 * Find dept code
			 */
			sql = "select dept_code from employee where emp_code  = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsDeptcode = checkNull(rs.getString("dept_code"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			valueXmlString.append("<dept_code>").append("<![CDATA[" + lsDeptcode + "]]>").append("</dept_code>");
			setNodeValue(dom, "dept_code", getAbsString(lsDeptcode));
			valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
			setNodeValue(dom, "site_code", getAbsString(loginSite));
			valueXmlString.append("<site_code__ship>").append("<![CDATA[" + loginSite + "]]>")
			.append("</site_code__ship>");
			setNodeValue(dom, "site_code__ship", getAbsString(loginSite));
			valueXmlString.append("<tax_date>").append("<![CDATA[" + tranDate + "]]>").append("</tax_date>");
			setNodeValue(dom, "tax_date", getAbsString(tranDate));
			valueXmlString.append("<status_date>").append("<![CDATA[" + tranDate + "]]>").append("</status_date>");
			setNodeValue(dom, "status_date", getAbsString(tranDate));
			valueXmlString.append("<order_date>").append("<![CDATA[" + tranDate + "]]>").append("</order_date>");
			setNodeValue(dom, "order_date", getAbsString(tranDate));
			valueXmlString.append("<pl_date>").append("<![CDATA[" + tranDate + "]]>").append("</pl_date>");
			setNodeValue(dom, "pl_date", getAbsString(tranDate));
			valueXmlString.append("<due_date>").append("<![CDATA[" + tranDate + "]]>").append("</due_date>");
			setNodeValue(dom, "due_date", getAbsString(tranDate));
			valueXmlString.append("<order_status>").append("<![CDATA[" + orderStatus + "]]>").append("</order_status>");
			setNodeValue(dom, "order_status", getAbsString(orderStatus));
			valueXmlString.append("<emp_code__ord>").append("<![CDATA[" + empCode + "]]>").append("</emp_code__ord>");
			setNodeValue(dom, "emp_code__ord", getAbsString(empCode));

			sql = "Select site_code__dlv, site_code__ord, status, curr_code__purc,tax_opt, single_ser from Purcctrl";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				mDlvsite = checkNull(rs.getString("site_code__dlv"));
				mOrdSite = checkNull(rs.getString("site_code__ord"));
				mStatus = checkNull(rs.getString("status"));
				mCurrency = checkNull(rs.getString("curr_code__purc"));
				mTaxOpt = checkNull(rs.getString("tax_opt"));
				mSingleSer = checkNull(rs.getString("single_ser"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			valueXmlString.append("<curr_code>").append("<![CDATA[" + mCurrency + "]]>").append("</curr_code>");
			setNodeValue(dom, "curr_code", getAbsString(mCurrency));

			sql = "Select std_exrt from currency where curr_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mCurrency);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				mExcRate = rs.getDouble("std_exrt");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			valueXmlString.append("<exch_rate>").append("<![CDATA[" + mExcRate + "]]>").append("</exch_rate>");
			setNodeValue(dom, "exch_rate", getAbsString(String.valueOf(mExcRate)));

			valueXmlString.append("<price_list protect = \"0\">").append("<![CDATA[]]>").append("</price_list>");
			setNodeValue(dom, "price_list", getAbsString(""));

			valueXmlString.append("<cust_code protect = \"0\">").append("<![CDATA[]]>").append("</cust_code>");
			setNodeValue(dom, "curr_code", getAbsString(""));

			valueXmlString.append("<cust_code__bil protect = \"0\">").append("<![CDATA[]]>")
			.append("</cust_code__bil>");
			setNodeValue(dom, "cust_code__bil", getAbsString(""));

			//Added by Dipesh p on[17/07/2019][Start][Request Id: D20BSER008] The system is setting an empty value in item_ser
			//valueXmlString.append("<item_ser protect = \"0\">").append("<![CDATA[]]>").append("</item_ser>");
			//setNodeValue(dom, "item_ser", getAbsString(""));
			//Added by Dipesh p on[17/07/2019][End][Request Id: D20BSER008] The system is setting an empty value in item_ser
			
			valueXmlString.append("<price_list__disc protect = \"0\">").append("<![CDATA[]]>")
			.append("</price_list__disc>");
			setNodeValue(dom, "price_list__disc", getAbsString(""));

			valueXmlString.append("<order_type protect = \"0\">").append("<![CDATA[]]>").append("</order_type>");
			setNodeValue(dom, "order_type", getAbsString(""));

			lsDueDays = distCommon.getDisparams("999999", "DELV_DAYS", conn);
			if (lsDueDays == null || lsDueDays.trim().length() == 0 || lsDueDays.equalsIgnoreCase("NULLFOUND")) {
				valueXmlString.append("<due_date>").append("<![CDATA[" + tranDate + "]]>").append("</due_date>");
				setNodeValue(dom, "due_date", getAbsString(tranDate));
			} else {
				ldDueDate = utlMethods.RelativeDate(Timestamp.valueOf(tranDate), Integer.parseInt(lsDueDays));
				valueXmlString.append("<due_date>").append("<![CDATA[" + ldDueDate + "]]>").append("</due_date>");
				setNodeValue(dom, "due_date", getAbsString(ldDueDate.toString()));
			}

			valueXmlString.append("<comm_perc protect = \"1\">").append("<![CDATA[]]>").append("</comm_perc>");
			setNodeValue(dom, "comm_perc", getAbsString(""));

			valueXmlString.append("<comm_perc__on protect = \"1\">").append("<![CDATA[]]>").append("</comm_perc__on>");
			setNodeValue(dom, "comm_perc__on", getAbsString(""));

			valueXmlString.append("<curr_code__comm protect = \"1\">").append("<![CDATA[]]>")
			.append("</curr_code__comm>");
			setNodeValue(dom, "curr_code__comm", getAbsString(""));

			valueXmlString.append("<exch_rate__comm protect = \"1\">").append("<![CDATA[]]>")
			.append("</exch_rate__comm>");
			setNodeValue(dom, "exch_rate__comm", getAbsString(""));

			valueXmlString.append("<comm_perc_1 protect = \"1\">").append("<![CDATA[]]>").append("</comm_perc_1>");
			setNodeValue(dom, "comm_perc_1", getAbsString(""));

			valueXmlString.append("<comm_perc_on_1 protect = \"1\">").append("<![CDATA[]]>")
			.append("</comm_perc_on_1>");
			setNodeValue(dom, "comm_perc_on_1", getAbsString(""));

			valueXmlString.append("<curr_code__comm_1 protect = \"1\">").append("<![CDATA[]]>")
			.append("</curr_code__comm_1>");
			setNodeValue(dom, "curr_code__comm_1", getAbsString(""));

			valueXmlString.append("<exch_rate__comm_1 protect = \"1\">").append("<![CDATA[]]>")
			.append("</exch_rate__comm_1>");
			setNodeValue(dom, "exch_rate__comm_1", getAbsString(""));

			valueXmlString.append("<comm_perc_2 protect = \"1\">").append("<![CDATA[]]>").append("</comm_perc_2>");
			setNodeValue(dom, "comm_perc_2", getAbsString(""));

			valueXmlString.append("<comm_perc_on_2 protect = \"1\">").append("<![CDATA[]]>")
			.append("</comm_perc_on_2>");
			setNodeValue(dom, "comm_perc_on_2", getAbsString(""));

			valueXmlString.append("<curr_code__comm_2 protect = \"1\">").append("<![CDATA[]]>")
			.append("</curr_code__comm_2>");
			setNodeValue(dom, "curr_code__comm_2", getAbsString(""));

			valueXmlString.append("<exch_rate__comm_2 protect = \"1\">").append("<![CDATA[]]>")
			.append("</exch_rate__comm_2>");
			setNodeValue(dom, "exch_rate__comm_2", getAbsString(""));

			reStr = itemChangedHdr(dom, dom1, dom2, objContext, "emp_code__ord", editFlag, xtraParams);
			pos = reStr.indexOf("<Detail1>");
			reStr = reStr.substring(pos + 9);
			pos = reStr.indexOf("</Detail1>");
			reStr = reStr.substring(0, pos);
			valueXmlString.append(reStr);

			reStr = itemChangedHdr(dom, dom1, dom2, objContext, "emp_code__ord1", editFlag, xtraParams);
			pos = reStr.indexOf("<Detail1>");
			reStr = reStr.substring(pos + 9);
			pos = reStr.indexOf("</Detail1>");
			reStr = reStr.substring(0, pos);
			valueXmlString.append(reStr);

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added BY Mukesh Chauhan on 06/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added  By Mukesh Chauhan on 05/08/19
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer itmDefaultEdit(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException, SQLException {
		// TODO Auto-generated method stub

		String sql = "", currCodeIns = "", priceList = "", priceListClg = "", custCodeBil = "", itemSer = "",
				priceListDisc = "", orderType = "", commPerc = "", commPercOn = "";
		String currCodeComm = "", commPerc1 = "", commPercOn1 = "", currCodeComm1 = "", commPerc2 = "",
				commPercOn2 = "", currCodeComm2 = "", mkeyval = "", lsOrderType = "";
		String lsOtypeDescr = "", lsSiteCode = "", lsCustCode = "", lsPlist = "", lsPlist1 = "", lsPlist2 = "",
				lsSalesPers = "", lsSalesPers1 = "", lsSalesPers2 = "";
		double exchRateIns = 0.00, insAmt = 0.00, exchRate = 0.00, exchRateFrt = 0.00, exchRateComm = 0.00,
				exchRateComm1 = 0.00, exchRateComm2 = 0.00;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		try {
			mkeyval = checkNull(getValueFromXTRA_PARAMS(xtraParams, "ref_id"));

			sql = "select count(*) as CNT from insurance_det where ref_ser = 'S-ORD' and ref_id =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mkeyval);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt("CNT");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			currCodeIns = checkNull(genericUtility.getColumnValue("curr_code__ins", dom));
			exchRateIns = Double.parseDouble(genericUtility.getColumnValue("exch_rate__ins", dom) == null ? "00.00"
					: genericUtility.getColumnValue("exch_rate__ins", dom));
			insAmt = Double.parseDouble(genericUtility.getColumnValue("ins_amt", dom) == null ? "00.00"
					: genericUtility.getColumnValue("ins_amt", dom));
			if (cnt > 0) {
				valueXmlString.append("<curr_code__ins protect = \"1\">").append("<![CDATA[" + currCodeIns + "]]>")
				.append("</curr_code__ins>");
				valueXmlString.append("<exch_rate__ins protect = \"1\">").append("<![CDATA[" + exchRateIns + "]]>")
				.append("</exch_rate__ins>");
				valueXmlString.append("<ins_amt protect = \"1\">").append("<![CDATA[" + insAmt + "]]>")
				.append("</ins_amt>");
			}
			lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom));
			sql = "select descr from sordertype where order_type =?";
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1, mkeyval); 
			pstmt.setString(1, lsOrderType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsOtypeDescr = rs.getString("descr");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			valueXmlString.append("<descr>").append("<![CDATA[" + lsOtypeDescr + "]]>").append("</descr>");

			mkeyval = checkNull(genericUtility.getColumnValue("sale_order", dom));
			sql = "select count(*) as CNT from sorddet where sale_order = ?"; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mkeyval);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt("CNT");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

			sql = "select price_list from site_customer where site_code =?  and cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSiteCode);
			pstmt.setString(2, lsCustCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsPlist = checkNull(rs.getString("price_list"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "select price_list  from customer where site_code = ?  and cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSiteCode);
			pstmt.setString(2, lsCustCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsPlist1 = checkNull(rs.getString("price_list"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "select price_list from sorder where site_code = ? and sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSiteCode);
			pstmt.setString(2, mkeyval);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsPlist2 = checkNull(rs.getString("price_list"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			priceList = checkNull(genericUtility.getColumnValue("price_list", dom));
			priceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom));
			if ((lsPlist != null && lsPlist.trim().length() > 0)
					|| (lsPlist1 != null && lsPlist1.trim().length() > 0)) {
				valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + priceList + "]]>")
				.append("</price_list>");
				valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + priceListClg + "]]>")
				.append("</price_list__clg>");
			} else if (cnt == 0 && lsPlist == null && lsPlist1 == null && lsPlist2 != null) {
				/*valueXmlString.append("<price_list protect = \"0\">").append("<![CDATA[" + priceList + "]]>")
				.append("</price_list>");
				valueXmlString.append("<price_list__clg protect = \"0\">").append("<![CDATA[" + priceListClg + "]]>")
				.append("</price_list__clg>");*/
				valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + priceList + "]]>")
				.append("</price_list>");   //protected by manish mhatre on 22-apr-2020
				valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + priceListClg + "]]>")
				.append("</price_list__clg>");   //protected by manish mhatre on 22-apr-2020
			}
			custCodeBil = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			priceListDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom));
			orderType = checkNull(genericUtility.getColumnValue("order_type", dom));
			if (cnt > 0) {
				valueXmlString.append("<cust_code protect = \"1\">").append("<![CDATA[" + lsCustCode + "]]>")
				.append("</cust_code>");
				valueXmlString.append("<cust_code__bil protect = \"1\">").append("<![CDATA[" + custCodeBil + "]]>")
				.append("</cust_code__bil>");
				valueXmlString.append("<item_ser protect = \"1\">").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
				valueXmlString.append("<price_list protect = \"1\">").append("<![CDATA[" + priceList + "]]>")
				.append("</price_list>");
				valueXmlString.append("<price_list__disc protect = \"1\">").append("<![CDATA[" + priceListDisc + "]]>")
				.append("</price_list__disc>");
				valueXmlString.append("<order_type protect = \"1\">").append("<![CDATA[" + orderType + "]]>")
				.append("</order_type>");
				valueXmlString.append("<price_list__clg protect = \"1\">").append("<![CDATA[" + priceListClg + "]]>")
				.append("</price_list__clg>");
			} else {
				valueXmlString.append("<cust_code protect = \"0\">").append("<![CDATA[" + lsCustCode + "]]>")
				.append("</cust_code>");
				valueXmlString.append("<cust_code__bil protect = \"0\">").append("<![CDATA[" + custCodeBil + "]]>")
				.append("</cust_code__bil>");
				valueXmlString.append("<item_ser protect = \"0\">").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
				/*valueXmlString.append("<price_list__disc protect = \"0\">").append("<![CDATA[" + priceListDisc + "]]>")
				.append("</price_list__disc>");*/
				valueXmlString.append("<price_list__disc protect = \"1\">").append("<![CDATA[" + priceListDisc + "]]>")
				.append("</price_list__disc>");   //protected by manish mhatre on 22-apr-2020
				valueXmlString.append("<order_type protect = \"0\">").append("<![CDATA[" + orderType + "]]>")
				.append("</order_type>");
			}
			exchRate = Double.parseDouble(genericUtility.getColumnValue("exch_rate", dom) == null ? "0"
					: genericUtility.getColumnValue("exch_rate", dom));
			exchRateFrt = Double.parseDouble(genericUtility.getColumnValue("exch_rate__frt", dom) == null ? "0"
					: genericUtility.getColumnValue("exch_rate__frt", dom));
			exchRateIns = Double.parseDouble(genericUtility.getColumnValue("exch_rate__ins", dom) == null ? "0"
					: genericUtility.getColumnValue("exch_rate__ins", dom));
			exchRateComm = Double.parseDouble(genericUtility.getColumnValue("exch_rate__comm", dom) == null ? "0"
					: genericUtility.getColumnValue("exch_rate__comm", dom));
			exchRateComm1 = Double.parseDouble(genericUtility.getColumnValue("exch_rate__comm_1", dom) == null ? "0"
					: genericUtility.getColumnValue("exch_rate__comm_1", dom));
			exchRateComm2 = Double.parseDouble(genericUtility.getColumnValue("exch_rate__comm_2", dom) == null ? "0"
					: genericUtility.getColumnValue("exch_rate__comm_2", dom));

			valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA[" + exchRate + "]]>")
			.append("</exch_rate>");
			valueXmlString.append("<exch_rate__frt protect = \"1\">").append("<![CDATA[" + exchRateFrt + "]]>")
			.append("</exch_rate__frt>");
			valueXmlString.append("<exch_rate__ins protect = \"1\">").append("<![CDATA[" + exchRateIns + "]]>")
			.append("</exch_rate__ins>");
			valueXmlString.append("<exch_rate__comm protect = \"1\">").append("<![CDATA[" + exchRateComm + "]]>")
			.append("</exch_rate__comm>");
			valueXmlString.append("<exch_rate__comm_1 protect = \"1\">").append("<![CDATA[" + exchRateComm1 + "]]>")
			.append("</exch_rate__comm_1>");
			valueXmlString.append("<exch_rate__comm_2 protect = \"1\">").append("<![CDATA[" + exchRateComm2 + "]]>")
			.append("</exch_rate__comm_2>");

			lsSalesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));
			lsSalesPers1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom));
			lsSalesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom));

			commPerc = checkNull(genericUtility.getColumnValue("comm_perc", dom));
			commPercOn = checkNull(genericUtility.getColumnValue("comm_perc__on", dom));
			currCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm", dom));
			commPerc1 = checkNull(genericUtility.getColumnValue("comm_perc_1", dom));
			commPercOn1 = checkNull(genericUtility.getColumnValue("comm_perc_on_1", dom));
			currCodeComm1 = checkNull(genericUtility.getColumnValue("curr_code__comm_1", dom));
			commPerc2 = checkNull(genericUtility.getColumnValue("comm_perc_2", dom));
			commPercOn2 = checkNull(genericUtility.getColumnValue("comm_perc_on_2", dom));
			currCodeComm2 = checkNull(genericUtility.getColumnValue("curr_code__comm_2", dom));

			if (lsSalesPers == null || lsSalesPers.trim().length() == 0) {
				valueXmlString.append("<comm_perc protect = \"1\">").append("<![CDATA[" + commPerc + "]]>")
				.append("</comm_perc>");
				valueXmlString.append("<comm_perc__on protect = \"1\">").append("<![CDATA[" + commPercOn + "]]>")
				.append("</comm_perc__on>");
				valueXmlString.append("<curr_code__comm protect = \"1\">").append("<![CDATA[" + currCodeComm + "]]>")
				.append("</curr_code__comm>");
			} else {
				valueXmlString.append("<comm_perc protect = \"0\">").append("<![CDATA[" + commPerc + "]]>")
				.append("</comm_perc>");
				valueXmlString.append("<comm_perc__on protect = \"0\">").append("<![CDATA[" + commPercOn + "]]>")
				.append("</comm_perc__on>");
				valueXmlString.append("<curr_code__comm protect = \"0\">").append("<![CDATA[" + currCodeComm + "]]>")
				.append("</curr_code__comm>");
			}

			if (lsSalesPers1 == null || lsSalesPers1.trim().length() == 0) {
				valueXmlString.append("<comm_perc_1 protect = \"1\">").append("<![CDATA[" + commPerc1 + "]]>")
				.append("</comm_perc_1>");
				valueXmlString.append("<comm_perc_on_1 protect = \"1\">").append("<![CDATA[" + commPercOn1 + "]]>")
				.append("</comm_perc_on_1>");
				valueXmlString.append("<curr_code__comm_1 protect = \"1\">").append("<![CDATA[" + currCodeComm1 + "]]>")
				.append("</curr_code__comm_1>");
			} else {
				valueXmlString.append("<comm_perc_1 protect = \"0\">").append("<![CDATA[" + commPerc1 + "]]>")
				.append("</comm_perc_1>");
				valueXmlString.append("<comm_perc_on_1 protect = \"0\">").append("<![CDATA[" + commPercOn1 + "]]>")
				.append("</comm_perc_on_1>");
				valueXmlString.append("<curr_code__comm_1 protect = \"0\">").append("<![CDATA[" + currCodeComm1 + "]]>")
				.append("</curr_code__comm_1>");
			}

			if (lsSalesPers2 == null || lsSalesPers2.trim().length() == 0) {
				valueXmlString.append("<comm_perc_2 protect = \"1\">").append("<![CDATA[" + commPerc2 + "]]>")
				.append("</comm_perc_2>");
				valueXmlString.append("<comm_perc_on_2 protect = \"1\">").append("<![CDATA[" + commPercOn2 + "]]>")
				.append("</comm_perc_on_2>");
				valueXmlString.append("<curr_code__comm_2 protect = \"1\">").append("<![CDATA[" + currCodeComm2 + "]]>")
				.append("</curr_code__comm_2>");
			} else {
				valueXmlString.append("<comm_perc_2 protect = \"0\">").append("<![CDATA[" + commPerc2 + "]]>")
				.append("</comm_perc_2>");
				valueXmlString.append("<comm_perc_on_2 protect = \"0\">").append("<![CDATA[" + commPercOn2 + "]]>")
				.append("</comm_perc_on_2>");
				valueXmlString.append("<curr_code__comm_2 protect = \"0\">").append("<![CDATA[" + currCodeComm2 + "]]>")
				.append("</curr_code__comm_2>");
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private Object itmCustCodeNotify(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsTermCode = "", descr = "", lsCurrCodeComm = "", lsSiteCode = "", lsCurrCode = "",
				lsCurrCodeComm1 = "", lsCurrCodeComm2 = "", lsCurrCodeIns = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String custCodeNotify = "", itemSer = "", custName = "", addr1 = "", addr2 = "", addr3 = "", city = "",
				pin = "", countCode = "", stanCode = "", stateCode = "", tele1 = "", tele2 = "", tele3 = "", fax1 = "",
				tranCode = "", dlvTerm = "";

		try {
			custCodeNotify = checkNull(genericUtility.getColumnValue("cust_code__notify", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			sql = "select cust_name, addr1, addr2,addr3, city, pin, count_code, stan_code,state_code, tele1, tele2, tele3, fax,"
					+ " tran_code from customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCodeNotify);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				custName = rs.getString("cust_name");
				addr1 = rs.getString("addr1");
				addr2 = rs.getString("addr2");
				addr3 = rs.getString("addr3");
				city = rs.getString("city");
				pin = rs.getString("pin");
				countCode = rs.getString("count_code");
				stanCode = rs.getString("stan_code");
				stateCode = rs.getString("state_code");
				tele1 = rs.getString("tele1");
				tele2 = rs.getString("tele2");
				tele3 = rs.getString("tele3");
				fax1 = rs.getString("fax");
				tranCode = rs.getString("tran_code");

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			valueXmlString.append("<notify_to>").append("<![CDATA[" + custName + "]]>").append("</notify_to>");
			valueXmlString.append("<notify_add1>").append("<![CDATA[" + addr1 + "]]>").append("</notify_add1>");
			valueXmlString.append("<notify_add2>").append("<![CDATA[" + addr2 + "]]>").append("</notify_add2>");
			valueXmlString.append("<notify_add3>").append("<![CDATA[" + addr3 + "]]>").append("</notify_add3>");
			valueXmlString.append("<notify_city>").append("<![CDATA[" + city + "]]>").append("</notify_city>");
			valueXmlString.append("<notify_pin>").append("<![CDATA[" + pin + "]]>").append("</notify_pin>");
			valueXmlString.append("<count_code__notify>").append("<![CDATA[" + countCode + "]]>")
			.append("</count_code__notify>");
			valueXmlString.append("<stan_code__notify>").append("<![CDATA[" + stanCode + "]]>")
			.append("</stan_code__notify>");
			valueXmlString.append("<state_code__notify>").append("<![CDATA[" + stateCode + "]]>")
			.append("</state_code__notify>");
			valueXmlString.append("<tel1__notify>").append("<![CDATA[" + tele1 + "]]>").append("</tel1__notify>");
			valueXmlString.append("<tel2__notify>").append("<![CDATA[" + tele2 + "]]>").append("</tel2__notify>");
			valueXmlString.append("<tel3__notify>").append("<![CDATA[" + tele3 + "]]>").append("</tel3__notify>");
			valueXmlString.append("<fax__notify>").append("<![CDATA[" + fax1 + "]]>").append("</fax__notify>");

			sql = "select descr from station where stan_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, stanCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				descr = rs.getString("descr");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			valueXmlString.append("<stan_descr__notify>").append("<![CDATA[" + descr + "]]>")
			.append("</stan_descr__notify>");

			sql = "select dlv_term from  customer_series 	where  cust_code = ? and  item_ser =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCodeNotify);
			pstmt.setString(2, itemSer);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				dlvTerm = rs.getString("dlv_term");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (dlvTerm == null || dlvTerm.trim().length() == 0) {
				sql = "select dlv_term from  customer	 where  cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCodeNotify);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					dlvTerm = rs.getString("dlv_term");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			/*
			 * if (dlvTerm == null || dlvTerm.trim().length() == 0) { dlvTerm = "NA"; }
			 */
			// Changed By Pragyan 19/Mar/17 to set notify term
			// valueXmlString.append("<ls_notify_term>").append("<![CDATA[" + dlvTerm +
			// "]]>").append("</ls_notify_term>");
			valueXmlString.append("<notify_term>").append("<![CDATA[" + dlvTerm + "]]>").append("</notify_term>");

		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return valueXmlString;
	}

	private StringBuffer itmCurrCodeIns(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsTermCode = "", descr = "", lsCurrCodeComm = "", lsSiteCode = "", lsCurrCode = "",
				lsCurrCodeComm1 = "", lsCurrCodeComm2 = "", lsCurrCodeIns = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String orderDate = "";
		try {
			double mNum = 0.00;
			lsCurrCodeIns = checkNull(genericUtility.getColumnValue("curr_code__ins", dom));
			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			//to handle parsing exceptio in getDailyExchRateSellBuy
			/*Timestamp orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");*/
			orderDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
			mNum = finCommon.getDailyExchRateSellBuy(lsCurrCodeIns, "", lsSiteCode, orderDate, "S", conn);

			valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + mNum + "]]>").append("</exch_rate__ins>");
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
			//exchangeRateProtect(lsCurrCodeIns, lsSiteCode, "exch_rate__ins", conn);
			String retVal = exchangeRateProtect(lsCurrCodeIns, lsSiteCode, "exch_rate__ins", conn);
			System.out.println("retVal--455["+retVal+"]");
			valueXmlString.append(retVal);
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]

		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer itmCurrCodeComm2(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsTermCode = "", descr = "", lsCurrCodeComm = "", lsSiteCode = "", lsCurrCode = "",
				lsCurrCodeComm1 = "", lsCurrCodeComm2 = "";
		String excRateStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String orderDate="";
		try {
			double mNum = 0.00;
			lsCurrCodeComm2 = checkNull(genericUtility.getColumnValue("curr_code__comm2", dom));

			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			lsCurrCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
			//to handle parsing exceptio in getDailyExchRateSellBuy
			/*Timestamp orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");*/

			// added by mahesh saggam on 09/aug/2019 [Start]
			excRateStr = checkNull(genericUtility.getColumnValue("exch_rate", dom));
			excRateStr = excRateStr == null || excRateStr.trim().length() <= 0 ? "0" : excRateStr;
			// mahesh saggam [End]	

			if (lsCurrCodeComm2.equalsIgnoreCase(lsCurrCode)) {
				// mNum = Double.valueOf(genericUtility.getColumnValue("exch_rate", dom)); // commented by mahesh saggam on 09/aug/2019
				mNum = Double.parseDouble(excRateStr);
			} else {
				orderDate = checkNull(genericUtility.getColumnValue("order_date", dom1)); 
				mNum = finCommon.getDailyExchRateSellBuy(lsCurrCodeComm2, "", lsSiteCode, orderDate, "S",
						conn);
			}

			valueXmlString.append("<exch_rate__comm2>").append("<![CDATA[" + mNum + "]]>")
			.append("</exch_rate__comm2>");
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
			//exchangeRateProtect(lsCurrCodeComm2, lsSiteCode, "exch_rate__comm2", conn);
			String retVal = exchangeRateProtect(lsCurrCodeComm2, lsSiteCode, "exch_rate__comm2", conn);
			System.out.println("retVal--455["+retVal+"]");
			valueXmlString.append(retVal);
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; // Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer itmCurrCodeComm1(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsTermCode = "", descr = "", lsCurrCodeComm = "", lsSiteCode = "", lsCurrCode = "",
				lsCurrCodeComm1 = "";
		String excRateStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String orderDate = "";
		try {
			double mNum = 0.00;
			lsCurrCodeComm1 = checkNull(genericUtility.getColumnValue("curr_code__comm1", dom));

			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			lsCurrCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
			//to handle parsing exceptio in getDailyExchRateSellBuy
			/*Timestamp orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");*/

			// added by mahesh saggam on 09/aug/2019 [Start]
			excRateStr = checkNull(genericUtility.getColumnValue("exch_rate", dom));
			excRateStr = excRateStr == null || excRateStr.trim().length() <= 0 ? "0" : excRateStr;
			// mahesh saggam [End]

			if (lsCurrCodeComm1.equalsIgnoreCase(lsCurrCode)) {
				// mNum = Double.valueOf(genericUtility.getColumnValue("exch_rate", dom));  // commented by mahesh saggam on 09/aug/2019
				mNum = Double.parseDouble(excRateStr);
			} else {
				orderDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
				mNum = finCommon.getDailyExchRateSellBuy(lsCurrCodeComm1, "", lsSiteCode, orderDate.toString(), "S",
						conn);
			}

			valueXmlString.append("<exch_rate__comm1>").append("<![CDATA[" + mNum + "]]>")
			.append("</exch_rate__comm1>");
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
			//exchangeRateProtect(lsCurrCodeComm1, lsSiteCode, "exch_rate__comm1", conn);
			String retVal = exchangeRateProtect(lsCurrCodeComm1, lsSiteCode, "exch_rate__comm1", conn);
			System.out.println("retVal--455["+retVal+"]");
			valueXmlString.append(retVal);
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; // Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer itmCurrCodeComm(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsTermCode = "", descr = "", lsCurrCodeComm = "", lsSiteCode = "", lsCurrCode = "";
		String excRateStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			double mNum = 0.00;
			lsCurrCodeComm = checkNull(genericUtility.getColumnValue("curr_code__comm", dom));
			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			lsCurrCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
			// Timestamp
			// orderDate=Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date",
			// dom1), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
			// + " 00:00:00.0");
			String ordDatStr = checkNull(genericUtility.getColumnValue("order_date", dom1));

			// added by mahesh saggam on 09/aug/2019 [Start]
			excRateStr = checkNull(genericUtility.getColumnValue("exch_rate", dom));
			excRateStr = excRateStr == null || excRateStr.trim().length() <= 0 ? "0" : excRateStr;
			// mahesh saggam [End]			

			if (lsCurrCodeComm.equalsIgnoreCase(lsCurrCode)) {
				//mNum = Double.valueOf(genericUtility.getColumnValue("exch_rate", dom)); // commented by mahesh saggam on 09/aug//2019
				mNum = Double.parseDouble(excRateStr);
			} else {
				mNum = finCommon.getDailyExchRateSellBuy(lsCurrCodeComm, "", lsSiteCode, ordDatStr, "S", conn);
			}

			valueXmlString.append("<exch_rate__comm>").append("<![CDATA[" + mNum + "]]>").append("</exch_rate__comm>");
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
			//exchangeRateProtect(lsCurrCodeComm, lsSiteCode, "exch_rate__comm", conn);
			String retVal = exchangeRateProtect(lsCurrCodeComm, lsSiteCode, "exch_rate__comm", conn);
			System.out.println("retVal--455["+retVal+"]");
			valueXmlString.append(retVal);
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer gbfIcTermCode(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql = "", lsTermCode = "", descr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			lsTermCode = checkNull(genericUtility.getColumnValue("term_code", dom1));
			sql = "select descr from sale_term where term_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsTermCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				descr = rs.getString("descr");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			valueXmlString.append("<descr>").append("<![CDATA[" + descr + "]]>").append("</descr>");
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcCrType(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String lsCrType = "";
		try {
			lsCrType = checkNull(genericUtility.getColumnValue("cr_type", dom));
			if (lsCrType.equalsIgnoreCase("03")) {
				valueXmlString.append("<rel_agnst protect = \"1\">").append("<![CDATA[" + "02" + "]]>")
				.append("</rel_agnst>");
				valueXmlString.append("<rel_after protect = \"0\">").append("<![CDATA[]]>").append("</rel_after>");
			} else {
				valueXmlString.append("<rel_agnst protect = \"0\">").append("<![CDATA[]]>").append("</rel_agnst>");
			}
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; // Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcNature(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String mCode = "", reStr = "";
		int pos = 0;
		try {
			mCode = checkNull(genericUtility.getColumnValue("nature", dom));
			System.out.println(" value of mCode rate " + mCode + "]");

			if ("F".equalsIgnoreCase(mCode) || "B".equalsIgnoreCase(mCode) || "S".equalsIgnoreCase(mCode)) {
				valueXmlString.append("<rate>").append("<![CDATA[" + '0' + "]]>").append("</rate>");
				setNodeValue(dom, "rate", getAbsString("0"));
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail2>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail2>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);
			}
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcCustItemRef(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "", lsCustItemCodeRef = "", lsCustCode = "", lsItemCode = "", lsCustItemCodeDescr = "",
				lsItemDescr = "";
		try {
			lsCustItemCodeRef = checkNull(genericUtility.getColumnValue("cust_item__ref", dom1));
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));

			if (lsCustItemCodeRef != null && lsCustItemCodeRef.trim().length() > 0) {
				try {
					sql = "select item_code, descr from customeritem where cust_code= ? and item_code__ref =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCustCode);
					pstmt.setString(2, lsCustItemCodeRef);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsItemCode = rs.getString("item_code");
						lsCustItemCodeDescr = rs.getString("descr");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;

					valueXmlString.append("<item_code__ord>").append("<![CDATA[" + lsItemCode + "]]>")
					.append("</item_code__ord>");

					sql = "select descr from item where item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsItemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsItemDescr = rs.getString("descr");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					valueXmlString.append("<item_descr>").append("<![CDATA[" + lsItemDescr + "]]>")
					.append("</item_descr>");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			valueXmlString.append("<cust_item_ref_descr>").append("<![CDATA[" + lsCustItemCodeDescr + "]]>")
			.append("</cust_item_ref_descr>");
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer gbfIcSiteCode(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String orderType = "";
		String custTaxOpt = "";	
		String custCodeBill= "";
		String custCodeTax = "";
		String stanCodeTo = "";
		String sql = "", lsSiteCodeDet = "", lsCustCode = "", lsItemCode = "", lsItemSer = "", lsStation = "",
				lsCustCodeDlv = "", frStation = "", lsTaxChapHdr = "", lsTaxClassHdr = "";
		String lsTaxEnvHdr = "", lsTaxChap = "", lsTaxClass = "", lsTaxEnv = "";
		try {
			lsSiteCodeDet = checkNull(genericUtility.getColumnValue("site_code", dom));
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
			lsItemCode = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			lsItemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
			lsStation = checkNull(genericUtility.getColumnValue("stan_code", dom1));
			lsCustCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom1));

			//added by Pavan Rane on 02aug18 [to set taxes based on a cust_tax_opt parameter in order type]
			System.out.println("----------Inside gbfIcSitecode()---Start--------");
			custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom1));
			orderType = checkNull(genericUtility.getColumnValue("order_type",dom1));

			System.out.println("siteCode["+lsSiteCodeDet+"]lsCustCode["+lsCustCode+"]custCodeBill["+custCodeBill+"]lsCustCodeDlv["+lsCustCodeDlv+"]");		

			sql = "select cust_tax_opt from sordertype where order_type = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, orderType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				custTaxOpt = checkNull(rs.getString("cust_tax_opt"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;	

			System.out.println("orderType:["+orderType+"]  custTaxOpt["+custTaxOpt+"]");
			//for default customer as dlv_cust if null found
			if(custTaxOpt == null || custTaxOpt.trim().length() == 0 || "null".equals(custTaxOpt))
			{
				custTaxOpt = "0";
			}

			sql = "select stan_code from customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			if("1".equals(custTaxOpt))	{
				pstmt.setString(1, custCodeBill);
				custCodeTax = custCodeBill;
			}else if("0".equals(custTaxOpt)) {						
				pstmt.setString(1, lsCustCodeDlv);
				custCodeTax = lsCustCodeDlv;
			}										
			rs = pstmt.executeQuery();
			if (rs.next()) 	{						
				stanCodeTo = rs.getString("stan_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("stanCodeTo["+stanCodeTo+"]");			
			//added by Pavan Rane on 02aug18 end

			sql = "SELECT stan_code FROM site WHERE site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSiteCodeDet);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				frStation = rs.getString("stan_code");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			lsTaxChapHdr = checkNull(genericUtility.getColumnValue("tax_chap", dom1));
			lsTaxClassHdr = checkNull(genericUtility.getColumnValue("tax_class", dom1));
			lsTaxEnvHdr = checkNull(genericUtility.getColumnValue("tax_env", dom1));

			//Changed by Pavan Rane on 02aug18 [to set taxes based on a cust_tax_opt parameter in order type]
			//if (lsTaxChapHdr == null || lsTaxChapHdr.trim().length() > 0) {
			//lsTaxChap = distCommon.getTaxChap(lsItemCode, lsItemSer, "C", lsCustCode, lsSiteCodeDet, conn);
			lsTaxChap = distCommon.getTaxChap(lsItemCode, lsItemSer, "C", custCodeTax, lsSiteCodeDet, conn);
			//} else {
			//	lsTaxChap = lsTaxChapHdr;
			//}

			//if (lsTaxClassHdr == null || lsTaxClassHdr.trim().length() > 0) {
			//lsTaxClass = distCommon.getTaxClass("C", lsCustCodeDlv, lsItemCode, lsSiteCodeDet, conn);
			lsTaxClass = distCommon.getTaxClass("C", custCodeTax, lsItemCode, lsSiteCodeDet, conn);
			//	} else {
			//lsTaxClass = lsTaxClassHdr;
			//}
			//if (lsTaxEnvHdr == null || lsTaxEnvHdr.trim().length() > 0) {
			//lsTaxEnv = distCommon.getTaxEnv(frStation, lsStation, lsTaxChap, lsTaxClass, lsSiteCodeDet, conn);
			lsTaxEnv = distCommon.getTaxEnv(frStation, stanCodeTo, lsTaxChap, lsTaxClass, lsSiteCodeDet, conn);
			//} else {
			//	lsTaxEnv = lsTaxEnvHdr;
			//}
			System.out.println("12004----tax_chap["+lsTaxChap+"]tax_class["+lsTaxClass+"]tax_env["+lsTaxEnv+"]");
			//Changed by Pavan Rane on 02aug18 end
			valueXmlString.append("<tax_chap>").append("<![CDATA[" + lsTaxChap + "]]>").append("</tax_chap>");
			valueXmlString.append("<tax_class>").append("<![CDATA[" + lsTaxClass + "]]>").append("</tax_class>");
			valueXmlString.append("<tax_env>").append("<![CDATA[" + lsTaxEnv + "]]>").append("</tax_env>");

		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException sQL) {
			sQL.printStackTrace();
			throw new ITMException(sQL); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcPackCode(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "", lsPackCode = "", descr = "";
		try {
			lsPackCode = checkNull(genericUtility.getColumnValue("pack_code", dom1));
			sql = "select descr from packing where pack_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsPackCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				descr = rs.getString("descr");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			valueXmlString.append("<packing_descr>").append("<![CDATA[" + descr + "]]>").append("</packing_descr>");
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException sQL) {
			sQL.printStackTrace();
			throw new ITMException(sQL); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcConvRtuomStduom(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String lcConvRtuomStduom = "", mVal = "", mVal1 = "", mItem = "";
		double ldRate = 0.00, lcRateStduom = 0.00;
		try {
			lcConvRtuomStduom = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
			mVal = checkNull(genericUtility.getColumnValue("unit__rate", dom));
			mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
			mItem = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			ldRate = Double.parseDouble(checkDouble(genericUtility.getColumnValue("rate", dom)));
			lcRateStduom = distCommon.convQtyFactor(mVal1, mVal, mItem, ldRate, conn);
			valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lcRateStduom + "]]>")
			.append("</rate__stduom>");
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; // Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcUnitRate(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String mCode = "", mVal1 = "", mItem = "";
		double ldRate = 0.00, lcConvRtuomStduom = 0.00, lcRateStduom = 0.00;
		try {
			mCode = checkNull(genericUtility.getColumnValue("unit__rate", dom));
			mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
			mItem = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			ldRate = Double.parseDouble(checkDouble(genericUtility.getColumnValue("rate", dom)));
			lcRateStduom = distCommon.convQtyFactor(mVal1, mCode, mItem, ldRate, conn);



			if(mVal1.trim().equalsIgnoreCase(mCode.trim()))//if condition added by nandkumar gadkari on 22/07/19
			{
				valueXmlString.append("<conv__rtuom_stduom protect = \"1\">").append("<![CDATA[" + lcConvRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
			}
			else
			{
				valueXmlString.append("<conv__rtuom_stduom protect = \"0\">").append("<![CDATA[" + lcConvRtuomStduom + "]]>")
				.append("</conv__rtuom_stduom>");
			}

			valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lcRateStduom + "]]>")
			.append("</rate__stduom>");
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcRate(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double ldRate = 0.00, mNum3 = 0.00, lcConvRtuomStduom = 0.00, mQty = 0.00, lcPlistDisc = 0.00, lcRetDiff = 0.00,
				lcEscRate = 0.00, lcRateStduom = 0.00, mRateClg = 0.00;
		double lcComm = 0.00, lcSpRate = 0.00, lcRateDiff = 0.00, lcOrdValue = 0.00;
		boolean lsDiscFlag = false;
		String sql = "", mVal = "", mVal1 = "", mItem = "", lsUnit = "", lsContractNo = "", lsPriceList = "",
				itemCode = "", lsDiscPricelist = "", lsOrdType = "", lsUdfStr1 = "";
		String lsUdfStr2 = "", mPriceListClg = "", lsListType = "", lsSalesPer = "", lsSpPlist = "", lsSalesPer1 = "",
				lsSalesPer2 = "";
		String mOrdDateStr = "", ldtPldateStr = "", ldtDateStr = "";
		Timestamp ldtDate = null, ldPlistDate = null;
		// ArrayList lcRateStduom=new ArrayList();

		try {
			ldRate = Double.parseDouble(checkDouble(genericUtility.getColumnValue("rate", dom)));
			System.out.println("Rate is:" + ldRate);
			// Timestamp mOrdDate =
			// Timestamp.valueOf(genericUtility.getColumnValue("order_date", dom1));
			mOrdDateStr = genericUtility.getColumnValue("order_date", dom1);
			mVal = checkNull(genericUtility.getColumnValue("unit__rate", dom));
			mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
			mItem = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			lcConvRtuomStduom = Double
					.parseDouble(checkDouble(genericUtility.getColumnValue("conv__rtuom_stduom", dom)));

			mNum3 = lcConvRtuomStduom;

			lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));
			mQty = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
			System.out.println("mQty>>>>>" + mQty);
			lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
			if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
			{
				lsContractNo = null;
			}
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
			/*
			 * if(genericUtility.getColumnValue("pl_date", dom1) !=null &&
			 * genericUtility.getColumnValue("pl_date", dom1).trim().length()>0) { Timestamp
			 * ldtPldate = Timestamp.valueOf(genericUtility.getColumnValue("pl_date",
			 * dom1)); }
			 */
			ldtPldateStr = genericUtility.getColumnValue("pl_date", dom1);

			/*
			 * if (ldtPldate == null) { ldtDate = ldtPldate; } else { ldtDate = mOrdDate; }
			 */
			if (ldtPldateStr == null || ldtPldateStr.trim().length() == 0) {
				ldtDateStr = ldtPldateStr;
			} else {
				ldtDateStr = mOrdDateStr;
			}
			lsPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
			String discPriceList = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
			System.out.println("lsPriceList rate" + lsPriceList + " discPriceList " + discPriceList);			
			if (lsPriceList == null || lsPriceList.trim().length() == 0) {
				lcPlistDisc = getDiscount(itemCode, lsUnit, mQty, dom, dom1, dom2, conn);
				//Changed by PavanR 04oct18[to protect discount if discount auto-populated]				
				if(discPriceList != null && discPriceList.trim().length() > 0) 
				{
					valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + lcPlistDisc + "]]>").append("</discount>");
				}else{
					valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + lcPlistDisc + "]]>").append("</discount>");
				}
				System.out.println("lcPlistDisc2:::::::::" + lcPlistDisc);
			}
			if ("M".equalsIgnoreCase(getPriceList(itemCode, lsUnit, lsDiscPricelist, ldPlistDate, conn))) {
				ldRate = 0.00;
				valueXmlString.append("<rate>").append("<![CDATA[" + ldRate + "]]>").append("</rate>");
				//////// Nandkumar Gadkari-----start COMMENTED
				//Changed by PavanR 04oct18[to protect discount if discount auto-populated]
				if(discPriceList != null && discPriceList.trim().length() > 0)	
				{
					valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + '0' + "]]>").append("</discount>");
				}else{
					valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + '0' + "]]>").append("</discount>");
				}
				//////// Nandkumar Gadkari-----end
			}
			// changes by mayur on 25/10/17
			if (mVal.trim().length() == 0) {
				sql = "Select unit from item where item_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mItem);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mVal = rs.getString("unit");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				lcRateStduom = distCommon.convQtyFactor(mVal1, mVal, itemCode, ldRate, conn);
				valueXmlString.append("<unit__rate>").append("<![CDATA[" + mVal + "]]>").append("</unit__rate>");
			} else {
				// lcRateStduom = distCommon.convQtyFactor(mVal1, mVal, mItem, ldRate, conn);
				// changes by mayur on 26/10/17 ---start
				ArrayList ratestduomArr = null;
				ratestduomArr = distCommon.getConvQuantityFact(mVal1, mVal, mItem, ldRate, lcConvRtuomStduom, conn);

				lcConvRtuomStduom = Double.parseDouble(ratestduomArr.get(0).toString());
				valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + lcConvRtuomStduom + "]]>")
				.append("</conv__rtuom_stduom>");
				// changes by mayur on 26/10/17 ---end
				if(mVal1.trim().equalsIgnoreCase(mVal.trim()))//if condition added by nandkumar gadkari on 22/07/19
				{
					valueXmlString.append("<conv__rtuom_stduom protect = \"1\">").append("<![CDATA[" + lcConvRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
				}
			}
			if (mNum3 == 0) {
				if (mVal.trim().equalsIgnoreCase(mVal1.trim())) {
					valueXmlString.append("<conv__rtuom_stduom protect = \"1\">").append("1").append("</conv__rtuom_stduom>"); //column protected by nandkumar gadkari on 22/07/19
				} else {
					valueXmlString.append("<conv__rtuom_stduom protect = \"0\">").append("<![CDATA[" + lcConvRtuomStduom + "]]>")
					.append("</conv__rtuom_stduom>");
				}
			}
			valueXmlString.append("<rate__stduom>").append("<![CDATA[" + lcRateStduom + "]]>")
			.append("</rate__stduom>");

			lsOrdType = checkNull(genericUtility.getColumnValue("order_type", dom1));

			sql = "select (case when udf_str1 is null then ''  else udf_str1 end) as udf_str1,(case when udf_str2 is null then '' else udf_str2 end) as udf_str2"
					+ " from gencodes where fld_name = 'ORDER_TYPE' and  mod_name ='W_SORDER' and  fld_value =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsOrdType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsUdfStr1 = rs.getString("udf_str1");
				lsUdfStr2 = rs.getString("udf_str2");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("lsUdfStr1" + lsUdfStr1);
			System.out.println("lsUdfStr2" + lsUdfStr2);
			if ((lsUdfStr1 != null && lsUdfStr1.trim().length() > 0)
					&& Double.parseDouble(checkDouble(lsUdfStr1)) > 0) {
				lcRetDiff = (100 - Double.parseDouble(checkDouble(lsUdfStr1))) / 100;
				lcEscRate = lcRateStduom * lcRetDiff;
				System.out.println("lcRateStduom" + lcRateStduom);
				valueXmlString.append("<rate__clg>").append("<![CDATA[" + lcEscRate + "]]>").append("</rate__clg>");
			} else {
				mPriceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom1));
				Double quantity = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
				System.out.print("Quantity3++++++++" + quantity);
				System.out.println("mPriceListClg rate " + lsListType);
				if (mPriceListClg != null && mPriceListClg.trim().length() > 0) {
					mRateClg = distCommon.pickRate(mPriceListClg, ldtDateStr, mItem, "", "L", quantity, conn);
					System.out.print("mRateClg gbfICRATE++++++++" + mRateClg);
				}
				lsListType = distCommon.getPriceListType(mPriceListClg, conn);
				System.out.println("lsListType" + lsListType);
				if ("B".equalsIgnoreCase(lsListType) && mRateClg == -1) {
					mRateClg = 0;
				}
				System.out.println("mRateClg" + mRateClg);
				if (mRateClg <= 0) {
					if (mPriceListClg != null && mPriceListClg.trim().length() > 0) {
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>")
						.append("</rate__clg>");
					} else {
						System.out.println("ldRate else" + ldRate);
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + ldRate + "]]>")
						.append("</rate__clg>");
					}
				} else {
					valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>").append("</rate__clg>");
				}
			}
			lcComm = Double.parseDouble(genericUtility.getColumnValue("comm_perc", dom1) == null ? "0.00"
					: genericUtility.getColumnValue("comm_perc", dom1));
			if (lcComm != 0) {
				lsSalesPer = checkNull(genericUtility.getColumnValue("sales_pers", dom1));
				sql = "select price_list from sales_pers where sales_pers =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsSalesPer);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsSpPlist = checkNull(rs.getString("price_list"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((lsSpPlist != null && lsSpPlist.trim().length() > 0) && mQty > 0) {
					lcSpRate = distCommon.pickRate(lsSpPlist, ldtDateStr, mItem, "", "L", conn);
					lcRateDiff = ldRate - lcSpRate;
					if (lcRateDiff > 0) {
						valueXmlString.append("<comm_perc_1>").append("<![CDATA[" + lcRateDiff + "]]>")
						.append("</comm_perc_1>");
						valueXmlString.append("<comm_perc_on_1>").append("<![CDATA[" + 'Q' + "]]>")
						.append("</comm_perc_on_1>");
					}
				}
			}

			lcComm = Double.parseDouble(checkDouble(genericUtility.getColumnValue("comm_perc_1", dom1)));
			if (lcComm != 0) {
				lsSalesPer1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom1));
				sql = "select price_list from sales_pers where sales_pers =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsSalesPer1);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsSpPlist = checkNull(rs.getString("price_list"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((lsSpPlist != null && lsSpPlist.trim().length() > 0) && mQty > 0) {
					lcSpRate = distCommon.pickRate(lsSpPlist, ldtDateStr, mItem, "", "L", conn);
					lcRateDiff = ldRate - lcSpRate;
					if (lcRateDiff > 0) {
						valueXmlString.append("<comm_perc_2>").append("<![CDATA[" + lcRateDiff + "]]>")
						.append("</comm_perc_2>");
						valueXmlString.append("<comm_perc_on_2>").append("<![CDATA[" + 'Q' + "]]>")
						.append("</comm_perc_on_2>");
					}
				}
			}

			lcComm = Double.parseDouble(checkDouble(genericUtility.getColumnValue("comm_perc_2", dom1)));
			if (lcComm != 0) {
				lsSalesPer2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom1));
				sql = "select price_list from sales_pers where sales_pers =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsSalesPer2);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsSpPlist = checkNull(rs.getString("price_list"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((lsSpPlist != null && lsSpPlist.trim().length() > 0) && mQty > 0) {
					lcSpRate = distCommon.pickRate(lsSpPlist, ldtDateStr, mItem, "", "L", conn);
					lcRateDiff = ldRate - lcSpRate;
					if (lcRateDiff > 0) {
						valueXmlString.append("<comm_perc_3>").append("<![CDATA[" + lcRateDiff + "]]>")
						.append("</comm_perc_3>");
						valueXmlString.append("<comm_perc_on_3>").append("<![CDATA[" + 'Q' + "]]>")
						.append("</comm_perc_on_3>");
					}
				}
			}
			if (ldRate > 0) {
				lcOrdValue = mQty * ldRate;
				valueXmlString.append("<ord_value>").append("<![CDATA[" + lcOrdValue + "]]>").append("</ord_value>");
			}
			valueXmlString.append("<amount>").append("<![CDATA[" + mQty * ldRate + "]]>").append("</amount>");

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 06/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcconvQtyStduom(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		double mNum = 0.00, mNum1 = 0.00, mNum2 = 0.00;
		String mVal = "", mVal1 = "", lsItemCodeOrd = "";
		// ArrayList mNum2=new ArrayList();
		try {
			mNum = Double.parseDouble(checkDouble(genericUtility.getColumnValue("conv__qty_stduom", dom)));
			mVal = checkNull(genericUtility.getColumnValue("unit", dom));
			mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
			lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			mNum1 = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
			System.out.println("mNum[" + mNum + "] mVal[" + mVal + "] mVal1[" + mVal1 + "] lsItemCodeOrd["
					+ lsItemCodeOrd + "] mNum1[" + mNum1 + "]");
			mNum2 = distCommon.convQtyFactor(mVal, mVal1, lsItemCodeOrd, mNum1, conn);
			System.out.println("mNum2[" + mNum2 + "]");
			valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + mNum2 + "]]>")
			.append("</quantity__stduom>");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 06/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	private StringBuffer gbfIcUnit(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", lsUnit = "", lsDescr = "", mVal1 = "", lsItemCodeOrd = "", lsPlistDis = "", lsCustCode = "",
				lsSiteCode = "", lsItemCode = "";
		String itemCode = "", lsDiscPricelist = "", mqtyStr = "";
		double mNum1 = 0.00, mNum2 = 0.00, ldRate = 0.00, idRateWtDiscount = 0.00, mqty = 0.0, lcPlistDisc = 0.0;
		ArrayList mNum = new ArrayList();
		Timestamp ldPlistDate = null;

		try {
			lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));

			sql = "select descr from uom where unit = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsUnit);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsDescr = rs.getString("descr");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (lsDescr != null && lsDescr.trim().length() > 0) {
				valueXmlString.append("<uom_descr>").append("<![CDATA[" + lsDescr + "]]>").append("</uom_descr>");
			}

			mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
			lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			mNum1 = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
			mNum2 = Double.parseDouble(checkDouble(genericUtility.getColumnValue("conv__qty_stduom", dom)));
			if (lsUnit.trim().equalsIgnoreCase(mVal1.trim())) {
				valueXmlString.append("<conv__qty_stduom protect = \"1\">").append("1").append("</conv__qty_stduom>");// column protected by nandkumar gadkari on 22/07/19
			} else {
				valueXmlString.append("<conv__qty_stduom protect = \"0\">").append("<![CDATA[" + mNum2 + "]]>")
				.append("</conv__qty_stduom>");
			}
			mNum = distCommon.getConvQuantityFact(lsUnit, mVal1, lsItemCodeOrd, mNum1, mNum2, conn);

			if (mNum.size() > 0) {
				mqtyStr = (String) mNum.get(1);
				mqty = Double.parseDouble(checkDouble(mqtyStr));

				// changes by mayur on 25/10/17 start
				mNum2 = Double.parseDouble(checkDouble(mNum.get(0).toString()));
				System.out.println("mNum2 @@@@ get 0" + mNum2);
				// changes by mayur on 25/10/17 end
			}
			valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + mqty + "]]>")
			.append("</quantity__stduom>");

			// changes by mayur on 25/10/17
			if (lsUnit.trim().equalsIgnoreCase(mVal1.trim())) {// if condition to protect column added by nandkumar gadkari on 22/07/19
				valueXmlString.append("<conv__qty_stduom protect = \"1\">").append("<![CDATA[" + mNum2 + "]]>").append("</conv__qty_stduom>");
			} else {
				valueXmlString.append("<conv__qty_stduom protect = \"0\">").append("<![CDATA[" + mNum2 + "]]>")
				.append("</conv__qty_stduom>");
			}
			lsPlistDis = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
			Timestamp orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
			lsItemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
			itemCode = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			if ("M".equalsIgnoreCase(getPriceList(itemCode, lsUnit, lsPlistDis, ldPlistDate, conn))) {
				ldRate = idRateWtDiscount; // Mining less
				// gbf_calc_rate(lc_plist_disc, ld_rate); // Mining less
				calRate(checkDoubleNull(lsPlistDis), ldRate);
				valueXmlString.append("<rate>").append("<![CDATA[" + ldRate + "]]>").append("</rate>");
				//////// Nandkumar Gadkari-----start COMMENTED
				//Changed by PavanR 04oct18[to protect discount if discount auto-populated]
				if (lsPlistDis != null && lsPlistDis.trim().length() > 0) {
					valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + "0" + "]]>").append("</discount>");
				}else{
					valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + "0" + "]]>").append("</discount>");
				}
				//valueXmlString.append("<discount>").append("<![CDATA[" + "0" + "]]>").append("</discount>");
				//////// Nandkumar Gadkari-----end
			}

		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException sQl) {
			sQl.printStackTrace();
			throw new ITMException(sQl); //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		return valueXmlString;
	}

	private StringBuffer gbfIcQuantity(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException {
		double mQty = 0.00, ldConvQtyStduom = 0.00, mRate = 0.00, mNum = 0.00, ldRate = 0.00, idRateWtDiscount = 0.00,
				acShipperQty = 0.00, acIntegralQty = 0.00, lcRate = 0.00;
		double lcOrdValue = 0.00, lcShipperQty = 0.00, lcIntQty = 0.00, lcBalQty = 0.00, lcLooseQty = 0.00,
				lcIntegralQty = 0.00, lcQty1 = 0.00, lcQtyFc = 0.00;
		String sql = "", mVal = "", mVal1 = "", lsNature = "", lsPriceListParent = "", lsUnitSal = "", lsPriceList = "",
				lsItemCodeOrd = "", lsContractNo = "", lsListType = "";
		String lsPlistDisc = "", lsPlistDiscount = "", lsCustCode = "", mSiteCode = "", lsPackCode = "", mStateCd = "",
				lsOrderType = "", lsCountCodeDlv = "", itemStru = "", lstype = "";
		String lsCurscheme = "", lsItemCodeParent = "", lsApplyCustList = "", lsNoapplyCustList = "",
				lsApplicableOrdTypes = "", lsSchemeCode = "", lsPrevscheme = "";
		String lsCustSchemeCode = "", lsItemStru = "", lsDisPobOrdTypeList = "", lsSchemeEdit = "", lsUnit = "",
				lsRefNo = "", lsSiteCodeShip = "", mSlabOn = "", lsDescr = "";
		String lsSalesOrd = "", lsQuotNo = "", reStr = "", ldtDateStr = "";
		int llNoOfArt = 0, cnt = 0, cnt1 = 0, llPlcount = 0, llNoOfArt1 = 0, llNoOfArt2 = 0, pos = 0, schecnt = 0,
				cntoffer = 0;
		Timestamp ldPlistDate = null, mTranDate = null;
		Timestamp mOrderDate = null, ldtPlDate = null;
		boolean lsDiscFalg = false, lbProceed = false, lbOrdFlag = false;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		double amount=0.0;//variable declared by nandkuamr gadkari on 26/02/19
		try {
			mQty = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
			mVal = checkNull(genericUtility.getColumnValue("unit", dom));
			mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
			if (genericUtility.getColumnValue("order_date", dom1) != null
					&& genericUtility.getColumnValue("order_date", dom1).trim().length() > 0) {
				mOrderDate = Timestamp
						.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if (genericUtility.getColumnValue("pl_date", dom1) != null
					&& genericUtility.getColumnValue("pl_date", dom1).trim().length() > 0) {
				ldtPlDate = Timestamp
						.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			}

			if (ldtPlDate != null) {
				// ldtDate = ldtPlDate;
				ldtDateStr = genericUtility.getColumnValue("order_date", dom1);

			} else {
				// ldtDate = mOrderDate;
				ldtDateStr = genericUtility.getColumnValue("pl_date", dom1);
			}
			lsPriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
			lsItemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
			ldConvQtyStduom = Double.parseDouble(checkDouble(genericUtility.getColumnValue("conv__qty_stduom", dom)));
			lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom1));
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
			if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
			{
				lsContractNo = null;
			}
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
			lsNature = checkNull(genericUtility.getColumnValue("nature", dom));

			if (lsContractNo == null || lsContractNo.trim().length() == 0) {
				if ("F".equalsIgnoreCase(lsNature) || "B".equalsIgnoreCase(lsNature)
						|| "S".equalsIgnoreCase(lsNature)) {
					mRate = 0;
				} else {
					ldPlistDate = mOrderDate;

					if (lsPriceList != null || lsPriceList.trim().length() > 0) {
						mRate = distCommon.pickRate(lsPriceList, ldtDateStr, lsItemCodeOrd, "", "L", mQty, conn);
						System.out.print("mRate gbfICquantity++++++++" + mRate);
						System.out.print("mqty++++++++" + mQty);
					}
					lsListType = distCommon.getPriceListType(lsPriceList, conn);

					if (lsListType == null || lsListType.trim().length() == 0) {
						sql = "select price_list__parent  from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsPriceList);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceListParent = rs.getString("price_list__parent");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceListParent != null || lsPriceListParent.trim().length() > 0) {
							lsListType = distCommon.getPriceListType(lsPriceListParent, conn);
						}
					}
					if ("B".equalsIgnoreCase(lsListType) && mRate < 0) {
						mRate = 0;
					}
				}
				valueXmlString.append("<rate>").append("<![CDATA[" + mRate + "]]>").append("</rate>");
				setNodeValue(dom, "rate", getAbsString(String.valueOf(mRate)));
			}
			if (mVal.trim().length() == 0) {
				sql = "select unit__sal, unit from item where item_code =  ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsItemCodeOrd);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsUnitSal = rs.getString("unit__sal");
					mVal = rs.getString("unit");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (lsUnitSal == null) {
					lsUnitSal = mVal;
				}
				mNum = distCommon.convQtyFactor(lsUnitSal, mVal1, lsItemCodeOrd, mQty, conn);
				valueXmlString.append("<unit>").append("<![CDATA[" + lsUnitSal + "]]>").append("</unit>");
				setNodeValue(dom, "unit", getAbsString(lsUnitSal));
			} else {
				mNum = distCommon.convQtyFactor(mVal, mVal1, lsItemCodeOrd, mQty, conn);
			}
			if (ldConvQtyStduom == 0) {
				valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + ldConvQtyStduom + "]]>")
				.append("</conv__qty_stduom>");
				setNodeValue(dom, "conv__qty_stduom", getAbsString(String.valueOf(ldConvQtyStduom)));
			}
			valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + mNum + "]]>")
			.append("</quantity__stduom>");
			setNodeValue(dom, "quantity__stduom", getAbsString(String.valueOf(mNum)));
			lsPlistDiscount = genericUtility.getColumnValue("price_list__disc", dom1);
			System.out.println("lsPlistDiscount" + lsPlistDiscount);
			/// Nandkumar Gadkari-----------start-----
			if (lsContractNo == null || lsContractNo.trim().length() == 0)

			{
				lsPlistDisc = String.valueOf(getDiscount(lsItemCodeOrd, mVal, mQty, dom, dom1, dom2, conn));
				//Changed by PavanRane 04oct18[to protect discount if discount auto-populated]
				lsPlistDisc = String.valueOf(getDiscount(lsItemCodeOrd, mVal, mQty, dom, dom1, dom2, conn));
				if(lsPlistDiscount != null && lsPlistDiscount.trim().length() > 0)
				{
					valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + lsPlistDisc + "]]>").append("</discount>");
				}else{
					valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + lsPlistDisc + "]]>").append("</discount>");
				}
				setNodeValue(dom, "discount", getAbsString(String.valueOf(lsPlistDisc)));
				System.out.println("lsPlistDisc1::::::::::::" + lsPlistDisc);
			}
			// ///Nandkumar Gadkari-----------end-----

			//lsPlistDiscount = genericUtility.getColumnValue("price_list__disc", dom1);
			//System.out.println("lsPlistDiscount" + lsPlistDiscount);
			if ("M".equalsIgnoreCase(distCommon.getPriceListType(lsPlistDisc, conn))) {
				ldRate = idRateWtDiscount;
				lsDiscFalg = false;
				valueXmlString.append("<rate>").append("<![CDATA[" + ldRate + "]]>").append("</rate>");
				setNodeValue(dom, "rate", getAbsString(String.valueOf(ldRate)));
				//////// Nandkumar Gadkari-----start COMMENTED
				if(lsPlistDiscount != null && lsPlistDiscount.trim().length() > 0)
				{
					valueXmlString.append("<discount protect = \"1\">").append("<![CDATA[" + '0' + "]]>").append("</discount>");
				}else{
					valueXmlString.append("<discount protect = \"0\">").append("<![CDATA[" + '0' + "]]>").append("</discount>");
				}
				setNodeValue(dom, "discount", getAbsString("0"));
				//////// Nandkumar Gadkari-----end
			}
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
			mSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
			lsPackCode = checkNull(genericUtility.getColumnValue("pack_code", dom1));
			llNoOfArt = distCommon.getNoArt(mSiteCode, lsCustCode, lsItemCodeOrd, lsPackCode, mNum, 'I', acShipperQty,
					acIntegralQty, conn);

			mStateCd = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
			lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
			if (checkNull(genericUtility.getColumnValue("due_date", dom1)).trim().length() > 0) {
				mTranDate = Timestamp.valueOf(
						genericUtility.getValidDateString(checkNull(genericUtility.getColumnValue("due_date", dom1)),
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			// mTranDate =
			// Timestamp.valueOf(checkNull(genericUtility.getColumnValue("due_date",
			// dom1)));
			lsCountCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));

			sql = "select bom_code,item_stru from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsItemCodeOrd);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsSchemeCode = checkNull(rs.getString("bom_code"));
				lstype = checkNull(rs.getString("item_stru"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (!"C".equalsIgnoreCase(lstype)) {
				sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det b"
						+ " where a.scheme_code= b.scheme_code and (a.item_code= ? or prod_sch='Y')" // chnages by
						// 16-12-17
						+ " and a.app_from<= ? and a.valid_upto>= ? "
						+ " and (b.site_code= ? or b.state_code= ? or b.count_code =? )";
				// or (a.item_code in (select (case when product_code is null then ' ' else
				// product_code end ) from item where item_code = ? )))"
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, lsItemCodeOrd);
				// pstmt1.setString(2, lsItemCodeOrd);
				pstmt1.setTimestamp(2, mTranDate);
				pstmt1.setTimestamp(3, mTranDate);
				pstmt1.setString(4, mSiteCode);
				pstmt1.setString(5, mStateCd);
				pstmt1.setString(6, lsCountCodeDlv);
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {

					lsCurscheme = rs1.getString("scheme_code");

					if (lsCurscheme != null || lsCurscheme.trim().length() == 0) {
						sql = "select item_code__parent from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsItemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsItemCodeParent = rs.getString("item_code__parent");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsItemCodeParent == null || lsItemCodeParent.trim().length() == 0) {
							sql = "select count(1) as cnt from item where item_code__parent =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (cnt > 0) {
							break;
						}
					}
					String prodSh = "N";
					String sql22 = "SELECT CASE WHEN PROD_SCH IS NULL THEN 'N' ELSE PROD_SCH END AS PROD_SCH FROM SCHEME_APPLICABILITY WHERE SCHEME_CODE = ? ";
					pstmt = conn.prepareStatement(sql22);
					pstmt.setString(1, lsCurscheme);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						prodSh = rs.getString(1);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if("Y".equalsIgnoreCase(prodSh) && (lsSchemeCode!=null && lsSchemeCode.trim().length()>0))// removed || condition by nandkumar gadkari on 21/09/19
					{
						sql = "select count (*) as cnt from sch_pur_items  where SCHEME_CODE =? and item_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCurscheme);
						pstmt.setString(2, lsItemCodeOrd);
						rs = pstmt.executeQuery();

						if (rs.next()) 
						{
							cnt1 = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt1==0)
						{

							sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsCurscheme);
							pstmt.setString(2, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt1 = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt1==0)
							{
								continue;
							}

						}
					}




					if ("N".equalsIgnoreCase(prodSh)) {
						sql = "Select count(1) as cnt From scheme_applicability A, bom b Where A.scheme_code = b.bom_code And B.bom_code= ?"
								+ " And(? between case when b.min_qty is null then 0 else b.min_qty end"
								+ " And case when b.max_qty is null then 0 else b.max_qty end) and B.promo_term is null";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsCurscheme);
						pstmt.setDouble(2, mQty);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0)
							continue;
					}
					/*
					 * if (cnt == 0) { // Goto Nextrec }
					 */
					sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as ls_apply_cust_list,"
							+ " (case when noapply_cust_list is null then ' ' else noapply_cust_list end) as ls_noapply_cust_list, order_type"
							+ " from scheme_applicability where scheme_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCurscheme);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsApplyCustList = rs.getString("ls_apply_cust_list");
						lsNoapplyCustList = rs.getString("ls_noapply_cust_list");
						lsApplicableOrdTypes = rs.getString("order_type");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if ("NE".equalsIgnoreCase(lsOrderType)
							&& (lsApplicableOrdTypes == null || lsApplicableOrdTypes.trim().length() == 0)) {
						// goto Nextrec
					} else if (lsApplicableOrdTypes != null && lsApplicableOrdTypes.trim().length() > 0) {
						System.out.println("lsApplicableOrdTypes1" + lsApplicableOrdTypes + ":::");
						lbProceed = false;
						String lsApplicableOrdTypesArr[] = lsApplicableOrdTypes.split(",");
						ArrayList<String> applicableOrdTypes = new ArrayList<String>(
								Arrays.asList(lsApplicableOrdTypesArr));
						if (applicableOrdTypes.contains(lsOrderType)) {
							System.out.println("lbProceed" + lbProceed);
							lbProceed = true;
							// break;
						}
						if (!lbProceed) {
							System.out.println("Inside lbproceed");
							continue;
							// goto Nextrec
						}
					}
					lsPrevscheme = lsSchemeCode;
					lsSchemeCode = lsCurscheme;

					if (lsApplyCustList.trim().length() > 0) {
						lsSchemeCode = null;
						System.out.println("lsSchemeCode:::::::1" + lsSchemeCode);
						lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						System.out.println("lsCustCode" + lsCustCode);
						String lsApplyCustListArr[] = lsApplyCustList.split(",");
						ArrayList<String> applyCustList = new ArrayList<String>(Arrays.asList(lsApplyCustListArr));
						if (applyCustList.contains(lsCustCode.trim())) {
							System.out.println("Inside applycustList");
							lsSchemeCode = lsCurscheme;
							lsCustSchemeCode = lsCurscheme;
							System.out.println("lsSchemeCode::" + lsSchemeCode + " lsCustSchemeCode::" + lsCurscheme);

							// break;
						}
					}
					if (lsNoapplyCustList.trim().length() > 0 && lsSchemeCode != null) {
						lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						String lsNoapplyCustListArr[] = lsNoapplyCustList.split(",");
						ArrayList<String> noapplyCustList = new ArrayList<String>(Arrays.asList(lsNoapplyCustListArr));
						if (noapplyCustList.contains(lsCustCode)) {
							lsSchemeCode = "";
							break;
						}
					}


					cnt = 0;
					if (lsSchemeCode != null) {
						schecnt++;
					} else if (schecnt == 1) {
						lsSchemeCode = lsPrevscheme;
						System.out.println("lsSchemeCode:::::::2" + lsSchemeCode);
					}
					// Nextrec:
					// fetch next curscheme into :ls_curscheme;
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				if (schecnt > 1) {
					lsSchemeCode = "";
				} else if (lsCustSchemeCode.trim().length() > 0) {
					System.out.println("lsSchemeCode:::::::3" + lsSchemeCode);
					lsSchemeCode = lsCustSchemeCode;
				}
			} else {
				valueXmlString.append("<item_code>").append("<![CDATA[" + lsSchemeCode + "]]>").append("</item_code>");
				setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
			}
			sql = "select batch_qty from bom where bom_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSchemeCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lcIntegralQty = rs.getInt("batch_qty");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (lcIntegralQty > 0) {
				if (mQty < lcIntegralQty) {
					System.out.println("lsSchemeCode:::::::4" + lsSchemeCode);
					lsSchemeCode = "";
				}
			}
			/////////////// 09-12-17 start arun for
			System.out.println("lsSchemeCode before " + lsSchemeCode + "]");
			//Modified by Anjali R. on[15/01/2018][To check lsSchemeCode null][Start]
			//if (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)// arun
			if (lsSchemeCode != null && (lsSchemeCode != null && lsSchemeCode.trim().length() > 0))
				//Modified by Anjali R. on[15/01/2018][To check lsSchemeCode null][End]
			{
				System.out.println("lsSchemeCode after inside 121 " + lsSchemeCode + "]");
				sql = "SELECT COUNT(*) AS CNT FROM SCHEME_APPLICABILITY WHERE SCHEME_CODE = ?  AND PROD_SCH='Y' ";
				// "AND ((ITEM_CODE IN (SELECT (CASE WHEN PRODUCT_CODE IS NULL THEN ' ' ELSE
				// PRODUCT_CODE END ) FROM ITEM WHERE ITEM_CODE = ? )) or (ITEM_CODE IN (SELECT
				// (CASE WHEN PRODUCT_CODE IS NULL THEN ' ' ELSE PRODUCT_CODE END ) FROM ITEM
				// WHERE ITEM_CODE = ? )) ) ";
				pstmt1 = conn.prepareStatement(sql);
				// System.out.println("lsItemCodeOrd in side
				// @@@@111"+lsItemCodeOrd+"]"+"lsSchemeCode in side @@@@2222"+lsSchemeCode+"]");
				pstmt1.setString(1, lsSchemeCode);
				// pstmt1.setString(2, lsItemCodeOrd);
				// System.out.println("lsItemCodeOrd in side @@@@"+sql+"]");
				rs1 = pstmt1.executeQuery();
				int countSch = 0;
				if (rs1.next()) {
					countSch = rs1.getInt("cnt");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("countSch@@@@@@@[" + countSch + "]");
				System.out.println("lsSchemeCode before " + lsSchemeCode + "]");
				if (countSch > 0) {

					System.out.println("lsCurscheme" + lsCurscheme + "]");
					/*
					 * if (rs.next()) {
					 */
					System.out.println(" in side while prod_sch@@@@@@@[" + sql + "]");
					System.out.println("lsCurscheme" + lsCurscheme + "]");

					// lsCurscheme = rs.getString("scheme_code");
					System.out.println("lsCurscheme" + lsCurscheme + "]");

					/*
					 * sql = "Select count(*) as cnt From	scheme_applicability A,bom b " +
					 * "Where A.scheme_code = b.bom_code And B.bom_code= ?"
					 * +" And (? between case when b.min_qty is null then 0 else b.min_qty end" +
					 * " And case when b.max_qty is null then 0 else b.max_qty end)" +
					 * " and B.promo_term is null" + " and prod_sch='Y'"; pstmt =
					 * conn.prepareStatement(sql); pstmt.setString(1, lsCurscheme);
					 * pstmt.setDouble(2, mQty); rs = pstmt.executeQuery(); if (rs.next()) { cnt =
					 * rs.getInt("cnt"); } rs.close(); rs = null; pstmt.close(); pstmt = null;
					 * System.out.println("before cnt1 "+cnt+"]");
					 */

					if (countSch > 0) {
						sql = "select count (*) as cnt from sch_pur_items  where SCHEME_CODE =? and item_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSchemeCode);
						pstmt.setString(2, lsItemCodeOrd);

						rs = pstmt.executeQuery();

						if (rs.next()) {
							cnt1 = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						////////
						// NANDKUMAR GADKARI START --------

						sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSchemeCode);
						pstmt.setString(2, lsItemCodeOrd);

						rs = pstmt.executeQuery();

						if (rs.next()) {
							cntoffer = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cntoffer > 0) {
							String schemeType = "";
							double discount = 0;
							sql = "select SCHEME_TYPE,DISCOUNT FROM SCH_GROUP_DEF where SCHEME_CODE=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsSchemeCode);
							rs = pstmt.executeQuery();

							if (rs.next()) {

								schemeType = rs.getString("SCHEME_TYPE");
								discount = rs.getDouble("DISCOUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (schemeType.equalsIgnoreCase("2")) {

								System.out.println(" schemeType::::::::::" + schemeType + discount + "]");
								valueXmlString.append("<discount protect = \"1\">")
								.append("<![CDATA[" + discount + "]]>").append("</discount>");
								setNodeValue(dom, "discount", getAbsString(String.valueOf(discount)));

							}
						}
						// --end--------------------------------------
						if (cnt1 == 0) {
							sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsSchemeCode);
							pstmt.setString(2, lsItemCodeOrd);

							rs = pstmt.executeQuery();

							if (rs.next()) {
								cnt1 = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						////////////
						if (cnt1 > 0) {

							//// nandkumar Start

							System.out.println(" after if cnt1 " + cnt + "]");
							/*valueXmlString.append("<sch_attr>").append("<![CDATA[" + 'Y' + "]]>").append("</sch_attr>");
							setNodeValue(dom, "sch_attr", getAbsString("Y"));*/// commented by nandkumar gadkari on 03/06/19
							String descr = "", schemeType = "";
							double discount = 0;
							// lsSchemeCode = checkNull(genericUtility.getColumnValue("item_code", dom));

							sql = "select DESCR,SCHEME_TYPE,DISCOUNT FROM SCH_GROUP_DEF where SCHEME_CODE=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsSchemeCode);
							rs = pstmt.executeQuery();
							descr = "";
							if (rs.next()) {
								descr = rs.getString("DESCR");
								schemeType = rs.getString("SCHEME_TYPE");
								/* discount = rs.getDouble("DISCOUNT"); */
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(!"3".equalsIgnoreCase(schemeType))// condintion added by nandkumar gadkari on 03/06/19
							{
								valueXmlString.append("<sch_attr>").append("<![CDATA[" + 'Y' + "]]>").append("</sch_attr>");
								setNodeValue(dom, "sch_attr", getAbsString("Y"));
								valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>")
								.append("</item_descr>");
								setNodeValue(dom, "item_descr", getAbsString(descr));
							}
							// valueXmlString.append("<item_code>").append("<![CDATA[" + lsSchemeCode +
							// "]]>").append("</item_code>");
							// setNodeValue( dom, "item_code", getAbsString(lsSchemeCode));
							System.out.println("descr SCH_GROUP_DEF" + descr + "]");

							/*
							 * if (schemeType.equalsIgnoreCase("2")) {
							 * 
							 * System.out.println(" schemeType::::::::::" + schemeType + discount + "]");
							 * valueXmlString.append("<discount protect = \"1\">") .append("<![CDATA[" +
							 * discount + "]]>").append("</discount>"); setNodeValue(dom, "discount",
							 * getAbsString(String.valueOf(discount)));
							 * 
							 * }
							 */
							// Nandkumar -------------------end --------------
							/*
							 * valueXmlString.append("<SCH_ATTR>").append("<![CDATA[" + "Y" +
							 * "]]>").append("</SCH_ATTR>"); setNodeValue( dom, "SCH_ATTR",
							 * getAbsString("Y"));
							 */

							lsSchemeCode = "";
							schecnt = 0;

						}

					} else {

					}

					// sql = "select (case when apply_cust_list is null then ' ' else
					// apply_cust_list end) as ls_apply_cust_list," + " (case when noapply_cust_list
					// is null then ' ' else noapply_cust_list end) as
					// ls_noapply_cust_list,order_type" + " from scheme_applicability where
					// scheme_code =?";
					/*
					 * pstmt = conn.prepareStatement(sql); pstmt.setString(1, lsCurscheme); rs =
					 * pstmt.executeQuery()
					 */

				}
				if (countSch > 0) {
					lsSchemeCode = "";
					schecnt = 0;

				}
			} else {
				valueXmlString.append("<sch_attr>").append("<![CDATA[" + 'N' + "]]>").append("</sch_attr>");
				setNodeValue(dom, "sch_attr", getAbsString("N"));
			}

			sql = "select (case when item_stru is null then 'S' else item_stru end) as ls_item_stru from item where item_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsItemCodeOrd);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsItemStru = rs.getString("ls_item_stru");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			lsOrderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
			//lsDisPobOrdTypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
			lsDisPobOrdTypeList = ""; //commentted and added by rupali on 02/04/2021
			lbOrdFlag = false;

			if (lsDisPobOrdTypeList != null && lsDisPobOrdTypeList.trim().length() > 0) 
			{
				String lsDisPobOrdTypeListArr[] = lsDisPobOrdTypeList.split(",");
				ArrayList<String> disPobOrdTypeList = new ArrayList<String>(Arrays.asList(lsDisPobOrdTypeListArr));
				//if (disPobOrdTypeList.contains(lsOrderType)) { 
				for( String pobOrdType : disPobOrdTypeList ) 
				{
					if(checkNull(pobOrdType).equalsIgnoreCase(lsOrderType))	{
						lbOrdFlag = true;
					}
				}
			}
			System.out.println("lbOrdFlag>?>>>[" + lbOrdFlag + "]lsItemStru>>>" + lsItemStru + "] lsSchemeCode["
					+ lsSchemeCode + "]");
			if (lbOrdFlag) {
				valueXmlString.append("<item_flg>").append("<![CDATA[" + 'I' + "]]>").append("</item_flg>");
				setNodeValue(dom, "item_flg", getAbsString("I"));
				System.out.println("lsSchemeCode:::::::5" + lsSchemeCode);
				valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsItemCodeOrd + "]]>")
				.append("</item_code>");
				setNodeValue(dom, "item_code", getAbsString(lsItemCodeOrd));
				System.out.println("lsItemCodeOrd>>>>" + lsItemCodeOrd + "objContext>>>>" + objContext);
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail2>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail2>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);
				System.out.println("INSIDE LOOOP>>>>" + valueXmlString.toString());

			}
			//Modified by Anjali R. on[15/01/2018][To check lsSchemeCode null][Start]
			//else if ("F".equalsIgnoreCase(lsItemStru) && lsSchemeCode.trim().length() > 0)
			else if ("F".equalsIgnoreCase(lsItemStru) && (lsSchemeCode != null && lsSchemeCode.trim().length() > 0))
				//Modified by Anjali R. on[15/01/2018][To check lsSchemeCode null][End]
			{
				sql = "select count(*) as cnt from scheme_applicability where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsItemCodeOrd);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					cnt = rs.getInt("cnt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (cnt > 1) {
					valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>").append("</item_flg>");
					setNodeValue(dom, "item_flg", getAbsString("B"));
					valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[]]>").append("</item_code>");

				} else {
					valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>").append("</item_flg>");
					setNodeValue(dom, "item_flg", getAbsString("B"));
					System.out.println("lsSchemeCode:::::::6" + lsSchemeCode);
					valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsSchemeCode + "]]>")
					.append("</item_code>");
					setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));

					reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

				}
			}
			//Modified by Anjali R. on[15/01/2018][To check lsSchemeCode null][Start]
			//else if (!"F".equalsIgnoreCase(lsItemStru) && lsSchemeCode.trim().length() > 0)
			else if (!"F".equalsIgnoreCase(lsItemStru) && (lsSchemeCode != null && lsSchemeCode.trim().length() > 0))
				//Modified by Anjali R. on[15/01/2018][To check lsSchemeCode null][End]	
			{
				///////////////////// arun
				// if(cnt1==0)
				// {
				valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>").append("</item_flg>");
				setNodeValue(dom, "item_flg", getAbsString("B"));
				// }
				System.out.println("lsSchemeCode:::::::7" + lsSchemeCode);
				System.out.println("F.equalsIgnoreCase(lsItemStru) && lsSchemeCode.trim().length() > 0 lsSchemeCode"
						+ lsSchemeCode);
				valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsSchemeCode + "]]>")
				.append("</item_code>");
				setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));

				reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail2>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail2>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);

				lsSchemeEdit = distCommon.getDisparams("999999", "SCHEME_ALLOWEDIT", conn);

				if ("Y".equalsIgnoreCase(lsSchemeEdit)) {
					System.out.println("lsSchemeCode:::::::8" + lsSchemeCode);
					valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + lsSchemeCode + "]]>")
					.append("</item_code>");
					setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
				}
				if ("N".equalsIgnoreCase(lsSchemeEdit)) {
					System.out.println("lsSchemeCode:::::::9" + lsSchemeCode);
					valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsSchemeCode + "]]>")
					.append("</item_code>");
					setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
				}
			} else if (!"F".equalsIgnoreCase(lsItemStru)
					&& (lsSchemeCode == null || lsSchemeCode.trim().length() == 0)) {
				System.out.println("F.equalsIgnoreCase(lsItemStru) && lsSchemeCode.trim().length() == 0 lsSchemeCode"
						+ lsSchemeCode);
				System.out.println("lsSchemeCode:::::::10" + lsSchemeCode);
				// if(cnt1==0)
				// {
				valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsSchemeCode + "]]>")
				.append("</item_code>");
				setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
				// }
				if (schecnt >= 1) {
					valueXmlString.append("<item_flg>").append("<![CDATA[" + 'B' + "]]>").append("</item_flg>");
					setNodeValue(dom, "item_flg", getAbsString("B"));
					System.out.println("lsSchemeCode:::::::11" + lsSchemeCode);
					valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsSchemeCode + "]]>")
					.append("</item_code>");
					setNodeValue(dom, "item_code", getAbsString(lsSchemeCode));
				} else {
					valueXmlString.append("<item_flg>").append("<![CDATA[" + 'I' + "]]>").append("</item_flg>");
					setNodeValue(dom, "item_flg", getAbsString("I"));
					System.out.println("lsSchemeCode:::::::12" + lsSchemeCode);
					// if(cnt1==0)
					// {
					valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + lsItemCodeOrd + "]]>")
					.append("</item_code>");
					setNodeValue(dom, "item_code", getAbsString(lsItemCodeOrd));
					// }
				}
				reStr = itemChangedDet(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail2>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail2>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);

			}
			if ("F".equalsIgnoreCase(lsNature) || "B".equalsIgnoreCase(lsNature) || "S".equalsIgnoreCase(lsNature)) {
				lcRate = 0;
			} else {
				lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));
				lsListType = distCommon.getPriceListType(lsPriceList, conn);

				sql = "select count(1)  as llPlcount from pricelist where price_list=?"
						+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
						+ " and (ref_no is not null)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsPriceList);
				pstmt.setString(2, lsItemCodeOrd);
				pstmt.setString(3, lsUnit);
				pstmt.setString(4, lsListType);
				pstmt.setTimestamp(5, mOrderDate);
				pstmt.setTimestamp(6, mOrderDate);
				pstmt.setDouble(7, mQty);
				pstmt.setDouble(8, mQty);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					llPlcount = rs.getInt("llPlcount");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (llPlcount >= 1) {
					sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
							+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsPriceList);
					pstmt.setString(2, lsItemCodeOrd);
					pstmt.setString(3, lsUnit);
					pstmt.setString(4, lsListType);
					pstmt.setTimestamp(5, mOrderDate);
					pstmt.setTimestamp(6, mOrderDate);
					pstmt.setDouble(7, mQty);
					pstmt.setDouble(8, mQty);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsRefNo = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					lcRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, lsItemCodeOrd, lsRefNo, lsListType, mQty,
							conn);
				}
				if (lcRate <= 0) {
					lcRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, lsItemCodeOrd, lsRefNo, "L", mQty,
							conn);

					// sql = "select (case when price_list__parent is null then '' else
					// price_list__parent end ) as ls_price_list__parent" + " from pricelist_mst
					// where price_list =? and list_type=? and price_list__parent is not null";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, lsPriceList);
					// pstmt.setString(2, lsListType);
					// rs = pstmt.executeQuery();
					// if (rs.next())
					// {
					// lsPriceListParent = rs.getString("ls_price_list__parent");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// lsListType = distCommon.getPriceListType(lsPriceListParent, conn);
					//
					// sql = "select count(1) as ll_plcount from pricelist where price_list= ? and
					// item_code=? and unit=?" + " and list_type=? and eff_from<=? and valid_upto>=?
					// and min_qty<=? and max_qty>=? and (ref_no is not null)";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, lsItemCodeOrd);
					// pstmt.setString(2, lsUnit);
					// pstmt.setString(3, lsListType);
					// pstmt.setTimestamp(4, mOrderDate);
					// pstmt.setTimestamp(5, mOrderDate);
					// pstmt.setDouble(6, mQty);
					// pstmt.setDouble(7, mQty);
					// rs = pstmt.executeQuery();
					// if (rs.next())
					// {
					// llPlcount = rs.getInt("ll_plcount");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// if (llPlcount >= 1)
					// {
					// sql = "select max(ref_no) from pricelist where price_list=? and item_code=?
					// and unit=? and list_type=? and eff_from<= ?" + " and valid_upto >=? and
					// min_qty<=? and max_qty>=? (ref_no is not null)";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, lsPriceListParent);
					// pstmt.setString(2, lsItemCodeOrd);
					// pstmt.setString(3, lsUnit);
					// pstmt.setString(4, lsListType);
					// pstmt.setTimestamp(5, mOrderDate);
					// pstmt.setTimestamp(6, mOrderDate);
					// pstmt.setDouble(7, mQty);
					// pstmt.setDouble(8, mQty);
					// rs = pstmt.executeQuery();
					// if (rs.next())
					// {
					// lsRefNo = rs.getString("ref_no");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// lcRate = distCommon.pickRateRefnoWise(lsPriceListParent, ldtDate.toString(),
					// lsItemCodeOrd, lsRefNo, "L", mQty, conn);
					// }
				}
			}
			System.out.println("mqty comment" + mQty);
			System.out.println("lcRate comment" + lcRate);
			if (lcRate < 0) {
				lcRate = 0;
			}
			lcOrdValue = mQty * lcRate;
			// valueXmlString.append("<rate>").append("<![CDATA[" + lcRate +
			// "]]>").append("</rate>");
			// setNodeValue( dom, "rate", getAbsString(String.valueOf(lcRate)));
			lcOrdValue=finCommon.getRequiredDecimal(lcOrdValue,3);// added by nandkumar gadkari on 26/02/19 for rounding amount upto 3 decimals 
			valueXmlString.append("<ord_value>").append("<![CDATA[" + lcOrdValue + "]]>").append("</ord_value>");
			setNodeValue(dom, "ord_value", getAbsString(String.valueOf(lcOrdValue)));

			lsSiteCodeShip = checkNull(genericUtility.getColumnValue("site_code", dom1));
			llNoOfArt = distCommon.getNoArt(lsSiteCodeShip, lsCustCode, lsItemCodeOrd, null, mQty, 'B', acShipperQty,
					acIntegralQty, conn);
			lcShipperQty = acShipperQty;
			lcIntQty = acIntegralQty;

			llNoOfArt1 = distCommon.getNoArt(lsSiteCodeShip, lsCustCode, lsItemCodeOrd, null, mQty, 'S', acShipperQty,
					acIntegralQty, conn);
			lcBalQty = mQty - (lcShipperQty * llNoOfArt1);
			llNoOfArt2 = distCommon.getNoArt(lsSiteCodeShip, lsCustCode, lsItemCodeOrd, null, mQty, 'I', acShipperQty,
					acIntegralQty, conn);
			lcIntQty = acIntegralQty;

			lcShipperQty = lcShipperQty * llNoOfArt1;
			lcIntQty = lcIntQty * llNoOfArt2;
			lcLooseQty = mQty - (lcShipperQty + lcIntQty);

			String lsStr = " Shipper Quantity = " + lcShipperQty + "   Integral Quantity = " + lcIntQty
					+ "   Loose Quantity = " + lcLooseQty;
			valueXmlString.append("<st_shrink>").append("<![CDATA[" + lsStr + "]]>").append("</st_shrink>");
			setNodeValue(dom, "st_shrink", getAbsString(lsStr));
			if (lsSchemeCode != null && lsSchemeCode.trim().length() > 0) {
				sql = "select slab_on from scheme_applicability where scheme_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsSchemeCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mSlabOn = rs.getString("slab_on");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if ("N".equalsIgnoreCase(mSlabOn)) {
					sql = "select descr from bom where bom_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSchemeCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<st_scheme>").append("<![CDATA[" + lsDescr + "]]>").append("</st_scheme>");
					setNodeValue(dom, "st_scheme", getAbsString(lsDescr));
				} else {
					lcIntegralQty = distCommon.getIntegralQty(lsCustCode, lsItemCodeOrd, mSiteCode, conn);
					valueXmlString.append("<st_scheme>")
					.append("<![CDATA[" + "Integral Quantity : " + lcIntegralQty + "]]>")
					.append("</st_scheme>");
				}
			} else {
				lcIntegralQty = distCommon.getIntegralQty(lsCustCode, lsItemCodeOrd, mSiteCode, conn);
				valueXmlString.append("<st_scheme>")
				.append("<![CDATA[" + "Integral Quantity : " + lcIntegralQty + "]]>").append("</st_scheme>");
			}
			lcQty1 = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity", dom)));
			lcQtyFc = Double.parseDouble(checkDouble(genericUtility.getColumnValue("quantity__fc", dom)));

			System.out.println("lcQty1[" + lcQty1 + "] lcQtyFc[" + lcQtyFc + "]");

			if (lcQtyFc == 0 || lcQty1 != lcQtyFc) {
				valueXmlString.append("<quantity__fc>").append("<![CDATA[" + lcQty1 + "]]>").append("</quantity__fc>");
				setNodeValue(dom, "quantity__fc", getAbsString(String.valueOf(lcQty1)));
			}

			lsListType = distCommon.getPriceListType(lsPriceList, conn);
			long amt = 0;
			if (lsListType.equalsIgnoreCase("L")) {
				lcRate = Double.parseDouble(genericUtility.getColumnValue("rate", dom));
				amt = (long) (mQty * lcRate);
				valueXmlString.append("<amount>").append("<![CDATA[" + amt + "]]>").append("</amount>");
				setNodeValue(dom, "amount", getAbsString(String.valueOf(amt)));
				System.out.println("IC quantity rate" + lcRate + "::amount" + amt + ":::lsListType" + lsListType);
			} else {
				amount=finCommon.getRequiredDecimal(mQty * lcRate,3);// added by nandkum ar gadkari on 26/02/19 for rounding amount upto 3 decimals 
				valueXmlString.append("<amount>").append("<![CDATA[" + amount + "]]>").append("</amount>");// change mQty * lcRate to amount by nandkumar gadkari on 26/02/09
				setNodeValue(dom, "amount", getAbsString(String.valueOf(amount)));// change mQty * lcRate to amount by nandkumar gadkari on 26/02/09
			}

			lsSalesOrd = checkNull(genericUtility.getColumnValue("sale_order", dom));

			sql = "select  quot_no from sorder where sale_order =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSalesOrd);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsQuotNo = rs.getString("quot_no");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (lsQuotNo != null && lsQuotNo.trim().length() > 0) {
				valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[]]>").append("</rate>");
			}

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added BY Mukesh Chauhan on 06/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}
	//Added By PriyankaC on 1FEB2018..[START]  
	private String checkNullandTrim(String input) {
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
	//Added By PriyankaC on 1FEB2018..[END]
	//Changes by mayur on 10-MAY-2018--START
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
	//Changes by mayur on 10-MAY-2018--END
	private double checkIntNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0;
		} else {
			return Double.parseDouble(str);
		}

	}

	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}

	// gbf_get_price_list_type
	public String getPriceList(String itemcode, String unit, String priceList, Timestamp pListDate, Connection conn)
			throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String listType = "";
		try {
			sql = "select list_type from	pricelist where	price_list=?  and item_code = ? and unit = ? and eff_from <= ? and	valid_upto	>=	?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemcode);
			pstmt.setString(3, unit);
			pstmt.setTimestamp(4, pListDate);
			pstmt.setTimestamp(5, pListDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				listType = rs.getString("list_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("listType from getPriceList function =====" + listType);
			if (listType == null) {
				listType = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return listType;

	}

	// gbf_get_discount
	public double getDiscount(String itemCode, String unit, double itemQuantity, Document dom, Document dom1,
			Document dom2, Connection conn) throws ITMException, Exception {
		String priceList, custCode, siteCode;
		// Date orderDate = null, plDate = null;
		Date plDate = null;
		Timestamp orderDate = null;
		double plistDisc = 0, discMerge = 0;
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		try {
			priceList = genericUtility.getColumnValue("price_list__disc", dom);
			if (genericUtility.getColumnValue("order_date", dom1) == null) {
				orderDate = null;
			} else {
				orderDate = Timestamp
						.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				System.out.println("getDiscount orderDate::" + orderDate);
			}
			custCode = genericUtility.getColumnValue("cust_code", dom1);
			siteCode = genericUtility.getColumnValue("site_code", dom1);
			if (genericUtility.getColumnValue("pl_date", dom1) == null) {
				plDate = null;
			} else {
				plDate = Timestamp
						.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("pl_date", dom1),
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			plistDisc = this.getDiscount(priceList, orderDate, custCode, siteCode, itemCode, unit, discMerge, plDate,
					itemQuantity, conn);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return plistDisc;
	}

	public double getDiscount(String priceList, Timestamp orderDate, String custCode, String siteCode, String itemCode,
			String unit, double discMerge, Date pListDate, double quantity, Connection conn) throws ITMException {
		String listType = "", itmSer = "";
		double disc = 0;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		double rate = 0;
		try {
			if (priceList != null && priceList.trim().length() > 0) {
				sql = " select case when rate is null then 0 else rate end as rate from pricelist  where price_list=? and item_code =? and unit= ? and list_type IN ('M','N')	and	case when min_qty is null then 0 else min_qty end <=?	and ((case when max_qty is null then 0 else max_qty end	>=	?) OR (case when max_qty is null then 0 else max_qty end=0)) and eff_from<=	?	and	valid_upto	>=	?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, priceList);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, unit);
				pstmt.setDouble(4, quantity);
				pstmt.setDouble(5, quantity);
				/*pstmt.setDouble(6, quantity);*/ //commented by Nandkumar Gadkari on 30/07/18  
				pstmt.setTimestamp(6, orderDate);
				pstmt.setTimestamp(7, orderDate);
				// pstmt.setDate(7, (java.sql.Date) orderDate);
				// pstmt.setDate(8, (java.sql.Date) orderDate);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					disc = rs.getDouble("rate");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (listType.equals("M") || priceList == null || priceList.trim().length() == 0 || disc == 0) {
				sql = "select item_ser from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					itmSer = rs.getString("item_ser");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql = "select disc_perc from customer_series 	where cust_code = ? and item_ser =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itmSer);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					disc = rs.getDouble("disc_perc");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (disc == 0) {
					sql = "select disc_perc from site_customer where site_code = ? and cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						disc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (disc == 0) {
					sql = "select disc_perc  from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						disc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (listType.equals("M")) {
					discMerge = disc;
					if (rate != 0) {
						disc = rate;
					}
				} else {
					discMerge = 0;
				}
				if (disc == 0) {
					disc = 0;
				}
				if (itemCode == null || itemCode.trim().length() == 0) {
					disc = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return disc;

	}

	// gbf_get_contract
	public String getContract(String siteCode, String custCode, Timestamp orderDate, String itemCode, String contractNo,
			String contractLineNo, Connection conn) throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		String sql = "";
		String saleContractNo = "";
		SimpleDateFormat sdf;
		System.out.println("====getContract ==");
		try {
			System.out.println("====getContract TRY=");
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			System.out.println("getContract >> contractNo ======>>>>"+contractNo);
			System.out.println("getContract >> contractLineNo ======>>>>"+contractLineNo);
			System.out.println("getContract >> itemCode ======>>>>"+itemCode);
			
			if (itemCode.trim().length() == 0) 
			{				
				//Modified by Yashwant S. on 19-10-2020[START][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
				// Modified by Sana S - 23-11-2020
				// if(contractNo == null || contractNo.trim().length() == 0)
				if(contractNo == null || contractNo.trim().length() == 0 || "null".equalsIgnoreCase(contractNo.trim()))
				{
					//Modified by Yashwant S. on 19-10-2020[END][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
					System.out.println("====getContract TRY11=");
					//Pavan Rane 13sep19 start[to validate closed contract if contract validy is exists.]
					//sql = "select contract_no from scontract where site_code = ? and	cust_code = ? and eff_from <= ?	and	valid_upto	>= ? and (case when confirmed is null then 'N' else confirmed end) = 'Y' and (case when status is null then 'N' else status end) <> 'X' order by eff_from";
					sql = "select contract_no from scontract where site_code = ? and cust_code = ? and eff_from <= ? and	valid_upto	>= ? and (case when confirmed is null then 'N' else confirmed end) = 'Y' and (case when status is null then 'N' else status end) not in('X','C') order by eff_from";
					//Pavan Rane 13sep19 end
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setTimestamp(3, orderDate);
					pstmt.setTimestamp(4, orderDate);
					// pstmt.setTimestamp(4, orderDate);
					rs = pstmt.executeQuery();
					//Modified by Yashwant S. on 19-10-2020[START]
					//while (rs.next())
					if(rs.next())
						//Modified by Yashwant S. on 19-10-2020[END]
					{
						saleContractNo = rs.getString("contract_no");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					contractNo = saleContractNo;
				}
				contractLineNo = "";
			}
			else
			{
				//Modified by Yashwant S. on 19-10-2020[START][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
				// Modified by Sana S - 23-11-2020
				// if(contractNo == null || contractNo.trim().length() == 0)
				if(contractNo == null || contractNo.trim().length() == 0 || "null".equalsIgnoreCase(contractNo.trim()))
				{
					//Modified by Yashwant S. on 19-10-2020[END][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
					System.out.println("====getContract TRY22=");
					//Pavan Rane 13sep19 start[to validate closed contract if contract validy is exists.]
					//sql = "select SC.contract_no, SC.line_no	from scontract S, scontractdet SC	where	S.contract_no	= SC.contract_no	and	S.site_code	= ?	and	S.cust_code	= ?	and	S.eff_from	<=	?	and	S.valid_upto >=	?  and   (case when S.confirmed is null then 'N' else S.confirmed end) = 'Y' and   (case when S.status is null then 'N' else S.status end) <> 'X' and SC.item_code= ? order by S.eff_from";
					sql = "select SC.contract_no, SC.line_no	from scontract S, scontractdet SC	where	S.contract_no	= SC.contract_no	and	S.site_code	= ?	and	S.cust_code	= ?	and	S.eff_from	<=	?	and	S.valid_upto >=	?  and   (case when S.confirmed is null then 'N' else S.confirmed end) = 'Y' and   (case when SC.status is null then 'N' else SC.status end) <> 'C' and SC.item_code= ? order by S.eff_from";
					//Pavan Rane 13sep19 end
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setTimestamp(3, orderDate);
					pstmt.setTimestamp(4, orderDate);
					pstmt.setString(5, itemCode);
					rs = pstmt.executeQuery();
					//Modified by Yashwant S. on 19-10-2020[START]
					//while (rs.next())
					if(rs.next())
					//Modified by Yashwant S. on 19-10-2020[END]
					{
						contractNo = rs.getString("contract_no");
						contractLineNo = rs.getString("line_no");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Modified by Yashwant S. on 19-10-2020[START][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
				}           	
				// Modified by Sana S - 23-11-2020
				// else if(contractLineNo == null || contractLineNo.trim().length() == 0)
				else if(contractLineNo == null || contractLineNo.trim().length() == 0 || "null".equalsIgnoreCase(contractLineNo.trim()))
				{
					System.out.println("====getContract TRY23=");
					sql = "select SC.contract_no, SC.line_no	from scontract S, scontractdet SC	where	S.contract_no	= SC.contract_no	and	S.site_code	= ?	and	S.cust_code	= ?	and	S.eff_from	<=	?	and	S.valid_upto >=	?  and   (case when S.confirmed is null then 'N' else S.confirmed end) = 'Y' and   (case when SC.status is null then 'N' else SC.status end) <> 'C' and SC.item_code= ? and s.contract_no = ? order by S.eff_from";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setTimestamp(3, orderDate);
					pstmt.setTimestamp(4, orderDate);
					pstmt.setString(5, itemCode);
					pstmt.setString(6, contractNo);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						contractNo = rs.getString("contract_no");
						contractLineNo = rs.getString("line_no");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				//Modified by Yashwant S. on 19-10-2020[END][To resolve issue of sales contract release, system picks wrong rate if sales contract is for same customer and item code]
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		if (contractNo == null || contractLineNo.trim().length() == 0) {

			return contractNo;

		} else {
			return contractNo + "@" + contractLineNo;
		}
	}

	// gbf_itemchangeddet1

	// gbf_get_contract_hdr
	public String getContractHrd(String siteCode, String custCode, Timestamp orderDate, String itemSer, Connection conn)
			throws Exception {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String contractNo = "";
		try {

			sql = "select contract_no from scontract	where site_code = ? and cust_code = ? and item_ser = ?  and ? "
					+ " between eff_from and valid_upto  and confirmed = 'Y'  and "
					//Pavan Rane 13sep19 start[to validate closed contract if contract validy is exists.]
					//+ "(case when status is null then ' ' else status end) <> 'X' " + " order by contract_no";
					+ "(case when status is null then ' ' else status end) not in ('X','C') " + " order by contract_no";
			//Pavan Rane 13sep19 start[to validate closed contract if contract validy is exists.]
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, custCode);
			pstmt.setString(3, itemSer);
			pstmt.setTimestamp(4, orderDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				contractNo = rs.getString("contract_no");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return contractNo;

	}
	// gbf_get_pricelist_for_ordertype
	public String getPriceListOrderType(Timestamp orderDate, Document dom, Document dom1, Document dom2,
			Connection conn) throws ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		String sql = "";
		String orderType = "", priceList = "", orderTypeList = "", orderType1 = "", priceList1 = "", lsExit = "F";
		int cnt = 0;
		Timestamp orderDate1 = null;
		// SimpleDateFormat sdf;
		try
		{
			//SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
			// SimpleDateFormat dateFormat=new
			// SimpleDateFormat(genericUtility.getDBDateFormat());
			//String orderDateInStr = dateFormat.format(orderDate);
			//System.out.println("orderDateInStr*****" + orderDateInStr);

			//Changes by mayur on 08-JAN-18--[START]
			String orderDateInStr = "";

			if(orderDateInStr.trim().length() > 0)
			{
				orderDateInStr = genericUtility.getValidDateString(orderDateInStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
				orderDate = Timestamp.valueOf(orderDateInStr + " 00:00:00");  
			}

			orderType = checkNull(genericUtility.getColumnValue("order_type", dom));
			System.out.println("orderType==" + orderType);
			sql = "select count(1) from pricelist where eff_from <= ? and valid_upto >= ? and order_type <> null";
			pstmt = conn.prepareStatement(sql);

			pstmt.setTimestamp(1,orderDate);
			pstmt.setTimestamp(2,orderDate);
			//pstmt.setString(1, orderDateInStr);
			//pstmt.setString(2, orderDateInStr);
			//Changes by mayur on 08-JAN-18--[END]
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				cnt = rs.getInt(1);
				System.out.println("Count is-----" + cnt);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (cnt > 0)
			{
				sql = "select distinct price_list , order_type from pricelist where eff_from <=? and valid_upto >= ? and order_type <> null";
				pstmt = conn.prepareStatement(sql);
				//Changes by mayur on 08-JAN-18--[START]
				pstmt.setTimestamp(1, orderDate);
				pstmt.setTimestamp(2, orderDate);
				//Changes by mayur on 08-JAN--[END]
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					priceList1 = checkNull(rs.getString("price_list"));
					orderTypeList = rs.getString("order_type");
					if (orderTypeList.trim().length() > 0)
					{
						do
						{
							orderType1 = distCommon.getToken(orderTypeList, ",");
							if (orderType1.trim().length() == orderType.trim().length())
							{
								priceList = priceList1;
								lsExit = "T";
								break;
							}
						} while (orderTypeList.trim().length() > 0);
					}
					if (lsExit == "T")
					{
						break;
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}

		return priceList;
	}
	//Changes done by mayur on 08-JAN-18---[END]

	// gbf_exchrate_protect
	public String exchangeRateProtect(String currCode, String siteCode, String exchangeRateCol, Connection conn)
			throws ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
		String curCodeBase = "";
		StringBuffer valueXmlString = null;
		//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
		try {

			valueXmlString =  new StringBuffer();
			if (currCode != null && currCode.trim().length() > 0) 
			{
				sql = "select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity  and b.site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					curCodeBase = checkNull(rs.getString("curr_code"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][Start]
			System.out.println("currCode---["+currCode+"]curCodeBase--["+curCodeBase+"]");
			if(curCodeBase.equalsIgnoreCase(currCode))
			{
				valueXmlString.append("<"+exchangeRateCol+" protect = \"1\">").append("<![CDATA[1.0]]>").append("</"+exchangeRateCol+">");
			}
			//Modified by Anjali R. on [04/09/2018][To protect exchange rate if curr_code and base currency is same][End]
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return valueXmlString.toString();//Modified by Anjali R. 
	}

	// gbf_get_scheme_code
	public String getSchemeCode(Document dom, Document dom1, Document dom2, String currentColumn, Connection conn)
			throws ITMException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy");
		SimpleDateFormat sdf;
		StringBuffer valueString = new StringBuffer();

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		String sql = "";
		// Date orderDate = null;
		Timestamp orderDate = null;
		String itemCodeOrd = "", siteCode = "", custCode = "", priceList = "", orderType = "", stateCodeDlv = "",
				itemCodeParent = "", schemeCode = "", countCode = "";
		int cnt = 0;
		Timestamp ordDate = null;
		try {
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom2));
			siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
			// ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
			ordDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			// orderDate = sdf.parse(ordDate);
			priceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
			orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
			stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
			System.out.println("itemCodeOrd==" + itemCodeOrd);
			System.out.println("siteCode==" + siteCode);
			System.out.println("custCode==" + custCode);
			System.out.println("ordDate==" + ordDate);
			System.out.println("priceList==" + priceList);
			System.out.println("orderType==" + orderType);
			System.out.println("stateCodeDlv==" + stateCodeDlv);
			sql = "select item_code__parent  from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCodeOrd);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				itemCodeParent = rs.getString("item_code__parent");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (itemCodeParent == null || itemCodeParent.trim().length() == 0) {
				sql = "select count(1) from item where item_code__parent =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeOrd);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (cnt > 0) {
					itemCodeParent = itemCodeOrd;
				}
			}
			schemeCode = getCheckScheme(itemCodeParent, orderType, custCode, siteCode, stateCodeDlv, countCode,
					orderDate);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return schemeCode;

	}

	// gbf_check_scheme
	public String getCheckScheme(String itemCode, String orderType, String custCode, String siteCode, String stateCode,
			String countCode, Timestamp orderDate) throws ITMException {
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		String sql = "";
		String plistDisc = "";

		boolean lbParent = false, lbProceed = false;
		;
		String countCodeDlv = "", saleOrder = "", schemeCode = "", applyCustList = "", noApplyCustList = "",
				appOrderType = "", lsToken = "", round = "";
		String prevScheme = "", schemeCode1 = "", applyCust = "", custSchemeCode = "", lineNo = "", browItemCode = "",
				currLineNo = "", itemCodeParentCurr = "";
		long schCnt = 0;
		try {
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			if (orderType == null) {
				orderType = "";
			}
			if (siteCode == null) {
				siteCode = "";
			}
			if (stateCode == null) {
				stateCode = "";
			}
			if (countCode == null) {
				countCode = "";
			}
			/*
			 * java.util.Date utilDate = new java.util.Date(orderDate.toString());
			 * java.sql.Timestamp sq = new java.sql.Timestamp(utilDate.getTime());
			 */
			System.out.println("sq @@@@@@<<<<<" + orderDate + "]");

			sql = "select a.scheme_code  from scheme_applicability   a,scheme_applicability_det  b "
					+ "where a.scheme_code = b.scheme_code and a.item_code =? "
					+ " and a.app_from  <= ?  and a.valid_upto  >= ?  and"
					+ " (b.site_code  = ?  or b.state_code = ? or b.count_code =? )";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setTimestamp(2, orderDate);
			pstmt.setTimestamp(3, orderDate);
			pstmt.setString(4, siteCode);
			pstmt.setString(5, stateCode);
			pstmt.setString(6, countCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				schemeCode = rs.getString("scheme_code");
				sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, schemeCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					applyCustList = rs1.getString("apply_cust_list");
					noApplyCustList = rs1.getString("noapply_cust_list");
					appOrderType = rs1.getString("order_type");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if (orderType.trim() == "NE" && (appOrderType == null || appOrderType.trim().length() == 0)) {
					break;
				} else if (appOrderType != null && appOrderType.trim().length() > 0) {
					lbProceed = false;
					do {
						lsToken = distCommon.getToken(appOrderType, ",");
						System.out.println("orderType == "+orderType+" lsToken == "+lsToken+" appOrderType == "+appOrderType);
						//if (orderType.trim().length() == lsToken.trim().length())
						if(orderType.equalsIgnoreCase(lsToken))//Changed by Jaffar S. on 15-02-19
						{
							lbProceed = true;

						}
						break;
					} while (appOrderType.trim().length() > 0);
				}
				prevScheme = schemeCode1;
				schemeCode1 = schemeCode;
				if (applyCustList.trim().length() > 0) {
					schemeCode1 = "";
					do {
						applyCust = distCommon.getToken(applyCustList, ",");
						if (applyCust.trim().length() == custCode.trim().length()) {
							schemeCode1 = schemeCode;
							custSchemeCode = schemeCode;
							break;
						}
					} while (applyCustList.trim().length() > 0);
				}
				if (noApplyCustList.trim().length() > 0 && schemeCode != null) {
					do {
						applyCust = distCommon.getToken(noApplyCustList, ",");
						if (noApplyCustList.trim().length() == custCode.trim().length()) {
							schemeCode1 = "";
						}
					} while (noApplyCustList.trim().length() > 0);
				}
				if (schemeCode1 != null) {
					schCnt++;
				} else if (schCnt == 1) {
					schemeCode1 = prevScheme;
				}
			}
			pstmt.close();
			rs.close();
			pstmt = null;
			rs = null;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		//Added for Close Connection.
		finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		//Added for Close Connection.
		return schemeCode1;

	}

	// gbf_rate

	// gbf_ic_rate

	// gbf_valdata_group_scheme

	public String valDataGroupScheme(Document dom, Document dom1, Document dom2, String currentColumn,
			String objContext, String editFlag, String nature, Connection conn) throws ITMException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy");
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		PreparedStatement pstmt = null, pstmt1 = null,pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		String sql = "",sql2="";
		double chargeQty = 0, batQty = 0, roundTo = 0, appMinQty = 0, appMaxQty = 0, qtyPer = 0, freeQty = 0,
				discount = 0, chargeQty1 = 0;
		double totChargeQty = 0, totFreeQty = 0, totSampleQty = 0, totBonusQty = 0, minQty = 0, unConfTotChargeQty = 0,unconfirmChargeValue = 0,
				unConfTotFreeQty = 0, unConfTotSampleQty = 0, unConfTotBonusQty = 0;
		double ConfTotChargeQty = 0, ConfTotFreeQty = 0, ConfTotSampleQty = 0, ConfTotBonusQty = 0, prvChargeQty = 0,chargeTotamt= 0,
				prvFreeQty = 0, prvBonusQty = 0, prvSampleQty = 0, quantity = 0, rate1 = 0, prvRate = 0;
		String itemCodeOrd = "", siteCode = "", custCode = "", ordDate = "", priceList = "", orderType = "",
				stateCodeDlv = "", itemCodeParent = "";
		String countCodeDlv = "", saleOrder = "", schemeCode = "", applyCustList = "", noApplyCustList = "",
				appOrderType = "", lsToken = "", round = "";
		String prevScheme = "", schemeCode1 = "", applyCust = "", custSchemeCode = "", lineNo = "", browItemCode = "",itemCodeoff ="" ,
				currLineNo = "", itemCodeParentCurr = "";
		String errString = "", productCodeCurr = "", productCode = "", prodSh="";
		String errCode = "";
		String errorType = "", schAttr = "", prodCodeOff = "", prodCodePur = "";
		String childNodeName = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName1 = null;
		int childNodeListLength;
		int ctr = 0, cnt1 = 0;
		int currentFormNo = 0, purcBase = 0, schAllowence = 0;
		int schemeType = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;

		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int cnt = 0,cntof = 0,cntpur = 0;
		double qty = 0;
		long schCnt = 0;
		// Date appFrom = null, validUpto = null;
		Timestamp orderDate = null, appFrom = null, validUpto = null;
		// Date orderDate = null;
		boolean lbParent = false, lbProceed = false;
		;
		try {
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("Priyanka testing : currentFormNo :" + currentFormNo);
			}

			itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom2));
			System.out.println("itemCodeOrd ");
			siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
			ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
			orderDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			// orderDate = sdf.parse(ordDate);
			qty = checkIntNull(genericUtility.getColumnValue("quantity", dom2));
			orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
			stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
			countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));
			saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom2));
			System.out.println("itemCodeOrd==" + itemCodeOrd);
			System.out.println("siteCode==" + siteCode);
			System.out.println("custCode==" + custCode);
			System.out.println("ordDate==" + ordDate);
			System.out.println("orderDate==" + orderDate);
			System.out.println("qty==" + qty);
			System.out.println("orderType==" + orderType);
			System.out.println("stateCodeDlv==" + stateCodeDlv);
			System.out.println("countCodeDlv==" + countCodeDlv);
			System.out.println("dom @@@@12" + dom + "]");
			System.out.println("dom @@@@13" + dom1 + "]");
			System.out.println("dom @@@@14" + dom2 + "]");
			System.out.println("saleOrder==" + saleOrder);

			System.out.println("schAttr value of 10" + schAttr + "]");
			schAttr = checkNull(genericUtility.getColumnValue("sch_attr", dom));
			System.out.println("dom @@@@" + dom + "]");
			System.out.println("schAttr value of 20" + schAttr + "]");
			if (!("Y").equalsIgnoreCase(schAttr)) {
				System.out.println("schAttr value of 30" + schAttr + "]");
				sql = "select item_code__parent from item where item_code = ?  and item_code__parent is not null";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeOrd);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					itemCodeParent = rs.getString("item_code__parent");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (itemCodeParent == null || itemCodeParent.trim().length() == 0) {
					sql = "select item_code__parent  from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						itemCodeParent = rs.getString("item_code__parent");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (itemCodeParent == null || itemCodeParent.trim().length() > 0) {
						sql = "select count(1) from item where item_code__parent =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt > 0) {
							itemCodeParent = itemCodeOrd;
							lbParent = true;
						}
					}
				}
				if (itemCodeOrd.trim().length() > 0) {
					if (orderType == null) {
						orderType = "";
					}
					if (siteCode == null) {
						siteCode = "";
					}
					if (stateCodeDlv == null) {
						stateCodeDlv = "";
					}
					if (countCodeDlv == null) {
						countCodeDlv = "";
					}
					sql2 = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code= b.scheme_code and a.item_code= ? and a.app_from <= ? and a.valid_upto>= ? and (b.site_code= ? or b.state_code = ?  or b.count_code= ?)";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, itemCodeOrd);
					pstmt2.setTimestamp(2, orderDate);
					pstmt2.setTimestamp(3, orderDate);
					pstmt2.setString(4, siteCode);
					pstmt2.setString(5, stateCodeDlv);
					pstmt2.setString(6, countCodeDlv);
					rs2 = pstmt2.executeQuery();// missing executeQuery and pstmt2 rs2 added  by Nandkumar Gadkari on 03/10/18
					while (rs2.next()) {
						//schemeCode = rs.getString("scheme_code"); //Commented & changed to rs2 by Jaffar S
						schemeCode = rs2.getString("scheme_code");

						String prodSh1 = "N";
						String sql22 = "SELECT CASE WHEN PROD_SCH IS NULL THEN 'N' ELSE PROD_SCH END AS PROD_SCH FROM SCHEME_APPLICABILITY WHERE SCHEME_CODE = ? ";
						pstmt = conn.prepareStatement(sql22);
						pstmt.setString(1, schemeCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							prodSh1 = rs.getString(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if("Y".equalsIgnoreCase(prodSh1) && (schemeCode!=null || schemeCode.trim().length()>0))
						{
							sql = "select count (*) as cnt from sch_pur_items  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							//pstmt.setString(2, schemeCode);//Commented and correction made by Jaffar S on 07-02-19
							pstmt.setString(2, itemCodeOrd);
							rs = pstmt.executeQuery();

							if (rs.next()) 
							{
								cnt1 = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt1==0)
							{

								sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt1 = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(cnt1==0)
								{
									continue;
								}

							}
						}



						sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, schemeCode);
						rs1 = pstmt1.executeQuery();// rs1 make by nandkumar gadkari on 03/10/18

						if (rs1.next()) {
							applyCustList = rs1.getString("apply_cust_list");
							noApplyCustList = rs1.getString("noapply_cust_list");
							appOrderType = rs1.getString("order_type");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if (orderType.trim() == "NE" && (appOrderType == null || appOrderType.trim().length() == 0)) {
							break;
						} else if (appOrderType != null && appOrderType.trim().length() > 0) {
							lbProceed = false;
							/*
							 * do { lsToken = distCommon.getToken(appOrderType, ","); if
							 * (orderType.trim().length() == lsToken.trim().length()) { lbProceed = true; }
							 * } while (appOrderType.trim().length() > 0);
							 */
						}
						prevScheme = schemeCode1;
						schemeCode1 = schemeCode;
						if (applyCustList.trim().length() > 0) {
							schemeCode1 = "";
							do {
								applyCust = distCommon.getToken(applyCustList, ",");
								if (applyCust.trim().length() == custCode.trim().length()) {
									schemeCode1 = schemeCode;
									custSchemeCode = schemeCode;
									break;
								}
							} while (applyCustList.trim().length() > 0);
						}
						if (noApplyCustList.trim().length() > 0 && schemeCode != null) {
							do {
								applyCust = distCommon.getToken(noApplyCustList, ",");
								if (noApplyCustList.trim().length() == custCode.trim().length()) {
									schemeCode1 = "";
								}
							} while (noApplyCustList.trim().length() > 0);
						}
						if (schemeCode1 != null) {
							schCnt++;
						} else if (schCnt == 1) {
							schemeCode1 = prevScheme;
						}
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;

					if (schCnt == 0) {
						errCode = "VTFREEQTY";// Scheme is not applicable for the
						// entered item code
						errList.add(errCode);
						//errFields.add(childNodeName.toLowerCase());
						System.out.println("Scheme is not applicable for the entered item code");
					} else if (schCnt > 0) {
						errCode = "VTITEM10";// Item cannot have more than one
						// scheme applicable for same period.
						errList.add(errCode);
						//errFields.add(childNodeName.toLowerCase());
						System.out.println("Item cannot have more than one scheme applicable for same period.");
					} else if (custSchemeCode.trim().length() > 0) {
						schemeCode = custSchemeCode;
					}
					sql = "select app_from, valid_upto from scheme_applicability where scheme_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode1);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						appFrom = rs.getTimestamp("app_from");
						validUpto = rs.getTimestamp("valid_upto");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;

					sql = "select tot_charge_qty, tot_free_qty,tot_sample_qty,tot_bonus_qty 	from prd_scheme_trace where site_code= ? and cust_code	=? and item_code = ? and scheme_code= ? and ? between eff_from and valid_upto";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setString(3, itemCodeParent);
					pstmt.setString(4, schemeCode);
					pstmt.setTimestamp(5, orderDate);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						totChargeQty = rs.getDouble("tot_charge_qty");
						totFreeQty = rs.getDouble("tot_free_qty");
						totSampleQty = rs.getDouble("tot_sample_qty");
						totBonusQty = rs.getDouble("tot_bonus_qty");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (totChargeQty == 0) {
						totChargeQty = 0;
					}
					if (totFreeQty == 0) {
						totFreeQty = 0;
					}
					if (totSampleQty == 0) {
						totSampleQty = 0;
					}
					if (totBonusQty == 0) {
						totBonusQty = 0;
					}
					if (minQty == 0) {
						minQty = 0;
					}
					if (saleOrder == null) {
						saleOrder = "";
					}
					if (lbParent == false) {
						sql = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty, "
								//--- Commented by Jaffar S on 30-01-19 for fixing the below line which was already presented when the component has been developed
								//+ " sum(case		int cnt = 0,cntof = 0,cntpur = 0;\n" + 
								+ "sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty, "
								+ "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, "
								+ "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty "
								+ "from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
								+ "and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
								+ "	and b.item_code__ord in (select item_code from item where item_code__parent = ?)"
								+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						pstmt.setString(3, saleOrder);
						pstmt.setTimestamp(4, orderDate);
						pstmt.setTimestamp(5, orderDate);
						pstmt.setString(6, itemCodeParent);
						pstmt.setString(7, nature);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							unConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
							unConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
							unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
							unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;

					} else {
						sql = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty ,"
								+ " sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty ,"
								+ "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, "
								+ "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty "
								+ "from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
								+ "and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
								+ "	and b.item_code__ord in (select item_code from item where item_code__parent = ?)"
								+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						pstmt.setString(3, saleOrder);
						pstmt.setTimestamp(4, orderDate);
						pstmt.setTimestamp(5, orderDate);
						pstmt.setString(6, itemCodeParent);
						pstmt.setString(7, nature);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							unConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
							unConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
							unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
							unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					}
					if (unConfTotChargeQty == 0) {
						unConfTotChargeQty = 0;
					}
					if (unConfTotFreeQty == 0) {
						unConfTotFreeQty = 0;
					}
					if (unConfTotSampleQty == 0) {
						unConfTotSampleQty = 0;
					}
					if (unConfTotBonusQty == 0) {
						unConfTotBonusQty = 0;
					}
					sql = "select sum(case when nature ='C' then quantity else 0 end) - sum(case when nature ='C' then qty_desp else 0 end)as confirmChargeQty,"
							+ " sum(case when nature ='F' then quantity else 0 end)- sum(case when nature ='F' then qty_desp else 0 end) as confirmFreeQty,"
							+ " sum(case when nature ='B' then quantity else 0 end)- sum(case when nature ='B' then qty_desp else 0 end) as confirmBonusQty,"
							+ " sum(case when nature ='S' then quantity else 0 end)- sum(case when nature ='S' then qty_desp else 0 end) as confirmSampleQty "
							+ "from sorditem ,SORDER where sorditem.sale_order = SORDER.SALE_ORDER AND sorditem.site_code = ? AND SORDER.cust_code =? "
							//-----Commented and changes made by Jaffar S on 30-01-19 for fixing the below line of the query which was presented from the beginning of the developed component
							//+ "and sorditem.sale_order <> ? and sorditem.line_type  <> 'B' and sorditem.order_date between '? and ? "
							+ "and sorditem.sale_order <> ? and sorditem.line_type  <> 'B' and sorditem.order_date between ? and ? "
							+ "and (sorditem.item_code in (select item_code from item where item_code__parent =?) OR sorditem.item_code = ?)"
							+ " and sorditem.nature in ('C' ,?) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setString(3, saleOrder);
					pstmt.setTimestamp(4, orderDate);
					pstmt.setTimestamp(5, orderDate);
					pstmt.setString(6, itemCodeParent);
					pstmt.setString(7, itemCodeParent);
					pstmt.setString(8, nature);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						//---Commented & changed by Jaffar S on 30-01-19 for using the correct alias which is used in above query [Start]

						/*ConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
						ConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
						ConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
						ConfTotBonusQty = rs.getDouble("unconfirmBonusQty");*/

						ConfTotChargeQty = rs.getDouble("confirmChargeQty");
						ConfTotFreeQty = rs.getDouble("confirmFreeQty");
						ConfTotSampleQty = rs.getDouble("confirmSampleQty");
						ConfTotBonusQty = rs.getDouble("confirmBonusQty");

						//---Commented & changed by Jaffar S on 30-01-19 for using the correct alias which is used in above query [End]
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (ConfTotChargeQty == 0) {
						ConfTotChargeQty = 0;
					}
					if (ConfTotFreeQty == 0) {
						ConfTotFreeQty = 0;
					}
					if (ConfTotSampleQty == 0) {
						ConfTotSampleQty = 0;
					}
					if (ConfTotBonusQty == 0) {
						ConfTotBonusQty = 0;
					}
					currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					prvChargeQty = 0;
					prvFreeQty = 0;
					prvBonusQty = 0;
					prvSampleQty = 0;
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();

					for (ctr = 0; ctr < childNodeListLength; ctr++) {

						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom2));
						nature = checkNull(genericUtility.getColumnValue("nature", dom2));
						browItemCode = checkNull(genericUtility.getColumnValue("item_code", dom2));

						if (currLineNo != lineNo) {
							cnt = 0;
							sql = "select item_code__parent from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, browItemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								itemCodeParentCurr = rs.getString("item_code__parent");
								System.out.println("itemCodeParentCurr: ====" + itemCodeParentCurr);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (itemCodeParentCurr == null || itemCodeParentCurr.trim().length() == 0) {
								sql = "select count(1)  from item where item_code__parent = ?	";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, browItemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;
								if (cnt > 0) {
									itemCodeParentCurr = browItemCode;
								}
							}
							if (itemCodeParentCurr.trim() == itemCodeParent.trim()) {
								quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom2));

								if (nature.equals("F")) {
									prvFreeQty = prvFreeQty + quantity;

								} else if (nature.equals("B")) {
									prvBonusQty = prvBonusQty + quantity;
								} else if (nature.equals("S")) {
									prvSampleQty = prvSampleQty + quantity;
								} else {
									prvChargeQty = prvChargeQty + quantity;
								}
							}
						}
					}
					chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;

					System.out.println("chargeQty ::::::::::::::" + chargeQty + ",free QTY:::::::::");
					sql = "Select count(1) From bom Where bom_code = ? And  ? between case when min_qty is null then 0 else min_qty end"
							+ " And case when max_qty is null then 0 else max_qty end";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode);
					//pstmt.setDouble(1, chargeQty);--------Commented & fixed by Jaffar S on 30-01-19 (Same parameter Index was set from beginning)
					pstmt.setDouble(2, chargeQty);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (cnt == 0) {
						errCode = "VTFREEQTY";// Chargeable quantity of group of
						// items is not eligible for the free
						// quantity
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						System.out
						.println("Chargeable quantity of group of items is not eligible for the free quantity");
					}
					sql = "select bom.batch_qty,bomdet.qty_per,bomdet.min_qty,bomdet.app_min_qty,bomdet.app_max_qty,bomdet.round,bomdet.round_to from bom, bomdet where bom.bom_code = bomdet.bom_code and	bomdet.bom_code = ? and	bomdet.nature	= ?";
					pstmt = conn.prepareStatement(sql);//---Added by Jaffar S, Missing line on 07-02-19
					pstmt.setString(1, schemeCode);
					//pstmt.setString(1, nature);--------Commented & fixed by Jaffar S on 30-01-19 (Same parameter Index was set from beginning)
					pstmt.setString(2, nature);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						batQty = rs.getDouble("batch_qty");
						qtyPer = rs.getDouble("qty_per");
						minQty = rs.getDouble("min_qty");
						appMinQty = rs.getDouble("app_min_qty");
						appMaxQty = rs.getDouble("app_max_qty");
						round = rs.getString("round");
						roundTo = rs.getDouble("round_to");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (chargeQty >= appMinQty && chargeQty <= appMaxQty) {
						freeQty = roundValue(chargeQty / batQty, 0) * qtyPer;
						System.out.println("freeeeOTY Nandkumar" + freeQty);
					} else {
						freeQty = 0;
					}
					if (freeQty == 0) {
						freeQty = 0;
					}
					if (freeQty > 0) {
						if (round != null && roundTo != 0) {
							freeQty = distCommon.getRndamt(freeQty, round, roundTo);
						}
					}
					if (nature.equals("F")) {
						if ((qty + totFreeQty + unConfTotFreeQty + prvFreeQty + ConfTotFreeQty) > freeQty) {
							errCode = "VTFREEQTY1";// Entered free quantity is
							// greater than scheme's free
							// quantity
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Entered free quantity is greater than scheme's free quantity");
						}
					}
					if (nature.equals("S")) {
						if ((qty + totSampleQty + unConfTotSampleQty + prvSampleQty + ConfTotSampleQty) > freeQty) {
							errCode = "VTSAMPQTY1";// Entered Sample quantity is
							// greater than scheme's Sample
							// quantity
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Entered Sample quantity is greater than scheme's Sample quantity");
						}
					}
				}
			} else {
				try {
					System.out.println("Arun pal testing : currentFormNo :" + currentFormNo);
					sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					if (objContext != null && objContext.trim().length() > 0) {
						currentFormNo = Integer.parseInt(objContext);
						System.out.println("Priyanka testing : currentFormNo :" + currentFormNo);
					}

					itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom2));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
					ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
					// orderDate = sdf.parse(ordDate);
					// ordDate =
					// Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("order_date",
					// dom1), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
					// + " 00:00:00.0");

					orderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					qty = checkIntNull(genericUtility.getColumnValue("quantity", dom2));
					orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
					stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
					countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom2));
					System.out.println("itemCodeOrd==" + itemCodeOrd);
					System.out.println("siteCode==" + siteCode);
					System.out.println("custCode==" + custCode);
					System.out.println("ordDate==" + ordDate);
					System.out.println("orderDate==" + orderDate);
					System.out.println("qty==" + qty);
					System.out.println("orderType==" + orderType);
					System.out.println("stateCodeDlv==" + stateCodeDlv);
					System.out.println("countCodeDlv==" + countCodeDlv);
					System.out.println("saleOrder==" + saleOrder);

					sql = "select product_code from item where item_code = ?  and product_code is not null";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						itemCodeParent = rs.getString("product_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (itemCodeParent == null || itemCodeParent.trim().length() == 0) {
						sql = "select product_code  from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							itemCodeParent = rs.getString("product_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (itemCodeParent == null || itemCodeParent.trim().length() > 0) {
							sql = "select count(1) from item where product_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt > 0) {
								itemCodeParent = itemCodeOrd;
								lbParent = true;
							}
						}
					}
					if (itemCodeParent.trim().length() > 0) {
						if (orderType == null) {
							orderType = "";
						}
						if (siteCode == null) {
							siteCode = "";
						}
						if (stateCodeDlv == null) {
							stateCodeDlv = "";
						}
						if (countCodeDlv == null) {
							countCodeDlv = "";
						}
						schAttr = checkNull(genericUtility.getColumnValue("sch_attr", dom1));
						/*
						 * if(("Y").equalsIgnoreCase(schAttr)) {
						 */
						sql = "select a.scheme_code,prod_sch from scheme_applicability a,scheme_applicability_det  b where a.scheme_code= b.scheme_code and (a.item_code= ? or prod_sch='Y') and a.app_from <= ? and a.valid_upto>= ? and (b.site_code= ? or b.state_code = ?  or b.count_code= ?)";// and
						// prod_sch='Y'";
						pstmt = conn.prepareStatement(sql);
						// pstmt.setString(1, itemCodeParent);
						pstmt.setString(1, itemCodeOrd);
						/*
						 * pstmt.setDate(2, (java.sql.Date) orderDate); pstmt.setDate(3, (java.sql.Date)
						 * orderDate);
						 */
						pstmt.setTimestamp(2, orderDate);
						pstmt.setTimestamp(3, orderDate);
						pstmt.setString(4, siteCode);
						pstmt.setString(5, stateCodeDlv);
						pstmt.setString(6, countCodeDlv);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							schemeCode = rs.getString("scheme_code");
							prodSh=rs.getString("prod_sch");

							System.out.println("In valdataGroupScheme prod_sch::"+prodSh);

							if("Y".equalsIgnoreCase(prodSh) && (schemeCode!=null || schemeCode.trim().length()>0))
							{
								sql = "select count (*) as cnt from sch_pur_items  where SCHEME_CODE =? and item_code=?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, schemeCode);
								pstmt1.setString(2, itemCodeOrd);
								rs1 = pstmt1.executeQuery();

								if (rs1.next()) 
								{
									cnt1 = rs1.getInt("cnt");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;

								if(cnt1==0)
								{

									sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, schemeCode);
									pstmt1.setString(2, itemCodeOrd);
									rs1 = pstmt1.executeQuery();
									if (rs1.next()) 
									{
										cnt1 = rs1.getInt("cnt");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;

									if(cnt1==0)
									{
										continue;
									}

								}


							}
							String sql1 = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, schemeCode);
							rs1 = pstmt1.executeQuery();
							System.out.println("schemeCode excute query " + schemeCode + ']');
							if (rs1.next()) {
								applyCustList = rs1.getString("apply_cust_list");
								noApplyCustList = rs1.getString("noapply_cust_list");
								appOrderType = rs1.getString("order_type");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							System.out.println("apply_cust_list" + applyCustList + "noapply_cust_list" + noApplyCustList
									+ "appOrderType");
							if (orderType.trim() == "NE"
									&& (appOrderType == null || appOrderType.trim().length() == 0)) {
								break;
							} else if (appOrderType != null && appOrderType.trim().length() > 0) {
								System.out.println(" inside while apply_cust_list" + applyCustList + "noapply_cust_list"
										+ noApplyCustList + "appOrderType");
								lbProceed = false;
								lsToken = distCommon.getToken(appOrderType, ",");
								if (orderType.contains(lsToken)) {
									System.out.println("lbProceed" + lbProceed);
									lbProceed = true;
									// break;
								}
								if (!lbProceed) {
									System.out.println("Inside lbproceed");
									continue;
									// goto Nextrec
								}
								/*
								 * lsToken = distCommon.getToken(appOrderType, ","); if
								 * (orderType.trim().length() == lsToken.trim().length()) { lbProceed = true; }
								 * 
								 * } while (appOrderType.trim().length() > 0);
								 */
							}
							System.out.println("prevScheme" + prevScheme + "schemeCode1" + schemeCode1 + "schemeCode1"
									+ schemeCode1 + "schemeCode+" + schemeCode + "]");
							prevScheme = schemeCode1;
							schemeCode1 = schemeCode;
							if (applyCustList.trim().length() > 0) {
								System.out.println("prevScheme" + prevScheme + "applyCustList" + applyCustList + "]");
								schemeCode1 = "";

								applyCust = distCommon.getToken(applyCustList, ",");
								System.out.println("applyCust" + applyCust + "]");
								System.out.println("custCode" + custCode + "]");
								if (applyCust.contains(custCode)) {
									System.out.println("applyCust" + applyCust + "]");

									schemeCode1 = schemeCode;
									custSchemeCode = schemeCode;
									System.out.println("lbProceed" + lbProceed);
									lbProceed = true;
									break;
									// break;
								}
								if (!lbProceed) {
									System.out.println("Inside lbproceed");
									continue;
								}
								/*
								 * do { applyCust = distCommon.getToken(applyCustList, ","); if
								 * (applyCust.trim().length() == custCode.trim().length()) { schemeCode1 =
								 * schemeCode; custSchemeCode = schemeCode; break; } } while
								 * (applyCustList.trim().length() > 0);
								 */
							}
							if (noApplyCustList.trim().length() > 0 && schemeCode != null) {
								applyCust = distCommon.getToken(noApplyCustList, ",");
								if (noApplyCustList.contains(custCode)) {
									schemeCode1 = "";
									// break;
								}
								if (!lbProceed) {
									System.out.println("Inside lbproceed");
									continue;
								}

								/*
								 * do { applyCust = distCommon.getToken(noApplyCustList, ","); if
								 * (noApplyCustList.trim().length() == custCode.trim().length()) { schemeCode1 =
								 * ""; } } while (noApplyCustList.trim().length() > 0);
								 */
							}
							if (schemeCode1 != null) {
								schCnt++;
							} else if (schCnt == 1) {
								System.out.println("schemeCode11515" + schemeCode1);
								schemeCode1 = prevScheme;
							}
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						if (schCnt == 0) {
							errCode = "VTFREEQTY";// Scheme is not applicable for the
							// entered item code
							errList.add(errCode);
							errFields.add(checkNull(" ".toLowerCase()));
							// errFields.add(checkNull(childNodeName.toLowerCase()));
							System.out.println("Scheme is not applicable for the entered item code");
						} else if (schCnt > 1) {
							errCode = "VTITEM10";// Item cannot have more than one
							// scheme applicable for same period.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Item cannot have more than one scheme applicable for same period.");
						} else if (custSchemeCode.trim().length() > 0) {
							schemeCode = custSchemeCode;
						}
						///////// 14-DEC-2017
						String sql1 = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, schemeCode1);// 22-01-18 SCHEME_CODE TO SCHEME_CODE1
						pstmt1.setString(2, itemCodeOrd);

						rs1 = pstmt1.executeQuery();

						if (rs1.next()) {
							cnt1 = rs1.getInt("cnt");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if (cnt1 == 0) {
							errCode = "VTINFEEQTY";// Scheme is not applicable for the
							// entered item code
							errList.add(errCode);
							System.out.println("errList" + errList + "]");
							// errFields.add(childNodeName.toLowerCase());
							System.out.println("invalid free quantity for this item code ");
						}

						///////// 14-DEC-2017

						sql1 = "select app_from, valid_upto from scheme_applicability where scheme_code =?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, schemeCode1);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) {
							appFrom = rs1.getTimestamp("app_from");
							validUpto = rs1.getTimestamp("valid_upto");
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;

						sql1 = "select tot_charge_qty, tot_free_qty " +
								// "tot_sample_qty,tot_bonus_qty " +
								" from prd_scheme_trace where site_code= ? and cust_code	=? "
								+ " and item_code = ? and scheme_code= ? and ? between eff_from and valid_upto";
						System.out.println("Toatal charge qty1" + totChargeQty);
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, siteCode);
						pstmt1.setString(2, custCode);
						pstmt1.setString(3, itemCodeParent);
						pstmt1.setString(4, schemeCode1);
						pstmt1.setTimestamp(5, orderDate);
						// pstmt.setTimestamp(3, orderDate);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) {
							totChargeQty = rs1.getDouble("tot_charge_qty");
							totFreeQty = rs1.getDouble("tot_free_qty");
							// totSampleQty = rs.getDouble("tot_sample_qty");
							// totBonusQty = rs.getDouble("tot_bonus_qty");
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;
						if (totChargeQty == 0) {
							totChargeQty = 0;
						}
						if (totFreeQty == 0) {
							totFreeQty = 0;
						}
						/*
						 * if (totSampleQty == 0) { totSampleQty = 0; } if (totBonusQty == 0) {
						 * totBonusQty = 0; }
						 */ if (minQty == 0) {
							 minQty = 0;
						 }
						 if (saleOrder == null) {
							 saleOrder = "";
						 }
						 if (lbParent == false) {
							 sql1 = " select sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty " +
									 // "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " +
									 // "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty " +
									 " from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
									 + " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
									 + "	and b.item_code__ord in (select item_code from item where product_code = ?)"
									 + " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('F')";

							 pstmt1 = conn.prepareStatement(sql1);
							 pstmt1.setString(1, siteCode);
							 pstmt1.setString(2, custCode);
							 pstmt1.setString(3, saleOrder);
							 pstmt1.setTimestamp(4, orderDate);
							 pstmt1.setTimestamp(5, orderDate);
							 // pstmt1.setTimestamp(4, appFrom);
							 // pstmt1.setTimestamp(5, validUpto);
							 pstmt1.setString(6, itemCodeParent);
							 // pstmt1.setString(7, nature);
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next()) {
								 // unConfTotChargeQty = rs1.getDouble("unconfirmChargeQty");
								 unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
								 System.out.println("unConfTotFreeQty separte free" + unConfTotFreeQty);
								 // unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
								 // unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
							 }
							 pstmt1.close();
							 rs1.close();
							 pstmt1 = null;
							 rs1 = null;

							 sql1 = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty " +
									 // "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " +
									 // "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty " +
									 " from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
									 + " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
									 + "	and b.item_code__ord in (select item_code from item where product_code = ?)"
									 + " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C')";

							 /*
							  * sql1 =
							  * "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty ,"
							  * + " sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty "
							  * +
							  * //"sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, "
							  * +
							  * //"sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty "
							  * +
							  * " from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
							  * +
							  * " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
							  * +
							  * "	and b.item_code__ord in (select item_code from item where product_code = ?)"
							  * +
							  * " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)"
							  * ;
							  */

							 pstmt1 = conn.prepareStatement(sql1);
							 pstmt1.setString(1, siteCode);
							 pstmt1.setString(2, custCode);
							 pstmt1.setString(3, saleOrder);
							 pstmt1.setTimestamp(4, orderDate);
							 pstmt1.setTimestamp(5, orderDate);
							 // pstmt1.setTimestamp(4, appFrom);
							 // pstmt1.setTimestamp(5, validUpto);
							 pstmt1.setString(6, itemCodeParent);
							 // pstmt1.setString(7, nature);
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next()) {
								 unConfTotChargeQty = rs1.getDouble("unconfirmChargeQty");
								 // unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
								 System.out.println("unConfTotChargeQty separte " + unConfTotChargeQty);
								 // unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
								 // unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
							 }
							 pstmt1.close();
							 rs1.close();
							 pstmt1 = null;
							 rs1 = null;

						 } else {
							 sql1 = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty " +
									 // " sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty " +
									 " sum(case when nature ='C' then quantity else 0 end) * rate  as unconfirmChargeValue "+		
									 // "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " +
									 // "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty " +
									 " from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
									 + " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ? "
									 + "	and (b.item_code__ord in (select item_code from item where product_code = ?) or b.item_code__ord in (select item_code from sch_offer_items where scheme_code = ?))"
									 + " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C')";

							 pstmt1 = conn.prepareStatement(sql1);
							 pstmt1.setString(1, siteCode);
							 pstmt1.setString(2, custCode);
							 pstmt1.setString(3, saleOrder);
							 pstmt1.setTimestamp(4, appFrom);
							 pstmt1.setTimestamp(5, validUpto);
							 pstmt1.setString(6, itemCodeParent);
							 pstmt1.setString(7,schemeCode );
							 // pstmt1.setString(7, nature);
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next()) {
								 unConfTotChargeQty = rs1.getDouble("unconfirmChargeQty");
								 //	unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
								 unconfirmChargeValue = rs1.getDouble("unconfirmChargeValue");
								 System.out.println("Toatal charge qty3" + totChargeQty);
								 // unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
								 // unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
							 }
							 pstmt1.close();
							 rs1.close();
							 pstmt1 = null;
							 rs1 = null;

							 sql1 = " select sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty " +
									 // "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " +
									 // "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty " +
									 " from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
									 + " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ? "
									 + "	and b.item_code__ord in (select item_code from item where product_code = ?)"
									 + " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('F')";

							 pstmt1 = conn.prepareStatement(sql1);
							 pstmt1.setString(1, siteCode);
							 pstmt1.setString(2, custCode);
							 pstmt1.setString(3, saleOrder);
							 pstmt1.setTimestamp(4, appFrom);
							 pstmt1.setTimestamp(5, validUpto);
							 pstmt1.setString(6, itemCodeParent);
							 // pstmt1.setString(7, nature);
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next()) {
								 // unConfTotChargeQty = rs1.getDouble("unconfirmChargeQty");
								 unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
								 System.out.println("Toatal charge qty3" + totChargeQty);
								 // unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
								 // unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
							 }
							 pstmt1.close();
							 rs1.close();
							 pstmt1 = null;
							 rs1 = null;
						 }
						 if (unConfTotChargeQty == 0) {
							 unConfTotChargeQty = 0;
						 }
						 if (unConfTotFreeQty == 0) {
							 unConfTotFreeQty = 0;
						 }
						 /*
						  * if (unConfTotSampleQty == 0) { unConfTotSampleQty = 0; } if
						  * (unConfTotBonusQty == 0) { unConfTotBonusQty = 0; }
						  */
						 sql1 = "select sum(case when nature ='C' then quantity else 0 end) - sum(case when nature ='C' then qty_desp else 0 end)as confirmChargeQty,"
								 + " sum(case when nature ='F' then quantity else 0 end)- sum(case when nature ='F' then qty_desp else 0 end) as confirmFreeQty"
								 +
								 // " sum(case when nature ='B' then quantity else 0 end)- sum(case when nature
								 // ='B' then qty_desp else 0 end) as confirmBonusQty," +
								 // " sum(case when nature ='S' then quantity else 0 end)- sum(case when nature
								 // ='S' then qty_desp else 0 end) as confirmSampleQty " +
								 " from sorditem ,SORDER where sorditem.sale_order = SORDER.SALE_ORDER AND sorditem.site_code = ? AND SORDER.cust_code =? "
								 + " and sorditem.sale_order <> ? and sorditem.line_type  <> 'B' and sorditem.order_date between ? and ? "
								 + " and (sorditem.item_code in (select item_code from item where product_code =?) OR sorditem.item_code = ?)"
								 + " and sorditem.nature in ('C' ,?) ";
						 pstmt1 = conn.prepareStatement(sql1);
						 pstmt1.setString(1, siteCode);
						 pstmt1.setString(2, custCode);
						 pstmt1.setString(3, saleOrder);
						 // pstmt1.setTimestamp(4, appFrom);
						 // pstmt1.setTimestamp(5, validUpto);
						 pstmt1.setTimestamp(4, orderDate);
						 pstmt1.setTimestamp(5, orderDate);
						 pstmt1.setString(6, itemCodeParent);
						 pstmt1.setString(7, itemCodeParent);
						 pstmt1.setString(8, nature);
						 rs1 = pstmt1.executeQuery();
						 if (rs1.next()) {
							 System.out.println("Toatal charge qty4" + totChargeQty);
							 ConfTotChargeQty = rs1.getDouble("confirmChargeQty");
							 ConfTotFreeQty = rs1.getDouble("confirmFreeQty");

							 // ConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
							 // ConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
						 }
						 pstmt1.close();
						 rs1.close();
						 pstmt1 = null;
						 rs1 = null;
						 if (ConfTotChargeQty == 0) {
							 ConfTotChargeQty = 0;
						 }
						 if (ConfTotFreeQty == 0) {
							 ConfTotFreeQty = 0;
						 }
						 /*
						  * if (ConfTotSampleQty == 0) { ConfTotSampleQty = 0; } if (ConfTotBonusQty ==
						  * 0) { ConfTotBonusQty = 0; }
						  */
						 /*
						  * System.out.println("currLineNo 15-DEC-17"+currLineNo+""); currLineNo =
						  * checkNull(genericUtility.getColumnValue("line_no", dom)); prvChargeQty = 0;
						  * prvFreeQty = 0; prvBonusQty = 0; prvSampleQty = 0; parentNodeList =
						  * dom.getElementsByTagName("Detail2"); //parentNode = parentNodeList.item(0);
						  * int parentNodeListLength = parentNodeList.getLength(); childNodeList =
						  * parentNode.getChildNodes(); childNodeListLength = childNodeList.getLength();
						  * System.out.println("currLineNo 12121"+currLineNo+""); for (ctr = 0; ctr <
						  * parentNodeListLength; ctr++) {
						  * System.out.println("parentNodeListLength"+parentNodeListLength+"");
						  * parentNode = parentNodeList.item(ctr); //childNode = childNodeList.item(ctr);
						  * //childNodeName = childNode.getNodeName(); lineNo =
						  * checkNull(genericUtility.getColumnValue("line_no", dom2)); nature =
						  * checkNull(genericUtility.getColumnValue("nature", dom2)); browItemCode =
						  * checkNull(genericUtility.getColumnValue("item_code", dom2)); if (currLineNo
						  * != lineNo) { cnt = 0; sql =
						  * "select product_code from item where item_code = ?"; pstmt =
						  * conn.prepareStatement(sql); pstmt.setString(1, browItemCode); rs =
						  * pstmt.executeQuery(); if (rs.next()) { itemCodeParentCurr =
						  * rs.getString("product_code"); System.out.println("itemCodeParentCurr: ====" +
						  * productCodeCurr); } pstmt.close(); rs.close(); pstmt = null; rs = null; if
						  * (itemCodeParentCurr == null || itemCodeParentCurr.trim().length() == 0) { sql
						  * = "select count(1)  from item where product_code = ?	"; pstmt =
						  * conn.prepareStatement(sql); pstmt.setString(1, browItemCode); rs =
						  * pstmt.executeQuery(); if (rs.next()) { cnt = rs.getInt(1); } pstmt.close();
						  * rs.close(); pstmt = null; rs = null; if (cnt > 0) { itemCodeParentCurr =
						  * browItemCode; } } if (itemCodeParentCurr.trim() == itemCodeParentCurr.trim())
						  * { quantity = checkDoubleNull(genericUtility.getColumnValue("quantity",
						  * dom2)); if (nature.equals("F")) { prvFreeQty = prvFreeQty + quantity; } else
						  * if (nature.equals("B")) { prvBonusQty = prvBonusQty + quantity; } else if
						  * (nature.equals("S")) { prvSampleQty = prvSampleQty + quantity; } else {
						  * prvChargeQty = prvChargeQty + quantity; } } } }
						  */

						 ////////// nandkumar

						 ///////////////// end n
						 double rate = checkDoubleNull(genericUtility.getColumnValue("rate", dom));
						 System.out.println("schemeCode1 type @>>>>>>>>PS2::" + rate + "]");

						 sql = "select  scheme_type,purc_base, sch_allowence,discount,PROD_CODE__OFF,PROD_CODE__PUR  from sch_group_def where scheme_code=? ";
						 // "and product_code=?";
						 pstmt = conn.prepareStatement(sql);
						 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 22-01-18
						 // pstmt.setInt(1, itemCodeParent);
						 rs = pstmt.executeQuery();
						 if (rs.next()) {

							 schemeType = rs.getInt("scheme_type");
							 purcBase = rs.getInt("purc_base");
							 schAllowence = rs.getInt("sch_allowence");
							 discount=rs.getDouble("discount");
							 prodCodeOff = rs.getString("PROD_CODE__OFF");
							 prodCodePur = rs.getString("PROD_CODE__PUR");

						 }
						 pstmt.close();
						 rs.close();
						 pstmt = null;
						 rs = null;

						 Node currDetail1 = null;
						 prvChargeQty = 0;
						 prvFreeQty = 0;
						 prvBonusQty = 0;
						 prvSampleQty = 0;
						 int count = 0;
						 currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						 NodeList detailList1 = dom2.getElementsByTagName("Detail2");
						 // System.out.println("Dom print"+genericUtility.serializeDom(dom2));
						 // System.out.println("current dom print"+genericUtility.serializeDom(dom));

						 int noOfDetails = detailList1.getLength();
						 // System.out.println("noOfDetails"+noOfDetails);
						 for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

							 currDetail1 = detailList1.item(ctr1);
							 // System.out.println("noOfDetails inside for loop"+noOfDetails);

							 /*
							  * System.out.println("detItemCode:"+genericUtility.getColumnValueFromNode(
							  * "line_no", currDetail1));
							  * System.out.println("detItemCode:"+genericUtility.getColumnValueFromNode(
							  * "nature", currDetail1));
							  * System.out.println("detItemCode:"+genericUtility.getColumnValueFromNode(
							  * "item_code", currDetail1));
							  */

							 // parentNode = parentNodeList.item(ctr);
							 // childNode = childNodeList.item(ctr);
							 // childNodeName = childNode.getNodeName();
							 lineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", currDetail1));
							 nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
							 browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
							 quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("quantity", currDetail1));
							 schAttr =checkNull(genericUtility.getColumnValueFromNode("sch_attr", currDetail1));


							 /// Nandkumar
							 rate1 = checkDoubleNull(genericUtility.getColumnValueFromNode("rate", currDetail1));
							 System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity1"+quantity);
							 //
							 System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");
							 //nandkumar
							 if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim()) && schAttr.trim().equalsIgnoreCase("Y") && (schemeType != 2) ) {
								 System.out.println("Insideif00000forSchemeType_0andSchemeType1 ");
								 cnt = 0;
								 sql1 = "select product_code from item where item_code = ?";
								 pstmt1 = conn.prepareStatement(sql1);
								 pstmt1.setString(1, browItemCode);
								 System.out.println("browItemCode" + browItemCode + "]");
								 rs1 = pstmt1.executeQuery();
								 if (rs1.next()) {
									 itemCodeParentCurr = rs1.getString("product_code");
									 System.out.println("itemCodeParentCurr: ====" + itemCodeParentCurr);
								 }
								 pstmt1.close();
								 rs1.close();
								 pstmt1 = null;
								 rs1 = null;

								 ///////////
								 /*
								  * System.out.println("itemCodeParentCurr444: ====" + itemCodeParentCurr+"]");
								  * if (itemCodeParentCurr == null || itemCodeParentCurr.trim().length() == 0) {
								  * sql1 = "select count(1)  from item where product_code = ?	"; pstmt1 =
								  * conn.prepareStatement(sql1); pstmt1.setString(1, browItemCode); rs1 =
								  * pstmt1.executeQuery(); if (rs1.next()) { cnt = rs1.getInt(1); }
								  * pstmt1.close(); rs1.close(); pstmt1 = null; rs1 = null; if (cnt > 0) {
								  * itemCodeParentCurr = browItemCode; }
								  */
								 // }
								 System.out.println("itemCodeParentCurr444: ====" + itemCodeParentCurr + "]prodCodeOff"
										 + prodCodeOff + "");
								 /// nandkumar



								 /*	prvRate = prvRate + rate1;*/
								 System.out.println("prvRate: ====" + prvRate + "]prvRate" + prvRate + "");


								 if (prodCodeOff != null && itemCodeParentCurr.trim().equalsIgnoreCase(prodCodeOff.trim())) {
									 System.out.println("insideitemCodeParentCurr555: ====" + itemCodeParentCurr
											 + "]prodCodeOff" + prodCodeOff + "");
									 if (nature.equals("F")) {
										 prvFreeQty = prvFreeQty + quantity;

									 }
									 System.out.println(
											 "prvFreeQty insdie F" + prvFreeQty + "prvChargeQty" + prvChargeQty + "]");
								 }
								 if(prodCodeOff == null)
								 {
									 sql = "select  count(*)  from SCH_OFFER_ITEMS where SCHEME_CODE = ? and ITEM_CODE = ? ";

									 pstmt = conn.prepareStatement(sql);
									 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 29-01-18
									 pstmt.setString(2, browItemCode);
									 rs = pstmt.executeQuery();
									 if (rs.next()) {

										 cntof = rs.getInt(1);
									 }
									 pstmt.close();
									 rs.close();
									 pstmt = null;
									 rs = null;

									 if(cntof > 0)
									 {
										 System.out.println("insideitemCodewithoutproductC: ====" + browItemCode + "");
										 if (nature.equals("F")) {
											 prvFreeQty = prvFreeQty + quantity;

										 }
										 System.out.println(
												 "prvFreeQty insdie F" + prvFreeQty + "prvChargeQty" + prvChargeQty + "]");
									 }

								 }


								 System.out.println("itemCodeParentCurr666: ====" + itemCodeParentCurr + "]prodCodePur"
										 + prodCodeOff + "");
								 if (prodCodePur != null && itemCodeParentCurr.trim().equalsIgnoreCase(prodCodePur.trim())) {
									 System.out.println("insideitemCodeParentCurr777: ====" + itemCodeParentCurr
											 + "]prodCodeOff" + prodCodeOff + "");
									 if (nature.equals("C")) {

										 prvChargeQty = prvChargeQty + quantity;
										 chargeTotamt = chargeTotamt + (quantity * rate1);
										 System.out.println(
												 "chargeTotamt insdie c" + chargeTotamt + "quantity" + quantity + "rate1" + rate1 + "]");


									 }
									 System.out.println(
											 "prvChargeQty insdie c" + prvChargeQty + "prvFreeQty" + prvFreeQty + "]");
								 }
								 if(prodCodePur == null)
								 {
									 sql = "select  count(*)  from SCH_PUR_ITEMS where SCHEME_CODE = ? and ITEM_CODE = ? ";

									 pstmt = conn.prepareStatement(sql);
									 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 29-01-18
									 pstmt.setString(2, browItemCode);
									 rs = pstmt.executeQuery();
									 if (rs.next()) {

										 cntpur = rs.getInt(1);
									 }
									 pstmt.close();
									 rs.close();
									 pstmt = null;
									 rs = null;

									 if(cntpur > 0)
									 {
										 System.out.println("insideitemCodewithoutproductC for chargeQ: ====" + browItemCode + "");

										 if (nature.equals("C")) {

											 prvChargeQty = prvChargeQty + quantity ;
											 chargeTotamt = chargeTotamt + (quantity * rate1);
											 System.out.println(
													 "chargeTotamt insdie c" + chargeTotamt + "quantity" + quantity + "rate1" + rate1 + "]");


										 }
										 System.out.println(
												 "prvChargeQty insdie c" + prvChargeQty + "prvFreeQty" + prvFreeQty + "]");
									 }

								 }	// end Nandkumar 
								 /*chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;
								chargeQty1= chargeQty1 +(chargeQty * rate1); */
							 }

							 // nandkumar-----------start----------------for sch type 2

							 if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim()) && schAttr.trim().equalsIgnoreCase("Y") && schemeType == 2 ) {
								 System.out.println("Inside if schemetype2validation:::::");
								 cnt = 0;
								 sql1 = "select product_code from item where item_code = ?";
								 pstmt1 = conn.prepareStatement(sql1);
								 pstmt1.setString(1, browItemCode);
								 System.out.println("browItemCode" + browItemCode + "]");
								 rs1 = pstmt1.executeQuery();
								 if (rs1.next()) {
									 itemCodeParentCurr = rs1.getString("product_code");
									 System.out.println("itemCodeParentCurr: ====" + itemCodeParentCurr);
								 }
								 pstmt1.close();
								 rs1.close();
								 pstmt1 = null;
								 rs1 = null;

								 System.out.println("itemCodeParentCurr444: ====" + itemCodeParentCurr + "]prodCodeOff"
										 + prodCodeOff + "");




								 System.out.println("prvRate: ====" + prvRate + "]prvRate" + prvRate + "");


								 if (prodCodeOff != null && itemCodeParentCurr.trim().equalsIgnoreCase(prodCodeOff.trim())) {

									 sql = "select  count(*)  from SCH_OFFER_ITEMS where SCHEME_CODE = ? and ITEM_CODE = ? ";

									 pstmt = conn.prepareStatement(sql);
									 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 29-01-18
									 pstmt.setString(2, browItemCode);
									 rs = pstmt.executeQuery();
									 if (rs.next()) {

										 cntof = rs.getInt(1);
									 }
									 pstmt.close();
									 rs.close();
									 pstmt = null;
									 rs = null;
									 if(cntof > 0)
									 {
										 System.out.println("insideitemCodeParentCurr555: ====" + itemCodeParentCurr
												 + "]prodCodeOff" + prodCodeOff + "");
										 if (nature.equals("C")) {
											 prvFreeQty = prvFreeQty + quantity;

										 }
										 System.out.println(
												 "prvFreeQty insdie c" + prvFreeQty + "prvChargeQty" + prvChargeQty + "]");
									 }
								 }
								 if(prodCodeOff == null)
								 {
									 sql = "select  count(*)  from SCH_OFFER_ITEMS where SCHEME_CODE = ? and ITEM_CODE = ? ";

									 pstmt = conn.prepareStatement(sql);
									 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 29-01-18
									 pstmt.setString(2, browItemCode);
									 rs = pstmt.executeQuery();
									 if (rs.next()) {

										 cntof = rs.getInt(1);
									 }
									 pstmt.close();
									 rs.close();
									 pstmt = null;
									 rs = null;

									 if(cntof > 0)
									 {
										 System.out.println("insideitemCodewithoutproductC: ====" + browItemCode + "");
										 if (nature.equals("C")) {
											 prvFreeQty = prvFreeQty + quantity;
											 System.out.println(
													 "prvFreeQty insdie F" + prvFreeQty + "prvChargeQty" + prvChargeQty + "]");

										 }
										 System.out.println(
												 "prvFreeQty insdie F" + prvFreeQty + "prvChargeQty" + prvChargeQty + "]");
									 }

								 }


								 System.out.println("itemCodeParentCurr666: ====" + itemCodeParentCurr + "]prodCodePur"
										 + prodCodeOff + "");
								 if (prodCodePur != null && itemCodeParentCurr.trim().equalsIgnoreCase(prodCodePur.trim())) {
									 System.out.println("insideitemCodeParentCurr777: ====" + itemCodeParentCurr
											 + "]prodCodeOff" + prodCodeOff + "");
									 if (nature.equals("C")) {

										 prvChargeQty = prvChargeQty + quantity;
										 chargeTotamt = chargeTotamt + (quantity * rate1);
										 System.out.println(
												 "chargeTotamt insdie c" + chargeTotamt + "quantity" + quantity + "rate1" + rate1 + "]");


									 }
									 System.out.println(
											 "prvChargeQty insdie c" + prvChargeQty + "prvFreeQty" + prvFreeQty + "]");
								 }
								 if(prodCodePur == null)
								 {
									 sql = "select  count(*)  from SCH_PUR_ITEMS where SCHEME_CODE = ? and ITEM_CODE = ? ";

									 pstmt = conn.prepareStatement(sql);
									 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 29-01-18
									 pstmt.setString(2, browItemCode);
									 rs = pstmt.executeQuery();
									 if (rs.next()) {

										 cntpur = rs.getInt(1);
									 }
									 pstmt.close();
									 rs.close();
									 pstmt = null;
									 rs = null;

									 if(cntpur > 0)
									 {
										 System.out.println("insideitemCodewithoutproductC for chargeQ: ====" + browItemCode + "");

										 if (nature.equals("C")) {

											 prvChargeQty = prvChargeQty + quantity ;
											 chargeTotamt = chargeTotamt + (quantity * rate1);
											 System.out.println(
													 "chargeTotamt insdie c" + chargeTotamt + "quantity" + quantity + "rate1" + rate1 + "]");


										 }
										 System.out.println(
												 "prvChargeQty insdie c" + prvChargeQty + "prvFreeQty" + prvFreeQty + "]");
									 }

								 }	
							 }


						 }
						 //chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;
						 //						chargeTotamt = 
						 System.out.println(
								 "final prvChargeQty insdie c" + prvChargeQty + "prvFreeQty" + prvFreeQty + "]");

						 //////////////////////////////// start by arun for scheme group validation

						 System.out.println("SCHEME QUANTITY @<<<<<<<" + quantity + "]");
						 System.out.println("schemeCode1 type @>>>>>>>>" + schemeCode1 + "]");
						 sql = "select count(1) from SCH_OFFER_ITEMS where scheme_code=? and item_code=? ";
						 pstmt = conn.prepareStatement(sql);
						 pstmt.setString(1, schemeCode1);// scheme_code change to scheme_code 1 22-01-18
						 pstmt.setString(2, itemCodeOrd);
						 // pstmt.setDouble(3, chargeQty);
						 rs = pstmt.executeQuery();
						 if (rs.next()) {

							 cnt = rs.getInt(1);
						 }
						 pstmt.close();
						 rs.close();
						 pstmt = null;
						 rs = null;
						 if (cnt == 0) {
							 errCode = "VTFREEQTY";// Chargeable quantity of group of
							 // items is not eligible for the free
							 // quantity
							 errList.add(errCode);
							 //errFields.add(childNodeName.toLowerCase());
							 System.out.println(
									 "Chargeable quantity of group of items is not eligible for the free quantity");
						 }
						 System.out.println("schemeCode1 type @>>>>>>>>PS1" + schemeCode1 + "]");

						 System.out.println("unConfTotChargeQty " + unConfTotChargeQty + "prvChargeQty " + prvChargeQty
								 + "totChargeQty " + totChargeQty + "ConfTotChargeQty " + ConfTotChargeQty + "]");
						 System.out.println("chargeQty[" + chargeQty + "]");

						 System.out.println(
								 "Scheme type validation <<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>> start:: scheme type:"
										 + schemeType);
						 if (schemeType == 0) {
							 System.out.println("Scheme type validation  type 0 start");
							 System.out.println("schemeType condition 1" + schemeType + "]" + "itemCodeOrd    "
									 + itemCodeOrd + "itemCodeParentCurr   " + itemCodeParentCurr + "]");
							 ////////////////////// ended by arun pal
							 System.out.println("chargeQty" + chargeQty + "]");
							 //nandkumar Start


							 chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;
							 /*
							  * sql =
							  * "Select count(1) From bom Where bom_code = ? And  ? between case when min_qty is null then 0 else min_qty end"
							  * + " And case when max_qty is null then 0 else max_qty end";
							  */
							 sql = "select count(1) from SCH_OFFER_ITEMS where scheme_code=? and item_code=?";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, schemeCode1);//scheme_code change to scheme_code 1 22-01-18
							 pstmt.setString(2, itemCodeOrd);
							 // pstmt.setDouble(3, chargeQty);
							 rs = pstmt.executeQuery();
							 if (rs.next()) {

								 cnt = rs.getInt(1);
							 }
							 pstmt.close();
							 rs.close();
							 pstmt = null;
							 rs = null;
							 if (cnt == 0) {
								 System.out.println("chargeQty1" + chargeQty + "]");

								 errCode = "VTFREEQTY";// Chargeable quantity of group of
								 // items is not eligible for the free
								 // quantity
								 errList.add(errCode);
								 //errFields.add(childNodeName.toLowerCase());
								 System.out.println(
										 "@@@@@@@@@@@@Chargeable quantity of group of items is not eligible for the free quantity");
							 }
							 /*
							  * sql =
							  * "select	bom.batch_qty,bomdet.qty_per,bomdet.min_qty	,bomdet.app_min_qty,bomdet.app_max_qty,bomdet.round	,bomdet.round_to from bom, bomdet where bom.bom_code = bomdet.bom_code and	bomdet.bom_code = ? and	bomdet.nature	= ?"
							  * ; pstmt.setString(1, schemeCode1); pstmt.setString(1, nature); rs =
							  * pstmt.executeQuery(); if (rs.next()) { batQty = rs.getDouble("batch_qty");
							  * qtyPer = rs.getDouble("qty_per"); minQty = rs.getDouble("min_qty"); appMinQty
							  * = rs.getDouble("app_min_qty"); appMaxQty = rs.getDouble("app_max_qty"); round
							  * = rs.getString("round"); roundTo = rs.getDouble("round_to"); } pstmt.close();
							  * rs.close(); pstmt = null; rs = null;
							  */

							 sql = "select purc_base, sch_allowence from sch_group_def where scheme_code = ? and scheme_type = ?";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, schemeCode1);//scheme_code change to scheme_code 1 22-01-18
							 pstmt.setInt(2, schemeType);
							 rs = pstmt.executeQuery();
							 if (rs.next()) {
								 purcBase = rs.getInt("purc_base");
								 schAllowence = rs.getInt("sch_allowence");
							 }
							 pstmt.close();
							 rs.close();
							 pstmt = null;
							 rs = null;
							 if (chargeQty > 0) {
								 freeQty = (chargeQty * schAllowence / purcBase);
							 }
							 System.out.println("chargeQty  after code excuting " + chargeQty + "]");
							 System.out.println("Calcurate free Qty " + freeQty + "]");
							 if (freeQty == 0) {
								 freeQty = 0;
							 }
							 if (freeQty > 0) {
								 if (round != null && roundTo != 0) {
									 freeQty = distCommon.getRndamt(freeQty, round, roundTo);
								 }
							 }
							 System.out.println("qty " + qty + "totFreeQty " + totFreeQty + "unConfTotFreeQty "
									 + unConfTotFreeQty + "prvFreeQty " + prvFreeQty + "]" + "ConfTotFreeQty"
									 + ConfTotFreeQty + "]");
							 System.out.println("inside scheme type @@@@@@@@0000000000");
							 if (nature.equals("F")) {
								 if ((qty + totFreeQty + unConfTotFreeQty + prvFreeQty + ConfTotFreeQty) > freeQty) {
									 errCode = "VTFREEQTY1";// Entered free quantity is
									 // greater than scheme's free
									 // quantity
									 System.out.println("before ....errList.." + errList);
									 errList.add(errCode);
									 System.out.println("after ....errList.." + errList);
									 System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
									 errFields.add(checkNull(" ".toLowerCase()));
									 // errFields.add(checkNull(childNodeName.toLowerCase()));
									 System.out.println(
											 "Entered free quantity is greater than scheme's free quantity" + errFields);
								 }

							 }

							 if (nature.equals("S")) {
								 if ((qty + totSampleQty + unConfTotSampleQty + prvSampleQty
										 + ConfTotSampleQty) > freeQty) {
									 errCode = "VTSAMPQTY1";// Entered Sample quantity is
									 // greater than scheme's Sample
									 // quantity
									 errList.add(errCode);
									 //errFields.add(childNodeName.toLowerCase());
									 System.out.println(
											 "Entered Sample quantity is greater than scheme's Sample quantity");
								 }
							 }

						 } else if (schemeType == 1) {

							 chargeQty1 = unConfTotChargeQty + chargeTotamt + totChargeQty + ConfTotChargeQty;

							 System.out.println("Rate is:" + rate);

							 System.out.println("Scheme type validation  type 1 start");

							 /*
							  * rate = distCommon.pickRate(lsPriceList, ldtDateStr, lsItemCodeOrd, "", "L",
							  * conn);
							  */
							 System.out.println("prvRate @>>>>>>>>" + prvRate + "]");

							 /*chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;

							chargeQty1 = chargeQty * prvRate;*/

							 System.out.println("chargeQty1 @>>>>>>>>" + chargeQty1 + "]");
							 /*
							  * sql =
							  * "Select count(1) From bom Where bom_code = ? And  ? between case when min_qty is null then 0 else min_qty end"
							  * + " And case when max_qty is null then 0 else max_qty end";
							  */
							 sql = "select count(1) from SCH_OFFER_ITEMS where scheme_code=? and item_code=? ";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, schemeCode1);//scheme_code change to scheme_code 1 22-01-18
							 pstmt.setString(2, itemCodeOrd);
							 // pstmt.setDouble(3, chargeQty);
							 rs = pstmt.executeQuery();
							 if (rs.next()) {

								 cnt = rs.getInt(1);
							 }
							 pstmt.close();
							 rs.close();
							 pstmt = null;
							 rs = null;
							 if (cnt == 0) {
								 errCode = "VTFREEQTY";// Chargeable quantity of group of
								 // items is not eligible for the free
								 // quantity
								 errList.add(errCode);
								 //errFields.add(childNodeName.toLowerCase());
								 System.out.println(
										 "Chargeable quantity of group of items is not eligible for the free quantity");
							 }

							 sql = "select purc_base, sch_allowence from sch_group_def where scheme_code=? and scheme_type = ?";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, schemeCode1);//scheme_code change to scheme_code 1 22-01-18
							 pstmt.setInt(2, schemeType);
							 rs = pstmt.executeQuery();
							 if (rs.next()) {
								 purcBase = rs.getInt("purc_base");
								 schAllowence = rs.getInt("sch_allowence");
							 }
							 pstmt.close();
							 rs.close();
							 pstmt = null;
							 rs = null;

							 if (chargeQty1 > 0) {
								 // freeQty = (chargeQty * rate * schAllowence / purcBase);

								 freeQty = (chargeQty1 * schAllowence / purcBase);
							 }
							 System.out.println("chargeQty  after code excuting FOR SCHEME TYPE 1 " + chargeQty + "]");
							 System.out.println("Calcurate free Qty FOR SCHEME TYPE 1 " + freeQty + "]");

							 /*
							  * if (chargeQty >= purcBase && chargeQty <= schAllowence) { freeQty =
							  * roundValue(chargeQty / schAllowence, 0) * purcBase; } else { freeQty = 0; }
							  */
							 if (freeQty == 0) {
								 freeQty = 0;
							 }
							 if (freeQty > 0) {
								 if (round != null && roundTo != 0) {

									 freeQty = distCommon.getRndamt(freeQty, round, roundTo);

									 System.out.println("outside if nature F:: " + freeQty + "]");
								 }
							 }
							 if (nature.equals("F")) {
								 if ((qty + totFreeQty + unConfTotFreeQty + prvFreeQty + ConfTotFreeQty) > freeQty) {
									 System.out.println("inside if nature F:: " + freeQty + "]");

									 errCode = "VTFREEQTY1";// Entered free quantity is
									 // greater than scheme's free
									 // quantity
									 System.out.println("before ....errList.." + errList);
									 errList.add(errCode);
									 System.out.println("after ....errList.." + errList);
									 System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
									 errFields.add(checkNull(" ".toLowerCase()));
									 // errFields.add(checkNull(childNodeName.toLowerCase()));
									 System.out.println(
											 "Entered free quantity is greater than scheme's free quantity" + errFields);

									 /*
									  * System.out.println("inside if nature F:: " + freeQty + "]"); errCode =
									  * "VTFREEQTY1";// Entered free quantity is // greater than scheme's free //
									  * quantity errList.add(errCode); errFields.add(childNodeName.toLowerCase());
									  * System.out.
									  * println("Entered free quantity is greater than scheme's free quantity");
									  */}
							 }
							 if (nature.equals("S")) {
								 if ((qty + totSampleQty + unConfTotSampleQty + prvSampleQty
										 + ConfTotSampleQty) > freeQty) {
									 errCode = "VTSAMPQTY1";// Entered Sample quantity is
									 // greater than scheme's Sample
									 // quantity
									 errList.add(errCode);
									 //errFields.add(childNodeName.toLowerCase());
									 System.out.println(
											 "Entered Sample quantity is greater than scheme's Sample quantity");
								 }
							 }

						 }

						 else if (schemeType == 2) {

							 // Nandkumar ------------Start

							 /*chargeQty1 = unConfTotChargeQty + chargeTotamt + totChargeQty + ConfTotChargeQty;*/

							 System.out.println("Rate is:" + rate);

							 System.out.println("Scheme type validation  type 2 start");

							 System.out.println("prvRate @>>>>>>>>" + prvRate + "]");

							 chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;

							 //chargeQty1 = chargeQty * prvRate;

							 System.out.println("chargeQty @>>>>>>>>" + chargeQty1 + "]");

							 sql = "select count(1) from SCH_OFFER_ITEMS where scheme_code=? and item_code=? ";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, schemeCode1);//scheme_code change to scheme_code 1 22-01-18
							 pstmt.setString(2, itemCodeOrd);
							 // pstmt.setDouble(3, chargeQty);
							 rs = pstmt.executeQuery();
							 if (rs.next()) {

								 cnt = rs.getInt(1);
							 }
							 pstmt.close();
							 rs.close();
							 pstmt = null;
							 rs = null;
							 if (cnt == 0) {
								 errCode = "VTFREEQTY";// Chargeable quantity of group of
								 // items is not eligible for the free
								 // quantity
								 errList.add(errCode);
								 //errFields.add(childNodeName.toLowerCase());
								 System.out.println(
										 "Chargeable quantity of group of items is not eligible for the free quantity");
							 }

							 sql = "select purc_base, sch_allowence from sch_group_def where scheme_code=? and scheme_type = ?";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, schemeCode1);//scheme_code change to scheme_code 1 22-01-18
							 pstmt.setInt(2, schemeType);
							 rs = pstmt.executeQuery();
							 if (rs.next()) {
								 purcBase = rs.getInt("purc_base");
								 schAllowence = rs.getInt("sch_allowence");
							 }
							 pstmt.close();
							 rs.close();
							 pstmt = null;
							 rs = null;
							 System.out.println("sch_allowence" + schAllowence + "purc_base"+purcBase);
							 if (chargeQty > 0) {
								 // freeQty = (chargeQty * rate * schAllowence / purcBase);

								 freeQty = (chargeQty * schAllowence / purcBase);
							 }
							 System.out.println("chargeQty  after code excuting FOR SCHEME TYPE 2 " + chargeQty + "]");
							 System.out.println("Calcurate free Qty FOR SCHEME TYPE 2 " + freeQty + "]");

							 if (freeQty == 0) {
								 freeQty = 0;
							 }
							 if (freeQty > 0) {
								 if (round != null && roundTo != 0) {

									 freeQty = distCommon.getRndamt(freeQty, round, roundTo);

									 System.out.println("outside if nature F:: " + freeQty + "]");
								 }
							 }
							 if (nature.equals("C") && (discount >0) ) {
								 if ((qty + totFreeQty + unConfTotFreeQty + prvFreeQty + ConfTotFreeQty) > freeQty) {
									 System.out.println("inside if nature F:: " + freeQty + "]");

									 errCode = "VTFREEQTY1";// Entered free quantity is
									 // greater than scheme's free
									 // quantity
									 System.out.println("before ....errList.." + errList);
									 errList.add(errCode);
									 System.out.println("after ....errList.." + errList);
									 System.out.println("errFields..[" + errFields + "][" + childNodeName + "]");
									 errFields.add(checkNull(" ".toLowerCase()));
									 // errFields.add(checkNull(childNodeName.toLowerCase()));
									 System.out.println(
											 "Entered free quantity is greater than scheme's free quantity" + errFields);

								 }
							 }
							 if (nature.equals("S")) {
								 if ((qty + totSampleQty + unConfTotSampleQty + prvSampleQty
										 + ConfTotSampleQty) > freeQty) {
									 errCode = "VTSAMPQTY1";// Entered Sample quantity is
									 // greater than scheme's Sample
									 // quantity
									 errList.add(errCode);
									 //errFields.add(childNodeName.toLowerCase());
									 System.out.println(
											 "Entered Sample quantity is greater than scheme's Sample quantity");
								 }
							 }

							 // Nandkumar --------------end --------------

							 /*
							  * chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty +
							  * ConfTotChargeQty;
							  * 
							  * sql =
							  * "Select count(1) From bom Where bom_code = ? And  ? between case when min_qty is null then 0 else min_qty end"
							  * + " And case when max_qty is null then 0 else max_qty end";
							  * 
							  * sql =
							  * "select count(1) from SCH_OFFER_ITEMS where scheme_code=? and item_code ";
							  * pstmt = conn.prepareStatement(sql); pstmt.setString(1, schemeCode);
							  * pstmt.setString(2, itemCodeOrd); // pstmt.setDouble(3, chargeQty); rs =
							  * pstmt.executeQuery(); if (rs.next()) {
							  * 
							  * cnt = rs.getInt(1); } pstmt.close(); rs.close(); pstmt = null; rs = null; if
							  * (cnt == 0) { errCode = "VTFREEQTY";// Chargeable quantity of group of //
							  * items is not eligible for the free // quantity errList.add(errCode);
							  * errFields.add(childNodeName.toLowerCase()); System.out.println(
							  * "Chargeable quantity of group of items is not eligible for the free quantity"
							  * ); } sql =
							  * "select purc_base, sch_allowence, discount from sch_group_def where scheme_code=? and scheme_type=?"
							  * ; pstmt.setString(1, schemeCode1); pstmt.setInt(2, schemeType); rs =
							  * pstmt.executeQuery(); if (rs.next()) {
							  * 
							  * 
							  * purcBase = rs.getInt("purc_base"); schAllowence = rs.getInt("sch_allowence");
							  * 
							  * } pstmt.close(); rs.close(); pstmt = null; rs = null;
							  * 
							  * if (chargeQty >= purcBase && chargeQty <= schAllowence) { freeQty =
							  * roundValue(chargeQty / purcBase, 0) * schAllowence; } else { freeQty = 0; }
							  * if (freeQty == 0) { freeQty = 0; } if (freeQty > 0) { if (round != null &&
							  * roundTo != 0) { freeQty = distCommon.getRndamt(freeQty, round, roundTo); } }
							  * if (nature.equals("F")) { if ((qty + totFreeQty + unConfTotFreeQty +
							  * prvFreeQty + ConfTotFreeQty) > freeQty) { errCode = "VTFREEQTY1";// Entered
							  * free quantity is // greater than scheme's free // quantity
							  * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); System.out.
							  * println("Entered free quantity is greater than scheme's free quantity"); } }
							  * if (nature.equals("S")) { if ((qty + totSampleQty + unConfTotSampleQty +
							  * prvSampleQty + ConfTotSampleQty) > freeQty) { errCode = "VTSAMPQTY1";//
							  * Entered Sample quantity is // greater than scheme's Sample // quantity
							  * errList.add(errCode); errFields.add(childNodeName.toLowerCase());
							  * System.out.println(
							  * "Entered Sample quantity is greater than scheme's Sample quantity"); } }
							  * 
							  */}
						 // }
						 ///////////////// added 13-DEC-17 arun
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Exception ::" + e.getMessage());
					throw new ITMException(e);
				}

				/////////////////////////// added arun 13-DEC-17

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return errCode;
	}

	// gbf_get_scheme_code

	public String priceListDiscount(String siteCode, String custCode, Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String plistDisc = "";
		try {
			sql = "select price_list__disc from site_customer where cust_code =? and site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				plistDisc = rs.getString("price_list__disc");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (plistDisc == null || plistDisc.trim().length() == 0) {
				sql = "select price_list__disc from customer  where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					plistDisc = rs.getString("price_list__disc");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return plistDisc;
	}

	public String priceListSite(String siteCode, String custCode, Connection conn) throws ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String priceList = "";
		try {
			sql = "select price_list  from site_customer 	where cust_code =?  and site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				priceList = checkNull(rs.getString("price_list"));
				System.out.println("price_list from priceListSite====== " + priceList);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (priceList == null || priceList.trim().length() == 0) {
				sql = "select price_list  from customer 	where cust_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					priceList = checkNull(rs.getString("price_list"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return priceList;
	}

	private double roundValue(double round, int scale) {
		return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
	}

	private String errorType(Connection conn, String errorCode) throws ITMException {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}
		return msgType;
	}

	private double round(double round, int scale) throws ITMException {
		return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
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
	
	//added by  Pratiksha A on 09-03-21-- end--
	private String getLineNewNo(String lineNo)	
	{
		lineNo = lineNo.trim();
		System.out.println("lineNo"+lineNo);
		String lenStr = "   " + lineNo ;
		System.out.println("lenStr"+lenStr);
		String lineNoNew = lenStr.substring(lenStr.length() - 3, lenStr.length());

		System.out.println("lineNonew["+lineNoNew+"]");

		return lineNoNew;
	}
	//added by  Pratiksha A on 09-03-21-- end--

	
	private boolean isExist(Connection conn, String tableName, String columnName, String value)
			throws ITMException, RemoteException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		boolean status = false;
		try {
			sql = "SELECT count(*) from " + tableName + " where " + columnName + "  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				if (rs.getBoolean(1)) {
					status = true;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			System.out.println("Exception in isExist ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from isExist ");
		return status;
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

	private static String getAbsString(String str) {
		return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}

	private String checkDouble(String input) {
		if (input == null || input.trim().length() == 0) {
			input = "0";
		}
		return input;
	}

	public double calRate(double discPer, double adRate) {
		if (adRate == 0) {
			adRate = 0;
		}
		if (discPer == 0) {
			discPer = 0;
		}
		adRate = adRate - (discPer * adRate) / 100;
		if (adRate < 0) {
			adRate = 0;
		}
		return adRate;
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
						variencetype = checkNullandTrim(rs.getString(1));
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

	//Added by Sarita to get cust_code_dlv from customer by matching cust code value on 04 APR 2019 [START]
	public String getCustCodeDlv(String custCode , Connection conn) throws ITMException
	{
		String custCodeDlv = "",sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			//Changed by sarita on 05 APR 2019 [START]
			/*sql = "select customer.cust_code__dlv "
					+ "from customer customer , sordform sform " 
					+ "where customer.cust_code = sform.cust_code " 
					+ "and sform.cust_code= ? ";*/
			sql = "select cust_code__dlv from customer where cust_code = ?";
			//Changed by sarita on 05 APR 2019 [END]
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
				custCodeDlv = checkNull(rs.getString("cust_code__dlv"));
			}
			System.out.println("cust_code__dlv ["+custCodeDlv+"]");
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception Inside[SorderForm] Method[getCustCodeDlv]" +e);
			e.printStackTrace();
			throw new ITMException( e );
		}
		return custCodeDlv;
	}


	private StringBuffer customerSerIC(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) throws ITMException 
	{
		System.out.println("--------------Inside itemchange of cust_code------------");					
		String custCode = "", custName="", lsDlvTerm = "", tranMode = "", mbillto = "", reStr = "", mslpers = "",
				mslPers1 = "", mslPers2 = "", mcrTerm = "";
		String lsTaxclasshdr = "", lsCustCode = "", ldtOrderDate = "", lsSiteCodeShip = "",
				lsStationfr = "", lsStationto = "", mCrdescr = "", lsPendingOrder = "";
		String lsItemser = "", lsSiteCode = "", sql = "", descr = "", mcountry = "", orderType = "", lsOrderType = "",
				maddr3 = "", mTransMode = "", mstate = "", lsCrTerm = "";
		String lcOvosAmt = "", lcOsAmt = "", lsContractNo = "", custTaxOpt = "", crTermSource = "",
				lcCreditLmt = "", lsTypeAllowCrLmtList = "", custCodeDlv = "", lsDisIndOrdtypeList = "";
		String lsDescrCl = "", orderTypeLs="";
		double mNum = 0.00;
		int pos = 0,cnt=0;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		String retVal = "";
		String ordTypePrd="";    //addded by manish mhatre on 28-aug-2019
		boolean ordTypeFlag = false; //addded by manish mhatre on 28-aug-2019
		try {

			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
			System.out.println("custCode getting from dom["+custCode+"]");
			lsItemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));


			custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
			orderType = checkNull(genericUtility.getColumnValue("order_type",dom));
			//if condition added to check if initially null from dom
			if(orderType == null || orderType.trim().length() == 0)
			{
				orderType = orderTypeLs;						
			}
			if(custCodeDlv == null || custCodeDlv.trim().length() == 0)
			{
				custCodeDlv = custCode;
			}
			System.out.println("6244orderType::["+orderType+"]custCodeDlv["+custCodeDlv+"]");
			sql = "select cust_tax_opt,cr_term_source from sordertype where order_type = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, orderType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				custTaxOpt = checkNull(rs.getString("cust_tax_opt"));

				crTermSource = checkNull(rs.getString("cr_term_source"));

			}				

			if(rs != null)
			{
				rs.close();
				rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			sql = "select cust_code__bil, cust_name, order_type from customer where cust_code =? ";
			System.out.println("sql["+sql+"]");
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {						
				mbillto = checkNull(rs.getString("cust_code__bil"));
				custName = checkNull(rs.getString("cust_name"));
				orderTypeLs = checkNull(rs.getString("order_type"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select sales_pers , sales_pers__1 	, sales_pers__2 from customer_series where cust_code =? and item_ser =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, lsItemser);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				mslpers = checkNull(rs.getString("sales_pers"));
				mslPers1 = checkNull(rs.getString("sales_pers__1"));
				mslPers2 = checkNull(rs.getString("sales_pers__2"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//addded by manish mhatre on 28-aug-2019 [For set cr_term_np when enter order type ]
			//start manish
			lsOrderType=checkNull(genericUtility.getColumnValue("order_type",dom));
			ordTypePrd = distCommon.getDisparams("999999","ORD_TYPE_NEWPRD",conn);
			ordTypePrd = ordTypePrd == null || ordTypePrd.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(ordTypePrd) ? " " :ordTypePrd.trim();

			if (ordTypePrd.trim().length() > 0 )
			{
				String ordTypeArr[] = ordTypePrd.split(",");
				for(int i = 0; i<ordTypeArr.length; i++)
				{
					if (lsOrderType.trim().equals(ordTypeArr[i].trim()))
					{
						ordTypeFlag=true;
					}
				}
			}
			System.out.println("Inside OrderType itemchanged..."+lsOrderType);
			System.out.println("Inside OrderType itemchanged flag"+ordTypeFlag);

			if(lsOrderType != null && ordTypeFlag) {

				sql = "select cr_term__np from customer_series where cust_code =? and item_ser  = ? ";
				pstmt = conn.prepareStatement(sql);

				if((crTermSource != null ) && ("B".equalsIgnoreCase(crTermSource)))
				{
					pstmt.setString(1, mbillto);
				}
				else if((crTermSource != null ) && ("D".equalsIgnoreCase(crTermSource)))
				{
					pstmt.setString(1, custCodeDlv);
				}
				else 
				{
					pstmt.setString(1, custCode);
				}
				pstmt.setString(2, lsItemser);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mcrTerm = rs.getString("cr_term__np");
				}						
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if (mcrTerm == null || mcrTerm.trim().length() == 0) {
					sql = "select cr_term__np from customer where cust_code = ? ";
					pstmt = conn.prepareStatement(sql);

					if((crTermSource != null ) && ("B".equalsIgnoreCase(crTermSource)))
					{
						pstmt.setString(1, mbillto);
					}
					else if((crTermSource != null ) && ("D".equalsIgnoreCase(crTermSource)))
					{
						pstmt.setString(1, custCodeDlv);
					}
					else 
					{
						pstmt.setString(1, custCode);
					}


					rs = pstmt.executeQuery();
					if (rs.next()) {
						mcrTerm = rs.getString("cr_term__np");
					}

					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				}


			}
			else
			{   //end manish
				lsDisIndOrdtypeList = distCommon.getDisparams("999999", "IND_ORD_TYPE", conn);
				boolean lbOrdFlag = false;
				String lsDisIndOrdtypeListArr[] = lsDisIndOrdtypeList.split(",");
				if (lsDisIndOrdtypeListArr.length > 0) {
					for (int i = 0; i < lsDisIndOrdtypeListArr.length; i++) {
						if (lsOrderType.equalsIgnoreCase(lsDisIndOrdtypeListArr[i])) {
							lbOrdFlag = true;
						}
					}
				}
				System.out.println("lbOrdFlag...6353["+lbOrdFlag+"]");
				if (lbOrdFlag) {

					sql = "select cr_term from customer_series where cust_code = ? and item_ser= ?";
					pstmt = conn.prepareStatement(sql);

					if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
					{
						pstmt.setString(1, mbillto);
					}
					else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
					{
						pstmt.setString(1, custCodeDlv);
					}
					else
					{
						pstmt.setString(1, custCode);
					}


					pstmt.setString(2, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mcrTerm = rs.getString("cr_term");
					}

					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}


					if (mcrTerm == null || mcrTerm.trim().length() == 0) {
						sql = "select cr_term from customer where cust_code =  ?";
						pstmt = conn.prepareStatement(sql);

						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, mbillto);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else
						{
							pstmt.setString(1, custCode);
						}							

						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else {	

					sql = "select cr_term from customer_series where cust_code = ? and item_ser= ?";
					pstmt = conn.prepareStatement(sql);

					if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
					{
						pstmt.setString(1, mbillto);
					}
					else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
					{
						pstmt.setString(1, custCodeDlv);
					}
					else
					{
						pstmt.setString(1, custCode);
					}						

					pstmt.setString(2, lsItemser);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mcrTerm = rs.getString("cr_term");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (mcrTerm == null || mcrTerm.trim().length() == 0) {
						sql = "select cr_term from customer where cust_code =  ?";
						pstmt = conn.prepareStatement(sql);

						if((crTermSource != null) && ("B".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, mbillto);
						}
						else if((crTermSource != null) && ("D".equalsIgnoreCase(crTermSource)))
						{
							pstmt.setString(1, custCodeDlv);
						}
						else
						{
							pstmt.setString(1, custCode);
						}							

						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcrTerm = rs.getString("cr_term");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
			}
			if (mslpers == null || mslpers.trim().length() == 0) {
				sql = "select sales_pers from customer where cust_code =  ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mslpers = checkNull(rs.getString("sales_pers"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (mslPers1 == null || mslPers1.trim().length() == 0) {
				sql = "select sales_pers__1 from customer where cust_code =   ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mslPers1 = checkNull(rs.getString("sales_pers__1"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (mslPers2 == null || mslPers2.trim().length() == 0) {
				sql = "select sales_pers__2 from customer where cust_code =   ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mslPers2 = checkNull(rs.getString("sales_pers__2"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}


			sql = "select order_type from customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsOrderType = checkNull(rs.getString("order_type"));

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("lsCrTerm"+lsCrTerm);
			sql = "SELECT CR_TERM_MAPPING.CR_TERM_MAP FROM CR_TERM_MAPPING"
					+ " WHERE ( CR_TERM_MAPPING.CR_TERM = ? ) AND ( CR_TERM_MAPPING.ORD_TYPE = ?) ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mcrTerm);
			pstmt.setString(2, lsOrderType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsCrTerm = rs.getString("CR_TERM_MAP");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (lsCrTerm != null && lsCrTerm.trim().length() > 0) {
				mcrTerm = lsCrTerm;
			}
			valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrTerm + "]]>").append("</cr_term>");
			setNodeValue(dom, "cr_term", getAbsString(mcrTerm));

			sql = "select descr from crterm where cr_term =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mcrTerm);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				descr = rs.getString("descr");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			valueXmlString.append("<crterm_descr>").append("<![CDATA[" + descr + "]]>")
			.append("</crterm_descr>");
			setNodeValue(dom, "crterm_descr", getAbsString(descr));

			lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
			lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
			ldtOrderDate = checkNull(genericUtility.getColumnValue("order_date", dom));



			lsSiteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));

			sql = "select stan_code from site where site_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSiteCodeShip);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsStationfr = rs.getString("stan_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select stan_code from customer where cust_code =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsStationto = rs.getString("stan_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select descr from crterm where cr_term =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mcrTerm);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				mCrdescr = rs.getString("descr");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			valueXmlString.append("<cr_term>").append("<![CDATA[" + mcrTerm + "]]>").append("</cr_term>");
			setNodeValue(dom, "cr_term", getAbsString(mcrTerm));

			valueXmlString.append("<crterm_descr>").append("<![CDATA[" + mCrdescr + "]]>")
			.append("</crterm_descr>");
			setNodeValue(dom, "crterm_descr", getAbsString(mCrdescr));


			valueXmlString.append("<sales_pers>").append("<![CDATA[" + mslpers + "]]>").append("</sales_pers>");
			setNodeValue(dom, "sales_pers", getAbsString(mslpers));

			if (mslpers != null && mslpers.trim().length() > 0) {
				reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail1>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail1>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);
			}
			valueXmlString.append("<sales_pers__1>").append("<![CDATA[" + mslPers1 + "]]>")
			.append("</sales_pers__1>");
			setNodeValue(dom, "sales_pers__1", getAbsString(mslPers1));

			if (mslPers1 != null && mslPers1.trim().length() > 0) {
				reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__1", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail1>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail1>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);
			}
			valueXmlString.append("<sales_pers__2>").append("<![CDATA[" + mslPers2 + "]]>")
			.append("</sales_pers__2>");
			setNodeValue(dom, "sales_pers__2", getAbsString(mslPers2));

			if (mslPers2 != null && mslPers2.trim().length() > 0) {
				reStr = itemChangedHdr(dom, dom1, dom2, objContext, "sales_pers__2", editFlag, xtraParams);
				pos = reStr.indexOf("<Detail1>");
				reStr = reStr.substring(pos + 9);
				pos = reStr.indexOf("</Detail1>");
				reStr = reStr.substring(0, pos);
				valueXmlString.append(reStr);
			}
			sql = "select count(*)as cnt from customer_series where cust_code = ? and 	item_ser  =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, lsItemser);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt("cnt");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (cnt > 0) {
				sql = "select pending_order from customer_series where cust_code =? and 	item_ser  =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, lsItemser);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsPendingOrder = rs.getString("pending_order");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (lsPendingOrder == null || lsPendingOrder.trim().length() == 0) {
				sql = "select pending_order from customer where cust_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsPendingOrder = rs.getString("pending_order");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			//Pavan R 04FEB2019 [if pending_order in customer master is null then consider as 'N'].
			if(lsPendingOrder == null || lsPendingOrder.trim().length() == 0 || "null".equals(lsPendingOrder) )
			{
				lsPendingOrder = "N";
			}	



			boolean lbOrdFlag=false;   //added by manish mhatre on 29-aug-2019
			lsTypeAllowCrLmtList = distCommon.getDisparams("999999", "TYPE_ALLOW_CR_LMT", conn);
			if (lsTypeAllowCrLmtList == null || lsTypeAllowCrLmtList.trim().length() == 0
					|| lsTypeAllowCrLmtList.equalsIgnoreCase("NULLFOUND")) {
				lbOrdFlag = false;
			} else {
				String lsTypeAllowCrLmt[] = lsTypeAllowCrLmtList.split(",");
				for (int i = 0; i < lsTypeAllowCrLmt.length; i++) {
					if (lsOrderType.equalsIgnoreCase(lsTypeAllowCrLmt[i])) {
						lbOrdFlag = true;
						break;
					}
				}
			}
			if (lbOrdFlag) {
				sql = "select CREDIT_LMT from customer_series where cust_code = ? and 	item_ser  = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				pstmt.setString(2, lsItemser);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcCreditLmt = rs.getString("CREDIT_LMT");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} else {
				sql = "select credit_lmt from customer where cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcCreditLmt = rs.getString("credit_lmt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}


			valueXmlString.append("<cr_lmt>").append("<![CDATA[" + lcCreditLmt + "]]>").append("</cr_lmt>");
			setNodeValue(dom, "cr_lmt", getAbsString(lcCreditLmt));

			valueXmlString.append("<pending_order>").append("<![CDATA[" + lsPendingOrder + "]]>")
			.append("</pending_order>");
			setNodeValue(dom, "pending_order", getAbsString(lsPendingOrder));


			String ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));




			sql = "select dlv_term from   customer_series where  cust_code = ? and    item_ser = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsCustCode);
			pstmt.setString(2, lsItemser);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsDlvTerm = rs.getString("dlv_term");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (lsDlvTerm == null || lsDlvTerm.trim().length() == 0) {
				sql = "select dlv_term from   customer where  cust_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsDlvTerm = rs.getString("dlv_term");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (lsDlvTerm == null || lsDlvTerm.trim().length() == 0) {
				lsDlvTerm = "NA";
			}
			valueXmlString.append("<dlv_term>").append("<![CDATA[" + lsDlvTerm + "]]>").append("</dlv_term>");
			setNodeValue(dom, "dlv_term", getAbsString(lsDlvTerm));

			Timestamp orderDate = Timestamp.valueOf(
					genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			lsContractNo = checkNull(genericUtility.getColumnValue("contract_no", dom));
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].Start
			if(lsContractNo != null && "null".equalsIgnoreCase(lsContractNo.trim()))
			{
				lsContractNo = null;
			}
			// Modified by Piyush on 19/10/2020 [To consider null word as null value].End
			if (lsContractNo == null || lsContractNo.trim().length() == 0) 
			{
				lsContractNo = getContractHrd(lsSiteCode, lsCustCode, orderDate, lsItemser, conn);
				valueXmlString.append("<contract_no>").append("<![CDATA[]]>").append("</contract_no>");
			}
			valueXmlString.append("<site_code__ship>").append("<![CDATA[" + lsSiteCode + "]]>")
			.append("</site_code__ship>");
			setNodeValue(dom, "site_code__ship", getAbsString(lsSiteCode));

			if (lbOrdFlag) {
				sql = "select fn_get_cust_series(?, ?,?, 'T') as lc_os_amt from dual";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				pstmt.setString(2, lsSiteCode);
				pstmt.setString(3, lsItemser);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcOsAmt = rs.getString("lc_os_amt");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				valueXmlString.append("<os_amt>").append("<![CDATA[" + lcOsAmt + "]]>").append("</os_amt>");
				setNodeValue(dom, "os_amt", getAbsString(lcOsAmt));

				sql = "select fn_get_cust_series(?,?,?, 'O') as lc_ovos_amt from dual";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				pstmt.setString(2, lsSiteCode);
				pstmt.setString(3, lsItemser);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcOvosAmt = rs.getString("lc_ovos_amt");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
			} else {
				sql = "select fn_get_custos(?,?, 'T') as lc_os_amt from dual";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				pstmt.setString(2, lsSiteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcOsAmt = rs.getString("lc_os_amt");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				valueXmlString.append("<os_amt>").append("<![CDATA[" + lcOsAmt + "]]>").append("</os_amt>");
				setNodeValue(dom, "os_amt", getAbsString(lcOsAmt));

				sql = "select fn_get_custos(?,?, 'O') as lc_ovos_amt from dual";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsCustCode);
				pstmt.setString(2, lsSiteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcOvosAmt = rs.getString("lc_ovos_amt");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
			}
			valueXmlString.append("<ovos_amt>").append("<![CDATA[" + lcOvosAmt + "]]>").append("</ovos_amt>");
			setNodeValue(dom, "ovos_amt", getAbsString(lcOvosAmt));



		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added BY Mukesh Chauhan on 06/08/19
		} catch (ITMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e; //Added By Mukesh Chauhan on 05/08/19
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}
	//Added by Sarita to get cust_code_dlv from customer by matching cust code value on 04 APR 2019 [END]
}