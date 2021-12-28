package ibase.webitm.ejb.dis;
/*
 * Request Id : D15ESUN013
 * Developer : Mahendra Jadhav
 * Functionality : Migrate Cust Stock Replenishment in GWT.
 * 
 * 
 */

import ibase.ejb.CommonDBAccessEJB;
import ibase.system.config.ConnDriver;
import ibase.utility.BaseException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;

import org.apache.xerces.dom.AttributeMap;
//import org.jboss.util.file.Files;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import ibase.utility.GenericUtility;

@Stateless 
public class CustStockGWTIC extends ValidatorEJB implements CustStockGWTICLocal,CustStockGWTICRemote //implements SessionBean
{
	GenLogFile genLogFile = null;
	private static String fileName ="";
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	UtilMethods utilMethod = new UtilMethods();
	//DistCommon discmn=new DistCommon();
	ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
    java.sql.Timestamp 	prdFrmDateTstmp =null;
    java.sql.Timestamp  prdtoDateTstmp =null;
    java.sql.Timestamp  prdtoDateTmstmp =null,prdFromoDateTmstmp=null;
    // static String custCodeStatic="";//Static commented by saurabh as suggested by Manoj Sir.-03/02/17
   /*String countryCode="",loginPositionCode="";
   String selectedInvList ="";
   String itemSerHeaderSplit="";
   String orderTypeHeader="";
   */
    String selectedInvList ="";
    int graceTransitDays=0;
  //boolean isItemSer = false;
    UtilMethods utlmethd=new UtilMethods();
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}
    public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
    
   
    
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			System.out.println("xmlString2>>>>>>>>"+xmlString2);
			genLogFile = new GenLogFile();
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			//genLogFile = null;
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	
	 public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
			NodeList parentNodeList = null;
			NodeList childNodeList = null;
			Node parentNode = null;
			String childNodeName = null;
			StringBuffer valueXmlString = new StringBuffer();
			
			String invoiceId="",itemCode="",period="";
			String siteCode = "",custCode="",userId="",sql="",tranId="",errCode="",errorType="",errString="",itemSer="",stmtDateStr="";
			String custType="",custType1="";
			String prdCodeDom="", posCodeDom="",itemSerDom="",tranIdLastDom="";
			String fromDateStr ="",custTypeDom="";
			String editSatus="" ,invCustCode="",custCodeXmlDataAll="";
			String salePer="",countryCode1="",empCode="",positionCode="";
			String invoiceDateStr="",dlvFlg="",transitGraceDaysStr="",positionCodeStr="";
			String sysDate="";
			int pohelpCnt=0,empCnt=0,transitGraceDays=0;
			int currentFormNo=0,cnt=0,childNodeListLength=0;
			int custstockCnt=0,custstockCnt1=0;
			double clStock=0.0;
			double opStock=0.0,purRcpQty=0.0,secSales=0.0,purcRetQty=0.0;
			java.sql.Timestamp FromDate=null,ToDate=null,stmtDateTmstp=null,fromDateTmstp=null;
			java.sql.Timestamp validUptoDate=null,regDateNew=null;
			 Timestamp dbSysDate= null;
			Boolean isOrdType = false;
			Boolean isClStockInt= true,isOpStockInt=true;
			Boolean isInvList=true;
			
		//	boolean dateFlag=false,dateFlagTodate=false,dateFlagFrmdate=false,dateFlagstmtDate=false;
			Node parentNode1 = null;
			Node childNode1 = null; 	
			int parentNodeListLength = 0;	
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			ConnDriver connDriver = new ConnDriver();
			Date currentDate = new Date();
		//	ArrayList<String> refNoList = new ArrayList<String>();
			Date currentDateval = new Date();
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errItemList = new ArrayList<String>();
			ArrayList<String> salNegitiveErrItemList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();
			ArrayList<String> errInvList = new ArrayList<String>();
			ArrayList<String> positionList = new ArrayList<String>();
			StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
			String isPrdClosed="",dummyProduct="";
			//Double rate=0.0;String rateDom="";
			String loginPositionCode="";
			//int selectedRowRate=0; 
			String  tranIdLast ="";
			java.sql.Timestamp toDateLast=null;
			int admCnt=0;String isAdmin="",itemSerChk="",fnlItmSer="";//Added by saurabh for admin login [02/02/17]
			try
			{
				System.out.println("CustStockGWTIC : wfValData called!!!!");
				SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
				sysDate = sdf2.format(currentDateval.getTime());
				dbSysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				System.out.println("dbSysDate>>>>>>>"+dbSysDate);
				//sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				DistCommon distCommon = new DistCommon();
				userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				//loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
				//loginEmpCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
				System.out.println("userId :"+userId);
				if ((objContext != null) && (objContext.trim().length() > 0))
				{
					currentFormNo = Integer.parseInt(objContext);
				}
				valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
				valueXmlString.append(editFlag).append("</editFlag> </header>");
				switch (currentFormNo)
				{
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("wfValData : case 1 called!!!!");
					ArrayList<String> filter = new ArrayList<String>();
					filter.add(0, "edit_status");
					filter.add(1, "pos_code");
					filter.add(2, "emp_code");
					filter.add(3, "cust_code");
					filter.add(4, "item_ser");
					//filter.add(5, "order_type");//qwerty
					filter.add(5, "cust_type");
					filter.add(6, "prd_code");
					filter.add(7, "stmt_date");
					for (int fld = 0; fld < filter.size(); fld++)
					//for (ctr = 0; ctr < childNodeListLength; ctr++)
					{
						//childNode = childNodeList.item(ctr);
						//childNodeName = childNode.getNodeName();
						childNodeName = (String) filter.get(fld);
						System.out.println("childNodeName :"+childNodeName);
						if (childNodeName.equalsIgnoreCase("edit_status"))
						{
							editSatus = this.genericUtility.getColumnValue("edit_status", dom);
							if("V".equalsIgnoreCase(editSatus))
							{
								System.out.println("validation stop");
								break;
							}
						}
						if (childNodeName.equalsIgnoreCase("cust_code"))
						{
							custCode = this.genericUtility.getColumnValue("cust_code", dom);
							System.out.println("custCode :"+custCode);
							siteCode = this.genericUtility.getColumnValue("site_code", dom);
							System.out.println("siteCode@@@@@@@ :"+siteCode);
							prdCodeDom = checkNull(this.genericUtility.getColumnValue("prd_code", dom));
							posCodeDom = checkNull(this.genericUtility.getColumnValue("pos_code", dom));
							itemSer = checkNull(this.genericUtility.getColumnValue("item_ser", dom));
							tranIdLastDom = checkNull(this.genericUtility.getColumnValue("tran_id__last", dom));
							System.out.println("prdCodeDom@@@@@@@ :["+prdCodeDom+"][posCodeDom]["+posCodeDom+"][itemSerDom]["+itemSer+"][tranIdLastDom]["+tranIdLastDom+"]");		
							 if(custCode == null)
							{
								System.out.println("Error : customer code should not be blank!!!");
								errCode = "NULSTRGCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(custCode != null && custCode.trim().length() > 0 )
							{
								
								sql = "select count(*) from customer where cust_code=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								if (cnt == 0)
								{
									errCode = "VTCUST1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								//Commented by saurabh as suggested by Bhavesh[04/01/17]
								/*sql = "SELECT COUNT(1) COUNT FROM CUSTOMER WHERE CUST_CODE = ? And" +
										" (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END ) = 'N'";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								if (cnt == 0)
								{
									System.out.println("Error :Customer Should not be channel partner!! ");
									errCode = "VTCUST2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								*/
								//Commented by saurabh as suggested by Bhavesh[04/01/17]
								if(siteCode == null || siteCode.trim().length()==0)
								{
										System.out.println("Error : Site code should exist in site_customer!!!");
										errCode = "NULSITECD";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
								}
								/*
								 * Modified by santosh to check tran_id_last is present .START
								 * */
								
								sql = " SELECT max(to_date) as to_date FROM CUST_STOCK WHERE CUST_CODE = ?  " +
										" AND ITEM_SER = ? " +
										" and pos_code is not null and confirmed='Y' and (status='X'  OR status='S')";//changes done for status s
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemSer);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									toDateLast = rs.getTimestamp("to_date");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								fileName= custCode.trim()+"_"+itemSer+"_"+posCodeDom+"_"+prdCodeDom;
								
//								genLogFile.writeLog(custCode.trim()+"_"+itemSer+"_"+posCodeDom+"_"+prdCodeDom, "in wfValData() case 1 ["+sql+"]");
//								genLogFile.writeLog(custCode.trim()+"_"+itemSer+"_"+posCodeDom+"_"+prdCodeDom, "in wfValData() case 1 custCode ["+custCode+"][itemSer]["+itemSer+"]");
								System.out.println("genLogFile in wfvaldata"+genLogFile);
								genLogFile.writeLog(fileName, "in wfValData() case 1 ["+sql+"]");
								genLogFile.writeLog(fileName, "in wfValData() case 1 custCode ["+custCode+"][itemSer]["+itemSer+"]");
								
	sql = " SELECT max(tran_id) as oldTranId FROM CUST_STOCK WHERE CUST_CODE = ?  " +
										" AND ITEM_SER = ? " +
										" and pos_code is not null and confirmed='Y' and (status='X' OR STATUS='S') and to_date=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemSer);
								pstmt.setTimestamp(3, toDateLast);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									tranIdLast = checkNull(rs.getString("oldTranId"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("tranIdLast (case 1):" + tranIdLast);
								genLogFile.writeLog(fileName, "in wfValData() case 1 ["+sql+"]");
								genLogFile.writeLog(fileName, "in wfValData() case 1 custCode ["+custCode+"][itemSer]["+itemSer+"][toDateLast]["+toDateLast+"]");
								if(tranIdLast != null && tranIdLastDom == null)
									{
									System.out.println("Error : tran_id__last is present in SQL");
									errCode = "VTTRANIDMI";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									}
								/*
								 * Modified by santosh to check tran_id_last is present .END
								 * */
								
								
							}//cust code
								

						}
						if (childNodeName.equalsIgnoreCase("item_ser"))
						{
							System.out.println("item_ser called!!!!!!");
							itemSer = this.genericUtility.getColumnValue("item_ser", dom);
							System.out.println("itemSer :"+itemSer);
							custCode = checkNull(this.genericUtility.getColumnValue("cust_code", dom));
							System.out.println("custCode :"+custCode);
							if(itemSer == null)
							{
								errCode = "VMNULLSER";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							/*else if(itemSer != null && itemSer.trim().length() > 0 )
							{
								sql = "select count(*) from customer_series where item_ser =? and cust_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemSer.trim());
								pstmt.setString(2,custCode.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								if (cnt == 0)
								{
									errCode = "VMINVITSER";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								
							}//cust code
*/
						}
						
						//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|Start]
						/*if(childNodeName.equalsIgnoreCase("order_type"))//qwerty
						{
							System.out.println("order_type called!!!!");
							orderType = this.genericUtility.getColumnValue("order_type", dom);
							System.out.println("orderType :"+orderType);
							custCode = this.genericUtility.getColumnValue("cust_code", dom);
							System.out.println("custCode :"+custCode);
							itemSer = this.genericUtility.getColumnValue("item_ser", dom);
							System.out.println("itemSer :"+itemSer);
							if(orderType == null)
							{
								errCode = "VMNULLORD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = "select count(*) from sordertype where order_type=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,orderType.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								if (cnt == 0)
								{
									System.out.println("Error :Order tpte should be exist in sordertype master ");
									errCode = "VTINVORD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}*/
						//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|End]
						
						//Start added by chandra shekar on 26-10-2015
						if(childNodeName.equalsIgnoreCase("cust_type"))
						{
							System.out.println("cust_type called!!!!");
							custTypeDom = this.genericUtility.getColumnValue("cust_type", dom);
							custType = distCommon.getDisparams("999999", "CUST_TYPE", conn);
							System.out.println("custType>>>>>>>"+custType);
							if (custType == null || "NULLFOUND".equalsIgnoreCase(custType)|| custType.trim().length()==0)
							{
								custType = "";
								errCode = "VTDISPMNUL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}else if (custType != null && (custType.trim().length() > 0)
									  && custTypeDom != null && custTypeDom.trim().length() > 0)
							{
								String[] arrStr = custType.split(",");
								int len = arrStr.length;
								for (int i = 0; i < len; i++)
								{
									custType1 = arrStr[i];
									if (custTypeDom.trim().equalsIgnoreCase(custType1.trim()))
									{
										isOrdType = true;
									}
								}
								if (!isOrdType)
								{
									errCode = "VTDISCUST";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						//End added by chandra shekar on 26-10-2015
						if(childNodeName.equalsIgnoreCase("prd_code"))
						{
							System.out.println("period called!!!!");
							period = this.genericUtility.getColumnValue("prd_code", dom);
							System.out.println("period :"+period);
							//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|Start]
							//orderType = this.genericUtility.getColumnValue("order_type", dom);//qwerty
							//System.out.println("orderType :"+orderType);
							//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|End]
							custCode = this.genericUtility.getColumnValue("cust_code", dom);
							System.out.println("custCode :"+custCode);
							itemSer = this.genericUtility.getColumnValue("item_ser", dom);
							System.out.println("itemSer :"+itemSer);
							positionCode = this.genericUtility.getColumnValue("pos_code", dom);
							System.out.println("positionCode :"+positionCode);
							
							
							salePer = this.genericUtility.getColumnValue("sale_per", dom);
							System.out.println("salePer :"+salePer);
							countryCode1 = this.genericUtility.getColumnValue("country_code", dom);
							System.out.println("countryCode1 :"+countryCode1);
							
							
							tranId = this.genericUtility.getColumnValue("tran_id", dom);
							System.out.println("tranId@@@@@@@"+tranId);
							editSatus = this.genericUtility.getColumnValue("edit_status", dom);
							System.out.println("editSatus@@@@@@@"+editSatus);
							System.out.println("itemSer :"+itemSer);
							if(period == null)
							{
								errCode = "VMNULLPRD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(itemSer != null)
							{
								//sql = "select count(*) from period_tbl where prd_code=? ";
								sql = "select count(*) from period_appl a,period_tbl b " +
										"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
										//"and a.site_code=? " +
										" AND b.prd_code = ? " +
										"and b.prd_tblno=? " +
										"AND case when a.type is null then 'X' else a.type end='S' ";
								pstmt = conn.prepareStatement(sql);
								//pstmt.setString(1,loginSiteCode.trim());
								pstmt.setString(1,period.trim());
								pstmt.setString(2,countryCode1+"_"+itemSer.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0)
								{
									System.out.println("Error :Period not exist in period_tbl master ");
									errCode = "VMINVPRDTB";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}else
								{
									/*sql= " select FR_DATE,TO_DATE from period_tbl  where  "
											+ "prd_code = ? ";*/
									sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
											",b.entry_start_dt as entry_start_dt" +
											",b.entry_end_dt as entry_end_dt ,b.prd_closed" +//Added by saurabh-15/12/16
											" from period_appl a,period_tbl b " +
											"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
											//"and a.site_code=? " +
											" AND b.prd_code = ? " +
											"and b.prd_tblno=? " +
											"AND case when a.type is null then 'X' else a.type end='S' ";
									pstmt = conn.prepareStatement(sql);
									//pstmt.setString(1,loginSiteCode.trim());
									pstmt.setString(1,period.trim());
									pstmt.setString(2,countryCode1+"_"+itemSer.trim());	
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										FromDate = rs.getTimestamp("FR_DATE"); 
										ToDate = rs.getTimestamp("TO_DATE"); 
										//entryStartDt = rs.getTimestamp("entry_start_dt");
										//entryEndDt = rs.getTimestamp("entry_end_dt");//
										isPrdClosed = rs.getString("prd_closed");//Added by saurabh-15/12/16
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									//Added by saurabh-15/12/16
									if("Y".equalsIgnoreCase(isPrdClosed))
									{
										errCode = "VMPRDCLOSE";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									//Added by saurabh-15/12/16
									if(!"V".equalsIgnoreCase(editSatus))
									{
										/*sql = "select count(*) from period_appl a,period_tbl b " +
												"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
												//"and a.site_code=? " +
												"AND b.prd_code = ? " +
												"and b.prd_tblno=? " +
												"AND case when a.type is null then 'X' else a.type end='S'  " +
												" and ?  between entry_start_dt and entry_end_dt";
										pstmt = conn.prepareStatement(sql);
										//pstmt.setString(1,loginSiteCode.trim());
										pstmt.setString(1,period.trim());
										pstmt.setString(2,countryCode1+"_"+itemSer.trim());
										pstmt.setTimestamp(3, dbSysDate);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											entryCnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;*/
										/*if(entryCnt == 0)
										{
											System.out.println("Error :Period not exist in period_tbl master start and end date ");
											errCode = "VMINVPRDST";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}*/
									//Added by azhar[24/10/16|Start]
									/*sql = "select count(*) from cust_stock where status='S' " +
											"and cust_code=? and from_date=? and to_date =?";*/
									sql = "select count(*) from cust_stock where status='S' " +
											"and cust_code=? and from_date=? and to_date =? and item_ser = ? and pos_code is not null ";//Added by saurabh[20/12/16]
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									pstmt.setTimestamp(2,FromDate);
									pstmt.setTimestamp(3,ToDate);
									pstmt.setString(4, itemSer);
									//Added by azhar[24/10/16|End]
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										custstockCnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if (custstockCnt > 0)
									{
										System.out.println("Error :cust_stock already submitted ");
										errCode = "VMINVPRDSU";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									
									}
									
									if("A".equalsIgnoreCase(editSatus))
									{
										//Added by Azhar[25/10/16|Start]
										//sql = "select count(*) from cust_stock where  " +
										//		" cust_code=? and from_date=? and to_date =?";
										
										
										sql = "select count(*) from cust_stock where  " +
												" cust_code=? and from_date=? and to_date =? and item_ser = ? and status<>'X' and pos_code is not null ";//Added by saurabh[20/12/16]
										//CHANGES DONE BY SANGITA FOR STATUS X 3/DEC/20
										/*sql = "select count(*) from cust_stock where  " +
												" cust_code=? and from_date=? and to_date =? and item_ser = ? and pos_code is not null ";//Added by saurabh[20/12/16]
*/										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,custCode);
										pstmt.setTimestamp(2,FromDate);
										pstmt.setTimestamp(3,ToDate);
										pstmt.setString(4, itemSer);
										//Added by Azhar[25/10/16|End]
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											custstockCnt1 = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if (custstockCnt1 > 0)
										{
											System.out.println("Error :cust_stock already exist in same period ");
											errCode = "VMINVPRDCU";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}else
										{
											//qwerty
										//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|Start]	
										sql="select count(*)  from org_structure org,org_structure_cust orgcust,customer cust ," +
												" station stn," +
												//" customer_series custser, " +
												" state state  where org.version_id=orgcust.version_id  " +
												" and org.pos_code=orgcust.pos_code and org.table_no=orgcust.table_no  " +
												" and orgcust.cust_code=cust.cust_code  and cust.stan_code=stn.stan_code" +
												//" and custser.cust_code=cust.cust_code "+//and case when custser.black_listed is null then 'N' else custser.black_listed end !='Y'  " +01/12/16
												//"and custser.item_ser=org.table_no  " +
												" AND orgcust.pos_code =?  " +
												" AND (case when orgcust.source is null then 'Y' else orgcust.source end) <> 'A' " +//Added by saurabh for avacs[04/02/17]
												"AND org.table_no =?  " +
												"and org.version_id = (select FN_GET_VERSION_ID from dual)  " +
												" AND (cust.SH_NAME LIKE upper(?)  or cust.cust_code LIKE upper(?) " +
												"OR cust.cust_name LIKE upper(?))  " +
												"and state.count_code=? " +
												"and state.state_code=stn.state_code  " +
												"and ( ?  BETWEEN orgcust.EFF_DATE and  orgcust.VALID_UPTO )"+
												"and ( ?  BETWEEN orgcust.EFF_DATE and  orgcust.VALID_UPTO ) "+
												/*"and custser.item_ser in(select item_ser from itemser where grp_code=? and item_ser<>case when grp_code is null then '.' else grp_code end " +
												"union all select item_ser from itemser where item_ser=? and item_ser<>case when grp_code is null then '.' else grp_code end) "+*/
												"order by stn.descr asc";
										//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|End]
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,positionCode);
										pstmt.setString(2,itemSer);
										pstmt.setString(3,custCode+"%");
										pstmt.setString(4,custCode+"%");
										pstmt.setString(5,custCode+"%");
										pstmt.setString(6,countryCode1);
										pstmt.setTimestamp(7,FromDate);
										pstmt.setTimestamp(8,ToDate);
										/*pstmt.setString(9,itemSer);
										pstmt.setString(10,itemSer);*/
										rs = pstmt.executeQuery();	
											
									/*	sql="select count(*)  " +
												"from org_structure org,org_structure_cust orgcust,customer cust ,station stn,customer_series custser," +
												"state state where org.version_id=orgcust.version_id and " +
												"org.pos_code=orgcust.pos_code and org.table_no=orgcust.table_no and " +
												"orgcust.cust_code=cust.cust_code and cust.stan_code=stn.stan_code" +
												"  and custser.cust_code=cust.cust_code and custser.black_listed !='Y' " +
												"and custser.item_ser=(select item_ser from department where dept_code in " +
												"(select dept_code from employee where emp_code=?)) " +
												"and orgcust.table_no=(select item_ser from department where dept_code in " +
												"(select dept_code from employee where emp_code=?)) " +
												"AND orgcust.pos_code in(SELECT pos_code FROM employee WHERE emp_code= ?) " +
												"  and state.count_code=? and cust.cust_code=? " +
												"and state.state_code=stn.state_code ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,salePer);
										pstmt.setString(2,salePer);
										pstmt.setString(3,salePer);
										pstmt.setString(4,countryCode1);
										pstmt.setString(5,custCode);
										rs = pstmt.executeQuery();*/
										if (rs.next())
										{
											pohelpCnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if (pohelpCnt == 0)
										{
											System.out.println("customer is not exist in org customer");
											errCode = "VMINVORGCU";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										}
									}
									
								}
								
							}
						}
						if(childNodeName.equalsIgnoreCase("stmt_date"))
						{
							stmtDateStr = this.genericUtility.getColumnValue("stmt_date",dom);
							fromDateStr = this.genericUtility.getColumnValue("from_date",dom);
							System.out.println("stmtDateStr>>>>"+stmtDateStr+">>>fromDateStr>>>"+fromDateStr);
							if(stmtDateStr == null)
							{
								System.out.println("Error :Statement date should not be blank!!! ");
								errCode = "VTEMPSTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if(stmtDateStr != null && fromDateStr != null)
							{
								stmtDateStr = genericUtility.getValidDateString( stmtDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
								stmtDateTmstp = java.sql.Timestamp.valueOf( stmtDateStr + " 00:00:00.0" );
								fromDateStr = genericUtility.getValidDateString( fromDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
								fromDateTmstp = java.sql.Timestamp.valueOf( fromDateStr + " 00:00:00.0" );
								//stmtDateTmstp = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(stmtDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								//fromDateTmstp = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								//Added by saurabh as per CCF [IT2016-140] [03/05/17|Start]
								if(fromDateTmstp.after(stmtDateTmstp))
								{
									System.out.println("fromDateTmstp > stmtDateTmstp : "+fromDateTmstp+" > "+stmtDateTmstp);
									errCode = "VTINVSTDTF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}else if(currentDate.before(stmtDateTmstp))
								{
									System.out.println("stmtDateTmstp < currentDate : "+stmtDateTmstp+" < "+currentDate);
									errCode = "VTINVEFFDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//Added by saurabh as per CCF [IT2016-140] [03/05/17|End]
								/*if(currentDate.before(stmtDateTmstp)) 
								{
									System.out.println("stmtDateTmstp < currentDate : "+stmtDateTmstp+" < "+currentDate);
									errCode = "VTINVEFFDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}*/
							}
							System.out.println("stmtDateTmstp :"+stmtDateTmstp);
							System.out.println("currentDate :"+currentDate);
							
						}
						if(childNodeName.equalsIgnoreCase("emp_code"))
						{
							empCode = this.genericUtility.getColumnValue("emp_code",dom);
							if(empCode == null || empCode.trim().length()==0)
							{
								/*errCode = "VTEMPCDNUL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());*/
							}
							else
							{
								sql = "select count(*) from employee where emp_code=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,empCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									empCnt = rs.getInt(1);
								}
								if (empCnt == 0)
								{
									errCode = "VTEMPCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							
							
						}
						if(childNodeName.equalsIgnoreCase("pos_code"))
						{
							//positionCode = this.genericUtility.getColumnValue("pos_code",dom);
							/*loginEmpCode = this.genericUtility.getColumnValue("emp_code",dom);
							sql = "select pos_code from employee where emp_code=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,loginEmpCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								loginPosCode = checkNull(rs.getString("pos_code")).trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;*/
							//Added by saurabh for admin login [02/02/17|Start]
							isAdmin = checkNull(this.genericUtility.getColumnValue("adm_chk",dom));//To check wheather admin login--Y-Yes/N-No
							System.out.println("isAdmin>>>>>"+isAdmin);
							System.out.println("loginPositionCode before>>>"+loginPositionCode);
							loginPositionCode = this.genericUtility.getColumnValue("login_poscode",dom);
							System.out.println("loginPositionCode after>>>"+loginPositionCode);
							positionCode = checkNull(this.genericUtility.getColumnValue("pos_code",dom));
							//Added by saurabh for admin login [02/02/17|Start]
							if("Y".equalsIgnoreCase(isAdmin))//For admin login below validation will run
							{
								itemSerChk=dist.getDisparams("999999","CAL_CRIT_ITEMSER",conn);
								if(itemSerChk.trim().length()>0)
								{
									//added by Saurabh to get item series of invoice[03/11/16|Start]
									StringBuffer strBuffer1=new StringBuffer();
									ArrayList<String> admItemSerList= new ArrayList<String>(Arrays.asList(itemSerChk.split(",")));
									for(int i=0,cntChk=0;i<admItemSerList.size();i++)
									{
										String calCriItemSerStrChk= (String) admItemSerList.get(i);
									    System.out.println("calCriItemSerStrChk -->"+calCriItemSerStrChk.trim());
									    if(calCriItemSerStrChk.trim().length()>0)
									    {
									    	String calCriItemSerStrFnl="'"+calCriItemSerStrChk.trim()+"'";
									    	strBuffer1.append(cntChk > 1? calCriItemSerStrFnl.trim():calCriItemSerStrFnl.trim()+",");
									    }
									}
									fnlItmSer=strBuffer1.substring(0, strBuffer1.lastIndexOf(","));
									System.out.println("fnlItmSer>>>>>"+fnlItmSer);
								}
								sql="SELECT count(1) as count from org_structure where version_id =(select fn_get_version_id from dual)" +
										" and table_no NOT IN("+fnlItmSer+") and pos_code=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,positionCode);
								rs = pstmt.executeQuery();
								while (rs.next())
								{
									admCnt = rs.getInt("count");
								}
								rs.close();
								rs=null;
								pstmt.close();
								pstmt=null;
								if(admCnt==0)
								{
									errCode = "VTINVPSCDE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
							}
							else //For other login below validation will run
							{
								sql ="SELECT trim(A.POS_CODE) as pos_code FROM ORG_STRUCTURE A ,EMPLOYEE C ,ORG_STRUCTURE_CUST B " +
										" WHERE A.VERSION_ID=B.VERSION_ID AND A.TABLE_NO=B.TABLE_NO AND A.POS_CODE=B.POS_CODE AND " +//ADDED BY SAURABH [07/02/17]
										" A.VERSION_ID =(select FN_GET_VERSION_ID from dual) AND (CASE WHEN B.SOURCE IS NULL THEN 'Y' ELSE B.SOURCE END) <> 'A' " +//ADDED BY SAURABH [07/02/17]
										//"AND A.EMP_CODE(+)=C.EMP_CODE  " +
										"AND A.EMP_CODE = C.EMP_CODE(+)  " +//vaccant employee display
										"AND A.POS_CODE IN(SELECT C.POS_CODE FROM ORG_STRUCTURE C START WITH C.POS_CODE=?  " +
										" CONNECT BY PRIOR C.POS_CODE=C.POS_CODE__REPTO  " +
										"AND C.VERSION_ID =(select FN_GET_VERSION_ID from dual) " +
										" ) ORDER BY LEVEL_NO";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,loginPositionCode);
								rs = pstmt.executeQuery();
								while (rs.next())
								{
									positionCodeStr = checkNull(rs.getString("pos_code")).trim();
									positionList.add(positionCodeStr);
								}
								rs.close();
								rs=null;
								pstmt.close();
								pstmt=null;
								System.out.println("positionCode@@@@@@@@["+positionCode+"]");
								System.out.println("positionList>>>>>>"+positionList);
								if(!positionList.contains(positionCode.trim()))
								{
									System.out.println("position code is not exist in ORG_STRUCTURE");
									errCode = "VTPOSCODE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//Added by saurabh for admin login [02/02/17|end]
						}
										
					}
					break;
				/*	
				case 2:		
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("wfValData : case 2 called!!!!");
					for (ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName in case 2:"+childNodeName);
						if (childNodeName.equalsIgnoreCase("stmt_date"))
						{
							stmtDateStr = this.genericUtility.getColumnValue("stmt_date",dom);
							System.out.println("stmtDateStr :"+stmtDateStr);
							fromDateStr = this.genericUtility.getColumnValue("from_date",dom);
							System.out.println("fromDateStr :"+fromDateStr);
							toDateStr = this.genericUtility.getColumnValue("to_date",dom);
							System.out.println("toDateStr :"+toDateStr);
							orderType = this.genericUtility.getColumnValue("order_type", dom1);
							System.out.println("orderType :"+orderType);
							custCode = this.genericUtility.getColumnValue("cust_code", dom1);
							System.out.println("custCode :"+custCode);
							itemSer = this.genericUtility.getColumnValue("item_ser", dom1);
							
						
							if(fromDateStr != null)
							{
								FromDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}
							
							if(toDateStr != null)
							{
								ToDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}
							
							tranId = checkNull(this.genericUtility.getColumnValue("tran_id",dom));
							System.out.println("tranId :"+tranId);
							
							sql = "select status from cust_stock where tran_id= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,tranId.trim());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								statusCust = checkNull(rs.getString("status"));
								System.out.println("statusCust :"+statusCust);
							}
							if (statusCust.equals("Y"))
							{
								System.out.println("Error :Transcation cant be edited!!!!! ");
								errCode = "VTINVEDI";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (childNodeName.equalsIgnoreCase("site_code"))
						{
							custCode = this.genericUtility.getColumnValue("cust_code", dom1);
							System.out.println("custCode :"+custCode);
							siteCode = this.genericUtility.getColumnValue("site_code", dom1);
							System.out.println("siteCode :"+siteCode);
							
							
							
							sql = "SELECT count(*) as count FROM SITE_CUSTOMER WHERE" +
									" CUST_CODE = ? AND SITE_CODE = ? AND NVL(ACTIVE_YN,'Y') = 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode.trim());
							pstmt.setString(2,siteCode.trim());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								siteCnt = rs.getInt("count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("siteCnt :"+siteCnt);
							if(siteCnt == 0)
							{
								System.out.println("Error :Record not found for this Customer and Site in CUSTOMER_SERIES.");
								errCode = "VTSITECUST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						
						}
						
						
					}	
					break;*/
					
				case 2:
					parentNodeList = dom2.getElementsByTagName("Detail2");
					parentNodeListLength = parentNodeList.getLength(); 
					System.out.println("parentNodeListLength Detail2:::::::::"+parentNodeListLength);							
					stmtDateStr = this.genericUtility.getColumnValue("stmt_date",dom2);
					editSatus = this.genericUtility.getColumnValue("edit_status", dom2);
					String ustCodeXmlDataAll1 = genericUtility.getColumnValue("cust_code", dom1);
					System.out.println("@S@custCode from xmldata1(wfvaldatacase 2) :"+ustCodeXmlDataAll1);
					custCodeXmlDataAll = genericUtility.getColumnValue("cust_code", dom2);
					System.out.println("@S@custCodeXmlDataAll(wfvaldatacase 2) :"+custCodeXmlDataAll);
					System.out.println("editSatus>>>>>case 2>>>"+editSatus);
					if(!"V".equalsIgnoreCase(editSatus))
					{
					Timestamp stmtDate = Timestamp.valueOf(genericUtility.getValidDateString(stmtDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					transitGraceDaysStr = dist.getDisparams("999999", "GRACE_DAYS_TRINV", conn);
					System.out.println("transitGraceDays.." + transitGraceDaysStr);
					for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{
						parentNode1 = parentNodeList.item(selectedRow);
						childNodeList = parentNode1.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						System.out.println("childNodeListLength::: " + childNodeListLength + "\n");
						isInvList = true;
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{
							invCustCode="";
							childNode1 = childNodeList.item(childRow);
							childNodeName = childNode1.getNodeName();
							System.out.println("childNodeName :" + childNodeName);
							if (childNodeName.equals("invoice_id"))
							{
								if (childNode1.getFirstChild() != null)
								{
									invoiceId = childNode1.getFirstChild().getNodeValue();
									/**Added by santosh to check invoice customer .START*/
									System.out.println("@S@invoiceId["+invoiceId+"]");
									sql="select cust_code from invoice where invoice_id= ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,invoiceId);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										invCustCode = rs.getString("cust_code");
										
									}
									rs.close();
									rs=null;
									pstmt.close();
									pstmt=null;
									System.out.println("@S@invCustCode["+invCustCode+"]");
									if(invCustCode.trim().length()==0)
									{
										sql="select cust_code from sreturn where tran_id = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,invoiceId);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											invCustCode = rs.getString("cust_code");
											
										}
										rs.close();
										rs=null;
										pstmt.close();
										pstmt=null;
									}
									if(!(invCustCode.trim()).equals(custCodeXmlDataAll.trim()))
									{
										/**Invoice customer code not match 
										 * with site customer code**/
										errCode = "VTICUSTCHG";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									/**Added by santosh to check invoice customer .END*/
								}
								System.out.println("invoiceId :" + invoiceId);
								if (isInvList == false && !errInvList.contains(invoiceId))
								{
									System.out.println("Add invoiceId>>>>>>>" + invoiceId);
									errInvList.add(invoiceId.trim());
								}
							}
							if (childNodeName.equals("dlv_flg"))
							{
								if (childNode1.getFirstChild() != null)
								{
									dlvFlg = childNode1.getFirstChild().getNodeValue();
								}
								System.out.println("dlvFlg :" + dlvFlg);
								//added by saurabh for invoice Receipt/Transit selection on basis of disparm applicable for domestic only [27/06/17|Start]
								if("S".equalsIgnoreCase(dlvFlg))
								{
									errCode = "VTINVSRT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//added by saurabh for invoice Receipt/Transit selection on basis of disparm applicable for domestic only [27/06/17|End]
							}
							if (childNodeName.equals("invoice_date"))
							{
								if (childNode1.getFirstChild() != null)
								{
									invoiceDateStr = childNode1.getFirstChild().getNodeValue();
								}
								System.out.println("invoiceDateStr :" + invoiceDateStr);

								if (invoiceDateStr != null && invoiceDateStr.trim().length() > 0)
								{
									validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(invoiceDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
									if (("NULLFOUND".equalsIgnoreCase(transitGraceDaysStr) || transitGraceDaysStr == null || transitGraceDaysStr.trim().length() == 0) && "N".equalsIgnoreCase(dlvFlg))
									{
										errCode = "VTGRCDAYS";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									} else if ("N".equalsIgnoreCase(dlvFlg))
									{
										transitGraceDays = Integer.parseInt(transitGraceDaysStr);
										graceTransitDays = Integer.parseInt(transitGraceDaysStr);
										regDateNew = utilMethod.RelativeDate(stmtDate, -transitGraceDays);
										if (validUptoDate.before(regDateNew))
										{
											errCode = "VTINVOCDT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											isInvList = false;
										}
									}
								}

							}

						}
					}
				}
					
					break;
					
				case 3:
					parentNodeList = dom2.getElementsByTagName("Detail3");
					parentNodeListLength = parentNodeList.getLength(); 
					System.out.println("parentNodeListLength Detail3:::::::::"+parentNodeListLength);							
					for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{	
						//selectedRowRate=selectedRow+1;
						parentNode1 = parentNodeList.item(selectedRow);
						childNodeList = parentNode1.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						System.out.println("childNodeListLength::: "+ childNodeListLength+"\n");
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{
							childNode1 = childNodeList.item(childRow);
							childNodeName = childNode1.getNodeName();
							System.out.println("childNodeName :"+childNodeName);
							//Added by saurabh[23/12/16]
							if(childNodeName.equals("attribute"))
							{
								String updateFlag = "";
								updateFlag = childNode1.getAttributes().getNamedItem("updateFlag").getNodeValue();
								System.out.println("updateFlag>>>"+updateFlag);
								if ("D".equalsIgnoreCase(updateFlag))
								{
									itemCode = this.genericUtility.getColumnValue("item_code", dom2);
									System.out.println("Break from here as the record is deleted");
									break;
								}
							}
							//Added by saurabh[23/12/16]
							if (childNodeName.equals("item_code"))
							{
								if(childNode1.getFirstChild()!=null)
								{
									itemCode = childNode1.getFirstChild().getNodeValue();
								}
								System.out.println("itemCode :"+itemCode);
								System.out.println("itemCode.trim().length() :"+itemCode);
								if(itemCode == null || itemCode.trim().length() == 0)
								//if(itemCode.trim().length() == 0)
								{
									System.out.println("Error : No data found in item details");
									errCode = "VTEMTITM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									errCode = isExist("item","item_code",itemCode,conn);
									if( "FALSE".equalsIgnoreCase(errCode ))		
									{
										errCode = "VMITMNOTEX";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										dummyProduct =  dist.getDisparams("999999","DUMMY_PRODUCT",conn);
										if (("NULLFOUND".equalsIgnoreCase(dummyProduct) || dummyProduct == null || dummyProduct.trim().length() == 0) )
										{
											System.out.println("Disparm not defined for dummy item!!!!");
											if("FALSE".equalsIgnoreCase(isFrequent(itemCode,conn)))
											{
												errCode = "VMFRITMCHK";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());	
											}
										}
										else
										{
											if(!dummyProduct.trim().equalsIgnoreCase(itemCode.trim())){
												if("FALSE".equalsIgnoreCase(isFrequent(itemCode,conn)))
												{
													errCode = "VMFRITMCHK";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());	
												}
												}
										}
										
									}
									
								System.out.println("isClStockInt>>>>>>"+isClStockInt);
								if(isClStockInt == false && !errItemList.contains(itemCode))
								{
									System.out.println("Add item>>>>>>>"+itemCode);
									errItemList.add(itemCode.trim());
								}
								
								}
							
							}
							
							
							if (childNodeName.equals("cl_stock"))
							{
								if(childNode1.getFirstChild()!=null &&  isValidDouble(childNode1.getFirstChild().getNodeValue()))
								{
									clStock = Double.parseDouble(childNode1.getFirstChild().getNodeValue());
								}
								else
								{
									clStock = 0;
								}
								System.out.println("clStock :"+clStock);
								if(childNode1.getFirstChild()!=null)
								{								
								isClStockInt= isValidNumber( childNode1.getFirstChild().getNodeValue());
								System.out.println("isClStockInt>>>>>>>>"+isClStockInt);
								System.out.println("itemCode>>>>>>"+itemCode);
								if(isClStockInt == false)
								{
									errCode = "VTCLSINT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("Closing Stock not be Integer");	
								}
								}
								if(clStock < 0)
							    {
									 	errCode = "VTCLSTVLD";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("Closing Stock not be valid");
										
							    } 
							}
							
							if (childNodeName.equals("op_stock"))
							{
								if(childNode1.getFirstChild()!=null && isValidDouble(childNode1.getFirstChild().getNodeValue()))//Added by saurabh[19/12/16]
								{
									opStock = Double.parseDouble(childNode1.getFirstChild().getNodeValue());
								}
								else
								{
									opStock = 0;
								}
								System.out.println("opStock :"+opStock);
								//Added by saurabh [19/12/16|Start]
								if(childNode1.getFirstChild()!=null)
								{								
								isOpStockInt= isValidNumber( childNode1.getFirstChild().getNodeValue());
								System.out.println("isOpStockInt>>>>>>>>"+isOpStockInt);
								System.out.println("itemCode>>>>>>"+itemCode);
								if(isOpStockInt == false)
								{
									errCode = "VTOPSINT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("Opening Stock not be Integer");	
								}
								}
								//Added by saurabh [19/12/16|End]
								if(opStock < 0)
							    {
									 	errCode = "VTOPSTVLD";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("Opening Stock not be valid");
							    }
								
							}
							
							if (childNodeName.equals("purc_rcp"))
							{
								if(childNode1.getFirstChild()!=null)
								{
									purRcpQty = Double.parseDouble(childNode1.getFirstChild().getNodeValue());
								}
								else
								{
									purRcpQty = 0.0;
								}
								System.out.println("purRcpQty :"+purRcpQty);
								if(purRcpQty < 0)
							    {
									 	errCode = "VTRCPTVLD";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("purchase receipt not valid");
							    }
								
							}
							
							if (childNodeName.equals("sales"))
							{
								if(childNode1.getFirstChild()!=null)
								{
									secSales = Double.parseDouble(childNode1.getFirstChild().getNodeValue());
								}
								else
								{
									secSales = 0.0;
								}
								System.out.println("secSales :"+secSales);
								if(secSales < 0)
								{
									//isSecSalesNeg=true;
									System.out.println("itemCode>>>"+itemCode);
									if(!salNegitiveErrItemList.contains(itemCode))
									{
										System.out.println("salNegitiveErrItemList item>>>>>>>"+itemCode);
										salNegitiveErrItemList.add(itemCode.trim());
									}
								}
							}
							
							if (childNodeName.equals("purc_ret"))
							{
								if(childNode1.getFirstChild()!=null)
								{
									purcRetQty = Double.parseDouble(childNode1.getFirstChild().getNodeValue());
								}
								else
								{
									purcRetQty = 0.0;
								}
								System.out.println("purcRetQty :"+purcRetQty);
								if(purcRetQty < 0)
							    {
									 	errCode = "VTRETVLD";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("purchase return not be valid");
							    }
								
							}
							/*if (childNodeName.equals("primary_sales"))
							{
								if(childNode1.getFirstChild()!=null)
								{
									priSales = Double.parseDouble(childNode1.getFirstChild().getNodeValue());
								}
								else
								{
									priSales = 0.0;
								}
								System.out.println("priSales :"+priSales);
								if(priSales < 0)
							    {
									 	errCode = "VTPRISVLD";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("purchase return not be valid");
							    }
								
							}*/
					}
					}
					break;
				}
				if(salNegitiveErrItemList.size()>0)
				{
					errCode = "VTSALNEG";
					errList.add(errCode);
					errFields.add(childNodeName.toLowerCase());
					System.out.println("Secondary sales is negitive");
				}
				int errListSize = errList.size();
				cnt = 0;
				String errFldName = null;
				if ((errList != null) && (errListSize > 0))
				{
					for (cnt = 0; cnt < errListSize; cnt++)
					{
						errCode = (String)errList.get(cnt);
						errFldName = (String)errFields.get(cnt);
						errString = getErrorString(errFldName, errCode, userId);
						errorType = errorType(conn, errCode);
						if(errInvList.size()>0 && errString.length() > 0)
						{
							 String begPart = errString.substring( 0, errString.indexOf("]]></description>") );
							  String mainStr="";
							    for(int i=0;i<errInvList.size();i++)
							    {
							    	mainStr=mainStr+ errInvList.get(i)+",";
							    }
							    
							    String endPart=errString.substring( errString.indexOf("]]></description>"), errString.length() );
							    mainStr=graceTransitDays+" for following Invoices "+mainStr.substring(0,mainStr.length()-1);
							    errString = begPart+mainStr +  endPart;
							
						}else if(errItemList.size()>0 && errString.length() > 0 )
						{
							 String begPart = errString.substring( 0, errString.indexOf("]]></description>") );
							  String mainStr="";
							    for(int i=0;i<errItemList.size();i++)
							    {
							    	mainStr=mainStr+ errItemList.get(i)+",";
							    }
							    
							    String endPart=errString.substring( errString.indexOf("]]></description>"), errString.length() );
							    mainStr=" for following Items "+mainStr.substring(0,mainStr.length()-1);
							    errString = begPart+mainStr +  endPart;
							
						}else if(salNegitiveErrItemList.size()>0 && errString.length() > 0 )
						{
							String begPart = errString.substring( 0, errString.indexOf("]]></description>") );
							  String mainStr="";
							    for(int i=0;i<salNegitiveErrItemList.size();i++)
							    {
							    	mainStr=mainStr+ salNegitiveErrItemList.get(i)+",";
							    }
							    
							    String endPart=errString.substring( errString.indexOf("]]></description>"), errString.length() );
							    mainStr=" for following Items "+mainStr.substring(0,mainStr.length()-1);
							    errString = begPart+mainStr +  endPart;
						}
						System.out.println("After errString@@@@@@@@@"+errString);
						if (errString.length() > 0)
						{
							String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 
									8, errString.indexOf("<trace>"));
							bifurErrString = bifurErrString + 
									errString.substring(errString.indexOf("</trace>") + 
											8, errString.indexOf("</Errors>"));
							
							errStringXml.append(bifurErrString);
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

			}
			catch (Exception e)
			{
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
				}
				catch (Exception d)
				{
					d.printStackTrace();
					throw new ITMException(d);
				}
			}
			errString = errStringXml.toString();
			return errString;
	}//wfValData
	
	//wfValData(Document dom) method ends here
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			System.out.println("xmlString itemChanged: " + xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("xmlString1 itemChanged :" + xmlString1);
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
        return valueXmlString;
	}
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			if (xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("xmlString itemChanged::" + xmlString);
			}
			if (xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1 itemChanged::" + xmlString1);
			}
			if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
				 System.out.println("xmlString2 itemChanged::"+xmlString2);
			 }
			//dom2=parseString(xmlString2);
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams,"",null);
		}
		catch(Exception e)
		{
			
			throw new ITMException(e);
		}
        return valueXmlString;
	}
	//Method override for external transaction generation process Added by saurabh[27/03/17|Start]
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams,String objName) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			if (xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("xmlString itemChanged::" + xmlString);
			}
			if (xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1 itemChanged::" + xmlString1);
			}
			if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
				 System.out.println("xmlString2 itemChanged::"+xmlString2);
			 }
			//dom2=parseString(xmlString2);
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams,objName,null);
		}
		catch(Exception e)
		{
			
			throw new ITMException(e);
		}
        return valueXmlString;
	}
	//Method override for external transaction generation process Added by saurabh[27/03/17|End]
	
	//Method override for external transaction generation process for AWACS Added by saurabh[24/07/17|Start]
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams,String objName,HashMap dataMap) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			genLogFile = new GenLogFile();
			if (xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("xmlString itemChanged::" + xmlString);
			}
			if (xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1 itemChanged::" + xmlString1);
			}
			if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
				 System.out.println("xmlString2 itemChanged::"+xmlString2);
			 }
			//dom2=parseString(xmlString2);
			
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams,objName,dataMap);
			//genLogFile = null;
		}
		catch(Exception e)
		{
			
			throw new ITMException(e);
		}
        return valueXmlString;
	}
	//Method override for external transaction generation process for AWACS Added by saurabh[24/07/17|End]
	
	//public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams,String objName,HashMap dataMap) throws RemoteException,ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1=null;
		String tranId="",orderType="",invInvoiceId="",invtranId="";
		String loginSite="",invoiceId="",clStockInp ="",dlvFlag="",custCode="",siteCode="",fromdate="",itemSer="",tranIdLast="";
		String todate="",sql="",descrItem="";
		String chgDate="",chgUser="",loginEmpCode="",chgTerm="",awcsDlvFlag="";
		String custCodeHd ="",itemSerHd = "" ,siteCodeHd ="",itemCode="",unitItem="";
		String lsOpStock="",lsTranId="",lsrateold="",lsrateorgold="";
		String locTypeItem = "",tranIdLastHd="",tranDateHd="",tranIdNew="",tranCode="",tranName="",lrNo="";
		String itemCodeLast ="" , unitLast = "",descrLast="",custName="",itemSerName="",spCode ="",itemSerLast="";
		//String sretSiteCode = "",sretLocCode = "",sretLotNo="",sretLotSl="",retRepFlag="",selQuery="",invStat="",periodDt="",tranDateStr="";
		//String selectedInvList ="",transitInvList="";
		String transitInvList="";
        String invoiceIdDom="";
        String locTypeLast="";
        String childNodeName = "",lineNoStrDom="",lineNoStr="",stockDlvFlg="",dlvFlagStock="";
        String invoiceIdList = "",dlvFlagDom="",transitListStr="";
        String stancode="",custNameStation="",stanDescr="";
        //String sretSiteCodeAll="",sretLocCodeAll="",sretLotNoAll="",sretLotSlAll="",retRepFlagAll="";
        String sysDate="",period="",custType="",custTypeDescr="";
        //String valueXmlStringSort="",poolCode="",stanCode="",posCode="";
        String currentTranId="",loginSiteCode="",stmtDateStr="";
        String toDateStr="",fromDateStr="";
        String startForm="", netAmtFinal="",netAmtStr="",countryIemSer="",priceList="";
        String confirmed="",status="",errString="",empCode="",positionCode="",empName="";
        String positionDescr="",resultItemSer="",opStock="";
        String reStr="",invoiceMonths="";
        String calCriItemSerStr="";
		double opStockLast=0.0,transitQty=0.0,salesLast=0.0,receiptLast=0.0,returnLast=0.0,rateLast=0.0,salesQty=0.0,clStock=0.0;
		double netAmt=0.0,opStkItem=0.0,awacsClStk=0.0;
		double primarySales=0.0,transitQtyAll=0.0;
		//double transitQtyBon = 0.0,transitQtyFrs=0.0,returnBon=0.0,returnFrs=0.0,receiptBon=0.0,receiptFrs=0.0;
		double rcpQtmDom = 0.0,retQtyDom=0.0,opStkDom=0.0,transitQtyDom=0.0,salesQtyCal=0.0,clStockNew=0.0;
		double retQtyAll=0.0,transitCharQty=0.0,replQtyAll=0.0,primarySalesAll=0.0,rcpFreesLast=0.0;
		double rateAll=0.0,rcpReplQtmDom=0.0,retQtyFreeDom=0.0,rcpQtyAll=0.0,sretQtyAllTot=0.0,transitCharQtyAll=0.0,transitQtyFreesLast=0.0,transitQtyFrees=0.0,rcpFreesAll=0.0,totReceiptLast=0.0,rcpChargableLast=0.0;
		double returnReciptRepl=0.0;
		double returnTransitRepl=0.0;
		double billRetQty=0.0;
		//double billretbonus=0.0;
		double secondarySales=0.0;
		double receiptAmount=0.0;
		double transitQtyRate=0.0,billRetQtyBonus=0.0,sretRate=0.0,transitQuantityVal=0.0;
		//Added by saurabh[23/12/16]
		double returnReciptReplVal=0.0,billRetQtyVal=0.0,returnTransitReplVal=0.0,billRetQtyBonusVal=0.0;
		//Added by saurabh[23/12/16]
		double rcpquantityVal=0,cmQuantity=0.0,cmValue=0.0;
		double rateOrgOld=0.0,rateOld=0.0;
		double rcpFreeQtmDom=0.0;
		//double secondaryQty=0.0,netSecondaryRate=0.0;
		double cmQuantity1=0.0,closingRate=0.0,closingValue=0.0,retValue=0.0,netSecondarySalesValue=0.0;
		double rcpValue=0.0,replValue=0.0,cmValue1=0.0,secondarySalesValue=0.0,formulaValue=0.0;
		double grossSecondarySalesValue=0.0;
		double grossSecondaryQty=0.0;
		double grossSecondaryRate=0.0,rcpFreeValue=0.0;
		double rcpFreesValue=0.0,transitFreeValue=0.0,rateOrg=0.0;
		//double grossClosingRate=0.0,grossClosingValue=0.0;
		double invQtystdm=0.0,invRatestdm=0.0,invDiscntdm=0.0,qtyRetDm=0.0,rateRetDm=0.0,discntRetDm=0.0,qtyRepDm=0.0,rateRepDm=0.0,discntRepDm=0.0;
		double quantityStd=0.0,rateStd=0.0,salesOrg=0.0;
		double opValue=0.0,salesValue=0.0,clValue=0.0,openingValue=0.0,salesValueCal=0.0;
		double grossSecondaryRateCal=0.0,grosSalesValueCal=0.0,opStockQty=0.0;
		double grossOpeningValue=0.0;
		int decimalNumber=0;
		ArrayList itemCodeOfReturnInvoice = new ArrayList();
		int lineNoCustSt=0,currentFormNo = 0,domID3=0;
		int lineNoItmCnt=0,itemResLen=0,itemPos=0,itmDetCnt=0,domID2=0;
		int invCnt=0;
		int parentNodeListLength = 0,lineNo=0;
		int pos=0,invoiceMonthsPrevious=0;
		Date invDate=null,lrDate=null,transitDate=null,toDateCamp=null;
		//Date RelativeDate=null;
	    //Date nextMonthDate=null;
	    Date toDate1=null,fromDate1=null;
	    Date tranDateEdit=null;
	    Timestamp dbSysDate= null,addDate=null;
		java.sql.Timestamp  ToDate = null,FromDate=null,prdFrom=null,prdTo=null,tranDate=null,toDateLast=null;
		Timestamp frmDateTstmp=null,toDateTstmp=null;
		String calEnablePrice="",calPriceDivision="";
		//Timestamp fromDatetmstmp=null,previousDay=null,frmDate1=null;	
		Timestamp thirdMonthDay=null;
		//Timestamp entryStartDt=null,entryEndDt=null;
		Boolean isClStockInt=true,isCustomer=false;
		SimpleDateFormat sdf;
		ArrayList defData = new ArrayList();
		ArrayList defData1 = new ArrayList();
		//Commented by saurabh as dom passed in SecSalesGenPrc is blank and windowName is not used also[15/02/17|Start]
		//windowName = (new StringBuilder("w_")).append(getObjName(dom, objContext)).toString();
		//System.out.println("windowName :"+windowName);
		//windowName="w_cust_stock_gwt";
		//System.out.println((new StringBuilder("Window Name :::[")).append(windowName).append("]").toString());
		//Commented by saurabh as dom passed in SecSalesGenPrc is blank and windowName is not used also[15/02/17|End]
		StringBuffer valueXmlString = new StringBuffer();
		//StringBuffer retString = new StringBuffer();
        //genericUtility = GenericUtility.getInstance();
        valueXmlString = new StringBuffer();
        Date currentDate = new Date();
        HashMap<String, String> invMap = new HashMap<String, String>();
        HashMap<String, String> itemMap = new HashMap<String, String>();
        List <String> invList = new ArrayList<String>();
        List <String> itemListLast = new ArrayList<String>();
        //ArrayList<String> sortItem =new ArrayList<String>();
        ArrayList<Double> rcpQtyList=new ArrayList<Double>();
        ArrayList<Double> transitQtyList=new ArrayList<Double>();
        ArrayList<Double> replQtyList=new ArrayList<Double>();
        //ArrayList<String> cmInvoiceList=new ArrayList<String>();
        ArrayList<Double>  cmrcpQtyList=new ArrayList<Double>();
        ArrayList<Double> transitFreeQtyList=new ArrayList<Double>();
        ArrayList<Double> rcpFreeQtyList=new ArrayList<Double>();
        HashMap<String, ArrayList<String>> invHashMap=new HashMap<String, ArrayList<String>>();
        String itemSerDom="",itemSerHdCl="";
        boolean isItemSerLocal = false;
        ArrayList<String> calCriItemSerList=null;
        int custCnt=0, retCnt=0;
        String refSer="",refSerDom="",dummyProduct="";
        double netAmtRet=0.0,netAmtRep=0.0;//
        String custCodeDom="",tranIdTemp = "",selInvListMap="",selInvListMapFinal="",transitInvListFinal="";
        String countryCode="",loginPositionCode="",countryCodeDom="";
        String isAdmin="",transitDays="";
        int admCnt=0,count=0;
        // int countDetail3=0;
        try
		{
			
			System.out.println((new StringBuilder("\nCustStockGWTIC : itemChanged[")).append(currentColumn).append("] : xtraParams :").append(xtraParams).toString());
            //AppConnectParm appConnect = new AppConnectParm();
            ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
            ConnDriver connDriver = null;
            connDriver = new ConnDriver();
            //conn = connDriver.getConnectDB("DriverITM");
            //Added By Vikas L on 09/05/19[For Sun Migration point]Start
            try 
            {
	            conn = getConnection();
	            
            } catch(Exception e) 
            {
            	System.out.println("Inside Exception.......1828 failed to get connection from getConnection method!!");
            	String userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
            	CommonDBAccessEJB commonDBAccessEJB = new CommonDBAccessEJB();
            	UserInfoBean userInfoBean = commonDBAccessEJB.createUserInfo(userId);
            	conn = connDriver.getConnectDB(userInfoBean.getTransDB());
            }
            //Added By Vikas L on 09/05/19[For Sun Migration point]End
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			chgDate = sdf.format(new java.util.Date());
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			loginEmpCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "entityCode"));//modified by santosh for switch user
			//loginEmpCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
			System.out.println("chgDate :"+chgDate+"@chgUser :"+chgUser+"@loginEmpCode :"+loginEmpCode+"@chgTerm "+chgTerm);
			String calCriItemSerIn="";	
			SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf2.format(currentDate.getTime());
			dbSysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			//UtilMethods utlmethd=new UtilMethods();
			//columnValue = genericUtility.getColumnValue(currentColumn, dom);
            if(objContext != null && objContext.trim().length() > 0)
            currentFormNo = Integer.parseInt(objContext);
            valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
            valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");            
            System.out.println("CustStockGWTIC : itemChanged[currentFormNo] IS : "+currentFormNo);
            //ArrayList calCriItemSerList=null;
            calCriItemSerStr =  dist.getDisparams("999999","CAL_CRIT_ITEMSER",conn);
            transitDays =  dist.getDisparams("999999","ES3_TRANSIT_DAYS",conn);//added by saurabh for invoice Receipt/Transit selection on basis of disparm [27/06/17]
            System.out.println("calCriItemSerStr.." + calCriItemSerStr+">>>transitDays>>>"+transitDays);
            if (("NULLFOUND".equalsIgnoreCase(transitDays) || transitDays == null || transitDays.trim().length() == 0) )
			{
            	transitDays="0";
			}
            System.out.println("isItemSer@@@@@@@before>>>>"+isItemSerLocal);
			//isItemSer=false;
			if (("NULLFOUND".equalsIgnoreCase(calCriItemSerStr) || calCriItemSerStr == null || calCriItemSerStr.trim().length() == 0) )
			{
				isItemSerLocal=false;
				System.out.println("isItemSer@@>>>>"+isItemSerLocal);
			}else
			{
				//added by Saurabh to get item series of invoice[03/11/16|Start]
				StringBuffer strBuffer=new StringBuffer();
				 calCriItemSerList= new ArrayList<String>(Arrays.asList(calCriItemSerStr.split(",")));
				for(int i=0,cnt=0;i<calCriItemSerList.size();i++)
				{
					String calCriItemSerStrChk= (String) calCriItemSerList.get(i);
				    System.out.println("calCriItemSerStrChk -->"+calCriItemSerStrChk.trim());
				    if(calCriItemSerStrChk.trim().length()>0)
				    {
				    	String calCriItemSerStrFnl="'"+calCriItemSerStrChk.trim()+"'";
				    	strBuffer.append(cnt > 1? calCriItemSerStrFnl.trim():calCriItemSerStrFnl.trim()+",");
				    }
				}
				calCriItemSerIn=strBuffer.substring(0, strBuffer.lastIndexOf(","));
				System.out.println("calCriItemSerIn"+calCriItemSerIn);
				//added by Saurabh to get item series of invoice[03/11/16|End]
				isItemSerLocal=false;
				System.out.println("isItemSer@@Chk>>>>"+isItemSerLocal);
			}
			System.out.println("isItemSer@@@@@@@after>>>>"+isItemSerLocal);
			
			switch (currentFormNo)
			{
			
			case 1:
				System.out.println("itemChanged : case 1 called!!!");
				if(currentColumn.trim().equals("itm_default"))
				{
					System.out.println("case 1: itm_default called ");				
					spCode = loginEmpCode.trim();
					System.out.println("value of Sales Person :"+spCode);
					
					//Added by saurabh For admin login[04/02/17|Start]
					sql = "select count(*) as count from org_structure where emp_code=? and version_id = (select FN_GET_VERSION_ID from dual) ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, spCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						admCnt=rs1.getInt("count");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;	
					//Added by saurabh For admin login[04/02/17|End]
					System.out.println("admCnt>>>"+admCnt);
					
					sql= "select count_code from state where " +
							"state_code in (select state_code from site where site_code=?)";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, loginSiteCode );
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						countryCode=checkNull(rs1.getString("count_code")).trim();
						System.out.println("countryCode >>> :"+countryCode);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("countryCode@@@@@>>>"+countryCode);
					sql = "select pos_code from employee where emp_code=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, spCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						loginPositionCode = checkNull(rs1.getString("pos_code")).trim();
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;				
					// Added By Chandrashekar - Suggested by Prajyot - Start
					String sqlFirstDefData = " SELECT CS.ADD_USER,CS.ADD_TERM,CS.ADD_DATE,CS.POS_CODE,CS.EMP_CODE,CS.CUST_CODE CUST_CODE, C.CUST_NAME CUST_NAME, C.CUST_TYPE CUST_TYPE, C.ORDER_TYPE ORDER_TYPE, CS.SITE_CODE SITE_CODE," +
							" CS.FROM_DATE FROM_DATE, CS.TO_DATE TO_DATE, CS.ITEM_SER ITEM_SER, CS.STMT_DATE STMT_DATE, CS.PRD_CODE PRD_CODE,CS.TRAN_DATE TRAN_DATE,CS.TRAN_ID__LAST TRAN_ID__LAST,C.STAN_CODE STAN_CODE " + 
							" FROM CUST_STOCK CS , CUSTOMER C " +
					 		" WHERE CS.CUST_CODE = C.CUST_CODE AND CS.TRAN_ID = ? and CS.pos_code is not null ";//Added by saurabh[20/12/16] 	 
					editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("editFlag", dom1);
					startForm = (startForm != null) ? startForm : genericUtility.getColumnValue("startForm", dom1);
					tranId = genericUtility.getColumnValue("prv_tran_id", dom1);
					System.out.println("Wizard via Homepage...editFlag>>>>"+editFlag + " tranId>>>>[" + tranId + "]");
					if( tranId != null && "E".equalsIgnoreCase(editFlag) || "V".equalsIgnoreCase(editFlag))
					{
						if( tranId.indexOf(":") != -1 )
						{
							String[] tranIdArr = tranId.split(":");
							tranId = tranIdArr[0];
						}
						System.out.println("Wizard via Homepage...editFlag>>>>"+editFlag + " tranId>>>>[" + tranId + "]");
						//Start Added by by chandrashekar on 29-dec-2015
						sql = "	select status,confirmed from  cust_stock where tran_id = ? and pos_code is not null ";//Added by saurabh[20/12/16]
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, tranId);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							status = rs1.getString("status");
							confirmed = rs1.getString("confirmed");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						System.out.println("status>>>>"+status);
						System.out.println("confirmed>>>>"+confirmed);
						if("Y".equalsIgnoreCase(confirmed) && "S".equalsIgnoreCase(status) && !"V".equalsIgnoreCase(editFlag))
						{
							errString = itmDBAccessLocal.getErrorString("", "VTNOTEDIT", "","",conn);
							System.out.println("errString!@@@@@@"+errString);
							return errString;
						}
						//End Added by by chandrashekar on 29-dec-2015
						SimpleDateFormat sdt = new SimpleDateFormat("dd-MMM-yyyy");
						pstmt = conn.prepareStatement(sqlFirstDefData);
						pstmt.setString(1, tranId);
						rs = pstmt.executeQuery();
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						if (rs.next())
						{
							addDate = rs.getTimestamp("ADD_DATE"); 
							fromDate1 = rs.getDate("FROM_DATE");
							toDate1 = rs.getDate("TO_DATE");
							tranDateEdit = rs.getDate("TRAN_DATE");
							fromDateStr = sdt.format(fromDate1.getTime());	 
							toDateStr = sdt.format(toDate1.getTime());
							stmtDateStr = genericUtility.getValidDateString(rs.getDate("STMT_DATE"), genericUtility.getApplDateFormat());
							System.out.println("fromDateStr>>>>"+fromDateStr);
							System.out.println("toDateStr>>>>"+toDateStr);
							System.out.println("tranDateEdit>>>>"+tranDateEdit);
							System.out.println("print@@@@@@@@"+sdf.format(tranDateEdit).toString());
							
							sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
											" from period_appl a,period_tbl b " +
											"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
											" AND b.prd_code = ? " +
											"and b.prd_tblno=? " +
											"AND case when a.type is null then 'X' else a.type end='S' ";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1,rs.getString("PRD_CODE"));
									pstmt1.setString(2, countryCode.trim()+"_"+rs.getString("ITEM_SER").trim());
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										FromDate = rs1.getTimestamp("FR_DATE"); 
										ToDate = rs1.getTimestamp("TO_DATE");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								
								sql= " select descr from itemser where item_ser = ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, rs.getString("ITEM_SER") );
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									itemSerName=checkNull(rs1.getString("descr"));
									System.out.println("itemSerName edit :"+itemSerName);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								
								//Change by chandra shekar on 26-10-2015
								if(rs.getString("CUST_TYPE") != null && rs.getString("CUST_TYPE").trim().length()>0)
								{
									sql = "select descr from gencodes where mod_name='W_CUST_STOCK_GWT' and fld_name='CUST_TYPE' AND fld_value=?";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, rs.getString("CUST_TYPE"));
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										custTypeDescr = checkNull(rs1.getString("descr"));
										System.out.println("custTypeDescr edit :" + custTypeDescr);
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								}
								sql = "select descr from station where stan_code= ? ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, rs.getString("STAN_CODE"));
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									stanDescr = checkNull(rs1.getString("descr"));
									System.out.println("stanDescr edit :" + stanDescr);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								custNameStation=rs.getString("CUST_NAME")+"-"+stanDescr;
								empCode=rs.getString("EMP_CODE");
								if(empCode != null && empCode.trim().length()>0)
								{
									sql = "select emp_fname||' '||emp_mname||' '||emp_lname as name from employee where emp_code=? ";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, empCode);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										empName = checkNull(rs1.getString("name"));
										System.out.println("empName edit :" + empName);
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									
								}
								sql = "select FN_GET_POSCODE_DESCR(?) as descr from dual ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, rs.getString("POS_CODE"));
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									positionDescr = checkNull(rs1.getString("descr"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;

							valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"E\"  status=\"O\" pkNames=\"\" />\r\n");
							valueXmlString.append("<tran_id>").append("<![CDATA["+tranId+"]]>").append("</tran_id>");
							valueXmlString.append("<tran_date>").append("<![CDATA["+sdf.format(tranDateEdit).toString()+"]]>").append("</tran_date>");
							valueXmlString.append("<cust_code protect='1'>").append("<![CDATA["+ rs.getString("CUST_CODE") +"]]>").append("</cust_code>");
							valueXmlString.append("<cust_name protect='1'>").append("<![CDATA["+ custNameStation +"]]>").append("</cust_name>");
							valueXmlString.append("<item_ser protect='1'>").append("<![CDATA["+ rs.getString("ITEM_SER") +"]]>").append("</item_ser>");
							valueXmlString.append("<order_type protect='1'>").append("<![CDATA["+ checkNull(rs.getString("ORDER_TYPE")) +"]]>").append("</order_type>");
							valueXmlString.append("<from_date protect='1'>").append("<![CDATA["+sdf.format(FromDate).toString()+"]]>").append("</from_date>");
							valueXmlString.append("<to_date protect='1'>").append("<![CDATA["+sdf.format(ToDate).toString()+"]]>").append("</to_date>");
							valueXmlString.append("<itemser_descr protect='1'>").append("<![CDATA["+itemSerName+"]]>").append("</itemser_descr>");
							valueXmlString.append("<site_code>").append("<![CDATA["+ rs.getString("SITE_CODE") +"]]>").append("</site_code>");
							valueXmlString.append("<tran_id__last>").append("<![CDATA["+ rs.getString("TRAN_ID__LAST") +"]]>").append("</tran_id__last>");
							//valueXmlString.append("<sordertype_descr protect='1'>").append("<![CDATA["+orderTypeName+"]]>").append("</sordertype_descr>");
							//Statment date to be editable in edit mode reported by Bhavesh Shah[17/05/17|Start] 
							if("V".equalsIgnoreCase(editFlag))
							{
								valueXmlString.append("<stmt_date protect='1'>").append("<![CDATA[" + stmtDateStr + "]]>").append("</stmt_date>");
							}
							else
							{
								valueXmlString.append("<stmt_date protect='0'>").append("<![CDATA[" + stmtDateStr + "]]>").append("</stmt_date>");
							}
							//Statment date to be editable in edit mode reported by Bhavesh Shah[17/05/17|End]
							valueXmlString.append("<status>").append("<![CDATA[O]]>").append("</status>");
							valueXmlString.append("<prd_code protect='1'>").append("<![CDATA["+ rs.getString("PRD_CODE") +"]]>").append("</prd_code>");
							valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
							valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
							valueXmlString.append("<chg_date>").append("<![CDATA["+sdf.format(currentDate)+"]]>").append("</chg_date>");
							valueXmlString.append("<missing_inserted>").append("<![CDATA[Y]]>").append("</missing_inserted>");
							valueXmlString.append("<cust_type protect='1'>").append("<![CDATA["+ rs.getString("CUST_TYPE") +"]]>").append("</cust_type>");
							valueXmlString.append("<cust_type__descr protect='1'>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
							valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
														
							valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag +"]]>").append("</edit_status>");
							valueXmlString.append("<prv_tran_id>").append("<![CDATA["+tranId +"]]>").append("</prv_tran_id>");
							valueXmlString.append("<startForm>").append("<![CDATA["+startForm +"]]>").append("</startForm>");
							valueXmlString.append("<login_poscode>").append("<![CDATA["+loginPositionCode +"]]>").append("</login_poscode>");
							valueXmlString.append("<country_code>").append("<![CDATA["+countryCode +"]]>").append("</country_code>");
							valueXmlString.append("<emp_code protect='1'>").append("<![CDATA["+empCode+"]]>").append("</emp_code>");
							valueXmlString.append("<pos_code protect='1'>").append("<![CDATA["+rs.getString("POS_CODE")+"]]>").append("</pos_code>");
							valueXmlString.append("<emp_name>").append("<![CDATA["+empName+"]]>").append("</emp_name>");
							valueXmlString.append("<position_descr>").append("<![CDATA["+positionDescr+"]]>").append("</position_descr>");
							valueXmlString.append("<add_user>").append("<![CDATA[" +  rs.getString("ADD_USER") + "]]>").append("</add_user>");
							valueXmlString.append("<add_term>").append("<![CDATA[" +  rs.getString("ADD_TERM") + "]]>").append("</add_term>");
							if(addDate !=null)
							{
									valueXmlString.append("<add_date>").append("<![CDATA[" + sdf.format(addDate) + "]]>").append("</add_date>");
							}else
							{
								valueXmlString.append("<add_date protect='1'>").append("<![CDATA[]]>").append("</add_date>");
							}
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//Added by saurabh for admin login check[02/02/17|Start]
						if(admCnt==0)
						{
							valueXmlString.append("<adm_chk>").append("Y").append("</adm_chk>");
						}
						else
						{
							valueXmlString.append("<adm_chk>").append("N").append("</adm_chk>");
						}
						//Added by saurabh for admin login check[02/02/17|End]
						valueXmlString.append("</Detail1>");
					}
					// Added By Chandrashekar - Suggested by Prajyot - End
					else
					{
						System.out.println("************************ADD mode @@@@@@@@@@@@@@@@@@@@@22");
					loginSite = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));
					System.out.println("loginSite :"+loginSite);
					//Start added by chandrashekar on 25-jan-2016
					sql = "select emp_fname||' '||emp_mname||' '||emp_lname as name,pos_code from employee where emp_code=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, spCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						empName = checkNull(rs1.getString("name"));
						positionCode = checkNull(rs1.getString("pos_code")).trim();
						System.out.println("empName edit :" + empName);
						loginPositionCode=positionCode;
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					sql = "select table_no from org_structure where pos_code=? " +
							//"and emp_code=?" +
							" and  version_id = (select FN_GET_VERSION_ID from dual) ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, positionCode);
					//pstmt1.setString(2, spCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						itemSer = checkNull(rs1.getString("table_no"));
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					sql = "select FN_GET_POSCODE_DESCR(?) as descr from dual ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, positionCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						positionDescr = checkNull(rs1.getString("descr"));
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					
					sql= " select descr from itemser where item_ser = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSerName=checkNull(rs.getString("descr"));
						System.out.println("itemSerName :"+itemSerName);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					countryIemSer=countryCode.trim()+"_"+itemSer.trim();
					System.out.println("countryIemSer@@@@@@@@@"+countryIemSer);
					
					//Start added by chandrashekar on 18-jan-2016
					 sql = "select b.FR_DATE as fr_date,b.TO_DATE as to_date,prd_code  from period_appl a,period_tbl b " +
								"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
								"and b.prd_tblno=? " +
								"AND case when a.type is null then 'X' else a.type end='S'  "+
								"and CASE WHEN b.prd_closed IS NULL THEN 'N' ELSE b.prd_closed END='N' " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,countryIemSer);
					
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								period = rs.getString("prd_code");
								fromDate1 = rs.getDate("fr_date");
								toDate1 = rs.getDate("to_date");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("fromDate1>>>>>>"+fromDate1);
							System.out.println("toDate1>>>>>>"+toDate1);
							SimpleDateFormat sdt = new SimpleDateFormat("dd-MMM-yyyy");
							if(fromDate1 !=null)
							{
								fromDateStr = sdt.format(fromDate1.getTime());	
							}
							if(toDate1 !=null)
							{
								toDateStr = sdt.format(toDate1.getTime());
							}
							
							 System.out.println("fromDateStr>>>>"+fromDateStr);
							 System.out.println("toDateStr>>>>"+toDateStr);
					
							 sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE" +
										" from period_appl a,period_tbl b " +
										"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
										" AND b.prd_code = ? " +
										"and b.prd_tblno=? " +
										"AND case when a.type is null then 'X' else a.type end='S' "+
										"and CASE WHEN b.prd_closed IS NULL THEN 'N' ELSE b.prd_closed END='N' " ;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,period.trim());
								pstmt.setString(2,countryIemSer);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									FromDate = rs.getTimestamp("FR_DATE"); 
									ToDate = rs.getTimestamp("TO_DATE");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							
					//End added by chandrashekar on 18-jan-2016
							
					
					//Added by saurabh[Point No.41 :Default position code should come blank if there are no stockiest in position code.|06/12/16|Start]
					sql = " select count(*) as count from org_structure_cust where version_id in(select fn_get_version_id from dual) " +
							" and pos_code=? and table_no=?" ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,positionCode);
						pstmt.setString(2,itemSer);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custCnt = rs.getInt("count"); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					
					//Added by saurabh[Point No.41 :Default position code should come blank if there are no stockiest in position code.|06/12/16|End]
						
					valueXmlString.append("<Detail1 domID='1' objContext=\"1\" selected=\"N\">\r\n");
					valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<tran_id protect='1'><![CDATA[]]></tran_id>");
					valueXmlString.append("<tran_date>").append("<![CDATA[" + sdf.format(currentDate).toString() + "]]>").append("</tran_date>");
					valueXmlString.append("<cust_code>").append("<![CDATA[" + custCode + "]]>").append("</cust_code>");
					valueXmlString.append("<cust_name protect='1'>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");
					valueXmlString.append("<item_ser protect='1'>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
					valueXmlString.append("<order_type protect='1'>").append("<![CDATA[" + orderType + "]]>").append("</order_type>");//qwerty
					if(FromDate !=null)
					{
						valueXmlString.append("<from_date protect='1'>").append("<![CDATA[" + sdf.format(FromDate).toString() + "]]>").append("</from_date>");
					}else
					{
						valueXmlString.append("<from_date protect='1'>").append("<![CDATA[]]>").append("</from_date>");
					}
					if(ToDate !=null)
					{
						valueXmlString.append("<to_date protect='1'>").append("<![CDATA[" + sdf.format(ToDate).toString() + "]]>").append("</to_date>");
					}else
					{
						valueXmlString.append("<to_date protect='1'>").append("<![CDATA[]]>").append("</to_date>");
					}
					valueXmlString.append("<itemser_descr protect='1'>").append("<![CDATA[" + itemSerName + "]]>").append("</itemser_descr>");
					valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
					valueXmlString.append("<tran_id__last>").append("<![CDATA[" + tranIdLast + "]]>").append("</tran_id__last>");
					//valueXmlString.append("<sordertype_descr protect='1'>").append("<![CDATA[" + orderTypeName + "]]>").append("</sordertype_descr>");
					valueXmlString.append("<stmt_date>").append("<![CDATA[" + stmtDateStr + "]]>").append("</stmt_date>");
					valueXmlString.append("<status>").append("<![CDATA[O]]>").append("</status>");
					valueXmlString.append("<prd_code protect='1'>").append("<![CDATA[" + period + "]]>").append("</prd_code>");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
					valueXmlString.append("<chg_date>").append("<![CDATA[" + sdf.format(currentDate) + "]]>").append("</chg_date>");
					valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
					valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
					valueXmlString.append("<add_date>").append("<![CDATA[" + sdf.format(currentDate) + "]]>").append("</add_date>");
					valueXmlString.append("<missing_inserted>").append("<![CDATA[Y]]>").append("</missing_inserted>");
					valueXmlString.append("<cust_type protect='1'>").append("<![CDATA[" + custType + "]]>").append("</cust_type>");
					valueXmlString.append("<cust_type__descr protect='1'>").append("<![CDATA[" + custTypeDescr + "]]>").append("</cust_type__descr>");
					valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
					
					valueXmlString.append("<itemser_descr protect='1'>").append("<![CDATA["+itemSerName+"]]>").append("</itemser_descr>");
					valueXmlString.append("<sale_per>").append("<![CDATA["+spCode+"]]>").append("</sale_per>");
					valueXmlString.append("<pophelp_frmdt>").append("<![CDATA["+fromDateStr+"]]>").append("</pophelp_frmdt>");
					valueXmlString.append("<pophelp_todt>").append("<![CDATA["+toDateStr+"]]>").append("</pophelp_todt>");
					valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag +"]]>").append("</edit_status>");
					valueXmlString.append("<prv_tran_id>").append("<![CDATA["+tranId +"]]>").append("</prv_tran_id>");
					valueXmlString.append("<startForm>").append("<![CDATA["+startForm +"]]>").append("</startForm>");
					valueXmlString.append("<login_poscode>").append("<![CDATA["+loginPositionCode +"]]>").append("</login_poscode>");
					valueXmlString.append("<country_code>").append("<![CDATA["+countryCode +"]]>").append("</country_code>");
					valueXmlString.append("<emp_code protect='1' >").append("<![CDATA["+spCode+"]]>").append("</emp_code>");
					//Added by saurabh[Point No.41 :Default position code should come blank if there are no stockiest in position code.|06/12/16|Start]
					if(custCnt>0){
					valueXmlString.append("<pos_code>").append("<![CDATA["+positionCode+"]]>").append("</pos_code>");
					valueXmlString.append("<position_descr>").append("<![CDATA["+positionDescr+"]]>").append("</position_descr>");
					}
					else
					{
					valueXmlString.append("<pos_code>").append("").append("</pos_code>");
					valueXmlString.append("<position_descr>").append("").append("</position_descr>");
					}
					//Added by saurabh[Point No.41 :Default position code should come blank if there are no stockiest in position code.|06/12/16|End]
					valueXmlString.append("<emp_name>").append("<![CDATA["+empName+"]]>").append("</emp_name>");
					//Added by saurabh for admin login check[02/02/17|Start]
					if(admCnt==0)
					{
						valueXmlString.append("<adm_chk>").append("Y").append("</adm_chk>");
					}
					else
					{
						valueXmlString.append("<adm_chk>").append("N").append("</adm_chk>");
					}
					//Added by saurabh for admin login check[02/02/17|End]
					valueXmlString.append("</Detail1>\r\n");
				}
					
				}
				if(currentColumn.trim().equals("cust_code"))
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
					System.out.println("custCode :"+custCode);
					orderType = checkNull(genericUtility.getColumnValue("order_type", dom));//qwerty
					System.out.println("orderType :"+orderType);
					spCode= checkNull(genericUtility.getColumnValue("sale_per", dom));
					System.out.println("spCode :"+spCode);
					stmtDateStr= checkNull(genericUtility.getColumnValue("stmt_date", dom));
					siteCode= checkNull(genericUtility.getColumnValue("site_code", dom));
					itemSer= checkNull(genericUtility.getColumnValue("item_ser", dom));
					System.out.println("login siteCode :"+siteCode);
					positionCode = checkNull(genericUtility.getColumnValue("pos_code",dom));
					empCode = checkNull(genericUtility.getColumnValue("emp_code",dom));
					period = checkNull(genericUtility.getColumnValue("prd_code",dom));
					System.out.println("@S@ inside cust_code itemChnage ["+period+"]");
					
					fileName = custCode+"_"+itemSer+"_"+positionCode+"_"+period;
					System.out.println("@S@fileName["+fileName+"]");
					/*File filePath=new java.io.File(CommonConstants.JBOSSHOME + File.separator +"ES3log"+custCode+"_"+itemSer+"_"+positionCode+"_"+period);
					
					if (fileName==null || fileName.trim().length()==0 || !filePath.exists()) 
					{						  // file exist
						fileName= custCode+"_"+itemSer+"_"+positionCode+"_"+period;	
					}*/
					spCode=empCode;
					sql= " select cust_name,cust_type,order_type,STAN_CODE from customer where cust_code=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						custName=checkNull(rs.getString("cust_name"));
						custType=checkNull(rs.getString("cust_type"));
						orderType=checkNull(rs.getString("order_type"));
						stancode=checkNull(rs.getString("STAN_CODE"));
						isCustomer=true;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					System.out.println("itemSer :"+itemSer);
					
					sql= " select descr from itemser where item_ser = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSerName=checkNull(rs.getString("descr"));
						System.out.println("itemSerName :"+itemSerName);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
				
					//Change by chandra shekar on 26-10-2015
					if(custType != null && custType.trim().length()>0)
					{
						sql = "select descr from gencodes where mod_name='W_CUST_STOCK_GWT' and fld_name='CUST_TYPE' AND fld_value=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custType);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							custTypeDescr = checkNull(rs.getString("descr"));
							System.out.println("custTypeDescr :" + custTypeDescr);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					sql = "select descr from station where stan_code= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stancode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stanDescr = checkNull(rs.getString("descr"));
						System.out.println("stanDescr :" + stanDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					custNameStation=custName+"-"+stanDescr;
					//01/12/16 ---COnfirmed=y and status=s
					//Added by saurabh 17/01/17 as per discussion with Manoj Sir.--Start
					//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|Start]
					sql = " SELECT max(to_date) as to_date FROM CUST_STOCK WHERE CUST_CODE = ?  " +
							" AND ITEM_SER = ? " +
							//" and order_type=? " +
							" and pos_code is not null and confirmed='Y' and (status='X' or status='S') ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemSer);
					//pstmt.setString(3, orderType);//qwerty
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						toDateLast = rs.getTimestamp("to_date");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = " SELECT max(tran_id) as oldTranId FROM CUST_STOCK WHERE CUST_CODE = ?  " +
							" AND ITEM_SER = ? " +
						//	" and order_type=? " +
							" and pos_code is not null and confirmed='Y' and (status='S' OR STATUS='X')  and to_date=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemSer);
					//pstmt.setString(3, orderType);
					pstmt.setTimestamp(3, toDateLast);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						tranIdLast = checkNull(rs.getString("oldTranId"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|End]
					//Added by saurabh 17/01/17 as per discussion with Manoj Sir.--End
					System.out.println("tranIdLast (case 1):" + tranIdLast);
					//Change by chandra shekar on 26-10-2015
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					if(isCustomer)
					{
						valueXmlString.append("<item_ser protect='1'>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");
						valueXmlString.append("<itemser_descr protect='1'>").append("<![CDATA["+itemSerName+"]]>").append("</itemser_descr>");
					}
					valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
					valueXmlString.append("<cust_name>").append("<![CDATA["+custNameStation+"]]>").append("</cust_name>");
					valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");//change by chandrashekar on 18-jan-2016
					//qwerty
					valueXmlString.append("<order_type protect='1'>").append("<![CDATA["+orderType+"]]>").append("</order_type>");
					//valueXmlString.append("<sordertype_descr protect='1'>").append("<![CDATA["+orderTypeName+"]]>").append("</sordertype_descr>");
					valueXmlString.append("<cust_type protect='1'>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
					valueXmlString.append("<cust_type__descr protect='1'>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
					valueXmlString.append("<stmt_date>").append("<![CDATA["+stmtDateStr+"]]>").append("</stmt_date>");
					valueXmlString.append("<tran_id__last>").append("<![CDATA[" + tranIdLast + "]]>").append("</tran_id__last>");
					valueXmlString.append("<sale_per>").append("<![CDATA["+empCode+"]]>").append("</sale_per>");
					valueXmlString.append("<pos_code>").append("<![CDATA["+positionCode+"]]>").append("</pos_code>");
					valueXmlString.append("<descr>").append("<![CDATA["+stanDescr+"]]>").append("</descr>");//stanDescr
					valueXmlString.append("</Detail1>");
				}
				
				if(currentColumn.trim().equals("pos_code"))
				{
					System.out.println("@@@@position code item change called@@@@");
					positionCode = checkNull(genericUtility.getColumnValue("pos_code",dom));
					loginPositionCode = checkNull(genericUtility.getColumnValue("login_poscode",dom));
					System.out.println("positionCode code itemchange value"+positionCode);
					System.out.println("loginPositionCode dom itemchange value"+loginPositionCode);
					
					//Added by saurabh For admin user login[04/02/17|Start]
					isAdmin = checkNull(genericUtility.getColumnValue("adm_chk",dom));
					System.out.println("isAdmin>>"+isAdmin);
					
					countryCodeDom = genericUtility.getColumnValue("country_code",dom);
					System.out.println("countryCode (pos code item change):"+countryCodeDom);
					//Added by saurabh For admin user login[04/02/17|End]
					
					if(positionCode == null || positionCode.trim().length()==0)
					{
						positionCode=loginPositionCode;
					}
					
					
					sql= " select table_no,emp_code from org_structure where pos_code=? " +
							" and version_id = (select FN_GET_VERSION_ID from dual)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, positionCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer=checkNull(rs.getString("table_no"));
						empCode=checkNull(rs.getString("emp_code"));
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql= " select descr from itemser where item_ser = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSerName=checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = "select emp_fname||' '||emp_mname||' '||emp_lname as name,pos_code from employee where emp_code=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, empCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						empName = checkNull(rs1.getString("name"));
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					sql = "select FN_GET_POSCODE_DESCR(?) as descr from dual ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, positionCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						positionDescr = checkNull(rs1.getString("descr"));
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					
					if("Y".equalsIgnoreCase(isAdmin)){
						//Added by saurabh for period to be set for admin user 04/02/17|Start
						//DOM
						countryIemSer=countryCodeDom.trim()+"_"+itemSer.trim();
						System.out.println("Admin>>countryIemSer@@@@@@@@@"+countryIemSer);

						//Start added by chandrashekar on 18-jan-2016
						 sql = "select b.FR_DATE as fr_date,b.TO_DATE as to_date,prd_code  from period_appl a,period_tbl b " +
									"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
									"and b.prd_tblno=? " +
									"AND case when a.type is null then 'X' else a.type end='S'  "+
									"and CASE WHEN b.prd_closed IS NULL THEN 'N' ELSE b.prd_closed END='N' " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,countryIemSer);

								rs = pstmt.executeQuery();
								if (rs.next())
								{
									period = rs.getString("prd_code");
									fromDate1 = rs.getDate("fr_date");
									toDate1 = rs.getDate("to_date");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Admin>>fromDate1>>>>>>"+fromDate1);
								System.out.println("Admin>>toDate1>>>>>>"+toDate1);
								SimpleDateFormat sdt = new SimpleDateFormat("dd-MMM-yyyy");
								if(fromDate1 !=null)
								{
									fromDateStr = sdt.format(fromDate1.getTime());	
								}
								if(toDate1 !=null)
								{
									toDateStr = sdt.format(toDate1.getTime());
								}
								
								 System.out.println("Admin>>fromDateStr>>>>"+fromDateStr);
								 System.out.println("Admin>>toDateStr>>>>"+toDateStr);

								 sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE" +
											" from period_appl a,period_tbl b " +
											"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
											" AND b.prd_code = ? " +
											"and b.prd_tblno=? " +
											"AND case when a.type is null then 'X' else a.type end='S' "+
											"and CASE WHEN b.prd_closed IS NULL THEN 'N' ELSE b.prd_closed END='N' " ;
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,period.trim());
									pstmt.setString(2,countryIemSer);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										FromDate = rs.getTimestamp("FR_DATE"); 
										ToDate = rs.getTimestamp("TO_DATE");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								
						//End added by chandrashekar on 18-jan-2016
						//Added by saurabh for period to be set for admin user 04/02/17|Start
						}
					
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<emp_code protect='1' >").append("<![CDATA["+empCode+"]]>").append("</emp_code>");
					valueXmlString.append("<sale_per protect='1'>").append("<![CDATA["+empCode+"]]>").append("</sale_per>");
					valueXmlString.append("<emp_name>").append("<![CDATA["+empName+"]]>").append("</emp_name>");
					valueXmlString.append("<pos_code>").append("<![CDATA["+positionCode.trim()+"]]>").append("</pos_code>");
					valueXmlString.append("<item_ser protect='1'>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");
					valueXmlString.append("<itemser_descr protect='1'>").append("<![CDATA["+itemSerName+"]]>").append("</itemser_descr>");
					valueXmlString.append("<position_descr>").append("<![CDATA["+positionDescr+"]]>").append("</position_descr>");
					valueXmlString.append("<login_poscode>").append("<![CDATA["+loginPositionCode +"]]>").append("</login_poscode>");
					//added by saurabh for admin user[040217|start]
					if("Y".equalsIgnoreCase(isAdmin)){
						if(FromDate !=null)
						{
							valueXmlString.append("<from_date protect='1'>").append("<![CDATA[" + sdf.format(FromDate).toString() + "]]>").append("</from_date>");
						}else
						{
							valueXmlString.append("<from_date protect='1'>").append("<![CDATA[]]>").append("</from_date>");
						}
						if(ToDate !=null)
						{
							valueXmlString.append("<to_date protect='1'>").append("<![CDATA[" + sdf.format(ToDate).toString() + "]]>").append("</to_date>");
						}else
						{
							valueXmlString.append("<to_date protect='1'>").append("<![CDATA[]]>").append("</to_date>");
						}
						valueXmlString.append("<prd_code protect='1'>").append("<![CDATA[" + period + "]]>").append("</prd_code>");
						valueXmlString.append("<pophelp_frmdt>").append("<![CDATA["+fromDateStr+"]]>").append("</pophelp_frmdt>");
						valueXmlString.append("<pophelp_todt>").append("<![CDATA["+toDateStr+"]]>").append("</pophelp_todt>");
						
					}
					//added by saurabh for admin user[040217|start]
					valueXmlString.append("</Detail1>");
				}
				
				System.out.println("valueXmlString from case 1 :"+valueXmlString);
                break;
			    
			case 2:
				System.out.println("itemChanged : case 2 called!!!");
				if(currentColumn.trim().equals("itm_default"))
				{	
					System.out.println("itm_default called for case 2");
					tranIdLast = checkNull(genericUtility.getColumnValue("tran_id__last",dom1));
					System.out.println("tranIdLast2(case 2) :"+tranIdLast);
					tranIdNew = checkNull(genericUtility.getColumnValue("tran_id",dom1));
					System.out.println("tranIdNew(case 2) :"+tranIdNew);
					custCode = genericUtility.getColumnValue("cust_code", dom1);
					System.out.println("custCode(case 2) :"+custCode);
					String custCode1 = genericUtility.getColumnValue("cust_code", dom2);
					System.out.println("@S@custCode(case 2) from all dom :"+custCode1);
					fromdate = genericUtility.getColumnValue("from_date", dom1);
					System.out.println("fromdate@@@@@@@@@@@ :"+fromdate);
					todate= genericUtility.getColumnValue("to_date", dom1);
					
					itemSer = genericUtility.getColumnValue("item_ser", dom1);
					siteCodeHd = genericUtility.getColumnValue("site_code", dom1);
					System.out.println("siteCodeHd (case 2):"+siteCodeHd);
					orderType = genericUtility.getColumnValue("order_type", dom1);////qwerty
					System.out.println("orderType (case 2):"+orderType);
					period = genericUtility.getColumnValue("prd_code", dom1);
					System.out.println("period (case 2):"+period);
					//Added by saurabh 04/02/17
					countryCodeDom = genericUtility.getColumnValue("country_code",dom1);
					System.out.println("countryCodeDom (case 2):"+countryCodeDom);
										
					//custCodeStatic=custCode;//Commented by saurabh 03/02/16 
					editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("edit_status", dom1);
					System.out.println("editFlag>>>>"+editFlag);
					fromDateStr = genericUtility.getColumnValue("from_date", dom1);
					System.out.println("fromdate CASE2:"+fromDateStr);
					toDateStr= genericUtility.getColumnValue("to_date", dom1);
					System.out.println("todate  CASE 2:"+toDateStr);
					if(fromDateStr != null)
					{
						FromDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					if(todate != null)
					{
						ToDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
						toDateCamp = sdf.parse(todate);
					}
				
					System.out.println("FromDate case2 :"+FromDate);
					System.out.println("ToDate  case 2:"+ToDate +"::::toDateCamp>>"+toDateCamp);
					
					//Start added by chandrashekar on 19-Sep-2016
					loginEmpCode= genericUtility.getColumnValue("emp_code", dom1);
					System.out.println("loginEmpCode>>>"+loginEmpCode);
					
					System.out.println("calCriItemSerStr.trim().length()>>>>>>>."+calCriItemSerStr.trim().length());
					if(calCriItemSerStr.trim().length()>0){
						System.out.println("calCriItemSerList.contains(itemSer.trim())>>>>>>>"+calCriItemSerList.contains(itemSer.trim()));
					if(calCriItemSerList.contains(itemSer.trim()))
					{
						System.out.println("Inside ItemSer true::::["+calCriItemSerList.contains(itemSerHd.trim())+"]");
						isItemSerLocal=true;
					}
					else
					{
						System.out.println("Inside ItemSer false::::["+calCriItemSerList.contains(itemSerHd.trim())+"]");
						isItemSerLocal=false;
					}
					}
					else{
						System.out.println("isItemSer:::::"+isItemSerLocal);
					}
					
					System.out.println("isItemSer@@@@@@@>>>>"+isItemSerLocal);
					
					//changes done by sangita satrt
					int cnt=0,cnt1=0;
					String tranIdNewXX="";
					sql= " SELECT count(*)  FROM CUST_STOCK WHERE CUST_CODE = ? AND ( status='O')" +//added by sangita for status x
							" AND ITEM_SER = ? " +
							//" and order_type=? " +
							" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemSer);
					//pstmt.setString(3, orderType);
					pstmt.setTimestamp(3, FromDate);
					pstmt.setTimestamp(4, ToDate);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt1=(rs.getInt(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					
							sql= " SELECT TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = ? AND (status ='O')" +//added by sangita for status x
								" AND ITEM_SER = ? " +
								//" and order_type=? " +
								" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemSer);
						//pstmt.setString(3, orderType);
						pstmt.setTimestamp(3, FromDate);
						pstmt.setTimestamp(4, ToDate);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranIdNew=checkNull(rs.getString("TRAN_ID"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|End]
						System.out.println("tranIdNew(case 3) :"+tranIdNew);
						//resultItemSer=getItemSerListInv(itemSer,conn);
						resultItemSer=getItemSerList(itemSer,conn);
						if(tranIdNew.trim().length() > 0)
						{
							// Commented by santosh to set desc order invoice date 
							//sql= " select invoice_id,invoice_date,dlv_flg,net_amt,ref_ser from cust_stock_inv where tran_id=?  order by invoice_date ";
							sql= " select invoice_id,invoice_date,dlv_flg,net_amt,ref_ser from cust_stock_inv where tran_id=?  order by invoice_date desc ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,tranIdNew);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								invCnt++;
								invDate=rs.getDate("invoice_date");
								invoiceId=checkNull(rs.getString("invoice_id"));
								dlvFlag=checkNull(rs.getString("dlv_flg"));
								netAmt = rs.getDouble("net_amt");
								refSer=checkNull(rs.getString("ref_ser"));
								domID2 = domID2 +1 ;
								System.out.println("invoiceId (case 3):"+invoiceId);
								/*invList.add(invoiceId);
								invMap.put(invoiceId,dlvFlag);
								*///if(!(dlvFlag.equalsIgnoreCase("Y"))) commented by chandrashekar on 27-10-2015
								//{
									sql= "select LR_NO ,LR_DATE,TRAN_CODE from invoice where invoice_id= ?";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, invoiceId);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										lrNo=checkNull(rs1.getString("LR_NO"));
										lrNo=lrNo.replace("~", "");//Added by saurabh 17/01/17
										lrDate=rs1.getDate("LR_DATE");
										tranCode=checkNull(rs1.getString("TRAN_CODE"));
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									
									System.out.println("lrNo (case 3):"+lrNo);
									System.out.println("lrDate (case 3):"+lrDate);
									System.out.println("tranCode (case 3):"+tranCode);
								
									tranName="";
									sql= "select tran_name from transporter where tran_code=?";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, tranCode);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										tranName=checkNull(rs1.getString("tran_name"));
										
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									
									System.out.println("tranName (case 3):"+tranName);
								
								System.out.println("dlvFlag is :"+dlvFlag+ "invoice :"+invoiceId);
								
	
								valueXmlString.append("<Detail2 dbID='' domID='"+invCnt+"' objName=\"cust_stock_gwt\" objContext=\"2\">");
								valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"E\" status=\"O\" />");
								valueXmlString.append("<tran_id>").append("<![CDATA["+tranIdNew+"]]>").append("</tran_id>");
								valueXmlString.append("<invoice_id>").append("<![CDATA["+invoiceId+"]]>").append("</invoice_id>");
								valueXmlString.append("<invoice_date>").append("<![CDATA["+sdf.format(invDate)+"]]>").append("</invoice_date>");
								if("V".equalsIgnoreCase(editFlag))
								{
									valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA["+dlvFlag+"]]>").append("</dlv_flg>");
								}else
								{
								valueXmlString.append("<dlv_flg>").append("<![CDATA["+dlvFlag+"]]>").append("</dlv_flg>");
								}
								
								valueXmlString.append("<lr_no>").append("<![CDATA["+lrNo+"]]>").append("</lr_no>");
								if(lrDate != null)
								{
									valueXmlString.append("<lr_date>").append("<![CDATA["+sdf.format(lrDate)+"]]>").append("</lr_date>");
								}
								else
								{
									valueXmlString.append("<lr_date>").append("<![CDATA[]]>").append("</lr_date>");
								}
								
								valueXmlString.append("<tran_code>").append("<![CDATA["+tranCode+"]]>").append("</tran_code>");
								valueXmlString.append("<tran_name>").append("<![CDATA["+tranName+"]]>").append("</tran_name>");
									valueXmlString.append("<ref_ser>").append("<![CDATA["+refSer+"]]>").append("</ref_ser>");
								
								valueXmlString.append("<net_amt>").append("<![CDATA["+(long)Math.round(netAmt)+"]]>").append("</net_amt>");
									/****Condition added by santosh on 08-MAR-2019 to make  return invoice as receipt D18LSUN001 .START***/
									if("S-RET".equalsIgnoreCase(refSer))
									{
										valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA[Y]]>").append("</dlv_flg>");
									}
									/****Condition added by santosh on 08-MAR-2019 to make  return invoice as receipt D18LSUN001 .END***/
								valueXmlString.append("</Detail2>");
								//}
								//}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("List of privious transaction record :"+invList);
						}
						else {
							System.out.println("tranid else part...");
							

							sql= " SELECT count(*)  FROM CUST_STOCK WHERE CUST_CODE = ? AND ( status='X')" +//added by sangita for status x
									" AND ITEM_SER = ? " +
									//" and order_type=? " +
									" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemSer);
							//pstmt.setString(3, orderType);
							pstmt.setTimestamp(3, FromDate);
							pstmt.setTimestamp(4, ToDate);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt=(rs.getInt(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("satus X record >>>"+cnt);
							if(cnt > 0)
							{
								System.out.println("inside x count");
								sql= " SELECT max(TRAN_ID) as TRAN_ID  FROM CUST_STOCK WHERE CUST_CODE = ? AND ( status='X')" +//added by sangita for status x
										" AND ITEM_SER = ? " +
										//" and order_type=? " +
										" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemSer);
								//pstmt.setString(3, orderType);
								pstmt.setTimestamp(3, FromDate);
								pstmt.setTimestamp(4, ToDate);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									tranIdNewXX=checkNull(rs.getString("TRAN_ID"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("tranIdNew(case 3) 11xxxxx :"+tranIdNewXX);
								if( tranIdNewXX.trim().length()>0)
								{


									// Commented by santosh to set desc order invoice date 
									//sql= " select invoice_id,invoice_date,dlv_flg,net_amt,ref_ser from cust_stock_inv where tran_id=?  order by invoice_date ";
									sql= " select invoice_id,invoice_date,dlv_flg,net_amt,ref_ser from cust_stock_inv where tran_id=?  order by invoice_date desc ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,tranIdNewXX);
									rs = pstmt.executeQuery();
									while(rs.next())
									{
										invCnt++;
										invDate=rs.getDate("invoice_date");
										invoiceId=checkNull(rs.getString("invoice_id"));
										dlvFlag=checkNull(rs.getString("dlv_flg"));
										netAmt = rs.getDouble("net_amt");
										refSer=checkNull(rs.getString("ref_ser"));
										domID2 = domID2 +1 ;
										System.out.println("invoiceId (case 3):"+invoiceId);
										/*invList.add(invoiceId);
										invMap.put(invoiceId,dlvFlag);
										*///if(!(dlvFlag.equalsIgnoreCase("Y"))) commented by chandrashekar on 27-10-2015
										//{
											sql= "select LR_NO ,LR_DATE,TRAN_CODE from invoice where invoice_id= ?";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, invoiceId);
											rs1 = pstmt1.executeQuery();
											if(rs1.next())
											{
												lrNo=checkNull(rs1.getString("LR_NO"));
												lrNo=lrNo.replace("~", "");//Added by saurabh 17/01/17
												lrDate=rs1.getDate("LR_DATE");
												tranCode=checkNull(rs1.getString("TRAN_CODE"));
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											
											System.out.println("lrNo (case 3):"+lrNo);
											System.out.println("lrDate (case 3):"+lrDate);
											System.out.println("tranCode (case 3):"+tranCode);
										
											tranName="";
											sql= "select tran_name from transporter where tran_code=?";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, tranCode);
											rs1 = pstmt1.executeQuery();
											if(rs1.next())
											{
												tranName=checkNull(rs1.getString("tran_name"));
												
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											
											System.out.println("tranName (case 3):"+tranName);
										resultItemSer=getItemSerList(itemSer,conn);
										System.out.println("dlvFlag is :"+dlvFlag+ "invoice :"+invoiceId);
										
			/*							netAmt=0.0;
										//sql= "select quantity__stduom,rate__stduom from invoice_trace where invoice_id=? ";
										sql= "select itrace.quantity__stduom,itrace.rate__stduom,itrace.discount " +
												" from invoice invoice,invoice_trace itrace ,item item, " +
												" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET  WHERE CINV.TRAN_ID = CDET.TRAN_ID  ) CINV "+
												" where invoice.invoice_id=itrace.invoice_id " +
												" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) AND CINV.TRAN_ID='"+tranIdNew+"' " ;
												if(recDivStrList.contains(resultItemSer))
												{
													sql=sql+" And  CINV.INVOICE_ID IS NULL ";
												}
												else
												{
													sql=sql+" And  CINV.INVOICE_ID IS NOT NULL ";
												//}
										sql=sql+" and itrace.item_code=item.item_code and itrace.invoice_id=? ";
										if(isItemSerLocal)
										{
											//sql=sql+ " and ( item_ser__prom in (" + resultItemSer + ") OR((item_ser__prom,ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
											sql=sql+ " and ( item.item_ser in (" + resultItemSer + ") OR((item.item_ser,item.ITEM_CODE) " +
													" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
										}else
										{
											//sql=sql+" and ( item_ser__prom in (" + resultItemSer + ")) ";
											//sql=sql+" and ( item.item_ser in (" + resultItemSer + ")) ";
											sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
										}
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, invoiceId);
										rs1 = pstmt1.executeQuery();
										while(rs1.next())
										{
											invQtystdm = rs1.getDouble("quantity__stduom");
											invRatestdm = rs1.getDouble("rate__stduom");
											//Added by saurabh to calcluate net amt with discount consideration reported by BHavesh Shah[16/01/17|Start]
											invDiscntdm = rs1.getDouble("discount");
											netAmt=netAmt+getRequiredDcml(((invQtystdm*invRatestdm)-((invQtystdm*invRatestdm*invDiscntdm)/100)), 3);
											//Added by saurabh to calcluate net amt with discount consideration reported by BHavesh Shah[16/01/17|End]
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;

										System.out.println("netAmt*********>>"+netAmt);
										sreturnCnt=0;
										sql= "SELECT tran_id,net_amt  FROM sreturn WHERE tran_id=?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, invoiceId);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											sreturnCnt=checkNull(rs1.getString("tran_id")).trim().length();
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										//Start added by chandrashekar on 09-sep-206
										
										if(sreturnCnt>0)
										{
											netAmt=0.0;netAmtRet=0.0;netAmtRep=0.0;
											sql = " select count(1) as count from cust_stock_inv where tran_id=? and invoice_id=? ";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, tranIdLast);
											pstmt1.setString(2, invoiceId);
											rs1 = pstmt1.executeQuery();
											while(rs1.next())
											{
												retCnt=rs1.getInt("count");
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											if(retCnt>0)
											{
												netAmtRet=0;
											}
											else{
											sql = "select  sdet.quantity__stduom ,sdet.rate__stduom,sdet.discount " +
													" from sreturn srn,sreturndet sdet,item itm,"+ 
													" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET  WHERE CINV.TRAN_ID = CDET.TRAN_ID  ) CINV "+
													" where srn.tran_id=sdet.tran_id " +
													" AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) AND CINV.TRAN_ID='"+tranIdNew+"' ";
													if(recDivStrList.contains(resultItemSer))
													{
														sql=sql+" And  CINV.INVOICE_ID IS NULL ";
													}
													else
													{
														sql=sql+" And  CINV.INVOICE_ID IS NOT NULL ";
													//}
												sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =? "+ 
													" and srn.confirmed='Y' and sdet.ret_rep_flag in ('R') "; 
												if(isItemSerLocal)
												{
													sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
															+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
													sql=sql+ " and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
															+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
												}else
												{
													//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
													//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
													sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
												}
												pstmt1 = conn.prepareStatement(sql);
												pstmt1.setString(1, invoiceId);
												rs1 = pstmt1.executeQuery();
												while(rs1.next())
												{
													qtyRetDm = rs1.getDouble("quantity__stduom");
													rateRetDm = rs1.getDouble("rate__stduom");
													discntRetDm = rs1.getDouble("discount");
													netAmtRet=getRequiredDcml(((qtyRetDm*rateRetDm)-((qtyRetDm*rateRetDm*discntRetDm)/100)),3);
												}
												rs1.close();
												rs1 = null;
												pstmt1.close();
												pstmt1 = null;
												
											}	
												//Replacement Calculation

												sql = "select  sdet.quantity__stduom , sdet.rate__stduom,sdet.discount " +
														" from sreturn srn,sreturndet sdet,item itm,"+
														" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET  WHERE CINV.TRAN_ID = CDET.TRAN_ID ) CINV "+
														" where srn.tran_id=sdet.tran_id " +
														" AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) AND CINV.TRAN_ID='"+tranIdNew+"' ";
														if(recDivStrList.contains(resultItemSer))
														{
															sql=sql+" And  CINV.INVOICE_ID IS NULL ";
														}
														else
														{
															sql=sql+" And  CINV.INVOICE_ID IS NOT NULL ";
														//}
													sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =? "+ 
														" and srn.confirmed='Y' and sdet.ret_rep_flag in ('P') "; 
												if(isItemSerLocal)
												{
													sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
															+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
													sql=sql+ " and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
															+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
												}else
												{
													//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
													//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
													sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
												}
												pstmt1 = conn.prepareStatement(sql);
												pstmt1.setString(1, invoiceId);
												rs1 = pstmt1.executeQuery();
												while (rs1.next())
												{
													qtyRepDm = rs1.getDouble("quantity__stduom");
													rateRepDm = rs1.getDouble("rate__stduom");
													discntRepDm = rs1.getDouble("discount");
													netAmtRep=getRequiredDcml(((qtyRepDm*rateRepDm)-((qtyRepDm*rateRepDm*discntRepDm)/100)),3);
												}
												rs1.close();
												rs1 = null;
												pstmt1.close();
												pstmt1 = null;
												
												//Calculation
												//netAmtRet=netAmtRet*-1;
												netAmt=(-netAmtRet+netAmtRep);
										}*/
										
										//End added by chandrashekar on 09-sep-206
										/*System.out.println("netAmt***after sreturn******>>"+netAmt);
										netAmtFinal=getRequiredDecimal(netAmt,2);
										netAmtStr = netAmtFinal+"";
										String[] arrStr =netAmtStr.split("\\.");
										decimalNumber = Integer.parseInt(arrStr[1]);
										System.out.println("decimalNumber edit mode>>>"+decimalNumber);
										if(decimalNumber==0)
										{
											netAmtFinal=arrStr[0];
										}
										System.out.println("netValue>>>edit mode>>"+netAmtFinal);*/
										/****Condition added by santosh***/
										/*int retCntCheck=0;
										sql = " select count(1) as count from cust_stock_inv where tran_id= ? and invoice_id= ? and dlv_flg='N' and ref_ser = 'S-RET' ";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, tranIdLast);
										pstmt1.setString(2, invoiceId);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											retCntCheck=rs1.getInt("count");
										
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										if(retCntCheck == 0)
										{*/
										String ab="";
										valueXmlString.append("<Detail2 dbID='' domID='"+invCnt+"' objName=\"cust_stock_gwt\" objContext=\"2\">");
										valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"O\" />");
										valueXmlString.append("<tran_id>").append("<![CDATA["+ab+"]]>").append("</tran_id>");
										valueXmlString.append("<invoice_id>").append("<![CDATA["+invoiceId+"]]>").append("</invoice_id>");
										valueXmlString.append("<invoice_date>").append("<![CDATA["+sdf.format(invDate)+"]]>").append("</invoice_date>");
										if("V".equalsIgnoreCase(editFlag))
										{
											valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA["+dlvFlag+"]]>").append("</dlv_flg>");
										}else
										{
										valueXmlString.append("<dlv_flg>").append("<![CDATA["+dlvFlag+"]]>").append("</dlv_flg>");
										}
										
										valueXmlString.append("<lr_no>").append("<![CDATA["+lrNo+"]]>").append("</lr_no>");
										if(lrDate != null)
										{
											valueXmlString.append("<lr_date>").append("<![CDATA["+sdf.format(lrDate)+"]]>").append("</lr_date>");
										}
										else
										{
											valueXmlString.append("<lr_date>").append("<![CDATA[]]>").append("</lr_date>");
										}
										
										valueXmlString.append("<tran_code>").append("<![CDATA["+tranCode+"]]>").append("</tran_code>");
										valueXmlString.append("<tran_name>").append("<![CDATA["+tranName+"]]>").append("</tran_name>");
											valueXmlString.append("<ref_ser>").append("<![CDATA["+refSer+"]]>").append("</ref_ser>");
										
										valueXmlString.append("<net_amt>").append("<![CDATA["+(long)Math.round(netAmt)+"]]>").append("</net_amt>");
											/****Condition added by santosh on 08-MAR-2019 to make  return invoice as receipt D18LSUN001 .START***/
											if("S-RET".equalsIgnoreCase(refSer))
											{
												valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA[Y]]>").append("</dlv_flg>");
											}
											/****Condition added by santosh on 08-MAR-2019 to make  return invoice as receipt D18LSUN001 .END***/
										valueXmlString.append("</Detail2>");
										//}
										//}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("fdgfffffffffList of privious transaction record :"+invList);
								
								}
								System.out.println("tranIdNew(case 3) xxxxx :"+tranIdNewXX);


							}
							
							else {
								Node detail1Node = dom2.getElementsByTagName("Detail1").item(0);
							tranId =  genericUtility.getColumnValueFromNode("tran_id",detail1Node);
							System.out.println("tranId form 2 :"+tranId);
		                
							System.out.println("ToDate : "+ToDate);
							System.out.println("FromDate : "+FromDate);
							prdFrom = getPreviousDate(FromDate,conn);
							System.out.println("prdFrom :"+prdFrom);
							
							
							
							//added by saurabh for invoice Receipt/Transit selection on basis of disparm [27/06/17|Start]
							sql=" select TO_DATE - "+transitDays+" as transit_date from period_tbl where prd_tblno=? and prd_code=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countryCodeDom+"_"+itemSer.trim());
							pstmt.setString(2, period);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								transitDate =rs.getDate("transit_date");
							}
							System.out.println("transitDate :"+transitDate);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//added by saurabh for invoice Receipt/Transit selection on basis of disparm [27/06/17|End]
							
							sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
									" from period_appl a,period_tbl b " +
									"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
									//"and a.site_code=? " +
									"and b.prd_tblno=? " +
									"AND case when a.type is null then 'X' else a.type end='S' and " +
									" ? between b.FR_DATE and b.TO_DATE ";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1,loginSiteCode);
							pstmt.setString(1, countryCodeDom+"_"+itemSer.trim());
							pstmt.setTimestamp(2, prdFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								prdFrom = rs.getTimestamp("FR_DATE"); 
								prdTo = rs.getTimestamp("TO_DATE");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("prdFrom :"+prdFrom);
							System.out.println("prdTo :"+prdTo);

							prdFrmDateTstmp = prdFrom;
							System.out.println("prdFrmDateTstmp :"+prdFrmDateTstmp);
							prdtoDateTstmp = prdTo;
							System.out.println("prdtoDateTstmp :"+prdtoDateTstmp);
							resultItemSer=getItemSerList(itemSer,conn);
				    //1)consider invoice which is based on period
					//2)cosider transit invoice.
					//3)Union query included for transit invoice not exist in previous month		
					
						
							sql=" SELECT DISTINCT INVOICE_ID,INVOICE_DATE,TRAN_ID,REF_SER FROM ( " +
									" SELECT  invoice.invoice_id invoice_id,invoice.tran_date as  invoice_date  ,'N' as DLV_FLG ,'' as tran_id ,'S-INV' as ref_ser " +
									" FROM invoice invoice,invoice_trace itrace,item item WHERE invoice.invoice_id=itrace.invoice_id " +
									" and itrace.item_code=item.item_code " +
									" and invoice.confirmed = 'Y' and invoice.cust_code = ? " +
									" and invoice.tran_date >= ? and invoice.tran_date <= ? ";
									if(isItemSerLocal)
									{
										/*sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+resultItemSer+")  OR ((itrace.item_ser__prom,itrace.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+") )) ) " ;*/
										/*sql=sql+" and ( itrace.item_ser__prom in ("+resultItemSer+")  OR ((itrace.item_ser__prom,itrace.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+") )) ) " ;*/
										/*sql=sql+" and item.item_usage='F' and ( item.item_ser in ("+resultItemSer+")  OR ((item.item_ser,item.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+") )) ) " ;*/
										sql=sql+" and item.item_usage='F' and ( " +
												//" item.item_ser in ("+resultItemSer+") " +
												" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultItemSer+") ) " +
												" OR ((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+")))) " ;
									}
									else
									{
										//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and( itrace.item_ser__prom in ("+resultItemSer+")) " ;
										//sql=sql+" and( itrace.item_ser__prom in ("+resultItemSer+")) " ;
										//CHanged by saurabh to check product transfer[10/03/17|Start]
										//sql=sql+" and item.item_usage='F' and ( item.item_ser in ("+resultItemSer+")) " ;
										sql=sql+" and item.item_usage='F' and (("+resultItemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual))" ;
										//CHanged by saurabh to check product transfer[10/03/17|End]
									}
							sql=sql+" UNION " ;
									
									if(isItemSerLocal)
									{
									/*sql=sql+" SELECT  CINV.INVOICE_ID , CINV.INVOICE_DATE  AS INVOICE_DATE,CINV.DLV_FLG  , " +
										" '' AS TRAN_ID,'S-INV' AS REF_SER FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET , INVOICE INVOICE " +
										" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND CINV.INVOICE_ID=INVOICE.INVOICE_ID AND CSTK.CUST_CODE=INVOICE.CUST_CODE " +
										" AND INVOICE.CUST_CODE = ? AND INVOICE.CONFIRMED = 'Y' AND " +
										" INVOICE.TRAN_DATE < ?  " +
										" AND CSTK.ITEM_SER in ('"+itemSer.trim()+"') " +
										" and CSTK.pos_code is not null "+//Added by saurabh[20/12/16]
										" AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' " +
										//CHanged by saurabh to check product transfer[10/03/17|End]
										" AND CSTK.TRAN_ID='"+tranIdLast+"' ";*/
										sql=sql+" SELECT CINV.INVOICE_ID , CINV.INVOICE_DATE AS INVOICE_DATE,CINV.DLV_FLG , '' AS TRAN_ID,'S-INV' AS REF_SER "+ 
												" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
												" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
												" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
												" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
												" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
												" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
												" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
												" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultItemSer+")  ) "+ 
												" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultItemSer+") ";	
									}
									else
									{
										sql=sql+" SELECT CINV.INVOICE_ID , CINV.INVOICE_DATE AS INVOICE_DATE,CINV.DLV_FLG , '' AS TRAN_ID,'S-INV' AS REF_SER "+ 
												" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
												" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
												" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
												" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
												" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
												" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
												" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X' ) "+ 
												//" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
												//Commented by santosh to get item ser from detail table
												//" and CDET1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
												" AND ("+resultItemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
									}
									/*if(isItemSerLocal)
									{
										sql=sql+" AND CSTK.TRAN_ID='"+tranIdLast+"' ";
									}
									else
									{
										sql=sql+" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
										" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
										" and CSTK1.POS_CODE IS NOT NULL  and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,INVOICE.TRAN_DATE) from DUAL)) ";
									}*/
									//CHanged by saurabh to check product transfer[10/03/17|End]
								sql=sql+" UNION " +
									" select  '' as invoice_id ,srn.tran_date as invoice_date,'N' as DLV_FLG,sdet.tran_id as tran_id,'S-RET' as ref_ser  " +
									" from sreturn srn,sreturndet sdet,item itm where srn.tran_id=sdet.tran_id " +
									" and sdet.item_code=itm.item_code  and srn.cust_code = ? " +
									" and srn.tran_date >= ? and srn.tran_date <= ? " +
									" and sdet.ret_rep_flag in ('R','P')  and srn.confirmed='Y' " ;
									if(isItemSerLocal)
									{
										/*sql=sql+" and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in ("+resultItemSer+") OR((itm.item_ser,itm.ITEM_CODE)  " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+"))) ) " ;*/
										sql=sql+" and itm.item_usage='F' and ( " +
												//" itm.item_ser in ("+resultItemSer+") " +
												" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultItemSer+") ) "+
												" OR ((itm.item_ser,itm.ITEM_CODE)  " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+"))) ) " ;
									}
									else
									{
										//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in ("+resultItemSer+")) " ;
										//CHanged by saurabh to check product transfer[10/03/17|Start]
										//sql=sql+" and ( itm.item_ser in ("+resultItemSer+")) " ;
										sql=sql+" and itm.item_usage='F' and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
										//CHanged by saurabh to check product transfer[10/03/17|End]
									}
							sql=sql+" union " ;
									if(isItemSerLocal)
									{
									/*sql=sql+" select  '' as invoice_id ,srn.tran_date as invoice_date,cinv.dlv_flg as DLV_FLG,srn.tran_id as tran_id ,'S-RET' as ref_ser " +
									" from sreturn srn,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id and " +
									" srn.tran_id=CINV.invoice_id and srn.cust_code=cstk.cust_code and  srn.cust_code = ? " +
									" and srn.tran_date < ?  " +
									" AND CSTK.ITEM_SER in ('"+itemSer.trim()+"') " +
									" and CSTK.pos_code is not null "+//Added by saurabh[20/12/16]
									" and srn.confirmed='Y' and cinv.dlv_flg='N' " +
							    	//CHanged by saurabh to check product transfer[10/03/17|Start]		
									" AND CSTK.TRAN_ID='"+tranIdLast+"' ";*/
										sql=sql+" select  '' as invoice_id ,CINV.INVOICE_DATE as invoice_date,cinv.dlv_flg as DLV_FLG,SDET.tran_id as tran_id ,'S-RET' as ref_ser "+ 
												" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
												" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
												" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
												" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
												" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultItemSer+") ) "+
												" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultItemSer+") ";
									}
									else
									{
										sql=sql+" select  '' as invoice_id ,CINV.INVOICE_DATE as invoice_date,cinv.dlv_flg as DLV_FLG,SDET.tran_id as tran_id ,'S-RET' as ref_ser "+ 
												" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
												" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
												" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
												" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  ) "+ 
												//" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
												//Commented by santosh to set item ser from detail table
												//" and CDET1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
												" AND ("+resultItemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
									}
									/*else
									{
										sql=sql+" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
										" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
										" and CSTK1.POS_CODE IS NOT NULL  and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,SRN.TRAN_DATE) from DUAL)) ";
									}*/
									
									//CHanged by saurabh to check product transfer[10/03/17|End]
							//sql=sql+" )ORDER BY invoice_date "; Commented by santosh to set invoice desc order
							sql=sql+" )ORDER BY invoice_date desc ";
						
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setTimestamp(2, FromDate);
						pstmt.setTimestamp(3, ToDate);
						pstmt.setString(4, custCode);
						if(isItemSerLocal)
						{
							//pstmt.setTimestamp(5, FromDate);
							pstmt.setTimestamp(5, prdFrom);
						}
						else
						{
							pstmt.setTimestamp(5, prdFrom);
						}
						pstmt.setString(6, custCode);
						pstmt.setTimestamp(7, FromDate);
						pstmt.setTimestamp(8, ToDate);
						pstmt.setString(9, custCode);
						if(isItemSerLocal)
						{
							//pstmt.setTimestamp(10, FromDate);
							pstmt.setTimestamp(10, prdFrom);
						}
						else
						{
							pstmt.setTimestamp(10, prdFrom);
						}
						//Query change 01/12/16
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							System.out.println("while !!!!!");
							invoiceId=checkNull(rs.getString("invoice_id"));
							invDate=rs.getDate("invoice_date");
							refSer=checkNull(rs.getString("ref_ser"));
							System.out.println("invoiceId >>>"+invoiceId+"invDate>>>"+invDate+"refSer>>>"+refSer);

							
							if(invoiceId==null || invoiceId.trim().length()==0)
							{
								invoiceId=checkNull(rs.getString("tran_id")).trim();
							}
							if("S-INV".equalsIgnoreCase(refSer))
							{
								sql= "select LR_NO ,LR_DATE,TRAN_CODE from invoice where invoice_id= ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									lrNo=checkNull(rs1.getString("LR_NO"));
									lrNo=lrNo.replace("~", "");
									lrDate=rs1.getDate("LR_DATE");
									tranCode=checkNull(rs1.getString("TRAN_CODE"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								System.out.println("lrNo :"+lrNo);
								System.out.println("lrDate :"+lrDate);
								System.out.println("tranCode :"+tranCode);
								
								tranName="";
								sql= "select tran_name from transporter where tran_code=?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranCode);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									tranName=checkNull(rs1.getString("tran_name"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
							
								stockDlvFlg="N";
								resultItemSer=getItemSerList(itemSer,conn);
								netAmt=0.0;
								//sql= "select quantity__stduom,rate__stduom from invoice_trace where invoice_id=? ";
								sql= " select itrace.quantity__stduom,itrace.rate__stduom,itrace.discount " +
								
								//",cinv.tran_id,cinv.invoice_id "+    //changes for net amt by sangita
										"from invoice invoice,invoice_trace itrace,item item, " +
										" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER "+
										//", cs.tran_id"  //changes for net amt by sangita
										
										
										 " FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS "
										+ " WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID "
										+ "AND NVL(CINV.DLV_FLG,'N') = 'Y' AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X' ) CINV "+
									//	", cust_stock       cs "+
										"  where invoice.invoice_id=itrace.invoice_id " +
										" AND itrace.invoice_id = CINV.INVOICE_ID (+) "
										+ "AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) "
										+ "AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+)  ";
								sql=sql+" And  CINV.INVOICE_ID IS NULL ";
								//sql=sql+ " AND ((cinv.invoice_id IS NULL) or (cs.status='X' and cinv.invoice_id IS NOT NULL and cs.tran_id=cinv.tran_id )) " ; 
								//changes for net amt by sangita
								sql=sql+" and itrace.item_code= item.item_code and itrace.invoice_id=? ";
								if(isItemSerLocal)
								{
									//sql=sql+ " and ( item_ser__prom in (" + resultItemSer + ") OR((item_ser__prom,ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									//sql=sql+ " and ( item_ser__prom in (" + resultItemSer + ") OR((item_ser__prom,ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									sql=sql+ " and ( " +
											//" item.item_ser in (" + resultItemSer + ") " +
											" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultItemSer+") ) "+
											" OR((item.item_ser,item.ITEM_CODE) " +
											" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
								}else
								{
									//sql=sql+" and ( item.item_ser in (" + resultItemSer + ")) ";
									sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
								}
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								while(rs1.next())
								{
									invQtystdm = rs1.getDouble("quantity__stduom");
									invRatestdm = rs1.getDouble("rate__stduom");
									invDiscntdm = rs1.getDouble("discount");
									//invtranId=rs1.getString("tran_id");
								//	invInvoiceId=rs1.getString("invoice_id");

									//netAmt=netAmt+(invQtystdm*invRatestdm);
									netAmt=netAmt+getRequiredDcml(((invQtystdm*invRatestdm)-((invQtystdm*invRatestdm*invDiscntdm)/100)),3);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								System.out.println("netAmt@@@@@@@@@"+netAmt);
							}
							else if("S-RET".equalsIgnoreCase(refSer))
							{
								sql= "select LR_NO ,LR_DATE,TRAN_CODE from sreturn where tran_id= ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									lrNo=checkNull(rs1.getString("LR_NO"));
									lrNo=lrNo.replace("~", "");
									lrDate=rs1.getDate("LR_DATE");
									tranCode=checkNull(rs1.getString("TRAN_CODE"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								System.out.println("lrNo :"+lrNo);
								System.out.println("lrDate :"+lrDate);
								System.out.println("tranCode :"+tranCode);
								
								tranName="";
								sql= "select tran_name from transporter where tran_code=?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranCode);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									tranName=checkNull(rs1.getString("tran_name"));
									
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								netAmt=0.0;netAmtRet=0.0;netAmtRep=0.0;
								sql = " select count(1) as count from cust_stock_inv where tran_id=? and invoice_id=? ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranIdLast);
								pstmt1.setString(2, invoiceId);
								rs1 = pstmt1.executeQuery();
								while(rs1.next())
								{
									retCnt=rs1.getInt("count");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								if(retCnt>0)
								{
									//added ketan by 06-MAY-21
									//netAmtRet=0;
									sql = " select net_amt as net_amt from cust_stock_inv where tran_id=? and invoice_id=? ";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, tranIdLast);
									pstmt1.setString(2, invoiceId);
									rs1 = pstmt1.executeQuery();
									while(rs1.next())
									{
										netAmtRet=rs1.getInt("net_amt");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									
								}
								else{
								sql = "select  sdet.quantity__stduom , sdet.rate__stduom,sdet.discount " +
										" from sreturn srn,sreturndet sdet,item itm ,"+
										" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND NVL(CINV.DLV_FLG,'N') = 'Y' AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  ) CINV "+
										" where srn.tran_id=sdet.tran_id " +
										" AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) ";
										sql=sql+" And  CINV.INVOICE_ID IS NULL ";
										sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =?  "+ 
									    " and srn.confirmed='Y' and sdet.ret_rep_flag in ('R') "; 
								if(isItemSerLocal)
								{
									/*sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;*/
									sql=sql+ " and ( " +
											//" itm.item_ser in (" + resultItemSer + ") " +
											" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultItemSer+") ) "+
											" OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + ")))) " ;
								}else
								{
									//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
									//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
									sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
								}
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{
									qtyRetDm = rs1.getDouble("quantity__stduom");
									rateRetDm = rs1.getDouble("rate__stduom");
									discntRetDm = rs1.getDouble("discount");
									netAmtRet=netAmtRet+getRequiredDcml(((qtyRetDm*rateRetDm)-((qtyRetDm*rateRetDm*discntRetDm)/100)),3);
									//netAmtRet = rs1.getDouble("net_amt");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								}
								//Replacement Calculation

								sql = "select  sdet.quantity__stduom , sdet.rate__stduom ,sdet.discount " +
									  " from sreturn srn,sreturndet sdet,item itm, "+ 
									  " (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND NVL(CINV.DLV_FLG,'N') = 'Y' AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  ) CINV "+
									  " where srn.tran_id=sdet.tran_id " +
									  " AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) ";
	  								  sql=sql+" And  CINV.INVOICE_ID IS NULL ";
									  sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =? "+ 
									  " and srn.confirmed='Y' and sdet.ret_rep_flag in ('P') "; 
								if(isItemSerLocal)
								{
									/*sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;*/
									sql=sql+ " and ( " +
											//" itm.item_ser in (" + resultItemSer + ") " +
											"  ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultItemSer+") )  "+
											" OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
								}else
								{
									//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
									//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
									sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
								}
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{
									qtyRepDm = rs1.getDouble("quantity__stduom");
									rateRepDm = rs1.getDouble("rate__stduom");
									discntRepDm = rs1.getDouble("discount");
									netAmtRep=netAmtRep+getRequiredDcml(((qtyRepDm*rateRepDm)-((qtyRepDm*rateRepDm*discntRepDm)/100)),3);
									//netAmtRep = rs1.getDouble("net_amt");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								//Calculation
								//netAmtRet=netAmtRet*-1;
								netAmt=(-netAmtRet+netAmtRep);
							
							}
							
								System.out.println("netAmt@@@@sreturnAdd@@@@@"+netAmt);
								netAmtFinal=getRequiredDecimal(netAmt,2);
								System.out.println("netAmtFinal@@@@@@@@@"+netAmtFinal);
								netAmtStr = netAmtFinal+"";
								String[] arrStr =netAmtStr.split("\\.");
								decimalNumber = Integer.parseInt(arrStr[1]);
								System.out.println("decimalNumber add mode>>>"+decimalNumber);
								if(decimalNumber==0)
								{
									netAmtFinal=arrStr[0];
								}
								System.out.println("netValue>>>ADd mode>>"+netAmtFinal);
								//dlvFlgCnt condition removed by saurabh[02/11/16|Start]
								//if(dlvFlgCnt == 0)
								//{
								int retCntCheck=0;
								sql = " select count(1) as count from cust_stock_inv where tran_id= ? and invoice_id= ? and dlv_flg='N' and ref_ser = 'S-RET' ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranIdLast);
								pstmt1.setString(2, invoiceId);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									retCntCheck=rs1.getInt("count");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								if(retCntCheck==0)
								{
									System.out.println("previous domID3 value :"+domID3);
									domID3 = domID3 + 1;
									System.out.println("increment domID3 value :"+domID3);
									System.out.println("stockDlvFlg :"+stockDlvFlg);
								
									invMap.put("invoiceId@"+domID3, invoiceId);
									invMap.put("tranCode@"+domID3, tranCode);
									invMap.put("lrNo@"+domID3, lrNo);
									invMap.put("invDate@"+domID3,sdf.format(invDate));
									if(lrDate != null)
									{
										invMap.put("lrDate@"+domID3,sdf.format(lrDate));
									}
									else
									{
										invMap.put("lrDate@"+domID3,"");
									}
									invMap.put("netAmt@"+domID3, String.valueOf(netAmt));
									dlvFlagStock=GetDlvFlag(invoiceId,conn);
									System.out.println("dlvFlagStock :"+dlvFlagStock);
									invMap.put("dlvFlag@"+domID3,dlvFlagStock);
									invMap.put("tranName@"+domID3, tranName);
									//added by saurabh[23/12/16]
									invMap.put("refSer@"+domID3, refSer);
									//added by saurabh[23/12/16]
								}
									System.out.println("tranName :"+tranName);
								//}	
								//dlvFlgCnt condition removed by saurabh[02/11/16|End]						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("List : "+invList);
						System.out.println("@S@ invMap"+invMap);
						System.out.println("tranIdNew !!!!!!!!!"+tranIdNew);
						System.out.println("domID3 :"+domID3);
						count =0;
						for(int invDomid = 1; invDomid <= domID3  ; invDomid++  )
						{
							
							count++;
							System.out.println("Invoice Id : "+invMap.get("invoiceId@"+invDomid));
							System.out.println("net Amt :"+invMap.get("netAmt@"+invDomid));
							System.out.println("delivert flag of invoice id :"+invMap.get("invoiceId@"+invDomid)+" is : "+invMap.get(invMap.get("invoiceId@"+invDomid)));
							/*if(!(invList.contains(invMap.get("invoiceId@"+invDomid))))
							{*/
								invCnt++;
								
								valueXmlString.append("<Detail2 dbID='' domID='"+invCnt+"' objName=\"cust_stock_gwt\" objContext=\"2\">");
								valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"N\" />");
								valueXmlString.append("<tran_id>").append("<![CDATA["+tranIdNew+"]]>").append("</tran_id>");
//								if("V".equalsIgnoreCase(editFlag))
//								{
//									valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA[Y]]>").append("</dlv_flg>");
//								}else
//								{ 
//									valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
//								}
								System.out.println(">>>>>>>>>"+invMap.get("invDate@"+invDomid));
								System.out.println("transitDate>>>"+transitDate+">>Invoice Date>>"+sdf.parse(invMap.get("invDate@"+invDomid)));
								System.out.println("chk1>>"+transitDate.before(sdf.parse(invMap.get("invDate@"+invDomid))));
								System.out.println("chk2>>"+transitDate.compareTo(sdf.parse(invMap.get("invDate@"+invDomid))));
								//added by saurabh for capturing Awacs invoice flag in ES3 transaction[28/07/17|Start]
								if("awacs_to_es3_prc".equalsIgnoreCase(objName))
								{
									if("S-INV".equalsIgnoreCase(invMap.get("refSer@"+invDomid)))
									{
										awcsDlvFlag="";
										awcsDlvFlag = (String)dataMap.get(invMap.get("invoiceId@"+invDomid));
										System.out.println("Invoice Id : "+invMap.get("invoiceId@"+invDomid)+ " Dlv flag awacs ::"+awcsDlvFlag);
										if(awcsDlvFlag !=null && awcsDlvFlag.trim().length() > 0 ){
										valueXmlString.append("<dlv_flg>").append("<![CDATA["+awcsDlvFlag+"]]>").append("</dlv_flg>");
										}
										else
										{
											valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
										}
									}
									else 
									{
										valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
									}
								}
								else if("sec_sale_gen_prc".equalsIgnoreCase(objName))
								{
									System.out.println("@S@ inside if >>sec_sale_gen_prc ["+objName+"]");
									valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
								}
								else
								{
									//added by saurabh for invoice Receipt/Transit selection on basis of disparm applicable for domestic only [27/06/17|Start]
									if(isItemSerLocal)
									{
										valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
									}
									else
									{
										if(transitDate.before(sdf.parse(invMap.get("invDate@"+invDomid))) || (transitDate.compareTo(sdf.parse(invMap.get("invDate@"+invDomid)))==0 && toDateCamp.compareTo(transitDate)!=0) )
										{
											valueXmlString.append("<dlv_flg>").append("<![CDATA[S]]>").append("</dlv_flg>");
										}else
										{ 
											valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
										}
									}
									//added by saurabh for invoice Receipt/Transit selection on basis of disparm applicable for domestic only [27/06/17|End]
								}
								/***Condition added by santosh D18LSUN001*/
								if("S-RET".equalsIgnoreCase(invMap.get("refSer@"+invDomid)))
								{
									valueXmlString.append("<dlv_flg protect =\"1\">").append("<![CDATA[Y]]>").append("</dlv_flg>");
								}
								//added by saurabh for capturing Awacs invoice flag in ES3 transaction[28/07/17|End] 
								valueXmlString.append("<invoice_id>").append("<![CDATA["+invMap.get("invoiceId@"+invDomid)+"]]>").append("</invoice_id>");
								valueXmlString.append("<invoice_date>").append("<![CDATA["+invMap.get("invDate@"+invDomid)+"]]>").append("</invoice_date>");
								valueXmlString.append("<lr_date>").append("<![CDATA["+invMap.get("lrDate@"+invDomid)+"]]>").append("</lr_date>");
								/*if(sreturnCnt>0){*/
								if("S-RET".equalsIgnoreCase(invMap.get("refSer@"+invDomid))){
									valueXmlString.append("<ref_ser>").append("<![CDATA[S-RET]]>").append("</ref_ser>");
								}
								else
								{
									valueXmlString.append("<ref_ser>").append("<![CDATA[S-INV]]>").append("</ref_ser>");
								}
								valueXmlString.append("<net_amt>").append("<![CDATA["+ (long)Math.round(Double.parseDouble(invMap.get("netAmt@"+invDomid).toString())) +"]]>").append("</net_amt>");
								valueXmlString.append("<lr_no>").append("<![CDATA["+invMap.get("lrNo@"+invDomid)+"]]>").append("</lr_no>");
								valueXmlString.append("<tran_code>").append("<![CDATA["+invMap.get("tranCode@"+invDomid)+"]]>").append("</tran_code>");
								valueXmlString.append("<tran_name>").append("<![CDATA["+invMap.get("tranName@"+invDomid)+"]]>").append("</tran_name>");
								
								valueXmlString.append("</Detail2>");
							//}
						}//end of for loop
						/**Added by Santosh to check Invoice is present for selected used**/
						System.out.println("@S@count["+count+"]");
						if(count==0)
						{
							//valueXmlString.append("<Detail2>");
							valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
				            valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");  
						}
					
						}
						}
						
						
						
						
						
						
						
					
					/*else {
						
						sql= " SELECT count(*)  FROM CUST_STOCK WHERE CUST_CODE = ? AND ( status='X')" +//added by sangita for status x
								" AND ITEM_SER = ? " +
								//" and order_type=? " +
								" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemSer);
						//pstmt.setString(3, orderType);
						pstmt.setTimestamp(3, FromDate);
						pstmt.setTimestamp(4, ToDate);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt=(rs.getInt(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						System.out.println("satus X record >>>"+cnt);
						if(cnt>0)
						{

							sql= " SELECT TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = ? AND ( status='X')" +//added by sangita for status x
									" AND ITEM_SER = ? " +
									//" and order_type=? " +
									" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemSer);
							//pstmt.setString(3, orderType);
							pstmt.setTimestamp(3, FromDate);
							pstmt.setTimestamp(4, ToDate);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								tranIdNewXX=checkNull(rs.getString("TRAN_ID"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("tranIdNew(case 3) 11xxxxx :"+tranIdNewXX);

							if( tranIdNewXX.trim().length()>0)
							{

								// Commented by santosh to set desc order invoice date 
								//sql= " select invoice_id,invoice_date,dlv_flg,net_amt,ref_ser from cust_stock_inv where tran_id=?  order by invoice_date ";
								sql= " select invoice_id,invoice_date,dlv_flg,net_amt,ref_ser from cust_stock_inv where tran_id=?  order by invoice_date desc ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,tranIdNewXX);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									invCnt++;
									invDate=rs.getDate("invoice_date");
									invoiceId=checkNull(rs.getString("invoice_id"));
									dlvFlag=checkNull(rs.getString("dlv_flg"));
									netAmt = rs.getDouble("net_amt");
									refSer=checkNull(rs.getString("ref_ser"));
									domID2 = domID2 +1 ;
									System.out.println("invoiceId (case 3):"+invoiceId);
									invList.add(invoiceId);
									invMap.put(invoiceId,dlvFlag);
									//if(!(dlvFlag.equalsIgnoreCase("Y"))) commented by chandrashekar on 27-10-2015
									//{
										sql= "select LR_NO ,LR_DATE,TRAN_CODE from invoice where invoice_id= ?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, invoiceId);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lrNo=checkNull(rs1.getString("LR_NO"));
											lrNo=lrNo.replace("~", "");//Added by saurabh 17/01/17
											lrDate=rs1.getDate("LR_DATE");
											tranCode=checkNull(rs1.getString("TRAN_CODE"));
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										
										System.out.println("lrNo (case 3):"+lrNo);
										System.out.println("lrDate (case 3):"+lrDate);
										System.out.println("tranCode (case 3):"+tranCode);
									
										tranName="";
										sql= "select tran_name from transporter where tran_code=?";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, tranCode);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											tranName=checkNull(rs1.getString("tran_name"));
											
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										
										System.out.println("tranName (case 3):"+tranName);
									resultItemSer=getItemSerList(itemSer,conn);
									System.out.println("dlvFlag is :"+dlvFlag+ "invoice :"+invoiceId);
									
									netAmt=0.0;
									//sql= "select quantity__stduom,rate__stduom from invoice_trace where invoice_id=? ";
									sql= "select itrace.quantity__stduom,itrace.rate__stduom,itrace.discount " +
											" from invoice invoice,invoice_trace itrace ,item item, " +
											" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET  WHERE CINV.TRAN_ID = CDET.TRAN_ID  ) CINV "+
											" where invoice.invoice_id=itrace.invoice_id " +
											" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) AND CINV.TRAN_ID='"+tranIdNew+"' " ;
											if(recDivStrList.contains(resultItemSer))
											{
												sql=sql+" And  CINV.INVOICE_ID IS NULL ";
											}
											else
											{
												sql=sql+" And  CINV.INVOICE_ID IS NOT NULL ";
											//}
									sql=sql+" and itrace.item_code=item.item_code and itrace.invoice_id=? ";
									if(isItemSerLocal)
									{
										//sql=sql+ " and ( item_ser__prom in (" + resultItemSer + ") OR((item_ser__prom,ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
										sql=sql+ " and ( item.item_ser in (" + resultItemSer + ") OR((item.item_ser,item.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									}else
									{
										//sql=sql+" and ( item_ser__prom in (" + resultItemSer + ")) ";
										//sql=sql+" and ( item.item_ser in (" + resultItemSer + ")) ";
										sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
									}
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, invoiceId);
									rs1 = pstmt1.executeQuery();
									while(rs1.next())
									{
										invQtystdm = rs1.getDouble("quantity__stduom");
										invRatestdm = rs1.getDouble("rate__stduom");
										//Added by saurabh to calcluate net amt with discount consideration reported by BHavesh Shah[16/01/17|Start]
										invDiscntdm = rs1.getDouble("discount");
										netAmt=netAmt+getRequiredDcml(((invQtystdm*invRatestdm)-((invQtystdm*invRatestdm*invDiscntdm)/100)), 3);
										//Added by saurabh to calcluate net amt with discount consideration reported by BHavesh Shah[16/01/17|End]
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;

									System.out.println("netAmt*********>>"+netAmt);
									sreturnCnt=0;
									sql= "SELECT tran_id,net_amt  FROM sreturn WHERE tran_id=?";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, invoiceId);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										sreturnCnt=checkNull(rs1.getString("tran_id")).trim().length();
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									//Start added by chandrashekar on 09-sep-206
									
									if(sreturnCnt>0)
									{
										netAmt=0.0;netAmtRet=0.0;netAmtRep=0.0;
										sql = " select count(1) as count from cust_stock_inv where tran_id=? and invoice_id=? ";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, tranIdLast);
										pstmt1.setString(2, invoiceId);
										rs1 = pstmt1.executeQuery();
										while(rs1.next())
										{
											retCnt=rs1.getInt("count");
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										if(retCnt>0)
										{
											netAmtRet=0;
										}
										else{
										sql = "select  sdet.quantity__stduom ,sdet.rate__stduom,sdet.discount " +
												" from sreturn srn,sreturndet sdet,item itm,"+ 
												" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET  WHERE CINV.TRAN_ID = CDET.TRAN_ID  ) CINV "+
												" where srn.tran_id=sdet.tran_id " +
												" AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) AND CINV.TRAN_ID='"+tranIdNew+"' ";
												if(recDivStrList.contains(resultItemSer))
												{
													sql=sql+" And  CINV.INVOICE_ID IS NULL ";
												}
												else
												{
													sql=sql+" And  CINV.INVOICE_ID IS NOT NULL ";
												//}
											sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =? "+ 
												" and srn.confirmed='Y' and sdet.ret_rep_flag in ('R') "; 
											if(isItemSerLocal)
											{
												sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
														+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
												sql=sql+ " and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
														+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
											}else
											{
												//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
												//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
												sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
											}
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, invoiceId);
											rs1 = pstmt1.executeQuery();
											while(rs1.next())
											{
												qtyRetDm = rs1.getDouble("quantity__stduom");
												rateRetDm = rs1.getDouble("rate__stduom");
												discntRetDm = rs1.getDouble("discount");
												netAmtRet=getRequiredDcml(((qtyRetDm*rateRetDm)-((qtyRetDm*rateRetDm*discntRetDm)/100)),3);
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											
										}	
											//Replacement Calculation

											sql = "select  sdet.quantity__stduom , sdet.rate__stduom,sdet.discount " +
													" from sreturn srn,sreturndet sdet,item itm,"+
													" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET  WHERE CINV.TRAN_ID = CDET.TRAN_ID ) CINV "+
													" where srn.tran_id=sdet.tran_id " +
													" AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) AND CINV.TRAN_ID='"+tranIdNew+"' ";
													if(recDivStrList.contains(resultItemSer))
													{
														sql=sql+" And  CINV.INVOICE_ID IS NULL ";
													}
													else
													{
														sql=sql+" And  CINV.INVOICE_ID IS NOT NULL ";
													//}
												sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =? "+ 
													" and srn.confirmed='Y' and sdet.ret_rep_flag in ('P') "; 
											if(isItemSerLocal)
											{
												sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
														+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
												sql=sql+ " and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
														+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
											}else
											{
												//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
												//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
												sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
											}
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, invoiceId);
											rs1 = pstmt1.executeQuery();
											while (rs1.next())
											{
												qtyRepDm = rs1.getDouble("quantity__stduom");
												rateRepDm = rs1.getDouble("rate__stduom");
												discntRepDm = rs1.getDouble("discount");
												netAmtRep=getRequiredDcml(((qtyRepDm*rateRepDm)-((qtyRepDm*rateRepDm*discntRepDm)/100)),3);
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											
											//Calculation
											//netAmtRet=netAmtRet*-1;
											netAmt=(-netAmtRet+netAmtRep);
									}
									
									//End added by chandrashekar on 09-sep-206
									System.out.println("netAmt***after sreturn******>>"+netAmt);
									netAmtFinal=getRequiredDecimal(netAmt,2);
									netAmtStr = netAmtFinal+"";
									String[] arrStr =netAmtStr.split("\\.");
									decimalNumber = Integer.parseInt(arrStr[1]);
									System.out.println("decimalNumber edit mode>>>"+decimalNumber);
									if(decimalNumber==0)
									{
										netAmtFinal=arrStr[0];
									}
									System.out.println("netValue>>>edit mode>>"+netAmtFinal);
									*//****Condition added by santosh***//*
									int retCntCheck=0;
									sql = " select count(1) as count from cust_stock_inv where tran_id= ? and invoice_id= ? and dlv_flg='N' and ref_ser = 'S-RET' ";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, tranIdLast);
									pstmt1.setString(2, invoiceId);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										retCntCheck=rs1.getInt("count");
									
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									if(retCntCheck == 0)
									{
									String ab="";
									valueXmlString.append("<Detail2 dbID='' domID='"+invCnt+"' objName=\"cust_stock_gwt\" objContext=\"2\">");
									valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"O\" />");
									valueXmlString.append("<tran_id>").append("<![CDATA["+ab+"]]>").append("</tran_id>");
									valueXmlString.append("<invoice_id>").append("<![CDATA["+invoiceId+"]]>").append("</invoice_id>");
									valueXmlString.append("<invoice_date>").append("<![CDATA["+sdf.format(invDate)+"]]>").append("</invoice_date>");
									if("V".equalsIgnoreCase(editFlag))
									{
										valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA["+dlvFlag+"]]>").append("</dlv_flg>");
									}else
									{
									valueXmlString.append("<dlv_flg>").append("<![CDATA["+dlvFlag+"]]>").append("</dlv_flg>");
									}
									
									valueXmlString.append("<lr_no>").append("<![CDATA["+lrNo+"]]>").append("</lr_no>");
									if(lrDate != null)
									{
										valueXmlString.append("<lr_date>").append("<![CDATA["+sdf.format(lrDate)+"]]>").append("</lr_date>");
									}
									else
									{
										valueXmlString.append("<lr_date>").append("<![CDATA[]]>").append("</lr_date>");
									}
									
									valueXmlString.append("<tran_code>").append("<![CDATA["+tranCode+"]]>").append("</tran_code>");
									valueXmlString.append("<tran_name>").append("<![CDATA["+tranName+"]]>").append("</tran_name>");
										valueXmlString.append("<ref_ser>").append("<![CDATA["+refSer+"]]>").append("</ref_ser>");
									
									valueXmlString.append("<net_amt>").append("<![CDATA["+(long)Math.round(netAmt)+"]]>").append("</net_amt>");
										*//****Condition added by santosh on 08-MAR-2019 to make  return invoice as receipt D18LSUN001 .START***//*
										if("S-RET".equalsIgnoreCase(refSer))
										{
											valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA[Y]]>").append("</dlv_flg>");
										}
										*//****Condition added by santosh on 08-MAR-2019 to make  return invoice as receipt D18LSUN001 .END***//*
									valueXmlString.append("</Detail2>");
									//}
									//}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("fdgfffffffffList of privious transaction record :"+invList);
							
							}
							
							
							
							System.out.println("tranIdNew(case 3) xxxxx :"+tranIdNewXX);

							
						
						}
						else
						{

							
							Node detail1Node = dom2.getElementsByTagName("Detail1").item(0);
							tranId =  genericUtility.getColumnValueFromNode("tran_id",detail1Node);
							System.out.println("tranId form 2 :"+tranId);
		                
							System.out.println("ToDate : "+ToDate);
							System.out.println("FromDate : "+FromDate);
							prdFrom = getPreviousDate(FromDate,conn);
							System.out.println("prdFrom :"+prdFrom);
							
							//added by saurabh for invoice Receipt/Transit selection on basis of disparm [27/06/17|Start]
							sql=" select TO_DATE - "+transitDays+" as transit_date from period_tbl where prd_tblno=? and prd_code=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countryCodeDom+"_"+itemSer.trim());
							pstmt.setString(2, period);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								transitDate =rs.getDate("transit_date");
							}
							System.out.println("transitDate :"+transitDate);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//added by saurabh for invoice Receipt/Transit selection on basis of disparm [27/06/17|End]
							
							sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
									" from period_appl a,period_tbl b " +
									"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
									//"and a.site_code=? " +
									"and b.prd_tblno=? " +
									"AND case when a.type is null then 'X' else a.type end='S' and " +
									" ? between b.FR_DATE and b.TO_DATE ";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1,loginSiteCode);
							pstmt.setString(1, countryCodeDom+"_"+itemSer.trim());
							pstmt.setTimestamp(2, prdFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								prdFrom = rs.getTimestamp("FR_DATE"); 
								prdTo = rs.getTimestamp("TO_DATE");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("prdFrom :"+prdFrom);
							System.out.println("prdTo :"+prdTo);

							prdFrmDateTstmp = prdFrom;
							System.out.println("prdFrmDateTstmp :"+prdFrmDateTstmp);
							prdtoDateTstmp = prdTo;
							System.out.println("prdtoDateTstmp :"+prdtoDateTstmp);
							resultItemSer=getItemSerList(itemSer,conn);
				    //1)consider invoice which is based on period
					//2)cosider transit invoice.
					//3)Union query included for transit invoice not exist in previous month		
					
						
							sql=" SELECT DISTINCT INVOICE_ID,INVOICE_DATE,TRAN_ID,REF_SER FROM ( " +
									" SELECT  invoice.invoice_id invoice_id,invoice.tran_date as  invoice_date  ,'N' as DLV_FLG ,'' as tran_id ,'S-INV' as ref_ser " +
									" FROM invoice invoice,invoice_trace itrace,item item WHERE invoice.invoice_id=itrace.invoice_id " +
									" and itrace.item_code=item.item_code " +
									" and invoice.confirmed = 'Y' and invoice.cust_code = ? " +
									" and invoice.tran_date >= ? and invoice.tran_date <= ? ";
									if(isItemSerLocal)
									{
										sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+resultItemSer+")  OR ((itrace.item_ser__prom,itrace.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+") )) ) " ;
										sql=sql+" and ( itrace.item_ser__prom in ("+resultItemSer+")  OR ((itrace.item_ser__prom,itrace.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+") )) ) " ;
										sql=sql+" and item.item_usage='F' and ( item.item_ser in ("+resultItemSer+")  OR ((item.item_ser,item.ITEM_CODE) " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+") )) ) " ;
										sql=sql+" and item.item_usage='F' and ( " +
												//" item.item_ser in ("+resultItemSer+") " +
												" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultItemSer+") ) " +
												" OR ((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+")))) " ;
									}
									else
									{
										//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and( itrace.item_ser__prom in ("+resultItemSer+")) " ;
										//sql=sql+" and( itrace.item_ser__prom in ("+resultItemSer+")) " ;
										//CHanged by saurabh to check product transfer[10/03/17|Start]
										//sql=sql+" and item.item_usage='F' and ( item.item_ser in ("+resultItemSer+")) " ;
										sql=sql+" and item.item_usage='F' and (("+resultItemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual))" ;
										//CHanged by saurabh to check product transfer[10/03/17|End]
									}
							sql=sql+" UNION " ;
									
									if(isItemSerLocal)
									{
									sql=sql+" SELECT  CINV.INVOICE_ID , CINV.INVOICE_DATE  AS INVOICE_DATE,CINV.DLV_FLG  , " +
										" '' AS TRAN_ID,'S-INV' AS REF_SER FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET , INVOICE INVOICE " +
										" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND CINV.INVOICE_ID=INVOICE.INVOICE_ID AND CSTK.CUST_CODE=INVOICE.CUST_CODE " +
										" AND INVOICE.CUST_CODE = ? AND INVOICE.CONFIRMED = 'Y' AND " +
										" INVOICE.TRAN_DATE < ?  " +
										" AND CSTK.ITEM_SER in ('"+itemSer.trim()+"') " +
										" and CSTK.pos_code is not null "+//Added by saurabh[20/12/16]
										" AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' " +
										//CHanged by saurabh to check product transfer[10/03/17|End]
										" AND CSTK.TRAN_ID='"+tranIdLast+"' ";
										sql=sql+" SELECT CINV.INVOICE_ID , CINV.INVOICE_DATE AS INVOICE_DATE,CINV.DLV_FLG , '' AS TRAN_ID,'S-INV' AS REF_SER "+ 
												" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
												" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
												" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
												" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
												" CSTK.pos_code is not null AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
												" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
												" and CSTK1.POS_CODE IS NOT NULL "+ 
												" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultItemSer+")  ) "+ 
												" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultItemSer+") ";	
									}
									else
									{
										sql=sql+" SELECT CINV.INVOICE_ID , CINV.INVOICE_DATE AS INVOICE_DATE,CINV.DLV_FLG , '' AS TRAN_ID,'S-INV' AS REF_SER "+ 
												" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
												" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
												" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
												" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
												" CSTK.pos_code is not null AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
												" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
												" and CSTK1.POS_CODE IS NOT NULL "+ 
												//" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
												//Commented by santosh to get item ser from detail table
												" and CDET1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
												" AND ("+resultItemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
									}
									if(isItemSerLocal)
									{
										sql=sql+" AND CSTK.TRAN_ID='"+tranIdLast+"' ";
									}
									else
									{
										sql=sql+" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
										" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
										" and CSTK1.POS_CODE IS NOT NULL  and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,INVOICE.TRAN_DATE) from DUAL)) ";
									}
									//CHanged by saurabh to check product transfer[10/03/17|End]
								sql=sql+" UNION " +
									" select  '' as invoice_id ,srn.tran_date as invoice_date,'N' as DLV_FLG,sdet.tran_id as tran_id,'S-RET' as ref_ser  " +
									" from sreturn srn,sreturndet sdet,item itm where srn.tran_id=sdet.tran_id " +
									" and sdet.item_code=itm.item_code  and srn.cust_code = ? " +
									" and srn.tran_date >= ? and srn.tran_date <= ? " +
									" and sdet.ret_rep_flag in ('R','P')  and srn.confirmed='Y' " ;
									if(isItemSerLocal)
									{
										sql=sql+" and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in ("+resultItemSer+") OR((itm.item_ser,itm.ITEM_CODE)  " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+"))) ) " ;
										sql=sql+" and itm.item_usage='F' and ( " +
												//" itm.item_ser in ("+resultItemSer+") " +
												" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultItemSer+") ) "+
												" OR ((itm.item_ser,itm.ITEM_CODE)  " +
												" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN ("+resultItemSer+"))) ) " ;
									}
									else
									{
										//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in ("+resultItemSer+")) " ;
										//CHanged by saurabh to check product transfer[10/03/17|Start]
										//sql=sql+" and ( itm.item_ser in ("+resultItemSer+")) " ;
										sql=sql+" and itm.item_usage='F' and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
										//CHanged by saurabh to check product transfer[10/03/17|End]
									}
							sql=sql+" union " ;
									if(isItemSerLocal)
									{
									sql=sql+" select  '' as invoice_id ,srn.tran_date as invoice_date,cinv.dlv_flg as DLV_FLG,srn.tran_id as tran_id ,'S-RET' as ref_ser " +
									" from sreturn srn,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id and " +
									" srn.tran_id=CINV.invoice_id and srn.cust_code=cstk.cust_code and  srn.cust_code = ? " +
									" and srn.tran_date < ?  " +
									" AND CSTK.ITEM_SER in ('"+itemSer.trim()+"') " +
									" and CSTK.pos_code is not null "+//Added by saurabh[20/12/16]
									" and srn.confirmed='Y' and cinv.dlv_flg='N' " +
							    	//CHanged by saurabh to check product transfer[10/03/17|Start]		
									" AND CSTK.TRAN_ID='"+tranIdLast+"' ";
										sql=sql+" select  '' as invoice_id ,CINV.INVOICE_DATE as invoice_date,cinv.dlv_flg as DLV_FLG,SDET.tran_id as tran_id ,'S-RET' as ref_ser "+ 
												" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
												" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
												" and CSTK.pos_code is not null AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
												" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL "+ 
												" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultItemSer+") ) "+
												" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultItemSer+") ";
									}
									else
									{
										sql=sql+" select  '' as invoice_id ,CINV.INVOICE_DATE as invoice_date,cinv.dlv_flg as DLV_FLG,SDET.tran_id as tran_id ,'S-RET' as ref_ser "+ 
												" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
												" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
												" and CSTK.pos_code is not null AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
												" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
												" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL "+ 
												//" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
												//Commented by santosh to set item ser from detail table
												" and CDET1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
												" AND ("+resultItemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
									}
									else
									{
										sql=sql+" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
										" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
										" and CSTK1.POS_CODE IS NOT NULL  and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,SRN.TRAN_DATE) from DUAL)) ";
									}
									
									//CHanged by saurabh to check product transfer[10/03/17|End]
							//sql=sql+" )ORDER BY invoice_date "; Commented by santosh to set invoice desc order
							sql=sql+" )ORDER BY invoice_date desc ";
						
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setTimestamp(2, FromDate);
						pstmt.setTimestamp(3, ToDate);
						pstmt.setString(4, custCode);
						if(isItemSerLocal)
						{
							//pstmt.setTimestamp(5, FromDate);
							pstmt.setTimestamp(5, prdFrom);
						}
						else
						{
							pstmt.setTimestamp(5, prdFrom);
						}
						pstmt.setString(6, custCode);
						pstmt.setTimestamp(7, FromDate);
						pstmt.setTimestamp(8, ToDate);
						pstmt.setString(9, custCode);
						if(isItemSerLocal)
						{
							//pstmt.setTimestamp(10, FromDate);
							pstmt.setTimestamp(10, prdFrom);
						}
						else
						{
							pstmt.setTimestamp(10, prdFrom);
						}
						//Query change 01/12/16
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							System.out.println("while !!!!!");
							invoiceId=checkNull(rs.getString("invoice_id"));
							invDate=rs.getDate("invoice_date");
							refSer=checkNull(rs.getString("ref_ser"));
							System.out.println("invoiceId >>>"+invoiceId+"invDate>>>"+invDate+"refSer>>>"+refSer);

							
							if(invoiceId==null || invoiceId.trim().length()==0)
							{
								invoiceId=checkNull(rs.getString("tran_id")).trim();
							}
							if("S-INV".equalsIgnoreCase(refSer))
							{
								sql= "select LR_NO ,LR_DATE,TRAN_CODE from invoice where invoice_id= ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									lrNo=checkNull(rs1.getString("LR_NO"));
									lrNo=lrNo.replace("~", "");
									lrDate=rs1.getDate("LR_DATE");
									tranCode=checkNull(rs1.getString("TRAN_CODE"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								System.out.println("lrNo :"+lrNo);
								System.out.println("lrDate :"+lrDate);
								System.out.println("tranCode :"+tranCode);
								
								tranName="";
								sql= "select tran_name from transporter where tran_code=?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranCode);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									tranName=checkNull(rs1.getString("tran_name"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
							
								stockDlvFlg="N";
								resultItemSer=getItemSerList(itemSer,conn);
								netAmt=0.0;
								//sql= "select quantity__stduom,rate__stduom from invoice_trace where invoice_id=? ";
								sql= " select itrace.quantity__stduom,itrace.rate__stduom,itrace.discount " +
								
								//",cinv.tran_id,cinv.invoice_id "+    //changes for net amt by sangita
										"from invoice invoice,invoice_trace itrace,item item, " +
										" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER "+
										//", cs.tran_id"  //changes for net amt by sangita
										
										
										 " FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS "
										+ " WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID "
										+ "AND NVL(CINV.DLV_FLG,'N') = 'Y' AND CS.POS_CODE IS NOT NULL ) CINV "+
									//	", cust_stock       cs "+
										"  where invoice.invoice_id=itrace.invoice_id " +
										" AND itrace.invoice_id = CINV.INVOICE_ID (+) "
										+ "AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) "
										+ "AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+)  ";
								sql=sql+" And  CINV.INVOICE_ID IS NULL ";
								//sql=sql+ " AND ((cinv.invoice_id IS NULL) or (cs.status='X' and cinv.invoice_id IS NOT NULL and cs.tran_id=cinv.tran_id )) " ; 
								//changes for net amt by sangita
								sql=sql+" and itrace.item_code= item.item_code and itrace.invoice_id=? ";
								if(isItemSerLocal)
								{
									//sql=sql+ " and ( item_ser__prom in (" + resultItemSer + ") OR((item_ser__prom,ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									//sql=sql+ " and ( item_ser__prom in (" + resultItemSer + ") OR((item_ser__prom,ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									sql=sql+ " and ( " +
											//" item.item_ser in (" + resultItemSer + ") " +
											" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultItemSer+") ) "+
											" OR((item.item_ser,item.ITEM_CODE) " +
											" IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
								}else
								{
									//sql=sql+" and ( item.item_ser in (" + resultItemSer + ")) ";
									sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
								}
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								while(rs1.next())
								{
									invQtystdm = rs1.getDouble("quantity__stduom");
									invRatestdm = rs1.getDouble("rate__stduom");
									invDiscntdm = rs1.getDouble("discount");
									//invtranId=rs1.getString("tran_id");
								//	invInvoiceId=rs1.getString("invoice_id");

									//netAmt=netAmt+(invQtystdm*invRatestdm);
									netAmt=netAmt+getRequiredDcml(((invQtystdm*invRatestdm)-((invQtystdm*invRatestdm*invDiscntdm)/100)),3);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								System.out.println("netAmt@@@@@@@@@"+netAmt);
							}
							else if("S-RET".equalsIgnoreCase(refSer))
							{
								sql= "select LR_NO ,LR_DATE,TRAN_CODE from sreturn where tran_id= ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									lrNo=checkNull(rs1.getString("LR_NO"));
									lrNo=lrNo.replace("~", "");
									lrDate=rs1.getDate("LR_DATE");
									tranCode=checkNull(rs1.getString("TRAN_CODE"));
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								System.out.println("lrNo :"+lrNo);
								System.out.println("lrDate :"+lrDate);
								System.out.println("tranCode :"+tranCode);
								
								tranName="";
								sql= "select tran_name from transporter where tran_code=?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranCode);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									tranName=checkNull(rs1.getString("tran_name"));
									
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								netAmt=0.0;netAmtRet=0.0;netAmtRep=0.0;
								sql = " select count(1) as count from cust_stock_inv where tran_id=? and invoice_id=? ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranIdLast);
								pstmt1.setString(2, invoiceId);
								rs1 = pstmt1.executeQuery();
								while(rs1.next())
								{
									retCnt=rs1.getInt("count");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								if(retCnt>0)
								{
									netAmtRet=0;
								}
								else{
								sql = "select  sdet.quantity__stduom , sdet.rate__stduom,sdet.discount " +
										" from sreturn srn,sreturndet sdet,item itm ,"+
										" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND NVL(CINV.DLV_FLG,'N') = 'Y' AND CS.POS_CODE IS NOT NULL ) CINV "+
										" where srn.tran_id=sdet.tran_id " +
										" AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) ";
										sql=sql+" And  CINV.INVOICE_ID IS NULL ";
										sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =?  "+ 
									    " and srn.confirmed='Y' and sdet.ret_rep_flag in ('R') "; 
								if(isItemSerLocal)
								{
									sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									sql=sql+ " and ( " +
											//" itm.item_ser in (" + resultItemSer + ") " +
											" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultItemSer+") ) "+
											" OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + ")))) " ;
								}else
								{
									//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
									//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
									sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
								}
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{
									qtyRetDm = rs1.getDouble("quantity__stduom");
									rateRetDm = rs1.getDouble("rate__stduom");
									discntRetDm = rs1.getDouble("discount");
									netAmtRet=netAmtRet+getRequiredDcml(((qtyRetDm*rateRetDm)-((qtyRetDm*rateRetDm*discntRetDm)/100)),3);
									//netAmtRet = rs1.getDouble("net_amt");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								}
								//Replacement Calculation

								sql = "select  sdet.quantity__stduom , sdet.rate__stduom ,sdet.discount " +
									  " from sreturn srn,sreturndet sdet,item itm, "+ 
									  " (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND NVL(CINV.DLV_FLG,'N') = 'Y' AND CS.POS_CODE IS NOT NULL ) CINV "+
									  " where srn.tran_id=sdet.tran_id " +
									  " AND sdet.tran_id = CINV.INVOICE_ID (+) AND sdet.ITEM_CODE = CINV.ITEM_CODE (+) AND sdet.ITEM_SER = CINV.ITEM_SER (+) ";
	  								  sql=sql+" And  CINV.INVOICE_ID IS NULL ";
									  sql=sql+" and sdet.item_code=itm.item_code and srn.tran_id =? "+ 
									  " and srn.confirmed='Y' and sdet.ret_rep_flag in ('P') "; 
								if(isItemSerLocal)
								{
									sql=sql+ " and srn.item_ser in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ") OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
									sql=sql+ " and ( " +
											//" itm.item_ser in (" + resultItemSer + ") " +
											"  ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultItemSer+") )  "+
											" OR((itm.item_ser,itm.ITEM_CODE) " 
											+ " IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN (" + resultItemSer + "))) ) " ;
								}else
								{
									//sql=sql+" and srn.item_ser not in ("+calCriItemSerIn+") and ( itm.item_ser in (" + resultItemSer + ")) ";
									//sql=sql+" and ( itm.item_ser in (" + resultItemSer + ")) ";
									sql=sql+" and (("+resultItemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
								}
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, invoiceId);
								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{
									qtyRepDm = rs1.getDouble("quantity__stduom");
									rateRepDm = rs1.getDouble("rate__stduom");
									discntRepDm = rs1.getDouble("discount");
									netAmtRep=netAmtRep+getRequiredDcml(((qtyRepDm*rateRepDm)-((qtyRepDm*rateRepDm*discntRepDm)/100)),3);
									//netAmtRep = rs1.getDouble("net_amt");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								//Calculation
								//netAmtRet=netAmtRet*-1;
								netAmt=(-netAmtRet+netAmtRep);
							
							}
							
								System.out.println("netAmt@@@@sreturnAdd@@@@@"+netAmt);
								netAmtFinal=getRequiredDecimal(netAmt,2);
								System.out.println("netAmtFinal@@@@@@@@@"+netAmtFinal);
								netAmtStr = netAmtFinal+"";
								String[] arrStr =netAmtStr.split("\\.");
								decimalNumber = Integer.parseInt(arrStr[1]);
								System.out.println("decimalNumber add mode>>>"+decimalNumber);
								if(decimalNumber==0)
								{
									netAmtFinal=arrStr[0];
								}
								System.out.println("netValue>>>ADd mode>>"+netAmtFinal);
								//dlvFlgCnt condition removed by saurabh[02/11/16|Start]
								//if(dlvFlgCnt == 0)
								//{
								int retCntCheck=0;
								sql = " select count(1) as count from cust_stock_inv where tran_id= ? and invoice_id= ? and dlv_flg='N' and ref_ser = 'S-RET' ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranIdLast);
								pstmt1.setString(2, invoiceId);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									retCntCheck=rs1.getInt("count");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								if(retCntCheck==0)
								{
									System.out.println("previous domID3 value :"+domID3);
									domID3 = domID3 + 1;
									System.out.println("increment domID3 value :"+domID3);
									System.out.println("stockDlvFlg :"+stockDlvFlg);
								
									invMap.put("invoiceId@"+domID3, invoiceId);
									invMap.put("tranCode@"+domID3, tranCode);
									invMap.put("lrNo@"+domID3, lrNo);
									invMap.put("invDate@"+domID3,sdf.format(invDate));
									if(lrDate != null)
									{
										invMap.put("lrDate@"+domID3,sdf.format(lrDate));
									}
									else
									{
										invMap.put("lrDate@"+domID3,"");
									}
									invMap.put("netAmt@"+domID3, String.valueOf(netAmt));
									dlvFlagStock=GetDlvFlag(invoiceId,conn);
									System.out.println("dlvFlagStock :"+dlvFlagStock);
									invMap.put("dlvFlag@"+domID3,dlvFlagStock);
									invMap.put("tranName@"+domID3, tranName);
									//added by saurabh[23/12/16]
									invMap.put("refSer@"+domID3, refSer);
									//added by saurabh[23/12/16]
								}
									System.out.println("tranName :"+tranName);
								//}	
								//dlvFlgCnt condition removed by saurabh[02/11/16|End]						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("List : "+invList);
						System.out.println("@S@ invMap"+invMap);
						System.out.println("tranIdNew !!!!!!!!!"+tranIdNew);
						System.out.println("domID3 :"+domID3);
						count =0;
						for(int invDomid = 1; invDomid <= domID3  ; invDomid++  )
						{
							
							count++;
							System.out.println("Invoice Id : "+invMap.get("invoiceId@"+invDomid));
							System.out.println("net Amt :"+invMap.get("netAmt@"+invDomid));
							System.out.println("delivert flag of invoice id :"+invMap.get("invoiceId@"+invDomid)+" is : "+invMap.get(invMap.get("invoiceId@"+invDomid)));
							if(!(invList.contains(invMap.get("invoiceId@"+invDomid))))
							{
								invCnt++;
								
								valueXmlString.append("<Detail2 dbID='' domID='"+invCnt+"' objName=\"cust_stock_gwt\" objContext=\"2\">");
								valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"N\" />");
								valueXmlString.append("<tran_id>").append("<![CDATA["+tranIdNew+"]]>").append("</tran_id>");
//								if("V".equalsIgnoreCase(editFlag))
//								{
//									valueXmlString.append("<dlv_flg protect='1'>").append("<![CDATA[Y]]>").append("</dlv_flg>");
//								}else
//								{ 
//									valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
//								}
								System.out.println(">>>>>>>>>"+invMap.get("invDate@"+invDomid));
								System.out.println("transitDate>>>"+transitDate+">>Invoice Date>>"+sdf.parse(invMap.get("invDate@"+invDomid)));
								System.out.println("chk1>>"+transitDate.before(sdf.parse(invMap.get("invDate@"+invDomid))));
								System.out.println("chk2>>"+transitDate.compareTo(sdf.parse(invMap.get("invDate@"+invDomid))));
								//added by saurabh for capturing Awacs invoice flag in ES3 transaction[28/07/17|Start]
								if("awacs_to_es3_prc".equalsIgnoreCase(objName))
								{
									if("S-INV".equalsIgnoreCase(invMap.get("refSer@"+invDomid)))
									{
										awcsDlvFlag="";
										awcsDlvFlag = (String)dataMap.get(invMap.get("invoiceId@"+invDomid));
										System.out.println("Invoice Id : "+invMap.get("invoiceId@"+invDomid)+ " Dlv flag awacs ::"+awcsDlvFlag);
										if(awcsDlvFlag !=null && awcsDlvFlag.trim().length() > 0 ){
										valueXmlString.append("<dlv_flg>").append("<![CDATA["+awcsDlvFlag+"]]>").append("</dlv_flg>");
										}
										else
										{
											valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
										}
									}
									else 
									{
										valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
									}
								}
								else if("sec_sale_gen_prc".equalsIgnoreCase(objName))
								{
									System.out.println("@S@ inside if >>sec_sale_gen_prc ["+objName+"]");
									valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
								}
								else
								{
									//added by saurabh for invoice Receipt/Transit selection on basis of disparm applicable for domestic only [27/06/17|Start]
									if(isItemSerLocal)
									{
										valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
									}
									else
									{
										if(transitDate.before(sdf.parse(invMap.get("invDate@"+invDomid))) || (transitDate.compareTo(sdf.parse(invMap.get("invDate@"+invDomid)))==0 && toDateCamp.compareTo(transitDate)!=0) )
										{
											valueXmlString.append("<dlv_flg>").append("<![CDATA[S]]>").append("</dlv_flg>");
										}else
										{ 
											valueXmlString.append("<dlv_flg>").append("<![CDATA[Y]]>").append("</dlv_flg>");
										}
									}
									//added by saurabh for invoice Receipt/Transit selection on basis of disparm applicable for domestic only [27/06/17|End]
								}
								*//***Condition added by santosh D18LSUN001*//*
								if("S-RET".equalsIgnoreCase(invMap.get("refSer@"+invDomid)))
								{
									valueXmlString.append("<dlv_flg protect =\"1\">").append("<![CDATA[Y]]>").append("</dlv_flg>");
								}
								//added by saurabh for capturing Awacs invoice flag in ES3 transaction[28/07/17|End] 
								valueXmlString.append("<invoice_id>").append("<![CDATA["+invMap.get("invoiceId@"+invDomid)+"]]>").append("</invoice_id>");
								valueXmlString.append("<invoice_date>").append("<![CDATA["+invMap.get("invDate@"+invDomid)+"]]>").append("</invoice_date>");
								valueXmlString.append("<lr_date>").append("<![CDATA["+invMap.get("lrDate@"+invDomid)+"]]>").append("</lr_date>");
								if(sreturnCnt>0){
								if("S-RET".equalsIgnoreCase(invMap.get("refSer@"+invDomid))){
									valueXmlString.append("<ref_ser>").append("<![CDATA[S-RET]]>").append("</ref_ser>");
								}
								else
								{
									valueXmlString.append("<ref_ser>").append("<![CDATA[S-INV]]>").append("</ref_ser>");
								}
								valueXmlString.append("<net_amt>").append("<![CDATA["+ (long)Math.round(Double.parseDouble(invMap.get("netAmt@"+invDomid).toString())) +"]]>").append("</net_amt>");
								valueXmlString.append("<lr_no>").append("<![CDATA["+invMap.get("lrNo@"+invDomid)+"]]>").append("</lr_no>");
								valueXmlString.append("<tran_code>").append("<![CDATA["+invMap.get("tranCode@"+invDomid)+"]]>").append("</tran_code>");
								valueXmlString.append("<tran_name>").append("<![CDATA["+invMap.get("tranName@"+invDomid)+"]]>").append("</tran_name>");
								
								valueXmlString.append("</Detail2>");
							//}
						}//end of for loop
						*//**Added by Santosh to check Invoice is present for selected used**//*
						System.out.println("@S@count["+count+"]");
						if(count==0)
						{
							//valueXmlString.append("<Detail2>");
							valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
				            valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");  
						}
					
						}
						
					}*/
					
					
					
				}
				 System.out.println("case 2 valueXmlString :"+valueXmlString);
                break;    

			case 3:
				System.out.println("itemChanged : case 3 called!!!@@@@@@@@@");
				//Node detailNode3 = dom.getElementsByTagName((new StringBuilder("Detail")).append(currentFormNo).toString()).item(0);
				if(currentColumn.trim().equals("itm_default"))
				{	
					//NodeList parentNodeList1 = null;
					NodeList childNodeList1 = null;
					Node parentNode1 = null;
					Node childNode1 = null;
					int childNodeListLength1;
					//int parentNodeListLength1 = 0;
			        NodeList parentNodeList3 = dom2.getElementsByTagName("Detail2");
	                int parentNodeListLength3 = parentNodeList3.getLength();
	                System.out.println("parentNodeListLength3 Detail3:::::::::"+parentNodeListLength3);
	                 
	                for (int selectedRow = 0; selectedRow < parentNodeListLength3; selectedRow++)
	                {               
	                    parentNode1 = parentNodeList3.item(selectedRow);
	                    childNodeList1 = parentNode1.getChildNodes();
	                    childNodeListLength1 = childNodeList1.getLength();
	                    System.out.println("childNodeListLength1::: "+ childNodeListLength1+"\n");
	                   
	                    for (int childRow = 0; childRow < childNodeListLength1; childRow++)
	                    {
	                        childNode1 = childNodeList1.item(childRow);
	                        childNodeName = childNode1.getNodeName();
	                       System.out.println("childNodeName :"+childNodeName);
	                       
	                       if(childNodeName.equals("dlv_flg"))
	                       {
	                        	if(childNode1.getFirstChild()!=null)
	                            {
	                            	dlvFlagDom = childNode1.getFirstChild().getNodeValue();
	                            	System.out.println("dlvFlagDom :"+dlvFlagDom);
	                            }
	                       } 
	                       if(childNodeName.equals("ref_ser"))
	                       {
	                        	if(childNode1.getFirstChild()!=null)
	                            {
	                        		refSerDom = childNode1.getFirstChild().getNodeValue();
	                            	System.out.println("refSerDom :"+refSerDom);
	                            }
	                       }
	                       //TODO
	                       if (childNodeName.equals("invoice_id"))
	                       {
	                            if(childNode1.getFirstChild()!=null)
	                            {
	                            	invoiceIdDom = childNode1.getFirstChild().getNodeValue();
	                            	System.out.println("Detail3 invoiceIdDom:::]" +invoiceIdDom);
		                            System.out.println("dlvFlagDom : "+dlvFlagDom);
		                            if(dlvFlagDom.equalsIgnoreCase("Y"))
		                            {
			                            if(childRow != (childNodeListLength1 - 1))
				                        {
				                        	invoiceIdList = invoiceIdList + "'"+invoiceIdDom.trim() + "',"; 
				                        }
				                        else
				                        {
				                        	invoiceIdList = invoiceIdList + "'"+invoiceIdDom.trim() + "'"; 
				                        }
		                            }
		                            else
		                            {
		                            	if(childRow != (childNodeListLength1 - 1))
				                        {
		                            		transitListStr = transitListStr + "'"+invoiceIdDom.trim() + "',"; 
				                        }
				                        else
				                        {
				                        	transitListStr = transitListStr + "'"+invoiceIdDom.trim() + "'"; 
				                        }
		                            }
		                         }
	                        }
	                        System.out.println("childRow :"+childRow);
	                        System.out.println("childNodeListLength1 :"+childNodeListLength1);
	                    }
	                }  
					
					System.out.println("invoiceIdList :"+invoiceIdList);
					System.out.println("invoiceIdList :"+transitListStr);
					if(invoiceIdList.trim().length() > 0)
					{
						selectedInvList = invoiceIdList.substring(0,invoiceIdList.length() - 1);
					}
					else
					{
						selectedInvList = "' '";
					}
					if(transitListStr.trim().length() > 0)
					{
						transitInvList = transitListStr.substring(0,transitListStr.length() - 1);
					}
					else
					{
						transitInvList = "' '";
					}
					
					
					System.out.println("selectedInvList :"+selectedInvList);
					System.out.println("transitInvList :"+transitInvList);
					/*------------------------------------------------------*/
					System.out.println("itm_default called for case 3");
					tranIdLast = checkNull(genericUtility.getColumnValue("tran_id__last",dom1));
					System.out.println("tranIdLast :"+tranIdLast);
					
					tranIdNew = checkNull(genericUtility.getColumnValue("tran_id",dom1));
					System.out.println("tranIdNew :"+tranIdNew);
					
					Node detail2Node = dom2.getElementsByTagName("Detail1").item(0);
					custCodeHd = genericUtility.getColumnValue("cust_code", dom1);
					System.out.println("custCodeHd (case 3):"+custCodeHd);
					itemSerHd = genericUtility.getColumnValueFromNode("item_ser",detail2Node);
					System.out.println("itemSerHd (case 3) :"+itemSerHd);
					System.out.println("calCriItemSerStr.trim().length()>>>>>>>."+calCriItemSerStr.trim().length());
					if(calCriItemSerStr.trim().length()>0){
						System.out.println("calCriItemSerList.contains(itemSerHd.trim())"+calCriItemSerList.contains(itemSerHd.trim()));
						if(calCriItemSerList.contains(itemSerHd.trim()))
						{
							System.out.println("Inside ItemSer true::::["+calCriItemSerList.contains(itemSerHd.trim())+"]");
							isItemSerLocal=true;
						}
						else{
							System.out.println("Inside ItemSer false::::["+calCriItemSerList.contains(itemSerHd.trim())+"]");
							isItemSerLocal=false;
						}
						}
					else
					{
							System.out.println("isItemSer:::::"+isItemSerLocal);
					}
					System.out.println("3.isItemSer@@@@@@@>>>>"+isItemSerLocal);
					siteCodeHd = genericUtility.getColumnValue("site_code", dom1);
					System.out.println("siteCodeHd (case 3) :"+siteCodeHd);
					tranIdLastHd = genericUtility.getColumnValue("tran_id__last", dom1);
					System.out.println("mTranIdLast (case 3) :"+tranIdLastHd);
					orderType = genericUtility.getColumnValue("order_type", dom1);//qwerty
					System.out.println("orderType (case 3) :"+orderType);
					tranDateHd = genericUtility.getColumnValue("tran_date",dom1);
					System.out.println("tranDateHd (case 3) :"+tranDateHd);
					fromdate = genericUtility.getColumnValue("from_date",dom1);
					System.out.println("fromdate (case 3) :"+fromdate);
					todate = genericUtility.getColumnValue("to_date",dom1);
					System.out.println("todate (case 3):"+todate);
					loginEmpCode = genericUtility.getColumnValue("emp_code",dom1);
					System.out.println("loginEmpCode (case 3):"+loginEmpCode);
					countryCodeDom = genericUtility.getColumnValue("country_code",dom1);
					System.out.println("countryCodeDom (case 3):"+countryCodeDom);
					editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("edit_status", dom1);
					System.out.println("editFlag case 3>>>>"+editFlag);
					
					/*---calculate previous month from input value period,start------------------------------*/
					
					frmDateTstmp = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					System.out.println("frmDateTstmp :"+frmDateTstmp);
					toDateTstmp = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(todate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					System.out.println("toDateTstmp :"+toDateTstmp);
					tranDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(tranDateHd, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					System.out.println("tranDate :"+tranDate);
					prdFrom = getPreviousDate(frmDateTstmp,conn);
					System.out.println("prdFrom :"+prdFrom);
								
					
					 sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
					 		//",b.entry_start_dt as entry_start_dt" +
							//	",b.entry_end_dt as entry_end_dt" +
								" from period_appl a,period_tbl b " +
								"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
								//"and a.site_code=? " +
								//"AND b.prd_code = ? " +
								"and b.prd_tblno=? " +
								"AND case when a.type is null then 'X' else a.type end='S' and " +
								"? between b.FR_DATE and b.TO_DATE ";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1,loginSiteCode);
						//pstmt.setString(2,rs.getString("PRD_CODE"));
						pstmt.setString(1, countryCodeDom+"_"+itemSerHd.trim());
						pstmt.setTimestamp(2, prdFrom);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							prdFrmDateTstmp = rs.getTimestamp("FR_DATE"); 
							prdtoDateTstmp = rs.getTimestamp("TO_DATE");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					
					System.out.println("prdFrmDateTstmp :"+prdFrmDateTstmp);
					System.out.println("prdtoDateTstmp :"+prdtoDateTstmp);
					
					/*sql= "select distinct(invoice_id) from invoice where invoice_id " +
						 "in ("+selectedInvList+","+transitInvList+") " +
						 "and tran_date >= ?  and tran_date <= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, frmDateTstmp);
					pstmt.setTimestamp(2, toDateTstmp);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						invoiceIdStr=checkNull(rs.getString("invoice_id"));
						cmInvoiceList.add(invoiceIdStr);
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("cmInvoiceList>>>>>>"+cmInvoiceList);*/
						
					prdtoDateTmstmp=toDateTstmp;
					prdFromoDateTmstmp=frmDateTstmp;
					/*-------------------------END------------------------------------------------------------------*/
					//qwerty
					//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|Start]
					sql= " SELECT TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = ? and status ='O' " +
							" AND ITEM_SER = ? " +
							//" and order_type=? " +
							" and from_date =? and to_date = ? and pos_code is not null ";//Added by saurabh[20/12/16]
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCodeHd);
					pstmt.setString(2, itemSerHd);
					//pstmt.setString(3, orderType);
					//Commented by saurabh as per mail request by Bhavesh on 7th JUN[12/06/17|End]
					pstmt.setTimestamp(3, frmDateTstmp);
					pstmt.setTimestamp(4, toDateTstmp);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranIdNew=checkNull(rs.getString("TRAN_ID"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("c"+tranIdNew);
					
					currentTranId=tranIdNew;
					itemSerDom=itemSerHd;
					itemSerHd=getItemSerList(itemSerHd,conn);
					//itemSerHeaderSplit=itemSerHd;
					//orderTypeHeader=orderType;
					
					System.out.println("Retrive Item Details data from previous month or current month which is exist in CUST_STOK_DET !!!!!!!!!!!!!");
						/*sql= "select item_code,unit,op_stock ,sales,purc_rcp,cl_stock,purc_ret,rate,line_no,transit_qty,item_ser ,purc_rcp__repl ,purc_rcp__free,purc_ret__repl,purc_ret__free,transit_qty__repl,transit_qty__free ,loc_type " +
								" from cust_stock_det where tran_id=? ";*/
					if(checkNull(currentTranId).trim().length()>0)
					{
						invHashMap=getItemwiseInvoice(custCodeHd,itemSerDom,itemSerHd,siteCodeHd,orderType,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,tranIdLast,tranIdNew,selectedInvList,conn,isItemSerLocal);
						sql= "select custdet.rate__org,custdet.rate,custdet.item_code,item.descr,custdet.unit,custdet.op_stock" +
								" ,custdet.sales,custdet.purc_rcp,custdet.cl_stock,custdet.purc_ret," +
								"custdet.rate,custdet.line_no,custdet.transit_qty,custdet.item_ser ," +
								"custdet.purc_rcp__repl ,custdet.purc_rcp__free,custdet.purc_ret__repl," +
								"custdet.purc_ret__free,custdet.transit_qty__repl,custdet.transit_qty__free ," +
								"custdet.op_value,custdet.sales_value,custdet.cl_value, "+
								"custdet.loc_type, custdet.sales__org  from cust_stock_det custdet,item item " +
								"where custdet.item_code=item.item_code " +
								" and tran_id=? order by item.descr ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, currentTranId);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							itmDetCnt++;
							itemCodeLast=checkNull(rs.getString("item_code"));
							itemSerLast=checkNull(rs.getString("item_ser"));
							unitLast=checkNull(rs.getString("unit"));
							locTypeLast=checkNull(rs.getString("loc_type"));
						    salesLast=rs.getDouble("sales");
							clStockNew=rs.getDouble("cl_stock");
							lineNoCustSt=rs.getInt("line_no");
							opStockLast=rs.getDouble("op_stock");
							rateLast=rs.getDouble("rate");
							rateOrg=rs.getDouble("rate__org");
							salesOrg=rs.getDouble("sales__org");
							opValue=rs.getDouble("op_value");
							salesValue=rs.getDouble("sales_value");
							clValue=rs.getDouble("cl_value");
							
							System.out.println("itemCodeLast (case 4):"+itemCodeLast);
							System.out.println("opStockLast(case 4) :"+opStockLast);
							System.out.println("receiptLast (case 4):"+receiptLast);
							System.out.println("returnLast (case 4):"+returnLast);
							System.out.println("lineNoCustSt (case 4):"+lineNoCustSt);
							
							sql = "	select cl_stock,CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
									" CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org from " +
									"cust_stock_det where tran_id in (SELECT tran_id__last FROM CUST_STOCK " +
									"WHERE TRAN_ID =? and pos_code is not null AND STATUS <> 'X'  ) and item_code=? ";//Added by saurabh[20/12/16]
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, currentTranId);
							pstmt1.setString(2, itemCodeLast);
							rs1 = pstmt1.executeQuery();
							if (rs1.next()) 
							{
								//clStock = rs1.getString("cl_stock");
								rateOld = rs1.getDouble("rate");
								rateOrgOld = rs1.getDouble("rate__org");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							
							if(rateOrgOld == 0)
							{
								rateOrgOld=rateOld;
							}
							/*---------------Receipt Quantity Calculation(start)------------------------------------------------*/
							
							if(opValue<=0){
							opValue=opStockLast*rateOld;
							System.out.println("rateOld>>>"+rateOld+"opStockLast>>>>"+opStockLast);
							}
							System.out.println("-------calculate Receipt quantity  for availble transcaion Id--------");
							receiptLast=0.0;
							//receiptRate=0.0;
							rcpQtyList=null;
							
							
							try
							{
								selInvListMap="";
								selInvListMap=invHashMap.get(itemCodeLast) == null ? "" : (invHashMap.get(itemCodeLast)).toString();
								selInvListMap = selInvListMap.replace("[","'").replace("]","'");
								if(selInvListMap.contains(","))
								{
									selInvListMap=selInvListMap.replaceAll(", ","','");
								}
								System.out.println("selInvListMap>>>>"+selInvListMap);
								
								String selectedInvArray[] = null;
								String selInvMapArray[] = null;
								String transitInvArray[] = null;
								
								selectedInvArray = selectedInvList.split(",");
								System.out.println( "CustStockGWTIC:::selectedInvArray:::"+ selectedInvArray);
								selInvMapArray = selInvListMap.split(",");
								System.out.println( "CustStockGWTIC:::selInvMapArray:::"+ selInvMapArray);
								transitInvArray = transitInvList.split(",");
								System.out.println( "CustStockGWTIC:::transitInvArray:::"+ transitInvArray);
								selInvListMapFinal="";
								transitInvListFinal="";
								
								for(String selInv : selInvMapArray)
								{
									System.out.println( "CustStockGWTIC:::selInv:::"+ selInv);
									for( String selectedInv : selectedInvArray )
									{
										System.out.println( "CustStockGWTIC:::selectedInv:::"+ selectedInv);
										if(selInv.equalsIgnoreCase(selectedInv))
										{
											if(selInvListMapFinal != null && selInvListMapFinal.trim().length() > 0)
											{
												selInvListMapFinal=selInvListMapFinal+","+selInv;
											}
											else
											{
												selInvListMapFinal=selInv;
											}
											System.out.println( "CustStockGWTIC:::selInvListMapFinal:::"+ selInvListMapFinal);
										}
									}
									for( String transitInv : transitInvArray )
									{
										System.out.println( "CustStockGWTIC:::itemchanged:::"+ selInv);
										if(selInv.equalsIgnoreCase(transitInv))
										{
											if(transitInvListFinal != null && transitInvListFinal.trim().length() > 0)
											{
												transitInvListFinal=transitInvListFinal+","+selInv;
											}
											else
											{
												transitInvListFinal=selInv;
											}
											System.out.println( "CustStockGWTIC:::transitInvListFinal:::"+ transitInvListFinal);
										}
									}
								}
								
								if(selInvListMapFinal == null || selInvListMapFinal.trim().length() == 0)
								{
									selInvListMapFinal = "''";
									System.out.println( "CustStockGWTIC:::selInvListMapFinal:::"+ selInvListMapFinal);
								}
								
								if(transitInvListFinal == null || transitInvListFinal.trim().length() == 0)
								{
									transitInvListFinal = "''";
									System.out.println( "CustStockGWTIC:::transitInvListFinal:::"+ transitInvListFinal);
								}
							}
							catch(Exception e)
							{
								System.out.println("Exception::Cust");
								e.printStackTrace();
							}
							
							rcpQtyList = GetRcpQtyCommon("R",custCodeHd,orderType,itemSerHd,siteCodeHd,selInvListMapFinal,transitInvListFinal,itemCodeLast,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							receiptLast=rcpQtyList.get(0);
							//receiptRate=rcpQtyList.get(1);
							rcpquantityVal=getRequiredDcml(rcpQtyList.get(1),3);
							
							//System.out.println("receiptRate@@@@@@@@@>>>>>>"+receiptRate);
							//receiptAmount=receiptLast*receiptRate;
							////System.out.println("Receipt quantity cal for avaible transcation id :"+receiptLast);
							System.out.println("Receipt quantity cal for avaible receiptAmount :"+receiptAmount);
							
							System.out.println("-------calculate Receipt Frees quantity for availble transcaion Id--------");
							rcpFreesLast=0.0;
							rcpFreeQtyList = GetFreeQtyCommon("R",custCodeHd,orderType,itemSerHd,siteCodeHd,selInvListMapFinal,transitInvListFinal,itemCodeLast,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							rcpFreesLast=rcpFreeQtyList.get(0);
							rcpFreesValue=getRequiredDcml(rcpFreeQtyList.get(1),3);
							System.out.println("rcpFreesLast :"+rcpFreesLast);
							//rcpFreesAmount=rcpFreesLast*receiptRate;
							//System.out.println("rcpFreesAmount :"+rcpFreesAmount);
							rcpChargableLast=0.0;
							rcpChargableLast = receiptLast - rcpFreesLast;
							System.out.println("rcpChargableLast :"+rcpChargableLast);
							
							/*------------------------------------------------------------------------------------------------------------------*/
							
							System.out.println("custCodeHd :"+custCodeHd);
							System.out.println("orderType :"+orderType);
							System.out.println("selectedInvList :"+selectedInvList);
							System.out.println("frmDateTstmp :"+frmDateTstmp);
							System.out.println("toDateTstmp :"+toDateTstmp);
							System.out.println("prdFrmDateTstmp :"+prdFrmDateTstmp);
							System.out.println("prdtoDateTstmp :"+prdtoDateTstmp);
							System.out.println("itemSerLast :"+itemSerLast);
							
							System.out.println("-------calculate Transit quantity for availble transcaion Id--------");
							transitQty=0.0;
							transitQty=0.0;
							transitQtyRate=0.0;
							
							transitQtyList=null;
							
							transitQtyList=GetRcpQtyCommon("T",custCodeHd,orderType,itemSerHd,siteCodeHd,selInvListMapFinal,transitInvListFinal,itemCodeLast,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							transitQty=transitQtyList.get(0);
							transitQuantityVal=getRequiredDcml(transitQtyList.get(1),3);//Changed by saurabh

							System.out.println("transitQty quantity cal for avaible transcation id :"+transitQty);
							System.out.println("receiptLast :"+receiptLast);
							System.out.println("transitQty :"+transitQty);
							System.out.println("totReceiptLast :"+totReceiptLast);
							
							System.out.println("-------calculate Transit Frees quantity for availble transcaion Id--------");
							transitQtyFreesLast=0.0;
							transitFreeQtyList=GetFreeQtyCommon("T",custCodeHd,orderType,itemSerHd,siteCodeHd,selInvListMapFinal,transitInvListFinal,itemCodeLast,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn, isItemSerLocal,tranIdNew);
							transitQtyFreesLast=transitFreeQtyList.get(0);
							transitFreeValue=getRequiredDcml(transitFreeQtyList.get(1),3);
							System.out.println("transitQtyFreesLast :"+transitQtyFreesLast);
							
							transitCharQty=0.0;
							transitCharQty = transitQty - transitQtyFreesLast;
							System.out.println("transitCharQty :"+transitCharQty);
							
							System.out.println("tranDate :"+tranDate);
							System.out.println("-------calculate Rate for availble transcaion Id--------");
							/*rateLast=0.0;
							rateLast = getRate(custCodeHd,tranDate,itemCodeLast,conn);
							System.out.println("rateLast :"+rateLast);*/
							cmrcpQtyList = GetRcpQtyCM("R",custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCodeLast,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal);
							System.out.println("cmrcpQtyList>>>>>>>"+cmrcpQtyList+"iTEMcODE@@@@@@"+itemCode);
							cmQuantity=cmrcpQtyList.get(0);
							cmValue=getRequiredDcml(cmrcpQtyList.get(1),3);
							
							
							descrLast="";
							sql = "Select descr from item "
										+" 	where item_code = '" + itemCodeLast + "'";
							pstmt1 = conn.prepareStatement( sql );
							rs1 = pstmt1.executeQuery( );
							if(rs1.next())
							{
								descrLast = rs1.getString( "descr" );
								
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							
								replQtyAll=0.0;
								billRetQty=0.0;
								returnTransitRepl=0.0;
								billRetQtyBonus=0.0;
								//retFreeQtyAll=0.0;
								returnTransitRepl=0;
								returnReciptRepl=0;
								replQtyList=null;
								
								replQtyList = GetReplacementList(custCodeHd,orderType,itemSerHd,siteCodeHd,selInvListMapFinal,transitInvListFinal,itemCodeLast,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdLast,tranIdNew);
								returnReciptRepl=replQtyList.get(0);
								billRetQty=replQtyList.get(1);
								returnTransitRepl=replQtyList.get(2);
								billRetQtyBonus=replQtyList.get(3);
								sretRate=replQtyList.get(4);
								returnReciptReplVal=replQtyList.get(5);
								billRetQtyVal=replQtyList.get(6);
								returnTransitReplVal=replQtyList.get(7);
								billRetQtyBonusVal=replQtyList.get(8);
								
								System.out.println("returnReciptReplVal>>"+returnReciptReplVal+"billRetQtyVal>>"+billRetQtyVal+"returnTransitReplVal>>"+returnTransitReplVal+"billRetQtyBonusVal>>"+billRetQtyBonusVal);
								
								sretRate=getRequiredDcml(sretRate,3);
								System.out.println("replQtyAll>>>>>>"+replQtyAll+">>>>>>"+itemCode);
								//sretQtyAllTot= billRetQty - replQtyAll -retFreeQtyAll ;
								sretQtyAllTot= billRetQty - returnReciptRepl -billRetQtyBonus ;
								System.out.println("sretQtyAllTot @@@@@@@@@@@!!!!!!:"+sretQtyAllTot);
								
							
								
								/*------------------------------------------------------------------------------------------------*/
							
								itemListLast.add(itemCodeLast);
							System.out.println("-------calculate Primary Sales for availble transcaion Id--------");
							primarySales=0.0;
							System.out.println("");
							/* Commented by manoj dtd 26/10/2016 to set actual value of primary sales
							 if(receiptLast > (sretQtyAllTot) )
							{
								primarySales = receiptLast - (sretQtyAllTot);
							}
							else
							{
								primarySales = receiptLast;
							}*/
							primarySales = receiptLast - (sretQtyAllTot);
							System.out.println("primarySales cal for avaible transcation id  :"+primarySales);
						
							salesLast=opStockLast+(receiptLast)+returnReciptRepl-billRetQty-clStockNew;
							System.out.println("SECONDARY SALES EDIT MODE>>>>>"+salesLast);
							
							salesOrg=opStockLast+(receiptLast)+returnReciptRepl+rcpFreesLast-billRetQty-clStockNew;
							System.out.println("salesOrg>>>>>"+salesOrg);
							/**Added by santosh*/
							/*itemCodeOfReturnInvoice =	getitemCodeOfReturnInvoiceList(tranIdLastHd,conn);
							if(!itemCodeOfReturnInvoice.contains(itemCodeLast))
							{*/
							if(receiptLast >= 0)
							{	//isPreviusItem=false;
								//isPreviusItem=isExistItem(itemCodeLast,tranIdLast,conn);
							System.out.println("@sa@>>>"+tranIdNew);
								valueXmlString.append( "<Detail3 dbID='' domID='"+itmDetCnt+"' objContext='"+currentFormNo+"' selected='Y'>\r\n" );
								if(tranIdNew.length() > 0)
								{
									valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"E\" status=\"O\" />");
									
									valueXmlString.append("<tran_id><![CDATA["+ tranIdNew+"]]></tran_id>");
										
								}
								else
								{
									valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"N\" />");
									
									valueXmlString.append("<tran_id><![CDATA["+ tranIdNew+"]]></tran_id>");
									
								}
								//01/12/16 Space removed for line no
								lineNoStr = "" + lineNoCustSt;
								//lineNoStr = lineNoStr.substring(lineNoStr.length() - 3);
								System.out.println("lineNoStr " + lineNoStr);
								valueXmlString.append("<line_no>").append("<![CDATA["+lineNoStr+"]]>").append("</line_no>");
								valueXmlString.append("<item_code sSrvCallOnChg='1'>").append("<![CDATA["+itemCodeLast+"]]>").append("</item_code>");
								setNodeValue( dom, "item_code", itemCodeLast+"" );
								valueXmlString.append("<item_ser>").append("<![CDATA["+itemSerLast+"]]>").append("</item_ser>");
								setNodeValue( dom, "item_ser", itemSerLast+"" );
								valueXmlString.append("<descr>").append("<![CDATA["+descrLast+"]]>").append("</descr>");
								setNodeValue( dom, "descr", descrLast+"" );
								valueXmlString.append("<unit>").append("<![CDATA["+unitLast+"]]>").append("</unit>");
								setNodeValue( dom, "unit", unitLast+"" );
								valueXmlString.append("<loc_type>").append("<![CDATA["+locTypeLast+"]]>").append("</loc_type>");
								setNodeValue( dom, "loc_type", locTypeLast+"" );
								if(!Double.isNaN(rateLast))
								{
									valueXmlString.append("<rate>").append("<![CDATA["+getRequiredDecimal(rateLast,3)+"]]>").append("</rate>");
									setNodeValue( dom, "rate", getRequiredDecimal(rateLast,3)+"" );
								}
								if(!Double.isNaN(rateOrg))
								{
									valueXmlString.append("<rate__org>").append("<![CDATA["+getRequiredDecimal(rateOrg,3)+"]]>").append("</rate__org>");
									setNodeValue( dom, "rate__org", getRequiredDecimal(rateOrg,3)+"" );
								}
								//valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+(long)Math.round(opStockLast)+"]]>").append("</op_stock>");
								//if(isPreviusItem || "V".equalsIgnoreCase(editFlag))
								//{
									valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+(long)Math.round(opStockLast)+"]]>").append("</op_stock>");
									setNodeValue( dom, "op_stock", (long)Math.round(opStockLast)+"" );
								/*}else
								{
									valueXmlString.append("<op_stock protect='0'>").append("<![CDATA["+(long)Math.round(opStockLast)+"]]>").append("</op_stock>");
									setNodeValue( dom, "op_stock", (long)Math.round(opStockLast)+"" );
								}*/
								
								
								valueXmlString.append("<purc_rcp>").append("<![CDATA["+(long)Math.round((receiptLast))+"]]>").append("</purc_rcp>");
								setNodeValue( dom, "purc_rcp", (long)Math.round((receiptLast))+"" );
								valueXmlString.append("<purc_rcp__repl>").append("<![CDATA["+(long)Math.round(returnReciptRepl)+"]]>").append("</purc_rcp__repl>");
								setNodeValue( dom, "purc_rcp__repl", (long)Math.round(returnReciptRepl)+"" );
								valueXmlString.append("<purc_rcp__free>").append("<![CDATA["+(long)Math.round((rcpFreesLast))+"]]>").append("</purc_rcp__free>");
								setNodeValue( dom, "purc_rcp__free", (long)Math.round((rcpFreesLast))+"" );
								
								valueXmlString.append("<purc_ret>").append("<![CDATA["+(long)Math.round(billRetQty)+"]]>").append("</purc_ret>");
								setNodeValue( dom, "purc_ret", (long)Math.round(billRetQty)+"" );
								valueXmlString.append("<purc_ret__repl>").append("<![CDATA[0]]>").append("</purc_ret__repl>");
								setNodeValue( dom, "purc_ret__repl", 0+"" );
								//new column
								valueXmlString.append("<purc_ret__free>").append("<![CDATA["+(long)Math.round(billRetQtyBonus)+"]]>").append("</purc_ret__free>");
								setNodeValue( dom, "purc_ret__free", (long)Math.round(billRetQtyBonus)+"" );
								valueXmlString.append("<ret_free_val>").append("<![CDATA["+(long)Math.round(billRetQtyBonusVal)+"]]>").append("</ret_free_val>");
								setNodeValue( dom, "ret_free_val", (long)Math.round(billRetQtyBonusVal)+"" );
								
								valueXmlString.append("<sales>").append("<![CDATA["+(long)Math.round(salesLast)+"]]>").append("</sales>");
								setNodeValue( dom, "sales", (long)Math.round(salesLast)+"" );
								if(tranIdNew.length() > 0)
								{
									if("V".equalsIgnoreCase(editFlag))
									{
										valueXmlString.append("<cl_stock protect='1'>").append("<![CDATA["+(long)Math.round(clStockNew)+"]]>").append("</cl_stock>");
										setNodeValue( dom, "cl_stock", (long)Math.round(clStockNew)+"" );
									}else
									{
										valueXmlString.append("<cl_stock protect='0'>").append("<![CDATA["+(long)Math.round(clStockNew)+"]]>").append("</cl_stock>");
										setNodeValue( dom, "cl_stock", (long)Math.round(clStockNew)+"" );
									}
								}
								else
								{
									valueXmlString.append("<cl_stock protect='0'>").append("<![CDATA[0]]>").append("</cl_stock>");
									setNodeValue( dom, "cl_stock", 0+"" );
								}
								//Added by saurabh[08/12/16]
								valueXmlString.append("<transit_qty>").append("<![CDATA["+(long)Math.round(transitQty)+"]]>").append("</transit_qty>");//
								setNodeValue( dom, "transit_qty", (long)Math.round(transitQty)+"" );
								valueXmlString.append("<transit_bill_val>").append("<![CDATA["+(long)Math.round(transitQuantityVal)+"]]>").append("</transit_bill_val>");//transitQuantityVal
								setNodeValue( dom, "transit_bill_val", (long)Math.round(transitQuantityVal)+"" );
								
								valueXmlString.append("<transit_qty__repl>").append("<![CDATA["+(long)Math.round(returnTransitRepl)+"]]>").append("</transit_qty__repl>");
								setNodeValue( dom, "transit_qty__repl", (long)Math.round(returnTransitRepl)+"" );
								valueXmlString.append("<transit_repl_val>").append("<![CDATA["+(long)Math.round(returnTransitRepl*sretRate)+"]]>").append("</transit_repl_val>");
								setNodeValue( dom, "transit_repl_val", (long)Math.round(returnTransitRepl*sretRate)+"" );
								//New column added
								
								valueXmlString.append("<transit_qty__free>").append("<![CDATA["+(long)Math.round(transitQtyFreesLast)+"]]>").append("</transit_qty__free>");
								setNodeValue( dom, "transit_qty__free", (long)Math.round(transitQtyFreesLast)+"" );
								valueXmlString.append("<transit_free_val>").append("<![CDATA["+(long)Math.round(transitFreeValue)+"]]>").append("</transit_free_val>");//New column added
								setNodeValue( dom, "transit_free_val", (long)Math.round(transitFreeValue)+"" );
								
								valueXmlString.append("<primary_sales>").append("<![CDATA["+(long)Math.round(primarySales)+"]]>").append("</primary_sales>");
								setNodeValue( dom, "primary_sales", (long)Math.round(primarySales)+"" );
								valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
								valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
								valueXmlString.append("<chg_date>").append("<![CDATA["+sdf.format(currentDate)+"]]>").append("</chg_date>");
								

								valueXmlString.append("<rate_old>").append("<![CDATA["+getRequiredDecimal(rateOld,3)+"]]>").append("</rate_old>");
								setNodeValue( dom, "rate_old", getRequiredDecimal(rateOld,3)+"" );
								valueXmlString.append("<rate_org_old>").append("<![CDATA["+getRequiredDecimal(rateOrgOld,3)+"]]>").append("</rate_org_old>");
								setNodeValue( dom, "rate_org_old", getRequiredDecimal(rateOrgOld,3)+"" );
								//Added by saurabh [06/12/16]
								valueXmlString.append("<rcp_value>").append("<![CDATA["+getRequiredDecimal(rcpquantityVal,3)+"]]>").append("</rcp_value>");
								setNodeValue( dom, "rcp_value", getRequiredDecimal(rcpquantityVal,3)+"" );
								valueXmlString.append("<rcp_val>").append("<![CDATA["+getRequiredDecimal(rcpquantityVal,3)+"]]>").append("</rcp_val>");
								setNodeValue( dom, "rcp_val", getRequiredDecimal(rcpquantityVal,3)+"" );

								valueXmlString.append("<repl_value>").append("<![CDATA["+getRequiredDecimal(returnReciptReplVal,3)+"]]>").append("</repl_value>");
								setNodeValue( dom, "repl_value", getRequiredDecimal(returnReciptReplVal,3)+"" );
								valueXmlString.append("<rcp_repl_val>").append("<![CDATA["+getRequiredDecimal(returnReciptReplVal,3)+"]]>").append("</rcp_repl_val>");
								setNodeValue( dom, "rcp_repl_val", getRequiredDecimal(returnReciptReplVal,3)+"" );
		
								valueXmlString.append("<sret_vale>").append("<![CDATA["+getRequiredDecimal(billRetQtyVal,3)+"]]>").append("</sret_vale>");
								setNodeValue( dom, "sret_vale", getRequiredDecimal(billRetQtyVal,3)+"" );
								valueXmlString.append("<ret_val>").append("<![CDATA["+getRequiredDecimal(billRetQtyVal,3)+"]]>").append("</ret_val>");
								setNodeValue( dom, "ret_val", getRequiredDecimal(billRetQtyVal,3)+"" );
								//Added by saurabh [06/12/16]
								valueXmlString.append("<cm_invoice>").append("<![CDATA["+getRequiredDecimal(cmQuantity,3)+"]]>").append("</cm_invoice>");
								setNodeValue( dom, "cm_invoice", getRequiredDecimal(cmQuantity,3)+"" );
								valueXmlString.append("<cm_value>").append("<![CDATA["+getRequiredDecimal(cmValue,3)+"]]>").append("</cm_value>");
								setNodeValue( dom, "cm_value", getRequiredDecimal(cmValue,3)+"" );
								valueXmlString.append("<secondary_value>").append("<![CDATA["+0+"]]>").append("</secondary_value>");
								setNodeValue( dom, "secondary_value", 0+"" );
								valueXmlString.append("<formula_value>").append("<![CDATA["+0+"]]>").append("</formula_value>");
								setNodeValue( dom, "formula_value", 0+"" );
								
								valueXmlString.append("<free_value>").append("<![CDATA["+getRequiredDecimal(rcpFreesValue,3)+"]]>").append("</free_value>");
								setNodeValue( dom, "free_value", getRequiredDecimal(rcpFreesValue,3)+"" );
								valueXmlString.append("<rcp_free_val>").append("<![CDATA["+getRequiredDecimal(rcpFreesValue,3)+"]]>").append("</rcp_free_val>");
								setNodeValue( dom, "rcp_free_val", getRequiredDecimal(rcpFreesValue,3)+"" );
								
								valueXmlString.append("<sales__org>").append("<![CDATA["+(long)Math.round(salesOrg)+"]]>").append("</sales__org>");
								setNodeValue( dom, "sales__org", (long)Math.round(salesOrg)+"" );
								valueXmlString.append("<pur_value>").append("<![CDATA["+getRequiredDecimal(rcpquantityVal, 3)+"]]>").append("</pur_value>");
								setNodeValue( dom, "pur_value", getRequiredDecimal(rcpquantityVal, 3)+"" );
								
								valueXmlString.append("<op_value>").append("<![CDATA["+getRequiredDecimal(opValue,3)+"]]>").append("</op_value>");
								setNodeValue( dom, "op_value", getRequiredDecimal(opValue,3)+"" );
								valueXmlString.append("<sales_value>").append("<![CDATA["+getRequiredDecimal(salesValue,3)+"]]>").append("</sales_value>");
								setNodeValue( dom, "sales_value", getRequiredDecimal(salesValue,3)+"" );
								valueXmlString.append("<cl_value>").append("<![CDATA["+getRequiredDecimal(clValue,3)+"]]>").append("</cl_value>");
								setNodeValue( dom, "cl_value", getRequiredDecimal(clValue,3)+"" );
								
								//setNodeValue( dom, "cl_stock", clStockNew+"" );
								if(!"V".equalsIgnoreCase(editFlag))
								{
								reStr=itemChanged(dom,dom1, dom2, objContext,"cl_stock",editFlag,xtraParams,"",null);
								pos = reStr.indexOf("<Detail3>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail3>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
								}
								valueXmlString.append("</Detail3>");
								}
								//valueXmlString.append("</Detail3>");
								
							//}	
					
							System.out.println("Detail4 cal for avaible transcation id  :"+valueXmlString);
							
						}//end of while
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
					//}
					}
					else
					{
						System.out.println("-----coding for those item code which is other than previos item code,start !!!!!!!!!!!!!!!");
						
						//orderDtTstmp=java.sql.Timestamp.valueOf(genericUtility.getValidDateString(tranDateHd, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
						System.out.println("------retrive Item data which is not exist in last month or current moth transcation !!!!");
						//TODO
						//Parameters[ItemMap with closing stock and objName] added for Awacs process [280717|Start]
						//added by ketan for testing
						defData1 = getItemFromCust(custCodeHd,itemSerDom,itemSerHd,siteCodeHd,orderType,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,tranIdLast,tranIdNew,selectedInvList,conn,calCriItemSerIn,itemListLast,isItemSerLocal,dataMap,objName);
						//Parameters[ItemMap with closing stock and objName] added for Awacs process [280717|End]
						System.out.println("itemResLen before : "+defData1.size());
						System.out.println("itemResLen before: "+defData1);
						//Add prevous items
						//Added by saurabh for product transfer[22/03/17|Start]
						//defData=addPreviousItems(itemListLast,defData,tranIdLast,conn);
						
						//defData=addPreviousItems(custCodeHd,itemSerDom,isItemSerLocal,frmDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,itemListLast,defData,tranIdLast,conn);
						defData=addPreviousItems(custCodeHd,itemSerDom,itemSerHd,isItemSerLocal,frmDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,itemListLast,defData1,tranIdLast,conn,isItemSerLocal);
						//Added by saurabh for product transfer[22/03/17|End]
						//Add prevous items
						
						itemResLen = defData.size();
						System.out.println("itemResLen after: "+defData);
						System.out.println("itemResLen after: "+itemResLen);
						if(defData.size()>0)
						{
							Collections.sort(defData, ItemDescr.ItemNameComparator);
						}
						
						for(itemPos = 0; itemPos < itemResLen; itemPos++ )
						{	
							itemCode = ( ( ItemDescr )defData.get(itemPos)).lsCode; 
							System.out.println("itemCode @@@@@:"+itemCode);
							lsOpStock= checkNull(( ( ItemDescr )defData.get(itemPos)).lsOpStock);
							System.out.println("obStock :"+lsOpStock);
							lsTranId = checkNull(( ( ItemDescr )defData.get(itemPos)).lsTranId);
							System.out.println("obStock :"+lsTranId);
							lsrateold = checkNull(( ( ItemDescr )defData.get(itemPos)).lsrateold);
							System.out.println("lsrateold :"+lsrateold);
							lsrateorgold = checkNull(( ( ItemDescr )defData.get(itemPos)).lsrateorgold);
							System.out.println("lsrateorgold :"+lsrateorgold);
							
							itemSer = ( ( ItemDescr )defData.get(itemPos)).lsItemSer; 
							System.out.println("itemSer @@@@@:"+itemSer);
							
							if(lsOpStock.length() > 0)
							{
								opStkItem=Double.parseDouble(lsOpStock);
							}
							else
							{
								opStkItem=0.0;
							}
							 
							System.out.println("opStkItem :"+opStkItem);
							System.out.println("@S@ after addPreviousItems"+itemListLast+"itemCode"+itemCode);
						if(!(itemListLast.contains(itemCode)))
						{
							System.out.println("itemCode of which is not available in last or current transcation "+itemCode);
							lineNoItmCnt++;
							//tranDtTstmp  = orderDtTstmp;		
						
							//itemSerDet = ( new DistCommon() ).getItemSer( itemCode, siteCodeHd, tranDtTstmp, custCodeHd, "C", conn );
							System.out.println("itemSer :"+itemSer);
							
							descrItem="";
							unitItem="";
							locTypeItem="";
							//rate=0.0;
						    sql = "Select descr, unit, loc_type ,sale_rate from item "
								+" 	where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery( );
							if( rs.next() )
							{
								descrItem = rs.getString( "descr" );
								unitItem = rs.getString( "unit" );
								locTypeItem = rs.getString( "loc_type");
								//rate = rs.getDouble( "sale_rate");
								System.out.println("descrItem :"+descrItem);
								System.out.println("mUnit :"+unitItem);
								System.out.println("mlType :"+locTypeItem);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						
							
							/*------------------------------RECEIPT QUANTITY CALCULATION-----------------------------------------------------*/
						    System.out.println("-- calculate recipt quantity which is not available for last or current  month transcation !!!");
							//rcpQty=0.0;
							System.out.println("frmDateTstmp :"+frmDateTstmp);
							System.out.println("toDateTstmp :"+toDateTstmp);
							rcpQtyAll=0.0;
							//receiptRate=0.0;
							rcpquantityVal=0.0;
							rcpQtyList=null;
							rcpQtyList = GetRcpQtyCommon("R",custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							rcpQtyAll=rcpQtyList.get(0);
							rcpquantityVal=rcpQtyList.get(1);
							rcpquantityVal=getRequiredDcml(rcpquantityVal,3);
							
							rcpFreesAll=0.0;
							rcpFreeQtyList=null;
							rcpFreeQtyList = GetFreeQtyCommon("R",custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							rcpFreesAll=rcpFreeQtyList.get(0);
							rcpFreesValue=rcpFreeQtyList.get(1);
							rcpFreesValue=getRequiredDcml(rcpFreesValue,3);
							System.out.println("rcpFreesAll :"+rcpFreesAll);
							
							//receiptRate = GetRateCommon(custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn);
							//System.out.println("receiptRate :"+receiptRate+"itemcode@@@@@@@"+itemCode);
							
							
							/*------------------------------------------------------------------------------------------------------------------------*/
							
							System.out.println("--------calculate All purchase return which other than previous or current transcation item code---------- ");
							
								replQtyAll=0.0;
								billRetQty=0.0;
								returnTransitRepl=0.0;
								billRetQtyBonus=0.0;
								//retFreeQtyAll=0.0;
								sretRate=0.0;
								replQtyList=null;
								replQtyList = GetReplacementList(custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdLast,tranIdNew);
								replQtyAll=replQtyList.get(0);
								billRetQty=replQtyList.get(1);
								returnTransitRepl=replQtyList.get(2);
								billRetQtyBonus=replQtyList.get(3);
								sretRate=replQtyList.get(4);
								returnReciptReplVal=replQtyList.get(5);
								billRetQtyVal=replQtyList.get(6);
								returnTransitReplVal=replQtyList.get(7);
								billRetQtyBonusVal=replQtyList.get(8);
								
								System.out.println("returnReciptReplVal>>"+returnReciptReplVal+"billRetQtyVal>>"+billRetQtyVal+"returnTransitReplVal>>"+returnTransitReplVal+"billRetQtyBonusVal>>"+billRetQtyBonusVal);
								
								sretRate=getRequiredDcml(sretRate,3);
								System.out.println("replQtyAll>>>>>>"+replQtyAll+">>>>>>"+itemCode+"Rate>>>>>"+sretRate);
								sretQtyAllTot= billRetQty - replQtyAll -billRetQtyBonus ;
								System.out.println("sretQtyAllTot @@@@@@@@@@@!!!!!!:"+sretQtyAllTot);
								
								/*
								System.out.println("retQtyAll :"+retQtyAll);
								System.out.println("replQty :"+replQty);
								sretQtyAllTot= retQtyAll - replQtyAll -retFreeQtyAll ;
								System.out.println("sretQtyAllTot !!!!!!:"+sretQtyAllTot);
							
								System.out.println("Return quantity for not available in last or current tranaction"+"itemCode :"+itemCode +"Return Quantity :"+sretQtyAllTot);
								*/
								
			  				
							transitQty=0.0;
							//transitQtyBon=0.0;
							System.out.println("--calculate primary sales which is not included for current or previous month transcation------");
							primarySalesAll=0.0;
							/*
							 Commented by Manoj dtd 26/10/2016 to set Actual Primary Sales
							 if(rcpQtyAll > (sretQtyAllTot ))
							{
								primarySalesAll = rcpQtyAll - (sretQtyAllTot );
							}
							else
							{
								primarySalesAll = rcpQtyAll;
							}*/
							primarySalesAll= rcpQtyAll - (sretQtyAllTot );
							System.out.println("Primary Sales for not available in last or current tranaction"+"itemCode :"+itemCode +"primarySales :"+primarySalesAll);
							
							System.out.println("--calculate Transit Quantoty which is not included for current or previous month transcation------");
							transitQtyAll=0.0;
							transitQtyRate=0.0;
							transitQtyList=null;
							transitQtyList = GetRcpQtyCommon("T",custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							transitQtyAll=transitQtyList.get(0);
							//transitQuantityVal=getRequiredDcml(rcpQtyList.get(1),3);
							transitQuantityVal=getRequiredDcml(transitQtyList.get(1),3);//Changed by saurabh
							//transitQtyRate=transitQtyList.get(1);
							System.out.println("transitQtyAll@@@@@@@"+transitQtyAll+">>>>>>"+itemSerHd);
							System.out.println("transitQtyRate@@@@@@@"+transitQtyRate);
							//transitQtyAll=GetTransitQty(custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn);
							System.out.println("Transit quantity for not available in last or current tranaction"+"itemCode :"+itemCode +"Transit Quantity :"+transitQtyAll);
							System.out.println("--calculate Transit Frees Quantoty which is not included for current or previous month transcation------");
							transitQtyFrees=0.0;
							transitFreeQtyList=null;
							transitFreeQtyList = GetFreeQtyCommon("T",custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal,tranIdNew);
							transitQtyFrees=transitFreeQtyList.get(0);
							transitFreeValue=getRequiredDcml(transitFreeQtyList.get(1),3);
							
							System.out.println("transitQtyFrees :"+transitQtyFrees);
							transitCharQtyAll = 0.0;
							transitCharQtyAll = transitQtyAll - transitQtyFrees;
							System.out.println("transitCharQtyAll :"+transitCharQtyAll);
							
							cmrcpQtyList=null;
							cmrcpQtyList = GetRcpQtyCM("R",custCodeHd,orderType,itemSerHd,siteCodeHd,selectedInvList,transitInvList,itemCode,frmDateTstmp,toDateTstmp,prdFrmDateTstmp,prdtoDateTstmp,conn,calCriItemSerIn,isItemSerLocal);
							System.out.println("cmrcpQtyList>>>>>>>"+cmrcpQtyList+"iTEMcODE@@@@@@"+itemCode);
							cmQuantity=cmrcpQtyList.get(0);
							cmValue=getRequiredDcml(cmrcpQtyList.get(1),3);
							
							/*
							rateAll=0.0;
							rateAll = getRate(custCodeHd,tranDate,itemCode,conn);
							System.out.println("Rate for not available in last or current tranaction"+"itemCode :"+itemCode +"rate :"+rate);*/
							
							salesQty=0;
							salesQty=opStkItem+(rcpQtyAll)+replQtyAll-billRetQty;
							System.out.println("salesQty AFTER>>>>"+salesQty);
							salesOrg=0;
							salesOrg=opStkItem+(rcpQtyAll)+replQtyAll+rcpFreesAll-billRetQty;
							System.out.println("salesOrg>>>>"+salesOrg);
							/*if(itemSerHd!=null && itemSerHd.trim().length()>0){
							String[] ItemSerArray= itemSerHd.split("'");
								itemSer=ItemSerArray[1];
							}
							else
							{
								itemSer="";
							}*/
							itemMap.put("itemCode@"+lineNoItmCnt, itemCode);
							//itemMap.put("mItemSer@"+lineNoItmCnt, itemSerDet);
							itemMap.put("mItemSer@"+lineNoItmCnt, itemSer );//Added by saurabh[03/11/16]
							itemMap.put("mDescr@"+lineNoItmCnt, descrItem);
							itemMap.put("mUnit@"+lineNoItmCnt, unitItem);
							itemMap.put("mlType@"+lineNoItmCnt, locTypeItem);
							itemMap.put("mopStk@"+lineNoItmCnt, String.valueOf(opStkItem));
							itemMap.put("mquantRcp@"+lineNoItmCnt, String.valueOf((rcpQtyAll)));
							itemMap.put("rcpQtyBon@"+lineNoItmCnt, String.valueOf(0.0));//purc_ret__repl
							itemMap.put("rcpFreesAll@"+lineNoItmCnt, String.valueOf((rcpFreesAll)));//purc_rcp__free
							itemMap.put("mquant@"+lineNoItmCnt, String.valueOf(billRetQty));//purc_ret
							itemMap.put("retQty@"+lineNoItmCnt, String.valueOf(retQtyAll));//NO USE
							itemMap.put("retFreeQty@"+lineNoItmCnt, String.valueOf(billRetQtyBonus));//purc_ret__free
							itemMap.put("retReplQty@"+lineNoItmCnt, String.valueOf(replQtyAll));//purc_rcp__repl
							itemMap.put("msalesQty@"+lineNoItmCnt, String.valueOf(salesQty));
							itemMap.put("rate@"+lineNoItmCnt, String.valueOf(rateAll));
							
							itemMap.put("transitQty@"+lineNoItmCnt, String.valueOf(transitQtyAll));
							itemMap.put("transitQtyVal@"+lineNoItmCnt, String.valueOf(transitQuantityVal));//New column
							
							itemMap.put("transitQtyBon@"+lineNoItmCnt, String.valueOf(returnTransitRepl));//transit_qty__repl
							itemMap.put("transitQtyBonVal@"+lineNoItmCnt, String.valueOf(returnTransitReplVal));//transit_qty__repl
							
							itemMap.put("transitQtyFrees@"+lineNoItmCnt, String.valueOf(transitQtyFrees));//
							itemMap.put("transitQtyFreesVal@"+lineNoItmCnt, String.valueOf(transitFreeValue));//transitFreeValue
							
							itemMap.put("primarySales@"+lineNoItmCnt, String.valueOf(primarySalesAll));
							
							itemMap.put("oldRate@"+lineNoItmCnt, String.valueOf(lsrateold));
							itemMap.put("oldRateOrg@"+lineNoItmCnt, String.valueOf(lsrateorgold));
							itemMap.put("rcpValue@"+lineNoItmCnt, String.valueOf(rcpquantityVal));
							itemMap.put("replValue@"+lineNoItmCnt, String.valueOf(returnReciptReplVal));
							
							itemMap.put("cmInvoice@"+lineNoItmCnt, String.valueOf(cmQuantity));
							itemMap.put("cmValue@"+lineNoItmCnt, String.valueOf(cmValue));
							itemMap.put("secondaryValue@"+lineNoItmCnt, String.valueOf(0));
							itemMap.put("formulaValue@"+lineNoItmCnt, String.valueOf(0));
							itemMap.put("freeValue@"+lineNoItmCnt, String.valueOf(rcpFreesValue));
							itemMap.put("sretValue@"+lineNoItmCnt, String.valueOf(billRetQtyVal));
							
							itemMap.put("salesOrg@"+lineNoItmCnt, String.valueOf(salesOrg));
							//Added by saurabh[06/12/16]
							itemMap.put("retFreeVal@"+lineNoItmCnt, String.valueOf(billRetQtyBonusVal));//purc_ret__free
							
							//itemMap.put("purValue@"+lineNoItmCnt, String.valueOf(rcpquantityRate));
							
						 }//end of IF	
						
						}//end of for loop
						//countDetail3=0;
						System.out.println("lineNoItmCnt :"+lineNoItmCnt);
						//TODO
						for(int itemDomId = 1 ; itemDomId <= lineNoItmCnt ; itemDomId++)
						{
							//countDetail3 ++;
							System.out.println("itemListLast!!! :"+itemListLast);
							System.out.println("itemCode : "+itemMap.get("itemCode@"+itemDomId));
							//valueXmlString.append( "<Detail4 dbID='' domID='"+itemDomId+"' objContext='"+currentFormNo+"' selected='N'>\r\n" );
							if(itemListLast.contains(itemMap.get("itemCode@"+itemDomId)))
							{
								System.out.println("item code in list!!!!!"+itemMap.get("itemCode@"+itemDomId));
								//valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"E\" status=\"O\" />");
							}
							else
							{
									double rcpQuantity=0.0;
									double transitQtycheck=0.0;
									rcpQuantity = Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId));
									transitQtycheck = Double.parseDouble(itemMap.get("transitQty@"+itemDomId));
									System.out.println("rcpQuantity>>>>>>>>>>"+rcpQuantity);
									System.out.println("transitQtycheck>>>>>>>>>>"+transitQtycheck);
									//if(rcpQuantity >= 0 )
									System.out.println("objName>>>>>>"+objName);
									//This code will run for transaction window from where itemchange method call with objName as null CHanged by Saurabh J.[27/03/17|Start]
									if(objName.trim().length()==0)
									{
										itmDetCnt++;
										lineNoCustSt = lineNoCustSt + 1;
										//isPreviusItem=false;
										//isPreviusItem=isExistItem(itemMap.get("itemCode@"+itemDomId),tranIdLast,conn);
										
										openingValue=0;
										//calRate=0.0;
										valueXmlString.append( "<Detail3 dbID='' domID='"+itmDetCnt+"' objContext='"+currentFormNo+"' selected='Y'>\r\n" );
										
									/*	if(tranIdLast.trim().length() == 0 && tranIdNew.trim().length() > 0 )
										{
											valueXmlString.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"E\" status=\"O\" />");
										}
										else
										{
											valueXmlString.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
										}*/
										
										valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"N\" />");
										System.out.println("@@@sa@@@>>"+tranIdNew);
										valueXmlString.append("<tran_id><![CDATA["+ tranIdNew+"]]></tran_id>");
										//01/12/16 Space removed for line no
										lineNoStrDom = "" + lineNoCustSt;
										//lineNoStrDom = lineNoStrDom.substring(lineNoStrDom.length() - 3);
										System.out.println("lineNoStrDom " + lineNoStrDom);
										
										valueXmlString.append("<line_no>").append("<![CDATA["+lineNoStrDom+"]]>").append("</line_no>");
										valueXmlString.append("<item_code sSrvCallOnChg='1'>").append("<![CDATA["+itemMap.get("itemCode@"+itemDomId)+"]]>").append("</item_code>");
										valueXmlString.append("<item_ser>").append("<![CDATA["+itemMap.get("mItemSer@"+itemDomId)+"]]>").append("</item_ser>");
										valueXmlString.append("<descr>").append("<![CDATA["+itemMap.get("mDescr@"+itemDomId)+"]]>").append("</descr>");
										valueXmlString.append("<unit>").append("<![CDATA["+itemMap.get("mUnit@"+itemDomId)+"]]>").append("</unit>");
										valueXmlString.append("<loc_type>").append("<![CDATA["+itemMap.get("mlType@"+itemDomId)+"]]>").append("</loc_type>");
										valueXmlString.append("<rate>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rate@"+itemDomId).toString()))+"]]>").append("</rate>");
										//valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"]]>").append("</op_stock>");
										//if(isPreviusItem || "V".equalsIgnoreCase(editFlag))
										//{
											valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"]]>").append("</op_stock>");
										/*}else
										{
											valueXmlString.append("<op_stock protect='0'>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"]]>").append("</op_stock>");
										}*/
										
										if("V".equalsIgnoreCase(editFlag))
										{
											valueXmlString.append("<cl_stock protect='1'>").append("<![CDATA[0]]>").append("</cl_stock>");
										}else
										{
											valueXmlString.append("<cl_stock protect='0'>").append("<![CDATA[0]]>").append("</cl_stock>");
										}
										
										valueXmlString.append("<purc_rcp>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString()))+"]]>").append("</purc_rcp>");
										valueXmlString.append("<purc_rcp__repl>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString()))+"]]>").append("</purc_rcp__repl>");
										valueXmlString.append("<purc_rcp__free>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rcpFreesAll@"+itemDomId).toString()))+"]]>").append("</purc_rcp__free>");
										
										
										valueXmlString.append("<purc_ret>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString()))+"]]>").append("</purc_ret>");
										valueXmlString.append("<purc_ret__repl >").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rcpQtyBon@"+itemDomId).toString()))+"]]>").append("</purc_ret__repl>");
										
										//Added by saurabh[06/12/16]
										valueXmlString.append("<purc_ret__free>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("retFreeQty@"+itemDomId).toString()))+"]]>").append("</purc_ret__free>");
										valueXmlString.append("<ret_free_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("retFreeVal@"+itemDomId).toString()))+"]]>").append("</ret_free_val>");
										//Added by saurabh[06/12/16]
										
										valueXmlString.append("<sales>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("msalesQty@"+itemDomId).toString()))+"]]>").append("</sales>");
										
										//Added by saurabh[06/12/16]
										valueXmlString.append("<transit_qty>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQty@"+itemDomId).toString()))+"]]>").append("</transit_qty>");
										valueXmlString.append("<transit_bill_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyVal@"+itemDomId).toString()))+"]]>").append("</transit_bill_val>");
										//Added by saurabh[06/12/16]
										
										valueXmlString.append("<transit_qty__free>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyFrees@"+itemDomId).toString()))+"]]>").append("</transit_qty__free>");
										valueXmlString.append("<transit_free_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyFreesVal@"+itemDomId).toString()))+"]]>").append("</transit_free_val>");//New column added
										
										valueXmlString.append("<transit_qty__repl>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyBon@"+itemDomId).toString()))+"]]>").append("</transit_qty__repl>");
										valueXmlString.append("<transit_repl_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyBonVal@"+itemDomId).toString()))+"]]>").append("</transit_repl_val>");
										//New column
										
										valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
										valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
										valueXmlString.append("<chg_date>").append("<![CDATA["+sdf.format(currentDate)+"]]>").append("</chg_date>");
										valueXmlString.append("<primary_sales>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("primarySales@"+itemDomId).toString()))+"]]>").append("</primary_sales>");
										
										valueXmlString.append("<rate_old>").append("<![CDATA["+itemMap.get("oldRate@"+itemDomId).toString()+"]]>").append("</rate_old>");
										valueXmlString.append("<rate_org_old>").append("<![CDATA["+itemMap.get("oldRateOrg@"+itemDomId).toString()+"]]>").append("</rate_org_old>");
										//Added by saurabh[06/12/16|Start]
										valueXmlString.append("<rcp_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_value>");
										valueXmlString.append("<rcp_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_val>");
										
										valueXmlString.append("<repl_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()),3)+"]]>").append("</repl_value>");
										valueXmlString.append("<rcp_repl_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_repl_val>");
		
										valueXmlString.append("<free_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()),3)+"]]>").append("</free_value>");
										valueXmlString.append("<rcp_free_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_free_val>");
										
										valueXmlString.append("<sret_vale>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()),3)+"]]>").append("</sret_vale>");
										valueXmlString.append("<ret_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()),3)+"]]>").append("</ret_val>");
										//Added by saurabh[06/12/16|End]
										valueXmlString.append("<cm_invoice>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("cmInvoice@"+itemDomId).toString()),3)+"]]>").append("</cm_invoice>");
										valueXmlString.append("<cm_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("cmValue@"+itemDomId).toString()),3)+"]]>").append("</cm_value>");
										valueXmlString.append("<secondary_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("secondaryValue@"+itemDomId).toString()),3)+"]]>").append("</secondary_value>");
										valueXmlString.append("<formula_value>").append("<![CDATA["+itemMap.get("formulaValue@"+itemDomId).toString()+"]]>").append("</formula_value>");
										
										
										valueXmlString.append("<pur_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3)+"]]>").append("</pur_value>");
										//valueXmlString.append("<pur_value>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()))+"]]>").append("</pur_value>");
										System.out.println("isItemSer>>>>>>>"+isItemSerLocal);
										//Commented by saurabh-040117
										//calRate=Double.parseDouble(itemMap.get("oldRate@"+itemDomId).toString());
										/*if(isItemSerLocal)
										{
											calRate=getOpeningRate(orderTypeHeader,itemSerHeaderSplit,selectedInvList,(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString())),itemMap.get("itemCode@"+itemDomId),prdtoDateTmstmp,prdFromoDateTmstmp,custCodeStatic,conn);
											System.out.println("isItemSer calRate"+calRate);
											openingValue=getRequiredDcml((Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))*(calRate),3);
											System.out.println("isItemSer salesopeningValue>>>>>>"+openingValue);
										}else
										{*/
											openingValue=getRequiredDcml((Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))*(Double.parseDouble(itemMap.get("oldRate@"+itemDomId).toString())),3);
											System.out.println("net secondary salesopeningValue>>>>>>"+openingValue);
										//}
										//Commented by saurabh-040117
										salesValueCal=openingValue+Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString())
													+ Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString())
													- Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString());
										valueXmlString.append("<op_value>").append("<![CDATA["+getRequiredDecimal(openingValue, 3)+"]]>").append("</op_value>");
										valueXmlString.append("<sales_value>").append("<![CDATA["+ getRequiredDecimal(salesValueCal,3)+"]]>").append("</sales_value>");
										valueXmlString.append("<cl_value>").append("<![CDATA[0]]>").append("</cl_value>");
		
										
										System.out.println("opening>>"+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString())));
										System.out.println("mquantRcp>>>"+(long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString())));
										System.out.println("retReplQty>>>"+(long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString())));
										System.out.println("mquantRcp>>>"+(long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString())));
										System.out.println("free>>>"+(long)Math.round(Double.parseDouble(itemMap.get("retFreeQty@"+itemDomId).toString())));
										
										grossSecondaryQty=(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+
												(long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString()))+
												(long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString()))+
												(long)Math.round(Double.parseDouble(itemMap.get("rcpFreesAll@"+itemDomId).toString()))-
												(long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString()));
											System.out.println("grossSecondaryQty>>>"+grossSecondaryQty);
										System.out.println("openingValue>>>>>>"+openingValue);
										System.out.println("rcp value>>>>>>"+Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()));
										System.out.println("repl value>>>>>>"+Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()));
										System.out.println("free value>>>>>>"+Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()));
										System.out.println("sret value>>>>>>"+Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()));
										//Commented by saurabh-040117
										/*if(isItemSerLocal)
										{
											grossOpeningValue=getRequiredDcml((Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))*(calRate),3);
											System.out.println("CAL_CRIT_ITEMSER true grossOpeningValue>>>>>>"+grossOpeningValue);
										}else
										{*/
											grossOpeningValue=getRequiredDcml(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString())*Double.parseDouble(itemMap.get("oldRateOrg@"+itemDomId).toString()),3);
											//System.out.println(" CAL_CRIT_ITEMSER false grossOpeningValue>>>>>>"+grossOpeningValue);
										//}
										//Commented by saurabh-040117
										//grosSalesValueCal= getRequiredDcml(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString())*Double.parseDouble(itemMap.get("oldRateOrg@"+itemDomId).toString()),3)
										grosSalesValueCal= grossOpeningValue
															+Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString())
															+Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString())
															+Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString())
															-Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString());
										grosSalesValueCal= getRequiredDcml(grosSalesValueCal,3);
										
										System.out.println("grosSalesValueCal>>>>"+grosSalesValueCal);
										if(grossSecondaryQty>0)
										{
											grossSecondaryRateCal=grosSalesValueCal/grossSecondaryQty;
											
										}else
										{
											grossSecondaryRateCal=0;
											
										}
										valueXmlString.append("<rate__org>").append("<![CDATA["+getRequiredDecimal(grossSecondaryRateCal, 3)+"]]>").append("</rate__org>");
										valueXmlString.append("<sales__org>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("salesOrg@"+itemDomId).toString()))+"]]>").append("</sales__org>");
										valueXmlString.append("</Detail3>");
										
									}
									//This code will run for transaction window from where itemchange method call with objName as null CHanged by Saurabh J.[27/03/17|End]
									//This code will run for auto generated transactions from where itemchange method call with objName as respective objName CHanged by Saurabh J.[27/03/17|Start]
									else
									{
										System.out.println("tranIdNew>>>>>>>>>>>>"+tranIdNew);
										itmDetCnt++;
										lineNoCustSt = lineNoCustSt + 1;
										openingValue=0;
										//calRate=0.0;
										valueXmlString.append( "<Detail3 dbID='' domID='"+itmDetCnt+"' objContext='"+currentFormNo+"' selected='Y'>\r\n" );
										valueXmlString.append("<attribute pkNames=\"\" selected=\"Y\" updateFlag=\"A\" status=\"N\" />");
										valueXmlString.append("<tran_id><![CDATA["+ tranIdNew+"]]></tran_id>");
										lineNoStrDom = "" + lineNoCustSt;
										System.out.println("lineNoStrDom " + lineNoStrDom);
										valueXmlString.append("<line_no>").append("<![CDATA["+lineNoStrDom+"]]>").append("</line_no>");
										valueXmlString.append("<item_code sSrvCallOnChg='1'>").append("<![CDATA["+itemMap.get("itemCode@"+itemDomId)+"]]>").append("</item_code>");
										System.out.println("aaaaaaa>>>"+itemMap.get("itemCode@"+itemDomId));
										setNodeValue( dom, "item_code", itemMap.get("itemCode@"+itemDomId)+"");
										valueXmlString.append("<item_ser>").append("<![CDATA["+itemMap.get("mItemSer@"+itemDomId)+"]]>").append("</item_ser>");
										setNodeValue( dom, "item_ser", itemMap.get("mItemSer@"+itemDomId)+"");
										valueXmlString.append("<descr>").append("<![CDATA["+itemMap.get("mDescr@"+itemDomId)+"]]>").append("</descr>");
										setNodeValue( dom, "descr", itemMap.get("mDescr@"+itemDomId)+"");
										valueXmlString.append("<unit>").append("<![CDATA["+itemMap.get("mUnit@"+itemDomId)+"]]>").append("</unit>");
										setNodeValue( dom, "unit", itemMap.get("mUnit@"+itemDomId)+"");
										valueXmlString.append("<loc_type>").append("<![CDATA["+itemMap.get("mlType@"+itemDomId)+"]]>").append("</loc_type>");
										setNodeValue( dom, "loc_type", itemMap.get("mlType@"+itemDomId)+"");
										valueXmlString.append("<rate>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rate@"+itemDomId).toString()))+"]]>").append("</rate>");
										setNodeValue( dom, "rate", (long)Math.round(Double.parseDouble(itemMap.get("rate@"+itemDomId).toString()))+"");
										valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"]]>").append("</op_stock>");
										setNodeValue( dom, "op_stock", (long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"");
										//Added for AWACS process closing stock logic [290717|Start]
										if("awacs_to_es3_prc".equalsIgnoreCase(objName))
										{
											if(dataMap.get(itemMap.get("itemCode@"+itemDomId)) != null)
											{
												awacsClStk=0.0;
												awacsClStk = (long)Math.round(Double.parseDouble(dataMap.get(itemMap.get("itemCode@"+itemDomId)).toString()));
												System.out.println("ItemCode::::"+itemMap.get("itemCode@"+itemDomId)+":::Closing stock:::"+awacsClStk);
												valueXmlString.append("<cl_stock protect='1'>").append("<![CDATA["+awacsClStk+"]]>").append("</cl_stock>");
												setNodeValue( dom, "cl_stock", awacsClStk+"");
											}
											else
											{
												valueXmlString.append("<cl_stock protect='1'>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"]]>").append("</cl_stock>");
												setNodeValue( dom, "cl_stock", (long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"");
											}
										}
										else
										{
											valueXmlString.append("<cl_stock protect='1'>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"]]>").append("</cl_stock>");
											setNodeValue( dom, "cl_stock", (long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+"");
										}
										//Added for AWACS process closing stock logic [290717|End]
										valueXmlString.append("<purc_rcp>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString()))+"]]>").append("</purc_rcp>");
										setNodeValue( dom, "purc_rcp", (long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString()))+"");
										valueXmlString.append("<purc_rcp__repl>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString()))+"]]>").append("</purc_rcp__repl>");
										setNodeValue( dom, "purc_rcp__repl", (long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString()))+"");
										valueXmlString.append("<purc_rcp__free>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rcpFreesAll@"+itemDomId).toString()))+"]]>").append("</purc_rcp__free>");
										setNodeValue( dom, "purc_rcp__free", (long)Math.round(Double.parseDouble(itemMap.get("rcpFreesAll@"+itemDomId).toString()))+"");
										valueXmlString.append("<purc_ret>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString()))+"]]>").append("</purc_ret>");
										setNodeValue( dom, "purc_ret", (long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString()))+"");
										valueXmlString.append("<purc_ret__repl>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("rcpQtyBon@"+itemDomId).toString()))+"]]>").append("</purc_ret__repl>");
										setNodeValue( dom, "purc_ret__repl", (long)Math.round(Double.parseDouble(itemMap.get("rcpQtyBon@"+itemDomId).toString()))+"");
										valueXmlString.append("<purc_ret__free>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("retFreeQty@"+itemDomId).toString()))+"]]>").append("</purc_ret__free>");
										setNodeValue( dom, "purc_ret__free", (long)Math.round(Double.parseDouble(itemMap.get("retFreeQty@"+itemDomId).toString()))+"");
										valueXmlString.append("<ret_free_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("retFreeVal@"+itemDomId).toString()))+"]]>").append("</ret_free_val>");
										setNodeValue( dom, "ret_free_val", (long)Math.round(Double.parseDouble(itemMap.get("retFreeVal@"+itemDomId).toString()))+"");
										valueXmlString.append("<sales>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("msalesQty@"+itemDomId).toString()))+"]]>").append("</sales>");
										setNodeValue( dom, "sales", (long)Math.round(Double.parseDouble(itemMap.get("msalesQty@"+itemDomId).toString()))+"");
										valueXmlString.append("<transit_qty>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQty@"+itemDomId).toString()))+"]]>").append("</transit_qty>");
										setNodeValue( dom, "transit_qty", (long)Math.round(Double.parseDouble(itemMap.get("transitQty@"+itemDomId).toString()))+"");
										valueXmlString.append("<transit_bill_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyVal@"+itemDomId).toString()))+"]]>").append("</transit_bill_val>");
										setNodeValue( dom, "transit_bill_val", (long)Math.round(Double.parseDouble(itemMap.get("transitQtyVal@"+itemDomId).toString()))+"");
										valueXmlString.append("<transit_qty__free>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyFrees@"+itemDomId).toString()))+"]]>").append("</transit_qty__free>");
										setNodeValue( dom, "transit_qty__free", (long)Math.round(Double.parseDouble(itemMap.get("transitQtyFrees@"+itemDomId).toString()))+"");
										valueXmlString.append("<transit_free_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyFreesVal@"+itemDomId).toString()))+"]]>").append("</transit_free_val>");//New column added
										setNodeValue( dom, "transit_free_val", (long)Math.round(Double.parseDouble(itemMap.get("transitQtyFreesVal@"+itemDomId).toString()))+"");
										valueXmlString.append("<transit_qty__repl>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyBon@"+itemDomId).toString()))+"]]>").append("</transit_qty__repl>");
										setNodeValue( dom, "transit_qty__repl", (long)Math.round(Double.parseDouble(itemMap.get("transitQtyBon@"+itemDomId).toString()))+"");
										valueXmlString.append("<transit_repl_val>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("transitQtyBonVal@"+itemDomId).toString()))+"]]>").append("</transit_repl_val>");
										setNodeValue( dom, "transit_repl_val", (long)Math.round(Double.parseDouble(itemMap.get("transitQtyBonVal@"+itemDomId).toString()))+"");
										valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
										setNodeValue( dom, "chg_user", chgUser+"");
										valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
										setNodeValue( dom, "chg_term", chgTerm+"");
										valueXmlString.append("<chg_date>").append("<![CDATA["+sdf.format(currentDate)+"]]>").append("</chg_date>");
										setNodeValue( dom, "chg_date", sdf.format(currentDate)+"");
										valueXmlString.append("<primary_sales>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("primarySales@"+itemDomId).toString()))+"]]>").append("</primary_sales>");
										setNodeValue( dom, "primary_sales", (long)Math.round(Double.parseDouble(itemMap.get("primarySales@"+itemDomId).toString()))+"");
										valueXmlString.append("<rate_old>").append("<![CDATA["+itemMap.get("oldRate@"+itemDomId).toString()+"]]>").append("</rate_old>");
										setNodeValue( dom, "rate_old", itemMap.get("oldRate@"+itemDomId)+"");
										valueXmlString.append("<rate_org_old>").append("<![CDATA["+itemMap.get("oldRateOrg@"+itemDomId).toString()+"]]>").append("</rate_org_old>");
										setNodeValue( dom, "rate_org_old", itemMap.get("oldRateOrg@"+itemDomId)+"");
										valueXmlString.append("<rcp_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_value>");
										setNodeValue( dom, "rcp_value", getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3));
										valueXmlString.append("<rcp_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_val>");
										setNodeValue( dom, "rcp_val", getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3));
										valueXmlString.append("<repl_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()),3)+"]]>").append("</repl_value>");
										setNodeValue( dom, "repl_value", getRequiredDecimal(Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()),3));
										valueXmlString.append("<rcp_repl_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_repl_val>");
										setNodeValue( dom, "rcp_repl_val", getRequiredDecimal(Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()),3));
										valueXmlString.append("<free_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()),3)+"]]>").append("</free_value>");
										setNodeValue( dom, "free_value", getRequiredDecimal(Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()),3));
										valueXmlString.append("<rcp_free_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()),3)+"]]>").append("</rcp_free_val>");
										setNodeValue( dom, "rcp_free_val", getRequiredDecimal(Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()),3));
										valueXmlString.append("<sret_vale>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()),3)+"]]>").append("</sret_vale>");
										setNodeValue( dom, "sret_vale", getRequiredDecimal(Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()),3));
										valueXmlString.append("<ret_val>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()),3)+"]]>").append("</ret_val>");
										setNodeValue( dom, "ret_val", getRequiredDecimal(Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()),3));
										valueXmlString.append("<cm_invoice>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("cmInvoice@"+itemDomId).toString()),3)+"]]>").append("</cm_invoice>");
										setNodeValue( dom, "cm_invoice", getRequiredDecimal(Double.parseDouble(itemMap.get("cmInvoice@"+itemDomId).toString()),3));
										valueXmlString.append("<cm_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("cmValue@"+itemDomId).toString()),3)+"]]>").append("</cm_value>");
										setNodeValue( dom, "cm_value", getRequiredDecimal(Double.parseDouble(itemMap.get("cmValue@"+itemDomId).toString()),3));
										valueXmlString.append("<secondary_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("secondaryValue@"+itemDomId).toString()),3)+"]]>").append("</secondary_value>");
										setNodeValue( dom, "secondary_value", getRequiredDecimal(Double.parseDouble(itemMap.get("secondaryValue@"+itemDomId).toString()),3));
										valueXmlString.append("<formula_value>").append("<![CDATA["+itemMap.get("formulaValue@"+itemDomId).toString()+"]]>").append("</formula_value>");
										setNodeValue( dom, "formula_value", itemMap.get("formulaValue@"+itemDomId));
										valueXmlString.append("<pur_value>").append("<![CDATA["+getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3)+"]]>").append("</pur_value>");
										setNodeValue( dom, "pur_value", getRequiredDecimal(Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()),3));

										System.out.println("isItemSer>>>>>>>"+isItemSerLocal);
										openingValue=getRequiredDcml((Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))*(Double.parseDouble(itemMap.get("oldRate@"+itemDomId).toString())),3);
										System.out.println("net secondary salesopeningValue>>>>>>"+openingValue);
										salesValueCal=openingValue+Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString())
													+ Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString())
													- Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString());

										valueXmlString.append("<op_value>").append("<![CDATA["+getRequiredDecimal(openingValue, 3)+"]]>").append("</op_value>");
										setNodeValue( dom, "op_value", getRequiredDecimal(openingValue, 3)+"");
										valueXmlString.append("<sales_value>").append("<![CDATA["+ getRequiredDecimal(salesValueCal,3)+"]]>").append("</sales_value>");
										setNodeValue( dom, "sales_value", getRequiredDecimal(salesValueCal,3)+"");
										valueXmlString.append("<cl_value>").append("<![CDATA[0]]>").append("</cl_value>");
										setNodeValue( dom, "cl_value", "0");

										System.out.println("opening>>"+(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString())));
										System.out.println("mquantRcp>>>"+(long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString())));
										System.out.println("retReplQty>>>"+(long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString())));
										System.out.println("mquantRcp>>>"+(long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString())));
										System.out.println("free>>>"+(long)Math.round(Double.parseDouble(itemMap.get("retFreeQty@"+itemDomId).toString())));
										grossSecondaryQty=(long)Math.round(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString()))+
												(long)Math.round(Double.parseDouble(itemMap.get("mquantRcp@"+itemDomId).toString()))+
												(long)Math.round(Double.parseDouble(itemMap.get("retReplQty@"+itemDomId).toString()))+
												(long)Math.round(Double.parseDouble(itemMap.get("rcpFreesAll@"+itemDomId).toString()))-
												(long)Math.round(Double.parseDouble(itemMap.get("mquant@"+itemDomId).toString()));
											System.out.println("grossSecondaryQty>>>"+grossSecondaryQty);
										System.out.println("openingValue>>>>>>"+openingValue);
										System.out.println("rcp value>>>>>>"+Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString()));
										System.out.println("repl value>>>>>>"+Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString()));
										System.out.println("free value>>>>>>"+Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString()));
										System.out.println("sret value>>>>>>"+Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString()));
										grossOpeningValue=getRequiredDcml(Double.parseDouble(itemMap.get("mopStk@"+itemDomId).toString())*Double.parseDouble(itemMap.get("oldRateOrg@"+itemDomId).toString()),3);
										grosSalesValueCal= grossOpeningValue
															+Double.parseDouble(itemMap.get("rcpValue@"+itemDomId).toString())
															+Double.parseDouble(itemMap.get("replValue@"+itemDomId).toString())
															+Double.parseDouble(itemMap.get("freeValue@"+itemDomId).toString())
															-Double.parseDouble(itemMap.get("sretValue@"+itemDomId).toString());
										grosSalesValueCal= getRequiredDcml(grosSalesValueCal,3);
										System.out.println("grosSalesValueCal>>>>"+grosSalesValueCal);
										if(grossSecondaryQty>0)
										{
											grossSecondaryRateCal=grosSalesValueCal/grossSecondaryQty;
											
										}else
										{
											grossSecondaryRateCal=0;
										}
										valueXmlString.append("<rate__org>").append("<![CDATA["+getRequiredDecimal(grossSecondaryRateCal, 3)+"]]>").append("</rate__org>");
										setNodeValue( dom, "rate__org", getRequiredDecimal(grossSecondaryRateCal, 3));
										valueXmlString.append("<sales__org>").append("<![CDATA["+(long)Math.round(Double.parseDouble(itemMap.get("salesOrg@"+itemDomId).toString()))+"]]>").append("</sales__org>");
										setNodeValue( dom, "sales__org", (long)Math.round(Double.parseDouble(itemMap.get("salesOrg@"+itemDomId).toString()))+"");

										reStr=itemChanged(dom,dom1, dom2, objContext,"cl_stock",editFlag,xtraParams,"sec_sales_gen_prc",null);
										System.out.println("reStr:::"+reStr);
										pos = reStr.indexOf("<Detail3>");
										reStr = reStr.substring(pos + 9);
										pos = reStr.indexOf("</Detail3>");
										reStr = reStr.substring(0,pos);
										valueXmlString.append(reStr);

										valueXmlString.append("</Detail3>");

									}
									//This code will run for auto generated transactions from where itemchange method call with objName as respective objName CHanged by Saurabh J.[27/03/17|End]
							}
							
							System.out.println("valueXmlString add flag >>>  "+valueXmlString);
						}
						//Added by santosh to if invoice is not present
						/*System.out.println("@S@countDetail3["+countDetail3+"]");
						if(countDetail3==0)
						{
							valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
				            valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");  
						}*/
						//comented code
						//coding for those item code which is other than previos item code,end
						if(itemResLen == 0 && itmDetCnt==0){
						dummyProduct =  dist.getDisparams("999999","DUMMY_PRODUCT",conn);
						if (("NULLFOUND".equalsIgnoreCase(dummyProduct) || dummyProduct == null || dummyProduct.trim().length() == 0) )
						{
							System.out.println("Disparm not defined for dummy item!!!!");
						}
						else
						{
							String itemSerDummy="",descrDummy="",unit="";
							sql=" select item_ser,descr,unit from item where item_code=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, dummyProduct);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								itemSerDummy = rs.getString("item_ser"); 
								descrDummy = rs.getString("descr");
								unit = rs.getString("unit");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("In Dummy item>>");
							Node detail1Node = dom2.getElementsByTagName("Detail1").item(0);
							tranIdNew = checkNull(genericUtility.getColumnValueFromNode("tran_id", detail1Node));
							System.out.println("tranIdNew>>>>sa>>>"+tranIdNew);
							valueXmlString.append("<Detail3 dbID='' domID=\"1\" objContext=\"3\" selected=\"Y\">\r\n");
							valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
							valueXmlString.append("<tran_id><![CDATA["+ tranIdNew+"]]></tran_id>");
							valueXmlString.append("<line_no>").append("1").append("</line_no>");
							valueXmlString.append("<item_code protect='0'>").append("<![CDATA["+dummyProduct+"]]>").append("</item_code>");
							valueXmlString.append("<item_ser>").append("<![CDATA["+itemSerDummy+"]]>").append("</item_ser>");
							valueXmlString.append("<descr>").append("<![CDATA["+descrDummy+"]]>").append("</descr>");
							valueXmlString.append("<unit>").append("<![CDATA["+unit+"]]>").append("</unit>");
							valueXmlString.append("<loc_type>").append("<![CDATA[]]>").append("</loc_type>");
							valueXmlString.append("<rate>").append("<![CDATA["+0+"]]>").append("</rate>");
							valueXmlString.append("<rate__org>").append("<![CDATA["+0+"]]>").append("</rate__org>");
							valueXmlString.append("<op_stock protect='0'>").append("<![CDATA["+0+"]]>").append("</op_stock>");
							valueXmlString.append("<purc_rcp>").append("<![CDATA["+0+"]]>").append("</purc_rcp>");
							valueXmlString.append("<purc_rcp__repl>").append("<![CDATA["+0+"]]>").append("</purc_rcp__repl>");
							valueXmlString.append("<purc_rcp__free>").append("<![CDATA["+0+"]]>").append("</purc_rcp__free>");
							valueXmlString.append("<purc_ret>").append("<![CDATA["+0+"]]>").append("</purc_ret>");
							valueXmlString.append("<purc_ret__repl>").append("<![CDATA[0]]>").append("</purc_ret__repl>");
							valueXmlString.append("<purc_ret__free>").append("<![CDATA["+0+"]]>").append("</purc_ret__free>");
							valueXmlString.append("<ret_free_val>").append("<![CDATA["+0+"]]>").append("</ret_free_val>");
							valueXmlString.append("<sales>").append("<![CDATA["+0+"]]>").append("</sales>");
							valueXmlString.append("<cl_stock>").append("<![CDATA["+0+"]]>").append("</cl_stock>");
							valueXmlString.append("<transit_qty>").append("<![CDATA["+0+"]]>").append("</transit_qty>");
							valueXmlString.append("<transit_bill_val>").append("<![CDATA["+0+"]]>").append("</transit_bill_val>");//new coulmn added
							valueXmlString.append("<transit_qty__repl>").append("<![CDATA["+0+"]]>").append("</transit_qty__repl>");
							valueXmlString.append("<transit_repl_val>").append("<![CDATA["+0+"]]>").append("</transit_repl_val>");
							valueXmlString.append("<transit_qty__free>").append("<![CDATA["+0+"]]>").append("</transit_qty__free>");
							valueXmlString.append("<transit_free_val>").append("<![CDATA["+0+"]]>").append("</transit_free_val>");//new column added
							valueXmlString.append("<primary_sales>").append("<![CDATA["+0+"]]>").append("</primary_sales>");
							valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
							valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
							valueXmlString.append("<chg_date>").append("<![CDATA["+sdf.format(currentDate)+"]]>").append("</chg_date>");
							valueXmlString.append("<rate_old>").append("<![CDATA["+0+"]]>").append("</rate_old>");
							valueXmlString.append("<rate_org_old>").append("<![CDATA["+0+"]]>").append("</rate_org_old>");
							valueXmlString.append("<rcp_value>").append("<![CDATA["+0+"]]>").append("</rcp_value>");
							valueXmlString.append("<rcp_val>").append("<![CDATA["+0+"]]>").append("</rcp_val>");
							valueXmlString.append("<repl_value>").append("<![CDATA["+0+"]]>").append("</repl_value>");
							valueXmlString.append("<rcp_repl_val>").append("<![CDATA["+0+"]]>").append("</rcp_repl_val>");
							valueXmlString.append("<rcp_free_val>").append("<![CDATA["+0+"]]>").append("</rcp_free_val>");
							valueXmlString.append("<sret_vale>").append("<![CDATA["+0+"]]>").append("</sret_vale>");
							valueXmlString.append("<ret_val>").append("<![CDATA["+0+"]]>").append("</ret_val>");
							valueXmlString.append("<cm_invoice>").append("<![CDATA["+0+"]]>").append("</cm_invoice>");
							valueXmlString.append("<cm_value>").append("<![CDATA["+0+"]]>").append("</cm_value>");
							valueXmlString.append("<secondary_value>").append("<![CDATA["+0+"]]>").append("</secondary_value>");
							valueXmlString.append("<formula_value>").append("<![CDATA["+0+"]]>").append("</formula_value>");
							valueXmlString.append("<free_value>").append("<![CDATA["+0.0+"]]>").append("</free_value>");
							valueXmlString.append("<sales__org>").append("<![CDATA["+0+"]]>").append("</sales__org>");
							valueXmlString.append("<pur_value>").append("<![CDATA["+0+"]]>").append("</pur_value>");
							valueXmlString.append("<op_value>").append("<![CDATA["+0+"]]>").append("</op_value>");
							valueXmlString.append("<sales_value>").append("<![CDATA["+0+"]]>").append("</sales_value>");
							valueXmlString.append("<cl_value>").append("<![CDATA["+0+"]]>").append("</cl_value>");
							valueXmlString.append("</Detail3>");
							
						
						}
						
						}
						/*System.out.println("@S@11countDetail3["+countDetail3+"]");
						if(countDetail3==0)
						{
							valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
				            valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");  
						}
						System.out.println("itemchanged : case 3 :valueXmlString :"+valueXmlString);*/
					}
					
					/*//Change by chandra shekar on 27-10-2015
					valueXmlStringSort=sortItemDom(dom2, sortItem,conn,valueXmlString);
					valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
		            valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");  
					valueXmlString.append(valueXmlStringSort);
					//End Change by chandra shekar on 27-10-2015*/
				}//end of for loop
				if(currentColumn.trim().equals("cl_stock"))
				{
					rateOld=0.0;rateOrgOld=0.0;
					//calRate=0.0;
					Node detail2Node = dom2.getElementsByTagName("Detail1").item(0);
					itemSerHdCl = genericUtility.getColumnValueFromNode("item_ser",detail2Node);
					System.out.println("itemSerHdCl (case 3) :"+itemSerHdCl);
					System.out.println("calCriItemSerStr.trim().length()>>>>>>>."+calCriItemSerStr.trim().length());
					if(calCriItemSerStr.trim().length()>0){
						System.out.println("calCriItemSerList.contains(itemSerHd.trim())"+calCriItemSerList.contains(itemSerHdCl.trim()));
						if(calCriItemSerList.contains(itemSerHdCl.trim()))
						{
							System.out.println("Inside ItemSer true::::["+calCriItemSerList.contains(itemSerHdCl.trim())+"]");
							isItemSerLocal=true;
						}
						else{
							System.out.println("Inside ItemSer false::::["+calCriItemSerList.contains(itemSerHdCl.trim())+"]");
							isItemSerLocal=false;
						}
						}
					else
					{
							System.out.println("isItemSer:::::"+isItemSerLocal);
					}
					System.out.println("3.isItemSer@@@@@@@>>>>"+isItemSerLocal);
					
					System.out.println("in cl stock !!!!!!!!!!!!");
					clStockInp = genericUtility.getColumnValue("cl_stock",dom);
					System.out.println("clStockInp :["+clStockInp+"]");
					String itemCode1 = genericUtility.getColumnValue("item_code",dom);
					System.out.println("itemCode1 :"+itemCode1);
					rcpQtmDom = Double.parseDouble(genericUtility.getColumnValue("purc_rcp",dom));
					System.out.println("rcpQtmDom :"+rcpQtmDom);
					rcpReplQtmDom = Double.parseDouble(genericUtility.getColumnValue("purc_rcp__repl",dom));
					System.out.println("rcpReplQtmDom :"+rcpReplQtmDom);
					rcpFreeQtmDom = Double.parseDouble(genericUtility.getColumnValue("purc_rcp__free",dom));
					System.out.println("rcpFreeQtmDom :"+rcpFreeQtmDom);
					retQtyDom = Double.parseDouble(genericUtility.getColumnValue("purc_ret",dom));
					System.out.println("retQtyDom :"+retQtyDom);
					retQtyFreeDom = Double.parseDouble(genericUtility.getColumnValue("purc_ret__repl",dom));
					System.out.println("retQtyFreeDom :"+retQtyFreeDom);
					opStkDom = Double.parseDouble(genericUtility.getColumnValue("op_stock",dom));
					System.out.println("opStkDom :"+opStkDom);
					
					opValue = Double.parseDouble(genericUtility.getColumnValue("op_value",dom));
					System.out.println("opValue :"+opValue);
					
					transitQtyDom = Double.parseDouble(genericUtility.getColumnValue("transit_qty",dom));
					System.out.println("transitQtyDom :"+transitQtyDom);
					secondarySales = Double.parseDouble(genericUtility.getColumnValue("sales",dom));
					System.out.println("SECONDARYsales :"+secondarySales);
					custCodeDom = checkNull(genericUtility.getColumnValue("cust_code",dom2));
					System.out.println("custCodeDom Closing Stock :"+custCodeDom);
					//String itemSerIC = genericUtility.getColumnValue("item_ser",dom);
					tranIdTemp = checkNull(genericUtility.getColumnValue("tran_id__last",dom2));
					System.out.println("tranIdTemp Closing Stock :"+tranIdTemp);
					
					
					//Added by saurabh[06/02/17|Start]
					Node detail1Node = dom2.getElementsByTagName("Detail1").item(0);
					itemSerHd = checkNull(genericUtility.getColumnValueFromNode("item_ser",detail1Node));
					orderType = genericUtility.getColumnValueFromNode("order_type",detail1Node);//qwerty
					System.out.println("itemSerHd>>>"+itemSerHd+"orderType>>>"+orderType);
					System.out.println("@S@itemSerHd["+itemSerHd+"]");
					//itemSerHd=getItemSerList(itemSerHd,conn);
					//itemSerHeaderSplit=itemSerHd;
					//orderTypeHeader=orderType;
					//Added by saurabh[06/02/17|Start]
					
					//isPreviusItemCl=isExistItem(itemCode1,tranIdTemp,prdFrmDateTstmp,prdtoDateTstmp,itemSerHd,custCodeDom,isItemSerLocal,conn);
					//System.out.println("is Previous item cl stock:::" + isPreviusItemCl);
					
					
					if(genericUtility.getColumnValue("rate_old",dom) !=null && genericUtility.getColumnValue("rate_old",dom).trim().length()>0)
					{
						rateOld = Double.parseDouble(genericUtility.getColumnValue("rate_old",dom));
					}else
					{
						rateOld=0.0;
					}
					if(genericUtility.getColumnValue("rate_org_old",dom) !=null && genericUtility.getColumnValue("rate_org_old",dom).trim().length()>0)
					{
						rateOrgOld = Double.parseDouble(genericUtility.getColumnValue("rate_org_old",dom));
					}
					else
					{
						rateOrgOld=0.0;
					}
					
					//Added by saurabh[06/12/16]
					if(genericUtility.getColumnValue("rcp_value",dom) !=null && genericUtility.getColumnValue("rcp_value",dom).trim().length()>0)
					{
						rcpValue = Double.parseDouble(genericUtility.getColumnValue("rcp_value",dom));
					}else
					{
						rcpValue=0.0;
					}
					if(genericUtility.getColumnValue("rcp_val",dom) !=null && genericUtility.getColumnValue("rcp_val",dom).trim().length()>0)
					{
						rcpValue = Double.parseDouble(genericUtility.getColumnValue("rcp_val",dom));
					}else
					{
						rcpValue=0.0;
					}
					
					if(genericUtility.getColumnValue("repl_value",dom) !=null && genericUtility.getColumnValue("repl_value",dom).trim().length()>0)
					{
						replValue = Double.parseDouble(genericUtility.getColumnValue("repl_value",dom));
					}else
					{
						replValue=0.0;
					}
					if(genericUtility.getColumnValue("rcp_repl_val",dom) !=null && genericUtility.getColumnValue("rcp_repl_val",dom).trim().length()>0)
					{
						replValue = Double.parseDouble(genericUtility.getColumnValue("rcp_repl_val",dom));
					}else
					{
						replValue=0.0;
					}
					
					if(genericUtility.getColumnValue("sret_vale",dom) !=null && genericUtility.getColumnValue("sret_vale",dom).trim().length()>0)
					{
						retValue = Double.parseDouble(genericUtility.getColumnValue("sret_vale",dom));
					}else
					{
						retValue=0.0;
					}
					if(genericUtility.getColumnValue("ret_val",dom) !=null && genericUtility.getColumnValue("ret_val",dom).trim().length()>0)
					{
						retValue = Double.parseDouble(genericUtility.getColumnValue("ret_val",dom));
					}else
					{
						retValue=0.0;
					}

					//Added by saurabh[06/12/16]
					if(genericUtility.getColumnValue("cm_invoice",dom) !=null && genericUtility.getColumnValue("cm_invoice",dom).trim().length()>0)
					{
						cmQuantity1 = Double.parseDouble(genericUtility.getColumnValue("cm_invoice",dom));
					}else
					{
						cmQuantity1=0.0;
					}
					if(genericUtility.getColumnValue("cm_value",dom) !=null && genericUtility.getColumnValue("cm_value",dom).trim().length()>0)
					{
						cmValue1 = Double.parseDouble(genericUtility.getColumnValue("cm_value",dom));
					}else
					{
						cmValue1=0.0;
					}
					if(genericUtility.getColumnValue("secondary_value",dom) !=null && genericUtility.getColumnValue("secondary_value",dom).trim().length()>0)
					{
						secondarySalesValue = Double.parseDouble(genericUtility.getColumnValue("secondary_value",dom));
					}else
					{
						secondarySalesValue=0.0;
					}
					if(genericUtility.getColumnValue("formula_value",dom) !=null && genericUtility.getColumnValue("formula_value",dom).trim().length()>0)
					{
						formulaValue = Double.parseDouble(genericUtility.getColumnValue("formula_value",dom));
					}else
					{
						formulaValue=0.0;
					}
					if(genericUtility.getColumnValue("free_value",dom) !=null && genericUtility.getColumnValue("free_value",dom).trim().length()>0)
					{
						rcpFreeValue = Double.parseDouble(genericUtility.getColumnValue("free_value",dom));
					}else
					{
						rcpFreeValue=0.0;
					}
					
					System.out.println("rateOld@@@@@@@@"+rateOld);
					System.out.println("rateOrgOld@@@@@@@@"+rateOrgOld);
					System.out.println("rcpValue@@@@@@@@"+rcpValue);
					System.out.println("replValue@@@@@@@@"+replValue);
					System.out.println("retValue@@@@@@@@"+retValue);
					System.out.println("cmQuantity1@@@@@@@@"+cmQuantity1);
					System.out.println("cmValue1@@@@@@@@"+cmValue1);
					System.out.println("secondarySalesValue@@@@@@@@"+secondarySalesValue);
					System.out.println("formulaValue@@@@@@@@"+formulaValue);
					System.out.println("rcpFreeValue@@@@@@@@"+rcpFreeValue);
					
					//tranIdItem = genericUtility.getColumnValue("tran_id",dom);
					if (clStockInp == null || clStockInp.trim().length()==0)
					{
						clStockInp="0";
					}
					//String pattern= "^[0-9[.]]*$";
					//String pattern= "^[0-9[.]]*$";
					System.out.println("itemCode >>>["+itemCode1+"] isClStockInt>>>>>>["+isClStockInt+"] closing stock>>["+clStockInp+"]");
					isClStockInt= isValidDouble(clStockInp);
					System.out.println("isClStockInt>>>>>>"+isClStockInt);
					if(isClStockInt && Double.parseDouble(clStockInp)>=0)
					//if(Double.parseDouble(clStockInp)>=0)-->//Commented by saurabh as per discussion with Manoj Sir.[16/01/17|End]
					{
						//Commented by saurabh as per discussion with Manoj Sir.[16/01/17|End]
						/*sql = " SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'CUST_STOCK_MODE'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							stockMode = checkNull(rs.getString("VAR_VALUE"));
							System.out.println("stockMode :" + stockMode);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("clStockInp :" + clStockInp);
						if (clStockInp != null)
						{
							//clStock = Double.parseDouble(clStockInp);
							clStock = Math.round(Double.parseDouble(clStockInp));
						} else
						{
							clStock = 0.0;
						}
						System.out.println("stockMode :" + stockMode);
						if (stockMode.trim().equals("S"))
						{
							salesQtyCal = 0;
						} else if (stockMode.trim().equals("C") || stockMode.trim().equals("A"))
						{*/
							//System.out.println("stockMode :" + stockMode);
						//Commented by saurabh as per discussion with Manoj Sir.[16/01/17|End]
						System.out.println("clStockInp :" + clStockInp);
						if (clStockInp != null)
						{
							clStock = Math.round(Double.parseDouble(clStockInp));
						} /*else
						{
							clStock = 0.0;
						}*/
							System.out.println("opStkDom :" + opStkDom);
							System.out.println("rcpQtmDom :" + rcpQtmDom);
							System.out.println("rcpReplQtmDom :" + rcpReplQtmDom);
							System.out.println("retQtyDom :" + retQtyDom);
							System.out.println("retQtyFreeDom :" + retQtyFreeDom);
							System.out.println("clStock :" + clStock);
							
							//Start Added by chandrashekar on 08-dec-2015
							//double   qtyValue=0.0;
							/*if(cmQuantity1>clStock)
							{
						    	//formulaValue=0;
						    	formulaValue=clStock;
							}else
							{
								formulaValue=clStock-cmQuantity1;
							}*/
							formulaValue=clStock;
							System.out.println("formulaValue@@@@@@>>>"+formulaValue);
							//System.out.println("countryCode>>>>>"+countryCode);
							//System.out.println("itemSerHd>>>>>"+itemSer1);
							System.out.println("isItemSer>>>>>"+isItemSerLocal);
							/*if(!isItemSerLocal)
							{*/
							invoiceMonths =  dist.getDisparams("999999","INVOICE_MONTHS",conn);
							System.out.println("invoiceMonths.." + invoiceMonths);
							if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0) )
							{
								 invoiceMonthsPrevious=-3;
							}else
							{
								invoiceMonthsPrevious=Integer.parseInt(invoiceMonths);
							}
							//thirdMonthDay= utlmethd.AddMonths(prdtoDateTmstmp, -3);
							//Changes done by santosh on 19-FEB-2019 to set date in Class variable .START
							fromdate = genericUtility.getColumnValue("from_date",dom1);
							System.out.println("@S@fromdate (case 3) :"+fromdate);
							todate = genericUtility.getColumnValue("to_date",dom1);
							System.out.println("@S@todate (case 3):"+todate);
							frmDateTstmp = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("@S@frmDateTstmp :"+frmDateTstmp);
							toDateTstmp = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(todate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("@S@toDateTstmp :"+toDateTstmp);
							prdtoDateTmstmp=toDateTstmp;
							prdFromoDateTmstmp=frmDateTstmp;
							//Changes done by santosh on 19-FEB-2019 to set date in Class variable .END
							
							System.out.println("@S@ invoiceMonthsPrevious>>>>>"+invoiceMonthsPrevious);
							System.out.println("@S@>> prdFromoDateTmstmp["+prdFromoDateTmstmp+"]prdtoDateTmstmp["+prdtoDateTmstmp+"]");
							thirdMonthDay= AddMonths(prdFromoDateTmstmp, invoiceMonthsPrevious);
							//thirdMonthDay= utlmethd.AddMonths(prdFromoDateTmstmp, -3);
							System.out.println("thirdMonthDay>>>>>>"+thirdMonthDay);
							
							/* sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
											" from period_appl a,period_tbl b " +
											"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
											"and b.prd_tblno=? " +
											"AND case when a.type is null then 'X' else a.type end='S' and " +
											"? between b.FR_DATE and b.TO_DATE ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, countryCode+"_"+itemSer1.trim());
									pstmt.setTimestamp(2, thirdMonthDay);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										thirdMonthFrDt = rs.getTimestamp("FR_DATE"); 
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;*/
									
							/*sql = "SELECT fr_date FROM period_tbl WHERE ? between fr_date and to_date";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1,thirdMonthDay);
							rs = pstmt.executeQuery( );
							if( rs.next() )
							{
								thirdMonthFrDt = rs.getTimestamp("fr_date" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;*/
							//Modified by santosh to set priceList(14/SEP/2017).[START]
							calEnablePrice =  dist.getDisparams("999999","ENABLE_SPEC_PRICELIST",conn);
							calPriceDivision =  dist.getDisparams("999999","SPEC_PRICELIST",conn);
							System.out.println("calEnablePrice["+calEnablePrice+"]calPriceDivision["+calPriceDivision+"]");
							if (("NULLFOUND".equalsIgnoreCase(calEnablePrice) || calEnablePrice == null || calEnablePrice.trim().length() == 0) )
							{
								 calEnablePrice="N";
							}
							if (("NULLFOUND".equalsIgnoreCase(calPriceDivision) || calPriceDivision == null || calPriceDivision.trim().length() == 0) )
							{
								 calEnablePrice="N";
							}
							System.out.println("calEnablePrice["+calEnablePrice+"]calPriceDivision["+calPriceDivision+"]");
							//Modified by santosh to set priceList(14/SEP/2017).[END]
							sql = "SELECT inv.invoice_id,itrc.rate__stduom as rate__stduom,itrc.quantity__stduom as quantity__stduom,inv.tran_date " +
									"FROM invoice_trace itrc,invoice inv WHERE itrc.item_code=?  " +
									"and itrc.invoice_id=inv.invoice_id and inv.tran_date>=? " +
									"and inv.tran_date<=? AND itrc.rate__stduom >0.001  and inv.cust_code=?  " +
									" ORDER BY inv.tran_date DESC";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode1);
							//pstmt.setTimestamp(2,thirdMonthFrDt);
							pstmt.setTimestamp(2,thirdMonthDay);
							pstmt.setTimestamp(3,prdtoDateTmstmp);
//							pstmt.setString(4, custCodeStatic );Commented and added by saurabh 30/02/17
							pstmt.setString(4, custCodeDom );
							rs = pstmt.executeQuery( );
							while( rs.next() )
							{
								rateStd = rs.getDouble("rate__stduom" );
								quantityStd = rs.getDouble("quantity__stduom" );
								System.out.println("rateStd :"+rateStd);
								System.out.println("quantityStd :"+quantityStd);
								 if(formulaValue>=quantityStd)
						         {
						        	 closingValue=closingValue+ quantityStd*rateStd;
						        	 System.out.println("closing value"+closingValue);
						        	 formulaValue=formulaValue-quantityStd;
						         }
						         else
						         {
						        	 closingValue=closingValue+ formulaValue*rateStd;
						        	 formulaValue=0;
						        	 System.out.println("closing value>>>>"+closingValue);
						         }
								 closingValue=getRequiredDcml(closingValue,3);
								 if(formulaValue == 0)
								 {
									 break;
								 }
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("closingValue>>>>>>"+closingValue);
							System.out.println("formulaValue>>>>>>"+formulaValue);
							if(formulaValue>0)
							{
								System.out.println("dbSysDate>>>>>>"+dbSysDate);
								//rateAll = getRate(custCodeStatic,dbSysDate,itemCode1,conn);
								//Modified by santosh to set priceList(14/SEP/2017).[START]
								if("Y".equalsIgnoreCase(calEnablePrice))
								{
									if("BR".equalsIgnoreCase(itemSerHd))
									{
										priceList = calPriceDivision.substring(calPriceDivision.indexOf(",")+1,calPriceDivision.length());
										System.out.println("@S@priceList for division ::BR:: ["+priceList+"]");
									}
									else
									{
										priceList= calPriceDivision.substring(0,calPriceDivision.indexOf(","));
										System.out.println("@S@priceList for all divisions ["+priceList+"]");
									}
								}
								else
								{
									sql = "select price_list from customer where cust_code =? ";
									pstmt1 = conn.prepareStatement(sql);
									//pstmt1.setString(1, custCodeStatic );//Commented and added by saurabh [03/02/17]
									pstmt1.setString(1, custCodeDom );
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										priceList = checkNull(rs1.getString("price_list"));
										System.out.println("priceList edit :" + priceList);
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								}
								//Modified by santosh to set priceList(14/SEP/2017).[END]
								//Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|Start] 
								//rateAll = discmn.pickRate(priceList,sysDate,itemCode1,conn);
								sysDate = genericUtility.getValidDateString( sysDate , getApplDateFormat() , getDBDateFormat());
								sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, TO_DATE( ? , ? ), ? ) FROM DUAL ";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, priceList );
								pstmt.setString( 2, sysDate );
								pstmt.setString( 3, getDBDateFormat() );
								pstmt.setString( 4, itemCode1 );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									rateAll = rs.getDouble(1);
									System.out.println("rateAll-----------> [" +rateAll+ "]");				
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
								rateAll=getRequiredDcml(rateAll,3);
								//Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|end]
								System.out.println("rateAll>>>>>"+rateAll);
								closingValue=closingValue+formulaValue*rateAll;
								closingValue=getRequiredDcml(closingValue,3);
								
							}
							/*}else
							{
								//calRate=getOpeningRate(orderTypeHeader,itemSerHeaderSplit,selectedInvList,rcpQtmDom,itemCode1,prdtoDateTmstmp,prdFromoDateTmstmp,custCodeStatic,conn);
								calRate=getOpeningRate(orderTypeHeader,itemSerHeaderSplit,selectedInvList,rcpQtmDom,itemCode1,prdtoDateTmstmp,prdFromoDateTmstmp,custCodeDom,conn);
								calRate=getRequiredDcml(calRate,3);
								closingValue=clStock*calRate;
							}*/
							if(clStock>0)
							{
								closingRate=closingValue/clStock;
							}else
							{
								closingRate=0.0;
							}
							closingRate=getRequiredDcml(closingRate,3);
							System.out.println("closingRate@@@@@@@@@"+closingRate);
							grossSecondaryQty=opStkDom+rcpQtmDom+rcpReplQtmDom+rcpFreeQtmDom-retQtyDom-clStock;
							System.out.println("grossSecondaryQty>>>>>>"+grossSecondaryQty);
							//End Added by chandrashekar on 08-dec-2015
							
							//added by chandrashekar on 29-dec-2015
						    formulaValue=0;
						   // closingValue=0;
						    //closingRate=0;
							/*System.out.println("cmQuantity1>>>>>>>>@@@@@@"+cmQuantity1);
							if(cmQuantity1>clStock)
							{
								formulaValue=0;
								//closingRate=cmValue1/cmQuantity1;
								grossClosingRate=cmValue1/cmQuantity1;
							}else
							{
								formulaValue=clStock-cmQuantity1;
								//closingRate=(cmValue1+(formulaValue*rateOld))/clStock;
								grossClosingRate=(cmValue1+(formulaValue*rateOrgOld))/clStock;
							}*/
							
							
							System.out.println("formulaValue>>>>>"+formulaValue);
							
							/*System.out.println("closingRate>>>>"+closingRate);
							System.out.println("grossClosingRate>>>"+grossClosingRate);
							closingValue=closingRate*clStock;
							System.out.println("closingValue>>>>>>>"+closingValue);*/
							//grossClosingValue=grossClosingRate*clStock;
							//Commented by saurabh-040117
							//if(!isItemSerLocal)
							//{
							
							//netSecondarySalesValue=(opStkDom*rateOld)+rcpValue+replValue-retValue-closingValue;
							netSecondarySalesValue=opValue+rcpValue+replValue-retValue-closingValue;
							
							/*}else
							{
								netSecondarySalesValue=(opStkDom*calRate)+rcpValue+replValue-retValue-closingValue;
							}*/
							//Commented by saurabh-040117
							netSecondarySalesValue=getRequiredDcml(netSecondarySalesValue,3);
							System.out.println("netSecondarySalesValue>>>>>>>"+netSecondarySalesValue);
							/*secondaryQty=opStkDom+rcpQtmDom+rcpReplQtmDom-retQtyDom-clStock;
							System.out.println("secondaryQty>>>>"+secondaryQty);
							netSecondaryRate=netSecondarySalesValue/secondaryQty;
							System.out.println("netSecondaryRate>>>"+netSecondaryRate);*/
							//Commented by saurabh-040117
							//if(!isItemSerLocal)
							//{
							
							//grossSecondarySalesValue=(opStkDom*rateOrgOld)+rcpValue+replValue+rcpFreeValue-retValue-closingValue;
							grossSecondarySalesValue=opValue+rcpValue+replValue+rcpFreeValue-retValue-closingValue;
							
							/*}else
							{
								grossSecondarySalesValue=(opStkDom*calRate)+rcpValue+replValue+rcpFreeValue-retValue-closingValue;
							}*/
								//Commented by saurabh-040117
							grossSecondarySalesValue=getRequiredDcml(grossSecondarySalesValue,3);
							System.out.println("rcpFreeValue>>>>"+rcpFreeValue);
							System.out.println("grossSecondarySalesValue>>>>"+grossSecondarySalesValue);
							System.out.println("grossSecondaryQty>>>>"+grossSecondaryQty);
							/*grossSecondaryQty=opStkDom+rcpQtmDom+rcpReplQtmDom+rcpFreeQtmDom-retQtyDom-clStock;
							System.out.println("grossSecondaryQty>>>>"+grossSecondaryQty);*/
							if(grossSecondaryQty>0)
							{
								grossSecondaryRate = grossSecondarySalesValue / grossSecondaryQty;
							}else
							{
								grossSecondaryRate=0.0;
							}
							grossSecondaryRate=getRequiredDcml(grossSecondaryRate,3);
							System.out.println("grossSecondaryRate>>>"+grossSecondaryRate);
							if(grossSecondaryRate<0)
							{
								grossSecondaryRate=0;
							}
							//End by chandrashekar on 29-dec-2015
							salesQtyCal = opStkDom + (rcpQtmDom + rcpReplQtmDom) - (retQtyDom + retQtyFreeDom) - clStock;
						//}-->Commented by saurabh as per discussion with Manoj Sir.[16/01/17|End]

						System.out.println("salesQtyCal :" + salesQtyCal);

						valueXmlString.append("<Detail3>\r\n");
						if(!Double.isNaN(salesQtyCal))
						{
							valueXmlString.append("<sales>").append("<![CDATA[" + (long) Math.round(salesQtyCal) + "]]>").append("</sales>");
						}
						if(!Double.isNaN(closingRate))
						{
							/*valueXmlString.append("<rate>").append("<![CDATA[" + getRequiredDecimal(netSecondaryRate,2) + "]]>").append("</rate>");*/
							valueXmlString.append("<rate>").append("<![CDATA[" + getRequiredDecimal(closingRate,3) + "]]>").append("</rate>");
						}else
						{
							valueXmlString.append("<rate>").append("<![CDATA[0]]>").append("</rate>");
						}
						if(!Double.isNaN(grossSecondaryRate))
						{
							valueXmlString.append("<rate__org>").append("<![CDATA[" + getRequiredDecimal(grossSecondaryRate,3) + "]]>").append("</rate__org>");
						}else
						{
							valueXmlString.append("<rate__org>").append("<![CDATA[0]]>").append("</rate__org>");
						}
						if(!Double.isNaN(grossSecondaryQty))
						{
							valueXmlString.append("<sales__org>").append("<![CDATA[" + (long) Math.round(grossSecondaryQty) + "]]>").append("</sales__org>");
						}
						if(!Double.isNaN(clStock))
						{
							valueXmlString.append("<cl_stock>").append("<![CDATA[" + (long) Math.round(clStock) + "]]>").append("</cl_stock>");
						}
						////added by azhar[21/FEB/2017][START]
						//System.out.println("is previous item flag cl stock:::" + isPreviusItemCl);
						//if(isPreviusItemCl || "V".equalsIgnoreCase(editFlag))
						//{
							valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+(long)Math.round(opStkDom)+"]]>").append("</op_stock>");
							
						//}else
						//{
						//	valueXmlString.append("<op_stock protect='0'>").append("<![CDATA["+(long)Math.round(opStkDom)+"]]>").append("</op_stock>");
							
						//}
						//added by azhar[21/FEB/2017][END]
						if(!Double.isNaN(closingValue))
						{
							valueXmlString.append("<cl_value>").append("<![CDATA[" + getRequiredDcml(closingValue,3) + "]]>").append("</cl_value>");
						}
						if(!Double.isNaN(netSecondarySalesValue))
						{
							valueXmlString.append("<sales_value>").append("<![CDATA[" + getRequiredDcml(netSecondarySalesValue,3) + "]]>").append("</sales_value>");
						}
						//Commented by saurabh-040117
						//if(!Double.isNaN(opStkDom*rateOld) && !isItemSerLocal)
						//{
							valueXmlString.append("<op_value>").append("<![CDATA[" + getRequiredDcml(opValue,3) + "]]>").append("</op_value>");
						/*}else if(!Double.isNaN(opStkDom*calRate) && isItemSerLocal)
						{
							valueXmlString.append("<op_value>").append("<![CDATA[" + getRequiredDcml((opStkDom*calRate),3) + "]]>").append("</op_value>");
						}*/
						//Commented by saurabh-040117
						valueXmlString.append("</Detail3>\r\n");

					}else
					{
						valueXmlString.append("<Detail3>\r\n");
						valueXmlString.append("<sales>").append("<![CDATA[" + (long) Math.round(secondarySales) + "]]>").append("</sales>");
						/*if(!clStockInp.matches(pattern))
						{*/
						valueXmlString.append("<cl_stock>").append("<![CDATA[0]]>").append("</cl_stock>");//Changes done by saurabh for alphanumeric values it should be set 0
						/*}else
						{
							//valueXmlString.append("<cl_stock>").append("<![CDATA[" + clStockInp + "]]>").append("</cl_stock>");
							valueXmlString.append("<cl_stock>").append("<![CDATA[0]]>").append("</cl_stock>");//Changes done by saurabh for pattern values it should be set 0
							//valueXmlString.append("<cl_stock>").append("<![CDATA[" + Math.round(Double.parseDouble(clStockInp)) + "]]>").append("</cl_stock>");//Commented by saurabh[16/01/17]
						}*/
						setNodeValue( dom, "cl_stock", 0+"" );
						reStr=itemChanged(dom,dom1, dom2, objContext,"cl_stock",editFlag,xtraParams,"",null);
						pos = reStr.indexOf("<Detail3>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail3>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						valueXmlString.append("</Detail3>\r\n");
					}
				}

				if (currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					int lineNo1=0;
					//NodeList parentNodeList1 = null;
					NodeList childNodeList1 = null;
					Node parentNode1 = null;
					Node childNode1 = null;
					int childNodeListLength1;
					
	                System.out.println("itm_default_add  called");
					Node detail1Node = dom2.getElementsByTagName("Detail1").item(0);
					tranIdNew = checkNull(genericUtility.getColumnValueFromNode("tran_id", detail1Node));
					System.out.println("tranIdNew for add new record>>>"+tranIdNew);
					int tmpLineNo=0;
					NodeList parentList = dom2.getElementsByTagName("Detail3");
					parentNodeListLength = parentList.getLength();
					System.out.println((new StringBuilder("parentNodeListLength[")).append(parentNodeListLength).append("]").toString());
					for (int prntCtr = 0; prntCtr < parentNodeListLength; prntCtr++)
					{
						parentNode1 = parentList.item(prntCtr);

						AttributeMap attrMap = (AttributeMap) parentNode1.getAttributes();
						System.out.println("[" + prntCtr + "] Node domID [" + attrMap.getNamedItem("domID").getLocalName() + ":" + attrMap.getNamedItem("domID").getNodeValue() + "]");
						lineNo = Integer.parseInt(attrMap.getNamedItem("domID").getNodeValue());
						System.out.println("lineNo----" + lineNo);
						
						childNodeList1 = parentNode1.getChildNodes();
	                    childNodeListLength1 = childNodeList1.getLength();
	                    System.out.println("childNodeListLength1::: "+ childNodeListLength1+"\n");
	                    for (int childRow = 0; childRow < childNodeListLength1; childRow++)
	                    {
	                        childNode1 = childNodeList1.item(childRow);
	                        childNodeName = childNode1.getNodeName();
	                        System.out.println("childNodeName :"+childNodeName);
	                        if (childNodeName.equals("line_no"))
	                        {
	                            if(childNode1.getFirstChild()!=null)
	                            {
	                            	lineNo1 = Integer.parseInt(childNode1.getFirstChild().getNodeValue().trim());
		                            System.out.println("Detail3 lineNo:::]" +lineNo1);
		                            if(tmpLineNo<=lineNo1)
		                            {
		                            	tmpLineNo=lineNo1;
		                            }
	                            }
	                        }
	                        System.out.println("childRow :"+childRow);
	                        System.out.println("childNodeListLength1 :"+childNodeListLength1);
	                    }
					}
					System.out.println("lineNo:::::>>>"+lineNo1);
					tmpLineNo=tmpLineNo+1;
	                System.out.println("NewlineNo:::::>>>"+tmpLineNo);
					
					System.out.println("dom id> before>>>>"+lineNo);
					//01/12/16 Space removed for line no
					lineNoStrDom = "" + tmpLineNo;
					//lineNoStrDom = lineNoStrDom.substring(lineNoStrDom.length() - 3);
					System.out.println("lineNoStrDom for add new record" + lineNoStrDom);
					
					Node detail1Node1 = dom2.getElementsByTagName("Detail1").item(0);
					itemSer = checkNull(genericUtility.getColumnValueFromNode("item_ser", detail1Node1));
					System.out.println("itemSer add new record>>>"+itemSer);
					//itmDetCnt++;  
					//System.out.println("itmDetCnt>>After>>>"+itmDetCnt);
					valueXmlString.append("<Detail3 domID='" + lineNo + "' objContext=\"3\" selected=\"Y\">\r\n");
					valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<tran_id><![CDATA["+ tranIdNew+"]]></tran_id>");
					valueXmlString.append("<line_no>").append("<![CDATA["+lineNoStrDom+"]]>").append("</line_no>");
					valueXmlString.append("<item_code protect='0'>").append("<![CDATA[]]>").append("</item_code>");
					valueXmlString.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");
					valueXmlString.append("<descr>").append("<![CDATA[]]>").append("</descr>");
					valueXmlString.append("<unit>").append("<![CDATA[]]>").append("</unit>");
					valueXmlString.append("<loc_type>").append("<![CDATA[]]>").append("</loc_type>");
					valueXmlString.append("<rate>").append("<![CDATA["+0+"]]>").append("</rate>");
					valueXmlString.append("<rate__org>").append("<![CDATA["+0+"]]>").append("</rate__org>");
					valueXmlString.append("<op_stock protect='1'>").append("<![CDATA["+0+"]]>").append("</op_stock>");
					valueXmlString.append("<purc_rcp>").append("<![CDATA["+0+"]]>").append("</purc_rcp>");
					valueXmlString.append("<purc_rcp__repl>").append("<![CDATA["+0+"]]>").append("</purc_rcp__repl>");
					valueXmlString.append("<purc_rcp__free>").append("<![CDATA["+0+"]]>").append("</purc_rcp__free>");
					valueXmlString.append("<purc_ret>").append("<![CDATA["+0+"]]>").append("</purc_ret>");
					valueXmlString.append("<purc_ret__repl>").append("<![CDATA[0]]>").append("</purc_ret__repl>");
					//Added by saurabh[06/12/16]
					valueXmlString.append("<purc_ret__free>").append("<![CDATA["+0+"]]>").append("</purc_ret__free>");
					valueXmlString.append("<ret_free_val>").append("<![CDATA["+0+"]]>").append("</ret_free_val>");
					//Added by saurabh[06/12/16]
					valueXmlString.append("<sales>").append("<![CDATA["+0+"]]>").append("</sales>");
					valueXmlString.append("<cl_stock>").append("<![CDATA["+0+"]]>").append("</cl_stock>");
					
					valueXmlString.append("<transit_qty>").append("<![CDATA["+0+"]]>").append("</transit_qty>");
					valueXmlString.append("<transit_bill_val>").append("<![CDATA["+0+"]]>").append("</transit_bill_val>");//new coulmn added
					
					valueXmlString.append("<transit_qty__repl>").append("<![CDATA["+0+"]]>").append("</transit_qty__repl>");
					valueXmlString.append("<transit_repl_val>").append("<![CDATA["+0+"]]>").append("</transit_repl_val>");
					
					valueXmlString.append("<transit_qty__free>").append("<![CDATA["+0+"]]>").append("</transit_qty__free>");
					valueXmlString.append("<transit_free_val>").append("<![CDATA["+0+"]]>").append("</transit_free_val>");//new column added
					
					valueXmlString.append("<primary_sales>").append("<![CDATA["+0+"]]>").append("</primary_sales>");
					valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
					valueXmlString.append("<chg_date>").append("<![CDATA["+sdf.format(currentDate)+"]]>").append("</chg_date>");
					valueXmlString.append("<rate_old>").append("<![CDATA["+0+"]]>").append("</rate_old>");
					valueXmlString.append("<rate_org_old>").append("<![CDATA["+0+"]]>").append("</rate_org_old>");
					//Added by saurabh[06/12/16]
					valueXmlString.append("<rcp_value>").append("<![CDATA["+0+"]]>").append("</rcp_value>");
					valueXmlString.append("<rcp_val>").append("<![CDATA["+0+"]]>").append("</rcp_val>");
					
					valueXmlString.append("<repl_value>").append("<![CDATA["+0+"]]>").append("</repl_value>");
					valueXmlString.append("<rcp_repl_val>").append("<![CDATA["+0+"]]>").append("</rcp_repl_val>");
					
					valueXmlString.append("<rcp_free_val>").append("<![CDATA["+0+"]]>").append("</rcp_free_val>");
					valueXmlString.append("<sret_vale>").append("<![CDATA["+0+"]]>").append("</sret_vale>");
					valueXmlString.append("<ret_val>").append("<![CDATA["+0+"]]>").append("</ret_val>");
					//Added by saurabh[06/12/16]
					valueXmlString.append("<cm_invoice>").append("<![CDATA["+0+"]]>").append("</cm_invoice>");
					valueXmlString.append("<cm_value>").append("<![CDATA["+0+"]]>").append("</cm_value>");
					valueXmlString.append("<secondary_value>").append("<![CDATA["+0+"]]>").append("</secondary_value>");
					valueXmlString.append("<formula_value>").append("<![CDATA["+0+"]]>").append("</formula_value>");
					valueXmlString.append("<free_value>").append("<![CDATA["+0.0+"]]>").append("</free_value>");
					valueXmlString.append("<sales__org>").append("<![CDATA["+0+"]]>").append("</sales__org>");
					valueXmlString.append("<pur_value>").append("<![CDATA["+0+"]]>").append("</pur_value>");
					valueXmlString.append("<op_value>").append("<![CDATA["+0+"]]>").append("</op_value>");
					valueXmlString.append("<sales_value>").append("<![CDATA["+0+"]]>").append("</sales_value>");
					valueXmlString.append("<cl_value>").append("<![CDATA["+0+"]]>").append("</cl_value>");
					valueXmlString.append("</Detail3>");
				
				}
				 if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					System.out.println("item_code item change called");
					itemCode = genericUtility.getColumnValue("item_code",dom);
					//clStockInp = genericUtility.getColumnValue("cl_stock",dom);
					sql = "Select descr, unit, loc_type ,sale_rate from item "
							+" 	where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						rs = pstmt.executeQuery( );
						if( rs.next() )
						{
							descrItem = rs.getString( "descr" );
							unitItem = rs.getString( "unit" );
							locTypeItem = rs.getString( "loc_type");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					
					valueXmlString.append("<Detail3>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
					valueXmlString.append("<descr>").append("<![CDATA["+descrItem+"]]>").append("</descr>");
					valueXmlString.append("<unit>").append("<![CDATA["+unitItem+"]]>").append("</unit>");
					valueXmlString.append("<loc_type>").append("<![CDATA["+locTypeItem+"]]>").append("</loc_type>");
					//valueXmlString.append("<cl_value>").append("<![CDATA["+clStockInp+"]]>").append("</cl_value>");
					valueXmlString.append("</Detail3>");
				
				}
				 if (currentColumn.trim().equalsIgnoreCase("op_stock"))
				{	
					 opStock = genericUtility.getColumnValue("op_stock",dom);
					 itemCode = genericUtility.getColumnValue("item_code",dom);
					 clStockInp=genericUtility.getColumnValue("cl_stock",dom);
					 custCodeDom=checkNull(genericUtility.getColumnValue("cust_code",dom2));
					 System.out.println("custCodeDom opening stock>>>"+custCodeDom);
					 clStock = Double.parseDouble(genericUtility.getColumnValue("cl_stock",dom));
					 rcpQtmDom = Double.parseDouble(genericUtility.getColumnValue("purc_rcp",dom));
					 rcpReplQtmDom = Double.parseDouble(genericUtility.getColumnValue("purc_rcp__repl",dom));
					 rcpFreeQtmDom = Double.parseDouble(genericUtility.getColumnValue("purc_rcp__free",dom));
					 retQtyDom = Double.parseDouble(genericUtility.getColumnValue("purc_ret",dom));
					 retQtyFreeDom = Double.parseDouble(genericUtility.getColumnValue("purc_ret__repl",dom));
					 System.out.println("op_stock item change called>>>"+opStock);
					 
					 sql = "select price_list from customer where cust_code =? ";
						pstmt1 = conn.prepareStatement(sql);
//						pstmt1.setString(1, custCodeStatic );
						pstmt1.setString(1, custCodeDom );
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							priceList = checkNull(rs1.getString("price_list"));
							System.out.println("priceList edit :" + priceList);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						rateAll=0.0;
						//Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|Start] 
						//rateAll = discmn.pickRate(priceList,sysDate,itemCode1,conn);
						sysDate = genericUtility.getValidDateString( sysDate , getApplDateFormat() , getDBDateFormat());
						sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, TO_DATE( ? , ? ), ? ) FROM DUAL ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, priceList );
						pstmt.setString( 2, sysDate );
						pstmt.setString( 3, getDBDateFormat() );
						pstmt.setString( 4, itemCode );
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rateAll = rs.getFloat(1);
							System.out.println("rateAll-----------> [" +rateAll+ "]");				
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
						rateAll=getRequiredDcml(rateAll,3);
						//Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|end] 
						System.out.println("rateAll>>>>"+rateAll);
						if(opStock !=null && opStock.trim().length() >0)
					 	{
							opStockQty = Double.parseDouble(opStock);
					 	}
						else
						{
							opStockQty = 0.0;
						}
						valueXmlString.append("<Detail3>\r\n");
						valueXmlString.append("<op_stock>").append("<![CDATA["+(long) Math.round(opStockQty)+"]]>").append("</op_stock>");
						valueXmlString.append("<rate_old>").append("<![CDATA["+rateAll+"]]>").append("</rate_old>");
						valueXmlString.append("<rate_org_old>").append("<![CDATA["+rateAll+"]]>").append("</rate_org_old>");
						//closing item change called
						setNodeValue( dom, "cl_stock", clStockInp );
						reStr=itemChanged(dom,dom1, dom2, objContext,"cl_stock",editFlag,xtraParams,"",null);
						pos = reStr.indexOf("<Detail3>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail3>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						valueXmlString.append("</Detail3>");
				}
				System.out.println("case 3 valueXmlString :"+valueXmlString);
                break;  
				
			}//Switch		
			valueXmlString.append("</Root>");	
			System.out.println("Final valueXmlString :"+valueXmlString);
		}//try
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}	
		finally
		{
			try
			{
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception s)
			{
			}
		}
		return valueXmlString.toString();
	}//itemChanged()
	/*private double getRate(String custCode, Timestamp trandate, String itemCode,Connection conn)throws RemoteException,ITMException
	{
		//ibase.webitm.utility.GenericUtility genericUtility = new ibase.webitm.utility.GenericUtility();
		//System.out.println("[CustStockEJB]Calcuating rate for the custCode :"+custCode+": tranDate :"+tranDate+": itemCode :"+itemCode+":");
			
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String priceList = "",selQuery="";
		java.sql.Timestamp invTranDate = null,retTranDate=null;
		double rate = 0.0;
		try
		{			
			selQuery = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE = ? ";
			pstmt = conn.prepareStatement(selQuery);
			pstmt.setString( 1, custCode );
			rs = pstmt.executeQuery( );
			if( rs.next() )
			{	
				priceList = rs.getString("PRICE_LIST");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			System.out.println("trandate :"+trandate);
			
			selQuery = "select max(rate) as rate from pricelist " +
					" where price_list = ? and " +
					" item_code  = ? and " +
				  	" eff_from <= ? " +
					" and valid_upto >= ? ";
			pstmt = conn.prepareStatement(selQuery);
			pstmt.setString( 1, priceList );
			pstmt.setString( 2, itemCode );
			pstmt.setTimestamp(3,trandate);
			pstmt.setTimestamp(4,trandate);
			rs = pstmt.executeQuery( );
			if( rs.next() )
			{	
				rate = rs.getDouble("rate");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			System.out.println("rate : "+rate);
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :getRate :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception d)
			{
			  d.printStackTrace();
			  throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		//System.out.println("[CustStockEJB]rate :"+retRate);
		return rate;
		////System.out.println("returning value from getRate as 1");
		//return 1;
	}*/
	/*private String GetInvList( String tranId, java.sql.Timestamp fromDate, java.sql.Timestamp toDate, Connection conn ) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceDate = null;
		String invoiceId = null;
		String dlvFlg = null ;
		String invoiceListString = "";
		java.sql.Timestamp tsInvoiceDate = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try
		{
			sql = " select inv.invoice_id "
				+"	from cust_stock_inv inv "
				+" where inv.tran_id = ? " //'" + tranId + "'"
				+"	and inv.DLV_FLG = 'Y' "
				+"	and inv.INVOICE_DATE between ? and ? ";
				
			pstmt = conn.prepareStatement( sql );
			
			pstmt.setString( 1, tranId );
			pstmt.setTimestamp( 2, fromDate );
			pstmt.setTimestamp( 3, toDate );
			
			rs = pstmt.executeQuery();
			
			while( rs.next() )
			{
				invoiceId = rs.getString( "invoice_id" );
				
				if ( invoiceListString.trim().length() == 0 )
				{
					invoiceListString = "'" + ( invoiceId == null ? "" : invoiceId ) + "'" ;
				}
				else
				{
					invoiceListString += ",'" + ( invoiceId == null ? "" : invoiceId ) + "'" ;
				}
			}
			
			rs.close();
			rs = null;
			
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return invoiceListString ;
	}*/
	/*private double	GetTransitQty(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itenmCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null;
		ResultSet rs = null,rs2 = null;
		String sql = null;
		String stockDlvFlg="";
		try
		{		
			System.out.println("calculate transit quantity!!!!!!!!!!!");
			
			sql = " select idet.QUANTITY__STDUOM,idet.invoice_id,idet.item_code from " +
					"invdet idet where idet.item_code= ? and  idet.invoice_id in " +
					" ( SELECT distinct invoice.invoice_id invoice_id " +
					" FROM invoice invoice ,invoice_trace itrace WHERE invoice.invoice_id=itrace.invoice_id and " +
					" ( invoice.cust_code = ? ) and invoice.site_code =? and invoice.inv_type=? " +
					" and itrace.item_ser__prom=? and  ( invoice.confirmed = 'Y' )  " +
					"and (  (invoice.tran_date >=?)  and (invoice.tran_date <= ? ) " +
					"or ((invoice.tran_date >=? )  and (invoice.tran_date <= ? ) ) and(exists (SELECT cust_stock_inv.invoice_id " +
					" FROM cust_stock_inv cust_stock_inv  WHERE cust_stock_inv.invoice_id = invoice.invoice_id and " +
					" cust_stock_inv.dlv_flg = 'N' )) ) " +
					"and  invoice.invoice_id not in ("+selectedInvList+")  " +
					"UNION " +
					" SELECT distinct cinv.invoice_id  FROM CUST_STOCK_INV cinv , " +
					" invoice invoice  WHERE  cinv.invoice_id=invoice.invoice_id and  invoice.cust_code = ? " +
					" and  invoice.item_ser= ? and invoice.inv_type = ?  and  invoice.confirmed = 'Y' and " +
					"CASE WHEN cinv.dlv_flg IS NULL THEN 'N' ELSE cinv.dlv_flg END='N' " +
					"and  ( invoice.confirmed = 'Y' )  and   (invoice.TRAN_DATE >= ? ) " +
					" and (invoice.TRAN_DATE <= ? ) "+
					" and cinv.invoice_id not in ("+selectedInvList+")  ) ";
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, itenmCode);
			pstmt.setString(2, custCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, orderType);
			pstmt.setString(5, itemSer);
			pstmt.setTimestamp(6, fromDate );
			pstmt.setTimestamp(7, toDate );
			pstmt.setTimestamp(8, prdFrom );
			pstmt.setTimestamp(9, prdTo );
			pstmt.setString(10, custCode );
			pstmt.setString(11, itemSer);
			pstmt.setString(12, orderType);
			pstmt.setTimestamp(13, fromDate );
			pstmt.setTimestamp(14, toDate );

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				dlvFlgTransit=0;
				sql= "select count(*) as count from cust_stock_inv where invoice_id=? and  dlv_flg='Y' ";
				//sql= "select count(*) as count from cust_stock_inv where invoice_id=?  ";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1, invoiceId);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					dlvFlgTransit=rs2.getInt("count");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				if(dlvFlgTransit == 0)
				{
				   System.out.println("transit invoice :"+invoiceId);
				   quantity = rs.getDouble("QUANTITY__STDUOM");
				   System.out.println("inv qty :"+quantity);
				   transitQty = transitQty + quantity;
				}   
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("transitQty :"+transitQty);
		return transitQty ;
	}
	*/
/*	private double GetTransitQty(String tranIdLast,String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try
		{		
		
			sql = " SELECT SUM( iv.QUANTITY__STDUOM ) QUANTITY " 
				+"	FROM INVDET iv "
				+" WHERE  iv.INVOICE_ID in ( select dtl.invoice_id "
				+"								from cust_stock_inv dtl " 
				+"							where dtl.tran_id = ? " //'" + tranId.trim() +"'"
				+"								and dtl.dlv_flg = 'N' )"
				+"	AND iv.ITEM_CODE = ? ";//'" + itemCode +"'";
			
			pstmt = conn.prepareStatement( sql );

			pstmt.setString( 1, tranIdLast );
			pstmt.setString( 2, itemCode );

			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				quantity = rs.getDouble("QUANTITY");
			}
			rs.close();
			rs = null;
			
			transitQty = quantity;
			
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return transitQty ;
	}*/

/*
private double GetTransitQty(String tranId, String itemCode, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try
		{		
		
			sql = " SELECT SUM( iv.QUANTITY__STDUOM ) QUANTITY " 
				+"	FROM INVDET iv "
				+" WHERE  iv.INVOICE_ID in ( select dtl.invoice_id "
				+"								from cust_stock_inv dtl " 
				+"							where dtl.tran_id = ? " //'" + tranId.trim() +"'"
				+"								and dtl.dlv_flg = 'N' )"
				+"	AND iv.ITEM_CODE = ? ";//'" + itemCode +"'";
			
			pstmt = conn.prepareStatement( sql );

			pstmt.setString( 1, tranId );
			pstmt.setString( 2, itemCode );

			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				quantity = rs.getDouble("QUANTITY");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			transitQty = quantity;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return transitQty ;
	}
	
*/
	
	/*private double	GetTransitBon(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null,rs2 = null,rs3 = null;
		String sql = null;
		String stockDlvFlg="";
		String invoiceIdArray[];
		String invoiceSep="",nature="";
		double bonusQty=0.0;
		try
		{		
			// transit quantity for those which are included in period also for dlv flag 'N'
			
			sql = " select idet.QUANTITY__STDUOM,idet.invoice_id,idet.item_code from " +
					"invdet idet where idet.item_code= ? and  idet.invoice_id in " +
					" ( SELECT distinct invoice.invoice_id invoice_id FROM invoice invoice WHERE " +
					" ( invoice.cust_code = ? ) and invoice.site_code =? and invoice.inv_type=? " +
					" and invoice.item_ser=? and  ( invoice.confirmed = 'Y' )  " +
					"and (  (invoice.tran_date >=?)  and (invoice.tran_date <= ? ) " +
					"or ((invoice.tran_date >=? )  and (invoice.tran_date <= ? ) ) and(exists (SELECT cust_stock_inv.invoice_id " +
					" FROM cust_stock_inv cust_stock_inv  WHERE cust_stock_inv.invoice_id = invoice.invoice_id and " +
					" cust_stock_inv.dlv_flg = 'N' )) ) " +
					"and  invoice.invoice_id not in ("+selectedInvList+")  " +
					"UNION " +
					" SELECT distinct cinv.invoice_id  FROM CUST_STOCK_INV cinv , " +
					" invoice invoice  WHERE  cinv.invoice_id=invoice.invoice_id and  invoice.cust_code = ? " +
					" and  invoice.item_ser= ?  and  invoice.confirmed = 'Y' and " +
					"CASE WHEN cinv.dlv_flg IS NULL THEN 'N' ELSE cinv.dlv_flg END='N' and " +
					" cinv.invoice_id not in ("+selectedInvList+")  ) ";
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, itemCode);
			pstmt.setString(2, custCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, orderType);
			pstmt.setString(5, itemSer);
			pstmt.setTimestamp(6, fromDate );
			pstmt.setTimestamp(7, toDate );
			pstmt.setTimestamp(8, prdFrom );
			pstmt.setTimestamp(9, prdTo );
			pstmt.setString(10, custCode );
			pstmt.setString(11, itemSer);

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				
				stockDlvFlg="N";
				sql= "select count(*) as count from cust_stock_inv where invoice_id=? and  dlv_flg='Y' ";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1, invoiceId);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					//stockDlvFlg = checkNull(rs2.getString("dlv_flg")); 
					dlvFlgTransit=rs2.getInt("count");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				System.out.println("stockDlvFlg >>>>"+stockDlvFlg);
				
				//if(!(stockDlvFlg.equalsIgnoreCase("Y")))
				if(dlvFlgTransit == 0)
				{
				 
					if(selectedInvList.trim().length() > 0)
					{
						invoiceIdArray = selectedInvList.split(",");


						for (int i = 0; i < invoiceIdArray.length ;i++)
						{
							System.out.println(invoiceIdArray[i]);
							
							invoiceSep  = invoiceIdArray[i];
							
							invoiceId = invoiceSep.replace("'", "");
						    System.out.println(invoiceId);
						    
						    
						    sql = "select nature,quantity__stduom from sorddet where sale_order " +
									" =(select sale_order from invoice where invoice_id=?) and item_code=?";
										
							pstmt3 = conn.prepareStatement( sql );
							pstmt3.setString(1,invoiceId );
							pstmt3.setString(2,itemCode );
							rs3 = pstmt3.executeQuery();
							while( rs3.next() )
							{
								quantity = rs3.getDouble("quantity__stduom");
								nature=rs3.getString("nature");
								System.out.println("nature :"+nature);
								if(nature.equalsIgnoreCase("B"))
								{
									bonusQty = bonusQty + quantity;
									System.out.println(bonusQty);
								}
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
						}
					}	
				}   
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("bonusQty for transit :"+bonusQty);
		return bonusQty ;
	}*/
	
	/*private double	GetTransitFrees(String tranIdLast,String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null,rs2 = null,rs3 = null;
		String sql = null;
		String stockDlvFlg="";
		String invoiceIdArray[];
		String invoiceSep="",nature="";
		double freesQty=0.0;
		try
		{		
			System.out.println("----calculate Transit Frees Quantity------------------");
			
			sql = "select dtl.invoice_id from cust_stock_inv dtl " 
				+"							where dtl.tran_id = ? " //'" + tranId.trim() +"'"
				+"								and dtl.dlv_flg = 'N'  ";
			
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, tranIdLast);

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				
				System.out.println("invoiceId :"+invoiceId);
				stockDlvFlg="N";
				sql= "select count(*) as count from cust_stock_inv where invoice_id=? and  dlv_flg='Y' ";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1, invoiceId);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					dlvFlgTransit=rs2.getInt("count");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				System.out.println("stockDlvFlg >>>>"+stockDlvFlg);
				
				if(dlvFlgTransit == 0)
				{
				  
						    
						    
						    sql = "select nature,quantity__stduom from sorddet where sale_order " +
									" =(select sale_order from invoice where invoice_id=?) and item_code=?";
										
							pstmt3 = conn.prepareStatement( sql );
							pstmt3.setString(1,invoiceId );
							pstmt3.setString(2,itemCode );
							rs3 = pstmt3.executeQuery();
							while( rs3.next() )
							{
								quantity = rs3.getDouble("quantity__stduom");
								nature=rs3.getString("nature");
								System.out.println("nature :"+nature);
								if(nature.equalsIgnoreCase("F"))
								{
									freesQty = freesQty + quantity;
									System.out.println(freesQty);
								}
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
						    
						    
						//}

					//}	
					
					
					
					
					
				}   
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("transitFreesQty for transit :"+freesQty);
		return freesQty ;
	}*/
	/*private double	GetTransitFrees(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null,rs2 = null,rs3 = null;
		String sql = null;
		String stockDlvFlg="";
		String invoiceIdArray[];
		String invoiceSep="",nature="";
		double freesQty=0.0;
		try
		{		
		
		

			// transit quantity for those which are included in period also for dlv flag 'N'
			System.out.println("----calculate Transit Frees Quantity------------------");
			
			sql = " select distinct idet.invoice_id from " +
					"invdet idet where idet.item_code= ? and  idet.invoice_id in " +
					" ( SELECT distinct invoice.invoice_id invoice_id " +
					" FROM invoice invoice ,invoice_trace itrace WHERE invoice.invoice_id=itrace.invoice_id  and " +
					" ( invoice.cust_code = ? ) and invoice.site_code =? and invoice.inv_type=? " +
					" and itrace.item_ser__prom=? and  ( invoice.confirmed = 'Y' )  " +
					"and (  (invoice.tran_date >=?)  and (invoice.tran_date <= ? ) " +
					"or ((invoice.tran_date >=? )  and (invoice.tran_date <= ? ) ) and(exists (SELECT cust_stock_inv.invoice_id " +
					" FROM cust_stock_inv cust_stock_inv  WHERE cust_stock_inv.invoice_id = invoice.invoice_id and " +
					" cust_stock_inv.dlv_flg = 'N' )) ) " +
					"and  invoice.invoice_id not in ("+selectedInvList+")  " +
					"UNION " +
					" SELECT distinct cinv.invoice_id  FROM CUST_STOCK_INV cinv , " +
					" invoice invoice  WHERE  cinv.invoice_id=invoice.invoice_id and  invoice.cust_code = ? " +
					" and  invoice.item_ser= ? and invoice.inv_type = ? and  invoice.confirmed = 'Y' and " +
					"CASE WHEN cinv.dlv_flg IS NULL THEN 'N' ELSE cinv.dlv_flg END='N' and " +
					" cinv.invoice_id not in ("+selectedInvList+")  ) ";
			
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, itemCode);
			pstmt.setString(2, custCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, orderType);
			pstmt.setString(5, itemSer);
			pstmt.setTimestamp(6, fromDate );
			pstmt.setTimestamp(7, toDate );
			pstmt.setTimestamp(8, prdFrom );
			pstmt.setTimestamp(9, prdTo );
			pstmt.setString(10, custCode );
			pstmt.setString(11, itemSer);
			pstmt.setString(12, orderType);

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				
				System.out.println("invoiceId :"+invoiceId);
				stockDlvFlg="N";
				sql= "select count(*) as count from cust_stock_inv where invoice_id=? and  dlv_flg='Y' ";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1, invoiceId);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					dlvFlgTransit=rs2.getInt("count");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				System.out.println("stockDlvFlg >>>>"+stockDlvFlg);
				
				if(dlvFlgTransit == 0)
				{
				   System.out.println("transit invoice :"+invoiceId);
				   sql = "select nature,quantity__stduom from sorddet where sale_order " +
							" =(select sale_order from invoice where invoice_id=?) and item_code=?";
								
					pstmt3 = conn.prepareStatement( sql );
					pstmt3.setString(1,invoiceId );
					pstmt3.setString(2,itemCode );
					rs3 = pstmt3.executeQuery();
					while( rs3.next() )
					{
						quantity = rs3.getDouble("quantity__stduom");
						nature=rs3.getString("nature");
						System.out.println("nature :"+nature);
						if(nature.equalsIgnoreCase("F"))
						{
							freesQty = freesQty + quantity;
							System.out.println(freesQty);
						}
					}
					rs3.close();
					rs3 = null;
					pstmt3.close();
					pstmt3 = null;
					if(selectedInvList.trim().length() > 0)
					{
						invoiceIdArray = selectedInvList.split(",");


						for (int i = 0; i < invoiceIdArray.length ;i++)
						{
							System.out.println(invoiceIdArray[i]);
							
							invoiceSep  = invoiceIdArray[i];
							
							invoiceId = invoiceSep.replace("'", "");
						    System.out.println(invoiceId);
						    
						    
						    sql = "select nature,quantity__stduom from sorddet where sale_order " +
									" =(select sale_order from invoice where invoice_id=?) and item_code=?";
										
							pstmt3 = conn.prepareStatement( sql );
							pstmt3.setString(1,invoiceId );
							pstmt3.setString(2,itemCode );
							rs3 = pstmt3.executeQuery();
							while( rs3.next() )
							{
								quantity = rs3.getDouble("quantity__stduom");
								nature=rs3.getString("nature");
								System.out.println("nature :"+nature);
								if(nature.equalsIgnoreCase("F"))
								{
									freesQty = freesQty + quantity;
									System.out.println(freesQty);
								}
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
						    
						    
						}

					}	
					
				}   
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("transitFreesQty for transit :"+freesQty);
		return freesQty ;
	}
	
	*/
	
	
	/*-----------------------------------------------------------------------------------------------*/
	
	/*
	
	private double GetTransitFreeQty(String tranId, String itemCode, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double quantity = 0.0,transitFreeQty=0.0;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		String sql = null;
		String invoice_id="",selQuery="";
		try
		{		
		
			sql = " SELECT invoice_id " 
				+"	FROM INVDET iv "
				+" WHERE  iv.INVOICE_ID in ( select dtl.invoice_id "
				+"								from cust_stock_inv dtl " 
				+"							where dtl.tran_id = ? " //'" + tranId.trim() +"'"
				+"								and dtl.dlv_flg = 'N' )"
				+"	AND iv.ITEM_CODE = ? ";//'" + itemCode +"'";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, tranId );
			pstmt.setString( 2, itemCode );

			rs = pstmt.executeQuery();
			while ( rs.next() )
			{
				//quantity = rs.getDouble("QUANTITY");
				invoice_id = rs.getString("invoice_id");
				
				
				selQuery = "select invtr.quantity__stduom from invoice_trace invtr ," +
						" sorditem soi where invtr.sord_no=soi.SALE_ORDER " +
						"  and invtr.sord_line_no = soi.LINE_NO and  invtr.exp_lev = soi.EXP_LEV " +
						" and soi.NATURE='F' and invtr.invoice_id = ? ";
						
				pstmt1.setString(1,invoiceId);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					quantity=rs.getDouble("quantity__stduom");
					System.out.println("quantity :"+quantity);
					transitFreeQty = transitFreeQty + quantity;
					
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				System.out.println("transitFreeQty :"+transitFreeQty);
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return transitFreeQty ;
	}
	*/
	
	//private double GetRcpQty(String custCode,String tranIdLast,String orderType,String selectedInvList, java.sql.Timestamp fromDate, java.sql.Timestamp toDate,String itemCode,String itemSer, Connection conn) throws Exception
	
/*
private double GetRcpQty(String custCode,String itemCode,String itemSer,String orderType,String siteCode, java.sql.Timestamp fromDate, java.sql.Timestamp toDate,Connection conn) throws Exception
	{
		String invoiceId="",selQuery="";
		double quantity = 0.0;
		double rcpQuantity = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try
		{
			
						sql = "select sum(b.quantity__stduom) qty_stduom from invoice a, invdet b "
							  +"	where a.invoice_id = b.invoice_id and a.confirmed='Y' "
							  +"		and a.cust_code = ? "
							  +"	    and a.tran_date >= ? "
							  +"	    and a.tran_date <= ? "
							  +"		and b.item_code = ? "
							  +"		and a.item_ser = ? "
							  +"		and a.inv_type = ? "
							  +"		and a.site_code = ? ";
							  
						pstmt = conn.prepareStatement( sql );
						pstmt.setString(1,custCode);
						pstmt.setTimestamp(2, fromDate );
						pstmt.setTimestamp(3, toDate );
						pstmt.setString(4,itemCode);
						pstmt.setString(5,itemSer);
						pstmt.setString(6,orderType);
						pstmt.setString(7,siteCode);
						rs = pstmt.executeQuery( );
						if( rs.next() )
						{	
							rcpQuantity = rs.getDouble( "qty_stduom" );
							System.out.println("rcpQuantity :"+rcpQuantity);
						}
						else
						{
							rcpQuantity = 0;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						
						
						
					
						
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return rcpQuantity ;
	}
*/

/*---------------------------------------------------------------------------------------------*/
	/*private ArrayList<Double>	GetRcpQty(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itenmCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double rcpQuantity = 0, quantity = 0,dlvFlgTransit=0,receiptQty=0,ratesStduom=0;
		PreparedStatement pstmt = null,pstmt2 = null;
		ResultSet rs = null,rs2 = null;
		String sql = null;
		String stockDlvFlg="";
		ArrayList<Double> rcpQtyListMethod=new ArrayList<Double>();
		try
		{		
		
				
			
			System.out.println("calculate receipt quantity!!!!!!!!!!");
			sql =  " SELECT SUM(itrace.QUANTITY__STDUOM) QUANTITY__STDUOM, itrace.RATE__STDUOM FROM" +
					" invoice invoice,invoice_trace itrace " +
					" 	where invoice.invoice_id=itrace.invoice_id  AND  invoice.cust_code = ? " +
					" 	and invoice.site_code = ? and invoice.inv_type=? and itrace.item_ser__prom=?  "
					+ " and invoice.tran_date between ? and ? "
					+ " and itrace.item_code = ? group by itrace.RATE__STDUOM ";
			sql = "SELECT SUM(QUANTITY__STDUOM) PURC_RCP,(RATE__STDUOM) AS RATE__STDUOM FROM  INVDET "
					+ " where invoice_id in "
					+ " (select invoice.invoice_id from invoice invoice,invoice_trace itrace " +
					" 	where invoice.invoice_id=itrace.invoice_id  AND  invoice.cust_code = ? " +
					" 	and invoice.site_code = ? and invoice.inv_type=? and itrace.item_ser__prom=?  "
					+ " and invoice.tran_date between ? and ? ) "
					+ " and item_code = ? ";
					
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, custCode );
				pstmt.setString( 2, siteCode );
				pstmt.setString( 3, orderType );
				pstmt.setString( 4, itemSer);
				pstmt.setTimestamp( 5, fromDate );
				pstmt.setTimestamp( 6, toDate );
				//pstmt.setString( 4, tranId );
				pstmt.setString( 7, itenmCode );
				// endded by chandni
				rs = pstmt.executeQuery();
				double rcpCur =0, rcpPre = 0, totRcp = 0;
				if (rs.next())
				{			    
					receiptQty = rs.getDouble("QUANTITY__STDUOM");
					ratesStduom = rs.getDouble("RATE__STDUOM");
				}	
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;		
			
				System.out.println("ratesStduom@@@@@@@@>>>>>>> :"+ratesStduom);
				System.out.println("rate@@@@@@@@>>>>>>> :"+receiptQty);
				
				rcpQtyListMethod.add(receiptQty);
				rcpQtyListMethod.add(ratesStduom);
				System.out.println("rcpQtyList>>>>>"+rcpQtyListMethod);
			
			
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw new ITMException( exception );
		}
		System.out.println("receiptQty :"+receiptQty);
		//return receiptQty ;
		return rcpQtyListMethod ;
	}*/
/*
private double	GetRcpQty(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itenmCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
{
	NodeList parentNodeList=null;
	NodeList childList = null;
	Node parentNode=null;
	Node childNode = null;
	String invoiceId = null;
	String dlvFlg = null ;
	double rcpQuantity = 0, quantity = 0,dlvFlgTransit=0;
	PreparedStatement pstmt = null,pstmt2 = null;
	ResultSet rs = null,rs2 = null;
	String sql = null;
	String stockDlvFlg="";
	try
	{		
	
			
		
		System.out.println("calculate receipt quantity!!!!!!!!!!");
		

		
		
		sql = " select idet.invoice_id ,idet.QUANTITY__STDUOM from " +
				"invdet idet where idet.item_code= ? and  idet.invoice_id in " +
				" ( SELECT distinct invoice.invoice_id invoice_id FROM invoice invoice WHERE " +
				" ( invoice.cust_code = ? ) and invoice.site_code =? and invoice.inv_type=? " +
				" and invoice.item_ser=? and  ( invoice.confirmed = 'Y' )  " +
				"and (  (invoice.tran_date >=?)  and (invoice.tran_date <= ? ) " +
				"or ((invoice.tran_date >=? )  and (invoice.tran_date <= ? ) ) and(exists (SELECT cust_stock_inv.invoice_id " +
				" FROM cust_stock_inv cust_stock_inv  WHERE cust_stock_inv.invoice_id = invoice.invoice_id and " +
				" cust_stock_inv.dlv_flg = 'N' )) ) )" ;
			
		
		pstmt = conn.prepareStatement( sql );
		pstmt.setString(1, itenmCode);
		pstmt.setString(2, custCode);
		pstmt.setString(3, siteCode);
		pstmt.setString(4, orderType);
		pstmt.setString(5, itemSer);
		pstmt.setTimestamp(6, fromDate );
		pstmt.setTimestamp(7, toDate );
		pstmt.setTimestamp(8, prdFrom );
		pstmt.setTimestamp(9, prdTo );
		pstmt.setString(10, custCode );
		pstmt.setString(11, itemSer);
		pstmt.setString(12, orderType);

		rs = pstmt.executeQuery();
		while( rs.next() )
		{
			invoiceId = rs.getString("invoice_id");
			System.out.println("invoiceId :"+invoiceId);
		    quantity = rs.getDouble("QUANTITY__STDUOM");
			System.out.println("inv qty :"+quantity);
			rcpQuantity = rcpQuantity + quantity;
			 
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		
		
	}
	catch(Exception exception)
	{
		exception.printStackTrace();
		//System.out.println("Exception ::" + exception.getMessage());
		throw new ITMException( exception );
	}
	System.out.println("rcpQuantity :"+rcpQuantity);
	return rcpQuantity ;
}*/





/*------------------------------------------------------------------------------------------------*/
	
	
	/*private double GetRcpQty(String selectedInvList,String itemCode, Connection conn) throws Exception
	{
		String invoiceId="";
		double quantity = 0.0;
		double rcpQuantity = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try
		{
			System.out.println("selectedInvList :"+selectedInvList);
			
			
			
			
			sql = "select quantity__stduom,invoice_id from invdet where" +
					" item_code=? and invoice_id in ("+selectedInvList+") ";
						
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1,itemCode );
			
			
			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				quantity = rs.getDouble("quantity__stduom");
				rcpQuantity = rcpQuantity + quantity;
				invoiceId = rs.getString("invoice_id");
				System.out.println("invoiceId :"+invoiceId);
				System.out.println("quantity :"+quantity);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return rcpQuantity ;
	}*/
	
	
	/*------------------------------------------------------------------------------*/
	
	/*private double GetRcpBonQty(String selectedInvList,String itemCode, Connection conn) throws Exception
	{
		
		double quantity = 0.0;
		double bonusQty = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String nature="";
		String[] invoiceIdArray;
		String invoiceId="",invoiceSep="";
		
		try
		{
			
			System.out.println("------calculate bonus quantity--------------");
			
			if(selectedInvList.trim().length() > 0)
			{
				invoiceIdArray = selectedInvList.split(",");


				for (int i = 0; i < invoiceIdArray.length ;i++)
				{
					System.out.println(invoiceIdArray[i]);
					
					invoiceSep  = invoiceIdArray[i];
					
					invoiceId = invoiceSep.replace("'", "");
				    System.out.println(invoiceId);
				    
				    
				    sql = "select nature,quantity__stduom from sorddet where sale_order " +
							" =(select sale_order from invoice where invoice_id=?) and item_code=?";
								
					pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,invoiceId );
					pstmt.setString(2,itemCode );
					rs = pstmt.executeQuery();
					while( rs.next() )
					{
						quantity = rs.getDouble("quantity__stduom");
						nature=rs.getString("nature");
						System.out.println("nature :"+nature);
						if(nature.equalsIgnoreCase("B"))
						{
							bonusQty = bonusQty + quantity;
							System.out.println(bonusQty);
						}
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				    
				    
				}

			}
			
			
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("bonusQty :"+bonusQty);
		return bonusQty ;
	}*/
	
	
	
	

	/*private double GetFreeQty(String selectedInvList,String itemCode, Connection conn) throws Exception
	{
		
		double quantity = 0.0;
		double freesQty = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String nature="";
		String[] invoiceIdArray;
		String invoiceId="",invoiceSep="";
		
		try
		{
			
			
			
			if(selectedInvList.trim().length() > 0)
			{
				invoiceIdArray = selectedInvList.split(",");


				for (int i = 0; i < invoiceIdArray.length ;i++)
				{
					System.out.println(invoiceIdArray[i]);
					
					invoiceSep  = invoiceIdArray[i];
					
					invoiceId = invoiceSep.replace("'", "");
				    System.out.println(invoiceId);
				    
				    
				    sql = "select nature,quantity__stduom from sorddet where sale_order " +
							" =(select sale_order from invoice where invoice_id=?) and item_code=?";
								
					pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,invoiceId );
					pstmt.setString(2,itemCode );
					rs = pstmt.executeQuery();
					while( rs.next() )
					{
						quantity = rs.getDouble("quantity__stduom");
						nature=rs.getString("nature");
						System.out.println("nature :"+nature);
						if(nature.equalsIgnoreCase("F"))
						{
							freesQty = freesQty + quantity;
							System.out.println(freesQty);
						}
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				    
				    
				}

			}
			
			
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("freesQty :"+freesQty);
		return freesQty ;
	}*/
	

/*
private double GetFreeQty(String custCode,String itemCode,String itemSer,String orderType, java.sql.Timestamp fromDate, java.sql.Timestamp toDate,Connection conn) throws Exception
	{
		String invoiceId="",nature="";
		double quantity = 0.0;
		double freesQty = 0.0;
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		String sql = null;
		
		try
		{
			
			
			
			sql = "select a.invoice_id from invoice a, invdet b "
							  +"	where a.invoice_id = b.invoice_id "
							  +"		and a.cust_code = ? "
							  +"	    and a.tran_date >= ? "
							  +"	    and a.tran_date <= ? "
							  +"		and b.item_code = ? "
							  +"		and a.item_ser = ? "
							  +"		and a.inv_type = ? ";
							  
							  
						pstmt = conn.prepareStatement( sql );
						pstmt.setString(1,custCode);
						pstmt.setTimestamp(2, fromDate );
						pstmt.setTimestamp(3, toDate );
						pstmt.setString(4,itemCode);
						pstmt.setString(5,itemSer);
						pstmt.setString(6,orderType);
						rs = pstmt.executeQuery( );
						while( rs.next() )
						{	
							invoiceId = rs.getString( "invoice_id" );
							
							nature="";
							sql = "select nature,quantity__stduom from sorddet where sale_order " +
							" =(select sale_order from invoice where invoice_id=?) and item_code=?";
								
							pstmt1 = conn.prepareStatement( sql );
							pstmt1.setString(1,invoiceId );
							pstmt1.setString(2,itemCode );
							rs1 = pstmt1.executeQuery();
							while( rs1.next() )
							{
								quantity = rs1.getDouble("quantity__stduom");
								nature=rs1.getString("nature");
								System.out.println("nature :"+nature);
								if(nature.equalsIgnoreCase("F"))
								{
									freesQty = freesQty + quantity;
									System.out.println(freesQty);
								}
							}
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
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return freesQty ;
	}

*/
	
/*	private double	GetFreeQty(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null,rs2 = null,rs3 = null;
		String sql = null;
		String stockDlvFlg="";
		String invoiceIdArray[];
		String invoiceSep="",nature="";
		double freesQty=0.0;
		try
		{		
			// transit quantity for those which are included in period also for dlv flag 'N'
			System.out.println("----calculate Receipt Frees Quantity------------------");
			
			sql = " select invoice_id from invoice where cust_code = ? and site_code = ? and inv_type=? and item_ser=? "
					+ " and tran_date between ? and ?  "
					+ " and item_code = ? ";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, custCode);
			pstmt.setString(2, siteCode);
			pstmt.setString(3, orderType);
			pstmt.setString(4, itemSer);
			pstmt.setTimestamp(5, fromDate );
			pstmt.setTimestamp(6, toDate );
			pstmt.setString(7, itemCode);
			

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				
				System.out.println("invoiceId :"+invoiceId);
						    
						    sql = "select nature,quantity__stduom from sorddet where sale_order " +
									" =(select sale_order from invoice where invoice_id=?) and item_code=?";
										
							pstmt3 = conn.prepareStatement( sql );
							pstmt3.setString(1,invoiceId );
							pstmt3.setString(2,itemCode );
							rs3 = pstmt3.executeQuery();
							while( rs3.next() )
							{
								quantity = rs3.getDouble("quantity__stduom");
								nature=rs3.getString("nature");
								System.out.println("nature :"+nature);
								if(nature.equalsIgnoreCase("F"))
								{
									freesQty = freesQty + quantity;
									System.out.println(freesQty);
								}
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println(" Receipt Frees :"+freesQty);
		return freesQty ;
	}*/
	/*private double	GetFreeQty(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null,rs2 = null,rs3 = null;
		String sql = null;
		String stockDlvFlg="";
		String invoiceIdArray[];
		String invoiceSep="",nature="";
		double freesQty=0.0;
		try
		{		
		
			System.out.println("----calculate Receipt Frees Quantity------------------");
			
			
			sql="select sum(CASE WHEN sorditem.quantity IS NULL THEN 0 ELSE sorditem.quantity END) as frqty " +
					"from sorditem sorditem, despatchdet despdet,invoice invoice,"+
					"invoice_trace itrace "+
					"where invoice.invoice_id= itrace.invoice_id and "+
					"itrace.desp_id=despdet.desp_id and itrace.desp_line_no=despdet.line_no "+
					"and despdet.sord_no= sorditem.sale_order and despdet.line_no__sord=sorditem.line_no "+
					"and itrace.item_code=despdet.item_code and despdet.item_code= sorditem.item_code  "+ 
					"and invoice.confirmed='Y'  "+ 
					"and sorditem.nature<>'C' "+ 
					"and invoice.site_code=? and invoice.cust_code=? and invoice.inv_type=? "+ 
					"and sorditem.item_code=? "+
					"and itrace.item_ser__prom=? "+
					"and ((invoice.tran_date>=? and invoice.tran_date<=? ) or "+
					"( invoice.tran_date>=? and invoice.tran_date<=? and invoice.invoice_id in(select invoice_id from  "+
					"cust_stock_inv where cust_stock_inv.invoice_id = invoice.invoice_id and cust_stock_inv.dlv_flg = 'N' )))";
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, siteCode);
			pstmt.setString(2, custCode);
			pstmt.setString(3, orderType);
			pstmt.setString(4, itemCode);
			pstmt.setString(5, itemSer);
			pstmt.setTimestamp(6, fromDate );
			pstmt.setTimestamp(7, toDate );
			pstmt.setTimestamp(8, prdFrom );
			pstmt.setTimestamp(9, prdTo );
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				freesQty = rs.getDouble("frqty");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println(" Receipt Frees :"+freesQty);
		return freesQty ;
	}*/
	
	/*private double	GetFreeQty(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0,dlvFlgTransit=0;
		PreparedStatement pstmt = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null,rs2 = null,rs3 = null;
		String sql = null;
		String stockDlvFlg="";
		String invoiceIdArray[];
		String invoiceSep="",nature="";
		double freesQty=0.0;
		try
		{		
			// transit quantity for those which are included in period also for dlv flag 'N'
			System.out.println("----calculate Receipt Frees Quantity------------------");
			
			sql = " select distinct idet.invoice_id from " +
					"invdet idet where idet.item_code= ? and  idet.invoice_id in " +
					" ( SELECT distinct invoice.invoice_id invoice_id " +
					" FROM invoice invoice,invoice_trace itrace WHERE invoice.invoice_id=itrace.invoice_id and " +
					" ( invoice.cust_code = ? ) and invoice.site_code =? and invoice.inv_type=? " +
					" and itrace.item_ser__prom=? and  ( invoice.confirmed = 'Y' )  " +
					"and (  (invoice.tran_date >=?)  and (invoice.tran_date <= ? ) " +
					"or ((invoice.tran_date >=? )  and (invoice.tran_date <= ? ) ) and(exists (SELECT cust_stock_inv.invoice_id " +
					" FROM cust_stock_inv cust_stock_inv  WHERE cust_stock_inv.invoice_id = invoice.invoice_id and " +
					" cust_stock_inv.dlv_flg = 'N' )) )) " ;
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, itemCode);
			pstmt.setString(2, custCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, orderType);
			pstmt.setString(5, itemSer);
			pstmt.setTimestamp(6, fromDate );
			pstmt.setTimestamp(7, toDate );
			pstmt.setTimestamp(8, prdFrom );
			pstmt.setTimestamp(9, prdTo );
			

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				
				System.out.println("invoiceId :"+invoiceId);
				
						    sql = "select nature,quantity__stduom from sorddet where sale_order " +
									" =(select sale_order from invoice where invoice_id=?) and item_code=?";
										
							pstmt3 = conn.prepareStatement( sql );
							pstmt3.setString(1,invoiceId );
							pstmt3.setString(2,itemCode );
							rs3 = pstmt3.executeQuery();
							while( rs3.next() )
							{
								quantity = rs3.getDouble("quantity__stduom");
								nature=rs3.getString("nature");
								System.out.println("nature :"+nature);
								if(nature.equalsIgnoreCase("F"))
								{
									freesQty = freesQty + quantity;
									System.out.println(freesQty);
								}
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println(" Receipt Frees :"+freesQty);
		return freesQty ;
	}*/
	/*
	
	private double GetRcpFreeQty(String custCode,String tranIdLast,String orderType, java.sql.Timestamp fromDate, java.sql.Timestamp toDate,String itemCode,String itemSer, Connection conn) throws Exception
	{
		String invoiceId="",selQuery="";
		double quantity = 0.0;
		double rcpFreeQuantity = 0.0;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		String sql = null;
		
		try
		{		
			sql = "select a.invoice_id from invoice a, invdet b " +
				  " where a.invoice_id = b.invoice_id and a.cust_code = ? and " +
				  " ( a.invoice_id in (select cinv.invoice_id from cust_stock_inv cinv where " +
				  " cinv.tran_id = ? and cinv.DLV_FLG = 'N' ) or (a.tran_date >= ? and " +
				  "  a.tran_date <=  ? ) ) and b.item_code = ? and " +
				  "  a.item_ser = ? and a.inv_type=?";
						
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1,custCode );
			pstmt.setString(2,tranIdLast );
			pstmt.setTimestamp(3,fromDate );
			pstmt.setTimestamp(4,toDate );
			pstmt.setString(5,itemCode );
			pstmt.setString(6,itemSer );
			pstmt.setString(7,orderType );
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				//quantity = rs.getDouble("quantity__stduom");
				//rcpQuantity = rcpQuantity + quantity;
				invoiceId = rs.getString("invoice_id");
				
				System.out.println("invoiceId :"+invoiceId);
				System.out.println("quantity :"+quantity);
				
				selQuery = "select invtr.quantity__stduom from invoice_trace invtr ," +
						" sorditem soi where invtr.sord_no=soi.SALE_ORDER " +
						"  and invtr.sord_line_no = soi.LINE_NO and  invtr.exp_lev = soi.EXP_LEV " +
						" and soi.NATURE='F' and invtr.invoice_id = ? ";
				pstmt1 = conn.prepareStatement( selQuery );		
				pstmt1.setString(1,invoiceId);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					quantity=rs.getDouble("quantity__stduom");
					System.out.println("quantity :"+quantity);
					rcpFreeQuantity = rcpFreeQuantity + quantity;
					
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				System.out.println("rcpFreeQuantity : "+rcpFreeQuantity);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return rcpFreeQuantity ;
	}
	
	*/
	/*
	private double GetRetQty(String custCodeHd,java.sql.Timestamp frmDateTstmp, java.sql.Timestamp toDateTstmp,String itemCode,String siteCodeHd,String itemSerHd, Connection conn) throws Exception
	{
		String invoiceId="",invStat="",sretSiteCode="",sretLocCode = "",sretLotNo = "",sretLotSl = "",retRepFlag = "",selQuery="" ;
	
		double quantity = 0.0,sretQty=0.0,retQty=0.0,replQty=0.0;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		String sql = null;
		try
		{	
			sql =" SELECT B.QUANTITY__STDUOM QUANTITY, B.ITEM_CODE ITEM_CODE, "
					+ " A.SITE_CODE SITE_CODE, B.LOC_CODE LOC_CODE, "
					+ " B.LOT_NO LOT_NO, B.LOT_SL LOT_SL, B.RET_REP_FLAG RET_REP_FLAG "
					+ " FROM  SRETURN A, SRETURNDET B "
					+ " WHERE A.TRAN_ID = B.TRAN_ID "
					+ " AND A.CUST_CODE = ? "
					+ " AND A.TRAN_DATE >= ? "
					+ " AND A.TRAN_DATE <= ? "
					+ " AND B.ITEM_CODE = ? "
					+ " AND A.SITE_CODE = ? "
					+"	  and a.item_ser = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCodeHd);
					pstmt.setTimestamp(2,frmDateTstmp );
					pstmt.setTimestamp(3,toDateTstmp );
					pstmt.setString(4,itemCode);
					pstmt.setString(5,siteCodeHd);
					pstmt.setString(6,itemSerHd);
					// endded by chandni
					rs = pstmt.executeQuery();
					while( rs.next() )
					{
						
						sretQty = rs.getDouble("QUANTITY");
						sretSiteCode = rs.getString("SITE_CODE");
						sretLocCode = rs.getString("LOC_CODE");
						sretLotNo = rs.getString("LOT_NO");
						sretLotSl = rs.getString("LOT_SL");
						retRepFlag = rs.getString("RET_REP_FLAG");
						
						System.out.println("sretQty :"+sretQty);
						System.out.println("sretSiteCode :"+sretSiteCode);
						System.out.println("sretLocCode :"+sretLocCode);
						System.out.println("sretLotNo :"+sretLotNo);
						System.out.println("sretLotSl :"+sretLotSl);
						
						
						
						retRepFlag = ( retRepFlag == null || retRepFlag.trim().length() == 0 ) ? "" : retRepFlag.trim();
						System.out.println("retRepFlag :"+retRepFlag);
						
						selQuery = "SELECT INV_STAT FROM STOCK  "
								+ " WHERE ITEM_CODE = ? "
								+ " AND SITE_CODE = ? "
								+ " AND LOC_CODE  = ? "
								+ " AND LOT_NO = ? "
								+ " AND LOT_SL = ? " ;
						
						pstmt1.setString(1,itemCode);
						pstmt1.setString(2,sretSiteCode);
						pstmt1.setString(3,sretLocCode);
						pstmt1.setString(4,sretLotNo);
						pstmt1.setString(5,sretLotSl);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							invStat = rs1.getString("INV_STAT");
							invStat = ( invStat == null || invStat.trim().length() == 0 ) ? "" : invStat.trim();
							if ("R".equalsIgnoreCase(retRepFlag.trim()) && "SALE".equalsIgnoreCase(invStat.trim()) )
							{
								retQty += sretQty;
							}
							else if ( "P".equalsIgnoreCase(retRepFlag.trim()) )
							{
								replQty += sretQty;
							}
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						
					}
					
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			
				sretQty = retQty - replQty ;
				System.out.println("sretQty :"+retQty);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return sretQty ;
	}
	*/
//Added by santosh on 16-FEB-2019 to set addmonth.start
	public java.sql.Timestamp AddMonths(java.sql.Timestamp baseDate, int noOfMonths)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(baseDate);
		cal.add(Calendar.MONTH, noOfMonths);
		java.util.Date newDate = cal.getTime();
		System.out.println("@S@ inside addmonths 7866");
    	SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
    	return java.sql.Timestamp.valueOf(sdt.format(newDate) + " 00:00:00.000");

	}
	//Added by santosh on 16-FEB-2019 to set addmonth.END
	//Changed by dayanand on 28/02/09[ Add getObjNameFromDom() method and checkNull() method req id='WS89SUN078']start
	/*private String getObjNameFromDom(Document dom,String attribute) throws RemoteException,ITMException
	{		
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String objName = "";
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail3");
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
			objName = currDetail.getAttributes().getNamedItem(attribute).getNodeValue();
					
		}
		return objName;
	}*/
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		else
		{
			input=input.trim();
		}
		return input;
	}
	/*private String checkNullInt(String input)	
	{
		if (input == null)
		{
			input="0";
		}
		return input;
	}*/
	//Changed by dayanand on 28/02/09[ Add getObjNameFromDom() method and checkNull() method req id='WS89SUN078']end
/*	private String getNodeValue( Node currDet, String fldName, boolean isAttribute )
	{
		String fldValue = null;
		boolean isFound = false;
		NodeList currNodes = currDet.getChildNodes();
		int currDetLen = currNodes.getLength();
		for(int detIdx = 0; detIdx < currDetLen && !isFound ; detIdx++ )
		{
			Node currNode = currNodes.item( detIdx );
			String nodeName = currNode.getNodeName();

			if( isAttribute == true )
			{
				if ( nodeName.equalsIgnoreCase( "attribute" ) )
				{
					fldValue = currNode.getAttributes().getNamedItem( fldName ).getNodeValue();
					isFound = true;
				}				
			}
			else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( fldName ) )
			{
				fldValue = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : null;
				isFound = true;
			}
		}
		return fldValue;
	}*/
	//added by msalam on 220409 for checking duplicate item
	/*private boolean isItemDuplicate( String columnValue, String itemLineNo, Document dom )
	{
		String updateFlag = null;
		String fldValue = null;
		String nodeName = null;
		Node currNode = null;
		Node currDet = null;
		NodeList currNodes = null;
		String lineNoStr = "0";
		
		int currNodeLen;
		System.out.println("Checking for duplicate items");
		NodeList detailNodes = dom.getElementsByTagName( "Detail2" );		
		int detLen = detailNodes.getLength();
		
		for( int detIdx = 0; detIdx < detLen ; detIdx++ )
		{
			currDet = detailNodes.item( detIdx );
			
			currNodes = currDet.getChildNodes();
			currNodeLen = currNodes.getLength();
			columnValue = columnValue != null ? columnValue.trim() : "";
			for(int curNodeIdx = 0; curNodeIdx < currNodeLen; curNodeIdx++ )
			{
				currNode = currNodes.item( curNodeIdx );
				nodeName = currNode.getNodeName();

				if ( nodeName.equalsIgnoreCase( "attribute" ) )
				{
					updateFlag = currNode.getAttributes().getNamedItem( "updateFlag" ).getNodeValue();
				}				
				else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( "item_code" ) )
				{
					fldValue = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "";
				}
				else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( "line_no" ) )
				{
					lineNoStr = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "0";
				}
			}
			if( !"D".equalsIgnoreCase( updateFlag ) )
			{
				System.out.println("not duplicate lineNoStr [" + lineNoStr + "] itemLineNo [" + itemLineNo+ "]fldValue [" + fldValue + "] columnValue [" + columnValue + "]");
				if( !lineNoStr.trim().equals( itemLineNo.trim() ) && fldValue.trim().equalsIgnoreCase( columnValue ) )
				{
					System.out.println("lineNoStr [" + lineNoStr + "] itemLineNo [" + itemLineNo+ "]fldValue [" + fldValue + "] columnValue [" + columnValue + "]");
					return true;
				}
			}
		}
		return false;		
	}
	*/
	/*public String getAttributesAboutNode(Node node)
	{
        StringBuffer strValue = new StringBuffer();
        short type = node.getNodeType();
        switch (type) {
            case 1: {
                NamedNodeMap attrs = node.getAttributes();
                int len = attrs.getLength();
                for (int i = 0; i < len; ++i) {
                    Attr attr = (Attr)attrs.item(i);
                    strValue.append(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
                }
            }
        }
        return strValue.toString();
    }
	*/
	
	/*-------------------Added by mahendra on dated 27-AUG-2015 --------------------------*/
	
	/*private ArrayList gbfGetDefaultData( String asCustCode, String asItmSer, String asSiteCode, Connection conn )
	{
		long llCount = 0, llRowNum = 0;
		String lsErrCode = null, lsCode = null, lsDescr = null, lsUnit = null, lsLocType = null,
				lsItemSer = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ItemDescr itemDescr = null;
		ArrayList defData = new ArrayList();
		//lds_custstock = create nvo_datastore
		//lds_custstock.reset()
		//lds_custstock.dataobject = 'd_cust_stock_det_brow'
		//lds_custstock.settransobject(sqlca)
		
		sql = "select count(*) cnt from customeritem where cust_code = '" + asCustCode + "'";

		try{
			pstmt = conn.prepareStatement( sql );
			
			rs = pstmt.executeQuery();
			
			if( rs.next() )
			{
				llCount = rs.getInt( "cnt" );
			}
			
		}catch( Exception ex )
		{
			llCount = 0;
			ex.printStackTrace();
		}
		finally
		{
			try{
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
			}catch( Exception e ){ e.printStackTrace(); }
		}
		
		if( llCount == 0 )
		{
			//declare site_item_cursor cursor for  
			sql = " select a.item_code, b.descr, b.unit, b.loc_type, b.item_ser "
				 +"		from siteitem a, item b "
				 +"	where a.item_code = b.item_code "
				 +"		and a.site_code = '" + asSiteCode + "'"
				 +"		and instr( '" + asItmSer + "', rtrim( b.item_ser ) ) > 0 "
				 +"		order by a.item_ser,b.descr ";
		}
		else
		{
			sql = "select a.item_code, b.descr, b.unit, b.loc_type, b.item_ser "
				+"		from customeritem a, item b "
				+"  where a.item_code = b.item_code "
				+"		and a.cust_code = '" + asCustCode + "'" 
				+"		and instr( '" + asItmSer + "', rtrim(b.item_ser)) > 0 " 
				+"		and b.active = 'Y' "
				+"	order by b.descr ";	
		}
		try
		{
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				itemDescr = new ItemDescr();
				
				itemDescr.lsCode = rs.getString( "item_code" );
				itemDescr.lsDescr = rs.getString( "descr" );
				itemDescr.lsUnit = rs.getString( "unit" );
				itemDescr.lsLocType = rs.getString( "loc_type" );
				itemDescr.lsItemSer = rs.getString( "item_ser" );
				
				defData.add( itemDescr );
				//ll_row_num = lds_custstock.insertrow(0)
				
				strBuff.append( "<Detail>" );
				strBuff.append( "<line_no>" ).append( llRowNum ).append( "</line_no>\n" );
				strBuff.append( "<item_code>" ).append( lsCode ).append( "</item_code>" );
				strBuff.append( "<item_descr>" ).append( lsDescr ).append( "</item_descr>" );
				strBuff.append( "<unit>" ).append( lsUnit ).append( "</unit>" ); 
				strBuff.append( "<loc_type>" ).append( lsLocType ).append( "</loc_type>" ); 
				strBuff.append( "<item_ser>" ).append( lsItemSer ).append( "</item_ser>" ); 
				strBuff.append( "</Detail>" );	
				
			}
		}catch( Exception ex )
		{
			ex.printStackTrace();
		}finally
		{
			try{
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
			}catch( Exception e ){ e.printStackTrace(); }
		}
		//return strBuff.toString();	
		return defData;
	}*/
	
	/*private ArrayList getItemFromInv( String invoiceList, Connection conn )
	{
		long llCount = 0, llRowNum = 0;
		String lsErrCode = null, lsCode = null, lsDescr = null, lsUnit = null, lsLocType = null,
				lsItemSer = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ItemDescr itemDescr = null;
		ArrayList defData = new ArrayList();
		//lds_custstock = create nvo_datastore
		//lds_custstock.reset()
		//lds_custstock.dataobject = 'd_cust_stock_det_brow'
		//lds_custstock.settransobject(sqlca)
		
		System.out.println("invoiceList : "+invoiceList);
		
		sql= "select distinct(item_code) from invdet where invoice_id in("+invoiceList+")";
		
		try
		{
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				itemDescr = new ItemDescr();
				
				itemDescr.lsCode = rs.getString( "item_code" );
			//	itemDescr.lsDescr = rs.getString( "descr" );
			//	itemDescr.lsUnit = rs.getString( "unit" );
			//	itemDescr.lsLocType = rs.getString( "loc_type" );
			//	itemDescr.lsItemSer = rs.getString( "item_ser" );
				
				defData.add( itemDescr );
				//ll_row_num = lds_custstock.insertrow(0)
				
				strBuff.append( "<Detail>" );
				strBuff.append( "<line_no>" ).append( llRowNum ).append( "</line_no>\n" );
				strBuff.append( "<item_code>" ).append( lsCode ).append( "</item_code>" );
				strBuff.append( "<item_descr>" ).append( lsDescr ).append( "</item_descr>" );
				strBuff.append( "<unit>" ).append( lsUnit ).append( "</unit>" ); 
				strBuff.append( "<loc_type>" ).append( lsLocType ).append( "</loc_type>" ); 
				strBuff.append( "<item_ser>" ).append( lsItemSer ).append( "</item_ser>" ); 
				strBuff.append( "</Detail>" );	
				
			}
		}catch( Exception ex )
		{
			ex.printStackTrace();
		}finally
		{
			try{
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
			}catch( Exception e ){ e.printStackTrace(); }
		}
		//return strBuff.toString();	
		return defData;
	}*/
	
	
	private ArrayList getItemFromCust(String custCode, String itemSerHdr,String resultitemSer,String siteCode, String orderType,java.sql.Timestamp frmDateTstmp, java.sql.Timestamp toDateTstmp,java.sql.Timestamp prdFrDateTstmp, java.sql.Timestamp prdToDateTstmp,String lastTranId,String newTranId,String selectedInvList, Connection conn ,String calCriItemSerIn,List<String> itemListLast,boolean isItemSer,HashMap<String,Integer> dataMap,String objName) throws ITMException
	{
		//long llCount = 0, llRowNum = 0;
		//String lsErrCode = null, lsCode = null, lsDescr = null, lsUnit = null, lsLocType = null,lsOpStock=null,
			//	lsItemSer = null,lsTranId = null;
		String sql = null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2=null;
		ResultSet rs = null,rs1 = null, rs2=null;
		ItemDescr itemDescr = null;
		LinkedList itemCodeOfReturnInvoice = new LinkedList();
		ArrayList defData = new ArrayList();
		//ItemDescr tmpData = null;
		//AWACS itemMap to be stored in temporary map for iteration of items not came from ES3 logic[14/SEP/2017|Start]
//		HashMap<String ,Integer> hm =new HashMap<String ,Integer>(dataMap);
		HashMap<String ,Integer> hm =null;
		if("awacs_to_es3_prc".equalsIgnoreCase(objName))
		{
			hm =new HashMap<String ,Integer>(dataMap);
		}
		//AWACS itemMap to be stored in temporary map for iteration of items not came from ES3 logic[14/SEP/2017|End]
		
		String invItemCode="",tranIdLast="",tranId="";
		String clStock="0.0";
		String rate="0.0",rateOrg="0.0";
		String itemDescription="",itemSer="",itemSerNew="",itemSerOld="",itemUsage="";
		//lds_custstock = create nvo_datastore
		//lds_custstock.reset()
		//lds_custstock.dataobject = 'd_cust_stock_det_brow'
		//lds_custstock.settransobject(sqlca)
		
		//String tranId="";
		System.out.println("calling getItemFromCust @@@@@@@@@@@@@@@@@@@@@@@@!!!!!!!!!!!!!!");
		System.out.println("custCode :"+custCode);
		System.out.println("itemSerHdr :"+itemSerHdr);
		System.out.println("resultitemSer :"+resultitemSer);
		System.out.println("lastTranId :"+lastTranId);
		System.out.println("newTranId :"+newTranId);
		
		// List <String> itemList = new ArrayList<String>();
		try
		{
			//TODO
			/***Modified by santosh*/
			if(lastTranId != null && lastTranId.trim().length()>0)
			{
				itemCodeOfReturnInvoice = getitemCodeOfReturnInvoiceList(lastTranId,conn,isItemSer,resultitemSer,tranId,custCode,frmDateTstmp,toDateTstmp,prdFrDateTstmp);
			}
			sql="select  distinct item_code from ( " +
					" SELECT  item.item_code FROM invoice invoice ,invoice_trace itrace,item item " +
					" WHERE invoice.invoice_id=itrace.invoice_id and itrace.item_code=item.item_code " +
					" and  invoice.cust_code = ? and  ( invoice.confirmed = 'Y' ) " +
					" and ( (invoice.tran_date >= ? ) and (invoice.tran_date <= ? ) ) " ;
					if(isItemSer)
					{
						//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+resultitemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
						//sql=sql+" and ( itrace.item_ser__prom in ("+resultitemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
						sql=sql+" and ( " +
								//" item.item_ser in ("+resultitemSer+") " +
								" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
								" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
					}
					else
					{
						//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+resultitemSer+")) " ;
						//sql=sql+" and ( itrace.item_ser__prom in ("+resultitemSer+")) " ;
						// added by saurabh for product transfer[14/03/17|Start]
						//sql=sql+" and ( item.item_ser in ("+resultitemSer+")) " ;
						sql=sql+" and (("+resultitemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
						// added by saurabh for product transfer[14/03/17|End]
					}
			sql=sql+" union " ;
					if(isItemSer)
					{
/*				sql=sql+" select  cdet.item_code " +
					" from cust_stock cstk,cust_stock_inv cinv ,cust_stock_det cdet ,invoice invoice,invoice_trace itrace ,item item " +
					" where cstk.tran_id=cinv.tran_id and cinv.tran_id=cdet.tran_id and  cinv.invoice_id=itrace.invoice_id and invoice.invoice_id=itrace.invoice_id " +
					" and cdet.item_code=item.item_code  and cdet.item_code=itrace.item_code and cstk.cust_code= invoice.cust_code " +
					" and cstk.cust_code = ? and invoice.confirmed = 'Y' " +
					" AND invoice.TRAN_DATE < ?  AND " +
					" CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' and CSTK.pos_code is not null "+//Added by saurabh[20/12/16]
					" AND CSTK.ITEM_SER in ('"+itemSerHdr.trim()+"') " +
					//CHanged by saurabh to check product transfer[10/03/17|Start]		
					" AND CSTK.TRAN_ID='"+lastTranId+"' " ;//Added by saurabh 040117
*/					
						sql=sql+" SELECT CDET.ITEM_CODE "+
								" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
								" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
								" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
								" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
								" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
								" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
								" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'   "+
								" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultitemSer+") ) "+ 
								" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";
					}
					else
					{
						sql=sql+" SELECT CDET.ITEM_CODE "+
								" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
								" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
								" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
								" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
								" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
								" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
								" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'   "+
								" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
								" AND ("+resultitemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL)";
					}
					/*else
					{
						sql=sql+" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
								" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
								" and CSTK1.POS_CODE IS NOT NULL  and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,INVOICE.TRAN_DATE) from DUAL)) ";
					}*/
					//CHanged by saurabh to check product transfer[10/03/17|End]
			sql=sql+" union " +
					" select  sdet.item_code " +
					" from sreturn srn,sreturndet sdet ,item item where srn.tran_id=sdet.tran_id and item.item_code=sdet.item_code " +
					" and srn.cust_code=? and srn.tran_date >= ? and srn.tran_date <= ? " +
					" and srn.confirmed='Y'  and " ;
					if(isItemSer)
					{
						//sql=sql+" srn.item_ser in ("+calCriItemSerIn+") and ( item.item_ser in ("+resultitemSer+") OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
						sql=sql+" (" +
								//" item.item_ser in ("+resultitemSer+") " +
								" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
								" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
					}
					else
					{
						//sql=sql+" srn.item_ser not in ("+calCriItemSerIn+") and ( item.item_ser in ("+resultitemSer+")) " ;
						// added by saurabh for product transfer[14/03/17|Start]
						//sql=sql+" ( item.item_ser in ("+resultitemSer+")) " ;
						sql=sql+" (("+resultitemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
						// added by saurabh for product transfer[14/03/17|End]
					}
			sql=sql+" and sdet.ret_rep_flag in('R','P') " +
					" union " ;
					if(isItemSer)
					{
			/*sql=sql+" select  cdet.item_code " +
					" from sreturn srn,sreturndet sdet,cust_stock cstk,cust_stock_inv cinv,cust_stock_det cdet,item item " +
					" where cstk.tran_id= cinv.tran_id and cinv.tran_id= cdet.tran_id and srn.tran_id=sdet.tran_id and " +
					" srn.tran_id=cinv.invoice_id and cstk.cust_code=srn.cust_code AND cdet.item_code=sdet.item_code  " +
					" and item.item_code=sdet.item_code and cstk.cust_code=? and srn.tran_date < ? " +
					" and cinv.dlv_flg='N' and CSTK.pos_code is not null " +
					" AND CSTK.ITEM_SER in ('"+itemSerHdr.trim()+"') " +
					// added by saurabh for product transfer[14/03/17|Strat]
					" AND CSTK.TRAN_ID='"+lastTranId+"' " ;//Added by saurabh 040117
*/					
						sql=sql+" select  CDET.ITEM_CODE "+ 
								" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
								" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
								" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
								" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
								" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultitemSer+") ) "+
								" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";
					}
					/*else
					{
						sql=sql+" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
								" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
								" and CSTK1.POS_CODE IS NOT NULL  and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,SRN.TRAN_DATE) from DUAL)) ";
					}*/
					else
					{
						sql=sql+" select  CDET.ITEM_CODE "+ 
								" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
								" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
								" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
								" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
								" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
								" AND ("+resultitemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
					}
					// added by saurabh for product transfer[14/03/17|End]
					sql=sql+" and sdet.ret_rep_flag in('R','P') " +
							" )";
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, custCode);
			pstmt.setTimestamp(2, frmDateTstmp);
			pstmt.setTimestamp(3, toDateTstmp);
			pstmt.setString(4, custCode);
			if(isItemSer)
			{
				//pstmt.setTimestamp(5, frmDateTstmp);
				pstmt.setTimestamp(5, prdFrDateTstmp);
			}
			else
			{
				pstmt.setTimestamp(5, prdFrDateTstmp);
			}
			pstmt.setString(6, custCode);
			pstmt.setTimestamp(7, frmDateTstmp);
			pstmt.setTimestamp(8, toDateTstmp);
			pstmt.setString(9, custCode);
			if(isItemSer)
			{
				//pstmt.setTimestamp(10, frmDateTstmp);
				pstmt.setTimestamp(10, prdFrDateTstmp);
			}
			else
			{
				pstmt.setTimestamp(10, prdFrDateTstmp);
			}
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				//TODO
				invItemCode=checkNull(rs.getString("item_code"));
				//Removing items from awacs itemMap came from ES3 logic [280717|Start] 
				if("awacs_to_es3_prc".equalsIgnoreCase(objName) && hm != null && hm.containsKey(invItemCode))
				{
					hm.remove(invItemCode);
				}
				//Removing items from awacs itemMap came from ES3 logic [280717|End]
				System.out.println("invItemCode other than cust_stock_det :"+invItemCode);
				System.out.println("itemListLast>>>>>>>"+itemListLast);
				if(!(itemListLast.contains(invItemCode)) )
				{
					if(itemCodeOfReturnInvoice!=null)
					{
						if(!itemCodeOfReturnInvoice.contains(invItemCode))
						{
					itemDescr = getItemBean(invItemCode,frmDateTstmp,custCode,lastTranId,resultitemSer,isItemSer,conn);
						}
					}
					else
					{
						itemDescr = getItemBean(invItemCode,frmDateTstmp,custCode,lastTranId,resultitemSer,isItemSer,conn);
					}
				}
				if(itemDescr!=null)
				{
				defData.add(itemDescr);
				}
				itemDescr=null;
			}
			//Item to mapped as per ES3 logic for Items from Awacs itemMap[290717|Start]
			if("awacs_to_es3_prc".equalsIgnoreCase(objName) && hm != null )
			{
				for(String itemCode : hm.keySet())
				{
					itemDescr = getItemBean(itemCode,frmDateTstmp,custCode,lastTranId,resultitemSer,isItemSer,conn);
					if(itemDescr!=null)
					{
					defData.add(itemDescr);
					}
					itemDescr=null;
				}
			}
			//Item to mapped as per ES3 logic for Items from Awacs itemMap[290717|End]
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		//System.out.println("Final itemList :"+itemList);	
			fileName ="";
		}catch( Exception ex )
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 02/08/19
		}finally
		{
			try{
				
			}catch( Exception e ){ e.printStackTrace(); }
		}
		//return strBuff.toString();	
		return defData;
	}
	
	private LinkedList getitemCodeOfReturnInvoiceList(String lastTranId,
			Connection conn, boolean isItemSer, String resultitemSer,
			String tranId, String custCode, Timestamp frmDateTstmp,
			Timestamp toDateTstmp,Timestamp prdFrDateTstmp)
	{
	String sql ="";
	PreparedStatement pstmt1=null,pstmt=null;
	ResultSet rs1=null,rs=null;
	LinkedList itemCodeOfReturnInvoice = new LinkedList();
	try
	{
			if(isItemSer)
				{
					sql=sql+" select  CDET.ITEM_CODE AS ITEM_CODE "+ 
							" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
							" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
							" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
							" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
							" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
							" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultitemSer+") ) "+
							" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";
				}
				else
				{
					sql=sql+" select  CDET.ITEM_CODE AS ITEM_CODE "+ 
							" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
							" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
							" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
							" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
							" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
							" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
							" AND ("+resultitemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
				}
				sql=sql+" and sdet.ret_rep_flag in('R','P') " ;
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1, custCode);
				if(isItemSer)
				{
					pstmt.setTimestamp(2, prdFrDateTstmp);
				}
				else
				{
					pstmt.setTimestamp(2, prdFrDateTstmp);
				}
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					itemCodeOfReturnInvoice.add(checkNull(rs.getString("ITEM_CODE")));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
		System.out.println("itemCodeOfReturnInvoice"+itemCodeOfReturnInvoice);
		//Asdasdfadsfsdf
		sql="select  distinct item_code from ( " +
				" SELECT  item.item_code FROM invoice invoice ,invoice_trace itrace,item item " +
				" WHERE invoice.invoice_id=itrace.invoice_id and itrace.item_code=item.item_code " +
				" and  invoice.cust_code = ? and  ( invoice.confirmed = 'Y' ) " +
				" and ( (invoice.tran_date >= ? ) and (invoice.tran_date <= ? ) ) " ;
				if(isItemSer)
				{
					sql=sql+" and ( " +
							" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
							" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
				}
				else
				{
					sql=sql+" and (("+resultitemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
				}
		sql=sql+" union " ;
				if(isItemSer)
				{
					sql=sql+" SELECT CDET.ITEM_CODE "+
							" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
							" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
							" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
							" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
							" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
							" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
							" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
							" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'   "+
							" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultitemSer+") ) "+ 
							" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";
				}
				else
				{
					sql=sql+" SELECT CDET.ITEM_CODE "+
							" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
							" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
							" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
							" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
							" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
							" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
							" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
							" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'   "+
							" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
							" AND ("+resultitemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL)";
				}
		sql=sql+" union " +
				" select  sdet.item_code " +
				" from sreturn srn,sreturndet sdet ,item item where srn.tran_id=sdet.tran_id and item.item_code=sdet.item_code " +
				" and srn.cust_code=? and srn.tran_date >= ? and srn.tran_date <= ? " +
				" and srn.confirmed='Y'  and " ;
				if(isItemSer)
				{
					sql=sql+" (" +
							" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
							" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
				}
				else
				{
					sql=sql+" (("+resultitemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
				}
		sql=sql+" and sdet.ret_rep_flag in('R','P')) ";
		///dfADSfasDfsadfsdsdgfsdfg
	/*	sql="select  distinct item_code from ( " +
				" SELECT  item.item_code FROM invoice invoice ,invoice_trace itrace,item item " +
				" WHERE invoice.invoice_id=itrace.invoice_id and itrace.item_code=item.item_code " +
				" and  invoice.cust_code = ? and  ( invoice.confirmed = 'Y' ) " +
				" and ( (invoice.tran_date >= ? ) and (invoice.tran_date <= ? ) ) " ;
				if(isItemSer)
				{
					sql=sql+" and ( " +
							" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
							" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
				}
				else
				{
					sql=sql+" and (("+resultitemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
				}
		sql=sql+" union " +
				" select  sdet.item_code " +
				" from sreturn srn,sreturndet sdet ,item item where srn.tran_id=sdet.tran_id and item.item_code=sdet.item_code " +
				" and srn.cust_code=? and srn.tran_date >= ? and srn.tran_date <= ? " +
				" and srn.confirmed='Y'  and " ;
				if(isItemSer)
				{
					sql=sql+" (" +
							" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
							" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
				}
				else
				{
					sql=sql+" (("+resultitemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
				}
		sql=sql+" and sdet.ret_rep_flag in('R','P')) ";
	*/			
		pstmt = conn.prepareStatement( sql );
		pstmt.setString(1, custCode);
		pstmt.setTimestamp(2, frmDateTstmp);
		pstmt.setTimestamp(3, toDateTstmp);
		pstmt.setString(4, custCode);
		pstmt.setTimestamp(5, prdFrDateTstmp);
		pstmt.setString(6, custCode);
		pstmt.setTimestamp(7, frmDateTstmp);
		pstmt.setTimestamp(8, toDateTstmp);
		rs = pstmt.executeQuery();
		while( rs.next() )
		{
			System.out.println(">>>>>>>>>>"+checkNull(rs.getString("item_code")));
			if(itemCodeOfReturnInvoice.contains(checkNull(rs.getString("item_code"))))
			{
				itemCodeOfReturnInvoice.remove(checkNull(rs.getString("item_code")));
			}
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
	}catch( Exception ex )
			{
				ex.printStackTrace();
			}finally
			{
				try
				{
				}catch( Exception e ){ e.printStackTrace(); }
			}
			return itemCodeOfReturnInvoice;
	}
	private ItemDescr getItemBean(String invItemCode, Timestamp frmDateTstmp,String custCode, String lastTranId,String resultitemSer,boolean isItemSer,Connection conn) throws ITMException 
	{
		String clStock="0.0",rate="0.0",rateOrg="0.0",tranIdLast="",itemSer="",sql="",itemDescription="",itemUsage="";
//		String itemSerNew="",itemSerOld="";
		String sysDate="";
		Timestamp dbSysDate = null;
		PreparedStatement pstmt1 = null; 
		ResultSet rs1 =null;
		ItemDescr itemDescr = null;
		Date currentDate = new Date();
		//ArrayList defData = new ArrayList();
		try 
		{
			SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("New itemcode added from invoice :"+invItemCode);
			sysDate = sdf2.format(currentDate.getTime());
			dbSysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("@S@ inside getItemBean ["+dbSysDate+"]");
			//Added by saurabh for product transfer[14/03/17|Start]
			
			if(isItemSer){
			sql=" SELECT MAX(CS.TRAN_ID) AS TRANIDLAST FROM CUST_STOCK CS,CUST_STOCK_DET CDET WHERE CS.TRAN_ID=CDET.TRAN_ID " +
				" and (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,CS.FROM_DATE)) from DUAL) in ("+resultitemSer+") " +
				" and CDET.ITEM_CODE= '"+invItemCode+"' "+
				" and CS.FROM_DATE = (SELECT MAX(CS1.FROM_DATE) AS FROM_DATE FROM CUST_STOCK CS1,CUST_STOCK_DET CDET1 WHERE CS1.TRAN_ID=CDET1.TRAN_ID " +
				" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CS1.FROM_DATE)) from DUAL) in ("+resultitemSer+") and CDET1.ITEM_CODE='"+invItemCode+"' " +
				" and CS1.FROM_DATE < ? and CS1.CUST_CODE= ? " +
				" AND CS1.POS_CODE IS NOT NULL AND CS1.STATUS <> 'X'  AND CS1.CONFIRMED='Y' AND CS1.STATUS='S') " +
				" and CS.CUST_CODE= '"+custCode+"' AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  AND CS.CONFIRMED='Y' AND CS.STATUS='S' ";
			}
			else
			{
				sql=" SELECT MAX(CS.TRAN_ID) AS TRANIDLAST FROM CUST_STOCK CS,CUST_STOCK_DET CDET WHERE CS.TRAN_ID=CDET.TRAN_ID " +
						" and CS.ITEM_SER=(select FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,CS.FROM_DATE) from DUAL) " +
						" and CDET.ITEM_CODE= '"+invItemCode+"' "+
						" and CS.FROM_DATE = (SELECT MAX(CS1.FROM_DATE) AS FROM_DATE FROM CUST_STOCK CS1,CUST_STOCK_DET CDET1 WHERE CS1.TRAN_ID=CDET1.TRAN_ID " +
						" and CS1.ITEM_SER=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CS1.FROM_DATE) from DUAL) and CDET1.ITEM_CODE='"+invItemCode+"' " +
						" and CS1.FROM_DATE < ? and CS1.CUST_CODE= ? " +
						" AND CS1.POS_CODE IS NOT NULL AND CS1.STATUS <> 'X'  AND CS1.CONFIRMED='Y' AND CS1.STATUS='S') " +
						" and CS.CUST_CODE= '"+custCode+"' AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  AND CS.CONFIRMED='Y' AND CS.STATUS='S' ";	
			}
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setTimestamp( 1, frmDateTstmp );
			pstmt1.setString(2, custCode);
			rs1 = pstmt1.executeQuery();
			if (rs1.next()) 
			{
				tranIdLast = rs1.getString("tranIdLast");
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			//Added by saurabh for product transfer[14/03/17|End]
			System.out.println("@s@>>>>>fileName["+fileName+"]");
			//genLogFile = new GenLogFile();
			/*System.out.println("@S@ genLogFile["+genLogFile+"]");
			genLogFile.writeLog(fileName, "in getItemBean itemChange case 3 ["+sql+"]");
			genLogFile.writeLog(fileName, "in getItemBean itemChange case 3  frmDateTstmp["+frmDateTstmp+"][custCode]["+custCode+"]");
			*/
			sql = "	select csd.cl_stock,CASE WHEN csd.rate IS NULL THEN 0 ELSE csd.rate END  as rate," +
					" CASE WHEN csd.rate__org IS NULL THEN 0 ELSE csd.rate__org END as rate__org,csd.item_ser from " +
					"cust_stock cs , cust_stock_det csd where cs.tran_id=csd.tran_id and csd.tran_id=? and csd.item_code=? AND cs.status <> 'X' ";
			pstmt1 = conn.prepareStatement(sql);
			//added by saurabh for product transfer[14/03/17|Start]
			if(tranIdLast!=null && tranIdLast.trim().length() > 0)
			{
				pstmt1.setString(1, tranIdLast);
			}
			else
			{
				pstmt1.setString(1, lastTranId);
			}
			//added by saurabh for product transfer[14/03/17|End]
			pstmt1.setString(2, invItemCode);
			rs1 = pstmt1.executeQuery();
			if (rs1.next()) 
			{
				clStock = rs1.getString("cl_stock");
				rate = rs1.getString("rate");
				rateOrg = checkNull(rs1.getString("rate__org")).trim();
//				itemSerOld=checkNull(rs1.getString("item_ser"));commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			
			if(rate.equalsIgnoreCase("0.0")){
				
				sql = "	select CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
						" CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org,item_ser from " +
						"cust_stock_det  where tran_id=? and item_code=? ";
				pstmt1 = conn.prepareStatement(sql);
				//added by saurabh for product transfer[14/03/17|Start]
				if(tranIdLast!=null && tranIdLast.trim().length() > 0)
				{
					pstmt1.setString(1, tranIdLast);
				}
				else
				{
					pstmt1.setString(1, lastTranId);
				}
				//added by saurabh for product transfer[14/03/17|End]
				pstmt1.setString(2, invItemCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					//clStock = rs1.getString("cl_stock");
					rate = rs1.getString("rate");
					rateOrg = checkNull(rs1.getString("rate__org")).trim();
//					itemSerOld=checkNull(rs1.getString("item_ser"));commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
			}
			
			
			if((fileName== null || fileName.trim().length()==0) && (genLogFile == null) )
			{
				genLogFile = new GenLogFile();
				fileName= custCode.trim()+"_"+itemSer+"_"+invItemCode;
			}
			else if((genLogFile == null))
			{
				genLogFile = new GenLogFile();
			}
			System.out.println("@S@fileName["+fileName+"]");
			//Changed By priyankaC on 11-FEB-2019 [Start]
			//genLogFile.writeLog(fileName, "in getItemBean itemChange case 3 ["+sql+"]");
		//	genLogFile.writeLog(fileName, "in getItemBean itemChange case 3  tranIdLast["+tranIdLast+"][lastTranId from dom]["+lastTranId+"]");
			//Changed By priyankaC on 11-FEB-2019 [END]
//			commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
//			sql= " select descr,item_ser,item_usage from item where item_code = ?";
			sql= " select descr,item_usage,fn_get_itemser_change(item.item_code,?) as item_ser from item where item_code = ?";
			pstmt1 = conn.prepareStatement(sql);
			//pstmt1.setTimestamp(1, frmDateTstmp); Commented by santosh to set current date to get item ser on the bases if current date 
			pstmt1.setTimestamp(1, dbSysDate);
			pstmt1.setString(2, invItemCode);
			rs1 = pstmt1.executeQuery();
			if(rs1.next())
			{
				itemDescription=checkNull(rs1.getString("descr"));
				itemSer=checkNull(rs1.getString("item_ser"));
				itemUsage=checkNull(rs1.getString("item_usage"));
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			System.out.println("@S@itemSer["+itemSer+"]");
			
			genLogFile.writeLog(fileName, "in getItemBean itemChange case 3 ["+sql+"]");
			genLogFile.writeLog(fileName, "in getItemBean itemChange case 3  frmDateTstmp["+frmDateTstmp+"][invItemCode]["+invItemCode+"]");
			
			/**Commented by santosh to set item series by using function 
			 * if(tranIdLast!=null && tranIdLast.trim().length() > 0)
			{
				itemSer=itemSerOld;
			}
			else
			{
				itemSer=itemSerNew;
			}
			*/
			if(rateOrg == null || rateOrg.trim().length()==0 || "0".equalsIgnoreCase(rateOrg))
			{
				rateOrg=rate;
			}
			if("F".equalsIgnoreCase(itemUsage)){
			itemDescr = new ItemDescr();
			itemDescr.setItemCode(invItemCode);
			itemDescr.setLsOpStock(clStock);
			itemDescr.setLsrateold(rate);
			itemDescr.setLsrateorgold(rateOrg);
			itemDescr.setItemDescription(itemDescription);
			itemDescr.setLsItemSer(itemSer);
			
			itemDescr.lsItemSer = itemSer;
			itemDescr.lsCode = invItemCode;
			itemDescr.lsOpStock= clStock;
			itemDescr.lsrateold= rate;
			itemDescr.lsrateorgold= rateOrg;
			//defData.add(itemDescr);
			//itemList.add(invItemCode);
			
			}
		
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return itemDescr;
	}
	
	private HashMap<String,ArrayList<String>> getItemwiseInvoice(String custCode, String itemSerHdr,String resultitemSer,String siteCode, String orderType,java.sql.Timestamp frmDateTstmp, java.sql.Timestamp toDateTstmp,java.sql.Timestamp prdFrDateTstmp, java.sql.Timestamp prdToDateTstmp,String lastTranId,String newTranId,String selectedInvList, Connection conn ,boolean isItemSer) throws ITMException
	{
		//long llCount = 0, llRowNum = 0;
		//String lsErrCode = null, lsCode = null, lsDescr = null, lsUnit = null, lsLocType = null,lsOpStock=null,lsItemSer = null,lsTranId = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//ItemDescr itemDescr = null;
		String itemCode="",invoiceId="";
		//String clStock="0.0";
		//String rate="0.0",rateOrg="0.0";
		//String itemDescription="",itemSer="",itemSerNew="",itemSerOld="",itemUsage="";
		//String tranId="";
		System.out.println("calling getItemFromCust @@@@@@@@@@@@@@@@@@@@@@@@!!!!!!!!!!!!!!");
		System.out.println("custCode :"+custCode);
		System.out.println("itemSerHdr :"+itemSerHdr);
		System.out.println("resultitemSer :"+resultitemSer);
		System.out.println("lastTranId :"+lastTranId);
		System.out.println("newTranId :"+newTranId);
		ArrayList<String> invList =null;
		HashMap<String ,ArrayList<String>> invHashMapStr=new HashMap<String, ArrayList<String>>();
		try
		{
			
			sql="select  distinct item_code,invoice_id from ( " +
					" SELECT  item.item_code,invoice.invoice_id FROM invoice invoice ,invoice_trace itrace,item item " +
					" WHERE invoice.invoice_id=itrace.invoice_id and itrace.item_code=item.item_code " +
					" and  invoice.cust_code = ? and  ( invoice.confirmed = 'Y' ) " +
					" and ( (invoice.tran_date >= ? ) and (invoice.tran_date <= ? ) ) " ;
					if(isItemSer)
					{
						sql=sql+" and ( " +
								" ( (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
								" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
					}
					else
					{
						sql=sql+" and (("+resultitemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,sysdate)) from dual)) " ;
					}
			sql=sql+" union " ;
					if(isItemSer)
					{
						sql=sql+" SELECT CDET.ITEM_CODE,cinv.invoice_id "+
								" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
								" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
								" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
								" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
								" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
								" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
								" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'   "+
								" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultitemSer+") ) "+ 
								" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";	
					}
					else
					{
						sql=sql+" SELECT CDET.ITEM_CODE,cinv.invoice_id "+
								" FROM CUST_STOCK CSTK,CUST_STOCK_INV CINV ,CUST_STOCK_DET CDET,INVOICE_TRACE ITRACE "+
								" WHERE CSTK.TRAN_ID=CINV.TRAN_ID AND CSTK.TRAN_ID=CDET.TRAN_ID AND "+
								" CINV.INVOICE_ID=ITRACE.INVOICE_ID AND CDET.ITEM_CODE=ITRACE.ITEM_CODE AND "+ 
								" CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND  "+
								" CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+ 
								" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
								" and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'   "+
								" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+ 
								" AND ("+resultitemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL)";
					}
			sql=sql+" union " +
					" select  sdet.item_code,sdet.tran_id " +
					" from sreturn srn,sreturndet sdet ,item item where srn.tran_id=sdet.tran_id and item.item_code=sdet.item_code " +
					" and srn.cust_code=? and srn.tran_date >= ? and srn.tran_date <= ? " +
					" and srn.confirmed='Y'  and " ;
					if(isItemSer)
					{
						sql=sql+" ( " +
								" ( (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual) in ("+resultitemSer+") ) "+
								" OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+resultitemSer+") )) ) " ;
					}
					else
					{
						sql=sql+" (("+resultitemSer+") = (select trim(fn_get_itemser_change(sdet.item_code,sysdate)) from dual)) " ;
					}
			sql=sql+" and sdet.ret_rep_flag in('R','P') " +
					" union " ;
					if(isItemSer)
					{
						sql=sql+" select  CDET.ITEM_CODE,SDET.TRAN_ID "+ 
								" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
								" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
								" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
								" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X'  "+ 
								" and (select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL) in ("+resultitemSer+")  ) "+
								" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";
					}
					else
					{
						sql=sql+" select  CDET.ITEM_CODE,SDET.TRAN_ID "+ 
								" from SRETURNDET SDET,CUST_STOCK CSTK,CUST_STOCK_INV CINV,CUST_STOCK_DET CDET where CSTK.tran_id= CINV.tran_id and CSTK.tran_id= CDET.tran_id "+
								" and  SDET.tran_id=CINV.invoice_id AND CDET.ITEM_CODE=SDET.ITEM_CODE and  CSTK.cust_code = ?  and CSTK.from_date = ? "+  
								" and CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  AND CASE WHEN CINV.DLV_FLG IS NULL THEN 'N' ELSE CINV.DLV_FLG END='N' "+ 
								" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1 ,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID and CSTK1.TRAN_ID = CSTK.TRAN_ID "+ 
								" and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCode+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S'and CSTK1.POS_CODE IS NOT NULL AND CSTK1.STATUS <> 'X' "+ 
								" and CSTK1.item_ser=(select FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE) from DUAL)) "+
								" AND ("+resultitemSer+") = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) ";
					}
					sql=sql+" and sdet.ret_rep_flag in('R','P') " +
							" )";
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1, custCode);
			pstmt.setTimestamp(2, frmDateTstmp);
			pstmt.setTimestamp(3, toDateTstmp);
			pstmt.setString(4, custCode);
			if(isItemSer)
			{
				pstmt.setTimestamp(5, prdFrDateTstmp);
			}
			else
			{
				pstmt.setTimestamp(5, prdFrDateTstmp);
			}
			pstmt.setString(6, custCode);
			pstmt.setTimestamp(7, frmDateTstmp);
			pstmt.setTimestamp(8, toDateTstmp);
			pstmt.setString(9, custCode);
			if(isItemSer)
			{
				pstmt.setTimestamp(10, prdFrDateTstmp);
			}
			else
			{
				pstmt.setTimestamp(10, prdFrDateTstmp);
			}
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				itemCode=checkNull(rs.getString("item_code"));
				invoiceId=checkNull(rs.getString("invoice_id"));
				invList=new ArrayList<String>();
				if(invHashMapStr.containsKey(itemCode)){
				invList=invHashMapStr.get(itemCode);
				invList.add(invoiceId);
				invHashMapStr.put(itemCode, invList);
				}else
				{
					invList.add(invoiceId);
					invHashMapStr.put(itemCode,invList);
				}
				invList=null;	
			}
			System.out.println("invHashMapStr>>>>>>>>>"+invHashMapStr);
			System.out.println("invHashMapStr>>>>>>>>>"+invHashMapStr.toString());
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 02/08/19
		}
		return invHashMapStr;
	}
	
	public static  class ItemDescr
	{
		public String lsCode = null;
		public String lsDescr = null;
		public String lsUnit = null;
		public String lsLocType = null;
		public String lsItemSer = null;
		public String lsTranId = null;
		public String lsOpStock = null;
		public String lsrateold = null;
		public String lsrateorgold = null;
		public String ItemDescription = null;
		
		public void setItemCode(String lsCode)
		{
			this.lsCode=lsCode;
		}
	    
	    public String getItemCode()
		{
			return this.lsCode;
		}
	    public String getLsCode()
        {
        	return lsCode;
        }

		public void setLsCode(String lsCode)
        {
        	this.lsCode = lsCode;
        }

		public String getLsDescr()
        {
        	return lsDescr;
        }

		public void setLsDescr(String lsDescr)
        {
        	this.lsDescr = lsDescr;
        }

		public String getLsUnit()
        {
        	return lsUnit;
        }

		public void setLsUnit(String lsUnit)
        {
        	this.lsUnit = lsUnit;
        }

		public String getLsLocType()
        {
        	return lsLocType;
        }

		public void setLsLocType(String lsLocType)
        {
        	this.lsLocType = lsLocType;
        }

		public String getLsItemSer()
        {
        	return lsItemSer;
        }

		public void setLsItemSer(String lsItemSer)
        {
        	this.lsItemSer = lsItemSer;
        }

		public String getLsTranId()
        {
        	return lsTranId;
        }

		public void setLsTranId(String lsTranId)
        {
        	this.lsTranId = lsTranId;
        }

		public String getLsOpStock()
        {
        	return lsOpStock;
        }

		public void setLsOpStock(String lsOpStock)
        {
        	this.lsOpStock = lsOpStock;
        }

		public String getLsrateold()
        {
        	return lsrateold;
        }

		public void setLsrateold(String lsrateold)
        {
        	this.lsrateold = lsrateold;
        }

		public String getLsrateorgold()
        {
        	return lsrateorgold;
        }

		public void setLsrateorgold(String lsrateorgold)
        {
        	this.lsrateorgold = lsrateorgold;
        }

		public String getItemDescription()
        {
        	return ItemDescription;
        }

		public void setItemDescription(String itemDescription)
        {
        	ItemDescription = itemDescription;
        }
		
		public static Comparator<ItemDescr> ItemNameComparator = new Comparator<ItemDescr>()
				 {

				public int compare(ItemDescr s1, ItemDescr s2) {
				   String itemCode1 = s1.getItemDescription();
				   String itemCode2 = s2.getItemDescription();
				   return itemCode1.compareTo(itemCode2);

			    }
				};

		@Override
		public String toString() {
			return "ItemDescr [lsCode=" + lsCode + ", lsDescr=" + lsDescr
					+ ", lsUnit=" + lsUnit + ", lsLocType=" + lsLocType
					+ ", lsItemSer=" + lsItemSer + ", lsTranId=" + lsTranId
					+ ", lsOpStock=" + lsOpStock + ", lsrateold=" + lsrateold
					+ ", lsrateorgold=" + lsrateorgold + ", ItemDescription="
					+ ItemDescription + "]";
		}
				
				
	}
	/*private String getObjName(Document dom2,String objContext)
	{	
		Node elementName = null;//, parentNode = null;
		NodeList elementList = null;
		//Element elementAttr = null;
		String objName = "";
		elementList = dom2.getElementsByTagName("Detail"+objContext);
		elementName = elementList.item(0);
		if (elementName!=null && ("Detail"+objContext).equalsIgnoreCase(elementName.getNodeName()))
		{
			NamedNodeMap etlAttributes = elementName.getAttributes();
			if (etlAttributes!=null)
			{
				if (etlAttributes.getNamedItem("objName")!=null)
				{
					objName = etlAttributes.getNamedItem("objName").getNodeValue();
				}
			}
		}
		return objName;
	}*/
	
	
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
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 02/08/19
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
	 
	 private String GetDlvFlag(String invoiceId, Connection conn) throws Exception
	{
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = null;
			String dlvFlag="";
			
			try
			{
				sql = "select dlv_flg from cust_stock_inv where invoice_id=?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,invoiceId );
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					dlvFlag = rs.getString("dlv_flg");
					System.out.println("dlvFlag :"+dlvFlag);
				}
				else
				{
					dlvFlag="Y";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("dlvFlag :"+dlvFlag);
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				//System.out.println("Exception ::" + exception.getMessage());
				throw new ITMException( exception );
			}
			return dlvFlag ;
		}
	 
	 private Timestamp getPreviousDate(java.sql.Timestamp FromDate, Connection conn) throws ITMException
	 {
		 String varValueStr="";
		 int varValue = 0;
		 java.sql.Timestamp prdFrom=null;//,compDate=null,ToDate=null;
	 try
	 {

			varValueStr =  dist.getDisparams("999999","SSD_TRANSIT_DAYS",conn);
			System.out.println("varValue.." + varValueStr);
			
			if( varValueStr.equalsIgnoreCase("NULLFOUND") )
			{
				System.out.println("varValue is NULLFOUND" + varValueStr);
			}
			else
			{
				varValue = Integer.parseInt(varValueStr);
			}
		 	//SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
	 		System.out.println("varValue = .. " + varValue);
	 		varValue = (-1) * varValue; 
	 		System.out.println("changed varValue for to date>>>>>>>>>>>>>>>>  = .. " + varValue);
	 		/*compDate = utilMethod.RelativeDate(ToDate,varValue);
	 				
	 		System.out.println("compDate >>>>>>"+compDate);*/
	 						
	 		varValue = (-1) * 1; 
	 		System.out.println("changed varValue>>>>>>>>>>>>>>>>  = .. " + varValue);
	 							
	 		if(FromDate !=null)
	 		{
	 			prdFrom = utilMethod.RelativeDate(FromDate,varValue);
	 		}
	 							
	 		System.out.println("prdFrom :"+prdFrom);
	 }
	 catch(Exception e)
	 {
		 throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
	 }
	 	return prdFrom;
	 }
		
		private boolean isSelected(String selectedInvList,String tranId) throws ITMException
	    {
			boolean isexist = false;
			String[] invoiceIdArray;
			String invoiceId="";
			ArrayList<String> selectedInv=new ArrayList<String>();
			//System.out.println("selectedInvList>>>>"+selectedInvList);
			if(selectedInvList.trim().length() > 0)
			{
				invoiceIdArray = selectedInvList.split(",");
				for (int i = 0; i < invoiceIdArray.length ;i++)
				{
					//System.out.println("invoice>>"+i+">>>>>>>"+invoiceIdArray[i]);
					
					invoiceId  = invoiceIdArray[i];
					selectedInv.add(invoiceId.substring(1, invoiceId.length()-1).trim());
				}
				//System.out.println("selectedInv>>>>"+selectedInv);
				//System.out.println("tranId>>>>"+tranId+"]");
				if(selectedInv.contains(tranId.trim()))
				{
					System.out.println("INSIDE ISEXIST");
					isexist = true;
				}
				
			}
			//System.out.println("isexist>>>>>"+isexist);
			return isexist;
	    }
		public boolean isValidNumber(String number) throws ITMException, Exception
		{

			Boolean isReult = true;
			int lineNoResult=0;
			try
			{
				System.out.println("number>>["+number+"]");
				lineNoResult=Integer.parseInt(number);
				System.out.println("lineNoResult>>>>>>>>"+lineNoResult);

			} catch (NumberFormatException e)
			{

				isReult = false;
			}
			return isReult;

		}
		public boolean isValidDouble(String number) throws ITMException, Exception
		{

			Boolean isReult = true;
			double amount=0.0;
			try
			{
				amount = Double.parseDouble(number);
				System.out.println("amount>>>>>>>>"+amount);

			} catch (NumberFormatException e)
			{

				isReult = false;
			}
			return isReult;

		}
		private ArrayList<Double> GetRcpQtyCommon(String quantityFlag,String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String transitInvList,String itenmCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn,String calCriItemSerIn,boolean isItemSer,String  tranIdNew) throws Exception
		{
			//NodeList parentNodeList=null;
			//NodeList childList = null;
			//Node parentNode=null;
			//Node childNode = null;
			//String invoiceId = null;
			//String dlvFlg = null ;
			double receiptQty=0,ratesStduom=0,disCnt=0;//,rcpQuantity = 0, quantity = 0,dlvFlgTransit=0;
			double QuantityValue=0.0,receiptQtySum=0.0;
			PreparedStatement pstmt = null;//,pstmt2 = null;
			ResultSet rs = null;//,rs2 = null;
			String sql = null;
			//String stockDlvFlg="";
			ArrayList<Double> rcpQtyListMethod=new ArrayList<Double>();
			try
			{		
				System.out.println("calculate GetRcpQtyCommon "+quantityFlag+"quantity!!!!!!!!!!itemcode"+itenmCode);
				if("R".equalsIgnoreCase(quantityFlag))
				{
					sql =  " SELECT (itrace.QUANTITY__STDUOM) QUANTITY__STDUOM, itrace.RATE__STDUOM RATE__STDUOM ,itrace.DISCOUNT DISCOUNT FROM" +
							//" invoice invoice,invoice_trace itrace ,item item " +
							" invoice invoice,invoice_trace itrace, " +
							// added by ketan
							" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  " ;
							if(tranIdNew.trim().length()==0){
							sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y' " ;
							}
							sql=sql+" ) CINV "+
							" 	where invoice.invoice_id=itrace.invoice_id " +
							//"and itrace.item_code=item.item_code " +
							" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) ";
					if(tranIdNew.trim().length()>0)
					{
						sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
					}
					else
					{
						sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) ";
					}
					//added by ketan
					/*else
					{
						sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) And  CINV.INVOICE_ID IS NULL ";
					}*/
					sql=sql+"AND invoice.cust_code = ? " +
							//" 	and invoice.site_code = ? " +
							//" and invoice.inv_type=? " +
							//" and itrace.item_ser__prom in("+itemSer+")  "+
							//" and ( itrace.item_ser__prom in("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) "+
							 //" and invoice.tran_date between ? and ? " +
							" AND itrace.RATE__STDUOM>0.001 "
							+ " and itrace.item_code = ? and itrace.invoice_id in("+selectedInvList+") " ;
							/*if(isItemSer)
							{
								//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) ";
								//sql=sql+" and ( itrace.item_ser__prom in("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) ";
								sql=sql+" and ( item.item_ser in("+itemSer+") OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) ";
							}else
							{
								//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and itrace.item_ser__prom in("+itemSer+")  ";
								//sql=sql+" and itrace.item_ser__prom in("+itemSer+")  ";
								//Changed by saurabh [23/03/17|Start]
								//sql=sql+" and item.item_ser in("+itemSer+")  ";
								sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
								//Changed by saurabh [23/03/17|End]
							}*/
				}else if("T".equalsIgnoreCase(quantityFlag))
				{
					sql =  " SELECT (itrace.QUANTITY__STDUOM) QUANTITY__STDUOM, itrace.RATE__STDUOM RATE__STDUOM ,itrace.DISCOUNT DISCOUNT FROM" +
							//" invoice invoice,invoice_trace itrace,item item " +
							" invoice invoice,invoice_trace itrace, " +
							// added by ketan
							" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X' " ;
					if(tranIdNew.trim().length()==0){
							sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y'" ;
					}
							sql=sql+" ) CINV "+
							" where invoice.invoice_id=itrace.invoice_id " +
							//" and itrace.item_code=item.item_code " +
							" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+)  ";
					if(tranIdNew.trim().length()>0)
					{
						sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
					}
					else
					{
						sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) And  CINV.INVOICE_ID IS NULL ";
					}
							sql=sql+" AND  invoice.cust_code = ? "+
							//" 	and invoice.site_code = ? " +
							//"  and invoice.inv_type=? " +
							// +" and ( itrace.item_ser__prom in("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  "
							//" and invoice.tran_date between ? and ? " +
							" AND itrace.RATE__STDUOM>0.001 "+
							" and itrace.item_code = ? and itrace.invoice_id in("+transitInvList+") " ;
							/*if(isItemSer)
							{
								//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
								//sql=sql+" and ( itrace.item_ser__prom in("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
								sql=sql+" and ( item.item_ser in("+itemSer+") OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
							}else
							{
								//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and itrace.item_ser__prom in("+itemSer+")  ";
								//sql=sql+" and itrace.item_ser__prom in("+itemSer+")  ";
								//Changed by saurabh [23/03/17|Start]
								//sql=sql+" and item.item_ser in("+itemSer+")  ";
								sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
								//Changed by saurabh [23/03/17|End]
							}*/
				}
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, custCode );
					//pstmt.setString( 2, siteCode );
					//pstmt.setString( 2, orderType );
					//pstmt.setString( 3, itemSer);
					//pstmt.setTimestamp( 5, fromDate );
					//pstmt.setTimestamp( 6, toDate );
					pstmt.setString( 2, itenmCode );
					rs = pstmt.executeQuery();
					//double rcpCur =0, rcpPre = 0, totRcp = 0;
					while (rs.next())
					{			    
						receiptQty = rs.getDouble("QUANTITY__STDUOM");
						ratesStduom = rs.getDouble("RATE__STDUOM");
						disCnt = rs.getDouble("DISCOUNT");
						receiptQtySum=receiptQtySum+receiptQty;
						//QuantityValue=QuantityValue+(receiptQty*ratesStduom);
						QuantityValue=QuantityValue+getRequiredDcml(((receiptQty*ratesStduom)-((receiptQty*ratesStduom*disCnt)/100)),3);
					}	
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;		
				
					System.out.println("ratesStduom@@@@@@@@>>>>>>> :"+ratesStduom);
					System.out.println("rate@@@@@@@@>>>>>>> :"+receiptQty);
					System.out.println("QuantityValue>>>>"+QuantityValue);
					
					rcpQtyListMethod.add(receiptQtySum);
					rcpQtyListMethod.add(QuantityValue);
					System.out.println("rcpQtyList@@@@@@@@@222>>>>>"+rcpQtyListMethod);
				
				
				
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				throw new ITMException( exception );
			}
			return rcpQtyListMethod ;
		}
		
		private ArrayList<Double> GetReplacementList(String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String transitInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn,String calCriItemSerIn,boolean isItemSer,String tranIdLast,String tranIdNew) throws Exception
		{
			//NodeList parentNodeList=null;
			//NodeList childList = null;
			//Node parentNode=null;
			//Node childNode = null;
			//String invoiceId = null;
			//String dlvFlg = null ;
			double sretRate=0.0;//,rcpQuantity = 0, quantity = 0,dlvFlgTransit=0,receiptQty=0,ratesStduom=0;
			PreparedStatement pstmt = null,pstmt1 = null;
			ResultSet rs = null,rs1 = null;
			String sql = null;
			//String stockDlvFlg="";
			double sretQtyAll=0.0,sretDiscnt=0.0,totalVal=0.0;//,retQtyAll=0.0,replQtyAll=0.0,sretQtyAllTot=0.0,retFreeQtyAll=0.0;
			double returnReciptRepl=0.0,billRetQty=0.0,returnTransitRepl=0.0,billRetQtyBonus=0.0;
			double returnReciptReplVal=0.0,billRetQtyVal=0.0,returnTransitReplVal=0.0,billRetQtyBonusVal=0.0;
			String retRepFlagAll="",sretLotNoAll="",sretunTranId;
			String  sretSiteCodeAll="";//,selQuery="";
			String  sretLocCodeAll="";
			String  sretLotSlAll="";
			//String  invStat="";
			String  lineTypeAll="";
			int cnt=0;
			ArrayList<Double> replQtyListMethod=new ArrayList<Double>();
			try
			{		
				System.out.println("calculate GetReplacement quantity!!!!!!!!!!");
				
				sql =" SELECT A.TRAN_ID,B.QUANTITY__STDUOM QUANTITY,B.RATE__STDUOM RATE, B.ITEM_CODE ITEM_CODE, "
						+ " A.SITE_CODE SITE_CODE, B.LOC_CODE LOC_CODE, "
						+ " B.LOT_NO LOT_NO, B.LOT_SL LOT_SL, B.RET_REP_FLAG RET_REP_FLAG,B.line_type ,B.DISCOUNT "
						+ " FROM  SRETURN A, SRETURNDET B, "
						//added by ketan 04-may-21
						//+" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL " ;
						+" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  " ;		
				if(tranIdNew.trim().length()==0){
						sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y' " ;
						}
						sql=sql+" ) CINV "
						+ " WHERE A.TRAN_ID = B.TRAN_ID " 
						//+ " AND ITEM.ITEM_CODE=B.ITEM_CODE "
						+" AND B.TRAN_ID = CINV.INVOICE_ID (+) AND B.ITEM_CODE = CINV.ITEM_CODE (+) ";
						if(tranIdNew.trim().length()>0)
						{
							sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
						}
						else
						{
							sql=sql+" AND B.ITEM_SER = CINV.ITEM_SER (+) And  CINV.INVOICE_ID IS NULL ";
						}
						sql=sql+" AND A.CUST_CODE = ? "+
						//+ " AND A.TRAN_DATE >= ? "
						//+ " AND A.TRAN_DATE <= ? "*/
						" AND A.TRAN_ID IN ("+selectedInvList+","+transitInvList+") "+
						" AND B.ITEM_CODE = ? ";
						/*if(isItemSer)
						{
							//sql=sql+" and A.item_ser in ("+calCriItemSerIn+") and ( ITEM.item_ser in ("+itemSer+") OR((ITEM.item_ser,B.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
							sql=sql+" and ( ITEM.item_ser in ("+itemSer+") OR((ITEM.item_ser,ITEM.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
						}else
						{
							//sql=sql+" and A.item_ser not in ("+calCriItemSerIn+") and ( ITEM.item_ser in ("+itemSer+"))";
							sql=sql+" and ( ITEM.item_ser in ("+itemSer+"))";
						}*/
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						/*pstmt.setTimestamp(2,fromDate );
						pstmt.setTimestamp(3,toDate );
						*/pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						while( rs.next() )
						{
							
							sretQtyAll = rs.getDouble("QUANTITY");
							sretRate = rs.getDouble("RATE");
							sretSiteCodeAll = rs.getString("SITE_CODE");
							sretLocCodeAll = rs.getString("LOC_CODE");
							sretLotNoAll = rs.getString("LOT_NO");
							sretLotSlAll = rs.getString("LOT_SL");
							retRepFlagAll = rs.getString("RET_REP_FLAG");
							lineTypeAll = rs.getString("line_type");
							sretunTranId = rs.getString("TRAN_ID");
							sretDiscnt = rs.getDouble("DISCOUNT");
							
							System.out.println("sretQtyAll :"+sretQtyAll);
							System.out.println("sretSiteCodeAll :"+sretSiteCodeAll);
							System.out.println("sretLocCodeAll :"+sretLocCodeAll);
							System.out.println("sretLotNoAll :"+sretLotNoAll);
							System.out.println("sretLotSlAll :"+sretLotSlAll);
							System.out.println("sretunTranId :"+sretunTranId);
							
							retRepFlagAll = ( retRepFlagAll == null || retRepFlagAll.trim().length() == 0 ) ? "" : retRepFlagAll.trim();
							lineTypeAll = ( lineTypeAll == null || lineTypeAll.trim().length() == 0 ) ? "" : lineTypeAll.trim();
							System.out.println("retRepFlagAll :"+retRepFlagAll);
							//Added by saurabh As if the item is selected reciept in previous month it should not appear in next month reported by BHavesh Shah[16/01/17|Start]
							
							if("R".equalsIgnoreCase(retRepFlagAll.trim()))
							{
							sql="select count(1) as count from cust_stock_inv where tran_id= ? and invoice_id=?  and dlv_flg = 'N' ";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1,tranIdLast);
							pstmt1.setString(2,sretunTranId);
							rs1 = pstmt1.executeQuery();
							while( rs1.next())
							{
								cnt= rs1.getInt("count");
								System.out.println("cnt0>>"+cnt);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							//Added by saurabh As if the item is selected reciept in previous month it should not appear in next month reported by BHavesh Shah[16/01/17|Start]
							}
							System.out.println("cnt>>"+cnt);
							//Formula
							totalVal=((sretQtyAll * sretRate)-((sretQtyAll * sretRate * sretDiscnt)/100));
							
							if(isSelected(selectedInvList,sretunTranId) && "P".equalsIgnoreCase(retRepFlagAll.trim()) )
							{
								 returnReciptRepl+= sretQtyAll;
								 //returnReciptReplVal+=(sretQtyAll * sretRate);
								 returnReciptReplVal+=getRequiredDcml(totalVal,3);
							}
							if ( (isSelected(selectedInvList,sretunTranId) || isSelected(transitInvList,sretunTranId) )&& "R".equalsIgnoreCase(retRepFlagAll.trim()) && cnt==0 )//Added by saurabh As if the item is selected reciept in previous month it should not appear in next month reported by BHavesh Shah[16/01/17]
							{
								billRetQty += sretQtyAll;
								//billRetQtyVal+=(sretQtyAll * sretRate);
								billRetQtyVal+=getRequiredDcml(totalVal,3);
								System.out.println("cnt1>>"+cnt);
							}
							//Added by saurabh As if the item is selected reciept in previous month it should not appear in next month reported by BHavesh Shah[16/01/17|Start]
							else if((isSelected(selectedInvList,sretunTranId) || isSelected(transitInvList,sretunTranId) )&& "R".equalsIgnoreCase(retRepFlagAll.trim()) && cnt>0)
							{
								billRetQty=0;billRetQtyVal=0;
								System.out.println("cnt2>>"+cnt);
							}
							if(isSelected(transitInvList,sretunTranId) && "P".equalsIgnoreCase(retRepFlagAll.trim()) )
							{
								 returnTransitRepl+= sretQtyAll;
								 //returnTransitReplVal+= (sretQtyAll * sretRate);
								 returnTransitReplVal+= getRequiredDcml(totalVal,3);
							} 
							if (isSelected(selectedInvList,sretunTranId) && "F".equalsIgnoreCase(lineTypeAll.trim()) && "R".equalsIgnoreCase(retRepFlagAll.trim()) )
							{
								billRetQtyBonus += sretQtyAll;
								//billRetQtyBonusVal += (sretQtyAll * sretRate);
								billRetQtyBonusVal += getRequiredDcml(totalVal,3);
							}	
							
						}
						
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					replQtyListMethod.add(returnReciptRepl);
					replQtyListMethod.add(billRetQty);
					replQtyListMethod.add(returnTransitRepl);
					replQtyListMethod.add(billRetQtyBonus);
					replQtyListMethod.add(sretRate);
					replQtyListMethod.add(returnReciptReplVal);
					replQtyListMethod.add(billRetQtyVal);
					replQtyListMethod.add(returnTransitReplVal);
					replQtyListMethod.add(billRetQtyBonusVal);
					System.out.println("GetReplacementList@@@@@>>>>>"+replQtyListMethod);
				
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				throw new ITMException( exception );
			}
			return replQtyListMethod ;
		}

		private ArrayList<Double> GetRcpQtyCM(String quantityFlag,String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String transitInvList,String itenmCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn,String calCriItemSerIn,boolean isItemSer) throws Exception
		{
			//NodeList parentNodeList=null;
			//NodeList childList = null;
			//Node parentNode=null;
			//Node childNode = null;
			//String invoiceId = null;
			//String dlvFlg = null ;
			double receiptQty=0,ratesStduom=0,disCnt=0;//,rcpQuantity = 0, quantity = 0,dlvFlgTransit=0;
			double QuantityValue=0.0,receiptQtySum=0.0;
			PreparedStatement pstmt = null;//,pstmt2 = null;
			ResultSet rs = null;//,rs2 = null;
			String sql = null;
			//String stockDlvFlg="";
			ArrayList<Double> rcpQtyListMethod=new ArrayList<Double>();
			try
			{		
				System.out.println("calculate GetRcpQtyCommon "+quantityFlag+" quantity!!!!!!!!!!itemcode>>>>>"+itenmCode);
				if("R".equalsIgnoreCase(quantityFlag))
				{
					sql =  " SELECT (itrace.QUANTITY__STDUOM) QUANTITY__STDUOM, itrace.RATE__STDUOM RATE__STDUOM ,itrace.DISCOUNT DISCOUNT FROM" +
							//" invoice invoice,invoice_trace itrace ,item item " +
							" invoice invoice,invoice_trace itrace " +
							" 	where invoice.invoice_id=itrace.invoice_id  " +
							//" and itrace.item_code=item.item_code " +
							" AND invoice.cust_code = ? "
							//" 	and invoice.site_code = ? " +
							//" and invoice.inv_type=? " +
							//" and ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) "
							+ " and invoice.tran_date between ? and ?  AND itrace.RATE__STDUOM>0 "
							+ " and itrace.item_code = ? and itrace.invoice_id in("+selectedInvList+") " ;
							//  "group by itrace.RATE__STDUOM ";
							/*if(isItemSer)
							{
								//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) ";
								//sql=sql+" and ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) ";
								sql=sql+" and ( item.item_ser in ("+itemSer+") OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) ";
							}else
							{
								//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+itemSer+"))";
								//sql=sql+" and ( itrace.item_ser__prom in ("+itemSer+"))";
								//Changed by saurabh [23/03/17|Start]
								//sql=sql+" and item.item_ser in("+itemSer+")  ";
								sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
								//Changed by saurabh [23/03/17|End]
							}*/
				}else if("T".equalsIgnoreCase(quantityFlag))
				{
					sql =  " SELECT (itrace.QUANTITY__STDUOM) QUANTITY__STDUOM, itrace.RATE__STDUOM RATE__STDUOM,itrace.DISCOUNT DISCOUNT FROM" +
							//" invoice invoice,invoice_trace itrace ,item item " +
							" invoice invoice,invoice_trace itrace " +
							" 	where invoice.invoice_id=itrace.invoice_id  " +
							//" and itrace.item_code=item.item_code " +
							" AND  invoice.cust_code = ? "
							//" 	and invoice.site_code = ? " +
							//" and invoice.inv_type=? " +
							//" and ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  "
							+ " and invoice.tran_date between ? and ? AND itrace.RATE__STDUOM>0 "
							+ " and itrace.item_code = ? and itrace.invoice_id in("+transitInvList+") " ;
							 // "group by itrace.RATE__STDUOM ";
							/*if(isItemSer)
							{
								//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
								//sql=sql+" and ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
								sql=sql+" and ( item.item_ser in ("+itemSer+") OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
								
							}else
							{
								//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+itemSer+"))";
								//sql=sql+" and ( itrace.item_ser__prom in ("+itemSer+"))";
								//Changed by saurabh [23/03/17|Start]
								//sql=sql+" and item.item_ser in("+itemSer+")  ";
								sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
								//Changed by saurabh [23/03/17|End]
							}*/
				}
				
				/*sql =  " SELECT (itrace.QUANTITY__STDUOM) QUANTITY__STDUOM, itrace.RATE__STDUOM RATE__STDUOM FROM" +
						" invoice invoice,invoice_trace itrace " +
						" 	where invoice.invoice_id=itrace.invoice_id  AND  invoice.cust_code = ? " +
						" 	and invoice.site_code = ? and invoice.inv_type=? and itrace.item_ser__prom=?  "
						+ " and invoice.tran_date between ? and ?  AND itrace.RATE__STDUOM>0 "
						+ " and itrace.item_code = ? and itrace.invoice_id in("+selectedInvList+","+transitInvList+") " ;*/
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, custCode );
					//pstmt.setString( 2, siteCode );
					//pstmt.setString( 2, orderType );
					//pstmt.setString( 3, itemSer);
					pstmt.setTimestamp( 2, fromDate );
					pstmt.setTimestamp( 3, toDate );
					pstmt.setString( 4, itenmCode );
					rs = pstmt.executeQuery();
					//double rcpCur =0, rcpPre = 0, totRcp = 0;
					while (rs.next())
					{			    
						receiptQty = rs.getDouble("QUANTITY__STDUOM");
						ratesStduom = rs.getDouble("RATE__STDUOM");
						disCnt = rs.getDouble("DISCOUNT");
						//QuantityValue=QuantityValue+(receiptQty*ratesStduom);
						QuantityValue=QuantityValue+getRequiredDcml(((receiptQty*ratesStduom)-((receiptQty*ratesStduom*disCnt)/100)),3);
						receiptQtySum=receiptQtySum+receiptQty;
					}	
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;		
				
					rcpQtyListMethod.add(receiptQtySum);
					rcpQtyListMethod.add(QuantityValue);
				
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				throw new ITMException( exception );
			}
			return rcpQtyListMethod ;
		}
		
		private ArrayList<Double> GetFreeQtyCommon(String quantityFlag,String custCode, String orderType, String itemSer,String siteCode,String selectedInvList,String transitInvList,String itemCode,java.sql.Timestamp fromDate, java.sql.Timestamp toDate,java.sql.Timestamp prdFrom, java.sql.Timestamp prdTo, Connection conn,String calCriItemSerIn,boolean isItemSer,String tranIdNew) throws Exception
		{
			/*NodeList parentNodeList=null;
			NodeList childList = null;
			Node parentNode=null;
			Node childNode = null;
			String invoiceId = null;*/
			//String dlvFlg = null ;
			//double transitQty = 0, quantity = 0,dlvFlgTransit=0;
			PreparedStatement pstmt = null;//,pstmt2 = null,pstmt3 = null;
			ResultSet rs = null;//,rs2 = null,rs3 = null;
			String sql = null;
			/*String stockDlvFlg="";
			String invoiceIdArray[];
			String invoiceSep="",nature="";*/
			double freesQty=0.0,totalFreesQtyValue=0.0;
			double freValue=0.0;
			ArrayList<Double> rcpFreeQtyListMethod=new ArrayList<Double>();
			boolean isExistFree=false;
			try
			{		
			
				System.out.println("----calculate Receipt Frees "+quantityFlag+" Quantity------------------itemcode>>>>>>>"+itemCode);
				
				if("R".equalsIgnoreCase(quantityFlag))
				{
					sql="select sum(totfreeQty) as freeqty,sum(totFreeValue) as totalvalue from" +
						" (select invoice_id,sum(quantity) totfreeQty,sum(rate),sum(quantity)*sum(rate) as totFreeValue " +
						"from ( select invoice.invoice_id as invoice_id,0 quantity, max(rate__stduom) " +
						//" rate from invoice invoice,invoice_trace itrace ,item item " +
						" rate from invoice invoice,invoice_trace itrace, " +
						" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  " ;
					if(tranIdNew.trim().length()==0){	
					sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y' " ;
					}
					sql=sql+" ) CINV "+
						" where invoice.invoice_id= Itrace.invoice_id  " +
						" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+)  ";
					if(tranIdNew.trim().length()>0)
					{
						sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
					}
					else
					{
						sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) And  CINV.INVOICE_ID IS NULL ";
					}
						//" and Itrace.item_code=item.item_code " +
						sql=sql+" and invoice.invoice_id in("+selectedInvList+") and Itrace.item_code='"+itemCode+"'  " ;
						/*if(isItemSer)
						{
							//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and  ( itrace.item_ser__prom in ("+itemSer+")  OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  " ;
							//sql=sql+" and  ( itrace.item_ser__prom in ("+itemSer+")  OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  " ;
							sql=sql+" and  ( item.item_ser in ("+itemSer+")  OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  " ;
						}else
						{
							//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//sql=sql+" and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//Changed by saurabh [23/03/17|Start]
							//sql=sql+" and item.item_ser in("+itemSer+")  ";
							sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
							//Changed by saurabh [23/03/17|End]
						}*/
						//" and  ( itrace.item_ser__prom in ("+itemSer+")  OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  " +
						//" and invoice.inv_type='"+orderType+"' " +
						sql=sql+" and rate__stduom>0.001 group by invoice.invoice_id " +
						"union all" +
						" select invoice.invoice_id as invoice_id,quantity__stduom quantity, 0 rate from " +
						//"invoice invoice,invoice_trace itrace,item item  " +
						"invoice invoice,invoice_trace itrace,  " +
						" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  " ;
						if(tranIdNew.trim().length()==0){
						sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y'" ;
						}
						sql=sql+" ) CINV "+
						"where invoice.invoice_id= Itrace.invoice_id " +
						" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) ";
						if(tranIdNew.trim().length()>0)
						{
							sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
						}
						else
						{
							sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) And  CINV.INVOICE_ID IS NULL ";
						}
						//"and Itrace.item_code=item.item_code " +
						sql=sql+" and invoice.invoice_id in("+selectedInvList+") and Itrace.item_code='"+itemCode+"' " ;
						/*if(isItemSer)
						{
							//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+itemSer+")   OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
							//sql=sql+" and ( itrace.item_ser__prom in ("+itemSer+")   OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
							sql=sql+" and ( item.item_ser in ("+itemSer+")   OR ((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  ";
						}else
						{
							//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//sql=sql+" and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//Changed by saurabh [23/03/17|Start]
							//sql=sql+" and item.item_ser in("+itemSer+")  ";
							sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
							//Changed by saurabh [23/03/17|End]
						}*/
						//" and ( itrace.item_ser__prom in ("+itemSer+")   OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) )  " +
						//" and invoice.inv_type='"+orderType+"'  " +
						//commented and added by saurabh for reciept bonus issue[10/10/16|Start]
						//sql=sql+" and rate__stduom<=0.001 ) group by invoice_id ) HAVING sum(totFreeValue) >0 AND sum(totfreeQty)>0 " ;
						sql=sql+" and rate__stduom<=0.001 ) group by invoice_id ) HAVING sum(totfreeQty)>0 " ;
						//commented and added by saurabh for reciept bonus issue[10/10/16|end]
				
				}else  if("T".equalsIgnoreCase(quantityFlag))
				{

					sql="select sum(totfreeQty) as freeqty,sum(totFreeValue) as totalvalue from" +
						" (select invoice_id,sum(quantity) totfreeQty,sum(rate),sum(quantity)*sum(rate) as totFreeValue " +
						"from ( select invoice.invoice_id as invoice_id,0 quantity, max(rate__stduom) " +
						//"rate from invoice invoice,invoice_trace itrace ,item item " +
						"rate from invoice invoice,invoice_trace itrace, " +
						" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  " ;
						if(tranIdNew.trim().length()==0){
						sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y'" ;
						}
						sql=sql+" ) CINV "+
						"where invoice.invoice_id= Itrace.invoice_id " +
						" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) ";
					if(tranIdNew.trim().length()>0)
					{
						sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
					}
					else
					{
						sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) And CINV.INVOICE_ID IS NULL ";
					}
						//"and Itrace.item_code=item.item_code " +
					sql=sql+"and invoice.invoice_id in("+transitInvList+") and Itrace.item_code='"+itemCode+"' " ;
						/*if(isItemSer)
						{
							//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and  ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
							//sql=sql+" and  ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
							sql=sql+" and  ( item.item_ser in ("+itemSer+") OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
						}else
						{
							//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//sql=sql+" and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//Changed by saurabh [23/03/17|Start]
							//sql=sql+" and item.item_ser in("+itemSer+")  ";
							sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
							//Changed by saurabh [23/03/17|End]
						}*/
						//" and  ( itrace.item_ser__prom in ("+itemSer+") OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " +
						//" and invoice.inv_type='"+orderType+"' " +
						sql=sql+ " and rate__stduom>0.001 group by invoice.invoice_id " +
						"union all" +
						" select invoice.invoice_id as invoice_id,quantity__stduom quantity, 0 rate from " +
						//" invoice invoice,invoice_trace itrace ,item item " +
						" invoice invoice,invoice_trace itrace, " +
						" (SELECT CINV.INVOICE_ID,CDET.ITEM_CODE,CDET.ITEM_SER,CINV.TRAN_ID FROM CUST_STOCK_INV CINV,CUST_STOCK_DET CDET,CUST_STOCK CS  WHERE CS.TRAN_ID=CINV.TRAN_ID AND CINV.TRAN_ID = CDET.TRAN_ID AND CS.POS_CODE IS NOT NULL AND CS.STATUS <> 'X'  " ;
						if(tranIdNew.trim().length()==0){
						sql=sql+" AND NVL(CINV.DLV_FLG,'N') = 'Y' " ;
						}
						sql=sql+" ) CINV "+
						" where invoice.invoice_id = Itrace.invoice_id " +
						" AND itrace.invoice_id = CINV.INVOICE_ID (+) AND itrace.ITEM_CODE = CINV.ITEM_CODE (+) ";
						if(tranIdNew.trim().length()>0)
						{
							sql=sql+" AND CINV.TRAN_ID='"+tranIdNew+"' And  CINV.INVOICE_ID IS NOT NULL ";
						}
						else
						{
							sql=sql+" AND itrace.ITEM_SER__PROM = CINV.ITEM_SER (+) And  CINV.INVOICE_ID IS NULL ";
						}
						//" and itrace.item_code=item.item_code " +//Added by saurabh for transit bonus quantity issue due missing join[06/02/17]
						sql=sql+" and invoice.invoice_id in("+transitInvList+") and Itrace.item_code='"+itemCode+"' " ;
						/*if(isItemSer)
						{
							//sql=sql+" and invoice.item_ser in ("+calCriItemSerIn+") and ( itrace.item_ser__prom in ("+itemSer+")  OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
							//sql=sql+" and ( itrace.item_ser__prom in ("+itemSer+")  OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
							sql=sql+" and ( item.item_ser in ("+itemSer+")  OR((item.item_ser,item.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " ;
						}else
						{
							//sql=sql+" and invoice.item_ser not in ("+calCriItemSerIn+") and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//sql=sql+" and  ( itrace.item_ser__prom in ("+itemSer+"))";
							//Changed by saurabh [23/03/17|Start]
							//sql=sql+" and item.item_ser in("+itemSer+")  ";
							sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
							//Changed by saurabh [23/03/17|End]
						}*/
						//" and ( itrace.item_ser__prom in ("+itemSer+")  OR((itrace.item_ser__prom,itrace.ITEM_CODE) IN (select item_ser,item_code from item_credit_perc WHERE item_ser  NOT IN("+itemSer+") )) ) " +
						//" and invoice.inv_type='"+orderType+"'  " + 
						//commented and added by saurabh for reciept bonus issue[10/10/16|Start]
						//sql=sql+" and rate__stduom<=0.001 ) group by invoice_id ) HAVING sum(totFreeValue) >0 AND sum(totfreeQty)>0 " ;
						sql=sql+" and rate__stduom<=0.001 ) group by invoice_id ) HAVING sum(totfreeQty)>0 " ;
						//commented and added by saurabh for reciept bonus issue[10/10/16|end]
				}
				
				pstmt = conn.prepareStatement( sql );
				//pstmt.setString(1, siteCode);
				//pstmt.setString(2, custCode);
				//pstmt.setString(3, orderType);
				//pstmt.setString(4, itemCode);
				//pstmt.setString(5, itemSer);
				//pstmt.setTimestamp(6, fromDate );
				//pstmt.setTimestamp(7, toDate );
				//pstmt.setTimestamp(8, prdFrom );
				//pstmt.setTimestamp(9, prdTo );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					freesQty = rs.getDouble("freeqty");
					totalFreesQtyValue = rs.getDouble("totalvalue");
					isExistFree=true;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("isExistFree>>>>>"+isExistFree);
				if("R".equalsIgnoreCase(quantityFlag) && isExistFree)
				{
					freValue=getPreviousInvoiceRateValue(isItemSer,custCode, orderType, itemSer,siteCode, selectedInvList, itemCode, fromDate,  toDate,prdFrom,  prdTo,  conn);
				}else  if("T".equalsIgnoreCase(quantityFlag) && isExistFree)
				{
					freValue=getPreviousInvoiceRateValue(isItemSer,custCode, orderType, itemSer,siteCode, transitInvList, itemCode, fromDate,  toDate,prdFrom,  prdTo,  conn);
				}
				System.out.println("totalFreesQtyValue>>>>>>"+totalFreesQtyValue);
				System.out.println("freValue>>>>>>"+freValue);
				totalFreesQtyValue=totalFreesQtyValue+freValue;
				
				System.out.println("totalFreesQtyValue>>@@@@@@@@@>>>>"+totalFreesQtyValue);
				rcpFreeQtyListMethod.add(freesQty);
				rcpFreeQtyListMethod.add(totalFreesQtyValue);
				
				
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				//System.out.println("Exception ::" + exception.getMessage());
				throw new ITMException( exception );
			}
			System.out.println(" totalFreesQtyValue List :"+totalFreesQtyValue);
			return rcpFreeQtyListMethod ;
		}
		
		
		public String getRequiredDecimal(double actVal, int prec)
		{
			String fmtStr = "############0";
			//String strValue = null;
			if (prec > 0)
			{
				fmtStr = fmtStr + "." + "000000000".substring(0, prec);
			}
			DecimalFormat decFormat = new DecimalFormat(fmtStr);
			return decFormat.format(actVal);
		}
		
		public String getItemSerList(String itemser, Connection conn) throws ITMException
		{
			String itemSerGrpValue="",itemSerSplit="",resultItemSer="";
			PreparedStatement pstmt = null;//,pstmt2 = null,pstmt3 = null;
			ResultSet rs = null;//,rs2 = null,rs3 = null;
			String sql = null;
			try
			{
			/* Changed by Manoj dtd 26/10/2016 to get all items of groupcode
			 sql= " select item_ser from itemser where grp_code=? and item_ser<>case when grp_code is null then '.' else grp_code end " +
					"union all " +
					"select item_ser from itemser where item_ser=? and item_ser<>case when grp_code is null then '.' else grp_code end";
					*/
				
				sql= " select distinct item_ser from" +
						"(select item_ser from itemser where grp_code=?  " +
						"union all " +
						"select item_ser from itemser where item_ser=?) ";
						
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemser);
			pstmt.setString(2, itemser);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				itemSerGrpValue=checkNull(rs.getString("item_ser")).trim();
				itemSerSplit=itemSerSplit+"'"+itemSerGrpValue+"',";
			}
			rs.close();
			rs = null;
			pstmt.close();
			resultItemSer = itemSerSplit.substring(0, itemSerSplit.length() - 1);
			//resultItemSer = itemSerSplit.substring(1, itemSerSplit.length() - 2);
			System.out.println("resultItemSer>>>>>"+resultItemSer);
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				try
                {
	                throw new ITMException( exception );
                } catch (ITMException e)
                {
	                e.printStackTrace();
                }
				throw new ITMException(exception); //Added By Mukesh Chauhan on 02/08/19
			}
			return resultItemSer;
		}
		//private ArrayList addPreviousItems(List<String> itemListLast,ArrayList defData,String tranIdLast, Connection conn)
		private ArrayList addPreviousItems(String custCodeHd ,String itemSerDom ,String resultitemSer ,boolean isItemSerLocal ,
				java.sql.Timestamp frmDateTstmp,java.sql.Timestamp prvMFromDate,java.sql.Timestamp prvMToDate,List<String> itemListLast,
				ArrayList defData1,String tranIdLast, Connection conn,boolean isItemSer) throws ITMException
	    {
			
			String sql = null;
			PreparedStatement pstmt = null,pstmt1 = null;
			ResultSet rs = null,rs1 = null;
			ItemDescr itemDescr = null;
			ArrayList previousItem = new ArrayList();
			ArrayList defData = new ArrayList();
			String itemCode="",clStock="",rate="",rateOrg="",previousItems="";
			String itemDescription="",itemSer="",itemUsage="";
			System.out.println("itemListLast==>"+itemListLast);
			itemListLast.clear(); // Added by santosh to set itemListLast as null 14/SEP/2017
			try
			{
				
				for(int itemPos = 0; itemPos < defData1.size(); itemPos++ )
				{
					
					previousItems = ( ( ItemDescr )defData1.get(itemPos)).lsCode; 
					System.out.println("class items"+previousItems);
					previousItem.add(previousItems);
				}
			//Added by saurabh for product transfer[22/03/17|Start]
			/*sql = "	select item_code,cl_stock,CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
					" CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org from " +
					"cust_stock_det where tran_id=? and cl_stock>0 ";//Added by saurabh[22/12/16]
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, tranIdLast);*/
			
			System.out.println("custCodeHd>>"+custCodeHd+">>itemSerDom>>"+itemSerDom+">>resultitemSer>>"+resultitemSer+">>>isItemSerLocal"+isItemSerLocal+">>frmDateTstmp>>"+frmDateTstmp);
			//TODO
			if(isItemSerLocal)
			{
				/*sql = "	select item_code,cl_stock,CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
					  " CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org,item_ser from " +
					  "cust_stock_det where tran_id='"+tranIdLast+"' and cl_stock>0 ";//Added by saurabh[22/12/16]
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranIdLast);*/
				sql="SELECT CDET.ITEM_CODE,CDET.CL_STOCK,CASE WHEN CDET.RATE IS NULL THEN 0 ELSE CDET.RATE END  AS RATE,  CASE WHEN CDET.RATE__ORG IS NULL THEN 0 ELSE CDET.RATE__ORG END AS RATE__ORG,CDET.ITEM_SER " +
						" FROM CUST_STOCK CSTK,CUST_STOCK_DET CDET WHERE CSTK.TRAN_ID=CDET.TRAN_ID "+ 
						" AND CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND CSTK.TO_DATE = ? AND CSTK.pos_code is not null AND CSTK.STATUS <> 'X'  and CDET.CL_STOCK > 0 "+
						" AND (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) in ("+resultitemSer+") ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, custCodeHd);
					pstmt1.setTimestamp(2, prvMFromDate);
					pstmt1.setTimestamp(3, prvMToDate);
			}
			else
			{
				/*sql = " SELECT B.ITEM_CODE,B.CL_STOCK,CASE WHEN B.RATE IS NULL THEN 0 ELSE B.RATE END  AS RATE,  CASE WHEN B.RATE__ORG IS NULL THEN 0 ELSE B.RATE__ORG END AS RATE__ORG,B.ITEM_SER "+ 
					  " FROM (SELECT MAX(TRAN_ID) AS TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = ? and FROM_DATE <= ? and CONFIRMED='Y' and STATUS='S' AND POS_CODE IS NOT NULL) A "+
					  " INNER JOIN CUST_STOCK C ON A.TRAN_ID = C.TRAN_ID "+
					  " INNER JOIN CUST_STOCK_DET B ON A.TRAN_ID = B.TRAN_ID and B.CL_STOCK>0 "+
					  " WHERE '"+itemSerDom.trim()+"'=(SELECT TRIM(FN_GET_ITEMSER_CHANGE(B.ITEM_CODE,TO_DATE(?,?))) FROM DUAL) ";*/
				sql="SELECT CDET.ITEM_CODE,CDET.CL_STOCK,CASE WHEN CDET.RATE IS NULL THEN 0 ELSE CDET.RATE END  AS RATE,  CASE WHEN CDET.RATE__ORG IS NULL THEN 0 ELSE CDET.RATE__ORG END AS RATE__ORG,CDET.ITEM_SER " +
					" FROM CUST_STOCK CSTK,CUST_STOCK_DET CDET WHERE CSTK.TRAN_ID=CDET.TRAN_ID "+ 
					" AND CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND CSTK.TO_DATE = ? AND CSTK.pos_code is not null AND CSTK.STATUS <> 'X' and CDET.CL_STOCK > 0 "+
					" AND '"+itemSerDom.trim()+"' = (select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL) " ;
				//Commented by santosh to show item code for CH division  
					//" and CSTK.ITEM_SER <> 'CH' ";//Added bay santosh to remove duplicate data 14/SEP/2017
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCodeHd);
				pstmt1.setTimestamp(2, prvMFromDate);
				pstmt1.setTimestamp(3, prvMToDate);
			}
			//Added by saurabh for product transfer[22/03/17|eND]
			rs1 = pstmt1.executeQuery();
			while(rs1.next()) 
			{	
				itemCode = checkNull(rs1.getString("item_code"));
				clStock = rs1.getString("cl_stock");
				rate = rs1.getString("rate");
				rateOrg = checkNull(rs1.getString("rate__org")).trim();
//				itemSer=checkNull(rs1.getString("item_ser")); commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
				System.out.println("Before IF");
				//if(!itemListLast.contains(itemCode) && !previousItem.contains(itemCode))
					if(!itemListLast.contains(itemCode))
				{
					System.out.println("IN IF");
//					commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
//					sql= " select descr,item_ser,item_usage from item where item_code = ?";
					sql= "select descr,item_usage,fn_get_itemser_change(item.item_code,?) as item_ser from item where item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, prvMFromDate);
					pstmt.setString(2, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemDescription=checkNull(rs.getString("descr"));
						itemSer=checkNull(rs.getString("item_ser"));
						itemUsage=checkNull(rs.getString("item_usage"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("@S@itemSer in addPreviousItems["+itemSer+"]");
					if(rateOrg == null || rateOrg.trim().length()==0 || "0".equalsIgnoreCase(rateOrg))
					{
						rateOrg=rate;
					}
					if("F".equalsIgnoreCase(itemUsage)){
					itemDescr = new ItemDescr();
					itemDescr.setItemCode(itemCode);
					itemDescr.setLsOpStock(clStock);
					itemDescr.setLsrateold(rate);
					itemDescr.setLsrateorgold(rateOrg);
					itemDescr.setItemDescription(itemDescription);
					itemDescr.setLsItemSer(itemSer);
					
					itemDescr.lsItemSer = itemSer;
					itemDescr.lsCode = itemCode;
					itemDescr.lsOpStock= clStock;
					itemDescr.lsrateold= rate;
					itemDescr.lsrateorgold= rateOrg;
					defData.add(itemDescr);
					itemListLast.add(itemCode);//Modified by santosh to set itemListLast as null 14/SEP/2017
					}
				}
					
				
					
					/*else{
					//defData.remove(itemCode);
					while(true) {
						if(defData.contains(itemCode))
							defData.remove(itemCode);
						else
							break;
					}
					System.out.println("itemCode:"+defData);
					System.out.println("itemCode:"+itemCode);
					System.out.println("IN ELSE");
//					commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
//					sql= " select descr,item_ser,item_usage from item where item_code = ?";
					sql= "select descr,item_usage,fn_get_itemser_change(item.item_code,?) as item_ser from item where item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, prvMFromDate);
					pstmt.setString(2, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemDescription=checkNull(rs.getString("descr"));
						itemSer=checkNull(rs.getString("item_ser"));
						itemUsage=checkNull(rs.getString("item_usage"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("@S@itemSer in addPreviousItems["+itemSer+"]");
					if(rateOrg == null || rateOrg.trim().length()==0 || "0".equalsIgnoreCase(rateOrg))
					{
						rateOrg=rate;
					}
					if("F".equalsIgnoreCase(itemUsage)){
					itemDescr = new ItemDescr();
					itemDescr.setItemCode(itemCode);
					itemDescr.setLsOpStock(clStock);
					itemDescr.setLsrateold(rate);
					itemDescr.setLsrateorgold(rateOrg);
					itemDescr.setItemDescription(itemDescription);
					itemDescr.setLsItemSer(itemSer);
					
					itemDescr.lsItemSer = itemSer;
					itemDescr.lsCode = itemCode;
					itemDescr.lsOpStock= clStock;
					itemDescr.lsrateold= rate;
					itemDescr.lsrateorgold= rateOrg;
					defData.add(itemDescr);
					System.out.println("defData==>"+defData);
					itemListLast.add(itemCode);//Modified by santosh to set itemListLast as null 14/SEP/2017
					}
				}*/
			}
			
			for(int itemPos = 0; itemPos < defData1.size(); itemPos++ )
			{
				
				previousItems = ( ( ItemDescr )defData1.get(itemPos)).lsCode; 
				System.out.println("class items"+previousItems);
				
				if(!itemListLast.contains(previousItems)){
					
					
					
					System.out.println("IN IF");
//					commented by santosh to set item series on 18/SEP/2017[D17FSUN006]
//					sql= " select descr,item_ser,item_usage from item where item_code = ?";
					sql= "select descr,item_usage,fn_get_itemser_change(item.item_code,?) as item_ser from item where item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, prvMFromDate);
					pstmt.setString(2, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemDescription=checkNull(rs.getString("descr"));
						itemSer=checkNull(rs.getString("item_ser"));
						itemUsage=checkNull(rs.getString("item_usage"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					itemDescr = getItemBean(previousItems,frmDateTstmp,custCodeHd,tranIdLast,resultitemSer,isItemSer,conn);
					defData.add(itemDescr);
					itemListLast.add(itemCode);
					
				}
				
			}
			
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			itemListLast.clear(); // Added by santosh to set itemListLast as null 14/SEP/2017
			}catch(Exception exception)
			{
				exception.printStackTrace();
				try
                {
	                throw new ITMException( exception );
                } catch (ITMException e)
                {
	                e.printStackTrace();
                }
				throw new ITMException(exception); //Added By Mukesh Chauhan on 02/08/19
			}
		    return defData;
	    }
		
		/*public boolean isExistItem(String itemCode,String tranIdLast,java.sql.Timestamp prvMFromDate,java.sql.Timestamp prvMToDate,String itemSerHd,String custCodeDom,boolean isItemSerLocal,Connection conn)
		{
			boolean isItemFound=true;
			String sql = "";
			int itemCnt=0;
			PreparedStatement pstmt = null,pstmt1 = null;
			ResultSet rs = null,rs1 = null;
			ItemDescr itemDescr = null;
			ArrayList previousItem = new ArrayList();
			try
			{
				if(isItemSerLocal)
				{
					sql = "	select count(1) from cust_stock_det where tran_id=?  and item_code= ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranIdLast);
					pstmt1.setString(2, itemCode);
				}
				else
				{
					sql="SELECT COUNT(1) " +
							" FROM CUST_STOCK CSTK,CUST_STOCK_DET CDET WHERE CSTK.TRAN_ID=CDET.TRAN_ID "+ 
							" AND CSTK.CUST_CODE = ? AND CSTK.FROM_DATE = ? AND CSTK.TO_DATE = ? AND   CSTK.pos_code is not null "+
							" and CSTK.TRAN_ID=(select max(CSTK1.TRAN_ID) from CUST_STOCK CSTK1,CUST_STOCK_DET CDET1 where CDET1.TRAN_ID = CDET.TRAN_ID "+
							" and CSTK1.TRAN_ID = CSTK.TRAN_ID and CDET1.item_code=CDET.item_code AND CSTK1.CUST_CODE= '"+custCodeDom+"' and CSTK1.CONFIRMED='Y' and CSTK1.STATUS='S' "+ 
							" and CSTK1.POS_CODE IS NOT NULL and CSTK1.item_ser=(select trim(FN_GET_ITEMSER_CHANGE(CDET1.ITEM_CODE,CSTK.FROM_DATE)) from DUAL))"+ 
							" AND "+itemSerHd+" =(select trim(FN_GET_ITEMSER_CHANGE(CDET.ITEM_CODE,SYSDATE)) from DUAL)" +
							" AND CDET.item_code=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, custCodeDom);
					pstmt1.setTimestamp(2, prvMFromDate);
					pstmt1.setTimestamp(3, prvMToDate);
					pstmt1.setString(4, itemCode);
				}
				rs1 = pstmt1.executeQuery();
				if(rs1.next()) 
				{	
					itemCnt = rs1.getInt(1);
					System.out.println("inside if@@@@"+itemCnt);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(itemCnt==0)
				{
					isItemFound=false;
				}else
				{
					isItemFound=true;
				}
				
			}catch(Exception exception)
			{
				exception.printStackTrace();
				try
                {
	                throw new ITMException( exception );
                } catch (ITMException e)
                {
	                e.printStackTrace();
                }
			}
			System.out.println("isItemFound>>>>>"+isItemFound);
			return isItemFound;
		}*/
		public double getRequiredDcml(double actVal, int prec)
		{
			double value=0.0;
			String fmtStr = "############0";
			//String strValue = null;
			if (prec > 0)
			{
				fmtStr = fmtStr + "." + "000000000".substring(0, prec);
			}
			DecimalFormat decFormat = new DecimalFormat(fmtStr);
			if(decFormat.format(actVal) != null && decFormat.format(actVal).trim().length() > 0 )
			{
				value=Double.parseDouble(decFormat.format(actVal));
			}else
			{
				value=0.00;
			}
			return value;
		}
		private String isExist(String table, String field, String value,Connection conn) throws SQLException
		{
			String sql = "",retStr="";
			PreparedStatement pstmt = null;
			ResultSet rs = null ;
			int cnt=0;

			sql = " SELECT COUNT(1) FROM "+ table + " WHERE " + field + " = ? ";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close(); 
			pstmt = null;
			if( cnt > 0)
			{
				retStr = "TRUE";
			}
			if( cnt == 0 )
			{
				retStr = "FALSE";
			}
			System.out.println("@@@@ isexist["+value+"]:::["+retStr+"]:::["+cnt+"]");
			return retStr;
		}
		//Commented by saurabh 040117
		private String isFrequent( String itemCode,Connection conn) throws SQLException
		{
			String sql = "",retStr="";
			PreparedStatement pstmt = null;
			ResultSet rs = null ;
			int cnt=0;
			sql = " SELECT COUNT(1) FROM item WHERE item_code = ? and item_usage='F' ";//Added by saurabh[22/12/16]
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close(); 
			pstmt = null;
			if( cnt > 0)
			{
				retStr = "TRUE";
			}
			if( cnt == 0 )
			{
				retStr = "FALSE";
			}
			System.out.println("@@@@ isexist["+itemCode+"]:::["+retStr+"]:::["+cnt+"]");
			return retStr;
		}
		//Commented by saurabh 040117
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
		private double getPreviousInvoiceRateValue(boolean isItemSer,String custCode, String orderType, String itemSer, String siteCode,String selectedInvList, String itemCode, Timestamp fromDate, Timestamp toDate, Timestamp prdFrom, Timestamp prdTo, Connection conn) throws Exception
        {
			PreparedStatement pstmt = null,pstmt1 = null;//,pstmt3 = null;
			ResultSet rs = null,rs1 = null;//,rs3 = null;
			String sql="",sql1="";
			//String invoiceId="";
			double previousRate=0.0;
			double freesQty=0.0;
			double finalValue=0.0;
			Timestamp invoiceTranDate=null;
			//ArrayList<String> rcpFreeQtyInvoice=new ArrayList<String>();
			try
			{
			
			sql= "select invoice.invoice_id as   invoice_id,invoice.tran_date as tran_date " +
					",itrace.quantity__stduom as quantity__stduom  from " +
					//"invoice invoice,invoice_trace itrace,item item " +
					"invoice invoice,invoice_trace itrace " +
					"where invoice.invoice_id= Itrace.invoice_id " +
					//"and Itrace.item_code=item.item_code " +
					//"invoice.invoice_id in("+selectedInvList+") and item_code='"+itemCode+"' and itrace.item_ser__prom in ("+itemSer+")" +
					"and invoice.invoice_id in("+selectedInvList+") and itrace.item_code='"+itemCode+"' " ;
					//Changed by saurabh [23/03/17|Start]
					/*if(isItemSer){
						sql=sql+" and item.item_ser in ("+itemSer+")" ;
					}
					else{
						sql=sql+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
					}*/
					//Changed by saurabh [23/03/17|End]
					sql=sql+" and invoice.inv_type='"+orderType+"'  and rate__stduom = 0.001 ";
			pstmt = conn.prepareStatement( sql );	
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				//invoiceId = rs.getString("invoice_id");
				invoiceTranDate = rs.getTimestamp("tran_date");
				freesQty = rs.getDouble("quantity__stduom");
				
				previousRate=0;
				sql1="select rate__stduom from invoice invoice,invoice_trace itrace " +
						" where invoice.invoice_id= Itrace.invoice_id " +
						//"and Itrace.item_code=item.item_code " +
						"and itrace.item_code= ?" +
						"  and invoice.cust_code= ?  " ;
						//" and itrace.item_ser__prom in ("+itemSer+") " +
						//Changed by saurabh [23/03/17|Start]
						/*if(isItemSer){
							sql1=sql1+" and item.item_ser in ("+itemSer+")" ;
						}else{
							sql1=sql1+" and (("+itemSer+") = (select trim(fn_get_itemser_change(itrace.item_code,invoice.tran_date)) from dual)) ";
						}*/
						//Changed by saurabh [23/03/17|End]
				sql1=sql1+" and invoice.inv_type=?  and invoice.tran_date < ? " +
						" and Itrace.rate__stduom>0.001 order by invoice.tran_date desc " ;		
				
				pstmt1 = conn.prepareStatement( sql1 );
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, custCode);
				pstmt1.setString(3, orderType);
				pstmt1.setTimestamp(4, invoiceTranDate );
				rs1 = pstmt1.executeQuery();
				if( rs1.next() )
				{
					previousRate = rs1.getDouble("rate__stduom");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("previousRate>>>>>"+previousRate);
				System.out.println("freesQty>>>>>"+freesQty);
				finalValue=finalValue+(previousRate*freesQty);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				//System.out.println("Exception ::" + exception.getMessage());
				throw new ITMException( exception );
			}
			
	        return finalValue;
        }
		/*private double getOpeningRate(String orderType,String itemSerHeaderSplit,String selectedInvList ,double rcpQtyDom,String itemCode, Timestamp prdtoDateTmstmp,Timestamp prdFromoDateTmstmp, String custCode, Connection conn) throws ITMException
	    {
		    String invoiceMonths="",sql="";
		    String sysDatetemp="",priceList="";
		    int invoiceMonthsPrevious=0;
		    Timestamp thirdMonthDay=null;
		    double openingRate=0.0,rateStd=0.0;
		    PreparedStatement pstmt = null,pstmt1 = null;
			ResultSet rs = null,rs1 = null;
			String sysDate="";
		    try
			{
		    	Date currentDate = new Date();
		    	SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
				sysDatetemp = sdf2.format(currentDate.getTime());
				
				if(rcpQtyDom > 0)
				{
					sql =  " SELECT itrace.RATE__STDUOM as RATE__STDUOM FROM" +
							" invoice invoice,invoice_trace itrace ,item item " +
							" 	where invoice.invoice_id=itrace.invoice_id  and itrace.item_code=item.item_code AND invoice.cust_code = ? " +
							//" and invoice.inv_type=? " +
							//" and itrace.item_ser__prom in("+itemSerHeaderSplit+") "+
							" and item.item_ser in ("+itemSerHeaderSplit+") "+
							" AND itrace.RATE__STDUOM>0.001 "+ 
							" and itrace.item_code = ? " +
							"and itrace.invoice_id in("+selectedInvList+") ORDER BY invoice.tran_date DESC " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode  );
					//pstmt.setString(2, orderType );
					pstmt.setString(2, itemCode );
					rs = pstmt.executeQuery( );
					if( rs.next() )
					{
						openingRate = rs.getDouble("RATE__STDUOM" );
						System.out.println("openingRate>>>>>> :"+openingRate);
						 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				else
				{
				invoiceMonths = dist.getDisparams("999999", "INVOICE_MONTHS", conn);
				System.out.println("invoiceMonths>>>>.." + invoiceMonths);
				if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0))
				{
					invoiceMonthsPrevious = -3;
				} else
				{
					invoiceMonthsPrevious = Integer.parseInt(invoiceMonths);
				}
				System.out.println("invoiceMonthsPrevious>>>>>" + invoiceMonthsPrevious);
				thirdMonthDay = utlmethd.AddMonths(prdFromoDateTmstmp, invoiceMonthsPrevious);
				System.out.println("thirdMonthDay from method>>>>>>" + thirdMonthDay);

				sql = "SELECT inv.invoice_id,itrc.rate__stduom as rate__stduom,inv.tran_date " + 
				"FROM invoice_trace itrc,invoice inv WHERE itrc.item_code=?  " + 
						"and itrc.invoice_id=inv.invoice_id and inv.tran_date>=? " +
				"and inv.tran_date<=? AND itrc.rate__stduom >0.001  and inv.cust_code=?  " + 
						" ORDER BY inv.tran_date DESC";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setTimestamp(2, thirdMonthDay);
				pstmt.setTimestamp(3, prdtoDateTmstmp);
				pstmt.setString(4, custCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					openingRate = rs.getDouble("rate__stduom");
					System.out.println("openingRate>>>>>> :" + openingRate);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (openingRate == 0)
				{
					sql = "select price_list from customer where cust_code =? ";
					pstmt1 = conn.prepareStatement(sql);
//					pstmt1.setString(1, custCodeStatic);
					pstmt1.setString(1, custCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						priceList = checkNull(rs1.getString("price_list"));
						System.out.println("priceList edit :" + priceList);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					//Commented by Manoj dtd 26/10/2016
					//openingRate = discmn.pickRate(priceList, sysDatetemp, itemCode, conn);
					//Changed by Manoj dtd 26/10/2016
					sysDate = genericUtility.getValidDateString( sysDatetemp , getApplDateFormat() , getDBDateFormat());
					sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, TO_DATE( ? , ? ), ? ) FROM DUAL ";
					pstmt1 = conn.prepareStatement( sql );
					pstmt1.setString( 1, priceList );
					pstmt1.setString( 2, sysDate );
					pstmt1.setString( 3, getDBDateFormat() );
					pstmt1.setString( 4, itemCode );
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						openingRate = rs1.getDouble(1);
						System.out.println("openingRate-----------> [" +openingRate+ "]");				
					}
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
				}
			}
		    	
			}catch(Exception exception)
			{
				exception.printStackTrace();
				//System.out.println("Exception ::" + exception.getMessage());
				throw new ITMException( exception );
			}
			System.out.println("return openingRate>>>>"+openingRate);
		    return openingRate;
	    }*/
}