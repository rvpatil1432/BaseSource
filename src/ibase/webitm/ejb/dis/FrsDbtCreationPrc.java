

package ibase.webitm.ejb.dis;

/*
Changes:
1-On 10042007 receivables update missmatch
2-On 22052007 receivable amount become the -ve
*/

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.util.*;
import java.sql.*;
import java.io.*;
import org.w3c.dom.*;
import javax.ejb.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import java.util.*;
import javax.ejb.Stateless; // added for ejb3



//public class FrsDbtCreationPrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class FrsDbtCreationPrc extends ProcessEJB implements FrsDbtCreationPrcLocal, FrsDbtCreationPrcRemote
{

	//****added on 10042007
	Vector referenceVector =null;

	DistCommon distCommonObj = new DistCommon();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	CommonConstants commonConstants = new CommonConstants();
	InvAllocTraceBean InvAllocTraceObj = new InvAllocTraceBean();
	File filePtr = new File("c:\\pb10\\log\\pbnitrace.log");
	Connection conn = null;
	Vector salVec = new Vector();
	java.sql.Timestamp dateTo =  null;
	java.sql.Timestamp dueDate =  null;
	java.sql.Timestamp dateFr =  null;

	ArrayList saleOrderArr = new ArrayList();
	ArrayList custCodeArr = new ArrayList();
	ArrayList dueDateArr = new ArrayList();
	ArrayList sretInvList = null;
	ArrayList customerList = new ArrayList();

	HashMap itemCodeMap = new HashMap();
	HashMap custCodeMap = new HashMap();
	HashMap saleReturnMap = new HashMap();
	HashMap saleReturnDetMap = new HashMap();
	HashMap refNoMap = null;
	HashMap  invallocTraceMap = new HashMap();
	HashMap  stockInfo = new HashMap();
	StringBuffer retBuf = null;
	String chgUser = null;
	String chgTerm = null;
	String siteCode = null;
	String itemSerFr = null;
	String itemSerTo = null;
	String custCodeFr = null;
	String custCodeTo = null;
	String custCode = null;
	String DefaultQtyFlag= null;
	String saleOrderFr = null;
	String saleOrderTo = null;
	String postOrderFg = null;
	String frsFlag = null;
	String 	sql = null;
	String sDateTo = null;
	String sDateFr = null;
	String blankTaxString = null;
	String taxAmount =null;
	String sreturnID = ",";
	String tranType = "";
	String tranId  = "";
	String applCurrDate = getCurrdateAppFormat();
	double totalAmtAdjusted =0;

	double balStockQty = 0.0;
	boolean priceNotFound = false;
	double frsBalInit = 0;
	int bFlag=0;
	boolean allocateFlag = false;
	boolean  custFlag = false;
	String tranFlag = "V";
	File mkd = new File("C:\\pb10\\log");

 	/*public void ejbCreate() throws RemoteException, CreateException	{}
	public void ejbRemove(){}
	public void ejbActivate(){}
	public void ejbPassivate(){}*/
	public String process() throws RemoteException,ITMException
	{return "";}

//	******************************************* G E T D A T A****************************************************************************
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;


		if(!mkd.exists())
		{
			mkd.mkdirs();
			writeLog(filePtr,"Directory Built ["+mkd+"]",false);
			writeLog(filePtr,"F E T C H I N G  D A T A F R O M S E R V E R  F O R M U L T I C U S T O M E R( UPDATE ON 10042007)",true);
		}
		else
		{
				writeLog(filePtr,"F E T C H I N G  D A T A F R O M S E R V E R  F O R M U L T I C U S T O M E R ( UPDATE ON 10042007)",false);
		}
		writeLog(filePtr,"View Data Start Time["+new java.sql.Timestamp(System.currentTimeMillis()).toString()+"]",true);
		writeLog(filePtr,"windowName::::["+ windowName+"]",true);
		writeLog(filePtr,"xtraParams::::["+ xtraParams+"]",true);

		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				writeLog(filePtr,"xmlString:::::"+ xmlString,true);
				headerDom = genericUtility.parseString(xmlString);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				writeLog(filePtr,"xmlString2:::::"+ xmlString2,true);
				detailDom = genericUtility.parseString(xmlString2);
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{

			rtrStr = e.getMessage();
			writeLog(filePtr,e,true);

		}
		return rtrStr;
	}//END OF GETDATA(1)
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errCode = "";
		String errString = "";
		String resultString = "";
		String getDataSql= null ;
		String sql= null ;
		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement pstmt = null;
		Statement st = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();
		double pendQty = 0;
		double allocQty = 0;
		boolean bappend = false ;
		boolean frsappend = false ;
		java.util.HashMap refMap = new java.util.HashMap();

		try
		{
			if(conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				connDriver = null;

			}
			DatabaseMetaData dbmd = conn.getMetaData();

			writeLog(filePtr,"________________________________________",true);
			writeLog(filePtr,"        Driver Information              ",true);
			writeLog(filePtr,"________________________________________",true);
			writeLog(filePtr,"DriverName["+dbmd.getDriverName() + "]",true);
			writeLog(filePtr,"DriverURI["+dbmd.getURL()  + "]",true);
			writeLog(filePtr,"DriverUSER["+dbmd.getUserName() + "]",true);
			writeLog(filePtr,"ApplDateFormat["+genericUtility.getApplDateFormat() + "]",true);
			writeLog(filePtr,"DBDateFormat["+genericUtility.getDBDateFormat() + "]",true);
			writeLog(filePtr,"________________________________________",true);
		}
		catch (Exception e)
		{
			writeLog(filePtr,e,true);
			System.out.println("Exception :FrsDbtCreationPrcEJB getData:==>"+e.getMessage());
		}
		tranType = distCommonObj.getDisparams("999999","SRET_TYPE_FRS",conn);
		writeLog(filePtr,"Transation Type[GetData]:::["+tranType+"]",true);

		try
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			siteCode = genericUtility.getColumnValue("site_code",headerDom);
			if ( siteCode == null || siteCode.trim().length() == 0 )
			{
				siteCode = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			postOrderFg = genericUtility.getColumnValue("post_order_flag",headerDom);
			if(postOrderFg == null || postOrderFg.trim().length() == 0)
			{
				postOrderFg = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			itemSerFr = genericUtility.getColumnValue("item_ser__from",headerDom);
			if ( itemSerFr == null || itemSerFr.trim().length() == 0 )
			{
				itemSerFr = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			itemSerTo = genericUtility.getColumnValue("item_ser__to",headerDom);
			if ( itemSerTo == null || itemSerTo.trim().length() == 0 )
			{
				itemSerTo = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
	    	custCodeFr = genericUtility.getColumnValue("cust_code__from",headerDom);
	    	if ( custCodeFr == null || custCodeFr.trim().length() == 0 )
			{
				custCodeFr = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			System.out.println("custCodeFr"+ custCodeFr);
			custCodeTo = genericUtility.getColumnValue("cust_code__to",headerDom);
			if ( custCodeTo == null || custCodeTo.trim().length() == 0 )
			{
				custCodeTo = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
            saleOrderFr = genericUtility.getColumnValue("sale_order__from",headerDom);
            if ( saleOrderFr == null || saleOrderFr.trim().length() == 0 )
			{
				saleOrderFr = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			saleOrderTo = genericUtility.getColumnValue("sale_order__to",headerDom);
			if ( saleOrderTo == null || saleOrderTo.trim().length() == 0 )
			{
				saleOrderTo = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
         	sDateFr = genericUtility.getColumnValue("due_date__from",headerDom);
         	writeLog(filePtr,"sDateFr[Filter]:::"+sDateFr,true);
         	sDateFr = sDateFr.substring(0,8);
         	writeLog(filePtr,"sDateFr(0-8):::"+sDateFr,true);
			if (genericUtility.getApplDateFormat().indexOf("/") != -1)
			{
				sDateFr = sDateFr.replace('-', '/');
			}
			writeLog(filePtr,"sDateFr(replace / to -):::"+sDateFr,true);
         	if ( sDateFr == null || sDateFr.trim().length() == 0 )
			{
				sDateFr = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			writeLog(filePtr,"Comming Date Format(FR):::"+sDateFr,true);
			//sDateFr = genericUtility.getValidDateString(sDateFr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			sDateFr = getValidDateString(sDateFr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			writeLog(filePtr,"Validate Date Str"+sDateFr,true);
			dateFr = java.sql.Timestamp.valueOf(sDateFr + " 00:00:00");
			sDateTo = genericUtility.getColumnValue("due_date__to",headerDom);
			writeLog(filePtr,"sDateTo[FILTER]:::"+sDateTo,true);
			sDateTo = sDateTo.substring(0,8);
			writeLog(filePtr,"sDateTo[0-8]:::"+sDateTo,true);
			if (genericUtility.getApplDateFormat().indexOf("/") != -1)
			{
				sDateTo = sDateTo.replace('-', '/');
			}
			writeLog(filePtr,"Replace - to /"+sDateTo,true);
			if ( sDateTo == null || sDateTo.trim().length() == 0 )
			{
				sDateTo = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			writeLog(filePtr,"Comming Date (TO)::"+sDateTo,true);
			//sDateTo = genericUtility.getValidDateString(sDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			sDateTo = getValidDateString(sDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			writeLog(filePtr,"Validate Date Str::"+sDateTo,true);
			dateTo = java.sql.Timestamp.valueOf(sDateTo + " 00:00:00");
			writeLog(filePtr,"Date To [final]::"+dateTo,true);
			DefaultQtyFlag = genericUtility.getColumnValue("default_qty_flag",headerDom);
			if(DefaultQtyFlag == null || DefaultQtyFlag.trim().length() == 0)
			{
				DefaultQtyFlag = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			frsFlag = genericUtility.getColumnValue("frs_repl",headerDom);
			System.out.println("frs flag  value ::::::::("+frsFlag+")");
			if(frsFlag == null || frsFlag.trim().length() == 0)
			{
				frsFlag = "";
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			if(tranType.equals("NULLFOUND"))
			{
				errString="";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}
			getDataSql = "SELECT SORDER.CUST_CODE, CUSTOMER.CUST_NAME,SORDDET.LINE_NO, "
				       +"SORDER.SALE_ORDER,SORDER.DUE_DATE,SORDITEM.ITEM_CODE,"
					   +"ITEM.DESCR,SORDITEM.QUANTITY,"
					   +"SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC PENDING_QUANTITY,"
					   +"SORDITEM.QTY_ALLOC,SORDDET.PACK_INSTR,"
					   +"SORDER.SITE_CODE,SORDITEM.EXP_LEV "
					   +"FROM SORDDET,SORDER,SORDITEM,CUSTOMER,ITEM  "
					   +"WHERE ( SORDER.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
					   +"( SORDITEM.SALE_ORDER = SORDER.SALE_ORDER ) AND "
					   +"( SORDDET.LINE_NO = SORDITEM.LINE_NO ) AND "
					   +"( SORDITEM.ITEM_CODE__ORD = ITEM.ITEM_CODE ) AND "
					   +"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND  "
					   +"( SORDDET.SITE_CODE = SORDITEM.SITE_CODE ) AND  "
					   +" SORDDET.SITE_CODE = '" + siteCode.trim() +"' AND "
					   +" ITEM.ITEM_SER >= '" + itemSerFr.trim() + "' AND "
					   +" ITEM.ITEM_SER <= '" + itemSerTo.trim() + "' AND "
					   +" CUSTOMER.CUST_CODE >= '" + custCodeFr.trim() + "' AND "
					   +" CUSTOMER.CUST_CODE <= '" + custCodeTo.trim() + "' AND "
					   +" SORDER.SALE_ORDER >= '" + saleOrderFr.trim() + "' AND "
					   +" SORDER.SALE_ORDER <=  '" + saleOrderTo.trim() + "' AND "
					   +" SORDER.DUE_DATE >=  ?  AND"
					   +" SORDER.DUE_DATE <= ? "
					   +"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS END <> 'C' "
					   +"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS END = 'P' "
					   +"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
					   +"AND SORDITEM.LINE_TYPE = 'I' "
					   +"ORDER BY SORDER.CUST_CODE,SORDER.SALE_ORDER,SORDITEM.LINE_NO,SORDITEM.EXP_LEV";
					writeLog(filePtr,"      GETDATA QUERY         ",true);
					writeLog(filePtr,"_____________________________",true);
					writeLog(filePtr,getDataSql,true);
					writeLog(filePtr,"_____________________________",true);
					pstmt = conn.prepareStatement(getDataSql);
					pstmt.setTimestamp(1,dateFr);
					pstmt.setTimestamp(2,dateTo);
					st = conn.createStatement();
					rs = pstmt.executeQuery();
			if(rs.next())
			{
				writeLog(filePtr,"RECORD EXIST",true);
				do
				{
					System.out.println("Processing Item ....." + rs.getString(6) );
					if(itemCodeMap.containsKey(rs.getString(6)))
					{
						balStockQty = Double.parseDouble(itemCodeMap.get(rs.getString(6)).toString());
						bappend = false ;

					}
					else
					{
						sql ="SELECT SUM(STOCK.QUANTITY - STOCK.ALLOC_QTY ) "
		  					+"FROM STOCK,ITEM,LOCATION,INVSTAT "
		     				+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
		     				+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
		     				+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
		     				+"AND INVSTAT.AVAILABLE = 'Y' "
		     				+"AND STOCK.ITEM_CODE = '"+ rs.getString(6) + "'"
		     				+"AND STOCK.SITE_CODE = '"+ rs.getString(12) +"'"
		     				+"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY) > 0  ";
						rs1 = st.executeQuery(sql);
						writeLog(filePtr,"			STOCK QUERY         ",true);
						writeLog(filePtr,"______________________________",true);
						writeLog(filePtr,sql,true);
						writeLog(filePtr,"______________________________",true);

						if (rs1.next())
						{
							bappend = true;
							balStockQty = rs1.getDouble(1);
							if (balStockQty > 0)
							{
								itemCodeMap.put(rs.getString(6),new Double(rs1.getDouble(1)));
							}
							System.out.println("Bal stk qty for item ....." + rs.getString(6) + " is " + balStockQty);
						}
						rs1.close();
					}
					//if (balStockQty > 0)
					if(itemCodeMap.containsKey(rs.getString(6)))
					{
						//CUST_CODE
						retTabSepStrBuff.append((rs.getString(1)==null)?" " :rs.getString(1)).append("\t");

						//CUST_NAME
			          	retTabSepStrBuff.append((rs.getString(2)==null)?" " :rs.getString(2)).append("\t");
						//LINE_NO
						retTabSepStrBuff.append((rs.getString(3)==null)?" " :rs.getString(3)).append("\t");
						//SALE_ORDER
						retTabSepStrBuff.append((rs.getString(4)==null)?" " :rs.getString(4)).append("\t");
						//DUE_DATE
						retTabSepStrBuff.append(rs.getTimestamp(5)).append("\t");
						dueDate = rs.getTimestamp(5);
						//ITEM_CODE
						retTabSepStrBuff.append((rs.getString(6)==null)?" " :rs.getString(6)).append("\t");
						//DESCR
						retTabSepStrBuff.append((rs.getString(7)==null)?" " :rs.getString(7)).append("\t");
						//FRSBALANCE
						if(custCodeMap.containsKey(rs.getString(1)))
						{
							frsappend = false;
						}
						else
						{
							custCodeMap.put(rs.getString(1),new Double(calcFrsBal(siteCode,rs.getString(1),tranType,refMap,conn)));//new Double(calcFrsBal(rs.getString(1),tranType,refMap));;
							frsappend = true;
						}
						if(frsappend)
						{
							retTabSepStrBuff.append(custCodeMap.get(rs.getString(1)).toString()).append("\t");
							//System.out.println("frsbal==================="+999);
						}
						else
						{
								retTabSepStrBuff.append("0").append("\t");

						}

						//QUANTITY
						retTabSepStrBuff.append(rs.getDouble(8)).append("\t");
						//PENDING_QUANTITY
						retTabSepStrBuff.append(rs.getDouble(9)).append("\t");
						//STOCK_QUANTITY
						pendQty = rs.getDouble(9);
						//***** check itemhashmap wheather current item_code already exists
						// if not add to itemhashmap and get the stock as follows and set in
						//  tabdelimited string else nothing is to be done just consider the stock as 0
						if (bappend == true)
						{
							retTabSepStrBuff.append(balStockQty).append("\t");
						}
						else
						{
							retTabSepStrBuff.append("0").append("\t");
						}
						// alloc_qty to be set based on stock availability
						// it should not be more than pending quantity
						// the balance quantity to be updated in itemCodeMap
						// and to be used for the item's next iteration
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
							retTabSepStrBuff.append(allocQty).append("\t");
						}
						else
						{
							retTabSepStrBuff.append("0").append("\t");
						}
						// this line has to be commented later
						// as this will be a input from the user
						itemCodeMap.put(rs.getString(6), new Double(balStockQty));
						//PACK_INSTR
						retTabSepStrBuff.append((rs.getString(11)==null?" ":rs.getString(11))).append("\t");
						//SITE_CODE
						retTabSepStrBuff.append((rs.getString(12)==null)?" " :rs.getString(12)).append("\t");
						//EXP_LEV
						retTabSepStrBuff.append((rs.getString(13)==null)?" " :rs.getString(13).trim()).append("\t");
						retTabSepStrBuff.append(frsFlag).append("\n");
						//retTabSepStrBuff.append(" ").append("\n");

					}

				}while(rs.next());

				resultString = retTabSepStrBuff.toString();
				System.out.println("ResultString....." + resultString);
				pstmt.clearParameters();
			}
			else
			{
				errCode ="ERROR,"+"VTNOREC2";
			}
			if (!errCode.equals(""))
			{
				resultString = itmDBAccessEJB.getErrorString("", errCode , "", "", conn);
				System.out.println("resultString: "+resultString);
			}
			rs.close();

		}
		catch (SQLException e)
		{
			writeLog(filePtr,e,true);
			System.out.println("SQLException :StockAllocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");

			throw new ITMException(e);
		}
		catch (Exception e)
		{
			writeLog(filePtr,e,true);
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
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		writeLog(filePtr,"Return String ------------->>>"+resultString,true);
		writeLog(filePtr,"View Data End Time["+new java.sql.Timestamp(System.currentTimeMillis()).toString()+"]",true);
		return resultString;
	}//END OF GETDATA(2)


//	************************************************ P R O C E S S ************************************************
		public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
		{
			writeLog(filePtr,"**********************************************************************",true);
			writeLog(filePtr,"                  P R O C E S S I N F O R M A T I O N (10042007)                ",true);
			writeLog(filePtr,"Start Time["+new java.sql.Timestamp(System.currentTimeMillis()).toString()+"]",true);
			writeLog(filePtr,"**********************************************************************",true);
			Document detailDom = null;
			Document headerDom = null;
			String retStr = "";

			try
			{
					writeLog(filePtr,"______________________________________________________________________",true);
					writeLog(filePtr,"xmlString[process]:::"+xmlString,true);
					writeLog(filePtr,"xmlString2[process]:::"+xmlString2,true);
					writeLog(filePtr,"windowName[process]:::"+windowName,true);
					writeLog(filePtr,"xtraParams[process]:::"+xtraParams,true);
					writeLog(filePtr,"______________________________________________________________________",true);

			}
			catch(Exception e)
			{
				writeLog(filePtr,e,true);
				System.out.println(e.getMessage());
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

				System.out.println("Exception :StockAllocationPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
				e.printStackTrace();
				retStr = e.getMessage();
			}
			return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		ResultSet rs = null;
		PreparedStatement pstmt = null;

		String siteCode = this.siteCode;
		String postOrderFg = this.postOrderFg;
		String custCode = "";
		String saleOrder = null;
		String expLev = null;
		String itemCode = null;
		String lineNo =null;
		String unit = null;
		String locCode = null;
		String lotSl= null;
		String lotNo = null;
		String locDescr = null;
		String itemShDescr = null;
		String stockQuantity = null;
		String errString = "";
		String getDataSql= null;
		String insertSql = null;
		String updateSql = null;
		String frsBrowFlag =null;
		String childNodeName = "";
		String errCode = "";
		String errFrSret ="";
		boolean validEntry = true;


		double frsBal =0.0;
		double allocQty = 0;
		double quantity = 0;
		double qtyAvailAlloc = 0;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;


		int updCnt = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;

		try
		{
			retBuf = new StringBuffer();
			if(conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				connDriver = null;

			}
		}
		catch (Exception e)
		{
			writeLog(filePtr,e,true);

		}
		tranType = distCommonObj.getDisparams("999999","SRET_TYPE_FRS",conn);
		writeLog(filePtr,"Tran type[process]::["+tranType+"]",true);
		frsFlag =  genericUtility.getColumnValue("frs_repl",headerDom);

	//	**************************** comming from header
		siteCode = genericUtility.getColumnValue("site_code",headerDom);
		postOrderFg = genericUtility.getColumnValue("post_order_flag",headerDom);
		itemSerFr = genericUtility.getColumnValue("item_ser__from",headerDom);
		itemSerTo = genericUtility.getColumnValue("item_ser__to",headerDom);
		custCodeFr = genericUtility.getColumnValue("cust_code__from",headerDom);
	   	custCodeTo = genericUtility.getColumnValue("cust_code__to",headerDom);
		saleOrderFr = genericUtility.getColumnValue("sale_order__from",headerDom);
       	saleOrderTo = genericUtility.getColumnValue("sale_order__to",headerDom);
		try
		{
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			saleReturnMap.put("chg_user",chgUser);
			saleReturnMap.put("chg_term",chgTerm);
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			System.out.println("parentNodeListLength:::::::::"+parentNodeListLength);
			writeLog(filePtr,"ParentNodeListLength==========="+parentNodeListLength,true);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				stockQuantity =null;
				saleOrder = null;
				lineNo =null;
				itemCode =null;
				allocQty =0;
				expLev =null;
				custCode =null;
				frsBrowFlag =null;



				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength::: "+ childNodeListLength+"\n");
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{

					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals("stock_quantity"))
					{
						if(childNode.getFirstChild()!=null)
						{
							stockQuantity = childNode.getFirstChild().getNodeValue();
						}
					}
					if (childNodeName.equals("sale_order"))
					{
						if(childNode.getFirstChild()!=null)
						{
							saleOrder = childNode.getFirstChild().getNodeValue();
						}
					}

					if (childNodeName.equals("line_no"))
					{
						if(childNode.getFirstChild()!=null)
						{
							lineNo = childNode.getFirstChild().getNodeValue();
						}
					}
					if (childNodeName.equals("item_code"))
					{
						if(childNode.getFirstChild()!=null)
						{
							itemCode = childNode.getFirstChild().getNodeValue();
						}
					}

					if (childNodeName.equals("qty_alloc"))
					{
						if(childNode.getFirstChild()!=null)
						{
							allocQty = Double.parseDouble(childNode.getFirstChild().getNodeValue());
						}
					}
					if (childNodeName.equals("exp_lev"))
					{
						if(childNode.getFirstChild()!=null)
						{
							expLev = childNode.getFirstChild().getNodeValue();
						}
					}
					if (childNodeName.equals("cust_code"))
					{
						if(childNode.getFirstChild()!=null)
						{
							custCode = childNode.getFirstChild().getNodeValue();
						}
					}
					if (childNodeName.equals("frs"))
					{
						if(childNode.getFirstChild()!=null)
						{
							frsBrowFlag = childNode.getFirstChild().getNodeValue();
						}
					}

				}//inner for loop

				CustomerBean customerBean= new CustomerBean();

				customerBean.setStockQuantity(stockQuantity);
				customerBean.setSaleOrder(saleOrder);
				customerBean.setLineNo(lineNo);
				customerBean.setItemCode(itemCode);
				customerBean.setItemSer(conn);
				customerBean. setExpLev(expLev);
				customerBean. setFrsBrowFlag(frsBrowFlag);
				customerBean.setAllocQty(allocQty);
				customerBean.setTaxEnv(siteCode,tranType,conn);
				customerBean.setTaxClass(siteCode,tranType,conn);
				CustomerListBean customerListObj = null;

				int ele = getCustomerInformation(customerList, custCode);
				writeLog(filePtr,"Existing Customer:-["+ele+","+custCode+"]",true);//Existing if ele != -1
				if(ele==-1)
				{
					customerListObj = new CustomerListBean();
					customerListObj.setCustCode(custCode);
					customerListObj.setSiteCode(siteCode);
					customerList.add(customerListObj);
					ele = customerList.size() -1 ;

				}
				else
				{
					customerListObj = (CustomerListBean)customerList.get(ele);
				}
				customerListObj.setCustRecordList(customerBean);
				System.out.println("The size of the element is ["+ele+"]");
				customerList.set(ele,customerListObj);
			}// OUT FOR LOOP


		for(int customerNo=0;customerNo<customerList.size();customerNo++)
		{
			priceNotFound = false;

			CustomerListBean customerObject= (CustomerListBean)customerList.get(customerNo);
			writeLog(filePtr,"************************************************ ",true);
			writeLog(filePtr,"Customer Code:-["+customerObject.getCustCode()+"]",true);
			writeLog(filePtr,"Site Code:-["+customerObject.getSiteCode()+"]",true);
			writeLog(filePtr,"************************************************ ",true);
			ArrayList custInfoRecordList =(ArrayList)customerObject.getCustRecordList();
			for(int record=0;record<custInfoRecordList.size();record++)
			{
				writeLog(filePtr,"__________________________________________________________",true);
				CustomerBean custReco = (CustomerBean)custInfoRecordList.get(record);
				writeLog(filePtr,"Sale Order Number:-["+custReco.getSaleOrder()+"]",true);
				writeLog(filePtr,"Line Number:-["+custReco.getLineNo()+"]",true);
				writeLog(filePtr,"Item Code:-["+custReco.getItemCode()+"]",true);
				writeLog(filePtr,"Item Series:-["+custReco.getItemSer()+"]",true);
				writeLog(filePtr,"Explosion Level:-["+custReco.getExpLev()+"]",true);
				writeLog(filePtr,"Allocation Quantity:-["+custReco.getAllocQty()+"]",true);
				writeLog(filePtr,"Tax Enviroment:-["+custReco.getTaxEnv()+"]",true);
				writeLog(filePtr,"Tax Class:-["+custReco.getTaxClass()+"]",true);
				writeLog(filePtr,"Frs Allocation:-["+custReco.getFrsBrowFlag()+"]",true);
				if(!(custReco.getFrsBrowFlag().trim().equalsIgnoreCase("Y") || custReco.getFrsBrowFlag().trim().equalsIgnoreCase("N")) )
				{
					validEntry=false;
				}

				writeLog(filePtr,"___________________________________________________________",true);
			}
		}
		System.out.println("validEntry====================["+validEntry+"]");
			tranFlag ="P";

			if(validEntry == true)
			{
				errString =  freDbtGen(customerList,conn);


			}
			else
			{
				errString="ERROR,"+"INVALIDENTR";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
			}
			writeLog(filePtr,"ERROR STRING [[["+errString+"]",true);
		}//try end
		catch(Exception e)
		{
			errString = "EXCEPTION";
			e.printStackTrace();
			return errString ;
		}
		finally
		{
			System.out.println("Closing Connection....");
			try
			{

				retBuf = null;
				writeLog(filePtr,"Returning Error String:::::::::::::::::::::::::"+errString,true);
				if((!errString.equals("EXCEPTION")) && (errString.indexOf("ERROR")==-1))
				{
						conn.commit();
						writeLog(filePtr,"Transaction comitted [[[conn.commit()]",true);
						System.out.println("Transaction Commit........................");
				}
				else
				{
						conn.rollback();
						writeLog(filePtr,"Transaction Rollback[[[conn.rollBack()]",true);
						System.out.println("Transaction Rollback........................");
				}
				saleOrderArr.clear();
				customerList.clear();
				if(conn != null)
				{
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

		}
		writeLog(filePtr,"Returning Error String---------------->["+errString+"]",true);
		writeLog(filePtr,"[Process End Time["+new java.sql.Timestamp(System.currentTimeMillis()).toString()+"]",true);

		return errString;
	}//END OF PROCESS(2)
//private String freDbtGen(ArrayList browArrList,double frsBal,java.util.HashMap refNoMap,Connection conn)
private String freDbtGen(ArrayList customerList,Connection conn)
{


	String errString = "";
	String custCode =null;
	String siteCode =null;
	//String tranDate  =  getCurrdateAppFormat();
	ArrayList custInfoRecordList = null;
	String frsBalFlg = "Y";
	String lineNo = null;
	String saleOrder = null;
	String itemCode=null;
	String itemSer = null;
	String expLev=null;
	String tranId = null;
	String taxClass = null;
	String taxChap ="    ";
	String taxEnv = null;
	String priceList =null;
	String priceListClg=null;
	double qtyAlloc=0;
	double frsBal =0;
	int customerNo =0;
	int record=0;
	String frsBrowFlag = null;
	char sepChar[] = new char[1];

		SretInvBean sretInvBeanObj= null;

	for(customerNo=0;customerNo<customerList.size();customerNo++)
	{
		custFlag=true;
		totalAmtAdjusted =0;
		bFlag =0;
		refNoMap=null;
		refNoMap = new HashMap();
		sretInvList = new ArrayList();
		//added on 10042007
		referenceVector = new Vector();

		//tranType print
		CustomerListBean customerObject= (CustomerListBean)customerList.get(customerNo);
		custCode = customerObject.getCustCode();
		siteCode = customerObject.getSiteCode();
		frsBal = calcFrsBal(siteCode, custCode,tranType,refNoMap,conn);
		writeLog(filePtr,"Reference Number Map in which Amount found  :-"+referenceVector,true);
		writeLog(filePtr,"[*****]Frs Balance For Customer["+custCode+"]  is ["+frsBal+"]",true);
		frsBalInit = frsBal;
		//20062007
		if(frsBalInit < 0)
		{
			errString="ERROR,"+"NEGFRS";
			try{errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);}catch(Exception e){}
			writeLog(filePtr,"-VE FRS F0R CUSTOMER ["+custCode+"]",true);
			return errString;

		}
		try
		{
			priceList = distCommonObj.setPlistTaxClassEnv(siteCode,siteCode,"",tranType,"","PRICE_LIST",conn);
			priceListClg = distCommonObj.setPlistTaxClassEnv(siteCode,siteCode,"",tranType,"","PRICE_LIST__CLG",conn);
			if(priceList==null)priceList=" ";
			if(priceListClg==null)priceListClg=" ";
			saleReturnMap.put("price_list",priceList);
			saleReturnMap.put("price_list__clg",priceList);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//tranId = generateTranId("W_SALESRETURN",tranDate,siteCode,conn);
		//saleReturnMap.put("tran_id",tranId);
		//saleReturnDetMap.put("tran_id",tranId);
		saleReturnDetMap.put("line_no",new Integer(0).toString());
		saleReturnMap.put("cust_code",custCode);
		saleReturnMap.put("site_code",siteCode);
		saleReturnMap.put("tran_type",tranType);
		saleReturnMap.put("item_ser","  ");
		cust(custCode,conn);
		//insertSReturn(saleReturnMap,conn);
		custInfoRecordList =(ArrayList)customerObject.getCustRecordList();
		writeLog(filePtr,"[*****]Total Record Selected ["+custInfoRecordList.size()+"]",true);
		for(record=0;record<custInfoRecordList.size();record++)
		{
			CustomerBean custReco = (CustomerBean)custInfoRecordList.get(record);
			lineNo = custReco.getLineNo();
			saleOrder = custReco.getSaleOrder();
			itemCode=custReco.getItemCode();;
			expLev=custReco.getExpLev();
		   	qtyAlloc =custReco.getAllocQty();
		   	taxClass = custReco.getTaxClass();
		   	taxEnv = custReco.getTaxEnv();
		   	itemSer =custReco.getItemSer();
		   	frsBrowFlag = custReco.getFrsBrowFlag();
			//taxChap = browObj.get("tax_chap").toString();
			saleReturnDetMap.put("tax_chap",taxChap);
			saleReturnDetMap.put("tax_class",taxClass);
			saleReturnDetMap.put("tax_env",taxEnv);
			saleReturnDetMap.put("item_ser",itemSer);
			saleReturnDetMap.put("item_code",itemCode);
			if(qtyAlloc!=0)
			{
				errString =	scanStock(saleOrder,lineNo,itemCode,siteCode,qtyAlloc,expLev,frsBrowFlag,conn);
			}
		}
		if(customerNo!=0)
		{
			retBuf.append("@");
		}
		updateReceivables(refNoMap,custCode,conn);
		//***********added on 100407
		tranId = saleReturnMap.get("tran_id").toString();
		updateSreturn(tranId,conn);
		if( bFlag==0)
		{
			retBuf.append(",");
		}
		else
		{
			retBuf.append(sreturnID);// 19-07-2006 manoharan
			//********************************************************************
			writeLog(filePtr,"Total AmountAdjusted:::-*****************["+totalAmtAdjusted+"]*******************",true);
			writeLog(filePtr,"___________________________________________________",true);
			//***********************
				writeLog(filePtr,"Before changes  The S Return Invoice bean",true);
				writeLog(filePtr,"sretInvList.size()["+sretInvList.size()+"]",true);
				for(int i=0;i<sretInvList.size();i++)
				{
					sretInvBeanObj= (SretInvBean)sretInvList.get(i);
					writeLog(filePtr,"RefSerise["+sretInvBeanObj.getRefSer()+"]",true);
					writeLog(filePtr,"RefNo["+sretInvBeanObj.getRefNo()+"]",true);
					writeLog(filePtr,"AdjAmt["+sretInvBeanObj.getAdjAmt()+"]",true);
					writeLog(filePtr,"RefBalAmt["+sretInvBeanObj.getRefBalAmt()+"]",true);
					writeLog(filePtr,"___________",true);
				}
				for(int i=0;i<sretInvList.size();i++)
				{
						sretInvBeanObj= (SretInvBean)sretInvList.get(i);
						double amt = sretInvBeanObj.getRefBalAmt();
						writeLog(filePtr,"Amount ["+amt+"]",true);
						if(totalAmtAdjusted!=0)
						{
							if(totalAmtAdjusted >= Math.abs(amt))
							{
								writeLog(filePtr,"totalAmtAdjusted >= Math.abs(amt)",true);
								totalAmtAdjusted  = totalAmtAdjusted - Math.abs(amt);
								if(amt<0)
								{
									sretInvBeanObj.setAdjAmt(amt);
								}
							}
							else
							{
								writeLog(filePtr,"totalAmtAdjusted < Math.abs(amt)",true);
								if(amt<0)
								{
									amt=(-1)*totalAmtAdjusted;
									sretInvBeanObj.setAdjAmt(amt);
								}
								else
								{
									amt =totalAmtAdjusted;
									sretInvBeanObj.setAdjAmt(amt);

								}
								totalAmtAdjusted=0;

							}
							sretInvList.set(i,sretInvBeanObj);
						}

					}
					writeLog(filePtr,"After  changes  The S Return Invoice bean",true);
					writeLog(filePtr,"sretInvList.size()["+sretInvList.size()+"]",true);
					for(int i=0;i<sretInvList.size();i++)
					{
					  sretInvBeanObj= (SretInvBean)sretInvList.get(i);
						writeLog(filePtr,"RefSerise["+sretInvBeanObj.getRefSer()+"]",true);
						writeLog(filePtr,"RefNo["+sretInvBeanObj.getRefNo()+"]",true);
						writeLog(filePtr,"AdjAmt["+sretInvBeanObj.getAdjAmt()+"]",true);
						writeLog(filePtr,"RefBalAmt["+sretInvBeanObj.getRefBalAmt()+"]",true);
						writeLog(filePtr,"___________",true);
					}



			//*************************************************
			//***********************************************************************
			String errFrSret  = insertSRetInv(sretInvList,sreturnID,conn);
			errString = errFrSret;
			writeLog(filePtr,"Returning From insertSRetInv["+errFrSret+"]",true);
			writeLog(filePtr,"Returning From insertSRetInv["+errString+"]",true);
		}
		writeLog(filePtr,"Appending the sreturnTranId....."+sreturnID,true);
		writeLog(filePtr,"saleOrder list contain ["+saleOrderArr.size()+"] saleorder",true);

		for(int counter =0;counter<saleOrderArr.size();counter++)
		{
			String sorder = (String)saleOrderArr.get(counter);
			if(counter == saleOrderArr.size()-1)
			{
				retBuf.append(sorder);
			}
			else
			{
				retBuf.append(sorder+",");
			}

		}
		if(!errString.equals("EXCEPTION")  )
		{

			if(allocateFlag == false)
			{
					errString = "";
				//conn.rollback();
					writeLog(filePtr,"Transaction Rollback[[[conn.rollback()]",true);
					errString="ERROR,"+"FRSMB";
					try{errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);}catch(Exception e){}
				//	conn.rollback();
					writeLog(filePtr,"TOTAL AMOUNT SELECTED FROM CUSTOMER ["+custCode+"] LESS THAN FRS BAL",true);
					return errString;
			}
			else
			{
				if( priceNotFound == true)
				{
					writeLog(filePtr,"Transaction Rollback[[[conn.rollBack()]",true);
					errString="ERROR,"+"PLNF";
					try	{errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);}catch(Exception e){}
					writeLog(filePtr,"Transaction Rollback[[["+errString+"]",true);
					writeLog(filePtr,"NO PRICE FORND FOR THE CUSTOMER["+custCode+"]",true);
					return errString;
				}
				else
				{
					writeLog(filePtr,"Adding in errString-----",true);
					errString = retBuf.toString();
				}
			}
		}

		System.out.println("errString:::::::::"+errString);
		saleOrderArr.clear();
		refNoMap = null;
		sretInvList = null;
		referenceVector = null;
	}//endof customer

		writeLog(filePtr,"Final  errString-----",true);

	return errString ;
}
/*private double updateRecMap(double mrp,java.util.HashMap refNoMap)
{

		java.util.Iterator refMapIterator = null;
		java.util.Set refMapSet = null;
		Map.Entry me =null;
		refMapSet = refNoMap.entrySet();
		refMapIterator = refMapSet.iterator();
		try{writeLog(filePtr,"B E F O R E A D J U S T  A M O U N T ["+mrp+"]",true);}catch(Exception d){}
		while(refMapIterator.hasNext())
		{
				me=(Map.Entry)refMapIterator.next();
				System.out.println("Ref No ::: ["+me.getKey()+"]    MrpValue:::["+me.getValue()+"]");
				try{writeLog(filePtr,"Ref No ["+me.getKey()+"] MrpValue["+me.getValue()+"]",true);}catch(Exception d1){}

				double mrpValueFrReceivable= ((Double)refNoMap.get(me.getKey())).doubleValue();
		    	if(mrp > mrpValueFrReceivable)
				{
					refNoMap.put( me.getKey(),new Double(0));
					mrp = mrp - mrpValueFrReceivable;
				}
				else
				{
					refNoMap.put(me.getKey(),new Double(mrpValueFrReceivable-mrp));
					mrp = 0;
					writeLog(filePtr,"Breaking (mrp=0)------------->",true);
					break;
				}
		}
		try{writeLog(filePtr,"A F T E R A D J U S T  A M O U N T :::["+mrp+"]",true);}catch(Exception eee){}
		refMapSet = refNoMap.entrySet();
		refMapIterator = refMapSet.iterator();
		while(refMapIterator.hasNext())
		{
			me=(Map.Entry)refMapIterator.next();
			try{writeLog(filePtr,"Ref No ["+me.getKey()+"]MrpValue:::["+me.getValue()+"]",true);}catch(Exception d2){};
		}
	return mrp;
	}*/
//****added on 10042007
private double updateRecMap(double mrp,java.util.HashMap refNoMap)
{
		java.util.Iterator refMapIterator = null;
		java.util.Set refMapSet = null;
		Map.Entry me =null;
		refMapSet = refNoMap.entrySet();
		refMapIterator = refMapSet.iterator();

	try
	{
		writeLog(filePtr,"B E F O R E A D J U S T  A M O U N T ["+mrp+"]",true);
		for(int i=0;i<referenceVector.size();i++)
		{

			double mrpValueFrReceivable= ((Double)refNoMap.get(referenceVector.get(i))).doubleValue();
			//added on 22052007
			if(mrpValueFrReceivable == 0) continue;
			//end
			try{writeLog(filePtr,"Ref No :-"+referenceVector.get(i)+" MrpValue :-"+mrpValueFrReceivable,true);}catch(Exception d1){}

			if(mrp > mrpValueFrReceivable)
			{
				refNoMap.put( referenceVector.get(i),new Double(0));
				mrp = mrp - mrpValueFrReceivable;
			}
			else
			{
				refNoMap.put(referenceVector.get(i),new Double(mrpValueFrReceivable-mrp));
				mrp = 0;
				writeLog(filePtr,"Breaking (mrp=0)------------->",true);
				break;
			}
		}
		writeLog(filePtr,"A F T E R A D J U S T  A M O U N T :::["+mrp+"]",true);
		refMapSet = refNoMap.entrySet();
		refMapIterator = refMapSet.iterator();
		while(refMapIterator.hasNext())
		{
			me=(Map.Entry)refMapIterator.next();
			writeLog(filePtr,"Ref No :-"+me.getKey()+" MrpValue::::-"+me.getValue(),true);
		}
	}
	catch(Exception d)
	{
		try{writeLog(filePtr,d,true);}catch(Exception e){}
	}
	return mrp;
}
//DEBIT NOTE GENERATION
private void 	updateSordItem(String saleOrder,String lineNo,double lotQtyToBeAllocated,String expLev)
{
		System.out.println("Updating SordItem.SaleOrder("+saleOrder+")LineNo("+lineNo+")Qty ("+lotQtyToBeAllocated);
		PreparedStatement  pstmt = null;
		ResultSet rsSodItem =null;
		String updateSorditem ="UPDATE SORDITEM  SET QTY_DESP = CASE WHEN QTY_DESP IS NULL THEN 0 ELSE QTY_DESP END  +   " + new Double(lotQtyToBeAllocated).toString()
								   +" WHERE SALE_ORDER = '" + saleOrder + "' "
								   +" AND LINE_NO = '" + lineNo + "' "
								   +" AND EXP_LEV = '" + expLev.trim() + "' ";
		try
		{
			pstmt = conn.prepareStatement(updateSorditem);
 			int u =pstmt.executeUpdate();
			System.out.println("No Of record update..........."+u);
		}
		catch(SQLException se1)
		{
			System.out.println("SQLException updateSordItem :" + se1);
			se1.printStackTrace();
		}
		catch(Exception e1)
		{
			System.out.println("Exception updateSordItem :" + e1);
			e1.printStackTrace();
		}
}
private double calcFrsBal(String siteCode, String custCode,String tranType,java.util.HashMap refMap,Connection conn)
{

		writeLog(filePtr,"Calculating Frs Balance In Progress For Customer["+custCode +"] \t"+tranType+"\t"+"["+tranFlag+"]",true);
		System.out.println("Calculating Frs Balance In Progress For Customer["+custCode +"] \t"+tranType);

		boolean expired = false;
		double frsBal =0.0;
		java.sql.Timestamp currDate = null;
		java.sql.Timestamp expDate = null;
		PreparedStatement  pstmt = null;
		PreparedStatement  pstmtSretDet = null;
		ResultSet rs =null;
		ResultSet rsSretDet =null;
		double refBal = 0.0;
		double refBalAmt =0.0;
		double expRate =0.0;
		String reNo="";
		SimpleDateFormat sdf=null;
		SretInvBean sretInvBeanObj= null;
		String refSer = "";
		double amount =0.0;
		double adjAmt =0.0;
		String sRet="";
		PreparedStatement sreturnPstmt=null;
		ResultSet sreturnRS=null;
		String sreturn=null;
		String tranIdFrSret ="";
		String dateStr=null;
		Calendar cal=null;
		int date=0;
		int month;
		int year=0;
		double amtAdjusted=0;
		double carryFrdAmt =0;
		double totalAmtAdjusted = 0;



		sql =  "SELECT REF_NO,RECEIVABLES.MRP_VALUE ,TOT_AMT - ADJ_AMT,TRAN_SER FROM RECEIVABLES "
					+"WHERE TRAN_SER in ('CRNRCP','MDRCRC') "
					+"AND TOT_AMT - ADJ_AMT <> 0 "
					+" AND MRP_VALUE<>0"
					+"AND REF_TYPE = '"+tranType.trim()+"' "
					+"AND CUST_CODE = '"+custCode.trim()+"' ";
		try
		{
			writeLog(filePtr,"Calculate FrsBal Query[Receivables] :::["+tranFlag+"]"+sql,true);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				writeLog(filePtr,"              R E C E I V A B L E S          ",true);
				writeLog(filePtr,"_____________________________________________",true);
				writeLog(filePtr,"Data Found[Transaction Flag]----->>>["+tranFlag+"]",true);

				expRate=0.0;
				expired = false;
				amount = rs.getDouble(2);
				reNo = rs.getString(1);
				frsBal = frsBal + amount;
				refBal = amount;
				refBalAmt = rs.getDouble(3);
				refSer = rs.getString(4);
				writeLog(filePtr,"Reference Number From Receivables :::["+reNo+"]",true);

				sreturn = "SELECT TRAN_ID FROM SRETURN WHERE TRAN_ID__CRN = '"+reNo+"'";
				writeLog(filePtr,"SaleReturn Query:::["+sreturn+"]",true);
				sreturnPstmt = conn.prepareStatement(sreturn);
				sreturnRS = sreturnPstmt.executeQuery();
				if(sreturnRS.next())
				{
					tranIdFrSret = (sreturnRS.getString(1)==null?"":sreturnRS.getString(1));
					writeLog(filePtr,"Tran id from SaleReturn:::["+tranIdFrSret+"]",true);
				}
				sreturnRS.close();
				sreturnPstmt.close();

				//************************************************the 6 month reduce from return date of sreturn not current date
					java.sql.Timestamp retDate =null;
					sreturn = "SELECT TRAN_DATE FROM SRETURN WHERE TRAN_ID = '"+tranIdFrSret+"'";
					writeLog(filePtr,"SaleReturn Query[Return Date]:::["+sreturn+"]",true);
					sreturnPstmt = conn.prepareStatement(sreturn);
					sreturnRS = sreturnPstmt.executeQuery();
					if(sreturnRS.next())
					{

						retDate = sreturnRS.getTimestamp(1);

					}
					writeLog(filePtr,"Return Date:::["+retDate+"]",true);
					sreturnRS.close();
					try
					{
						if(retDate!=null)
						{
							cal= Calendar.getInstance();
							cal.setTime(retDate);
							writeLog(filePtr,"Return Date:::["+retDate+"]",true);

						}
						else
						{
							cal= Calendar.getInstance();
							writeLog(filePtr,"Current Date:::["+retDate+"]",true);
						}


						cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)-6,cal.get(Calendar.DATE));
						date=cal.get(Calendar.DATE);
						month=cal.get(Calendar.MONTH);
						year=cal.get(Calendar.YEAR);
						dateStr= new Integer(year).toString()+"-"+new Integer(month+1).toString()+"-"+new Integer(date).toString();
						writeLog(filePtr,"Date String :::["+dateStr+"]",true);
						currDate = java.sql.Timestamp.valueOf(dateStr+" 00:00:00.0");
						writeLog(filePtr,"Return Date After substract  6 month :::["+currDate+"]",true);
					}
					catch(Exception e){writeLog(filePtr,e,true);}
				//*************************************************

					sRet = "SELECT EXP_DATE, QUANTITY * RATE__CLG FROM SRETURNDET WHERE TRAN_ID = '"+tranIdFrSret+"'";
					writeLog(filePtr,"SaleReturn Query:::["+sRet+"]",true);

					if(tranFlag.equalsIgnoreCase("P"))
					{
						sretInvBeanObj = new SretInvBean();
						referenceVector.add(reNo);
						writeLog(filePtr,"Reference No:::["+reNo+"]",true);
						writeLog(filePtr,"Reference Balance Amount[tot-adj/(refBalAmt)]:::["+refBalAmt+"]",true);
						writeLog(filePtr,"Reference series:::["+refSer+"]",true);
						writeLog(filePtr,"MrpValue(Receivables)[amount]:::["+amount+"]",true);
						writeLog(filePtr,"SaleReturn Detail Calculate Expired Rate:::["+sRet+"]",true);
					}
					//added on 10042007


					refMap.put(reNo,new Double(refBal));

					System.out.println(sRet);
					pstmtSretDet = conn.prepareStatement(sRet);
					rsSretDet = pstmtSretDet.executeQuery();
					while(rsSretDet.next())
					{
					//add latter for 6 month substract from return date
						expired = false;

						writeLog(filePtr,"              R E C E I V A B L E S          ",true);
						writeLog(filePtr,"_____________________________________________",true);
						expDate = rsSretDet.getTimestamp(1);
						writeLog(filePtr,"ExpiredDate:::["+expDate+"]",true);
						writeLog(filePtr,"Currentdate (6):::["+currDate+"]",true);
						if(expDate.compareTo(currDate)<=0)
						{
							expired = true;

						}
						writeLog(filePtr,"Expired :::["+expired+"]",true);
						if(expired)
						{
							writeLog(filePtr,"ExpiredRate:::["+rsSretDet.getDouble(2)+"]",true);
							if(tranFlag.equalsIgnoreCase("P"))
							{
								expRate =expRate+rsSretDet.getDouble(2) ;
							}
							frsBal = frsBal - rsSretDet.getDouble(2);
							refBal = refBal - rsSretDet.getDouble(2);
							// 	********** reduce then add in refMap
							writeLog(filePtr,"Reduce In FRS Balance Because Of Expiri.....",true);
							refMap.put(reNo,new Double(refBal));
						}
						writeLog(filePtr,"_____________________________________________",true);
					}
					if(tranFlag.equalsIgnoreCase("P"))
		 			{


							if(refBalAmt!=0)
							{

									adjAmt=0;
									sretInvBeanObj.setRefSer(refSer);
									sretInvBeanObj.setRefNo(reNo);
									sretInvBeanObj.setAdjAmt(adjAmt);
									sretInvBeanObj.setRefBalAmt(refBalAmt);
									sretInvList.add(sretInvBeanObj);

							}//end if
						}


			}
			rs.close();

			//***********************
			writeLog(filePtr,"Returning Frs Balance From Function  calfrsbal() ["+frsBal+"]",true);
		}
		catch(SQLException se)
		{
			writeLog(filePtr,se,true);
			System.out.println("SQLException :" + se);
			se.printStackTrace();
		}
		catch(Exception e)
		{
			writeLog(filePtr,e,true);
			System.out.println("Exception :" + e);
			e.printStackTrace();

		}
		return frsBal;
}//END OF FRS BALANCE
//UPDATE RECEIVABLES
private void updateReceivables(java.util.HashMap refNoMap,String custCode,Connection conn)
{
	System.out.println("Updating the receivables-------------->>>");
	java.util.Iterator refMapIterator = null;
	java.util.Set refMapSet = null;
	refMapSet =refNoMap.entrySet();;
	refMapIterator = refMapSet.iterator();
	PreparedStatement recPstmt = null;
	Map.Entry me =null;
	int updateRow=0;
	int u =0;
	try
	{
		String recSql ="UPDATE RECEIVABLES SET MRP_VALUE = ? WHERE REF_NO= ? AND CUST_CODE = ?" ;
		recPstmt = conn.prepareStatement(recSql);
		while(refMapIterator.hasNext())
		{

			me=(Map.Entry)refMapIterator.next();
			recPstmt.setDouble(1,((Double)refNoMap.get(me.getKey())).doubleValue());
			recPstmt.setString(2,me.getKey().toString());
			recPstmt.setString(3,custCode);
			u  = recPstmt.executeUpdate();
			updateRow = updateRow + u;
		}
		writeLog(filePtr,"No of row Update = ["+updateRow+"]",true);
		recPstmt.close();
	}
	catch(Exception e)
	{
		System.out.println("Exception in updateReceivables method"+e.getMessage());
		writeLog(filePtr,e,true);
	}
}//END OF UPDATE RECEIVABLES
private void updateSreturn(String tranId,Connection conn)
{
		double netAmountTot = 0.0;
		double taxAmountTot = 0.0;
		PreparedStatement  pstmt = null;
		PreparedStatement  pstmtSretDet = null;
		ResultSet rs =null;
		String sql1 = null;
		int u =0;
		String sql  = " SELECT SUM(NET_AMT),SUM(TAX_AMT) FROM SRETURNDET WHERE TRAN_ID='"+tranId+"' ";
		writeLog(filePtr,"      U P D A T E S R E T U R N (TAX AMT)/(NET AMT) ",true);
		writeLog(filePtr,"____________________________________________________",true);
		writeLog(filePtr,sql,true);
		try
		{
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				netAmountTot = rs.getDouble(1);
				taxAmountTot = rs.getDouble(2);
			}
			rs.close();
			writeLog(filePtr,"netAmountTot ["+netAmountTot+"]",true);
			writeLog(filePtr,"taxAmountTot ["+taxAmountTot+"]",true);
		}
		catch(SQLException se)
		{
			System.out.println("SQLException updateSreturn1 :" + se);
			se.printStackTrace();
			writeLog(filePtr,se,true);
		}
		catch(Exception e)
		{
			System.out.println("Exception updateSreturn1 :" + e);
			e.printStackTrace();
	     	writeLog(filePtr,e,true);
		}
		try
		{
			sql1  = " UPDATE SRETURN SET NET_AMT = ? , TAX_AMT = ? WHERE TRAN_ID='"+tranId+"' ";
			writeLog(filePtr,sql1,true);
			pstmt = conn.prepareStatement(sql1);
			pstmt.setDouble(1,netAmountTot);
			pstmt.setDouble(2,taxAmountTot);
			u =pstmt.executeUpdate();
			writeLog(filePtr,"No of record update["+u+"]",true);
			writeLog(filePtr,"____________________________________________________",true);
		}
		catch(SQLException se1)
		{
			System.out.println("SQLException updateSreturn :" + se1);
			se1.printStackTrace();
			writeLog(filePtr,se1,true);
		}
		catch(Exception e1)
		{
			System.out.println("Exception updateSreturn :" + e1);
			writeLog(filePtr,e1,true);
			e1.printStackTrace();
		}
}
private void insertSReturn(HashMap saleReturnMap,Connection conn)
{
	String insertSt  = "";
	PreparedStatement pstmt = null;
	int u =0;
	String costCode ="";
	String siteCode ="";
	String tranId = "";;
	String priceList = "";
	String tranCode ="";
	String transMode = "";
	String currCode ="";
	String currCodeBc = null;
	String custCodeBill = "";
	String exchRateStr= " ";
	double exchrate = 0.0;
	String priceListClg ="";
	String tranType ="";
	String itemSer ="";
	String chgUser="";
	String chgTerm ="";
	try
	{
		writeLog(filePtr,"INSERTING INTO SRETURN THE FOLLOWING VALUES  ",true);
		writeLog(filePtr,"______________________________________________",true);
		//*****added latter for curr_code on date 15092006 by taranisen
		priceList = saleReturnMap.get("price_list").toString();
		if(priceList!=null)
		{
			writeLog(filePtr,"priceList:::["+priceList+"]",true);
		}

		 priceListClg = saleReturnMap.get("price_list__clg").toString();
		if(priceListClg!=null)
		{
			writeLog(filePtr,"priceListClg:::["+priceListClg+"]",true);
		}
		costCode = saleReturnMap.get("cust_code").toString();
		if(costCode!=null)
		{
			writeLog(filePtr,"costCode:::["+costCode+"]",true);
		}
		siteCode= saleReturnMap.get("site_code").toString();
		if(siteCode!=null)
		{
			writeLog(filePtr,"siteCode:::["+siteCode+"]",true);
		}

		tranId = saleReturnMap.get("tran_id").toString();
		if(tranId!=null)
		{
			writeLog(filePtr,"tranId:::["+tranId+"]",true);
		}
		//priceList = saleReturnMap.get("price_list").toString();
		tranCode = saleReturnMap.get("tran_code").toString();
		if(tranCode!=null)
		{
			writeLog(filePtr,"tranCode:::["+tranCode+"]",true);
		}
		transMode =  saleReturnMap.get("tran_mode").toString();
		if(transMode!=null)
		{
			writeLog(filePtr,"transMode:::["+transMode+"]",true);
		}
		currCode = saleReturnMap.get("curr_code").toString();
		if(currCode!=null)
		{
			writeLog(filePtr,"currCode:::["+currCode+"]",true);
		}

		custCodeBill = saleReturnMap.get("cust_code__bill").toString();
		if(custCodeBill!=null)
		{
			writeLog(filePtr,"custCodeBill:::["+custCodeBill+"]",true);
		}
		//String custCodeDlv = saleReturnMap.get("cust_code__bill").toString();
		tranType = saleReturnMap.get("tran_type").toString();
		if(tranType!=null)
		{
			writeLog(filePtr,"tranType:::["+tranType+"]",true);
		}
		itemSer =  saleReturnMap.get("item_ser").toString();
		if(itemSer!=null)
		{
			writeLog(filePtr,"itemSer:::["+itemSer+"]",true);
		}
		 chgUser =  saleReturnMap.get("chg_user").toString();
		if(chgUser!=null)
		{
			writeLog(filePtr,"chgUser:::["+chgUser+"]",true);
		}
		chgTerm =  saleReturnMap.get("chg_term").toString();
		if(chgUser!=null)
		{
			writeLog(filePtr,"chgTerm:::["+chgTerm+"]",true);
		}
		//*****added latter fou curr_code on date 15092006 by taranisen
		exchRateStr= saleReturnMap.get("exch_rate").toString();
		if(exchRateStr!=null)
		{
			writeLog(filePtr,"exchRateStr:::["+exchRateStr+"]",true);
		}
		exchrate = Double.parseDouble(exchRateStr);
		Object date = null;
		java.sql.Timestamp chgDate = new java.sql.Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
		date = sdf.parse(chgDate.toString());
		chgDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
		insertSt  = "INSERT INTO SRETURN (TRAN_ID,TRAN_DATE,TRAN_TYPE,SITE_CODE , CUST_CODE,ITEM_SER, CURR_CODE ,TRAN_CODE "
						+" , TRANS_MODE, CUST_CODE__BILL,EFF_DATE, RET_OPT,CHG_DATE,CHG_USER,CHG_TERM,PRICE_LIST,PRICE_LIST__CLG, STATUS,CONFIRMED,EXCH_RATE, CURR_CODE__BC, CUST_CODE__DLV,TAX_DATE,ADJ_MISC_CRN   )VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'O','N',?,?,?,?,'MC')";
		writeLog(filePtr,"insertSt  ["+insertSt+"]",true);
		pstmt = conn.prepareStatement(insertSt);
		pstmt.setString(1,tranId);
		pstmt.setTimestamp(2,chgDate);//new Timestamp(System.currentTimeMillis()));
		pstmt.setString(3,"RS"); // 20-07-2006 manoharan
		pstmt.setString(4,siteCode.trim());
		pstmt.setString(5,costCode.trim());
		pstmt.setString(6,itemSer.trim());
		pstmt.setString(7,currCode.trim());
		pstmt.setString(8,tranCode.trim());
		pstmt.setString(9,transMode.trim());
		pstmt.setString(10,custCodeBill.trim());
		pstmt.setTimestamp(11,chgDate);//new Timestamp(System.currentTimeMillis()));
		pstmt.setString(12,"D");
		pstmt.setTimestamp(13,new Timestamp(System.currentTimeMillis())) ;
		pstmt.setString(14,chgUser);
		pstmt.setString(15,chgTerm);
		pstmt.setString(16,priceList);
		pstmt.setString(17,priceListClg);
		//*****added latter fou curr_code on date 15092006 by taranisen
		pstmt.setDouble(18,exchrate);
//		pstmt.setString(10,custCodeDlv);
		pstmt.setString(19,currCode.trim());
		pstmt.setString(20,costCode.trim());
		pstmt.setTimestamp(21,chgDate);
		u = pstmt.executeUpdate();
		writeLog(filePtr,"No Of Record In Sale Return In Sale Return ["+u+"]",true);
		//retBuf.append(tranId +",");
		sreturnID = tranId  + ","; // 19-07-2006 manoharan
		pstmt.close();
		writeLog(filePtr,"______________________________________________",true);
	}
	catch(SQLException insertSr)
	{

		try{conn.rollback();writeLog(filePtr,"TRANSATION ROLL BACK IN insertSReturn",true);	}catch(Exception ee2){}
		System.out.println("SQLEXCEPTION IN INSERTING SRETURN:" + insertSt);
		writeLog(filePtr,insertSr,true);
		insertSr.printStackTrace();
	}
	catch(Exception insertSrEx)
	{

		try{conn.rollback();writeLog(filePtr,"TRANSATION ROLL BACK IN insertSReturn",true);	}catch(Exception ee2){}
		System.out.println("EXCEPTION IN INSERTING SRETURN :" + insertSt);
		writeLog(filePtr,insertSrEx,true);
		insertSrEx.printStackTrace();
	}
}
private void insertSReturnDet(HashMap saleReturnDetMap,Connection conn)
{
	String saleOrder = saleReturnDetMap.get("sale_order").toString().trim();
	PreparedStatement pstmt = null;
	int u=0;
	if(!saleOrderArr.contains(saleOrder.trim()))
	{
		try{writeLog(filePtr,"Adding Sale Order SaleOrder List (DBTOTE)*********["+saleOrder+"]",true);	}catch(Exception e){};
		saleOrderArr.add(saleOrderArr.size() , saleOrder.trim());
	}
	writeLog(filePtr,"      INSERTING INTO SRETURN(DETAIL)           ",true);
	writeLog(filePtr,"____________________________________________________________",true);
	double quantity =((Double)saleReturnDetMap.get("quantity")).doubleValue();
	System.out.println("Quantity= "+quantity);
	writeLog(filePtr,"Quantity= ["+quantity+"]",true);
	double rate = ((Double)saleReturnDetMap.get("rate")).doubleValue();
	writeLog(filePtr,"rate= ["+rate+"]",true);
	System.out.println("rate= "+rate);
	double totAmt = ((Double)saleReturnDetMap.get("tot_amt")).doubleValue();
	writeLog(filePtr,"totAmt= ["+totAmt+"]",true);
	System.out.println("totAmt= "+totAmt);
	double taxAmt = ((Double)saleReturnDetMap.get("tax_amt")).doubleValue();
	writeLog(filePtr,"taxAmt= ["+taxAmt+"]",true);
	System.out.println("taxAmt= "+taxAmt);
	double netAmt =taxAmt+totAmt;
	writeLog(filePtr,"netAmt= ["+netAmt+"]",true);
	System.out.println("netAmt= "+netAmt);
	double rateClg = ((Double)saleReturnDetMap.get("rate_clg")).doubleValue();
	writeLog(filePtr,"rateClg= ["+rateClg+"]",true);
	System.out.println("rateClg= "+rateClg);
	String locCode = saleReturnDetMap.get("loc_code").toString();
	writeLog(filePtr,"locCode= ["+locCode+"]",true);
	System.out.println("locCode= "+locCode);
	String lineNo = saleReturnDetMap.get("line_no").toString();
	writeLog(filePtr,"lineNo= ["+lineNo+"]",true);
	System.out.println("lineNo= "+lineNo);
	String 	lotSl = saleReturnDetMap.get("lot_sl").toString();
	writeLog(filePtr,"lotSl= ["+lotSl+"]",true);
	System.out.println("lotSl= "+lotSl);
	String lotNo = 	saleReturnDetMap.get("lot_no").toString();
	writeLog(filePtr,"lotNo= ["+lotNo+"]",true);
	System.out.println("lotNo= "+lotNo);
	String tranId = saleReturnDetMap.get("tran_id").toString();
	writeLog(filePtr,"tranId= ["+tranId+"]",true);
	System.out.println("tranId= "+tranId);
	String itemCode = saleReturnDetMap.get("item_code").toString();
	writeLog(filePtr,"itemCode= ["+itemCode+"]",true);
	System.out.println("itemCode= "+itemCode);
	String unit  = getUnitFrItem(itemCode);// saleReturnDetMap.get("unit").toString();
	System.out.println("unit= "+unit);
	writeLog(filePtr,"unit= ["+unit+"]",true);
	String taxClass = saleReturnDetMap.get("tax_class").toString();
	writeLog(filePtr,"taxClass= ["+taxClass+"]",true);
	String taxEnv = saleReturnDetMap.get("tax_env").toString();
	writeLog(filePtr,"taxEnv= ["+taxEnv+"]",true);
	String taxChap = saleReturnDetMap.get("tax_chap").toString();
	writeLog(filePtr,"taxChap= ["+taxChap+"]",true);
	String itemSer = saleReturnDetMap.get("item_ser").toString();
	writeLog(filePtr,"itemSer= ["+itemSer+"]",true);
	double costRate = ((Double)saleReturnDetMap.get("cost_rate")).doubleValue();
	writeLog(filePtr,"costRate= ["+costRate+"]",true);
	System.out.println("itemSer= "+itemSer);
	double mrpValues = rateClg * quantity;
	writeLog(filePtr,"rateClg * quantity/(mrpValues)= ["+mrpValues+"]",true);
	String insertSt  = "INSERT INTO SRETURNDET (TRAN_ID,LINE_NO,ITEM_CODE,QUANTITY,LOC_CODE, LOT_NO ,LOT_SL ,"
            +" UNIT ,ITEM_SER  ,RATE__CLG ,NET_AMT, REAS_CODE,TAX_CLASS,TAX_CHAP,TAX_ENV,TAX_AMT,UNIT__STD,CONV__QTY_STDUOM,"
 			+" CONV__RTUOM_STDUOM ,RATE,RATE__STD,DISCOUNT,QUANTITY__STDUOM,RATE__STDUOM,RET_REP_FLAG,SALE_ORDER, COST_RATE ,MRP_VALUE)"
            +" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     writeLog(filePtr,insertSt,true);
	try
	{
		pstmt = conn.prepareStatement(insertSt);
		pstmt.setString(1,tranId);
		pstmt.setInt(2,Integer.parseInt(lineNo));
		pstmt.setString(3,itemCode);
		pstmt.setDouble(4,quantity);
		pstmt.setString(5,locCode);
		pstmt.setString(6,lotNo);
		pstmt.setString(7,lotSl);
		pstmt.setString(8,unit);
//		pstmt.setDouble(12,taxAmt);
		pstmt.setString(9,itemSer);
		pstmt.setDouble(10,rateClg);
		pstmt.setDouble(11,netAmt);
		pstmt.setString(12,"   ");
		pstmt.setString(13,taxClass);
		pstmt.setString(14,taxChap);
		pstmt.setString(15,taxEnv);
		pstmt.setDouble(16,taxAmt);
		pstmt.setString(17,unit);
		pstmt.setDouble(18,1);
		pstmt.setDouble(19,1);
		pstmt.setDouble(20,rate);
		pstmt.setDouble(21,rate);
		pstmt.setDouble(22,0);
		pstmt.setDouble(23,quantity);
		pstmt.setDouble(24,rate);
		pstmt.setString(25,"P");
		pstmt.setString(26,saleOrder);
		pstmt.setDouble(27,costRate);
		pstmt.setDouble(28,mrpValues);
		//pstmt.setDouble(22,0)
		//pstmt.setDouble(11,quantity);
		u = pstmt.executeUpdate();
		writeLog(filePtr,"NO OF RECORE INSERT  ["+u+"]",true);
		writeLog(filePtr,"____________________________________________________________",true);
		pstmt.close();

	}
	catch(SQLException insertSrd)
	{
		try{conn.rollback();
		writeLog(filePtr,"TRANSATION ROLL BACK IN insertSReturnDet",true);}catch(Exception ee2){}
		System.out.println("SQLEXCEPTION IN INSERTING SRETURN:" + insertSt);
		writeLog(filePtr,insertSrd,true);
		insertSrd.printStackTrace();
	}
	catch(Exception insertSrdEx)
	{
		try
		{
			conn.rollback();
			writeLog(filePtr,insertSrdEx,true);
			writeLog(filePtr,"TRANSATION ROLL BACK IN insertSReturnDet",true);
		}catch(Exception ee2){}
	}
}
	private String generateTranId(String windowName,String orderDate,String siteCode,Connection conn)
	{
		writeLog(filePtr,"generateTranId....",true);
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String tranId = "";
		String newKeystring = "";
		String srType = "RS";
		 try
	     {
	    	sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)= '"+windowName+"'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			System.out.println("keyString :"+rs.toString());
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rs.next())
			{
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);
			}
			// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [Start]
			if( stmt != null )
			{
				stmt.close();
				stmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [End]
			
			writeLog(filePtr,"keyString :"+keyString,true);
			writeLog(filePtr,"keyCol :"+keyCol,true);
			writeLog(filePtr,"tranSer1 :"+tranSer1,true);
			writeLog(filePtr,"siteCode :"+siteCode,true);

			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer1 :"+tranSer1);
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>\r\n";
			xmlValues = xmlValues + "<Detail1>\r\n";
			xmlValues = xmlValues +	"<tran_id></tran_id>\r\n";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>\r\n";
			xmlValues = xmlValues + "<tran_date>"+ orderDate + "</tran_date>\r\n";
			xmlValues = xmlValues + "<tran_type>" + srType + "</tran_type>\r\n";
			xmlValues = xmlValues + "</Detail1>\r\n</Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
		}
		catch (SQLException ex)
		{

			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
			writeLog(filePtr,ex,true);
			ex.printStackTrace();

		}
		catch (Exception e)
		{

			System.out.println("Exception ::" + e.getMessage() + ":");
			writeLog(filePtr,e,true);
			e.printStackTrace();
		}
		return tranId;
	}//generateTranTd()

//FINDING PRICELIST AND PRICELISTCLG FOR SRETURN BY GIVING SALEORDER
private void cust(String custCode,Connection conn)
{
		String tranCode = null;
		String currCode = null;
		String transMode = null;
		String custCodeBill = null;
		String custCodeDlv = null;
		System.out.println("sql::"+sql);
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		double exchRate = 0.0;
		String sqlEx =null;
		String exchRateStr=null;
		//	Finding curr_code,tran_code,tran_mode From Customer by custcode
		String frsSql  = "SELECT  TRAN_CODE, CURR_CODE, TRANS_MODE ,CUST_CODE__BIL  FROM CUSTOMER WHERE CUST_CODE = ?";
		try
		{
			pstmt = conn.prepareStatement(frsSql);
			pstmt.setString(1,custCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranCode = (rs.getString(1)==null?"":rs.getString(1));
				currCode = (rs.getString(2)==null?"":rs.getString(2));
				transMode = (rs.getString(3)==null?"":rs.getString(3));;
				custCodeBill = (rs.getString(4)==null?"":rs.getString(4));;
			}
			//***********added later by tarani for adding exch rate 15092006
			if(currCode!=null)
			{
				sqlEx = "SELECT STD_EXRT FROM CURRENCY WHERE  CURR_CODE ='"+currCode+"'";
				pstmt = conn.prepareStatement(sqlEx);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					exchRate = rs.getDouble(1);
				}
			}
			rs.close();
			pstmt.close();
			System.out.println("tranCode"+tranCode);
			System.out.println("currCode"+currCode);
			System.out.println("transMode"+transMode);
			System.out.println("custCodeBill"+custCodeBill);
			exchRateStr = new Double(exchRate).toString();
			saleReturnMap.put("tran_code",tranCode);
			saleReturnMap.put("curr_code",currCode);
			saleReturnMap.put("tran_mode",transMode);
			saleReturnMap.put("cust_code__bill",custCodeBill);
			saleReturnMap.put("cust_code__dlv",custCodeDlv);
			saleReturnMap.put("exch_rate",exchRateStr);
		}
		catch(SQLException se1)
		{
			System.out.println("SQLException 2:" + frsSql);
			writeLog(filePtr,se1,true);
			se1.printStackTrace();
		}
		catch(Exception e1)
		{
			System.out.println("Exception 2 :" + frsSql);
			writeLog(filePtr,e1,true);
			e1.printStackTrace();
		}
}
void cleanup()
{
	itemCodeMap.clear();
	custCodeMap.clear();

}

//TAX CALCULATION PROCEDURE
private void handleTax(String tranId,int lineNo,String tranDate,double quantity,double rate, String currCode,String siteCode,int ctr,String taxClass,String taxChap,String taxEnv,double rateClg,double discount,Connection conn)throws Exception
{
		StringBuffer valueXmlString =null;
		try
		{
			writeLog(filePtr,"                H A N D L E           T A X              ",true);
			writeLog(filePtr,"_________________________________________________________",true);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>");
			valueXmlString.append("<Root>");
			valueXmlString.append("<Detail2 dbID='' domID='"+ctr+"' objName='sreturn' objContext='2'>");
			valueXmlString.append("<attribute pkNames='' status='N' updateFlag='A' selected='N' />");
			valueXmlString.append("<tran_id>").append(tranId).append("</tran_id>");
			valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
			valueXmlString.append("<tax_date>").append(tranDate).append("</tax_date>");
			valueXmlString.append("<rate>").append(rate).append("</rate>");
			valueXmlString.append("<rate__clg>").append(rateClg).append("</rate__clg>");
			valueXmlString.append("<tax_class>").append(taxClass).append("</tax_class>");
			valueXmlString.append("<tax_chap>").append(taxChap).append("</tax_chap>");
			valueXmlString.append("<tax_env>").append(taxEnv).append("</tax_env>");
			valueXmlString.append("<tax_amt>").append("0").append("</tax_amt>");
			valueXmlString.append("<discount>").append(discount).append("</discount>");
			valueXmlString.append("<quantity>").append(quantity).append("</quantity>");
			valueXmlString.append("<Taxes/>");
			valueXmlString.append("</Detail2>");
			valueXmlString.append("</Root>");
			writeLog(filePtr,"valueXmlString :::::"+valueXmlString.toString(),true);
			Document itemDoc = genericUtility.parseString(valueXmlString.toString());
			Node currRecordNode = itemDoc.getElementsByTagName("Detail2").item(0);
			TaxCalculation taxCal = new TaxCalculation();
			//TaxCalculation taxCal = new TaxCalculation("sorder");
			writeLog(filePtr,"CurrNode:::(Before (appendOrReplaceTaxesNode)"+currRecordNode,true);
			appendOrReplaceTaxesNode(currRecordNode);
			writeLog(filePtr,"CurrNode:::(After (appendOrReplaceTaxesNode)"+currRecordNode,true);
			NodeList currRecordChildList = currRecordNode.getChildNodes();
			int childListLength = currRecordChildList.getLength();
			Node currTaxNode = null;
			for (int i = 0; i < childListLength; i++)
			{
				if (currRecordChildList.item(i).getNodeName().equalsIgnoreCase("Taxes"))
				{
					currTaxNode = currRecordChildList.item(i);
				}
			}
			taxCal.setUpdatedTaxDom(currTaxNode);
			taxCal.setTaxDom(currTaxNode);
			String domId = Integer.toString(ctr);
			writeLog(filePtr,"Before setDataNode"+currRecordNode,true);
			taxCal.setDataNode(currRecordNode,"2",domId);
		//	taxCal.setDataNode(currRecordNode);
			writeLog(filePtr,"After:::(setDataNode)"+currRecordNode,true);
			//taxCal.taxCalc("S-RET", tranId,tranDate,"rate__stduom", "quantity__stduom", currCode,siteCode,"2");//...added form no "2" ** vishakha
			taxCal.taxCalc("S-RET", tranId,tranDate,"rate__stduom", "quantity__stduom", currCode,siteCode,"2", "1");//...added form no "2" ** vishakha
			writeLog(filePtr,"CurrentTaxNode ::"+serializeDom(currTaxNode),true);
			writeLog(filePtr,"CurrentRecordNode ::"+serializeDom(currRecordNode),true);
			taxAmount = genericUtility.getColumnValueFromNode("tax_amt",currRecordNode);
			saleReturnDetMap.put("tax_amt",new Double(taxAmount));
			//saleReturnDetMap.put("tax_amt",new Double(taxCal.totalTax));
			writeLog(filePtr,"Tax Amt :[" + taxAmount+"]",true);
			//taxAmt = Double.parseDouble(taxAmount);
			currRecordChildList = currRecordNode.getChildNodes();
			for (int i = 0; i < childListLength; i++)
			{
				if (currRecordChildList.item(i).getNodeName().equalsIgnoreCase("Taxes"))
				{
					currTaxNode = currRecordChildList.item(i);
					saveData(currTaxNode,conn);
				}
			}
		writeLog(filePtr,"_________________________________________________________"+currRecordNode,true);
		}
		catch(Exception e)
		{
			writeLog(filePtr,e,true);

			throw new ITMException(e);
		}
	}
	private void appendOrReplaceTaxesNode(Node currRecordNode)throws ITMException
	{
		boolean found = false;
		try
		{

			if(	this.blankTaxString == null)
			{
				MasterDataStatefulEJB masterDataStateful = new MasterDataStatefulEJB();
				blankTaxString = masterDataStateful.getBlankTaxDomForAdd("2");//***added "2" previously it was blank **vishakha
			}
			NodeList dataNodeChildList = currRecordNode.getChildNodes();
			int dataNodeChildListLen = dataNodeChildList.getLength();
			for (int i=0; i < dataNodeChildListLen; i++)
			{
				if (dataNodeChildList.item(i) != null)
				{
					if (dataNodeChildList.item(i).getNodeName().equalsIgnoreCase("Taxes") )
					{
						currRecordNode.replaceChild(currRecordNode.getOwnerDocument().importNode(genericUtility.parseString(blankTaxString).getFirstChild(), true),dataNodeChildList.item(i));
						found = true;
						break;
					}
				}
			}
			if (!found)
			{
				currRecordNode.appendChild(currRecordNode.getOwnerDocument().importNode(genericUtility.parseString(blankTaxString).getFirstChild(), true));
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :: removeTaxNodeInDetail :==>\n"+e);
			writeLog(filePtr,e,true);
			throw new ITMException(e);
		}
	}
	private String serializeDom(Node dom)throws ITMException
	{
		String retString = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
			retString = out.toString();
			out.flush();
			out.close();
			out = null;
		}
		catch (Exception e)
		{
			System.out.println("Exception :: serializeDom :"+e);
			writeLog(filePtr,e,true);
			throw new ITMException(e);
		}
		return retString;
	}

	private void saveData (Node currNode,Connection conn) throws Exception
	{
		try
		{
			writeLog(filePtr,"Current Node:::"+currNode,true);
			StringBuffer fieldNameBuff = new StringBuffer();
			StringBuffer fieldValueBuff = new StringBuffer();
			StringBuffer insertQueryBuff = new StringBuffer();
			PreparedStatement pstmt =null;
			String taxtranSql =null;
			int q =0,noOfField = 0,nodeLength = 0,prepCount=0,i=0;
		//******this block is used to collect field and corresponding type of taxtran
			String sql ="SELECT * FROM TAXTRAN";
			HashMap columnAndTypeMap = new HashMap();
			HashMap xmlFldTypeMap = new HashMap();
			ResultSet rs =null;
			pstmt = conn.prepareStatement(sql);
		 	rs=pstmt.executeQuery();
		  	ResultSetMetaData rsmd = rs.getMetaData();
		 	int noOfColumn =rsmd.getColumnCount();
		 //	writeLog(filePtr,"T A B L E S T R U C T U R E(TAXTRAN)",true);

	 	for( i=1;i<=noOfColumn;i++)
	 	{

	 		String colName = rsmd.getColumnName(i);
	 		String colType = rsmd.getColumnTypeName(i);
	 	//	writeLog(filePtr,"COLUMN_NAME["+colName+"]   "+"COLUMN_TYPE["+colType+"]",true);
	 		columnAndTypeMap.put(colName.toUpperCase().trim(),colType.toUpperCase().trim());
	 		if(i==noOfColumn)
	 		{
		 		fieldNameBuff.append(rsmd.getColumnName(i));
		 		fieldValueBuff.append("?");
	 		}
	 		else
	 		{
		 		fieldNameBuff.append(rsmd.getColumnName(i)).append(",");
		 		fieldValueBuff.append("?").append(",");
	 		}

	 	}
	 	taxtranSql = "INSERT INTO TAXTRAN (" +fieldNameBuff.toString()+") VALUES ("+fieldValueBuff.toString()+")";
	 	writeLog(filePtr,"taxtranSql**************"+taxtranSql,true);
	 	rs.close();
	 	pstmt.close();
	 	pstmt= conn.prepareStatement(taxtranSql);
	 	Node currChildNode = null;
		NodeList currNodeList = currNode.getChildNodes();
		nodeLength = currNodeList.getLength();
		for ( i = 0;i < nodeLength ; i++ )
		{
				prepCount =0;
				Node taxNode = currNodeList.item(i);
				NodeList taxfield = taxNode.getChildNodes();
				noOfField = taxfield.getLength();
				for (int j = 0;j < noOfField ; j++ )
				{
					String fieldName = 	taxfield.item(j).getNodeName();
					if (taxfield.item(j).getFirstChild() != null && taxfield.item(j).getFirstChild().getNodeValue() != null && taxfield.item(j).getFirstChild().getNodeValue().trim().length() > 0)
					{
						String ColValue =taxfield.item(j).getFirstChild().getNodeValue().trim();
						if(!ColValue.equalsIgnoreCase("null"))
						xmlFldTypeMap.put(fieldName.toUpperCase().trim(),ColValue);
					//	writeLog(filePtr,"FROM XML COLUMN_NAME["+fieldName+"] COLUMN_TYPE["+ColValue+"]",true);
					}
				}
				String[] totColInQry = fieldNameBuff.toString().split(",");
			//	writeLog(filePtr,"Total Column in taxtran:::["+totColInQry.length+"]" ,true);

				for(int j =0;j<totColInQry.length;j++)
				{
					String column = totColInQry[j];
					String dataType = columnAndTypeMap.get(column.toUpperCase()).toString();
					prepCount++;
					if(xmlFldTypeMap.containsKey(column.toUpperCase()))
					{
					//	writeLog(filePtr,"Found in Dom["+column+"]" ,true);
						String dataValue = xmlFldTypeMap.get(column.toUpperCase()).toString();
						if((dataType.toUpperCase().equalsIgnoreCase("CHAR"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR2"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR")))
						{
						//	writeLog(filePtr,"setting String value===>"+dataValue+"<===["+prepCount+"]" ,true);
							if (column.equalsIgnoreCase("line_no") )
							{
								String line_No = (("    "+dataValue).substring(("    "+dataValue).length()-3));
								pstmt.setString(prepCount,line_No);
							}
							else
							{
								pstmt.setString(prepCount,String.valueOf(dataValue));
							}
						}
						else if ((dataType.toUpperCase().equalsIgnoreCase("NUMBER")))
						{
						//	writeLog(filePtr,"setting Number value===>"+dataValue+"<===["+prepCount+"]" ,true);
							pstmt.setDouble(prepCount, Double.valueOf(dataValue).doubleValue());
						}
						else if ((dataType.toUpperCase().equalsIgnoreCase("DATE"))||(dataType.toUpperCase().equalsIgnoreCase("TIMESTAMP")))
						{
						//	writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.TIMESTAMP);
						}

					}
					else
					{
					//	writeLog(filePtr,"Not Found in Dom{"+column+"}" ,true);
						if((dataType.toUpperCase().equalsIgnoreCase("CHAR"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR2")))
						{

						//	writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.VARCHAR);
						}
						else if((dataType.toUpperCase().equalsIgnoreCase("NUMBER")))
						{
						//	writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.DOUBLE);
						}
						else if ((dataType.toUpperCase().equalsIgnoreCase("DATE"))||(dataType.toUpperCase().equalsIgnoreCase("TIMESTAMP")))
						{
						//	writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.TIMESTAMP);
						}
					}
				}
				pstmt.addBatch();
		}
		pstmt.executeBatch();
	}
	catch(SQLException se)
	{
		writeLog(filePtr,se,true);
		throw se;
	}
	catch(Exception e)
	{
		writeLog(filePtr,e,true);
		throw e;
	}
}
//GETTING THE VALID DATE FORMAT
	public String getValidDateString(String dateStr, String sourceDateFormat, String targetDateFormat) throws ITMException
	{
		Object date = null;
		String retDateStr = "";
		try
		{
			writeLog(filePtr,"sourceDateFormat="+sourceDateFormat,true);
			writeLog(filePtr,"targetDateFormat="+targetDateFormat,true);
			writeLog(filePtr,"ORIGINAL date="+dateStr,true);
			if (!(sourceDateFormat.equalsIgnoreCase(targetDateFormat)))
			{
				if (sourceDateFormat.indexOf("/") != -1)
				{
					dateStr.replace('/', '-');
				}
				else if (sourceDateFormat.indexOf(".") != -1)
				{
					dateStr.replace('.', '-');
				}
				writeLog(filePtr,"parsing date="+dateStr,true);
				date = new SimpleDateFormat(sourceDateFormat).parse(dateStr);
				SimpleDateFormat sdfOutput = new SimpleDateFormat(targetDateFormat);
				retDateStr = sdfOutput.format(date);
			}
			else
			{

				retDateStr = dateStr;
			}
			writeLog(filePtr,"return from getValidDateStringe="+retDateStr,true);
		}
		catch (Exception e)
		{
			writeLog(filePtr,e,true);
			System.out.println("Exception :GenericUtility :getValidDateString :==>"+e.getMessage());
			throw new ITMException(e);
		}
		//System.out.println("[GenericUtility]Converted Datestr :["+retDateStr+"]");
		return retDateStr;
	}
	static void writeLog(File f,String ex,boolean flag)
	{
		try
		{
			PrintWriter pw = new PrintWriter((new FileOutputStream(f,flag)),flag);
			pw.println(ex);
			pw.close();
		}
		catch(Exception exWm){exWm.printStackTrace();}
	}
	 static void writeLog(File f,Exception ex,boolean flag)
	{
		try
		{
			PrintWriter pw = new PrintWriter((new FileOutputStream(f,flag)),flag);
			ex.printStackTrace(pw);
			pw.close();
		}
		catch(Exception exWe){exWe.printStackTrace();}
	}
//CALCULATE EXPIRY DATE FOR A ITEM
public java.sql.Timestamp calculateExpDate(String itemCode,String saleOrder,String lineNo,String Explev, Connection conn)
{
	java.sql.Timestamp expDate= null;
	try
	{

		String trackShelfLife ="";
		int lifePrd=0;
		int curDate = 0;
		int	curMonth = 0;
		int	curYear = 0;
		writeLog(filePtr,"IN calculateExpDate.............",true);
		String sql  = "SELECT  TRACK_SHELF_LIFE FROM ITEM WHERE ITEM_CODE='"+itemCode+"'";
		writeLog(filePtr,"ITEM SQL:::"+sql,true);
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		pstmt = conn.prepareStatement(sql);
		rs=pstmt.executeQuery();
		if(rs.next())
		{
			trackShelfLife = rs.getString(1);
		}
		writeLog(filePtr,"trackShelfLife:::["+trackShelfLife+"]",true);
		rs.close();
		pstmt.close();
		if(trackShelfLife.equalsIgnoreCase("Y"))
		{
					 sql  = "SELECT MIN_SHELF_LIFE  FROM SORDITEM WHERE SALE_ORDER = '" +saleOrder+ "' AND LINE_NO = '" + lineNo + "' AND EXP_LEV ='"+Explev+"' AND ITEM_CODE='"+itemCode+"'";
					writeLog(filePtr,"SORDITEM SQL"+sql,true);
					pstmt = conn.prepareStatement(sql);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						lifePrd = rs.getInt(1);
						writeLog(filePtr,"lifePeriod["+lifePrd+"]",true);
					}
					rs.close();
					pstmt.close();

		}
		Calendar calCurr = Calendar.getInstance();
		curDate = calCurr.get(calCurr.DATE);
		curMonth = calCurr.get(calCurr.MONTH);
		curYear = calCurr.get(calCurr.YEAR);
		curMonth =curMonth+1;
		writeLog(filePtr,"CURRENT DATE:::::["+curYear+"-"+curMonth+"-"+curDate+"]",true);

		calCurr.add(calCurr.MONTH,lifePrd);
		curDate = calCurr.get(calCurr.DATE);
		curMonth = calCurr.get(calCurr.MONTH);
		curYear = calCurr.get(calCurr.YEAR);
		curMonth =curMonth+1;

		String ValidDate = new Integer(curYear).toString()+ "-"+new Integer(curMonth).toString()+"-"+new Integer(curDate).toString();
		writeLog(filePtr,"ValidDate:::"+ValidDate,true);

		expDate =	java.sql.Timestamp.valueOf(ValidDate + " 00:00:00.0");
		writeLog(filePtr,"After adding ["+lifePrd+"]"+"Date "+expDate.toString(),true);
		writeLog(filePtr,"Returning expDate"+expDate.toString(),true);

	}
	catch(Exception ee)
	{
		writeLog(filePtr,"EXCEPTION IN DATE CALCULATING",true);
		writeLog(filePtr,ee,true);
	}
	return expDate;



}
//SCAN THE STOCK SITE AND ITEM CODE WISE

private String  scanStock(String saleOrder, String lineNo, String itemCode, String siteCode,double allocQty, String expLev,String frsBrowFlag,Connection conn)
{
	writeLog(filePtr,"S C A N N I N G S T O C K- - - - - - - - - - >["+allocQty+"]",true);
	writeLog(filePtr,"F R S B A L A N C E ["+frsBalInit+"]",true);

	writeLog(filePtr,"saleOrder["+saleOrder+"]lineNo["+lineNo+"]itemCode["+itemCode+"]siteCode["+siteCode+"]expLev["+expLev+"] frsBrowFlag["+frsBrowFlag+"]",true);


	PreparedStatement pstmtStock = null;
	ResultSet rs = null ;
	double qtyAvailAlloc =0;
	double freeQty =0;
	double saleQty = 0;
	double convQtyStduom=0;
	double quantity=0;
	String getDataSql = "";
	String errCode = "";
	String errString = "";
	String currAppdate = "";
	String lotNo="";
	String lotSl="";
	String locCode ="";
	String unit = "";
	String grade="";
	double mrpValue=0;
	String priceList ="";
	String stockSql  ="";
	String siteCodeMfg ="";
	double lotQtyToBeAllocated =0;
	double rateClg=0;
	String priceListRate="";
	double rate=0;
	currAppdate = this.applCurrDate;
	java.sql.Timestamp calExpDate = null;
	java.sql.Timestamp expDate = null;
	java.sql.Timestamp mfgDate = null;
	expDate = new java.sql.Timestamp(System.currentTimeMillis());
	mfgDate = new java.sql.Timestamp(System.currentTimeMillis());
	java.sql.Timestamp dateAlloc= new java.sql.Timestamp(System.currentTimeMillis());
	try
	{
			stockSql = "SELECT PRICE_LIST__CLG,PRICE_LIST FROM SORDER WHERE SALE_ORDER='"+saleOrder+"'";
			writeLog(filePtr,"SQL :::-["+stockSql+"]",true);
			pstmtStock = conn.prepareStatement(stockSql);
			rs = pstmtStock.executeQuery();
			writeLog(filePtr,"PRICE_LIST__CLG SQL "+stockSql,true);
			if(rs.next())
			{
				priceList = (rs.getString(1)==null?" ":rs.getString(1));
				priceListRate = (rs.getString(2)==null?" ":rs.getString(2));
				writeLog(filePtr,"PriceList:::["+priceList+"]",true);
				writeLog(filePtr,"PriceListRate:::["+priceListRate+"]",true);
			}
			rs.close();
			pstmtStock.close();
			getDataSql = "SELECT STOCK.LOT_NO,STOCK.LOT_SL,"
					   +"STOCK.LOC_CODE, "
					   +"STOCK.UNIT,  "
					   +"(STOCK.QUANTITY - STOCK.ALLOC_QTY ) AS QTY_AVAIL_ALLOC ,"
					   +"STOCK.GRADE,STOCK.EXP_DATE,STOCK.CONV__QTY_STDUOM,STOCK.QUANTITY, "
					   +"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG "
					   +"FROM STOCK,ITEM,LOCATION,INVSTAT "
					   +"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
					   +"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
					   +"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
					   +"AND INVSTAT.AVAILABLE = 'Y' "
					   +"AND STOCK.ITEM_CODE = '" + itemCode.trim() + "' AND STOCK.SITE_CODE = '" +siteCode.trim() + "' "
					   +"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY) > 0  "
					   +"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' )"
					   +" ORDER BY STOCK.EXP_DATE, STOCK.LOT_NO, STOCK.LOT_SL "	;
					   writeLog(filePtr,"STOCK SQL :::-["+getDataSql+"]",true);
			pstmtStock = conn.prepareStatement(getDataSql);
			rs = pstmtStock.executeQuery();
			System.out.println("siteCode------"+ siteCode);
			System.out.println("itemCode------"+ itemCode);
			System.out.println("Select completed");
			int stkCtr = 0;
			while(rs.next())
			{
				if (allocQty <= 0)
				{
					break;
				}
				System.out.println( "INSIDE WHILE LOOP.............");
				writeLog(filePtr,"sorderAllocate inside stock loop "+allocQty,true);
				//LOT_NO
				lotNo = rs.getString(1);
				if(lotNo == null)
				lotNo = "  ";
				//LOT_SL
				lotSl = rs.getString(2);
				if(lotSl == null)
				lotSl = "  ";
				//LOC_CODE
				locCode = rs.getString(3);
				if(locCode == null)
				locCode = "  ";
				//UNIT
				unit = rs.getString(4);
				//QTY_AVAIL_ALLO
				qtyAvailAlloc = rs.getDouble(5);
				//GRADE
				grade = rs.getString(6);
				//EXP_DATE
				expDate =rs.getTimestamp(7);
				//CONV__QTY_STDUOM
				convQtyStduom=rs.getDouble(8);
				//QUANTITY
				quantity= rs.getDouble(9);
				//MFG_DATE
				mfgDate = rs.getTimestamp(10);
				//SITE_CODE__MFG
				siteCodeMfg = rs.getString(11);
				writeLog(filePtr,"Parameter pass to calculateExpDate " ,true);
				writeLog(filePtr,"itemCode::"+itemCode ,true);
				writeLog(filePtr,"saleOrder::"+saleOrder ,true);
				writeLog(filePtr,"lineNo::"+lineNo ,true);
				writeLog(filePtr,"Explev::"+expLev ,true);
				calExpDate= calculateExpDate(itemCode,saleOrder,lineNo,expLev,conn);
				writeLog(filePtr,"calExpDate::"+calExpDate ,true);
				writeLog(filePtr,"expDate(From Stck)::"+expDate ,true);
				if(calExpDate.compareTo(expDate)> 0)
				{
					if(lotNo==null)lotNo="";if(lotSl==null)lotSl="";if(locCode==null)locCode="";
					writeLog(filePtr,"ItemExpired[sorderAllocate]:::itemCode:-["+itemCode+"]"+"siteCode:-["+siteCode+"]"+"lotNo:-["+lotNo+"]"+"lotSl:-["+lotSl+"]"+"locCode:-["+locCode+"]",true);
					continue;
				}
				writeLog(filePtr,"allocQty::-["+allocQty+"]" ,true);
				writeLog(filePtr,"qtyAvailAlloc::-["+qtyAvailAlloc+"]" ,true);
				if (allocQty >= qtyAvailAlloc)
				{
					lotQtyToBeAllocated = qtyAvailAlloc;
				}
				else
				{
					lotQtyToBeAllocated = allocQty;
				}
				writeLog(filePtr,"lotQtyToBeAllocated:::-["+lotQtyToBeAllocated+"]" ,true);
				invallocTraceMap.put("site_code",siteCode);
				invallocTraceMap.put("item_code",itemCode);
				invallocTraceMap.put("loc_code",locCode);
				invallocTraceMap.put("lot_no",lotNo);
				invallocTraceMap.put("lot_sl",lotSl);
				if(expDate != null)
				{
					invallocTraceMap.put("exp_date",expDate.toString());
				}
				else
				{
					invallocTraceMap.put("exp_date"," ");
				}
				if(mfgDate != null)
				{
					invallocTraceMap.put("mfg_date",mfgDate.toString());
				}
				else
				{
					invallocTraceMap.put("mfg_date"," ");
				}

		 		writeLog(filePtr,"_______________________________________________________________",true);
				writeLog(filePtr,"Parameter pass to pick rate method of discommon>>>>>>>",true);
				writeLog(filePtr,"priceList:-"+priceList,true);
				writeLog(filePtr,"currDate:-"+currAppdate,true);
				writeLog(filePtr,"itemCode:-"+itemCode,true);
				writeLog(filePtr,"lotNo:-"+lotNo,true);
				writeLog(filePtr,"qtyFrSordDet:-"+lotQtyToBeAllocated,true);
				rateClg =distCommonObj.pickRate(priceList,currAppdate,itemCode,lotNo,"",lotQtyToBeAllocated,conn);

				writeLog(filePtr,"rate calculate price list goes:-"+priceListRate,true);
				rate=distCommonObj.pickRate(priceListRate,currAppdate,itemCode,lotNo,"",lotQtyToBeAllocated,conn);
				writeLog(filePtr,"Rate clg from pickRate>>>>>>>>>>>>>>["+rateClg+"]",true);
				writeLog(filePtr,"Rate  from pickRate>>>>>>>>>>>>>>["+rate+"]",true);
				if(rateClg<0)
				{
					priceNotFound = true;
					writeLog(filePtr,"priceListNotFound::::::::["+priceNotFound+"]",true);

				}
				if(rate<0)
				{
					priceNotFound = true;
					writeLog(filePtr,"RateNotFound::::::::["+priceNotFound+"]",true);

				}
				stkCtr++;
				writeLog(filePtr,"Stock [" + stkCtr + "] loc_code [" + locCode + "] Lot_no [" + lotNo + "[ lot_sl [" + lotSl + "]" ,true);
				saleReturnDetMap.put("rate_clg",new Double(rateClg));
				saleReturnDetMap.put("rate",new Double(rate));
				writeLog(filePtr,"_______________________________________________________________",true);
				allocQty -= lotQtyToBeAllocated ;
				writeLog(filePtr,"After Deducting :::-["+lotQtyToBeAllocated+"] Allocate Qty is ["+allocQty+"]" ,true);
				writeLog(filePtr,"siteCode["+siteCode+"]",true);
				if(lotQtyToBeAllocated > 0)
				{
					if(frsBrowFlag.trim().equalsIgnoreCase("Y"))
					{
						mrpValue = lotQtyToBeAllocated * rateClg;
						if(mrpValue <= frsBalInit)
						{
							writeLog(filePtr,"[mrpValue < frsBalInit]",true);
							writeLog(filePtr,"Frs Balance["+frsBalInit+"]",true);
							frsBalInit = frsBalInit - mrpValue;
							writeLog(filePtr,"After Deducting["+mrpValue+"] Fre Balance["+frsBalInit+"]",true);
							freeQty = lotQtyToBeAllocated;
							mrpValue = updateRecMap(mrpValue,refNoMap);
							writeLog(filePtr,"freeQty["+freeQty+"]",true);
							errString = debitNoteGenerate(saleOrder,lineNo,itemCode,freeQty,expLev,conn) ;
						}
						else
						{
							writeLog(filePtr,"[mrpValue > frsBalInit]",true);
							writeLog(filePtr,"Frs Balance["+frsBalInit+"]",true);
							writeLog(filePtr,"rateClg["+rateClg+"]",true);
							freeQty = Math.floor(frsBalInit / rateClg);
							if(freeQty !=0)
							{
									writeLog(filePtr,"[frsBalInit > rateClg]",true);
									writeLog(filePtr,"freeQty["+freeQty+"]",true);
									frsBalInit =  frsBalInit - freeQty * rateClg ;
									mrpValue = freeQty * rateClg;
									saleQty = lotQtyToBeAllocated - freeQty;
									writeLog(filePtr,"saleQty["+saleQty+"]",true);
									errString = debitNoteGenerate(saleOrder,lineNo,itemCode,freeQty,expLev,conn) ;
									mrpValue =  updateRecMap(mrpValue,refNoMap);
									if(saleQty >=0)
									{
										errString = sorderAllocate(saleOrder, lineNo, itemCode, saleQty, expLev,convQtyStduom,conn);
									//	allocateFlag = true;
									}
				 			}
							else
							{
								writeLog(filePtr,"[rateClg > frsBalInit]",true);
								writeLog(filePtr,"saleQty["+lotQtyToBeAllocated+"]",true);
							//	allocateFlag = true;
								errString = sorderAllocate(saleOrder, lineNo, itemCode, lotQtyToBeAllocated, expLev,convQtyStduom,conn);
							}
						}
					}//END IF
					else
					{
						errString = sorderAllocate(saleOrder, lineNo, itemCode, lotQtyToBeAllocated, expLev,convQtyStduom,conn);

					}
				}
			}
	}
	catch(Exception ff)
	{
		errString = "EXCEPTION";
		writeLog(filePtr,ff,true);
	}
	return errString;
}
//DEBITE NOTE GENERATION FOR FREE QUANTITY
private String debitNoteGenerate(String saleOrder, String lineNo, String itemCode, double freeQty, String expLev,Connection conn) throws ITMException
{
		writeLog(filePtr,"custFlag["+custFlag+"]",true);
		String siteCode =invallocTraceMap.get("site_code").toString();
		if(custFlag==true)
		{

			//tranId = generateTranId("W_SALESRETURN",tranDate,siteCode,conn);
		    writeLog(filePtr,"SiteCode calling before tranid gen["+siteCode+"]",true);

		    tranId = generateTranId("W_SALESRETURN",getCurrdateAppFormat(),siteCode,conn);

		    writeLog(filePtr,"SiteCode calling after tranid gen["+siteCode+"]",true);
			saleReturnMap.put("tran_id",tranId);
			saleReturnDetMap.put("tran_id",tranId);
			insertSReturn(saleReturnMap,conn);
			writeLog(filePtr,"SALE RETURN TRANID[[[[[[[[[[[[[["+tranId+"]]]]]]]]]]]]]]]]]]]]]]]]]",true);
			custFlag = false;
		}

		//String siteCode =invallocTraceMap.get("site_code").toString();
		itemCode = 	invallocTraceMap.get("item_code").toString();
		String locCode = invallocTraceMap.get("loc_code").toString();
		String lotNo = 	invallocTraceMap.get("lot_no").toString();
		String lotSl = invallocTraceMap.get("lot_sl").toString();
		String tranId = saleReturnMap.get("tran_id").toString();
		String chgUser =  saleReturnMap.get("chg_user").toString();
		String chgTerm =  saleReturnMap.get("chg_term").toString();
		int intCnt=0;
		double stockQuantity = 0;
		double qtyAvailAlloc = 0;
		double lotQtyToBeAllocated = 0;
		String getDataSql = "";
		java.sql.Timestamp calExpDate=null;
		java.sql.Timestamp expDate = new java.sql.Timestamp(System.currentTimeMillis());
		java.sql.Timestamp mfgDate = new java.sql.Timestamp(System.currentTimeMillis());
		double quantity =0;
		double convQtyStduom = 0 ;
		double quantityStduom = 0 ;
		double pendingQuantity = 0;
		double costRate =0.0;
		String unitStd = "" ;
		bFlag=1;

		String itemShDescr = "";
		String locDescr = "";
		String unit = "";
		String grade = "";
		String siteCodeMfg = "";
		String itemRef = "";
		String status ="";
		String updateSorditem = "";
		String insertSql = "";
		String 	errString = "";
		PreparedStatement pstmtStock = null;
		PreparedStatement pstmtStockInsertSordAlloc = null;
		Statement st = null;
		ResultSet rs = null ;
		ResultSet rsSItem = null;

		try
		{
				invallocTraceMap.put("ref_id",tranId);
				invallocTraceMap.put("ref_line",lineNo);
				invallocTraceMap.put("chg_user",chgUser);
				invallocTraceMap.put("chg_term",chgTerm);
				invallocTraceMap.put("chg_win","W_SORDALLOC");
				invallocTraceMap.put("ref_ser","S-ALC");
				invallocTraceMap.put("alloc_qty",new Double(freeQty));

				writeLog(filePtr,"IN....[debitNoteGenerate] ",true);
				writeLog(filePtr,"ref_id:-"+tranId+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"ref_line:-"+lineNo+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"siteCode:-"+siteCode+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"item_code:-"+itemCode+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"saleOrder:-"+saleOrder+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"locCode:-"+locCode+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"lotNo:-"+lotNo+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"lotSl:-"+lotSl+"[debitNoteGenerate] ",true);
				writeLog(filePtr,"lotQtyToBeAllocated["+freeQty+"][debitNoteGenerate] ",true);

			 	errString = InvAllocTraceObj.updateInvallocTrace(invallocTraceMap,conn);
			 	writeLog(filePtr,"Stock Update For dbtnote generation..."+"[debitNoteGenerate] "+errString,true);
				System.out.print("freeQty = " + freeQty);

				int line =Integer.parseInt(saleReturnDetMap.get("line_no").toString());
				line ++;
				updateSordItem(saleOrder,lineNo,freeQty, expLev);
				//String tranDate =(String)saleReturnMap.get("tran_date");
				String tranDate ="";
				String 	taxClass = (String)saleReturnDetMap.get("tax_class");
				double rateClg =((Double)saleReturnDetMap.get("rate_clg")).doubleValue();//setPlistTaxClassEnv(siteCode,siteCode,itemCode,"FE",itemSer,"TAX_CLASS",conn);
				String taxEnv = (String)saleReturnDetMap.get("tax_env");
				String taxChap = (String)saleReturnDetMap.get("tax_chap");
				double rate = ((Double)saleReturnDetMap.get("rate")).doubleValue();
				double total = rate*freeQty;
				saleReturnDetMap.put("tot_amt",new Double(total));
				double discount = 0;
				String currCode = 	(String)saleReturnMap.get("curr_code");
				tranId  = (String)saleReturnMap.get("tran_id");
				tranDate  = getCurrdateAppFormat();

				writeLog(filePtr,"   Tax Calculation parameter    ",true);
				writeLog(filePtr,"___________________________________",true);
				writeLog(filePtr,"tranId::: "+tranId,true);
				writeLog(filePtr,"line::: "+line,true);
				writeLog(filePtr,"tranDate::: "+tranDate.toString(),true);
				writeLog(filePtr,"freeQty::: "+freeQty,true);
				writeLog(filePtr,"currCode::: "+currCode,true);
				writeLog(filePtr,"siteCode::: "+siteCode,true);
				writeLog(filePtr,"taxClass::: "+taxClass,true);
				writeLog(filePtr,"taxChap::: "+taxChap,true);
				writeLog(filePtr,"taxEnv::: "+taxEnv,true);
				writeLog(filePtr,"rateClg::: "+rateClg,true);
				writeLog(filePtr,"discount::: "+discount,true);
				writeLog(filePtr,"___________________________________",true);

				handleTax(tranId,line,tranDate,freeQty,rate,currCode,siteCode,line,taxClass,taxChap,taxEnv,rateClg,discount,conn);

				saleReturnDetMap.put("quantity",new Double(freeQty));
				saleReturnDetMap.put("loc_code",locCode);
				saleReturnDetMap.put("line_no",new Integer(line).toString());
				saleReturnDetMap.put("lot_sl",lotSl);
				saleReturnDetMap.put("lot_no",lotNo);
				saleReturnDetMap.put("unit",unit);
				saleReturnDetMap.put("sale_order",saleOrder);
				saleReturnDetMap.put("cost_rate",new Double(costRate));
				///INSERT INTO THE SRETURN AND SRETURN DETAIL
				insertSReturnDet(saleReturnDetMap,conn);
				totalAmtAdjusted = totalAmtAdjusted+total;


			}//end of if(lotQtyToBeAllocated > 0)
			catch(Exception ee)
			{
				errString = "EXCEPTION";
				writeLog(filePtr,ee,true);
			}
				return errString;
	}

//ALLOCATE STOCK FOR INVOICE ITEM
private String sorderAllocate(String saleOrder, String lineNo, String itemCode, double saleQty, String expLev,double convQtyStduom,Connection conn) throws ITMException
{

		allocateFlag=true;
		writeLog(filePtr,"Setting allocateFlag[true]",true);

		String siteCode =invallocTraceMap.get("site_code").toString();
		itemCode = 	invallocTraceMap.get("item_code").toString();
		String locCode = invallocTraceMap.get("loc_code").toString();
		String lotNo = 	invallocTraceMap.get("lot_no").toString();
		String lotSl = invallocTraceMap.get("lot_sl").toString();

		writeLog(filePtr,"siteCode:-"+siteCode+"[sorderAllocate] ",true);
		writeLog(filePtr,"item_code:-"+itemCode+"[sorderAllocate] ",true);
		writeLog(filePtr,"locCode:-"+locCode+"[sorderAllocate] ",true);
		writeLog(filePtr,"lotNo:-"+lotNo+"[sorderAllocate] ",true);
		writeLog(filePtr,"lotSl:-"+lotSl+"[sorderAllocate] ",true);
		writeLog(filePtr,"saleOrder:-"+saleOrder+"[sorderAllocate] ",true);
		writeLog(filePtr,"lineNo:-"+lineNo+"[sorderAllocate] ",true);
		writeLog(filePtr,"expLev:-"+expLev+"[sorderAllocate] ",true);
		writeLog(filePtr,"convQtyStduom:-"+convQtyStduom+"[sorderAllocate] ",true);

		String sorditemSql="";
		String itemCodeOrd = "" ;
		String unitStd = "" ;
		String itemShDescr = "";
		String locDescr = "";
		String unit = "";
		String grade = "";
		String siteCodeMfg = "";
		String itemRef = "";
		String status = "";
		java.sql.Timestamp calExpDate = null;
		java.sql.Timestamp expDate = new java.sql.Timestamp(System.currentTimeMillis());
		java.sql.Timestamp mfgDate = new java.sql.Timestamp(System.currentTimeMillis());
		java.sql.Timestamp dateAlloc= new java.sql.Timestamp(System.currentTimeMillis());

		String updateSorditem = null;
		String insertSql = null;
		String updateSordalloc = null;
		String updateSql= null;
		String flag = null;
		double stockQuantity = 0;
		double qtyAvailAlloc = 0;
		double qtyDesp =0;
		double quantity =0;
		double quantityStduom = 0 ;
		double pendingQuantity = 0;
		int intCnt=0;
		int updCnt=0;
		String errString = "";
		String error = "";
		String errCode = "";
		PreparedStatement pstmtStockInsertSordAlloc = null;
		Statement st = null;
		ResultSet rs = null ;
		ResultSet rsSItem = null;
		String sexpDate = null;
		String smfgDate = null;
		try
		{
				//************************
				invallocTraceMap.put("ref_id",saleOrder);
				invallocTraceMap.put("ref_line",lineNo);
				invallocTraceMap.put("alloc_qty",new Double(saleQty));
	    		invallocTraceMap.put("chg_user",chgUser);
				invallocTraceMap.put("chg_term",chgTerm);
				invallocTraceMap.put("chg_win","W_SORDALLOC");
				invallocTraceMap.put("ref_ser","S-ALC");

				sexpDate = invallocTraceMap.get("exp_date").toString();
				smfgDate = invallocTraceMap.get("mfg_date").toString();
				if (sexpDate != null && sexpDate.trim().length() > 0)
				{
					expDate = java.sql.Timestamp.valueOf(sexpDate);
				}
				else
				{
					expDate = dateAlloc;
				}

				if (smfgDate != null && smfgDate.trim().length() > 0)
				{
					mfgDate = java.sql.Timestamp.valueOf(smfgDate);
				}
				else
				{
					mfgDate = dateAlloc;
				}

				errString =InvAllocTraceObj.updateInvallocTrace(invallocTraceMap,conn);
				if (errString != null && errString.trim().length() > 0)
				{
					System.out.println("errString :::"+ errString );
					return errString;
				}
				if (!saleOrderArr.contains(saleOrder.trim()))
				{
					saleOrderArr.add(saleOrderArr.size() , saleOrder.trim());
					writeLog(filePtr,"Adding SaleOrder [sorderAllocate]:::" + saleOrder.trim() ,true);
				}
				sorditemSql =" SELECT ITEM_CODE,ITEM_CODE__ORD,UNIT, QTY_DESP, ITEM_REF,"
							+" QUANTITY - QTY_DESP PENDING_QUANTITY "
							+" FROM SORDITEM WHERE SALE_ORDER = '" + saleOrder + "' "
							+" AND LINE_NO = '" + lineNo + "' "
							+" AND EXP_LEV = '" + expLev + "' ";
				System.out.println("sorditemSql:::"+sorditemSql);
				writeLog(filePtr,"sorditemSql--->"+sorditemSql,true);
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
				}
				rsSItem.close();
				updateSorditem ="UPDATE SORDITEM  SET QTY_ALLOC = CASE WHEN QTY_ALLOC IS NULL THEN 0 ELSE QTY_ALLOC END  +   " + new Double(saleQty).toString()
							   +" WHERE SALE_ORDER = '" + saleOrder + "' "
							   +" AND LINE_NO = '" + lineNo + "' "
							   +" AND EXP_LEV = '" + expLev + "' ";
				writeLog(filePtr,"updateSorditem--->"+updateSorditem,true);
				System.out.println("Updating SordItem in sorder Allocate .SaleOrder("+saleOrder+")LineNo("+lineNo+")Qty ("+saleQty);
				System.out.println("updateSql------->"+updateSorditem);
				st.executeUpdate(updateSorditem);
				System.out.println("UPDATE  SUCCESS FOR SORDITEM....>>>>>>>>");
				sorditemSql = "SELECT COUNT(1) FROM SORDALLOC "
							+ " WHERE SALE_ORDER = '" + saleOrder + "' "
							+ " AND LINE_NO = '" + lineNo + "' "
							+ " AND EXP_LEV = '" + expLev + "' "
							+ " AND ITEM_CODE__ORD = '" + itemCodeOrd + "' "
							+ " AND ITEM_CODE = '" + itemCode + "' "
							+ " AND LOT_NO = '" + lotNo + "' "
							+ " AND LOT_SL = '" + lotSl + "' "
							+ " AND LOC_CODE = '" + locCode + "' " ;
				writeLog(filePtr,"sorditemSql--->"+sorditemSql,true);
				rsSItem =  st.executeQuery(sorditemSql);
				int count = 0 ;
				if (rsSItem.next())
				{
					count = rsSItem.getInt(1);
				}
				if (count > 0 )
				{
					updateSql = "UPDATE SORDALLOC SET QTY_ALLOC =  QTY_ALLOC + " + new Double(saleQty).toString()
							  + " WHERE SALE_ORDER = '" + saleOrder + " ' "
							  + " AND LINE_NO = '" + lineNo + "' "
							  + " AND EXP_LEV = '" + expLev + "' "
							  + " AND ITEM_CODE__ORD = '" + itemCodeOrd + "' "
							  + " AND ITEM_CODE = '" + itemCode + "' "
							  + " AND LOT_NO = '" + lotNo + "' "
							  + " AND LOT_SL = '" + lotSl + "' "
							  + " AND LOC_CODE = '" + locCode + "' " ;

					writeLog(filePtr,"sorditemSql--->"+sorditemSql,true);
					System.out.println("updateSql:::>>>>"+ updateSql);
					st.executeUpdate(updateSql);
					System.out.println("UPDATE  SUCCESS FOR SORDALLOC....");
				}
				else

				{
					insertSql ="INSERT INTO SORDALLOC (SALE_ORDER,LINE_NO,EXP_LEV,ITEM_CODE__ORD,SITE_CODE ,"
							  +"ITEM_CODE,QUANTITY ,LOT_NO, LOT_SL, LOC_CODE, UNIT, QTY_ALLOC,"
							  +"ITEM_REF, DATE_ALLOC, STATUS,ITEM_GRADE, EXP_DATE, ALLOC_MODE, "
							  +" CONV__QTY_STDUOM, UNIT__STD, QUANTITY__STDUOM, "
							  +"MFG_DATE, SITE_CODE__MFG ) "
							  +"VALUES ( ?, ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,"
							  +" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ;
					writeLog(filePtr,"Insert Sql--->"+insertSql,true);
					pstmtStockInsertSordAlloc = conn.prepareStatement(insertSql);
					System.out.println("Insert Sql :"+ insertSql );
					//SALE_ORDER
					pstmtStockInsertSordAlloc.setString(1, saleOrder);
					writeLog(filePtr,"saleOrder------->["+saleOrder+"]",true);
					System.out.println("saleOrder------->"+saleOrder);
					//LINE_NO
					pstmtStockInsertSordAlloc.setString(2, lineNo);
					writeLog(filePtr,"lineNo------->["+lineNo+"]",true);
					System.out.println("lineNo------->"+lineNo);
					//EXP_LEV
					pstmtStockInsertSordAlloc.setString(3, expLev);
					writeLog(filePtr,"EXP_LEV------->["+expLev+"]",true);
					System.out.println("EXP_LEV------->"+expLev);
					//ITEM_CODE__ORD
					pstmtStockInsertSordAlloc.setString(4, itemCodeOrd);
					System.out.println("itemCode------->"+itemCodeOrd);
					writeLog(filePtr,"itemCode------->"+itemCodeOrd,true);
					//SITE_CODE
					pstmtStockInsertSordAlloc.setString(5, siteCode);
					writeLog(filePtr,"siteCode------->"+siteCode,true);
					System.out.println("siteCode------->"+siteCode);
					//ITEM_CODE
					pstmtStockInsertSordAlloc.setString(6, itemCode);
					writeLog(filePtr,"itemCode------->"+itemCode,true);
					System.out.println("itemCode------->"+itemCode);
					//QUANTITY***** set pending qty
					pstmtStockInsertSordAlloc.setDouble(7, pendingQuantity);
					writeLog(filePtr,"pendingQuantity------->"+pendingQuantity,true);
					System.out.println("pendingQuantity------->"+pendingQuantity);
					//LOT_NO
					pstmtStockInsertSordAlloc.setString(8, lotNo);
					writeLog(filePtr,"lotNo------->"+lotNo,true);
					System.out.println("lotNo------->"+lotNo);
					//LOT_SL
					pstmtStockInsertSordAlloc.setString(9, lotSl);
					writeLog(filePtr,"lotSl------->"+lotSl,true);
					System.out.println("lotSl------->"+lotSl);
					//LOC_CODE
					pstmtStockInsertSordAlloc.setString(10, locCode);
					writeLog(filePtr,"locCode------->"+locCode,true);
					System.out.println("locCode------->"+locCode);
					//UNIT
					pstmtStockInsertSordAlloc.setString(11, unitStd);
					writeLog(filePtr,"unit------->"+unitStd,true);
					System.out.println("unit------->"+unitStd);
					//QTY_ALLOC
					pstmtStockInsertSordAlloc.setDouble(12, saleQty);
					writeLog(filePtr,"saleQty------->"+saleQty,true);
					System.out.println("saleQty------->"+saleQty);
					//ITEM_REF
					pstmtStockInsertSordAlloc.setString(13,itemRef);
					writeLog(filePtr,"itemRef------->"+itemRef,true);
					System.out.println("itemRef------->"+itemRef);
					//DATE_ALLOC
					pstmtStockInsertSordAlloc.setTimestamp(14,dateAlloc);
					System.out.println("dateAlloc------->"+dateAlloc);
					//STATUS
					pstmtStockInsertSordAlloc.setString(15,"P");
					System.out.println("status------->P");
					//ITEM_GRADE
					pstmtStockInsertSordAlloc.setString(16,grade);
					writeLog(filePtr,"grade------->"+grade,true);
					System.out.println("grade------->"+grade);
					//EXP_DATE
					pstmtStockInsertSordAlloc.setTimestamp(17,expDate);
					System.out.println("expDate------->"+ expDate);
					//ALLOC_MODE
					pstmtStockInsertSordAlloc.setString(18,"M");
					//CONV__QTY_STDUOM
					pstmtStockInsertSordAlloc.setDouble(19,convQtyStduom);
					writeLog(filePtr,"convQtyStduom------->"+convQtyStduom,true);
					System.out.println("convQtyStduom------->"+convQtyStduom);
					//UNIT__STD
					pstmtStockInsertSordAlloc.setString(20,unitStd);
					writeLog(filePtr,"unitStd------->"+unitStd,true);
					System.out.println("unitStd------->"+unitStd);
					quantityStduom = convQtyStduom * saleQty ;
					//QUANTITY__STDUOM
					pstmtStockInsertSordAlloc.setDouble(21,quantityStduom);
					System.out.println("quantityStduom------->"+quantityStduom);
					//MFG_DATE
					pstmtStockInsertSordAlloc.setTimestamp(22,mfgDate);
					System.out.println("mfgDate------->"+mfgDate);
					//SITE_CODE__MFG
					pstmtStockInsertSordAlloc.setString(23,siteCodeMfg);
					writeLog(filePtr,"siteCodeMfg------->"+siteCodeMfg,true);
					System.out.println("siteCodeMfg------->"+siteCodeMfg);
					//QTY_DESP
					intCnt = pstmtStockInsertSordAlloc.executeUpdate();
					System.out.println("insertion  success ...............>>>>>>>>");
					pstmtStockInsertSordAlloc.close();
				}//end else
				st.close();
				writeLog(filePtr,"Sorder update sql..." + "UPDATE SORDER SET ALLOC_FLAG = 'Y' WHERE SALE_ORDER = '" + saleOrder.trim() ,true);
				st = conn.createStatement();
				st.executeUpdate("UPDATE SORDER SET ALLOC_FLAG = 'Y' WHERE SALE_ORDER = '" + saleOrder.trim() + "' ");
				st.close();
		}
		catch(SQLException se)
		{
			writeLog(filePtr,se,true);
			System.out.println("SQLException :" + se);
			se.printStackTrace();
			errString = "EXCEPTION";
			return errString;
		}
		catch(Exception e)
		{
			System.out.println("Exception :" + e);
			errString = "EXCEPTION";
			e.printStackTrace();

			return errString ;
		}
		finally
		{
			try
			{
				if(conn != null)
				{
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

				}
			}
			catch(Exception e)
			{
				errString = "EXCEPTION";
				e.printStackTrace();
				return errString ;
			}
			return errString;
		}

	}//end of sorder

//THIS FUNCTION GET UNIT FROM ITEM MASTAR FOR A ITEM
private String getUnitFrItem(String itemCode)
	{
		String unit = "";
		String sql = "";
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			sql = "SELECT  UNIT FROM ITEM  WHERE  item_code = '"+itemCode.trim()+"'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				unit = rs.getString(1);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception...[getUnit] "+sql+e.getMessage());
			e.printStackTrace();
		}
		return unit ;
	}//getPriceListType


//THIS FUNCTION RETURN DATE IN APPLICATION FORMAT
private String getCurrdateAppFormat()
{
	String currAppdate ="";
	java.sql.Timestamp currDate = null;
	try
	{
			Object date = null;
			currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
			System.out.println(genericUtility.getDBDateFormat());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
	}
	catch(Exception e)
	{

		writeLog(filePtr,e,true);
		System.out.println("Exception in :::calcFrsBal"+e.getMessage());
	}
	return (currAppdate);
}
//insert in to sreturnInv
private String insertSRetInv(ArrayList sretInvList,String tran_id,Connection conn)
{
	writeLog(filePtr,"Insert in sale return invoice****["+tran_id+"]",true);
	SretInvBean sretInvObj= null;
	String insert  = "";
	PreparedStatement pstmt = null;
	String tranId=tran_id.substring(0,tran_id.length()-1);
	//String tranId=tran_id;
	String errStr="";
  	String refSer = null;
    String refNo = null;
    double adjAmt=0;
    double refBalAmt=0;
    String lineNo=null;
	try
	{
		insert  = "INSERT INTO SRETURN_INV (TRAN_ID,LINE_NO,REF_SER,REF_NO,ADJ_AMT,REF_BAL_AMT)"
		+" VALUES(?,?,?,?,?,?)";
		pstmt = conn.prepareStatement(insert);
		for(int i=0;i<sretInvList.size();i++)
		{
			lineNo = new Integer(i+1).toString();
			lineNo = "   "+lineNo;
			lineNo=lineNo.substring(lineNo.length()-3);

			sretInvObj = (SretInvBean)sretInvList.get(i);
			refSer = sretInvObj.getRefSer();
    		refNo = sretInvObj.getRefNo();
    		adjAmt=sretInvObj.getAdjAmt();
   			refBalAmt=sretInvObj.getRefBalAmt();

   			writeLog(filePtr,"tranId:-["+tranId+"]",true);
   			writeLog(filePtr,"lineNo:-["+lineNo+"]",true);
   			writeLog(filePtr,"refSer:-["+refSer+"]",true);
   			writeLog(filePtr,"refNo:-["+refNo+"]",true);
   			writeLog(filePtr,"adjAmt:-["+adjAmt+"]",true);
   			writeLog(filePtr,"refBalAmt:-["+refBalAmt+"]",true);

			pstmt.setString(1,tranId);
			pstmt.setString(2,lineNo);
			pstmt.setString(3,refSer);
			pstmt.setString(4,refNo);
			pstmt.setDouble(5,adjAmt);
			pstmt.setDouble(6,refBalAmt);
			pstmt.addBatch();
		}
		pstmt.executeBatch();
	}
	catch(Exception e)
	{
		errStr ="EXCEPTION";
	//	e.printStackTrace();
		writeLog(filePtr,"Exception in Inserting insertSRetInv"+e,true);
	}
	return errStr;
}
private int getCustomerInformation(ArrayList customerList,String custCode)
{
		int ctr = 0;
		int retVal = -1;
		CustomerListBean customerElement;
		for (ctr = 0;ctr < customerList.size();ctr++)
		{
			customerElement = (CustomerListBean)customerList.get(ctr);
			if (customerElement.getCustCode().trim().equalsIgnoreCase(custCode.trim()))
			{
				retVal = ctr;
				break;
			}
		}
		return retVal;
	}



}//END OF EJB