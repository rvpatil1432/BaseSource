/********************************************************
	Title : CustDiscApprvIC [Mukesh Chauhan]
	Date  : 16 - APR - 2020

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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import ibase.webitm.ejb.sys.UtilMethods;
import javax.ejb.Stateless;

import org.json.simple.parser.JSONParser;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class CustDiscApprvIC extends ValidatorEJB implements CustDiscApprvICLocal,CustDiscApprvICRemote
{

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
		Timestamp TranDate = null;
		Node parentNode = null;
		Node childNode = null;
		NodeList parentNodeList1 = null;
		NodeList childNodeList1 = null;
		Node parentNode1 = null;
		Node childNode1 = null;
		// String columnValue = null;
		String childNodeName = null;
		String childNodeName1 = null;
		String tranDateStr="";
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

		FinCommon FinCommon = new FinCommon();
		DistCommon DistCommon = new DistCommon();



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
		String custCode="";
		String errCode = "";
		String scCode="";
		String discntType = "";
		String applBasis = "";
		String offerDate = "";
		String marginPerc = "";
		String discPerc = "";
		String effFromstr = "";
		String validUptostr="";
		String lotNoFrom = "";
		String lotNoTo = "";
		String contractValue = "";
		String validUptoStr="";
		String effFromString="";
		String validuptostr="";
		String tranId="",discType="";
		double discpercval = 0.0;
		double BillToMarginPerc = 0.0;
		int contractVal = 0;
		PreparedStatement pstmt = null, pstmt1 = null;
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
		Timestamp validUpto=null;
		Timestamp effFrom =null;
		Timestamp ordDate2 = null, ordDated2 = null;

		Timestamp reqDateTm = null, ordDateTm = null, dlvDateTm = null;

		String queryStdoum = "";
        String InstCode="";
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try {
			System.out.println("wfValData called");
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
					if (childNodeName.equalsIgnoreCase("cust_code")) {
						custCode = genericUtility.getColumnValue("cust_code", dom);
						scCode = genericUtility.getColumnValue("sc_code", dom);
					
						if (scCode == null) {

							if(custCode == null )
							{
								errCode = "NULSTRGCOD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							if(custCode != null && custCode.trim().length() > 0)
							{
								sql = " Select Count(*) cnt from customer where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0) {
									errCode = "VTCUSTNEX";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					    if ((scCode != null) && (custCode != null))
						{
					    	errCode = "VTINVALDET";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					    
					}
					if (childNodeName.equalsIgnoreCase("sc_code")) {
						
						scCode = genericUtility.getColumnValue("sc_code", dom);
						custCode = genericUtility.getColumnValue("cust_code", dom);
						
						if (custCode == null ) {

							if(scCode == null)
							{
							errCode = "VTSCCDNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
							}
							if(scCode != null && scCode.trim().length() > 0)
							{
							sql = " Select Count(*) cnt from strg_customer where sc_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, scCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
							cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
							errCode = "VTSCCODE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							}
							}
							}
						if ((scCode != null) && (custCode != null))
						{
					    	errCode = "VTINVALDET";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
					}
					if (childNodeName.equalsIgnoreCase("discount_type")) {
						
						discntType = genericUtility.getColumnValue("discount_type", dom);
						
						if (discntType == null) {
							errCode = "VTDISCTYP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					if (childNodeName.equalsIgnoreCase("appl_basis")) {
						applBasis = genericUtility.getColumnValue("appl_basis", dom);
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						System.out.println("Item Code In Appl Basis:"+itemCode);
						if (applBasis == null) {
							errCode = "VTAPLBASIS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if ("I".equalsIgnoreCase(applBasis))
                		{
                			if(itemCode == null)
                			{
                				errCode = "VTEMTITM";
    							errList.add(errCode);
    							errFields.add(childNodeName.toLowerCase());
                			}
                		}

					}

					if (childNodeName.equalsIgnoreCase("item_ser")) {
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						
						if (itemSer == null) {
							errCode = "NULITEMSER";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (itemSer != null && itemSer.trim().length() == 0 ) {
							errCode = "NULITEMSER";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (itemSer != null && itemSer.trim().length() > 0) {
							sql = "select count(*) from itemser where item_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) {
								errCode = "VMITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("price_list")) {
						priceList = genericUtility.getColumnValue("price_list", dom);
						discntType = genericUtility.getColumnValue("discount_type", dom);
						
							if ("P".equalsIgnoreCase(discntType)) { //uncommented by kailasG on 26-march-21 for  not require validation on price list in special rate case

								if (priceList == null) {
									
									errCode = "VMPRLNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									
								} 
								else
								{
									sql = " Select Count(*) cnt from pricelist_mst where price_list = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, priceList);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										cnt = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt == 0) 
									{
										errCode = "VTPLIST1"; 
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							 /*
							else
							{
							if (priceList != null ) {	
							    errCode = "VTPRICELST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							}*/
							
						
						
					}
					if (childNodeName.equalsIgnoreCase("offer_date")) {
						offerDate = genericUtility.getColumnValue("offer_date", dom);
						
						if (offerDate == null) {
							errCode = "VTOFFDATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (offerDate != null && offerDate.trim().length() == 0 ) {
							errCode = "VTOFFDATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("bill_to_margin_perc")) {
						marginPerc = genericUtility.getColumnValue("bill_to_margin_perc", dom);
						discType = checkNull(genericUtility.getColumnValue("discount_type", dom));
						
						if (marginPerc != null && marginPerc.trim().length() > 0) {
							BillToMarginPerc = Double.parseDouble(marginPerc.trim());
						
							if("R".equalsIgnoreCase(discType)) {/*// commented by kailasG on 26-march-21 in which not required validation on bill to margin percentage  in special rate case.
							if (BillToMarginPerc <= 0) {
								errCode = "VTMARPERC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							*/}
						}
					}
					if (childNodeName.equalsIgnoreCase("disc_perc")) {
						
						discPerc = genericUtility.getColumnValue("disc_perc", dom);
						discType = checkNull(genericUtility.getColumnValue("discount_type", dom));
						
						
						if (discPerc != null && discPerc.trim().length() > 0) {
							discpercval = Double.parseDouble(discPerc.trim());
							System.out.println("disc perc val>>>>>>>>>>>>>>>>>>>>>>>>>451" + discpercval);
						}
						if(!"R".equalsIgnoreCase(discType))
						{
							if (discpercval <= 0) {
								errCode = "VTDISPERC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("eff_from")) {
						
						if(genericUtility
								.getColumnValue("eff_from", dom)!=null)
						{
							        effFromString=genericUtility.getValidDateString(genericUtility
									.getColumnValue("eff_from", dom),genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())+ " 00:00:00.0";
							effFrom = effFromString==null?null:Timestamp.valueOf(effFromString);
							
						}
						System.out.println("effFrom Date>>>>>>>>>>>>>>>>>>>>>>>>>473" + effFrom);
						if (effFrom == null) {
							errCode = "VMSTDPAY07";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
						

					}
					if (childNodeName.equalsIgnoreCase("valid_upto")) {
						
						if(genericUtility
								.getColumnValue("valid_upto", dom)!=null)
						{
							validuptostr=genericUtility.getValidDateString(genericUtility
									.getColumnValue("valid_upto", dom),genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())+ " 00:00:00.0";
							validUpto = validuptostr==null?null:Timestamp.valueOf(validuptostr);
							
						}
						
						if(genericUtility
								.getColumnValue("eff_from", dom)!=null)
						{
							        effFromString=genericUtility.getValidDateString(genericUtility
									.getColumnValue("eff_from", dom),genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())+ " 00:00:00.0";
							effFrom = effFromString==null?null:Timestamp.valueOf(effFromString);
							
						}						
						
						
						if (validUpto == null) {
							errCode = "VMTRVVALID";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
						if (validUpto != null  && effFrom != null ) {
							if (validUpto.compareTo(effFrom) < 0) {
								errCode = "VTVLDUPTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							tranId = genericUtility.getColumnValue("tran_id",dom);
							System.out.println("tran   Id>>>>"+tranId);
							if(tranId==null || tranId.trim().length()==0)
							{
							tranId=" ";
							}
							else
							{
							tranId=tranId;
							}
							System.out.println("tran   Id>>>>before status"+tranId);
							status = genericUtility.getColumnValue("status",dom);
		                	System.out.println("status>>>> 15"+status);
							if("O".equalsIgnoreCase(status)) // added by kailasg on 3-feb-2021 [After closing, system allow to create new master with same customer and same period] added for consider item code from detail on 23-march 
							{
								/*sql ="select COUNT(*) as cnt from DISC_APR_STRG i,DISC_APR_STRG_det j where (i.cust_code=?  or i.sc_code=?) and i.item_ser =? and j.item_code=? and i.tran_id <> ? "
								+ "and  ? between  EFF_FROM and valid_upto and status = 'O'";*/
						
						sql ="select COUNT(*) as cnt from DISC_APR_STRG where (cust_code=?  or sc_code=?) and item_ser =? and tran_id <> ? "
								+ "and  ? between  EFF_FROM and valid_upto and status = 'O'";
							
								System.out.println("Loading Sql [" + sql+"] validUpto ["+validUpto+"]");
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, scCode);
								pstmt.setString(3, itemSer);
								//pstmt.setString(4, itemCode);
								pstmt.setString(4, tranId);
								pstmt.setTimestamp(5, validUpto);
								rs = pstmt.executeQuery();
								System.out.println("Loading executeQuery"); 
								if (rs.next()) {
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt > 0) 
								{
									errCode = "VMDUPDTA"; 
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
						     }
						}
					}
					if (childNodeName.equalsIgnoreCase("contract_value")) {
						contractValue = genericUtility.getColumnValue("contract_value", dom);
						
						if (contractValue != null && contractValue.trim().length() > 0) {
							contractVal = Integer.parseInt(contractValue.trim());
							
						}
						if (contractVal <= 0) {
							errCode = "VTCONTVAL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					 if (childNodeName.equalsIgnoreCase("site_code"))//addded by kailasg on 31-march 
					{
						siteCode = this.genericUtility.getColumnValue("site_code", dom);
						cnt=0;
						sql = "select count(*) from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
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
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
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
				NodeList itemNodeList = null, lineNoList = null, detail2List = null, childDetilList = null;
				Node itemNode = null, lineNoNode = null, detailNode = null, chidDetailNode = null;
				String uniqueItem = "", itemCode1 = "", pOrderType = "";
				String lineValue = "", updateFlag = "";
				int lineNoInt = 0, lineValueInt = 0;
				int cnt1 = 0;
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("item_code")) {
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						applBasis = genericUtility.getColumnValue("appl_basis", dom1);
						System.out.println("item Code>>>>>>>>>>>>>>>>>>>>>>>>>525" + itemCode);
                        System.out.println("Appl Basis In Item Code>>>>>>>"+applBasis);
                        if (itemCode != null && itemCode.trim().length() > 0)
                        {
                        	if ("C".equalsIgnoreCase(applBasis)) 
    						{
    							 errCode = "VTITMVAL";
    							 errList.add(errCode);
    							 errFields.add(childNodeName.toLowerCase());
    							
    						}
                        	else
                        	{
        							sql = " Select Count(*) cnt from item where item_code = ? ";
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
        								errCode = "STKVALITNE";
        								errList.add(errCode);
        								errFields.add(childNodeName.toLowerCase());
        							}
                        	}
         
                        }
                        else 
                		{
                        		errCode = "VTITMCNN";
    							errList.add(errCode);
    							errFields.add(childNodeName.toLowerCase());	
						}
                        
					}
					
					if (childNodeName.equalsIgnoreCase("lot_no__from")) {
						lotNoFrom = checkNull(genericUtility.getColumnValue("lot_no__from", dom));
						
						if (lotNoFrom == null ) {
							errCode = "VTLOTNOFRM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (lotNoFrom != null && lotNoFrom.trim().length() == 0 ) {
							errCode = "VTLOTNOFRM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
					if (childNodeName.equalsIgnoreCase("lot_no__to")) {
						lotNoTo = checkNull(genericUtility.getColumnValue("lot_no__to", dom));
						lotNoFrom = checkNull(genericUtility.getColumnValue("lot_no__from", dom));
						
						if (lotNoTo == null ) {
							errCode = "VTLOTNOTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (lotNoTo != null && lotNoTo.trim().length() == 0 ) {
							errCode = "VTLOTNOTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (lotNoTo != null && lotNoTo.trim().length() > 0 && lotNoFrom != null && lotNoFrom.trim().length() > 0 ){
							
							if (lotNoTo.compareTo(lotNoFrom) < 0)//change by mukesh chauhan on 27/05/2020
							 { 
								errCode = "VTLOTNOCK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}
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
		ResultSet rs = null;
		ResultSet rsNew = null;
		Timestamp TranDate=null;
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
		String currCodeFr = ""; 
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
		String custCode="";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		Node childNode = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
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

		String ls_cont = "", ls_type = "", ls_sitecode = "", ls_ind_no = "", ls_item_ser = "", item_code__mfg = "";
		String mloc = "", ls_invacct = "", errstr = "", ls_qcreqd = "", mloc__aprv = "", mloc__insp = "";
		String ls_unitpur = "", ls_pack1 = "", ls_packinstr1 = "", ls_itemser = "", ls_rate_unit = "",
				ls_emp_code__qcaprv = "";
		String ls_empfname = "", ls_empmname = "", ls_emplname = "", ls_supp_code__mnfr = "", ls_item_code = "",
				ls_taxclasshdr = "", ls_taxchaphdr = "";
		String ldt_orddateStr = "", ls_pricelist = "", ls_unitstd = "";
		ArrayList lc_qtystduom = null;
		
		ArrayList lc_ratestduomArrayList = new ArrayList();
		
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
		String childNodeName = null;
        String custDescr="";
        String custName="";
        String tranDateStr="";
		// cpatil end

		Timestamp ordDate2 = null;
		System.out.println("Inside itmchange>>>>"+custCode);
		String queryStdoum = "";
		//nandkumar
		String discType="";
		String retailType="",instCode="";// add by kailas gaikwad on 21/12/2020
		double margPer=0;
		String chguserhdr="";
		String chgtermhdr="";
        String siteDescr="";
		try {
			DistCommon distCommon = new DistCommon();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			this.finCommon = new FinCommon();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			chguserhdr = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}
					ctr++;
				} while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN [" + currentColumn + "]");
				/*
				 * if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) {
				 * 
				 * } else
				 */ 
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					if (loginSite != null || loginSite.trim().length() == 0)//added by kailasg on 26-march-21 for add sitecode in detail suggeste by SP sir
						
					{	
						
						sql = "select descr from site where site_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, loginSite);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							siteDescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						
					System.out.println("Inside Itm Default>>>>>>>>>>>");
					String currAppdate ="";
					java.sql.Timestamp currDate = null;
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
					System.out.println("Tran date is"+currAppdate);
					valueXmlString.append("<tran_date>").append("<![CDATA["+currAppdate.trim()+"]]>").append("</tran_date>");
					valueXmlString.append("<site_code>").append("<![CDATA[" + (loginSite) + "]]>").append("</site_code>");////added by kailasg on 26-march-21 for add sitecode in detail suggeste by SP sir
					valueXmlString.append("<site_descr>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr>");////added by kailasg on 26-march-21 for add sitecode in detail suggeste by SP sir
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chguserhdr + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + chgtermhdr + "]]>").append("</chg_term>");
					
					discType=checkNull(genericUtility.getColumnValue("discount_type", dom));
					
					/*if(!"R".equalsIgnoreCase(discType)) //changed by Mukesh chauhan on 21/05/2020// commented by kailasg on 1-april-21 for [Bill to margin should allowed for Type Percentage in Discount Master]
					{
						margPer =checkDoubleNull(genericUtility.getColumnValue("bill_to_margin_perc", dom));
						
						 valueXmlString.append("<bill_to_margin_perc protect =\"1\">").append("<![CDATA[" + margPer + "]]>").append("</bill_to_margin_perc>");
					}*/
				}
				}
				
				 else if (currentColumn.trim().equalsIgnoreCase("cust_code")) 
				 { 
					 System.out.println("Inside Cust code itmchange>>>>"+custCode);
					 custCode=checkNull(genericUtility.getColumnValue("cust_code", dom));
				  System.out.println("Cust code itmchange>>>>"+custCode);
				  
				  sql = "select cust_name from customer where cust_code=?"; 
				  pstmt =conn.prepareStatement(sql); 
				  pstmt.setString(1, custCode); 
				  rs =pstmt.executeQuery(); 
				  if (rs.next()) 
				  { 
					  custName =checkNull(rs.getString("cust_name"));
				  System.out.println("Cust Name itmchange>>>>"+custName); } 
				  rs.close(); 
				  rs =null; 
				  pstmt.close(); 
				  pstmt = null;
				  
				  valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");
				 
				  
                  }
				//Add by Kailasg on 26/10/20:START
	                else if (currentColumn.trim().equalsIgnoreCase("item_ser")) {
	                	String itemSeriesdescr="";
	                	String itemSeries="";
	                	itemSeries = checkNull(genericUtility.getColumnValue("item_ser", dom));
							
						 sql = "select descr from itemser where item_ser =?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemSeries);
							rs = pStmt.executeQuery();
							if (rs.next()) {
							itemSeriesdescr = rs.getString("descr");
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							valueXmlString.append("<itemser_descr>").append("<![CDATA[" + itemSeriesdescr + "]]>")
							.append("</itemser_descr>");
							//Add by Kailasg on 26/10/20:END
	                }
				//Add by Kailasg on 26/10/20:START
				 else if (currentColumn.trim().equalsIgnoreCase("inst_code")) {
					 String instDescr="";
					 instCode = checkNull(genericUtility.getColumnValue("inst_code", dom));
					System.out.println("instCode  :" + instCode);	
					sql = "select  cust_name as instcust from customer where cust_code=?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1,instCode);
						rs = pStmt.executeQuery();
						if (rs.next()) {
							instDescr = rs.getString("instcust");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						valueXmlString.append("<institute_name>").append("<![CDATA[" + instDescr + "]]>")
						.append("</institute_name>");;
 				 }
				

				//NANDKUMAR
				 else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				 { 
					discType=checkNull(genericUtility.getColumnValue("discount_type", dom));
					
					/*if(!"R".equalsIgnoreCase(discType)) { // commented by kailasg on 1-april-21 for [Bill to margin should allowed for Type Percentage in Discount Master]
						margPer =checkDoubleNull(genericUtility.getColumnValue("bill_to_margin_perc", dom));
						
						 valueXmlString.append("<bill_to_margin_perc protect =\"1\">").append("<![CDATA[" + margPer + "]]>").append("</bill_to_margin_perc>");
					}*/
					
				  }
				 else if (currentColumn.trim().equalsIgnoreCase("discount_type")) 
				 { 
					 discType=checkNull(genericUtility.getColumnValue("discount_type", dom));
					 margPer =checkDoubleNull(genericUtility.getColumnValue("bill_to_margin_perc", dom));//changed by Mukesh chauhan on 21/05/2020
					 retailType=checkNull(genericUtility.getColumnValue("retail_type", dom));
						/*if(!"R".equalsIgnoreCase(discType)) //changed by Mukesh chauhan on 21/05/2020  // commented by kailasg on 1-april-21 for [Bill to margin should allowed for Type Percentage in Discount Master]
						
						{
							
							
							 valueXmlString.append("<bill_to_margin_perc protect =\"1\">").append("<![CDATA[" + margPer + "]]>").append("</bill_to_margin_perc>");
						}
						else
						{
							 valueXmlString.append("<bill_to_margin_perc protect =\"0\">").append("<![CDATA[" + margPer + "]]>").append("</bill_to_margin_perc>");
						}*/
						if("R".equalsIgnoreCase(discType)) //added by kailasg on 1/2/2021
							
						{
							
							
							 valueXmlString.append("<retail_type protect =\"1\">").append("<![CDATA[" + "C" + "]]>").append("</retail_type>");
						}
						else
						{
							 valueXmlString.append("<retail_type protect =\"0\">").append("<![CDATA[" + "C" + "]]>").append("</retail_type>");
						}
				  
				  }
				
				valueXmlString.append("</Detail1>");
				break;
				  
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				String offerdate="";
				Timestamp offerDate=null;
				String pricelist="";
				String itemcode="";
				double OfferRate=0.0;
				double quantity=0.0;
				do
				{
					
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}

					ctr++;
				} while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN [" + currentColumn + "]");
				
				 pricelist=checkNull(genericUtility.getColumnValue("price_list", dom1));
				 offerdate=checkNull(genericUtility.getColumnValue("offer_date", dom1));
				if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					System.out.println("Item Code itmchange>>>>"+itemCode);
					sql = "select descr from item where item_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemDescr = checkNull(rs.getString("descr"));
						System.out.println("Item Descr itmchange>>>>"+itemDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//added by kailasg on 23-march-21 Offer Rate and Quantity  fileds required in DIscount master det . Rate should come from pricelist based on offer date entered in header  start 
					
					OfferRate= distCommon.pickRate( pricelist,  offerdate,itemCode, conn);
					
					
					valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr + "]]>").append("</item_descr>");
					valueXmlString.append("<offer_rate>").append("<![CDATA[" + OfferRate + "]]>").append("</offer_rate>");
					
					
					//added by kailasg on 23-march-21 Offer Rate and Quantity  fileds required in DIscount master det . Rate should come from pricelist based on offer date entered in header  end 

					
					//nandkumar
					valueXmlString=getCalculation(dom,dom1,valueXmlString,conn);
				}
			    if (currentColumn.trim().equalsIgnoreCase("lot_no__from") || currentColumn.trim().equalsIgnoreCase("lot_no__to"))
				{
					System.out.println("Inside lot no fr and to"+currentColumn);
					valueXmlString=getCalculation(dom,dom1,valueXmlString,conn);
					System.out.println("mukesh valuexmlstring>>>"+valueXmlString);
				}
				
				valueXmlString.append("</Detail2>");
				
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
	private double checkDoubleNull(String input)	
	{
		double var=0.0;
		if (input != null && input.trim().length() > 0)
		{
			var =Double.parseDouble(input);
		}
		return var;
	}
	public StringBuffer getCalculation(Document dom,Document dom1,StringBuffer valueXmlString, Connection conn)
	{
		System.out.println("Inside getCalculation>>>>>>>>>>");
		String discType="",lotFrom="",lotTo="",itemCode="",tranDate="",offerDate="",priceList="",sql="",unit="",refNo="",mrpPriceList="";
		String listType="";
		double margPer=0,rate=0,mrpRate=0,OfferRate=0,margAmt=0,priceDiff=0,discOfferAmt=0,discOfferPer=0;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		Timestamp TranDate=null;
		int count=0;
		try {
		DistCommon distCommon = new DistCommon();
		 discType=checkNull(genericUtility.getColumnValue("discount_type", dom1));
		 lotFrom=(genericUtility.getColumnValue("lot_no__from", dom));
		 lotTo=(genericUtility.getColumnValue("lot_no__to", dom));
		 itemCode=(genericUtility.getColumnValue("item_code", dom));
		 
		 if("R".equalsIgnoreCase(discType)) {
			 if((itemCode != null && itemCode.trim().length() > 0) && (lotFrom != null && lotFrom.trim().length() > 0)&&(lotTo != null && lotTo.trim().length() > 0) ) {
				
				 margPer =checkDoubleNull(genericUtility.getColumnValue("bill_to_margin_perc", dom1));
				 priceList=checkNull(genericUtility.getColumnValue("price_list", dom1));
				 tranDate=checkNull(genericUtility.getColumnValue("tran_date", dom1));
				 offerDate=checkNull(genericUtility.getColumnValue("offer_date", dom1));
				
				 rate=distCommon.pickRate(priceList,tranDate,itemCode,lotFrom,conn);
				 System.out.println("rate>> line:1411 Method>>"+rate);
				 if (rate <= 0) {
						mrpRate = distCommon.pickRate(priceList,tranDate,itemCode,lotTo,conn);
				}
				 OfferRate=distCommon.pickRate(priceList,offerDate,itemCode,lotFrom,conn);
				 System.out.println("Offer Rate :>>>>"+OfferRate);
				 
				 if (OfferRate <= 0) {
					 OfferRate = distCommon.pickRate(priceList,offerDate,itemCode,lotTo,conn);
					 System.out.println("Offer Rate line no 1416 :>>>>"+OfferRate);
					}
				 
				 mrpPriceList = checkNull(distCommon.getDisparams( "999999", "MRP", conn ));
				 System.out.println("Price List line no 1420 :>>>>"+mrpPriceList);
				 
				 if(!"NULLFOUND".equalsIgnoreCase(mrpPriceList))
				 {
					 listType = distCommon.getPriceListType(mrpPriceList, conn);
					 System.out.println("list Type line no 1425 :>>>>"+listType);

					 sql = "Select unit from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							unit= rs.getString("unit");
							System.out.println("unit>> line:1436 Method>"+unit);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDate,genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())	+ " 00:00:00.0");
					 sql = "select count(1)  as count from pricelist where price_list=?"
							+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
							+ " and (ref_no is not null)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mrpPriceList);
					pstmt.setString(2, itemCode);
					pstmt.setString(3, unit);
					pstmt.setString(4, listType);
					pstmt.setTimestamp(5, TranDate);
					pstmt.setTimestamp(6, TranDate);
					pstmt.setDouble(7, 1);
					pstmt.setDouble(8, 1);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						count = rs.getInt("count");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (count >= 1) {
						sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
								+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mrpPriceList);
						pstmt.setString(2, itemCode);
						pstmt.setString(3, unit);
						pstmt.setString(4, listType);
						pstmt.setTimestamp(5, TranDate);
						pstmt.setTimestamp(6, TranDate);
						pstmt.setDouble(7, 1);
						pstmt.setDouble(8, 1);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							refNo = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						mrpRate = distCommon.pickRateRefnoWise(mrpPriceList, tranDate, itemCode, refNo, listType, 1,conn);
						System.out.println("mrpRate>> line:1483 Method>"+mrpRate);
					}
					if (mrpRate <= 0) {
						
						mrpRate = distCommon.pickRate(mrpPriceList,tranDate,itemCode,lotFrom,conn);
						System.out.println("mrpRate>> line:1488 Method>"+mrpRate);
						if (mrpRate <= 0) {
							mrpRate = distCommon.pickRate(mrpPriceList,tranDate,itemCode,lotTo,conn);
							
						}
					}
				 }
				 
				 System.out.println("Rate>> line:1496 Method>"+rate);
				 System.out.println("OfferRate>> line:1497 Method>"+OfferRate);
				priceDiff=rate-OfferRate;
				System.out.println("priceDiff>> line:1499 Method>"+priceDiff);
				priceDiff=distCommon.getRndamt(priceDiff, "R", 0.001);
				System.out.println("priceDiff>> line:1501 Method>"+priceDiff);
				System.out.println("margPer>> line:1502 Method>"+margPer);
				margAmt=OfferRate * margPer;
				System.out.println("margAmt>> line:1504 Method>"+margAmt);
				margAmt=distCommon.getRndamt(margAmt, "R", 0.001);
				System.out.println("margAmt>> line:1506 Method>"+margAmt);
				discOfferAmt= priceDiff + margAmt;
				System.out.println("discOfferAmt>> line:1502 Method>"+discOfferAmt);
				discOfferAmt=distCommon.getRndamt(discOfferAmt, "R", 0.001);
				System.out.println("discOfferAmt>> line:1510 Method>"+discOfferAmt);
				
				if(rate>0)
				{
				discOfferPer=(discOfferAmt/rate)*100;
				System.out.println("discOfferPer>> line:1515 Method>"+discOfferPer);
				discOfferPer=distCommon.getRndamt(discOfferPer, "R", 0.001);
				System.out.println("discOfferPer>> line:1517 Method>"+discOfferPer);
				}
				valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
				valueXmlString.append("<mrp_rate>").append("<![CDATA[" + mrpRate + "]]>").append("</mrp_rate>");
				valueXmlString.append("<rate_diff>").append("<![CDATA[" + priceDiff + "]]>").append("</rate_diff>");
				valueXmlString.append("<margin_amt>").append("<![CDATA[" + margAmt + "]]>").append("</margin_amt>");
				valueXmlString.append("<disc_off_per>").append("<![CDATA[" + discOfferPer + "]]>").append("</disc_off_per>");
				valueXmlString.append("<disc_off_amt>").append("<![CDATA[" + discOfferAmt + "]]>").append("</disc_off_amt>");
				
			 }
			 
			
			 
		 }
		 else
		 {
		 
		 }
		 
		
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		return valueXmlString;
	}
}