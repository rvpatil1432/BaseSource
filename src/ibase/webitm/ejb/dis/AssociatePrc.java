
/********************************************************
	Title : AssociatePrcEJB
	Date  : 4/13/2009	

********************************************************/
package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3
import ibase.utility.E12GenericUtility;

//public class ConsumeIssuePos extends ValidatorEJB implements SessionBean //commented for ejb3
@Stateless // added for ejb3
public class AssociatePrc extends ProcessEJB implements AssociatePrcLocal , AssociatePrcRemote //SessionBean
{
	Connection conn = null;
	String errorString = null;
	NodeList parentNodeList = null;
	NodeList enqNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node enqNode = null;
	Node childNode = null;
	int ctr = 0,cnt = 0;
	String childNodeName = null;
	String errCode = "";
	int currentFormNo=0;
	int childNodeListLength;
	String selectQry = "";
	String selectItem = "";	
	ResultSet rs = null;
	PreparedStatement psmt = null;
	Statement stmt = null;
	Statement dtl2Stmt = null;
	ConnDriver connDriver = null;
	ResultSet dtlRs = null;
	String working = "";
	StringBuffer xmlString = null;
	int date = 0,month= 0,year= 0,hour = 0,min = 0,sec = 0;
	Date d = null;	
	//BasePreparedStatement oraPsmt = null;
	Statement st = null;
	SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd");
	int upd = 0;
	String returnString = "";
	int noOfDetail = 1 ;
	
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Create Method Called....");
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
	public String process() throws RemoteException,ITMException
	{
		return "";
	}	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{	
		System.out.println("Xform :getData() function called");
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;	
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				//System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				//System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 				
			}

			rtrStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :Xform :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();			
		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String process(Document dom1, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException
	{		

		Connection conn = null,connCP = null ;
		PreparedStatement pstmt = null, pstmtCP = null, pstmtCP1 = null,pstmtInser = null,Dtlpstmt = null ;
		PreparedStatement pstmt1 = null;
		//Statement stmt = null;
		ResultSet rs = null, rsCP = null, rsCP1 = null;
		ResultSet rs1 = null,Dtlrs = null ;
		ResultSet rsScd = null;

		String sql = "", sql1 = "", sql2 = "",sqlInser;
	   	String mopt ="", tranId ="", custCode ="";
		String fromDate ="", tranDate ="", toDate = "",detCnt = "0",dateFlag= "";
		String siteCodeCurr ="", siteCode ="", itemCode = "", itemDescr = "", unit = "";
		String locType ="", source ="", siteCodeSupp ="", itemSer ="", schemeCd ="", stateCd ="";
		String applyCustList = "", applyCust = "", noApplyCustList ="", noApplyCust ="", applicableOrdTypes ="";
		String prevscheme ="", schemeCode = "", slabOn ="", mnature ="", orderType ="", token ="";
		int mcnt=0,count =0,mchk = 0; 
		double orderQty = 0, clStk=0, qtyStk =0;
		double totQty =0, batchQty =0, freeQty =0, perc =0, mainStk =0, chkStk =0, avaiStk =0, balStk =0;
		int schcnt=0;
		boolean proceed ;
		String replRate = "";
		String loginCode = "";
		String errString = "";
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		StringBuffer valueXmlString = new StringBuffer();
		String loginSiteCode = "",chgUser = "",chgTerm = "",userId = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		siteCodeCurr = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		//loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
		chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");	
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
		String xmlValues = "",tranIdG = "",sqlmax="",remarks = "";
		int maxLineNo =1,cnt = 1;
		String returnErrorString = "";
		Timestamp tranDateTimestmp = null,tranDateTimestmpApp = null ;		
		try
		{	
			ConnDriver connDriver = new ConnDriver();			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//connCP = connDriver.getConnectDB("DriverCP");
			connCP = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			
			connCP.setAutoCommit(false);
			connDriver = null;
			tranDate = genericUtility.getColumnValue("tran_date",dom1);
			System.out.println("tranDate......"+tranDate);
			tranDateTimestmp = java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
			System.out.println("tranDateTimestmp......"+tranDateTimestmp);
			//tranDateTimestmpApp = java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
			//System.out.println("tranDateTimestmpApp......"+tranDateTimestmpApp);
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>"+siteCodeCurr.trim()+"</site_code>" ;
			xmlValues = xmlValues + "<tran_date>"+tranDate.trim()+"</tran_date>" ;
			xmlValues = xmlValues + "</Detail1></Root>";	
			tranIdG = generateTranId("w_associate",xmlValues,conn);//function to generate NEW transaction id			
			if(tranIdG == null || tranIdG.trim().length() ==0)
			{
				errCode = "VTTRANID";
				System.out.println("errcode......"+errCode);
				errString = itmDBAccess.getErrorString("","VTTRANID",userId,"",conn);
				return errString;
			}
			//tranDate = genericUtility.getColumnValue("tan_date",dom1); // added  on 4/13/2009
			//tranId = genericUtility.getColumnValue("tran_id",dom1);
			dateFlag = genericUtility.getColumnValue( "date_flag", dom1 ); // added by pankaj on 18.01.09
			custCode = genericUtility.getColumnValue("cust_code",dom1);
			custCode = custCode == null ?"":custCode.trim();			
			fromDate = genericUtility.getColumnValue("from_date",dom1);
			toDate = genericUtility.getColumnValue("to_date",dom1);
			remarks = genericUtility.getColumnValue("remarks",dom1);
			remarks = remarks == null ? "":remarks ;
			if(custCode == null || custCode.trim().length()==0)
			{
				return itmDBAccess.getErrorString("","VTPROCUN","","",conn);				
			}
			if(custCode!=null &&  custCode.trim().length()> 0)
			{
				sql="select count(*) from customer where cust_code = ?  and (channel_partner = 'N'or channel_partner is null) ";
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,custCode);
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
					return itmDBAccess.getErrorString("","VTPROCUS","","",conn);		
				
				}
			}
			java.sql.Timestamp  fromDateTms = Timestamp.valueOf(genericUtility.getValidDateString(fromDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");										
			java.sql.Timestamp toDateTms = Timestamp.valueOf(genericUtility.getValidDateString(toDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			if(toDateTms.compareTo (fromDateTms) <=  0 )
			{
				return itmDBAccess.getErrorString("","VTFRTODATE","","",conn);
			
			}
			//connDriver = new ConnDriver();
			//connCP = connDriver.getConnectDB("DriverCP");
			//connDriver = null;
			
			//stmt = conn.createStatement();//Removed Resultset Sensitive Type // To be Updated in Server - Jiten - 
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); 
			if(detCnt.equals("0"))
			{
				orderType = "F";//Added by Jiten 09/10/06 as it is in PB
				// orginal disparm INCL_FREITM_INASOSTK
				//sql= "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='SOREPL_EXCLUDE_FREE_ITEMS'"; //variable anme changed by pankaj on 18/02/09
				//System.out.println("DISPARM:sql:"+sql);
				
				//rs = stmt.executeQuery(sql);
				//if(rs.next())
				//{
				//	mopt = rs.getString(1);
				//}
				//System.out.println("mopt:"+mopt+":");
				DistCommon disCommon = new DistCommon();
				mopt = disCommon.getDisparams("999999","SOREPL_EXCLUDE_FREE_ITEMS", conn);
				disCommon = null;
				if(!"C".equalsIgnoreCase(mopt))
				{
					
					//siteCodeCurr =  genericUtility.getColumnValue("site_code",dom1);
					//Added by Jiten 09/10/06 as added in PB
					//replRate = genericUtility.getColumnValue("repl_rate",dom1); // comment on 4/13/2009
					//orderType = genericUtility.getColumnValue("order_type",dom1); // comment oon  4/13/2009 
					// changes by manazir on 4/13/2009 
					sql="SELECT ORDER_TYPE FROM CUSTOMER WHERE CUST_CODE = ?";					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
					  orderType = rs.getString("ORDER_TYPE")==null ? "F":rs.getString("ORDER_TYPE");
					}					
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql="SELECT  REPL_RATE FROM  sordertype where order_type = ? ";					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,orderType);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
					  replRate = rs.getString("REPL_RATE")==null ? "0":rs.getString("REPL_RATE");
					}					
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					// end of code on 4/13/2009 				
					//System.out.println("tranId:"+tranId+":custCode:"+custCode+":tranDate:"+tranDate+":fromDate"+fromDate+":toDate:"+toDate+":siteCodeCurr:"+siteCodeCurr);
					//System.out.println("Repl rate :"+replRate+" orderType :"+orderType);
					sql="SELECT SITE_CODE FROM CUSTOMER WHERE CUST_CODE = ?";
					//System.out.println("CUSTOMER:sql:"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
					  siteCode = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//System.out.println("siteCode:"+siteCode+":");
					//Added by Jiten 09/10/06 As code changed in PB
					if(orderType == null || orderType.trim().length() == 0){
						orderType = "F";
					}
					//End
					
					if(mopt != null && mopt.trim().length()!= 0)
					{
						if(mopt.equalsIgnoreCase("I")) // changed by pankaj on 18/02/09 to Include free items
						{
							mnature = "%";
						}
						if(mopt.equalsIgnoreCase("E")) //changed by pankaj on 18/02/09 to exclude free items
						{	
							//mnature = "C"; // //Piyush Friday, May 05, 2006
							mnature = "C%";
						}
					}//mopt != null
					//System.out.println("mnature:"+mnature+":");
					//Added by Jiten 09/10/06 as added in PB & below sql changed
					if(replRate != null && replRate.trim().equalsIgnoreCase("1"))
					{
						orderType = orderType.trim()+"%"; 
					}
					else
					{
						orderType = "%";
					}
					// header and detial insert in custstock and custstock_det value on 4/14/2009
					sql = "	INSERT INTO cust_stock(date_flag,tran_date, chg_date, "
					  +"	order_type,chg_term,chg_user, "
					  +"	repl_rate,status,from_date, "
					  +"	tran_id,remarks,cust_code, "
					  +"	site_code,to_date,confirmed) "
					  +"	VALUES (?,?,?, " 
					  +" ?,?,?, " 
					  +" ?,?,?, "
					  +" ?,?,?, "
					  +" ?,?,?) " ;
					pstmt	= conn.prepareStatement(sql);
					pstmt.setString(1,dateFlag);

					pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()))); // chg_date
					if(orderType.equalsIgnoreCase("%")||orderType.length()==0 )
					{
						pstmt.setString(4,"F");
					}
					else
					{
						pstmt.setString(4,orderType);
					}
					pstmt.setString(5,chgTerm); // chg_term
					pstmt.setString(6,chgUser); // chg_user
					pstmt.setString(7,replRate);
					pstmt.setString(8,"N"); // status
					pstmt.setTimestamp(9,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()))); // from date
					pstmt.setString(10,tranIdG); // tran_id
					pstmt.setString(11,remarks); // remarks 
					pstmt.setString(12,custCode); // cust_code
					pstmt.setString(13,siteCodeCurr); // site_code
					pstmt.setTimestamp(14,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));// to date
					pstmt.setString(15,"N");
					pstmt.executeUpdate();
					pstmt.close();
					pstmt=null;
					sqlInser = "	INSERT INTO CUST_STOCK_DET(chg_date,chg_term,chg_user, "
						+"	loc_type,tran_id,line_no , "
						+"	rate,item_ser,cl_stock, "
						+"	unit,item_code)	" 
						+"	VALUES (?,?,?, "
						+"	?,?,?,	"
						+"	?,?,?,	"
						+"	? , ? ) " ;
					pstmtInser	= conn.prepareStatement(sqlInser);

					// end of code  on 4/14/2009 
					/*sql =" SELECT B.ITEM_CODE ,SUM(B.QUANTITY - B.QTY_DESP),C.DESCR ,C.UNIT ,"+
						" C.LOC_TYPE FROM SORDITEM B , ITEM C ,SORDER A"+
						" WHERE A.SALE_ORDER = B.SALE_ORDER"+ 
						" AND A.ORDER_DATE >= ? "+
						" AND A.ORDER_DATE <= ? "+
						" AND  (A.STATUS IS NULL OR A.STATUS = 'P')"+
						" AND  B.SITE_CODE = '"+siteCode+"'"+
						" AND B.LINE_TYPE  <> 'B' "+
						" AND  (B.QUANTITY - QTY_DESP) > 0 "+
						" AND   B.NATURE LIKE '"+mnature+"' "+
						" AND 	B.ITEM_CODE   = C.ITEM_CODE"+
						" GROUP BY B.ITEM_CODE, C.DESCR, C.UNIT, C.LOC_TYPE ORDER BY DESCR"  ;*/
					// CommonConstants.setIBASEHOME();
					// if(CommonConstants.DB_NAME.equalsIgnoreCase("db2")){
						// sql = "SELECT ITEM_CODE, SUM(BAL_QTY) , DESCR , UNIT , "+ //LOC_TYPE , AC_RATE "+ // commented by pankaj on 18/01/09
						// "case when c.loc_type__parent is null then c.loc_type else c.loc_type__parent end loc_type," + // changed by pankaj as code changed in pb on 18/01/09
						// "FROM (SELECT B.ITEM_CODE ITEM_CODE, (B.QUANTITY - B.QTY_DESP) BAL_QTY , C.DESCR DESCR ,"+ 
						// "C.UNIT UNIT , C.LOC_TYPE LOC_TYPE,(CASE WHEN ? = '1' THEN D.RATE ELSE 0 END) AC_RATE "+
						// "FROM SORDITEM_CP B ,ITEM C ,SORDER A ,SORDDET D WHERE A.SALE_ORDER  = B.SALE_ORDER "+  // table name changed  to sorditem_cp by pankaj on 18/02/9
						// "AND B.ITEM_CODE = C.ITEM_CODE "+
						// "AND B.SALE_ORDER = D.SALE_ORDER "+
						// "AND B.LINE_NO = D.LINE_NO "+	  
						// "AND A.ORDER_DATE >= ? "+ 
						// "AND A.ORDER_DATE <= ? "+
						// "AND (A.STATUS IS NULL OR A.STATUS = 'P') "+
						// "AND B.SITE_CODE   = ? "+
						// "AND B.LINE_TYPE  <> 'B' "+
						// "AND (B.QUANTITY - QTY_DESP) > 0 "+
						// "AND B.NATURE LIKE ? "+
						// "AND (CASE WHEN A.ORDER_TYPE IS NULL THEN ' ' ELSE A.ORDER_TYPE END ) LIKE ? ) AS DD "+
						// "GROUP BY ITEM_CODE, DESCR, UNIT, LOC_TYPE , AC_RATE ORDER BY DESCR";
					// }else if(CommonConstants.DB_NAME.equalsIgnoreCase("oracle")){
						// sql = "SELECT ITEM_CODE, SUM(BAL_QTY) , DESCR , UNIT , "+ //LOC_TYPE , AC_RATE "+ // commented by pankaj on 18/01/09
						// "case when c.loc_type__parent is null then c.loc_type else c.loc_type__parent end loc_type," + // changed by pankaj as code changed in pb on 18/01/09
						// "FROM (SELECT B.ITEM_CODE ITEM_CODE, (B.QUANTITY - B.QTY_DESP) BAL_QTY , C.DESCR DESCR ,"+ 
						// "C.UNIT UNIT , C.LOC_TYPE LOC_TYPE,(CASE WHEN ? = '1' THEN D.RATE ELSE 0 END) AC_RATE "+
						// "FROM SORDITEM_CP B ,ITEM C ,SORDER A ,SORDDET D WHERE A.SALE_ORDER  = B.SALE_ORDER "+ // table name changed  to sorditem_cp by pankaj on 18/02/9
						// "AND B.ITEM_CODE = C.ITEM_CODE "+
						// "AND B.SALE_ORDER = D.SALE_ORDER "+
						// "AND B.LINE_NO = D.LINE_NO "+	  
						// "AND A.ORDER_DATE >= ? "+ 
						// "AND A.ORDER_DATE <= ? "+
						// "AND (A.STATUS IS NULL OR A.STATUS = 'P') "+
						// "AND B.SITE_CODE   = ? "+
						// "AND B.LINE_TYPE  <> 'B' "+
						// "AND (B.QUANTITY - QTY_DESP) > 0 "+
						// "AND B.NATURE LIKE ? "+
						// "AND (CASE WHEN A.ORDER_TYPE IS NULL THEN ' ' ELSE A.ORDER_TYPE END ) LIKE ? ) "+
						// "GROUP BY ITEM_CODE, DESCR, UNIT, LOC_TYPE , AC_RATE ORDER BY DESCR";
						////******* 15/04/09
						// query changed by pankaj 0n 18.01.09
						if( orderType == null || orderType.trim().length() == 0 )
						{
							 orderType = "F";
						}
						sql = "select 	item_code, sum(bal_qty), descr, unit, loc_type, ac_rate from ( ";
						sql = sql + " select b.item_code item_code, ";
						sql = sql + "(b.quantity - b.qty_desp) bal_qty, c.descr descr, c.unit unit, " ;
						sql = sql + " case when c.loc_type__parent is null then c.loc_type else c.loc_type__parent end loc_type, ";
						if((replRate != null && replRate.trim().length() > 0) && replRate.equals("1"))
						{
							sql = sql + " b.rate ac_rate ";
						}
						else
						{
							sql = sql + " (0) ac_rate ";
						}
						//sql = sql + "(case when '" + ls_repl_rate + "' = '1' then d.rate else 0 end) ac_rate ";
						// 20/03/09 manoharan seperate connection instead of synonym
						//sql = sql + " from sorditem_cp b, item c, sorder_cp a,	sorddet_cp d where ";
  						// 27/03/09 manoharan sorddet removed and function used
						//sql = sql + " from sorditem b, item c, sorder a,	sorddet d where ";
						sql = sql + " from sorditem b, item c ";
						sql = sql + " where b.item_code   = c.item_code ";
						if((dateFlag != null && dateFlag.trim().length() > 0) && dateFlag.equalsIgnoreCase("O") )
						{
							sql = sql + " and 	b.order_date >= ?"; 
							sql = sql + " and 	b.order_date <= ?"; 
						}
						else if((dateFlag != null && dateFlag.trim().length() > 0) && dateFlag.equalsIgnoreCase("D") )
						{
							sql = sql + " and 	b.dsp_date >= ?"; 
							sql = sql + " and 	b.dsp_date <= ?"; 
						}
						sql = sql + " and 	b.site_code   = ? ";
						sql = sql + " and  (case when b.order_type is null then ' ' else b.order_type end ) like '" + orderType + "'"; 
						sql = sql + " and  (b.status is null or b.status in ('P','D')) ";
						sql = sql + " and   case when b.nature is null then 'C' else b.nature end like ? ";
						sql = sql + " and 	b.line_type  <> 'B' ";
						sql = sql + " and  (b.quantity - qty_desp) > 0 ";
						sql = sql + " ) ";
						sql = sql + " group by descr, item_code,  unit, loc_type , ac_rate ";
						sql = sql + " order by descr" ;
					}
					//End change by pankaj
					// 20/03/09 manoharan seperate connection instead of synonym
					//pstmt = conn.prepareStatement(sql);
					pstmtCP = connCP.prepareStatement(sql);
					// end 20/03/09 manoharan seperate connection instead of synonym
					//System.out.println(":sql:"+sql);
					//System.out.println("Value form function:"+genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					//System.out.println("2. From date :"+java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					//System.out.println("3. To Date :"+java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					//System.out.println("1. replRate :"+replRate);
					//System.out.println("4. siteCode :"+siteCode);
					//System.out.println("5. mnature :"+mnature);
					//System.out.println("6. orderType :"+orderType);
					
					//pstmt.setString(1,replRate);//Added by Jiten 10/10/06 as sql changed in PB
					// 20/03/09 manoharan seperate connection instead of synonym
					//pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					//pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					
					if((dateFlag != null && dateFlag.trim().length() > 0) && (dateFlag.equalsIgnoreCase("D") || dateFlag.equalsIgnoreCase("O") ) )
					{
						pstmtCP.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmtCP.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmtCP.setString(3,siteCode);
						pstmtCP.setString(4,mnature);
					}
					else
					{
						pstmtCP.setString(1,siteCode);
						pstmtCP.setString(2,mnature);
					}
					////******* 15/04/09 end
					// end 20/03/09 manoharan seperate connection instead of synonym
					//pstmt.setString(4,siteCode);//Added by Jiten 09/10/06 as sql changed in PB
					//pstmt.setString(5,mnature);//Added by Jiten 09/10/06 as sql changed in PB
					//pstmt.setString(6,orderType);//Added by Jiten 09/10/06 as sql changed in PB   //  change by pankaj
					// 20/03/09 manoharan seperate connection instead of synonym
					//rs1 = pstmt.executeQuery();
					rsCP = pstmtCP.executeQuery();
					maxLineNo = 1;
					//while(rs1.next())
					while(rsCP.next())
					{
						//itemCode = rs1.getString(1);
						//orderQty = rs1.getDouble(2);
						//itemDescr = rs1.getString(3);
						//unit = rs1.getString(4);
						//locType = rs1.getString(5);
						itemCode = rsCP.getString(1);
						orderQty = rsCP.getDouble(2);
						itemDescr = rsCP.getString(3);
						unit = rsCP.getString(4);
						locType = rsCP.getString(5);
						//System.out.println("itemCode:"+itemCode+":orderQty:"+orderQty+":itemDescr:"+itemDescr+":unit:"+unit+":locType:"+locType+":"); 
						//if get_sqlcode() <> 0 then
						//if isnull(lc_order_qty) then lc_order_qty = 0
						sql=" SELECT SUM(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END )"+  
							" FROM STOCK A, INVSTAT  B "+ // table name changed to stock_cp by pankaj on 18/01/09, reverted to stock tableby manoharan 20/03/09
							" WHERE A.INV_STAT  = B.INV_STAT "+
							" AND A.ITEM_CODE = ? "	+
							" AND A.SITE_CODE = ? "	+
							" AND A.QUANTITY  > 0 "+
							" AND B.AVAILABLE = 'Y'"+
							" AND B.INV_STAT NOT IN (SELECT DISTINCT INV_STAT FROM INV_RESTR)";
						//System.out.println(":sql:"+sql);
						//rs = stmt.executeQuery(sql);
						pstmtCP1 = connCP.prepareStatement(sql);
						pstmtCP1.setString(1,itemCode);
						pstmtCP1.setString(2,siteCode);
						rsCP1 = pstmtCP1.executeQuery();
						if(rsCP1.next())
						{
							qtyStk = rsCP1.getDouble(1);
						}
						rsCP1.close();
						rsCP1 = null;
						pstmtCP1.close();
						pstmtCP1 = null;
						//System.out.println("qtyStk:"+qtyStk+":");
						//if isnull(lc_qty_stk) then lc_qty_stk = 0
						sql ="SELECT SUPP_SOUR,SITE_CODE__SUPP "+
							" FROM 	SITEITEM "+
							" WHERE SITE_CODE = ? " +
							" AND ITEM_CODE =? ";
						//System.out.println(":sql:"+sql);
						//rs = stmt.executeQuery(sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if (!rs.next())
						{
							source = "P";
							siteCodeSupp = siteCodeCurr;
						}
						else
						{
							source =rs.getString(1);	
							siteCodeSupp =rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//System.out.println("orderQty - qtyStk:"+(orderQty - qtyStk));
						//System.out.println("orderQty - qtyStk:"+((orderQty - qtyStk) > 0));
						//System.out.println("Condition siteCodeSupp :"+(siteCodeSupp!= null && siteCodeSupp.trim().equalsIgnoreCase(siteCodeCurr.trim())));
						//System.out.println("Condition source :"+(source != null && source.trim().equalsIgnoreCase("P")));
						//System.out.println("Condition:"+((orderQty - qtyStk > 0) && (source != null && source.trim().equalsIgnoreCase("P")) && (siteCodeSupp!= null && siteCodeSupp.trim().equalsIgnoreCase(siteCodeCurr.trim()))));
						if(((orderQty - qtyStk) > 0) && (source != null && source.trim().equalsIgnoreCase("P")) && (siteCodeSupp != null && siteCodeSupp.trim().equalsIgnoreCase(siteCodeCurr.trim())))
						{
							itemSer = "";
							 disCommon = new DistCommon();
							//itemSer = itmDBAccess.getItemSeries(itemCode, siteCodeCurr,tranDate,custCode,'C',null);
							itemSer = disCommon.getItemSer(itemCode, siteCodeCurr,tranDateTimestmp,custCode,"C",conn);
							disCommon = null;
							//System.out.println("FromFunction :itemSer:"+itemSer+":");
							sql="SELECT COUNT(*) FROM CUSTOMER_SERIES "+
								" WHERE  CUST_CODE = ? "+ 
								" AND ITEM_SER  = ? ";
							//System.out.println(":sql:"+sql);
							//rs = stmt.executeQuery(sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(2,itemSer);
							rs = pstmt.executeQuery();
							
							if(rs.next())
							{
							  mcnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//System.out.println("mcnt:"+mcnt+":");
							if(mcnt > 0)
							{
								clStk = orderQty - qtyStk;
								count = 1;
								schemeCd = "";
								sql ="SELECT STATE_CODE FROM CUSTOMER WHERE CUST_CODE = ? ";					
								//System.out.println(":sql:"+sql);
								//rs = stmt.executeQuery(sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									stateCd = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								//System.out.println("stateCd:"+stateCd+":");
								sql2 = " SELECT A.SCHEME_CODE FROM SCHEME_APPLICABILITY A,"+
										" SCHEME_APPLICABILITY_DET  B" +
										" WHERE A.SCHEME_CODE	= B.SCHEME_CODE" +
										" AND A.ITEM_CODE = ? "	+
										" AND A.APP_FROM <= ? "+
										" AND A.VALID_UPTO >= ?"+
										" AND (B.SITE_CODE = ? "+ 
										" OR B.STATE_CODE =  ? )";
								pstmt1 = conn.prepareStatement(sql2);
								//System.out.println(":sql2:"+sql2);
								pstmt1.setString(1,itemCode);
								pstmt1.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
								pstmt1.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
								pstmt1.setString(4,siteCodeCurr);
								pstmt1.setString(5,stateCd);
								rsScd = pstmt1.executeQuery();
								while(rsScd.next())
								{
									 schemeCd = rsScd.getString(1);
									 //System.out.println("schemeCd:"+schemeCd+":");
									 sql ="SELECT (CASE WHEN APPLY_CUST_LIST IS NULL THEN ' ' ELSE APPLY_CUST_LIST END),"+
											 "(CASE WHEN NOAPPLY_CUST_LIST IS NULL THEN ' ' ELSE NOAPPLY_CUST_LIST END),"+
											 "ORDER_TYPE FROM SCHEME_APPLICABILITY "+
											 "WHERE SCHEME_CODE =  ? ";
									 //System.out.println("sql:"+sql);
									// rs = stmt.executeQuery(sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,schemeCd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										applyCustList =rs.getString(1);
										noApplyCustList =rs.getString(2);
										applicableOrdTypes =rs.getString(3);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									 //System.out.println("applyCustList:"+applyCustList+":noApplyCustList:"+noApplyCustList+":applicableOrdTypes:"+applicableOrdTypes);
								     if((orderType.trim().equalsIgnoreCase("NE")) && (applicableOrdTypes == null || applicableOrdTypes.trim().length()== 0))
									 {
										//Below lines commented as not required - jiten - 09/10/06
								     	//System.out.println("schemeCode:"+schemeCode+":");
										//if(schemeCode != null && schemeCode.trim().length()== 0)
										//{
											continue;
										//}
										//else
										//{
										//	break;
										//} 
									 }
									 else if(applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
									 {
										proceed = false;
										StringTokenizer st = new StringTokenizer(applicableOrdTypes,",");
										while(st.hasMoreTokens())
										{
											token = st.nextToken();
											if(orderType.equalsIgnoreCase(token))
											{
												proceed = true;
												break;
											}
										}//st loop
										if(!proceed)
										{
											//Below lines commented as not required - Jiten - 09/10/06
											//if(schemeCode != null && schemeCode.trim().length()== 0)
											//{
												continue;
											//}
											// else
											//{
											//	break;
											//} 
										//}
									}
									prevscheme	= schemeCode; 
									schemeCode = schemeCd ;
									System.out.println("\n********* prevscheme:"+prevscheme+":schemeCode:"+schemeCode);
									if(applyCustList.trim().length() > 0)
									{
										//setnull(ls_scheme_code)
										schemeCode	="";
										//ls_cust_code = dw_header.getitemstring(1,"cust_code")
										StringTokenizer st1 = new StringTokenizer(applyCustList,",");
										while(st1.hasMoreTokens())
										{
											applyCust = (st1.nextToken()).trim();
											if(applyCust.equalsIgnoreCase(custCode.trim()))
											{
												schemeCode = schemeCd;
												break;
											}
										}//st1 loop
									}
									if((noApplyCustList.trim().length() > 0) && (schemeCode!= null))
									{
										//custCode = dw_header.getitemstring(1,"cust_code")
										if(noApplyCustList.trim().length() > 0)
										{
											StringTokenizer st2 = new StringTokenizer(noApplyCustList,",");
											while(st2.hasMoreTokens())
											{
											   noApplyCust = (st2.nextToken()).trim();
												if(noApplyCust.equalsIgnoreCase(custCode.trim()))
												{
													//setnull(ls_scheme_code)
													schemeCode	="";
													break;
												}
											}
										}
									}
									if(schemeCode != null)
									{
										schcnt ++;
									}
									else if(schcnt == 1)
									{
										schemeCode	= prevscheme;
									}
								  //System.out.println("\n===schemeCode:"+schemeCode+":");
								}//end loop if(rsScd.next()) Next Scheme Code
								if(schemeCode != null && schemeCode.trim().length()> 0)
								{
									//CHECKING SLAB ON IS NOT APPL or NOT
									sql="SELECT SLAB_ON FROM SCHEME_APPLICABILITY WHERE  SCHEME_CODE = ? ";
									//System.out.println("sql:"+sql);
									//rs = stmt.executeQuery(sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,schemeCd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										slabOn = rs.getString(1);
									}
									//System.out.println("slabOn:"+slabOn+":");
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(slabOn.equalsIgnoreCase("N")) // scheme is applicable for current site/state
									//CALCULATION OF FREE QTY
									{
										sql="SELECT COUNT(ITEM_CODE) FROM BOMDET "+
											" WHERE BOM_CODE  = ? AND ITEM_CODE = ? ";
										//System.out.println("sql:"+sql);
										//rs = stmt.executeQuery(sql);
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,schemeCd);
										pstmt.setString(2,itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count = rs.getInt(1);
										}
										//System.out.println("count:"+count+":");
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(count > 1) // scheme is on same item
										{
											sql=" SELECT SUM(QTY_PER)FROM BOMDET	"+
												" WHERE BOM_CODE  = ? AND ITEM_CODE = ? ";
											//System.out.println("sql:"+sql);
											//rs = stmt.executeQuery(sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,schemeCd);
											pstmt.setString(2,itemCode);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												totQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("totQty:"+totQty);
											sql="SELECT BATCH_QTY FROM BOM WHERE BOM_CODE = ? ";
											//System.out.println("sql:"+sql);
											//rs = stmt.executeQuery(sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,schemeCd);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												batchQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("batchQty:"+batchQty+":");
											sql=" SELECT SUM(QTY_PER) FROM BOMDET"+
												" WHERE BOM_CODE  = ? AND ITEM_CODE = ? "+
												 " AND 	NATURE = 'F'";
											//System.out.println("sql:"+sql);
											//rs = stmt.executeQuery(sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,schemeCd);
											pstmt.setString(2,itemCode);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												freeQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("freeQty:"+freeQty+":clStk:"+clStk);
											//clStk 	 = clStk - (clStk%(batchQty + freeQty)) ;
											clStk = clStk - (clStk % batchQty);//Above Commented and added by Jiten as code changed in PB - 09/10/06
											//System.out.println("clStk:"+clStk);
											if(clStk % batchQty > 0){//if condition added by Jiten 09/10/06 as code changed in PB
												perc 	 = (freeQty / totQty) * 100	;
												mainStk = clStk - (clStk * perc / 100);
												clStk 	 = mainStk;
											}
											//System.out.println("clStk:"+clStk+":perc:"+perc+":mainStk:"+mainStk+":clStk:"+clStk);
											sql = "SELECT SUM(A.QUANTITY - NVL(A.ALLOC_QTY,0)) "+
												 "FROM STOCK A,INVSTAT B "+ 
												 "WHERE A.INV_STAT  = B.INV_STAT "+
												 "AND A.ITEM_CODE =  ? " +
												 "AND A.SITE_CODE = ? "+
												 "AND A.QUANTITY  > 0"+
												 "AND B.AVAILABLE = 'Y'	"+
												 "AND B.INV_STAT NOT IN (SELECT DISTINCT INV_STAT FROM INV_RESTR)";
											//System.out.println("sql:"+sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,itemCode);
											pstmt.setString(2,siteCodeCurr);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												chkStk = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("chkStk:"+chkStk+":");
											//If the stock in our site(Chnl. Part. repl site) is >0  but less than the reqd qty
											if(chkStk < (orderQty - qtyStk) && chkStk > 0)
											{
												avaiStk = chkStk ;
												//System.out.println("avaiStk:"+avaiStk+":");
												//chkStk = chkStk -(chkStk%(batchQty + freeQty));
												chkStk = chkStk -(chkStk % batchQty);
												//System.out.println("chkStk:"+chkStk+":");
												if(chkStk % batchQty > 0){ // if condition added by Jiten - 09/10/06 
													clStk = chkStk - (chkStk * perc / 100);
												}
												//System.out.println("clStk:"+clStk+":");
												balStk = avaiStk - chkStk;
												//System.out.println("balStk:"+balStk+":");
												if(balStk > 0)
												{
													if(balStk < (batchQty + freeQty))
													{
														//		lc_bal_stk = lc_batch_qty
													}
													else
													{
														//balStk = balStk - ((balStk%batchQty + freeQty)) + (batchQty +freeQty);
														//balStk = balStk - (balStk * perc / 100);
														balStk = balStk - (balStk % batchQty);
														//System.out.println("IN ELSE :balStk:"+balStk+":");
													}
													if(clStk == 0)
													{
														clStk = (orderQty - qtyStk) - balStk ;
														if (clStk < (batchQty + freeQty)) 
														{
															clStk = batchQty;
														}
														else
														{
															//clStk = clStk - (clStk%(batchQty + freeQty)) + (batchQty + freeQty);
															//clStk = clStk - (clStk * perc / 100);
															clStk = clStk - (clStk % batchQty);
															//System.out.println("IN ELSE :clStk:"+clStk+":");
														}
													}
													count = 2;
												}
												else
												{
													count = 1 ;
												}
											}
											else
											{
												//if stk available in sun > ord qty
												balStk = orderQty - qtyStk;
												//System.out.println("1:balStk:"+balStk+":orderQty:"+orderQty+":qtyStk:"+qtyStk+":");
												//balStk = balStk - (balStk % (batchQty + freeQty)) + (batchQty + freeQty);
												balStk = balStk - (balStk % batchQty);
												//System.out.println("2:balStk:"+balStk+":");
												
												//balStk = balStk - (balStk * perc / 100);
												//System.out.println("3:balStk:"+balStk+":");
												clStk = balStk;
												count = 1;
											}
											} // if ll_count > 1
										}// if ls_slab_on = 'N'					 
									}//schemeCode != null
									
							}//while(rsScd.next())
							rsScd.close();
							rsScd = null;
							pstmt1.close();
							pstmt1 = null;							
							// changed by pankaj on 19.02.09 to bring valuexml string out of while(rsScd.next()) loop
							for(int i = 0 ; i < count; i++)
							{
								if(i == 2)
								{
									clStk = balStk;
								}
								//int noOfDetail = i+1;																
								System.out.println("clStk:"+clStk+":");															
								pstmtInser.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()))); // chg_date
								pstmtInser.setString(2,chgTerm); 
								pstmtInser.setString(3,chgUser); 
								pstmtInser.setString(4,locType);
								pstmtInser.setString(5,tranIdG); 
								//changes on by manazir 23/04/09
								String lineNoStr1 ="   "+maxLineNo;
								String lineNoStr2 = (new Integer(maxLineNo)).toString();
								int length1 = lineNoStr1.length();
								int length2 = lineNoStr2.length();
								String subLineNo= lineNoStr1.substring(length2,length1);
								pstmtInser.setString(6,subLineNo);	
								
								//end of changes on 23/04/09
								//pstmtInser.setInt(6,maxLineNo+1);								
								pstmtInser.setString(7,rsCP.getString(6) == null ? "":rsCP.getString(6));
								pstmtInser.setString(8,itemSer);
								pstmtInser.setDouble(9,clStk);							
								pstmtInser.setString(10,unit); // unit 
								pstmtInser.setString(11,itemCode==null?"":itemCode); // itemCode							
								pstmtInser.addBatch();
								maxLineNo++;
								/*valueXmlString.append("<Detail>\r\n"); // comment on 4/14/2009
								valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode==null?"":itemCode).append("]]>").append("</item_code>\r\n");
								valueXmlString.append("<descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</descr>\r\n");
								valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
								valueXmlString.append("<loc_type isSrvCallOnChg='0'>").append("<![CDATA[").append(locType).append("]]>").append("</loc_type>\r\n");
								valueXmlString.append("<item_ser isSrvCallOnChg='0'>").append("<![CDATA[").append(itemSer).append("]]>").append("</item_ser>\r\n");
								valueXmlString.append("<cl_stock isSrvCallOnChg='0'>").append("<![CDATA[").append(clStk).append("]]>").append("</cl_stock>\r\n");
								if (tranId != null)	     // if condition added in 23/05/06 - jiten
								{
									valueXmlString.append("<tran_id isSrvCallOnChg='0'>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
								}
								//valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rs1.getString(6) == null ? "":rs1.getString(6)).append("]]>").append("</rate>\r\n");
								valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rsCP.getString(6) == null ? "":rsCP.getString(6)).append("]]>").append("</rate>\r\n");
								
								valueXmlString.append("</Detail>\r\n");	*/							
								mchk++;
							}//for(int i = 0; i < count; i++)							
							// changed by pankaj on 19.02.09
						}//if(mcnt > 0)
					}//rs1 close()
				}//if(!mopt.equalsIgnoreCase("C"))	
				pstmtInser.executeBatch();
				conn.commit();
				maxLineNo = 0;
				pstmtInser.close();
				pstmtInser=null;
				rsCP.close();
				rsCP = null;
				pstmtCP.close();
				pstmtCP = null;
			}//detCnt
			if (mchk == 0)
			{
				//System.out.println(itmDBAccess.getErrorString("","VTPROCESS1","","",conn));
				return itmDBAccess.getErrorString("","VTPROCESS1","","",conn);
			}
			//valueXmlString.append("</Root>\r\n");
		} // end of try 
		catch(SQLException e)
		{
			try{
			conn.rollback();
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
			
			//System.out.println("Exception : Associate : actionProcess " +e.getMessage());
			throw new ITMException(e);
			
		}
		catch(Exception e)
		{
			try{
			conn.rollback();
			}
			catch(Exception ex)
			{
				//System.out.println("Exception : Associate : actionHandler :(Document dom)" +ex.getMessage());
				throw new ITMException(ex);
				
			}
			
			//System.out.println("Exception : Associate : actionHandler :(Document dom)" +e.getMessage());
				throw new ITMException(e);
			
		}
		finally
		{
			try
			{
				//System.out.println("Closing Connection.......");
				conn.close();
				conn = null;
				connCP.close();
				connCP = null;
			}catch(Exception e){}
		}
		returnErrorString = "<?xml version=\"1.0\"?><Root><Errors><error id=\"VTPROCESS1\" type=\"P\" column_name=\"\"><message>Process Insert Successfully</message><description>"+"Tran Id "+tranIdG+"</description><type>P</type><option>Y</option><time>null</time><alarm>null</alarm><source>null</source><trace></trace><redirect>1</redirect></error></Errors></Root>"; 	
		return returnErrorString ;
				
		
	}//END OF GETDATA(2)

	private Timestamp getCurrdateAppFormat()
    {
        String s = "";	
		 Timestamp timestamp = null;		
       // GenericUtility genericUtility = GenericUtility.getInstance();
		 E12GenericUtility genericUtility= new  E12GenericUtility();
        try
        {
            java.util.Date date = null;
            timestamp = new Timestamp(System.currentTimeMillis());
           // System.out.println(genericUtility.getDBDateFormat());
            
            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
           // s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
        }
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
        }
        return timestamp;
    }
	private String generateTranId(String windowName,String xmlValues,Connection conn)
	{
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = "",errCode ="",errString ="";
			String tranId = null;
			String newKeystring = "";
			boolean found =false;
			 try
			 {
				sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)=UPPER('"+windowName+"')";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				System.out.println("keyString :"+rs.toString());
				String tranSer1 = "";
				String keyString = "";
				String keyCol = "";
				if (rs.next())
				{	
					found =true;
					keyString = rs.getString(1);
					keyCol = rs.getString(2);
					tranSer1 = rs.getString(3);				
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!found)
				{	  
					  sql ="SELECT key_string,TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
					  pstmt	= conn.prepareStatement(sql);
					  rs = pstmt.executeQuery();
					  if (rs.next())
					  {	
						keyString = rs.getString(1);
						keyCol = rs.getString(2);
						tranSer1 = rs.getString(3);	
					  }
					  rs.close();
					  rs = null;
					  pstmt.close();
					  pstmt = null ;
				}
				if(keyString ==null || keyString.trim().length() ==0)
				{
					errCode = "VTSEQ";
					System.out.println("errcode......"+errCode);
					errString = itmDBAccessEJB.getErrorString("","VTSEQ","BASE","",conn);

				}
				//System.out.println("keyString=>"+keyString);
				//System.out.println("keyCol=>"+keyCol);
				//System.out.println("tranSer1"+tranSer1);
				
				//System.out.println("xmlValues  :["+xmlValues+"]");
				
				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			
				//System.out.println(" new tranId :"+tranId);
				if(rs!=null)
				 {
					rs.close();
				 }
				 if(pstmt!=null)
				 {	
					pstmt.close();
				 }
			}
			catch(SQLException ex)
			{
				//System.out.println("Exception ::" +sql+ ex.getMessage() + ":");			
				ex.printStackTrace();	
				tranId=null;
			}
			catch(Exception e)
			{
				//System.out.println("Exception ::" + e.getMessage() + ":");
				e.printStackTrace();
				tranId=null;
			}
			return tranId;
	}//generateTranTd()
}//end class