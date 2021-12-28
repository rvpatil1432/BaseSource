/*
    Window Name : w_sord_dealloc
    Dharmendra Debata
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
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.webitm.ejb.dis.adv.StockDeAllocConf;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

//public class StockDeallocationPrcEJB extends ProcessEJB implements SessionBean
public class StockDeallocationPrc  extends ProcessEJB  implements StockDeallocationPrcLocal,StockDeallocationPrcRemote //SessionBean
{
	String batchId = null;
	String siteCode = null;
	String custCodeFr = null;
	String custCodeTo = null;

	String saleOrderFr = null;
	String saleOrderTo = null;
	String chgUser = null;
	String chgTerm = null;

	InvAllocTraceBean invallocTraceBean = new InvAllocTraceBean();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	//changed by rajendra on 02/11/07 to remove global connection
	//ConnDriver connDriver = new ConnDriver();
	CommonConstants commonConstants = new CommonConstants();
	HashMap invallocTraceMap = new HashMap();


	/*	public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("StockDeallocationPrcEJB ejbCreate called.........");

		}
		catch (Exception e)
		{
			System.out.println("Exception :StockDeallocationPrcEJB :ejbCreate :==>"+e);
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

		}
		catch (Exception e)
		{
			System.out.println("Exception :StockDeallocationPrcEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();
		}

		return rtrStr;
	}

	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println(" change done by RK.");
        String batchsql= "";
		String errCode = "" ;
		String errString = "";
		String getDataSql= "";
		String resultString = "";
		String sqlDealloc ="";
		//added by Kunal on 22/10/12 start
		String itemSerFr = "";
		String itemSerTo = "";
		String DefaultQtyFlag= "";
		String itemCodeFr = "";
		String itemCodeTo = "";
		String postOrderFg = "";
		String sDateTo = "";
		String sDateFr = "";
		String sql = "";
		double balStockQty = 0;
		double holdQty = 0;
		java.sql.Timestamp dateTo =  null;
		java.sql.Timestamp dueDate =  null;
		java.sql.Timestamp dateFr =  null;
		//added by Kunal on 22/10/12 end

		//Changed by sumit on 27/11/12 start
		String waveFlagRet = "";
		String wavesql = "";
		//Changed by sumit on 27/11/12 end
		int cnt1=0;
		String sql1="",waveType="";
		PreparedStatement pstmt2 = null;
		ResultSet rs2 = null; 
		String activeAllow ="";
		Connection conn = null;
		ResultSet rs=null,rsDealloc= null,rs1=null;
		PreparedStatement pstmt = null ,pstmtDeAlloc=null;
		Statement st = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();
		ConnDriver connDriver = new ConnDriver();	 //added by rajendra on 02/11/07
		retTabSepStrBuff = new StringBuffer("<?xml version = \"1.0\"?>");
		retTabSepStrBuff.append("<DocumentRoot>");
		retTabSepStrBuff.append("<description>").append("Datawindow Root").append("</description>");
		retTabSepStrBuff.append("<group0>");
		retTabSepStrBuff.append("<description>").append("Group0 description").append("</description>");
		retTabSepStrBuff.append("<Header0>");
		try
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;

			siteCode = genericUtility.getColumnValue("site_code",headerDom);
			if ( siteCode == null || siteCode.trim().length() == 0 )
			{
				siteCode = "";
				System.out.println("Site Code From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("SITE CODE "+ siteCode);

			custCodeFr = genericUtility.getColumnValue("cust_code__from",headerDom);
			if ( custCodeFr == null || custCodeFr.trim().length() == 0 )
			{
				custCodeFr = "";
				System.out.println("Cust Code From is Null...");
			}
			System.out.println("custCodeFr"+ custCodeFr);
			custCodeTo = genericUtility.getColumnValue("cust_code__to",headerDom);
			if ( custCodeTo == null || custCodeTo.trim().length() == 0 )
			{
				custCodeTo = "";
				System.out.println("Cust Code To is Null...");

			}
			System.out.println("custCodeTo"+ custCodeTo);
			saleOrderFr = genericUtility.getColumnValue("sale_order__from",headerDom);
			if ( saleOrderFr == null || saleOrderFr.trim().length() == 0 )
			{
				saleOrderFr = "";
				System.out.println("Sale Order From is Null...");

			}

			saleOrderTo = genericUtility.getColumnValue("sale_order__to",headerDom);
			if ( saleOrderTo == null || saleOrderTo.trim().length() == 0 )
			{
				saleOrderTo = "";
				System.out.println("Sale Order To is Null...");

			}
			if(saleOrderFr.trim().length()==0 && saleOrderTo.trim().length()==0 && custCodeFr.trim().length()==0 && custCodeTo.trim().length()==0)
			{
				errCode ="VTMRPPARM";
				resultString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				System.out.println("resultString: "+resultString);
				return resultString;

			}
			//added by Kunal on 22/10/12
			postOrderFg = genericUtility.getColumnValue("post_order_flag",headerDom);
			if(postOrderFg == null || postOrderFg.trim().length() == 0)
			{
				postOrderFg = "";
				System.out.println("Site Code From is Null...");
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

			sDateFr = genericUtility.getColumnValue("due_date__from",headerDom);
			if ( sDateFr == null || sDateFr.trim().length() == 0 )
			{
				sDateFr = "";
				System.out.println("Date From is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("due_date__from"+sDateFr);
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
			System.out.println("due_date__to"+sDateTo);

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
			//Changed by sumit on 27/11/12 getting value of wave_flag start
			waveFlagRet = genericUtility.getColumnValue("wave_flag",headerDom);
			System.out.println(" sumit - waveFlagRet ["+waveFlagRet+"]");
			if( !"B".equalsIgnoreCase(waveFlagRet) || "Y".equalsIgnoreCase(waveFlagRet) || "N".equalsIgnoreCase(waveFlagRet) )
			{
				System.out.println(" in side 'B' ");
				wavesql = " AND CASE WHEN SORDALLOC.WAVE_FLAG IS NULL THEN 'N' ELSE SORDALLOC.WAVE_FLAG END = ? ";
			}
			else
			{
				wavesql = " ";
			}
			System.out.println(" wavesql ["+wavesql+"]");
			//Changed by sumit on 27/11/12 getting value of wave_flag end			

			//added by Kunal on 22/10/12 
		
			batchId = genericUtility.getColumnValue("batch_id",headerDom);
			//changes by vishakha on 22-jan-2015 for D14JSUN006
			
			
		if(batchId == null || batchId.trim().length() == 0)
		{
			batchsql = "";
		}
		else{
			
			sql = "select count(*) from sordalloc where batch_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,batchId);
			rs=pstmt.executeQuery();
			if(rs.next()){
				cnt1=rs.getInt(1);
			}
			if(rs!=null){
			rs.close();
			rs=null;
			}
			if(pstmt!=null){
			pstmt.close();
			pstmt=null;
			}
			System.out.println("Count value for BATCH ID -->"+cnt1);	
			if(cnt1 == 0){
			errString = itmDBAccessEJB.getErrorString("","VMDBTCH","","",conn);
			return errString;
			}
			else
			{
			batchsql =" AND SORDALLOC.BATCH_ID = ? ";
			}
		}
		
			System.out.println("Batch Id>>>>>"+batchId);
			
			if(	saleOrderFr.trim().length()>0 && saleOrderTo.trim().length()>0)
			{
				getDataSql = "SELECT SORDALLOC.SALE_ORDER,SORDDET.LINE_NO,SORDITEM.EXP_LEV ,"
						+" SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,"
						+" SORDALLOC.QTY_ALLOC   PENDING_QUANTITY," //SORDITEM.QTY_ALLOC - SORDITEM.QTY_DESP  PENDING_QUANTITY,"
						+" SORDALLOC.LOT_NO,SORDALLOC.LOT_SL,SORDALLOC.LOC_CODE, "
						+" ITEM.GTIN_CASE,SORDITEM.ITEM_CODE,SORDER.SITE_CODE__SHIP ,ITEM.GTIN_UNIT "   //added by Kunal on 22/10/12
						//Changed by sumit on 26/11/12 retrieve wave flag
						+", CASE WHEN SORDALLOC.WAVE_FLAG IS NULL THEN 'N' ELSE SORDALLOC.WAVE_FLAG END AS WAVE_FLAG "
						+",SORDDET.UNIT,SORDDET.UNIT__STD,SORDDET.CONV__QTY_STDUOM,SORDDET.QUANTITY__STDUOM " 
						+ ",SORDER.CUST_CODE__DLV "		//ADD BY RITESH ON 08/10/13	
						+" FROM SORDDET,SORDALLOC,SORDITEM,CUSTOMER,ITEM, SORDER  "
						+" WHERE SORDER.SALE_ORDER = SORDDET.SALE_ORDER AND "
						+"(SORDALLOC.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
						+"( SORDALLOC.LINE_NO = SORDDET.LINE_NO ) AND "
						+"( SORDALLOC.EXP_LEV = SORDITEM.EXP_LEV ) AND "
						+"( SORDITEM.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
						+"( SORDITEM.LINE_NO = SORDDET.LINE_NO ) AND "
						+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
						+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND "
						//Commented by Manoj dtd 07/06/2013 to check site code from sorder table
						//+" SORDDET.SITE_CODE = ? AND "
						+" SORDER.SITE_CODE = ? AND "
						+" SORDER.SALE_ORDER >= ? AND "
						+" SORDER.SALE_ORDER <= ? AND "
						+" ITEM.ITEM_SER >=? AND "
						+" ITEM.ITEM_SER <=? AND "  
						+" CUSTOMER.CUST_CODE >=? AND "
						+" CUSTOMER.CUST_CODE <=? AND "  
						+" SORDER.DUE_DATE >=  ?  AND" 
						+" SORDER.DUE_DATE <= ?  AND "
						+" SORDITEM.ITEM_CODE >= ? AND "
						+" SORDITEM.ITEM_CODE <= ?  "
						+"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end  in ( 'P','H')  " //change done by kunal on 08/jan/13  remove the hold condition
						+"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
						+"AND CASE WHEN SORDALLOC.STATUS IS NULL THEN 'P' ELSE SORDALLOC.STATUS end = 'P' "
						//+"AND SORDITEM.QTY_ALLOC - SORDITEM.QTY_DESP > 0 "Commented by Manoj dtd  12/11/2013 to remove qty_desp condition
						+"AND SORDITEM.QTY_ALLOC> 0 "
						+"AND SORDITEM.LINE_TYPE = 'I'"	   
						//Changed by sumit on 27/11/12 adding wave flag condition in case of both wave						   
						+wavesql+batchsql+//changes by vishakha on 22-jan-2015 for D14JSUN006
						"ORDER BY SORDER.SALE_ORDER,SORDITEM.EXP_LEV,SORDITEM.ITEM_CODE__ORD";

				pstmt = conn.prepareStatement(getDataSql);
				
				pstmt.setString(1,siteCode);
				pstmt.setString(2,saleOrderFr);
				pstmt.setString(3,saleOrderTo);
				pstmt.setString(4,itemSerFr);
				pstmt.setString(5,itemSerTo);
				pstmt.setString(6,custCodeFr);
				pstmt.setString(7,custCodeTo);
				pstmt.setTimestamp(8,dateFr);
				pstmt.setTimestamp(9,dateTo);
				pstmt.setString(10,itemCodeFr);
				pstmt.setString(11,itemCodeTo);
			
				//Changed by sumit on 27/11/12 adding parameter start
				if( !"B".equalsIgnoreCase(waveFlagRet) || "Y".equalsIgnoreCase(waveFlagRet) || "N".equalsIgnoreCase(waveFlagRet) )
				{
					pstmt.setString(12,waveFlagRet);
					
					if(batchId != null ){
					pstmt.setString(13,batchId);//changes by vishakha on 22-jan-2015 for D14JSUN006
					}
				}
				else//changes by vishakha on 22-jan-2015 for D14JSUN006
				{
					if(batchId != null ){
					pstmt.setString(12,batchId);
					}
				}
				//Changed by sumit on 27/11/12 adding parameter end
				st = conn.createStatement();
				rs = pstmt.executeQuery();

				if(rs.next())
				{
					do
					{
						sql ="SELECT SUM(STOCK.HOLD_QTY ) " 
								+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
								+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
								+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
								+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
								+"AND INVSTAT.AVAILABLE = 'Y' "
								+"AND STOCK.ITEM_CODE = '"+ rs.getString(12) + "'"
								+"AND STOCK.SITE_CODE = '"+ rs.getString(13) +"'" 
								+"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY) > 0  ";
						rs1 = st.executeQuery(sql);
						if (rs1.next())
						{

							//balStockQty = rs1.getDouble(1);
							holdQty = rs1.getDouble(1);  

							System.out.println("  holdQty"+holdQty);


						}
						rs1.close();
						retTabSepStrBuff.append("<Detail2>");
						System.out.println("1111111WW111144");
						
						// change for arrange column sequence : on 11/feb/14 
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
						// COLUMN SEQ: CHANGE BY RITESH ON 11/FEB/2014 FOR SCM issue tracker point # 195-N
						//SALE_ORDER

						retTabSepStrBuff.append("<sale_order>").append("<![CDATA[" + rs.getString(1) +"]]>").append("</sale_order>");
						//LINE_NO

						retTabSepStrBuff.append("<line_no>").append("<![CDATA[" + rs.getString(2) +"]]>").append("</line_no>");
						//ITEM_CODE

						retTabSepStrBuff.append("<item_code>").append("<![CDATA[" + rs.getString(4) +"]]>").append("</item_code>");
						//ITEM_DESCR

						retTabSepStrBuff.append("<item_descr>").append("<![CDATA[" + rs.getString(5) +"]]>").append("</item_descr>");
						//STOCK QTY
						//retTabSepStrBuff.append("<stock_qty>").append("<![CDATA[" + allocStockQty +"]]>").append("</stock_qty>");
						
						//HOLD_QTY
						retTabSepStrBuff.append("<hold_qty>").append("<![CDATA[" + holdQty +"]]>").append("</hold_qty>");

						//QUANTITY

						retTabSepStrBuff.append("<quantity>").append("<![CDATA[" + rs.getString(6) +"]]>").append("</quantity>");
						//quantity__stduom
						retTabSepStrBuff.append("<quantity__stduom>").append("<![CDATA[" + rs.getString("QUANTITY__STDUOM") +"]]>").append("</quantity__stduom>");
						//Total allocated Qty added by kunal on 19/NOV/13
						retTabSepStrBuff.append("<tot_alloc_qty>").append("<![CDATA[" + rs.getDouble(7) +"]]>").append("</tot_alloc_qty>");

						//dealloc_qty
						//Start added by chandrashekar on 10-Apr-2015
						System.out.println("DefaultQtyFlag>>>>>>>"+DefaultQtyFlag);
						if(DefaultQtyFlag.equals("Y"))
						{
							retTabSepStrBuff.append("<dealloc_qty>").append("<![CDATA[" + rs.getDouble(7) +"]]>").append("</dealloc_qty>");
						}
						else
						{
							retTabSepStrBuff.append("<dealloc_qty>").append("<![CDATA["+0+"]]>").append("</dealloc_qty>");
						}
						//End added by chandrashekar on 10-Apr-2015
						//ADD BY RITESH ON 10/10/13	FOR REQUEST DI3GSUN017 START
						//active_pick_allow
						
						retTabSepStrBuff.append("<active_pick_allow>").append("<![CDATA[" + activeAllow +"]]>").append("</active_pick_allow>");

						//ADD BY RITESH ON 10/10/13	FOR REQUEST DI3GSUN017 END
						
						//unit
						retTabSepStrBuff.append("<unit>").append("<![CDATA[" + rs.getString("UNIT") +"]]>").append("</unit>");
						//unit__std
						retTabSepStrBuff.append("<unit__std>").append("<![CDATA[" + rs.getString("UNIT__STD") +"]]>").append("</unit__std>");
						//conv__qty_stduom
						retTabSepStrBuff.append("<conv__qty_stduom>").append("<![CDATA[" + rs.getString("CONV__QTY_STDUOM") +"]]>").append("</conv__qty_stduom>");
												
						//wave_flag   
						retTabSepStrBuff.append("<wave_flag>").append("<![CDATA[" + rs.getString("WAVE_FLAG") +"]]>").append("</wave_flag>");
						//EXP_LEV

						retTabSepStrBuff.append("<exp_lev>").append("<![CDATA[" + rs.getString(3) +"]]>").append("</exp_lev>");
						//CUST_CODE

						retTabSepStrBuff.append("<cust_code>").append("<![CDATA[ ]]>").append("</cust_code>");
						
						
						//
						//retTabSepStrBuff.append("<pending_quantity>").append("<![CDATA[" + rs.getDouble(9) +"]]>").append("</pending_quantity>");
						
						//gtin_case
						//	retTabSepStrBuff.append(rs.getString(11)== null ?"": rs.getString(11)).append("\t");
						retTabSepStrBuff.append("<gtin_case>").append("<![CDATA[" + (rs.getString(11)== null ?"": rs.getString(11)) +"]]>").append("</gtin_case>");

						//gtin_unit

						retTabSepStrBuff.append("<gtin_unit>").append("<![CDATA[" + (rs.getString(14)== null ?"": rs.getString(14)) +"]]>").append("</gtin_unit>");
						//LOT_NO

						retTabSepStrBuff.append("<lot_no>").append("<![CDATA[" + rs.getString(8) +"]]>").append("</lot_no>");
						//LOT_SL

						retTabSepStrBuff.append("<lot_sl>").append("<![CDATA[" + rs.getString(9) +"]]>").append("</lot_sl>");
						//LOC_CODE

						retTabSepStrBuff.append("<loc_code>").append("<![CDATA[" + rs.getString(10) +"]]>").append("</loc_code>");

						//ALLOC_TRANID
						//	retTabSepStrBuff.append(" ").append("\t");
						retTabSepStrBuff.append("<tranid>").append("<![CDATA[ ]]>").append("</tranid>");
						//ALLOC_LINENO
						//	retTabSepStrBuff.append(" ").append("\t");
						retTabSepStrBuff.append("<alloc_lineno>").append("<![CDATA[ ]]>").append("</alloc_lineno>");
						retTabSepStrBuff.append("<empty>").append("<![CDATA[ ]]>").append("</empty>");

						//	retTabSepStrBuff.append("  ").append("\t");

						retTabSepStrBuff.append("</Detail2>");

					}while(rs.next());

					retTabSepStrBuff.append("</Header0>");
					retTabSepStrBuff.append("</group0>");
					retTabSepStrBuff.append("</DocumentRoot>");
					resultString = retTabSepStrBuff.toString();
					System.out.println("ResultString....." + resultString);
					pstmt.clearParameters();

				}


			}
			else if(custCodeFr.trim().length()>0 && custCodeTo.trim().length()>0 )
			{

				sqlDealloc= "SELECT SORD_ALLOC.CUST_CODE,SORD_ALLOC_DET.ITEM_CODE, ITEM.DESCR, SORD_ALLOC_DET.QUANTITY,"
						+"SORD_ALLOC_DET.QUANTITY - SORD_ALLOC_DET.DEALLOC_QTY PENDING_QUANTY ,"
						+"SORD_ALLOC_DET.LOT_NO,SORD_ALLOC_DET.LOT_SL,SORD_ALLOC_DET.LOC_CODE , "
						+"SORD_ALLOC_DET.TRAN_ID,SORD_ALLOC_DET.LINE_NO ,"
						+"  ITEM.GTIN_CASE , SORDITEM.ITEM_CODE,SORDER.SITE_CODE__SHIP,ITEM.GTIN_UNIT "   //added by Kunal on 22/10/12
						+" FROM SORD_ALLOC,SORD_ALLOC_DET, ITEM  "
						+" WHERE SORD_ALLOC.TRAN_ID = SORD_ALLOC_DET.TRAN_ID "
						+" AND SORD_ALLOC_DET.ITEM_CODE = ITEM.ITEM_CODE "
						+"AND SORD_ALLOC.SALE_ORDER IS NULL "
						+"AND SORD_ALLOC.SITE_CODE= ? "
						+"AND SORD_ALLOC.CUST_CODE >=? AND SORD_ALLOC.CUST_CODE <=?  "
						+"AND SORDER.SALE_ORDER >= ? AND "
						+" SORDER.SALE_ORDER <= ? AND "
						+" ITEM.ITEM_SER >=? AND "
						+" ITEM.ITEM_SER <=? AND "  
						+" CUSTOMER.CUST_CODE >=? AND "
						+" CUSTOMER.CUST_CODE <=? AND "  
						+" SORDER.DUE_DATE >=  ?  AND" 
						+" SORDER.DUE_DATE <= ?  AND "
						+" SORDITEM.ITEM_CODE >= ? AND "
						+" SORDITEM.ITEM_CODE <= ?  "
						+"AND SORD_ALLOC_DET.QUANTITY -(CASE WHEN SORD_ALLOC_DET.DEALLOC_QTY IS NULL THEN 0  ELSE SORD_ALLOC_DET.DEALLOC_QTY END) > 0 ";

				pstmtDeAlloc = conn.prepareStatement(sqlDealloc);
				pstmtDeAlloc.setString(1,siteCode);
				pstmtDeAlloc.setString(2,custCodeFr);
				pstmtDeAlloc.setString(3,custCodeTo);
				pstmtDeAlloc.setString(4,saleOrderFr);
				pstmtDeAlloc.setString(5,saleOrderTo);
				pstmtDeAlloc.setString(6,itemSerFr);
				pstmtDeAlloc.setString(7,itemSerTo);

				pstmtDeAlloc.setTimestamp(8,dateFr);
				pstmtDeAlloc.setTimestamp(9,dateTo);
				pstmtDeAlloc.setString(10,itemCodeFr);
				pstmtDeAlloc.setString(11,itemCodeTo);

				st = conn.createStatement();
				rsDealloc = pstmtDeAlloc.executeQuery();
				if(rsDealloc.next())
				{

					do
					{
						sql ="SELECT SUM(STOCK.HOLD_QTY ) " 
								+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
								+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
								+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
								+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
								+"AND INVSTAT.AVAILABLE = 'Y' "
								+"AND STOCK.ITEM_CODE = '"+ rsDealloc.getString(12) + "'"
								+"AND STOCK.SITE_CODE = '"+ rsDealloc.getString(13) +"'" 
								+"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY) > 0  ";
						rs1 = st.executeQuery(sql);
						if (rs1.next())
						{

							//balStockQty = rs1.getDouble(1);
							holdQty = rs1.getDouble(1);  

							System.out.println("  holdQty"+holdQty);


						}
						rs1.close();



						//SALE_ORDER
						retTabSepStrBuff.append(" ").append("\t");
						//LINE_NO
						retTabSepStrBuff.append(" ").append("\t");
						//EXP_LEV
						retTabSepStrBuff.append(" ").append("\t");
						//CUST_CODE
						retTabSepStrBuff.append(rsDealloc.getString(1)).append("\t");
						//ITEM_CODE
						retTabSepStrBuff.append(rsDealloc.getString(2)).append("\t");
						//ITEM_DESCR
						retTabSepStrBuff.append(rsDealloc.getString(3)).append("\t");
						//QUANTITY
						retTabSepStrBuff.append(rsDealloc.getDouble(4)).append("\t");
						//PENDING_QTY
						retTabSepStrBuff.append(rsDealloc.getDouble(5)).append("\t");
						//LOT_NO
						retTabSepStrBuff.append(rsDealloc.getString(6)).append("\t");
						//LOT_SL
						retTabSepStrBuff.append(rsDealloc.getString(7)).append("\t");
						//LOC_CODE
						retTabSepStrBuff.append(rsDealloc.getString(8).trim()).append("\t");
						//ALLOC_TRANID
						retTabSepStrBuff.append(rsDealloc.getString(9).trim()).append("\t");
						//ALLOC_LINENO
						retTabSepStrBuff.append(rsDealloc.getString(10)).append("\t");

						retTabSepStrBuff.append("  ").append("\t");

						//hold qty
						retTabSepStrBuff.append(holdQty).append("\t");
						//gtin_case
						retTabSepStrBuff.append(rsDealloc.getString(11) == null ?" ":rsDealloc.getString(11)).append("\t"); 
						//gtin_unit
						retTabSepStrBuff.append(rsDealloc.getString(14) == null ?" ":rsDealloc.getString(14)).append("\n"); //added by Kunal on 25/10/12

						resultString = retTabSepStrBuff.toString();
						pstmt.clearParameters();

					}while(rsDealloc.next());



				}


				pstmtDeAlloc.close();
				rsDealloc.close();
			}


		}
		catch (SQLException e)
		{
			System.out.println("SQLException :StockDeallocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :StockDeallocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{

			try
			{
				cleanup();
				retTabSepStrBuff = null;

				if(conn != null)
				{
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					if(rs != null)
					{
						rs.close();
					}
					if(pstmtDeAlloc !=null)
					{
						pstmtDeAlloc.close();
						pstmtDeAlloc = null;
					}
					if(rsDealloc !=null)
					{
						rsDealloc.close();
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
			System.out.println("Exception :StockDeallocationPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
		}
		return retStr;

	}
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("detailDom------------------->"+detailDom);
		String childNodeName = "";
		String errCode = "";
		String siteCode = this.siteCode;
		String saleOrder = " ";
		String custCode=" ";
		String expLev = " " ;
		String itemCode = " ";
		String lineNo = " ";
		String locCode = " ";
		String lotSl= " ";
		String lotNo = " " ;
		String tranId=" ";
		String allocLineno=" ";
		double DeallocQty = 0 ,totDeallocQty = 0;
		double quantity = 0;
		String errString = " ";
		//Changed by sumit on 27/11/12 
		String waveFlag = "";
		Connection conn = null;

		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		PreparedStatement pstmt = null;
		Statement st = null;
		ResultSet rs=null;
		String siteCodeShip="";
		String activeAllow = "";
		ArrayList list = new ArrayList();
		ArrayList list1 = new ArrayList();
		ArrayList list2 = new ArrayList();
		boolean flag  = false,isFlag = true,allocqtyflag = false,pendingOrderFlag=false,isCompleteDealloc = true;
		double qtyAvailAlloc = 0;
		int elements = 0,elements1 = 0,cnt=0,elements2 = 0 ;
		ConnDriver connDriver = new ConnDriver();	//added by rajendra on 02/11/07
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String sql = "",shipStatus="",retString="",xmlString="";
		double qty=0d;
		HashMap<String, ArrayList<String>> hdrMap=new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<DetElement>> detailMap=new HashMap<String, ArrayList<DetElement>>();
		StringBuffer xmlBuff = null;
		Timestamp sysDate = null;
		Calendar currentDate = Calendar.getInstance();
		String userId = "";//Added By Pavan R 27/DEC/17
		try
		{
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

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
				parentNode = parentNodeList.item(selectedRow);

				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength---->>> "+ childNodeListLength);
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName---->>> "+ childNodeName);
					if (childNodeName.equals("quantity"))
					{
						quantity = Double.parseDouble(childNode.getFirstChild().getNodeValue());
						System.out.print("quantity::::"+ quantity );
					}

					if (childNodeName.equals("sale_order"))
					{
						saleOrder = childNode.getFirstChild().getNodeValue();
						System.out.print("saleOrder::::"+ saleOrder );

					}

					if (childNodeName.equals("line_no"))
					{
						lineNo = childNode.getFirstChild().getNodeValue();
						System.out.print("lineNo::::"+ lineNo );
					}

					if (childNodeName.equals("item_code"))
					{
						itemCode = childNode.getFirstChild().getNodeValue();
						System.out.print("itemCode::::"+ itemCode );
					}

					if (childNodeName.equals("dealloc_qty"))
					{
						DeallocQty = Double.parseDouble(childNode.getFirstChild().getNodeValue());
						System.out.print("DeallocQty::::"+ DeallocQty );
					}
					if (childNodeName.equals("exp_lev"))
					{
						expLev = childNode.getFirstChild().getNodeValue();
						System.out.print("expLev::::"+ expLev );
					}
					if (childNodeName.equals("cust_code"))
					{
						custCode = childNode.getFirstChild().getNodeValue();
						System.out.print("custCode::::"+ custCode );
					}
					if (childNodeName.equals("lot_sl"))
					{
						lotSl = childNode.getFirstChild().getNodeValue();
						System.out.print("lineNo::::"+ lineNo );
					}
					if (childNodeName.equals("lot_no"))
					{
						lotNo = childNode.getFirstChild().getNodeValue();
						System.out.print("lotSl::::"+ lotSl );
					}
					if (childNodeName.equals("loc_code"))
					{
						locCode = childNode.getFirstChild().getNodeValue();
						System.out.print("locCode::::"+ locCode );
					}
					if (childNodeName.equals("tranid"))
					{
						tranId = childNode.getFirstChild().getNodeValue();
						System.out.print("alloc_tranid::::"+ tranId );
					}
					if (childNodeName.equals("alloc_lineno"))
					{
						allocLineno = childNode.getFirstChild().getNodeValue();
						System.out.print("allocLineno::::"+ allocLineno );
					}
					if (childNodeName.equals("wave_flag"))
					{
						waveFlag = childNode.getFirstChild().getNodeValue();
						System.out.print("waveFlag :*::"+ allocLineno );
					}
					//added by ritesh on 10/10/13 for request DI3GSUN017 START
					if (childNodeName.equals("active_pick_allow"))
					{
						if(childNode.getFirstChild() != null)
						{
							activeAllow = childNode.getFirstChild().getNodeValue();
						}

					}
					if (childNodeName.equals("tot_alloc_qty"))
					{
						totDeallocQty = Double.parseDouble(childNode.getFirstChild().getNodeValue());
						System.out.print("Total  DeallocQty::::"+ totDeallocQty +"  "+DeallocQty);
					}

				}//inner for loop
				System.out.println("Tot DeallocQty"+totDeallocQty+"    "+DeallocQty);
				if(DeallocQty > totDeallocQty) //added by kunaal on 04/jan/14
				{
					isFlag = false;
					flag = true;
					isCompleteDealloc= false;//Added by chandrashekar on 08-04-2015
					list2.add(saleOrder);
					list2.add(lineNo);
					list2.add(itemCode);
					list2.add(DeallocQty);
					elements2 ++;
				}

				if(!"Y".equalsIgnoreCase(activeAllow) )
				{
					if(!"N".equalsIgnoreCase(activeAllow))
					{
						System.out.println("==activeAllow validate==");
						errString = itmDBAccessEJB.getErrorString("","VTACTALW","","",conn);
						return errString;

					}
				}                 			
				if("N".equalsIgnoreCase(activeAllow))
				{
					String sql1 = "";
					double shipperSize = 0 ,qtyDiff = 0;
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
					
					qtyDiff =  totDeallocQty -DeallocQty;
					System.out.println("qtyDiff:::"+qtyDiff);
					
					try{

						/*if(DeallocQty % shipperSize != 0)
						{
							flag = true;
							errString = itmDBAccessEJB.getErrorString("","VTACTALW2","","",conn);
							System.out.println("==activeAllow validate 4 N =="+errString);
						}*/
						if(qtyDiff < 0)
						{
							System.out.println("skip");
							continue;
						}
						if(qtyDiff % shipperSize != 0) //added by kunal on on 18/dec/13 check for remaining Qty
						{
							System.out.println("error..");
							flag = true;
							errString = itmDBAccessEJB.getErrorString("","VTACTALW2","","",conn);
						}
					}
					catch(Exception ex)
					{
						System.out.println("==ArithmeticException==");
						flag = true;
						errString = itmDBAccessEJB.getErrorString("","VTACTALW3","","",conn);
					}
					if( errString != null && errString.trim().length() > 0 && flag == true )
					{
						isFlag = false;
						isCompleteDealloc= false;//Added by chandrashekar on 08-04-2015
						list.add(saleOrder);
						list.add(lineNo);
						list.add(itemCode);
						list.add(DeallocQty);
						elements ++;
						errString = "";
					}
					

				}		//added by ritesh on 10/10/13 for request DI3GSUN017 END


				// added by cpatil on 27/11/13 as per manoj sir instruction start

				if( saleOrder != null && saleOrder.trim().length() > 0 )
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
						if( DeallocQty > 0 )
						{
							System.out.println("@@@@@@saleOrder["+saleOrder+"]:::DeallocQty["+DeallocQty+"]");
							flag = true;
							errString = "PROCSUCC"; //  "VTALLQTYSO";
							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						}
						if( errString != null && errString.trim().length() > 0 && flag == true )
						{
							//if( !(list1.contains(saleOrder)))   // commented for multiple line
							{
								isFlag = false;
								isCompleteDealloc= false;//Added by chandrashekar on 08-04-2015
								list1.add(saleOrder);
								list1.add(lineNo);
								list1.add(itemCode);
								list1.add(DeallocQty);
								elements1 ++;
								errString = "";
							}
						}
					}
				}
				// added by cpatil on 27/11/13 as per manoj sir instruction end



				System.out.println(" sumit wave flag - >["+waveFlag+"]");
				//Changed by sumit on 27/11/12 error msg if wave done condition start.
				//if(DeallocQty > 0 )

				/*	  // commented by cpatil on 04/12/13 as per manoj sir instruction because same functionality provided with line no
			    if( "Y".equalsIgnoreCase(waveFlag))
				{					
					errString="VTWAPROALL";
					errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
					System.out.println(" error ["+errString+"]");
					return errString;
				}				
				else if(DeallocQty > 0 && "N".equalsIgnoreCase(waveFlag) &&  flag == false)
				 */

				if(DeallocQty > 0 && "N".equalsIgnoreCase(waveFlag) &&  flag == false)	
				{
					//Changed by sumit on 27/11/12 error msg if wave done condition end.					
					System.out.println("if calling...*....");
					//Added by manoj dtd 07/06/2013 to Stock deallocate for Shit Code Ship
					String getDataSql = "SELECT SITE_CODE__SHIP FROM SORDER WHERE SALE_ORDER = ? ";
					pstmt = conn.prepareStatement(getDataSql);

					pstmt.setString(1,saleOrder);

					rs = pstmt.executeQuery();

					System.out.println("siteCode------"+ siteCode);
					if(rs.next())
					{
						siteCodeShip = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					qty=0;
					sql="SELECT ITEM_CODE,(CASE WHEN qty_alloc IS NULL THEN 0 ELSE qty_alloc END), EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					pstmt.setString(2,lineNo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						//itemCode = rs.getString("ITEM_CODE");
						qty  =  rs.getDouble(2);					
						expLev = rs.getString("EXP_LEV");
						System.out.println("=========qty========="+qty);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql="SELECT  CUST_CODE FROM  SORDER  WHERE  SALE_ORDER = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						custCode = rs.getString("cust_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					//errString = sorderDeAllocate(saleOrder,siteCodeShip, lineNo, itemCode, DeallocQty, expLev,custCode,lotSl,lotNo,locCode,tranId,allocLineno ,activeAllow,xtraParams );
					
					if(!hdrMap.containsKey(saleOrder))
					{
						ArrayList< String> hdrElem= new ArrayList<String>();
						hdrElem.add(custCode);
						hdrElem.add(saleOrder);
						hdrElem.add(siteCode);
						hdrElem.add(siteCodeShip);	
						hdrElem.add(chgTerm);	
						hdrElem.add(chgUser);
						hdrElem.add(activeAllow);
						hdrMap.put(saleOrder, hdrElem);
						DetElement detObj = new DetElement();
						detObj.setLineNo(lineNo);
						detObj.setItemCode(itemCode);
						detObj.setDeallocQty(DeallocQty);
						detObj.setExpLev(expLev);
						detObj.setLotNo(lotNo);
						detObj.setLotSl(lotSl);
						detObj.setLocCode(locCode);
						detObj.setSiteCode(siteCodeShip);
						detObj.setPendingQty(qty);
						
						ArrayList< DetElement> detElem= new ArrayList<DetElement>();
						detElem.add(detObj);
						detailMap.put(saleOrder, detElem);
					}
					else
					{
						DetElement detObj = new DetElement();
						detObj.setLineNo(lineNo);
						detObj.setItemCode(itemCode);
						detObj.setDeallocQty(DeallocQty);
						detObj.setExpLev(expLev);
						detObj.setLotNo(lotNo);
						detObj.setLotSl(lotSl);
						detObj.setLocCode(locCode);
						detObj.setSiteCode(siteCodeShip);
						detObj.setPendingQty(qty);
						
						ArrayList<DetElement> detElem= new ArrayList<DetElement>();
						detElem= detailMap.get(saleOrder);
						detElem.add(detObj);
						
						detailMap.put(saleOrder, detElem);
						
						
					}
					if (errString != null && errString.trim().length() > 0)
					{
						System.out.println("errString :"+errString);
						return errString;
					}else
					{
						allocqtyflag = true;
					}
				}				

			}// out for loop
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(
					sysDateStr, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			
			StockDeAllocConf confirmdealloc = new StockDeAllocConf();
			xmlBuff = new StringBuffer();
			
			String keyParam="";
			ArrayList< String> hdrList=null;
			Set< String> keySet =  hdrMap.keySet();
			Iterator< String> itr = keySet.iterator();
			while(itr.hasNext())
			{
				int linenoDet=0;
				keyParam=itr.next();
				hdrList= hdrMap.get(keyParam);
				System.out.println("hdrList"+hdrList);
				xmlBuff = new StringBuffer();
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("sorddealloc").append("]]></objName>");  
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
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorddealloc\" objContext=\"1\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<TRAN_DATE><![CDATA["+ sdf.format(sysDate) +"]]></TRAN_DATE>");					
				xmlBuff.append("<cust_code><![CDATA["+ hdrList.get(0) +"]]></cust_code>");
				xmlBuff.append("<sale_order><![CDATA["+ hdrList.get(1) +"]]></sale_order>");
				xmlBuff.append("<site_code><![CDATA["+ hdrList.get(2)  +"]]></site_code>");
				xmlBuff.append("<site_code__ship><![CDATA["+ hdrList.get(3)  +"]]></site_code__ship>");
				xmlBuff.append("<chg_date><![CDATA["+sdf.format(sysDate)+"]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA["+ chgTerm +"]]></chg_term>");						
				xmlBuff.append("<chg_user><![CDATA["+ chgUser  +"]]></chg_user>");
				xmlBuff.append("<add_date><![CDATA["+sdf.format(sysDate)+"]]></add_date>");
				xmlBuff.append("<add_term><![CDATA["+ chgTerm +"]]></add_term>");						
				xmlBuff.append("<add_user><![CDATA["+ chgUser  +"]]></add_user>");
				xmlBuff.append("<alloc_source><![CDATA[P]]></alloc_source>");	
				xmlBuff.append("<active_pick_allow><![CDATA["+ activeAllow +"]]></active_pick_allow>");						
				xmlBuff.append("</Detail1>");
				ArrayList<DetElement> detElem= detailMap.get(keyParam);
				for(int i=0; i<detElem.size();i++)
				{
					DetElement detObj=detElem.get(i);
					linenoDet++;
					System.out.println("keyParam["+keyParam+"]");
					System.out.println("detElemsize["+detElem.size()+"]");
					xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"sorddealloc\" objContext=\"2\">"); 
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<line_no><![CDATA["+linenoDet+"]]></line_no>");
					//xmlBuff.append("<line_no><![CDATA[1]]></line_no>");
					xmlBuff.append("<tran_id/>");
					xmlBuff.append("<sale_order><![CDATA["+ keyParam +"]]></sale_order>");					
					xmlBuff.append("<line_no__sord><![CDATA["+detObj.getLineNo()  +"]]></line_no__sord>");
					xmlBuff.append("<item_code><![CDATA["+ detObj.getItemCode() +"]]></item_code>");
					xmlBuff.append("<loc_code><![CDATA["+detObj.getLocCode()+"]]></loc_code>");
					xmlBuff.append("<lot_no><![CDATA["+ detObj.getLotNo() +"]]></lot_no>");
					xmlBuff.append("<lot_sl><![CDATA["+detObj.getLotSl()+"]]></lot_sl>");
					xmlBuff.append("<quantity><![CDATA["+ detObj.getDeallocQty() +"]]></quantity>");	
					xmlBuff.append("<dealloc_qty><![CDATA[0]]></dealloc_qty>");	
					xmlBuff.append("<site_code><![CDATA["+ detObj.getSiteCode()  +"]]></site_code>");
					xmlBuff.append("<exp_lev><![CDATA["+ detObj.getExpLev() +"]]></exp_lev>");					
					xmlBuff.append("<pending_qty><![CDATA["+ detObj.getPendingQty()  +"]]></pending_qty>");					
					xmlBuff.append("</Detail2>");
					
					
				}

				
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
				System.out.println("...............just before savdata()");
				System.out.println("Getting site code value");
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				System.out.println("==site code =="+siteCode);
				//added by Pavan R on 27/DEC/17 userId passwed to savData() and processRequest()
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				System.out.println("userId::["+userId+"]");
				System.out.println("Printing xmlString"+xmlString);
				System.out.println("conn:::	"+conn);
				
				retString = saveData(siteCode,xmlString,userId,conn);
				System.out.println("@@@@@2: retString11111111111:"+retString);
				if (retString.indexOf("Success") > -1)
				{
					System.out.println("@@@@@@3: retString22222222222"+retString);
					
					String[] arrayForTranId = retString.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					System.out.println("***********going for confirmation************");
					String tranIdForIssue = arrayForTranId[1].substring(0,endIndex);
					System.out.println("tranIdForIssue"+ tranIdForIssue);
					retString=confirmdealloc.confirm(tranIdForIssue, xtraParams, "",conn);
					
					System.out.println("@@@@@@3: retString Confirm"+retString);
					System.out.println("=====Its Confirmed=====");
					System.out.println("retString from conf ::"+retString);
					if(retString.indexOf("CONFSUCCES") > -1 )
					{
						conn.commit();
						System.out.println("complete dealloc>>>>>>>>>");
					}else
					{
						System.out.println(" confirm partial dealloc>>>>>>>>>>");
						isCompleteDealloc= false;
						isFlag = false;
					}	
					
				}else
				{
					System.out.println(" save partial dealloc>>>>>>>>>>");
					isCompleteDealloc= false;
					isFlag = false;
				}		
								
			}//while
			
			System.out.println("CHEK isFlag:::["+isFlag+"]");
			if (isFlag == false)
			{
				/*if(allocqtyflag = true)
				{
					System.out.println("@@@@@ inside if["+errString+"]");
					// errString = "VTACTALW2"; 
					errString = "PROCSUCC"; 
					errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				}*/
				//errString = "PROCSUCC"; //added by kunal on 04/JAN/13 
				//Start added by chandrashekar on 08-04-2015
				System.out.println("isCompleteDealloc>>>"+isCompleteDealloc);
				if(isCompleteDealloc)
				{
					errString="VTPROC";//added by chandrashekar on 12-09-2014
				}else
				{
					errString = "VTPRCERR"; 
				}
				System.out.println("errString>>>>"+errString);
				//End added by chandrashekar on 08-04-2015
				//errString="VTPROC";//added by chandrashekar on 12-09-2014
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
				String endPart = errString.substring( errString.indexOf("</trace>"));
				String mainStr = ""  ;
				if(elements > 0 || elements1 > 0 || elements2 > 0 )
				{ 
					mainStr = begPart + "Following error has occured\n" ;
				}
				if(elements > 0 )
				{ 
					mainStr	= mainStr + "Invalid quantity,Remaining quantity after deallocation is not in multiple of Case/shipper Quantity.\n";
				}
				for(int i = 0; i < elements * 4;i++)
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

				for(int i = 0; i < elements1 * 4 ;i++)
				{
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list1.get(i++)+ ",line no :"+ list1.get(i++) + ",item code :"+list1.get(i++)+",quantity :"+list1.get(i++)+"\n" ;
				}
				//added by kunal on 04/jan/13
				if(elements2 > 0 )
				{ 
					mainStr = mainStr + "\n Deallocate quantity more than Total Allocated Quantity. \n";
				}

				for(int i = 0; i < elements2 * 4 ;i++)
				{
					if( i > 0)
					{
						i -= 1;
					}
					mainStr = mainStr + 
							"sale order :"+list2.get(i++)+ ",line no :"+ list2.get(i++) + ",item code :"+list2.get(i++)+",quantity :"+list2.get(i++)+"\n" ;
				}
				if(mainStr.trim().length()==0)
				{
					mainStr = begPart;
				}//Condition added by chandrashekar on 08-04-2015
				mainStr = mainStr +  endPart;	
				errString = mainStr;
				begPart =null;
				endPart =null;
				mainStr =null;	
				System.out.println("errString>>>>>>>>["+errString+"]");
				return errString;

			}
			//Start added by chandrashekar on 15-09-2014
			System.out.println("errString["+errString+"]");
			if (errString == null || errString.trim().length() == 0)
			{
				errString="VTCOMPL";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}
			else if (errString.contains("VTWAPROALL"))
			{				
				return errString;
			}
			else if(errString.indexOf("VTACTALW2") > -1 || errString.indexOf("VTACTALW3") > -1 || errString.indexOf("VTACTALW") > -1 || errString.indexOf("PROCSUCC") > -1)
			{
				return errString;
			}
			else
			{
				errString="VTPRCERR";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}
			//end added by chandrashekar on 15-09-2014

		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :StockDeallocationPrcEJB :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			errString = e.getMessage();
			e.printStackTrace();
			return errString;
		}
		finally
		{
			System.out.println("Closing Connection....");
			try
			{
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
			/*System.out.println("errString["+errString+"]"); commented by chandrashekar on 15-09-2014
			if (errString == null || errString.trim().length() == 0)
			{
				errString="VTCOMPL";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}
			//Changed by sumit on 27/11/12 showing wave messages start.
			else if (errString.contains("VTWAPROALL"))
			{				
				return errString;
			}
			//Changed by sumit on 27/11/12 showing wave messages end.  // PROCSUCC
			else if(errString.indexOf("VTACTALW2") > -1 || errString.indexOf("VTACTALW3") > -1 || errString.indexOf("VTACTALW") > -1 || errString.indexOf("PROCSUCC") > -1)
			{
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

	/*private String sorderDeAllocate(String saleOrder,String siteCodeShip, String lineNo, String itemCode, double DeallocQty, String expLev,String custCode,String lotSl,String lotNo,String locCode,String tranId,String allocLineno ,String activeAllow,String xtraParams ) throws ITMException
	{
		System.out.println("updateSOrder calling ..............");
		String updateSorditem = null,updateSordAllocDet=null;
		String updateSql= null,DelQtyAlloc= null,SqlQtyAlloc= null ;
		String errString = "";
		String error = "";
		String errCode = "";
		Connection conn = null;
		PreparedStatement pstmtDeAlloc=null;
		Statement st = null;
		ResultSet rsQtyAlloc=null;
		String sql="";
		PreparedStatement pstmt2=null;
		ResultSet rs2=null;
		double qty=0;
		StringBuffer xmlBuff = null;
		ConnDriver connDriver = new ConnDriver();	 //added by manoharan
		Calendar currentDate = Calendar.getInstance();
		Timestamp sysDate = null;
		String xmlString = null,retString  = null,custCode1="";
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			st = conn.createStatement();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(
					sysDateStr, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			
			StockDeAllocConf confirmdealloc = new StockDeAllocConf();
			invallocTraceMap.put("ref_ser","D-ALOC");
			invallocTraceMap.put("site_code",siteCodeShip);
			invallocTraceMap.put("item_code",itemCode);
			invallocTraceMap.put("loc_code",locCode);
			invallocTraceMap.put("lot_no",lotNo);
			invallocTraceMap.put("lot_sl",lotSl);

			invallocTraceMap.put("alloc_qty",new Double(-1 * DeallocQty));
			

			System.out.print("DeallocQty = [" + (-1*DeallocQty)+"]");
			invallocTraceMap.put("chg_user",chgUser);
			invallocTraceMap.put("chg_term",chgTerm);
			invallocTraceMap.put("chg_win","W_SORD_DEALLOC");
			
			generate new confirmed transaction while process the data  from manual
            stock Deallocation process.Created new Transaction for deallocated quantity for StockAllocation.
            It will save and confirm the Stock allocation transactionfor allocated quantity
            
			//Start Added by Chandrashekar on 12-june-2014
			invallocTraceMap.put("ref_id",saleOrder);
			invallocTraceMap.put("ref_line",lineNo);
			if(DeallocQty>0 && saleOrder.trim().length()>0)
			{			
				sql="SELECT ITEM_CODE,(CASE WHEN qty_alloc IS NULL THEN 0 ELSE qty_alloc END), EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ?";
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
				
				sql="SELECT  CUST_CODE FROM  SORDER  WHERE  SALE_ORDER = ? ";
				pstmt2 =  conn.prepareStatement(sql);
				pstmt2.setString(1,saleOrder);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					custCode1 = rs2.getString("cust_code");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				
				System.out.println("custCode------["+custCode1+"]");				
				System.out.println("New Record Inserting*********============***********");				
				xmlBuff = new StringBuffer();
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("sorddealloc").append("]]></objName>");  
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
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorddealloc\" objContext=\"1\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<TRAN_DATE><![CDATA["+ sdf.format(sysDate) +"]]></TRAN_DATE>");					
				xmlBuff.append("<cust_code><![CDATA["+ custCode1 +"]]></cust_code>");
				xmlBuff.append("<sale_order><![CDATA["+ saleOrder +"]]></sale_order>");
				xmlBuff.append("<site_code><![CDATA["+ siteCodeShip  +"]]></site_code>");
				xmlBuff.append("<site_code__ship><![CDATA["+ siteCodeShip  +"]]></site_code__ship>");
				xmlBuff.append("<chg_date><![CDATA["+sdf.format(sysDate)+"]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA["+ chgTerm +"]]></chg_term>");						
				xmlBuff.append("<chg_user><![CDATA["+ chgUser  +"]]></chg_user>");
				xmlBuff.append("<add_date><![CDATA["+sdf.format(sysDate)+"]]></add_date>");
				xmlBuff.append("<add_term><![CDATA["+ chgTerm +"]]></add_term>");						
				xmlBuff.append("<add_user><![CDATA["+ chgUser  +"]]></add_user>");
				xmlBuff.append("<alloc_source><![CDATA[P]]></alloc_source>");	
				xmlBuff.append("<active_pick_allow><![CDATA["+ activeAllow +"]]></active_pick_allow>");						
				xmlBuff.append("</Detail1>");
				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"sorddealloc\" objContext=\"2\">"); 
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<line_no><![CDATA[1]]></line_no>");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<sale_order><![CDATA["+ saleOrder +"]]></sale_order>");					
				xmlBuff.append("<line_no__sord><![CDATA["+lineNo  +"]]></line_no__sord>");
				xmlBuff.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");
				xmlBuff.append("<loc_code><![CDATA["+locCode+"]]></loc_code>");
				xmlBuff.append("<lot_no><![CDATA["+ lotNo +"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+lotSl+"]]></lot_sl>");
				xmlBuff.append("<quantity><![CDATA["+ DeallocQty +"]]></quantity>");	
				xmlBuff.append("<dealloc_qty><![CDATA[0]]></dealloc_qty>");	
				xmlBuff.append("<site_code><![CDATA["+ siteCode  +"]]></site_code>");
				xmlBuff.append("<exp_lev><![CDATA["+ expLev +"]]></exp_lev>");					
				xmlBuff.append("<pending_qty><![CDATA["+ qty  +"]]></pending_qty>");					
				xmlBuff.append("</Detail2>");				
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
				System.out.println("...............just before savdata()");
				System.out.println("Getting site code value");
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				System.out.println("==site code =="+siteCode);
				System.out.println("Printing xmlString"+xmlString);
				System.out.println("	");

				retString = saveData(siteCode,xmlString,conn);
				System.out.println("@@@@@2: retString11111111111:"+retString);
				if (retString.indexOf("Success") > -1)
				{
					System.out.println("@@@@@@3: retString22222222222"+retString);
					
					String[] arrayForTranId = retString.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					System.out.println("***********going for confirmation************");
					String tranIdForIssue = arrayForTranId[1].substring(0,endIndex);
					System.out.println("tranIdForIssue"+ tranIdForIssue);
					retString=confirmdealloc.confirm(tranIdForIssue, xtraParams, "",conn);
					
					System.out.println("@@@@@@3: retString Confirm"+retString);
					System.out.println("=====Its Confirmed=====");
					System.out.println("retString from conf ::"+retString);
				}					
				else
				{
					return retString;
			    }
			}
			// End Added by Chandrashekar on 12-june-2014
			
			if (DeallocQty > 0 && saleOrder.trim().length()>0)
			{
				invallocTraceMap.put("ref_id",saleOrder);
				invallocTraceMap.put("ref_line",lineNo);
				errString = invallocTraceBean.updateInvallocTrace(invallocTraceMap,conn);

				if (errString != null && errString.trim().length() > 0)
				{
					System.out.println("errString :::"+ errString );
					return errString;
				}

				updateSorditem ="UPDATE SORDITEM  SET QTY_ALLOC = QTY_ALLOC - " + new Double(DeallocQty).toString()
						+" WHERE SALE_ORDER = '" + saleOrder + "' "
						+" AND LINE_NO = '" + lineNo + "' "
						+" AND EXP_LEV = '" + expLev + "' ";

				System.out.println("updateSql------->"+updateSorditem);
				st.executeUpdate(updateSorditem);
				System.out.println("UPDATE  SUCCESS FOR SORDITEM....>>>>>>>>");

				updateSql = " UPDATE SORDALLOC SET QTY_ALLOC =  QTY_ALLOC - " + new Double(DeallocQty).toString()
						+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
						+ " AND LINE_NO = '" + lineNo + "' "
						+ " AND EXP_LEV = '" + expLev + "' "
						+ " AND ITEM_CODE = '" + itemCode + "' "
						+ " AND LOT_NO = '" + lotNo + "' "
						+ " AND LOT_SL = '" + lotSl + "' "
						+ " AND LOC_CODE = '" + locCode + "' " ;
				System.out.println("updateSql:::>>>>"+ updateSql);
				st.executeUpdate(updateSql);
				System.out.println("UPDATE  SUCCESS FOR SORDALLOC....");
				SqlQtyAlloc = "SELECT QTY_ALLOC FROM SORDALLOC "
						+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
						+ " AND LINE_NO = '" + lineNo + "' "
						+ " AND EXP_LEV = '" + expLev + "' "
						+ " AND ITEM_CODE = '" + itemCode + "' "
						+ " AND LOT_NO = '" + lotNo + "' "
						+ " AND LOT_SL = '" + lotSl + "' "
						+ " AND LOC_CODE = '" + locCode + "' " ;
				rsQtyAlloc  = st.executeQuery(SqlQtyAlloc);
				if (rsQtyAlloc.next())
				{
					System.out.println("Updated Allocated Qty :::"+rsQtyAlloc.getDouble(1));
					if(rsQtyAlloc.getDouble(1)<=0)
					{
						DelQtyAlloc = "DELETE FROM SORDALLOC"
								+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
								+ " AND LINE_NO = '" + lineNo + "' "
								+ " AND EXP_LEV = '" + expLev + "' "
								+ " AND ITEM_CODE = '" + itemCode + "' "
								+ " AND LOT_NO = '" + lotNo + "' "
								+ " AND LOT_SL = '" + lotSl + "' "
								+ " AND LOC_CODE = '" + locCode + "' " ;
						System.out.println("DelQtyAlloc:::"+DelQtyAlloc);
						st.executeUpdate(DelQtyAlloc);
						System.out.println("Delete completed ");
					}
				}
				rsQtyAlloc.close();
			}
			//For saleOrder is null and you have cust_code
			else if(DeallocQty > 0 && tranId.trim().length()>0)
			{
				invallocTraceMap.put("ref_id",tranId);
				System.out.println("alloc_tranid::----"+tranId);
				invallocTraceMap.put("ref_line",allocLineno);
				System.out.println("siteCode::"+siteCode+"itemCode:"+itemCode+"locCode::"+locCode +"lotNo:"+lotNo+"lotSl:"+"QtyToBeDeallocated::"+(-1*DeallocQty)+"alloc_tranid::"+tranId+"allocLineno::"+allocLineno);
				errString = invallocTraceBean.updateInvallocTrace(invallocTraceMap,conn);
				if (errString != null && errString.trim().length() > 0)
				{
					System.out.println("errString :::"+ errString );
					return errString;
				}
				updateSordAllocDet= " UPDATE SORD_ALLOC_DET SET DEALLOC_QTY = DEALLOC_QTY + "+  new Double(DeallocQty).toString()
						+" WHERE SORD_ALLOC_DET.TRAN_ID='"+ tranId +"'"
						+" AND SORD_ALLOC_DET.LINE_NO ="+ allocLineno ;
				System.out.println("updateSordAllocDet::"+ updateSordAllocDet);
				st.executeUpdate(updateSordAllocDet);
				System.out.println("updateSordAllocDet completed successfully");
			}
			st.close();

			conn.commit();
		}
		catch(SQLException se)
		{
			System.out.println("SQLException :" + se);
			se.printStackTrace();
			return errString;
		}

		catch(Exception e)
		{
			System.out.println("Exception :" + e);
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
			return errString;
		}

	}*/
	void cleanup()
	{
		invallocTraceMap.clear();
	}
	private String checkNull( String input )
	{
		if ( input == null )
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
		System.out.println("-----retString["+retString+"]");
		return retString;
		
	}

}
class DetElement
{
	
	private String lineNo;
	private String itemCode;
	private String locCode;
	private String lotNo;
	private String lotSl;
	private double deallocQty;
	private String siteCode;
	private String expLev;
	private double pendingQty;
	
	public String getLineNo() {
		return lineNo;
	}
	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getLocCode() {
		return locCode;
	}
	public void setLocCode(String locCode) {
		this.locCode = locCode;
	}
	public String getLotNo() {
		return lotNo;
	}
	public void setLotNo(String lotNo) {
		this.lotNo = lotNo;
	}
	public String getLotSl() {
		return lotSl;
	}
	public void setLotSl(String lotSl) {
		this.lotSl = lotSl;
	}
	public double getDeallocQty() {
		return deallocQty;
	}
	public void setDeallocQty(double deallocQty) {
		this.deallocQty = deallocQty;
	}
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	public String getExpLev() {
		return expLev;
	}
	public void setExpLev(String expLev) {
		this.expLev = expLev;
	}
	public double getPendingQty() {
		return pendingQty;
	}
	public void setPendingQty(double pendingQty) {
		this.pendingQty = pendingQty;
	}
	
}