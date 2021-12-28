/*
    Window Name : w_sordalloc
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;

//import webitm4.*;
//import SessionManager.*;
import org.omg.CORBA.ORB;
import org.w3c.dom.*;

import java.util.Properties;

import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.adv.StockAllocPrc;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.XMLType;

import javax.xml.rpc.ParameterMode;
import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

//public class StockAllocationPrcEJB extends ProcessEJB implements SessionBean
public class StockAllocationPrc extends ProcessEJB implements StockAllocationPrcLocal,StockAllocationPrcRemote //SessionBean
{
	String siteCode = null;
	String itemSerFr = null;
	String itemSerTo = null;
	String custCodeFr = null;
	String custCodeTo = null;
	String custCode = null;
	String DefaultQtyFlag= null;
	String itemCodeFr = null;
	String itemCodeTo = null;

	String saleOrderFr = null;
	String saleOrderTo = null;
	String postOrderFg = null;
	String 	sql = null;
	String sDateTo = null;
	String sDateFr = null;
	double balStockQty = 0;
	double holdQty = 0;

	java.sql.Timestamp dateTo =  null;
	java.sql.Timestamp dueDate =  null;
	java.sql.Timestamp dateFr =  null;
	HashMap itemCodeMap = new HashMap();
	ArrayList saleOrderArr = new ArrayList();
	ArrayList custCodeArr = new ArrayList();
	ArrayList dueDateArr = new ArrayList();

	InvAllocTrace invallocTrace = new InvAllocTrace();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	//ConnDriver connDriver = new ConnDriver();
	CommonConstants commonConstants = new CommonConstants();
	String chgUser = null;
	String chgTerm = null;
	
	boolean stkAllocateFlag  = false;
	String shiperSizeError = "";
	boolean errFlag = false;
	ArrayList list2 = new ArrayList();
	int elements2 = 0;

	/*public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("StockAllocationPrcEJB ejbCreate called.........");

		}
		catch (Exception e)
		{
			System.out.println("Exception :StockAllocationPrcEJB :ejbCreate :==>"+e);
			throw new CreateException();
		}
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}
	 */
	public String process() throws RemoteException,ITMException
	{
		return "";
	}

	//getData Method
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;
		System.out.println("getData(xmlString,xmlString2,windowName,xtraParams CALLED..."+ xmlString);
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 				
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
			System.out.println("@@@@@@@@@@@RTR STR:- ["+rtrStr+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception :StockAllocationPrcEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			/*rtrStr = e.getMessage();*/ //Commented By Mukesh Chauhan on 05/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return rtrStr; 
	}

	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errCode = "";
		String errString = "";
		String getDataSql= "" ;
		String sql= "" ;
		String resultString = "";
		String sql1 ="",custCodeSord = "",waveType = "",	activeAllow="";
		String resrvLoc = "",casePickLoc = "",activePickLoc = "" ,deepStoreLoc = "" ,partialResrvLoc = "";//added by kunal on 15/NOV/13
		String orderByStkStr = "" , disOpt = "",batchId="";
		int waveType1 = 0,cnt=0;
		double pendingQty = 0, minSelfLife = 0;
		//Added by wasim on 20-APR-17 for DDUK changes
		double maxShelfLife = 0;
		Connection conn = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null ;
		Statement st = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();
		double pendQty = 0;
		double allocQty = 0;	
		boolean bappend = false ;
		boolean checkLocation = false;
		boolean isActives = false;
		SimpleDateFormat sdf=null;
		DistCommon dComm = new DistCommon();
		String subSQL="";
		retTabSepStrBuff = new StringBuffer("<?xml version = \"1.0\"?>");
		retTabSepStrBuff.append("<DocumentRoot>");
		retTabSepStrBuff.append("<description>").append("Datawindow Root").append("</description>");
		retTabSepStrBuff.append("<group0>");
		retTabSepStrBuff.append("<description>").append("Group0 description").append("</description>");
		retTabSepStrBuff.append("<Header0>");

		try
		{
			sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			ConnDriver connDriver = new ConnDriver();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			DecimalFormat df = new DecimalFormat("0.000");

			//added by kunal on 15/NOV/13 DI3ESUN009
			resrvLoc  = checkNull(dComm.getDisparams("999999","RESERV_LOCATION",conn)); // marked
			casePickLoc  = checkNull(dComm.getDisparams("999999","CASE_PICK_INVSTAT",conn));
			activePickLoc  = checkNull(dComm.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn));
			deepStoreLoc = checkNull(dComm.getDisparams("999999","DEEP_STORE_INVSTAT",conn));
			partialResrvLoc = checkNull(dComm.getDisparams("999999","PRSRV_INVSTAT",conn));	
			if(resrvLoc.trim().length() == 0 || resrvLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				resrvLoc = "";
			}
			if(casePickLoc.trim().length() == 0 || casePickLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				casePickLoc = "";
			}
			if(activePickLoc.trim().length() == 0 || activePickLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				activePickLoc = "";
			}
			if(deepStoreLoc.trim().length() == 0 || deepStoreLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				deepStoreLoc = "";
			}
			if(partialResrvLoc.trim().length() == 0 || partialResrvLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				partialResrvLoc = "";
			}

			System.out.println("CASE_PICK_INVSTAT="+casePickLoc+"     ACTIVE_PICK_INVSTAT="+activePickLoc+"       RESERV_LOCATION="+resrvLoc+"     DEEP_STORE_INVSTAT="+deepStoreLoc+"     PARTIAL RESERVE LOC="+partialResrvLoc);
			if(resrvLoc.trim().length() > 0 || casePickLoc.trim().length() > 0 || activePickLoc.trim().length() > 0 || deepStoreLoc.trim().length() > 0  || partialResrvLoc.trim().length() > 0)
			{
				checkLocation = true;
			}
			System.out.println("checkLocation="+checkLocation);
			//added by kunal on 15/NOV/13 DI3ESUN009 end

			siteCode = genericUtility.getColumnValue("site_code",headerDom);
			if ( siteCode == null || siteCode.trim().length() == 0 )
			{
				siteCode = "";
				System.out.println("Site Code From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			} 
			System.out.println("SITE CODE "+ siteCode);
			postOrderFg = genericUtility.getColumnValue("post_order_flag",headerDom);
			if(postOrderFg == null || postOrderFg.trim().length() == 0)
			{
				postOrderFg = "";
				System.out.println("Post Order From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			itemSerFr = genericUtility.getColumnValue("item_ser__from",headerDom);
			if ( itemSerFr == null || itemSerFr.trim().length() == 0 )
			{
				itemSerFr = "";
				System.out.println("Item Series From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("Item Ser FR "+ itemSerFr);
			itemSerTo = genericUtility.getColumnValue("item_ser__to",headerDom);
			if ( itemSerTo == null || itemSerTo.trim().length() == 0 )
			{
				itemSerTo = "";
				System.out.println("Item Series To is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			itemCodeFr = genericUtility.getColumnValue("item_code__from",headerDom);
			if ( itemCodeFr == null || itemCodeFr.trim().length() == 0 )
			{
				itemCodeFr = "";
				System.out.println("Item Code From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("Item Code FR "+ itemCodeFr);
			itemCodeTo = genericUtility.getColumnValue("item_code__to",headerDom);
			if ( itemCodeTo == null || itemCodeTo.trim().length() == 0 )
			{
				itemCodeTo = "";
				System.out.println("Item Code To is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("Item Ser To"+itemSerTo);
			custCodeFr = genericUtility.getColumnValue("cust_code__from",headerDom);
			if ( custCodeFr == null || custCodeFr.trim().length() == 0 )
			{
				custCodeFr = "";
				System.out.println("Cust Code From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("custCodeFr"+ custCodeFr);
			custCodeTo = genericUtility.getColumnValue("cust_code__to",headerDom); 
			if ( custCodeTo == null || custCodeTo.trim().length() == 0 )
			{
				custCodeTo = "";
				System.out.println("Cust Code To is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}  
			System.out.println("custCodeTo"+ custCodeTo);      
			saleOrderFr = genericUtility.getColumnValue("sale_order__from",headerDom);
			if ( saleOrderFr == null || saleOrderFr.trim().length() == 0 )
			{
				saleOrderFr = "";
				System.out.println("Sale Order From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}

			saleOrderTo = genericUtility.getColumnValue("sale_order__to",headerDom);         
			if ( saleOrderTo == null || saleOrderTo.trim().length() == 0 )
			{
				saleOrderTo = "";
				System.out.println("Sale Order To is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			sDateFr = genericUtility.getColumnValue("due_date__from",headerDom);
			if ( sDateFr == null || sDateFr.trim().length() == 0 )
			{
				sDateFr = "";
				System.out.println("Date From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			sDateFr = genericUtility.getValidDateString(sDateFr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dateFr = java.sql.Timestamp.valueOf(sDateFr + " 00:00:00");

			sDateTo = genericUtility.getColumnValue("due_date__to",headerDom);            
			if ( sDateTo == null || sDateTo.trim().length() == 0 )
			{
				sDateTo = "";
				System.out.println("Date To is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			sDateTo = genericUtility.getValidDateString(sDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dateTo = java.sql.Timestamp.valueOf(sDateTo + " 00:00:00");

			DefaultQtyFlag = genericUtility.getColumnValue("default_qty_flag",headerDom);
			if(DefaultQtyFlag == null || DefaultQtyFlag.trim().length() == 0)
			{
				DefaultQtyFlag = "";
				System.out.println("Default Qty flag is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			/**
			 * VALLABH KADAM
			 * Validation for 
			 * if BATCH_ID is not null check exist in SORD_ALLOC 
			 * */
			batchId=genericUtility.getColumnValue("batch_id",headerDom);
			System.out.println("Batch id at validation :- "+batchId);
			if(batchId!=null && batchId.trim().length()>0)
			{
				System.out.println("*****IN if VALIDATION *****");
				sql="select count(*) as cnt from sord_alloc where batch_id=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,batchId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
//					waveType = rs2.getString("wave_type");
					cnt=rs.getInt("cnt");
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Batch id cnt at validation :- "+cnt);
				if(cnt<=0){
					errString = itmDBAccessEJB.getErrorString("","VTBCHIDINV","","",conn);
					return errString;
				}
			}
			//added by Kunal on 18/NOV/13
			disOpt = checkNull( genericUtility.getColumnValue("dis_opt",headerDom));
			System.out.println("Display option="+disOpt);
			if( "A".equalsIgnoreCase(disOpt))
			{
				subSQL = " AND (((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC >= 0 ) "  
						+" OR (FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )";	 
				
			}
			else
			{
				subSQL = " AND (((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC > 0 ) "  
						+" OR (FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )";	 
						
			}
			getDataSql = "SELECT SORDER.CUST_CODE, CUSTOMER.CUST_NAME,SORDDET.LINE_NO, "
					+"SORDER.SALE_ORDER,SORDER.DUE_DATE,SORDITEM.ITEM_CODE,"
					+"ITEM.DESCR,SORDITEM.QUANTITY,"
					//Changed by Manoj dtd 18/06/2013 to get Pending Quantity in Std Unit
					//+"(SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDUOM) - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC PENDING_QUANTITY,"
					//Changed by Manoj dtd 12/11/2013 to get Pending Quantity in Std Unit
					+"(SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC PENDING_QUANTITY,"
					+"SORDITEM.QTY_ALLOC,SORDDET.PACK_INSTR,"
					+"SORDER.SITE_CODE__SHIP,SORDITEM.EXP_LEV , "
					+"CASE WHEN SORDDET.HOLD_FLAG IS NULL THEN 'N' ELSE SORDDET.HOLD_FLAG END, "// ADDED BY AKHILESH FOR NEW COLUNM
					+" ITEM.GTIN_CASE,ITEM.GTIN_UNIT  "   //added by Kunal on 22/10/12
					+",SORDDET.UNIT,SORDDET.UNIT__STD,SORDDET.CONV__QTY_STDUOM,SORDDET.QUANTITY__STDUOM , "
					+" SORDER.PROM_DATE,SORDER.CUST_CODE__DLV,CUSTA.CUST_NAME AS CUST_CODE__DLV_NAME,SORDER.STATE_CODE__DLV, CUSTOMER.GROUP_CODE,CUSTB.CUST_NAME AS PCUST_NAME, " //added by kunal on 02/07/13
					+" SORDER.CUST_PORD,SORDER.PORD_DATE " //added by kunal on 02/07/13
					+"FROM SORDDET,SORDER,SORDITEM,CUSTOMER,ITEM ,customer custa,customer custb " 
					+"WHERE ( SORDER.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
					+"( SORDITEM.SALE_ORDER = SORDER.SALE_ORDER ) AND "
					+"( SORDDET.LINE_NO = SORDITEM.LINE_NO ) AND "
					+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
					+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE )  "
					+" and ( sorder.cust_code__dlv = custa.cust_code ) " 
					+" and ( customer.group_code = custb.cust_code ) " 

				+"AND ( SORDDET.SITE_CODE = SORDITEM.SITE_CODE ) AND  "

				+" SORDER.SITE_CODE = ? AND "
				+" ITEM.ITEM_SER >=? AND "
				+" ITEM.ITEM_SER <=? AND "  
				+" CUSTOMER.CUST_CODE >=? AND "
				+" CUSTOMER.CUST_CODE <=? AND "
				
				+" SORDER.SALE_ORDER >= ? AND "
				+" SORDER.SALE_ORDER <= ? AND " 
				+" SORDER.DUE_DATE >=  ?  AND" 
				+" SORDER.DUE_DATE <= ?  AND "
				+" SORDITEM.ITEM_CODE >= ? AND "
				+" SORDITEM.ITEM_CODE <= ?  " 
				+" AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
				+" AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end  IN ('P','H') " ;
				 //change done by Kunal on 22/10/12 as per S Manoharan instruction ,consider Hold status
			
				if( "A".equalsIgnoreCase(disOpt))   // added condition by cpatil on 17/12/13
				{
					getDataSql = getDataSql + "AND (SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC >= 0 ";	
				} 
				else
				{
					getDataSql = getDataSql + "AND (SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC > 0 ";
				}
				/**
				 *VALLABH KADAM Add condition 7/APR/15 
				 *for Scheduler [AutoAllocOrdrSch.java]
				 * Req Id:- [D15ASUN001]
				 * */
				
				if(windowName.equalsIgnoreCase("w_sordalloc_sh")){
					System.out.println("In if win name===='w_sordalloc_sh'");
					getDataSql = getDataSql +" AND CASE WHEN CUSTOMER.AUTO_STK_ALLOC IS NULL THEN 'Y' ELSE CUSTOMER.AUTO_STK_ALLOC end = 'Y' ";							
				}
				/**
				 * VALLABH KADAM Add condition 7/APR/15 END
				 * */
				
				getDataSql = getDataSql + " AND SORDITEM.LINE_TYPE = 'I' "
				+ " ORDER BY SORDER.CUST_CODE,SORDER.SALE_ORDER,SORDITEM.ITEM_CODE__ORD "; 

			pstmt = conn.prepareStatement(getDataSql);

			pstmt.setString(1,siteCode);
			pstmt.setString(2,itemSerFr);
			pstmt.setString(3,itemSerTo);
			pstmt.setString(4,custCodeFr);
			pstmt.setString(5,custCodeTo);
			pstmt.setString(6,saleOrderFr);
			pstmt.setString(7,saleOrderTo);
			pstmt.setTimestamp(8,dateFr);
			pstmt.setTimestamp(9,dateTo);
			pstmt.setString(10,itemCodeFr);
			pstmt.setString(11,itemCodeTo);
			st = conn.createStatement();
			rs = pstmt.executeQuery();

			if(rs.next())
			{

				do
				{

					System.out.println("Processing Item ....." + rs.getString(6) );
					//added by ritesh  0n 7/8/13  for request DI3ESUN009 START
					sql1 = " select wave_type from customer where cust_code = ? ";
					pstmt2 = conn.prepareStatement(sql1);
					pstmt2.setString(1,rs.getString("CUST_CODE__DLV"));
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						waveType = rs2.getString("wave_type");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					sql1 = " select case when ACTIVE_PICK_ALLOW is null then 'Y' else ACTIVE_PICK_ALLOW end as ACTIVE_PICK_ALLOW from wave_type where wave_type = ? ";
					pstmt2 = conn.prepareStatement(sql1);
					pstmt2.setString(1,waveType);
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						activeAllow = rs2.getString("ACTIVE_PICK_ALLOW");
					}
					else
					{
						activeAllow = "Y";
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;	
					//added by ritesh 0n 7/8/13  for request DI3ESUN009 END

					/* comment by kunal on 24/dec/13  
					if(itemCodeMap.containsKey(rs.getString(6)) )
					{						
						balStockQty = Double.parseDouble(itemCodeMap.get(rs.getString(6)).toString());
						bappend = false ;

					}
					else
					{*/

						if(checkLocation)
						{

							sql =" SELECT SORDER.CUST_CODE, CUSTOMER.CUST_NAME,SORDDET.LINE_NO, "
									+"SORDER.SALE_ORDER,SORDER.DUE_DATE,SORDITEM.ITEM_CODE,"
									+"ITEM.DESCR,SORDITEM.QUANTITY,"
									+"((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC) PENDING_QUANTITY,"
									+"SORDITEM.QTY_ALLOC,SORDDET.PACK_INSTR,"
									+"SORDER.SITE_CODE,SORDITEM.EXP_LEV, "
									+"SORDER.PART_QTY AS PART_QTY, SORDER.SINGLE_LOT AS SINGLE_LOT, SORDITEM.MIN_SHELF_LIFE AS MIN_SHELF_LIFE "//Gulzar 5/13/2012
									+" ,SORDER.ALLOC_FLAG AS ALLOC_FLAG , WAVE_TYPE.MASTER_PACK_ALLOW , WAVE_TYPE.ACTIVE_PICK_ALLOW , WAVE_TYPE.STOCK_TO_DOCK_ALLOW "		   
									+"FROM SORDDET,SORDER,SORDITEM,CUSTOMER,ITEM , WAVE_TYPE "
									//Changed by sumit 20/08/12 getting column data end.
									+"WHERE ( SORDER.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
									+"( SORDITEM.SALE_ORDER = SORDER.SALE_ORDER ) AND "
									+"( SORDDET.LINE_NO = SORDITEM.LINE_NO ) AND "
									+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
									+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND  "
									+"( SORDDET.SITE_CODE = SORDITEM.SITE_CODE ) AND  "
									//Changed by sumit on 20/08/12 join customer with wave_type
									+"( CUSTOMER.WAVE_TYPE = WAVE_TYPE.WAVE_TYPE(+)) AND "
									//+" SORDER.SITE_CODE = ? AND "							   
									+" SORDER.SALE_ORDER = ? "
									+" AND SORDITEM.LINE_NO = ? "
									+"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
									+"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end = 'P' "
									//Changed by sumit on 12/09/12 considering hold_flag from sorddet 
									+" AND CASE WHEN SORDDET.HOLD_FLAG IS NULL THEN 'N' ELSE SORDDET.HOLD_FLAG end <> 'Y'"
									//Chnaged by Rohan on 11/07/12 revert Changes of Manual Stock Allocation
									//Changed by Rohan on 22/06/12
									//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
									//+"AND (SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 OR SORDER.ALLOC_FLAG='M' )"
									//Changed by sumit on 13/08/12 as per manual stock allocation start.
									//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
								//	+" AND (((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC > 0 ) "  
								//	+" OR (FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )"                                                           // both line commented by cpatil on 17/12/13	 
									+"AND SORDITEM.LINE_TYPE = 'I' ";
							sql=sql+subSQL;
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1,rs.getString(4));
							pstmt1.setString(2, rs.getString(3));
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								pendingQty = rs1.getInt("PENDING_QUANTITY");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							HashMap itemVolMap = getItemVoumeMap( rs.getString(6), "", conn);
							double packSize = (Double)itemVolMap.get("PACK_SIZE");
							System.out.println(" pendingQty = "+pendingQty+"  packSize ="+packSize);
							if((pendingQty % packSize) > 0)
							{
								isActives = true;
								orderByStkStr = " AND LOCATION.INV_STAT IN(?,?,?,?,?) ";
							}
							else
							{
								isActives = false;
								orderByStkStr = " AND LOCATION.INV_STAT IN(?,?,?,?) ";
							}
						}
						minSelfLife = 0;
						maxShelfLife = 0;
						//Changed by wasim on 20-APR-17 for DDUK changes
						//sql =" SELECT MIN_SHELF_LIFE  FROM SORDITEM WHERE SALE_ORDER = ?  AND LINE_NO = ?  AND EXP_LEV = ? "; 
						sql =" SELECT MIN_SHELF_LIFE,MAX_SHELF_LIFE  FROM SORDITEM WHERE SALE_ORDER = ?  AND LINE_NO = ?  AND EXP_LEV = ? "; 
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,rs.getString(4));
						pstmt1.setString(2, rs.getString(3));
						pstmt1.setString(3, rs.getString(13));
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							minSelfLife= rs1.getDouble("MIN_SHELF_LIFE");
							//Added by wasim on 20-APR-17 for DDUK changes
							maxShelfLife= rs1.getDouble("MAX_SHELF_LIFE");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						sql1 ="SELECT SUM(STOCK.QUANTITY - case when STOCK.ALLOC_QTY is null then 0 else STOCK.ALLOC_QTY end - case when STOCK.HOLD_QTY is null then 0 else STOCK.HOLD_QTY end  ),SUM(case when STOCK.HOLD_QTY is null then 0 else STOCK.HOLD_QTY end ) " 
								+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
								+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
								+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
								+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
								+"AND INVSTAT.AVAILABLE = 'Y' "
								+"AND STOCK.ITEM_CODE = ? "
								+"AND STOCK.SITE_CODE = ? " 
								+"AND (STOCK.QUANTITY - case when STOCK.ALLOC_QTY is null then 0 else STOCK.ALLOC_QTY end - case when STOCK.HOLD_QTY is null then 0 else STOCK.HOLD_QTY end ) > 0   "
								+"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' ) "; //added by kunal on 20/NOV/13 SAME COND. FOR GETDATA AND PROCESS 

						if("N".equalsIgnoreCase(activeAllow))//added by kunal on 20/NOV/13 SAME COND. FOR GETDATA AND PROCESS
						{
							sql1 = sql1.concat(" AND LOCATION.INV_STAT NOT IN(?,?) ");
						}
						//Added by manoj dtd 09/08/2013 to implement FEFO concept  
						
						//Changed by wasim on 20-APR-17 for DDUK changes [START]
						//sql1= sql1+" AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
						System.out.println("@@MAX_SHELF_LIFE["+maxShelfLife+"]");
						if(maxShelfLife > 0)
						{	
							sql1= sql1+" AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
						}
						//Changed by wasim on 20-APR-17 for DDUK changes [END]

						// 01/11/12 manoharan preparedstatement used instead of statement
						//rs1 = st.executeQuery(sql);

						if(checkLocation) //added by kunal on 15/NOV/13  DI3ESUN009 
						{
							sql1=sql1+orderByStkStr;
						}
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1,rs.getString(6) );
						pstmt1.setString(2,rs.getString(12));
						//Changed by wasim on 20-APR-17 for DDUK changes [START]
						//pstmt1.setDouble(3,minSelfLife);
						if(maxShelfLife > 0)
						{	
							pstmt1.setDouble(3,minSelfLife);
						}
						//Changed by wasim on 20-APR-17 for DDUK changes [END]
						if("N".equalsIgnoreCase(activeAllow))
						{
							pstmt1.setString(3,activePickLoc);
							pstmt1.setString(4,partialResrvLoc);
							//Changed by wasim on 20-APR-17 for DDUK changes [START]
							//pstmt1.setDouble(5,minSelfLife);
							if(maxShelfLife > 0)
							{
								pstmt1.setDouble(5,minSelfLife);
							}
							//Changed by wasim on 20-APR-17 for DDUK changes [END]
							if(checkLocation) //added by kunal on 15/NOV/13  DI3ESUN009
							{
								//Changed by wasim on 20-APR-17 for DDUK changes [START]
								/*pstmt1.setString(6,resrvLoc);
								pstmt1.setString(7,casePickLoc);
								pstmt1.setString(8,activePickLoc);
								pstmt1.setString(9,deepStoreLoc);
								if(isActives)
								{
									pstmt1.setString(10,partialResrvLoc);
								}*/
								if(maxShelfLife > 0)
								{
									pstmt1.setString(6,resrvLoc);
									pstmt1.setString(7,casePickLoc);
									pstmt1.setString(8,activePickLoc);
									pstmt1.setString(9,deepStoreLoc);
									if(isActives)
									{
										pstmt1.setString(10,partialResrvLoc);
									}
								}
								else
								{
									pstmt1.setString(5,resrvLoc);
									pstmt1.setString(6,casePickLoc);
									pstmt1.setString(7,activePickLoc);
									pstmt1.setString(8,deepStoreLoc);
									if(isActives)
									{
										pstmt1.setString(9,partialResrvLoc);
									}
								}
								//Changed by wasim on 20-APR-17 for DDUK changes [END]
							}
						}
						else
						{
							if(checkLocation) //added by kunal on 15/NOV/13  DI3ESUN009
							{
								//Changed by wasim on 20-APR-17 for DDUK changes [START]
								/*pstmt1.setString(4,resrvLoc);
								pstmt1.setString(5,casePickLoc);
								pstmt1.setString(6,activePickLoc);
								pstmt1.setString(7,deepStoreLoc);
								if(isActives)
								{
									pstmt1.setString(8,partialResrvLoc);
								}*/
								if(maxShelfLife > 0)
								{
									pstmt1.setString(4,resrvLoc);
									pstmt1.setString(5,casePickLoc);
									pstmt1.setString(6,activePickLoc);
									pstmt1.setString(7,deepStoreLoc);
									if(isActives)
									{
										pstmt1.setString(8,partialResrvLoc);
									}
								}
								else
								{
									pstmt1.setString(3,resrvLoc);
									pstmt1.setString(4,casePickLoc);
									pstmt1.setString(5,activePickLoc);
									pstmt1.setString(6,deepStoreLoc);
									if(isActives)
									{
										pstmt1.setString(7,partialResrvLoc);
									}
								}
								//Changed by wasim on 20-APR-17 for DDUK changes [END]
							}

						}
						rs1 = pstmt1.executeQuery();

						if (rs1.next())
						{
							bappend = true;
							balStockQty = rs1.getDouble(1);
							holdQty = rs1.getDouble(2); //added by Kunal on 22/10/12 
							if (balStockQty > 0)
							{
								//itemCodeMap.put(rs.getString(6),new Double(rs1.getDouble(1)));
								//change done by kunal change key of itemmap on 24/dec/13
								itemCodeMap.put((rs.getString(6).trim()+"@"+rs.getString(12).trim()),new Double(rs1.getDouble(1)));
							}
							else
							{
								if ("A".equalsIgnoreCase(disOpt))// To display all records, added by Kunal on 18/NOV/13
								{
									//itemCodeMap.put(rs.getString(6),new Double(rs1.getDouble(1)));
									//change done by kunal change key of itemmap\
									itemCodeMap.put((rs.getString(6).trim()+"@"+rs.getString(12).trim()),new Double(rs1.getDouble(1)));
								}
							}
							System.out.println("Bal stk qty for item ....." + rs.getString(6) + " is " + balStockQty+"  hold qty="+holdQty);
							 
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					//}

					//if (balStockQty > 0) 
					System.out.println("kunal test map::"+itemCodeMap.toString());	
					System.out.println("kunal test::"+itemCodeMap.containsKey((rs.getString(6).trim()+"@"+rs.getString(12).trim())));
					if(itemCodeMap.containsKey((rs.getString(6).trim()+"@"+rs.getString(12).trim())) )//change the key of itemmap
					{
						
						retTabSepStrBuff.append("<Detail2>");
						// COLUMN SEQ: CHANGE BY RITESH ON 11/FEB/2014 FOR SCM issue tracker point # 195-N
						//SALE_ORDER
						//retTabSepStrBuff.append(rs.getString(4)).append("\t");
						retTabSepStrBuff.append("<sale_order>").append("<![CDATA[" + rs.getString(4) +"]]>").append("</sale_order>");

						//DLV CUST_CODE NAME
						retTabSepStrBuff.append("<cust_name>").append("<![CDATA[" + checkNull(rs.getString("CUST_CODE__DLV_NAME")) +"]]>").append("</cust_name>");
						//LINE_NO
						//retTabSepStrBuff.append(rs.getString(3)).append("\t");
						retTabSepStrBuff.append("<line_no>").append("<![CDATA[" + rs.getString(3) +"]]>").append("</line_no>");

						//ITEM_CODE
						//retTabSepStrBuff.append(rs.getString(6)).append("\t");
						retTabSepStrBuff.append("<item_code>").append("<![CDATA[" + rs.getString(6) +"]]>").append("</item_code>");
						//DESCR
						//retTabSepStrBuff.append(rs.getString(7)).append("\t");
						retTabSepStrBuff.append("<item_descr>").append("<![CDATA[" + rs.getString(7) +"]]>").append("</item_descr>");
						//STOCK QTY
						retTabSepStrBuff.append("<stock_quantity>").append("<![CDATA[" + df.format(balStockQty) +"]]>").append("</stock_quantity>");
						//Hold Qty
						//retTabSepStrBuff.append(holdQty).append("\t");// added by Kunal on 22/10/12
						retTabSepStrBuff.append("<hold_qty>").append("<![CDATA[" + holdQty +"]]>").append("</hold_qty>");
						//active_pick_allow
						retTabSepStrBuff.append("<active_pick_allow>").append("<![CDATA[" + activeAllow +"]]>").append("</active_pick_allow>");
						//QUANTITY
						//retTabSepStrBuff.append(rs.getDouble(8)).append("\t");
						retTabSepStrBuff.append("<quantity>").append("<![CDATA[" + rs.getDouble(8) +"]]>").append("</quantity>");
						
						//QTY__STD
						retTabSepStrBuff.append("<quantity__stduom>").append("<![CDATA[" + rs.getString("QUANTITY__STDUOM") +"]]>").append("</quantity__stduom>");
						//PENDING_QUANTITY
						//retTabSepStrBuff.append(rs.getDouble(9)).append("\t");
						//retTabSepStrBuff.append("<pending_quantity>").append("<![CDATA[" + rs.getDouble(9) +"]]>").append("</pending_quantity>");
						//Manoj dtd 18/06/2013 Pending Quantity in standard unit
						retTabSepStrBuff.append("<pending_quantity>").append("<![CDATA[" + (rs.getDouble(9)) +"]]>").append("</pending_quantity>");
					
						//STOCK_QUANTITY
						pendQty = rs.getDouble(9);	
						
						//QTY_ALLOC
						if(balStockQty >= pendQty)
						{
							allocQty = pendQty;
							balStockQty -= pendQty;
						}
						else
						{
							allocQty = balStockQty;
							balStockQty = 0;
						}
						if(DefaultQtyFlag.equals("Y"))
						{
							//retTabSepStrBuff.append(allocQty).append("\t");
							retTabSepStrBuff.append("<qty_alloc>").append("<![CDATA[" + (allocQty) +"]]>").append("</qty_alloc>");
						}
						else
						{
							//retTabSepStrBuff.append("0").append("\t");
							retTabSepStrBuff.append("<qty_alloc>").append("<![CDATA[" + 0 +"]]>").append("</qty_alloc>");
						}
						//qty allocated												
						//retTabSepStrBuff.append(rs.getDouble(10)).append("\t");// ADDED BY AKHILESH
						retTabSepStrBuff.append("<qty_allocated>").append("<![CDATA[" + (rs.getDouble(10)) +"]]>").append("</qty_allocated>");
						//Hold Flag
						//retTabSepStrBuff.append(rs.getString(14) == null?"N":rs.getString(14)).append("\t");// ADDED BY AKHILESH
						retTabSepStrBuff.append("<hold_flag>").append("<![CDATA[" + (rs.getString(14) == null? " ":rs.getString(14)) +"]]>").append("</hold_flag>");
						//PROM_DATE
						if(rs.getTimestamp("PROM_DATE") != null)
						{
							retTabSepStrBuff.append("<prom_date>").append("<![CDATA[" + sdf.format(rs.getTimestamp("PROM_DATE")) +"]]>").append("</prom_date>");
						}
						else
						{
							retTabSepStrBuff.append("<prom_date>").append("<![CDATA[]]>").append("</prom_date>");
						}
						
						//DUE_DATE
						//retTabSepStrBuff.append(rs.getTimestamp(5)).append("\t");
						if(rs.getTimestamp(5) != null)
						{
							retTabSepStrBuff.append("<due_date>").append("<![CDATA[" + sdf.format(rs.getTimestamp(5)) +"]]>").append("</due_date>");
						}
						else
						{
							retTabSepStrBuff.append("<due_date>").append("<![CDATA[]]>").append("</due_date>");
						}
						dueDate = rs.getTimestamp(5);

						//DLV CUST_CODE STATE 
						retTabSepStrBuff.append("<state_code__dlv>").append("<![CDATA[" + checkNull(rs.getString("STATE_CODE__DLV")) +"]]>").append("</state_code__dlv>");

						//change done by kunal on 27/NOV/13 ,change the display order 
											
						//DLV CUST_CODE
						retTabSepStrBuff.append("<cust_code__dlv>").append("<![CDATA[" + checkNull(rs.getString("CUST_CODE__DLV")) +"]]>").append("</cust_code__dlv>");
						
						//UNIT
						retTabSepStrBuff.append("<unit>").append("<![CDATA[" + rs.getString("UNIT") +"]]>").append("</unit>");
						//UNIT__STD
						retTabSepStrBuff.append("<unit__std>").append("<![CDATA[" + rs.getString("UNIT__STD") +"]]>").append("</unit__std>");
						//UNIT__STD
						retTabSepStrBuff.append("<conv__qty_stduom>").append("<![CDATA[" + rs.getString("CONV__QTY_STDUOM") +"]]>").append("</conv__qty_stduom>");
						
		
						

								

						//***** check itemhashmap wheather current item_code already exists
						// if not add to itemhashmap and get the stock as follows and set in 
						//  tabdelimited string else nothing is to be done just consider the stock as 0



						///////////////////////////////////////////////////////////////////////////////////					
						// alloc_qty to be set based on stock availability
						// it should not be more than pending quantity
						// the balance quantity to be updated in itemCodeMap
						// and to be used for the item's next iteration


						//PARENT CUST_CODE
						retTabSepStrBuff.append("<group_code>").append("<![CDATA[" + checkNull(rs.getString("GROUP_CODE")) +"]]>").append("</group_code>");
						//PARENT CUST_CODE NAME
						retTabSepStrBuff.append("<cust_name_1>").append("<![CDATA[" + checkNull(rs.getString("PCUST_NAME")) +"]]>").append("</cust_name_1>");

						//PARENT CUST_CODE NAME
						retTabSepStrBuff.append("<cust_pord>").append("<![CDATA[" + checkNull(rs.getString("CUST_PORD")) +"]]>").append("</cust_pord>");

						//PO DATE
						if(rs.getTimestamp("PORD_DATE") != null)
						{
							retTabSepStrBuff.append("<pord_date>").append("<![CDATA[" + sdf.format(rs.getTimestamp("PORD_DATE")) +"]]>").append("</pord_date>");
						}
						else
						{
							retTabSepStrBuff.append("<pord_date>").append("<![CDATA[]]>").append("</pord_date>");
						}

						//CUST_CODE
						//retTabSepStrBuff.append(rs.getString(1)).append("\t");
						retTabSepStrBuff.append("<cust_code>").append("<![CDATA[" + rs.getString(1) +"]]>").append("</cust_code>");
						//CUST_NAME
						//retTabSepStrBuff.append(rs.getString(2)).append("\t");
						retTabSepStrBuff.append("<cust_name_2>").append("<![CDATA[" + rs.getString(2) +"]]>").append("</cust_name_2>");

						//Gtin Case
						//retTabSepStrBuff.append(rs.getString(15)== null?" ":rs.getString(15)).append("\t");// added by Kunal on 22/10/12
						retTabSepStrBuff.append("<gtin_case>").append("<![CDATA[" + (rs.getString(15) == null? " ":rs.getString(15)) +"]]>").append("</gtin_case>");
						//Gtin Unit
						//retTabSepStrBuff.append(rs.getString(16) == null?" ":rs.getString(16)).append("\t");// added by Kunal on 25/10/12
						retTabSepStrBuff.append("<gtin_unit>").append("<![CDATA[" + (rs.getString(16) == null? " ":rs.getString(16)) +"]]>").append("</gtin_unit>");


						// this line has to be commented later
						// as this will be a input from the user							
						itemCodeMap.put((rs.getString(6).trim()+"@"+rs.getString(12).trim()), new Double(balStockQty));
						//PACK_INSTR
						//retTabSepStrBuff.append(rs.getString(11) == null? " ":rs.getString(11)).append("\t");
						retTabSepStrBuff.append("<pack_instr>").append("<![CDATA[" + (rs.getString(11) == null? " ":rs.getString(11)) +"]]>").append("</pack_instr>");

						//SITE_CODE
						//retTabSepStrBuff.append(rs.getString(12) == null?" ":rs.getString(12)).append("\t");
						retTabSepStrBuff.append("<site_code>").append("<![CDATA[" + (rs.getString(12) == null? " ":rs.getString(12)) +"]]>").append("</site_code>");
						//EXP_LEV
						//retTabSepStrBuff.append(rs.getString(13) == null?" " :rs.getString(13) ).append("\t");
						retTabSepStrBuff.append("<exp_lev>").append("<![CDATA[" + (rs.getString(13) == null? " ":rs.getString(13)) +"]]>").append("</exp_lev>");



						/*if (bappend == true)
						{
							//retTabSepStrBuff.append(balStockQty).append("\t");
							retTabSepStrBuff.append("<stock_quantity>").append("<![CDATA[" + balStockQty +"]]>").append("</stock_quantity>");
						}
						else
						{
							//retTabSepStrBuff.append("0").append("\t");
							retTabSepStrBuff.append("<stock_quantity>").append("<![CDATA[" + 0 +"]]>").append("</stock_quantity>");
						}*/


						retTabSepStrBuff.append("</Detail2>");

						//retTabSepStrBuff.append("\n");
					}


				}while(rs.next());
				retTabSepStrBuff.append("</Header0>");
				retTabSepStrBuff.append("</group0>");
				retTabSepStrBuff.append("</DocumentRoot>");
				resultString = retTabSepStrBuff.toString();
				System.out.println("ResultString....." + resultString);
				pstmt.clearParameters();	
			}

			else
			{
				errCode ="VTNOREC2";
			}
			if (!errCode.equals(""))
			{
				resultString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				System.out.println("resultString: "+resultString);
			}

			rs.close();
		}
		catch (SQLException e)
		{
			System.out.println("SQLException :StockAllocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :StockAllocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			cleanup();

			try
			{
				retTabSepStrBuff = null;
				if(conn != null)
				{					
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		return resultString;	
	}

	//Process Method

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;

		//GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println("below genericUtility--------------->>>>>>>>>");
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
				System.out.println("HeaderDoM :-["+xmlString+"]");
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
				System.out.println("DetailDoM :-["+xmlString2+"]");
			}

			retStr = process(headerDom, detailDom, windowName, xtraParams);

		}
		catch (Exception e)
		{
			System.out.println("Exception :StockAllocationPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			/*retStr = e.getMessage();*/ // Commented By Mukesh Chauhan on 05/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("retStr:::"+retStr);
		return retStr;

	}
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("detailDom------------------->"+detailDom);
		String childNodeName = "";
		String errCode = "";
		//String siteCode = this.siteCode; 			// 21/08/12 manoharan get the shipping site
		String siteCode="";
		String postOrderFg = this.postOrderFg;
		String saleOrder = null;
		String expLev = null ;
		String itemCode = null;
		String lineNo = null;
		String unit = null;
		String locCode = null;
		String lotSl= null;
		String lotNo = null;
		String locDescr = null;
		String itemShDescr = null,batchId=null,keyString="",keyCol="",tranSer1="";
		double allocQty = 0 , pendingQty = 0;
		double quantity = 0;
		double qtyAvailAlloc = 0;
		String stockQuantity = "";
		String errString = "";
		String holdFlag = "";
		String activeAllow = "";
		String getDataSql= null;
		String insertSql = null;
		String updateSql = null;

		Connection conn = null;

		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;

		int updCnt = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		PreparedStatement pstmt = null;
		Statement st = null; 
		int elements = 0,elements1 = 0,cnt=0,elements3 = 0,elements4 = 0;
		elements2 = 0;
		ArrayList list = new ArrayList();
		ArrayList list1 = new ArrayList();
//		ArrayList list2 = new ArrayList();
		list2 =  new ArrayList();
		ArrayList list3 = new ArrayList();
		ArrayList list4 = new ArrayList();
		boolean flag  = false,isFlag = true,allocqtyflag = false,pendingOrderFlag=false;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String sql = "",shipStatus="";
//		ArrayList<Object> itemList =  new ArrayList<Object> ();
		HashMap<String,HashMap> map = new HashMap<String,HashMap>();
		HashMap<String,String> lineNoMap = null;
//		ArrayList itemList  =  new ArrayList();;
		String itemList  = "";
		ArrayList<String> sorderList = new ArrayList<String>();
		stkAllocateFlag = false;
		shiperSizeError = "";
		errFlag = false;
		System.out.println(" reset class variables --");

		try
		{
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("parentNodeListLength------------------->"+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				pendingOrderFlag = false;
				flag  = false;
				//isFlag = true; change by kunal on 26/dec/13 no need to reset flag
				//System.out.println("ERROR STRING:::"+errString);
				parentNode = parentNodeList.item(selectedRow);

				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength---->>> "+ childNodeListLength);
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName---->>> "+ childNodeName);
					if (childNodeName.equals("stock_quantity"))
					{
						stockQuantity = childNode.getFirstChild().getNodeValue();
					}

					if (childNodeName.equals("sale_order"))
					{
						saleOrder = childNode.getFirstChild().getNodeValue();
					}

					if (childNodeName.equals("line_no"))
					{
						lineNo = childNode.getFirstChild().getNodeValue();
					}

					if (childNodeName.equals("item_code"))
					{
						itemCode = childNode.getFirstChild().getNodeValue();
					}

					if (childNodeName.equals("qty_alloc"))  //qty_alloc
					{
						allocQty = Double.parseDouble(childNode.getFirstChild().getNodeValue());
					}
					if (childNodeName.equals("exp_lev"))
					{
						expLev = childNode.getFirstChild().getNodeValue();
					}
					if (childNodeName.equals("cust_code"))
					{
						custCode = childNode.getFirstChild().getNodeValue();
					}
					if (childNodeName.equals("hold_flag"))
					{
						holdFlag = childNode.getFirstChild().getNodeValue();
					}
					//added by ritesh on 7/8/13  for request DI3ESUN009 START
					if (childNodeName.equals("active_pick_allow"))
					{
						if(childNode.getFirstChild() != null)
						{
							activeAllow = childNode.getFirstChild().getNodeValue();
						}

					}											//added by ritesh 0n 7/8/13  for request DI3ESUN009 END
					if (childNodeName.equals("pending_quantity"))  
					{
						pendingQty = Double.parseDouble(childNode.getFirstChild().getNodeValue());
					}

				}//inner for loop
				System.out.println("Pending Qty="+pendingQty+"  alloc Qty="+allocQty);
				if(allocQty > pendingQty) ///added by kunal on 27/dec/13
				{
					//errString = itmDBAccessEJB.getErrorString("","ALLCQTY01","","",conn);
					flag = true;
					isFlag = false;
					list3.add(saleOrder);
					list3.add(lineNo);
					list3.add(itemCode);
					list3.add(allocQty);
					elements3 ++;
				}
				if(!"Y".equalsIgnoreCase(activeAllow) )
				{
					if(!"N".equalsIgnoreCase(activeAllow))
					{
						System.out.println("==activeAllow validate==");
						//errString = itmDBAccessEJB.getErrorString("","VTACTALW","","",conn);
						flag = true;
						isFlag = false;
						list4.add(saleOrder);
						list4.add(lineNo);
						list4.add(itemCode); 
						list4.add(allocQty);
						elements4 ++;
						//return errString;
					}
				}                 			//added by ritesh on 07/10/13 for request DI3ESUN009 
				if("N".equalsIgnoreCase(activeAllow))
				{
					String sql1 = "";
					double shipperSize = 0;
					sql1 = "select shipper_size from item_lot_packsize where item_code = ? ";
					pstmt = conn.prepareStatement(sql1);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						shipperSize = rs.getDouble(1); 
					}
					pstmt.close();pstmt = null;
					rs.close();rs = null;
					try{
						if(allocQty % shipperSize != 0)
						{
							System.out.println("==activeAllow validate 4 N ==");
							flag = true;
							errString = "VTACTALW4"; 
							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						}
					}
					catch(ArithmeticException ex)
					{
						System.out.println("==ArithmeticException==");
						flag = true;
						errString = "VTACTALW3";
						errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);

					}
					System.out.println("TEST 1099::"+errString);
					if( errString != null && errString.trim().length() > 0 && flag == true)
					{
						isFlag = false;
						list.add(saleOrder);
						list.add(lineNo);
						list.add(itemCode);
						list.add(allocQty);
						elements ++;
						errString = "";//added by kunal for handle multiple error message 31/DEC/13 
					}
				}		//added by ritesh on 07/10/13 for request DI3ESUN009 END

				// added by cpatil on 27/11/13 as per manoj sir instruction start

				if(saleOrder != null && saleOrder.trim().length() > 0  )
				{
					sql = " select count(1) from repl_ord_det d,wave_task w,wave_task_det k" +
							" where d.repl_order = k.ref_id  and w.wave_id = k.wave_id " +
							" and w.cancel <> 'Y' and   k.status <>'Y'  " +
							" and d.cancel_mode IS NULL  and d.sale_order = ? " +
							" and d.line_no__sord = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					pstmt.setString(2,lineNo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt(1); 
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					System.out.println("@@@@ first cnt["+cnt+"]");
					if( cnt > 0 )
					{
						pendingOrderFlag = true;
					}
					else
					{
						sql = " SELECT count(1) FROM PICK_ORD_DET D,wave_task w,wave_task_det k " +
								" WHERE d.PICK_order = k.ref_id and w.wave_id = k.wave_id and w.cancel <> 'Y' " +
								" and k.status <>'Y' and   D.SALE_ORDER = ? AND D.LINE_NO__SORD = ? " +
								" and (D.QUANTITY - (CASE WHEN D.DEALLOC_QTY IS NULL THEN 0 ELSE DEALLOC_QTY END )) > 0 ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,saleOrder);
						pstmt.setString(2,lineNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1); 
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						System.out.println("@@@@ second cnt["+cnt+"]");
						if( cnt > 0)
						{
							pendingOrderFlag = true;
						}

					}
					System.out.println("@@@@ pendingOrderFlag["+pendingOrderFlag+"] sorder["+saleOrder+"]line no["+lineNo+"]");

					if(pendingOrderFlag == true )
					{
						if( allocQty > 0 )
						{
							System.out.println("@@@@@@saleOrder["+saleOrder+"]::allocQty["+allocQty+"]");
							flag = true;
							errString = "PROCSUCC"; //  "VTALLQTYSO";
							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						}
						if( errString != null && errString.trim().length() > 0 && flag == true )
						{
							//if( !(list1.contains(saleOrder)))   // commented for multiple line
							{
								isFlag = false;
								list1.add(saleOrder);
								list1.add(lineNo);
								list1.add(itemCode);
								list1.add(allocQty);
								elements1 ++;
								errString = "";
							}
						}
					}
				}
				// added by cpatil on 27/11/13 as per manoj sir instruction end


				if(allocQty > 0 && flag == false)			
				{
					// ADDED BY RITESH ON 09/08/14 START
					System.out.println("if calling...allocQty > 0.....");	
					itemList  =  saleOrder+","+lineNo+","+itemCode+","+allocQty+","+expLev+","+holdFlag+","+activeAllow;
					System.out.println("@@@@@@@@ sorderList["+sorderList+"]saleOrder+activeAllow["+saleOrder+activeAllow+"]");
					if(!sorderList.contains(saleOrder+activeAllow))   // added activeAllow in if condition by cpatil on 20/08/14
					{
						lineNoMap = new HashMap<String,String>(); 
						System.out.println(" lineNoMap cleared()");
					}
					else   // added else condition by cpatil for retriving previous value from map on 20/08/14
					{
						lineNoMap = map.get(saleOrder+activeAllow);
						System.out.println(" lineNoMap exist -- lineNoMap["+lineNoMap+"]");
					}
					
					sorderList.add(saleOrder+activeAllow);
//					itemList.add(saleOrder);
//					itemList.add(lineNo);
//					itemList.add(itemCode);
//					itemList.add(allocQty);
//					itemList.add(expLev);
//					itemList.add(holdFlag);
//					itemList.add(activeAllow);
					System.out.println("itemList::"+itemList);
					lineNoMap.put(lineNo.trim()+"-"+saleOrder,itemList); System.out.println("lineNoMap for each row ::"+lineNoMap);
					map.put(saleOrder+activeAllow,lineNoMap);
					System.out.println("@@@@@@ after added saleOrder["+saleOrder+activeAllow+"]:::lineNoMap:["+lineNoMap+"]");
//					errString = sorderAllocate(saleOrder, lineNo, itemCode, allocQty, expLev ,holdFlag,activeAllow,xtraParams);
//					System.out.println("@@@@@@ sorderAllocate return string["+errString+"]");
//					if (errString != null && errString.trim().length() > 0)
//					{
//						if( errString.indexOf("VSTQTYER") != -1)
//						{
//							System.out.println("@@@@@@@@@[VSTQTYER]");
//							isFlag = false;
//							list2.add(saleOrder);
//							list2.add(lineNo);
//							list2.add(itemCode);
//							list2.add(allocQty);
//							elements2 ++;
//							errString = "";
//						}
//						else
//						{
//						System.out.println("@@@errString :"+errString);
//						return errString;
//						}
//					}
//					else
//					{
//						allocqtyflag = true;
//					}
				}else if(allocQty == 0 && flag == false) //added by kunal on 27/12/12 as per manoj instruction 
				{
					updateSql = "UPDATE SORDDET SET HOLD_FLAG ='"+holdFlag+"'"
							+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
							+ " AND LINE_NO = '" + lineNo + "' ";
					System.out.println("updateSql::"+ updateSql);	
					pstmt = conn.prepareStatement(updateSql);
					int noRowupdated = pstmt.executeUpdate();
					System.out.println("no of row updated in sorddet = "+noRowupdated);
					pstmt.close();
					pstmt = null;
				}
				/**
				 * Generate BATCH_ID if not exist.
				 * VALLABH KADAM 
				 * */
				batchId=genericUtility.getColumnValue("batch_id",headerDom);
				System.out.println("Initial Batch Id :- ["+batchId+"]");
				if(batchId==null || batchId.trim().length()<=0)
				{
					System.out.println("********************Batch id found null Generating Batch id now ************************");
					//Generate new BATCH_ID here
					sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE lower(TRAN_WINDOW) = 'w_sordalloc'";
					System.out.println("keyStringQuery--------->>"+sql);
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{ 
						keyString = rs.getString(1);
						keyCol = rs.getString(2);
						tranSer1 = rs.getString(3);				
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt =null;
					siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					String xmlValues = "";
					String tranDateStr = getCurrdateAppFormat();
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";
					xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
					xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>"; 
					xmlValues = xmlValues +"</Detail1></Root>";
					System.out.println("xmlValues  :["+xmlValues+"]");
					TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
					batchId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
					System.out.println("@@@@ generated BATCH ID ******** :["+batchId+"]");
				}else
				{
					System.out.println("*****IN if VALIDATION *****");
					sql="select count(*) as cnt from sord_alloc where batch_id=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,batchId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt=rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("Batch id cnt at validation :- "+cnt);
					if(cnt<=0){
						errString = itmDBAccessEJB.getErrorString("","VTBCHIDINV","","",conn);
						return errString;
					}
				}
				
				errString = ""; //added by kunal for handle multiple error message 31/DEC/13
			}// out for loop 
			//added by ritesh on 07/10/13 for request DI3ESUN009 start
			//if (errString != null && errString.trim().length() > 0)
			//errString = "PROCSUCC"; 
			//errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
			System.out.println(" fINALLY map ::["+map+"]");
			Set<String> set  = map.keySet();
			Iterator<String> it  =  set.iterator();
			while(it.hasNext())
			{
			String key =  (String)it.next();
			HashMap soLnValues =(HashMap) map.get(key);
			System.out.println("swecondry map "+soLnValues);
//			errString = sorderAllocate(soLnValues,xtraParams);   //VALLABH without BATCH_ID
			errString = sorderAllocate(soLnValues,xtraParams,batchId.trim(),conn);

//			errString = sorderAllocate(saleOrder, lineNo, itemCode, allocQty, expLev ,holdFlag,activeAllow,xtraParams);
			// ADDED BY RITESH ON 09/08/14 END
			System.out.println("@@@@@@ sorderAllocate return string["+errString+"]");
			
			if ((errString != null && errString.trim().length() > 0 ) || errFlag)
			{
				if( errString.indexOf("VSTQTYER") != -1 || errFlag)
				{
					System.out.println("@@@@@@@@@[VSTQTYER]");
					isFlag = false;
//					list2.add(saleOrder);
//					list2.add(lineNo);
//					list2.add(itemCode);
//					list2.add(allocQty);
					System.out.println(" error item list2 ::"+list2);
//					elements2 ++;
					errString = "";
				}
				else
				{
				System.out.println("@@@errString :"+errString);
				return errString;
				}
			}
			else
			{
				allocqtyflag = true;
			}
			
			}
			System.out.println("CHEK isFlag:::"+isFlag);
			//if(isFlag == false && errString != null && errString.trim().length() > 0)//CHANGE DONE BY KUNAL ON 26/DEC/13 FOR SHOW MULTIPE ERROR MESSAGE 
			if(isFlag == false)
			{
				/*Commented by Manoj dtd 17/12/2013
				 if(allocqtyflag = true)
				{
					System.out.println("@@@@@ inside if["+errString+"]");
					// errString = "VTACTALW2"; 
					errString = "PROCSUCC"; 
					errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				}*/
				if(stkAllocateFlag)
				{
					//errString = "PROCSUCC"; 
					errString="VTPROC";//added by chandrashekar on 12-09-2014
				}
				else
				{
					errString = "VTPRCERR"; 
				}
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				System.out.println("before errString=="+errString);
				String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
				String endPart = errString.substring( errString.indexOf("</trace>"));
				String mainStr = "";
				System.out.println("error check =="+elements+"     "+elements1+"        "+elements2+"        "+elements3+"        "+elements4);
				if(elements > 0 || elements1 > 0 || elements2 > 0 || elements3 > 0 || elements4 > 0)
				{
					mainStr = begPart + "Following error has occured\n" ;
				}
				
				if(elements > 0 )
				{ 
					mainStr	= mainStr + "Invalid quantity,Entered quantity is not in multiple of Case/shipper Quantity.\n";
				}
				for(int i = 0; i < elements * 4; i++ )
				{
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list.get(i++)+ ",line no :"+ list.get(i++) + ",item code :"+list.get(i++)+",quantity :"+list.get(i++)+"\n" ;
				}
				
				if(elements1 > 0 )
				{ 
					mainStr = mainStr + "\n pending / incomplete Wave found against sale order.\n";
				}
				for(int i = 0; i < elements1 * 4 ; i++ )
				{
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list1.get(i++)+ ",line no :"+ list1.get(i++) + ",item code :"+list1.get(i++)+",quantity :"+list1.get(i++)+"\n" ;
				}

				if( elements2 > 0 )
				{ 
					String singLot = "";
					sql = "select single_lot from sorder where sale_order = '"+saleOrder+"'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						singLot =  rs.getString(1);
						System.out.println("singLot ::["+singLot+"]");
					}
					pstmt.close();pstmt = null;
					System.out.println(" @@@@@@@1 ");

					rs.close();rs = null;
					System.out.println(" @@@@@@@2 "+mainStr);
					
					
					if("SHIPPERSIZEERR".equalsIgnoreCase(shiperSizeError))
					{
						mainStr = mainStr + "\n Shipper Size not define.\n";System.out.println(" @@@@@@@45 ");
						
					}
					else if("Y".equalsIgnoreCase(singLot))
					{
						mainStr = mainStr + "\n Stock not available in single lot.\n";System.out.println(" @@@@@@@455 ");
					}
					else
					{
						mainStr = mainStr + "\n Insufficient stock available.\n";System.out.println(" @@@@@@@4555 ");
					}
					System.out.println(" @@@@@@@3 "+mainStr);
				}
				System.out.println(" elsement2 - "+elements2 +"&&&&"+ list2);
				for(int i = 0; i < elements2 * 4 ; i++ )
				{
					System.out.println(" iterator i - "+i);
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list2.get(i++)+ ",line no :"+ list2.get(i++) + ",item code :"+list2.get(i++)+",quantity :"+list2.get(i++)+"\n" ;
					
				}
				System.out.println(" @@@@@@@4 "+mainStr);
				//added by kunal on 27/dec/13
				if( elements3 > 0 )
				{ 
					mainStr = mainStr + "\n Allocated quantity more than Pending quantity.\n";
				}
				for(int i = 0; i < elements3 * 4 ; i++ )
				{
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list3.get(i++)+ ",line no :"+ list3.get(i++) + ",item code :"+list3.get(i++)+",quantity :"+list3.get(i++)+"\n" ;
				}
				if( elements4 > 0 )
				{ 
					mainStr = mainStr + "\n Active allow should be either Y or N.\n";
				}
				for(int i = 0; i < elements4 * 4 ; i++ )
				{
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list4.get(i++)+ ",line no :"+ list4.get(i++) + ",item code :"+list4.get(i++)+",quantity :"+list4.get(i++)+"\n" ;
				}
				//added by kunal on 27/dec/13 end
				
				//Start Added by chandrashekar on 12-Feb-2015
				System.out.println("errString>>>>["+errString+"]");
				System.out.println("batchId>>>>"+batchId);
				if(errString.indexOf("VTPROC") > -1)
				{
					mainStr = begPart + "Following Batch id generated :" +batchId;
				}
				//End Added by chandrashekar on 12-Feb-2015
				
				System.out.println(" @@@@@@@mainStr "+mainStr);
				mainStr = mainStr +  endPart;		System.out.println(" @@@@@@@mainStr+endPart "+mainStr);
				errString = mainStr;
				begPart =null;
				endPart =null;
				mainStr =null;	
				System.out.println("after errString=="+errString);
				return errString;

			}
			else
			{ 
				if (errString == null || errString.trim().length() == 0)
				{
					conn.commit();	
					errString="VTCOMPL";
					errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
					String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
					String endPart = errString.substring( errString.indexOf("</trace>"));
					String mainStr = "";
					mainStr = begPart + "Following Batch id generated : " +batchId;
					mainStr = mainStr +  endPart;	
					errString = mainStr;
					begPart =null;
					endPart =null;
					mainStr =null;	
					System.out.println("after errString=="+errString);
					return errString;
				}
				else
				{
					conn.rollback();
					errString="VTPRCERR";
					errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
					return errString;
				}
				
			}//else block added by chandrashekar on 13-02-15
			
			//added by ritesh on 07/10/13 for request DI3ESUN009 end

			/*Commented By Manoj Sharma dtd 29/032012 Not required as discussed with Manohar Sir
			if(postOrderFg.equals("Y"))
			{

				errString = postOrder(saleOrderArr, xtraParams);
				if (errString != null && errString.trim().length() > 0)
				{
					System.out.println("errString :"+ errString);
					return errString;
				}
			}*/	
			//added by kunal on 27/12/12 
			/*System.out.println("errString@@@@@@["+errString+"]");
			if (errString == null || errString.trim().length() == 0)
			{
				conn.commit();	
				errString="VTCOMPL";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}
			else
			{	conn.rollback();
				errString="VTPRCERR";	
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;

			}*/
			//added by kunal on 27/12/12

		}//try end	
		catch(Exception e)
		{	
		    e.printStackTrace();
			System.out.println("Exception :StockAllocationPrcEJB :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			errString = e.getMessage();
			System.out.println();
			return errString ;
		}
		finally
		{
			System.out.println("Closing Connection....");
			try
			{
				saleOrderArr.clear();
				if(conn != null)
				{					
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					conn.close();
					conn = null;
				}

			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString ;

			}
			/*if (errString == null || errString.trim().length() == 0)
			{
				errString="VTCOMPL";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}
			else
			{
				errString="VTPRCERR";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;

			}*/
		}

	}
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
	
	private String getCurrdateAppFormat() throws ITMException
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(genericUtility.getDBDateFormat());
			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		}
		catch(Exception exception)
		{
			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
			throw new ITMException(exception); //Added By Mukesh Chauhan on 05/08/19
		}
		return s;
	}	

	/**
	 * @param saleOrder
	 * @param lineNo
	 * @param itemCode
	 * @param allocQty
	 * @param expLev
	 * @param holdFlag
	 * @param activeAllow
	 * @return
	 * @throws ITMException
	 */
//	private String sorderAllocate(String saleOrder, String lineNo, String itemCode, double allocQty, String expLev ,String holdFlag,String activeAllow,String xtraParams) throws ITMException
//	private String sorderAllocate(HashMap soLnValues,String xtraParams) throws ITMException  VALLABH without BATCH_ID
//	private String sorderAllocate(HashMap soLnValues,String xtraParams, String batchId) throws ITMException
	private String sorderAllocate(HashMap soLnValues,String xtraParams, String batchId, Connection conn) throws ITMException
	{
		System.out.println("sorderAllocate calling .260814...RITESH..........");
		String saleOrder = "", lineNo = "", itemCode = "";
		double allocQty = 0d;
		String expLev = "" ,holdFlag = "", activeAllow= "";
		
		String getDataSql = null;
		String sorditemSql=null;
		String itemCodeOrd = null ;
		String unitStd = null ;
		String lotSl= null;
		String lotNo = null;
		String locCode = null;
		String itemShDescr = null;
		String locDescr = null;
		String unit = null;
		String grade = null;
		String siteCodeMfg =null;
		String itemRef = null;
		String status =null;
		String resrvLoc = "",casePickLoc = "",activePickLoc = "" ,deepStoreLoc = "" ,partialResrvLoc = "";//added by kunal on 14/NOV/13
		String siteCodeShip = "";//added by kunal on 23/DEC/13
		java.sql.Date expDate = new java.sql.Date(System.currentTimeMillis());
		java.sql.Date mfgDate = new java.sql.Date(System.currentTimeMillis());
		java.sql.Date dateAlloc= new java.sql.Date(System.currentTimeMillis());
		String forcedFlag=null;
		String updateSorditem = null;
		String insertSql = null;
		String updateSordalloc = null;
		String updateSql= null;
		String flag = null;
		String sql1 = "",sql = "";
		String orderByStkStr = "";
		double stockQuantity = 0;
		double qtyAvailAlloc = 0;
		double lotQtyToBeAllocated = 0;
		double qtyDesp =0;
		double quantity =0;
		double convQtyStduom = 0 ;
		double quantityStduom = 0 ;
		double pendingQuantity = 0;
		double qty=0;
		int intCnt=0;
		int updCnt=0;
		int pendingQty = 0;
		String varValue1 = "",varValue2 = "";
		String errString = "";
		String error = "";
		String errCode = "";
//		boolean checkLocation = false;
//		boolean isActives = false;
//		Connection conn = null;                             //VALLABH KADAM get connection form process().
		PreparedStatement pstmtStock = null ,pstmt1 = null;
		PreparedStatement pstmtStockInsertSordAlloc = null;
		Statement st = null;
		ResultSet rs = null ;
		ResultSet rsSItem = null;
		StringBuffer xmlBuff = null;
		String xmlString = "",retString  = "";
		DistCommon dComm = new DistCommon();
		Timestamp sysDate = null;
		Calendar currentDate = Calendar.getInstance();
		double minSelfLife=0;
		double maxSelfLife=0;//Added by wasim on 20-APR-2017 for DDUK changes
		// ADDED BY RITESH ON 15/07/14 START
		String singleLot = "";                
//		TreeMap stockLot=new TreeMap<String,Double>();
//		TreeMap<String,ArrayList<String>>stockLotDetail=new TreeMap<String, ArrayList<String>>();
//		ArrayList<String> lotList= new ArrayList<String>();
//		ArrayList<String> lotDeatilList  =  new ArrayList<String>();
		PreparedStatement pstmtShip = null;
		ResultSet rsShip = null;
		String sqlShip = "";
		double shipperSizeLot = 0;
//		boolean getLotFlag = false ;
//		double totalQtyAvailAlloc = 0d;
//		boolean isQtyAvail = false ;
		int linenoDet = 0;
		int  count =0;
		boolean isHdr = true;
		String allocqtyStr = "";
		boolean allocQtyFlag = false;
		boolean isdetFlag =  false;
		String autoAlloc="";				//VALLABH KADAM 08/APR/15
		// ADDED BY RITESH ON 15/07/14 END
		String userId = "";//Added By Pavan R 27/DEC/17
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(
					sysDateStr, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			
			StockAllocPrc allocPrc=new StockAllocPrc();
			//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//connDriver = null;
			// 21/08/12 manoharan get the shipping site
			System.out.println("soLnValues::"+soLnValues);
//			ArrayList listmap = new ArrayList(soLnValues.values());System.out.println("listmap::"+listmap);
//			for(int k = 0 ; k < listmap.size();k++)
//			{
			// ADDED BY RITESH ON 09/08/14 START
			Set set  = soLnValues.keySet();System.out.println("SET::"+set);
			Iterator it  =  set.iterator();
			while(it.hasNext())
			{
			count ++;
			TreeMap stockLot=new TreeMap<String,Double>();
			TreeMap<String,ArrayList<String>>stockLotDetail=new TreeMap<String, ArrayList<String>>();
			ArrayList<String> lotList= new ArrayList<String>();
			ArrayList<String> lotDeatilList  =  new ArrayList<String>();
			boolean isQtyAvail = false ;
			boolean checkLocation = false;
			boolean isActives = false;
			boolean getLotFlag = false ;
			double totalQtyAvailAlloc = 0d;
//			if(count>1)
//				isHdr =  false;
			String key =  (String)it.next();System.out.println("key value:[]:"+key);System.out.println("soLnValues.get(key)::"+soLnValues.get(key));

			String solnValueList[] = ((String)soLnValues.get(key)).split(",");System.out.println("solnValueList::"+solnValueList);

			saleOrder = (String)solnValueList[0];
			lineNo = (String)solnValueList[1];
			itemCode =(String) solnValueList[2];
			allocqtyStr = (String)solnValueList[3];
			allocQty = Double.parseDouble(allocqtyStr);
			expLev = (String)solnValueList[4];
			holdFlag = (String)solnValueList[5];
			activeAllow =(String) solnValueList[6];
			
//			ArrayList solnValueList = new ArrayList();
//			solnValueList =(ArrayList) soLnValues.get(key);System.out.println("solnValueList:12:"+solnValueList);
//			saleOrder = (String)solnValueList.get(0);
//			lineNo = (String)solnValueList.get(1);
//			itemCode =(String) solnValueList.get(2);
//			allocQty = Double.parseDouble((solnValueList.get(3)).toString());
//			expLev = (String)solnValueList.get(4);
//			holdFlag = (String)solnValueList.get(5);
//			activeAllow =(String)solnValueList.get(6);
			System.out.println("@@@@count["+count+"]");
			System.out.println(" saleOrder::"+saleOrder);
			System.out.println(" lineNo::"+lineNo);
			System.out.println(" itemCode::"+itemCode);
			System.out.println(" allocQty::"+allocQty);
			System.out.println(" expLev::"+expLev);
			System.out.println(" holdFlag::"+holdFlag);
			System.out.println(" activeAllow::"+activeAllow);
			// ADDED BY RITESH ON 09/08/14 END
			getDataSql = "SELECT SITE_CODE__SHIP FROM SORDER WHERE SALE_ORDER = ? ";
			pstmtStock = conn.prepareStatement(getDataSql);

			pstmtStock.setString(1,saleOrder);

			rs = pstmtStock.executeQuery();

			System.out.println("siteCodeShip------"+ siteCodeShip);
			if(rs.next())
			{
				siteCodeShip = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmtStock.close();
			pstmtStock = null;
			// end 21/08/12 manoharan get the shipping site

			System.out.println("siteCodeShip------"+ siteCodeShip);
			sorditemSql = "SELECT ITEM_CODE,ITEM_CODE__ORD,UNIT, QTY_DESP, ITEM_REF,"
					//Changed by wasim on 20-APR-2017 for taking max self life for DDUK changes
					//+" QUANTITY - QTY_DESP PENDING_QUANTITY,MIN_SHELF_LIFE "
					+" QUANTITY - QTY_DESP PENDING_QUANTITY,MIN_SHELF_LIFE,MAX_SHELF_LIFE "
					+" FROM SORDITEM WHERE SALE_ORDER = '" + saleOrder + "' "
					+" AND LINE_NO = '" + lineNo + "' "
					+" AND EXP_LEV = '" + expLev + "' ";
			System.out.println("sorditemSql:::"+sorditemSql);
			st = conn.createStatement();
			rsSItem = st.executeQuery(sorditemSql);
			if (rsSItem.next())
			{
				//ITEM_CODE
				itemCode = rsSItem.getString(1);
				System.out.println("itemCode::::"+ itemCode);
				//ITEM_CODE__ORD
				itemCodeOrd = rsSItem.getString(2);	
				//UNIT
				unitStd = rsSItem.getString(3);	
				//QTY_DESP
				qtyDesp = 0;
				//ITEM_REF
				itemRef = rsSItem.getString(5);	
				//PENDING_QUANTITY
				pendingQuantity = rsSItem.getDouble(6);
				minSelfLife= rsSItem.getDouble("MIN_SHELF_LIFE");
				maxSelfLife= rsSItem.getDouble("MAX_SHELF_LIFE");
			}
			rsSItem.close();
			st.close();	

			//added by kunal on 14/NOV/13 DI3ESUN009
			resrvLoc  = checkNull(dComm.getDisparams("999999","RESERV_LOCATION",conn));
			casePickLoc  = checkNull(dComm.getDisparams("999999","CASE_PICK_INVSTAT",conn));
			activePickLoc  = checkNull(dComm.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn));
			deepStoreLoc = checkNull(dComm.getDisparams("999999","DEEP_STORE_INVSTAT",conn));
			partialResrvLoc = checkNull(dComm.getDisparams("999999","PRSRV_INVSTAT",conn));	
			if(resrvLoc.trim().length() == 0 || resrvLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				resrvLoc = "";
			}
			if(casePickLoc.trim().length() == 0 || casePickLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				casePickLoc = "";
			}
			if(activePickLoc.trim().length() == 0 || activePickLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				activePickLoc = "";
			}
			if(deepStoreLoc.trim().length() == 0 || deepStoreLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				deepStoreLoc = "";
			}
			if(partialResrvLoc.trim().length() == 0 || partialResrvLoc.trim().equalsIgnoreCase("NULLFOUND"))
			{
				partialResrvLoc = "";
			}

			System.out.println("CASE_PICK_INVSTAT="+casePickLoc+"     ACTIVE_PICK_INVSTAT="+activePickLoc+"       RESERV_LOCATION="+resrvLoc+"     DEEP_STORE_INVSTAT="+deepStoreLoc+"     PARTIAL RESERVE LOC="+partialResrvLoc);
			if(resrvLoc.trim().length() > 0 || casePickLoc.trim().length() > 0 || activePickLoc.trim().length() > 0 || deepStoreLoc.trim().length() > 0  || partialResrvLoc.trim().length() > 0)
			{
				checkLocation = true;
			}

			if(checkLocation)
			{
				// ADDED BY RITESH ON 02/MAY/2014 START
				String sqlSorditem = "";
				PreparedStatement psmtSord = null;

				if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
				{
					sqlSorditem = " select * from sorditem where sale_order   = '"+saleOrder+"' and line_no = '"+lineNo+"' for update ";
				}
				else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
				{
					sqlSorditem = " select * from sorditem (updlock) where sale_order   = '"+saleOrder+"' and line_no = '"+lineNo+"' ";
				}
				else
				{
					sqlSorditem = " select * from sorditem where sale_order   = '"+saleOrder+"' and line_no = '"+lineNo+"' ";//for update nowait ";
				}
				
				psmtSord = conn.prepareStatement(sqlSorditem);
				psmtSord.executeQuery();
				if(psmtSord!=null)
					psmtSord.close();
				psmtSord= null;
				// ADDED BY RITESH ON 02/MAY/2014 END
				
				sql =" SELECT SORDER.CUST_CODE, CUSTOMER.CUST_NAME,SORDDET.LINE_NO, "
						+"SORDER.SALE_ORDER,SORDER.DUE_DATE,SORDITEM.ITEM_CODE,"
						+"ITEM.DESCR,SORDITEM.QUANTITY,"
						+"((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC) PENDING_QUANTITY,"
						+"SORDITEM.QTY_ALLOC,SORDDET.PACK_INSTR,"
						+"SORDER.SITE_CODE,SORDITEM.EXP_LEV, "
						+"SORDER.PART_QTY AS PART_QTY, SORDER.SINGLE_LOT AS SINGLE_LOT, SORDITEM.MIN_SHELF_LIFE AS MIN_SHELF_LIFE "//Gulzar 5/13/2012
						+" ,SORDER.ALLOC_FLAG AS ALLOC_FLAG , WAVE_TYPE.MASTER_PACK_ALLOW , WAVE_TYPE.ACTIVE_PICK_ALLOW , WAVE_TYPE.STOCK_TO_DOCK_ALLOW "		   
						+ " ,SORDER.SINGLE_LOT AS SINGLE_LOT "        //ADDED BY RITESH ON 15/07/14
						+"FROM SORDDET,SORDER,SORDITEM,CUSTOMER,ITEM , WAVE_TYPE "
						//Changed by sumit 20/08/12 getting column data end.
						+"WHERE ( SORDER.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
						+"( SORDITEM.SALE_ORDER = SORDER.SALE_ORDER ) AND "
						+"( SORDDET.LINE_NO = SORDITEM.LINE_NO ) AND "
						+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
						+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND  "
						+"( SORDDET.SITE_CODE = SORDITEM.SITE_CODE ) AND  "
						//Changed by sumit on 20/08/12 join customer with wave_type
						+"( CUSTOMER.WAVE_TYPE = WAVE_TYPE.WAVE_TYPE(+)) AND "
						//+" SORDER.SITE_CODE = ? AND "							   
						+" SORDER.SALE_ORDER = ? "
						+" AND SORDITEM.LINE_NO = ? "
						+"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
						+"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end = 'P' "
						//Changed by sumit on 12/09/12 considering hold_flag from sorddet 
						+" AND CASE WHEN SORDDET.HOLD_FLAG IS NULL THEN 'N' ELSE SORDDET.HOLD_FLAG end <> 'Y'"
						//Chnaged by Rohan on 11/07/12 revert Changes of Manual Stock Allocation
						//Changed by Rohan on 22/06/12
						//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
						//+"AND (SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 OR SORDER.ALLOC_FLAG='M' )"
						//Changed by sumit on 13/08/12 as per manual stock allocation start.
						//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
						+" AND (((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC > 0 ) "  
						+" OR (FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )"	 
						+"AND SORDITEM.LINE_TYPE = 'I'";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,saleOrder);
				pstmt1.setString(2,lineNo);
				rs = pstmt1.executeQuery();
				if(rs.next())
				{
					pendingQty = rs.getInt("PENDING_QUANTITY");
//					singleLot = rs.getString("SINGLE_LOT")==null?"N":rs.getString("SINGLE_LOT");    //ADDED BY RITESH ON 15/07/14
				}
				rs.close();
				rs = null;
				pstmt1.close();
				pstmt1 = null;

				sql = " SELECT SINGLE_LOT FROM SORDER WHERE SALE_ORDER = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,saleOrder);
				rs = pstmt1.executeQuery();
				if(rs.next())
				{
					singleLot = rs.getString("SINGLE_LOT")==null?"N":rs.getString("SINGLE_LOT");   
				}
				rs.close();
				rs = null;
				pstmt1.close();
				pstmt1 = null;
				
				HashMap itemVolMap = getItemVoumeMap(itemCode, "", conn);
				double packSize = (Double)itemVolMap.get("PACK_SIZE");
				System.out.println(" pendingQty = "+pendingQty+"  packSize ="+packSize);
				System.out.println("allocQty = "+allocQty);
				if((allocQty % packSize) > 0) //change done by kunal on 7/may/14 replace pending qty to alloc qty 
				{
					isActives = true;
					orderByStkStr = " AND LOCATION.INV_STAT IN(?,?,?,?,?) ";
					orderByStkStr = orderByStkStr +"  ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF, STOCK.LOC_CODE ";     // ADDED ON 30/APR/14 BY RITESH 
				}
				else
				{
					isActives = false;
					orderByStkStr = " AND LOCATION.INV_STAT IN(?,?,?,?) ";
					orderByStkStr = orderByStkStr +"  ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF_CASE, STOCK.LOC_CODE "; // ADDED ON 30/APR/14 BY RITESH
				}
			}

			//added by kunal on 14/NOV/13  DI3ESUN009 END


			getDataSql = "SELECT STOCK.LOT_NO,STOCK.LOT_SL,"
					+"STOCK.LOC_CODE, "
					+"STOCK.UNIT,  "
					+"(STOCK.QUANTITY - STOCK.ALLOC_QTY - case when STOCK.HOLD_QTY is null then 0 else STOCK.HOLD_QTY end ) AS QTY_AVAIL_ALLOC ,"
					+"STOCK.GRADE,STOCK.EXP_DATE,STOCK.CONV__QTY_STDUOM,STOCK.QUANTITY, " 
					+"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG "
					+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
					+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
					+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
					+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
					+"AND INVSTAT.AVAILABLE = 'Y' "
					+"AND STOCK.ITEM_CODE = ? AND STOCK.SITE_CODE = ? "
					+"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY - case when STOCK.HOLD_QTY is null then 0 else STOCK.HOLD_QTY end) > 0  "
					+"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' ) ";// + " AND STOCK.LOC_CODE NOT IN ('D1911C ') ";


			if("N".equalsIgnoreCase(activeAllow))
			{
				getDataSql = getDataSql.concat(" AND LOCATION.INV_STAT NOT IN(?,?) ");
			}
			
			//Added by manoj dtd 09/08/2013 to implement FEFO concept  
			//Changed by wasim on 20-APR-2017 for DDUK changes [START]
			//getDataSql=getDataSql+" AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
			if(maxSelfLife > 0)
			{	
			  getDataSql=getDataSql+" AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
			}  
			//Changed by wasim on 20-APR-2017 for DDUK changes [END]
			//getDataSql=getDataSql+"AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) AND STOCK.LOC_CODE NOT IN ('D1911C')";
			

			if(checkLocation) //added by kunal on 14/NOV/13  DI3ESUN009 
			{
				getDataSql=getDataSql+orderByStkStr;
			}
			else				// CHANGE ON 30/APR/14 BY RITESH
			{
			getDataSql=getDataSql+"  ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF, STOCK.LOC_CODE ";
			}
			pstmtStock = conn.prepareStatement(getDataSql);
			pstmtStock.setString(1,itemCode);
			pstmtStock.setString(2,siteCodeShip);
			//Changed by wasim on 20-APR-17 for DDUK changes [START]
			//pstmtStock.setDouble(3,minSelfLife);
			if(maxSelfLife > 0)
			{
				pstmtStock.setDouble(3,minSelfLife);
			}
			//Changed by wasim on 20-APR-17 for DDUK changes [END]
			//added by ritesh 0n 7/8/13  for request DI3ESUN009 START
			if("N".equalsIgnoreCase(activeAllow))			
			{	  
				varValue1 = dComm.getDisparams("999999","ACTIVE_PICK_INVSTAT", conn);
				varValue2 = dComm.getDisparams("999999","PRSRV_INVSTAT", conn); 
				pstmtStock.setString(3,varValue1);
				pstmtStock.setString(4,varValue2);
				//Changed by wasim on 20-APR-17 for DDUK changes [START]
				//pstmtStock.setDouble(5,minSelfLife);
				if(maxSelfLife > 0)
				{
					pstmtStock.setDouble(5,minSelfLife);
				}
				//Changed by wasim on 20-APR-17 for DDUK changes [END]
				if(checkLocation) //added by kunal on 14/NOV/13  DI3ESUN009
				{
					//Changed by wasim on 20-APR-17 for DDUK changes [START]
					/*pstmtStock.setString(6,resrvLoc);
					pstmtStock.setString(7,casePickLoc);
					pstmtStock.setString(8,activePickLoc);
					pstmtStock.setString(9,deepStoreLoc);
					if(isActives)
					{
						pstmtStock.setString(10,partialResrvLoc);
					}*/
					if(maxSelfLife > 0)
					{
						pstmtStock.setString(6,resrvLoc);
						pstmtStock.setString(7,casePickLoc);
						pstmtStock.setString(8,activePickLoc);
						pstmtStock.setString(9,deepStoreLoc);
						if(isActives)
						{
							pstmtStock.setString(10,partialResrvLoc);
						}
					}
					else
					{
						pstmtStock.setString(5,resrvLoc);
						pstmtStock.setString(6,casePickLoc);
						pstmtStock.setString(7,activePickLoc);
						pstmtStock.setString(8,deepStoreLoc);
						if(isActives)
						{
							pstmtStock.setString(9,partialResrvLoc);
						}
					}
					//Changed by wasim on 20-APR-17 for DDUK changes [END]
				}
			}
			else
			{
				if(checkLocation) //added by kunal on 14/NOV/13  DI3ESUN009
				{
					//Changed by wasim on 20-APR-17 for DDUK changes [START]
					/*pstmtStock.setString(4,resrvLoc);
					pstmtStock.setString(5,casePickLoc);
					pstmtStock.setString(6,activePickLoc);
					pstmtStock.setString(7,deepStoreLoc);
					if(isActives)
					{
						pstmtStock.setString(8,partialResrvLoc);
					}*/
					if(maxSelfLife > 0)
					{
						pstmtStock.setString(4,resrvLoc);
						pstmtStock.setString(5,casePickLoc);
						pstmtStock.setString(6,activePickLoc);
						pstmtStock.setString(7,deepStoreLoc);
						if(isActives)
						{
							pstmtStock.setString(8,partialResrvLoc);
						}
					}
					else
					{
						pstmtStock.setString(3,resrvLoc);
						pstmtStock.setString(4,casePickLoc);
						pstmtStock.setString(5,activePickLoc);
						pstmtStock.setString(6,deepStoreLoc);
						if(isActives)
						{
							pstmtStock.setString(7,partialResrvLoc);
						}
					}
					//Changed by wasim on 20-APR-17 for DDUK changes [END]
				}
			}
			//added by ritesh 0n 7/8/13  for request DI3ESUN009 START


			rs = pstmtStock.executeQuery();
			System.out.println("siteCodeShip------"+ siteCodeShip);
			System.out.println("itemCode------"+ itemCode);
			System.out.println("saleOrder------"+ saleOrder);
			System.out.println("siteCode------"+ siteCode);
			System.out.println("chgTerm------"+ chgTerm);
			System.out.println("chgUser------"+ chgUser);
			System.out.println("activeAllow------"+ activeAllow);
			System.out.println("Select completed");
			//CHANGE BY RITESH ON 18/07/14 START
			while(rs.next())//for(lotListNew)
			{
				System.out.println( " INSIDE WHILE LOOP.............SINGLELOT __12 ["+singleLot+"]");
				//LOT_NO
				lotNo = rs.getString(1);
				//LOT_SL
				lotSl = rs.getString(2);
				//LOC_CODE
				locCode = rs.getString(3);
				//UNIT
				unit = rs.getString(4);
				//QTY_AVAIL_ALLO
				qtyAvailAlloc = rs.getDouble(5);
				//GRADE
				grade = rs.getString(6);
				//EXP_DATE
				expDate =rs.getDate(7);
				//CONV__QTY_STDUOM
				convQtyStduom=rs.getDouble(8);
				//QUANTITY
				quantity= rs.getDouble(9);
				//MFG_DATE
				mfgDate = rs.getDate(10);
				//SITE_CODE__MFG
				siteCodeMfg = rs.getString(11);

//				ArrayList<String> stockList=new ArrayList<String>();
				
				
				sqlShip = "select shipper_size from item_lot_packsize where item_code = ? and '"+lotNo+"' between lot_no__from and lot_no__to";
				pstmtShip = conn.prepareStatement(sqlShip);
				pstmtShip.setString(1,itemCode);
				rsShip = pstmtShip.executeQuery();
				if(rsShip.next())
				{
					shipperSizeLot = rsShip.getDouble(1); 
				}
				pstmtShip.close();pstmtShip = null;
				rsShip.close();rsShip = null;	
				System.out.print(":: qtyAvailAlloc ::"+qtyAvailAlloc);
				System.out.print(":: shipperSizeLot ::"+shipperSizeLot);
				
				double sz = 0;
				if("N".equalsIgnoreCase(activeAllow))	
				{
					if(shipperSizeLot <= 0d)
						sz = 0;
					else
						sz = qtyAvailAlloc % shipperSizeLot;
					if(qtyAvailAlloc > shipperSizeLot)
						sz= 0;
						
				}else
				{
					sz = 0;
				}
				if(sz == 0 )
				{
				
				if("N".equalsIgnoreCase(singleLot.trim()))
				{
					totalQtyAvailAlloc = totalQtyAvailAlloc + qtyAvailAlloc ;
					System.out.println("totalQtyAvailAlloc::"+totalQtyAvailAlloc+" allocQty::"+allocQty);
					if(totalQtyAvailAlloc >= allocQty)
					{
						isQtyAvail  = true;getLotFlag = true; 
//						lotDeatilList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+unit+"@"+qtyAvailAlloc+"@"+grade+"@"+convQtyStduom+"@"+quantity+"@"+siteCodeMfg);

					}
					lotDeatilList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+unit+"@"+qtyAvailAlloc+"@"+grade+"@"+convQtyStduom+"@"+quantity+"@"+siteCodeMfg);
					System.out.println("lotDeatilList1 ::"+lotDeatilList);
				}
				System.out.println("lotDeatilList2 ::"+lotDeatilList);

				//ADDED BY RITESH ON 16/07/14 START
				if("Y".equalsIgnoreCase(singleLot.trim()))  // CURRECT FINALLY
				{
							System.out.println("singleLot N CONDITION ::");

					if(stockLot.containsKey(lotNo))
					{
						System.out.println("singleLot N CONDITION IF ::");
						stockLot.put(lotNo, ((Double)stockLot.get(lotNo)+qtyAvailAlloc));
						ArrayList<String> stockList=new ArrayList<String>();
						stockList=stockLotDetail.get(lotNo);
						System.out.println(lotNo+"@@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvailAlloc);
//						stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvailAlloc);
						stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+unit+"@"+qtyAvailAlloc+"@"+grade+"@"+convQtyStduom+"@"+quantity+"@"+siteCodeMfg);
						stockLotDetail.put(lotNo, stockList);
					}
					else
					{		
						System.out.println("singleLot N CONDITION ELSE ::");

						stockLot.put(lotNo,qtyAvailAlloc);
						ArrayList<String> stockList=new ArrayList<String>();
						//stockList=stockLotDetail.get(lotNo);
						System.out.println(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvailAlloc);
//						stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvailAlloc);
						stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+unit+"@"+qtyAvailAlloc+"@"+grade+"@"+convQtyStduom+"@"+quantity+"@"+siteCodeMfg);
						stockLotDetail.put(lotNo, stockList);
					}
					stockLotDetail.descendingMap();
					stockLot.descendingMap();
					System.out.println("stockLot ::"+stockLot);
					System.out.println("stockLotDetail ::"+stockLotDetail);
//					String lotDetail="";
					Set<String> lotKey=stockLot.keySet();
					Iterator<String> lot=lotKey.iterator();
					System.out.println("lotKey :: [-"+lotKey+"-]");
					while(lot.hasNext())
					{
						String lotKeyVal=lot.next();
						double qty1=Double.parseDouble(""+stockLot.get(lotKeyVal));
						System.out.println("qty1---"+qty1+"---qty to be allocate---"+allocQty);
						if(qty1>=allocQty)
						{
							System.out.println("qty1>=allocQty::y");
//							lotDetail=stockLotDetail.get(lotKeyVal).get(0);
							getLotFlag = true;         // break  add lot no 
							lotDeatilList = stockLotDetail.get(lotKeyVal);
							isQtyAvail  = true;   // check avail qty 
							break;
						}
					}
					
				}System.out.println("   OUT OF Y / N SINGLELOT");
				}
				if(getLotFlag)
				{
					break;
				}
				System.out.println(getLotFlag+"--"+lotNo+"@"+lotSl+"@"+locCode+"@"+unit+"@"+qtyAvailAlloc+"@"+grade+"@"+convQtyStduom+"@"+quantity+"@"+siteCodeMfg);
			}
			System.out.println(" lotDeatilList ::"+lotDeatilList);
			System.out.println(" isQtyAvail ::["+isQtyAvail+"]");
			if(isQtyAvail == false)
			{
				errFlag = true;
			}
			
			if(lotDeatilList.size() > 0  && isQtyAvail)
			{
				if(isHdr)
				{
				System.out.println("New Record Inserting*****250814****============***********");	
				System.out.println("BATCH id while SAVE XML generation :- "+batchId);
				isHdr =  false;
				isdetFlag =  false;
				xmlBuff = new StringBuffer();
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("sord_alloc").append("]]></objName>");  
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
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sord_alloc\" objContext=\"1\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<TRAN_DATE><![CDATA["+ sdf.format(sysDate) +"]]></TRAN_DATE>");					
				xmlBuff.append("<cust_code><![CDATA["+ custCode +"]]></cust_code>");
				xmlBuff.append("<sale_order><![CDATA["+ saleOrder +"]]></sale_order>");
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");				
				xmlBuff.append("<site_code><![CDATA["+ siteCode  +"]]></site_code>");//
				xmlBuff.append("<site_code__ship><![CDATA["+ siteCodeShip  +"]]></site_code__ship>");
				xmlBuff.append("<chg_date><![CDATA["+sdf.format(sysDate)+"]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA["+ chgTerm +"]]></chg_term>");						
				xmlBuff.append("<chg_user><![CDATA["+ chgUser  +"]]></chg_user>");
				xmlBuff.append("<add_date><![CDATA["+sdf.format(sysDate)+"]]></add_date>");
				xmlBuff.append("<add_term><![CDATA["+ chgTerm +"]]></add_term>");						
				xmlBuff.append("<add_user><![CDATA["+ chgUser  +"]]></add_user>");
				xmlBuff.append("<active_pick_allow><![CDATA["+ activeAllow +"]]></active_pick_allow>");			
				xmlBuff.append("<batch_id><![CDATA["+ batchId +"]]></batch_id>");		//VALLABH BATCH_ID to save.	
				xmlBuff.append("<alloc_source><![CDATA[P]]></alloc_source>");	
				xmlBuff.append("<alloc_flag><![CDATA[A]]></alloc_flag>");
				
				/**
				 * VALLABH KADAM 08/APR/15
				 * EDI_STAT set to 'N'
				 * For AutoAllocOrdrSch.java
				 * Req Id:- [D15ASUN001]
				 * */
				autoAlloc=genericUtility.getValueFromXTRA_PARAMS(xtraParams, "autoAlloc");
				System.out.println("Xtra Param autoAlloc :- ["+autoAlloc+"]");
				if(autoAlloc!=null && ("Y".equalsIgnoreCase(autoAlloc))){
					xmlBuff.append("<edi_stat><![CDATA[N]]></edi_stat>");
				}
				/**
				 * VALLABH KADAM 08/APR/15 END
				 * */
				
				xmlBuff.append("</Detail1>");
				}
				HashMap  mapDet = new HashMap();
				boolean errItemAddFlag = false;
				for(int i = 0; i < lotDeatilList.size();i++)
				{

					String lotDetailData = lotDeatilList.get(i);
					System.out.println("lotDetailData::["+lotDetailData+"]");
					if(lotDetailData.contains("@"))
					{
						String[] detailArr = lotDetailData.split("@");
						lotNo = detailArr[0];
						lotSl = detailArr[1];
						locCode = detailArr[2];
						unit = detailArr[3];
						qtyAvailAlloc = Double.parseDouble(detailArr[4]);
						grade = detailArr[5];
						convQtyStduom=Double.parseDouble(detailArr[6]);
						quantity= Double.parseDouble(detailArr[7]);
						siteCodeMfg = detailArr[8];
					}
				
				invallocTrace.setRefSer("S-ALC");
				invallocTrace.setRefId(saleOrder);
				invallocTrace.setRefLine(lineNo);
				invallocTrace.setSiteCode(siteCodeShip);
				invallocTrace.setItemCode(itemCode);
				invallocTrace.setLocCode(locCode);
				invallocTrace.setLotNo(lotNo);
				invallocTrace.setLotSl(lotSl);
				PreparedStatement pstmt = null,pstmt2=null;
				ResultSet rs1 = null,rs2=null;
				String sqls = "";
				double shipperSize = 0;
				lotQtyToBeAllocated = 0;

				//added by ritesh on 07/10/13 for request DI3ESUN009 start
				if(!"N".equalsIgnoreCase(activeAllow))
				{
					if (allocQty >= qtyAvailAlloc)
					{
						lotQtyToBeAllocated = qtyAvailAlloc;
					}
					else
					{
						lotQtyToBeAllocated = allocQty;
					}
				}
				else
				{
					boolean shipperSizeFlag = true;
					sqls = "select shipper_size from item_lot_packsize where item_code = ? and '"+lotNo+"' between lot_no__from and lot_no__to";
					pstmt = conn.prepareStatement(sqls);
					pstmt.setString(1,itemCode);
					rs1 = pstmt.executeQuery();
					if(rs1.next())
					{
						shipperSizeFlag = false;
						shipperSize = rs1.getDouble(1); 
					}
					pstmt.close();pstmt = null;
					rs1.close();rs1 = null;	
					System.out.print(":: qtyAvailAlloc ::"+qtyAvailAlloc);
					System.out.print(":: shipperSize ::"+shipperSize);
					if(shipperSizeFlag  && shipperSize <=0d)
					{
						errFlag = true;
						shiperSizeError = "SHIPPERSIZEERR";
						System.out.println("shiperSizeError::["+shiperSizeError+"]");
						if(errItemAddFlag == false)
						{
						list2.add(saleOrder);
						list2.add(lineNo);
						list2.add(itemCode);
						list2.add(allocQty);
						errItemAddFlag = true;
						elements2 ++;  System.out.println("elements2 ++ shiperSizeError ::"+elements2);
						}
					}
					System.out.println("shiperSizeError::"+shiperSizeError);
					if(qtyAvailAlloc >= shipperSize )
					{
						int div = 0;
						if(shipperSize > 0)
						{
							div = (int) (allocQty / shipperSize);
							lotQtyToBeAllocated=div*shipperSize;
							if(lotQtyToBeAllocated > qtyAvailAlloc)
							{
								//lotQtyToBeAllocated = 0; 
								lotQtyToBeAllocated = qtyAvailAlloc;
								//System.out.println("::::::uncommon");
							}
							System.out.println("lotQtyToBeAllocated---"+lotQtyToBeAllocated+"---qtyAvailAlloc---"+qtyAvailAlloc);
						}
						System.out.println("::lotQtyToBeAllocated::"+lotQtyToBeAllocated);
					}
					else
					{
						continue;
					}

				}

				//added by ritesh on 07/10/13 for request DI3ESUN009 end		
				System.out.println("allocQty =::"+allocQty);
				invallocTrace.setAllocQty(lotQtyToBeAllocated);	
				//	
				allocQty -= lotQtyToBeAllocated ;

				System.out.print("AllocQty = " + lotQtyToBeAllocated);
				
				/*generate new confirmed transaction while process the data  from manual
	             stock allocation process.Created new Transaction for allocated quantity for StockAllocation.
	             It will save and confirm the Stock allocation transactionfor allocated quantity
                 */
				//added by priyanka for request id D14BSUN002 as per manoj sharma instruction on 15/05/2014
				
				if(lotQtyToBeAllocated>0)
				{			
					
					sql="SELECT ITEM_CODE,((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END)*CONV__QTY_STDQTY - CASE WHEN qty_alloc IS NULL THEN 0 ELSE qty_alloc END - (CASE WHEN qty_desp IS NULL THEN 0 ELSE qty_desp END)*CONV__QTY_STDQTY ), EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ?";
					pstmt2 =  conn.prepareStatement(sql);
					pstmt2.setString(1,saleOrder);
					pstmt2.setString(2,lineNo);
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						itemCode = rs2.getString("ITEM_CODE");
						qty  =  rs2.getDouble(2);					
						expLev = rs2.getString("EXP_LEV");
						System.out.println("=========qty========="+qty);
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
									
//					System.out.println("New Record Inserting*********============***********");				
//					xmlBuff = new StringBuffer();
//					xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
//					xmlBuff.append("<DocumentRoot>");
//					xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
//					xmlBuff.append("<group0>");
//					xmlBuff.append("<description>").append("Group0 description").append("</description>");
//					xmlBuff.append("<Header0>");
//					xmlBuff.append("<objName><![CDATA[").append("sord_alloc").append("]]></objName>");  
//					xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
//					xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
//					xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
//					xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
//					xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
//					xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
//					xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
//					xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
//					xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
//					xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
//					xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
//					xmlBuff.append("<description>").append("Header0 members").append("</description>");		
//					xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sord_alloc\" objContext=\"1\">");  
//					xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
//					xmlBuff.append("<tran_id/>");
//					xmlBuff.append("<TRAN_DATE><![CDATA["+ sdf.format(sysDate) +"]]></TRAN_DATE>");					
//					xmlBuff.append("<cust_code><![CDATA["+ custCode +"]]></cust_code>");
//					xmlBuff.append("<sale_order><![CDATA["+ saleOrder +"]]></sale_order>");
//					xmlBuff.append("<site_code><![CDATA["+ siteCode  +"]]></site_code>");//
//					xmlBuff.append("<site_code__ship><![CDATA["+ siteCodeShip  +"]]></site_code__ship>");
//					xmlBuff.append("<chg_date><![CDATA["+sdf.format(sysDate)+"]]></chg_date>");
//					xmlBuff.append("<chg_term><![CDATA["+ chgTerm +"]]></chg_term>");						
//					xmlBuff.append("<chg_user><![CDATA["+ chgUser  +"]]></chg_user>");
//					xmlBuff.append("<add_date><![CDATA["+sdf.format(sysDate)+"]]></add_date>");
//					xmlBuff.append("<add_term><![CDATA["+ chgTerm +"]]></add_term>");						
//					xmlBuff.append("<add_user><![CDATA["+ chgUser  +"]]></add_user>");
//					xmlBuff.append("<active_pick_allow><![CDATA["+ activeAllow +"]]></active_pick_allow>");						
//					xmlBuff.append("</Detail1>");
					linenoDet ++;
					String linenoDetStr = Integer.toString(linenoDet);
					mapDet.put(linenoDetStr,linenoDet+","+saleOrder+","+lineNo+","+itemCode+","+locCode+","+lotSl+","+lotNo+","+lotQtyToBeAllocated+","+siteCodeShip+","+expLev+","+qty);
//					xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"sord_alloc\" objContext=\"2\">"); 
//					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
//					xmlBuff.append("<line_no><![CDATA["+linenoDet+"]]></line_no>");
//					xmlBuff.append("<tran_id/>");
//					xmlBuff.append("<sale_order><![CDATA["+ saleOrder +"]]></sale_order>");					
//					xmlBuff.append("<line_no__sord><![CDATA["+lineNo  +"]]></line_no__sord>");
//					xmlBuff.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");
//					xmlBuff.append("<loc_code><![CDATA["+locCode+"]]></loc_code>");
//					xmlBuff.append("<lot_no><![CDATA["+ lotNo +"]]></lot_no>");
//					xmlBuff.append("<lot_sl><![CDATA["+lotSl+"]]></lot_sl>");
//					xmlBuff.append("<quantity><![CDATA["+ lotQtyToBeAllocated +"]]></quantity>");	
//					xmlBuff.append("<dealloc_qty><![CDATA[0]]></dealloc_qty>");	
//					xmlBuff.append("<site_code><![CDATA["+ siteCodeShip  +"]]></site_code>");
//					xmlBuff.append("<exp_lev><![CDATA["+ expLev +"]]></exp_lev>");					
//					xmlBuff.append("<pending_qty><![CDATA["+ qty  +"]]></pending_qty>");					
//					xmlBuff.append("</Detail2>");				
				}
				else if(errItemAddFlag == false)
				{   
					
					list2.add(saleOrder);
					list2.add(lineNo);
					list2.add(itemCode);
					list2.add(allocQty);
					errItemAddFlag = true;
					elements2 ++;  System.out.println("elements2 ++ lotQtyToBeAllocated ::"+elements2);
				}

			}//end of while 
				System.out.println("mapDet::"+mapDet);
				if(mapDet.size() > 0  && allocQty == 0)
				{
					allocQtyFlag = true;
					Set<String> lotKey=mapDet.keySet();
					Iterator<String> lot=lotKey.iterator();
					System.out.println("lotKey ::mapDet:: [-"+lotKey+"-]");
					while(lot.hasNext())
					{
						isdetFlag = true;
						String lotKeyVal=lot.next();
						String detStr = (String)mapDet.get(lotKeyVal);
						String[] detHdr = detStr.split(",");
						xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"sord_alloc\" objContext=\"2\">"); 
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
						xmlBuff.append("<line_no><![CDATA["+Integer.parseInt((String)detHdr[0])+"]]></line_no>");
						xmlBuff.append("<tran_id/>");
						xmlBuff.append("<sale_order><![CDATA["+ (String)detHdr[1] +"]]></sale_order>");					
						xmlBuff.append("<line_no__sord><![CDATA["+(String)detHdr[2]  +"]]></line_no__sord>");
						xmlBuff.append("<item_code><![CDATA["+ (String)detHdr[3] +"]]></item_code>");
						xmlBuff.append("<loc_code><![CDATA["+(String)detHdr[4]+"]]></loc_code>");
						xmlBuff.append("<lot_sl><![CDATA["+ (String)detHdr[5] +"]]></lot_sl>");
						xmlBuff.append("<lot_no><![CDATA["+(String)detHdr[6]+"]]></lot_no>");
						xmlBuff.append("<quantity><![CDATA["+ Double.parseDouble((String)detHdr[7]) +"]]></quantity>");	
						xmlBuff.append("<dealloc_qty><![CDATA[0]]></dealloc_qty>");	
						xmlBuff.append("<site_code><![CDATA["+(String)detHdr[8]  +"]]></site_code>");
						xmlBuff.append("<exp_lev><![CDATA["+ (String)detHdr[9] +"]]></exp_lev>");					
						xmlBuff.append("<pending_qty><![CDATA["+Double.parseDouble((String)detHdr[10])  +"]]></pending_qty>");					
						xmlBuff.append("</Detail2>"); 
					}
				}
			}
			else{
			list2.add(saleOrder);
			list2.add(lineNo);
			list2.add(itemCode);
			list2.add(allocQty);
			elements2 ++;System.out.println("elements2 ++ empty list ::"+elements2);
			}
			}
			if(xmlBuff!=null)
			{
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
			}
			System.out.println("...............just before savdata()");
			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("==site code =="+siteCode);
			System.out.println("Printing xmlString"+xmlString);
			//added by Pavan R on 27/DEC/17 userId passwed to savData() and processRequest()
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("userId::["+userId+"]");
			System.out.println("Going to save data method++++++++++++++++++++++++");
            if(xmlString != null && xmlString.trim().length()>0 && isdetFlag)
            {
            	//retString = saveData(siteCode,xmlString,conn);
            	retString = saveData(siteCode,xmlString,userId,conn);
            }
			System.out.println("@@@@@2: retString11111111111:"+retString);
			if (retString.indexOf("Success") > -1)
			{
				System.out.println("@@@@@@3: retString22222222222"+retString);
				
				String[] arrayForTranId = retString.split("<TranID>");
				int endIndex = arrayForTranId[1].indexOf("</TranID>");
				System.out.println("***********going for confirmation************");
				String tranIdForIssue = arrayForTranId[1].substring(0,endIndex);
				System.out.println("@@@@@@@@ Tran id for conformation :- "+ tranIdForIssue);
				retString=allocPrc.confirm(tranIdForIssue, xtraParams, "",conn);
				
				System.out.println("@@@@@@3: retString Confirm"+retString);
				System.out.println("=====Its Confirmed=====");
				System.out.println("retString from conf ::"+retString);
			}					
			else
			{
				//System.out.println("[" + retString + "]");	
//				return retString ;
				return errString  = itmDBAccessEJB.getErrorString("", "VSTQTYER", "", "", conn);
		    }
			 	//CHANGE BY RITESH ON 18/07/14 END
			rs.close();
			pstmtStock.clearParameters();
			pstmtStock.close();
			System.out.println("manohar connection  errString :::"+ errString );
			System.out.println("manoj if Quantity is fuly allocated commiting transaction else rollbacking :::"+ allocQty );
			if(allocQty==0 || allocQtyFlag)
			{
				conn.commit();
				stkAllocateFlag = true;
				System.out.println("Transaction commited while  allocQty =="+allocQty);
				if(allocQty != 0 )
				{
					errString = itmDBAccessEJB.getErrorString("", "VSTQTYER", "", "", conn);
				}
			}
			else
			{
				System.out.println("connection rollback____[]");
				//errCode = "VSTQTYER";
				errString = itmDBAccessEJB.getErrorString("", "VSTQTYER", "", "", conn);
				//errString = "VSTQTYER";
				conn.rollback();
			}
		
		}
		catch(SQLException se)
		{
			System.out.println("SQLException :" + se);
			se.printStackTrace();
			errString = se.getMessage();
			System.out.println("manohar connection rolledback errString 3 :::"+ errString );
			conn.rollback();
			return errString;
		}

		catch(Exception e)
		{
			System.out.println("Exception :" + e);
			errString = e.getMessage();
			e.printStackTrace();
			try
			{
				System.out.println("manohar connection rolledback errString 2 :::"+ errString );
				conn.rollback();
			}
			catch(Exception e1)
			{
				e = e1;
			}
			return errString ;


		}
		finally
		{
			try
			{
				if (errString == null || errString.trim().length() == 0)
				{
					System.out.println("manohar connection commited errString :::"+ errString );
					conn.commit();
					
				}
				if(conn != null)
				{	
					if(pstmtStock != null)
					{
						pstmtStock.close();
						pstmtStock = null;
					}
					if(pstmtStockInsertSordAlloc != null)
					{
						pstmtStockInsertSordAlloc.close();
						pstmtStockInsertSordAlloc = null;
					}
					if(st != null)
					{
						st.close();
						st = null;
					}
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				System.out.println("::@errString@:::"+errString);
				return errString ;

			}
			return errString;
		}

	}

	//	updateInvallocTrace
	public String updateInvallocTrace(InvAllocTrace invallocTrace, Connection conn) throws ITMException
	{
		String errString = "";
		String errCode = "" ;
		String sql = null;
		String sqlUpdate = null;
		String keyStringQuery = null;
		java.sql.Date tranDate = invallocTrace.getTranDate();
		String refSer = invallocTrace.getRefSer();
		String refId = invallocTrace.getRefId();
		String refLine = invallocTrace.getRefLine();
		String siteCode = invallocTrace.getSiteCode();
		String itemCode = invallocTrace.getItemCode();
		String locCode = invallocTrace.getLocCode();
		String lotNo = invallocTrace.getLotNo();
		String lotSl = invallocTrace.getLotSl();
		double allocQty = invallocTrace.getAllocQty();
		String chgUser = invallocTrace.getChgUser();
		String chgTerm = invallocTrace.getChgTerm();
		String chgWin = invallocTrace.getChgWin();
		java.sql.Date chgDate = new java.sql.Date(System.currentTimeMillis());
		//Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rSet = null ;
		Statement stmt = null;
		String tranId = null;

		try
		{
			keyStringQuery = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'T_ALLOCTRACE'";
			System.out.println("keyStringQuery--------->>"+keyStringQuery);
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			//connDriver = null;
			stmt = conn.createStatement();
			rSet = stmt.executeQuery(keyStringQuery);
			System.out.println("keyString :"+rSet.toString());
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rSet.next())
			{
				keyString = rSet.getString(1);
				keyCol = rSet.getString(2);
				tranSer1 = rSet.getString(3);				
			}
			// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [Start]
			if( stmt != null )
			{
				stmt.close();
				stmt = null;
			}
			if ( rSet != null )
			{
				rSet.close();
				rSet = null;
			}
			// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [End]
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(genericUtility.getDBDateFormat());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			String tranDateStr = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer1 :"+tranSer1);

			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			//Added by Manoj dtd 10/09/2012 to generate key
			xmlValues = xmlValues + "<dummy>D</dummy>";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
			//Added by Manoj dtd 10/09/2012 to generate key
			xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>";
			xmlValues = xmlValues +"</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);

			sql = "INSERT INTO INVALLOC_TRACE (TRAN_ID, TRAN_DATE, REF_SER, REF_ID,"
					+"REF_LINE,ITEM_CODE, SITE_CODE, LOC_CODE,LOT_NO, LOT_SL, ALLOC_QTY, CHG_WIN," 
					+"CHG_USER, CHG_TERM, CHG_DATE )VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			pstmt = conn.prepareStatement(sql);

			System.out.println("trans id="+tranId);
			System.out.println("tranDate="+tranDate);
			System.out.println("refSer="+refSer);
			System.out.println("refId="+refId);
			System.out.println("refLine="+refLine);
			System.out.println("itemCode="+itemCode);
			System.out.println("siteCode="+siteCode);
			System.out.println("locCode="+locCode);
			System.out.println("lotNo="+lotNo);
			System.out.println("LOT_SL="+lotSl);
			System.out.println("LLOC_QTY="+allocQty);
			System.out.println("chgWin="+chgWin);
			System.out.println("chgUser="+chgUser);
			System.out.println("chgTerm="+chgTerm);
			System.out.println("chgDate="+chgDate);

			pstmt.setString(1,tranId);
			pstmt.setDate(2,tranDate);
			pstmt.setString(3,refSer);
			pstmt.setString(4,refId);
			pstmt.setString(5,refLine);
			pstmt.setString(6,itemCode );
			pstmt.setString(7,siteCode);
			pstmt.setString(8,locCode);
			pstmt.setString(9,lotNo );
			pstmt.setString(10,lotSl);
			pstmt.setDouble(11,allocQty);
			pstmt.setString(12,chgWin);
			pstmt.setString(13,chgUser);
			pstmt.setString(14,chgTerm);
			pstmt.setDate(15,chgDate);
			pstmt.executeUpdate();
			System.out.println("insertion of sql inside updateInvallocTrace success on date "+ chgDate);

			System.out.println("Stock Updated...............................");
			if(allocQty >= 0)
			{

				sqlUpdate = "UPDATE STOCK SET ALLOC_QTY =(CASE WHEN ALLOC_QTY IS NULL THEN 0 ELSE ALLOC_QTY END) + ? "
						+"WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?";
				pstmt = conn.prepareStatement(sqlUpdate);
				pstmt.setDouble(1,allocQty);
				pstmt.setString(2,itemCode);
				pstmt.setString(3, siteCode);
				pstmt.setString(4,locCode);
				pstmt.setString(5,lotNo);
				pstmt.setString(6,lotSl);
				pstmt.executeUpdate();
			}
			System.out.println("Updated End.");
		}
		catch(SQLException e)
		{
			System.out.println("SQLException :updateInvallocTrace : "  + sqlUpdate + "\n" +e.getMessage());
			System.out.println("ALLOC_QTY : " + allocQty);
			System.out.println("ITEM_CODE : " + itemCode);
			System.out.println("SITE_CODE : " + siteCode);
			System.out.println("LOC_CODE : " + locCode);
			System.out.println("LOT_NO : " + lotNo);
			System.out.println("LOT_SL : " + lotSl);
			errString = e.getMessage();
			e.printStackTrace();
			return errString;

		}
		catch(Exception e)
		{
			System.out.println("manohar :updateInvallocTrace connection rollback:"  + sqlUpdate + "\n" +e.getMessage());
			errString = e.getMessage();
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				e = e1;
			}
			return errString;	

		}
		finally
		{
			try
			{
				//if(conn != null)
				//{	
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				//conn.close();
				//conn = null;
				//}
			}
			catch(Exception e)
			{
				errString = e.getMessage(); 
				e.printStackTrace();
				return errString;	
			}
			return errString;	
		}

	}
	/*Commented By Manoj Sharma dtd 29/032012 Not required as discussed with Manohar Sir
	private String postOrder(ArrayList saleOrderArr, String xtraParams) throws ITMException
	{
		String errString = "";
		String lotSl= null ;
		String tranId = null ;
		String sql = null;
		String adjDrcr = null ;
		String adjCustAdv = null ;
		String adjAdvMode = null ;
		String custCode = "";
		String dueDate = "";
		String orderType = "";
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		StringBuffer TabSepStrBuff = new StringBuffer();

		nvo_business_object_win_methods comp = null;
		try
		{
			System.out.println("Looking Up for NVO..........");
			Properties props = new Properties();
			props.put("org.omg.CORBA.ORBClass", "com.sybase.CORBA.ORB");
			ORB orb = ORB.init(((String []) (null)), props);
			SessionManager.Manager manager = ManagerHelper.narrow(orb.string_to_object("iiop://192.168.0.217:9000"));
			SessionManager.Session session = manager.createSession("jagadmin", "");
			comp = nvo_business_object_win_methodsHelper.narrow(factory.create());

			System.out.println("saleOrderArr.size() ::" + saleOrderArr.size());
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;

			for(int saleOrderCtr = 0;saleOrderCtr < saleOrderArr.size(); saleOrderCtr++)
			{
				System.out.println("sale order from is ::: "+(saleOrderArr.get(saleOrderCtr)).toString());
				String saleOrderStr = (saleOrderArr.get(saleOrderCtr)).toString();
				System.out.println("saleOrderStr ---- "+ saleOrderStr);
				sql = "SELECT CUST_CODE, DUE_DATE, ORDER_TYPE FROM SORDER WHERE SALE_ORDER ='"+ ((saleOrderArr.get(saleOrderCtr)).toString()) +"'" ;  
				st = conn.createStatement();
				rs = st.executeQuery(sql);
				System.out.println(sql);
				while(rs.next())
				{
					custCode = rs.getString(1);
					dueDate = rs.getString(2);
					orderType = rs.getString(3);
					TabSepStrBuff.append(saleOrderStr).append("\t") ;
					System.out.println("sale order from is ::: "+saleOrderStr);
					TabSepStrBuff.append(saleOrderStr).append("\t") ;
					System.out.println("sale order to is ::: "+saleOrderStr);
					TabSepStrBuff.append(custCode).append("\t");
					System.out.println("custCode from::"+ custCode);
					TabSepStrBuff.append(custCode).append("\t");
					System.out.println("custCode to ::"+custCode);
					TabSepStrBuff.append(dueDate).append("\t");
					System.out.println("dueDate from ::"+ dueDate);
					TabSepStrBuff.append(dueDate).append("\t");
					System.out.println("dueDate to ::"+ dueDate);
					//lot_sl 
					TabSepStrBuff.append(" ").append("\t");

					TabSepStrBuff.append(" ").append("\t");
					System.out.println("orderType:::"+orderType);
					adjDrcr = itmDBAccessEJB.getEnvDis("999999", "ADJUST_DR_CR_NOTE",conn);
					if(adjDrcr.equals("NULLFOUND"))
					{
						adjDrcr = "N";
					}
					TabSepStrBuff.append(adjDrcr).append("\t");
					adjCustAdv = itmDBAccessEJB.getEnvDis("999999", "ADJUST_CUST_ADV",conn);
					if(adjCustAdv.equals("NULLFOUND"))
					{
						adjCustAdv = "N";
					}
					TabSepStrBuff.append(adjCustAdv).append("\t");
					//club_pend_ord
					TabSepStrBuff.append("N").append("\t");
					//alloc_stock
					TabSepStrBuff.append("N").append("\t");
					//club_order
					TabSepStrBuff.append("N").append("\t");
					//rfresh_db
					TabSepStrBuff.append("N").append("\t");
					//ib_changed
					TabSepStrBuff.append("0").append("\t");
					adjAdvMode = itmDBAccessEJB.getEnvDis("999999", "ADJ_ADV_CUST_SALE",conn);
					if(adjAdvMode.equals("NULLFOUND"))
					{
						adjAdvMode = "C";
					}
					TabSepStrBuff.append(adjAdvMode).append("\t");
					System.out.println("adjAdvMode::"+ adjAdvMode);
					TabSepStrBuff.append(siteCode).append("\t");
					System.out.println("siteCode::"+ siteCode);

					TabSepStrBuff.append("TEMP SITE").append("\n");

					String retTabSepStr = TabSepStrBuff.toString();

					System.out.println("Tabstring :" + retTabSepStr);

					errString = comp.gbf_process("nvo_bo_post_order",retTabSepStr , "" , xtraParams);

					if (errString != null && errString.trim().length() > 0)
					{
						System.out.println("errString :"+ errString);
						return errString ;
					}
				}

			}
		}//try
		catch(SQLException se)
		{
			System.out.println("SQLException :" + se);
			se.printStackTrace();
			errString = se.getMessage();
			return errString ;

		}
		catch(Exception e)
		{
			System.out.println("Exception :ProcessNVOService :getComponent :==>\n"+e.getMessage());
			e.printStackTrace();
			errString = e.getMessage();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				e = e1;
			}
			return errString ;

		}
		finally
		{
			try
			{
				if(conn != null)
				{	
					if(st != null)
					{
						st.close();
						st = null;
					}
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString ; 
			}
			return errString ;
		}

	}*/
	void cleanup()
	{
		itemCodeMap.clear();
	}
	
	
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		//System.out.println("saving data...........");
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
	
	
	
	private HashMap getItemVoumeMap(String itemCode,String lotNo,Connection con)throws Exception
	{
		double packSize = 0,itemSize = 0,lotSize = 0;
		PreparedStatement pstmt = null;
		String sql="";
		ResultSet rs = null;
		double itmLen = 0,itmWidth = 0,itmHeight = 0,itemWeight = 0,lotLen = 0 ,lotHeight = 0,lotWidth = 0,lotWeight = 0;
		HashMap dataVolumeMap = new HashMap();

		try {

			sql = "SELECT I.LENGTH ITEM_LEN,I.WIDTH ITEM_WID,I.HEIGHT ITEM_HEIGHT,I.GROSS_WEIGHT ITEM_WEIGHT,"
					+" L.LENGTH LITEM_LEN,L.WIDTH LITEM_WID,L.HEIGHT LITEM_HEIGHT,L.SHIPPER_SIZE SHIPSIZE,L.GROSS_WEIGHT LOT_WEIGHT FROM"
					+" ITEM I,ITEM_LOT_PACKSIZE L"
					+" WHERE I.ITEM_CODE = L.ITEM_CODE"
					+" AND L.LOT_NO__FROM <= ? AND L.LOT_NO__TO >= ?"
					+" AND  I.ITEM_CODE = ?";


			pstmt = con.prepareStatement(sql);
			if(lotNo != null && lotNo.length() > 0)
			{
				pstmt.setString(1, lotNo);
				pstmt.setString(2, lotNo);
			}
			else
			{
				pstmt.setString(1, "00");
				pstmt.setString(2, "ZZ");
			}
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();

			if(rs.next())
			{
				itmLen = rs.getDouble("ITEM_LEN");
				itmWidth = rs.getDouble("ITEM_WID");
				itmHeight = rs.getDouble("ITEM_HEIGHT");
				itemWeight = rs.getDouble("ITEM_WEIGHT");
				lotLen = rs.getDouble("LITEM_LEN");
				lotWidth = rs.getDouble("LITEM_WID");
				lotHeight = rs.getDouble("LITEM_HEIGHT");				
				packSize = rs.getDouble("SHIPSIZE");				
				lotWeight = rs.getDouble("LOT_WEIGHT");				
			}

			//packSize = (lotHeight * lotWidth * lotLen)/(itmLen * itmWidth * itmHeight);
			/*itemSize = Math.floor(itmLen * itmWidth * itmHeight);
			lotSize = Math.floor((lotHeight * lotWidth * lotLen));*/
			itemSize = itmLen * itmWidth * itmHeight;
			lotSize = lotHeight * lotWidth * lotLen;

			dataVolumeMap.put("PACK_SIZE", packSize);
			dataVolumeMap.put("ITEM_SIZE", itemSize);
			dataVolumeMap.put("LOT_SIZE", lotSize);
			dataVolumeMap.put("ITEM_WEIGHT", itemWeight);
			dataVolumeMap.put("PACK_WEIGHT", lotWeight);

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

		} catch (Exception e) {
			// TODO: handle exception

			throw e;
		}
		finally
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

		return dataVolumeMap;
	}

}