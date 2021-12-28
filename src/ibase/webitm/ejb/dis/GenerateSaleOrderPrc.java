/*
 develop by Ritesh 
 purpose - generate confirm  sale order and despatch order from receipt 
 */

package ibase.webitm.ejb.dis;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ProcessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ParameterMode;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;

@Stateless
public class GenerateSaleOrderPrc extends ProcessEJB implements GenerateSaleOrderPrcLocal,GenerateSaleOrderPrcRemote 
{
 
	E12GenericUtility genericUtility= new  E12GenericUtility();
	static
	{
		System.out.println("-- GenerateSaleOrderPrc called -- ");
		
	}
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";
		try
		{				
			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :SoHoldRefPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return retStr;
	
	} //end of confirm method
	public String process(Document dom, Document dom2, String windowName,String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("process called..20-10-2014....5:14  !!!!..");
		String sql = "";
		String errString = "";
		String siteCode = "";
		String  tranId = "",custCode="",conf="";
		
		//String orderDate=null;
		Date orderDate=null;
		String priceList="" ,deliverTo="" ,deliverTerm = "" ,transporter="" ,transporterMode="",prdCode="";
		
		
		int tranIdFound=0,cusCodeFound=0,prcpFound = 0;
		int deliverToFound=0;
		int deliveryTermFound=0;
		int transporterFound=0;
		int transporterModeFound=0;
		int pricelistFound=0;
		int periodCnt=0;
	
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		//System.out.println("tran id = "+tranId);
		try
		{
			System.out.println("process  starts!!!!!!!");
			//genericUtility = new GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			tranId = checkNull(genericUtility.getColumnValue("tran_id", dom));
			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
			//added by mahendra dated 28/Apr/2014
			//orderDate=genericUtility.getColumnValue("price_list", dom);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				
			
			
			/*if (genericUtility.getColumnValue("order_date", dom) == null)
			{
				orderDate = null;
			} 
		    else
		    {
		    	orderDate=sdf.format(genericUtility.getColumnValue("order_date", dom)).toString();
		    }*/
			
			System.out.println("orderDate  : "+orderDate);
			if(genericUtility.getColumnValue("order_date", dom) != null)
			{
				orderDate=sdf.parse(genericUtility.getColumnValue("order_date", dom));
			}
			
			priceList=checkNull(genericUtility.getColumnValue("price_list", dom));
			deliverTo=checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
			deliverTerm=checkNull(genericUtility.getColumnValue("dlv_term", dom));
			transporter=checkNull(genericUtility.getColumnValue("tran_code", dom));
			transporterMode=checkNull(genericUtility.getColumnValue("trans_mode", dom));
			System.out.println("orderDate !!!!!! :"+orderDate);
			System.out.println("priceList  :"+priceList);
			System.out.println("deliverTo  :"+deliverTo);
			System.out.println("deliverTerm  :"+deliverTerm);
			System.out.println("transporter  :"+transporter);
			System.out.println("transporterMode  :"+transporterMode);
			
			
			
			
			
			conn.setAutoCommit(false);
			if(tranId.trim() !=null && tranId.trim().length() >0)
			{
				sql = "select count(*) from porcp where tran_id = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					tranIdFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(tranIdFound <= 0)
				{
					System.out.println("Tran id not found");
					errString = itmDBAccessEJB.getErrorString("","VTTNNTFD    ","","",conn);
					return errString;
				}
				sql = "select count(*) from sorder where TRAN_ID__PORCP = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					prcpFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(prcpFound > 0)
				{
					System.out.println("sorder already generated for this p.reciept ");
					errString = itmDBAccessEJB.getErrorString("","VTSOGENAL    ","","",conn);
					return errString;
				}
				sql = "SELECT CONFIRMED FROM PORCP WHERE TRAN_ID = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					conf = rs.getString(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!"Y".equalsIgnoreCase(conf))
				{
					System.out.println("Tran id not confirmed");
					errString = itmDBAccessEJB.getErrorString("","VTPRCNTCNF    ","","",conn);
					return errString;
				}
			}else
			{
				errString = itmDBAccessEJB.getErrorString("","VTTNNTNL    ","","",conn);
				return errString;
			}
			if(custCode.trim() !=null && custCode.trim().length() >0)
			{
				sql = "select site_code from porcp where tran_id =  ? ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					siteCode = rs.getString("site_code");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				//sql = "select count(*) from site_customer where cust_code = ? and channel_partner = 'Y' and dis_link = 'A' and site_code = ? ";
				sql = "select count(*) from site_customer where cust_code = ? and channel_partner = 'Y' and site_code = ? ";//Change by chandrashekar 0n 12-AUG-2015
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setString(2,siteCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cusCodeFound = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cusCodeFound == 0)
				{
					//sql = "select count(*) from customer where cust_code = ? and channel_partner = 'Y' and dis_link = 'A' ";
					sql = "select count(*) from customer where cust_code = ? and channel_partner = 'Y' ";//Change by chandrashekar 0n 12-AUG-2015
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cusCodeFound = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(cusCodeFound == 0)
					{
						System.out.println("--cust_code validate--");
						errString = itmDBAccessEJB.getErrorString("","VTCSCDNTFD ","","",conn);
						return errString;
					}
				}
			}else
			{
				errString = itmDBAccessEJB.getErrorString("","VTCSCDNTNL ","","",conn);
				return errString;
			}
			
			
			//added by mahendra dated 30/APR/14
			
			//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			//Date d = new Date();
			//String formattedCurrentDate=dateFormat.format(d);
			//System.out.println("formattedCurrentDate:"+formattedCurrentDate);
			
					
			
			System.out.println("orderDate : "+orderDate);
			
			
			
			if(orderDate != null)
			{
				sql = "select code from period where ?  between fr_date and to_date";
		    	
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1, new java.sql.Date(orderDate.getTime()));
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					prdCode = rs.getString(1);

				}
				System.out.println("prdCode :"+prdCode);
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				sql = "select count(*) from period_stat where site_code = ? and prd_code = ?  and stat_sal='Y' ";
				System.out.println("sql1 :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, prdCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					periodCnt = rs.getInt(1);

				}
				System.out.println("periodCnt :"+periodCnt);
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("periodCnt :"+periodCnt);
				if (periodCnt == 0)
				{
					System.out.println("Invalid order date !!!");
					errString = itmDBAccessEJB.getErrorString("","VTORDTINV    ","","",conn);
					return errString;
					
				}
				
				
			}
			
			if(priceList.trim() !=null && priceList.trim().length() >0)
			{
				
				//sql = "select count(*) from gencodes where fld_value=? and FLD_NAME='PRICE_LIST' ";
				sql="select count(*) from pricelist_mst  where price_list=? ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					pricelistFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(pricelistFound == 0)
				{
					System.out.println("Invalid price list !!!!");
					errString = itmDBAccessEJB.getErrorString("","VTPRLSTINV    ","","",conn);
					return errString;
				}
				
			}
		    if(deliverTo.trim() !=null && deliverTo.trim().length() >0)
			{
				
				sql = "select count(*) from customer where cust_code=?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,deliverTo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					deliverToFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(deliverToFound == 0)
				{
					System.out.println("Invalid delivery To");
					errString = itmDBAccessEJB.getErrorString("","VTDLVTOINV    ","","",conn);
					return errString;
				}
				
			}
			 if(deliverTerm.trim() !=null && deliverTerm.trim().length() >0)
			{
				sql = "select count(*) from delivery_term where dlv_term=? ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,deliverTerm);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					deliveryTermFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(deliveryTermFound == 0)
				{
					System.out.println("Invalid delivery term !!!");
					errString = itmDBAccessEJB.getErrorString("","VTDLVTMINV    ","","",conn);
					return errString;
				}
				
			}
		     if(transporter.trim() !=null && transporter.trim().length() >0)
			{
				sql = "select count(*) from transporter where tran_code=?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,transporter);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					transporterFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(transporterFound == 0)
				{
					System.out.println("Invalid transporter !!!");
					errString = itmDBAccessEJB.getErrorString("","VTTRANCD1    ","","",conn);
					return errString;
				}
				
			}
			 if(transporterMode.trim() !=null && transporterMode.trim().length() >0)
			{
				sql = "select count(*) from gencodes where fld_value=? and FLD_NAME = 'TRANS_MODE' ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,transporterMode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					transporterModeFound = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(transporterModeFound == 0)
				{
					System.out.println("Invalid transporter mode !!!");
					errString = itmDBAccessEJB.getErrorString("","VTTRSMDINV    ","","",conn);
					return errString;
				}
				
			}
			
			
			
