
/********************************************************
	Title : SOrderFormEJB
	Date  : 28/08/08
	Author: pankaj singh

********************************************************/

package ibase.webitm.ejb.dis;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.SysCommon;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.sql.Timestamp;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.text.DecimalFormat;
import javax.ejb.Stateless; // added for ejb3


//public class SOrderFormEJB extends Validator implements SessionBean
@Stateless // added for ejb3
public class SOrderForm extends ValidatorEJB implements SOrderFormLocal, SOrderFormRemote
{
	FinCommon finCommon = new FinCommon();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	DecimalFormat df = new DecimalFormat( "##.00" );
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		//System.out.println("<======= SOrderForm DISPLAY IS IN PROCESS ! \n Welcome!========>");
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate()
	{
	}
	public void ejbPassivate()
	{
	}*/
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			//System.out.println("xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : SOrderFormEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
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
			//System.out.println("xmlString:-" + xmlString );
			//long startTime = System.currentTimeMillis();
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			//long endTime = System.currentTimeMillis();
			//long totalTime = endTime - startTime;
			//System.out.println(xmlString2);
			//System.out.println("start Time Spend :: "+startTime+" Milliseconds");
			//System.out.println("End Time Spend :: "+endTime+" Milliseconds");
			//System.out.println("Total Time Spend :: "+totalTime+" Milliseconds");
			
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : SOrderFormEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		String errString = "";
		String errCode = "",errcode = "";
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null ;
		ResultSet rs = null , rs1 = null,rs2=null;
		String sql = "" ,sql1 = "";
		String userId = "";
		String effFromStr = null;
		String validUptoStr = null;
		int cnt = 0,cnt1 = 0;
		int currentFormNo=0;
		int childNodeListLength;
        ConnDriver connDriver = new ConnDriver();
		String custCode = null;
		String orderDate = null;
		String tranDate = null;
		String tranId = null;
		String orderDateStr = null;
		Timestamp OrderDate = null;
		String blackListed = null;
		String stopBusiness = null;
		String sch[] = new String[12];
		Timestamp scheduleDate[] = new Timestamp[12];
		//Timestamp schedule1 = null;
		//Timestamp schedule2 = null;
		Date schedule1 = null;
		Date schedule2 = null;
		String var_value = null;
		int varValue = 0;
		String custPord = null;
		String pord_date = null;
		Timestamp pordDate = null;
		String channelPartner = null,disLink = null;
		String status = null;
		SysCommon sysCommon = null;
		String siteCodeShip = null;
		String schRatioStr[] = new String[12];
		double schRatio[] = new double[12]; 
		double totalRatio = 0.0;
		String itemCode = null;
		String itemSer = null;
		String itemSerHdr = null;
		String othSeries = null;
		String itemSer1 = null;
		String itemSerCrpolicyHdr = null;
		String itemSerCrpolicy = null;
		String orderTypeHdr = null;
		String stateCode = null;
		String schemeCode = null;
		String itemSerItem = null;
		String siteCode = null;
		String qtyStr[] = new String[12];
		String scheduleDateStr[] = new String[12];
		double qty[] = new double[12];
		double batchQty = 0.0;
		double integralQty = 0.0;
		Timestamp restrictUpto = null;
		String totQty = null;
		String salesPers = null;
		String salesPers1 = null;
		double TotQty = 0.0; 
		String ordValue = null;
		double OrdValue = 0.0; 
		String maxOrdValue = null,totValue = null;
		double MaxOrdValue = 0.0,TotValue = 0.0;
		double lcOrdValueO = 0.0;		
		double total = 0.0;	
		String orderType = null;
		String custItemRef	 = null;
		String strSch	 = null;
		String disparmVal = null;
		DistCommon distCommon = null;
		distCommon = new DistCommon();	
		String SalesPers1 = null;
		String totqtyStr=null;
		
		String stateCd = null, browItemCode = null, itemCodeParentCur = null;
		String  itemCodeParent = null,  colname = null;
		double qtyper = 0.0, batqty = 0.0, appMinQty = 0.0, appMaxQty = 0.0, minqty = 0.0, freeQty = 0.0, totChargeQty = 0.0;
		double totFreeQty = 0.0;
		double chargrQty = 0.0, prvChargeQty = 0.0, unconfirmChargeQty = 0.0;
		double mqty = 0.0, unconfirmFreeQty = 0.0, prvFreeQty = 0.0;
		double qtyVal = 0.0;
		int curLineno;			  
		Timestamp appfromDate = null, validuptoDate = null, tranDateTs = null;
		
