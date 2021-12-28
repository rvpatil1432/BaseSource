package ibase.webitm.ejb.dis;

import java.math.BigDecimal;
import java.rmi.RemoteException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
//import ibase.webitm.ejb.dis_exp.PurcOrderAmdTWFInvokeConfirm;
import ibase.webitm.ejb.sys.CreateRCPXML;
import org.w3c.dom.*;
import java.sql.*;
import java.text.SimpleDateFormat;

//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
//import javax.swing.text.Document;
import java.util.Date;

@Stateless
public class POrderAmdConf extends ActionHandlerEJB implements POrderAmdConfLocal, POrderAmdConfRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon = new DistCommon();
	public String actionHandler() throws RemoteException, ITMException
	{
		return "";
	}
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("item actionHandler(...) called............");
		String str = "";
		System.out.println("actionType---" + actionType);
		System.out.println("xmlString---" + xmlString);
		System.out.println("objContext---" + objContext);
		System.out.println("xtraParams---" + xtraParams);
		return str;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		System.out.println("POrderAmdConf confirm called..............");
		String confirmed = "";
		String sql = "";
	
		// String sql1 = "";
		// int status = 0;
		// double frtAmt = 0.0;
		// Date lrDate = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		// PreparedStatement pstmt1 = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null, errCode = "",runMode="";
		ResultSet rs = null;
		// ResultSet rs1 = null;
		String purcOrder = "", poStatus = "", amdNo = "", workflowStatus = "";
		int recCnt = 0, cnt = 0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			
			runMode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "runMode");
			System.out.println("runMode["+runMode+"]");
			if (errString == null || errString.trim().length() == 0)
			{
				amdNo = tranId;
				if (tranId != null && tranId.trim().length() > 0)
				{
					purcOrder = setDescription("purc_order", "poamd_hdr", "amd_no", tranId, conn);

					if (purcOrder != null && purcOrder.trim().length() > 0)
					{
						poStatus = setDescription("status", "porder", "purc_order", purcOrder, conn);

						if ("C".equalsIgnoreCase(poStatus) || "X".equalsIgnoreCase(poStatus))
						{
							errString = "VTPOCX";
							errString = itmDBAccessLocal.getErrorString("", errString, "","",conn);
							return errString;
						}

						sql = "	select confirmed, (case when workflow_status is null then '0' else workflow_status end) " +
						" from   poamd_hdr where amd_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, amdNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							recCnt++;
							confirmed = rs.getString(1);
							workflowStatus = rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (recCnt == 0)
						{
							errString = "VTMCONF20";
							errString = itmDBAccessLocal.getErrorString("", errString, "","",conn);
							return errString;

						} else if ("Y".equalsIgnoreCase(confirmed) && "I".equalsIgnoreCase(runMode))
						{
							errString = "VTPACONF1";
							errString = itmDBAccessLocal.getErrorString("", errString, "","",conn);
							return errString;
						}

						sql = " select count(1) from poamd_det where amd_no = ? and purc_order <> ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, amdNo);
						pstmt.setString(2, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@@@@@111 cnt[" + cnt + "]");
						if (cnt > 0)
						{
							errString = "VTPOHRDT";
							errString = itmDBAccessLocal.getErrorString("", errString, "","",conn);
							return errString;
						}

						sql = " select count(1) from   poamd_term where amd_no = ? and purc_order <> ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, amdNo);
						pstmt.setString(2, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@@@@@222 cnt[" + cnt + "]");
						if (cnt > 0)
						{
							errString = "VTPOHRDT";
							errString = itmDBAccessLocal.getErrorString("", errString, "","",conn);
							return errString;
						}

						if (errString == null || errString.trim().length() == 0)
						{
							errString = RetrievePordAmd(amdNo, xtraParams, conn);
						}
					}
				}
				if (errString == null || errString.trim().length() == 0)
				{
					errString = "PRCUSUCCES";
					errString = itmDBAccessLocal.getErrorString("", errString, "","",conn);
					return errString;
				}

			} // end if errstrng
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		finally
		{
			try
			{
				System.out.println( ">>>>>>>>>>>>>In finaly errString:"+errString);
				if (errString != null && errString.trim().length() > 0 && !("d".equalsIgnoreCase(errString)))
				{
					
					conn.rollback();
					System.out.println("Transaction rollback... ");
					conn.close();
					conn = null;
				}
				else
				{
					conn.commit(); // test
					System.out.println("@@@@ Transaction commit... ");
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("errString[" + errString + "]:::::::::::errCode[" + errCode + "]");
		return errString;
	}

	private String RetrievePordAmd(String amdNo, String xtraParams, Connection conn) throws RemoteException, ITMException
	{

		PreparedStatement pstmt = null, pstmt1 = null, pstmtSql = null;
		ResultSet rs = null, rs1 = null;
		int cnt = 0;
		String retString = "", sql = "", sql1 = "", purcOrder = "", lineNoOrd = "", errCode = "";
		String loginEmpCode = "", ediOption = "", dataStr = "";
		double vouchAdv = 0, amdDetTot = 0, poDetTot = 0, poHdrTot = 0;
		Timestamp sysDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			System.out.println("@@@@@@@@@@@@@@@ RetrievePordAmd confirm called..............");

			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr + ":::loginEmpCode[" + loginEmpCode + "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			sql = " select a.tot_amt, case when a.vouch_adv_amt is null then 0 else a.vouch_adv_amt end " + " from porder a, poamd_hdr b " + " where a.purc_order = b.purc_order " + " and b.amd_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);

			rs = pstmt.executeQuery();
			if (rs.next())
			{
				poHdrTot = rs.getDouble(1);//330600
				vouchAdv = rs.getDouble(2);//261000
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (vouchAdv > 0) // lc_vouchadv > 0 then
			{
				sql = " select purc_order,line_no__ord, tot_amt from poamd_det where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					purcOrder = rs.getString("purc_order");
					lineNoOrd = rs.getString("line_no__ord");
					amdDetTot = rs.getDouble("tot_amt");

					if (lineNoOrd != null && lineNoOrd.trim().length() > 0 && purcOrder != null && purcOrder.trim().length() > 0)
					{
						sql1 = " select tot_amt from porddet where purc_order = ? and line_no = ?  ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, purcOrder);
						pstmt1.setString(2, lineNoOrd);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							poDetTot = rs1.getDouble("tot_amt");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					else
					{
						poDetTot=0;
					}
					System.out.println("poHdrTot" +poHdrTot);
					System.out.println("poDetTot" +poDetTot);
					System.out.println("amdDetTot" +amdDetTot);
					poHdrTot = poHdrTot - poDetTot + amdDetTot;
					System.out.println("poHdrTot after" +poHdrTot);
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("@@@@@@@@@ vouchAdv[" + vouchAdv + "]:::::poHdrTot[" + poHdrTot + "]");

				if (vouchAdv > poHdrTot)
				{
					errCode = "VTADVEXCES";
					errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
					return errCode;
				}
			}

			/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]
			[changed method signature to pass xtraParams to update login user and login terminal]*/
			//errCode = ConfirmPordAmd(amdNo, conn);
			errCode = ConfirmPordAmd(amdNo, xtraParams, conn);
			/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
			
			if (errCode != null && errCode.trim().length() > 0)
			{
				conn.rollback();
			} else
			{
				sql = "select edi_option from transetup where tran_window = 'w_porderamd' ";
				pstmtSql = conn.prepareStatement(sql);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					ediOption = rs.getString("EDI_OPTION");
				}
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
				//changed by  wasim on 09-03-2015 to check edi option null start.
				ediOption = ediOption != null?ediOption:"0";
				int ediOpt = Integer.parseInt(ediOption);
				System.out.println("@@@@@@@@@@@@@@@ ediOption  called next..............");
				if(ediOpt > 0)
				{
					CreateRCPXML createRCPXML = new CreateRCPXML("w_porderamd", "tran_id");
					dataStr = createRCPXML.getTranXML(amdNo, conn);
					System.out.println("dataStr =[ " + dataStr + "]");
					Document ediDataDom = genericUtility.parseString(dataStr);

					E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
					retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_porderamd", ""+ediOpt , xtraParams, conn);
					createRCPXML = null;
					e12CreateBatchLoad = null;

					if (retString != null && "SUCCESS".equals(retString))
					{
						System.out.println("retString from batchload = [" + retString + "]");
					}
				}
				
				/*if ("2".equals(ediOption))
				{','4','line_no__ord');
insert into obj_itemchange(obj_name,form_no,field_name)values('porderamd
					CreateRCPXML createRCPXML = new CreateRCPXML("w_porderamd", "tran_id");
					dataStr = createRCPXML.getTranXML(amdNo, conn);
					System.out.println("dataStr =[ " + dataStr + "]");
					Document ediDataDom = genericUtility.parseString(dataStr);

					E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
					retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_porderamd", "2", xtraParams, conn);
					createRCPXML = null;
					e12CreateBatchLoad = null;

					if (retString != null && "SUCCESS".equals(retString))
					{
						System.out.println("retString from batchload = [" + retString + "]");
					}
				}

				else
				{

					CreateRCPXML createRCPXML = new CreateRCPXML("w_porderamd", "tran_id");
					dataStr = createRCPXML.getTranXML(amdNo, conn);
					System.out.println("dataStr =[ " + dataStr + "]");
					Document ediDataDom = genericUtility.parseString(dataStr);

					E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
					retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_porderamd", ediOption, xtraParams, conn);
					createRCPXML = null;
					e12CreateBatchLoad = null;

					if (retString != null && "SUCCESS".equals(retString))
					{
						System.out.println("retString from batchload = [" + retString + "]");
					}
				}
				System.out.println("@@@@@@@@@@@@@@@ ediOption  called end..............");*/
				//changed by  wasim on 09-03-2015 to check edi option null end.

			}

			if (errCode == null || errCode.trim().length() == 0)
			{
				sql = " update poamd_hdr set confirmed = 'Y',conf_date = ?, emp_code__aprv = ? where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, loginEmpCode);
				pstmt.setString(3, amdNo);
				cnt = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("@@@@@@ cnt...[" + cnt + "]");
				if (!(cnt == 1))
				{
					errCode = "VTNOAMD";
					errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
					return errCode;
				}
			}

			if (errCode != null && errCode.trim().length() > 0)
			{
				conn.rollback();
			} else
			{
				conn.commit();
			}
		} catch (Exception e)
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return errCode;
	}
	/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]
	[changed method signature to pass xtraParams to update login user and login terminal]*/			
	//public String ConfirmPordAmd(String amdNo, Connection conn) throws RemoteException, ITMException
	public String ConfirmPordAmd(String amdNo, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null, pstmt1 = null, pstmt3 = null, pstmtSql = null, pstmt4 = null,pstmt2 = null,pstmt5 = null,pstmt6 = null, pstmt7 = null, pstmtmax = null, pstmtcount = null,pstmtupd = null;
		PreparedStatement pstmtopen=null,pstmtrcp=null,pstmtret=null,pstmtold=null;
		ResultSet rsopen=null,rsrcp=null,rsret=null,rsold=null;
		ResultSet rs = null, rs1 = null, rs3 = null, rs2 = null, rs6= null, rs7 = null, rsmax= null, rscount= null;
		int cnt = 0 ,cnt1 = 0,cntpo = 0;
		// Document dom = null;
		String retString = "", sql = "", sql1 = "", sql3 = "", sql2 = "", sql5= "", sql6= "", sql7= "", purcOrder = "", lineNoOrd = "", errCode = "", sql4 = null;
		// String loginEmpCode = "", ediOption = "", dataStr = "";
		// double pohdrTot = 0, vouchAdv = 0, amdDetTot = 0, poDetTot = 0,
		// poHdrTot = 0;
		String errString="";
		Timestamp sysDate = null, taxDate = null, refDate = null, amdDate = null;
		String siteCodeDlv = "", siteCodeOrd = "", siteCodeBill = "", deptCode = "", empCode = "", itemSer = "", taxOpt = "", crTerm = "";
		String currCode = "", taxChapHdr = "", taxClassHdr = "", taxEnvHdr = "", remarks = "", projCode = "",projCodeDet="", salesPers = "", commPerc = "", commPercOn = "";
		String currCodeComm = "", quotNo = "", tranCode = "", currCodeFrt = "", frtTerm = "", dlvTerm = "", currCodeIns = "", empCodeAprv = "", suppCode = "";
		double ordAmt = 0, taxAmt = 0, totAmt = 0, exchRate = 0, frtAmt = 0, insuranceAmt = 0;
		String siteCode = "", indNo = "", itemCode = "", lineNo = "", discountType = "", taxClass = "";
		double quantity = 0, rate = 0, convQtyStduom = 0, convRtuomStduom = 0, quantityStduom = 0, rateStduom = 0;
		double discount = 0, rateClg = 0;
		String UnitStd = "", unitRate = "";
		Timestamp reqDate = null, dlvDate = null;
		java.sql.Date duedate=null;
		String taxChap = "", taxEnv = "", workOrder = "", packCode = "", packInstr = "", acctCodeDr = "", cctrCodeDr = "";
		String acctCodeCr = "", cctrCodeCr = "";
		double noArt = 0;
		String locCode = "", status = "", specificInstr = "", suppCodeMnfr = "", unit = "";
		String specialInstr = "", benefitType = "", licenceNo = "", dutyPaid = "", formNo = "", lineNoPO = "", lineNoPO1 = "";

		String lineNoTax = "", taxCode = "", taxBase = "", taxPerc = "", chgStat = "", taxSet = "", effect = "", acctCodeReco = "";
		String cctrCodeReco = "", recoPerc = "", acctCode = "", cctrCode = "", rateType = "", round = "", roundTo = "", taxForm = "",excedAmt1="";
		String acctCodeApAdv = "", cctrCodeApAdv = ""; // VALLABH KADAM Add two
													   // new field 19/NOV/14
		String chgUser = "", chgTerm = "", posted = "", payTax = "", orderOpt = "", bom = "", oldItem = "", lastTermLine = "",lastTermLine1 = "", lineNoPOterm ="";
		double taxableAmt = 0, recoAmont = 0, dlvQuantity = 0, clgRate = 0, ordQty = 0, totOrdQty = 0, quantStduom = 0;
		Timestamp taxFormDate = null, chgDate = null;
		int updCnt = 0, updCnt1=0;
		double taxAmtS = 0, totAmtS = 0, ordAmtS = 0;
		double vouchAmt = 0;
		String advance = "";
		String frtType = "";
		double frtRate = 0, frtAmtQty = 0, frtAmtFixed = 0;
		double relamt=0,relafter=0,retperc=0,adjustmentperc=0;
		String adjmet ="", acctcode="",cctr_code="", taxclass="",taxchap="",taxenv="" ,sitecodeadv="",vouchcreated=""; 
		String  taskcode="",type="",relagnst="",amttype="",fchgtype="",refcode=""; 
		/*min_day,max_day,fin_chg,fchg_type,min_amt,max_amt*/
		double minday=0,maxday=0,finchg=0,minamt=0,maxamt=0,indquantity =0;
		int cntTerm = 0;
		int cntPyterm =0;
		int cntDyterm =0;
		String linnoprev= "";
        String allowover="";
        String taskcodeParent="";
        double apprvlead= 0 ;
        String lineNoold="";
        String remark="";
        double totAmtpoamdtemp =0,approxCost = 0,totAmtDetpordertot = 0,totAmtPoamd =0,totAmtDetpordertotNtc = 0.0 ,totAmtDetpordertotPorcpConf=0.0, totAmtDetpordertotPorcpSer=0.0;
        String pordType ="";
		String varValue ="",remarkDet = "";
		boolean ValueType = false, firstNull=true;
		DistCommon discommon = new DistCommon();
		String dimension = "";
		/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
		String loginUsr = "", termId = "", itemCodeInd = "", siteCodeInd= "";
		Timestamp reqDateInd = null;
		double quantityOld = 0;
		/**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		String termCode = "", descr = "", printOpt = "";
		try
		{
			System.out.println("@@@@@@@@@@@@@@@ ConfirmPordAmd method called next..............");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
			loginUsr = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
		    HashMap demandSupplyMap = new HashMap();
			/**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
			sql = " select purc_order,site_code__dlv,site_code__ord,site_code__bill,dept_code,emp_code,item_ser,tax_opt," + "	cr_term,ord_amt,tax_amt,tot_amt,curr_code,exch_rate,tax_chap,tax_class,tax_env,remarks,tax_date," + " proj_code,sales_pers,comm_perc,comm_perc__on,curr_code__comm,quot_no,tran_code,frt_amt,curr_code__frt," + " frt_term,dlv_term,insurance_amt,curr_code__ins,emp_code__aprv,ref_date,amd_date,supp_code," + "  FRT_TYPE, FRT_RATE,FRT_AMT__QTY, FRT_AMT__FIXED " + " from poamd_hdr where amd_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				cnt++;
				purcOrder = rs.getString("purc_order");
				siteCodeDlv = rs.getString("site_code__dlv");
				siteCodeOrd = rs.getString("site_code__ord");
				siteCodeBill = rs.getString("site_code__bill");
				deptCode = rs.getString("dept_code");
				empCode = rs.getString("emp_code");
				itemSer = rs.getString("item_ser");
				taxOpt = rs.getString("tax_opt");
				crTerm = rs.getString("cr_term");
				ordAmt = rs.getDouble("ord_amt");
				taxAmt = rs.getDouble("tax_amt");
				totAmt = rs.getDouble("tot_amt");
				currCode = rs.getString("curr_code");
				exchRate = rs.getDouble("exch_rate");
				taxChapHdr = rs.getString("tax_chap");
				taxClassHdr = rs.getString("tax_class");
				taxEnvHdr = rs.getString("tax_env");
				remarks = rs.getString("remarks");
				taxDate = rs.getTimestamp("tax_date");
				projCode = rs.getString("proj_code");
				salesPers = rs.getString("sales_pers");
				commPerc = rs.getString("comm_perc");
				commPercOn = rs.getString("comm_perc__on");
				currCodeComm = rs.getString("curr_code__comm");
				quotNo = rs.getString("quot_no");
				tranCode = rs.getString("tran_code");
				frtAmt = rs.getDouble("frt_amt");
				currCodeFrt = rs.getString("curr_code__frt");
				frtTerm = rs.getString("frt_term");
				dlvTerm = rs.getString("dlv_term");
				insuranceAmt = rs.getDouble("insurance_amt");
				currCodeIns = rs.getString("curr_code__ins");
				empCodeAprv = rs.getString("emp_code__aprv");
				refDate = rs.getTimestamp("ref_date");
				amdDate = rs.getTimestamp("amd_date");
				suppCode = rs.getString("supp_code");
				// added by cpatil
				frtType = rs.getString("FRT_TYPE");
				frtRate = rs.getDouble("FRT_RATE");
				frtAmtQty = rs.getDouble("FRT_AMT__QTY");
				frtAmtFixed = rs.getDouble("FRT_AMT__FIXED");
				// end

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (cnt == 0)
			{
				errCode = "VTNOAMD";
				errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
				return errCode;
			}
			
			/*Request ID -D15ISUN003 Start Changed By Priyanka Das */
			
			double totAmtnotcurr = 0.0;
			sql = "select pord_type from porder where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				pordType = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("Amdtype.........." + pordType);
			varValue = discommon.getDisparams("999999","TYPE_ALLOW_PROJBUDGET", conn);
			System.out.println(">>>>>>>>>In for loop afer schemeKey varValue:"+varValue);
			varValue = varValue.trim();
			if(varValue != null && varValue.trim().length() > 0)
			{
				String varValArr[] = varValue.split(",");
				ArrayList varValList = new ArrayList<String>(Arrays.asList(varValArr));
				
				System.out.println("varValList ::"+varValList);
				System.out.println("varValList.contains(pordType) ::"+varValList.contains(pordType));
				
				if(varValList.contains(pordType))
				{
					ValueType = true;
				}
				else
				{
					ValueType = false;
				}
				
			}
			
			System.out.println("ValueType11111>>>>"+ValueType);
			if (varValue != null && varValue.trim().length() > 0 && varValue != "NULLFOUND" && ValueType == true)
			{
				System.out.println("ValueType2222222>>>>"+ValueType);
			
				System.out.println("Project Code>>>> [" + projCode);
				double totAmtpoamdtempold=0;
				 /* sql="select line_no__ord , proj_code from poamd_det where amd_no  = ?";
				  pstmt1 = conn.prepareStatement(sql);
				  pstmt1.setString(1, amdNo);
				  System.out.println("Line is for poamdconf....."+ projCode);
				  rs1 = pstmt1.executeQuery();
				  while(rs1.next())
					{
						lineNoold= rs1.getString(1);
						projCode= rs1.getString(2);
						
						System.out.println(" Abhi  Line is " +lineNoold);
						
			/*sql ="select a.proj_code, sum(a.tot_amt * b.exch_rate)" +//a.net_amt * b.exch_rate
					" from poamd_det a,poamd_hdr b " +
					"where a.amd_no = b.amd_no  and a.line_no__ord= ?" +
					"and a.amd_no  = ? group by a.proj_code";*/
						sql="select sum(a.tot_amt * b.exch_rate),a.proj_code from poamd_det a,poamd_hdr b " +
						"where a.amd_no = b.amd_no and a.amd_no  = ? group by a.proj_code";
			            System.out.println("AmdNo>>>>>" + amdNo);
			            pstmt = conn.prepareStatement(sql);
			            pstmt.setString(1, amdNo);
			            rs = pstmt.executeQuery();
			           while (rs.next()) 
			              {
				           totAmtpoamdtemp = rs.getDouble(1);
				           projCode= rs.getString(2);
				           System.out.println("TOTAMT OF POAMD"+totAmtpoamdtemp);
				           System.out.println("Project-" + projCode);
				           
				           sql2 ="select approx_cost from project where proj_code =?";
				           pstmt2 = conn.prepareStatement(sql2);
				           pstmt2.setString(1,projCode);
				           rs2 = pstmt2.executeQuery();
				           if(rs2.next())
				             {
					             approxCost = rs2.getDouble(1);
				             }
				          System.out.println("APPROXCOST FOR PROJECT CODE>>>>"+approxCost);
				          rs2.close();
				          rs2 = null;
				          pstmt2.close();
				          pstmt2 = null; 
				          
				          sql3 = "select sum(a.tot_amt * b.exch_rate) from porddet a,porder b " +
					         "where (a.purc_order = b.purc_order) and" +
					         " b.confirmed = 'Y' and a.proj_code = ? and b.purc_order = ? and a.status ! = 'C' and b.status ! = 'X' and" +
					         " a.line_no NOT IN (select line_no__ord from poamd_det where amd_no = ? " +
					         "and proj_code = ? and line_no__ord is not null) ";
				         pstmt3 = conn.prepareStatement(sql3);
				         pstmt3.setString(1,projCode);
				         pstmt3.setString(2,purcOrder);
				         pstmt3.setString(3,amdNo);
				         pstmt3.setString(4,projCode);
				         System.out.println("Project code is for poamdvalidation....."+ projCode);
				         rs3 = pstmt3.executeQuery();
				         if(rs3.next())
				            {
					          totAmtnotcurr = rs3.getDouble(1) ;
					
				            }
				        System.out.println("totAmtnotcurr>>>>"+totAmtnotcurr);
				        rs3.close();
				        rs3 = null;
				        pstmt3.close();
				        pstmt3 = null; 
				        System.out.println("@@@@@@@ In @@@@@@@@@");
				        sql= "select sum(a.net_amt * b.exch_rate) from " +
						     "porcpdet a, porcp b ,porddet c " +
						" where ( a.purc_order = c.purc_order ) " +
						" and (a.tran_id = b.tran_id ) and a.line_no__ord = c.line_no " +
						" and b.confirmed = 'Y' and c.proj_code = ?  " +
						" and b.status ! = 'X'"  +
						" and c.status = 'C'  and  b.tran_ser='P-RCP' ";
			    pstmtrcp = conn.prepareStatement(sql);
			    pstmtrcp.setString(1, projCode);
			       //pstmt.setString(2, pordNo);
			    rsrcp = pstmtrcp.executeQuery();
			    if (rsrcp.next()) 
			      {
				     totAmtDetpordertotPorcpConf= rsrcp.getDouble(1);
				     System.out.println(" POrcp VALUE>>>>>>>> "+ totAmtDetpordertotPorcpConf);

			      }
			      rsrcp.close();
			      rsrcp = null;
			      pstmtrcp.close();
			      pstmtrcp = null;
			      System.out.println("purc_order"+ purcOrder);
				  sql = "select sum( a.tot_amt * b.exch_rate) from porddet a, porder b"
					+ " where ( a.purc_order = b.purc_order ) and b.confirmed = 'Y' and a.proj_code = ? and b.status! = 'X' and a.status! ='C' and b.purc_order != ? " ;

			    pstmtopen = conn.prepareStatement(sql);
			    pstmtopen.setString(1, projCode);
			    pstmtopen.setString(2, purcOrder);
			    rsopen = pstmtopen.executeQuery();
			    if (rsopen.next()) 
			      {
				     totAmtDetpordertotNtc= rsopen.getDouble(1);
				     System.out.println("totAmtDet for Porder without  in  loop and status pordet is not C>>>>>>>> "+ totAmtDetpordertotNtc);

			      }
			      rsopen.close();
			      rsopen = null;
			      pstmtopen.close();
			      pstmtopen = null;
			          
				  sql= "select sum(a.net_amt * b.exch_rate) from porcpdet a, porcp b ,porddet c " +
					   " where ( a.purc_order = c.purc_order )" +
					   "and (a.tran_id = b.tran_id )" +
					   "and b.confirmed = 'Y' and a.line_no__ord = c.line_no and  c.proj_code = ?" +
					   " and b.status ! = 'X'and c.status = 'C' and  b.tran_ser= 'P-RET'";
				  pstmtret = conn.prepareStatement(sql);
				  pstmtret.setString(1, projCode);
					     //  pstmt.setString(2, pordNo);
				  rsret = pstmtret.executeQuery();
			      if (rsret.next()) 
			         {
					    totAmtDetpordertotPorcpSer = rsret.getDouble(1);
						System.out.println(" return value is>>>>>>>> "+ totAmtDetpordertotPorcpSer);

					  }
				  rsret.close();
				  rsret = null;
				  pstmtret.close();
				  pstmtret = null;
				  System.out.println("totAmtDetpordertotNtc["+totAmtDetpordertotNtc+"] + totAmtDetpordertotPorcpConf["+totAmtDetpordertotPorcpConf+"] - totAmtDetpordertotPorcpSer["+totAmtDetpordertotPorcpSer+"]");
				  
				totAmtDetpordertot= totAmtDetpordertotNtc + totAmtDetpordertotPorcpConf + totAmtnotcurr - totAmtDetpordertotPorcpSer;
				
				totAmtPoamd =  totAmtDetpordertot + totAmtpoamdtemp;
				System.out.println("totAmtDetpordertot["+totAmtDetpordertot+"] + totAmtpoamdtemp["+totAmtpoamdtemp+"] ");
				System.out.println("totAmtDetpordertot["+totAmtDetpordertot+"] + totAmtpoamdtemp["+totAmtpoamdtemp+"] ");
				if(totAmtPoamd >  approxCost)
				{
					System.out.println("In approxcost validation>>");
					errCode = "VTPROJCNF";
					
					errCode = new ValidatorEJB().getErrorString("", errCode, "");
					System.out.println("::: errCode["+errCode+"]");
					String startStr = errCode.substring(0,errCode.indexOf("<description>") + 13);
					
					String endStr = errCode.substring(errCode.indexOf("</description>"),errCode.length());
					System.out.println("endStr ["+endStr+"]");
					String descrStr = errCode.substring(errCode.indexOf("<description>") + 13,errCode.indexOf("</description>"));
					System.out.println("::: descrStr ["+descrStr+"]");
					String descrStart = descrStr.substring(0, descrStr.indexOf("]"));
					String descrEnd = descrStr.substring(descrStr.indexOf("]"),descrStr.length());
					
					String value = " ;    Amount Exceeded  Project Code :      " + projCode  
							+" ;   Project Aprroved Amount :    "+ BigDecimal.valueOf(approxCost).toPlainString()
						//	+" ;   Consumed Amount :    "+ BigDecimal.valueOf(totAmtDetpordertot + totAmtnotcurr).toPlainString()
							+" ;   Consumed Amount :  "+ BigDecimal.valueOf(totAmtDetpordertot).toPlainString()
							+" ;   Current Purchase Order Amendment Amount :   "+ BigDecimal.valueOf(totAmtpoamdtemp).toPlainString()
						//	+" ;   Exceeded Amount :   "+ BigDecimal.valueOf(totAmtPoamd - approxCost).toPlainString();
						//	+" ;   Exceeded Amount :  "+ BigDecimal.valueOf(approxCost - totAmtDetpordertot).toPlainString();
							//+" ;   Exceeded Amount :  "+ BigDecimal.valueOf( totAmtpoamdtempold + totAmtDetpordertot - approxCost).toPlainString();
							+" ;   Exceeded Amount :  "+ BigDecimal.valueOf( totAmtPoamd - approxCost ).toPlainString();
							
							
					System.out.println("Value ::: "+ value);
					descrStart = descrStart.concat(value).concat(descrEnd);
					errCode = startStr.concat(descrStart).concat(endStr);
					
					return errCode;
					
				}
				   totAmtpoamdtemp =0;
				   approxCost = 0;
				   totAmtDetpordertot = 0;
				   totAmtPoamd =0;
				   totAmtDetpordertotNtc = 0.0;
				   totAmtDetpordertotPorcpConf=0.0; 
				   totAmtDetpordertotPorcpSer=0.0;
			
			}
			
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
					//}//end while
			
			}
			/*Request ID -D15ISUN003 End*/
			
			
			/**
			 * VALLABH select Advance account code CCTR code from header
			 * supp_code VALLABH KADAM 19/NOV/14 END
			 */
		//commented by-Monika-20-May-2019
			/*	sql = "select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV from supplier where supp_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, suppCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				acctCodeApAdv = rs.getString("ACCT_CODE__AP_ADV");
				cctrCodeApAdv = rs.getString("CCTR_CODE__AP_ADV");

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			*/
			//changes by monika-20-may-2019
			//purchaseorder and sitecode
			
			cctrCodeApAdv=finCommon.getAcctDetrTtype("", itemSer, "APADV", pordType, siteCodeOrd, conn);
				//  gbf_acct_detr_ttype("",ls_itemser,'PO', ls_type);
			System.out.println("tuesdayconf:"+type);
			acctCodeApAdv = distCommon.getToken(cctrCodeApAdv, ",");
		  
			
			
			//changes by monika-22-may-2019
			if (cctrCodeApAdv != null
					&& cctrCodeApAdv.trim().length() > 0) {
				String ls_cctr_drArray[] = cctrCodeApAdv
						.split(",");
				System.out
						.println("@@@@@ ls_cctr_drArray.length["
								+ ls_cctr_drArray.length + "]");
				if (ls_cctr_drArray.length > 0) {
					acctCodeApAdv = ls_cctr_drArray[0];
									}
				if (ls_cctr_drArray.length > 1) {
					acctCodeApAdv = ls_cctr_drArray[0];
					cctrCodeApAdv = ls_cctr_drArray[1];
				}

			}
			//end
		 
			//changes by monika-22-may-2019		
		if((acctCodeApAdv ==null || acctCodeApAdv.trim().length() == 0)) {
		
				sql = "select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV from supplier where supp_code=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctCodeApAdv = rs.getString("ACCT_CODE__AP_ADV");
					cctrCodeApAdv = rs.getString("CCTR_CODE__AP_ADV");

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}
		 if(acctCodeApAdv == null )
		  {
			  errString= itmDBAccessLocal.getErrorString("", "VTSUPPAC", "","",conn);
			  return errString;
		  }
			//end

			/**
			 * VALLABH select Advance account code CCTR code from header
			 * supp_code VALLABH KADAM 19/NOV/14 END
			 */
			System.out.println("000000000000000 suppCode[" + suppCode + "]");
			System.out.println("000000000000000 acctCodeApAdv[" + acctCodeApAdv + "]");
			System.out.println("000000000000000 cctrCodeApAdv[" + cctrCodeApAdv + "]");

			sql = " select count(*) from poamd_det where amd_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				cnt = rs.getInt(1);

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@@@ inside cnt " + cnt);
			if (cnt > 0)
			{
				System.out.println("@@@@@@ inside cnt " + cnt);

				sql1 = " select purc_order,line_no__ord,site_code,ind_no,item_code,quantity,rate,tax_amt,tot_amt,req_date,"
				+ "	conv__qty_stduom,conv__rtuom_stduom,quantity__stduom,rate__stduom,line_no,unit,discount,"
						+ " discount_type,tax_class,tax_chap,tax_env,remarks,work_order,pack_code,pack_instr,acct_code__dr,"
				+ "	cctr_code__dr,acct_code__cr,cctr_code__cr,no_art,unit__std,unit__rate,dlv_date,loc_code,status,"
				//		+ "	specific_instr,supp_code_mnfr,rate__clg,special_instr,benefit_type,licence_no,duty_paid,form_no,proj_code"//Modifiedby Rohini T on 16/04/2021 
				+ "	specific_instr,supp_code_mnfr,rate__clg,special_instr,benefit_type,licence_no,duty_paid,form_no,proj_code,dimension" 
				+ " from   poamd_det where  amd_no = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, amdNo);
				rs1 = pstmt1.executeQuery();
				while (rs1.next())
				{
					System.out.println("@@@@@@ inside while ");
					purcOrder = checknull(rs1.getString("purc_order"));
					lineNoOrd = checknull(rs1.getString("line_no__ord"));
					siteCode = checknull(rs1.getString("site_code"));
					indNo = checknull(rs1.getString("ind_no"));
					itemCode = checknull(rs1.getString("item_code"));
					quantity = rs1.getDouble("quantity");
					rate = rs1.getDouble("rate");
					taxAmt = rs1.getDouble("tax_amt");
					totAmt = rs1.getDouble("tot_amt");
					reqDate = rs1.getTimestamp("req_date");
					convQtyStduom = rs1.getDouble("conv__qty_stduom");
					convRtuomStduom = rs1.getDouble("conv__rtuom_stduom");
					quantityStduom = rs1.getDouble("quantity__stduom");
					rateStduom = rs1.getDouble("rate__stduom");
					lineNo = checknull(rs1.getString("line_no"));
					unit = rs1.getString("unit");
					discount = rs1.getDouble("discount");
					discountType = checknull(rs1.getString("discount_type"));
					taxClass = checknull(rs1.getString("tax_class"));
					taxChap = checknull(rs1.getString("tax_chap"));
					taxEnv = checknull(rs1.getString("tax_env"));
					//remarks = checknull(rs1.getString("remarks"));
					remarkDet = checknull(rs1.getString("remarks"));
					workOrder = checknull(rs1.getString("work_order"));
					packCode = checknull(rs1.getString("pack_code"));
					packInstr = checknull(rs1.getString("pack_instr"));
					acctCodeDr = checknull(rs1.getString("acct_code__dr"));
					cctrCodeDr = checknull(rs1.getString("cctr_code__dr"));
					acctCodeCr = checknull(rs1.getString("acct_code__cr"));
					cctrCodeCr = checknull(rs1.getString("cctr_code__cr"));
					noArt = rs1.getDouble("no_art");
					UnitStd = rs1.getString("unit__std");
					unitRate = rs1.getString("unit__rate");
					dlvDate = rs1.getTimestamp("dlv_date");
					locCode = checknull(rs1.getString("loc_code"));
					status = rs1.getString("status");
					specificInstr = checknull(rs1.getString("specific_instr"));
					suppCodeMnfr = checknull(rs1.getString("supp_code_mnfr"));
					rateClg = rs1.getDouble("rate__clg");
					specialInstr = checknull(rs1.getString("special_instr"));
					benefitType = checknull(rs1.getString("benefit_type"));
					licenceNo = checknull(rs1.getString("licence_no"));
					dutyPaid = checknull(rs1.getString("duty_paid"));
					formNo = checknull(rs1.getString("form_no"));
					projCodeDet = checknull(rs1.getString("proj_code"));
					dimension = checknull(rs1.getString("dimension"));//Modifiedby Rohini T on 16/04/2021 
					System.out.println("@@@@@@@@ indNo [" + indNo + "]");
					System.out.println("CLG rate  select from poamd_det[" + rateClg + "]");
					if (status == null|| status.trim().length() == 0)
					{
						status="O";
					}
					if (indNo != null && indNo.trim().length() > 0)
					{
						/**Modified by Pavan Rane 24dec19 start[fetiching colums like eq_date, item_code, site_code to update with demand/supply in summary table(RunMRP process) related changes]*/
						//sql = " select quantity__stduom,ord_qty from indent where  ind_no = ? ";
						sql = " select quantity__stduom,ord_qty, req_date, item_code, site_code from indent where  ind_no = ? ";
						/**Modified by Pavan Rane 24dec19 end[fetiching colums like eq_date, item_code, site_code to update with demand/supply in summary table(RunMRP process) related changes]*/
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, indNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							indquantity = rs.getDouble("quantity__stduom");//Changed variable to indquantity from quantity for indent value by Priyanka Das
							ordQty = rs.getDouble("ord_qty");
							/**Modified by Pavan Rane 24dec19 start[fetiching colums like eq_date, item_code, site_code to update with demand/supply in summary table(RunMRP process) related changes]*/
					    	reqDateInd = rs.getTimestamp("req_date");
					    	itemCodeInd = rs.getString("item_code");
					    	siteCodeInd = rs.getString("site_code");
					    	/**Modified by Pavan Rane 24dec19 end[fetiching colums like eq_date, item_code, site_code to update with demand/supply in summary table(RunMRP process) related changes]*/
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						// adding ord qty from table + ord qty entered
						sql = "	select quantity__stduom from porddet where purc_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							quantStduom = rs.getDouble("quantity__stduom");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						totOrdQty = (ordQty - quantStduom) + quantityStduom;
						System.out.println("totOrdQty["+totOrdQty+"] ordQty["+ordQty+"] quantStduom["+quantStduom+"] quantityStduom["+quantityStduom+"]");
						if (indquantity > totOrdQty)//Changed variable to indquantity from quantity  for indent value by Priyanka Das
						{
							sql = " update indent set status = 'O', status_date = ? , ord_qty = ?," + " unit__ord   = ?  where  ind_no  = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, sysDate);
							pstmt.setDouble(2, totOrdQty);
							pstmt.setString(3, UnitStd);
							pstmt.setString(4, indNo);
							updCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

						} else
						// status= 'L' (complete)
						{
							sql = " update indent set status = 'L', status_date = ? , ord_qty = ? ," + " unit__ord = ? where  ind_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, sysDate);
							pstmt.setDouble(2, totOrdQty);
							pstmt.setString(3, UnitStd);
							pstmt.setString(4, indNo);
							updCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

						}
						if (updCnt == 0)
						{
							errCode = "VTINDUPD";
							errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
							return errCode;
						}/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
						else
						{							
						    //InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
						    //HashMap demandSupplyMap = new HashMap();
						    demandSupplyMap.put("site_code", siteCodeInd);
							demandSupplyMap.put("item_code", itemCodeInd);		
							demandSupplyMap.put("ref_ser", "IND");
							demandSupplyMap.put("ref_id", indNo);
							demandSupplyMap.put("ref_line", "NA");
							demandSupplyMap.put("due_date", reqDateInd);		
							demandSupplyMap.put("demand_qty", 0.0);
							demandSupplyMap.put("supply_qty", (totOrdQty- ordQty) *(-1));
							demandSupplyMap.put("change_type", "C");
							demandSupplyMap.put("chg_process", "T");
							demandSupplyMap.put("chg_user", loginUsr);
						    demandSupplyMap.put("chg_term", termId);	
						    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
						    demandSupplyMap.clear();
						    if(errString != null && errString.trim().length() > 0)
						    {
						    	System.out.println("errString["+errString+"]");
				                return errString;
						    }
						}
						/**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
					}

					System.out.println("@@@@@@ lineNoOrd indNo [" + indNo + "]:::lineNoOrd[" + lineNoOrd + "]");

					if (lineNoOrd != null && lineNoOrd.trim().length() > 0)
					{
						System.out.println("@@@@@@ inside indNo [" + indNo + "]:::lineNoOrd[" + lineNoOrd + "]");
						/**Modified by Pavan Rane 24dec19 start[fetching quantity__stduom to with update demand/supply in summary table(RunMRP process) related changes]*/
						//sql3 = " select item_code from  porddet where purc_order = ? and  line_no = ? ";
						sql3 = " select item_code, quantity__stduom from  porddet where purc_order = ? and  line_no = ? ";
						/**Modified by Pavan Rane 24dec19 end[fetching quantity__stduom to with update demand/supply in summary table(RunMRP process) related changes]*/
						pstmt3 = conn.prepareStatement(sql3);
						pstmt3.setString(1, purcOrder);
						pstmt3.setString(2, lineNoOrd);
						rs3 = pstmt3.executeQuery();
						if (rs3.next())
						{
							oldItem = rs3.getString("item_code");
							quantityOld = rs3.getDouble("quantity__stduom");

							{
								System.out.println("@@@@@@@@@@oldItem[" + oldItem + "]::::itemCode[" + itemCode + "]");
								if (!(itemCode.equalsIgnoreCase(oldItem)))
								{
									errCode = "VTITEMDIFF";
									errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
									return errCode;
								}
								System.out.println("Status is " + status);
								sql = " update porddet set site_code = ?,quantity = ?,rate = ?,tax_amt = ?,tot_amt = ?,"
								+ " remarks = ?, discount_type = ?,discount = ?,req_date = ?,conv__qty_stduom = ?,"
										+ " conv__rtuom_stduom = ?,quantity__stduom = ?,rate__stduom = ?,tax_class = ?,"
								+ " tax_chap = ?, tax_env= ?,status = ?,status_date = ?, pack_instr = ?, specific_instr= ?,"
										+ " supp_code__mnfr = ?,rate__clg = ?,special_instr = ?,dlv_date = ?,benefit_type = ?,"
								+ " licence_no	= ?,acct_code__ap_adv=?,cctr_code__ap_adv=?,proj_code=?,"
								//+ " acct_code__dr= ?,cctr_code__dr= ?, acct_code__cr= ? ,cctr_code__cr= ? where purc_order = ?	and line_no = ? ";//Modified by Rohini T on 16/04/2021
								+ " acct_code__dr= ?,cctr_code__dr= ?, acct_code__cr= ? ,cctr_code__cr= ? ,no_art= ? ,dimension= ? where purc_order = ?	and line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setDouble(2, quantity);
								pstmt.setDouble(3, rate);
								pstmt.setDouble(4, taxAmt);
								pstmt.setDouble(5, totAmt);
								//pstmt.setString(6, remarks);
								pstmt.setString(6, remarkDet);
								pstmt.setString(7, discountType);
								pstmt.setDouble(8, discount);
								pstmt.setTimestamp(9, reqDate);
								pstmt.setDouble(10, convQtyStduom);
								pstmt.setDouble(11, convRtuomStduom);
								pstmt.setDouble(12, quantityStduom);
								pstmt.setDouble(13, rateStduom);
								pstmt.setString(14, taxClass);
								pstmt.setString(15, taxChap);
								pstmt.setString(16, taxEnv);
								pstmt.setString(17, status);
								pstmt.setTimestamp(18, sysDate);
								pstmt.setString(19, packInstr);
								pstmt.setString(20, specificInstr);
								pstmt.setString(21, suppCodeMnfr);
								// pstmt.setDouble(22, clgRate);
								pstmt.setDouble(22, rateClg); // VALLABH KADAM
															  // 7-NOV-14
															  // DI3ESUN002
															  // change variable
															  // from [clgRate]
															  // to [rateClg]
								pstmt.setString(23, specialInstr);
								pstmt.setTimestamp(24, dlvDate);
								pstmt.setString(25, benefitType);
								pstmt.setString(26, licenceNo);
								pstmt.setString(27, acctCodeApAdv);
								pstmt.setString(28, cctrCodeApAdv);
								pstmt.setString(29, projCodeDet);
								pstmt.setString(30, acctCodeDr);
								pstmt.setString(31, cctrCodeDr);
								pstmt.setString(32, acctCodeCr);
								pstmt.setString(33, cctrCodeCr);
								pstmt.setDouble(34, noArt);//Modified by Rohini T on 16/04/2021
								pstmt.setString(35, dimension);
								pstmt.setString(36, purcOrder);
								pstmt.setString(37, lineNoOrd);
								System.out.println("Acctcode Dr" + acctCodeDr);
								System.out.println("cctrCode Dr" + cctrCodeDr);
								System.out.println("cctrCode Dr" + acctCodeCr);
								System.out.println("cctrCode Cr" + cctrCodeCr);

								updCnt = pstmt.executeUpdate();
								pstmt.close();
								pstmt = null;

								if (!(updCnt == 1))
								{
									errCode = "VTORDDT1";
									errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
									return errCode;
								}
								/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/								
							    demandSupplyMap.put("site_code",siteCode);
								demandSupplyMap.put("item_code", oldItem);		
								demandSupplyMap.put("ref_ser", "P-ORD");
								demandSupplyMap.put("ref_id", purcOrder);
								demandSupplyMap.put("ref_line", lineNoOrd);
								demandSupplyMap.put("due_date", reqDate);		
								demandSupplyMap.put("demand_qty", 0.0);
								demandSupplyMap.put("supply_qty", (quantityStduom - quantityOld));
								demandSupplyMap.put("change_type", "C");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", loginUsr);
							    demandSupplyMap.put("chg_term", termId);	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    demandSupplyMap.clear();
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("errString["+errString+"]");
					                return errString;
							    }
							    /**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
								// modify for ORO on 15/12/14 by cpatil start
								System.out.println("@@@@@@@@@@@- code for P-ORO-----");
								
								sql = " delete from taxtran where tran_code = 'P-ORO' and tran_id = ? and line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								pstmt.setString(2, lineNoOrd);
								pstmt.executeUpdate();
								pstmt.close();
								pstmt = null;
								
								
								sql = " select line_no__tax,tax_code,"
										 + " tax_class,tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,"
												+ " acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,rate_type,round,round_to,"
										 + " tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax,form_no "
												+ " from taxtran where tran_code = ?  and   tran_id = ? and   line_no = ? ";//form_no field added by priyanka as per manoj sharma instruction
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, "P-ORD");
										pstmt.setString(2, purcOrder);
										pstmt.setString(3, lineNoOrd);
										rs = pstmt.executeQuery();
										// if (rs.next()) {
										while (rs.next())
										{
											lineNoTax = rs.getString("line_no__tax");
											taxCode = rs.getString("tax_code");
											taxClass = rs.getString("tax_class");
											taxChap = rs.getString("tax_chap");
											taxBase = rs.getString("tax_base");
											taxEnv = rs.getString("tax_env");
											taxableAmt = rs.getDouble("taxable_amt");
											taxPerc = rs.getString("tax_perc");
											taxAmt = rs.getDouble("tax_amt");
											chgStat = rs.getString("chg_stat");
											taxSet = rs.getString("tax_set");
											effect = rs.getString("effect");
											acctCodeReco = rs.getString("acct_code__reco");
											cctrCodeReco = rs.getString("cctr_code__reco");
											recoPerc = rs.getString("reco_perc");
											recoAmont = rs.getDouble("reco_amount");
											acctCode = rs.getString("acct_code");
											cctrCode = rs.getString("cctr_code");
											rateType = rs.getString("rate_type");
											round = rs.getString("round");
											roundTo = rs.getString("round_to");
											taxForm = rs.getString("tax_form");
											chgDate = rs.getTimestamp("chg_date");
											chgUser = rs.getString("chg_user");
											chgTerm = rs.getString("chg_term");
											posted = rs.getString("posted");
											taxFormDate = rs.getTimestamp("tax_form_date");
											payTax = rs.getString("pay_tax");
											formNo = rs.getString("form_no");

											System.out.println("Getting Form====="+formNo);
											/** Insert taxtran rows */
											sql4 = " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,"
											+ " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
													+ " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code,"
											+ "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
													+ "	posted,tax_form_date,pay_tax,form_no) "
											+ " values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
											pstmt4 = conn.prepareStatement(sql4);
											pstmt4.setString(1, "P-ORO");
											pstmt4.setString(2, purcOrder);
											pstmt4.setString(3, lineNoOrd); 
											pstmt4.setString(4, lineNoTax);
											pstmt4.setString(5, taxCode);
											pstmt4.setString(6, taxClass);
											pstmt4.setString(7, taxChap);
											pstmt4.setString(8, taxBase);
											pstmt4.setString(9, taxEnv);
											pstmt4.setDouble(10, taxableAmt);
											pstmt4.setString(11, taxPerc);
											pstmt4.setDouble(12, taxAmt);
											pstmt4.setString(13, chgStat);
											pstmt4.setString(14, taxSet);
											pstmt4.setString(15, effect);
											pstmt4.setString(16, acctCodeReco);
											pstmt4.setString(17, cctrCodeReco);
											pstmt4.setString(18, recoPerc);
											pstmt4.setDouble(19, recoAmont);
											pstmt4.setString(20, acctCode);
											pstmt4.setString(21, cctrCode);
											pstmt4.setString(22, rateType);
											pstmt4.setString(23, round);
											pstmt4.setString(24, roundTo);
											pstmt4.setString(25, taxForm);
											pstmt4.setTimestamp(26, chgDate);
											pstmt4.setString(27, chgUser);
											pstmt4.setString(28, chgTerm);
											pstmt4.setString(29, posted);
											pstmt4.setTimestamp(30, taxFormDate);
											pstmt4.setString(31, payTax);
											pstmt4.setString(32, formNo);//form_no field added by priyanka as per manoj sharma instruction

											cnt = pstmt4.executeUpdate();
											pstmt4.close();
											pstmt4 = null;
											/** Indert taxtran rows END */

										}

										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

								
								
								
								// modify for ORO on 15/12/14 by cpatil end								
								
								sql = " delete from taxtran where tran_code = 'P-ORD' and tran_id = ? and line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								pstmt.setString(2, lineNoOrd);
								pstmt.executeUpdate();
								pstmt.close();
								pstmt = null;

								lineNo = "    " + lineNo;
								lineNo = lineNo.substring(lineNo.length() - 3, lineNo.length());

								sql = " select count(*) from taxtran where tran_code = 'P-AMD'" 
								+ " and tran_id = ?	and line_no = ? and	 taxable_amt <> 0 and tax_amt <> 0 ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, amdNo);
								pstmt.setString(2, lineNo);taxClass = checknull(rs1.getString("tax_class"));
								taxChap = checknull(rs1.getString("tax_chap"));
								taxEnv = checknull(rs1.getString("tax_env"));
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt > 0)
								{

									/*
									 * sql =
									 * " insert into taxtran ( tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,tax_chap,tax_base,tax_env,"
									 * +
									 * " taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,acct_code__reco,cctr_code__reco,reco_perc,reco_amount,"
									 * +
									 * " acct_code,cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax  ) "
									 * +
									 * " select 'P-ORD',tran_id,line_no,line_no__tax,tax_code,tax_class,tax_chap,tax_base,tax_env,taxable_amt,"
									 * +
									 * " tax_perc,tax_amt,chg_stat,tax_set,effect,acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,"
									 * +
									 * " rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,posted	,tax_form_date, pay_tax "
									 * +
									 * " from taxtran where tran_code = ?	and   tran_id = ? and 	line_no = ? "
									 * ; pstmt = conn.prepareStatement(sql);
									 * pstmt.setString(1, "P-AMD");
									 * pstmt.setString(2, amdNo);
									 * pstmt.setString(3, lineNo); int cnt2 =
									 * pstmt.executeUpdate(); pstmt.close();
									 * pstmt = null;
									 * 
									 * System.out.println("@@@@1 cnt2["+cnt2+"]")
									 * ;
									 *//**/
									sql = " select line_no__tax,tax_code,"
									 + " tax_class,tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,"
											+ " acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,rate_type,round,round_to,"
									 + " tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax,form_no "
											+ " from taxtran where tran_code = ?  and   tran_id = ? and   line_no = ? ";//added by priyanka 
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "P-AMD");
									pstmt.setString(2, amdNo);
									pstmt.setString(3, lineNo);
									rs = pstmt.executeQuery();
									// if (rs.next()) {
									while (rs.next())
									{ // Change fom if to while loop VALLABH
									  // KADAM 19/NOV/14
										lineNoTax = rs.getString("line_no__tax");
										taxCode = rs.getString("tax_code");
										taxClass = rs.getString("tax_class");
										taxChap = rs.getString("tax_chap");
										taxBase = rs.getString("tax_base");
										taxEnv = rs.getString("tax_env");
										taxableAmt = rs.getDouble("taxable_amt");
										taxPerc = rs.getString("tax_perc");
										taxAmt = rs.getDouble("tax_amt");
										chgStat = rs.getString("chg_stat");
										taxSet = rs.getString("tax_set");
										effect = rs.getString("effect");
										acctCodeReco = rs.getString("acct_code__reco");
										cctrCodeReco = rs.getString("cctr_code__reco");
										recoPerc = rs.getString("reco_perc");
										recoAmont = rs.getDouble("reco_amount");
										acctCode = rs.getString("acct_code");
										cctrCode = rs.getString("cctr_code");
										rateType = rs.getString("rate_type");
										round = rs.getString("round");
										roundTo = rs.getString("round_to");
										taxForm = rs.getString("tax_form");
										chgDate = rs.getTimestamp("chg_date");
										chgUser = rs.getString("chg_user");
										chgTerm = rs.getString("chg_term");
										posted = rs.getString("posted");
										taxFormDate = rs.getTimestamp("tax_form_date");
										payTax = rs.getString("pay_tax");
										formNo = rs.getString("form_no");//form_no field added by priyanka as per manoj sharma instruction

										/** Insert taxtran rows */
										sql4 = " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,"
										+ " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
												+ " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code,"
										+ "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
												+ "	posted,tax_form_date,pay_tax,form_no) "
										+ " values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";//form_no field added by priyanka as per manoj sharma instruction
										pstmt4 = conn.prepareStatement(sql4);
										pstmt4.setString(1, "P-ORD");
										pstmt4.setString(2, purcOrder);
										pstmt4.setString(3, lineNoOrd); // lineNo
																		// change
																		// by
																		// cpatil
																		// on
																		// 17/10/13
										pstmt4.setString(4, lineNoTax);
										pstmt4.setString(5, taxCode);
										pstmt4.setString(6, taxClass);
										pstmt4.setString(7, taxChap);
										pstmt4.setString(8, taxBase);
										pstmt4.setString(9, taxEnv);
										pstmt4.setDouble(10, taxableAmt);
										pstmt4.setString(11, taxPerc);
										pstmt4.setDouble(12, taxAmt);
										pstmt4.setString(13, chgStat);
										pstmt4.setString(14, taxSet);
										pstmt4.setString(15, effect);
										pstmt4.setString(16, acctCodeReco);
										pstmt4.setString(17, cctrCodeReco);
										pstmt4.setString(18, recoPerc);
										pstmt4.setDouble(19, recoAmont);
										pstmt4.setString(20, acctCode);
										pstmt4.setString(21, cctrCode);
										pstmt4.setString(22, rateType);
										pstmt4.setString(23, round);
										pstmt4.setString(24, roundTo);
										pstmt4.setString(25, taxForm);
										pstmt4.setTimestamp(26, chgDate);
										pstmt4.setString(27, chgUser);
										pstmt4.setString(28, chgTerm);
										pstmt4.setString(29, posted);
										pstmt4.setTimestamp(30, taxFormDate);
										pstmt4.setString(31, payTax);
										pstmt4.setString(32, formNo);//added by priyanka
										cnt = pstmt4.executeUpdate();
										pstmt4.close();
										pstmt4 = null;
										/** Indert taxtran rows END */

									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									// sql =
									// " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,"
									// +
									// " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
									// +
									// " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code,"
									// +
									// "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
									// + "	posted,tax_form_date,pay_tax) "
									// +
									// " values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";
									// pstmt = conn.prepareStatement(sql);
									// pstmt.setString(1, "P-ORD");
									// pstmt.setString(2, purcOrder);
									// pstmt.setString(3, lineNoOrd); //lineNo
									// change by cpatil on 17/10/13
									// pstmt.setString(4, lineNoTax);
									// pstmt.setString(5, taxCode);
									// pstmt.setString(6, taxClass);
									// pstmt.setString(7, taxChap);
									// pstmt.setString(8, taxBase);
									// pstmt.setString(9, taxEnv);
									// pstmt.setDouble(10, taxableAmt);
									// pstmt.setString(11, taxPerc);
									// pstmt.setDouble(12, taxAmt);
									// pstmt.setString(13, chgStat);
									// pstmt.setString(14, taxSet);
									// pstmt.setString(15, effect);
									// pstmt.setString(16, acctCodeReco);
									// pstmt.setString(17, cctrCodeReco);
									// pstmt.setString(18, recoPerc);
									// pstmt.setDouble(19, recoAmont);
									// pstmt.setString(20, acctCode);
									// pstmt.setString(21, cctrCode);
									// pstmt.setString(22, rateType);
									// pstmt.setString(23, round);
									// pstmt.setString(24, roundTo);
									// pstmt.setString(25, taxForm);
									// pstmt.setTimestamp(26, chgDate);
									// pstmt.setString(27, chgUser);
									// pstmt.setString(28, chgTerm);
									// pstmt.setString(29, posted);
									// pstmt.setTimestamp(30, taxFormDate);
									// pstmt.setString(31, payTax);
									//
									// cnt = pstmt.executeUpdate();
									// pstmt.close();
									// pstmt = null;
									// /**/
									// System.out.println("@@@@1 cnt["+cnt+"]");
								}
								// end if
							}

						} else
						// if (oldItem == null || oldItem.trim().length() == 0)
						{
							lineNo = "    " + lineNo;
							lineNo = lineNo.substring(lineNo.length() - 3, lineNo.length());
							System.out.println("@@@@In the ELSE No item code found for purchase order and Line no");
							if (cctrCodeDr == null)
							{
								cctrCodeDr = "";
							}

							if (cctrCodeCr == null)
							{
								cctrCodeCr = "";
							}
							
							sql = " insert into porddet ( purc_order,line_no,site_code,ind_no,item_code,quantity,unit,rate,"
							+ "	discount,tax_amt,tot_amt,loc_code,req_date,dlv_date,dlv_qty,status,status_date," 
									+ " tax_class,tax_chap,tax_env,remarks,work_order,unit__rate,conv__qty_stduom,conv__rtuom_stduom," 
							+ "	unit__std,quantity__stduom,rate__stduom,pack_code,no_art,pack_instr,acct_code__dr," 
									+ " cctr_code__dr,acct_code__cr,cctr_code__cr,discount_type,supp_code__mnfr,order_opt,bom_code,"
							+ "	specific_instr,rate__clg,special_instr,benefit_type,licence_no,duty_paid,form_no,"
							//+ "acct_code__ap_adv,cctr_code__ap_adv,proj_code) "//Modified by Rohini T on 16/04/2021
							+ "acct_code__ap_adv,cctr_code__ap_adv,proj_code,dimension) "
									+ " values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
								//	+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder); // v
							// pstmt.setString(2, lineNo); // line_no
							pstmt.setString(2, lineNoOrd); // line_no Change
														   // from [lineNo] to
														   // [lineNoOrd]
														   // VALLABH KADAM
														   // 17/NOV/14
							pstmt.setString(3, siteCode); // site_code
							pstmt.setString(4, indNo); // ind_no
							pstmt.setString(5, itemCode); // item code
							pstmt.setDouble(6, quantity);// quantity
							pstmt.setString(7, unit); // unit
							pstmt.setDouble(8, rate); // rate

							pstmt.setDouble(9, discount); // discount
							pstmt.setDouble(10, taxAmt); // tax_amt
							pstmt.setDouble(11, totAmt); // tot_amt
							pstmt.setString(12, locCode); // loc_code
							pstmt.setTimestamp(13, reqDate); // req_date
							pstmt.setTimestamp(14, dlvDate); // dlv_date
							pstmt.setDouble(15, dlvQuantity); // dlv_qty
							pstmt.setString(16, status); // status
							pstmt.setTimestamp(17, sysDate); // status_date

							pstmt.setString(18, taxClass); // tax_class
							pstmt.setString(19, taxChap); // tax_chap
							pstmt.setString(20, taxEnv); // tax_env
							//pstmt.setString(21, remarks); // remarks
							pstmt.setString(21, remarkDet);
							pstmt.setString(22, workOrder); // work_order
							pstmt.setString(23, unitRate); // unit__rate
							pstmt.setDouble(24, convQtyStduom); // conv__qty_stduom
							pstmt.setDouble(25, convRtuomStduom); // conv__rtuom_stduom

							pstmt.setString(26, UnitStd); // unit__std
							pstmt.setDouble(27, quantityStduom); // quantity__stduom
							pstmt.setDouble(28, rateStduom); // rate__stduom
							pstmt.setString(29, packCode); // pack_code
							pstmt.setDouble(30, noArt); // no_art
							pstmt.setString(31, packInstr); // pack_instr
							pstmt.setString(32, acctCodeDr); // acct_code__dr

							pstmt.setString(33, cctrCodeDr); // cctr_code__dr
							pstmt.setString(34, acctCodeCr); // acct_code__cr
							pstmt.setString(35, cctrCodeCr); // cctr_code__cr
							pstmt.setString(36, discountType); // discount_type
							pstmt.setString(37, suppCodeMnfr); // supp_code__mnfr
							pstmt.setString(38, orderOpt); // order_opt
							pstmt.setString(39, bom); // bom_code

							pstmt.setString(40, specificInstr); // specific_instr
							// pstmt.setDouble(41, clgRate); // rate__clg
							pstmt.setDouble(41, rateClg); // rate__clg Change
														  // from [clgRate] to
														  // [rateClg] VALLABH
														  // KADAM 17/NOV/14
							pstmt.setString(42, specialInstr); // special_instr
							pstmt.setString(43, benefitType); // benefit_type
							pstmt.setString(44, licenceNo); // licence_no
							pstmt.setString(45, dutyPaid); // duty_paid
							pstmt.setString(46, formNo); // form_no
							pstmt.setString(47, acctCodeApAdv);// Add column for
							   // insert VALLABH
							   // KADAM 19/NOV/14
							pstmt.setString(48, cctrCodeApAdv);// Add column for
							   // insert VALLABH
							   // KADAM 19/NOV/14
							pstmt.setString(49, projCodeDet);
							pstmt.setString(50, dimension);//Modified by Rohini T on 16/04/2021
							cnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
						    demandSupplyMap.put("site_code",siteCode);
							demandSupplyMap.put("item_code", itemCode);		
							demandSupplyMap.put("ref_ser", "P-ORD");
							demandSupplyMap.put("ref_id", purcOrder);
							demandSupplyMap.put("ref_line", lineNoOrd);
							demandSupplyMap.put("due_date", reqDate);		
							demandSupplyMap.put("demand_qty", 0.0);
							demandSupplyMap.put("supply_qty", quantityStduom);
							demandSupplyMap.put("change_type", "A");
							demandSupplyMap.put("chg_process", "T");
							demandSupplyMap.put("chg_user", loginUsr);
						    demandSupplyMap.put("chg_term", termId);	
						    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
						    demandSupplyMap.clear();
						    if(errString != null && errString.trim().length() > 0)
						    {
						    	System.out.println("errString["+errString+"]");
				                return errString;
						    }
						    /**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
							/*
							 * sql =
							 * " insert into taxtran ( tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,tax_chap,tax_base,tax_env,"
							 * +
							 * " taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,acct_code__reco,cctr_code__reco,reco_perc,reco_amount,"
							 * +
							 * " acct_code,cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax  ) "
							 * +
							 * " select 'P-ORD',tran_id,line_no,line_no__tax,tax_code,tax_class,tax_chap,tax_base,tax_env,taxable_amt,"
							 * +
							 * " tax_perc,tax_amt,chg_stat,tax_set,effect,acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,"
							 * +
							 * " rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,posted	,tax_form_date, pay_tax "
							 * +
							 * " from taxtran where tran_code = ?	and   tran_id = ? and 	line_no = ? "
							 * ; pstmt = conn.prepareStatement(sql);
							 * pstmt.setString(1, "P-AMD"); pstmt.setString(2,
							 * amdNo); pstmt.setString(3, lineNo); cnt =
							 * pstmt.executeUpdate(); pstmt.close(); pstmt =
							 * null; System.out.println("@@@@2 cnt["+cnt+"]");
							 */
							/**/{
								sql = " select line_no__tax,tax_code,"
							+ " tax_class,tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,"
										+ " acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,rate_type,round,round_to," 
							+ " tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax,form_no "
										+ " from taxtran where tran_code = 'P-AMD' and   tran_id = ? and   line_no = ? ";//form_no field added by priyanka as per manoj sharma instruction
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, amdNo);
								pstmt.setString(2, lineNo);
								rs = pstmt.executeQuery();
								// if (rs.next()) {
								while (rs.next())
								{ // Change from if to while VALLABH KADAM
								  // 19/NOV/14
									lineNoTax = rs.getString("line_no__tax");
									taxCode = rs.getString("tax_code");
									taxClass = rs.getString("tax_class");
									taxChap = rs.getString("tax_chap");
									taxBase = rs.getString("tax_base");
									taxEnv = rs.getString("tax_env");
									taxableAmt = rs.getDouble("taxable_amt");
									taxPerc = rs.getString("tax_perc");
									taxAmt = rs.getDouble("tax_amt");
									chgStat = rs.getString("chg_stat");
									taxSet = rs.getString("tax_set");
									effect = rs.getString("effect");
									acctCodeReco = rs.getString("acct_code__reco");
									cctrCodeReco = rs.getString("cctr_code__reco");
									recoPerc = rs.getString("reco_perc");
									recoAmont = rs.getDouble("reco_amount");
									acctCode = rs.getString("acct_code");
									cctrCode = rs.getString("cctr_code");
									rateType = rs.getString("rate_type");
									round = rs.getString("round");
									roundTo = rs.getString("round_to");
									taxForm = rs.getString("tax_form");
									chgDate = rs.getTimestamp("chg_date");
									chgUser = rs.getString("chg_user");
									chgTerm = rs.getString("chg_term");
									posted = rs.getString("posted");
									taxFormDate = rs.getTimestamp("tax_form_date");
									payTax = rs.getString("pay_tax");
									formNo = rs.getString("form_no");//form_no field added by priyanka as per manoj sharma instruction

									/** Insert taxtran rows */
									sql4 = " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class," 
									+ " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
											+ " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code,"
									+ "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
											+ "	posted,tax_form_date,pay_tax,form_no) "
									+ " values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";//form_no field added by priyanka as per manoj sharma instruction
									pstmt4 = conn.prepareStatement(sql4);
									pstmt4.setString(1, "P-ORD");
									pstmt4.setString(2, purcOrder);
									pstmt4.setString(3, lineNoOrd); // lineNo
																	// change by
																	// cpatil on
																	// 17/10/13
									pstmt4.setString(4, lineNoTax);
									pstmt4.setString(5, taxCode);
									pstmt4.setString(6, taxClass);
									pstmt4.setString(7, taxChap);
									pstmt4.setString(8, taxBase);
									pstmt4.setString(9, taxEnv);
									pstmt4.setDouble(10, taxableAmt);
									pstmt4.setString(11, taxPerc);
									pstmt4.setDouble(12, taxAmt);
									pstmt4.setString(13, chgStat);
									pstmt4.setString(14, taxSet);
									pstmt4.setString(15, effect);
									pstmt4.setString(16, acctCodeReco);
									pstmt4.setString(17, cctrCodeReco);
									pstmt4.setString(18, recoPerc);
									pstmt4.setDouble(19, recoAmont);
									pstmt4.setString(20, acctCode);
									pstmt4.setString(21, cctrCode);
									pstmt4.setString(22, rateType);
									pstmt4.setString(23, round);
									pstmt4.setString(24, roundTo);
									pstmt4.setString(25, taxForm);
									pstmt4.setTimestamp(26, chgDate);
									pstmt4.setString(27, chgUser);
									pstmt4.setString(28, chgTerm);
									pstmt4.setString(29, posted);
									pstmt4.setTimestamp(30, taxFormDate);
									pstmt4.setString(31, payTax);
									pstmt4.setString(32, formNo);//form_no field added by priyanka as per manoj sharma instruction

									cnt = pstmt4.executeUpdate();
									pstmt4.close();
									pstmt4 = null;

									/** Insert in taxtran rows END */
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								// sql =
								// " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,"
								// +
								// " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
								// +
								// " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code,"
								// +
								// "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
								// + "	posted,tax_form_date,pay_tax) "
								// +
								// " values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";
								// pstmt = conn.prepareStatement(sql);
								// pstmt.setString(1, "P-ORD");
								// pstmt.setString(2, purcOrder);
								// pstmt.setString(3, lineNoOrd); //lineNo
								// change by cpatil on 17/10/13
								// pstmt.setString(4, lineNoTax);
								// pstmt.setString(5, taxCode);
								// pstmt.setString(6, taxClass);
								// pstmt.setString(7, taxChap);
								// pstmt.setString(8, taxBase);
								// pstmt.setString(9, taxEnv);
								// pstmt.setDouble(10, taxableAmt);
								// pstmt.setString(11, taxPerc);
								// pstmt.setDouble(12, taxAmt);
								// pstmt.setString(13, chgStat);
								// pstmt.setString(14, taxSet);
								// pstmt.setString(15, effect);
								// pstmt.setString(16, acctCodeReco);
								// pstmt.setString(17, cctrCodeReco);
								// pstmt.setString(18, recoPerc);
								// pstmt.setDouble(19, recoAmont);
								// pstmt.setString(20, acctCode);
								// pstmt.setString(21, cctrCode);
								// pstmt.setString(22, rateType);
								// pstmt.setString(23, round);
								// pstmt.setString(24, roundTo);
								// pstmt.setString(25, taxForm);
								// pstmt.setTimestamp(26, chgDate);
								// pstmt.setString(27, chgUser);
								// pstmt.setString(28, chgTerm);
								// pstmt.setString(29, posted);
								// pstmt.setTimestamp(30, taxFormDate);
								// pstmt.setString(31, payTax);
								//
								// cnt = pstmt.executeUpdate();
								// pstmt.close();
								// pstmt = null;
								// System.out.println("@@@@2 cnt["+cnt+"]");

							} // end if
							/**/
						}

						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;
					} else
					{
						lineNo = "    " + lineNo;
						lineNo = lineNo.substring(lineNo.length() - 3, lineNo.length());
						
						
						//sql = " select max(line_no)	 from porddet where purc_order = ? ";
						sql = "select trim(max(cast(line_no as number))) from porddet where purc_order = ?";//Changed by Jaffar S. for getting proper max line no
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lineNoPO = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("@@@@@@lineNoPO1[" + lineNoPO + "]");

						if (lineNoPO == null)
						{
							lineNoPO = "0";
						}
						int tempId = (Integer.parseInt(lineNoPO.trim()) + 1);
						lineNoPO = "    " + tempId;
						lineNoPO = lineNoPO.substring(lineNoPO.length() - 3, lineNoPO.length());
						System.out.println("@@@@@@lineNoPO4[" + lineNoPO + "]");
						if (cctrCodeDr == null || cctrCodeDr.trim().length() == 0)
						{
							cctrCodeDr = " ";
						}

						if (cctrCodeCr == null || cctrCodeCr.trim().length() == 0)
						{
							cctrCodeCr = " ";
						}
						

						sql = " insert into porddet ( purc_order,line_no,site_code,ind_no,item_code,quantity,unit,rate,"
						+ "	discount,tax_amt,tot_amt,loc_code,req_date,dlv_date,dlv_qty,status,status_date,"
								+ " tax_class,tax_chap,tax_env,remarks,work_order,unit__rate,conv__qty_stduom,conv__rtuom_stduom,"
						+ "	unit__std,quantity__stduom,rate__stduom,pack_code,no_art,pack_instr,acct_code__dr,"
								+ " cctr_code__dr,acct_code__cr,cctr_code__cr,discount_type,supp_code__mnfr,order_opt,bom_code,"
						+ "	specific_instr,rate__clg,special_instr,benefit_type,licence_no,duty_paid,form_no,"
								//+ "acct_code__ap_adv,cctr_code__ap_adv,proj_code) " // Add //Modified by Rohini T on 16/04/2021
						+ "acct_code__ap_adv,cctr_code__ap_adv,proj_code,dimension) "																																																																																																																																															   // 19/NOV/14
						        + " values ( " + "?,?,?,?,?,?,?,?,?,?," 
								+ "?,?,?,?,?,?,?,?,?,?," 
						        + "?,?,?,?,?,?,?,?,?,?," 
								+ "?,?,?,?,?,?,?,?,?,?," 
						      //+ "?,?,?,?,?,?,?,?,? ) ";
						      	+ "?,?,?,?,?,?,?,?,?,? ) ";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoPO);
						pstmt.setString(3, siteCode);
						pstmt.setString(4, indNo);
						pstmt.setString(5, itemCode);
						pstmt.setDouble(6, quantity);
						pstmt.setString(7, unit);
						pstmt.setDouble(8, rate);
						pstmt.setDouble(9, discount);
						pstmt.setDouble(10, taxAmt);
						pstmt.setDouble(11, totAmt);
						pstmt.setString(12, locCode);
						pstmt.setTimestamp(13, reqDate);
						pstmt.setTimestamp(14, dlvDate);
						pstmt.setDouble(15, dlvQuantity);
						pstmt.setString(16, status);
						pstmt.setTimestamp(17, sysDate);
						pstmt.setString(18, taxClass);
						pstmt.setString(19, taxChap);
						pstmt.setString(20, taxEnv);
						//pstmt.setString(21, remarks);
						pstmt.setString(21, remarkDet);
						pstmt.setString(22, workOrder);
						pstmt.setString(23, unitRate);
						pstmt.setDouble(24, convQtyStduom);
						pstmt.setDouble(25, convRtuomStduom);
						pstmt.setString(26, UnitStd);
						pstmt.setDouble(27, quantityStduom);
						pstmt.setDouble(28, rateStduom);
						pstmt.setString(29, packCode);
						pstmt.setDouble(30, noArt);
						pstmt.setString(31, packInstr);
						pstmt.setString(32, acctCodeDr);
						pstmt.setString(33, cctrCodeDr);
						pstmt.setString(34, acctCodeCr);
						pstmt.setString(35, cctrCodeCr);
						pstmt.setString(36, discountType);
						pstmt.setString(37, suppCodeMnfr);
						pstmt.setString(38, orderOpt);
						pstmt.setString(39, bom); // bom_code
						pstmt.setString(40, specificInstr);
						// pstmt.setDouble(41, clgRate);
						pstmt.setDouble(41, rateClg); // Change from clgRate to
													  // rateClg VALLABH KADAM
													  // 17/NOV/14
						pstmt.setString(42, specialInstr);
						pstmt.setString(43, benefitType);
						pstmt.setString(44, licenceNo);
						pstmt.setString(45, dutyPaid);
						pstmt.setString(46, formNo);
						pstmt.setString(47, acctCodeApAdv);// Add column for
														   // insert VALLABH
														   // KADAM 19/NOV/14
						pstmt.setString(48, cctrCodeApAdv);// Add column for
						pstmt.setString(49, projCodeDet);								   // insert VALLABH
														   // KADAM 19/NOV/14
						pstmt.setString(50, dimension);//Modified by Rohini T on 16/04/2021
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;

//						if (cnt == 0)
							if (cnt > 0)   //Change condtion from if (cnt == 0) to if (cnt > 0) VALLABH KADAM 20/NOV/14  
						{
								/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
							    demandSupplyMap.put("site_code",siteCode);
								demandSupplyMap.put("item_code", itemCode);		
								demandSupplyMap.put("ref_ser", "P-ORD");
								demandSupplyMap.put("ref_id", purcOrder);
								demandSupplyMap.put("ref_line", lineNoPO);
								demandSupplyMap.put("due_date", reqDate);		
								demandSupplyMap.put("demand_qty", 0.0);
								demandSupplyMap.put("supply_qty", quantityStduom);
								demandSupplyMap.put("change_type", "A");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", loginUsr);
							    demandSupplyMap.put("chg_term", termId);	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    demandSupplyMap.clear();
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("errString["+errString+"]");
					                return errString;
							    }
							  /**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
							/*
							 * sql =
							 * " insert into taxtran ( tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,tax_chap,tax_base,tax_env,"
							 * +
							 * " taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,acct_code__reco,cctr_code__reco,reco_perc,reco_amount,"
							 * +
							 * " acct_code,cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax  ) "
							 * +
							 * " select 'P-ORD' ,tran_id,line_no,line_no__tax,tax_code,tax_class,tax_chap,tax_base,tax_env,taxable_amt,"
							 * +
							 * " tax_perc,tax_amt,chg_stat,tax_set,effect,acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,"
							 * +
							 * " rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,posted	,tax_form_date, pay_tax "
							 * +
							 * " from taxtran where tran_code = ?	and   tran_id = ? and 	line_no = ? "
							 * ; pstmt = conn.prepareStatement(sql);
							 * pstmt.setString(1, "P-AMD"); pstmt.setString(2,
							 * amdNo); pstmt.setString(3, lineNo); cnt =
							 * pstmt.executeUpdate(); pstmt.close(); pstmt =
							 * null; System.out.println("@@@@3 cnt["+cnt+"]");
							 *//**/
							sql = " select line_no__tax,tax_code,"
							 + " tax_class,tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set,effect,"
									+ " acct_code__reco,cctr_code__reco,reco_perc,reco_amount,acct_code,cctr_code,rate_type,round,round_to,"
							 + " tax_form,chg_date,chg_user,chg_term,posted,tax_form_date,pay_tax,form_no"
									+ " from taxtran where tran_code = ?  and   tran_id = ? and   line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, "P-AMD");
							pstmt.setString(2, amdNo);
							pstmt.setString(3, lineNo);
							rs = pstmt.executeQuery();
//							if (rs.next())
							while (rs.next())   // Change from if to while VALLABH KADAM 19/NOV/14
							{
								lineNoTax = rs.getString("line_no__tax");
								taxCode = rs.getString("tax_code");
								taxClass = rs.getString("tax_class");
								taxChap = rs.getString("tax_chap");
								taxBase = rs.getString("tax_base");
								taxEnv = rs.getString("tax_env");
								taxableAmt = rs.getDouble("taxable_amt");
								taxPerc = rs.getString("tax_perc");
								taxAmt = rs.getDouble("tax_amt");
								chgStat = rs.getString("chg_stat");
								taxSet = rs.getString("tax_set");
								effect = rs.getString("effect");
								acctCodeReco = rs.getString("acct_code__reco");
								cctrCodeReco = rs.getString("cctr_code__reco");
								recoPerc = rs.getString("reco_perc");
								recoAmont = rs.getDouble("reco_amount");
								acctCode = rs.getString("acct_code");
								cctrCode = rs.getString("cctr_code");
								rateType = rs.getString("rate_type");
								round = rs.getString("round");
								roundTo = rs.getString("round_to");
								taxForm = rs.getString("tax_form");
								chgDate = rs.getTimestamp("chg_date");
								chgUser = rs.getString("chg_user");
								chgTerm = rs.getString("chg_term");
								posted = rs.getString("posted");
								taxFormDate = rs.getTimestamp("tax_form_date");
								payTax = rs.getString("pay_tax");
								formNo = rs.getString("form_no");
								
								/** Insert tax tran row */
								sql4 = " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,"
										+ " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
												+ " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code," 
										+ "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
												+ "	posted,tax_form_date,pay_tax,form_no) "
										+ " values ("
												+ " ?,?,?,?,?,?,?,?,?,?," 
										+ " ?,?,?,?,?,?,?,?,?,?," 
												+ " ?,?,?,?,?,?,?,?,?,?," 
										+ " ?,? ) ";//form_no field added by priyanka as per manoj sharma instruction
										pstmt4 = conn.prepareStatement(sql4);
										pstmt4.setString(1, "P-ORD");
										pstmt4.setString(2, purcOrder);
//										pstmt4.setString(3, lineNoOrd); // lineNo change bycpatil on 17/10/13
										pstmt4.setString(3, lineNoPO); // Chanfe from lineNoOrd to lineNoPO VALLABH KADAM 20/NOV/14 
										pstmt4.setString(4, lineNoTax);
										pstmt4.setString(5, taxCode);
										pstmt4.setString(6, taxClass);
										pstmt4.setString(7, taxChap);
										pstmt4.setString(8, taxBase);
										pstmt4.setString(9, taxEnv);
										pstmt4.setDouble(10, taxableAmt);
										pstmt4.setString(11, taxPerc);
										pstmt4.setDouble(12, taxAmt);
										pstmt4.setString(13, chgStat);
										pstmt4.setString(14, taxSet);
										pstmt4.setString(15, effect);
										pstmt4.setString(16, acctCodeReco);
										pstmt4.setString(17, cctrCodeReco);
										pstmt4.setString(18, recoPerc);
										pstmt4.setDouble(19, recoAmont);
										pstmt4.setString(20, acctCode);
										pstmt4.setString(21, cctrCode);
										pstmt4.setString(22, rateType);
										pstmt4.setString(23, round);
										pstmt4.setString(24, roundTo);
										pstmt4.setString(25, taxForm);
										pstmt4.setTimestamp(26, chgDate);
										pstmt4.setString(27, chgUser);
										pstmt4.setString(28, chgTerm);
										pstmt4.setString(29, posted);
										pstmt4.setTimestamp(30, taxFormDate);
										pstmt4.setString(31, payTax);
										pstmt4.setString(32, formNo);//form_no field added by priyanka as per manoj sharma instruction

										cnt = pstmt4.executeUpdate();
										pstmt4.close();
										pstmt4 = null;
										/** Insert tax tran row END */
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

//							sql = " insert into taxtran (tran_code,tran_id,line_no,line_no__tax,tax_code,tax_class,"
//							+ " tax_chap,tax_base,tax_env,taxable_amt,tax_perc,tax_amt,chg_stat,tax_set	,	"
//									+ " effect,acct_code__reco,	cctr_code__reco,reco_perc,reco_amount,acct_code," 
//							+ "	cctr_code,rate_type,round,round_to,tax_form,chg_date,chg_user,chg_term,"
//									+ "	posted,tax_form_date,pay_tax) "
//							+ " values ("
//									+ " ?,?,?,?,?,?,?,?,?,?," 
//							+ " ?,?,?,?,?,?,?,?,?,?," 
//									+ " ?,?,?,?,?,?,?,?,?,?," 
//							+ " ? ) ";
//							pstmt = conn.prepareStatement(sql);
//							pstmt.setString(1, "P-ORD");
//							pstmt.setString(2, purcOrder);
//							pstmt.setString(3, lineNoOrd); // lineNo change by
//														   // cpatil on 17/10/13
//							pstmt.setString(4, lineNoTax);
//							pstmt.setString(5, taxCode);
//							pstmt.setString(6, taxClass);
//							pstmt.setString(7, taxChap);
//							pstmt.setString(8, taxBase);
//							pstmt.setString(9, taxEnv);
//							pstmt.setDouble(10, taxableAmt);
//							pstmt.setString(11, taxPerc);
//							pstmt.setDouble(12, taxAmt);
//							pstmt.setString(13, chgStat);
//							pstmt.setString(14, taxSet);
//							pstmt.setString(15, effect);
//							pstmt.setString(16, acctCodeReco);
//							pstmt.setString(17, cctrCodeReco);
//							pstmt.setString(18, recoPerc);
//							pstmt.setDouble(19, recoAmont);
//							pstmt.setString(20, acctCode);
//							pstmt.setString(21, cctrCode);
//							pstmt.setString(22, rateType);
//							pstmt.setString(23, round);
//							pstmt.setString(24, roundTo);
//							pstmt.setString(25, taxForm);
//							pstmt.setTimestamp(26, chgDate);
//							pstmt.setString(27, chgUser);
//							pstmt.setString(28, chgTerm);
//							pstmt.setString(29, posted);
//							pstmt.setTimestamp(30, taxFormDate);
//							pstmt.setString(31, payTax);
//
//							cnt = pstmt.executeUpdate();
//							pstmt.close();
//							pstmt = null;
							System.out.println("@@@@3 cnt[" + cnt + "]");
							/*					*/
							sql = "	update poamd_det set line_no__ord = ?  where amd_no  = ? and line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lineNoPO);
							pstmt.setString(2, amdNo);
							pstmt.setString(3, lineNo);
							updCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

							if (!(updCnt == 1))
							{
								errCode = "VTAMDDT";
								errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
								return errCode;
							}
						}
					}
				} // end while
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}
			if (errCode == null || errCode.trim().length() == 0)
			{
			int count =0;
							
			sql = " select count(1) from poamd_term where amd_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
			cntTerm = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@@@@@@trem cntTerm:[" + cntTerm + "]");
			if(cntTerm > 0)
			{
			sql = " select * from poamd_term where amd_no = ? ";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, amdNo);
			rs1 = pstmt1.executeQuery();
			while(rs1.next())
			{
			purcOrder = rs1.getString("purc_order");
			lineNoPO = rs1.getString("line_no_ord");//LINE_NO_ORD
			termCode=rs1.getString("term_code");
			descr = rs1.getString("descr");
			printOpt = rs1.getString("print_opt");
			if (lineNoPO != null && lineNoPO.trim().length() > 0)
			{
			sql="select count(*) from pord_term where purc_order = ? and line_no = ?";
			pstmtcount = conn.prepareStatement(sql);
			pstmtcount.setString(1, purcOrder);
			pstmtcount.setString(2, lineNoPO);
			rscount = pstmtcount.executeQuery();
			if (rscount.next())
			{
				count = rscount.getInt(1);
			}
			rscount.close();
			rscount = null;
			pstmtcount.close();
			pstmtcount = null;
			if(count > 0)
			{
			System.out.println("@@@@@@@@@term update executed.... lineNoPO:[" + lineNoPO + "]");
			sql = "	update pord_term set term_code = ?, descr = ?, print_opt = ?  where purc_order = ? and   line_no = ? ";
			pstmtupd = conn.prepareStatement(sql);
			pstmtupd.setString(1, termCode);
			pstmtupd.setString(2, descr);
			pstmtupd.setString(3, printOpt);
			pstmtupd.setString(4, purcOrder);
			pstmtupd.setString(5, lineNoPO);
			updCnt = pstmtupd.executeUpdate();
			pstmtupd.close();
			pstmtupd = null;
			if (!(updCnt == 1))
			{
			errCode = "VTORDTERM";
			errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
			return errCode;
			}
			}
			else
			{
			sql = " insert into pord_term ( purc_order, line_no, term_code, descr , print_opt) values (?,?,?,?, ?) ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			pstmt.setString(2, lineNoPO);
			pstmt.setString(3, termCode);
			pstmt.setString(4, descr);
			pstmt.setString(5, printOpt);
			cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			}
			}
			else
			{
			sql = " select max(line_no) from  pord_term where purc_order = ?  ";
			pstmtmax = conn.prepareStatement(sql);
			pstmtmax.setString(1, purcOrder);
			rsmax = pstmtmax.executeQuery();
			if (rsmax.next())
			{
			lineNoPOterm = rsmax.getString(1);
			System.out.println("@@@@@@lineNoPO4[" + lineNoPOterm + "]");
			}
			rsmax.close();
			rsmax = null;
			pstmtmax.close();
			pstmtmax = null;
			if (lineNoPOterm == null)
			{
				lineNoPOterm = "0";
			}
			int tempId = (Integer.parseInt(lineNoPOterm.trim()) + 1);
			System.out.println("TempID " +tempId);
			lineNoPOterm = "    " + tempId;
			lineNoPOterm = lineNoPOterm.substring(lineNoPOterm.length() - 3, lineNoPOterm.length());
			System.out.println("@@@@@@lineNoPO[" + lineNoPOterm + "]");
			System.out.println("**********************insert Data***********************");
			sql = " insert into pord_term ( purc_order, line_no, term_code, descr , print_opt) values (?,?,?,?, ?) ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			pstmt.setString(2, lineNoPOterm);
			pstmt.setString(3, termCode);
			pstmt.setString(4, descr);
			pstmt.setString(5, printOpt);
			cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			}
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			}
			}
			
		/*	if (errCode == null || errCode.trim().length() == 0)
			{
			   cntTerm = 0;
				sql = " select count(1) from poamd_term where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					cntTerm = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("@@@@@@@@@trem cntTerm:[" + cntTerm + "]");
				if (cntTerm > 0)
				{
					sql = " select max(line_no) from  pord_term where purc_order = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lastTermLine = rs.getString(1) == null ? "0" : rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("@@@@@@@@@trem lastTermLine:[" + lastTermLine + "]");
					// for (int i = 1; i < Integer.parseInt(lastTermLine); i++)
					for (int i = 1; i <= cntTerm; i++)
					{
						System.out.println("@@@@@@@@@term for Line:[" + i + "]::lineNo[" + lineNo + "]");
						// 31/10/13 manoharan print_opt added
						sql = " select purc_order,line_no_ord,term_code,descr, print_opt from poamd_term where amd_no = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, amdNo);
						//pstmt.setString(2, lineNoPO);
						pstmt.setString(2, "" + i);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							purcOrder = rs.getString("purc_order");
							lineNoPO = rs.getString("line_no_ord");
							termCode = rs.getString("term_code");
							descr = rs.getString("descr");
							printOpt = rs.getString("print_opt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("@@@@@@@@@trem lineNoPO:[" + lineNoPO + "]");
						if (lineNoPO == null || lineNoPO.trim().length() == 0)
						{
							System.out.println("@@@@@@@@@trem insert executed.... lineNoPO:[" + lineNoPO + "]");
							int temp = Integer.parseInt(lastTermLine) + 1;
							lastTermLine = "" + temp;

							lineNoPO = lastTermLine;

							sql = " insert into pord_term ( purc_order, line_no, term_code, descr , print_opt) values (?,?,?,?, ?) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNoPO);
							pstmt.setString(3, termCode);
							pstmt.setString(4, descr);
							pstmt.setString(5, printOpt);

							cnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

						} else
						{
							System.out.println("@@@@@@@@@trem update executed.... lineNoPO:[" + lineNoPO + "]");
							sql = "	update pord_term set term_code = ?, descr = ?, print_opt = ?  where purc_order = ? and   line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, termCode);
							pstmt.setString(2, descr);
							pstmt.setString(3, printOpt);
							pstmt.setString(4, purcOrder);
							pstmt.setString(5, lineNoPO);
							updCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							if (!(updCnt == 1))
							{
								errCode = "VTORDTERM";
								errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
								return errCode;
							}
						}
						
					}
				}
			}*/
			/*if (errCode == null || errCode.trim().length() == 0)
			{
				sql="select * from poamd_pay_term where amd_no = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					purcOrder = rs.getString("purc_order");
					lineNo = rs.getString("line_no");
					lineNoPO = rs.getString("line_no__ord");
					linnoprev=rs.getString("line_no__prev");
					type = rs.getString("type");
				    relagnst = rs.getString("rel_agnst");
				    amttype=rs.getString("amt_type");
					relamt=rs.getDouble("rel_amt");
					relafter=rs.getDouble("rel_after");
				    adjmet = rs.getString("adj_method");
				    acctcode = checknull(rs.getString("acct_code"));
				    cctr_code = checknull(rs.getString("cctr_code"));
				    taxclass = checknull(rs.getString("tax_class"));
					taxchap = checknull(rs.getString("tax_chap"));
					taxenv = checknull(rs.getString("tax_env"));
				    retperc = rs.getDouble("retention_perc");
				    sitecodeadv = checknull(rs.getString("site_code__adv"));
				    adjustmentperc=rs.getDouble("adj_perc");
				    taskcode = rs.getString("task_code");
				    allowover = checknull(rs.getString("allow_override"));
				    taskcodeParent = checknull(rs.getString("task_code__parent"));
					apprvlead = rs.getDouble("apprv_lead_time");
					remark = rs.getString("remarks");
				    
				    lineNoPO = lineNoPO == null ? "" : lineNoPO.trim();
					if(lineNoPO.length()>0)
					{
						System.out.println("@@@@@@@@@trem update executed.... lineNoPO:[" + lineNoPO + "]");
						sql2 = "update pord_pay_term set type=?,line_no__prev = ? ,rel_agnst=?,amt_type=?,rel_amt=?,rel_after=?,adj_method=?,acct_code=?,cctr_code=?,tax_class=?,tax_chap=?,tax_env=?,retention_perc=?,site_code__adv=?,adj_perc=?,task_code=?,allow_override = ? ,task_code__parent = ? ,apprv_lead_time = ? ,remarks = ? where purc_order = ? and line_no = ? ";
						System.out.println("**********************update data***********************");
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1,type);
						pstmt2.setString(2,linnoprev);
						pstmt2.setString(3,relagnst);
						pstmt2.setString(4,amttype);
						pstmt2.setDouble(5,relamt);
						pstmt2.setDouble(6,relafter);
					    pstmt2.setString(7,adjmet);
					    pstmt2.setString(8,acctcode);
					    pstmt2.setString(9,cctr_code);
					    pstmt2.setString(10, taxclass );
						pstmt2.setString(11,taxchap);
						pstmt2.setString(12,taxenv);
					    pstmt2.setDouble(13,retperc);
					    //pstmt.setString(13,vouchcreated);
					    pstmt2.setString(14,sitecodeadv);
					    pstmt2.setDouble(15,  adjustmentperc);
					    pstmt2.setString(16,taskcode);
					    //pstmt.setDate(17,duedate);
					    pstmt2.setString(17, allowover);
					    pstmt2.setString(18, taskcodeParent);
					    pstmt2.setDouble(19, apprvlead);
					    pstmt2.setString(20, remark);
					    pstmt2.setString(21, purcOrder);
					    pstmt2.setString(22, lineNoPO);
					    updCnt = pstmt2.executeUpdate();
					    System.out.println("update Count "+updCnt);
						pstmt2.close();
						pstmt2 = null;
						System.out.println("@@@@@@@@ update executed[" + lineNoPO + "]");
						if (!(updCnt == 1))
						{
							errCode = "VTORDPYTRM";
							errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
							return errCode;
						}
					}
					else
					{
						System.out.println("**********************insert Data into porrder pay term***********************");
						sql5 = " insert into pord_pay_term (purc_order,line_no,line_no__prev,type,rel_agnst,amt_type,rel_amt,rel_after,adj_method,acct_code,cctr_code,tax_class,tax_chap,tax_env,retention_perc,vouch_created,site_code__adv,adj_perc,task_code,allow_override,task_code__parent,apprv_lead_time,remarks) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
						System.out.println("line noooooooo"+lineNo);
						pstmt5 = conn.prepareStatement(sql5);
						System.out.println("**********************insert Data***********************");
						pstmt5.setString(1, purcOrder);
						pstmt5.setString(2, lineNo);
						pstmt5.setString(3,linnoprev);
						pstmt5.setString(4,type);
						pstmt5.setString(5,relagnst);
						pstmt5.setString(6,amttype);
						pstmt5.setDouble(7,relamt);
						pstmt5.setDouble(8,relafter);
					    pstmt5.setString(9,adjmet);
					    pstmt5.setString(10,acctcode);
					    pstmt5.setString(11,cctr_code);
					    pstmt5.setString(12, taxclass );
						pstmt5.setString(13,taxchap);
						pstmt5.setString(14,taxenv);
					    pstmt5.setDouble(15,retperc);
					    pstmt5.setString(16,"N");
					    pstmt5.setString(17,sitecodeadv);
					    pstmt5.setDouble(18,  adjustmentperc);
					    pstmt5.setString(19,taskcode);
					    pstmt5.setString(20, allowover);
					    pstmt5.setString(21, taskcodeParent);
					    pstmt5.setDouble(22, apprvlead);
					    pstmt5.setString(23, remark);
					    System.out.println("@@@@@@@@@trem update executed.... lineNoPO:[" + lineNoPO + "]");
					  //  pstmt.setDate(19,duedate);
						cnt = pstmt5.executeUpdate();
						
						pstmt5.close();
						pstmt5 = null;
					}
					
				}
				
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}*/
			if (errCode == null || errCode.trim().length() == 0)
			{
				int count =0;
				sql = " select count(1) from poamd_pay_term where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					 count =rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(count > 0)
				{
				 sql = " select * from poamd_pay_term where amd_no = ? ";
	             pstmt = conn.prepareStatement(sql);
	             pstmt.setString(1, amdNo);
	             rs = pstmt.executeQuery();
	             while(rs.next())
					{
	            	    purcOrder = rs.getString("purc_order");
						//lineNo = rs.getString("line_no");
						lineNoPO = rs.getString("line_no__ord");
						linnoprev=rs.getString("line_no__prev");
						type = rs.getString("type");
					    relagnst = rs.getString("rel_agnst");
					    amttype=rs.getString("amt_type");
						relamt=rs.getDouble("rel_amt");
						relafter=rs.getDouble("rel_after");
					    adjmet = rs.getString("adj_method");
					    acctcode = checknull(rs.getString("acct_code"));
					    cctr_code = checknull(rs.getString("cctr_code"));
					    taxclass = checknull(rs.getString("tax_class"));
						taxchap = checknull(rs.getString("tax_chap"));
						taxenv = checknull(rs.getString("tax_env"));
					    retperc = rs.getDouble("retention_perc");
					    sitecodeadv = checknull(rs.getString("site_code__adv"));
					    adjustmentperc=rs.getDouble("adj_perc");
					    taskcode = rs.getString("task_code");
					    allowover = checknull(rs.getString("allow_override"));
					    taskcodeParent = checknull(rs.getString("task_code__parent"));
						apprvlead = rs.getDouble("apprv_lead_time");
						remark = rs.getString("remarks");
						if (lineNoPO != null && lineNoPO.trim().length() > 0)
						{
							sql="select count(*) from pord_pay_term where purc_order = ? and line_no = ?";
							pstmtcount = conn.prepareStatement(sql);
							pstmtcount.setString(1, purcOrder);
							pstmtcount.setString(2, lineNoPO);
							rscount = pstmtcount.executeQuery();
							if (rscount.next())
							{
								cntpo = rscount.getInt(1);
							}
							rscount.close();
							rscount = null;
							pstmtcount.close();
							pstmtcount = null;
							if(cntpo > 0)
							{
							System.out.println("@@@@@@@@@ Payterm update executed.... lineNoPO:[" + lineNoPO + "]");
      						sql2 = "update pord_pay_term set type=?,line_no__prev = ? ,rel_agnst=?,amt_type=?,rel_amt=?,rel_after=?,adj_method=?,acct_code=?,cctr_code=?,tax_class=?,tax_chap=?,tax_env=?,retention_perc=?,site_code__adv=?,adj_perc=?,task_code=?,allow_override = ? ,task_code__parent = ? ,apprv_lead_time = ? ,remarks = ? where purc_order = ? and line_no = ? ";
      						System.out.println("**********************update data***********************");
      						pstmt2 = conn.prepareStatement(sql2);
      						pstmt2.setString(1,type);
      						pstmt2.setString(2,linnoprev);
      						pstmt2.setString(3,relagnst);
      						pstmt2.setString(4,amttype);
      						pstmt2.setDouble(5,relamt);
      						pstmt2.setDouble(6,relafter);
      					    pstmt2.setString(7,adjmet);
      					    pstmt2.setString(8,acctcode);
      					    pstmt2.setString(9,cctr_code);
      					    pstmt2.setString(10, taxclass );
      						pstmt2.setString(11,taxchap);
      						pstmt2.setString(12,taxenv);
      					    pstmt2.setDouble(13,retperc);
      					    //pstmt.setString(13,vouchcreated);
      					    pstmt2.setString(14,sitecodeadv);
      					    pstmt2.setDouble(15,  adjustmentperc);
      					    pstmt2.setString(16,taskcode);
      					    //pstmt.setDate(17,duedate);
      					    pstmt2.setString(17, allowover);
      					    pstmt2.setString(18, taskcodeParent);
      					    pstmt2.setDouble(19, apprvlead);
      					    pstmt2.setString(20, remark);
      					    pstmt2.setString(21, purcOrder);
      					    pstmt2.setString(22, lineNoPO);
      					    updCnt = pstmt2.executeUpdate();
      					    System.out.println("update Count "+updCnt);
      						pstmt2.close();
      						pstmt2 = null;
      						System.out.println("@@@@@@@@ update executed[" + lineNoPO + "]");
      						if (!(updCnt == 1))
      						{
      							errCode = "VTORDPYTRM";
      							errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
      							return errCode;
      						}
      						
							}
							else
							{
								sql5 = " insert into pord_pay_term (purc_order,line_no,line_no__prev,type,rel_agnst,amt_type,rel_amt,rel_after,adj_method,acct_code,cctr_code,tax_class,tax_chap,tax_env,retention_perc,vouch_created,site_code__adv,adj_perc,task_code,allow_override,task_code__parent,apprv_lead_time,remarks) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
								System.out.println("line noooooooo Insert##################"+lineNo);
								pstmt5 = conn.prepareStatement(sql5);
								System.out.println("**********************insert Data***********************");
								pstmt5.setString(1, purcOrder);
								pstmt5.setString(2, lineNoPO);
								pstmt5.setString(3,linnoprev);
								pstmt5.setString(4,type);
								pstmt5.setString(5,relagnst);
								pstmt5.setString(6,amttype);
								pstmt5.setDouble(7,relamt);
								pstmt5.setDouble(8,relafter);
							    pstmt5.setString(9,adjmet);
							    pstmt5.setString(10,acctcode);
							    pstmt5.setString(11,cctr_code);
							    pstmt5.setString(12, taxclass );
								pstmt5.setString(13,taxchap);
								pstmt5.setString(14,taxenv);
							    pstmt5.setDouble(15,retperc);
							    pstmt5.setString(16,"N");
							    pstmt5.setString(17,sitecodeadv);
							    pstmt5.setDouble(18,  adjustmentperc);
							    pstmt5.setString(19,taskcode);
							    pstmt5.setString(20, allowover);
							    pstmt5.setString(21, taskcodeParent);
							    pstmt5.setDouble(22, apprvlead);
							    pstmt5.setString(23, remark);
							    System.out.println("@@@@@@@@@ PAy term  Insert executed.... lineNoPO:[" + lineNoPO + "]");
							  //  pstmt.setDate(19,duedate);
								cnt = pstmt5.executeUpdate();
								pstmt5.close();
								pstmt5 = null;
							}
							
							
						}
						else
						{
							sql = " select max(line_no) from  pord_pay_term where purc_order = ?  ";
							pstmtmax = conn.prepareStatement(sql);
							pstmtmax.setString(1, purcOrder);
							rsmax = pstmtmax.executeQuery();
							if (rsmax.next())
							{
								lineNoPO1 = rsmax.getString(1);
								System.out.println("@@@@@@lineNoPO4[" + lineNoPO1 + "]");
							}
							rsmax.close();
							rsmax = null;
							pstmtmax.close();
							pstmtmax = null;
							if (lineNoPO1 == null)
							{
								lineNoPO1 = "0";
							}
							int tempId = (Integer.parseInt(lineNoPO1.trim()) + 1);
							lineNoPO1 = "    " + tempId;
							lineNoPO1 = lineNoPO1.substring(lineNoPO1.length() - 3, lineNoPO1.length());
							System.out.println("@@@@@@lineNoPO4[" + lineNoPO1 + "]");
							System.out.println("**********************insert Data***********************");
		                    sql5 = " insert into pord_pay_term (purc_order,line_no,line_no__prev,type,rel_agnst,amt_type,rel_amt,rel_after,adj_method,acct_code,cctr_code,tax_class,tax_chap,tax_env,retention_perc,vouch_created,site_code__adv,adj_perc,task_code,allow_override,task_code__parent,apprv_lead_time,remarks) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
							System.out.println("line noooooooo Insert##################"+lineNo);
							pstmt5 = conn.prepareStatement(sql5);
							System.out.println("**********************insert Data***********************");
							pstmt5.setString(1, purcOrder);
							pstmt5.setString(2, lineNoPO1);
							pstmt5.setString(3,linnoprev);
							pstmt5.setString(4,type);
							pstmt5.setString(5,relagnst);
							pstmt5.setString(6,amttype);
							pstmt5.setDouble(7,relamt);
							pstmt5.setDouble(8,relafter);
						    pstmt5.setString(9,adjmet);
						    pstmt5.setString(10,acctcode);
						    pstmt5.setString(11,cctr_code);
						    pstmt5.setString(12, taxclass );
							pstmt5.setString(13,taxchap);
							pstmt5.setString(14,taxenv);
						    pstmt5.setDouble(15,retperc);
						    pstmt5.setString(16,"N");
						    pstmt5.setString(17,sitecodeadv);
						    pstmt5.setDouble(18,  adjustmentperc);
						    pstmt5.setString(19,taskcode);
						    pstmt5.setString(20, allowover);
						    pstmt5.setString(21, taskcodeParent);
						    pstmt5.setDouble(22, apprvlead);
						    pstmt5.setString(23, remark);
						    System.out.println("@@@@@@@@@ PAy term  Insert executed.... lineNoPO:[" + lineNoPO + "]");
						  //  pstmt.setDate(19,duedate);
							cnt = pstmt5.executeUpdate();
							pstmt5.close();
							pstmt5 = null;

						}
					}
	                rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
			}
		}
		/*	//	int cntPyterm1 = 0;
				//sql = " select count(1) from poamd_pay_term where amd_no = ? ";
				//pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					cntPyterm1 = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("@@@@@@@@@ Pay term cntTerm:[" + cntPyterm1 + "]");
				if (cntPyterm1 > 0)
				{
					sql = " select max(line_no) from  pord_pay_term where purc_order = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lastTermLine1 = rs.getString(1) == null ? "0" : rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("@@@@@@@@@ Payterm Line lastTermLine:[" + lastTermLine1 + "]");
					// for (int i = 1; i < Integer.parseInt(lastTermLine1); i++)
					for (int i = 1; i <= cntPyterm1; i++)
					{
						System.out.println("@@@@@@@@@ Payterm for Line:[" + i + "]::lineNo[" + lineNo + "]");
			             sql = " select * from poamd_term where amd_no = ? and line_no = ? ";
			             pstmt = conn.prepareStatement(sql);
			             pstmt.setString(1, amdNo);
			            // pstmt.setString(2, lineNo);
			             pstmt.setString(2, "" + i);
			             rs = pstmt.executeQuery();
			             if (rs.next())
							{
			            	    purcOrder = rs.getString("purc_order");
								//lineNo = rs.getString("line_no");
								lineNoPO = rs.getString("line_no__ord");
								linnoprev=rs.getString("line_no__prev");
								type = rs.getString("type");
							    relagnst = rs.getString("rel_agnst");
							    amttype=rs.getString("amt_type");
								relamt=rs.getDouble("rel_amt");
								relafter=rs.getDouble("rel_after");
							    adjmet = rs.getString("adj_method");
							    acctcode = checknull(rs.getString("acct_code"));
							    cctr_code = checknull(rs.getString("cctr_code"));
							    taxclass = checknull(rs.getString("tax_class"));
								taxchap = checknull(rs.getString("tax_chap"));
								taxenv = checknull(rs.getString("tax_env"));
							    retperc = rs.getDouble("retention_perc");
							    sitecodeadv = checknull(rs.getString("site_code__adv"));
							    adjustmentperc=rs.getDouble("adj_perc");
							    taskcode = rs.getString("task_code");
							    allowover = checknull(rs.getString("allow_override"));
							    taskcodeParent = checknull(rs.getString("task_code__parent"));
								apprvlead = rs.getDouble("apprv_lead_time");
								remark = rs.getString("remarks");
								
							}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
			                  /* System.out.println("@@@@@@@@@trem lineNoPO:[" + lineNoPO + "]");
			                   sql2 = "select line_no__ord  from poamd_pay_term where amd_no = ? ";
			                   pstmt2 = conn.prepareStatement(sql2);
                               pstmt2.setString(1, amdNo);
                               rs2 = pstmt2.executeQuery();
                               if (rs2.next())
				                 {
            	                   lineNoPO = rs2.getString("line_no__ord");
            	                   System.out.println("***Line no ord for insert is=" + lineNoPO);
		                         }
                               		rs2.close();
                               		rs2 = null;
                               		pstmt2.close();
                               		pstmt2 = null;
                               if (lineNoPO == null || lineNoPO.trim().length() == 0)
                                   {
				                    System.out.println("################## insert executed.... lineNoPO for insert:[" + lineNoPO + "]");
				                    int temp = Integer.parseInt(lastTermLine) + 1;
				                    lastTermLine = "" + temp;
				                    lineNoPO = lastTermLine;
				                    System.out.println("**********************insert Data***********************");
				                    sql5 = " insert into pord_pay_term (purc_order,line_no,line_no__prev,type,rel_agnst,amt_type,rel_amt,rel_after,adj_method,acct_code,cctr_code,tax_class,tax_chap,tax_env,retention_perc,vouch_created,site_code__adv,adj_perc,task_code,allow_override,task_code__parent,apprv_lead_time,remarks) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
									System.out.println("line noooooooo Insert##################"+lineNo);
									pstmt5 = conn.prepareStatement(sql5);
									System.out.println("**********************insert Data***********************");
									pstmt5.setString(1, purcOrder);
									pstmt5.setString(2, lineNoPO);
									pstmt5.setString(3,linnoprev);
									pstmt5.setString(4,type);
									pstmt5.setString(5,relagnst);
									pstmt5.setString(6,amttype);
									pstmt5.setDouble(7,relamt);
									pstmt5.setDouble(8,relafter);
								    pstmt5.setString(9,adjmet);
								    pstmt5.setString(10,acctcode);
								    pstmt5.setString(11,cctr_code);
								    pstmt5.setString(12, taxclass );
									pstmt5.setString(13,taxchap);
									pstmt5.setString(14,taxenv);
								    pstmt5.setDouble(15,retperc);
								    pstmt5.setString(16,"N");
								    pstmt5.setString(17,sitecodeadv);
								    pstmt5.setDouble(18,  adjustmentperc);
								    pstmt5.setString(19,taskcode);
								    pstmt5.setString(20, allowover);
								    pstmt5.setString(21, taskcodeParent);
								    pstmt5.setDouble(22, apprvlead);
								    pstmt5.setString(23, remark);
								    System.out.println("@@@@@@@@@ PAy term  Insert executed.... lineNoPO:[" + lineNoPO + "]");
								  //  pstmt.setDate(19,duedate);
									cnt = pstmt5.executeUpdate();
									
									pstmt5.close();
									pstmt5 = null;
			                  } 
                          else
		                   	{
                        	  System.out.println("@@@@@@@@@ Payterm update executed.... lineNoPO:[" + lineNoPO + "]");
      						sql2 = "update pord_pay_term set type=?,line_no__prev = ? ,rel_agnst=?,amt_type=?,rel_amt=?,rel_after=?,adj_method=?,acct_code=?,cctr_code=?,tax_class=?,tax_chap=?,tax_env=?,retention_perc=?,site_code__adv=?,adj_perc=?,task_code=?,allow_override = ? ,task_code__parent = ? ,apprv_lead_time = ? ,remarks = ? where purc_order = ? and line_no = ? ";
      						System.out.println("**********************update data***********************");
      						pstmt2 = conn.prepareStatement(sql2);
      						pstmt2.setString(1,type);
      						pstmt2.setString(2,linnoprev);
      						pstmt2.setString(3,relagnst);
      						pstmt2.setString(4,amttype);
      						pstmt2.setDouble(5,relamt);
      						pstmt2.setDouble(6,relafter);
      					    pstmt2.setString(7,adjmet);
      					    pstmt2.setString(8,acctcode);
      					    pstmt2.setString(9,cctr_code);
      					    pstmt2.setString(10, taxclass );
      						pstmt2.setString(11,taxchap);
      						pstmt2.setString(12,taxenv);
      					    pstmt2.setDouble(13,retperc);
      					    //pstmt.setString(13,vouchcreated);
      					    pstmt2.setString(14,sitecodeadv);
      					    pstmt2.setDouble(15,  adjustmentperc);
      					    pstmt2.setString(16,taskcode);
      					    //pstmt.setDate(17,duedate);
      					    pstmt2.setString(17, allowover);
      					    pstmt2.setString(18, taskcodeParent);
      					    pstmt2.setDouble(19, apprvlead);
      					    pstmt2.setString(20, remark);
      					    pstmt2.setString(21, purcOrder);
      					    pstmt2.setString(22, lineNoPO);
      					    updCnt = pstmt2.executeUpdate();
      					    System.out.println("update Count "+updCnt);
      						pstmt2.close();
      						pstmt2 = null;
      						System.out.println("@@@@@@@@ update executed[" + lineNoPO + "]");
      						if (!(updCnt == 1))
      						{
      							errCode = "VTORDPYTRM";
      							errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
      							return errCode;
      						}
				}
			   }
			}
		}
			
		//}
		/////
				*/
				
				if (errCode == null || errCode.trim().length() == 0)
				{
					sql="select * from poamd_dlv_term where amd_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, amdNo);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						purcOrder = rs.getString("purc_order");
						lineNo = rs.getString("line_no");
						lineNoPO = rs.getString("line_no__ord");
						refcode = checknull(rs.getString("ref_code"));
						minday=rs.getDouble("min_day");
						maxday=rs.getDouble("max_day");
						finchg=rs.getDouble("fin_chg");
						fchgtype = rs.getString("fchg_type");
						minamt=rs.getDouble("min_amt");
						maxamt=rs.getDouble("max_amt");
					    
						lineNoPO = lineNoPO == null ? "" : lineNoPO.trim();
						
							if(lineNoPO.length()> 0)
							{
								System.out.println("@@@@@@@@@ Update Of dlv termlineNoPO:[" + lineNoPO + "]");
								sql6 = "update pord_dlv_term set min_day=?,max_day=?,fin_chg=?,fchg_type=?,min_amt=?,max_amt=?,ref_code=? where purc_order = ? and   line_no = ? ";
								pstmt6 = conn.prepareStatement(sql6);
								pstmt6.setDouble(1,minday);
								pstmt6.setDouble(2,maxday);
								pstmt6.setDouble(3,finchg);
								pstmt6.setString(4,fchgtype);
								pstmt6.setDouble(5,minamt);
								pstmt6.setDouble(6,maxamt);
								pstmt6.setString(7,refcode);
								pstmt6.setString(8, purcOrder);
							    pstmt6.setString(9, lineNoPO);
								updCnt1 = pstmt6.executeUpdate();
								System.out.println("Update count of Dlv term "+ updCnt1);
								pstmt6.close();
								pstmt6 = null;
								if (!(updCnt1 == 1))
								{
									System.out.println("@@@@@@@@@@@@@@@@@@@ Error@@@@" + updCnt1);
									errCode = "VTORDDLTRM";
									errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
									return errCode;
		                        }
							}
							else
							{
								System.out.println("**********************insert Data into porrder dlv term***********************");
								sql7 = " insert into pord_dlv_term (purc_order,line_no,min_day,max_day,fin_chg,fchg_type,min_amt,max_amt,ref_code) values (?,?,?,?,?,?,?,?,?) ";
								pstmt7 = conn.prepareStatement(sql7);
								pstmt7.setString(1, purcOrder);
								pstmt7.setString(2, lineNo);
								pstmt7.setDouble(3,minday);
								pstmt7.setDouble(4,maxday);
								pstmt7.setDouble(5,finchg);
								pstmt7.setString(6,fchgtype);
								pstmt7.setDouble(7,minamt);
								pstmt7.setDouble(8,maxamt);
								pstmt7.setString(9,refcode);
								cnt1 = pstmt7.executeUpdate();
								pstmt7.close();
								pstmt7 = null;
							}
								
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
				  }
					/* cntDyterm = 0;
					sql = " select count(1) from poamd_dlv_term where amd_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, amdNo);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cntDyterm = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("@@@@@@@@@trem cntTerm:[" + cntDyterm + "]");
					if (cntDyterm > 0)
					{
						sql = " select max(line_no) from   pord_dlv_term where purc_order = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lastTermLine = rs.getString(1) == null ? "0" : rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("@@@@@@@@@trem lastTermLine:[" + lastTermLine + "]");
						// for (int i = 1; i < Integer.parseInt(lastTermLine); i++)
						for (int i = 1; i <= cntDyterm; i++)
						{
							System.out.println("@@@@@@@@@term for Line:[" + i + "]::lineNo[" + lineNo + "]");		
	
								// 31/10/13 manoharan print_opt added
								sql = " select purc_order,line_no__ord,ref_code,min_day,max_day,fin_chg,fchg_type,min_amt,max_amt from poamd_dlv_term where amd_no = ? and line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, amdNo);
								// pstmt.setString(2, lineNo);
								pstmt.setString(2, "" + i);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									purcOrder = rs.getString("purc_order");
									lineNoPO = rs.getString("line_no__ord");
									refcode = checknull(rs.getString("ref_code"));
									minday=rs.getDouble("min_day");
									maxday=rs.getDouble("max_day");
									finchg=rs.getDouble("fin_chg");
									fchgtype = rs.getString("fchg_type");
									minamt=rs.getDouble("min_amt");
									maxamt=rs.getDouble("max_amt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
						
									System.out.println("@@@@@@@@@trem lineNoPO:[" + lineNoPO + "]");
									if (lineNoPO == null || lineNoPO.trim().length() == 0)
									{
										System.out.println("@@@@@@@@@trem insert executed.... lineNoPO:[" + lineNoPO + "]");
										int temp = Integer.parseInt(lastTermLine) + 1;
										lastTermLine = "" + temp;
						
										lineNoPO = lastTermLine;
						
										sql = " insert into pord_dlv_term (purc_order,line_no,min_day,max_day,fin_chg,fchg_type,min_amt,max_amt,ref_code) values (?,?,?,?,?,?,?,?,?) ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, purcOrder);
										pstmt.setString(2, lineNoPO);
										pstmt.setDouble(3,minday);
										pstmt.setDouble(4,maxday);
										pstmt.setDouble(5,finchg);
										pstmt.setString(6,fchgtype);
										pstmt.setDouble(7,minamt);
										pstmt.setDouble(8,maxamt);
										pstmt.setString(9,refcode);
										cnt = pstmt.executeUpdate();
										pstmt.close();
										pstmt = null;
						
									}
									else
									{
										System.out.println("@@@@@@@@@trem update executed.... lineNoPO:[" + lineNoPO + "]");
										sql = "	update pord_dlv_term set min_day=?,max_day=?,fin_chg=?,fchg_type=?,min_amt=?,max_amt=?,ref_code=? where purc_order = ? and   line_no = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setDouble(1,minday);
										pstmt.setDouble(2,maxday);
										pstmt.setDouble(3,finchg);
										pstmt.setString(4,fchgtype);
										pstmt.setDouble(5,minamt);
										pstmt.setDouble(6,maxamt);
										pstmt.setString(7,refcode);
										pstmt.setString(8, purcOrder);
									    pstmt.setString(9, lineNoPO);
										updCnt = pstmt.executeUpdate();
										pstmt.close();
										pstmt = null;
										if (!(updCnt == 1))
										{
											errCode = "VTORDDLTRM";
											errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
											return errCode;
				                        }
			                       }
		            }
	           }*/
		     

			if (errCode == null || errCode.trim().length() == 0)
			{

				sql = " select sum(case when tax_amt is null then 0 else tax_amt end), sum(case when tot_amt is null then 0 else tot_amt end), " 
				+ " sum(case when tot_amt is null then 0 else tot_amt end  - case when tax_amt is null then 0 else tax_amt end) "
						+ " from  porddet where purc_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					taxAmtS = rs.getDouble(1);
					totAmtS = rs.getDouble(2);
					ordAmtS = rs.getDouble(3);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (errCode == null || errCode.trim().length() == 0)
				{
					sql = "select sum(case when net_amt is null then 0 else net_amt end)  from	voucher where purc_order = ? " 
				+ " and	vouch_type = ? and confirmed  = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					pstmt.setString(2, "A");
					pstmt.setString(3, "Y");
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						vouchAmt = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (totAmtS < vouchAmt)
					{
						errCode = "ORDLESSVCH";
						errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
						return errCode;
					}
				}

			}
				

			if (errCode == null || errCode.trim().length() == 0)
			{
				sql = " select count(1) from porder where purc_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				// 31/10/13 manoharan emp_code__aprv should not be changed to
				// amendment approver
				if (cnt > 0)
				{
					sql = " update porder set amd_no = ?, amd_date=?, site_code__dlv=?, site_code__ord=?,site_code__bill=?,dept_code=?,"
				+ "supp_code=?,emp_code=?,item_ser=?,tax_opt=?,cr_term=?,ord_amt=?,tax_amt=?,tot_amt=?,curr_code=?,exch_rate=?,"
							+ "tax_chap=?,tax_class=?,tax_env=?,remarks=?,tax_date=?,proj_code=?,sales_pers=?,comm_perc=?,comm_perc__on=?,"
				+ "curr_code__comm=?,quot_no=?,tran_code=?,frt_amt=?,curr_code__frt=?,frt_term=?,dlv_term=?,insurance_amt	=?,"
							+ "curr_code__ins	=?,ref_date=?,advance=?, " + "frt_type = ?, frt_rate=?, frt_amt__qty=? , frt_amt__fixed = ? "
				+ " where purc_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, amdNo);
					pstmt.setTimestamp(2, amdDate);
					pstmt.setString(3, siteCodeDlv);
					pstmt.setString(4, siteCodeOrd);
					pstmt.setString(5, siteCodeBill);
					pstmt.setString(6, deptCode);
					pstmt.setString(7, suppCode);
					pstmt.setString(8, empCode);
					pstmt.setString(9, itemSer);
					pstmt.setString(10, taxOpt);
					pstmt.setString(11, crTerm);
					pstmt.setDouble(12, ordAmtS);
					pstmt.setDouble(13, taxAmtS);
					pstmt.setDouble(14, totAmtS);
					pstmt.setString(15, currCode);
					pstmt.setDouble(16, exchRate);
					pstmt.setString(17, taxChapHdr);
					pstmt.setString(18, taxClassHdr);
					pstmt.setString(19, taxEnvHdr);
					pstmt.setString(20, remarks);
					pstmt.setTimestamp(21, taxDate);
					pstmt.setString(22, projCode);
					pstmt.setString(23, salesPers);
					pstmt.setString(24, commPerc);
					pstmt.setString(25, commPercOn);
					pstmt.setString(26, currCodeComm);
					pstmt.setString(27, quotNo);
					pstmt.setString(28, tranCode);
					pstmt.setDouble(29, frtAmt);
					pstmt.setString(30, currCodeFrt);
					pstmt.setString(31, frtTerm);
					pstmt.setString(32, dlvTerm);
					pstmt.setDouble(33, insuranceAmt);
					pstmt.setString(34, currCodeIns);
					// pstmt.setString(35, empCodeAprv);
					pstmt.setTimestamp(35, refDate);
					pstmt.setString(36, advance);
					pstmt.setString(37, frtType);
					pstmt.setDouble(38, frtRate);
					pstmt.setDouble(39, frtAmtQty);
					pstmt.setDouble(40, frtAmtFixed);
					pstmt.setString(41, purcOrder);
					updCnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;

				} else
				{
					errCode = "VTPORD1";
					errCode = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
					return errCode;
				}

				if (updCnt > 0)
				{
					double updFrtAmtQty = 0;
					System.out.println("@@@@@@@@ for cross update into java.....");

					sql = " select frt_type, frt_rate, frt_amt__qty, frt_amt__fixed from porder	where purc_order = ?	 ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcOrder);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						frtType = rs.getString("frt_type");
						frtRate = rs.getDouble("frt_rate");
						frtAmtQty = rs.getDouble("frt_amt__qty");
						frtAmtFixed = rs.getDouble("frt_amt__fixed");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("@@@@frtType[" + frtType + "]frtRate[" + frtRate + "]frtAmtQty[" + frtAmtQty + "]frtAmtFixed[" + frtAmtFixed + "]");

					if ("F".equalsIgnoreCase(frtType))
					{
						sql = "	update porder set frt_amt__fixed = ? ,frt_amt = ? ,frt_amt__qty = 0  where purc_order = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDouble(1, (frtAmtFixed + frtAmtQty));
						pstmt.setDouble(2, (frtAmtFixed + frtAmtQty));
						pstmt.setString(3, purcOrder);
						updCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;

					} else if ("Q".equalsIgnoreCase(frtType))
					{
						sql = " select quantity from porddet where purc_order = ?	 ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						while (rs.next())
						{
							quantity = rs.getDouble("quantity");
							updFrtAmtQty = updFrtAmtQty + (quantity * frtRate);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@@updFrtAmtQty[" + updFrtAmtQty + "]");
						sql = "	update porder set frt_amt__qty = ? , frt_amt= ? , frt_amt__fixed = 0   where purc_order = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDouble(1, updFrtAmtQty);
						pstmt.setDouble(2, updFrtAmtQty);
						pstmt.setString(3, purcOrder);
						updCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;

					}

				}
			}

			// } for test
		} // end try
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		

		return errCode;
	}

	private String checknull(String string)
	{
		if (string == null)
		{
			string = "";
		}
		return string;
	}

	private String setDescription(String descr, String table, String field, String value, Connection conn) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		System.out.println("@@@@@@@@table[" + table + "]:::field[" + field + "]::value[" + value + "]");
		sql = "select " + descr + " from " + table + " where " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			descr = checknull(rs.getString(1));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		System.out.print("::descr[" + descr);
		return descr;
	}

}