
/********************************************************
	Title : Supplier
	Date  : 30/09/14
	Developer: Sagar Mane

 ********************************************************/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException; 

@Stateless 
public class Supplier extends ValidatorEJB implements SupplierLocal,SupplierRemote {
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String suppName = "";
		String suppType = "";
		String groupCode = "";
		String keyFlag = "";
		String payTo = "";
		String suppCode = "";
		String currCode = "";
		String currCode1 = "";
		String stanCode = "";
		String sundryType = "";
		String taxChap = "";
		String taxClass = "";
		String stateCode = "";
		String contactCode = "";
		String acctCode = "";
		String siteCode = "";
		String channelPartner = "";
		String cctrCode = "";
		String crTerm = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String regCode="",existFlag="",regDate="",validUpto="",dlvTerm="",lockGroup="",tranCode="",siteCodePay="",taxEnv="",siteCodeBus="";
        // Change By ANIK 25TH-JAN-2021 START;
        String mobile_no_tax_reg = "" ;
        String tele1 = "";
        String tele2 = "";
        String tele3 = "";
        String alt_tele1 = "";
        // Change By ANIK 25TH-JAN-2021 START;
		String pin="",countCode = "",pin_pattern="";
        int ctr=0;
		int childNodeListLength;
		long count = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		String commTable = "";
		String labOurInvolved = "", pfRegNo = "", lwfNo = "", pTaxNo = "", eciNo = "",esicRegNo = "", ssi = "", msmeRegDate = "", msmeRegNo = "", msmeType = "";//Changed By Nasruddin 21-SEp-16
		try
		{
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println(">>>>>>>>>>>>>>>>currentFormNo In validation:"+currentFormNo);
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					
					if(childNodeName.equalsIgnoreCase("supp_code"))
					{    
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						keyFlag = findValue(conn, "key_flag", "transetup", "tran_window", "w_supplier");
						if(!("A".equalsIgnoreCase(keyFlag)) && (suppCode == null || suppCode.trim().length() == 0)) 
						{
							errCode = "VTSUPCDNLL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(editFlag.equals("A"))
						{
							if(suppCode!= null && suppCode.trim().length() > 0)
							{
								existFlag = isExist("supplier", "supp_code", suppCode, conn);
								if ("TRUE".equals(existFlag))
								{
									errCode = "VTSUPCDVLD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("supp_name"))
					{    
						suppName = genericUtility.getColumnValue("supp_name", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						if(suppName == null || suppName.trim().length() == 0)
						{
							errCode = "VTSUPNMNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*) from supplier where supp_code <> ? and supp_name = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, suppCode);
							pstmt.setString(2, suppName);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							//Added by Jaffar on 06-04-18 ---> Start
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//---- End ----
							
							if(count > 0)
							{
								errCode = "VTSUPNMVLD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("supp_type"))
					{    
						suppType = genericUtility.getColumnValue("supp_type", dom);
						if(suppType == null || suppType.trim().length() == 0)
						{
							errCode = "VMSUPPTP";      
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("group_code"))
					{
						groupCode = genericUtility.getColumnValue("group_code", dom);
						keyFlag = findValue(conn, "key_flag", "transetup", "tran_window", "w_supplier");
						if(!("A".equalsIgnoreCase(keyFlag)) && (groupCode == null || groupCode.trim().length() == 0)) 
						{
							errCode = "VMGRPCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}		
					}/*
					Changed By Nasruddin 21-SEP-16 STARt
					else if(childNodeName.equalsIgnoreCase("supp_code__pay"))
					{
						System.out.println(">>>>>>>>>>>supp_code__pay validation:");
						payTo = genericUtility.getColumnValue("supp_code__pay", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						keyFlag = findValue(conn, "key_flag", "transetup", "tran_window", "w_supplier");
						if(!("A".equalsIgnoreCase(keyFlag)) && (payTo == null || payTo.trim().length() == 0))
						{
							errCode = "VEPAYTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					    else if(!(payTo.equals(suppCode)))
						{
					    	existFlag = isExist("supplier", "supp_code", payTo, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMPAYTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
					}
					}*/
					else if(childNodeName.equalsIgnoreCase("supp_code__pay"))
					{
						System.out.println(">>>>>>>>>>>supp_code__pay validation:");
						payTo =  checkNull(genericUtility.getColumnValue("supp_code__pay", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						//Modified by Anjali R. on[26/04/2018][To check length without trim function][Start]
						//if(payTo != null && payTo.trim().length() > 0)
						if(payTo != null && payTo.length() > 0)
						//Modified by Anjali R. on[26/04/2018][To check length without trim function][End]
						{
							//Modified by Anjali R. on[26/04/2018][To compare two strings with trim function][Start]
							//if(!suppCode.equalsIgnoreCase(payTo))
							if(!suppCode.trim().equalsIgnoreCase(payTo.trim()))
							//Modified by Anjali R. on[26/04/2018][To compare two strings with trim function][End]
							{
								count = 0;
								sql = "SELECT COUNT(1) FROM SUPPLIER WHERE SUPP_CODE = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, payTo);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									count = rs.getInt(1);
								}
								//Added by Jaffar on 06-04-18 ---> Start
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								//---- End ----

								if (count == 0)
								{
									errCode = "VMCODPAY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}
					}
					//Modified by Anjali R. on[26/04/2018][No such a column as "pay_to" in supplier master ][Start]
					/*else if(childNodeName.equalsIgnoreCase("pay_to"))
					{
						System.out.println(">>>>>>>>>>pay_to validation:");
						payTo = genericUtility.getColumnValue("pay_to", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						keyFlag = findValue(conn, "key_flag", "transetup", "tran_window", "w_supplier");
						if(!("A".equalsIgnoreCase(keyFlag)) && (payTo == null || payTo.trim().length() == 0)) 
						{
							errCode = "VEPAYTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
					    else if(!(payTo.equals(suppCode)))
						{
					    	existFlag = isExist("supplier", "supp_code", payTo, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMPAYTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}*/
					//Modified by Anjali R. on[26/04/2018][No such a column as "pay_to" in supplier master ][End]
					
					//Changed By Nasruddin 21-SEP-16 END
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode =genericUtility.getColumnValue("curr_code", dom);
						/* Comment BY Nasruddin 21-SEP-16 START
						 * if(currCode== null || currCode.trim().length() == 0)
						{
							errCode = "VMNULLCURN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{Comment BY Nasruddin 21-SEP-16 END
						*/
							existFlag = isExist("currency", "curr_code", currCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMCURRCND";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if("E".equalsIgnoreCase(editFlag))
							{
								suppCode = genericUtility.getColumnValue("supp_code", dom);
								currCode1 = findValue(conn, "curr_code", "supplier", "supp_code", suppCode);
								//if((currCode.trim() != currCode1.trim()) && (currCode1 != null && currCode1.trim().length() > 0))
								if((!(currCode.trim().equals(currCode1.trim())) && (currCode1 != null && currCode1.trim().length() > 0)))
								{
									sundryType = "S";
									sql = " Select count(*) from sundrybal where sundry_type = ? and sundry_code = ? and (dr_amt != 0 or cr_amt != 0)";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, sundryType);
									pstmt.setString(2, suppCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									if(count != 0)
									{
										  // Error : Currency cannot be changed, amt exists in sundrybal. 
										errCode = "VXCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						//}
					}
					else if(childNodeName.equalsIgnoreCase("stan_code"))
					{
						stanCode = genericUtility.getColumnValue("stan_code", dom);
						if(stanCode!= null && stanCode.trim().length() > 0)
						{
							existFlag = isExist("station", "stan_code", stanCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMSTAN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						/* Comment BY Nasruddin 21_sep-16 STARt
						else
						{
							errCode = "VMSTANCOD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						 Comment BY Nasruddin 21_sep-16 END*/
						
					}
					else if(childNodeName.equalsIgnoreCase("cr_term"))
					{
						
						crTerm = genericUtility.getColumnValue("cr_term", dom);
						existFlag = isExist("crterm", "cr_term", crTerm, conn);
						if ("FALSE".equals(existFlag))
						{
							errCode = "VMCRTER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						if(taxChap != null && taxChap.trim().length() != 0)
						{
							existFlag = isExist("taxchap", "tax_chap", taxChap, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMTAXCHP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}                       
					else if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						if(taxClass != null && taxClass.trim().length() != 0)
						{
							existFlag = isExist("taxclass", "tax_class", taxClass, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VTTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("acct_code__ap"))
					{
						System.out.println(">>>>>>acct_code__ap:");
						acctCode = genericUtility.getColumnValue("acct_code__ap", dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						errCode = supplier_acct(siteCode, acctCode, conn);
						System.out.println(">>>>>>>>>>acct_code__ap error:"+errCode);
						if(errCode != null && errCode.trim().length() > 0 )
						{
							System.out.println(">>>>>> Addd acct_code__ap error:"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("acct_code__ap_adv"))
					{
						System.out.println(">>>>>>acct_code__ap_adv:");
						acctCode = genericUtility.getColumnValue("acct_code__ap_adv", dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						errCode = supplier_acct(siteCode, acctCode, conn);
						System.out.println(">>>>>>>>>>acct_code__ap_adv error:"+errCode);
						if(errCode != null && errCode.trim().length() > 0 )
						{
							System.out.println(">>>>>> Addd acct_code__ap_adv error:"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code__ap"))
					{
						System.out.println(">>>>>>cctr_code__ap:");
						cctrCode = genericUtility.getColumnValue("cctr_code__ap", dom);
						acctCode = genericUtility.getColumnValue("acct_code__ap",dom);
						if(cctrCode != null && cctrCode.trim().length() > 0)
						{
							errCode = supplier_cctr( acctCode, cctrCode,  conn);
							System.out.println(">>>>>>>>>>cctr_code__ap error:"+errCode);
							if(errCode != null && errCode.trim().length() > 0 )
							{
								System.out.println(">>>>>> Addd cctr_code__ap error:"+errCode);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code__ap_adv"))
					{
						System.out.println(">>>>>>cctr_code__ap_adv:");
						cctrCode = genericUtility.getColumnValue("cctr_code__ap_adv", dom);
						acctCode = genericUtility.getColumnValue("acct_code__ap_adv",dom);
						if(cctrCode != null && cctrCode.trim().length() > 0)
						{
							errCode = supplier_cctr( acctCode, cctrCode,  conn);
							System.out.println(">>>>>>>>>>cctr_code__ap_adv error:"+errCode);
							if(errCode != null && errCode.trim().length() > 0 )
							{
								System.out.println(">>>>>> Addd cctr_code__ap_adv error:"+errCode);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("state_code__alt"))
					{
						System.out.println(">>>>>>state_code__alt:");
						stateCode = genericUtility.getColumnValue("state_code__alt", dom);
						if(stateCode != null && stateCode.trim().length() != 0)
						{
							existFlag = isExist("state", "state_code", stateCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMSTATCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("contact_code"))
					{
						contactCode = genericUtility.getColumnValue("contact_code", dom);
						existFlag = isExist("contact", "contact_code", contactCode, conn);
						if ("FALSE".equals(existFlag))
						{
							errCode = "VMCONTCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("site_code"))
					{
						siteCode = genericUtility.getColumnValue("site_code", dom);
						channelPartner = genericUtility.getColumnValue("channel_partner", dom);
						if("Y".equalsIgnoreCase(channelPartner) && (siteCode == null || siteCode.trim().length() == 0))
						{
							errCode = "VMSITECD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());							
						}
						if(siteCode != null && siteCode.trim().length() > 0)
						{
							existFlag = isExist("site", "site_code", siteCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMSITECDX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					/* Comment By Nasruddin 21-SEP-16 START
					else if(childNodeName.equalsIgnoreCase("site_code__pay"))
					{
						siteCodePay = genericUtility.getColumnValue("site_code__pay", dom);
						if(siteCodePay != null && siteCodePay.trim().length() > 0)
						{
							existFlag = isExist("site", "site_code", siteCodePay, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMPYSITECD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}Comment By Nasruddin 21-SEP-16 END
					*/
					else if(childNodeName.equalsIgnoreCase("dlv_term"))
					{
						dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
						if(dlvTerm != null && dlvTerm.trim().length() > 0)
						{
							existFlag = isExist("delivery_term", "dlv_term", dlvTerm, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMDLVTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("lock_group"))
					{
						lockGroup = genericUtility.getColumnValue("lock_group", dom);
						if(lockGroup != null && lockGroup.trim().length() > 0)
						{
							existFlag = isExist("lock_group", "lock_group", lockGroup, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMLOCKCODE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					/* Comment By Nasruddin  21-SEp-16 START
					 * 
					else if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = genericUtility.getColumnValue("tran_code", dom);
						if(tranCode != null && tranCode.trim().length() > 0)
						{
							existFlag = isExist("transporter", "tran_code", tranCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "INVTRANPRT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				
					else if(childNodeName.equalsIgnoreCase("tax_env"))
					{
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						if(taxEnv != null && taxEnv.trim().length() > 0)
						{
							existFlag = isExist("taxenv", "tax_env", taxEnv, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "INTAXENV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("site_code__pbus"))
					{
						siteCodeBus = genericUtility.getColumnValue("site_code__pbus", dom);
						if(siteCodeBus != null && siteCodeBus.trim().length() > 0)
						{
							existFlag = isExist("site", "site_code", siteCodeBus, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMSTEBUSCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}Comment By Nasruddin  21-SEp-16 end*/
					//Changed By Nasruddin khan [19/JUL/16] START
					else if( childNodeName.equalsIgnoreCase("comm_table"))
					{
						
						commTable = checkNull(this.genericUtility.getColumnValue("comm_table", dom));
						
						if( commTable != null && commTable.trim().length() > 0){
							count = 0;
							sql = "select count(1) as count from  comm_hdr where comm_table = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, commTable);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								count = rs.getInt("count");
							}
							//Added by Jaffar on 06-04-18 ---> Start
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//---- End ----

							if (count == 0)
							{
								errCode = "VMCOMMTBCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						
						}
					}//Changed By Nasruddin khan [19/JUL/16] END
					
					// Changed By Nasruddin Start 21-SEP-16 START
					else if(childNodeName.equalsIgnoreCase("pf_reg_no"))
					{
						labOurInvolved = genericUtility.getColumnValue("labour_involved", dom);
						if("Y".equals(labOurInvolved))
						{
							pfRegNo = genericUtility.getColumnValue("pf_reg_no", dom);
							if(pfRegNo == null || pfRegNo.trim().length() == 0 )
							{
								errCode = "VMSUPHRD01";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("esic_reg_no"))
					{
						labOurInvolved = genericUtility.getColumnValue("labour_involved", dom);
						if("Y".equals(labOurInvolved))
						{
							esicRegNo = genericUtility.getColumnValue("esic_reg_no", dom);
							if(esicRegNo == null || esicRegNo.trim().length() == 0 )
							{
								errCode = "VMSUPHRD02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("eci_no"))
					{
						labOurInvolved = genericUtility.getColumnValue("labour_involved", dom);
						if("Y".equals(labOurInvolved))
						{
							eciNo = genericUtility.getColumnValue("eci_no", dom);
							if(eciNo == null || eciNo.trim().length() == 0 )
							{
								errCode = "VMSUPHRD03";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("ptax_no"))
					{
						labOurInvolved = genericUtility.getColumnValue("labour_involved", dom);
						if("Y".equals(labOurInvolved))
						{
							pTaxNo = genericUtility.getColumnValue("ptax_no", dom);
							if(pTaxNo == null || pTaxNo.trim().length() == 0 )
							{
								errCode = "VMSUPHRD04";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("lwf_no"))
					{
						labOurInvolved = genericUtility.getColumnValue("labour_involved", dom);
						if("Y".equals(labOurInvolved))
						{
							lwfNo = genericUtility.getColumnValue("lwf_no", dom);
							if(lwfNo == null || lwfNo.trim().length() == 0 )
							{
								errCode = "VMSUPHRD05";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("msme_type"))
					{
						ssi = genericUtility.getColumnValue("ssi", dom);
						if("Y".equals(ssi))
						{
							msmeType = genericUtility.getColumnValue("msme_type", dom);
							if(msmeType == null || msmeType.trim().length() == 0 )
							{
								errCode = "VMSUPSSI02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("msme_reg_no"))
					{
						ssi = genericUtility.getColumnValue("ssi", dom);
						if("Y".equals(ssi))
						{
							msmeRegNo = genericUtility.getColumnValue("msme_reg_no", dom);
							if(msmeRegNo == null || msmeRegNo.trim().length() == 0 )
							{
								errCode = "VMSUPSSI03";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Added by Shubham.S.B on 01-02-2021
					// pinPattern Validation
					else if (childNodeName.equalsIgnoreCase("pin"))
					{
						pin = checkNull(genericUtility.getColumnValue("pin", dom));
						countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
						
						sql = "select pin_pattern from country where count_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, countCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							pin_pattern = checkNull(rs.getString("pin_pattern"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("pin_pattern is:::::"+pin_pattern);
						if (pin != null && pin.trim().length() > 0)
						{
							
							if (!pin.trim().matches(pin_pattern)) 
							{
								errCode = "VTINVLPIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					}
					
					// pinPattern Validation ended
					else if(childNodeName.equalsIgnoreCase("msme_reg_date"))
					{
						ssi = genericUtility.getColumnValue("ssi", dom);
						if("Y".equals(ssi))
						{
							msmeRegDate = genericUtility.getColumnValue("msme_reg_date", dom);
							if(msmeRegDate == null || msmeRegDate.trim().length() == 0 )
							{
								errCode = "VMSUPSSI04";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
                    // Changed By Nasruddin Start 21-SEP-16 end
                    // Changed By ANIK 25TH-JAN-2021 START
                    else if(childNodeName.equalsIgnoreCase("mobile_no_tax_reg"))
					{
                        mobile_no_tax_reg = checkNull(genericUtility.getColumnValue("mobile_no_tax_reg", dom));
                        System.out.println("MOB NO Tax Reg IS "+mobile_no_tax_reg);
                        // String mobile_no_tax_reg_result = String.valueOf(mobile_no_tax_reg);
						if (mobile_no_tax_reg.length() > 0 && mobile_no_tax_reg != null && !((mobile_no_tax_reg.trim()).matches("^([0-9]{6,12})$")) )
						{
                            
                                errCode = "VMMOBNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
                            
								
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("tele1"))
					{
                        tele1 = checkNull(genericUtility.getColumnValue("tele1", dom));
                        System.out.println("tele 1 "+tele1);
                        // String tele1_result = String.valueOf(tele1);
                        // System.out.println("Length Of Tele1 : "+tele1_result.length());
						if (tele1.length() > 0 && tele1 != null && !((tele1.trim()).matches("^([0-9]{6,12})$")) )
						{
                            
                                errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

                            
								
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("tele2"))
					{
                        tele2 = checkNull(genericUtility.getColumnValue("tele2", dom));
                        System.out.println("tele 2 "+tele2);
                        // String tele2_result = String.valueOf(tele2);
                        System.out.println("Length Of Tele2 : "+tele2.length());
						if (tele2.length() > 0 && tele2 != null && !((tele2.trim()).matches("^([0-9]{6,12})$")) )
						{
                            
                                errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

                            
								
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("tele3"))
					{
                        tele3 = checkNull(genericUtility.getColumnValue("tele3", dom));
                        System.out.println("tele 3 "+tele3);
                        // String tele3_result = String.valueOf(tele3);
                        System.out.println("Length Of Tele3 : "+tele3.length());
						if (tele3.length() > 0 && tele3 != null && !((tele3.trim()).matches("^([0-9]{6,12})$")) )
						{
                            
                                errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

						}
					}

                    else if(childNodeName.equalsIgnoreCase("alt_tele1"))
					{
                        alt_tele1 = checkNull(genericUtility.getColumnValue("alt_tele1", dom));
                        System.out.println("alt_tele 1 "+alt_tele1);
                        // String alt_tele1_result = String.valueOf(alt_tele1);
						if (alt_tele1.length() > 0 && alt_tele1 != null && !((alt_tele1.trim()).matches("^([0-9]{6,12})$")) )
						{
                           
                                errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

                            
								
						}
					}
                    // Changed By ANIK 25TH-JAN-2021 END
				}
				break; //end case 1 validation.
				
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("reg_code"))
					{
						regCode = checkNull(genericUtility.getColumnValue("reg_code", dom));
						System.out.println(">>>>>reg_code Validation" + regCode);
						if (regCode != null && regCode.trim().length() > 0)
						{
							existFlag = isExist("reg_requirements", "reg_code", regCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VTRCODEXT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VTRCODNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("reg_date"))
					{
						regDate = checkNull(genericUtility.getColumnValue("reg_date", dom));
						System.out.println(">>>>>>>>>>>regDate:"+regDate);
						if(regDate==null || regDate.trim().length()==0)
						{
							errCode = "VTREGNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("valid_upto"))
					{
						validUpto = checkNull(genericUtility.getColumnValue("valid_upto", dom));
						System.out.println(">>>>>>>>>>>validUpto:"+regDate);
						if(validUpto==null || validUpto.trim().length()==0)
						{
							errCode = "VTVALNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							regDate = checkNull(genericUtility.getColumnValue("reg_date", dom));
							if(regDate!=null && regDate.trim().length()> 0)
							{
								Timestamp validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(validUpto, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								Timestamp regDateNew = Timestamp.valueOf(genericUtility.getValidDateString(regDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								System.out.println(">>>> validUptoDate is:"+validUptoDate);
								System.out.println(">>>>>regDateNew is:" + regDateNew);
								if(validUptoDate!=null && regDateNew!=null)
								{
									if(validUptoDate.compareTo(regDateNew)<=0)
									{
										errCode = "VTVALREGDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}
				}	
				break; // end case 2  validation.
			} 
			
			int errListSize = errList.size();
			int cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}

		}// End of try

		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
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
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				connDriver = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [supplier][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		//Declare variable
		String tranCode = "";
		String tranName = "";
		String name = "";
		String shName = "";
		String contPers = "";
		String contpfx = "";
		String chqName = "";
		String crTerm = "";
		String stanCode = "";
		String crDays = "";
		String altAdd = "";
		String addr1 = "";
		String addr2 = "";
		String addr3 = "";
		String city = "";
		String pin = "";
		String stateCode = "";
		String countCode = "";
		String tele1 = "";
		String tele2 = "";
		String tele3 = "";
		String teleExt = "";
		String fax = "";
		String descr = "";
		String descr1 = "";
		String suppCode = "";
		String contactCode = "";
		String contactType="";
		String suppName = "";
		String fullName = "";
		String sql = "";
		String sql1 = "";
		String cityHdr="",pinHdr="",regCode="",regDescr="",resBKList="",lsDescr="";
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
        ConnDriver connDriver = new ConnDriver();
        int currentFormNo = 0;
        int ctr = 0;
        NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String suppType = "" , suppTypeDescr = ""; // Added by sarita on 15MAY2018
		try
		{  
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
            
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo + "**************");
			switch (currentFormNo)
			{
			case 1:
				
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				
				valueXmlString.append("<Detail1>");
				
				if(currentColumn.trim().equalsIgnoreCase("supp_code"))
				{
					System.out.println(">>>supp_code itemchange:");
					suppCode = genericUtility.getColumnValue("supp_code", dom);
					descr = genericUtility.getColumnValue("supp_code__pay", dom);
					if(descr == null || descr.trim().length() == 0) 
					{
						valueXmlString.append("<supp_code__pay>").append("<![CDATA[" + suppCode +"]]>").append("</supp_code__pay>");
					}
					descr1 = genericUtility.getColumnValue("group_code", dom);
					if(descr1 == null || descr1.trim().length() == 0)
					{
						valueXmlString.append("<group_code>").append("<![CDATA[" + suppCode +"]]>").append("</group_code>");
					}
				 }
				 else if(currentColumn.trim().equalsIgnoreCase("contact_code"))
				 {
					suppCode = genericUtility.getColumnValue("supp_code", dom);
					contactCode = genericUtility.getColumnValue("contact_code", dom);
					suppName = checkNull(genericUtility.getColumnValue("supp_name", dom));
					
					if(suppName.trim().length() == 0)
					{
						sql = " select name, sh_name, cont_pers, cont_pfx, addr1, addr2,addr3, city, pin, state_code, count_code, tele1, tele2, tele3, "+   
								" tele_ext, fax, email_addr, edi_addr,contact_type from contact where contact_code = ?"; 
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, contactCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							name = rs.getString("name");
							shName = rs.getString("sh_name");
							contPers = rs.getString("cont_pers");
							contpfx = rs.getString("cont_pfx");
							addr1 = rs.getString("addr1");
							addr2 = rs.getString("addr2");
							addr3 = rs.getString("addr3");
							city = rs.getString("city");
							pin = rs.getString("pin");
							stateCode = rs.getString("state_code");
							countCode = rs.getString("count_code");
							tele1 = rs.getString("tele1");
							tele2 = rs.getString("tele2");
							tele3 = rs.getString("tele3");
							teleExt = rs.getString("tele_ext");
							fax = rs.getString("fax");
							//Added by Amey W[To get contact_type from contact master] 25/9/18
							contactType = checkNull(rs.getString("contact_type"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						altAdd = findValue(conn, "alt_addr1", "supplier", "supp_code", suppCode);
						
						if(altAdd == null || altAdd.trim().length() == 0) 
						{
							valueXmlString.append("<alt_addr1>").append("<![CDATA[" + checkNull(addr1) +"]]>").append("</alt_addr1>");
							valueXmlString.append("<alt_addr2>").append("<![CDATA[" + checkNull(addr2) +"]]>").append("</alt_addr2>");
							valueXmlString.append("<alt_addr3>").append("<![CDATA[" + checkNull(addr3) +"]]>").append("</alt_addr3>");
							valueXmlString.append("<alt_city>").append("<![CDATA[" + checkNull(city) +"]]>").append("</alt_city>");
							valueXmlString.append("<alt_pin>").append("<![CDATA[" + checkNull(pin) +"]]>").append("</alt_pin>");
							valueXmlString.append("<state_code__alt>").append("<![CDATA[" + checkNull(stateCode) +"]]>").append("</state_code__alt>");
						}
						
						valueXmlString.append("<supp_name>").append("<![CDATA[" + checkNull(name) +"]]>").append("</supp_name>");
						valueXmlString.append("<full_name>").append("<![CDATA[" + checkNull(name) +"]]>").append("</full_name>");
						valueXmlString.append("<chq_name>").append("<![CDATA[" + checkNull(name) +"]]>").append("</chq_name>");
						valueXmlString.append("<sh_name>").append("<![CDATA[" + checkNull(shName) +"]]>").append("</sh_name>");
						valueXmlString.append("<cont_pers>").append("<![CDATA[" + checkNull(contPers) +"]]>").append("</cont_pers>");
						valueXmlString.append("<cont_pfx>").append("<![CDATA[" + checkNull(contpfx) +"]]>").append("</cont_pfx>");
						valueXmlString.append("<addr1>").append("<![CDATA[" + checkNull(addr1) +"]]>").append("</addr1>");
						valueXmlString.append("<addr2>").append("<![CDATA[" + checkNull(addr2) +"]]>").append("</addr2>");
						valueXmlString.append("<addr3>").append("<![CDATA[" + checkNull(addr3) +"]]>").append("</addr3>");
						valueXmlString.append("<city>").append("<![CDATA[" + checkNull(city) +"]]>").append("</city>");
						valueXmlString.append("<pin>").append("<![CDATA[" + checkNull(pin) +"]]>").append("</pin>");
						valueXmlString.append("<state_code>").append("<![CDATA[" + checkNull(stateCode) +"]]>").append("</state_code>");
						valueXmlString.append("<count_code>").append("<![CDATA[" + checkNull(countCode) +"]]>").append("</count_code>");
						valueXmlString.append("<tele1>").append("<![CDATA[" + checkNull(tele1) +"]]>").append("</tele1>");
						valueXmlString.append("<tele2>").append("<![CDATA[" + checkNull(tele2) +"]]>").append("</tele2>");
						valueXmlString.append("<tele3>").append("<![CDATA[" + checkNull(tele3) +"]]>").append("</tele3>");
						valueXmlString.append("<tele_ext>").append("<![CDATA[" + checkNull(teleExt) +"]]>").append("</tele_ext>");
						valueXmlString.append("<fax>").append("<![CDATA[" + checkNull(fax) +"]]>").append("</fax>");
						
						//Added by AMEY on 25/09/2018 [start] to get data from supplier default master from supp_type from respective contact code
						if (contactType != null && contactType.trim().length() > 0)
						{
							StringBuffer supplierDefaultData = setSupplierDefaultData(contactType,conn);
							if( supplierDefaultData != null && supplierDefaultData.length() > 0 )
							{
								valueXmlString.append( supplierDefaultData );
							}
						}
						//Added by AMEY on 25/09/2018 [end] to get data from supplier default master from supp_type from respective contact code
					}
				 }
				 else if(currentColumn.trim().equalsIgnoreCase("full_name"))
				 {
					fullName = genericUtility.getColumnValue("full_name", dom);
					if(fullName.trim().length() > 40)
					{
						name = mid(fullName,1,40);
					}
					valueXmlString.append("<supp_name>").append("<![CDATA[" + name +"]]>").append("</supp_name>");		
				 }
				 //Added by Pooja S. on 6 sept 18[Itemchange of reas_code__bklist] Start
				 else if(currentColumn.trim().equalsIgnoreCase("reas_code__bklist"))
				 {
					System.out.println(">>>>>>>>>>reas_code__bklist itemchange:");
					resBKList =genericUtility.getColumnValue("reas_code__bklist", dom);
					System.out.println("reasoncodebalcklist"+resBKList);
					sql ="select descr from gencodes where mod_name='W_SUPPLIER' and fld_name ='REAS_CODE__BKLIST' and fld_value =?";
					pstmt =conn.prepareStatement(sql);
					pstmt.setString(1,(resBKList).toUpperCase());
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lsDescr = rs.getString("descr");
						System.out.println("Descrription of reascode"+lsDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<bklist_reason>").append("<![CDATA[" + checkNull(lsDescr) + "]]>").append("</bklist_reason>");
				 }
				 //Added by Pooja S. on 6 sept 18[Itemchange of reas_code__bklist] End
				 else if(currentColumn.trim().equalsIgnoreCase("supp_name"))	
				 {
					suppName =genericUtility.getColumnValue("supp_name", dom);
					chqName =genericUtility.getColumnValue("chq_name", dom);
					if(suppName==null || suppName.trim().length()==0)
					{
						suppName="";
					}

					if(chqName == null || chqName.trim().length() == 0)
					{
						valueXmlString.append("<chq_name>").append("<![CDATA[" + suppName + "]]>").append("</chq_name>");
					}
				 }
				 //Added by sarita on 15MAY2018 [start] to show description of selected type
				 else if(currentColumn.trim().equalsIgnoreCase("supp_type"))
				 {					 
					 suppType = genericUtility.getColumnValue("supp_type", dom);
					 System.out.println("suppType == ["+suppType+"]");

					 sql = "select descr from gencodes where mod_name='W_SUPPLIER' and fld_name='SUPP_TYPE' and fld_value = ?";
					 pstmt =  conn.prepareStatement(sql);
					 pstmt.setString(1, suppType);
					 rs = pstmt.executeQuery();
					 if(rs.next())
					 {
						 suppTypeDescr = rs.getString("descr");
						System.out.println("Descr is :::" +suppTypeDescr);
					 }
					 rs.close();
					 rs = null;
					 pstmt.close();
					 pstmt = null;
					 valueXmlString.append("<supp_type_descr>").append("<![CDATA[" + suppTypeDescr + "]]>").append("</supp_type_descr>");

					 //Added by AMEY W on 12/09/2018 [start] to get data from supplier default master from supp_type
					 if (suppType != null && suppType.trim().length() > 0)
					 {
						 StringBuffer supplierDefaultData = setSupplierDefaultData( suppType, conn );
						 System.out.println("Back to method 1" );
						 
						 if( supplierDefaultData != null && supplierDefaultData.length() > 0 )
						 {
							 System.out.println("data found x==>" + supplierDefaultData);
							 valueXmlString.append(supplierDefaultData);	
						 }
					 }
					 //Added by AMEY W on 12/09/2018 [end] to get data from supplier default master from supp_type
				 }
				//Added by sarita on 15MAY2018 [end] to show description of selected type
				 else if(currentColumn.trim().equalsIgnoreCase("cr_term"))	
				 {
					 System.out.println(">>>>>>>>>>Itemchange cr_term:");
					crTerm = genericUtility.getColumnValue("cr_term", dom);
					crDays = findValue(conn, "cr_days", "crterm", "cr_term", crTerm);
					System.out.println(">>>>>>>>crDays IC:"+crDays);
					if(crDays!=null && crDays.trim().length() > 0)
					{
						valueXmlString.append("<credit_prd>").append("<![CDATA[" + crDays + "]]>").append("</credit_prd>");
					}
					else
					{
						crDays="0";
						valueXmlString.append("<credit_prd>").append("<![CDATA[" + crDays + "]]>").append("</credit_prd>");
					}
				 }
				 else if(currentColumn.trim().equalsIgnoreCase("stan_code"))	
				 {
					System.out.println(">>>>>>>>>>stan_code itemchange:");
					stanCode =genericUtility.getColumnValue("stan_code", dom);
					sql = "select descr,state_code,city,pin from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString("descr");
						stateCode = rs.getString("state_code");
						city = rs.getString("city");
						pin = rs.getString("pin");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;		
					sql = " select descr,count_code from state where state_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stateCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr1 = rs.getString("descr");
						countCode = rs.getString("count_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;		
					valueXmlString.append("<station_descr>").append("<![CDATA[" + checkNull(descr) + "]]>").append("</station_descr>");
					valueXmlString.append("<state_code>").append("<![CDATA[" + checkNull(stateCode) + "]]>").append("</state_code>");
					valueXmlString.append("<state_descr>").append("<![CDATA[" + checkNull(descr1) + "]]>").append("</state_descr>");
					valueXmlString.append("<count_code>").append("<![CDATA[" + checkNull(countCode) + "]]>").append("</count_code>");
					cityHdr =genericUtility.getColumnValue("city", dom); //change cityHdr and pinHdr variable by sagar on 23/09/14 
					pinHdr =genericUtility.getColumnValue("pin", dom);
				   //Commented by sarita on 15MAY2018[start] to perform itemchnage for city and pin code even if value exists
					/*if(cityHdr == null || cityHdr.trim().length() == 0)
					{*/
						valueXmlString.append("<city>").append("<![CDATA[" + checkNull(city) + "]]>").append("</city>");
					/*}
					if(pinHdr == null || pinHdr.trim().length() == 0)
					{*/
						valueXmlString.append("<pin>").append("<![CDATA[" + checkNull(pin) + "]]>").append("</pin>");
					//}
					//Commented by sarita on 15MAY2018[end] to perform itemchnage for city and pin code even if value exists	
				 }
				 else if(currentColumn.trim().equalsIgnoreCase("tran_code"))	
				 {
					tranCode =genericUtility.getColumnValue("tran_code", dom);
					tranName = findValue(conn, "tran_name", "transporter", "tran_code", tranCode);
					valueXmlString.append("<tran_name>").append("<![CDATA[" + tranName + "]]>").append("</tran_name>");
				 }
				 valueXmlString.append("</Detail1>");
				 break;  //end case 1 for itemchange.
				   
			case 2:
				
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail2>");
				
				System.out.println(">>>currentColumn In case 2:"+currentColumn);

				if (currentColumn.trim().equalsIgnoreCase("reg_code"))
				{
					regCode = genericUtility.getColumnValue("reg_code", dom);
					regDescr = findValue(conn, "DESCR", "REG_REQUIREMENTS", "REG_CODE", regCode);
					valueXmlString.append("<descr>").append("<![CDATA[" + regDescr + "]]>").append("</descr>");
				}
				valueXmlString.append("</Detail2>");
				break; // end case 2 of itemchange
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		//Added by Jaffar on 04 Apr 2018 ---> Start
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
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				connDriver = null;
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		//------- End -------
		
		return valueXmlString.toString();
	}	 

	//Added by AMEY on 25/09/2018 [start] to get data from supplier default master from supp_type
	/**
	 * 
	 * @param suppType
	 * @param conn
	 * @return
	 */
	private StringBuffer setSupplierDefaultData( String suppType, Connection conn )
	{
		StringBuffer valueXmlString = new StringBuffer();
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String sql="";
		String colName="";
		String colValue="";
		try
		{
			
			System.out.println("Dirty Connection Check");
			sql = "select * from supplier_default where supp_type = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, suppType);
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			if (rs.next())
			{
				for (int i = 1; i <= columnCount; i++)
				{
					colName = rsmd.getColumnName(i).toLowerCase();
					colValue = checkNull(rs.getString(colName));
					if (colValue != null && colValue.trim().length() > 0)
					{
						if( ! ( (colName.equalsIgnoreCase("chg_date")) || (colName.equalsIgnoreCase("chg_user")) || (colName.equalsIgnoreCase("chg_term")) ) )
						{
							valueXmlString.append("<" + colName + ">").append("<![CDATA[" + colValue + "]]>").append("</" + colName + ">");
						}
					}
				}
				
			}
			
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
		}
		catch (Exception e) 
		{
			System.out.println("Supplier.setSupplierDefaultData() : ["+ e.getMessage() +"]");
		}
		finally
		{
			try
			{
				System.out.println("Inside Finally Block :");
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch (SQLException e) 
			{
				System.out.println(e);
			}
		}
		return valueXmlString;
	}
	//Added by AMEY on 25/09/2018 [end] to get data from supplier default master from supp_type

	public String getfinparm(String pCode , String varName ,Connection conn)
	{
		String sql = "";
		String varValue = "";
		String addValue = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		try
		{
			sql = "select var_value, addl_value from finparm where prd_code = ? and var_name = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,pCode);
			pstmt.setString(2,varName);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				varValue = rs.getString("var_value");
				addValue = rs.getString("addl_value");
			}

		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("Catch block of Supplier.getfinparm()====> "+e);
		}
		//Added by Jaffar on 4th Apr 18 ---> Start
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		// ---- End ----
		
		if(addValue == null || addValue.trim().length() == 0)
		{
			return varValue;
		}
		else	
		{
			return varValue.trim()+";"+addValue.trim();
		}
	}
	//Changed by Manish on 09/09/16 for parameter interchange
  //public String supplier_cctr(String cctrCode , String acctCode, Connection conn)
	public String supplier_cctr(String acctCode , String cctrCode, Connection conn)
	{
		String sql = "";
		String errCode = "";
		String finparamCcterm ="";
		int count = 1;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		try
		{
			finparamCcterm = getfinparm("999999","CCTR_CHECK",conn);
			if(finparamCcterm.equals("Y"))
			{
				if(acctCode.trim().length() > 0 && acctCode != null)
				{
					sql = "select count(*) from costctr where cctr_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,cctrCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					//Added by Jaffar on 06-04-18 ---> Start
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//------- End -------
					
					if(count == 0)
					{
						errCode = "VMCCTR1";
					}
				}
				sql = "select count(*) from accounts_cctr where acct_code = ? and cctr_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,acctCode);
				pstmt.setString(2,cctrCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				//Added by Jaffar on 06-04-18 ---> Start
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				// ---- End ----
				
				if(count == 0)
				{
					sql = "select count(*) from accounts_cctr where acct_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,acctCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					//Added by Jaffar on 06-04-18 ---> Start
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//---- End ----
					
					if(count > 0)
					{

						errCode = "VMCCTR2";;
					}
					else if(count == 0)
					{

						errCode = "VMCCTR2";
					}
				}

			}
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("Catch block of Supplier.supplier_cctr()=== "+e);
		}
		//Added by Jaffar on 4th Apr 18 ---> Start
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//---- End ----
		
		return errCode;
	}

	public String supplier_acct( String siteCode ,String acctcode ,Connection conn )
	{
		String sql = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String siteSpec = "";
		String active = "";
		String errCode="";
		int count =0;
		try
		{
			sql = "select var_value from finparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_ACCT' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteSpec = rs.getString(1);
			}
			//Added by sarita on 16NOV2017
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select count(*) from accounts where acct_code = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,acctcode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			//Added by sarita on 16NOV2017
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(count != 0)
			{
				sql="select active from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,acctcode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					active = rs.getString("active");
				}
				//Added by sarita on 16NOV2017
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!active.equals("Y"))
				{
					errCode = "VMACCTA";
				}
				//Changed By PriyankaC on 23April2018..[START]
				//else if(siteSpec != null && !siteSpec.equals("Y"))
				else if(siteSpec != null && siteSpec.equals("Y"))
				//Changed By PriyankaC on 23April2018..[END]	
				{
					sql = "select count(*) from site_account where site_code = ? and acct_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,siteCode);
					pstmt.setString(2,acctcode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					//Added by sarita on 16NOV2017
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if(count == 0)
					{
						errCode = "VMACCT3";
					}
				}

			}		
			else
			{
				errCode = "VMACCT1";

			}
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("Catch block of Supplier.supplier_acct()=== "+e);
		}
		//Added by Jaffar on 4th Apr 18 ---> Start
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//---- End ----
		
		return errCode;
	}
	public String mid(String fname ,int i , int j)
	{
		String name = fname.substring(1, 40);
		return name; 
	}
	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}
	
	private String isExist(String table, String field, String value, Connection conn) throws SQLException
	{
		String sql = "", retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
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
			retStr = "TRUE";
		}
		if (cnt == 0)
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::[" + cnt + "]");
		return retStr;
	}
	private String findValue(Connection conn, String columnName, String tableName, String columnName2, String value) throws ITMException, RemoteException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String findValue = "";
		try
		{
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 + "= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (findValue == null || findValue.trim().length()== 0)
			{
				findValue = "";
			}
		} catch (Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		//Added by Jaffar on 4th Apr 18 ---> Start
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//---- End ----
		
		System.out.println("returning String from findValue " + findValue);
		return findValue;
	}


	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		
		return msgType;
	}
}  


