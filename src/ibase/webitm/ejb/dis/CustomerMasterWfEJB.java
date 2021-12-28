package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import ibase.system.config.AppConnectParm;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import org.w3c.dom.Document;


@Stateless
public class CustomerMasterWfEJB extends ValidatorEJB implements CustomerMasterWfEJBLocal, CustomerMasterWfEJBRemote {
	public String insertCustomerMaster(String objName,String tranId,String transInfoXml,String docId,String xtraParams,String entityCode) throws RemoteException, ITMException {
		Connection conn      = null;
		String retString     = "";
        String siteCode      = "";
        E12GenericUtility genericUtility = null;
        Document xmlDataAll  = null;
  	    MasterStatefulLocal masterStatefulLocal = null;  
        System.out.println("objName: "+tranId+" tranId: "+tranId+" docId: "+docId+" xtraParams: "+xtraParams+" entityCode: "+entityCode+" transInfoXml: "+transInfoXml);
        String returnString = "";
	try {
		conn = getConnection();
		conn.setAutoCommit(false);
		AppConnectParm appConnect = new AppConnectParm();
		Properties p = appConnect.getProperty();
		InitialContext ctx = new InitialContext(p);
		genericUtility = new E12GenericUtility();
		String userId = "";//Added By Pavan R 27/DEC/17
		//added by Pavan R on 27/DEC/17 userId passwed to savData() and processRequest()
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		System.out.println("userId::["+userId+"]");
		String [] authencate = new String[2];
        authencate[0]        = userId;
        authencate[1]        = "";
		SimpleDateFormat dt = new SimpleDateFormat(genericUtility.getApplDateFormat());
		if (transInfoXml != null && transInfoXml.trim().length() != 0) {
			xmlDataAll = genericUtility.parseString(transInfoXml);
		}
		
		StringBuffer xmlString= new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xmlString.append("<DocumentRoot><description>Datawindow Root</description><group0><description>Group0 escription</description>");
		xmlString.append("<Header0>");
		xmlString.append("<description>Header0 members</description>");
		xmlString.append("<objName><![CDATA[").append("customer_wf_new").append("]]></objName>");
		xmlString.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
		xmlString.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
		xmlString.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
		xmlString.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
		xmlString.append("<action><![CDATA[").append("SAVE").append("]]></action>");
		xmlString.append("<elementName><![CDATA[").append("").append("]]></elementName>");
		xmlString.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
		xmlString.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
		xmlString.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
		xmlString.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
		xmlString.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
		xmlString.append("<Detail1 dbID='' domID=\"1\" objName=\"customer_wf_new\" objContext=\"1\">");
		xmlString.append("<attribute pkNames=\"cust_code:\" status=\"N\" updateFlag=\"A\" selected=\"N\" />");
		
		if(xmlDataAll.getElementsByTagName("Detail1")!=null && xmlDataAll.getElementsByTagName("Detail1").getLength()>0)
		{
				String custCode        = checkNull(genericUtility.getColumnValueFromNode("cust_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contactCode     = checkNull(genericUtility.getColumnValueFromNode("contact_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String custName        = checkNull(genericUtility.getColumnValueFromNode("cust_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String shName          = checkNull(genericUtility.getColumnValueFromNode("sh_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String groupCode       = checkNull(genericUtility.getColumnValueFromNode("group_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String custType        = checkNull(genericUtility.getColumnValueFromNode("cust_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String custCodeBil     = checkNull(genericUtility.getColumnValueFromNode("cust_code__bil", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ignoreCredit    = checkNull(genericUtility.getColumnValueFromNode("ignore_credit", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String addr1           = checkNull(genericUtility.getColumnValueFromNode("addr1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String addr2           = checkNull(genericUtility.getColumnValueFromNode("addr2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String addr3           = checkNull(genericUtility.getColumnValueFromNode("addr3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String city            = checkNull(genericUtility.getColumnValueFromNode("city", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ceo_pfx         = checkNull(genericUtility.getColumnValueFromNode("ceo_pfx", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ceo             = checkNull(genericUtility.getColumnValueFromNode("ceo", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String stan_code       = checkNull(genericUtility.getColumnValueFromNode("stan_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String state_code      = checkNull(genericUtility.getColumnValueFromNode("state_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pin             = checkNull(genericUtility.getColumnValueFromNode("pin", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String count_code      = checkNull(genericUtility.getColumnValueFromNode("count_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tele1           = checkNull(genericUtility.getColumnValueFromNode("tele1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tele2           = checkNull(genericUtility.getColumnValueFromNode("tele2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tele3           = checkNull(genericUtility.getColumnValueFromNode("tele3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String teleExt         = checkNull(genericUtility.getColumnValueFromNode("tele_ext", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String fax             = checkNull(genericUtility.getColumnValueFromNode("fax", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String modem           = checkNull(genericUtility.getColumnValueFromNode("modem", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String edi_addr        = checkNull(genericUtility.getColumnValueFromNode("edi_addr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String emailAddr       = checkNull(genericUtility.getColumnValueFromNode("email_addr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPfx         = checkNull(genericUtility.getColumnValueFromNode("cont_pfx", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPers        = checkNull(genericUtility.getColumnValueFromNode("cont_pers", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contDept        = checkNull(genericUtility.getColumnValueFromNode("cont_dept", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cont_pers_tele1 = checkNull(genericUtility.getColumnValueFromNode("cont_pers_tele1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cont_pers_tele2 = checkNull(genericUtility.getColumnValueFromNode("cont_pers_tele2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cont_pers_fax   = checkNull(genericUtility.getColumnValueFromNode("cont_pers_fax", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String bankCode        = checkNull(genericUtility.getColumnValueFromNode("bank_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String bankName        = checkNull(genericUtility.getColumnValueFromNode("bank_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String business        = checkNull(genericUtility.getColumnValueFromNode("business", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String creditPrd       = checkNull(genericUtility.getColumnValueFromNode("credit_prd", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String creditLmt       = checkNull(genericUtility.getColumnValueFromNode("credit_lmt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String crTerm          = checkNull(genericUtility.getColumnValueFromNode("cr_term", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String currCode        = checkNull(genericUtility.getColumnValueFromNode("curr_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chqName         = checkNull(genericUtility.getColumnValueFromNode("chq_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String acctCodeAr      = checkNull(genericUtility.getColumnValueFromNode("acct_code__ar", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cctrCodeAr      = checkNull(genericUtility.getColumnValueFromNode("cctr_code__ar", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String priceList       = checkNull(genericUtility.getColumnValueFromNode("price_list", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String taxClass        = checkNull(genericUtility.getColumnValueFromNode("tax_class", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String taxChap         = checkNull(genericUtility.getColumnValueFromNode("tax_chap", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String eccNo           = checkNull(genericUtility.getColumnValueFromNode("ecc_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cstNo           = checkNull(genericUtility.getColumnValueFromNode("cst_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String lstNo           = checkNull(genericUtility.getColumnValueFromNode("lst_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String taxReg1         = checkNull(genericUtility.getColumnValueFromNode("tax_reg_1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String taxReg2         = checkNull(genericUtility.getColumnValueFromNode("tax_reg_2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String taxReg3         = checkNull(genericUtility.getColumnValueFromNode("tax_reg_3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String drugLicNo       = checkNull(genericUtility.getColumnValueFromNode("drug_lic_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String drugLicNo1      = checkNull(genericUtility.getColumnValueFromNode("drug_lic_no_1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String drugLicNo2      = checkNull(genericUtility.getColumnValueFromNode("drug_lic_no_2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String siteCodeRcp     = checkNull(genericUtility.getColumnValueFromNode("site_code__rcp", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String salesPers       = checkNull(genericUtility.getColumnValueFromNode("sales_pers", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tranCode        = checkNull(genericUtility.getColumnValueFromNode("tran_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String transMode       = checkNull(genericUtility.getColumnValueFromNode("trans_mode", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rcpMode         = checkNull(genericUtility.getColumnValueFromNode("rcp_mode", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPfxAlt      = checkNull(genericUtility.getColumnValueFromNode("cont_pfx_alt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPersAlt     = checkNull(genericUtility.getColumnValueFromNode("cont_pers_alt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contDeptAlt     = checkNull(genericUtility.getColumnValueFromNode("cont_dept_alt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPersAltTele1= checkNull(genericUtility.getColumnValueFromNode("cont_pers_alt_tele1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPersAltTele2= checkNull(genericUtility.getColumnValueFromNode("cont_pers_alt_tele2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String contPersAltFax  = checkNull(genericUtility.getColumnValueFromNode("cont_pers_alt_fax", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String teleExtAlt      = checkNull(genericUtility.getColumnValueFromNode("tele_ext_alt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String workAddr1       = checkNull(genericUtility.getColumnValueFromNode("work_addr1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String workAddr2       = checkNull(genericUtility.getColumnValueFromNode("work_addr2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String work_addr3      = checkNull(genericUtility.getColumnValueFromNode("work_addr3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String work_city       = checkNull(genericUtility.getColumnValueFromNode("work_city", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String work_pin        = checkNull(genericUtility.getColumnValueFromNode("work_pin", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String work_tele1      = checkNull(genericUtility.getColumnValueFromNode("work_tele1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String work_tele2      = checkNull(genericUtility.getColumnValueFromNode("work_tele2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String work_fax        = checkNull(genericUtility.getColumnValueFromNode("work_fax", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String channel_partner = checkNull(genericUtility.getColumnValueFromNode("channel_partner", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String site_code       = checkNull(genericUtility.getColumnValueFromNode("site_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String dis_link        = checkNull(genericUtility.getColumnValueFromNode("dis_link", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String fin_link        = checkNull(genericUtility.getColumnValueFromNode("fin_link", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pending_order   = checkNull(genericUtility.getColumnValueFromNode("pending_order", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String black_listed    = checkNull(genericUtility.getColumnValueFromNode("black_listed", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String black_listed_date= checkNull(genericUtility.getColumnValueFromNode("black_listed_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chg_date        = checkNull(genericUtility.getColumnValueFromNode("chg_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chg_user        = checkNull(genericUtility.getColumnValueFromNode("chg_user", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chg_term        = checkNull(genericUtility.getColumnValueFromNode("chg_term", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String stan_descr      = checkNull(genericUtility.getColumnValueFromNode("stan_descr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String sp_name      = checkNull(genericUtility.getColumnValueFromNode("sp_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				/*String add_date       = checkNull(genericUtility.getColumnValueFromNode("add_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String add_user        = checkNull(genericUtility.getColumnValueFromNode("add_user", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String add_term      = checkNull(genericUtility.getColumnValueFromNode("add_term", xmlDataAll.getElementsByTagName("Detail1").item(0)));*/
				String ignore_days      = checkNull(genericUtility.getColumnValueFromNode("ignore_days", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String round        = checkNull(genericUtility.getColumnValueFromNode("round", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String round_to = checkNull(genericUtility.getColumnValueFromNode("round_to", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String bank_addr1       = checkNull(genericUtility.getColumnValueFromNode("bank_addr1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String bank_addr2        = checkNull(genericUtility.getColumnValueFromNode("bank_addr2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String sales_pers__1        = checkNull(genericUtility.getColumnValueFromNode("sales_pers__1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String sales_pers__2   = checkNull(genericUtility.getColumnValueFromNode("sales_pers__2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String excise_ref    = checkNull(genericUtility.getColumnValueFromNode("excise_ref", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String price_list__disc= checkNull(genericUtility.getColumnValueFromNode("price_list__disc", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String disc_perc        = checkNull(genericUtility.getColumnValueFromNode("disc_perc", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String located_at        = checkNull(genericUtility.getColumnValueFromNode("located_at", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String stop_business        = checkNull(genericUtility.getColumnValueFromNode("stop_business", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String market_reg      = checkNull(genericUtility.getColumnValueFromNode("market_reg", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String sales_option      = checkNull(genericUtility.getColumnValueFromNode("sales_option", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String dlv_term       = checkNull(genericUtility.getColumnValueFromNode("dlv_term", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String hold_shipment        = checkNull(genericUtility.getColumnValueFromNode("hold_shipment", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String repl_factor      = checkNull(genericUtility.getColumnValueFromNode("repl_factor", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String repl_opt      = checkNull(genericUtility.getColumnValueFromNode("repl_opt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String comm_perc__base        = checkNull(genericUtility.getColumnValueFromNode("comm_perc__base", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String acct_code__adv = checkNull(genericUtility.getColumnValueFromNode("acct_code__adv", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cctr_code__adv       = checkNull(genericUtility.getColumnValueFromNode("cctr_code__adv", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String loc_group        = checkNull(genericUtility.getColumnValueFromNode("loc_group", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String business_start_dt        = checkNull(genericUtility.getColumnValueFromNode("business_start_dt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String customer_ref   = checkNull(genericUtility.getColumnValueFromNode("customer_ref", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cust_anal    = checkNull(genericUtility.getColumnValueFromNode("cust_anal", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String site_code__pbus= checkNull(genericUtility.getColumnValueFromNode("site_code__pbus", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ser_tax_reg        = checkNull(genericUtility.getColumnValueFromNode("ser_tax_reg", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String order_type        = checkNull(genericUtility.getColumnValueFromNode("order_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String member_code        = checkNull(genericUtility.getColumnValueFromNode("member_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String price_list__clg      = checkNull(genericUtility.getColumnValueFromNode("price_list__clg", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String split_factor      = checkNull(genericUtility.getColumnValueFromNode("split_factor", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String repl_dlv_sch       = checkNull(genericUtility.getColumnValueFromNode("repl_dlv_sch", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String terr_code        = checkNull(genericUtility.getColumnValueFromNode("terr_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String territory_descr      = checkNull(genericUtility.getColumnValueFromNode("territory_descr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String drug_licno_upto      = checkNull(genericUtility.getColumnValueFromNode("drug_licno_upto", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String drug_licno1_upto        = checkNull(genericUtility.getColumnValueFromNode("drug_licno1_upto", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String drug_licno2_upto = checkNull(genericUtility.getColumnValueFromNode("drug_licno2_upto", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String agreement_no       = checkNull(genericUtility.getColumnValueFromNode("agreement_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String agreement_date        = checkNull(genericUtility.getColumnValueFromNode("agreement_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String frt_term        = checkNull(genericUtility.getColumnValueFromNode("frt_term", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String locality_code   = checkNull(genericUtility.getColumnValueFromNode("locality_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String min_inv_amt    = checkNull(genericUtility.getColumnValueFromNode("min_inv_amt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String locality_descr= checkNull(genericUtility.getColumnValueFromNode("locality_descr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String curr_code__frt        = checkNull(genericUtility.getColumnValueFromNode("curr_code__frt", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String curr_code__ins        = checkNull(genericUtility.getColumnValueFromNode("curr_code__ins", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String comm_table__1        = checkNull(genericUtility.getColumnValueFromNode("comm_table__1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String comm_table__2      = checkNull(genericUtility.getColumnValueFromNode("comm_table__2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String comm_table__3      = checkNull(genericUtility.getColumnValueFromNode("comm_table__3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String excise_ref1       = checkNull(genericUtility.getColumnValueFromNode("excise_ref1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String excise_ref2        = checkNull(genericUtility.getColumnValueFromNode("excise_ref2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String excise_ref3      = checkNull(genericUtility.getColumnValueFromNode("excise_ref3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cr_term__np      = checkNull(genericUtility.getColumnValueFromNode("cr_term__np", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String loss_perc        = checkNull(genericUtility.getColumnValueFromNode("loss_perc", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String reas_code__bklist = checkNull(genericUtility.getColumnValueFromNode("reas_code__bklist", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String adhoc_repl       = checkNull(genericUtility.getColumnValueFromNode("adhoc_repl", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String adhoc_repl_perc        = checkNull(genericUtility.getColumnValueFromNode("adhoc_repl_perc", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String udf_num1        = checkNull(genericUtility.getColumnValueFromNode("udf_num1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String addl_lic_no   = checkNull(genericUtility.getColumnValueFromNode("addl_lic_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String addl_lic_upto    = checkNull(genericUtility.getColumnValueFromNode("addl_lic_upto", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String full_name= checkNull(genericUtility.getColumnValueFromNode("full_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cust_category        = checkNull(genericUtility.getColumnValueFromNode("cust_category", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String fmt_code__shipper        = checkNull(genericUtility.getColumnValueFromNode("fmt_code__shipper", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String fmt_code__inner        = checkNull(genericUtility.getColumnValueFromNode("fmt_code__inner", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String fmt_code__pallet      = checkNull(genericUtility.getColumnValueFromNode("fmt_code__pallet", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String term_table__no      = checkNull(genericUtility.getColumnValueFromNode("term_table__no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String lst_no_date       = checkNull(genericUtility.getColumnValueFromNode("lst_no_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cst_no_date        = checkNull(genericUtility.getColumnValueFromNode("cst_no_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ecc_no_date      = checkNull(genericUtility.getColumnValueFromNode("ecc_no_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tax_reg_1_date      = checkNull(genericUtility.getColumnValueFromNode("tax_reg_1_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tax_reg_2_date        = checkNull(genericUtility.getColumnValueFromNode("tax_reg_2_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tax_reg_3_date = checkNull(genericUtility.getColumnValueFromNode("tax_reg_3_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String tax_reg_st_date       = checkNull(genericUtility.getColumnValueFromNode("tax_reg_st_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ser_tax_reg_date        = checkNull(genericUtility.getColumnValueFromNode("ser_tax_reg_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String udf_str1        = checkNull(genericUtility.getColumnValueFromNode("udf_str1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String auto_debit__note   = checkNull(genericUtility.getColumnValueFromNode("auto_debit__note", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String shipment_lead__time    = checkNull(genericUtility.getColumnValueFromNode("shipment_lead__time", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate_round_to= checkNull(genericUtility.getColumnValueFromNode("rate_round_to", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate_round        = checkNull(genericUtility.getColumnValueFromNode("rate_round", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String master_pack        = checkNull(genericUtility.getColumnValueFromNode("master_pack", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pd_group        = checkNull(genericUtility.getColumnValueFromNode("pd_group", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cancel_bo      = checkNull(genericUtility.getColumnValueFromNode("cancel_bo", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String cust_code__pd      = checkNull(genericUtility.getColumnValueFromNode("cust_code__pd", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cust_code__disc       = checkNull(genericUtility.getColumnValueFromNode("cust_code__disc", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String min_shelf_life        = checkNull(genericUtility.getColumnValueFromNode("min_shelf_life", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String single_lot      = checkNull(genericUtility.getColumnValueFromNode("single_lot", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String sgroup_code      = checkNull(genericUtility.getColumnValueFromNode("sgroup_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String old_cust_ref        = checkNull(genericUtility.getColumnValueFromNode("old_cust_ref", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cust_code__ar = checkNull(genericUtility.getColumnValueFromNode("cust_code__ar", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String old_corp_parent       = checkNull(genericUtility.getColumnValueFromNode("old_corp_parent", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String store_number        = checkNull(genericUtility.getColumnValueFromNode("store_number", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String cust_tran_type        = checkNull(genericUtility.getColumnValueFromNode("cust_tran_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ita_reg_no   = checkNull(genericUtility.getColumnValueFromNode("ita_reg_no", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String eng_name    = checkNull(genericUtility.getColumnValueFromNode("eng_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String part_qty= checkNull(genericUtility.getColumnValueFromNode("part_qty", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String asn_reqd        = checkNull(genericUtility.getColumnValueFromNode("asn_reqd", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String wave_type        = checkNull(genericUtility.getColumnValueFromNode("wave_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String customer_cust_name        = checkNull(genericUtility.getColumnValueFromNode("customer_cust_name", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String customer_cust_name_1      = checkNull(genericUtility.getColumnValueFromNode("customer_cust_name_1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String return_allowed      = checkNull(genericUtility.getColumnValueFromNode("return_allowed", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String site_code__pay       = checkNull(genericUtility.getColumnValueFromNode("site_code__pay", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String ship_cons_grp        = checkNull(genericUtility.getColumnValueFromNode("ship_cons_grp", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String bklist_reason      = checkNull(genericUtility.getColumnValueFromNode("bklist_reason", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String edi_reqd      = checkNull(genericUtility.getColumnValueFromNode("edi_reqd", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String registr_1        = checkNull(genericUtility.getColumnValueFromNode("registr_1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String registr_2 = checkNull(genericUtility.getColumnValueFromNode("registr_2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String registr_3       = checkNull(genericUtility.getColumnValueFromNode("registr_3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String registr_4        = checkNull(genericUtility.getColumnValueFromNode("registr_4", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String registr_5        = checkNull(genericUtility.getColumnValueFromNode("registr_5", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String available_yn   = checkNull(genericUtility.getColumnValueFromNode("available_yn", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String emp_code__ord1    = checkNull(genericUtility.getColumnValueFromNode("emp_code__ord1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String emp_code__ord= checkNull(genericUtility.getColumnValueFromNode("emp_code__ord", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String emp_fname        = checkNull(genericUtility.getColumnValueFromNode("emp_fname", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String emp_lname        = checkNull(genericUtility.getColumnValueFromNode("emp_lname", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String employee_emp_fname        = checkNull(genericUtility.getColumnValueFromNode("employee_emp_fname", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String employee_emp_lname      = checkNull(genericUtility.getColumnValueFromNode("employee_emp_lname", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				String tnt_option      = checkNull(genericUtility.getColumnValueFromNode("tnt_option", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String auto_stk_alloc       = checkNull(genericUtility.getColumnValueFromNode("auto_stk_alloc", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String uniform_code        = checkNull(genericUtility.getColumnValueFromNode("uniform_code", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String sms_notify      = checkNull(genericUtility.getColumnValueFromNode("sms_notify", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String email_notify      = checkNull(genericUtility.getColumnValueFromNode("email_notify", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String sundry_balance        = checkNull(genericUtility.getColumnValueFromNode("sundry_balance", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String disc_list = checkNull(genericUtility.getColumnValueFromNode("disc_list", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String descr       = checkNull(genericUtility.getColumnValueFromNode("descr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				xmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
				xmlString.append("<contact_code>").append("<![CDATA["+contactCode+"]]>").append("</contact_code>");
				xmlString.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>");
				xmlString.append("<sh_name><![CDATA[").append(shName).append("]]></sh_name>");
				xmlString.append("<group_code><![CDATA[").append(groupCode).append("]]></group_code>");
				xmlString.append("<cust_type><![CDATA[").append(custType).append("]]></cust_type>");
				xmlString.append("<cust_code__bil><![CDATA[").append(custCodeBil).append("]]></cust_code__bil>");
				xmlString.append("<ignore_credit><![CDATA[").append(ignoreCredit).append("]]></ignore_credit>");
				xmlString.append("<addr1><![CDATA[").append(addr1).append("]]></addr1>");
				xmlString.append("<addr2><![CDATA[").append(addr2).append("]]></addr2>");
				xmlString.append("<addr3><![CDATA[").append(addr3).append("]]></addr3>");
				xmlString.append("<city><![CDATA[").append(city).append("]]></city>");
				xmlString.append("<ceo_pfx><![CDATA[").append(ceo_pfx).append("]]></ceo_pfx>");
				xmlString.append("<ceo><![CDATA[").append(ceo).append("]]></ceo>");
				xmlString.append("<stan_code><![CDATA[").append(stan_code).append("]]></stan_code>");
				xmlString.append("<state_code><![CDATA[").append(state_code).append("]]></state_code>");
				xmlString.append("<pin><![CDATA[").append(pin).append("]]></pin>");
				xmlString.append("<count_code><![CDATA[").append(count_code).append("]]></count_code>");
				xmlString.append("<tele1><![CDATA[").append(tele1).append("]]></tele1>");
				xmlString.append("<tele2><![CDATA[").append(tele2).append("]]></tele2>");
				xmlString.append("<tele3><![CDATA[").append(tele3).append("]]></tele3>");
				xmlString.append("<tele_ext><![CDATA[").append(teleExt).append("]]></tele_ext>");
				xmlString.append("<fax><![CDATA[").append(fax).append("]]></fax>");
				xmlString.append("<modem><![CDATA[").append(modem).append("]]></modem>");
				xmlString.append("<edi_addr><![CDATA[").append(edi_addr).append("]]></edi_addr>");
				xmlString.append("<email_addr><![CDATA[").append(emailAddr).append("]]></email_addr>");
				xmlString.append("<cont_pfx><![CDATA[").append(contPfx).append("]]></cont_pfx>");
				xmlString.append("<cont_pers><![CDATA[").append(contPers).append("]]></cont_pers>");
				xmlString.append("<cont_dept><![CDATA[").append(contDept).append("]]></cont_dept>");
				xmlString.append("<cont_pers_tele1><![CDATA[").append(cont_pers_tele1).append("]]></cont_pers_tele1>");
				xmlString.append("<cont_pers_tele2><![CDATA[").append(cont_pers_tele2).append("]]></cont_pers_tele2>");
				xmlString.append("<cont_pers_fax><![CDATA[").append(cont_pers_fax).append("]]></cont_pers_fax>");
				xmlString.append("<bank_code><![CDATA[").append(bankCode).append("]]></bank_code>");
				xmlString.append("<bank_name><![CDATA[").append(bankName).append("]]></bank_name>");
				xmlString.append("<business><![CDATA[").append(business).append("]]></business>");
				xmlString.append("<credit_prd><![CDATA[").append(creditPrd).append("]]></credit_prd>");
				xmlString.append("<credit_lmt><![CDATA[").append(creditLmt).append("]]></credit_lmt>");
				xmlString.append("<cr_term><![CDATA[").append(crTerm).append("]]></cr_term>");
				xmlString.append("<curr_code><![CDATA[").append(currCode).append("]]></curr_code>");
				xmlString.append("<chq_name><![CDATA[").append(chqName).append("]]></chq_name>");
				xmlString.append("<acct_code__ar><![CDATA[").append(acctCodeAr).append("]]></acct_code__ar>");
				xmlString.append("<cctr_code__ar><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__ar>");
				xmlString.append("<price_list><![CDATA[").append(priceList).append("]]></price_list>");
				xmlString.append("<tax_class><![CDATA[").append(taxClass).append("]]></tax_class>");
				xmlString.append("<tax_chap><![CDATA[").append(taxChap).append("]]></tax_chap>");
				xmlString.append("<ecc_no><![CDATA[").append(eccNo).append("]]></ecc_no>");
				xmlString.append("<cst_no><![CDATA[").append(cstNo).append("]]></cst_no>");
				xmlString.append("<lst_no><![CDATA[").append(lstNo).append("]]></lst_no>");
				xmlString.append("<tax_reg_1><![CDATA[").append(taxReg1).append("]]></tax_reg_1>");
				xmlString.append("<tax_reg_2><![CDATA[").append(taxReg2).append("]]></tax_reg_2>");
				xmlString.append("<tax_reg_3><![CDATA[").append(taxReg3).append("]]></tax_reg_3>");
				xmlString.append("<drug_lic_no><![CDATA[").append(drugLicNo).append("]]></drug_lic_no>");
				xmlString.append("<drug_lic_no_1><![CDATA[").append(drugLicNo1).append("]]></drug_lic_no_1>");
				xmlString.append("<drug_lic_no_2><![CDATA[").append(drugLicNo2).append("]]></drug_lic_no_2>");
				xmlString.append("<site_code__rcp><![CDATA[").append(siteCodeRcp).append("]]></site_code__rcp>");
				xmlString.append("<sales_pers><![CDATA[").append(salesPers).append("]]></sales_pers>");
				xmlString.append("<tran_code><![CDATA[").append(tranCode).append("]]></tran_code>");
				xmlString.append("<trans_mode><![CDATA[").append(transMode).append("]]></trans_mode>");
				xmlString.append("<rcp_mode><![CDATA[").append(rcpMode).append("]]></rcp_mode>");
				xmlString.append("<cont_pfx_alt><![CDATA[").append(contPfxAlt).append("]]></cont_pfx_alt>");
				xmlString.append("<cont_pers_alt><![CDATA[").append(contPersAlt).append("]]></cont_pers_alt>");
				xmlString.append("<cont_dept_alt><![CDATA[").append(contDeptAlt).append("]]></cont_dept_alt>");
				xmlString.append("<cont_pers_alt_tele1><![CDATA[").append(contPersAltTele1).append("]]></cont_pers_alt_tele1>");
				xmlString.append("<cont_pers_alt_tele2><![CDATA[").append(contPersAltTele2).append("]]></cont_pers_alt_tele2>");
				xmlString.append("<cont_pers_alt_fax><![CDATA[").append(contPersAltFax).append("]]></cont_pers_alt_fax>");
				xmlString.append("<tele_ext_alt><![CDATA[").append(teleExtAlt).append("]]></tele_ext_alt>");
				xmlString.append("<work_addr1><![CDATA[").append(workAddr1).append("]]></work_addr1>");
				xmlString.append("<work_addr2><![CDATA[").append(workAddr2).append("]]></work_addr2>");
				xmlString.append("<work_addr3><![CDATA[").append(work_addr3).append("]]></work_addr3>");
				xmlString.append("<work_city><![CDATA[").append(work_city).append("]]></work_city>");
				xmlString.append("<work_pin><![CDATA[").append(work_pin).append("]]></work_pin>");
				xmlString.append("<work_tele1><![CDATA[").append(work_tele1).append("]]></work_tele1>");
				xmlString.append("<work_tele2><![CDATA[").append(work_tele2).append("]]></work_tele2>");
				xmlString.append("<work_fax><![CDATA[").append(work_fax).append("]]></work_fax>");
				xmlString.append("<channel_partner><![CDATA[").append(channel_partner).append("]]></channel_partner>");
				xmlString.append("<site_code><![CDATA[").append(site_code).append("]]></site_code>");
				xmlString.append("<dis_link><![CDATA[").append(dis_link).append("]]></dis_link>");
				xmlString.append("<fin_link><![CDATA[").append(fin_link).append("]]></fin_link>");
				xmlString.append("<pending_order><![CDATA[").append(pending_order).append("]]></pending_order>");
				xmlString.append("<black_listed><![CDATA[").append(black_listed).append("]]></black_listed>");
				xmlString.append("<black_listed_date><![CDATA[").append(black_listed_date).append("]]></black_listed_date>");
				xmlString.append("<chg_date><![CDATA[").append(chg_date).append("]]></chg_date>");
				xmlString.append("<chg_user><![CDATA[").append(chg_user).append("]]></chg_user>");
				xmlString.append("<chg_term><![CDATA[").append(chg_term).append("]]></chg_term>");
				xmlString.append("<stan_descr><![CDATA[").append(stan_descr).append("]]></stan_descr>");
				xmlString.append("<sp_name><![CDATA[").append(sp_name).append("]]></sp_name>");
				xmlString.append("<add_date><![CDATA[").append(dt.format(new java.util.Date())).append("]]></add_date>");
				xmlString.append("<add_user><![CDATA[").append(chg_user).append("]]></add_user>");
				xmlString.append("<add_term><![CDATA[").append(chg_term).append("]]></add_term>");
				xmlString.append("<ignore_days><![CDATA[").append(ignore_days).append("]]></ignore_days>");
				xmlString.append("<round><![CDATA[").append(round).append("]]></round>");
				xmlString.append("<round_to><![CDATA[").append(round_to).append("]]></round_to>");
				xmlString.append("<bank_addr1><![CDATA[").append(bank_addr1).append("]]></bank_addr1>");
				xmlString.append("<bank_addr2><![CDATA[").append(bank_addr2).append("]]></bank_addr2>");
				xmlString.append("<sales_pers__1><![CDATA[").append(sales_pers__1).append("]]></sales_pers__1>");
				xmlString.append("<sales_pers__2><![CDATA[").append(sales_pers__2).append("]]></sales_pers__2>");
				xmlString.append("<excise_ref><![CDATA[").append(excise_ref).append("]]></excise_ref>");
				xmlString.append("<price_list__disc><![CDATA[").append(price_list__disc).append("]]></price_list__disc>");
				xmlString.append("<disc_perc><![CDATA[").append(disc_perc).append("]]></disc_perc>");
				xmlString.append("<located_at><![CDATA[").append(located_at).append("]]></located_at>");
				xmlString.append("<stop_business><![CDATA[").append(stop_business).append("]]></stop_business>");
				xmlString.append("<market_reg><![CDATA[").append(market_reg).append("]]></market_reg>");
				xmlString.append("<sales_option><![CDATA[").append(sales_option).append("]]></sales_option>");
				xmlString.append("<dlv_term><![CDATA[").append(dlv_term).append("]]></dlv_term>");
				xmlString.append("<hold_shipment><![CDATA[").append(hold_shipment).append("]]></hold_shipment>");
				xmlString.append("<repl_factor><![CDATA[").append(repl_factor).append("]]></repl_factor>");
				xmlString.append("<repl_opt><![CDATA[").append(repl_opt).append("]]></repl_opt>");
				xmlString.append("<comm_perc__base><![CDATA[").append(comm_perc__base).append("]]></comm_perc__base>");
				xmlString.append("<acct_code__adv><![CDATA[").append(acct_code__adv).append("]]></acct_code__adv>");
				xmlString.append("<cctr_code__adv><![CDATA[").append(cctr_code__adv).append("]]></cctr_code__adv>");
				xmlString.append("<loc_group><![CDATA[").append(loc_group).append("]]></loc_group>");
				xmlString.append("<business_start_dt><![CDATA[").append(business_start_dt).append("]]></business_start_dt>");
				xmlString.append("<customer_ref><![CDATA[").append(customer_ref).append("]]></customer_ref>");
				xmlString.append("<cust_anal><![CDATA[").append(cust_anal).append("]]></cust_anal>");
				xmlString.append("<site_code__pbus><![CDATA[").append(site_code__pbus).append("]]></site_code__pbus>");
				xmlString.append("<ser_tax_reg><![CDATA[").append(ser_tax_reg).append("]]></ser_tax_reg>");
				xmlString.append("<order_type><![CDATA[").append(order_type).append("]]></order_type>");
				xmlString.append("<member_code><![CDATA[").append(member_code).append("]]></member_code>");
				xmlString.append("<price_list__clg><![CDATA[").append(price_list__clg).append("]]></price_list__clg>");
				xmlString.append("<split_factor><![CDATA[").append(split_factor).append("]]></split_factor>");
				xmlString.append("<repl_dlv_sch><![CDATA[").append(repl_dlv_sch).append("]]></repl_dlv_sch>");
				xmlString.append("<terr_code><![CDATA[").append(terr_code).append("]]></terr_code>");
				xmlString.append("<territory_descr><![CDATA[").append(territory_descr).append("]]></territory_descr>");
				xmlString.append("<drug_licno_upto><![CDATA[").append(drug_licno_upto).append("]]></drug_licno_upto>");
				  xmlString.append("<drug_licno1_upto><![CDATA[").append(drug_licno1_upto).append("]]></drug_licno1_upto>");
				  xmlString.append("<drug_licno2_upto><![CDATA[").append(drug_licno2_upto).append("]]></drug_licno2_upto>");
				  xmlString.append("<agreement_no><![CDATA[").append(agreement_no).append("]]></agreement_no>");
				  xmlString.append("<agreement_date><![CDATA[").append(agreement_date).append("]]></agreement_date>");
				  xmlString.append("<frt_term><![CDATA[").append(frt_term).append("]]></frt_term>");
				  xmlString.append("<locality_code><![CDATA[").append(locality_code).append("]]></locality_code>");
				   xmlString.append("<min_inv_amt><![CDATA[").append(min_inv_amt).append("]]></min_inv_amt>");
				   xmlString.append("<locality_descr><![CDATA[").append(locality_descr).append("]]></locality_descr>");
				   xmlString.append("<curr_code__frt><![CDATA[").append(curr_code__frt).append("]]></curr_code__frt>");
				   xmlString.append("<curr_code__ins><![CDATA[").append(curr_code__ins).append("]]></curr_code__ins>");
				   xmlString.append("<comm_table__1><![CDATA[").append(comm_table__1).append("]]></comm_table__1>");
				   xmlString.append("<comm_table__2><![CDATA[").append(comm_table__2).append("]]></comm_table__2>");
				   xmlString.append("<comm_table__3><![CDATA[").append(comm_table__3).append("]]></comm_table__3>");
				   xmlString.append("<excise_ref1><![CDATA[").append(excise_ref1).append("]]></excise_ref1>");
				   xmlString.append("<excise_ref2><![CDATA[").append(excise_ref2).append("]]></excise_ref2>");
				   xmlString.append("<excise_ref3><![CDATA[").append(excise_ref3).append("]]></excise_ref3>");
				   xmlString.append("<cr_term__np><![CDATA[").append(cr_term__np).append("]]></cr_term__np>");
				   xmlString.append("<loss_perc><![CDATA[").append(loss_perc).append("]]></loss_perc>");
				   xmlString.append("<reas_code__bklist><![CDATA[").append(reas_code__bklist).append("]]></reas_code__bklist>");
				   xmlString.append("<adhoc_repl><![CDATA[").append(adhoc_repl).append("]]></adhoc_repl>");
				   xmlString.append("<adhoc_repl_perc><![CDATA[").append(adhoc_repl_perc).append("]]></adhoc_repl_perc>");
				   xmlString.append("<udf_num1><![CDATA[").append(udf_num1).append("]]></udf_num1>");
				   xmlString.append("<addl_lic_no><![CDATA[").append(addl_lic_no).append("]]></addl_lic_no>");
				   xmlString.append("<addl_lic_upto><![CDATA[").append(addl_lic_upto).append("]]></addl_lic_upto>");
				   xmlString.append("<full_name><![CDATA[").append(full_name).append("]]></full_name>");
				   xmlString.append("<cust_category><![CDATA[").append(cust_category).append("]]></cust_category>");
				   xmlString.append("<fmt_code__shipper><![CDATA[").append(fmt_code__shipper).append("]]></fmt_code__shipper>");
				   xmlString.append("<fmt_code__inner><![CDATA[").append(fmt_code__inner).append("]]></fmt_code__inner>");
				   xmlString.append("<fmt_code__pallet><![CDATA[").append(fmt_code__pallet).append("]]></fmt_code__pallet>");
				   xmlString.append("<term_table__no><![CDATA[").append(term_table__no).append("]]></term_table__no>");
				   xmlString.append("<lst_no_date><![CDATA[").append(lst_no_date).append("]]></lst_no_date>");
				   xmlString.append("<cst_no_date><![CDATA[").append(cst_no_date).append("]]></cst_no_date>");
				   xmlString.append("<ecc_no_date><![CDATA[").append(ecc_no_date).append("]]></ecc_no_date>");
				   xmlString.append("<tax_reg_1_date><![CDATA[").append(tax_reg_1_date).append("]]></tax_reg_1_date>");
				   xmlString.append("<tax_reg_2_date><![CDATA[").append(tax_reg_2_date).append("]]></tax_reg_2_date>");
				   xmlString.append("<tax_reg_3_date><![CDATA[").append(tax_reg_3_date).append("]]></tax_reg_3_date>");
				   xmlString.append("<tax_reg_st_date><![CDATA[").append(tax_reg_st_date).append("]]></tax_reg_st_date>");
				   xmlString.append("<ser_tax_reg_date><![CDATA[").append(ser_tax_reg_date).append("]]></ser_tax_reg_date>");
				   xmlString.append("<udf_str1><![CDATA[").append(udf_str1).append("]]></udf_str1>");
				   xmlString.append("<auto_debit__note><![CDATA[").append(auto_debit__note).append("]]></auto_debit__note>");
				   xmlString.append("<shipment_lead__time><![CDATA[").append(shipment_lead__time).append("]]></shipment_lead__time>");
				   xmlString.append("<rate_round_to><![CDATA[").append(rate_round_to).append("]]></rate_round_to>");
				   xmlString.append("<rate_round><![CDATA[").append(rate_round).append("]]></rate_round>");
				   xmlString.append("<master_pack><![CDATA[").append(master_pack).append("]]></master_pack>");
				   xmlString.append("<pd_group><![CDATA[").append(pd_group).append("]]></pd_group>");
				   xmlString.append("<cancel_bo><![CDATA[").append(cancel_bo).append("]]></cancel_bo>");
				   xmlString.append("<cust_code__pd><![CDATA[").append(cust_code__pd).append("]]></cust_code__pd>");
				   xmlString.append("<cust_code__disc><![CDATA[").append(cust_code__disc).append("]]></cust_code__disc>");
				   xmlString.append("<min_shelf_life><![CDATA[").append(min_shelf_life).append("]]></min_shelf_life>");
				   xmlString.append("<single_lot><![CDATA[").append(single_lot).append("]]></single_lot>");
				   xmlString.append("<sgroup_code><![CDATA[").append(sgroup_code).append("]]></sgroup_code>");
				   xmlString.append("<old_cust_ref><![CDATA[").append(old_cust_ref).append("]]></old_cust_ref>");
				   xmlString.append("<cust_code__ar><![CDATA[").append(cust_code__ar).append("]]></cust_code__ar>");
				   xmlString.append("<old_corp_parent><![CDATA[").append(old_corp_parent).append("]]></old_corp_parent>");
				   xmlString.append("<store_number><![CDATA[").append(store_number).append("]]></store_number>");
				   xmlString.append("<cust_tran_type><![CDATA[").append(cust_tran_type).append("]]></cust_tran_type>");
				   xmlString.append("<ita_reg_no><![CDATA[").append(ita_reg_no).append("]]></ita_reg_no>");
				   xmlString.append("<eng_name><![CDATA[").append(eng_name).append("]]></eng_name>");
				   xmlString.append("<part_qty><![CDATA[").append(part_qty).append("]]></part_qty>");
				   xmlString.append("<asn_reqd><![CDATA[").append(asn_reqd).append("]]></asn_reqd>");
				   xmlString.append("<wave_type><![CDATA[").append(wave_type).append("]]></wave_type>");
				   xmlString.append("<customer_cust_name><![CDATA[").append(customer_cust_name).append("]]></customer_cust_name>");
				   xmlString.append("<customer_cust_name_1><![CDATA[").append(customer_cust_name_1).append("]]></customer_cust_name_1>");
				   xmlString.append("<return_allowed><![CDATA[").append(return_allowed).append("]]></return_allowed>");
				   xmlString.append("<site_code__pay><![CDATA[").append(site_code__pay).append("]]></site_code__pay>");
				   xmlString.append("<ship_cons_grp><![CDATA[").append(ship_cons_grp).append("]]></ship_cons_grp>");
				   xmlString.append("<bklist_reason><![CDATA[").append(bklist_reason).append("]]></bklist_reason>");
				   xmlString.append("<edi_reqd><![CDATA[").append(edi_reqd).append("]]></edi_reqd>");
				   xmlString.append("<registr_1><![CDATA[").append(registr_1).append("]]></registr_1>");
				   xmlString.append("<registr_2><![CDATA[").append(registr_2).append("]]></registr_2>");
				   xmlString.append("<registr_3><![CDATA[").append(registr_3).append("]]></registr_3>");
				   xmlString.append("<registr_4><![CDATA[").append(registr_4).append("]]></registr_4>");
				   xmlString.append("<registr_5><![CDATA[").append(registr_5).append("]]></registr_5>");
				   xmlString.append("<available_yn><![CDATA[").append(available_yn).append("]]></available_yn>");
				   xmlString.append("<emp_code__ord1><![CDATA[").append(emp_code__ord1).append("]]></emp_code__ord1>");
				   xmlString.append("<emp_code__ord><![CDATA[").append(emp_code__ord).append("]]></emp_code__ord>");
				   xmlString.append("<emp_fname><![CDATA[").append(emp_fname).append("]]></emp_fname>");
				   xmlString.append("<emp_lname><![CDATA[").append(emp_lname).append("]]></emp_lname>");
				   xmlString.append("<employee_emp_fname><![CDATA[").append(employee_emp_fname).append("]]></employee_emp_fname>");
				   xmlString.append("<employee_emp_lname><![CDATA[").append(employee_emp_lname).append("]]></employee_emp_lname>");
				   xmlString.append("<tnt_option><![CDATA[").append(tnt_option).append("]]></tnt_option>");
				   xmlString.append("<auto_stk_alloc><![CDATA[").append(auto_stk_alloc).append("]]></auto_stk_alloc>");
				   xmlString.append("<uniform_code><![CDATA[").append(uniform_code).append("]]></uniform_code>");
				   xmlString.append("<sms_notify><![CDATA[").append(sms_notify).append("]]></sms_notify>");
				   xmlString.append("<email_notify><![CDATA[").append(email_notify).append("]]></email_notify>");
				   xmlString.append("<sundry_balance><![CDATA[").append(sundry_balance).append("]]></sundry_balance>");
				   xmlString.append("<disc_list><![CDATA[").append(disc_list).append("]]></disc_list>");
				   xmlString.append("<descr><![CDATA[").append(descr).append("]]></descr>");
				
				xmlString.append("</Detail1>");
				site_code = siteCode;
		}
		xmlString.append("</Header0></group0></DocumentRoot>");
		
		System.out.println("DOM===="+ xmlString.toString());
		try{
			masterStatefulLocal = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local"); 
			retString = masterStatefulLocal.processRequest( authencate, siteCode, true, xmlString.toString(),false,conn);
			if(retString.contains("Success")){
				returnString = "Y";
			}else{
				returnString = "N";
			 }
		}catch(Exception eee){
			eee.printStackTrace();
		}
	} catch (Exception e) {
		e.printStackTrace();
		throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
	}
	finally{
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	return returnString;
}


	private String checkNull(String str) {
		if(str == null){
			str = "";
		}
		else{
			str = str.trim();
		}
		return str;
	}
}
