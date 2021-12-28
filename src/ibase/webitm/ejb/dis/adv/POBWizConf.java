/********************************************************
	Title : POBWizConf[D15ESUN017]
	Date  : 10/09/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;


import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;

import java.sql.*;
import java.text.SimpleDateFormat;

import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.GenerateXmlFromDB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Calendar;

@Stateless
public class POBWizConf extends ActionHandlerEJB implements POBWizConfLocal, POBWizConfRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	
	public String pobConfirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		String userInfoStr = "";
		String errString = "";
		try
		{
			// Modified by Piyush on 26/11/2020 [For User info to provide button on front end]
			userInfoStr = getUserInfo().toString();
			System.out.println("userInfoStr of confirm::::: " +userInfoStr);
			errString = pobConfirm(tranId, xtraParams, forcedFlag, userInfoStr);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in [POBWizConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
		
	}
	
	public String pobConfirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>POBWizConf confirm called>>>>>>>>>>>>>>>>>>>");
		System.out.println("inside pobConfirm:::::::::");

		String confirmed = "";
		String sql = "";
		String transDB = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;
	    String errString = null;
		ResultSet rs = null;
		String loginEmpCode="",siteCode="";
		String pobNo="",loginSiteCode="",custCode="";
		String itemSer="",orderType="",retailerCode="";
		String pricelist="",pricelistClg="",crTerm="",custCodeBil="",salespers="",salespers1="",salespers2="",frtTerm="";
		String groupCode="",salesPersTmp="",crTermTmp="",salesPersTmp1="",salesPersTmp2="",transMode="";
		String deliveryTerm="",tranCode="",currCodeFrt="",currCodeIns="",add1="",add2="",add3="",tele1="",tele2="",tele3="";
		String stanCode="",city="",countCode="",pin="",stateCode="",dlvDescr="";
		String userId="",termId="";	
		String currCode1="",currCode2="",discount="";
		String linenoStr="",itemCode="",rate="",quantityStr="",freeQtyStr="",totQty="",netAmt="";
		StringBuffer xmlBuff = null;
		String xmlString = null,retString  = null;
		String sysDate="",lineNoStr="";
	    int cnt = 0,lineNo=0;
		double exchRateFr=0;
		double quantity=0,freeQty=0;
		Timestamp currDate = null;
		Timestamp sysDate1 = null,addDate=null,chgDate=null, tranDate = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		FinCommon finCommon = null;
		String schemeCode = "",itemFlag = "";
		double totQuantity = 0;
		try 
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if(userInfoStr != null && userInfoStr.trim().length() > 0)
			{
				UserInfoBean userInfo = new UserInfoBean(userInfoStr);
		    	transDB       = userInfo.getTransDB();
			}
	    	if (transDB != null && transDB.trim().length() > 0)
	    	{
	    		conn = connDriver.getConnectDB(transDB);
		    }
			
	    	else
	    	{
	    		conn = connDriver.getConnectDB("DriverITM");
	    	}
			conn.setAutoCommit(false);
			finCommon = new FinCommon();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  
			termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			currDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			Calendar currentDate = Calendar.getInstance();
			System.out.println("currDate>>>>>>"+currDate);
			SimpleDateFormat sdf = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			 sysDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDate);
			if (tranId != null && tranId.trim().length() > 0) 
			{
				System.out.println("@@@@@tranId "+tranId+"]");

				sql = "	select tran_id,tran_date,site_code,item_ser,order_type,cust_code,confirmed " +
						" from  pob_hdr where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{

					pobNo = rs.getString("tran_id");
					tranDate	= rs.getTimestamp("tran_date");
					siteCode=checkNull(rs.getString("site_code"));
					itemSer=checkNull(rs.getString("item_ser"));		
					orderType=checkNull(rs.getString("order_type"));
					custCode=checkNull(rs.getString("cust_code"));
					confirmed=checkNull(rs.getString("confirmed"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("N".equalsIgnoreCase(confirmed))
				{
					sql = "select price_list, price_list__clg from site_customer where site_code = ? and cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						pricelist = rs.getString("price_list");
						pricelistClg = rs.getString("price_list__clg");
					}

					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;

					sql = "select cr_term,stan_code ,city,count_code,pin,state_code,frt_term,curr_code," + 
							"cust_name,group_code,trans_mode,cust_code__bil,price_list,price_list__clg," +
							"sales_pers,sales_pers__1,sales_pers__2,dlv_term, TRAN_CODE, addr1,addr2,addr3,tele1,tele2,tele3 " +
							" from customer where cust_code = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						crTerm = rs.getString("cr_term") == null ? " " : rs.getString("cr_term");
						stanCode = rs.getString("stan_code") == null ? " " : rs.getString("stan_code");
						city = rs.getString("city") == null ? " " : rs.getString("city");
						countCode = rs.getString("count_code") == null ? " " : rs.getString("count_code");
						pin = rs.getString("pin") == null ? " " : rs.getString("pin");
						stateCode = rs.getString("state_code") == null ? " " : rs.getString("state_code");
						frtTerm = rs.getString("frt_term") == null ? " " : rs.getString("frt_term");
						dlvDescr = rs.getString("cust_name");
						groupCode = rs.getString("group_code") == null ? " " : rs.getString("group_code");
						transMode = rs.getString("trans_mode") == null ? " " : rs.getString("trans_mode");
						custCodeBil = rs.getString("cust_code__bil") == null ? " " : rs.getString("cust_code__bil");
						pricelist = rs.getString("price_list") == null ? " " : rs.getString("price_list");
						pricelistClg = rs.getString("price_list__clg") == null ? " " : rs.getString("price_list__clg");
						salespers = rs.getString("sales_pers") == null ? " " : rs.getString("sales_pers");
						salespers1 = rs.getString("sales_pers__1") == null ? " " : rs.getString("sales_pers__1");
						salespers2 = rs.getString("sales_pers__2") == null ? " " : rs.getString("sales_pers__2");
						deliveryTerm = rs.getString("dlv_term") == null ? " " : rs.getString("dlv_term");
						tranCode = rs.getString("TRAN_CODE") == null ? " " : rs.getString("TRAN_CODE");
						add1 = rs.getString("addr1") == null ? " " : rs.getString("addr1");
						add2 = rs.getString("addr2") == null ? " " : rs.getString("addr2");
						add3 = rs.getString("addr3") == null ? " " : rs.getString("addr3");
						tele1 = rs.getString("tele1") == null ? " " : rs.getString("tele1");
						tele2 = rs.getString("tele2") == null ? " " : rs.getString("tele2");
						tele3 = rs.getString("tele3") == null ? " " : rs.getString("tele3");
						currCode1 = rs.getString("curr_code") == null ? "" : rs.getString("curr_code");
					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select sales_pers,sales_pers__1,sales_pers__2,cr_term from customer_series where cust_code = ? and item_ser = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						salesPersTmp = rs.getString("sales_pers") == null ? " " : rs.getString("sales_pers");
						crTermTmp = rs.getString("cr_term") == null ? " " : rs.getString("cr_term");
						salesPersTmp1 = rs.getString("sales_pers__1") == null ? " " : rs.getString("sales_pers__1");
						salesPersTmp2 = rs.getString("sales_pers__2") == null ? " " : rs.getString("sales_pers__2");

					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select curr_code from finent where fin_entity in (select fin_entity from site where site_code = ? )";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						currCode2 = rs.getString("curr_code") == null ? "" : rs.getString("curr_code");
					}
					if (rs != null)
						rs.close();
					rs = null;
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					
					xmlBuff = new StringBuffer();

					System.out.println("--XML CREATION --");

					xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
					xmlBuff.append("<DocumentRoot>");
					xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
					xmlBuff.append("<group0>");
					xmlBuff.append("<description>").append("Group0 description").append("</description>");
					xmlBuff.append("<Header0>");
					xmlBuff.append("<objName><![CDATA[").append("sorder").append("]]></objName>");
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
					
					xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorder\" objContext=\"1\">");
					xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
					xmlBuff.append("<sale_order/>");
					//xmlBuff.append("<order_type><![CDATA[" + orderType + "]]></order_type>");
					xmlBuff.append("<order_date><![CDATA[" + sysDate + "]]></order_date>");
					xmlBuff.append("<item_ser><![CDATA[" + itemSer.trim() + "]]></item_ser>");
					xmlBuff.append("<site_code><![CDATA[" + siteCode.trim() + "]]></site_code>");
					xmlBuff.append("<site_code__ship><![CDATA[" + siteCode.trim() + "]]></site_code__ship>");
					xmlBuff.append("<cust_code><![CDATA[" + groupCode.trim() + "]]></cust_code>");
					xmlBuff.append("<dlv_to><![CDATA[" + dlvDescr + "]]></dlv_to>");
					xmlBuff.append("<cust_code__bil><![CDATA[" + custCode + "]]></cust_code__bil>");
					xmlBuff.append("<cust_code__dlv><![CDATA[" + custCode + "]]></cust_code__dlv>");
					xmlBuff.append("<order_type><![CDATA[" + orderType + "]]></order_type>"); //changed related to order_type made by Jaffar S on 28-01-19
					xmlBuff.append("<stan_code><![CDATA[" + stanCode.trim() + "]]></stan_code>");
					xmlBuff.append("<STAN_CODE__INIT><![CDATA[" + stanCode.trim() + "]]></STAN_CODE__INIT>");
					xmlBuff.append("<cr_term><![CDATA[" + crTerm + "]]></cr_term>");
					xmlBuff.append("<trans_mode><![CDATA[" + transMode.trim() + "]]></trans_mode>");
					xmlBuff.append("<emp_code__ord><![CDATA[" + loginEmpCode + "]]></emp_code__ord>");
					xmlBuff.append("<curr_code><![CDATA[" + currCode1.trim() + "]]></curr_code>");
					xmlBuff.append("<curr_code__frt><![CDATA[" + currCode1.trim() + "]]></curr_code__frt>");
					xmlBuff.append("<curr_code__ins><![CDATA[" + currCode1.trim() + "]]></curr_code__ins>");
					if (currCode1.equalsIgnoreCase(currCode2))
					{
						exchRateFr = 1.0;
						xmlBuff.append("<exch_rate><![CDATA[" + exchRateFr + "]]></exch_rate>");
						xmlBuff.append("<exch_rate__comm><![CDATA[" + exchRateFr + "]]></exch_rate__comm>");
						xmlBuff.append("<exch_rate__comm_1><![CDATA[" + exchRateFr + "]]></exch_rate__comm_1>");
						xmlBuff.append("<exch_rate__comm_2><![CDATA[" + exchRateFr + "]]></exch_rate__comm_2>");
					} else
					{
						exchRateFr = finCommon.getDailyExchRateSellBuy(currCode1, "", siteCode, sdf.format(tranDate).toString(), "S", conn);
						System.out.println("((((((((( " + exchRateFr + " ))))))))))))");
						xmlBuff.append("<exch_rate><![CDATA[" + exchRateFr + "]]></exch_rate>");
						xmlBuff.append("<exch_rate__comm><![CDATA[" + exchRateFr + "]]></exch_rate__comm>");
						xmlBuff.append("<exch_rate__comm_1><![CDATA[" + exchRateFr + "]]></exch_rate__comm_1>");
						xmlBuff.append("<exch_rate__comm_2><![CDATA[" + exchRateFr + "]]></exch_rate__comm_2>");
					}
					xmlBuff.append("<dlv_city><![CDATA[" + city + "]]></dlv_city>");
					xmlBuff.append("<dlv_pin><![CDATA[" + pin + "]]></dlv_pin>");
					xmlBuff.append("<dlv_add1><![CDATA[" + add1 + "]]></dlv_add1>");
					xmlBuff.append("<dlv_add2><![CDATA[" + add2 + "]]></dlv_add2>");
					xmlBuff.append("<dlv_add3><![CDATA[" + add3 + "]]></dlv_add3>");
					xmlBuff.append("<tel1__dlv><![CDATA[" + tele1 + "]]></tel1__dlv>");
					xmlBuff.append("<tel2__dlv><![CDATA[" + tele2 + "]]></tel2__dlv>");
					xmlBuff.append("<tel3__dlv><![CDATA[" + tele3 + "]]></tel3__dlv>");
					xmlBuff.append("<state_code__dlv><![CDATA[" + stateCode + "]]></state_code__dlv>");
					xmlBuff.append("<count_code__dlv><![CDATA[" + countCode + "]]></count_code__dlv>");
					xmlBuff.append("<tran_id__porcp><![CDATA[" + tranId + "]]></tran_id__porcp>");
					xmlBuff.append("<status_remarks><![CDATA[" + "generate from POB " + tranId + "]]></status_remarks>");
					xmlBuff.append("<chg_user><![CDATA[" + userId + "]]></chg_user>");
					xmlBuff.append("<chg_term><![CDATA[" + termId + "]]></chg_term>");
					xmlBuff.append("<chg_date><![CDATA[" + sysDate + "]]></chg_date>");
					xmlBuff.append("<tran_code><![CDATA[" + tranCode.trim() + "]]></tran_code>");
					xmlBuff.append("<price_list><![CDATA[" + pricelist.trim() + "]]></price_list>");
					xmlBuff.append("<price_list__clg><![CDATA[" + pricelistClg.trim() + "]]></price_list__clg>");
					xmlBuff.append("<sales_pers><![CDATA[" + salespers.trim() + "]]></sales_pers>");
					xmlBuff.append("<sales_pers__1><![CDATA[" + salespers1.trim() + "]]></sales_pers__1>");
					xmlBuff.append("<sales_pers__2><![CDATA[" + salespers1.trim() + "]]></sales_pers__2>");
					xmlBuff.append("<frt_term><![CDATA[" + frtTerm.trim() + "]]></frt_term>");
					xmlBuff.append("<dlv_term><![CDATA[" + deliveryTerm + "]]></dlv_term>");
					xmlBuff.append("</Detail1>");
					
					sql = "select * from pob_det where tran_id=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
				    while( rs.next() )
				    {
						linenoStr =  rs.getString("line_no");	
						itemCode = rs.getString("item_code") == null ? " " : rs.getString("item_code");	
						rate = rs.getString("rate")== null ? " " : rs.getString("rate");
						quantityStr = rs.getString("quantity") == null ? "0" : rs.getString("quantity")  ;
						freeQtyStr = rs.getString("free_qty") == null ? " " : rs.getString("free_qty");
						totQty = rs.getString("tot_qty") == null ? " " : rs.getString("tot_qty");
						discount = rs.getString("discount") == null ? " " : rs.getString("discount");
						netAmt = rs.getString("net_amt") == null ? " " : rs.getString("net_amt");
						schemeCode =   rs.getString("scheme_code") == null ? " " : rs.getString("scheme_code"); //added by rupali on 20/04/2021 to get scheme code from pob detail
						System.out.println("schemeCode is:::::"+schemeCode);
						/*
						if(schemeCode.trim().length() == 0)
						{
							//schemeCode = itemCode;
							itemFlag = "I";
						}
						else
						{
							itemFlag = "B";
						}
						*/
						if(quantityStr != null && quantityStr.trim().length()>0)
						{
							quantity=Double.parseDouble(quantityStr);
						}
						if(freeQtyStr != null && freeQtyStr.trim().length()>0)
						{
							freeQty=Double.parseDouble(freeQtyStr);
						}
						//added by rupali on 20/04/2021 to combine chargeable and free quantity together to insert in sorddet [start]
						if(schemeCode.trim().length() > 0)
						{
							totQuantity = quantity + freeQty;
							lineNo=lineNo+1;
							lineNoStr="";
							lineNoStr = "   "+lineNo;
							lineNoStr = lineNoStr.substring(lineNoStr.length()-3,lineNoStr.length());
							
							xmlBuff.append("<Detail2 dbID='' domID='" + lineNo + "' objName=\"sorder\" objContext=\"2\">");
							xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
							xmlBuff.append("<sale_order/>");
							xmlBuff.append("<remarks><![CDATA["+ "generated from POB transaction "+tranId+"]]></remarks>");
						    xmlBuff.append("<line_no><![CDATA["+lineNoStr+"]]></line_no>");
							xmlBuff.append("<item_code__ord><![CDATA["+ itemCode.trim() +"]]></item_code__ord>");
							xmlBuff.append("<item_code><![CDATA["+ itemCode.trim() +"]]></item_code>");
							xmlBuff.append("<scheme_code><![CDATA["+ schemeCode.trim() +"]]></scheme_code>"); // added by rupali on 20/04/2021
							xmlBuff.append("<rate><![CDATA["+rate +"]]></rate>");
							xmlBuff.append("<quantity><![CDATA["+ totQuantity +"]]></quantity>");
							xmlBuff.append("<quantity__fc><![CDATA["+ totQuantity +"]]></quantity__fc>");
							xmlBuff.append("<net_amt><![CDATA["+ netAmt +"]]></net_amt>");
							xmlBuff.append("<item_ser><![CDATA["+ itemSer.trim()   +"]]></item_ser>");
							xmlBuff.append("<discount><![CDATA["+ 0  +"]]></discount>");
							xmlBuff.append("<item_flg><![CDATA["+ "I" +"]]></item_flg>"); // CAN NOT BE NULL
							//xmlBuff.append("<item_flg><![CDATA["+ itemFlag +"]]></item_flg>");
							xmlBuff.append("<CHG_USER><![CDATA["+ userId +"]]></CHG_USER>");
							xmlBuff.append("<CHG_TERM><![CDATA["+ termId +"]]></CHG_TERM>");
							xmlBuff.append("<chg_date><![CDATA["+ currDate +"]]></chg_date>");
							xmlBuff.append("<nature><![CDATA["+ "C" +"]]></nature>");
							xmlBuff.append("</Detail2>");
						}
						//added by rupali on 20/04/2021 to combine chargeable and free quantity together to insert in sorddet [end]
						else
						{
							if(quantity>0)
							{
								lineNo=lineNo+1;
								lineNoStr="";
								lineNoStr = "   "+lineNo;
								lineNoStr = lineNoStr.substring(lineNoStr.length()-3,lineNoStr.length());
								
								xmlBuff.append("<Detail2 dbID='' domID='" + lineNo + "' objName=\"sorder\" objContext=\"2\">");
								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
								xmlBuff.append("<sale_order/>");
								xmlBuff.append("<remarks><![CDATA["+ "generated from POB transaction "+tranId+"]]></remarks>");
							    xmlBuff.append("<line_no><![CDATA["+lineNoStr+"]]></line_no>");
								xmlBuff.append("<item_code__ord><![CDATA["+ itemCode.trim() +"]]></item_code__ord>");
								xmlBuff.append("<item_code><![CDATA["+ itemCode.trim() +"]]></item_code>");
								xmlBuff.append("<scheme_code><![CDATA["+ schemeCode.trim() +"]]></scheme_code>");
								xmlBuff.append("<rate><![CDATA["+rate +"]]></rate>");
								xmlBuff.append("<quantity><![CDATA["+ quantity +"]]></quantity>");
								xmlBuff.append("<quantity__fc><![CDATA["+ quantity +"]]></quantity__fc>");
								xmlBuff.append("<net_amt><![CDATA["+ netAmt +"]]></net_amt>");
								xmlBuff.append("<item_ser><![CDATA["+ itemSer.trim()   +"]]></item_ser>");
								xmlBuff.append("<discount><![CDATA["+ 0  +"]]></discount>");
								xmlBuff.append("<item_flg><![CDATA["+ "I" +"]]></item_flg>"); // CAN NOT BE NULL
								//xmlBuff.append("<item_flg><![CDATA["+ itemFlag +"]]></item_flg>");
								xmlBuff.append("<CHG_USER><![CDATA["+ userId +"]]></CHG_USER>");
								xmlBuff.append("<CHG_TERM><![CDATA["+ termId +"]]></CHG_TERM>");
								xmlBuff.append("<chg_date><![CDATA["+ currDate +"]]></chg_date>");
								xmlBuff.append("<nature><![CDATA["+ "C" +"]]></nature>");
								xmlBuff.append("</Detail2>");
							}
							if(freeQty>0)
							{
								lineNo=lineNo+1;
								lineNoStr="";
								lineNoStr = "   "+lineNo;
								lineNoStr = lineNoStr.substring(lineNoStr.length()-3,lineNoStr.length());
								
								xmlBuff.append("<Detail2 dbID='' domID='" + lineNo + "' objName=\"sorder\" objContext=\"2\">");
								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
								xmlBuff.append("<sale_order/>");
								xmlBuff.append("<remarks><![CDATA["+ "generated from POB transaction "+tranId+"]]></remarks>");
							    xmlBuff.append("<line_no><![CDATA["+lineNoStr+"]]></line_no>");
								//xmlBuff.append("<item_code__ord><![CDATA["+ itemCode.trim() +"]]></item_code__ord>");
								xmlBuff.append("<item_code><![CDATA["+ itemCode.trim() +"]]></item_code>");
								xmlBuff.append("<item_code__ord><![CDATA["+ itemCode.trim() +"]]></item_code__ord>");
								xmlBuff.append("<rate><![CDATA["+0.0+"]]></rate>");
								xmlBuff.append("<quantity><![CDATA["+ freeQty +"]]></quantity>");
								xmlBuff.append("<quantity__fc><![CDATA["+ freeQty +"]]></quantity__fc>");
								xmlBuff.append("<net_amt><![CDATA["+ netAmt +"]]></net_amt>");
								xmlBuff.append("<item_ser><![CDATA["+ itemSer.trim()   +"]]></item_ser>");
								xmlBuff.append("<discount><![CDATA["+ 0  +"]]></discount>");
								xmlBuff.append("<item_flg><![CDATA["+ "I" +"]]></item_flg>"); // CAN NOT BE NULL
								//xmlBuff.append("<item_flg><![CDATA["+ itemFlag +"]]></item_flg>");
								xmlBuff.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
								xmlBuff.append("<chg_term><![CDATA["+ termId +"]]></chg_term>");
								xmlBuff.append("<chg_date><![CDATA["+ currDate +"]]></chg_date>");
								xmlBuff.append("<nature><![CDATA["+ "F" +"]]></nature>");
								xmlBuff.append("</Detail2>");
							}
						}
				    }
				    rs.close();
					rs =null;
					pstmt.close();
					pstmt = null;
					xmlBuff.append("</Header0>");
					xmlBuff.append("</group0>");
					xmlBuff.append("</DocumentRoot>");
					xmlString = xmlBuff.toString();
					System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
					System.out.println("...............just before savdata()");
					siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					System.out.println("== site code =="+siteCode);
					retString = saveData(siteCode,xmlString,userId,conn);
					System.out.println("@@@@@2: retString:"+retString);
					System.out.println("--retString finished--");
					
					if (retString.indexOf("Success") > -1)
					{
						//wf_status and status_date added in update sql by sagar on 06/10/2015
						sql = " update pob_hdr set confirmed = 'Y', conf_date = ?, emp_code__aprv = ?, wf_status='C', status_date= ? where tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, currDate);
						pstmt.setString(2, loginEmpCode);
						pstmt.setTimestamp(3, currDate); 
						pstmt.setString(4, tranId);
						
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@@@ cnt...[" + cnt + "]");
						if (cnt>0) 
						{	
							conn.commit();
							errString = itmDBAccessLocal.getErrorString("","VTCNFSUCC","","",conn);
						}else
						{	
							errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);
						}
					}
				} else
				{
					errString = itmDBAccessLocal.getErrorString("", "VTINVCONF2", "","",conn);
				}
			}


			// end if errstrng
		} catch (Exception e) 
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{		
			try
			{
				if(conn != null && !conn.isClosed())
				{
					conn.close();
					conn = null;
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
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
			System.out.println("--retString - -"+retString);
		}
		catch(ITMException itme)
		{
			System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}
	
	// Code added by sagar on 06/10/15 for workflow, Start
	// Added confirm() method for Submit the Workflow Status and workflow will initialize On Submit button 
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		System.out.println(">>>>>>>>>>>POBWizConf called for submit>>>>>>>>>>>>");
		String sql = "",wfStatus="",confirmed="";
		Connection conn = null;
		PreparedStatement pstmt = null;
		String errString = null;
		ResultSet rs = null;
		int updCount = 0;
		boolean isError= false;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
            System.out.println(">>>tranId:"+tranId);
            
			if (tranId != null && tranId.trim().length() > 0)
			{
				System.out.println(">>>tranId:"+tranId);
				sql = "	select wf_status, confirmed from pob_hdr where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					wfStatus = rs.getString("wf_status");
					confirmed = rs.getString("confirmed");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println(">>>Check wfStatus:"+wfStatus);
				if ("S".equalsIgnoreCase(wfStatus))
				{
					errString =  itmDBAccessLocal.getErrorString("","VTINVSUB2","","",conn);
					isError= true;
				}
				else if ("Y".equalsIgnoreCase(confirmed))
				{
					errString =  itmDBAccessLocal.getErrorString("","VTINVCONF2","","",conn);
					isError= true;
				} 
				else
				{
					 System.out.println(">>>>Before Calling method invokeWorkflow tranId:"+tranId);
					 errString = invokeWorkflow(conn,tranId, xtraParams, forcedFlag);
					 System.out.println(">>>>From invokeWorkflow errString:"+errString);
					
					 if("success".equalsIgnoreCase(errString))
					 {	
						 sql = " update pob_hdr set wf_status = 'S' where tran_id = ? ";
    					 pstmt = conn.prepareStatement(sql);
    					 pstmt.setString(1, tranId);
    					 updCount = pstmt.executeUpdate();
    					 pstmt.close();
    					 pstmt = null;
    					 System.out.println(">>>After Workflow init Success updCount:"+updCount);
    					 if (updCount > 0)
    					 {
    						 errString = itmDBAccessLocal.getErrorString("", "VTPOBSUBM", "","",conn);
    					 }
    					 else
    					 {
    						 errString = itmDBAccessLocal.getErrorString("", "SUBMITFAIL", "","",conn);
    						 isError=true;
    					 }
					 }
					 else
					 {
						 errString = itmDBAccessLocal.getErrorString("", "SUBMITFAIL", "","",conn);
						 isError=true;
					 }
			    }
			}
			// end if errstrng
		}
		catch (Exception e)
		{
			if(conn!=null)
			{
				isError=true;
				try 
				{
					conn.rollback();
				} 
				catch (SQLException ex) 
				{
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				System.out.println(">>>Before Closing Connection in POBWizConf finally");
				//if(conn != null && !conn.isClosed())
				System.out.println("In finally Check isError:"+isError);
				if(!isError)
				{
					conn.commit();
					System.out.println(">>>In if commit successfuly");
				}
				else
				{
					conn.rollback();
					System.out.println(">>>In else rollback successfuly");
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
					System.out.println(">>>Close Connection Successfuly in confirm()");
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
			catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
			
	} //end confirm method
	
	//Added invokeWorkflow() method by sagar on 28/09/15 for POB Transaction Workflow Initiation 
	private String invokeWorkflow(Connection conn , String tranId, String xtraParams,String hdrEdiFlag)throws ITMException
	{
		String winName = "w_pob_view";
		//GenericUtility genericUtility = null;
		Document domAll = null;
		NodeList nodeList = null;
		Node node = null;
		Element nodeElement = null;
		String sql="";
		PreparedStatement pStmt =null;
		ResultSet rs= null;
		String retString = "";
		String objName = "pob_view";
		String wrkflwInit = "";
		String refSer = "";
		String nodeName = "";
		
		try
		{
			System.out.println(">>>>invokeWorkflow method");

			XML2DBEJB xml2dbObj = new XML2DBEJB(); 
			//genericUtility = GenericUtility.getInstance();
			GenerateXmlFromDB generateXmlFromDB = new GenerateXmlFromDB();
			String retXml = generateXmlFromDB.getXMLData(winName, tranId, conn , true);
			System.out.println(">>>In invokeWorkflow retXml:"+retXml);
		    retXml = retXml.replace("<Root>", "");
		    retXml = retXml.replace("</Root>", "");
		   if (retXml != null && retXml.trim().length() > 0 )
		   {
				domAll = genericUtility.parseString(retXml);
		   }
			nodeList  = domAll.getElementsByTagName("Detail1");
			node = nodeList.item(0);
			if(node != null)
			{
				objName = node.getAttributes().getNamedItem("objName").getNodeValue();
				nodeList = node.getChildNodes();
				int nodeListLength = nodeList.getLength();
				for (int i = 0;i < nodeListLength; i++ )
				{
					node = nodeList.item(i);
					if(node != null)
					{
						nodeName = node.getNodeName();
					}
					
					if ("wf_status".equalsIgnoreCase(nodeName)) 
					{
						if (node.getFirstChild() != null) 
						{
							node.getFirstChild().setNodeValue("S");
							
						} 
						else 
						{
							nodeElement = (Element) node;
							nodeElement.appendChild(domAll.createCDATASection("S"));
						}  
					}
				}
			}
		    nodeList  = domAll.getElementsByTagName("DocumentRoot");
			node = nodeList.item(0);
		
			sql = "SELECT WRKFLW_INIT,REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = 'w_"+objName+"'";
			pStmt = conn.prepareStatement(sql);
			rs=  pStmt.executeQuery();
			if(rs.next())
			{
				wrkflwInit = rs.getString("WRKFLW_INIT") == null ? "" : rs.getString("WRKFLW_INIT");
				refSer = rs.getString("REF_SER") == null ? "" : rs.getString("REF_SER");
			}
			if(rs !=null)
			{
				rs.close();
				rs=null;
			}
			if(pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			String entityCodeInit = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			//Commented & changed to method with connection by Jaffar S. on 13-11-18 [Start]
			//retString = xml2dbObj.invokeWorkflowExternal(domAll, entityCodeInit, wrkflwInit, objName, refSer, tranId);
			retString = xml2dbObj.invokeWorkflowExternal(domAll, entityCodeInit, wrkflwInit, objName, refSer, tranId, conn);
			//Commented & changed to method with connection by Jaffar S. on 13-11-18 [End]
			System.out.println(">>>retString From xml2dbObj.invokeWorkflowExternal:"+retString);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(rs !=null)
    			{
    				rs.close();
    				rs=null;
    			}
    			if(pStmt != null)
    			{
    				pStmt.close();
    				pStmt=null;
    			}
			}
			catch(SQLException sqlEx)
			{
				System.out.println("Exception in Finally "+sqlEx.getMessage());
				sqlEx.printStackTrace();
			}
		}
		return "success";
			
	} //end invokeWorkflow method
	// Code added by sagar on 06/10/15 for workflow, End
}