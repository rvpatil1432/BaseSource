
/********************************************************
	Title : DissOrderDmgPrcEJB
	Date  : 30/04/09	

********************************************************/
package ibase.webitm.ejb.dis;
import ibase.utility.*;
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

@Stateless // added for ejb3
public class DissOrderDmgPrc extends ProcessEJB implements DissOrderDmgPrcLocal,DissOrderDmgPrcRemote //SessionBean
{
	Connection conn = null,connCP = null;
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
	PreparedStatement psmt = null,pstmtDetail = null ;	
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
	
	E12GenericUtility genericUtility = new E12GenericUtility();
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
		
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;	
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

			rtrStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :Xform :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			/*rtrStr = e.getMessage();*/ //Commented By Mukesh Chauhan on 07/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String process(Document dom1, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		
		Statement stmt = null;				
		String locCode="";
		String stockItemCode = "";
		double stockQuantity = 0;
		int liCnt = 0;
		int rows = 0;
		String avail = "";
		String available = "";
		String site = "";
		String strLocCode = "";	
		E12GenericUtility genericUtility = new E12GenericUtility();	
		Connection conn = null;
		CallableStatement cstmt = null;
		PreparedStatement pstmt = null ,pstmtGen=null ,pstmtInv=null,pstmtStock=null;
		PreparedStatement pstmt1 = null,pstmtHeader=null;
		//Statement stmt = null;
		ResultSet rs = null, rsCP = null, rsCP1 = null;
		ResultSet rs1 = null,Dtlrs = null ;
		ResultSet rsScd = null,rsHeader=null;
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
		String tranType =""; 
		String  orderDate = "";
		String salesOrder = "",sundryType = "",sundryCode = "",remarks = ""; 
		String confirmed = "",	confDate = "",	purcOrder = "",	remarks1 = "",	remarks2 = "",	siteCodeShip ="";
		String siteCodeDlv = "",transMode = "",	shipDate = "",	dueDate = "",distRoute = "",policyNo = ""; 
		String custCodeDlv = "",dlvTo = "", dlvAddr1 ="", dlvAddr2 = "", dlvAddr3 = "", dlvCity = "",stanCode = "",	stateCodeDlv = ""; 
		String countCodeDlv = "",dlvPin = "",tel1Dlv = "",tel2Dlv = "",tel3Dlv = "",faxDlv = "",locCodeGit = "";
		String locCodeCons = "",locCodeGitbf = "",locCodeDamaged = "", 	custCodeNotify = "",notifyTo = ""; 
		String notifyAdd1 = "",	notifyAdd2 = "",	notifyAdd3 = "",	notifyCity = "",stanCodeNotify = ""; 
		String stateCodeNotify ="",countCodeNotify = "",notifyPin = "",tel1Notify ="",	tel2Notify ="",	tel3Notify = "",faxNotify ="" ;
		String 	notifyTerm = "",priceList = "",	priceListClg ="",	currCode ="",	exchRateStr = "",	totAmt ="" ;
		String 	netAmt ="",	siteCodeBil ="",projCode = "",	tranSer ="",salesPers = "",	autoReceipt = "",	avaliableYn = "" ;
		String 	targetWgt ="",	targetVol = "",	custOrderNo ="",chgDate = "",status = "",tranMode="";
		// end of code				
		siteCodeCurr = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
		chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");	
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
		String xmlValues = "",tranIdG = "",sqlmax="";
		int maxLineNo =1,cnt = 1;
		String  returnErrorString ="";
		Timestamp tranDateTimestmp = null;
		int lineNo = 1;
		String sqlInvtra = "",sqlGenCode="",sqlDetail="",sqlStock="" ;
		//item_code and qty_order variable
		double rateDec =0;
		double rateClgDec = 0;
		String priceListType ="",taxClassAcct = "",taxEnvAcct = "",taxChap = "",taxClass="",taxEnv="",suppOrCustCode="";
		ArrayList factAndQtyOrderAlt = new ArrayList();	
		ArrayList getNoArtArrayList = new ArrayList();
		double looseQty =0 ;
		String  descr ="",unitSal="",saleOrder="",custItemDescr="";
		double qtyOrder = 0,exchRate =0,fact=0,qtyOrderAlt=0,integQty=0,noArt=0,noArt1=0,noArt2=0,shipQty=0,acIntegralQty=0;
		String stationFrom="",stationTo ="",sitecodeShip="",qtyReturn="",qtyConf="",received="",overShipRec="",tranIdDemand="",packInstr="";
		String factDbl="",qtyOrderAltDbl="",chgSite="",locGroupCons="",taxAmt="";
		double acShipperQty =0,balQty=0,lineNoSord=1,discount=0,qtyShipped=0,confQtyAlt=0;
		boolean insertHeaderbool = false;
		double sumNetAmt = 0,sumTotAmt = 0,taxAmtDec =0,netAmtDec =0;
		int headerVal =0;
		//end of code		
		try
		{	
			ConnDriver connDriver = new ConnDriver();			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;			
			conn.setAutoCommit(false); 
			//connCP.setAutoCommit(false);			
			tranDate = genericUtility.getColumnValue("order_date",dom1);
			tranDateTimestmp = java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
			//tranDateTimestmpApp = java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
			locCodeDamaged = genericUtility.getColumnValue("loc_code__damaged",dom1); 
			locCodeDamaged =locCodeDamaged 	!=null ? locCodeDamaged.trim()    :"";
			if(locCodeDamaged == null || locCodeDamaged.trim().length()==0)
			{
				return itmDBAccess.getErrorString("","VTLOCDMG","","",conn);				
			}
			DistCommon distComm = new DistCommon();
			tranType  =	genericUtility.getColumnValue("tran_type",dom1);
			tranType  =	tranType !=null ?tranType :"";
			if(tranType == null || tranType.trim().length()==0)
			{
				return itmDBAccess.getErrorString("","VTTRATYP","","",conn);				
			}
			orderDate  =genericUtility.getColumnValue("order_date",dom1); 			
			salesOrder =genericUtility.getColumnValue("sales_order",dom1); 
			salesOrder =salesOrder	!=null ?salesOrder.trim()  :"";
			orderType =	genericUtility.getColumnValue("order_type",dom1); 
			orderType =	orderType 	!=null ?orderType.trim()    :"";
			sundryType =genericUtility.getColumnValue("sundry_type",dom1); 
			sundryType =sundryType !=null ? sundryType.trim()   :"";
			
			sundryCode =genericUtility.getColumnValue("sundry_code",dom1); 
			sundryCode =sundryCode !=null ?sundryCode.trim()     :"";			
			siteCode =	genericUtility.getColumnValue("site_code",dom1); 
			siteCode =	siteCode 	!=null ? siteCode.trim()    :"";
			remarks =	genericUtility.getColumnValue("remarks",dom1); 
			remarks =	remarks !=null ? remarks.trim() :"";
			confirmed =	genericUtility.getColumnValue("confirmed",dom1); 
			confirmed =	confirmed 	!=null  ? confirmed.trim()   :"N";
			confDate =	tranDate ;//genericUtility.getColumnValue("conf_date",dom1);				
			purcOrder = genericUtility.getColumnValue("purc_order",dom1);
			purcOrder =	purcOrder 	!=null ?purcOrder.trim()    :"";
			remarks1 = genericUtility.getColumnValue("remarks1",dom1); 
			remarks1 =	remarks1  !=null  ?remarks1.trim()	:"";
			remarks2 =	genericUtility.getColumnValue("remarks2",dom1); 
			remarks2 =	remarks2 	!=null ? remarks2.trim()   :"";
			siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom1); 
			siteCodeShip =siteCodeShip 	!=null ?siteCodeShip.trim()   :"";
			if(siteCodeShip == null || siteCodeShip.trim().length() == 0)
			{
				return itmDBAccess.getErrorString("","VTSITNUL","","",conn);
			}
			if(siteCodeShip != null && siteCodeShip.trim().length() > 0)
			{
				cnt = 0;
				sql="Select COUNT(*) from site  "
					+"where SITE_CODE = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeShip.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(cnt ==0 )
				{
					return itmDBAccess.getErrorString("","VTSITE1","","",conn);
				}			

			}
			siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom1); 
			siteCodeDlv =siteCodeDlv !=null ?siteCodeDlv.trim() :"";
			if(siteCodeDlv == null || siteCodeDlv.trim().length() == 0)
			{
				return itmDBAccess.getErrorString("","VTSITNUL","","",conn);
			}
			if(siteCodeDlv != null && siteCodeDlv.trim().length() > 0)
			{
				cnt = 0;
				sql="Select COUNT(*) from site  "
					+"where SITE_CODE = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeDlv.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(cnt ==0 )
				{
					return itmDBAccess.getErrorString("","VTSITE1","","",conn);
				}
				/*sql="select chg_site  from distorder_type  "
					+"	where tran_type = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranType.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					chgSite=rs.getString("chg_site") == null ? "" :rs.getString("chg_site");			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if("Y".equalsIgnoreCase(chgSite.trim()) )
				{
					return itmDBAccess.getErrorString("","VTSITE2","","",conn);
				}
				if("N".equalsIgnoreCase(chgSite.trim()))
				{
					return itmDBAccess.getErrorString("","VTSITE6","","",conn);
				}*/

			}
			transMode = genericUtility.getColumnValue("trans_mode",dom1); 
			transMode =transMode !=null ?  transMode.trim() :"";
			shipDate = genericUtility.getColumnValue("ship_date",dom1); 			
			dueDate = genericUtility.getColumnValue("due_date",dom1);
			java.sql.Timestamp  orderDateTms = Timestamp.valueOf(genericUtility.getValidDateString( orderDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");										
			java.sql.Timestamp dueDateTms = Timestamp.valueOf(genericUtility.getValidDateString(dueDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			java.sql.Timestamp shipDateTms = Timestamp.valueOf(genericUtility.getValidDateString(shipDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			if(dueDateTms.compareTo (orderDateTms) <  0 )
			{
				return itmDBAccess.getErrorString("","VTDATE8","","",conn);		
			}
			if(dueDateTms.compareTo (shipDateTms) <  0 )
			{
				return itmDBAccess.getErrorString("","VTDATE13","","",conn);		
			}
			if(shipDateTms.compareTo (orderDateTms) <  0 )
			{
				return itmDBAccess.getErrorString("","VTDATE8","","",conn);		
			}
			locCodeGit = genericUtility.getColumnValue("loc_code__git",dom1); 
			locCodeGit =locCodeGit !=null ? locCodeGit   :"";
			if(locCodeGit == null || locCodeGit.trim().length() == 0)
			{
				return itmDBAccess.getErrorString("","VTLOCGINUL","","",conn);
			}
			if(locCodeGit != null && locCodeGit.trim().length() > 0)
			{
				cnt = 0;
				sql="select COUNT(*) from location  "
					+" where loc_code  = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,locCodeGit.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(cnt ==0 )
				{
					return itmDBAccess.getErrorString("","VTLOC1","","",conn);
				}
			}
			locCodeCons = genericUtility.getColumnValue("loc_code__cons",dom1); 
			locCodeCons =locCodeCons !=null ?locCodeCons   :"";
			if(locCodeCons != null && locCodeCons.trim().length() > 0)
			{
				cnt = 0;
				sql="select loc_group__cons from distorder_type  "
					+"where tran_type  = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranType.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locGroupCons=rs.getString("loc_group__cons")== null ? "":rs.getString("loc_group__cons");			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(locGroupCons !=null && locGroupCons.trim().length() > 0)
				{
					
					sql="select count(*) from location where loc_code = ?  "
						+"	and loc_group = ? " ;		
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,locCodeCons.trim());
					pstmt.setString(2,locGroupCons.trim());
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt=rs.getInt(1);				

					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;				
					if(cnt ==0 )
					{
						return itmDBAccess.getErrorString("","VMLOC5","","",conn);
					}
				}
			}
			locCodeGitbf = genericUtility.getColumnValue("loc_code__gitbf",dom1); 
			locCodeGitbf =locCodeGitbf !=null ?locCodeGitbf  :"";			
			priceList = genericUtility.getColumnValue("price_list",dom1); 
			priceList =priceList !=null ?  priceList   :"";
			priceListClg = genericUtility.getColumnValue("price_list__clg",dom1); 
			priceListClg =priceListClg !=null ?priceListClg.trim()   :"";
			currCode = genericUtility.getColumnValue("curr_code",dom1); 
			currCode =currCode !=null ?currCode.trim()  :"";
			if(currCode == null || currCode.trim().length() == 0)
			{
				return itmDBAccess.getErrorString("","VECUR2","","",conn);
			}
			if(currCode != null && currCode.trim().length() > 0)
			{
				cnt = 0;
				sql="select COUNT(*) from currency  "
					+"where curr_code  = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,currCode.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(cnt ==0 )
				{
					return itmDBAccess.getErrorString("","VMCUR1","","",conn);
				}
			}
			exchRateStr = genericUtility.getColumnValue("exch_rate",dom1);
			exchRate =exchRateStr !=null ? Double.parseDouble(exchRateStr)  :1;
			totAmt = genericUtility.getColumnValue("tot_amt",dom1);
			totAmt =totAmt 	!=null ?totAmt  :"0.00";
			taxAmt = genericUtility.getColumnValue("tax_amt",dom1);
			taxAmt =taxAmt !=null ?taxAmt  :"";
			netAmt = genericUtility.getColumnValue("net_amt",dom1);
			netAmt =netAmt !=null ?  netAmt  :"0.00";
			netAmtDec = Double.parseDouble(netAmt);
			siteCodeBil = genericUtility.getColumnValue("site_code__bil",dom1);
			siteCodeBil =siteCodeBil !=null ? siteCodeBil   :"";			
			if(siteCodeBil != null && siteCodeBil.trim().length() > 0)
			{
				sql="Select COUNT(*) from site  "
					+"where SITE_CODE = ? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeBil.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);			

				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(cnt ==0 )
				{
					return itmDBAccess.getErrorString("","VTSITE1","","",conn);
				}			

			}
			projCode = genericUtility.getColumnValue("proj_code",dom1);
			projCode =projCode !=null ?projCode.trim()  :"";
			tranSer = genericUtility.getColumnValue("tran_ser",dom1);
			tranSer =tranSer !=null ?tranSer  :"";
			salesPers = genericUtility.getColumnValue("sales_pers",dom1);
			salesPers =salesPers !=null ? salesPers   :"";
			autoReceipt = genericUtility.getColumnValue("auto_receipt",dom1);
			autoReceipt =autoReceipt !=null ? autoReceipt  :"";
			avaliableYn = genericUtility.getColumnValue("avaliable_yn",dom1);
			avaliableYn =avaliableYn !=null ?   avaliableYn  :"";
			targetWgt = genericUtility.getColumnValue("target_wgt",dom1);
			targetWgt =targetWgt !=null ? targetWgt  :"";
			targetVol = genericUtility.getColumnValue("target_vol",dom1);
			targetVol =targetVol !=null ?targetVol 	:"";
			custOrderNo = genericUtility.getColumnValue("cust_order__no",dom1);
			custOrderNo = custOrderNo!=null ? custOrderNo  :"";
			chgUser = genericUtility.getColumnValue("chg_user",dom1);
			chgUser =chgUser !=null ? chgUser :"";			
			chgTerm = genericUtility.getColumnValue("chg_term",dom1);
			chgTerm =chgTerm	!=null ? chgTerm   :"";			
			chgDate = genericUtility.getColumnValue("chg_date",dom1);			
			status = genericUtility.getColumnValue("status",dom1);
			status =status !=null ?status   :"";			
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<dist_order></dist_order>";
			xmlValues = xmlValues + "<site_code>"+loginSiteCode.trim()+"</site_code>" ;
			xmlValues = xmlValues + "<order_date>"+orderDate.trim()+"</order_date>" ;
			xmlValues = xmlValues + "</Detail1></Root>";	
			tranIdG = generateTranId("w_dist_order",xmlValues,conn);//function to generate NEW transaction id			
			if(tranIdG == null || tranIdG.trim().length() ==0)
			{
				errCode = "VTTRANID";
				System.out.println("errcode......"+errCode);
				errString = itmDBAccess.getErrorString("","VTTRANID",userId);
				return errString;
			}
			sql = "insert into distorder( "
				+"tran_type, order_date,sale_order, " 
				+"	order_type,sundry_type, sundry_code, "
				+"	site_code, remarks,confirmed, "
				+"	conf_date,purc_order, remarks1 , "
				+"	remarks2,site_code__ship, site_code__dlv , "
				+"	trans_mode,ship_date,due_date , "				
				+"	chg_term, loc_code__git,loc_code__cons , "
				+"	loc_code__gitbf,loc_code__damaged, policy_no ," 			
				+"	price_list,price_list__clg,curr_code,"
				+"	exch_rate,tot_amt,tax_amt, "
				+"	net_amt,site_code__bil,proj_code, "
				+"	tran_ser,sales_pers,auto_receipt, "
				+"	avaliable_yn,target_wgt,target_vol, "
				+"	cust_order__no,chg_user,chg_date, "
				+"	status , dist_order ) "
				+"	 VALUES "
				+"	(?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "				
				+"	?,?,?, "
				+"	?,?,?,"  
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "
				+"	?,?,?, "				
				+"	? ,?)" ;
			pstmtHeader	= conn.prepareStatement(sql);
			pstmtHeader.setString(1,tranType);
			pstmtHeader.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
			pstmtHeader.setString(3,salesOrder);
			pstmtHeader.setString(4,orderType);
			pstmtHeader.setString(5,sundryType);
			pstmtHeader.setString(6,sundryCode);
			pstmtHeader.setString(7,siteCode);
			pstmtHeader.setString(8,remarks);
			pstmtHeader.setString(9,confirmed);
			pstmtHeader.setTimestamp(10,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(confDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
			pstmtHeader.setString(11,purcOrder);
			pstmtHeader.setString(12,remarks1);
			pstmtHeader.setString(13,remarks2);
			pstmtHeader.setString(14,siteCodeShip);
			pstmtHeader.setString(15,siteCodeDlv);
			pstmtHeader.setString(16,transMode);
			pstmtHeader.setTimestamp(17,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(shipDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
			pstmtHeader.setTimestamp(18,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(dueDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
			pstmtHeader.setString(19,chgTerm);			
			pstmtHeader.setString(20,locCodeGit);
			pstmtHeader.setString(21,locCodeCons);
			pstmtHeader.setString(22,locCodeGitbf);
			pstmtHeader.setString(23,locCodeDamaged);
			pstmtHeader.setString(24,policyNo);
			pstmtHeader.setString(25,priceList);
			pstmtHeader.setString(26,priceListClg);
			pstmtHeader.setString(27,currCode);
			pstmtHeader.setDouble(28,exchRate);
			//pstmtHeader.setString(29,totAmt);
			pstmtHeader.setString(30,taxAmt);
			//pstmtHeader.setString(31,netAmt);
			pstmtHeader.setString(32,siteCodeBil);
			pstmtHeader.setString(33,projCode);
			pstmtHeader.setString(34,tranSer);
			pstmtHeader.setString(35,salesPers);
			pstmtHeader.setString(36,autoReceipt);
			pstmtHeader.setString(37,avaliableYn);
			pstmtHeader.setString(38,targetWgt);
			pstmtHeader.setString(39,targetVol);
			pstmtHeader.setString(40,custOrderNo);
			pstmtHeader.setString(41,chgUser);
			pstmtHeader.setTimestamp(42,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(chgDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
			pstmtHeader.setString(43,status);	
			pstmtHeader.setString(44,tranIdG);
			
			
			sqlDetail =	"insert into distorder_det( "
					+"	line_no__sord, discount, tax_class, "
					+"	tot_amt, rate__clg, qty_shipped, "
					+"	remarks, qty_received, over_ship_perc, "
					+"	tax_env, item_code, sale_order, "
					+"	pack_instr, tran_id__demand, unit, "
					+"	qty_return, qty_confirm, dist_order, " 
					+"	ship_date, qty_order__alt, due_date, " 
					+"	net_amt, qty_order, tax_amt, "
					+"	conv__qty__alt, unit__alt, line_no, "
					+"	rate, tax_chap  "
					+"	) VALUES  "
					+"	(?,?,?, "
					+"	?,?,?, "
					+"	?,?,?, "
					+"	?,?,?,"
					+"	?,?,?, "
					+"	?,?,?, "
					+"	?,?,?, "
					+"	?,?,?, "
					+"	?,?,?, "
					+"	?,? ) " ;	
			pstmtDetail	= conn.prepareStatement(sqlDetail);
			locCodeDamaged = genericUtility.getColumnValue( "loc_code__damaged", dom1 );			
			System.out.println("locCodeDamaged:"+locCodeDamaged+":");

			if(locCodeDamaged != null && locCodeDamaged.trim().length() >0)
			{
				StringTokenizer st = new StringTokenizer(locCodeDamaged,",");
				ArrayList totLocCode = new ArrayList();
				int totCnt = 0;
				sqlGenCode= "SELECT COUNT(*) FROM GENCODES WHERE  FLD_NAME  = 'LOCATIONS'"+
							" AND MOD_NAME  = 'W_DIST_ORDER' and fld_value = ? ";
				pstmtGen = conn.prepareStatement(sqlGenCode);	

				sqlInvtra="SELECT CASE WHEN A.AVAILABLE IS NULL THEN 'N' ELSE A.AVAILABLE END "+
							" FROM INVSTAT A,LOCATION B	WHERE  A.INV_STAT = B.INV_STAT "+
							" AND B.LOC_CODE = ? " ;
				pstmtInv = conn.prepareStatement(sqlInvtra);	
				while(st.hasMoreTokens())
				{
					locCode = st.nextToken();  //get one locations code from dom header
					totLocCode.add("'"+locCode+"'");
					System.out.println("From StringToken:locCode:"+locCode+":");
					
					pstmtGen.setString(1,locCode);
					rs = pstmtGen.executeQuery();					
					if(rs.next())
					{
						liCnt = rs.getInt(1);
					}					
					rs.close();				
					rs=null;
					System.out.println("liCnt:"+liCnt+":");
					// to note the location not in gencodes 
					if(liCnt == 0)
					{
						//totLocCode.remove(totCnt,"");
						if(!totLocCode.isEmpty())
						{
							System.out.println("At liCnt Condition:Removing LocCode:"+locCode+":");
							totLocCode.remove(totLocCode.indexOf("'"+locCode+"'"));
						}
					}
					// added to compare inventory status of location 
					// with header's availableyn feild 
					if(locCode != null && locCode.trim().length()!= 0)   
					{	
						avail = " ";
											
						pstmtInv.setString(1,locCode);
						rs = pstmtInv.executeQuery();						
						if(rs.next())
						{
							avail = rs.getString(1); 
						}
						
						rs.close();						
						rs=null;
						available =  genericUtility.getColumnValue("avaliable_yn",dom1);
						System.out.println("avail:"+avail+":available:"+available);
						if (available == null)
						{
							available = "";
						}
						if(!avail.equalsIgnoreCase(available)) 
						{
							//totLocCode.set(totCnt,"");
							if(!totLocCode.isEmpty())
							{
								System.out.println("At Avail Condition :Removing LocCode:"+locCode+":");
								totLocCode.remove(totLocCode.indexOf("'"+locCode+"'"));
							}
						}
					}
					totCnt++;
				} //end stToken
				//rs.close(); comment by manazir on 30-05-09
				rs=null;
				pstmtGen.close();
				pstmtGen=null;
				pstmtInv.close();
				pstmtInv=null;
				System.out.println("totLocCode:"+totLocCode+":");
				if(!totLocCode.isEmpty())
				{
					strLocCode = totLocCode.toString();
					strLocCode = strLocCode.substring(1,strLocCode.length()-1);
				}	
				else
				{
				   strLocCode ="''";
				}
				System.out.println("strLocCode:"+strLocCode+":");
				siteCodeShip =  genericUtility.getColumnValue("site_code__ship",dom1);
				System.out.println("siteCodeShip:"+siteCodeShip+":");
				if(strLocCode == null || strLocCode.trim().length()==0 || strLocCode.trim().equalsIgnoreCase("''") )
				{
					return itmDBAccess.getErrorString("","VTLOCDINV","","",conn);
					
				}				
				if(strLocCode != null && strLocCode.trim().length()>0)
				{				
					sqlStock="SELECT STOCK.ITEM_CODE,SUM(STOCK.QUANTITY - (CASE WHEN STOCK.AllOC_QTY IS NULL THEN 0 ELSE STOCK.AllOC_QTY END) ) AS QUANTITY "+
						" FROM STOCK WHERE ( STOCK.SITE_CODE = ? ) AND " + 
						" ( STOCK.LOC_CODE IN ( "+strLocCode.trim()+" ) ) AND "+ 
						" ( STOCK.QUANTITY - (CASE WHEN STOCK.AllOC_QTY IS NULL THEN 0 ELSE STOCK.AllOC_QTY END) > 0 ) GROUP BY STOCK.ITEM_CODE ";
					System.out.println("STOCK:sql:"+sqlStock+":");
					pstmtStock = conn.prepareStatement(sqlStock);
					pstmtStock.setString(1,siteCodeShip);
					//pstmtStock.setString(2,strLocCode);
					rsHeader = pstmtStock.executeQuery();
					lineNo =1;				
					while(rsHeader.next()) // while() comment fortesting purpose
					{
						stockItemCode = rsHeader.getString(1);
						stockQuantity = rsHeader.getDouble(2);					
						itemCode = stockItemCode 	;
						qtyOrder = stockQuantity;
						taxClass = "";
						taxEnv="";
						taxChap="";

						sql="Select descr, unit,unit__sal  from item "
							+"where item_code = ? ";		
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr=rs.getString("descr") != null ?rs.getString("descr") :"";
							unit = rs.getString("unit") !=null ?  rs.getString("unit") : "" ; // uom 
							unitSal= rs.getString("unit__sal") !=null ? rs.getString("unit__sal") : "" ;

						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;
						if(unitSal == null || unitSal.trim().length()==0)
						{
							unitSal = unit ;
						}
						if(saleOrder !=null && saleOrder.trim().length()>0 )
						{
							sql="select cust_code from sorder where sale_order = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custCode=rs.getString("cust_code") != null ? rs.getString("cust_code") :"";
							}
							rs.close();
							rs=null;
							pstmt.close();
							pstmt=null;
							sql="select descr  from customeritem where cust_code = ? and item_code = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(1,itemCode.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custItemDescr=rs.getString("descr") != null ? rs.getString("descr") :"";
							}						
							rs.close();
							rs=null;
							pstmt.close();
							pstmt=null;
						}//saleOrder 
						if(priceList==null || priceList.trim().length()==0 )
						{
							rateDec=0;
						}
						else
						{
							priceListType  = distComm.getPriceListType(priceList,conn); // ls_type
							if(("B".equalsIgnoreCase(priceListType)) )
							{
								rateDec =  0;
							}
							else
							{
								rateDec =  distComm.pickRate( priceList,orderDate,itemCode,"","L",conn);
							}

							// System.out.println("Return From pickRate[:  "+rateDec+"  :]");						
						}
						//System.out.println("[[[[[[ CALL getPriceListType[[[[[["+priceList);
						//System.out.println("[[[[[[ CALL END getPriceListType[[[[[["+priceListType);
						//System.out.println("Return From getPriceListType[:  "+priceListType+"  :]");
						if(!("L".equalsIgnoreCase(priceListType)) && rateDec < 0 )
						{	
							rateDec= 0 ;
						}
						//System.out.println("rateDec[[[[[[[[[["+rateDec);
						rateDec = rateDec * exchRate;
						//System.out.println("rateDec = rateDec * exchRate[[[[[[[[[["+(rateDec * exchRate));
						if(priceListClg == null || priceListClg.trim().length()==0)
						{
							 rateClgDec = 0;	
						}
						else
						{
							priceListType  = distComm.getPriceListType(priceListClg,conn); // ls_type
							if(("B".equalsIgnoreCase(priceListType)) )
							{
								rateClgDec =  0;
							}
							else
							{
								rateClgDec =  distComm.pickRate( priceListClg,orderDate,itemCode,"","L",conn);
							}
							//System.out.println("Return From pickRate[:  "+rateClgDec+"  :]");
						}
						if(rateClgDec == -1)
						{
							rateClgDec =0;
						}
						if(rateClgDec == 0 )
						{						
							  rateClgDec  = rateDec * exchRate ;
						}
						else
						{
							   rateClgDec = rateClgDec * exchRate ;
							
						}
						sql="select stan_code  from site where site_code = ? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeShip);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stationFrom=rs.getString("stan_code") != null ? rs.getString("stan_code") :"";
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;
						
						sql="select stan_code  from site where site_code = ?  " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeDlv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stationTo = rs.getString("stan_code") != null ? rs.getString("stan_code") :"";
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;

						sql="select item_ser  from item where item_code= ?  " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemSer=rs.getString("item_ser") != null ? rs.getString("item_ser") :"";
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;
						//System.out.println("siteCodeShip[[[[:  "+siteCodeShip+":]");
						//System.out.println("siteCodeDlv[[[[[:  "+siteCodeDlv+":]");
						//System.out.println("stationFrom[[[[[:  "+stationFrom+":]");
						//System.out.println("stationTo[[[[[[[:  "+stationTo+":]");
						//System.out.println("itemSer[[[[[[[[[:  "+itemSer+":]");
						//System.out.println("itemCode[[[[[[[[:  "+itemCode+":]");
						//System.out.println("tranType[[[[[[[[:  "+tranType+":]");

						taxClassAcct =  distComm.setPlistTaxClassEnv(siteCodeShip.trim(),siteCodeDlv.trim(),itemCode, tranType,itemSer,"TAX_CLASS", conn);
						//System.out.println("Return From setPlistTaxClassEnv[:  "+taxClassAcct+"  :]");
						if(taxClassAcct!=null && taxClassAcct.trim().length()>0)
						{						
							taxClass = taxClassAcct;						
						}
						else
						{
							//gf_get_taxclass('', '', mcode , ls_site_code__ship)						
							taxClass = distComm.getTaxClass("","",itemCode,siteCodeShip,conn);
							//System.out.println(" Return taxClass[[[[[["+taxClass);
							
						}
						taxEnvAcct =  distComm.setPlistTaxClassEnv(siteCodeShip.trim(),siteCodeDlv.trim(),itemCode,tranType, itemSer,"TAX_ENV", conn);
						//System.out.println("Return From setPlistTaxClassEnv[:  "+taxEnvAcct+"  :]");
						if(taxEnvAcct!=null && taxEnvAcct.trim().length()>0 )
						{											
							taxEnv = taxEnvAcct;
						}
						else
						{
							//System.out.println("taxChap[[[[[[[[[["+taxChap);
							//ls_taxenv	= gf_get_taxenv(ls_stationfr, ls_stationto, ls_taxchap, ls_taxclass, ls_site_code__ship)
							taxEnv = distComm.getTaxEnv( stationFrom , stationTo,  taxChap,  taxClass, siteCodeShip,conn );	
							//System.out.println("Return taxEnv[[[[[[[[[["+taxEnv);
						}
						//ls_taxchap = gf_get_taxchap(mcode, ls_item_ser, '', '' ,ls_site_code__ship)
						taxChap = distComm.getTaxChap( itemCode, itemSer,  "", "", siteCodeShip , conn );	
						//System.out.println("Return taxChap[[[[[[[[[["+taxChap);
						// end of itemCode on 27/04/09 
						
						// itemChange for qty_order

						confQtyAlt = qtyOrder;
						factAndQtyOrderAlt = distComm.getConvQuantityFact( unitSal,  unit,  itemCode,  qtyOrder,  fact,  conn);
						factDbl = (String)factAndQtyOrderAlt.get(0);
						//System.out.println("Return From getConvQuantityFact factDbl [:  "+factDbl+"  :]");
						fact = Double.parseDouble(factDbl);					
						qtyOrderAltDbl = (String)factAndQtyOrderAlt.get(1);
						//System.out.println("Return From getConvQuantityFact qtyOrderAltDbl [:  "+qtyOrderAltDbl+"  :]");
						qtyOrderAlt = Double.parseDouble(qtyOrderAltDbl);
						if (fact > 0) 
						{ 
							qtyOrderAlt = qtyOrder / fact;
						}
						//System.out.println("qtyOrderAlt)..."+qtyOrderAlt);
						//gf_get_no_art(ls_site_code__ship,ls_sundry_code,ls_item_code,'NULL',mqty,'B',ac_shipper_qty,ac_integral_qty)
						getNoArtArrayList = distComm.getNoArtAList( siteCodeShip, sundryCode, itemCode ,  "",  qtyOrder , 'B' ,  acShipperQty ,  acIntegralQty , conn);
						noArt = Double.parseDouble((String)getNoArtArrayList.get(0));
						acShipperQty = Double.parseDouble((String)getNoArtArrayList.get(1));
						acIntegralQty = Double.parseDouble((String)getNoArtArrayList.get(2));	
						//System.out.println("Return From get_no_art_distOrder[: noArt   "+noArt+"  :]");
						//System.out.println("Return From  get_no_art_distOrder[: acShipperQty "+acShipperQty+"  :]");
						//System.out.println("Return From  get_no_art_distOrder[: acIntegralQty "+acIntegralQty+"  :]");					
						shipQty = acShipperQty;
						integQty = acIntegralQty;
						getNoArtArrayList = distComm.getNoArtAList( siteCodeShip, sundryCode, itemCode ,  "",  qtyOrder , 'S' , acShipperQty ,acIntegralQty ,conn) ;//
						noArt1 = Double.parseDouble((String)getNoArtArrayList.get(0));
						System.out.println("Return From   get_no_art_distOrder[: noArt1 "+noArt1+"  :]");
						balQty = qtyOrder - shipQty * noArt1;
						//System.out.println("noArt1)..."+noArt1);
						//System.out.println("shipQty)..."+shipQty);
						//System.out.println("qtyOrder)..."+qtyOrder);
						//System.out.println("balQty)..."+balQty);
						getNoArtArrayList =  distComm.getNoArtAList( siteCodeShip, custCode, itemCode ,  "",  qtyOrder , 'I' , acShipperQty , acIntegralQty , conn) ;

						noArt2 = Double.parseDouble((String)getNoArtArrayList.get(0));					 
						acIntegralQty = Double.parseDouble((String)getNoArtArrayList.get(2));					
						//System.out.println("Return From   get_no_art_distOrder[:noArt2  "+noArt2+"  :]");
						//System.out.println("Return From  get_no_art_distOrder[:  acIntegralQty2 "+acIntegralQty+"  :]");
						integQty = acIntegralQty ;
						shipQty = shipQty * noArt1 ;
						integQty =integQty*noArt2 ;
						//System.out.println("integQty)..."+integQty);
						//System.out.println("shipQty)..."+shipQty);
						//System.out.println("integQty)..."+integQty);
						looseQty = qtyOrder - (shipQty + integQty);	
						//System.out.println("qtyOrder - (shipQty + integQty)..."+looseQty);
						// end of code
						double totAmtDet =qtyOrder*rateDec ;
						netAmtDec = totAmtDet - (totAmtDet* discount)/100  + taxAmtDec ;
						pstmtDetail.setDouble(1,lineNoSord);
						pstmtDetail.setDouble(2,discount);
						pstmtDetail.setString(3,taxClass);
						pstmtDetail.setDouble(4,totAmtDet);
						pstmtDetail.setDouble(5,rateClgDec);
						// 28/11/14 manoharan  this column will be updated when dist issue is confirmed
						//pstmtDetail.setDouble(6,shipQty);
						pstmtDetail.setDouble(6,0);
						pstmtDetail.setString(7,remarks);
						pstmtDetail.setString(8,received);
						pstmtDetail.setString(9,overShipRec);
						pstmtDetail.setString(10,taxEnv);
						pstmtDetail.setString(11,itemCode);
						pstmtDetail.setString(12,saleOrder);
						pstmtDetail.setString(13,packInstr);
						pstmtDetail.setString(14,tranIdDemand);
						pstmtDetail.setString(15,unit);
						pstmtDetail.setString(16,qtyReturn);
						pstmtDetail.setDouble(17,qtyOrder);
						pstmtDetail.setString(18,tranIdG);
						pstmtDetail.setTimestamp(19,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(shipDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmtDetail.setDouble(20,qtyOrderAlt);
						pstmtDetail.setTimestamp(21,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(dueDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmtDetail.setDouble(22,netAmtDec);
						pstmtDetail.setDouble(23,qtyOrder);
						pstmtDetail.setString(24,taxAmt);
						pstmtDetail.setDouble(25,fact);
						pstmtDetail.setString(26,unitSal);
						pstmtDetail.setDouble(27,lineNo);
						pstmtDetail.setDouble(28,rateDec);
						pstmtDetail.setString(29,taxChap);											
						pstmtDetail.addBatch();
						lineNo++;
						insertHeaderbool = true ;
						sumTotAmt = sumTotAmt + totAmtDet ;
						sumNetAmt = sumNetAmt + netAmtDec ;
											
					}//end while
					if(insertHeaderbool == true && lineNo > 1 )
					{
						pstmtHeader.setDouble(29,sumTotAmt);
						//pstmtHeader.setString(30,taxAmt);
						pstmtHeader.setDouble(31,sumNetAmt);
						headerVal = pstmtHeader.executeUpdate();						
					}
					if(insertHeaderbool == false )
					{
						return itmDBAccess.getErrorString("","VTLOCSTOC","","",conn);
					}
					pstmtHeader.close();
					pstmtHeader=null;
					pstmtDetail.executeBatch();
					pstmtDetail.close();
					pstmtDetail = null;
					rsHeader.close();
					rsHeader=null;
					pstmtStock.close();
					pstmtStock=null;
					conn.commit();
				}				 
			}//locCode != null		
		} // end of try 	
		catch(SQLException e)
		{
			try{ conn.rollback();}
			catch(Exception ex)
			{
				System.out.println("Exception : DistOrderDmgPrcEJB : actionAddAll " +ex.getMessage());
				throw new ITMException(ex);
			}
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			try{ conn.rollback();}
			catch(Exception ex)
			{	ex.getMessage();			
				throw new ITMException(ex);
			}
			System.out.println("Exception : DistOrderDmgPrcEJB : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.........");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		if(headerVal > 0)
		{
		returnErrorString = "<?xml version=\"1.0\"?><Root><Errors><error id=\"VTPROCESS1\" type=\"P\" column_name=\"\"><message>Process Completed Successfully</message><description>"+"Tran Id "+tranIdG+"</description><type>P</type><option>Y</option><time>null</time><alarm>null</alarm><source>null</source><trace>Insert Into DistOrder and DistOrderDet </trace><redirect>1</redirect></error></Errors></Root>"; 
		}
		if(headerVal == 0 || strLocCode == null || strLocCode.trim().length()==0)
		{
			returnErrorString = "<?xml version=\"1.0\"?><Root><Errors><error id=\"VTPROCUNS\" type=\"E\" column_name=\"\"><message>Process Not Completed  Successfully</message><description>Tran Id </description><type>P</type><option>Y</option><time>null</time><alarm>null</alarm><source>null</source><trace>Not Insert  </trace><redirect>1</redirect></error></Errors></Root>"; 
		}
		return returnErrorString ;		
	}//END OF GETDATA(2)	
	private String generateTranId(String windowName,String xmlValues,Connection conn) throws Exception 
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
				System.out.println("keyString=>"+keyString);
				System.out.println("keyCol=>"+keyCol);
				System.out.println("tranSer1"+tranSer1);
				
				System.out.println("xmlValues  :["+xmlValues+"]");
				
				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			
				System.out.println(" new tranId :"+tranId);
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
				System.out.println("Exception ::" +sql+ ex.getMessage() + ":");			
				ex.printStackTrace();
				tranId=null;
				throw new  Exception(ex);
				
			}
			catch(Exception e)
			{
				System.out.println("Exception ::" + e.getMessage() + ":");
				e.printStackTrace();
				tranId=null;
				throw new  Exception(e);
			}
			return tranId;
	}//generateTranTd()		
}//end class