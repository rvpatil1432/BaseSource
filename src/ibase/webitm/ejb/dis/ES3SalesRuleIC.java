
package ibase.webitm.ejb.dis;



import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ES3SalesRuleIC extends ValidatorEJB implements ES3SalesRuleICLocal , ES3SalesRuleICRemote
{
	
	
	static String  ruleCodeInstance ="";
	
	
	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";

		try 
		{
			System.out.println("xmlString"+xmlString);
			System.out.println("xmlString1"+xmlString1);
			System.out.println("xmlString2"+xmlString2);
			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0) 
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) 
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : AttachmentMailPrcIC : itemChanged :==>\n" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return valueXmlString;
	}
	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException 
			{
		Connection conn = null;
		int currentFormNo = 0;
		ResultSet rs = null;
		String sql = "";
		PreparedStatement pstmt = null;
		StringBuffer valueXmlString = null;
		SimpleDateFormat simpleDateFormat = null;
		String currDate = "", ruleDescr = "",tranId = "",sqlInput="",ApplyOn="",expression="",itemCodeFrom="",itemSerFrom="";
		String stanCodeFrom="",custCodeFrom="";
		Timestamp tranDate = null,validFrom = null,validTo = null,addDate = null,chgDate = null;
		String active="",carryData="",addTerm="",addUser="",chgTerm="",chgUser="";
		String tranDateStr="",validFromStr="",validToStr="",addDateStr="",chgDateStr="";
		String ruleCode="";
		String itemCodeTo="",itemSerTo="",custCodeTo="",stanCodeTo="",applyOnExp="",applyOnExpVal1="",applyOnExpVal2="";
		String[] expressionArray= null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		try 
		{
			valueXmlString = new StringBuffer();
			ConnDriver connDriver = new ConnDriver();
			/* conn = connDriver.getConnectDB("DriverITM"); */
			conn = getConnection();
			conn.setAutoCommit(false);
			simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			Calendar currentDate = Calendar.getInstance();
			
			currDate = simpleDateFormat.format(currentDate.getTime());
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
				case 1:
					System.out.println("adnan... ");
					valueXmlString.append("<Detail1>");		
					if(currentColumn.equalsIgnoreCase("itm_default")) 
					{
						/*Calendar currentDate = Calendar.getInstance();
						
						currDate = simpleDateFormat.format(currentDate.getTime());*/
						valueXmlString.append("<tran_date>").append("<![CDATA["+currDate+"]]>").append("</tran_date>\r\n");
						valueXmlString.append("<valid_from>").append("<![CDATA[" + currDate + "]]>").append("</valid_from>\r\n");
						valueXmlString.append("<valid_to>").append("<![CDATA[" + currDate + "]]>").append("</valid_to>\r\n");
						//valueXmlString.append("<chg_date>").append("<![CDATA[" + currDate + "]]>").append("</chg_date>\r\n");
						//valueXmlString.append("<add_date>").append("<![CDATA[" + currDate + "]]>").append("</add_date>\r\n");
						valueXmlString.append("<active>").append("<![CDATA[Y]]>").append("</active>\r\n");
						valueXmlString.append("<carry_data>").append("<![CDATA[N]]>").append("</carry_data>\r\n");
						valueXmlString.append("<item_code_from protect = \"1\">").append("").append("</item_code_from>\r\n");
						valueXmlString.append("<item_code_to protect = \"1\">").append("").append("</item_code_to>\r\n");
						valueXmlString.append("<item_ser_from protect = \"1\">").append("").append("</item_ser_from>\r\n");
						valueXmlString.append("<item_ser_to protect = \"1\">").append("").append("</item_ser_to>\r\n");
						valueXmlString.append("<cust_code_from protect = \"1\">").append("").append("</cust_code_from>\r\n");
						valueXmlString.append("<cust_code_to protect = \"1\">").append("").append("</cust_code_to>\r\n");
						valueXmlString.append("<stan_code_from protect = \"1\">").append("").append("</stan_code_from>\r\n");
						valueXmlString.append("<stan_code_to protect = \"1\">").append("").append("</stan_code_to>\r\n");
						valueXmlString.append("<apply_on_exp_val1 protect = \"1\">").append("").append("</apply_on_exp_val1>\r\n");
						valueXmlString.append("<apply_on_exp_val2 protect = \"1\">").append("").append("</apply_on_exp_val2>\r\n");
						
						System.out.println("defult end");
					}
					else if(currentColumn.equalsIgnoreCase("rule_code"))
					{
						ruleCode = checkNull(getColumnValue("rule_code", dom));
						System.out.println("ruleCode >> "+ruleCode);
						ruleCodeInstance=ruleCode;
						//ValueToGet val = new ValueToGet();
						//val.setRuleCode(ruleCode);
						System.out.println("@S@ruleCode["+ruleCodeInstance+"]");
						sql = "select descr from tax_balance_grp where bal_group = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ruleCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ruleDescr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("@S@ruleDescr["+ruleDescr+"]");
						valueXmlString.append("<descr>").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
					//	commented by adnan added new rule code
						//if("R0003".equalsIgnoreCase(ruleCode) ||"R0004".equalsIgnoreCase(ruleCode))
						if("ES3R3".equalsIgnoreCase(ruleCode) ||"ES3R4".equalsIgnoreCase(ruleCode))
						{
							valueXmlString.append("<carry_data protect = \"0\">").append("<![CDATA[Y]]>").append("</carry_data>\r\n");
						}
						else
						{
							valueXmlString.append("<carry_data protect = \"0\">").append("<![CDATA[N]]>").append("</carry_data>\r\n");
						}

						sql = "select sql_input from tax_bal_grp_det where bal_group = ? and ref_ser = 'C-ES3' ";//
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
							System.out.println(">>  ApplyOn ["+ApplyOn+"]");
						
							if(ApplyOn!=null && ApplyOn.trim().length()>0)
							{
								ArrayList<String> ApplyOnArray = new ArrayList <String>(Arrays.asList(ApplyOn.split(",")));
								
								System.out.println("System.out.println(ApplyOnArray)"+ApplyOnArray);
							/*	String[] ApplyOnArray =	ApplyOn.split(",");
								boolean itemCodeFromBol=false,itemSerFromBol=false,stanCodeFromBol=false,custCodeFromBol=false;
								for (String string : ApplyOnArray) 
								{
									string = string.concat("_from");
									System.out.println(">>>> line no 201["+string+"]");
									if("item_code_from".equalsIgnoreCase(string))
									{
										itemCodeFromBol=true;
										valueXmlString.append("<item_code_from protect = \"0\">").append("").append("</item_code_from>\r\n");
										valueXmlString.append("<item_code_to protect = \"0\">").append("").append("</item_code_to>\r\n");
									}
									else if("item_ser_from".equalsIgnoreCase(string))
									{
										itemSerFromBol=true;
										valueXmlString.append("<item_ser_from protect = \"0\">").append("").append("</item_ser_from>\r\n");
										valueXmlString.append("<item_ser_to protect = \"0\">").append("").append("</item_ser_to>\r\n");
									}
									else if("cust_code_from".equalsIgnoreCase(string))
									{
										custCodeFromBol=true;
										valueXmlString.append("<cust_code_from protect = \"0\">").append("").append("</cust_code_from>\r\n");
										valueXmlString.append("<cust_code_to protect = \"0\">").append("").append("</cust_code_to>\r\n");
											
									}
									else if("stan_code_from".equalsIgnoreCase(string))
									{
										stanCodeFromBol=true;
										valueXmlString.append("<stan_code_from protect = \"0\">").append("").append("</stan_code_from>\r\n");
										valueXmlString.append("<stan_code_to protect = \"0\">").append("").append("</stan_code_to>\r\n");
									}
									
								}
								if(!itemCodeFromBol || !itemSerFromBol || !custCodeFromBol || !stanCodeFromBol)
								{
									
								}*/
								if(ApplyOnArray.contains("item_code"))
								{
									valueXmlString.append("<item_code_from protect = \"0\">").append("").append("</item_code_from>\r\n");
									valueXmlString.append("<item_code_to protect = \"0\">").append("").append("</item_code_to>\r\n");
								}
							    if(ApplyOnArray.contains("item_ser"))
								{
									valueXmlString.append("<item_ser_from protect = \"0\">").append("").append("</item_ser_from>\r\n");
									valueXmlString.append("<item_ser_to protect = \"0\">").append("").append("</item_ser_to>\r\n");
								}
								if(ApplyOnArray.contains("cust_code"))
								{
									valueXmlString.append("<cust_code_from protect = \"0\">").append("").append("</cust_code_from>\r\n");
									valueXmlString.append("<cust_code_to protect = \"0\">").append("").append("</cust_code_to>\r\n");
										
								}
								if(ApplyOnArray.contains("stan_code"))
								{
									valueXmlString.append("<stan_code_from protect = \"0\">").append("").append("</stan_code_from>\r\n");
									valueXmlString.append("<stan_code_to protect = \"0\">").append("").append("</stan_code_to>\r\n");
								}
								
								if(!ApplyOnArray.contains("item_code"))
								{
									valueXmlString.append("<item_code_from protect = \"1\">").append("").append("</item_code_from>\r\n");
									valueXmlString.append("<item_code_to protect = \"1\">").append("").append("</item_code_to>\r\n");
								}
							    if(!ApplyOnArray.contains("item_ser"))
								{
									valueXmlString.append("<item_ser_from protect = \"1\">").append("").append("</item_ser_from>\r\n");
									valueXmlString.append("<item_ser_to protect = \"1\">").append("").append("</item_ser_to>\r\n");
								}
								if(!ApplyOnArray.contains("cust_code"))
								{
									valueXmlString.append("<cust_code_from protect = \"1\">").append("").append("</cust_code_from>\r\n");
									valueXmlString.append("<cust_code_to protect = \"1\">").append("").append("</cust_code_to>\r\n");
										
								}
								if(!ApplyOnArray.contains("stan_code"))
								{
									valueXmlString.append("<stan_code_from protect = \"1\">").append("").append("</stan_code_from>\r\n");
									valueXmlString.append("<stan_code_to protect = \"1\">").append("").append("</stan_code_to>\r\n");
								}
								if(expression!=null && expression.trim().length()>0)
								{
									valueXmlString.append("<apply_on_exp protect = \"1\">").append(expression).append("</apply_on_exp>\r\n");
								/*
								 * valueXmlString.append("<apply_on_exp_val1 protect = \"0\">").append("").
								 * append("</apply_on_exp_val1>\r\n");
								 * valueXmlString.append("<apply_on_exp_val2 protect = \"0\">").append("").
								 * append("</apply_on_exp_val2>\r\n");
								 */
									
							        // added by adnan 11/11/20
									
									valueXmlString.append("<apply_on_exp_val1 protect = \"0\">").append("<![CDATA[100]]>").append("</apply_on_exp_val1>\r\n");
									valueXmlString.append("<apply_on_exp_val2 protect = \"0\">").append("<![CDATA[100]]>").append("</apply_on_exp_val2>\r\n");		
									
									
									
									
								
								}
								else
								{

									valueXmlString.append("<apply_on_exp protect = \"1\">").append("").append("</apply_on_exp>\r\n");
									valueXmlString.append("<apply_on_exp_val1 protect = \"1\">").append("").append("</apply_on_exp_val1>\r\n");
									valueXmlString.append("<apply_on_exp_val2 protect = \"1\">").append("").append("</apply_on_exp_val2>\r\n");
								
								
								}
							}
						}
					
					}
					else if(currentColumn.equalsIgnoreCase("tran_id"))
					{
						int countVal=0;
						tranId = checkNull(getColumnValue("tran_id", dom));
						sql = "select tran_date,rule_code,valid_from,valid_to,active,carry_data,add_date,add_term,add_user,chg_date,chg_term," +
								"chg_user,item_code_from,item_code_to,item_ser_from,item_ser_to,cust_code_from,cust_code_to,apply_on_exp,stan_code_from,stan_code_to," +
								"apply_on_exp_val1,apply_on_exp_val2 from es3_rule_hdr where tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							countVal++;
							ruleCode = checkNull(rs.getString("rule_code"));
							active = checkNull(rs.getString("active"));
							carryData = checkNull(rs.getString("carry_data"));
							addTerm = checkNull(rs.getString("add_term"));
							addUser = checkNull(rs.getString("add_user"));
							chgTerm = checkNull(rs.getString("chg_term"));
							chgUser = checkNull(rs.getString("chg_user"));
							tranDate = rs.getTimestamp("tran_date");
							validFrom = rs.getTimestamp("valid_from");
							validTo = rs.getTimestamp("valid_to");
							addDate = rs.getTimestamp("add_date");
							chgDate = rs.getTimestamp("chg_date");
							itemCodeFrom=checkNull(rs.getString("item_code_from"));
							itemCodeTo=checkNull(rs.getString("item_code_to"));
							itemSerFrom=checkNull(rs.getString("item_ser_from"));
							itemSerTo=checkNull(rs.getString("item_ser_to"));
							custCodeFrom=checkNull(rs.getString("cust_code_from"));
							custCodeTo=checkNull(rs.getString("cust_code_to"));
							stanCodeFrom=checkNull(rs.getString("stan_code_from"));
							stanCodeTo=checkNull(rs.getString("stan_code_to"));
							applyOnExp=checkNull(rs.getString("apply_on_exp"));
							applyOnExpVal1=checkNull(rs.getString("apply_on_exp_val1"));
							applyOnExpVal2=checkNull(rs.getString("apply_on_exp_val2"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select descr  from tax_balance_grp where bal_group = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ruleCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ruleDescr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("@S@ruleDescr["+ruleDescr+"]");
						
						if(tranDate!=null && tranDate.toString().trim().length()>0)
						{
							tranDateStr = simpleDateFormat.format(tranDate);
						}
						else
						{
							tranDateStr = currDate;
						}
						if(validFrom!=null && validFrom.toString().trim().length()>0)
						{
							validFromStr = simpleDateFormat.format(validFrom);
						}
						if(validTo!=null && validTo.toString().trim().length()>0)
						{
							validToStr = simpleDateFormat.format(validTo);
						}
						if(addDate!=null && addDate.toString().trim().length()>0)
						{
							addDateStr = simpleDateFormat.format(addDate);
						}
						if(chgDate!=null && chgDate.toString().trim().length()>0)
						{
							chgDateStr = simpleDateFormat.format(chgDate);
						}
						
						valueXmlString.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
						valueXmlString.append("<rule_code protect = \"1\">").append("<![CDATA[" + ruleCode + "]]>").append("</rule_code>\r\n");
						valueXmlString.append("<descr protect = \"1\">").append("<![CDATA[" + ruleDescr + "]]>").append("</descr>\r\n");
						valueXmlString.append("<valid_from protect = \"1\">").append("<![CDATA[" + validFromStr + "]]>").append("</valid_from>\r\n");
						valueXmlString.append("<valid_to protect = \"1\">").append("<![CDATA[" + validToStr + "]]>").append("</valid_to>\r\n");
						valueXmlString.append("<active>").append("<![CDATA[" + active + "]]>").append("</active>\r\n");
						valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
						valueXmlString.append("<chg_date>").append("<![CDATA[" + chgDateStr + "]]>").append("</chg_date>\r\n");
						valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>\r\n");
						valueXmlString.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
						valueXmlString.append("<add_date>").append("<![CDATA[" + addDateStr + "]]>").append("</add_date>\r\n");
						valueXmlString.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
						valueXmlString.append("<carry_data>").append("<![CDATA[" + carryData + "]]>").append("</carry_data>\r\n");
						
						if((itemCodeFrom!=null && itemCodeFrom.trim().length()>0) && (itemCodeTo!=null && itemCodeTo.trim().length()>0))
						{
							valueXmlString.append("<item_code_from protect = \"1\">").append(itemCodeFrom).append("</item_code_from>\r\n");
							valueXmlString.append("<item_code_to protect = \"1\">").append(itemCodeTo).append("</item_code_to>\r\n");
						}
						if((itemSerFrom!=null && itemSerFrom.trim().length()>0) && (itemSerTo!=null && itemSerTo.trim().length()>0))
						{
							valueXmlString.append("<item_ser_from protect = \"1\">").append(itemSerFrom).append("</item_ser_from>\r\n");
							valueXmlString.append("<item_ser_to protect = \"1\">").append(itemSerTo).append("</item_ser_to>\r\n");
						}
						if((custCodeFrom!=null && custCodeFrom.trim().length()>0) && (custCodeTo!=null && custCodeTo.trim().length()>0))
						{
							valueXmlString.append("<cust_code_from protect = \"1\">").append(custCodeFrom).append("</cust_code_from>\r\n");
							valueXmlString.append("<cust_code_to protect = \"1\">").append(custCodeTo).append("</cust_code_to>\r\n");
								
						}
						if((stanCodeFrom!=null && stanCodeFrom.trim().length()>0) && (stanCodeTo !=null && stanCodeTo.trim().length()>0))
						{
							valueXmlString.append("<stan_code_from protect = \"1\">").append(stanCodeFrom).append("</stan_code_from>\r\n");
							valueXmlString.append("<stan_code_to protect = \"1\">").append(stanCodeTo).append("</stan_code_to>\r\n");
						}
						if(applyOnExp!=null && applyOnExp.trim().length()>0)
						{
							valueXmlString.append("<apply_on_exp protect = \"1\">").append(applyOnExp).append("</apply_on_exp>\r\n");
							valueXmlString.append("<apply_on_exp_val1 protect = \"1\">").append(applyOnExpVal1).append("</apply_on_exp_val1>\r\n");
							valueXmlString.append("<apply_on_exp_val2 protect = \"1\">").append(applyOnExpVal2).append("</apply_on_exp_val2>\r\n");
						}
						//To make disable the fields and blank
						if((itemCodeFrom==null || itemCodeFrom.trim().length()==0) && (itemCodeTo==null || itemCodeTo.trim().length()==0))
						{
							valueXmlString.append("<item_code_from protect = \"1\">").append("<![CDATA[]]>").append("</item_code_from>\r\n");
							valueXmlString.append("<item_code_to protect = \"1\">").append("<![CDATA[]]>").append("</item_code_to>\r\n");
						}
						if((itemSerFrom==null || itemSerFrom.trim().length()==0) && (itemSerTo==null || itemSerTo.trim().length()==0))
						{
							valueXmlString.append("<item_ser_from protect = \"1\">").append("<![CDATA[]]>").append("</item_ser_from>\r\n");
							valueXmlString.append("<item_ser_to protect = \"1\">").append("<![CDATA[]]>").append("</item_ser_to>\r\n");
						}
						if((custCodeFrom==null || custCodeFrom.trim().length()==0) && (custCodeTo== null || custCodeTo.trim().length()==0))
						{
						valueXmlString.append("<cust_code_from protect = \"1\">").append("<![CDATA[]]>").append("</cust_code_from>\r\n");
						valueXmlString.append("<cust_code_to protect = \"1\">").append("<![CDATA[]]>").append("</cust_code_to>\r\n");
						}
					
						if((stanCodeFrom == null || stanCodeFrom.trim().length()==0) && (stanCodeTo ==null || stanCodeTo.trim().length()==0))
						{
						valueXmlString.append("<stan_code_from protect = \"1\">").append("<![CDATA[]]>").append("</stan_code_from>\r\n");
						valueXmlString.append("<stan_code_to protect = \"1\">").append("<![CDATA[]]>").append("</stan_code_to>\r\n");
						}
						if(applyOnExp == null || applyOnExp.trim().length()== 0)
						{
							valueXmlString.append("<apply_on_exp protect = \"1\">").append("<![CDATA[]]>").append("</apply_on_exp>\r\n");
							valueXmlString.append("<apply_on_exp_val1 protect = \"1\">").append("<![CDATA[]]>").append("</apply_on_exp_val1>\r\n");
							valueXmlString.append("<apply_on_exp_val2 protect = \"1\">").append("<![CDATA[]]>").append("</apply_on_exp_val2>\r\n");
						}
						
						if(countVal==0)
						{
							valueXmlString.append("<tran_date>").append("<![CDATA[" + tranDateStr + "]]>").append("</tran_date>\r\n");
							valueXmlString.append("<rule_code protect = \"0\">").append("<![CDATA[]]>").append("</rule_code>\r\n");
							valueXmlString.append("<descr protect = \"0\">").append("<![CDATA[]]>").append("</descr>\r\n");
							valueXmlString.append("<valid_from protect = \"0\">").append("<![CDATA[" + currDate + "]]>").append("</valid_from>\r\n");
							valueXmlString.append("<valid_to protect = \"0\">").append("<![CDATA[" + currDate + "]]>").append("</valid_to>\r\n");
							valueXmlString.append("<carry_data>").append("<![CDATA[N]]>").append("</carry_data>\r\n");
							valueXmlString.append("<active>").append("<![CDATA[Y]]>").append("</active>\r\n");
						}
						
					}
					else if(currentColumn.equalsIgnoreCase("item_code_from"))
					{
						itemCodeFrom = checkNull(getColumnValue("item_code_from", dom));
						if(itemCodeFrom!=null && itemCodeFrom.trim().length()>0)
						{
							if("00".equalsIgnoreCase(itemCodeFrom))
							{
								valueXmlString.append("<item_code_to protect = \"1\">").append("<![CDATA[ZZ]]>").append("</item_code_to>\r\n");
							}
							else
							{
								valueXmlString.append("<item_code_to protect = \"0\">").append("").append("</item_code_to>\r\n");
							}
						}
					}
					else if(currentColumn.equalsIgnoreCase("item_ser_from"))
					{
						itemSerFrom = checkNull(getColumnValue("item_ser_from", dom));
						if(itemSerFrom!=null && itemSerFrom.trim().length()>0)
						{
							if("00".equalsIgnoreCase(itemSerFrom))
							{
								valueXmlString.append("<item_ser_to protect = \"1\">").append("<![CDATA[ZZ]]>").append("</item_ser_to>\r\n");
							}
							else
							{
								valueXmlString.append("<item_ser_to protect = \"0\">").append("").append("</item_ser_to>\r\n");
							}
						}
					}
					else if(currentColumn.equalsIgnoreCase("cust_code_from"))
					{
						custCodeFrom = checkNull(getColumnValue("cust_code_from", dom));
						if(custCodeFrom!=null && custCodeFrom.trim().length()>0)
						{
							if("00".equalsIgnoreCase(custCodeFrom))
							{
								valueXmlString.append("<cust_code_to protect = \"1\">").append("<![CDATA[ZZ]]>").append("</cust_code_to>\r\n");
							}
							else
							{
								valueXmlString.append("<cust_code_to protect = \"0\">").append("").append("</cust_code_to>\r\n");
							}
						}
					}
					else if(currentColumn.equalsIgnoreCase("stan_code_from"))
					{
						stanCodeFrom = checkNull(getColumnValue("stan_code_from", dom));
						if(stanCodeFrom!=null && stanCodeFrom.trim().length()>0)
						{
							if("00".equalsIgnoreCase(stanCodeFrom))
							{
								valueXmlString.append("<stan_code_to protect = \"1\">").append("<![CDATA[ZZ]]>").append("</stan_code_to>\r\n");
							}
							else
							{
								valueXmlString.append("<stan_code_to protect = \"0\">").append("").append("</stan_code_to>\r\n");
							}
						}
						
					}
					else if(currentColumn.equalsIgnoreCase("apply_on_exp_val1"))
					{
						applyOnExp = checkNull(getColumnValue("apply_on_exp", dom));
						applyOnExpVal1 = checkNull(getColumnValue("apply_on_exp_val1", dom));
						System.out.println("@S@>> cline No 425["+applyOnExp+"]applyOnExpVal1["+applyOnExpVal1+"]");
						if((applyOnExpVal1!=null && applyOnExpVal1.trim().length()>0) && "%".equalsIgnoreCase(applyOnExp))
						{
							int applyOnExpVal1Int = Integer.parseInt(applyOnExpVal1);
							System.out.println(">>>>>["+applyOnExpVal1Int+"]");
						/*
						 * if(applyOnExpVal1Int==100) {
						 * valueXmlString.append("<apply_on_exp_val2 protect = \"1\">").append(
						 * "<![CDATA[100]]>").append("</apply_on_exp_val2>\r\n"); } else {
						 * valueXmlString.append("<apply_on_exp_val2 protect = \"0\">").append("").
						 * append("</apply_on_exp_val2>\r\n"); }
						 */
						}

					}
					valueXmlString.append("</Detail1>");
					break;
			}
			valueXmlString.append("</Root>");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} 
		finally 
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
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}
		System.out.println("valueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
			}
	private String checkNull(String input) 
	{
		if (input == null) 
		{
			input = "";
		} 
		else 
		{
			input = input.trim();
		}
		return input;
	}
	@Override
	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		try 
		{
			System.out.println("xmlString"+xmlString);
			System.out.println("xmlString1"+xmlString1);
			System.out.println("xmlString2"+xmlString2);
			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0) 
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) 
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : FinEntAcctPrd : wfValData(String xmlString) : ==>" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException 
			{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int currentFormNo = 0, childNodeListLength = 0;
		int count = 0,countRuleCode = 0;
		String childNodeName = null;
		String errString = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String tranId="",tranDateStr="",ruleCode="",validFromStr="",validToStr="",active="",carryData="";
		String sql = "",userId="";
		String itemCodeFrom="",itemCodeTo="",itemSerFrom="",itemSerTo="",custCodeFrom="",custCodeTo="",stanCodeFrom="",stanCodeTo="";
		String applyOnExp="",applyOnExpVal1="",applyOnExpVal2="";
		Timestamp validTo = null;
		Timestamp validFrom = null;
		String  childNodeValue = "";
		String msgType = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		ArrayList<String> itemSerList = new ArrayList<String>();
		boolean itemSerValPres= false;
		
		try 
		{
			ConnDriver connDriver = new ConnDriver();
			/* conn = connDriver.getConnectDB("DriverITM"); */
			conn = getConnection();
			conn.setAutoCommit(false);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (int ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					if (childNode.getNodeType() != Node.ELEMENT_NODE) 
					{
						continue;
					}
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName>>"+childNodeName);
					if(childNode.getFirstChild()!=null)
					{
						childNodeValue = childNode.getFirstChild().getNodeValue();
						System.out.println("@S@columnValue>>"+childNodeValue);
					}
					else
					{
						childNodeValue = childNode.getNodeValue();
						System.out.println("@S@columnValue1>>"+childNodeValue);
					}
					if(childNodeName.equalsIgnoreCase("tran_id"))
					{
						tranId = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("tran_date"))
					{
						tranDateStr = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("rule_code"))
					{
						ruleCode = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("valid_from"))
					{
						validFromStr = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("valid_to"))
					{
						validToStr = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("active"))
					{
						active = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("carry_data"))
					{
						carryData = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("item_code_from"))
					{
						itemCodeFrom = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("item_code_to"))
					{
						itemCodeTo = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("item_ser_from"))
					{
						itemSerFrom = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("item_ser_to"))
					{
						itemSerTo = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("cust_code_from"))
					{
						custCodeFrom = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("cust_code_to"))
					{
						custCodeTo = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("apply_on_exp"))
					{
						applyOnExp = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("apply_on_exp_val1"))
					{
						applyOnExpVal1 = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("apply_on_exp_val2"))
					{
						applyOnExpVal2 = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("stan_code_from"))
					{
						stanCodeFrom = childNodeValue;
					}
					else if(childNodeName.equalsIgnoreCase("stan_code_to"))
					{
						stanCodeTo = childNodeValue;
					}
					
				   System.out.println("childNodeName.editFlag." + childNodeName + ".." + editFlag);
					
					if (childNodeName.equalsIgnoreCase("tran_id")) 
					{
						if(tranId!= null && tranId.trim().length()>0)
						{
							sql = "select count(*) as cnt from es3_rule_hdr where tran_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(count==0)
							{
								errList.add("VTES3TRAID");
								errFields.add(childNodeName.toLowerCase());

								msgType = errorType(conn, "VTES3TRAID");
								if (msgType.equalsIgnoreCase("E")) 
								{
									break;
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("rule_code")) 
					{
						if(ruleCode!= null && ruleCode.trim().length()>0)
						{
							System.out.println("@S@ruleCode 435 ["+ruleCode+"]");
							sql = "select count(*) as cnt from tax_bal_grp_det where bal_group = ? and ref_ser = 'C-ES3' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ruleCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								countRuleCode = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(countRuleCode==0)
							{
								errList.add("VTES3RULCD");
								errFields.add(childNodeName.toLowerCase());

								msgType = errorType(conn, "VTES3RULCD");
								if (msgType.equalsIgnoreCase("E")) 
								{
									break;
								}
							}
							
						}
						else
						{
							errList.add("VTES3BLKRC");
							errFields.add(childNodeName.toLowerCase());

							msgType = errorType(conn, "VTES3BLKRC");
							if (msgType.equalsIgnoreCase("E")) 
							{
								break;
							}
						}
					}
					/*else if (childNodeName.equalsIgnoreCase("valid_from")) 
					{
						System.out.println("@S@ inside FromDate["+validFromStr+"]");
						if (validFromStr == null || validFromStr.trim().length() == 0) 
						{
							errList.add("VTES3VAFBK");
							errFields.add(childNodeName.toLowerCase());

							msgType = errorType(conn, "VTES3VAFBK");
							if (msgType.equalsIgnoreCase("E")) 
							{
								break;
							}
						}
						
					}
					else if (childNodeName.equalsIgnoreCase("valid_to")) 
					{
						System.out.println("@S@ inside FromDate["+validToStr+"]");
						if (validToStr == null || validToStr.trim().length() == 0) 
						{
							errList.add("VTES3VATBK");
							errFields.add(childNodeName.toLowerCase());

							msgType = errorType(conn, "VTES3VATBK");
							if (msgType.equalsIgnoreCase("E")) 
							{
								break;
							}
						}
						else
						{
							if((validFromStr!=null && validFromStr.trim().length()>0) && (validToStr!=null && validToStr.trim().length()>0))
							{
								try
								{
								validFrom = Timestamp.valueOf(genericUtility.getValidDateTimeString(validFromStr, getApplDateFormat(), getDBDateFormat()));
								}
								catch(Exception e)
								{
									errList.add("VTES3INVFD");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTES3INVFD");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
								try
								{
								validTo = Timestamp.valueOf(genericUtility.getValidDateTimeString(validToStr, getApplDateFormat(), getDBDateFormat()));
								}
								catch(Exception e)
								{
									errList.add("VTES3INVTD");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTES3INVTD");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
								if(validTo.before(validFrom))
								{
									errList.add("VTES3VTLVF");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTES3VTLVF");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
							}
						}
						
					}
*/					else if (childNodeName.equalsIgnoreCase("item_code_from"))
					{
						int cnt=0;
						if(itemCodeFrom!=null && itemCodeFrom.trim().length()>0)
						{
							if(!"00".equalsIgnoreCase(itemCodeFrom))
							{
								sql="select count (*) as cnt  from item where item_code = ? and active = 'Y' and item_usage='F' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCodeFrom);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errList.add("VTINVITCOF");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTINVITCOF");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
								
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("item_code_to"))
					{
						int cnt=0;
						if(itemCodeTo!=null && itemCodeTo.trim().length()>0)
						{
							if(!"ZZ".equalsIgnoreCase(itemCodeTo))
							{
								sql="select count (*) as cnt  from item where item_code = ? and active = 'Y' and item_usage='F' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCodeTo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errList.add("VTINVITCOT");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTINVITCOT");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
								
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("cust_code_from"))
					{
						int cnt=0;
						if(custCodeFrom!=null && custCodeFrom.trim().length()>0)
						{
							if(!"00".equalsIgnoreCase(custCodeFrom))
							{
								sql="select count (*) as cnt  from customer where cust_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCodeFrom);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errList.add("VTINVCUSTF");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTINVCUSTF");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
								
							}
						}
					
						
					}
					else if (childNodeName.equalsIgnoreCase("cust_code_to"))
					{
						int cnt=0;
						if(custCodeTo!=null && custCodeTo.trim().length()>0)
						{
							if(!"ZZ".equalsIgnoreCase(custCodeTo))
							{
								sql="select count (*) as cnt  from customer where cust_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCodeTo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errList.add("VTINVCUSTT");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTINVCUSTT");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}

							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("stan_code_from"))
					{
						int cnt=0;
						if(stanCodeFrom!=null && stanCodeFrom.trim().length()>0)
						{
							sql="select count (*) as cnt  from station where stan_code = ? and active = 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,stanCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errList.add("VTINVSTANF");
								errFields.add(childNodeName.toLowerCase());

								msgType = errorType(conn, "VTINVSTANF");
								if (msgType.equalsIgnoreCase("E")) 
								{
									break;
								}
							}
							else
							{
								String stanCode="";
								/*
								 * if((custCodeFrom!= null && custCodeFrom.trim().length()>0) &&
								 * (!"R0002".equalsIgnoreCase(ruleCode) && !"R0007".equalsIgnoreCase(ruleCode)
								 * && !"R0001".equalsIgnoreCase(ruleCode)))
								 */
								if((custCodeFrom!= null && custCodeFrom.trim().length()>0) 
										&& (!"ES3R2".equalsIgnoreCase(ruleCode) && !"ES3R7".equalsIgnoreCase(ruleCode) && !"ES3R1".equalsIgnoreCase(ruleCode)))
								
								{
									sql="select stan_code from customer where cust_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCodeFrom);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										stanCode = checkNull(rs.getString("stan_code"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(!stanCode.equalsIgnoreCase(stanCodeFrom) && !"00".equalsIgnoreCase(custCodeFrom))
									{
										errList.add("VTMISSTANC");
										errFields.add(childNodeName.toLowerCase());

										msgType = errorType(conn, "VTMISSTANC");
										if (msgType.equalsIgnoreCase("E")) 
										{
											break;
										}


									}
								}
							}

						}
					}
					else if (childNodeName.equalsIgnoreCase("stan_code_to"))
					{
						int cnt=0;
						if(stanCodeTo!=null && stanCodeTo.trim().length()>0)
						{
							sql="select count (*) as cnt  from station where stan_code = ? and active = 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,stanCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errList.add("VTINVSTANT");
								errFields.add(childNodeName.toLowerCase());

								msgType = errorType(conn, "VTINVSTANT");
								if (msgType.equalsIgnoreCase("E")) 
								{
									break;
								}
							}
							else
							{

							}

						}
					}
					else if (childNodeName.equalsIgnoreCase("item_ser_from"))
					{
						int cnt=0;
						if(itemSerFrom!=null && itemSerFrom.trim().length()>0)
						{
							//if(!"00".equalsIgnoreCase(itemSerFrom) || !"R0002".equalsIgnoreCase(ruleCode) || !"R0007".equalsIgnoreCase(ruleCode) || !"R0001".equalsIgnoreCase(ruleCode))
							if(!"00".equalsIgnoreCase(itemSerFrom) ||( !"ES3R2".equalsIgnoreCase(ruleCode) && !"ES3R7".equalsIgnoreCase(ruleCode) && !"ES3R1".equalsIgnoreCase(ruleCode)))
							{
								sql="select count (*) as cnt from  itemser  where item_ser = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemSerFrom);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errList.add("VTINVITMSF");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTINVITMSF");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
								else
								{
									String itemSer="";
									if(((custCodeFrom!= null && custCodeFrom.trim().length()>0) ||(itemCodeFrom!= null && itemCodeFrom.trim().length()>0)) 
										&& (!"ES3R2".equalsIgnoreCase(ruleCode) && !"ES3R7".equalsIgnoreCase(ruleCode) && !"ES3R1".equalsIgnoreCase(ruleCode)))
									{
										
										//sql="select item_ser from  item  where item_code = ? ";
										sql="select item_ser  from item where item_code = ? and active = 'Y' and item_usage='F' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,itemCodeFrom);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											itemSer = checkNull(rs.getString("item_ser"));
											itemSerValPres= true;
										}
										else
										{
											pstmt.clearParameters();
											rs.close();
											rs= null;
											
											sql="select item_ser from customer_series where cust_code = ? ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,custCodeFrom);
											rs = pstmt.executeQuery();
											while (rs.next())
											{
												itemSer = checkNull(rs.getString("item_ser"));
												itemSerList.add(checkNull(rs.getString("item_ser")));
											}
											
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										System.out.println("@S@>>> 1126"+itemSerList);
										if(itemSerList!=null){
											for (String string : itemSerList) {
												if (string.contains(itemSerFrom)) 
													itemSerValPres= true;
											}
										}
										System.out.println("@S@>>> 1136"+itemSerValPres);
										if(!itemSer.equalsIgnoreCase(itemSerFrom) || !itemSerValPres) 
										{
											if ((itemCodeFrom!=null && itemCodeFrom.trim().length()>0 && !"00".equalsIgnoreCase(itemCodeFrom)) 
													|| (custCodeFrom!=null && custCodeFrom.trim().length()>0 && !"00".equalsIgnoreCase(custCodeFrom)))
											{
												errList.add("VTMISSITMS");
												errFields.add(childNodeName.toLowerCase());

												msgType = errorType(conn, "VTMISSITMS");
												if (msgType.equalsIgnoreCase("E")) 
													break;
											}
											
										}
									}
								}

							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("item_ser_to"))
					{
						int cnt=0;
						if(itemSerTo!=null && itemSerTo.trim().length()>0)
						{
							if(!"ZZ".equalsIgnoreCase(itemSerTo) || (!"ES3R2".equalsIgnoreCase(ruleCode) && !"ES3R7".equalsIgnoreCase(ruleCode) && !"ES3R1".equalsIgnoreCase(ruleCode)))
							{
								sql="select count (*) as cnt from  itemser  where item_ser = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemSerTo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errList.add("VTINVITMST");
									errFields.add(childNodeName.toLowerCase());

									msgType = errorType(conn, "VTINVITMST");
									if (msgType.equalsIgnoreCase("E")) 
									{
										break;
									}
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("apply_on_exp_val1"))
					{
						if((applyOnExp!=null && applyOnExp.trim().length()>0 ) &&(applyOnExpVal1== null || applyOnExpVal1.trim().length()==0)) 
						{
							errList.add("VTINVEXPV1");
							errFields.add(childNodeName.toLowerCase());

							msgType = errorType(conn, "VTINVEXPV1");
							if (msgType.equalsIgnoreCase("E")) 
							{
								break;
							}
							
						}
					}
					else if (childNodeName.equalsIgnoreCase("apply_on_exp_val2"))
					{
						int applyOnExpVal1Int=0,applyOnExpVal2Int=0 ,applyOnExpTot=0;
						System.out.println("@S@> 1193 applyOnExp["+applyOnExp+"][applyOnExpVal1]["+applyOnExpVal1+"][applyOnExpVal2]["+applyOnExpVal2+"]");
						if((applyOnExpVal1!=null && applyOnExpVal1.trim().length()>0) && (applyOnExpVal2== null || applyOnExpVal2.trim().length()==0))
						{
							errList.add("VTINVEXPV2");
							errFields.add(childNodeName.toLowerCase());

							msgType = errorType(conn, "VTINVEXPV2");
							if (msgType.equalsIgnoreCase("E")) 
							{
								break;
							}
						}
						else if((applyOnExpVal2!=null && applyOnExpVal2.trim().length()>0)&&((applyOnExpVal1!=null && applyOnExpVal1.trim().length()>0))
								&&(("%".equalsIgnoreCase(applyOnExp))))
						{
							applyOnExpVal1Int=	Integer.parseInt(applyOnExpVal1);
							applyOnExpVal2Int =	Integer.parseInt(applyOnExpVal2);
							/*
							 * if(applyOnExpVal1Int!=100 && applyOnExpVal2Int!=100) {
							 * applyOnExpTot=applyOnExpVal1Int+applyOnExpVal2Int; if(applyOnExpTot!=100) {
							 * errList.add("VTINVEXPVT"); errFields.add(childNodeName.toLowerCase());
							 * 
							 * msgType = errorType(conn, "VTINVEXPVT"); if (msgType.equalsIgnoreCase("E")) {
							 * break; } } }
							 */
							
						//added by adnan 	11/11/20
							
						if(applyOnExpVal1Int>100 || applyOnExpVal1Int<0 || applyOnExpVal2Int>100 ||applyOnExpVal2Int<0  ) {
							errList.add("VTINVEXPVT");
							errFields.add(childNodeName.toLowerCase());

							msgType = errorType(conn, "VTINVEXPVT");
							if (msgType.equalsIgnoreCase("E")) {
								break;
							}
						}	
							

						}
					}

						
				}
				break;
			}
			int errListSize = errList.size();
			StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");

			if (errList != null && errListSize > 0) 
			{
				for (int cnt = 0; cnt < errListSize; cnt++) 
				{
					String errCode = (String) errList.get(cnt);
					String errFldName = (String) errFields.get(cnt);
					System.out.println("errCode .........." + errCode);

					errString = getErrorString(errFldName, errCode, userId);
					System.out.println("errString is : ::::>>>> " + errString);
					msgType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........." + errStringXml);
						errString = "";
					}
					if (msgType.equalsIgnoreCase("E")) 
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;

				errStringXml.append("</Errors></Root>\r\n");
			} 
			else 
			{
				errStringXml = new StringBuffer("");
			}
			errString = errStringXml.toString();
		}catch (Exception e) 
		{
			System.out.println("Exception ::" + e.getMessage());
			e.printStackTrace();
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		} 
		finally 
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
				if (conn != null) 
				{
					conn.close();
				}
				conn = null;
			} 
			catch (Exception d) 
			{
				d.printStackTrace();
			}
		}
		System.out.println("ErrString ::" + errString);
		return errString;

			}
	private String errorType(Connection conn, String errorCode) throws ITMException 
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				msgType = rs.getString("MSG_TYPE");
			}
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		} 
		finally 
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
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}
	


}
