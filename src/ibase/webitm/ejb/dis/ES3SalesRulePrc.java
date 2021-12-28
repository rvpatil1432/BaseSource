package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.io.File;
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
import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.mysql.jdbc.Statement;


@Stateless
public class ES3SalesRulePrc extends ProcessEJB implements ES3SalesRulePrcLocal , ES3SalesRulePrcRemote
{
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("Inside " + this.getClass().getSimpleName() + " getData(S)");
		String retString = "";
		Document dom = null, dom2 = null;
		try {
			E12GenericUtility genericUtility = new E12GenericUtility();
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				dom = genericUtility.parseString(xmlString);
				System.out.println("@S@xmlString in ES3SalesMasterPrc["+xmlString+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) 
			{
				dom2 = genericUtility.parseString(xmlString2);
				System.out.println("@S@xmlString2 in ES3SalesMasterPrc["+xmlString2+"]");
			}
			retString = getData(dom, dom2, windowName, xtraParams);
		} catch (Exception e) 
		{
			System.out.println("Inside Exception [" + this.getClass().getSimpleName() + "][getData(xml)]" + e.getMessage());
			e.printStackTrace();
		}
		return retString;
	}
	
	public String getData(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{
		String resultString = "", sql = "",sql1 = "";
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		int count = 0,cnt=0;
		boolean hasUserSite = false;;
		StringBuffer dataStrBuff = new StringBuffer();
		Connection conn = null;
		String acctPrdDesc ="",paySite="";
		String currDate = "", ruleDescr = "",tranId = "";
		Timestamp tranDate = null,validFrom = null,validTo = null,addDate = null,chgDate = null;
		String active="",carryData="",addTerm="",addUser="",chgTerm="",chgUser="";
		String itemCodeTo="",itemSerTo="",custCodeTo="",stanCodeTo="",applyOnExp="",applyOnExpVal1="",applyOnExpVal2="",itemCodeFrom="",itemSerFrom="";
		String custCodeFrom="",stanCodeFrom="",itemCode="";
		String tranDateStr="",validFromStr="",validToStr="",addDateStr="",chgDateStr="",ruleCode="",sqlInput="";
		String ApplyOn ="",expression="";
		GenericUtility genericUtility = GenericUtility.getInstance();
		String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		String loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
		String[] expressionArray= null;
		System.out.println("@S@ inside getdata loginCode["+loginCode+"][loginEmpCode]["+loginEmpCode+"]");
		try 
		{
			ConnDriver connDriver = new ConnDriver();
			/* conn = connDriver.getConnectDB("Driver"); */
			conn = getConnection();
			tranId = checkNull(genericUtility.getColumnValue("tran_id", dom2));
			tranDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom2));
			ruleCode = checkNull(genericUtility.getColumnValue("rule_code", dom2));
			ruleDescr = checkNull(genericUtility.getColumnValue("descr", dom2));
			System.out.println("@S@in get data ruleDescr["+ruleDescr+"]");
			validFromStr = checkNull(genericUtility.getColumnValue("valid_from", dom2));
			validToStr = checkNull(genericUtility.getColumnValue("valid_to", dom2));
			carryData = checkNull(genericUtility.getColumnValue("carry_data", dom2));
			active = checkNull(genericUtility.getColumnValue("active", dom2));
			chgUser = checkNull(genericUtility.getColumnValue("chg_user", dom2));
			chgDateStr = checkNull(genericUtility.getColumnValue("chg_date", dom2));
			chgTerm = checkNull(genericUtility.getColumnValue("chg_term", dom2));
			addUser = checkNull(genericUtility.getColumnValue("add_user", dom2));
			addDateStr = checkNull(genericUtility.getColumnValue("add_date", dom2));
			addTerm = checkNull(genericUtility.getColumnValue("add_term", dom2));
			itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code_from", dom2));
			itemCodeTo = checkNull(genericUtility.getColumnValue("item_code_to", dom2));
			itemSerFrom = checkNull(genericUtility.getColumnValue("item_ser_from", dom2));
			itemSerTo = checkNull(genericUtility.getColumnValue("item_ser_to", dom2));
			custCodeFrom = checkNull(genericUtility.getColumnValue("cust_code_from", dom2));
			custCodeTo = checkNull(genericUtility.getColumnValue("cust_code_to", dom2));
			stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code_from", dom2));
			stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code_to", dom2));
			applyOnExp = checkNull(genericUtility.getColumnValue("apply_on_exp", dom2));
			applyOnExpVal1 = checkNull(genericUtility.getColumnValue("apply_on_exp_val1", dom2));
			applyOnExpVal2 = checkNull(genericUtility.getColumnValue("apply_on_exp_val2", dom2));
			String objContext = checkNull(genericUtility.getColumnValue("OBJ_CONTEXT", dom2));
			String editFlag = checkNull(genericUtility.getColumnValue("EDIT_FLAG", dom2));
			System.out.println("@S@objContext["+objContext+"][editFlag]["+editFlag+"]");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());		
			tranDate = Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			//validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(validFromStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			/*dataStrBuff.append("<DocumentRoot><description>Datawindow Root</description><group0>"+
					"<description>Group0 description</description><Header0><description>Header0 members</description>");
			dataStrBuff.append("<Detail2>");
			dataStrBuff.append("<attribute  updateFlag='A'/>");
			*/
			
			
			String passRuleCode = ES3SalesRuleIC.ruleCodeInstance;
			System.out.println("passRuleCode >> "+ passRuleCode);
			
			
			
			dataStrBuff = new StringBuffer("<?xml version = \"1.0\"?>");
			dataStrBuff.append("<DocumentRoot>");
			dataStrBuff.append("<description>").append("Datawindow Root").append("</description>");
			dataStrBuff.append("<group0>");
			dataStrBuff.append("<description>").append("Group0 description").append("</description>");
			dataStrBuff.append("<Header0>");
			if(tranId!=null && tranId.trim().length()>0)
			{
				sql = "select count(*) as cnt from es3_rule_det where tran_id = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					cnt = rs.getInt("cnt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				for(int i=1;i<=cnt ;i++)
				{
					sql = "select tran_id,tran_date,rule_code,valid_from,valid_to,carry_data,active,chg_user," +
							"chg_date,chg_term,add_user,add_date,add_term,item_code_from,item_code_to,cust_code_from," +
							"cust_code_to,stan_code_from,stan_code_to,item_ser_from,item_ser_to,apply_on_exp,apply_on_exp_val1,apply_on_exp_val2 " +
							" from es3_rule_hdr where tran_id = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						dataStrBuff.append("<Detail2>");
						dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
						sql1 = "select tran_id,line_no,apply_on,apply_on_from,apply_on_to,apply_on_exp,apply_on_exp_val1,apply_on_exp_val2" +
								" ,valid_from,valid_to from es3_rule_det where tran_id = ?  and line_no = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, tranId);
						pstmt1.setInt(2, i);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							
						//	dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
							dataStrBuff.append("<line_no>").append("<![CDATA[" + checkNull(rs1.getString("line_no")) + "]]>").append("</line_no>\r\n");
							dataStrBuff.append("<apply_on>").append("<![CDATA[" + checkNull(rs1.getString("apply_on")) + "]]>").append("</apply_on>\r\n");
							dataStrBuff.append("<apply_on_from>").append("<![CDATA["+ checkNull(rs1.getString("apply_on_from")) +"]]>").append("</apply_on_from>\r\n");
							dataStrBuff.append("<apply_on_to>").append("<![CDATA["+checkNull(rs1.getString("apply_on_to"))+"]]>").append("</apply_on_to>\r\n");
							/*if(rs1.getString("apply_on_exp")!=null && rs1.getString("apply_on_exp").trim().length()>0)
							{
								dataStrBuff.append("<apply_on_exp protect =\"0\">").append("<![CDATA[" + checkNull(rs1.getString("apply_on_exp")) + "]]>").append("</apply_on_exp>\r\n");
								dataStrBuff.append("<apply_on_exp_val1 protect =\"0\">").append("<![CDATA["+ checkNull(rs1.getString("apply_on_exp_val1")) +"]]>").append("</apply_on_exp_val1>\r\n");
								dataStrBuff.append("<apply_on_exp_val2 protect =\"0\">").append("<![CDATA["+ checkNull(rs1.getString("apply_on_exp_val2")) +"]]>").append("</apply_on_exp_val2>\r\n");

							}
							else
							{
								dataStrBuff.append("<apply_on_exp protect =\"1\">").append("<![CDATA[]]>").append("</apply_on_exp>\r\n");
								dataStrBuff.append("<apply_on_exp_val1 protect =\"1\">").append("<![CDATA[]]>").append("</apply_on_exp_val1>\r\n");
								dataStrBuff.append("<apply_on_exp_val2 protect =\"1\">").append("<![CDATA[]]>").append("</apply_on_exp_val2>\r\n");

							}*/
							dataStrBuff.append("<apply_on_exp>").append("<![CDATA[" + checkNull(rs1.getString("apply_on_exp")) + "]]>").append("</apply_on_exp>\r\n");
							dataStrBuff.append("<apply_on_exp_val1>").append("<![CDATA["+ checkNull(rs1.getString("apply_on_exp_val1")) +"]]>").append("</apply_on_exp_val1>\r\n");
							dataStrBuff.append("<apply_on_exp_val2>").append("<![CDATA["+ checkNull(rs1.getString("apply_on_exp_val2")) +"]]>").append("</apply_on_exp_val2>\r\n");
						//	validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(rs1.getString("valid_from"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat())+" 00:00:00.0");
						//	validTo = Timestamp.valueOf(genericUtility.getValidDateTimeString(rs1.getString("valid_to"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat())+" 00:00:00.0");
							validFrom = rs1.getTimestamp("valid_from");
							validFromStr =	sdf.format(validFrom);
							validTo = rs1.getTimestamp("valid_to");
							validToStr =	sdf.format(validTo);
							System.out.println("@S@>>>["+validFrom+"]validFromStr["+validFromStr+"]validTo["+validTo+"]validToStr["+validToStr+"]");
							dataStrBuff.append("<valid_from>").append("<![CDATA[" + validFromStr+ "]]>").append("</valid_from>\r\n");
							dataStrBuff.append("<valid_to>").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
							

						}
						dataStrBuff.append("<tran_date>").append("<![CDATA[" + rs.getString("tran_date") + "]]>").append("</tran_date>\r\n");
						dataStrBuff.append("<rule_code>").append("<![CDATA[" + checkNull(ruleCode) + "]]>").append("</rule_code>\r\n");
						dataStrBuff.append("<descr>").append("<![CDATA[" + checkNull(ruleDescr) + "]]>").append("</descr>\r\n");
						//dataStrBuff.append("<valid_from>").append("<![CDATA[" + rs.getString("valid_from") + "]]>").append("</valid_from>\r\n");
						//dataStrBuff.append("<valid_to>").append("<![CDATA[" + rs.getString("valid_to") + "]]>").append("</valid_to>\r\n");
						/*dataStrBuff.append("<carry_data>").append("<![CDATA[" + checkNull(rs.getString("carry_data")) + "]]>").append("</carry_data>\r\n");
						dataStrBuff.append("<active>").append("<![CDATA[" + checkNull(rs.getString("active")) + "]]>").append("</active>\r\n");
						*/
						dataStrBuff.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
						dataStrBuff.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
						dataStrBuff.append("<item_code_from>").append("<![CDATA[" + checkNull(rs.getString("item_code_from")) + "]]>").append("</item_code_from>\r\n");
						dataStrBuff.append("<item_code_to>").append("<![CDATA[" + checkNull(rs.getString("item_code_to")) + "]]>").append("</item_code_to>\r\n");
						dataStrBuff.append("<cust_code_from>").append("<![CDATA[" + checkNull(rs.getString("cust_code_from")) + "]]>").append("</cust_code_from>\r\n");
						dataStrBuff.append("<cust_code_to>").append("<![CDATA[" + checkNull(rs.getString("cust_code_to")) + "]]>").append("</cust_code_to>\r\n");
						dataStrBuff.append("<item_ser_from>").append("<![CDATA[" + checkNull(rs.getString("item_ser_from")) + "]]>").append("</item_ser_from>\r\n");
						dataStrBuff.append("<item_ser_to>").append("<![CDATA[" + checkNull(rs.getString("item_ser_to")) + "]]>").append("</item_ser_to>\r\n");
						dataStrBuff.append("<stan_code_from>").append("<![CDATA[" + checkNull(rs.getString("stan_code_from")) + "]]>").append("</stan_code_from>\r\n");
						dataStrBuff.append("<stan_code_to>").append("<![CDATA[" + checkNull(rs.getString("stan_code_to")) + "]]>").append("</stan_code_to>\r\n");
						dataStrBuff.append("<chg_user>").append("<![CDATA[" + checkNull(rs.getString("chg_user")) + "]]>").append("</chg_user>\r\n");
						dataStrBuff.append("<chg_date>").append("<![CDATA[" + rs.getString("chg_date") + "]]>").append("</chg_date>\r\n");
						dataStrBuff.append("<chg_term>").append("<![CDATA[" + checkNull(rs.getString("chg_term")) + "]]>").append("</chg_term>\r\n");
						dataStrBuff.append("<add_user>").append("<![CDATA[" + checkNull(rs.getString("add_user")) + "]]>").append("</add_user>\r\n");
						dataStrBuff.append("<add_date>").append("<![CDATA[" + rs.getString("add_date") + "]]>").append("</add_date>\r\n");
						dataStrBuff.append("<add_term>").append("<![CDATA[" + checkNull(rs.getString("add_term")) + "]]>").append("</add_term>\r\n");
						dataStrBuff.append("</Detail2>");
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
				}
			}
			else
			{
				String query="",ExpMain="",Exp2="" ,ApplyOn2="",ApplyOn3="";
				ArrayList <String>getParaVal = null;
				sql="select b.sql_expr,b.sql_expr1 from tax_balance_grp a ,tax_bal_grp_det b where  a.bal_group=b.bal_group " +
						"and b.ref_ser='C-ES3'  and b.bal_group= ? order by line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ruleCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					query=rs.getString(1);
					ExpMain=rs.getString(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("@S@ >query["+query+"]Exp1["+ExpMain+"]");
				if(ExpMain!=null && ExpMain.trim().length()>0)
				{
				int num1=ExpMain.indexOf('@');
				String ApplyOn1Sub = ExpMain.substring(0, num1);
				String ApplyOn2Sub = ExpMain.substring(num1+1, ExpMain.length());
				int num2 =ApplyOn1Sub.indexOf('[');
				int num3 =ApplyOn2Sub.indexOf('[');
				ApplyOn2 =  ApplyOn1Sub.substring(num2+1, ApplyOn1Sub.length()-1);
				ApplyOn3 =  ApplyOn2Sub.substring(num3+1, ApplyOn2Sub.length()-1);
				System.out.println(ApplyOn1Sub+">>>"+ApplyOn2Sub);
				System.out.println(ApplyOn2+">>>"+ApplyOn3);
				getParaVal = new ArrayList( Arrays.asList(ApplyOn2.split(",")));
				System.out.println("@S@>>"+getParaVal);
				}
				dataStrBuff = dataStrBuff.append(getSqlValueFromTaxBal(query,ApplyOn2,ApplyOn3,getParaVal,conn,dom2, tranId ));
				
				/**
			Commented On 02-MAY-2019 To get data on the bases of External sql and display in Screen 
				if("R0001".equalsIgnoreCase(ruleCode))
				{
					System.out.println("itemCodeFrom"+itemCodeFrom+"itemCodeTo"+itemCodeTo+"itemSerFrom"+itemSerFrom+"itemSerTo"+itemSerTo);

					sql="select count(*) as cnt from item where( item_ser between ? and ? ) and ( item_code between  ? and ? )";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemSerFrom);
					pstmt.setString(2,itemSerTo);
					pstmt.setString(3,itemCodeFrom);
					pstmt.setString(4,itemCodeTo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt("cnt");

					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;

					for (int i = 1; i <cnt; i++)
					{
						int i=0;
						sql="select item_code,item_ser from item where ( item_ser between ? and ? ) and ( item_code between  ? and ? )";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemSerFrom);
						pstmt.setString(2,itemSerTo);
						pstmt.setString(3,itemCodeFrom);
						pstmt.setString(4,itemCodeTo);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							i++;
							itemCode = checkNull(rs.getString("item_code"));
							dataStrBuff.append("<Detail2>");
							dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
							dataStrBuff.append("<line_no>").append("<![CDATA[" + i + "]]>").append("</line_no>\r\n");
							dataStrBuff.append("<apply_on>").append("<![CDATA[item_code]]>").append("</apply_on>\r\n");
							dataStrBuff.append("<apply_on_from>").append("<![CDATA["+itemCode+"]]>").append("</apply_on_from>\r\n");
							dataStrBuff.append("<apply_on_to>").append("<![CDATA["+itemCode+"]]>").append("</apply_on_to>\r\n");
							dataStrBuff.append("<apply_on_exp protect =\"0\">").append("<![CDATA[" + applyOnExp + "]]>").append("</apply_on_exp>\r\n");
							dataStrBuff.append("<apply_on_exp_val2 protect =\"0\">").append("<![CDATA["+applyOnExpVal1+"]]>").append("</apply_on_exp_val2>\r\n");
							dataStrBuff.append("<apply_on_exp_val1 protect =\"0\">").append("<![CDATA["+applyOnExpVal2+"]]>").append("</apply_on_exp_val1>\r\n");
							dataStrBuff.append("<valid_from>").append("<![CDATA[" + validFromStr + "]]>").append("</valid_from>\r\n");
							dataStrBuff.append("<valid_to>").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
							dataStrBuff.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
							dataStrBuff.append("<rule_code>").append("<![CDATA[" + ruleCode + "]]>").append("</rule_code>\r\n");
							dataStrBuff.append("<descr>").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
							dataStrBuff.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
							dataStrBuff.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
							dataStrBuff.append("<item_code_from>").append("<![CDATA[" + itemCodeFrom + "]]>").append("</item_code_from>\r\n");
							dataStrBuff.append("<item_code_to>").append("<![CDATA[" + itemCodeTo + "]]>").append("</item_code_to>\r\n");
							dataStrBuff.append("<cust_code_from>").append("<![CDATA[" + custCodeFrom + "]]>").append("</cust_code_from>\r\n");
							dataStrBuff.append("<cust_code_to>").append("<![CDATA[" + custCodeTo + "]]>").append("</cust_code_to>\r\n");
							dataStrBuff.append("<item_ser_from>").append("<![CDATA[" +checkNull(rs.getString("item_ser")) + "]]>").append("</item_ser_from>\r\n");
							dataStrBuff.append("<item_ser_to>").append("<![CDATA[" + checkNull(rs.getString("item_ser")) + "]]>").append("</item_ser_to>\r\n");
							dataStrBuff.append("<stan_code_from>").append("<![CDATA[" + stanCodeFrom + "]]>").append("</stan_code_from>\r\n");
							dataStrBuff.append("<stan_code_to>").append("<![CDATA[" + stanCodeTo + "]]>").append("</stan_code_to>\r\n");
							dataStrBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
							dataStrBuff.append("<chg_date>").append("<![CDATA[" + chgDateStr + "]]>").append("</chg_date>\r\n");
							dataStrBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>\r\n");
							dataStrBuff.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
							dataStrBuff.append("<add_date>").append("<![CDATA[" + addDateStr + "]]>").append("</add_date>\r\n");
							dataStrBuff.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
							dataStrBuff.append("</Detail2>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
				//	}

				}//R0001 End
				else if("R0002".equalsIgnoreCase(ruleCode) || "R0007".equalsIgnoreCase(ruleCode))
				{

					System.out.println("itemCodeFrom"+itemCodeFrom+"itemCodeTo"+itemCodeTo+"custCodeFrom"+custCodeFrom+"custCodeTo"+custCodeTo);

					sql="select count(*) as cnt from customer_series where  ( item_ser between ? and ? ) and ( cust_code between  ? and ? )";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemSerFrom);
					pstmt.setString(2,itemSerTo);
					pstmt.setString(3,custCodeFrom);
					pstmt.setString(4,custCodeTo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt("cnt");

					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;

					for (int i = 1; i <cnt; i++)
					{
					int i=0;
						sql="select item_ser,cust_code from customer_series where  ( item_ser between ? and ? ) and ( cust_code between  ? and ? )";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemSerFrom);
						pstmt.setString(2,itemSerTo);
						pstmt.setString(3,custCodeFrom);
						pstmt.setString(4,custCodeTo);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							i++;
							dataStrBuff.append("<Detail2>");
							dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
							dataStrBuff.append("<line_no>").append("<![CDATA[" + i + "]]>").append("</line_no>\r\n");
							dataStrBuff.append("<apply_on>").append("<![CDATA[cust_code]]>").append("</apply_on>\r\n");
							dataStrBuff.append("<apply_on_from>").append("<![CDATA["+checkNull(rs.getString("cust_code"))+"]]>").append("</apply_on_from>\r\n");
							dataStrBuff.append("<apply_on_to>").append("<![CDATA["+checkNull(rs.getString("cust_code"))+"]]>").append("</apply_on_to>\r\n");
							dataStrBuff.append("<apply_on_exp protect =\"0\">").append("<![CDATA[" + applyOnExp + "]]>").append("</apply_on_exp>\r\n");
							dataStrBuff.append("<apply_on_exp_val2 protect =\"0\">").append("<![CDATA["+applyOnExpVal1+"]]>").append("</apply_on_exp_val2>\r\n");
							dataStrBuff.append("<apply_on_exp_val1 protect =\"0\">").append("<![CDATA["+applyOnExpVal2+"]]>").append("</apply_on_exp_val1>\r\n");
							dataStrBuff.append("<valid_from>").append("<![CDATA[" + validFromStr + "]]>").append("</valid_from>\r\n");
							dataStrBuff.append("<valid_to>").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
							dataStrBuff.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
							dataStrBuff.append("<rule_code>").append("<![CDATA[" + ruleCode + "]]>").append("</rule_code>\r\n");
							dataStrBuff.append("<descr>").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
							dataStrBuff.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
							dataStrBuff.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
							dataStrBuff.append("<item_code_from>").append("<![CDATA[" + itemCodeFrom + "]]>").append("</item_code_from>\r\n");
							dataStrBuff.append("<item_code_to>").append("<![CDATA[" + itemCodeTo + "]]>").append("</item_code_to>\r\n");
							dataStrBuff.append("<cust_code_from>").append("<![CDATA[" + custCodeFrom + "]]>").append("</cust_code_from>\r\n");
							dataStrBuff.append("<cust_code_to>").append("<![CDATA[" + custCodeTo + "]]>").append("</cust_code_to>\r\n");
							dataStrBuff.append("<item_ser_from>").append("<![CDATA[" +checkNull(rs.getString("item_ser")) + "]]>").append("</item_ser_from>\r\n");
							dataStrBuff.append("<item_ser_to>").append("<![CDATA[" + checkNull(rs.getString("item_ser")) + "]]>").append("</item_ser_to>\r\n");
							dataStrBuff.append("<stan_code_from>").append("<![CDATA[" + stanCodeFrom + "]]>").append("</stan_code_from>\r\n");
							dataStrBuff.append("<stan_code_to>").append("<![CDATA[" + stanCodeTo + "]]>").append("</stan_code_to>\r\n");
							dataStrBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
							dataStrBuff.append("<chg_date>").append("<![CDATA[" + chgDateStr + "]]>").append("</chg_date>\r\n");
							dataStrBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>\r\n");
							dataStrBuff.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
							dataStrBuff.append("<add_date>").append("<![CDATA[" + addDateStr + "]]>").append("</add_date>\r\n");
							dataStrBuff.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
							dataStrBuff.append("</Detail2>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					//}

				
					
				}
				else
				{
					dataStrBuff.append("<Detail2>");
					dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
					dataStrBuff.append("<line_no>").append("<![CDATA[1]]>").append("</line_no>\r\n");
					if("R0003".equalsIgnoreCase(ruleCode) || "R0006".equalsIgnoreCase(ruleCode) || "R0008".equalsIgnoreCase(ruleCode))
					{
					dataStrBuff.append("<apply_on>").append("<![CDATA[cust_code]]>").append("</apply_on>\r\n");
					dataStrBuff.append("<apply_on_from>").append("<![CDATA["+custCodeFrom+"]]>").append("</apply_on_from>\r\n");
					dataStrBuff.append("<apply_on_to>").append("<![CDATA["+custCodeTo+"]]>").append("</apply_on_to>\r\n");
					}
					else if("R0004".equalsIgnoreCase(ruleCode))
					{
						dataStrBuff.append("<apply_on>").append("<![CDATA[item_code]]>").append("</apply_on>\r\n");
						dataStrBuff.append("<apply_on_from>").append("<![CDATA["+itemCodeFrom+"]]>").append("</apply_on_from>\r\n");
						dataStrBuff.append("<apply_on_to>").append("<![CDATA["+itemCodeTo+"]]>").append("</apply_on_to>\r\n");
							
					}
					dataStrBuff.append("<apply_on_exp>").append("<![CDATA[" + applyOnExp + "]]>").append("</apply_on_exp>\r\n");
					dataStrBuff.append("<apply_on_exp_val2>").append("<![CDATA["+applyOnExpVal1+"]]>").append("</apply_on_exp_val2>\r\n");
					dataStrBuff.append("<apply_on_exp_val1>").append("<![CDATA["+applyOnExpVal2+"]]>").append("</apply_on_exp_val1>\r\n");
					dataStrBuff.append("<valid_from>").append("<![CDATA[" + validFromStr + "]]>").append("</valid_from>\r\n");
					dataStrBuff.append("<valid_to>").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
					dataStrBuff.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
					dataStrBuff.append("<rule_code>").append("<![CDATA[" + ruleCode + "]]>").append("</rule_code>\r\n");
					dataStrBuff.append("<descr>").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
					dataStrBuff.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
					dataStrBuff.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
					dataStrBuff.append("<item_code_from>").append("<![CDATA[" + itemCodeFrom + "]]>").append("</item_code_from>\r\n");
					dataStrBuff.append("<item_code_to>").append("<![CDATA[" + itemCodeTo + "]]>").append("</item_code_to>\r\n");
					dataStrBuff.append("<cust_code_from>").append("<![CDATA[" + custCodeFrom + "]]>").append("</cust_code_from>\r\n");
					dataStrBuff.append("<cust_code_to>").append("<![CDATA[" + custCodeTo + "]]>").append("</cust_code_to>\r\n");
					dataStrBuff.append("<item_ser_from>").append("<![CDATA[" +itemSerFrom + "]]>").append("</item_ser_from>\r\n");
					dataStrBuff.append("<item_ser_to>").append("<![CDATA[" + itemSerTo + "]]>").append("</item_ser_to>\r\n");
					dataStrBuff.append("<stan_code_from>").append("<![CDATA[" + stanCodeFrom + "]]>").append("</stan_code_from>\r\n");
					dataStrBuff.append("<stan_code_to>").append("<![CDATA[" + stanCodeTo + "]]>").append("</stan_code_to>\r\n");
					dataStrBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
					dataStrBuff.append("<chg_date>").append("<![CDATA[" + chgDateStr + "]]>").append("</chg_date>\r\n");
					dataStrBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>\r\n");
					dataStrBuff.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
					dataStrBuff.append("<add_date>").append("<![CDATA[" + addDateStr + "]]>").append("</add_date>\r\n");
					dataStrBuff.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
					dataStrBuff.append("</Detail2>");
				
				}

			
					
				sql = "select sql_input from tax_bal_grp_det where bal_group = ? and ref_ser = 'D-ES3' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ruleCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					sqlInput = rs.getString("sql_input");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(sqlInput!=null && sqlInput.trim().length()>0)
				{
					int ApplyOnIndex = sqlInput.indexOf('@');
					if(ApplyOnIndex==-1)
					{
						int seperatorIndex = sqlInput.indexOf('[');
						ApplyOn = sqlInput.substring(seperatorIndex+1, sqlInput.length()-1);
						System.out.println(">> inside ifApplyOn ["+ApplyOn+"]");
					}
					else
					{
						ApplyOn = sqlInput.substring(0, ApplyOnIndex);
						int seperatorIndex = ApplyOn.indexOf('[');
						ApplyOn = ApplyOn.substring(seperatorIndex+1, ApplyOn.length()-1);
						System.out.println(">> inside else ApplyOn ["+ApplyOn+"]");
						
						expression = sqlInput.substring(ApplyOnIndex + 1, sqlInput.length());
						int seperatorIndex1 = expression.indexOf('[');
						expression = expression.substring(seperatorIndex1+1, expression.length()-1);
						System.out.println(">> inside else expression [" + expression+"]");
					}
					//Checking where expression will be required
					if(expression!= null && expression.trim().length()>0)
					{
						 expressionArray = expression.split(",");
						 System.out.println("@S@ expressionArray["+expressionArray+"]");
					}

					if(ApplyOn!=null && ApplyOn.trim().length()>0)
					{
						String[] ApplyOnArray = ApplyOn.split(",");
						for (String string : ApplyOnArray) 
						{	
							count++;
							System.out.println(string);
							dataStrBuff.append("<Detail2>");
							//dataStrBuff.append("<attribute  selected='Y'/>");
							//dataStrBuff.append("<selected>").append("<![CDATA[Y]]>").append("</selected>\r\n");
							dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
							dataStrBuff.append("<line_no>").append("<![CDATA[" + count + "]]>").append("</line_no>\r\n");
							dataStrBuff.append("<apply_on>").append("<![CDATA[" + string + "]]>").append("</apply_on>\r\n");
							dataStrBuff.append("<apply_on_from>").append("<![CDATA[]]>").append("</apply_on_from>\r\n");
							dataStrBuff.append("<apply_on_to>").append("<![CDATA[]]>").append("</apply_on_to>\r\n");
							//if((expression!=null && expression.trim().length()>0) &&(count==ApplyOnArray.length))
							if((expressionArray!=null) &&(!expressionArray.toString().equalsIgnoreCase("0") && expressionArray.toString().trim().length()>0))
							{
								dataStrBuff.append("<apply_on_exp protect =\"0\">").append("<![CDATA[" + expressionArray[count-1] + "]]>").append("</apply_on_exp>\r\n");
								dataStrBuff.append("<apply_on_exp_val1 protect =\"0\">").append("<![CDATA[]]>").append("</apply_on_exp_val1>\r\n");
								dataStrBuff.append("<apply_on_exp_val2 protect =\"0\">").append("<![CDATA[]]>").append("</apply_on_exp_val2>\r\n");
								
							}
							else
							{
								dataStrBuff.append("<apply_on_exp protect =\"1\">").append("<![CDATA[]]>").append("</apply_on_exp>\r\n");
								dataStrBuff.append("<apply_on_exp_val1 protect =\"1\">").append("<![CDATA[]]>").append("</apply_on_exp_val1>\r\n");
								dataStrBuff.append("<apply_on_exp_val2 protect =\"1\">").append("<![CDATA[]]>").append("</apply_on_exp_val2>\r\n");
							}
							dataStrBuff.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
							dataStrBuff.append("<rule_code>").append("<![CDATA[" + ruleCode + "]]>").append("</rule_code>\r\n");
							dataStrBuff.append("<descr>").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
							dataStrBuff.append("<valid_from>").append("<![CDATA[" + validFromStr + "]]>").append("</valid_from>\r\n");
							dataStrBuff.append("<valid_to>").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
							dataStrBuff.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
							dataStrBuff.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
							dataStrBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
							dataStrBuff.append("<chg_date>").append("<![CDATA[" + chgDateStr + "]]>").append("</chg_date>\r\n");
							dataStrBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>\r\n");
							dataStrBuff.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
							dataStrBuff.append("<add_date>").append("<![CDATA[" + addDateStr + "]]>").append("</add_date>\r\n");
							dataStrBuff.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
							
							dataStrBuff.append("</Detail2>");
						}
					}
				}
			*/}
			
			dataStrBuff.append("</Header0>");
			dataStrBuff.append("</group0>");
			dataStrBuff.append("</DocumentRoot>");
		System.out.println("valueXmlString["+dataStrBuff+"]");
	
		if(dataStrBuff!=null)
		{
			resultString = dataStrBuff.toString();
			System.out.println("ResultString....." + resultString);
		}
            
		}
		catch (SQLException e)
		{
			System.out.println("SQLException " + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally 
		{
			try {
				if (rs1 != null) 
				{
					rs1.close();
					rs1 = null;
				}
				if (pstmt1 != null) 
				{
					pstmt1.close();
					pstmt1 = null;
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
				if (conn != null) 
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("Inside Exception  getdata(D) [" + e.getMessage() +"]");
				e.printStackTrace();
			}
		}
		return resultString;

}

	private StringBuffer getSqlValueFromTaxBal(String query, String exp1,
			String exp2, ArrayList getParaVal, Connection conn, Document dom2 ,String tranId)  throws RemoteException, ITMException
	{

	    System.out.println("In PopUpDataAccess.getPopUpJSONData");
	    Connection myCon = null;
	    //Statement stmt = null;
	    String transDB = null;
	    StringBuffer dataStrBuff = new StringBuffer();
	    GenericUtility genericUtility = GenericUtility.getInstance();
	    String sql="";
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    String active="",carryData="",addTerm="",addUser="",chgTerm="",chgUser="";
		String itemCodeTo="",itemSerTo="",custCodeTo="",stanCodeTo="",applyOnExp="",applyOnExpVal1="",applyOnExpVal2="",itemCodeFrom="",itemSerFrom="";
		String custCodeFrom="",stanCodeFrom="",itemCode="",ruleDescr="";
		String tranDateStr="",validFromStr="",validToStr="",addDateStr="",chgDateStr="",ruleCode="",sqlInput="";
	    try
	    {
	    	ConnDriver connDriver = new ConnDriver();
	    	System.out.println("getPopUpJSONData..transDB..." + transDB);
	    	int counter = 0;
	    	System.out.println("Query :" + query);
	    	String inputName = null;
	    	String inputValue = null;
	    	validFromStr = checkNull(genericUtility.getColumnValue("valid_from", dom2));
	    	validToStr = checkNull(genericUtility.getColumnValue("valid_to", dom2));
	    	carryData = checkNull(genericUtility.getColumnValue("carry_data", dom2));
	    	active = checkNull(genericUtility.getColumnValue("active", dom2));
	    	ruleDescr = checkNull(genericUtility.getColumnValue("descr", dom2));
	    	chgUser = checkNull(genericUtility.getColumnValue("chg_user", dom2));
	    	chgDateStr = checkNull(genericUtility.getColumnValue("chg_date", dom2));
	    	chgTerm = checkNull(genericUtility.getColumnValue("chg_term", dom2));
	    	addUser = checkNull(genericUtility.getColumnValue("add_user", dom2));
	    	addDateStr = checkNull(genericUtility.getColumnValue("add_date", dom2));
	    	addTerm = checkNull(genericUtility.getColumnValue("add_term", dom2));
	    	itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code_from", dom2));
	    	itemCodeTo = checkNull(genericUtility.getColumnValue("item_code_to", dom2));
	    	itemSerFrom = checkNull(genericUtility.getColumnValue("item_ser_from", dom2));
	    	itemSerTo = checkNull(genericUtility.getColumnValue("item_ser_to", dom2));
	    	custCodeFrom = checkNull(genericUtility.getColumnValue("cust_code_from", dom2));
	    	custCodeTo = checkNull(genericUtility.getColumnValue("cust_code_to", dom2));
	    	stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code_from", dom2));
	    	stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code_to", dom2));
	    	applyOnExp = checkNull(genericUtility.getColumnValue("apply_on_exp", dom2));
	    	applyOnExpVal1 = checkNull(genericUtility.getColumnValue("apply_on_exp_val1", dom2));
	    	applyOnExpVal2 = checkNull(genericUtility.getColumnValue("apply_on_exp_val2", dom2));
	    	if(query != null && query.trim().length()>0)
	    	{	
	    		if (getParaVal != null && getParaVal.size()>0)
	    		{
	    			while (query.indexOf('?') != -1)
	    			{
	    				StringBuffer queryBuffer = new StringBuffer(query);
	    				int index = queryBuffer.toString().indexOf('?');
	    				inputValue = (String)getParaVal.get(counter);
	    				System.out.println(" In if condition inputValue[ " + inputValue + "]");
	    				if ((getParaVal != null) && (counter < getParaVal.size()) && ((inputValue == null) || (inputValue.trim().length() == 0) || ("null".equalsIgnoreCase(inputValue))))
	    				{
	    					inputName = ((String)getParaVal.get(counter)).toUpperCase();

	    				}
	    				inputValue = inputValue == null ? "" : inputValue;
	    				System.out.println("input name[" + index + "] :[" + inputName + "] input value[" + index + "] :[" + inputValue + "]");
	    				inputValue = checkNull(genericUtility.getColumnValue(inputValue, dom2));
	    				System.out.println("@S@>>> 635 ["+inputValue+"]");
	    				inputValue ="'"+inputValue+"'";
	    				System.out.println("@S@>>> 694 ["+inputValue+"]");
	    				queryBuffer.replace(index, index + 1, inputValue);
	    				query = queryBuffer.toString();
	    				queryBuffer = null;
	    				counter++;
	    			}
	    		}
	    		else
	    		{
	    			query=query;
	    		}

	    		System.out.println("In PopUpDataAccessEjb final query-->\n" + query);
	    		//Set value to dom
	    		sql=query;
	    		pstmt = conn.prepareStatement(sql);
	    		rs = pstmt.executeQuery();
	    		int i=0;
	    		while(rs.next())
	    		{
	    			i++;
	    			itemCode = checkNull(rs.getString(1));
	    			System.out.println(" @S@ itemCode"+itemCode);
	    			dataStrBuff.append("<Detail2>");
	    			dataStrBuff.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>\r\n");
	    			dataStrBuff.append("<line_no>").append("<![CDATA[" + i + "]]>").append("</line_no>\r\n");
	    			dataStrBuff.append("<apply_on>").append("<![CDATA["+exp2+"]]>").append("</apply_on>\r\n");
	    			dataStrBuff.append("<apply_on_from>").append("<![CDATA["+itemCode+"]]>").append("</apply_on_from>\r\n");
	    			dataStrBuff.append("<apply_on_to>").append("<![CDATA["+itemCode+"]]>").append("</apply_on_to>\r\n");
	    			dataStrBuff.append("<apply_on_exp protect =\"0\">").append("<![CDATA[" + applyOnExp + "]]>").append("</apply_on_exp>\r\n");
	    			dataStrBuff.append("<apply_on_exp_val2 protect =\"0\">").append("<![CDATA["+applyOnExpVal1+"]]>").append("</apply_on_exp_val2>\r\n");
	    			dataStrBuff.append("<apply_on_exp_val1 protect =\"0\">").append("<![CDATA["+applyOnExpVal2+"]]>").append("</apply_on_exp_val1>\r\n");
	    			dataStrBuff.append("<valid_from>").append("<![CDATA[" + validFromStr + "]]>").append("</valid_from>\r\n");
	    			dataStrBuff.append("<valid_to>").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
	    			//dataStrBuff.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
	    		//added By adnan
	    			dataStrBuff.append("<tran_date>").append("<![CDATA[" + checkNull(genericUtility.getColumnValue("tran_date", dom2)) + "]]>").append("</tran_date>\r\n");
	    			//dataStrBuff.append("<rule_code>").append("<![CDATA[" + ruleCode + "]]>").append("</rule_code>\r\n");
	    			dataStrBuff.append("<rule_code>").append("<![CDATA[" + checkNull(genericUtility.getColumnValue("rule_code", dom2)) + "]]>").append("</rule_code>\r\n");
	    			dataStrBuff.append("<descr>").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
	    			dataStrBuff.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
	    			dataStrBuff.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
	    			dataStrBuff.append("<item_code_from>").append("<![CDATA[" + itemCodeFrom + "]]>").append("</item_code_from>\r\n");
	    			dataStrBuff.append("<item_code_to>").append("<![CDATA[" + itemCodeTo + "]]>").append("</item_code_to>\r\n");
	    			dataStrBuff.append("<cust_code_from>").append("<![CDATA[" + custCodeFrom + "]]>").append("</cust_code_from>\r\n");
	    			dataStrBuff.append("<cust_code_to>").append("<![CDATA[" + custCodeTo + "]]>").append("</cust_code_to>\r\n");
	    			dataStrBuff.append("<item_ser_from>").append("<![CDATA[" +itemSerFrom + "]]>").append("</item_ser_from>\r\n");
	    			dataStrBuff.append("<item_ser_to>").append("<![CDATA[" + itemSerTo + "]]>").append("</item_ser_to>\r\n");
	    			dataStrBuff.append("<stan_code_from>").append("<![CDATA[" + stanCodeFrom + "]]>").append("</stan_code_from>\r\n");
	    			dataStrBuff.append("<stan_code_to>").append("<![CDATA[" + stanCodeTo + "]]>").append("</stan_code_to>\r\n");
	    			dataStrBuff.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
	    			dataStrBuff.append("<chg_date>").append("<![CDATA[" + chgDateStr + "]]>").append("</chg_date>\r\n");
	    			dataStrBuff.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>\r\n");
	    			dataStrBuff.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
	    			dataStrBuff.append("<add_date>").append("<![CDATA[" + addDateStr + "]]>").append("</add_date>\r\n");
	    			dataStrBuff.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
	    			dataStrBuff.append("</Detail2>");
	    		}
	    		rs.close();
	    		rs = null;
	    		pstmt.close();
	    		pstmt = null;
	    		

	    	}
	    }
	    catch (Exception e)
	    {
	      System.out.println("Exception :PopUpDataAccessEJB :getPopUpData :==>");
	      throw new ITMException(e);
	    }
	    finally
	    {
	      try
	      {
	        if (myCon != null)
	        {
					/*
					 * if (stmt != null) { stmt.close(); stmt = null; }
					 */
	          myCon.close();
	          myCon = null;
	        }
	      }
	      catch (Exception e)
	      {
	        throw new ITMException(e);
	      }
	    }
	   return dataStrBuff;
	  
	}

	public static String checkNull(String input) 
	{
		return input == null ? "" : input.trim();
	}

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("In ES3SalesMasterPrc.process() START");
		String retString = "";
		Document dom = null;
		Document dom2 = null;
		GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println("@S@xml["+xmlString+"][xml2]["+xmlString2+"]");
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			retString = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) 
		{
			System.out.println("Exception in ES3SalesMasterPrc.process() " + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("Returning from ES3SalesMasterPrc.process() " + retString);
		return retString;
	}

	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "", sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		Connection conn = null;
		GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		boolean connBollen = false;
		try {
			ConnDriver connDriver = new ConnDriver();
			/* conn = connDriver.getConnectDB("Driver"); */
			conn = getConnection();
			int mailAttachCount = 0;
			String fileList = "", newFileList = "";
			String fileSeparator = "";
			String transInfo = "";
			String fileListArray[] = null;
			String emailString = "";
			Timestamp tranDate = null,validFrom = null,validTo = null,addDate = null,chgDate = null;
			String active="",carryData="",addTerm="",addUser="",chgTerm="",chgUser="",tranId="",ruleDescr="";
			String tranDateStr="",validFromStr="",validToStr="",addDateStr="",chgDateStr="",ruleCode="",sqlInput="",lineNoStr="";
			String applyOn ="",expression="",applyOnFrom="",applyOnTo="",applyOnExpVal1="",applyOnExpVal2="",applyOnExp="";
			String itemCodeTo="",itemSerTo="",custCodeTo="",stanCodeTo="",itemCodeFrom="",itemSerFrom="",stanCodeFrom="",custCodeFrom="";
			String paySite = "";
			Date date = new Date();
			ES3RuleDetail ES3RuleDetailValue = null;
			ArrayList <ES3RuleDetail> setAllValueList = new ArrayList<ES3RuleDetail>();
			//String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//String termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdf2 = new SimpleDateFormat("hhmmssSSS");
			//HashMap setvalueToInsert = null;
			System.out.println("date fromat>>["+sdf.format(date)+"][yyyyMMdd]["+sdf1.format(date)+"][hhmmssSSS]["+sdf2.format(date)+"]");
			genericUtility = GenericUtility.getInstance();	
			NodeList parentNodeList = null,childNodeList = null;
			Node parentNode = null,childNode = null;
			int parentNodeListLength = 0,childNodeListLength = 0;
			ArrayList<String> applyOnList=new ArrayList<String>();
			tranId = checkNull(genericUtility.getColumnValue("tran_id", dom2));
			tranDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom2));
			System.out.println("tranDateStr..."+tranDateStr);
			ruleCode = checkNull(genericUtility.getColumnValue("rule_code", dom2));
			System.out.println("ruleCode.."+ruleCode);
			String ruleCode1 = checkNull(genericUtility.getColumnValue("rule_code", dom));
			System.out.println("@S@>>>>[ruleCode]["+ruleCode+"][ruleCode1]["+ruleCode1+"]");
			ruleDescr = checkNull(genericUtility.getColumnValue("descr", dom2));
			System.out.println("@S@in get data ruleDescr["+ruleDescr+"]");
		//	validFromStr = checkNull(genericUtility.getColumnValue("valid_from", dom2));
		//	validToStr = checkNull(genericUtility.getColumnValue("valid_to", dom2));
			carryData = checkNull(genericUtility.getColumnValue("carry_data", dom2));
			active = checkNull(genericUtility.getColumnValue("active", dom2));
			
			chgUser = checkNull(genericUtility.getColumnValue("chg_user", dom2));
			System.out.println("chgUser..."+chgUser);
			chgDateStr = checkNull(genericUtility.getColumnValue("chg_date", dom2));
			chgTerm = checkNull(genericUtility.getColumnValue("chg_term", dom2));
			addUser = checkNull(genericUtility.getColumnValue("add_user", dom2));
			addDateStr = checkNull(genericUtility.getColumnValue("add_date", dom2));
			addTerm = checkNull(genericUtility.getColumnValue("add_term", dom2));
			String objContext = checkNull(genericUtility.getColumnValue("OBJ_CONTEXT", dom2));
			String editFlag = checkNull(genericUtility.getColumnValue("EDIT_FLAG", dom2));
			itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code_from", dom2));
			itemCodeTo = checkNull(genericUtility.getColumnValue("item_code_to", dom2));
			itemSerFrom = checkNull(genericUtility.getColumnValue("item_ser_from", dom2));
			itemSerTo = checkNull(genericUtility.getColumnValue("item_ser_to", dom2));
			custCodeFrom = checkNull(genericUtility.getColumnValue("cust_code_from", dom2));
			custCodeTo = checkNull(genericUtility.getColumnValue("cust_code_to", dom2));
			stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code_from", dom2));
			stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code_to", dom2));
			applyOnExp = checkNull(genericUtility.getColumnValue("apply_on_exp", dom2));
			applyOnExpVal1 = checkNull(genericUtility.getColumnValue("apply_on_exp_val1", dom2));
			applyOnExpVal2 = checkNull(genericUtility.getColumnValue("apply_on_exp_val2", dom2));
			
			System.out.println("@S@objContext["+objContext+"][editFlag]["+editFlag+"]");
			if(tranId == null || tranId.trim().length()==0)
			{
			if(tranDateStr!=null && tranDateStr.trim().length()>0) {	
				
			tranDate = Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			
			}
		//	validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(validFromStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
		//	validTo = Timestamp.valueOf(genericUtility.getValidDateTimeString(validToStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}
			/*else
			{
				validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(validFromStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
				validTo = Timestamp.valueOf(genericUtility.getValidDateTimeString(validToStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			}*/
			//	addDate = Timestamp.valueOf(genericUtility.getValidDateTimeString(addDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
		//	chgDate = Timestamp.valueOf(genericUtility.getValidDateTimeString(chgDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			
			//setvalueToInsert = new HashMap();
		/*	setvalueToInsert.put("tran_id", tranId);
			setvalueToInsert.put("tran_date", tranDate);
			setvalueToInsert.put("rule_code",ruleCode);
			setvalueToInsert.put("valid_from", validFrom);
			setvalueToInsert.put("valid_to",validTo);
			setvalueToInsert.put("active",active);
			setvalueToInsert.put("carry_data", carryData);
			setvalueToInsert.put("chg_date",chgDate);
			setvalueToInsert.put("chg_user",chgUser);
			setvalueToInsert.put("chg_term",chgTerm);
			setvalueToInsert.put("add_date",addDate);
			setvalueToInsert.put("add_user",addUser);
			setvalueToInsert.put("add_term",addTerm);*/
		/*	ES3RuleDetailValue.setTranId(tranId);
			ES3RuleDetailValue.setTranDate(tranDate);
			ES3RuleDetailValue.setRuleCode(ruleCode);
			ES3RuleDetailValue.setValidFrom(validFrom);
			ES3RuleDetailValue.setValidTo(validTo);
			ES3RuleDetailValue.setActive(active);
			ES3RuleDetailValue.setCarryData(carryData);
			ES3RuleDetailValue.setChgDate(chgDate);
			ES3RuleDetailValue.setChgTerm(chgTerm);
			ES3RuleDetailValue.setChgUser(chgUser);
			ES3RuleDetailValue.setAddDate(addDate);
			ES3RuleDetailValue.setAddUser(addUser);
			ES3RuleDetailValue.setAddTerm(addTerm);
			setAllValueList.add(ES3RuleDetailValue);
			System.out.println("@S@>>>>>["+setAllValueList+"]");*/
			
			/*Class c = Class.forName("ValueToGet");
			ValueToGet a = (ValueToGet)c.newInstance();
			System.out.println("@S@ ValueToGet >>>>>>>>>>>>"+a.getRuleCode());
			*/
			String passRuleCode = ES3SalesRuleIC.ruleCodeInstance;
			System.out.println("@S@ passRuleCode >> "+ passRuleCode);
			//ValueToGet val= new ValueToGet(); 
			//System.out.println("@S@ >>>>>>>>>>>>"+val.getRuleCode());
			parentNodeList = dom2.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("parentNodeListLength"+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				ES3RuleDetailValue =  new ES3RuleDetail();
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					//setvalueToInsert = new HashMap();
					 
					childNode = childNodeList.item(childRow);
					String childNodeName = childNode.getNodeName();
					String columnValue = (childNode.getFirstChild() == null)?"":childNode.getFirstChild().getNodeValue();
					System.out.println("childNodeName : ["+childNodeName+"]   columnValue : ["+columnValue+"] ");
					ES3RuleDetailValue.setTranId(tranId);
					ES3RuleDetailValue.setTranDate(tranDate);
					ES3RuleDetailValue.setRuleCode(ruleCode);
					//ES3RuleDetailValue.setValidFrom(validFrom);
					//ES3RuleDetailValue.setValidTo(validTo);
					ES3RuleDetailValue.setActive(active);
					ES3RuleDetailValue.setItemCodeFrom(itemCodeFrom);
					ES3RuleDetailValue.setItemCodeTo(itemCodeTo);
					ES3RuleDetailValue.setCustCodeFrom(custCodeFrom);
					ES3RuleDetailValue.setCustCodeTo(custCodeTo);
					ES3RuleDetailValue.setItemSerFrom(itemSerFrom);
					ES3RuleDetailValue.setItemSerTo(itemSerTo);
					ES3RuleDetailValue.setStanCodeFrom(stanCodeFrom);
					ES3RuleDetailValue.setStanCodeTo(stanCodeTo);
					ES3RuleDetailValue.setChgDate(chgDate);
					ES3RuleDetailValue.setChgTerm(chgTerm);
					ES3RuleDetailValue.setChgUser(chgUser);
					ES3RuleDetailValue.setAddDate(addDate);
					ES3RuleDetailValue.setAddUser(addUser);
					ES3RuleDetailValue.setAddTerm(addTerm);
					ES3RuleDetailValue.setCarryData(carryData);
					//setAllValueList.add(ES3RuleDetailValue);
					//System.out.println("@S@>>>>>["+setAllValueList+"]");
					if (childNodeName.equals("tran_id"))
					{
						tranId = columnValue;
						//setvalueToInsert.put("tran_id", tranId);
						//ES3RuleDetailValue.setTranId(tranId);
					}
					else if (childNodeName.equals("line_no"))
					{
						lineNoStr = columnValue;
						//setvalueToInsert.put("line_no", lineNoStr);
					//	ES3RuleDetailValue.setLineNoStr(lineNoStr);
						System.out.println("@S@ inside lineNoStr ["+lineNoStr+"]");
					}
					else if (childNodeName.equals("apply_on"))
					{
						applyOn = columnValue;
						//setvalueToInsert.put("apply_on", applyOn);
						//ES3RuleDetailValue.setApplyOn(applyOn);
						System.out.println("@S@ inside apply_on ["+applyOn+"]");
						applyOnList.add(selectedRow, applyOn);
					}
					else if (childNodeName.equals("apply_on_from"))
					{
						applyOnFrom = columnValue;
						//setvalueToInsert.put("apply_on_from", applyOnFrom);
						//ES3RuleDetailValue.setApplyOnFrom(applyOnFrom);
						System.out.println("@S@ inside apply_on ["+applyOn+"]");
					}
					else if (childNodeName.equals("apply_on_to"))
					{
						applyOnTo = columnValue;
						//setvalueToInsert.put("apply_on_to", applyOnTo);
						//ES3RuleDetailValue.setApplyOnTo(applyOnTo);
						System.out.println("@S@ inside apply_on ["+applyOn+"]");
					}
					else if (childNodeName.equals("apply_on_exp"))
					{
						applyOnExp = columnValue;
						//setvalueToInsert.put("apply_on_exp", applyOnExp);
						//ES3RuleDetailValue.setApplyOnExp(applyOnExp);
						System.out.println("@S@ inside applyOnExp ["+applyOnExp+"]");
					}
					else if (childNodeName.equals("apply_on_exp_val1"))
					{
						applyOnExpVal1 = columnValue;
						//setvalueToInsert.put("apply_on_exp_val1", applyOnExpVal1);
						//ES3RuleDetailValue.setApplyOnExpVal1(applyOnExpVal1);
						System.out.println("@S@ inside applyOnExpVal1 ["+applyOnExpVal1+"]");
					}
					else if (childNodeName.equals("apply_on_exp_val2"))
					{
						applyOnExpVal2 = columnValue;
						//setvalueToInsert.put("apply_on_exp_val2", applyOnExpVal2);
						//ES3RuleDetailValue.setApplyOnExpVal2(applyOnExpVal2);
						System.out.println("@S@ inside applyOnExpVal2 ["+applyOnExpVal2+"]");
					}
					else if (childNodeName.equals("valid_from"))
					{
						validFromStr = columnValue;
						//setvalueToInsert.put("apply_on_exp_val2", applyOnExpVal2);
						//ES3RuleDetailValue.setApplyOnExpVal2(applyOnExpVal2);
						System.out.println("@S@ inside applyOnExpVal2 ["+validFromStr+"]");
					}
					else if (childNodeName.equals("valid_to"))
					{
						validToStr = columnValue;
						//setvalueToInsert.put("apply_on_exp_val2", applyOnExpVal2);
						//ES3RuleDetailValue.setApplyOnExpVal2(applyOnExpVal2);
						System.out.println("@S@ inside applyOnExpVal2 ["+validToStr+"]");
					}
					//System.out.println("@S@ inside apply_on ["+applyOn+"]");
					//setAllValueList.add(ES3RuleDetailValue);
					
				}//inner for loop
				System.out.println("@S@ line No 554["+lineNoStr+"]");
				System.out.println("@S@ line No 554["+applyOn+"]");
				System.out.println("@S@ line No 554["+applyOnFrom+"]");
				System.out.println("@S@ line No 554["+applyOnTo+"]");
				System.out.println("@S@ line No 554["+applyOnExp+"]");
				System.out.println("@S@ line No 554["+applyOnExpVal1+"]");
				System.out.println("@S@ line No 554["+applyOnExpVal2+"]");
				ES3RuleDetailValue.setLineNoStr(checkNull(lineNoStr));
				ES3RuleDetailValue.setApplyOn(checkNull(applyOn));
				ES3RuleDetailValue.setApplyOnFrom(checkNull(applyOnFrom));
				ES3RuleDetailValue.setApplyOnTo(checkNull(applyOnTo));
				ES3RuleDetailValue.setApplyOnExp(checkNull(applyOnExp));
				ES3RuleDetailValue.setApplyOnExpVal1(checkNull(applyOnExpVal1));
				ES3RuleDetailValue.setApplyOnExpVal2(checkNull(applyOnExpVal2));
				errString =getDateValidation(validFromStr,validToStr,conn,dom,dom2);
				if(errString == null || errString.trim().length()==0)
				{
					validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(validFromStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
					validTo = Timestamp.valueOf(genericUtility.getValidDateTimeString(validToStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
										
					ES3RuleDetailValue.setValidFrom(validFrom);
					ES3RuleDetailValue.setValidTo(validTo);
				}
				
				setAllValueList.add(ES3RuleDetailValue);
				System.out.println("@S@>>>>["+errString+"]");
				
			}// out for loop 
			System.out.println("@S@ 511 setAllValueList["+setAllValueList.toString()+"]");
			System.out.println("@S@ 511 setAllValueList["+setAllValueList.size()+"]");
			System.out.println("@S@ applyOnList["+applyOnList+"]");
			if ((errString == null || errString.trim().length()==0) && !connBollen) 
			{
				System.out.println("@S@>>>>"+errString);
			errString= insertAndUpdateInRuleTable(tranId,xtraParams,setAllValueList,conn);
			}
			
		}
		catch (Exception e) 
		{
			System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			e.printStackTrace();
			if(errString.indexOf("VTES3")==-1)
			{
			errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
			}
			connBollen=true;
		}
		finally 
		{
			try 
			{
				
				if ((errString == null || errString.trim().length()==0) && !connBollen) 
				{
					System.out.println("@S@ 575 Connection Commited");
					errString = itmDBAccessEJB.getErrorString("", "VTDATASUCC","", "", conn);
					conn.commit();
					
				}
				else
				{
					if(errString.indexOf("VTES3")==-1)
					{
					errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL","", "", conn);
					conn.rollback();
					}
				}
				if (conn != null)
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
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		
		return errString;
		
	}
	private String getDateValidation(String validFromStr , String validToStr,Connection conn ,Document dom,Document dom2) throws Exception 
	{
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		GenericUtility genericUtility = GenericUtility.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String errString="";
		Date validFrom=null,validTo=null;
		String active="",carryData="", ruleCode="",applyOnExpVal1="",applyOnExpVal2="",applyOnExp="";
		String itemCodeTo="",itemSerTo="",custCodeTo="",stanCodeTo="",itemCodeFrom="",itemSerFrom="",stanCodeFrom="",custCodeFrom="";
		String activeHdr="",carryDataHdr="", ruleCodeHdr="",applyOnExpVal1Hdr="",applyOnExpVal2Hdr="",applyOnExpHdr="";
		String itemCodeToHdr="",itemSerToHdr="",custCodeToHdr="",stanCodeToHdr="",itemCodeFromHdr="",itemSerFromHdr="",stanCodeFromHdr="",custCodeFromHdr="";
		try
		{
			System.out.println("@S@ inside FromDate["+validFromStr+"]");
			System.out.println("@S@ inside FromDate["+validToStr+"]");
			//Det data Process 
			ruleCode = checkNull(genericUtility.getColumnValue("rule_code", dom2));
			carryData = checkNull(genericUtility.getColumnValue("carry_data", dom2));
			active = checkNull(genericUtility.getColumnValue("active", dom2));
			itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code_from", dom2));
			itemCodeTo = checkNull(genericUtility.getColumnValue("item_code_to", dom2));
			itemSerFrom = checkNull(genericUtility.getColumnValue("item_ser_from", dom2));
			itemSerTo = checkNull(genericUtility.getColumnValue("item_ser_to", dom2));
			custCodeFrom = checkNull(genericUtility.getColumnValue("cust_code_from", dom2));
			custCodeTo = checkNull(genericUtility.getColumnValue("cust_code_to", dom2));
			stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code_from", dom2));
			stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code_to", dom2));
			applyOnExp = checkNull(genericUtility.getColumnValue("apply_on_exp", dom2));
			applyOnExpVal1 = checkNull(genericUtility.getColumnValue("apply_on_exp_val1", dom2));
			applyOnExpVal2 = checkNull(genericUtility.getColumnValue("apply_on_exp_val2", dom2));
			//HDR DATA
			ruleCodeHdr = checkNull(genericUtility.getColumnValue("rule_code", dom));
			carryDataHdr = checkNull(genericUtility.getColumnValue("carry_data", dom));
			activeHdr = checkNull(genericUtility.getColumnValue("active", dom));
			itemCodeFromHdr = checkNull(genericUtility.getColumnValue("item_code_from", dom));
			itemCodeToHdr = checkNull(genericUtility.getColumnValue("item_code_to", dom));
			itemSerFromHdr = checkNull(genericUtility.getColumnValue("item_ser_from", dom));
			itemSerToHdr = checkNull(genericUtility.getColumnValue("item_ser_to", dom));
			custCodeFromHdr = checkNull(genericUtility.getColumnValue("cust_code_from", dom));
			custCodeToHdr = checkNull(genericUtility.getColumnValue("cust_code_to", dom));
			stanCodeFromHdr = checkNull(genericUtility.getColumnValue("stan_code_from", dom));
			stanCodeToHdr = checkNull(genericUtility.getColumnValue("stan_code_to", dom));
			applyOnExpHdr = checkNull(genericUtility.getColumnValue("apply_on_exp", dom));
			applyOnExpVal1Hdr = checkNull(genericUtility.getColumnValue("apply_on_exp_val1", dom));
			applyOnExpVal2Hdr = checkNull(genericUtility.getColumnValue("apply_on_exp_val2", dom));
			
			if(!ruleCode.equalsIgnoreCase(ruleCodeHdr))
			{
				System.out.println("@S@ ruleCode["+ruleCode+"]ruleCodeHdr["+ruleCodeHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}
			/*else if(!carryData.equalsIgnoreCase(carryDataHdr))
			{
				System.out.println("@S@ carryData["+carryData+"]carryDataHdr["+carryDataHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}
			else if(!active.equalsIgnoreCase(activeHdr))
			{
				System.out.println("@S@ active["+active+"]activeHdr["+activeHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}*/
			else if(!itemCodeFrom.equalsIgnoreCase(itemCodeFromHdr))
			{
				System.out.println("@S@ itemCodeFrom["+itemCodeFrom+"]itemCodeFromHdr["+itemCodeFromHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}
			else if(!itemCodeTo.equalsIgnoreCase(itemCodeToHdr))
			{
				System.out.println("@S@ itemCodeTo["+itemCodeTo+"]itemCodeToHdr["+itemCodeToHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}
			else if(!itemSerFrom.equalsIgnoreCase(itemSerFromHdr))
			{
				System.out.println("@S@ itemSerFrom["+itemSerFrom+"]itemSerFromHdr["+itemSerFromHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!itemSerTo.equalsIgnoreCase(itemSerToHdr))
			{
				System.out.println("@S@ itemSerTo["+itemSerTo+"]itemSerToHdr["+itemSerToHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!custCodeFrom.equalsIgnoreCase(custCodeFromHdr))
			{
				System.out.println("@S@ custCodeFrom["+custCodeFrom+"]custCodeFromHdr["+custCodeFromHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!custCodeTo.equalsIgnoreCase(custCodeToHdr))
			{
				System.out.println("@S@ custCodeTo["+custCodeTo+"]custCodeToHdr["+custCodeToHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!stanCodeFrom.equalsIgnoreCase(stanCodeFromHdr))
			{
				System.out.println("@S@ stanCodeFrom["+stanCodeFrom+"]stanCodeFromHdr["+stanCodeFromHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!stanCodeTo.equalsIgnoreCase(stanCodeToHdr))
			{
				System.out.println("@S@ stanCodeTo["+stanCodeTo+"]stanCodeToHdr["+stanCodeToHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!applyOnExp.equalsIgnoreCase(applyOnExpHdr))
			{
				System.out.println("@S@ applyOnExp["+applyOnExp+"]applyOnExpHdr["+applyOnExpHdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!applyOnExpVal1.equalsIgnoreCase(applyOnExpVal1Hdr))
			{
				System.out.println("@S@ applyOnExpVal1["+applyOnExpVal1+"]applyOnExpVal1Hdr["+applyOnExpVal1Hdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if(!applyOnExpVal2.equalsIgnoreCase(applyOnExpVal2Hdr))
			{
				System.out.println("@S@ applyOnExpVal2["+applyOnExpVal2+"]applyOnExpVal2Hdr["+applyOnExpVal2Hdr+"]");
				errString = itmDBAccessEJB.getErrorString("", "VTES3HDMIS", "","", conn);
			}else if (validFromStr == null || validFromStr.trim().length() == 0) 
			{
				errString = itmDBAccessEJB.getErrorString("", "VTES3VAFBK", "","", conn);
			}
			else if (validToStr == null || validToStr.trim().length() == 0) 
			{

				errString = itmDBAccessEJB.getErrorString("", "VTES3VATBK", "","", conn);
			}
			else
			{
				if((validFromStr!=null && validFromStr.trim().length()>0) && (validToStr!=null && validToStr.trim().length()>0))
				{
					try
					{
					//validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(validFromStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
						validFrom = sdf.parse(validFromStr);
					}
					catch(Exception e)
					{
						errString = itmDBAccessEJB.getErrorString("", "VTES3INVFD", "","", conn);
					}
					try
					{
					//validTo = Timestamp.valueOf(genericUtility.getValidDateTimeString(validToStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
						validTo = sdf.parse(validToStr);
					}
					catch(Exception e)
					{
						errString = itmDBAccessEJB.getErrorString("", "VTES3INVTD", "","", conn);
											}
					if(validTo.before(validFrom))
					{
						errString = itmDBAccessEJB.getErrorString("", "VTES3VTLVF", "","", conn);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		

		return errString;
	}

	private String insertAndUpdateInRuleTable(String tranId, String xtraParams,ArrayList<ES3RuleDetail> setAllValueList, Connection conn) throws RemoteException, ITMException 
	{
		String errString="",chgTerm="",chgUser="",loginSiteCode="",tranIdNew="",sysDate="";
		PreparedStatement pstmt= null;
		Timestamp sysDateTS=null;
		ResultSet rs= null;
		String sql1="",sql="";
		 int count =0; 
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		GenericUtility genericUtility = GenericUtility.getInstance();
		
		try 
		{
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
			SimpleDateFormat sdf= new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(new java.util.Date());
			sysDateTS = Timestamp.valueOf(genericUtility.getValidDateTimeString(sysDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()));
			if(tranId!=null && tranId.trim().length()>0)
			{
				//For Update the Record
				if(setAllValueList!=null)
				{
					for (ES3RuleDetail ES3RuleDetailGetValue : setAllValueList) 
					{

						//To insert HDR table at once 
						if(count == 0)
						{
							count++;
							sql ="update es3_rule_hdr set rule_code = ? ,active = ? ," +
									"add_date = ? ,add_user = ? ,add_term = ? ,carry_data = ? ,item_code_from = ? ," +
									" item_code_to = ?, item_ser_from  = ? , item_ser_to = ? , cust_code_from = ? , cust_code_to = ? ," +
									"stan_code_from = ? ,stan_code_to = ? where tran_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ES3RuleDetailGetValue.getRuleCode());
							//pstmt.setTimestamp(2, ES3RuleDetailGetValue.getValidFrom());
							//pstmt.setTimestamp(3, ES3RuleDetailGetValue.getValidTo());
							pstmt.setString(2, ES3RuleDetailGetValue.getActive());
							pstmt.setTimestamp(3,sysDateTS);
							pstmt.setString(4, chgUser);
							pstmt.setString(5, chgTerm);
							pstmt.setString(6, ES3RuleDetailGetValue.getCarryData());
							pstmt.setString(7,ES3RuleDetailGetValue.getItemCodeFrom());
							pstmt.setString(8,ES3RuleDetailGetValue.getItemCodeTo());
							pstmt.setString(9,ES3RuleDetailGetValue.getItemSerFrom());
							pstmt.setString(10,ES3RuleDetailGetValue.getItemSerTo());
							pstmt.setString(11,ES3RuleDetailGetValue.getCustCodeFrom());
							pstmt.setString(12,ES3RuleDetailGetValue.getCustCodeTo());
							pstmt.setString(13,ES3RuleDetailGetValue.getStanCodeFrom());
							pstmt.setString(14,ES3RuleDetailGetValue.getStanCodeTo());
							pstmt.setString(15, ES3RuleDetailGetValue.getTranId());
							int updCnt = pstmt.executeUpdate();
							if(updCnt>0)
							{
								errString="";
								System.out.println(" @S@ update HDR Data !!! ");
							}
							else
							{
								System.out.println(" @S@ update HDR Data !!! ");
								errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
							}
							pstmt.close();
							pstmt= null;

							sql=" update es3_rule_det set apply_on = ? ,apply_on_from = ? ,apply_on_to = ? ,apply_on_exp = ? ," +
									"apply_on_exp_val1 = ? ,apply_on_exp_val2 = ? ,valid_from = ? ,valid_to = ?  where  tran_id = ?  and trim(line_no) = ? ";        
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,ES3RuleDetailGetValue.getApplyOn());
							pstmt.setString(2,ES3RuleDetailGetValue.getApplyOnFrom());
							pstmt.setString(3,ES3RuleDetailGetValue.getApplyOnTo());
							pstmt.setString(4,ES3RuleDetailGetValue.getApplyOnExp());
							pstmt.setString(5,ES3RuleDetailGetValue.getApplyOnExpVal1());
							pstmt.setString(6,ES3RuleDetailGetValue.getApplyOnExpVal2());
							pstmt.setTimestamp(7, ES3RuleDetailGetValue.getValidFrom());
							pstmt.setTimestamp(8, ES3RuleDetailGetValue.getValidTo());
							pstmt.setString(9,ES3RuleDetailGetValue.getTranId());
							pstmt.setInt(10,count);
							int updCnt1 = pstmt.executeUpdate();
							if(updCnt1>0)
							{
								errString="";
								System.out.println("@S@ update DET Data!!! ");
							}
							else
							{
								System.out.println("@S@ update DET Data fail!!!");
								errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
							}
							pstmt.close();
							pstmt= null;

						}
						else
						{
							count++;
							sql=" update es3_rule_det set apply_on = ? ,apply_on_from = ? ,apply_on_to = ? ,apply_on_exp = ? ," +
									"apply_on_exp_val1 = ? ,apply_on_exp_val2 = ? ,valid_from = ? ,valid_to = ?  where  tran_id = ?  and trim(line_no) = ? ";        
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,ES3RuleDetailGetValue.getApplyOn());
							pstmt.setString(2,ES3RuleDetailGetValue.getApplyOnFrom());
							pstmt.setString(3,ES3RuleDetailGetValue.getApplyOnTo());
							pstmt.setString(4,ES3RuleDetailGetValue.getApplyOnExp());
							pstmt.setString(5,ES3RuleDetailGetValue.getApplyOnExpVal1());
							pstmt.setString(6,ES3RuleDetailGetValue.getApplyOnExpVal2());
							pstmt.setTimestamp(7, ES3RuleDetailGetValue.getValidFrom());
							pstmt.setTimestamp(8, ES3RuleDetailGetValue.getValidTo());
							pstmt.setString(9,ES3RuleDetailGetValue.getTranId());
							pstmt.setInt(10,count);
							int updCnt1 = pstmt.executeUpdate();
							if(updCnt1>0)
							{
								errString="";
								System.out.println("@S@ update DET Data!!! ");
							}
							else
							{
								System.out.println("@S@ update DET Data fail!!!");
								errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
							}
							pstmt.close();
							pstmt= null;	
						}
					}
				}
				
			}
			else
			{//Insert the New Record
				tranIdNew=generateTranIDForEs3SalesMaster("es3_sales_rule",conn);
				if(setAllValueList!=null)
				{
					int con=0;
					
					for (ES3RuleDetail ES3RuleDetailGetValue : setAllValueList) 
					{
						System.out.println(setAllValueList.size());
						
						System.out.println("value of"+con++);
						System.out.println("@S@ inside for each loop[ "+ES3RuleDetailGetValue.getLineNoStr());
						
						//To insert HDR table at once 
						if(count == 0)
						{
							count++;
							sql=" insert into es3_rule_hdr(tran_id,tran_date,rule_code,active,chg_date,chg_user," +
									"chg_term,add_date,add_user,add_term,carry_data,item_code_from,item_code_to,cust_code_from,cust_code_to," +
									"item_ser_from,item_ser_to,stan_code_from,stan_code_to)" +
									"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";   
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdNew);
							pstmt.setTimestamp(2, ES3RuleDetailGetValue.getTranDate());
							pstmt.setString(3, ES3RuleDetailGetValue.getRuleCode());
							pstmt.setString(4, ES3RuleDetailGetValue.getActive());
							pstmt.setTimestamp(5, sysDateTS);
							pstmt.setString(6, chgUser);
							pstmt.setString(7, chgTerm);
							pstmt.setTimestamp(8, sysDateTS);
							pstmt.setString(9, chgUser);
							pstmt.setString(10, chgTerm);
							pstmt.setString(11, ES3RuleDetailGetValue.getCarryData());
							pstmt.setString(12, ES3RuleDetailGetValue.getItemCodeFrom());
							pstmt.setString(13, ES3RuleDetailGetValue.getItemCodeTo());
							pstmt.setString(14, ES3RuleDetailGetValue.getCustCodeFrom());
							pstmt.setString(15, ES3RuleDetailGetValue.getCustCodeTo());
							pstmt.setString(16, ES3RuleDetailGetValue.getItemSerFrom());
							pstmt.setString(17, ES3RuleDetailGetValue.getItemSerTo());
							pstmt.setString(18, ES3RuleDetailGetValue.getStanCodeFrom());
							pstmt.setString(19, ES3RuleDetailGetValue.getStanCodeTo());
							int updCnt = pstmt.executeUpdate();
							if(updCnt>0)
							{
								errString="";
								System.out.println("Data inserted!!!");
							}
							else
							{
								System.out.println("Data insertion fail!!!");
								errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
							}
							pstmt.close();
							pstmt= null;
							System.out.println("@S@ inside for each loop[ "+ES3RuleDetailGetValue.getApplyOn());
							System.out.println("@S@ inside for each loop[ "+ES3RuleDetailGetValue.getLineNoStr());
							sql=" insert into es3_rule_det (tran_id,line_no,apply_on,apply_on_from,apply_on_to,apply_on_exp," +
									"apply_on_exp_val1,apply_on_exp_val2,valid_from,valid_to) " +
									"values(?,?,?,?,?,?,?,?,?,?)";        
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,tranIdNew);
							pstmt.setInt(2,count);
							pstmt.setString(3,ES3RuleDetailGetValue.getApplyOn());
							pstmt.setString(4,ES3RuleDetailGetValue.getApplyOnFrom());
							pstmt.setString(5,ES3RuleDetailGetValue.getApplyOnTo());
							pstmt.setString(6,ES3RuleDetailGetValue.getApplyOnExp());
							pstmt.setString(7,ES3RuleDetailGetValue.getApplyOnExpVal1());
							pstmt.setString(8,ES3RuleDetailGetValue.getApplyOnExpVal2());
							pstmt.setTimestamp(9, ES3RuleDetailGetValue.getValidFrom());
							pstmt.setTimestamp(10, ES3RuleDetailGetValue.getValidTo());
							int updCnt1 = pstmt.executeUpdate();
							if(updCnt1>0)
							{
								errString="";
								System.out.println("Data inserted!!!");
							}
							else
							{
								System.out.println("Data insertion fail!!!");
								errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
							}
							pstmt.close();
							pstmt= null;
							

						}
						else
						{
							count++;
							System.out.println("@S@ inside for each loop[ "+ES3RuleDetailGetValue.getApplyOn());
							System.out.println("@S@ inside for each loop[ "+ES3RuleDetailGetValue.getLineNoStr());
							sql=" insert into es3_rule_det (tran_id,line_no,apply_on,apply_on_from,apply_on_to,apply_on_exp," +
									"apply_on_exp_val1,apply_on_exp_val2,valid_from,valid_to) " +
									"values(?,?,?,?,?,?,?,?,?,?)";      
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,tranIdNew);
							pstmt.setInt(2,count);
							pstmt.setString(3,ES3RuleDetailGetValue.getApplyOn());
							pstmt.setString(4,ES3RuleDetailGetValue.getApplyOnFrom());
							pstmt.setString(5,ES3RuleDetailGetValue.getApplyOnTo());
							pstmt.setString(6,ES3RuleDetailGetValue.getApplyOnExp());
							pstmt.setString(7,ES3RuleDetailGetValue.getApplyOnExpVal1());
							pstmt.setString(8,ES3RuleDetailGetValue.getApplyOnExpVal2());
							pstmt.setTimestamp(9, ES3RuleDetailGetValue.getValidFrom());
							pstmt.setTimestamp(10, ES3RuleDetailGetValue.getValidTo());
							int updCnt = pstmt.executeUpdate();
							if(updCnt>0)
							{
								errString="";
								System.out.println("Data @S@ 809 inserted!!!");
							}
							else
							{
								System.out.println("Data insertion fail!!!");
								errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
							}
							pstmt.close();
							pstmt= null;
						}
					}
					
				}
			}
		} 
		catch (Exception e)
		{
			System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);

		}
		return errString;
		
	}
	private String generateTranIDForEs3SalesMaster(String objName,Connection conn) throws ITMException 
	{

		String retString = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String keyString = "", refSer = "",sysDate="";
		E12GenericUtility genericUtility =new E12GenericUtility();
		try
		{
			SimpleDateFormat sdf= new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(new java.util.Date());
			System.out.println("SalesConsolidationProcess-ES3 :: objName =>"+objName);
			HashMap<String, String> transetupMap = new HashMap<String, String>();
			transetupMap = getTransetupMap("w_"+objName, conn);
			keyString = (String)transetupMap.get("key_string");
			refSer = (String)transetupMap.get("ref_ser");
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<TRAN_ID></TRAN_ID>";
			xmlValues = xmlValues +	"<TRAN_DATE>"+sysDate+"</TRAN_DATE>";
			//xmlValues = xmlValues +	"<SITE_CODE>"+loginSiteCode+"</SITE_CODE>";
			//xmlValues = xmlValues +	"<ITEM_SER>"+itemSer+"</ITEM_SER>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues for Sales Consolidation :["+xmlValues+"]");
			System.out.println("keyString>>>>"+keyString+">>>refSer>>>"+refSer);
			TransIDGenerator tranIdGenerator = new TransIDGenerator(xmlValues, "SYSTEM", CommonConstants.DB_NAME);
			String tranIdGenerated = tranIdGenerator.generateTranSeqID(refSer, "tran_id", keyString, conn);
			System.out.println("tranIdGenerated for SalesConsolidationProcess-ES3 => "+tranIdGenerated);
			retString = tranIdGenerated;
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
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return retString;
	
	}
	private HashMap<String, String> getTransetupMap(String winName, Connection conn) throws ITMException
	{
		String keyString = "";
		String refSer = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, String> transetupMap = null;
		try 
		{
			sql = "SELECT KEY_STRING, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, winName);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyString = rs.getString("KEY_STRING") ;
				refSer = rs.getString("REF_SER");
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
			System.out.println("ITWizardBean :: getKeyString :: keyString =>"+keyString);
			System.out.println("ITWizardBean :: getKeyString :: refSer =>"+refSer);
			transetupMap = new HashMap<String, String>();
			transetupMap.put("key_string", keyString);
			transetupMap.put("ref_ser", refSer);
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
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
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return transetupMap;
	}
	public static  class ES3RuleDetail
	{
		private Timestamp tranDate = null,validFrom = null,validTo = null,addDate = null,chgDate = null;
		private String active="",carryData="",addTerm="",addUser="",chgTerm="",chgUser="",tranId="",ruleDescr="";
		private	String tranDateStr="",validFromStr="",validToStr="",addDateStr="",chgDateStr="",ruleCode="",sqlInput="",lineNoStr="";
		private String applyOn ="",expression="",applyOnFrom="",applyOnTo="",applyOnExpVal1="",applyOnExpVal2="",applyOnExp="";
		private String  itemCodeTo="",itemSerTo="",custCodeTo="",stanCodeTo="",itemCodeFrom="",itemSerFrom="",stanCodeFrom="",custCodeFrom=""; 
		public Timestamp getTranDate() {
			return tranDate;
		}
		public void setTranDate(Timestamp tranDate) {
			this.tranDate = tranDate;
		}
		public Timestamp getValidFrom() {
			return validFrom;
		}
		public void setValidFrom(Timestamp validFrom) {
			this.validFrom = validFrom;
		}
		public Timestamp getValidTo() {
			return validTo;
		}
		public void setValidTo(Timestamp validTo) {
			this.validTo = validTo;
		}
		public Timestamp getAddDate() {
			return addDate;
		}
		public void setAddDate(Timestamp addDate) {
			this.addDate = addDate;
		}
		public Timestamp getChgDate() {
			return chgDate;
		}
		public void setChgDate(Timestamp chgDate) {
			this.chgDate = chgDate;
		}
		public String getActive() {
			return active;
		}
		public void setActive(String active) {
			this.active = active;
		}
		public String getCarryData() {
			return carryData;
		}
		public void setCarryData(String carryData) {
			this.carryData = carryData;
		}
		public String getAddTerm() {
			return addTerm;
		}
		public void setAddTerm(String addTerm) {
			this.addTerm = addTerm;
		}
		public String getAddUser() {
			return addUser;
		}
		public void setAddUser(String addUser) {
			this.addUser = addUser;
		}
		public String getChgTerm() {
			return chgTerm;
		}
		public void setChgTerm(String chgTerm) {
			this.chgTerm = chgTerm;
		}
		public String getChgUser() {
			return chgUser;
		}
		public void setChgUser(String chgUser) {
			this.chgUser = chgUser;
		}
		public String getTranId() {
			return tranId;
		}
		public void setTranId(String tranId) {
			this.tranId = tranId;
		}
		public String getRuleDescr() {
			return ruleDescr;
		}
		public void setRuleDescr(String ruleDescr) {
			this.ruleDescr = ruleDescr;
		}
		public String getTranDateStr() {
			return tranDateStr;
		}
		public void setTranDateStr(String tranDateStr) {
			this.tranDateStr = tranDateStr;
		}
		public String getValidFromStr() {
			return validFromStr;
		}
		public void setValidFromStr(String validFromStr) {
			this.validFromStr = validFromStr;
		}
		public String getValidToStr() {
			return validToStr;
		}
		public void setValidToStr(String validToStr) {
			this.validToStr = validToStr;
		}
		public String getAddDateStr() {
			return addDateStr;
		}
		public void setAddDateStr(String addDateStr) {
			this.addDateStr = addDateStr;
		}
		public String getChgDateStr() {
			return chgDateStr;
		}
		public void setChgDateStr(String chgDateStr) {
			this.chgDateStr = chgDateStr;
		}
		public String getRuleCode() {
			return ruleCode;
		}
		public void setRuleCode(String ruleCode) {
			this.ruleCode = ruleCode;
		}
		public String getSqlInput() {
			return sqlInput;
		}
		public void setSqlInput(String sqlInput) {
			this.sqlInput = sqlInput;
		}
		public String getLineNoStr() {
			return lineNoStr;
		}
		public void setLineNoStr(String lineNoStr) {
			this.lineNoStr = lineNoStr;
		}
		public String getApplyOn() {
			return applyOn;
		}
		public void setApplyOn(String applyOn) {
			this.applyOn = applyOn;
		}
		public String getExpression() {
			return expression;
		}
		public void setExpression(String expression) {
			this.expression = expression;
		}
		public String getApplyOnFrom() {
			return applyOnFrom;
		}
		public void setApplyOnFrom(String applyOnFrom) {
			this.applyOnFrom = applyOnFrom;
		}
		public String getApplyOnTo() {
			return applyOnTo;
		}
		public void setApplyOnTo(String applyOnTo) {
			this.applyOnTo = applyOnTo;
		}
		public String getApplyOnExpVal1() {
			return applyOnExpVal1;
		}
		public void setApplyOnExpVal1(String applyOnExpVal1) {
			this.applyOnExpVal1 = applyOnExpVal1;
		}
		public String getApplyOnExpVal2() {
			return applyOnExpVal2;
		}
		public void setApplyOnExpVal2(String applyOnExpVal2) {
			this.applyOnExpVal2 = applyOnExpVal2;
		}
		public String getApplyOnExp() {
			return applyOnExp;
		}
		public void setApplyOnExp(String applyOnExp) {
			this.applyOnExp = applyOnExp;
		}
		public String getItemCodeTo() {
			return itemCodeTo;
		}
		public void setItemCodeTo(String itemCodeTo) {
			this.itemCodeTo = itemCodeTo;
		}
		public String getItemSerTo() {
			return itemSerTo;
		}
		public void setItemSerTo(String itemSerTo) {
			this.itemSerTo = itemSerTo;
		}
		public String getCustCodeTo() {
			return custCodeTo;
		}
		public void setCustCodeTo(String custCodeTo) {
			this.custCodeTo = custCodeTo;
		}
		public String getStanCodeTo() {
			return stanCodeTo;
		}
		public void setStanCodeTo(String stanCodeTo) {
			this.stanCodeTo = stanCodeTo;
		}
		public String getItemCodeFrom() {
			return itemCodeFrom;
		}
		public void setItemCodeFrom(String itemCodeFrom) {
			this.itemCodeFrom = itemCodeFrom;
		}
		public String getItemSerFrom() {
			return itemSerFrom;
		}
		public void setItemSerFrom(String itemSerFrom) {
			this.itemSerFrom = itemSerFrom;
		}
		public String getStanCodeFrom() {
			return stanCodeFrom;
		}
		public void setStanCodeFrom(String stanCodeFrom) {
			this.stanCodeFrom = stanCodeFrom;
		}
		public String getCustCodeFrom() {
			return custCodeFrom;
		}
		public void setCustCodeFrom(String custCodeFrom) {
			this.custCodeFrom = custCodeFrom;
		}
	}

}