		String curscheme = null, sqlStr = null;
		String applyCustList = null;
		String noapplyCustList = null, applicableordtypes = null, tranDateStr = null;
		String[] token; 
		String prevscheme = null, applyCust = null;
		String custSchemeCode = null, noapplyCust = null;
		int llRow = 0, llSchcnt = 0, llCnt = 0;
		String countCode = null;
		boolean  proceed;
		try
		{
			//System.out.println(".....call validate method ....");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//System.out.println("userId:- "  + userId);
			//GenericUtility genericUtility = GenericUtility.getInstance();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			//System.out.println("currentFormNo...." + currentFormNo);
			switch(currentFormNo)
			{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						//System.out.println("Current child:- " + childNodeName);
						//System.out.println("childNodeName.startsWith sch_ratio_" + childNodeName.startsWith("sch_ratio_") );
						if (childNodeName.equalsIgnoreCase("order_date"))
						{
							//orderDate == null ? "" : orderDate;
							siteCode = genericUtility.getColumnValue("site_code",dom);
							orderDate = genericUtility.getColumnValue("order_date",dom1);
							//System.out.println("orderDate ......"+orderDate );
							orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
							sysCommon = new SysCommon();
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = sysCommon.nfCheckPeriod("SAL",OrderDate,siteCode,conn);
							errCode=finCommon.nfCheckPeriod("SAL",OrderDate,siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							//System.out.println("errCode returmed:-" + errCode);
							//System.out.println("order_date for case 1 is:-" + OrderDate);
							if( errCode != null && errCode.trim().length() > 0 )
							{
								errString = getErrorString("order_date",errCode,userId); 
								break;
							}
						}
						else  if (childNodeName.equalsIgnoreCase("sch_ratio_1"))
						{
							sql = "select var_value from disparm where var_name='MAX_NO_SCHEDULE'"
									+ "and prd_code='999999'";
							//System.out.println("select qry from var_value.." + sql);
							pstmt= conn.prepareStatement(sql);
							//System.out.println("Inside sch_ratio_");
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   var_value = rs.getString(1) == null ? "" : rs.getString(1);
							}
							//System.out.println("var_value.." + var_value);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
								 
							varValue = Integer.parseInt(var_value);
							for(int i = 0 ;i < varValue; i++)
							{	strSch ="sch_ratio_"+(i+1) ; 
								schRatioStr[i] = genericUtility.getColumnValue(strSch,dom);
								// 17/06/10 manoharan take care off null
								if (schRatioStr[i] == null)
								{
									schRatioStr[i] = "0";
								}
								//System.out.println("schRatioStr[i]:-" + schRatioStr[i]);
								schRatio[i] = Double.parseDouble( schRatioStr[i] );
								total = total + schRatio[i];
							}
							//System.out.println("total:-" + total);
							if( total != 100 )
							{
								errCode = "VTRATIO1";
								errString = getErrorString("sch_ratio_1",errCode,userId);
								break;
							}
						}//end sch_ratio
						else if (childNodeName.equalsIgnoreCase("sch_1"))
						{
							boolean flag = false; 
							boolean existsFlag = false; 
							Date OrderDateD = null;
							Date ScheduleDateD[] = new Date[20];
							SimpleDateFormat sd = null;
							orderDate = genericUtility.getColumnValue("order_date",dom);							
							//orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							sd = new SimpleDateFormat(genericUtility.getApplDateFormat());
							//System.out.println("sd:- " + sd);
							OrderDateD = sd.parse(orderDate);
							//System.out.println("orderDate in date:- " + OrderDateD);
							//OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
							//System.out.println("orderDate for sch " + orderDate);
							for( int i = 0;i<=11;i++ )
							{
								strSch = "sch_"+(i + 1);
								//System.out.println("strSch value...." + strSch);
								sch[i] = genericUtility.getColumnValue(strSch,dom);
								//System.out.println("sch["+i+"]...."+sch[i]);	
								//System.out.println("sd for schedule:- " + sd);
								if(sch[i] != null)
								{
									ScheduleDateD[i] = sd.parse(sch[i]);
									//System.out.println("ScheduleDateD[i]:- " + ScheduleDateD[i]);
									if( ScheduleDateD[i].before( OrderDateD ) )
									{
										flag = true;
									}
								}
							}							
							sql = "select var_value from disparm where var_name='MAX_NO_SCHEDULE'"
									+ "and prd_code='999999'";
							//System.out.println("select qry from var_value.." + sql);
							pstmt= conn.prepareStatement(sql);
							rs = pstmt.executeQuery(); 
							stopBusiness = "";
							if(rs.next())
							{
							   var_value = rs.getString(1) == null ? "12" : rs.getString(1);
							}
							else
							{
								var_value = "12";
							}
							
							//System.out.println("var_value.." + var_value);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							varValue = Integer.parseInt(var_value);
							for(int i = 0 ;i < varValue; i++)
							{
								for(int j = 0 ;j < varValue; j++)
								{
									schedule1 = ScheduleDateD[i];
									schedule2 = ScheduleDateD[j];
									if( schedule1 != null && schedule2 != null &&   i!=j  && schedule1.compareTo( schedule2 ) == 0 )
									{
										existsFlag = true;										
									}
								}
							}
							if ( flag  )
							{
								errCode = "VTSCH1";
								errString = getErrorString("sch_1",errCode,userId);
								break;
							}
							else if ( existsFlag  )
							{
								errCode = "VTSCH2";
								errString = getErrorString("sch_1",errCode,userId);
								break;
							}
						}// end sch_date
					 	else  if (childNodeName.equalsIgnoreCase("cust_code"))
						{
							custCode = genericUtility.getColumnValue("cust_code",dom);
							custCode = custCode== null ? "" : custCode;
							orderDate = genericUtility.getColumnValue("order_date",dom);
							itemSer = genericUtility.getColumnValue("item_ser",dom);
							itemSer = itemSer== null ? "" : itemSer;
							//itemSerHdr = genericUtility.getcolumnvalue("item_ser",dom);
							//System.out.println("custCode for case 1 is:-" + custCode);
							siteCode = genericUtility.getColumnValue("site_code",dom);
							//System.out.println("siteCode [" +siteCode+ "]");
								errCode = isCustomer(siteCode,custCode,"S-FORM",conn);
								//System.out.println("errCode [" +errCode+ "]");
								if (errCode.length() !=0 )
								{								
									//errString = getErrorString("cust_code",errCode,userId); 
									//checkNextCol = false;									
									errString = getErrorString("cust_code",errCode,userId); 
									break;
								}

								
							sql = "select black_listed "
										+ " from customer_series where cust_code = ?"
										+ " and item_ser = ?";
							//System.out.println("select qry from black_listed.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(2,itemSer);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
								
							   blackListed = rs.getString("black_listed") == null ? " " : rs.getString("black_listed");
							}
							//System.out.println("blackListed.." + blackListed);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if ( blackListed!=null && blackListed.equalsIgnoreCase("Y")  )
							{
								errCode = "VTCUSTCD3";
								errString = getErrorString("cust_code",errCode,userId);
								break;
							}
							else
							{
								sql = "select stop_business "
										+ " from customer where cust_code = ?";
								//System.out.println("select qry from stop_business.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{	
								   stopBusiness = rs.getString("stop_business") == null ? "" : rs.getString("stop_business");
								}
								//System.out.println("stopBusiness.." + stopBusiness);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if ( stopBusiness != null  && stopBusiness.equalsIgnoreCase("Y")  )
								{
									errCode = "VTICC";
									errString = getErrorString("cust_code",errCode,userId);
									break;
								}
							}
						} 
						else  if (childNodeName.equalsIgnoreCase("site_code"))
						{
							siteCode = genericUtility.getColumnValue("site_code",dom);
							sql = "Select Count(*)  from site where site_code = ? ";
							//System.out.println("select qry from site_code.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   cnt = rs.getInt(1);
							}							
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if (cnt == 0)
							{
								errCode = "VTSITE1";
								errString = getErrorString("site_code",errCode,userId);
								break;
							}
						} 						
						 //end sch_
						else if (childNodeName.equalsIgnoreCase("cust_pord"))
						{
							custPord = genericUtility.getColumnValue("cust_pord",dom);
							pord_date = genericUtility.getColumnValue("pord_date",dom);
							//System.out.println("pord_date>>>>>>>>>>>>>>>> " + pord_date);
							tranId = genericUtility.getColumnValue("tran_id",dom);
							if (tranId == null)
							{
								tranId = "";
							}
							pordDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(pord_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.00");
							//System.out.println("pordDate:- " + pordDate);
							sql = "select count(*) from sordform" 
									+ " where cust_pord = ?"
									+ " and pord_date = ?"
									+ " and tran_id <> ?";
							//System.out.println("select count(*) var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custPord);
							pstmt.setTimestamp(2,pordDate);
							pstmt.setString(3,tranId);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   cnt = rs.getInt(1);
							}
							//System.out.println("cnt.." + cnt);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if(cnt > 0 )
							{
								errCode = "VTCUSTPODT";
								errString = getErrorString("cust_pord",errCode,userId);
								break;
							}
							custCode = genericUtility.getColumnValue("cust_code",dom);
							siteCode = genericUtility.getColumnValue("site_code",dom);
							sql = "select channel_partner,dis_link from site_customer" 
									+ " where cust_code = ?"
									+ " and site_code = ?";
							//System.out.println("select count(*) var_value.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(2,siteCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   channelPartner = rs.getString(1) == null ? "" : rs.getString(1);
							   disLink = rs.getString(2) == null ? "" : rs.getString(2);
							}
							//System.out.println("channelPartner.." + cnt);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if( channelPartner == null )
							{
								sql = "select channel_partner,dis_link from customer" 
									+ " where cust_code = ?";
								//System.out.println("select channel_partner var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   channelPartner = rs.getString(1) == null ? "" : rs.getString(1);
								   disLink = rs.getString(2) == null ? "" : rs.getString(2);
								}
								//System.out.println("channelPartner.." + cnt);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}
							if(disLink != null && channelPartner != null)
							{
								if( ( disLink.equalsIgnoreCase("A") || disLink.equalsIgnoreCase("S") ) && channelPartner.equalsIgnoreCase("Y") )
								{
									if( custPord != null && custPord.trim().length() > 0 )
									{
										sql = "select status  from porder" 
											+ " where purc_order = ? and	confirmed  = 'Y'";
										//System.out.println("select channel_partner var_value.." + sql);
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,custPord);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
										   status = rs.getString(1) == null ? "" : rs.getString(1);
										}
										//System.out.println("status.." + status);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										if( status == null)
										{
											status = "";	
										}
										else if( ! status.equalsIgnoreCase("O") ) 
										{
											errCode = "VTPONF";
											errString = getErrorString("cust_pord",errCode,userId);
											break;
										}
									}
								}//END IF disLink A
							}
							
						}//end cust_pord
						else  if (childNodeName.equalsIgnoreCase("pord_date"))
						{
							pord_date = genericUtility.getColumnValue("pord_date",dom);
							pordDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(pord_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.00");
							orderDate = genericUtility.getColumnValue("order_date",dom);
							orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
							if ( pordDate.compareTo(OrderDate) > 0  )
							{
								errCode = "VTPODATE";
								errString = getErrorString("pord_date",errCode,userId);
								break;
							}
						}//end pord_date
						
						else  if (childNodeName.equalsIgnoreCase("site_code__ship"))
						{
							siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
							sql = "Select count(*)  from site where site_code = ? ";
							//System.out.println("select qry from var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,siteCodeShip);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   cnt = rs.getInt(1);
							}
							//System.out.println("var_value.." + var_value);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if ( cnt == 0  )
							{
								errCode = "VTSHSITE";
								errString = getErrorString("pord_date",errCode,userId);
								break;
							}
						}//end site_code__ship
						
						else if(childNodeName.equalsIgnoreCase("sales_pers"))
							
						{
							cnt = 0;
							disparmVal = distCommon.getDisparams("999999","SALES_BANGLA",conn);
							System.out.println("getDisparams()....disparmVal..."+disparmVal);								
							disparmVal = disparmVal == null ?" " : disparmVal.trim();
							
							if(disparmVal != null && disparmVal.trim().length() > 0)
							{
								if(disparmVal=="NULLFOUND")
								{
									disparmVal = "N";
								}
								if(disparmVal.trim().equalsIgnoreCase("Y"))
								{
									salesPers = genericUtility.getColumnValue("sales_pers",dom);
									if(salesPers == null || salesPers.trim().length() == 0)
									{
										errCode = "VTSLPERS3";
										errString = getErrorString("sales_pers",errCode,userId);
										break;
									}
									//salesPers = salesPers== null ? "" : salesPers.trim();
									
									if(salesPers != null && salesPers.trim().length()>0)
									{
										sql="select Count(*)  from sales_pers where sales_pers= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,salesPers);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
										   cnt =rs.getInt(1);
										}
										System.out.println("cnt.." + cnt);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
									}
									if(cnt == 0 )
									{
										errCode = "VMSLPERS1";
										errString = getErrorString("sales_pers",errCode,userId);
										break;
									}
								}
							}
						}
						else if(childNodeName.equalsIgnoreCase("sales_pers__1"))//END sales_pers
						{
							int cnt3 = 0;
							disparmVal = distCommon.getDisparams("999999","SALES_BANGLA",conn);
							System.out.println("getDisparams()....disparmVal..."+disparmVal);								
							disparmVal = disparmVal == null ?" " : disparmVal.trim();
							
							if(disparmVal != null && disparmVal.trim().length() > 0)
							{
								if(disparmVal=="NULLFOUND")
								{
									disparmVal = "N";
								}
								if(disparmVal.trim().equalsIgnoreCase("Y"))
								{
									salesPers1 = genericUtility.getColumnValue("sales_pers__1",dom);
									System.out.println("Salesperson1>>>>>>>>>>" + salesPers1);
									if(salesPers1 == null || salesPers1.trim().length() == 0)
									{
										errCode = "VTSLPERS4";
										errString = getErrorString("sales_pers__1",errCode,userId);
										break;
									}
									//salesPers = salesPers== null ? "" : salesPers.trim();
									
									if(salesPers1 != null && salesPers1.trim().length()>0)
									{
										sql="select Count(*)  from sales_pers where sales_pers= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,salesPers1);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
											cnt3 =rs.getInt(1);
										}
										System.out.println("cnt3.." + cnt3);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
									}
									if(cnt3 == 0 )
									{
										errCode = "VMSLPERS3";
										errString = getErrorString("sales_pers__1",errCode,userId);
										break;
									}
								}
							}
						}

						
					}//end for	
					break;
				case 2 :
					System.out.println( "Detail 2 Validation called " );
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					String itemValue = "",lineNo = "",lineValue= "",updateFlag = "", lsStopBusiness = "";
					int lineNoInt = 0,lineValueInt = 0;
					NodeList itemNodeList = null,lineNoList = null,detail2List = null,childDetilList = null;
					
					Node itemNode = null,lineNoNode = null,detailNode = null,chidDetailNode = null;
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if (childNodeName.equalsIgnoreCase("item_code"))
						{
							//itemNodeList = dom2.getElementsByTagName("item_code");
							//lineNoList = dom2.getElementsByTagName("line_no");
							detail2List = dom2.getElementsByTagName("Detail2");
							itemCode = genericUtility.getColumnValue("item_code",dom);
							lineNo = genericUtility.getColumnValue("line_no",dom);
							//System.out.println("lineNo in str " + lineNo);
							//System.out.println("lineNo after trim " + lineNo.trim());
							if(lineNo != null && lineNo.trim().length() > 0)
							{
								lineNoInt = Integer.parseInt(lineNo.trim());
							}
							for(int t =0; t < detail2List.getLength(); t++ )
							{
								detailNode = detail2List.item(t);
								childDetilList = detailNode.getChildNodes();
								for(int p =0; p < childDetilList.getLength(); p++ )
								{
									chidDetailNode = childDetilList.item(p);
									//System.out.println("current child node>>>>>>>>>> " + chidDetailNode.getNodeName() );
									if(chidDetailNode.getNodeName().equalsIgnoreCase("line_no") )
									{
										//System.out.println("line node found >>>>>" + chidDetailNode.getNodeName());
										if(chidDetailNode.getFirstChild() != null )
										{
											lineValue = chidDetailNode.getFirstChild().getNodeValue();
											if(lineValue != null && lineValue.trim().length() > 0)
											{
												lineValueInt = Integer.parseInt(lineValue.trim());
											}
										}
									}
									if(chidDetailNode.getNodeName().equalsIgnoreCase("attribute") )
									{
										//System.out.println("operation node found >>>>>" + chidDetailNode.getNodeName());
										updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
										//System.out.println("Update flag is......."+updateFlag);
										
									}
									if(chidDetailNode.getNodeName().equalsIgnoreCase("item_code") )
									{
										//System.out.println("itemcode node found >>>>>" + chidDetailNode.getNodeName());
										if(chidDetailNode.getFirstChild() != null )
										{
											itemValue = chidDetailNode.getFirstChild().getNodeValue();
											/*
											System.out.println("itemValue value >>>>>" + itemValue);
											System.out.println("itemCode value >>>>>" + itemCode);
											System.out.println("itemValue.equalsIgnoreCase(itemCode) value >>>>>" + itemValue.trim().equalsIgnoreCase(itemCode.trim()));
											System.out.println("lineValueInt from dom 2 >>>>>>>> " + lineValueInt);
											System.out.println("lineNo from dom  >>>>>>>> " + lineNoInt);
											System.out.println("!updateFlag.equalsIgnoreCase(D)  >>>>>>>> " + !updateFlag.equalsIgnoreCase("D"));
											System.out.println("lineNoInt != lineValueInt  >>>>>>>> " + (lineNoInt != lineValueInt));
											System.out.println("itemValue.equalsIgnoreCase(itemCode) >>>>>>>> " + itemValue.equalsIgnoreCase(itemCode));
											*/
											if(itemCode !=null && !updateFlag.equalsIgnoreCase("D") && lineNoInt != lineValueInt && itemValue.trim().equalsIgnoreCase(itemCode.trim())) //itemCode !=null condition added by nandkumar gadkari on 09/01/19
											{
												//System.out.println("Duplicate item");
												errCode = "VMSODUPIT   ";
												//Changed by Santosh on 13/01/2017 to send column name
												//errString = getErrorString("VMSODUPIT",errCode,userId);
												errString = getErrorString("item_code",errCode,userId);
												//break;
												return errString;
											}
										}
									}
								}
								
								//lineNoNode = lineNoList.item(t);
								
							}
				
							//itemCode = genericUtility.getColumnValue("item_code",dom);
							itemCode = itemCode ==null ? "" : itemCode;
							//System.out.println("itemCode ......"+itemCode );
							siteCode = genericUtility.getColumnValue("site_code",dom1);
							//System.out.println("siteCode ......"+siteCode );
							orderDate = genericUtility.getColumnValue("order_date",dom1);
							custCode = genericUtility.getColumnValue("cust_code",dom1);
							//System.out.println("custCode ......"+custCode );
							custPord = genericUtility.getColumnValue("cust_pord",dom);
							orderTypeHdr = genericUtility.getColumnValue("order_type",dom1);
							if (orderTypeHdr == null)
							{
								orderTypeHdr = "";
							}
							//System.out.println("custPord ......"+custPord );
							orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
							sql = "Select count(*) from item where item_code = ? ";
							//System.out.println("select qry from var_value.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   cnt = rs.getInt(1);
							}
							//System.out.println("cnt of item.." + cnt);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if ( cnt == 0  )
							{
								errCode = "VMITEM1";
								errString = getErrorString("item_code",errCode,userId);
								break;
							}
							sql = "Select item_ser  from item where item_code = ? ";
							//System.out.println("select qry from var_value.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   itemSer = rs.getString(1) == null ? "" : rs.getString(1);
							}
							//System.out.println("itemSer.." + itemSer);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							sql = "Select black_listed  from customer_series where cust_code ='" + custCode + "'"
									+ "and item_ser = ?";
							//System.out.println("select qry from var_value.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,itemSer);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   blackListed = rs.getString(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if(blackListed == null)
							{
								blackListed = "";
							}
							if(blackListed.trim().length() > 0)
							{
								if ( blackListed.equalsIgnoreCase("Y")  )
								{
									errCode = "VTCUSTCD3";
									errString = getErrorString("cust_code",errCode,userId);
									break;
								}
							}
							else
							{
								sql = "select stop_business "
										+ " from customer where cust_code = ? ";
								//System.out.println("select qry from stop_business.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   stopBusiness = rs.getString(1) == null ? "" : rs.getString(1);
								}
								//System.out.println("stopBusiness.." + stopBusiness);
								rs.close();
								pstmt.close();
								pstmt = null;
                                rs = null;
                                //Changed by Pravin k on 9-MAR-21 [changed equalsIgnoreCase for null pointer ]
								//if ( stopBusiness.equalsIgnoreCase("Y")  )
								if ( "Y".equalsIgnoreCase(stopBusiness)  )
                                {
									errCode = "VTICC";
									errString = getErrorString("cust_code",errCode,userId);
									break;
								}
							}
							//Pavan Rane 25apr19 [Stop business flag consideration in SOF]
							sql = "select stop_business from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsStopBusiness = checkNullandTrim(rs.getString("stop_business"));								
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if ("Y".equalsIgnoreCase(lsStopBusiness)) {								
								errCode = "VTIIC";
								errString = getErrorString("item_code",errCode,userId);
								break;
							}
							//Pavan Rane 25apr19 end

							sql = "select channel_partner,dis_link from customer" 
								+ " where cust_code = ?";
							//System.out.println("select channel_partner var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   channelPartner = rs.getString(1) == null ? "" : rs.getString(1);
							   disLink = rs.getString(2) == null ? "" : rs.getString(2);
							}
							//System.out.println("disLink.." + disLink);
							rs.close();
							pstmt.close();
							pstmt = null;
                            rs = null;
                            //Changed by Pravin k on 9-MAR-21 [changed equalsIgnoreCase for null pointer ]
                            //if( ( disLink.equalsIgnoreCase("A") || disLink.equalsIgnoreCase("S") ) && channelPartner.equalsIgnoreCase("Y") )
                            if( ( "A".equalsIgnoreCase(disLink) || "S".equalsIgnoreCase(disLink) ) && "Y".equalsIgnoreCase(channelPartner) )
							{
								if( custPord != null && custPord.trim().length() > 0 )
								{	
									sql = " select count(*) "
												+ " from porddet"
												+ " where purc_order = ? "
												+ " and	item_code  = ? "
												+ " and	status = 'O'";
									//System.out.println("select count var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custPord);
									pstmt.setString(2,itemCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   cnt = rs.getInt(1) ;
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									if ( cnt == 0  )
									{
										errCode = "VTPODET";
										errString = getErrorString("item_code",errCode,userId);
										break;
									}
								}// if( custPord != null
							}//end  rs = null;if( ( disLink.equalsIgnoreCase("A")
							//System.out.println("itemSerHdr.." + itemSerHdr);
							sql = " select oth_series "
										+ " from itemser"
										+ " where item_ser = ?";
							//System.out.println("select itemSerHdr var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemSerHdr);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   othSeries = rs.getString(1) == null ? "" : rs.getString(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if( othSeries == null)
							{
								othSeries = "N";
							}
							itemSer1 = distCommon.getItemSer(itemCode,siteCode,OrderDate,custCode,"C",conn);
							sql =  " select item_ser"
										+ " from item_credit_perc"
										+ " where item_code = ?"
										+ " and item_ser in ( select item_ser "
										+ " from customer_series"
										+ " where cust_code = ?"
										+ " and item_ser  = item_credit_perc.item_ser)";
							//System.out.println("select item_ser.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,custCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   itemSer = rs.getString(1) == null ? "" : rs.getString(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if( ( ! itemSer1.equalsIgnoreCase( itemSerHdr ) ) && othSeries.equalsIgnoreCase("G") )
							{
								sql = " select item_ser__crpolicy,item_ser "
										+ " from itemser"
										+ " where item_ser = ?";
								//System.out.println("select itemSerHdr var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemSerHdr);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   itemSerCrpolicyHdr = rs.getString(1) == null ? "" : rs.getString(1);
								   itemSer = rs.getString(2) == null ? "" : rs.getString(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								 if( itemSerCrpolicyHdr == null )
								{
									itemSerCrpolicyHdr = itemSer;
								}
								 sql = " select item_ser__crpolicy,item_ser "
										+ " from itemser"
										+ " where item_ser = ?";
								//System.out.println("select itemSerHdr var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemSer1);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   itemSerCrpolicy = rs.getString(1) == null ? "" : rs.getString(1);
								   itemSer = rs.getString(2) == null ? "" : rs.getString(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								 if( itemSerCrpolicy == null )
								{
									itemSerCrpolicy = itemSer;
								}
								 if ( ! itemSerCrpolicyHdr.equalsIgnoreCase(itemSerCrpolicy) )
								{
									errCode = "VTITEM2";
									errString = getErrorString("item_code",errCode,userId);
									break;
								}
							}
							else
							{
								if( "NE".equalsIgnoreCase( orderTypeHdr ) )
								{
									sql= "select state_code, count_code from customer where cust_code = ?";
									//System.out.println("select itemSerHdr var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   stateCode = rs.getString(1) == null ? "" : rs.getString(1);
									   countCode = rs.getString(2) == null ? "" : rs.getString(2);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									sql = " select a.scheme_code "
												+ " from scheme_applicability a, scheme_applicability_det b"
												+ " where a.scheme_code = b.scheme_code"
												+ " and a.item_code = ?"
												+ " and a.app_from <= ?"
												+ " and a.valid_upto >= ?"
												+ " and ( b.site_code = ? or b.state_code = ? or b.count_code = ? )" // stateCode value not fetched
												+ " and a.order_type = ?";
									//System.out.println("select scheme_code var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									pstmt.setTimestamp(2,OrderDate);
									pstmt.setTimestamp(3,OrderDate);
									pstmt.setString(4,siteCode);
									pstmt.setString(5,stateCode);
									pstmt.setString(6,countCode);
									pstmt.setString(7,orderTypeHdr);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   schemeCode = rs.getString(1) == null ? "" : rs.getString(1);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}
								else
								{
									sql= "select state_code, count_code from customer where cust_code = ?";
									
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   stateCode = rs.getString(1) == null ? "" : rs.getString(1);
									   countCode = rs.getString(2) == null ? "" : rs.getString(2);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								
									sql = " select a.scheme_code "
												+ " from scheme_applicability a, scheme_applicability_det b"
												+ " where a.scheme_code = b.scheme_code"
												+ " and a.item_code = ?"
												+ " and a.app_from <= ?"
												+ " and a.valid_upto >= ?"
												+ " and (b.site_code = ? or b.state_code = ? or b.count_code = ? )" // stateCode value not fetched
												+ " and a.order_type is NULL";
									//System.out.println("select scheme_code var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									pstmt.setTimestamp(2,OrderDate);
									pstmt.setTimestamp(3,OrderDate);
									pstmt.setString(4,siteCode);
									pstmt.setString(5,stateCode);
									pstmt.setString(6,countCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   schemeCode = rs.getString(1) == null ? "" : rs.getString(1);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}//end else  "NE".equalsIgnoreCase
							}//end else if( ( disLink.equalsIgnoreCase("A")
							
							String natureVal = genericUtility.getColumnValue( "nature", dom );
							if( "F".equalsIgnoreCase( natureVal ) )
							{
								sqlStr = "select item_code__parent from item where item_code = ? ";
								
								pstmt = conn.prepareStatement( sqlStr );
								
								pstmt.setString( 1, itemCode );
								
								rs = pstmt.executeQuery();
								
								if( rs.next() )
								{
									itemCodeParent = rs.getString( "item_code__parent" ); 
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if( itemCodeParent == null || itemCodeParent.trim().length() == 0 || "null".equalsIgnoreCase( itemCodeParent ) )
								{
									errCode = "VTSCHITEM";
									errString = getErrorString("item_code",errCode,userId);
									break;									
								}
							}
							
						}//end item_code
						else  if (childNodeName.equalsIgnoreCase("item_ser"))
						{
							if( "A".equalsIgnoreCase( editFlag ) )
							{
								itemSer = genericUtility.getColumnValue("item_ser",dom);
								itemCode = genericUtility.getColumnValue("item_code",dom);
								sql = " select ITEM_SER "
													+ " from item "
													+ " where item_code = ?";
								//System.out.println("select ITEM_SER var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   itemSerItem = rs.getString(1) == null ? "" : rs.getString(1);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								if( itemSerHdr != null && itemSerHdr.trim().length() > 0 )
								{
									if( ! itemSerItem.equalsIgnoreCase( itemSerHdr ) )
									{
										sql = " select oth_series "
														+ " from itemser "
														+ " where item_ser = ?";
										//System.out.println("select ITEM_SER var_value.." + sql);
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,itemSerHdr);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
										   othSeries = rs.getString(1) == null ? "" : rs.getString(1);
										}
										rs.close();
										pstmt.close();
										pstmt = null;
										if( othSeries == null)
										{
											othSeries = "N";
										}
										if( "N".equalsIgnoreCase( othSeries ) )
										{
											errCode = "VTITEM2A";
											errString = getErrorString("item_ser",errCode,userId);
											break;
										}
									}
								}//end if( itemSerHdr != null && i
							}//if( "A".equalsIgnoreCase( editFlag ) )
							itemSer = genericUtility.getColumnValue("item_ser",dom);
							custCode = genericUtility.getColumnValue("cust_code",dom1);
							sql = " select count(*) "
										+ " from customer_series "
										+ " where cust_code = ?"
										+ " and item_ser = ?";
							//System.out.println("select customer_series var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(2,itemSer);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   cnt = rs.getInt(1) ;
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							if( cnt == 0 )
							{
								errCode = "VTITEM7";
								errString = getErrorString("item_ser",errCode,userId);
								break;
							}
						}//end item_ser	
						else  if (childNodeName.startsWith("qty"))
						{
							//System.out.println( "Detail 2 Validation called for qty " );

							itemCode = genericUtility.getColumnValue("item_code",dom);
							siteCode =  genericUtility.getColumnValue("site_code",dom1);
							orderDate =  genericUtility.getColumnValue("order_date",dom1);
							custCode =  genericUtility.getColumnValue("cust_code",dom1);
							orderType =  genericUtility.getColumnValue("order_type",dom1);
							totqtyStr = genericUtility.getColumnValue("totqty",dom);
							orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
							//System.out.println("OrderDate in time stamp:-" +OrderDate);
							String totqtystr = genericUtility.getColumnValue("totqty",dom);
							System.out.println("total Quantity"+totqtystr);
							if( totqtystr == null || totqtystr.trim().length() == 0 || "null".equalsIgnoreCase( totqtystr ) )
							{
								errString = getErrorString("totqty","VTTOTQTBLK",userId);
								break;
							}

							double totqty = Double.parseDouble( totqtystr );

							if( totqty <= 0 )
							{
								errString = getErrorString("totqty","VTTOTQTNG",userId);
								break;
							}

							boolean flag = false;
							double totalqty = 0;
							for(int i = 0; i < 12; i++)	
							{
								strSch="";
								strSch = "qty_"+i;
								//System.out.println("strSch value...." + strSch);								
								qtyStr[i] = genericUtility.getColumnValue(strSch,dom);
								if(qtyStr[i] != null && qtyStr[i].trim().length() > 0)
								{
									qty[i] = Double.parseDouble( qtyStr[i] );
									totalqty += qty[i];
								}
								if( qty[i] < 0 )
								{
									flag = true;
									//break;""
								}								
							}//end for
							if( flag )
							{
								errCode = "VTQTY1";
								errString = getErrorString("qty_1",errCode,userId);
								break;
							}
							System.out.println("totalqty"+totalqty);
							System.out.println("totqty"+totqty);
							if (totalqty != totqty)
							{
								errCode = "VTQTY1";
								errString = getErrorString("qty_1",errCode,userId);
								break;
							}
							sql = " select state_code, count_code "
										+ " from customer "
										+ " where cust_code = ?";
							//System.out.println("select cust_code var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   stateCode = rs.getString(1) == null ? "" : rs.getString(1);
							   countCode = rs.getString(2) == null ? "" : rs.getString(2);
							   
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							sql = " select  a.scheme_code"
											+ " from scheme_applicability a, scheme_applicability_det b,bom c "
											+ " where a.scheme_code = b.scheme_code and  a.scheme_code = c.bom_code"
											+ " and a.item_code = ?"
											+ " and  a.app_from <= ?"
											+ " and  a.valid_upto >= ?"
											+ " and (b.site_code = ? or b.state_code = ? or b.count_code = ? ) and" +
											"(? between case when c.min_qty is null then 0 else c.min_qty end And case when c.max_qty is null then 0 else c.max_qty end) ";
							System.out.println("select ITEM_SER var_value.." + sql);
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1,itemCode);
							pstmt1.setTimestamp(2,OrderDate);
							pstmt1.setTimestamp(3,OrderDate);
							pstmt1.setString(4,siteCode);
							pstmt1.setString(5,stateCode);
							pstmt1.setString(6,countCode);
							pstmt1.setString(7,totqtyStr);
							rs2 = pstmt1.executeQuery(); 
							while(rs2.next()) // Request_id-SUN15OCT15-added while condition For multiple scheme code by abhijit Gaikwad on 16-OCT-2015
							{
								curscheme = rs2.getString("scheme_code") ;
							System.out.println("schemeCode 1@@@@@@ [" + curscheme + "]");
							
							
							
							sql = " select (case when apply_cust_list is null then ' ' else apply_cust_list end) apply_cust_list , "
								+" (case when noapply_cust_list is null then ' ' else noapply_cust_list end) noapply_cust_list, "
								+"	order_type "
								+"	from scheme_applicability "
								+"	where scheme_code = ? ";
								
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, curscheme );
							rs  = pstmt.executeQuery();
							if( rs.next() )
							{
								applyCustList = rs.getString( "apply_cust_list" );
								noapplyCustList =  rs.getString( "noapply_cust_list" );
								applicableordtypes =  rs.getString( "order_type" );
							}	
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if( "NE".equalsIgnoreCase( orderType.trim() ) && ( applicableordtypes == null || applicableordtypes.trim().length() == 0 ) ) 
							{
								continue;
							}
							else if( applicableordtypes != null && applicableordtypes.trim().length() > 0 )
							{
								proceed = false;
								
								token = applicableordtypes.split( "," ); // f_get_token(ls_applicableordtypes,',')
								for( int tokenCtr = 0; tokenCtr < token.length; tokenCtr++ )
								{
									if( orderType.trim().equalsIgnoreCase( token[ tokenCtr ] ) )
									{
										proceed = true;
										break;
									}
								}
								if( !proceed )
								{	
									break;
								}
							}
							
							//applicable customer
							prevscheme	= schemeCode;
								
							schemeCode = curscheme;
							if(  applyCustList != null && applyCustList.trim().length() > 0 )
							{
								schemeCode = null;
								token = applyCustList.split( "," );
								for( int tokenCtr = 0; tokenCtr < token.length; tokenCtr++ )
								{
									if( custCode.trim().equalsIgnoreCase( token[ tokenCtr ] ) )
									{
										schemeCode = curscheme;
										custSchemeCode = curscheme;
										break;
									}
								}											
							}
							
							//non-applicable customer
							token = noapplyCustList.split( "," );
							for( int tokenCtr = 0; tokenCtr < token.length; tokenCtr++ )
							{
								if( custCode.trim().equalsIgnoreCase( token[ tokenCtr ] ) )
								{
									schemeCode = null;
									break;
								}
							}											
																	
							if( schemeCode != null && schemeCode.trim().length() > 0 )
							{
								llSchcnt++;
							}
							else if( llSchcnt == 1 )
							{
								schemeCode	= prevscheme;
							}
							System.out.println("Scheme code is:"+ schemeCode);
							System.out.println("Scheme code is11111111:"+ curscheme);
							
							
							
							if( schemeCode == null || schemeCode.trim().length() == 0 ) 
							{
								System.out.println("Enter if condition");
								sql = " select  a.scheme_code"
											+ " from scheme_applicability a, scheme_applicability_det b "
											+ " where a.scheme_code = b.scheme_code"
											+ " and a.item_code = ?"
											+ " and  a.app_from <= ?"
											+ " and  a.valid_upto >=?"
											+ " and (b.site_code = ?"
											+ " or b.state_code = ? or b.count_code = ? )";
								//System.out.println("select ITEM_SER var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								pstmt.setTimestamp(2,OrderDate);
								pstmt.setTimestamp(3,OrderDate);
								pstmt.setString(4,siteCode);
								pstmt.setString(5,stateCode);
								pstmt.setString(6,countCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   schemeCode = rs.getString(1) ;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("if scheme code" + schemeCode );
							}
							if(schemeCode != null && schemeCode.trim().length() > 0 ) 
							{
								System.out.println("schemeCode 2 [" + schemeCode + "]");
								sql = " select batch_qty "
											+ " from bom"
											+ " where bom_code = ?";
								//System.out.println("select batch_qty var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,schemeCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   batchQty = rs.getDouble(1) ;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								System.out.println("BAtch Quntity"+batchQty);
								flag = false;
								for(int i = 0; i < 12; i++)	
								{
									strSch="";
									strSch = "qty_"+i;
									System.out.println("strSch value...." + strSch);								
									qtyStr[i] = genericUtility.getColumnValue(strSch,dom);
									
									//qtyStr[i] = qty_[i];
									qty[i] = Double.parseDouble( ( qtyStr[i] == null || qtyStr[i].trim().length() == 0 ? "0" : qtyStr[i].trim() )  );
									System.out.println("qty[i] [" + qty[i] + "] batchQty [" + batchQty + "] mod [" + (qty[i] % batchQty) +"]");
									if( (qty[i] % batchQty) > 0 )
									{
										flag = true;
										break;
									}
								}//end for
								System.out.println("Flag is========"+ flag);
								if( flag )
								{
									errCode = "VTSCHQTY";
									errString = getErrorString(strSch,errCode,userId);
									break;
								}
								sql1 = "select restrict_upto from customeritem " +
										"where cust_code = ?" +
										" and item_code =?";
								pstmt = conn.prepareStatement(sql1);
								pstmt.setString(1,custCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									restrictUpto = rs.getTimestamp(1);
									
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(restrictUpto != null)
								{
									if(OrderDate.compareTo(restrictUpto) <= 0)
									{
									errCode = "VTRESDT";
									errString = getErrorString("qty_1",errCode,userId);
									break;
									}
								}
							}
						/*	else 
							{
								sql = " select integral_qty, restrict_upto "
											+ " from customeritem"
											+ " where cust_code = ?"
											+ " and item_code = ?";
								//System.out.println("select integral_qty var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,itemCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   integralQty = rs.getDouble(1) ;
								   restrictUpto = rs.getTimestamp(2) ;
								}
								rs.close();
								pstmt.close();
								pstmt = null;		
								if( integralQty == 0.0)		
								{
									sql = " select integral_qty "
											+ " from siteitem"
											+ " where site_code = ?"
											+ " and item_code = ?";
									//System.out.println("select integral_qty var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									
									pstmt.setString(1,siteCode);
									pstmt.setString(2,itemCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   integralQty = rs.getDouble(1) ;
									}
									rs.close();
									pstmt.close();
									pstmt = null;
								}
								if( integralQty == 0.0)		
								{
									sql = " select integral_qty"
											+ " from item"
											+ " where item_code = ?";
									//System.out.println("select integral_qty var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   integralQty = rs.getDouble(1) ;
									}
									rs.close();
									pstmt.close();
									pstmt = null;
								}
								if( restrictUpto != null )
								{
									if( OrderDate.compareTo( restrictUpto ) <= 0 )
									{
										errCode = "VTRESDT";
										errString = getErrorString("qty_1",errCode,userId);
										break;
									}
								}
								if( integralQty > 0 )
								{
									flag = false;
									for(int i = 1; i < 12; i++)	
									{
										strSch="";
										strSch = "qty_"+i;
										//System.out.println("strSch value...." + strSch);								
										qtyStr[i] = genericUtility.getColumnValue(strSch,dom);
										//qtyStr[i] = qty_[i];
										if(qtyStr[i] != null && qtyStr[i].trim().length() > 0)
										{
											qty[i] = Double.parseDouble( qtyStr[i] );
										}
										if( ( qty[i] % integralQty ) > 0 )
										{
											flag = true;
											break;
										}
									}//end for
									if( flag )
									{
										errCode = "VTINTQTY";
										errString = getErrorString("qty_1",errCode,userId);
										break;
									}
								}
							}*/
						}
							rs2.close();
							rs2 = null;
							pstmt1.close();
							pstmt1 = null;//-------------------------Ended By Abhijit Gaiwkad on 16-OCT-2015
							System.out.println("Scheme COde in After While :" + schemeCode);
							if( schemeCode == null || schemeCode.trim().length() == 0 ) //-------------------------Added By Abhijit Gaiwkad on 16-OCT-2015
							{
								System.out.println("Not Scheme Apply for item COde");
								sql = " select integral_qty, restrict_upto "
											+ " from customeritem"
											+ " where cust_code = ?"
											+ " and item_code = ?";
								//System.out.println("select integral_qty var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,itemCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   integralQty = rs.getDouble(1) ;
								   restrictUpto = rs.getTimestamp(2) ;
								}
								rs.close();
								pstmt.close();
								pstmt = null;		
								if( integralQty == 0.0)		
								{
									sql = " select integral_qty "
											+ " from siteitem"
											+ " where site_code = ?"
											+ " and item_code = ?";
									//System.out.println("select integral_qty var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									
									pstmt.setString(1,siteCode);
									pstmt.setString(2,itemCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   integralQty = rs.getDouble(1) ;
									}
									rs.close();
									pstmt.close();
									pstmt = null;
								}
								if( integralQty == 0.0)		
								{
									sql = " select integral_qty"
											+ " from item"
											+ " where item_code = ?";
									//System.out.println("select integral_qty var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   integralQty = rs.getDouble(1) ;
									}
									rs.close();
									pstmt.close();
									pstmt = null;
								}
								if( restrictUpto != null )
								{
									if( OrderDate.compareTo( restrictUpto ) <= 0 )
									{
										errCode = "VTRESDT";
										errString = getErrorString("qty_1",errCode,userId);
										break;
									}
								}
								if( integralQty > 0 )
								{
									flag = false;
									for(int i = 1; i < 12; i++)	
									{
										strSch="";
										strSch = "qty_"+i;
										//System.out.println("strSch value...." + strSch);								
										qtyStr[i] = genericUtility.getColumnValue(strSch,dom);
										//qtyStr[i] = qty_[i];
										if(qtyStr[i] != null && qtyStr[i].trim().length() > 0)
										{
											qty[i] = Double.parseDouble( qtyStr[i] );
										}
										if( ( qty[i] % integralQty ) > 0 )
										{
											flag = true;
											break;
										}
									}//end for
									if( flag )
									{
										errCode = "VTINTQTY";
										errString = getErrorString("qty_1",errCode,userId);
										break;
									}
								}
							
							}
							
							//validation as given by jasmina on 130310-DI89SUN240
							String natureVal = genericUtility.getColumnValue( "nature", dom );
							if( "F".equalsIgnoreCase( natureVal ) )
							{
								/*String stateCd = null, browItemCode = null, itemCodeParentCur = null;
								String  itemCodeParent = null,  colname = null;
								double qtyper = 0.0, batqty = 0.0, appMinQty = 0.0, appMaxQty = 0.0, minqty = 0.0, freeQty = 0.0, totChargeQty = 0.0;
								double totFreeQty = 0.0;
								double chargrQty = 0.0, prvChargeQty = 0.0, unconfirmChargeQty = 0.0;
								double mqty = 0.0, unconfirmFreeQty = 0.0, prvFreeQty = 0.0;
								double qtyVal = 0.0;
								int curLineno;			  
								Timestamp appfromDate = null, validuptoDate = null, tranDateTs = null;
								
								String curscheme = null, sqlStr = null;
								String applyCustList = null;
								String noapplyCustList = null, applicableordtypes = null, tranDateStr = null;
								String[] token; 
								String prevscheme = null, applyCust = null;
								String custSchemeCode = null, noapplyCust = null;
								int llRow = 0, llSchcnt = 0, llCnt = 0, i = 0;
								boolean  proceed;*/
								//ResultSet rs1;
								
								itemCode = genericUtility.getColumnValue( "item_code", dom );
								siteCode = genericUtility.getColumnValue( "site_code", dom1 );
								custCode = genericUtility.getColumnValue( "cust_code", dom1 );
								tranDateStr = genericUtility.getColumnValue( "order_date", dom1 );
								
								tranDateTs = Timestamp.valueOf(genericUtility.getValidDateString( tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

								orderType = genericUtility.getColumnValue( "order_type", dom1 );
								tranId = genericUtility.getColumnValue( "tran_id", dom );

								//scheme define on groups of items 
								sqlStr = " select item_code__parent item_code__parent "
										+"	from item "
										+"	where item_code = ? "//:ls_item_code
										+"		and item_code__parent is not null ";

								pstmt = conn.prepareStatement( sqlStr );
								
								pstmt.setString( 1, itemCode );
								
								rs = pstmt.executeQuery();
								
								if( rs.next() )
								{
									itemCodeParent = rs.getString( "item_code__parent" ); 
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
										
								if( itemCodeParent != null && itemCodeParent.trim().length() > 0 )
								{
									sqlStr = " select state_code, count_code "
										//into :mstate_cd, :ls_count_code 
										+"	from customer where cust_code = ? "; //:ls_cust_code ;
									
									pstmt = conn.prepareStatement( sqlStr );
									
									pstmt.setString( 1, custCode );
									
									rs = pstmt.executeQuery();
									
									if( rs.next() )
									{
										stateCd = rs.getString( "state_code" ); 
										countCode = rs.getString( "count_code" ); 
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									orderType = getAbsString( orderType );
									siteCode =  getAbsString( siteCode );
									stateCd = getAbsString( stateCd );
									countCode = getAbsString( countCode );
									
									sqlStr = " select a.scheme_code " 
											+" from scheme_applicability a, "
											+"		scheme_applicability_det b"
											+" where a.scheme_code	= b.scheme_code"
											+" and a.item_code 		= ? " //'" + ls_item_code__parent  	+ "'"		+ &
											+" and a.app_from 			<= ? " //		+ &
											+" and a.valid_upto 		>= ? " //		+ &
											+" and (b.site_code 		= ? " //'" + ls_site_code 			+ "'"		+ &
											+" or b.state_code 		= ? " //'" + mstate_cd 				+ "'"    + &
											+" or b.count_code 		= ? ) "; // '" + ls_count_code	+ "')"
									pstmt = conn.prepareStatement( sqlStr );
									
									pstmt.setString( 1, itemCodeParent );
									pstmt.setTimestamp( 2, tranDateTs );
									pstmt.setTimestamp( 3, tranDateTs );
									pstmt.setString( 4, siteCode );
									pstmt.setString( 5, stateCd );
									pstmt.setString( 6, countCode );
									rs = pstmt.executeQuery();
									
									while( rs.next() )
									{
										curscheme = rs.getString( "scheme_code" );
										
										sql = " select (case when apply_cust_list is null then ' ' else apply_cust_list end) apply_cust_list , "
											+" (case when noapply_cust_list is null then ' ' else noapply_cust_list end) noapply_cust_list, "
											+"	order_type "
												//into :ls_apply_cust_list,
												//:ls_noapply_cust_list,
												//:ls_applicableordtypes
											+"	from scheme_applicability "
											+"	where scheme_code = ? "; //:ls_curscheme;
											
										pstmt1 = conn.prepareStatement( sql );
										
										pstmt1.setString( 1, curscheme );
										
										rs1 = pstmt1.executeQuery();
										
										if( rs1.next() )
										{
											applyCustList = rs1.getString( "apply_cust_list" );
											noapplyCustList =  rs1.getString( "noapply_cust_list" );
											applicableordtypes =  rs1.getString( "order_type" );
										}	
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										
										if( "NE".equalsIgnoreCase( orderType.trim() ) && ( applicableordtypes == null || applicableordtypes.trim().length() == 0 ) ) 
										{
											continue;
										}
										else if( applicableordtypes != null && applicableordtypes.trim().length() > 0 )
										{
											proceed = false;
											
											token = applicableordtypes.split( "," ); // f_get_token(ls_applicableordtypes,',')
											for( int tokenCtr = 0; tokenCtr < token.length; tokenCtr++ )
											{
												if( orderType.trim().equalsIgnoreCase( token[ tokenCtr ] ) )
												{
													proceed = true;
													break;
												}
											}
											if( !proceed )
											{	
												break;
											}
										}
										
										//applicable customer
										prevscheme	= schemeCode;
											
										schemeCode = curscheme;
										if(  applyCustList != null && applyCustList.trim().length() > 0 )
										{
											schemeCode = null;
											token = applyCustList.split( "," );
											for( int tokenCtr = 0; tokenCtr < token.length; tokenCtr++ )
											{
												if( custCode.trim().equalsIgnoreCase( token[ tokenCtr ] ) )
												{
													schemeCode = curscheme;
													custSchemeCode = curscheme;
													break;
												}
											}											
										}
										
										//non-applicable customer
										token = noapplyCustList.split( "," );
										for( int tokenCtr = 0; tokenCtr < token.length; tokenCtr++ )
										{
											if( custCode.trim().equalsIgnoreCase( token[ tokenCtr ] ) )
											{
												schemeCode = null;
												break;
											}
										}											
																				
										if( schemeCode != null && schemeCode.trim().length() > 0 )
										{
											llSchcnt++;
										}
										else if( llSchcnt == 1 )
										{
											schemeCode	= prevscheme;
										}										
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
																				
									if( llSchcnt == 0 )
									{
										errCode = "VTFREEQTY"; //Scheme is not applicable for the entered item code
										errString = getErrorString( childNodeName, errCode, userId ); 
										break;				
									}
									else if( llSchcnt > 1 )
									{
										errCode = "VTITEM10"; //Item cannot have more than one scheme applicable for same period.
										errString = getErrorString( childNodeName, errCode, userId ); 
										break;				
									}else if( custSchemeCode != null && custSchemeCode.trim().length() > 0 )
									{
										schemeCode = custSchemeCode;
									}
									
									sqlStr = "select app_from, valid_upto "//into :ldt_appfrom, :ldt_validupto 
											+"	from scheme_applicability "
											+"	where scheme_code = ? "; //:ls_scheme_code ;
									pstmt1 = conn.prepareStatement( sqlStr );
									
									pstmt1.setString( 1, schemeCode );
									
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next() )
									{
										appfromDate = rs1.getTimestamp( "app_from" );
										validuptoDate =  rs1.getTimestamp( "valid_upto" );
									}	
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									
									//to find out total charge qty
									sqlStr = "select ( case when tot_charge_qty is null then 0 else tot_charge_qty end ) tot_charge_qty, "
										+"		( case when tot_free_qty is null then 0 else tot_free_qty end ) tot_free_qty "
										+"	from prd_scheme_trace "
										+"	where site_code= 	? " //:ls_site_code
										+"	and cust_code	=	? " //:ls_cust_code
										+"	and item_code	=	? " //:ls_item_code__parent
										+"	and scheme_code= ? " //:ls_scheme_code
										+"	and ? between eff_from and valid_upto ";
									
									pstmt1 = conn.prepareStatement( sqlStr );
									
									pstmt1.setString( 1, siteCode );
									pstmt1.setString( 2, custCode );
									pstmt1.setString( 3, itemCodeParent );
									pstmt1.setString( 4, schemeCode );
									pstmt1.setTimestamp( 5, tranDateTs );
									
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next() )
									{
										totChargeQty = rs1.getDouble( "tot_charge_qty" );
										totFreeQty = rs1.getDouble( "tot_free_qty" );
									}	
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;

									minqty = 0;
									
									//tO find out UNconfirmed charge qty		
									sqlStr = " select sum(case when nature ='C' then (qty_1+qty_2+qty_3+qty_4+qty_5+qty_6+qty_7+qty_8+qty_9+qty_10+qty_11+qty_12) else 0 end) unconfirm_charge_qty, "
											+" sum(case when nature ='F' then (qty_1+qty_2+qty_3+qty_4+qty_5+qty_6+qty_7+qty_8+qty_9+qty_10+qty_11+qty_12) else 0 end) unconfirm_free_qty "
									//into :lc_unconfirm_charge_qty , :lc_unconfirm_free_qty 
											+"	from  sordform a, sordformdet b "
											+"	where a.tran_id = b.tran_id "
											+"	and a.tran_id 	<>	? " //:ls_tran_id "
											+"	and a.site_code = ? " //:ls_site_code "
											+"	and a.cust_code = ? " //:ls_cust_code "
											+"	and a.order_date between ? and ? " // :ldt_appfrom and :ldt_validupto
											+"	and b.item_code in (select item_code from item where item_code__parent = ? ) " //:ls_item_code__parent) "
											+"	and (case when a.status is null then 'N' else a.status end )= 'N' "
											+"	and b.nature in ('C' ,'F') ";;
									
									pstmt1 = conn.prepareStatement( sqlStr );
									
									pstmt1.setString( 1, tranId );
									pstmt1.setString( 2, siteCode );
									pstmt1.setString( 3, custCode );
									pstmt1.setTimestamp( 4, appfromDate );
									pstmt1.setTimestamp( 5, validuptoDate );
									pstmt1.setString( 6, itemCodeParent );
									
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next() )
									{
										unconfirmChargeQty = rs1.getDouble( "unconfirm_charge_qty" );
										unconfirmFreeQty  = rs1.getDouble( "unconfirm_free_qty" );
									}	
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;

									//if isnull(lc_unconfirm_charge_qty) then lc_unconfirm_charge_qty = 0
									//if isnull(lc_unconfirm_free_qty) then lc_unconfirm_free_qty = 0
									
									//to find out charge qty in current transction
									String curLinenoStr = getNumString( genericUtility.getColumnValue( "line_no", dom ) );
									curLineno = Integer.parseInt( curLinenoStr );
									
									prvChargeQty = 0;
									prvFreeQty = 0;
									
									NodeList detNodeList = dom2.getElementsByTagName("Detail2");
									Node currNode = null;
									int deNodeListLength = detNodeList.getLength();
									for(int detCtr = 0; detCtr < deNodeListLength; detCtr++)
									{
										currNode = detNodeList.item( detCtr );
										
										int lineNoCurr = Integer.parseInt( getNumString( genericUtility.getColumnValueFromNode("line_no", currNode) ) );
										String nature = getAbsString( genericUtility.getColumnValueFromNode( "nature", currNode ) );
										browItemCode = getAbsString( genericUtility.getColumnValueFromNode( "item_code", currNode ) );
										
										if( curLineno != lineNoCurr )
										{
											llCnt = 0;
											sqlStr = " select item_code__parent " //into :ls_item_code__parent_cur 
													+"	from item "
													+"	where item_code = ? "; //:ls_brow_item_code ;
											pstmt1 = conn.prepareStatement( sqlStr );
											
											pstmt1.setString( 1, browItemCode );
											
											rs1 = pstmt1.executeQuery();
											
											if( rs1.next() )
											{
												itemCodeParentCur  = rs1.getString( "item_code__parent" );
											}	
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											if( itemCodeParentCur != null && itemCodeParent != null && itemCodeParentCur.trim().equalsIgnoreCase( itemCodeParent.trim() ) )
											{
												double totalQty = 0;
												String fldQtyStr = "0";
												for( int fldCnt = 1; fldCnt <=12; fldCnt++ )
												{
													fldQtyStr = getNumString( genericUtility.getColumnValueFromNode( "qty_" + fldCnt, currNode ) );
													totalQty = totalQty + Double.parseDouble( fldQtyStr );
												}
												
												if( "F".equalsIgnoreCase( nature ) )
												{
													prvFreeQty = totalQty;
												}
												else
												{
													prvChargeQty = totalQty;
												}
											}
										}
									} // end for

									
									//Total cahrge qty = unconfiem charge qty + charge qty in current transaction + total charge qty in invoice
									chargrQty = unconfirmChargeQty + prvChargeQty + totChargeQty;
									
									//quantity slab
									sqlStr = " Select count(1) ll_cnt " //into :ll_cnt
											+"	From bom "
											+"	Where bom_code = ? " //:ls_scheme_code
											+"	And	 ? between case when min_qty is null then 0 else min_qty end And "
											+"							case when max_qty is null then 0 else max_qty end ";
									pstmt1 = conn.prepareStatement( sqlStr );
									
									pstmt1.setString( 1, schemeCode );
									pstmt1.setDouble( 2, chargrQty );
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next() )
									{
										llCnt  = rs1.getInt( "ll_cnt" );
									}	
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
																
									if( llCnt == 0 )
									{
										errCode = "VTFREEQTY2"; //Chargeable quantity of group of items not eligible for the free quantity
										errString = getErrorString( childNodeName, errCode, userId ); 
										break;				
									}
										
									//Free quantity validation 
									sqlStr = " select bom.batch_qty lc_batqty, bomdet.qty_per lc_qtyper, "
											+" bomdet.min_qty lc_minqty, bomdet.app_min_qty lc_app_min_qty, "
											+" bomdet.app_max_qty lc_app_max_qty "
											//into 		:lc_batqty			,	:lc_qtyper			, 	
											//	:lc_minqty			,	:lc_app_min_qty	,	:lc_app_max_qty
											+"	from bom, bomdet "
											+"	where bom.bom_code = bomdet.bom_code and "
											+"		bomdet.bom_code 	= ? "// :ls_scheme_code and 
											+"	and bomdet.nature 		= 'F' ";
									
									pstmt1 = conn.prepareStatement( sqlStr );
									
									pstmt1.setString( 1, schemeCode );
									
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next() )
									{
										batqty = rs1.getDouble( "lc_batqty" );
										qtyper = rs1.getDouble( "lc_qtyper" );
										minqty = rs1.getDouble( "lc_minqty" );
										appMinQty = rs1.getDouble( "lc_app_min_qty" );
										appMaxQty = rs1.getDouble( "lc_app_max_qty" );
									}
									else
									{
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										
										errCode = "VTFREEQTY"; //Scheme is not applicable for the entered item code
										errString = getErrorString( childNodeName, errCode, userId ); 
										break;				
									}
									if( rs1 != null ) 
										rs1.close();
									rs1 = null;
									if( pstmt1 != null )
										pstmt1.close();
									pstmt1 = null;
																		
									if( chargrQty >= appMinQty && chargrQty <= appMaxQty )
									{
										freeQty = ( (int)( chargrQty / batqty) ) * qtyper; //Calculating free qty based on slab
									}
									else
									{
										freeQty = 0;
									}
										
									qtyVal = 0;
									mqty = 0; 
									for( int fldCnt = 1; fldCnt <= 12; fldCnt++ )
									{
										colname = "qty_" + fldCnt;
										qtyVal = Double.parseDouble( getNumString( genericUtility.getColumnValue( colname, dom ) ) );
										mqty = mqty + qtyVal;
									}
													
									
									//(Entred free qty + total given free qty + unconfirmed free qty) > Eligible free qty
									if( ( mqty + totFreeQty + unconfirmFreeQty + prvFreeQty ) > ( freeQty ) )
									{
										errCode = "VTFREEQTY1"; //Entered free quantity is greater than scheme's free quantity
										errString = getErrorString( childNodeName, errCode, userId ); 
										break;				
									}

								}								
							}
							//end 
						}  //end qty_1
						else  if ( childNodeName.equalsIgnoreCase("totqty") )
						{
							totQty = genericUtility.getColumnValue("totqty",dom);
							//System.out.println( "In totqty validation :: " + totQty );
							if( totQty == null || totQty.trim().length() == 0 || "null".equalsIgnoreCase( totQty ) )
							{
								errString = getErrorString("totqty","VTTOTQTBLK",userId);
								break;
							}
							
							if( totQty == null)
							{
								TotQty = 0.0;
							}
							else
							{
								TotQty = Double.parseDouble( totQty);
							}

							if( TotQty <= 0 )
							{
								errString = getErrorString("totqty","VTTOTQTNG",userId);
								break;
							}

							/*for(int i = 1; i < 12; i++)	
							{
								//qtyStr[i] = qty_[i];
								qty[i] = Double.parseDouble( qtyStr[i] );
								total = total + qty[i];
							}//end for
							if( total != TotQty)
							{
								errCode = "VTTOTQTY1";
								errString = getErrorString("totqty",errCode,userId);
								break;
							}*/
						}//end totqty
						else  if ( childNodeName.equalsIgnoreCase("ord_value") )
						{
							double detOrdSumVal=0;
							Node currDetail1 = null;
							NodeList detailList1 = dom2.getElementsByTagName("Detail2");
							int noOfDetails = detailList1.getLength();
							// System.out.println("Dom print"+genericUtility.serializeDom(dom2));
							// System.out.println("current dom print"+genericUtility.serializeDom(dom));
							for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) 
							{
								currDetail1 = detailList1.item(ctr1);
                                //Changed by Pravin k on 9-MAR-21 [Incase getColumnValueFromNode() return no value parsDouble will thor exception ] START
                                //double eachOrdVal = Double.parseDouble(genericUtility.getColumnValueFromNode("ord_value", currDetail1));
                                double eachOrdVal = 0.0;
                                if( (E12GenericUtility.checkNull( genericUtility.getColumnValueFromNode("ord_value", currDetail1)) ).length() > 0 )
                                {
                                    eachOrdVal = Double.parseDouble(genericUtility.getColumnValueFromNode("ord_value", currDetail1));
                                }
                                //Changed by Pravin k on 9-MAR-21 [Incase getColumnValueFromNode() return no value parsDouble will thor exception] END
								detOrdSumVal=detOrdSumVal+eachOrdVal;
							}
							
							System.out.println("detOrdSumVal======"+detOrdSumVal);
							
							ordValue = genericUtility.getColumnValue("ord_value",dom);
							totValue = genericUtility.getColumnValue("tot_value",dom1);
							maxOrdValue = genericUtility.getColumnValue("max_ord_value",dom1);
							System.out.println("ordVal"+ordValue+"tot_val"+totValue+"maxOrdVal"+maxOrdValue);
							if(ordValue != null && ordValue.trim().length() > 0)
							{
								OrdValue = Double.parseDouble(ordValue);
							}
							if(totValue != null && totValue.trim().length() > 0)
							{
								TotValue = Double.parseDouble(totValue);
							}
							if(maxOrdValue != null && maxOrdValue.trim().length() > 0)
							{
								MaxOrdValue = Double.parseDouble(maxOrdValue); // lcOrdValueO not clear
							}
							if( ( MaxOrdValue != 0) && ( (detOrdSumVal  ) > MaxOrdValue ) )
							{
								errCode = "VTMXORDAMT";
								errString = getErrorString("ord_value",errCode,userId);
								break;
							}
						}//end ord_value
						else  if ( childNodeName.equalsIgnoreCase("site_code__ship") )
						{
							siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
							sql = " select count(*) "
											+ " from site"
											+ " where site_code = ?";
							//System.out.println("select siteCodeShip var_value.." + sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,siteCodeShip);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   cnt = rs.getInt(1) ;
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							if( cnt == 0)
							{
								errCode = "VTSHSITE";
								errString = getErrorString("site_code__ship",errCode,userId);
								break;
							}
						}//end totqty
						else  if ( childNodeName.equalsIgnoreCase("cust_item__ref") )
						{
							custItemRef = genericUtility.getColumnValue("cust_item__ref",dom);
							itemCode = genericUtility.getColumnValue("item_code",dom);
							custCode = genericUtility.getColumnValue("cust_code",dom1);
							//msalam - as per kandarp sir on phone - 080809
							//Validation required if custItemRef is not null only
							if( custItemRef != null && custItemRef.trim().length() > 0 )
							{
								sql = " select count(*) "
										+ " from customeritem"
										+ " where cust_code = ?"
										+ " and item_code = ?"
										+ " and item_code__ref = ?" ;
								//System.out.println("select custItemRef var_value.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,custItemRef);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   cnt = rs.getInt(1) ;
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								if( cnt == 0)
								{
									errCode = "VTCUSTITM";
									errString = getErrorString("cust_item__ref",errCode,userId);
									break;
								}
							}
						}     //end totqty
