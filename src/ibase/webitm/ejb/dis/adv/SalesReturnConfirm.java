package ibase.webitm.ejb.dis.adv;

import ibase.utility.CommonConstants;

import ibase.webitm.utility.*;
import ibase.webitm.utility.wms.CommonWmsUtil;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.InvHoldGen;
import ibase.webitm.ejb.dis.PostOrdInvoicePost;
import ibase.webitm.ejb.dis.PostOrderActivity;
import ibase.webitm.ejb.dis.StockUpdate;
import ibase.webitm.ejb.fin.*;
import ibase.webitm.ejb.fin.adv.CalculateCommission;
import ibase.webitm.ejb.fin.adv.DrCrRcpConf;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.ejb.sys.UtilMethods;

import java.rmi.RemoteException;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.util.Date;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;

@Stateless
public class SalesReturnConfirm extends ActionHandlerEJB implements SalesReturnConfLocal, SalesReturnConfRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	String DB = CommonConstants.DB_NAME;
	ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	//ValidatorEJB vdt = new ValidatorEJB();
	CommonWmsUtil common = new  CommonWmsUtil(); 
	DistCommon distCommon = new DistCommon();
	//Added by wasim [START]
	InvAcct invacct = new InvAcct();
	String gs_run_mode = "";
	FinCommon finCommon = new FinCommon();
	//Added by wasim [END]

	@Override

	//Changed by wasim on 23-MAY-2017 for overloading confirm method as to accept Connection parameter [START]
	public String confirm(String msalereturn, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String retString = "";
		Connection conn = null;
		try
		{
			retString = confirm(msalereturn,xtraParams,forcedFlag,conn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return retString;
	}
	//Changed by wasim on 23-MAY-2017 for overloading confirm method as to accept Connection parameter [END]

	//Changed by wasim on 23-MAY-2017 for overloading confirm method as to accept Connection parameter [START]
	//public String confirm(String msalereturn, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	public String confirm(String msalereturn, String xtraParams, String forcedFlag,Connection conn)throws RemoteException, ITMException
	{
		//Changed by wasim on 23-MAY-2017 for overloading confirm method as to accept Connection parameter [END]

		//System.out.println("SalesReturnConf Called........" + msalereturn+"connectionn--"+conn);

		//Connection conn = null;

		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		ResultSet rs = null;
		PreparedStatement pstmt = null;
		java.sql.Timestamp chgDate = null,currDate = null;
		String sql = "", ls_invoice_id ="", ls_full_ret = "", ls_var_value = "", ls_lr_no = "",ls_status = "", ls_confirmed ="",ls_sale_order = "",ls_cust_code__trf="";
		String ls_errcode = "", mreturn = "", ls_edioption = "",totAmtstr="", ls_ledg_post_conf = "", ls_verify_password_filter = "", ls_filter = "", ls_cust_code__bill = "";
		String errMsg = "";
		double lc_sret_amt = 0, lc_inv_amt = 0, lc_amt_drcr	= 0,totAmt;	
		boolean  lb_ask_pwd=false;
		boolean isError = false;
		Timestamp sysDate = null;//Added by wasim on 19-MAY-2017
		boolean isLocConn = false;//Added by wasim on 23-MAY-2017
		try
		{
			//Changed by wasim on 23-MAY-2017 to check is connection is null then creation connection [START]
			//conn = getConnection();
			if(conn == null)
			{
				conn = getConnection();
				isLocConn = true;
			}
			//System.out.println("connectionn 2--"+conn);

			//Changed by wasim on 23-MAY-2017 to check is connection is null then creation connection [END]	

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			//Added by wasim on 19-MAY-2017 to get current Date without Timestamp
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(currDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			//Added by wasim on 19-MAY-2017 to get GS_MODE from Xtra Params as per manohar sir
			gs_run_mode = checkNullAndTrim((genericUtility.getValueFromXTRA_PARAMS(xtraParams, "runMode"))); 
			
			sql = "select invoice_id, full_ret,status,confirmed,cust_code__trf from sreturn where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_invoice_id = checkNullAndTrim(rs.getString("invoice_id"));
				ls_full_ret = checkNullAndTrim(rs.getString("full_ret"));
				ls_status = checkNullAndTrim(rs.getString("status"));
				ls_confirmed = checkNullAndTrim(rs.getString("confirmed"));
				ls_cust_code__trf = checkNullAndTrim(rs.getString("cust_code__trf"));
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			//System.out.println("ls_invoice_id---------->>"+ls_invoice_id);
			//System.out.println("ls_status---------->>"+ls_status);
			//System.out.println("ls_confirmed---------->>"+ls_confirmed);

			if("S".equalsIgnoreCase(ls_status))
			{
				errMsg = itmDBAccessLocal.getErrorString("","VTALSUB","","",conn);
				return errMsg;
			}
			else if("X".equalsIgnoreCase(ls_status))
			{
				errMsg = itmDBAccessLocal.getErrorString("","VTSRET18","","",conn);
				return errMsg;
			}
			if("Y".equalsIgnoreCase(ls_confirmed))
			{
				errMsg = itmDBAccessLocal.getErrorString("","VTMCONF1","","",conn);
				return errMsg;
			}

			ls_var_value = distCommon.getDisparams( "999999", "HO_APPROVAL_REQD", conn);
			//System.out.println("ls_var_value---------->>"+ls_var_value);
			if("Y".equalsIgnoreCase(ls_var_value))
			{
				sql = "select lr_no from despatch where desp_id in ( select desp_id from invoice where invoice_id = ? )";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ls_invoice_id );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ls_lr_no = checkNullAndTrim(rs.getString("lr_no"));
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				//System.out.println("ls_lr_no---------->>"+ls_lr_no);
			}

			// Sneha need to uncomment
			/*if((ls_invoice_id == null || ls_invoice_id.length() == 0) || (ls_lr_no != null && ls_lr_no.length() > 0))
			{
				errMsg = itmDBAccessLocal.getErrorString("","VTHOAPRV","","",conn);
				return errMsg;
			}*/

			//System.out.println("ls_invoice_id---------->>"+ls_invoice_id);
			if(ls_invoice_id.trim()!= null && ls_invoice_id.trim().length() > 0)
			{
				//System.out.println("ls_full_ret---------->>"+ls_full_ret);
				if("Y".equalsIgnoreCase(ls_full_ret))
				{
					sql = "select sum((case when b.net_amt is null then 0 else b.net_amt end) - (case when b.tax_amt is null then 0 else b.tax_amt end)) from sreturn a, sreturndet b " +
							" where b.tran_id = a.tran_id and b.invoice_id = ? and a.status <> 'X' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_invoice_id );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						lc_sret_amt = rs.getDouble(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					//System.out.println("lc_sret_amt---------->>"+lc_sret_amt);

					sql = "select ((case when net_amt is null then 0 else net_amt end) - (case when round_adj is null then 0 else round_adj end) - (case when tax_amt is null then 0 else tax_amt end))" +
							" from invoice where invoice_id = ?";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_invoice_id );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						lc_inv_amt = rs.getDouble(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					//System.out.println("lc_inv_amt---------->>"+lc_inv_amt);

					sql = "select sum(case when a.drcr_flag = 'C' then ((case when b.drcr_amt is null then 0 else b.drcr_amt end) * -1) else 0 End) " +
							" from drcr_rcp a, drcr_rdet b where  a.tran_id = b.tran_id and b.invoice_id  = ? and a.sreturn_no is null ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_invoice_id );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						lc_amt_drcr = rs.getDouble(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					//System.out.println("lc_amt_drcr---------->>"+lc_amt_drcr);
				}

				lc_inv_amt = lc_inv_amt + lc_amt_drcr;
				//System.out.println("lc_inv_amt---------->>"+lc_inv_amt);
				//System.out.println("lc_sret_amt---------->>"+lc_sret_amt);

				if ((lc_sret_amt - lc_inv_amt) > 1 )
				{
					errMsg = itmDBAccessLocal.getErrorString("","VTAMTDIF","","",conn);
					return errMsg;
				}
			}


			/*	nvo_business_object_dist_salesreturn nvo_dist_sales
			nvo_business_object_dist_interstockist nvo_dist_interstock
			 */
			//nvo_dist_sales		  = create nvo_business_object_dist_salesreturn 	

			sql = "select edi_option, (case when ledg_post_conf is null then 'N' else ledg_post_conf end), verify_password " +
					" from transetup where  lower(tran_window) = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"w_salesreturn" );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_edioption = checkNullAndTrim(rs.getString(1));
				ls_ledg_post_conf = checkNullAndTrim(rs.getString(2));
				ls_verify_password_filter = checkNullAndTrim(rs.getString(3));
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			if(ls_verify_password_filter.trim() == null)
			{
				ls_verify_password_filter= "";
			}

			if("db2".equalsIgnoreCase(DB))
			{
				sql = "select confirmed from sreturn where tran_id = ? for update";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ls_confirmed = checkNullAndTrim(rs.getString("confirmed"));
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			else if("mssql".equalsIgnoreCase(DB))
			{
				sql = "select confirmed from sreturn (updlock) where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ls_confirmed = checkNullAndTrim(rs.getString("confirmed"));
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			else
			{
				sql = "select confirmed from sreturn where tran_id = ? for update nowait";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ls_confirmed = checkNullAndTrim(rs.getString("confirmed"));
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			//System.out.println("ls_confirmed---------->>"+ls_confirmed);
			//System.out.println("ls_ledg_post_conf---------->>"+ls_ledg_post_conf);
			//System.out.println("gs_run_mode---------->>"+gs_run_mode);
			//System.out.println("ls_verify_password_filter---------->>"+ls_verify_password_filter);

			if("Y".equalsIgnoreCase(ls_ledg_post_conf))
			{
				if(!"B".equalsIgnoreCase(gs_run_mode))
				{

					sql = "update sreturn set tran_date = ? where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					//Changed by wasim on 15-may-2017 to set current date in Time stamp in prepared statement
					//pstmt.setString(1,currDateStr );
					pstmt.setTimestamp(1,sysDate );
					pstmt.setString(2,msalereturn );
					int rowcnt = pstmt.executeUpdate();
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				}
			}
			if("Y".equalsIgnoreCase(ls_confirmed))
			{
				errMsg = itmDBAccessLocal.getErrorString("","VTMCONF1","","",conn);
				return errMsg;
			}

			if (ls_verify_password_filter.trim().length() > 0)
			{
				lb_ask_pwd = false;
			}

			sql = "select cust_code, invoice_id	from sreturn where  tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_cust_code__bill = checkNullAndTrim(rs.getString("cust_code"));
				ls_invoice_id = checkNullAndTrim(rs.getString("invoice_id"));
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			//System.out.println("ls_cust_code__bill---------->>"+ls_cust_code__bill);
			//System.out.println("ls_invoice_id---------->>"+ls_invoice_id);

			ls_errcode = confirmSalesReturn(msalereturn, xtraParams, conn);
			//ls_errcode = nvo_dist_sales.gbf_sreturn_confirm(msalereturn, 1, sqlca_cp) 

			//System.out.println("after sales return confirm=== ["+ls_errcode+"]");
			if(ls_cust_code__trf.trim().length() > 0)
			{
				//nvo_dist_interstock = create nvo_business_object_dist_interstockist
				//ls_errcode = nvo_dist_interstock.gbf_create_inter_stock_transfer(mreturn,'*',sqlca_cp)
			}

			if (ls_errcode != null && ls_errcode.trim().length() > 0) 
			{
				//errMsg = itmDBAccessLocal.getErrorString("",ls_errcode,"","",conn);
				errMsg = ls_errcode;
				return errMsg;
			}
			System.err.println("errMsg===========>>"+errMsg);
		} 
		catch (Exception e) 
		{
			System.out.println("Inside catch confirm==>"+e.getMessage());
			if(conn!=null)
			{
				try 
				{
					if(isLocConn)
					{
						conn.rollback();
					}
				}
				catch (SQLException ex) 
				{
					e.printStackTrace();
					throw new ITMException(e);

				}
			}
			//end

			isError = true;
			e.printStackTrace();
		}
		finally 
		{
			System.out.println("Inside Finally ["+isError+"] isLocConn["+isLocConn+"] ErrorCode ["+ls_errcode+"] ");
			try
			{
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				//Changed by wasim on 17-05-2017 to check the error code [START]
				/*if (isError)//VTSRTRNCMP 
				{
					conn.rollback();
				} */
				if(isLocConn)
				{
					if(isError || (ls_errcode != null && ls_errcode.indexOf("VTSRTRNCMP") == -1))
					{
						System.out.println("Rollback Transaction.............");
						conn.rollback();
					}
					else 
					{
						System.out.println("Committed Transaction.............");
						conn.commit();//For test only 
					}

					if (conn != null) 
					{
						conn.close();
						conn = null;
					}
				}
				//Changed by wasim on 17-05-2017 to check the error code [END]
			}
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("errString is +++:"+errMsg);
		return errMsg;

	}

	public String confirmSalesReturn(String msalereturn,String xtraParams, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "", dataStr = "";
		PreparedStatement pstmt = null,pstmtUpd = null,pstmtSorditem = null,pstmtSorddet = null,pstmtUpdFree = null ;
		ResultSet rs = null;
		//ValidatorEJB vdt=new ValidatorEJB();
		DistCommon distCommon = new DistCommon();
		HashMap stockUpd = new HashMap();
		//FinCommon finCommon = new FinCommon();
		PostOrderActivity postordact=new PostOrderActivity();
		String	mconf = "",	msite_code = "", mitem_ser	= "", merrcode = "", mitem_code	= "", mflag = "", munit = "", ls_ret_opt = "", mloc_code = "", mlot_no = "", mlot_sl = "",	msite_code_mfg	= "", errorString = "";
		String mvar_value = "", minvoice_id = "", mreas_code = "",	mpack_code = "", minv_stat = "", mfull_ret = "", mcust_code = "", memp_code = "", ls_descr = "", ls_acct_code__dr = "",	ls_cctr_code__dr = "",	ls_acct_code__cr = "";	
		String ls_grade= "",ls_trantype	= "",ls_cctr_code__cr= "",ls_channel_partner = "", ls_tran_type	= "", ls_cust_code= "",	ls_site_code = "", ls_crdt = "",ls_item_code = "", ls_unit = "", ls_hdr = "", ls_det	= "";
		String ls_dis_link = "", ls_site_code_dlv = "",	ls_loc_code_git	= "", ls_tran_stat = "", ls_online_acct = "", ls_edioption = "", ls_ledg_post_conf = ""	,	ls_invoice_id = "", ls_acctmethod = ""	,ls_sretinvqty	= "",  ls_var_value	= "", ls_reas_code	= ""; 
		String ls_conf_drcr_opt = "", ls_conf_jv_opt = "", ls_comm_calc_on_off = "",ls_desp_id = ""	,ls_desp_line_no = "", ls_full_ret = "", ls_lot_no = "", ls_lot_sl = "", ls_sitecodedlv = "",ls_quar_lockcode = "", gs_run_mode = ""; 
		String chgUser = "", chgTerm = "",lc_no_art = "",mline_no = ""; 
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		int	mcount = 0,	mmin_shelf_life = 0	,mdays = 0 , mcount1 = 0,	ll_min_shelf_life =0, ll_count = 0,	ll_counter = 0 , ll_sret_count = 0,	ll_invtrace_count =0,ll_row	=0,	ll_line_no_inv = 0,	ll_invamd_count = 0, ll_line_no = 0;
		Timestamp currDate = null, mtran_date = null;
		Date mmfg_date = null,	mexp_date = null, meff_date = null, mrestrict_upto = null, mchk_date = null,	ld_today = null, ld_tran_date = null, ld_eff_date = null, ld_restr_upto_date = null, ldt_conf_date = null ;
		double	mquantity = 0, mset_qty = 0, mtot_stk = 0, mrate = 0, lc_physicalqty = 0, lc_qty = 0,	lc_qty1 = 0;
		double lc_rate = 0, lc_gross_wt = 0, lc_net_wt = 0, lc_tare_wt = 0, lc_invqty_lot = 0, lc_srqty_lot = 0, lc_srqty_prev = 0,	lc_srqty = 0;

		int	 li_restr_days = 0,	li_cnt = 0, ll_line_no__invtrace = 0, mnet_amt = 0, meff_amt = 0, mret_ref_netamt = 0; 
		int detailCnt= 0;     //added by manish mhatre on 1-sep-2020

		StockUpdate stockUpdate = new StockUpdate();

		/*	nvo_business_object_drcr_rcp i_nvo_drcrrcp 
		i_nvo_drcrrcp = Create nvo_business_object_drcr_rcp 

		nvo_business_object_dist_porcp nvo_porcp
		nvo_porcp= create nvo_business_object_dist_porcp*/
		UtilMethods utilMethods = UtilMethods.getInstance();
		ArrayList tempList = new ArrayList();
		InvHoldGen invHoldGen = new InvHoldGen();
		//Added by wasim 
		boolean isError = false,isOpenOrder = false;
		Timestamp sysDate = null;
		ResultSet rsDetail = null;
		PreparedStatement pstmtDetail = null;
		String errMessage = "";
		int detCnt = 0;
		String docKey = "", gitUpdate= "",reasCodeHdr = "";
		String saleOrder = "", lineNoSord= "", expLev = "", lineType = "",tranIdBal = "",ls_tranidcrn = "";
		int returnPoints = 0,points = 0,offierPoints = 0;
		Timestamp invDate = null;
		double lc_sramt = 0,lc_crnamt = 0;

		try
		{
			//System.out.println("------------------- Inside confirmSalesReturn --------------- ");
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate.getTime());
			//Added by wasim on 19-MAY-2017 to get current without Timestamp
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(currDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			//Added by wasim on 19-MAY-2017 to get GS_MODE from Xtra Params as per manohar sir
			gs_run_mode = checkNullAndTrim((genericUtility.getValueFromXTRA_PARAMS(xtraParams, "runMode"))); 
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			sql = "select edi_option, (case when ledg_post_conf is null then 'N' else ledg_post_conf end) " +
					" from transetup where  lower(tran_window) = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"w_salesreturn_retn" );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_edioption = checkNullAndTrim(rs.getString(1));
				ls_ledg_post_conf = checkNullAndTrim(rs.getString(2));
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			//System.out.println("---------- ls_ledg_post_conf  ---------------"+ls_ledg_post_conf);
			if("Y".equalsIgnoreCase(ls_ledg_post_conf))
			{
				// No vale is initialized for gs_run_mode
				//System.out.println("---------- gs_run_mode  ---------------"+gs_run_mode);
				//Changed by wasim on 19-MAY-2017 to check runMode as not equal to B
				//if("B".equalsIgnoreCase(gs_run_mode))
				if(!"B".equalsIgnoreCase(gs_run_mode))
				{
					sql = "update sreturn set tran_date = ? where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					//Changed by wasim on 15-may-2017 to set current date in Time stamp in prepared statement
					//pstmt.setString(1,currDateStr );
					pstmt.setTimestamp(1,sysDate );
					pstmt.setString(2,msalereturn );
					int rowcnt = pstmt.executeUpdate();
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				}
			}	

			//Changed by wasim on 19-MAY-2017 for changing prepared statement and result set variables because while loop used [START]
			/*sql = "select b.reas_code, b.quantity, b.rate, a.eff_date, b.invoice_id , b.item_code, a.cust_code	,b.line_no__inv, b.full_ret ,b.line_no__invtrace, b.line_no, a.ret_opt,	b.lot_no, b.lot_sl " +
			" from sreturn a,sreturndet b where a.tran_id = b.tran_id and a.tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			 */

			sql = "update min_rate_history set quantity_adj = ( case when quantity_adj is null then 0 else quantity_adj end ) + ? "
					+ " where doc_key = ?";	
			pstmtUpd =  conn.prepareStatement(sql);
			sql = "update sorditem set qty_desp = qty_desp - ?, status = 'P' "
					+ " where sale_order =  ? and line_no = ?  and exp_lev = ? ";
			pstmtSorditem = conn.prepareStatement(sql);
			sql = "update sorddet set status = 'P' "
					+ " where sale_order = ?  and line_no = ?";
			pstmtSorddet = conn.prepareStatement(sql);

			sql = "select b.reas_code, b.quantity, b.rate, a.eff_date, b.invoice_id , b.item_code, a.cust_code	, "
					+ " b.line_no__inv, b.full_ret ,b.line_no__invtrace, b.line_no, a.ret_opt,	b.lot_no, b.lot_sl, "
					+ "  b.doc_key, a.channel_partner, a.reas_code as reas_code_hdr "
					+ " from sreturn a,sreturndet b where a.tran_id = b.tran_id and a.tran_id = ? ";
			pstmtDetail = conn.prepareStatement(sql);
			pstmtDetail.setString(1,msalereturn );
			rsDetail = pstmtDetail.executeQuery();
			while(rsDetail.next())
			{
				//Changed by wasim on 19-MAY-2017 for changing prepared statement and result set variables becuasue while loop used [END]	
				ls_reas_code = checkNullAndTrim(rsDetail.getString(1));
				lc_qty = rsDetail.getDouble(2);
				lc_rate = rsDetail.getDouble(3);
				ld_eff_date = rsDetail.getDate(4);
				ls_invoice_id = checkNullAndTrim(rsDetail.getString(5));
				ls_item_code = checkNullAndTrim(rsDetail.getString(6));
				ls_cust_code = checkNullAndTrim(rsDetail.getString(7));
				ll_line_no_inv = rsDetail.getInt(8);
				ls_full_ret = checkNullAndTrim(rsDetail.getString(9));
				ll_line_no__invtrace = rsDetail.getInt(10);
				ll_line_no = rsDetail.getInt(11);
				ls_ret_opt = checkNullAndTrim(rsDetail.getString(12));
				ls_lot_no = checkNull(rsDetail.getString(13));
				ls_lot_sl = checkNull(rsDetail.getString(14));

				docKey = checkNull(rsDetail.getString("doc_key"));
				gitUpdate = checkNull(rsDetail.getString("channel_partner"));
				reasCodeHdr = checkNull(rsDetail.getString("reas_code_hdr"));

				pstmtUpd.setDouble(1, lc_qty);
				pstmtUpd.setString(2, docKey);
				pstmtUpd.executeUpdate();
				pstmtUpd.clearParameters();


				//System.out.println("---------- ls_invoice_id  ---------------"+ls_invoice_id);
				if(ls_invoice_id .trim()!= null && ls_invoice_id.trim().length() > 0)
				{
					//System.out.println("---------- ll_line_no__invtrace  ---------------"+ll_line_no__invtrace);
					if(ll_line_no__invtrace > 0)
					{
						sql = "select count(1) from sreturndet where tran_id  = ? and invoice_id = ? and line_no__invtrace = ? and line_no <> ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,msalereturn );
						pstmt.setString(2,ls_invoice_id );
						pstmt.setInt(3,ll_line_no__invtrace );
						pstmt.setInt(4,ll_line_no );
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							li_cnt = rs.getInt(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						if(li_cnt > 0) 
						{
							merrcode = "VTINVLN"; 	// Pending
							errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
							return errorString;
						}
					}

					//System.out.println("---------- ll_line_no__invtrace  ---------------"+ll_line_no__invtrace);
					if(ll_line_no__invtrace == 0)
					{
						sql = "select sum(quantity__stduom) from invoice_trace where  invoice_id  = ? and inv_line_no = ? and lot_no = ? and lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_invoice_id );
						pstmt.setInt(2,ll_line_no_inv );
						pstmt.setString(3,ls_lot_no );
						pstmt.setString(4,ls_lot_sl );
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							lc_invqty_lot = rs.getDouble(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
					else
					{
						sql = "select sum(quantity__stduom) from invoice_trace where  invoice_id  = ? and inv_line_no = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_invoice_id );
						pstmt.setInt(2,ll_line_no_inv );
						pstmt.setInt(3,ll_line_no__invtrace );
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							lc_invqty_lot = rs.getDouble(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
					//System.out.println("---------- lc_invqty_lot  ---------------"+lc_invqty_lot);

					sql = "select sum(b.quantity__stduom) from sreturn a, sreturndet b where b.tran_id = a.tran_id and b.invoice_id = ? and b.line_no__inv = ? " +
							" and ((b.line_no__invtrace = ?) or (b.line_no__invtrace is null or b.line_no__invtrace = 0)) and a.tran_id <> ? and a.status  <> 'X'" ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_invoice_id );
					pstmt.setInt(2,ll_line_no_inv );
					pstmt.setInt(3,ll_line_no__invtrace );
					pstmt.setString(4,msalereturn );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						lc_srqty_prev = rs.getDouble(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}	
					//System.out.println("---------- lc_srqty_prev  ---------------"+lc_srqty_prev);

					sql = "select sum(b.quantity__stduom) from sreturn a, sreturndet b where b.tran_id = a.tran_id and b.tran_id = ? and b.invoice_id = ? and b.line_no__inv = ?" +
							" and ((b.line_no__invtrace = ?) or (b.line_no__invtrace is null or b.line_no__invtrace = 0))" ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,msalereturn );
					pstmt.setString(2,ls_invoice_id );
					pstmt.setInt(3,ll_line_no_inv );
					pstmt.setInt(4,ll_line_no__invtrace );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						lc_srqty = rs.getDouble(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}		
					//System.out.println("---------- lc_srqty  ---------------"+lc_srqty);

					lc_srqty_lot = lc_srqty + lc_srqty_prev ;

					System.out.println("---------- lc_srqty_lot  ---------------"+lc_srqty_lot);
					System.out.println("---------- lc_invqty_lot  ---------------"+lc_invqty_lot);
					System.out.println("---------- ls_full_ret  ---------------"+ls_full_ret);

					if (lc_srqty_lot > lc_invqty_lot)
					{
						//System.out.println("---------- ls_ret_opt  ---------------"+ls_ret_opt);
						if(!"D".equalsIgnoreCase(ls_ret_opt))
						{
							merrcode = "VTSRET7";
							errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
							return errorString;
						}
					}
					else if(lc_srqty_lot < lc_invqty_lot && "Y".equalsIgnoreCase(ls_full_ret))
					{
						merrcode = "VTSRET11";
						errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
						return errorString;
					}
					else if(lc_srqty_prev == 0)
					{
						if (lc_srqty_lot == lc_invqty_lot && !"Y".equalsIgnoreCase(ls_full_ret))
						{
							merrcode = "VTSRET10";
							errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
							return errorString;
						}
						else if (lc_srqty_lot != lc_invqty_lot && "Y".equalsIgnoreCase(ls_full_ret))
						{
							merrcode = "VTSRET11";
							errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
							return errorString;
						}
					}
					else if (lc_srqty_lot != lc_invqty_lot && "Y".equalsIgnoreCase(ls_full_ret))
					{
						merrcode = "VTSRET12";
						errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
						return errorString;
					}
				}

				sql = "select restr_days from sreturn_reason where reason_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ls_reas_code );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					li_restr_days = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				//System.out.println("li_restr_days===["+li_restr_days+"]");
				if (li_restr_days > 0 )
				{
					ld_restr_upto_date = utilMethods.RelativeDate(ld_eff_date,li_restr_days);
					//System.out.println("ld_restr_upto_date===["+ld_restr_upto_date+"]");

					sql = "select unit, descr from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_item_code );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						ls_unit = checkNullAndTrim(rs.getString(1));
						ls_descr = checkNullAndTrim(rs.getString(2));
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					sql = "select count(*) from customeritem where cust_code = ? and item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_cust_code );
					pstmt.setString(2,ls_item_code );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						li_cnt = rs.getInt(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					//System.out.println("li_cnt for customeritem["+li_cnt+"]");

					if(li_cnt > 0)
					{
						sql = "select quantity from sreturndet where tran_id = ? "
								+" and line_no = ? "; // 23-jul-2019 manoharan get the quantity of the line_no instead of invoice_trace.line_no to take case in return is not against invoice"
						//+ " and line_no__inv = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,msalereturn );
						//pstmt.setInt(2,ll_line_no_inv );
						pstmt.setInt(2,ll_line_no );
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							lc_qty1 = rs.getInt(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						sql = "update customeritem set restrict_upto = ?, quantity__dem = (case when quantity__dem is null then 0 else quantity__dem end) " +
								" + (? - ?) where cust_code = ? and item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDate(1,new java.sql.Date(ld_restr_upto_date.getTime()));
						pstmt.setDouble(2,lc_qty );
						pstmt.setDouble(3,lc_qty1 );
						pstmt.setString(4,ls_cust_code );
						pstmt.setString(5,ls_item_code );
						int rowcnt = pstmt.executeUpdate();
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
					else
					{	
						sql = "insert into customeritem (cust_code, item_code, item_code__ref, rate__ref, quantity__dem, unit, descr, restrict_upto, " +
								"min_qty, integral_qty, min_shelf_life, chg_date, chg_user, chg_term) " +
								"values(?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_cust_code );
						pstmt.setString(2,ls_item_code );
						pstmt.setString(3,ls_item_code );
						pstmt.setDouble(4,lc_rate );
						pstmt.setDouble(5,lc_qty );
						pstmt.setString(6,ls_unit );
						pstmt.setString(7,ls_descr );
						pstmt.setDate(8,new java.sql.Date(ld_restr_upto_date.getTime()) );
						pstmt.setString(9,"0" );
						pstmt.setString(10,"0" );
						pstmt.setString(11,"0" );
						//Changed by wasim on 15-may-2017 to set current date in Time stamp in prepared statement
						//pstmt.setString(12,currDateStr );
						pstmt.setTimestamp(12,currDate);
						pstmt.setString(13,chgUser );
						pstmt.setString(14,chgTerm );
						pstmt.executeUpdate();
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}	
				}
				//Added by wasim on 19-MAY-2017 for while loop instead of if loop [END]
			}//While loop END for Sales return Detail
			if (rsDetail != null)
			{
				rsDetail.close();
				rsDetail = null;
			}
			if (pstmtDetail != null)
			{
				pstmtDetail.close();
				pstmtDetail = null;
			}
			//Added by wasim on 19-MAY-2017 for while loop instead of if loop [END]


			sql = "Select site_code, item_ser, tran_date, invoice_id, eff_date , cust_code, ret_opt, tran_type, case when site_code__dlv is null then site_code else site_code__dlv end from sreturn where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				msite_code = rs.getString(1);
				mitem_ser = rs.getString(2);
				mtran_date = rs.getTimestamp(3);
				minvoice_id = rs.getString(4);
				meff_date = rs.getDate(5);
				mcust_code = rs.getString(6);
				ls_ret_opt = rs.getString(7);
				ls_trantype = rs.getString(8);
				ls_sitecodedlv = rs.getString(9);
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			// 11-mar-2019 manoharan whether to open the order based on reason code
			String openOrderStr  = distCommon.getDisparams( "999999", "SRET_OPEN_ORD_REASON", conn); 
			// end 11-mar-2019 manoharan whether to open the order based on reason code
			if (reasCodeHdr.trim().equals(openOrderStr.trim()))
			{
				isOpenOrder = true;
			}
			// end 11-mar-2019 manoharan whether to open the order based on reason code
			sql = "select count(1) From sreturndet Where tran_id = ? and qc_reqd = 'Y'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ll_count = rs.getInt(1);
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			if(ll_count > 0 && !"B".equalsIgnoreCase(gs_run_mode))
			{
				merrcode = gbf_create_sretqc(msalereturn, xtraParams, conn);
				//System.out.println("After creating QC order="+merrcode);
				if(merrcode != null && merrcode.trim().length() > 0)
				{
					errorString = itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
					return errorString;
				}
			}
			//System.out.println("------------- ls_ret_opt ------------ "+ ls_ret_opt);
			if("R".equalsIgnoreCase(ls_ret_opt))
			{
				sql = "Select count(*) From sreturndet Where tran_id = ? and ret_rep_flag = 'P'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ll_count = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if(ll_count == 0)
				{
					merrcode = "VTRETREP01";
					//errorString = vdt.getErrorString("",merrcode,"");
					itmDBAccessLocal.getErrorString("",merrcode,"","",conn);
					return errorString;
				}

				sql = "Select abs(sum((case when eff_net_amt is null then 0 else eff_net_amt end))), sum((case when net_amt is null then 0 else net_amt end)) From sreturndet Where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					meff_amt = rs.getInt(1);
					mnet_amt = rs.getInt(2);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				mvar_value = distCommon.getDisparams( "999999", "PERMIT_AMT", conn);
				if(meff_amt > Integer.parseInt(mvar_value))
				{
					merrcode = "VTSRET3";
					//errorString = vdt.getErrorString("",merrcode,"");
					itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;
				}
				sql = "Select	sum((case when net_amt is null then 0 else net_amt end)) from sreturndet Where tran_id	= ? and	ret_rep_flag = 'R'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					mnet_amt = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				mvar_value = distCommon.getDisparams( "999999", "PERMIT_PERCENT", conn);

				if( ((mnet_amt * Integer.parseInt(mvar_value)) / 100 ) < meff_amt) 
				{
					merrcode = "VTSRET3";
					//errorString = vdt.getErrorString("",merrcode,"");
					itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;
				}
			}
			else if("D".equalsIgnoreCase(ls_ret_opt))
			{
				sql = "Select sum((case when net_amt is null then 0 else net_amt end) - (case when tax_amt is null then 0 else tax_amt end)) from sreturndet Where tran_id	= ? and	ret_rep_flag = 'R'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					mnet_amt = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				sql = "Select	net_amt - (case when tax_amt is null then 0 else tax_amt end) from sreturn Where tran_id	= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					mret_ref_netamt = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				mvar_value = distCommon.getDisparams( "999999", "PERMIT_AMT", conn);
				if((mnet_amt - mret_ref_netamt) > Integer.parseInt(mvar_value)) 
				{
					merrcode = "VTAMTDIF1";
					//errorString = vdt.getErrorString("",merrcode,"");
					errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;
				}
			}
			sql = "Select channel_partner, dis_link, site_code__ch from site_customer where cust_code =	? And site_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,mcust_code );
			pstmt.setString(2,msite_code );
			rs = pstmt.executeQuery();
			if(rs.next())
			{				
				ls_channel_partner = checkNullAndTrim(rs.getString(1));
				ls_dis_link = checkNullAndTrim(rs.getString(2));
				ls_site_code_dlv = checkNullAndTrim(rs.getString(3));
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(ls_channel_partner.trim()== null || ls_channel_partner.trim().length()==0)
			{
				sql ="select channel_partner, dis_link, site_code from  customer where cust_code = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, mcust_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_channel_partner = checkNullAndTrim(rs.getString(1));
					ls_dis_link = checkNullAndTrim(rs.getString(2));
					ls_site_code_dlv = checkNullAndTrim(rs.getString(3));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			ls_loc_code_git = distCommon.getDisparams( "999999", "TRANSIT_LOC", conn);

			// 01-mar-2019 manoharan transit entry in case of automatic tranaction also done as suggested by KB
			if( "Y".equalsIgnoreCase(gitUpdate) && "E".equalsIgnoreCase(ls_dis_link))
				// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
				//if( "Y".equalsIgnoreCase(ls_channel_partner) && ("E".equalsIgnoreCase(ls_dis_link) || ("A".equalsIgnoreCase(ls_dis_link) ))
				if( "Y".equals(gitUpdate) && ("E".equalsIgnoreCase(ls_dis_link) || ("A".equalsIgnoreCase(ls_dis_link) )))
				{
					sql = "Select inv_stat From location Where loc_code	= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, ls_loc_code_git);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_tran_stat = checkNullAndTrim(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
			ls_sretinvqty = distCommon.getDisparams("999999","SRET_INV_QTY",conn) ;

			//Changed by wasim on 19-MAY-2017 for changing prepared statements for while loop [START]
			/*sql = "Select line_no, item_code, quantity__stduom, ret_rep_flag, unit__std, loc_code, lot_no, lot_sl, mfg_date, exp_date, site_code__mfg, cost_rate, reas_code, pack_code, full_ret, physical_qty, no_art, line_no__inv,	line_no__invtrace ,	gross_weight, tare_weight, net_weight From sreturndet where tran_id	= ? Order by line_no";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, msalereturn);
			rs = pstmt.executeQuery();*/
			sql = "Select line_no, item_code, quantity__stduom, ret_rep_flag, unit__std, loc_code, lot_no, lot_sl, mfg_date, exp_date, site_code__mfg, cost_rate, reas_code, pack_code, full_ret, physical_qty, no_art, line_no__inv,	line_no__invtrace ,	gross_weight, tare_weight, net_weight From sreturndet where tran_id	= ? Order by line_no";
			pstmtDetail=conn.prepareStatement(sql);
			pstmtDetail.setString(1, msalereturn);
			rsDetail = pstmtDetail.executeQuery();
			//Changed by wasim on 19-MAY-2017 for changing prepared statements for while loop [END]
			//Changed by wasim on 19-MAY-2017 to use while loop instead of if for multiple lines in sales return details
			//if(rs.next())
			while(rsDetail.next())
			{
				detCnt++;//Added by wasim on 05-JUN-2017 for detail counter

				mline_no = checkNullAndTrim(rsDetail.getString(1));
				mitem_code = checkNullAndTrim(rsDetail.getString(2));
				mquantity = rsDetail.getDouble(3);
				mflag = checkNullAndTrim(rsDetail.getString(4));
				munit = checkNullAndTrim(rsDetail.getString(5));
				mloc_code = checkNullAndTrim(rsDetail.getString(6));
				mlot_no = checkNull(rsDetail.getString(7));
				mlot_sl = checkNull(rsDetail.getString(8));
				//Changed by wasim on 16-MAY-2017 to get date in Timestamp [START]
				//mmfg_date = rs.getDate(9);
				//mexp_date = rs.getDate(10);
				mmfg_date = rsDetail.getTimestamp(9);
				mexp_date = rsDetail.getTimestamp(10);
				//Changed by wasim on 16-MAY-2017 to get date in Timestamp [END]
				msite_code_mfg = checkNullAndTrim(rsDetail.getString(11));
				lc_rate = rsDetail.getDouble(12);
				mreas_code = checkNullAndTrim(rsDetail.getString(13));
				mpack_code = checkNullAndTrim(rsDetail.getString(14));
				mfull_ret = checkNullAndTrim(rsDetail.getString(15));
				lc_physicalqty = rsDetail.getDouble(16);
				lc_no_art = checkNullAndTrim(rsDetail.getString(17));
				ll_line_no_inv = rsDetail.getInt(18);
				ll_line_no__invtrace = rsDetail.getInt(19);
				lc_gross_wt = rsDetail.getDouble(20);
				lc_tare_wt = rsDetail.getDouble(21);
				lc_net_wt = rsDetail.getDouble(22);

				if( "Y".equalsIgnoreCase(mfull_ret))
				{
					mvar_value = distCommon.getDisparams( "999999", "RESTRICT_ON_FRET", conn);
				}
				//Changed by wasim on 15-may-2017 if mvar_value is blank or null then set as 0 [START]
				if(mvar_value .trim()== null || mvar_value.trim().length() == 0)
				{
					mvar_value = "0";
				}
				//Changed by wasim on 15-may-2017 if mvar_value is blank or null then set as 0 [END]
				mrestrict_upto = utilMethods.RelativeDate(meff_date,Integer.parseInt(mvar_value));

				sql = "Select count(*) From	customeritem Where cust_code = ? And item_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, mcust_code);
				pstmt.setString(2, mitem_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					mcount1 = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if( "R".equalsIgnoreCase(mflag))
				{
					//System.out.println("$$$$$$$$$$$Inside Sales Return------->["+mflag+"]$$$$$$$$$$$$$$");

					sql = "Select inv_stat From location Where loc_code	= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, mloc_code);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						minv_stat = checkNullAndTrim(rs.getString("inv_stat"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Changed by wasim as this closing bracket will end at after compeleting stock update for Returned tran type (R) 	

					minvoice_id = checkNull(minvoice_id);//Added by wasim as it is giving null pointer if minvoice_id is null
					if(minvoice_id.trim().length() >0)
					{
						sql = "Select inv_type From invoice Where invoice_id = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, minvoice_id);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_trantype = checkNullAndTrim(rs.getString("inv_type"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					if(ls_trantype.trim() == null || ls_trantype.trim().length() == 0)
					{
						ls_trantype = " ";
					}

					sql = "Select acct_code__inv, cctr_code__inv, grade From stock Where site_code = ? And item_code = ? And loc_code = ? And lot_no = ? And lot_sl	= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, msite_code);
					pstmt.setString(2, mitem_code);
					pstmt.setString(3, mloc_code);
					pstmt.setString(4, mlot_no);
					pstmt.setString(5, mlot_sl);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_acct_code__dr = checkNullAndTrim(rs.getString("acct_code__inv"));
						ls_cctr_code__dr = checkNullAndTrim(rs.getString("cctr_code__inv"));
						ls_grade = checkNullAndTrim(rs.getString("grade"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(ls_acct_code__dr .trim()== null || ls_acct_code__dr.trim().length() == 0)
					{
						ls_cctr_code__dr = finCommon.getAcctDetrTtype(mitem_code, mitem_ser, "STKINV", ls_trantype, conn);

						if (ls_cctr_code__dr != null && ls_cctr_code__dr.trim().length() > 0) 
						{
							String tokens [] = ls_cctr_code__dr.split(",");
							//System.out.println("Length=["+tokens.length+"]");

							if ( tokens.length >= 2)
							{
								ls_acct_code__dr = tokens[0];
								ls_cctr_code__dr = tokens[1];

								ls_acct_code__dr = checkNullAndTrim(ls_acct_code__dr);
								ls_cctr_code__dr = checkNullAndTrim(ls_cctr_code__dr);
							}
							else
							{
								ls_acct_code__dr = ls_cctr_code__dr.substring(0,ls_cctr_code__dr.indexOf(","));
								ls_cctr_code__dr = ls_cctr_code__dr.substring(ls_cctr_code__dr.indexOf(",") + 1);
							}
							tokens = null;
						}
						//System.out.println("@@ls_cctr_code__dr ["+ls_cctr_code__dr+"]");
						//ls_acct_code__dr = ls_cctr_code__dr.substring(0,ls_cctr_code__dr.indexOf(","));
						//ls_cctr_code__dr = ls_cctr_code__dr.substring(ls_cctr_code__dr.indexOf(",")+1);
					}
					if(ls_cctr_code__cr == null || ls_cctr_code__cr.length() == 0)
					{
						ls_cctr_code__cr = finCommon.getAcctDetrTtype(mitem_code, mitem_ser, "COGS", ls_trantype, conn);
						if (ls_cctr_code__cr != null && ls_cctr_code__cr.trim().length() > 0) 
						{
							String tokens [] = ls_cctr_code__cr.split(",");
							//System.out.println("Length=["+tokens.length+"]");

							if ( tokens.length >= 2)
							{
								ls_acct_code__cr = tokens[0];
								ls_cctr_code__cr = tokens[1];

								ls_acct_code__cr = checkNullAndTrim(ls_acct_code__cr);
								ls_cctr_code__cr = checkNullAndTrim(ls_cctr_code__cr);
							}
							else
							{
								ls_acct_code__cr = ls_cctr_code__cr.substring(0,ls_cctr_code__cr.indexOf(","));
								ls_cctr_code__cr = ls_cctr_code__cr.substring(ls_cctr_code__cr.indexOf(",") + 1);
							}
							tokens = null;
						}

					}

					if(ls_cctr_code__cr == null || ls_cctr_code__cr.trim().length() == 0)
					{
						ls_cctr_code__cr = ls_cctr_code__dr;
					}

					if(minvoice_id != null && minvoice_id.trim().length()>0)
					{
						if(ll_line_no__invtrace == 0)
						{
							sql = "select desp_id,desp_line_no from invoice_trace where invoice_id = ? and inv_line_no = ? and	item_code =	? and lot_no = ? and lot_sl	= ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, minvoice_id);
							pstmt.setInt(2, ll_line_no_inv);
							pstmt.setString(3, mitem_code);
							pstmt.setString(4, mlot_no);
							pstmt.setString(5, mlot_sl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_desp_id = checkNullAndTrim(rs.getString("desp_id"));
								ls_desp_line_no = checkNullAndTrim(rs.getString("desp_line_no"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						else
						{
							sql = "select desp_id,desp_line_no from invoice_trace where invoice_id = ? and inv_line_no = ? and line_no = ? and	item_code =	? and lot_no = ? and lot_sl	= ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, minvoice_id);
							pstmt.setInt(2, ll_line_no_inv);
							pstmt.setInt(3, ll_line_no__invtrace);
							pstmt.setString(4, mitem_code);
							pstmt.setString(5, mlot_no);
							pstmt.setString(6, mlot_sl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_desp_id = checkNullAndTrim(rs.getString("desp_id"));
								ls_desp_line_no = checkNullAndTrim(rs.getString("desp_line_no"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

					sql = "select cost_rate from despatchdet where desp_id = ? and line_no = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, ls_desp_id);
					pstmt.setString(2, ls_desp_line_no);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lc_rate = rs.getDouble("cost_rate");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					stockUpd.put("tran_date",mtran_date) ;
					stockUpd.put("tran_ser","S-RET") ;
					stockUpd.put("gross_rate",lc_rate) ;
					stockUpd.put("tran_id",msalereturn);
					mline_no= "   "+mline_no;
					mline_no = mline_no.substring(mline_no.length()-3, mline_no.length());
					stockUpd.put("line_no",mline_no);
					stockUpd.put("sale_order",minvoice_id);
					stockUpd.put("item_code",mitem_code);
					stockUpd.put("site_code",ls_sitecodedlv); 
					stockUpd.put("loc_code",mloc_code);
					stockUpd.put("unit",munit);
					stockUpd.put("lot_no",mlot_no);
					stockUpd.put("lot_sl", mlot_sl);

					if( lc_physicalqty == 0 )
					{
						lc_physicalqty = mquantity;
					}
					if("P".equalsIgnoreCase(ls_sretinvqty))
					{
						stockUpd.put("qty_stduom",lc_physicalqty);
					}
					else{
						stockUpd.put("qty_stduom",mquantity);
					}
					stockUpd.put("rate",lc_rate);
					stockUpd.put("inv_stat",minv_stat);
					stockUpd.put("exp_date",mexp_date);
					stockUpd.put("item_ser",mitem_ser);
					stockUpd.put("mfg_date",mmfg_date);
					stockUpd.put("site_code__mfg",msite_code_mfg);
					stockUpd.put("reas_code",mreas_code);
					stockUpd.put("pack_code",mpack_code);
					stockUpd.put("tran_type","R");
					stockUpd.put("grade",ls_grade);
					stockUpd.put("acct_code_inv",ls_acct_code__dr);
					stockUpd.put("cctr_code_inv",ls_cctr_code__dr);
					stockUpd.put("acct_code__cr",ls_acct_code__cr);
					stockUpd.put("acct_code__dr",ls_acct_code__dr);
					stockUpd.put("cctr_code__cr",ls_cctr_code__cr);
					stockUpd.put("cctr_code__dr",ls_cctr_code__dr);
					if (lc_no_art == null || lc_no_art.trim().length() == 0 || "null".equals(lc_no_art))
					{
						lc_no_art = "0";
					}
					stockUpd.put("no_art",Double.parseDouble(lc_no_art));
					stockUpd.put("gross_weight",lc_gross_wt);
					stockUpd.put("tare_weight",lc_tare_wt);
					stockUpd.put("net_weight",lc_net_wt);
					System.out.println("Before stockUpdate1 ["+merrcode+"]");
					merrcode = stockUpdate.updateStock(stockUpd,xtraParams, conn);
					System.out.println("After stockUpdate1 ["+merrcode+"]");
					if(merrcode !=null && merrcode.trim().length() >0)  
					{
						return merrcode;
					}

					// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
					// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
					//if( "Y".equalsIgnoreCase(ls_channel_partner) && ("E".equalsIgnoreCase(ls_dis_link) || ("A".equalsIgnoreCase(ls_dis_link) ))
					if( "Y".equals(gitUpdate) && ("E".equalsIgnoreCase(ls_dis_link) || ("A".equalsIgnoreCase(ls_dis_link) )))
					{
						stockUpd.put("inv_stat",ls_tran_stat);
						stockUpd.put("loc_code",ls_loc_code_git);
						stockUpd.put("tran_type","I");
						merrcode = stockUpdate.updateStock(stockUpd,xtraParams, conn);
						//System.out.println("After stockUpdate2 ["+merrcode+"]");
						if(merrcode !=null && merrcode.trim().length() >0)  
						{
							return merrcode;
						}
					}
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// 17-Mar-2018 manoharan in case of full return to reduse despatch quantity
					// 11-mar-2019 manoharan whether to open the order based on reason code
					//if ls_FULLRET_HDR = "Y" and ls_ret_opt = "C" then
					if (isOpenOrder)
					{

						sql = "select SORD_NO,SORD_LINE_NO,exp_lev  from invoice_trace " 
								+ " where invoice_id 	= 	? "  // ": 
								+ " and line_no = 	? ";  
						//+ " and inv_line_no = 	? " //": 
						//+ " and item_code	=	? " // ":mitem_code 
						//+ " and lot_no		=	? " //":mlot_no 
						//+ " and lot_sl		=	? " ;//:mlot_sl;				
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, minvoice_id);
						pstmt.setInt(2, ll_line_no__invtrace);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							saleOrder = rs.getString("SORD_NO");
							lineNoSord = rs.getString("SORD_LINE_NO");
							expLev = rs.getString("exp_lev");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						pstmtSorditem.setDouble(1, mquantity);
						pstmtSorditem.setString(2, saleOrder);
						pstmtSorditem.setString(3, lineNoSord);
						pstmtSorditem.setString(4, expLev);

						pstmtSorditem.executeUpdate();
						pstmtSorditem.clearParameters();


						pstmtSorddet.setString(1, saleOrder);
						pstmtSorddet.setString(2, lineNoSord);
						pstmtSorddet.executeUpdate();
						pstmtSorddet.clearParameters();
					}
					// end 17-Mar-2018 manoharan in case of full return to reduse despatch quantity
					// 11-nov-2019 manoharan in case of value/quantity replacement scheme or point based scheme to reduce the 
					// used_free_qty = ?, used_free_value
					if (minvoice_id != null && minvoice_id.trim().length() >  0 && ll_line_no_inv != 0 && ll_line_no__invtrace != 0)
					{
						returnPoints = 0;
						sql = "select a.line_type, a.SORD_NO,  b.tran_date "  
								+ " from invoice_trace a, invoice b "
								+ " where a.invoice_id = b.invoice_id "
								+ " and a.invoice_id = ? " 
								+ " and line_no = ?  ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, ls_invoice_id);
						pstmt.setInt(2, ll_line_no__invtrace);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							lineType = checkNullAndTrim(rs.getString("line_type"));
							saleOrder = checkNullAndTrim(rs.getString("SORD_NO"));
							invDate = rs.getTimestamp("tran_date");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						String stateCodeDlv = "", countCodeDlv= "", ls_siteCode = "";
						if ("P".equals(lineType))
						{
							// Point based scheme given free get the point from 
							sql = "select state_code__dlv,count_code__dlv,site_code "//into :, :, :  
									+ " from sorder where sale_order = ? : ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{

								stateCodeDlv = checkNullAndTrim(rs.getString("state_code__dlv"));
								countCodeDlv = checkNullAndTrim(rs.getString("count_code__dlv"));
								ls_siteCode =  checkNullAndTrim(rs.getString("site_code"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sql = "select required_points " 
									+ " from SCH_OFFER_ITEMS  where SCHEME_CODE in "
									+ " (select a.scheme_code from scheme_applicability a,scheme_applicability_det  b " 
									+ " where a.scheme_code= b.scheme_code and ? between a.app_from  and a.valid_upto " 
									+ " and (b.site_code= ? or b.state_code = ?  or b.count_code= ?) "
									+ " and PROD_SCH = 'Y') and item_code = ? " ;
							pstmt=conn.prepareStatement(sql);
							pstmt.setTimestamp(1, invDate);
							pstmt.setString(2, ls_siteCode);
							pstmt.setString(3, stateCodeDlv);
							pstmt.setString(4, countCodeDlv);
							pstmt.setString(5, mitem_code);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								points = rs.getInt("required_points");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							returnPoints = (int) (mquantity * points);


							sql = " UPDATE SCHEME_BALANCE SET USED_FREE_VALUE = USED_FREE_VALUE - ? "   
									+ " WHERE  CUST_CODE = ?   AND ITEM_CODE= 'X' AND  ?  between EFF_FROM and  VALID_UPTO";
							pstmtUpdFree = conn.prepareStatement(sql);
							pstmtUpdFree.setInt(1, returnPoints);
							pstmtUpdFree.setString(2, ls_cust_code);
							pstmtUpdFree.setTimestamp(3, invDate);
							pstmtUpdFree.close();
							pstmtUpdFree = null;


						}

						// end 11-nov-2019 manoharan in case of value/quantity replacement scheme or point based scheme to reduce the 
						// 06-dec-2019 manoharan Also if the return is against invoice and in that invoice offer points (BALANCE_FREE_VALUE) are added the same to be reduced scheme_balance
						// sch_group_def.scheme_type = '3' (poits based) then only reduce the points
						// to reduce BALANCE_FREE_VALUE in SCHEME_BALANCE
						sql = "select count(1)  from FREE_BALANCE_TRACE "  
								+ " where INVOICE_ID = ? "
								+ " and LINE_NO_INVOICETRACE = ? "
								+ " and scheme_code in (select scheme_code from sch_group_def where scheme_type = '3')";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, minvoice_id);
						pstmt.setInt(2, ll_line_no__invtrace);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ll_count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						if (ll_count > 0 )
						{

							sql = " select a.line_type, a.SORD_NO,  b.tran_date "//       into :, :,  :invDate 
									+ " from invoice_trace a, invoice b "
									+ " where a.invoice_id = b.invoice_id "
									+ " and a.invoice_id = ? " // :ls_invoice_id 
									+ " and line_no = ?";//:ll_line_no__invtrace;
							pstmt.setString(1, minvoice_id);
							pstmt.setInt(2, ll_line_no__invtrace);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								lineType = rs.getString("line_type");
								saleOrder = rs.getString("SORD_NO");
								invDate = rs.getTimestamp("tran_date");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if ("C".equals(lineType))
							{
								// Point based scheme given free get the point from 
								sql = " select state_code__dlv,count_code__dlv,site_code "  
										+ " from sorder where sale_order = ?";
								pstmt.setString(1, saleOrder);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									stateCodeDlv = rs.getString("state_code__dlv");
									countCodeDlv = rs.getString("count_code__dlv");
									ls_siteCode = rs.getString("site_code");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = " select TRAN_ID__BAL,VAL_BAL_AFTER - VAL_BAL_BEFORE from FREE_BALANCE_TRACE "
										+ " where INVOICE_ID = ? " 
										+ " and LINE_NO_INVOICETRACE = ?";
								pstmt.setString(1, minvoice_id);
								pstmt.setInt(2, ll_line_no__invtrace);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									tranIdBal = rs.getString("TRAN_ID__BAL");
									offierPoints = rs.getInt(2);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;


								if (offierPoints > 0 )
								{
									sql = " UPDATE SCHEME_BALANCE SET BALANCE_FREE_VALUE = BALANCE_FREE_VALUE - ? " //:  
											+ " WHERE  CUST_CODE = ?  AND ITEM_CODE= 'X' AND ?  between EFF_FROM and  VALID_UPTO";
									pstmtUpdFree = conn.prepareStatement(sql);
									pstmtUpdFree.setInt(1, offierPoints);
									pstmtUpdFree.setString(2, ls_cust_code);
									pstmtUpdFree.setTimestamp(3, invDate);
									pstmtUpdFree.close();
									pstmtUpdFree = null;

								}
							}

						}

					}

					// end 06-dec-2019 manoharan Also if the return is against invoice and in that invoice offer points (BALANCE_FREE_VALUE) are added the same to be reduced scheme_balance
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}//END tran type R (Sales return) by wasim on 19-MAY-2017
				else //Added by wasim on 19-05-2017 else part for Replace (ID) 
				{
					//System.out.println("$$$$$$$$$$$Inside Sales Replacement------->["+mflag+"]$$$$$$$$$$$$$$");

					sql = "Select (case when var_value is null then 'N' else var_value end) From finparm Where prd_code = '999999' And var_name = 'INV_ACCT_SRET'";
					pstmt=conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_online_acct = checkNullAndTrim(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(minvoice_id != null && minvoice_id.length() > 0)
					{
						sql = "Select inv_type From invoice Where invoice_id = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, minvoice_id);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_trantype = checkNullAndTrim(rs.getString("inv_type"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						ls_trantype = "  ";
					}

					sql = "Select acct_code__inv, cctr_code__inv, grade From stock Where site_code = ? And item_code = ? And loc_code  = ? And lot_no =  ? And lot_sl = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, msite_code);
					pstmt.setString(2, mitem_code);
					pstmt.setString(3, mloc_code);
					pstmt.setString(4, mlot_no);
					pstmt.setString(5, mlot_sl);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_acct_code__cr = checkNullAndTrim(rs.getString("acct_code__inv"));
						ls_cctr_code__cr = checkNullAndTrim(rs.getString("cctr_code__inv"));
						ls_grade = checkNullAndTrim(rs.getString("grade"));
					}
					if(ls_cctr_code__cr == null || ls_cctr_code__cr.length() == 0)
					{
						ls_cctr_code__cr = finCommon.getAcctDetrTtype(mitem_code, mitem_ser, "COGS", ls_trantype, conn);
						if (ls_cctr_code__cr != null && ls_cctr_code__cr.trim().length() > 0) 
						{
							String tokens [] = ls_cctr_code__cr.split(",");

							//System.out.println("Length="+tokens.length);

							if ( tokens.length >= 2)
							{
								ls_acct_code__cr = tokens[0];
								ls_cctr_code__cr = tokens[1];

								ls_acct_code__cr = checkNullAndTrim(ls_acct_code__cr);
								ls_cctr_code__cr = checkNullAndTrim(ls_cctr_code__cr);
							}
							else
							{
								ls_acct_code__cr = ls_cctr_code__cr.substring(0,ls_cctr_code__cr.indexOf(","));
								ls_cctr_code__cr = ls_cctr_code__cr.substring(ls_cctr_code__cr.indexOf(",") + 1);
							}
							tokens = null;
						}
					}

					if(ls_acct_code__dr == null || ls_acct_code__dr.length() == 0)
					{
						ls_cctr_code__dr = finCommon.getAcctDetrTtype(mitem_code, mitem_ser, "", ls_trantype, conn);
						if (ls_cctr_code__dr != null && ls_cctr_code__dr.trim().length() > 0) 
						{
							String tokens [] = ls_cctr_code__dr.split(",");

							//System.out.println("Length="+tokens.length);

							if ( tokens.length >= 2)
							{
								ls_acct_code__dr = tokens[0];
								ls_cctr_code__dr = tokens[1];

								ls_acct_code__dr = checkNullAndTrim(ls_acct_code__dr);
								ls_cctr_code__dr = checkNullAndTrim(ls_cctr_code__dr);
							}
							else
							{
								ls_acct_code__dr = ls_cctr_code__dr.substring(0,ls_cctr_code__dr.indexOf(","));
								ls_cctr_code__dr = ls_cctr_code__dr.substring(ls_cctr_code__dr.indexOf(",") + 1);
							}
							tokens = null;
						}

					}
					if(ls_cctr_code__dr == null || ls_cctr_code__dr.trim().length() == 0)
					{
						ls_cctr_code__dr = ls_cctr_code__cr;
					}

					if("db2".equalsIgnoreCase(DB))
					{
						sql = "Select a.quantity From stock a, invstat b Where a.inv_stat = b.inv_stat And a.item_code = ? and a.site_code = ? And a.loc_code = ? And a.lot_no = ? And a.lot_sl = ? for update";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitem_code);
						pstmt.setString(2, msite_code);
						pstmt.setString(3, mloc_code);
						pstmt.setString(4, mlot_no);
						pstmt.setString(5, mlot_sl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							mtot_stk = rs.getDouble(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
					else if("mysql".equalsIgnoreCase(DB))
					{
						sql = "Select a.quantity From stock a, invstat b Where a.inv_stat = b.inv_stat And a.item_code = ? and a.site_code = ? And a.loc_code = ? And a.lot_no = ? And a.lot_sl = ? for update";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitem_code);
						pstmt.setString(2, msite_code);
						pstmt.setString(3, mloc_code);
						pstmt.setString(4, mlot_no);
						pstmt.setString(5, mlot_sl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							mtot_stk = rs.getDouble(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
					else if("mssql".equalsIgnoreCase(DB))
					{
						sql = "Select a.quantity From stock a (updlock), invstat b Where a.inv_stat = b.inv_stat And a.item_code = ? and a.site_code = ? And a.loc_code = ? And a.lot_no = ? And a.lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitem_code);
						pstmt.setString(2, msite_code);
						pstmt.setString(3, mloc_code);
						pstmt.setString(4, mlot_no);
						pstmt.setString(5, mlot_sl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							mtot_stk = rs.getDouble(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
					else
					{
						sql = "Select a.quantity From stock a, invstat b Where a.inv_stat = b.inv_stat And a.item_code = ? and a.site_code = ? And a.loc_code = ? And a.lot_no = ? And a.lot_sl = ? for update nowait ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitem_code);
						pstmt.setString(2, msite_code);
						pstmt.setString(3, mloc_code);
						pstmt.setString(4, mlot_no);
						pstmt.setString(5, mlot_sl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{										
							mtot_stk = rs.getDouble(1);
						}
						if (rs != null)
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}

					if(mtot_stk < mquantity)     
					{
						merrcode = "VTDIST4" + " ~t for site code : " + msite_code + " for item code : " + mitem_code + " for loc.code : " + mloc_code + " for lot no : " + mlot_no + " for lot sl.: " + mlot_sl;
						//errorString = vdt.getErrorString("",merrcode,"");
						errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

						return errorString;
					}

					stockUpd.put("tran_date",mtran_date) ;
					stockUpd.put("tran_ser","S-RET") ;
					stockUpd.put("tran_id",msalereturn);
					mline_no= "   "+mline_no;
					mline_no = mline_no.substring(mline_no.length()-3, mline_no.length());
					stockUpd.put("line_no",mline_no);
					stockUpd.put("sale_order",minvoice_id);
					stockUpd.put("item_code",mitem_code);
					stockUpd.put("site_code",ls_sitecodedlv); 
					stockUpd.put("loc_code",mloc_code);
					stockUpd.put("unit",munit);
					stockUpd.put("lot_no",mlot_no);
					stockUpd.put("lot_sl", mlot_sl);
					stockUpd.put("qty_stduom",mquantity);
					stockUpd.put("rate",lc_rate);
					stockUpd.put("exp_date",mexp_date);
					stockUpd.put("item_ser",mitem_ser);
					stockUpd.put("mfg_date",mmfg_date);
					stockUpd.put("site_code__mfg",msite_code_mfg);
					stockUpd.put("reas_code",mreas_code);
					stockUpd.put("tran_type","ID");
					stockUpd.put("grade",ls_grade);
					stockUpd.put("acct_code_inv",ls_acct_code__dr);
					stockUpd.put("cctr_code_inv",ls_cctr_code__dr);
					stockUpd.put("acct_code__cr",ls_acct_code__cr);
					stockUpd.put("acct_code__dr",ls_acct_code__dr);
					stockUpd.put("cctr_code__cr",ls_cctr_code__cr);
					stockUpd.put("cctr_code__dr",ls_cctr_code__dr);
					stockUpd.put("no_art",Double.parseDouble(lc_no_art));
					stockUpd.put("gross_weight",lc_gross_wt);
					stockUpd.put("tare_weight",lc_tare_wt);
					stockUpd.put("net_weight",lc_net_wt);

					merrcode = stockUpdate.updateStock(stockUpd,xtraParams, conn);
					//System.out.println("After stockUpdate3 ["+merrcode+"]");
					if(merrcode !=null && merrcode.trim().length() >0)  
					{
						//Commented by wasim as it is returning the full XML
						//errorString = vdt.getErrorString("",merrcode,"");
						//return errorString;
						return merrcode;
					}

					if("Y".equalsIgnoreCase(ls_channel_partner) && "E".equalsIgnoreCase(ls_dis_link))
					{
						stockUpd.put("inv_stat",ls_tran_stat);
						stockUpd.put("loc_code",ls_loc_code_git);
						stockUpd.put("tran_type","R");
						merrcode = stockUpdate.updateStock(stockUpd,xtraParams, conn);
						if(merrcode !=null && merrcode.trim().length() >0)  
						{
							//errorString = vdt.getErrorString("",merrcode,"");
							errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

							return errorString;
						}
					}
				} //End else part for Replaced (ID) by wasim on 19-MAY-2017

			}//End while loop for sales return Details
			//Added by wasim on 19-MAY-2017 to use while loop instead of if for multiple lines in sales return details [END]
			if(rsDetail!=null)
			{
				rsDetail.close();rsDetail = null;
			}
			if(pstmtDetail!=null)
			{
				pstmtDetail.close();pstmtDetail = null;
			}
			pstmtUpd.close();
			pstmtUpd = null;
			pstmtSorditem.close();
			pstmtSorditem = null;
			pstmtSorddet.close();
			pstmtSorddet = null;
			//Added by wasim on 19-MAY-2017 to use while loop instead of if for multiple lines in sales return details [END]
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 17-Mar-2018 manoharan in case of full return to reduse despatch quantity
			// 11-mar-2019 manoharan whether to open the order based on reason code
			//if ls_FULLRET_HDR = "Y" and ls_ret_opt = "C" then
			if (isOpenOrder)
			{
				sql = "update sorder set status = 'P' where sale_order = ?";

				pstmtUpd = conn.prepareStatement(sql);
				pstmtUpd.setString(1, saleOrder);
				pstmtUpd.executeUpdate();
				pstmtUpd.close();
				pstmtUpd= null;
			}
			// end 17-Mar-2018 manoharan in case of full return to reduse despatch quantity

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////			
			ls_quar_lockcode = distCommon.getDisparams("999999","QUARNTINE_LOCKCODE",conn) ;
			if("NULLFOUND".equalsIgnoreCase(ls_quar_lockcode))
			{
				ls_quar_lockcode = " ";
			}
			else if(ls_quar_lockcode != null && ls_quar_lockcode.trim().length() > 0)
			{
				sql = "Select count(*) From sreturndet Where tran_id = ? and qc_reqd = 'Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ll_count = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			if(ll_count > 0)
			{
				sql = "select a.site_code,b.item_code,b.loc_code,b.lot_no,b.lot_sl from sreturn a, sreturndet b where a.tran_id = ? and a.tran_id = b.tran_id and b.qc_reqd='Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				while(rs.next())
				{										
					msite_code = checkNullAndTrim(rs.getString(1));
					mitem_code = checkNullAndTrim(rs.getString(2));
					mloc_code = checkNullAndTrim(rs.getString(3));
					mlot_no = checkNull(rs.getString(4));
					mlot_sl = checkNull(rs.getString(5));

					tempList.add(msite_code);
					tempList.add(mitem_code);
					tempList.add(mloc_code);
					tempList.add(mlot_no);
					tempList.add(mlot_sl);
					System.out.println("tempList on confirm:::"+tempList);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				merrcode = invHoldGen.generateHoldTrans(ls_quar_lockcode, msalereturn, "S-RET", msite_code, tempList, xtraParams, conn);
				//System.out.println("After invHoldGen.generateHoldTrans ["+merrcode+"]");
				if (merrcode != null && merrcode.trim().length() > 0)
				{
					//errorString = vdt.getErrorString("",merrcode,"");
					errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;
				}
			}

			if("Y".equalsIgnoreCase(ls_channel_partner) && "E".equalsIgnoreCase(ls_dis_link))
			{
				sql = "Select count(*) From sreturndet Where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ll_sret_count = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				sql = "Select count(*) From invtrace Where ref_ser = 'S-RET' And ref_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ll_invtrace_count = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if(ll_sret_count != ll_invtrace_count)
				{
					merrcode = "VTMISMATCH";
					//errorString = vdt.getErrorString("",merrcode,"");
					errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;
				}
			}

			if((merrcode.trim().length() == 0 || merrcode == null) && !"B".equalsIgnoreCase(gs_run_mode))
			{
				sql = "select full_ret ,invoice_id, tran_type from sreturn where  tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					mfull_ret = checkNullAndTrim(rs.getString(1));
					minvoice_id = checkNullAndTrim(rs.getString(2));
					ls_tran_type = checkNullAndTrim(rs.getString(3));
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(minvoice_id != null && minvoice_id.length()>0)
				{
					sql = "Select count(*) From invoice_amendment where invoice_id = ? and confirmed  = 'Y' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,minvoice_id );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						ll_invamd_count = rs.getInt(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}

					if("IC".equalsIgnoreCase(ls_tran_type) && "Y".equalsIgnoreCase(mfull_ret) && ll_invamd_count == 0)
					{
						sql = "Update invoice Set doc_status = 'C' Where invoice_id	= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,minvoice_id );
						int rowcnt = pstmt.executeUpdate();
						if (pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
					}
				}
				if(gs_run_mode == null)
				{
					gs_run_mode = "I";
				}

				if((merrcode == null || merrcode.trim().length() == 0) && !"B".equalsIgnoreCase(gs_run_mode) )
				{
					sql = "Select emp_code From users Where code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,chgUser );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						memp_code = checkNullAndTrim(rs.getString("emp_code"));
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}

					sql = "Update sreturn Set confirmed ='Y', conf_date = ?, emp_code__aprv = ? Where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, currDate);
					pstmt.setString(2,memp_code );
					pstmt.setString(3,msalereturn );
					int rowcnt = pstmt.executeUpdate();
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				}
			}

			//Changed by wasim on 05-JUN-2017 for [START]
			if(detCnt != 0)
			{
				retString  =  invacct.postSalesReturn (msalereturn, xtraParams, "", conn);
			}
			//System.out.println("After InvAcct confirmSalesReturn ["+retString+"]");
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}
			//Changed by wasim on 05-JUN-2017 for [END]

			if(!"B".equalsIgnoreCase(gs_run_mode))
			{
				String ediOption = "";
				sql = "select edi_option from transetup where tran_window = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "w_salesreturn_retn");
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					ediOption = rs.getString("edi_option");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				//System.out.println("ls_channel_partner+["+ls_channel_partner+"]");
				//.out.println("ediOption+["+ediOption+"]");
				if("Y".equalsIgnoreCase(ls_channel_partner) && "E".equalsIgnoreCase(ls_dis_link))
				{
					if ("2".equals(ediOption)) 
					{
						//	CreateRCPXML createRCPXML = new CreateRCPXML("w_salesreturn", "tran_id");
						CreateRCPXML createRCPXML = new CreateRCPXML("w_salesreturn_retn", "tran_id");
						dataStr = createRCPXML.getTranXML(msalereturn, conn);
						//System.out.println("dataStr =[ " + dataStr + "]");
						Document ediDataDom = genericUtility.parseString(dataStr);

						E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
						//	retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_salesreturn", "2", xtraParams, conn);
						retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_salesreturn_retn", "2", xtraParams, conn);
						createRCPXML = null;
						e12CreateBatchLoad = null;

						if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
						{
							//System.out.println("retString from edi 2 batchload = [" + retString + "]");
						}
					}

					else 
					{
						//	CreateRCPXML createRCPXML = new CreateRCPXML("w_salesreturn","tran_id");
						CreateRCPXML createRCPXML = new CreateRCPXML("w_salesreturn_retn","tran_id");
						dataStr = createRCPXML.getTranXML(msalereturn, conn);
						//System.out.println("dataStr =[ " + dataStr + "]");
						Document ediDataDom = genericUtility.parseString(dataStr);

						E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
						//	retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_salesreturn", ediOption, xtraParams, conn);
						retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_salesreturn_retn", ediOption, xtraParams, conn);
						createRCPXML = null;
						e12CreateBatchLoad = null;

						if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
						{
							//System.out.println("retString from batchload = ["+ retString + "]");
						}
					}
					//system.out.println("retString from["+retString+"]");
				}//
				sql = "Select count(*) From sreturndet Where tran_id = ? and ret_rep_flag = 'P'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,msalereturn );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ll_count = rs.getInt(1);
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if(ll_count > 0)
				{
					merrcode = postordact.schemeHistoryUpd(msalereturn,msite_code,"R",conn) ;
					if (merrcode != null && merrcode.trim().length() > 0)
					{
						//errorString = vdt.getErrorString("",merrcode,"");
						errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

						return errorString;
					}
					ls_var_value = distCommon.getDisparams("999999","PUR_INTEGRATED",conn) ;

					if(("Y".equalsIgnoreCase(ls_channel_partner) && "Y".equalsIgnoreCase(ls_var_value)) || Integer.parseInt(ls_edioption) > 0)
					{
						if("E".equalsIgnoreCase(ls_dis_link))
						{
							if ("2".equals(ls_edioption)) 
							{
								CreateRCPXML createRCPXML = new CreateRCPXML("w_salesrcp", "tran_id");
								dataStr = createRCPXML.getTranXML(msalereturn, conn);
								//System.out.println("dataStr =[ " + dataStr + "]");
								Document ediDataDom = genericUtility.parseString(dataStr);

								E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
								retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_salesrcp", "2", xtraParams, conn);
								createRCPXML = null;
								e12CreateBatchLoad = null;

								if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
								{
									System.out.println("retString from edi 2 batchload = [" + retString + "]");
								}
							}

							else 
							{
								CreateRCPXML createRCPXML = new CreateRCPXML("w_salesrcp","tran_id");
								dataStr = createRCPXML.getTranXML(msalereturn, conn);
								//System.out.println("dataStr =[ " + dataStr + "]");
								Document ediDataDom = genericUtility.parseString(dataStr);

								E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
								retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_salesrcp", ls_edioption, xtraParams, conn);
								createRCPXML = null;
								e12CreateBatchLoad = null;

								if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
								{
									System.out.println("retString from batchload = ["+ retString + "]");
								}
							}
						}
						/*else if("A".equalsIgnoreCase(ls_dis_link) || "S".equalsIgnoreCase(ls_dis_link) || "C".equalsIgnoreCase(ls_dis_link) )
						{
							merrcode = nvo_porcp.gbf_create_sreturn_porcp(ls_hdr,ls_det,sqlca_cp,ls_dis_link);
						}*/
						else
						{
							merrcode = "VTSRET24";
							//errorString = vdt.getErrorString("",merrcode,"");
							errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

							return errorString;	
						}
					}
				}
			}
			sql = "select invoice_id, tran_type, site_code, tran_date, cust_code, ret_opt From sreturn Where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_invoice_id = checkNullAndTrim(rs.getString("invoice_id"));
				ls_tran_type = checkNullAndTrim(rs.getString("tran_type"));
				ls_site_code = checkNullAndTrim(rs.getString("site_code"));
				ld_tran_date = rs.getDate("tran_date");
				ls_cust_code = checkNullAndTrim(rs.getString("cust_code"));
				ls_ret_opt = checkNullAndTrim(rs.getString("ret_opt"));
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			if(!"R".equalsIgnoreCase(ls_ret_opt) && !"B".equalsIgnoreCase(gs_run_mode))
			{
				ls_crdt = finCommon.getFinparams("999999", "SRET_CRN_DATE", conn);
				String ls_crdtArray[]=ls_crdt.split(";");
				ls_crdt = ls_crdtArray[0];
				if(ls_crdt == null || ls_crdt.trim().length() == 0)
				{
					ls_crdt = " ";
				}
				if("NULLFOUND".equalsIgnoreCase(ls_crdt))
				{
					merrcode = "VTENVAR1";
					//errorString = vdt.getErrorString("",merrcode,"");
					errorString=itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;	
				}
				else if("S".equalsIgnoreCase(ls_crdt))
				{
					ld_today = new Date(currDate.getTime());
				}
				else if("T".equalsIgnoreCase(ls_crdt))
				{
					ld_today = ld_tran_date;
				}
				ls_var_value = distCommon.getDisparams("999999","CRN_ON_SRETURN",conn) ;

				System.out.println("CRN_ON_SRETURN["+ls_var_value+"]");

				if(ls_var_value == null || ls_var_value.trim().length() == 0)
				{
					ls_var_value = " ";
				}
				else if(!"N".equalsIgnoreCase(ls_var_value.trim()))
				{
					// 05-sep-2019 manoharan to create misc or normal credit note as per detail invoice_id	
					//without invoice no
					sql = " select count(*) from 	sreturn a , sreturndet b " 
							+ " where a.tran_id = b.tran_id "
							+ " and a.tran_id = ? " //:
							+ " and a.ret_opt = 'C' "
							+ " and b.invoice_id is null "
							+ " and a.tran_id__crn is null ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,msalereturn );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						mcount1 = rs.getInt(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}

					//added by manish mhatre on 31-aug-2020[For getting count from sales return detail]
					//start manish
					sql="Select count(*) from sreturndet where tran_id= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,msalereturn );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						detailCnt = rs.getInt(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					// end manish
					// 10-sep-2019 manoharan to check amount of credit note and sales return
					sql = " select sum(net_amt)  from sreturndet where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,msalereturn );
					rs = pstmt.executeQuery();
					if(rs.next())
					{										
						lc_sramt = rs.getDouble(1);
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					// end 10-sep-2019 manoharan to check amount of credite note and sales return
					//if(ls_invoice_id == null || ls_invoice_id.trim().length() == 0)
					if(detailCnt>0)   // added by manish mhatre on 31-aug-2020[if condition added for when getting count=0 from sreturn detail then do not create crn/misc crn]
					{            
						if (mcount1 > 0)
						{
							merrcode = gbf_auto_crnote_sreturn_misc(currDate, ls_tran_type, ls_site_code, msalereturn, msalereturn, ld_tran_date, ld_tran_date, ls_cust_code,ls_cust_code, "Y", xtraParams, conn);
							//merrcode = gbf_auto_crnote_sreturn_misc(ld_today, ls_tran_type, ls_site_code,as_tran_id, as_tran_id, ld_tran_date, ld_tran_date, ls_cust_code, ls_cust_code, 'Y');
							// 10-sep-2019 manoharan to check amount of credite note and sales return
							if (merrcode == null || merrcode.trim().length() == 0 )
							{
								sql = " select tran_id  from misc_drcr_rcp where sreturn_no = ? and site_code= ? ";   //site code added by manish on 12-feb-2019
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,msalereturn );
								pstmt.setString(2, ls_site_code);   //added by manish mhatre on 12-feb-2019
								rs = pstmt.executeQuery();
								if(rs.next())
								{										
									ls_tranidcrn = checkNullAndTrim(rs.getString("tran_id"));
								}
								if (rs != null)
								{
									rs.close();
									rs = null;
								}
								if (pstmt != null)
								{
									pstmt.close();
									pstmt = null;
								}


								if (ls_tranidcrn == null  || ls_tranidcrn.trim().length() == 0 )
								{
									//errorString = vdt.getErrorString("","VTNOCRN","");
									errorString=itmDBAccessLocal.getErrorString("","VTNOCRN","","",conn);

									return errorString;
								}
								else
								{
                                    // 18-sep-2020   manoharan to consider all amount as there is a tax mismatch
									sql = " select sum(amount) from misc_drcr_rdet where tran_id = ?  ";//and net_amt is not null";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,ls_tranidcrn );
									rs = pstmt.executeQuery();
									if(rs.next())
									{										
										lc_crnamt = rs.getDouble(1);
									}
									if (rs != null)
									{
										rs.close();
										rs = null;
									}
									if (pstmt != null)
									{
										pstmt.close();
										pstmt = null;
                                    }
                                    System.out.println("Amount mismatch misc CRN VTSRCRAMT lc_crnamt [ " + lc_crnamt + "] lc_sramt [" + lc_sramt +"]");
									if (Math.abs(lc_crnamt - lc_sramt) > 1 )
									{
										//errorString = vdt.getErrorString("","VTSRCRAMT","");
										// 17-dec-2020 Manoharan to show amount mismatch lines 
										//errorString =itmDBAccessLocal.getErrorString("","VTSRCRAMT","","",conn);
										errorString = getAmtMismatchLines(ls_tranidcrn,msalereturn, "M","VTSRCRAMT",  conn);
										// end 17-dec-2020 Manoharan to show amount mismatch lines 
										isError = true;
										merrcode = "VTSRCRAMT";
										return errorString;
									}
								}
								// end 10-sep-2019 manoharan to check amount of credite note and sales return
							}
							/*else
						{
							merrcode = gbf_auto_crnote_sreturn(currDate, ls_tran_type, ls_site_code, msalereturn, msalereturn, ld_tran_date, ld_tran_date, ls_cust_code,ls_cust_code, "Y", xtraParams, conn);
							// 10-sep-2019 manoharan to check amount of credite note and sales return
							if (merrcode == null || merrcode.trim().length() == 0 )
							{
								sql = " select tran_id  from drcr_rcp where sreturn_no = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,msalereturn );
								rs = pstmt.executeQuery();
								if(rs.next())
								{										
									ls_tranidcrn = checkNullAndTrim(rs.getString("tran_id"));
								}
								if (rs != null)
								{
									rs.close();
									rs = null;
								}
								if (pstmt != null)
								{
									pstmt.close();
									pstmt = null;
								}


								if (ls_tranidcrn == null  || ls_tranidcrn.trim().length() == 0 )
								{
									errorString = vdt.getErrorString("","VTNOCRN","");
									return errorString;
								}
								else
								{
									sql = " select sum(net_amt) from drcr_rdet where tran_id = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,ls_tranidcrn );
									rs = pstmt.executeQuery();
									if(rs.next())
									{										
										lc_crnamt = rs.getDouble(1);
									}
									if (rs != null)
									{
										rs.close();
										rs = null;
									}
									if (pstmt != null)
									{
										pstmt.close();
										pstmt = null;
									}
									if (Math.abs(lc_crnamt - lc_sramt) > 1 )
									{
										errorString = vdt.getErrorString("","VTSRCRAMT","");
										return errorString;
									}
								}
							// end 10-sep-2019 manoharan to check amount of credit note and sales return
							}
						}//commented by monika 23 dec 19 to get vALUE FROM CRDRRCP WHERE INVOICE IS NULL
							 */
						}//TO ADD THE ELSE PART HERE WHERE INVOICE IS NOT NULL

						else
						{
							merrcode = gbf_auto_crnote_sreturn(currDate, ls_tran_type, ls_site_code, msalereturn, msalereturn, ld_tran_date, ld_tran_date, ls_cust_code,ls_cust_code, "Y", xtraParams, conn);
							// 10-sep-2019 manoharan to check amount of credite note and sales return
							//System.out.println("value of  merrcode"+merrcode);
							if (merrcode == null || merrcode.trim().length() == 0 )
							{
								//System.out.println("inside merrcode");
								sql = " select tran_id  from drcr_rcp where sreturn_no = ? and site_code = ? ";    //site code added by manish mhatre on 12-feb-2019 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,msalereturn );
								pstmt.setString(2, ls_site_code);   //added by manish mhatre on 12-feb-2019
								rs = pstmt.executeQuery();
								if(rs.next())
								{										
									ls_tranidcrn = checkNullAndTrim(rs.getString("tran_id"));
								}
								if (rs != null)
								{
									rs.close();
									rs = null;
								}
								if (pstmt != null)
								{
									pstmt.close();
									pstmt = null;
								}


								if (ls_tranidcrn == null  || ls_tranidcrn.trim().length() == 0 )
								{
									//errorString = vdt.getErrorString("","VTNOCRN","");
									errorString =itmDBAccessLocal.getErrorString("","VTRCPT8","","",conn);
									return errorString;
								}
								else
								{

									//System.out.println("inside merrcode");
									sql = " select sum(net_amt) from drcr_rdet where tran_id = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,ls_tranidcrn );
									rs = pstmt.executeQuery();
									if(rs.next())
									{										
										lc_crnamt = rs.getDouble(1);
									}
									if (rs != null)
									{
										rs.close();
										rs = null;
									}
									if (pstmt != null)
									{
										pstmt.close();
										pstmt = null;
									}

                                    //System.out.println("sum(net_amt)"+lc_crnamt+"  sr"+lc_sramt);
                                    System.out.println("Amount mismatch CRN VTSRCRAMT lc_crnamt [ " + lc_crnamt + "] lc_sramt [" + lc_sramt +"]");
									if (Math.abs(lc_crnamt - lc_sramt) > 1 )
									{
										//errorString = vdt.getErrorString("","VTSRCRAMT","");
										// 17-dec-2020 Manoharan to show amount mismatch lines 
										//errorString =itmDBAccessLocal.getErrorString("","VTSRCRAMT","","",conn);
										errorString = getAmtMismatchLines(ls_tranidcrn,msalereturn, "C","VTSRCRAMT",  conn);
										// end 17-dec-2020 Manoharan to show amount mismatch lines
										isError = true;
										merrcode =  "VTSRCRAMT";
										return errorString;
									}
								}
								// end 10-sep-2019 manoharan to check amount of credit note and sales return
							}
						}//commented by monika 23 dec 19 to get vALUE FROM CRDRRCP
					}  //end of detCnt [added by manish mhatre on 31-aug-2020]
				}

				if(merrcode != null && merrcode.trim().length() >  0)
				{
					if(merrcode.indexOf("<?xml version") == -1)
					{
						//errorString = vdt.getErrorString("",merrcode,"");
						errorString =itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

						return errorString;
					}
					else
					{
						return merrcode;
					}
				}
			}
			if((ls_invoice_id != null && ls_invoice_id.trim().length() > 0) && "C".equalsIgnoreCase(ls_ret_opt))
			{
				ls_comm_calc_on_off =  finCommon.getFinparams("999999", "COMM_CALC_ON_OFF", conn);
			}

			if("Y".equalsIgnoreCase(ls_comm_calc_on_off))
			{
				CalculateCommission calCom = new CalculateCommission();
				ls_conf_drcr_opt =  finCommon.getFinparams("999999", "COMM_DRCR_CONF", conn);
				ls_conf_jv_opt =  finCommon.getFinparams("999999", "COMM_JV_CONF", conn);
				merrcode =  calCom.CalCommission(ls_invoice_id,"SR",msalereturn,ls_conf_drcr_opt,ls_conf_jv_opt,xtraParams, conn);
				if(merrcode != null && merrcode.trim().length() > 0)
				{
					//errorString = vdt.getErrorString("",merrcode,"");
					errorString =itmDBAccessLocal.getErrorString("",merrcode,"","",conn);

					return errorString;
				}
			}

			//Added  by Arun p 31-10-17 for generate edi outbond data when edi medium=4 and edi medium=1 -Start
			String ediOption = "";
			sql = "select edi_option from transetup where tran_window = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "w_salesreturn_retn");
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				ediOption = rs.getString("edi_option");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//System.out.println("<-----------Edi Code calling ------>");
			//System.out.println("PS3 merrcode[+"+merrcode+"]");
			//System.out.println("PS ls_channel_partner[+"+ls_channel_partner+"]");
			//System.out.println("PS ediOption[+"+ediOption+"]");
			if((!("Y".equalsIgnoreCase(ls_channel_partner)) && "1".equals(ediOption)) && ((merrcode == null || merrcode.trim().length() == 0)))
			{
				//System.out.println("merrcode[+"+merrcode+"]");
				CreateRCPXML createRCPXML = new CreateRCPXML("w_salesreturn_retn", "tran_id");
				dataStr = createRCPXML.getTranXML(msalereturn, conn);
				//System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);
				//System.out.println("xtraParams:["+xtraParams+"]");
				E12GenerateEDIEJB e12GenerateEDIEJB = new E12GenerateEDIEJB();
				retString = e12GenerateEDIEJB.nfCreateEdiMultiLogic(ediDataDom,"w_salesreturn_retn", xtraParams);
				//stem.out.println("retString from E12GenerateEDIEJB before = ["+ retString + "]");
				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					//System.out.println("retString from E12GenerateEDIEJB = ["+ retString + "]");
				}
			}
			//Added  by Arun p 31-10-17 for generate edi outbond data when edi medium=4 and edi medium=1 -end
			//System.out.println("<-----------Edi Code ending  ------>");

		}
		catch(Exception e)
		{	
			System.out.println("Expception inside confirmSalesReturn-->["+e.getMessage()+"]");
			isError = true;
			e.printStackTrace();
			errMessage = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//Changed by wasim on 15-MAY-17 for returning the success error string if merrcode and errorString is blank [START]
				System.out.println("Inside Finaly confirmSalesReturn["+isError+"] merrcode["+merrcode+"] errorString["+errorString+"]");
				if(!isError)
				{
					if((errorString == null || errorString.trim().length() == 0) && (merrcode == null || merrcode.trim().length() == 0))
					{
						errorString = itmDBAccessLocal.getErrorString("", "VTSRTRNCMP", "", "", conn);
					}
				}
				else if(isError)
				{
					// 17-dec-2020 manoharan same error to be displayed in case already errorstring populated
					if((errorString == null || errorString.trim().length() == 0) )
					{
						errorString = getError(errMessage , "VTSRTRNFLR" , conn);
					}
					return errorString ;
				}
				//Changed by wasim on 15-MAY-17 for returning the success error string  if merrcode and errorString and blank [END]
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
				if(rsDetail!=null)
				{
					rsDetail.close();rsDetail = null;
				}
				if(pstmtDetail!=null)
				{
					pstmtDetail.close();pstmtDetail = null;
				}
				if (pstmtUpd != null )
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
				if (pstmtSorditem != null )
				{
					pstmtSorditem.close();
					pstmtSorditem = null;
				}
				if (pstmtSorddet != null )
				{
					pstmtSorddet.close();
					pstmtSorddet = null;
				}
				if (pstmtUpdFree != null )
				{
					pstmtUpdFree.close();
					pstmtUpdFree = null;
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
		System.out.println("Returning from confirmSalesReturn ["+errorString+"]");
		return errorString;
	}
	// 17-dec-2020 Manoharan to show line wise mismatch
	private String getAmtMismatchLines(String crnTranId,String srTranId, String crnType,String errCode, Connection conn)  throws ITMException
	{
		String errMsg = "",errString = "",sql = "",sqlCRN = "";
		double srNetAmt = 0.00, crnNetAmt = 0.00,srTaxAmt = 0, crnTaxAmt = 0;
		int srLineNo =0, crnLineNo =0; 
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null, rs1 = null ;
		//UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			if ("M".contentEquals(crnType))
			{
				sqlCRN = "select line_no,net_amt,tax_amt from misc_drcr_rdet where tran_id = ? and line_no__sret = ?";
			}
			else
			{
				sqlCRN = "select line_no,net_amt,tax_amt from drcr_rdet where tran_id = ? and line_no__sret = ?";
			}
			pstmt1 = conn.prepareStatement(sqlCRN);
			System.out.println("Preparedstatment for CRN done");
			
			sql = "select line_no,net_amt,tax_amt from sreturndet where tran_id = ? order by line_no";
			pstmt = conn.prepareStatement(sql);
			System.out.println("Preparedstatment for SR done");
			pstmt.setString(1, srTranId);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				srLineNo = rs.getInt("line_no");
				srNetAmt = rs.getDouble("net_amt");
				srTaxAmt = rs.getDouble("tax_amt");

				pstmt1.setString(1, crnTranId);
				pstmt1.setInt(2, srLineNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					crnLineNo = rs1.getInt("line_no");
					crnNetAmt = rs1.getDouble("net_amt");
					crnTaxAmt = rs1.getDouble("tax_amt");
					
				}
				rs1.close();
				rs1 = null;
				pstmt1.clearParameters();
				srNetAmt = Double.parseDouble(UtilMethods.getInstance().getReqDecString(srNetAmt, 3));
				crnNetAmt = Double.parseDouble(UtilMethods.getInstance().getReqDecString(crnNetAmt, 3));
				if(srNetAmt != crnNetAmt)
				{
					if (errMsg != null && errMsg.trim().length() > 0)
					{
						errMsg = errMsg + ", ";
					}

					errMsg = errMsg + "SR line [" + srLineNo + "] tax [" + srTaxAmt + "] Net [" + srNetAmt +"] CRN tax [" + crnTaxAmt + "] Net [" + crnNetAmt + "]"; 
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			pstmt1.close();
			pstmt1 = null;
			
			//errString = getMsg(errMsg,errCode, conn);
			errString =  finCommon.getErrorXML(errMsg, "Amount mismatch between sales return and credit note", errCode,  "Amount mismatch between sales return and credit note");
		}
		catch(Exception e)
		{	
			System.out.println("Expception inside confirmSalesReturn-->["+e.getMessage()+"]");
			e.printStackTrace();
			errMsg = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
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
				if (pstmt1 != null )
				{
					pstmt1.close();
					pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();
					rs1 = null;
				}
			}
			catch(Exception e)
			{
				throw new ITMException(e);
			}
		}
		return errString;
	}
		
	private  String getMsg(String trace, String Code, Connection conn) throws ITMException, Exception
	{
		String mainStr ="";
		try
		{
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			String errString = "";
			errString =  itmDBAccessEJB.getErrorString("",Code,"","",conn);
			String begPart = errString.substring(0,errString.indexOf("<message>")+9);
			String begDesc = errString.substring(0,errString.indexOf("<description>")+13);
			String endDesc = errString.substring(errString.indexOf("</description>"));
			mainStr= begPart + trace + " </message><description>";
			mainStr= mainStr+endDesc;
			begPart = null;
			itmDBAccessEJB = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);

		}
		return mainStr;
	}
	

	// end 17-dec-2020 Manoharan to show line wise mismatch
	
	public String gbf_create_sretqc(String msalereturn, String xtraParams, Connection conn) throws ITMException
	{
		String	sql = "",chgUser = "", chgTerm = "",errorString = "", ls_errcode = "", ls_itemcode = "", ls_unit = "", ls_loccode = "", ls_lotno = "", ls_lotsl = "", merrcode = "", ls_sitecode = "", ls_sretinvqty = "", ls_supp_code__mfg = "", ls_supp_code = "";
		Date ldt_expdate = null, ldt_mfgdate = null;
		double	lc_quantity = 0, lc_sample_qty = 0, lc_physicalqty = 0, lc_gross_wt = 0, lc_net_wt = 0;
		String ll_lineno = "";
		int ll_ctr = 0, ll_count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//ValidatorEJB vdt=new ValidatorEJB();
		DistCommon distCommon = new DistCommon();
		//FinCommon finCommon = new FinCommon();
		Timestamp currDate = null;
		HashMap lstr_qc = new HashMap();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		/*s_qcorder lstr_qc
		nvo_business_object_create_qord lnvo_qc

		if itm_structure.dis_comp = 0 then 
			lnvo_qc = create nvo_business_object_create_qord
		else
			itm_app_server[itm_structure.dis_comp].createinstance(lnvo_qc)
		end if*/

		try
		{
			//System.out.println("------- Inside gbf_create_sretqc ----------------");
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			sql = "select site_code from sreturn where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_sitecode = checkNullAndTrim(rs.getString("site_code"));
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
			ls_sretinvqty = distCommon.getDisparams("999999","SRET_INV_QTY",conn) ;
			//System.out.println("ls_sretinvqty--------->>"+ls_sretinvqty);

			sql = "select item_code, sum(quantity__stduom) ,count(1), max(lot_sl), max(unit__std), loc_code, lot_no, min(exp_date), Min(line_no), min(mfg_date), sum(physical_qty) " +
					" from sreturndet where tran_id = ? and	qc_reqd = 'Y' Group by loc_code, item_code, lot_no Order by loc_code, item_code, lot_no";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,msalereturn );
			rs = pstmt.executeQuery();
			while(rs.next())
			{										
				ls_itemcode = checkNullAndTrim(rs.getString(1));
				lc_quantity = rs.getDouble(2);
				ll_ctr = rs.getInt(3);
				ls_lotsl = checkNullAndTrim(rs.getString(4));
				ls_unit = checkNullAndTrim(rs.getString(5));
				ls_loccode = checkNullAndTrim(rs.getString(6));
				ls_lotno = checkNullAndTrim(rs.getString(7));
				ldt_expdate = rs.getDate(8);
				ll_lineno = checkNullAndTrim(rs.getString(9));
				ldt_mfgdate = rs.getDate(10);
				lc_physicalqty = rs.getDouble(11);
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

			//System.out.println("ll_ctr--------->>"+ll_ctr);
			if(ll_ctr > 1)
			{
				//SetNull(ls_lotsl)
				ls_lotsl = " ";
			}

			sql = "select QTY_SAMPLE from siteitem where site_code = ? and item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_sitecode );
			pstmt.setString(2,ls_itemcode );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				lc_sample_qty = rs.getDouble("QTY_SAMPLE");
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
			//System.out.println("lc_sample_qty--------->>"+lc_sample_qty);

			if(lc_sample_qty == 0)
			{
				sql = "select QTY_SAMPLE from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ls_itemcode );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					lc_sample_qty = rs.getDouble("QTY_SAMPLE");
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
				//System.out.println("lc_sample_qty 111111 --------->>"+lc_sample_qty);

			}
			sql = "select count(*) from item_lot_info where item_code = ? and lot_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_itemcode );
			pstmt.setString(2,ls_lotno );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ll_count = rs.getInt(1);;
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
			//System.out.println("ll_count 111111 --------->>"+ll_count);

			if (ll_count > 0)
			{
				sql = "select supp_code__mfg , supp_code, exp_date, mfg_date from item_lot_info where item_code = ? and lot_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ls_itemcode );
				pstmt.setString(2,ls_lotno );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ls_supp_code__mfg = checkNullAndTrim(rs.getString("supp_code__mfg"));
					ls_supp_code = checkNullAndTrim(rs.getString("supp_code"));
					ldt_expdate = rs.getDate("exp_date");
					ldt_mfgdate = rs.getDate("mfg_date");
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
			//System.out.println("ll_count 22222222 --------->>"+ll_count);
			//System.out.println("ls_supp_code__mfg 22222222 --------->>"+ls_supp_code__mfg);
			if (ll_count == 0 || ls_supp_code__mfg == null)
			{
				sql = "select exp_date, mfg_date, supp_code__mfg from stock where item_code = ? and site_code = ? and lot_no = ? " +
						"and exp_date is not null ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ls_itemcode );
				pstmt.setString(2,ls_sitecode );
				pstmt.setString(3,ls_lotno );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ldt_expdate = rs.getDate(1);
					ldt_mfgdate = rs.getDate(2);
					ls_supp_code__mfg = checkNullAndTrim(rs.getString(3));
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
				//System.out.println(" 22222222 --------->>");
			}

			lstr_qc.put("supp_code", ls_supp_code);	
			lstr_qc.put("supp_code__mfg", ls_supp_code__mfg);	
			lstr_qc.put("qorder_type", "I");	
			lstr_qc.put("qorder_date", currDate);	
			lstr_qc.put("site_code", ls_sitecode);	
			lstr_qc.put("item_code", ls_itemcode);	

			if(lc_physicalqty == 0 )
			{
				lc_physicalqty = lc_quantity;
			}
			if ("P".equalsIgnoreCase(ls_sretinvqty))
			{
				lstr_qc.put("quantity", lc_physicalqty);
			}
			else
			{
				lstr_qc.put("quantity", lc_quantity);
			}
			lstr_qc.put("lot_no", ls_lotno);
			lstr_qc.put("lot_sl", ls_lotsl);
			lstr_qc.put("loc_code", ls_loccode);
			lstr_qc.put("tran_id", msalereturn);

			ll_lineno= "   "+ll_lineno;
			ll_lineno = ll_lineno.substring(ll_lineno.length()-3, ll_lineno.length());

			String expdate = (ldt_expdate == null) ? null : sdf.format(ldt_expdate);
			String mfgdate = (ldt_mfgdate == null) ? null : sdf.format(ldt_mfgdate);

			lstr_qc.put("line_no", ll_lineno);
			lstr_qc.put("unit", ls_unit);
			lstr_qc.put("batch_no", ls_lotno);
			lstr_qc.put("qc_create_type", "A");
			/*lstr_qc.put("expiry_date", expdate);
			lstr_qc.put("mfg_date", mfgdate);*/
			lstr_qc.put("expiry_date", ldt_expdate);
			lstr_qc.put("mfg_date", ldt_mfgdate);

			lstr_qc.put("qty_sample", lc_sample_qty);
			lstr_qc.put("route_code", null);

			ls_errcode = createQc(lstr_qc, xtraParams, conn) ;
			if (ls_errcode != null && ls_errcode.trim().length() > 0) 
			{
				//errorString = vdt.getErrorString("",ls_errcode,"");
				errorString =itmDBAccessLocal.getErrorString("",ls_errcode,"","",conn);
				return errorString;
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
		return errorString;
	}
	public String createQc(HashMap qcOrd,String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String ls_errcode = "", ls_key = "", ls_win = "", ls_qcno = "", ls_genlotauto = "", ls_supp_code = "",ls_gene_lot_no = "";
		String tranSer = "",keyCol = "",userId = "",chgTerm = "",chgUser = "",sql = "";
		Date ldt_duedate = null;
		String ls_aprv = "", ls_rej = "", ls_empcode = "", ls_item_ser = "", ls_lotno = "", ls_supp_code__mfg = "", ls_procmth ="";
		String lc_qccycletime = "", lc_qcleadtime = "", lc_qcleadtime_item = "", lc_qcleadtime_siteitem = "", lc_qtysample = "";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//ValidatorEJB vdt=new ValidatorEJB();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		Timestamp currDate = null;
		UtilMethods utilMethods = UtilMethods.getInstance();
		java.sql.Date startDate = null, dueDate = null, retestDate = null,qcOrderDate = null;
		try
		{
			//System.out.println("----------- Inside createQc -------------");
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
				lc_qccycletime = checkNullAndTrim(rs.getString(1));
				lc_qcleadtime_item = checkNullAndTrim(rs.getString(2));
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
				lc_qcleadtime_siteitem = checkNullAndTrim(rs.getString(1));
				ls_item_ser = checkNullAndTrim(rs.getString(2));
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

			if(ls_item_ser == null || ls_item_ser.trim().length() == 0 )
			{
				sql = "select item_ser from item where item_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,qcOrd.get("item_code").toString() );
				rs = pstmt.executeQuery();
				if(rs.next())
				{										
					ls_item_ser = checkNullAndTrim(rs.getString(1));
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

			if(lc_qccycletime == null || lc_qccycletime.length() == 0)
			{
				lc_qccycletime = "0";
			}

			if(lc_qcleadtime_siteitem == null || lc_qcleadtime_siteitem.length() == 0)
			{
				lc_qcleadtime = lc_qcleadtime_item;
			}
			else
			{
				lc_qcleadtime = lc_qcleadtime_siteitem;
			}

			if(lc_qcleadtime == null || lc_qcleadtime.length() == 0)
			{
				lc_qcleadtime = "0"; 
			}

			ldt_duedate =  utilMethods.RelativeDate(new Date(currDate.getTime()),(Integer.parseInt(lc_qccycletime) + Integer.parseInt(lc_qcleadtime)));

			qcOrd.put("qorder_date", currDateStr);
			qcOrd.put("start_date", currDateStr);
			qcOrd.put("due_date", sdfAppl.format(ldt_duedate));

			sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"w_qcorder_new" );
			rs = pstmt.executeQuery();
			if(rs.next())
			{										
				ls_key = checkNullAndTrim(rs.getString(1));
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

			if(ls_key == null || ls_key.length() == 0)
			{
				sql = " select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_key  = rs.getString("key_string");
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

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			String XMLString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>"+
					"\r\n</header><Detail1><test></test><site_code>"+qcOrd.get("site_code").toString()+"</site_code><qorder_type>"+qcOrd.get("qorder_type").toString()+"</qorder_type>"+
					" <qorder_date>"+currDateStr+"</qorder_date><lot_no>"+qcOrd.get("lot_no").toString()+"</lot_no><item_ser>"+ls_item_ser+"</item_ser></Detail1></Root>";
			TransIDGenerator tg = new TransIDGenerator(XMLString, userId, "");

			ls_qcno = tg.generateTranSeqID(tranSer, keyCol, ls_key, conn);
			if ("ERROR".equals(ls_qcno))
			{
				ls_errcode = new ITMDBAccessEJB().getErrorString("", "VTTRANID", "","",conn);
			}

			sql = "select loc_code__aprv,loc_code__rej,PROC_MTH,  QTY_SAMPLE  from siteitem where site_code = ? and item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,qcOrd.get("site_code").toString() );
			pstmt.setString(2,qcOrd.get("item_code").toString() );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_aprv = checkNullAndTrim(rs.getString(1));
				ls_rej = checkNullAndTrim(rs.getString(2));
				ls_procmth = checkNullAndTrim(rs.getString(3));
				lc_qtysample = checkNullAndTrim(rs.getString(4));
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

			if(ls_procmth == null || ls_procmth.length() == 0)
			{
				if(lc_qtysample == null || lc_qtysample.length() == 0)
				{
					lc_qtysample = "0";
				}
				else if(lc_qtysample != null || lc_qtysample.length() > 0)
				{
					qcOrd.put("qty_sample", lc_qtysample);
				}
			}
			else
			{
				qcOrd.put("qty_sample", "0");
			}

			if(qcOrd.get("lot_no").toString() == null || qcOrd.get("lot_no").toString().length() == 0)
			{
				qcOrd.put("lot_no", ls_qcno);
				/*choose case upper(trim(astr_qcorder.transer))
				case 'W-RCP'*/
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
				ls_gene_lot_no = checkNull(rs.getString(1));
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

			if(ls_gene_lot_no == null || ls_gene_lot_no.length() == 0)
			{
				ls_gene_lot_no = "1";
			}
			ls_genlotauto = distCommon.getDisparams("999999","GENERATE_LOT_NO_AUTO",conn) ;

			if("D-RCP".equalsIgnoreCase(tranSer))
			{
				if(("Y".equalsIgnoreCase(ls_genlotauto) || "M".equalsIgnoreCase(ls_genlotauto)) && "1".equalsIgnoreCase(ls_gene_lot_no))
				{
					ls_lotno = qcOrd.get("lot_no").toString();
					qcOrd.put("lot_no",ls_qcno);
				}
			}
			sql = "select emp_code from users where code = ?"; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,userId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_empcode = checkNullAndTrim(rs.getString(1));
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
			qcOrd.put("qcorder_no", ls_qcno);

			double qtyPass = Double.parseDouble(qcOrd.get("quantity").toString())-Double.parseDouble(qcOrd.get("qty_sample").toString());

			if(qcOrd.get("due_date") != null)
			{
				dueDate = java.sql.Date.valueOf(genericUtility.getValidDateString(qcOrd.get("due_date").toString(), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}
			//System.out.println("---------- dueDate -----------------"+dueDate);

			//Changed by wasim to convert for getting proper date formate [START]
			if(qcOrd.get("qorder_date") != null)
			{
				qcOrderDate = java.sql.Date.valueOf(genericUtility.getValidDateString(qcOrd.get("qorder_date").toString(), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}
			//Changed by wasim to convert for getting proper date formate [END]


			/*if(qcOrd.get("retest_date") != null)
			{
				retestDate = java.sql.Date.valueOf(genericUtility.getValidDateString(qcOrd.get("retest_date").toString(), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}
			System.out.println("---------- retestDate -----------------"+retestDate);*/

			sql = "insert into qc_order (qorder_no,qorder_type,qorder_date,site_code,item_code,route_code,quantity,qty_passed,qty_rejected,start_date,due_date,rel_date,porcp_no,porcp_line_no," +
					" lot_no,lot_sl,chg_date,chg_user,chg_term,loc_code,qty_sample,status, unit,qc_create_type,unit__sample,loc_code__aprv,loc_code__rej,lot_no__new, batch_no,expiry_date, proj_code," +
					" emp_code,item_code__new,mfg_date,spec_ref,supp_code,supp_code__mfg,retest_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_qcno);	
			pstmt.setString(2,qcOrd.get("qorder_type").toString());	
			//pstmt.setString(3,qcOrd.get("qorder_date").toString());//Changed by wasim on 15-MAY-17
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
			pstmt.setString(26,ls_aprv);
			pstmt.setString(27,ls_rej);
			pstmt.setString(28,qcOrd.get("lot_no").toString());
			pstmt.setString(29,qcOrd.get("batch_no").toString());
			pstmt.setDate(30, (java.sql.Date) qcOrd.get("expiry_date"));
			pstmt.setString(31,null);

			pstmt.setString(32,ls_empcode);
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
		return ls_errcode;
	}

	public String gbf_auto_crnote_sreturn (Timestamp adt_credit_note, String as_tran_type, String as_site_code, String as_tranid_from, String as_tranid_to, Date adt_tran_date_from, Date adt_tran_date_to, String as_cust_code_from, String as_cust_code_to, String as_post, String xtraParams, Connection conn)
			throws RemoteException, ITMException
	{
		String ls_errcode = "", errorString = "", ls_tran_id = "", ls_invoice_id = "", ls_item_code = "", ls_cust_code = "";
		String ls_old_cust_code = "",  trnofld = "", keystr = "", ls_auto_tran_id = "", ls_round = "";
		String ls_tax_chap = "", ls_tax_class = "", ls_tax_env = "", ls_sales_pers = "", ls_reas_code = "",ls_list_type = "";
		String ls_fin_entity = "", ls_curr_code = "", ls_anal_code = "", ls_emp_code__aprv = "", ls_sales_pers_sreturn = "", ls_sr_inv_tran_id = "", ls_prevtranid = "";
		String ls_acct_code = "", ls_cctr_code = "", ls_cr_term = "", ls_tax_rec = "", ls_item_ser = "", ls_old_item_ser = "", ls_lotsl = "";
		String ls_ret_opt = "",ls_drcr_flag = "",ls_tranwin = "", ls_serial_no = "", ls_sales_order = "",ls_apply_price = "";
		String ls_sord_line_no = "", ls_pricelist = "", ls_item_code_sord = "", ls_item_code__ord = "",ls_sr_tran_id = "", ls_item_code_sr = "",ls_finpara = "",ls_lot_no = "",ls_gp_no = "", ls_sr_ref_ser = "", ls_sr_ref_no = "";
		double lc_qty = 0, lc_qty1 = 0, lc_qty2 = 0, lc_exch_rate = 0, lc_tot_net_amt = 0;
		int ll_line_no = 0;
		String ll_line_no__inv = "", ll_hdr_row = "", ll_det_row = "", ll_line_no_sret = "", ll_count = "", ll_sr_line_no = "", ll_line_no_invtrace = "", li_currow = "";
		String lc_quantity = "", lc_round_to = "", lc_sr_adj_amt = "", lc_sr_ref_bal_amt = "", lc_round_amt = "", lc_diff_amt = "";
		String lc_old_amount = "", lc_old_amount_bc = "", lc_dprice_item = "", chgUser = "", chgTerm = "", transer = "", remarks = "", sql = "", keyCol = "", tranSer = "";
		double lc_rate__clg = 0, lc_qty_stduom = 0, lc_rate_stduom = 0, lc_discount = 0, lc_amt = 0, lc_drcr_amt = 0, lc_net_amt = 0, lc_tax_amt = 0; 
		Timestamp ld_eff_date = null, ld_due_date = null,ld_gp_date = null;
		String ls_rndstr = "", ls_rndTo = "", ls_rndoff = "", ls_ret_ref = "", ls_site_type = "", ls_creat_inv_othlist = "", ls_creat_inv_oth = "", ls_other_site = "",drNtTranId="";
		double refBalAmt=0,adjamt=0,totAMt=0;
		String totAmtStr="";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null,pstmt2=null,pstmt4;
		ResultSet rs = null,rs1=null;
		//ValidatorEJB vdt=new ValidatorEJB();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		//FinCommon finCommon = new FinCommon();
		StringBuffer xmlBuff = null;
		Timestamp currDate = null;
		UtilMethods utilMethods = UtilMethods.getInstance();
		boolean lb_flag;
		int currRow = 0; //Added by wasim
		//String invIbcaGen=null,siteCodeRcv=null;
		//change by monika salla  14 may 2020
		String invIbcaGen="",siteCodeRcv="";//end
		boolean adjInvoice = false;
		boolean bIbca=false;
		////change by monika salla  14 may 2020
		//String sretFullRetType=null,tranType=null;
		String sretFullRetType="",tranType="";
		String siteType="",creatInvOthlist="",creatInvOth="",otherSite="",retString1="",refDate="" ;     //added by manish mhatre
		try
		{
			xmlBuff= new StringBuffer();
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			transer = "CRNRCP";
			trnofld = "tran_id";
			ls_cr_term =  finCommon.getFinparams("999999", "CR_PRD_ZERO", conn);

			if ("D".equalsIgnoreCase(ls_ret_opt))
			{
				ls_drcr_flag = "D";		
				transer = "DRNRCP";
				ls_tranwin = "W_DRCRRCP_DR";
				//ls_auto_tran_id = generateTranId("w_drcrrcp_dr",as_site_code,ls_drcr_flag,currDateStr,conn);	
				//remarks = "Auto Generated Debit Note for Sales Return " + ls_tran_id;//commented by monika 20 nov 2019
			}
			else
			{
				ls_drcr_flag = "C";			
				transer = "CRNRCP";
				ls_tranwin = "W_DRCRRCP_CR";
				//ls_auto_tran_id = generateTranId("w_drcrrcp_cr",as_site_code,ls_drcr_flag,currDateStr,conn);	
				//remarks = "Auto Generated Credit Note for Sales Return " + ls_tran_id;//commented by monika 20 nov 2019
			}

			lb_flag = false;

			sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where upper(tran_window) = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_tranwin );
			rs = pstmt.executeQuery();
			if(rs.next())
			{	
				keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
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

			if(keystr == null || keystr.length() == 0)
			{
				sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{	
					keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
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
			//Changed by wasim on 30-MAY-2017 to copy below code in while loop below [START]
			//System.out.println("Old CustCode["+ls_old_cust_code+"] CustCode["+ls_cust_code+"] ls_old_item_ser["+ls_old_item_ser+"] ItemSer["+ls_item_ser+"]");
			/*if(ls_old_cust_code != ls_cust_code || ls_old_item_ser != ls_item_ser)
			{
				sql = "select fin_entity, acct_code__ar, cctr_code__ar, due_date, gp_no, gp_date from invoice where invoice_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_invoice_id);
				rs = pstmt.executeQuery();
				if(rs.next())
				{	
					ls_fin_entity = checkNullAndTrim(rs.getString(1));
					ls_acct_code = checkNullAndTrim(rs.getString(2));
					ls_cctr_code = checkNullAndTrim(rs.getString(3));
					ld_due_date = rs.getTimestamp(4);
					ls_gp_no = checkNullAndTrim(rs.getString(5));
					ld_gp_date = rs.getTimestamp(6);
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

				ls_finpara =  finCommon.getFinparams("999999", "INVOICE_DRCR_ADJ", conn);
				if("NULLFOUND".equalsIgnoreCase(ls_finpara))
				{
					ls_finpara = "N" ;
				}

				ls_rndstr = transer + "-RND";
				ls_rndoff =  finCommon.getFinparams("999999", ls_rndstr, conn);

				if (!"NULLFOUND".equalsIgnoreCase(ls_rndoff)) 
				{
					ls_rndoff = ls_rndoff.trim();
				}
				ls_rndstr = transer + "-RNDTO";
				ls_rndTo =  finCommon.getFinparams("999999", ls_rndstr, conn);
				if (!"NULLFOUND".equalsIgnoreCase(ls_rndTo)) 
				{
					ls_rndTo = ls_rndTo.trim();
				}

				sql = "INSERT INTO DRCR_RCP ( tran_id, tran_date, site_code, fin_entity, tran_type," +
				" tran_ser, drcr_flag, cust_code, item_ser, invoice_id ,acct_code ,cctr_code ,eff_date ," +
				" due_date ,curr_code ,exch_rate ,anal_code ,cr_term ,emp_code__aprv ,chg_date ,chg_user," +
				" chg_term,remarks,gp_no,gp_date ,rnd_off,rnd_to,amount,amount__bc,adj_recv,sreturn_no,cust_ref_no) " +
				" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,? ,?,?,?,?,?,?,?) " ; 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, ls_auto_tran_id); 
				pstmt.setTimestamp( 2, adt_credit_note ); 
				pstmt.setString( 3, as_site_code );
				pstmt.setString( 4, ls_fin_entity );
				pstmt.setString( 5, as_tran_type);
				pstmt.setString( 6, transer );
				pstmt.setString( 7, ls_drcr_flag);
				pstmt.setString( 8,ls_cust_code );
				pstmt.setString( 9, ls_item_ser ); 
				pstmt.setString( 10, ls_invoice_id );
				pstmt.setString( 11, ls_acct_code ); 
				pstmt.setString( 12, ls_cctr_code ); 
				pstmt.setTimestamp( 13, ld_eff_date ); 
				pstmt.setTimestamp( 14, ld_eff_date ); 
				pstmt.setString( 15, ls_curr_code ); 
				pstmt.setDouble( 16, lc_exch_rate ); 
				pstmt.setString( 17, ls_anal_code ); 
				pstmt.setString( 18, ls_cr_term ); 
				pstmt.setString( 19, ls_emp_code__aprv ); 
				pstmt.setTimestamp( 20, currDate ); 
				pstmt.setString( 21, chgUser );
				pstmt.setString( 22, chgTerm );
				pstmt.setString( 23, remarks );
				pstmt.setString( 24, ls_gp_no );
				pstmt.setTimestamp( 25, ld_gp_date );
				pstmt.setString( 26, ls_rndoff );
				pstmt.setString( 27, ls_rndTo );
				pstmt.setDouble( 28, lc_tot_net_amt );
				pstmt.setDouble( 29, (lc_tot_net_amt * lc_exch_rate) );
				pstmt.setString( 30, ls_finpara );
				pstmt.setString( 31, ls_tran_id );
				pstmt.setString( 32, ls_ret_ref );

				int cnt=pstmt.executeUpdate();
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
			}

			sql = "select sales_pers from invoice where invoice_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, ls_invoice_id); 
			rs = pstmt.executeQuery();
			if(rs.next())
			{	
				ls_sales_pers = checkNullAndTrim(rs.getString(1));
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
			}*/	
			//Changed by wasim on 30-MAY-2017 to copy below code in while loop below [END]

			sql = "select a.cust_code__bill, a.eff_date, a.curr_code, a.exch_rate, a.anal_code, a.emp_code__aprv, a.item_ser, b.tran_id, b.invoice_id, b.line_no__inv," +
					" b.item_code, b.quantity, b.net_amt, b.tax_chap, b.tax_class, b.tax_env, b.quantity__stduom, b.rate__stduom, b.discount, b.tax_amt, b.reas_code, b.line_no," +
					" a.sales_pers , a.ret_opt, b.tran_id, b.line_no, b.rate__clg, b.lot_no, b.line_no__invtrace, b.lot_sl, a.ret_ref,a.remarks from 	sreturn a , sreturndet b " +
					" where a.tran_id = b.tran_id and a.tran_id >= ? and a.tran_id <= ? and a.tran_date >= ? " +
					" and a.tran_date <= ? and a.cust_code >= ? and a.cust_code <= ? and a.site_code = ? " +
					" and a.tran_type = ? and a.confirmed = 'Y' and a.ret_opt in ('C','D') and b.invoice_id is not null and a.tran_id__crn is null " +
					" order by a.cust_code, a.item_ser, a.tran_date, a.tran_id ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,as_tranid_from );
			pstmt.setString(2,as_tranid_to );
			pstmt.setDate(3, (java.sql.Date) adt_tran_date_from);
			pstmt.setDate(4,(java.sql.Date) adt_tran_date_to );
			pstmt.setString(5,as_cust_code_from );
			pstmt.setString(6,as_cust_code_to );
			pstmt.setString(7,as_site_code );
			pstmt.setString(8,as_tran_type );
			rs = pstmt.executeQuery();
			while(rs.next())
			{	
				lb_flag = false;

				ll_line_no ++;
				//ls_cust_code = checkNullAndTrim(rs.getString(1));commented by monika salla 3 march 2020 --getting error while match cust_code and cust_code__dlv..
				ls_cust_code = rs.getString(1);//end

				ld_eff_date = rs.getTimestamp(2);
				ls_curr_code = checkNullAndTrim(rs.getString(3));
				lc_exch_rate = rs.getDouble(4);
				ls_anal_code = checkNullAndTrim(rs.getString(5));
				ls_emp_code__aprv = checkNullAndTrim(rs.getString(6));
				ls_item_ser = checkNullAndTrim(rs.getString(7));
				ls_tran_id = checkNullAndTrim(rs.getString(8));
				ls_invoice_id = checkNullAndTrim(rs.getString(9));
				ll_line_no__inv = checkNullAndTrim(rs.getString(10));
				ls_item_code = checkNullAndTrim(rs.getString(11));
				lc_quantity = checkNullAndTrim(rs.getString(12));
				lc_net_amt = rs.getDouble(13);
				ls_tax_chap = checkNullAndTrim(rs.getString(14));
				ls_tax_class = checkNullAndTrim(rs.getString(15));
				ls_tax_env = checkNullAndTrim(rs.getString(16));
				lc_qty_stduom = rs.getDouble(17);
				lc_rate_stduom = rs.getDouble(18);
				lc_discount = rs.getDouble(19);
				lc_tax_amt = rs.getDouble(20);
				ls_reas_code = checkNullAndTrim(rs.getString(21));
				ll_line_no_sret = rs.getString(22);
				ls_sales_pers_sreturn = checkNullAndTrim(rs.getString(23));
				ls_ret_opt = checkNullAndTrim(rs.getString(24));
				ls_sr_tran_id = checkNullAndTrim(rs.getString(25));
				ll_sr_line_no = checkNullAndTrim(rs.getString(26));
				lc_rate__clg = rs.getDouble(27);
				ls_lot_no = checkNull(rs.getString(28));
				ll_line_no_invtrace = checkNullAndTrim(rs.getString(29));
				ls_lotsl = checkNullAndTrim(rs.getString(30));
				ls_ret_ref = checkNullAndTrim(rs.getString(31));
				remarks=rs.getString(32);
				System.out.println();
				//Added by wasim on 01-JUN-2017 as amount calculation was not migrated [START]
				sql = "select sum(quantity) from invdet where invoice_id = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_invoice_id); 
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{	
					lc_qty1 = rs1.getDouble(1);
				}
				if (pstmt1 != null )
				{
					pstmt1.close();pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();rs1 = null;
				}	
				sql = "select sum(quantity) from sreturndet where tran_id =  ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_sr_tran_id); 
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{	
					lc_qty2 = rs1.getDouble(1);
				}
				if (pstmt1 != null )
				{
					pstmt1.close();pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();rs1 = null;
				}	

				if (lc_qty1 == lc_qty2)
				{
					lb_flag = false;
				}

				System.out.println("lb_flag-->"+lb_flag);

				if (lb_flag == false)
				{
					lc_drcr_amt = (lc_qty_stduom * lc_rate_stduom) - ((lc_qty_stduom * lc_rate_stduom) * lc_discount / 100);
					lc_tot_net_amt = lc_tot_net_amt + lc_net_amt ;


				}
				if (lb_flag == true)
				{
					lc_tot_net_amt = lc_tot_net_amt + lc_amt + lc_tax_amt;
				}
				System.out.println("DRCR_RCP---lc_tot_net_amt["+lc_tot_net_amt+"] lc_amt ["+lc_amt+"] lc_tax_amt["+lc_tax_amt+"] lc_drcr_amt["+lc_drcr_amt+"]"
						+ "lc_qty_stduom["+lc_qty_stduom+"] lc_rate_stduom["+lc_rate_stduom+"]  lc_discount["+lc_discount+"]");
				//Added by wasim on 01-JUN-2017 as amount calculation was not migrated [END]


				//Changed by wasim to insert into header [START]
				//System.out.println("Old CustCode["+ls_old_cust_code+"] CustCode["+ls_cust_code+"] ls_old_item_ser["+ls_old_item_ser+"] ItemSer["+ls_item_ser+"]");

				if( ! ls_old_cust_code.equalsIgnoreCase(ls_cust_code))//  || ! ls_old_item_ser.equalsIgnoreCase(ls_item_ser) )
				{
					//System.out.println("Inside header loop");
					sql = "select fin_entity, acct_code__ar, cctr_code__ar, due_date, gp_no, gp_date from invoice where invoice_id = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, ls_invoice_id);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{	
						ls_fin_entity = checkNullAndTrim(rs1.getString(1));
						ls_acct_code = checkNullAndTrim(rs1.getString(2));
						//ls_cctr_code = checkNullAndTrim(rs1.getString(3));
						ls_cctr_code = rs1.getString(3);   //removed checknullandtrim
						ld_due_date = rs1.getTimestamp(4);
						ls_gp_no = checkNullAndTrim(rs1.getString(5));
						ld_gp_date = rs1.getTimestamp(6);
					}
					if (pstmt1 != null )
					{
						pstmt1.close();pstmt1 = null;
					}				
					if (rs1 != null )
					{
						rs1.close();rs1 = null;
					}		

					ls_finpara =  finCommon.getFinparams("999999", "INVOICE_DRCR_ADJ", conn);
					if("NULLFOUND".equalsIgnoreCase(ls_finpara))
					{
						ls_finpara = "N" ;
					}

					ls_rndstr = transer + "-RND";
					ls_rndoff =  finCommon.getFinparams("999999", ls_rndstr, conn);

					if (!"NULLFOUND".equalsIgnoreCase(ls_rndoff)) 
					{
						ls_rndoff = ls_rndoff.trim();
					}
					ls_rndstr = transer + "-RNDTO";
					ls_rndTo =  finCommon.getFinparams("999999", ls_rndstr, conn);
					if (!"NULLFOUND".equalsIgnoreCase(ls_rndTo)) 
					{
						ls_rndTo = ls_rndTo.trim();
					}

					/*		sql = "INSERT INTO DRCR_RCP ( tran_id, tran_date, site_code, fin_entity, tran_type," +
					" tran_ser, drcr_flag, cust_code, item_ser, invoice_id ,acct_code ,cctr_code ,eff_date ," +
					" due_date ,curr_code ,exch_rate ,anal_code ,cr_term ,emp_code__aprv ,chg_date ,chg_user," +
					" chg_term,remarks,gp_no,gp_date ,rnd_off,rnd_to,amount,amount__bc,adj_recv,sreturn_no,cust_ref_no, confirmed) " +
					" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,? ,?,?,?,?,?,?,?,? ) " ; 
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString( 1, ls_auto_tran_id); 
					pstmt1.setTimestamp( 2, adt_credit_note ); 
					pstmt1.setString( 3, as_site_code );
					pstmt1.setString( 4, ls_fin_entity );
					pstmt1.setString( 5, as_tran_type);
					pstmt1.setString( 6, transer );
					pstmt1.setString( 7, ls_drcr_flag);
					pstmt1.setString( 8,ls_cust_code );
					pstmt1.setString( 9, ls_item_ser ); 
					pstmt1.setString( 10, ls_invoice_id );
					pstmt1.setString( 11, ls_acct_code ); 
					pstmt1.setString( 12, ls_cctr_code ); 
					pstmt1.setTimestamp( 13, ld_eff_date ); 
					pstmt1.setTimestamp( 14, ld_eff_date ); 
					pstmt1.setString( 15, ls_curr_code ); 
					pstmt1.setDouble( 16, lc_exch_rate ); 
					pstmt1.setString( 17, ls_anal_code ); 
					pstmt1.setString( 18, ls_cr_term ); 
					pstmt1.setString( 19, ls_emp_code__aprv ); 
					pstmt1.setTimestamp( 20, currDate ); 
					pstmt1.setString( 21, chgUser );
					pstmt1.setString( 22, chgTerm );
					pstmt1.setString( 23, remarks );
					pstmt1.setString( 24, ls_gp_no );
					pstmt1.setTimestamp( 25, ld_gp_date );
					pstmt1.setString( 26, ls_rndoff );
					pstmt1.setString( 27, ls_rndTo );
					pstmt1.setDouble( 28, lc_tot_net_amt );
					pstmt1.setDouble( 29, (lc_tot_net_amt * lc_exch_rate) );
					pstmt1.setString( 30, ls_finpara );
					pstmt1.setString( 31, ls_tran_id );
					pstmt1.setString( 32, ls_ret_ref );
					pstmt1.setString( 33, "N" );

					ls_old_cust_code = ls_cust_code.trim();
					//int cnt=pstmt1.executeUpdate();
					if(pstmt1 != null)
					{
						pstmt1.close();pstmt1 = null;
					}
					 *///commented by monika 20 nov 2019
					//System.out.println("mukesh tran date....."+tranDate+"currdate"+sdfAppl.format(adt_credit_note).toString());
					if(ll_line_no==1)//condition  added by nandkumar gadkari on 13-03-20 
					{
						xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
						xmlBuff.append("<description>Datawindow Root</description>");
						xmlBuff.append("<group0>");
						xmlBuff.append("<description>Group0 description</description>");
						xmlBuff.append("<Header0>");
						xmlBuff.append("<objName><![CDATA[").append("drcrrcp_cr").append("]]></objName>");		
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
						xmlBuff.append("<description>Header0 members</description>");


						xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"drcrrcp_cr\">");		
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
						//xmlBuff.append("<drcrrcp_cr/>");
						xmlBuff.append("<tran_id/>");

						xmlBuff.append("<tran_ser>").append("<![CDATA[" + tranSer + "]]>").append("</tran_ser>");
						//xmlBuff.append("<tran_date>").append("<![CDATA[" + sdfAppl.format(adt_credit_note).toString() + "]]>").append("</tran_date>"); //comment by mukesh chauhan on 22/05/2020
						xmlBuff.append("<tran_date>").append("<![CDATA[" + sdfAppl.format(adt_tran_date_from)+ "]]>").append("</tran_date>");//changed by mukesh chauhan to append tran date on 22/05/2020
						xmlBuff.append("<fin_entity>").append("<![CDATA[" + ls_fin_entity + "]]>").append("</fin_entity>");
						xmlBuff.append("<site_code>").append("<![CDATA[" + as_site_code + "]]>").append("</site_code>");
						xmlBuff.append("<sundry_type>").append("<![CDATA[" + "C" + "]]>").append("</sundry_type>");
						xmlBuff.append("<cust_code>").append("<![CDATA[" + ls_cust_code + "]]>").append("</cust_code>");
						xmlBuff.append("<item_ser>").append("<![CDATA[" + ls_item_ser + "]]>").append("</item_ser>");
						xmlBuff.append("<invoice_id>").append("<![CDATA[" + ls_invoice_id + "]]>").append("</invoice_id>");
						xmlBuff.append("<acct_code>").append("<![CDATA[" + ls_acct_code + "]]>").append("</acct_code>");
						xmlBuff.append("<cctr_code>").append("<![CDATA[" + ls_cctr_code + "]]>").append("</cctr_code>");
						xmlBuff.append("<amount>").append("<![CDATA[" + (lc_tot_net_amt) + "]]>").append("</amount>");
						xmlBuff.append("<curr_code>").append("<![CDATA[" + checkNull(ls_curr_code ) + "]]>").append("</curr_code>");
						xmlBuff.append("<exch_rate>").append("<![CDATA[" + lc_exch_rate + "]]>").append("</exch_rate>");			
						xmlBuff.append("<anal_code>").append("<![CDATA[" + ls_anal_code + "]]>").append("</anal_code>");
						xmlBuff.append("<cr_term>").append("<![CDATA[" + ls_cr_term + "]]>").append("</cr_term>");
						//	xmlBuff.append("<emp_code__aprv>").append("<![CDATA[" + ls_emp_code__aprv + "]]>").append("</emp_code__aprv>");
						xmlBuff.append("<drcr_flag>").append("<![CDATA[" + "C" + "]]>").append("</drcr_flag>");

						//added by manish mhatre on 7-feb-2019
						//start manish
						if ((remarks!=null)&& (remarks.trim().length() > 0 ))
						{
							remarks = remarks.trim() + " " + ls_tran_id;
						}
						else
						{
							if ("D".equalsIgnoreCase(ls_ret_opt))
							{	
								remarks = "Auto Generated Debit Note for Sales Return " + ls_tran_id;
							}
							else
							{	
								remarks = "Auto Generated Credit Note for Sales Return " + ls_tran_id;
							}
						} 

						//	remarks=remarks.substring(0, remarks.length()-1);
						remarks=utilMethods.left(remarks, 60);
						//end manish
						xmlBuff.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");

						xmlBuff.append("<gp_no>").append("<![CDATA[" + ls_gp_no + "]]>").append("</gp_no>");
						xmlBuff.append("<gp_date>").append("<![CDATA[" + (ld_gp_date==null?"":sdfAppl.format(ld_gp_date).toString()) + "]]>").append("</gp_date>");
						//xmlBuff.append("<tran_id__rcv>").append("<![CDATA[" + "" + "]]>").append("</tran_id__rcv>");
						xmlBuff.append("<adj_recv>").append("<![CDATA[" + ls_finpara + "]]>").append("</adj_recv>");
						xmlBuff.append("<confirmed>").append("<![CDATA[" + "N" + "]]>").append("</confirmed>");
						xmlBuff.append("<eff_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</eff_date>");
						xmlBuff.append("<due_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</due_date>");
						xmlBuff.append("<item_ser>").append("<![CDATA[" + checkNull(ls_item_ser ) + "]]>").append("</item_ser>");
						xmlBuff.append("<tran_type>").append("<![CDATA[" + checkNull(as_tran_type) + "]]>").append("</tran_type>");
						xmlBuff.append("<amount__bc>").append("<![CDATA[" + (lc_tot_net_amt * lc_exch_rate ) + "]]>").append("</amount__bc>");
						xmlBuff.append("<rnd_off>").append("<![CDATA[" + ls_rndoff + "]]>").append("</rnd_off>");
						xmlBuff.append("<rnd_to>").append("<![CDATA[" + ls_rndTo + "]]>").append("</rnd_to>");			
						//added by monika 24 dec 2019-
						//SRETURN_NO
						xmlBuff.append("<sreturn_no>").append("<![CDATA[" + ls_tran_id + "]]>").append("</sreturn_no>");			

						xmlBuff.append("</Detail1>");
						xmlBuff.append("\n");
					}


				}//commented by monika 20 nov 2019

				/*	sql = "select sales_pers from invoice where invoice_id = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_invoice_id); 
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{	
					ls_sales_pers = checkNullAndTrim(rs1.getString(1));
				}
				if (pstmt1 != null )
				{
					pstmt1.close();pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();rs1 = null;
				}*///commented by monika 20 nov 2019
				//Changed by wasim to insert into header [END]

				/*sql = "select sum(quantity) from invdet where invoice_id = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_invoice_id); 
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{	
					lc_qty1 = rs1.getDouble(1);
				}
				if (pstmt1 != null )
				{
					pstmt1.close();pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();rs1 = null;
				}	
				sql = "select sum(quantity) from sreturndet where tran_id =  ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_sr_tran_id); 
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{	
					lc_qty2 = rs1.getDouble(1);
				}
				if (pstmt1 != null )
				{
					pstmt1.close();pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();rs1 = null;
				}	

				if (lc_qty1 == lc_qty2)
				{
					lb_flag = false;
				}*/


				xmlBuff.append("<Detail2 dbID=\"\" domID=\""+ll_line_no+"\" objContext=\"2\" objName=\"drcrrcp_cr\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
				xmlBuff.append("<drcrrcp_cr/>");

				xmlBuff.append("<line_no>").append("<![CDATA[" + ll_line_no + "]]>").append("</line_no>");
				xmlBuff.append("<invoice_id>").append("<![CDATA[" + ls_invoice_id + "]]>").append("</invoice_id>");
				xmlBuff.append("<line_no__inv>").append("<![CDATA[" + ll_line_no__inv + "]]>").append("</line_no__inv>");
				xmlBuff.append("<item_code>").append("<![CDATA[" + ls_item_code + "]]>").append("</item_code>");
				xmlBuff.append("<sales_pers>").append("<![CDATA[" + ls_sales_pers + "]]>").append("</sales_pers>");
				xmlBuff.append("<tax_class>").append("<![CDATA[" + ls_tax_class + "]]>").append("</tax_class>");
				xmlBuff.append("<tax_chap>").append("<![CDATA[" + ls_tax_chap + "]]>").append("</tax_chap>");
				xmlBuff.append("<tax_env>").append("<![CDATA[" + ls_tax_env + "]]>").append("</tax_env>");				
				//added by monika 24 dec 2019-
				//SRETURN_NO
				xmlBuff.append("<sreturn_no>").append("<![CDATA[" + ls_tran_id + "]]>").append("</sreturn_no>");			


				/*
				sql = " INSERT INTO DRCR_RDET ( TRAN_ID, LINE_NO, INVOICE_ID, LINE_NO__INV, ITEM_CODE, SALES_PERS, TAX_CLASS, TAX_CHAP, TAX_ENV, DRCR_AMT," +
				" TAX_AMT, NET_AMT, REAS_CODE, QUANTITY, RATE, RATE__CLG, DISCOUNT, LINE_NO__SRET, LINE_NO__INVTRACE, LOT_NO, LOT_SL) " +
				" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ; 
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_auto_tran_id); 
				pstmt1.setInt(2, ll_line_no ); 
				pstmt1.setString( 3, ls_invoice_id );
				pstmt1.setString( 4, ll_line_no__inv );
				pstmt1.setString( 5, ls_item_code );
				pstmt1.setString( 6, ls_sales_pers);
				pstmt1.setString( 7, ls_tax_class );
				pstmt1.setString( 8, ls_tax_chap);
				pstmt1.setString( 9, ls_tax_env);

				if (lb_flag == false)
				{
					//lc_drcr_amt = (lc_qty_stduom * lc_rate_stduom) - ((lc_qty_stduom * lc_rate_stduom) * lc_discount / 100);
					pstmt1.setDouble( 10, lc_drcr_amt);
					//lc_tot_net_amt = lc_tot_net_amt + lc_net_amt ;
					pstmt1.setDouble( 12, lc_net_amt);*///commented by monika 20 nov 2019


				xmlBuff.append("<drcr_amt>").append("<![CDATA[" + lc_drcr_amt + "]]>").append("</drcr_amt>");
				xmlBuff.append("<net_amt>").append("<![CDATA[" + lc_net_amt + "]]>").append("</net_amt>");
				/*}
				if (lb_flag == true)
				{
					pstmt1.setDouble( 10, lc_amt);
					pstmt1.setDouble( 12, (lc_amt + lc_tax_amt));*///commented by monika 20 nov 2019

				/*	xmlBuff.append("<drcr_amt>").append("<![CDATA[" + lc_amt + "]]>").append("</drcr_amt>");
				xmlBuff.append("<net_amt>").append("<![CDATA[" + (lc_amt + lc_tax_amt) + "]]>").append("</net_amt>");*/
				/*}

				//pstmt1.setDouble( 10, lc_drcr_amt);
				pstmt1.setDouble( 11, lc_tax_amt);
				//pstmt1.setDouble( 12, (lc_amt + lc_tax_amt));
				pstmt1.setString( 13, ls_reas_code);
				pstmt1.setDouble( 14, lc_qty_stduom);
				pstmt1.setDouble( 15, lc_rate_stduom);
				pstmt1.setDouble( 16, lc_rate__clg);
				pstmt1.setDouble( 17, lc_discount);
				pstmt1.setString( 18, ll_line_no_sret);
				pstmt1.setString( 19, ll_line_no_invtrace);
				pstmt1.setString( 20, ls_lot_no);
				pstmt1.setString( 21, ls_lotsl);*///commented by monika 20 nov 2019


				xmlBuff.append("<tax_amt>").append("<![CDATA[" + (lc_tax_amt) + "]]>").append("</tax_amt>");
				xmlBuff.append("<reas_code>").append("<![CDATA[" + (ls_reas_code) + "]]>").append("</reas_code>");
				xmlBuff.append("<quantity>").append("<![CDATA[" + (lc_qty_stduom) + "]]>").append("</quantity>");
				xmlBuff.append("<rate>").append("<![CDATA[" + (lc_rate_stduom) + "]]>").append("</rate>");
                //xmlBuff.append("<rate_clg>").append("<![CDATA[" + (lc_rate__clg) + "]]>").append("</rate_clg>");
                xmlBuff.append("<rate__clg>").append("<![CDATA[" + (lc_rate__clg) + "]]>").append("</rate__clg>");
				xmlBuff.append("<discount>").append("<![CDATA[" + (lc_discount) + "]]>").append("</discount>");
				xmlBuff.append("<line_no__sret>").append("<![CDATA[" + (ll_line_no_sret) + "]]>").append("</line_no__sret>");//changed by mukesh chauhan on 22/05/2020
				xmlBuff.append("<line_no__invtrace>").append("<![CDATA[" + (ll_line_no_invtrace) + "]]>").append("</line_no__invtrace>");
				xmlBuff.append("<lot_no>").append("<![CDATA[" + (ls_lot_no) + "]]>").append("</lot_no>");
				xmlBuff.append("<lot_sl>").append("<![CDATA[" + (ls_lotsl) + "]]>").append("</lot_sl>");

				xmlBuff.append("</Detail2>");
				xmlBuff.append("\n");	


				//int detCnt = pstmt1.executeUpdate();
				/*if(pstmt1 != null)
				{
					pstmt1.close();pstmt1 = null;
				}*/
			} //End while loop
			
			if (rs != null )
			{
				rs.close();
				rs = null;
			}	
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}				
			
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			errorString = saveData(xtraParams,as_site_code,xmlBuff.toString(),conn);
			//System.out.println("@@@@@2: retString:"+errorString);
			//System.out.println("--retString finished--");
			if (errorString.indexOf("Success") > -1)
			{
				//	System.out.println("@@@@@@3: Success"+errorString);
				Document dom = genericUtility.parseString(errorString);
				//System.out.println("dom>>>"+dom);
				drNtTranId = genericUtility.getColumnValue("TranID",dom);
			}
			else
			{
				//System.out.println("[SuccessSuccess" + errorString + "]");	
				conn.rollback();
				return errorString;
			}
			//System.out.println("@@tranId cr note @@@@:"+drNtTranId+"tran_id sreturn.. "+ls_tran_id);
			sql = "update sreturn set tran_id__crn = ? where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, drNtTranId);
			pstmt.setString(2, ls_tran_id);
			int rowcnt = pstmt.executeUpdate();
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			//added by monika 0n 20 nov 2019	

			currRow = 0;
			/*sql = "SELECT TRAN_ID, REF_SER, REF_NO, ADJ_AMT, REF_BAL_AMT FROM SRETURN_INV WHERE TRAN_ID = ?";
			pstmtselect tran_id,tran_ser,ref_no , = conn.prepareStatement(sql);
			pstmt.setString( 1,ls_tran_id); 
			rs = pstmt.executeQuery();
			while(rs.next())
			{	
				currRow++;
				ls_sr_inv_tran_id = checkNullAndTrim(rs.getString(1));
				ls_sr_ref_ser = checkNullAndTrim(rs.getString(2));
				ls_sr_ref_no = checkNullAndTrim(rs.getString(3));
				lc_sr_adj_amt = checkNullAndTrim(rs.getString(4));
				lc_sr_ref_bal_amt = checkNullAndTrim(rs.getString(5));*///commented by monika 22 nov 19 to insert data in drcr


			/*sql = "select tran_id,tran_ser,ref_no ,(case when adj_amt is null then 0 else adj_amt end) as adj_amt,(case when tot_amt is null then 0 else tot_amt end) as tot_amt"
					+ " from receivables where ref_no = ? and tran_ser = 'S-INV' ";
			 */
			sql = "select tran_id,tran_ser,ref_no ,(case when adj_amt is null then 0 else adj_amt end) as adj_amt,(case when tot_amt is null then 0 else tot_amt end) as tot_amt"
					+ " from receivables where ref_no = ? and tran_ser like 'R-IBC%' and (tot_amt-adj_amt<>0)";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_invoice_id);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				currRow++;
				ls_sr_inv_tran_id = checkNullAndTrim(rs.getString("tran_id"));
				ls_sr_ref_ser = checkNullAndTrim(rs.getString("tran_ser"));
				ls_sr_ref_no = checkNullAndTrim(rs.getString("ref_no"));
				adjamt =rs.getDouble("adj_amt");
				totAMt =rs.getDouble("tot_amt");

				refBalAmt = totAMt - adjamt;

				//System.out.println("@@TotAmt["+totAMt+"]  @@AdjAmt["+adjamt+"]@@ refSer["+ls_sr_ref_ser+"]@@ refBalAmt ["+refBalAmt);
				//.out.println("tran_id [" +ls_sr_inv_tran_id+" ]ref_ser ["+ls_sr_ref_ser+" ]ref_no ["+ls_sr_ref_no+" ]adj_amt ["+adjamt+" ]REF_BAL_AMT ["+refBalAmt);
				//added by monika 10 jan 2020

				sretFullRetType = checkNullAndTrim(distCommon.getDisparams("999999","SRET_FULL_TRANTYPE",conn));

				//System.out.println("sretFullRetType["+sretFullRetType+"]");
				if("NULLFOUND".equals(sretFullRetType) || sretFullRetType.length() > 0)
				{
					adjInvoice = true;
				}
				else
				{
					adjInvoice = false;
				}

				sql = " select tran_type from sreturn where tran_id = ? " ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,ls_tran_id);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					tranType = rs1.getString("tran_type");
				}
				if(pstmt1 != null)
				{
					pstmt1.close();pstmt1 = null;
				}
				if(rs1 != null)
				{
					rs1.close();rs1 = null;
				}
				invIbcaGen = checkNullAndTrim(finCommon.getFinparams("999999","INV_IBCA_GEN",conn));
				System.out.println("INV_IBCA_GEN["+invIbcaGen+"]");

				if("NULLFOUND".equals(invIbcaGen))
				{
					invIbcaGen = "Y";
				}
				else if("N".equals(invIbcaGen))
				{
					bIbca=false;
				}
				else
				{
					bIbca=true;


					sql = " select site_customer.site_code__rcp from site_customer "
							+ " where ( site_customer.site_code = ? ) and ( site_customer.cust_code = ? ) ";
					pstmt4 = conn.prepareStatement(sql);
					pstmt4.setString(1,as_site_code);
					pstmt4.setString(2,ls_cust_code);
					rs1 = pstmt4.executeQuery();
					if(rs1.next())
					{
						siteCodeRcv = rs1.getString("site_code__rcp");
					}
					if(pstmt4 != null)
					{
						pstmt4.close();pstmt4 = null;
					}
					if(rs1 != null)
					{
						rs1.close();rs1 = null;
					}
					System.out.println("sitecode rcv from sitecuxtomer---"+siteCodeRcv);
					//	if(siteCodeRcv.length() == 0)//change by monika 0n 13 may 20 --getting null pointer exception
					if(siteCodeRcv == null || siteCodeRcv.trim().length() ==0)
					{
						sql = " select site_code__rcp from customer where cust_code = ?  ";
						pstmt4 = conn.prepareStatement(sql);
						pstmt4.setString(1,ls_cust_code);
						rs1 = pstmt4.executeQuery();
						if(rs1.next())
						{
							//change by monika 0n 13 may 20 --getting null pointer exception
							//siteCodeRcv = checkNullAndTrim(rs1.getString("site_code__rcp"));
							siteCodeRcv = rs1.getString("site_code__rcp");

						}
						System.out.println("sitecode rcv from cuxtomer---"+siteCodeRcv);
						if(pstmt4 != null)
						{
							pstmt4.close();pstmt4 = null;
						}
						if(rs1 != null)
						{
							rs1.close();rs1 = null;
						}
					}

					//	if(siteCodeRcv.length() == 0)//change by monika 0n 13 may 20 --getting null pointer exception
					if(siteCodeRcv == null || siteCodeRcv.trim().length() ==0)//end
					{
						siteCodeRcv = as_site_code ;
					}
					//added by monika 9 jan 2020
					if((siteCodeRcv.trim()).equals(as_site_code.trim()))
					{
						bIbca=false;
					}



					System.out.println("adjInvoice["+adjInvoice+"] INVOICE IBCA ["+invIbcaGen+" ]");

					if(adjInvoice && !bIbca)
					{
						//end

						//Added by wasim on 01-jun-2017 to insert data into drcr_rcpinv as per NVO code, previously it was writtten this code [START]
						sql = " insert into drcr_rcpinv (tran_id, line_no, ref_ser, ref_no, adj_amt, ref_bal_amt) values (?,?,?,?,?,?) ";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, drNtTranId);
						pstmt2.setInt(2, currRow);
						pstmt2.setString(3, ls_sr_ref_ser);
						pstmt2.setString(4, ls_sr_ref_no);
						/*	pstmt1.setDouble(5, Double.valueOf(lc_sr_adj_amt));
				pstmt1.setDouble(6, Double.valueOf(lc_sr_ref_bal_amt));
						 */
						pstmt2.setDouble(5, Double.valueOf(adjamt));
						pstmt2.setDouble(6, Double.valueOf(refBalAmt));

						pstmt2.executeUpdate();
						if (pstmt2 != null )
						{
							pstmt2.close();
							pstmt2 = null;
						}
						//System.out.println(" data inserted tran_id["+drNtTranId+" ]line no ["+currRow+" ]ref_ser ["+ls_sr_ref_ser+" ]ref_no ["+ls_sr_ref_no +" ] adj_amt ["+lc_sr_adj_amt);
						//Added by wasim on 01-jun-2017 to insert data into drcr_rcpinv as per NVO code, previously it was writtten this code [END]
						/*	xmlBuff.append("<Detail3 dbID=\"\" domID=\"1\" objName=\"drcrrcp_cr\" objContext=\"3\">");
				xmlBuff.append("<line_no><![CDATA["+ currRow   +"]]></line_no>");
				xmlBuff.append("<ref_ser><![CDATA["+ ls_sr_ref_ser   +"]]></ref_ser>");
				xmlBuff.append("<ref_no><![CDATA["+ ls_sr_ref_no   +"]]></ref_no>");
				xmlBuff.append("<ref_bal_amt><![CDATA["+ lc_sr_ref_bal_amt   +"]]></ref_bal_amt>");
				xmlBuff.append("<adj_amt><![CDATA["+ lc_sr_adj_amt   +"]]></adj_amt>");
				xmlBuff.append("</Detail3>");*/
					}
				}//end
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
			/*xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			errorString = saveData(xtraParams,as_site_code,xmlBuff.toString(),conn);
			System.out.println("@@@@@2: retString:"+errorString);
			System.out.println("--retString finished--");
			if (errorString.indexOf("Success") > -1)
			{
				System.out.println("@@@@@@3: Success"+errorString);
				Document dom = genericUtility.parseString(errorString);
				System.out.println("dom>>>"+dom);
				drNtTranId = genericUtility.getColumnValue("TranID",dom);
			}
			else
			{
				System.out.println("[SuccessSuccess" + errorString + "]");	
				conn.rollback();
				return errorString;
			}
			 */

			/*sql = "update sreturn set tran_id__crn = ? where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, drNtTranId);
			pstmt.setString(2, ls_tran_id);
			int rowcnt = pstmt.executeUpdate();
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			 */


			//Changed by wasim on 24-MAY-2017 to call gbfRetrieveDrCrRCP as previously gbfRetrieveDrCrInv login was migrated [START]
			/*ls_errcode = gbfRetrieveDrCr(ls_auto_tran_id, as_site_code, xtraParams, conn);
			if (ls_errcode != null && ls_errcode.trim().length() > 0) 
			{
				errorString = vdt.getErrorString("",ls_errcode,"");
				return errorString;
			}*/

			//ls_errcode = gbfRetrieveDrCrRcp(ls_auto_tran_id, as_site_code, xtraParams, conn);
			DrCrRcpConf drcrObj = new DrCrRcpConf();
			errorString = drcrObj.confirm (drNtTranId, xtraParams, "" , conn);
			System.out.println("After DrCrRcpConf---->["+errorString+"]");
			if(errorString != null && errorString.indexOf("VTCICONF3") != -1)
			{
				errorString = "";

				//added by manish mhatre on 20-jan-2020
				//start manish
				sql = " select site_type  from site where site_code = ? "; 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, as_site_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					siteType= checkNullAndTrim(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;


				creatInvOthlist = finCommon.getFinparams("999999", "ALOW_INV_OTH_SITE", conn); 

				if( "NULLFOUND".equalsIgnoreCase(creatInvOthlist) || creatInvOthlist == null)
				{
					creatInvOthlist="";
				}

				if(creatInvOthlist.trim().length() > 0)
				{
					String[] arrStr = creatInvOthlist.split(",");
					for (int i = 0; i < arrStr.length; i++) {
						creatInvOth = arrStr[i];
						System.out.println("creatInvOth>>>>>>>>" + creatInvOth);
						if(siteType.equalsIgnoreCase(creatInvOth.trim()))
						{
							otherSite = finCommon.getFinparams("999999", "INVOICE_OTHER_SITE", conn); 
							if( !"NULLFOUND".equalsIgnoreCase(creatInvOthlist) && creatInvOthlist != null && creatInvOthlist.trim().length() > 0)
							{
								errorString=gbfAutoCrnoteSreturnOth( drNtTranId,  otherSite , refDate, xtraParams,  conn);
								if( errorString != null && errorString.trim().length() > 0 )
								{
									return errorString;
								}
							}
						}

					}	
				}  //end manish

			}
			//Changed by wasim on 24-MAY-2017 to call gbfRetrieveDrCrRCP as previously gbfRetrieveDrCrInv login was migrated [END]
		}
		catch(Exception e)
		{
			System.out.println("Exception inside gbf_auto_crnote_sreturn="+e.getMessage());
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
		return errorString;
	}

	public String gbf_auto_crnote_sreturn_misc (Timestamp adt_credit_note, String as_tran_type, String as_site_code, String as_tranid_from, String as_tranid_to, Date adt_tran_date_from, Date adt_tran_date_to, String as_cust_code_from, String as_cust_code_to, String as_post, String xtraParams, Connection conn)
			throws RemoteException, ITMException
	{
		String ls_errcode = "", errorString = "", ls_tran_id = "", ls_invoice_id = "", ls_item_code = "", ls_cust_code = "", ls_posttype = "", ls_adj_misc_crn = "";
		String ls_old_cust_code = "",  trnofld = "", keystr = "", ls_auto_tran_id = "", ls_round = "", ls_acct_code__pfee = "", ls_cctr_code__pfee = "";
		String ls_tax_chap = "", ls_tax_class = "", ls_tax_env = "", ls_sales_pers = "", ls_reas_code = "",ls_list_type = "", ls_trantype = "", ls_unit = "";
		String ls_fin_entity = "", ls_curr_code = "", ls_anal_code = "", ls_emp_code__aprv = "", ls_sales_pers_sreturn = "", ls_sr_inv_tran_id = "", ls_prevtranid = "";
		String ls_acct_code = "", ls_cctr_code = "", ls_det_acct = "", ls_det_cctr = "", ls_cr_term = "", ls_tax_rec = "", ls_item_ser = "", ls_old_item_ser = "", ls_lotsl = "";
		String ls_ret_opt = "",ls_drcr_flag = "",ls_tranwin = "", ls_serial_no = "", ls_sales_order = "",ls_apply_price = "", ls_process_fee_reason = "";
		String ls_sord_line_no = "", ls_pricelist = "", ls_item_code_sord = "", ls_item_code__ord = "",ls_sr_tran_id = "", ls_item_code_sr = "",ls_finpara = "",ls_lot_no = "",ls_gp_no = "", ls_sr_ref_ser = "", ls_sr_ref_no = "";
		double lc_qty = 0, lc_qty1 = 0, lc_qty2 = 0, lc_exch_rate = 0, lc_tot_net_amt = 0, lc_process_fee = 0;
		int ll_line_no = 0;
		String ll_line_no__inv = "", ll_hdr_row = "", ll_det_row = "", ll_line_no_sret = "", ll_count = "", ll_sr_line_no = "", ll_line_no_invtrace = "", li_currow = "";
		String lc_quantity = "", lc_round_to = "", lc_sr_adj_amt = "", lc_sr_ref_bal_amt = "", lc_round_amt = "", lc_diff_amt = "";
		String lc_old_amount = "", lc_old_amount_bc = "", lc_dprice_item = "", chgUser = "", chgTerm = "", transer = "", remarks = "", sql = "", keyCol = "", tranSer = "";
		double lc_rate__clg = 0, lc_qty_stduom = 0, lc_rate_stduom = 0, lc_discount = 0, lc_amt = 0, lc_drcr_amt = 0, lc_net_amt = 0, lc_tax_amt = 0; 
		Timestamp ld_eff_date = null, ld_due_date = null,ld_gp_date = null;
		String ls_rndstr = "", ls_rndTo = "", ls_rndoff = "", ls_ret_ref = "", ls_site_type = "", ls_creat_inv_othlist = "", ls_creat_inv_oth = "", ls_other_site = "";
		String ls_temp = "", li_Sreturn_cnt = "", ls_sr_REF_SER = "", ls_sr_REF_NO = "", lc_SR_ADJ_AMT = "", lc_SR_REF_BAL_AMT = "", lc_SR_mrp_value__adj = "";

		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		//ValidatorEJB vdt=new ValidatorEJB();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		//FinCommon finCommon = new FinCommon();
		Timestamp currDate = null;
		UtilMethods utilMethods = UtilMethods.getInstance();
		StringBuffer xmlBuff = null;
		String objName= "";
		boolean lb_flag;
		String siteType="",creatInvOthlist="",creatInvOth="",otherSite="",retString1="",refDate="" ;     //added by manish mhatre
		try
		{
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			xmlBuff = new StringBuffer();
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			transer = "MDRCRC";
			trnofld = "tran_id";

			ls_posttype =  finCommon.getFinparams("999999", "SALES_INV_POST_HDR", conn);
			if ("NULLFOUND".equalsIgnoreCase(ls_posttype))
			{
				ls_posttype = "H";
			}

			sql = "select case when (process_fee) is null then 0 else ( process_fee) end from sreturn where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, as_tranid_from); 
			rs = pstmt.executeQuery();
			if(rs.next())
			{	
				lc_process_fee = rs.getDouble(1);
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
			if ("D".equalsIgnoreCase(ls_ret_opt))
			{
				ls_drcr_flag = "D";		
				transer = "MDRCRD";
				ls_tranwin = "W_MISC_DRCR_RCP_DR";
				objName = "misc_drcr_rcp_dr";
				//ls_auto_tran_id = generateTranId("w_misc_drcr_rcp_dr",as_site_code,ls_drcr_flag,currDateStr,conn);	
				//remarks = "Auto Generated Debit Note for Sales Return " + ls_tran_id;
			}
			else
			{
				ls_drcr_flag = "C";			
				transer = "MDRCRC";
				ls_tranwin = "W_MISC_DRCR_RCP_CR";
				objName = "misc_drcr_rcp_cr";
				//	ls_auto_tran_id = generateTranId("w_misc_drcr_rcp_cr",as_site_code,ls_drcr_flag,currDateStr,conn);	
				//	remarks = "Auto Generated Credit Note for Sales Return " + ls_tran_id;
			}

			lb_flag = false;
			sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where upper(tran_window) = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_tranwin );
			rs = pstmt.executeQuery();
			if(rs.next())
			{	
				keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
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

			if(keystr == null || keystr.length() == 0)
			{
				sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{	
					keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
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
			//System.out.println("ls_tran_id ["+ls_tran_id+"]");

			////////////////////////////////
			//System.out.println("ls_cust_code ["+ls_cust_code+"]");



			sql = "select a.cust_code__bill, a.eff_date, a.curr_code, a.exch_rate, a.anal_code, a.emp_code__aprv, a.item_ser, b.tran_id, b.quantity, " +
					" case when (b.net_amt) is null then 0 else (b.net_amt) end , b.tax_chap, b.tax_class, b.tax_env, b.quantity__stduom, b.rate__stduom, " +
					" b.discount,case when ( b.tax_amt) is null then 0 else ( b.tax_amt) end, b.reas_code, b.line_no, a.ret_opt, a.tran_type, b.item_code, " +
					" a.adj_misc_crn, b.lot_no, b.line_no__invtrace, b.lot_sl,a.ret_ref, b.unit,a.remarks from 	sreturn a , sreturndet b where a.tran_id = b.tran_id " +
					" and a.tran_id >= ? and a.tran_id <= ? and a.tran_date >= ? and a.tran_date <= ? and a.cust_code >= ? and a.cust_code <= ? and a.site_code = ?" +
					" and a.tran_type = ? and a.confirmed = 'Y' and a.ret_opt in ('C','D') and b.invoice_id is null and a.tran_id__crn is null order by a.cust_code, a.item_ser, a.tran_date, a.tran_id " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,as_tranid_from );
			pstmt.setString(2,as_tranid_to );
			pstmt.setDate(3, (java.sql.Date) adt_tran_date_from);
			pstmt.setDate(4,(java.sql.Date) adt_tran_date_to );
			pstmt.setString(5,as_cust_code_from );
			pstmt.setString(6,as_cust_code_to );
			pstmt.setString(7,as_site_code );
			pstmt.setString(8,as_tran_type );
			rs = pstmt.executeQuery();
			while(rs.next())
			{	
				ll_line_no ++;
				ls_cust_code = rs.getString(1);
				ld_eff_date = rs.getTimestamp(2);
				ls_curr_code = checkNullAndTrim(rs.getString(3));
				lc_exch_rate = rs.getDouble(4);
				ls_anal_code = checkNullAndTrim(rs.getString(5));
				ls_emp_code__aprv = checkNullAndTrim(rs.getString(6));
				ls_item_ser = checkNullAndTrim(rs.getString(7));
				ls_tran_id = checkNullAndTrim(rs.getString(8));
				lc_quantity = checkNullAndTrim(rs.getString(9));
				lc_net_amt = rs.getDouble(10);
				ls_tax_chap = checkNullAndTrim(rs.getString(11));
				ls_tax_class = checkNullAndTrim(rs.getString(12));
				ls_tax_env = checkNullAndTrim(rs.getString(13));
				lc_qty_stduom = rs.getDouble(14);
				lc_rate_stduom = rs.getDouble(15);
				lc_discount = rs.getDouble(16);
				lc_tax_amt = rs.getDouble(17);
				ls_reas_code = checkNullAndTrim(rs.getString(18));
				ll_line_no_sret = checkNullAndTrim(rs.getString(19));
				ls_ret_opt = checkNullAndTrim(rs.getString(20));
				ls_trantype = checkNullAndTrim(rs.getString(21));
				ls_item_code = checkNullAndTrim(rs.getString(22));
				ls_adj_misc_crn = checkNullAndTrim(rs.getString(23));
				ls_lot_no = checkNull(rs.getString(24));
				ll_line_no_invtrace = checkNullAndTrim(rs.getString(25));
				ls_lotsl = checkNullAndTrim(rs.getString(26));
				ls_ret_ref = checkNullAndTrim(rs.getString(27));
				ls_unit = checkNullAndTrim(rs.getString(28));
				remarks=rs.getString(29);   
				////////
				//System.out.println("ls_tran_id ["+ls_tran_id+"]");
				//System.out.println("ls_cust_code ["+ls_cust_code+"]");
				////////////// Added arun 
				sql = "select acct_code__pfee, cctr_code__pfee from itemser where item_ser = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,ls_item_ser );
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{	
					ls_acct_code__pfee = checkNullAndTrim(rs1.getString("acct_code__pfee"));
					//ls_cctr_code__pfee = checkNullAndTrim(rs1.getString("cctr_code__pfee"));
					ls_cctr_code__pfee = rs1.getString("cctr_code__pfee");     //removed checknullandtrim
				}

				if (pstmt1 != null )
				{
					pstmt1.close();
					pstmt1 = null;
				}				
				if (rs1 != null )
				{
					rs1.close();
					rs1 = null;
				}			
				//added by apal for Outbound Integration
				//System.out.println("ls_old_cust_code["+ls_old_cust_code+"]"+"[ls_cust_code"+ls_cust_code+"]"+"ls_old_item_ser["+ls_old_item_ser+"]"+"ls_item_ser["+ls_item_ser+"]");
				if(! (ls_old_cust_code.trim().equalsIgnoreCase(ls_cust_code))  || !(ls_old_item_ser.trim().equalsIgnoreCase(ls_item_ser) ))
				{
					
					sql = "select fin_entity from site where site_code = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, as_site_code);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{	
						ls_fin_entity = checkNullAndTrim(rs1.getString(1));
					}

					if (pstmt1 != null )
					{
						pstmt1.close();
						pstmt1 = null;
					}				
					if (rs1 != null )
					{
						rs1.close();
						rs1 = null;
					}	
					sql = "select acct_code__ar, cctr_code__ar from customer where cust_code = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, ls_cust_code);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{	
						ls_acct_code = checkNullAndTrim(rs1.getString(1));
						//ls_cctr_code = checkNullAndTrim(rs1.getString(2));
						ls_cctr_code = rs1.getString(2);          //removed checknullandtrim
					}

					if (pstmt1 != null )
					{
						pstmt1.close();
						pstmt1 = null;
					}				
					if (rs1 != null )
					{
						rs1.close();
						rs1 = null;
					}


					else if((ls_acct_code == null || ls_cctr_code == null) || (ls_acct_code.length() == 0 || ls_cctr_code.length() == 0)) 
					{
						sql = "select acct_code__ar, cctr_code__ar from itemser where item_ser = ? " ;
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, ls_item_ser);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{	
							ls_acct_code = checkNullAndTrim(rs1.getString(1));
							//ls_cctr_code = checkNullAndTrim(rs1.getString(2));
							ls_cctr_code = rs1.getString(2);       //removed checknullandtrim
						}

						if (pstmt1 != null )
						{
							pstmt1.close();
							pstmt1 = null;
						}				
						if (rs1 != null )
						{
							rs1.close();
							rs1 = null;
						}
					}	
					/*ls_cctr_code = gbf_acct_detr_ttype(ls_itemcode,ls_item_ser,'AR',ls_trantype, as_site_code)
					ls_acct_code = f_get_token(ls_cctr_code,'~t')*/


					if (ls_acct_code == null || ls_acct_code.trim().length() == 0 || ls_cctr_code == null || ls_cctr_code.trim().length() == 0 ) 
					{
						ls_cctr_code = finCommon.getAcctDetrTtype(ls_item_code, ls_item_ser, "AR", as_tran_type, conn);
						String tokens [] = ls_cctr_code.split(",");

						//System.out.println("Length="+tokens.length);

						if ( tokens.length >= 2)
						{
							ls_acct_code = tokens[0];
							ls_cctr_code = tokens[1];

							ls_acct_code = checkNullAndTrim(ls_acct_code);
							//ls_cctr_code = checkNullAndTrim(ls_cctr_code);
							ls_cctr_code = ls_cctr_code;        //removed checknullandtrim
						}
						else
						{
							ls_acct_code = ls_cctr_code.substring(0,ls_cctr_code.indexOf(","));
							ls_cctr_code = ls_cctr_code.substring(ls_cctr_code.indexOf(",") + 1);
						}
						tokens = null;
					}


					/*ls_det_cctr = gbf_acct_detr_ttype(ls_itemcode,ls_item_ser,'SRET', ls_trantype, as_site_code)
					ls_det_acct = f_get_token(ls_det_cctr,'~t')*/

					ls_finpara =  finCommon.getFinparams("999999", "INVOICE_DRCR_ADJ", conn);
					if("NULLFOUND".equalsIgnoreCase(ls_finpara))
					{
						ls_finpara = "N" ;
					}

					ls_rndstr = transer + "-RND";
					ls_rndoff =  finCommon.getFinparams("999999", ls_rndstr, conn);

					if (!"NULLFOUND".equalsIgnoreCase(ls_rndoff)) 
					{
						ls_rndoff = ls_rndoff.trim();
					}
					ls_rndstr = transer + "-RNDTO";
					ls_rndTo =  finCommon.getFinparams("999999", ls_rndstr, conn);
					if (!"NULLFOUND".equalsIgnoreCase(ls_rndTo)) 
					{
						ls_rndTo = ls_rndTo.trim();
					}

					/*	System.out.println("ls_fin_entity["+ls_fin_entity+"]");
					sql = "insert into MISC_DRCR_RCP(TRAN_ID, TRAN_SER, TRAN_DATE, EFF_DATE, FIN_ENTITY, SITE_CODE, SUNDRY_TYPE, SUNDRY_CODE, ACCT_CODE, CCTR_CODE, AMOUNT, CURR_CODE," +
					" EXCH_RATE, REMARKS, DRCR_FLAG, CHG_USER, CHG_DATE, CHG_TERM, EMP_CODE__APRV, DUE_DATE, TRAN_TYPE, ITEM_SER, AMOUNT__BC, SRETURN_NO, ADJ_MISC_CRN, CUST_REF_NO, RND_OFF, RND_TO,confirmed)" +
					" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,? ,?,?,?,?) " ; 
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString( 1, ls_auto_tran_id); 
					pstmt1.setString( 2, transer ); 
					pstmt1.setTimestamp( 3, adt_credit_note ); 
					pstmt1.setTimestamp( 4, ld_eff_date );
					pstmt1.setString( 5, ls_fin_entity );
					pstmt1.setString( 6, as_site_code );
					pstmt1.setString( 7, "C" );
					pstmt1.setString( 8, ls_cust_code );
					pstmt1.setString( 9, ls_acct_code );
					pstmt1.setString( 10, ls_cctr_code ); 
					pstmt1.setDouble( 11, (lc_tot_net_amt + lc_process_fee) ); 
					pstmt1.setString( 12, ls_curr_code ); 
					pstmt1.setDouble( 13, lc_exch_rate ); 
					pstmt1.setString( 14, remarks );
					pstmt1.setString( 15, ls_drcr_flag);
					pstmt1.setString( 16, chgUser );
					pstmt1.setTimestamp( 17, currDate ); 
					pstmt1.setString( 18, chgTerm );
					pstmt1.setString( 19, ls_emp_code__aprv );
					pstmt1.setTimestamp( 20, ld_eff_date );
					pstmt1.setString( 21, as_tran_type);
					pstmt1.setString( 22, ls_item_ser ); 
					pstmt1.setDouble( 23, (lc_tot_net_amt + lc_process_fee) * lc_exch_rate ); 
					pstmt1.setString( 24, ls_tran_id );
					pstmt1.setString( 25, ls_adj_misc_crn );
					pstmt1.setString( 26, ls_ret_ref );
					pstmt1.setString( 27, ls_rndoff );
					pstmt1.setString( 28, ls_rndTo );
					pstmt1.setString( 29, "N" );

					System.out.println("ls_fin_entity["+ls_fin_entity+"]");
					//int cnt=pstmt1.executeUpdate();
					if(pstmt1 != null)
					{
						pstmt1.close();pstmt1 = null;
					}*/
					if(ll_line_no==1)//condition  added by nandkumar gadkari on 13-03-20 
					{
						xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
						xmlBuff.append("<description>Datawindow Root</description>");
						xmlBuff.append("<group0>");
						xmlBuff.append("<description>Group0 description</description>");
						xmlBuff.append("<Header0>");
						xmlBuff.append("<objName><![CDATA[").append("misc_drcr_rcp_cr").append("]]></objName>");		
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
						xmlBuff.append("<description>Header0 members</description>");


						/*xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\""+objName+"\" objContext=\"1\">");  
						xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
						xmlBuff.append("<tran_id/>");
						xmlBuff.append("<tran_ser><![CDATA["+ tranSer +"]]></tran_ser>");
						xmlBuff.append("<tran_date><![CDATA["+ sdfAppl.format(currDate).toString() +"]]></tran_date>");
						xmlBuff.append("<eff_date><![CDATA["+ sdfAppl.format(currDate).toString() +"]]></eff_date>");
						xmlBuff.append("<fin_entity><![CDATA["+ ls_fin_entity   +"]]></fin_entity>");
						xmlBuff.append("<site_code><![CDATA["+as_site_code +"]]></site_code>");
						xmlBuff.append("<sundry_type><![CDATA["+ "C"  +"]]></sundry_type>");
						xmlBuff.append("<sundry_code><![CDATA["+ ls_cust_code  +"]]></sundry_code>");
						xmlBuff.append("<acct_code><![CDATA["+checkNull(ls_acct_code)  +"]]></acct_code>");
						xmlBuff.append("<curr_code><![CDATA["+ checkNull(ls_cctr_code) +"]]></curr_code>");
						xmlBuff.append("<amount><![CDATA["+ (lc_tot_net_amt + lc_process_fee) +"]]></amount>");
						xmlBuff.append("<curr_code><![CDATA["+ checkNull(ls_curr_code ) +"]]></curr_code>");
						xmlBuff.append("<exch_rate><![CDATA["+ lc_exch_rate +"]]></exch_rate>");
						xmlBuff.append("<drcr_flag><![CDATA["+ "C"   +"]]></drcr_flag>");
						xmlBuff.append("<tran_id__rcv><![CDATA["+ ""   +"]]></tran_id__rcv>");
						xmlBuff.append("<confirmed><![CDATA["+ "N"   +"]]></confirmed>");
						xmlBuff.append("<chg_user><![CDATA["+ chgUser   +"]]></chg_user>");
						xmlBuff.append("<chg_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></chg_date>");
						xmlBuff.append("<chg_term><![CDATA["+ chgTerm   +"]]></chg_term>");
						xmlBuff.append("<conf_date><![CDATA["+ sdfAppl.format(currDate).toString()   +"]]></conf_date>");
						xmlBuff.append("<emp_code__aprv><![CDATA["+ ls_emp_code__aprv   +"]]></emp_code__aprv>");
						xmlBuff.append("<due_date><![CDATA["+ sdfAppl.format(currDate).toString()   +"]]></due_date>");
						xmlBuff.append("<tran_type><![CDATA["+ checkNull(ls_trantype)   +"]]></tran_type>");
						xmlBuff.append("<item_ser><![CDATA["+ checkNull(ls_item_ser )   +"]]></item_ser>");
						xmlBuff.append("<amount__bc><![CDATA["+  ((lc_tot_net_amt + lc_process_fee) * lc_exch_rate ) +"]]></amount__bc>");
						xmlBuff.append("<sreturn_no><![CDATA["+ checkNull(ls_tran_id)   +"]]></sreturn_no>");
						xmlBuff.append("<adj_misc_crn><![CDATA["+ls_adj_misc_crn+"]]></adj_misc_crn>");
						xmlBuff.append("<adj_amount><![CDATA["+ 0.0   +"]]></adj_amount>");
						xmlBuff.append("<parent__tran_id><![CDATA["+ ""   +"]]></parent__tran_id>");
						xmlBuff.append("<rev__tran><![CDATA["+ ""   +"]]></rev__tran>");
						xmlBuff.append("<round_adj><![CDATA["+ 0.0   +"]]></round_adj>");
						xmlBuff.append("<cust_ref_no><![CDATA["+  ls_ret_ref  +"]]></cust_ref_no>");
						xmlBuff.append("<rnd_off><![CDATA["+ls_rndoff+"]]></rnd_off>");
						xmlBuff.append("<rnd_to><![CDATA["+ls_rndTo+"]]></rnd_to>");
						xmlBuff.append("</Detail1>");*/

						xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"misc_drcr_rcp_cr\">");		
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
						xmlBuff.append("<misc_drcr_rcp_dr/>");
						xmlBuff.append("<tran_id/>");

						xmlBuff.append("<tran_ser>").append("<![CDATA[" + tranSer + "]]>").append("</tran_ser>");
						// 03-dec-2020 manoharan to create credit note as per transaction date of sales return
						//xmlBuff.append("<tran_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</tran_date>");
						xmlBuff.append("<tran_date>").append("<![CDATA[" + sdfAppl.format(ld_eff_date).toString() + "]]>").append("</tran_date>");
						xmlBuff.append("<fin_entity>").append("<![CDATA[" + ls_fin_entity + "]]>").append("</fin_entity>");
						xmlBuff.append("<site_code>").append("<![CDATA[" + as_site_code + "]]>").append("</site_code>");
						xmlBuff.append("<sundry_type>").append("<![CDATA[" + "C" + "]]>").append("</sundry_type>");
						xmlBuff.append("<sundry_code>").append("<![CDATA[" + ls_cust_code + "]]>").append("</sundry_code>");
						xmlBuff.append("<cust_code>").append("<![CDATA[" + ls_cust_code + "]]>").append("</cust_code>");
						xmlBuff.append("<cust_code__dlv>").append("<![CDATA[" + ls_cust_code + "]]>").append("</cust_code__dlv>");
						xmlBuff.append("<acct_code>").append("<![CDATA[" + ls_acct_code + "]]>").append("</acct_code>");
						xmlBuff.append("<cctr_code>").append("<![CDATA[" + ls_cctr_code + "]]>").append("</cctr_code>");
						xmlBuff.append("<amount>").append("<![CDATA[" + (lc_net_amt + lc_process_fee) + "]]>").append("</amount>");
						xmlBuff.append("<curr_code>").append("<![CDATA[" + checkNull(ls_curr_code ) + "]]>").append("</curr_code>");
						xmlBuff.append("<exch_rate>").append("<![CDATA[" + lc_exch_rate + "]]>").append("</exch_rate>");	
						//added by manish mhatre on 7-feb-2019
						//start manish
						if ((remarks!=null)&& (remarks.trim().length() > 0 ))
						{
							remarks = remarks.trim() + " " + ls_tran_id;
						}
						else
						{
							if ("D".equalsIgnoreCase(ls_ret_opt))
							{	
								remarks = "Auto Generated Debit Note for Sales Return " + ls_tran_id;
							}
							else
							{	
								remarks = "Auto Generated Credit Note for Sales Return " + ls_tran_id;
							}
						} 
						//remarks=remarks.substring(0, remarks.length()-1);
						remarks=utilMethods.left(remarks, 60);
						//end manish
						xmlBuff.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");
						//xmlBuff.append("<remarks>").append("<![CDATA[" + "Against contract id:"+contractId+" from "+fromDateStr+" to "+toDateStr+" "+"]]>").append("</remarks>");
						xmlBuff.append("<drcr_flag>").append("<![CDATA[" + "C" + "]]>").append("</drcr_flag>");

						//xmlBuff.append("<tran_id__rcv>").append("<![CDATA[" + "" + "]]>").append("</tran_id__rcv>");
						xmlBuff.append("<confirmed>").append("<![CDATA[" + "N" + "]]>").append("</confirmed>");
						xmlBuff.append("<due_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</due_date>");
						xmlBuff.append("<item_ser>").append("<![CDATA[" + checkNull(ls_item_ser ) + "]]>").append("</item_ser>");
						xmlBuff.append("<tran_type>").append("<![CDATA[" + checkNull(ls_trantype) + "]]>").append("</tran_type>");
						xmlBuff.append("<amount__bc>").append("<![CDATA[" + ((lc_net_amt + lc_process_fee) * lc_exch_rate ) + "]]>").append("</amount__bc>");
						xmlBuff.append("<rnd_off>").append("<![CDATA[" + ls_rndoff + "]]>").append("</rnd_off>");
						xmlBuff.append("<rnd_to>").append("<![CDATA[" + ls_rndTo + "]]>").append("</rnd_to>");			
						//added by monika 24 dec 2019-to add sreturn no

						xmlBuff.append("<sreturn_no>").append("<![CDATA[" + ls_tran_id + "]]>").append("</sreturn_no>");			

						xmlBuff.append("</Detail1>");
						xmlBuff.append("\n");
					}
				}
				/*xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append(objName).append("]]></objName>");  
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
				xmlBuff.append("<description>").append("Header0 members").append("</description>");	*/




				//System.out.println("ls_tran_id ["+ls_tran_id+"]");
				//System.out.println("ls_cust_code ["+ls_cust_code+"]");
				ls_old_cust_code = ls_cust_code;
				ls_old_item_ser = ls_item_ser;
				//System.out.println("ls_old_cust_code["+ls_cust_code+"]"+"[ls_old_item_ser["+ls_item_ser+"]");
				////////
				if((ls_acct_code__pfee == null || ls_acct_code__pfee.length()== 0) || (ls_cctr_code__pfee == null || ls_cctr_code__pfee.length() == 0) )
				{
					/*ls_cctr_code__pfee =  gbf_acct_detr_ttype(ls_itemcode,ls_itemser,'SRET', ls_trantype, as_site_code)
					ls_acct_code__pfee =  f_get_token(ls_cctr_code__pfee,'~t')*/

					ls_cctr_code__pfee = finCommon.getAcctDetrTtype(ls_item_code, ls_item_ser, "SRET", ls_trantype, conn);
					if (ls_cctr_code__pfee != null && ls_cctr_code__pfee.trim().length() > 0) 
					{
						String tokens [] = ls_cctr_code__pfee.split(",");

						//System.out.println("Length="+tokens.length);

						if ( tokens.length >= 2)
						{
							ls_acct_code__pfee = tokens[0];
							ls_cctr_code__pfee = tokens[1];

							ls_acct_code__pfee = checkNullAndTrim(ls_acct_code__pfee);
							//ls_cctr_code__pfee = checkNullAndTrim(ls_cctr_code__pfee);
							ls_cctr_code__pfee = ls_cctr_code__pfee;   //removed checknullandtrim
						}
						else
						{
							ls_acct_code__pfee = ls_cctr_code__pfee.substring(0,ls_cctr_code__pfee.indexOf(","));
							ls_cctr_code__pfee = ls_cctr_code__pfee.substring(ls_cctr_code__pfee.indexOf(",") + 1);
						}
						tokens = null;
					}

				}
				ls_det_cctr = finCommon.getAcctDetrTtype(ls_item_code, ls_item_ser, "SRET", ls_trantype, conn);
				if (ls_det_cctr != null && ls_det_cctr.trim().length() > 0) 
				{
					String tokens [] = ls_det_cctr.split(",");

					//System.out.println("Length="+tokens.length);

					if ( tokens.length >= 2)
					{
						ls_det_acct = tokens[0];
						ls_det_cctr = tokens[1];

						ls_det_acct = checkNullAndTrim(ls_det_acct);
						//ls_det_cctr = checkNullAndTrim(ls_det_cctr);
						ls_det_cctr = ls_det_cctr;         //removed checknullandtrim
					}
					else
					{
						ls_det_acct = ls_det_cctr.substring(0,ls_det_cctr.indexOf(","));
						ls_det_cctr = ls_det_cctr.substring(ls_det_cctr.indexOf(",") + 1);
					}
					tokens = null;
				}
				//// ended arun 

				/*
				sql = "INSERT INTO MISC_DRCR_RDET(TRAN_ID, LINE_NO, ACCT_CODE, CCTR_CODE, AMOUNT, NET_AMT, TAX_AMT, TAX_CLASS, TAX_CHAP, TAX_ENV, REAS_CODE, ITEM_CODE, QUANTITY, RATE, LINE_NO__SRET, LINE_NO__INVTRACE, LOT_NO, LOT_SL, UNIT)" +
						" values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, ls_auto_tran_id); 
				pstmt1.setInt(2, ll_line_no ); 
				pstmt1.setString( 8, ls_tax_class );
				pstmt1.setString( 9, ls_tax_chap);
				pstmt1.setString( 10, ls_tax_env);
				 */
				/*xmlBuff.append("<Detail2 dbID=\"\" domID=\"1\" objName=\""+objName+"\" objContext=\"2\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");	*/

				xmlBuff.append("<Detail2 dbID=\"\" domID=\""+ll_line_no+"\" objContext=\"2\" objName=\"misc_drcr_rcp_cr\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
				xmlBuff.append("<misc_drcr_rcp_dr/>");

				//xmlBuff.append("<line_no><![CDATA["+ ll_line_no+"]]></line_no>");

				xmlBuff.append("<line_no>").append("<![CDATA[" + ll_line_no + "]]>").append("</line_no>");

				ls_process_fee_reason = distCommon.getDisparams("999999","PROCESS_FEE_REASON",conn) ;
				if ("NULLFOUND".equalsIgnoreCase(ls_process_fee_reason) || ls_process_fee_reason == null || ls_process_fee_reason.trim().length() == 0)
				{
					ls_process_fee_reason = " ";
				}
				if( lc_process_fee != 0)
				{
					/*
					pstmt1.setString( 3, ls_acct_code__pfee ); 
					pstmt1.setString( 4, ls_cctr_code__pfee );
					pstmt1.setDouble( 5, lc_process_fee );
					pstmt1.setDouble( 6, lc_process_fee );
					pstmt1.setString( 11, ls_process_fee_reason); 
					 */
					/*xmlBuff.append("<acct_code><![CDATA["+ ls_acct_code__pfee   +"]]></acct_code>");
					xmlBuff.append("<cctr_code><![CDATA["+ ls_cctr_code__pfee   +"]]></cctr_code>");
					xmlBuff.append("<amount><![CDATA["+ lc_process_fee   +"]]></amount>");
					xmlBuff.append("<net_amt><![CDATA["+ lc_process_fee   +"]]></net_amt>");
					xmlBuff.append("<reas_code><![CDATA["+ lc_process_fee   +"]]></reas_code>");*/

					xmlBuff.append("<acct_code>").append("<![CDATA[" + ls_acct_code__pfee + "]]>").append("</acct_code>");
					xmlBuff.append("<cctr_code>").append("<![CDATA[" + ls_cctr_code__pfee + "]]>").append("</cctr_code>");
					xmlBuff.append("<amount>").append("<![CDATA[" + lc_process_fee + "]]>").append("</amount>");
					xmlBuff.append("<net_amt>").append("<![CDATA[" + lc_process_fee + "]]>").append("</net_amt>");
					xmlBuff.append("<reas_code>").append("<![CDATA[" + ls_process_fee_reason + "]]>").append("</reas_code>");


				}	
				else
				{
					/*
					pstmt1.setString( 3, ls_det_acct ); 
					pstmt1.setString( 4, ls_det_cctr );
					pstmt1.setString( 11, ls_reas_code);
					*/
					xmlBuff.append("<acct_code><![CDATA["+ ls_det_acct   +"]]></acct_code>");
					xmlBuff.append("<cctr_code><![CDATA["+ ls_det_cctr   +"]]></cctr_code>");
					xmlBuff.append("<reas_code><![CDATA["+ ls_reas_code   +"]]></reas_code>");

					xmlBuff.append("<acct_code>").append("<![CDATA[" + ls_det_acct + "]]>").append("</acct_code>");
					xmlBuff.append("<cctr_code>").append("<![CDATA[" + ls_det_cctr + "]]>").append("</cctr_code>");
					xmlBuff.append("<reas_code>").append("<![CDATA[" + ls_reas_code + "]]>").append("</reas_code>");
				}
				
				lc_drcr_amt = (lc_qty_stduom * lc_rate_stduom) - ((lc_qty_stduom * lc_rate_stduom) * lc_discount / 100)	;
				// 22-oct-2020 manoharan to round the amount to 3 decimals
				lc_drcr_amt = Double.parseDouble(UtilMethods.getInstance().getReqDecString(lc_drcr_amt, 3));
				//System.out.println("lc_drcr_amt("+lc_drcr_amt+") = ("+lc_qty_stduom+" * "+lc_qty_stduom+") - ("+lc_qty_stduom+"*"+lc_rate_stduom+") * "+lc_discount+"/ 100");
				System.out.println("Sreturndet line---> [" + ll_line_no_sret+ "] lc_drcr_amt [" + lc_drcr_amt + "] lc_tax_amt [" + lc_tax_amt + "] lc_discount [" + lc_discount + "]");
				//if (lc_net_amt >= 0 ) // 22-oct-2020 manoharan commented 
				if (lc_drcr_amt != 0 )// 22-oct-2020 manoharan added based on above calculation of lc_drcr_amt as in case discount is 100 amount calculated will be 0
				{
					//pstmt1.setDouble( 5, lc_drcr_amt );
					//pstmt1.setDouble( 6, lc_net_amt );
					xmlBuff.append("<amount>").append("<![CDATA[" + lc_drcr_amt + "]]>").append("</amount>");
					xmlBuff.append("<net_amt>").append("<![CDATA[" + lc_net_amt + "]]>").append("</net_amt>");
				}
				else
				{
					lc_net_amt = 0;
					xmlBuff.append("<amount>").append("<![CDATA[0.00]]>").append("</amount>");
					xmlBuff.append("<net_amt>").append("<![CDATA[0.00]]>").append("</net_amt>");
				}
				/*
				pstmt1.setDouble( 7, lc_tax_amt );
				pstmt1.setString( 12, ls_item_code);
				pstmt1.setDouble( 13, lc_qty_stduom);
				pstmt1.setDouble( 14, lc_rate_stduom);
				pstmt1.setString( 15, ll_line_no_sret);
				pstmt1.setString( 16, ll_line_no_invtrace);
				pstmt1.setString( 17, ls_lot_no);
				pstmt1.setString( 18, ls_lotsl);
				pstmt1.setString( 19, ls_unit);
				*/
				lc_tot_net_amt = Double.parseDouble(UtilMethods.getInstance().getReqDecString(lc_tot_net_amt, 3)); // 22-oct-20 added by manoharan to avoid jvm floating point issue
				lc_tot_net_amt = lc_tot_net_amt + lc_net_amt	;

				/*				xmlBuff.append("<tax_amt><![CDATA["+ (lc_tax_amt)   +"]]></tax_amt>");
				xmlBuff.append("<item_code><![CDATA["+ (lc_tax_amt)   +"]]></item_code>");
				xmlBuff.append("<quantity><![CDATA["+ (lc_tax_amt)   +"]]></quantity>");
				xmlBuff.append("<rate><![CDATA["+ (lc_tax_amt)   +"]]></rate>");
				xmlBuff.append("<line_no__sret><![CDATA["+ (ll_line_no_sret)+"]]></line_no__sret>");
				xmlBuff.append("<line_no__invtrace><![CDATA["+(ll_line_no_invtrace)+"]]></line_no__invtrace>");
				xmlBuff.append("<lot_no><![CDATA["+ ls_lot_no+"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+ ls_lotsl +"]]></lot_sl>");
				xmlBuff.append("<unit><![CDATA["+ ls_unit +"]]></unit>");
				xmlBuff.append("</Detail2>");
				 */				

				xmlBuff.append("<tax_amt>").append("<![CDATA[" + lc_tax_amt + "]]>").append("</tax_amt>");
				xmlBuff.append("<item_code>").append("<![CDATA[" + ls_item_code + "]]>").append("</item_code>");
				xmlBuff.append("<quantity>").append("<![CDATA[" + lc_qty_stduom + "]]>").append("</quantity>");
				xmlBuff.append("<rate>").append("<![CDATA[" + lc_rate_stduom + "]]>").append("</rate>");
				xmlBuff.append("<line_no__sret>").append("<![CDATA[" + ll_line_no_sret + "]]>").append("</line_no__sret>");
				xmlBuff.append("<lot_no>").append("<![CDATA[" + ls_lot_no + "]]>").append("</lot_no>");
				xmlBuff.append("<lot_sl>").append("<![CDATA[" + ls_lotsl + "]]>").append("</lot_sl>");
				xmlBuff.append("<unit>").append("<![CDATA[" + ls_unit + "]]>").append("</unit>");
				xmlBuff.append("<line_no__invtrace>").append("<![CDATA[" + ll_line_no_invtrace + "]]>").append("</line_no__invtrace>");
				xmlBuff.append("<tax_class><![CDATA["+ checkNull(ls_tax_class) +"]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA["+ checkNull(ls_tax_chap) +"]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA["+ checkNull(ls_tax_env) +"]]></tax_env>");
				//added by monika 24 dec 2019-	to set SRETURN_NO

				xmlBuff.append("<sreturn_no>").append("<![CDATA[" + ls_tran_id + "]]>").append("</sreturn_no>");			

				xmlBuff.append("</Detail2>");
				xmlBuff.append("\n");		



				//int detCnt = pstmt1.executeUpdate();
				/*
				if(pstmt1 != null)
				{
					pstmt1.close();pstmt1 = null;
				}
				*/
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

			///////////////////// arun <Detail3 dbID=\"\" domID=\"1\" objName=\"
			//System.out.println("<<-----------code calling ------------->>");

			sql="update MISC_DRCR_RCP set amount =? , AMOUNT__BC = ? where tran_id = ?";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setDouble(1, lc_tot_net_amt + lc_process_fee);
			pstmt1.setDouble(2, (lc_tot_net_amt + lc_process_fee) * lc_exch_rate );
			pstmt1.setString(3,ls_auto_tran_id );
			//System.out.println("ls_auto_tran_id["+ls_auto_tran_id+"]");
			int s=pstmt1.executeUpdate();
			if(s>0)
			{
				System.out.println("<-----------set the amount");
			}
			else
			{
				System.out.println("<----------- not set the amount");
			}


			//System.out.println("<<-----------code ending------------->>");

			////////////
			//	}//added by monika 21 nov 19

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			String xmlString = xmlBuff.toString().trim().replaceFirst("^([\\W]+)<","<");
			String drNtTranId="";
			//System.out.println("Just before saving misc_drcr_rcp---> [" + xmlString + "]");
			
			errorString = saveData(xtraParams,as_site_code,xmlString,conn);
			//System.out.println("@@@@@2: retString:"+errorString);
			//System.out.println("--retString finished--");
			if (errorString.indexOf("Success") > -1)
			{
				//System.out.println("@@@@@@3: Success"+errorString);
				Document dom = genericUtility.parseString(errorString);
				//System.out.println("dom>>>"+dom);
				drNtTranId = genericUtility.getColumnValue("TranID",dom);
			}
			else
			{
				System.out.println("[SuccessSuccess" + errorString + "]");	
				conn.rollback();
				return errorString;
			}

			sql = "update sreturn set tran_id__crn = ? where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, drNtTranId);
			pstmt.setString(2, ls_tran_id);
			int rowcnt = pstmt.executeUpdate();
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}

			if( rowcnt == 0 )
			{
				//ls_errcode = "DS000NR";
			}

			/*if(("MI".equalsIgnoreCase(ls_adj_misc_crn) || "MC".equalsIgnoreCase(ls_adj_misc_crn)) && (ls_prevtranid != ls_tran_id))
			{
			 */	sql = "SELECT TRAN_ID,LINE_NO,REF_SER,REF_NO,ADJ_AMT,REF_BAL_AMT,mrp_value__adj FROM SRETURN_INV WHERE TRAN_ID = ? " ;
			 pstmt = conn.prepareStatement(sql);
			 pstmt.setString(1, ls_tran_id);
			 rs = pstmt.executeQuery();

			 while(rs.next())
			 {	
				 ls_temp = checkNullAndTrim(rs.getString(1));
				 li_Sreturn_cnt = checkNullAndTrim(rs.getString(2));
				 ls_sr_REF_SER = checkNullAndTrim(rs.getString(3));
				 ls_sr_REF_NO = checkNullAndTrim(rs.getString(4));
				 lc_SR_ADJ_AMT = checkNullAndTrim(rs.getString(5));
				 lc_SR_REF_BAL_AMT = checkNullAndTrim(rs.getString(6));
				 lc_SR_mrp_value__adj = checkNullAndTrim(rs.getString(7));

				 sql = "insert into misc_drcr_rcpinv(TRAN_ID, LINE_NO, REF_SER, REF_NO, REF_BAL_AMT, ADJ_AMT, MRP_VALUE__ADJ) "
						 +" values (?,?,?,?,?,?,?)";
				 pstmt1 = conn.prepareStatement(sql);
				 pstmt1.setString( 1, ls_auto_tran_id); 
				 pstmt1.setString( 2, li_Sreturn_cnt ); 
				 pstmt1.setString( 3, ls_sr_REF_SER ); 
				 pstmt1.setString( 4, ls_sr_REF_NO );
				 pstmt1.setString( 5, lc_SR_REF_BAL_AMT );
				 pstmt1.setString( 6, lc_SR_ADJ_AMT );
				 pstmt1.setString( 7, lc_SR_mrp_value__adj );
				 //int detCnt = pstmt1.executeUpdate();
				 if(pstmt1 != null)
				 {
					 pstmt1.close();pstmt1 = null;
				 }

				 xmlBuff.append("<Detail3 dbID=\"\" domID=\"1\" objName=\""+objName+"\" objContext=\"3\">");
				 xmlBuff.append("<line_no><![CDATA["+ li_Sreturn_cnt   +"]]></line_no>");
				 xmlBuff.append("<ref_ser><![CDATA["+ li_Sreturn_cnt   +"]]></ref_ser>");
				 xmlBuff.append("<ref_no><![CDATA["+ li_Sreturn_cnt   +"]]></ref_no>");
				 xmlBuff.append("<ref_bal_amt><![CDATA["+ li_Sreturn_cnt   +"]]></ref_bal_amt>");
				 xmlBuff.append("<adj_amt><![CDATA["+ li_Sreturn_cnt   +"]]></adj_amt>");
				 xmlBuff.append("<mrp_val__adj><![CDATA["+ li_Sreturn_cnt   +"]]></mrp_val__adj>");
				 xmlBuff.append("</Detail3>");

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
			 /*}
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			String xmlString = xmlBuff.toString().trim().replaceFirst("^([\\W]+)<","<");
			String drNtTranId="";
			errorString = saveData(xtraParams,as_site_code,xmlString,conn);
			System.out.println("@@@@@2: retString:"+errorString);
			System.out.println("--retString finished--");
			if (errorString.indexOf("Success") > -1)
			{
				System.out.println("@@@@@@3: Success"+errorString);
				Document dom = genericUtility.parseString(errorString);
				System.out.println("dom>>>"+dom);
				drNtTranId = genericUtility.getColumnValue("TranID",dom);
			}
			else
			{
				System.out.println("[SuccessSuccess" + errorString + "]");	
				conn.rollback();
				return errorString;
			}

			sql = "update sreturn set tran_id__crn = ? where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, drNtTranId);
			pstmt.setString(2, ls_tran_id);
			int rowcnt = pstmt.executeUpdate();
			if (pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}

			if( rowcnt == 0 )
			{
				//ls_errcode = "DS000NR";
			}
			  */
			 /*  DrCrRcpConf drcrObj = new DrCrRcpConf();
			  errorString = drcrObj.confirm (ls_auto_tran_id, xtraParams, "" , conn);
			  System.out.println("After DrCrRcpConf---->["+errorString+"]");
			  */

			 // added arun  start 
			 MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
			 errorString= confDebitNote.confirm(drNtTranId,xtraParams, "" , conn);
			 System.out.println("After DrCrRcpConf---->["+errorString+"]");
			 System.out.println("errorString["+errorString+"]");
			 PostOrdInvoicePost postOrdInvoicePost =  new PostOrdInvoicePost();
			 if(errorString != null && errorString.indexOf("CONFSUCCES") != -1)
			 {
				 errorString = "";

				 //added by manish mhatre on 20-jan-2020
				 //start manish
				 sql = " select site_type  from site where site_code = ? "; 
				 pstmt = conn.prepareStatement(sql);
				 pstmt.setString(1, as_site_code);
				 rs = pstmt.executeQuery();
				 if(rs.next())
				 {
					 siteType= checkNullAndTrim(rs.getString(1));
				 }
				 rs.close();
				 rs = null;
				 pstmt.close();
				 pstmt = null;


				 creatInvOthlist = finCommon.getFinparams("999999", "ALOW_INV_OTH_SITE", conn); 

				 if( "NULLFOUND".equalsIgnoreCase(creatInvOthlist) || creatInvOthlist == null)
				 {
					 creatInvOthlist="";
				 }

				 if(creatInvOthlist.trim().length() > 0)
				 {
					 String[] arrStr = creatInvOthlist.split(",");
					 for (int i = 0; i < arrStr.length; i++) {
						 creatInvOth = arrStr[i];
						 System.out.println("creatInvOth>>>>>>>>" + creatInvOth);
						 if(siteType.equalsIgnoreCase(creatInvOth.trim()))
						 {
							 otherSite = finCommon.getFinparams("999999", "INVOICE_OTHER_SITE", conn); 
							 if( !"NULLFOUND".equalsIgnoreCase(creatInvOthlist) && creatInvOthlist != null && creatInvOthlist.trim().length() > 0)
							 {
								 errorString=gbfAutoMiscCrnoteSreturnOth( drNtTranId,  otherSite ,  xtraParams,  conn);
								 if( errorString != null && errorString.trim().length() > 0 )
								 {
									 return errorString;
								 }
							 }
						 }

					 }	
				 }  //end manish

			 }

			 //ended arun 

			 /*if( "Y".equalsIgnoreCase( as_post )  )
			{
				ls_errcode = retrieveMiscDrcrRcp("misc_drcr_rcp_cr",ls_auto_tran_id, xtraParams, "N");
				if (ls_errcode != null && ls_errcode.trim().length() > 0) 
				{
					errorString = vdt.getErrorString("",ls_errcode,"");
					return errorString;
				}
			}*/

		}
		catch(Exception e)
		{			
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errorString;
	}

	//added by manish mhatre on22-jan-2020
	//start manish
	public String gbfAutoCrnoteSreturnOth (String tranIdAs,String siteCodeAs,String refDate ,String xtraParams, Connection conn) throws Exception
	{
		String   trnofld="" , finEntity="" , custCode="",acctCode="",cctrCode="" , invoiceId="", tranId="" ;
		String currCode="" ,remarks="",analCode="",drcrFlag="",tranIdRcv="",confirmed="" ,siteCode="";
		String empCodeAprv="",tranType="",tranSer="" , crTerm="" ,itemSer="" ,sreturnNo="",adjRecv="";
		String gpSer="",gpNo="",parentTranId="",revTran="",custRefNo="",rndOff="",rndTo="",sundryType="";
		String sundryCode="",siteCodeDrcr="",ediStat="" , drcrType="" , errcode="" , keystr="" ,tranwin="" , tranIdNew="";
		String itemCode="" ,salesPers="" , taxChap="" , taxClass="",taxEnv="" ,reasCode="",lotNo="" , lotSl="";
		String tranIdRcp="" , refSer="" , refNo=""  , returnType="" , sretunTypeList="" , sretunType="" ; 
		String stanCode="" , stanCodeSite="";
		String errString="";
		Timestamp chgDate=null , tranDate=null,custRefDate=null ,effDate=null,confDate=null ,gpDate=null , dueDate=null;
		double amount =0 , amountBc = 0 , roundAdj=0 ,custRefAmt=0,ordBillbackAmt=0,ordOffinvAmt=0;
		double lineBillbackAmt=0 , lineOffinvAmt=0 ,drcrAmt=0 ,taxAmt= 0  ,netAmt = 0 ,quantity=0 ;
		double discount=0,billbackAmt=0 ,offinvAmt=0 , refBalAmt=0 , adjAmt=0 , mrpValueAdj=0 , taxAmtdet = 0; 
		double tottax=0 , mtotamt = 0 , totdiscamt = 0 ,totNetAmt = 0;
		double costRate=0 ,rateStd=0 ,rate=0 , rateClg=0;
		double exchRate=0;
		int lineNo=0, lineNoInv=0 ,lineNoSret=0 , lineNoInvtrace=0 , count=0 , i;     //long
		String taxEnvSr="",WINDOW="",parentTranIdAs="",temp="",objName="";
		boolean lbFlag = false;
		Timestamp taxDate=null;
		String chgUser="",chgTerm="";
		String transer = "CRNRCP";
		trnofld = "tran_id";
		//chg_date = datetime(today(),now())
		String sql="",sql1="";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		//ValidatorEJB vdt=new ValidatorEJB();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		//FinCommon finCommon = new FinCommon();
		Timestamp currDate = null;
		UtilMethods utilMethods = UtilMethods.getInstance();
		StringBuffer xmlBuff = null;

		try
		{
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			xmlBuff = new StringBuffer();
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			//chgDate= new java.sql.Timestamp(System.currentTimeMillis()) ;	
			chgDate = new java.sql.Timestamp(new java.util.Date().getTime());

			sql="select tran_date,fin_entity,cust_code,acct_code,cctr_code,eff_date,invoice_id,amount, "+
					" curr_code,exch_rate,remarks,site_code,anal_code,drcr_flag,tran_id__rcv,confirmed,conf_date, "+
					" emp_code__aprv,tran_type,tran_ser,due_date,cr_term,amount__bc,item_ser,round_adj,sreturn_no, "+
					" adj_recv,gp_ser,gp_no,gp_date,parent__tran_id,rev__tran,cust_ref_no,cust_ref_date,cust_ref_amt, "+
					" rnd_off,rnd_to,sundry_type,sundry_code,site_code__drcr,drcr_type,ord_billback_amt,ord_offinv_amt, "+
					" line_billback_amt,line_offinv_amt,edi_stat from drcr_rcp where tran_id = ? ";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranIdAs );							

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				tranDate=rs.getTimestamp("tran_date");
				finEntity=rs.getString("fin_entity");
				custCode=rs.getString("cust_code");
				acctCode=rs.getString("acct_code");
				cctrCode=rs.getString("cctr_code");
				effDate=rs.getTimestamp("eff_date");
				invoiceId=rs.getString("invoice_id");
				amount=rs.getDouble("amount");
				currCode=rs.getString("curr_code");
				exchRate=rs.getDouble("exch_rate");
				remarks=checkNullAndTrim(rs.getString("remarks"));
				siteCode=rs.getString("site_code");
				analCode=checkNullAndTrim(rs.getString("anal_code"));
				drcrFlag=rs.getString("drcr_flag");
				tranIdRcv=checkNullAndTrim(rs.getString("tran_id__rcv"));
				confirmed=rs.getString("confirmed");
				confDate=rs.getTimestamp("conf_date");
				empCodeAprv=rs.getString("emp_code__aprv");
				tranType=rs.getString("tran_type");
				tranSer=rs.getString("tran_ser");
				dueDate=rs.getTimestamp("due_date");
				crTerm=checkNullAndTrim(rs.getString("cr_term"));
				amountBc=rs.getDouble("amount__bc");
				itemSer=rs.getString("item_ser");
				roundAdj=rs.getDouble("round_adj");
				sreturnNo=rs.getString("sreturn_no");
				adjRecv=rs.getString("adj_recv");
				gpSer=checkNullAndTrim(rs.getString("gp_ser"));
				gpNo=rs.getString("gp_no");
				gpDate=rs.getTimestamp("gp_date");
				parentTranId=rs.getString("parent__tran_id");
				revTran=rs.getString("rev__tran");
				//custRefNo=rs.getString("cust_ref_no");
				//	custRefDate=rs.getTimestamp("cust_ref_date");
				//custRefAmt=rs.getDouble("cust_ref_amt");
				rndOff=rs.getString("rnd_off");
				rndTo=rs.getString("rnd_to");
				sundryType=rs.getString("sundry_type");
				sundryCode=rs.getString("sundry_code");
				siteCodeDrcr=rs.getString("site_code__drcr");
				drcrType=rs.getString("drcr_type");
				ordBillbackAmt=rs.getDouble("ord_billback_amt");
				ordOffinvAmt=rs.getDouble("ord_offinv_amt");
				lineBillbackAmt=rs.getDouble("line_billback_amt");
				lineOffinvAmt=rs.getDouble("line_offinv_amt");
				ediStat=rs.getString("edi_stat");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if ("D".equalsIgnoreCase(drcrFlag)) 
			{
				transer = "DRNRCP";
				tranwin = "W_DRCRRCP_DR";
				WINDOW = "T_DRCRRCP_DRHO";
				//tranIdAs = generateTranId("W_DRCRRCP_CR",siteCode,drcrFlag,currDateStr,conn);	
				tranIdNew = generateTranId("w_drcrrcp_cr",siteCodeAs,drcrFlag,currDateStr,tranType,conn);   //added tran type by manish mhatre on 28-feb-2020	
				//System.out.println("tran id auto generated>>>>>"+tranIdNew);

			}
			else if ("C".equalsIgnoreCase(drcrFlag))
			{
				transer = "CRNRCP";
				tranwin = "W_DRCRRCP_CR";
				WINDOW = "T_DRCRRCP_CRHO";
				//tranIdAs = generateTranId("W_DRCRRCP_CR",siteCode,drcrFlag,currDateStr,conn);	
				tranIdNew = generateTranId("w_drcrrcp_cr",siteCodeAs,drcrFlag,currDateStr,tranType,conn);	 //added tran type by manish mhatre on 28-feb-2020
				//System.out.println("tran id auto generated>>>>>"+tranIdNew);

			}

			/*	if ("D".equalsIgnoreCase(drcr_flag)) 
				{
//					transer = 'DRNRCP'
					WINDOW = "T_DRCRRCP_DRHO";
				}
				else if ("C".equalsIgnoreCase(drcr_flag) )
					{
//					transer = 'CRNRCP'
					WINDOW = "T_DRCRRCP_CRHO";
					}*/

			sql="select key_string  from transetup where upper(tran_window) = ? " ;
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, WINDOW);
			rs=pstmt.executeQuery();
			if (rs.next())
			{
				keystr=checkNullAndTrim(rs.getString("key_string"));
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

			if(keystr == null || keystr.length() == 0)
			{
				sql = "select KEY_STRING from transetup where tran_window = 'GENERAL' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{	
					keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
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
			/*String xmlValues = "",mpadjTranid="";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<tran_id/>";
			xmlValues = xmlValues + "<site_code>" + siteCodeAs + "</site_code>";
			xmlValues = xmlValues + "<tran_date>"+ sdf.format(chg_date) + "</tran_date>";
			xmlValues = xmlValues + "<tran_type>"+ sdf.format(tran_type) + "</tran_type>";
			xmlValues = xmlValues + "</Detail1></Root>";

			TransIDGenerator tg = new TransIDGenerator(xmlValues, "SYSTEM", CommonConstants.DB_NAME);
			mpadjTranid = tg.generateTranSeqID(transer , trnofld,keystr, conn);*/


			sql="select count(*) from site_customer where site_code__ch = ? and site_code = ? and channel_partner = 'Y' ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeAs);
			pstmt.setString(2, siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				count=rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(count==0)
			{
				sql="select count(*) from customer where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeAs);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					count=rs.getInt(1);
				}


				if(count > 1)
				{
					errString = itmDBAccessLocal.getErrorString("", "ERRORVTCPC", "","",conn);
					return errString;
				}
				else if(count == 0)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTCUSTCD4", "","",conn);
					return errString;
				}
				else if(count == 1)
				{
					sql1 = "select cust_code from customer where site_code = ? and channel_partner = 'Y'" ;
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1,siteCodeAs);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						custCode = rs1.getString("cust_code");
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
				}
			}

			else if(count == 1)
			{
				sql = "select cust_code from site_customer where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeAs);
				pstmt.setString(2,siteCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					custCode = checkNull(rs.getString("cust_code"));
				}
			}
			if(rs != null) 
			{
				rs.close();rs = null;

			}
			if(pstmt != null) 
			{
				pstmt.close();pstmt = null;
			}	

			sql="select fin_entity from site where site_code= ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeAs);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				finEntity=rs.getString("fin_entity");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			sql="select remarks from sreturn where tran_id= ? ";
			pstmt1=conn.prepareStatement(sql);
			pstmt1.setString(1, sreturnNo);
			rs1=pstmt1.executeQuery();
			if(rs1.next())
			{
				remarks=checkNullAndTrim(rs1.getString("remarks"));
			}
			pstmt1.close();
			pstmt1=null;
			rs1.close();
			rs1=null;


			/*sql="select invoice_id from invoice where parent__tran_id= ? ";
		pstmt=conn.prepareStatement(sql);
		pstmt.setString(1, invoiceId);
		rs=pstmt.executeQuery();
		if(rs.next())
		{
			parentTranIdAs=rs.getString(1);
		}
		pstmt.close();
		pstmt=null;
		rs.close();
		rs=null;
		if(parentTranIdAs== null || parentTranIdAs.trim().length() == 0)
		{
			return errString;
		}*/
			sql=" insert into drcr_rcp (tran_id,tran_date,fin_entity,cust_code,acct_code,cctr_code,eff_date,invoice_id,amount, "
					+" curr_code,exch_rate,remarks,site_code,anal_code,drcr_flag,tran_id__rcv,confirmed, "
					+" emp_code__aprv,tran_type,tran_ser,due_date,cr_term,chg_date,chg_user,chg_term,add_date,add_user, "
					+" add_term,amount__bc,item_ser,round_adj,sreturn_no,adj_recv,gp_ser,gp_no,gp_date,parent__tran_id, "
					+" rev__tran,cust_ref_no,cust_ref_date,cust_ref_amt,rnd_off,rnd_to,sundry_type,sundry_code,site_code__drcr, "
					+" drcr_type,ord_billback_amt,ord_offinv_amt,line_billback_amt,line_offinv_amt,edi_stat) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranIdNew);
			//pstmt.setString(1, tranIdAs);
			pstmt.setTimestamp(2, tranDate);
			pstmt.setString(3, finEntity);
			pstmt.setString(4, custCode);
			pstmt.setString(5, acctCode);
			pstmt.setString(6, cctrCode);
			pstmt.setTimestamp(7, effDate);
			pstmt.setString(8, invoiceId);
			pstmt.setDouble(9, amount);
			pstmt.setString(10, currCode);
			pstmt.setDouble(11, exchRate);
			pstmt.setString(12, remarks);
			pstmt.setString(13, siteCodeAs);
			pstmt.setString(14,analCode);
			pstmt.setString(15, drcrFlag);
			pstmt.setString(16, tranIdRcv);
			pstmt.setString(17, "N");
			pstmt.setString(18, empCodeAprv);
			pstmt.setString(19, tranType);
			pstmt.setString(20, tranSer);
			pstmt.setTimestamp(21, dueDate);
			pstmt.setString(22, crTerm);
			pstmt.setTimestamp(23, chgDate);
			pstmt.setString(24, chgUser);
			pstmt.setString(25, chgTerm);
			pstmt.setTimestamp(26, chgDate);
			pstmt.setString(27, chgUser);
			pstmt.setString(28, chgTerm);
			pstmt.setDouble(29, amountBc);
			pstmt.setString(30, itemSer);
			pstmt.setDouble(31, 0);
			pstmt.setString(32, sreturnNo);  // 16-dec-20 manoharan  Uncommented //commented by manish mhatre on 1-july-20
			//pstmt.setString(32, "");    // 16-dec-20 manoharan  commented //added by manish mhatre[for set main credit note tranid in sale return credit note no]
			pstmt.setString(33, adjRecv);
			pstmt.setString(34, gpSer);
			pstmt.setString(35, gpNo);
			pstmt.setTimestamp(36, gpDate);
			pstmt.setString(37, tranIdAs);
			pstmt.setString(38, revTran);
			pstmt.setString(39, custRefNo);
			pstmt.setTimestamp(40, custRefDate);
			pstmt.setDouble(41, custRefAmt);
			pstmt.setString(42, rndOff);
			pstmt.setString(43, rndTo);
			pstmt.setString(44, sundryType);
			pstmt.setString(45, custCode);
			pstmt.setString(46, siteCodeDrcr);
			pstmt.setString(47, drcrType);
			pstmt.setDouble(48, ordBillbackAmt);
			pstmt.setDouble(49, ordOffinvAmt);
			pstmt.setDouble(50, lineBillbackAmt);
			pstmt.setDouble(51, lineOffinvAmt);
			pstmt.setString(52, ediStat);
			pstmt.executeUpdate();
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			/*xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
		xmlBuff.append("<description>Datawindow Root</description>");
		xmlBuff.append("<group0>");
		xmlBuff.append("<description>Group0 description</description>");
		xmlBuff.append("<Header0>");
		xmlBuff.append("<objName><![CDATA[").append("drcrrcp_cr").append("]]></objName>");		
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
		xmlBuff.append("<description>Header0 members</description>");


		xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"drcrrcp_cr\">");		
		xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");

		//xmlBuff.append("<tran_id>").append("<![CDATA[" + tranIdNew + "]]>").append("</tran_id>");
		xmlBuff.append("<tran_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</tran_date>");
		xmlBuff.append("<fin_entity>").append("<![CDATA[" + finEntity + "]]>").append("</fin_entity>");
		xmlBuff.append("<cust_code>").append("<![CDATA[" + custCode + "]]>").append("</cust_code>");
		xmlBuff.append("<acct_code>").append("<![CDATA[" + acctCode + "]]>").append("</acct_code>");
		xmlBuff.append("<cctr_code>").append("<![CDATA[" + cctrCode + "]]>").append("</cctr_code>");

		if(effDate !=null)
		{
			xmlBuff.append("<eff_date>").append("<![CDATA[" + sdfAppl.format(effDate).toString() + "]]>").append("</eff_date>");
		}
		xmlBuff.append("<invoice_id>").append("<![CDATA[" + parentTranIdAs + "]]>").append("</invoice_id>");
		xmlBuff.append("<amount>").append("<![CDATA[" + amount + "]]>").append("</amount>");
		xmlBuff.append("<curr_code>").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");
		xmlBuff.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");			
		xmlBuff.append("<remarks>").append("<![CDATA[" +  remarks +"]]>").append("</remarks>");
		xmlBuff.append("<site_code>").append("<![CDATA[" + siteCodeAs + "]]>").append("</site_code>");
		xmlBuff.append("<anal_code>").append("<![CDATA[" + analCode + "]]>").append("</anal_code>");
		xmlBuff.append("<drcr_flag>").append("<![CDATA[" + drcrFlag + "]]>").append("</drcr_flag>");
		xmlBuff.append("<tran_id__rcv>").append("<![CDATA[" + tranIdRcv + "]]>").append("</tran_id__rcv>");
		xmlBuff.append("<confirmed>").append("<![CDATA[" + "N" + "]]>").append("</confirmed>");
		xmlBuff.append("<emp_code__aprv>").append("<![CDATA[" + empCodeAprv  + "]]>").append("</emp_code__aprv>");
		xmlBuff.append("<tran_type>").append("<![CDATA[" + tranType + "]]>").append("</tran_type>");
		xmlBuff.append("<tran_ser>").append("<![CDATA[" + tranSer + "]]>").append("</tran_ser>");	
		if(dueDate !=null)
		{
		xmlBuff.append("<due_date>").append("<![CDATA[" + sdfAppl.format(dueDate).toString() + "]]>").append("</due_date>");
		}
		xmlBuff.append("<cr_term>").append("<![CDATA[" + crTerm + "]]>").append("</cr_term>");
		xmlBuff.append("<chg_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</chg_date>");
		xmlBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
		xmlBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
		xmlBuff.append("<add_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</add_date>");
		xmlBuff.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
		xmlBuff.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
		xmlBuff.append("<amount__bc>").append("<![CDATA[" +amountBc + "]]>").append("</amount__bc>");
		xmlBuff.append("<item_ser>").append("<![CDATA[" + itemSer+ "]]>").append("</item_ser>");
		xmlBuff.append("<round_adj>").append("<![CDATA[" + 0 + "]]>").append("</round_adj>");
		xmlBuff.append("<sreturn_no>").append("<![CDATA[" + "" + "]]>").append("</sreturn_no>");
		xmlBuff.append("<adj_recv>").append("<![CDATA[" + adjRecv + "]]>").append("</adj_recv>");
		xmlBuff.append("<gp_ser>").append("<![CDATA[" + gpSer + "]]>").append("</gp_ser>");
		xmlBuff.append("<gp_no>").append("<![CDATA[" + gpNo + "]]>").append("</gp_no>");
		if(gpDate !=null)
		{

			xmlBuff.append("<gp_date>").append("<![CDATA[" + sdfAppl.format(gpDate).toString() + "]]>").append("</gp_date>");
		}
			xmlBuff.append("<parent__tran_id>").append("<![CDATA[" + tranIdAs + "]]>").append("</parent__tran_id>");
		xmlBuff.append("<rev__tran>").append("<![CDATA[" + revTran + "]]>").append("</rev__tran>");
		//xmlBuff.append("<cust_ref_no>").append("<![CDATA[" + custRefNo + "]]>").append("</cust_ref_no>");
		if(custRefDate !=null)
		{
			xmlBuff.append("<cust_ref_date>").append("<![CDATA[" + sdfAppl.format(custRefDate).toString() + "]]>").append("</cust_ref_date>");
		}
		//xmlBuff.append("<cust_ref_amt>").append("<![CDATA[" + custRefAmt + "]]>").append("</cust_ref_amt>");
		xmlBuff.append("<rnd_off>").append("<![CDATA[" + rndOff + "]]>").append("</rnd_off>");
		xmlBuff.append("<rnd_to>").append("<![CDATA[" + rndTo + "]]>").append("</rnd_to>");
		xmlBuff.append("<sundry_type>").append("<![CDATA[" + sundryType + "]]>").append("</sundry_type>");
		xmlBuff.append("<sundry_code>").append("<![CDATA[" + custCode + "]]>").append("</sundry_code>");		
		xmlBuff.append("<site_code__drcr>").append("<![CDATA[" + siteCodeDrcr + "]]>").append("</site_code__drcr>");
		xmlBuff.append("<drcr_type>").append("<![CDATA[" + drcrType + "]]>").append("</drcr_type>");
		xmlBuff.append("<ord_billback_amt>").append("<![CDATA[" + ordBillbackAmt + "]]>").append("</ord_billback_amt>");
		xmlBuff.append("<ord_offinv_amt>").append("<![CDATA[" + ordOffinvAmt + "]]>").append("</ord_offinv_amt>");
		xmlBuff.append("<line_billback_amt>").append("<![CDATA[" + lineBillbackAmt  + "]]>").append("</line_billback_amt>");
		xmlBuff.append("<line_offinv_amt>").append("<![CDATA[" + lineOffinvAmt + "]]>").append("</line_offinv_amt>");
		xmlBuff.append("<edi_stat>").append("<![CDATA[" + ediStat + "]]>").append("</edi_stat>");

		xmlBuff.append("</Detail1>");
		xmlBuff.append("\n");*/

			sql="select tran_type,tax_date from sreturn where tran_id= ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sreturnNo);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				returnType=rs.getString("tran_type");
				taxDate=rs.getTimestamp("tax_date");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			if(taxDate == null)       //if isnull(ld_tax_date) or string(ld_tax_date,'ddmmyyyy') = '01011900' then ld_tax_date = ld_tran_date
			{
				taxDate=tranDate;
			}

			taxEnvSr=(finCommon.getFinparams("999999","SRETURN_TAX_ENV",conn));
			if("NULLFOUND".equalsIgnoreCase(taxEnvSr) || taxEnvSr == null || taxEnvSr.trim().length() ==0  )
			{
				taxEnvSr="";
			}
			lbFlag=false;
			sretunTypeList=(distCommon.getDisparams("999999", "SRETURN_TYPE", conn));
			if("NULLFOUND".equalsIgnoreCase(sretunTypeList)|| sretunTypeList == null || sretunTypeList.trim().length() ==0 )
			{
				sretunTypeList="";
			}

			String sreturn_type[]=sretunTypeList.split(",");
			if(sreturn_type.length > 0)
			{
				for (i = 0; i < sreturn_type.length; i++) {

					if((returnType.trim()).equalsIgnoreCase(sretunType) )
					{
						lbFlag=true;
					}
				}
			}

			sql="select  line_no,invoice_id,line_no__inv,item_code,sales_pers,tax_class,tax_chap,  " +
					" tax_env,drcr_amt,tax_amt,net_amt,reas_code,cost_rate,rate__std,quantity,rate,rate__clg, " +
					" discount,line_no__sret,line_no__invtrace,lot_no,lot_sl,tran_id__rcp,billback_amt,offinv_amt " +
					" from drcr_rdet  " +
					" where tran_id = ? ";
			pstmt1=conn.prepareStatement(sql);
			pstmt1.setString(1, tranIdAs);
			rs=pstmt1.executeQuery();
			while(rs.next())
			{
				lineNo=rs.getInt("line_no");
				invoiceId=rs.getString("invoice_id");
				lineNoInv=rs.getInt("line_no__inv");
				itemCode=rs.getString("item_code");
				salesPers=rs.getString("sales_pers");
				taxClass=rs.getString("tax_class");
				taxChap=rs.getString("tax_chap");
				taxEnv=rs.getString("tax_env");
				drcrAmt=rs.getDouble("drcr_amt");
				taxAmt=rs.getDouble("tax_amt");
				netAmt=rs.getDouble("net_amt");
				reasCode=rs.getString("reas_code");
				costRate=rs.getDouble("cost_rate");
				rateStd=rs.getDouble("rate__std");
				quantity=rs.getDouble("quantity");
				rate=rs.getDouble("rate");
				rateClg=rs.getDouble("rate__clg");
				discount=rs.getDouble("discount");
				lineNoSret=rs.getInt("line_no__sret");
				lineNoInvtrace=rs.getInt("line_no__invtrace");
				lotNo=rs.getString("lot_no");
				lotSl=rs.getString("lot_sl");
				tranIdRcp=checkNullAndTrim(rs.getString("tran_id__rcp"));
				billbackAmt=rs.getDouble("billback_amt");
				offinvAmt=rs.getDouble("offinv_amt");


				if(netAmt==0)
				{
					netAmt=0;
				}


				netAmt=netAmt-taxAmt;

				if(!lbFlag)
				{
					taxEnvSr=taxEnv;
				}
				System.out.println("manish tran id>>>>"+tranIdNew+"\ntran id"+tranId+"\ntran id "+tranIdAs+"\ntranid"+tranIdRcp+"\ntranid"+tranIdRcv);
				sql=" insert into drcr_rdet (TRAN_ID ,LINE_NO,INVOICE_ID,LINE_NO__INV,ITEM_CODE,SALES_PERS,TAX_CLASS,TAX_CHAP, "+
						" TAX_ENV,DRCR_AMT,TAX_AMT,NET_AMT,REAS_CODE,COST_RATE,RATE__STD,QUANTITY,RATE,RATE__CLG, " +
						" DISCOUNT,LINE_NO__SRET,LINE_NO__INVTRACE,LOT_NO,LOT_SL,TRAN_ID__RCP,BILLBACK_AMT,OFFINV_AMT) "+
						" Values (?,?,?,?,?,?, "+
						" ?,?,?,?,?,?,?,?, "+
						"?,?,?,?,?,?,?, "+
						"?,?,?,?,? ) ";

				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, tranIdNew);
				pstmt.setInt(2, lineNo);
				pstmt.setString(3, invoiceId);
				//pstmt.setInt(4, lineNoInv);
				pstmt.setNull(4, Types.INTEGER);
				pstmt.setString(5, itemCode);
				pstmt.setString(6, salesPers);
				pstmt.setString(7, " ");
				pstmt.setString(8, " ");
				pstmt.setString(9, " ");
				pstmt.setDouble(10, drcrAmt);
				pstmt.setDouble(11, 0);
				pstmt.setDouble(12, drcrAmt);
				pstmt.setString(13, reasCode);
				pstmt.setDouble(14,costRate);
				pstmt.setDouble(15, rateStd);
				pstmt.setDouble(16, quantity);
				pstmt.setDouble(17, rate);
				pstmt.setDouble(18, rateClg);
				pstmt.setDouble(19, discount);
				pstmt.setInt(20, lineNoSret);
				pstmt.setNull(21, Types.INTEGER);
				//	pstmt.setInt(21,0);
				pstmt.setString(22, lotNo);
				pstmt.setString(23, lotSl);
				pstmt.setString(24, tranId);
				pstmt.setDouble(25, billbackAmt);
				pstmt.setDouble(26, offinvAmt);

				pstmt.executeUpdate();
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}

				/*	xmlBuff.append("<Detail2 dbID=\"\" domID=\""+lineNo+"\" objContext=\"2\" objName=\"drcrrcp_cr\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");

				//xmlBuff.append("<tran_id>").append("<![CDATA[" + tranIdNew + "]]>").append("</tran_id>");
				xmlBuff.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>");
				xmlBuff.append("<invoice_id>").append("<![CDATA[" + parentTranIdAs + "]]>").append("</invoice_id>");
				xmlBuff.append("<line_no__inv>").append("<![CDATA[" + lineNoInv + "]]>").append("</line_no__inv>");
				xmlBuff.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
				if(salesPers !=null && (salesPers.trim().length()>0) && (!("null".equalsIgnoreCase(salesPers))))
				{
				xmlBuff.append("<sales_pers>").append("<![CDATA[" + salesPers + "]]>").append("</sales_pers>");
				}
				xmlBuff.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
				xmlBuff.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
				xmlBuff.append("<tax_env>").append("<![CDATA[" + taxEnvSr + "]]>").append("</tax_env>");				
				xmlBuff.append("<drcr_amt>").append("<![CDATA[" + drcrAmt + "]]>").append("</drcr_amt>");
				xmlBuff.append("<tax_amt>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt>");
				xmlBuff.append("<net_amt>").append("<![CDATA[" + netAmt + "]]>").append("</net_amt>");
				xmlBuff.append("<reas_code>").append("<![CDATA[" + reasCode + "]]>").append("</reas_code>");
				xmlBuff.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
				xmlBuff.append("<rate__std>").append("<![CDATA[" + rateStd + "]]>").append("</rate__std>");
				xmlBuff.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
				xmlBuff.append("<rate>").append("<![CDATA[" +rate + "]]>").append("</rate>");
				xmlBuff.append("<rate_clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate_clg>");
				xmlBuff.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
				xmlBuff.append("<line_no__sret>").append("<![CDATA[" + lineNoSret + "]]>").append("</line_no__sret>");		
				xmlBuff.append("<line_no__invtrace>").append("<![CDATA[" + lineNoInvtrace + "]]>").append("</line_no__invtrace>");
				xmlBuff.append("<lot_no>").append("<![CDATA[" + (lotNo) + "]]>").append("</lot_no>");
				xmlBuff.append("<lot_sl>").append("<![CDATA[" + (lotSl) + "]]>").append("</lot_sl>");
				xmlBuff.append("<tran_id__rcp>").append("<![CDATA[" + tranIdRcp + "]]>").append("</tran_id__rcp>");
				xmlBuff.append("<billback_amt>").append("<![CDATA[" + billbackAmt + "]]>").append("</billback_amt>");
				xmlBuff.append("<offinv_amt>").append("<![CDATA[" + offinvAmt + "]]>").append("</offinv_amt>");

				xmlBuff.append("</Detail2>");
				xmlBuff.append("\n");*/	
			}
			rs.close();
			rs = null;
			pstmt1.close();
			pstmt1 = null;

			sql="select SUM(DRCR_AMT) from drcr_rdet where tran_id= ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranIdNew);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				amount=rs.getDouble(1);

			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			amountBc=amount *exchRate;

			sql=" update drcr_rcp set amount = ? ,amount__bc= ?  where tran_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setDouble(1,amount);
			pstmt.setDouble(2,amountBc);
			pstmt.setString(3,tranIdNew);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;

			//System.out.println("--xmlString--"+xmlBuff.toString());
			/*errorString = saveData(xtraParams,siteCodeAs,xmlBuff.toString(),conn);
		//System.out.println("@@@@@2: retString:"+errorString);
		//System.out.println("--retString finished--");
		if (errorString.indexOf("Success") > -1)
		{
		//	System.out.println("@@@@@@3: Success"+errorString);
			Document dom = genericUtility.parseString(errorString);
			//System.out.println("dom>>>"+dom);
			drNtTranId = genericUtility.getColumnValue("TranID",dom);
		}
		else
		{
			//System.out.println("[SuccessSuccess" + errorString + "]");	
			conn.rollback();
			return errorString;
		}*/

			/*if(tax_env_sr.trim().length()>0 && tax_env_sr!=null)
		{
			if(lbFlag=true)
			{
				lds_drcr_rcp_det.retrieve(ls_tran_id_new , ll_line_no)     
				ds_tax_detbrow.reset()
				//lc_tax_amtdet = gf_calc_tax_ds(lds_drcr_rcp_det,ds_tax_detbrow,transer,ls_tran_id_new,ld_tax_date,"rate", "quantity",0,ls_curr_code,'2')       
			}
			else 
			{
						ds_salesreturn_retnline.retrieve(ls_sreturn_no , ll_line_no__sret)     
						ds_salesreturn_retnline.setitem(1,'line_no',ll_line_no)
						ds_tax_detbrow.reset()
						lc_tax_amtdet = gf_calc_tax_ds(ds_salesreturn_retnline,ds_tax_detbrow,transer,ls_tran_id_new,ld_tax_date,"rate__stduom", "quantity__stduom",0,ls_curr_code,'2')       
			}
		}*/
			/*if(taxAmtdet==0 )	//  if isnull(lc_tax_amtdet) or lc_tax_amtdet = -999999999 then lc_tax_amtdet = 0
		{
			taxAmtdet=0;
		}

		if(netAmt==0)
		{
			netAmt=0;
		}
		if(totNetAmt==0)
		{
			totNetAmt=0;
		}
		if(mtotamt==0)
		{
			mtotamt=0;
		}
		if(totdiscamt==0)
		{
			totdiscamt=0;
		}
		totNetAmt=totNetAmt+netAmt+taxAmtdet;

		sql="  update drcr_rdet" + 
				" set tax_amt =  ?, " + 
				" net_amt = (net_amt + (? - ?)) " + 
				" where tran_id = ? " + 
				" and line_no = ? ";

		pstmt=conn.prepareStatement(sql);
		pstmt.setDouble(1,taxAmtdet);
		pstmt.setDouble(2,taxAmtdet);
		pstmt.setDouble(3,discount);
		pstmt.setString(4,tranIdNew);
		pstmt.setInt(5, lineNo);
		pstmt.executeUpdate();
		pstmt.close();
		pstmt=null;*/

			sql="select line_no,ref_ser,ref_no,ref_bal_amt,adj_amt,mrp_value__adj "+
					" from drcr_rcpinv where tran_id= ? ";
			pstmt1=conn.prepareStatement(sql);
			pstmt1.setString(1, tranIdAs);
			rs=pstmt1.executeQuery();
			while(rs.next())
			{
				lineNo=rs.getInt("line_no");
				refSer=rs.getString("ref_ser");
				refNo=rs.getString("ref_no");
				refBalAmt=rs.getDouble("ref_bal_amt");
				adjAmt=rs.getDouble("adj_amt");
				mrpValueAdj=rs.getDouble("mrp_value__adj");			


				sql="insert into drcr_rcpinv (tran_id, line_no,ref_ser,ref_no,ref_bal_amt,adj_amt,mrp_value__adj) "+
						" Values (?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdNew);
				pstmt.setInt(2, lineNo);
				pstmt.setString(3, refSer);
				pstmt.setString(4, refNo);
				pstmt.setDouble(5, refBalAmt);
				pstmt.setDouble(6, adjAmt);
				pstmt.setDouble(7, mrpValueAdj);

				pstmt.executeUpdate();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				/*	xmlBuff.append("<Detail3 dbID=\"\" domID=\""+lineNo+"\" objContext=\"3\" objName=\"drcrrcp_cr\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
			// xmlBuff.append("<tran_id><![CDATA["+ tranIdNew   +"]]></tran_id>");
			 xmlBuff.append("<line_no><![CDATA["+ lineNo   +"]]></line_no>");
			 xmlBuff.append("<ref_ser><![CDATA["+ refSer   +"]]></ref_ser>");
			 xmlBuff.append("<ref_no><![CDATA["+ refNo  +"]]></ref_no>");
			 xmlBuff.append("<ref_bal_amt><![CDATA["+ refBalAmt   +"]]></ref_bal_amt>");
			 xmlBuff.append("<adj_amt><![CDATA["+  adjAmt +"]]></adj_amt>");
			 xmlBuff.append("<mrp_val__adj><![CDATA["+ mrpValueAdj  +"]]></mrp_val__adj>");
			 xmlBuff.append("</Detail3>");*/


				/*sql="update drcr_rcp " + 
						"	set AMOUNT = ?, " + 
						"		 AMOUNT__BC = ? " + 
						"	where tran_id = ? ";

				pstmt=conn.prepareStatement(sql);
				pstmt.setDouble(1,totNetAmt);
				pstmt.setDouble(2,totNetAmt);
				pstmt.setString(3,tranIdNew);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;*/
				/*	
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				String xmlString = xmlBuff.toString().trim().replaceFirst("^([\\W]+)<","<");
				String drNtTranId="";
//				errString = saveData(xtraParams,siteCodeAs,xmlString,conn);
				 */			
			}
			pstmt1.close();
			pstmt1=null;
			rs.close();
			rs=null;
			/*xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
			 */
			//errString = saveData(xtraParams,siteCodeAs,xmlBuff.toString(),conn);
			//System.out.println("@@@@@2: retString:"+errorString);
			//System.out.println("--retString finished--");
			/*if (errString.indexOf("Success") > -1)
				{
					System.out.println("@@@@@@3: Success"+errString);
					Document dom = genericUtility.parseString(errString);
					//System.out.println("dom>>>"+dom);
					tranIdNew = genericUtility.getColumnValue("TranID",dom);
				}
				else
				{
					//System.out.println("[SuccessSuccess" + errorString + "]");	
					conn.rollback();
					return errString;
				}	*/	
			//errString = nvo_misc.gbf_retrieve_misc_drcr_rcp(ls_tran_id_new, ls_tran_id_new,1,siteCodeAs);
			DrCrRcpConf drcrObj = new DrCrRcpConf();
			errString = drcrObj.confirm (tranIdNew, xtraParams, "" , conn);
			//System.out.println("After DrCrRcpConf---->["+errorString+"]");
			if(errString != null && errString.indexOf("VTCICONF3") != -1)
			{
				errString = "";
			}

			/*if(errString.trim().length()==0)
				{
					sql=" update sreturn "+
					    " set tran_id__crn = ? "+
					    " where tran_id = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,tranIdAs);
					pstmt.setString(2,sreturnNo);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt=null;
				}*/

		}
		catch(Exception e)
		{			
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;

	}//end manish


	//added by manish mhatre on22-jan-2020
	//start manish
	public String  gbfAutoMiscCrnoteSreturnOth(String tranIdAs,String siteCodeAs,String xtraParams, Connection conn) throws Exception
	{
		String   trnofld="" , finEntity="" , custCode="",acctCode="",cctrCode="" , invoiceId="", tranId="" ;
		String currCode="" ,remarks="",analCode="",drcrFlag="",tranIdRcv="",confirmed="" ,siteCode="";
		String empCodeAprv="",tranType="",tranSer="" , crTerm="" ,itemSer="" ,sreturnNo="",adjRecv="";
		String gpSer="",gpNo="",parentTranId="",revTran="",custRefNo="",rndOff="",sundryType="";   //,rnd_to=""
		String sundryCode="",siteCodeDrcr="",ediStat="" , drcrType="" , errcode="" , keystr="" ,tranwin="" , tranIdNew="";
		String itemCode="" ,salesPers="" , taxChap="" , taxClass="",taxEnv="" ,reasCode="",lotNo="" , lotSl="";
		String tranIdRcp="" , refSer="" , refNo=""  , returnType="" , sretunTypeList="" , sretunType="",adjmisccrn="" ; 
		String refNoCr="" ,wfStatus="",custCodeDlv="" , custCodePord="" , saleOrder="" ,custPord="" , empCode="";
		String analysis1="",analysis2="",analysis3="",unit="" , refSerRcpinv="",refNoRcpinv="",stanCode="",stanCodeSite="";
		Timestamp chgDate=null , tranDate=null,custRefDate=null ,effDate=null,confDate=null ,gpDate=null , dueDate=null ,pordDate=null;
		double amount =0 , amountBc = 0 , roundAdj=0 ,custRefAmt=0,ordBillbackAmt=0,ordOffinvAmt=0;
		double  lineBillbackAmt=0 , lineOffinvAmt=0 ,drcrAmt=0 ,taxAmt=0 ,netAmt = 0 ,quantity=0 ;
		double discount=0,billbackAmt=0 ,offinvAmt=0 , refBalAmt=0 , adjAmt=0 , mrpValueAdj=0,adjAmount=0;
		double rndTo=0 , refBalAmtRcpinv=0,adjAmtRcpinv=0,mrpValueAdjRcpinv=0,taxAmtdet=0 , tottax=0 , totdiscamt=0;
		double  amountdet= 0 , totNetAmt=0;

		double costRate=0 ,rateStd=0 ,rate=0 , rateClg=0; 
		double exchRate=0;
		int lineNo=0 , lineNoInv=0 ,lineNoSret=0 , lineNoInvtrace=0	, count=0 , lineNoRcpinv=0 , i=0;
		String taxEnvSr="", window="",objName="";
		boolean lbFlag = false;
		Timestamp taxDate=null;
		String temp="";
		String chgUser="",chgTerm="",errString="";
		String transer = "MDRCRC";
		trnofld = "tran_id";
		//chg_date = datetime(today(),now());
		String sql="",sql1="";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		//ValidatorEJB vdt=new ValidatorEJB();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		//FinCommon finCommon = new FinCommon();
		Timestamp currDate = null;
		UtilMethods utilMethods = UtilMethods.getInstance();
		StringBuffer xmlBuff = null;

		try
		{
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			xmlBuff = new StringBuffer();
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			//chgDate= new java.sql.Timestamp(System.currentTimeMillis()) ;
			chgDate = new java.sql.Timestamp(new java.util.Date().getTime());

			sql="select tran_ser,tran_date,eff_date,fin_entity,site_code,sundry_type, "+
					" sundry_code,acct_code,cctr_code,amount,curr_code,exch_rate,remarks,drcr_flag, "+
					" tran_id__rcv,confirmed,conf_date,emp_code__aprv,due_date,tran_type,item_ser, "+
					" amount__bc,sreturn_no,adj_misc_crn,adj_amount,parent__tran_id,rev__tran,round_adj, "+
					" cust_ref_no,cust_ref_date,cust_ref_amt,rnd_off,rnd_to,ref_no__cr,wf_status, "+
					" cust_code__dlv,cust_pord,pord_date,edi_stat,sale_order from misc_drcr_rcp where tran_id = ? ";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranIdAs );							

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				transer=rs.getString("tran_ser");
				tranDate=rs.getTimestamp("tran_date");
				effDate=rs.getTimestamp("eff_date");
				finEntity=rs.getString("fin_entity");
				siteCode=rs.getString("site_code");
				sundryType=rs.getString("sundry_type");
				sundryCode=rs.getString("sundry_code");
				acctCode=checkNullAndTrim(rs.getString("acct_code"));
				cctrCode=rs.getString("cctr_code");
				amount=rs.getDouble("amount");
				currCode=rs.getString("curr_code");
				exchRate=rs.getDouble("exch_rate");
				remarks=checkNullAndTrim(rs.getString("remarks"));
				drcrFlag=rs.getString("drcr_flag");
				tranIdRcv=checkNullAndTrim(rs.getString("tran_id__rcv"));
				confirmed=rs.getString("confirmed");
				confDate=rs.getTimestamp("conf_date");
				empCodeAprv=rs.getString("emp_code__aprv");
				dueDate=rs.getTimestamp("due_date");
				tranType=rs.getString("tran_type");
				itemSer=rs.getString("item_ser");
				amountBc=rs.getDouble("amount__bc");
				sreturnNo=rs.getString("sreturn_no");
				adjmisccrn=rs.getString("adj_misc_crn");
				adjAmount=rs.getDouble("adj_amount");
				parentTranId=rs.getString("parent__tran_id");
				revTran=rs.getString("rev__tran");
				roundAdj=rs.getDouble("round_adj");
				custRefNo=rs.getString("cust_ref_no");
				custRefDate=rs.getTimestamp("cust_ref_date");
				custRefAmt=rs.getDouble("cust_ref_amt");
				rndOff=rs.getString("rnd_off");
				rndTo=rs.getDouble("rnd_to");
				refNoCr=rs.getString("ref_no__cr");
				wfStatus=rs.getString("wf_status");
				custCodeDlv=rs.getString("cust_code__dlv");
				custPord=rs.getString("cust_pord");
				pordDate=rs.getTimestamp("pord_date");
				ediStat=rs.getString("edi_stat");
				saleOrder=rs.getString("sale_order");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if ("D".equalsIgnoreCase(drcrFlag)) 
			{
				tranSer = "MDRCRD";
				tranwin = "W_MISC_DRCR_RCP_DR";
				window = "T_MISC_DRCR_RCP_DRHO";
				//ls_auto_tran_id = generateTranId("w_misc_drcr_rcp_cr",as_site_code,ls_drcr_flag,currDateStr,conn);	
				//tranIdNew = generateTranId("w_misc_drcr_rcp_cr",siteCodeAs,drcrFlag,currDateStr,tranType,conn);       //added tran type by manish mhatre on 28-feb-2020
				tranIdNew = generateTranId("t_misc_drcr_rcp_drho",siteCodeAs,drcrFlag,currDateStr,tranType,conn); 

			}
			else if ("C".equalsIgnoreCase(drcrFlag))
			{
				tranSer = "MDRCRC";
				tranwin = "W_MISC_DRCR_RCP_CR";
				window = "T_MISC_DRCR_RCP_CRHO";
				//ls_auto_tran_id = generateTranId("w_misc_drcr_rcp_cr",as_site_code,ls_drcr_flag,currDateStr,conn);	
				//tranIdNew = generateTranId("w_misc_drcr_rcp_cr",siteCodeAs,drcrFlag,currDateStr,tranType,conn);     //added tran type by manish mhatre on 28-feb-2020
				tranIdNew = generateTranId("t_misc_drcr_rcp_crho",siteCodeAs,drcrFlag,currDateStr,tranType,conn); 
			}
			System.out.println("key String"+keystr);
			/*sql="select key_string  from transetup where upper(tran_window) = ? " ;
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,window);
			rs=pstmt.executeQuery();
			if (rs.next())
			{
				keystr=checkNullAndTrim(rs.getString("key_string"));
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

			if(keystr == null || keystr.length() == 0)
			{
				sql = "select KEY_STRING from transetup where tran_window = 'GENERAL' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{	
					keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
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
			}*/

			/*	String xmlValues = "",mpadjTranid="";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<tran_id/>";
			xmlValues = xmlValues + "<site_code>" + siteCodeAs + "</site_code>";
			xmlValues = xmlValues + "<tran_date>"+ sdf.format(currDate) + "</tran_date>";
			xmlValues = xmlValues + "<tran_type>"+ sdf.format(tran_type) + "</tran_type>";
			xmlValues = xmlValues + "</Detail1></Root>";

			TransIDGenerator tg = new TransIDGenerator(xmlValues, "SYSTEM", CommonConstants.DB_NAME);
			mpadjTranid = tg.generateTranSeqID(transer,trnofld,keystr, conn);*/

			/*lds_keygen = create nvo_datastore
					lds_keygen.dataobject = 'd_genkey_temp'
					lds_keygen.insertrow(0)
					lds_keygen.setitem(1,"site_code", siteCodeAs)
					lds_keygen.setitem(1,"tran_date", ld_chg_date)
					lds_keygen.setitem(1,"tran_type", ls_tran_type)

					ls_tran_id_new = gf_gen_key_nvo(lds_keygen,transer, trnofld, keystr)
					destroy lds_keygen*/



			/*if ls_tran_id_new = 'ERROR' then
					populateerror(9999,'populateerror')
					ls_errcode = 'VTTRANID' + ' ~t for tran. series :' +  transer 
					ls_errcode = gf_error_location(ls_errcode)
				end if*/


			System.out.println("tranid manish"+tranIdNew);
			sql="select count(*) from site_customer where site_code__ch = ? and site_code = ? and channel_partner = 'Y' ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeAs);
			pstmt.setString(2, siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				count=rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(count==0)
			{
				sql="select count(*) from customer where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeAs);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					count=rs.getInt(1);
				}


				if(count > 1)
				{
					errString = itmDBAccessLocal.getErrorString("", "ERRORVTCPC", "","",conn);
					return errString;
				}
				else if(count == 0)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTCUSTCD4", "","",conn);
					return errString;
				}
				else if(count == 1)
				{
					sql1 = "select cust_code from customer where site_code = ? and channel_partner = 'Y'" ;
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1,siteCodeAs);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						custCode = rs1.getString("cust_code");
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
				}
			}

			else if(count == 1)
			{
				sql = "select cust_code from site_customer where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeAs);
				pstmt.setString(2,siteCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					custCode = checkNull(rs.getString("cust_code"));
				}
			}
			if(rs != null) 
			{
				rs.close();rs = null;

			}
			if(pstmt != null) 
			{
				pstmt.close();pstmt = null;
			}	


			sql = " select fin_entity from site where site_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeAs);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{ 
				finEntity = checkNull(rs.getString("fin_entity"));
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			sql="select remarks from sreturn where tran_id= ? ";
			pstmt1=conn.prepareStatement(sql);
			pstmt1.setString(1, sreturnNo);
			rs1=pstmt1.executeQuery();
			if(rs1.next())
			{
				remarks=checkNullAndTrim(rs1.getString("remarks"));
			}
			pstmt1.close();
			pstmt1=null;
			rs1.close();
			rs1=null;

			//MISC_DRCR_RCP
			sql = "insert into misc_drcr_rcp  (tran_id,tran_ser,tran_date,eff_date,fin_entity,site_code,sundry_type,sundry_code,acct_code," +
					" cctr_code,amount,curr_code,exch_rate,remarks,drcr_flag,tran_id__rcv,confirmed,chg_user,chg_date,chg_term," +
					" emp_code__aprv,due_date,tran_type,item_ser,amount__bc,sreturn_no,adj_misc_crn,adj_amount,parent__tran_id,rev__tran," +
					" round_adj,cust_ref_no,cust_ref_date,cust_ref_amt,rnd_off,rnd_to,ref_no__cr,wf_status,cust_code__dlv,cust_pord,pord_date," +
					" edi_stat,sale_order,add_date,add_user,add_term) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "; 

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			pstmt.setString(1,tranIdNew);
			pstmt.setString(2,tranSer);
			pstmt.setTimestamp(3,tranDate);
			pstmt.setTimestamp(4,effDate);
			pstmt.setString(5,finEntity);
			pstmt.setString(6,siteCodeAs);
			pstmt.setString(7,sundryType);
			//pstmt.setString(8,sundryCode);
			pstmt.setString(8,custCode);
			pstmt.setString(9,acctCode);
			pstmt.setString(10,cctrCode);
			pstmt.setDouble(11,amount);
			pstmt.setString(12,currCode);
			pstmt.setDouble(13,exchRate);
			pstmt.setString(14,remarks);
			pstmt.setString(15,drcrFlag);
			pstmt.setString(16,tranIdRcv);
			pstmt.setString(17,"N");
			pstmt.setString(18,chgUser);
			//pstmt.setTimestamp(19,chg_date);
			pstmt.setTimestamp(19,currDate);
			pstmt.setString(20,chgTerm);
			pstmt.setString(21,empCodeAprv);
			pstmt.setTimestamp(22,dueDate);
			pstmt.setString(23,tranType);
			pstmt.setString(24,itemSer);
			pstmt.setDouble(25,amountBc);
			pstmt.setString(26,sreturnNo);  // 16-dec-20 manoharan  Uncommented  //commented by manish mhatre on 1-july-20
			//pstmt.setString(26,"");   // 16-dec-20 manoharan  commented //added by manish mhatre[for set main credit note tranid in sale return credit note no]
			pstmt.setString(27,adjmisccrn);
			pstmt.setDouble(28,adjAmount);
			pstmt.setString(29,tranIdAs);
			pstmt.setString(30,revTran);
			pstmt.setDouble(31,0);
			pstmt.setString(32,custRefNo);
			pstmt.setTimestamp(33,custRefDate);
			pstmt.setDouble(34,custRefAmt);
			pstmt.setString(35,rndOff); 
			pstmt.setDouble(36,rndTo);
			pstmt.setString(37,refNoCr);
			pstmt.setString(38,wfStatus);
			//  pstmt.setString(39,custCode);
			pstmt.setString(39,sundryCode);
			pstmt.setString(40,custPord);
			pstmt.setTimestamp(41,pordDate);
			pstmt.setString(42,ediStat);
			pstmt.setString(43,saleOrder);
			// pstmt.setTimestamp(44,chg_date);
			pstmt.setTimestamp(44,currDate);
			pstmt.setString(45,chgUser);
			pstmt.setString(46,chgTerm); 

			pstmt.executeUpdate();
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			/*xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
						xmlBuff.append("<description>Datawindow Root</description>");
						xmlBuff.append("<group0>");
						xmlBuff.append("<description>Group0 description</description>");
						xmlBuff.append("<Header0>");
						xmlBuff.append("<objName><![CDATA[").append("misc_drcr_rcp_cr").append("]]></objName>");		
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
						xmlBuff.append("<description>Header0 members</description>");



						xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"misc_drcr_rcp_cr\">");		
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
						xmlBuff.append("<misc_drcr_rcp_dr/>");
						xmlBuff.append("<tran_id/>");

						//xmlBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>");
						xmlBuff.append("<tran_ser>").append("<![CDATA[" + tranSer + "]]>").append("</tran_ser>");
						xmlBuff.append("<tran_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</tran_date>");
						if(effDate !=null)
						{
							xmlBuff.append("<eff_date>").append("<![CDATA[" + sdfAppl.format(effDate).toString() + "]]>").append("</eff_date>");
						}
						xmlBuff.append("<fin_entity>").append("<![CDATA[" + finEntity + "]]>").append("</fin_entity>");
						xmlBuff.append("<site_code>").append("<![CDATA[" + siteCodeAs + "]]>").append("</site_code>");
						xmlBuff.append("<sundry_type>").append("<![CDATA[" + sundryType + "]]>").append("</sundry_type>");
						xmlBuff.append("<sundry_code>").append("<![CDATA[" + sundryCode + "]]>").append("</sundry_code>");
						xmlBuff.append("<acct_code>").append("<![CDATA[" + acctCode + "]]>").append("</acct_code>");
						xmlBuff.append("<cctr_code>").append("<![CDATA[" + cctrCode + "]]>").append("</cctr_code>");
						xmlBuff.append("<amount>").append("<![CDATA[" + amount + "]]>").append("</amount>");
						xmlBuff.append("<curr_code>").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");
						xmlBuff.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");			
						xmlBuff.append("<remarks>").append("<![CDATA[" +  remarks +"]]>").append("</remarks>");
						xmlBuff.append("<drcr_flag>").append("<![CDATA[" + drcrFlag + "]]>").append("</drcr_flag>");
						xmlBuff.append("<tran_id__rcv>").append("<![CDATA[" + tranIdRcv + "]]>").append("</tran_id__rcv>");
						xmlBuff.append("<confirmed>").append("<![CDATA[" + "N" + "]]>").append("</confirmed>");
						xmlBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
						xmlBuff.append("<chg_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</chg_date>");
						xmlBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
						xmlBuff.append("<emp_code__aprv>").append("<![CDATA[" + empCodeAprv  + "]]>").append("</emp_code__aprv>");
						if(dueDate !=null)
						{
							xmlBuff.append("<due_date>").append("<![CDATA[" + sdfAppl.format(dueDate).toString() + "]]>").append("</due_date>");
						}
						xmlBuff.append("<tran_type>").append("<![CDATA[" + tranType + "]]>").append("</tran_type>");
						xmlBuff.append("<item_ser>").append("<![CDATA[" + itemSer+ "]]>").append("</item_ser>");
						xmlBuff.append("<amount__bc>").append("<![CDATA[" +amountBc + "]]>").append("</amount__bc>");
						xmlBuff.append("<sreturn_no>").append("<![CDATA[" + " " + "]]>").append("</sreturn_no>");
						xmlBuff.append("<ad_misc_crn>").append("<![CDATA[" + adjmisccrn + "]]>").append("</adj_misc_crn>");
						xmlBuff.append("<adj_amount>").append("<![CDATA[" + adjAmount + "]]>").append("</adj_amount>");
						xmlBuff.append("<parent__tran_id>").append("<![CDATA[" + parentTranId + "]]>").append("</parent__tran_id>");
						xmlBuff.append("<rev__tran>").append("<![CDATA[" + revTran + "]]>").append("</rev__tran>");
						xmlBuff.append("<round_adj>").append("<![CDATA[" + roundAdj + "]]>").append("</round_adj>");
						xmlBuff.append("<cust_ref_no>").append("<![CDATA[" + custRefNo + "]]>").append("</cust_ref_no>");
						if(custRefDate !=null)
						{
						xmlBuff.append("<cust_ref_date>").append("<![CDATA[" + sdfAppl.format(custRefDate).toString() + "]]>").append("</cust_ref_date>");
						}
						xmlBuff.append("<cust_ref_amt>").append("<![CDATA[" + custRefAmt + "]]>").append("</cust_ref_amt>");
						xmlBuff.append("<rnd_off>").append("<![CDATA[" + rndOff + "]]>").append("</rnd_off>");
						xmlBuff.append("<rnd_to>").append("<![CDATA[" + rndTo + "]]>").append("</rnd_to>");
						xmlBuff.append("<ref_no__cr>").append("<![CDATA[" + refNoCr + "]]>").append("</ref_no__cr>");
						xmlBuff.append("<wf_status>").append("<![CDATA[" + wfStatus + "]]>").append("</wf_status>");
						xmlBuff.append("<cust_code__dlv>").append("<![CDATA[" + custCodeDlv + "]]>").append("</cust_code__dlv>");
						xmlBuff.append("<cust_pord>").append("<![CDATA[" + custPord + "]]>").append("</cust_pord>");
						if(pordDate !=null)
						{
							xmlBuff.append("<pord_date>").append("<![CDATA[" + sdfAppl.format(pordDate).toString() + "]]>").append("</pord_date>");
						}
						xmlBuff.append("<edi_stat>").append("<![CDATA[" + ediStat + "]]>").append("</edi_stat>");
						xmlBuff.append("<sale_order>").append("<![CDATA[" + saleOrder + "]]>").append("</sale_order>");
						xmlBuff.append("<add_date>").append("<![CDATA[" + sdfAppl.format(currDate).toString() + "]]>").append("</add_date>");
						xmlBuff.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
						xmlBuff.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
						xmlBuff.append("</Detail1>");
						xmlBuff.append("\n");*/


			sql="select tran_type,tax_date from sreturn where tran_id= ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sreturnNo);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				returnType=rs.getString("tran_type");
				taxDate=rs.getTimestamp("tax_date");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			if(taxDate == null)       //if isnull(ld_tax_date) or string(ld_tax_date,'ddmmyyyy') = '01011900' then ld_tax_date = ld_tran_date
			{
				taxDate=tranDate;
			}

			lbFlag=false;
			sretunTypeList=distCommon.getDisparams("999999", "SRETURN_TYPE", conn);
			if("NULLFOUND".equalsIgnoreCase(sretunTypeList)|| sretunTypeList == null || sretunTypeList.trim().length() ==0 )
			{
				sretunTypeList="";
			}
			String sreturn_type[]=sretunTypeList.split(",");
			if("NULLFOUND".equalsIgnoreCase(sretunTypeList))
			{
				sretunTypeList="";
			}
			if(sreturn_type.length > 0)
			{
				for (i = 0; i < sreturn_type.length; i++) {

					if((returnType.trim()).equalsIgnoreCase(sretunType) )
					{
						lbFlag=true;
					}
				}
			}


			sql="select line_no,acct_code,cctr_code,amount,anal_code,emp_code, " +
					" reas_code,tax_amt,net_amt,tax_class,tax_chap,tax_env,ref_no,item_code,quantity,rate," +
					"line_no__sret,line_no__invtrace,lot_no,analysis1,analysis2,analysis3,lot_sl,unit" +
					" from misc_drcr_rdet " +
					//" where tran_id = ? and ITEM_CODE is not null"; Commented by nandkumar gadkari on 22/04/20
					//"where tran_id = ? and NET_AMT is not null";    commented by manish mhatre on 24/06/20
                    //Added by Varsha V on 27-NOV-2020 as per suggested by Manohar sir
                    " where tran_id = ? and (NET_AMT is not null and net_amt <> 0) " ;
			//" and case when line_no__sret is null then 0 else line_no__sret end > 0 ";  //added by manish mhatre on 24/06/20 // commented by kailasg on 20/080/20 for getting error while confirm chargebck 
			pstmt1=conn.prepareStatement(sql);
			pstmt1.setString(1, tranIdAs);
			rs=pstmt1.executeQuery();
			while(rs.next())
			{
				lineNo=rs.getInt("line_no");
				acctCode=rs.getString("acct_code");
				cctrCode=rs.getString("cctr_code");
				amountdet=rs.getDouble("amount");
				analCode=rs.getString("anal_code");
				empCode=rs.getString("emp_code");
				reasCode=rs.getString("reas_code");
				taxAmt=rs.getDouble("tax_amt");
				netAmt=rs.getDouble("net_amt");
				taxClass=rs.getString("tax_class");
				taxChap=rs.getString("tax_chap");
				taxEnv=rs.getString("tax_env");
				refNo=rs.getString("ref_no");
				itemCode=rs.getString("item_code");
				quantity=rs.getDouble("quantity");
				rate=rs.getDouble("rate");
				lineNoSret=rs.getInt("line_no__sret");
				lineNoInvtrace=rs.getInt("line_no__invtrace");
				lotNo=rs.getString("lot_no");
				analysis1=rs.getString("analysis1");
				analysis2=rs.getString("analysis2");
				analysis3=rs.getString("analysis3");
				lotSl=rs.getString("lot_sl");
				unit=rs.getString("unit");

				if( netAmt==0 ) {
					netAmt=0;
				}
				/*if(amountdet==0)
		        			{
		        				amountdet=0;
		        				netAmt=netAmt-taxAmt;
		        			}*/
				if(lbFlag)
				{
					taxEnvSr=(finCommon.getFinparams("999999","SRETURN_TAX_ENV",conn));
					if("NULLFOUND".equalsIgnoreCase(taxEnvSr) || taxEnvSr == null || taxEnvSr.trim().length() ==0  )
					{
						taxEnvSr="";
					}

				}
				else
				{
					taxEnvSr=taxEnv;
				}

				sql="insert into misc_drcr_rdet (tran_id,line_no,acct_code,cctr_code,amount,anal_code,emp_code, " + 
						"	reas_code,tax_amt,net_amt,tax_class,tax_chap,tax_env,ref_no,item_code,quantity, " + 
						"	rate,line_no__sret,line_no__invtrace,lot_no,analysis1,analysis2,analysis3,lot_sl,unit) " + 
						"	Values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdNew);
				pstmt.setInt(2, lineNo);
				pstmt.setString(3, acctCode);
				pstmt.setString(4, cctrCode);
				pstmt.setDouble(5, amountdet);
				pstmt.setString(6, analCode);
				pstmt.setString(7, empCode);
				pstmt.setString(8, reasCode);
				pstmt.setDouble(9, 0);
				netAmt=netAmt-taxAmt;
				pstmt.setDouble(10, netAmt);
				pstmt.setString(11, " ");
				pstmt.setString(12, " ");
				pstmt.setString(13, " ");
				pstmt.setString(14, refNo);
				pstmt.setString(15, itemCode);
				pstmt.setDouble(16, quantity);
				pstmt.setDouble(17, rate);
				pstmt.setInt(18, lineNoSret);
				pstmt.setNull(19,Types.INTEGER );
				//	pstmt.setInt(19, 0);
				pstmt.setString(20, lotNo);
				pstmt.setString(21, analysis1);
				pstmt.setString(22, analysis2);
				pstmt.setString(23, analysis3);
				pstmt.setString(24, lotSl);
				pstmt.setString(25, unit);

				pstmt.executeUpdate();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				/*xmlBuff.append("<Detail2 dbID=\"\" domID=\""+lineNo+"\" objContext=\"2\" objName=\"misc_drcr_rcp_cr\">");		
								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
								xmlBuff.append("<misc_drcr_rcp_dr/>");

								//xmlBuff.append("<line_no><![CDATA["+ ll_line_no+"]]></line_no>");
								//xmlBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>");
								xmlBuff.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>");
								xmlBuff.append("<acct_code>").append("<![CDATA[" + acctCode + "]]>").append("</acct_code>");
								xmlBuff.append("<cctr_code>").append("<![CDATA[" + cctrCode + "]]>").append("</cctr_code>");
								xmlBuff.append("<amount>").append("<![CDATA[" + amount + "]]>").append("</amount>");
								xmlBuff.append("<anal_code>").append("<![CDATA[" + analCode + "]]>").append("</anal_code>");
								xmlBuff.append("<emp_code>").append("<![CDATA[" + empCode + "]]>").append("</emp_code>");
								xmlBuff.append("<reas_code>").append("<![CDATA[" + reasCode + "]]>").append("</reas_code>");
								//xmlBuff.append("<tax_amt>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt>");
								xmlBuff.append("<net_amt>").append("<![CDATA[" + netAmt + "]]>").append("</net_amt>");
							//	xmlBuff.append("<tax_class><![CDATA["+ taxClass +"]]></tax_class>");
								//xmlBuff.append("<tax_chap><![CDATA["+ taxChap +"]]></tax_chap>");
								//xmlBuff.append("<tax_env><![CDATA["+ taxEnvSr +"]]></tax_env>");
								xmlBuff.append("<ref_no><![CDATA["+ refNo +"]]></ref_no>");
								xmlBuff.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
								xmlBuff.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
								xmlBuff.append("<rate>").append("<![CDATA[" + rate+ "]]>").append("</rate>");
								xmlBuff.append("<line_no__sret>").append("<![CDATA[" + lineNoSret + "]]>").append("</line_no__sret>");
								xmlBuff.append("<line_no__invtrace>").append("<![CDATA[" + lineNoInvtrace + "]]>").append("</line_no__invtrace>");
								xmlBuff.append("<lot_no>").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
								xmlBuff.append("<analysis1><![CDATA["+ analysis1 +"]]></analysis1>");
								xmlBuff.append("<analysis2><![CDATA["+ analysis2 +"]]></analysis2>");
								xmlBuff.append("<analysis3><![CDATA["+ analysis3 +"]]></analysis3>");
								xmlBuff.append("<lot_sl>").append("<![CDATA[" + lotSl + "]]>").append("</lot_sl>");
								xmlBuff.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
								xmlBuff.append("</Detail2>");
								xmlBuff.append("\n");*/




				/*if(taxEnvSr.trim().length()>0 && taxEnvSr!=null)
				        		{
				        			if(lbFlag=true)
				        			{
				        				lds_drcr_rcp_det.retrieve(ls_tran_id_new , ll_line_no)     
				        				ds_tax_detbrow.reset()
				        				//lc_tax_amtdet = gf_calc_tax_ds(lds_drcr_rcp_det,ds_tax_detbrow,transer,ls_tran_id_new,ld_tax_date,"rate", "quantity",0,ls_curr_code,'2')       
				        			}
				        			else 
				        			{
				        						ds_salesreturn_retnline.retrieve(ls_sreturn_no , ll_line_no__sret)     
				        						ds_salesreturn_retnline.setitem(1,'line_no',ll_line_no)
				        						ds_tax_detbrow.reset()
				        						lc_tax_amtdet = gf_calc_tax_ds(ds_salesreturn_retnline,ds_tax_detbrow,transer,ls_tran_id_new,ld_tax_date,"rate__stduom", "quantity__stduom",0,ls_curr_code,'2')       
				        			}
				        		}*/
				/*if(taxAmtdet==0 )	//  if isnull(lc_tax_amtdet) or lc_tax_amtdet = -999999999 then lc_tax_amtdet = 0
				        		{
				        			taxAmtdet=0;
				        		}

				        		if(netAmt==0)
				        		{
				        			netAmt=0;
				        		}
				        		if(totNetAmt==0)
				        		{
				        			totNetAmt=0;
				        		}
				        		if(tottax==0)
				        		{
				        			tottax=0;
				        		}
				        		if(totdiscamt==0)
				        		{
				        			totdiscamt=0;
				        		}
				        		totNetAmt=totNetAmt+netAmt+taxAmtdet;


				        		sql="  update misc_drcr_rdet" + 
				        				"            set tax_amt = ?," + 
				        				"            net_amt = (net_amt + (? - ?))" + 
				        				"            where tran_id = ? " + 
				        				"				and line_no = ?";

				        		pstmt=conn.prepareStatement(sql);
				    			pstmt.setDouble(1,taxAmtdet);
				    			pstmt.setDouble(2,taxAmtdet);
				    			pstmt.setDouble(3,discount);
				    			pstmt.setString(4,tranIdNew);
				    			pstmt.setInt(5, lineNo);
				    			pstmt.executeUpdate();
				    			pstmt.close();
				    			pstmt=null;*/
			}
			pstmt1.close();
			pstmt1=null;
			rs.close();
			rs=null;

			sql="select SUM(net_amt) from misc_drcr_rdet where tran_id= ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranIdNew);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				amount=rs.getDouble(1);

			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			amountBc=amount *exchRate;

			sql=" update misc_drcr_rcp set amount = ? ,amount__bc= ?  where tran_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setDouble(1,amount);
			pstmt.setDouble(2,amountBc);
			pstmt.setString(3,tranIdNew);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;			


			sql="select line_no,ref_ser,ref_no,ref_bal_amt,adj_amt,mrp_value__adj "+
					" from misc_drcr_rcpinv where tran_id= ? ";
			pstmt1=conn.prepareStatement(sql);
			pstmt1.setString(1, tranIdAs);
			rs=pstmt1.executeQuery();
			while(rs.next())
			{
				lineNoRcpinv=rs.getInt("line_no");
				refSerRcpinv=rs.getString("ref_ser");
				refNoRcpinv=rs.getString("ref_no");
				refBalAmtRcpinv=rs.getDouble("ref_bal_amt");
				adjAmtRcpinv=rs.getDouble("adj_amt");
				mrpValueAdjRcpinv=rs.getDouble("mrp_value__adj");			


				sql="insert into misc_drcr_rcpinv (tran_id, line_no,ref_ser,ref_no,ref_bal_amt,adj_amt,mrp_value__adj) "+
						" Values (?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdNew);
				pstmt.setInt(2, lineNoRcpinv);
				pstmt.setString(3, refSerRcpinv);
				pstmt.setString(4, refNoRcpinv);
				pstmt.setDouble(5, refBalAmtRcpinv);
				pstmt.setDouble(6, adjAmtRcpinv);
				pstmt.setDouble(7, mrpValueAdjRcpinv);

				pstmt.executeUpdate();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				/*xmlBuff.append("<Detail3 dbID=\"\" domID=\""+lineNoRcpinv+"\" objContext=\"3\" objName=\"misc_drcr_rcp_cr\">");		
		 xmlBuff.append("<line_no><![CDATA["+ lineNoRcpinv   +"]]></line_no>");
		 xmlBuff.append("<ref_ser><![CDATA["+ refSerRcpinv   +"]]></ref_ser>");
		 xmlBuff.append("<ref_no><![CDATA["+ refNoRcpinv   +"]]></ref_no>");
		 xmlBuff.append("<ref_bal_amt><![CDATA["+ refBalAmtRcpinv   +"]]></ref_bal_amt>");
		 xmlBuff.append("<adj_amt><![CDATA["+  adjAmtRcpinv  +"]]></adj_amt>");
		 xmlBuff.append("<mrp_val__adj><![CDATA["+ mrpValueAdjRcpinv   +"]]></mrp_val__adj>");
		 xmlBuff.append("</Detail3>");*/



				//fetch c_misc_drcr_rcpinv into :ll_line_no_rcpinv,:ls_ref_ser_rcpinv,:ls_ref_no_rcpinv,:lc_ref_bal_amt_rcpinv,:lc_adj_amt_rcpinv,:lc_mrp_value__adj_rcpinv ;

				/*sql="update misc_drcr_rcp " + 
				"	set AMOUNT = ?, " + 
				"		 AMOUNT__BC = ? " + 
				"	where tran_id = ? ";

		pstmt=conn.prepareStatement(sql);
		pstmt.setDouble(1,totNetAmt);
		pstmt.setDouble(2,totNetAmt);
		pstmt.setString(3,tranIdNew);
		pstmt.executeUpdate();
		pstmt.close();
		pstmt=null;*/
			}
			pstmt1.close();
			pstmt1=null;
			rs.close();
			rs=null;
			/*	xmlBuff.append("</Header0>");
		xmlBuff.append("</group0>");
		xmlBuff.append("</DocumentRoot>");*/
			/*String xmlString = xmlBuff.toString().trim().replaceFirst("^([\\W]+)<","<");
		String drNtTranId="";
//		errString = saveData(xtraParams,siteCodeAs,xmlString,conn);
		errString = saveData(xtraParams,siteCodeAs,xmlBuff.toString(),conn);
		//System.out.println("@@@@@2: retString:"+errorString);
		//System.out.println("--retString finished--");
		if (errString.indexOf("Success") > -1)
		{
			System.out.println("@@@@@@3: Success"+errString);
			Document dom = genericUtility.parseString(errString);
			//System.out.println("dom>>>"+dom);
			tranIdNew = genericUtility.getColumnValue("TranID",dom);
		}
		else
		{
			//System.out.println("[SuccessSuccess" + errorString + "]");	
			conn.rollback();
			return errString;
		}*/		
			//errString = nvo_misc.gbf_retrieve_misc_drcr_rcp(ls_tran_id_new, ls_tran_id_new,1,siteCodeAs);
			MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
			errString= confDebitNote.confirm(tranIdNew,xtraParams, "" , conn);
			//System.out.println("After DrCrRcpConf---->["+errorString+"]");
			if(errString != null && errString.indexOf("CONFSUCCES") != -1)
			{
				errString = "";
			}

			if(errString.trim().length()==0)
			{
				sql=" update sreturn "+
						" set tran_id__crn = ? "+
						" where tran_id = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,tranIdAs);
				pstmt.setString(2,sreturnNo);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
			}

		}

		catch(Exception e)
		{			
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;


	}  //end manish
	private String generateTranId(String windowName, String siteCode,String ls_drcr_flag, String tranDateStr,String tranType, Connection conn) throws ITMException         //added tran type by manish mhatre on 28-feb-2020
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		String paySiteCode = "";
		String effectiveDate = "";
		java.sql.Date effDate = null;
		try
		{
			System.out.println("generateTranId() called");
			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			// System.out.println("selSql :"+selSql);
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString(1, windowName);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("keyString :" + keyString);
			System.out.println("keyCol :" + keyCol);
			System.out.println("tranSer :" + tranSer);
			xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>";
			xmlValues = xmlValues + "<drcr_flag>" + ls_drcr_flag + "</drcr_flag>";
			xmlValues = xmlValues + "<tran_type>" + tranType + "</tran_type>";      //added by manish mhatre on 28-feb-2020
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues :[" + xmlValues + "]");
			TransIDGenerator generatedTranid = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = generatedTranid.generateTranSeqID(tranSer, keyCol, keyString, conn);

			System.out.println("tranId :" + tranId);
		} catch (SQLException ex)
		{
			System.out.println("Exception ::" + selSql + ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		} catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return tranId;
	}

	private String gbfRetrieveDrCr(String tranId,String siteCode,String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		String errCode = "",keyFld = "" , expHdr = "",expDet = "", winName = "", ledgPostconf= "" , ledgPostConf = "",loginEmpCode = "",
				sql = "" , sql1 = "" ,winname ="",acctCode = "",cctrCode= "",invoiceId = "", tranSer = "";
		String acctReco = "", cctrReco = "", taxRecoAcct = "", taxRecoCctr = "", acctExch = "", cctrExch = "", acctRnd = "", cctrRnd = "";



		double recoAmount = 0,diffExchAmt = 0,roundAdj = 0,amountMap = 0, exchRateInv = 0, exchRatehdr = 0 ;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		HashMap HdrMap = null;
		HashMap DetMap = null;
		int lineNo = 0,findIndex = -1;
		ArrayList DetList = new ArrayList();
		ITMDBAccessEJB itmDBAccessEJB = null;
		itmDBAccessEJB = new ITMDBAccessEJB();
		E12GenericUtility genericUtility = new E12GenericUtility();
		int cntrHdr = 0,cntr = 0,insertedRow = 0,rows = 0;
		java.sql.Timestamp tranDate = null, today;
		//FinCommon finCommon = null;
		String tranType = "";
		try
		{
			if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				sql1 = "SELECT tran_id FROM drcr_inv  WHERE tran_id = ? for update ";
			}

			else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				sql1 = "SELECT tran_id FROM drcr_inv (updlock) WHERE tran_id = ? " ;
			}
			else
			{
				sql1 =" SELECT tran_id  FROM drcr_inv WHERE tran_id =  ? for update nowait" ;
			}
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1,tranId);
			rs1 = pstmt1.executeQuery();
			if(rs1.next())
			{
				keyFld = rs1.getString("tran_id") == null ? " ":rs1.getString("tran_id");
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			today = new java.sql.Timestamp(System.currentTimeMillis()) ;

			winname = "W_DRCRINV";

			sql = "select ledg_post_conf from transetup where upper(tran_window) = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"W_DRCRINV");
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ledgPostConf = rs.getString(1);
			}
			else
			{
				errCode = itmDBAccessEJB.getErrorString("","VTSEQ","","",conn);
				return errCode;
			}

			if("Y".equalsIgnoreCase(ledgPostConf))
			{
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
				tranDate = java.sql.Timestamp.valueOf(sdf.format(today) + " 00:00:00.000");

				sql = " update drcr_inv set tran_date = ? where tran_id   = ?" ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setString(2,tranId);
				pstmt.executeUpdate();		

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			sql = "select tran_id ,tran_date ,fin_entity ,acct_code , cctr_code ,eff_date , invoice_id , amount , curr_code , exch_rate , remarks , " +
					"site_code,  anal_code ,  drcr_flag  ,  tran_id__rcv,  confirmed,conf_date ,emp_code__aprv, tran_type , tran_ser ,   " +
					"due_date ,cr_term ,amount__bc, item_ser , round_adj , sundry_type , sundry_code ,  " +
					"  acct_code__cf , cctr_code__cf  , diff_amt_exch ,voucher_no , invvouc_flag , parent__tran_id , rev__tran " +
					" from drcr_inv where tran_id = ?";


			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				HdrMap = new HashMap();
				HdrMap.put("tran_id",tranId);
				HdrMap.put("tran_date",rs.getTimestamp("tran_date"));
				HdrMap.put("fin_entity",rs.getString("fin_entity"));
				HdrMap.put("acct_code",rs.getString("acct_code"));
				cctrCode = rs.getString("cctr_code");
				if (cctrCode == null || "null".equals(cctrCode))
				{
					cctrCode = "    ";
				}

				HdrMap.put("cctr_code",cctrCode);
				HdrMap.put("eff_date",rs.getTimestamp("eff_date"));
				HdrMap.put("invoice_id",rs.getString("invoice_id"));
				HdrMap.put("amount",rs.getDouble("amount"));
				HdrMap.put("curr_code",rs.getString("curr_code"));
				exchRatehdr = rs.getDouble("exch_rate");
				if (exchRatehdr == 0 )
				{
					exchRatehdr = 1;
				}
				HdrMap.put("exch_rate",exchRatehdr);
				HdrMap.put("remarks",rs.getString("remarks"));
				HdrMap.put("site_code",rs.getString("site_code"));
				HdrMap.put("anal_code",rs.getString("anal_code"));
				HdrMap.put("drcr_flag",rs.getString("drcr_flag"));
				HdrMap.put("tran_id__rcv",rs.getString("tran_id__rcv"));
				HdrMap.put("confirmed",rs.getString("confirmed"));
				HdrMap.put("conf_date",rs.getTimestamp("conf_date"));
				HdrMap.put("emp_code__aprv",rs.getString("emp_code__aprv"));
				HdrMap.put("tran_type",rs.getString("tran_type"));
				HdrMap.put("tran_ser",rs.getString("tran_ser"));
				tranSer = rs.getString("tran_ser");
				HdrMap.put("due_date",rs.getTimestamp("due_date"));
				HdrMap.put("cr_term",rs.getString("cr_term"));
				HdrMap.put("amount__bc",rs.getDouble("amount__bc"));
				HdrMap.put("item_ser",rs.getString("item_ser"));
				HdrMap.put("round_adj",rs.getDouble("round_adj"));
				HdrMap.put("sundry_type",rs.getString("sundry_type"));
				HdrMap.put("sundry_code",rs.getString("sundry_code"));
				HdrMap.put("acct_code__cf",rs.getString("acct_code__cf"));
				cctrCode = rs.getString("cctr_code__cf");
				if (cctrCode == null || "null".equals(cctrCode))
				{
					cctrCode = "    ";
				}
				HdrMap.put("cctr_code__cf",cctrCode);
				HdrMap.put("diff_amt_exch",rs.getDouble("diff_amt_exch"));
				HdrMap.put("voucher_no",rs.getString("voucher_no"));
				HdrMap.put("invvouc_flag",rs.getString("invvouc_flag"));
				HdrMap.put("parent__tran_id",rs.getString("parent__tran_id"));
				HdrMap.put("rev__tran",rs.getString("rev__tran"));

				System.out.println(" Header Map..........");
				System.out.println(HdrMap.toString());
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = " select acct_code, cctr_code, sum(tax_amt) as tax_amt, case when sum(reco_amount) is null then 0 else sum(reco_amount) end as reco_amt "
					+ " from taxtran where tran_code = ? and tran_id = ? and tax_amt <> 0 and (case when reco_amount is null then 0 else reco_amount end) <> 0 and effect <> 'N' group by acct_code, cctr_code";              
			pstmt = conn.prepareStatement(sql);

			//Changed by wasim on 24-MAY-2017 to check tran_ser null or not [START]
			if(HdrMap !=null && HdrMap.get("tran_ser") != null)
			{
				tranSer = (String) HdrMap.get("tran_ser");
			}
			//Changed by wasim on 24-MAY-2017 to check tran_ser null or not [END]

			pstmt.setString(1, tranSer);
			pstmt.setString(2,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				acctCode = rs.getString("acct_code");
				acctCode = rs.getString("cctr_code");
				if (acctCode == null)
				{
					if (!"CRNVOU".equals(tranSer) )
					{
						sql = " select acct_code__sal, cctr_code__sal  "
								+ " from invoice  where invoice_id = ? ";              
						pstmt1 = conn.prepareStatement(sql);

						pstmt1.setString(1, (String) HdrMap.get("invoice_id"));
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							acctCode = rs1.getString("acct_code__sal");
							acctCode = rs1.getString("cctr_code__sal");
						}
						else
						{
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							errCode = "VTINVCD1";
							break;
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					else
					{
						errCode = "VTACTAX"; 
						break;
					}
				}
				if (cctrCode == null || cctrCode.trim().length() == 0 )
				{
					cctrCode = (String) HdrMap.get("cctr_code");
				}
				if (cctrCode == null || "null".equals(cctrCode))
				{
					cctrCode = "    ";
				}

				DetMap = new HashMap();
				DetMap.put("tran_id",(String) HdrMap.get("tran_id"));
				lineNo++;
				DetMap.put("line_no",(String) "" +lineNo);
				DetMap.put("acct_code",acctCode);
				DetMap.put("cctr_code",cctrCode);
				DetMap.put("amount", rs.getDouble("tax_amt") - rs.getDouble("reco_amt"));
				DetMap.put("emp_code","");
				DetMap.put("anal_code","");
				DetMap.put("exch_rate", (double) Double.parseDouble( "" + HdrMap.get("exch_rate")));
				DetList.add(DetMap);

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (errCode != null && errCode.trim().length() > 0 )
			{
				return errCode;
			}
			recoAmount = 0;

			//Changed by wasim on 24-MAY-2017 to check tran_type null or not [START]
			//taxRecoCctr = finCommon.getAcctDetrTtype(" ", " ", "TAXRECO", (String) HdrMap.get("tran_type"), conn);
			if(HdrMap !=null && HdrMap.get("tran_type") != null)
			{
				tranType = (String) HdrMap.get("tran_type");
			}
			taxRecoCctr = finCommon.getAcctDetrTtype(" ", " ", "TAXRECO", tranType, conn);
			//Changed by wasim on 24-MAY-2017 to check tran_type null or not [END]
			if (taxRecoCctr != null && taxRecoCctr.trim().length() > 0)
			{
				taxRecoAcct = taxRecoCctr.substring(0,taxRecoCctr.indexOf(","));
				taxRecoCctr = taxRecoCctr.substring(taxRecoCctr.indexOf(",")+1);
			}

			sql = " select acct_code__reco,	cctr_code__reco, case when sum(reco_amount) is null then 0 else sum(reco_amount) end reco_amount "
					+ " from taxtran where tran_code = ? and tran_id = ? and tax_amt <> 0 and (case when reco_amount is null then 0 else reco_amount end) <> 0  and effect <> 'N' "
					+ " group by acct_code__reco,	cctr_code__reco";              
			pstmt = conn.prepareStatement(sql);

			//Changed by wasim on 24-MAY-2017
			//pstmt.setString(1, (String) HdrMap.get("tran_ser"));
			pstmt.setString(1, tranSer);
			pstmt.setString(2,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{

				acctReco = rs.getString("acct_code__reco");
				cctrReco = rs.getString("cctr_code__reco");
				recoAmount = rs.getDouble("reco_amount");
				if (acctReco == null || acctReco.trim().length() == 0 )
				{
					cctrReco = taxRecoCctr;
					acctReco = taxRecoAcct;
				}

				// If ACCT Code is NULL/SPACE Then Exit
				if (acctReco == null || acctReco.trim().length() == 0 )
				{
					errCode = "VTACCTRECO"; 
					break;
				}
				if (cctrReco == null || "null".equals(cctrReco))
				{
					cctrReco = "    ";
				}
				DetMap = new HashMap();
				DetMap.put("tran_id",(String) HdrMap.get("tran_id"));
				lineNo++;
				DetMap.put("line_no",(String) "" +lineNo);
				DetMap.put("acct_code",acctReco);
				DetMap.put("cctr_code",cctrReco);
				DetMap.put("amount", recoAmount);
				DetMap.put("emp_code","");
				DetMap.put("anal_code","");
				DetMap.put("exch_rate",(double) Double.parseDouble( "" + HdrMap.get("exch_rate")));
				DetList.add(DetMap);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			if (errCode != null && errCode.trim().length() > 0 )
			{
				return errCode;
			}

			sql = " select tran_id ,line_no ,acct_code,cctr_code ,drcr_amt,exch_rate__inv from drcr_invdet where tran_id = ? and drcr_amt <> 0";              
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				findIndex = -1;
				findIndex = findExistingIndex(DetList,rs.getString("acct_code"), rs.getString("cctr_code"), rs.getDouble("exch_rate__inv"));
				System.out.println(" 16/06/14 findExistingIndex [" + findIndex + "] Map [" + DetList.toString() + "]");
				if (findIndex > -1) 
				{
					DetMap = (HashMap) DetList.get(findIndex);
					amountMap = (double) Double.parseDouble( "" + DetMap.get("amount"));
					System.out.println(" 16/06/14 findExistingIndex [" + findIndex + "] amountMap [" + amountMap + "] amount [" + rs.getDouble("drcr_amt") + "]");
					DetMap.put("amount", amountMap + rs.getDouble("drcr_amt"));
					DetList.set(findIndex, DetMap);
				}
				else
				{
					DetMap = new HashMap();
					DetMap.put("tran_id", rs.getString("tran_id"));
					lineNo++;
					DetMap.put("line_no",(String) "" +lineNo);
					DetMap.put("amount", rs.getDouble("drcr_amt"));
					DetMap.put("emp_code", "");
					DetMap.put("anal_code", "");
					DetMap.put("acct_code", rs.getString("acct_code"));

					cctrCode = rs.getString("cctr_code");

					if (cctrCode == null || "null".equals(cctrCode))
					{
						cctrCode = "    ";
					}


					DetMap.put("cctr_code", cctrCode );
					exchRateInv = rs.getDouble("exch_rate__inv");
					if (exchRateInv == 0)
					{
						exchRateInv = (double) Double.parseDouble( "" + HdrMap.get("exch_rate"));
					}
					DetMap.put("exch_rate",exchRateInv);
					DetList.add(DetMap);
				}
				System.out.println(DetMap.toString());
			}    
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			//Changed by wasim on 24-MAY-2017 to check diff_amt_exch null or not [START]
			//diffExchAmt = (double) Double.parseDouble( "" +HdrMap.get("diff_amt_exch"));
			if(HdrMap !=null && HdrMap.get("diff_amt_exch") != null)
			{
				diffExchAmt = (double) Double.parseDouble( "" +HdrMap.get("diff_amt_exch"));
			}
			//Changed by wasim on 24-MAY-2017 to check tran_type null or not [END]
			if (diffExchAmt != 0 )
			{
				acctExch = (String) HdrMap.get("acct_code__cf"); 
				cctrExch = (String) HdrMap.get("cctr_code__cf");
				if (cctrExch == null || "null".equals(cctrExch))
				{
					cctrExch = "    ";
				}

				DetMap = new HashMap();
				DetMap.put("tran_id", rs.getString("tran_id"));
				lineNo++;
				DetMap.put("line_no",(String) "" +lineNo);
				DetMap.put("amount", diffExchAmt);
				DetMap.put("emp_code", "");
				DetMap.put("anal_code", "");
				DetMap.put("acct_code", acctExch);
				DetMap.put("cctr_code", cctrExch);
				DetMap.put("exch_rate", 1);
				DetList.add(DetMap);

			}

			//Changed by wasim on 24-MAY-2017 to check round_adj null or not [START]
			//roundAdj = (double) Double.parseDouble( "" +HdrMap.get("round_adj"));
			if(HdrMap !=null && HdrMap.get("round_adj") != null)
			{
				roundAdj = (double) Double.parseDouble( "" +HdrMap.get("round_adj"));
			}
			//Changed by wasim on 24-MAY-2017 to check round_adj null or not [END]
			if ( roundAdj != 0 )
			{

				cctrRnd = finCommon.getFinparams("999999", "DRCR_ACCT_RND", conn);
				if (cctrRnd != null && cctrRnd.trim().length() > 0)
				{
					acctRnd = cctrRnd.substring(0,cctrRnd.indexOf(";"));
					cctrRnd = cctrRnd.substring(cctrRnd.indexOf(";")+1);
				}
				if (cctrRnd == null || "null".equals(cctrRnd))
				{
					cctrRnd = "    ";
				}

				DetMap = new HashMap();
				DetMap.put("tran_id", rs.getString("tran_id"));
				lineNo++;
				DetMap.put("line_no",(String) "" +lineNo);
				DetMap.put("amount", roundAdj);
				DetMap.put("emp_code", "");
				DetMap.put("anal_code", "");
				DetMap.put("acct_code", acctRnd);
				DetMap.put("cctr_code", cctrRnd);
				DetMap.put("exch_rate", 1);
				DetList.add(DetMap);
			}
			sql = "Insert into drcr_invacct (TRAN_ID,LINE_NO,ACCT_CODE,CCTR_CODE,EXCH_RATE__INV,AMOUNT) values (?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			int maxline = 0;
			System.out.println("08/01/15 DetList ["+DetList.toString() + "]");
			for(int i = 0 ; i < DetList.size() ; i++)
			{

				double tempAmt = (double) Double.parseDouble( "" +DetMap.get("amount"));
				if (tempAmt != 0)
				{
					maxline++;
					DetMap = (HashMap) DetList.get(i);
					System.out.println(" Insert in drcr_invacct ctr [" + i + "] Map [" + HdrMap.toString() + "]");
					pstmt.setString(1,(String) DetMap.get("tran_id"));
					pstmt.setInt(2,maxline);
					pstmt.setString(3,(String) DetMap.get("acct_code"));
					cctrCode = (String) DetMap.get("cctr_code");
					if (cctrCode == null || "null".equals(cctrCode))
					{
						cctrCode = "    ";
					}
					pstmt.setString(4,cctrCode);
					pstmt.setDouble(5,(double) Double.parseDouble( "" +DetMap.get("exch_rate")));
					pstmt.setDouble(6,(double) Double.parseDouble( "" +DetMap.get("amount")));
					pstmt.addBatch();
					pstmt.clearParameters();
				}
			}
			if (maxline > 0 ) 
			{
				pstmt.executeBatch();
			}
			pstmt.close();
			pstmt = null;

			errCode = gbfPostDrCr(tranId, HdrMap,DetList,xtraParams,conn);

		}
		catch(SQLException sqx)
		{
			SQLException ex ;

			while (sqx != null)
			{
				if((CommonConstants.DB_NAME).equalsIgnoreCase("oracle") && (sqx).toString().indexOf("ORA-00054") > -1)
				{
					errCode = "VTLCKERR";

					System.out.println("The SQLException occurs in UpdatStock [Stockupdate]  Exception[Recod is locked try after some time]  ["+sqx.toString() + "]");
				}
				else
				{
					errCode = "ERROR";
					System.out.println("The SQLException occurs in UpdatStock [Stockupdate]  Exception ["+sqx.toString() + "]");
				}
				ex = sqx;

				sqx.printStackTrace();
				sqx =  sqx.getNextException();
				if(sqx == null)
				{
					sqx = ex;
					break;
				}
			}
			throw new ITMException(sqx);
		}
		catch(Exception se12){
			System.out.println("Exception in UpdatStock [Stockupdate]"+se12);
			se12.printStackTrace();
			throw new ITMException(se12);

		}
		return errCode;
	}
	private String gbfPostDrCr(String tranId, HashMap HdrMap,ArrayList DetList, String xtraParams, Connection conn) throws RemoteException,ITMException
	{
		String errCode = "";
		//FinCommon finCommon = null;
		try
		{
			//finCommon = new  FinCommon();
			errCode = gbfPostDrcrInvhdr(tranId, HdrMap, xtraParams,conn);
			System.out.println("16/06/14 after gbfPostDrcrInvhdr errCode [" + errCode + "]");
			if(errCode == null || errCode.trim().length() ==0)
			{
				errCode = gbfPostDrcrInvdet(tranId, HdrMap,DetList, xtraParams,conn);
				System.out.println("16/06/14 after gbfPostDrcrInvdet errCode [" + errCode + "]");
				if(errCode != null && errCode.trim().length() >0)
				{
					return errCode;
				}
			}
			System.out.println("16/06/14 after gbfPostDrcrInvdet errCode [" + errCode + "]");
			errCode = finCommon.checkGlTranDrCr("CRNINV",tranId,conn);

		}
		catch (Exception e)
		{
			throw new ITMException(e);
		}
		return errCode ;

	}
	private String gbfPostDrcrInvhdr(String tranId,HashMap HdrMap,String xtraParams,Connection conn)throws RemoteException,ITMException
	{
		System.out.println("gbfPostDrcrinv called..............");
		String errString = "";
		String currCode = "";
		String siteCode = "";
		String siteCodeFor = "";
		String finEntity = "";
		String acctCode = "";
		String cctrCode = ""; 
		String recoAcctCode = "";
		String recoCctrCode = "";
		String taxAcctCode = "";
		String taxCctrCode = "";
		String acctCode1 = "";
		String cctrCode1 = "";
		String acctCodeAr = "";
		String cctrCodeAr = "";
		String projectCode = "";
		String analCode = "";
		String bankCode = "";
		String remarks = "";
		String sundryType = "";
		String contactCode = "";
		String sundryCode = "";
		String sundryTypeFor = "";
		String sundryCodeFor = "";
		String tranType = "";
		String empCode = "";
		String refNO = "";
		String payMode = "";
		String rcpMode = "";
		String batchNo = "";
		String confirm = "";
		String loginEmpCode = "";
		String winName = "";
		String editOption = "";
		String sql = "";
		String hcurrCode = "";
		String errorType = "";
		String payslip = "";
		String partyDocRef = "";
		String refSer = "";
		String refNo = "";
		String salesPers = "";
		String status = "";
		String lineNoPef = "";

		String tranSer = "",drcrFlag = "";
		int count = 0;
		int lineNo = 0;
		double exchRate = 0.0;
		double hexchRate = 0.0;
		double netAmount = 0.0;
		double amount = 0.0;
		double taxAmount = 0.0;
		double recoAmount = 0.0;
		double debit  = 0.0;
		double credit = 0.0;
		double totAmt = 0.0;
		double advAmt = 0.0;
		double adjAmt = 0.0;
		double advAdj = 0.0;
		double recoverAmt = 0.0;
		java.sql.Timestamp effDate = null;
		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp refDate = null;
		java.sql.Timestamp billDate = null;
		java.sql.Timestamp dueDate = null;

		HashMap DetMap = null;
		HashMap miscPayMap = null;
		HashMap glTraceMap = null;
		HashMap sundryBalMap = null;
		HashMap bankTranLogMap = null;
		HashMap detListMap = null;
		HashMap recofiltMap = null;
		HashMap recodataMap = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//FinCommon finCommon = null;
		DistCommon distCommon = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		ValidatorEJB validatorEJB = new ValidatorEJB();
		//		GenericUtility genericUtility = new GenericUtility();
		try
		{
			//finCommon = new  FinCommon();
			distCommon = new DistCommon();
			itmDBAccessEJB = new ITMDBAccessEJB();
			loginEmpCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			//tranId = (String)HdrMap.get("tran_id");
			tranSer = (String)HdrMap.get("tran_ser");

			tranDate = (Timestamp)HdrMap.get("tran_date");
			effDate = (Timestamp)HdrMap.get("eff_date");
			finEntity = (String)HdrMap.get("fin_entity");
			siteCode = (String)HdrMap.get("site_code");
			siteCodeFor = (String)HdrMap.get("site_code__for");
			projectCode = (String)HdrMap.get("proj_code");
			hcurrCode = (String)HdrMap.get("curr_code");
			hexchRate = (double) Double.parseDouble( "" + HdrMap.get("exch_rate"));
			remarks = (String)HdrMap.get("remarks");
			confirm = (String)HdrMap.get("confirm");
			dueDate = (Timestamp)HdrMap.get("due_date");

			sundryType = (String)HdrMap.get("sundry_type");
			sundryCode = (String)HdrMap.get("sundry_code");
			amount = (double) Double.parseDouble( "" + HdrMap.get("amount")); 
			remarks = (String)HdrMap.get("remarks");

			glTraceMap = new HashMap();
			glTraceMap.put("tran_date",new Timestamp(tranDate.getTime()));
			glTraceMap.put("eff_date",new Timestamp(effDate.getTime()));
			glTraceMap.put("fin_entity", finEntity);
			glTraceMap.put("site_code", siteCode);
			glTraceMap.put("sundry_type",sundryType);
			glTraceMap.put("sundry_code",sundryCode);
			glTraceMap.put("acct_code", (String)HdrMap.get("acct_code"));

			glTraceMap.put("cctr_code", (String)HdrMap.get("cctr_code")==null?"":(String)HdrMap.get("cctr_code"));
			glTraceMap.put("emp_code", "");
			glTraceMap.put("anal_code", (String)HdrMap.get("anal_code"));
			glTraceMap.put("curr_code", (String)HdrMap.get("curr_code")==null?hcurrCode:(String)HdrMap.get("curr_code"));

			glTraceMap.put("exch_rate", (double) Double.parseDouble( "" + HdrMap.get("exch_rate")));
			drcrFlag = (String) HdrMap.get("drcr_flag");
			System.out.println("gltrace  drcrFlag ["+ drcrFlag + "]amount [" + amount+ "]");
			if("D".equals(drcrFlag))
			{
				glTraceMap.put("dr_amt",amount);
				glTraceMap.put("cr_amt",0.0);
			}
			else
			{
				glTraceMap.put("dr_amt", 0.0);
				glTraceMap.put("cr_amt",amount);
			}
			glTraceMap.put("ref_type","F");
			glTraceMap.put("ref_ser", tranSer);
			glTraceMap.put("ref_id", tranId);
			glTraceMap.put("remarks",remarks);

			System.out.println("848 glTraceUpdate Called glTraceMap[" + glTraceMap.toString() + "]");
			errString = finCommon.glTraceUpdate(glTraceMap,conn);
			if(errString != null && errString.trim().length() > 0)
			{
				System.out.println("ERROR IN GLTRACE ...........");

				return errString;
			}

			// Populate sundry balance structure
			sundryBalMap = new HashMap();
			sundryBalMap.put("tran_date",new Timestamp(tranDate.getTime()));
			sundryBalMap.put("eff_date",new Timestamp(effDate.getTime()));
			sundryBalMap.put("fin_entity", finEntity);
			sundryBalMap.put("site_code", siteCode);
			sundryBalMap.put("sundry_type", sundryType);
			sundryBalMap.put("sundry_code", sundryCode);
			sundryBalMap.put("acct_code", (String)HdrMap.get("acct_code"));
			sundryBalMap.put("cctr_code", (String)HdrMap.get("cctr_code")==null?"":(String)HdrMap.get("cctr_code"));
			sundryBalMap.put("curr_code", (String)HdrMap.get("curr_code"));
			sundryBalMap.put("exch_rate", (double) Double.parseDouble( "" + HdrMap.get("exch_rate")));

			if("D".equals(drcrFlag))
			{
				sundryBalMap.put("dr_amt", amount);
				sundryBalMap.put("cr_amt", 0.0);
			}

			else
			{
				sundryBalMap.put("dr_amt", 0.0);
				sundryBalMap.put("cr_amt", amount);
			}


			sundryBalMap.put("adv_amt", 0.0);

			System.out.println("2366 sundryBaleUpdate Called..............");
			errString = finCommon.gbf_sundrybal_upd(sundryBalMap, conn);
			if(errString != null && errString.trim().length() > 0)
			{
				System.out.println("error is sundryBaleUpdate ..........."+errString);

				return errString;
			}


			///Populate the misc_payables structure				/////
			// insert into misc_payable	
			miscPayMap = new HashMap();
			miscPayMap.put("tran_date",new Timestamp(tranDate.getTime()));
			miscPayMap.put("tran_ser",(String)HdrMap.get("tran_ser"));
			miscPayMap.put("ref_no",(String)HdrMap.get("ref_no"));
			miscPayMap.put("ref_date", new Timestamp(tranDate.getTime()));			
			//miscPayMap.put("bill_no", (String)HdrMap.get("bill_no"));
			//miscPayMap.put("bill_date", new Timestamp(billDate.getTime()));
			miscPayMap.put("sundry_type",(String)HdrMap.get("sundry_type")==null?"O":(String)HdrMap.get("sundry_type"));
			miscPayMap.put("sundry_code", (String)HdrMap.get("sundry_code")==null?"O":(String)HdrMap.get("sundry_code"));
			miscPayMap.put("acct_code", (String)HdrMap.get("acct_code"));
			miscPayMap.put("cctr_code", (String)HdrMap.get("cctr_code")==null?"":(String)HdrMap.get("cctr_code"));
			miscPayMap.put("curr_code", (String)HdrMap.get("curr_code"));
			miscPayMap.put("exch_rate", (double) Double.parseDouble( "" + HdrMap.get("exch_rate")));


			miscPayMap.put("due_date", new Timestamp(dueDate.getTime()));
			miscPayMap.put("site_code",siteCode);
			miscPayMap.put("fin_entity",finEntity);
			if("C".equals(drcrFlag))
			{
				miscPayMap.put("tot_amt", amount);
			}
			else
			{
				miscPayMap.put("tot_amt", -1 * amount);
			}
			sql = "select bank_code   from site where site_code = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				bankCode = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select pay_mode from sales_pers where sales_pers = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, salesPers);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				payMode = rs.getString(1)==null?"Q":rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			miscPayMap.put("bank_code", bankCode);
			miscPayMap.put("auto_pay", "Y");
			miscPayMap.put("pay_mode", payMode);
			miscPayMap.put("paid", "N");
			miscPayMap.put("adv_amt", 0.0);
			miscPayMap.put("hold_amt", 0.0);
			miscPayMap.put("supp_bill_amt","0.0");
			miscPayMap.put("tds_amt","0.0");


			System.out.println("884 sundryBaleUpdate Called..............");
			errString = finCommon.gbfMiscPayUpd(miscPayMap, conn);
			if(errString != null && errString.trim().length() > 0)
			{
				return errString;
			}
			if(confirm == null || "null".equals(confirm))
			{
				confirm = "N";
			}
			/*if(!"Y".equals(confirm))
			{
				sql = "update drcr_inv set confirmed = 'Y', conf_date = ? ,emp_code__aprv = ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
				pstmt.setString(2, loginEmpCode);
				pstmt.setString(3, tranId);
				int updateCoount = pstmt.executeUpdate();
				System.out.println("no of row update = "+updateCoount);
				pstmt.close();
				pstmt = null;
				if(updateCoount > 0)
				{
					errString= itmDBAccessEJB.getErrorString("","VTCICONF3 ","","",conn);
					return errString;
				}
			}*/



		}
		catch(Exception e)
		{
			errString = "ERROR";
			throw new ITMException(e);
		}
		return errString;
	}

	private String gbfPostDrcrInvdet(String tranId,HashMap HdrMap,ArrayList DetList,String xtraParams,Connection conn)throws RemoteException,ITMException
	{
		System.out.println("gbfPostDrcrInvdet called........");
		String errString = "";
		String linkType = "";
		String currCode = "";
		String siteCode = "";
		String siteCodeFor = "";
		String finEntity = "";
		HashMap DetMap = null;
		String acctCode = "";
		String cctrCode = "";
		String acctCodePay = "";
		String cctrCodePay = "";
		String acctCodeAr = "";
		String cctrCodeAr = "";
		String tranSer = "";
		String projectCode = "";
		String analCode = "";
		String bankCode = "";
		String remarks = "";
		String sundryType = "";
		String sundryCode = "";
		String sundryTypeFor = "";
		String sundryCodeFor = "";
		String contactCode = "";
		String empCode = "";
		String refNO = "";
		String rcpMode = "";
		String batchNo = "";
		String tranType = "";
		String confirm = "";
		String loginEmpCode = "";
		String winName = "";
		String editOption = "";
		String confirmed = "";
		String sql = "";
		String errorType = "";
		String bdFluctuationCf = "";
		String acctCodeBal = "";
		String cctrCodeBal = "";
		String basecurrCode = "";
		String projCode = "";
		String custCode = "";
		String salesPers = "";
		String refSer = "";
		String refNo = "";
		String acctCodeCf = "";
		String payslip = "";
		//String tranId = "";
		boolean isInBaseCurr =false;
		int count = 0;
		int cnt1 = 0 ;
		int cnt = 0;
		double exchRate = 0.0;
		double netAmount = 0.0;
		double amount = 0.0;
		double advAmt = 0.0;
		double netAmt = 0.0;
		double chqAmt = 0.0;
		double debit  = 0.0;
		double credit = 0.0;
		double totAmt = 0.0;
		double ediOption = 0.0;
		double netAmountBc = 0.0;
		Date effDate = null;
		Date tranDate = null;
		Date refDate = null;
		HashMap detListMap = null;
		HashMap glTraceMap = null;
		HashMap sundryBalMap = null;
		HashMap bankTranLogMap = null;
		HashMap receivablesMap = null;
		HashMap epcadjMap = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//FinCommon finCommon = null;
		ValidatorEJB validatorEJB = new ValidatorEJB();
		//		GenericUtility genericUtility = new GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = null;
		try
		{
			//finCommon =new  FinCommon();

			loginEmpCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");

			java.util.Date today=	new java.sql.Date(new java.util.Date().getTime());

			itmDBAccessEJB = new ITMDBAccessEJB();

			linkType = (String)HdrMap.get("link_type");
			//tranId = (String)HdrMap.get("tran_id");

			tranDate = (Date)HdrMap.get("tran_date");
			refDate = (Date)HdrMap.get("ref_date");
			effDate = (Date)HdrMap.get("eff_date");
			remarks = (String)HdrMap.get("remarks");

			finEntity = (String)HdrMap.get("fin_entity");
			siteCode = (String)HdrMap.get("site_code");
			tranSer = (String)HdrMap.get("tran_ser");
			bankCode = (String)HdrMap.get("bank_code");

			acctCode = (String)HdrMap.get("acct_code");
			cctrCode = (String)HdrMap.get("cctr_code");

			currCode = (String)HdrMap.get("curr_code");
			exchRate = (double) Double.parseDouble( "" + HdrMap.get("exch_rate"));

			System.out.println("DetList SIZE "+DetList.size());
			System.out.println("DetList SIZE ["+DetList.toString() + "]");

			for (int ctr =0; ctr < DetList.size(); ctr++)
			{

				System.out.println("no of times "+ctr);
				DetMap = new HashMap();
				DetMap = (HashMap) DetList.get(ctr);


				sundryCode = (String)DetMap.get("sundry_code");
				sundryType = (String)DetMap.get("sundry_type");

				acctCode = (String)DetMap.get("acct_code");
				cctrCode = (String)DetMap.get("cctr_code");
				//empCode = (String)DetMap.get("emp_code");
				//analCode = (String)DetMap.get("anal_code");

				exchRate = (double) Double.parseDouble( "" + DetMap.get("exch_rate"));
				analCode = (String)DetMap.get("anal_code");
				analCode = (String)DetMap.get("anal_code");
				amount = (double) Double.parseDouble( "" + DetMap.get("amount"));


				System.out.println("DetMap SIZE "+DetMap);

				glTraceMap = new HashMap();
				glTraceMap.put("tran_date", new Timestamp(tranDate.getTime()));
				glTraceMap.put("eff_date",new Timestamp(effDate.getTime()));
				glTraceMap.put("fin_entity", finEntity);
				glTraceMap.put("site_code", siteCode);
				glTraceMap.put("sundry_type", sundryType);
				glTraceMap.put("sundry_code", sundryCode);
				glTraceMap.put("acct_code", acctCode);
				glTraceMap.put("cctr_code", cctrCode);
				//glTraceMap.put("emp_code", empCode);
				//glTraceMap.put("anal_code", analCode);
				glTraceMap.put("curr_code", currCode);
				glTraceMap.put("exch_rate", exchRate);
				if("D".equals(HdrMap.get("drcr_flag")))
				{
					if(amount > 0 )
					{
						glTraceMap.put("dr_amt",0.0);
						glTraceMap.put("cr_amt",amount);
					}
					else
					{
						glTraceMap.put("dr_amt", -1 * amount);
						glTraceMap.put("cr_amt",0.0);
					}
				}
				else
				{
					if(amount > 0 )
					{
						glTraceMap.put("dr_amt", amount);
						glTraceMap.put("cr_amt", 0.0 );
					}
					else
					{
						glTraceMap.put("dr_amt", 0.0);
						glTraceMap.put("cr_amt",  -1 * amount);
					}
				}

				glTraceMap.put("ref_type","F");
				glTraceMap.put("ref_ser", tranSer);
				glTraceMap.put("ref_id", tranId);
				glTraceMap.put("remarks", remarks);

				System.out.println("1766 glTraceUpdate Called..drcrflag [" + HdrMap.get("drcr_flag") + "] glTraceMap [" + glTraceMap + "]");
				errString = finCommon.glTraceUpdate(glTraceMap,conn); 
				if(errString != null && errString.trim().length() > 0)
				{
					return errString;
				}
			}
		}
		catch(Exception e)
		{
			errString = "ERROR";
			throw new ITMException(e);
		}
		return errString;

	}
	private int findExistingIndex(ArrayList detList, String acctCode, String cctrCode, double exchRate)throws RemoteException,ITMException
	{
		int findIndex  = -1;
		HashMap detMap = null;
		String acctCodeMap = null, cctrCodeMap = null;
		double exchRateMap = 0;
		try
		{
			for(int ctr=0 ; ctr < detList.size() ; ctr++)
			{
				detMap = (HashMap) detList.get(ctr);
				acctCodeMap = (String) detMap.get("acct_code");
				cctrCodeMap = (String) detMap.get("cctr_code");
				exchRateMap = (double) Double.parseDouble( "" + detMap.get("exch_rate"));
				if (acctCodeMap.trim().equalsIgnoreCase(acctCode.trim()) && cctrCodeMap.trim().equalsIgnoreCase(cctrCode.trim()) && exchRateMap == exchRate)
				{
					findIndex = ctr;
					break;
				}

			}
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return findIndex;
	}
	public String retrieveMiscDrcrRcp(String businessObj, String tranIdFr,String xtraParams, String forcedFlag) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		int cnt = 0;

		try
		{
			ConnDriver connDriver = new ConnDriver();
			conn = getConnection();
			conn.setAutoCommit(false);
			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1,businessObj);
			rs = pStmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1,serviceCode);
			rs = pStmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			Service service = new Service();
			Call call = (Call)service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));			
			call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI); 
			Object[] aobj = new Object[4];

			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String(forcedFlag);

			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);
			System.out.println("Return string from NVO is:==>["+retString+"]");	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (pStmt != null )
				{
					pStmt.close();
					pStmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				if( conn != null ){
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{}
		}
		return retString;
	}
	private String checkNull(String input)
	{
		if(input == null)
		{
			input = "";
		}
		return input;
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

	//Changed by wasim on 24-MAY-2017 for migration to retrieve DRCRrcp [START]

	public  String getError(String message,String Code, Connection conn)  throws ITMException, Exception
	{
		String mainStr ="";

		try
		{
			String errString = "";
			errString =  new ITMDBAccessEJB().getErrorString("",Code,"","",conn);

			String begPart = errString.substring(0,errString.indexOf("</description>"));
			String endDesc = errString.substring(errString.indexOf("</description>"),errString.length());

			mainStr = begPart + message + endDesc;
			begPart = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return mainStr;
	}

	//Changed by wasim on 24-MAY-2017 for migration to retrieve DRCRrcp [END]
	//Changed by wasim on 24-MAY-2017 for migration to retrieve DRCRrcp [START]
	private String saveData(String xtraParams,String siteCode, String xmlString, Connection conn) throws ITMException
	{
		//System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		String userId =""; //Added By PriyankaC on 15/03/2018. [START]
		MasterStatefulLocal masterStateful = null; // for ejb3
		E12GenericUtility genericUtility=new E12GenericUtility();
		ibase.utility.UserInfoBean userInfoBean=new UserInfoBean();

		try
		{
			//Added By PriyankaC on 15/03/2018. [START]
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//System.out.println("userId" + userId + "]");
			//Added By PriyankaC on 15/03/2018. [END]
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			//System.out.println("-----------masterStateful------- " + masterStateful);
			String[] authencate = new String[2];

			//authencate[0] = "";
			authencate[0] = userId; //Changed By PriynkaC on 15/03/2018
			authencate[1] = "";
			System.out.println("09-mar-2020 xmlString to masterstateful11 [" + xmlString + "]");


			userInfoBean.setEmpCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			userInfoBean.setRemoteHost(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			userInfoBean.setSiteCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			userInfoBean.setLoginCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
			userInfoBean.setEntityCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));

			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);

		} catch (ITMException itme)
		{
			System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}
	private String CheckTaxMisMatch(String tranId) throws Exception
	{
		String errCode = "";
		try
		{
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception : SalesReturnConfirm.java : CheckTaxMisMatch:==>" + e.getMessage());
			throw new ITMException(e);
		}
			
		return errCode;
	}
	

}