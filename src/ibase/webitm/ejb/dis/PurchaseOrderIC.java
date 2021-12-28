/********************************************************
	Title : PurchaseOrderIC
	Date  : 17/05/2012
	Developer: Mahesh Patidar
 ********************************************************/

package ibase.webitm.ejb.dis; 

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class PurchaseOrderIC extends ValidatorEJB implements PurchaseOrderICLocal, PurchaseOrderICRemote
{
	//changed by nasruddin 05-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = new FinCommon();
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
		int ctr = 0;
		int value1 = 0;
		int qty = 0;
		int qtyStdUom = 0;
		int intQty = 0;
		int frtAmt = 0;
		int relAmt = 0;
		int ordAmt = 0;
		int totAmt = 0;
		int lineNo1 = 0;
		int qtyPer = 0;
		int advAmt = 0;
		int conv = 0;
		int rate = 0;
		int rateStd = 0;
		int minQty = 0;
		int currentFormNo = 0;
		int advPerc = 0;
		int childNodeListLength;
		int quantity = 0;
		int ordQty = 0;
		String transer = "P-ORD";
		String siteCodeDlv = "";
		String amtType = "";
		String siteCodeBil = "";
		String channelPartner = "";
		String errorType = "";
		String uom = "";
		String childNodeName = null;
		String errString = "";
		String qtyStr = "";
		String itemCode = "";
		String acceptCriteria = "";
		String saleOrder = "";
		String formNo = "";
		String opReason = ""; 
		String unit = "";
		String specRef = "";
		String qcRequired = "";
		String specRequired = "";
		String locCode = "";
		String unitStd = "";
		String value = "";
		String cctr = "";
		String unitRate = "";
		String dlvTerm = "";
		String errCode = "";
		String quotNo = "";
		String advPercStr = "";
		String siteCode = "";
		String currCodeIns = "";
		String invAcct = "";
		String transMode = "";
		String invAcctQc = "";
		String dutypaid = "";
		String taxClass = "";
		String deptCode = "";
		String acct = "";
		String empCode = "";
		String suppCode = "";
		String suppCodeMnfr = "";
		String bankCodePay = "";
		String distRoute = "";
		String stanCode = "";
		String salesPers1 = "";
		String salesPers2 = "";
		String itemSer = "";
		String itemSer1 = "";
		String countCodeDlv = "";
		String siteCodeShip = "";
		String custCode = "";
		String custCodeBil = "";
		String custCodeDlv = "";
		String errFldName = "";
		String userId = "";
		String taxChap = "";
		String packCode = "";
		String taxEnv = "";
		String active = "";
		String confirm = "";
		String sql = "";
		String empCodeCon = "";
		String eou = "";
		String tranCode = "";
		String enqNo = "";
		String payMode = "";
		String salesPers = "";
		String currCodeComm = "";
		String currCode = "";
		String currCodeFrt = "";
		String contractNo = "";
		String bomCode = "";
		String pordType = "";
		String termTable = "";
		String workOrder = "";
		String indNo = "";
		String crTerm = "";
		String priceList = "";
		String priceListClg = "";
		String purcCode = "";
		String policyNo = "";
		String stateCode = "";
		String projCode = "";
		String status = "";
		int cnt = 0;
		double qtyBrow = 0;
		double totQty = 0;
		Timestamp date1 = null;
		Timestamp date2 = null;
		Timestamp date3 = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		DistCommon disCommon = new DistCommon();
		FinCommon finCommon = new FinCommon();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();

						if(childNodeName.equalsIgnoreCase("ord_date")) 
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							siteCode = genericUtility.getColumnValue("site_code__ord", dom);
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = this.nfCheckPeriod("PUR", date1, siteCode);
							errCode=finCommon.nfCheckPeriod("PUR", date1, siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("contract_no")) 
						{
							contractNo = genericUtility.getColumnValue("contract_no", dom);
							pordType = genericUtility.getColumnValue("pord_type", dom);
							bomCode = genericUtility.getColumnValue("bom_code", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(contractNo != null && contractNo.trim().length() > 0)
							{
								sql = "select contract_fromdate,contract_todate from pcontract_hdr where contract_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, contractNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									date2 = rs.getTimestamp(1);
									date3 = rs.getTimestamp(2);
								}
								else
								{
									errCode = "VTINCONT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(date1 != null && date2!= null && (date2.after(date1) || date3.before(date1)))
								{
									errCode = "VTVLD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("pord_type")) 
						{
							pordType = genericUtility.getColumnValue("pord_type", dom);
							if(pordType == null || pordType.trim().length() == 0)
							{
								errCode = "VTPOTYBL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("supp_code")) 
						{
							suppCode = genericUtility.getColumnValue("supp_code", dom);
							siteCode = genericUtility.getColumnValue("site_code__ord", dom);
							errCode = finCommon.isSupplier(siteCode, suppCode, transer, conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("site_code__dlv")) 
						{
							siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
							sql = "select count(*) from site where site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(cnt == 0)
							{
								errCode = "VMSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase()); 
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("site_code__ord")) 
						{
							siteCode = genericUtility.getColumnValue("site_code__ord", dom);
							if(siteCode != null && siteCode.trim().length() > 0)
							{
								sql = "select count(*) from site where site_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(cnt == 0)
								{
									errCode = "VMSITE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase()); 
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("site_code__bill")) 
						{
							siteCode = genericUtility.getColumnValue("site_code__bill", dom);
							if(siteCode != null && siteCode.trim().length() > 0)
							{
								sql = "select count(*) from site where site_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(cnt == 0)
								{
									errCode = "VMSITE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase()); 
								}
							}
							else
							{
								errCode = "VMSITECD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("dept_code")) 
						{
							deptCode = genericUtility.getColumnValue("dept_code", dom);
							sql = "select count(*) from DEPARTMENT where dept_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, deptCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(cnt == 0)
							{
								errCode = "VMDEP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase()); 
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("emp_code")) 
						{
							empCode = genericUtility.getColumnValue("emp_code", dom);
							siteCode = genericUtility.getColumnValue("site_code__ord", dom);
							errCode = finCommon.isEmployee(siteCode, empCode, transer, conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if(cnt == 0)
							{
								errCode = "VMDEP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase()); 
							}
							if(errList.size() == 0)
							{
								sql = "select relieve_date from employee where emp_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, empCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									date1 = rs.getTimestamp(1);
									if(date1 == null)
									{
										errCode = "VMEMP2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VMEMP1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;								
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("term_table")) 
						{
							termTable = genericUtility.getColumnValue("term_table", dom);
							sql = "select Count(*) from pur_term_table where term_table = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, termTable);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								errCode = "VTTERM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tran_code")) 
						{
							tranCode = genericUtility.getColumnValue("tran_code", dom);
							if(tranCode != null && tranCode.trim().length() > 0)
							{
								sql = "Select Count(*) from transporter where tran_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, tranCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt == 0)
								{
									errCode = "VMTRAN1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("item_ser")) 
						{
							itemSer = genericUtility.getColumnValue("item_ser", dom);
							if(itemSer != null && itemSer.trim().length() > 0)
							{
								sql = "select count(*) from itemser where item_ser = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(cnt == 0)
								{
									errCode = "VMITEMSER1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase()); 
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("cr_term")) 
						{
							crTerm = genericUtility.getColumnValue("cr_term", dom);
							sql = "select count(*) from crterm where cr_term = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								errCode = "VTCRTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("curr_code")) 
						{
							currCode = genericUtility.getColumnValue("curr_code", dom);
							if(currCode == null || currCode.trim().length() == 0)
							{
								errCode = "VTCRTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = "Select Count(*) from currency where curr_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, currCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTCRTERM1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										suppCode = genericUtility.getColumnValue("supp_code", dom);
										
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select curr_code from supplier where supp_code = ? ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, suppCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											currCodeComm = rs.getString(1)==null?"":rs.getString(1);
										}
										if(! currCodeComm.trim().equals(currCode.trim()))
										{
											errCode = "VTCURR2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tax_chap")) 
						{
							taxChap = genericUtility.getColumnValue("tax_chap", dom);
							if(taxChap != null && taxChap.trim().length() > 0)
							{
								sql = "select count(*) from taxchap where tax_chap = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxChap);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCHAP1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tax_class")) 
						{
							taxClass = genericUtility.getColumnValue("tax_class", dom);
							if(taxClass != null && taxClass.trim().length() > 0)
							{
								sql = "select count(*) from taxclass where tax_class = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxClass);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCLA1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tax_env")) 
						{
							taxEnv = genericUtility.getColumnValue("tax_env", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(taxEnv != null && taxEnv.trim().length() > 0)
							{
								sql = "select count(*) from taxenv where tax_env = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxEnv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXENV1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										errCode = disCommon.getCheckTaxEnvStatus(taxEnv, date1,"P", conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("proj_code")) 
						{
							pordType = genericUtility.getColumnValue("pord_type", dom)==null?"":genericUtility.getColumnValue("pord_type", dom);
							indNo = genericUtility.getColumnValue("ind_no", dom);
							projCode = genericUtility.getColumnValue("proj_code", dom);
							if(projCode == null || projCode.trim().length() == 0)
							{
								if(pordType.trim().equals("P"))
								{
									errCode = "VTPROVI";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else
							{
								sql = "select case when proj_status is null then 'X' else proj_status end,ind_no" +
										" from project where proj_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									status = rs.getString(1);
									String pstatus = rs.getString(2);
									if(! status.equals("A"))
									{
										errCode = "VTPROJ2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(pordType.equals("P"))
									{
										if(! indNo.equals(pstatus))
										{
											errCode = "VINDPJMM";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());		
										}
									}
								}
								else
								{
									errCode = "VTPROJ1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("quot_no")) 
						{
							quotNo = genericUtility.getColumnValue("quot_no", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(quotNo != null && quotNo.trim().length() > 0)
							{
								sql = "select status,valid_upto from pquot_hdr where quot_no = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, quotNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									status = rs.getString(1);
									date2 = rs.getTimestamp(2);
									if(status.trim().toUpperCase().equals("U"))
									{
										errCode = "VTPQUOT1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(date1 != null && date2 != null && date1.after(date2))
									{
										errCode = "VTQDTNV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VTPQUOT2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("sales_pers")) 
						{
							salesPers = genericUtility.getColumnValue("sales_pers", dom);
							siteCode = genericUtility.getColumnValue("site_code__ord", dom);
							if(salesPers != null && salesPers.trim().length() > 0)
							{
								errCode = finCommon.isSalesPerson(siteCode,	salesPers, transer, conn);
								if(errCode != null && errCode.trim().length() > 0)
								{
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("dlv_term")) 
						{
							dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
							if(dlvTerm != null && dlvTerm.trim().length() > 0)
							{
								sql = "select count(*) from delivery_term where dlv_term = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, dlvTerm);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTDLV1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("curr_code__frt")) 
						{
							currCodeFrt = genericUtility.getColumnValue("curr_code__frt", dom);
							advPercStr = genericUtility.getColumnValue("frt_amt", dom);
							try 
							{
								frtAmt = Integer.parseInt(advPercStr);
							} 
							catch (Exception e) 
							{
								System.out.println("Error line 561");
							}
							if((currCodeFrt != null && currCodeFrt.trim().length() > 0) || frtAmt > 0)
							{
								sql = "Select Count(*) from currency where curr_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, currCodeFrt);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("curr_code__ins")) 
						{
							currCodeIns = genericUtility.getColumnValue("curr_code__ins", dom);
							advPercStr = genericUtility.getColumnValue("insurance_amt", dom);
							try 
							{
								frtAmt = Integer.parseInt(advPercStr);
							} 
							catch (Exception e) 
							{
								System.out.println("Error line 561");
							}
							if((currCodeIns != null && currCodeIns.trim().length() > 0) || frtAmt > 0)
							{
								sql = "Select Count(*) from currency where curr_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, currCodeIns);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("sale_order")) 
						{
							saleOrder = genericUtility.getColumnValue("sale_order", dom);
							if(saleOrder != null && saleOrder.trim().length() > 0)
							{
								sql = "select confirmed,status from sorder where sale_order = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, empCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									confirm = rs.getString(1);
									status = rs.getString(2);
									if(confirm.equals("N"))
									{
										errCode = "VTSORD2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									if(status.equals("X") || status.equals("C"))
									{
										errCode = "VTSORDCX";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VTSORD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("ind_no")) 
						{
							indNo = genericUtility.getColumnValue("ind_no", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							pordType = genericUtility.getColumnValue("pord_type", dom);
							if(indNo != null && indNo.trim().length() > 0)
							{
								sql = "Select item_code, status,quantity,ord_qty,site_code__dlv," +
										" site_code__bil,ind_date from indent where ind_no = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, indNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									itemCode = rs.getString(1)==null?"":rs.getString(1);
									status = rs.getString(2)==null?"":rs.getString(2);
									quantity = rs.getInt(3);
									ordQty = rs.getInt(4);
									siteCodeDlv = rs.getString(5)==null?"":rs.getString(5);
									siteCodeBil = rs.getString(6)==null?"":rs.getString(6);
									date2 = rs.getTimestamp(7);
								}
								else
								{
									errCode = "VTINDENT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(status.equals("U"))
								{
									errCode = "VTINDENT3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(status.equals("L"))
								{
									tranCode = genericUtility.getColumnValue("provi_tran_id", dom).trim();
									if(! pordType.equals("P"))
									{
										if(tranCode == null || tranCode.trim().length() == 0)
										{
											errCode = "VTINDENT4";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								else if(status.equals("C"))
								{
									errCode = "VTINDCL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(date1.before(date2))
								{
									errCode = "VTPOINDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if(errList.size() == 0)
							{
								quotNo = genericUtility.getColumnValue("quot_no", dom);
								if(quotNo != null && quotNo.trim().length() > 0)
								{
									sql = "select enq_no from pquot_hdr where quot_no = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, quotNo);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										enqNo = rs.getString(1);
										if(enqNo != null && enqNo.trim().length() > 0 )
										{
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											sql = "select status from enq_det where enq_no = ? and ind_no = ?";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, enqNo);
											pstmt.setString(2, indNo);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												status = rs.getString(1);
												if(status.equals("O"))
												{
													errCode = "VTINDENT5";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							if(errList.size() == 0)
							{
								itemSer = genericUtility.getColumnValue("item_ser", dom);
								sql = "select item_ser from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									itemSer1 = rs.getString(1)==null?"":rs.getString(1);
									if(! itemSer1.equals(itemSer))
									{
										errCode = "VTITEMSER";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(errList == null || errList.size() == 0)
							{
								sql = "select indent_opt from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									objContext = rs.getString(1)==null?"":rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(objContext.equals("M"))
								{
									if(indNo == null || indNo.trim().length() == 0)
									{
										errCode = "VTINDNO";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else if(objContext.equals("N"))
								{
									if(indNo != null && indNo.trim().length() > 0)
									{
										errCode = "VTINDRQ";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("accept_criteria")) 
						{
							acceptCriteria = genericUtility.getColumnValue("accept_criteria", dom);
							if(acceptCriteria == null || acceptCriteria.trim().length() == 0)
							{
								errCode = "VTNULCRT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("policy_no")) 
						{
							policyNo = genericUtility.getColumnValue("policy_no", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(policyNo != null && policyNo.trim().length() > 0)
							{
								sql = "select count(*) from insurance where policy_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, policyNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTPOLI1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(errList == null || errList.size() == 0)
								{
									sql = "select status 	, from_date , valid_upto from insurance where policy_no = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, policyNo);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										status = rs.getString(1)==null?"":rs.getString(1);
										date2 = rs.getTimestamp(2);
										date3 = rs.getTimestamp(3);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(status.equals("C") || status.equals("X"))
									{
										errCode = "VTCX";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(date1.before(date2) || date1.after(date3))
									{
										errCode = "VTPOLEXP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("bank_code__pay")) 
						{
							bankCodePay = genericUtility.getColumnValue("bank_code__pay", dom);
							suppCode = genericUtility.getColumnValue("supp_code", dom);
							if(bankCodePay != null && bankCodePay.trim().length() > 0 )
							{
								sql = "select pay_mode from supplier where supp_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									payMode = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(payMode == null || payMode.trim().length() == 0 || ! payMode.equals("E"))
								{
									errCode = "VTBENBCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									sql = "select case when confirmed is null then 'N' else confirmed end," +
											" case when active_yn is null then 'Y' else active_yn end " +
											" from supplier_bank where supp_code = ? and bank_code__ben = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, suppCode);
									pstmt.setString(2, bankCodePay);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										confirm = rs.getString(1);
										active = rs.getString(2);
										if(active.equals("N"))
										{
											errCode = "VTBENBCD4";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										else if(confirm.equals("N"))
										{
											errCode = "VTBENBCD5";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									else
									{
										errCode = "VTBENBCD2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						}
					}
					break;
					
				case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("site_code")) 
						{
							siteCode = genericUtility.getColumnValue("site_code", dom);
							siteCodeDlv = genericUtility.getColumnValue("site_code__dlv", dom);
							if(! siteCode.equals(siteCodeDlv))
							{
								errCode = "VTHDSINSME";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if(errList == null || errList.size() == 0)
							{
								sql = "select count(*) from site where site_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(cnt == 0)
								{
									errCode = "VMSITE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase()); 
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("ind_no")) 
						{
							indNo = genericUtility.getColumnValue("ind_no", dom);
							siteCodeBil = genericUtility.getColumnValue("site_code__bill", dom1);
							siteCode = genericUtility.getColumnValue("site_code", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(indNo != null && indNo.trim().length() > 0)
							{
								itemCode = genericUtility.getColumnValue("item_code", dom);
								qtyStr = genericUtility.getColumnValue("quantity", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									quantity = Integer.parseInt(qtyStr.trim());
								}
								sql = "Select item_code, status,quantity,ord_qty,site_code__dlv, " +
										"site_code__bil,ind_date from indent where ind_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, indNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									String itemCode1 = rs.getString(1)==null?"":rs.getString(1);
									status = rs.getString(2)==null?"":rs.getString(2);
									ordQty = rs.getInt(4);
									String siteCodeDlv1 = rs.getString(5)==null?"":rs.getString(5);
									String siteCodeBil1 = rs.getString(6)==null?"":rs.getString(6);
									date2 = rs.getTimestamp(7);
									if(! itemCode.equals(itemCode1))
									{
										errCode = "VTINDENT2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(status.equals("U"))
									{
										errCode = "VTINDENT3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(status.equals("L"))
									{
										errCode = "VTINDENT4";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(status.equals("C"))
									{
										errCode = "VTINDCL";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(date1.before(date2))
									{
										errCode = "VTPOINDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(! siteCodeDlv.trim().equals(siteCodeDlv1.trim()))
									{
										errCode = "VTINDSITE";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(! siteCodeBil.trim().equals(siteCodeBil1.trim()))
									{
										errCode = "VTINDBILL";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VTINDENT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(errList == null || errList.size() == 0)
								{
									quotNo = genericUtility.getColumnValue("quot_no", dom);
									if(quotNo != null && quotNo.trim().length() > 0)
									{
										sql = "select enq_no from pquot_hdr where quot_no = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, quotNo);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											enqNo = rs.getString(1)==null?"":rs.getString(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(enqNo != null && enqNo.trim().length() > 0)
										{
											sql = "select status from enq_det " +
													"where enq_no = ? and ind_no = ?";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, itemCode);
											pstmt.setString(1, itemCode);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												status = rs.getString(1);
												if(status.equals("O"))
												{
													errCode = "VTINDENT5";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
										}
									}
								}
								purcCode = genericUtility.getColumnValue("item_code", dom);
								
								sql = "select quantity__stduom from indent where ind_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									qtyStdUom = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								sql = "select sum(a.quantity__stduom) from porddet a , porder b" +
										" where a.purc_order = b.purc_order and	a.purc_order <> ? " +
										" and b.pord_type <> 'P' and b.status <> 'X' and b.status <> 'C' " +
										" and a.ind_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, purcCode);
								pstmt.setString(1, indNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(cnt >= qtyStdUom )
								{
									errCode = "VTQPO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("proj_code")) 
						{
							projCode = genericUtility.getColumnValue("proj_code", dom);
							indNo = genericUtility.getColumnValue("ind_no", dom);
							if(projCode != null && projCode.trim().length() > 0)
							{
								sql = "select case when proj_status is null then 'X' else proj_status end ," +
										" ind_no from project where proj_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									status = rs.getString(1);
									String indNo1 = rs.getString(2)==null?"":rs.getString(2);
									if(! status.equals("A"))
									{
										errCode = "VTPROJ2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(indNo1 != null && indNo1.trim().length() > 0 )
									{
										if(! indNo.trim().equals(indNo1.trim()))
										{
											errCode = "VINDPJMM";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								else
								{
									errCode = "VTPROJ1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("item_code")) 
						{
							itemCode = genericUtility.getColumnValue("item_code", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom);
							itemSer = genericUtility.getColumnValue("item_ser", dom);
							suppCode = genericUtility.getColumnValue("supp_code", dom);
							indNo = genericUtility.getColumnValue("ind_no", dom);
							String tranDate = genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							
							sql = "Select case when stop_business is null then 'N' else stop_business end " +
									"From   Item Where  item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								active = rs.getString(1);
								if(active.equals("N"))
								{
									errCode = this.checkItem(siteCode, itemCode, conn);
									if(errCode != null && errCode.trim().length() > 0)
									{
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VTIIC";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(errList == null || errList.size() == 0)
							{
								sql = "select channel_partner,site_code__ch " +
										"from site_supplier where site_code = ?	and supp_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, suppCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									channelPartner = rs.getString(1);
									siteCodeDlv = rs.getString(2);
								}
								else if((! rs.next()) || channelPartner == null || channelPartner.trim().length() ==0)
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "select channel_partner,site_code from supplier where supp_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, suppCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										channelPartner = rs.getString(1);
										siteCodeDlv = rs.getString(2);
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(errList == null || errList.size() == 0)
							{
								if(channelPartner.trim().toUpperCase().equals("Y"))
								{
									siteCode = siteCodeDlv;
								}
								else 
								{
									itemSer = this.getItemSeries(itemCode, siteCode, tranDate, suppCode, 'S');
									errCode = disCommon.getToken(itemSer, "~t");
									if(errCode != null && errCode.trim().length() > 0)
									{
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							if(errList == null || errList.size() == 0)
							{
								sql = "select oth_series from itemser where item_ser = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									status = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if((! itemSer.equals(itemSer1)) && status.equals("N"))
								{
									errCode = "VTITEM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if(errList == null || errList.size() == 0)
							{
								quotNo = genericUtility.getColumnValue("quot_no", dom1);
								itemCode = genericUtility.getColumnValue("item_code", dom);
								if(quotNo != null && quotNo.trim().length() > 0)
								{
									sql = "select status from pquot_det where quot_no = ? and item_code = ? and status='A'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, quotNo);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										status = rs.getString(1);
									}
									else
									{
										errCode = "VTNOTITMQT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							if(errList == null || errList.size() == 0)
							{
								tranCode = genericUtility.getColumnValue("provi_tran_id", dom1);
								if(tranCode != null && tranCode.trim().length() > 0)
								{
									itemCode = genericUtility.getColumnValue("item_code", dom);
									sql = "select count(*) from porder a,porddet b " +
											" where a.purc_order = b.purc_order	and	a.provi_tran_id = ? " +
											" and a.purc_order <> ?	and	b.item_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, tranCode);
									pstmt.setString(2, purcCode);
									pstmt.setString(3, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
										if(cnt > 0)
										{
											errCode = "VTSTDPOCR";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							if(errList == null || errList.size() == 0)
							{
								sql = "select indent_opt from item where item_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									String indentOpt = rs.getString(1);
									if(indentOpt.equals("M"))
									{
										if(indNo == null || indNo.trim().length() == 0)
										{
											errCode = "VTINDNO";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									else if(indentOpt.equals("N"))
									{
										if(indNo != null && indNo.trim().length() > 0)
										{
											errCode = "VTINDRQ";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(errList == null || errList.size() == 0)
							{
								sql = "select quot_opt from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									String quotOpt = rs.getString(1);
									if(quotOpt.equals("M"))
									{
										if(quotNo == null || quotNo.trim().length() == 0)
										{
											errCode = "VTQUOTM";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("quantity")) 
						{
							qtyStr = genericUtility.getColumnValue("quantity", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								quantity = Integer.parseInt(qtyStr.trim());
							}	
							quotNo = genericUtility.getColumnValue("quot_no", dom1);
							indNo = genericUtility.getColumnValue("ind_no", dom1);
							if(quantity <= 0)
							{
								errCode = "VTQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								qtyStr = genericUtility.getColumnValue("quantity__stduom", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									quantity = Integer.parseInt(qtyStr.trim());
								}	
								purcCode = genericUtility.getColumnValue("purc_order", dom);
								String lineNo =  genericUtility.getColumnValue("line_no", dom);
								if(indNo != null && indNo.trim().length() > 0)
								{
									if(isReasonRequired(purcCode , lineNo , indNo, quantity, conn))
									{
										errCode = "VTPIQTY1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								if(errList == null || errList.size() == 0)
								{
									itemCode = genericUtility.getColumnValue("item_code", dom);
									sql = "select sum(quantity) from pquot_det " +
											" where quot_no = ? and item_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, quotNo);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
										if(quantity > cnt)
										{
											errCode = "VTPQQTY1";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								qtyStr = genericUtility.getColumnValue("quantity", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									quantity = Integer.parseInt(qtyStr.trim());
								}
								qtyStr = genericUtility.getColumnValue("quantity__stduom", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									qtyStdUom = Integer.parseInt(qtyStr.trim());
								}
								qtyStr = genericUtility.getColumnValue("rate", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									rate = Integer.parseInt(qtyStr.trim());
								}
								qtyStr = genericUtility.getColumnValue("rate__stduom", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									rateStd = Integer.parseInt(qtyStr.trim());
								}
								uom = genericUtility.getColumnValue("unit", dom).trim();
								unitRate = genericUtility.getColumnValue("unit__rate", dom).trim();
								value = this.getEnvDis("999999", "RCP_UOM_VARIANCE", conn);
								if(value != null && value.trim().length() > 0)
								{
									value1 = Integer.parseInt(qtyStr.trim());
								}
								if(unitRate.trim().equals(uom.trim()))
								{
									if(Math.abs((quantity * rate)- (qtyStdUom * rateStd)) > value1)
									{
										errCode = "VTCONV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("unit")) 
						{
							uom = genericUtility.getColumnValue("unit", dom).trim();
							unitStd = genericUtility.getColumnValue("unit__std", dom).trim();
							
							sql = "Select Count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, uom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "VTUNIT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(! uom.equals(unitStd))
								{
									qtyStr = genericUtility.getColumnValue("conv__qty_stduom", dom);
									if(qtyStr != null && qtyStr.trim().length() > 0)
									{
										conv = Integer.parseInt(qtyStr.trim());
									}
									if(conv == 0)
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ? ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, uom);
										pstmt.setString(2, unitStd);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt(1);
											if(cnt == 0)
											{
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												
												sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ? ";
												pstmt =  conn.prepareStatement(sql);
												pstmt.setString(1, unitStd);
												pstmt.setString(2, uom);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													cnt = rs.getInt(1);
													if(cnt == 0)
													{
														errCode = "VTUNIT3";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
												}
											}
										}
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;														
						}
						
						else if(childNodeName.equalsIgnoreCase("unit__rate")) 
						{
							unitRate = genericUtility.getColumnValue("unit__rate", dom).trim();
							unitStd = genericUtility.getColumnValue("unit__std", dom).trim();
							sql = "Select Count(*) from uom where unit = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, unitRate);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "VTUNIT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if (! unitRate.equals(unitStd))
								{
									qtyStr = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
									if(qtyStr != null && qtyStr.trim().length() > 0)
									{
										conv = Integer.parseInt(qtyStr.trim());
									}
									if(conv == 0)
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ? ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, unitRate);
										pstmt.setString(2, unitStd);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt(1);
											if(cnt == 0)
											{
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												
												sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ? ";
												pstmt =  conn.prepareStatement(sql);
												pstmt.setString(1, unitStd);
												pstmt.setString(2, unitRate);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													cnt = rs.getInt(1);
													if(cnt == 0)
													{
														errCode = "VTUNIT3";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
												}
											}
										}
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("rate")) 
						{
							qtyStr = genericUtility.getColumnValue("rate", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								rate = Integer.parseInt(qtyStr.trim());
							}
							itemCode = genericUtility.getColumnValue("item_code", dom);
							String dateStr = genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) ;
							priceList = genericUtility.getColumnValue("price_list", dom);
							qtyStr = genericUtility.getColumnValue("quantity__stduom", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								qtyStdUom = Integer.parseInt(qtyStr.trim());
							}
							if(rate > 0)
							{
								indNo = genericUtility.getColumnValue("ind_no", dom);
								if(indNo != null && indNo.trim().length() > 0)
								{
									sql = "select case when purc_rate is null then 0 else purc_rate end ," +
											" case when max_rate is null then 0 else max_rate end " +
											" from indent where ind_no = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, indNo);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										int purcRate = rs.getInt(1);
										int maxRate = rs.getInt(2);
										if(maxRate != 0)
										{
											if(rate > maxRate || rate < purcRate)
											{
												errCode = "VNOTBETIND";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							if(errList == null || errList.size() == 0)
							{
								if(priceList == null || priceList.trim().length() == 0)
								{
									sql = "select var_value from disparm " +
											" where prd_code = '999999' and var_name = 'REGULATED_PRICE_LIST'";
									pstmt =  conn.prepareStatement(sql);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										priceList = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								if(priceList != null && priceList.trim().length() > 0)
								{
									double rate1 =  disCommon.pickRate(priceList, dateStr, itemCode, "", "L", qtyStdUom, conn);
									if(rate > rate1 && rate1 > 0)
									{
										errCode = "VTDPCORT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								String varValue = disCommon.getDisparams("999999", "UOM_ROUND", conn);
								if(varValue.equals("NULLFOUND"))
								{
									errCode = "VTUOMVARPARM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								try
								{
									rate = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
								}
								catch(Exception e1)
								{
									System.out.println("Error at 2017");
								}
								try
								{
									qtyStdUom = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
								}
								catch(Exception e1)
								{
									System.out.println("Error at 2025");
								}
								unitRate = genericUtility.getColumnValue("rate", dom);
								unitStd = genericUtility.getColumnValue("rate", dom);
								itemCode = genericUtility.getColumnValue("rate", dom);
								siteCode = genericUtility.getColumnValue("rate", dom);
								date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("rate", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
								if(unitRate == null || unitRate.trim().length() == 0)
								{
									sql = "Select unit from item where item_code = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										unitRate = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									int rateStdUom = 0;
									if(varValue != null && (varValue.equals("R") || varValue.equals("B")))
									{
										//lc_ratestduom = gf_conv_qty_fact1(ls_unitstd, ls_unitrate, ls_itemcode, lc_rate, lc_convrtuomstduom,'Y')
									}
									else
									{
										//lc_ratestduom = gf_conv_qty_fact1(ls_unitstd, ls_unitrate, ls_itemcode, lc_rate, lc_convrtuomstduom,'N')
									}
									sql = "select porddet.purc_order, porder.ord_date, porddet.rate__stduom  from porddet, porder " +
											" where ( porddet.purc_order = porder.purc_order ) and ( ( porddet.site_code = ? ) and " +
											" ( porddet.item_code = ? ) and ( porder.ord_date < ? ) ) order by porder.ord_date desc";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, siteCode);
									pstmt.setString(2, itemCode);
									pstmt.setTimestamp(3, date1);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										pordType = rs.getString(1);
										date2 = rs.getTimestamp(2);
										rateStd = rs.getInt(3);
										if(rateStd < rateStdUom)
										{
											errCode = "VTPORATE1";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("conv__qty_stduom")) 
						{
							qtyStr = genericUtility.getColumnValue("conv__qty_stduom", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								conv = Integer.parseInt(qtyStr.trim());
							}
							uom = genericUtility.getColumnValue("unit", dom).trim();
							unitStd = genericUtility.getColumnValue("unit__std", dom).trim();
							if(uom.equals(unitStd) && conv != 1)
							{
								errCode = "VTUCON1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(! uom.equals(unitStd))
							{
								itemCode = genericUtility.getColumnValue("item_code", dom);
								//errcode = gf_check_conv_fact(ls_item_code, mval, mval1, lc_convqtystduom)
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("conv__rtuom_stduom")) 
						{
							qtyStr = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								conv = Integer.parseInt(qtyStr.trim());
							}
							unitRate = genericUtility.getColumnValue("unit__rate", dom).trim();
							unitStd = genericUtility.getColumnValue("unit__std", dom).trim();
							if(unitRate.equals(unitStd) && conv != 1)
							{
								errCode = "VTUCON1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(! unitRate.equals(unitStd))
							{
								itemCode = genericUtility.getColumnValue("item_code", dom);
								//errcode = gf_check_conv_fact(ls_item_code, mval, mval1, lc_convqtystduom)
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("loc_code")) 
						{
							locCode = genericUtility.getColumnValue("loc_code", dom);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "Select Count(*) from location where loc_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "VTLOC1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("work_order")) 
						{
							pordType = genericUtility.getColumnValue("pord_type", dom);
							if(pordType.trim().equals("O"))
							{
								workOrder = genericUtility.getColumnValue("work_order", dom);
								if(workOrder != null && workOrder.trim().length() > 0)
								{
									sql = "select status from workorder where work_order = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, workOrder);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										status = rs.getString(1);
										if(status.equals("C") || status.equals("X"))
										{
											errCode = "VTWORDER2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									else
									{
										errCode = "VTWORD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("pack_code")) 
						{
							packCode = genericUtility.getColumnValue("pack_code", dom);
							if(packCode != null && packCode.trim().length() > 0)
							{
								sql = "Select Count(*) from packing where pack_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, packCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTPKCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("quantity__stduom")) 
						{
							long qty1 = 0;
							qtyStr = genericUtility.getColumnValue("quantity__stduom", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								qty1 = Integer.parseInt(qtyStr.trim());
							}
							if(qty1 == -999999999)
							{
								errCode = "VTPOQTY3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("rate__stduom")) 
						{
							long qty1 = 0;
							qtyStr = genericUtility.getColumnValue("rate__stduom", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								qty1 = Integer.parseInt(qtyStr.trim());
							}
							if(qty1 == -999999999)
							{
								errCode = "VTPORATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("acct_code__dr")) 
						{
							acct = genericUtility.getColumnValue("acct_code__dr", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom);
							errCode = finCommon.isAcctCode(siteCode, acct, transer, conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 
							if(errList == null || errList.size() == 0)
							{
								errCode = finCommon.isAcctType(acct, "", "0", conn);
								if(errCode != null && errCode.trim().length() > 0)
								{
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("acct_code__cr")) 
						{
							acct = genericUtility.getColumnValue("acct_code__cr", dom).trim();
							siteCode = genericUtility.getColumnValue("site_code", dom).trim();
							errCode = finCommon.isAcctCode(siteCode, acct, transer, conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if(errList == null || errList.size() == 0)
							{
								invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
								if(invAcct != "ERROR")
								{
									if(invAcct == null || invAcct.equals("NULLFOUND") || invAcct.trim().length() == 0)
									{
										invAcct = "N";
									}
									else if(invAcct.equals("Y"))
									{
										errCode = finCommon.isAcctType(acct, "", "0", conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									else
									{
										errCode = finCommon.isAcctType(acct, siteCode, "S", conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("cctr_code__dr")) 
						{
							cctr = genericUtility.getColumnValue("cctr_code__dr", dom);
							acct = genericUtility.getColumnValue("acct_code__dr", dom);
							errCode = finCommon.isCctrCode(acct, cctr, transer, conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("cctr_code__cr")) 
						{
							cctr = genericUtility.getColumnValue("cctr_code__cr", dom);
							acct = genericUtility.getColumnValue("acct_code__cr", dom);
							errCode = finCommon.isCctrCode(acct, cctr, transer, conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("dept_code")) 
						{
							deptCode = genericUtility.getColumnValue("dept_code", dom);
							acct = genericUtility.getColumnValue("acct_code__dr", dom);
							if(deptCode != null && deptCode.trim().length() > 0)
							{
								sql = "select count(1) from department where dept_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, deptCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(cnt == 0)
								{
									errCode = "VMDEPT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									String acctSpec = finCommon.getFinparams("999999", "ACCT_SPECIFIC_DEPT", conn);
									if(acctSpec.trim().equalsIgnoreCase("Y"))
									{
										sql = "select count(1) from accounts_dept where acct_code = ? and dept_code = ? ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, acct);
										pstmt.setString(2, deptCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										if(cnt == 0)
										{
											sql = "select count(1) from accounts_dept where acct_code = ?";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, acct);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												cnt = rs.getInt(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											if(cnt == 0)
											{
												errCode = "VMDEPT2";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
											else if(cnt > 0)
											{
												errCode = "VMDEPT2";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("supp_code__mnfr")) 
						{
							suppCodeMnfr = genericUtility.getColumnValue("supp_code__mnfr", dom);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							suppCode = genericUtility.getColumnValue("supp_code", dom);
							siteCode = genericUtility.getColumnValue("site_code__ord", dom);
							if(suppCodeMnfr != null && suppCodeMnfr.trim().length() > 0)
							{
								sql = "select count(1) from itemmnfr " +
										"where item_code = ? and supp_code__mnfr = ? and supp_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, suppCodeMnfr);
								pstmt.setString(3, suppCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTINVMNFR1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(errList == null || errList.size() == 0)
							{
								sql = "select count(1) from itemmnfr a,itemregno b	where a.item_code = b.item_code" +
										" and b.site_code = ? and a.item_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt > 0 && (suppCodeMnfr == null || suppCodeMnfr.trim().length() == 0))
									{
										errCode = "VTSUPPMNFR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("req_date")) 
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("req_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date3 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("dlv_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(date1.before(date2) || date1.before(date3))
							{
								errCode = "VTPOREQDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("dlv_date")) 
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("dlv_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							contractNo = genericUtility.getColumnValue("contract_no", dom);
							pordType = genericUtility.getColumnValue("pord_type", dom);
							if(! pordType.equals("P"))
							{
								if(date2.before(date1))
								{
									errCode = "VTPODLVDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if(errList == null || errList.size() == 0)
							{
								if(contractNo != null && contractNo.trim().length() > 0)
								{
									sql = "select contract_fromdate,contract_todate from pcontract_hdr" +
											" where contract_no = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, contractNo);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										date1 = rs.getTimestamp(1);
										date3 = rs.getTimestamp(1);
										if(date2.before(date1) || date2.after(date3))
										{
											errCode = "VTCONVAL";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("bom_code")) 
						{
							pordType = genericUtility.getColumnValue("pord_type", dom1);
							if(! pordType.trim().equals("O"))
							{
								bomCode = genericUtility.getColumnValue("bom_code", dom);
								if((pordType.trim().equals("C") || pordType.trim().equals("J")) && (bomCode == null || bomCode.trim().length() == 0))
								{
									errCode = "VTBOMCJ";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if((pordType.trim().equals("C") || pordType.trim().equals("J")) && (bomCode != null && bomCode.trim().length() > 0))
								{
									sql = "select count(*) from bom where bom_code = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, bomCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
										if(cnt == 0)
										{
											errCode = "VMBOM1";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								else
								{
									errCode = "VTBOMNCJ";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("item_code")) 
						{
							qtyStr = genericUtility.getColumnValue("rate__stduom", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								qty = Integer.parseInt(qtyStr.trim());
							}
							if(qty > 100)
							{
								errCode = "VTDISC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("op_reason")) 
						{
							opReason = genericUtility.getColumnValue("op_reason", dom);
							if(opReason == null || opReason.trim().length() == 0)
							{
								pordType = genericUtility.getColumnValue("purc_order", dom1);
								indNo = genericUtility.getColumnValue("ind_no", dom);
								String lineNo = genericUtility.getColumnValue("line_no", dom);
								try
								{
									quantity = Integer.parseInt(genericUtility.getColumnValue("quantity__stduom", dom));
								}
								catch(Exception e1)
								{
									System.out.println("Error at 2017");
								}
								
								if(isReasonRequired(pordType, lineNo, indNo, quantity, conn));
								{
									errCode = "VTPORESN2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tax_chap")) 
						{
							taxChap = genericUtility.getColumnValue("tax_chap", dom);
							if(taxChap != null && taxChap.trim().length() > 0)
							{
								sql = "select count(*) from taxchap where tax_chap = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxChap);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCHAP1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tax_class")) 
						{
							taxClass = genericUtility.getColumnValue("tax_class", dom);
							if(taxClass != null && taxClass.trim().length() > 0)
							{
								sql = "select count(*) from taxclass where tax_class = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxClass);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCLA1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("tax_env")) 
						{
							taxEnv = genericUtility.getColumnValue("tax_env", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(taxEnv != null && taxEnv.trim().length() > 0)
							{
								sql = "select count(*) from taxenv where tax_env = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxEnv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXENV1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										errCode = disCommon.getCheckTaxEnvStatus(taxEnv, date1, "P", conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("emp_code__qcaprv")) 
						{
							empCode = genericUtility.getColumnValue("emp_code__qcaprv", dom);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "select qc_reqd from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								status = rs.getString(1);
								if(status.equals("Y"))
								{
									errCode = finCommon.isEmployee("", empCode, "", conn);
									if(errCode != null && errCode.trim().length() > 0)
									{
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								if(errList == null || errList.size() == 0)
								{
									errCode = "VERREMPL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("item_code__mfg"))
						{
							itemCode = genericUtility.getColumnValue("item_code__mfg", dom);
							if(itemCode != null && itemCode.trim().length() > 0)
							{
								sql = "select count(*) from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VMITEM1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("spec_ref")) 
						{
							specRef = genericUtility.getColumnValue("spec_ref", dom);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							siteCode = genericUtility.getColumnValue("site_code__dlv", dom1);
							//ls_specreqd = gf_spec_reqd(ls_sitecode,ls_itemcode)
							//ls_qcreqd = gf_qc_reqd(ls_sitecode,ls_itemcode)
							if(qcRequired.equals("Y"))
							{
								if(specRequired.equals("Y"))
								{
									if(specRef == null || specRef.trim().length() == 0)
									{
										errCode = "VTSPEC";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										sql = "select count(1) from qcitem_spec_det where item_code = ?	and spec_ref = ? ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, itemCode);
										pstmt.setString(1, specRef);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt(1);
											if(cnt == 0)
											{
												errCode = "VTINVSPEC";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("acct_code__prov_dr")) 
						{
							acct = genericUtility.getColumnValue("acct_code__prov_dr", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom);
							invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
							if(! invAcct.equals("ERROR"))
							{
								if(invAcct == null || invAcct.trim().length() == 0 || invAcct.equals("NULLFOUND"))
								{
									invAcct = "N";
								}
								invAcct = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);
								if(invAcctQc.equals("ERROR"))
								{
									if(invAcctQc == null || invAcctQc.trim().length() == 0 || invAcctQc.equals("NULLFOUND"))
									{
										invAcctQc = "N";
									}
									if(invAcct.equals("Y") && invAcctQc.equals("Y"))
									{
										errCode = finCommon.isAcctCode(acct, siteCode, transer, conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										if(errList == null || errList.size() == 0)
										{
											errCode = finCommon.isAcctType(acct, "", "0", conn);
											if(errCode != null && errCode.trim().length() > 0)
											{
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
										else
										{
											errCode = "VTPROVACCT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("cctr_code__prov_dr")) 
						{
							cctr = genericUtility.getColumnValue("cctr_code__prov_dr", dom);
							acct = genericUtility.getColumnValue("acct_code__prov_dr", dom);
							invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
							if(! invAcct.equals("ERROR"))
							{
								if(invAcct == null || invAcct.trim().length() == 0 || invAcct.equals("NULLFOUND"))
								{
									invAcct = "N";
								}
								invAcct = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);
								if(invAcctQc.equals("ERROR"))
								{
									if(invAcctQc == null || invAcctQc.trim().length() == 0 || invAcctQc.equals("NULLFOUND"))
									{
										invAcctQc = "N";
									}
									if(invAcct.equals("Y") && invAcctQc.equals("Y"))
									{
										errCode = finCommon.isCctrCode(acct, cctr, transer, conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("cctr_code__prov_cr")) 
						{
							cctr = genericUtility.getColumnValue("cctr_code__prov_cr", dom);
							acct = genericUtility.getColumnValue("acct_code__prov_cr", dom);
							invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
							if(! invAcct.equals("ERROR"))
							{
								if(invAcct == null || invAcct.trim().length() == 0 || invAcct.equals("NULLFOUND"))
								{
									invAcct = "N";
								}
								invAcct = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);
								if(invAcctQc.equals("ERROR"))
								{
									if(invAcctQc == null || invAcctQc.trim().length() == 0 || invAcctQc.equals("NULLFOUND"))
									{
										invAcctQc = "N";
									}
									if(invAcct.equals("Y") && invAcctQc.equals("Y"))
									{
										errCode = finCommon.isCctrCode(acct, cctr, transer, conn);
										if(errCode != null && errCode.trim().length() > 0)
										{
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("prd_code__rfc")) 
						{
							pordType = genericUtility.getColumnValue("prd_code__rfc", dom);
							if(pordType != null && pordType.trim().length() > 0)
							{
								sql = "Select count(1) from period where code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, pordType);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTRFCDATE";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										/*if mVal < string(today(), 'yyyymm') then
											errcode = "VTRFCDATE2"
										end if*/
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("duty_paid")) 
						{
							dutypaid = genericUtility.getColumnValue("duty_paid", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom);
							sql = "Select case when eou is null then 'N' else eou end " +
									" From site Where site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								eou = rs.getString(1);
								if(eou.equals("Y"))
								{
									if(dutypaid == null || dutypaid.trim().length() > 0)
									{
										errCode = "VTDUTYBK";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(dutypaid.equals("N"))
									{
										itemCode = genericUtility.getColumnValue("item_code", dom);
										date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;

										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "Select b.lop_reqd From item a, itemser b" +
												" Where a.item_ser = b.item_ser And a.item_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											specRequired = rs.getString(1);
											if(specRequired.equals("Y"))
											{
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												
												sql = "Select count(1) From lop_hdr a, lop_det b " +
														"Where a.lop_ref_no = b.lop_ref_no And " +
														" a.site_code = ? And a.confirmed = 'Y' And " +
														" b.item_code = ? And b.item_status ='A' And " +
														" ? >= a.valid_from And ? <= a.valid_to And " +
														" b.buy_sell_flag In ('P','B')";
												pstmt =  conn.prepareStatement(sql);
												pstmt.setString(1, siteCode);
												pstmt.setString(1, itemCode);
												pstmt.setTimestamp(1, date1);
												pstmt.setTimestamp(1, date1);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													cnt = rs.getInt(1);
													if(cnt == 0)
													{
														errCode = "VTLOPITEM1";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
												}
											}
										}
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("form_no")) 
						{
							formNo = genericUtility.getColumnValue("form_no", dom);
							siteCode = genericUtility.getColumnValue("form_no", dom);
							dutypaid = genericUtility.getColumnValue("form_no", dom);
							sql = "Select case when eou is null then 'N' else eou end " +
									" From site Where site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								eou = rs.getString(1);
								if(eou.equals("Y") && dutypaid.equals("N"))
								{
									if(formNo != null && formNo.trim().length() > 0)
									{
										suppCode = genericUtility.getColumnValue("supp_code", dom);
										pordType = genericUtility.getColumnValue("purc_order", dom);
										date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
										String lineNo = genericUtility.getColumnValue("purc_order", dom);
										itemCode = genericUtility.getColumnValue("item_code", dom);
										qtyStr = genericUtility.getColumnValue("quantity", dom);
										if(qtyStr != null && qtyStr.trim().length() > 0)
										{
											qty = Integer.parseInt(qtyStr.trim());
										}
										sql = "select sum(case when b.quantity is null then 0 else b.quantity end) -" +
												" sum(case when b.dlv_qty is null then 0 else b.dlv_qty end)	" +
												"from  porder a, porddet b	where a.purc_order = b.purc_order" +
												"	and 	a.purc_order <> ?	and	b.form_no = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, pordType);
										pstmt.setString(1, formNo);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											quantity = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										for(int count = 1 ; count <= currentFormNo; count++)
										{
											if(genericUtility.getColumnValue("purc_order", dom).trim().equals(pordType.trim()) && genericUtility.getColumnValue("form_no", dom).trim().equals(formNo.trim()) &&  genericUtility.getColumnValue("line_no", dom).trim().equals(lineNo.trim()) )
											{
												try
												{
													qtyBrow = Integer.parseInt(genericUtility.getColumnValue("quantity", dom).trim());
												}
												catch(Exception e1)
												{
													System.out.println("Error at 2017");
												}
												
												totQty = totQty + qtyBrow;
											}
										}
										
										sql = "Select a.status, case when b.quantity is null then 0 else b.quantity end," +
												" case when b.qty_used is null then 0 else b.qty_used end " +
												" From ct3form_hdr a , ct3form_det b Where a.form_no = b.form_no " +
												" And a.form_no = ? And a.site_code = ? And b.supp_code = ? " +
												" And b.item_code = ? And ? >= a.eff_from And ? <= a.valid_upto " +
												" And b.purc_order = ?	And b.line_no = ?	And a.status = 'O' " +
												" And case when a.confirmed is null then 'N' else a.confirmed end = 'Y'";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, formNo);
										pstmt.setString(2, siteCode);
										pstmt.setString(3, suppCode);
										pstmt.setString(4, itemCode);
										pstmt.setTimestamp(5, date1);
										pstmt.setTimestamp(6, date1);
										pstmt.setString(7, pordType);
										pstmt.setString(8, lineNo);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											status = rs.getString(1);
											int qty1 = rs.getInt(2);
											int qtyUsed = rs.getInt(3);
											if(! status.equals("O"))
											{
												errCode = "VTCT3FORM2";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
											else if((quantity + totQty + qty) > (qty1 - qtyUsed))
											{
												errCode = "VTCT3QTY";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
										else
										{
											errCode = "VTCT3FORM1";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(eou.equals("Y") && dutypaid.equals("Y"))
							{
								if(formNo != null && formNo.trim().length() > 0)
								{
									errCode = "VTCT3DUTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					break;

				case 3:
					parentNodeList = dom.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("term_code")) 
						{
							termTable = genericUtility.getColumnValue("term_code", dom1);
							sql = "select count(*) from pur_term where term_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, termTable);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "VTTERM1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					break;

				case 4:
					parentNodeList = dom.getElementsByTagName("Detail4");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("rel_amt")) 
						{
							qtyStr = genericUtility.getColumnValue("rel_amt", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								relAmt = Integer.parseInt(qtyStr.trim());
							}
							if(relAmt <= 0)
							{
								errCode = "VTAMOUNT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							qtyStr = genericUtility.getColumnValue("ord_amt", dom1);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								ordAmt = Integer.parseInt(qtyStr.trim());
							}
							qtyStr = genericUtility.getColumnValue("tot_amt", dom1);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								totAmt = Integer.parseInt(qtyStr.trim());
							}
							qtyStr = genericUtility.getColumnValue("line_no", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								lineNo1 = Integer.parseInt(qtyStr.trim());
							}
							
							for(int count = 1; count <= lineNo1; count++)
							{
								amtType = genericUtility.getColumnValue("amt_type", dom);
								qtyStr = genericUtility.getColumnValue("rel_amt", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									relAmt = Integer.parseInt(qtyStr.trim());
								}
								if(count != lineNo1)
								{
									if(amtType.trim().equals("01"))
									{
										advAmt = ordAmt * ( relAmt / 100 );
									}
									if(amtType.trim().equals("02"))
									{
										advAmt = totAmt * ( relAmt / 100 );
									}
									if(amtType.trim().equals("03"))
									{
										advAmt = relAmt;
									}					
									quantity = quantity + advAmt;
								}
							}
							if(quantity > totAmt)
							{
								errCode = "POADVMIS";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					break;
			}
			int errListSize = errList.size();
			cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
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
			System.out.println("Exception : [ItemRegNo][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		int ctr = 0;
		int currentFormNo = 0;
		int quantity = 0 ;
		double rate = 0;
		double rateClg = 0;
		int pos = 0;
		int totalAmt = 0;
		double exchRate = 0;
		int maxQty = 0;
		int cnt = 0;
		int ordAmt = 0;
		int advPerc = 0;
		int discount = 0;
		int frtAmtFixed = 0;
		int frtAmtQty = 0;
		double qtyStdUom1 = 0;
		double stdRate = 0;
		double rateStdUom1 = 0;
		int commPerc = 0;
		int frtAmt = 0;
		int restDuom = 0;
		int childNodeListLength = 0;
		long rowCount = 0;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String siteCode = "";
		String advType = "";
		String termCode = "";
		String reStr = "";
		String value = "";
		String indNo = "";
		String varValue = "";
		String quotNo = "";
		String amtStr = "";
		String bankCodePay = "";
		String suppCode = "";
		String projCode = "";
		String analCode = "";
		String purcOrder = "";
		String itemSer = "";
		String itemSerInv = "";
		String saleOrder = "";
		String packCode = "";
		String itemCode = "";
		String contractNo = "";
		String priceList = "";
		String custCode = "";
		String toStation = "";
		String fromStation = "";
		String contractDateStr = "";
		String priceListClg = "";
		String crTerm = "";
		String crTerm1 = "";
		String currCode1 = "";
		String frtTerm = "";
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";
		String salesPers = "";
		String discountType = "";
		String cctrCr = "";
		String acctCr = "";
		String commPercOn = "";
		String bomCode = "";
		String contractType = "";
		String emp = "";
		String termTable = "";
		String sql = "";
		String enqNo = "";
		String eou = "";
		String siteCodeDlv = "";
		String siteCodeOrd = "";
		String status = "";
		String currCOdePurc = "";
		String deptCode = "";
		String currCodeFrt = "";
		String empCode = "";
		String siteCodeBil = "";
		String addr1 = "";
		String addr2 = "";
		String city = "";
		String stanCode = "";
		String tranCode = "";
		String transMode = "";
		String dlvTerm = "";
		String currCode = "";
		String frtType = "";
		String crDescr = "";
		String descr = "";
		String policyNo = "";
		String descr1 = "";
		String uom = "";
		String unitStd = "";
		String unitPur = "";
		String locCode = "";
		String unitRate = "";
		String pack = "";
		String payMode = "";
		String bankNameBen = "";
		String bankCodeNoBen = "";
		String pordType = "";
		String date1 = "";
		int pendQty = 0;
		Timestamp reqDate = null;
		double conv = 0;
		String specialInstr = "";
		String specifiedInstr = "";
		String qtyStdUom = "";
		String rateStdUom = "";
		String indRemark = "";
		String qcRequired = "";
		String acct = "";
		String cctr = "";
		String packInstr = "";
		String acctDr = "";
		String invAcct = "";
		String cctrDr = "";
		String invAcctQc = "";
		String mfgItem = "";
		String workOrder = "";
		String empFname = "";
		String empMname = "";
		String empLname = "";
		String suppMnfr = "";
		String locCodeAprv = "";
		String locCodeInsp = "";
		String empCodeAprv = "";
		String itemCode1 = "";
		Timestamp tranDate = null;
		Timestamp ordDate = null;
		Timestamp dspDate = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			DistCommon disCommon = new DistCommon();
			FinCommon finCommon = new FinCommon();
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
				case 1 : 
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail1>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
						sql = "select descr, add1, add2, city, stan_code " +
								" from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
							addr1 = rs.getString(2);
							addr2 = rs.getString(3);
							city = rs.getString(5);
							stanCode = rs.getString(6);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select emp_code from users where code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							empCode = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select dept_code from employee where emp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, empCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							deptCode = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select descr from department where dept_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, deptCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr1 = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "Select Site_code__dlv, Site_code__ord, Status, Curr_code__purc," +
								" Tax_opt, Single_Ser From  PurcCtrl";
						pstmt =  conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteCodeDlv = rs.getString(1);
							siteCodeOrd = rs.getString(2);
							status = rs.getString(3);
							currCOdePurc = rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "Select std_exrt from Currency where Curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							exchRate = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select count(1) gencodes " +
								" WHERE  fld_name = 'PORD_TYPE' and ltrim(rtrim(fld_value)) = 'R'";
						pstmt =  conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(cnt > 0 )
						{
							valueXmlString.append("<pord_type >").append("<![CDATA[R]]>").append("</pord_type>");
						}
						date1 = getCurrdateInAppFormat();
						valueXmlString.append("<dept_code >").append("<![CDATA[" + deptCode + "]]>").append("</dept_code>");
						valueXmlString.append("<department_descr >").append("<![CDATA[" + descr1 + "]]>").append("</department_descr>");
						valueXmlString.append("<Site_code__dlv >").append("<![CDATA[" + siteCode + "]]>").append("</Site_code__dlv>");
						valueXmlString.append("<Site_code__ord >").append("<![CDATA[" + siteCode + "]]>").append("</Site_code__ord>");
						valueXmlString.append("<Site_code__bill >").append("<![CDATA[" + siteCode + "]]>").append("</Site_code__bill>");
						valueXmlString.append("<Curr_code >").append("<![CDATA[" + currCOdePurc + "]]>").append("</Curr_code>");
						valueXmlString.append("<Curr_code__comm >").append("<![CDATA[" + currCOdePurc + "]]>").append("</Curr_code__comm>");
						valueXmlString.append("<Curr_code__frt >").append("<![CDATA[" + currCOdePurc + "]]>").append("</Curr_code__frt>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "Curr_code__frt", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						valueXmlString.append("<Curr_code__ins >").append("<![CDATA[" + currCOdePurc + "]]>").append("</Curr_code__ins>");
						valueXmlString.append("<Tax_Opt >").append("<![CDATA[L]]>").append("</Tax_Opt>");
						valueXmlString.append("<Exch_rate >").append("<![CDATA[" + exchRate + "]]>").append("</Exch_rate>");
						valueXmlString.append("<Ord_date >").append("<![CDATA[" + date1 + "]]>").append("</Ord_date>");
						valueXmlString.append("<Status_date >").append("<![CDATA[" + date1 + "]]>").append("</Status_date>");
						valueXmlString.append("<tax_date >").append("<![CDATA[" + date1 + "]]>").append("</tax_date>");
						valueXmlString.append("<Site_descr >").append("<![CDATA[" + descr + "]]>").append("</Site_descr>");
						valueXmlString.append("<Site_add1 >").append("<![CDATA[" + addr1 + "]]>").append("</Site_add1>");
						valueXmlString.append("<Site_add2 >").append("<![CDATA[" + addr2 + "]]>").append("</Site_add2>");
						valueXmlString.append("<city__site >").append("<![CDATA[" + city + "]]>").append("</city__site>");
						valueXmlString.append("<Site_stan_code >").append("<![CDATA[" + stanCode + "]]>").append("</Site_stan_code>");
						valueXmlString.append("<Curr_code__ins >").append("<![CDATA[" + currCOdePurc + "]]>").append("</Curr_code__ins>");
						valueXmlString.append("<ref_date >").append("<![CDATA[" + date1 + "]]>").append("</ref_date>");
						valueXmlString.append("<emp_code >").append("<![CDATA[" + empCode + "]]>").append("</emp_code>");
						valueXmlString.append("<adv_perc protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("adv_perc", dom)+ "]]>").append("</adv_perc>");
						valueXmlString.append("<ind_no  protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("ind_no", dom) + "]]>").append("</ind_no>");
						valueXmlString.append("<site_code__dlv  protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("site_code__dlv", dom) + "]]>").append("</site_code__dlv>");
						valueXmlString.append("<proj_code  protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("proj_code", dom) + "]]>").append("</proj_code>");
						valueXmlString.append("<adv_perc >").append("<![CDATA[0.000]]>").append("</adv_perc>");
						valueXmlString.append("<bank_code__pay  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("bank_code__pay", dom) + "]]>").append("</bank_code__pay>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
					{
						purcOrder = genericUtility.getColumnValue("purc_order", dom);

						sql = "select count(*) from porddet where purc_order = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(cnt > 0)
						{
							valueXmlString.append("<ind_no  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("ind_no", dom) + "]]>").append("</ind_no>");
							valueXmlString.append("<quot_no  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("quot_no", dom) + "]]>").append("</quot_no>");
							valueXmlString.append("<contract_no  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("contract_no", dom) + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("proj_code", dom) + "]]>").append("</proj_code>");
							valueXmlString.append("<site_code__dlv  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("site_code__dlv", dom) + "]]>").append("</site_code__dlv>");
						}
						else
						{
							indNo = genericUtility.getColumnValue("ind_no", dom);
							quotNo = genericUtility.getColumnValue("quot_no", dom);
							contractNo = genericUtility.getColumnValue("contract_no", dom)==null?"":genericUtility.getColumnValue("contract_no", dom);
							projCode = genericUtility.getColumnValue("proj_code", dom);
							valueXmlString.append("<site_code__dlv  protect=\"0\" >").append("<![CDATA[" + genericUtility.getColumnValue("site_code__dlv", dom) + "]]>").append("</site_code__dlv>");
							
							if(indNo != null && indNo.trim().length() > 0 )
							{
								valueXmlString.append("<ind_no  protect=\"0\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
								valueXmlString.append("<quot_no  protect=\"1\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
								valueXmlString.append("<contract_no  protect=\"1\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
								valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							}
							else if(quotNo != null && quotNo.trim().length() > 0 )
							{
								valueXmlString.append("<ind_no  protect=\"1\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
								valueXmlString.append("<quot_no  protect=\"0\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
								valueXmlString.append("<contract_no  protect=\"1\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
								valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							}
							else if(contractNo != null && contractNo.trim().length() > 0 )
							{
								valueXmlString.append("<ind_no  protect=\"1\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
								valueXmlString.append("<quot_no  protect=\"1\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
								valueXmlString.append("<contract_no  protect=\"0\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
								valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							}
							else if(projCode != null && projCode.trim().length() > 0 )
							{
								valueXmlString.append("<ind_no  protect=\"1\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
								valueXmlString.append("<quot_no  protect=\"1\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
								valueXmlString.append("<contract_no  protect=\"1\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
								valueXmlString.append("<proj_code  protect=\"0\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							}
							else
							{
								valueXmlString.append("<ind_no  protect=\"0\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
								valueXmlString.append("<quot_no  protect=\"0\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
								valueXmlString.append("<contract_no  protect=\"0\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
								valueXmlString.append("<proj_code  protect=\"0\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");								
							}
						}
						valueXmlString.append("<exch_rate  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("exch_rate", dom) + "]]>").append("</exch_rate>");
						valueXmlString.append("<exch_rate__sp  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("exch_rate__sp", dom) + "]]>").append("</exch_rate__sp>");
						valueXmlString.append("<frt_rate  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("frt_rate", dom) + "]]>").append("</frt_rate>");
						
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						sql = "select pay_mode from supplier where supp_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							payMode = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(payMode == null || payMode.trim().length() == 0 || ! payMode.equals("E") || ! payMode.equals("N") || ! payMode.equals("G"))
						{
							valueXmlString.append("<bank_code__pay  protect=\"1\" >").append("<![CDATA[" + genericUtility.getColumnValue("bank_code__pay", dom) + "]]>").append("</bank_code__pay>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("pord_type"))
					{
						pordType = genericUtility.getColumnValue("pord_type", dom);
						if(pordType != null && pordType.trim().equals("A"))
						{
							sql = "select udf_str1 from gencodes where fld_name = 'PORD_TYPE' AND fld_value ='A' ";
							pstmt =  conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								itemSer = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<item_ser >").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
						}
						else
						{
							valueXmlString.append("<item_ser >").append("<![CDATA[]]>").append("</item_ser>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("ind_no"))
					{
						indNo = genericUtility.getColumnValue("ind_no", dom);
						quotNo = genericUtility.getColumnValue("quot_no", dom)==null?"":genericUtility.getColumnValue("quot_no", dom);
						contractNo = genericUtility.getColumnValue("contract_no", dom)==null?"":genericUtility.getColumnValue("contract_no", dom);
						projCode = genericUtility.getColumnValue("proj_code", dom)==null?"":genericUtility.getColumnValue("proj_code", dom);
						
						if(indNo != null && indNo.trim().length() > 0)
						{
							valueXmlString.append("<quot_no  protect=\"1\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
							valueXmlString.append("<contract_no  protect=\"1\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
						
							sql = "select b.item_ser,a.site_code__dlv, a.supp_code__pref, a.emp_code__pur," +
									" a.SITE_CODE__BIL, proj_code, a.anal_code from indent a, item b " +
									" where a.item_code = b.item_code and a.ind_no = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, indNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
								siteCodeDlv = rs.getString(2)==null?"":rs.getString(2);
								suppCode = rs.getString(3)==null?"":rs.getString(3);
								empCode = rs.getString(4)==null?"":rs.getString(4);
								siteCodeBil = rs.getString(5)==null?"":rs.getString(5);
								projCode = rs.getString(6)==null?"":rs.getString(6);
								analCode = rs.getString(7)==null?"":rs.getString(7);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<proj_code  >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							valueXmlString.append("<item_ser  >").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
							valueXmlString.append("<site_code__dlv >").append("<![CDATA[" + siteCodeDlv + "]]>").append("</site_code__dlv>");

							setNodeValue(dom, "site_code__dlv", siteCodeDlv);
							reStr = itemChanged(dom, dom1, dom2, objContext, "site_code__dlv", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
							valueXmlString.append("<anal_code >").append("<![CDATA[" + analCode + "]]>").append("</anal_code>");
							if(suppCode != null && suppCode.trim().length() > 0)
							{
								valueXmlString.append("<supp_code >").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");

								reStr = itemChanged(dom, dom1, dom2, objContext, "supp_code", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail1>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail1>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
							}
							if(empCode != null && empCode.trim().length() > 0)
							{
								valueXmlString.append("<emp_code >").append("<![CDATA[" + empCode + "]]>").append("</emp_code>");
							}
							if(siteCodeBil != null && siteCodeBil.trim().length() > 0)
							{
								valueXmlString.append("<site_code__bill >").append("<![CDATA[" + siteCodeBil + "]]>").append("</site_code__bill>");
							}			
							setNodeValue(dom, "ref_date", date1);
						}
						else
						{
							valueXmlString.append("<proj_code >").append("<![CDATA[]]>").append("</proj_code>");
							valueXmlString.append("<quot_no  protect=\"0\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
							valueXmlString.append("<contract_no  protect=\"0\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code  protect=\"0\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");								
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("item_ser"))
					{
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						empCode = genericUtility.getColumnValue("emp_code", dom);
						if(empCode == null || empCode.trim().length() == 0)
						{
							sql = "select emp_code__pur from itemser where item_ser = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								emp = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(emp != null && emp.trim().length() > 0)
							{
								valueXmlString.append("<emp_code >").append("<![CDATA[" + emp + "]]>").append("</emp_code>");
							}
						}
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						fromStation = genericUtility.getColumnValue("station_stan_code", dom);
						
						sql = "select stan_code from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							toStation = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(editFlag.trim().equals("A"))
						{
							if(taxClass != null && taxClass.trim().length() > 0 )
							{
								taxClass = this.getTaxClass('S', suppCode, "", siteCode);
							}
							if(taxChap != null && taxChap.trim().length() > 0)
							{
								taxChap = this.getTaxChapter("", itemSer, 'S', suppCode, siteCode);
							}
							if(taxEnv != null && taxEnv.trim().length() > 0)
							{
								taxEnv = this.getTaxEnv(fromStation, toStation, taxChap, taxClass, siteCode);
							}
							valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_chap >").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_env >").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						}
						sql = "select term_table from itemser where item_ser = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							termTable = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(termTable != null && termTable.trim().length() > 0)
						{
							valueXmlString.append("<term_table >").append("<![CDATA[" + termTable + "]]>").append("</term_table>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("supp_code"))
					{
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						sql = "Select cr_term, curr_code from site_supplier " +
								" where supp_code = ? and site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							crTerm = rs.getString(1)==null?"":rs.getString(1);
							currCode = rs.getString(2)==null?"":rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select supp_name, addr1, addr2, city, stan_code, tran_code , price_list, " +
								" price_list__clg, cr_term, curr_code from supplier where supp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							addr1 = rs.getString(2)==null?"":rs.getString(2);
							addr2 = rs.getString(3)==null?"":rs.getString(3);
							city = rs.getString(4)==null?"":rs.getString(4);
							stanCode = rs.getString(5)==null?"":rs.getString(5);
							tranCode = rs.getString(6)==null?"":rs.getString(6);
							priceList = rs.getString(7)==null?"":rs.getString(7);
							priceListClg = rs.getString(8)==null?"":rs.getString(8);
							crTerm1 = rs.getString(9)==null?"":rs.getString(9);
							currCode1 = rs.getString(10)==null?"":rs.getString(10);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(crTerm == null || crTerm.trim().length() == 0)
						{
							crTerm = crTerm1;
						}
						if(currCode == null || currCode.trim().length() == 0)
						{
							currCode = currCode1;
						}
						sql = "select descr from crterm where cr_term = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, crTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							crDescr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(tranCode != null && tranCode.trim().length() > 0)
						{
							currCode1 = "";
							sql = "select frt_term, curr_code from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								frtTerm = rs.getString(1)==null?"":rs.getString(1);
								currCode1 = rs.getString(2)==null?"":rs.getString(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<curr_code__frt >").append("<![CDATA[" + currCode1 + "]]>").append("</curr_code__frt>");

							reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__frt", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
							valueXmlString.append("<frt_term >").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");
						}
						else
						{
							valueXmlString.append("<curr_code__frt >").append("<![CDATA[" + currCode + "]]>").append("</curr_code__frt>");

							reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__frt", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
							valueXmlString.append("<frt_term >").append("<![CDATA[B]]>").append("</frt_term>");
						}
						valueXmlString.append("<supp_name >").append("<![CDATA[" + descr + "]]>").append("</supp_name>");
						valueXmlString.append("<supplier_addr1 >").append("<![CDATA[" + addr1 + "]]>").append("</supplier_addr1>");
						valueXmlString.append("<supplier_addr2 >").append("<![CDATA[" + addr2 + "]]>").append("</supplier_addr2>");
						valueXmlString.append("<supplier_city >").append("<![CDATA[" + city + "]]>").append("</supplier_city>");
						valueXmlString.append("<station_stan_code >").append("<![CDATA[" + stanCode + "]]>").append("</station_stan_code>");
						valueXmlString.append("<cr_term >").append("<![CDATA[" + crTerm + "]]>").append("</cr_term>");
						valueXmlString.append("<crterm_descr >").append("<![CDATA[" + crDescr + "]]>").append("</crterm_descr>");
						valueXmlString.append("<tran_code >").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "tran_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						valueXmlString.append("<curr_code >").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						valueXmlString.append("<curr_code__comm >").append("<![CDATA[" + currCode + "]]>").append("</curr_code__comm>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__comm", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						if(genericUtility.getColumnValue("price_list", dom) == null ||genericUtility.getColumnValue("price_list", dom).trim().length() == 0)
						{
							valueXmlString.append("<price_list >").append("<![CDATA[" + priceList + "]]>").append("</price_list>");
						}
						
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						fromStation = genericUtility.getColumnValue("station_stan_code", dom);
						
						sql = "select stan_code from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							toStation = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(editFlag.trim().equals("A"))
						{
							if(taxClass != null && taxClass.trim().length() > 0 )
							{
								taxClass = this.getTaxClass('S', suppCode, "", siteCode);
							}
							if(taxChap != null && taxChap.trim().length() > 0)
							{
								taxChap = this.getTaxChapter("", itemSer, 'S', suppCode, siteCode);
							}
							if(taxEnv != null && taxEnv.trim().length() > 0)
							{
								taxEnv = this.getTaxEnv(fromStation, toStation, taxChap, taxClass, siteCode);
							}
							valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_chap >").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_env >").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						}
						valueXmlString.append("<price_list__clg >").append("<![CDATA[" + priceListClg + "]]>").append("</price_list__clg>");
						sql = "select pay_mode from supplier where supp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							payMode = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(payMode != null && payMode.trim().length() > 0 &&( payMode.equals("E") ||  payMode.equals("N") ||  payMode.equals("G")))
						{
							valueXmlString.append("<bank_code__pay  protect=\"0\" >").append("<![CDATA[" + genericUtility.getColumnValue("bank_code__pay", dom) + "]]>").append("</bank_code__pay>");
						}
						else
						{
							valueXmlString.append("<bank_code__pay >").append("<![CDATA[" + taxEnv + "]]>").append("</bank_code__pay>");
							valueXmlString.append("<bank_name__ben >").append("<![CDATA[" + taxEnv + "]]>").append("</bank_name__ben>");
							valueXmlString.append("<bank_acct_no__ben >").append("<![CDATA[" + taxEnv + "]]>").append("</bank_acct_no__ben>");
							valueXmlString.append("<bank_code__pay  protect=\"1\">").append("<![CDATA[" + genericUtility.getColumnValue("bank_code__pay", dom) + "]]>").append("</bank_code__pay>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("site_code__dlv"))
					{
						siteCodeDlv = genericUtility.getColumnValue("site_code__dlv", dom);
						sql = "select descr, add1, add2, city, stan_code from site where site_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							addr1 = rs.getString(2)==null?"":rs.getString(2);
							addr2 = rs.getString(3)==null?"":rs.getString(3);
							city = rs.getString(4)==null?"":rs.getString(4);
							stanCode = rs.getString(5)==null?"":rs.getString(5);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_descr >").append("<![CDATA[" + descr + "]]>").append("</site_descr>");
						valueXmlString.append("<site_add1 >").append("<![CDATA[" + addr1 + "]]>").append("</site_add1>");
						valueXmlString.append("<site_add2 >").append("<![CDATA[" + addr2 + "]]>").append("</site_add2>");
						valueXmlString.append("<city__site >").append("<![CDATA[" + city + "]]>").append("</city__site>");
						valueXmlString.append("<site_stan_code >").append("<![CDATA[" + stanCode + "]]>").append("</site_stan_code>");
						valueXmlString.append("<site_code__bill >").append("<![CDATA[" + siteCodeDlv + "]]>").append("</site_code__bill>");

						taxClass = genericUtility.getColumnValue("tax_class", dom);
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						fromStation = genericUtility.getColumnValue("station_stan_code", dom);
						
						sql = "select stan_code from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							toStation = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(editFlag.trim().equals("A"))
						{
							if(taxClass != null && taxClass.trim().length() > 0 )
							{
								taxClass = this.getTaxClass('S', suppCode, "", siteCode);
							}
							if(taxChap != null && taxChap.trim().length() > 0)
							{
								taxChap = this.getTaxChapter("", itemSer, 'S', suppCode, siteCode);
							}
							if(taxEnv != null && taxEnv.trim().length() > 0)
							{
								taxEnv = this.getTaxEnv(fromStation, toStation, taxChap, taxClass, siteCode);
							}
							valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_chap >").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_env >").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("dept_code"))
					{
						deptCode = genericUtility.getColumnValue("dept_code", dom);
						sql = "select descr from department where dept_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, deptCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<department_descr >").append("<![CDATA[" + descr + "]]>").append("</department_descr>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("cr_term"))
					{
						deptCode = genericUtility.getColumnValue("cr_term", dom);
						sql = "select descr from crterm where cr_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, deptCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<crterm_descr >").append("<![CDATA[" + descr + "]]>").append("</crterm_descr>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("curr_code__frt"))
					{
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						String tranDateStr = genericUtility.getColumnValue("ord_date", dom);
						sql = "select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currCode1 = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						currCode = genericUtility.getColumnValue("curr_code__frt", dom);
						if(currCode != null && currCode.trim().length() > 0 )
						{
							exchRate = finCommon.getDailyExchRateSellBuy(currCode, currCode1, siteCode, tranDateStr, "B", conn);
							valueXmlString.append("<frt_rate >").append("<![CDATA[" + exchRate + "]]>").append("</frt_rate>");
						}
						frtType = genericUtility.getColumnValue("frt_type", dom);
						if(frtType != null && frtType.trim().equals("Q"))
						{
							valueXmlString.append("<frt_rate protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("frt_rate", dom) + "]]>").append("</frt_rate>");
						}
						else
						{
							valueXmlString.append("<frt_rate protect=\"1\">").append("<![CDATA[" + genericUtility.getColumnValue("frt_rate", dom) + "]]>").append("</frt_rate>");
						}

						reStr = itemChanged(dom, dom1, dom2, objContext, "frt_rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
					{
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						String tranDateStr = genericUtility.getColumnValue("ord_date", dom);
						sql = "select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currCode1 = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						currCode = genericUtility.getColumnValue("curr_code", dom)==null ?"":genericUtility.getColumnValue("curr_code", dom);
						if(currCode != null && currCode.trim().length() > 0)
						{
							exchRate = finCommon.getDailyExchRateSellBuy(currCode, currCode1, siteCode, tranDateStr, "B", conn);
							valueXmlString.append("<exch_rate >").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
						}
						valueXmlString.append("<curr_code__comm >").append("<![CDATA[" + currCode + "]]>").append("</curr_code__comm>");
						valueXmlString.append("<exch_rate__sp >").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate__sp>");
						if( currCode == currCode1 || ! currCode.trim().equals(currCode1.trim()))
						{
							valueXmlString.append("<exch_rate protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("exch_rate", dom) + "]]>").append("</exch_rate>");
						}
						else
						{
							valueXmlString.append("<exch_rate protect=\"1\">").append("<![CDATA[" + genericUtility.getColumnValue("exch_rate", dom) + "]]>").append("</exch_rate>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("curr_code__comm"))
					{

						siteCode = genericUtility.getColumnValue("site_code__dlv", dom);
						String tranDateStr = genericUtility.getColumnValue("ord_date", dom);
						sql = "select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currCode1 = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						currCode = genericUtility.getColumnValue("curr_code__comm", dom)==null?"":genericUtility.getColumnValue("curr_code__comm", dom);
						if(currCode != null && currCode.trim().length() > 0)
						{
							exchRate = finCommon.getDailyExchRateSellBuy(currCode, currCode1, siteCode, tranDateStr, "B", conn);
							valueXmlString.append("<exch_rate__sp >").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate__sp>");
						}
						if(! currCode.trim().equals(currCode1.trim()))
						{
							valueXmlString.append("<exch_rate__sp protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("exch_rate__sp", dom) + "]]>").append("</exch_rate__sp>");
						}
						else
						{
							valueXmlString.append("<exch_rate__sp protect=\"1\">").append("<![CDATA[" + genericUtility.getColumnValue("exch_rate__sp", dom) + "]]>").append("</exch_rate__sp>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("sales_pers"))
					{
						salesPers = genericUtility.getColumnValue("sales_pers", dom);
						sql = "select comm_perc, comm_perc__on, curr_code from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							commPerc = rs.getInt(1);
							commPercOn = rs.getString(2)==null?"":rs.getString(2);
							currCode = rs.getString(2)==null?"":rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<comm_perc >").append("<![CDATA[" + commPerc + "]]>").append("</comm_perc>");
						valueXmlString.append("<comm_perc__on >").append("<![CDATA[" + commPercOn + "]]>").append("</comm_perc__on>");
						valueXmlString.append("<curr_code__comm >").append("<![CDATA[" + currCode + "]]>").append("</curr_code__comm>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__comm", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
					{
						tranCode = genericUtility.getColumnValue("tran_code", dom);
						if(tranCode != null && tranCode.trim().length() > 0)
						{
							sql = "select trans_mode from transporter_mode where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								transMode = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(transMode != null && transMode.trim().length() > 0)
							{
								valueXmlString.append("<trans_mode >").append("<![CDATA[" + transMode + "]]>").append("</trans_mode>");
							}
							else
							{
								valueXmlString.append("<trans_mode >").append("<![CDATA[R]]>").append("</trans_mode>");
							}
							
							sql = "select tran_name,frt_term,curr_code from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
								frtTerm = rs.getString(2)==null?"":rs.getString(2);
								currCode = rs.getString(3)==null?"":rs.getString(3);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<tran_name >").append("<![CDATA[" + descr + "]]>").append("</tran_name>");
							valueXmlString.append("<frt_term >").append("<![CDATA[" + frtTerm + "]]>").append("</frt_term>");
							valueXmlString.append("<curr_code__frt >").append("<![CDATA[" + currCode + "]]>").append("</curr_code__frt>");

							reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__frt", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
						else
						{
							valueXmlString.append("<trans_mode >").append("<![CDATA[]]>").append("</trans_mode>");
							valueXmlString.append("<tran_name >").append("<![CDATA[]]>").append("</tran_name>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("dlv_term"))
					{
						dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
						sql = "select descr, policy_no from delivery_term where dlv_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, dlvTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							policyNo = rs.getString(2)==null?"":rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<delivery_term_descr >").append("<![CDATA[" + descr + "]]>").append("</delivery_term_descr>");
						valueXmlString.append("<policy_no >").append("<![CDATA[" + policyNo + "]]>").append("</policy_no>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("contract_no"))
					{
						contractNo = genericUtility.getColumnValue("contract_no", dom);
						if(contractNo != null && contractNo.trim().length() > 0)
						{
							indNo = genericUtility.getColumnValue("ind_no", dom);
							quotNo = genericUtility.getColumnValue("quot_no", dom);
							projCode = genericUtility.getColumnValue("proj_code", dom);
							valueXmlString.append("<ind_no  protect=\"1\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
							valueXmlString.append("<quot_no  protect=\"1\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
							valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							
							sql = "select supp_code,proj_code,tran_code,tax_class,tax_chap,tax_env, " +
									"frt_term,curr_code__frt,dlv_term,cr_term,frt_amt, " +
									"contract_type, price_list from pcontract_hdr where contract_no = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, contractNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								suppCode = rs.getString(1)==null?"":rs.getString(1);
								projCode = rs.getString(2)==null?"":rs.getString(2);
								tranCode = rs.getString(3)==null?"":rs.getString(3);
								taxClass = rs.getString(4)==null?"":rs.getString(4);
								taxChap = rs.getString(5)==null?"":rs.getString(5);
								taxEnv = rs.getString(6)==null?"":rs.getString(6);
								frtTerm = rs.getString(7)==null?"":rs.getString(7);
								currCodeFrt = rs.getString(8)==null?"":rs.getString(8);
								dlvTerm = rs.getString(9)==null?"":rs.getString(9);
								crTerm = rs.getString(10)==null?"":rs.getString(10);
								frtAmt = rs.getInt(11);
								contractType = rs.getString(12)==null?"":rs.getString(12);
								priceList = rs.getString(13)==null?"":rs.getString(13);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<supp_code >").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");
							valueXmlString.append("<proj_code >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							valueXmlString.append("<tran_code >").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
							valueXmlString.append("<tax_class >").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_chap >").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_env >").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
							valueXmlString.append("<dlv_term >").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term>");
							valueXmlString.append("<frt_amt >").append("<![CDATA[" + frtAmt + "]]>").append("</frt_amt>");
							valueXmlString.append("<pord_type >").append("<![CDATA[" + contractType + "]]>").append("</pord_type>");
						}
						else
						{
							valueXmlString.append("<ind_no  protect=\"0\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
							valueXmlString.append("<quot_no  protect=\"0\" >").append("<![CDATA[" + quotNo + "]]>").append("</quot_no>");
							valueXmlString.append("<proj_code  protect=\"0\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
						}
					}

					
					else if(currentColumn.trim().equalsIgnoreCase("quot_no"))
					{
						indNo = genericUtility.getColumnValue("ind_no", dom);
						quotNo = genericUtility.getColumnValue("quot_no", dom);
						projCode = genericUtility.getColumnValue("proj_code", dom);
						contractNo = genericUtility.getColumnValue("contract_no", dom)==null?"":genericUtility.getColumnValue("contract_no", dom);
						if(quotNo == null || quotNo.trim().length() > 0)
						{
							valueXmlString.append("<ind_no  protect=\"0\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
							valueXmlString.append("<contract_no  protect=\"0\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code  protect=\"0\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
						}
						else
						{
							valueXmlString.append("<ind_no  protect=\"1\" >").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
							valueXmlString.append("<contract_no  protect=\"1\" >").append("<![CDATA[" + contractNo + "]]>").append("</contract_no>");
							valueXmlString.append("<proj_code  protect=\"1\" >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
						}
						sql = "select supp_code, item_ser from pquot_hdr where quot_no = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, quotNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							suppCode = rs.getString(1)==null?"":rs.getString(1);
							itemSer = rs.getString(2)==null?"":rs.getString(2);
							
							valueXmlString.append("<supp_code >").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");
							
							reStr = itemChanged(dom, dom1, dom2, objContext, "supp_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
							valueXmlString.append("<item_ser >").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("adv_type"))
					{
						advType = genericUtility.getColumnValue("adv_type", dom);
						amtStr = genericUtility.getColumnValue("tot_amt", dom).trim();
						try
						{
							totalAmt = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						amtStr = genericUtility.getColumnValue("ord_amt", dom).trim();
						try
						{
							ordAmt = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						amtStr = genericUtility.getColumnValue("adv_perc", dom).trim();
						try
						{
							advPerc = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						if(advType.trim().equals("F"))
						{
							valueXmlString.append("<advance >").append("<![CDATA[" + advPerc + "]]>").append("</advance>");
						}
						else if(advType.trim().equals("P"))
						{
							int advance = (advPerc / 100) * totalAmt;
							valueXmlString.append("<advance >").append("<![CDATA[" + advance + "]]>").append("</advance>");
						}
						else
						{
							int advance = (advPerc / 100) * totalAmt;
							valueXmlString.append("<advance >").append("<![CDATA[" + advance + "]]>").append("</advance>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("adv_perc"))
					{
						advType = genericUtility.getColumnValue("adv_type", dom);
						amtStr = genericUtility.getColumnValue("tot_amt", dom).trim();
						try
						{
							totalAmt = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						amtStr = genericUtility.getColumnValue("ord_amt", dom).trim();
						try
						{
							ordAmt = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						amtStr = genericUtility.getColumnValue("adv_perc", dom).trim();
						try
						{
							advPerc = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						if(advType.trim().equals("F"))
						{
							valueXmlString.append("<advance >").append("<![CDATA[" + advPerc + "]]>").append("</advance>");
						}
						else if(advType.trim().equals("P"))
						{
							int advance = (advPerc / 100) * totalAmt;
							valueXmlString.append("<advance >").append("<![CDATA[" + advance + "]]>").append("</advance>");
						}
						else
						{
							int advance = (advPerc / 100) * totalAmt;
							valueXmlString.append("<advance >").append("<![CDATA[" + advance + "]]>").append("</advance>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("frt_type"))
					{
						frtType = genericUtility.getColumnValue("frt_type", dom).trim();
						if(frtType.equals("F"))
						{
							valueXmlString.append("<frt_rate protect='1' >").append("<![CDATA[0]]>").append("</frt_rate>");
						}
						else if(frtType.equals("Q"))
						{
							valueXmlString.append("<frt_rate protect='0' >").append("<![CDATA[" + genericUtility.getColumnValue("frt_rate", dom) + "]]>").append("</frt_rate>");
						}						
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("frt_rate"))
					{
						frtType = genericUtility.getColumnValue("frt_type", dom);
						if(frtType != null && frtType.equals("Q"))
						{
							try 
							{
								frtAmt = Integer.parseInt(genericUtility.getColumnValue("frt_rate", dom).trim());	
							} 
							catch (Exception e) 
							{
								System.out.println("Parsing error at frt_amt in frt_rate");
							}
							
							if(frtAmt == 0)
							{
								pordType = genericUtility.getColumnValue("purc_order", dom).trim();
								sql = "select (case when sum(case when quantity is null then 0 else quantity end) is null then " +
										" 0 else sum(case when quantity is null then 0 else quantity end) end) " +
										" from porddet where purc_order = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, pordType);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									quantity = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								frtAmt = frtAmt * quantity;
								valueXmlString.append("<frt_amt protect='0' >").append("<![CDATA[" + frtAmt + "]]>").append("</frt_amt>");
							}
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("frt_amt__fixed"))
					{
						amtStr = genericUtility.getColumnValue("frt_amt__fixed", dom).trim();
						try
						{
							frtAmtFixed = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						amtStr = genericUtility.getColumnValue("frt_amt__qty", dom).trim();
						try
						{
							frtAmtQty = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						totalAmt = frtAmtFixed + frtAmtQty ;
						valueXmlString.append("<frt_amt >").append("<![CDATA[" + totalAmt + "]]>").append("</frt_amt>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("bank_code__pay"))
					{
						bankCodePay = genericUtility.getColumnValue("bank_code__pay", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						sql = "select bank_name__ben, bank_acct_no__ben	from supplier_bank " +
								" where supp_code = ? and bank_code__ben = ? " +
								" and case when confirmed is null then 'N' else confirmed end = 'Y' " +
								" and case when active_yn is null then 'Y' else active_yn end = 'Y' ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						pstmt.setString(2, bankCodePay);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							bankNameBen = rs.getString(1)==null?"":rs.getString(1);
							bankCodeNoBen = rs.getString(2)==null?"":rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<bank_name__ben >").append("<![CDATA[" + bankNameBen + "]]>").append("</bank_name__ben>");
						valueXmlString.append("<bank_acct_no__ben >").append("<![CDATA[" + bankCodeNoBen + "]]>").append("</bank_acct_no__ben>");
					}
					valueXmlString.append("</Detail1>");
					break;  

				case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail2>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						purcOrder = genericUtility.getColumnValue("purc_order", dom1);
						valueXmlString.append("<purc_order >").append("<![CDATA[" +  purcOrder + "]]>").append("</purc_order>");
						siteCode = genericUtility.getColumnValue("site_code__dlv", dom1);
						valueXmlString.append("<site_code >").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
						date1 = getCurrdateInAppFormat();
						valueXmlString.append("<status_date >").append("<![CDATA[" +  date1 + "]]>").append("</status_date>");
						valueXmlString.append("<req_date >").append("<![CDATA[" +  date1 + "]]>").append("</req_date>");
						valueXmlString.append("<dlv_date >").append("<![CDATA[" +  date1 + "]]>").append("</dlv_date>");
						
						sql = "Select case when eou is null then 'N' else eou From site Where site_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							eou = rs.getString(1);
							if(eou.trim().equals("Y"))
							{
								valueXmlString.append("<duty_paid >").append("<![CDATA[N]]>").append("</duty_paid>");
							}
							else
							{
								valueXmlString.append("<duty_paid >").append("<![CDATA[]]>").append("</duty_paid>");
							}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						itemSer = genericUtility.getColumnValue("item_ser", dom1);
						saleOrder = genericUtility.getColumnValue("sale_order", dom1);
						if(saleOrder != null && saleOrder.trim().length() > 0)
						{
							valueXmlString.append("<line_no__sord protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("line_no__sord", dom1) + "]]>").append("</line_no__sord>");
						}
						indNo = genericUtility.getColumnValue("ind_no", dom1);
						valueXmlString.append("<site_code protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("site_code", dom1) + "]]>").append("</site_code>");
						valueXmlString.append("<ind_no protect=\"0\">").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
						valueXmlString.append("<item_code protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("item_code", dom1) + "]]>").append("</item_code>");
						valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");
						
						if(indNo != null && indNo.trim().length() > 0)
						{
							valueXmlString.append("<ind_no protect=\"1\">").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");

							reStr = itemChanged(dom, dom1, dom2, objContext, "ind_no", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
						else
						{
							valueXmlString.append("<ind_no protect=\"0\">").append("<![CDATA[" + indNo + "]]>").append("</ind_no>");
						}
						projCode = genericUtility.getColumnValue("proj_code", dom1);
						valueXmlString.append("<proj_code >").append("<![CDATA[" +  projCode + "]]>").append("</proj_code>");
						amtStr = genericUtility.getColumnValue("exch_rate", dom).trim();
						try
						{
							exchRate = Integer.parseInt(amtStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 3026 error");
						}
						valueXmlString.append("<exch_rate >").append("<![CDATA[" +  exchRate + "]]>").append("</exch_rate>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("item_code"))
					{
						pordType = genericUtility.getColumnValue("purc_order", dom1);
						indNo = genericUtility.getColumnValue("ind_no", dom);
						String lineNo = genericUtility.getColumnValue("line_no", dom);
						try
						{
							quantity = Integer.parseInt(genericUtility.getColumnValue("quantity__stduom", dom));
						}
						catch(Exception e1)
						{
							System.out.println("error at 4798");
						}
						if(isReasonRequired(pordType, lineNo, indNo, quantity, conn));
						{
							String rowCountStr = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "row_count");
							try
							{
								rowCount = Long.parseLong(rowCountStr);
							}
							catch(Exception exc)
							{
								System.out.println("line no 3026 error");
							}
							itemCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "item_code");
							indNo = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "ind_no");
							String itemCodeMfg = genericUtility.getColumnValue("item_code__mfg", dom);
							if(rowCount > 0)
							{
								//descr = gf_get_desc_specs(ls_code);
								if(indNo != null && indNo.trim().length() > 0)
								{
									valueXmlString.append("<item_code__mfg protect=\"1\">").append("<![CDATA[" + itemCodeMfg + "]]>").append("</item_code__mfg>");
								}
								else
								{
									valueXmlString.append("<item_code__mfg protect=\"0\">").append("<![CDATA[" + itemCodeMfg + "]]>").append("</item_code__mfg>");
								}
							}
							else
							{
								valueXmlString.append("<item_code__mfg protect=\"0\">").append("<![CDATA[" + itemCodeMfg + "]]>").append("</item_code__mfg>");
							}
							valueXmlString.append("<spl_instr >").append("<![CDATA[" + descr + "]]>").append("</spl_instr>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("ind_no"))
					{
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						indNo = genericUtility.getColumnValue("ind_no", dom);
						quotNo = genericUtility.getColumnValue("quot_no", dom);
						suppCode = genericUtility.getColumnValue("supp_code", dom);
						projCode = genericUtility.getColumnValue("proj_code", dom);
						if(indNo != null && indNo.trim().length() > 0)
						{
							sql = "Select item_code, unit__ind, case when quantity__stduom is null then 0 else quantity__stduom end " +
									", case when ord_qty is null then 0 else ord_qty end,req_date, " +
									" site_code__dlv,conv__qty_stduom,special_instr,	 specific_instr,remarks, pack_instr, acct_code, cctr_code, unit__std, " +
									" emp_code__qcaprv, item_code__mfg,WORK_ORDER,dept_code,supp_code__mnfr, proj_code" +
									" from indent where ind_no = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, indNo);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemCode = rs.getString(1)==null?"":rs.getString(1);
								uom = rs.getString(2)==null?"":rs.getString(2);
								pendQty = rs.getInt(3);
								reqDate = rs.getTimestamp(4);
								siteCodeDlv = rs.getString(5)==null?"":rs.getString(5);
								conv = rs.getInt(6);
								specialInstr = rs.getString(7)==null?"":rs.getString(7);
								specifiedInstr = rs.getString(8)==null?"":rs.getString(8);
								indRemark = rs.getString(9)==null?"":rs.getString(9);
								packInstr = rs.getString(10)==null?"":rs.getString(10);
								acctDr = rs.getString(11)==null?"":rs.getString(11);
								cctrDr = rs.getString(12)==null?"":rs.getString(12);
								unitStd = rs.getString(13)==null?"":rs.getString(13);
								empCode = rs.getString(14)==null?"":rs.getString(14);
								mfgItem = rs.getString(15)==null?"":rs.getString(15);
								workOrder = rs.getString(16)==null?"":rs.getString(16);
								deptCode = rs.getString(17)==null?"":rs.getString(17);
								suppMnfr = rs.getString(18)==null?"":rs.getString(18);
								projCode = rs.getString(19)==null?"":rs.getString(19);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							valueXmlString.append("<remarks >").append("<![CDATA[" + indRemark + "]]>").append("</remarks>");
							valueXmlString.append("<item_code >").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
							valueXmlString.append("<acct_code__dr >").append("<![CDATA[" + acctDr + "]]>").append("</acct_code__dr>");
							valueXmlString.append("<cctr_code__dr >").append("<![CDATA[" + cctrDr + "]]>").append("</cctr_code__dr>");
							valueXmlString.append("<site_code >").append("<![CDATA[" + siteCodeDlv + "]]>").append("</site_code>");
							valueXmlString.append("<special_instr >").append("<![CDATA[" + specialInstr + "]]>").append("</special_instr>");
							valueXmlString.append("<specific_instr >").append("<![CDATA[" + specifiedInstr + "]]>").append("</specific_instr>");
							valueXmlString.append("<pack_instr >").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr>");
							valueXmlString.append("<unit >").append("<![CDATA[" + uom + "]]>").append("</unit>");
							valueXmlString.append("<unit__std >").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
							valueXmlString.append("<unit__rate >").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
							valueXmlString.append("<CONV__QTY_STDUOM >").append("<![CDATA[" + conv + "]]>").append("</CONV__QTY_STDUOM>");
							valueXmlString.append("<emp_code__qcaprv >").append("<![CDATA[" + empCode + "]]>").append("</emp_code__qcaprv>");
							valueXmlString.append("<work_order >").append("<![CDATA[" + workOrder + "]]>").append("</work_order>");
							valueXmlString.append("<dept_code >").append("<![CDATA[" + deptCode + "]]>").append("</dept_code>");
							valueXmlString.append("<proj_code >").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");

							reStr = itemChanged(dom, dom1, dom2, objContext, "acct_code__dr", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
							reStr = itemChanged(dom, dom1, dom2, objContext, "cctr_code__dr", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
							if(projCode != null && projCode.trim().length() > 0)
							{
								valueXmlString.append("<proj_code protect=\"1\">").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
							}
							
							sql = "select descr from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, mfgItem);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<item_code__mfg  protect=\"1\">").append("<![CDATA[" + mfgItem + "]]>").append("</item_code__mfg>");
							valueXmlString.append("<mfg_item_descr >").append("<![CDATA[" + descr + "]]>").append("</mfg_item_descr>");
							if(quotNo != null && quotNo.trim().length() > 0)
							{
								sql = "select rate,discount from pquot_det " +
										"where quot_no = ? and item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, quotNo);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									rate = rs.getInt(1);
									discount = rs.getInt(2);
								}
								else
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "select purc_rate from item where item_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										rate = rs.getInt(1);
									}								
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								valueXmlString.append("<rate  protect=\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
								valueXmlString.append("<rate__clg  protect=\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
								valueXmlString.append("<discount  protect=\"1\">").append("<![CDATA[" + discount + "]]>").append("</discount>");
							}
							else 
							{
								sql = "select rate__ref from supplieritem where supp_code = ? and item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									rate = rs.getInt(1);
								}
								else
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "select purc_rate from item where item_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										rate = rs.getInt(1);
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								valueXmlString.append("<rate  protect=\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
								valueXmlString.append("<rate__clg  protect=\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
							}
							valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + pendQty + "]]>").append("</quantity__stduom>");
							if(! uom.trim().equals(unitStd.trim()))
							{
								conv = 0;
								//pendqty = gf_conv_qty_fact(ls_unitstd, uom, itemcode, pendqty, lc_conv)
								//pendQty = disCommon.convQtyFactor(unitStd, uom, itemCode, pendQty, conv, conn); //Commented due to array list return
								value = this.getEnvDis("999999","UOM_ROUND" , conn);
								if(value.equals("R") || value.equals("B"))
								{
									//lc_ratestduom = gf_conv_qty_fact1(ls_unitstd, uom, itemcode, mrate, lc_conv,'Y')
								}
								else
								{
									//lc_ratestduom = gf_conv_qty_fact1(ls_unitstd, uom, itemcode, mrate, lc_conv,'N')
								}
								valueXmlString.append("<CONV__RTUOM_STDUOM>").append("<![CDATA[" + conv + "]]>").append("</CONV__RTUOM_STDUOM>");
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + restDuom + "]]>").append("</rate__stduom>");
							}
							valueXmlString.append("<quantity>").append("<![CDATA[" + pendQty + "]]>").append("</quantity>");
							sql = "Select descr, loc_code, unit, unit__pur from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
								locCode = rs.getString(2)==null?"":rs.getString(2);
								uom = rs.getString(3)==null?"":rs.getString(3);
								unitPur = rs.getString(4)==null?"":rs.getString(4);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(quotNo != null && quotNo.trim().length() > 0 )
							{
								sql = "select enq_no from pquot_det where quot_no = ? and item_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, quotNo);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									enqNo = rs.getString(1)==null?"":rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(enqNo != null && enqNo.trim().length() > 0)
								{
									sql = "select pack_code,pack_instr,supp_code__mnfr " +
											" from enq_det where enq_no = ? and item_code = ? and status = 'O' ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, enqNo);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										packCode = rs.getString(1)==null?"":rs.getString(1);
										packInstr = rs.getString(2)==null?"":rs.getString(2);
										suppMnfr = rs.getString(3)==null?"":rs.getString(3);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</rate__stduom>");
							valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
							valueXmlString.append("<req_date>").append("<![CDATA[" + genericUtility.getValidDateTimeString(reqDate, getApplDateFormat()) + "]]>").append("</req_date>");
							valueXmlString.append("<dlv_date>").append("<![CDATA[" + genericUtility.getValidDateTimeString(reqDate, getApplDateFormat()) + "]]>").append("</dlv_date>");
							valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
							valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr>");
							valueXmlString.append("<supp_code__mnfr>").append("<![CDATA[" + suppMnfr + "]]>").append("</supp_code__mnfr>");
							
							reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
						else
						{
							valueXmlString.append("<item_code__mfg  protect=\"0\">").append("<![CDATA[" + mfgItem + "]]>").append("</item_code__mfg>");
						}
						if(indNo == null || indNo.trim().length() == 0)
						{
							valueXmlString.append("<proj_code  protect=\"0\">").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
						}
						if(siteCodeDlv == null || siteCodeDlv.trim().length() == 0)
						{
							valueXmlString.append("<site_code  protect=\"0\">").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("line_no__sord"))
					{
						String lineNo = genericUtility.getColumnValue("line_no__sord", dom);
						saleOrder = genericUtility.getColumnValue("sale_order", dom1);
						valueXmlString.append("<line_no__sord>").append("<![CDATA[" + lineNo.trim() + "]]>").append("</line_no__sord>");
						
						sql = "select item_code,quantity,unit from sorditem where sale_order = ? and line_no = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						pstmt.setString(2, lineNo);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							itemCode = rs.getString(1)==null?"":rs.getString(1);
							quantity = rs.getInt(2);
							uom = rs.getString(3)==null?"":rs.getString(3);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						
						sql = "select descr,loc_code,item_ser from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							locCode = rs.getString(2)==null?"":rs.getString(2);
							itemSer = rs.getString(3)==null?"":rs.getString(3);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select dsp_date from sorddet where sale_order = ? and line_no = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							dspDate = rs.getTimestamp(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
						valueXmlString.append("<req_date>").append("<![CDATA[" + genericUtility.getValidDateTimeString(dspDate, getApplDateFormat()) + "]]>").append("</req_date>");
						valueXmlString.append("<dlv_date>").append("<![CDATA[" + genericUtility.getValidDateTimeString(dspDate, getApplDateFormat()) + "]]>").append("</dlv_date>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
						valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + quantity + "]]>").append("</quantity__stduom>");
						valueXmlString.append("<unit>").append("<![CDATA[" + uom + "]]>").append("</unit>");
						valueXmlString.append("<unit__std>").append("<![CDATA[" + uom + "]]>").append("</unit__std>");
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + uom + "]]>").append("</unit__rate>");
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[1]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("item_code"))
					{
						itemCode = genericUtility.getColumnValue("item_code", dom);
						contractNo = genericUtility.getColumnValue("contract_no", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						pordType = genericUtility.getColumnValue("pord_type", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						indNo = genericUtility.getColumnValue("ind_no", dom);
						if(indNo != null && indNo.trim().length() > 0)
						{
							valueXmlString.append("<item_code__mfg protect=\"1\">").append("<![CDATA[" + genericUtility.getColumnValue("item_code__mfg", dom) + "]]>").append("</item_code__mfg>");
						}
						else
						{
							valueXmlString.append("<item_code__mfg protect=\"0\">").append("<![CDATA[" + genericUtility.getColumnValue("item_code__mfg", dom) + "]]>").append("</item_code__mfg>");
						}
						sql = "Select descr, loc_code, unit, unit__pur, pack_code,pack_instr, item_ser,unit__rate " +
								" from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							locCode = rs.getString(2)==null?"":rs.getString(2);
							uom = rs.getString(3)==null?"":rs.getString(3);
							unitPur = rs.getString(4)==null?"":rs.getString(4);
							packCode = rs.getString(5)==null?"":rs.getString(5);
							packInstr = rs.getString(6)==null?"":rs.getString(6);
							itemSer = rs.getString(7)==null?"":rs.getString(7);
							unitRate = rs.getString(8)==null?"":rs.getString(8);
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select loc_code__aprv,loc_code__insp from siteitem " +
								"where  site_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							locCodeAprv = rs.getString(1)==null?"":rs.getString(1);
							locCodeInsp = rs.getString(2)==null?"":rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//ls_qcreqd = gf_qc_reqd(ls_sitecode,mcode)
						if(locCodeInsp != null && locCodeInsp.trim().length() > 0 && qcRequired.equals("Y"))
						{
							valueXmlString.append("<loc_code>").append("<![CDATA[" + locCodeInsp + "]]>").append("</loc_code>");
						}
						else if(locCodeAprv != null && locCodeAprv.trim().length() > 0 && qcRequired.equals("N"))
						{
							valueXmlString.append("<loc_code>").append("<![CDATA[" + locCodeAprv + "]]>").append("</loc_code>");
						}
						else
						{
							valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
						}

						sql = "select emp_code__qcaprv from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							empCodeAprv = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select emp_fname,emp_mname,emp_lname " +
								" from employee where emp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							empFname = rs.getString(1)==null?"":rs.getString(1);
							empMname = rs.getString(2)==null?"":rs.getString(2);
							empLname = rs.getString(3)==null?"":rs.getString(3);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						date1 = genericUtility.getValidDateString(genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						reqDate = Timestamp.valueOf(date1 + " 00:00:00.0");
						value = this.getEnvDis("999999","STD_PO_PL" , conn);
						if(priceList == null || priceList.equals("NULLFOUND"))
						{
							priceList = genericUtility.getColumnValue("price_list", dom);
							unitStd = genericUtility.getColumnValue("unit__std", dom1);
							
							sql = "select rate from pricelist where price_list=? and item_code=? and unit=?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, unitStd);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								rate = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						else
						{
							stdRate = disCommon.pickRate(priceList, date1, itemCode, "", "L", conn);
						}
						valueXmlString.append("<std_rate>").append("<![CDATA[" + stdRate + "]]>").append("</std_rate>");
						valueXmlString.append("<emp_fname>").append("<![CDATA[" + empFname + "]]>").append("</emp_fname>");
						valueXmlString.append("<emp_mname>").append("<![CDATA[" + empMname + "]]>").append("</emp_mname>");
						valueXmlString.append("<emp_lname>").append("<![CDATA[" + empLname + "]]>").append("</emp_lname>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" + itemSer + "]]>").append("</item_descr>");
						valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeAprv + "]]>").append("</emp_code__qcaprv>");
						
						if(unitPur == null || unitPur.trim().length() == 0)
						{
							unitPur = uom;
						}
						if(unitRate == null || unitRate.trim().length() == 0)
						{
							unitRate = unitPur;
						}
						valueXmlString.append("<unit__std>").append("<![CDATA[" + uom + "]]>").append("</unit__std>");
						valueXmlString.append("<unit>").append("<![CDATA[" + unitPur + "]]>").append("</unit>");
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
						if(!(uom.trim().equals(unitPur.trim())))
						{
							amtStr = genericUtility.getColumnValue("quantity", dom).trim();
							try
							{
								quantity = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3811");
							}
							double tempQty = quantity;
							conv = 0;
							if(value.equals("Q") || value.equals("B"))
							{
								//lc_qtystduom  = gf_conv_qty_fact1(ls_unitpur, uom, mcode, lc_qty, lc_conv,'Y')
								//qtyStdUom = disCommon.convQtyFactor(unitPur, uom, itemCode, quantity, conv, conn); //Commented By Mahesh Patidar
							}
							else
							{
								//lc_qtystduom  = gf_conv_qty_fact1(ls_unitpur, uom, mcode, lc_qty, lc_conv,'N')
							}
							valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__qty_stduom>");
							if(tempQty != 0)
							{
								valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyStdUom + "]]>").append("</quantity__stduom>");
							}
							amtStr = genericUtility.getColumnValue("rate", dom).trim();
							try
							{
								rate = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							tempQty = rate ;
							conv = 0;
							if(value.equals("R") || value.equals("B"))
							{
								//lc_ratestduom = gf_conv_qty_fact1(uom,ls_rate_unit, mcode, lc_rate, lc_conv,'Y') 
							}
							else
							{
								//lc_ratestduom = gf_conv_qty_fact1(uom,ls_rate_unit, mcode, lc_rate, lc_conv,'N') 	
							}
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__rtuom_stduom>");
							if(tempQty != 0)
							{
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStdUom + "]]>").append("</rate__stduom>");
							}
						}
						else
						{
							valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[1]]>").append("</conv__qty_stduom>");
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");
							amtStr = genericUtility.getColumnValue("rate", dom).trim();
							try
							{
								rate = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							amtStr = genericUtility.getColumnValue("quantity", dom).trim();
							try
							{
								quantity = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + quantity + "]]>").append("</quantity__stduom>");
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate + "]]>").append("</rate__stduom>");
						}
						valueXmlString.append("<pack_code>").append("<![CDATA[" + pack + "]]>").append("</pack_code>");
						pack = genericUtility.getColumnValue("pack_code", dom);
						if(pack == null || pack.trim().length() == 0)
						{
							valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
						}
						String packInstr1 = genericUtility.getColumnValue("pack_instr", dom);
						if(packInstr1 == null || packInstr1.trim().length() == 0)
						{
							valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr>");
						}
						indNo = genericUtility.getColumnValue("ind_no", dom);
						if(indNo == null || indNo.trim().length() == 0)
						{
							cctr = finCommon.getAcctDetrTtype(itemCode, itemSer, "IN", pordType , conn);
							acct = disCommon.getToken(cctr, "~t");
							valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + acct + "]]>").append("</acct_code__dr>");
							valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + cctr + "]]>").append("</cctr_code__dr>");
						}
						if(contractNo != null && contractNo.trim().length() > 0)
						{
							sql = "select max_qty,rate,unit,loc_code,tax_class,tax_chap,tax_env, " +
									" discount_type,discount,acct_code__dr,cctr_code__dr, " +
									" acct_code__cr,cctr_code__cr,bom_code from pcontract_det " +
									" where contract_no = ? and item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, contractNo);
							pstmt.setString(2, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								maxQty = rs.getInt(1);
								rate = rs.getInt(2);
								uom = rs.getString(3)==null?"":rs.getString(3);
								locCode = rs.getString(4)==null?"":rs.getString(4);
								taxClass = rs.getString(5)==null?"":rs.getString(5);
								taxChap = rs.getString(6)==null?"":rs.getString(6);
								taxEnv = rs.getString(7)==null?"":rs.getString(7);
								discountType = rs.getString(8)==null?"":rs.getString(8);
								discount = rs.getInt(9);
								acctDr = rs.getString(10)==null?"":rs.getString(10);
								cctrDr = rs.getString(11)==null?"":rs.getString(11);
								acctCr = rs.getString(12)==null?"":rs.getString(12);
								cctrCr = rs.getString(13)==null?"":rs.getString(13);
								bomCode = rs.getString(14)==null?"":rs.getString(14);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
							valueXmlString.append("<unit__rate>").append("<![CDATA[" + uom + "]]>").append("</unit__rate>");
							valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
							valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + quantity + "]]>").append("</quantity__stduom>");
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate + "]]>").append("</rate__stduom>");
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");
							valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
							valueXmlString.append("<discount_type>").append("<![CDATA[" + discountType + "]]>").append("</discount_type>");
							valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
							valueXmlString.append("<bom_code>").append("<![CDATA[" + bomCode + "]]>").append("</bom_code>");
						}
						valueXmlString.append("<contract_detail>").append("<![CDATA[]]>").append("</contract_detail>");
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("dlv_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						dspDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						String cont = "";
						
						sql = "select pcontract_det.contract_no from pcontract_det, pcontract_hdr " +
								" where ( pcontract_det.contract_no = pcontract_hdr.contract_no ) and " +
								" ( ( pcontract_det.item_code = ? ) and ( pcontract_hdr.status = 'O' ) and " +
								" ( pcontract_hdr.contract_fromdate <= ? ) and ( pcontract_hdr.contract_todate >= ? ) )";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setTimestamp(2, dspDate);
						pstmt.setTimestamp(3, dspDate);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							cont = cont + rs.getString(1).trim() + ",";
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cont.length() > 0)
						{
							cont = cont.substring(0, cont.length() - 1);
						}
						valueXmlString.append("<contract_detail>").append("<![CDATA[" + cont + "]]>").append("</contract_detail>");
						amtStr = genericUtility.getColumnValue("rate", dom).trim();
						try
						{
							rate = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 3836");
						}
						if(rate == 0)
						{
							priceList = genericUtility.getColumnValue("price_list", dom);
							contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							ordDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							amtStr = genericUtility.getColumnValue("quantity", dom).trim();
							try
							{
								quantity = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							unitPur = genericUtility.getColumnValue("unit__rate", dom);
							if(priceList != null && priceList.trim().length() > 0)
							{
								rate = disCommon.pickRate(priceList, contractDateStr, itemCode, "", "L", quantity, unitPur, conn);
							}
							if(!(rate > 0))
							{
								valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
								unitStd = genericUtility.getColumnValue("unit__std", dom);
								amtStr = genericUtility.getColumnValue("conv__rtuom_stduom", dom).trim();
								try
								{
									conv = Integer.parseInt(amtStr);
								}
								catch (Exception e) 
								{
									System.out.println("Error at 3836");
								}
								double convTemp = conv;
								if(value.equals("R") || value.equals("B"))
								{
									//lc_ratestduom = gf_conv_qty_fact1(mVal1, ls_unitpur, mcode, lc_rate, lc_conv,'Y')	
								}
								else
								{
									//lc_ratestduom = gf_conv_qty_fact1(mVal1, ls_unitpur, mcode, lc_rate, lc_conv,'N')	
								}
								if(convTemp == 0)
								{
									valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__rtuom_stduom>");
								}
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + restDuom + "]]>").append("</rate__stduom>");
							}
							else
							{
								rate = -1;
								valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
							}
						}
						indNo = genericUtility.getColumnValue("ind_no", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom1);
						quotNo = genericUtility.getColumnValue("quot_no", dom1);
						suppCode = genericUtility.getColumnValue("supp_code", dom1);
						projCode = genericUtility.getColumnValue("proj_code", dom1);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						contractNo = genericUtility.getColumnValue("contract_no", dom1);
						cctrCr = genericUtility.getColumnValue("cctr_code__cr", dom1);
						acctCr = genericUtility.getColumnValue("acct_code__cr", dom1);
						if(indNo != null &&  indNo.trim().length() > 0)
						{
							sql = "Select acct_code, cctr_code from indent where ind_no = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, indNo);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								acct = rs.getString(1)==null?"":rs.getString(1);
								cctr = rs.getString(2)==null?"":rs.getString(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
						}
						if(acctDr == null || acctDr.trim().length() == 0)
						{
							if(cont != null && cont.trim().length() > 0)
							{
								sql = "select acct_code__dr,cctr_code__dr, acct_code__cr,cctr_code__cr " +
										" from pcontract_det where contract_no = ? and item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, cont);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									acctDr = rs.getString(1)==null?"":rs.getString(1);
									cctrDr = rs.getString(2)==null?"":rs.getString(2);
									acctCr = rs.getString(3)==null?"":rs.getString(3);
									cctrCr = rs.getString(4)==null?"":rs.getString(4);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(acctDr == null || acctDr.trim().length() == 0)
							{
								if(projCode != null && projCode.trim().length() > 0)
								{
									sql = "select acct_code,cctr_code from project where proj_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, projCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										acctDr = rs.getString(1)==null?"":rs.getString(1);
										cctrDr = rs.getString(2)==null?"":rs.getString(2);
										acctCr = rs.getString(3)==null?"":rs.getString(3);
										cctrCr = rs.getString(4)==null?"":rs.getString(4);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							if(acctDr == null || acctDr.trim().length() == 0)
							{
								cctrDr = finCommon.getAcctDetrTtype(itemCode, itemSer, "IN", pordType, conn);
								acctDr = disCommon.getToken(cctrDr, "~t");
							}
						}
						invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
						if(invAcct == null || invAcct.trim().length() == 0 || invAcct.equals("NULLFOUND"))
						{
							invAcct = "N";
						}
						invAcct = finCommon.getFinparams("999999", "INV_ACCT_QCORDER", conn);
						if(invAcctQc == null || invAcctQc.trim().length() == 0 || invAcctQc.equals("NULLFOUND"))
						{
							invAcctQc = "N";
						}
						if(acctCr == null || acctCr.trim().length() == 0)
						{
							if(invAcct.equals("Y") && ! invAcctQc.equals("Y"))
							{
								cctrCr = finCommon.getAcctDetrTtype(itemCode, itemSer, "PORCP", pordType, conn);
								acctCr = disCommon.getToken(cctrCr, "~t");
							}
							else
							{

								cctrCr = finCommon.getAcctDetrTtype(itemCode, itemSer, "PO", pordType, conn);
								acctCr = disCommon.getToken(cctrCr, "~t");
								if(acctCr == null || acctCr.trim().length() == 0)
								{
									sql = "select acct_code__ap , cctr_code__ap from supplier where  supp_code = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, suppCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										acctCr = rs.getString(1)==null?"":rs.getString(1);
										cctrCr = rs.getString(2)==null?"":rs.getString(2);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						}
						valueXmlString.append("<acct_code__dr>").append("<![CDATA[" + acctDr + "]]>").append("</acct_code__dr>");
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA[" + cctrDr + "]]>").append("</cctr_code__dr>");
						valueXmlString.append("<acct_code__cr>").append("<![CDATA[" + acctCr + "]]>").append("</acct_code__cr>");
						valueXmlString.append("<cctr_code__cr>").append("<![CDATA[" + cctrCr + "]]>").append("</cctr_code__cr>");

						reStr = itemChanged(dom, dom1, dom2, objContext, "acct_code__dr", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

						reStr = itemChanged(dom, dom1, dom2, objContext, "cctr_code__dr", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

						reStr = itemChanged(dom, dom1, dom2, objContext, "acct_code__cr", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

						reStr = itemChanged(dom, dom1, dom2, objContext, "cctr_code__cr", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						suppCode = genericUtility.getColumnValue("supp_code", dom1);
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						
						sql = "select tax_class, tax_env from site_supplier where site_code = ? and supp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, suppCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxClass = rs.getString(1)==null?"":rs.getString(1);
							taxEnv= rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(taxClass != null && taxClass.trim().length() > 0)
						{
							valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
						}
						if(taxEnv != null && taxEnv.trim().length() > 0)
						{
							valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
						}
						itemCode = genericUtility.getColumnValue("item_code", dom);
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						
						String taxChapHdr = genericUtility.getColumnValue("tax_chap", dom1);
						String taxClassHdr = genericUtility.getColumnValue("tax_class", dom1);
						String taxEnvHdr = genericUtility.getColumnValue("tax_env", dom1);

						if(taxChap == null || taxChap.trim().length() == 0)
						{
							if(taxChapHdr == null || taxChapHdr.trim().length() == 0)
							{
								taxChap = this.getTaxChapter(itemCode, itemSer, 'S', suppCode, siteCode);
								valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							}
							else
							{
								valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChapHdr + "]]>").append("</tax_chap>");
							}
						}
						if(taxClass == null || taxClass.trim().length() == 0)
						{
							if(taxClassHdr == null || taxClassHdr.trim().length() == 0)
							{
								taxClass = this.getTaxClass('S', suppCode, itemCode, siteCode);
								valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							}
							else
							{
								valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClassHdr + "]]>").append("</tax_class>");
							}
						}
						if(taxEnv == null || taxEnv.trim().length() == 0)
						{
							if(taxEnvHdr == null || taxEnvHdr.trim().length() == 0)
							{
								suppCode = genericUtility.getColumnValue("supp_code", dom1);
								
								sql = "select tax_env from supplieritem where supp_code = ? and item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									taxEnv = rs.getString(1)==null?"":rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(taxEnv != null && taxEnv.trim().length() > 0)
								{
									valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_chap>");
								}
								else
								{
									sql = "select tax_env from supplier where supp_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, suppCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										taxEnv = rs.getString(1)==null?"":rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if(taxEnv != null && taxEnv.trim().length() > 0)
									{
										valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_chap>");
									}
									else
									{
										taxChap = genericUtility.getColumnValue("tax_chap", dom);
										taxClass = genericUtility.getColumnValue("tax_class", dom);
										fromStation = genericUtility.getColumnValue("station_stan_code", dom);
										sql = "select stan_code from site where site_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, siteCode);
										rs = pstmt.executeQuery();
										if(rs.next()) 
										{
											toStation = rs.getString(1)==null?"":rs.getString(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										taxEnv = this.getTaxEnv(fromStation, toStation, taxChap, taxClass, siteCode);
										valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_chap>");
									}
								}
							}
							else
							{
								valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxEnvHdr + "]]>").append("</tax_chap>");
							}
						}
						cctrDr = genericUtility.getColumnValue("cctr_code__dr", dom);
						if(cctrDr == null || cctrDr.trim().length() == 0)
						{
							cctrDr = genericUtility.getColumnValue("cctr_code__cr", dom);	
						}
						locCode = this.getEnvDis("999999", "CCENTER_AS_LOCATION", conn);
						if(! locCode.equals("NULLFOUND") && locCode.equals("Y"))
						{
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "select (case when qc_reqd is null then 'N' else qc_reqd end) from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								qcRequired = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(qcRequired.equals("Y"))
							{
								valueXmlString.append("<loc_code protect=\"1\">").append("<![CDATA[" + cctrDr.trim()+"Q" + "]]>").append("</loc_code>");
							}
							else
							{
								valueXmlString.append("<loc_code protect=\"1\">").append("<![CDATA[" + cctrDr.trim() + "]]>").append("</loc_code>");
							}
						}
						itemCode = genericUtility.getColumnValue("item_code", dom);
						if(!editFlag.equals("V"))
						{
							priceList = genericUtility.getColumnValue("price_list", dom1);
							if(priceList != null && priceList.trim().length()==0)
							{
								sql = "select count(*) from pricelist where price_list = ? and item_code = ? and " +
										" (list_type = 'F' or list_type = 'B')";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									valueXmlString.append("<rate protect='1'>").append("<![CDATA[0]]>").append("</rate>");
									valueXmlString.append("<rate__clg protect='1'>").append("<![CDATA[0]]>").append("</rate__clg>");
									valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
								}
								else
								{
									sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) " +
											" from pricelist where price_list = ? and list_type = 'B'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, priceList);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										priceListClg = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									if(priceListClg != null && priceListClg.trim().length() > 0)
									{
										sql = "select count(*) from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B')";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, priceList);
										pstmt.setString(2, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next()) 
										{
											cnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(cnt > 0 )
										{
											valueXmlString.append("<rate protect='1'>").append("<![CDATA[0]]>").append("</rate>");
											valueXmlString.append("<rate__clg protect='1'>").append("<![CDATA[0]]>").append("</rate__clg>");
											valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
										}
										else
										{
											valueXmlString.append("<rate protect='0'>").append("<![CDATA["+rate+"]]>").append("</rate>");
										}
									}
								}
							}
						}
						//mdescr = gf_get_desc_specs(mcode)
						valueXmlString.append("<spl_instr>").append("<![CDATA[" + descr + "]]>").append("</spl_instr>");
						priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);
						rateClg = disCommon.pickRate(priceListClg, contractDateStr, itemCode, "", "L", conn);
						if(rateClg == -1)
						{
							if(!editFlag.equals("V"))
							{
								priceList = genericUtility.getColumnValue("price_list", dom1);
								if(priceList != null && priceList.trim().length()==0)
								{
									sql = "select count(*) from pricelist where price_list = ? and item_code = ? and " +
											" (list_type = 'F' or list_type = 'B')";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, priceList);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(cnt > 0)
									{
										valueXmlString.append("<rate protect='1'>").append("<![CDATA[0]]>").append("</rate>");
										valueXmlString.append("<rate__clg protect='1'>").append("<![CDATA[0]]>").append("</rate__clg>");
										valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
									}
									else
									{
										sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) " +
												" from pricelist	where price_list = ? and list_type = 'B'";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, priceList);
										rs = pstmt.executeQuery();
										if(rs.next()) 
										{
											priceListClg = rs.getString(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										if(priceListClg != null && priceListClg.trim().length() > 0)
										{
											sql = "select count(*) from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B')";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, priceList);
											pstmt.setString(2, itemCode);
											rs = pstmt.executeQuery();
											if(rs.next()) 
											{
												cnt = rs.getInt(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											if(cnt > 0 )
											{
												valueXmlString.append("<rate protect='1'>").append("<![CDATA[0]]>").append("</rate>");
												valueXmlString.append("<rate__clg protect='1'>").append("<![CDATA[0]]>").append("</rate__clg>");
												valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
											}
											else
											{
												valueXmlString.append("<rate protect='0'>").append("<![CDATA["+rate+"]]>").append("</rate>");
											}
										}
									}
								}
							}
						}
						else
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
						}
						itemCode = genericUtility.getColumnValue("item_code", dom);
						indNo = genericUtility.getColumnValue("ind_no", dom1);
						
						sql = "select supp_code__mnfr from indent where ind_no = ? and	item_code= ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, indNo);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							suppMnfr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						pos = suppMnfr.indexOf(",");
						if(pos > 0)
						{
							suppMnfr = "";
						}
						valueXmlString.append("<supp_code__mnfr>").append("<![CDATA[" + suppMnfr.trim() + "]]>").append("</supp_code__mnfr>");
						
						sql = "select item_code__ref from supplieritem where supp_code = ?	and	item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, suppCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							itemCode1 = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(itemCode1 == null || itemCode1.trim().length() == 0)
						{
							itemCode1 = itemCode ;
						}
						valueXmlString.append("<supp_item__ref>").append("<![CDATA[" + itemCode1.trim() + "]]>").append("</supp_item__ref>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("cctr_code__dr"))
					{
						String costCtr = genericUtility.getColumnValue("cctr_code__dr", dom);
						costCtr = disCommon.getDisparams("999999", "CCENTER_AS_LOCATION", conn);
						if(! locCode.equals("NULLFOUND") && locCode.equals("Y"))
						{
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "select case when qc_reqd is null then 'N' else qc_reqd end from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								qcRequired = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(qcRequired.equals("Y"))
							{
								valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[" + costCtr.trim()+"Q" + "]]>").append("</loc_code>");
							}
							else
							{
								valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[" + costCtr.trim() + "]]>").append("</loc_code>");
							}
						}
						invAcct = finCommon.getFinparams("999999","INV_ACCT_PORCP" , conn);
						if(invAcct == null || invAcct.equals("NULLFOUND") || invAcct.trim().length() == 0)
						{
							invAcct = "N";
						}
						invAcct = finCommon.getFinparams("999999","INV_ACCT_QCORDER" , conn);
						if(invAcctQc == null || invAcctQc.equals("NULLFOUND") || invAcctQc.trim().length() == 0)
						{
							invAcctQc = "N";
						}
						if(invAcct.equals("Y") && invAcctQc.equals("Y"))
						{
							costCtr = "";
						}
						valueXmlString.append("<cctr_code__prov_dr>").append("<![CDATA[" + costCtr + "]]>").append("</cctr_code__prov_dr>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("cctr_code__cr"))
					{
						String costCtr = genericUtility.getColumnValue("cctr_code__cr", dom);
						locCode = this.getEnvDis("999999", "CCENTER_AS_LOCATION", conn);
						if(! locCode.equals("NULLFOUND") && locCode.equals("Y"))
						{
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "select case when qc_reqd is null then 'N' else qc_reqd end from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								qcRequired = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(qcRequired.equals("Y"))
							{
								valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[" + costCtr.trim()+"Q" + "]]>").append("</loc_code>");
							}
							else
							{
								valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[" + costCtr.trim() + "]]>").append("</loc_code>");
							}
						}
						invAcct = finCommon.getFinparams("999999","INV_ACCT_PORCP" , conn);
						if(invAcct == null || invAcct.equals("NULLFOUND") || invAcct.trim().length() == 0)
						{
							invAcct = "N";
						}
						invAcct = finCommon.getFinparams("999999","INV_ACCT_QCORDER" , conn);
						if(invAcctQc == null || invAcctQc.equals("NULLFOUND") || invAcctQc.trim().length() == 0)
						{
							invAcctQc = "N";
						}
						if(invAcct.equals("Y") && invAcctQc.equals("Y"))
						{
							costCtr = "";
						}
						valueXmlString.append("<cctr_code__prov_cr>").append("<![CDATA[" + costCtr + "]]>").append("</cctr_code__prov_cr>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("quantity"))
					{
						amtStr = genericUtility.getColumnValue("quantity", dom).trim();
						try
						{
							quantity = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 3836");
						}
						uom = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						amtStr = genericUtility.getColumnValue("conv__qty_stduom", dom).trim();
						try
						{
							conv = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 3836");
						}
						double convTemp = conv;
						if(unitStd == null || unitStd.trim().length() == 0)
						{
							sql = "Select unit from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								uom = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(value.equals("Q") || value.equals("B"))
							{
								//lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1, itemcode, lc_qty, lc_conv,'Y')	
							}
							else
							{
								//lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1, itemcode, lc_qty, lc_conv,'N')
							}
							valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
						}
						else
						{
							if(value.equals("Q") || value.equals("B"))
							{
								//lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1, itemcode, lc_qty, lc_conv,'Y')		
							}
							else
							{
								//lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1, itemcode, lc_qty, lc_conv,'N')	
							}
						}
						if(convTemp == 0)
						{
							valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__qty_stduom>");
						}
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyStdUom + "]]>").append("</quantity__stduom>");
						pordType = genericUtility.getColumnValue("purc_order", dom1);
						indNo = genericUtility.getColumnValue("ind_no", dom);
						String lineNo = genericUtility.getColumnValue("line_no", dom);
						try
						{
							quantity = Integer.parseInt(genericUtility.getColumnValue("quantity__stduom", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 6208");
						}
						if(isReasonRequired(pordType, lineNo, indNo, quantity, conn));
						{
							priceList = genericUtility.getColumnValue("price_list", dom);
							contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("ord_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							ordDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							amtStr = genericUtility.getColumnValue("rate", dom).trim();
							try
							{
								rate = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							if(rate <= 0)
							{
								if(priceList != null && priceList.trim().length() > 0)
								{
									rate = disCommon.pickRate(priceList, contractDateStr, itemCode, "", "L", quantity, unitStd, conn); 
									if(rate == 0)
									{
										rate = -1;
									}
									sql = "select count(*) from	pricelist where	price_list = ? and item_code = ? " +
											" and (list_type = 'F' or list_type = 'B')";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, priceList);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
									valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
									valueXmlString.append("<unit__rate>").append("<![CDATA[" + uom + "]]>").append("</unit__rate>");
									unitStd = genericUtility.getColumnValue("unit__std", dom);
									amtStr = genericUtility.getColumnValue("conv__rtuom_stduom", dom).trim();
									try
									{
										conv = Integer.parseInt(amtStr);
									}
									catch (Exception e) 
									{
										System.out.println("Error at 3836");
									}
									convTemp = conv;
									if(value.equals("R") || value.equals("B"))
									{
										//lc_ratestduom = gf_conv_qty_fact1(mVal1, mVal, itemcode, lc_rate, lc_conv,'Y')
									}
									else
									{
										//lc_ratestduom = gf_conv_qty_fact1(mVal1, mVal, itemcode, lc_rate, lc_conv,'Y')
									}
									if(convTemp == 0)
									{
										valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__rtuom_stduom>");
									}
									valueXmlString.append("<rate__stduom>").append("<![CDATA[" + restDuom + "]]>").append("</rate__stduom>");
								}
							}
						}
						if(!editFlag.equals("V"))
						{
							priceList = genericUtility.getColumnValue("price_list", dom1);
							if(priceList != null && priceList.trim().length()==0)
							{
								sql = "select count(*) from pricelist where price_list = ? and item_code = ? and " +
										" (list_type = 'F' or list_type = 'B')";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									valueXmlString.append("<rate protect='1'>").append("<![CDATA[0]]>").append("</rate>");
									valueXmlString.append("<rate__clg protect='1'>").append("<![CDATA[0]]>").append("</rate__clg>");
									valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
								}
								else
								{
									sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) " +
											" from pricelist	where price_list = ? and list_type = 'B'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, priceList);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										priceListClg = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									if(priceListClg != null && priceListClg.trim().length() > 0)
									{
										sql = "select count(*) from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B')";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, priceList);
										pstmt.setString(2, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next()) 
										{
											cnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(cnt > 0 )
										{
											valueXmlString.append("<rate protect='1'>").append("<![CDATA[0]]>").append("</rate>");
											valueXmlString.append("<rate__clg protect='1'>").append("<![CDATA[0]]>").append("</rate__clg>");
											valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
										}
										else
										{
											valueXmlString.append("<rate protect='0'>").append("<![CDATA["+rate+"]]>").append("</rate>");
										}
									}
								}
							}
						}					
						reStr = itemChanged(dom, dom1, dom2, objContext, "adj_variance", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("adj_variance"))
					{
						varValue = disCommon.getDisparams("999999", "PO_ADJ_VARIANCE", conn);
						if(varValue.equals("Y"))
						{
							uom = genericUtility.getColumnValue("unit", dom);
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							unitStd = genericUtility.getColumnValue("unit__std", dom);
							amtStr = genericUtility.getColumnValue("quantity", dom).trim();
							try
							{
								quantity = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							amtStr = genericUtility.getColumnValue("rate", dom).trim();
							try
							{
								rate = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							amtStr = genericUtility.getColumnValue("quantity__stduom", dom).trim();
							try
							{
								qtyStdUom1 = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							amtStr = genericUtility.getColumnValue("rate__stduom", dom).trim();
							try
							{
								rateStdUom1 = Integer.parseInt(amtStr);
							}
							catch (Exception e) 
							{
								System.out.println("Error at 3836");
							}
							int value1 = 0;
							varValue = disCommon.getDisparams("999999", "RCP_UOM_VARIANCE", conn);
							if(quantity > 0 && rate > 0)
							{
								double amt = quantity * rate;
								double amtStd = qtyStdUom1 * rateStdUom1;
								if(Math.abs(amt - amtStd) > value1 && ! uom.equals(unitStd))
								{
									qtyStdUom1 = amt / rateStdUom1 ;
									conv = qtyStdUom1 / quantity ;
									valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__qty_stduom>");
									valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyStdUom1 + "]]>").append("</quantity__stduom>");
								}
								amtStd = qtyStdUom1 * rateStdUom1;
								if(Math.abs(amt - amtStd) > value1 && ! unitRate.equals(unitStd))
								{
									rateStdUom1 = amt / qtyStdUom1 ;
									conv = rateStdUom1 / rate ;
									valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__rtuom_stduom>");
									valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStdUom1 + "]]>").append("</rate__stduom>");
								}
							}
						}
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("unit"))
					{
						uom = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						amtStr = genericUtility.getColumnValue("quantity", dom).trim();
						try
						{
							quantity = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 3836");
						}
						if(unitStd == null || unitStd.trim().length() > 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								unitStd = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
						}
						if(value.equals("Q") || value.equals("B"))
						{
							//lc_qtystduom  = gf_conv_qty_fact1(mcode, mVal1, itemcode, lc_qty, lc_conv,'Y')
						}
						else
						{
							//lc_qtystduom  = gf_conv_qty_fact1(mcode, mVal1, itemcode, lc_qty, lc_conv,'N')
						}
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyStdUom + "]]>").append("</quantity__stduom>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("conv__qty_stduom"))
					{
						amtStr = genericUtility.getColumnValue("conv__qty_stduom", dom).trim();
						try
						{
							conv = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 4767");
						}
						uom = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						amtStr = genericUtility.getColumnValue("quantity", dom).trim();
						try
						{
							quantity = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 4779");
						}
						if(unitStd == null || unitStd.trim().length() > 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								unitStd = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
						}
						if(value.equals("Q") || value.equals("B"))
						{
							//lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1, itemcode, lc_qty, lc_conv,'Y')
						}
						else
						{
							//lc_qtystduom = gf_conv_qty_fact1(mVal, mVal1, itemcode, lc_qty, lc_conv,'N')
						}
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + qtyStdUom + "]]>").append("</quantity__stduom>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("rate"))
					{
						amtStr = genericUtility.getColumnValue("rate", dom).trim();
						try
						{
							rate = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 4767");
						}
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						amtStr = genericUtility.getColumnValue("quantity", dom).trim();
						try
						{
							quantity = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 4779");
						}
						double convTemp = conv;
						if(unitStd == null || unitStd.trim().length() > 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								unitStd = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
						}
						if(unitRate == null || unitRate.trim().length() == 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								uom = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(value.equals("R") || value.equals("B"))
							{
								//lc_ratestduom = gf_conv_qty_fact1(mVal1, mVal, itemcode, lc_rate, lc_conv,'Y')	
							}
							else
							{
								//lc_ratestduom = gf_conv_qty_fact1(mVal1, mVal, itemcode, lc_rate, lc_conv,'N')
							}
							valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
						}
						else
						{
							if(value.equals("R") || value.equals("B"))
							{
								//lc_ratestduom = gf_conv_qty_fact1(mVal1, mVal, itemcode, lc_rate, lc_conv,'Y')		
							}
							else
							{
								//lc_ratestduom = gf_conv_qty_fact1(mVal1, mVal, itemcode, lc_rate, lc_conv,'N')
							}
						}
						if(convTemp == 0)
						{
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__rtuom_stduom>");
						}
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStdUom + "]]>").append("</rate__stduom>");
						amtStr = genericUtility.getColumnValue("rate__clg", dom).trim();
						try
						{
							rateClg = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 4779");
						}
						if(rateClg <= 0)
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
						}

						reStr = itemChanged(dom, dom1, dom2, objContext, "adj_variance", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("unit__rate"))
					{
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						amtStr = genericUtility.getColumnValue("rate", dom).trim();
						try
						{
							rate = Integer.parseInt(amtStr);
						}
						catch (Exception e) 
						{
							System.out.println("Error at 4779");
						}
						if(unitStd == null || unitStd.trim().length() > 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								unitStd = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							valueXmlString.append("<unit__std>").append("<![CDATA[" + unitStd + "]]>").append("</unit__std>");
						}
						if(value.equals("R") || value.equals("B"))
						{
							//lc_ratestduom = gf_conv_qty_fact1(mVal1, mcode, itemcode, lc_rate, lc_conv,'Y')
						}
						else
						{
							//lc_ratestduom = gf_conv_qty_fact1(mVal1, mcode, itemcode, lc_rate, lc_conv,'N')
						}
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + conv + "]]>").append("</conv__rtuom_stduom>");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStdUom + "]]>").append("</rate__stduom>");
					}
					valueXmlString.append("</Detail2>");
					break;
					
				case 3:
					parentNodeList = dom.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail3>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						valueXmlString.append("<purc_order >").append("<![CDATA[" +  genericUtility.getColumnValue("purc_order", dom1) + "]]>").append("</purc_order>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("term_code"))
					{
						termCode = genericUtility.getColumnValue("term_code", dom);
						sql = "select descr,input_nos from pur_term where term_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<descr >").append("<![CDATA[" +  descr + "]]>").append("</descr>");
					}
					valueXmlString.append("</Detail3>");
					break;   
					
				case 4:
					parentNodeList = dom.getElementsByTagName("Detail4");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail4>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{ 
						valueXmlString.append("<purc_order >").append("<![CDATA[" +  genericUtility.getColumnValue("purc_order", dom1) + "]]>").append("</purc_order>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("acct_code"))
					{
						acct = genericUtility.getColumnValue("acct_code", dom);
						sql = "Select descr from Accounts Where acct_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, acct);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<accounts_descr >").append("<![CDATA[" +  descr + "]]>").append("</accounts_descr>");
					}
					valueXmlString.append("</Detail4>");
					break;
			}
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
				throw new ITMException(d);
			}			
		}
		return valueXmlString.toString();
	}	 

	private int getDailyExchRate(String curr, String string, String siteCode,Timestamp contractDate, String string2, Connection conn) 
	{
		int exchRateSell = 0;
		String sql = "";
		String varValue = "";
		String finEntity = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(curr == null || curr.trim().length() == 0)
		{
			try
			{
				sql = "select fin_entity from site where site_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					finEntity = rs.getString(1)==null?"":rs.getString(1);

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select curr_code from finent where  fin_entity = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						curr = rs.getString(1)==null?"":rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				sql = "select exch_rate__sell from daily_exch_rate_sell_buy where curr_code = ? and " +
						" curr_code__to = ?  and ? between from_date and to_date ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, curr);
				pstmt.setString(2, curr);
				pstmt.setTimestamp(3, contractDate);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					exchRateSell = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(exchRateSell == 0)
				{
					sql = "select exch_rate from daily_exch_rate_sell_buy where curr_code = ? " +
							" and curr_code__to = ? and ? between from_date and to_date";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, curr);
					pstmt.setString(2, curr);
					pstmt.setTimestamp(3, contractDate);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						exchRateSell = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(exchRateSell != 0)
				{
					exchRateSell = 1 / exchRateSell;
				}
				sql = "select rtrim(case when var_value is null then 'Y' else var_value end) " +
						" from finparm where prd_code = '999999' and var_name = 'EXCRT_CURR'";
				pstmt =  conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					varValue = rs.getString(1);
				}
				if(!rs.next() || varValue.equals("Y"))
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select std_exrt from currency where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, curr);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						exchRateSell = rs.getInt(1);
					}
				}
				else
				{
					return 0;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}
			catch (Exception e)
			{
				System.out.println("Exception in getDailyExchRate ........");
			}
		}
		return exchRateSell;
	}

	private int getConvQuantityFact(String uom, String unitStd,String itemCode, int quantity, int num1,Connection conn) 
	{
		int cnt = 0;
		int fact = 0;
		int newQty = 0;
		int roundTo = 0;
		int conv = 0;
		String sql = "";
		String round = "";
		String order = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(uom == null && unitStd == null)
		{
			fact = 1;
			return num1;
		}
		else if(uom != null && unitStd != null && uom.equals(unitStd))
		{
			fact = 1;
			return num1;
		}
		try
		{
			sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and item_code = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, uom);
			pstmt.setString(2, unitStd);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				cnt = rs.getInt(1);
				if(cnt == 0 )
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and item_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, unitStd);
					pstmt.setString(2, uom);
					pstmt.setString(3, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						cnt = rs.getInt(1);
						if(cnt == 0 )
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and  item_code = 'X'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, uom);
							pstmt.setString(2, unitStd);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and  item_code = 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, unitStd);
									pstmt.setString(2, uom);
									pstmt.setString(3, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										if(cnt == 0)
										{
											return -999999999;
										}
										else
										{
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, unitStd);
											pstmt.setString(2, uom);
											rs = pstmt.executeQuery();
											if(rs.next()) 
											{
												conv = rs.getInt(1);
												round = rs.getString(2)==null?"":rs.getString(2);
												roundTo = rs.getInt(3);
												order = "REVORD";
											}											
										}
									}
								}
								else
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, uom);
									pstmt.setString(2, unitStd);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										conv = rs.getInt(1);
										round = rs.getString(2)==null?"":rs.getString(2);
										roundTo = rs.getInt(3);
										order = "ACTORD";
									}			
								}
							}
						}
						else
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, unitStd);
							pstmt.setString(2, uom);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								conv = rs.getInt(1);
								round = rs.getString(2)==null?"":rs.getString(2);
								roundTo = rs.getInt(3);
								order = "REVORD";
							}										
						}
					}
				}
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, uom);
					pstmt.setString(2, unitStd);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						conv = rs.getInt(1);
						round = rs.getString(2)==null?"":rs.getString(2);
						roundTo = rs.getInt(3);
						order = "ACTORD";
					}			
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(fact == 0)
			{
				if(order.equals("ACTORD"))
				{
					newQty = conv * num1;
					fact = conv;
				}
				else
				{
					newQty = 1 / conv * num1;
					fact = 1 / conv;
				}
			}
			else
			{
				newQty = fact * num1;
			}
			newQty = getQuantity(newQty,round,roundTo);
			
		}
		catch (Exception e) 
		{
			System.out.println("Exception in getConvQuantityFact........");
		}
		return newQty;
	}

	private int getQuantity(int newQty, String round, int roundTo) 
	{
		if(newQty < 0)
		{
			newQty = Math.abs(newQty);
		}
		else if(newQty == 0)
		{
			return newQty;
		}
		if(round.equals("N"))
		{
			return newQty;
		}
		if(roundTo == 0)
		{
			return newQty;
		}
		if(round.equals("X"))
		{
			if(newQty % roundTo > 0)
			{
				newQty = newQty - (newQty % roundTo) + roundTo;
			}
		}
		else if(round.equals("P"))
		{
			newQty = newQty - (newQty % roundTo);
		}
		else if(round.equals("R"))
		{
			if(newQty % roundTo < roundTo/2)
			{
				newQty = newQty - (newQty % roundTo);
			}
			else
			{
				newQty = newQty - (newQty % roundTo) + roundTo;
			}
		}
		return newQty;
	}

	private boolean isReasonRequired(String purcCode, String lineNo,String indNo, int quantity, Connection conn) 
	{
		boolean isRequired = false;
		String sql = "";
		int qty = 0;
		int ordQty = 0;
		int pqty =0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = "select (case when quantity__stduom is null then 0 else quantity__stduom end) ," +
					" (case when ord_qty is null then 0 else ord_qty end) from indent where ind_no = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, indNo);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				qty = rs.getInt(1);
				ordQty = rs.getInt(2);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = "select case when sum(case when porddet.quantity__stduom is null then 0 else porddet.quantity__stduom end)" +
					" is null then 0 else sum(case when porddet.quantity__stduom is null then 0" +
					" else porddet.quantity__stduom end) end " +
					" from porddet, porder where (porddet.purc_order = porder.purc_order ) and " +
					" (((case when porder.status is null then 'O' else porder.status end) <> 'X' ) and " +
					" ((case when porder.confirmed is null then 'N' else porder.confirmed end) <> 'Y' and " +
					" (case when porder.status is null then 'O' else porder.status end) <> 'C') )  and	(porddet.ind_no = ? ) and " +
					" ((porddet.purc_order <> ? ) or (porddet.purc_order = ? and porddet.line_no <> ?) )";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, indNo);
			pstmt.setString(2, purcCode);
			pstmt.setString(3, purcCode);
			pstmt.setString(4, lineNo);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				pqty = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if((quantity + ordQty + pqty) > qty)
			{
				isRequired = true;
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in isRequired" + e.getMessage());
		}
		return isRequired;
	}

	private int getPickRate(String priceList,String nrp, Timestamp contractDate,String itemCode, String string, String string2, Connection conn) 
	{
		int nrpRate = 0;
		String sql = "";
		String priceListParent = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try
		{
			sql = "select rate from pricelist where price_list = ? and item_code  = ?" +
				" and list_type = 'L' and eff_from <= ?  and valid_upto >= ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, contractDate);
			pstmt.setTimestamp(4, contractDate);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				nrpRate = rs.getInt(1);				
			}

			else
			{
				String pList = priceList;
				do
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "lect (case when price_list__parent is null  then '' else price_list__parent end ) " +
							" from pricelist_mst where price_list =  ? and list_type = 'L'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, pList);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						priceListParent = rs.getString(1)==null?"":rs.getString(1);
						if(priceListParent.trim().length() > 0)
						{
							sql = "select rate from pricelist where price_list = ? and item_code  = ? " +
									" and list_type = 'L' and eff_from <= ?  and valid_upto >= ?";
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1, priceListParent);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, contractDate);
							pstmt1.setTimestamp(4, contractDate);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()) 
							{
								nrpRate = rs.getInt(1);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							if(nrpRate > 0)
							{
								break;
							}
							else
							{
								pList = priceListParent;
								priceListParent = null;
							}
						}
					}
					else
					{
						return -1;
					}
				}while(true);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e) 
		{
			System.out.println("Exception in Pick rate get.......");
		}
		return nrpRate;
	}

	private String getPriceListType(String priceList,Connection conn) 
	{
		String sql = "";
		String priceListType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = " select list_type from pricelist where price_list  = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				priceListType = rs.getString(1)==null?"":rs.getString(1);
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in getPriceListType ........" );
		}
		
		return priceListType;
	}

	private int getPickRate(String priceList, Timestamp contractDate,String itemCode, String string, String string2, int quantity, Connection conn) 
	{
		int rate = 0;
		String sql = "";
		String priceListParent = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try
		{
			if(getPriceListType(priceList,conn).equals(""))
			{
				return -1;
			}
			sql = "select rate from pricelist where price_list = ? and item_code  = ? and list_type = 'L' " +
					" and eff_from <= ? and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, contractDate);
			pstmt.setTimestamp(4, contractDate);
			pstmt.setInt(5, quantity);
			pstmt.setInt(6, quantity);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				rate = rs.getInt(1);
			}
			else
			{
				String pList = priceList;
				do
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) " +
							" from pricelist_mst where price_list = ? and list_type = 'L'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, pList);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						priceListParent = rs.getString(1)==null?"":rs.getString(1);
						if(priceListParent.trim().length() > 0)
						{
							sql = "select rate from pricelist where price_list = ? " +
									" and item_code  = ?  and list_type = 'L' and eff_from <= ? " +
									" and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1, priceListParent);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, contractDate);
							pstmt1.setTimestamp(4, contractDate);
							pstmt1.setInt(5, quantity);
							pstmt1.setInt(6, quantity);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()) 
							{
								rate = rs.getInt(1);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							if(rate > 0)
							{
								break;
							}
							else
							{
								pList = priceListParent;
								priceListParent = null;
							}
						}
					}
					else
					{
						return -1;
					}
				}while(true);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exc1)
		{
			System.out.println("GET PICK RATE EXCEPTION////////");
		}
		return rate;
	}

	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
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

	private String getCurrdateInAppFormat()
	{
		String currAppdate =null;
		java.sql.Timestamp currDate = null;
		Object date = null;
		SimpleDateFormat DBDate=null;
		try
		{
				currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
				
			 	DBDate= new SimpleDateFormat(genericUtility.getDBDateFormat());
				date = DBDate.parse(currDate.toString());
				currDate =	java.sql.Timestamp.valueOf(DBDate.format(date).toString() + " 00:00:00.0");
				currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
			System.out.println("Exception in  getCurrdateInAppFormat:::"+e.getMessage());
		}
		return (currAppdate);
	}
	
	public String checkItem(String siteCodeShip,String itemCode,Connection conn) throws ITMException
	{
		System.out.println("In CheckForTtem siteCodeShip and itemCode"+	siteCodeShip+""+itemCode);
		String errCode = "",varValue = "",active = "";
		String sql = "",modName = "D-ORD";
		int count = 0;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		try
		{
			sql = "select active from item where item_code = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				active = rs.getString(1);
			}
			else
			{
				errCode = "VTITEM1";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(active == null && active.trim().length() == 0)
			{
				active="Y";
			}
			if(!active.equals("Y"))
			{
				if(modName.equals("S-RET") || modName.equals("SRFRM")  )
				{
					errCode = "VTITEM9";
				}
				else
				{
					errCode = "VTITEM4";
				}
			}

			sql = "select var_value  from disparm where prd_code = ? and var_name =	?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, "999999");
			pstmt.setString(2, "SITE_SPECIFIC_ITEM");
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				varValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(varValue.equals("Y"))
			{
				sql = "select case when active is null then 'Y' else active end from " +
						" siteitem where site_code = ? and item_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeShip);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					active = rs.getString(1);
				}
				else
				{
					if(modName.equalsIgnoreCase("D-ORD"))
					{
						errCode = "VTITEM3A";
					}
					else
					{
						errCode = "VTITEM3";
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(active.equals("N"))
				{
					errCode = "VTITEM4";}
			}
			else
			{
				sql = "select count(*)  from item where item_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					count = rs.getInt(1);
				}
				if(count==0)
				{
					errCode="VTITEM1";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errCode;
    }
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}	
}
