package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;
import java.sql.*;
import java.text.SimpleDateFormat;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Stateless
public class SordBulkConf extends ActionHandlerEJB implements SordBulkConfLocal, SordBulkConfRemote
{
E12GenericUtility genericUtility= new  E12GenericUtility();
public String confirm(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException
{
	System.out.println(">>>>confirm called");
	String confirmed = "";
	String sql = "",sql1="",sql2="",sql3="";
	Connection conn = null;
	PreparedStatement pstmt = null;
	PreparedStatement pstmt1 = null;
	PreparedStatement pstmt2 = null;
	PreparedStatement pstmt3 = null;
    String errString = null;
	ResultSet rs = null;
	ResultSet rs1 = null;
	ResultSet rs2 = null;
	ResultSet rs3 = null;
	System.out.println("forcedFlag["+forcedFlag+"]");
	String loginEmpCode="",siteCode="",sordbulkNo="";
	String loginSiteCode="",custCode="";
	String itemSer="",orderType="",retailerCode="";
	String pricelist="",pricelistClg="",crTerm="",custCodeBil="",salespers="",salespers1="",salespers2="",frtTerm="";
	String groupCode="",salesPersTmp="",crTermTmp="",salesPersTmp1="",salesPersTmp2="",transMode="";
	String deliveryTerm="",tranCode="",add1="",add2="",add3="",tele1="",tele2="",tele3="";
	String stanCode="",city="",countCode="",pin="",stateCode="",dlvDescr="";
	String userId="",termId="";	
	String currCode1="",currCode2="",discount="";
	String linenoStr="",itemCode="",quantityStr="";
	
	String linedetnew=null,quantitydetNew=null;
	StringBuffer xmlBuff = null;
	String xmlString = null,retString  = null;
	String sysDate="";
    int cnt = 0,lineNo=0;
	double exchRateFr=0;
	double quantity=0;
	Timestamp currDate = null;
	Timestamp tranDate = null;
	ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String custCodeDet="",unit="",itemDescr="";;
	FinCommon finCommon = null;
	try 
	{
		ConnDriver connDriver = null;
		connDriver = new ConnDriver();
		//conn = connDriver.getConnectDB("DriverITM");
		conn = getConnection();
		conn.setAutoCommit(false);
		finCommon = new FinCommon();
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  
		termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
		loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
		SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
		currDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
		Calendar currentDate = Calendar.getInstance();
		System.out.println("currDate>>>>>>"+currDate);
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		sysDate = sdf.format(currentDate.getTime());
		System.out.println("Now the date is :=>  " + sysDate);
		String unitRate="" ,packCode="",packinstr ="",itemCodeBulkDet="";
		HashMap splitCodeWiseMap = new HashMap();
		//ArrayList tempList = null;
		HashMap tempMap = null;	
		ArrayList detailList=new ArrayList();
		ArrayList sorderbulklList = new ArrayList();
		if (tranId != null && tranId.trim().length() > 0) 
		{
			System.out.println("tranId"+tranId+"]");
			
			HashMap<String,ArrayList<HashMap<String,String>>> custCodeWiseHMap= new HashMap();
			sql ="select sord_bulk.tran_id,sord_bulk.tran_date,sord_bulk.site_code,sord_bulk.item_ser,"
					+ "sord_bulk_det.line_no,sord_bulk_det.quantity, " +
				 "sord_bulk.order_type,sord_bulk_det.cust_code,sord_bulk.confirmed,sord_bulk_det.item_code  from  sord_bulk , sord_bulk_det where sord_bulk.tran_id=sord_bulk_det.tran_id and sord_bulk.tran_id = ? order by sord_bulk_det.cust_code,sord_bulk_det.item_code";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{

				sordbulkNo = rs.getString("tran_id");
				tranDate	= rs.getTimestamp("tran_date");
				siteCode=checkNull(rs.getString("site_code"));
				itemSer=checkNull(rs.getString("item_ser"));		
				orderType=checkNull(rs.getString("order_type"));
				custCode=checkNull(rs.getString("cust_code"));
				confirmed=checkNull(rs.getString("confirmed"));
				itemCodeBulkDet=checkNull(rs.getString("item_code"));
				linedetnew=checkNull(rs.getString("line_no"));
				quantitydetNew=checkNull(rs.getString("quantity"));
				
				if("N".equalsIgnoreCase(confirmed))
				{
					
					sql1 = "select price_list, price_list__clg from site_customer where site_code = ? and cust_code = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, siteCode);
					pstmt1.setString(2, custCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						pricelist = rs1.getString("price_list");
						pricelistClg = rs1.getString("price_list__clg");
					}

					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
					
					sql1 = "select cr_term,stan_code ,city,count_code,pin,state_code,frt_term,curr_code," + 
							"cust_name,group_code,trans_mode,cust_code__bil,price_list,price_list__clg," +
							"sales_pers,sales_pers__1,sales_pers__2,dlv_term, TRAN_CODE, addr1,addr2,addr3,tele1,tele2,tele3 " +
							" from customer where cust_code = ?  ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, custCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						crTerm = rs1.getString("cr_term") == null ? " " : rs1.getString("cr_term");
						stanCode = rs1.getString("stan_code") == null ? " " : rs1.getString("stan_code");
						city = rs1.getString("city") == null ? " " : rs1.getString("city");
						countCode = rs1.getString("count_code") == null ? " " : rs1.getString("count_code");
						pin = rs1.getString("pin") == null ? " " : rs1.getString("pin");
						stateCode = rs1.getString("state_code") == null ? " " : rs1.getString("state_code");
						frtTerm = rs1.getString("frt_term") == null ? " " : rs1.getString("frt_term");
						dlvDescr = rs1.getString("cust_name");
						groupCode = rs1.getString("group_code") == null ? " " : rs1.getString("group_code");
						transMode = rs1.getString("trans_mode") == null ? " " : rs1.getString("trans_mode");
						custCodeBil = rs1.getString("cust_code__bil") == null ? " " : rs1.getString("cust_code__bil");
						pricelist = rs1.getString("price_list") == null ? " " : rs1.getString("price_list");
						pricelistClg = rs1.getString("price_list__clg") == null ? " " : rs1.getString("price_list__clg");
						salespers = rs1.getString("sales_pers") == null ? " " : rs1.getString("sales_pers");
						salespers1 = rs1.getString("sales_pers__1") == null ? " " : rs1.getString("sales_pers__1");
						salespers2 = rs1.getString("sales_pers__2") == null ? " " : rs1.getString("sales_pers__2");
						deliveryTerm = rs1.getString("dlv_term") == null ? " " : rs1.getString("dlv_term");
						tranCode = rs1.getString("TRAN_CODE") == null ? " " : rs1.getString("TRAN_CODE");
						add1 = rs1.getString("addr1") == null ? " " : rs1.getString("addr1");
						add2 = rs1.getString("addr2") == null ? " " : rs1.getString("addr2");
						add3 = rs1.getString("addr3") == null ? " " : rs1.getString("addr3");
						tele1 = rs1.getString("tele1") == null ? " " : rs1.getString("tele1");
						tele2 = rs1.getString("tele2") == null ? " " : rs1.getString("tele2");
						tele3 = rs1.getString("tele3") == null ? " " : rs1.getString("tele3");
						currCode1 = rs1.getString("curr_code") == null ? "" : rs1.getString("curr_code");
					}

					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					sql1 = "select sales_pers,sales_pers__1,sales_pers__2,cr_term from customer_series where cust_code = ? and item_ser = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, custCode);
					pstmt1.setString(2, itemSer);
					rs1= pstmt1.executeQuery();
					if (rs1.next())
					{
						salesPersTmp = rs1.getString("sales_pers") == null ? " " : rs1.getString("sales_pers");
						crTermTmp = rs1.getString("cr_term") == null ? " " : rs1.getString("cr_term");
						salesPersTmp1 = rs1.getString("sales_pers__1") == null ? " " : rs1.getString("sales_pers__1");
						salesPersTmp2 = rs1.getString("sales_pers__2") == null ? " " : rs1.getString("sales_pers__2");

					}

					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					sql1 = "select curr_code from finent where fin_entity in (select fin_entity from site where site_code = ? )";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, siteCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						currCode2 = rs1.getString("curr_code") == null ? "" : rs1.getString("curr_code");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					tempMap = new HashMap();
					tempMap.put("order_type", orderType);
					tempMap.put("order_date", sysDate);
					tempMap.put("item_ser", itemSer.trim());
					tempMap.put("site_code", siteCode.trim());
					tempMap.put("site_code__ship", siteCode.trim());
					//tempMap.put("cust_code",groupCode);
					tempMap.put("dlv_to", dlvDescr);
					tempMap.put("cust_code__bil", custCode);
					tempMap.put("cust_code__dlv", custCode);
					tempMap.put("stan_code", stanCode.trim());
					tempMap.put("STAN_CODE__INIT", stanCode.trim());
					tempMap.put("cr_term", crTerm);
					tempMap.put("trans_mode", transMode.trim());
					tempMap.put("emp_code__ord", loginEmpCode);
					tempMap.put("curr_code", currCode1.trim());
					tempMap.put("curr_code__frt", currCode1.trim());
					tempMap.put("curr_code__ins", currCode1.trim());
					if (currCode1.equalsIgnoreCase(currCode2))
					{
						exchRateFr = 1.0;
						tempMap.put("exch_rate",exchRateFr);
						tempMap.put("exch_rate__comm",exchRateFr);
						tempMap.put("exch_rate__comm_1",exchRateFr);
						tempMap.put("exch_rate__comm_2",exchRateFr);
						
					} else
					{
						exchRateFr = finCommon.getDailyExchRateSellBuy(currCode1, "", siteCode, sdf.format(tranDate).toString(), "S", conn);
						System.out.println("((((((((( " + exchRateFr + " ))))))))))))");
						tempMap.put("exch_rate",exchRateFr);
						tempMap.put("exch_rate__comm",exchRateFr);
						tempMap.put("exch_rate__comm_1",exchRateFr);
						tempMap.put("exch_rate__comm_2",exchRateFr);
					}
					tempMap.put("dlv_city",city);
					tempMap.put("dlv_pin",pin);
					tempMap.put("dlv_add1",add1);
					tempMap.put("dlv_add2",add2);
					tempMap.put("dlv_add3",add3);
					tempMap.put("tel1__dlv",tele1);
					tempMap.put("tel2__dlv",tele2);
					tempMap.put("tel3__dlv",tele3);
					tempMap.put("state_code__dlv",stateCode);
					tempMap.put("count_code__dlv",countCode);
					String temp="generate from Upload Utility " + tranId;
					tempMap.put("tran_id__porcp",tranId);
					tempMap.put("status_remarks",temp);
					tempMap.put("chg_user",userId);
					tempMap.put("chg_term",termId);
					tempMap.put("chg_date",sysDate);
					tempMap.put("tran_code",tranCode.trim());
					tempMap.put("price_list",pricelist.trim());
					tempMap.put("price_list__clg",pricelist.trim());
					tempMap.put("sales_pers",salespers.trim());
					tempMap.put("sales_pers__1",salespers1.trim());
					tempMap.put("sales_pers__2",salespers1.trim());
					tempMap.put("frt_term",frtTerm.trim());
					tempMap.put("dlv_term",deliveryTerm);
					
			    	/*sql="select * from sord_bulk_det where item_code= ? and cust_code= ?";
			    	pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,itemCodeBulkDet);	
					pstmt1.setString(2,custCode);
					rs1 = pstmt1.executeQuery();
					if( rs1.next() )*/
					/*{
					linenoStr =  rs1.getString("line_no");	
					itemCode = rs1.getString("item_code") == null ? " " : rs1.getString("item_code");	
					quantityStr = rs1.getString("quantity") == null ? "0" : rs1.getString("quantity")  ;
					custCodeDet = rs1.getString("cust_code") == null ? " " : rs1.getString("cust_code")  */;
					//System.out.println("item_code["+itemCode+"]quantity["+quantityStr+"]custCodeDet["+custCodeDet+"]");
					
					linenoStr=linedetnew;
					itemCode=itemCodeBulkDet;
					quantityStr=quantitydetNew;
					custCodeDet=custCode;
					if(quantityStr != null && quantityStr.trim().length()>0)
					{
							quantity=Double.parseDouble(quantityStr);
					}
					System.out.println("item_code["+itemCode+"]quantity["+quantityStr+"]custCodeDet["+custCodeDet+"]");
					//}
					/*rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;*/
					sql2="select unit from item where item_code= ?";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, itemCodeBulkDet);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						unit = rs2.getString("unit") == null ? "" : rs2.getString("unit");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					System.out.println("Unit is ["+unit+"]");
					
					//tempdetail=new HashMap();
					tempMap.put("line_no",linenoStr);
					tempMap.put("item_code",itemCode);
					tempMap.put("cust_code",custCodeDet);
					tempMap.put("quantity",quantity);
					tempMap.put("unit",unit);
					tempMap.put("item_ser",itemSer);
					//detailList.add(tempdetail);
				    //tempMap.put("cust_code",custCode);
					//tempMap.put("detailList", detailList);
					//tempList.add(tempMap);
					if(custCodeWiseHMap.containsKey(custCodeDet))
					{
						System.out.println("Match List ");
						ArrayList tempList=custCodeWiseHMap.get(custCodeDet);
						tempList.add(tempMap);
						custCodeWiseHMap.put(custCodeDet, tempList);
					}
					else
					{
						System.out.println("New List ");
						sorderbulklList=new ArrayList();
						sorderbulklList.add(tempMap);
						custCodeWiseHMap.put(custCodeDet, sorderbulklList);
					}
					
					System.out.println("@@@@@@@@@@@@@@@@ siteCodeWiseHMap1["+custCodeWiseHMap+"]");
					System.out.println("@@@@@@@@@@@@@@@@  Size[List1]["+custCodeWiseHMap.size()+"]");
				}
				else
				{
					errString = itmDBAccessLocal.getErrorString("", "VTINVCONF2", "","",conn);
				}
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("--XML CREATION --");
			System.out.println("--Records Details--"+sorderbulklList);
			System.out.println("--Records --"+sorderbulklList.size());
			
			if(custCodeWiseHMap.size()>0)
			{
				Set <String> custCodekeys=custCodeWiseHMap.keySet();
				System.out.println("custCodekeys["+custCodekeys+"]");
				for(String eachCustCode : custCodekeys)
				{
					System.out.println("eachCustCode["+eachCustCode+"]");
					System.out.println("custCodekeys["+custCodekeys+"]");
					xmlBuff = new StringBuffer();
					xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
					xmlBuff.append("<DocumentRoot>");
					xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
					xmlBuff.append("<group0>");
					xmlBuff.append("<description>").append("Group0 description").append("</description>");
					xmlBuff.append("<Header0>");
					xmlBuff.append("<objName><![CDATA[").append("sorder_bulk").append("]]></objName>");
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
					
					xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorder_bulk\" objContext=\"1\">");
					xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
					xmlBuff.append("<sale_order/>");
					xmlBuff.append("<order_type><![CDATA[" + orderType + "]]></order_type>");
					xmlBuff.append("<order_date><![CDATA[" + sysDate + "]]></order_date>");
					xmlBuff.append("<item_ser><![CDATA[" + itemSer.trim() + "]]></item_ser>");
					xmlBuff.append("<site_code><![CDATA[" + siteCode.trim() + "]]></site_code>");
					xmlBuff.append("<site_code__ship><![CDATA[" + siteCode.trim() + "]]></site_code__ship>");
					//xmlBuff.append("<cust_code><![CDATA[" + groupCode.trim() + "]]></cust_code>");
					xmlBuff.append("<cust_code><![CDATA[" + eachCustCode + "]]></cust_code>");
					xmlBuff.append("<dlv_to><![CDATA[" + dlvDescr + "]]></dlv_to>");
					//xmlBuff.append("<cust_code__bil><![CDATA[" + custCode + "]]></cust_code__bil>");
					//xmlBuff.append("<cust_code__dlv><![CDATA[" + custCode + "]]></cust_code__dlv>");
					xmlBuff.append("<cust_code__bil><![CDATA[" + eachCustCode + "]]></cust_code__bil>");
					xmlBuff.append("<cust_code__dlv><![CDATA[" + eachCustCode + "]]></cust_code__dlv>");
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
					ArrayList detailDataList=custCodeWiseHMap.get(eachCustCode);
					System.out.println("detailDataList["+detailDataList+"]");
					System.out.println("detailDataList size["+detailDataList.size()+"]");
					int lilineno1=1;
			        for(int n = 0;n < detailDataList.size();n++)
			        {
			        	 String itemSerMap="",unitMap="",itemCodeMap="",refIdDet="",refIdAdvDet="",taxClassDet="",taxChapDet="",taxEnvDet="";
			        	 double quantityMap=0,lctaxdet=0;
			        	 Timestamp refDadteDet=null;
			        	 SimpleDateFormat sdftemp  = null;
			        	 HashMap payDetTranMap = (HashMap)detailDataList.get(n);
			        	 System.out.println("lilineno1["+lilineno1+"]");
			        	 itemCodeMap =(String)payDetTranMap.get("item_code");	
			        	 itemSerMap = (String)payDetTranMap.get("item_ser"); 
			        	quantityMap 	= ((Double)payDetTranMap.get("quantity")).doubleValue();
			        	unitMap = (String)payDetTranMap.get("unit"); 
			        	System.out.println("itemCode["+itemCode+"]itemSerMap["+itemSerMap+"]quantityMap["+quantityMap+"]");
			        	
						xmlBuff.append("<Detail2 dbID='' domID='" + lineNo + "' objName=\"sorder_bulk\" objContext=\"2\">");
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
						xmlBuff.append("<sale_order/>");
						xmlBuff.append("<remarks><![CDATA["+ "generated from POB transaction "+tranId+"]]></remarks>");
					    xmlBuff.append("<line_no><![CDATA["+lilineno1+"]]></line_no>");
						xmlBuff.append("<site_code><![CDATA[" + siteCode.trim() + "]]></site_code>");
					    xmlBuff.append("<quantity isSrvCallOnChg='0'><![CDATA["+ quantityMap +"]]></quantity>");
						xmlBuff.append("<item_code__ord isSrvCallOnChg='0'><![CDATA["+ itemCodeMap.trim() +"]]></item_code__ord>");
						xmlBuff.append("<item_code isSrvCallOnChg='0'><![CDATA["+ itemCodeMap.trim() +"]]></item_code>");
						xmlBuff.append("<rate isSrvCallOnChg='0'><![CDATA["+0 +"]]></rate>");
						xmlBuff.append("<quantity isSrvCallOnChg='0'><![CDATA["+ quantityMap +"]]></quantity>");
						xmlBuff.append("<quantity__fc isSrvCallOnChg='0'><![CDATA["+ quantityMap +"]]></quantity__fc>");
						xmlBuff.append("<item_ser isSrvCallOnChg='0'><![CDATA["+ itemSerMap+"]]></item_ser>");
						xmlBuff.append("<discount isSrvCallOnChg='0'><![CDATA["+ 0  +"]]></discount>");
						xmlBuff.append("<rate><![CDATA["+ 0 +"]]></rate>");
						xmlBuff.append("<item_flg><![CDATA["+ "I" +"]]></item_flg>"); 
						xmlBuff.append("<item_ser__prom><![CDATA["+ itemSerMap.trim()   +"]]></item_ser__prom>");
						xmlBuff.append("<quantity__stduom><![CDATA["+ quantityMap +"]]></quantity__stduom>");
						xmlBuff.append("<unit><![CDATA["+ unitMap +"]]></unit>");
						xmlBuff.append("<unit__std><![CDATA["+unitMap +"]]></unit__std>");
						//conv__rtuom_stduom
						xmlBuff.append("<conv__qty_stduom><![CDATA["+1 +"]]></conv__qty_stduom>");
						xmlBuff.append("<conv__rtuom_stduom><![CDATA["+1 +"]]></conv__rtuom_stduom>");
						sql2 = "select unit__rate, pack_code,pack_instr,descr from item where item_code = ?";
						pstmt3= conn.prepareStatement(sql2);
						pstmt3.setString(1,itemCodeMap );
						rs3 = pstmt3.executeQuery();
						if(rs3.next())
						{
							unitRate = checkNull(rs3.getString("unit__rate"));
							packCode = rs3.getString("pack_code");
							packinstr = rs3.getString("pack_instr");
							itemDescr=rs3.getString("descr");
							
							xmlBuff.append("<unit__rate><![CDATA["+unitRate +"]]></unit__rate>");
							xmlBuff.append("<pack_code><![CDATA["+packCode +"]]></pack_code>");
							xmlBuff.append("<pack_instr><![CDATA["+packinstr +"]]></pack_instr>");
							xmlBuff.append("<item_descr><![CDATA["+itemDescr +"]]></item_descr>");
							
						}
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;
						xmlBuff.append("<CHG_USER><![CDATA["+ userId +"]]></CHG_USER>");
						xmlBuff.append("<CHG_TERM><![CDATA["+ termId +"]]></CHG_TERM>");
						xmlBuff.append("<chg_date><![CDATA["+ currDate +"]]></chg_date>");
						xmlBuff.append("<nature><![CDATA["+ "F" +"]]></nature>");
						xmlBuff.append("</Detail2>");
						System.out.println("XML@@@@@@@@@@@@@Deatil2"+xmlBuff);
						lilineno1++;
			        	}
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
					String tranIdPe="";
					if (retString.indexOf("Success") > -1)
					{
						conn.commit();
						System.out.println("Connection Commited");
					}
					if (retString.indexOf("Success") > -1)
					{
						System.out.println(" Generated retString [" + retString + "]");
						String[] arrayForTranId1 = retString.split("<TranID>");

			            System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId1);
			            System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId1[1]);

			            int endIndex1 = arrayForTranId1[1].indexOf("</TranID>");

			            System.out.println("endIndex1:::::::"+endIndex1);

			            tranIdPe = arrayForTranId1[1].substring(0, endIndex1);
			            System.out.println("tranIdPe=====["+tranIdPe+"]");
			            System.out.println("forcedFlag Method["+forcedFlag+"]");
			            retString=confirmSaleorder("sorder",tranIdPe, xtraParams,  forcedFlag);
			            
						/*if ("PRCSTATUS".indexOf("Success") > -1 || retString.indexOf("Success") > -1 )
						{
						sql = "update sord_bulk set confirmed = 'Y', conf_date = ?, emp_code__aprv = ?  where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, currDate);
						pstmt.setString(2, loginEmpCode);
						pstmt.setString(3, tranId);
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("Updae cnt...[" + cnt + "]");
						if (cnt>0) 
						{	
							conn.commit();
							errString = itmDBAccessLocal.getErrorString("","VTCNFSUCC","","",conn);
						}else
						{	
							conn.rollback();
							return retString;
						}
						}
						else
						{	
							conn.rollback();
							return retString;
						}*/
					}
					else
					{
						conn.rollback();
						return retString;
					}
			        
				}// end for loop eachCustCode 

			}//end custCodeWiseHMap map
			else
			{
				retString = itmDBAccessLocal.getErrorString("","NORECFND","","",conn);
			}
			if (retString.indexOf("PRCSTATUS") > -1 || retString.indexOf("Success") > -1 )
			{
			sql = "update sord_bulk set confirmed = 'Y', conf_date = ?, emp_code__aprv = ?  where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, currDate);
			pstmt.setString(2, loginEmpCode);
			pstmt.setString(3, tranId);
			cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			System.out.println("Updae cnt...[" + cnt + "]");
			if (cnt>0) 
			{	
				conn.commit();
				errString = itmDBAccessLocal.getErrorString("","VTCNFSUCC","","",conn);
			}else
			{	
				conn.rollback();
				return retString;
			}
			}
			}
			
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
	//Connection conn=null;
	MasterStatefulLocal masterStateful = null; // for ejb3
	try
	{
		//ConnDriver connDriver = new ConnDriver();
		//conn = getConnection();
		//conn=connDriver.getConnectDB("DriverITM");
		//conn.setAutoCommit(false);
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
		System.out.println("ITMException : Create SORDER BULK :saveData :==>");
		throw itme;
	}
	catch(Exception e)
	{
		e.printStackTrace();
		System.out.println("Exception :Create Sorder Bulk :saveData :==>");
		throw new ITMException(e);
	}
	return retString;
}
public String confirmSaleorder(String businessObj, String tranIdFr,String xtraParams, String forcedFlag) throws ITMException
{
	String methodName = "";
	String compName = "";
	String retString = "";
	String serviceCode = "";
	String serviceURI = "";
	String actionURI = "";
	String sql = "";
	Connection conn1 = null;
	PreparedStatement pStmt = null;
	ResultSet rs = null;
	int cnt = 0;

	try
	{
		ConnDriver connDriver = new ConnDriver();
		conn1 = getConnection();
		//conn1=connDriver.getConnectDB("DriverITM");
		conn1.setAutoCommit(false);
		methodName = "gbf_post";
		actionURI = "http://NvoServiceurl.org/" + methodName;
		forcedFlag="";

		System.out.println("forcedFlag Method["+forcedFlag+"]");
		sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
		pStmt = conn1.prepareStatement(sql);
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
		pStmt = conn1.prepareStatement(sql);
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
		System.out.println("SERVICE_URI["+serviceURI+"]");
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
			if( conn1 != null ){
				conn1.close();
				conn1 = null;
			}
		}
		catch(Exception e)
		{}
	}
	return retString;
}

}