package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;  
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import ibase.webitm.ejb.dis.SaleOrdItmGen;
import java.text.*;
import javax.annotation.*;
import javax.ejb.Stateless; // added for ejb3


//public class SorderAttConfEJB extends ActionHandlerEJB implements SessionBean
@Stateless // added for ejb3
public class SorderAttConf extends ActionHandlerEJB implements SorderAttConfLocal, SorderAttConfRemote
{

	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = null;
	ConnDriver connDriver = new ConnDriver();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	Connection conn = null;
	String errorString = null;
	@PostConstruct
	public void createCall() throws RemoteException	//, CreateException 
	{
		System.out.println("Create Method Called....");
		System.out.println("Create Method Called[SaleOrdItmGen].................");
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		}
		catch(Exception e)
		{
			System.out.println("Exception in creating Connection");
		}
	}
		/*
	public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Create Method Called....");
		System.out.println("Create Method Called[SaleOrdItmGen].................");
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		}
		catch(Exception e)
		{
			System.out.println("Exception in creating Connection");
		}
	}*/
	/*public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String actionHandler() throws RemoteException,ITMException
	{
		System.out.println("actionHandler() Method Called....");
		return "";
	}
	public String actionHandler(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException
	{
		Document dom = null;
		String  retString = null;
		System.out.println("Xtra Params : " + xtraParams);
		//genericUtility = GenericUtility.getInstance();
		try
		{	
			retString = actionConfirm(xmlString, xtraParams);  
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Enquiry :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from  actionHandler"+retString);
	    return (retString);
	}
	private String actionConfirm(String tranID, String xtraParams) throws RemoteException , ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "" ,errString = "",confirmed = "";
        String retString = "" ,errCode = "";
		StringBuffer xmlString = new StringBuffer();
		Document dom = null;
		int updst = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		System.out.println("tranID ...."+tranID );
		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
            sql = "SELECT  CONFIRMED FROM SORDFORM_ATT WHERE  TRAN_ID = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tranID);
            rs = pstmt.executeQuery();
            System.out.println("sql....."+sql);
           
            if(rs.next())
            {
                confirmed =rs.getString(1) ;
            }
            rs.close();
            pstmt.close();
            if(confirmed.equalsIgnoreCase("Y"))
            {
           
                errCode = "VTMCONF1";
                errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
                return retString = errString;
            }
            else
            {
			xmlString.append ("<?xml version=\"1.0\"?><Root>");
			xmlString.append("<Detail>");
		
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			SimpleDateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			System.out.println("Confirm Sorder Attribute Called.....");

			sql = " SELECT TRAN_ID ,ORD_DATE, ORDER_TYPE, SITE_CODE, CUST_CODE ,ITEM_SER,"
					+ " CUST_PORD, PORD_DATE, PRICE_LIST, PL_DATE, CONFIRMED, CONF_DATE, "
					+ " EMP_CODE_APRV, CURR_CODE, EXCH_RATE, REMARKS, DUE_DATE,	STATUS,"
					+ "	STATUS_DATE, STATUS_REMARKS, TRAN_CODE,	TRANS_MODE,	ORD_AMT, TOT_AMT,"
					+ "	TAX_AMT, CHG_DATE, CHG_USER, CHG_TERM, PRICE_LIST__CLG,	TAX_DATE,"
					+ "	CUST_CODE__DLV,	CUST_CODE__BIL,	TAX_OPT, SALES_PERS, COMM_PERC,"
					+ "	TAX_CLASS, TAX_CHAP, TAX_ENV, CR_TERM, QUOT_NO,	PROM_DATE, DLV_ADD1,"
					+ " DLV_ADD2, DLV_ADD3, DLV_CITY, STATE_CODE__DLV, COUNT_CODE__DLV, "
					+ " DLV_PIN, STAN_CODE,	PART_QTY, CONSUME_FC,PROJ_CODE,	DLV_TERM ,FRT_AMT,"
					+ " CURR_CODE__FRT ,EXCH_RATE__FRT ,ALLOC_FLAG ,FRT_TERM ,CONTRACT_NO,"
					+ " EMP_CODE__ORD,	INV_AMT ,ADV_PERC ,	DIST_ROUTE ,COMM_PERC__ON,"
					+ " COMM_AMT,CURR_CODE__COMM,SALES_PERS__1,	COMM_PERC_1,COMM_PERC_ON_1,"
					+ "	CURR_CODE__COMM_1,SALES_PERS__2 ,COMM_PERC_2 ,COMM_PERC_ON_2,"
					+ "	CURR_CODE__COMM_2,RCP_MODE,BANK_CODE,ORDER_MODE,UDF__STR1,UDF__STR2,"
					+ "	UDF__NUM1,UDF__NUM2 ,UDF__DATE1 ,OFFSHORE_INVOICE ,	LABEL_TYPE,"
					+ " OUTSIDE_INSPECTION,REMARKS2,REMARKS3,STAN_CODE__INIT,CURR_CODE__INS,"
					+ " EXCH_RATE__INS,INS_AMT ,DLV_TO ,ACCT_CODE__SAL ,CCTR_CODE__SAL,"
					+ " TEL1__DLV ,	TEL2__DLV ,	TEL3__DLV ,FAX__DLV ,EXCH_RATE__COMM,"
					+ " EXCH_RATE__COMM_1 ,	EXCH_RATE__COMM_2 ,	PRICE_LIST__DISC ,MARKET_REG,"
					+ "	HAZARD_YN,SN_CODE,SALES_PERS_COMM_1,SALES_PERS_COMM_2,SALES_PERS_COMM_3,"
					+ "	TOT_ORD_VALUE ,	MAX_ORDER_VALUE ,COMM_AMT__OC ,	LOC_GROUP,FIN_SCHEME,"
					+ " SITE_CODE__SHIP,CHEQUE_DATE,CHEQUE_NO,FOB_VALUE,PORD_MODE,TERR_CODE,"
					+ " CHQ_NAME,CHQ_AMOUNT,REV__TRAN,PARENT__TRAN_ID,CUST_CODE__END,"
					+ " SALE_ORDER__END,ORDER_DB FROM SORDFORM_ATT WHERE TRAN_ID = ?  " ;
					
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs = pstmt.executeQuery();
			System.out.println("sql....."+sql);
			String ord_date = "",tax_date = "",pl_date = "",conf_date = "",due_date = "",prom_date ="",udf__date1 ="",cheque_date ="";
			String status_date ="",	chg_date ="",pord_date = "";
			if(rs.next()) //header  values set  to XML Detail1
																
			{		
				    System.out.println("comm_perc__on......"+rs.getString("comm_perc__on"));
				    xmlString.append("<Detail1>");//dateFormat.format(date)
					xmlString.append("<tran_id><![CDATA[").append( rs.getString("tran_id")).append("]]></tran_id>");
					xmlString.append("<order_date><![CDATA[").append(rs.getDate("ord_date")== null ? "" : dateFormat.format(rs.getDate("ord_date"))).append("]]></order_date>");
					xmlString.append("<order_type><![CDATA[").append(rs.getString("order_type")== null ? "" : rs.getString("order_type")).append("]]></order_type>");
					xmlString.append("<cust_code><![CDATA[").append(rs.getString("cust_code")==null ? "" : rs.getString("cust_code")).append("]]></cust_code>");
					xmlString.append("<cust_code__dlv><![CDATA[").append(rs.getString("cust_code__dlv")== null ? "" : rs.getString("cust_code__dlv")).append("]]></cust_code__dlv>");
					xmlString.append("<cust_code__bil><![CDATA[").append(rs.getString("cust_code__bil")== null ? "" : rs.getString("cust_code__bil")).append("]]></cust_code__bil>");
					//xmlString.append("<cust_name_bill><![CDATA[").append(rs.getString("cust_name_bill")).append("]]></cust_name_bill>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<tax_opt><![CDATA[").append(rs.getString("tax_opt")== null ? "" : rs.getString("tax_opt")).append("]]></tax_opt>");

					xmlString.append("<item_ser><![CDATA[").append(rs.getString("item_ser")== null ? "" : rs.getString("item_ser")).append("]]></item_ser>");
					xmlString.append("<cust_pord><![CDATA[").append(rs.getString("cust_pord")== null ? "" : rs.getString("cust_pord")).append("]]></cust_pord>");
					xmlString.append("<sales_pers><![CDATA[").append(rs.getString("sales_pers")== null ? "" : rs.getString("sales_pers")).append("]]></sales_pers>");	
					xmlString.append("<tax_class><![CDATA[").append(rs.getString("tax_class")== null ? "" : rs.getString("tax_class")).append("]]></tax_class>");
					xmlString.append("<tax_chap><![CDATA[").append(rs.getString("tax_chap")== null ? "" : rs.getString("tax_chap")).append("]]></tax_chap>");
					xmlString.append("<tax_env><![CDATA[").append(rs.getString("tax_env")== null ? "" : rs.getString("tax_env")).append("]]></tax_env>");
					
					xmlString.append("<tax_date><![CDATA[").append(rs.getDate("tax_date")== null ? "" : dateFormat.format(rs.getDate("tax_date"))).append("]]></tax_date>");
					
					xmlString.append("<pl_date><![CDATA[").append(rs.getDate("pl_date")== null ? "" : dateFormat.format(rs.getDate("tax_date"))).append("]]></pl_date>");
					
					xmlString.append("<price_list><![CDATA[").append(rs.getString("price_list")== null ? "" : rs.getString("price_list")).append("]]></price_list>");
					xmlString.append("<cr_term><![CDATA[").append(rs.getString("cr_term")== null ? "" : rs.getString("cr_term")).append("]]></cr_term>");
					xmlString.append("<site_code><![CDATA[").append(rs.getString("site_code")== null ? "" : rs.getString("site_code")).append("]]></site_code>");
					xmlString.append("<quot_no><![CDATA[").append(rs.getString("quot_no")== null ? "" : rs.getString("quot_no")).append("]]></quot_no>");
					xmlString.append("<confirmed><![CDATA[").append("").append("]]></confirmed>");
					
					xmlString.append("<conf_date><![CDATA[").append("").append("]]></conf_date>");
					
					xmlString.append("<curr_code><![CDATA[").append(rs.getString("curr_code")== null ? "" : rs.getString("curr_code")).append("]]></curr_code>");
					
					xmlString.append("<due_date><![CDATA[").append(rs.getDate("due_date")== null ? "" : dateFormat.format(rs.getDate("due_date"))).append("]]></due_date>");
					
					xmlString.append("<prom_date><![CDATA[").append(rs.getDate("prom_date")==null ? "" : dateFormat.format(rs.getDate("prom_date"))).append("]]></prom_date>");
					
					xmlString.append("<remarks ><![CDATA[").append(rs.getString("remarks")== null ? "" : rs.getString("remarks")).append("]]></remarks >");
					xmlString.append("<dlv_add1><![CDATA[").append(rs.getString("dlv_add1")== null ? "" : rs.getString("dlv_add1")).append("]]></dlv_add1>");
					xmlString.append("<dlv_add2><![CDATA[").append(rs.getString("dlv_add2")== null ? "" : rs.getString("dlv_add2")).append("]]></dlv_add2>");			
					xmlString.append("<dlv_city><![CDATA[").append(rs.getString("dlv_city")== null ? "" : rs.getString("dlv_city")).append("]]></dlv_city>");
					xmlString.append("<count_code__dlv><![CDATA[").append(rs.getString("count_code__dlv")== null ? "" : rs.getString("count_code__dlv")).append("]]></count_code__dlv>");
					xmlString.append("<dlv_pin><![CDATA[").append(rs.getString("dlv_pin")== null ? "" : rs.getString("dlv_pin")).append("]]></dlv_pin>");
					xmlString.append("<stan_code><![CDATA[").append(rs.getString("stan_code")== null ? "" : rs.getString("stan_code")).append("]]></stan_code>");
					xmlString.append("<part_qty><![CDATA[").append(rs.getString("part_qty")== null ? "" : rs.getString("part_qty")).append("]]></part_qty>");
					//xmlString.append("<status><![CDATA[").append(rs.getString("status")== null ? "" : rs.getString("status")).append("]]></status>");
					//added by rajendra on 01/06/07
					xmlString.append("<status><![CDATA[").append("P").append("]]></status>");
					xmlString.append("<status_date><![CDATA[").append(rs.getDate("status_date")== null ? "" : dateFormat.format(rs.getDate("status_date"))).append("]]></status_date>");
					
					xmlString.append("<consume_fc><![CDATA[").append(rs.getString("consume_fc")== null ? "" : rs.getString("consume_fc")).append("]]></consume_fc>");
					xmlString.append("<tran_code><![CDATA[").append(rs.getString("tran_code")== null ? "" : rs.getString("tran_code")).append("]]></tran_code>");
					
					xmlString.append("<chg_date/>");
					
					xmlString.append("<chg_user/>");
					xmlString.append("<chg_term/>");
					xmlString.append("<order_db><![CDATA[").append(rs.getString("order_db")== null ? "" : rs.getString("order_db")).append("]]></order_db>");
					//xmlString.append("<crterm_descr><![CDATA[").append(rs.getString("crterm_descr")== null ? "" : rs.getString("crterm_descr")).append("]]></crterm_descr>");//EXIST IN XML BUT NOT IN TABLE
					//xmlString.append("<station_descr><![CDATA[").append(rs.getString("station_descr")== null ? "" : rs.getString("station_descr")).append("]]></station_descr>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<proj_code><![CDATA[").append(rs.getString("proj_code")== null ? "" : rs.getString("proj_code")).append("]]></proj_code>");
					xmlString.append("<tot_amt><![CDATA[").append(rs.getString("tot_amt")== null ? "" : rs.getString("tot_amt")).append("]]></tot_amt>");
					xmlString.append("<comm_perc__on><![CDATA[").append(rs.getString("comm_perc__on")== null ? "" : rs.getString("comm_perc__on")).append("]]></comm_perc__on>");	
					xmlString.append("<udf__str1><![CDATA[").append(rs.getString("udf__str1")== null ? "" : rs.getString("udf__str1")).append("]]></udf__str1>");
					xmlString.append("<udf__str2><![CDATA[").append(rs.getString("udf__str2")== null ? "" : rs.getString("udf__str2")).append("]]></udf__str2>");
					xmlString.append("<status_remarks><![CDATA[").append(rs.getString("status_remarks")== null ? "" : rs.getString("status_remarks")).append("]]></status_remarks>");
					xmlString.append("<dlv_term><![CDATA[").append(rs.getString("dlv_term")== null ? "" : rs.getString("dlv_term")).append("]]></dlv_term>");
					//xmlString.append("<tran_name><![CDATA[").append(rs.getString("tran_name")== null ? "" : rs.getString("tran_name")).append("]]></tran_name>");//EXIST IN XML BUT NOT IN TABLE
					//xmlString.append("<sp_name><![CDATA[").append(rs.getString("sp_name")== null ? "" : rs.getString("sp_name")).append("]]></sp_name>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<curr_code__frt><![CDATA[").append(rs.getString("curr_code__frt")== null ? "" : rs.getString("curr_code__frt")).append("]]></curr_code__frt>");
					xmlString.append("<frt_term><![CDATA[").append(rs.getString("frt_term")== null ? "" : rs.getString("frt_term")).append("]]></frt_term>");
					xmlString.append("<alloc_flag><![CDATA[").append(rs.getString("alloc_flag")== null ? "" : rs.getString("alloc_flag")).append("]]></alloc_flag>");	
					xmlString.append("<contract_no><![CDATA[").append(rs.getString("contract_no")== null ? "" : rs.getString("contract_no")).append("]]></contract_no>");
					//xmlString.append("<cust_name><![CDATA[").append(rs.getString("cust_name")== null ? "" : rs.getString("cust_name")).append("]]></cust_name>");
					xmlString.append("<emp_code__ord><![CDATA[").append(rs.getString("emp_code__ord")== null ? "" : rs.getString("emp_code__ord")).append("]]></emp_code__ord>");
					
					xmlString.append("<pord_date><![CDATA[").append(rs.getDate("pord_date")== null ? "" : dateFormat.format(rs.getDate("pord_date"))).append("]]></pord_date>");
					
					xmlString.append("<dist_route><![CDATA[").append(rs.getString("dist_route")== null ? "" : rs.getString("dist_route")).append("]]></dist_route>");
					xmlString.append("<curr_code__comm><![CDATA[").append(rs.getString("curr_code__comm")== null ? "" : rs.getString("curr_code__comm")).append("]]></curr_code__comm>");
					xmlString.append("<comm_perc_on_1><![CDATA[").append(rs.getString("comm_perc_on_1")== null ? "" : rs.getString("comm_perc_on_1")).append("]]></comm_perc_on_1>");
					xmlString.append("<curr_code__comm_1><![CDATA[").append(rs.getString("curr_code__comm_1")== null ? "" : rs.getString("curr_code__comm_1")).append("]]></curr_code__comm_1>");
					xmlString.append("<sales_pers__2><![CDATA[").append(rs.getString("sales_pers__2")== null ? "" : rs.getString("sales_pers__2")).append("]]></sales_pers__2>");
					xmlString.append("<comm_perc_on_2><![CDATA[").append(rs.getString("comm_perc_on_2")== null ? "" : rs.getString("comm_perc_on_2")).append("]]></comm_perc_on_2>");
					//xmlString.append("<currency_descr><![CDATA[").append(rs.getString("currency_descr")== null ? "" : rs.getString("currency_descr")).append("]]></currency_descr>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<comm_perc><![CDATA[").append(rs.getString("comm_perc_1")== null ? "" : rs.getString("comm_perc")).append("]]></comm_perc>");
					xmlString.append("<exch_rate><![CDATA[").append(rs.getString("exch_rate")== null ? "0" : rs.getString("exch_rate")).append("]]></exch_rate>");
					xmlString.append("<ord_amt><![CDATA[").append(rs.getString("ord_amt")== null ? "0" : rs.getString("ord_amt")).append("]]></ord_amt>");
					xmlString.append("<tax_amt><![CDATA[").append(rs.getString("tax_amt")== null ? "0" : rs.getString("tax_amt")).append("]]></tax_amt>");
					xmlString.append("<udf__num1><![CDATA[").append(rs.getString("udf__num1")== null ? "0" : rs.getString("udf__num1")).append("]]></udf__num1>");
					xmlString.append("<udf__num2><![CDATA[").append(rs.getString("udf__num2")== null ? "0" : rs.getString("udf__num2")).append("]]></udf__num2>");
					xmlString.append("<frt_amt><![CDATA[").append(rs.getString("frt_amt")== null ? "0" : rs.getString("frt_amt")).append("]]></frt_amt>");
					xmlString.append("<inv_amt><![CDATA[").append(rs.getString("inv_amt")== null ? "" : rs.getString("inv_amt")).append("]]></inv_amt>");
					xmlString.append("<adv_perc><![CDATA[").append(rs.getString("adv_perc")== null ? "" : rs.getString("adv_perc")).append("]]></adv_perc>");
					xmlString.append("<sales_pers__1><![CDATA[").append(rs.getString("sales_pers__1")== null ? "" : rs.getString("sales_pers__1")).append("]]></sales_pers__1>");
					xmlString.append("<comm_perc_1><![CDATA[").append(rs.getString("comm_perc_1")== null ? "0" : rs.getString("comm_perc_1")).append("]]></comm_perc_1>");
					xmlString.append("<comm_perc_2><![CDATA[").append(rs.getString("comm_perc_2")== null ? "0" : rs.getString("comm_perc_2")).append("]]></comm_perc_2>");	
					
					xmlString.append("<udf__date1><![CDATA[").append(rs.getDate("udf__date1")== null ? "" : dateFormat.format(rs.getDate("udf__date1"))).append("]]></udf__date1>");
					
					xmlString.append("<trans_mode><![CDATA[").append(rs.getString("trans_mode")== null ? "" : rs.getString("trans_mode")).append("]]></trans_mode>");	
					xmlString.append("<rcp_mode><![CDATA[").append(rs.getString("rcp_mode")== null ? "" : rs.getString("rcp_mode")).append("]]></rcp_mode>");
					xmlString.append("<bank_code><![CDATA[").append(rs.getString("bank_code")== null ? "" : rs.getString("bank_code")).append("]]></bank_code>");
					xmlString.append("<state_code__dlv><![CDATA[").append(rs.getString("state_code__dlv")== null ? "" : rs.getString("state_code__dlv")).append("]]></state_code__dlv>");	
					xmlString.append("<dlv_add3><![CDATA[").append(rs.getString("dlv_add3")== null ? "" : rs.getString("dlv_add3")).append("]]></dlv_add3>");
					xmlString.append("<comm_amt><![CDATA[").append(rs.getString("comm_amt")== null ? "0" : rs.getString("comm_amt")).append("]]></comm_amt>");
					xmlString.append("<order_mode><![CDATA[").append(rs.getString("order_mode")== null ? "" : rs.getString("order_mode")).append("]]></order_mode>");
					xmlString.append("<remarks2><![CDATA[").append(rs.getString("remarks2")== null ? "" : rs.getString("remarks2")).append("]]></remarks2>");
					xmlString.append("<remarks3><![CDATA[").append(rs.getString("remarks3")== null ? "" : rs.getString("remarks3")).append("]]></remarks3>");
					xmlString.append("<curr_code__ins><![CDATA[").append(rs.getString("curr_code__ins")== null ? "" : rs.getString("curr_code__ins")).append("]]></curr_code__ins>");
					xmlString.append("<exch_rate__ins><![CDATA[").append(rs.getString("exch_rate__ins")== null ? "0" : rs.getString("exch_rate__ins")).append("]]></exch_rate__ins>");
					xmlString.append("<ins_amt><![CDATA[").append(rs.getString("ins_amt")== null ? "0" : rs.getString("ins_amt")).append("]]></ins_amt>");	
					xmlString.append("<stan_code__init><![CDATA[").append(rs.getString("stan_code__init")== null ? "" : rs.getString("stan_code__init")).append("]]></stan_code__init>");
					xmlString.append("<exch_rate__frt><![CDATA[").append(rs.getString("exch_rate__frt")== null ? "" : rs.getString("exch_rate__frt")).append("]]></exch_rate__frt>");	
					xmlString.append("<dlv_to><![CDATA[").append(rs.getString("dlv_to")== null ? "" : rs.getString("dlv_to")).append("]]></dlv_to>");	
					//xmlString.append("<sales_pers_sp_name><![CDATA[").append(rs.getString("sales_pers_sp_name")== null ? "" : rs.getString("sales_pers_sp_name")).append("]]></sales_pers_sp_name>");//EXIST IN XML BUT NOT IN TABLE	
					xmlString.append("<acct_code__sal><![CDATA[").append(rs.getString("acct_code__sal")== null ? "" : rs.getString("acct_code__sal")).append("]]></acct_code__sal>");
					xmlString.append("<cctr_code__sal><![CDATA[").append(rs.getString("cctr_code__sal")== null ? "" : rs.getString("cctr_code__sal")).append("]]></cctr_code__sal>");
					xmlString.append("<label_type><![CDATA[").append(rs.getString("label_type")== null ? "" : rs.getString("label_type")).append("]]></label_type>");
					xmlString.append("<outside_inspection><![CDATA[").append(rs.getString("outside_inspection")== null ? "" : rs.getString("outside_inspection")).append("]]></outside_inspection>");
					xmlString.append("<tel1__dlv><![CDATA[").append(rs.getString("tel1__dlv")== null ? "" : rs.getString("tel1__dlv")).append("]]></tel1__dlv>");
					xmlString.append("<tel2__dlv><![CDATA[").append(rs.getString("tel2__dlv")== null ? "" : rs.getString("tel2__dlv")).append("]]></tel2__dlv>");
					xmlString.append("<tel3__dlv><![CDATA[").append(rs.getString("tel3__dlv")== null ? "" : rs.getString("tel3__dlv")).append("]]></tel3__dlv>");
					xmlString.append("<fax__dlv><![CDATA[").append(rs.getString("fax__dlv")== null ? "" : rs.getString("fax__dlv")).append("]]></fax__dlv>");
					xmlString.append("<exch_rate__comm><![CDATA[").append(rs.getString("exch_rate__comm")== null ? "0" : rs.getString("exch_rate__comm")).append("]]></exch_rate__comm>");
					xmlString.append("<exch_rate__comm_1><![CDATA[").append(rs.getString("exch_rate__comm_1")== null ? "0" : rs.getString("exch_rate__comm_1")).append("]]></exch_rate__comm_1>");
					xmlString.append("<exch_rate__comm_2><![CDATA[").append(rs.getString("exch_rate__comm_2")== null ? "0" : rs.getString("exch_rate__comm_2")).append("]]></exch_rate__comm_2>");
					//xmlString.append("<sales_pers_sp_name_1><![CDATA[").append(rs.getString("sales_pers_sp_name_1")== null ? "" : rs.getString("sales_pers_sp_name_1")).append("]]></sales_pers_sp_name_1>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<price_list__disc><![CDATA[").append(rs.getString("price_list__disc")== null ? "" : rs.getString("price_list__disc")).append("]]></price_list__disc>");
					xmlString.append("<market_reg><![CDATA[").append(rs.getString("market_reg")== null ? "" : rs.getString("market_reg")).append("]]></market_reg>");
					//xmlString.append("<email_addr><![CDATA[").append(rs.getString("email_addr")== null ? "" : rs.getString("email_addr")).append("]]></email_addr>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<hazard_yn><![CDATA[").append(rs.getString("hazard_yn")== null ? "" : rs.getString("hazard_yn")).append("]]></hazard_yn>");	
					xmlString.append("<sales_pers_comm_1><![CDATA[").append(rs.getString("sales_pers_comm_1")== null ? "0" : rs.getString("sales_pers_comm_1")).append("]]></sales_pers_comm_1>");
					xmlString.append("<sales_pers_comm_2><![CDATA[").append(rs.getString("sales_pers_comm_2")== null ? "0" :rs.getString("sales_pers_comm_2")).append("]]></sales_pers_comm_2>");
					xmlString.append("<sales_pers_comm_3><![CDATA[").append(rs.getString("sales_pers_comm_3") == null ? "0" : rs.getString("sales_pers_comm_3")).append("]]></sales_pers_comm_3>");
					xmlString.append("<sn_code><![CDATA[").append(rs.getString("sn_code")== null ? "" : rs.getString("sn_code")).append("]]></sn_code>");					
					xmlString.append("<tot_ord_value><![CDATA[").append(rs.getString("tot_ord_value")== null ? "0" : rs.getString("tot_ord_value")).append("]]></tot_ord_value>");
					xmlString.append("<max_order_value><![CDATA[").append(rs.getString("max_order_value")== null ? "0" : rs.getString("max_order_value")).append("]]></max_order_value>");
					xmlString.append("<cust_code__end><![CDATA[").append(rs.getString("cust_code__end")== null ? "" : rs.getString("cust_code__end")).append("]]></cust_code__end>");
					xmlString.append("<sale_order__end><![CDATA[").append(rs.getString("sale_order__end")== null ? "" : rs.getString("sale_order__end")).append("]]></sale_order__end>");
					xmlString.append("<comm_amt__oc><![CDATA[").append(rs.getString("comm_amt__oc")== null ? "0" : rs.getString("comm_amt__oc")).append("]]></comm_amt__oc>");
					xmlString.append("<loc_group><![CDATA[").append(rs.getString("loc_group")== null ? "" : rs.getString("loc_group")).append("]]></loc_group>");
					xmlString.append("<fin_scheme><![CDATA[").append(rs.getString("fin_scheme")== null ? "" : rs.getString("fin_scheme")).append("]]></fin_scheme>");
					xmlString.append("<site_code__ship><![CDATA[").append(rs.getString("site_code__ship")== null ? "" : rs.getString("site_code__ship")).append("]]></site_code__ship>");
					xmlString.append("<price_list__clg><![CDATA[").append(rs.getString("price_list__clg")== null ? "" : rs.getString("price_list__clg")).append("]]></price_list__clg>");
					xmlString.append("<parent__tran_id><![CDATA[").append(rs.getString("parent__tran_id")== null ? "" : rs.getString("parent__tran_id")).append("]]></parent__tran_id>");
					xmlString.append("<rev__tran><![CDATA[").append(rs.getString("rev__tran")== null ? "" : rs.getString("rev__tran")).append("]]></rev__tran>");
					xmlString.append("<cheque_no><![CDATA[").append(rs.getString("cheque_no")== null ? "" : rs.getString("cheque_no")).append("]]></cheque_no>");
					
					xmlString.append("<cheque_date><![CDATA[").append(rs.getDate("cheque_date")==null ?"":dateFormat.format(rs.getDate("cheque_date"))).append("]]></cheque_date>");
					
					//xmlString.append("<itemser_descr><![CDATA[").append(rs.getString("itemser_descr")== null ? "" : rs.getString("itemser_descr")).append("]]></itemser_descr>");//EXIST IN XML BUT NOT IN TABLE
					//xmlString.append("<descr><![CDATA[").append(rs.getString("descr")== null ? "" : rs.getString("descr")).append("]]></descr>"); //EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<fob_value><![CDATA[").append(rs.getString("fob_value")== null ? "0" : rs.getString("fob_value")).append("]]></fob_value>");
					xmlString.append("<pord_mode><![CDATA[").append(rs.getString("pord_mode")== null ? "" : rs.getString("pord_mode")).append("]]></pord_mode>");
					xmlString.append("<terr_code><![CDATA[").append(rs.getString("terr_code")== null ? "" : rs.getString("terr_code")).append("]]></terr_code>");
					//xmlString.append("<territory_descr><![CDATA[").append(rs.getString("territory_descr")== null ? "" : rs.getString("territory_descr")).append("]]></territory_descr>");//EXIST IN XML BUT NOT IN TABLE
					xmlString.append("<chq_amount><![CDATA[").append(rs.getString("chq_amount")== null ? "0" : rs.getString("chq_amount")).append("]]></chq_amount>");
					xmlString.append("<chq_name><![CDATA[").append(rs.getString("chq_name")== null ? "" : rs.getString("chq_name")).append("]]></chq_name>");
					//xmlString.append("<cr_lmt><![CDATA[").append(rs.getString("cr_lmt")== null ? "" : rs.getString("cr_lmt")).append("]]></cr_lmt>");//EXIST IN XML BUT NOT IN TABLE
					//xmlString.append("<os_amt><![CDATA[").append(rs.getString("os_amt")== null ? "" : rs.getString("os_amt")).append("]]></os_amt>");//EXIST IN XML BUT NOT IN TABLE
					//xmlString.append("<ovos_amt><![CDATA[").append(rs.getString("ovos_amt")== null ? "" : rs.getString("ovos_amt")).append("]]></ovos_amt>");//EXIST IN XML BUT NOT IN TABLE
										
					//xmlString.append("<curr_code__comm_2><![CDATA[").append(rs.getString("curr_code__comm_2")== null ? "" : rs.getString("curr_code__comm_2")).append("]]></curr_code__comm_2>");//EXIST IN TABLE  BUT NOT IN XML
					//xmlString.append("<emp_code_aprv><![CDATA[").append(rs.getString("emp_code_aprv")== null ? "" : rs.getString("emp_code_aprv")).append("]]></emp_code_aprv>");//EXIST IN TABLE  BUT NOT IN XML
					//xmlString.append("<offshore_invoice><![CDATA[").append(rs.getString("offshore_invoice")== null ? "" : rs.getString("offshore_invoice")).append("]]></offshore_invoice>");//EXIST IN TABLE  BUT NOT IN XML
								
					
			
					xmlString.append("</Detail1>");
							
			} //end detail1
			rs.close();
			pstmt.close();
			//Get Detail2
			sql = " SELECT TRAN_ID ,LINE_NO ,ITEM_TYPE ,QUANTITY ,UNIT ,RATE ,UNIT__RATE,"
					+ "	PRICE_LIST__DISC,DISCOUNT,UNIT__STD,CONV__QTY_STDUOM,QUANTITY__STDUOM,"
					+ " CONV__RTUOM_STDUOM ,RATE__STDUOM ,RATE__CLG ,TAX_CLASS ,TAX_CHAP,"
					+ " TAX_ENV ,TAX_AMT ,NET_AMT ,REMARKS ,STATUS ,STATUS_DATE ,NO_ART,"
					+ " LOC_TYPE ,ITEM_CODE ,PHY_ATTRIB_1 ,PHY_ATTRIB_2 ,PHY_ATTRIB_3,"
					+ " PHY_ATTRIB_4 ,PHY_ATTRIB_5 ,PHY_ATTRIB_6 ,PHY_ATTRIB_7 ,PHY_ATTRIB_8,"
					+ " PHY_ATTRIB_9 ,PHY_ATTRIB_10 ,PHY_ATTRIB_11 ,PHY_ATTRIB_12 ,PHY_ATTRIB_13,"
					+ " PHY_ATTRIB_14 ,PHY_ATTRIB_15 ,ITEM_FLAG ,FINISHSIZE ,MIX_PACK ,PHY_ATTRIB_16,"
					+ " PHY_ATTRIB_17 ,PHY_ATTRIB_18 ,PHY_ATTRIB_19 ,PHY_ATTRIB_20 ,PHY_ATTRIB_21,"
					+ " PHY_ATTRIB_22 ,	ITEM_DESCR  FROM SORDFORM_ATT_DET WHERE "
					+ " TRAN_ID = ? AND PHY_ATTRIB_21 = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			pstmt.setString(2, "N");
			rs = pstmt.executeQuery();
			System.out.println("sql......."+sql);
			
			while (rs.next())		  
			{ 
				    xmlString.append("<Detail2>");
					xmlString.append("<tran_id><![CDATA[").append(rs.getString("tran_id")).append("]]></tran_id>");
					xmlString.append("<line_no><![CDATA[").append(rs.getString("line_no") == null ? "":rs.getString("line_no")).append("]]></line_no>");
					xmlString.append("<item_type><![CDATA[").append(rs.getString("item_type") ==null ? "":rs.getString("item_type")).append("]]></item_type>");
					xmlString.append("<quantity><![CDATA[").append(rs.getDouble("quantity")).append("]]></quantity>");
					xmlString.append("<unit><![CDATA[").append(rs.getString("unit") == null ? "":rs.getString("unit")).append("]]></unit>");
					xmlString.append("<rate><![CDATA[").append(rs.getDouble("rate")).append("]]></rate>");
					xmlString.append("<unit__rate><![CDATA[").append(rs.getString("unit__rate") ==null ? "":rs.getString("unit__rate") ).append("]]></unit__rate>");
					xmlString.append("<price_list__disc><![CDATA[").append(rs.getString("price_list__disc") ==null ? "":rs.getString("price_list__disc")).append("]]></price_list__disc>");
					xmlString.append("<discount><![CDATA[").append(rs.getDouble("discount")).append("]]></discount>");
					xmlString.append("<unit__std><![CDATA[").append(rs.getString("unit__std") ==null ? "":rs.getString("unit__std")).append("]]></unit__std>");
					xmlString.append("<conv__qty_stduom><![CDATA[").append(rs.getDouble("conv__qty_stduom")).append("]]></conv__qty_stduom>");
					xmlString.append("<quantity__stduom><![CDATA[").append(rs.getDouble("quantity__stduom")).append("]]></quantity__stduom>");
					xmlString.append("<conv__rtuom_stduom><![CDATA[").append(rs.getDouble("conv__rtuom_stduom")).append("]]></conv__rtuom_stduom>");
					xmlString.append("<rate__stduom><![CDATA[").append(rs.getDouble("rate__stduom")).append("]]></rate__stduom>");
					xmlString.append("<rate__clg><![CDATA[").append(rs.getDouble("rate__clg")).append("]]></rate__clg>");
					xmlString.append("<tax_class><![CDATA[").append(rs.getString("tax_class") ==null ? "":rs.getString("tax_class")).append("]]></tax_class>");
					xmlString.append("<tax_chap><![CDATA[").append(rs.getString("tax_chap")==null ? "":rs.getString("tax_chap")).append("]]></tax_chap>");
					xmlString.append("<tax_env><![CDATA[").append(rs.getString("tax_env") ==null ? "":rs.getString("tax_env")).append("]]></tax_env>");
					xmlString.append("<tax_amt><![CDATA[").append(rs.getDouble("tax_amt")).append("]]></tax_amt>");
					xmlString.append("<net_amt><![CDATA[").append(rs.getDouble("net_amt")).append("]]></net_amt>");
					xmlString.append("<remarks><![CDATA[").append(rs.getString("remarks") ==null ? "":rs.getString("remarks")).append("]]></remarks>");
					xmlString.append("<status><![CDATA[").append(rs.getString("status") == null ? "":rs.getString("status") ).append("]]></status>");
 			        xmlString.append("<status_date><![CDATA[").append(rs.getDate("status_date") == null ?"":dateFormat.format(rs.getDate("status_date"))).append("]]></status_date>");
					xmlString.append("<no_art><![CDATA[").append(rs.getInt("no_art")).append("]]></no_art>");
					xmlString.append("<loc_type><![CDATA[").append(rs.getString("loc_type") ==null ?"":rs.getString("loc_type")).append("]]></loc_type>");
					xmlString.append("<item_code><![CDATA[").append(rs.getString("item_code") ==null ? "":rs.getString("item_code")).append("]]></item_code>");
					xmlString.append("<phy_attrib_1><![CDATA[").append(rs.getString("phy_attrib_1") ==null ?"":rs.getString("phy_attrib_1")).append("]]></phy_attrib_1>");
					xmlString.append("<phy_attrib_2><![CDATA[").append(rs.getString("phy_attrib_2") == null ? "":rs.getString("phy_attrib_2")).append("]]></phy_attrib_2>");
					xmlString.append("<phy_attrib_3><![CDATA[").append(rs.getString("phy_attrib_3") ==null ? "" :rs.getString("phy_attrib_3")).append("]]></phy_attrib_3>");
					xmlString.append("<phy_attrib_4><![CDATA[").append(rs.getString("phy_attrib_4") ==null ? "":rs.getString("phy_attrib_4")).append("]]></phy_attrib_4>");
					xmlString.append("<phy_attrib_5><![CDATA[").append(rs.getString("phy_attrib_5") ==null ? "":rs.getString("phy_attrib_5")).append("]]></phy_attrib_5>");
					xmlString.append("<phy_attrib_6><![CDATA[").append(rs.getString("phy_attrib_6") == null ? "":rs.getString("phy_attrib_6")).append("]]></phy_attrib_6>");
					xmlString.append("<phy_attrib_7><![CDATA[").append(rs.getString("phy_attrib_7") ==null ? "":rs.getString("phy_attrib_7")).append("]]></phy_attrib_7>");
					xmlString.append("<phy_attrib_8><![CDATA[").append(rs.getString("phy_attrib_8") ==null ? "":rs.getString("phy_attrib_8")).append("]]></phy_attrib_8>");
					xmlString.append("<phy_attrib_9><![CDATA[").append(rs.getString("phy_attrib_9") ==null ? "" :rs.getString("phy_attrib_9")).append("]]></phy_attrib_9>");
					xmlString.append("<phy_attrib_10><![CDATA[").append(rs.getString("phy_attrib_10") ==null ? "":rs.getString("phy_attrib_10")).append("]]></phy_attrib_10>");
					xmlString.append("<phy_attrib_11><![CDATA[").append(rs.getString("phy_attrib_11") ==null ? "":rs.getString("phy_attrib_11")).append("]]></phy_attrib_11>");
					xmlString.append("<phy_attrib_12><![CDATA[").append(rs.getString("phy_attrib_12") ==null ? "":rs.getString("phy_attrib_12") ).append("]]></phy_attrib_12>");
					xmlString.append("<phy_attrib_13><![CDATA[").append(rs.getString("phy_attrib_13") ==null ? "":rs.getString("phy_attrib_13")).append("]]></phy_attrib_13>");
					xmlString.append("<phy_attrib_14><![CDATA[").append(rs.getString("phy_attrib_14") ==null ? "":rs.getString("phy_attrib_14")).append("]]></phy_attrib_14>");
					xmlString.append("<phy_attrib_15><![CDATA[").append(rs.getString("phy_attrib_15") ==null ? "":rs.getString("phy_attrib_15")).append("]]></phy_attrib_15>");
					xmlString.append("<item_flag><![CDATA[").append(rs.getString("item_flag") ==null ? "":rs.getString("item_flag")).append("]]></item_flag>");
					xmlString.append("<finishsize><![CDATA[").append(rs.getString("finishsize") ==null ? "":rs.getString("finishsize")).append("]]></finishsize>");
					xmlString.append("<mix_pack><![CDATA[").append(rs.getString("mix_pack") == null ? "":rs.getString("mix_pack")).append("]]></mix_pack>");
					xmlString.append("<phy_attrib_16><![CDATA[").append(rs.getString("phy_attrib_16") ==null ? "":rs.getString("phy_attrib_16")).append("]]></phy_attrib_16>");
					xmlString.append("<phy_attrib_17><![CDATA[").append(rs.getString("phy_attrib_17") ==null ? "":rs.getString("phy_attrib_17")).append("]]></phy_attrib_17>");
					xmlString.append("<phy_attrib_18><![CDATA[").append(rs.getString("phy_attrib_18") ==null ? "":rs.getString("phy_attrib_18")).append("]]></phy_attrib_18>");
					xmlString.append("<phy_attrib_19><![CDATA[").append(rs.getString("phy_attrib_19") ==null ? "":rs.getString("phy_attrib_19")).append("]]></phy_attrib_19>");
					xmlString.append("<phy_attrib_20><![CDATA[").append(rs.getString("phy_attrib_20") ==null ? "":rs.getString("phy_attrib_20")).append("]]></phy_attrib_20>");
					xmlString.append("<phy_attrib_21><![CDATA[").append(rs.getString("phy_attrib_21") ==null ? "":rs.getString("phy_attrib_21")).append("]]></phy_attrib_21>");
					xmlString.append("<phy_attrib_22><![CDATA[").append(rs.getString("phy_attrib_22") ==null ? "":rs.getString("phy_attrib_22")).append("]]></phy_attrib_22>");
					xmlString.append("<item_descr><![CDATA[").append(rs.getString("item_descr") ==null ? "":rs.getString("item_descr")).append("]]></item_descr>");
					xmlString.append("</Detail2>");
				
			}
			rs.close();
			pstmt.close();
			
			xmlString.append("</Detail>");
			xmlString.append("</Root>");
			System.out.println("xmlString..... ::"+xmlString.toString());
		   	dom = genericUtility.parseString(xmlString.toString());
			SaleOrdItmGen iso =	new SaleOrdItmGen();
		    retString = iso.genItemSalesOrd(dom,conn);
			if(retString.indexOf("Success") == -1)
			{
				conn.rollback();
			}
			if(retString.indexOf("Success") != -1)
			{
			    sql	="UPDATE SORDFORM_ATT SET  CONFIRMED = ? , CONF_DATE = ? WHERE  TRAN_ID = ? ";
				
				pstmt = conn.prepareStatement(sql);
				System.out.println("sql......."+sql);
			    pstmt.setString(1, "Y");
				pstmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
			    pstmt.setString(3, tranID);
			    updst = pstmt.executeUpdate();
				if(updst == 1)
				{
					System.out.println("CONFIRMED UPDATED ::"+updst);
					
					errCode = "CONFSUCC";
					errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
					retString = errString;
				}
				pstmt.close();
			
			}
			
			System.out.println("retString ::"+retString);
            }	
		}
		catch(Exception e)
		{  
			try
			{
				conn.rollback();
				
			}
			catch(Exception e1)
			{				   
			   System.out.println("Ex...."+e1.getMessage());
			   e1.printStackTrace();
			}
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				conn.commit();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			/*	if(conn != null)
				{
					conn.close();
					conn = null;
				}	   */
			} 
			catch(Exception e)
			{
				System.err.println("Exception :SorderAttConfEJB......... :\n"+e.getMessage());
				
			}
		}
		System.out.println("retString ::"+retString);
		return retString;
	}
	
}
		