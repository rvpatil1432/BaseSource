package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.E12GenerateEDIEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.TransactionEmailTempltEJB;
import ibase.webitm.ejb.dis.SordItemBean;
import ibase.webitm.ejb.dis.StockBean;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.InvAcct;
import ibase.webitm.ejb.fin.adv.CalculateCommission;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.LinkedHashSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.apache.poi.util.SystemOutLogger;
import org.w3c.dom.Document;
//import ibase.utility.CommunicationComp;
import org.w3c.dom.NodeList;

@Stateless
public class PostOrderProcess extends ProcessEJB implements PostOrderProcessLocal,PostOrderProcessRemote
{
	private static final String Document = null;
	E12GenericUtility e12GenericUtility = new E12GenericUtility();
	FileOutputStream fos1 = null;
	java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
	UtilMethods utilMethod = new UtilMethods();
	Calendar calendar = Calendar.getInstance();
	ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
	FinCommon fnComm=new FinCommon();
	    //Added By PriyankaC on 16Oct2019 [START]
		//	ibase.utility.UserInfoBean userInfo = new UserInfoBean();
		TransactionEmailTempltEJB TransactionEmailTempltEJB = new TransactionEmailTempltEJB();
		//Added By PriyankaC on 16Oct2019 [END]
	E12GenericUtility genericUtility = new E12GenericUtility();
	UtilMethods utilmethod = new UtilMethods();
	SimpleDateFormat sdf =null;
	String startDateStr = null;
	String strToWrite="",strToWriteHead="",logFileInit="",xmlString1="",chgTerm="",chgUser="";
	String err = "";
	boolean postLogYn=false;
	PostOrderActivity postordact=new PostOrderActivity();
	String xtraparam="";
	//Connection conn = null;
	ArrayList<String> salesOrderListAll=new ArrayList<String>();
	ArrayList<String> postSalOder=new ArrayList<String>();
	
	//added By Pavan R on 2K18/Feb/12 to store the posted so.[Start]
	ArrayList<String> sorderpostedList = new ArrayList<String>();
	//added By Pavan R on 2K18/Feb/12[End]

	ArrayList<Log> erroLogSordItme=new ArrayList<Log>();//added By Nandkumar gadkari on 10/05/19
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		String retStr = "";
		//ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				xmlString1=xmlString;
				headerDom = e12GenericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = e12GenericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
				//writeLog(filePtr,"detailDom-->"+detailDom,true);
			}
			//System.out.println("xmlString*********"+xmlString);
			//System.out.println("xmlString2*********"+xmlString2);
			xtraparam=xtraParams;
			//Changed By Nasruddin 04-11-16 Start 
			startTime = System.currentTimeMillis();
			retStr = process(headerDom, detailDom, windowName, xtraParams);
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;
			//System.out.println("Total Time Spend :: " + totalTime + " Milliseconds");

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend [" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
			//Changed By Nasruddin 04-11-16 End
			System.out.println("@@@@@@@@@@@ retStr["+retStr+"]");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		} 
		return retStr;
	}

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		//writeLog(filePtr,"In process.............",true);
		//writeLog(filePtr,"headerDom-->"+headerDom,true);
		//writeLog(filePtr,"detailDom-->"+detailDom,true);
		//writeLog(filePtr,"windowName-->"+windowName,true);
		//writeLog(filePtr,"xtraParams-->"+xtraParams,true);
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String postSal="";
		String saleOrder="", crCheckError = "";