//						---------------------------- Nandkumar Gadkari -------------on 12/11/18---------start--------------------
						else if (childNodeName.equalsIgnoreCase("nature")) {
							System.out.println("####  Validating Nature ....");
							//Changed by AMOL S Created common component to for SOrderForm and PlaceOrdWizIc [START]
							String nature_ = "";
							if(childNode.getFirstChild() != null)
							{
								nature_ = childNode.getFirstChild().getNodeValue();
							}
							//System.out.println("#### nature "+nature_);							
							System.out.println("#### Counter "+ctr+" nature "+nature_+" itemCode "+itemCode);
							ValidateSorderForm validateSorderForm = new ValidateSorderForm();
							errCode = validateSorderForm.validateNature(conn,dom,dom1,dom2,nature_,itemCode);
							//errCode = validateSorderForm.validateNature(conn,dom,dom1,dom2);
							System.out.println("#### Error Code "+errCode);
							if(errCode.trim().length() > 0)
							{
								errString = getErrorString("nature",errCode,userId);
								/*errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());*/
								break;
							}
							//Changed by AMOL S Created common component to for SOrderForm and PlaceOrdWizIc [START]
							/*
						//	-------------------------------------------------------------------------------------------
							
							double  unConfTotFreeQty = 0 , 	 rate1 = 0,qtyTot=0.0,mRate = 0.00,value=0.0,valueAmount=0.0,quantity=0.0;
							String  currLineNo = "",ldtDateStr = "",lsPriceList = "",retlSchmRateBase="",lsUnit="",lsListType="",lsRefNo="",nature="";
							Timestamp  ldtPlDate = null,ldPlistDate = null;
							int llPlcount=0;
							SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
							nature = checkNullandTrim(genericUtility.getColumnValue("nature", dom));
							itemCode= checkNull(genericUtility.getColumnValue("item_code", dom));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
							custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
							OrderDate = Timestamp.valueOf(
									genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
							tranId = checkNull(genericUtility.getColumnValue("tran_id", dom2));
							if(tranId.trim().length() == 0)
							{
								tranId=" ";
							}
							freeQty=0.0;
							qtyTot = checkDoubleNull(genericUtility.getColumnValue("totqty", dom));
							System.out.println("inside scheme balance..............." +nature);
							System.out.println("itemCodeOrd==" + itemCode);
							System.out.println("siteCode==" + siteCode);
							System.out.println("custCode==" + custCode);
							System.out.println("quantity==" + qtyTot);
							System.out.println("orderDate==" + OrderDate);
							
							
							
							if ("V".equalsIgnoreCase(nature.trim()))
							{	
								retlSchmRateBase = checkNullandTrim(distCommon.getDisparams( "999999", "RETL_SCHM_RATE_BASE", conn ));	

								
									ldtDateStr = genericUtility.getColumnValue("order_date", dom1);

								
								ldPlistDate = OrderDate;
								
								if("M".equalsIgnoreCase(retlSchmRateBase))
								{
									lsPriceList = checkNullandTrim(distCommon.getDisparams( "999999", "MRP", conn ));
									lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));
									lsListType = distCommon.getPriceListType(lsPriceList, conn);

									sql = "select count(1)  as llPlcount from pricelist where price_list=?"
											+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
											+ " and (ref_no is not null)";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, lsPriceList);
									pstmt.setString(2, itemCode);
									pstmt.setString(3, lsUnit);
									pstmt.setString(4, lsListType);
									pstmt.setTimestamp(5, OrderDate);
									pstmt.setTimestamp(6, OrderDate);
									pstmt.setDouble(7, qtyTot);
									pstmt.setDouble(8, qtyTot);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										llPlcount = rs.getInt("llPlcount");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (llPlcount >= 1) {
										sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
												+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, lsPriceList);
										pstmt.setString(2, itemCode);
										pstmt.setString(3, lsUnit);
										pstmt.setString(4, lsListType);
										pstmt.setTimestamp(5, OrderDate);
										pstmt.setTimestamp(6, OrderDate);
										pstmt.setDouble(7, qtyTot);
										pstmt.setDouble(8, qtyTot);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											lsRefNo = rs.getString("ref_no");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
												conn);
									}
									if (mRate <= 0) {
										mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
												conn);
									}
										
								}
								else
								{
									
									sql = " SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE= ?  ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,custCode);
										
										rs1 = pstmt.executeQuery();

										if (rs1.next()) 
										{
											lsPriceList =checkNull( rs1.getString(1));
										}
										rs1.close();
										rs1 = null;
										pstmt.close();
										pstmt = null;
									
									if (lsPriceList != null || lsPriceList.trim().length() > 0)
									{
										mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCode, "", "L", qtyTot, conn);
										System.out.print("mRate gbfICquantity++++++++" + mRate);
										System.out.print("mqty++++++++" + qtyTot);
									}
									
								}

								valueAmount= qtyTot * mRate;
								
								//---------------------------------------------------
								
								
								
								sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
									+ " AND EFF_FROM <= ? AND VALID_UPTO >=?  AND CUST_CODE = ?  AND ITEM_CODE= ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, OrderDate);
								pstmt.setTimestamp(2, OrderDate);
								pstmt.setString(3,custCode);
								pstmt.setString(4,"X");
								rs1 = pstmt.executeQuery();

								if (rs1.next()) 
								{
									freeQty = rs1.getDouble(1);
								}
								rs1.close();
								rs1 = null;
								pstmt.close();
								pstmt = null;
								if(freeQty > 0)
								{
									sql = " SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE= ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									
									rs1 = pstmt.executeQuery();

									if (rs1.next()) 
									{
										lsPriceList =checkNull( rs1.getString(1));
									}
									rs1.close();
									rs1 = null;
									pstmt.close();
									pstmt = null;
									
									sql = " select ( (case when qty_1 is null then 0 else qty_1 end) + (case when qty_2 is null then 0 else qty_2 end)  + (case when qty_3 is null then 0 else qty_3 end)  +(case when qty_4 is null then 0 else qty_4 end)  +(case when qty_5 is null then 0 else qty_5 end)  + (case when qty_6 is null then 0 else qty_6 end) )  ,"
											+ " a.order_date,b.unit " +								
													" from sordform a,sordformdet b	where a.TRAN_ID = b.TRAN_ID and a.site_code = ?	"
													+ " and a.cust_code = ?  and a.TRAN_ID <> ? and a.order_date between ? and ?"
													+ " and (case when a.status is null then 'N' else trim(a.status) end )= 'N'	and b.nature in ('V')";

											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, siteCode);
											pstmt1.setString(2, custCode);
											pstmt1.setString(3, tranId);
											pstmt1.setTimestamp(4, OrderDate);
											pstmt1.setTimestamp(5, OrderDate);
											rs1 = pstmt1.executeQuery();
											while (rs1.next()) {
											
												
												qtyTot = rs1.getDouble(1);
												OrderDate = rs1.getTimestamp(2);
												lsUnit = rs1.getString(3);
												
											    ldtDateStr = sdf.format(OrderDate);

												
												ldPlistDate = OrderDate;
												
												
												if("M".equalsIgnoreCase(retlSchmRateBase))
												{
													lsPriceList = checkNullandTrim(distCommon.getDisparams( "999999", "MRP", conn ));
													lsListType = distCommon.getPriceListType(lsPriceList, conn);

													sql = "select count(1)  as llPlcount from pricelist where price_list=?"
															+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
															+ " and (ref_no is not null)";
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, lsPriceList);
													pstmt.setString(2, itemCode);
													pstmt.setString(3, lsUnit);
													pstmt.setString(4, lsListType);
													pstmt.setTimestamp(5, OrderDate);
													pstmt.setTimestamp(6, OrderDate);
													pstmt.setDouble(7, qtyTot);
													pstmt.setDouble(8, qtyTot);
													rs = pstmt.executeQuery();
													if (rs.next()) {
														llPlcount = rs.getInt("llPlcount");
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;

													if (llPlcount >= 1) {
														sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
																+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, lsPriceList);
														pstmt.setString(2, itemCode);
														pstmt.setString(3, lsUnit);
														pstmt.setString(4, lsListType);
														pstmt.setTimestamp(5, OrderDate);
														pstmt.setTimestamp(6, OrderDate);
														pstmt.setDouble(7, qtyTot);
														pstmt.setDouble(8, qtyTot);
														rs = pstmt.executeQuery();
														if (rs.next()) {
															lsRefNo = rs.getString("ref_no");
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;

														mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
																conn);
													}
													if (mRate <= 0) {
														mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
																conn);
													}
												}
												else
												{
													
													if (lsPriceList != null || lsPriceList.trim().length() > 0)
													{
														mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCode, "", "L", qtyTot, conn);
														System.out.print("mRate gbfICquantity++++++++" + mRate);
														System.out.print("mqty++++++++" + qtyTot);
													}
													
												}
												value= qtyTot * mRate;
												unConfTotFreeQty= unConfTotFreeQty + value;
												System.out.println("unConfTotFreeQty separte free" + lsPriceList +ldtPlDate+ qtyTot );
												
												
											}
											pstmt1.close();
											rs1.close();
											pstmt1 = null;
											rs1 = null;
											
											Node currDetail1 = null;
											prvFreeQty = 0;
											int count = 0;
											currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
											NodeList detailList1 = dom2.getElementsByTagName("Detail2");
											
											
											int noOfDetails = detailList1.getLength();
											
											
											OrderDate = Timestamp.valueOf(
													genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
															genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
															+ " 00:00:00.0");
										
											ldtDateStr = genericUtility.getColumnValue("order_date", dom1);

											ldPlistDate = OrderDate;
											for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

												currDetail1 = detailList1.item(ctr1);
												
												lineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", currDetail1));
												nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
												browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
												quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("totqty", currDetail1));
												lsUnit = checkNullandTrim(genericUtility.getColumnValueFromNode("unit", currDetail1));
												System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);
												
												System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");
											
												if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
													System.out.println("Insideif00000forSCHEME_BALANCE ");
														if (nature.equals("V")) {
															
															if("M".equalsIgnoreCase(retlSchmRateBase))
															{
																lsPriceList = checkNullandTrim(distCommon.getDisparams( "999999", "MRP", conn ));
																lsListType = distCommon.getPriceListType(lsPriceList, conn);

																sql = "select count(1)  as llPlcount from pricelist where price_list=?"
																		+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
																		+ " and (ref_no is not null)";
																pstmt = conn.prepareStatement(sql);
																pstmt.setString(1, lsPriceList);
																pstmt.setString(2, browItemCode);
																pstmt.setString(3, lsUnit);
																pstmt.setString(4, lsListType);
																pstmt.setTimestamp(5, OrderDate);
																pstmt.setTimestamp(6, OrderDate);
																pstmt.setDouble(7, quantity);
																pstmt.setDouble(8, quantity);
																rs = pstmt.executeQuery();
																if (rs.next()) {
																	llPlcount = rs.getInt("llPlcount");
																}
																rs.close();
																rs = null;
																pstmt.close();
																pstmt = null;

																if (llPlcount >= 1) {
																	sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
																			+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
																	pstmt = conn.prepareStatement(sql);
																	pstmt.setString(1, lsPriceList);
																	pstmt.setString(2, browItemCode);
																	pstmt.setString(3, lsUnit);
																	pstmt.setString(4, lsListType);
																	pstmt.setTimestamp(5, OrderDate);
																	pstmt.setTimestamp(6, OrderDate);
																	pstmt.setDouble(7, quantity);
																	pstmt.setDouble(8, quantity);
																	rs = pstmt.executeQuery();
																	if (rs.next()) {
																		lsRefNo = rs.getString("ref_no");
																	}
																	rs.close();
																	rs = null;
																	pstmt.close();
																	pstmt = null;

																	mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, browItemCode, lsRefNo, "L", quantity,
																			conn);
																}
																if (mRate <= 0) {
																	mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, browItemCode, lsRefNo, "L", quantity,
																			conn);
																}
															}
															else
															{
																
																if (lsPriceList != null || lsPriceList.trim().length() > 0)
																{
																	mRate = distCommon.pickRate(lsPriceList, ldtDateStr, browItemCode, "", "L", quantity, conn);
																	System.out.print("mRate gbfICquantity++++++++" + mRate);
																	System.out.print("mqty++++++++" + quantity);
																}
																
															}
															value =quantity * mRate;
															prvFreeQty =prvFreeQty + value;
														}
														System.out.println(
																"prvFreeQty insdie V[" + prvFreeQty+ "]");
													
												}	
											}	
											
											if ((valueAmount +unConfTotFreeQty + prvFreeQty ) > freeQty) {
												errCode = "VTFREEQTY1";// Entered free quantity is
												// greater than scheme's free
												// quantity
												errString = getErrorString( childNodeName, errCode, userId ); 
												
												System.out.println(
														"Entered free quantity is greater than sCHEME_BALANCE quantity");
												break;
											}
												
								}
								else
								{
									errCode = "VTQTYSCBAL";// Entered free quantity is
									// greater than scheme's free
									// quantity
									errString = getErrorString( childNodeName, errCode, userId ); 
									
									System.out.println(
											"Entered free quantity is greater than sCHEME_BALANCE quantity");
									break;
								}
								
							}
							if ("I".equalsIgnoreCase(nature.trim()))
							{
								sql = " SELECT BALANCE_FREE_QTY - USED_FREE_QTY  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_QTY - USED_FREE_QTY > 0 "
										+ " AND EFF_FROM <= ? AND VALID_UPTO >=? AND CUST_CODE = ?  AND ITEM_CODE= ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setTimestamp(1, OrderDate);
									pstmt.setTimestamp(2, OrderDate);
									pstmt.setString(3,custCode);
									pstmt.setString(4,itemCode);
									rs1 = pstmt.executeQuery();

									if (rs1.next()) 
									{
										freeQty = rs1.getDouble(1);
									}
									rs1.close();
									rs1 = null;
									pstmt.close();
									pstmt = null;
									if(freeQty > 0)
									{
										sql = " select sum(case when nature ='I' then  ( (case when qty_1 is null then 0 else qty_1 end) + (case when qty_2 is null then 0 else qty_2 end)  + (case when qty_3 is null then 0 else qty_3 end)  +(case when qty_4 is null then 0 else qty_4 end)  +(case when qty_5 is null then 0 else qty_5 end)  + (case when qty_6 is null then 0 else qty_6 end) ) else 0 end) as unconfirmFreeQty " +								
														" from sordform a,sordformdet b	where a.TRAN_ID = b.TRAN_ID and a.site_code = ?	"
														+ " and a.cust_code = ? and a.TRAN_ID <> ? and a.order_date between ? and ?"
														+ "	and b.item_code = ?"
														+ " and (case when a.status is null then 'N' else trim(a.status) end )= 'N'	and b.nature in ('I')";

												pstmt1 = conn.prepareStatement(sql);
												pstmt1.setString(1, siteCode);
												pstmt1.setString(2, custCode);
												pstmt1.setString(3, tranId);
												pstmt1.setTimestamp(4, OrderDate);
												pstmt1.setTimestamp(5, OrderDate);
												pstmt1.setString(6, itemCode);
												rs1 = pstmt1.executeQuery();
												if (rs1.next()) {
												
													unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
													System.out.println("unConfTotFreeQty separte free" + unConfTotFreeQty);
													
												}
												pstmt1.close();
												rs1.close();
												pstmt1 = null;
												rs1 = null;
												
												//---------------------------
												Node currDetail1 = null;
												prvFreeQty = 0;
												int count = 0;
												currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
												NodeList detailList1 = dom2.getElementsByTagName("Detail2");
												
												int noOfDetails = detailList1.getLength();
												
												for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

													currDetail1 = detailList1.item(ctr1);
													
													lineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", currDetail1));
													nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
													browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
													quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("totqty", currDetail1));
												
													System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);
													
													System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");
												
													if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
														System.out.println("Insideif00000forSCHEME_BALANCE ");
															if (nature.equals("I") && (browItemCode.equalsIgnoreCase(itemCode))) {
																prvFreeQty = prvFreeQty + quantity;

															}
															System.out.println(
																	"prvFreeQty insdie V[" + prvFreeQty+ "]");
														
													}	
												}	
												
												if ((qtyTot +unConfTotFreeQty + prvFreeQty ) > freeQty) {
													errCode = "VTFREEQTY1";// Entered free quantity is
													errString = getErrorString( childNodeName, errCode, userId ); 
														
													System.out.println(
															"Entered free quantity is greater than sCHEME_BALANCE quantity");
													break;
												}
							        }
									else
									{
										errCode = "VTQTYSCBAL";// Entered free quantity is
										// greater than scheme's free
										// quantity
										errString = getErrorString( childNodeName, errCode, userId ); 
										
										System.out.println(
												"Entered free quantity is greater than sCHEME_BALANCE quantity");
										break;
									}
						
							
							
						 }*/
						}
