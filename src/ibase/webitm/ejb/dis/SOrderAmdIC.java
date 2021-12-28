/********************************************************
	Title : SOrderAmdIC [D16EBAS002]
	Date  : 05 - AUG - 2016
	Author: Poonam Gole

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class SOrderAmdIC extends ValidatorEJB implements SOrderAmdICRemote,SOrderAmdICLocal 
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	DistCommon distCommon = new DistCommon();
	FinCommon finCommon = new FinCommon();
	//ValidatorEJB ValidatorEJB = new ValidatorEJB(); //Changed By PriyankaC on 04JAN18
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	public String wfValData() throws RemoteException, ITMException 
	{
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		System.out.println("Validation Start.....xmlString[" + xmlString+ "]]]]]xmlString1[[[[[" + xmlString1 + "]]]]]][[[[[[["+ xmlString2 + "]]]]]]].....");
		try 
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : StarclubEligWorkIC() : wfValData(String xmlString) : ==>\n"+ e.getMessage());
			
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String userId = null;
		int cnt = 0,ctr= 0, llCnt = 0 ,llCnt1 = 0 ,llCount = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		Connection conn = null;
		PreparedStatement pStmt = null ,pStmt1 = null;
		ResultSet rs = null,rs1 = null;
		ConnDriver connDriver = new ConnDriver();
		String errorType = "";
		java.util.Date today = new java.util.Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		today = cal.getTime();
		SimpleDateFormat sdf = null;
		Date amdDate1 = null;
		int bomCnt = 0 ,mCnt = 0 ,custSerCnt = 0 ;
		boolean ordFlag = false ,lbProceed = false;
		Timestamp sysDate = null ,DueDate = null ,orderDate = null ,appFrom =null ,validUpto = null;
		
		String errString = " ",sql = "",sql1 = "",siteCode = "",errcode = "",amdDate = "", amdNo = "", confirm = "",despId = "", despList = "",saleOrder = "",crTerm = "";
		String custCodeDlv = "", custCodeBil = "",  taxChap = "", taxClass = "", taxEnv = "", status = "", dlvTerm = "",countCodeDlv = "",stanCode =  "",orderType =  "",
		priceList =  "",schemeCode = "" ,itemSer =  "",disPobOrdType = "",acctCodeSal =  "",cctrCodeSal = "",itemCode= "" , nature = "", disPobOrdtypeList = "",
		lineNoSord = "",rate = "",reasCode = "",  qtyStduom = "",itemFlg = "" ,lineNo = "",qty = "", schemeFlag = "",
		qtyO = "",packCode = "" , unit = "",unitStd = "", convQtyStduom = "", rateO = "",minshelfLife = "",minshelfLifeO = "",rateClg = "",custCode ="",
		stateCodeDlv= "",custPord = "", channelPartner = "",disLink = "",lsEou= "",exportOrderType = "",lopReqd= "", applyCustList= "",itemSerNew = "",
		noapplyCustList = "" , applicableOrdtypes= "" , itemSerCrpolicy = "" ,itemSerCrpolicyHdr = "",othSeries= "" ,
		stopBusiness = "" ,salesOptionItem = "",contractReq = "" ;
	     
		Timestamp amdDatets = null  ;
		
		Date restrictUpto = null;
		 
		double mqtyDesp = 0.0 ,qtyAlloc = 0.0 ,integralQty = 0.0 ,lcMod = 0.0 ,intQty = 0.0 ,modQty = 0.0 ,qtyDesp = 0.0 ,minRate = 0.0 ,qtyStduomDb=0.0,
				rateDb = 0.0 ,rateODb = 0.0 ,qtyODb = 0.0 , qtyDb = 0.0 ,minshelfLifeODb = 0.0 ,minshelfLifeDb = 0.0, convQtyStduom1 = 0.0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		ArrayList despListArr = new ArrayList<String>();

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try
		{
			System.out.println("wfValData called");
			// Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
				case 1: 
				{
					System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					int cnt1 = 0;
					for (ctr = 0; ctr < childNodeListLength; ctr++) 
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName[" + childNodeName + "]");
						if (childNodeName.equalsIgnoreCase("amd_date")) 
						{
							amdDate = checkNull(genericUtility.getColumnValue("amd_date", dom));
							saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
							
							if (amdDate != null && amdDate.trim().length() > 0) 
							{
								sysDate = Timestamp.valueOf(genericUtility.getValidDateString(amdDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							}
							sql = "select site_code from sorder where  sale_order = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, saleOrder);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								siteCode = checkNull(rs.getString("site_code"));
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errcode = ValidatorEJB.nfCheckPeriod("SAL", sysDate,siteCode);
							errcode=finCommon.nfCheckPeriod("SAL", sysDate,siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							System.out.println("errcode in amddate"+errcode);
							if (errcode != null && errcode.trim().length() > 0) 
							{
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
						if(childNodeName.equalsIgnoreCase("sale_order")) 
						{
							saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
							amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
							
							if(amdNo == null || amdNo.trim().length() == 0)
							{
								amdNo = " ";
							}
							
							if("A".equalsIgnoreCase(editFlag))
							{
								sql = " select count(*) from sordamd where sale_order = ? and " +
									  " (confirmed <> 'Y' or confirmed is null) and (status <> 'X' or status is null) and amd_no <> ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, saleOrder);
								pStmt.setString(2, amdNo);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt > 0)
								{
									errcode = "VTACONF" ;
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
							if(errcode == null || errcode.trim().length() == 0)
							{
								sql = "select count(*) from sorder where  sale_order = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, saleOrder);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTSORD1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									sql1 = "select status, confirmed from sorder where sale_order = ?";
									pStmt1 = conn.prepareStatement(sql1);
									pStmt1.setString(1, saleOrder);
									rs1 = pStmt1.executeQuery();
									if (rs1.next()) 
									{
										status = checkNull(rs1.getString("status"));
										confirm = checkNull(rs1.getString("confirmed"));
									}
									rs1.close();rs1 = null;
									pStmt1.close();pStmt1 = null;
									
									if("C".equalsIgnoreCase(status) || "X".equalsIgnoreCase(status))
									{
										errcode = "VTSOAMD5";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if("N".equalsIgnoreCase(confirm) )
									{
										errcode = "VTSOAMD7";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								
								System.out.println("errcode>>"+errcode);
								if(errcode.trim().length() == 0)
								{
									sql = "select count(*) from despatch where	sord_no = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, saleOrder);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										llCnt = rs.getInt(1);
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									System.out.println("llCnt>>"+llCnt);
									if(llCnt > 0 )
									{
										sql = "select count(*) from despatch where	sord_no = ? and confirmed ='Y'";
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, saleOrder);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											llCnt1 = rs.getInt(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
										System.out.println("llCnt1>>"+llCnt1);
										if(llCnt1 > 0 )
										{
											
											sql = "select count(*) from despatch where	sord_no = ? and confirmed ='Y' and status = 'I'";
											pStmt = conn.prepareStatement(sql);
											pStmt.setString(1, saleOrder);
											rs = pStmt.executeQuery();
											if (rs.next()) 
											{
												llCount = rs.getInt(1);
											}
											rs.close();rs = null;
											pStmt.close();pStmt = null;
											System.out.println("llCount>>"+llCount);
											if(llCount > 0)
											{
												sql = "select desp_id from despatch where sord_no = ? and confirmed ='Y' and 	status = 'I'";
												pStmt = conn.prepareStatement(sql);
												pStmt.setString(1, saleOrder);
												rs = pStmt.executeQuery();
												while (rs.next()) 
												{
													despId = checkNull(rs.getString("desp_id"));
													System.out.println("despId>>"+despId);
													despListArr.add(despId);
												}
												rs.close();rs = null;
												pStmt.close();pStmt = null;
												System.out.println("despList if >>"+despListArr);
												errcode = "VTDESPINV";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
											else
											{
												sql = "select desp_id from despatch where	sord_no = ? and confirmed ='Y'";
												pStmt = conn.prepareStatement(sql);
												pStmt.setString(1, saleOrder);
												rs = pStmt.executeQuery();
												while (rs.next()) 
												{
													despId = checkNull(rs.getString("desp_id"));
													System.out.println("despId>>"+despId);
													/*despList = despList + ",";
													despList = despList + despId ;*/
													despListArr.add(despId);
												}
												rs.close();rs = null;
												pStmt.close();pStmt = null;
												System.out.println("despList else >>"+despListArr);
												errcode = "VTDSPCONF";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
											
										}
										else
										{
											
											sql = "select desp_id from despatch where sord_no = ? ";
											pStmt = conn.prepareStatement(sql);
											pStmt.setString(1, saleOrder);
											rs = pStmt.executeQuery();
											while(rs.next()) 
											{
												despId = checkNull(rs.getString("desp_id"));
												System.out.println("despId>>"+despId);
												/*despList = despList + ",";
												despList = despList + despId ;*/
												despListArr.add(despId);
											}
											rs.close();rs = null;
											pStmt.close();pStmt = null;
											System.out.println("despList else else >>"+despListArr);
											errcode = "VDSPUNCONF";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
											
										}
										
									}
									
								}
								
								
								sql = "select count(1) from distorder where sale_order = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, saleOrder);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt1 =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt1 > 0)
								{
									sql = "select count(1) from distorder where sale_order = ? and status = 'P' ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, saleOrder);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										cnt =rs.getInt(1);
										
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if(cnt == 0)
									{
										errcode = "VTCANCO";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
						if (childNodeName.equalsIgnoreCase("cust_code__dlv")) 
						{
							custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
							
							sql = "select count(1) from customer where cust_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, custCodeDlv);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt =rs.getInt(1);
								
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if(cnt == 0)
							{
								errcode = "VTCUST1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if (childNodeName.equalsIgnoreCase("cust_code__bil")) 
						{
							custCodeBil = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
							
							sql = "select count(1) from customer where cust_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, custCodeBil);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt =rs.getInt(1);
								
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if(cnt == 0)
							{
								errcode = "VTCUST1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if (childNodeName.equalsIgnoreCase("tax_class")) 
						{
							taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
							
							if(taxClass != null && taxClass.trim().length() > 0)
							{
								sql = "select count(*) from taxclass where tax_class = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taxClass);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTTCLASS1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						if (childNodeName.equalsIgnoreCase("tax_chap")) 
						{
							taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
							
							if(taxChap != null && taxChap.trim().length() > 0)
							{
								sql = "select count(*) from taxchap where tax_chap = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taxChap);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTTCHAP1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

						}
						if (childNodeName.equalsIgnoreCase("tax_env"))
						{
							taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
							amdDate = checkNull(genericUtility.getColumnValue("amd_date", dom));

							if(amdDate != null && amdDate.trim().length() > 0)
							{
							amdDate1 = sdf.parse(amdDate);
							amdDatets = new java.sql.Timestamp(amdDate1.getTime());
							}
							System.out.println("amdDatets"+amdDatets + "amdDate"+amdDate);
							if(taxEnv != null && taxEnv.trim().length() > 0)
							{
								sql = "select count(*) from taxenv where tax_env = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, taxEnv);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTTENV1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{	//Pavan R 17sept19 start[to validate tax environment]
									//errcode = distCommon.getCheckTaxEnvStatus(taxEnv, amdDatets, conn);
									errcode = distCommon.getCheckTaxEnvStatus(taxEnv, amdDatets, "S", conn);
									//Pavan R 17sept19 end[to validate tax environment]
									//Changes done by Mayur on 14/JUNE/17---[START]
								    if(errcode != null && errcode.trim().length() > 0)
									{
								    	errList.add(errcode);
								    	errFields.add(childNodeName.toLowerCase());
									}
									//Changes done by Mayur on 14/JUNE/17---[END]
									/*errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());*/}
									
							}
							
						}
						if (childNodeName.equalsIgnoreCase("cr_term")) 
						{
							crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
							
							if(crTerm != null && crTerm.trim().length() > 0)
							{
								sql = "select count(*) from crterm where cr_term = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, crTerm);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTCRTERM1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
						}
						if (childNodeName.equalsIgnoreCase("count_code__dlv")) 
						{
							countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom));
							
							if(countCodeDlv != null && countCodeDlv.trim().length() > 0)
							{
								sql = "select count(*) from country where count_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, countCodeDlv);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTCONT1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						if (childNodeName.equalsIgnoreCase("stan_code"))
						{
							stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
							
							if(stanCode != null && stanCode.trim().length() > 0)
							{
								sql = "select count(*) from station where stan_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, stanCode);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTSTAN1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						if (childNodeName.equalsIgnoreCase("stan_code__init") )
						{
							stanCode = checkNull(genericUtility.getColumnValue("stan_code__init", dom));
							
							if(stanCode != null && stanCode.trim().length() > 0)
							{
								sql = "select count(*) from station where stan_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, stanCode);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
									
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTSTAN1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						if (childNodeName.equalsIgnoreCase("dlv_term")) 
						{
							dlvTerm = checkNull(genericUtility.getColumnValue("dlv_term", dom));
							if(dlvTerm == null || dlvTerm.trim().length() == 0)
							{
								
								errcode = "VMDLVTERM2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							
							if( dlvTerm.trim().length() != 0)
							{	
								sql = "select count(*) from delivery_term where dlv_term = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, dlvTerm);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VMDLVTERM1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
						}
						if (childNodeName.equalsIgnoreCase("reas_code")) 
						{
							reasCode = checkNull(genericUtility.getColumnValue("reas_code", dom));
							System.out.println("in reas_code.........");
							if(reasCode != null && reasCode.trim().length() > 0)
							{
								sql = "select count(*) from gencodes where fld_Name = 'REAS_CODE'  and fld_value = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, reasCode);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt =rs.getInt(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTREASON";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						if (childNodeName.equalsIgnoreCase("acct_code__sal"))
						{
							System.out.println("in acct_code__sal.........");
							acctCodeSal = checkNull(genericUtility.getColumnValue("acct_code__sal", dom));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							// 12-nov-2020 manoharan consider posting type
							String lsPosttype =  checkNull(finCommon.getFinparams("999999", "SALES_INV_POST_HDR", conn)); //Added by Dipesh p on[16/06/2020]
							System.out.println("lsPosttype::::::"+lsPosttype);
							System.out.println("acctCodeSal: " + acctCodeSal);
							
							if (acctCodeSal != null && acctCodeSal.trim().length() > 0) 
							{
								errcode = finCommon.isAcctCode(siteCode,acctCodeSal, "S-AMD", conn);
								//Changed By Nasruddin 12-OCT-16 Add if Condition
								if( errcode != null && errcode.trim().length() > 0)
								{
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else if("H".equalsIgnoreCase(lsPosttype) && (acctCodeSal == null || acctCodeSal.trim().length() == 0))
							{
								errcode = "VTACTSAL02";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							// end 12-nov-2020 manoharan check post type
							
							
						}
						if (childNodeName.equalsIgnoreCase("cctr_code__sal")) 
						{
							System.out.println("in cctr_code__sal.........");
							cctrCodeSal = checkNull(genericUtility.getColumnValue("cctr_code__sal", dom));
							acctCodeSal = checkNull(genericUtility.getColumnValue("acct_code__sal", dom));
							if(cctrCodeSal != null && cctrCodeSal.trim().length() > 0)
							{
								errcode = finCommon.isCctrCode(acctCodeSal, cctrCodeSal,"S-AMD" , conn);
								//Changed By Nasruddin 12-OCT-16 Add if Condition
								if( errcode != null && errcode.trim().length() > 0)
								{
									System.out.println("errcode in cctr_code__sal"+errcode);
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}
	
					}
				}
				break;

			case 2:
			{
				System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName[" + childNodeName + "]");
					
					if(childNodeName.equalsIgnoreCase("line_no__sord")) 
					{
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						System.out.println("line before"+ lineNoSord);
						if(lineNoSord != null && lineNoSord.trim().length() > 0)
						{
							lineNoSord = getLineNewNo(lineNoSord);
						}
						// commented by mahesh saggam on 01/Aug/19
						/*else
						{
							errcode = "VMEMTLSNO";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}*/
						System.out.println("line after"+ lineNoSord);
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						qtyStduom = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						System.out.println("qtyStduom after"+ qtyStduom);
						if(qtyStduom!= null && qtyStduom.trim().length() > 0)
						{
							qtyStduomDb = Double.parseDouble(qtyStduom);
						}
						{
							sql = "select count(*) from sorddet where sale_order = ? and line_no = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, saleOrder);
							pStmt.setString(2, lineNoSord);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt =rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if(cnt == 0)
							{
								if(itemCode == null || itemCode.trim().length() == 0)
								{
									errcode = "VTSODETLN1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else
							{
								if(cnt == 1)
								{
									sql = "select item_flg from sorddet where sale_order = ? and line_no = ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, saleOrder);
									pStmt.setString(2, lineNoSord);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										itemFlg = checkNull(rs.getString("item_flg"));
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if(!"I".equalsIgnoreCase(itemFlg))
									{
										sql = "select sum(qty_desp) from sorditem where sale_order = ? and line_no = ? and exp_lev   <> '1.' ";
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, saleOrder);
										pStmt.setString(2, lineNoSord);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											mqtyDesp = rs.getDouble(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
										
										if(mqtyDesp > 0)
										{
											errcode = "VTSOAMD3";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
										else
										{
											if ("B".equalsIgnoreCase(itemFlg))
											{
												errcode = "VTSOAMD10";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
											
									}
									else
									{
										sql = "select qty_alloc from sorditem where sale_order = ? and line_no = ? and exp_lev   <> '1.' ";
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, saleOrder);
										pStmt.setString(2, lineNoSord);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											qtyAlloc = rs.getDouble(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
										System.out.println("qtyStduomDb after["+ qtyStduomDb+"]qtyAlloc["+qtyAlloc+"]");
										if( qtyStduomDb < qtyAlloc)
										{
											errcode = "VTSOAMD2";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
							
							
						}
						/*else
						{
							//added new validation for line no sord
							errcode = "VMEMTLINO ";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						*/
						amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						
						sql = "select count(*) from sordamddet where sale_order = ? and line_no__sord = ? and amd_no = ? and line_no <> ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, saleOrder);
						pStmt.setString(2, lineNoSord);
						pStmt.setString(3, amdNo);
						pStmt.setString(4, lineNo);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();rs = null;
						pStmt.close();pStmt = null;		
								
						if(cnt == 1)
						{
							errcode = "VTSORDAMD9";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
								
					}
			
					if (childNodeName.equalsIgnoreCase("site_code")) 
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						
						sql = "select count(*) from site where site_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, siteCode);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							cnt =rs.getInt(1);
								
						}
						rs.close();rs = null;
						pStmt.close();pStmt = null;
						
						if(cnt == 0)
						{
							errcode = "VTSITE1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("quantity"))
					{
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom1));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						amdDate = checkNull(genericUtility.getColumnValue("amd_date", dom1));
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						
						System.out.println("line before"+ lineNoSord);
						if(lineNoSord != null && lineNoSord.trim().length() > 0)
						{
							lineNoSord = getLineNewNo(lineNoSord);
						}
						System.out.println("line after"+ lineNoSord);
						System.out.println("Inside Quantity["+qty +"]");
						
						if((qty != null && qty.trim().length() > 0 ))
						{
							qtyDb = Double.parseDouble(qty);
						}
						
						if( qtyDb < 0 )
						{
							errcode = "VTAMDQTY";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql ="select count(*) from customeritem where  cust_code = ? and    item_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, custCodeDlv);
							pStmt.setString(2, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if(cnt > 0)
							{
								sql ="select integral_qty, restrict_upto  from customeritem where  cust_code = ? and    item_code = ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, custCodeDlv);
								pStmt.setString(2, itemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									integralQty = rs.getDouble("integral_qty");
									restrictUpto = rs.getTimestamp("restrict_upto");
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(restrictUpto != null)
								{
									System.out.println("amdDate["+sdf.parse(amdDate)+"]restrictUpto["+restrictUpto+"]");
									
									if(amdDate != null)
									{
										amdDate1 = sdf.parse(amdDate);
									}
									System.out.println("amdDate1["+amdDate1);
									if(amdDate1.before(restrictUpto) || amdDate1.equals(restrictUpto))
									{
										errcode = "VTRESDT";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								
								if(integralQty > 0 && qtyDb > 0)
								{
									sql ="select fn_mod(?,?) from dual";
									pStmt = conn.prepareStatement(sql);
									pStmt.setDouble(1, qtyDb);
									pStmt.setDouble(2, integralQty);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										lcMod = rs.getDouble(1);
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if(lcMod > 0 )
									{
										errcode = "VTINTQTY";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							else
							{
								intQty = distCommon.getIntegralQty(custCodeDlv, itemCode, siteCode, conn);
								System.out.println(">>>>CommonConstants.DB_NAME:"+ CommonConstants.DB_NAME);
								if(intQty > 0 && qtyDb > 0)
								{
									nature = checkNull(genericUtility.getColumnValue("nature", dom));
									if("C".equalsIgnoreCase(nature) )
									{
										if("db2".equalsIgnoreCase(CommonConstants.DB_NAME))
										{
											sql ="select fn_mod(?,?) from dual";
											pStmt = conn.prepareStatement(sql);
											pStmt.setDouble(2, intQty);
											pStmt.setDouble(1, qtyDb);
											rs = pStmt.executeQuery();
											if (rs.next()) 
											{
												lcMod = rs.getDouble(1);
											}
											rs.close();rs = null;
											pStmt.close();pStmt = null;
											
											if(qtyDb < intQty || lcMod > 0)
											{
												errcode = "VTINTQTY1";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
										else
										{
											sql ="select mod(?,?) from dual";
											pStmt = conn.prepareStatement(sql);
											pStmt.setDouble(2, intQty);
											pStmt.setDouble(1, qtyDb);
											rs = pStmt.executeQuery();
											if (rs.next()) 
											{
												modQty = rs.getDouble(1);
											}
											rs.close();rs = null;
											pStmt.close();pStmt = null;
											
											System.out.println("qtyDb>["+qtyDb+"]intQty["+ intQty +"]modQty["+modQty+"]");
											if(qtyDb < intQty || modQty > 0)
											{
												errcode = "VTINTQTY1";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
								
							}
							
							sql ="select order_type from sorder where sale_order =  ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, saleOrder);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								orderType = checkNull(rs.getString("order_type"));
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							disPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
							
							System.out.println("disPobOrdtypeList.trim().length()"+disPobOrdtypeList.trim().length());
						
							if(disPobOrdtypeList != null && disPobOrdtypeList.trim().length() > 0)
							{
								disPobOrdType = distCommon.getTokenList(disPobOrdtypeList, ",");
								if(orderType.equalsIgnoreCase(disPobOrdType))
								{
									ordFlag = true;
								}
							}
							
							if(errcode == null || errcode.trim().length() == 0)
							{
								nature = checkNull(genericUtility.getColumnValue("nature", dom));
								
								if(("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature)) && ordFlag == false )
								{
									schemeCode = getSchemeCode(dom, dom1, dom2, "", conn);
									
									sql ="select CASE when scheme_flag is null then 'Q' ELSE scheme_flag END  from bom where bom_code = ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, schemeCode);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										schemeFlag = checkNull(rs.getString(1));
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if("Q".equalsIgnoreCase(schemeFlag))
									{
										errcode = valDataGroupScheme(dom, dom1, dom2, "", objContext, editFlag, nature, conn);
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
								        errcode = valDataGroupSchemeValue(dom, dom1, dom2, "", objContext, editFlag, nature, conn);//Pending poonam
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							
							sql ="select sum(qty_desp),sum(case when qty_alloc is null then 0 else qty_alloc end)  from sorditem where sale_order = ? and   line_no	  = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, saleOrder);
							pStmt.setString(2, lineNoSord);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								qtyDesp = rs.getDouble(1);
								qtyAlloc = rs.getDouble(2);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							System.out.println("(qtyDesp + qtyAlloc)["+qtyDesp + qtyAlloc + "]qtyDb[" +qtyDb+"]");
							
							if(qtyDesp + qtyAlloc > qtyDb)
							{
								errcode = "VTSOAMDQTY";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						if(errcode == null || errcode.trim().length() == 0)
						{
							
							qtyO = checkNull(genericUtility.getColumnValue("quantity_o", dom));
							qty = checkNull(genericUtility.getColumnValue("quantity", dom));
							
							if(qtyO != null && qtyO.trim().length() > 0)
							{
								qtyODb = Double.parseDouble(qtyO);
							}
							if(qty != null && qty.trim().length() > 0)
							{
								qtyDb = Double.parseDouble(qty);
							}
							lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
							
							System.out.println("qtyDb["+qtyDb+"]qtyODb["+qtyODb+"]");
							if(lineNoSord != null && lineNoSord.trim().length() > 0)
							{
								if(qtyDb > qtyODb)
								{
									errcode = "VTSOAMD6";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
					}
					if (childNodeName.equalsIgnoreCase("tax_class")) 
					{
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
						
						if(taxClass != null && taxClass.trim().length() > 0)
						{
							sql = "select count(*) from taxclass where tax_class = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxClass);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							System.out.println("In taxClass cnt"+ cnt);
							if(cnt == 0)
							{
								errcode = "VTTCLASS1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_chap")) 
					{
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
						if(taxChap != null && taxChap.trim().length() > 0)
						{
							sql = "select count(*) from taxchap where tax_chap = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxChap);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							System.out.println("In taxChap cnt"+ cnt);
							if(cnt == 0)
							{
								errcode = "VTTCHAP1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_env"))
					{
						//taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						taxEnv = checkNull(distCommon.getParentColumnValue("tax_env", dom, "2"));
						amdDate = checkNull(genericUtility.getColumnValue("amd_date", dom1));
						
						if(amdDate != null && amdDate.trim().length() > 0)
						{
						 amdDate1 = sdf.parse(amdDate);
					     amdDatets = new java.sql.Timestamp(amdDate1.getTime());
						}
						System.out.println("amdDatets"+amdDatets + "amdDate"+amdDate);
						if(taxEnv != null && taxEnv.trim().length() > 0)
						{
							sql = "select count(*) from taxenv where tax_env = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, taxEnv);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt =rs.getInt(1);
								
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							System.out.println("In tax_env cnt"+ cnt);
							if(cnt == 0)
							{
								errcode = "VTTENV1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{//Pavan R 17sept19 start[to validate tax environment]
								//errcode = distCommon.getCheckTaxEnvStatus(taxEnv, amdDatets, conn);
								errcode = distCommon.getCheckTaxEnvStatus(taxEnv, amdDatets,"S", conn);
								//Pavan R 17sept19 end[to validate tax environment]
								//Changes done by Mayur on 14/JUNE/17---[START]
							    if(errcode != null && errcode.trim().length() > 0)
								{
							    	errList.add(errcode);
							    	errFields.add(childNodeName.toLowerCase());
								}
								//Changes done by Mayur on 14/JUNE/17---[END]
								/*errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());*/}
								
						}
						
					}
					if (childNodeName.equalsIgnoreCase("pack_code")) 
					{
						packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
						
						if(packCode != null && packCode.trim().length() != 0 )
						{
							sql ="select count(*) from packing where pack_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, packCode);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
								
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							System.out.println("In pack_code cnt"+ cnt);
							if(cnt == 0)
							{
								errcode = "VTPKCD1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					}
					if (childNodeName.equalsIgnoreCase("unit")) 
					{
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						
						sql ="select count(*) from uom where unit = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, unit);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
							
						}
						rs.close();rs = null;
						pStmt.close();pStmt = null;
						System.out.println("In unit cnt"+ cnt);
						if(cnt == 0)
						{
							errcode = "VTUNIT1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(unitStd.trim().equalsIgnoreCase(unit.trim()))
						{
							
							convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
							
							if(convQtyStduom != null && convQtyStduom.trim().length() > 0)
							{
								convQtyStduom1 = Double.parseDouble(convQtyStduom);
							}
							if(convQtyStduom1 != 1)
							{
								errcode = "VTUCON1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else if(!unitStd.trim().equalsIgnoreCase(unit.trim()))
						{
							sql ="select count(*) from uomconv where (item_code = ? or item_code = 'X') " +
									"and ((unit__fr = ? and unit__to = ?) or (unit__to = ? and unit__fr = ?)) ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							pStmt.setString(2, unit);
							pStmt.setString(3, unitStd);
							pStmt.setString(4, unit);
							pStmt.setString(5, unitStd);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if(cnt == 0)
							{
								errcode = "VTUOMCONV";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("unit__rate")) 
					{
						unit = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						
						sql ="select count(*) from uom where unit = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, unit);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
							
						}
						rs.close();rs = null;
						pStmt.close();pStmt = null;
						System.out.println("In unit__rate cnt"+ cnt);
						if(cnt == 0)
						{
							errcode = "VTUNIT1";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(unitStd.equalsIgnoreCase(unit))
						{
							convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
							
							if(convQtyStduom != null && convQtyStduom.trim().length() > 0)
							{
								// convQtyStduom1 = Integer.parseInt(convQtyStduom);  // commented by mahesh saggam on 05/aug/19 as it was parsing int 
								convQtyStduom1 = Double.parseDouble(convQtyStduom);
							}
							if( convQtyStduom1 != 1)
							{
								errcode = "VTUCON1";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else if(!unitStd.equalsIgnoreCase(unit))
						{
							sql ="select count(*) from uomconv where (item_code = ? or item_code = 'X') " +
									"and ((unit__fr = ? and unit__to = ?) or (unit__to = ? and unit__fr = ?)) ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							pStmt.setString(2, unit);
							pStmt.setString(3, unitStd);
							pStmt.setString(4, unit);
							pStmt.setString(5, unitStd);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if(cnt == 0)
							{
								errcode = "VTUNIT2";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("rate")) 
					{
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						rateO = checkNull(genericUtility.getColumnValue("rate_o", dom));
						
						if(rate != null &&  rate.trim().length() > 0)
						{
							rateDb = Double.parseDouble(rate);
						}
						if(rateO != null && rateO.trim().length() > 0)
						{
							rateODb = Double.parseDouble(rateO);
						}
						System.out.println("rateDb["+rateDb+"]rateODb["+rateODb+"]");
						if(rateDb != rateODb)
						{
							saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
							lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
							System.out.println("line before rate"+ lineNoSord);
							if(lineNoSord != null && lineNoSord.trim().length() > 0)
							{
								lineNoSord = getLineNewNo(lineNoSord);
							}
							System.out.println("line after rate"+ lineNoSord);
							sql = "select count(1) from despatch a,despatchdet b where  a.desp_id = b.desp_id " +
								  " and b.sord_no = ? and b.line_no__sord = ? and a.confirmed = 'Y' ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, saleOrder);
							pStmt.setString(2, lineNoSord);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							if(cnt > 0)
							{
								errcode = "VTSOAMD9";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
						amdDate = checkNull(genericUtility.getColumnValue("amd_date", dom1));
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						nature = checkNull(genericUtility.getColumnValue("nature", dom));
						
						if(amdDate != null && amdDate.trim().length() > 0)
						{
							amdDatets = Timestamp.valueOf(genericUtility.getValidDateString(amdDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							System.out.println("amdDateTs>>"+amdDatets);
						}
						if(qty != null && qty.trim().length() > 0)
						{
							qtyDb = Double.parseDouble(qty) ;
							System.out.println("qtyDb>>"+qtyDb);
						}
						
						if(("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature)) )
						{
							if(rateDb > 0)
							{
								errcode = "VTFREEITEM";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							if("I".equalsIgnoreCase(itemFlg))
							{
								sql = "select max(case when min_rate is null then 0 else min_rate end)  from pricelist " +
									  "where price_list = ? and item_code = ? and list_type = 'L' and  " +
									  "eff_from <= ? and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, priceList);
								pStmt.setString(2, itemCode);
								pStmt.setTimestamp(3,amdDatets);
								pStmt.setTimestamp(4,amdDatets);
								pStmt.setDouble(5, qtyDb);
								pStmt.setDouble(6, qtyDb);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									minRate = rs.getDouble(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(rateDb < minRate )
								{
									errcode = "VTRATE3";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
								
							}
						}
						
						if((priceList == null || priceList.trim().length() == 0) && rateDb == 0)
						{
							errcode = "VTRATE3";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
						
					}
					if (childNodeName.equalsIgnoreCase("rate__clg")) 
					{
						rateClg = checkNull(genericUtility.getColumnValue("rate__clg", dom));
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						

						if(rate != null && rate.trim().length()>0)
						{
							rateDb = Double.parseDouble(rate);
						}
						
						if(rateDb > 0)
						{
							if(rateClg == null || rateClg.trim().length() <= 0)
							{
								errcode = "VTECRNZ";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("item_code")) 
					{
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
						
						sql = "select due_date, order_date, state_code__dlv, cust_code, cust_pord, item_ser, order_type  " +
							  "from sorder where sale_order = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, saleOrder);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							DueDate = rs.getTimestamp("due_date");
							orderDate = rs.getTimestamp("order_date");
							stateCodeDlv = checkNull(rs.getString("state_code__dlv"));
							custCode = checkNull(rs.getString("cust_code"));
							custPord = checkNull(rs.getString("cust_pord"));
							itemSer = checkNull(rs.getString("item_ser"));
							orderType = checkNull(rs.getString("order_type"));
							
						}
						rs.close();rs = null;
						pStmt.close();pStmt = null;
						
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						itemFlg = checkNull(genericUtility.getColumnValue("item_flg", dom));
						
						sql = "select dis_link,channel_partner from customer  where cust_code= ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, custCode);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							disLink = checkNull(rs.getString("dis_link"));
							channelPartner = checkNull(rs.getString("channel_partner"));
						}
						rs.close();rs = null;
						pStmt.close();pStmt = null;
						
						if(("A".equalsIgnoreCase(disLink) || "S".equalsIgnoreCase(disLink)) && "Y".equalsIgnoreCase(channelPartner))
						{
							if(custPord != null && custPord.trim().length() > 0)
							{
								//Pavan Rane 13sep19 start [bug fixed]
								//sql = "select count(*) from porddet where purc_order = ? and and	item_code  = ? and	status = 'O'";
								sql = "select count(*) from porddet where purc_order = ? and item_code = ? and status = 'O'";
								//Pavan Rane 13sep19 end [bug fixed]
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, custPord);
								pStmt.setString(2, itemCode);
								rs = pStmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();rs = null;
								pStmt.close();pStmt = null;
								
								if(cnt == 0)
								{
									errcode = "VTPODET";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						if(errcode == null || errcode.trim().length() == 0)
						{
							sql = "Select case when eou is null then 'N' else eou end  From site Where site_code = ?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, siteCode);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								lsEou = rs.getString(1);
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							if("Y".equalsIgnoreCase(lsEou))
							{
								exportOrderType = distCommon.getDisparams("999999", "EXPORT_DESPATCH_ORDER_TYPE", conn);
								if(exportOrderType != null && exportOrderType.trim().length() > 0 && exportOrderType.equalsIgnoreCase(orderType))
								{
									sql = "Select lop_reqd From itemser Where item_ser = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, siteCode);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										lopReqd = rs.getString(1);
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if("Y".equalsIgnoreCase(lopReqd))
									{
										sql = " Select count(1) from lop_hdr a, lop_det b where a.lop_ref_no = b.lop_ref_no and " +
											  " a.site_code = ? and a.confirmed = 'Y' and b.item_code = ? and b.item_status ='A' and" +
											  " ? >= a.valid_from and ? <= a.valid_to and b.buy_sell_flag in ('S','B')";
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, siteCode);
										pStmt.setString(2, itemCode);
										pStmt.setTimestamp(3, orderDate);
										pStmt.setTimestamp(4, orderDate);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											cnt = rs.getInt(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
										
										if(cnt == 0)
										{
											errcode = "VTLOPITEM1";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								
							}
						}
						disPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
						ordFlag = false;
						System.out.println("disPobOrdtypeList.trim().length()13"+disPobOrdtypeList.trim().length());
						if(disPobOrdtypeList.trim().length() > 0)
						{
							disPobOrdType = distCommon.getTokenList(disPobOrdtypeList, ",");
							System.out.println("(orderType.equalsIgnoreCase(disPobOrdType in 2nd while))["+orderType.equalsIgnoreCase(disPobOrdType) + "]");
							System.out.println("disPobOrdType"+disPobOrdType+"orderType"+orderType);
							if(orderType.equalsIgnoreCase(disPobOrdType))
							{
								ordFlag = true;
							}
						}
						
						if(errcode == null || errcode.trim().length() == 0)
						{
							if("B".equalsIgnoreCase(itemFlg) && ordFlag == false)
							{
								if (itemCode == null || itemCode.trim().length() == 0)
								{
										errcode = "VTITEM8";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									qty = checkNull(genericUtility.getColumnValue("quantity", dom));
									
									if(qty != null && qty.trim().length() > 0)
									{
										qtyDb = Double.parseDouble(qty);
									}
									//itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
									
									sql = "Select count(1) from bom  Where bom_code = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, itemCode);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										bomCnt = rs.getInt(1);
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									System.out.println("Bom existence"+bomCnt);
									if(bomCnt == 0 )
									{
										errcode = "VTITEM8";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
									
									sql = "select app_from,	valid_upto,	apply_cust_list,noapply_cust_list,	order_type from scheme_applicability  Where scheme_code = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, itemCode);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										cnt++;
										appFrom = rs.getTimestamp("app_from");
										validUpto = rs.getTimestamp("valid_upto");
										applyCustList = checkNull(rs.getString("apply_cust_list"));
										noapplyCustList = checkNull(rs.getString("noapply_cust_list"));
										applicableOrdtypes = checkNull(rs.getString("order_type"));
										
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									System.out.println(" applicability period "+cnt);
									
									if(cnt == 0)
									{
										errcode = "VTITEM8";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
									
									Date DueDate1 = null ,validUpto1 = null ,appFrom1  = null  ; 
									if(DueDate != null)
									{
										DueDate1 = new Date(DueDate.getTime());
									}
									if(validUpto != null)
									{
										 validUpto1 = new Date(validUpto.getTime());
									}
									if(appFrom != null)
									{
										 appFrom1 = new Date(appFrom.getTime());
									}
									System.out.println("DueDate1["+DueDate1+"]appFrom1["+appFrom1+"]validUpto1["+validUpto1+"]");
									
									if(DueDate1 != null && appFrom1 != null && validUpto1 != null)
									{
										if(DueDate1.before(appFrom1) || DueDate1.after(validUpto1))
										{
											errcode = "VTITEM8";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									System.out.println("order type "+applicableOrdtypes);
									
									//schemeCode = getCustSchemeCode(dom,dom1,orderType,applicableOrdtypes, applyCustList ,itemCode,noapplyCustList ,conn);
									
									if("NE".equalsIgnoreCase(orderType) && (applicableOrdtypes == null || applicableOrdtypes.trim().length() == 0))
									{
										errcode = "VTITEM8";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(applicableOrdtypes != null&& applicableOrdtypes.trim().length() > 0)
									{

										lbProceed = false ;
										String lsApplicableOrdTypesArr[]= applicableOrdtypes.split(",");
										for(int i=0;i<lsApplicableOrdTypesArr.length;i++)
										{
											System.out.println("lsApplicableOrdTypesArr[i]"+lsApplicableOrdTypesArr[i]);
											if(orderType.equalsIgnoreCase(lsApplicableOrdTypesArr[i]))
											{
												lbProceed=false;
											}
										}
										if(!lbProceed)
										{
											errcode = "VTITEM8";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									
									if(applyCustList.trim().length() > 0)
									{
										System.out.println("4. applicable customer"+applyCustList ) ;
										custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
										lbProceed =  false;
										String applyCustListArr[]=applyCustList.split(",");
										for(int i=0; i < applyCustListArr.length;i++)
										{
											System.out.println("applyCustListArr[i]" + applyCustListArr[i]);
											
											if(applyCustListArr[i].equalsIgnoreCase(custCode))
											{
												lbProceed =  true;
											}
										}
										
										if(!lbProceed)
										{
											errcode = "VTITEM8";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									
									System.out.println("5. non-applicable customer"+ noapplyCustList ) ;
									if(noapplyCustList.trim().length() > 0 && schemeCode != null)
									{
										custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
										lbProceed =  true;
										
										if(noapplyCustList.trim().length() > 0 && schemeCode != null)
										{
											custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
											String noapplyCustListArr[]=noapplyCustList.split(",");
											for(int i=0; i < noapplyCustListArr.length;i++)
											{
												System.out.println("noapplyCustListArr[i]" + noapplyCustListArr[i]);
												if(noapplyCustListArr[i].equalsIgnoreCase(custCode))
												{
													lbProceed = false ;
												}
											}
										}
										if(!lbProceed)
										{
											errcode = "VTITEM8";
											errList.add(errcode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									
									System.out.println("6. quantity slab"+ nature ) ;
									if("C".equalsIgnoreCase(nature))
									{
										sql = "Select count(1) from scheme_applicability A ,bom	b Where A.scheme_code = b.bom_code And	 B.bom_code 	= ?" +
												"And	 ? between case when b.min_qty is null then 0 else b.min_qty end And case when b.max_qty is null then 0 else b.max_qty end";
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, itemCode);
										pStmt.setDouble(2, qtyDb );
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											llCnt = rs.getInt(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
										
										
									}
									else
									{
										sql = "Select count(1) from scheme_applicability A ,bom	b Where A.scheme_code = b.bom_code And	 B.bom_code 	= :mval" ;
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, itemCode);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											llCnt = rs.getInt(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
									}
									
									if(llCnt == 0)
									{
										errcode = "VTITEM8";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								
									System.out.println("7. site_code  / state (VTITEM6)" ) ;
								
									sql = "Select count(1) from scheme_applicability_det where  scheme_code = ? and site_code = ? and state_code is null";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, itemCode);
									pStmt.setString(2, siteCode);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										mCnt = rs.getInt(1);
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if(mCnt == 0)
									{
										//sql = "Select count(1) from scheme_applicability_det where  scheme_code = ? and and site_code is null and state_code = :mstate_cd"; // commented by mahesh saggam on 01/aug/19
										//Pavan Rane 13sep19 start [bug fixed]
										//sql = "Select count(1) from scheme_applicability_det where  scheme_code = ? and and site_code is null and state_code = ?"; // changes made to state_code by mahesh saggam on 01/aug/19
										sql = "Select count(1) from scheme_applicability_det where  scheme_code = ? and site_code is null and state_code = ?";
										//Pavan Rane 13sep19 end [bug fixed]
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, itemCode);
										pStmt.setString(2, stateCodeDlv);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											mCnt = rs.getInt(1);
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
									}
									
									if(mCnt == 0)
									{
										errcode = "VTITEM6";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
									
									System.out.println("8. customer_series..." ) ;
									//Pavan Rane 13sep19 start [bug fixed] 
									//sql = "Select item_code from customer_series where  cust_code = ? and item_ser = ?";
									sql = "Select count(*) from customer_series where  cust_code = ? and item_ser = ?";
									//Pavan Rane 13sep19 end [bug fixed] 
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, custCode);
									pStmt.setString(2, itemSer);
									rs = pStmt.executeQuery();
									if (rs.next()) 
									{
										custSerCnt = rs.getInt(1);
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									
									if(custSerCnt == 0 && bomCnt == 0)
									{
										errcode = "VTITEM7";
										errList.add(errcode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							
							if("I".equalsIgnoreCase(itemFlg))
							{
								errcode = isItem(siteCode, itemCode,"S-AMD", conn);
								if(errcode.trim().length() == 0)
								{
									itemSerNew = distCommon.getItemSer(itemCode, siteCode, orderDate, custCode, "C", conn);
									errcode = distCommon.getToken(itemSerNew, "~t");
									if(errcode == null || errcode.trim().length() == 0)
									{ 
										sql = "select oth_series from itemser where item_ser = ?";
										pStmt = conn.prepareStatement(sql);
										pStmt.setString(1, itemSer);
										rs = pStmt.executeQuery();
										if (rs.next()) 
										{
											othSeries = checkNull(rs.getString("oth_series"));
										}
										rs.close();rs = null;
										pStmt.close();pStmt = null;
										
										if(othSeries == null || othSeries == "")
										{
											othSeries = "N";
										}
										
										if(itemSer.trim() != itemSerNew  && "G".equalsIgnoreCase(othSeries))
										{
											sql = "select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) from itemser where  item_ser = ? ";
											pStmt = conn.prepareStatement(sql);
											pStmt.setString(1, itemSer);
											rs = pStmt.executeQuery();
											if (rs.next()) 
											{
												itemSerCrpolicyHdr = checkNull(rs.getString(1));
											}
											rs.close();rs = null;
											pStmt.close();pStmt = null;
											
											
											sql = "select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) from itemser where  item_ser = ? ";
											pStmt = conn.prepareStatement(sql);
											pStmt.setString(1, itemSerNew);
											rs = pStmt.executeQuery();
											if (rs.next()) 
											{
												itemSerCrpolicy = checkNull(rs.getString(1));
											}
											rs.close();rs = null;
											pStmt.close();pStmt = null;
											
											if (!itemSerCrpolicy.equalsIgnoreCase(itemSerCrpolicyHdr))
											{
												errcode = "VTITEM2";
												errList.add(errcode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								
								}
							}
							
							sql = "select stop_business, case when sale_option is null then 'A' else sale_option end , contract_req" +
								  " from item where  item_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, itemCode);
							rs = pStmt.executeQuery();
							if (rs.next()) 
							{
								stopBusiness = checkNull(rs.getString("stop_business"));
								salesOptionItem = checkNull(rs.getString(2));
								contractReq = checkNull(rs.getString("contract_req"));
							}
							rs.close();rs = null;
							pStmt.close();pStmt = null;
							
							System.out.println("stopBusiness["+stopBusiness+"]salesOptionItem"+salesOptionItem+"contractReq"+contractReq);
							if("Y".equalsIgnoreCase(stopBusiness))	
							{
								errcode = "VTIIC" ;
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					if (childNodeName.equalsIgnoreCase("min_shelf_life")) 
					{
						minshelfLife = checkNull(genericUtility.getColumnValue("min_shelf_life", dom));
						minshelfLifeO = checkNull(genericUtility.getColumnValue("min_shelf_life_o", dom));
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom)); // added by mahesh saggam on 05/aug/19
						
						if(minshelfLife != null && minshelfLife.trim().length() > 0)
						{
							minshelfLifeDb = Double.parseDouble(minshelfLife);
						}
						if(minshelfLifeO != null && minshelfLifeO.trim().length() > 0)
						{
							minshelfLifeODb = Double.parseDouble(minshelfLifeO);
						}
						
						// if(minshelfLife != null && minshelfLife.trim().length() > 0) // commented by  mahesh saggam on 05/aug/19
						if(minshelfLife != null && minshelfLife.trim().length() > 0 && lineNoSord != null && lineNoSord.trim().length() > 0) // added by mahesh saggam on 05/aug/19
						{
							if(minshelfLifeDb > minshelfLifeODb)
							{
								errcode = "VTSELFLIFE";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
					}
					
				}
			}
				break;
		}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			System.out.println("errListSize [" + errListSize+ "] errFields size [" + errFields.size() + "]");
			if ((errList != null) && (errListSize > 0)) 
			{
				System.out.println("Inside errList>" + errList);
				for (cnt = 0; cnt < errListSize; cnt++) 
				{
					errcode = (String) errList.get(cnt);
					System.out.println("errcode :" + errcode);
					int pos = errcode.indexOf("~");
					System.out.println("pos :" + pos);
					if (pos > -1) 
					{
						errcode = errcode.substring(0, pos);
					}
					System.out.println("despList>>["+despList+"]");
					/*if(errcode.equalsIgnoreCase("VDSPUNCONF"))
					{
						//errcode = errcode + despList;
						 errString = itmDBAccessEJB.getErrorString("sale_order","VDSPUNCONF",userId,"",conn);
						 System.out.println("errString@@@>>>["+errString+"]");
						 String begPart = errString.substring( 0, errString.indexOf("<description>") + 35 );
		                 String endPart = errString.substring( errString.indexOf("</description>"));
		                 String mainStr= ","+despListArr+"";
		                 mainStr=begPart+despListArr+endPart;
			                
		                 errcode = mainStr;
		                 
		                 System.out.println("@@@@@ modify errString["+errcode+"]");
					}*/
					
	                 
					System.out.println("error code is :" + errcode);
					errFldName = (String) errFields.get(cnt);
					System.out.println(" cnt [" + cnt + "] errcode [" + errcode+ "] errFldName [" + errFldName + "]");
					if (errcode != null && errcode.trim().length() > 0) 
					{
						errString = getErrorString(errFldName, errcode, userId);
						errorType = errorType(conn, errcode);
					}
					System.out.println("errorType :[" + errorType+ "]errString[" + errString + "]");
					if (errString != null && errString.trim().length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString+ errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						
						System.out.println("@@@@@ bifurErrString["+bifurErrString+"]");
						
						
						errStringXml.append(bifurErrString);
						System.out.println("@@@@@ errStringXml["+errStringXml+"]");
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}

		} catch (Exception e) 
		{
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} 
		finally 
		{
			try 
			{
				if (conn != null) 
				{
					if (pStmt != null) 
					{
						pStmt.close();
						pStmt = null;
					}
					if (pStmt1 != null) 
					{
						pStmt1.close();
						pStmt1 = null;
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (rs1 != null)
					{
						rs1.close();
						rs1 = null;
					}
					conn.close();
				}
				conn = null;
			} 
			catch (Exception d) 
			{
				d.printStackTrace();
			}
			System.out.println(" < SOrederAmd > CONNECTION IS CLOSED");
		}

		System.out.println("ErrString ::[ " + errStringXml.toString() + " ]");
		return errStringXml.toString();
	}

	public String itemChanged() throws RemoteException, ITMException
	{
        System.out.println("Itemchanged return ");
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try {
			System.out.println("xmlString::" + xmlString);
			dom = parseString(xmlString);
			System.out.println("xmlString1::" + xmlString1);
			dom1 = parseString(xmlString1);

			if (xmlString2.trim().length() > 0) 
			{
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e) 
		{
			System.out.println("Exception : [StarclubEligWorkIC][itemChanged] :==>\n"+ e.getMessage());
			
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("Inside ItemChanged..........."+xtraParams);
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		Node parentNode = null;
		int ctr = 0,currentFormNo = 0  ,cnt = 0 ;
		SimpleDateFormat sdf = null;
		Timestamp morderDate =null,mtrandate = null ,amdDate1 = null;
		double exchRate = 0,  taxAmt = 0, totAmt = 0, ordAmt = 0, exchRateFrt =  0.0,exchRateIns =  0.0,frtAmt = 0.0,
				insAmt = 0.0 ,discount = 0.0 ,quantity = 0.0 ,rateD = 0.0 , netAmt = 0.0,qStduom = 0.0, rStduom = 0.0,minShelfLife = 0.0,
				maxShelfLife = 0.0 ,minLifePerc = 0.0 ,shelfLife = 0.0 ,temp = 0.0 ,integralQty = 0.0 , mrate = 0.0 ,idRatewtDiscount = 0.0 ,
				lcPlistDisc = 0.0 ,ldRate = 0.0 ,convQtyStduomDb = 0.0 ,qtyDb = 0.0 ,convRtuomStduomDb = 0.0 ,rateDb = 0.0 ,discountStrDb = 0.0;
		boolean ordFlag = false ,lbProceed = false ;
		ArrayList mNum = new ArrayList(); 
		int  llCnt = 0 ,pos = 0 ,llSchcnt = 0 ,lineNoSordInt = 0;
		String sql, sql1 = "" ,sqlNew = "", loginSite = "",columnValue = "",chguser = "", empCode = "", deptCode = "", reasCode = "", 
		reasCodeDescr = "", saleOrder = "",custCodeDlv = "",custCodeBil =  "",custPord =  "",partQty = "",
		commPerc = "",taxClass = "",taxChap = "",taxEnv =  "",crTerm =  "",dlvAdd1 = "",dlvAdd2 =  "",dlvCity =  "",
		countCodeDlv = "",dlvPin =  "",stanCode =  "",advPerc =  "",distRoute =  "",orderType =  "",orderMode =  "",siteCode =  "",
		currCodeFrt =  "",currCodeIns =  "",transMode =  "",frtTerm =  "",dlvTerm = "",priceList =  "",priceListDisc =  "",
		udfStr1 =  "",remarks =  "",remarks2 =  "",remarks3 =  "",siteCodeShip =  "",stanCodeInit =  "",dlvAdd3 =  "", tranCode = "",
		itemSer =  "",salesPers =  "",currCode =  "", spName = "",custCodeDlvDescr = "",custCodeBilDescr = "", taxClassDescr = "",
		taxChapDescr = "",taxEnvDescr = "", dlvTermDescr = "",tranCodeDescr = "",distRouteDescr= "",crTermDescr = "", countCodeDlvDescr= "",
		stanCodeDescr= "",empCodeOrd = "",acctCodeSal =  "",cctrCodeSal = "",stdExrt = "",descr = "",itemCode= "" ,
		itemCodeOrd = "",lineNoSord = "",amdNo = "", quotNo = "",rate = "",plistType = "",
		priceListClg = "", packInstr = "",noArt= "",packCode = "",itemDescr =  "",unitStd = "",convQtystduom =  "",rateClg =  "",unit = "",
		unitRate =  "",convRtuomStduom =  "",itemFlg =  "",custSpecNo =  "",nature= "",
		convQtyStduom = "",qty = "",reStr = "",
		custCode = "" , amdDate = "",mdescr = "" , uom= "", uomr= "" , mpackinstr = "" , salesOrderType = "",varValue = "" , mstateCd= "",
		schemeCode= "" ,lsType = "" , curScheme = "" ,itemCodeParent = "" ,applyCustList = "", noapplyCustList = "" , applicableOrdTypes= "" ,
		prevScheme= "" ,applyCust = "" , noApplyCust = "" ,itemStru = "" ,disPobOrdtypeList = "" ,disPobOrdType = "",mpriceList = "",siteCodeDet = "",
		taxClassHdr= "",stanCodeTo = "",frStanCode = "" ,plistDate = "" , pListDisc= "" ,discountStr = "",listType = "" ;
		
		Timestamp plDate =  null ,plDate1 = null,pordDate = null ,dueDate = null,taxDate = null ,dspDate =null ;
		try 
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
			connDriver = null;
			java.util.Date today = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			today = cal.getTime();

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(today);

			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			empCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("loginSite[" + loginSite + "][chguserhdr "+ chguser +"]empCode["+ empCode );
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");

			System.out.println("Current Form No [" + currentFormNo + "]");
			switch (currentFormNo)
			{
				case 1: 
				{
					valueXmlString.append("<Detail1>");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					ctr = 0;
					System.out.println("[" + currentColumn + "] ==> '"+ columnValue + "'");
					if (currentColumn.trim().equals("itm_default")) 
					{
						System.out.println("Inside itm_default..."+sysDate);
						valueXmlString.append("<amd_date>").append("<![CDATA[" + sysDate + "]]>").append("</amd_date>");
						valueXmlString.append("<order_status>").append("<![CDATA[" + "O" + "]]>").append("</order_status>");
						valueXmlString.append("<emp_code__amd>").append("<![CDATA[" + empCode + "]]>").append("</emp_code__amd>");
						
						sql = "select dept_code from employee where emp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, empCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							deptCode = checkNull(rs.getString(1));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("Inside deptCode..."+deptCode);
						valueXmlString.append("<dept_code>").append("<![CDATA[" + deptCode + "]]>").append("</dept_code>");
					}
					if (currentColumn.trim().equals("itm_defaultedit"))
					{
						
						reasCode = checkNull(genericUtility.getColumnValue("reas_code", dom));
						if(reasCode.trim().length() > 0 )
						{
							sql = "select descr from gencodes where mod_name = 'W_SORDAMD' and fld_name = 'REAS_CODE' AND fld_value= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, reasCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								reasCodeDescr =  checkNull(rs.getString(1));
							}
							else
							{
								sql1 = "select descr from gencodes where mod_name = 'X' and fld_name = 'REAS_CODE' AND fld_value= ?";
								pstmt1 =  conn.prepareStatement(sql1);
								pstmt1.setString(1, reasCode);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									reasCodeDescr =  checkNull(rs1.getString(1));
								}
								rs1.close();rs1 = null;
								pstmt1.close();pstmt1 = null;
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							if(reasCodeDescr != null)
							{
								valueXmlString.append("<reas_desc>").append("<![CDATA[" + reasCodeDescr + "]]>").append("</reas_desc>");
							}
						}
						valueXmlString.append("<emp_code__amd>").append("<![CDATA[" + empCode + "]]>").append("</emp_code__amd>");
						
						sql = "select dept_code from employee where emp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, empCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							deptCode =  checkNull(rs.getString(1));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						valueXmlString.append("<dept_code>").append("<![CDATA[" + deptCode + "]]>").append("</dept_code>");
						
					}
					if (currentColumn.trim().equals("reas_code")) 
					{
						System.out.println("iNSIDE IC REAS CODE.... ");
						reasCode = checkNull(genericUtility.getColumnValue("reas_code", dom));
						System.out.println("iNSIDE IC REAS CODE.... "+reasCode);
						if(reasCode.trim().length() > 0 )
						{
							sql = "select descr from gencodes where mod_name = 'W_SORDAMD' and fld_name = 'REAS_CODE' AND fld_value= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, reasCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								reasCodeDescr =  checkNull(rs.getString(1));
							}
							else
							{
								sql1 = "select descr from gencodes where mod_name = 'X' and fld_name = 'REAS_CODE' AND fld_value= ?";
								pstmt1 =  conn.prepareStatement(sql1);
								pstmt1.setString(1, reasCode);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									reasCodeDescr =  checkNull(rs1.getString(1));
								}
								rs1.close();rs1 = null;
								pstmt1.close();pstmt1 = null;
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							if(reasCodeDescr != null)
							{
								valueXmlString.append("<reas_desc>").append("<![CDATA[" + reasCodeDescr + "]]>").append("</reas_desc>");
							}
						}
					}
					if (currentColumn.trim().equals("sale_order")) 
					{
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						System.out.println("iNSIDE IC saleOrder .... "+saleOrder);
						
						if(saleOrder != null && saleOrder.trim().length() > 0)
						{
							sql = " select cust_code__dlv,cust_code__bil,cust_pord,comm_perc,tax_class, " +
								  " tax_date,tax_chap,tax_env,cr_term,dlv_add1,dlv_add2, dlv_city,  " 	 +
								  " count_code__dlv,dlv_pin,stan_code,part_qty,tran_code,ord_amt, "	 +
								  " tax_amt,tot_amt,pord_date,adv_perc,dist_route,order_type,order_mode,site_code," 	  +
								  " curr_code__frt, curr_code__ins, trans_mode, frt_term,"				 +
								  " exch_rate__frt, exch_rate__ins, frt_amt, ins_amt,dlv_term," 		 +
								  " price_list, pl_date, price_list__disc, udf__str1, "				 +
								  " remarks, remarks2, remarks3, site_code__ship,stan_code__init, dlv_add3, exch_rate, " +
								  " item_ser, emp_code__ord, sales_pers ,curr_code ," 					 +
								  " acct_code__sal , cctr_code__sal from sorder where sale_order = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custCodeDlv =  checkNull(rs.getString("cust_code__dlv"));
								custCodeBil =  checkNull(rs.getString("cust_code__bil"));
								custPord =  checkNull(rs.getString("cust_pord"));
								commPerc =  checkNull(rs.getString("comm_perc"));
								taxClass =  checkNull(rs.getString("tax_class"));
								taxDate =  rs.getTimestamp("tax_date");
								taxChap =  checkNull(rs.getString("tax_chap"));
								taxEnv =  checkNull(rs.getString("tax_env"));
								crTerm =  checkNull(rs.getString("cr_term"));
								dlvAdd1 =  checkNull(rs.getString("dlv_add1"));
								dlvAdd2 =  checkNull(rs.getString("dlv_add2"));
								dlvCity =  checkNull(rs.getString("dlv_city"));
								countCodeDlv =  checkNull(rs.getString("count_code__dlv"));
								dlvPin =  checkNull(rs.getString("dlv_pin"));
								stanCode =  checkNull(rs.getString("stan_code"));
								partQty =  checkNull(rs.getString("part_qty"));
								tranCode =  checkNull(rs.getString("tran_code"));
								ordAmt = rs.getDouble("ord_amt");
								taxAmt =  rs.getDouble("tax_amt");
								totAmt =  rs.getDouble("tot_amt");
								pordDate =  rs.getTimestamp("pord_date");
								advPerc =  checkNull(rs.getString("adv_perc"));
								distRoute =  checkNull(rs.getString("dist_route"));
								orderType =  checkNull(rs.getString("order_type"));
								orderMode =  checkNull(rs.getString("order_mode"));
								siteCode =  checkNull(rs.getString("site_code"));
								currCodeFrt =  checkNull(rs.getString("curr_code__frt"));
								currCodeIns =  checkNull(rs.getString("curr_code__ins"));
								transMode =  checkNull(rs.getString("trans_mode"));
								frtTerm =  checkNull(rs.getString("frt_term"));
								exchRateFrt =  rs.getDouble("exch_rate__frt");
								exchRateIns =  rs.getDouble("exch_rate__ins");
								frtAmt =  rs.getDouble("frt_amt");
								insAmt =  rs.getDouble("ins_amt");
								dlvTerm =  checkNull(rs.getString("dlv_term"));
								priceList =  checkNull(rs.getString("price_list"));
								plDate =  rs.getTimestamp("pl_date");
								priceListDisc =  checkNull(rs.getString("price_list__disc"));
								udfStr1 =  checkNull(rs.getString("udf__str1"));
								remarks =  checkNull(rs.getString("remarks"));
								remarks2 =  checkNull(rs.getString("remarks2"));
								remarks3 =  checkNull(rs.getString("remarks3"));
								siteCodeShip =  checkNull(rs.getString("site_code__ship"));
								stanCodeInit =  checkNull(rs.getString("stan_code__init"));
								dlvAdd3 =  checkNull(rs.getString("dlv_add3"));
								exchRate = rs.getDouble("exch_rate");
								itemSer =  checkNull(rs.getString("item_ser"));
								empCodeOrd =  checkNull(rs.getString("emp_code__ord"));
								salesPers =  checkNull(rs.getString("sales_pers"));
								currCode =  checkNull(rs.getString("curr_code"));
								acctCodeSal =  checkNull(rs.getString("acct_code__sal"));
								cctrCodeSal =  checkNull(rs.getString("cctr_code__sal"));
								
								
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select cust_name from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCodeDlv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custCodeDlvDescr =  checkNull(rs.getString("cust_name"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select cust_name from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCodeBil);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custCodeBilDescr =  checkNull(rs.getString("cust_name"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from taxclass where tax_class = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								taxClassDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from taxchap where tax_chap = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								taxChapDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from taxenv where tax_env = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, taxEnv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								taxEnvDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from crterm where cr_term = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								crTermDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from country where count_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, countCodeDlv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								countCodeDlvDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								stanCodeDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
					
							sql = "select tran_name from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								tranCodeDescr =  checkNull(rs.getString("tran_name"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from distroute where dist_route = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, distRoute);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								distRouteDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select descr from delivery_term where dlv_term = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, dlvTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								dlvTermDescr =  checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							sql = "select sp_name from sales_pers where sales_pers = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, salesPers);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								spName =  checkNull(rs.getString("sp_name"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
						}
						
						System.out.println("(sdf.format(taxDate).toString())"+taxDate);
						
						valueXmlString.append("<cust_code__dlv_o>").append("<![CDATA[" + custCodeDlv + "]]>").append("</cust_code__dlv_o>");
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + custCodeDlv + "]]>").append("</cust_code__dlv>");
						valueXmlString.append("<customer_a_cust_name>").append("<![CDATA[" + custCodeDlvDescr + "]]>").append("</customer_a_cust_name>");
						valueXmlString.append("<cust_code__bil_o>").append("<![CDATA[" + custCodeBil + "]]>").append("</cust_code__bil_o>");
						valueXmlString.append("<cust_code__bil>").append("<![CDATA[" + custCodeBil + "]]>").append("</cust_code__bil>");
						valueXmlString.append("<customer_b_cust_name>").append("<![CDATA[" + custCodeBilDescr + "]]>").append("</customer_b_cust_name>");
						valueXmlString.append("<cust_pord_o>").append("<![CDATA[" + custPord + "]]>").append("</cust_pord_o>");
						valueXmlString.append("<cust_pord>").append("<![CDATA[" + custPord + "]]>").append("</cust_pord>");
						valueXmlString.append("<comm_perc_o>").append("<![CDATA[" + commPerc + "]]>").append("</comm_perc_o>");
						valueXmlString.append("<comm_perc>").append("<![CDATA[" + commPerc + "]]>").append("</comm_perc>");
						valueXmlString.append("<tax_class_o>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class_o>");
						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
						valueXmlString.append("<taxclass_descr>").append("<![CDATA[" + taxClassDescr + "]]>").append("</taxclass_descr>");
						if(taxDate != null)
						{
						valueXmlString.append("<tax_date_o>").append("<![CDATA[" + (sdf.format(taxDate)).toString() + "]]>").append("</tax_date_o>");
						valueXmlString.append("<tax_date>").append("<![CDATA[" +  (sdf.format(taxDate)).toString() + "]]>").append("</tax_date>");
						}
						valueXmlString.append("<tax_chap_o>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap_o>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<taxchap_descr>").append("<![CDATA[" + taxChapDescr + "]]>").append("</taxchap_descr>");
						valueXmlString.append("<tax_env_o>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env_o>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						valueXmlString.append("<taxenv_descr>").append("<![CDATA[" + taxEnvDescr + "]]>").append("</taxenv_descr>");
						valueXmlString.append("<cr_term_o>").append("<![CDATA[" + crTerm + "]]>").append("</cr_term_o>");
						valueXmlString.append("<cr_term>").append("<![CDATA[" + crTerm + "]]>").append("</cr_term>");
						valueXmlString.append("<crterm_descr>").append("<![CDATA[" + crTermDescr + "]]>").append("</crterm_descr>");
						valueXmlString.append("<dlv_add1_o>").append("<![CDATA[" + dlvAdd1 + "]]>").append("</dlv_add1_o>");
						valueXmlString.append("<dlv_add1>").append("<![CDATA[" + dlvAdd1 + "]]>").append("</dlv_add1>");
						valueXmlString.append("<dlv_add2_o>").append("<![CDATA[" + dlvAdd2 + "]]>").append("</dlv_add2_o>");
						valueXmlString.append("<dlv_add2>").append("<![CDATA[" + dlvAdd2 + "]]>").append("</dlv_add2>");
						valueXmlString.append("<dlv_city_o>").append("<![CDATA[" + dlvCity + "]]>").append("</dlv_city_o>");
						valueXmlString.append("<dlv_city>").append("<![CDATA[" + dlvCity + "]]>").append("</dlv_city>");
						valueXmlString.append("<count_code__dlv_o>").append("<![CDATA[" + countCodeDlv + "]]>").append("</count_code__dlv_o>");
						valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countCodeDlv + "]]>").append("</count_code__dlv>");
						valueXmlString.append("<country_descr>").append("<![CDATA[" + countCodeDlvDescr + "]]>").append("</country_descr>");
						valueXmlString.append("<dlv_pin_o>").append("<![CDATA[" + dlvPin + "]]>").append("</dlv_pin_o>");
						valueXmlString.append("<dlv_pin>").append("<![CDATA[" + dlvPin + "]]>").append("</dlv_pin>");
						valueXmlString.append("<stan_code_o>").append("<![CDATA[" + stanCode + "]]>").append("</stan_code_o>");
						valueXmlString.append("<stan_code>").append("<![CDATA[" + stanCode + "]]>").append("</stan_code>");
						valueXmlString.append("<station_descr>").append("<![CDATA[" + stanCodeDescr + "]]>").append("</station_descr>");
						valueXmlString.append("<part_qty_o>").append("<![CDATA[" + partQty + "]]>").append("</part_qty_o>");
						valueXmlString.append("<part_qty>").append("<![CDATA[" + partQty + "]]>").append("</part_qty>");
						valueXmlString.append("<tran_code_o>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code_o>");
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
						valueXmlString.append("<transporter_tran_name>").append("<![CDATA[" + tranCodeDescr + "]]>").append("</transporter_tran_name>");
						valueXmlString.append("<ord_amt_o>").append("<![CDATA[" + ordAmt + "]]>").append("</ord_amt_o>");
						valueXmlString.append("<ord_amt>").append("<![CDATA[" + 0 + "]]>").append("</ord_amt>");
						valueXmlString.append("<tax_amt_o>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt_o>");
						valueXmlString.append("<tax_amt>").append("<![CDATA[" + 0 + "]]>").append("</tax_amt>");
						valueXmlString.append("<tot_amt_o>").append("<![CDATA[" + totAmt + "]]>").append("</tot_amt_o>");
						valueXmlString.append("<tot_amt>").append("<![CDATA[" + 0 + "]]>").append("</tot_amt>");
						if(pordDate != null)
						{
						valueXmlString.append("<pord_date_o>").append("<![CDATA[" +  (sdf.format(pordDate)).toString() + "]]>").append("</pord_date_o>");
						valueXmlString.append("<pord_date>").append("<![CDATA[" + (sdf.format(pordDate)).toString() + "]]>").append("</pord_date>");
						}
						valueXmlString.append("<adv_perc_o>").append("<![CDATA[" + advPerc + "]]>").append("</adv_perc_o>");
						valueXmlString.append("<adv_perc>").append("<![CDATA[" + advPerc + "]]>").append("</adv_perc>");
						valueXmlString.append("<dist_route_o>").append("<![CDATA[" + distRoute + "]]>").append("</dist_route_o>");
						valueXmlString.append("<dist_route>").append("<![CDATA[" + distRoute + "]]>").append("</dist_route>");
						valueXmlString.append("<distroute_descr>").append("<![CDATA[" + distRouteDescr + "]]>").append("</distroute_descr>");
						valueXmlString.append("<amd_type>").append("<![CDATA[" + orderType + "]]>").append("</amd_type>");
						valueXmlString.append("<sorder_mode>").append("<![CDATA[" + orderMode + "]]>").append("</sorder_mode>");
						valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");
						valueXmlString.append("<site_code__ship>").append("<![CDATA[" + siteCodeShip + "]]>").append("</site_code__ship>");
						valueXmlString.append("<curr_code__frt_o>").append("<![CDATA[" + currCodeFrt + "]]>").append("</curr_code__frt_o>");
						valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + currCodeFrt + "]]>").append("</curr_code__frt>");
						valueXmlString.append("<curr_code__ins_o>").append("<![CDATA[" + currCodeIns + "]]>").append("</curr_code__ins_o>");
						valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + currCodeIns + "]]>").append("</curr_code__ins>");
						valueXmlString.append("<trans_mode__o>").append("<![CDATA[" + transMode + "]]>").append("</trans_mode__o>");
						valueXmlString.append("<trans_mode>").append("<![CDATA[" + transMode + "]]>").append("</trans_mode>");
						valueXmlString.append("<frt_term__o>").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term__o>");
						valueXmlString.append("<frt_term>").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");
						valueXmlString.append("<exch_rate__frt_o>").append("<![CDATA[" + exchRateFrt + "]]>").append("</exch_rate__frt_o>");
						valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + exchRateFrt + "]]>").append("</exch_rate__frt>");
						valueXmlString.append("<exch_rate__ins_o>").append("<![CDATA[" + exchRateIns + "]]>").append("</exch_rate__ins_o>");
						valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + exchRateIns + "]]>").append("</exch_rate__ins>");
						valueXmlString.append("<frt_amt_o>").append("<![CDATA[" + frtAmt + "]]>").append("</frt_amt_o>");
						valueXmlString.append("<frt_amt>").append("<![CDATA[" + frtAmt + "]]>").append("</frt_amt>");
						valueXmlString.append("<ins_amt_o>").append("<![CDATA[" + insAmt + "]]>").append("</ins_amt_o>");
						valueXmlString.append("<ins_amt>").append("<![CDATA[" + insAmt + "]]>").append("</ins_amt>");
						valueXmlString.append("<dlv_term_o>").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term_o>");
						valueXmlString.append("<dlv_term>").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term>");
						valueXmlString.append("<delivery_term_descr>").append("<![CDATA[" + dlvTermDescr + "]]>").append("</delivery_term_descr>");
						valueXmlString.append("<price_list>").append("<![CDATA[" + priceList + "]]>").append("</price_list>");
						valueXmlString.append("<price_list__disc>").append("<![CDATA[" + priceListDisc + "]]>").append("</price_list__disc>");
						
						if(plDate != null)
						{
						valueXmlString.append("<pl_date>").append("<![CDATA[" +(sdf.format(plDate)).toString() + "]]>").append("</pl_date>");
						}
						valueXmlString.append("<udf__str1_o>").append("<![CDATA[" + udfStr1 + "]]>").append("</udf__str1_o>");
						valueXmlString.append("<udf__str1>").append("<![CDATA[" + udfStr1 + "]]>").append("</udf__str1>");
						valueXmlString.append("<remarks_o>").append("<![CDATA[" + remarks + "]]>").append("</remarks_o>");
						valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");
						valueXmlString.append("<remarks2_o>").append("<![CDATA[" + remarks2 + "]]>").append("</remarks2_o>");
						valueXmlString.append("<remarks2>").append("<![CDATA[" + remarks2 + "]]>").append("</remarks2>");
						valueXmlString.append("<remarks3_o>").append("<![CDATA[" + remarks3 + "]]>").append("</remarks3_o>");
						valueXmlString.append("<remarks3>").append("<![CDATA[" + remarks3 + "]]>").append("</remarks3>");
						valueXmlString.append("<stan_code__init>").append("<![CDATA[" + stanCodeInit + "]]>").append("</stan_code__init>");
						valueXmlString.append("<stan_code__init_o>").append("<![CDATA[" + stanCodeInit + "]]>").append("</stan_code__init_o>");
						valueXmlString.append("<dlv_add3_o>").append("<![CDATA[" + dlvAdd3 + "]]>").append("</dlv_add3_o>");
						valueXmlString.append("<dlv_add3>").append("<![CDATA[" + dlvAdd3 + "]]>").append("</dlv_add3>");
						valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
						valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
						valueXmlString.append("<emp_code__ord>").append("<![CDATA[" + empCodeOrd + "]]>").append("</emp_code__ord>");
						valueXmlString.append("<sales_pers>").append("<![CDATA[" + salesPers + "]]>").append("</sales_pers>");
						valueXmlString.append("<sp_name>").append("<![CDATA[" + spName + "]]>").append("</sp_name>");
						valueXmlString.append("<curr_code>").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");
						valueXmlString.append("<acct_code__sal__o>").append("<![CDATA[" + acctCodeSal+ "]]>").append("</acct_code__sal__o>");
						valueXmlString.append("<acct_code__sal>").append("<![CDATA[" + acctCodeSal + "]]>").append("</acct_code__sal>");
						valueXmlString.append("<cctr_code__sal__o>").append("<![CDATA[" + cctrCodeSal + "]]>").append("</cctr_code__sal__o>");
						valueXmlString.append("<cctr_code__sal>").append("<![CDATA[" + cctrCodeSal + "]]>").append("</cctr_code__sal>");
						
						System.out.println("valueXmlString in sale_order"+valueXmlString.toString());
					}
					if (currentColumn.trim().equals("cust_code__dlv")) 
					{
						custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
						
						System.out.println("INSIDE cust_code__dlv "+custCodeDlv );
						sql = "select cust_name from customer where cust_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCodeDlv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custCodeDlvDescr =  checkNull(rs.getString("cust_name"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						valueXmlString.append("<customer_a_cust_name>").append("<![CDATA[" + custCodeDlvDescr + "]]>").append("</customer_a_cust_name>");
					}
					if (currentColumn.trim().equals("curr_code__frt")) 
					{
						currCodeFrt = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));
						System.out.println("INSIDE currCodeFrt "+currCodeFrt );
						sql = "select std_exrt from currency where curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, currCodeFrt);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stdExrt =  checkNull(rs.getString("std_exrt"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt curr_code__frt>>"+stdExrt);
						valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + stdExrt + "]]>").append("</exch_rate__frt>");
					}
					if (currentColumn.trim().equals("curr_code__ins")) 
					{
						currCodeIns = checkNull(genericUtility.getColumnValue("curr_code__ins", dom));
						System.out.println("INSIDE currCodeIns "+currCodeIns );
						sql = "select std_exrt from currency where curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, currCodeIns);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stdExrt =  checkNull(rs.getString("std_exrt"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt curr_code__ins>>"+stdExrt);
						valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + stdExrt + "]]>").append("</exch_rate__ins>");
					}
					if (currentColumn.trim().equals("cust_code__bil")) 
					{
						custCodeBil = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
						System.out.println("INSIDE custCodeBil "+custCodeBil );
						
						sql = "select cust_name from customer where cust_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCodeBil);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custCodeBilDescr =  checkNull(rs.getString("cust_name"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt cust_code__bil>>"+custCodeBilDescr);
						valueXmlString.append("<customer_b_cust_name>").append("<![CDATA[" + custCodeBilDescr + "]]>").append("</customer_b_cust_name>");
						
						
					}
					if (currentColumn.trim().equals("tax_class")) 
					{
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
						
						System.out.println("INSIDE tax_class "+taxClass );
						sql = "select descr from taxclass where tax_class = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, taxClass);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt cust_code__bil>>"+descr);
						valueXmlString.append("<taxclass_descr>").append("<![CDATA[" + descr + "]]>").append("</taxclass_descr>");
						
					}
					if (currentColumn.trim().equals("tax_chap")) 
					{
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
						System.out.println("INSIDE taxChap "+taxChap );
						sql = "select descr from taxchap where tax_chap = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, taxChap);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt tax_chap>>"+descr);
						valueXmlString.append("<taxchap_descr>").append("<![CDATA[" + descr + "]]>").append("</taxchap_descr>");
						
					}
					if (currentColumn.trim().equals("tax_env")) 
					{
						taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						
						sql = "select descr from taxenv where tax_env = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, taxEnv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt tax_env>>"+descr);
						valueXmlString.append("<taxenv_descr>").append("<![CDATA[" + descr + "]]>").append("</taxenv_descr>");
						
					}
					if (currentColumn.trim().equals("cr_term")) 
					{
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						
						sql = "select descr from crterm where cr_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, crTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt cr_term>>"+descr);
						valueXmlString.append("<crterm_descr>").append("<![CDATA[" + descr + "]]>").append("</crterm_descr>");
					}
					if (currentColumn.trim().equals("count_code__dlv")) 
					{
						countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom));
						
						sql = "select descr from country where count_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, countCodeDlv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("stdExrt count_code__dlv>>"+descr);
						valueXmlString.append("<country_descr>").append("<![CDATA[" + descr + "]]>").append("</country_descr>");
					}
					if (currentColumn.trim().equals("stan_code")) 
					{
						stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
						sql = "select descr from station where stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, stanCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("descr stan_code>>"+descr);
						valueXmlString.append("<station_descr>").append("<![CDATA[" + descr + "]]>").append("</station_descr>");
						
					}
					if (currentColumn.trim().equals("tran_code")) 
					{
						
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						sql = "select tran_name from transporter where tran_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("tran_name"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("descr tran_code>>"+descr);
						valueXmlString.append("<transporter_tran_name>").append("<![CDATA[" + descr + "]]>").append("</transporter_tran_name>");
						
					}
					if (currentColumn.trim().equals("dist_route")) 
					{
						distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom));
						sql = "select descr from distroute where dist_route = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, distRoute);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("descr distroute_descr>>"+descr);
						valueXmlString.append("<distroute_descr>").append("<![CDATA[" + descr + "]]>").append("</distroute_descr>");
						
					}
					if (currentColumn.trim().equals("dlv_term")) 
					{
						dlvTerm = checkNull(genericUtility.getColumnValue("dlv_term", dom));
						sql = "select descr from delivery_term where dlv_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, dlvTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr =  checkNull(rs.getString("descr"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("descr dlv_term>>"+descr);
						valueXmlString.append("<delivery_term_descr>").append("<![CDATA[" + descr + "]]>").append("</delivery_term_descr>");
						
					}
					
					valueXmlString.append("</Detail1>");
					valueXmlString.append("</Root>");
				}
				break;

				case 2: 
				{
                    System.out.println("In ItemChaged Case 2::_3038");
                    //reStr = itemChanged(dom1, dom1, dom2, "1","itm_defaultedit", editFlag, xtraParams);
                    reStr = itemChanged(dom, dom1, dom2, "1","itm_defaultedit", editFlag, xtraParams);
					
					reStr=reStr.substring(reStr.indexOf("<Detail1>"), reStr.indexOf("</Detail1>")+10);
					System.out.println("Detail 1String"+reStr);
					
					valueXmlString = new StringBuffer(
							"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
					valueXmlString.append(editFlag).append("</editFlag></header>");
					//Commented by Varsha V on 05-10-18 for removing extra closing tag
					//valueXmlString.append(editFlag).append("</editFlag> </header>");
					valueXmlString.append(reStr);
					valueXmlString.append("<Detail2>");
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					//childNodeList = parentNode.getChildNodes();
					ctr = 0;
					System.out.println("[" + currentColumn + "] ==> '"+ columnValue + "'");
					if (currentColumn.trim().equals("itm_default")) 
					{
                        //Changed by Anagha R on 04/11/2020 for Gimatex:- _Sales order amendment system show wrong item code START
                        //itemCode = checkNull(genericUtility.getColumnValue("item_code", dom1));
						//itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom1));
                        //lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom1));
                        //amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
                        itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						amdNo = checkNull(genericUtility.getColumnValue("amd_no", dom));
						//Changed by Anagha R on 04/11/2020 for Gimatex:- _Sales order amendment system show wrong item code END
                        saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						
						//Changed by Anagha R on 04/11/2020 for Gimatex:- _Sales order amendment system show wrong item code START
						//valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
                        //valueXmlString.append("<item_code__ord protect = \"0\">").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
                        if(lineNoSord!=null && lineNoSord.trim().length() > 0){
                            valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
                            valueXmlString.append("<item_code__ord protect = \"1\">").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
                        }else{
                            valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
                            valueXmlString.append("<item_code__ord protect = \"0\">").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
                        }
                        //Changed by Anagha R on 04/11/2020 for Gimatex:- _Sales order amendment system show wrong item code END
                        valueXmlString.append("<line_no__sord protect = \"0\">").append("<![CDATA[" + lineNoSord + "]]>").append("</line_no__sord>");
                        valueXmlString.append("<amd_no>").append("<![CDATA[" + amdNo + "]]>").append("</amd_no>");
						valueXmlString.append("<sale_order>").append("<![CDATA[" + saleOrder + "]]>").append("</sale_order>");
								
						sql = "select due_date , quot_no from sorder where sale_order = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							dueDate = rs.getTimestamp("due_date");
							quotNo =  checkNull(rs.getString("quot_no"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println(" dueDate>>"+dueDate +"quotNo"+quotNo);
						if(quotNo != null && quotNo.trim().length() > 0)
						{
							valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
						}	
						else
						{
							priceList = checkNull(genericUtility.getColumnValue("price_list", dom));
							plistType = distCommon.getPriceListType(priceList, conn);
							
							if(plistType == null || plistType.trim().length() == 0)
							{
								plistType = "L";
							}
							if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType))
							{
								valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
							}
							else
							{
								valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
							}
						}
						if(dueDate!= null)
						{
						valueXmlString.append("<dsp_date>").append("<![CDATA[" +sdf.format(dueDate).toString() + "]]>").append("</dsp_date>");
						valueXmlString.append("<dsp_date_o>").append("<![CDATA[" + sdf.format(dueDate).toString() + "]]>").append("</dsp_date_o>");
						}
						valueXmlString.append("<nature>").append("<![CDATA[" + "C" + "]]>").append("</nature>");
					}
					if (currentColumn.trim().equals("itm_defaultedit")) 
					{
                        System.out.println("In itm_defaultedit");
						//itemCode = checkNull(genericUtility.getColumnValue("item_code", dom1));
						//itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom1));
                        saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
                        itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						
						valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						valueXmlString.append("<item_code__ord protect = \"1\">").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
						valueXmlString.append("<sale_order>").append("<![CDATA[" + saleOrder + "]]>").append("</sale_order>");
						
						sql = "select quot_no ,price_list__clg from sorder where sale_order = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							quotNo =checkNull(rs.getString("quot_no"));
							priceListClg =  checkNull(rs.getString("price_list__clg"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if(quotNo != null && quotNo.trim().length() > 0)
						{
							valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
						}
						else
						{
							priceList = checkNull(genericUtility.getColumnValue("price_list", dom));
							plistType = distCommon.getPriceListType(priceList, conn);
							
							if(plistType == null || plistType.trim().length() == 0)
							{
								plistType = "L";
							}
							if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType))
							{
								valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
							}
							else
							{
								valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
							}
						}
						plistType = "";
						plistType = distCommon.getPriceListType(priceListClg, conn);
						
						if(plistType == null || plistType.trim().length() == 0)
						{
							plistType = "L";
						}
						if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType))
						{
							valueXmlString.append("<rate__clg protect = \"1\">").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
						}
						else
						{
							valueXmlString.append("<rate__clg protect = \"0\">").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
						}
						
						// added by mahesh saggam on 05/aug/19 [Start]
						
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						convQtyStduom = genericUtility.getColumnValue("conv__qty_stduom", dom);
						unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						convRtuomStduom = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
						if (unit.trim().equalsIgnoreCase(unitStd.trim())) {
							valueXmlString.append("<conv__qty_stduom protect = \"1\">").append("<![CDATA[" + convQtyStduom + "]]>").append("</conv__qty_stduom>");
						} 
						else
						{
							valueXmlString.append("<conv__qty_stduom protect = \"0\">").append("<![CDATA[" + convQtyStduom + "]]>").append("</conv__qty_stduom>");
						}
						if(unitStd.trim().equalsIgnoreCase(unitRate.trim()))
						{
							valueXmlString.append("<conv__rtuom_stduom protect = \"1\">").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
						}
						else
						{
							valueXmlString.append("<conv__rtuom_stduom protect = \"0\">").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
						}
						
						// added by mahesh saggam on 05/aug/19 [End]
                    }
                         

					if (currentColumn.trim().equalsIgnoreCase("line_no__sord"))
					{
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						
						System.out.println("line before"+ lineNoSord);
						if(lineNoSord != null && lineNoSord.trim().length() > 0)
						{
							lineNoSord = getLineNewNo(lineNoSord);
						}
						System.out.println("line after"+ lineNoSord);
						// 09-dec-2020 manoharan line_no__ord should have leading space
						valueXmlString.append("<line_no__sord>").append("<![CDATA[" + lineNoSord + "]]>").append("</line_no__sord>");
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						/*itemCode = checkNull(genericUtility.getColumnValue("item_code", dom1));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom1));*/
						
						sql = " select site_code,quantity,rate,discount,tax_amt,tax_class,tax_chap,tax_env,net_amt,quantity__stduom,rate__stduom,pack_instr,no_art," +
							  " pack_code,dsp_date, item_code, item_descr, unit__std,conv__qty_stduom, rate__clg, unit, conv__qty_stduom, unit__rate, " +
							  " conv__rtuom_stduom,item_flg,item_code__ord,min_shelf_life,cust_spec__no,nature, max_shelf_life  from sorddet where sale_order = ? and line_no = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						pstmt.setString(2, lineNoSord);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteCode =  checkNull(rs.getString("site_code"));
							quantity =  rs.getDouble("quantity");
							rateD =  rs.getDouble("rate");
							discount =  rs.getDouble("discount");
							taxAmt =  rs.getDouble("tax_amt");
							taxClass =  checkNull(rs.getString("tax_class"));
							taxChap =  checkNull(rs.getString("tax_chap"));
							taxEnv =  checkNull(rs.getString("tax_env"));
							netAmt =  rs.getDouble("net_amt");
							qStduom =  rs.getDouble("quantity__stduom");
							rStduom =  rs.getDouble("rate__stduom");
							packInstr =  checkNull(rs.getString("pack_instr"));
							noArt =  checkNull(rs.getString("no_art"));
							packCode =  checkNull(rs.getString("pack_code"));
							dspDate =  rs.getTimestamp("dsp_date");
							itemCode =  checkNull(rs.getString("item_code"));
							itemDescr =  checkNull(rs.getString("item_descr"));
							unitStd =  checkNull(rs.getString("unit__std"));
							convQtystduom =  checkNull(rs.getString("conv__qty_stduom"));
							rateClg =  checkNull(rs.getString("rate__clg"));
							unit =  checkNull(rs.getString("unit"));
							unitRate =  checkNull(rs.getString("unit__rate"));
							convRtuomStduom =  checkNull(rs.getString("conv__rtuom_stduom"));
							itemFlg =  checkNull(rs.getString("item_flg"));
							itemCodeOrd =  checkNull(rs.getString("item_code__ord"));
							minShelfLife =  rs.getDouble("min_shelf_life");
							custSpecNo =  checkNull(rs.getString("cust_spec__no"));
							nature =  checkNull(rs.getString("nature"));
							maxShelfLife =  rs.getDouble("max_shelf_life");
							
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						System.out.println("rateD>>>"+rateD);
						valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");
						valueXmlString.append("<site_code_o>").append("<![CDATA[" + siteCode + "]]>").append("</site_code_o>");
						valueXmlString.append("<quantity_o>").append("<![CDATA[" + quantity + "]]>").append("</quantity_o>");
						valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
						valueXmlString.append("<rate_o>").append("<![CDATA[" + rateD + "]]>").append("</rate_o>");
						valueXmlString.append("<rate>").append("<![CDATA[" + rateD + "]]>").append("</rate>");
						//valueXmlString.append("<rate>").append("<![CDATA[" + rateD + "]]>").append("</rate>");
						valueXmlString.append("<discount_o>").append("<![CDATA[" + discount + "]]>").append("</discount_o>");
						valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
						valueXmlString.append("<tax_amt_o>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt_o>");
						valueXmlString.append("<tax_amt>").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt>");
						valueXmlString.append("<tax_class_o>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class_o>");
						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
						valueXmlString.append("<tax_chap_o>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap_o>");
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_env_o>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env_o>");
						valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						valueXmlString.append("<net_amt_o>").append("<![CDATA[" + netAmt + "]]>").append("</net_amt_o>");
						valueXmlString.append("<net_amt>").append("<![CDATA[" + netAmt + "]]>").append("</net_amt>");
						valueXmlString.append("<quantity__stduom_o>").append("<![CDATA[" + qStduom + "]]>").append("</quantity__stduom_o>");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" +qStduom + "]]>").append("</quantity__stduom>");
						valueXmlString.append("<rate__stduom_o>").append("<![CDATA[" + rStduom + "]]>").append("</rate__stduom_o>");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" +rStduom  + "]]>").append("</rate__stduom>");
						valueXmlString.append("<no_art_o>").append("<![CDATA[" + noArt + "]]>").append("</no_art_o>");
						valueXmlString.append("<no_art>").append("<![CDATA[" + noArt + "]]>").append("</no_art>");
						valueXmlString.append("<pack_code_o>").append("<![CDATA[" + packCode + "]]>").append("</pack_code_o>");
						valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
						valueXmlString.append("<pack_instr_o>").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr_o>");
						valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr>");
						
						if(dspDate != null)
						{
						valueXmlString.append("<dsp_date_o>").append("<![CDATA[" + sdf.format(dspDate).toString() + "]]>").append("</dsp_date_o>");
						valueXmlString.append("<dsp_date>").append("<![CDATA[" + sdf.format(dspDate).toString() + "]]>").append("</dsp_date>");
						}
						
						valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						valueXmlString.append("<item_code_o>").append("<![CDATA[" + itemCode + "]]>").append("</item_code_o>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr + "]]>").append("</item_descr>");
						valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
						valueXmlString.append("<unit_o>").append("<![CDATA[" + unit + "]]>").append("</unit_o>");
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + convQtystduom + "]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<rate__clg_o>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg_o>");
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
						valueXmlString.append("<item_flg>").append("<![CDATA[" + itemFlg + "]]>").append("</item_flg>");
						valueXmlString.append("<conv__qty_stduom_o>").append("<![CDATA[" + convQtystduom + "]]>").append("</conv__qty_stduom_o>");
						valueXmlString.append("<unit__rate_o>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate_o>");
						valueXmlString.append("<conv__rtuom_stduom_o>").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom_o>");
						valueXmlString.append("<item_flg_o>").append("<![CDATA[" + itemFlg + "]]>").append("</item_flg_o>");
						valueXmlString.append("<item_code__ord>").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
						valueXmlString.append("<item_code__ord_o>").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord_o>");
						valueXmlString.append("<min_shelf_life_o>").append("<![CDATA[" + minShelfLife + "]]>").append("</min_shelf_life_o>");
						valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minShelfLife + "]]>").append("</min_shelf_life>");
						valueXmlString.append("<cust_spec__no_o>").append("<![CDATA[" + custSpecNo + "]]>").append("</cust_spec__no_o>");
						valueXmlString.append("<cust_spec__no>").append("<![CDATA[" + custSpecNo + "]]>").append("</cust_spec__no>");
						valueXmlString.append("<nature>").append("<![CDATA[" + nature + "]]>").append("</nature>");
						valueXmlString.append("<max_shelf_life>").append("<![CDATA[" + minShelfLife + "]]>").append("</max_shelf_life>");
						valueXmlString.append("<max_shelf_life_o>").append("<![CDATA[" + minShelfLife + "]]>").append("</max_shelf_life_o>");
					
						
						sql = "select count(1) from sorddet where sale_order =  ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							llCnt = rs.getInt(1);
						}
						rs.close();rs = null;
                        pstmt.close();pstmt = null;		
                        
                        System.out.println("llCnt_3351:: "+llCnt);
						if(lineNoSord!= null && lineNoSord.trim().length() > 0)
						{
							lineNoSordInt = Integer.parseInt(lineNoSord.trim());
						}
						System.out.println("lineNoSordInt>>"+lineNoSordInt);
						if( lineNoSordInt <= llCnt)
						{
							valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
							valueXmlString.append("<item_code__ord protect = \"1\">").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
							
						}
						else
						{
							valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
							valueXmlString.append("<item_code__ord protect = \"0\">").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code__ord>");
							
						}
						
						
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
						plistType = distCommon.getPriceListType(priceList, conn);
						
						if(plistType == null || plistType.trim().length() == 0)
						{
							plistType = "L";
						}
						
						System.out.println("rate >>>++["+rate+"]");
						
						rate =  checkNull(genericUtility.getColumnValue("rate", dom1));
						System.out.println("rate >>>++afe["+rate+"]");
						if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType))
						{
							valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + rateD + "]]>").append("</rate>");
						}
						else
						{
							valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + rateD + "]]>").append("</rate>");
						}
						
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
						
						sql = "select  price_list__clg from sorder where  sale_order = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceListClg = rs.getString(1);
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;	
						
						plistType = "";
						
						plistType = distCommon.getPriceListType(priceListClg, conn);
						
						if(plistType == null || plistType.trim().length() == 0)
						{
							plistType = "L";
						}
						if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType))
						{
							valueXmlString.append("<rate__clg protect = \"1\">").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
						}
						else
						{
							valueXmlString.append("<rate__clg protect = \"0\">").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
						}
					}
					if (currentColumn.trim().equals("quantity"))
					{
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						
						
						if(qty != null && qty.trim().length() > 0)
						{
							qtyDb = Double.parseDouble(qty) ;
						}
						else
						{
							qtyDb = 0.0 ;
						}
						System.out.println("qtyDb in quantity ic[ "+qtyDb);
						if(unit != null && unit.trim().length() == 0)
						{
							sql = "Select unit from item where item_code = ? " ;
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								unit = rs.getString(1);
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;	
							
							if(convQtyStduom != null && convQtyStduom.trim().length() > 0)
							{
								convQtyStduomDb = Double.parseDouble(convQtyStduom) ;
							}
							else
							{
								convQtyStduomDb = 0.0 ;
							}
							
							mNum = distCommon.getConvQuantityFact(unit, unitStd,itemCode, qtyDb, convQtyStduomDb, conn);
							valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
							setNodeValue(dom, "unit", getAbsString(unit));
						}
						else
						{
							mNum = distCommon.getConvQuantityFact(unit, unitStd,itemCode, qtyDb, convQtyStduomDb, conn);
						}
						
						if(convQtyStduomDb == 0)
						{
							valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + convQtyStduom + "]]>").append("</conv__qty_stduom>");
							setNodeValue(dom, "conv__qty_stduom", getAbsString(convQtyStduom));
						}
						if(mNum.size() > 1)
						{
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + mNum.get(1) + "]]>").append("</quantity__stduom>");
						setNodeValue(dom, "quantity__stduom", getAbsString((String) mNum.get(1)));
						}
						//gbf_itemchanged_logic(as_form_no,"item_code__ord",as_editflag)
						//setNodeValue(dom, "item_code__ord", getAbsString(itemCodeOrd));
						reStr = itemChanged(dom, dom1, dom2, objContext,"item_code__ord", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						
					}
					if (currentColumn.trim().equals("conv__qty_stduom"))
					{
						convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						//gbf_itemchanged_logic(as_form_no,"quantity",as_editflag)
						
						setNodeValue(dom, "conv__qty_stduom", getAbsString(convQtyStduom));
						reStr = itemChanged(dom, dom1, dom2, objContext,"quantity", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						
						System.out.println("convQtyStduom??"+convQtyStduom);
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + convQtyStduom + "]]>").append("</conv__qty_stduom>");
						
					}
					if (currentColumn.trim().equals("rate"))
					{
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						convRtuomStduom = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						
						System.out.println("unitRate IN RATE [" +unitRate +"]rate["+rate +"itemCode"+itemCode +"convRtuomStduom"+convRtuomStduom);
						
						if(convRtuomStduom != null && convRtuomStduom.trim().length() > 0)
						{
							convRtuomStduomDb = Double.parseDouble(convRtuomStduom) ;
						}
						else
						{
							convQtyStduomDb = 0.0 ;
						}
						if(rate != null && rate.trim().length() > 0)
						{
							rateDb = Double.parseDouble(rate) ;
						}
						else
						{
							rateDb = 0.0 ;
						}
						if(unitRate != null && unitRate.trim().length() > 0)
						{
							sql = "Select unit from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								unit = rs.getString(1);
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;	
							
							mNum = distCommon.getConvQuantityFact(unitStd,unitRate,itemCode, rateDb,convRtuomStduomDb, conn);
							valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
							
						}
						else
						{
							mNum = distCommon.getConvQuantityFact(unitStd,unitRate,itemCode, rateDb, convRtuomStduomDb, conn);
						}
						
						
						if(convRtuomStduomDb == 0)
						{
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + convRtuomStduom + "]]>").append("</conv__rtuom_stduom>");
						}
						if (mNum.size() > 1)
						{
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + mNum.get(1) + "]]>").append("</rate__stduom>");
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + mNum.get(1) + "]]>").append("</rate__clg>");
						}
						
						// added by mahesh saggam on 01/Aug/19 [Start]
						
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						

						if(unit != null && unit.trim().length() > 0)
						{
							ArrayList ratestduomArr = null;
							ratestduomArr = distCommon.getConvQuantityFact(unit, unitStd, itemCodeOrd, rateDb, convRtuomStduomDb, conn);
							convRtuomStduomDb = Double.parseDouble(ratestduomArr.get(0).toString());
							
							if(unit.trim().equalsIgnoreCase(unitStd.trim()))
							{
								valueXmlString.append("<conv__rtuom_stduom protect = \"1\">").append("1").append("</conv__rtuom_stduom>");
							}
							else
							{
								valueXmlString.append("<conv__rtuom_stduom protect = \"0\">").append("<![CDATA[" + convRtuomStduomDb + "]]>").append("</conv__rtuom_stduom>");
							}
						}
						
						// added by mahesh saggam on 02/Aug/2019 [End]
					}
					if (currentColumn.trim().equals("conv__rtuom_stduom"))
					{
						System.out.println("iNSIDE conv__rtuom_stduom ");
						///gbf_itemchanged_logic(as_form_no,"rate",as_editflag)
						
						convRtuomStduom = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						setNodeValue(dom, "conv__rtuom_stduom", getAbsString(convRtuomStduom));
						reStr = itemChanged(dom, dom1, dom2, objContext,"rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						//valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
						
					}
					if (currentColumn.trim().equals("unit"))
					{
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						
						

						if(qty != null && qty.trim().length() > 0)
						{
							qtyDb = Double.parseDouble(qty) ;
						}
						else
						{
							qtyDb = 0.0 ;
						}
						
						mNum = distCommon.getConvQuantityFact(unit, unitStd,itemCode, qtyDb, 0.0, conn);
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + 0 + "]]>").append("</conv__qty_stduom>");
						
						if (mNum.size() > 1)
						{
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + mNum.get(1) + "]]>").append("</quantity__stduom>");
						}
						
						// added by mahesh saggam on 02/Aug/2019 [Start]
						
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						convQtyStduom = genericUtility.getColumnValue("conv__qty_stduom", dom);
						convQtyStduomDb = convQtyStduom == null || convQtyStduom.trim().length() == 0 ? 0.0 : Double.parseDouble(convQtyStduom);
						System.out.println("conversion rate is = "+convQtyStduomDb);
						
						mNum = distCommon.getConvQuantityFact(unit, unitStd, itemCodeOrd, qtyDb, convQtyStduomDb, conn);

						if (mNum.size() > 0) 
						{
							qty = (String) mNum.get(1);
							qtyDb = Double.parseDouble(qty);

							convQtyStduomDb = Double.parseDouble(mNum.get(0).toString());
							System.out.println("mNum2 @@@@ get 0" + convQtyStduomDb);
						}
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyDb + "]]>").append("</quantity__stduom>");

						if (unit.trim().equalsIgnoreCase(unitStd.trim())) 
						{
							valueXmlString.append("<conv__qty_stduom protect = \"1\">").append("<![CDATA[" + convQtyStduomDb + "]]>").append("</conv__qty_stduom>");
						} 
						else 
						{
							valueXmlString.append("<conv__qty_stduom protect = \"0\">").append("<![CDATA[" + convQtyStduomDb + "]]>").append("</conv__qty_stduom>");
						}
						
						// added by mahesh saggam on 02/Aug/2019 [End]
					}
					if (currentColumn.trim().equals("unit__rate"))
					{
						unitRate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						
						
						if(rate != null && rate.trim().length() > 0)
						{
							rateDb = Double.parseDouble(rate) ;
						}
						else
						{
							rateDb = 0.0 ;
						}
						mNum = distCommon.getConvQuantityFact(unitStd,unitRate,itemCode,rateDb, 0.0, conn);
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + 0 + "]]>").append("</conv__rtuom_stduom>");
						
						if (mNum.size() > 1)
						{
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + mNum.get(1) + "]]>").append("</rate__stduom>");
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + mNum.get(1) + "]]>").append("</rate__clg>");
						}
					}
					if (currentColumn.trim().equals("item_code__ord"))
					{
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						priceListDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
						nature = checkNull(genericUtility.getColumnValue("nature", dom));
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						
						valueXmlString.append("<line_no__sord protect = \"1\">").append("<![CDATA[" + lineNoSord + "]]>").append("</line_no__sord>");
						setNodeValue(dom, "line_no__sord", getAbsString(lineNoSord));
						
						sql = "select order_date, cust_code, stan_code, cust_pord, tax_class , pl_date	, order_type from sorder where  sale_order = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							//orderDate = rs.getTimestamp("order_date");
							custCode = rs.getString("cust_code");
							stanCodeTo = rs.getString("stan_code");
							custPord = rs.getString("cust_pord");
							taxClassHdr = rs.getString("tax_class");
							plDate1 = rs.getTimestamp("pl_date");
							orderType = rs.getString("order_type");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;	
						
						System.out.println("custCode"+custCode+"stanCodeTo"+stanCodeTo+"taxClassHdr"+taxClassHdr+"custPord"+custPord+"orderType"+orderType);
						
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						amdDate = checkNull(genericUtility.getColumnValue("amd_date", dom1));
						
						if(amdDate != null)
						{
							 Date parsedDate = sdf.parse(amdDate);
							 amdDate1 = new java.sql.Timestamp(parsedDate.getTime());
						}
						
						itemSer = distCommon.getItemSer(itemCodeOrd, siteCode, amdDate1 , custCode, "C", conn);
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							
						System.out.println("itemCodeOrd["+itemCodeOrd  +"]itemCode["+itemCode);
						if(!itemCodeOrd.equalsIgnoreCase(itemCode))
						{
							sql = "Select descr, unit, unit__rate, item_stru, pack_code,pack_instr from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mdescr = checkNull(rs.getString("descr"));
								uom = checkNull(rs.getString("unit"));
								uomr = checkNull(rs.getString("unit__rate"));
								//mtype = checkNull(rs.getString("item_stru"));
								packCode = checkNull(rs.getString("pack_code"));
								mpackinstr = checkNull(rs.getString("pack_instr"));
								
								System.out.println("uom["+uom  +"]uomr["+uomr +"]packCode["+packCode + "]mpackinstr["+mpackinstr);
								valueXmlString.append("<item_descr>").append("<![CDATA[" + mdescr + "]]>").append("</item_descr>");
								valueXmlString.append("<unit__std>").append("<![CDATA[" + uom + "]]>").append("</unit__std>");
								valueXmlString.append("<unit__std_o>").append("<![CDATA[" + uom + "]]>").append("</unit__std_o>");
								valueXmlString.append("<unit>").append("<![CDATA[" + uom + "]]>").append("</unit>");
								valueXmlString.append("<unit_o>").append("<![CDATA[" + uom + "]]>").append("</unit_o>");
								valueXmlString.append("<unit__rate>").append("<![CDATA[" + uomr + "]]>").append("</unit__rate>");
								valueXmlString.append("<unit__rate_o>").append("<![CDATA[" + uomr + "]]>").append("</unit__rate_o>");
								valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
								valueXmlString.append("<pack_code_o>").append("<![CDATA[" + packCode + "]]>").append("</pack_code_o>");
								valueXmlString.append("<pack_instr>").append("<![CDATA[" + mpackinstr + "]]>").append("</pack_instr>");
								valueXmlString.append("<pack_instr_o>").append("<![CDATA[" + mpackinstr + "]]>").append("</pack_instr_o>");
								valueXmlString.append("<site_code_o>").append("<![CDATA[" + siteCode + "]]>").append("</site_code_o>");
								valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");
								setNodeValue(dom, "unit__rate", getAbsString(""+uomr));
								setNodeValue(dom, "unit__std", getAbsString(uom));
								setNodeValue(dom, "unit", getAbsString(uom));
								setNodeValue(dom, "pack_code", getAbsString(packCode));
								setNodeValue(dom, "pack_instr", getAbsString(mpackinstr));
								setNodeValue(dom, "site_code", getAbsString(""+siteCode));
								
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
						}
						
					
						
						sql = "select min_shelf_life ,max_shelf_life from sordertype where order_type = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, orderType);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							minShelfLife = rs.getDouble("min_shelf_life");
							maxShelfLife = rs.getDouble("max_shelf_life");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						
						if(minShelfLife == 0)
						{
						
							sql = "select min_shelf_life from customeritem where cust_code = ? and item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCodeOrd);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								minShelfLife = rs.getDouble("min_shelf_life");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							if(minShelfLife == 0)
							{
								sql = "select case when min_shelf_perc is null then 0 else min_shelf_perc end from customer_series where cust_code = ? and item_ser = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemSer);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									minLifePerc = rs.getDouble(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								
								if(minLifePerc == 0)
								{
									minShelfLife = 0;
									maxShelfLife = 0;
								}
								else
								{
									sql = "select (case when shelf_life is null then 0 else shelf_life end ) from item where item_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, itemCodeOrd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										shelfLife = rs.getDouble(1);
									}
									rs.close();rs = null;
									pstmt.close();pstmt = null;
									
									if(shelfLife > 0)
									{
										minShelfLife = round((minLifePerc/100) * shelfLife,0);
										maxShelfLife = shelfLife ;
									}
									else
									{
										minShelfLife = 0;
										maxShelfLife = 0;
									}
								}
							}
							
							if(minShelfLife == 0)
							{
								sql = "select min_shelf_life from customer where cust_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									minShelfLife = rs.getDouble("min_shelf_life");
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
							}
							
							if(minShelfLife == 0)
							{
								sql = "select min_shelf_life from item where item_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									minShelfLife = rs.getDouble("min_shelf_life");
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
							}
							
							if( priceListDisc != null && priceListDisc.trim().length() > 0)
							{
								sql = "select order_type from sorder where sale_order = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									salesOrderType = rs.getString("order_type");
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								
								if("NE".equalsIgnoreCase(salesOrderType.trim()))
								{
									sql = "select (case when no_sales_month is null then 0 else no_sales_month end) from item where item_code = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, itemCodeOrd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										maxShelfLife = rs.getDouble(1);
									}
									rs.close();rs = null;
									pstmt.close();pstmt = null;
									
									if(maxShelfLife == 0)
									{
										varValue = distCommon.getDisparams("999999", "NEAR_EXP_SHELF_LIFE", conn);
										
										if("NULLFOUND".equalsIgnoreCase(varValue) || varValue == null)
										{
											varValue = "0" ;
										}
										maxShelfLife = Double.parseDouble(varValue);
									}
									
									temp = maxShelfLife ;
									maxShelfLife = minShelfLife ;
									minShelfLife = temp ;
								
								}
								else
								{
									maxShelfLife = 0 ;
								}
							}
							
						}
						valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minShelfLife + "]]>").append("</min_shelf_life>");
						valueXmlString.append("<max_shelf_life>").append("<![CDATA[" + maxShelfLife + "]]>").append("</max_shelf_life>");
						
						sql = "select order_date , state_code__dlv , order_type , due_date from sorder where sale_order = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							morderDate = rs.getTimestamp(1);
							mstateCd = checkNull(rs.getString("state_code__dlv"));
							orderType =checkNull(rs.getString("order_type"));
							mtrandate  = rs.getTimestamp(4);
							
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						nature = checkNull(genericUtility.getColumnValue("nature", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						qty = checkNull(genericUtility.getColumnValue("quantity", dom));
						
						if(qty != null && qty.trim().length() > 0)
						{
							qtyDb = Double.parseDouble(qty) ;
						}
							
						sql = "select bom_code , item_stru from item where  item_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							schemeCode = checkNull(rs.getString("bom_code"));
							lsType =checkNull(rs.getString("item_stru"));
							
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						System.out.println("lsType["+lsType +"]schemeCode["+schemeCode+"]");
						if("C".equalsIgnoreCase(lsType))
						{
							sql = " select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code = b.scheme_code " +
								  " and a.item_code = ?  and a.app_from <= ? and a.valid_upto >= ? and (b.site_code = ? or b.state_code = ? )";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeOrd);
							pstmt.setTimestamp(2, mtrandate);
							pstmt.setTimestamp(3, mtrandate);
							pstmt.setString(4, siteCode);
							pstmt.setString(5, mstateCd);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								curScheme = checkNull(rs.getString(1));
								
								sql1 = "select item_code__parent from item where  item_code =  ? ";
								pstmt1 =  conn.prepareStatement(sql1);
								pstmt1.setString(1, itemCodeOrd);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									itemCodeParent = checkNull(rs1.getString("item_code__parent"));
									
								}
								rs1.close();rs1 = null;
								pstmt1.close();pstmt1 = null;
								
								
								if(itemCodeParent == null || itemCodeParent.trim().length() == 0)
								{
									sqlNew = "select count(1) from item where  item_code__parent =  ? ";
									pstmt1 =  conn.prepareStatement(sqlNew);
									pstmt1.setString(1, itemCodeOrd);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										cnt = rs1.getInt(1);
										
									}
									rs1.close();rs1 = null;
									pstmt1.close();pstmt1 = null;
									
									if(cnt > 0)
									{
										break;
									}
								}
								
								sql1= "select count(1) from scheme_applicability A,bom b where A.scheme_code = b.bom_code And	 B.bom_code 	= ? " +
										"And ? between case when b.min_qty is null then 0 else b.min_qty end And case when b.max_qty is null then 0 else b.max_qty end";
								pstmt1 =  conn.prepareStatement(sql1);
								pstmt1.setString(1,curScheme );
								pstmt1.setDouble(2, qtyDb);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									llCnt = rs1.getInt(1);
									
								}
								rs1.close();rs1 = null;
								pstmt1.close();pstmt1 = null;
								
								if(llCnt == 0)
								{
									continue;
								}
								custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
								
								/*sql1= " select (case when apply_cust_list is null then ' ' else apply_cust_list end)," +
									  " (case when noapply_cust_list is null then ' ' else noapply_cust_list end),order_type from 	scheme_applicability " +
									  " where scheme_code = ? ";
								pstmt1 =  conn.prepareStatement(sql1);
								pstmt1.setString(1,schemeCode );
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									applyCustList = checkNull(rs.getString(1));
									noapplyCustList = checkNull(rs.getString(2));
									applicableOrdTypes = checkNull(rs.getString(3));
									
								}
								rs1.close();rs1 = null;
								pstmt1.close();pstmt1 = null;*/
								
								System.out.println("applyCustList["+applyCustList+"]noapplyCustList["+noapplyCustList+"]applicableOrdTypes["+applicableOrdTypes+"]");
								
								schemeCode = getCustSchemeCode(custCode, schemeCode, salesOrderType, conn);
								
								System.out.println("schemeCode["+schemeCode+"]noapplyCustList["+noapplyCustList+"]applicableOrdTypes["+applicableOrdTypes+"]");
								
							/*	if("NE".equalsIgnoreCase(orderType) && (applicableOrdTypes == null || applicableOrdTypes.trim().length() == 0))
								{
									continue;
								}
								else if(applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
								{
									lbProceed = false ;
									String lsApplicableOrdTypesArr[]= applicableOrdTypes.split(",");
									for(int i=0;i<lsApplicableOrdTypesArr.length;i++)
									{
										System.out.println("lsApplicableOrdTypesArr[i]"+lsApplicableOrdTypesArr[i]);
										if(orderType.equalsIgnoreCase(lsApplicableOrdTypesArr[i]))
										{
											lbProceed=true;
											break;
										}
									}
									
								}
								prevScheme	= schemeCode ;	 
								schemeCode = curScheme ; 
								
								if(applyCustList.trim().length() > 0)
								{
									custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
									String applyCustListArr[]=applyCustList.split(",");
									for(int i=0;i<applyCustListArr.length;i++)
									{
										System.out.println("applyCustListArr[i]"+applyCustListArr[i]);
										if(applyCustListArr[i].equalsIgnoreCase(custCode))
										{
											schemeCode = curScheme;
										}
									}
								}
								
								if(noapplyCustList.trim().length() > 0 && schemeCode != null)
								{
									custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
									String noapplyCustListArr[]=noapplyCustList.split(",");
									for(int i=0; i < noapplyCustListArr.length;i++)
									{
										System.out.println("noapplyCustListArr[i]" + noapplyCustListArr[i]);
										if(noapplyCustListArr[i].equalsIgnoreCase(custCode))
										{
											schemeCode = "";
											break;
										}
									}
								}*/
								
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
						}
						else
						{
							valueXmlString.append("<item_code>").append("<![CDATA[" + schemeCode + "]]>").append("</item_code>");
						}
							
							sql1= " select batch_qty from bom where bom_code = ? ";
							pstmt1 =  conn.prepareStatement(sql1);
							pstmt1.setString(1,schemeCode );
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								integralQty = rs1.getDouble("batch_qty");
							}
							rs1.close();rs1 = null;
							pstmt1.close();pstmt1 = null;
							
							System.out.println("integralQty["+integralQty+"]qtyDb["+qtyDb+"]");
							
							if(integralQty > 0 )
							{
								if( qtyDb < integralQty)
								{
									schemeCode = "";
								}
							}
							
							System.out.println("Check for integral qty of scheme from bom"+ schemeCode);
							sql1= " select (case when item_stru is null then 'S' else item_stru end) from item where item_code = ? ";
							pstmt1 =  conn.prepareStatement(sql1);
							pstmt1.setString(1,itemCodeOrd );
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								itemStru  = rs1.getString(1);
							}
							rs1.close();rs1 = null;
							pstmt1.close();pstmt1 = null;
						
							disPobOrdtypeList = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
							
							System.out.println("disPobOrdtypeList.trim().length()IN ITEM CODE ORD"+disPobOrdtypeList.trim().length() +"orderType["+orderType);
							ordFlag = false;
							if(disPobOrdtypeList.trim().length() > 0)
							{
								String disPobOrdtypeListArr[] = disPobOrdtypeList.split(",");
								
								for(int i=0 ; i< disPobOrdtypeListArr.length ; i++)
								{
									System.out.println("disPobOrdtypeListArr[i]"+disPobOrdtypeListArr[i]);
									if(orderType.equalsIgnoreCase(disPobOrdtypeListArr[i]))
									{
										ordFlag = true;
									}
								}
							}
							
							System.out.println("itemStru["+itemStru+"]schemeCode["+schemeCode+"]");
							
							if(ordFlag == true)
							{
								System.out.println("1>>>");
								valueXmlString.append("<item_flg>").append("<![CDATA[" +"I" + "]]>").append("</item_flg>");
								valueXmlString.append("<item_code protect = \"1\">").append("<![CDATA[" +itemCodeOrd + "]]>").append("</item_code>");
							}
							else if("F".equalsIgnoreCase(itemStru) && schemeCode.trim().length() > 0)
							{
								System.out.println("2>>>");
								sqlNew = "select count(*) from scheme_applicability where  item_code =  ? ";
								pstmt1 =  conn.prepareStatement(sqlNew);
								pstmt1.setString(1, itemCodeOrd);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									cnt = rs1.getInt(1);
								}
								rs1.close();rs1 = null;
								pstmt1.close();pstmt1 = null;
								
								if(cnt > 1)
								{
									System.out.println("3>>>");
									valueXmlString.append("<item_flg>").append("<![CDATA[" +"B" + "]]>").append("</item_flg>");
									valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" +"" + "]]>").append("</item_code>");
									
									valueXmlString.append("<item_code >").append("<![CDATA[" +itemCodeOrd + "]]>").append("</item_code>");
								}
								else
								{
									System.out.println("4>>>");
									valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" +"" + "]]>").append("</item_code>");
									valueXmlString.append("<item_flg>").append("<![CDATA[" +"B" + "]]>").append("</item_flg>");
									valueXmlString.append("<item_code>").append("<![CDATA[" + schemeCode + "]]>").append("</item_code>");
									valueXmlString.append("<item_code protect = \"1\" >").append("<![CDATA[" + schemeCode + "]]>").append("</item_code>");
								}
								
							}
							else if(!"F".equalsIgnoreCase(itemStru) && schemeCode.trim().length() > 0)
							{
								System.out.println("5>>>");
								valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" +"" + "]]>").append("</item_code>");
								valueXmlString.append("<item_code>").append("<![CDATA[" + schemeCode + "]]>").append("</item_code>");
								valueXmlString.append("<item_flg>").append("<![CDATA[" +"B" + "]]>").append("</item_flg>");
								valueXmlString.append("<item_code protect = \"1\" >").append("<![CDATA[" + schemeCode + "]]>").append("</item_code>");
							}
							else if(!"F".equalsIgnoreCase(itemStru) && (schemeCode == null || schemeCode.trim().length() == 0))
							{
								System.out.println("6>>>["+llSchcnt+"]");
								valueXmlString.append("<item_code protect = \"0\">").append("<![CDATA[" +"" + "]]>").append("</item_code>");
								
								if(llSchcnt >=1)
								{
									valueXmlString.append("<item_flg>").append("<![CDATA[" +"B" + "]]>").append("</item_flg>");
									valueXmlString.append("<item_code >").append("<![CDATA[" + schemeCode + "]]>").append("</item_code>");
								}
								else
								{
									valueXmlString.append("<item_flg>").append("<![CDATA[" +"I" + "]]>").append("</item_flg>");
									valueXmlString.append("<item_code >").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code>");
									valueXmlString.append("<item_code protect = \"1\" >").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code>");
								}
							}
							
							mpriceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
							qty = checkNull(genericUtility.getColumnValue("quantity", dom));
							
							System.out.println("mpriceList>>>["+mpriceList+"]qty"+qty);
							if(qty != null && qty.trim().length() > 0 )
							{
								qtyDb = Double.parseDouble(qty);
							}
							
							mrate = distCommon.pickRate(mpriceList,sdf.format(plDate1), itemCode,"","L",qtyDb, conn);
							idRatewtDiscount = mrate ;
							System.out.println("MRATE IN ITEM_CIDE ORD B"+mrate);
							plistType = distCommon.getPriceListType(mpriceList, conn);
							
							System.out.println("plistType>>"+plistType);
							if(plistType == null || plistType.trim().length() == 0)
							{
								plistType = "L";
							}
							if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType) && mrate < 0)
							{
								mrate = 0;
							}
							System.out.println("MRATE IN ITEM_CIDE ORD "+mrate);
							valueXmlString.append("<rate>").append("<![CDATA[" + mrate + "]]>").append("</rate>");
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + mrate + "]]>").append("</rate__clg>");
							setNodeValue(dom, "rate__clg", getAbsString(""+mrate));
							System.out.println("mrate >>["+mrate +"]");
							
							setNodeValue(dom, "rate", getAbsString(""+mrate));
							reStr = itemChanged(dom, dom1, dom2, objContext,"rate", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
							
							System.out.println("reStr >>["+reStr +"]");
							System.out.println("valueXmlString ITEMCODE >>["+valueXmlString +"]");
							
							
							if(plistType == null || plistType.trim().length() == 0)
							{
								plistType = "L";
							}
							if("B".equalsIgnoreCase(plistType) || "I".equalsIgnoreCase(plistType) || "F".equalsIgnoreCase(plistType))
							{
								valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + mrate + "]]>").append("</rate>");
							}
							else
							{
								valueXmlString.append("<rate protect = \"0\">").append("<![CDATA[" + mrate + "]]>").append("</rate>");
							}
							valueXmlString.append("<item_code >").append("<![CDATA[" + itemCodeOrd + "]]>").append("</item_code>");
						
							siteCodeDet = checkNull(genericUtility.getColumnValue("site_code", dom));
							taxChap = distCommon.getTaxChap(itemCodeOrd, itemSer, "C", custCode, siteCodeDet, conn);
							
							sqlNew = "select stan_code from site where site_code =  ? ";
							pstmt1 =  conn.prepareStatement(sqlNew);
							pstmt1.setString(1, siteCodeDet);
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								frStanCode = rs1.getString("stan_code");
							}
							rs1.close();rs1 = null;
							pstmt1.close();pstmt1 = null;
							
							custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom1));
							
							taxClass = distCommon.getTaxClass("C", custCodeDlv, itemCodeOrd, siteCodeDet, conn);
							
							if(taxClassHdr != null && taxClassHdr.trim().length() > 0)
							{
								taxClass = taxClassHdr ;
							}
							taxEnv = distCommon.getTaxEnv(frStanCode, stanCodeTo, taxChap, taxClass, siteCodeDet, conn);
							
							System.out.println("taxChap >>["+taxChap +"]taxClass["+taxClass+"]taxEnv["+taxEnv+"]");
							
							valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
							
							pListDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom1));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
							itemCode	 = checkNull(genericUtility.getColumnValue("item_code", dom));
							unit = checkNull(genericUtility.getColumnValue("unit", dom));
							plistDate = checkNull(genericUtility.getColumnValue("pl_date", dom1));
							qty = checkNull(genericUtility.getColumnValue("quantity", dom));
							saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
							
							sqlNew = "select order_date, cust_code from sorder where  sale_order =  ? ";
							pstmt1 =  conn.prepareStatement(sqlNew);
							pstmt1.setString(1, saleOrder);
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								morderDate = rs1.getTimestamp("order_date");
								custCode = rs1.getString("cust_code");
							}
							rs1.close();rs1 = null;
							pstmt1.close();pstmt1 = null;
							
							lcPlistDisc = getDiscount(pListDisc, morderDate, custCode, siteCodeDet, itemCode, unit, 0, plDate1,qtyDb, conn);
									
							discountStr = checkNull(genericUtility.getColumnValue("discount", dom));
							
							if(discountStr != null && discountStr.trim().length() > 0)
							{
								discountStrDb =  Double.parseDouble(discountStr) ;
							}
							
							if(discountStrDb == 0 || discountStr == null)
							{
								valueXmlString.append("<discount>").append("<![CDATA[" + lcPlistDisc + "]]>").append("</discount>");
							}
							
							priceList = pListDisc ; 
							
							listType = distCommon.getPriceListType(priceList, conn);;
							
							if(listType == null )
							{
								listType = "";
							}
							System.out.println("listType>>>.."+listType);
							
							if("M".equalsIgnoreCase(listType))
							{
								ldRate	=	idRatewtDiscount ;
								//discFlag = true ; 
								calRate(lcPlistDisc, ldRate);
								
								if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature))
								{
									ldRate	= 0;
								}
								valueXmlString.append("<rate>").append("<![CDATA[" + ldRate + "]]>").append("</rate>");
								valueXmlString.append("<discount >").append("<![CDATA[" + 0 + "]]>").append("</discount>");
							}
							
							if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature))
							{
								ldRate	= 0;
								valueXmlString.append("<rate>").append("<![CDATA[" + ldRate + "]]>").append("</rate>");
							}
							//qtyStd = checkNull(genericUtility.getColumnValue("quantity__stduom", dom1));;
							packCode =checkNull(genericUtility.getColumnValue("pack_code", dom1));;
						
							// added by mahesh saggam on 01/Aug/19 [Start]
							
							unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
							convQtyStduom = genericUtility.getColumnValue("conv__qty_stduom", dom);
							convQtyStduomDb = convQtyStduom == null || convQtyStduom.trim().length() == 0 ? 0.0 : Double.parseDouble(convQtyStduom);
							System.out.println("conversion rate is = "+convQtyStduomDb);
							
							mNum = distCommon.getConvQuantityFact(unit, unitStd, itemCodeOrd, qtyDb, convQtyStduomDb, conn);

							if (mNum.size() > 0) 
							{
								qty = (String) mNum.get(1);
								qtyDb = Double.parseDouble(qty);
	
								convQtyStduomDb = Double.parseDouble(mNum.get(0).toString());
								System.out.println("mNum2 @@@@ get 0" + convQtyStduomDb);
							}
							valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyDb + "]]>").append("</quantity__stduom>");

							if (unit.trim().equalsIgnoreCase(unitStd.trim())) 
							{
								valueXmlString.append("<conv__qty_stduom protect = \"1\">").append("<![CDATA[" + convQtyStduomDb + "]]>").append("</conv__qty_stduom>");
							} 
							else 
							{
								valueXmlString.append("<conv__qty_stduom protect = \"0\">").append("<![CDATA[" + convQtyStduomDb + "]]>").append("</conv__qty_stduom>");
							}
						// added by mahesh saggam on 01/Aug/19 [End]
					}
					if (currentColumn.trim().equals("nature"))
					{
						nature = checkNull(genericUtility.getColumnValue("nature", dom));
						
						if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) | "S".equalsIgnoreCase(nature))
						{
							valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
							
							//gbf_itemchanged_logic(as_form_no,"item_code__ord",as_editflag)
							setNodeValue(dom, "nature", getAbsString(nature));
							reStr = itemChanged(dom, dom1, dom2, objContext,"item_code__ord", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
							
						}
					}
	
					valueXmlString.append("</Detail2>");
					valueXmlString.append("</Root>");
				}
				break;

			}
					
		} 
		catch (Exception e)
		{
			System.out.println("SOrderAmdIC Exception ::" + e.getMessage());
			e.printStackTrace();
			
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
				if (conn != null) 
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e) 
			{
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}
		

	private String errorType(Connection conn, String errorCode) throws ITMException 
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			try {
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
			}
			
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
			}
		}
		return msgType;
	}
	
	private String getLineNewNo(String lineNo)	
	{
		lineNo = lineNo.trim();
        System.out.println("lineNo"+lineNo);
		String lenStr = "   " + lineNo ;
		System.out.println("lenStr"+lenStr);
		String lineNoNew = lenStr.substring(lenStr.length() - 3, lenStr.length());
        	
    	System.out.println("lineNonew["+lineNoNew+"]");
    	
		return lineNoNew;
	}

	public String checkNull(String inputVal)
	{
		if (inputVal == null)
		{
			inputVal = "";
		}
		return inputVal;
	}

	private static String getAbsString(String str) 
	{
		return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}

	

	private static void setNodeValue(Document dom, String nodeName,String nodeVal) throws Exception 
	{
		Node tempNode = dom.getElementsByTagName(nodeName).item(0);

		if (tempNode != null) 
		{
			if (tempNode.getFirstChild() == null)
			{
				CDATASection cDataSection = dom.createCDATASection(nodeVal);
				tempNode.appendChild(cDataSection);
			}
			else 
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
	private double checkIntNull(String str)
	{
		if (str == null || str.trim().length() == 0)
		{
			return 0;
		} else
		{
			return Double.parseDouble(str);
		}

	}
	private double checkDoubleNull(String str)
	{
		if (str == null || str.trim().length() == 0)
		{
			return 0.0;
		} else
		{
			return Double.parseDouble(str);
		}
	}
	private double round(double round, int scale) throws ITMException 
	{
		return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
	}
	private double roundValue(double round, int scale)
	{
		return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
	}
	// gbf_get_scheme_code
	public String getSchemeCode(Document dom, Document dom1, Document dom2, String currentColumn, Connection conn) throws ITMException
	{
			SimpleDateFormat sdf;

			PreparedStatement pstmt = null;
			ResultSet rs = null;

			E12GenericUtility genericUtility = new E12GenericUtility();
			String sql = "";
			Date orderDate = null;
			String itemCodeOrd = "", siteCode = "", custCode = "", ordDate = "", priceList = "", orderType = "", stateCodeDlv = "", itemCodeParent = "", schemeCode = "", countCode = "";
			int cnt = 0;
			try
			{
				System.out.println("Inside getSchemeCode...... ");
				sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
				siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
				custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
				ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
				System.out.println("ordDate>>["+ordDate+"]");
				if(ordDate != null && ordDate.trim().length() > 0 )
				{
					orderDate = sdf.parse(ordDate);
				}
				System.out.println("orderDate>>"+orderDate);
				priceList = checkNull(genericUtility.getColumnValue("price_list", dom1));
				orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
				stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
				System.out.println("itemCodeOrd==" + itemCodeOrd);
				System.out.println("siteCode==" + siteCode);
				System.out.println("custCode==" + custCode);
				System.out.println("ordDate==" + ordDate);
				System.out.println("priceList==" + priceList);
				System.out.println("orderType==" + orderType);
				System.out.println("stateCodeDlv==" + stateCodeDlv);
				sql = "select item_code__parent  from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeOrd);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					itemCodeParent = rs.getString("item_code__parent");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (itemCodeParent == null || itemCodeParent.trim().length() == 0)
				{
					sql = "select count(1) from item where item_code__parent =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
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
						itemCodeParent = itemCodeOrd;
					}
				}
				schemeCode = getCheckScheme(itemCodeParent, orderType, custCode, siteCode, stateCodeDlv, countCode, orderDate , conn);
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
			return schemeCode;

		}
		
		// gbf_valdata_group_scheme

		public String valDataGroupScheme(Document dom, Document dom1, Document dom2, String currentColumn, String objContext, String editFlag, String nature, Connection conn) throws ITMException
		{
			SimpleDateFormat sdf;
			PreparedStatement pstmt = null, pstmt1 = null;
			ResultSet rs = null, rs1 = null;
			E12GenericUtility genericUtility = new E12GenericUtility();
			String sql = "";
			double chargeQty = 0, batQty = 0, roundTo = 0, appMinQty = 0, appMaxQty = 0, qtyPer = 0, freeQty = 0;
			double totChargeQty = 0, totFreeQty = 0, totSampleQty = 0, totBonusQty = 0, minQty = 0, unConfTotChargeQty = 0, unConfTotFreeQty = 0, unConfTotSampleQty = 0, unConfTotBonusQty = 0;
			double ConfTotChargeQty = 0, ConfTotFreeQty = 0, ConfTotSampleQty = 0, ConfTotBonusQty = 0, prvChargeQty = 0, prvFreeQty = 0, prvBonusQty = 0, prvSampleQty = 0, quantity = 0;
			String itemCodeOrd = "", siteCode = "", custCode = "", ordDate = "",orderType = "", stateCodeDlv = "", itemCodeParent = "";
			String countCodeDlv = "", saleOrder = "", schemeCode = "", applyCustList = "", noApplyCustList = "", appOrderType = "", lsToken = "", round = "";
			String prevScheme = "", schemeCode1 = "", applyCust = "", custSchemeCode = "", lineNo = "", browItemCode = "", currLineNo = "", itemCodeParentCurr = "";
			String errCode = "",lineNoSord = "",mQty = "",mstateCd ="";
			String childNodeName = null;
			Node parentNode = null;
			Node childNode = null;
			int childNodeListLength;
			int ctr = 0;
			int currentFormNo = 0;
			NodeList parentNodeList = null;
			NodeList childNodeList = null;

			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();
			int cnt = 0;
			double qty = 0;
			long schCnt = 0;
			Date orderDate = null;//appFrom = null, validUpto = null;
			boolean lbParent = false ,lbProceed = false;
			Timestamp tranDate = null ;
			java.sql.Date appFrom = null,validUpto = null ;
			try
			{
				sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				if (objContext != null && objContext.trim().length() > 0)
				{
					currentFormNo = Integer.parseInt(objContext);
					System.out.println("Priyanka testing : currentFormNo :" + currentFormNo);
				}

				
				itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
				lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
				siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
				//lineNoSord = checkNull(genericUtility.getColumnValue("quantity", dom));
				//mQty = checkNull(genericUtility.getColumnValue("cust_code", dom));
				//countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));
				/*ordDate = checkNull(genericUtility.getColumnValue("order_date", dom1));
				if(ordDate != null && ordDate.trim().length() > 0 )
				{
				orderDate = sdf.parse(ordDate);
				}*/
				qty = checkIntNull(genericUtility.getColumnValue("quantity", dom2));
				orderType = checkNull(genericUtility.getColumnValue("order_type", dom1));
				stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom1));
				countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));
				saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom1));
				
				sql = "select order_type,order_date,cust_code,state_code__dlv  from sorder where sale_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					orderType = checkNull(rs.getString("order_type"));
					tranDate = rs.getTimestamp("order_date");
					custCode = checkNull(rs.getString("cust_code"));
					mstateCd = checkNull(rs.getString("state_code__dlv"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("itemCodeOrd==" + itemCodeOrd);
				System.out.println("siteCode==" + siteCode);
				System.out.println("custCode==" + custCode);
				System.out.println("ordDate==" + ordDate);
				System.out.println("mstateCd==" + mstateCd);
				System.out.println("orderDate==" + orderDate);
				System.out.println("qty==" + qty);
				System.out.println("tranDate==" + tranDate);
				System.out.println("stateCodeDlv==" + stateCodeDlv);
				System.out.println("countCodeDlv==" + countCodeDlv);
				System.out.println("saleOrder==" + saleOrder);
				sql = "select item_code__parent from item where item_code = ?  and item_code__parent is not null";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeOrd);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					itemCodeParent = rs.getString("item_code__parent");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (itemCodeParent == null || itemCodeParent.trim().length() == 0)
				{
					sql = "select item_code__parent  from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemCodeParent = checkNull(rs.getString("item_code__parent"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (itemCodeParent == null || itemCodeParent.trim().length() > 0)
					{
						sql = "select count(1) from item where item_code__parent =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
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
							itemCodeParent = itemCodeOrd;
							lbParent = true;
						}
					}
				}
				if (itemCodeOrd.trim().length() > 0)
				{
					sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code= b.scheme_code " +
							"and a.item_code= ? and a.app_from <= ? and a.valid_upto>= ? and (b.site_code= ? or b.state_code = ?  or b.count_code= ?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					pstmt.setTimestamp(2, tranDate);
					pstmt.setTimestamp(3, tranDate);
					pstmt.setString(4, siteCode);
					pstmt.setString(5, mstateCd);
					pstmt.setString(6, countCodeDlv);
					rs = pstmt.executeQuery();
					while (rs.next())
					{
						schemeCode = checkNull(rs.getString("scheme_code"));
						/*sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, schemeCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							applyCustList = rs1.getString("apply_cust_list");
							noApplyCustList = rs1.getString("noapply_cust_list");
							appOrderType = rs1.getString("order_type");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if (orderType.trim() == "NE" && (appOrderType == null || appOrderType.trim().length() == 0))
						{
							break;
						} 
						else if (appOrderType != null && appOrderType.trim().length() > 0)
						{
							lbProceed = false ;
							String lsApplicableOrdTypesArr[]= appOrderType.split(",");
							for(int i=0;i<lsApplicableOrdTypesArr.length;i++)
							{
								System.out.println("lsApplicableOrdTypesArr[i]"+lsApplicableOrdTypesArr[i]);
								if(orderType.equalsIgnoreCase(lsApplicableOrdTypesArr[i]))
								{
									lbProceed=true;
									break;
								}
							}
						}
						prevScheme = schemeCode1;
						schemeCode1 = schemeCode;
						if (applyCustList.trim().length() > 0)
						{
							schemeCode1 = "";
						
							String applyCustListArr[]=applyCustList.split(",");
							for(int i=0;i<applyCustListArr.length;i++)
							{
								System.out.println("applyCustListArr[i]"+applyCustListArr[i]);
								if(applyCustListArr[i].equalsIgnoreCase(custCode))
								{
									schemeCode1 = schemeCode;
									custSchemeCode = schemeCode;
									break;
								}
							}
						}
						if (noApplyCustList.trim().length() > 0 && schemeCode != null)
						{
							String noapplyCustListArr[]= noApplyCustList.split(",");
							for(int i=0; i < noapplyCustListArr.length;i++)
							{
								System.out.println("noapplyCustListArr[i]" + noapplyCustListArr[i]);
								if(noapplyCustListArr[i].equalsIgnoreCase(custCode))
								{
									schemeCode1 = "";
									break;
								}
							}
						}*/
						
						System.out.println("CHECK22");
						schemeCode1 = getCustSchemeCode(custCode, schemeCode, orderType, conn);
						System.out.println("CHECK22 schemeCode1"+schemeCode1);
						System.out.println("schemeCode111["+schemeCode1+"]");
						if (schemeCode1 != null)
						{
							schCnt++;
						} 
						else if (schCnt == 1)
						{
							schemeCode1 = prevScheme;
						}
						System.out.println("schCnt["+schCnt+"]");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					System.out.println("schCnt121313["+schCnt+"]");
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeListLength; ctr++) 
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						
						if (schCnt == 0)
						{
							System.out.println("Scheme is not applicable for the entered item code");
							errCode = "VTFREEQTY";// Scheme is not applicable for the
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
						else if (schCnt > 1)
						{
							errCode = "VTITEM10";// Item cannot have more than one
							                     // scheme applicable for same period.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Item cannot have more than one scheme applicable for same period.");
						} 
						else if (custSchemeCode.trim().length() > 0)
						{
							schemeCode = custSchemeCode;
						}
					}
					sql = "select app_from, valid_upto from scheme_applicability where scheme_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode1);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						 appFrom = rs.getDate("app_from");
						 validUpto = rs.getDate("valid_upto");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					System.out.println("appFrom["+appFrom+"]validUpto["+validUpto+"]");
					
					sql = "select tot_charge_qty, tot_free_qty,tot_sample_qty,tot_bonus_qty 	from prd_scheme_trace where site_code= ? and cust_code	=? and item_code = ? and scheme_code= ? and ? between eff_from and valid_upto";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setString(3, itemCodeParent);
					pstmt.setString(4, schemeCode1);
					pstmt.setTimestamp(5, tranDate);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						totChargeQty = rs.getDouble("tot_charge_qty");
						totFreeQty = rs.getDouble("tot_free_qty");
						totSampleQty = rs.getDouble("tot_sample_qty");
						totBonusQty = rs.getDouble("tot_bonus_qty");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (totChargeQty == 0)
					{
						totChargeQty = 0;
					}
					if (totFreeQty == 0)
					{
						totFreeQty = 0;
					}
					if (totSampleQty == 0)
					{
						totSampleQty = 0;
					}
					if (totBonusQty == 0)
					{
						totBonusQty = 0;
					}
					if (minQty == 0)
					{
						minQty = 0;
					}
					if (saleOrder == null)
					{
						saleOrder = "";
					}
					if (lbParent == false)
					{
						sql = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty ," 
								+ " sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty ," 
								+ "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " 
								+ "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty "
								+ "from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
								+ "and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
								+ "	and b.item_code__ord in (select item_code from item where item_code__parent = ?)"
								+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						pstmt.setString(3, saleOrder);
						pstmt.setDate(4, appFrom);
						pstmt.setDate(5, validUpto);
						pstmt.setString(6, itemCodeParent);
						pstmt.setString(7, nature);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							unConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
							unConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
							unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
							unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;

					} 
					else
					{
						sql = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty ,"
								+ " sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty ," 
								+ "sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, "
								+ "sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty "
								+ "from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	" 
								+ "and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
								+ "	and b.item_code__ord in (select item_code from item where item_code__parent = ?)" 
								+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						pstmt.setString(3, saleOrder);
						pstmt.setDate(4, appFrom);
						pstmt.setDate(5, validUpto);
						pstmt.setString(6, itemCodeParent);
						pstmt.setString(7, nature);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							unConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
							unConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
							unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
							unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					}
					
					if (unConfTotChargeQty == 0)
					{
						unConfTotChargeQty = 0;
					}
					if (unConfTotFreeQty == 0)
					{
						unConfTotFreeQty = 0;
					}
					if (unConfTotSampleQty == 0)
					{
						unConfTotSampleQty = 0;
					}
					if (unConfTotBonusQty == 0)
					{
						unConfTotBonusQty = 0;
					}
					sql = "select sum(case when nature = 'C' then quantity else 0 end) - sum(case when nature ='C' then qty_desp else 0 end)as confirmChargeQty,"
							+ " sum(case when nature ='F' then quantity else 0 end)- sum(case when nature ='F' then qty_desp else 0 end) as confirmFreeQty," 
							+ " sum(case when nature ='B' then quantity else 0 end)- sum(case when nature ='B' then qty_desp else 0 end) as confirmBonusQty," 
							+ " sum(case when nature ='S' then quantity else 0 end)- sum(case when nature ='S' then qty_desp else 0 end) as confirmSampleQty " 
							+ "from sorditem ,SORDER where sorditem.sale_order = SORDER.SALE_ORDER AND sorditem.site_code = ? AND SORDER.cust_code = ? "
							+ "and sorditem.sale_order <> ? and sorditem.line_type  <> 'B' and sorditem.order_date between  ? and  ?  "
							+ "and (sorditem.item_code in (select item_code from item where item_code__parent = ?) OR sorditem.item_code = ?)"
							+ " and sorditem.nature in ('C' ,?) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					pstmt.setString(3, saleOrder);
					pstmt.setDate(4, appFrom);
					pstmt.setDate(5, validUpto);
					pstmt.setString(6, itemCodeParent);
					pstmt.setString(7, itemCodeParent);
					pstmt.setString(8, nature);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						ConfTotChargeQty = rs.getDouble("confirmChargeQty");
						ConfTotFreeQty = rs.getDouble("confirmFreeQty");
						ConfTotSampleQty = rs.getDouble("confirmBonusQty");
						ConfTotBonusQty = rs.getDouble("confirmSampleQty");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (ConfTotChargeQty == 0)
					{
						ConfTotChargeQty = 0;
					}
					if (ConfTotFreeQty == 0)
					{
						ConfTotFreeQty = 0;
					}
					if (ConfTotSampleQty == 0)
					{
						ConfTotSampleQty = 0;
					}
					if (ConfTotBonusQty == 0)
					{
						ConfTotBonusQty = 0;
					}
					currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					prvChargeQty = 0;
					prvFreeQty = 0;
					prvBonusQty = 0;
					prvSampleQty = 0;
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();

					for (ctr = 0; ctr < childNodeListLength; ctr++)
					{

						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom2));
						nature = checkNull(genericUtility.getColumnValue("nature", dom2));
						browItemCode = checkNull(genericUtility.getColumnValue("item_code", dom2));
						if (currLineNo != lineNo)
						{
							cnt = 0;
							sql = "select item_code__parent from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, browItemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								itemCodeParentCurr = rs.getString("item_code__parent");
								System.out.println("itemCodeParentCurr: ====" + itemCodeParentCurr);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (itemCodeParentCurr == null || itemCodeParentCurr.trim().length() == 0)
							{
								sql = "select count(1)  from item where item_code__parent = ?	";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, browItemCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;
								if (cnt > 0)
								{
									itemCodeParentCurr = browItemCode;
								}
							}
							if (itemCodeParentCurr.trim() == itemCodeParent.trim())
							{
								quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom2));
								if (nature.equals("F"))
								{
									prvFreeQty = prvFreeQty + quantity;
								} else if (nature.equals("B"))
								{
									prvBonusQty = prvBonusQty + quantity;
								} else if (nature.equals("S"))
								{
									prvSampleQty = prvSampleQty + quantity;
								} else
								{
									prvChargeQty = prvChargeQty + quantity;
								}
							}
						}
					}
					chargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty + ConfTotChargeQty;
					sql = "Select count(1) From bom Where bom_code = ? And  ? between case when min_qty is null then 0 else min_qty end"
						+ " And case when max_qty is null then 0 else max_qty end";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode1);
					pstmt.setDouble(2, chargeQty);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt(1);
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (cnt == 0)
					{
						System.out.println("Chargeable quantity of group of items is not eligible for the free quantity");
						errCode = "VTFREEQTY";// Chargeable quantity of group of
						                      // items is not eligible for the free
						                      // quantity
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						//System.out.println("Chargeable quantity of group of items is not eligible for the free quantity");
					}
					sql = "select bom.batch_qty,bomdet.qty_per,bomdet.min_qty	," +
						  "bomdet.app_min_qty,bomdet.app_max_qty,bomdet.round	,bomdet.round_to from bom, bomdet where bom.bom_code = bomdet.bom_code and	bomdet.bom_code = ? and	bomdet.nature	= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode1);
					pstmt.setString(2, nature);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						batQty = rs.getDouble("batch_qty");
						qtyPer = rs.getDouble("qty_per");
						minQty = rs.getDouble("min_qty");
						appMinQty = rs.getDouble("app_min_qty");
						appMaxQty = rs.getDouble("app_max_qty");
						round = rs.getString("round");
						roundTo = rs.getDouble("round_to");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if (chargeQty >= appMinQty && chargeQty <= appMaxQty)
					{
						freeQty = roundValue(chargeQty / batQty, 0) * qtyPer;
					} else
					{
						freeQty = 0;
					}
					if (freeQty == 0)
					{
						freeQty = 0;
					}
					if (freeQty > 0)
					{
						if (round != null && roundTo != 0)
						{
							freeQty = distCommon.getRndamt(freeQty, round, roundTo);
						}
					}
					if (nature.equals("F"))
					{
						if ((qty + totFreeQty + unConfTotFreeQty + prvFreeQty + ConfTotFreeQty) > freeQty)
						{
							System.out.println("Entered free quantity is greater than scheme's free");
							errCode = "VTFREEQTY1";// Entered free quantity is
							                       // greater than scheme's free
							                       // quantity
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Entered free quantity is greater than scheme's free quantity");
						}
					}
					if (nature.equals("S"))
					{
						if ((qty + totSampleQty + unConfTotSampleQty + prvSampleQty + ConfTotSampleQty) > freeQty)
						{
							errCode = "VTSAMPQTY1";// Entered Sample quantity is
							                       // greater than scheme's Sample
							                       // quantity
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("Entered Sample quantity is greater than scheme's Sample quantity");
						}
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
			System.out.println("errCode OF valdataSchemeCode ::" + errCode);
			return errCode;
		}
		// gbf_check_scheme
		public String getCheckScheme(String itemCode, String orderType, String custCode, String siteCode, String stateCode, String countCode, Date orderDate,Connection conn) throws ITMException
		{
			System.out.println("Inside getCheckScheme......");
			PreparedStatement pstmt = null, pstmt1 = null;
			ResultSet rs = null, rs1 = null;
			String sql = "";
			boolean  lbProceed = false;
			;
			String  schemeCode = "", applyCustList = "", noApplyCustList = "", appOrderType = "", lsToken = "";
			String prevScheme = "", schemeCode1 = "", applyCust = "";
			long schCnt = 0;
			try
			{
				if (orderType == null)
				{
					orderType = "";
				}
				if (siteCode == null)
				{
					siteCode = "";
				}
				if (stateCode == null)
				{
					stateCode = "";
				}
				if (countCode == null)
				{
					countCode = "";
				}
				sql = "select a.scheme_code  from scheme_applicability   a,scheme_applicability_det  b " +
				      " where a.scheme_code = b.scheme_code and a.item_code =? " + " and a.app_from  <= ?  and a.valid_upto  >= ?  and" + 
				      " (b.site_code  = ?  or b.state_code = ? or b.count_code =? )";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setDate(2, (java.sql.Date) orderDate);
				pstmt.setDate(3, (java.sql.Date) orderDate);
				pstmt.setString(4, siteCode);
				pstmt.setString(5, stateCode);
				pstmt.setString(6, countCode);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					schemeCode = rs.getString("scheme_code");
					/*sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, schemeCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						applyCustList = rs1.getString("apply_cust_list");
						noApplyCustList = rs1.getString("noapply_cust_list");
						appOrderType = rs1.getString("order_type");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if (orderType.trim() == "NE" && (appOrderType == null || appOrderType.trim().length() == 0))
					{
						break;
					} else if (appOrderType != null && appOrderType.trim().length() > 0)
					{
						lbProceed = false;
						do
						{
							lsToken = distCommon.getToken(appOrderType, ",");
							if (orderType.trim().length() == lsToken.trim().length())
							{
								lbProceed = true;
							}
						} while (appOrderType.trim().length() > 0);
					}
					prevScheme = schemeCode1;
					schemeCode1 = schemeCode;
					if (applyCustList.trim().length() > 0)
					{
						schemeCode1 = "";
						do
						{
							applyCust = distCommon.getToken(applyCustList, ",");
							if (applyCust.trim().length() == custCode.trim().length())
							{
								schemeCode1 = schemeCode;
								//custSchemeCode = schemeCode;
								break;
							}
						} while (applyCustList.trim().length() > 0);
					}
					if (noApplyCustList.trim().length() > 0 && schemeCode != null)
					{
						do
						{
							applyCust = distCommon.getToken(noApplyCustList, ",");
							if (noApplyCustList.trim().length() == custCode.trim().length())
							{
								schemeCode1 = "";
							}
						} while (noApplyCustList.trim().length() > 0);
					}
					if (schemeCode1 != null)
					{
						schCnt++;
					} else if (schCnt == 1)
					{
						schemeCode1 = prevScheme;
					}*/
					
					schemeCode1= getCustSchemeCode(custCode, schemeCode1, orderType, conn);
				}
				pstmt.close();pstmt = null;
				rs.close();rs = null;
				
				

			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			System.out.println("errCode OF getcheckScheme ::" + schemeCode1);
			return schemeCode1;

		}
		
		public double getDiscount(String plistDisc,Timestamp orderDate,String custCode,String siteCode,String itemCode,String unit,double discMerge,Timestamp plDate,double sordItmQty,Connection conn) throws SQLException, ITMException
		{
			String ls_listtype = "", itemSer = "",sql="";
			double rate=0.0,discPerc=0.0;
			PreparedStatement pstmt=null;
			ResultSet rs = null;
			try
			{
				System.out.println("Inside getDiscount...... +"+plistDisc);
				if(plistDisc.trim().length() > 0)
				{

					sql = "    select case when rate is null then 0 else rate end as rate" +
							" from    pricelist where price_list    = ? and " +
							"    item_code     = ? and unit = ? " +
							" and    list_type    IN    ('M','N') " +
							" and    case when min_qty is null then 0 else min_qty end     <=    ? " +
							" and    ((case when max_qty is null then 0 else max_qty end    >=    ? ) " +
							" OR  (case when max_qty is null then 0 else max_qty end    =0)) and eff_from <=    ?  " +
							" and    valid_upto >=    ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,plistDisc);
					pstmt.setString(2,itemCode);
					pstmt.setString(3,unit);
					pstmt.setDouble(4,sordItmQty);
					pstmt.setDouble(5,sordItmQty);
					pstmt.setTimestamp(6,plDate);
					pstmt.setTimestamp(7,plDate);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						rate = rs.getDouble("rate");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


				}

				if("M".equalsIgnoreCase(ls_listtype) || plistDisc == null || plistDisc.trim().length() == 0 
						|| rate == 0)
				{
					sql = "select item_ser from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer = rs.getString("item_ser");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select disc_perc from customer_series where cust_code = ? and item_ser = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					pstmt.setString(2,itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						discPerc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(discPerc == 0)
					{
						sql = "select disc_perc from site_customer where site_code = ? and cust_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							discPerc = rs.getDouble("disc_perc");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if(discPerc == 0)
					{
						sql = "select disc_perc  from customer where cust_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							discPerc = rs.getDouble("disc_perc");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

					}
					if("M".equalsIgnoreCase(ls_listtype))
					{
						discMerge = discPerc;
						if(rate != 0)
						{
							discPerc = rate;    
						}
					}
					else
					{
						discMerge = 0;
					}


				}
				if(itemCode == null)
				{
					discPerc = 0;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::"+ e.getMessage()); 
				throw new ITMException(e); 
			}


			return discPerc;
		}
		
		// gbf_calc_rate
		public double calRate(double discPer, double adRate)
		{
			if (adRate == 0)
			{
				adRate = 0;
			}
			if (discPer == 0)
			{
				discPer = 0;
			}
			adRate = adRate - (discPer * adRate) / 100;
			if (adRate < 0)
			{
				adRate = 0;
			}
			return adRate;
		}
		
		// gbf_valdata_group_scheme_value

	public String valDataGroupSchemeValue(Document dom, Document dom1, Document dom2, String currentColumn, String objContext, String editFlag, String nature, Connection conn) throws ITMException
	{
		System.out.println("Inside valDataGroupSchemeValue......");
		SimpleDateFormat sdf;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		String sql = "" ;
		double totChargeQty = 0, unConfTotChargeQty = 0, unConfTotFreeQty = 0, unConfTotSampleQty = 0, unConfTotBonusQty = 0,prvChargeQty = 0, quantity = 0;
		String itemCodeOrd = "", siteCode = "", custCode = "",orderType = "",itemCodeParent = "",itemCodeParentCurr = "";
		String countCodeDlv = "", saleOrder = "", schemeCode = "", applyCustList = "", qty = "",lsToken = "";
		String prevScheme = "", lineNoSord = "", browItemCode = "", currLineNo = "";
		String errCode = "",mstateCd = "", pricelist = "",curscheme = "" ,noapplyCustList= "",applicableOrdTypes ="",
		applyCust = "" ,noApplyCust = "" , custschemeCode= "" , errcode = "" , schemeFlag= "" ,
		mNumCurLineNo= "";
		String childNodeName = null;
		Node childNode = null;
		int childNodeListLength = 0;
		int ctr = 0 ,parentCnt = 0;
		NodeList childNodeList = null;
		NodeList parentNodeList = null;

		
		double lcTotChargeQty = 0.0,qtyDb = 0.0 ,lcTotFreeQty = 0.0,lcRate = 0.0,lcTotBonusQty = 0.0,lcTotSampleQty = 0.0 ,lcUnconfirmChargeValue = 0.0 ,lcUnconfirmFreeValue = 0.0 ,	lcUnconfirmBonusValue = 0.0 ,	lcUnconfirmSampleValue = 0.0 ,	
				lcTotChargeValue = 0.0 ,lcTotFreeValue = 0.0 ,lcTotBonusValue =0.0 ,lcTotSampleValue = 0.0 ,mvalue = 0.0 ,
				lcConfirmChargeValue = 0.0 ,lcConfirmFreeValue = 0.0 ,	lcConfirmBonusValue = 0.0 ,	lcConfirmSampleValue = 0.0 ,
				ConfChargeQty = 0.0,ConfFreeQty = 0.0,ConfSampleQty = 0.0,ConfBonusQty = 0.0,lcConfirmPreChargeValue = 0.0,lcConfirmPreFreeValue = 0.0,lcConfirmPreBonusValue = 0.0	,lcConfirmPreSampleValue = 0.0,
				ConfPreChargeQty = 0.0,ConfPreFreeQty = 0.0,ConfPreBonusQty = 0.0,ConfPreSampleQty = 0.0 ,
				lcPrvChargeQty = 0,	prvSampleValue = 0.0 , prvFreeValue = 0.0 ,prvBonusValue = 0.0 ,prvChargeValue = 0.0,
				lcConfFreeValue = 0 ,lcConfBonusValue = 0 ,lcConfSampleValue = 0 ,lcConfChargeValue = 0 ,lcChargeQty = 0.0 ,lcChargeValue = 0.0 ,
				lcBatvalue = 0.0 ,lcValueper = 0.0 ,lcMinvalue = 0.0 ,lcAppMinValue = 0.0 ,lcAppMaxValue = 0.0 ,lsRound = 0.0 ,ldRoundto = 0.0 ,lcFreeValue = 0.0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int cnt = 0 ,llSchcnt = 0;
		Date orderDate = null ;//appFrom = null, validUpto = null;
		java.sql.Date appFrom= null , validUpto = null;
		boolean lbParent = false ,lbProceed = false ;
		Timestamp tranDate = null ;
					
			try
			{
				sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						
				itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
				lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
				siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
				qty = checkNull(genericUtility.getColumnValue("quantity", dom));
				countCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom1));
				saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
				if(qty != null && qty.trim().length() > 0 )
				{
					qtyDb = Double.parseDouble(qty);
				}
				sql = "select order_type,order_date,cust_code,state_code__dlv,price_list from sorder where sale_order = ? ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					orderType = checkNull(rs.getString("order_type"));
					tranDate = rs.getTimestamp("order_date");
					custCode = checkNull(rs.getString("cust_code"));
					mstateCd = checkNull(rs.getString("state_code__dlv"));
					pricelist  = checkNull(rs.getString("price_list"));
					
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				System.out.println("custCode["+custCode+"]orderType["+orderType+"]mstateCd["+mstateCd +"]pricelist["+pricelist);
				sql = "select item_code__parent from item where item_code = ?  and item_code__parent is not null";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeOrd);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemCodeParent  = checkNull(rs.getString("item_code__parent"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if (itemCodeParent == null || itemCodeParent.trim().length() == 0)
				{
					
					sql = "select item_code__parent from item where item_code = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeOrd);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemCodeParent  = checkNull(rs.getString("item_code__parent"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					if (itemCodeParent == null || itemCodeParent.trim().length() == 0)
					{
						
						sql = "select count(1)  from item where item_code__parent = ?	";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							parentCnt = rs.getInt(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						if (parentCnt > 0)
						{
							itemCodeParent = itemCodeOrd;
							lbParent = true ;
						}
					}
				}
				System.out.println("itemCodeParent.trim().length()"+itemCodeParent.trim().length());
				if(itemCodeParent.trim().length() > 0)  //poonamcom
				{
					System.out.println("iNSIDE WHILE");
					sql = " select a.scheme_code  from scheme_applicability   a,scheme_applicability_det  b " +
						  " where a.scheme_code = b.scheme_code and a.item_code =? " + " and a.app_from  <= ?  and a.valid_upto  >= ?  and" + 
						  " (b.site_code  = ?  or b.state_code = ? or b.count_code = ? )";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeParent);
						pstmt.setTimestamp(2,  tranDate);
						pstmt.setTimestamp(3,  tranDate);
						pstmt.setString(4, siteCode);
						pstmt.setString(5, mstateCd);
						pstmt.setString(6, countCodeDlv);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							curscheme = checkNull(rs.getString(1));
							
							System.out.println("while curscheme"+curscheme);
							/*sql1 = " select (case when apply_cust_list is null then ' ' else apply_cust_list end)," +
								  " (case when noapply_cust_list is null then ' ' else noapply_cust_list end),order_type from 	scheme_applicability " +
								  " where scheme_code = ? ";
							pstmt1 =  conn.prepareStatement(sql1);
							pstmt1.setString(1,curscheme );
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								applyCustList = checkNull(rs.getString(1));
								noapplyCustList = checkNull(rs.getString(2));
								applicableOrdTypes = checkNull(rs.getString(3));
								
							}
							rs1.close();rs1 = null;
							pstmt1.close();pstmt1 = null;
							
							if("NE".equalsIgnoreCase(orderType) && (applicableOrdTypes == null || applicableOrdTypes.trim().length() == 0))
							{
								continue;
							}
							else if(applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
							{

								lbProceed = false ;
								String lsApplicableOrdTypesArr[]= applicableOrdTypes.split(",");
								for(int i=0;i<lsApplicableOrdTypesArr.length;i++)
								{
									System.out.println("lsApplicableOrdTypesArr[i]"+lsApplicableOrdTypesArr[i]);
									if(orderType.equalsIgnoreCase(lsApplicableOrdTypesArr[i]))
									{
										lbProceed=true;
										break;
									}
								}
							
								if(!lbProceed)
								{
									continue;
								}
							}
							prevScheme	= schemeCode ;	 
							schemeCode = curscheme ; 
							
							if(applyCustList.trim().length() > 0)
							{
								custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
									String applyCustListArr[]=applyCustList.split(",");
									for(int i=0;i<applyCustListArr.length;i++)
									{
										System.out.println("applyCustListArr[i]"+applyCustListArr[i]);
										if(applyCustListArr[i].equalsIgnoreCase(custCode))
										{
											schemeCode = curscheme;
											custschemeCode = curscheme ;
											break;
										}
									}
							}
				
							if(noapplyCustList.trim().length() > 0 && schemeCode != null)
							{
								if(noapplyCustList.trim().length() > 0 && schemeCode != null)
								{
									custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
									String noapplyCustListArr[]=noapplyCustList.split(",");
									for(int i=0; i < noapplyCustListArr.length;i++)
									{
										System.out.println("noapplyCustListArr[i]" + noapplyCustListArr[i]);
										if(noapplyCustListArr[i].equalsIgnoreCase(custCode))
										{
											schemeCode = "";
											break;
										}
									}
								}
							}
							
							if(schemeCode != null)
							{
								llSchcnt ++;
							}
							else if(llSchcnt == 1)
							{
								schemeCode	= prevScheme ;
							}
							continue;*/
							System.out.println("BEFORE SCHME CODE");
							schemeCode = getCustSchemeCode(custCode, curscheme, orderType, conn);
							System.out.println("AFTER SCHME CODE["+schemeCode+"]");
							
							if(schemeCode != null)
							{
								llSchcnt ++;
							}
							else if(llSchcnt == 1)
							{
								schemeCode	= prevScheme ;
							}
							System.out.println("llSchcnt["+llSchcnt+"]");
							continue;
							
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						parentNodeList = dom.getElementsByTagName("Detail2");
						Node parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for (ctr = 0; ctr < childNodeListLength; ctr++)
						{
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							
							if(llSchcnt == 0 )
							{
								System.out.println("CHECK221llSchcnt");
								errcode = "VTFREEQTY" ;
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(llSchcnt > 1)
							{
								errcode = "VTITEM10" ;
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(custschemeCode.trim().length() > 0)
							{
								schemeCode = custschemeCode;
							}
						}
					    
						sql = "select scheme_flag from bom where bom_code = ?	";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, schemeCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							schemeFlag = rs.getString("scheme_flag");
						}
						pstmt.close();pstmt = null;
						rs.close();rs = null;
						
						sql = "select scheme_flag from bom where bom_code = ?	";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, schemeCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							schemeFlag = rs.getString("scheme_flag");
						}
						pstmt.close();pstmt = null;
						rs.close();rs = null;
					
						sql = "select app_from, valid_upto from scheme_applicability where scheme_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, schemeCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							appFrom = rs.getDate("app_from");
							validUpto = rs.getDate("valid_upto");
						}
						pstmt.close();pstmt = null;
						rs.close();rs = null;
						
						sql = "select tot_charge_qty, tot_free_qty,rate,tot_bonus_qty,tot_sample_qty" +
								" from prd_scheme_trace where site_code = ? and cust_code	= ? and item_code	= ?" +
								" and scheme_code= ? and ? between eff_from and valid_upto ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						pstmt.setString(3, itemCodeParent);
						pstmt.setString(4, schemeCode);
						pstmt.setTimestamp(5, tranDate);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lcTotChargeQty = rs.getDouble("tot_charge_qty");
							lcTotFreeQty = rs.getDouble("tot_free_qty");
							lcRate = rs.getDouble("rate");
							lcTotBonusQty = rs.getDouble("tot_bonus_qty");
							lcTotSampleQty = rs.getDouble("tot_sample_qty");
							
						}
						pstmt.close();pstmt = null;
						rs.close();rs = null;
						
						if(lcRate == 0)
						{
							if(pricelist.trim().length() > 0)
							{
								lcRate = distCommon.pickRate(pricelist, sdf.format(tranDate), itemCodeParent,"","L" ,qtyDb, conn);
							}
							else
							{
								lcRate = 0 ;
							}
							
							if(lcRate == 0 || Double.toString(lcRate) == null)
							{
								pricelist = distCommon.getDisparams("999999", "STD_SO_PL", conn);
								if(pricelist == null || "NULLFOUND".equalsIgnoreCase(pricelist))
								{
									lcRate = 0 ;
								}
								else
								{
									lcRate = distCommon.pickRate(pricelist, sdf.format(tranDate), itemCodeParent,"","L" , conn);
								}
							}
							
							if(lbParent == false)
							{

								sql = "select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty ," +
								" sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty ," + 
								" sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " + 
								" sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty " +
								" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	" + 
								" and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ? " + 
								" and b.item_code__ord in (select item_code from item where item_code__parent = ?)" +
								" and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, custCode);
								pstmt.setString(3, saleOrder);
								pstmt.setDate(4, appFrom );
								pstmt.setDate(5, validUpto);
								pstmt.setString(6, itemCodeParent);
								pstmt.setString(7, nature);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									unConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
									unConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
									unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
									unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;

							
							}
							else
							{
								sql = 	" select sum(case when nature ='C' then quantity else 0 end) as unconfirmChargeQty ," + 
										" sum(case when nature ='F' then quantity else 0 end) as unconfirmFreeQty ," + 
										" sum(case when nature ='B' then quantity else 0 end)as unconfirmBonusQty, " +
										" sum(case when nature ='S' then quantity else 0 end)as unconfirmSampleQty " + 
										" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	" +
										" and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?" + 
										" and b.item_code__ord in (select item_code from item where item_code__parent = ?)" + 
										" and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('C' ,?)";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, custCode);
								pstmt.setString(3, saleOrder);
								pstmt.setDate(4, appFrom);
								pstmt.setDate(5, validUpto);
								pstmt.setString(6, itemCodeParent);
								pstmt.setString(7, nature);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									unConfTotChargeQty = rs.getDouble("unconfirmChargeQty");
									unConfTotFreeQty = rs.getDouble("unconfirmFreeQty");
									unConfTotSampleQty = rs.getDouble("unconfirmSampleQty");
									unConfTotBonusQty = rs.getDouble("unconfirmBonusQty");
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;
							
							}
							
							if (unConfTotChargeQty == 0)
							{
								unConfTotChargeQty = 0;
							}
							if (unConfTotFreeQty == 0)
							{
								unConfTotFreeQty = 0;
							}
							if (unConfTotSampleQty == 0)
							{
								unConfTotSampleQty = 0;
							}
							if (unConfTotBonusQty == 0)
							{
								unConfTotBonusQty = 0;
							}
							
							if("V".equalsIgnoreCase(schemeFlag))
							{
								lcUnconfirmChargeValue = unConfTotChargeQty * lcRate ;
								lcUnconfirmFreeValue = unConfTotFreeQty * lcRate ;	
								lcUnconfirmBonusValue = unConfTotBonusQty * lcRate ;	
								lcUnconfirmSampleValue = unConfTotSampleQty * lcRate ;	
								lcTotChargeValue = lcTotChargeQty * lcRate ;
								lcTotFreeValue = lcTotFreeQty * lcRate ;
								lcTotBonusValue = lcTotBonusQty * lcRate ;
								lcTotSampleValue = lcTotSampleQty * lcRate ;
								mvalue = qtyDb * lcRate ;
								
										
							}
							
							sql = 	" select sum(case when nature ='C' then quantity else 0 end) ," + 
									" sum(case when nature ='F' then quantity else 0 end)  ," + 
									" sum(case when nature ='B' then quantity else 0 end), " +
									" sum(case when nature ='S' then quantity else 0 end) " + 
									" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	" +
									" and a.cust_code = ? and a.sale_order = ? and a.order_date between ? and ?" + 
									" and b.item_code__ord in (select item_code from item where item_code__parent = ?)" + 
									" and a.confirmed = 'Y'	and b.nature in ('C' ,?)";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, custCode);
							pstmt.setString(3, saleOrder);
							pstmt.setDate(4, appFrom);
							pstmt.setDate(5,validUpto);
							pstmt.setString(6, itemCodeParent);
							pstmt.setString(7, nature);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ConfChargeQty = rs.getDouble(1);
								ConfFreeQty = rs.getDouble(2);
								ConfBonusQty = rs.getDouble(3);
								ConfSampleQty = rs.getDouble(4);
								
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							
							if("V".equalsIgnoreCase(schemeFlag))
							{
								lcConfirmChargeValue = ConfChargeQty * lcRate ;
								lcConfirmFreeValue = ConfFreeQty * lcRate ;	
								lcConfirmBonusValue = ConfBonusQty * lcRate ;	
								lcConfirmSampleValue = ConfSampleQty * lcRate ;	
							}
							
						}
						
						sql = " select sum(case when nature ='C' then quantity else 0 end) - sum(case when nature ='C' then qty_desp else 0 end)," + 
							  " sum(case when nature ='F' then quantity else 0 end)- sum(case when nature ='F' then qty_desp else 0 end) ," +
							  " sum(case when nature ='B' then quantity else 0 end)- sum(case when nature ='B' then qty_desp else 0 end) ," + 
							  " sum(case when nature ='S' then quantity else 0 end)- sum(case when nature ='S' then qty_desp else 0 end)  " + 
							  " from sorditem ,SORDER where sorditem.sale_order = SORDER.SALE_ORDER AND sorditem.site_code = ? AND SORDER.cust_code =? " +
							  " and sorditem.sale_order <> ? and sorditem.line_type  <> 'B' and sorditem.order_date between ? and ? " +
							  " and (sorditem.item_code in (select item_code from item where item_code__parent =?) OR sorditem.item_code = ?)" +
							  " and sorditem.nature in ('C' ,?) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						pstmt.setString(3, saleOrder);
						pstmt.setDate(4, appFrom);
						pstmt.setDate(5, validUpto);
						pstmt.setString(6, itemCodeParent);
						pstmt.setString(7, itemCodeParent);
						pstmt.setString(8, nature);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ConfPreChargeQty = rs.getDouble(1);
							ConfPreFreeQty = rs.getDouble(2);
							ConfPreBonusQty = rs.getDouble(3);
							ConfPreSampleQty = rs.getDouble(4);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						
						if("V".equalsIgnoreCase(schemeFlag))
						{
							lcConfirmPreChargeValue = ConfPreChargeQty * lcRate ;
							lcConfirmPreFreeValue = ConfPreFreeQty * lcRate ;	
							lcConfirmPreBonusValue = ConfPreBonusQty * lcRate ;	
							lcConfirmPreSampleValue = ConfPreSampleQty * lcRate ;	
						}
						parentNodeList = dom.getElementsByTagName("Detail2");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for (ctr = 0; ctr < childNodeListLength; ctr++)
						{

							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							mNumCurLineNo = checkNull(genericUtility.getColumnValue("line_no", dom2));
							nature = checkNull(genericUtility.getColumnValue("nature", dom2));
							browItemCode = checkNull(genericUtility.getColumnValue("item_code__ord", dom2));
							if (currLineNo != mNumCurLineNo)
							{
								cnt = 0;
								sql = "select item_code__parent from item where item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, browItemCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									itemCodeParentCurr = rs.getString("item_code__parent");
									System.out.println("itemCodeParentCurr: ====" + itemCodeParentCurr);
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;
								if (itemCodeParentCurr == null || itemCodeParentCurr.trim().length() == 0)
								{
									sql = "select count(1)  from item where item_code__parent = ?	";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, browItemCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt = rs.getInt(1);
									}
									pstmt.close();
									rs.close();
									pstmt = null;
									rs = null;
									if (cnt > 0)
									{
										itemCodeParentCurr = browItemCode;
									}
								}
								if (itemCodeParentCurr.trim() == itemCodeParent.trim())
								{
									if("V".equalsIgnoreCase(schemeFlag))
									{
										
										quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom2));
										if (nature.equals("F"))
										{
											prvFreeValue = prvFreeValue + (quantity * lcRate);
										} 
										else if (nature.equals("B"))
										{
											prvBonusValue = prvBonusValue + (quantity * lcRate);
										} 
										else if (nature.equals("S"))
										{
											prvSampleValue = prvSampleValue + (quantity * lcRate);
										} 
										else
										{
											prvChargeValue = prvChargeValue + (quantity * lcRate);
										}
									}
								}
							}
						}
						
						if(prvChargeValue > 0)
						{
							lcConfChargeValue = 0;
						}
						
						if(lineNoSord.trim().length() > 0)
						{
							lcConfFreeValue = 0 ;
							lcConfBonusValue = 0 ;
							lcConfSampleValue = 0 ;
						}
						
						lcChargeQty = unConfTotChargeQty + prvChargeQty + totChargeQty ;
						
						if("V".equalsIgnoreCase(schemeFlag))
						{
							lcChargeValue = lcUnconfirmChargeValue + prvChargeValue + lcTotChargeValue +lcConfChargeValue + lcConfirmPreChargeValue;
						}

						if("V".equalsIgnoreCase(schemeFlag))
						{
							
							sql = "Select count(1)  From	 bom Where  bom_code 	= ? and  ? between case when min_batch_value is null then 0 else min_batch_value end And" +
									"case when max_batch_value is null then 0 else max_batch_value end";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							pstmt.setDouble(2, lcChargeValue);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
								
						}
						if (cnt ==  0)
						{
							errcode = "VTFREEVAL2";
							errList.add(errcode);
							errFields.add(childNodeName.toLowerCase());
						}
							
						

						if("V".equalsIgnoreCase(schemeFlag))
						{
							sql = "Select select bom.batch_value,bomdet.value_per ,bomdet.min_value	,bomdet.app_min_value," +
									"bomdet.app_max_value,bomdet.round, bomdet.round_to from bom, bomdet Where bom.bom_code = bomdet.bom_code and" +
									"bomdet.bom_code 	= ? and  bomdet.nature 		= ?" ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							pstmt.setString(2, nature);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lcBatvalue = rs.getDouble(1);
								lcValueper = rs.getDouble(2);
								lcMinvalue = rs.getDouble(3);
								lcAppMinValue = rs.getDouble(4);
								lcAppMaxValue = rs.getDouble(5);
								lsRound = rs.getDouble(6);
								ldRoundto = rs.getDouble(7);
							}
							else
							{
								System.out.println("CHECK221");
								errcode = "VTFREEQTY";
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
						}
						
						if("V".equalsIgnoreCase(schemeFlag))
						{
							if(lcChargeValue >= lcAppMinValue && lcChargeValue<= lcAppMaxValue)
							{
								lcFreeValue = lcChargeValue / lcBatvalue * lcValueper ;
							}
							else
							{
								lcFreeValue = 0;
							}
						}
						
						if(lcFreeValue > 0 )
						{
							if(Double.toString(lsRound) != null && Double.toString(ldRoundto) != null)
							{
								lcFreeValue = distCommon.getRndamt(lcFreeValue, Double.toString(lsRound), ldRoundto);
							}
						}
						
						if("V".equalsIgnoreCase(schemeFlag))
						{
							if("F".equalsIgnoreCase(nature))
							{
								if((mvalue + lcTotFreeValue + lcUnconfirmFreeValue + prvFreeValue + lcConfirmFreeValue + lcConfirmPreFreeValue ) > lcFreeValue)
								{
									errcode = "VTFREEVAL1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if("B".equalsIgnoreCase(nature))
							{
								if((mvalue + lcTotBonusValue + lcUnconfirmBonusValue + prvBonusValue + lcConfirmBonusValue + lcConfirmPreBonusValue ) > lcFreeValue)
								{
									errcode = "VTBONUVAL1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if("S".equalsIgnoreCase(nature))
							{
								if((mvalue + lcTotSampleValue + lcUnconfirmSampleValue + prvSampleValue + lcConfirmBonusValue + lcConfirmPreSampleValue ) > lcFreeValue)
								{
									errcode = "VTSAMPVAL1";
									errList.add(errcode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
				}
						
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
			System.out.println("errCode OF valdatacheckSchemeValue::" + errCode);
		return errCode;
		}
	
	public String getCustSchemeCode(String custCode,String schemeCode,String orderType,Connection conn) throws ITMException
	{
		System.out.println("Inside getCustSchemeCode......");
			boolean lbProceed = false;
			String noapplyCustList = "",applicableOrdTypes = "" ,applyCustList = "" ,curscheme = "";
			try 
			{
				curscheme = schemeCode;
				String sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
				PreparedStatement pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, curscheme);
				ResultSet rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					applyCustList = rs1.getString("apply_cust_list");
					noapplyCustList = rs1.getString("noapply_cust_list");
					applicableOrdTypes = rs1.getString("order_type");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				if("NE".equalsIgnoreCase(orderType) && (applicableOrdTypes == null || applicableOrdTypes.trim().length() == 0))
				{
					//continue;
				}
				else if(applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
				{

					lbProceed = false ;
					String lsApplicableOrdTypesArr[]= applicableOrdTypes.split(",");
					for(int i=0;i<lsApplicableOrdTypesArr.length;i++)
					{
						System.out.println("lsApplicableOrdTypesArr[i]"+lsApplicableOrdTypesArr[i]);
						if(orderType.equalsIgnoreCase(lsApplicableOrdTypesArr[i]))
						{
							lbProceed=true;
							break;
						}
					}
				
				}
				
				if(applyCustList.trim().length() > 0)
				{
					//custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						String applyCustListArr[]=applyCustList.split(",");
						for(int i=0;i<applyCustListArr.length;i++)
						{
							System.out.println("applyCustListArr[i]"+applyCustListArr[i]);
							if(applyCustListArr[i].equalsIgnoreCase(custCode))
							{
								schemeCode = curscheme;
								break;
							}
						}
				}
	
				if(noapplyCustList.trim().length() > 0 && schemeCode != null)
				{
					if(noapplyCustList.trim().length() > 0 && schemeCode != null)
					{
						//custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						String noapplyCustListArr[]=noapplyCustList.split(",");
						for(int i=0; i < noapplyCustListArr.length;i++)
						{
							System.out.println("noapplyCustListArr[i]" + noapplyCustListArr[i]);
							if(noapplyCustListArr[i].equalsIgnoreCase(custCode))
							{
								schemeCode = "";
								break;
							}
						}
					}
				}
				
				
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
			System.out.println("errCode OF getCustCodeScheme::" + schemeCode);
			return schemeCode;

		}


}