;


		//PostOrder
		SimpleDateFormat sdf = null;
		Timestamp timestamp = null,toDate=null,fromDate=null;
		String currDate="";
		String fromCustCode="",toCustCode="",fromSaleOrder="",toSaleOrder="",lotSl="",toDateStr="",fromDateStr="";
		String refreshDb="",allocStk="",clubPendingOrd="",adjDrcr="",adjCustAdv="",clubOrder="",siteCodeShip="";
		String advAdjMode="",postUptoInvoice="",adjNewProdInv="",loginSiteCode="",errString="",retString="",orderType="",refDate ="";
		String sql="",custCode="";
		//		String[] custCode;
		int cnt = 0,liCtr = 0,custCdIndex = 0,sordIndex=0,count = 0;
		boolean isPostDone=false;
		long startTime2 = 0, endTime2 = 0;

		ArrayList<String> custCodeList = new ArrayList<String>();
		//custCodeList = null;
		ArrayList<String> saleOrderList = new ArrayList<String>();
		//saleOrderList = null;
		try
		{
			//System.out.println("process starts................");
			//conn = connDriver.getConnectDB("DriverITM");
			//conn.setAutoCommit(false);
			conn = getConnection() ;

			sdf = new SimpleDateFormat(e12GenericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());
			currDate = (sdf.format(timestamp).toString()).trim();
			loginSiteCode =(e12GenericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			
		/*	 refDate = checkNull(this.genericUtility.getColumnValue("ref_date", headerDom));*/
			
			orderType=e12GenericUtility.getColumnValue("order_type",headerDom);
			//System.out.println("orderType :"+orderType);
			fromSaleOrder=checkNullAndTrim(e12GenericUtility.getColumnValue("tran_id__fr",headerDom));
			//System.out.println("fromSaleOrder :********"+fromSaleOrder);
			toSaleOrder=checkNullAndTrim(e12GenericUtility.getColumnValue("tran_id__to",headerDom));
			//System.out.println("toSaleOrder :"+toSaleOrder);
			fromCustCode=checkNullAndTrim(e12GenericUtility.getColumnValue("cust_code__fr",headerDom));
			//System.out.println("fromCustCode :***********"+fromCustCode);
			toCustCode=checkNullAndTrim(e12GenericUtility.getColumnValue("cust_code__to",headerDom));
			//System.out.println("toCustCode :"+toCustCode);
			clubPendingOrd=e12GenericUtility.getColumnValue("club_pend_ord",headerDom);
			//System.out.println("clubPendingOrd :"+clubPendingOrd);
			clubOrder=e12GenericUtility.getColumnValue("club_order",headerDom);
			//System.out.println("clubOrder :"+clubOrder);
			siteCodeShip=e12GenericUtility.getColumnValue("site_code",headerDom);
			//System.out.println("siteCodeShip :"+siteCodeShip);
			refreshDb=e12GenericUtility.getColumnValue("refresh_db",headerDom);
			//System.out.println("refreshDb :"+refreshDb);
			fromDateStr=checkNullAndTrim(e12GenericUtility.getColumnValue("desp_date__fr",headerDom));
			//System.out.println("fromDateStr :"+fromDateStr);
			toDateStr=checkNullAndTrim(e12GenericUtility.getColumnValue("desp_date__to",headerDom));
			//System.out.println("toDateStr :"+toDateStr);
			//SETTING OF FROM SO TO 0 IF IT IS NULL
			if( fromSaleOrder == null || fromSaleOrder.trim().length() == 0  ) 
			{	
				fromSaleOrder = "0";
			}
			if( toSaleOrder == null || toSaleOrder.trim().length() == 0  ) 
			{	
				toSaleOrder = "Z";
			}
			//SETTING CUST_CODE FROM TO 0
			if( fromCustCode == null || fromCustCode.trim().length() == 0 )
			{
				fromCustCode = "0";
			}
			if( toCustCode == null || toCustCode.trim().length() == 0 )
			{
				toCustCode = "Z";
			}
			// Added by Sneha on 08/02/2017, for validation [Start]
			/*			System.out.println("----------- fromSaleOrder  ------------"+fromSaleOrder);
			System.out.println("----------- toSaleOrder ------------"+toSaleOrder);
			System.out.println("----------- fromCustCode  ------------"+fromCustCode);
			System.out.println("----------- toCustCode ------------"+toCustCode);
			System.out.println("----------- fromDateStr  ------------"+fromDateStr);
			System.out.println("----------- toDateStr ------------"+toDateStr);
			 */
			if(fromSaleOrder.trim().length() == 0 ||  toSaleOrder.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("", "VMTRNIDNUL", "","", conn);
				return retString;
			}
			else
			{
				//count = fromSaleOrder.compareTo(toSaleOrder);
				count = toSaleOrder.compareTo(fromSaleOrder);
				System.out.println("count inside tran_id__to =========>>"+count);

				if((count < 0))
				{
					retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD1", "","", conn);
					return retString;
				}
			}
			if(!"0".equalsIgnoreCase(fromCustCode) && !"Z".equalsIgnoreCase(toCustCode))
			{
				count = 0;
				count = fromCustCode.compareTo(toCustCode);
				System.out.println("count inside cust_code__to =========>>"+count);
				if((count < 0))
				{
					retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD4", "","", conn);
					return retString;
				}
			}
			if(fromDateStr.length() == 0)
			{
				retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD2", "","", conn);
				return retString;
			}
			else if(toDateStr.length() == 0)
			{
				retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD2", "","", conn);
				return retString;
			}
			else if(sdf.parse(toDateStr).before(sdf.parse(fromDateStr)))
			{
				retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD5", "","", conn);
				return retString;
			}
			else
			{
				Date ld_from_date1 = sdf.parse(fromDateStr);
				Date ld_to_date1 = sdf.parse(toDateStr);
				long diff = ld_from_date1.getTime() - ld_to_date1.getTime();
				System.out.println("diff===========>>"+diff);

				if(diff > 30 && toSaleOrder.equalsIgnoreCase("0") && fromSaleOrder.equalsIgnoreCase("Z"))
				{
					retString = itmDBAccessEJB.getErrorString("", "VTDAYS1", "","", conn);
					return retString;
				}
			}
			// Added by Sneha on 08/02/2017, for validation [End]
			//System.out.println("clubPendingOrd :"+clubPendingOrd);
			//Added by wasim on 21-APR-17 for site code validation if it blank [START]
			if(siteCodeShip == null || siteCodeShip.trim().length() == 0)
			{
				retString = itmDBAccessEJB.getErrorString("", "VTSITCODE", "","", conn);
				return retString;
			}
			else
			{
				sql=" SELECT COUNT(1) FROM SITE WHERE SITE_CODE= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeShip);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					cnt =  rs.getInt(1);
				}
				if(rs != null) 
				{
					rs.close();rs = null;
				}
				if(pstmt != null) 
				{
					pstmt.close();pstmt = null;
				}
				if(cnt == 0)
				{
					retString = itmDBAccessEJB.getErrorString("", "VTSITEXT", "","", conn);
					return retString;
				}
			}
			//Added by wasim on 21-APR-17 for site code validation if it blank [END]
			if(clubPendingOrd != null && clubPendingOrd.equalsIgnoreCase("Y") )
			{
				sql="select count(1) from customer where cust_code= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,fromCustCode);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					cnt =  rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//	System.out.println("cnt :"+cnt);
				if(cnt == 0) 
				{
					//	System.out.println("Invalid customer!,Customer clubbing option is selected a valid to customer must be entered::::LINE NO==221");
					retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD1", "","", conn);
					//	System.out.println("retString if fromCustCode is not present>>> 223: "+retString);
					return retString;
				}

				sql="select count(1) from customer where cust_code= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,toCustCode);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					cnt =  rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cnt == 0) 
				{
					//	System.out.println("Invalid customer!,Customer clubbing option is selected a valid to customer must be entered....LINE NO ==241");
					retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD1", "","", conn);
					//	System.out.println("retString if toCustCode is not present>>> 243: "+retString);
					return retString;
				}
			}

			// From date logic modified as now date format is picked up from ini file
			//fromDate=e12GenericUtility.getColumnValue("desp_date__fr",headerDom);
			if( fromDateStr == null ) 
			{	
				//ls_errcode = "VTPOSTORD2~tInvalid From Date!,Enter Valid From Date"
				retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD2", "","", conn);
				//	System.out.println("retString if fromDate is null>>> : "+retString);
				return retString;
			}
			else
			{ 
				//toDate=e12GenericUtility.getColumnValue("desp_date__to",headerDom);
				if( toDateStr == null ) 
				{	
					//ls_errcode = "VTPOSTORD2~tInvalid To date!,Enter Valid To Date"
					retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD2", "","", conn);
					//	System.out.println("retString if toDate is null>>> : "+retString);
					return retString;
				}
				else
				{
					fromDate= Timestamp.valueOf(e12GenericUtility.getValidDateString(fromDateStr, e12GenericUtility.getApplDateFormat(),e12GenericUtility.getDBDateFormat()) + " 00:00:00.0");
					//	System.out.println("fromDate :"+fromDate);
					toDate= Timestamp.valueOf(e12GenericUtility.getValidDateString(toDateStr, e12GenericUtility.getApplDateFormat(),e12GenericUtility.getDBDateFormat()) + " 00:00:00.0");
					//	System.out.println("toDate :"+toDate);
					if( toDate.before(fromDate)) 
					{  
						//ls_errcode = "VTPOSTORD3~tInvalid To date!,Enter Valid To Date"
						retString = itmDBAccessEJB.getErrorString("", "VTPOSTORD3", "","", conn);
						//	System.out.println("retString if  toDate.before(fromDate)>>> : "+retString);
						return retString;
					}
				}
			}

			//

			if(clubOrder != null && clubOrder.equalsIgnoreCase("Y"))
			{
				/*				sql="select distinct cust_code from sorder " +
						"where sale_order >= ? and sale_order <= ? and cust_code  >= ? and cust_code  <= ? " +
						"and due_date   >= ? and due_date   <= ? and confirmed ='Y' and status ='P' " +
						"and site_code__ship = ? ";   commented by abhijit Gaikwad */
				sql="select distinct cust_code__bil from sorder " +
						"where sale_order >= ? and sale_order <= ? and cust_code  >= ? and cust_code  <= ? " +
						"and due_date   >= ? and due_date   <= ? and confirmed ='Y' and status ='P' " +
						"and site_code__ship = ? ";
				if(orderType != null && orderType.trim().length() > 0)
				{
					sql = sql +" and order_type = ? ";
				}

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, fromSaleOrder);
				pstmt.setString(2, toSaleOrder);
				pstmt.setString(3, fromCustCode);
				pstmt.setString(4, toCustCode);
				//pstmt.setString(5, fromDateStr);
				//pstmt.setString(6, toDateStr);
				pstmt.setTimestamp(5, fromDate);
				pstmt.setTimestamp(6, toDate);
				pstmt.setString(7, siteCodeShip);
				if(orderType != null && orderType.trim().length() > 0)
				{
					pstmt.setString(8, orderType);
				}
				rs = pstmt.executeQuery();
				//				List rowValues = new ArrayList();
				//				ArrayList<String> a = new ArrayList<String>();
				while(rs.next())
				{
					custCodeList.add(rs.getString("cust_code__bil"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//System.out.println("custCodeList>>>>>>> ["+custCodeList+"]");
				//	liCtr = 1;
				if(custCodeList.size() <= 0 )
				{
					//System.out.println("Posting Unsuccessful!! Order(s) either not confirmed or already been posted");
					errString = itmDBAccessEJB.getErrorString("","VTPOST05","","",conn);
					return retString;  // 23-Nov-16 added by manoharan
				}

				for(custCdIndex = 0 ; custCdIndex < custCodeList.size() ;custCdIndex++)
				{
					//Changed By Nasruddin Start 04-11-16
					startTime2 = System.currentTimeMillis();
					System.out.println("");
					retString = postOrder( headerDom, custCodeList.get(custCdIndex),"",xtraParams,conn);
					//System.out.println("cust post ord retString :"+retString);
					endTime2 = System.currentTimeMillis();
					System.out.println("Posting time taken for customer [" + custCodeList.get(custCdIndex) + "] ["+(endTime2-startTime2)/1000+"] seconds");
					//Changed By Nasruddin END 04-11-16
					if(retString== null || retString.trim().length()==0)
					{
						isPostDone=true;

					}
					else
					{
						conn.rollback();
						//	Changes done by apal for stock  allocation29/11/2017 start 
						//	System.out.println("Invalid customer!,Customer clubbing option is selected a valid to customer must be entered....LINE NO ==348");
						if (retString.indexOf("Errors") != -1)
						{
							//  01-feb-2021 manoharan in case credit check failed one order continue with next order
							if (retString.indexOf("VTWBLGCCHK") != -1)
							{
								retString = "";
							}
							else
							{
								return retString;
							}
						}else
						{
							//Changed by Pavan R on 25/JAN/2K18 [Start]
							//errString = itmDBAccessEJB.getErrorString("", "VTPOSTORD1", "","", conn);
							errString = getErrorNew(retString, "VTPOSTORD1", conn);
							//System.out.println("errString>>>>"+errString);
							//Changed by Pavan R on 25/JAN/2K18 [End]
						}
						//Changes done by apal for stock  allocation29/11/2017 ended
						//	System.out.println("retString if toCustCode is not present>>> 350: "+retString);
					}
				}
			}
			else
			{
				sql="select sale_order from sorder " +
						"where sale_order >= ? and sale_order <= ? and cust_code  >= ? and cust_code  <= ? " +
						"and due_date   >= ? and due_date   <= ? and confirmed ='Y' and status ='P' " +
						"and site_code__ship = ? ";

				if(orderType != null && orderType.trim().length() > 0)
				{
					sql = sql +" and order_type = ? order by due_date, sale_order ";
				}
				else
				{
					sql = sql + "order by due_date, sale_order";
				}

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, fromSaleOrder);
				pstmt.setString(2, toSaleOrder);
				pstmt.setString(3, fromCustCode);
				pstmt.setString(4, toCustCode);
				//pstmt.setString(5, fromDateStr);
				//pstmt.setString(6, toDateStr);//change by chandrashekar on jun 02
				pstmt.setTimestamp(5, fromDate);
				pstmt.setTimestamp(6, toDate);
				pstmt.setString(7, siteCodeShip);
				if(orderType != null && orderType.trim().length() > 0)
				{
					pstmt.setString(8, orderType);
				}
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					saleOrderList.add(rs.getString("sale_order"));  // cust_code
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//System.out.println("saleOrderList>>>>>>> ["+saleOrderList+"]");
				liCtr = 1;
				if( saleOrderList.size() <= 0 )
				{
					//System.out.println("Posting Unsuccessful!! Order(s) either not confirmed or already been posted");
					errString = itmDBAccessEJB.getErrorString("","VTPOST05","","",conn);
					//return retString;  // 23-Nov-16 added by manoharan
					return errString; //Changed by Santosh on 19/07/2017 to return errString value instead on retString
				}
				for(sordIndex=0; sordIndex < saleOrderList.size() ;sordIndex++)
				{
					startTime2 = System.currentTimeMillis();
					retString = postOrder(headerDom, "",saleOrderList.get(sordIndex),xtraParams,conn);
					endTime2 = System.currentTimeMillis();
					System.out.println("Posting time taken for SO [" + saleOrderList.get(sordIndex) + "] ["+(endTime2-startTime2)/1000+"] seconds");
					System.out.println("postOrder return:::::::::::[["+retString+"]]......");

					if(retString== null || retString.trim().length()==0)
					{
						if(postSalOder.size()>0)
						{
							isPostDone=true;
						}

					}else
					{
						conn.rollback();
						// Changes done by apal for stock  allocation29/11/2017 start
						if (retString.indexOf("Errors") != -1)
						{
							// 01-feb-2021 manoharan in case credit check failed one order continue with next order
							if (retString.indexOf("VTWBLGCCHK") != -1)
							{
								crCheckError = retString;
								retString = "";
							}
							else
							{
								return retString;
							}
						}else////////
						{
							//Changed by Pavan R on 25/JAN/2K18 [Start]							            
							//errString = itmDBAccessEJB.getErrorString("", "VTPOSTORD1", "","", conn);
							errString = getErrorNew(retString, "VTPOSTORD1", conn);
							System.out.println("errString>>>>>>"+errString);
							//Changed by Pavan R on 25/JAN/2K18 [End]
								
						}
						// Changes done by apal for stock  allocation29/11/2017 ended
						//						errString = itmDBAccessEJB.getErrorString("", "VTPOSTORD1", "","", conn);
					}
				}
			}


			//if(isPostDone) // 15-mar-2021 commented as alwys message to be shown when single order posted and credit check failed message not displayed
			//{
				System.out.println("salesOrderListAll>>>>"+salesOrderListAll);
				System.out.println("postSalOder>>>>"+postSalOder);
				for(int i=0;i<salesOrderListAll.size();i++)
				{
					saleOrder=salesOrderListAll.get(i);
					for(int j=0;j<postSalOder.size();j++)
					{	
						postSal=postSalOder.get(j);
						if(postSal.trim().equalsIgnoreCase(saleOrder.trim()));
						{
							salesOrderListAll.remove(postSal);
						}
					}
				}
				System.out.println("salesOrderListAll>>> After Remove>>>"+salesOrderListAll);
				retString="";
				String mainStr="";				
				for(int i=0;i<salesOrderListAll.size();i++)
				{
					retString=retString+salesOrderListAll.get(i)+",";
				}
				if(retString !=null && retString.trim().length()>0)
				{
					retString = retString.substring(0, retString.length() - 1);
					mainStr = "\nFor Following Sales orders Not posted :"  ;
				}else
				{
					mainStr = "All Order Posted Successfully ."  ;
				}
				if (crCheckError != null && crCheckError.trim().length() > 0)
				{
					errString = crCheckError;
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTPOST03","","",conn);
				}

				String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
				String endPart = errString.substring( errString.indexOf("</trace>"));

				//added By Pavan R on 2K18/Feb/12 for 'Ship complete Order' Flag not considered in Order Posting.[Start]
				//to display posted and not posted so
				LinkedHashSet<String> sorderpostedSet = new LinkedHashSet<String>(sorderpostedList);
				ArrayList<String> sorderpstdLst = new ArrayList<String>(sorderpostedSet);
				String mainStr1="For Following Sales orders posted :" ;
				String reString1 ="";	
				
				if(sorderpstdLst.size() != 0)
				{
					for(int i=0;i<sorderpstdLst.size();i++)						
					{
						
						reString1=reString1+sorderpstdLst.get(i)+",";
					}									
					mainStr=begPart+mainStr1+reString1+mainStr+retString; 					
				}
				else
				{
					mainStr=begPart+mainStr+retString;
				}
				//mainStr=begPart+mainStr+retString;
				
				//added By Pavan R on 2K18/Feb/12 for [End]
				if(mainStr.trim().length()==0)
				{
					mainStr = begPart;
				}
				mainStr = mainStr +  endPart;	
				errString = mainStr;

			//} // 15-Mar-2021 commented


		}	
		catch(Exception e)
		{
			try 
			{
				System.out.println("@@@@@@@@@ Exception.........conn.rollback().........");
				conn.rollback();
			}
			catch (SQLException e1) 
			{
				e1.printStackTrace();
				System.out.println("Exception ::"+ e1.getMessage()); 
				throw new ITMException(e1); 
			}
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);

		} 
		finally
		{

			try
			{	
				salesOrderListAll.clear();
				postSalOder.clear();

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
				if(conn !=null)
				{
					conn.close();
					conn=null;
				}


			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::"+e.getMessage());
				throw new ITMException(e);
			}
		}

		return errString;

	}

	public String postOrder(Document headerDom,String custCode, String SaleOrder,String xtraParams,Connection conn) throws ITMException 
	{
		String fromCustCode="",toCustCode="",fromSaleOrder="",toSaleOrder="",lotSl="",toDateStr="",fromDateStr="";
		String refreshDb="",allocStk="",clubPendingOrd="",adjDrcr="",adjCustAdv="",clubOrder="",siteCodeShip="";
		String advAdjMode="",postUptoInvoice="",adjNewProdInv="",loginSiteCode="",errString="",retString="",orderType="";
		String sql="",varValue ="",errStringConf = "",invoiceId="",applyTime="I",crPolicy="";
		//		String[] custCode;
		int cnt = 0,liCtr = 0,liCc = 0;
		//Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String tranIdDespatch="",lsStatus="";
		Timestamp toDate=null, fromDate=null;
		int sordItemAllocCnt = 0;
		String errCode="";
		InitialContext ctx = null;
		String postUpto="",confDespOnPost="",autoInvOnDesp="";
		Connection connCP=null;
		ArrayList showSordrList=new ArrayList<String>();
		long startTime2 = 0, endTime2 = 0;
		//Modified by Azhar K. on [07-05-2019][Start]
		HashMap additionalMap = null;
		//Modified by Azhar K. on [07-05-2019][End]
		//Added By PriyankaC on 16OCt2019.[Start]
		String toAddr = "",ccAddr = "",bccAddr = "",subject = "",body = "",templateName = "",attachObjLinks = "",attachments = "";
		String templateCode  = fnComm.getFinparams("999999","GET_MAIL_FORMAT", conn);
		String SendEmailOnNotify = "";
		String xmlString = "",reportType = "PDF",usrLevel = "",sordListStr="";
		Calendar currentDate = Calendar.getInstance();
	//	Timestamp today = null;
//Added By PriyankaC on 16OCt2019.[END]	
		try
		{
			//Added By PriyankaC on 16Oct2019 [START].
			ArrayList soList = new ArrayList();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			Timestamp today = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			System.out.println("today date " +today);
			DBAccessEJB dbAccess = new DBAccessEJB();
			String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);
			sql = "select usr_lev from users where code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				usrLevel = checkNull(rs.getString("usr_lev"));
				userInfo.setUserLevel(usrLevel);
			}
			else
			{
				userInfo.setUserLevel("0");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//Added By PriyankaC on 16Oct2019 [END].
			//Modified by Azhar K. on [07-05-2019][Start]
			additionalMap = new HashMap();
			//Modified by Azhar K. on [07-05-2019][End]
			//conn = connDriver.getConnectDB("DriverITM");
			//conn.setAutoCommit(false);
			DistCommon dis=new DistCommon();
			//System.out.println("postOrder........");
			fromCustCode=checkNull(e12GenericUtility.getColumnValue("cust_code__fr",headerDom));
			toCustCode=checkNull(e12GenericUtility.getColumnValue("cust_code__to",headerDom));
			fromSaleOrder=checkNull(e12GenericUtility.getColumnValue("tran_id__fr",headerDom));
			toSaleOrder=checkNull(e12GenericUtility.getColumnValue("tran_id__to",headerDom));
			lotSl=checkNull(e12GenericUtility.getColumnValue("lot_sl",headerDom));
			fromDateStr=checkNull(e12GenericUtility.getColumnValue("desp_date__fr",headerDom));
			toDateStr=checkNull(e12GenericUtility.getColumnValue("desp_date__to",headerDom));
			refreshDb=checkNull(e12GenericUtility.getColumnValue("refresh_db",headerDom));
			allocStk=checkNull(e12GenericUtility.getColumnValue("alloc_stock",headerDom));
			clubPendingOrd=checkNull(e12GenericUtility.getColumnValue("club_pend_ord",headerDom));
			adjDrcr=checkNull(e12GenericUtility.getColumnValue("adj_drcr",headerDom));
			adjCustAdv=checkNull(e12GenericUtility.getColumnValue("adj_cust_adv",headerDom));
			clubOrder=checkNull(e12GenericUtility.getColumnValue("club_order",headerDom));
			siteCodeShip=checkNull(e12GenericUtility.getColumnValue("site_code",headerDom));
			advAdjMode=checkNull(e12GenericUtility.getColumnValue("adv_adj_mode",headerDom));
			postUptoInvoice=checkNull(e12GenericUtility.getColumnValue("posting_upto",headerDom));
			adjNewProdInv=checkNull(e12GenericUtility.getColumnValue("adj_new_product_invoice",headerDom));
			orderType=checkNull(e12GenericUtility.getColumnValue("order_type",headerDom));
			
			//Modified by Azhar K. on [07-05-2019][Start]
			
			additionalMap.put("ADJ_DRCR", adjDrcr);
			
			//Modified by Azhar K. on [07-05-2019][End]
			additionalMap.put("ADJ_CUST_ADV", adjCustAdv);   //added by nandkumar gadkari on 06/08/19
			fromDate = java.sql.Timestamp.valueOf(e12GenericUtility.getValidDateString(fromDateStr, e12GenericUtility.getApplDateFormat(),e12GenericUtility.getDBDateFormat()) + " 00:00:00.0");
			toDate = java.sql.Timestamp.valueOf(e12GenericUtility.getValidDateString(toDateStr, e12GenericUtility.getApplDateFormat(),e12GenericUtility.getDBDateFormat()) + " 00:00:00.0");
			/*----------------changes start by mahendra -----------------*/
			postUpto = dis.getDisparams("999999", "POST_SORDER_UPTO", conn);
			confDespOnPost = dis.getDisparams("999999", "CONFIRM_DESPATCH_ONPOST", conn);
			//autoInvOnDesp = dis.getDisparams("999999", "AUTO_INV_ON_DESPATCH", conn);
			sql=" select count(1) FROM sorder " ;
			sql = sql + " where site_code__ship = ? " ;
			if(fromCustCode.equalsIgnoreCase(toCustCode))
			{
				sql = sql + " and cust_code  = '"+fromCustCode+"' ";
			}
			else
			{
				sql = sql + " and cust_code  >= '"+fromCustCode+"' and cust_code  <= '"+toCustCode+"' " ;
			}

			sql = sql + " and due_date   >= ? and due_date   <= ? and sale_order >= ? AND sale_order <= ?" ;
			sql = sql + " and confirmed = 'Y' and status = 'P' " ;
			sql = sql + " and (case when alloc_flag is null then ' ' else alloc_flag end) <> 'Y' ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeShip);
			pstmt.setTimestamp(2, fromDate);
			pstmt.setTimestamp(3, toDate);
			if(clubOrder != null && clubOrder.equalsIgnoreCase("Y") )
			{
				pstmt.setString(4, fromSaleOrder);
				pstmt.setString(5, toSaleOrder);
			}
			else
			{
				pstmt.setString(4, SaleOrder);
				pstmt.setString(5, SaleOrder);
			}
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt =  rs.getInt(1);					
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(cnt == 0 && allocStk.equalsIgnoreCase("Y"))
			{
				retString = itmDBAccessEJB.getErrorString("", "VTALLOC04", "","", conn);
				//System.out.println("Stock Allocation:No Orders found for allocation");
			}
			else
			{
				//Added by Pavan R on 14/NOV/17 Start for club order issue

				if(clubOrder != null && clubOrder.equalsIgnoreCase("Y"))
				{
					sql = " select count(1) from sorder a, sorditem b " ;
					sql = sql +	" where a.sale_order = b.sale_order and b.site_code = ? " ;
					if(fromCustCode.trim().equalsIgnoreCase(toCustCode.trim()))//custCode
					{
						sql = sql + " and b.cust_code__dlv  = '"+fromCustCode+"' ";
					}
					else
					{
						sql = sql + " and b.cust_code__dlv  >= '"+fromCustCode+"' and b.cust_code__dlv  <= '"+toCustCode+"' " ;
					}
					sql = sql +" and a.cust_code__bil = ? " ;
					sql = sql +" and b.due_date   >= ? " ;
					sql = sql +" and b.due_date   <= ? " ;
					sql = sql +" and b.sale_order >= ? " ;
					sql = sql +" and b.sale_order <= ? " ;
					sql = sql +" and (case when b.qty_alloc is null then 0 else b.qty_alloc end) > 0 " ;

					pstmt = conn.prepareStatement(sql);

					pstmt.setString(1, custCode);
					pstmt.setString(2, siteCodeShip);
					pstmt.setTimestamp(3, fromDate);
					pstmt.setTimestamp(4, toDate);
					if(clubOrder != null && clubOrder.equalsIgnoreCase("Y") )
					{
						pstmt.setString(5, fromSaleOrder);
						pstmt.setString(6, toSaleOrder);
					}
					else
					{
						pstmt.setString(5, SaleOrder);
						pstmt.setString(6, SaleOrder);
					}
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						sordItemAllocCnt =  rs.getInt(1);					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


				}
				else
				{
					sql = " select count(1) from sorditem b " ;
					sql = sql +	" where b.site_code = ? " ;
					if(fromCustCode.trim().equalsIgnoreCase(toCustCode.trim()))
					{
						sql = sql + " and cust_code__dlv  = '"+fromCustCode+"' ";
					}
					else
					{
						sql = sql + " and cust_code__dlv  >= '"+fromCustCode+"' and cust_code__dlv  <= '"+toCustCode+"' " ;
					}
					sql = sql +" and b.due_date   >= ? " ;
					sql = sql +" and b.due_date   <= ? " ;
					sql = sql +" and b.sale_order >= ? " ;
					sql = sql +" and b.sale_order <= ? " ;
					sql = sql +" and (case when b.qty_alloc is null then 0 else b.qty_alloc end) > 0 " ;

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeShip);
					pstmt.setTimestamp(2, fromDate);
					pstmt.setTimestamp(3, toDate);
					if(clubOrder != null && clubOrder.equalsIgnoreCase("Y") )
					{
						pstmt.setString(4, fromSaleOrder);
						pstmt.setString(5, toSaleOrder);
					}
					else
					{
						pstmt.setString(4, SaleOrder);
						pstmt.setString(5, SaleOrder);
					}
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						sordItemAllocCnt =  rs.getInt(1);					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				//Added End by Pavan R on 14/NOV/17 for club order issue
				/*System.out.println("sordItemAllocCnt :"+sordItemAllocCnt);
				System.out.println("clubOrder :"+clubOrder);*/
				if( sordItemAllocCnt == 0 ) 
				{	

					//Changed By Nasruddin Start 04-11-16
					startTime2 = System.currentTimeMillis();
					if("Y".equalsIgnoreCase(clubOrder))    
					{			

						errCode = postOrder(fromSaleOrder,toSaleOrder,custCode,custCode, fromDate, toDate,lotSl,siteCodeShip,xtraParams,conn);
						endTime2 = System.currentTimeMillis();
						System.out.println("Posting time taken for SO [" + fromSaleOrder + "] - [" +toSaleOrder + "] ["+(endTime2-startTime2)/1000+"] seconds");

					}
					else	
					{              

						errCode = postOrder(SaleOrder,SaleOrder,fromCustCode,toCustCode, fromDate, toDate,lotSl,siteCodeShip,xtraParams,conn);
						endTime2 = System.currentTimeMillis();
						System.out.println("Posting time taken for SO [" + SaleOrder + "] ["+(endTime2-startTime2)/1000+"] seconds");
					}
					//Changed By Nasruddin END 04-11-16

				}

				//System.out.println("errCode.trim().length()!!!! :"+errCode);

				if(errCode != null && errCode.trim().length() > 0)
				{
					conn.rollback();
					//added by nandkumar gadkari on 12/05/19 
					for(Log log:erroLogSordItme)
					{
						postLog(log,conn);
					}
					
					return errCode;
					//RETURN;
				}
				//else if("Y".equalsIgnoreCase(allocStk))
				else
				{
					//conn.commit();
					//errCode="VTALLOC03";

					/*System.out.println("Checking all process of post order!!!!");
					System.out.println("Despatch creation Test@@@@@@@@@");*/
					//dispact coding starts
					//added by priyanka

					PostOrdDespatchGen postOrdDespatchGen=new PostOrdDespatchGen();
					//showSordrList= postOrdDespatchGen.createDespatch(fromSaleOrder, toSaleOrder, fromCustCode, toCustCode, fromDate, toDate, siteCodeShip,clubOrder, conn);
					//showSordrList= postOrdDespatchGen.createDespatch(SaleOrder, SaleOrder, custCode, custCode, fromDate, toDate, siteCodeShip,clubOrder, conn);
					//Changed By Nasruddin Start 04-11-16
					startTime2 = System.currentTimeMillis();
					if("Y".equalsIgnoreCase(clubOrder))    
					{   //Modified by Azhar K. on [07-05-2019][Start]
						//errString= postOrdDespatchGen.createDespatch(fromSaleOrder, toSaleOrder, custCode, custCode, fromDate, toDate, siteCodeShip,clubOrder, xtraParams, conn);
						errString= postOrdDespatchGen.createDespatch(fromSaleOrder, toSaleOrder, custCode, custCode, fromDate, toDate, siteCodeShip,clubOrder, xtraParams, conn,additionalMap);
						//Modified by Azhar K. on [07-05-2019][End]
						endTime2 = System.currentTimeMillis();
						System.out.println("createDespatch time taken for SO [" + fromSaleOrder + "] - [" +toSaleOrder + "] ["+(endTime2-startTime2)/1000+"] seconds");

					}else
					{  //Modified by Azhar K. on [07-05-2019][Start]
						////errString= postOrdDespatchGen.createDespatch(fromSaleOrder, toSaleOrder, custCode, custCode, fromDate, toDate, siteCodeShip,clubOrder, xtraParams, conn);
						errString= postOrdDespatchGen.createDespatch(SaleOrder, SaleOrder, fromCustCode, toCustCode, fromDate, toDate, siteCodeShip,clubOrder, xtraParams, conn,additionalMap);
						//Modified by Azhar K. on [07-05-2019][End]
						endTime2 = System.currentTimeMillis();
						System.out.println("createDespatch time taken for SO [" + SaleOrder + "] ["+(endTime2-startTime2)/1000+"] seconds");
						System.out.println("Before confirm Calling sendMailonConfirm");
					}
					//	System.out.println("ErrCode from Despatch creation===="+errCode);
					//added by nandkumar gadkari on 08/05/19 --------start---------for stock mismatch case 
					if(errString !=null && errString.trim().length() > 0)
					{
						conn.rollback();
						if(connCP!=null )
						{
							connCP.rollback();	
						}
					}
					else
					{
						conn.commit();
						if(connCP!=null )
						{
							connCP.commit();
						}
						System.out.println("Before confirm Calling sendMailonConfirm");
						cnt = 0;
						//Added by PriyankaC to send mail to customer on invoice confirmation.[Start].
						System.out.println("Value of postUptoInvoice and clubOrder" +postUptoInvoice +""+clubOrder);
						if("I".equalsIgnoreCase(postUptoInvoice) )
						{
							if( "Y".equalsIgnoreCase(clubOrder))
							{


								sql = " select sale_order from sorder where sale_order >= ? and sale_order <= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, fromSaleOrder);
								pstmt.setString(1, toSaleOrder);
								rs = pstmt.executeQuery();
								while (rs.next()) 
								{
									soList.add(checkNull(rs.getString("sale_order")));
								}
								rs.close();
								rs = null;
								pstmt.close();	
							}
							else
							{
								soList.add(SaleOrder);
							}
							for (int ctr = 0; ctr < soList.size(); ctr++)
							{
								sql = "select max(invoice_id) as invoice_id from invoice where sale_order = ?   and TRAN_DATE = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,(String)(soList.get(ctr)));
								pstmt.setTimestamp(2, today);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									invoiceId =  checkNull(rs.getString("invoice_id"));
									System.out.println("invoiceId :" +invoiceId);
									if(invoiceId!= null && invoiceId.trim().length() >0)
									{
										errString =  sendMailonConfirm(invoiceId,fromCustCode,templateCode,userInfo,conn);
									}
								}
								rs.close();
								rs = null;
								pstmt.close();								
							}
							System.out.println("errString from send mail" +errString);
							/*if(errString!= null && errString.trim().length() > 0)
							{
								String begPart = errString.substring(0,errString.indexOf("<message>")+9);
								String endDesc = errString.substring(errString.indexOf("</description>"));
								errString= begPart+"Invalid Data"+"</message><description><![CDATA[";
								errString= errString+"retString "+errString+" "+"Mail Not Send to customer"+"]]>"+endDesc;
								return errString;
							}*/
							System.out.println("After confirm Calling SendEmail");
						}
						//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[Start]
					}
					//System.out.println("postLog:::"+erroLogSordItme);
					for(Log log:erroLogSordItme)
					{
						postLog(log,conn);
					}
					//added by nandkumar gadkari on 08/05/19 --------end---------for stock mismatch case
					//Changed By Nasruddin END 04-11-16
					if (errCode == null || errCode.trim().length()==0)
					{
						//added By Pavan R on 2K18/Feb/12 for 'Ship complete Order' Flag not considered in Order Posting.[Start]
						//to display posted and not posted so
						if ("Y".equalsIgnoreCase(clubOrder))
						{	
							for (String postedSO : sorderpostedList) 
							{
								postSalOder.add(postedSO);
							}
						}
						else
						{
							postSalOder.add(SaleOrder);
						}
						//postSalOder.add(SaleOrder);
						//added By Pavan R on 2K18/Feb/12[End]
					}
					/*for(int i=0;i<showSordrList.size();i++)
					{
						errString=errString+showSordrList.get(i)+",";
					}
					if(errString !=null && errString.trim().length()>0)
					{
						errString = errString.substring(0, errString.length() - 1);
						postSalOder.add(errString);
					}*/
					//System.out.println(" postSalOder list>>>"+postSalOder);


					/*if(errCode != null && errCode.trim().length() > 0)
					{
						if(errCode.indexOf("VTCONPARM") > -1)
						{
							if("D".equalsIgnoreCase(postUpto) && "N".equalsIgnoreCase(confDespOnPost))
							{
								errString="";
								return errString;
							}
							else
							{

								String[] arrayForTranId = errCode.split("<TranID>");

								System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId);
								System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId[1]);

								int endIndex = arrayForTranId[1].indexOf("</TranID>");

								System.out.println("endIndex:::::::"+endIndex);

								tranIdDespatch = arrayForTranId[1].substring(0, endIndex);
								System.out.println("tranIdDespatch====="+tranIdDespatch);
								System.out.println("Calling Despatch Confirmation");	
								String despIdArr[]=tranIdDespatch.split(",");

								if(despIdArr.length>0)
									{
										DespatchConfirm despatchConfirm = null;
										despatchConfirm = new DespatchConfirm();
										String forcedFlag="N";
										String desTranId="",desSOrder="";
										for(int ctr=0;ctr<despIdArr.length;ctr++)
										{
											String[] arrStr =despIdArr[ctr].split("@");
											if(arrStr.length>0)
											{
												 desTranId =arrStr[0];
												System.out.println("desTranId>>>>"+desTranId);
											}
											if(arrStr.length>1)
											{
												 desSOrder =arrStr[1];
												System.out.println("desSOrder>>>>"+desSOrder);
											}
											connCP=chaneParnerExist(desTranId,xtraParams,conn);
											errString=despatchConfirm.confirm(desTranId, xtraParams, forcedFlag,conn,connCP);
											System.out.println("despatchConfirm return string >>>>"+errString);
											//errString=despatchConfirm.confirm(despIdArr[ctr], xtraParams, forcedFlag,conn);
											if(errString == null || errString.trim().length()==0 || "".equalsIgnoreCase(errString) || errString.contains("CONFSUCC")|| errString.contains("VTPOSTDES") )
											{
												errString = "";
												connCP.commit();
												conn.commit();
											}
											else
											{
												connCP.rollback();	
												conn.rollback();
												break;
											}
										}
									}






								System.out.println("errCode in despatch confirmation===="+errString);
							}
						}
					}*/

					/*  //commented same code repeate in despatchconfirm...					
					if(errStringConf != null && errStringConf.trim().length() > 0)
					{
						if(errStringConf.indexOf("VTPOSTDES") > -1)
						{	

							//conn.commit();
							String[] arrayForTranId = errStringConf.split("<TranID>");

							System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId);
							System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId[1]);

							int endIndex = arrayForTranId[1].indexOf("</TranID>");

							System.out.println("endIndex:::::::"+endIndex);

							tranIdDespatch = arrayForTranId[1].substring(0, endIndex);
							System.out.println("tranIdDespatch====="+tranIdDespatch);
							System.out.println("Calling Despatch Posting");							

							InvAcct inv = new InvAcct();
							errString = inv.despatchPost(tranIdDespatch,"S-DSP",conn);

							System.out.println("errCode in despatch posting====["+errString+"]");
							if(errString.trim().length() == 0 || errString == null)
							{
								errString=invPosting( fromSaleOrder, toSaleOrder, fromCustCode, toCustCode, tranIdDespatch,  orderType, fromDate, clubOrder, clubPendingOrd, adjDrcr, adjCustAdv, advAdjMode, adjNewProdInv, siteCodeShip, conn);
								System.out.println("@@@@@@@@@689 invPosting() errString::::::["+errString+"]");
							}
						}

					}
					 */


				}

				/*------------------------------------------------------------------------------*/
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	//Modified by Anjali R. on [12/11/2018][DepatchConfirm class's object pass as parameter][Start]
	//public String invPosting(String fromSaleOrder,String toSaleOrder,String fromCustCode,String toCustCode,String tranIdDespatch, String orderType,Timestamp fromDate,String clubOrder,String clubPendingOrd,String adjDrcr,String adjCustAdv,String advAdjMode,String adjNewProdInv,String siteCodeShip,String xtraParams,Connection conn) throws ITMException
	public String invPosting(String fromSaleOrder,String toSaleOrder,String fromCustCode,String toCustCode,String tranIdDespatch, String orderType,Timestamp fromDate,String clubOrder,String clubPendingOrd,String adjDrcr,String adjCustAdv,String advAdjMode,String adjNewProdInv,String siteCodeShip,String xtraParams,Connection conn) throws ITMException, SQLException
	{
		String retString = "";
		String poRcpNo = "";
		try
		{
			retString = invPosting(fromSaleOrder, toSaleOrder, fromCustCode, toCustCode, tranIdDespatch, orderType, fromDate, clubOrder, clubPendingOrd, adjDrcr, adjCustAdv, advAdjMode, adjNewProdInv, siteCodeShip, xtraParams, poRcpNo, conn);
		}
		catch(Exception e)
		{
			conn.rollback();
			System.out.println("Exception--["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	
	public String invPosting(String fromSaleOrder,String toSaleOrder,String fromCustCode,String toCustCode,String tranIdDespatch, String orderType,Timestamp fromDate,String clubOrder,String clubPendingOrd,String adjDrcr,String adjCustAdv,String advAdjMode,String adjNewProdInv,String siteCodeShip,String xtraParams,String poRcpTranId,Connection conn) throws ITMException
	//Modified by Anjali R. on [12/11/2018][DepatchConfirm class's object pass as parameter][End]
	{
		System.out.println("@@@@@@@@@@@@ invPosting() called.........");
		Boolean adjDrcrFlag= false, adjAdv=false, adjNewProdInvFlag=false;
		String advAdj="";
		String commDrcrConf="",commJvConf="";
		DistCommon dis=new DistCommon();
		String errString="",invoiceId="",sql="",ls_drnid="",loginEmpCode,custCodeBil="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String applyTime="I",crPolicy="",lsStatus="",finScheme="";
		double finAdjAmt=0;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String autoInvOnDesp = dis.getDisparams("999999", "AUTO_INV_ON_DESPATCH", conn);
		InitialContext ctx = null;
		Timestamp sysDate = null;
		//Added By PriyankaC on 16OCt2019.[Start]
				String toAddr = "",ccAddr = "",bccAddr = "",subject = "",body = "",templateName = "",attachObjLinks = "",attachments = "";
				String templateCode  = fnComm.getFinparams("999999","GET_MAIL_FORMAT", conn);
				String SendEmailOnNotify = "";
				String xmlString = "",reportType = "PDF",usrLevel = "";
		//Added By PriyankaC on 16OCt2019.[END]	
		String siteType="",creatInvOthlist="",creatInvOth="",otherSite="",refDate="";
		if("Y".equalsIgnoreCase(autoInvOnDesp))
		{

			//conn.commit(); // added testing purpose
			try
			{
				Calendar currentDate = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				String sysDateStr = sdf.format(currentDate.getTime());
				//System.out.println("Now the date is :=>  " + sysDateStr);
				sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				//System.out.println("xtraParams>>>"+xtraParams);
				loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
				PostOrdInvoiceGen invgen = new PostOrdInvoiceGen();
				/*System.out.println("from date ::::::::"+fromDate);								
				//	SimpleDateFormat dateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
				//String frmDate  = dateFormat.format(fromDate);
				System.out.println("Date parsedd:::::::::::"+fromDate);
				System.out.println("Posting fromSaleOrder :::::::"+fromSaleOrder);
				System.out.println("Posting toSaleOrder :::::::"+toSaleOrder);
				System.out.println("Posting fromCustCode :::::::"+fromCustCode);
				System.out.println("Posting toCustCode :::::::"+toCustCode);
				System.out.println("Posting tranIdDespatch :::::::"+tranIdDespatch);
				System.out.println("Posting orderType :::::::"+orderType);
				System.out.println("Posting frmDate :::::::"+fromDate);
				System.out.println("Posting clubOrder :::::::"+clubOrder);
				System.out.println("Posting clubPendingOrd :::::::"+clubPendingOrd);
				System.out.println("Posting adjDrcr :::::::"+adjDrcr);
				System.out.println("Posting adjCustAdv :::::::"+adjCustAdv);
				System.out.println("Posting advAdjMode :::::::"+advAdjMode);
				System.out.println("Posting adjNewProdInv :::::::"+adjNewProdInv);*/

				//Added By PriyankaC on 16Oct2019 [START].
				DBAccessEJB dbAccess = new DBAccessEJB();
				String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);
				sql = "select usr_lev from users where code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, loginCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					usrLevel = checkNull(rs.getString("usr_lev"));
					userInfo.setUserLevel(usrLevel);
				}
				else
				{
					userInfo.setUserLevel("0");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//Added By PriyankaC on 16Oct2019 [END].
				
				if("Y".equalsIgnoreCase(adjDrcr))
				{
					adjDrcrFlag = true;
				}

				if("Y".equalsIgnoreCase(adjCustAdv))
				{
					adjAdv = true;
				}
				if("Y".equalsIgnoreCase(adjNewProdInv))
				{
					adjNewProdInvFlag = true;
				}
				advAdj = advAdjMode;
				//Changed By Nasruddin Start 04-11-16
				long startTime2 = System.currentTimeMillis();
				errString = invgen.invoiceProcess(fromSaleOrder, toSaleOrder, fromCustCode, toCustCode, tranIdDespatch, orderType, fromDate, clubOrder, clubPendingOrd, adjDrcr, adjCustAdv, advAdjMode, adjNewProdInv,xtraParams, conn);
				long endTime2 = System.currentTimeMillis();
				System.out.println("DIFFERANCE IN TIME INVOICE PROCESS DATA IN SECONDS INSIDE invPosting METHOD:::["+(endTime2-startTime2)/1000+"]");
				//Changed By Nasruddin END 04-11-16
				//System.out.println("@@@@@@@@@695::::invgen.invoiceProcess():::errString["+errString+"]");
				//if(errString != null && errString.trim().length()>0 )
				//{
				//	System.out.println("Invoice not Generated !!!!!!!!!!!!!!!!!!!!!!!!!!!");
				//	conn.rollback();	
				//}
				if( errString != null && errString.trim().length() > 0 && errString.contains("Success"))
				{

					String[] arrayForTranId1 = errString.split("<TranID>");

					//System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId1);
					//System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId1[1]);

					int endIndex1 = arrayForTranId1[1].indexOf("</TranID>");

					//System.out.println("endIndex1:::::::"+endIndex1);

					invoiceId = arrayForTranId1[1].substring(0, endIndex1);
					//System.out.println("invoiceId=====["+invoiceId+"]");

					//Modified by Anjali R. on[12/11/2018][To update invoice number in purchase receipt][Start]
					if(poRcpTranId != null && poRcpTranId.trim().length() > 0)
					{
						sql = "update PORCP set INVOICE_DATE = ?,INVOICE_NO = ? where TRAN_ID = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, fromDate);
						pstmt.setString(2, invoiceId);
						pstmt.setString(3, poRcpTranId);
						int count = pstmt.executeUpdate();
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						System.out.println("updated ["+count+"] rows in porcp table");
					}
					//Modified by Anjali R. on[12/11/2018][To update invoice number in purchase receipt][end]
					
					String itemSer=""; 
					double netAmt=0,lc_check_amt=0;

					sql = " select item_ser,net_amt from invoice where invoice_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, invoiceId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer =  rs.getString("item_ser");					
						netAmt =  rs.getDouble("net_amt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					errString = "";
					
					sql="Select cust_code__bil from sorder where sale_order= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, fromSaleOrder);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						custCodeBil =  rs.getString("cust_code__bil");					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					
					System.out.println("Inside invPosting credit check cust_code["+fromCustCode+"] custCodeBil["+custCodeBil+"]");
					
					HashMap paramMap = new HashMap();
					//Changed By PriyankaC on 04June2019 to set correct custCodeSold and Bill to value [Start].
					/*paramMap.put("as_cust_code_bil",fromCustCode);
					//added by kunal on 12/11/2018 to add custCodeBil in map for credit check
					paramMap.put("as_cust_code_sold_to", custCodeBil);*/
					paramMap.put("as_cust_code_bil",custCodeBil);
					paramMap.put("as_cust_code_sold_to",fromCustCode);
					//Changed By PriyankaC on 04June2019 to set correct custCodeSold and Bill to value [END].
					paramMap.put("as_item_ser",itemSer);
					paramMap.put("ad_net_amt",""+netAmt);
					paramMap.put("as_sorder",fromSaleOrder);
					paramMap.put("adt_tran_date",fromDate);  //fromDate  fromDateStr
					paramMap.put("as_site_code",siteCodeShip);
					paramMap.put("as_apply_time", applyTime);
					paramMap.put("as_despid",tranIdDespatch);
					//System.out.println("@@@@@@@@@@ paramMap["+paramMap+"]");

					PostOrdCreditChk postOrdCreditChk = new PostOrdCreditChk();
					//errString = postOrdCreditChk.CreditCheck(paramMap, conn);
					ArrayList<String> errStringList = new ArrayList<String>(); 
					errStringList = postOrdCreditChk.CreditCheck(paramMap, conn);
					System.out.println("@@@@@@@@@@@ errStringList size["+errStringList.size()+"]");
					//Pavan Rane 27aug19 start[to display error message to front end]
					if(errStringList.size() > 0 && errStringList.contains("Error"))
					{
						conn.rollback();
						errString = errStringList.get(errStringList.indexOf("Error")+1);
						return errString;
					}
					//if( errStringList.size() > 0 ) 
					else if( errStringList.size() > 0 )  //Pavan Rane 27aug19 end 
					{
						conn.rollback();
						int retCtr = writeBusinessLogicCheck( errStringList,siteCodeShip,applyTime,conn );

						//System.out.println("@@@@@@@@@ insert retCtr["+retCtr+"]errStringList.size()["+errStringList.size()+"]");
						if( retCtr > 0  )
						{
							//System.out.println("@@@@@@@@@ errorlist and inserted record missmatch........");
							conn.commit();
						}
						//errString = "VTWBLGCCHK";
						errString = itmDBAccessEJB.getErrorString("", "VTWBLGCCHK", "","", conn);
						//System.out.println("@@@@@@@@@@ writeBusinessLogicCheck errString["+errString+"]");
					}

					//	System.out.println("@@@@@@@@@2 postOrdCreditChk errString["+errString+"]");

					if( errString == null || errString.trim().length() == 0 )
					{
						// Commented by Manoj dtd 20/04/2016 not required
						ArrayList<String> retArrayList = new ArrayList<String>();
						// i_nvo_sales.gbf_credit_check_update(ls_sale_order,ls_cr_policy,mnet_amt,'S',lc_check_amt,'C',ls_status) 	//Added Ruchira 29/08/2k6, to pass invoice amt.
						retArrayList = postOrdCreditChk.credit_check_update(fromSaleOrder, crPolicy, netAmt, "S", lc_check_amt, "C", lsStatus, conn);
						/*
						// added on 19/04/16 for status and amount
						System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
						if( retArrayList.size() > 0)
						{
							lsStatus = retArrayList.get(0);
						}
						if( retArrayList.size() > 1)
						{
							netAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
						}	
						System.out.println("@@@@@ crPolicy["+crPolicy+"]lc_check_amt["+lc_check_amt+"]lsStatus["+lsStatus+"]");
						 */
						errString=postordact.prdSchemeTraceUpd(invoiceId,xtraParams,conn);
						//PriyankaC on 9oct2018 [START]
						if( errString == null || errString.trim().length() == 0 )
						{
							errString = postordact.updateSchemBalance(invoiceId,xtraParams,conn);
						}
						//PriyankaC on 9oct2018 [END]
						if( errString == null || errString.trim().length() == 0 )
						{
							//gbf_scheme_history
							errString=postordact.schemeHistoryUpd(invoiceId,siteCodeShip,"I", conn);
						}
						if( errString == null || errString.trim().length() == 0 )
						{
							errString=postordact.schemeDiscTrace(invoiceId,xtraParams,conn);
						}
						if( errString == null || errString.trim().length() == 0 )
						{
							sql="select fin_scheme from sorder where sale_order = ?";	
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, fromSaleOrder);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								finScheme=rs.getString(1);
							}
							rs.close();
							rs=null;
							pstmt.close();
							pstmt=null;
							sql="select abs(sum(tot_amt - adj_amt) ) from receivables "
									+ " where cust_code = ? and fin_scheme = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, fromCustCode);
							pstmt.setString(2, finScheme);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								finAdjAmt=rs.getDouble(1);
							}
							rs.close();
							rs=null;
							pstmt.close();
							pstmt=null;
							if(finAdjAmt>=netAmt)
							{
								errString=postordact.finSchemeInvAdj(siteCodeShip,itemSer,fromCustCode,invoiceId,netAmt,adjDrcrFlag, adjAdv,conn);
							}
						}
						if( errString == null || errString.trim().length() == 0 )
						{
							//System.out.println("@@@@@@@@@@@@ postOrdCreditChk()...........finish.........");
							//System.out.println("@@@@@@@@@@@@ InvoiceDrcrAdj()...........calling..........");
							sql = " select tran_id from misc_drcr_rcp where sreturn_no in (select desp_id " +
									" from despatch where desp_id in ( ? ) " +
									" and case when FREIGHT_AMT_ADD is null then 0 else FREIGHT_AMT_ADD end > 0) "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdDespatch);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								ls_drnid =  ls_drnid +","+ checkNull(rs.getString("tran_id"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							//System.out.println("@@@@@@@@@@@ ls_drnid["+ls_drnid+"]");
							String frtDrnArr[]=ls_drnid.split(",");
							//System.out.println("@@@@@ frtDrnArr.length["+frtDrnArr.length+"]");
							InvoiceDrcrAdj invoiceDrCrAdj1 = new InvoiceDrcrAdj();
							errString = invoiceDrCrAdj1.invoiceDrcrAdj(siteCodeShip, invoiceId, custCodeBil, itemSer, netAmt, adjDrcrFlag, adjAdv, advAdj, adjNewProdInvFlag, frtDrnArr, conn);//toCustCode Change to  custCodeBil by nandkumar gadkari on 12/08/19 
							//retString = invoiceDrCrAdj1.invoiceDrcrAdj(siteCodeShip, invoiceId, toCustCode, itemSer, netAmt, adjDrcrFlag, adjCustAdvFlag, advAdj, adjNewProdInvFlag, frtDrnArr, conn);
							//System.out.println("@@@@@@@@@3 invoiceDrCrAdj1.invoiceDrcrAdj() errString["+errString+"]");
						}

						if( errString == null || errString.trim().length() == 0 )
						{
							//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
							PostOrdInvoicePost postOrdInvoicePost =  new PostOrdInvoicePost();
							String forcedFlag="N";
							String retString1=postOrdInvoicePost.invoicePosting(invoiceId, xtraParams, forcedFlag,conn);
							//postOrdInvoicePost = null; COMMENTED BY NANDKUMAR GADKARI ON 13/01/20
							System.out.println("PostOrdInvoicePost return string >>>>"+retString1);
							if( retString1 != null && retString1.trim().length() > 0 )
							{
								return retString1;
							}
							sql="update invoice set confirmed = 'Y',conf_date=?, emp_code__aprv = ?  where invoice_id = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setTimestamp(1,sysDate);
							pstmt.setString(2,loginEmpCode);
							pstmt.setString(3,invoiceId);
							pstmt.executeUpdate();
							pstmt.close();
							pstmt=null;
							
							//added by nandkumar gadkari on 13/01/20-----------------------start---------------------
							
							sql = " select site_type  from site where site_code = ? "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeShip);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								siteType= checkNullAndTrim(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							
							creatInvOthlist = fnComm.getFinparams("999999", "ALOW_INV_OTH_SITE", conn); 
							
							if( "NULLFOUND".equalsIgnoreCase(creatInvOthlist) || creatInvOthlist == null)
							{
								creatInvOthlist="";
							}
							
							if(creatInvOthlist.trim().length() > 0)
							{
								String[] arrStr = creatInvOthlist.split(",");
								for (int i = 0; i < arrStr.length; i++) {
									creatInvOth = arrStr[i];
									System.out.println("creatInvOth>>>>>>>>" + creatInvOth);
									if(siteType.equalsIgnoreCase(creatInvOth.trim()))
									{
										otherSite = fnComm.getFinparams("999999", "INVOICE_OTHER_SITE", conn); 
										if( !"NULLFOUND".equalsIgnoreCase(creatInvOthlist) && creatInvOthlist != null && creatInvOthlist.trim().length() > 0)
										{
										
											retString1=postOrdInvoicePost.gbfCreateInvHdrOth( invoiceId,  otherSite , "", refDate, xtraParams,  conn);
											if( retString1 != null && retString1.trim().length() > 0 )
											{
												return retString1;
											}
										}
									}
									
								}	
							}
							
						
							//added by nandkumar gadkari on 13/01/20-----------------------end---------------------
							
							//Added Arun by p 31-10-17 for generate edi outbond data when edi medium=4 and edi medium=1 -Start
							String ediOption = "";
							sql = "select edi_option from transetup where tran_window = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, "w_invoice");
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								ediOption = rs.getString("edi_option");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("ediOption:["+ediOption+"]");
							System.out.println("invoiceId:["+invoiceId+"]");
							if("1".equalsIgnoreCase(ediOption))
							{
								CreateRCPXML createRCPXML = new CreateRCPXML("w_invoice", "tran_id");
								String dataStr = createRCPXML.getTranXML(invoiceId, conn);
								System.out.println("dataStr =[ " + dataStr + "]");
								Document ediDataDom = genericUtility.parseString(dataStr);
								System.out.println("xtraParams:["+xtraParams+"]");
								E12GenerateEDIEJB e12GenerateEDIEJB = new E12GenerateEDIEJB();
								String retString = e12GenerateEDIEJB.nfCreateEdiMultiLogic(ediDataDom,"w_invoice", xtraParams);
								System.out.println("retString from E12GenerateEDIEJB before = ["+ retString + "]");
								if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
								{
									System.out.println("retString from E12GenerateEDIEJB = ["+ retString + "]");
								}
							}
							//Added Arun p 31-10-17 for generate edi outbond data when edi medium=4 and edi medium=1 -Ended
							//CreteCommCrNote creteCommCrNote = new CreteCommCrNote();
							CalculateCommission calCom=new CalculateCommission();
							commDrcrConf = checkNull(fnComm.getFinparams("999999", "COMM_DRCR_CONF", conn));
							commJvConf = checkNull(fnComm.getFinparams("999999", "COMM_JV_CONF", conn));
							errString =  calCom.CalCommission(invoiceId,"I","",commDrcrConf,commJvConf,xtraParams, conn);
							//errString =  postordact.createCommCrNote(invoiceId, conn);
							//System.out.println("@@@@@@@@@4 CreteCommCrNote() errString["+errString+"]");
							if( errString != null && errString.trim().length() > 0 )
							{
								return errString;
							}
							if( errString == null || errString.trim().length() == 0 )
							{
								//	AutoExciseDrNote autoExciseDrNote = new AutoExciseDrNote();
								errString =  postordact.autoExciseDrNote(invoiceId,xtraParams, conn);
								//System.out.println("@@@@@@@@@5 autoExciseDrNote() errString["+errString+"]");
							}
							else
							{
								//System.out.println("@@@@@@@@@ error in autoExciseDrNote.autoExciseDrNote.....");
								return errString;	
							}
						}
						else
						{
							//System.out.println("@@@@@@@@@ error in invoiceDrCrAdj1.invoiceDrcrAdj.....");
							return errString;	
						}
					}
					else
					{
						//System.out.println("@@@@@@@@@ error in postOrdCreditChk.CreditCheck(paramMap, conn)......");
						return errString;
						//conn.rollback();	
					}
				}
				else
				{
					System.out.println("Invoice not Generated !!!!!!!!!!!!!!!!!!!!!!!!!!!");
					conn.rollback();	
				}

			}catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			return errString;
		}
		return errString;
	}
	public int writeBusinessLogicCheck(ArrayList<String> errStringList, String siteCode,String applyTime, Connection conn) throws ITMException, Exception  
	{
		
		PreparedStatement pstmt = null;
		String sql = "",tranId="";
		int cnt = 0,cnt2=0;
		ResultSet rs = null;
		//Added by Pavan R on 27/OCT/17
		double chkamt =0;
		String lsStatus = null;
		//Pavan R End
		String lsCrPolicy= "",asDespId="",asSorder="", lsStr="";
		String custCodeBil="",custCodeSold="",refDate="",itemSer="";  
		String jsonStr = "";
		//added by manish mhatre on 28-6-2019
		Timestamp refDateTs=null,tranDate = null;
		String rfDate = null;
		/*Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat sdf;
		SimpleDateFormat simpleDateFormat = null;
		sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String tranDateStr = sdf.format(tranDate.getTime());*/
		
		//end manish mhatre 
		int insrtCnt = 0;
		System.out.println("@@@@@@@@@@ errStringList["+errStringList.size()+"]");
		

		for( int i = 0 ; i < errStringList.size(); i++)
		{
			lsCrPolicy= "";asDespId="";asSorder=""; lsStr="";
			cnt2=0;

			String str = errStringList.get(i).toString();

			//System.out.println("@@@@@@@ i["+i+"]str["+str+"]");

			String strArray[]=str.split("\t");
			//System.out.println("@@@@@ strArray.length["+strArray.length+"]");
			for(int j=0;j<strArray.length;j++)
			{
				if( strArray.length > 0 )
				{
					lsCrPolicy=strArray[0];
					System.out.println("cr policy:"+lsCrPolicy);
				}
				if( strArray.length > 1 )
				{
					asDespId=strArray[1];
					System.out.println("desp id:"+asDespId);
				}
				if( strArray.length > 2 )
				{
					asSorder=strArray[2];
					System.out.println("sorder:"+asSorder);
				}
				if( strArray.length > 3 )
				{
					
					//lsStr=strArray[3];//Pavan R on 14sept18 [to handle NumberFormatException from lsStr in failedPolicyList]
					chkamt =(Double.parseDouble(strArray[3]));
					System.out.println("chk amt:"+chkamt);
				}
				//Added by Pavan R on 27/OCT/17
				if( strArray.length > 4 )
				{					
					//chkamt =(Double.parseDouble(strArray[4]));//Pavan R on 14sept18 [to handle NumberFormatException from lsStr in failedPolicyList]
					lsStr=strArray[4];
					System.out.println("lsstr:"+lsStr);
				}
				if( strArray.length > 5 )
				{
					System.out.println("Out side if lsStatus::["+lsStatus+"]");
					lsStatus = checkNull(strArray[5]);
					if (lsStatus.trim().equalsIgnoreCase("null"))
					{
						System.out.println("In side if lsStatus::["+lsStatus+"]");
						lsStatus =" " ;
					}

				}
				
				//added by manish mhatre on 28-6-2019
				if(strArray.length > 6)
				{
					custCodeBil=strArray[6];
					System.out.println("bill:"+custCodeBil);
				}
				
				if(strArray.length>7)
				{
					custCodeSold=strArray[7];
					System.out.println("sold:"+custCodeSold);
				}
				
				if(strArray.length>8)
				{
					/*siteCode=strArray[8];
					System.out.println("site code:"+siteCode);*/
					siteCode = checkNull(strArray[8]);
					
				}
				
				if(strArray.length>9)
				{
					itemSer=checkNull(strArray[9]);
					System.out.println("item ser:"+itemSer);
				}
				
				if(strArray.length>10)
				{
					/*refDate=strArray[10];
					System.out.println("refdate"+refDate);
					
					if(refDate!=null && refDate.trim().length()>0)
					{
						refDateTs = Timestamp.valueOf(genericUtility.getValidDateString( refDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						tranDate=Timestamp.valueOf(genericUtility.getValidDateString(rfDate,genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						System.out.println("refdate timestamp:"+refDateTs);
					}*/
					refDate=strArray[10];
					System.out.println("refdate"+refDate);
					
					if(refDate!=null && refDate.trim().length()>0)
					{
						tranDate = Timestamp.valueOf(genericUtility.getValidDateString( refDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				
						/*tranDate=Timestamp.valueOf(genericUtility.getValidDateString(rfDate,genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");*/
						System.out.println("refdate timestamp:"+tranDate);
					}
					}
				//Modified by Rohini T on 26/04/2021[Start]
				if(strArray.length>11)
				{
					jsonStr=checkNull(strArray[11]);
					System.out.println("jsonStr :"+jsonStr);
				}
				//Modified by Rohini T on 26/04/2021[End]
				
				System.out.println("refdate timestamp1:"+tranDate);
					 //end manish mhatre
				
				System.out.println("chkamt::["+chkamt+"]::lsStatus::["+lsStatus+"]");
				// Pavan R End
			}
			//System.out.println("@@@@@@@@["+lsCrPolicy+"]["+asDespId+"]["+asSorder+"]["+lsStr+"]");

			// 23-dec-2020 manoharan as per mail from KB to delete old failed rows and do a fresh check
			// to avoid override in case bank receipt made later after earlier credit check against the order
			if(i == 0)
			{
				sql = "delete from  business_logic_check where sale_order = ? and aprv_stat = 'F' ";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,asSorder);
				int delCount = pstmt.executeUpdate();
				if(pstmt!=null)//Modified by Rohini T on 10/05/2021
				{
					pstmt.close();
					pstmt=null;
				}
				System.out.println("Tottal business_logic_check deleted ["+delCount+"]");
			}
			// end 23-dec-2020 manoharan 


			sql  = "select count(1) from BUSINESS_LOGIC_CHECK  where " +
					" tran_type = ? and sale_order = ? and cr_policy = ? and aprv_stat = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,applyTime);
			pstmt.setString(2,asSorder);
			pstmt.setString(3,lsCrPolicy);
			pstmt.setString(4,"O");
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				cnt2 = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//	System.out.println("@@@@@@@@@@@@ BUSINESS_LOGIC_CHECK cnt2["+cnt2+"]");

			//if( cnt2 == 0 )
			//Added by Pavan R on 27/OCT/17 ls 
			//added lsStatus == null on 31/OCT/17
			if(chkamt > 0 || lsStatus == null || lsStatus.trim().length() == 0 )
			{	// Pavan R End
				//Pavan R on 17sept18 Start[to bypass multiple insert in BUSINESS_LOGIC_CHECK for same policy]
				sql  = "select COUNT(1) from BUSINESS_LOGIC_CHECK " 
					+ " where TRAN_TYPE = ? and SALE_ORDER = ? and CR_POLICY = ? ";//and aprv_stat = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,applyTime);
				pstmt.setString(2,asSorder);
				pstmt.setString(3,lsCrPolicy);
				//pstmt.setString(4,"F");
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					insrtCnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(insrtCnt == 0)
				{
					//Pavan R on 17sept18 End				
					tranId = generateTranId( "T_CRCHKTRACE",siteCode, conn );
					if( tranId == null || tranId.trim().length() == 0 )
					{
						tranId = generateTranId( "GENERAL",siteCode, conn );
					}
					//	System.out.println("@@@@@@@@@@ tranId["+tranId+"]");
	
					sql = " insert into BUSINESS_LOGIC_CHECK (" +
							" TRAN_ID, TRAN_TYPE, SALE_ORDER, CR_POLICY, DESCR, APRV_STAT" +
							//		" , APRV_DATE, EMP_CODE__APRV" +
							", APRV_AMT, USED_AMT " +
							//		" , LINE_NO, OS_AMT__CUST, OS_AMT__CONT, REMARKS, AMD_NO " +
							", CUST_CODE__BIL , CUST_CODE , SITE_CODE , ITEM_SER , REF_DATE " +     //added by manish mhatre on 28-6-2019
							//" )" +//Modified by Rohini T on 26/04/2021
							" , FAILED_INFO)" +
							//" values (?,?,?,?,?,?,?,?,?,?,?,?,?" +
							" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
							//		" ,?,?,?," +
							//		" ?,?,?,?,?" +
							" ) ";
					pstmt = conn.prepareStatement(sql);
					//TRAN_ID                        NOT NULL CHAR(10)
					pstmt.setString(1,tranId);
					//TRAN_TYPE                      NOT NULL CHAR(1)
					pstmt.setString(2,applyTime);
					//SALE_ORDER                     NOT NULL CHAR(10)
					pstmt.setString(3,asSorder);
					//CR_POLICY                               CHAR(3)
					pstmt.setString(4,lsCrPolicy);
					//DESCR                                   VARCHAR2(500)
					pstmt.setString(5,lsStr);
					//APRV_STAT                               CHAR(1)
					pstmt.setString(6,"F");
					//APRV_AMT                                NUMBER(14,3)
					pstmt.setDouble(7,0);
					//USED_AMT                                NUMBER(14,3)
					pstmt.setDouble(8,0);
					
					//added by manish mhatre on 28-6-2019
					
					//CUST_CODE__BIL      
					pstmt.setString(9,custCodeBil);
					//CUST_CODE
					pstmt.setString(10, custCodeSold);
					// SITE_CODE
					pstmt.setString(11, siteCode);
					// ITEM_SER
					pstmt.setString(12, itemSer);
					//REF_DATE
					pstmt.setTimestamp(13, tranDate);
					pstmt.setString(14, jsonStr);//Modified by Rohini T on 26/04/2021
					/*pstmt.setTimestamp(13,refDateTs );*/
					System.out.println("trandate"+tranDate);
					
					//end  manish mhatre
	
					/*	//APRV_DATE                               DATE
				pstmt.setString(7,tranId);
				//EMP_CODE__APRV                          CHAR(10)
				pstmt.setString(8,tranId);
	
				//LINE_NO                                 CHAR(3)
				pstmt.setString(11,tranId);
				//OS_AMT__CUST                            NUMBER(14,3)
				pstmt.setString(12,tranId);
				//OS_AMT__CONT                            NUMBER(14,3)
				pstmt.setString(13,tranId);
				//REMARKS                                 VARCHAR2(120)
				pstmt.setString(14,tranId);
				//AMD_NO  
				pstmt.setString(15,tranId);*/
	
					pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					cnt++;
				}
				else
				{
					cnt = insrtCnt;
				}				
			}
		}
			System.out.println("@@@@@@@@@ insert cnt["+cnt+"]");
		return cnt;
	}

	private String generateTranId( String windowName,  String siteCode, Connection conn ) throws ITMException 
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			/*System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);*/

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			//System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			//System.out.println("tranId :"+tranId);
		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +selSql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
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
			}
			catch(Exception e){}
		}
		return tranId;
	}

	private String postOrder(String fromSaleOrder, String toSaleOrder,String custCodeFrom, String custCodeTo, Timestamp fromDate,Timestamp toDate, String lotSldom, String siteCodeShip, String xtraParams,
			Connection conn) throws ITMException, IOException 
			{
		Connection connCP = null;
		String errStrng="",sqlstatement="",sql1="",sql2="",sql3="",sql="",logMsg="",postLog="",itemCodeParent="",schemeCode="";
		String merrcode="",varValue="";
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null,rs5=null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null,pstmt3 = null,pstmt5=null;

		String saleOrder="",priceListDisc="",priceListClg="",unitPack="",priceList="",priceListType="",partQty="",sorderLock="",status="",orderType="";
		String lineNoOld="",itemCodeOld="",saleOrderOld = "";
		double stockQtyTot=0.0,  convQtyStduom = 0.0 ,	convFact = 0.0, qtyStk = 0.0, allocQty = 0.0,	netQty = 0.0,	balQty = 0.0;
		double totQty = 0.0,discMerge=0.0,orgQty=0.0,sordItmQty=0.0,lcQty=0.0,totStk = 0.0,siteSuppQty=0,	inputQty = 0.0,	netQuantity=0.0,lockqty = 0.0,	holdQty = 0.0 ,minShelfLife = 0.0,maxShelfLife = 0.0,convQty=0.0;
		double conv=0.0;
		String itemCode="",lineNo="",itemCodeOrd="",siteCode="",unit="",expLev="",itemRef="",unitRef="",itemFlag="",nature="" ;
		String stateCodeDlv="",countCodeDlv="";
		double totChargeQty=0.0,totFreeQty=0.0,allocFreeQty=0.0,allocChargeQty=0.0,chargeQty=0.0,plistDisc=0.0;
		double batqty=0.0,qtyper=0.0,appMinQty=0.0,appMaxQty=0.0,freeQty=0.0,totAlloc=0.0;
		int sordallocCnt=0,insCnt=0,totLines=0;
		String userId="",termId="";
		double qtyAlloc=0.0, qtyDesp=0.0,batQty=0.0,modQuantity=0.0,quantityStduom=0.0,bomQtyPer=0.0,orderQty=0.0,siteQty=0.0,siteItmQty=0.0,rate = 0.0,rateClg = 0.0;
		ArrayList quantityList = null;
		String unitStd="",ratio="",ratioOld="",orderUnit="",locGroup="",stkOpt="",salesGrp="",lotNo="",lotSl="",locCode="",grade="",siteCodeMfg="";
		Timestamp expDate=null,mfgDate=null,chkDate = null,chkDate1 = null ,chkDate2 = null;
		String invStat="",skiplot="",siteCodeSupp="",skipline="",postUpto="",custCode="",applyCust="";
		String applyCustList ="",noapplyCustList="",noApplyCust="",trackShelfLife="",rateFailed="",allocDateStr="",itemCodeScheme="",lineScheme="";
		String logFile="",errorString="";
		boolean isRejected= false,isScheme=false,isSkip=false;
		//Date allocDate = null;
		int stockOpt=0 ,graceDays =0 ,schemeCnt = 0,schemeNo=0,priceListCnt=0,loghandle=0;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon discommon = new DistCommon();
		ArrayList<SorderBean> sorderList = new ArrayList<SorderBean>();
		ArrayList<SordItemBean> sordItemList = null;
		ArrayList<StockBean> stockList = null;
		Date currentDate = new Date();
		Timestamp allocDate = null,orderDate=null,plDate=null;
		int totRecords=0,records=0;
		int saleOrderCnt=0,skiplineCnt=0;
		String tranidSoalloc="",linenoSoalloc="";
		String itemOld="",lsItem="";
		int countItem=0;
		double pendingDeallocQty=0.0,stockTot=0.0;
		int updSordAllDetCnt=0;
		String createLog=null,itemReasCode="";
		String allocRef="";// added by nandkumar Gadkari on 17/04/19
		//Pavan Rane 01nov19 start 					
		String tranIdAlc="", lineNoAlc="",siteCodeAlc="", itemCodeAlc="", locCodeAlc="", lotNoAlc="",lotSlAlc="",freeProductLoc="";
		double alloCnt = 0.0, sordAllocQty = 0.0, availStkQty = 0;					
		//Pavan Rane 01nov19 end
		try
		{
			//System.out.println("postOrder business logic starts........");
			sdf = new SimpleDateFormat(e12GenericUtility.getApplDateFormat());
			allocDate = new  Timestamp(currentDate.getTime());
			//System.out.println("allocDate :"+allocDate);
			userId=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//System.out.println("userId :"+userId);
			termId=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			System.out.println("termId :"+termId); 
			chgUser=userId;
			chgTerm=termId;
			InvAllocTraceBean invBean = new InvAllocTraceBean(); //added by nandkumar gadkari on 24/01/19
			freeProductLoc = discommon.getDisparams("999999", "FREE_PRODUCT_LOCATION", conn);//Pavan Rane 01nov19 [to get free product location]


			sqlstatement = "select sale_order, part_qty, price_list, price_list__clg,status,loc_group,cust_code ,order_type ,price_list__disc,pl_date,order_date,cust_code,site_code ,state_code__dlv, count_code__dlv " ;
			if((CommonConstants.DB_NAME).equalsIgnoreCase("mssql")) 
			{
				sqlstatement = sqlstatement +	" from sorder (updlock)" ;	
			}
			else
			{
				sqlstatement = sqlstatement +	" from sorder " ;	
			}
			//sqlstatement = sqlstatement +	" from sorder " ;	
			sqlstatement = sqlstatement +	" where sale_order >= ? " ;
			sqlstatement = sqlstatement +	" and sale_order <=  ? " ;
			sqlstatement = sqlstatement +	" and cust_code  >=  ? " ; //commented by abhijit Gaikwad //Changes Reverted by Nandkumar Gadkari As discuss with manoharan sir on 28/08/18 
			sqlstatement = sqlstatement +	" and cust_code  <=  ? " ;
			//sqlstatement = sqlstatement +	" and cust_code__bil  >=  ? " ; //commented by Nandkumar Gadkari 28/08/18 
			//sqlstatement = sqlstatement +	" and cust_code__bil  <=  ? " ;
			sqlstatement = sqlstatement +	" and due_date   >=  ? " ;
			sqlstatement = sqlstatement +	" and due_date   <=  ? " ;
			sqlstatement = sqlstatement +	" and confirmed = 'Y' and status = 'P'  " ;
			sqlstatement = sqlstatement +	" and (alloc_flag <> 'Y' or alloc_flag is null) " ;
			sqlstatement = sqlstatement +	" and site_code__ship =  ? " ; 
			if((CommonConstants.DB_NAME).equalsIgnoreCase("mssql")) 
			{
				sqlstatement = sqlstatement + " order by due_date, sale_order"; 
			}
			else if((CommonConstants.DB_NAME).equalsIgnoreCase("db2"))
			{
				sqlstatement = sqlstatement + " order by due_date, sale_order for update"; 	
			}
			else if((CommonConstants.DB_NAME).equalsIgnoreCase("mysql")) 
			{
				sqlstatement = sqlstatement + " order by due_date, sale_order for update";	
			}
			else 
			{
				//sqlstatement = sqlstatement + " order by due_date, sale_order for update nowait";
				sqlstatement = sqlstatement + " order by due_date, sale_order for update nowait";
			}

			pstmt = conn.prepareStatement(sqlstatement);			
			pstmt.setString(1,fromSaleOrder);
			pstmt.setString(2,toSaleOrder);	
			pstmt.setString(3,custCodeFrom);	
			pstmt.setString(4,custCodeTo);	
			pstmt.setTimestamp(5,fromDate);	
			pstmt.setTimestamp(6,toDate);	
			pstmt.setString(7,siteCodeShip);	
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				saleOrderCnt++;
				skipline="";
				saleOrder = checkNull(rs.getString("sale_order"));
				//System.out.println("saleOrder :"+saleOrder);
				partQty = checkNull(rs.getString("part_qty"));
				//System.out.println("partQty :"+partQty);
				priceList = checkNull(rs.getString("price_list"));
				//System.out.println("priceList :"+priceList);
				priceListClg = checkNull(rs.getString("price_list__clg"));
				//System.out.println("priceListClg :"+priceListClg);
				status =  checkNull(rs.getString("status"));
				//System.out.println("status :"+status);
				locGroup =  checkNull(rs.getString("loc_group"));
				//System.out.println("locGroup :"+locGroup);
				custCode = checkNull(rs.getString("cust_code"));
				orderType = checkNull(rs.getString("order_type"));
				priceListDisc = checkNull(rs.getString("price_list__disc"));
				plDate = rs.getTimestamp("pl_date");
				orderDate = rs.getTimestamp("order_date");
				siteCode = checkNull(rs.getString("site_code"));
				stateCodeDlv = checkNull(rs.getString("state_code__dlv"));
				countCodeDlv = checkNull(rs.getString("count_code__dlv"));



				SorderBean sorderBean = new SorderBean();
				sorderBean.setSaleOrder(saleOrder);
				sorderBean.setPartQty(partQty);
				sorderBean.setPriceList(priceList);
				sorderBean.setPriceListClg(priceListClg);
				sorderBean.setStatus(status);
				sorderBean.setLocGroup(locGroup);
				sorderBean.setCustCode(custCode);
				sorderBean.setOrderType(orderType);
				sorderBean.setPlDate(plDate);
				sorderBean.setPriceListDisc(priceListDisc);
				sorderBean.setOrderdate(orderDate);
				sorderBean.setSiteCode(siteCode);
				sorderBean.setStateCodeDlv(stateCodeDlv);
				sorderBean.setCountCodeDlv(countCodeDlv);
				sorderList.add(sorderBean);

				System.out.println(" after sorderbean sorderList"+sorderList+"]");

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("saleOrderCnt"+saleOrderCnt);

			/*-----Start Iterator for SorderItem against sale order----------------*/


			/*Iterator sordIterator = sorderList.iterator();
			while(sordIterator.hasNext())
			{*/
			System.out.println("sorderList :"+sorderList.size());
			//sordItemList= new ArrayList<SordItemBean>();arun p
			int sorderlistcnt=0;

			for(SorderBean sorderBean : sorderList)
			{

				System.out.println(" inside  sorderbean sorderList >>>>>>>>>:"+sorderList.size());
				sorderlistcnt++;
				sordItemList= new ArrayList<SordItemBean>();
				//Added by Pavan R on 28/12/17 start 
				records = 0;
				//Added by Pavan R on 28/12/17 end
				/*	SorderBean sorderBean = new SorderBean();
				sorderBean = (SorderBean) sordIterator.next();*/
				System.out.println("sale order :"+sorderBean.getSaleOrder());

				saleOrder = sorderBean.getSaleOrder();

				status = sorderBean.getStatus();
				locGroup=sorderBean.getLocGroup();
				System.out.println("status :"+status);
				if(!salesOrderListAll.contains(saleOrder))
				{
					salesOrderListAll.add(saleOrder);
				}
				createLog = itmDBAccessEJB.getEnvDis("999999", "CREATE_POST_LOG_FILE", conn);
				//System.out.println("varValue :"+varValue);
				//ls_varvalue = gf_getenv_dis('999999','CREATE_POST_LOG_FILE')
				if( "NULLFOUND".equalsIgnoreCase(createLog) )
				{
					createLog ="N";
				}
				if("Y".equalsIgnoreCase(createLog) )
				{
					//logFile="c:\\appl\\itm26\\" + fromSaleOrder.trim().toLowerCase()+ "_post.log";
					logFileInit=intializingLog("post_order",saleOrder);
					strToWrite="";
					//strToWriteHead="Series :"+seriesArr[serNo]+" Tax Code:"+taxCode+" Site Code:"+siteCode+" Tran Date From:"+tranDateFrm+" Tran Date To:"+tranDateTo+" Voucher Date:"+tranDateFrm+" Voucher Due on:"+tranDateTo+"\n";
					//strToWriteHead="SalesOrder\tItem Code\tlocation code\tLotNo\tline_no\tMessage\r\n";
					//strToWriteHead=strToWriteHead+"========\t========\t=========\t============\t==============\t=================\r\n";
					strToWrite=strToWrite + strToWriteHead;
					//	strToWrite=strToWrite + strToWriteHead;
					// loghandle = fileopen (ls_logfile , LineMode!, Write!, LockWrite!, Append!)
					//filewrite(li_loghandle,"SalesOrder~tItem Code~tlocation code~tLotNo~tmline_no~tMessage")
				}



				if("C".equalsIgnoreCase(status))
				{
					//merrcode = 'VTLOCK1'
					//merrcode = this.event trigger ue_post_log('LxmlStringock Err', 'Cannot put lock on Sale Order : ' + string(msaleorder), 'sorditem',msaleorder, '', '', ' ', ' ', msaleorder, 'S-DSP','REAS_CODE','W_POST_ORDER') //atul 21.02.02				
					errorLog("Lock Err", "VTLOCK1:Cannot put lock on Sale Order : " + saleOrder, "sorditem",saleOrder, "", "", " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
					break;	
				}

				sql = " select si.line_no, si.item_code, " +
						" (si.quantity - case when si.qty_desp is null then 0 else si.qty_desp end) as sordItmQty ," +
						" si.item_code__ord, si.site_code, si.unit, si.status, si.exp_lev, si.item_ref," +
						" si.min_shelf_life,si.max_shelf_life, si.unit__ref, si.item_flag, si.status,si.qty_alloc," +
						" case when si.qty_desp is null then 0 else si.qty_desp end," +
						" si.nature  ,sdet.quantity__stduom,sdet.unit__std , sdet.conv__qty_stduom, " +
						" (case when sdet.rate is null then 0 else sdet.rate end ) rate ," +
						" (case when sdet.rate__clg is null then 0 else sdet.rate__clg end ) rate__clg" +
						" from sorditem si , sorddet sdet  where si.sale_order = sdet.sale_order and " +
						" si.line_no = sdet.line_no and si.sale_order  = ?  	" +
						" and  si.line_type = 'I' and (si.quantity - case when si.qty_desp is null" +
						" then 0 else si.qty_desp end) > 0  order by si.line_no, si.exp_lev " ;
				pstmt = conn.prepareStatement(sql);			
				pstmt.setString(1,saleOrder);
				rs = pstmt.executeQuery();
				int sorditmlistcnt=0;
				while(rs.next())
				{
					sorditmlistcnt++;
					//lockqty=0.0;


					SordItemBean sordItemBean = new SordItemBean();
					//SorderBean sorderBean2 = new SorderBean();
					totLines++;
					lineNo = checkNull(rs.getString("line_no"));
					System.out.println("lineNo :"+lineNo);
					expLev = checkNull(rs.getString("exp_lev"));
					itemCode = checkNull(rs.getString("item_code"));
					System.out.println("itemCode :"+itemCode);
					itemFlag = checkNull(rs.getString("item_flag"));
					minShelfLife = rs.getDouble("min_shelf_life");
					maxShelfLife = rs.getDouble("max_shelf_life");
					nature = checkNull(rs.getString("nature"));
					siteCode = checkNull(rs.getString("site_code"));
					itemCodeOrd = checkNull(rs.getString("item_code__ord"));
					status = checkNull(rs.getString("status"));
					unit = checkNull(rs.getString("unit"));
					unitStd = checkNull(rs.getString("unit__std"));
					unitRef = checkNull(rs.getString("unit__ref"));
					rate = rs.getDouble("rate");
					rateClg = rs.getDouble("rate__clg");

					convQtyStduom = rs.getDouble("conv__qty_stduom");
					sordItmQty = rs.getDouble("sordItmQty");
					//System.out.println("sordItmQty :"+sordItmQty);
					quantityStduom = rs.getDouble("quantity__stduom");
					qtyAlloc = rs.getDouble("qty_alloc");
					//qtyDesp = rs.getDouble("qty_desp");
					nature = checkNull(rs.getString("nature"));

					//Start Added by chandrashekar on 26-sep-2016
					if("B".equalsIgnoreCase(itemFlag) && "F".equalsIgnoreCase(nature))
					{
						sql = "select item_code__parent , unit   from item where item_code = ?";
						pstmt3 =  conn.prepareStatement(sql);
						pstmt3.setString(1,itemCode);
						rs3 = pstmt3.executeQuery();
						if(rs3.next())
						{
							itemCodeParent =  rs3.getString("item_code__parent");
							unitStd =  rs3.getString("unit");
						}
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;
						sordItemBean.setUnitStd(unitStd);
						sordItemBean.setUnit(unitStd);
					}else
					{
						sordItemBean.setUnitStd(unitStd);
						sordItemBean.setUnit(unit);
					}
					//End Added by chandrashekar on 26-sep-2016
					sordItemBean.setSaleOrder(saleOrder);
					System.out.println(" before sorditemlist sale order["+saleOrder+"]");
					sordItemBean.setLineNo(lineNo);
					sordItemBean.setConvQtyStduom(convQtyStduom);
					sordItemBean.setExpLev(expLev);
					sordItemBean.setItemCode(itemCode);
					sordItemBean.setItemFlag(itemFlag);
					sordItemBean.setMinShelfLife(minShelfLife);
					sordItemBean.setMaxShelfLife(maxShelfLife);
					sordItemBean.setNature(nature);
					sordItemBean.setUnitRef(unitRef);
					sordItemBean.setQtyAlloc(rs.getDouble("qty_alloc"));
					sordItemBean.setSiteCode(siteCode);
					//sordItemBean.setQtyDesp(qtyDesp);
					sordItemBean.setItemCodeOrd(itemCodeOrd);
					if(!(unit.equalsIgnoreCase(unitStd)))//Added by chandrashekar on 21-sep-2016
					{
						sordItemBean.setQuantity(sordItmQty);
						sordItmQty=sordItmQty*convQtyStduom;
						sordItemBean.setSordItmQty(sordItmQty);

					}else
					{
						sordItemBean.setQuantity(sordItmQty);
						sordItemBean.setSordItmQty(sordItmQty);
					}
					sordItemBean.setQuantityStduom(quantityStduom);
					sordItemBean.setStatus(status);
					sordItemBean.setRate(rate); //rate value set and removed  rateClg by nandkumar gadkari on 15/11/19 
					sordItemBean.setRateClg(rateClg);
					sordItemBean.setNature(nature);
					sordItemBean.setSorderBean(sorderBean);
					//	sordItemList.add(sordItemBean);


					if(!(unit.equalsIgnoreCase(unitStd)))
					{
						System.out.println("unit and unitStd are not equals!!");					
						sordItmQty=dist.convQtyFactor(unit,unitStd,itemCode, convQtyStduom, conn);
						System.out.println("sordItmQty@@@@ :"+sordItmQty);
						//if(sordItmQty == -999999999)
						if(sordItmQty == 0)
						{
							System.out.println("Unable to convert quantity");
							logMsg="Unable to convert quantity";
							//strToWriteHead=strToWriteHead+saleOrder+"\t"+itemCode+"\t"+logMsg+"\t\r\n";
							strToWriteHead = createPostLog(saleOrder, itemCode, "", "", "", logMsg);
							strToWrite=strToWrite + strToWriteHead;
							//strToWrite=strToWrite + errorString;													
							break;

						}
						else
						{
							orderUnit = unit;
							unit = unitStd;
							//sordItemBean.setUnit(unit);
						}


					}
					else
					{
						orderUnit = unit;
						//sordItemBean.setUnit(unit);
					}


					System.out.println("sordItmQty>>"+sordItmQty);
					sql = " select sum(quantity - case when qty_desp is null then 0 else qty_desp end) " +
							"as siteItmQty from sorditem	where sale_order =  ? and " +
							" item_code = ? and (line_no <  ?  or (line_no = ? and exp_lev < ?))";
					pstmt2 =  conn.prepareStatement(sql);
					pstmt2.setString(1,saleOrder);
					pstmt2.setString(2,itemCode);
					pstmt2.setString(3,lineNo);
					pstmt2.setString(4,lineNo);
					pstmt2.setString(5,expLev);
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						siteItmQty =  rs2.getDouble("siteItmQty");
						sordItemBean.setSiteItmQty(siteItmQty);

					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;



					sql = "select stk_opt,item_code__parent from item where item_code = ? ";
					pstmt3 =  conn.prepareStatement(sql);
					pstmt3.setString(1,itemCode);
					rs3 = pstmt3.executeQuery();
					if(rs3.next())
					{
						stockOpt =  rs3.getInt("stk_opt");
						itemCodeParent =  rs3.getString("item_code__parent");
						sordItemBean.setStockOpt(stockOpt);
						sordItemBean.setItemCodeParent(itemCodeParent);
					}
					rs3.close();
					rs3 = null;
					pstmt3.close();
					pstmt3 = null;

					sordItemList.add(sordItemBean);

				}
				System.out.println("sorditmlistcnt"+sorditmlistcnt);
				sorderBean.setSorditemList(sordItemList);
				//sordItemList.clear();
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//				}//End of iterator sordIterator commented by arun 
				System.out.println("sorderlistcnt"+sorderlistcnt);

				int stockTestCnt=0,forTestCnt=0;
				System.out.println("sordItemList size :"+sordItemList.size());
				for(SordItemBean sordItemBean :sordItemList )
				{
					//sordItemBean.setSaleOrder(saleOrder);
					System.out.println("sale order :"+sordItemBean.getSorderBean().getSaleOrder());
					System.out.println("sale order :"+sordItemBean.getSaleOrder());
					forTestCnt++;
					stockList=new ArrayList<StockBean>();
					itemCode = sordItemBean.getItemCode();
					System.out.println("itemCode :"+itemCode);
					siteCode = sordItemBean.getSiteCode();
					System.out.println("siteCode :"+siteCode);
					locGroup= sordItemBean.getSorderBean().getLocGroup();
					stockOpt=sordItemBean.getStockOpt();
					saleOrder = sordItemBean.getSorderBean().getSaleOrder();
					System.out.println("saleOrder :"+saleOrder);
					lineNo=sordItemBean.getLineNo();
					System.out.println("lineNo "+lineNo);
					expLev = sordItemBean.getExpLev();
					System.out.println("expLev "+expLev);
					stockOpt = sordItemBean.getStockOpt();
					System.out.println("stockOpt "+stockOpt);
					sordItmQty = sordItemBean.getSordItmQty();
					System.out.println("sordItmQty "+sordItmQty);
					itemCodeOrd = sordItemBean.getItemCodeOrd();
					System.out.println("itemCodeOrd "+itemCodeOrd);
					itemFlag = sordItemBean.getItemFlag();
					System.out.println("itemFlag "+itemFlag);
					rate =  sordItemBean.getRate();
					System.out.println("rate "+rate);
					rateClg = sordItemBean.getRateClg();
					System.out.println("rateClg "+rateClg);
					siteItmQty = sordItemBean.getSiteItmQty();
					System.out.println("siteItmQty "+siteItmQty);

					priceListDisc = sordItemBean.getSorderBean().getPriceListDisc();
					System.out.println("priceListDisc "+priceListDisc);
					custCode = sordItemBean.getSorderBean().getCustCode();
					System.out.println("custCode "+custCode);
					orderDate = sordItemBean.getSorderBean().getOrderdate();
					System.out.println("orderDate "+orderDate);
					priceListDisc = sordItemBean.getSorderBean().getPriceListDisc();
					System.out.println("priceListDisc "+priceListDisc);
					plDate =  sordItemBean.getSorderBean().getPlDate();
					System.out.println("plDate "+plDate);
					minShelfLife = sordItemBean.getMinShelfLife();
					System.out.println("minShelfLife "+minShelfLife);
					maxShelfLife = sordItemBean.getMaxShelfLife();
					System.out.println("maxShelfLife "+maxShelfLife);
					quantityStduom = sordItemBean.getQuantityStduom();
					System.out.println("quantityStduom "+quantityStduom);
					orderType = sordItemBean.getSorderBean().getOrderType();
					System.out.println("orderType "+orderType);
					partQty = sordItemBean.getSorderBean().getPartQty();
					System.out.println("partQty "+partQty);
					nature = sordItemBean.getNature();
					System.out.println("nature::: "+nature);
					itemCodeParent=sordItemBean.getItemCodeParent();
					System.out.println("itemCodeParent:::["+itemCodeParent+"]");
					unit = sordItemBean.getUnit();
					unitStd = sordItemBean.getUnitStd();
					System.out.println("@@@unit["+unit+"]");
					System.out.println("@@@unitStd["+unitStd+"]");
					
					int stockcnt=0;
					logMsg="";//Added by nandkumar gadkari on 02/05/19
				if(stockOpt != 0)// Condition  Added by Nandkumar Gadkari on 27/06/18 for  Non Inventory Item.
				{				
					//Pavan Rane 01nov19 start [to deallocate stockist stock chargable/free product as per sord_alloc]					
					tranIdAlc=""; lineNoAlc=""; siteCodeAlc=""; itemCodeAlc=""; locCodeAlc=""; lotNoAlc="";lotSlAlc="";
					alloCnt = 0.0; sordAllocQty = 0.0; availStkQty = 0;					
					sql  = "select count(*) from sord_alloc a, sord_alloc_det b where "
						+ " a.tran_id = b.tran_id"
						+ " and a.cust_code = ?"
						+ " and B.item_code = ?"
						+ " and A.site_code= ? "
						+ " and b.sale_order is null"
						+ " and b.quantity - b.dealloc_qty > 0";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					pstmt.setString(2,itemCode);
					pstmt.setString(3,siteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						alloCnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(alloCnt > 0)
					{
						sql  = "select a.tran_id, b.line_no, b.site_code, b.item_code, b.loc_code, b.lot_no, b.lot_sl,b.quantity - b.dealloc_qty as quantity" 
								+ " from sord_alloc a, sord_alloc_det b where "
								+ " a.tran_id = b.tran_id"
								+ " and a.cust_code = ?"
								+ " and B.item_code = ?"
								+ " and b.site_code = ? ";
						if(!"C".equalsIgnoreCase(nature)) //for nature !C to pick up stock as per FreePorductLoc loc_code(partial case).
						{
							sql = sql + " and b.loc_code = ? ";	
						}else {
							sql = sql + " and b.loc_code <> ? "; 
						}
						sql = sql + " and b.sale_order is null";
						sql = sql + " and b.quantity - b.dealloc_qty > 0";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						pstmt.setString(2,itemCode);
						pstmt.setString(3,siteCode);
						pstmt.setString(4,freeProductLoc);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranIdAlc = rs.getString("tran_id");
							lineNoAlc = rs.getString("line_no");
							siteCodeAlc = rs.getString("site_code");
							itemCodeAlc = rs.getString("item_code");
							locCodeAlc = rs.getString("loc_code");
							lotNoAlc = rs.getString("lot_no");
							lotSlAlc = rs.getString("lot_sl");	
							sordAllocQty = rs.getDouble("quantity");
							HashMap invAllocTraceMap = new HashMap();	
							if(sordAllocQty > 0)
							{
								//System.out.println("before invTrace["+sordAllocQty+"] sordItmQty["+sordItmQty+"]");
								if(sordAllocQty > sordItmQty) 	//to deallocate qty as per sorditem qty(if partial quantity)
								{
									sordAllocQty = sordItmQty;
								}
								System.out.println("after invTrace["+sordAllocQty+"] sordItmQty["+sordItmQty+"]");
								invAllocTraceMap.put("ref_ser","S-ALC");
								invAllocTraceMap.put("ref_id",tranIdAlc);
								invAllocTraceMap.put("ref_line",lineNoAlc);
								invAllocTraceMap.put("site_code",siteCodeAlc);
								invAllocTraceMap.put("item_code",itemCodeAlc);
								invAllocTraceMap.put("loc_code",locCodeAlc);
								invAllocTraceMap.put("lot_no",lotNoAlc);
								invAllocTraceMap.put("lot_sl",lotSlAlc);
								invAllocTraceMap.put("alloc_qty",sordAllocQty *-1);
								invAllocTraceMap.put("chg_user",userId);
								invAllocTraceMap.put("chg_term",termId);
								invAllocTraceMap.put("chg_win","W_SORDALLOC");																			
								logMsg= tranIdAlc +" "+lineNoAlc + " "+"De-Allocation of stock from PostOrderProcess";
								invAllocTraceMap.put("alloc_ref",logMsg);										
								merrcode = invBean.updateInvallocTrace(invAllocTraceMap,conn);
								if(merrcode != null && merrcode.trim().length() > 0)
								{
									merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
									return merrcode;
								}
								else
								{										
									sql = "select alloc_ref from stock where item_code= ? and site_code= ? and lot_no= ? and loc_code=? and lot_sl=? ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1,itemCodeAlc);
									pstmt2.setString(2,siteCodeAlc);
									pstmt2.setString(3,lotNoAlc);
									pstmt2.setString(4,locCodeAlc);
									pstmt2.setString(5,lotSlAlc);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										allocRef = rs2.getString(1);
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;
									System.out.println("allocRef["+allocRef+"]");
									System.out.println("allocReflog["+logMsg+"]");
									if(!allocRef.equalsIgnoreCase(logMsg))
									{
										merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
										return merrcode;
									}																				
									logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo +"\t"+"Inserted data in INVALLOC_TRACE";										
									//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, logMsg); commented by nandkumar gadkari on 24/12/19
									
									// To get stockist stock detail to update on stockbeans for sordalloc table
									sql = " select a.lot_no, a.lot_sl, a.quantity , a.alloc_qty,a.hold_qty,  ";
									sql = sql + " a.exp_date, a.grade, a.mfg_date, a.site_code__mfg, a.loc_code, a.hold_qty ";
									sql = sql + "from stock a, invstat b, location c ";
									sql = sql + " where c.inv_stat = b.inv_stat";
									sql = sql + " and c.loc_code = a.loc_code "; 						
									sql = sql + " and a.loc_code = ? ";
									sql = sql + " and a.lot_no = ? ";	
									sql = sql + " and a.item_code = ? ";
									sql = sql + " and a.site_code = ? ";
									sql = sql + " and a.quantity - (case when a.alloc_qty is null then 0 else a.alloc_qty end ) > 0 ";
									sql = sql + " and b.available = 'Y' ";
									if(lotSldom.trim().length() > 0) {
										sql = sql + " and a.lot_sl = '"+lotSldom.trim()+"'";
									}
									if(locGroup.trim().length() > 0) {
										sql = sql + " and c.loc_group = '"+locGroup.trim()+"'";
									}
									sql = sql + " order by a.partial_used, a.exp_date, a.lot_no, a.lot_sl ";
									pstmt5 =  conn.prepareStatement(sql);
									pstmt5.setString(1,locCodeAlc);
									pstmt5.setString(2,lotNoAlc);
									pstmt5.setString(3,itemCode);
									pstmt5.setString(4,siteCode);
									rs5 = pstmt5.executeQuery();
									if(rs5.next())
									{													
										availStkQty = rs5.getDouble("quantity") - rs5.getDouble("alloc_qty");
										StockBean stockBean = new StockBean();
										stockBean.setLotNo(rs5.getString("lot_no"));
										stockBean.setLotSl(rs5.getString("lot_sl"));
										stockBean.setStockQty(rs5.getDouble("quantity"));//stockBean.setStockQty(sordAllocQty);
										stockBean.setAllocQty(rs5.getDouble("alloc_qty"));
										stockBean.setHoldQty(rs5.getDouble("hold_qty"));
										stockBean.setExpDate(rs5.getTimestamp("exp_date"));
										stockBean.setGrade(rs5.getString("grade"));
										stockBean.setMfgDate(rs5.getTimestamp("mfg_date"));
										stockBean.setSiteCodeMfg(rs5.getString("site_code__mfg"));
										stockBean.setLocCode(rs5.getString("loc_code"));
										stockBean.setStockQtyTot(availStkQty);
										stockBean.setSordItemBean(sordItemBean);
										stockList.add(stockBean);
									}									
									if(tranIdAlc != null && tranIdAlc.trim().length() > 0)
									{
										if(sordAllocQty > 0)
										{											
											sql = " update sord_alloc_det set dealloc_qty = dealloc_qty + ? where tran_id = ?  and line_no = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setDouble(1,sordAllocQty);
											pstmt2.setString(2,tranIdAlc);
											pstmt2.setString(3,lineNoAlc);
											pstmt2.executeUpdate();
											pstmt2.close();
											pstmt2 = null;
										}										
									}
								}								
							}//end of if(sordAllocQty > 0)
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}//if(alloCnt > 0)	
					System.out.println("before stock loop::> alloCnt["+alloCnt+"] sordItmQty["+sordItmQty+"] sordAllocQty["+sordAllocQty+"]");
					if(sordItmQty > sordAllocQty) 	 //If allocated stock qty is not sufficient then check stock for remaining qty]
					{
						//Pavan Rane 01nov19 End [to deallocate stockist stock chargable/free product]
						sql = " select sum(a.quantity - case when a.alloc_qty is null then 0 else a.alloc_qty end - case when a.hold_qty is null then 0 else a.hold_qty end ) as quantity   ";
						sql = sql + "from stock a, invstat b, location c ";
						sql = sql + " where c.inv_stat = b.inv_stat";
						sql = sql + " and c.loc_code = a.loc_code ";
						sql = sql + " and a.item_code = ? ";
						sql = sql + " and a.site_code = ? ";
						sql = sql + " and a.quantity - (case when a.alloc_qty is null then 0 else a.alloc_qty end ) > 0 ";// ADDED BY NANDKUMAR GADKARI 
						sql = sql + " and b.available = 'Y' ";
	
						if(lotSldom.trim().length() > 0)
						{
							sql = sql + " and a.lot_sl = '"+lotSldom.trim()+"'";
						}
	
						if(locGroup.trim().length() > 0)
						{
							sql = sql + " and c.loc_group = '"+locGroup.trim()+"'";
						}
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stockQtyTot = rs.getDouble("quantity");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;							
						System.out.println("stockQtyTot >>"+stockQtyTot);
						
						//Pavan Rane 01nov19 start [If allocated stock qty is not sufficient then check stock for remaining qty]
						if(alloCnt > 0 && sordItmQty > sordAllocQty) //if(alloCnt > 0 && sordAllocQty <=  0)
						{						
							stockQtyTot = stockQtyTot - sordAllocQty;	
							sordItmQty = sordItmQty - sordAllocQty;
							System.out.println("2662:: after stockQtyTot["+stockQtyTot+"] sordItmQty["+sordItmQty+"] sordAllocQty["+sordAllocQty+"]");												
						}//Pavan Rane 01nov19 end [If allocated stock qty is not sufficient then check stock for remaining qty]
												
						//double allocQty1=0.0; 
						sql = " select a.lot_no, a.lot_sl, a.quantity , a.alloc_qty,a.hold_qty,  ";
						sql = sql + " a.exp_date, a.grade, a.mfg_date, a.site_code__mfg, a.loc_code, a.hold_qty ";
						//sql = sql + "from stock a, invstat b, location c";
						sql = sql + "from stock a, invstat b, location c ";
						sql = sql + " where c.inv_stat = b.inv_stat";
						sql = sql + " and c.loc_code = a.loc_code ";
						sql = sql + " and a.item_code = ? ";
						sql = sql + " and a.site_code = ? ";
						sql = sql + " and a.quantity - (case when a.alloc_qty is null then 0 else a.alloc_qty end ) > 0 ";//ADDED BY NANDKUMAR GADKARI
						sql = sql + " and b.available = 'Y' ";
	
						if(lotSldom.trim().length() > 0)
						{
							sql = sql + " and a.lot_sl = '"+lotSldom.trim()+"'";
						}
	
						if(locGroup.trim().length() > 0)
						{
							sql = sql + " and c.loc_group = '"+locGroup.trim()+"'";
						}
						sql = sql + " order by a.partial_used, a.exp_date, a.lot_no, a.lot_sl ";
						pstmt5 =  conn.prepareStatement(sql);
						pstmt5.setString(1,itemCode);
						pstmt5.setString(2,siteCode);
						rs5 = pstmt5.executeQuery();					
						//int stockcnt=0; // commented  by Nandkumar Gadkari on 27/06/18 
						//added lockqty by arun 
						stockTot=0.0;
						lockqty=0.0;
						//added lockqty by arun 04-12-17
						rateFailed = "F";	
						while(rs5.next())
						{
							stockcnt++;
							//Pavan R 03jul19 start[to reinitialise the variables reset previous value for postlog]
							lotNo = "";
							lotSl = "";
							locCode =  "";
							//Pavan R 03jul19 end
							isRejected=false;
							allocQty=0;
							qtyStk=0;
							holdQty=0;
							System.out.println("Inside d while loop!!!!!!!!!!!.....");
							stockTestCnt++;
							lotNo =  rs5.getString("lot_no");
							lotSl =  rs5.getString("lot_sl");
							qtyStk =  rs5.getDouble("quantity");
							allocQty =  rs5.getDouble("alloc_qty");
							System.out.println("allocQty >>>>>>>>>>>>>>"+allocQty);
							holdQty =  rs5.getDouble("hold_qty");
							expDate =  rs5.getTimestamp("exp_date");
							System.out.println("expDate:"+expDate);
							grade =  rs5.getString("grade");
							mfgDate =  rs5.getTimestamp("mfg_date");
							siteCodeMfg =  rs5.getString("site_code__mfg");
							locCode =  rs5.getString("loc_code");
	
							System.out.println("lotNo["+lotNo+"]"+"lotSl["+lotSl+"]");
							System.out.println("qtyStk["+qtyStk+"]"+"allocQty["+allocQty+"]");
							System.out.println("holdQty["+holdQty+"]"+"locCode["+locCode+"]");
	
	
							//stockQtyTot = stockQtyTot + (qtyStk - allocQty - holdQty);
	
	
							StockBean stockBean = new StockBean();
							stockBean.setLotNo(lotNo);
	
							stockBean.setLotSl(lotSl);
							//stockBean.setQuantity(stockQty);
							stockBean.setStockQty(qtyStk);
							stockBean.setAllocQty(allocQty);
							stockBean.setHoldQty(holdQty);
							stockBean.setExpDate(expDate);
							stockBean.setGrade(grade);
							stockBean.setMfgDate(mfgDate);
							stockBean.setSiteCodeMfg(siteCodeMfg);
							stockBean.setLocCode(locCode);
							//	stockBean.setStockQty(stockQtyTot);
							stockBean.setStockQtyTot(stockQtyTot);
							stockBean.setSordItemBean(sordItemBean);
							//stockList.add(stockBean);
							System.out.println("After while setSordItemBean  ["+stockBean+"]");
							
							/*}
					rs5.close();
					rs5 = null;
					pstmt5.close();
					pstmt5 = null;
							 */
							System.out.println("stockTestCnt :"+stockTestCnt);
							System.out.println("stockQtyTot :"+stockQtyTot);
	
	
							/*if(stockOpt != 0)
							{*/// Condition  commented  by Nandkumar Gadkari on 27/06/18 for  Non Inventory Item.
								//1.Check Stock Quantity Avaibality 
								System.out.println("stockQtyTot["+stockQtyTot+"]"+"sordItmQty["+sordItmQty+"]");
								if(stockQtyTot < sordItmQty)
								{
									//isRejected=true;
									//stockBean.setRejected(isRejected);
	
									stockQtyTot=0.0;
	
	
									sql = "select site_code__supp  from siteitem where " +
											" site_code = ? and item_code = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,siteCode);
									pstmt.setString(2,itemCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										siteCodeSupp = checkNull(rs.getString("site_code__supp"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
	
									if(siteCodeSupp.trim().length() == 0)
									{
										sql = "select site_code__supp from " +
												" site where site_code = ? ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1,siteCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											siteCodeSupp = checkNull(rs.getString("site_code__supp"));
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
	
	
									if(siteCodeSupp.trim().length() > 0)
									{
	
										sql = " select sum(a.quantity - case when a.alloc_qty is null " +
												" then 0 else a.alloc_qty end - case when a.hold_qty is null " +
												" then 0 else a.hold_qty end ) siteSuppQty " +
												" from stock a, invstat b where a.inv_stat = b.inv_stat " +
												" and a.item_code = ? and " +
												" a.site_code = ? and a.quantity   > 0	" +
												" and b.available = 'Y' ";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1,itemCode);
										pstmt.setString(2,siteCodeSupp);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											siteSuppQty = rs.getDouble("siteSuppQty");
											stockQtyTot=siteSuppQty;
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
	
										if(stockQtyTot >= sordItmQty)
										{
											System.out.println("Stock found in other sites for Sale order :" + saleOrder +",Item Code : "+ itemCode +",line No:"+ lineNo);
											isRejected=true;
											stockBean.setRejected(isRejected);
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tStock found in other sites";
											//strToWrite=strToWrite+logMsg+"\t\r\n";
											strToWrite= strToWrite + createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Stock found in other sites");
											postLog = "Stock found in other sites for Sale order :" + saleOrder +",Item Code : "+ itemCode +",line No:"+ lineNo;
											errorLog("P01", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
										}
	
	
									}
									//commented by kunal on 31/8/18 for scheme item skipped without error for no stock
									//break;
									
									System.out.println("commented break");
								}//	if(stockQty < sordItmQty)
	
								//2.Check Scheme Avaibality
	
								if("B".equalsIgnoreCase(itemFlag.trim()))
								{
	
									System.out.println("itemCodeOrd>>>>@@@["+itemCodeOrd+"]");
									sql = " select grace_days from scheme_applicability " +
											" where scheme_code = ? and app_from <= ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCodeOrd);
									pstmt.setTimestamp(2,allocDate);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										graceDays = rs.getInt("grace_days");
									}
									System.out.println("GraceDays:::"+graceDays);
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									if(graceDays > 0)
									{
										graceDays = -1 * graceDays;
									}
									System.out.println("if >0 GraceDays:::"+graceDays);
	
									allocDate = utilmethod.RelativeDate(allocDate, graceDays);
									System.out.println("allocDate:::"+allocDate);
									System.out.println("allocDate >>>>>"+allocDate);
	
									sql = " select count(*) as count from scheme_applicability where" +
											" scheme_code = ? and app_from <= ? " +
											" and valid_upto >= ?	";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCodeOrd);
									pstmt.setTimestamp(2,allocDate);
									pstmt.setTimestamp(3,allocDate);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										schemeCnt = rs.getInt(1);
									}
	
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("schemeCnt:::"+schemeCnt);
	
	
									if(schemeCnt >1)
									{
										System.out.println("Scheme details not found for line no :"+lineNo);
										System.out.println("scheme count break:::::");
										isRejected=true;
										stockBean.setRejected(isRejected);
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tScheme details not found ";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										//strToWrite=strToWrite + createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme details not found ");
										strToWrite=strToWrite + createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Multiple scheme define in scheme applicability for scheme item");// added by nandkumar gadkari on 24/12/19
										errorLog("Error", "Scheme details not found for line no : " + lineNo,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
										//continue;
										//break;
										continue;//added by nandkumar gadkari and commented break on 08/05/19
									}
									if(schemeCnt == 0)
									{
										System.out.println("scheme count continue::::");
										skipline = skipline + "'"+lineNo;
										//setDate = datetime(today(),now())
										//System.out.println("Scheme Not Applicable Due To Scheme Validity Period");
										isRejected=true;
										stockBean.setRejected(isRejected);
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tScheme Not Applicable Due To Scheme Validity Period";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										strToWrite=strToWrite + createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme Not Applicable Due To Scheme Validity Period");									
										postLog="Scheme Not Applicable Due To Scheme Validity Period For " + saleOrder +" "+ itemCodeOrd +" "+ itemCode +" "+ lineNo;
										errorLog("P02", postLog ,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
										schemeNo++;
										continue;
									}
									else
									{
										varValue = dist.getDisparams("999999", "SCHEME_PART_DESP", conn);
	
										if("NULLFOUND".equalsIgnoreCase(varValue))
										{
											isScheme = false;
										}
										else if("Y".equalsIgnoreCase(varValue))
										{
											isScheme = true;
										}
										else
										{
											isScheme = false;
										}
	
									}
									//Start added by chandrashekar on 03-aug-2016
									countItem=0;
									if(isScheme)
									{   
										sql = " select  count(*) as count from SORDITEM where SALE_ORDER = ? " +
												" and LINE_TYPE = 'I' AND line_no = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,saleOrder);
										pstmt.setString(2,lineNo);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											countItem  = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										System.out.println("countItem:::"+countItem);
										if(countItem>1)
										{
											itemOld="";
											sql = " select item_code from sorditem where sale_order  = ? " +
													"and line_type = 'I' and line_no = ? order by item_code ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,saleOrder);
											pstmt.setString(2,lineNo);
											rs = pstmt.executeQuery();
											while(rs.next())
											{
												lsItem = checkNull(rs.getString("item_code"));
												System.out.println("lsItem"+lsItem+"itemOld"+itemOld);
												if (lsItem.trim().length()>0)
												{
													if(!itemOld.trim().equalsIgnoreCase(lsItem))
													{
														System.out.println("inside old break");
														isScheme = false;
														break;
													}
												}
												itemOld=lsItem;
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
										}
	
									}
									//End added by chandrashekar on 03-aug-2016
	
	
									//CHECKING CUSTOMER FOR WHICH ORDER IS BOOKED IS APPLICABLE OR NOT IN SCHEME.
									int mcount1=0,mcount2=0;
									sql = " select (case when apply_cust_list is null then ' ' else apply_cust_list end) as applyCustList, " +
											" (case when noapply_cust_list is null then ' ' else noapply_cust_list end ) as noapplyCustList " +
											"  from scheme_applicability " +
											"  where scheme_code = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCodeOrd);
									rs = pstmt.executeQuery();
									while(rs.next())
									{
										applyCustList = checkNull(rs.getString("applyCustList"));
										noapplyCustList = checkNull(rs.getString("noapplyCustList"));
										System.out.println("applyCustList["+applyCustList+"] noapplyCustList["+noapplyCustList+"]");
										if(applyCustList.trim().length() > 0)
										{
											System.out.println("inside applyCustList");
											applyCust = dist.getToken(applyCustList, ",");
											System.out.println("custCode::"+custCode);
											String lsApplyCustListArr[] = applyCustList.split(",");
											ArrayList<String> lsapplyCustList=new ArrayList<String>(Arrays.asList(lsApplyCustListArr));
											if(lsapplyCustList.contains(custCode.trim()))
											{
												mcount1 = 1;
												System.out.println("Inside mcount ["+mcount1+"]");
	
											}
											/*if(applyCust.trim().indexOf(custCode.trim()) == 1)
										{
	
											mcount1 = 1;
											System.out.println("Inside mcount ["+mcount1+"]");
										}*/
	
										}
	
										if(noapplyCustList.trim().length() > 0)
										{
											System.out.println("Inside noapplyCustList");
											noApplyCust = dist.getToken(applyCustList, ",");
											if(noApplyCust.trim().indexOf(custCode.trim()) == 1)
											{
	
												mcount2 = 1;
												System.out.println("Inside mcount2["+mcount2+"]");
											}
	
										}
	
	
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
	
									if(applyCustList.trim().length() > 0 && mcount1 == 0)
									{
										System.out.println("Inside continue applyCustList::::");
										//System.out.println("Scheme Not Applicable Due To Scheme Validity Period ");
										skipline = skipline + "'"+lineNo;
										isRejected=true;
										stockBean.setRejected(isRejected);
										sordItemBean.setSkipline(skipline);
										schemeNo++;
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tScheme Not Applicable Due To Scheme Validity Period";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										//strToWrite=strToWrite + createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme Not Applicable Due To Scheme Validity Period");
										strToWrite=strToWrite + createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme Not Applicable Because Of Apply Customer List");//added by nandkumar gadkari on 24/12/19
										//logMsg  = saleOrder + "/t" + itemCodeOrd + "/t" + itemCode + "/t" + "/t/t" + "/t" + lineNo + "/tScheme Not Applicable Due To Scheme Validity Period";
	
										continue;
	
									}
	
									if(noapplyCustList.trim().length() > 0 && mcount2 == 1)
									{
										System.out.println("Inside noapplyCustList continue::::");
										//System.out.println("Scheme Not Applicable Because Of NoApply Customer List ");
										skipline = skipline + "'"+lineNo;
										isRejected=true;
										stockBean.setRejected(isRejected);
										sordItemBean.setSkipline(skipline);
										schemeNo++;
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tScheme Not Applicable Because Of NoApply Customer List";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme Not Applicable Because Of NoApply Customer List");
										//	logMsg  = saleOrder + "/t" + itemCodeOrd + "/t" + itemCode + "/t" + "/t/t" + "/t" + lineNo + "/tScheme Not Applicable Because Of NoApply Customer List";
										continue;
									}
	
	
	
	
								}//end of check scheme Avaibality
	
	
								//sordItmQty  : mQuantity;
	
								netQuantity = sordItmQty;
	
								System.out.println("netQuantity >>>>>>"+netQuantity);
	
								sql = "select (case when track_shelf_life is null then 'N' else track_shelf_life end )" +
										" as track_shelf_life  from item where item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									trackShelfLife = rs.getString("track_shelf_life");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
	
	
								/*--------------------10-FEB-2015------------------------*/
	
								rateFailed = "F";	
								//System.out.println("qtyStk["+qtyStk+"]"+"allocQty["+allocQty+"]"+"allocQty["+allocQty+"]");
								if(qtyStk - allocQty - holdQty == 0)
								{
									//System.out.println("Call next iteration,(qtyStk - allocQty - holdQty == 0)");
									strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Stock quantity is not available");// added by nandkumar gadkari on 24/12/19
									continue;
								}
								//3. To Check for Consignment Location, and bypass the Lot No.
	
	
								sql = " select inv_stat from location where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,locCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									invStat = rs.getString("inv_stat");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								isSkip = false;
	
								isSkip = invRetstr("S-DSP",invStat,conn);
								System.out.println("isSkip >>"+isSkip);
	
								if(isSkip)
								{
									System.out.println("Location :- "+locCode + "cannot be issued");
									skiplot = skiplot + "'"+lineNo;
									isRejected=true;
									stockBean.setRejected(isRejected);
									stockBean.setSkiplot(skiplot);
									logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tLocation cannot be issued";
									//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Expiry Date cannot be empty");
									strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Location cannot be issued");	//added by nandkumar gadkari on 24/12/19
									//strToWrite=strToWrite+logMsg+"\t\r\n";
									//logMsg  = saleOrder + "/t" + itemCode + "/t/t" + locCode + "/t" + lotNo + "/t" + lineNo + "/t" + "Location :- " + locCode + " cannot be issued " ;
									continue;
								}
	
								if("Y".equalsIgnoreCase(trackShelfLife) && expDate == null )
								{
									System.out.println("Expiry Date cannot be empty");
									skiplot = skiplot + "'"+lineNo;
									isRejected=true;
									stockBean.setRejected(isRejected);
									stockBean.setSkiplot(skiplot);
									logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tExpiry Date cannot be empty";
									//strToWrite=strToWrite+logMsg+"\t\r\n";
									strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Expiry Date cannot be empty");								
									//logMsg  = saleOrder + "/t" + itemCode +  "/t" + lotNo + "/t" + lineNo + "/t Expiry Date cannot be empty";
									continue;
								}
								System.out.println("minShelfLife>>>"+minShelfLife);
								System.out.println("orderType>>>"+orderType);
								if(minShelfLife  > 0)
								{
									if("NE".equalsIgnoreCase(orderType.trim()))
									{
										chkDate1 = dist.CalcExpiry(allocDate, minShelfLife + 1);
										chkDate2 = dist.CalcExpiry(allocDate, maxShelfLife );
										System.out.println("chkDate1["+chkDate1+"]"+"chkDate2["+chkDate2+"]");
									}
									else
									{
										chkDate = dist.CalcExpiry(allocDate, minShelfLife + 1);
										System.out.println("chkDate :"+chkDate);
									}
	
									if(chkDate1 == null && chkDate2 == null)
									{
										//if mchk_date >= mexp_date 
										System.out.println("chkDate["+chkDate+"]"+"expDate["+expDate+"]");
										if(chkDate != null && expDate != null)
										{
											if(chkDate.after(expDate))
											{
												System.out.println("Near Expiry Item or Expired Item!!!!");
												skiplot = skiplot + "'"+lineNo;
												isRejected=true;
												stockBean.setRejected(isRejected);
												stockBean.setSkiplot(skiplot);
												logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tNear Expiry Item or Expired Item!!!";
												//strToWrite=strToWrite+logMsg+"\t\r\n";
												//strToWrite=strToWrite+createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Near Expiry Item Lot or Expired Item Lot!!!");
												strToWrite=strToWrite+createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Expired Item Lot!!!");// added by nandkumar gadkari on 24/12/19
												itemReasCode="P03";
												//logMsg  = saleOrder + "/t" + itemCode + "/t" + lotNo + "/t" + lineNo + "/t Near Expiry Item or Expired Item";
												continue;
											}
										}
	
									}
									else
									{
										//(NOT(mexp_date >= mchk_date1 AND mexp_date <= mchk_date2) )
										if(!(expDate.after(chkDate1) && expDate.before(chkDate2)))
										{
											System.out.println("Near Expiry Item or Expired Item");
											skiplot = skiplot + "'"+lineNo;
											isRejected=true;
											stockBean.setRejected(isRejected);
											stockBean.setSkiplot(skiplot);
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\t Near Expiry Item or Expired Item ";
											//strToWrite=strToWrite+logMsg+"\t\r\n";
											postLog = saleOrder+" "+itemCode+" "+skiplot+" "+lineNo+" is Near Expiry Item or Expired Item ";
											strToWrite=strToWrite+createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Near Expiry Item Lot or Expired Item Lot!!!");
											errorLog("P03", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
											continue;
										}
									}
	
								}// end of if(minShelfLife  > 0)
	
								//4.CHECKING RATE AND SKIPPING FOR ITEM CODE & LOT_NO
	
								rateFailed = "F";
								if(rate <= 0)
								{
									
									if(priceList != null && priceList.trim().length() > 0)
									{
										priceListType = dist.getPriceListType(priceList, conn);
										if("B".equalsIgnoreCase(priceListType))
										{
											sql = " Select count(1) as priceListCnt from pricelist_mst  " +
													" where price_list = ?  ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,priceList);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												priceListCnt = rs.getInt("priceListCnt");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
	
											if(priceListCnt == 0)
											{
												//System.out.println("Batch Price List Not found in pricelist_mst table");
												skipline = skipline + "'"+lineNo;
												isRejected=true;
												stockBean.setRejected(isRejected);
												stockBean.setSkipline(skipline);
												skiplineCnt++;
												rateFailed = "T";
												logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tBatch Price List Not found in pricelist_mst table";
												//strToWrite=strToWrite+logMsg+"\t\r\n";
												strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Batch Price List Not found in pricelist_mst table");
												postLog = "Batch Price List Not found in pricelist_mst table for "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
												errorLog("Error",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
												//System.out.println("called exit from loop");
												//break;
												continue;//added by nandkumar gadkari and commented break on 08/05/19
											}
	
	
										}//end of if("B".equalsIgnoreCase(priceListType))
									}//end of if(priceList != null && priceList.trim().length() > 0)								
									else  //Pavan Rane 25sep19 start [to skip chargable item line if rate zero and pricelist is not defined]
									{									
										if(nature == null || nature.trim().length()==0)
										{
											nature = "C";
										}
										if("C".equalsIgnoreCase(nature) )
										{										
											skipline = skipline + "'"+lineNo;
											isRejected=true;
											stockBean.setRejected(isRejected);
											stockBean.setSkipline(skipline);
											skiplineCnt++;
											rateFailed = "T";
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tRate is zero and Pricelist not defined for chargable Item ";
										//	strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Rate is zero and Pricelist not defined for chargable Item ");
											strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Rate is zero for chargable Item and Pricelist not defined ");// added by nandkumar gadkari on 24/12/19
											postLog = "Rate is zero and Pricelist not defined for chargable Item "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
											errorLog("Error",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
											continue;
										}
									}
									//Pavan Rane 25sep19 end [to skip chargable item line if pricelist is not defined]
									if(priceListDisc == null || priceListDisc.trim().length() == 0)
									{
										
										if(priceList != null && priceList.trim().length() > 0)
										{
											
											if(allocDate != null)
											{
												allocDateStr=sdf.format(allocDate);
												rate = dist.pickRate(priceList, allocDateStr,itemCode,lotNo,"D", quantityStduom, conn);
												//Pavan Rane 27apr2019 [to write log if rate found 0 or -1 for lot-item]
												if("B".equalsIgnoreCase(priceListType) && rate <= 0)
												{
													//added by nandkumar gadkari on 08/05/19 for stock mismatch entry ---start------------
													skiplot = skiplot + "'"+lineNo;
													isRejected=true;
													stockBean.setRejected(isRejected);
													stockBean.setSkiplot(skiplot);
													//added by nandkumar gadkari on 08/05/19 for stock mismatch entry ---end------------
													logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tBatch Price List or Parent Batch Price List Not found in pricelist_mst table";												
													//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Batch Price List or Parent Batch Price List Not found in pricelist_mst table");
													strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Rate is zero and Batch Price list is not defined in Price list ");// added by nandkumar gadkari on 24/12/19
													postLog = "Batch Price List Not found in pricelist_mst table for "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
													errorLog("Error",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);												
													continue;////added by nandkumar gadkari on 08/05/19
												}
											}
	
										}
									}
									else
									{
										//System.out.println(" inside rate else @@@@@<>>< >>>>>>>>>>>>>><<<<<<<"+priceListType+"]");
										priceListType="";
										plistDisc =  getDiscount(priceListDisc,orderDate,custCode,siteCode,itemCode,unit,discMerge,plDate,sordItmQty,conn);
										priceListType = dist.getPriceListType(priceListDisc, conn);
										if("M".equalsIgnoreCase(priceListType))
										{
											if(priceList != null && priceList.trim().length() > 0)
											{
												allocDateStr=sdf.format(allocDate);//Added by sarita on 09 APR 2019
												rate = dist.pickRate(priceList, allocDateStr,itemCode,lotNo,"D", quantityStduom, conn);
											}
	
											rate = calcRate(rate,plistDisc);
	
										}
										else
										{
											if(priceList != null && priceList.trim().length() > 0)
											{
												allocDateStr=sdf.format(allocDate);
												rate = dist.pickRate(priceList, allocDateStr,itemCode,lotNo,"D", quantityStduom, conn);
											}
										}
									}
	
									
									if(rate < 0)
									{
										
										//System.out.println(" inside rate else @@@@@<>>< >>>>>>>>>>>>>><<<<<<<"+rate+"]");
										//	System.out.println("rate not available");
										skipline = skipline + "'"+lineNo;
										isRejected=true;
										stockBean.setRejected(isRejected);
										stockBean.setSkipline(skipline);
										skiplineCnt++;
										rateFailed = "T";
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\trate not available";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Rate not available in Order or Price List");
										postLog = "Rate not available for "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo; 
										errorLog("P04",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
										//	System.out.println("called exit from loop");
										//break;
										continue;//added by nandkumar gadkari and commented break on 08/05/19
									}
	
	
	
								}//end if(rate <= 0)
	
								if(rateClg <= 0)
								{
									//	if (not isnull(ls_price_list__clg)) and len(trim(ls_price_list__clg)) > 0
									if(priceListClg != null && priceListClg.trim().length() > 0)
									{
										priceListType = dist.getPriceListType(priceListClg, conn);
	
										if("B".equalsIgnoreCase(priceListType))
										{
											priceListCnt=0;
											sql = " Select count(1) as priceListCnt from pricelist_mst  " +
													" where price_list = ?  ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,priceListClg);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												priceListCnt = rs.getInt("priceListCnt");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
	
	
											if(priceListCnt == 0)
											{
												//System.out.println("Batch Price List Not found in pricelist_mst table");
												//System.out.println(" Excise clg. rate not available.");
												skipline = skipline + "'"+lineNo;
												isRejected=true;
												stockBean.setRejected(isRejected);
												stockBean.setSkipline(skipline);
												skiplineCnt++;
												rateFailed = "T";
												logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tBatch Price List Not found in pricelist_mst table";
												//strToWrite=strToWrite+logMsg+"\t\r\n";
												strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Clearing Batch Price List Not found in pricelist_mst table");											
												postLog = "Batch Price List Not found in pricelist_mst table for "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
												errorLog("Error",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
												//System.out.println("called exit from loop");
												//break;
												continue;//added by nandkumar gadkari and commented break on 08/05/19
											}
										}
	
										//Added by sarita as getting Unparseable date: "" exception in case when pricelist is null on 09 APR 2019[START]
										allocDateStr=sdf.format(allocDate); System.out.println("allocDateStr ["+allocDateStr+"]");
										rateClg = dist.pickRate(priceListClg, allocDateStr,itemCode,lotNo,"D", quantityStduom, conn);
										//Added by sarita as getting Unparseable date: "" exception in case when pricelist is null on 09 APR 2019[END]
										//Pavan Rane 27apr2019 [to write log if rate found 0 or -1 for lot-item]
										if("B".equalsIgnoreCase(priceListType) && rateClg <= 0)
										{
											//added by nandkumar gadkari on 08/05/19 for stock mismatch entry ---start------------
											skiplot = skiplot + "'"+lineNo;
											isRejected=true;
											stockBean.setRejected(isRejected);
											stockBean.setSkiplot(skiplot);
											//added by nandkumar gadkari on 08/05/19 for stock mismatch entry ---end------------
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tBatch Price List or Parent Batch Price List Not found in pricelist_mst table";										
										//	strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Batch Price List or Parent Batch Price List Not found in pricelist_mst table");											
											strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Clearing Rate is zero and Clearing Batch Price list is not defined in Price list ");// added by nandkumar gadkari on 24/12/19
											postLog = "Batch Price List Not found in pricelist_mst table for "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
											errorLog("Error",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);									
											continue;//added by nandkumar gadkari on 08/05/19
										}
									}
	
	
									if(rateClg < 0)
									{
										System.out.println("rate not available");
										skipline = skipline + "'"+lineNo;
										isRejected=true;
										stockBean.setRejected(isRejected);
										stockBean.setSkipline(skipline);
										skiplineCnt++;
										rateFailed = "T";
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\trate not available";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Clearing rate not available in Order or Price List");
										postLog = "Excise clg. rate not available for "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
										errorLog("P12", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
										//System.out.println("called exit from loop");
										//break;
										continue;//added by nandkumar gadkari and commented break on 08/05/19
									}
								}//end of if(rateClg <= 0)
	
								//lockqty=0;
								System.out.println("qtyStk["+qtyStk+"]"+"allocQty["+allocQty+"]"+"holdQty["+holdQty+"]");
								System.out.println("siteItmQty["+siteItmQty+"]"+"lockqty["+lockqty+"]"+"holdQty["+holdQty+"]");
								if(((qtyStk - allocQty -holdQty) - siteItmQty ) > 0)
								{
									lockqty = lockqty + ((qtyStk - allocQty -holdQty) - siteItmQty);
									if(siteItmQty > 0)
									{
										siteItmQty = 0;
									}
								}
								else
								{
									siteItmQty = siteItmQty - (qtyStk - allocQty -holdQty);
								}
								System.out.println("lockqty["+lockqty+"]"+"netQuantity["+netQuantity+"]");
								//if(lockqty >= netQuantity)
								//if(lockqty <= netQuantity)//code in line no 2225
								//{
								System.out.println("lock quantity greater than  or eqaual to net quentity!!");
								System.out.println("called exit from loop");
								//break;
								//}
	
								//System.out.println("rateFailed >>"+rateFailed);
								//Commented by mayur on [26-12-17]----START
								/*if("F".equalsIgnoreCase(rateFailed))
								{
	
									if(!("F".equalsIgnoreCase(rateFailed)))
							{
									 
									if(("B".equalsIgnoreCase(itemFlag) || "N".equalsIgnoreCase(partQty)) && isScheme == false)
									{
	
										if(lockqty < netQuantity)
										{
	
											System.out.println("Short Quantity for scheme item");
											skipline = skipline + "'"+lineNo;
											isRejected=true;
											stockBean.setRejected(isRejected);
											stockBean.setSkipline(skipline);
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tShort Quantity for scheme item";
											strToWrite=strToWrite+logMsg+"\t\r\n";
											postLog = "Short Quantity for scheme item, "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
											errorLog("P06",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
											continue;
										}
									}
									else if(lockqty == 0)
									{	
										if(isScheme == true && "B".equalsIgnoreCase(itemFlag) && skipline.trim().indexOf("'"+lineNo.trim()) == 0 && itemCode.equalsIgnoreCase(itemCodeScheme)
												&& lineNo.equalsIgnoreCase(lineScheme))
										{
	
										}
										else
										{
											System.out.println("No Stock Available");
											skipline = skipline + "'"+lineNo;
											isRejected=true;
											stockBean.setRejected(isRejected);
											stockBean.setSkipline(skipline);
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tNo Stock Available";
											strToWrite=strToWrite+logMsg+"\t\r\n";
											postLog = "No Stock Available for "+saleOrder+" "+itemCode+" "+locCode+" "+lotNo+" "+lineNo;
											errorLog("P09", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
											continue;
										}
									}
	
								}*///end of if rateFailed--F
								//Commented by mayur on [26-12-17]----END
								itemCodeScheme = itemCode;
								lineScheme  = lineNo;
	
								/*-------------------changes done on 08-02-2015--------------------------------------*/
	
								netQuantity = sordItmQty;
								System.out.println("sordItmQty :"+sordItmQty);
								System.out.println("netQuantity:"+netQuantity);
	
	
	
								if(netQuantity > 0)
								{
	
									sql = " select grace_days from scheme_applicability " +
											" where scheme_code = ? and app_from <= ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCodeOrd);
									pstmt.setTimestamp(2,allocDate);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										graceDays = rs.getInt("grace_days");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									if(graceDays > 0)
									{
										graceDays = -1 * graceDays;
									}
	
									allocDate = utilmethod.RelativeDate(allocDate, graceDays);
	
	
									sql = " select count(*) as count from scheme_applicability where" +
											" scheme_code = ? and app_from <= ? " +
											" and valid_upto >= ?	";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,itemCodeOrd);
									pstmt.setTimestamp(2,allocDate);
									pstmt.setTimestamp(3,allocDate);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										schemeCnt = rs.getInt("count");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
	
									if(schemeCnt > 0)
									{
										//changes done by PavanR on 16/JAN/18 for scheme item[Start]
										System.out.println("lineNo>>>>["+lineNo+"]");
										System.out.println("lineNoOld>>>>["+lineNoOld+"]");
										System.out.println("saleOrder>>>>["+saleOrder+"]");
										System.out.println("saleOrderOld>>>>["+saleOrderOld+"]");
										System.out.println("itemCodeOld>>>>["+itemCodeOld+"]");
										System.out.println("itemCode>>>>["+itemCode+"]");									
										
										//if(lineNo != lineNoOld || saleOrder != saleOrderOld || itemCodeOld != itemCode )
										//changes done by PavanR on 16/JAN/18 for scheme item[End]
										if(!(lineNo.equalsIgnoreCase(lineNoOld)) || !(saleOrder.equalsIgnoreCase(saleOrderOld)) || !(itemCodeOld.equalsIgnoreCase(itemCode)))
										{
											//ratio = i_nvo_post_order.gbf_scheme(msaleorder,integer(mline_no),as_lot_sl,ls_loc_group)
											ratio=gbfScheme(saleOrder,lineNo,lotSl,locGroup,itemCode,conn);
											ratioOld = ratio;
										}
										else if("0".equalsIgnoreCase(ratio) && lineNo.equalsIgnoreCase(lineNoOld)
												&& saleOrder.equalsIgnoreCase(saleOrderOld) && itemCode.equalsIgnoreCase(itemCodeOrd))
										{
											ratio = ratioOld;
										}
										else
										{	
											ratio = "10000"; 
										}
										if(ratio==null || ratio.trim().length()==0)
										{
											ratio="0";
										}
										if("0".equalsIgnoreCase(ratio))
										{
											System.out.println("Short Quantity for scheme item");
											skipline = skipline + "'"+lineNo;
											isRejected=true;
											stockBean.setRejected(isRejected);
											stockBean.setSkipline(skipline);
											lineNoOld = lineNo;
											saleOrderOld = saleOrder;
											itemCodeOld = itemCode;
											logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tShort Quantity for scheme item";
											//strToWrite=strToWrite+logMsg+"\t\r\n";
											strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Short Quantity for scheme item");
											postLog = "Short Quantity for scheme item, "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
											errorLog("P06", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
											continue;
										}
										/*elseif not isnumber(lc_ratio)  then
								merrcode = lc_ratio
								goto exit_now*/
										else if(Double.parseDouble(ratio) !=10000)
											//else if(!("1000".equalsIgnoreCase(ratio)))
										{
											sql = "SELECT bomdet.qty_per FROM bomdet WHERE " +
													" ( bomdet.bom_code = ? ) AND ( bomdet.item_code = ? )" +
													"   and   ( bomdet.nature = ?) ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,itemCodeOrd);
											pstmt.setString(2,itemCode);
											pstmt.setString(3,nature);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												bomQtyPer = rs.getDouble("qty_per");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											if(ratio != null && ratio.trim().length()>0)
											{
												netQuantity = bomQtyPer * Double.parseDouble(ratio);
											}
											qtyStk = netQuantity;
										}
	
									}//end of if(schemeCnt > 0)
	
									System.out.println("PSqtyStk["+qtyStk+"]"+"allocQty["+allocQty+"]"+"holdQty["+holdQty+"]");
									System.out.println(" PSnetQuantity>>"+netQuantity);
									System.out.println("PSsordItemBean>>>>>>>>"+sordItemBean+"]");
									if((qtyStk - allocQty - holdQty) <= netQuantity)
									{
										inputQty = qtyStk - allocQty - holdQty; 
										//								sordItemBean.setSordItmQty(inputQty);//Added by chandrashekar on 03-oct-2016 ARUN PAL
									}
									else
									{
										inputQty = netQuantity;
										//								sordItemBean.setSordItmQty(inputQty);//Added by chandrashekar on 03-oct-2016 ARUN PAL
									}
									System.out.println("inputQty>><<<<<<<<"+inputQty);
									if(inputQty > 0)
									{
										//if isnull(as_tranid) or len(trim(as_tranid)) = 0
										//{
										orgQty = inputQty;
										unitPack="";
										sql = "select unit__pack  from" +
												"  item_lot_packsize where item_code = ? and" +
												" lot_no__from <= ?and lot_no__to >= ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,itemCode);
										pstmt.setString(2,lotNo);
										pstmt.setString(3,lotNo);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											unitPack = rs.getString("unit__pack");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										//System.out.println("unitPack>>@@@@>>["+unitPack+"]");
										if(unitPack == null || unitPack.trim().length()==0 || "B".equalsIgnoreCase(itemFlag))
										{
											unitPack = unit;
										}
										System.out.println("unitPack>>>><<<<<<<["+unitPack+"]");
										System.out.println("unit>>>><<<<<,"+unit+"]");
										if(!(unitPack.equalsIgnoreCase(unit)))
										{
	
											sql = "select quantity  " +
													" from stock where item_code = ? and " +
													" site_code = ? and loc_code = ? and " +
													" lot_no = ? and lot_sl = ? 	";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,itemCode);
											pstmt.setString(2,siteCode);
											pstmt.setString(3,locCode);
											pstmt.setString(4,lotNo);
											pstmt.setString(5,lotSl);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												batQty = rs.getDouble("quantity");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
	
											if(batQty <= qtyStk)
											{
												inputQty =  orgQty ;
											}
											System.out.println("orgQty ["+orgQty+"]"+"inputQty ["+inputQty+"]");
	
	
	
											conv = convertBox(itemCode,unitPack,unit,convQtyStduom,conn);
	
	
											sql = "Select mod(?,?) as modQuantity from dual";
											pstmt = conn.prepareStatement(sql);
											pstmt.setDouble(1,inputQty);
											pstmt.setDouble(2,conv);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												modQuantity = rs.getDouble("modQuantity");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
	
											System.out.println("lcQty["+lcQty+"]"+"inputQty["+inputQty+"]"+"modQuantity["+modQuantity+"]");
											lcQty = inputQty - modQuantity;
											inputQty = lcQty;
											System.out.println("inputQty["+inputQty+"]"+"orgQty["+orgQty+"]");
	
											if((orgQty - inputQty) > 0)
											{
												System.out.println("Quantity converted as per box. Pending Quantity ");
	
												isRejected=true;
												stockBean.setRejected(isRejected);
												logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tQuantity converted as per box. Pending Quantity ";
												//strToWrite=strToWrite+logMsg+"\t\r\n";
											//	strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Quantity converted as per box. Pending Quantity");
												strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Quantity converted as per box is not in multiples");// added by nandkumar gadkari on 24/12/19
												postLog = "Quantity converted as per box. Pending Quantity = " + (orgQty - inputQty)+", "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
												errorLog (" ", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REASCODE","W_POST_ORDER",conn);
	
											}
	
										}
	
										if((qtyStk - allocQty - holdQty) <= netQty)
										{
											netQty = netQty - inputQty ;
										}
										else
										{
											netQty = netQty - qtyStk - allocQty - holdQty;
										}
	
										//}
	
										sql="select nature from sorddet where line_no=? and sale_order= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,lineNo);
										pstmt.setString(2,saleOrder);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											nature = rs.getString("nature");
										}
										System.out.println("nature::"+nature);
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
	
										if(inputQty > 0)//2514
										{
											if("F".equalsIgnoreCase(nature))
											{
	
												sql = "select item_code__parent from item where item_code = ?"	;
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1,itemCode);
												rs = pstmt.executeQuery();
												while(rs.next())
												{
													itemCodeParent = rs.getString("item_code__parent");
												}
												pstmt.close();
												pstmt = null;
												rs.close();
												rs = null;
	
												if(itemCodeParent != null && itemCodeParent.trim().length() > 0 )
												{
													schemeCode = checkScheme(itemCodeParent, orderType,custCode,siteCode,stateCodeDlv,countCodeDlv,allocDate,conn);
													System.out.println("checkScheme:::"+schemeCode);
	
													if(schemeCode != null && schemeCode.trim().length() > 0)
													{
														//to find out total charge qty
														sql = "select tot_charge_qty, tot_free_qty   " +
																"  from prd_scheme_trace where " +
																" site_code= ? and cust_code	=	? " +
																"  and item_code	= ?" +
																"  and scheme_code=	? and ? " +
																" between eff_from and valid_upto	";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1,siteCode);
														pstmt.setString(2,custCode);
														pstmt.setString(3,itemCodeParent);
														pstmt.setString(4,schemeCode);
														pstmt.setTimestamp(5,allocDate);
														rs = pstmt.executeQuery();
														if(rs.next())
														{
															totChargeQty = rs.getDouble("tot_charge_qty");
															totFreeQty = rs.getDouble("tot_free_qty");
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
	
	
	
														//Total Allocate qty
	
														sql = "select sum(case when b.nature ='C' then a.qty_alloc else 0 end) as lc_alloc_charge_qty, " +
																" sum(case when b.nature ='F' then a.qty_alloc else 0 end) as lc_alloc_free_qty" +
																" from sordalloc a," +
																"  sorddet b where a.sale_order = b.sale_order  " +
																" and a.line_no = b.line_no  and a.line_no < ?" +
																"   and a.sale_order = ? and b.nature IN ('C','F') " +
																"and a.item_code in (select item_code from item " +
																" where item_code__parent = ?)";
	
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1,lineNo);
														pstmt.setString(2,saleOrder);
														pstmt.setString(3,itemCodeParent);
														rs = pstmt.executeQuery();
														if(rs.next())
														{
															allocChargeQty = rs.getDouble("lc_alloc_charge_qty");
															allocFreeQty = rs.getDouble("lc_alloc_free_qty");
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
	
	
														chargeQty = allocChargeQty + totChargeQty ;
	
														//quantity slab
	
	
	
														sql = " select bom.batch_qty as lc_batqty,bomdet.qty_per as lc_qtyper,bomdet.app_min_qty as lc_app_min_qty ,	" +
																" bomdet.app_max_qty as lc_app_max_qty	" +
																"  from bom, bomdet where " +
																" bom.bom_code = bomdet.bom_code and bomdet.bom_code " +
																" = ? and bomdet.nature= 'F' and  ? " +
																"  between case when bom.min_qty is null then 0 else bom.min_qty end" +
																"  And  case when bom.max_qty is null then 0 else bom.max_qty end  ";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1,schemeCode);
														pstmt.setDouble(2,chargeQty);
														rs = pstmt.executeQuery();
														if(rs.next())
														{
															batqty = rs.getDouble("lc_batqty");
															qtyper = rs.getDouble("lc_qtyper");
															appMinQty = rs.getDouble("lc_app_min_qty");
															appMaxQty = rs.getDouble("lc_app_max_qty");
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
														System.out.println("chargeQty>>>"+chargeQty);
														System.out.println("appMinQty>>>"+appMinQty);
														System.out.println("appMaxQty>>>"+appMaxQty);
	
														if(chargeQty >= appMinQty && chargeQty <= appMaxQty)
														{
															//freeQty = truncate(chargeQty / batqty,0) * qtyper
															freeQty = (chargeQty / batqty) * qtyper;
														}
														else
														{
															freeQty=0;
														}
														System.out.println("inputQty>>>"+inputQty);
														System.out.println("totFreeQty>>>"+totFreeQty);
														System.out.println("allocFreeQty>>>"+allocFreeQty);
														System.out.println("freeQty>>>"+freeQty);
														if((inputQty + totFreeQty + allocFreeQty) > freeQty )
														{
															System.out.println("Chargeable quantity of group of items not eligible for the free quantity");
															skipline = skipline + "'"+lineNo;
															isRejected=true;
															stockBean.setRejected(isRejected);
															stockBean.setSkipline(skipline);
															logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tChargeable quantity of group of items not eligible for the free quantity";
															//strToWrite=strToWrite+logMsg+"\t\r\n";
															strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Chargeable quantity of group of items not eligible for the free quantity");
															//logMsg  = saleOrder + "/t" + itemCode + "/t/t" + "/t" + lineNo + "/t Group of items is not eligible for the free quantity";
															postLog  = "Group of items is not eligible for the free quantity" + saleOrder +" "+ itemCodeOrd +" "+ itemCode +" "+ lineNo;
															errorLog("P02", postLog ,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
															continue;
	
	
														}
	
														if(batqty == 0.0 && qtyper == 0.0 && appMinQty == 0.0 && appMaxQty == 0.0)//record not found above sql query
														{
															System.out.println("Group of items is not eligible for the free quantity");
															skipline = skipline + "'"+lineNo;
															isRejected=true;
															stockBean.setRejected(isRejected);
															stockBean.setSkipline(skipline);
															logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tGroup of items is not eligible for the free quantity";
															//strToWrite=strToWrite+logMsg+"\t\r\n";
															strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Group of items is not eligible for the free quantity");
															//logMsg  = saleOrder + "/t" + itemCode + "/t/t" + "/t" + lineNo + "/t Chargeable quantity of group of items not eligible for the free quantity";
															postLog = "Chargeable quantity of group of items not eligible for the " +
																	"free quantity " + saleOrder +" "+ itemCodeOrd +" "+ itemCode +" "+ lineNo;
															errorLog("P02", postLog ,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
															continue;
	
														}
	
	
													}
													else //No Scheme exists
													{
														System.out.println("Scheme Not Applicable Due To Scheme Validity Period ");
														skipline = skipline + "'"+lineNo;
														isRejected=true;
														stockBean.setRejected(isRejected);
														stockBean.setSkipline(skipline);
														logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tScheme Not Applicable Due To Scheme Validity Period ";
														//strToWrite=strToWrite+logMsg+"\t\r\n";
														//logMsg  = saleOrder + "/t" + itemCode + "/t/t" + "/t" + lineNo + "\tScheme Not Applicable Due To Scheme Validity Period";
														strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme Not Applicable Due To Scheme Validity Period");
														postLog = "Chargeable quantity of group of items not eligible for the " +
																"free quantity " + saleOrder +" "+ itemCodeOrd +" "+ itemCode +" "+ lineNo;
														errorLog("P02", postLog ,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
														continue;
													}
	
												}//end of if(itemCodeParent != null && itemCodeParent.trim().length() > 0 )
												else
												{
													System.out.println("Scheme Item is not belong to the group of items");
													skipline = skipline + "'"+lineNo;
													isRejected=true;
													stockBean.setRejected(isRejected);
													stockBean.setSkipline(skipline);
													logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tScheme Item is not belong to the group of items";
													//strToWrite=strToWrite+logMsg+"\t\r\n";
													strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Scheme Item is not belong to the group of items");
													//logMsg  = saleOrder + "/t" + itemCode + "\t\t" + "\t" + lineNo + "~\tScheme Item is not belong to the group of items";
													postLog =  "Scheme Item is not belong to the group of items " + saleOrder +" "+ itemCodeOrd +" "+ itemCode +" "+ lineNo;
													errorLog("P02", postLog ,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
													continue;
												}
											}//end of if("F".equalsIgnoreCase(nature))
	
	
										}//end of inputQty > 0 
										else
										{
											inputQty = 0;
										}
									}//end of inputQty > 0 
	
	
	
	
	
								}//end of if(netQuantity > 0)
	
	
	
								/*-------------------changes done on 08-02-2015,end--------------------------------------*/
								System.out.println("-----------------Map the record for validation----------");
								System.out.println("saleOrder["+saleOrder+"]"+"itemCode["+itemCode+"]"+"lineNo["+lineNo+"]");
								System.out.println("lotNo["+lotNo+"]"+"lotSl["+lotSl+"]"+"locCode["+locCode+"]");
								System.out.println("qtyStk["+qtyStk+"]"+"holdQty["+holdQty+"]"+"allocQty["+allocQty+"]");
								System.out.println("skipline :"+sordItemBean.getSkipline());
								System.out.println("skiplot :"+sordItemBean.getSkiplot());
								System.out.println("---------------------------------------------------------");
								/*----------------------------------------------------------------------------------*/	
	
							//}//end of if(stockOpt != 0) // commented  by Nandkumar Gadkari on 27/06/18 for  Non Inventory Item.
							
							//Pavan Rane 01nov19 start [to break stock loop if alloatced stock lot fulfill sorditem Quantity]
							//(so that only stockBean having qty will added in stockList).
							System.out.println("sordItmQty["+sordItemBean.getSordItmQty()+"] sordAllocQty["+sordAllocQty+"]availStkQty["+availStkQty+"]");						
							if(sordItemBean.getSordItmQty() > sordAllocQty && sordItemBean.getSordItmQty() < availStkQty) 
							{
								stockTot = availStkQty; // to set avail stock to ;
								System.out.println("sufficient stock quantity break!!!");
								strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Sufficient stock quantity break for alloatced stock lot fulfill sorditem Quantity");// added by nandkumar gadkari on 24/12/19
								break;
							}
							//Pavan Rane 01nov19 End [to break stock loop if alloatced stock lot fulfill sorditem Quantity]
							stockList.add(stockBean);
	
							System.out.println("lotNO>>"+lotNo+"lotSl >>"+lotSl);
							System.out.println("PSallocQty["+allocQty+"]"+"holdQty["+holdQty+"]"+"qtyStk - allocQty - holdQty["+(qtyStk - allocQty - holdQty)+"");
							System.out.println("PSstockTot["+stockTot+"]"+"qtyStk["+qtyStk+"]"+"sordItmQty["+sordItmQty+"");
	
							//					stockTot=0.0;// commemted by arun pal
							stockTot = stockTot + (qtyStk - allocQty - holdQty);
							System.out.println("stockTot >>>>"+stockTot);
							
							//changes done by PavanR on 16/JAN/18 for scheme item[Start]
							lineNoOld = lineNo;
							saleOrderOld = saleOrder;
							itemCodeOld = itemCode;
							System.out.println("Replacing value"+lineNoOld+":"+saleOrderOld+":"+itemCodeOld);
							//changes done by PavanR on 16/JAN/18 for scheme item[End]
							if(stockTot >= sordItmQty)
							{
								System.out.println("total stock quantity greater than sorder quantity!!!");
								
								break;
							}
	
	
						}//end of while 
						rs5.close();
						rs5 = null;
						pstmt5.close();
						pstmt5 = null;
						// changes by arun start 30-11-17
					}// end of if(alloCnt > 0 && sordItmQty <= sordAllocQty) //{
				}//end of if(stockOpt != 0)
				else // else condition by Nandkumar Gadkari on 27/06/18 for  Non Inventory Item.
				{		
					StockBean stockBean = new StockBean();
					stockBean.setSordItemBean(sordItemBean);
					if(stockQtyTot < sordItmQty)
					{
						stockQtyTot=0.0;
						sql = "select site_code__supp  from siteitem where " +
								" site_code = ? and item_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteCodeSupp = checkNull(rs.getString("site_code__supp"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(siteCodeSupp.trim().length() == 0)
						{
							sql = "select site_code__supp from " +
									" site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								siteCodeSupp = checkNull(rs.getString("site_code__supp"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

					}//	if(stockQty < sordItmQty)

					netQuantity = sordItmQty;

					System.out.println("netQuantity >>>>>>"+netQuantity);

					sql = "select (case when track_shelf_life is null then 'N' else track_shelf_life end )" +
							" as track_shelf_life  from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						trackShelfLife = rs.getString("track_shelf_life");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					

					
					System.out.println("records@["+records+"]");
					isRejected=false;
					stockBean.setRejected(isRejected);
					stockList.add(stockBean);
					

				} //end of else(stockOpt != 0)
					/*System.out.println(" Ratefild  insert into loop["+rateFailed+"]");
					System.out.println("Before stockTot<>>><<<<<<<>"+stockTot+"]");
					System.out.println("Before netQuantity<>>><<<<<<<>"+netQuantity+"]");*/
					if("F".equalsIgnoreCase(rateFailed))
					{
						rateFailed = "F";	
						System.out.println(" Ratefild  insert into loop["+rateFailed+"]");
						if(("B".equalsIgnoreCase(itemFlag) || "N".equalsIgnoreCase(partQty)) && isScheme == false)
						{
							/*System.out.println("Before stockTot<>>><<<<<<<>"+stockTot+"]");
						System.out.println("Before netQuantity<>>><<<<<<<>"+netQuantity+"]");*/

							if(stockTot < netQuantity)
							{

								System.out.println("Short Quantity for scheme item");
								skipline = skipline + "'"+lineNo;
								isRejected=true;
								//stockBean.setRejected(isRejected);
								//stockBean.setSkipline(skipline);
								logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tShort Quantity for scheme item";
								//strToWrite=strToWrite+logMsg+"\t\r\n";
								strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Short Quantity for scheme item");
								postLog = "Short Quantity for scheme item, "+saleOrder+" "+itemCode+" "+lotNo+" "+lineNo;
								errorLog("P06",postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
								continue;
							}
						}
						else if(stockTot == 0)
						{	
							System.out.println(" Ratefild   else insert into loop["+rateFailed+"]");
							if(isScheme == true && "B".equalsIgnoreCase(itemFlag) && skipline.trim().indexOf("'"+lineNo.trim()) == 0 && itemCode.equalsIgnoreCase(itemCodeScheme)
									&& lineNo.equalsIgnoreCase(lineScheme))
							{

							}
							else
							{
								System.out.println("No Stock Available");
								skipline = skipline + "'"+lineNo;
								isRejected=true;
								//							stockBean.setRejected(isRejected);
								//							stockBean.setSkipline(skipline);
								
								if(logMsg!=null && logMsg.trim().length()>0)
								{
									errorLog(itemReasCode, logMsg,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
								}
								else
								{
								logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tNo Stock Available";
								//strToWrite=strToWrite+logMsg+"\t\r\n";
								strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "No Stock Available");
								postLog = "No Stock Available for "+saleOrder+" "+itemCode+" "+locCode+" "+lotNo+" "+lineNo;
								errorLog("P09", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
								}
								continue;
							}

						}
						
					}//end of if rateFailed--F

					// changes by arun ended 30-11-17
					//}//end of while

					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@test stockList[[["+stockList.toString()+"]]]");
					System.out.println("stockcnt"+stockcnt);
					/*rs5.close();
					rs5 = null;
					pstmt5.close();
					pstmt5 = null;*/
					
					//Pavan R Start 12/JAN/18
					String sqlAlloc=""; 
					PreparedStatement pstmt6=null;				
					double qtyStkAlloc =0.0, allocQtyAlloc=0.0, holdQtyAlloc=0.0, inputQtyAlloc=0.0,inputQtyTotAlloc=0.0,sordItmQtyallc=0.0,updQty=0.0;
					//Pavan Rane 01nov19 start [to assign sorditem qty from sordItemBean]
					//sordItmQtyallc = sordItmQty;
					sordItmQtyallc = sordItemBean.getSordItmQty();
					//Pavan Rane 01nov19 end [to assign sorditem qty from sordItemBean]
					System.out.println("sordItmQty::["+sordItmQty+"]");
					if(stockOpt != 0)// Condition  Added by Nandkumar Gadkari on 29/06/18 for  Non Inventory Item.
					{
						for (StockBean stockBean : stockList) 
						{						 
							boolean isRejected1 = stockBean.isRejected();
							System.out.println("isRejected1"+isRejected1);
							if(isRejected1==false)
							{			
								qtyStkAlloc = stockBean.getStockQty();
								allocQtyAlloc = stockBean.getAllocQty();							
								holdQtyAlloc = stockBean.getHoldQty();
								siteCode = stockBean.getSordItemBean().getSiteCode();
								itemCode = stockBean.getSordItemBean().getItemCode();
								lotNo = stockBean.getLotNo();
								lotSl = stockBean.getLotSl();
								locCode=stockBean.getLocCode();
								System.out.println("qtyStkAlloc["+qtyStkAlloc+"]allocQtyAlloc["+allocQtyAlloc+"]");
								System.out.println("holdQtyAlloc["+holdQtyAlloc+"]itemCode["+itemCode+"]");
								System.out.println("sordItmQtyallc["+sordItmQtyallc+"]");
								if ((sordItmQtyallc) <= 0){
									
									strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Sorder item quantity is zero");// added by nandkumar gadkari on 24/12/19
									break;
									
								}
								HashMap invAllocTraceMap = new HashMap();	
								if(qtyStkAlloc - allocQtyAlloc - holdQtyAlloc >= sordItmQtyallc)
								{
									updQty = sordItmQtyallc;
									
									/*sqlAlloc = "UPDATE STOCK SET ALLOC_QTY =(CASE WHEN ALLOC_QTY IS NULL THEN 0 ELSE ALLOC_QTY END) + ? "
											  +"WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?";
									pstmt6 = conn.prepareStatement(sqlAlloc);
									pstmt6.setDouble(1,updQty) ;
									pstmt6.setString(2,itemCode.trim()) ;
									pstmt6.setString(3, siteCode.trim()) ;
									pstmt6.setString(4,locCode.trim()) ;
									pstmt6.setString(5,lotNo.trim()) ;
									pstmt6.setString(6,lotSl.trim()) ; 
									pstmt6.executeUpdate();
									pstmt6.close();
									pstmt6 = null;*/
									//added by nandkumar gadkari on 24/01/19----------start---------------
									
									invAllocTraceMap.put("ref_ser","S-ORD");
									invAllocTraceMap.put("ref_id",saleOrder);
									invAllocTraceMap.put("ref_line",lineNo);
									invAllocTraceMap.put("site_code",siteCode);
									invAllocTraceMap.put("item_code",itemCode);
									invAllocTraceMap.put("loc_code",locCode);
									invAllocTraceMap.put("lot_no",lotNo);
									invAllocTraceMap.put("lot_sl",lotSl);
									invAllocTraceMap.put("alloc_qty",updQty);
									invAllocTraceMap.put("chg_user",userId);
									invAllocTraceMap.put("chg_term",termId);
									invAllocTraceMap.put("chg_win","W_SORDER");	
									//added by nandkumar gadkari on 17/04/19-------start=----------
									expLev = stockBean.getSordItemBean().getExpLev();
									logMsg= saleOrder +" "+expLev+" "+lineNo + " "+"Allocation of stock from PostOrderProcess";
									invAllocTraceMap.put("alloc_ref",logMsg);	
									//added by nandkumar gadkari on 17/04/19-------end=----------
									merrcode = invBean.updateInvallocTrace(invAllocTraceMap,conn);
									if(merrcode != null && merrcode.trim().length() > 0)
									{
										merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
											return merrcode;
									}
									else
									{
										//added by nandkumar gadkari on 17/04/19-------start=----------
										sql = "select alloc_ref from stock where item_code= ? and site_code= ? and lot_no= ? and loc_code=? and lot_sl=? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,itemCode);
										pstmt.setString(2,siteCode);
										pstmt.setString(3,lotNo);
										pstmt.setString(4,locCode);
										pstmt.setString(5,lotSl);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											allocRef = rs.getString(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										System.out.println("allocRef["+allocRef+"]");
										System.out.println("allocReflog["+logMsg+"]");
										if(!allocRef.equalsIgnoreCase(logMsg))
										{
											merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
											return merrcode;
										}
										//added by nandkumar gadkari on 17/04/19-------end=----------
										
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo +"\t"+"Inserted data in INVALLOC_TRACE";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, logMsg);commented by nandkumar gadkari on 24/12/19
									}
									//added by nandkumar gadkari on 24/01/19------------- end------------
								//	inputQtyTotAlloc = inputQtyTotAlloc+inputQtyAlloc;
									
									sordItmQtyallc = 0;
									stockBean.setAllocQtyUpd(updQty);
								}
								else
								{
									updQty = qtyStkAlloc - allocQtyAlloc - holdQtyAlloc;
									stockBean.setAllocQtyUpd(updQty);
									 
									/*sqlAlloc = "UPDATE STOCK SET ALLOC_QTY =(CASE WHEN ALLOC_QTY IS NULL THEN 0 ELSE ALLOC_QTY END) + ? "
											  +"WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?";
									pstmt6 = conn.prepareStatement(sqlAlloc);
									pstmt6.setDouble(1,updQty) ;
									pstmt6.setString(2,itemCode.trim()) ;
									pstmt6.setString(3, siteCode.trim()) ;
									pstmt6.setString(4,locCode.trim()) ;
									pstmt6.setString(5,lotNo.trim()) ;
									pstmt6.setString(6,lotSl.trim()) ; 
									pstmt6.executeUpdate();
									pstmt6.close();
									pstmt6 = null;*/
									
									//added by nandkumar gadkari on 24/01/19----------start---------------
									
									invAllocTraceMap.put("ref_ser","S-ORD");
									invAllocTraceMap.put("ref_id",saleOrder);
									invAllocTraceMap.put("ref_line",lineNo);
									invAllocTraceMap.put("site_code",siteCode);
									invAllocTraceMap.put("item_code",itemCode);
									invAllocTraceMap.put("loc_code",locCode);
									invAllocTraceMap.put("lot_no",lotNo);
									invAllocTraceMap.put("lot_sl",lotSl);
									invAllocTraceMap.put("alloc_qty",updQty);
									invAllocTraceMap.put("chg_user",userId);
									invAllocTraceMap.put("chg_term",termId);
									invAllocTraceMap.put("chg_win","W_SORDER");

									//added by nandkumar gadkari on 17/04/19-------start=----------
									expLev = stockBean.getSordItemBean().getExpLev();
									logMsg= saleOrder +" "+expLev+" "+lineNo + " "+"Allocation for Stock from PostOrderProcess";
									invAllocTraceMap.put("alloc_ref",logMsg);	
									//added by nandkumar gadkari on 17/04/19-------end=----------
									
									
									merrcode = invBean.updateInvallocTrace(invAllocTraceMap,conn);
									if(merrcode != null && merrcode.trim().length() > 0)
									{
										merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
											return merrcode;
									}
									else
									{
										//added by nandkumar gadkari on 17/04/19-------start=----------
										sql = "select alloc_ref from stock where item_code= ? and site_code= ? and lot_no= ? and loc_code=? and lot_sl=? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,itemCode);
										pstmt.setString(2,siteCode);
										pstmt.setString(3,lotNo);
										pstmt.setString(4,locCode);
										pstmt.setString(5,lotSl);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											allocRef = rs.getString(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										System.out.println("allocRef["+allocRef+"]");
										System.out.println("allocReflog["+logMsg+"]");
										if(!allocRef.equalsIgnoreCase(logMsg))
										{
											merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
											return merrcode;
										}
										//added by nandkumar gadkari on 17/04/19-------end=----------
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tInserted data in INVALLOC_TRACE";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, logMsg);commented by nandkumar gadkari on 24/12/19
									}
									//added by nandkumar gadkari on 24/01/19------------- end------------
									
									sordItmQtyallc = sordItmQtyallc - (qtyStkAlloc - allocQtyAlloc - holdQtyAlloc);
									
								}
								
							}
							
						}
					}//end of if(stockOpt != 0) condition 
					//Pavan R End 12/JAN/18 
					//System.out.println("@@@@@@@@@@ forTestCnt["+forTestCnt+"]:::stockList.size()>>>>"+stockList.size());	
					sordItemBean.setStockList(stockList);
					//stockList.clear();
				}// end of sordItemList commented by arun line no 3457 27-11-17

				System.out.println("stockTestCnt :"+stockTestCnt);
				System.out.println("forTestCnt :"+forTestCnt);

				//}added arun

				//added By Pavan R on 2K18/Feb/12 for 'Ship complete Order' Flag not considered in Order Posting.[Start]
				boolean partQtyFlag = false;
				if("0".equalsIgnoreCase(partQty) || "1".equalsIgnoreCase(partQty))
				{
					System.out.println("sordItemList::["+sordItemList+"]");
					for(SordItemBean sordItemBean : sordItemList)
					{
						double updQty1 = 0.0, updQtyTot = 0.0;
						sordItmQty = sordItemBean.getSordItmQty();
						stockList=sordItemBean.getStockList();
						
						partQty = sordItemBean.getSorderBean().getPartQty();
						saleOrder = sordItemBean.getSaleOrder();
						itemCode = sordItemBean.getItemCode();						
						lineNo=sordItemBean.getLineNo();
						System.out.println("####SaleOrder["+saleOrder+"]itemCode["+itemCode+"]lineNo["+lineNo+"]");
						System.out.println("3688### stockList Size["+stockList.size()+"]");
						for(StockBean stockBean :stockList)
						{							
							updQty1 = stockBean.getAllocQtyUpd();
							System.out.println("stockbean---updQty1::"+updQty1);
							itemCodeOrd = stockBean.getSordItemBean().getItemCodeOrd();																			
							quantityStduom = stockBean.getSordItemBean().getQuantityStduom();
							System.out.println("stockbean---itemCodeOrd::"+itemCodeOrd);
							itemCode = stockBean.getSordItemBean().getItemCode();
							System.out.println("stockbean---itemCode::"+itemCode);
							System.out.println("stockbean---quantityStduom::"+quantityStduom);																			
							isRejected = stockBean.isRejected();
							System.out.println("stockbean---isRejected-"+isRejected);
																			
							if(isRejected)
							{
								continue;
							}
							System.out.println("stockOpt::["+stockOpt+"]sordItmQty["+sordItmQty+"]");
							
							updQtyTot = updQtyTot + updQty1;
							System.out.println("stockbean---updQtyTot::"+updQtyTot);
							
						}//end of for(StockBean stockBean :stockList) 2nd iterator..........
						
						if(sordItmQty > 0)
						{		
							System.out.println("$$$SaleOrder["+saleOrder+"]itemCode["+itemCode+"]lineNo["+lineNo+"]");
							//System.out.println("updQty1 < quantityStduom ["+updQty1+"<"+quantityStduom);
							System.out.println("-------["+updQtyTot+"<"+sordItmQty+"]------");
		
							//if(updQtyTot < quantityStduom)	
							if(stockOpt != 0)	//condition added by nandkumar gadkari on 02/07/18
							{
								
								if(updQtyTot < sordItmQty)
								{		
									String prtQty[] = {"Ship complete order", "Ship part order - Full Qty", "Ship Available" };
									String prtQtyMsg = "";
									if("0".equalsIgnoreCase(partQty)){
										prtQtyMsg = prtQty[0];
									}else{
										prtQtyMsg = prtQty[1];
									}
									System.out.println("No Stock Available PartQty");
									skipline = skipline + "'"+lineNo;
									isRejected=true;
									sordItemBean.setRejected(isRejected);
									sordItemBean.setSkipline(skipline);
									logMsg = saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tNo Stock Available";
									//strToWrite=strToWrite+logMsg+"\t\r\n";
									//Pavan Rane 5jul19 start [to display proper details on PostingLog]
									//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "No Stock Available for Part Qty");
									stockList = sordItemBean.getStockList();
									if(stockList.isEmpty()) 
									{
										strToWrite = strToWrite +createPostLog(saleOrder, sordItemBean.getItemCodeOrd(), "", "", lineNo, "Part Quantity Delivery is "+prtQtyMsg+":Stock not sufficient");	
									}
									for(StockBean stockBean :stockList)
									{																												
										if(isRejected)
										{
											lotNo = stockBean.getLotNo();
											locCode = stockBean.getLocCode();
											strToWrite = strToWrite +createPostLog(saleOrder, sordItemBean.getItemCodeOrd(), locCode, lotNo, lineNo, "Part Quantity Delivery is "+prtQtyMsg+":Stock not sufficient");
										}
									}
									//Pavan Rane 5jul19 end
									postLog = "Short stock for "+prtQtyMsg+" type PartQty "+saleOrder+" "+itemCode+" "+locCode+" "+lotNo+" "+lineNo;
									if(!skipline.contains(lineNo.trim()))
									{
										errorLog("P09", postLog,"sorditem",saleOrder, lineNo, expLev, " ", " ", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);
									}
									//partQtyFlag = true; //If partQty is 0 commented by nandkumar gadkari on 26/06/19
									if("0".equalsIgnoreCase(partQty))
									{
										partQtyFlag = true;
										System.out.println("break Available PartQty");
										break;
									}
									continue;
								}
						   }//end of if(stockOpt != 0) condition 
						}//end of if(netQuantity > 0)
						
					}//end of SordItemBean loop
					
				}//end of if("0".equals(partQty) || "1".equals(partQty))
				//added By Pavan R on 2K18/Feb/12[End]
				
				int vaildateData=0;
				double totInputQty=0.0;
				PreparedStatement pstmt6 = null;
				String sqlAlloc = "";
				System.out.println("sordItemList lenth@@@"+sordItemList.size());
				sql = "select b.tran_id, b.line_no, (b.quantity - b.dealloc_qty) as pending_dealloc_qty" +
						" from sord_alloc a, sord_alloc_det b   where a.tran_id   	= b.tran_id	 " +
						" and a.cust_code 	= ?  and a.site_code   = ? and a.sale_order " +
						"  is null  and b.item_code 	= ?   and b.loc_code	 	= ? " +
						"   and b.lot_no		= ?  and b.lot_sl		= ? " +
						"  and b.quantity - b.dealloc_qty > 0 ";
				pstmt1 = conn.prepareStatement(sql);
				for(SordItemBean sordItemBean :sordItemList )//required for update sorditem accordingly line no and sale order// rename  sordItemBean to sordItemBean1 by arun pal 27/11/17
				{

					sordItmQty = sordItemBean.getSordItmQty();
					netQuantity=sordItmQty;
					System.out.println("netQuantity>>>"+netQuantity);
					stockList=sordItemBean.getStockList();
					System.out.println("stockList>>>"+stockList);
					System.out.println("expLev>@@@@@@@@@>"+expLev);
					totInputQty=0;
					System.out.println("stockList lenth@@@"+stockList.size());

					for(StockBean stockBean :stockList)
					{

						//saleOrder = stockBean.getSordItemBean().getSaleOrder();
						System.out.println("stockList iterator starts...........");
						stockOpt = stockBean.getSordItemBean().getStockOpt();
						//sordItmQty = stockBean.getSordItemBean().getSordItmQty();
						siteCode = stockBean.getSordItemBean().getSiteCode();
						itemCode = stockBean.getSordItemBean().getItemCode();
						itemCodeOrd = stockBean.getSordItemBean().getItemCodeOrd();
						itemCodeParent = stockBean.getSordItemBean().getItemCodeParent();
						itemFlag = stockBean.getSordItemBean().getItemFlag();
						rate =  stockBean.getSordItemBean().getRate();
						unit = stockBean.getSordItemBean().getUnit();
						unitStd = stockBean.getSordItemBean().getUnitStd();
						rateClg = stockBean.getSordItemBean().getRateClg();
						siteItmQty = stockBean.getSordItemBean().getSiteItmQty();
						convQtyStduom = stockBean.getSordItemBean().getConvQtyStduom();
						lineNo=stockBean.getSordItemBean().getLineNo();
						nature=stockBean.getSordItemBean().getNature();
						stockQtyTot = stockBean.getStockQty();
						//qtyStk = stockBean.getStockQty();
						/*custCode = stockBean.getSordItemBean().getSorderBean().getCustCode();
					orderDate = stockBean.getSordItemBean().getSorderBean().getOrderdate();
					priceListDisc = stockBean.getSordItemBean().getSorderBean().getPriceListDisc();
					plDate =  stockBean.getSordItemBean().getSorderBean().getPlDate();
					minShelfLife = stockBean.getSordItemBean().getMinShelfLife();
					maxShelfLife = stockBean.getSordItemBean().getMaxShelfLife();
					quantityStduom = stockBean.getSordItemBean().getQuantityStduom();
					orderType = stockBean.getSordItemBean().getSorderBean().getOrderType();
					partQty = stockBean.getSordItemBean().getSorderBean().getPartQty();
					saleOrder = stockBean.getSordItemBean().getSaleOrder();
					//////////////////////////////////////////////////////
					System.out.println(" after stockbean saleOrder ["+saleOrder+"]");
					stateCodeDlv = stockBean.getSordItemBean().getSorderBean().getStateCodeDlv();
					countCodeDlv = stockBean.getSordItemBean().getSorderBean().getCountCodeDlv();*/

						custCode = stockBean.getSordItemBean().getSorderBean().getCustCode();
						System.out.println("after custCode["+custCode+"]");
						orderDate = stockBean.getSordItemBean().getSorderBean().getOrderdate();
						System.out.println("after orderDate["+orderDate+"]");
						priceListDisc = stockBean.getSordItemBean().getSorderBean().getPriceListDisc();
						System.out.println("after priceListDisc["+priceListDisc+"]");
						plDate =  stockBean.getSordItemBean().getSorderBean().getPlDate();
						System.out.println("after plDate["+plDate+"]");
						minShelfLife = stockBean.getSordItemBean().getMinShelfLife();
						System.out.println("after minShelfLife["+minShelfLife+"]");
						maxShelfLife = stockBean.getSordItemBean().getMaxShelfLife();
						System.out.println("after custCode["+custCode+"]");
						quantityStduom = stockBean.getSordItemBean().getQuantityStduom();
						System.out.println("after custCode["+custCode+"]");
						orderType = stockBean.getSordItemBean().getSorderBean().getOrderType();
						System.out.println("after custCode["+custCode+"]");
						partQty = stockBean.getSordItemBean().getSorderBean().getPartQty();
						//saleOrder = stockBean.getSordItemBean().getSorderBean().getSaleOrder();
						saleOrder = stockBean.getSordItemBean().getSaleOrder();


						//////////////////////////////////////////////////////
						System.out.println(" after stockbean saleOrder ["+saleOrder+"]");
						stateCodeDlv = stockBean.getSordItemBean().getSorderBean().getStateCodeDlv();
						countCodeDlv = stockBean.getSordItemBean().getSorderBean().getCountCodeDlv();
						lotNo = stockBean.getLotNo();
						lotSl = stockBean.getLotSl();
						locCode=stockBean.getLocCode();
						qtyStk = stockBean.getStockQty();
						allocQty = stockBean.getAllocQty();
						expDate = stockBean.getExpDate();
						grade = stockBean.getGrade();
						mfgDate = stockBean.getMfgDate();
						siteCodeMfg = stockBean.getSiteCodeMfg();
						locCode = stockBean.getLocCode();
						holdQty = stockBean.getHoldQty();
						//commented by kunal on 5/09/2019 to take skipline value from method local variable
						//skipline=stockBean.getSkipline();
						skiplot =stockBean.getSkiplot();
						isRejected = stockBean.isRejected();
						System.out.println("isRejected>>"+isRejected);
						//inputQty=sordItmQty;
						//quantityStduom=sordItmQty;

						expLev=stockBean.getSordItemBean().getExpLev();
						System.out.println("expLev>>>"+expLev);
						skipline = skipline == null ? "" : skipline;
						skiplot = skiplot == null ? "" : skiplot;
						/*System.out.println("saleOrder["+saleOrder+"]"+"itemCode["+itemCode+"]"+"lineNo["+lineNo+"]");
					System.out.println("lotNo["+lotNo+"]"+"lotSl["+lotSl+"]"+"locCode["+locCode+"]");
					System.out.println("stockQty["+qtyStk+"]"+"holdQty["+holdQty+"]"+"allocQty["+allocQty+"]");
					System.out.println("qtyStk["+qtyStk+"]"+"rate["+rate+"]"+"unit["+unit+"]");
					System.out.println("sordItmQty :"+sordItmQty);
					System.out.println("skipline:"+skipline);
					System.out.println("skiplot:"+skiplot);
					System.out.println("itemCode@@@@["+itemCode+"]unit@@@["+unit+"]unitsTD"+unitStd);
					System.out.println("skiplot.trim() :"+skiplot.trim());
					System.out.println("'"+lineNo.trim());
					System.out.println(">>>"+skipline.trim().indexOf(lineNo.trim()));
					System.out.println("<<<<"+skiplot.trim().indexOf(lineNo.trim()));*/
				System.out.println("skipline["+skipline+"] lineNo["+lineNo+"]");
				
				//Uncommented by kunal on 5/09/2019 to skip scheme item if stock for chargeable item is insufficient
				//COMMENTED BY NANDKUMAR GADKARI ON 21/01/19 DUE TO THIS ALLOC QTY NOT GETTING UPDATE
				/*if(skipline.trim().indexOf(lineNo.trim()) > 0 )
				{
					System.out.println("Inside skipline");
					continue;
					
				}
				if(skiplot.trim().indexOf(lineNo.trim()) > 0 )
				{
					System.out.println("Inside skiplot");
					continue;
				}*/
				
						
				//Pavan R Start 12/JAN/18
				double allocQtyUpd = stockBean.getAllocQtyUpd();
				System.out.println("allocQtyUpd::["+allocQtyUpd+"]");
				if(allocQtyUpd > 0)
				{
					/*sqlAlloc = "UPDATE STOCK SET ALLOC_QTY =(CASE WHEN ALLOC_QTY IS NULL THEN 0 ELSE ALLOC_QTY END) + ? "
							  +"WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?";
					pstmt6 = conn.prepareStatement(sqlAlloc);
					pstmt6.setDouble(1,(allocQtyUpd * (-1))) ;
					pstmt6.setString(2,itemCode.trim()) ;
					pstmt6.setString(3, siteCode.trim()) ;
					pstmt6.setString(4,locCode.trim()) ;
					pstmt6.setString(5,lotNo.trim()) ;
					pstmt6.setString(6,lotSl.trim()) ; 
					pstmt6.executeUpdate();
					pstmt6.close();
					pstmt6 = null;*/// commented by nandkumar gadkari on 24/01/19
					
				}
				//Pavan R End 12/JAN/18
				
				// commented by nandkumar gadkari on 24/01/19
						/*if(isRejected)
						{
							continue;
						}*/


				//if(skipline.trim().indexOf("'"+lineNo.trim()) > 0 || skiplot.trim().indexOf("'"+lineNo.trim()) > 0 || isRejected)// condition merged by nandkumar gadkari on 24/01/19
		//		condition commented and modified by nandkumar gadkari on 26/06/19
				if(skipline.trim().indexOf(("'"+lineNo).trim()) >= 0 || skiplot.trim().indexOf("'"+lineNo.trim()) > 0 || isRejected ||partQtyFlag)
				{
					//added by nandkumar gadkari on 24/01/19----------start---------------
					if(allocQtyUpd > 0)
					{
						HashMap invAllocTraceMap = new HashMap();
						invAllocTraceMap.put("ref_ser","S-ORD");
						invAllocTraceMap.put("ref_id",saleOrder);
						invAllocTraceMap.put("ref_line",lineNo);
						invAllocTraceMap.put("site_code",siteCode);
						invAllocTraceMap.put("item_code",itemCode);
						invAllocTraceMap.put("loc_code",locCode);
						invAllocTraceMap.put("lot_no",lotNo);
						invAllocTraceMap.put("lot_sl",lotSl);
						invAllocTraceMap.put("alloc_qty",allocQtyUpd * (-1));
						invAllocTraceMap.put("chg_user",userId);
						invAllocTraceMap.put("chg_term",termId);
						invAllocTraceMap.put("chg_win","W_SORDER");			
						//added by nandkumar gadkari on 17/04/19-------start=----------
						expLev = stockBean.getSordItemBean().getExpLev();
						logMsg= saleOrder +" "+expLev+" "+lineNo + " "+"Deallocation for Skip line from PostOrderProcess";
						invAllocTraceMap.put("alloc_ref",logMsg);	
						//added by nandkumar gadkari on 17/04/19-------end=----------
						merrcode = invBean.updateInvallocTrace(invAllocTraceMap,conn);
						if(merrcode != null && merrcode.trim().length() > 0)
						{
							merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
								return merrcode;
						}
						else
						{
							//added by nandkumar gadkari on 17/04/19-------start=----------
							sql = "select alloc_ref from stock where item_code= ? and site_code= ? and lot_no= ? and loc_code=? and lot_sl=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,siteCode);
							pstmt.setString(3,lotNo);
							pstmt.setString(4,locCode);
							pstmt.setString(5,lotSl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								allocRef = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("allocRef["+allocRef+"]");
							System.out.println("allocReflog["+logMsg+"]");
							if(!allocRef.equalsIgnoreCase(logMsg))
							{
								merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
								return merrcode;
							}
							//added by nandkumar gadkari on 17/04/19-------end=----------
							logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tInserted data in INVALLOC_TRACE";
							//strToWrite=strToWrite+logMsg+"\t\r\n";
							//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, logMsg);commented by nandkumar gadkari on 24/12/19
						}
					}	//added by nandkumar gadkari on 24/01/19------------- end------------
					System.out.println("Inside Skipline");
					strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Deallocation of stock for Skip line");// added by nandkumar gadkari on 24/12/19
					continue;
				}
				/*if(skiplot.trim().indexOf("'"+lineNo.trim()) > 0 )
				{
					System.out.println("Inside skiplot");
					continue;
				}*/// commented by nandkumar gadkari on 24/01/19
				

						if(stockOpt != 0)
						{



							if(sordItmQty > 0)
							{
								sql = "select count(*) sordallocCnt from sordalloc where sale_order = ? " +
										" and line_no = ? and exp_lev = ?  	  " +
										" and item_code__ord = ? and item_code = ?  " +
										" and lot_no = ? and lot_sl = ?  and loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,saleOrder);
								pstmt.setString(2,lineNo);
								pstmt.setString(3,expLev);
								pstmt.setString(4,itemCodeOrd);
								pstmt.setString(5,itemCode);
								pstmt.setString(6,lotNo);
								pstmt.setString(7,lotSl);
								pstmt.setString(8,locCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									sordallocCnt = rs.getInt("sordallocCnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								int updSordAllCnt=0;
								if(sordallocCnt == 0)
								{
									System.out.println("ps2qtyStk["+qtyStk+"]"+"allocQty["+allocQty+"]"+"holdQty["+holdQty+"]");
									System.out.println("PS2netQuantity["+netQuantity+"]"+"inputQty["+inputQty+"]"+"netQuantity["+netQuantity+"]");
									System.out.println("PS2totInputQty["+totInputQty+"]"+"sordItmQty["+sordItmQty+"]"+"holdQty["+holdQty+"]");
									System.out.println("totInputQty<<<<<<<<>>>>>>>>>>>"+totInputQty+"]");
									System.out.println("netQuantity<<<<<>>>>>>>"+netQuantity+"]");

									if(qtyStk - allocQty - holdQty <= netQuantity)
									{
										inputQty = qtyStk - allocQty - holdQty;
									}
									else
									{
										inputQty = sordItmQty - totInputQty;
									}

									totInputQty = totInputQty + inputQty ;
									netQuantity = sordItmQty - totInputQty;





									System.out.println("insert query for sordalloc ......");
									System.out.println("saleOrder["+saleOrder+"]"+"lineNo["+lineNo+"]"+"itemCode["+itemCode+"]");
									System.out.println("quantityStduom["+quantityStduom+"]"+"quantity__stduom["+inputQty+"]"+"qty_alloc["+inputQty+"]");

									vaildateData++;	
									sql = "INSERT INTO sordalloc (sale_order, line_no, item_code, quantity, item_code__ord, site_code, unit, status, exp_lev, item_ref, alloc_mode,date_alloc, quantity__stduom, loc_code, lot_no, lot_sl,item_grade, exp_date, unit__std, conv__qty_stduom, qty_alloc, mfg_date, site_code__mfg)";
									sql = sql + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, saleOrder);
									pstmt.setString(2,lineNo);
									pstmt.setString(3, itemCode);
									pstmt.setDouble(4, quantityStduom);//quantity
									pstmt.setString(5, itemCodeOrd);
									pstmt.setString(6, siteCode);
									pstmt.setString(7, unit);
									//pstmt.setString(7, orderUnit);
									pstmt.setString(8, status);
									pstmt.setString(9, expLev);
									pstmt.setString(10, itemRef);
									pstmt.setString(11,"A");
									pstmt.setTimestamp(12, allocDate);
									pstmt.setDouble(13,inputQty);//quantity__stduom
									pstmt.setString(14, locCode);
									pstmt.setString(15, lotNo);
									pstmt.setString(16, lotSl);
									pstmt.setString(17, grade);
									pstmt.setTimestamp(18, expDate);
									pstmt.setString(19, unitStd);
									pstmt.setDouble(20, convQtyStduom);
									pstmt.setDouble(21, inputQty);//qty_alloc
									pstmt.setTimestamp(22, mfgDate);
									pstmt.setString(23, siteCodeMfg);

									insCnt = pstmt.executeUpdate();
									pstmt.close();
									pstmt = null;
									System.out.println(">>>>>>>>insCnt:"+insCnt);
									if(insCnt > 0)
									{
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tInserted data in sordalloc";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Inserted data in sordalloc");
										strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Entry in sordalloc for Stock allocated against the sale order line item");// added by nandkumar gadkari on 24/12/19
										records = records + 1;
										totRecords = totRecords + records;
									}


								}//end of if(sordallocCnt == 0)
								else
								{
									inputQty = sordItmQty;
									sql = "update sordalloc set qty_alloc = qty_alloc + ?," +
											" status = 'D' where sale_order = ? " +
											" and line_no = ? and exp_lev = ? " +
											" and item_code__ord = ? " +
											" and item_code = ? and lot_no = ? " +
											" and lot_sl = ? and loc_code = ? ";
									pstmt = conn.prepareStatement(sql);
									//pstmt1.setDate(1,(java.sql.Date)reschDate);
									//	pstmt.setTimestamp(1,reschDateHdTstm);
									pstmt.setDouble(1,inputQty);
									pstmt.setString(2,saleOrder);
									pstmt.setString(3,lineNo);
									pstmt.setString(4,expLev);
									pstmt.setString(5,itemCodeOrd);
									pstmt.setString(6,itemCode);
									pstmt.setString(7,lotNo);
									pstmt.setString(8,lotSl );
									pstmt.setString(9,locCode);
									updSordAllCnt = pstmt.executeUpdate();
									pstmt.close();
									pstmt = null;
									if(updSordAllCnt > 0)
									{
										logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tUpdated data in sordalloc";
										//strToWrite=strToWrite+logMsg+"\t\r\n";
										//strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Updated data in sordalloc");
										strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, "Update in sordalloc for Stock allocated againts the sale order line item");// added by nandkumar gadkari on 24/12/19
										records = records + 1;
										totRecords = totRecords + records;
									}

								}



								/*sql = "select b.tran_id, b.line_no, (b.quantity - b.dealloc_qty) as pending_dealloc_qty" +
									" from sord_alloc a, sord_alloc_det b   where a.tran_id   	= b.tran_id	 " +
									" and a.cust_code 	= ?  and a.site_code   = ? and a.sale_order " +
									"  is null  and b.item_code 	= ?   and b.loc_code	 	= ? " +
									"   and b.lot_no		= ?  and b.lot_sl		= ? " +
									"  and b.quantity - b.dealloc_qty > 0 ";
							pstmt = conn.prepareStatement(sql);*/
								pstmt1.setString(1,custCode);
								pstmt1.setString(2,siteCode);
								pstmt1.setString(3,itemCodeOrd);
								pstmt1.setString(4,locCode);
								pstmt1.setString(5,lotNo);
								pstmt1.setString(6,lotSl);
								rs = pstmt1.executeQuery();
								if(rs.next())
								{
									tranidSoalloc = rs.getString("tran_id");
									linenoSoalloc = rs.getString("line_no");
									pendingDeallocQty = rs.getDouble("pending_dealloc_qty");

								}
								rs.close();
								rs = null;
								/*pstmt.close();
							pstmt = null;*/


								if(tranidSoalloc != null && tranidSoalloc.trim().length() > 0)
								{
									if(pendingDeallocQty > inputQty)
									{
										pendingDeallocQty = inputQty;

										sql = " update sord_alloc_det set dealloc_qty = dealloc_qty + ? " +
												" where tran_id = ?  and line_no = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setDouble(1,pendingDeallocQty);
										pstmt.setString(2,tranidSoalloc);
										pstmt.setString(3,linenoSoalloc);
										updSordAllDetCnt = pstmt.executeUpdate();
										pstmt.close();
										pstmt = null;

									}
								}

								//

								//System.out.println("inputQty :"+inputQty);
								/*HashMap invAllocTraceMap = new HashMap();
								invAllocTraceMap.put("ref_ser","S-ORD");
								invAllocTraceMap.put("ref_id",saleOrder);
								invAllocTraceMap.put("ref_line",lineNo);
								invAllocTraceMap.put("site_code",siteCode);
								invAllocTraceMap.put("item_code",itemCode);
								invAllocTraceMap.put("loc_code",locCode);
								invAllocTraceMap.put("lot_no",lotNo);
								invAllocTraceMap.put("lot_sl",lotSl);
								invAllocTraceMap.put("alloc_qty",inputQty);
								invAllocTraceMap.put("chg_user",userId);
								invAllocTraceMap.put("chg_term",termId);
								invAllocTraceMap.put("chg_win","W_SORDER");

								//added By Pavan R on 2K18/Feb/12 to store the posted so.[Start]
								sorderpostedList.add(saleOrder);
								//added By Pavan R on 2K18/Feb/12[End]
								InvAllocTraceBean invBean = new InvAllocTraceBean(); 
								merrcode = invBean.updateInvallocTrace(invAllocTraceMap,conn);
								if(merrcode != null && merrcode.trim().length() > 0)
								{
									merrcode = itmDBAccessEJB.getErrorString("VTSTKNOAVL",merrcode,"","",conn);
										return merrcode;
								}
								else
								{
									logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tInserted data in INVALLOC_TRACE";
									strToWrite=strToWrite+logMsg+"\t\r\n";
								}*/ //commented by nandkujmar gadkari on 24/01/19
								sorderpostedList.add(saleOrder);
								totAlloc = totAlloc + inputQty;



							}//end of if(netQuantity > 0)


						}//end of if(stockOpt != 0)
						else // else condition added by Nandkumar Gadkari on 29/06/18 
						{
							records = records + 1;
							totAlloc = totAlloc + sordItmQty;
							System.out.println("stockOpt==0 totAlloc:"+totAlloc);
						}

					}//end of for(StockBean stockBean :stockList) 2nd iterator..........
					System.out.println("vaildateData :"+vaildateData);
					System.out.println("records["+records+"]"+"totAlloc["+totAlloc+"]");
					int updSorditemCnt=0;
					if(records > 0 )
					{
						if(totAlloc > 0)
						{

							sql = "update sorditem set status = 'A', date_alloc  = ? , " +
									" status_date = ? , qty_alloc   = qty_alloc + ? " +
									" where sale_order = ? and line_no    = ? " +
									" and exp_lev	  = ? ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1,allocDate);
							pstmt.setTimestamp(2,allocDate);
							pstmt.setDouble(3,totAlloc);
							pstmt.setString(4,saleOrder);
							pstmt.setString(5,lineNo);
							pstmt.setString(6,expLev);
							updSorditemCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
						}

					}

					totAlloc = 0;
					ratio = "0"; 

					//System.out.println("@@@@@@@@ update sorder commented..........");
					/*int updSordCnt=0;
				if(records > 0  || stockOpt == 0)
				{
					sql = " update sorder set alloc_flag = 'Y' where sale_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					updSordCnt = pstmt.executeUpdate();
					if(updSordCnt > 0)
					{
						logMsg= saleOrder + "\t" + itemCode + "\t"+locCode + "\t" +lotNo+"\t"+ lineNo + "\tupdate sorder alloc_flag 'Y'";
						strToWrite=strToWrite+logMsg+"\t\r\n";
					}
				}*/
					//Start Added by chandrashekar on 13-sep-2016
					//System.out.println("********************************************************");
					//System.out.println("records["+records+"]"+"stockOpt["+stockOpt+"]"+"saleOrder["+saleOrder+"]");
					int updSordCnt=0;
					if(records > 0  || stockOpt == 0 && !isRejected)
					{
						sql = " update sorder set alloc_flag = 'Y' where sale_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,saleOrder);
						updSordCnt = pstmt.executeUpdate();
						if(updSordCnt > 0)
						{
							logMsg= saleOrder + "\t" + "\tupdate sorder alloc_flag 'Y'";
							//strToWrite=strToWrite+logMsg+"\t\r\n";
							//strToWrite=strToWrite+createPostLog(saleOrder, "", "", "", "", "update sorder alloc_flag 'Y'");
							strToWrite = strToWrite +createPostLog(saleOrder, "", "", "", "", "Stock is Allocated against the sale order and allocation flag in sorder is updated.");// added by nandkumar gadkari on 24/12/19
						}
						pstmt.close();
						pstmt = null;
					}
					//End Added by chandrashekar on 13-sep-2016

				}
				pstmt1.close();
				pstmt1 = null;

			}// added by arun pal

			//end of 	for(SordItemBean sordItemBean :sordItemList )...2nd
			/*System.out.println("********************************************************");
			System.out.println("records["+records+"]"+"stockOpt["+stockOpt+"]"+"saleOrder["+saleOrder+"]");
			int updSordCnt=0;
			if(records > 0  || stockOpt == 0)
			{
				sql = " update sorder set alloc_flag = 'Y' where sale_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				updSordCnt = pstmt.executeUpdate();
				if(updSordCnt > 0)
				{
					logMsg= saleOrder + "\t" + "\tupdate sorder alloc_flag 'Y'";
					strToWrite=strToWrite+logMsg+"\t\r\n";
				}
				pstmt.close();
				pstmt = null;
			}*/
			if(totLines - skiplineCnt == 0)
			{
				merrcode = "VTPOSTSKIP";
			}

			if(stockOpt != 0)
			{
				if(totRecords == 0 && schemeNo > 0)
				{
					merrcode = "VTPOST11";
				}
				else if(totRecords == 0 &&  schemeNo == 0)
				{
					merrcode = "VTPOST06"; 
				}
			}

			if(logMsg.trim().length() > 0)
			{
				if("Y".equalsIgnoreCase(createLog))
				{
					//strToWrite = strToWrite + logMsg+"\r\n\r\n";
					fos1.write(strToWrite.getBytes());
				}
			}


			//strToWrite = strToWrite + retString+"\r\n\r\n";
			//System.out.println(">>>Before Write to fos1 strToWrite:"+strToWrite);
			//fos1.write(strToWrite.getBytes());






		}//end of try
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			errorString=e.getMessage();
			//strToWrite=strToWrite + errorString+"\r\n\r\n";
			strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, errorString);
			fos1.write(strToWrite.getBytes());

			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);


		}
		catch(SQLException e)
		{

			//errorLog( String as_reas_code,  String as_reas_detail,  String as_table_name, 
			//String keyfld1,  String keyfld2,  String keyfld3,  String keyfld4,  String keyfld5, 
			//String as_tran_id,  String as_tran_code,  String gencode_fldname,  String mod_name )

			//merrcode = this.event trigger ue_post_log('Error', 'Cannot read allocation detail record for sale order : '  + string(msaleorder) + " Line No: " + mline_no, 'sorditem',msaleorder, '', '', ' ', ' ', msaleorder, 'S-DSP','REAS_CODE','W_POST_ORDER') //atul 21.02.02
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			errorString=e.getMessage();
			//strToWrite=strToWrite + errorString+"\r\n\r\n";
			strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, errorString);
			fos1.write(strToWrite.getBytes());
			errorLog("Error","SQLException : Cannot read allocation detail record for sale order : "+saleOrder + " Line No : "+lineNo ,"sorditem",saleOrder, " ", "" , "", "", saleOrder, "S-DSP","REAS_CODE","W_POST_ORDER",conn);


			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			errorString=e.getMessage();
			//strToWrite=strToWrite + errorString+"\r\n\r\n";
			strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, errorString);
			fos1.write(strToWrite.getBytes());
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
				//conn.close();
			}
			catch (Exception e)
			{
				//writeLog(filePtr,"Exception in process-->"+e,true);
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.out.println("Exception ::"+e.getMessage());
				errorString=e.getMessage();
				//strToWrite=strToWrite + errorString+"\r\n\r\n";
				strToWrite = strToWrite +createPostLog(saleOrder, itemCode, locCode, lotNo, lineNo, errorString);
				fos1.write(strToWrite.getBytes());


				System.out.println(e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);

			}
		}


		// TODO Auto-generated method stub
		return merrcode;
			}





	private String intializingLog(String fileName,String saleOrder) throws ITMException
	{
		String log="";
		String strToWrite = "";
		String currTime = null;
		try
		{
			//System.out.println(">>>In intializingLog() fileName:"+fileName);
			SimpleDateFormat sdf1 = new SimpleDateFormat(e12GenericUtility.getDBDateFormat());
			try
			{
				System.out.println("In intializingLog method");
				currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
				currTime = currTime.replaceAll("-","");
				calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
				//fileName = fileName+currTime+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+".csv";
				//Pavan R on 15mar19[to change the log file name] start
				//fileName= saleOrder.trim().toLowerCase()+ "_post.log";
				fileName= "post_order"+saleOrder.trim()+ ".log";
				System.out.println("fileName: :"+fileName);
				//fos1 = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+fileName);
				//Added by Anagha R on 08/12/2020 for Sales order posting START
				String filePath = CommonConstants.JBOSSHOME + File.separator +"EDI";
				System.out.println("filePath..."+filePath);
				try 
				{
				       File file = new java.io.File(filePath);
				       System.out.println("file..."+file);
				       if (!file.exists()) 
				       {
				           file.mkdir();
				       } 
				       filePath = filePath + File.separator +fileName;
				       System.out.println("filePath...$$$$"+filePath);
				} 
				catch (Exception e) 
				{
				       System.out.println("exception occured while creating file...");
				       e.printStackTrace();
				}
				fos1 = new FileOutputStream(filePath);
				System.out.println("fos1...11"+fos1);
				//Added by Anagha R on 08/12/2020 for Sales order posting END
				
				//logFile="c:\\appl\\itm26\\" + fromSaleOrder.trim().toLowerCase()+ "_post.log";
				//strToWrite="\"TRANID\",\"START TIME\",\"END TIME\",\"STATUS\"\r\n";
				//fos1.write(strToWrite.getBytes());
				log ="IntializingLog_Successesfull";
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
			startDate = new java.util.Date(System.currentTimeMillis());
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			fos1.write(("Post Order process started at: " + startDateStr +"\r\n~").getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Exception []::"+e.getMessage());
			log="IntializingLog_Failed";
			e.printStackTrace();


			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);


		}
		return log;
	}


	public void writeLog(File f,String Msg,boolean flag) throws Exception
	{


		try
		{
			SimpleDateFormat sdf1 = new SimpleDateFormat(e12GenericUtility.getDBDateFormat());
			String currTime = null;
			Calendar calendar = Calendar.getInstance();
			currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
			currTime = currTime.replaceAll("-","");
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));

			if(! CommonConstants.DEBUG_LEVEL.equals("0")) 
			{
				PrintWriter pw = new PrintWriter((new FileOutputStream(f,flag)),flag);
				pw.println("{" + currTime  + " " + calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE) + "}\t" + Msg);
				pw.close();
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
	}

	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}

	public double getDiscount(String plistDisc,Timestamp orderDate,String custCode,String siteCode,String itemCode,String unit,double discMerge,Timestamp plDate,double sordItmQty,Connection conn) throws SQLException, ITMException
	{
		String ls_listtype = "", itemSer = "",sql="";
		double lc_rate=0.0, lc_disc=0.0,rate=0.0,discPerc=0.0;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{


			if(plistDisc.trim().length() > 0)
			{

				sql = "	select case when rate is null then 0 else rate end as rate" +
						" from	pricelist where price_list	= ? and " +
						"	item_code 	= ? and unit = ? " +
						" and	list_type	IN	('M','N') " +
						" and	case when min_qty is null then 0 else min_qty end 	<=	? " +
						" and	((case when max_qty is null then 0 else max_qty end	>=	? ) " +
						" OR  (case when max_qty is null then 0 else max_qty end	=0)) and eff_from <=	?  " +
						" and	valid_upto >=	? ";
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
			discPerc=0;

			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		}
		return discPerc;
	}

	public double calcRate(double rate,double plistDisc) throws ITMException
	{
		try
		{
			rate =  rate - (plistDisc * rate)/100;
			if( rate < 0 )
			{
				rate=0;
			}

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}


		return rate;
	}


	public double convertBox(String itemCode,String unitPack,String unit,double convQtyStduom,Connection conn) throws ITMException
	{
		double convQty = 0.0;
		double convFact=0.0;
		ArrayList convAr = null;
		try
		{
			//convQty = dist.convQtyFactor(unit,unitPack,itemCode, convQtyStduom, conn);
			convAr = dist.getConvQuantityFact(unit, unitPack, itemCode, convQtyStduom, convFact, conn);
			convFact = Double.parseDouble( convAr.get(0).toString() );
			//System.out.println("convFact>>>>"+convFact);
			convQty = Double.parseDouble( convAr.get(1).toString() );
			//System.out.println("convQty>>>>"+convQty);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}



		return convQty;

	}
	public String createPostLog(String saleOrder, String itemCode, String locCode, String lotNo, String lineNo, String logMsg)
	{
		String retString = "";				
		retString = "#" + saleOrder + "~" + itemCode + "~" + locCode + "~" + lotNo + "~" + lineNo + "~" + logMsg;		
		return retString;
	}
	//('P02', ls_postlog ,'sorditem',msaleorder, mline_no, mexp_lev, ' ', ' ', msaleorder, 'S-DSP','REAS_CODE','W_POST_ORDER')
	public String errorLog( String as_reas_code,  String as_reas_detail,  String as_table_name, 
			String keyfld1,  String keyfld2,  String keyfld3,  String keyfld4,  String keyfld5, 
			String as_tran_id,  String as_tran_code,  String gencode_fldname,  String mod_name,Connection conn) throws ITMException
			{
		String errorString = "",reasDetail="",sql="",reasDetailDom="" ,retString="";
		//String gencodeFldname = "",reasCode="",modName="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Connection connCP = null;

		String ls_reas_detail = "", merrcode = "";
		////// added arun 
		ITMDBAccessEJB itmDBAccessEJB = null;
		boolean connStatus=false;
		try
		{
			//23feb19 Pavan R[commented and changed to avoid unnecessary conn creation]
			//connCP = getConnection() ;
			if(conn  == null)
			{				
				conn = getConnection() ;
				conn.setAutoCommit(false);
				connStatus = true;
			}
			//23feb19 Pavan R end
			//if(postLogYn)
			//{
			//System.out.println("call errorLog!!!!!!!....");
			//System.out.println("as_reas_detail.trim().length()>>>"+as_reas_detail.trim().length());
			BaseLogger.log("3", null, null, "call errorLog!!!!!!!....as_reas_detail.trim().length()>>>"+as_reas_detail.trim().length());
			if(as_reas_detail.trim().length() == 0)
			{
				sql = "   select descr from gencodes where " +
						" fld_name = ? and    ltrim(rtrim(fld_value))= ? and " +
						"    ltrim(rtrim(mod_name)) = ? ";
				//pstmt = connCP.prepareStatement(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,gencode_fldname);
				pstmt.setString(2,as_reas_code);
				pstmt.setString(3,mod_name);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_reas_detail  = rs.getString("descr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(ls_reas_detail == null)
				{
					ls_reas_detail="Error";
				}
				as_reas_detail = ls_reas_detail;
			}
			/*System.out.println("as_reas_detail :"+as_reas_detail);
			System.out.println("as_table_name["+as_table_name+"]"+"keyfld1["+keyfld1+"]"+"keyfld2["+keyfld2+"]");
			System.out.println("keyfld3["+keyfld3+"]"+"keyfld4["+keyfld4+"]"+"keyfld5["+keyfld5+"]");
			System.out.println("as_tran_id["+as_tran_id+"]"+"as_tran_code["+as_tran_code+"]"+"gencode_fldname["+gencode_fldname+"]");
			System.out.println("mod_name:"+mod_name);*/
			BaseLogger.log("3", null, null, "as_reas_detail :"+as_reas_detail);
			BaseLogger.log("3", null, null, "as_table_name["+as_table_name+"]"+"keyfld1["+keyfld1+"]"+"keyfld2["+keyfld2+"]"); 
			BaseLogger.log("3", null, null, "keyfld3["+keyfld3+"]"+"keyfld4["+keyfld4+"]"+"keyfld5["+keyfld5+"]");
			BaseLogger.log("3", null, null, "as_tran_id["+as_tran_id+"]"+"as_tran_code["+as_tran_code+"]"+"gencode_fldname["+gencode_fldname+"]");
			BaseLogger.log("3", null, null, "mod_name:"+mod_name);
			Log log = new Log();
			log.setReasCode(as_reas_code);
			log.setReasDetail(as_reas_detail);
			log.setTableName(as_table_name);
			log.setKeyfld1(keyfld1);
			log.setKeyfld2(keyfld2);
			log.setKeyfld3(keyfld3);
			log.setKeyfld4(keyfld4);
			log.setKeyfld5(keyfld5);
			log.setTranId(as_tran_id);
			log.setTranCode(as_tran_code);
			log.setGencodeFldname(gencode_fldname);
			log.setModName(mod_name);
			
			//merrcode=postLog(log,conn); commented  and added erroLogSordItme list by nandkumar gadkari on 10/05/19]
			erroLogSordItme.add(log); 
			//merrcode=postLog(log,connCP);

		}
		catch(Exception e)
		{
			BaseLogger.log("0", null, null, "Exception in PostOrderProcess :: errorLog()::"+e.getMessage());
			//System.out.println(e.getMessage());			
			e.printStackTrace();
			throw new ITMException(e);
		}
		//23feb19 Pavan R[to close open resources]
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
				if( conn != null && !conn.isClosed() && connStatus ) 
				{					
					conn.close();
					conn = null;
				}											
			}
			catch(Exception e)
			{
				BaseLogger.log("0", null, null, "Exception in PostOrderProcess :: errorLog():: finally::"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
			
		}
		//23feb19 Pavan R end

		return merrcode;
			}


	/*-----------------------------------------------------------------------------------*/

	public String postLog(Log log,Connection connCP) throws SQLException, ITMException
	{
		String msg="";
		String ls_table = "", ls_reas_code = "" , ls_reas_detail = "" , ls_errcode = "";
		String keyfld1 = "" , keyfld2 = "" , keyfld3 = "",sql="";
		String keyfld4 = "" , keyfld5 = "";
		String ls_date = "", merrcode ="";
		Date currentDate = new Date();
		PreparedStatement pstmt=null;
		boolean connStatus = false;

		int updCnt=0;


		try
		{	
			//23feb19 Pavan R[commented and changed to avoid unnecessary conn creation]			
			if(connCP  == null)
			{							
				//connCP = getConnection();
				connCP = getConnection() ;
				connCP.setAutoCommit(false);
				connStatus = true;
			}
			//23feb19 Pavan R end				
			BaseLogger.log("3", null, null, "start calling postLog........");
			//System.out.println(" start calling postLog........");
			ls_date = sdf.format(currentDate);
			//	System.out.println("ls_date :"+ls_date);
			ls_table = checkNull(log.getTableName());
			ls_reas_code = log.getReasCode();
			ls_reas_detail = log.getReasDetail();
			keyfld1=log.getKeyfld1();
			keyfld2=log.getKeyfld2();
			keyfld3=log.getKeyfld3();
			keyfld4=log.getKeyfld4();
			keyfld5=log.getKeyfld5();
			/*System.out.println("ls_table["+ls_table+"]"+"ls_reas_code["+ls_reas_code+"]"+"ls_reas_detail"+ls_reas_detail+"]");
			System.out.println("keyfld1["+keyfld1+"]"+"keyfld2["+keyfld2+"]"+"keyfld3["+keyfld3+"]");
			System.out.println("keyfld4["+keyfld4+"]"+"keyfld5["+keyfld5+"]");*/
			//added by nandkumar gadkari on 08/05/19----------start-------------
			if(ls_reas_detail != null && ls_reas_detail.length() > 120)
			{
				ls_reas_detail=ls_reas_detail.substring(0, 119);
			}
			//added by nandkumar gadkari on 08/05/19----------end-------------
			if("sorditem".equalsIgnoreCase(ls_table))
			{	
				//remove commented by arun p 01-12-17 start
				//System.out.println("Insdie sorditem  <<<<<<<>>>>>>>["+sql+"]");
				if(keyfld2 == null && keyfld3 == null)
				{
					//System.out.println("Update reason code and reason deatils <<<<<<<>>>>>>>["+sql+"]");
					sql = "update sorditem set reas_code = ? ," +
							" reas_detail = ? where sale_order = ? ";
				}
				else
				{
					//System.out.println("Update reason code and reason deatils  and line no and exp_value<<<<<<<>>>>>>>["+sql+"]");
					BaseLogger.log("3", null, null, "Update reason code and reason deatils  and line no and exp_value<<<<<<<>>>>>>>["+sql+"]");
					sql = "update sorditem  set reas_code 	= ? , " +
							" reas_detail = ? where sale_order = ? " +
							" and	(	(line_no	  = ?) or (? ='') ) and	" +
							" (  (exp_lev	  = ?) or (? ='') ) ";
				}

				pstmt = connCP.prepareStatement(sql);

				if(keyfld2 == null && keyfld3 == null)
				{
					//System.out.println("@@@@@@@@@@@ keyfld2 and keyfld3  value "+keyfld1+"]");
					pstmt.setString(1,ls_reas_code);
					pstmt.setString(2,ls_reas_detail);
					pstmt.setString(3,keyfld1);
				}
				else
				{
					//System.out.println("@@@@@@@@@@@ keyfld2 and keyfld3  value "+keyfld2+"]");
					BaseLogger.log("3", null, null, "@@@@@@@@@@@ keyfld2 and keyfld3  value "+keyfld2+"]");
					pstmt.setString(1,ls_reas_code);
					pstmt.setString(2,ls_reas_detail);
					pstmt.setString(3,keyfld1);
					pstmt.setString(4,keyfld2);
					pstmt.setString(5,keyfld4);
					pstmt.setString(6,keyfld3);
					pstmt.setString(7,keyfld5);
				}
				//remove commented by arun p 01-12-17 start ended 
				 
				/*sql = "update sorditem set reas_code = ? ," +
						" reas_detail = ? where sale_order = ? ";
				pstmt =connCP.prepareStatement(sql);
				pstmt.setString(1,ls_reas_code);
				pstmt.setString(2,ls_reas_detail);
				pstmt.setString(3,keyfld1);*/ 
				// commented by arun pal 01-12-17
				updCnt = pstmt.executeUpdate();
				pstmt.close();//[pstmt closed and nulled by Pavan R]
				pstmt = null;

				//	System.out.println("updCnt :"+updCnt);

			}

			//merrcode = gbf_sorder_status_log(keyfld1,ls_date,5,keyfld2,keyfld3,ls_reas_code,ls_reas_detail);
			merrcode = sorderStatusLog(keyfld1,ls_date,5,keyfld2,keyfld3,ls_reas_code,ls_reas_detail,connCP);

			if(merrcode.trim().length() ==0)
			{
				merrcode = "";
				connCP.commit();
				
			}
			else
			{
				connCP.rollback();
			}

		}
		catch(Exception e)
		{
			connCP.rollback();
			//return merrcode;

			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		}//23feb19 Pavan R[to close open resources]
		finally 
		{
			try
			{						
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}				
				if( connCP != null && !connCP.isClosed() && connStatus ) 
				{					
					connCP.close();
					connCP = null;
				}											
			}
			catch(Exception e)
			{
				BaseLogger.log("0", null, null, "Exception in PostOrderProcess :: postLog():: finally::"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}			
		}
		//23feb19 Pavan R end


		return merrcode;
	}

	public String checkScheme(String itemCode, String as_order_type, String as_cust_code, String siteCode,
			String stateCode, String countCode, Timestamp tranDate,Connection conn) throws ITMException
			{
		String 	ls_token = "",sql="" ,sql1="" ,ls_scheme_code = "" , ls_curscheme = "" , ls_sql = "", ls_cust_scheme_code = "", ls_apply_cust_list = "", ls_noapply_cust_list ="";
		String ls_applicableordtypes = "" , ls_prevscheme = "", curscheme = "",curschemeCode="",prevscheme="",schemeCode="",ls_apply_cust = "", ls_noapply_cust = "";
		boolean	lb_proceed = false;
		long ll_schcnt=0;
		PreparedStatement pstmt=null,pstmt1=null,pstmt2=null;
		ResultSet rs=null,rs1=null;
		String applyCustList="",noApplyCustList="",applicableordtypes="";

		try
		{


			sql = " select a.scheme_code from scheme_applicability a, scheme_applicability_det  b" +
					"  where a.scheme_code	= b.scheme_code and a.item_code  = ? " + 	
					" and a.app_from <= ? and a.valid_upto >= ?  and " +
					" (b.site_code 	= ?	 or b.state_code = ? or b.count_code = ? )";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);
			pstmt.setTimestamp(2,tranDate);
			pstmt.setTimestamp(3,tranDate);
			pstmt.setString(4,siteCode);
			pstmt.setString(5,stateCode);
			pstmt.setString(6,countCode);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				curscheme = rs.getString("scheme_code");



				sql1 = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list," +
						" (case when noapply_cust_list is null then ' ' else noapply_cust_list end) noapply_cust_list , order_type " +
						" from scheme_applicability  where scheme_code = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1,curscheme);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{

					applyCustList = rs1.getString("apply_cust_list");
					noApplyCustList = rs1.getString("noapply_cust_list");
					applicableordtypes = rs1.getString("order_type");

					if("NE".equalsIgnoreCase(as_order_type) && applicableordtypes==null)
					{
						continue;
					}
					else if (applicableordtypes != null && applicableordtypes.trim().length() > 0)
					{
						lb_proceed = false;
						ls_token = dist.getToken(ls_applicableordtypes,",");
						if(as_order_type.trim().equalsIgnoreCase(ls_token.trim()))
						{
							lb_proceed = true;
							break;
						}
					}

					prevscheme	= schemeCode;	 
					schemeCode = curscheme ;
					if(applyCustList != null && applyCustList.trim().length() > 0)
					{
						/*ls_apply_cust = dist.getToken(ls_apply_cust_list,",");
						if(ls_apply_cust.trim().equalsIgnoreCase(as_cust_code))
						{
							schemeCode = curscheme;
							curschemeCode = curscheme ;
						}*/

						schemeCode=null;
						System.out.println("lsSchemeCode:::::::1"+schemeCode);
						//lsCustCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						System.out.println("CustCode"+as_cust_code+":::");
						String lsApplyCustListArr[] = applyCustList.split(",");
						ArrayList<String> applyCustList1= new ArrayList<String>(Arrays.asList(lsApplyCustListArr));
						if(applyCustList1.contains(as_cust_code.trim()))
						{
							System.out.println("Inside applycustList");
							schemeCode = curscheme;
							curschemeCode = curscheme ;
							System.out.println("SchemeCode::"+schemeCode+" CustSchemeCode::"+curscheme);

							//					break;
						}

					}

					if(noApplyCustList != null && noApplyCustList.length() > 0)
					{
						ls_noapply_cust = dist.getToken(ls_noapply_cust_list,",");
						if(ls_noapply_cust.trim().equalsIgnoreCase(as_cust_code.trim()))
						{
							ls_scheme_code=null;
							break;
						}

					}
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(schemeCode != null)
				{
					ll_schcnt ++;
				}
				else if(ll_schcnt == 1 )
				{
					schemeCode	= prevscheme;
				}
				System.out.println("ll_schcnt::"+ll_schcnt);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(!(ll_schcnt == 1))
			{
				schemeCode =null;
			}
			else if(curschemeCode.trim().length() > 0)
			{
				System.out.println("Inside curschemeCode");
				schemeCode = curschemeCode;
				System.out.println("schemeCode"+schemeCode);
			}

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("");
		return schemeCode;
			}

	public String sorderStatusLog(String as_sale_order, String as_log_date, 
			int ai_event_type, String as_line_no, String as_exp_lev, String as_reas_code,
			String as_ref_descr,Connection conn) throws SQLException, ITMException
			{

		//	Connection conn = null;
		String merrcode="",sql="",ls_site_code="",ls_win="",ls_key="",userid="",termId="";
		String tranId="",keyCol="",tranSer="",ls_edi_option="";
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		Date currentDate = new Date();
		int cnt = 0;
		try
		{
			//System.out.println("call sorderStatusLog!.......");
			//xmlString1
			//System.out.println("xmlString1>>"+xmlString1);
			//System.out.println("chgUser["+chgUser+"]"+"chgTerm["+chgTerm+"]");
			/*ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB("DriverITM");
			conn.setAutoCommit(false);*/

			sql = " select site_code " +
					" from sorder where sale_order = ?   ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,as_sale_order);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_site_code  = rs.getString("site_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//System.out.println("ls_site_code :"+ls_site_code);
			ls_win = "w_sorder_stat_log" ;


			sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_win);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_key  = rs.getString("key_string");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//System.out.println("ls_key["+ls_key+"]"+"keyCol["+keyCol+"]"+"tranSer["+tranSer+"]");

			if(ls_key == null)
			{
				sql = " select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,as_sale_order);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_key  = rs.getString("key_string");
					keyCol = rs.getString("TRAN_ID_COL");
					tranSer = rs.getString("REF_SER");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//System.out.println("ls_key!!["+ls_key+"]"+"keyCol!!["+keyCol+"]"+"tranSer!!["+tranSer+"]");
			}


			//	ls_tran_id = gf_gen_key_nvo_log(lds_hdr_edit,'S-LOG','tran_id',ls_key,ltr__sqlca)

			TransIDGenerator tg = new TransIDGenerator(xmlString1, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, ls_key, conn);
			//System.out.println("tranId :"+tranId);
			//ld_today = datetime(today())
			//ldt_log_date = datetime(date(as_log_date))
			java.sql.Timestamp ldt_log_date = java.sql.Timestamp.valueOf(e12GenericUtility.getValidDateString(sdf.format(currentDate).toString() , e12GenericUtility.getApplDateFormat(),e12GenericUtility.getDBDateFormat()) + " 00:00:00.0");
			//java.sql.Timestamp ldt_log_date =java.sql.Timestamp.valueOf(sdf.format(currentDate).toString() + " 00:00:00.0");
			sql = "INSERT INTO sorder_stat_log (TRAN_ID,SORDER,LOG_DATE,EVENT_TYPE,LINE_NO,EXP_LEV,REAS_CODE,REF_DESCR,CHG_DATE,CHG_USER,CHG_TERM)";
			sql = sql + "values(?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2,as_sale_order);
			pstmt.setTimestamp(3, ldt_log_date);
			pstmt.setInt(4, ai_event_type);
			pstmt.setString(5, as_line_no);
			pstmt.setString(6, as_exp_lev);
			pstmt.setString(7, as_reas_code);
			pstmt.setString(8, as_ref_descr);
			pstmt.setTimestamp(9, ldt_log_date);
			pstmt.setString(10, chgUser);
			pstmt.setString(11,chgTerm);
			cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;//[pstmt set to null by Pavan R]
			/*System.out.println("cnt :"+cnt);
			System.out.println("tranId!!! :"+tranId);*/
			//if lds_hdr_edit.retrieve(ls_tran_id) > 0 then
			if(cnt > 0)
			{
				if(tranId.trim().length() > 0)
				{
					sql = " select edi_option " +
							"  from transetup where tran_window = 'w_sorder_stat_log'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_edi_option  = rs.getString("edi_option");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//	System.out.println("ls_edi_option:"+ls_edi_option);
					if("1".equalsIgnoreCase(ls_edi_option))
					{
						//						merrcode = nvo_functions_adv.nf_create_edi_multi('w_sorder_stat_log', &
						//								 lds_hdr_edit.describe("datawindow.syntax") &
						//						 + '~r' + lds_hdr_edit.describe("datawindow.syntax.data"), &
						//								 'A', '2', 1, '' + '~r' + '', '', '', '', '', '', ltr__sqlca)
					}
					else if("2".equalsIgnoreCase(ls_edi_option))
					{
						//						nvo_functions_adv.post nf_create_edi_multi('w_sorder_stat_log', &
						//								 lds_hdr_edit.describe("datawindow.syntax") &
						//							 + '~r' + lds_hdr_edit.describe("datawindow.syntax.data"), &
						//							 'A', '2', 1, '' + '~r' + '', '', '', '', '', '', ltr__sqlca)
					}
				}
				else
				{
					//merrcode = 'DS000'+ string(ltr__sqlca.sqldbcode)
				}
			}

		}
		catch(Exception e)
		{
			merrcode = "DS000" +  "/t" + e.getMessage();
			conn.rollback();
			//return ls_errcode;

			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		}
		System.out.println("merrcode>>>>"+merrcode);
		return merrcode;

			}

	public int createTransaction(String inifile)
	{

		Connection conn = null;
		try
		{

			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);

			if(conn != null)
			{
				postLogYn=false;
				return -1;
			}
			else
			{
				postLogYn=true;
				return 0;
			}


		}
		catch(Exception e)
		{
			postLogYn=false;
			return -1;
		}

	}


	public boolean invRetstr(String refSer,String invStat,Connection conn) throws ITMException
	{
		long cnt=0;
		boolean isSkip=false;
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql = "select count(*) count from inv_restr where inv_stat = ?  and " +
					"  ref_ser  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,invStat);
			pstmt.setString(2,refSer);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt("count");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt > 0)
			{
				isSkip=true;
			}
			else if(cnt == 0)
			{
				isSkip=false;
			}




		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}




		return isSkip;


	}
	private String gbfScheme(String saleOrder, String lineNo, String lotSl, String locGroup, String itemCode,Connection conn) throws ITMException
	{
		Timestamp sysDate=null;
		String sql="",sql1="",sql2="",mitemCode="",mitemCodeOrd="",msiteCode="",lineNoStr="",trackShelfLife="";
		String mchkDateStr="";
		PreparedStatement pstmt1=null,pstmt=null,pstmt2=null;
		ResultSet rs1=null,rs=null,rs2=null;
		Double mquantity=0.0,mminShelfLife=0.0;
		double mmin_shelf_life=0.0,stkQty=0.0,mqty=0.0,mmodqty=0.0;
		double lc_ratio=10000.0,li_ratio=0.0;
		Timestamp mchkDate=null;
		DistCommon discmn=new DistCommon();
		try
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			String DB = CommonConstants.DB_NAME;
			//System.out.println("DB  ==========>>>>>"+DB);
			lineNo="   "+lineNo;
			//System.out.println("---"+lineNo+"---");
			lineNo = lineNo.substring(lineNo.length() - 3);
			sql1="select distinct(item_code) as item_code , sum(quantity - qty_desp) as qty, " +
					"item_code__ord, site_code " +
					" from sorditem where sale_order  = ?    and" +
					"  line_no = ? and (quantity - qty_desp) >0 and" +
					" line_type = 'I' and item_code = ? " +
					" group by item_code, item_code__ord, site_code " +
					"order by item_code, item_code__ord, site_code";
			pstmt1= conn.prepareStatement(sql1);
			pstmt1.setString(1, saleOrder);
			pstmt1.setString(2, lineNo);
			pstmt1.setString(3, itemCode);
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) 
			{
				mitemCode = rs1.getString("item_code");
				mquantity = rs1.getDouble("qty");
				mitemCodeOrd = rs1.getString("item_code__ord");
				msiteCode = rs1.getString("site_code");
				mminShelfLife = 0.0;

				sql2=" select sum(a.quantity - a.alloc_qty - case when a.hold_qty is null then 0 else a.hold_qty end ) as qty " +
						" from stock a, invstat b, location c where c.inv_stat = b.inv_stat  and c.loc_code = a.loc_code " +
						"and a.item_code = '"+mitemCode+"'  and a.site_code = '"+msiteCode+"' " +
						"and a.quantity   > 0 and b.available = 'Y' ";
				if(lotSl != null && checkNull(lotSl).trim().length()>0)
				{
					sql2=sql2+" and a.lot_sl = '"+lotSl+"' ";
				}
				if(locGroup != null &&  checkNull(locGroup).trim().length()>0)
				{
					sql2=sql2+" and c.loc_group = '"+locGroup+"' ";
				}

				sql="select track_shelf_life  from item where item_code =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mitemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					trackShelfLife = rs.getString("track_shelf_life");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql="select min_shelf_life from sorditem where sale_order = ?  " +
						"and line_no = ?  and line_type = 'I'  and item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				pstmt.setString(2, lineNo);
				pstmt.setString(3, mitemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mminShelfLife = rs.getDouble("min_shelf_life");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				mchkDate=discmn.CalcExpiry(sysDate,mminShelfLife+1);
				//  System.out.println("mchkDate>>>>"+mchkDate);
				if("Y".equalsIgnoreCase(trackShelfLife) && mminShelfLife >0)
				{
					if("db2".equalsIgnoreCase(DB))
					{
						SimpleDateFormat sdt = new SimpleDateFormat("yyyy-mm-dd");
						if(mchkDate !=null)
						{
							mchkDateStr = sdt.format(mchkDate.getTime());	
						}
						sql2=sql2+" and ( a.exp_date > TIMESTAMP('"+mchkDateStr+" 00:00:00.000"+"') or a.exp_date is null) ";
					}else
					{
						SimpleDateFormat sdt = new SimpleDateFormat("dd-MMM-yyyy");
						if(mchkDate !=null)
						{
							mchkDateStr = sdt.format(mchkDate.getTime());	
						}
						sql2=sql2+" and ( a.exp_date > '"+mchkDateStr+"' or a.exp_date is null) ";
					}

				}
				pstmt2=conn.prepareStatement(sql2);
				rs2=pstmt2.executeQuery();
				while(rs2.next())
				{
					stkQty=rs2.getDouble("qty");

					sql="SELECT SUM(qty_per) as   mqty  FROM bomdet  WHERE ( bom_code = ? ) AND  ( item_code = ? )";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mitemCodeOrd);
					pstmt.setString(2, mitemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						mqty = rs.getDouble("mqty");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					if(stkQty<mqty)
					{
						break;
					}
					if(stkQty < mquantity)
					{
						if(mqty>0)
						{
							sql="Select mod(?,?) as mmodqty from dual";
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1, stkQty);
							pstmt.setDouble(2, mqty);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								mmodqty = rs.getDouble("mmodqty");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							li_ratio = ((stkQty - mmodqty) / mqty);
							if (lc_ratio >  li_ratio)
							{
								lc_ratio = li_ratio ;
							}

						}
					}

				}
				rs2.close();
				rs2=null;
				pstmt2.close();
				pstmt2=null;

			}
			rs1.close();
			rs1= null;
			pstmt1.close();
			pstmt1= null;




		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return Double.toString(lc_ratio);
	}

	private String checkNullAndTrim(String inputVal)
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}
	//Added by Pavan R on 25/JAN/2K18 [Start]
		private  String getErrorNew(String retString,String Code,Connection conn)  throws ITMException, Exception
		{
			String mainStr ="";

			try
			{
				PreparedStatement pstmt=null;
				ResultSet rs = null;
				String msgDesc = "";
				String sql = "SELECT MSG_DESCR FROM MESSAGES WHERE MSG_NO = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, checkNull(retString));
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					msgDesc = checkNull(rs.getString(1));
				}
				rs.close();
				rs= null;
				pstmt.close();
				pstmt = null;
				
				String errString = "";
				errString =  new ITMDBAccessEJB().getErrorString("",Code,"","",conn);
				String begPart = errString.substring(0,errString.indexOf("<message>")+9);
				String endDesc = errString.substring(errString.indexOf("</description>"));
				mainStr= begPart+"Invalid Data"+"</message><description><![CDATA[";
				mainStr= mainStr+"retString "+retString+" "+msgDesc+"]]>"+endDesc;
				System.out.println("mainStr:::::::::::::::::: "+mainStr);
				begPart = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			return mainStr;
		}
		//Added by Pavan R on 25/JAN/2K18 [End]
		//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[START]
		private String sendMailonConfirm(String invoiceId, String fromCustCode , String templateCode ,UserInfoBean userInfo,Connection conn ) throws SQLException, ITMException
		{
			PreparedStatement pstmt=null;
			ResultSet rs = null;
			String SendEmailOnNotify = "",sql="";
			String errString = "";
			String toAddr = "",ccAddr = "",bccAddr = "",subject = "",body = "",templateName = "",attachObjLinks = "",attachments = "";
			String xmlString = "",reportType = "PDF",usrLevel = "";
			System.out.println("invoiceId in send mail:" +invoiceId);
			sql = " select email_notify from customer where cust_code =  ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, fromCustCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				SendEmailOnNotify = checkNull(rs.getString("email_notify"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if("Y".equalsIgnoreCase(SendEmailOnNotify))
			{
				System.out.println("After confirm Calling SendEmail");
				sql = "select  SEND_TO ,COPY_TO ,BLIND_COPY ,SUBJECT , BODY_TEXT , MAIL_DESCR ,ATTACH_TEXT ,ATTACH_TYPE  from MAIL_FORMAT  WHERE FORMAT_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, templateCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					toAddr = checkNull(rs.getString("SEND_TO"));
					ccAddr = checkNull(rs.getString("COPY_TO"));
					bccAddr = checkNull(rs.getString("BLIND_COPY"));
					subject = checkNull(rs.getString("SUBJECT"));
					body = checkNull(rs.getString("BODY_TEXT"));
					templateName = checkNull(rs.getString("MAIL_DESCR"));
					attachments	 = checkNull(rs.getString("ATTACH_TEXT"));
					attachObjLinks = checkNull(rs.getString("ATTACH_TYPE"));
					//confirmed = checkNull(rs.getString("confirmed"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Before confirm Calling SendEmail with attachment");
				errString =  TransactionEmailTempltEJB.sendMail("invoice",userInfo,toAddr, ccAddr,bccAddr,subject,body,"","",invoiceId,attachments,"true",templateName,templateCode,"");
				//errString =  TransactionEmailTempltEJB.sendMail("invoice",userInfo,toAddr, ccAddr,bccAddr,subject,body,"","",invoiceId,attachments,"",templateName,templateCode,"");
				System.out.println("After confirm Calling SendEmail with attachment" +errString);

				if( errString != null && errString.trim().length() > 0 )
				{
					String begPart = errString.substring(errString.indexOf("<STATUS>")+8,errString.indexOf("</STATUS>"));
					System.out.println("<STATUS> ::: " +begPart);
					if("N".equalsIgnoreCase(begPart))
					{
						return errString;
					}
					else
					{
						errString="";
					}
				}
			}
			//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[End]
			System.out.println("mainStr final error:::::::::::::::::: "+errString);
			return errString;
			
		}
}



class Log
{
	private String reasCode=null;
	private String reasDetail=null;
	private String tableName=null;
	private String keyfld1=null;
	private String keyfld2=null;
	private String keyfld3=null;
	private String keyfld4=null;
	private String keyfld5=null;
	private String tranId=null;
	private String tranCode=null;
	private String gencodeFldname=null;
	private String modName=null;




	public String getReasCode() {
		return reasCode;
	}
	public void setReasCode(String reasCode) {
		this.reasCode = reasCode;
	}
	public String getReasDetail() {
		return reasDetail;
	}
	public void setReasDetail(String reasDetail) {
		this.reasDetail = reasDetail;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getKeyfld1() {
		return keyfld1;
	}
	public void setKeyfld1(String keyfld1) {
		this.keyfld1 = keyfld1;
	}
	public String getKeyfld2() {
		return keyfld2;
	}
	public void setKeyfld2(String keyfld2) {
		this.keyfld2 = keyfld2;
	}
	public String getKeyfld3() {
		return keyfld3;
	}
	public void setKeyfld3(String keyfld3) {
		this.keyfld3 = keyfld3;
	}
	public String getKeyfld4() {
		return keyfld4;
	}
	public void setKeyfld4(String keyfld4) {
		this.keyfld4 = keyfld4;
	}
	public String getKeyfld5() {
		return keyfld5;
	}
	public void setKeyfld5(String keyfld5) {
		this.keyfld5 = keyfld5;
	}
	public String getTranId() { 
		return tranId;
	}
	public void setTranId(String tranId) {
		this.tranId = tranId;
	}
	public String getTranCode() {
		return tranCode;
	}
	public void setTranCode(String tranCode) {
		this.tranCode = tranCode;
	}
	public String getGencodeFldname() {
		return gencodeFldname;
	}
	public void setGencodeFldname(String gencodeFldname) {
		this.gencodeFldname = gencodeFldname;
	}
	public String getModName() {
		return modName;
	}
	public void setModName(String modName) {
		this.modName = modName;
	}



}