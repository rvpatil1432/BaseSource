package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.DistOrderRcpConfLocal;
import ibase.webitm.ejb.dis.DistOrderRcpConfRemote;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
import ibase.webitm.ejb.dis.DistOrderRcpConf;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.fin.adv.MiscValConf;

public class DissIssuePosConf extends ValidatorEJB implements DissIssuePosConfLocal, DissIssuePosConfRemote {

	DistCommon distCommon = new DistCommon();
	CommonConstants commonConstants = new CommonConstants();
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon fincommon = new FinCommon();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String postConfirm() throws ITMException {
		return "";
	}

	public String postConfirm(String xmlStringAll, String tranId, String xtraParams)
			throws RemoteException, ITMException {
		String retString = "SEND_SUCCESS";
		Connection conn = null;
		Document dom = null;
		try {
			if (xmlStringAll != null && xmlStringAll.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlStringAll);
			}
			retString = actionDistIssueConf(dom, xtraParams, conn);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception in [IndentReqConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String actionDistIssueConf(Document dom, String xtraParams, Connection conn)
			throws RemoteException, ITMException {
		String tranId = "";
		String siteCodedlv = "";
		String recoverCsaGst = "";
		String siteType = "";
		String creatInvOth = "";
		String creatInvOthlist = "";
		String otherSite = "";
		String siteCode = "";
		String siteTypr = "";
		String sql = "";
		String errString = "", retString = "";
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		boolean connStatus = false, isError = false;
		String tranType="",recoverTranType="";//added by monika salla

		try {

			if (conn == null) 
			{
				conn = getConnection();
				conn.setAutoCommit(false);
				connStatus = true;
			}

			NodeList detail1NodeList = dom.getElementsByTagName("Detail1");
			int detail1NodeListlen = detail1NodeList.getLength();
			System.out.println("detail1NodeListlen [" + detail1NodeListlen + "]");
			for (int ctrH = 0; ctrH < detail1NodeListlen; ctrH++) 
			{
				NodeList childNodeList = detail1NodeList.item(ctrH).getChildNodes();
				int childNodeListlen = childNodeList.getLength();

				for (int ctrD = 0; ctrD < childNodeListlen; ctrD++) 
				{
					Node childNode = childNodeList.item(ctrD);

					if (childNode != null && "tran_id".equalsIgnoreCase(childNode.getNodeName())
							&& childNode.getFirstChild() != null)
					{
						tranId = childNode.getFirstChild().getNodeValue();
						System.out.println("tran_id is====" + tranId);
					}

					//added by monika salla 7 12 20 to get tran type
					if(childNode != null && "tran_type".equalsIgnoreCase(childNode.getNodeName())
							&& childNode.getFirstChild() != null)
					{
						tranType = childNode.getFirstChild().getNodeValue();
						System.out.println("tran_type is 11====" + tranType);
					}
				}
			}
			//need to add tran type on basis of tran type recovery will be done DISTORDER_TYPE--table
			//added by monika salla 7 12 20
			sql = "select RECOVER_GST,RECOVER_TRANTYPE from DISTORDER_TYPE where tran_type = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranType);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				recoverCsaGst = rs.getString("RECOVER_GST");
				recoverTranType = rs.getString("RECOVER_TRANTYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//recoverCsaGst = fincommon.getFinparams("999999", "RECOVER_CSA_GST", conn);
			System.out.println("recoverCsaGst--11["+recoverCsaGst+" ]recoverTranType ["+recoverTranType);
			if("NULLFOUND".equalsIgnoreCase(recoverCsaGst) || recoverCsaGst == null || recoverCsaGst == "")
			{
				recoverCsaGst = "N";
			}

			if("Y".equalsIgnoreCase(recoverCsaGst))
			{
				sql="select site_code__dlv from distord_iss where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					siteCodedlv = rs.getString("site_code__dlv");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = " select site_type  from site where site_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCodedlv);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					siteType = rs.getString("site_type");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;


				creatInvOthlist = fincommon.getFinparams("999999", "ALOW_INV_OTH_SITE", conn);

				if ("NULLFOUND".equalsIgnoreCase(creatInvOthlist) || creatInvOthlist == null) {
					creatInvOthlist = "";
				}

				if (creatInvOthlist.trim().length() > 0)
				{
					String[] arrStr = creatInvOthlist.split(",");
					for (int j = 0; j < arrStr.length; j++)
					{
						creatInvOth = arrStr[j];
						System.out.println("creatInvOth>>>>>>>>" + creatInvOth);
						if (siteType.equalsIgnoreCase(creatInvOth.trim()))
						{
							//commented by monika salla on 9 dec 20
							otherSite = fincommon.getFinparams("999999", "INVOICE_OTHER_SITE", conn);
							if (!"NULLFOUND".equalsIgnoreCase(creatInvOthlist) && creatInvOthlist != null
									&& creatInvOthlist.trim().length() > 0)
							{
								//added by monika salla on 23 03 21 ---
								//' DN' = 'Raised  GST DN to Customer ' 
								//will be used in distribution Order type for transferring material from Serdia to CFA . In this case DN will be generated towards the Customer in Issuing site (Receivables) . In  Receiving site CN will be created towards the Supplier  (Payables). 
								if("CG".equalsIgnoreCase(recoverTranType.trim()))
								{   
									System.out.println("recovertrantype CG--->>"+ recoverTranType);
									retString = genMiscDrCrRcp(tranId,recoverTranType, otherSite, xtraParams, conn);
									//retString = genMiscDrCrRcp(tranId, otherSite, xtraParams, conn,recoverTranType);
									System.out.println("REt string recovertrantype CG--->>"+ retString);
								}
								//' DN' = ' Reverse GST DN   '
								// will be used in distribution Order type for transferring material from CFA to Serdia . In this case DN will be generated towards the Supplier in Issuing site (Payable) . In Receiving site CN will be created towards  the Customer (Receivables) .  
								else if("RG".equalsIgnoreCase(recoverTranType.trim()))
								{
									System.out.println("recovertrantype RG--->>"+ recoverTranType+" ]otherSite ["+otherSite+" ] tranId ["+tranId);
									DistOrderRcpConf distordrcpconf=new  DistOrderRcpConf();
									//retString= distordrcpconf.generateMiscVoucher(tranId, otherSite,recoverTranType, xtraParams, conn);
									retString= generateMiscVoucher(tranId, recoverTranType,otherSite, xtraParams, conn);
									System.out.println("retString recovertrantype RG--->>"+retString);
									if( retString != null && retString.trim().length() > 0 )
									{
										if(retString.indexOf("CONFSUCCES") > -1)
										{
											retString = "";
										}
										else
										{
											isError = true;
											if(connStatus) {
												conn.rollback();
												return retString;
											}
										}
										System.out.println("retString3::::"+retString);
										//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
										return retString;
									}
								}
								else
								{
									retString="";
								}
								//end
								System.out.println("inside else where recover gst is Y123 " + retString);
							}
						}

					}
				}

			}
			else 
			{
				//System.out.println("inside else where recover gst is N " + retString);
				retString ="SEND_SUCCESS";
				//System.out.println("RETString  " + retString);
				return retString;
			}
		}
		catch (ITMException ie)
		{
			System.out.println("ITMException : " + ie);

			try {

				conn.rollback();

			} catch (Exception t) {

			}

			ie.printStackTrace();

			retString = itmDBAccessEJB.getErrorString("", "VTDESNCONF", "", "", conn);
			//System.out.println("Returnng String From DistOrderIssuepostConfEJB :" + errString);
			//connStatus = true;
			isError = true;
			return retString;
		} 
		catch (Exception e) 
		{
			//System.out.println("Exception in Confirm [DistOrderIssuepostConfEJB]" + e);
			try 
			{
				conn.rollback();
			}
			catch (Exception t)
			{

			}
			e.printStackTrace();
			retString = itmDBAccessEJB.getErrorString("", "VTDESNCONF", "", "", conn);
			//System.out.println("Returnng String From DistOrderIssuepostConfEJB :" + errString);
			//connStatus = true;
			isError = true;
			return retString;
		}
		finally 
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (rs1 != null) 
				{
					rs1.close();
					rs1 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null) 
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if (conn != null) 
				{
					//if (connStatus)
					if (isError && connStatus)
					{
						System.out.println("actionDistIssueConf Local connection rolledback");
						conn.rollback();
					} 
					else if (!isError && connStatus)
					{
						System.out.println("actionDistIssueConf Local connection committed");
						conn.commit();
						//retString = itmDBAccessEJB.getErrorString("", "VMCPSUCC  ", "", "", conn);
					}
					if (!connStatus)
					{
						System.out.println("actionDistIssueConf not Local connection so not commit or rollback");
					}
				}
			} 
			catch (Exception e) 
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
			}
		}
		//System.out.println("Returnng String From DistOrderIssuepostConfEJB :" + errString);
		return retString;
	}

	private String genMiscDrCrRcp(String tranId,String recoverTranType, String siteCodeAs, String xtraParams, Connection conn)
			throws ITMException {

		String currCode = "";
		String confirmed = "";
		String siteCode = "";
		String tranType = "";
		String sreturnNo = "";
		String tranIdNew = "";
		String itemCode = "";
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";
		String transMode = "";
		String chgUser = "";
		String chgTerm = "";
		String errString = "";
		String sql = "";
		String sql1 = "";
		String distOrder = "";
		String tranIdIss = "";
		String siteCodeShip = "";
		String confPasswd = "";
		String orderType = "";
		String qcReqd = "";
		String issueRef = "";
		String locCodeGit = "";
		String frtType = "";
		String chgUsr = "";
		String empCodeAprv = "";
		String lotNo = "";
		String cctrCode = "";
		String cctrCodeDet = "";
		String tranSer = "";
		String tranWin = "";
		String analysis1 = "";
		String analysis2 = "";
		String analysis3 = "";
		String unit = "";
		String lotSl = "";
		String acctCode = "";
		String lineNoDistOrder = "";
		String locCode = "";
		String packCode = "";
		String packInstr = "";
		String dimension = "";
		String grade = "";
		String suppCode = "";
		String acctCodeAp = "";
		String cctrCodeAp = "";
		String acctCodeApAdv = "";
		String cctrCodeApAdv = "";
		String stanCode = "";
		String suppType = "";
		String payMode = "";
		String crTerm = "";
		String suppName = "";
		String lrNo = "";
		String site = "";
		String CodeDl = "";
		String distRoute = "";
		String tranCode = "";
		String lorryNo = "";
		String remarks = "";
		String siteCodeDlv = "";
		String chgUsrs = "", ordType = "", availableyn = "", stockStatus = "", gpNo = "", gpSer = "";
		String ediStat = "", saleOrder = "", custRefNo = "", refNoCr = "", wfStatus = "";
		String sundryCode = "", custPord = "";
		double adjAmount = 0.0;
		double custRefAmt = 0.0;
		double rndOff = 0.0, rndTo = 0.0;
		String tranIdAs = "", revTran = "", adjmisccrn = "", itemSer = "";
		Timestamp chgDate = null, pordDate = null;
		Timestamp custRefDate = null;
		Timestamp dueDate = null;
		Timestamp tranDate = null;
		Timestamp effDate = null;
		Timestamp confDate = null;
		Timestamp gpDate = null;
		Timestamp retestDate = null;
		Timestamp lrDate = null;
		Timestamp currDate = null;
		double amount = 0, taxAmt = 0, netAmt = 0, rateClg = 0, exchRate = 0, rate = 0;
		double discount = 0;
		double costRate = 0;
		int lineNo = 0, count = 0;
		double grossWeight = 0;
		String sundryType = "";
		double tareWeight = 0;
		int volume = 0, noArt = 0, quantity = 0;
		double frtAmt = 0;
		double netWeight = 0, amountBc = 0.0;
		String drcrFlag = "";
		String retString = "SEND_SUCCESS";
		String tranIdRcv = "";
		int lineNoSret = 0, lineNoInvtrace = 0, lineNoRcpinv = 0, i = 0, actualQty = 0, palletWt = 0;
		String siteCodeBill = "";
		String finEntity = "";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String shipmentId = "", currCodeFrt = "", exchRateFrt = "", projCode = "", dcNo = "", confPwd = "";
		String custCode = "";
		String mDistOrder, mItemCode = "", mUnit = "", mLocCode = "", mPackCode = "";
		double mQuantity = 0.0, mRate = 0.0;
		int mLineNo = 0, mLineDist = 0, llNoArt = 0;
		double lcGrossWeight = 0.0, lcTareWeight = 0.0, lcNetWeight = 0.0, amountdet = 0.0, totAmtDet = 0.0;
		String lsBatchNo = "", lsGrade = "", mLotNoDist = "", mLotSlDist = "";
		String analCode = "", empCode = "", reasCode = "", refNo = "";
		String acctCodeaAr = "", cctrCodeAr = "", custType = "", rcpMode = "", custName = "";
		Timestamp ldtRetestDate = null;
		try {
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);

			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			chgDate = new java.sql.Timestamp(new java.util.Date().getTime());

			sql = "select tran_date,eff_date,dist_order,site_code,  site_code__dlv ,amount,tax_amt,net_amt, "
					+ " remarks,chg_user,chg_term ,  curr_code,chg_date,confirmed, "
					+ " conf_date,order_type,proj_code,trans_mode,conf_passwd,exch_rate, "
					+ " tran_type,tran_ser,site_code__bil  from distord_iss  where tran_id= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if ( rs.next()) {
				tranDate = rs.getTimestamp("tran_date");
				effDate = rs.getTimestamp("eff_date");
				distOrder = rs.getString("dist_order");
				siteCode = rs.getString("site_code");
				siteCodeDlv = rs.getString("site_code__dlv");
				amount = rs.getDouble("amount");
				taxAmt = rs.getDouble("tax_amt");
				netAmt = rs.getDouble("net_amt");
				remarks = rs.getString("remarks");
				chgUsrs = rs.getString("chg_user");
				chgTerm = rs.getString("chg_term");
				currCode = rs.getString("curr_code");
				chgDate = rs.getTimestamp("chg_date");
				confirmed = rs.getString("confirmed");
				confDate = rs.getTimestamp("conf_date");
				ordType = rs.getString("order_type");
				projCode = rs.getString("proj_code");
				transMode = rs.getString("trans_mode");
				confPwd = rs.getString("conf_passwd");
				exchRate = rs.getDouble("exch_rate");
				tranType = rs.getString("tran_type");
				tranSer = rs.getString("tran_ser");
				siteCodeBill = rs.getString("site_code__bil");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// tran_id new generator
			//added by monika to set rate---

			sql = "select rate from distord_issdet  where tran_id= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				rate = rs.getDouble("rate");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//end

			String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode.trim() + "</site_code>";
			xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>";
			xmlValues = xmlValues + "<tran_type>" + recoverTranType + "</tran_type>";//added by monika salla on 9 dec 2020--to set tran type from distoreder type
			//xmlValues = xmlValues + "<tran_type>" + tranType + "</tran_type>";

			xmlValues = xmlValues + " <drcr_flag>" + "D" + "</drcr_flag>";
			xmlValues = xmlValues + "</Detail1></Root>";

			//tranIdNew = generateTranId("w_misc_drcr_rcp_dr", xmlValues, conn);
			//commented by monika salla-2 sept 2020-we have to insert the same dist. issue  tran_id 

			// data from site_customer
			sql = "select count(*) from site_customer where site_code__ch = ? and site_code = ? and channel_partner = 'Y' ";

			System.out.println("tranType:--["+tranType+"] recoverTranType---["+recoverTranType+" ]site code dlv-->["+siteCodeDlv+" ] site code---["+siteCode);


			//sql = "select count(*) from site_customer where  site_code = ? and channel_partner = 'Y' ";--site_Code--s0001 to sitw_Code delc--sooo2

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1, siteCodeAs);
			// pstmt.setString(2, siteCodeDlv);
			pstmt.setString(1, siteCodeDlv);//s0003
			pstmt.setString(2, siteCode);//s0001
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			// data from customer
			if (count == 0) {
				sql = "select count(*) from customer where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1, siteCodeAs);
				// pstmt.setString(1, siteCodeDlv);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}

				if (count > 1) {
					retString = itmDBAccessEJB.getErrorString("", "ERRORVTCPC", "", "", conn);
					return retString;
				} else if (count == 0) {
					retString = itmDBAccessEJB.getErrorString("", "VTCUSTCD4", "", "", conn);
					return retString;
				}
				// cust_code from customer
				else if (count == 1)
				{
					sql1 = "select cust_code from customer where site_code = ? and channel_partner = 'Y'";
					pstmt1 = conn.prepareStatement(sql1);
					//pstmt1.setString(1, siteCodeAs);
					//pstmt1.setString(1, siteCodeDlv); 
					pstmt1.setString(1, siteCode); 
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) {
						custCode = rs1.getString("cust_code");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
			}
			// cust_code from site_customer
			else if (count == 1) 
			{
				sql = "select cust_code from site_customer where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1, siteCodeAs);
				//pstmt.setString(2, siteCodeDlv);
				pstmt.setString(1, siteCodeDlv);
				pstmt.setString(2, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					custCode = rs.getString("cust_code");
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
			// finentity
			sql = "select fin_entity from site where site_code= ? ";
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1, siteCodeAs);
			pstmt.setString(1, siteCodeDlv);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				finEntity = rs.getString("fin_entity");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "select acct_code__ar,cctr_code__ar,acct_code__adv,cctr_code__adv,stan_code,cust_type, rcp_mode, cr_term, cust_name from customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				acctCodeaAr = rs.getString("acct_code__ar");
				cctrCodeAr = rs.getString("cctr_code__ar");
				acctCodeApAdv = rs.getString("acct_code__adv");
				cctrCodeApAdv = rs.getString("cctr_code__adv");
				stanCode = rs.getString("stan_code");
				custType = rs.getString("cust_type");
				rcpMode = rs.getString("rcp_mode");
				crTerm = rs.getString("cr_term");
				custName = rs.getString("cust_name");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			// MISC_DRCR_RCP
			//Added by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change START
			amount = 0.0;


			//added parent__tran_id in insert query to store diss issue tran id-by monika salla on 9 dec 2020
			sql = "insert into misc_drcr_rcp(tran_id,tran_ser,tran_date,eff_date,fin_entity,site_code,sundry_type,sundry_code,acct_code,"
					+ " cctr_code,amount,curr_code,exch_rate,confirmed,chg_user,chg_date,chg_term,"
					+ " due_date,tran_type,emp_code__aprv,drcr_flag,remarks,parent__tran_id) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1, tranIdNew);
			pstmt.setString(1, tranId);//added by monika on 2 sept 2020 -to set tran id same same dist issue
			pstmt.setString(2, "MDRCRD");
			pstmt.setTimestamp(3, tranDate);
			pstmt.setTimestamp(4, effDate);
			pstmt.setString(5, finEntity);
			//pstmt.setString(6, siteCodeAs);
			//pstmt.setString(6, siteCodeDlv);
			pstmt.setString(6, siteCode);
			pstmt.setString(7, "C");
			pstmt.setString(8, custCode);
			pstmt.setString(9, acctCodeaAr);
			pstmt.setString(10, cctrCodeAr);
			pstmt.setDouble(11, amount);
			pstmt.setString(12, currCode);
			pstmt.setDouble(13, exchRate);
			pstmt.setString(14, "N");
			pstmt.setString(15, chgUser);
			pstmt.setTimestamp(16, tranDate);
			pstmt.setString(17, chgTerm);
			pstmt.setTimestamp(18, tranDate);
			pstmt.setString(19, recoverTranType);
			//pstmt.setString(19, tranType);
			pstmt.setString(20, null);
			pstmt.setString(21, "D");
			pstmt.setString(22, "GST payable against D-ISS " + tranId);
			pstmt.setString(23, tranId);//added by monika salla to get tran id in creation of misc debit credit

			int count1 = pstmt.executeUpdate();
			System.out.println("inserted dATA -->"+count1+"account code___["+acctCode+"] cctrcode");
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			//Added by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change END

			// misc_voucher
			//	sql = "select sum(tr.tax_amt) from taxtran tr, tax t where t.tax_code = tr.tax_code and t.tax_type in ('G','H','I') and tr.tran_code = 'D-ISS' and tr.tran_id= ?";
			//changes done by monika salla on 29 may 2020 as coount code should be from tax_tran

			sql ="select tx.acct_code,tx.cctr_code,sum(tx.tax_amt) from taxtran tx, tax t where tx.tax_code=t.tax_code "+
					"	 and t.tax_type in ('I','G','H','J') and tx.tran_id=? and tx.tax_amt <>0 and tran_code='D-ISS' "+
					" GROUP BY tx.acct_code,tx.cctr_code";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			//if (rs.next()) { //Commented by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change
			int rowCnt = 0;
			while (rs.next()) { //Added by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change     
				acctCode=rs.getString(1);
				cctrCode=rs.getString(2);
				amount = rs.getDouble(3);
				System.out.println("Amount calculated: [ " + amount+" ]aactcode["+acctCode+" ]cctr code["+cctrCode+"rate---"+rate);  

				//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change START

				rowCnt++;     
				if ("NULLFOUND".equalsIgnoreCase(cctrCode) || cctrCode == null || cctrCode == "" || cctrCode.length() == 0) {
					cctrCode = cctrCodeAp;
				}

				sql = "insert into misc_drcr_rdet (tran_id,line_no,acct_code,cctr_code,amount,quantity,rate,unit,net_amt) "
						+ "	Values (?,?,?,?,?,?,?,?,?)";
				pstmt2 = conn.prepareStatement(sql);
				//pstmt.setString(1, tranIdNew);
				pstmt2.setString(1, tranId);//added by monika to set tran id as dist issue on -2 sept 2020
				pstmt2.setInt(2, rowCnt);
				pstmt2.setString(3, acctCode);
				pstmt2.setString(4, cctrCode);
				pstmt2.setDouble(5, amount);
				pstmt2.setDouble(6, 1);
				//pstmt2.setDouble(7, amount);
				pstmt2.setDouble(7, rate);
				pstmt2.setString(8, mUnit);
				pstmt2.setDouble(9, amount);//added by monika salla on 12 jan 21
				count = pstmt2.executeUpdate();
				if(pstmt2!=null)
				{
					pstmt2.close();
					pstmt2 = null;
				}

				totAmtDet=totAmtDet+Math.abs(amount);
				//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change END
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//Commented by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change START
			/*sql = "insert into misc_drcr_rcp(tran_id,tran_ser,tran_date,eff_date,fin_entity,site_code,sundry_type,sundry_code,acct_code,"
					+ " cctr_code,amount,curr_code,exch_rate,confirmed,chg_user,chg_date,chg_term,"
					+ " due_date,tran_type,emp_code__aprv,drcr_flag,remarks ) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1, tranIdNew);
            pstmt.setString(1, tranId);//added by monika on 2 sept 2020 -to set tran id same same dist issue
            pstmt.setString(2, "MDRCRD");
			pstmt.setTimestamp(3, tranDate);
			pstmt.setTimestamp(4, effDate);
			pstmt.setString(5, finEntity);
			pstmt.setString(6, siteCodeAs);
			pstmt.setString(7, "C");
			pstmt.setString(8, custCode);
			pstmt.setString(9, acctCodeaAr);
			pstmt.setString(10, cctrCodeAr);
			pstmt.setDouble(11, amount);
			pstmt.setString(12, currCode);
			pstmt.setDouble(13, exchRate);
			pstmt.setString(14, "N");
			pstmt.setString(15, chgUser);
			pstmt.setTimestamp(16, tranDate);
			pstmt.setString(17, chgTerm);
			pstmt.setTimestamp(18, tranDate);
			pstmt.setString(19, tranType);
			pstmt.setString(20, null);
			pstmt.setString(21, "D");
			pstmt.setString(22, "GST payable against D-ISS " + tranId);

			int count1 = pstmt.executeUpdate();
			System.out.println("inserted dATA -->"+count1+"account code___["+acctCode+"] cctrcode");
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}*/
			//Commented by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change END
			/*
			 * acctCode = fincommon.getFinparams("999999", "GST_RECO_ACCT_DET", conn);
			 * 
			 * cctrCode = fincommon.getFinparams("999999", "GST_RECO_CCTR_DET", conn);
			 *///commented by monika

			//Commented by Anagha R on 09/09/2020 for Serdia: GST Recovery change START 
			/*if ("NULLFOUND".equalsIgnoreCase(cctrCode) || cctrCode == null || cctrCode == ""
					|| cctrCode.length() == 0) {
				cctrCode = cctrCodeAp;
			}

			sql = "select line_no, dist_order,item_code, quantity,unit,rate"
					+ " from distord_issdet where tran_id= ? ORDER BY LINE_NO ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				mLineNo = rs.getInt("LINE_NO");
				mDistOrder = rs.getString("DIST_ORDER");
				mItemCode = rs.getString("ITEM_CODE");
				mQuantity = rs.getDouble("QUANTITY");
				mUnit = rs.getString("UNIT");
				mRate = rs.getDouble("RATE");
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}

			sql = "insert into misc_drcr_rdet (tran_id,line_no,acct_code,cctr_code,amount,quantity,rate,unit) "
					+ "	Values (?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
            //pstmt.setString(1, tranIdNew);
            pstmt.setString(1, tranId);//added by monika to set tran id as dist issue on -2 sept 2020
			pstmt.setInt(2, mLineNo);
			pstmt.setString(3, acctCode);
			pstmt.setString(4, cctrCode);
			pstmt.setDouble(5, amount);
			pstmt.setDouble(6, 1);
			pstmt.setDouble(7, amount);
			pstmt.setString(8, mUnit);
			count = pstmt.executeUpdate();
			if(pstmt!=null)
			{
				pstmt.close();
				pstmt = null;
            }*/
			//Commented by Anagha R on 09/09/2020 for Serdia: GST Recovery change END

			//Added by Anagha R on 09/09/2020 to update totAmt in misc_drcr_rcp for Serdia: GST Recovery change START
			sql = "update misc_drcr_rcp set amount = ? where tran_id = ?";
			pstmt = conn.prepareStatement(sql);

			pstmt.setDouble(1, totAmtDet);
			pstmt.setString(2, tranId);
			int updCnt = pstmt.executeUpdate();

			if(pstmt!=null)
			{
				pstmt.close();
				pstmt = null;
			}
			System.out.println("update amount Successfully1234:"+updCnt);
			//Added by Anagha R on 09/09/2020 to update totAmt in misc_drcr_rcp for Serdia: GST Recovery change END


			//Added by Monika salla on 07/01/2021 to update netAmt in misc_drcr_rdet for Serdia: GST Recovery change START
			/*sql = "update misc_drcr_rdet set net_amt = ? where tran_id = ?";
			pstmt = conn.prepareStatement(sql);

			pstmt.setDouble(1, totAmtDet);
			pstmt.setString(2, tranId);
			int updCnt1 = pstmt.executeUpdate();

			if(pstmt!=null)
			{
				pstmt.close();
				pstmt = null;
			}
			System.out.println("update netamount Successfully:"+updCnt1);*/
			//end bymonika salla on 07/01/2021 to update netAmt in misc_drcr_rcp for Serdia: GST Recovery change END

			MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
			//retString = confDebitNote.confirm(tranIdNew, xtraParams, "", conn);
			retString = confDebitNote.confirm(tranId, xtraParams, "", conn);//added by monika to set tran id as dist issue on -2 sept 2020
			//System.out.println("After DrCrRcpConf---->[" + retString + "]");
			if(retString != null && retString.indexOf("CONFSUCCES") != -1) 
			{ 
				//System.out.println("IN DrCrRcpConf---->[" + retString + "]");
				retString ="SEND_SUCCESS";
				// System.out.println("out DrCrRcpConf---->[" + retString + "]");
			}
		}

		catch (SQLException e) {
			System.out.println("Exception :DISSPOSCONF: actionVoucher " + e.getMessage());
			throw new ITMException(e);
		} catch (Exception e) {
			System.out.println("Exception :DISSPOSCONF : actionHandler " + e.getMessage());
			throw new ITMException(e);
		} finally {

			try {
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null) {
					pstmt1.close();
					pstmt1 = null;
				}

				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (rs1 != null) {
					rs1.close();
					rs1 = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;

				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}

		}
		System.out.println("valueXmlString.toString() " + retString.toString());
		return retString;
	}



	public String generateMiscVoucher(String tranId,String recoverTranType, String otherSite, String xtraParams, Connection conn)//added by monika salla for recovertrantype on 31 dec 20
			throws ITMException {
		String currCode = "", confirmed = "", siteCode = "", tranType = "", sreturnNo = "", tranIdNew = "";
		String itemCode = "", taxChap = "", taxClass = "", taxEnv = "";
		String transMode = "", chgUser = "", chgTerm = "", errString = "", sql = "", sql1 = "";
		String distOrder = "", tranIdIss = "", siteCodeShip = "", confPasswd = "", orderType = "", qcReqd = "",
				issueRef = "";
		String locCodeGit = "", frtType = "", chgUsr = "", empCodeAprv = "", lotNo = "", cctrCode = "",
				cctrCodeDet = "", tranSer = "", tranWin = "";
		String analysis1 = "", analysis2 = "", analysis3 = "", unit = "", lotSl = "", acctCode = "";
		String lineNoDistOrder = "", locCode = "", packCode = "", packInstr = "", dimension = "", grade = "";
		String suppCode = "", acctCodeAp = "", cctrCodeAp = "", acctCodeApAdv = "", cctrCodeApAdv = "", stanCode = "",
				suppType = "", payMode = "", crTerm = "", suppName = "", lrNo = "";
		Timestamp chgDate = null, tranDate = null, effDate = null, confDate = null, gpDate = null, retestDate = null,
				lrDate = null, currDate = null;
		double amount = 0, taxAmt = 0, netAmt = 0, rateClg = 0, exchRate = 0, rate = 0, calAmount = 0;
		double discount = 0, costRate = 0;
		int count = 0;
		String  lineNo = ""; 
		int grossWeight = 0, tareWeight = 0, netWeight = 0, volume = 0, frtAmt = 0, noArt = 0, quantity = 0;
		int lineNoSret = 0, lineNoInvtrace = 0, lineNoRcpinv = 0, i = 0, actualQty = 0, palletWt = 0;
		String startFrom = "", billDate = "", finEntity = "";
		int crDays = 0;
		Timestamp dueDate = null;
		double totAmtDet = 0.0, totBillAmtDet = 0.0, totNetAmtDet = 0.0;

		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		String siteCodeDlv="",remarks="",projCode="",confPwd="",ordType="",chgUsrs="",siteCodeBill="";
		boolean isError = false;
		FinCommon fcom = new FinCommon();
		try {
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);

			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			chgDate = new java.sql.Timestamp(new java.util.Date().getTime());

			sql = "select tran_date,eff_date,dist_order,site_code,  site_code__dlv ,amount,tax_amt,net_amt, "
					+ " remarks,chg_user,chg_term ,  curr_code,chg_date,confirmed, "
					+ " conf_date,order_type,proj_code,trans_mode,conf_passwd,exch_rate, "
					+ " tran_type,tran_ser,site_code__bil  from distord_iss  where tran_id= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				tranDate = rs.getTimestamp("tran_date");
				effDate = rs.getTimestamp("eff_date");
				distOrder = rs.getString("dist_order");
				siteCode = rs.getString("site_code");
				siteCodeDlv = rs.getString("site_code__dlv");
				amount = rs.getDouble("amount");
				taxAmt = rs.getDouble("tax_amt");
				netAmt = rs.getDouble("net_amt");
				remarks = rs.getString("remarks");
				chgUsrs = rs.getString("chg_user");
				chgTerm = rs.getString("chg_term");
				currCode = rs.getString("curr_code");
				chgDate = rs.getTimestamp("chg_date");
				confirmed = rs.getString("confirmed");
				confDate = rs.getTimestamp("conf_date");
				ordType = rs.getString("order_type");
				projCode = rs.getString("proj_code");
				transMode = rs.getString("trans_mode");
				confPwd = rs.getString("conf_passwd");
				exchRate = rs.getDouble("exch_rate");
				tranType = rs.getString("tran_type");
				tranSer = rs.getString("tran_ser");
				siteCodeBill = rs.getString("site_code__bil");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// tran_id new generator
			//added by monika to set rate---
			System.out.println("Recover tran type [ " + recoverTranType);  


			sql = "select rate from distord_issdet  where tran_id= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				rate = rs.getDouble("rate");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//end


			sql = "select site_code__ship from distorder  where dist_order= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, distOrder);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				siteCodeShip = rs.getString("site_code__ship");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<tran_id>" + "</tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode.trim() + "</site_code>";
			xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>";
			xmlValues = xmlValues + "<tran_type>" + recoverTranType + "</tran_type>";
			xmlValues = xmlValues + "<vouch_type> E </vouch_type>";
			xmlValues = xmlValues + "</Detail1></Root>";

			sql = "select count(*) from site_supplier where site_code__ch = ? and site_code = ? and channel_partner = 'Y' ";
			pstmt = conn.prepareStatement(sql);
            //pstmt.setString(1, siteCodeShip);
            pstmt.setString(1, siteCodeDlv);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (count == 0) {
				sql = "select count(*) from supplier where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
				pstmt = conn.prepareStatement(sql);
				// pstmt.setString(1, siteCode);
                //pstmt.setString(1, siteCodeShip);
                pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}

				if (count > 1) {
					errString = itmDBAccessLocal.getErrorString("", "ERRORVTCPC", "", "", conn);// Error code need to
					// change

				} else if (count == 0) 
				{
					errString = itmDBAccessLocal.getErrorString("", "VTCUSTCD4", "", "", conn);// Error code need to
					// change

				} else if (count == 1) {
					sql1 = "select supp_code from supplier where site_code = ? and channel_partner = 'Y'";
					pstmt1 = conn.prepareStatement(sql1);
                    //pstmt1.setString(1, siteCodeShip);
                    pstmt1.setString(1, siteCode);
					rs = pstmt1.executeQuery();
					if (rs.next()) {
						suppCode = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt1.close();
					pstmt1 = null;
				}
			}

			else if (count == 1) {
				sql = "select supp_code from site_supplier where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1, siteCode);
                //pstmt.setString(2, siteCodeShip);
               // pstmt.setString(2, siteCodeDlv);//
				pstmt.setString(1, siteCodeDlv);
                pstmt.setString(2, siteCode);//
				rs = pstmt.executeQuery();
				if (rs.next()) {
					suppCode = rs.getString(1);
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

			sql = "select acct_code__ap,cctr_code__ap,acct_code__ap_adv,cctr_code__ap_adv,stan_code,supp_type, pay_mode, cr_term, supp_name from supplier where supp_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, suppCode);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				acctCodeAp = rs.getString("acct_code__ap");
				cctrCodeAp = rs.getString("cctr_code__ap");
				acctCodeApAdv = rs.getString("acct_code__ap_adv");
				cctrCodeApAdv = rs.getString("cctr_code__ap_adv");
				stanCode = rs.getString("stan_code");
				suppType = rs.getString("supp_type");
				payMode = rs.getString("pay_mode");
				crTerm = rs.getString("cr_term");
				suppName = rs.getString("supp_name");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			// misc_voucher
			//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change START

			billDate = currDateStr;
			crDays = Integer.parseInt(findValue(conn, "cr_days", "crterm", "cr_term", crTerm));
			startFrom = findValue(conn, "start_from", "crterm", "cr_term", crTerm);
			dueDate = this.getDueDate(conn, sdfAppl, startFrom, sdfAppl.format(tranDate), sdfAppl.format(effDate),
					billDate, crDays);

			// finentity
			sql = "select fin_entity from site where site_code= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				finEntity = rs.getString("fin_entity");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "insert into misc_voucher(tran_id,tran_date,tran_type,eff_date,curr_code,exch_rate,"
					+ "site_code,tax_amt,confirmed,conf_date,chg_date,chg_user,chg_term,remarks,mvouch_gen_tran_id,net_amt,tran_mode,gp_date,"
					+ "sundry_code,bill_no,bill_date,acct_code__ap,cctr_code__ap,stan_code,"
					+ "tax_class,tax_chap,tax_env,pay_mode,cr_term,vouch_type,auto_pay,adv_amt,due_date,"
					+ "fin_entity,bill_amt,tot_amt,sundry_type,sundry_type__pay,sundry_code__pay,acct_code__pay,net_amt__bc)"
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setTimestamp(2, tranDate);
			pstmt.setString(3, recoverTranType);
			pstmt.setTimestamp(4, effDate);
			pstmt.setString(5, currCode);
			pstmt.setDouble(6, exchRate);
			pstmt.setString(7, siteCode);
			pstmt.setDouble(8, 0.0);
			pstmt.setString(9, "N");
			pstmt.setTimestamp(10, null);
			pstmt.setTimestamp(11, chgDate);
			pstmt.setString(12, chgUser);
			pstmt.setString(13, chgTerm);
			pstmt.setString(14, "GST payable against D-issue" + tranId);
			pstmt.setString(15, tranId);
			pstmt.setDouble(16, calAmount);
			pstmt.setString(17, transMode);
			pstmt.setTimestamp(18, gpDate);
			pstmt.setString(19, suppCode);
			pstmt.setString(20, lrNo);
			pstmt.setTimestamp(21, lrDate);
			pstmt.setString(22, acctCodeAp);
			pstmt.setString(23, cctrCodeAp);
			pstmt.setString(24, stanCode);
			pstmt.setString(25, taxClass);
			pstmt.setString(26, taxChap);
			pstmt.setString(27, taxEnv);
			pstmt.setString(28, payMode);
			pstmt.setString(29, crTerm);
			pstmt.setString(30, "E");
			pstmt.setString(31, "Y");
			pstmt.setDouble(32, 0.0);
			pstmt.setTimestamp(33, confDate);
			pstmt.setString(34, finEntity);
			pstmt.setDouble(35, calAmount);
			pstmt.setDouble(36, calAmount);
			pstmt.setString(37, "S");
			pstmt.setString(38, "S");
			pstmt.setString(39, suppCode);
			pstmt.setString(40, acctCodeAp);
			pstmt.setDouble(41, calAmount * exchRate);

			int updateCountHeader = pstmt.executeUpdate();
			System.out.println("no of header row update = " + updateCountHeader);

			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			sql ="select tx.acct_code,tx.cctr_code,sum(tx.tax_amt) from taxtran tx, tax t where tx.tax_code=t.tax_code "+
					"	 and t.tax_type in ('I','G','H','J') and tx.tran_id=? and tx.tax_amt <>0 and tran_code='D-ISS' "+
					" GROUP BY tx.acct_code,tx.cctr_code";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			//if (rs.next()) { //Commented by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change
			int rowCnt = 0;
			while (rs.next()) { //Added by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change     
				acctCode=rs.getString(1);
				cctrCode=rs.getString(2);
				amount = rs.getDouble(3);
				System.out.println("Amount calculated: [ " + amount+" ]aactcode["+acctCode+" ]cctr code["+cctrCode+"rate---"+rate);  


				rowCnt++; 
				if ("NULLFOUND".equalsIgnoreCase(cctrCode) || cctrCode == null || cctrCode == ""
						|| cctrCode.length() == 0) {
					cctrCode = cctrCodeAp;
				}

				sql = "insert into misc_vouchdet (tran_id,line_no,acct_code,cctr_code,item_code,amount,tax_amt,tax_class, "
						+ "	tax_chap,tax_env,rate_clg,net_amount,bill_amt) Values (?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1, tranId);
				pstmt2.setInt(2, rowCnt);
				pstmt2.setString(3, acctCode);
				pstmt2.setString(4, cctrCode);
				pstmt2.setString(5, itemCode);
				pstmt2.setDouble(6, amount);
				pstmt2.setDouble(7, 0.0);
				pstmt2.setString(8, taxClass);
				pstmt2.setString(9, taxChap);
				pstmt2.setString(10, taxEnv);
				pstmt2.setDouble(11, rateClg);
				pstmt2.setDouble(12, amount * exchRate);
				pstmt2.setDouble(13, amount);

				int updateCount = pstmt2.executeUpdate();
				System.out.println("no of row update = " + updateCount);

				pstmt2.close();
				pstmt2= null;

				totAmtDet=totAmtDet+Math.abs(amount);
				totNetAmtDet=totNetAmtDet+Math.abs(amount);
				totBillAmtDet=totBillAmtDet+Math.abs(amount);


			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "update misc_voucher set net_amt = ?, bill_amt = ?, tot_amt = ? where tran_id = ?";
			pstmt = conn.prepareStatement(sql);

			pstmt.setDouble(1, totAmtDet);
			pstmt.setDouble(2, totBillAmtDet);
			pstmt.setDouble(3, totNetAmtDet);
			pstmt.setString(4, tranId);
			int updCnt = pstmt.executeUpdate();

			if(pstmt!=null)
			{
				pstmt.close();
				pstmt = null;
			}
			System.out.println("update amount Successfully:"+updCnt);



			MiscValConf MiscValConfObj=new MiscValConf(); //VTSUCC1

			errString=MiscValConfObj.confirm(tranId, xtraParams, "",conn); //Added by Anagha R on 2/9/2020 for Serdia: GST Recovery change
			//Modified by Rohini T on [28/02/2021][Start]

			System.out.println("errString:: "+errString);
			//MiscValConfObj=null;



			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		}

		catch (Exception e) {
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
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}

	///newly added for reverse
	private String generateTranId(String windowName, String xmlValues, Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", errCode = "", errString = "";
		String tranId = null;
		String newKeystring = "";
		String srType = "RS";
		boolean found = false;
		ITMDBAccessEJB itmDBAccessEJB = null;
		try {
			itmDBAccessEJB = new ITMDBAccessEJB();
			sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)=UPPER(?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, windowName);
			rs = pstmt.executeQuery();
			System.out.println("keyString :" + rs.toString());
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rs.next()) {
				found = true;
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (!found) {
				sql = "SELECT key_string,TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					keyString = rs.getString(1);
					keyCol = rs.getString(2);
					tranSer1 = rs.getString(3);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (keyString == null || keyString.trim().length() == 0) {
				errCode = "VTSEQ";
				System.out.println("errcode......" + errCode);
				errString = itmDBAccessEJB.getErrorString("", "VTSEQ", "BASE", "", conn);

			}
			System.out.println("keyString=>" + keyString);
			System.out.println("keyCol=>" + keyCol);
			System.out.println("tranSer1" + tranSer1);

			System.out.println("xmlValues  :[" + xmlValues + "]");

			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);

			System.out.println(" new tranId :" + tranId);
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException ex) {
			System.out.println("Exception ::" + sql + ex.getMessage() + ":");
			ex.printStackTrace();
			tranId = null;
			throw new ITMException(ex);
		} catch (Exception e) {
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			tranId = null;
			throw new ITMException(e);
		}
		return tranId;
	}// generateTranTd()

	private String getCurrdateAppFormat() throws ITMException {
		String s = "";
		// GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		try {
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(genericUtility.getDBDateFormat());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		} catch (Exception exception) {
			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
			throw new ITMException(exception);
		}
		return s;
	}

	private String findValue(Connection conn, String columnName, String tableName, String columnName2, String value)
			throws ITMException, RemoteException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String findValue = "";
		try {
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 + "= ?";
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
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from findValue " + findValue);
		return findValue;
	}

	private java.sql.Timestamp getDueDate(Connection conn, SimpleDateFormat simpleDateFormat, String startFrom,
			String tranDate, String effDate, String billDate, int crDays) throws ITMException {
		Calendar cal = Calendar.getInstance();
		Timestamp dueDate = null;
		try {

			/*
			 * System.out.println("In getDueDate startFrom [" + startFrom + "] tranDate [" +
			 * tranDate + "] effDate [" + effDate + "] billDate [" + billDate + "] crDays ["
			 * + crDays + "]");
			 	System.out.println("In getDueDate  tranDate [" + simpleDateFormat.parse(tranDate) + "]");*/
			if ("R".equalsIgnoreCase(startFrom)) {
				cal.setTime((simpleDateFormat.parse(tranDate)));
				cal.getTime();
				cal.add(Calendar.DATE, crDays);
				dueDate = (Timestamp) cal.getTime();
			} else if ("D".equalsIgnoreCase(startFrom)) {
				cal.setTime((simpleDateFormat.parse(effDate)));
				cal.getTime();
				cal.add(Calendar.DATE, crDays);
				dueDate = (Timestamp) cal.getTime();
			} else if ("Q".equalsIgnoreCase(startFrom)) {
				cal.setTime((simpleDateFormat.parse(effDate)));
				cal.getTime();
				cal.add(Calendar.DATE, crDays);
				dueDate = (Timestamp) cal.getTime();
			} else if ("B".equalsIgnoreCase(startFrom)) {
				cal.setTime((simpleDateFormat.parse(billDate)));
				cal.getTime();
				cal.add(Calendar.DATE, crDays);
				dueDate = (Timestamp) cal.getTime();
			}
			System.out.println("In getDueDate [" + startFrom + "] dueDate [" + simpleDateFormat.format(dueDate) + "]");

		} catch (ParseException e) {
			System.out.println("Exception in  date [duedate]" + e);
			e.printStackTrace();
			throw new ITMException(e);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Returning Due date [duedate] from function" + dueDate);
		return dueDate;
	}
}