//						---------------------------- Nandkumar Gadkari -------------on 12/11/18----------end----------------------
					}// end for
			} //END switch
		}//END TRY
		catch(Exception e)
		{
			//System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException( e );
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					if(rs1 != null )rs.close();
					rs = null;
					rs1 = null;
					if(pstmt != null )pstmt.close();
					pstmt =null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
				{
				  d.printStackTrace();
				  throw new ITMException( d );
				}
			//System.out.println("[SOrderFormEJB] Connection is Closed");
		}
		//System.out.println("ErrString ::"+errString);
		return errString;
	}//END OF VALIDATION
	
	//public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			// if (xmlString2.trim().length() > 0 )
			// {
				// dom2 = parseString(xmlString2);
			// }
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
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
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			 if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
			 }
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			
			throw new ITMException(e);
		}
        return valueXmlString;
	}
	//public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();

		DecimalFormat deciFormater = new DecimalFormat("0.00");

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null , pstmt1 = null,pstmt2 = null;
		ResultSet rs = null ,rs1 = null,rs2=null ;
		int ctr=0;
		String childNodeName = null;
		String columnValue = null;
		String Col_name = "";
		int currentFormNo = 0 ,cnt = 0;
		String cadreCode="" ;
		String sql = "",descr="",locCode = "",empCode="",locDescr="",sql1 = "";
		String siteCode = null;
		String empLname = null,empMname = null,empFname = null;
		String itemDescr = null,itemCode = null;
		ConnDriver connDriver = new ConnDriver();
		Date today = null;
		Timestamp todayDate = null;
		Timestamp todayDateDb = null;
		String todayStr = null;
		String loginSiteCode = null;
		String siteDescr = null;
		String tranId = null;
		String siteCodeShip = null;
		String custCode = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String custName = null;
		String transMode  = null;
		String creditLmt  = null;
		String addr1  = null;
		String addr2  = null;
		String addr3  = null;
		String city  = null;
		String stateDescr  = null;
		String countryDescr  = null;
		String orderType  = null;
		String totAmtAdjAmt = null;
		String orderDate = null;
		String sch[] = new String[20];
		double qty[] = new double[20];
		double totQty = 0;
		String qtyStr[] = new String[20];
		double LcRatio[] = new double[20];
		String lcRatio[] = new String[20];
		String priceList = null;
		int maxScheduleNo = 0;
		double rate = 0.0;
		String totqtyStr = null;		
		String inCtrStr =null;
		int inCtr = 0;
		int noOfSchedule = 0;
		double totDiff = 0.0;
		double totalRatio = 0.0;
		String schRatioStr[] = new String[20];
		double schRatio[] = new double[20];
		double distQty = 0.0;
		String listType = null;
		String maxRefNoStr = "";
		String itemSerProm = null;
		String itemSerCrPerc = null;
		String mitemSer = null;
		String lsItemSer = null;
		//String descr = null;
		String unit = null;
		String locType = null;
		String siteString = null;
		String disparmLoc = null;
		String schemeCode  = null;
		String lscustschemecode = null;
		String custCd  = null;
		String stateCd  = null;
		String lsItemCode  = null;
		String lsType  = null;
		String lsOrderType  = null;
		String lsItemStru  = null;
		double lcAvailStk = 0.0;
		//String lsItemStru  = null;
		String lsSchemeCode  = null;
		String mslabOn  = null;
		String lsDescr  = null;
		String lcRate  = null;
		//String lsType  = null;
		String lsPriceList  = null;
		String lsCustItemCodeRef  = null;
		String strSch  = null;	
		String strSchratio  = null;
		String strQty = null;
		double qtyTotal	 =0.0;
		double OrdValue	 =0.0;
		String orderDateStr = null;
		Timestamp OrderDate = null;		
		String custItemRef = null;		
		String  itemCodeDescr = null;
		Timestamp tranDate = null;
		String tranDateStr = null;
		String itemSer = null;
		String code = null ;		
		String lsCustItemCodeDescr = null;		
		int maxRefNo = 0;
		DistCommon distCommon = null;
		distCommon = new DistCommon();	
		String 	applicableordTypes	=null;		
		boolean lbProceed = false;
		String lsCurscheme = null;
		String lsToken = null;
		String applicableordtypes = null;
		String noapplyCustList = null;
		String applyCustList = null;
		String SchemeCode = null;
		String lsPrevscheme = null;
		String mcustCd = null;
		String applyCust = null;
		String noapplyCust = null;
		int llSchcnt = 0;
		int mcount = 0;	
		int count = 0;			
		String userId = "";
		String aLotNo=null;		     
		String priceListParent =null;		
		double integralQty = 0.0;
		double acintegralQty = 0.0;
		double shipperQty = 0.0;		
		double ordValue =0.0;
		double noArt =0.0;
		double noArt1 =0.0;
		double noArt2 =0.0;
		double balQty =0;
		double intQty =0.0;
		double looseQty =0.0;
		double acIntegralQty =0.0;
		String lsStr = null,maxOrdVal="";
		double acShipperQty = 0.0;
		double sumRatio=0;	
		String loginSiteDescr = null;
		String userType = null;
		String quantityStr = "";
		int pos = 0;
		String totQtyStr = "0";
		String disparmVal = null;
		String salesPers = "";
		String Descr= null;
		int cnt1= 0;
		try
		{
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			//System.out.println("FORM NO:::"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			//System.out.println(".....call item change ....");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver=null;
			sql = "select var_value"
					+" from	disparm"
					+" where var_name='MAX_NO_SCHEDULE'"
					+" and		prd_code='999999'";
			//System.out.println("select qry from maxScheduleNo.." + sql);
			pstmt= conn.prepareStatement(sql);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
			   maxScheduleNo  = rs.getInt(1) ;
			}
			else
			{
				maxScheduleNo = 12;
			}
			//System.out.println("maxScheduleNodescr.." + maxScheduleNo);
							rs.close();
							pstmt.close();
			pstmt = null;
			rs = null; 
			switch(currentFormNo)
			{			
				
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					valueXmlString.append("<Detail1>");
					int childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					if (currentColumn.trim().equals("itm_default") )			
					{	
						//System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
						loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
						 
						////System.out.println("loginSiteCode :::::::::::::::::::::::::::::::::: "+loginSiteCode);
						userType = getValueFromXTRA_PARAMS(xtraParams,"userType");
						userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
						//System.out.println("userId :::::::::::::::::::::::::::::::::: "+userId);
						String currAppdate ="";
						java.sql.Timestamp currDate = null;
						currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
						//System.out.println("\n today date----" + currDate);
						currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
						valueXmlString.append("<order_date protect =\"1\">").append(currAppdate).append("</order_date>");
						//valueXmlString.append("<order_date protect =\"1\">").append("</order_date>");
						//todayDateDb =  Timestamp.valueOf(currAppdate +" 00:00:00.00");		
						
						//get login site description
						sql = " select descr from site where site_code = ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, loginSiteCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
						   loginSiteDescr = rs.getString( "descr" ); 
						}
							rs.close();
							pstmt.close();
						pstmt = null;
						rs = null;
						//end getting login site description
						
						if( "C".equalsIgnoreCase( userType ) )
						{
							sql = " select u.entity_code,s.site_code "
										+ " from users u,site_customer s "
										+ " where u.entity_code = s.cust_code"
										+ " and code = ?"
										+ " group by s.site_code,u.entity_code";
							//System.out.println("select qry from cust and site.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,userId);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   custCode = rs.getString(1) == null ? "" : rs.getString(1);
							   siteCode = rs.getString(2) == null ? "" : rs.getString(2);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							
							//System.out.println("custCode:- " + custCode + " siteCode:- " + siteCode);
							
							sql = "select count(*) from site_customer where cust_code=?";
							//System.out.println("select qry from cust and site.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   count = rs.getInt(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if( count > 1)
							{
								sql = " select site_code "
										+ " from site_customer "
										+ " where primary_site = 'Y' "
										+ " and cust_code = ?";
								//System.out.println("select qry for multi site.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   siteCode = rs.getString(1) == null ? "" : rs.getString(1);
								}
							rs.close();
							pstmt.close();
								pstmt = null;
								rs = null;
							}
							sql = "Select  descr from site where site_code =?";
							//System.out.println("select descr from site.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   siteDescr = rs.getString(1) == null ? "" : rs.getString(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null; 

							valueXmlString.append("<cust_code  isSrvCallOnChg='1' protect =\"1\">").append(custCode).append("</cust_code>");
						}
						
						else
						{
							valueXmlString.append("<cust_code  isSrvCallOnChg='0' protect =\"0\">").append( "" ).append("</cust_code>");
							siteCode = loginSiteCode;
							siteDescr = loginSiteDescr ; 

						}
						//System.out.println("siteDescr .." + siteDescr);
						//valueXmlString.append("<quantity isSrvCallOnChg='1'>").append(pickQty).append("</quantity>");
						if( "C".equalsIgnoreCase( userType ) )
						{
							valueXmlString.append("<site_code protect =\"1\">").append(siteCode).append("</site_code>");
						    valueXmlString.append("<site_code__ship protect =\"1\">").append(siteCode).append("</site_code__ship>");
							valueXmlString.append("<site_descr>").append(siteDescr).append("</site_descr>");
							valueXmlString.append("<descr>").append(siteDescr).append("</descr>");
						}else
						{
							valueXmlString.append("<site_code protect =\"1\">").append(loginSiteCode).append("</site_code>");
						    valueXmlString.append("<site_code__ship protect =\"1\">").append(loginSiteCode).append("</site_code__ship>");
							valueXmlString.append("<site_descr>").append(loginSiteDescr).append("</site_descr>");
							valueXmlString.append("<descr>").append(loginSiteDescr).append("</descr>");						
						}

						//System.out.println("\n today date set ----" + currAppdate);
						valueXmlString.append("<pord_date>").append(currAppdate).append("</pord_date>");
						valueXmlString.append("<sch_1>").append(currAppdate).append("</sch_1>");
						valueXmlString.append("<sch_ratio_1>").append("" + 100).append("</sch_ratio_1>");
						
						if( "C".equalsIgnoreCase( userType ) )
						{

							sql = " select customer.cust_name,customer.trans_mode,customer.credit_lmt,"
										+ " customer.addr1,customer.addr2,customer.addr3,"
										+ " customer.city,state.descr,country.descr,"
										+ " customer.order_type  "
										+ " from customer customer,state state,country country"
										+ " where customer.state_code = state.state_code"
										+ " and state.count_code = country.count_code "
										+ " and customer.cust_code = ?";
							//System.out.println("select qry from customer.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
								custName = rs.getString(1) == null ? "" : rs.getString(1);
								transMode = rs.getString(2) == null ? "" : rs.getString(2);
								creditLmt = rs.getString(3) == null ? "0" : rs.getString(3);
								addr1 = rs.getString(4) == null ? "" : rs.getString(4);
								addr2 = rs.getString(5) == null ? "" : rs.getString(5);
								addr3 = rs.getString(6) == null ? "" : rs.getString(6);
								city = rs.getString(7) == null ? "" : rs.getString(7);
								stateDescr = rs.getString(8) == null ? "" : rs.getString(8);
								countryDescr = rs.getString(9) == null ? "" : rs.getString(9);
								orderType = rs.getString(10) == null ? "" : rs.getString(10);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							valueXmlString.append("<name>").append("<![CDATA["+custName.trim()+"]]>").append("</name>");
							if( transMode != null)
							{
								valueXmlString.append("<trans_mode>").append("<![CDATA["+transMode.trim()+"]]>").append("</trans_mode>");
							}
							sql = "select sum(tot_amt-adj_amt) from receivables where cust_code = ?";
							//System.out.println("select qry from receivables.." + sql);
							pstmt= conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery(); 
							if(rs.next())
							{
							   totAmtAdjAmt = rs.getString(1) == null ? "0.00" : rs.getString(1);
							}
							 //System.out.println("totAmtAdjAmt .." + totAmtAdjAmt);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							
							double creditLmtDbl = Double.parseDouble(creditLmt.trim());
							double totAmtAdjAmtDbl = Double.parseDouble(totAmtAdjAmt.trim());
							creditLmtDbl = Double.parseDouble(df.format(creditLmtDbl));
							totAmtAdjAmtDbl = Double.parseDouble(df.format(totAmtAdjAmtDbl));
							//valueXmlString.append("<st_cust_outs>").append("Customer Outstanding : Rs.").append("<![CDATA["+totAmtAdjAmt.trim()+"]]>").append("</st_cust_outs>");
							valueXmlString.append("<st_cr_limit>").append("<![CDATA["+creditLmtDbl+"]]>").append("</st_cr_limit>");
							valueXmlString.append("<st_cust_outs>").append("<![CDATA["+totAmtAdjAmtDbl+"]]>").append("</st_cust_outs>");
							valueXmlString.append("<customer_addr1>").append("<![CDATA["+addr1.trim()+"]]>").append("</customer_addr1>");
							valueXmlString.append("<customer_addr2>").append("<![CDATA["+addr2.trim()+"]]>").append("</customer_addr2>");
							valueXmlString.append("<customer_addr3>").append("<![CDATA["+addr3.trim()+"]]>").append("</customer_addr3>");
							valueXmlString.append("<customer_city>").append("<![CDATA["+city.trim()+"]]>").append("</customer_city>");
							valueXmlString.append("<state_descr>").append("<![CDATA["+stateDescr.trim()+"]]>").append("</state_descr>");
							valueXmlString.append("<country_descr>").append("<![CDATA["+countryDescr.trim()+"]]>").append("</country_descr>");
							//valueXmlString.append("<order_type>").append("<![CDATA["+orderType.trim()+"]]>").append("</order_type>");
							//System.out.println("valueXmlString returned by itm_default .." + valueXmlString);

							

							valueXmlString.append("<item_ser protect =\"1\">").append("</item_ser>");
							valueXmlString.append("<order_type protect =\"1\">").append("F").append("</order_type>");	
							valueXmlString.append("<trans_mode protect =\"1\">").append("</trans_mode>");	
							valueXmlString.append("<max_ord_value protect =\"1\">").append("</max_ord_value>");	
						}
					
						else
						{
							
							valueXmlString.append("<item_ser protect =\"0\">").append("</item_ser>");
							valueXmlString.append("<order_type protect =\"0\">").append("F").append("</order_type>");	
							valueXmlString.append("<trans_mode protect =\"0\">").append("</trans_mode>");	
							valueXmlString.append("<max_ord_value protect =\"0\">").append("</max_ord_value>");	
						}
						disparmVal = distCommon.getDisparams("999999","SALES_BANGLA",conn);
						System.out.println("getDisparams()....disparmVal..."+disparmVal);								
						disparmVal = disparmVal == null ?" " : disparmVal.trim();
						if(disparmVal != null && disparmVal.trim().length() > 0)
						{
							if(disparmVal=="NULLFOUND")
							{
								disparmVal = "N";
							}
							if(disparmVal.trim().equalsIgnoreCase("Y"))
							{
								valueXmlString.append("<order_type protect =\"1\">").append("F").append("</order_type>");	
								valueXmlString.append("<sales_pers protect =\"1\">").append("<![CDATA["+salesPers+"]]>").append("</sales_pers>");
								
							}
						
					 
						
						

					}
					}
					else if (currentColumn.trim().equals("itm_defaultedit") )			
					{	
						//start getting no of detail lines.
						//If there is no detail then make the fields enabled
						int detCount = 0;
						String tranIdSOrd = genericUtility.getColumnValue( "tran_id", dom );
						sql = "Select count(1) cnt from sordformdet where TRAN_ID = ? ";
						//System.out.println("select descr from site.." + sql);
						pstmt= conn.prepareStatement(sql);
						pstmt.setString( 1, tranIdSOrd );
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						   detCount = rs.getInt( "cnt" );
						}
						//System.out.println("siteDescr .." + siteDescr);
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null; 
						//end getting no of detail lines
					
						//System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
						//loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
						//System.out.println("loginSiteCode :::::::::::::::::::::::::::::::::: "+loginSiteCode);
						userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
						//System.out.println("userId :::::::::::::::::::::::::::::::::: "+userId);
						String currAppdate ="";
						java.sql.Timestamp currDate = null;
						currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
						//System.out.println("\n today date----" + currDate);
						currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
						///valueXmlString.append("<order_date protect =\"1\">").append(currAppdate).append("</order_date>");
						//valueXmlString.append("<order_date protect =\"1\">").append("</order_date>");
						//todayDateDb =  Timestamp.valueOf(currAppdate +" 00:00:00.00");						
						siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						custCode = genericUtility.getColumnValue("cust_code",dom);
						
						valueXmlString.append("<cust_code  isSrvCallOnChg='1' protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append(custCode).append("</cust_code>");
						sql = "Select  descr from site where site_code =?";
						//System.out.println("select descr from site.." + sql);
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						   siteDescr = rs.getString(1) == null ? "" : rs.getString(1);
						}
						//System.out.println("siteDescr .." + siteDescr);
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null; 
						//valueXmlString.append("<quantity isSrvCallOnChg='1'>").append(pickQty).append("</quantity>");
						//valueXmlString.append("<site_code protect =\"1\">").append(siteCode).append("</site_code>");
					    //valueXmlString.append("<site_code__ship protect =\"1\">").append(siteCode).append("</site_code__ship>");
						valueXmlString.append("<site_code protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append(siteCode).append("</site_code>");
					    valueXmlString.append("<site_code__ship protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append(siteCode).append("</site_code__ship>");
						valueXmlString.append("<site_descr>").append(siteDescr).append("</site_descr>");
						valueXmlString.append("<descr>").append(siteDescr).append("</descr>");
						//System.out.println("\n today date set ----" + currAppdate);
						//valueXmlString.append("<pord_date>").append(currAppdate).append("</pord_date>");
						//valueXmlString.append("<sch_1>").append(currAppdate).append("</sch_1>");
						//valueXmlString.append("<sch_ratio_1>").append("" + 100).append("</sch_ratio_1>");
						
						//valueXmlString.append("<item_ser protect =\"1\">").append("</item_ser>");
						valueXmlString.append("<item_ser protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append("</item_ser>");
						
						
						orderType = genericUtility.getColumnValue("trans_mode",dom);
						maxOrdVal = genericUtility.getColumnValue("max_ord_value",dom);
						
						//valueXmlString.append("<trans_mode protect =\"1\">").append(orderType).append("</trans_mode>");	
						//valueXmlString.append("<max_ord_value protect =\"1\">").append("</max_ord_value>");	
						valueXmlString.append("<trans_mode protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append(orderType).append("</trans_mode>");	
						valueXmlString.append("<max_ord_value protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append(maxOrdVal).append("</max_ord_value>");	

						//changed by rajendra for retain old value 
						orderType = genericUtility.getColumnValue("order_type",dom);
						//System.out.println("orderType.........."+orderType);
						if(orderType != null)
						{
						
						   //valueXmlString.append("<order_type protect =\"1\">").append(orderType).append("</order_type>");	
						   valueXmlString.append("<order_type protect =\"" + ( detCount > 0 ? "1" : "0" ) + "\">").append(orderType).append("</order_type>");	
						}
						disparmVal = distCommon.getDisparams("999999","SALES_BANGLA",conn);
						System.out.println("getDisparams()....disparmVal..."+disparmVal);								
						disparmVal = disparmVal == null ?" " : disparmVal.trim();
						if(disparmVal != null && disparmVal.trim().length() > 0)
						{
							if(disparmVal=="NULLFOUND")
							{
								disparmVal = "N";
							}
							if(disparmVal.trim().equalsIgnoreCase("Y"))
							{
								valueXmlString.append("<order_type protect =\"1\">").append("F").append("</order_type>");	
								
								
							}
						
					 
					}
						disparmVal = distCommon.getDisparams("999999","SALES_BANGLA",conn);
						System.out.println("getDisparams()....disparmVal..."+disparmVal);								
						disparmVal = disparmVal == null ?" " : disparmVal.trim();
						if(disparmVal != null && disparmVal.trim().length() > 0)
						{
							if(disparmVal=="NULLFOUND")
							{
								disparmVal = "N";
							}
							if(disparmVal.trim().equalsIgnoreCase("Y"))
							{
								custCode= genericUtility.getColumnValue( "cust_code", dom );
								sql="select sales_pers from customer where cust_code = ?";
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
									salesPers =rs.getString(1) == null ? "" : rs.getString(1);
								
								
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								
								
								valueXmlString.append("<sales_pers protect =\"1\">").append("<![CDATA["+salesPers+"]]>").append("</sales_pers>");
								
						}
					
					

				}
						
						/* sql = " select customer.cust_name,customer.trans_mode,customer.credit_lmt,"
									+ " customer.addr1,customer.addr2,customer.addr3,"
									+ " customer.city,state.descr,country.descr,"
									+ " customer.order_type  "
									+ " from customer customer,state state,country country"
									+ " where customer.state_code = state.state_code"
									+ " and state.count_code = country.count_code "
									+ " and customer.cust_code = '" + custCode + "'";
						System.out.println("select qry from customer.." + sql);
						pstmt= conn.prepareStatement(sql);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						    custName = rs.getString(1) == null ? "" : rs.getString(1);
						    transMode = rs.getString(2) == null ? "" : rs.getString(2);
						    creditLmt = rs.getString(3) == null ? "" : rs.getString(3);
						    addr1 = rs.getString(4) == null ? "" : rs.getString(4);
							addr2 = rs.getString(5) == null ? "" : rs.getString(5);
							addr3 = rs.getString(6) == null ? "" : rs.getString(6);
							city = rs.getString(7) == null ? "" : rs.getString(7);
							stateDescr = rs.getString(8) == null ? "" : rs.getString(8);
							countryDescr = rs.getString(9) == null ? "" : rs.getString(9);
							orderType = rs.getString(10) == null ? "" : rs.getString(10);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<name>").append("<![CDATA["+custName.trim()+"]]>").append("</name>");
						if( transMode != null)
						{
							valueXmlString.append("<trans_mode>").append("<![CDATA["+transMode.trim()+"]]>").append("</trans_mode>");
						}
						sql = "select sum(tot_amt-adj_amt) from receivables where cust_code = '" + custCode + "'";
						System.out.println("select qry from receivables.." + sql);
						pstmt= conn.prepareStatement(sql);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						   totAmtAdjAmt = rs.getString(1) == null ? "0.00" : rs.getString(1);
						}
						 System.out.println("totAmtAdjAmt .." + totAmtAdjAmt);
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<st_cust_outs>").append("Customer Outstanding : Rs.").append("<![CDATA["+totAmtAdjAmt.trim()+"]]>").append("</st_cust_outs>");
						valueXmlString.append("<customer_addr1>").append("<![CDATA["+addr1.trim()+"]]>").append("</customer_addr1>");
						valueXmlString.append("<customer_addr2>").append("<![CDATA["+addr2.trim()+"]]>").append("</customer_addr2>");
						valueXmlString.append("<customer_addr3>").append("<![CDATA["+addr3.trim()+"]]>").append("</customer_addr3>");
						valueXmlString.append("<customer_city>").append("<![CDATA["+city.trim()+"]]>").append("</customer_city>");
						valueXmlString.append("<state_descr>").append("<![CDATA["+stateDescr.trim()+"]]>").append("</state_descr>");
						valueXmlString.append("<country_descr>").append("<![CDATA["+countryDescr.trim()+"]]>").append("</country_descr>");
						//valueXmlString.append("<order_type>").append("<![CDATA["+orderType.trim()+"]]>").append("</order_type>");
						 */
						 //System.out.println("valueXmlString returned by itm_default .." + valueXmlString);
					}
					else if(currentColumn.trim().equalsIgnoreCase("site_code__ship"))
					{
						siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
						siteCodeShip = siteCodeShip == null ? "" : siteCodeShip;
						sql = "Select  descr from site where site_code =?";
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeShip);
						rs = pstmt.executeQuery(); 
						siteDescr = "";
						if(rs.next())
						{
						   siteDescr = rs.getString(1) == null ? "" : rs.getString(1);
						}
						//System.out.println("siteDescr .." + siteDescr);
							rs.close();
							pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<site_descr>").append("<![CDATA["+siteDescr.trim()+"]]>").append("</site_descr>");
					}														
					else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
					{
						creditLmt = "";totAmtAdjAmt="";addr1="";addr2="";addr3="";city="";stateDescr="";countryDescr="";orderType="";
						custCode = genericUtility.getColumnValue("cust_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						custCode = custCode == null ? "" : custCode;
						siteCode = siteCode == null ? "" : siteCode;
						sql = " select customer.cust_name,customer.trans_mode,customer.credit_lmt,"
									+ " customer.addr1,customer.addr2,customer.addr3,"
									+ " customer.city,state.descr,country.descr,"
									+ " customer.order_type  "
									+ " from customer customer,state state,country country"
									+ " where customer.state_code = state.state_code"
									+ " and state.count_code = country.count_code "
									+ " and customer.cust_code = ?";
						//System.out.println("select qry from customer.." + sql);
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						    custName = rs.getString(1) == null ? "" : rs.getString(1);
						    transMode = rs.getString(2) == null ? "" : rs.getString(2);
						    creditLmt = rs.getString(3) == null ? "0" : rs.getString(3);
						    addr1 = rs.getString(4) == null ? "" : rs.getString(4);
							addr2 = rs.getString(5) == null ? "" : rs.getString(5);
							addr3 = rs.getString(6) == null ? "" : rs.getString(6);
							city = rs.getString(7) == null ? "" : rs.getString(7);
							stateDescr = rs.getString(8) == null ? "" : rs.getString(8);
							countryDescr = rs.getString(9) == null ? "" : rs.getString(9);
							orderType = rs.getString(10) == null ? "" : rs.getString(10);
							valueXmlString.append("<name>").append("<![CDATA["+custName.trim()+"]]>").append("</name>");
						}
							rs.close();
							pstmt.close();
						pstmt = null;
						rs = null;
						if( transMode != null)
						{
							valueXmlString.append("<trans_mode>").append("<![CDATA["+transMode.trim()+"]]>").append("</trans_mode>");
						}
						sql = "select sum(tot_amt-adj_amt) from receivables where cust_code = ?";
						//System.out.println("select qry from receivables.." + sql);
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						   totAmtAdjAmt = rs.getString(1) == null ? "0.00" : rs.getString(1);
						}
						 //System.out.println("totAmtAdjAmt .." + totAmtAdjAmt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
					//Modified by Anjali R. on[01/10/2018][Start]
					//double creditLmtDbl = Double.parseDouble(creditLmt.trim());
					//double totAmtAdjAmtDbl = Double.parseDouble(totAmtAdjAmt.trim());
					double creditLmtDbl = 0.0;
					double totAmtAdjAmtDbl = 0.0;
					//creditLmt = "";totAmtAdjAmt="";addr1="";addr2="";addr3="";city="";stateDescr="";countryDescr="";orderType="";
					
					if(creditLmt != null && creditLmt.trim().length() >0)
					{
						creditLmtDbl = Double.parseDouble(creditLmt.trim());
					}
					if(totAmtAdjAmt != null && totAmtAdjAmt.trim().length() > 0)
					{
						totAmtAdjAmtDbl = Double.parseDouble(totAmtAdjAmt.trim());
					}
					//Modified by Anjali R. on[01/10/2018][End]
						creditLmtDbl = Double.parseDouble(df.format(creditLmtDbl));
						totAmtAdjAmtDbl = Double.parseDouble(df.format(totAmtAdjAmtDbl));
						//valueXmlString.append("<st_cust_outs>").append("Customer Outstanding : Rs.").append("<![CDATA["+totAmtAdjAmt.trim()+"]]>").append("</st_cust_outs>");
						valueXmlString.append("<st_cr_limit>").append("<![CDATA["+creditLmtDbl+"]]>").append("</st_cr_limit>");
						valueXmlString.append("<st_cust_outs>").append("<![CDATA["+totAmtAdjAmtDbl+"]]>").append("</st_cust_outs>");
						valueXmlString.append("<customer_addr1>").append("<![CDATA["+addr1.trim()+"]]>").append("</customer_addr1>");
						valueXmlString.append("<customer_addr2>").append("<![CDATA["+addr2.trim()+"]]>").append("</customer_addr2>");
						valueXmlString.append("<customer_addr3>").append("<![CDATA["+addr3.trim()+"]]>").append("</customer_addr3>");
						valueXmlString.append("<customer_city>").append("<![CDATA["+city.trim()+"]]>").append("</customer_city>");
						valueXmlString.append("<state_descr>").append("<![CDATA["+stateDescr.trim()+"]]>").append("</state_descr>");
						valueXmlString.append("<country_descr>").append("<![CDATA["+countryDescr.trim()+"]]>").append("</country_descr>");
						valueXmlString.append("<order_type>").append("<![CDATA["+orderType.trim()+"]]>").append("</order_type>");
						disparmVal = distCommon.getDisparams("999999","SALES_BANGLA",conn);
						System.out.println("getDisparams()....disparmVal..."+disparmVal);								
						disparmVal = disparmVal == null ?" " : disparmVal.trim();
						if(disparmVal != null && disparmVal.trim().length() > 0)
						{
							if(disparmVal=="NULLFOUND")
							{
								disparmVal = "N";
							}
							if(disparmVal.trim().equalsIgnoreCase("Y"))
							{
								custCode= genericUtility.getColumnValue( "cust_code", dom );
								sql="select sales_pers from customer where cust_code = ?";
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
									salesPers = rs.getString(1) == null ? "" : rs.getString(1);
								
								
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								
								
								valueXmlString.append("<sales_pers protect =\"1\">").append("<![CDATA["+salesPers+"]]>").append("</sales_pers>");
								
						}
					
					

				}
						
					}
					else if(currentColumn.trim().equalsIgnoreCase("site_code"))
					{
						siteCode = genericUtility.getColumnValue("site_code",dom);
						siteCode = siteCode == null ? "" : siteCode;
						sql = "Select  descr from site where site_code =?";
						//System.out.println("select site_code of detail 1  qry from location.." + sql);
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						   siteDescr = rs.getString(1) == null ? "" : rs.getString(1);
						}
						//System.out.println("siteDescr .." + siteDescr);
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<descr>").append("<![CDATA["+siteDescr.trim()+"]]>").append("</descr>");
					} 
					else if(currentColumn.trim().equalsIgnoreCase("order_date"))
					{
						orderDateStr = genericUtility.getColumnValue("order_date",dom);
						orderDateStr = orderDateStr == null ? "" : orderDateStr;	
						valueXmlString.append("<pord_date>").append("<![CDATA["+orderDateStr.trim()+"]]>").append("</pord_date>");
						valueXmlString.append("<sch_1>").append("<![CDATA["+orderDateStr.trim()+"]]>").append("</sch_1>");
					}
					else if(currentColumn.trim().startsWith("sch_ratio_"))
					{
						double schRat  = 0.0;
						String kval = currentColumn.substring(10);
						int k = 0;
						//System.out.println("currentColumn>>> " + currentColumn + "kval>>>>>>>>. " + kval);
						if(kval != null && kval.trim().length() > 0)
						{
							k = Integer.parseInt(kval);
						}
						//System.out.println("k>>>>>>>>. " + k);
						strSch = "";
						strSchratio="";	
						//System.out.println("item change called for schedule date................");
						for(int i = 1; i < (maxScheduleNo +1) ; i++)
						{
							strSch = "sch_"+(i);							
							sch[i] = genericUtility.getColumnValue(strSch , dom);
							//System.out.println("sch["+i+"]..."+sch[i]);	
							sumRatio=0.0;
							for(int j = 2; j < (maxScheduleNo + 1) ; j++)
							{								
								if( j == i) continue;
								strSchratio = "sch_ratio_"+(j);
								//System.out.println("strSchratio ..."+strSchratio);							
								lcRatio[j] = genericUtility.getColumnValue( strSchratio,dom);								
								//System.out.println(strSchratio + "value>>>  "+lcRatio[j]);															
								LcRatio[j] = Double.parseDouble(lcRatio[j]);
								sumRatio = sumRatio + LcRatio[j] ;
							}
							totalRatio = sumRatio + LcRatio[i] ;	
							//System.out.println("totalRatio ......... " + totalRatio);
							if( sch[i] == null)
							{	
								//System.out.println("protecting .........>>>>>> " + (i));
								valueXmlString.append("<sch_ratio_"+ (i) + " protect =\"1\">").append("<![CDATA[0]]>").append("</sch_ratio_"+ (i)  + ">");
							}
							else
							{
								
								 while(totalRatio > 100)
								{
									 totalRatio = totalRatio - 100;
								}
								//System.out.println("totalRatio before....... :- " + totalRatio + "\n scheduled ratio before......... " + schRat ) ;	
								if(totalRatio <= 100)
								{
									schRat = 100 - totalRatio;
								}
								
								//System.out.println("totalRatio...... :- " + totalRatio + "\n scheduled ratio......... " + schRat ) ;	
								//System.out.println("setting:- " + schRat + "in schedule ratio no :- " + k);
								//if(i != 1)
								//{
									valueXmlString.append("<sch_ratio_1 protect =\"0\">").append("<![CDATA["+schRat+"]]>").append("</sch_ratio_1>");
									
									//valueXmlString.append("<sch_ratio_"+ k + " protect =\"0\">").append("<![CDATA[0]]>").append("</sch_ratio_"+ k + ">");
								//}
								////System.out.println("setting in 1 scheduled ratio......... " + schRat ) ;	
								//valueXmlString.append("<sch_ratio_1 protect =\"0\">").append("<![CDATA["+schRat+"]]>").append("</sch_ratio_1>");
							}
							
						}//end for
						
					}
					else if(currentColumn.trim().startsWith("sch_"))
					{
						double schRat  = 0.0;
						String kval = currentColumn.substring(4);
						int k = 0;
						//System.out.println("currentColumn>>> " + currentColumn + "kval>>>>>>>>. " + kval);
						if(kval != null && kval.trim().length() > 0)
						{
							k = Integer.parseInt(kval);
						}
						//System.out.println("k>>>>>>>>. " + k);
						strSch = "";
						strSchratio="";	
						//System.out.println("item change called for schedule date................");
						for(int i = 1; i < (maxScheduleNo +1) ; i++)
						{
							strSch = "sch_"+(i);							
							sch[i] = genericUtility.getColumnValue(strSch , dom);
							System.out.println("sch["+i+"]..."+sch[i]);	
							sumRatio=0.0;
							for(int j = 2; j < (maxScheduleNo + 1) ; j++)
							{								
								if( j == i) continue;
								strSchratio = "sch_ratio_"+(j);
								////System.out.println("strSchratio ..."+strSchratio);							
								lcRatio[j] = genericUtility.getColumnValue( strSchratio,dom);								
								System.out.println(strSchratio + "value>>>  "+lcRatio[j]);															
								// 15/06/10 manoharan check null
								if (lcRatio[j] != null)
								{
									LcRatio[j] = Double.parseDouble(lcRatio[j]);
								}
								else
								{
									LcRatio[j] = 0;
								}
								// end 15/06/10 manoharan
								sumRatio = sumRatio + LcRatio[j] ;
							}
							totalRatio = sumRatio + LcRatio[i] ;	
							System.out.println("totalRatio ......... " + totalRatio);
							if( sch[i] == null)
							{	
								//System.out.println("protecting .........>>>>>> " + (i));
								valueXmlString.append("<sch_ratio_"+ (i) + " protect =\"1\">").append("<![CDATA[0]]>").append("</sch_ratio_"+ (i)  + ">");
							}
							else
							{
								//System.out.println("totalRatio > 100>>>>>>>>>>>>>>> " + (totalRatio > 100));
								 while(totalRatio > 100)
								{
									 totalRatio = totalRatio - 100;
								}
								//System.out.println("totalRatio before....... :- " + totalRatio + "\n scheduled ratio before......... " + schRat ) ;	
								if(totalRatio <= 100)
								{
									schRat = 100 - totalRatio;
								}
								
								//System.out.println("totalRatio...... :- " + totalRatio + "\n scheduled ratio......... " + schRat ) ;	
								//System.out.println("setting:- " + schRat + "in schedule ratio no :- " + k);
								//if(i != 1)
								//{
									valueXmlString.append("<sch_ratio_1 protect =\"0\">").append("<![CDATA["+schRat+"]]>").append("</sch_ratio_1>");
									valueXmlString.append("<sch_ratio_"+ k + " protect =\"0\">").append("<![CDATA[0]]>").append("</sch_ratio_"+ k + ">");
								//}
								////System.out.println("setting in 1 scheduled ratio......... " + schRat ) ;	
								//valueXmlString.append("<sch_ratio_1 protect =\"0\">").append("<![CDATA["+schRat+"]]>").append("</sch_ratio_1>");
							}
							
						}//end for
						
					}					
					valueXmlString.append("</Detail1>");					
					break;
					case 2:
						
						custCode=genericUtility.getColumnValue("cust_code", dom1);
				          siteCode=genericUtility.getColumnValue("site_code", dom1);
				          siteCodeShip=genericUtility.getColumnValue("site_code__ship", dom1);
				          itemSer = genericUtility.getColumnValue("item_ser", dom1);
				          orderType =	genericUtility.getColumnValue("order_type", dom1);
				          
				          valueXmlString.append("<Detail1>");
				          valueXmlString.append("<cust_code protect =\"1\">").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
				          valueXmlString.append("<site_code protect =\"1\">").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
				          valueXmlString.append("<site_code__ship protect =\"1\">").append("<![CDATA["+siteCodeShip+"]]>").append("</site_code__ship>");
				          valueXmlString.append("<itemSer protect =\"1\">").append("<![CDATA["+itemSer+"]]>").append("</itemSer>");
				          valueXmlString.append("<order_type protect =\"1\">").append("<![CDATA["+orderType+"]]>").append("</order_type>");
				          
				          valueXmlString.append("</Detail1>");
							parentNodeList = dom.getElementsByTagName("Detail2");
							parentNode = parentNodeList.item(0);
							childNodeList = parentNode.getChildNodes();
							ctr = 0;
							valueXmlString.append("<Detail2>");
							childNodeListLength = childNodeList.getLength();
							
							
							
							
							do
							{
								childNode = childNodeList.item(ctr);
								childNodeName = childNode.getNodeName();
								if(childNodeName.equals(currentColumn.trim()))
								{
									if (childNode.getFirstChild() != null)
									{
										columnValue = childNode.getFirstChild().getNodeValue();
									}
								}
								ctr++;
							}
						    while(ctr < childNodeListLength && !childNodeName.equals(currentColumn));
							//System.out.println("currentColumn..." + currentColumn);
							if(currentColumn.trim().equalsIgnoreCase("itm_default"))
							{
								noOfSchedule = NoOfSchedule(maxScheduleNo,dom,dom1);
								//valueXmlString.append(getSchDatesString(maxScheduleNo, dom1));
								siteCode = genericUtility.getColumnValue("site_code__ship", dom1);
								sql = "Select  descr from site where site_code =?";
								//System.out.println("select descr from site.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,siteCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   siteDescr = rs.getString(1) == null ? "" : rs.getString(1);
								}
								//System.out.println("siteDescr .." + siteDescr);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null; 
								// 21/03/09 manoharan 
								//for(int i = 1; i <= maxScheduleNo; i++)
								/*for(int i = maxScheduleNo + 1; i <= 12; i++) // 21/03/09 manoharan
								{
									valueXmlString.append("<qty_"+ i + " protect =\"1\">").append("</qty_"+ i + ">");
								}*/
								
									for(int i = 1; i <= maxScheduleNo + 1; i++)
									{
										//if (i <= maxScheduleNo)
										//{
											strSch = "sch_"+(i);
											quantityStr = "qty_"	+(i);	
											sch[i] = genericUtility.getColumnValue(strSch , dom1);
											qtyStr[i] = genericUtility.getColumnValue(quantityStr , dom);
										//}
										//System.out.println("sch["+i+"]..."+sch[i]);	
										//System.out.println("valu of ........... " +  quantityStr + " === "+qtyStr[i]);	
										if( sch[i] == null)
										{
											valueXmlString.append("<qty_"+ i + "_t protect =\"1\">").append("<![CDATA[]]>").append("</qty_"+ i + "_t>");
											valueXmlString.append("<qty_"+ i + " protect =\"1\">").append("<![CDATA[0]]>").append("</qty_"+ i + ">");
											//System.out.println("protecting qty.."+i);
										}
										else
										{
											if (qtyStr[i] == null)
											{
												qtyStr[i] = "0";
											}
											qty[i] = Double.parseDouble( qtyStr[i] );
											//System.out.println("adding  qty.."+qty[i]);
											// 21/03/09 manoharan
											valueXmlString.append("<qty_"+ i + "_t protect =\"1\">").append("<![CDATA[" + sch[i] + "]]>").append("</qty_"+ i + "_t>");
											valueXmlString.append("<qty_"+ i + " protect =\"0\">").append("<![CDATA[0]]>").append("</qty_"+ i + ">");
										}
									}
								// end 21/03/09 manoharan

								valueXmlString.append("<tax_type protect =\"1\">").append("</tax_type>");
								valueXmlString.append("<site_code__ship protect =\"1\">").append(siteCode).append("</site_code__ship>");
								valueXmlString.append("<site_descr>").append(siteDescr).append("</site_descr>");
								System.out.println( "In itmDefault nature " );
								valueXmlString.append( "<nature>" + "C" + "</nature>"  );
								//valueXmlString.append("<site_code protect =\"1\">").append(siteCode).append("</site_code>");
								//valueXmlString.append("<site_descr>").append(siteDescr).append("</site_descr>");
							} // end itm_default
							else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
							{	
								
								noOfSchedule = NoOfSchedule(maxScheduleNo,dom,dom1);
								//valueXmlString.append(getSchDatesString(maxScheduleNo, dom1));
								
								siteCode = genericUtility.getColumnValue("site_code",dom1);
								//valueXmlString.append("<site_code__ship protect =\"1\">").append(siteCode).append("</site_code__ship>");
								//valueXmlString.append("<site_code protect =\"1\">").append("</site_code>");
								//System.out.println("maxScheduleNo.."+maxScheduleNo);
								/* NodeList parNodeList = null,childqtyList= null;
								Node parentqtyNode = null,childqtyNode = null;
								parNodeList = dom2.getElementsByTagName("Detail2");
								//System.out.println("no of detail2 in parent>>>>>>>> " + parNodeList.getLength());
								 for(int u = 0 ; u < parNodeList.getLength() ; u ++)
								 {
									qtyTotal = 0; */
									//for(int i = 1; i <= maxScheduleNo; i++)
									//for(int i = maxScheduleNo + 1; i <= 12; i++) // 21/03/09 manoharan
									for(int i = 1; i < maxScheduleNo + 1; i++)
									{
										//System.out.println("sch["+i+"]..."+sch[i]);	
										//if (i <= maxScheduleNo)
										//{
											strSch = "sch_"+(i);
											quantityStr = "qty_"	+(i);	
											sch[i] = genericUtility.getColumnValue(strSch , dom1);
											qtyStr[i] = genericUtility.getColumnValue(quantityStr , dom);
										//}
										//System.out.println("sch["+i+"]..."+sch[i]);	
										//System.out.println("valu of ........... " +  quantityStr + " === "+qtyStr[i]);	
										if( sch[i] == null)
										{
											valueXmlString.append("<qty_"+ i + "_t protect =\"1\">").append("<![CDATA[]]>").append("</qty_"+ i + "_t>");
											valueXmlString.append("<qty_"+ i + " protect =\"1\">").append("<![CDATA[0]]>").append("</qty_"+ i + ">");
											//System.out.println("protecting qty.."+i);
										}
										else
										{
											if (qtyStr[i] == null)
											{
												qtyStr[i] = "0";
											}
											qty[i] = Double.parseDouble( qtyStr[i] );
											//System.out.println("adding  qty.."+qty[i]);
											valueXmlString.append("<qty_"+ i + "_t protect =\"1\">").append("<![CDATA[" + sch[i] + "]]>").append("</qty_"+ i + "_t>");
											valueXmlString.append("<qty_"+ i + " protect =\"0\">").append("<![CDATA[" + qtyStr[i] + "]]>").append("</qty_"+ i + ">");
										}
									}
									////System.out.println("total quantity>>>>>>>>>>>>>> " + qtyTotal);
									//valueXmlString.append("<totqty>").append(qtyTotal).append("</totqty>");
								 //}
								 
							/*	custCode = genericUtility.getColumnValue("cust_code" , dom1);
								siteCodeShip = genericUtility.getColumnValue("site_code__ship" , dom);
								totQtyStr = genericUtility.getColumnValue("totqty",dom);
								if (totQtyStr == null || totQtyStr.trim().length() == 0)
								{
									totQtyStr = "0";
								}
								totQty = Double.parseDouble(totQtyStr);
										
								itemCode = genericUtility.getColumnValue("item_code" , dom);
								noArt = distCommon.getNoArt(siteCodeShip,custCode,itemCode,"NULL",totQty,'B',acShipperQty,acIntegralQty,conn);
								//System.out.println("getNoArt()......noArt..."+noArt);
								shipperQty = acShipperQty;
								intQty = acIntegralQty;
								noArt1 = distCommon.getNoArt(siteCodeShip,custCode,itemCode,"NULL",totQty,'S',acShipperQty,acIntegralQty,conn);
								//System.out.println("getNoArt()......noArt1..."+noArt1);
								//balQty = totQty - (int)(shipperQty * noArt1);								
								balQty = totQty - (double)(shipperQty * noArt1);								
								noArt2 = distCommon.getNoArt(siteCodeShip,custCode,itemCode,"NULL",balQty,'I',acShipperQty,acIntegralQty,conn);
								//System.out.println("getNoArt().......noArt2.."+noArt2);
								intQty = acIntegralQty;
								shipperQty = shipperQty * noArt1;
								intQty = intQty * noArt2;
								looseQty = totQty - (shipperQty + intQty);								
								lsStr = " Shipper Quantity = "+shipperQty+ "   Integral Quantity = " +intQty+  "  Loose Quantity = "+looseQty ;
								valueXmlString.append("<st_shipper>").append("<![CDATA["+lsStr+"]]>").append("</st_shipper>");	
								 
								*/
								String reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
								//System.out.println("modified string [" + reStr + "]");
								//<?xml version="1.0"?><Root><header><editFlag>E</editFlag></header><Detail2><item_ser__prom><![CDATA[CO]]></item_ser__prom><descr><![CDATA[FERTIGYN 5000FERTIGYN]]></descr><item_ser><![CDATA[ZT]]></item_ser><unit><![CDATA[A01]]></unit><loc_type><![CDATA[C1]]></loc_type><curr_stk><![CDATA[200000.0]]></curr_stk><st_scheme><![CDATA[]]></st_scheme><st_scheme><![CDATA[Integral Quantity :4.0]]></st_scheme><cust_item__ref><![CDATA[0132                ]]></cust_item__ref><cust_item_ref_descr><![CDATA[SAWAN TEST ]]></cust_item_ref_descr></Detail2></Root>
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								//System.out.println("modified string after [" + reStr + "]");
								valueXmlString.append(reStr);
								reStr = itemChanged(dom, dom1, dom2, objContext, "totqty", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								//System.out.println("modified string after [" + reStr + "]");
								valueXmlString.append(reStr);
								
								
								
							}// end itm_defaultedit	
							/* else if(currentColumn.trim().equalsIgnoreCase("cust_item__ref"))
							{
								String custItemRefDescr = null;
								custItemRef = genericUtility.getcolumnvalue("cust_item__ref",dom);
								custItemRef == null ? "" : custItemRef;
								//System.out.println("custItemRef:- " + custItemRef);
								sql = " select i.item_code, c.descr,i.descr"
											 + " from customeritem c,item i"
											 + " where c.item_code = i.item_code"
											 + " and item_code__ref =' " + custItemRef + "'";
								//System.out.println("select qry custItemRef.." + sql);
								pstmt= conn.prepareStatement(sql);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   itemCode = rs.getString(1) == null ? "" : rs.getString(1);
								   custItemRefDescr = rs.getString(2) == null ? "" : rs.getString(2);
								   itemDescr = rs.getString(3) == null ? "" : rs.getString(3);
								}
								//System.out.println("itemCode .." + itemCode);
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;
								valueXmlString.append("<item_code>").append(itemCode).append("</item_code>");
								valueXmlString.append("<descr>").append(itemDescr).append("</descr>");
								valueXmlString.append("<cust_item_ref_descr>").append(custItemRefDescr).append("</cust_item_ref_descr>");
								
							}// end itm_defaultedit */													
							else if(currentColumn.trim().startsWith("qty_"))
							{
								itemCode = genericUtility.getColumnValue("item_code",dom);
								totqtyStr = genericUtility.getColumnValue("totqty",dom);
								//System.out.println("xtraParams>>>>> " + xtraParams);
								//inCtrStr = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"in_ctr");
								//System.out.println("inCtrStr>>>>> " + inCtrStr);
								
								//by alam as inCtrStr is coming "BRDCNF" start
								//later on to verify and set something appropriate.
								//try{
								//	inCtr = Integer.parseInt( ( inCtrStr == null || inCtrStr.trim().length() == 0 ? "0" : inCtrStr.trim() ) );
								//}catch( Exception e )
								//{
								//	inCtr = 0;
								//}
								//by alam as inCtrStr is coming "BRDCNF" end
								if (totqtyStr == null || totqtyStr.trim().length() == 0)
								{
									totQty = 0;
								}
								else
								{
									totQty = Double.parseDouble(totqtyStr.trim() );
								}
								
								noOfSchedule = NoOfSchedule(maxScheduleNo,dom,dom1);
								//System.out.println( "noOfSchedule :: " + noOfSchedule ); 
								//if( inCtr == 0)
								//{
									inCtr = noOfSchedule;
								//}
								qtyTotal = 0;
								for(int i = 1;i <= inCtr; i++ )
								{
									strQty = "";
									strQty = "qty_"+i;									
									qtyStr[i] = genericUtility.getColumnValue( strQty,dom);
									if (qtyStr[i] == null || qtyStr[i].trim().length() == 0)
									{
										qty[i] = 0;
									}
									else
									{
										qty[i] = Double.parseDouble(qtyStr[i] );
									}
									
									qtyTotal = qtyTotal + qty[i] ;
								}
								totDiff = totQty - qtyTotal;
								//for(int i = 0;i < inCtr; i++ )
								//{
								//	valueXmlString.append("<qty_"+i+ ">").append("<![CDATA["+totDiff+"]]>").append("</qty_" + i + ">");
								//}
								
								/*
								sql = " select price_list from site_customer"
											+ " where site_code =? "
											+ " and cust_code = ? ";
								//System.out.println("select qry from price_list.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,siteCode);
								pstmt.setString(2,custCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   priceList = rs.getString(1) == null ? "" : rs.getString(1);
								}
								  //System.out.println("siteDescr .." + siteDescr);
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;
								if( priceList == null || priceList.trim().length() == 0)
								{
									sql = " select price_list from customer"
											+ " where cust_code = ?";
									//System.out.println("select qry from price_list.." + sql);
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   priceList = rs.getString(1) == null ? "" : rs.getString(1);
									}
									  //System.out.println("priceList .." + priceList);
									pstmt.close();
									rs.close();
									pstmt = null;
									rs = null;	
								}
								sql = " select rate from pricelist"
											+ " where price_list = ?"
											+ " and item_code = ?"
											+ " and ref_no = (select max(ref_no) from pricelist"
											+ " where price_list = ?"
											+ " and item_code = ?"
											+ " and (ref_no is not null))";
								//System.out.println("select qry from price_list.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,priceList);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,priceList);
								pstmt.setString(4,itemCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								    rate = rs.getDouble(1) ;
								}
								//System.out.println("rate .." + rate);
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;	
								*/
								itemCode = genericUtility.getColumnValue("item_code",dom);
								//System.out.println("itemCode......"+itemCode);
								siteCode = genericUtility.getColumnValue("site_code",dom1);
								custCode = genericUtility.getColumnValue("cust_code",dom1);
								//System.out.println("custCode ......"+custCode );
								orderDate = genericUtility.getColumnValue("order_date",dom1);
								//System.out.println("orderDate ......"+orderDate );
								orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
								sql = " select price_list from site_customer where cust_code =?"
											+ " and site_code = ?";
								//System.out.println("select  totqty qry from location.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);	
								pstmt.setString(2,siteCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   priceList = rs.getString(1) == null ? "" : rs.getString(1);
								}
								System.out.println("priceList .." + priceList);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if( priceList == null || priceList.trim().length() == 0)
								{
									sql = " select price_list from customer where cust_code =?";
									//System.out.println("select qry from price_list from customer.." + sql);
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,custCode);	
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   priceList = rs.getString(1) == null ? "" : rs.getString(1);
									}
									System.out.println("priceList>>>>>>>>" + priceList);
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}
								unit = genericUtility.getColumnValue("unit",dom);
								listType = getPriceListType(priceList,conn);
							
								sql = " select count(1)  from pricelist"
											+ " where price_list   = ?"
											+ " and 	item_code = ?"
											+ " and 	unit  = ?"
											+ " and 	list_type = ?"
											+ " and 	eff_from    <= ?"
											+ " and 	valid_upto  >= ?"
											+ " and 	min_qty <= ?" 
											+ " and 	max_qty >= ?"
											+ " and 	(ref_no is not null) ";	
								//System.out.println("select qry from price_list.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,priceList);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,unit);
								pstmt.setString(4,listType);
								pstmt.setTimestamp(5,OrderDate);
								pstmt.setTimestamp(6,OrderDate);
								pstmt.setDouble(7,totQty);
								pstmt.setDouble(8,totQty);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   cnt = rs.getInt(1) ;
								}
								System.out.println("priceList COUNT .." + cnt);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								
								if( cnt >= 1)
								{
									sql = "select lot_no__from, lot_no__to from pricelist"
												+ " where price_list   = ? "
												+ " and 	item_code = ? "
												+ " and 	unit  = ? "
												+ " and 	list_type = ? "
												+ " and 	eff_from    <= ? "
												+ " and 	valid_upto  >= ? "
												+ " and 	min_qty <= ? "
												+ " and 	max_qty >= ? "
												+ " and 	(ref_no is not null) "
												+ " and ref_no = ( select max(ref_no) from pricelist "
																	+ " where price_list   = ? "
																	+ " and 	item_code = ? "
																	+ " and 	unit  = ? "
																	+ " and 	list_type = ? "
																	+ " and 	eff_from    <= ? "
																	+ " and 	valid_upto  >= ? "
																	+ " and 	min_qty <= ? "
																	+ " and 	max_qty >= ? "
																	+ " and 	(ref_no is not null) ) ";	
									//System.out.println("select qry maxRefNoStr.." + sql);
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,priceList);
									pstmt.setString(2,itemCode);
									pstmt.setString(3,unit);
									pstmt.setString(4,listType);
									pstmt.setTimestamp(5,OrderDate);
									pstmt.setTimestamp(6,OrderDate);
									pstmt.setDouble(7,totQty);
									pstmt.setDouble(8,totQty);

									pstmt.setString(9,priceList);
									pstmt.setString(10,itemCode);
									pstmt.setString(11,unit);
									pstmt.setString(12,listType);
									pstmt.setTimestamp(13,OrderDate);
									pstmt.setTimestamp(14,OrderDate);
									pstmt.setDouble(15,totQty);
									pstmt.setDouble(16,totQty);

									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   maxRefNoStr  = rs.getString(1) == null ? "" : rs.getString(1);
									}
									//System.out.println("maxRefNo .." + maxRefNo);
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									//System.out.println("orderDate in db date format " + orderDate.toString());
									// 21/03/09 manoharan
									//orderDateStr = genericUtility.getValidDateString(orderDate.toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
									orderDateStr = genericUtility.getValidDateString(orderDateStr,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
									
									//System.out.println("orderDate in appl date format " + orderDateStr);
									rate = distCommon.pickRate(priceList,orderDateStr,itemCode,maxRefNoStr,"L",totQty,conn);
									System.out.println("rate from distcommon>>>>>>>>> " + rate);
								}//end cnt >= 1								
								if(rate <= 0)
								{		
									do
									{
										
										priceListParent = null;
										sql="select (case when price_list__parent is null  then '' else price_list__parent end )"
											+"from pricelist_mst where price_list = ?"
											+"and list_type =?  and price_list__parent is not null";								
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,priceList);
										pstmt.setString(2,listType);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
											priceListParent  = rs.getString(1) == null ? "" : rs.getString(1);
										}
										else // 29/07/10 manoharan this else condition added as was going in loop
										{
											break;
										}
										System.out.println("priceListParent .." + priceListParent);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										System.out.println("priceListParent null codition:-" + priceListParent);
										if(priceListParent == null || priceListParent.trim().length() == 0 || "null".equalsIgnoreCase( priceListParent ) )
										{
											break;
										}
										listType = distCommon.getPriceListType(priceListParent,conn);
									
										sql = " select count(1)  from pricelist"
											+ " where price_list   = ? "
											+ " and 	item_code = ? "
											+ " and 	unit  = ? "
											+ " and 	list_type = ? "
											+ " and 	eff_from    <= ? "
											+ " and 	valid_upto  >= ? "
											+ " and 	min_qty <= ? " 
											+ " and 	max_qty >= ? " 
											+ " and 	(ref_no is not null) ";
											
										//System.out.println("select qry from price_list.." + sql);
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,priceList);
										pstmt.setString(2,itemCode);
										pstmt.setString(3,unit);
										pstmt.setString(4,listType);
										pstmt.setTimestamp(5,OrderDate);
										pstmt.setTimestamp(6,OrderDate);
										pstmt.setDouble(7,totQty);
										pstmt.setDouble(8,totQty);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
										   cnt = rs.getInt(1) ;
										}
										System.out.println("cnt .." + cnt);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										
										if( cnt >= 1)
										{
											//sql = " select max(ref_no) from pricelist"
											sql = " select lot_no__from from pricelist"
														+ " where price_list   = ? "
														+ " and 	item_code =? "
														+ " and 	unit  = ? "
														+ " and 	list_type =? "
														+ " and 	eff_from    <= ? "
														+ " and 	valid_upto  >= ? "
														+ " and 	min_qty <= ? "
														+ " and 	max_qty >=? "
														+ " and 	(ref_no is not null) "
														+ " and ref_no = ( select max(ref_no) from pricelist "
																				+ " where price_list   = ? "
																				+ " and 	item_code = ? "
																				+ " and 	unit  = ? "
																				+ " and 	list_type = ? "
																				+ " and 	eff_from    <= ? "
																				+ " and 	valid_upto  >= ? "
																				+ " and 	min_qty <= ? "
																				+ " and 	max_qty >= ? "
																				+ " and 	(ref_no is not null) ) ";	
											//System.out.println("select qry from price_list.." + sql);
											pstmt= conn.prepareStatement(sql);
											pstmt.setString(1,priceList);
											pstmt.setString(2,itemCode);
											pstmt.setString(3,unit);
											pstmt.setString(4,listType);
											pstmt.setTimestamp(5,OrderDate);
											pstmt.setTimestamp(6,OrderDate);
											pstmt.setDouble(7,totQty);
											pstmt.setDouble(8,totQty);

											pstmt.setString(9,priceList);
											pstmt.setString(10,itemCode);
											pstmt.setString(11,unit);
											pstmt.setString(12,listType);
											pstmt.setTimestamp(13,OrderDate);
											pstmt.setTimestamp(14,OrderDate);
											pstmt.setDouble(15,totQty);
											pstmt.setDouble(16,totQty);

											rs = pstmt.executeQuery(); 
											if(rs.next())
											{
												maxRefNoStr  = rs.getString(1) == null ? "" : rs.getString(1);
											}
											 
											//System.out.println("maxRefNo .." + maxRefNo);
											rs.close();
											pstmt.close();
											pstmt = null;
											rs = null;
										}//end cnt >= 1
										else
										{
											priceList =	priceListParent;
											continue;
										}
										//System.out.println("orderDateStr before parsing:- " + orderDateStr);
										if(orderDateStr.indexOf("/") == -1)
										{
											orderDateStr = genericUtility.getValidDateString(orderDateStr,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
										}
										else
										{
											orderDateStr = orderDateStr;
										}
										//System.out.println("orderDateStr for rate:- " + orderDateStr);
										//System.out.println(" priceListParent:- " + priceListParent + " priceListParent:- " + priceListParent + " itemCode:- " + itemCode + " maxRefNoStr:- " + maxRefNoStr + " totQty:- " + totQty);
										rate = distCommon.pickRate(priceListParent,orderDateStr,itemCode,maxRefNoStr,"L",totQty,conn);
										System.out.println("rate :- " + rate);
										if(rate >0)
										{
											priceList =	priceListParent;
											break;
										}
										
									}
									while(true);								
								}
								
								System.out.println("rate :- " + rate);
								OrdValue = qtyTotal * rate ;
								System.out.println("rate :- " + rate);
								System.out.println("qtyTotal :- " + qtyTotal);
								System.out.println("setting OrdValue from  :["+OrdValue+"]");
								
								
								
								valueXmlString.append("<ord_value>").append("<![CDATA["+ deciFormater.format( OrdValue ) +"]]>").append("</ord_value>");
							} 
							// end qty
							else if(currentColumn.trim().equalsIgnoreCase("site_code__ship"))
							{
								siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom1);
								//System.out.println("siteCodeShip....."+siteCodeShip);
								siteCodeShip = siteCodeShip == null ? "" : siteCodeShip;
								sql = "select descr from site where site_code =?";
								//System.out.println("select site_code__ship  from location.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,siteCodeShip);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   siteDescr = rs.getString(1) == null ? "" : rs.getString(1);
								}
								  //System.out.println("siteDescr .." + siteDescr);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								valueXmlString.append("<site_descr>").append("<![CDATA["+siteDescr.trim()+"]]>").append("</site_descr>");
							}  // end ord_value							
							//ITEM_CODE
							else if(currentColumn.trim().equalsIgnoreCase("item_code"))
							{
								String countCd = null;
								PreparedStatement scmPStmt = null;
								ResultSet scmRs = null;
								itemCode = genericUtility.getColumnValue("item_code",dom);
								itemCode = itemCode ==null ? "" : itemCode;
								siteCode = genericUtility.getColumnValue("site_code",dom1); 
								siteCode = siteCode ==null ? "" : siteCode;
								tranDateStr = genericUtility.getColumnValue("order_date",dom1);								
								tranDateStr = genericUtility.getValidDateString(tranDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());   
								tranDate = java.sql.Timestamp.valueOf(tranDateStr + " 00:00:00.00");
								custCode = genericUtility.getColumnValue("cust_code",dom1);						
								custCode = custCode ==null ? "" : custCode;
								orderType = genericUtility.getColumnValue("order_type",dom1);
								orderType = orderType ==null ? "" : orderType;
								mitemSer  = distCommon.getItemSer(itemCode,siteCode,tranDate,custCode,"C",conn);
								System.out.println("call getItemSer() and return ..mitemSer...."+mitemSer);
								itemSerProm  = distCommon.getItemSer(itemCode,siteCode,tranDate,custCode,"O",conn);
								System.out.println("call getItemSer() and  return .itemSerProm....."+itemSerProm);
								sql="select count(*) from customer_series where cust_code=? and item_ser=? ";
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,itemSerProm);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   cnt = rs.getInt(1);
								   
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;	
								if(cnt == 0)
								{
									sql= "select item_ser from item_credit_perc where item_code=? and  item_ser in( select item_ser from customer_series where cust_code=? and item_ser = item_credit_perc.item_ser)";
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									pstmt.setString(2,custCode);
									rs = pstmt.executeQuery();									
									if(rs.next())
									{
										itemSerCrPerc = rs.getString("item_ser") == null ? "" : rs.getString("item_ser");
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;						
									if (itemSerCrPerc != null && itemSerCrPerc.trim().length()>0 )
									{
										valueXmlString.append("<item_ser__prom>").append("<![CDATA["+itemSerProm.trim()+"]]>").append("</item_ser__prom>");	
										sql="select item_ser__inv from customer_series where cust_code= ?  and  item_ser=? ";
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,custCode);
										pstmt.setString(2,itemSerCrPerc);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											lsItemSer = rs.getString("item_ser__inv") == null ? "" : rs.getString("item_ser__inv");
										}
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										lsItemSer = lsItemSer == null ? "" : lsItemSer;	
										valueXmlString.append("<item_ser>").append("<![CDATA["+lsItemSer+"]]>").append("</item_ser>");
									}
									else  
									{
										valueXmlString.append("<item_ser__prom>").append("<![CDATA["+itemSerProm+"]]>").append("</item_ser__prom>");		
									}
									
								}
								else
								{
									valueXmlString.append("<item_ser__prom>").append("<![CDATA["+itemSerProm+"]]>").append("</item_ser__prom>");
								}
									
								if(lsItemSer != null && lsItemSer.length()>0)	
								{
									 mitemSer = lsItemSer;
								}
								sql="select descr, unit, (case when loc_type__parent is null then loc_type else loc_type__parent end) loc_type  from  item where item_code=? ";
								
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									descr = rs.getString("descr") == null ? "" : rs.getString("descr") ;  
									unit = rs.getString("unit") == null ? "" : rs.getString("unit") ;  
									locType = rs.getString("loc_type") == null ? "" : rs.getString("loc_type") ;  
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								//System.out.println("mitemSer...."+mitemSer);
								descr = descr == null ? "" : descr;
								unit = unit == null ? "" : unit;
								mitemSer = mitemSer == null ? "" : mitemSer; // changes made for null pointer expn of item_ser ***vishakha***25-11-2014
								locType = locType == null ? "" : locType;								
								valueXmlString.append("<descr>").append("<![CDATA["+ descr.trim() +"]]>").append("</descr>");	
								valueXmlString.append("<item_ser>").append("<![CDATA["+ mitemSer.trim() +"]]>").append("</item_ser>");	
								valueXmlString.append("<unit>").append("<![CDATA["+ unit.trim() +"]]>").append("</unit>");	
								valueXmlString.append("<loc_type>").append("<![CDATA["+ locType.trim() +"]]>").append("</loc_type>");
								sql= "select case when udf2 is null then '' else udf2 end  from site where site_code = '"+siteCode+"'";
								pstmt= conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									siteString = rs.getString(1) == null ? "" : rs.getString(1) ;									
								}
								//System.out.println("siteString...."+siteString);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								disparmLoc = distCommon.getDisparams("999999","CURR_LOC_CODE",conn);
								//System.out.println("getDisparams()....disparmLoc..."+disparmLoc);								
								disparmLoc = disparmLoc == null ?"" : disparmLoc.trim();
								if(disparmLoc.equalsIgnoreCase("NULLFOUND"))
								{
									disparmLoc="FRSH";
								}
								if(siteString == null || siteString.trim().length()==0 )
								{
									lcAvailStk =0.00;						
									sql="select sum((case when quantity is null then 0 else quantity end) - (case when alloc_qty is null then 0 else alloc_qty end)) "					
											+" from stock a, invstat b "
											+" where a.inv_stat = b.inv_stat "
											+" and a.item_code = ? "
											+" and b.available = 'Y' "
											+" and a.loc_code  = ? ";
									//System.out.println("sql...."+sql);				
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									pstmt.setString(2,disparmLoc);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lcAvailStk = rs.getInt(1);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}		
								else
								{
									lcAvailStk =0.00;	
									//	23/03/10 manoharan
									StringTokenizer st = new StringTokenizer(siteString,",");
									ArrayList siteCodeList = new ArrayList();
									String tempCode = "";

									while(st.hasMoreTokens())
									{
										tempCode = st.nextToken();
										if (tempCode != null && tempCode.trim().length() > 0)
										{
											siteCodeList.add("'"+tempCode.trim()+"'");
										}
									}
									if(!siteCodeList.isEmpty())
									{
										siteString = siteCodeList.toString();
										siteString = siteString.substring(1,siteString.length()-1);
									}	
									else
									{
									   siteString ="''";
									}
									
									sql="select sum((case when quantity is null then 0 else quantity end) - (case when alloc_qty is null then 0 else alloc_qty end)) "
										+" from stock a, invstat b "
										+" where  a.inv_stat = b.inv_stat "
										+" and a.item_code = ? "
										+" and a.site_code in (" + siteString + ") "
										+" and b.available = 'Y' "
										+" and a.quantity > 0 "
										+" and a.loc_code  = ? ";
//										+" and   instr(?,a.site_code) > 0 "
									//System.out.println("sql...."+sql);	
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									//pstmt.setString(2,siteString);
									pstmt.setString(2,disparmLoc);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lcAvailStk = rs.getInt(1);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;									
								}  
								valueXmlString.append("<curr_stk>").append("<![CDATA["+lcAvailStk+"]]>").append("</curr_stk>");		
								schemeCode = "";							
								custCd = genericUtility.getColumnValue("cust_code",dom1);								
								sql="select state_code, count_code  from customer where cust_code = ? ";	
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCd);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									stateCd = rs.getString("state_code") == null ? "" : rs.getString("state_code");
									countCd = rs.getString("count_code") == null ? "" : rs.getString("count_code");
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								lsItemCode = itemCode ;
								sql = "select count(*)  from item where item_code = ? ";	
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,lsItemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								valueXmlString.append("<st_scheme>").append("<![CDATA[]]>").append("</st_scheme>");	
								sql = "select bom_code , item_stru from item where  item_code = ? ";
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									schemeCode =rs.getString("bom_code") == null ?"" : rs.getString("bom_code"); 
									lsType =rs.getString("item_stru") == null ? "" : rs.getString("item_stru").trim(); 
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if(lsType != "C")
								{
									lsOrderType = lsOrderType == null ? "" : lsOrderType ;
									siteCode = siteCode == null ? "" : siteCode ;
									stateCd = stateCd == null ? ""  : stateCd ;
								}
								sql = " select a.scheme_code "  
										+" from scheme_applicability a, " 
										+" scheme_applicability_det  b" 
										+" where a.scheme_code	= b.scheme_code"  
										+" and a.item_code 	= ? "
										+" and a.app_from <= ? "
										+" and a.valid_upto >= ? "
										+" and (b.site_code  = ?"	
										+" or b.state_code 	= ? or b.count_code = ? )";								
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								pstmt.setTimestamp(2,tranDate);
								pstmt.setTimestamp(3,tranDate);
								pstmt.setString(4,siteCode);
								pstmt.setString(5,stateCd);
								pstmt.setString(6,countCd);
								rs = pstmt.executeQuery();
								while(rs.next())
								{									
									lsCurscheme = rs.getString("scheme_code") == null ? "" : rs.getString("scheme_code");
									System.out.println("Scheme COde of table scheme applicability total "+lsCurscheme);
									mcount = 0 ;	
									sql ="select (case when apply_cust_list is null then '' else apply_cust_list end) apply_cust_list, "
											+" (case when noapply_cust_list is null then '' else noapply_cust_list end) noapply_cust_list, "
											+" order_type from 	scheme_applicability where scheme_code = ? " ;

									scmPStmt= conn.prepareStatement(sql);
									scmPStmt.setString(1,lsCurscheme);
									scmRs = scmPStmt.executeQuery();
									if(scmRs.next())
									{
										
										applyCustList =scmRs.getString("apply_cust_list") == null ?"" : scmRs.getString("apply_cust_list"); 
										noapplyCustList=scmRs.getString("noapply_cust_list") == null ? "" : scmRs.getString("noapply_cust_list"); 
										applicableordtypes=scmRs.getString("order_type") == null ? "" : scmRs.getString("order_type"); 
									}
									scmRs.close();
									scmRs = null;								
									scmPStmt.close();
									scmPStmt = null;
//									if(applicableordtypes.trim() == "NE" && applicableordtypes.trim().length()==0)
									//System.out.println("Cust list applicable.....["+applyCustList + "]");
									//System.out.println("Cust list non applicable.....["+noapplyCustList + "]");
									System.out.println("order type applicable.....["+applicableordtypes + "]");
									//System.out.println("current order type ["+orderType + "]");
									if(applicableordtypes.trim().equalsIgnoreCase("NE") && applicableordtypes.trim().length()==0)
									{
										System.out.println("In NE Oreder type Condtion");
										continue;
									}
									else if ( applicableordtypes !=null && applicableordtypes.trim().length()>0) 
									{
										lbProceed = false ;
										while (applicableordtypes.length()>0)
										{
											pos = applicableordtypes.indexOf(",");
											System.out.println("POSL:" + pos);
											if (pos > -1)
											{
												lsToken = distCommon.getToken(applicableordtypes,",");
												System.out
														.println("Token is:"+lsToken);
									
												//System.out.println("getToken().call and returnValue is....."+lsToken);
												if (orderType.trim().equals(lsToken.trim()))
												{
													System.out.println("order type matched.....["+orderType + "]");
													lbProceed = true ;
													break;
												}
												applicableordtypes = applicableordtypes.substring(pos+1);
											}
											else
											{
												
												if (orderType.trim().equals(applicableordtypes.trim()))
												{
													System.out.println("Else order type matched.....["+orderType + "]");
													lbProceed = true ;
												}
												break;
											}
										}
										if(!lbProceed) continue;
									}	
									System.out.println("Prev Scheme code"+lsPrevscheme);
									lsPrevscheme	= schemeCode;	 
									System.out.println("SCheme code is" +lsPrevscheme);
									schemeCode = lsCurscheme ;
									System.out.println("Scheme COde"+schemeCode);
									if (applyCustList.trim().length() > 0)
									{
										schemeCode = null;
										mcustCd = genericUtility.getColumnValue("cust_code",dom1);
										mcustCd = mcustCd == null ?"" : mcustCd;
										while(applyCustList.trim().length() > 0)
										{
											pos = applyCustList.indexOf(",");
											if (pos > -1)
											{
												applyCust = distCommon.getToken(applyCustList,",");
												if (applyCust.trim().equals( mcustCd.trim())) 
												{
													schemeCode = lsCurscheme;
													break;
												}
												applyCustList = applyCustList.substring(pos+1);
											}
											else
											{
												// 03/06/10 manoharan
												//if (applyCust.trim().equals( applyCustList.trim())) 
												if (mcustCd.trim().equals( applyCustList.trim())) 
												{
													schemeCode = lsCurscheme;
												}
												break;
											}
										}
									}								
									if(schemeCode != null && noapplyCustList != null && noapplyCustList.trim().length()>0 ) 
									{									
										mcustCd = genericUtility.getColumnValue("cust_code",dom1);
										mcustCd = mcustCd == null ? "" : mcustCd;
										 while (noapplyCustList.trim().length()>0)
										{ 
											pos = noapplyCustList.indexOf(",");
											if (pos > -1)
											{
												noapplyCust = distCommon.getToken(noapplyCustList,",");
												if (noapplyCust.trim().equals( mcustCd.trim()))
												{	
													schemeCode = null ;
													break;
												}
												noapplyCustList = noapplyCustList.substring(pos+1);
											}
											else
											{
												// 03/06/10 manoharan
												//if (noapplyCust.trim().equals( noapplyCustList.trim()))
												if (mcustCd.trim().equals( noapplyCustList.trim()))
												{	
													schemeCode = null ;
												}
												
												break;
											}
										}
									}
									if (schemeCode !=null)
										llSchcnt ++ ;
									else if( llSchcnt > 1 )
										schemeCode	= lsPrevscheme;							
								} // end of while 	
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if (llSchcnt > 1 )
									schemeCode = lsPrevscheme ;	
								System.out.println("schemeCode = lsPrevscheme "+schemeCode);
								System.out.println("schemeCode = lsPrevscheme@@@ "+lsPrevscheme);
										
								//System.out.println("Scheme count.....["+llSchcnt + "] Scheme Code [" + schemeCode + "]");						
								sql="select (case when item_stru is null then 'S' else item_stru end)"						
										+"from 	item "
										+"where item_code = ?" ;									
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,lsItemCode);
								rs = pstmt.executeQuery();	
								if(rs.next())
								{
									lsItemStru = rs.getString(1) == null ?"" : rs.getString(1).trim();
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								//System.out.println("lsItemStru.....["+lsItemStru + "]");
								if(  lsItemStru != null && "F".equalsIgnoreCase(lsItemStru) && schemeCode.trim().length()>0)	
								{
									sql="select count(*)  from scheme_applicability where item_code = ?";
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									rs = pstmt.executeQuery();	
									if(rs.next())
									{
										cnt= rs.getInt(1);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;						
									if(cnt >1)
									{
										schemeCode = null;
									}
									else
									{
										schemeCode = schemeCode;
									}
								}
							     System.out.println("schemeCode.....after item_stru check.....["+schemeCode + "]");
							     
								if(schemeCode !=null && schemeCode.trim().length()>0)	
								{
									sql=" select slab_on  from scheme_applicability	where scheme_code = ? ";
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,schemeCode);
									rs = pstmt.executeQuery();	
									if(rs.next())
									{
										mslabOn = rs.getString("slab_on") == null ? "" : rs.getString("slab_on");
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									System.out.println("slab on....["+mslabOn + "]");
									if(mslabOn != null && mslabOn.trim().length() > 0 && mslabOn.equalsIgnoreCase("N"))
									{
										sql="select descr  from bom "
										+"where bom_code = ?";
										pstmt= conn.prepareStatement(sql);
										//pstmt.setString(1,lsSchemeCode);
										pstmt.setString(1,schemeCode);
										
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											lsDescr = rs.getString("descr") == null ? "" : rs.getString("descr");
										}
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										System.out.println("Scheme Descr"+lsDescr);
										valueXmlString.append("<st_scheme>").append("<![CDATA[Scheme Descr :"+lsDescr+"]]>").append("</st_scheme>");	
									}
									else
									{
										//System.out.println("..call()..getIntegralQty..");
										integralQty = distCommon.getIntegralQty(custCode,itemCode,siteCode,conn);
										//System.out.println("getIntegralQty()....return value......  "+integralQty);
										//dw_detedit[ii_currformno].setitem(1,"st_scheme","Integral Quantity : " + string(lc_integral_qty,'#,##0.00') );
										valueXmlString.append("<st_scheme>").append("<![CDATA[Integral Quantity :"+integralQty+"]]>").append("</st_scheme>");
									}	
								}
								else
								{	//System.out.println("....call()..getIntegralQty........");	
									integralQty = distCommon.getIntegralQty(custCode,itemCode,siteCode,conn);
									//System.out.println("getIntegralQty()....return value......  "+integralQty);
									//dw_detedit[ii_currformno].setitem(1,"st_scheme","Integral Quantity : " + string(lc_integral_qty,'#,##0.00') );
									valueXmlString.append("<st_scheme>").append("<![CDATA[Integral Quantity :"+integralQty+"]]>").append("</st_scheme>");
										
								}
								sql = "select pricelist.rate rate, " 				
										+" case when pricelist.rate_type = 'P' then '%' else 'Fix' end as rate_type, "
										+"pricelist.price_list price_list" 
										+" from	site_customer ,pricelist "  
										+" where site_customer.price_list__disc = pricelist.price_list " 
										+" and site_customer.site_code = ? "  
										+" and site_customer.cust_code = ? "
										+" and pricelist.item_code = ? "  
										+" and site_customer.price_list__disc is not null" ;
									pstmt= conn.prepareStatement(sql);		
									pstmt.setString(1,siteCode);	
									pstmt.setString(2,custCode);	
									pstmt.setString(3,itemCode);	
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										if (lsDescr ==null)
										{
											lsDescr = "";
										}
										System.out.println("Description "+lsDescr);
										lcRate =rs.getString("rate") == null ? "" : rs.getString("rate");
										lsType =rs.getString("rate_type") == null ? "" : rs.getString("rate_type");
										lsPriceList =rs.getString("price_list") == null ? "" : rs.getString("price_list");
										valueXmlString.append("<st_scheme>").append("<![CDATA[" + lsDescr + " Integral Quantity :"+integralQty+ "Price List Disc :"+priceList+ " Rate : "+lcRate+ " " +lsType+"]]>").append("</st_scheme>");
										//valueXmlString.append("<st_scheme>").append("<![CDATA[Integral Quantity :"+integralQty+"]]>").append("</st_scheme>"); 
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;			
									
								sql="select item_code__ref ,descr  from customeritem " 
										+" where cust_code =? and item_code = ?";
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);	
								pstmt.setString(2,itemCode);	
								rs = pstmt.executeQuery();		
								if(rs.next())
								{
									lsCustItemCodeRef = rs.getString("item_code__ref")== null ?"" : rs.getString("item_code__ref");;
									lsCustItemCodeDescr = rs.getString("descr")== null ?"" : rs.getString("descr");;
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if (lsCustItemCodeRef == null)
								{
									lsCustItemCodeRef = "";
									lsCustItemCodeDescr = "";
								}
								//System.out.println("lsCustItemCodeRef:- " + lsCustItemCodeRef + " lsCustItemCodeDescr:- " + lsCustItemCodeDescr );
								valueXmlString.append("<cust_item__ref>").append("<![CDATA["+lsCustItemCodeRef+"]]>").append("</cust_item__ref>");
								valueXmlString.append("<cust_item_ref_descr>").append("<![CDATA["+lsCustItemCodeDescr+"]]>").append("</cust_item_ref_descr>");
							}  					
							//END ITEM_CODE							
							else if(currentColumn.trim().equalsIgnoreCase("totqty"))
							{    
								
								/*Request_id-SUN15OCT15-multiple scheme define in same item then pick up the wrong scheme code Quantity not selected for scheme code.
								 *  Abhijit Gaikwad 16/10/15 Start*/
								String Curscheme = null;
								String countCd1 = null;
								String siteCode1 = null;
								String stateCd1 = null;
								String descr1= null;
								String itemCode1= null;
								itemCode1 = genericUtility.getColumnValue("item_code",dom);
								totqtyStr = genericUtility.getColumnValue("totqty",dom);
								siteCode1 = genericUtility.getColumnValue("site_code",dom1); 
								siteCode1 = siteCode1 ==null ? "" : siteCode1;
								tranDateStr = genericUtility.getColumnValue("order_date",dom1);								
								tranDateStr = genericUtility.getValidDateString(tranDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());   
								tranDate = java.sql.Timestamp.valueOf(tranDateStr + " 00:00:00.00");
								custCd = genericUtility.getColumnValue("cust_code",dom1);
								
								sql="select state_code, count_code  from customer where cust_code = ? ";	
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCd);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									stateCd1 = rs.getString("state_code") == null ? "" : rs.getString("state_code");
									countCd1 = rs.getString("count_code") == null ? "" : rs.getString("count_code");
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								lsItemCode = itemCode1 ;
								System.out.println("Item code in total qty"+lsItemCode);
								sql = "select bom_code , item_stru from item where  item_code = ? ";
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,itemCode1);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									schemeCode =rs.getString("bom_code") == null ?"" : rs.getString("bom_code"); 
									lsType =rs.getString("item_stru") == null ? "" : rs.getString("item_stru").trim(); 
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								orderType = genericUtility.getColumnValue("order_type",dom1);
								orderType = orderType ==null ? "" : orderType;
								if(lsType != "C")
								{
									lsOrderType = lsOrderType == null ? "" : lsOrderType ;
									siteCode = siteCode == null ? "" : siteCode ;
									stateCd = stateCd == null ? ""  : stateCd ;
								}
								//System.out.println("totqtyStr...value....."+totqtyStr);	
								sql = " select a.scheme_code "  
									+" from scheme_applicability a, " 
									+" scheme_applicability_det  b" 
									+" where a.scheme_code	= b.scheme_code"  
									+" and a.item_code 	= ? "
									+" and a.app_from <= ? "
									+" and a.valid_upto >= ? "
									+" and (b.site_code  = ?"	
									+" or b.state_code 	= ? or b.count_code = ? )";								
							           pstmt= conn.prepareStatement(sql);
							           pstmt.setString(1,itemCode1);
							           pstmt.setTimestamp(2,tranDate);
							           pstmt.setTimestamp(3,tranDate);
							           pstmt.setString(4,siteCode1);
							           pstmt.setString(5,stateCd1);
							           pstmt.setString(6,countCd1);
							           rs = pstmt.executeQuery();
							         while(rs.next())
							            {
							        	 lsCurscheme = rs.getString("scheme_code") == null ? "" : rs.getString("scheme_code");
											System.out.println("Scheme COde of table scheme applicability total "+lsCurscheme);
											sql1="select count(1)From scheme_applicability A,bom b Where  A.scheme_code = b.bom_code And  B.bom_code     = ? " +
												 "And (? between case when min_qty is null then 0 else min_qty end And case when max_qty is null then 0 else max_qty end) and B.promo_term is null";
											  pstmt2= conn.prepareStatement(sql1);
									           pstmt2.setString(1,lsCurscheme);
									           pstmt2.setString(2,totqtyStr);
									           rs2 = pstmt2.executeQuery();
									         if(rs2.next())
									         {
									        	 cnt1= rs2.getInt(1);
									         }
									            rs2.close();
												pstmt2.close();
												pstmt2 = null;
												rs2 = null;
												if(cnt1 == 0)
												{
													continue;
												}	
											sql ="select (case when apply_cust_list is null then '' else apply_cust_list end) apply_cust_list, "
													+" (case when noapply_cust_list is null then '' else noapply_cust_list end) noapply_cust_list, "
													+" order_type from 	scheme_applicability where scheme_code = ? " ;

											pstmt1= conn.prepareStatement(sql);
											pstmt1.setString(1,lsCurscheme);
											rs1 = pstmt1.executeQuery();
											if(rs1.next())
											{
												
												applyCustList =rs1.getString("apply_cust_list") == null ?"" : rs1.getString("apply_cust_list"); 
												noapplyCustList=rs1.getString("noapply_cust_list") == null ? "" : rs1.getString("noapply_cust_list"); 
												applicableordtypes=rs1.getString("order_type") == null ? "" : rs1.getString("order_type"); 
											}
											rs1.close();
											rs1 = null;								
											pstmt1.close();
											if(applicableordtypes.trim().equalsIgnoreCase("NE") && applicableordtypes.trim().length()==0)
											{
												System.out.println("In NE Oreder type Condtion");
												continue;
											}
											else if ( applicableordtypes !=null && applicableordtypes.trim().length()>0) 
											{
												lbProceed = false ;
												while (applicableordtypes.length()>0)
												{
													pos = applicableordtypes.indexOf(",");
													System.out.println("POS total Qty:" + pos);
													if (pos > -1)
													{
														lsToken = distCommon.getToken(applicableordtypes,",");
														System.out.println("Token is:"+lsToken);
														if (orderType.trim().equals(lsToken.trim()))
														{
															System.out.println("order type matched.....["+orderType + "]");
															lbProceed = true ;
															break;
														}
														applicableordtypes = applicableordtypes.substring(pos+1);
													}
													else
													{
														
														if (orderType.trim().equals(applicableordtypes.trim()))
														{
															System.out.println("Else order type matched total qty.....["+orderType + "]");
															lbProceed = true ;
														}
														break;
													}
												}
												if(!lbProceed) continue;
											}	
											System.out.println("Prev Scheme code"+lsPrevscheme);
											lsPrevscheme	= schemeCode;	 
											System.out.println("SCheme code is" +lsPrevscheme);
											schemeCode = lsCurscheme ;
											System.out.println("Scheme COde"+schemeCode);
											System.out.println("Apply Cust Code is" +applyCustList);
											if (applyCustList.trim().length() > 0)
											{
												schemeCode = null;
												mcustCd = genericUtility.getColumnValue("cust_code",dom1);
												System.out
														.println("Cust code is :" + mcustCd);
												mcustCd = mcustCd == null ?"" : mcustCd;
												while(applyCustList.trim().length() > 0)
												{
													pos = applyCustList.indexOf(",");
													if (pos > -1)
													{
														applyCust = distCommon.getToken(applyCustList,",");
														if (applyCust.trim().equals( mcustCd.trim())) 
														{
															schemeCode = lsCurscheme;
															//code add
															break;
														}
														applyCustList = applyCustList.substring(pos+1);
													}
													else
													{
														// 03/06/10 manoharan
														//if (applyCust.trim().equals( applyCustList.trim())) 
														if (mcustCd.trim().equals( applyCustList.trim())) 
														{
															schemeCode = lsCurscheme;
															System.out.println("Else SchemeCOde" + schemeCode);
														}
														break;
													}
												}
											}	
											if(schemeCode != null && noapplyCustList != null && noapplyCustList.trim().length()>0 ) 
											{									
												mcustCd = genericUtility.getColumnValue("cust_code",dom1);
												System.out.println("Cust cod "+mcustCd);
												mcustCd = mcustCd == null ? "" : mcustCd;
												 while (noapplyCustList.trim().length()>0)
												{ 
													pos = noapplyCustList.indexOf(",");
													if (pos > -1)
													{
														noapplyCust = distCommon.getToken(noapplyCustList,",");
														if (noapplyCust.trim().equals( mcustCd.trim()))
														{	
															schemeCode = null ;
															break;
														}
														noapplyCustList = noapplyCustList.substring(pos+1);
													}
													else
													{
														// 03/06/10 manoharan
														//if (noapplyCust.trim().equals( noapplyCustList.trim()))
														if (mcustCd.trim().equals( noapplyCustList.trim()))
														{	
															schemeCode = null ;
														}
														
														break;
													}
												}
											}
											if (schemeCode !=null)
											{
												llSchcnt ++ ;
											System.out.println("llSchcnt"+ llSchcnt);
											}
											else if( llSchcnt == 1 )
											{
												schemeCode	= lsPrevscheme;
											}
										} // end of while 	
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										if (llSchcnt > 1 )
										{
											schemeCode = null ;
											///schemeCode = lsPrevscheme ;
										}
										System.out.println("schemeCode = Current "+ schemeCode);
										System.out.println("schemeCode = lscustschemecode@@@ "+ lsPrevscheme);
												
										//System.out.println("Scheme count.....["+llSchcnt + "] Scheme Code [" + schemeCode + "]");						
										sql="select (case when item_stru is null then 'S' else item_stru end)"						
												+"from 	item "
												+"where item_code = ?" ;									
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,itemCode1);
										rs = pstmt.executeQuery();	
										if(rs.next())
										{
											lsItemStru = rs.getString(1) == null ?"" : rs.getString(1).trim();
										}
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										//System.out.println("lsItemStru.....["+lsItemStru + "]");
										if(  lsItemStru != null && "F".equalsIgnoreCase(lsItemStru) && schemeCode.trim().length()>0)	
										{
											sql="select count(*)  from scheme_applicability where item_code = ?";
											pstmt= conn.prepareStatement(sql);
											pstmt.setString(1,itemCode1);
											rs = pstmt.executeQuery();	
											if(rs.next())
											{
												cnt= rs.getInt(1);
											}
											rs.close();
											pstmt.close();
											pstmt = null;
											rs = null;						
											if(cnt >1)
											{
												schemeCode = null;
											}
											else
											{
												schemeCode = schemeCode;
											}
										}
									    // System.out.println("schemeCode.....after item_stru check.....["+schemeCode + "]");
									     
										if(schemeCode !=null && schemeCode.trim().length()>0)	
										{
											sql=" select slab_on  from scheme_applicability	where scheme_code = ? ";
											pstmt= conn.prepareStatement(sql);
											pstmt.setString(1,schemeCode);
											rs = pstmt.executeQuery();	
											if(rs.next())
											{
												mslabOn = rs.getString("slab_on") == null ? "" : rs.getString("slab_on");
											}
											rs.close();
											pstmt.close();
											pstmt = null;
											rs = null;
											System.out.println("slab on....["+mslabOn + "]");
											if(mslabOn != null && mslabOn.trim().length() > 0 && mslabOn.equalsIgnoreCase("N"))
											{
												sql="select descr  from bom "
												+"where bom_code = ? and (? between case when min_qty is null then 0 else min_qty end And case when max_qty is null then 0 else max_qty end) ";
												pstmt= conn.prepareStatement(sql);
												//pstmt.setString(1,lsSchemeCode);
												pstmt.setString(1,schemeCode);
												pstmt.setString(2,totqtyStr);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													Descr = rs.getString("descr");
												}
												rs.close();
												pstmt.close();
												pstmt = null;
												rs = null;
												System.out.println("Scheme Descr"+Descr);
												Descr = Descr ==null ? "" : Descr;
												valueXmlString.append("<st_scheme>").append("<![CDATA[Scheme Descr :"+Descr+"]]>").append("</st_scheme>");	
											}
											else
											{
												integralQty = distCommon.getIntegralQty(custCd,itemCode1,siteCode1,conn);
												valueXmlString.append("<st_scheme>").append("<![CDATA[Integral Quantity :"+integralQty+"]]>").append("</st_scheme>");
											}	
										}
										else
										{	//System.out.println("....call()..getIntegralQty........");
											System.out.println("Site code :"+ siteCode1);
											integralQty = distCommon.getIntegralQty(custCd,itemCode1,siteCode1,conn);
											//System.out.println("getIntegralQty()....return value......  "+integralQty);
											//dw_detedit[ii_currformno].setitem(1,"st_scheme","Integral Quantity : " + string(lc_integral_qty,'#,##0.00') );
											valueXmlString.append("<st_scheme>").append("<![CDATA[Integral Quantity :"+integralQty+"]]>").append("</st_scheme>");
												
										}
							            
							        	  /*  Curscheme = rs.getString("scheme_code") == null ? "" : rs.getString("scheme_code");
											System.out.println("Scheme COde of table scheme applicability total quantity "+lsCurscheme);
											
											sql="select descr From scheme_applicability a,bom b Where a.scheme_code = b.bom_code And b.bom_code = ? And (? between case when b.min_qty is null then 0 else b.min_qty end And case when b.max_qty is null then 0 else b.max_qty end)";
											 pstmt1= conn.prepareStatement(sql);
											 pstmt1.setString(1,Curscheme);
											 pstmt1.setString(2,totqtyStr);
											 rs1 = pstmt1.executeQuery();
											 if(rs1.next())
											 {
												 descr1 = rs1.getString(1);
											 }
											    rs1.close();
											    pstmt1.close();
											    pstmt1 = null;
												rs1 = null;
							        	    
							            }
							            rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										System.out.println("Description:"+descr1);
										 if((totqtyStr != null && totqtyStr.trim().length()>0))
										 {
										valueXmlString.append("<st_scheme>").append("<![CDATA[Scheme Descr :"+descr1+"]]>").append("</st_scheme>");	
										 }
										 else
										 {
											 valueXmlString.append("<st_scheme>").append("<![CDATA[]]>").append("</st_scheme>"); 
										 } //Abhijit Gaikwad 16/10/15 End
*/
										 
								if(totqtyStr != null && totqtyStr.trim().length() > 0)
								{
									// 25/03/10 manoharan the string is in the form of 999999.999 format
									// cannot be parsed as int
									//totQty = Integer.parseInt(totqtyStr);
									//totQty = (new Double(Double.parseDouble(totqtyStr))).intValue();
									totQty = Double.parseDouble(totqtyStr);
									// end 25/03/10 manoharan
								}
								siteCode = genericUtility.getColumnValue("site_code",dom1);
								//System.out.println("xtraParams...value....."+xtraParams);	
								inCtrStr = getValueFromXTRA_PARAMS(xtraParams,"in_ctr"); //  REC_CTR
								//System.out.println("inCtrStr...value....."+inCtrStr);
								inCtr = Integer.parseInt( inCtrStr );
								//System.out.println("inCtr......"+inCtr);
								noOfSchedule = NoOfSchedule(maxScheduleNo,dom,dom1);
								if( inCtrStr == null || inCtr <= 0)
								{
									inCtr = noOfSchedule;
								}
								//System.out.println("inCtr from noOfSchedule :-"+inCtr);
								for(int i = 0;i < inCtr; i++)
								{									
									strSch = "";
									strSch ="sch_ratio_"+(i+1) ; 		
									//System.out.println("strSch......"+strSch);	
									schRatioStr[i] = genericUtility.getColumnValue(strSch , dom1);
									//System.out.println("schRatioStr:-"+schRatioStr[i] );
									schRatio[i] = Double.parseDouble(schRatioStr[i]);
									//System.out.println("schRatio:-" +schRatio[i] );
									distQty = (totQty * schRatio[i])/100;
									//System.out.println("distQty:-" +distQty);
									int k = i+1;
									valueXmlString.append("<qty_"+ k + ">").append("<![CDATA["+distQty+"]]>").append("</qty_"+ k + ">");
								}	
								itemCode = genericUtility.getColumnValue("item_code",dom);
								//System.out.println("itemCode......"+itemCode);
								custCode = genericUtility.getColumnValue("cust_code",dom1);
								//System.out.println("custCode ......"+custCode );
								orderDate = genericUtility.getColumnValue("order_date",dom1);
								//System.out.println("orderDate ......"+orderDate );
								orderDateStr = genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								OrderDate = java.sql.Timestamp.valueOf(orderDateStr + " 00:00:00.00");
								sql = " select price_list from site_customer where cust_code =?"
											+ " and site_code = ?";
								//System.out.println("select  totqty qry from location.." + sql);
								pstmt= conn.prepareStatement(sql);
								pstmt.setString(1,custCode);	
								pstmt.setString(2,siteCode);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   priceList = rs.getString(1) == null ? "" : rs.getString(1);
								}
								//System.out.println("priceList .." + priceList);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if( priceList == null || priceList.trim().length() == 0)
								{
									sql = " select price_list from customer where cust_code =?";
									//System.out.println("select qry from price_list from customer.." + sql);
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,custCode);	
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   priceList = rs.getString(1) == null ? "" : rs.getString(1);
									}
									//System.out.println("priceList .." + priceList);
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}
								unit = genericUtility.getColumnValue("unit",dom);
								listType = getPriceListType(priceList,conn);
								//Commented by sarita on 01MAR2018
								//if(listType.equalsIgnoreCase("L"))
								if("L".equalsIgnoreCase(listType))
								{
								sql1 = " select count(1)  from pricelist "
											+ " where price_list   = ? "
											+ " and 	item_code = ? "
											+ " and 	unit  = ? "
											+ " and 	list_type = ? "
											+ " and 	eff_from    <= ? "
											+ " and 	valid_upto  >= ? "
											+ " and 	min_qty <= ? " 
											+ " and 	max_qty >= ? ";
									
									pstmt= conn.prepareStatement(sql1);
									pstmt.setString(1,priceList);
									pstmt.setString(2,itemCode);
									pstmt.setString(3,unit);
									pstmt.setString(4,listType);
									pstmt.setTimestamp(5,OrderDate);
									pstmt.setTimestamp(6,OrderDate);
									pstmt.setDouble(7,totQty);
									pstmt.setDouble(8,totQty);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   cnt = rs.getInt(1) ;
									}
									//System.out.println("cnt .." + cnt);
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									
											
											
								}
								else
								{
								sql = " select count(1)  from pricelist"
											+ " where price_list   = ?"
											+ " and 	item_code = ?"
											+ " and 	unit  = ?"
											+ " and 	list_type = ?"
											+ " and 	eff_from    <= ?"
											+ " and 	valid_upto  >= ?"
											+ " and 	min_qty <= ?" 
											+ " and 	max_qty >= ?"
											+ " and 	(ref_no is not null) ";	
								//System.out.println("select qry from price_list.." + sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,priceList);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,unit);
								pstmt.setString(4,listType);
								pstmt.setTimestamp(5,OrderDate);
								pstmt.setTimestamp(6,OrderDate);
								pstmt.setDouble(7,totQty);
								pstmt.setDouble(8,totQty);
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
								   cnt = rs.getInt(1) ;
								}
								//System.out.println("priceList .." + priceList);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								}
								System.out.println("Cnt first>>>>>>>" + cnt);
								if( cnt >= 1)
								{
									sql = "select lot_no__from, lot_no__to from pricelist"
												+ " where price_list   = ? "
												+ " and 	item_code = ? "
												+ " and 	unit  = ? "
												+ " and 	list_type = ? "
												+ " and 	eff_from    <= ? "
												+ " and 	valid_upto  >= ? "
												+ " and 	min_qty <= ? "
												+ " and 	max_qty >= ? "
												+ " and 	(ref_no is not null) "
												+ " and ref_no = ( select max(ref_no) from pricelist "
																	+ " where price_list   = ? "
																	+ " and 	item_code = ? "
																	+ " and 	unit  = ? "
																	+ " and 	list_type = ? "
																	+ " and 	eff_from    <= ? "
																	+ " and 	valid_upto  >= ? "
																	+ " and 	min_qty <= ? "
																	+ " and 	max_qty >= ? "
																	+ " and 	(ref_no is not null) ) ";	
									//System.out.println("select qry maxRefNoStr.." + sql);
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,priceList);
									pstmt.setString(2,itemCode);
									pstmt.setString(3,unit);
									pstmt.setString(4,listType);
									pstmt.setTimestamp(5,OrderDate);
									pstmt.setTimestamp(6,OrderDate);
									pstmt.setDouble(7,totQty);
									pstmt.setDouble(8,totQty);

									pstmt.setString(9,priceList);
									pstmt.setString(10,itemCode);
									pstmt.setString(11,unit);
									pstmt.setString(12,listType);
									pstmt.setTimestamp(13,OrderDate);
									pstmt.setTimestamp(14,OrderDate);
									pstmt.setDouble(15,totQty);
									pstmt.setDouble(16,totQty);

									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   maxRefNoStr  = rs.getString(1) == null ? "" : rs.getString(1);
									}
									//System.out.println("maxRefNo .." + maxRefNo);
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									//System.out.println("orderDate in db date format " + orderDate.toString());
									// 21/03/09 manoharan
									//orderDateStr = genericUtility.getValidDateString(orderDate.toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
									orderDateStr = genericUtility.getValidDateString(orderDateStr,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
									
									//System.out.println("orderDate in appl date format " + orderDateStr);
									rate = distCommon.pickRate(priceList,orderDateStr,itemCode,maxRefNoStr,"L",totQty,conn);
									//System.out.println("rate from distcommon>>>>>>>>> " + rate);
								}//end cnt >= 1	
								System.out.println("Rate>>>>>>>>" +rate);
								if(rate <= 0)
								{		
									do
									{
										
										priceListParent = null;
										sql="select (case when price_list__parent is null  then '' else price_list__parent end )"
											+"from pricelist_mst where price_list = ?"
											+"and list_type =?  and price_list__parent is not null";								
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,priceList);
										pstmt.setString(2,listType);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
											priceListParent  = rs.getString(1) == null ? "" : rs.getString(1);
										}
										//System.out.println("priceListParent .." + priceListParent);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										//System.out.println("priceListParent null codition:-" + priceListParent);
										if(priceListParent == null || priceListParent.trim().length() == 0 || "null".equalsIgnoreCase( priceListParent ) )
										{
											break;
										}
										listType = distCommon.getPriceListType(priceListParent,conn);
										if(listType.equalsIgnoreCase("L"))
										{
										 sql1 = " select count(1)  from pricelist"
													+ " where price_list   = ? "
													+ " and 	item_code = ? "
													+ " and 	unit  = ? "
													+ " and 	list_type = ? "
													+ " and 	eff_from    <= ? "
													+ " and 	valid_upto  >= ? "
													+ " and 	min_qty <= ? " 
													+ " and 	max_qty >= ? ";
											
											pstmt= conn.prepareStatement(sql1);
											pstmt.setString(1,priceList);
											pstmt.setString(2,itemCode);
											pstmt.setString(3,unit);
											pstmt.setString(4,listType);
											pstmt.setTimestamp(5,OrderDate);
											pstmt.setTimestamp(6,OrderDate);
											pstmt.setDouble(7,totQty);
											pstmt.setDouble(8,totQty);
											rs = pstmt.executeQuery(); 
											if(rs.next())
											{
											   cnt = rs.getInt(1) ;
											}
											//System.out.println("cnt .." + cnt);
											rs.close();
											pstmt.close();
											pstmt = null;
											rs = null;
											
													
													
										}
										else
										{
										sql = " select count(1)  from pricelist"
											+ " where price_list   = ? "
											+ " and 	item_code = ? "
											+ " and 	unit  = ? "
											+ " and 	list_type = ? "
											+ " and 	eff_from    <= ? "
											+ " and 	valid_upto  >= ? "
											+ " and 	min_qty <= ? " 
											+ " and 	max_qty >= ? " 
											+ " and 	(ref_no is not null) ";
											
										//System.out.println("select qry from price_list.." + sql);
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,priceList);
										pstmt.setString(2,itemCode);
										pstmt.setString(3,unit);
										pstmt.setString(4,listType);
										pstmt.setTimestamp(5,OrderDate);
										pstmt.setTimestamp(6,OrderDate);
										pstmt.setDouble(7,totQty);
										pstmt.setDouble(8,totQty);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
										   cnt = rs.getInt(1) ;
										}
										//System.out.println("cnt .." + cnt);
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										}
										System.out.println("Count " + cnt);
										if( cnt >= 1)
										{
											//sql = " select max(ref_no) from pricelist"
											sql = " select lot_no__from from pricelist"
														+ " where price_list   = ? "
														+ " and 	item_code =? "
														+ " and 	unit  = ? "
														+ " and 	list_type =? "
														+ " and 	eff_from    <= ? "
														+ " and 	valid_upto  >= ? "
														+ " and 	min_qty <= ? "
														+ " and 	max_qty >=? "
														+ " and 	(ref_no is not null) "
														+ " and ref_no = ( select max(ref_no) from pricelist "
																				+ " where price_list   = ? "
																				+ " and 	item_code = ? "
																				+ " and 	unit  = ? "
																				+ " and 	list_type = ? "
																				+ " and 	eff_from    <= ? "
																				+ " and 	valid_upto  >= ? "
																				+ " and 	min_qty <= ? "
																				+ " and 	max_qty >= ? "
																				+ " and 	(ref_no is not null) ) ";	
											//System.out.println("select qry from price_list.." + sql);
											pstmt= conn.prepareStatement(sql);
											pstmt.setString(1,priceList);
											pstmt.setString(2,itemCode);
											pstmt.setString(3,unit);
											pstmt.setString(4,listType);
											pstmt.setTimestamp(5,OrderDate);
											pstmt.setTimestamp(6,OrderDate);
											pstmt.setDouble(7,totQty);
											pstmt.setDouble(8,totQty);

											pstmt.setString(9,priceList);
											pstmt.setString(10,itemCode);
											pstmt.setString(11,unit);
											pstmt.setString(12,listType);
											pstmt.setTimestamp(13,OrderDate);
											pstmt.setTimestamp(14,OrderDate);
											pstmt.setDouble(15,totQty);
											pstmt.setDouble(16,totQty);

											rs = pstmt.executeQuery(); 
											if(rs.next())
											{
												maxRefNoStr  = rs.getString(1) == null ? "" : rs.getString(1);
											}
											 
											//System.out.println("maxRefNo .." + maxRefNo);
											rs.close();
											pstmt.close();
											pstmt = null;
											rs = null;
										}//end cnt >= 1
										else
										{
											priceList =	priceListParent;
											continue;
										}
										//System.out.println("orderDateStr before parsing:- " + orderDateStr);
										if(orderDateStr.indexOf("/") == -1)
										{
											orderDateStr = genericUtility.getValidDateString(orderDateStr,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
										}
										else
										{
											orderDateStr = orderDateStr;
										}
										//System.out.println("orderDateStr for rate:- " + orderDateStr);
										//System.out.println(" priceListParent:- " + priceListParent + " priceListParent:- " + priceListParent + " itemCode:- " + itemCode + " maxRefNoStr:- " + maxRefNoStr + " totQty:- " + totQty);
										rate = distCommon.pickRate(priceListParent,orderDateStr,itemCode,maxRefNoStr,"L",totQty,conn);
										//System.out.println("rate :- " + rate);
										if(rate >0)
										{
											priceList =	priceListParent;
											break;
										}
										
									}
									while(true);								
								}
								System.out.println("totQty>>>>>>>>>>>>>>>> " + totQty);
								System.out.println("rate>>>>>>>>>>>>>>>> " + rate);
								
								ordValue = totQty * rate ; 
								System.out.println("setting calculated ordValue>>>>>>>>>>>>>>>> " +ordValue);
								valueXmlString.append("<ord_value>").append("<![CDATA["+ deciFormater.format( ordValue )+"]]>").append("</ord_value>");	
								siteCodeShip = genericUtility.getColumnValue("site_code",dom);
								
								noArt = distCommon.getNoArt(siteCodeShip,custCode,itemCode,"NULL",totQty,'B',acShipperQty,acIntegralQty,conn);
								//System.out.println("getNoArt()......noArt..."+noArt);
								shipperQty = acShipperQty;
								intQty = acIntegralQty;
								noArt1 = distCommon.getNoArt(siteCodeShip,custCode,itemCode,"NULL",totQty,'S',acShipperQty,acIntegralQty,conn);
								//System.out.println("getNoArt()......noArt1..."+noArt1);
								//balQty = totQty - (int)(shipperQty * noArt1);								
								balQty = totQty - (double)(shipperQty * noArt1);								
								noArt2 = distCommon.getNoArt(siteCodeShip,custCode,itemCode,"NULL",balQty,'I',acShipperQty,acIntegralQty,conn);
								//System.out.println("getNoArt().......noArt2.."+noArt2);
								intQty = acIntegralQty;
								shipperQty = shipperQty * noArt1;
								intQty = intQty * noArt2;
								looseQty = totQty - (shipperQty + intQty);								
								lsStr = " Shipper Quantity = "+shipperQty+ "   Integral Quantity = " +intQty+  "  Loose Quantity = "+looseQty ;
								valueXmlString.append("<st_shipper>").append("<![CDATA["+lsStr+"]]>").append("</st_shipper>");	
								//System.out.println("valueXmlString :["+valueXmlString.toString()+"]");
								
							} //end  totqt
							else if(currentColumn.trim().equalsIgnoreCase("cust_item__ref"))
							{
								custItemRef = genericUtility.getColumnValue("cust_item__ref",dom);
								custItemRef = custItemRef == null ? "" : custItemRef.trim();	
								custCode = genericUtility.getColumnValue("cust_code",dom1);
								custCode = custCode == null ? "" : custCode.trim();
								//System.out.println("custItemRef.." + custItemRef);
								if(custItemRef.length()>0)
								{
									sql = " select item_code, descr "
													+ " from customeritem"
													+ " where cust_code = ?"											
													+ " and item_code__ref = ?" ;
									//System.out.println("select custItemRef var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									pstmt.setString(2,custItemRef);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   itemCode = rs.getString(1) == null ?"" : rs.getString(1) ;
									   itemCodeDescr = rs.getString(2) == null ?"" : rs.getString(2) ;
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									valueXmlString.append("<item_code>").append("<![CDATA["+itemCode.trim()+"]]>").append("</item_code>");
									sql= "select descr  from item where item_code =? ";
									//System.out.println("select custItemRef var_value.." + sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCode);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
									   itemDescr = rs.getString(1) == null ?"" : rs.getString(1) ;									   
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									valueXmlString.append("<descr>").append("<![CDATA["+itemDescr.trim()+"]]>").append("</descr>");								
								}							
							}
							// end cust_item__ref
							itemCodeDescr = itemCodeDescr == null ? "" : itemCodeDescr ;
							//valueXmlString.append("<cust_item_ref_descr>").append("<![CDATA["+itemCodeDescr.trim()+"]]>").append("</cust_item_ref_descr>");
							valueXmlString.append("</Detail2>");
							break;
		   	}//end of switch
			valueXmlString.append("</Root>");
			
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null)rs.close();
					rs = null;
					if(pstmt != null)pstmt.close();
					pstmt = null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
			{
			  d.printStackTrace();
			  throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		//System.out.println("valueXmlString:::::"+valueXmlString.toString());
		return ( valueXmlString == null || valueXmlString.toString().trim().length() == 0 ? "" : valueXmlString.toString() );
	 }//END OF ITEMCHANGE
	 /*private String checkNull(String input) {
		if(input == null)
		{
			input ="";
		}
	
	
			// TODO Auto-generated method stub
		return input;
	}*/
	public int NoOfSchedule( int maxScheduleNo,Document dom, Document dom1) throws Exception
	 {
		String sch_[] = new String[maxScheduleNo + 1];
		int schedule = 0;
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			//System.out.println("manohar in NoOfSchedule() " + maxScheduleNo);
			for(int ctr = 1 ; ctr <= maxScheduleNo; ctr++)
			{
				sch_[ctr] = genericUtility.getColumnValue("sch_" + ctr,dom1);
				//System.out.println("sch_" + ctr + ":- " + sch_[ctr]);
				if(sch_[ctr] != null && sch_[ctr].trim().length() >= 0 && !sch_[ctr].trim().equalsIgnoreCase( "null" ) )
					schedule ++;
				//System.out.println("returning schedule:- " +schedule);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		return schedule;
	 }// end NoOfSchedule
	 
	 public String getPriceListType(String priceList, Connection conn) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String listType = null;
		String sql = null;
		
		try
		{
			sql = " select list_type from pricelist where price_list =?";
			//System.out.println("select qry from list_type.." + sql);
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1,priceList);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
			   listType = rs.getString(1) == null ? "" : rs.getString(1);
			}
			//System.out.println("priceList .." + priceList);
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
		}
		catch(Exception e)
		{
			//System.out.println("Exception...[getPriceListType] "+sql+e.getMessage());
			e.printStackTrace();
			throw new ITMException( e );
		}
		return listType;
	}
	private double getTotAmtForRep( Document dom ) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		double totAmount = 0;
		double quantity = 0;
		String qtyStr =  "";
		String rateStr = "";
		double rate = 0;
		String retFlag = null;
		
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			parentNodeList = dom.getElementsByTagName("Detail2");
			int childNodeListLength = parentNodeList.getLength();
			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				System.out.println("ctr ::"+ctr);
				parentNode = parentNodeList.item(ctr);
				
				qtyStr = genericUtility.getColumnValueFromNode("quantity", parentNode);
				retFlag = genericUtility.getColumnValueFromNode("ret_rep_flag", parentNode);
				quantity = Double.parseDouble( qtyStr == null || qtyStr.trim().length() == 0 || "null".equalsIgnoreCase( qtyStr ) ? "0" : qtyStr.trim() );
				rateStr = genericUtility.getColumnValueFromNode("rate", parentNode);
				rate = Double.parseDouble( rateStr == null || rateStr.trim().length() == 0 || "null".equalsIgnoreCase( rateStr ) ? "0" : rateStr.trim() );
				if( retFlag != null && "P".equalsIgnoreCase( retFlag.trim() ) )
				{
					totAmount = totAmount + (quantity * rate);
				}
			} // end for
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		return totAmount;
	}
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}
	private static String getNumString( String iValStr )
	{
		return ( iValStr == null || iValStr.trim().length() == 0 || "null".equals( iValStr.trim() ) ? "0" : iValStr.trim() );
	}
	
	/*private String getSchDatesString(int maxScheduleNo, Document dom) throws Exception
	{
		String sch_[] = new String[maxScheduleNo + 1];
		String schXML = "";
		for(int ctr = 1 ; ctr <= maxScheduleNo; ctr++)
		{
			sch_[ctr] = genericUtility.getColumnValue("sch_" + ctr,dom);
			if(sch_[ctr] != null && sch_[ctr].trim().length() >= 0 && !sch_[ctr].trim().equalsIgnoreCase( "null" ) )
			{
				schXML += "<sch_"+(ctr) + ">" + sch_[ctr] + "</sch_"+(ctr) + ">" ;
			}
			
		}
		System.out.println("schXML ["+ schXML + "]");
		return schXML;
	}*/
	private String checkNullandTrim(String input) {
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
	
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}
 }// END OF MAIN CLASS