			if(tranIdFound > 0 && cusCodeFound > 0)
			{
				errString = generateSaleOrder(tranId,custCode,orderDate,priceList,deliverTo,deliverTerm,transporter,transporterMode,xtraParams,conn);
				errString = errString.toLowerCase();               // change on 18/apr/14 ..ritesh 
				System.out.println("err String from generateSaleOrder ="+errString);
				if((errString != null ) && (errString.indexOf("success") > -1 || errString.indexOf("vtsucc1") >-1 || errString.indexOf("confirmed") >-1))//Added by manoj dtd 23/08/2013 to check whether generated transaction is confirmed successfully
				{
					System.out.println("--order despatch successfully--");
					errString = itmDBAccessEJB.getErrorString("","VTPRCSUCC","","",conn);
					return errString;
				}
				else 
				{
					conn.rollback();
					System.out.println("--order not despatch--");
					errString = itmDBAccessEJB.getErrorString("","VTPRCNTSUC ","","",conn);
					return errString;
				}
			}
		}
		catch( Exception e)
		{			
				try 
				{
					conn.rollback();
				} 
				catch (SQLException ex) 
				{
					Logger.getLogger(GenerateSaleOrderPrc.class.getName()).log(Level.SEVERE, null, ex);
				}
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		finally
		{		
				try
				{
					if(errString != null && errString.trim().length() > 0)
					{
						
						if(errString.indexOf("VTPRCSUCC") > -1 )
							
						{
							conn.commit();
							System.out.println("--transaction commited--");
						}
						else
						{
							conn.rollback();
							System.out.println("--transaction rollback--");
						}
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;					
					}		
					if( conn != null && ! conn.isClosed() )
					{
						conn.close();
						conn = null;
					}
					
					if(rs != null)
					{
						rs.close();
						rs = null;
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
	
	private String generateSaleOrder(String tranId,String custCode,Date orderDateDom,String priceListDom,String deliverToDom,String deliverTermDom,String transporterDom,String transporterModeDom,String xtraParams, Connection conn) throws RemoteException, ITMException
	{	
		String  userId = "";
		String  termId = "";
		String  orderType = ""; 
		String  remarks = "";
		String  projCode = "";
		String  sql = "";
		String  siteCode = "";	
		String  empCode = "";
		String  crTerm = "";
		String  currCodeFrt = "";
		String  currCodeIns = "";
		String	quntyStduom = "";
		String	exchRate1 = "";
		String	itemSer  = "";
		String	currencyCode = "";
		String	transMode = "";
		String	stanCode="";
		String	itemCode = "";
		String	quantity ="";
		String	rate="";
		String	unit="";
		String	conQtyStd ="";
		String	unitRate="";
		String	taxClass="";
		String	taxChap="";
		String groupCode="";
		String	currCode1 ="",currCode2="",custCodeDlv="";
		String	taxEnv = "",purcOrder = "",tranCode="",pricelistTmp="",pricelistClgTmp="";
		String	packCode ="",convRtuomStduom="",rateStduom="",dlvTo="";
		String lineno = "",noArt = "",netAmt = "",frtTerm="",deliveryTerm="",commpercon="",commPercOn2="";
		String pin = "", stateCode = "",city = "",countCode = "",unitStd = "",salesPersTmp="",crTermTmp="",salesPersTmp1="",salesPersTmp2="";
		String transmodeFromCust="",custCodeBil="",pricelist="",pricelistClg="",salespers="",salespers1="",salespers2="",dlvTO="";
		String add1="",add2="",add3="",tele1="",tele2="",tele3="",invoiceNo="",packInstr="",palletNo="",dlvDescr="",lineNo="";
		Date orderDt = null;
		Date orderDateInput=null;
		Date lrDate=null;
		String remarks2="",remarks3="",stanCodeInit="";//added by priyanka on 1/11/14
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double exchRate =0,rateClg = 0,discount = 0,minShelf=0,maxShelf=0;
		int lnno = 0;
		boolean isCustsalePers = false,isCustsalePers1 = false,isCustsalePers2 = false;
		String commPercOn1="";
		double commperc=0,commPerc1=0,commPerc2=0;
		double minShelfPerc=0,shelife=0;
		double exchRateFr=0;
		ResultSet rs2 = null;
		PreparedStatement pstmt2 =null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = null;
		Timestamp currDate = null;
		StringBuffer xmlBuff = null;
		String xmlString = null,retString  = null;
		FinCommon finCommon = null;
		try   
		{
				finCommon = new FinCommon();
				DistCommon dComm = new DistCommon();
				itmDBAccessEJB = new ITMDBAccessEJB();
				System.out.println("@@@@@@@ generateSaleOrder  function called");
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
				currDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
				//DistCommon distCommom = new DistCommon();
				//ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
				termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); System.out.println("--term id--"+termId);
				sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, userId);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					empCode = rs.getString("EMP_CODE")== null ?"":rs.getString("EMP_CODE");
				}
				
				pstmt.close();
				pstmt = null;					
				rs.close();
				rs = null;

				sql = "select tran_type,tran_date,item_ser,site_code,curr_code,exch_rate,tran_code," +
					  "trans_mode,curr_code__frt,curr_code__ins,purc_order,lr_date,invoice_no,remarks2,remarks3,stan_code__init from porcp where tran_id = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					orderType = rs.getString("tran_type") == null ? " " : rs.getString("tran_type");
					orderDt = rs.getDate("tran_date") ;
					siteCode = rs.getString("SITE_CODE") == null ? " " : rs.getString("SITE_CODE")  ;
					itemSer = rs.getString("item_ser")== null ? " " : rs.getString("item_ser");
					currencyCode = rs.getString("curr_code") == null ? " " : rs.getString("curr_code");
					exchRate1 = rs.getString("exch_rate") == null ? " " : rs.getString("exch_rate");
					transMode = rs.getString("trans_mode") == null ? " " : rs.getString("trans_mode");
					currCodeFrt = rs.getString("curr_code__frt") == null ? " " : rs.getString("curr_code__frt");
					currCodeIns = rs.getString("curr_code__ins") == null ? " " : rs.getString("curr_code__ins");
					purcOrder = rs.getString("purc_order") == null ? " " : rs.getString("purc_order");
					tranCode = rs.getString("tran_code") == null ? " " : rs.getString("tran_code");
					invoiceNo = rs.getString("invoice_no") == null ? " " : rs.getString("invoice_no");
					System.out.println("Added by mahendra 05-05-2014");
					lrDate= rs.getDate("lr_date");
					remarks2=checkNull(rs.getString("remarks2"));//added by priyanka on 1/11/14
					remarks3=checkNull(rs.getString("remarks3"));//added by priyanka on 1/11/14
					stanCodeInit=checkNull(rs.getString("stan_code__init"));//added by priyanka on 1/11/14
				}
				System.out.println("lrDate  !!! "+lrDate);
				System.out.println(">>>>>>>>>>>remarks2  !!! "+remarks2);
				System.out.println(">>>>>>>>>>>>>>>>>remarks3  !!! "+remarks3);
				System.out.println(">>>>>>>>>>>>>>stanCodeInit  !!! "+stanCodeInit);
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;

				sql ="select price_list, price_list__clg from site_customer where site_code = ? and cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					pricelist = rs.getString("price_list");
					pricelistClg = rs.getString("price_list__clg");
				}
				
				pstmt.close();
				pstmt = null;					
				rs.close();
				rs = null;
				
				sql ="select cr_term,stan_code ,city,count_code,pin,state_code,frt_term,cust_name,group_code," +
					 "trans_mode,cust_code__bil,price_list,price_list__clg,sales_pers,sales_pers__1,sales_pers__2"+
					 " from customer where cust_code = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					crTerm = rs.getString("cr_term")== null ?" ":rs.getString("cr_term");
					//stanCode = rs.getString("stan_code")==null ?" ":rs.getString("stan_code");
					//city = rs.getString("city")== null ?" ":rs.getString("city");
					//countCode = rs.getString("count_code")== null ?" ":rs.getString("count_code");
					//pin = rs.getString("pin")== null ?" ":rs.getString("pin");
					//stateCode = rs.getString("state_code")== null ?" ":rs.getString("state_code");
					custCodeBil = rs.getString("cust_code__bil")== null ?" ":rs.getString("cust_code__bil");
					salespers = rs.getString("sales_pers")== null ?" ":rs.getString("sales_pers");
					salespers1 = rs.getString("sales_pers__1")== null ?" ":rs.getString("sales_pers__1");
					salespers2 = rs.getString("sales_pers__2")== null ?" ":rs.getString("sales_pers__2");
					frtTerm = rs.getString("frt_term")== null ?" ":rs.getString("frt_term");
					//dlvTo = rs.getString("cust_name")== null ?" ":rs.getString("cust_name");
					groupCode = rs.getString("group_code")== null ?" ":rs.getString("group_code");
					
					//if(transMode == null || transMode.trim().length()==0)
					//{
						//transMode = rs.getString("trans_mode")== null ?" ":rs.getString("trans_mode");
					//}
					if(pricelist == null || pricelist.trim().length()==0)
					{
						pricelist = rs.getString("price_list")== null ?" ":rs.getString("price_list");
					}
					if(pricelistClg == null || pricelistClg.trim().length()==0)
					{
						pricelistClg = rs.getString("price_list__clg")== null ?" ":rs.getString("price_list__clg");
					}
				}
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;

				sql = "select sales_pers,sales_pers__1,sales_pers__2,cr_term from customer_series where cust_code = ? and item_ser = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setString(2,itemSer);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					salesPersTmp = rs.getString("sales_pers") == null ? " " : rs.getString("sales_pers");
					crTermTmp = rs.getString("cr_term") == null ? " " : rs.getString("cr_term");
					salesPersTmp1 = rs.getString("sales_pers__1") == null ? " " : rs.getString("sales_pers__1");
					salesPersTmp2 = rs.getString("sales_pers__2") == null ? " " : rs.getString("sales_pers__2");

				}
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				System.out.println("itemSer   :::::"+itemSer);
				if (salesPersTmp != null && salesPersTmp.trim().length() > 0 )
				{
					salespers = salesPersTmp;
					isCustsalePers = true;
				}
				if (salesPersTmp1 != null && salesPersTmp1.trim().length() > 0 )
				{
					salespers1 = salesPersTmp1;
					isCustsalePers1 = true;
				}
				if (salesPersTmp2 != null && salesPersTmp2.trim().length() > 0 )
				{
					salespers2 = salesPersTmp2;
					isCustsalePers2 = true;
				}
				if (crTermTmp != null && crTermTmp.trim().length() > 0 )
				{
					crTerm = crTermTmp;
				}
				System.out.println("itemSer   @@@@"+itemSer);
				sql = "select price_list,price_list__clg from site_customer where site_code = ? and cust_code=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					pricelistTmp = rs.getString("price_list")==null?"":rs.getString("price_list");
					pricelistClgTmp = rs.getString("price_list__clg")==null?"":rs.getString("price_list__clg");

				}
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				
				if (pricelistTmp != null && pricelistTmp.trim().length() > 0 )
				{
					pricelist = pricelistTmp;
				}
				if (pricelistClgTmp != null && pricelistClgTmp.trim().length() > 0 )
				{
					pricelistClg = pricelistClgTmp;
				}
				
				sql = "select tax_class,tax_chap,tax_env from porcpdet where tran_id = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					taxClass = rs.getString("tax_class") == null ? "" :rs.getString("tax_class");
					taxChap = rs.getString("tax_chap") == null ? "" :rs.getString("tax_chap");
					taxEnv = rs.getString("tax_env") == null ? "" :rs.getString("tax_env");
				}
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				System.out.println("itemSer   !!!!!"+itemSer);
				//commented by manoj on dated 13-oct-14
				
				/*sql = "select dlv_term, TRAN_CODE, CURR_CODE__FRT ,CURR_CODE__INS,addr1,addr2,addr3,tele1,tele2,tele3  from customer where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//priceList = rs2.getString("price_list");
					//deliveryTerm = deliveryTerm == " " ? rs2.getString("dlv_term") : deliveryTerm;

					deliveryTerm=rs.getString("dlv_term");
					if(tranCode == null || tranCode.trim().length()==0)
					{
						tranCode =rs.getString("TRAN_CODE")==null?" ":rs.getString("TRAN_CODE");
					}
					if(currCodeFrt == null || currCodeFrt.trim().length()==0)
					{
						currCodeFrt =rs.getString("CURR_CODE__FRT")==null?" ":rs.getString("CURR_CODE__FRT");
					}
					if(currCodeIns == null || currCodeIns.trim().length()==0)
					{
						currCodeIns =rs.getString("CURR_CODE__INS")==null?" ":rs.getString("CURR_CODE__INS");
					}
					add1 = rs.getString("addr1")== null ?" ":rs.getString("addr1");
					add2 = rs.getString("addr2")==null ?" ":rs.getString("addr2");
					add3 = rs.getString("addr3")== null ?" ":rs.getString("addr3");
					tele1 = rs.getString("tele1")== null ?" ":rs.getString("tele1");
					tele2 = rs.getString("tele2")== null ?" ":rs.getString("tele2");
					tele3 = rs.getString("tele3")== null ?" ":rs.getString("tele3");

				}
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;*/
				
				
				System.out.println("itemSer   ######"+itemSer);
				if(!isCustsalePers)
				{
					sql = " select  comm_perc , comm_perc__on  	from   customer_series 	where  cust_code = ? and  item_ser  = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					pstmt.setString(2,itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						if(commperc == 0 )
						{
							commperc =rs.getDouble("comm_perc");
						}
						if(tranCode == null || tranCode.trim().length()==0)
						{
							commpercon =rs.getString("comm_perc__on")==null?" ":rs.getString("comm_perc__on");
						}
					}
					rs.close();
					rs =null;
					pstmt.close();
					pstmt = null;
				}
				
				
				if(commperc == 0)
				{
					sql = " select comm_perc 	from   sales_pers  where  sales_pers = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salespers);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commperc = rs.getDouble("comm_perc");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if(commpercon.trim().length() == 0)
				{
					sql = " select comm_perc__on 	from   sales_pers  where  sales_pers = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salespers);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commpercon = checkNull(rs.getString("comm_perc__on"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if(!isCustsalePers1)
				{
					sql = " select  comm_perc__1 , comm_perc__on_1  	from   customer_series 	where  cust_code = ? and  item_ser  = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemSer);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commPerc1 = rs.getDouble("comm_perc__1");
						commPercOn1 = checkNull(rs.getString("comm_perc__on_1"));

					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if(commPerc1 == 0)
				{
					sql = " select comm_perc 	from   sales_pers  where  sales_pers = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salespers1);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commPerc1 = rs.getDouble("comm_perc");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if(commPercOn1.trim().length() == 0)
				{
					sql = " select comm_perc__on 	from   sales_pers  where  sales_pers = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salespers1);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commPercOn1 = checkNull(rs.getString("comm_perc__on"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				
				if(!isCustsalePers2)
				{
					sql = " select  comm_perc__2 , comm_perc__on_2  	from   customer_series 	where  cust_code = ? and  item_ser  = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemSer);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commPerc2 = rs.getDouble("comm_perc__2");
						commPercOn2 = checkNull(rs.getString("comm_perc__on_2"));

					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if(commPerc2 == 0)
				{
					sql = " select comm_perc 	from   sales_pers  where  sales_pers = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salespers2);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commPerc2 = rs.getDouble("comm_perc");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if(commPercOn2.trim().length() == 0)
				{
					sql = " select comm_perc__on 	from   sales_pers  where  sales_pers = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salespers2);   //change done by kunal on 26/04/13 change  salePers1 to salePers2
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						commPercOn2 = checkNull(rs.getString("comm_perc__on"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
			    System.out.printf("data --------"+orderType,orderDt,siteCode,projCode,currencyCode,exchRate,remarks);
			    
			    
			    //added by mahendra dated 28/APR/2014
			    System.out.println("mahendra testing :  checked value!@@@@!!!!!");
			    System.out.println("pricelist :"+pricelist);
			    System.out.println("priceList_input :"+priceListDom);
			    System.out.println("orderDate_input :"+orderDateDom);
			    
			   
			    System.out.println("orderDt :"+orderDt);
			    System.out.println("orderDate_input :"+orderDateDom);  
			 
			    orderDt=orderDateDom==null?orderDt:orderDateDom;
			    System.out.println("orderDt final value :"+orderDt);
			    pricelist=priceListDom==""?pricelist:priceListDom;
			    custCodeDlv=deliverToDom==""?custCode:deliverToDom;
			    /*-------------------added by manoj on dated 13-oct-14----------------------------------------------------------------------*/
			    
				sql = "select dlv_term, TRAN_CODE, addr1,addr2,addr3,tele1,tele2,tele3,trans_mode,stan_code,city,count_code,pin,state_code,CURR_CODE__FRT,CURR_CODE__INS ,cust_name from customer where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCodeDlv);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//priceList = rs2.getString("price_list");
					//deliveryTerm = deliveryTerm == " " ? rs2.getString("dlv_term") : deliveryTerm;

					deliveryTerm=rs.getString("dlv_term");
					if(tranCode == null || tranCode.trim().length()==0)
					{
						tranCode =rs.getString("TRAN_CODE")==null?" ":rs.getString("TRAN_CODE");
					}
					if(currCodeFrt == null || currCodeFrt.trim().length()==0)
					{
						currCodeFrt =rs.getString("CURR_CODE__FRT")==null?" ":rs.getString("CURR_CODE__FRT");
					}
					if(currCodeIns == null || currCodeIns.trim().length()==0)
					{
						currCodeIns =rs.getString("CURR_CODE__INS")==null?" ":rs.getString("CURR_CODE__INS");
					}
					add1 = rs.getString("addr1")== null ?" ":rs.getString("addr1");
					add2 = rs.getString("addr2")==null ?" ":rs.getString("addr2");
					add3 = rs.getString("addr3")== null ?" ":rs.getString("addr3");
					tele1 = rs.getString("tele1")== null ?" ":rs.getString("tele1");
					tele2 = rs.getString("tele2")== null ?" ":rs.getString("tele2");
					tele3 = rs.getString("tele3")== null ?" ":rs.getString("tele3");
					stanCode = rs.getString("stan_code")==null ?" ":rs.getString("stan_code");
					city = rs.getString("city")== null ?" ":rs.getString("city");
					countCode = rs.getString("count_code")== null ?" ":rs.getString("count_code");
					pin = rs.getString("pin")== null ?" ":rs.getString("pin");
					stateCode = rs.getString("state_code")== null ?" ":rs.getString("state_code");
					System.out.println("");
					//dlvTO = rs.getString("cust_name") == null ?" ":rs.getString("cust_name");
					dlvDescr = rs.getString("cust_name");
					System.out.println("@@@@@@ dlvTo :"+dlvTO);
					if(transMode == null || transMode.trim().length()==0)
					{
						transMode = rs.getString("trans_mode")== null ?" ":rs.getString("trans_mode");
					}

				}
				
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				System.out.println("dlvAdd1 :"+add1);
				System.out.println("dlvAdd2 :"+add2);
				System.out.println("dlvAdd3 :"+add3);
				System.out.println("transMode :"+transMode);
				System.out.println("tranCode :"+tranCode);
				System.out.println("tele1 :"+tele1+" @tele2 :"+tele2+" @tele3 :"+tele3);
				System.out.println("CURR_CODE__FRT  @@@@"+currCodeFrt);
				System.out.println("currCodeIns  @@@@"+currCodeIns);
			    /*------------------------------------------------------------------------------------*/
			    
			    deliveryTerm=deliverTermDom==""?deliveryTerm:deliverTermDom;	
			    transMode=transporterModeDom==""?transMode:transporterModeDom;				
			    tranCode=transporterDom==""?tranCode:transporterDom;
			    
			    System.out.println("orderDt :"+orderDt);
			    System.out.println("pricelist :"+pricelist);
			    System.out.println("dlvTo :"+dlvTo);
			    System.out.println("deliveryTerm :"+deliveryTerm);
			    System.out.println("transMode :"+transMode);
			    System.out.println("tranCode :"+tranCode);
			    System.out.println("dlvDescr :"+dlvDescr);
			    
			    
			    
			    
			    
			    
			    
			    System.out.println("pricelist :"+pricelist);
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
				xmlBuff.append("<order_type><![CDATA["+ orderType +"]]></order_type>");
				xmlBuff.append("<order_date><![CDATA["+ sdf.format(orderDt).toString() +"]]></order_date>");
				xmlBuff.append("<item_ser><![CDATA["+ itemSer.trim()   +"]]></item_ser>");
				xmlBuff.append("<site_code><![CDATA["+ siteCode.trim() +"]]></site_code>");
				xmlBuff.append("<site_code__ship><![CDATA["+ siteCode.trim() +"]]></site_code__ship>");
				xmlBuff.append("<cust_code><![CDATA["+ groupCode.trim() +"]]></cust_code>");
				xmlBuff.append("<dlv_to><![CDATA["+ dlvDescr +"]]></dlv_to>");
				xmlBuff.append("<cust_code__bil><![CDATA["+ custCodeBil.trim() +"]]></cust_code__bil>");
				xmlBuff.append("<cust_code__dlv><![CDATA["+ custCodeDlv.trim() +"]]></cust_code__dlv>");
				xmlBuff.append("<stan_code><![CDATA["+ stanCode.trim() +"]]></stan_code>");
				xmlBuff.append("<STAN_CODE__INIT><![CDATA["+ stanCode.trim() +"]]></STAN_CODE__INIT>");
				xmlBuff.append("<cr_term><![CDATA["+ crTerm +"]]></cr_term>");
				xmlBuff.append("<curr_code__frt><![CDATA["+ currencyCode.trim() +"]]></curr_code__frt>");
				xmlBuff.append("<trans_mode><![CDATA["+ transMode.trim() +"]]></trans_mode>");
				xmlBuff.append("<emp_code__ord><![CDATA["+ empCode.trim() +"]]></emp_code__ord>");
				xmlBuff.append("<curr_code__frt><![CDATA["+ currCodeFrt.trim() +"]]></curr_code__frt>");
				xmlBuff.append("<curr_code__ins><![CDATA["+ currCodeIns.trim() +"]]></curr_code__ins>");
				xmlBuff.append("<curr_code><![CDATA["+ currencyCode.trim() +"]]></curr_code>");
				//xmlBuff.append("<due_date><![CDATA["+ sdf.format(orderDt).toString() +"]]></due_date>");
				
				//sql = "select customer.curr_code curr_code_cust,finent.curr_code curr_code_fin from customer customer,finent finent "+
				 //     " where finent.fin_entity in (select SITE.fin_entity from site SITE where SITE.site_code = ?) AND customer.cust_code = ? ";
				sql = "select curr_code from customer where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, groupCode);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					currCode1 = rs.getString("curr_code")==null?"":rs.getString("curr_code");
				}
				if(rs !=null)
					rs.close();
				rs = null;
				if(pstmt !=null)
					pstmt.close();
				pstmt = null;
				
				sql = "select curr_code from finent where fin_entity in (select fin_entity from site where site_code = ? )";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					currCode2 = rs.getString("curr_code")==null?"":rs.getString("curr_code");
				}
				if(rs !=null)
					rs.close();
				rs = null;
				if(pstmt !=null)
					pstmt.close();
				pstmt = null;
				if(currCode1.equalsIgnoreCase(currCode2))
				{
					exchRateFr = 1.0;
					xmlBuff.append("<exch_rate><![CDATA["+  exchRateFr +"]]></exch_rate>");// CAN NOT BE NULL
					xmlBuff.append("<exch_rate__comm><![CDATA["+  exchRateFr +"]]></exch_rate__comm>");	
					xmlBuff.append("<exch_rate__comm_1><![CDATA["+ exchRateFr +"]]></exch_rate__comm_1>");
					xmlBuff.append("<exch_rate__comm_2><![CDATA["+ exchRateFr +"]]></exch_rate__comm_2>");
				}else
				{
					exchRateFr = finCommon.getDailyExchRateSellBuy(currCode1,"",siteCode,sdf.format(orderDt).toString() , "S", conn);
				    System.out.println("((((((((( "+exchRateFr+" ))))))))))))");
				    xmlBuff.append("<exch_rate><![CDATA["+  exchRateFr +"]]></exch_rate>");// CAN NOT BE NULL
					xmlBuff.append("<exch_rate__comm><![CDATA["+  exchRateFr +"]]></exch_rate__comm>");	
					xmlBuff.append("<exch_rate__comm_1><![CDATA["+ exchRateFr +"]]></exch_rate__comm_1>");
					xmlBuff.append("<exch_rate__comm_2><![CDATA["+ exchRateFr +"]]></exch_rate__comm_2>");
				}
				xmlBuff.append("<tax_class><![CDATA["+taxClass.trim() +"]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA["+taxChap.trim() +"]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA["+ taxEnv.trim()+"]]></tax_env>");
				xmlBuff.append("<dlv_city><![CDATA["+ city +"]]></dlv_city>");
				xmlBuff.append("<dlv_pin><![CDATA["+ pin +"]]></dlv_pin>");
				xmlBuff.append("<dlv_add1><![CDATA["+ add1 +"]]></dlv_add1>");
				xmlBuff.append("<dlv_add2><![CDATA["+ add2 +"]]></dlv_add2>");
				xmlBuff.append("<dlv_add3><![CDATA["+ add3 +"]]></dlv_add3>");
				xmlBuff.append("<tel1__dlv><![CDATA["+ tele1 +"]]></tel1__dlv>");
				xmlBuff.append("<tel2__dlv><![CDATA["+ tele2 +"]]></tel2__dlv>");
				xmlBuff.append("<tel3__dlv><![CDATA["+ tele3 +"]]></tel3__dlv>");
				xmlBuff.append("<state_code__dlv><![CDATA["+ stateCode +"]]></state_code__dlv>");
				xmlBuff.append("<count_code__dlv><![CDATA["+ countCode +"]]></count_code__dlv>");
				xmlBuff.append("<tran_id__porcp><![CDATA["+ tranId +"]]></tran_id__porcp>");
				xmlBuff.append("<status_remarks><![CDATA["+ " generate from receipt  "+tranId +"]]></status_remarks>");
				xmlBuff.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA["+ termId +"]]></chg_term>");
				xmlBuff.append("<chg_date><![CDATA["+ currDate +"]]></chg_date>");
				xmlBuff.append("<purc_order><![CDATA["+ purcOrder +"]]></purc_order>");
				xmlBuff.append("<tran_code><![CDATA["+ tranCode.trim() +"]]></tran_code>");
				xmlBuff.append("<price_list><![CDATA["+ pricelist.trim() +"]]></price_list>");
				if(pricelistClg != null && pricelistClg.trim().length() > 0)
				{
					xmlBuff.append("<price_list__clg><![CDATA["+ pricelistClg.trim() +"]]></price_list__clg>");
				}
				else
				{
					xmlBuff.append("<price_list__clg><![CDATA["+ pricelist.trim() +"]]></price_list__clg>");
				}
				
				xmlBuff.append("<sales_pers><![CDATA["+ salespers.trim() +"]]></sales_pers>");
				xmlBuff.append("<sales_pers__1><![CDATA["+ salespers1.trim() +"]]></sales_pers__1>");
				xmlBuff.append("<sales_pers__2><![CDATA["+ salespers1.trim() +"]]></sales_pers__2>");
				xmlBuff.append("<frt_term><![CDATA["+ frtTerm.trim() +"]]></frt_term>");
				xmlBuff.append("<dlv_term><![CDATA["+ deliveryTerm +"]]></dlv_term>");
				xmlBuff.append("<dlv_to><![CDATA["+ dlvTo +"]]></dlv_to>");
				xmlBuff.append("<comm_perc><![CDATA["+ commperc +"]]></comm_perc>");
				xmlBuff.append("<comm_perc__on><![CDATA["+ commpercon +"]]></comm_perc__on>");
				xmlBuff.append("<comm_perc_1><![CDATA["+ commPerc1 +"]]></comm_perc_1>");
				xmlBuff.append("<comm_perc_on_1><![CDATA["+ commPercOn1 +"]]></comm_perc_on_1>");
				xmlBuff.append("<comm_perc_2><![CDATA["+ commPerc2 +"]]></comm_perc_2>");
				xmlBuff.append("<comm_perc_on_2><![CDATA["+ commPercOn2 +"]]></comm_perc_on_2>");
				
				//added by priyanka on 1/11/14
				xmlBuff.append("<remarks2><![CDATA["+ remarks2 +"]]></remarks2>");
				xmlBuff.append("<remarks3><![CDATA["+ remarks3 +"]]></remarks3>");
				xmlBuff.append("<stan_code__init><![CDATA["+ stanCodeInit +"]]></stan_code__init>");
                //end by priyanka
				
				
				
				xmlBuff.append("</Detail1>");
				
				sql = "select line_no,item_code,quantity,rate,unit,conv__qty_stduom,unit__rate,tax_class,tax_chap,tax_env,pack_code,rate__clg,quantity__stduom,unit__std," +
						"no_art,conv__rtuom_stduom,rate__stduom,NET_AMT,discount,PACK_INSTR,PALLET_NO from porcpdet where tran_id = ? order by line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
			    while( rs.next() )
			    {
					lineno =  rs.getString("line_no");	
					unitStd = rs.getString("unit__std") == null ? " " : rs.getString("unit__std");	
					itemCode = rs.getString("item_code") == null ? " " : rs.getString("item_code");	
					quantity = rs.getString("quantity") == null ? "0" : rs.getString("quantity")  ;
					rate = rs.getString("rate")== null ? " " : rs.getString("rate");
					unit = rs.getString("unit") == null ? " " : rs.getString("unit");
					conQtyStd = rs.getString("conv__qty_stduom") == null ? " " : rs.getString("conv__qty_stduom");
					unitRate = rs.getString("unit__rate") == null ? " " : rs.getString("unit__rate");
					taxClass = rs.getString("tax_class") == null ? " " : rs.getString("tax_class");
					taxChap = rs.getString("tax_chap") == null ? " " : rs.getString("tax_chap");
					taxEnv = rs.getString("tax_env") == null ? " " : rs.getString("tax_env");
					packCode = rs.getString("pack_code") == null ? " " : rs.getString("pack_code");
					packInstr = rs.getString("PACK_INSTR") == null ? " " : rs.getString("PACK_INSTR");
					rateClg = rs.getDouble("rate__clg");
					netAmt =  rs.getString("NET_AMT");
					noArt =  rs.getString("no_art");
					discount =  rs.getDouble("discount");
					rateStduom =  rs.getString("rate__stduom");	
					convRtuomStduom =  rs.getString("conv__rtuom_stduom");	
					quntyStduom = rs.getString("quantity__stduom") == null ? "0" : rs.getString("quantity__stduom");
					palletNo = rs.getString("PALLET_NO") == null ? " " : rs.getString("PALLET_NO");
					
					sql = " select min_shelf_life ,max_shelf_life "
							+" from sordertype where order_type = ? ";

					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1,orderType);
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						minShelf = rs2.getDouble("min_shelf_life");
						maxShelf = rs2.getDouble("max_shelf_life");
					}	
					pstmt2.close();
					pstmt2 = null;					
					rs2.close();
					rs2 = null;
					if(minShelf ==0 )
					{
						sql="select min_shelf_life "
								+" from      customeritem "
								+" where  cust_code = ? "
								+" and    item_code = ? ";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1,custCode);
						pstmt2.setString(2,itemCode);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							minShelf = rs2.getDouble("min_shelf_life");
						}	
						pstmt2.close();
						pstmt2 = null;					
						rs2.close();
						rs2 = null;
					}
					if(minShelf ==0 )
					{
						sql="select case when min_shelf_perc is null then 0 else " 
								+" min_shelf_perc end "
								+" from      customer_series "
								+" where  cust_code = ? "
								+" and      item_ser = ? ";
						pstmt2 = conn.prepareStatement(sql);
						//pstmt2.setString(1,custCodehd.trim());Commented by Manoj dtd 05/07/2013 to get min shelf life for cust_code__dlv
						pstmt2.setString(1,custCode);
						pstmt2.setString(2,itemCode);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							minShelfPerc = rs2.getDouble(1);
						}	
						pstmt2.close();
						pstmt2 = null;					
						rs2.close();
						rs2 = null;
					}
					if(minShelfPerc !=0 )
					{
						sql="select (case when shelf_life is null then 0 else " 
								+"	shelf_life end ) as shelf_life "
								+" from      item "
								+ " where  item_code = ? ";

						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1,itemCode);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							shelife = rs2.getDouble("shelf_life");
						}	
						pstmt2.close();
						pstmt2 = null;					
						rs2.close();
						rs2 = null;
						if(shelife >0 )
						{
							minShelf = Math.round(((minShelfPerc/100)*shelife));

						}
					}
					if(minShelf ==0)
					{
						sql=" select min_shelf_life from customer " 
								+ " where cust_code = ? ";

						pstmt2 = conn.prepareStatement(sql);
						//pstmt2.setString(1,custCodehd.trim());Commented by Manoj dtd 05/07/2013 to get min shelf life for cust_code__dlv
						pstmt2.setString(1,custCode.trim());
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							minShelf = rs2.getDouble("min_shelf_life");
						}	
						pstmt2.close();
						pstmt2 = null;					
						rs2.close();
						rs2 = null;
					}
					if(minShelf ==0 )
					{
						sql=" select min_shelf_life from item " 
								+ " where item_code = ? ";

						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1,itemCode);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							minShelf = rs2.getDouble("min_shelf_life");
						}	
						pstmt2.close();
						pstmt2 = null;					
						rs2.close();
						rs2 = null;

					}

					
					sql = " select  comm_perc__1 , comm_perc__on_1  	from   customer_series 	where  cust_code = ? and  item_ser  = ?  ";
					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, custCode);
					pstmt2.setString(2, itemSer);
					rs2 = pstmt2.executeQuery();
					if ( rs2.next() )
					{
						commPerc1 = rs2.getDouble("comm_perc__1");
						commPercOn1 = checkNull(rs2.getString("comm_perc__on_1"));

					}
					pstmt2.close();
					pstmt2 = null;
					rs2.close();
					rs2 = null;
					if(commPerc1 == 0)
					{
						sql = " select comm_perc 	from   sales_pers  where  sales_pers = ?  ";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, salespers1);
						rs2 = pstmt2.executeQuery();
						if ( rs2.next() )
						{
							commPerc1 = rs2.getDouble("comm_perc");
						}
						pstmt2.close();
						pstmt2 = null;
						rs2.close();
						rs2 = null;
					}
					if(commPercOn1.trim().length() == 0)
					{
						sql = " select comm_perc__on 	from   sales_pers  where  sales_pers = ?  ";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, salespers1);
						rs2 = pstmt2.executeQuery();
						if ( rs2.next() )
						{
							commPercOn1 = checkNull(rs2.getString("comm_perc__on"));
						}
						pstmt2.close();
						pstmt2 = null;
						rs2.close();
						rs2 = null;
					}
					System.out.println("MinShelf["+minShelf+"] maxShelf["+maxShelf+"]");
					
					xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"sorder\" objContext=\"2\">"); 
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<sale_order/>");
					xmlBuff.append("<remarks><![CDATA["+ "generated from receipt "+tranId+"]]></remarks>");
					
					//xmlBuff.append("<rate><![CDATA["+ exchRate1 +"]]></rate>");
					
					    lineno=lineno.trim();
					    lineno="   "+lineno;
						System.out.println("---"+lineno+"---");
						lineno = lineno.substring(lineno.length() - 3);
                    System.out.println("lineno @@@@ "+lineno);
				    xmlBuff.append("<line_no><![CDATA["+lineno+"]]></line_no>");
					xmlBuff.append("<item_code__ord><![CDATA["+ itemCode.trim() +"]]></item_code__ord>");
					xmlBuff.append("<item_code><![CDATA["+ itemCode.trim() +"]]></item_code>");
					//xmlBuff.append("<rate><![CDATA["+rate +"]]></rate>");
					xmlBuff.append("<unit><![CDATA["+ unit+"]]></unit>");
					xmlBuff.append("<conv__qty_stduom><![CDATA["+ conQtyStd +"]]></conv__qty_stduom>");
					xmlBuff.append("<unit__rate><![CDATA["+ unitRate +"]]></unit__rate>");
					xmlBuff.append("<tax_class><![CDATA["+taxClass.trim() +"]]></tax_class>");
					xmlBuff.append("<tax_chap><![CDATA["+taxChap.trim() +"]]></tax_chap>");
					xmlBuff.append("<tax_env><![CDATA["+ taxEnv.trim() +"]]></tax_env>");
					xmlBuff.append("<pack_code><![CDATA["+packCode+"]]></pack_code>");
					xmlBuff.append("<pack_instr><![CDATA["+packInstr+"]]></pack_instr>");
					
					xmlBuff.append("<quantity><![CDATA["+ quantity.trim() +"]]></quantity>");
					//added by mahendra on 08-MAY-2014
					xmlBuff.append("<quantity__fc><![CDATA["+ quantity.trim() +"]]></quantity__fc>");
					
					xmlBuff.append("<unit__std><![CDATA["+ unitStd +"]]></unit__std>");
					xmlBuff.append("<net_amt><![CDATA["+ netAmt +"]]></net_amt>");
					xmlBuff.append("<item_ser><![CDATA["+ itemSer.trim()   +"]]></item_ser>");
					xmlBuff.append("<discount><![CDATA["+ discount  +"]]></discount>");

					/*if(rateClg != 0)
					{
						xmlBuff.append("<rate__clg><![CDATA["+ rateClg +"]]></rate__clg>");	
					}else
					{
						xmlBuff.append("<rate__clg><![CDATA["+ rate +"]]></rate__clg>"); // SHOULD BE GREATER THAN ZERO
					}*/
					xmlBuff.append("<item_flg><![CDATA["+ "I" +"]]></item_flg>"); // CAN NOT BE NULL
					xmlBuff.append("<quantity__stduom><![CDATA["+ quntyStduom+"]]></quantity__stduom>");
					xmlBuff.append("<CHG_USER><![CDATA["+ userId +"]]></CHG_USER>");
					xmlBuff.append("<CHG_TERM><![CDATA["+ termId +"]]></CHG_TERM>");
					xmlBuff.append("<chg_date><![CDATA["+ currDate +"]]></chg_date>");
					xmlBuff.append("<no_art><![CDATA["+ noArt +"]]></no_art>");
					//xmlBuff.append("<rate__stduom><![CDATA["+ rateStduom +"]]></rate__stduom>");
					xmlBuff.append("<conv__rtuom_stduom><![CDATA["+ convRtuomStduom +"]]></conv__rtuom_stduom>");
					xmlBuff.append("<min_shelf_life><![CDATA["+ minShelf +"]]></min_shelf_life>");
					xmlBuff.append("<max_shelf_life><![CDATA["+ maxShelf +"]]></max_shelf_life>");
					xmlBuff.append("<comm_perc_1><![CDATA["+ commperc +"]]></comm_perc_1>");
					xmlBuff.append("<comm_perc_on_1><![CDATA["+ commpercon +"]]></comm_perc_on_1>");
					xmlBuff.append("<comm_perc_2><![CDATA["+ commPerc1 +"]]></comm_perc_2>");
					xmlBuff.append("<comm_perc_on_2><![CDATA["+ commPercOn1 +"]]></comm_perc_on_2>");
					xmlBuff.append("<comm_perc_3><![CDATA["+ commPerc2 +"]]></comm_perc_3>");
					xmlBuff.append("<comm_perc_on_3><![CDATA["+ commPercOn2 +"]]></comm_perc_on_3>");
					//xmlBuff.append("<no_pallet><![CDATA["+ palletNo +"]]></no_pallet>");
					xmlBuff.append("</Detail2>");
			    }
			    lnno = 0;
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
					System.out.println("@@@@@@3: retString"+retString);
					String[] arrayForTranId = retString.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					String tranIdForIssue = arrayForTranId[1].substring(0,endIndex);
					System.out.println("-tranIdForIssue-"+tranIdForIssue);	
					System.out.println("dlvDescr  :"+dlvDescr);
					
					sql = " update sorder set purc_order = ? ,DLV_TO = ?where sale_order = ? ";
					pstmt  = conn.prepareStatement(sql);
					pstmt.setString(1,purcOrder);
					pstmt.setString(2,dlvDescr);
					pstmt.setString(3,tranIdForIssue);
					int rows = pstmt.executeUpdate();
					System.out.println("updated rows =="+rows);
					conn.commit();
					
					
					/*------------------------changes by mahendra 22-10-14------------------------------*/
					System.out.println("update query after before confirm sale order");
					System.out.println("tranIdForIssue  :"+tranIdForIssue);
					System.out.println("packInstr  :"+packInstr);
					System.out.println("packCode  :"+packCode);
					System.out.println("palletNo  :"+palletNo);
					System.out.println("dlvDescr  :"+dlvDescr);
					
					
					sql = "select PACK_CODE,PACK_INSTR,Line_no from porcpdet where TRAN_ID = ?";
					pstmt2  = conn.prepareStatement(sql);
					pstmt2.setString(1,tranId);
					rs2 = pstmt2.executeQuery();
					while(rs2.next())
					{
						packCode=rs2.getString("pack_code");
						packInstr=rs2.getString("PACK_INSTR");
						lineNo=rs2.getString("Line_no");
						
						System.out.println("packCode :"+packCode +"@packInstr :"+packInstr+" @lineNo :"+lineNo);
						
						sql = " update sorddet set PACK_INSTR = ?,PACK_CODE = ?  where sale_order = ?  and line_no = ?";
						pstmt  = conn.prepareStatement(sql);
						pstmt.setString(1,packInstr);
						pstmt.setString(2,packCode);
						pstmt.setString(3,tranIdForIssue);
						pstmt.setString(4,lineNo);
											
						int row = pstmt.executeUpdate();
						System.out.println("updated rows@@@ =="+row);
						conn.commit();
						
					}
					pstmt2.close();
					pstmt2 = null;
					rs2.close();
					rs2 = null;
					
						/*------------------------------------------------------------------------------------*/
					
					// perameter added on 17/apr/2014 by RITESH 
					String varValue1 = dComm.getDisparams("999999","AUTO_CONF_SORD", conn); 
					if("NULLFOUND".equalsIgnoreCase(varValue1))
						varValue1 = "Y";
					System.out.println("IS SORDER CONFIRM --"+varValue1);
					if("Y".equalsIgnoreCase(varValue1.trim()))
					{
						retString = confirmSaleOrder("sorder",tranIdForIssue,xtraParams,conn);
						retString = retString.toLowerCase();
						System.out.println("retString from generateSaleOrder conf ::"+retString);
						if((retString != null ) && ( retString.indexOf("success") > -1 || retString.indexOf("confirmed") > -1))
						{
							System.out.println( "confirm sale order generate and confirm successfully ...");
							if(tranIdForIssue != null)
							{
								retString = despatchOrder(tranIdForIssue,custCode,tranId,lrDate,invoiceNo,xtraParams,conn);
								System.out.println("ret String from despatchOrder ="+retString);
								return retString;							
							}
						}	
					}
				}
				else
				{
					System.out.println("[" + retString + "]");	
					return retString;
				}		
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
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result [" + retString + "]");
		return retString;
	}
	private String despatchOrder(String saleOrder,String custCode,String tranId,Date lrDate,String invoiceNo, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String userId = "", termId = "";
		String sql = "";
		String siteCode = "";	
		String currCodedlv  = "";
		String lnNoSord = "";
		String currencyCode = "";
		String currCodeFrt="";
		String currCodeIns ="";
		String quntyStduom="";
		String transMode = "";
		String lotSl = "";
		String lotNo = "";
		String explLev = "";
		String stanCode="";
		String exchRate1="";
		String netTotAmt = "";	
		String itemCode = "";
		String quantity ="";
		String rate="";
		String unit="";
		String conQtyStd ="";
		String unitRate="";
		String taxClass="";
		String taxChap="";
		String taxEnv = "";
		String packCode ="",dlvTo="";
		String siteCodeDet ="",convRtuomStduom="";
		String locCode = "",rateStduom="",netAmt="";
		String varValue = "",unitStd = "",noArt = "",packInstr="";
		String stanCodeDlv="",dlvAdd1="",dlvAdd2="",dlvAdd3="",dlvCity="",dlvPin="",countCodeDlv="",tranCode="",stanCodeInit="",status=" ",statusRemarks="",shipmentId="";
		String lrDateVal="",siteCodeMfg="",pack_instr="",lineNo="";
		Date orderDt = null,statusDate=null,expDate=null,mfgDate=null;
		PreparedStatement pstmt = null,pstmt1 =null,pstmt2 =null;
		ResultSet rs = null,rs1 = null,rs2=null;
		double rateClg=0;
		int lnno = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		StringBuffer xmlBuff = null;
		String xmlString = null,retString  = null;
		ITMDBAccessEJB itmDBAccessLocal = null;
		DistCommon dComm = new DistCommon();
		String dimension="";
		double qtyStk=0d,grossWeight=0d,tareWeight=0d,netWeight=0d,palletWt=0d,cAllocQty=0d,cQty=0d;
		double balQty = 0d;
		double grossPer = 0d;
		double netPer = 0d;
		double tarePer = 0d;
		double grossWeight1 = 0d,tareWeight1 = 0d,netWeight1 = 0d;
		DecimalFormat df = new DecimalFormat("#########.###");
		String sqlTemp ="";
		String trackInfoXml="" ,ipAddress="",trackRequest="";
		double netWeightFmPorcp = 0d,tareWeightFmPorcp=0d,grossWeightFmPorcp=0d;
		  
		try   
		{
				System.out.println("@@@@@@@ despatchOrder  function called");
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
				Date today =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
				System.out.println("==today=="+today);
				itmDBAccessLocal = new ITMDBAccessEJB();
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
				termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); System.out.println("--term id--"+termId);
				
				sql = "select item_ser,site_code,curr_code,exch_rate,tran_code, trans_mode,curr_code__frt,curr_code__ins,cust_code__dlv," +
					    " stan_code,order_date,EXCH_RATE__FRT,EXCH_RATE__INS,FOB_VALUE,CONF_DATE,CHG_DATE,STATE_CODE__DLV,UDF__STR1,UDF__STR2,DLV_ADD1,DLV_ADD2,DLV_ADD3," +
						" DLV_TO,DLV_CITY,DLV_PIN,COUNT_CODE__DLV,TRAN_CODE,STAN_CODE,STAN_CODE__INIT,PARENT__TRAN_ID,REV__TRAN,STATUS_REMARKS,SPEC_REASON,DIST_ROUTE,STATUS,STATUS_DATE"+
						" from sorder where sale_order = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					siteCode = rs.getString("site_code") == null ? " " : rs.getString("site_code")  ;
					currencyCode = rs.getString("curr_code") == null ? " " : rs.getString("curr_code");
					exchRate1 = rs.getString("exch_rate") == null ? " " : rs.getString("exch_rate");
					transMode = rs.getString("trans_mode") == null ? "R " : rs.getString("trans_mode");
					currCodeFrt = rs.getString("curr_code__frt") == null ? " " : rs.getString("curr_code__frt");
					currCodeIns = rs.getString("curr_code__ins") == null ? " " : rs.getString("curr_code__ins");
					currCodedlv = rs.getString("cust_code__dlv") == null ? " " : rs.getString("cust_code__dlv");
					stanCode = rs.getString("stan_code") == null ? "" : rs.getString("stan_code");
					orderDt = rs.getDate("order_date");
					stanCodeDlv = rs.getString("STATE_CODE__DLV") == null ? " " : rs.getString("STATE_CODE__DLV");
					/*----------------------changes done by mahendra on dated 13-oct-14-------------*/
					dlvAdd1 = rs.getString("DLV_ADD1") == null ? " " : rs.getString("DLV_ADD1");
					dlvAdd2 = rs.getString("DLV_ADD2") == null ? " " : rs.getString("DLV_ADD2");
					dlvAdd3 = rs.getString("DLV_ADD3") == null ? " " : rs.getString("DLV_ADD3");
					dlvTo = rs.getString("DLV_TO") == null ? " " : rs.getString("DLV_TO");
					/*------------------------------------------------------------------------------*/
					
					dlvCity = rs.getString("DLV_CITY") == null ? " " : rs.getString("DLV_CITY");
					dlvPin = rs.getString("DLV_PIN") == null ? " " : rs.getString("DLV_PIN");
					countCodeDlv = rs.getString("COUNT_CODE__DLV") == null ? " " : rs.getString("COUNT_CODE__DLV");
					tranCode = rs.getString("TRAN_CODE") == null ? " " : rs.getString("TRAN_CODE");
					stanCodeInit = rs.getString("STAN_CODE__INIT") == null ? "" : rs.getString("STAN_CODE__INIT");
					//status = rs.getString("STATUS") == null ? " " : rs.getString("STATUS");
					statusRemarks = rs.getString("STATUS_REMARKS") == null ? " " : rs.getString("STATUS_REMARKS");
					statusDate = rs.getDate("STATUS_DATE");
					
				
				}
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				sql ="select exp_lev  from sorditem where sale_order = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					explLev = rs.getString("exp_lev")==null ? " ":rs.getString("exp_lev");
				}
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				
				
				/*------------shipment id added by mahendra on 17/07/2014------*/
				//(for tnt_track_info shipment id required)
				sql ="select shipment_id from porcp where TRAN_ID=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					shipmentId = rs.getString("shipment_id")==null ? " ":rs.getString("shipment_id");
				}
				rs.close();
				rs =null;
				pstmt.close();
				pstmt = null;
				System.out.println("shipment id is :::::"+shipmentId);
					
				/*------------shipment id added by mahendra on 17/07/2014------*/
				//lrDate added by mahendra on 05-05-2014
				System.out.println("lrDate date is :"+lrDate);
				lrDateVal=lrDate==null?"":sdf.format(lrDate);
				System.out.println("lrDateVal date is :"+lrDateVal);
				
				xmlBuff = new StringBuffer();
				System.out.println("--XML CREATION --");
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("despatch").append("]]></objName>");  
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
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"despatch\" objContext=\"1\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<desp_id/>");
				xmlBuff.append("<desp_date><![CDATA["+ sdf.format(orderDt).toString() +"]]></desp_date>");
				xmlBuff.append(	"<sord_date><![CDATA["+ sdf.format(orderDt).toString() +"]]></sord_date>");
				xmlBuff.append("<sord_no><![CDATA["+ saleOrder.trim() +"]]></sord_no>");
				xmlBuff.append("<cust_code><![CDATA["+ custCode.trim() +"]]></cust_code>");
				xmlBuff.append("<cust_code__dlv><![CDATA["+ custCode.trim() +"]]></cust_code__dlv>");
				xmlBuff.append("<stan_code><![CDATA["+ stanCode.trim() +"]]></stan_code>");
				xmlBuff.append("<trans_mode><![CDATA["+ transMode.trim() +"]]></trans_mode>");
				//lrDate added by mahendra dated 05-05-2014
				xmlBuff.append("<lr_date><![CDATA["+ lrDateVal.toString() +"]]></lr_date>");
				xmlBuff.append("<shipment_id><![CDATA["+ shipmentId.trim() +"]]></shipment_id>");//added by mahendra 17.07.2014(for tnt_track_info shipment id required) 
				xmlBuff.append("<curr_code__frt><![CDATA["+ currCodeFrt.trim() +"]]></curr_code__frt>");
				xmlBuff.append("<curr_code__ins><![CDATA["+ currCodeIns.trim() +"]]></curr_code__ins>");
				xmlBuff.append("<curr_code><![CDATA["+ currencyCode.trim() +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+  exchRate1 +"]]></exch_rate>");// CAN NOT BE NULL
				xmlBuff.append("<exch_rate__frt><![CDATA["+  exchRate1 +"]]></exch_rate__frt>");	
				xmlBuff.append("<exch_rate__ins><![CDATA["+ exchRate1 +"]]></exch_rate__ins>");
				xmlBuff.append("<cust_code__dlv><![CDATA["+ currCodedlv +"]]></cust_code__dlv>");
				xmlBuff.append("<site_code><![CDATA["+ siteCode.trim()+"]]></site_code>");
				xmlBuff.append("<remarks><![CDATA["+ " generate from sale order  "+ saleOrder +"]]></remarks>");				
				xmlBuff.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA["+ termId +"]]></chg_term>");
				xmlBuff.append("<chg_date><![CDATA["+ today +"]]></chg_date>");
				xmlBuff.append("<state_code__dlv><![CDATA["+ stanCodeDlv.trim()+"]]></state_code__dlv>");
				/*--------------------changed by mahendra on dated 13-oct-14-----------------------------*/
				xmlBuff.append("<dlv_add1><![CDATA["+ dlvAdd1 +"]]></dlv_add1>");
				xmlBuff.append("<dlv_add2><![CDATA["+ dlvAdd3 +"]]></dlv_add2>");
				xmlBuff.append("<dlv_add3><![CDATA["+ dlvAdd2 +"]]></dlv_add3>");
				/*----------------------------------------------------------------------------*/
				xmlBuff.append("<dlv_city><![CDATA["+ dlvCity +"]]></dlv_city>");				
				xmlBuff.append("<dlv_pin><![CDATA["+ dlvPin +"]]></dlv_pin>");
				xmlBuff.append("<count_code__dlv><![CDATA["+ countCodeDlv +"]]></count_code__dlv>");
				xmlBuff.append("<tran_code><![CDATA["+ tranCode +"]]></tran_code>");
				xmlBuff.append("<stan_code__init><![CDATA["+ stanCodeInit.trim() +"]]></stan_code__init>");
				xmlBuff.append("<status_remarks><![CDATA["+ statusRemarks +"]]></status_remarks>");
				//xmlBuff.append("<status><![CDATA["+ status +"]]></status>");
				xmlBuff.append("<status><![CDATA["+status+"]]></status>");//status set blank on dated 17-10-2014
				xmlBuff.append("</Detail1>");
				
				sql = "select line_no,item_code,quantity,rate,unit,conv__qty_stduom,unit__rate,tax_class,tax_chap,tax_env,pack_code,rate__clg,quantity__stduom," +
						"net_tot_amt,net_amt,site_code,unit__std,no_art,rate__stduom,conv__rtuom_stduom,PACK_INSTR,NO_PALLET from sorddet where sale_order = ? order by line_no";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				rs = pstmt.executeQuery();
			   while( rs.next() )
			   {
					lnNoSord = rs.getString("line_no");	
					itemCode = rs.getString("item_code") == null ? " " : rs.getString("item_code");	
					quantity = rs.getString("quantity") == null ? "0" : rs.getString("quantity");
					rate = rs.getString("rate")== null ? " " : rs.getString("rate");
					unit = rs.getString("unit") == null ? " " : rs.getString("unit");
					conQtyStd = rs.getString("conv__qty_stduom") == null ? " " : rs.getString("conv__qty_stduom");
					unitRate = rs.getString("unit__rate") == null ? " " : rs.getString("unit__rate");
					taxClass = rs.getString("tax_class") == null ? " " : rs.getString("tax_class");
					taxChap = rs.getString("tax_chap") == null ? " " : rs.getString("tax_chap");
					taxEnv = rs.getString("tax_env") == null ? " " : rs.getString("tax_env");
					packCode = rs.getString("pack_code") == null ? " " : rs.getString("pack_code");
					packInstr = rs.getString("PACK_INSTR") == null ? " " : rs.getString("PACK_INSTR");
					rateClg = rs.getDouble("rate__clg");
					quntyStduom = rs.getString("quantity__stduom") == null ? "0" : rs.getString("quantity__stduom");
					netTotAmt = rs.getString("net_tot_amt") == null ? " " : rs.getString("net_tot_amt");
					siteCodeDet = rs.getString("site_code") == null ? " " : rs.getString("site_code");	
					unitStd = rs.getString("unit__std") == null ? " " : rs.getString("unit__std");	
					noArt = rs.getString("no_art");	
					rateStduom = rs.getString("rate__stduom");	
					convRtuomStduom = rs.getString("conv__rtuom_stduom");	
					netAmt = rs.getString("net_amt");	
					//palletWt = rs.getInt("NO_PALLET");
					//palletWt = rs.getString("NO_PALLET") == null ? " " : rs.getString("NO_PALLET");
					

					sql =" SELECT LOT_SL,LOT_NO,LOC_CODE,GROSS_WEIGHT,TARE_WEIGHT,NET_WEIGHT,DIMENSION,PALLET_WT,pack_code  FROM PORCPDET WHERE TRAN_ID = ? AND LINE_NO = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,tranId);
					pstmt1.setString(2,lnNoSord);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						lotSl = rs1.getString("LOT_SL")==null ? " ":rs1.getString("LOT_SL");
						lotNo = rs1.getString("LOT_NO")==null ? " ":rs1.getString("LOT_NO");
						locCode = rs1.getString("LOC_CODE")==null ? " ":rs1.getString("LOC_CODE");
						dimension = rs1.getString("DIMENSION")==null ? " ":rs1.getString("DIMENSION");
						grossWeightFmPorcp		= rs1.getDouble("GROSS_WEIGHT");
						tareWeightFmPorcp		= rs1.getDouble("TARE_WEIGHT");
						netWeightFmPorcp		= rs1.getDouble("NET_WEIGHT");
						//palletWt = rs1.getInt("PALLET_WT");
						palletWt = rs1.getDouble("PALLET_WT");
						packCode = rs1.getString("pack_code") == null ? " " : rs1.getString("pack_code");
	
					} 
					rs1.close();
					rs1 =null;
					pstmt1.close();
					pstmt1 = null;
				    System.out.println("pallet wt from receipt :"+palletWt);
				    System.out.println("packCode from receipt :"+packCode);
					// ADDED BY RITESH ON 26/05/13 START
					sqlTemp ="SELECT A.EXP_DATE, " 
							+"A.QUANTITY, "
							+"A.SITE_CODE__MFG, "
							+"A.MFG_DATE, "
							+"A.PACK_CODE, " 
							+"A.GROSS_WEIGHT, "
							+"A.TARE_WEIGHT, "
							+"A.NET_WEIGHT, " 
							+"A.DIMENSION, "
							+"A.SUPP_CODE__MFG, " 
							+"A.QTY_PER_ART, "
							+"A.GROSS_WT_PER_ART, " 
							+"A.TARE_WT_PER_ART ,"  
							+"A.PALLET_WT,  "
							+"A.ALLOC_QTY  "
							+"FROM STOCK A " 
							+"WHERE A.ITEM_CODE = '"+itemCode+"' "  
							+"AND A.SITE_CODE = '"+siteCodeDet+"' "  
							+"AND A.LOC_CODE = '"+locCode+"' "  
							+"AND A.LOT_NO = '"+lotNo+"' "   
							+"AND A.LOT_SL = '"+lotSl+"' ";   
					System.out.println("sqlGetWeight :"+sqlTemp);
					pstmt1 = conn.prepareStatement(sqlTemp);
					rs1 = pstmt1.executeQuery(sqlTemp);
					if (rs1.next())
					{
						qtyStk			= rs1.getDouble("QUANTITY");
						grossWeight		= rs1.getDouble("GROSS_WEIGHT");
						tareWeight		= rs1.getDouble("TARE_WEIGHT");
						netWeight		= rs1.getDouble("NET_WEIGHT");
						//if(dimension == null)
						//dimension		= rs1.getString("DIMENSION");
						//palletWt		= rs1.getDouble("PALLET_WT");
						cAllocQty		= rs1.getDouble("alloc_qty");
						expDate         = rs1.getDate("EXP_DATE");
						siteCodeMfg     = rs1.getString("SITE_CODE__MFG"); 
						mfgDate         = rs1.getDate("MFG_DATE");
					}
					rs1.close();
					rs1 =null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println(" qtyStk @@@1 ::["+qtyStk+"]");
					System.out.println(" expDate ::["+expDate+"]");
					System.out.println(" siteCodeMfg::["+siteCodeMfg+"]");
					System.out.println(" mfgDate::["+mfgDate+"]");
					System.out.println(" dimension::["+dimension+"]");
					System.out.println(" palletWt::["+palletWt+"]");
					
					dimension = dimension == null ? " " : dimension;
					siteCodeMfg = siteCodeMfg == null ? " " : siteCodeMfg;
					
					if(quantity != null && quantity.trim().length() > 0)
					{
						cQty = Double.parseDouble(quantity);
					}
					balQty = cQty - cAllocQty;
					
					if (qtyStk > balQty)
					{
						qtyStk = balQty;
					}
					System.out.println(" qtyStk @@@2 ::["+qtyStk+"]");
					System.out.println(" balQty @@@ ::["+qtyStk+"]");
					if (qtyStk > 0)
					{
						grossPer = (grossWeight / qtyStk) ;
						grossPer = df.parse(df.format(grossPer)).doubleValue();
						System.out.println("grossPer :"+grossPer);
						netPer 	=  (netWeight / qtyStk);
						netPer = df.parse(df.format(netPer)).doubleValue();
						System.out.println("netPer 	:"+netPer);
						tarePer	=  (tareWeight / qtyStk);
						tarePer = df.parse(df.format(tarePer)).doubleValue();
						System.out.println("tarePer	:"+tarePer);									
						grossWeight1 = (balQty * grossPer);
						System.out.println("grossWeight1 :"+grossWeight1);
						netWeight1 = (balQty * netPer);
						System.out.println("netWeight1 :"+netWeight1);
						tareWeight1 = (balQty * tarePer);
						System.out.println("tareWeight1 :"+tareWeight1);	
						
						grossWeight1 = df.parse(df.format(grossWeight1)).doubleValue(); 	
						netWeight1	= df.parse(df.format(netWeight1)).doubleValue(); 	
						tareWeight1	= df.parse(df.format(tareWeight1)).doubleValue(); 
						System.out.println("stk > 0 :: grossWeight1:["+grossWeight1+"]netWeight1:["+netWeight1+"]tareWeight1:["+tareWeight1+"]");
					}
					// ADDED BY RITESH ON 26/05/13 END
					
			    	lnno ++;
			    	System.out.println("--LINE NO--"+lnno);
					xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"despatch\" objContext=\"2\">"); 
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<desp_id/>");
					xmlBuff.append("<sord_no><![CDATA["+ saleOrder.trim() +"]]></sord_no>");
					
					
					lineNo=String.valueOf(lnno);
					lineNo=lineNo.trim();
				    lineNo="   "+lineNo;
					System.out.println("---"+lineNo+"---");
					lineNo = lineNo.substring(lineNo.length() - 3);
					System.out.println("--@@@@@-"+lineNo+"---");
					xmlBuff.append("<line_no><![CDATA["+lineNo+"]]></line_no>");					
					xmlBuff.append("<line_no__sord><![CDATA["+ lnNoSord +"]]></line_no__sord>");					
					xmlBuff.append("<lot_no><![CDATA["+ lotNo +"]]></lot_no>");					
					xmlBuff.append("<lot_sl><![CDATA["+ lotSl +"]]></lot_sl>");					
					xmlBuff.append("<loc_code><![CDATA["+ locCode +"]]></loc_code>");					
					xmlBuff.append("<exp_lev><![CDATA["+ explLev +"]]></exp_lev>");					
					xmlBuff.append("<remarks><![CDATA["+ "generated from sorder "+"]]></remarks>");					
					xmlBuff.append("<rate><![CDATA["+rate +"]]></rate>");					
					xmlBuff.append("<item_code__ord><![CDATA["+ itemCode.trim() +"]]></item_code__ord>");					
					xmlBuff.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");					
					xmlBuff.append("<quantity><![CDATA["+ quantity.trim() +"]]></quantity>");	
					xmlBuff.append("<quantity__ord><![CDATA["+ quantity.trim() +"]]></quantity__ord>");
					xmlBuff.append("<unit><![CDATA["+ unit+"]]></unit>");					
					xmlBuff.append("<conv__qty_stduom><![CDATA["+ conQtyStd +"]]></conv__qty_stduom>");					
					xmlBuff.append("<unit__rate><![CDATA["+ unitRate +"]]></unit__rate>");				
					xmlBuff.append("<tax_class><![CDATA["+taxClass.trim() +"]]></tax_class>");				
					xmlBuff.append("<tax_chap><![CDATA["+taxChap +"]]></tax_chap>");				
					xmlBuff.append("<tax_env><![CDATA["+ taxEnv+"]]></tax_env>");					
					xmlBuff.append("<rate__clg><![CDATA["+ rateClg +"]]></rate__clg>");					
					xmlBuff.append("<tot_net_amt><![CDATA["+ netTotAmt.trim()+"]]></tot_net_amt>");					
					xmlBuff.append("<site_code><![CDATA["+ siteCodeDet.trim()+"]]></site_code>");					
					xmlBuff.append("<quantity__stduom><![CDATA["+ quntyStduom.trim()+"]]></quantity__stduom>");					
					xmlBuff.append("<pack_code><![CDATA["+ packCode.trim()+"]]></pack_code>");
					xmlBuff.append("<unit__std><![CDATA["+ unitStd+"]]></unit__std>");
					xmlBuff.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
					xmlBuff.append("<chg_term><![CDATA["+ termId +"]]></chg_term>");
					xmlBuff.append("<chg_date><![CDATA["+ today +"]]></chg_date>");
					xmlBuff.append("<no_art><![CDATA["+ noArt +"]]></no_art>");
					xmlBuff.append("<rate__stduom><![CDATA["+ rateStduom +"]]></rate__stduom>");
					xmlBuff.append("<conv__rtuom_stduom><![CDATA["+ convRtuomStduom +"]]></conv__rtuom_stduom>");
					xmlBuff.append("<tot_net_amt><![CDATA["+ netAmt +"]]></tot_net_amt>");
					xmlBuff.append("<quantity__inv><![CDATA["+ quantity +"]]></quantity__inv>");
					xmlBuff.append("<quantity__real><![CDATA["+ quantity +"]]></quantity__real>");
					// ADDED BY RITESH ON 26/05/13 START
					xmlBuff.append("<gross_weight>").append("<![CDATA[").append(grossWeightFmPorcp).append("]]>").append("</gross_weight>");
					xmlBuff.append("<nett_weight>").append("<![CDATA[").append(netWeightFmPorcp).append("]]>").append("</nett_weight>");
					xmlBuff.append("<tare_weight>").append("<![CDATA[").append(tareWeightFmPorcp).append("]]>").append("</tare_weight>");
					xmlBuff.append("<dimension><![CDATA["+ dimension +"]]></dimension>");
					xmlBuff.append("<pallet_wt><![CDATA["+ palletWt +"]]></pallet_wt>");
					//xmlBuff.append("<pack_code><![CDATA["+ packCode +"]]></pack_code>");
					// ADDED BY RITESHON 26/05/13 END
					xmlBuff.append("<pack_instr><![CDATA["+packInstr.trim()+"]]></pack_instr>");
					xmlBuff.append("<site_code__mfg><![CDATA["+siteCodeMfg.trim()+"]]></site_code__mfg>");
					if(expDate != null)
					{
						xmlBuff.append("<exp_date><![CDATA["+sdf.format(expDate).toString()+"]]></exp_date>");
					}
					else
					{
						xmlBuff.append("<exp_date><![CDATA[]]></exp_date>");
					}
					if(mfgDate != null)
					{
						xmlBuff.append("<mfg_date><![CDATA["+sdf.format(mfgDate).toString()+"]]></mfg_date>");
					}
					else
					{
						xmlBuff.append("<mfg_date><![CDATA[]]></mfg_date>");
					}
					
					xmlBuff.append("</Detail2>");
			    }
			    lnno = 0;
			    rs.close();
			    rs = null;
			    pstmt.close();
			    pstmt = null;
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@2: xmlString  for despatch:"+xmlBuff.toString());
				System.out.println("...............just before savdata() for despatch");
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				System.out.println("==site code =="+siteCode);
				retString = saveData(siteCode,xmlString,userId,conn);
				System.out.println("@@@@@2: retString from despatch:"+retString);
				System.out.println("--retString finished from despatch--");
				
				if (retString.indexOf("Success") > -1)
				{
					System.out.println("@@@@@@3: retString from despatch"+retString);
					conn.commit();
					/*-----------------------------------------------------------------------*/
					System.out.println("update pack code before confirmation !!! ");
					String[] arrayForTranId = retString.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					String tranIdForDesp = arrayForTranId[1].substring(0,endIndex);
					System.out.println("-tranIdForDesp-"+tranIdForDesp);
					System.out.println("packInstr  : "+packInstr);
					System.out.println("packCode  : "+packCode);
					
						
					sql = "select PACK_CODE,PACK_INSTR,Line_no from porcpdet where TRAN_ID = ?";
					pstmt2  = conn.prepareStatement(sql);
					pstmt2.setString(1,tranId);
					rs2 = pstmt2.executeQuery();
					while(rs2.next())
					{
						packCode=rs2.getString("pack_code");
						packInstr=rs2.getString("PACK_INSTR");
						lineNo=rs2.getString("Line_no");
						
						System.out.println("packCode :"+packCode +"@packInstr :"+packInstr+" @lineNo :"+lineNo);
						
						sql = " update despatchdet set PACK_INSTR = ?,PACK_CODE = ?  where desp_id = ?  and line_no = ?";
						pstmt  = conn.prepareStatement(sql);
						pstmt.setString(1,packInstr);
						pstmt.setString(2,packCode);
						pstmt.setString(3,tranIdForDesp);
						pstmt.setString(4,lineNo);
											
						int row = pstmt.executeUpdate();
						System.out.println("updated rows@@@ =="+row);
						conn.commit();
						
					}
					pstmt2.close();
					pstmt2 = null;
					rs2.close();
					rs2 = null;
					
					
					
					/*sql = " update despatchdet set PACK_INSTR = ?,PACK_CODE = ?  where desp_id = ? ";
					pstmt  = conn.prepareStatement(sql);
					pstmt.setString(1,packInstr);
					pstmt.setString(2,packCode);
					pstmt.setString(3,tranIdForDesp);
										
					int row = pstmt.executeUpdate();
					System.out.println("updated rows@@@ =="+row);
					conn.commit();*/
					
									
					/*-----------------------------------------------------------------------*/
								
					varValue = dComm.getDisparams("999999","AUTO_CONF_DESP", conn); 
					System.out.println("varValue --"+varValue);
					if("Y".equalsIgnoreCase(varValue.trim()))
					{
						/*String[] arrayForTranId = retString.split("<TranID>");
						int endIndex = arrayForTranId[1].indexOf("</TranID>");
						String tranIdForDesp = arrayForTranId[1].substring(0,endIndex);
						System.out.println("-tranIdForDesp-"+tranIdForDesp);*/
						
						retString = confirmSaleOrder("despatch",tranIdForDesp,xtraParams,conn);
						System.out.println("retString from conf from despatch ::"+retString);
						
					
						return retString;
					}
				}
				else
				{
					System.out.println("[" + retString + "]");	
					return retString;
				}
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
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result [" + retString + "]");
		return retString;
		
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
	public String confirmSaleOrder(String businessObj, String tranIdFr,String xtraParams, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println("confirmSaleOrder(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");

		try
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,businessObj);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = "+serviceCode+" compName "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,serviceCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
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
			//aobj[3] = new String(forcedFlag);
			//System.out.println("@@@@@@@@@@loginEmpCode:" +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);
			System.out.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>["+retString+"]");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{		
			try{
				
			
				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				/*if( conn != null ){
					conn.close();
					conn = null;
				}*/
			}
			catch(Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try{
					conn.rollback();

				}catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}
		}
		return retString;
	}
	
	
		
	
	public Document parseString(String xmlString) throws RemoteException,ITMException
	{
		Document dom1 = null;
		try
		{
			//Changed by Monif on 2/23/2009 [For special char parsing issue].Start
			//ibase.webitm.utility.GenericUtility genericUtility = new ibase.webitm.utility.GenericUtility();
			xmlString = genericUtility.setXmlDec( xmlString );			
			//Changed by Monif on 2/23/2009 [For special char parsing issue].End
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setIgnoringComments(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			OutputStreamWriter errorWriter =   new OutputStreamWriter(System.err, CommonConstants.ENCODING); //$NON-NLS-1$
			//Changed by Monif on 6/3/2008 [TO support Character encoding].
			//ByteArrayInputStream baos = new ByteArrayInputStream(xmlString.getBytes());
			ByteArrayInputStream baos = new ByteArrayInputStream(xmlString.getBytes( CommonConstants.ENCODING ));
		
			dom1 = db.parse(baos);
		}
		catch(Exception e)
		{
			dom1 = null;
			System.out.println("Exception : [ValidatorEJB][parseString] :==>\n"+e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
			throw new ITMException(e);
		}
		return dom1;
	}
	

	
	
	
	public String trackInfoServlet(String xmlString)throws ITMException, IOException,UnsupportedEncodingException {
		String msg="";
		String urlAddress="";
		String ipAddress = "";
		URL url;
	    HttpURLConnection connection = null; 
	    String urlParameters = "fName=" + URLEncoder.encode("mahendra", "UTF-8") +"&lName=" + URLEncoder.encode("jadhav", "UTF-8");
	    
      
        
		try
		{ 
			System.out.println("Inside trackInfo servlet !!!!");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse("/IBASEHOME/ibase.xml");
			ipAddress = dom.getElementsByTagName("IPADDRESS").item(0).getFirstChild().getNodeValue();
			System.out.println("ipAddress1  :::: "+ipAddress);
		
			 //Create connection
			  System.out.println("call trackInfoServlet======");
			  System.out.println("xmlString  !!"+xmlString);
			  ipAddress=ipAddress.trim();
			 // urlAddress="http://"+ipAddress+"/ibase/TrackInfoServlet";
			  urlAddress=ipAddress+"/ibase/TrackInfoServlet";
			  System.out.println("urlAddress is :::"+urlAddress);
		      url = new URL(urlAddress);
		      connection = (HttpURLConnection)url.openConnection();
		      connection.setRequestMethod("POST");
		      connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		      connection.setUseCaches (false);
		      connection.setDoInput(true);
		      connection.setDoOutput(true);
		  
		      //send request
		      OutputStreamWriter out = new OutputStreamWriter(
              connection.getOutputStream());
		      out.write("xmlData=" + xmlString);
		      out.close();
		    
	
		      
		      
		    //Get Response	
		      InputStream is = connection.getInputStream();
		      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		      String line;
		      StringBuffer response = new StringBuffer(); 
		      while((line = rd.readLine()) != null) 
		      {
		    	System.out.println("line  !!!! "+line);
		        response.append(line);
		        response.append('\r');
		      }
		      rd.close();
		      return response.toString();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			msg="failed :"+e.getMessage();
			return msg;
		}
		finally
		{
			 if(connection != null) 
			 {
			        connection.disconnect(); 
			 }
		}
		
		//return msg;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}