package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.naming.InitialContext;

public class CreatePoVoucherAdvance extends ValidatorEJB implements
CreatePoVoucherAdvanceLocal, CreatePoVoucherAdvanceRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	UtilMethods utilMethods = UtilMethods.getInstance();
	FinCommon finCommon = new FinCommon();
	ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	ibase.utility.UserInfoBean userInfo = new ibase.utility.UserInfoBean();

	public String createPoVoucherAdv(String tranId, String xtraParams,Connection conn, String as_flag, int ad_advperc, Date day)throws ITMException 
	{
		String errcode="",mileStoneId="",lineNoTerm="",relAgnstTerm="";
		

		try
		{
			System.out.println("-----in gbf_porder_advances------");
		//	errcode =  createPoVoucherAdv(tranId, xtraParams, conn, as_flag, ad_advperc, day, mileStoneId);
			errcode =  createPoVoucher(tranId, xtraParams, conn, as_flag, ad_advperc, day, mileStoneId,relAgnstTerm,lineNoTerm);
			System.out.println("@@@@@@@@@@@39 errcode["+errcode+"]");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			throw new ITMException(e);
		}

		System.out.println("Returning Result ::"+errcode);
		return errcode;
	}


	public String createPoVoucher(String tranId, String xtraParams,Connection conn, String as_flag, int ad_advperc, Date day, String mileStoneId,String relAgnstTerm,String lineNoTerm)throws ITMException 
	{
		
		
		return createPoVoucher(tranId,  xtraParams, conn,  as_flag, Double.parseDouble(""+ad_advperc),  day,  mileStoneId, relAgnstTerm, lineNoTerm);
	}

	public String createPoVoucher(String tranId, String xtraParams,Connection conn, String as_flag, double ad_advperc, Date day, String mileStoneId,String relAgnstTerm,String lineNoTerm)throws ITMException 
	{
		System.out.println("- createPoVoucher!! -----as_flag["+as_flag+"]mileStoneId["+mileStoneId+"]");
		System.out.println("relAgnstTerm :"+relAgnstTerm);
		System.out.println("lineNoTerm :"+lineNoTerm);
		String site = "", paymode = "", supp = "", curr = "", errcode = "",loginSiteCode="";
		String acctapitem = "", cctrapitem = "", crterm = "", finent = "", proj = "";
		String key = "", win = "", vouchid = "", taxclass = "", taxchap = "";
		String taxenv = "", itemser = "", acctap = "", cctrap = "", applytax = "";
		String acctapadv = "", cctrapadv = "", str = "", remark = "", type = "";
		String provpo = "", taxtype = "", bankcode = "", advtype = "";
		String porcpid = "", rndstr = "", rndoff = "";
		String lsrndto = "", invacct = "", acctcodecr = "", cctrcodecr = "";
		String relagnst = "", SITECODEDLV = "", SITECODEORD = "", link = "", findlv = "", finord = "";
		String poid = "", sitecodeadv = "",refSer="";
		String sql = "",sql2="";
		String code = "",aprvLeadTime="";
		String userId = "SYSTEM", termid = "SYSTEM";
		boolean edilink;
		int row = 0, cnt = 0, lineno = 0, relafter = 0, rndto = 0,aprvLeadTimeInt=0;
		int updCnt = 0;
		TransIDGenerator generator = null;
		double amount = 0, tax = 0, netamt = 0, netamtbase = 0,  advperc = 0, lcordamt = 0, vouchadvamt = 0,calRelAmt=0.0;
		double advance = 0, vouchamt = 0, ordamt = 0, taxamt = 0, totalpoamt = 0, retperc = 0, retamt = 0;
		double exch = 0,ordAmt=0.0,ordAmtPord=0.0;
		porcpid = tranId;
		SimpleDateFormat sdf  = null;
		String tran_Date =null,tranDateApp="";
		java.sql.Timestamp tranDate = null ,today = null, chgdate = null, milestonedt = null, orddate = null,sysDate=null,sysDate1=null;
		PreparedStatement pstmt = null, pstmtUp = null,pstmt1 = null,pstmt2=null;
		ResultSet rs = null, rs1 =null,rs2=null;
		MasterStatefulLocal masterStatefulLocal = null;

		StringBuffer xmlBuff = new StringBuffer();
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			sdf =  new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			java.util.Calendar cal = java.util.Calendar.getInstance();
			tran_Date = sdf.format(cal.getTime());
			
			System.out.println("tran_Date :"+tran_Date);
			SimpleDateFormat sdfApp =  new SimpleDateFormat(genericUtility.getApplDateFormat());
			
		/*	
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			java.util.Date currentDate1 = new java.util.Date();
			sysDate1 = java.sql.Timestamp.valueOf( sdf1.format(currentDate1)+" 00:00:00.0");
			System.out.println("sysDate1 :"+sysDate1);*/
			
		/*	java.util.Date currentDate = new java.util.Date();
			sysDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
			System.out.println("sysDate :"+sysDate);
			*/
			
			
			System.out.println("sdf.format(new java.util.Date() :"+sdf.format(new java.util.Date()));
			today=java.sql.Timestamp.valueOf( sdf1.format(new java.util.Date())+" 00:00:00.0");
			System.out.println("today date is :"+today);
			//sysDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
			
			
			AppConnectParm appConnect = new AppConnectParm();
			Properties p = appConnect.getProperty();
			InitialContext ctx = new InitialContext(p);
			System.out.println("xtraParams :"+xtraParams);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			
			
			System.out.println("userId :"+userId);
			System.out.println("loginSiteCode :"+loginSiteCode);
			xmlBuff = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			xmlBuff.append("<![CDATA[No]]></editFlag> </header>");
			xmlBuff.append("<Detail0>");
			xmlBuff.append("<loginCode><![CDATA[").append(userId).append("]]></loginCode>");
			xmlBuff.append("<termId><![CDATA[").append(termid).append("]]></termId>");
			xmlBuff.append("<site_code><![CDATA[").append(loginSiteCode).append("]]></site_code>");
			xmlBuff.append("</Detail0></Root>");
			if ( ( as_flag.equals("PR")  || as_flag.equals("ML") ) && ( mileStoneId == null || mileStoneId.trim().length()==0))
			{
				sql = "select purc_order from   porcp where tran_id=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();

				if (rs.next()) 
				{
					tranId = rs.getString("purc_order");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if (as_flag.equals("QC")) 
			{
				sql = "Select A.purc_order,A.tran_id From porcp A, qc_order B Where B.qorder_no = ? And B.porcp_no	 = A.tran_id";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					tranId = rs.getString("purc_order");
					porcpid = rs.getString("train_id");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}

              System.out.println("as_flag@@@@@ :"+as_flag);
			if (as_flag.equals("PO") || as_flag.equals("POPRO"))
			{
				sql = "Select porder.ord_date , porder.supp_code , porder.tot_amt,porder.ord_amt,"
						+ "porder.curr_code,porder.exch_rate,porder.cr_term,porder.proj_code,"
						+ "porder.site_code__bill,porder.item_ser," 
						+ "(case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end) as vouch_adv_amt , "
						+ "porder.tot_amt , porder.advance, porder.pord_type, porder.provi_tran_id  From porder"
						+ " Where purc_order = ? And confirmed ='Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					tranDate = rs.getTimestamp("ord_date");
					supp = rs.getString("supp_code");
					amount = rs.getDouble("tot_amt");
					lcordamt = rs.getDouble("ord_amt");
					curr = rs.getString("curr_code");
					exch = rs.getDouble("exch_rate");
					crterm = rs.getString("cr_term");
					proj = rs.getString("proj_code");
					site = rs.getString("site_code__bill");
					vouchadvamt= rs.getDouble("vouch_adv_amt");
					itemser = rs.getString("item_ser");
					totalpoamt = rs.getDouble("tot_amt");
					advance = rs.getDouble("advance");
					type = rs.getString("pord_type");
					provpo = rs.getString("provi_tran_id");
				} 
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					errcode = itmDBAccessLocal.getErrorString("","VTPORD3","","",conn);
					return errcode;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				poid = tranId;
				if (!as_flag.equalsIgnoreCase("POPRO"))
				{
					if (errcode == null || errcode.trim().length() == 0) 
					{
						sql = "select SITE_CODE__DLV,SITE_CODE__ORD intofrom   porder where  PURC_ORDER = ? And confirmed  = 'Y'";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, poid);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							SITECODEDLV = rs.getString("SITE_CODE__DLV");
							SITECODEORD = rs.getString("SITE_CODE__ORD");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else if (SITECODEDLV.trim() != SITECODEORD.trim())
					{
						System.out.println("fin entity");
						sql = "select FIN_ENTITY  from   site where  site_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, SITECODEDLV);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							findlv = rs.getString("FIN_ENTITY");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("fin site");
						sql = "select FIN_ENTITY from   site where  site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, SITECODEORD);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							finord = rs.getString("FIN_ENTITY");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "select link_type from   ibca_pay_ctrl where  site_code__from  =?"
								+ "and site_code__to=? "
								+ "and fin_entity__from =?"
								+ "and fin_entity__to=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, SITECODEORD);
						pstmt.setString(2, SITECODEDLV);
						pstmt.setString(3, finord);
						pstmt.setString(4, findlv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							link = rs.getString("link_type");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (link.equals("N") && link.equals("E"))
						{
							edilink = true;
						}

					} 
					else 
					{
						as_flag.equals("PO");
					} 
				}

				if (errcode == null || errcode.trim().length() == 0)
				{
					//if (retString == null || retString.trim().length() == 0)
					edilink = true;
					//}
				}
				//}
				else if (as_flag.equals("PR") || as_flag.equals("QC") || as_flag.equals("ML")) 
				{
					System.out.println("@@@@@@264 as_flag["+as_flag+"]");
					
					sql = " Select porder.ord_date , porder.supp_code , (case when porcp.amount is null then 0 else porcp.amount end ),"
							+ "(case when porcp.amount is null then 0 else porcp.amount end - case when porcp.tax is null then 0 else porcp.tax end),"
							+ " porder.curr_code 		, porder.exch_rate , porder.cr_term		  , porder.proj_code,"
							+ "porder.site_code__bill , porder.item_ser  , (case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end ),"
							+ "porder.tot_amt 			, porder.advance	 , porder.pord_type	  , porder.provi_tran_id,"
							+ "porder.purc_order From   porder , porcp Where  porder.purc_order = porcp.purc_order And porcp.tran_id = ? And porder.confirmed ='Y'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, porcpid);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						tranDate = rs.getTimestamp("ord_date");
						supp = rs.getString("supp_code");
						amount = rs.getDouble("tot_amt");
						lcordamt = rs.getDouble(4);
						curr = rs.getString("curr_code");
						exch = rs.getDouble("exch_rate");
						crterm = rs.getString("cr_term");
						proj = rs.getString("proj_code");
						site = rs.getString("site_code__bill");
						itemser = rs.getString("item_ser");
						vouchadvamt = rs.getDouble("vouch_adv_amt");
						totalpoamt = rs.getDouble("tot_amt");
						advance = rs.getDouble("advance");
						type = rs.getString("pord_type");
						provpo = rs.getString("provi_tran_id");
						poid = rs.getString("purc_order");
						tax= rs.getDouble("tax");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}
				sql="select bank_code, fin_entity into from   site where site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, site);
				rs = pstmt.executeQuery();

				if(rs.next())
				{
					bankcode=rs.getString("bank_code");
					finent=rs.getString("fin_entity");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (as_flag.equals("PO") || as_flag.equals("POPRO"))
				{
					code = "01";
				} else if (as_flag.equals("PR")  || as_flag.equals("ML"))
				{
					code = "02";
				} else if (as_flag.equals("QC"))
				{
					code = "03";

				}
				System.out.println("do while");
			if( mileStoneId != null &&  mileStoneId.trim().length() > 0)
			{
				// apprv_lead_time column added by Sagar on 27/11/15
				sql = " Select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv, apprv_lead_time " +
					  "	from pord_pay_term where purc_order = ?  " +
					  " and  case when vouch_created is null then 'N' else vouch_created end = 'N' " +
					  " and task_code = ? "   ;
			}
			else
			{
				// apprv_lead_time column added by Sagar on 27/11/15
				sql = " Select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv, apprv_lead_time " +
					  " from pord_pay_term where  purc_order =? and    case when vouch_created is null then 'N' else vouch_created end = 'N' ";
			}
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, poid);
				if( mileStoneId != null &&  mileStoneId.trim().length() > 0)
				{
					pstmt.setString(2, mileStoneId);
				}
				rs = pstmt.executeQuery();
				while (rs.next()) 
				{
					lineno = rs.getInt("line_no");
					relagnst = rs.getString("rel_agnst");
					advtype = rs.getString("amt_type");
					advperc = rs.getDouble("rel_amt");
					relafter = rs.getInt("rel_after");
					retperc = rs.getDouble("retention_perc");
					taxclass = rs.getString("tax_class");
					taxchap = rs.getString("tax_chap");
					taxenv = rs.getString("tax_env");
					sitecodeadv = rs.getString("site_code__adv");
					aprvLeadTime = rs.getString("apprv_lead_time"); // Code added by Sagar on 27/11/15 
					System.out.println(">>>aprvLeadTime:"+aprvLeadTime);
					
					if(aprvLeadTime== null || aprvLeadTime.trim().length()==0)
					{
						aprvLeadTime="0";
					}
					if(aprvLeadTime!=null && aprvLeadTime.trim().length() > 0)
					{
						aprvLeadTime=aprvLeadTime.trim();
						aprvLeadTimeInt=Integer.parseInt(aprvLeadTime);
					}
					
					if (sitecodeadv.length() > 0) 
					{
						if (sitecodeadv != SITECODEORD) 
						{
							continue;
						}
					} else {
						if (edilink = true) 
						{

							continue;
						}

					}

					if ((relagnst == "04") || (code != relagnst)) {
						continue;
					}

					lineno = rs.getInt("line_no");
					relagnst = rs.getString("rel_agnst");
					advtype = rs.getString("amt_type");
					advperc = rs.getDouble("rel_amt");
					relafter = rs.getInt("rel_after");
					retperc = rs.getDouble("retention_perc");
					taxclass = rs.getString("tax_class");
					taxchap = rs.getString("tax_chap");
					taxenv = rs.getString("tax_env");
					sitecodeadv = rs.getString("site_code__adv");
					aprvLeadTime = rs.getString("apprv_lead_time"); // Code added by Sagar on 27/11/15 
					System.out.println(">>>aprvLeadTime:"+aprvLeadTime);
					
					if(aprvLeadTime== null || aprvLeadTime.trim().length()==0)
					{
						aprvLeadTime="0";
					}
					if(aprvLeadTime!=null && aprvLeadTime.trim().length() > 0)
					{
						aprvLeadTime=aprvLeadTime.trim();
						aprvLeadTimeInt=Integer.parseInt(aprvLeadTime);
					}
					
					if (sitecodeadv.length() > 0) 
					{
						if (sitecodeadv != SITECODEORD) 
						{
							continue;
						}
					} else {
						if (edilink = true) 
						{

							continue;
						}

					}

					if ((relagnst == "04") || (code != relagnst)) {
						continue;
					}

					if (advtype == "01") {

						advtype = "B";
					} else if (advtype == "02") {

						advtype = "P";
					} else if (advtype == "03") {

						advtype = "F";

					}
					else if (advtype == "04")
					{
			            
			              advtype = "T";
			            }

				}
				if (tranId == provpo) 
				{
					// populateerror(9999,"populateerror");
					// errcode = "VTPURPROV1" + " ~t Prov. PO : " +
					// as_tran_id;
					// errcode = gf_error_location(ls_errcode);
					// return errcode;
				}
				if (errcode.trim().length() > 0) 
				{
					return errcode;
				}
				System.out.println("pay mode");
				sql = "select pay_mode,acct_code__ap,cctr_code__ap,acct_code__ap_adv,cctr_code__ap_adv from  supplier where supp_code = ?;";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, supp);
				rs = pstmt.executeQuery();


				if (rs.next())
				{
					paymode = rs.getString("pay_mode");
					acctap = rs.getString("acct_code__ap");
					cctrap = rs.getString("cctr_code__ap");
					acctapadv = rs.getString("acct_code__ap_adv");
					cctrapadv = rs.getString("cctr_code__ap_adv");
				}
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					errcode = itmDBAccessLocal.getErrorString("","VTPORD3","","",conn);
					return errcode;
				}
				
				if ((invacct == "NULL") || (invacct == "NULLFOUND")
						|| (invacct.trim().length() == 0)) {
					invacct = "N";

				}
				System.out.println("acct_code ap");
				sql = "select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV,acct_code__cr,cctr_code__cr from  porddet where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					acctapadv = rs.getString("ACCT_CODE__AP_ADV");
					cctrapadv = rs.getString("CCTR_CODE__AP_ADV");
					acctcodecr = rs.getString("acct_code__cr");
					cctrcodecr = rs.getString("cctr_code__cr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("confirmed");
				sql = "Select porder.ord_date , porder.supp_code , porder.tot_amt,porder.ord_amt,"
						+ "porder.curr_code,porder.exch_rate,porder.cr_term,porder.proj_code,"
						+ "porder.site_code__bill,porder.item_ser,(case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end)vouch_adv_amt, "
						+ "porder.tot_amt , porder.advance, porder.pord_type, porder.provi_tran_id  From porder"
						+ " Where purc_order = ? And confirmed ='Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					tranDate = rs.getTimestamp("ord_date");
					supp = rs.getString("supp_code");
					amount = rs.getDouble("tot_amt");
					lcordamt = rs.getDouble("ord_amt");
					curr = rs.getString("curr_code");
					exch = rs.getDouble("exch_rate");
					crterm = rs.getString("cr_term");
					proj = rs.getString("proj_code");
					site = rs.getString("site_code__bill");
					vouchadvamt= rs.getDouble("vouch_adv_amt");
					itemser = rs.getString("item_ser");
					totalpoamt = rs.getDouble("tot_amt");
					advance = rs.getDouble("advance");
					type = rs.getString("pord_type");
					provpo = rs.getString("provi_tran_id");
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				poid = tranId;

				if (!as_flag.equalsIgnoreCase("POPRO"))
				{
					if (errcode == null || errcode.trim().length() == 0) 
					{
						System.out.println("site code dlv");
						sql = "select SITE_CODE__DLV,SITE_CODE__ORD into"
								+ "from   porder"
								+ "where  PURC_ORDER = ? And confirmed  = 'Y'";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, poid);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							SITECODEDLV = rs.getString("SITE_CODE__DLV");
							SITECODEORD = rs.getString("SITE_CODE__ORD");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (SITECODEDLV.trim() != SITECODEORD.trim()) 
						{
							System.out.println("fi entity site code");
							sql = "select FIN_ENTITY  from   site where  site_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, SITECODEDLV);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								findlv = rs.getString("FIN_ENTITY");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("fin entity site code rd");
							sql = "select FIN_ENTITY from   site where  site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, SITECODEORD);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								finord = rs.getString("FIN_ENTITY");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("link type ibca");
							sql = "select link_type from   ibca_pay_ctrl where  site_code__from  =?"
									+ "and site_code__to=? "
									+ "and fin_entity__from =?"
									+ "and fin_entity__to=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, SITECODEORD);
							pstmt.setString(2, SITECODEDLV);
							pstmt.setString(3, finord);
							pstmt.setString(4, findlv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								link = rs.getString("link_type");
							}
							else
							{
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								errcode = itmDBAccessLocal.getErrorString("","VTNIBCA","","",conn);
								return errcode;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (link.equals("N") && link.equals("E"))
							{
								edilink = true;
							}

						} 
						else 
						{
							as_flag.equals("PO");
						}

					}

				}
				edilink = true;
			}
            else if (as_flag.equals("PR") || as_flag.equals("QC")  || as_flag.equals("ML")) 
			{
				System.out.println("PR or QC as as_flag");
				String sql1 = " Select porder.ord_date  		, porder.supp_code , (case when porcp.amount is null then 0 else porcp.amount end ) amount,"
						+ "(case when porcp.amount is null then 0 else porcp.amount end - case when porcp.tax is null then 0 else porcp.tax end) netamount,"
						+ " porder.curr_code 		, porder.exch_rate , porder.cr_term		  , porder.proj_code,"
						+ " porder.site_code__bill , porder.item_ser  , (case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end ) vouch_adv_amt,"
						+ " porder.tot_amt 			, porder.advance	 , porder.pord_type	  , porder.provi_tran_id,"
						+ " porder.purc_order,(case when porcp.tax is null then 0 else porcp.tax end) tax From   porder , porcp Where  porder.purc_order = porcp.purc_order " 
						+ " And porder.confirmed ='Y'  And porcp.tran_id = ? ";

				if( mileStoneId != null && mileStoneId.trim().length() > 0 )
				{
				sql1 = "Select porder.ord_date , porder.supp_code , porder.tot_amt,porder.ord_amt,"
						+ "porder.curr_code,porder.exch_rate,porder.cr_term,porder.proj_code,"
						+ "porder.site_code__bill,porder.item_ser,(case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end)vouch_adv_amt, "
						+ "porder.tot_amt as amount ,porder.tot_amt, porder.advance, porder.pord_type, porder.provi_tran_id, " 
						+ " porder.purc_order" 
						+ ",(case when porder.tax_amt is null then 0 else porder.tax_amt end) tax " 
						+ " From porder"
						+ " Where purc_order = ? And confirmed ='Y'";
				}
				pstmt1=conn.prepareStatement(sql1);
				pstmt1.setString(1, porcpid);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
				tranDate = rs1.getTimestamp("ord_date");
				System.out.println("tranDate :"+tranDate);
				supp = rs1.getString("supp_code");
				System.out.println("supp :"+supp);
				amount = rs1.getDouble("amount");
				System.out.println("amount :"+amount);
				ordamt = rs1.getDouble(4);
				System.out.println("ordamt :"+ordamt);
				curr = rs1.getString("curr_code");
				System.out.println("curr :"+curr);
				exch = rs1.getDouble("exch_rate");
				System.out.println("exch :"+exch);
				crterm = rs1.getString("cr_term");
				System.out.println("crterm :"+crterm);
				proj = rs1.getString("proj_code");
				System.out.println("proj :"+proj);
				site = rs1.getString("site_code__bill");
				System.out.println("site :"+site);
				itemser = rs1.getString("item_ser");
				System.out.println("itemser :"+itemser);
				vouchadvamt = rs1.getDouble("vouch_adv_amt");
				System.out.println("vouchadvamt :"+vouchadvamt);
				totalpoamt = rs1.getDouble("tot_amt");
				System.out.println("totalpoamt :"+totalpoamt);
				advance = rs1.getDouble("advance");
				System.out.println("advance :"+advance);
				type = rs1.getString("pord_type");
				System.out.println("type :"+type);
				provpo = rs1.getString("provi_tran_id");
				System.out.println("provpo :"+provpo);
				poid = rs1.getString("purc_order");
				System.out.println("poid :"+poid);
				//poid=porcpid;
				
				lcordamt= ordamt; // Code added by Sagar on 23/11/15 to set bill amount  
				System.out.println(">>lcordamt:"+ lcordamt);
				//ordAmt = rs.getDouble("ord_amt");
				
				//poid = rs.getString("purc_order");
			//	if( mileStoneId == null || mileStoneId.trim().length() == 0 )
			//	{
				taxamt =rs1.getDouble("tax");
				System.out.println("tax :"+taxamt);
			//	}
				
				}
				else
				{
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					errcode = itmDBAccessLocal.getErrorString("","VTPORD3","","",conn);
					return errcode;
				}
				
			
				/*if( mileStoneId != null && mileStoneId.trim().length() > 0 )
				{
				sql = "Select porder.ord_date , porder.supp_code , porder.tot_amt,porder.ord_amt,"
						+ "porder.curr_code,porder.exch_rate,porder.cr_term,porder.proj_code,"
						+ "porder.site_code__bill,porder.item_ser,(case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end)vouch_adv_amt, "
						+ "porder.tot_amt as amount ,porder.tot_amt, porder.advance, porder.pord_type, porder.provi_tran_id   From porder"
						+ " Where purc_order = ? And confirmed ='Y'";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, porcpid);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					
					supp = rs.getString("supp_code");
					System.out.println("supp :"+supp);
					amount = rs.getDouble("amount");
					System.out.println("amount :"+amount);
					ordamt = rs.getDouble(4);
					System.out.println("lcordamt :"+ordamt);
					curr = rs.getString("curr_code");
					System.out.println("curr :"+curr);
					exch = rs.getDouble("exch_rate");
					System.out.println("exch :"+exch);
					crterm = rs.getString("cr_term");
					System.out.println("crterm :"+crterm);
					proj = rs.getString("proj_code");
					System.out.println("proj :"+proj);
					site = rs.getString("site_code__bill");
					System.out.println("site :"+site);
					itemser = rs.getString("item_ser");
					System.out.println("itemser :"+itemser);
					vouchadvamt = rs.getDouble("vouch_adv_amt");
					System.out.println("vouchadvamt :"+vouchadvamt);
					totalpoamt = rs.getDouble("tot_amt");
					System.out.println("totalpoamt :"+totalpoamt);
					advance = rs.getDouble("advance");
					System.out.println("advance :"+advance);
					type = rs.getString("pord_type");
					System.out.println("type :"+type);
					provpo = rs.getString("provi_tran_id");
					System.out.println("provpo :"+provpo);
					poid = rs.getString("purc_order");
					System.out.println("poid :"+poid);
					poid=porcpid;
					tranDate = rs.getTimestamp("ord_date");
					System.out.println("tranDate :"+tranDate);
					ordAmt = rs.getDouble("ord_amt");
					
					//poid = rs.getString("purc_order");
					
					
					
					
					
				}
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					errcode = itmDBAccessLocal.getErrorString("","VTPORD3","","",conn);
					return errcode;
				}
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
				}*/
				if(rs1 != null)
				{
				rs1.close();
				rs1 = null;
				}
				if(pstmt1!= null)
				{
				pstmt1.close();
				pstmt1 = null;
				}
			}
			
           
			
			
			System.out.println("@@@@@@@@@@@@@@@@@");
			sql="select bank_code, fin_entity from   site where site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, site);
			rs = pstmt.executeQuery();
            if(rs.next())
			{
            	System.out.println("!!!!");
				bankcode=rs.getString("bank_code");
				finent=rs.getString("fin_entity");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			System.out.println("bankcode :"+bankcode);
			System.out.println("finent :"+finent);

			if (as_flag.equals("PO") || as_flag.equals("POPRO"))
			{
				code = "01";
			} else if (as_flag.equals("PR")  || as_flag.equals("ML") )
			{
				code = "02";
			} else if (as_flag.equals("QC"))
			{
				code = "03";

			}
			System.out.println("code :"+code);
			
			
		/*	sql = "Select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv from pord_pay_term where  purc_order =? and    case when vouch_created is null then 'N' else vouch_created end = 'N'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, poid);
			rs = pstmt.executeQuery();
			if (errcode == null || errcode.trim().length() == 0) 
			{
				while (rs.next()) 
				{
					lineno = rs.getInt("line_no");
					relagnst = checkNull(rs.getString("rel_agnst"));
					advtype = checkNull(rs.getString("amt_type"));
					advperc = rs.getDouble("rel_amt");
					relafter = rs.getInt("rel_after");
					retperc = rs.getDouble("retention_perc");
					taxclass = checkNull(rs.getString("tax_class"));
					taxchap = checkNull(rs.getString("tax_chap"));
					taxenv = checkNull(rs.getString("tax_env"));
					sitecodeadv = checkNull(rs.getString("site_code__adv"));
					
					
					
					if (sitecodeadv.length() > 0) 
					{
						if (sitecodeadv != SITECODEORD) 
						{
							continue;
						}
					} else {
						if (edilink = true) 
						{

							continue;
						}

					}

					if ((relagnst == "04") || (code != relagnst)) {
						continue;
					}

					lineno = rs.getInt("line_no");
					relagnst = checkNull(rs.getString("rel_agnst"));
					advtype = checkNull(rs.getString("amt_type"));
					advperc = rs.getDouble("rel_amt");
					relafter = rs.getInt("rel_after");
					retperc = rs.getDouble("retention_perc");
					taxclass = checkNull(rs.getString("tax_class"));
					taxchap = checkNull(rs.getString("tax_chap"));
					taxenv = checkNull(rs.getString("tax_env"));
					sitecodeadv = checkNull(rs.getString("site_code__adv"));
					if (sitecodeadv.length() > 0) 
					{
						if (sitecodeadv != SITECODEORD) 
						{
							continue;
						}
					} else {
						if (edilink = true) 
						{

							continue;
						}

					}

					if ((relagnst == "04") || (code != relagnst)) {
						continue;
					}

					if (advtype == "01") {

						advtype = "B";
					} else if (advtype == "02") {

						advtype = "P";
					} else if (advtype == "03") {

						advtype = "F";

					}

					if (advtype == "01") {

						advtype = "B";
					} else if (advtype == "02") {

						advtype = "P";
					} else if (advtype == "03") {

						advtype = "F";

					}
				}*/
				if (tranId == provpo) 
				{
					// populateerror(9999,"populateerror");
					// errcode = "VTPURPROV1" + " ~t Prov. PO : " +
					// as_tran_id;
					// errcode = gf_error_location(ls_errcode);
					// return errcode;
				}
				if (errcode.trim().length() > 0) 
				{
					errcode = itmDBAccessLocal.getErrorString("","VTPURPROV1 ","","",conn);

					return errcode;
				}
				System.out.println("pay mode from supplier");
				
				sql = "select pay_mode,acct_code__ap,cctr_code__ap,acct_code__ap_adv,cctr_code__ap_adv from  supplier where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, supp);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					paymode = rs.getString("pay_mode");
					acctap = rs.getString("acct_code__ap");
					cctrap = rs.getString("cctr_code__ap");
					acctapadv = rs.getString("acct_code__ap_adv");
					cctrapadv = rs.getString("cctr_code__ap_adv");

				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((invacct == "NULL") || (invacct == "NULLFOUND")
						|| (invacct.trim().length() == 0)) {
					invacct = "N";

				}
/*
				sql = "select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV,acct_code__cr,cctr_code__cr from  porddet where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					acctapadv = rs.getString("ACCT_CODE__AP_ADV");
					cctrapadv = rs.getString("CCTR_CODE__AP_ADV");
					acctcodecr = rs.getString("acct_code__cr");
					cctrcodecr = rs.getString("cctr_code__cr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				*/
				
				
				
				
				if ((acctap == null) || (invacct.trim().length() == 0)) 
				{
					cctrapitem = this.finCommon.getAcctDetrTtype("", itemser,"PO", type, conn);
					acctapitem = cctrapitem.substring(0, cctrapitem
							.indexOf(","));
					if (acctapitem == null) 
					{
						errcode = "VTSUPPAC";
					}
				}
				if (cctrap == null || cctrap.trim().length() == 0) 
				{
					cctrap = cctrapitem;
				}
				if (acctcodecr == null || acctcodecr.trim().length() == 0) 
				{
					acctcodecr = acctap;

					if (cctrapadv == null || cctrapadv.trim().length() == 0) 
					{
						sql = "select cctr_code__cr from  porddet where purc_order = ?";// :as_tran_id;/tranId
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cctrapadv = rs.getString("cctr_code__cr");
						}

						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

					}
				}

				if (cctrap == "NULL" || cctrap.trim().length() == 0) {
					cctrap = cctrapadv;
				}
				
				//added by cpatil on 24/07/15 for account code setting
				sql = "select pay_mode,acct_code__ap,cctr_code__ap,acct_code__ap_adv,cctr_code__ap_adv from  supplier where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, supp);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					paymode = rs.getString("pay_mode");
					acctap = rs.getString("acct_code__ap");
					cctrap = rs.getString("cctr_code__ap");
					acctapadv = rs.getString("acct_code__ap_adv");
					cctrapadv = rs.getString("cctr_code__ap_adv");

				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("@@@ supplier::acctap["+acctap+"]cctrap["+cctrap+"]acctapadv["+acctapadv+"]cctrapadv["+cctrapadv+"]");
				
				
				if (errcode.trim().length() == 0) 
				{
					
					sql = "Select key_string from transetup where tran_window =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, win);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						key = rs.getString("key_string");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = "select key_string from transetup where tran_window ='GENERAL'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();

					if (rs.next())
					{
						key = rs.getString("key_string");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("Advtype>>>>>>>>>>>>" + advtype );
					/*generator = new TransIDGenerator(xmlBuff.toString(),userId,CommonConstants.DB_NAME);
					vouchid = generator.generateTranSeqID("VOUCH", "tran_id", key, conn);*/
					if (errcode.trim().length() > 0) {
						return errcode;
					}
					if (advtype == "P") {
						advperc = amount * (ad_advperc / 100);
					}
					if (advtype == "F") {
						advperc = ad_advperc;
					}
					if (advtype == "B") {
						advperc = ordamt * (ad_advperc / 100);

					}
					/*if (advtype == "T") {
						System.out.println("Ord Amt>>>>>" + ordamt );
						System.out.println("ad_advperc>>>>>" + ad_advperc );
						
						System.out.println("tax>>>>>" + tax );
						
						
				          advperc = (ordamt * (ad_advperc / 100)) + tax;
				        }
					System.out.println("Advperc>>>>>>>>>>>>>>>>>>>>>>>>" + advperc);*/
					netamtbase = advperc * exch;
					netamt = advperc;
					System.out.println("Netamt>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + netamt);
					sql = "select  sum(adv_amt) amt from 	voucher where tran_id Between '0' and 'Z'and vouch_type = 'A' and purc_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						vouchamt = rs.getDouble("amt");
					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (totalpoamt < (vouchamt + advperc)) 
					{

						errcode = "VTPURCADV";
					}
					//remark = "PO" + tranId + + "Date Display" + (tranDate, "dd/mm/yyyy")+" Adv. Amt"+ (advperc);
					
					//PO:006PPG0011Date:19/11/2015 Adv. Amt:10000.000
				//	if(errcode.trim().length() == 0)
				//	{
						if (relafter == 0) 
						{
							milestonedt = today;

						}

						else {
							milestonedt = utilMethods.RelativeDate(today, relafter);

						}

						rndstr = "VOUCH-RND";
						rndoff = finCommon.getFinparams("999999", rndstr, conn);
						if (rndoff != "NULLFOUND") 
						{
							rndoff = rndoff.trim();
							rndstr = "VOUCH" + "-RNDTO";
							lsrndto = finCommon.getFinparams("999999", rndstr, conn);
							if (lsrndto != "NULLFOUND") 
							{

								rndto = Integer.parseInt(lsrndto.trim());
							}
						}
						
						
						/*---------------------------------------------------------------------------------*/
						
						if( mileStoneId != null &&  mileStoneId.trim().length() > 0)
						{
							
							//userId=userInfo.getLoginCode();
							//System.out.println("userId is :"+userId);
							
							// apprv_lead_time column added by Sagar on 27/11/15
							sql = "Select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv, apprv_lead_time from pord_pay_term where  purc_order =? and    case when " +
									"vouch_created is null then 'N' else vouch_created end = 'N' and task_code =?  and REL_AGNST=? and LINE_NO=? ";	
						}
						else
						{
							// apprv_lead_time column added by Sagar on 27/11/15
							sql = "Select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv, apprv_lead_time from pord_pay_term where  purc_order =? and    case when vouch_created is null then 'N' else vouch_created end = 'N'";
						}
						
						
						
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, poid);
						if( mileStoneId != null &&  mileStoneId.trim().length() > 0)
						{
							pstmt.setString(2,mileStoneId); 
							pstmt.setString(3,relAgnstTerm);
							pstmt.setString(4,lineNoTerm);
						}
						rs = pstmt.executeQuery();
						while (rs.next()) 
						{
							    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@");
								lineno = rs.getInt("line_no");
								relagnst = checkNull(rs.getString("rel_agnst"));
								advtype = checkNull(rs.getString("amt_type"));
								advperc = rs.getDouble("rel_amt");
								relafter = rs.getInt("rel_after");
								retperc = rs.getDouble("retention_perc");
								taxclass = checkNull(rs.getString("tax_class"));
								taxchap = checkNull(rs.getString("tax_chap"));
								taxenv = checkNull(rs.getString("tax_env"));
								System.out.println("taxenv>>>>>>>>>>" + taxenv);
								sitecodeadv = checkNull(rs.getString("site_code__adv"));
								aprvLeadTime = rs.getString("apprv_lead_time"); // Code added by Sagar on 27/11/15 
								System.out.println(">>>aprvLeadTime:"+aprvLeadTime);
								
								if(aprvLeadTime== null || aprvLeadTime.trim().length()==0)
								{
									aprvLeadTime="0";
								}
								if(aprvLeadTime!=null && aprvLeadTime.trim().length() > 0)
								{
									aprvLeadTime=aprvLeadTime.trim();
									aprvLeadTimeInt=Integer.parseInt(aprvLeadTime);
								}
								
								System.out.println("lineno :"+lineno);
								System.out.println("sitecodeadv :"+sitecodeadv);
								
								/*if (sitecodeadv.length() > 0) 
								{
									System.out.println("SITECODEORD :"+SITECODEORD);
									if (sitecodeadv != SITECODEORD) 
									{
										System.out.println("continue if sitecodeadv not equals to SITECODEORD");
										continue;
									}
								} else {
								
									if (edilink = true) 
									{
											System.out.println("continue if edilink true");
										continue;
									}

								}*/
                               System.out.println("relagnst :"+relagnst);
                               System.out.println("code :"+code);
                               
								/*if ((relagnst == "04") || (code != relagnst)) {
									System.out.println("continue if rel against 4 or code not equals with relagainst");
									continue;
								}
*/
							
                               
                     /*          lineno = rs.getInt("line_no");
								relagnst = checkNull(rs.getString("rel_agnst"));
								advtype = checkNull(rs.getString("amt_type"));
								advperc = rs.getDouble("rel_amt");
								relafter = rs.getInt("rel_after");
								retperc = rs.getDouble("retention_perc");
								taxclass = checkNull(rs.getString("tax_class"));
								taxchap = checkNull(rs.getString("tax_chap"));
								taxenv = checkNull(rs.getString("tax_env"));
								sitecodeadv = checkNull(rs.getString("site_code__adv"));*/
								
								
								
								/*if (sitecodeadv.length() > 0) 
								{
									if (sitecodeadv != SITECODEORD) 
									{
										continue;
									}
								} else {
									if (edilink = true) 
									{

										continue;
									}

								}

								if ((relagnst == "04") || (code != relagnst)) {
									continue;
								}*/
                               System.out.println("@@@@advtype["+advtype+"]ordamt["+ordamt+"]advperc["+advperc+"]amount["+amount+"]");
                          if("ML".equalsIgnoreCase(as_flag))
                          {
                        	
                        	/*String sql1 = " select amount from pur_milstn where PURC_ORDER = ? and LINE_NO__ORD= ? ";
                        			  pstmt1 = conn.prepareStatement(sql1);
          					pstmt1.setString(1, tranId);
          					pstmt1.setString(2, lineNoTerm);
          					rs1 = pstmt1.executeQuery();
          					if (rs1.next()) 
          					{
          						calRelAmt = rs1.getDouble("amount");
          					}

          					rs1.close();
          					rs1 = null;
          					pstmt1.close();
          					pstmt1 = null;*/
          					
                        	  calRelAmt = ad_advperc;
          					System.out.println("@@@@@@@@@@@pur_milstn calRelAmt["+calRelAmt+"]");
                        	  
                          }
                          else
                          {	  
                               if(advtype.equalsIgnoreCase("01"))
               				{
               					calRelAmt=(ordamt * advperc)/100;   // change by cpatil ordAmt to  ordamt on 15/07/15
               				}
               				else if(advtype.equalsIgnoreCase("02"))
               				{
               					calRelAmt=(amount * advperc)/100;
               				}
               				else if(advtype.equalsIgnoreCase("04"))
               				{
               					System.out.println("TAX>>>>>>>>>>>>"+ taxamt );
               					System.out.println("Advperc11111>>>>>>>>>>" + advperc);
               					System.out.println("ORDER amount>>>>>>>>>>" + ordamt);
               					calRelAmt=(ordamt * advperc)/100 + taxamt;
               				}
               				else
               				{
               					calRelAmt=advperc;
               				}
                          }  
                              System.out.println("calRelAmt in voucher:"+calRelAmt); 
                               
                               
								if (advtype == "01") {

									advtype = "B";
								} else if (advtype == "02") {

									advtype = "P";
								} else if (advtype == "03") {

									advtype = "F";

								}
								else if (advtype == "04") {
									
									advtype = "T";

								}

								if (advtype == "01") {

									advtype = "B";
								} else if (advtype == "02") {

									advtype = "P";
								} else if (advtype == "03") {

									advtype = "F";

								}
								else if (advtype == "04") {
									
									advtype = "T";

								}

								generator = new TransIDGenerator(xmlBuff.toString(),userId,CommonConstants.DB_NAME);
								vouchid = generator.generateTranSeqID("VOUCH", "tran_id", key, conn);	
								
							    System.out.println("vouchid : "+vouchid);
								refSer="P-ADV";
								
								if(tranDate!= null) // Code added by Sagar on 26/11/15
								{
									tranDateApp = sdfApp.format(tranDate);
								}
								System.out.println(">>>tranDateApp:"+tranDateApp);
								
								remark = "PO: " + tranId +" Date: " + tranDateApp +" Adv. Amt: "+ calRelAmt; // Code added by Sagar on 20/11/15
								System.out.println(">>>Set remark:"+remark);
								
								// Code added by Sagar on 27/11/15 Start
								sql2 = "select ord_amt from porder where purc_order = ? "; // Comment added by Sagar on 27/11/15
								pstmt2 = conn.prepareStatement(sql2);
								pstmt2.setString(1, tranId);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									ordAmtPord = rs2.getDouble("ord_amt");
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
								System.out.println(">>>voucher_adv ordAmtPord:"+ ordAmtPord);
								// Code added by Sagar on 27/11/15 End
								
								
								
								
								String[] authencate = new String[2];
								authencate[0] = userId;
								authencate[1] = "";
								xmlBuff=null;
								xmlBuff= new StringBuffer("");
								System.out.println("::: XML Cretation!!!!!!!!!!!!!::::");
								System.out.println("::: due date"+milestonedt);

								xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
								xmlBuff.append("<DocumentRoot>");
								xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
								xmlBuff.append("<group0>");
								xmlBuff.append("<description>").append("Group0 description").append("</description>");
								xmlBuff.append("<Header0>");
								xmlBuff.append("<objName><![CDATA[").append("voucher_adv").append("]]></objName>");  
								xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
								xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
								xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
								xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
								xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
								xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
								xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
								xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
								xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
								xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
								xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
								xmlBuff.append("<description>").append("Header0 members").append("</description>");		
								xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"voucher_adv\" objContext=\"1\">");  
								xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
								//xmlBuff.append("<tran_id><![CDATA["+ (vouchid == null ? "" : vouchid) + "]]></tran_id>"); // Comment added by Sagar on 20/Nov/15
								xmlBuff.append("<tran_id/>"); // Code added by Sagar on 20/Nov/15
								xmlBuff.append("<vouch_type><![CDATA[A]]></vouch_type>");
								//sdf =  new SimpleDateFormat(genericUtility.getApplDateFormat());
								xmlBuff.append("<tran_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></tran_date>");
								System.out.println("Date is:"+today);
								xmlBuff.append("<eff_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></eff_date>");
								xmlBuff.append("<purc_order><![CDATA["+ (tranId == null ? "" : tranId) + "]]></purc_order>");
								//xmlBuff.append("<bill_no><![CDATA[]]></bill_no>"); // Comment added by Sagar on 20/Nov/15
								xmlBuff.append("<bill_no><![CDATA["+ (tranId == null ? "" : tranId) + "]]></bill_no>"); // Code added by Sagar on 20/Nov/15
								xmlBuff.append("<bill_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></bill_date>");
								xmlBuff.append("<supp_code><![CDATA["+(supp == null ? "" : supp) + "]]></supp_code>");
								xmlBuff.append("<curr_code><![CDATA["+(curr == null ? "" : curr) + "]]></curr_code>");
								xmlBuff.append("<site_code><![CDATA[" +(site == null ? "" : site)+ "]]></site_code>");
								xmlBuff.append("<fin_entity><![CDATA[" +(finent == null ? "" : finent)+ "]]></fin_entity>");
								xmlBuff.append("<auto_pay><![CDATA[" + "Y" + "]]></auto_pay>");
								xmlBuff.append("<bank_code><![CDATA["+ (bankcode == null ? "" : bankcode)+ "]]></bank_code>");
								xmlBuff.append("<pay_mode><![CDATA["+ (paymode == null ? "" : paymode) + "]]></pay_mode>");
								xmlBuff.append("<cr_term><![CDATA["+ (crterm == null ? "" : crterm) + "]]></cr_term>");
								//xmlBuff.append("<due_date><![CDATA["+ sdf.format(utilMethods.RelativeDate((new java.util.Date()), relafter)).toString() +"]]></due_date>"); // Comment added by Sagar on 27/11/15
								
							    if( mileStoneId != null &&  mileStoneId.trim().length() > 0) // Condition Added by Sagar on 27/11/15
								{
							    	xmlBuff.append("<due_date><![CDATA["+ sdf.format(utilMethods.RelativeDate((new java.util.Date()), aprvLeadTimeInt)).toString() +"]]></due_date>");
								}
							    else
							    {
							    	xmlBuff.append("<due_date><![CDATA["+ sdf.format(utilMethods.RelativeDate((new java.util.Date()), relafter)).toString() +"]]></due_date>");
							    }
								//xmlBuff.append("<bill_amt><![CDATA[" + 0 + "]]></bill_amt>"); // Comment added by Sagar on 20/Nov/15
								xmlBuff.append("<bill_amt><![CDATA[" + ordAmtPord + "]]></bill_amt>");// Code added by Sagar on 20/Nov/15
								xmlBuff.append("<tax_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></tax_date>");
								xmlBuff.append("<tax_amt><![CDATA[" + tax + "]]></tax_amt>");
								xmlBuff.append("<adv_amt><![CDATA[" + String.valueOf(calRelAmt)+ "]]></adv_amt>");
								xmlBuff.append("<tot_amt><![CDATA[" + String.valueOf(calRelAmt)+ "]]></tot_amt>");
								xmlBuff.append("<proj_code><![CDATA["+ (proj == null ? "" : proj) + "]]></proj_code>");
								xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
								xmlBuff.append("<conf_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></conf_date>");
								xmlBuff.append("<paid><![CDATA[]]></paid>");
								//xmlBuff.append("<acct_code><![CDATA["+ (acctcodecr == null ? "" : acctcodecr)+ "]]></acct_code>");
								//xmlBuff.append("<cctr_code><![CDATA[" + cctrcodecr+ "]]></cctr_code>");
								xmlBuff.append("<acct_code__adv><![CDATA["+ (acctap == null ? "" : acctap)+ "]]></acct_code__adv>");//Account Code , cost center values changed by Manoj dtd 29/12/2015
								xmlBuff.append("<cctr_code__adv><![CDATA[" + cctrap + "]]></cctr_code__adv>");//Account Code , cost center values changed by Manoj dtd 29/12/2015
								xmlBuff.append("<emp_code><![CDATA[]]></emp_code>");
								xmlBuff.append("<net_amt><![CDATA[" + String.valueOf(calRelAmt)+ "]]></net_amt>");
								xmlBuff.append("<chg_date><![CDATA[" + sdf.format(new java.util.Date()).toString()+ "]]></chg_date>");
								//xmlBuff.append("<chg_date><![CDATA[" + today+ "]]></chg_date>");
								xmlBuff.append("<chg_user><![CDATA["+ (userId == null ? "BASE" : userId)+ "]]></chg_user>");
								xmlBuff.append("<chg_term><![CDATA["+ (termid == null ? "BASE" : termid)+ "]]></chg_term>");
								xmlBuff.append("<net_amt__bc><![CDATA[" + String.valueOf(calRelAmt)+ "]]></net_amt__bc>");
								xmlBuff.append("<diff_amt__exch><![CDATA["+ 0 +"]]></diff_amt__exch>");
								xmlBuff.append("<cctr_code><![CDATA["+ (cctrapadv == null ? "" : cctrapadv)+ "]]></cctr_code>");//Account Code , cost center values changed by Manoj dtd 29/12/2015
								xmlBuff.append("<acct_code><![CDATA["+ (acctapadv == null ? "" : acctapadv)+ "]]></acct_code>");//Account Code , cost center values changed by Manoj dtd 29/12/2015
								xmlBuff.append("<supp_bill_amt><![CDATA[" + netamt+ "]]></supp_bill_amt>");
								xmlBuff.append("<exch_rate><![CDATA[" + exch+ "]]></exch_rate>");
								xmlBuff.append("<tax_class><![CDATA["+ (taxclass == null ? "" : taxclass)+ "]]></tax_class>");
								xmlBuff.append("<tax_chap><![CDATA["+ (taxchap == null ? "" : taxchap) + "]]></tax_chap>");
								xmlBuff.append("<tax_env><![CDATA["+ (taxenv == null ? "" : taxenv) + "]]></tax_env>");
								xmlBuff.append("<rnd_off><![CDATA["+ (rndoff == null ? "" : rndoff) + "]]></rnd_off>");
								xmlBuff.append("<rnd_to><![CDATA[" + rndto + "]]></rnd_to>");
								//xmlBuff.append("<remarks><![CDATA["+ (remark == null ? "" : remark) + "]]></remarks>"); // Comment added by Sagar on 20/Nov/15
								xmlBuff.append("<remarks><![CDATA["+ (remark == null ? "" : remark) + "]]></remarks>");// Code added by Sagar on 20/Nov/15
								xmlBuff.append("<tran_mode><![CDATA[A]]></tran_mode>");
								xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
								xmlBuff.append("</Detail1>");
								/*xmlBuff.append("<Detail2 dbID=\"\" domID=\"1\" objContext=\"2\" objName=\"voucher_adv\">");
								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"O\" updateFlag=\"A\"/>");
								xmlBuff.append("<tran_id><![CDATA["+ (vouchid == null ? "" : vouchid) + "]]></tran_id>");
								xmlBuff.append("<line_no><![CDATA[1]]></line_no>");
								xmlBuff.append("<ref_ser><![CDATA[" + refSer.trim() + "]]></ref_ser>");
								xmlBuff.append("<ref_no><![CDATA[" + poid + "]]></ref_no>");
								xmlBuff.append("<acct_code><![CDATA["+ (acctcodecr == null ? "" : acctcodecr)+ "]]></acct_code>");
								xmlBuff.append("<cctr_code><![CDATA[" + cctrcodecr+ "]]></cctr_code>");
								xmlBuff.append("<tot_amt><![CDATA[" + advperc+"]]></tot_amt>");
								xmlBuff.append("<tax_amt><![CDATA[" + tax + "]]></tax_amt>");
								xmlBuff.append("</Detail2>");*/
							
								xmlBuff.append("<Detail4 dbID=\"\" domID=\"1\" objContext=\"4\" objName=\"voucher_adv\">");
								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"O\" updateFlag=\"A\"/>");
								xmlBuff.append("<tran_id><![CDATA["+ (vouchid == null ? "" : vouchid) + "]]></tran_id>");
								xmlBuff.append("<line_no><![CDATA["+ 1 + "]]></line_no>");
								xmlBuff.append("<acct_code><![CDATA["+ (acctap == null ? "" : acctap)+ "]]></acct_code>");//Account Code , cost center values changed by Manoj dtd 29/12/2015
								xmlBuff.append("<cctr_code><![CDATA[" + cctrap+ "]]></cctr_code>");//Account Code , cost center values changed by Manoj dtd 29/12/2015
								xmlBuff.append("<amount><![CDATA[" + String.valueOf(calRelAmt)+ "]]></amount>");
								xmlBuff.append("<emp_code><![CDATA[]]></emp_code>");
								xmlBuff.append("<tax_amt><![CDATA[" + tax + "]]></tax_amt>");
								xmlBuff.append("<tax_class><![CDATA["+ (taxclass == null ? "" : taxclass)+ "]]></tax_class>");
								xmlBuff.append("<tax_chap><![CDATA["+ (taxchap == null ? "" : taxchap) + "]]></tax_chap>");
								xmlBuff.append("<tax_env><![CDATA["+ (taxenv == null ? "" : taxenv) + "]]></tax_env>");
								xmlBuff.append("<apply_tax><![CDATA["+ (applytax == null ? "" : applytax)+ "]]></apply_tax>");
								xmlBuff.append("</Detail4>");
								xmlBuff.append("</Header0>");
								xmlBuff.append("</group0>");
								xmlBuff.append("</DocumentRoot>");

								System.out.println("xml.toString() for voucher [" + xmlBuff.toString()+ "]");
								masterStatefulLocal = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
								System.out.println("-----------masterStateful------- " + masterStatefulLocal);
								errcode = masterStatefulLocal.processRequest(authencate, site, true, xmlBuff.toString(),false,conn);
								//Comment Added by Sagar on 20/11/15  
								/*System.out.println("errcode.:: " + errcode);
								System.out.println("Value1.:: " + xmlBuff.toString());
								System.out.println("Value2.:: " + xmlBuff.toString().indexOf("<TranID>"));
								
								
								String tranIdVoucher = xmlBuff.toString().substring(xmlBuff.toString().indexOf("<tran_id>") +  xmlBuff.toString().indexOf("</tran_id>"));
								System.out.println("tranIdVoucher is :["+tranIdVoucher+"]as_flag["+as_flag+"]errcode["+errcode+"]");*/
								//Comment Added by Sagar on 20/11/15  
							//}
							
							
							System.out.println("CreatePoVoucher errcode  :"+errcode);
							if ( (errcode == null || errcode.trim().length() == 0 ) || ( errcode != null  &&  errcode.indexOf("Success") > -1  ) ) 
							{
								System.out.println("as_flag :"+as_flag);
								System.out.println("mileStoneId :"+mileStoneId);
								
								if( as_flag == "POPRO" || as_flag == "PO" || ( mileStoneId != null && mileStoneId.trim().length() > 0  ) )
								{
									sql="update pord_pay_term  set  vouch_created = 'Y' where  purc_order    = ? and line_no =?";
									pstmtUp = conn.prepareStatement(sql);
									pstmtUp.setString(1, tranId);
									pstmtUp.setInt(2,lineno);
									updCnt = pstmtUp.executeUpdate();
									System.out.println("updCnt :"+updCnt);
									if(updCnt > 0)
									{
										conn.commit();
									}
									pstmtUp.close();
									pstmtUp = null;
								}
							}	
								
								
								
								
								
								
						}//end of while loop
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						
						
						
						
						
						/*------------------------------------------------------------------------------------*/
						
						
						
						
						
					/*	String[] authencate = new String[2];
						authencate[0] = "";
						authencate[1] = "";
						xmlBuff=null;
						xmlBuff= new StringBuffer("");
						System.out.println("::: XML Cretation::::");
						System.out.println("::: due date"+milestonedt);

						xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
						xmlBuff.append("<DocumentRoot>");
						xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
						xmlBuff.append("<group0>");
						xmlBuff.append("<description>").append("Group0 description").append("</description>");
						xmlBuff.append("<Header0>");
						xmlBuff.append("<objName><![CDATA[").append("voucher_adv").append("]]></objName>");  
						xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
						xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
						xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
						xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
						xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
						xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
						xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
						xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
						xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
						xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
						xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
						xmlBuff.append("<description>").append("Header0 members").append("</description>");		
						xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"voucher_adv\" objContext=\"1\">");  
						xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
						xmlBuff.append("<tran_id><![CDATA["+ (vouchid == null ? "" : vouchid) + "]]></tran_id>");
						xmlBuff.append("<vouch_type><![CDATA[A]]></vouch_type>");
						sdf =  new SimpleDateFormat(genericUtility.getApplDateFormat());
						xmlBuff.append("<tran_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></tran_date>");
						System.out.println("Date is:"+today);
						xmlBuff.append("<eff_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></eff_date>");
						xmlBuff.append("<purc_order><![CDATA["+ (tranId == null ? "" : tranId) + "]]></purc_order>");
						xmlBuff.append("<bill_no><![CDATA[]]></bill_no>");
						xmlBuff.append("<bill_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></bill_date>");
						xmlBuff.append("<supp_code><![CDATA["+(supp == null ? "" : supp) + "]]></supp_code>");
						xmlBuff.append("<curr_code><![CDATA["+(curr == null ? "" : curr) + "]]></curr_code>");
						xmlBuff.append("<site_code><![CDATA[" +(site == null ? "" : site)+ "]]></site_code>");
						xmlBuff.append("<fin_entity><![CDATA[" +(finent == null ? "" : finent)+ "]]></fin_entity>");
						xmlBuff.append("<auto_pay><![CDATA[" + "Y" + "]]></auto_pay>");
						xmlBuff.append("<bank_code><![CDATA["+ (bankcode == null ? "" : bankcode)+ "]]></bank_code>");
						xmlBuff.append("<pay_mode><![CDATA["+ (paymode == null ? "" : paymode) + "]]></pay_mode>");
						xmlBuff.append("<cr_term><![CDATA["+ (crterm == null ? "" : crterm) + "]]></cr_term>");
						xmlBuff.append("<due_date><![CDATA["+ sdf.format(utilMethods.RelativeDate((new java.util.Date()), relafter)).toString() +"]]></due_date>");
						xmlBuff.append("<bill_amt><![CDATA[" + 0 + "]]></bill_amt>");
						xmlBuff.append("<tax_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></tax_date>");
						xmlBuff.append("<tax_amt><![CDATA[" + tax + "]]></tax_amt>");
						xmlBuff.append("<adv_amt><![CDATA[" + advperc+ "]]></adv_amt>");
						xmlBuff.append("<tot_amt><![CDATA[" + 0 + "]]></tot_amt>");
						xmlBuff.append("<proj_code><![CDATA["+ (proj == null ? "" : proj) + "]]></proj_code>");
						xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
						xmlBuff.append("<conf_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></conf_date>");
						xmlBuff.append("<paid><![CDATA[]]></paid>");
						xmlBuff.append("<acct_code><![CDATA["+ (acctcodecr == null ? "" : acctcodecr)+ "]]></acct_code>");
						xmlBuff.append("<cctr_code><![CDATA[" + cctrcodecr+ "]]></cctr_code>");
						xmlBuff.append("<emp_code><![CDATA["+null+ "]]></emp_code>");
						xmlBuff.append("<net_amt><![CDATA[" + netamt + "]]></net_amt>");
						xmlBuff.append("<chg_date><![CDATA[" + today+ "]]></chg_date>");
						xmlBuff.append("<chg_user><![CDATA["+ (userId == null ? "BASE" : userId)+ "]]></chg_user>");
						xmlBuff.append("<chg_term><![CDATA["+ (termid == null ? "BASE" : termid)+ "]]></chg_term>");
						xmlBuff.append("<net_amt__bc><![CDATA[" + netamtbase+ "]]></net_amt__bc>");
						xmlBuff.append("<diff_amt__exch><![CDATA["+ 0 +"]]></diff_amt__exch>");
						xmlBuff.append("<cctr_code__cf><![CDATA["+ (cctrcodecr == null ? "" : cctrcodecr)+ "]]></cctr_code__cf>");
						xmlBuff.append("<acct_code__adv><![CDATA["+ (acctcodecr == null ? "" : acctcodecr)+ "]]></acct_code__adv>");
						xmlBuff.append("<supp_bill_amt><![CDATA[" + netamt+ "]]></supp_bill_amt>");
						xmlBuff.append("<exch_rate><![CDATA[" + exch+ "]]></exch_rate>");
						xmlBuff.append("<tax_class><![CDATA["+ (taxclass == null ? "" : taxclass)+ "]]></tax_class>");
						xmlBuff.append("<tax_chap><![CDATA["+ (taxchap == null ? "" : taxchap) + "]]></tax_chap>");
						xmlBuff.append("<tax_env><![CDATA["+ (taxenv == null ? "" : taxenv) + "]]></tax_env>");
						xmlBuff.append("<rnd_off><![CDATA["+ (rndoff == null ? "" : rndoff) + "]]></rnd_off>");
						xmlBuff.append("<rnd_to><![CDATA[" + rndto + "]]></rnd_to>");
						xmlBuff.append("<remarks><![CDATA["+ (remark == null ? "" : remark) + "]]></remarks>");
						xmlBuff.append("<tran_mode><![CDATA[A]]></tran_mode>");
						xmlBuff.append("</Detail1>");
						xmlBuff.append("<Detail4 dbID=\"\" domID=\"1\" objContext=\"4\" objName=\"voucher_adv\">");
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"O\" updateFlag=\"A\"/>");
						xmlBuff.append("<tran_id><![CDATA["+ (vouchid == null ? "" : vouchid) + "]]></tran_id>");
						xmlBuff.append("<line_no><![CDATA["+ 1 + "]]></line_no>");
						xmlBuff.append("<acct_code><![CDATA["+ (acctcodecr == null ? "" : acctcodecr)+ "]]></acct_code>");
						xmlBuff.append("<cctr_code><![CDATA[" + cctrcodecr+ "]]></cctr_code>");
						xmlBuff.append("<amount><![CDATA[" + advperc+"]]></amount>");
						xmlBuff.append("<emp_code><![CDATA[]]></emp_code>");
						xmlBuff.append("<tax_amt><![CDATA[" + tax + "]]></tax_amt>");
						xmlBuff.append("<apply_tax><![CDATA["+ (applytax == null ? "" : applytax)+ "]]></apply_tax>");
						xmlBuff.append("</Detail4>");
						xmlBuff.append("</Header0>");
						xmlBuff.append("</group0>");
						xmlBuff.append("</DocumentRoot>");

						System.out.println("abhixml.toString() [" + xmlBuff.toString()+ "]");
						masterStatefulLocal = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
						System.out.println("-----------masterStateful------- " + masterStatefulLocal);
						errcode = masterStatefulLocal.processRequest(authencate, site, true, xmlBuff.toString());
						System.out.println("errcode.:: " + errcode);
						System.out.println("Value1.:: " + xmlBuff.toString());
						System.out.println("Value2.:: " + xmlBuff.toString().indexOf("<TranID>"));
						String tranIdVoucher = xmlBuff.toString().substring(xmlBuff.toString().indexOf("<tran_id>") +  xmlBuff.toString().indexOf("</tran_id>"));
						System.out.println("tranIdVoucher is :["+tranIdVoucher+"]as_flag["+as_flag+"]errcode["+errcode+"]");
					//}
					
					
					System.out.println("CreatePoVoucher errcode  :"+errcode);
					if ( (errcode == null || errcode.trim().length() == 0 ) || ( errcode != null  &&  errcode.indexOf("Success") > -1  ) ) 
					{
						System.out.println("as_flag :"+as_flag);
						System.out.println("mileStoneId :"+mileStoneId);
						
						if( as_flag == "POPRO" || as_flag == "PO" || ( mileStoneId != null && mileStoneId.trim().length() > 0  ) )
						{
							sql="update pord_pay_term  set  vouch_created = 'Y' where  purc_order    = ? and line_no =?";
							pstmtUp = conn.prepareStatement(sql);
							pstmtUp.setString(1, tranId);
							pstmtUp.setInt(2,lineno);
							updCnt = pstmtUp.executeUpdate();
							System.out.println("updCnt :"+updCnt);
							if(updCnt > 0)
							{
								conn.commit();
							}
							pstmtUp.close();
							pstmtUp = null;
						}
					}*/
				}//end of if for errorcode
			//}
		}
		catch(Exception e)
		{

			try
			{
				conn.rollback();
			}
			catch (Exception e1)
			{
			}
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}

					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;					
					}
					if(pstmtUp != null)
					{
						pstmtUp.close();
						pstmtUp = null;					
					}
					System.out.println("@@@@@1099 mileStoneId["+mileStoneId+"]");
					if(mileStoneId == null || mileStoneId.trim().length() == 0 )
					{
						conn.close();
						conn=null;
					}
				}

			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::"+errcode);
		return errcode;


	}
	
	
	
	
	/*------------------------------------------------------------------------------------------------------*/
	
	private String checkNull(String value)
	{
		if (value == null)
		{
			value = "";
		}
		return value;
	}
}

