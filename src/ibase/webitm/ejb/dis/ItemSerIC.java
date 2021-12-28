package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ItemSerIC extends ValidatorEJB implements ItemSerICLocal, ItemSerICRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon=new FinCommon();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{		
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
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
		int ctr = 0,cnt=0;
		int childNodeListLength;
		String errorType = "",active = "",childNodeName = null,errString = "",errCode = "",sql = "",
				errFldName = "",userId = "",taxChap = "",siteCode = "",locCode = "",unit = "",
				itemSerCrpolicy = "",itemSer = "",acctCodeAp = "",acctCodePr = "",acctCodeIn = "",
				acctCodePh = "",acctCodeSal = "",acctCodeFrt = "",acctCodeDis = "",acctCodeGsd = "",
				acctCodeSret = "",acctCodeCiss = "",acctCodeAdcost = "",acctCodeOh = "",acctCodeWp = "",
				acctCodeCogs = "",acctCodeWpRcp = "",acctCodeQcSample = "",acctCodeYldLoss = "",
				acctCodeYldGain = "",cctrCodeAp = "",cctrCodePr = "",cctrCodeIn = "",cctrCodePh = "",
				cctrCodeSal = "",cctrCodeFrt = "",cctrCodeDis = "",cctrCodeGsd = "",cctrCodeSret = "",
				cctrCodeCiss = "",cctrCodeAdcost = "",cctrCodeOh = "",cctrCodeWp = "",cctrCodeCogs = "",
				cctrCodeWpRcp = "",cctrCodeQcSample = "",cctrCodeYldLoss = "",cctrCodeYldGain = "";			
		//Modified by Rohini T on[08/05/19][start]
		String acctCodeApAdv = "";
		String cctrCodeApAdv = "";
		//Modified by Rohini T on[08/05/19][end]
		//added by manish mhatre on 3-jan-2019
		String cctrCodeAr = "",	acctCodeAr="",cctrCodeCle = "",acctCodeCle="";
		String cctrCodeIns ="", acctCodeIns= "",cctrCodeFre = "",acctCodeFre="";
		String cctrCodeTrf = "",acctCodeTrf="",cctrCodeConvGl = "",acctCodeConvGl= "";
		String cctrCodeFaxFri ="",acctCodeFaxFri="",cctrCodeFaxFrr = "",acctCodeFaxFrr="";
		String cctrCodeInRev ="",acctCodeInRev="";  //end manish
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;		
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			System.out.println("childNodeListLength--------->>["+childNodeListLength+"]");
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{				
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("Child Node Name------->>["+childNodeName+"]");
				if(childNodeName.equalsIgnoreCase("item_ser"))
				{
					String keyFlag="";
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));					
					keyFlag=getNameOrDescrForCode(conn, "transetup", "key_flag", "tran_window", "w_itemser");
					keyFlag=keyFlag==null ? "M" : keyFlag.trim();
					if("M".equalsIgnoreCase(keyFlag) && itemSer.length() ==0)
					{
						errCode = "VMCODNULL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());	
					}
					else if("A".equalsIgnoreCase(editFlag.trim()))
					{
						cnt=getDBRowCount(conn,"itemser","item_ser",itemSer);
						if(cnt > 0)
						{
							errCode = "VMDUPL1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}						  
					}			
				}
				else if(childNodeName.equalsIgnoreCase("descr"))
				{ 
					String descr="";
					descr = checkNull(genericUtility.getColumnValue("descr", dom));		
					if(descr.length()== 0)
					{
						errCode = "VMDESCR";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());	
					}
					else
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
						sql="select count(1) from itemser where item_ser <> ? and descr = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						pstmt.setString(2, descr);
						rs=pstmt.executeQuery();
						if(rs.next())
							cnt=rs.getInt(1);
						rs.close();
						rs=null;
						pstmt.close();
						if(cnt > 0){
							errCode = "VMDUPDESCR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());		
						}					
					}			

				}
				else if(childNodeName.equalsIgnoreCase("tax_chap"))
				{
					taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					System.out.println("tAX CHAP ----->>>["+taxChap+"]");					
					if(taxChap.length() > 0)
					{
						cnt=getDBRowCount(conn,"TAXCHAP","TAX_CHAP",taxChap);						
						if(cnt == 0)
						{
							errCode = "VMTAXCHP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}					
					}
				}
				else if(childNodeName.equalsIgnoreCase("site_code"))
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					System.out.println("siteCode ----->>>["+siteCode+"]");		
					if(siteCode.length() > 0)
					{
						cnt=getDBRowCount(conn,"SITE","SITE_CODE",siteCode);					
						if(cnt == 0)
						{
							errCode = "VMSITE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());											
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("loc_code"))
				{
					locCode = checkNull(genericUtility.getColumnValue("loc_code", dom));
					System.out.println("locCode ----->>>["+locCode+"]");	
					if(locCode.length() > 0)
					{
						cnt=getDBRowCount(conn,"LOCATION","LOC_CODE",locCode);						
						if(cnt == 0)
						{
							errCode = "VMLOC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("unit"))
				{
					unit = checkNull(genericUtility.getColumnValue("unit", dom));
					System.out.println("unit ----->>>["+unit+"]");
					if(unit.length() > 0)
					{
						cnt=getDBRowCount(conn,"UOM","unit",unit);						
						if(cnt == 0)
						{
							errCode = "VMUNIT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}							
					}
				}
				else if(childNodeName.equalsIgnoreCase("item_ser__crpolicy"))
				{
					itemSerCrpolicy = checkNull(genericUtility.getColumnValue("item_ser__crpolicy", dom));
					System.out.println("itemSerCrpolicy ----->>>["+itemSerCrpolicy+"]");
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					if(itemSerCrpolicy.length() > 0)
					{
						cnt=getDBRowCount(conn,"ITEMSER","ITEM_SER",itemSerCrpolicy);						
						if(cnt == 0 && !(itemSer.equalsIgnoreCase(itemSerCrpolicy)))
						{
							errCode = "VTITEMSER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__ap"))
				{
					acctCodeAp = checkNull(genericUtility.getColumnValue("acct_code__ap", dom));
					System.out.println("acctCodeAp---->>["+acctCodeAp+"]");
					if(acctCodeAp.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeAp);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeAp);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__pr"))
				{
					acctCodePr = checkNull(genericUtility.getColumnValue("acct_code__pr", dom));
					System.out.println("acctCodePr---->>["+acctCodePr+"]");
					if(acctCodePr.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodePr);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodePr);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__in"))
				{
					acctCodeIn = checkNull(genericUtility.getColumnValue("acct_code__in", dom));					
					cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeIn);	
					System.out.println("acctCodeIn---->>["+acctCodeIn+"]");
					if(acctCodeIn.length() > 0)
					{
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeIn);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__ph"))
				{
					acctCodePh = checkNull(genericUtility.getColumnValue("acct_code__ph", dom));
					System.out.println("acctCodePh---->>["+acctCodePh+"]");
					if(acctCodePh.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodePh);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodePh);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__sal"))
				{
					acctCodeSal = checkNull(genericUtility.getColumnValue("acct_code__sal", dom));
					System.out.println("acctCodeSal---->>["+acctCodeSal+"]");
					if(acctCodeSal.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeSal);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeSal);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__frt"))
				{
					acctCodeFrt = checkNull(genericUtility.getColumnValue("acct_code__frt", dom));
					System.out.println("acctCodeFrt---->>["+acctCodeFrt+"]");
					if(acctCodeFrt.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeFrt);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeFrt);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__dis"))
				{
					acctCodeDis = checkNull(genericUtility.getColumnValue("acct_code__dis", dom));	
					System.out.println("acctCodeDis---->>["+acctCodeDis+"]");
					if(acctCodeDis.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeDis);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeDis);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__gsd"))
				{
					acctCodeGsd = checkNull(genericUtility.getColumnValue("acct_code__gsd", dom));	
					System.out.println("acctCodeGsd---->>["+acctCodeGsd+"]");
					if(acctCodeGsd.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeGsd);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeGsd);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}
				else if(childNodeName.equalsIgnoreCase("acct_code__sret"))
				{
					acctCodeSret = checkNull(genericUtility.getColumnValue("acct_code__sret", dom));
					System.out.println("acctCodeSret---->>["+acctCodeSret+"]");
					if(acctCodeSret.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeSret);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeSret);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}							
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__ciss"))
				{
					acctCodeCiss = checkNull(genericUtility.getColumnValue("acct_code__ciss", dom));
					System.out.println("acctCodeCiss---->>["+acctCodeCiss+"]");
					if(acctCodeCiss.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeCiss);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeCiss);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}						
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__adcost"))
				{
					acctCodeAdcost = checkNull(genericUtility.getColumnValue("acct_code__adcost", dom));
					System.out.println("acctCodeAdcost---->>["+acctCodeAdcost+"]");
					if(acctCodeAdcost.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeAdcost);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeAdcost);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}							
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__oh"))
				{
					acctCodeOh = checkNull(genericUtility.getColumnValue("acct_code__oh", dom));	
					System.out.println("acctCodeOh---->>["+acctCodeOh+"]");
					if(acctCodeOh.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeOh);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeOh);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}							
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__wp"))
				{
					acctCodeWp = checkNull(genericUtility.getColumnValue("acct_code__wp", dom));	
					System.out.println("acctCodeWp---->>["+acctCodeWp+"]");
					if(acctCodeWp.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeWp);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeWp);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}							
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__cogs"))
				{
					acctCodeCogs = checkNull(genericUtility.getColumnValue("acct_code__cogs", dom));	
					System.out.println("acctCodeCogs---->>["+acctCodeCogs+"]");
					if(acctCodeCogs.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeCogs);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeCogs);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__wp_rcp"))
				{
					acctCodeWpRcp = checkNull(genericUtility.getColumnValue("acct_code__wp_rcp", dom));	
					System.out.println("acctCodeWpRcp---->>["+acctCodeWpRcp+"]");
					if(acctCodeWpRcp.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeWpRcp);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeWpRcp);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__qc_sample"))
				{
					acctCodeQcSample = checkNull(genericUtility.getColumnValue("acct_code__qc_sample", dom));
					System.out.println("acctCodeQcSample---->>["+acctCodeQcSample+"]");
					if(acctCodeQcSample.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeQcSample);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeQcSample);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__yld_loss"))
				{
					acctCodeYldLoss = checkNull(genericUtility.getColumnValue("acct_code__yld_loss", dom));	
					System.out.println("acctCodeYldLoss---->>["+acctCodeYldLoss+"]");
					if(acctCodeYldLoss.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeYldLoss);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeYldLoss);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__yld_gain"))
				{
					acctCodeYldGain = checkNull(genericUtility.getColumnValue("acct_code__yld_gain", dom));	
					System.out.println("acctCodeYldGain---->>["+acctCodeYldGain+"]");
					if(acctCodeYldGain.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeYldGain);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeYldGain);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}							
					}
				}
				//Modified by Rohini T on[08/05/19][start]
				else if(childNodeName.equalsIgnoreCase("acct_code__ap_adv"))
				{
					acctCodeApAdv = checkNull(genericUtility.getColumnValue("acct_code__ap_adv", dom));	
					System.out.println("acctCodeApAdv---->>["+acctCodeApAdv+"]");
					if(acctCodeApAdv.length() > 0)
					{
						cnt=getDBRowCount(conn,"ACCOUNTS","ACCT_CODE",acctCodeApAdv);						
						if(cnt == 0)
						{
							errCode = "VMACCT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							active=getNameOrDescrForCode(conn, "ACCOUNTS", "ACTIVE", "ACCT_CODE", acctCodeApAdv);
							if(!("Y".equalsIgnoreCase(active)))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}							
					}
				}
				//Modified by Rohini T on[08/05/19][end]
				else if(childNodeName.equalsIgnoreCase("cctr_code__ap"))
				{
					cctrCodeAp = checkNull(genericUtility.getColumnValue("cctr_code__ap", dom));
					System.out.println("cctrCodeAp---->>["+cctrCodeAp+"]");
					if(cctrCodeAp!=null && cctrCodeAp.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeAp, cctrCodeAp, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish
						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeAp);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}							*/
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__pr"))
				{
					cctrCodePr = checkNull(genericUtility.getColumnValue("cctr_code__pr", dom));
					System.out.println("cctrCodePr---->>["+cctrCodePr+"]");
					if(cctrCodePr!=null && cctrCodePr.length() > 0)
					{
							//added by manish mhatre on 3-jan-2020
							errCode = finCommon.isCctrCode(acctCodePr, cctrCodePr, " ", conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}//end manish
//						cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodePr);						
//						if(cnt == 0)
//						{
//							errCode = "VMCCTR";
//							errList.add(errCode);
//							errFields.add(childNodeName.toLowerCase());
//						}						
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__in"))
				{
					cctrCodeIn = checkNull(genericUtility.getColumnValue("cctr_code__in", dom));
					System.out.println("cctrCodeIn---->>["+cctrCodeIn+"]");
					if(cctrCodeIn!=null && cctrCodeIn.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeIn, cctrCodeIn, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

					/*	cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeIn);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						*/	
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__ph"))
				{
					cctrCodePh = checkNull(genericUtility.getColumnValue("cctr_code__ph", dom));
					System.out.println("acctCodePr---->>["+cctrCodePh+"]");
					if(cctrCodePh!=null && cctrCodePh.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodePh, cctrCodePh, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodePh);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/	
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__sal"))
				{
					cctrCodeSal = checkNull(genericUtility.getColumnValue("cctr_code__sal", dom));
					System.out.println("cctrCodeSal---->>["+cctrCodeSal+"]");
					if(cctrCodeSal!=null && cctrCodeSal.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeSal, cctrCodeSal, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeSal);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/			
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__frt"))
				{
					cctrCodeFrt = checkNull(genericUtility.getColumnValue("cctr_code__frt", dom));
					System.out.println("cctrCodeFrt---->>["+cctrCodeFrt+"]");
					if(cctrCodeFrt!=null && cctrCodeFrt.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeFrt, cctrCodeFrt, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeFrt);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}							*/
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__dis"))
				{
					cctrCodeDis = checkNull(genericUtility.getColumnValue("cctr_code__dis", dom));		
					System.out.println("cctrCodeDis---->>["+cctrCodeDis+"]");
					if(cctrCodeDis!=null && cctrCodeDis.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeDis, cctrCodeDis, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeDis);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}							*/
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__gsd"))
				{
					cctrCodeGsd = checkNull(genericUtility.getColumnValue("cctr_code__gsd", dom));
					System.out.println("cctrCodeGsd---->>["+cctrCodeGsd+"]");
					if(cctrCodeGsd!=null && cctrCodeGsd.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeGsd, cctrCodeGsd, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeGsd);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/			
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__sret"))
				{
					cctrCodeSret = checkNull(genericUtility.getColumnValue("cctr_code__sret", dom));
					System.out.println("cctrCodeSret---->>["+cctrCodeSret+"]");
					if(cctrCodeSret!=null && cctrCodeSret.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeSret, cctrCodeSret, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeSret);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/			
					}
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__ciss"))
				{
					cctrCodeCiss = checkNull(genericUtility.getColumnValue("cctr_code__ciss", dom));	
					System.out.println("cctrCodeCiss---->>["+cctrCodeCiss+"]");
					if(cctrCodeCiss!=null && cctrCodeCiss.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeCiss, cctrCodeCiss, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeCiss);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/		
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__adcost"))
				{
					cctrCodeAdcost = checkNull(genericUtility.getColumnValue("cctr_code__adcost", dom));
					System.out.println("cctrCodeAdcost---->>["+cctrCodeAdcost+"]");
					if(cctrCodeAdcost!=null && cctrCodeAdcost.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeAdcost, cctrCodeAdcost, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeAdcost);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}					*/	
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__oh"))
				{
					cctrCodeOh = checkNull(genericUtility.getColumnValue("cctr_code__oh", dom));
					System.out.println("cctrCodeOh---->>["+cctrCodeOh+"]");
					if(cctrCodeOh!=null && cctrCodeOh.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeOh, cctrCodeOh, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeOh);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/		
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__wp"))
				{
					cctrCodeWp = checkNull(genericUtility.getColumnValue("cctr_code__wp", dom));
					System.out.println("cctrCodeWp---->>["+cctrCodeWp+"]");
					if(cctrCodeWp!=null  && cctrCodeWp.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeWp, cctrCodeWp, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeWp);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						*/
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__cogs"))
				{
					cctrCodeCogs = checkNull(genericUtility.getColumnValue("cctr_code__cogs", dom));
					System.out.println("cctrCodeCogs---->>["+cctrCodeCogs+"]");
					if(cctrCodeCogs!=null && cctrCodeCogs.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeCogs, cctrCodeCogs, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

					/*	cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeCogs);						

						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						*/
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__wp_rcp"))
				{
					cctrCodeWpRcp = checkNull(genericUtility.getColumnValue("cctr_code__wp_rcp", dom));
					System.out.println("cctrCodeWpRcp---->>["+cctrCodeWpRcp+"]");
					if(cctrCodeWpRcp!=null && cctrCodeWpRcp.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeWpRcp, cctrCodeWpRcp, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeWpRcp);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						*/
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__qc_sample"))
				{
					cctrCodeQcSample = checkNull(genericUtility.getColumnValue("cctr_code__qc_sample", dom));
					System.out.println("cctrCodeQcSample---->>["+cctrCodeQcSample+"]");
					if(cctrCodeQcSample!=null && cctrCodeQcSample.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeQcSample, cctrCodeQcSample, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeQcSample);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}				*/		
					}

				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__yld_loss"))
				{
					cctrCodeYldLoss = checkNull(genericUtility.getColumnValue("cctr_code__yld_loss", dom));		
					System.out.println("cctrCodeYldLoss---->>["+cctrCodeYldLoss+"]");
					if(cctrCodeYldLoss!=null && cctrCodeYldLoss.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeYldLoss, cctrCodeYldLoss, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

					/*	cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeYldLoss);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}					*/	
					}					
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__yld_gain"))
				{
					cctrCodeYldGain = checkNull(genericUtility.getColumnValue("cctr_code__yld_gain", dom));
					System.out.println("cctrCodeYldGain---->>["+cctrCodeYldGain+"]");
					if(cctrCodeYldGain!=null && cctrCodeYldGain.length() > 0)
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeYldGain, cctrCodeYldGain, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeYldGain);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						*/
					}										
				}
				//Modified by Rohini T on[08/05/19][start]
				else if(childNodeName.equalsIgnoreCase("cctr_code__ap_adv"))
				{
					cctrCodeApAdv = checkNull(genericUtility.getColumnValue("cctr_code__ap_adv", dom));
					System.out.println("cctrCodeApAdv---->>["+cctrCodeApAdv+"]");
					if(cctrCodeApAdv!=null && cctrCodeApAdv.length() > 0)
					{  
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeApAdv, cctrCodeApAdv, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish

						/*cnt=getDBRowCount(conn,"COSTCTR","CCTR_CODE",cctrCodeApAdv);						
						if(cnt == 0)
						{
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}					*/	
					}										
				}
				//added by manish mhatre on 3-jan-2020
				else if(childNodeName.equalsIgnoreCase("cctr_code__ar"))
				{
					cctrCodeAr = checkNull(genericUtility.getColumnValue("cctr_code__ar", dom));
					acctCodeAr= checkNull(genericUtility.getColumnValue("acct_code__ar", dom));
					if(cctrCodeAr!=null && cctrCodeAr.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeAr, cctrCodeAr, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__cle"))
				{
					cctrCodeCle = checkNull(genericUtility.getColumnValue("cctr_code__cle", dom));
					acctCodeCle= checkNull(genericUtility.getColumnValue("acct_code__cle", dom));
					if(cctrCodeCle!=null && cctrCodeCle.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeCle, cctrCodeCle, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__ins"))
				{
					cctrCodeIns = checkNull(genericUtility.getColumnValue("cctr_code__ins", dom));
					acctCodeIns= checkNull(genericUtility.getColumnValue("acct_code__ins", dom));
					if(cctrCodeIns!=null && cctrCodeIns.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeIns, cctrCodeIns, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__fre"))
				{
					cctrCodeFre = checkNull(genericUtility.getColumnValue("cctr_code__fre", dom));
					acctCodeFre= checkNull(genericUtility.getColumnValue("acct_code__fre", dom));
					if(cctrCodeFre!=null && cctrCodeFre.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeFre, cctrCodeFre, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__trf"))
				{
					cctrCodeTrf = checkNull(genericUtility.getColumnValue("cctr_code__trf", dom));
					acctCodeTrf= checkNull(genericUtility.getColumnValue("acct_code__trf", dom));
					if(cctrCodeTrf!=null && cctrCodeTrf.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeTrf, cctrCodeTrf, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__conv_gl"))
				{
					cctrCodeConvGl = checkNull(genericUtility.getColumnValue("cctr_code__conv_gl", dom));
					acctCodeConvGl= checkNull(genericUtility.getColumnValue("acct_code__conv_gl", dom));
					if(cctrCodeConvGl!=null && cctrCodeConvGl.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeConvGl, cctrCodeConvGl, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__faxfri"))
				{
					cctrCodeFaxFri = checkNull(genericUtility.getColumnValue("cctr_code__faxfri", dom));
					acctCodeFaxFri= checkNull(genericUtility.getColumnValue("acct_code__faxfri", dom));
					
					if(cctrCodeFaxFri!=null && cctrCodeFaxFri.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeFaxFri, cctrCodeFaxFri, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code__faxfrr"))
				{
					cctrCodeFaxFrr = checkNull(genericUtility.getColumnValue("cctr_code__faxfrr", dom));
					acctCodeFaxFrr= checkNull(genericUtility.getColumnValue("acct_code__faxfrr", dom));
					
					if(cctrCodeFaxFrr!=null && cctrCodeFaxFrr.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeFaxFrr, cctrCodeFaxFrr, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				else if(childNodeName.equalsIgnoreCase("cctr_code__inrev"))
				{
					cctrCodeInRev = checkNull(genericUtility.getColumnValue("cctr_code__inrev", dom));
					acctCodeInRev= checkNull(genericUtility.getColumnValue("acct_code__inrev", dom));
					if(cctrCodeInRev!=null && cctrCodeInRev.length() > 0)
					{
						errCode = finCommon.isCctrCode(acctCodeInRev, cctrCodeInRev, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}										
				}
				
				//end manish
				//Modified by Rohini T on[08/05/19][end]
			}
			int errListSize = errList.size();
			cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString( errFldName, errCode, userId );					
					errorType = errorType(conn , errCode);
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
		}
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
				if(conn != null)
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
					conn.close();
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

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
			System.out.println("Exception : [ItemSerIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	} 

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		int ctr = 0;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String lsApplyax = "";
		String query1= "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			valueXmlString.append("<Detail1>");	

			if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
			{
				int cnt1=0;
				query1 = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
				pstmt = conn.prepareStatement(query1);
				pstmt.setString(1, "999999");
				pstmt.setString(2, "CHANGE_APPLY_TAX");
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt1++;
					lsApplyax = rs.getString(1);
				}
				if(cnt1 > 0)
					lsApplyax = "NULLFOUND";

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsApplyax == null || lsApplyax.trim().length() == 0 || lsApplyax.trim() == "N" || lsApplyax == "NULLFOUND" )
				{
					valueXmlString.append("<apply_tax protect=\"1\">").append("<![CDATA[" + lsApplyax + "]]>").append("</apply_tax>");
				}
				else
				{
					valueXmlString.append("<apply_tax protect=\"0\">").append("<![CDATA[" + lsApplyax + "]]>").append("</apply_tax>");
				}
			}
			valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}

	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, errorCode);			
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
	private int getDBRowCount(Connection conn, String table_name, String whrCondCol, String whrCondVal)
	{
		int count=-1;

		if(conn!=null){

			ResultSet rs=null;
			PreparedStatement pstmt = null;

			String sql="select count(1) from "+table_name+" where "+whrCondCol+" = ?";
			System.out.println("SQL in getDBRowCount method : "+sql);
			try
			{
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,whrCondVal);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			catch(SQLException e)
			{
				System.out.println("SQL Exception In getDBRowCount method of ItemSerIC Class : "+e.getMessage());
				e.printStackTrace();
			}
			catch(Exception ex)
			{
				System.out.println("Exception In getDBRowCount method of ItemSerIC Class : "+ex.getMessage());
				ex.printStackTrace();
			}
		}

		return count;
	}
	private String getNameOrDescrForCode(Connection conn, String table_name, String descr_col_name,String whrCondCol, String whrCondVal)
	{
		String descr = null;

		if(conn!=null){

			ResultSet rs=null;
			PreparedStatement pstmt = null;

			String sql="SELECT "+descr_col_name+" FROM "+table_name+" WHERE "+whrCondCol+" = ?";

			System.out.println("SQL in getNameOrDescrForCode method : "+sql);
			try
			{
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,whrCondVal);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString(descr_col_name);
				}

				descr=descr==null ? "" : descr;
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
			catch(SQLException e)
			{
				System.out.println("SQL Exception In getNameOrDescrForCode method of ItemSerIC Class : "+e.getMessage());
				e.printStackTrace();
			}
			catch(Exception ex)
			{
				System.out.println("Exception In getNameOrDescrForCode method of ItemSerIC Class : "+ex.getMessage());
				ex.printStackTrace();
			}
		}

		return descr;
	}
	private String checkNull(String input){
		if(input==null){
			return "";
		}
		return input.trim();
	}

}
