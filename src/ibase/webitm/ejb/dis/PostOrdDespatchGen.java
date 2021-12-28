package ibase.webitm.ejb.dis;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import javax.naming.InitialContext;
import ibase.utility.BaseLogger;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import java.util.Random;
import ibase.webitm.ejb.MasterStatefulLocal;

public class PostOrdDespatchGen
{
	E12GenericUtility genericUtility=new E12GenericUtility();
	FinCommon finCommon=new FinCommon();
	ArrayList<String> sordDisList=new ArrayList<String>();
	@SuppressWarnings("unchecked")
	//Modified by Azhar K. on [07-05-2019][Start]
	public String createDespatch(String saleOrderfr, String saleOrderTo,String custCodeFr, String custCodeTo, Timestamp frDate,
			Timestamp toDate, String siteCodeShip, String clubOrder,String xtraParams,Connection conn)	throws RemoteException, ITMException, SQLException
	{
		HashMap hm = new HashMap();
		String retStr = "";
		try
		{
			retStr = createDespatch(saleOrderfr, saleOrderTo, custCodeFr, custCodeTo, frDate, toDate, siteCodeShip, clubOrder, xtraParams, conn, hm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		return retStr;
	}
	public String createDespatch(String saleOrderfr, String saleOrderTo,String custCodeFr, String custCodeTo, Timestamp frDate,
			Timestamp toDate, String siteCodeShip, String clubOrder,String xtraParams,Connection conn,HashMap additionalMap)	throws RemoteException, ITMException, SQLException
	 //Modified by Azhar K. on [07-05-2019][End]
			{
		ibase.utility.UserInfoBean userInfo = new UserInfoBean();
		System.out.println("CREATING DESPATCH  !!!!!!!!!!!!");
		String sql = "";
		// Connection conn = null;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		java.util.Date today = new java.util.Date();
		PreparedStatement pstmt = null, pstmt1 = null,pstmt2=null,pstmt3=null,pstmtdDet=null;
		ResultSet rs = null, rs1 = null,rs2=null,rs3=null;;
		String saleOrder = "", custCode = "", custCodeDlv = "", custCodeBill = "", itemSer = "", priceList = "", crTerm = "";
		String custName="",custNameDlv="",listType="";
		String siteCode = "", currCode = "", dlvAdd1 = "", dlvAdd2 = "", dlvAdd3 = "", tranCode = "", stateCodeDlv = "";
		String stanCode = "",stationDescr="", dlvPin = "", countCodeDlv = "", dlvCity = "", dlvTerm = "", orderType = "";
		String lineNo = "", transMode = "", itemCode = "", remarks = "", currCodeFrt = "", currCodeIns = "";
		String keyString="",keyCol="",tranSer1="";
		double exchrate = 0, exchrateFrt = 0, exchrateIns = 0, insAmt = 0, frtAmt = 0,sordQty=0,itemRate=0,totRate=0,diffRate=0,discAmt=0;
		String stkOpt = "",gpSer="",shName="",lrNo="",gpNo="",confirmed="";
		Date orderDate = null,retestDate=null;
		String splitCode = "",restdate="";
		String tempSplitCode = "";
		String priceListDisc="",priceListClg="",getPriceList="",itemRef="",priceListType="",priceListDiscType="",priceListClgType="";
		Date plDate=null,ordDate=null;
		int count = 0;
		HashMap splitCodeWiseMap = new HashMap();
		int splitCodeSize=0, generatedDesp=0;
		String generatedId="";
		HashMap tempMap = null;	
		HashMap tempMapSord = null;
		HashMap sordDetMap=null;
		ArrayList tempList = null;
		ArrayList sordList=new ArrayList();
		ArrayList sordDetList=null;
		HashMap newTempMap = new HashMap();
		//HashMap newTempMapDet=new HashMap();
		StringBuffer xmlString = null;
		String userId="",chgUser="",chgTerm="";
		String sysDate = "",despDate="";
		Timestamp despDateTs=null,effDate=null, chgDate = null;
		String tranId="";
		int insertCntHdr=0,insertCntDet=0;
		//String xtraParams = null;
		//Details
		String errString = "";
		String orderDtStr ="",plDateStr="";
		Timestamp ordDtTS=null,plDateTs=null;
		int whileCnt=0,whileCntDet=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String lineNoOrd="",expLev="",locCode="",descr="",itemCodeRef="",nature="",applyPrice="",priceVar="";
		String itemType="",unitWt="",unitRate="",rateOpt="",priceListClgYN="",temp="",status="";
		double grossWtPerart=0,tareWtPerArt=0,netWtPerArt=0;
		String siteCodeMfg="",itemCodeOrd="",lotNo="",lotSl="",siteCde="",unitStd="",unit="",custCodeSal="";
		double mZero=0,allocQty=0,qtyOrduom=0,mrate=0,discMerge=0,plistDisc=0,rateStdum=0;
		int cnt=0,lineNum=0;
		boolean lbCheck=false,isDetailExist=false;
		Date expDate=null,mfgDate=null;
		double quantityStd=0,qtyAllocStd=0,convQtyStduom=0,costRate=0,noArt=0,confDiffAmt=0,totAmount=0;
		double rate=0,rateClg=0,qtyDesp=0,pendingQty=0,shipperQty=0,intQty=0,convRtuomStduom=0,rateStduom=0,rateStd=0;
		String taxClass="",taxChap="",taxEnv="",packCode="",custItemRef="",unitRateSord="",unitSord="";
		String postUpto="",confDespOnPost="";
		DistCommon distCommon=new DistCommon();
		Connection connCP=null;
		InitialContext ctx = null;
		int[] insCnt = null;
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		//Pavan Rane 11mar19 
		StringBuffer xmlBuff=new StringBuffer();
		String xmlInvString="",retString="";
		String loginSite = "";
		double grossWeight = 0,tareWeight = 0,netWeight = 0,noAart = 0,offinvAmt = 0,billbackAmt = 0 ;
		double quantityStduom = 0 ,rateStduoms = 0,offinvAmtDet = 0,taxAmtDet = 0 ,discount = 0,totAmt = 0,amount = 0;  		
		int lineNoDet = 0;
		HashMap invAllocTraceMap = null;
		InvAllocTraceBean invBean = null;
		String sqlSord = "",lineType="";//lineType ADDED BY NANDKUMAR GADKARI ON 06/08/19
		boolean cpFlag = false;
		try {
				startTime = System.currentTimeMillis();
			//System.out.println("process starts---------");
			itmDBAccessEJB = new ITMDBAccessEJB();
			//xtraParams = "loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+userInfo.getSiteCode()+"~~loginEmpCode="+userInfo.getEmpCode();
			chgDate = new java.sql.Timestamp(System.currentTimeMillis());
			java.util.Date currentDate = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat(this.genericUtility.getApplDateFormat());
			Calendar c = Calendar.getInstance();
			c.setTime(today);
			today = c.getTime();
			SimpleDateFormat sdf1 = new SimpleDateFormat(this.genericUtility.getApplDateFormat());
			sysDate = sdf.format(today);
			//System.out.println("sysDate****==========" + sysDate);
			despDate=sysDate;
			despDateTs = Timestamp.valueOf(genericUtility.getValidDateString(despDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			//System.out.println("Date to be set as despDateTs@@@@@@@==="+despDateTs);
			//System.out.println("Despatch Date-========="+despDate);
			effDate=despDateTs;
			//System.out.println("Effective date====="+effDate);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			userId =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			ArrayList quantityList = null;
			userId=userInfo.getLoginCode();
		  /*System.out.println("User Id :- ["+userId+"]");
			System.out.println("saleOrderfr=====" + saleOrderfr);
			System.out.println("saleOrderTo=====" + saleOrderTo);
			System.out.println("custCodeFr=====" + custCodeFr);
			System.out.println("custCodeTo=====" + custCodeTo);
			System.out.println("frDate=====" + frDate);
			System.out.println("toDate=====" + toDate);
			System.out.println("siteCodeShip=====" + siteCodeShip);*/
			postUpto = distCommon.getDisparams("999999", "POST_SORDER_UPTO", conn);
			confDespOnPost = distCommon.getDisparams("999999", "CONFIRM_DESPATCH_ONPOST", conn);
			
			java.util.Date date= new java.util.Date();
			Timestamp gpDate =new Timestamp(date.getTime());
			
			//System.out.println("Now the gpDate with time :=>  [" + gpDate+"]");
            
			sql = "SELECT SORDER.SALE_ORDER, SORDER.ORDER_DATE,SORDER.CUST_CODE,SORDER.CUST_CODE__DLV,SORDER.CUST_CODE__BIL  , "
					+ " SORDER.ITEM_SER , SORDER.PRICE_LIST,SORDER.PRICE_LIST__DISC,SORDER.PRICE_LIST__CLG,SORDER.PL_DATE,SORDER.CR_TERM," 
					//"SORDER.SITE_CODE, " +
					+"SORDER.CONFIRMED,SORDER.CONF_DATE , SORDER.CURR_CODE , SORDER.DLV_ADD1 ,SORDER.DLV_ADD2,SORDER.DLV_CITY ,"
					+ "SORDER.COUNT_CODE__DLV,SORDER.DLV_PIN,SORDER.STAN_CODE,SORDER.TRAN_CODE,SORDER.STATE_CODE__DLV,"
					+ "SORDER.DLV_ADD3,SORDER.ALLOC_FLAG,SORDER.DLV_TERM,SORDDET.SITE_CODE,SORDDET.QUANTITY,"
					+ "SORDDET.UNIT, SORDDET.RATE,SORDDET.DISCOUNT,SORDDET.TAX_AMT,SORDDET.TAX_CLASS,SORDDET.TAX_CHAP,"
					+ "SORDDET.TAX_ENV,SORDDET.NET_AMT,SORDDET.UNIT__RATE,SORDDET.RATE__CLG,SORDDET.PACK_CODE,SORDDET.CUST_ITEM__REF,SORDDET.CONV__QTY_STDUOM,"
					+ "SORDDET.CONV__RTUOM_STDUOM,SORDDET.UNIT__STD,SORDDET.QUANTITY__STDUOM,SORDDET.RATE__STDUOM,SORDDET.RATE__STD,"
					+ "SORDDET.NO_ART,SORDDET.ITEM_CODE,SORDDET.ITEM_SER, SORDER.ORDER_TYPE,SORDDET.LINE_NO,"
					+ "SORDER.TRANS_MODE,SORDER.REMARKS,SORDER.EXCH_RATE, SORDER.EXCH_RATE__FRT,SORDER.CURR_CODE__FRT,"
					+ "SORDER.EXCH_RATE__INS,SORDER.CURR_CODE__INS,SORDER.FRT_AMT,SORDER.INS_AMT,SORDDET.NATURE,SORDDET.ITEM_CODE__ORD  "// NATURE AND ITEM_CODE ORD COLUMN ADDED BY NANDKUMAR 03/06/19
					+ "FROM SORDER,SORDDET "
					+ "WHERE (   SORDER  .  SALE_ORDER   =   SORDDET  .  SALE_ORDER   ) "
					+ "and  ( sorder.sale_order >= ? ) "
					+ "AND   ( sorder.sale_order <= ? )"
					+ "AND   ( sorder.cust_code >= ?)" //commented by abhijit Gaikwad  //Changes Reverted by Nandkumar Gadkari As discuss with manoharan sir on 28/08/18 
					//+ "AND   ( sorder.cust_code__bil >= ?)" commented by Nandkumar Gadkari on 28/08/18
					+ "AND  ( sorder.cust_code <= ?) " //commented by abhijit Gaikwad //Changes Reverted by Nandkumar Gadkari As discuss with manoharan sir on 28/08/18 
					//+ "AND  ( sorder.cust_code__bil <= ?) "
					+ "AND    ( sorder.due_date >= ?) "
					+ "AND ( sorder.due_date <= ?) "
					+ "AND   ( sorder.confirmed = 'Y' )"
					+ " AND  ( sorder.alloc_flag = 'Y' )"
					+ "AND  ( sorder.status = 'P' )"
					+ "AND   sorder.site_code__ship = ? "
					+ "ORDER BY   SORDER  .  CUST_CODE   ASC,SORDER.CUST_CODE__DLV ASC, "
					+ "SORDER.ITEM_SER ASC,SORDER.CR_TERM ASC,SORDER.DLV_TERM ASC,"
					+ "SORDER.ORDER_TYPE ASC,SORDER.TRANS_MODE ASC,SORDDET.SITE_CODE ASC";




			pstmt = conn.prepareStatement(sql);
			/*if(clubOrder != null && "Y".equalsIgnoreCase(clubOrder) )
			{
				pstmt.setString(1, saleOrderfr);
				pstmt.setString(2, saleOrderTo);
			}
			else
			{
				pstmt.setString(1, SaleOrder);
				pstmt.setString(2, SaleOrder);
			}*/
			pstmt.setString(1, saleOrderfr);
			pstmt.setString(2, saleOrderTo);
			pstmt.setString(3, custCodeFr);
			pstmt.setString(4, custCodeTo);
			pstmt.setTimestamp(5, frDate);// due date frm
			pstmt.setTimestamp(6, toDate);// due date to
			pstmt.setString(7, siteCodeShip);

			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				whileCnt++;
			  //System.out.println("while cnt==="+whileCnt);
				saleOrder = rs.getString("SALE_ORDER");
				orderDate = rs.getDate("ORDER_DATE");
				custCode = rs.getString("CUST_CODE");
				custCodeDlv = rs.getString("CUST_CODE__DLV");
				custCodeBill = rs.getString("CUST_CODE__BIL");
				itemSer = rs.getString("ITEM_SER");
				priceList = rs.getString("PRICE_LIST");
				priceListDisc=rs.getString("PRICE_LIST__DISC");
				priceListClg=rs.getString("PRICE_LIST__CLG");
				plDate = rs.getDate("PL_DATE");
				crTerm = rs.getString("CR_TERM");
				siteCode = rs.getString("SITE_CODE");
				currCode = rs.getString("CURR_CODE");
				dlvAdd1 = rs.getString("DLV_ADD1");
				dlvAdd2 = rs.getString("DLV_ADD2");
				dlvAdd3 = rs.getString("DLV_ADD3");
				dlvCity = rs.getString("DLV_CITY");
				countCodeDlv = rs.getString("COUNT_CODE__DLV");
				dlvPin = rs.getString("DLV_PIN");
				stanCode = rs.getString("STAN_CODE");
				tranCode = rs.getString("TRAN_CODE");
				stateCodeDlv = rs.getString("STATE_CODE__DLV");
				dlvTerm = rs.getString("DLV_TERM");
				orderType = rs.getString("ORDER_TYPE");
				lineNo = rs.getString("LINE_NO");
				transMode = rs.getString("TRANS_MODE");
				if("P".equalsIgnoreCase(rs.getString("NATURE")))
				{
				itemCode = rs.getString("ITEM_CODE__ORD");
				}
				else
				{
					itemCode = rs.getString("item_code");
				}
				remarks = rs.getString("remarks");
				exchrate = rs.getDouble("EXCH_RATE");
				currCodeFrt = rs.getString("CURR_CODE__FRT");
				currCodeIns = rs.getString("CURR_CODE__INS");
				exchrateFrt = rs.getDouble("EXCH_RATE__FRT");
				exchrateIns = rs.getDouble("EXCH_RATE__INS");
				frtAmt = rs.getDouble("FRT_AMT");
				insAmt = rs.getDouble("INS_AMT");

				rateClg = rs.getDouble("RATE__CLG");
				packCode = rs.getString("PACK_CODE");
				custItemRef = rs.getString("CUST_ITEM__REF");				
				convRtuomStduom = rs.getDouble("CONV__RTUOM_STDUOM");
				rateStduom = rs.getDouble("RATE__STDUOM");
				rateStd = rs.getDouble("RATE__STD");
				unitRateSord = rs.getString("UNIT__RATE");
				unitSord = rs.getString("UNIT");
				 taxClass=rs.getString("TAX_CLASS");
				 taxChap=rs.getString("TAX_CHAP");
				 taxEnv=rs.getString("TAX_ENV");
				 discount = rs.getDouble("DISCOUNT");//Added By Pavan Rane 22OCT19[set discount in despatchdet]

			  /*System.out.println("saleOrder=====" + saleOrder);
				
				System.out.println("orderDate=====" + orderDate);
				System.out.println("custCode=====" + custCode);
				System.out.println("custCodeDlv=====" + custCodeDlv);
				System.out.println("custCodeBill=====" + custCodeBill);
				System.out.println("itemSer=====" + itemSer);

				System.out.println("priceList=====" + priceList);
				System.out.println("priceListClg=====" + priceListClg);
				System.out.println("priceListDisc=====" + priceListDisc);
				System.out.println("plDate=====" + plDate);

				System.out.println("crTerm=====" + crTerm);
				System.out.println("siteCode=====" + siteCode);
				System.out.println("currCode=====" + currCode);
				System.out.println("dlvAdd1=====" + dlvAdd1);
				System.out.println("dlvAdd1=====" + dlvAdd1);

				System.out.println("dlvAdd3=====" + dlvAdd3);
				System.out.println("dlvCity=====" + dlvCity);
				System.out.println("countCodeDlv=====" + countCodeDlv);
				System.out.println("currCode=====" + currCode);
				System.out.println("dlvPin=====" + dlvPin);
				System.out.println("stanCode=====" + stanCode);

				System.out.println("tranCode=====" + tranCode);
				System.out.println("stateCodeDlv=====" + stateCodeDlv);
				System.out.println("dlvTerm=====" + dlvTerm);
				System.out.println("orderType=====" + orderType);
				System.out.println("lineNo=====" + lineNo);
				System.out.println("transMode=====" + transMode);

				System.out.println("itemCode=====" + itemCode);
				System.out.println("remarks=====" + remarks);
				System.out.println("exchrate=====" + exchrate);
				System.out.println("currCodeFrt=====" + currCodeFrt);
				System.out.println("currCodeIns=====" + currCodeIns);
				System.out.println("exchrateFrt=====" + exchrateFrt);
				System.out.println("exchrateIns=====" + exchrateIns);
				System.out.println("frtAmt=====" + frtAmt);
				System.out.println("insAmt=====" + insAmt);				
				System.out.println("rateClg=====" + rateClg);
				System.out.println("packCode=====" + packCode);
				System.out.println("custItemRef=====" + custItemRef);
				System.out.println("convRtuomStduom=====" + convRtuomStduom);
				System.out.println("rateStduom=====" + rateStduom);
				System.out.println("unitRateSord=====" + unitRateSord);*/
				if(orderDate!=null)
				{
					orderDtStr = sdf.format(orderDate);
					ordDtTS = Timestamp.valueOf(genericUtility.getValidDateString(orderDtStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					//System.out.println("Date to be set as ordDtTS@@@@@@@==="+ordDtTS);
				}

				sql = "select stk_opt from siteitem where site_code = ? and item_code = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCode);
				pstmt1.setString(2, itemCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					stkOpt = rs1.getString("stk_opt");
					//System.out.println("stkOpt=====" + stkOpt);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;


				if (stkOpt == null || stkOpt.trim().length() == 0)
				{
					sql = "select stk_opt from item where item_code = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						stkOpt = rs1.getString("stk_opt");
						//System.out.println("stkOpt=====" + stkOpt);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					if (stkOpt == null || stkOpt.trim().length() == 0)
					{
						stkOpt = "0";
					}
				}
				//System.out.println(" final stkOpt=====" + stkOpt);

				// //SELECT TO CHECK WHETHER ITEM IS ALLOCATED OR NOT

				sql = "select count(*)  from sorditem where sale_order = ? "
						+ "and line_no = ?	and item_code__ref = ?"
						+ "and line_type = 'I'	"
						+ "and quantity - (case when qty_desp is NULL then 0 else qty_desp end )  > 0 "
						+ "and case when ? = '0' then 1 else (case when qty_alloc is NULL then 0 else qty_alloc end ) end > 0  ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, saleOrder);
				pstmt1.setString(2, lineNo);
				pstmt1.setString(3, itemCode);
				pstmt1.setString(4, stkOpt);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					count = rs1.getInt(1);
				//	System.out.println("Count =======" + count);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				stkOpt="";

				if (count > 0) 
				{

					/*tempSplitCode = custCode + "@" + custCodeDlv + "@" + itemSer  commented by abhijit Gaikwad
							+ "@" + crTerm + "@" + dlvTerm + "@" + orderType + "@"
							+ siteCode + "@"  + transMode;*/
					System.out.println("Ordertype::::::["+orderType+"]");
					System.out.println("custCodeBill["+custCodeBill+"]");
					tempSplitCode = custCodeBill + "@" + custCodeDlv + "@" + itemSer
							+ "@" + crTerm + "@" + dlvTerm + "@" //+ orderType + "@"---commented by mayur
							+ siteCode + "@"  + transMode;
					//System.out.println("splitCodeWiseMap===" + splitCodeWiseMap);
					//System.out.println("tempSplitCode Combo===" + tempSplitCode);
					if (splitCodeWiseMap.containsKey(tempSplitCode)) 
					{
					//	System.out.println("*****Combination Exist*****");
						tempList = (ArrayList) splitCodeWiseMap.get(tempSplitCode);
					//	System.out.println("Combination Exist TempList======"+ tempList);
					} 
					else 
					{
					//	System.out.println("*****Combination Not Exist*****");
						tempList = new ArrayList();
					//	System.out.println("Combination Not $$ Exist TempList======"+ tempList);
					}

					//System.out.println("tempList======" + tempList);

					tempMap = new HashMap();//to put header detail value from sorder


					sql="  select cust_name from customer where cust_code = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,custCode);

					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						custName =rs1.getString("cust_name");
						//System.out.println("custName ======="+custName);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;


					sql="select cust_name  from customer where cust_code = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,custCodeDlv);

					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						custNameDlv =rs1.getString("cust_name");
						//System.out.println("custNameDlv ======="+custNameDlv);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					sql="select gp_ser from sordertype where order_type = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,orderType);		  	
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						gpSer =rs1.getString("gp_ser");
						//System.out.println("gpSer ======="+gpSer);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(gpSer==null||gpSer.trim().length()==0)
					{
						//ls_gp_ser = left(trim(ls_ordertype),1)
						gpSer=orderType.substring(1);
						//System.out.println("gpSer==="+gpSer);
					}

					sql="select descr from station where stan_code = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,stanCode);		  	
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						stationDescr =rs1.getString("descr");
						//System.out.println("stationDescr ======="+stationDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;


					//Detail Migrate for sorditem
					//System.out.println("Starting Detail Migration@@@@");

					sql="select item_code from sorditem where sale_order = ? " +
							"and line_no = ?  and (status <> 'I' or status is null) order by line_no,item_code  ";

					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,saleOrder);
					pstmt1.setString(2,lineNo);		  	

					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						itemCode =rs1.getString("item_code");
						//System.out.println("itemCode ======="+itemCode);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;



					sql="select stk_opt from item where item_code = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,itemCode);

					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						stkOpt =rs1.getString("stk_opt");
						//System.out.println("stkOpt ======="+stkOpt);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				


					if("0".equals(stkOpt))
					{
						//System.out.println("0.equals(stkOpt)");
						/*sql="  select line_no, exp_lev, item_code__ord, quantity, item_code, quantity qty_alloc,lot_no,lot_sl," +
								"loc_code,unit  unit__std,1 conv__qty_stduom, unit,exp_date, site_code,mfg_date,site_code__mfg " +
								"from sorditem " +
								"where sale_order = ? " +
								"and line_no =?  " +
								"and site_code = ? " +  	
								"and (status <> 'I' or status is null) order by item_code, line_no  ";*/ //commented by nandkumar gadkari on 27/06/18 for  Non Inventory Item.
						sql="  select line_no, exp_lev, item_code__ord, quantity, item_code, quantity qty_alloc," +
								"unit  unit__std,1 conv__qty_stduom, unit, site_code " +
								"from sorditem " +
								"where sale_order = ? " +
								"and line_no =?  " +
								"and site_code = ? " +  	
								"and (status <> 'I' or status is null) order by item_code, line_no  ";// changes in sql  by nandkumar gadkari on 27/06/18
					}
					else
					{
						//System.out.println("Else////");
						/*sql="  select line_no, exp_lev, item_code__ord, quantity, item_code,qty_alloc, " +
			    		"lot_no, lot_sl, loc_code, unit__std, conv__qty_stduom,unit," +
			    		" exp_date, site_code, mfg_date, site_code__mfg " +
			    		"from	 sordalloc " +
			    		"where  sale_order 	=? " +
			    		"and 	 line_no     = ? " +
			    		"and 	 site_code   = ? " +
			    		"and (status <> 'I' or status is null) order by item_code, exp_lev,exp_date";*/

						sql="  select line_no, exp_lev, item_code__ord, quantity, item_code,qty_alloc, " +
								"lot_no, lot_sl, loc_code, unit__std, conv__qty_stduom,unit," +
								" exp_date, site_code, mfg_date, site_code__mfg " +
								"from	 sordalloc " +
								"where  sale_order 	=? " +	
								"and 	 line_no     = ? " +
								"and 	 site_code   = ? " +
								"and (status <> 'I' or status is null) order by item_code, exp_lev,exp_date";

					}
					pstmt2=conn.prepareStatement(sql);    
					pstmt2.setString(1,saleOrder);
					pstmt2.setString(2,lineNo);  
					pstmt2.setString(3,siteCode);
					sordList=new  ArrayList();
					rs2=pstmt2.executeQuery();
					while(rs2.next())
					{
						whileCntDet++;
						//System.out.println("While of Detail count@@@@"+whileCntDet);
						lineNoOrd =rs2.getString("line_no");
						expLev =rs2.getString("exp_lev");
						itemCodeOrd =rs2.getString("item_code__ord");
						quantityStd =rs2.getDouble("quantity");
						itemCode =rs2.getString("item_code");
						qtyAllocStd =rs2.getDouble("qty_alloc");
						unitStd =checkNull(rs2.getString("unit__std")).trim();
						convQtyStduom =rs2.getDouble("conv__qty_stduom");
						unit =checkNull(rs2.getString("unit")).trim();
						siteCde =rs2.getString("site_code");
						if(!"0".equals(stkOpt))// condition added  by nandkumar gadkari on 27/06/18
						{
						lotNo =rs2.getString("lot_no");
						lotSl =rs2.getString("lot_sl");
						locCode =rs2.getString("loc_code");
						expDate =rs2.getDate("exp_date");
						mfgDate =rs2.getDate("mfg_date");
						siteCodeMfg =rs2.getString("site_code__mfg");
						}
						

						/*System.out.println("lineNoOrd ======="+lineNoOrd);
						System.out.println("expLev ======="+expLev);
						System.out.println("itemCodeOrd ======="+itemCodeOrd);
						System.out.println("quantity ======="+quantityStd);
						System.out.println("itemCode ======="+itemCode);
						System.out.println("qtyAllocStd ======="+qtyAllocStd);
						System.out.println("lotNo ======="+lotNo);
						System.out.println("lotSl ======="+lotSl);
						System.out.println("locCode ======="+locCode);
						System.out.println("unitStd ======="+unitStd);
						System.out.println("convQtyStduom ======="+convQtyStduom);
						System.out.println("unit ======="+unit);
						System.out.println("expDate ======="+expDate);
						System.out.println("siteCde ======="+siteCde);
						System.out.println("mfgDate ======="+mfgDate);
						System.out.println("siteCodeMfg ======="+siteCodeMfg);*/


						sql="select sum(quantity__stduom) as qty_desp from despatch, despatchdet 	" +
								"where  despatch.desp_id = despatchdet.desp_id " +
								" and despatchdet.sord_no = ? " +
								" and line_no__sord = ?	" +
								" and exp_lev =  ? " +
								" and item_code = ?  and despatch.confirmed = 'N' 	";  	
						pstmt1=conn.prepareStatement(sql);    
						pstmt1.setString(1,saleOrder);
						pstmt1.setString(2,lineNoOrd);	
						pstmt1.setString(3,expLev);
						pstmt1.setString(4,itemCode);	
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{

							qtyDesp =rs1.getDouble("qty_desp");    
						//	System.out.println("qtyDesp ======="+qtyDesp);

						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if(qtyDesp==0)
						{
							qtyDesp=0;
						}

						sql="select sum(quantity - qty_desp) as pending_qty from sorditem " +
								"where sale_order = ? " +
								"and line_no = ? " +
								"and exp_lev = ? ";
						pstmt1=conn.prepareStatement(sql);    
						pstmt1.setString(1,saleOrder);
						pstmt1.setString(2,lineNoOrd);	
						pstmt1.setString(3,expLev);	
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{

							pendingQty =rs1.getDouble("pending_qty");    
						//	System.out.println("pendingQty ======="+pendingQty);

						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if(pendingQty==0)
						{
							pendingQty=0;
						}

						if(pendingQty < (qtyAllocStd+qtyDesp))
						{
							qtyAllocStd=qtyAllocStd-qtyDesp;
						//	System.out.println("qtyDesp Value=="+qtyDesp);
						}
						if(qtyAllocStd==0)
						{
						//	System.out.println(">>The selected transaction is confirmed");
							rs2.close();//23feb19[to close the cursor and pstmt while returning string]
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;
							errString = itmDBAccessEJB.getErrorString("","VTDESP6","","",conn);
							return errString;
						}
						//if(unitStd!=unit)
					//	System.out.println("unitStd>>["+unitStd+"]");
					//	System.out.println("unit>>["+unit+"]");
						if(!unitStd.equalsIgnoreCase(unit))
						{
							mZero=0;

							//allocQty=distCommon.convQtyFactor(unitStd, unit, itemCode, qtyAllocStd, conn);
							quantityList= new ArrayList();
							quantityList = distCommon.getConvQuantityFact(unitStd, unit, itemCode, qtyAllocStd,0, conn);
							allocQty = Double.parseDouble(quantityList.get(1).toString());
							
							if(allocQty==-999999999)
							{
								//	errcode = "Unable to convert quantity";
							//	System.out.println("Unable to convert quantity");
								rs2.close();//23feb19[to close the cursor and pstmt while returning string]
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
								errString = itmDBAccessEJB.getErrorString("","VTQTY01","","",conn);
								return errString;
							}
							quantityList= new ArrayList();
							//doubt
							//qtyOrduom=distCommon.convQtyFactor(unitStd, unit, itemCode, quantityStd ,conn);
							quantityList = distCommon.getConvQuantityFact(unitStd, unit, itemCode, quantityStd,0, conn);
							qtyOrduom = Double.parseDouble(quantityList.get(1).toString());

							if(qtyOrduom==-999999999)
							{
								//errcode = "Unable to convert quantity";
							//	System.out.println("Unable to convert quantity");
								rs2.close();//23feb19[to close the cursor and pstmt while returning string]
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
								errString = itmDBAccessEJB.getErrorString("","VTQTY01","","",conn);
								return errString;
							}
						}
						else
						{
							allocQty=qtyAllocStd;
							qtyOrduom=quantityStd;
						}

						sql="select descr  from item where item_code = ?";
						pstmt1=	conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							descr =rs1.getString("descr");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						status=" ";// Remvoved I and added  space by  Nandkumar Gadkari on 08/08/18
						
						
						//adding tax chap from sorditem
						sql="select tax_chap  from sorditem where  sale_order = ? and item_code = ? and exp_lev = ? and line_no = ?";
						pstmt1=	conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);
						pstmt1.setString(2, itemCode);
						pstmt1.setString(3, expLev);
						pstmt1.setString(4, lineNo);
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							taxChap =checkNull(rs1.getString("tax_chap"));
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						
						
					//	System.out.println("@@@@@@@ quantity allocQty["+allocQty+"]");
						
						tempMapSord=new HashMap();// contain values for sorditem

						tempMapSord.put("line_no__sord", lineNoOrd);	       
						tempMapSord.put("exp_lev",expLev);
						tempMapSord.put("item_code__ord", itemCodeOrd);
						tempMapSord.put("quantity__ord", qtyOrduom);
						tempMapSord.put("item_code", itemCode);
						
						//  tempMap.put("item_descr", dlvCity);
						tempMapSord.put("quantity", allocQty);
						tempMapSord.put("lot_no", lotNo);
						tempMapSord.put("lot_sl", lotSl);
						tempMapSord.put("loc_code", locCode);

						tempMapSord.put("unit__std", unitStd);
						tempMapSord.put("conv__qty_stduom", convQtyStduom);
						tempMapSord.put("unit", unit);

						//tempMapSord.put("quantity__stduom", allocQty);//Change by chandrashekar on 21-sep-2016
						tempMapSord.put("quantity__stduom", qtyAllocStd);
						tempMapSord.put("quantity_real", allocQty);

						if(expDate!=null)
						{
							tempMapSord.put("exp_date", expDate);
						}
						if(mfgDate!=null)
						{
							tempMapSord.put("mfg_date", mfgDate);
						}

						tempMapSord.put("site_code", siteCode);
						tempMapSord.put("site_code__mfg", siteCde);
						tempMapSord.put("status", status);

						//code change by Pavan R Start 12/JAN/18
						tempMapSord.put("tax_chap", taxChap==null?"":taxChap.trim());
						tempMapSord.put("tax_class", taxClass==null?"":taxClass.trim());
						tempMapSord.put("tax_env", taxEnv==null?"":taxEnv.trim());
						/*tempMapSord.put("tax_chap", taxChap.trim());
						tempMapSord.put("tax_class", taxClass.trim());
						tempMapSord.put("tax_env", taxEnv.trim());*/
						//code change by Pavan R End 12/JAN/18
						
						confDiffAmt=0;
						tempMapSord.put("conf_diff_amt", confDiffAmt);

					//	System.out.println("@@@@@@@@@@@ custItemRef["+custItemRef+"]");
						if(custItemRef!=null && custItemRef.trim().length()>0)
						{
							tempMapSord.put("cust_item__ref", custItemRef);

						}
						else
						{
							sql=" select  ITEM_CODE__REF  from customeritem where item_code  = ? and cust_code =? ";
							pstmt1=conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, custCode);
							rs1=pstmt1.executeQuery();
							if(rs1.next())
							{
								itemCodeRef =rs1.getString("ITEM_CODE__REF");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
						//	System.out.println("@@@@@@@@@@ itemCodeRef==========="+itemCodeRef);

							tempMapSord.put("cust_item__ref",checkNull(itemCodeRef));
						}



						sql="  select (case when rate is null then 0 else rate end ) as costRate  ,retest_date as  retest_date  " +
								"from stock	" +
								"where item_code = ? " +
								"and site_code = ? " +
								"and loc_code = ? " +
								"and lot_no = ? " +
								"and lot_sl = ? ";
						pstmt1=	conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						pstmt1.setString(2, siteCde);
						pstmt1.setString(3, locCode);
						pstmt1.setString(4, lotNo);
						pstmt1.setString(5, lotSl);

						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							costRate =rs1.getDouble("costRate");
							retestDate =rs1.getDate("retest_date");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					//	System.out.println("costRate====="+costRate);
					//	System.out.println("retestDate====="+retestDate);

						tempMapSord.put("cost_rate", costRate);


						if(retestDate!=null)
						{
							restdate=sdf.format(retestDate);
						}

					//	System.out.println("restdate======"+restdate);
						if(restdate!=null && restdate.trim().length()>0  &&restdate.equals("01/01/1900"))
						{
							restdate= null;
						}
						else
						{
							tempMapSord.put("retest_date", retestDate);
						}



						noArt=distCommon.getNoArt(siteCde, custCode, itemCode, packCode, allocQty, 'B', shipperQty, intQty, conn);
					//	System.out.println("noArt========"+noArt);

						tempMapSord.put("no_art", noArt);

						sql="  select nature from sorditem where  sale_order = ?  and line_no = ? and exp_lev = ?";
						pstmt1=	conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);    	
						pstmt1.setString(2, lineNoOrd);    	
						pstmt1.setString(3, expLev);    	
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							nature =rs1.getString("nature");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					//	System.out.println("Nature==="+nature);

						if(nature==null ||nature.trim().length()==0)
						{
							nature="C";
						}

						if(rate==0 && "C".equalsIgnoreCase(nature))
						{

							plDateStr = sdf.format(plDate);
						//	System.out.println("plDateStr======"+plDateStr);
							plDateTs = Timestamp.valueOf(genericUtility.getValidDateString(plDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
						//	System.out.println("plDateTs time stamp@@@@@@@==="+plDateTs);
							///

							sql="select quantity__stduom from  sorddet where  sale_order = ? and  line_no = ?";
							pstmt1=	conn.prepareStatement(sql);
							pstmt1.setString(1, saleOrder);    	
							pstmt1.setString(2, lineNoOrd); 		
							rs1=pstmt1.executeQuery();
							if(rs1.next())
							{
								sordQty =rs1.getDouble("quantity__stduom");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
						//	System.out.println("sordQty======"+sordQty);


							if(priceListDisc==null||priceListDisc.trim().length()==0)
							{
								priceListType= distCommon.getPriceListType(priceList, conn);
							//	System.out.println("priceListType====="+priceListType);
								if(!"L".equals(priceListType))
								{
									sql="   select count(*) from  pricelist where  price_list =? and list_type = 'I'";
									pstmt1=	conn.prepareStatement(sql);
									pstmt1.setString(1, priceList);   					 	
									rs1=pstmt1.executeQuery();
									if(rs1.next())
									{
										cnt =rs1.getInt(1);
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								//	System.out.println("cnt======"+cnt);

									if(cnt==0)
									{

										mrate=distCommon.pickRate(priceList, despDate, itemCode, lotNo, "D", sordQty, conn);//changes	required

									}
									else
									{
										//String listTyp=   siteCode + "@" + locCode + "@" + lotNo;
										String listTyp=   siteCode + "~t" + locCode + "~t" + lotNo;
										mrate=distCommon.pickRate(priceList, despDate, itemCode, listTyp, "I", sordQty, conn);//changes	required


									}
									//Pavan R on 26apr19[error on not rate updated in pricelist master against item and lot for type B]
									if("B".equals(priceListType) && (mrate < 0 || mrate == 0))
									{										
										rs2.close();
										rs2 = null;
										pstmt2.close();
										pstmt2 = null;
										errString = itmDBAccessEJB.getErrorString("","VTRATE1","","",conn);
										return errString;
									}
									//end
									if(mrate<0)
									{
										// errcode = 'VTRATE1' 
										//System.out.println(">Rate is not proper for "+ itemCode + "@" + lotNo);
										rs2.close();//23feb19[to close the cursor and pstmt while returning string]
										rs2 = null;
										pstmt2.close();
										pstmt2 = null;
										errString = itmDBAccessEJB.getErrorString("","VTRATE1","","",conn);
										return errString;
									}

								}

							}
							else
							{
								//	 lc_plist_disc = i_nvo_gbf_func.gbf_get_discount(ls_plist_disc,ld_order_date,ls_custcode,ls_site_code,ls_itemcode,ls_unitstd,lc_disc_merge,ld_plist_date,lc_sord_qty)
								plistDisc=getDiscount(priceListDisc, ordDtTS, custCode, siteCode, itemCode, unit, discMerge, plDateTs, sordQty, conn);
								priceListDiscType= distCommon.getPriceListType(priceListDisc, conn);
							//	System.out.println("priceListDiscType for priceListDisc ======"+priceListDiscType);
								if("M".equals(priceListDiscType))
								{
								//	System.out.println("priceListDiscType for priceListDisc===M");
									priceListType= distCommon.getPriceListType(priceList, conn);
								//	System.out.println("priceListType====="+priceListType);
									if(!"L".equalsIgnoreCase(priceListType))
									{
									//	System.out.println("priceListType not equal to l");
										mrate=distCommon.pickRate(priceList, despDate, itemCode, lotNo, "D", sordQty, conn);//changes	required
									}
									mrate =calcRate(mrate, plistDisc);
								//	System.out.println("mrate from calcRate function====="+mrate);

								}
								else
								{
									sql="select count(*) from pricelist where price_list = ? and list_type = 'I'";
									pstmt1=	conn.prepareStatement(sql);
									pstmt1.setString(1, priceList);   					 	
									rs1=pstmt1.executeQuery();
									if(rs1.next())
									{
										cnt =rs1.getInt(1);
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
							//		System.out.println("cnt======"+cnt);
									if(cnt==0)
									{
									//	System.out.println("cnt is zero======");
										priceListType= distCommon.getPriceListType(priceList, conn);
									//	System.out.println("priceListType====="+priceListType);
										if("L".equalsIgnoreCase(priceListType))
										{
											mrate=distCommon.pickRate(priceList, despDate, itemCode, lotNo, "D", sordQty, conn);
										}else 
										{
											mrate=distCommon.pickRate(priceList, despDate, itemCode, lotNo, "D", sordQty, conn);
										}
										
									}
									else
									{
										mrate=distCommon.pickRate(priceList, despDate, itemCode, lotNo, "D", sordQty, conn);//changes	required****
									}
									if(mrate<0)
									{
									//	System.out.println(">Rate is not proper for !!! "+ itemCode + "@" + lotNo);
										rs2.close();//23feb19[to close the cursor and pstmt while returning string]
										rs2 = null;
										pstmt2.close();
										pstmt2 = null;
										errString = itmDBAccessEJB.getErrorString("","VTRATE1","","",conn);
										return errString;
									}
								}

							}

						}
						if( (nature!=null && nature.trim().length()>0) && ("F".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature) ||"V".equalsIgnoreCase(nature)))
						{
							mrate=0;
						}



						listType= distCommon.getPriceListType(priceList, conn);
						sql="select apply_price,price_var from bom	where bom_code =?";
						pstmt1=	conn.prepareStatement(sql);
						pstmt1.setString(1, itemCodeOrd);   					 	
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							applyPrice =rs1.getString("apply_price");
							priceVar =rs1.getString("price_var");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					//	System.out.println("applyPrice======"+applyPrice);
					//	System.out.println("priceVar======"+priceVar);

						sql="select item_ref  from sorditem where sale_order =? and line_no = ? and exp_lev = ?";
						pstmt1=	conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);   	
						pstmt1.setString(2, lineNo);  
						pstmt1.setString(3, expLev);  
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							itemRef =rs1.getString("item_ref");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					//	System.out.println("itemRef======"+itemRef);

						if((listType!=null && listType.trim().length()>0) && (applyPrice!=null && applyPrice.trim().length()>0 ))
						{
							if("L".equalsIgnoreCase(listType) &&  "E".equalsIgnoreCase(applyPrice))
							{
								sql="select (case when eff_cost is null then 0 else eff_cost end ) as mrate	from   bomdet  " +
										"where  bom_code = ? and    item_ref = ?";
								pstmt1=	conn.prepareStatement(sql);
								pstmt1.setString(1, itemCodeOrd);   	
								pstmt1.setString(2, itemRef);  									
								rs1=pstmt1.executeQuery();
								if(rs1.next())
								{
									mrate =rs1.getDouble("mrate");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
							//	System.out.println("mrate from bomdet======"+mrate);

							}
						}
						tempMapSord.put("conv__rtuom_stduom", convRtuomStduom);
						if(rateStduom !=0)
						{
							rateStdum=convRtuomStduom*rateStduom;
						}else
						{
							rateStdum=convRtuomStduom*mrate;
						}
						if("F".equalsIgnoreCase(nature)|| "I".equalsIgnoreCase(nature) ||"V".equalsIgnoreCase(nature))// nanture type I and V added by nandkumar gadkari on 12/12/18
						{
							rateStdum=0.0;
						}
						System.out.println("convRtuomStduom>>>>"+convRtuomStduom);
						System.out.println("rateStdum==="+rateStdum);
						if(rateStdum<=0 && "C".equalsIgnoreCase(nature) )//Added by chandrashekar on 10-aug-2016 // removed nanture type I condition  by nandkumar gadkari on 12/12/18
						{
							//errString = itmDBAccessEJB.getErrorString("","VTRATE1","","",conn);
							continue;
						}
						tempMapSord.put("rate__stduom", rateStdum);
						tempMapSord.put("rate__std", rateStdum);


						sql="select sum(quantity * rate) as amount from sorddet where sale_order = ? ";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);   
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							totAmount = rs1.getDouble("amount");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if (totAmount > 0)
						{
							CalcSchemeAmt calcSchemeAmt= new CalcSchemeAmt();
							calcSchemeAmt.updateAmt(saleOrder, conn);
						}


						sql="select item_type, unit__netwt, unit__rate from item where item_code = ? ";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1, itemCodeOrd);   
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{

							itemType =rs1.getString("item_type");
							unitWt =rs1.getString("unit__netwt");
							unitRate =rs1.getString("unit__rate");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					
						/*System.out.println("itemType======"+itemType);
						System.out.println("unitWt======"+unitWt);
						System.out.println("unitRate======"+unitRate);*/

						if(itemType!=null && itemType.trim().length()>0)
						{
							sql="select rate_opt from item_type where  item_type = ?";
							pstmt1= conn.prepareStatement(sql);
							pstmt1.setString(1, itemType);
							rs1=pstmt1.executeQuery();
							if(rs1.next())
							{
								rateOpt=rs1.getString("rate_opt");
								//System.out.println("rateOpt===="+rateOpt);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							if("1".equalsIgnoreCase(rateOpt))
							{
								if(unitWt.trim().equalsIgnoreCase(unitRate.trim()))
								{
									if((unitSord!=null && unitSord.trim().length()>0) &&(unitRateSord!=null && unitRateSord.trim().length()>0))

									{
										//if(unitSord!=unitRateSord)
										if(!unitSord.equalsIgnoreCase(unitRateSord))
										{
											sql="select gross_wt_per_art, tare_wt_per_art from stock  " +
													"where item_code = ? " +
													" and site_code = ? " +
													" and   loc_code = ? " +
													" and  lot_no= ? " +
													" and   lot_sl= ? ";
											pstmt1= conn.prepareStatement(sql);
											pstmt1.setString(1, itemCodeOrd);
											pstmt1.setString(2, siteCode);
											pstmt1.setString(3, locCode);
											pstmt1.setString(4, lotNo);
											pstmt1.setString(5, lotSl);
											rs1=pstmt1.executeQuery();
											if(rs1.next())
											{

												grossWtPerart=rs1.getDouble("gross_wt_per_art");
												tareWtPerArt=rs1.getDouble("tare_wt_per_art");
											  /*System.out.println("grossWtPerart===="+grossWtPerart);
												System.out.println("tareWtPerArt===="+tareWtPerArt);*/
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											if(grossWtPerart==0)
											{
												grossWtPerart=0;
											}
											if(tareWtPerArt==0)
											{
												tareWtPerArt=0;
											} 
											netWtPerArt=grossWtPerart-tareWtPerArt;
										//	System.out.println("netWtPerArt====="+netWtPerArt);
											tempMapSord.put("conv__rtuom_stduom", netWtPerArt);

											tempMapSord.put("rate__stduom", netWtPerArt*mrate);
											if (totAmount > 0)
											{
												CalcSchemeAmt calcSchemeAmt= new CalcSchemeAmt();
												calcSchemeAmt.updateAmt(saleOrder, conn);
											}

											/*	 lc_offinv_rate = nvo_fin_inv.gbf_calc_discount_rate(as_sordno,ls_linenoord,ls_itemcode,(ld_net_wt_perart * mrate),'O')
															if isnull(lc_offinv_rate) then lc_offinv_rate = 0
															if lc_offinv_rate > 0 and lc_offinv_rate <> ((ld_net_wt_perart * mrate))  then
																a_dwobject.setitem(ll_newrow, "rate__stduom", lc_offinv_rate)  
															end if
															a_dwobject.setitem(ll_newrow, "rate__std", (ld_net_wt_perart * mrate))  

															mdesc_offinv_amt = nvo_fin_inv.gbf_calc_detdisc_amt(as_sordno,ls_linenoord,ls_itemcode,(ld_net_wt_perart * mrate),lc_qtyallocstd,'O')

															mdesc_bb_amt = nvo_fin_inv.gbf_calc_detdisc_amt(as_sordno,ls_linenoord,ls_itemcode,(ld_net_wt_perart * mrate),lc_qtyallocstd,'B')

															a_dwobject.SetItem(ll_newrow, "disc_schem_offinv_amt", mdesc_offinv_amt )    
															a_dwobject.setitem(ll_newrow, "disc_schem_billback_amt", mdesc_bb_amt )  */

										}
									}
								}
							}


						}

						if(rateClg==0)
						{
							priceListClgYN= distCommon.getDisparams("999999", "PRICE_LIST__CLG_YN", conn);
							//System.out.println("priceListClgYN====="+priceListClgYN);

							if(priceListClgYN.equals("NULLFOUND"))
							{
								priceListClgYN="N";
							}
							if("N".equalsIgnoreCase(priceListClgYN))
							{
								rateClg=mrate;
							}
							else
							{
								priceListClgType= distCommon.getPriceListType(priceListClg, conn);
								//System.out.println("priceListClgType========="+priceListClgType);
								if("L".equalsIgnoreCase(priceListClgType))
								{
									rateClg=distCommon.pickRate(priceListClg, despDate, itemCode, lotNo, "L", quantityStd, conn);
								}else if("B".equalsIgnoreCase(priceListClgType))
								{
									rateClg=distCommon.pickRate(priceListClg, despDate, itemCode, lotNo, "B", quantityStd, conn);
								}

							}
						}

						if(rateClg==0||rateClg==-1)
						{
							tempMapSord.put("rate__clg", mrate);
						}
						else
						{
							tempMapSord.put("rate__clg", rateClg);
						}

						sql=" select distinct list_type from pricelist where price_list = ?";
						pstmt1= conn.prepareStatement(sql);
						pstmt1.setString(1, priceList);
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							listType=rs1.getString("list_type");
						//	System.out.println("listType===="+listType);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if("L".equalsIgnoreCase(listType) && "P".equalsIgnoreCase(applyPrice))
						{
							sql="select rate  from pricelist  where price_list =?";
							pstmt1= conn.prepareStatement(sql);
							pstmt1.setString(1, priceList);
							rs1=pstmt1.executeQuery();
							if(rs1.next())
							{
								itemRate=rs1.getDouble("rate");
								//System.out.println("itemRate===="+itemRate);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							sql=" select sum(rate)  as tot_rate  from pricelist " +
									" where price_list = ? " +
									"and item_code in (select item_code  from sorditem	" +
									"where sale_order = ? 	" +
									"and line_no= ? " +
									"and line_type 	= 'I')";
							pstmt1= conn.prepareStatement(sql);
							pstmt1.setString(1, priceList);
							pstmt1.setString(2, saleOrder);
							pstmt1.setString(3, lineNoOrd);

							rs1=pstmt1.executeQuery();
							if(rs1.next())
							{
								totRate=rs1.getDouble("tot_rate");
								//System.out.println("totRate===="+totRate);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							diffRate= totRate*rateStduom;
							//System.out.println("diffRate===="+diffRate);
							discAmt=diffRate*(itemRate/totRate);
							//System.out.println("discAmt===="+discAmt);
							if(discAmt==0)
							{
								discAmt=0;
							}
							tempMapSord.put("disc_amt", discAmt);

							//System.out.println("DisAmt==="+ checkDoubleNullVal((Double) tempMapSord.get("disc_amt")));	
						}
						else
						{
							//Added By Pavan Rane 22OCT19 start [set discount Amt in despatchdet]
							if( applyPrice== null || applyPrice.trim().length() == 0 )
							{							
								discAmt = ((discount/100) * ((Double)tempMapSord.get("quantity__stduom") * (Double)tempMapSord.get("rate__stduom")));								
								//System.out.println("@@@@@4444 discAmt["+discAmt+"]");
								tempMapSord.put("disc_amt", discAmt);
							}
							else
							{							
								discAmt=0;
								tempMapSord.put("disc_amt", discAmt);
							}
							//Pavan Rane 22OCT19 end [set discount Amt in despatchdet]
						}
						// sordList=new ArrayList();
						tempMapSord.put("discount", discount);//Added By Pavan Rane 22OCT19 end [set discount in despatchdet]
						tempMapSord.put("sord_no", saleOrder);
						
						//ADDED BY NANDKUMAR GADKARI ON 06/08/19------------------START----------------------------------
						sql = " select nature,line_type " +
								" from sorditem where	sale_order = ?  " +
								" and	line_no    = ? and 	exp_lev	   = ? " +
								"	and 	line_type  <> 'B' ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);
						pstmt1.setString(2, lineNo );
						pstmt1.setString(3, expLev );
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							nature = checkNull( rs1.getString("nature"));
							lineType = checkNull( rs1.getString("line_type"));
							
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;  

						if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature) || "V".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature) || "C".equalsIgnoreCase(nature))
						{
							tempMapSord.put("line_type",nature);
						}	
						else
						{
							tempMapSord.put("line_type",lineType);
						}
						//ADDED BY NANDKUMAR GADKARI ON 06/08/19------------------END----------------------------------
						
						sordList.add(tempMapSord);

						//System.out.println("Sord item list====="+sordList);
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;

					lrNo=null;
					tempMap.put("desp_date", sysDate);
					tempMap.put("sord_no", saleOrder);
					if
					(orderDate!=null)
					{
						tempMap.put("sord_date",orderDate);
					}

					tempMap.put("cust_code", custCode);
					tempMap.put("cust_code__dlv", custCodeDlv);	//CUST_CODE__BIL  Added by abhijit Gaikwad
					System.out.println("CUST_CODE__BIL["+custCodeBill+"]");
					tempMap.put("cust_code__bil", custCodeBill);
					tempMap.put("site_code", siteCode);
					tempMap.put("dlv_add1", dlvAdd1);
					tempMap.put("dlv_add2", dlvAdd2);
					tempMap.put("dlv_add3", dlvAdd3);
					tempMap.put("dlv_city", dlvCity);
					tempMap.put("dlv_pin", dlvCity);
					tempMap.put("count_code__dlv", countCodeDlv);
					tempMap.put("state_code__dlv", stateCodeDlv);
					tempMap.put("tran_code", tranCode);

					sql="select sh_name from transporter where tran_code = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,tranCode);		  	
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						shName =rs1.getString("sh_name");
						//System.out.println("shName ======="+shName);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					tempMap.put("stan_code", stanCode);

					sql="select descr  from station where stan_code = ?";
					pstmt1=conn.prepareStatement(sql);    
					pstmt1.setString(1,tranCode);		  	
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						stationDescr =rs1.getString("descr");
					//	System.out.println("stationDescr ======="+stationDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					gpNo=null;
					/* tempMap.put("lr_no", lrNo);
	        tempMap.put("gp_ser", gpSer);	        

	        tempMap.put("gp_no", gpNo);
	        tempMap.put("gp_date", sysDate);*/
					tempMap.put("eff_date", sysDate);


					/*  if len(trim(ls_post_upto)) = 0 or isnull(ls_post_upto) then
	                ls_post_upto = gf_getenv_dis('999999','POST_SORDER_UPTO')    
	            end if
	            ls_conf_onpost = gf_getenv_dis('999999','CONFIRM_DESPATCH_ONPOST')    
	            if ls_conf_onpost = 'Y' or ls_post_upto = 'I' then                            
	                despatch_edit.setitem(ls_hdr_row, "confirmed", 'Y')
	                despatch_edit.setitem(ls_hdr_row, "conf_date", mdesp_date)
	            else
	                despatch_edit.setitem(ls_hdr_row, "confirmed", 'N')
	            end if
					 */


					tempMap.put("trans_mode", transMode);
					tempMap.put("curr_code", currCode);
					tempMap.put("remarks", remarks);	        
					tempMap.put("exch_rate", exchrate);	        
					tempMap.put("curr_code__frt", currCodeFrt);	        
					tempMap.put("exch_rate__frt", exchrateFrt);	        
					tempMap.put("curr_code__ins", currCodeIns);     

					tempMap.put("curr_code__ins", currCodeIns);	        
					tempMap.put("exch_rate__ins", exchrateIns);
					tempMap.put("freight", frtAmt);	        
					tempMap.put("insurance", insAmt);

					tempMap.put("detailList", sordList);

					//System.out.println("Getting value in Map======" + tempMap);
					tempList.add(tempMap);
					//System.out.println("1303Getting tempList value after ading in list ======" + tempList);


					if (splitCodeWiseMap.containsKey(tempSplitCode))
					{
						//System.out.println("splitCodeWiseMap.containsKey(tempSplitCode)");
						splitCodeWiseMap.put(tempSplitCode, tempList);
					}
					else
					{
						//System.out.println("else*************");
						splitCodeWiseMap.put(tempSplitCode, tempList);
					}

				}



				// //SELECT TO CHECK WHETHER ITEM IS ALLOCATED OR NOT

			} 
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//iterating Map

			Set setItem = splitCodeWiseMap.entrySet();
			splitCodeSize=splitCodeWiseMap.size();
			//System.out.println("1327getting set item=====" + setItem);
			tempList = null;
			Iterator itrItem = setItem.iterator();
			int testCnt = 0;
			String tranIdDespatch="";
			double sumNetAmt = 0.0D;
			while (itrItem.hasNext())
			{
				testCnt++;

			//	System.out.println("1335while loop@@@@@@@ " + testCnt);
				Map.Entry itemMapEntry = (Map.Entry)itrItem.next();
				splitCode = (String)itemMapEntry.getKey();
			/*	System.out.println("splitCode---" + splitCode);
				System.out.println("Length---" + splitCode.split("@").length);*/
				tempList = (ArrayList)splitCodeWiseMap.get(splitCode);
			//	System.out.println("TempList Inside While=======" + tempList);

				if (splitCode.split("@").length != 0)
				{
					String spiltArr[]=splitCode.split("@");
					//custCode = checkNull(spiltArr[0]);
					custCodeBill = checkNull(spiltArr[0]);
					custCodeDlv = checkNull(spiltArr[1]);
					itemSer = checkNull(spiltArr[2]);
					crTerm = checkNull(spiltArr[3]);
					dlvTerm = checkNull(spiltArr[4]);
					System.out.print("commented ordertype in split:::");
					//orderType = checkNull(spiltArr[5]);
					siteCode = checkNull(spiltArr[5]);		         
					transMode = checkNull(spiltArr[6]);

				}

				sql="select dlv_add1,dlv_add2,dlv_add3,dlv_city,dlv_pin,count_code__dlv,state_code__dlv from sorder where cust_code__dlv= ? and sale_order = ? ";// sale order filter added by nandkumar gadkari on 21/06/19
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCodeDlv);
				pstmt1.setString(2, saleOrder);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					dlvAdd1 = rs1.getString("dlv_add1");
					dlvAdd2 = rs1.getString("dlv_add2");
					dlvAdd3 = rs1.getString("dlv_add3");
					dlvCity = rs1.getString("dlv_city");				
					dlvPin = rs1.getString("dlv_pin");
					countCodeDlv = rs1.getString("count_code__dlv");
					stateCodeDlv = rs1.getString("state_code__dlv");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			/*	System.out.println("dlvAdd1=====" + dlvAdd1);
				System.out.println("dlvAdd1=====" + dlvAdd2);

				System.out.println("dlvAdd3=====" + dlvAdd3);
				System.out.println("dlvCity=====" + dlvCity);
				System.out.println("countCodeDlv=====" + countCodeDlv);
				System.out.println("stateCodeDlv=====" + stateCodeDlv);

				System.out.println("Header Creation for Despatch@@@@@@@@");*/

				//Pavan Rane 11mar19 start
				/*
				sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE lower(TRAN_WINDOW) = 'w_despatch'";
			//	System.out.println("keyStringQuery--------->>"+sql);
				pstmt1 = conn.prepareStatement(sql);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{ 
					keyString = rs1.getString(1);
					keyCol = rs1.getString(2);
					tranSer1 = rs1.getString(3);				
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1 =null;
				*/
				//Pavan Rane 11mar19 end
				/*System.out.println("keyString :"+ keyString);
				System.out.println("keyCol :"+ keyCol);
				System.out.println("tranSer1 :"+ tranSer1);*/

				String xmlValues = "";
				String tranDateStr = getCurrdateAppFormat();
				xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues +	"<tran_id></tran_id>";
				xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";						
				xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>"; 
				xmlValues = xmlValues + "<desp_date>" + tranDateStr + "</desp_date>"; 
				xmlValues = xmlValues +"</Detail1></Root>";
				//System.out.println("xmlValues  :["+xmlValues+"]");
				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
				//System.out.println("@@@@ generated tranId :["+tranId+"]");
				gpNo=generateGpNo("gpno",siteCde,tempMap.get("sord_no").toString(),conn);
				//Pavan Rane 11jun19 start [to generate password channel partner auto receitp]
				/*Random pwdGenerator = new Random();
				String confPasswd = "" + pwdGenerator.nextInt(12345678);
				System.out.println("CPDespatch PWD #"+confPasswd);*/
				//Pavan Rane 11jun19 end
				//Pavan Rane 11mar19 start [despatch generation instead of dire insert xml to be generated and processrequest to be called]
				/*confirmed="N";
				sql="Insert into despatch(desp_id,desp_date,sord_no,sord_date,cust_code,cust_code__dlv," +
						"site_code,dlv_add1,dlv_add2,dlv_add3,dlv_city,dlv_pin,count_code__dlv,state_code__dlv," +
						"tran_code,stan_code,trans_mode,curr_code,exch_rate," +
						"curr_code__frt,exch_rate__frt,curr_code__ins,exch_rate__ins,freight," +
						//"insurance,eff_date,confirmed,status,chg_user,chg_date,chg_term,lr_date,gp_date,gp_ser,gp_no,benefit_type)" +
						"insurance,eff_date,confirmed,status,chg_user,chg_date,chg_term,lr_date,gp_date,gp_ser,gp_no,benefit_type,remarks)" +
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				//tranId = generateTranId("w_despatch",userId,conn);
				pstmt.setString(1,tranId);
				pstmt.setTimestamp(2,despDateTs);

				pstmt.setString(3,tempMap.get("sord_no").toString());
				pstmt.setDate(4,(Date)tempMap.get("sord_date"));//
				pstmt.setString(5,custCode);
				pstmt.setString(6,custCodeDlv);
				//pstmt.setString(6,tempMap.get("cust_code__dlv").toString());
				pstmt.setString(7,siteCode);
				pstmt.setString(8,dlvAdd1);	
				pstmt.setString(9,dlvAdd2);
				pstmt.setString(10,dlvAdd3);
				pstmt.setString(11,dlvCity);
				pstmt.setString(12,dlvPin);
				pstmt.setString(13,countCodeDlv);
				pstmt.setString(14,stateCodeDlv);		
				//pstmt.setString(11,tempMap.get("count_code__dlv").toString());
				//pstmt.setString(12,tempMap.get("state_code__dlv").toString());		
				if(tempMap.get("tran_code")!= null)
	    		{
					pstmt.setString(15,tempMap.get("tran_code").toString());
	    		}else
	    		{
	    			pstmt.setString(15,"");
	    		}
				//pstmt.setString(15,tempMap.get("tran_code").toString());
				pstmt.setString(16,tempMap.get("stan_code").toString());

				pstmt.setString(17,transMode);
				pstmt.setString(18,currCode);
				//pstmt.setString(31,tempMap.get("remarks").toString());//			
				pstmt.setDouble(19,exchrate);					
				pstmt.setString(20,currCodeFrt);
				pstmt.setDouble(21,exchrateFrt);
				pstmt.setString(22,currCodeIns);	
				pstmt.setDouble(23,exchrateIns);					
				pstmt.setString(24,tempMap.get("freight").toString());	
				pstmt.setString(25,tempMap.get("insurance").toString());
				//	pstmt.setDate(26,(Date)tempMap.get("eff_date"));//
				pstmt.setTimestamp(26,effDate );					
				pstmt.setString(27,confirmed);
				pstmt.setString(28,"I");
				pstmt.setString(29,chgUser);
				//pstmt.setTimestamp(30,despDateTs);
				pstmt.setTimestamp(30,chgDate);
				pstmt.setString(31,chgTerm);
				pstmt.setTimestamp(32,despDateTs);
				//pstmt.setTimestamp(33,despDateTs);//gp_date
				pstmt.setTimestamp(33,gpDate);//gp_date
				pstmt.setString(34,gpSer);
				pstmt.setString(35,gpNo);
				pstmt.setString(36,"FS");
				//Added By PriyankaC on 04March2019. [START]
			    pstmt.setString(37,tempMap.get("remarks")!= null ? tempMap.get("remarks").toString() : "" );
			   //pstmt.setString(37,checkNull(tempMap.get("remarks").toString()));
				//Added By PriyankaC on 04March2019.[END]
				insertCntHdr = pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				//System.out.println("Despatch Header Count===="+insertCntHdr);
				//sumNetAmt = 0.0D; variable assignment added by Pavan R on 07/NOV/17
				*/
				System.out.println("Pavan tempMap...["+tempMap+"]");
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
				xmlBuff.append("<description>Datawindow Root</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>Group0 description</description>");
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
				xmlBuff.append("<description>Header0 members</description>");

				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"despatch11\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
				xmlBuff.append("<desp_id/>");
				
				xmlBuff.append("<desp_date><![CDATA[" + sdf.format(despDateTs) + "]]></desp_date>");
				xmlBuff.append("<sord_no><![CDATA[" + tempMap.get("sord_no").toString() + "]]></sord_no>");
				xmlBuff.append("<sord_date><![CDATA[" + sdf.format((Date)tempMap.get("sord_date")) + "]]></sord_date>");
				xmlBuff.append("<cust_code><![CDATA[" + custCode + "]]></cust_code>");
				xmlBuff.append("<cust_code__dlv><![CDATA[" + custCodeDlv + "]]></cust_code__dlv>");
				xmlBuff.append("<site_code><![CDATA[" + siteCode + "]]></site_code>");
				xmlBuff.append("<dlv_add1><![CDATA[" + dlvAdd1 + "]]></dlv_add1>");
				xmlBuff.append("<dlv_add2><![CDATA[" + dlvAdd2 + "]]></dlv_add2>");
				xmlBuff.append("<dlv_add3><![CDATA[" + dlvAdd3 + "]]></dlv_add3>");
				xmlBuff.append("<dlv_city><![CDATA[" + dlvCity + "]]></dlv_city>");
				xmlBuff.append("<dlv_pin><![CDATA[" + dlvPin + "]]></dlv_pin>");
				xmlBuff.append("<count_code__dlv><![CDATA[" + countCodeDlv + "]]></count_code__dlv>");
				xmlBuff.append("<state_code__dlv><![CDATA[" + stateCodeDlv + "]]></state_code__dlv>");
				if(tempMap.get("tran_code")!= null)
	    		{
					xmlBuff.append("<tran_code><![CDATA[" + tempMap.get("tran_code") + "]]></tran_code>");
	    		}else
	    		{
	    			xmlBuff.append("<tran_code><![CDATA[" + "" + "]]></tran_code>");
	    		}
				xmlBuff.append("<stan_code><![CDATA[" + tempMap.get("stan_code") + "]]></stan_code>");
				xmlBuff.append("<trans_mode><![CDATA[" + transMode + "]]></trans_mode>");
				xmlBuff.append("<curr_code><![CDATA[" + currCode + "]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA[" + exchrate + "]]></exch_rate>");
				xmlBuff.append("<curr_code__frt><![CDATA[" + currCodeFrt + "]]></curr_code__frt>");
				xmlBuff.append("<exch_rate__frt><![CDATA[" + exchrateFrt + "]]></exch_rate__frt>");
				xmlBuff.append("<curr_code__ins><![CDATA[" + currCodeIns + "]]></curr_code__ins>");
				xmlBuff.append("<exch_rate__ins><![CDATA[" + exchrateIns + "]]></exch_rate__ins>");
				xmlBuff.append("<freight><![CDATA[" + tempMap.get("freight") + "]]></freight>");
				xmlBuff.append("<insurance><![CDATA[" + tempMap.get("insurance") + "]]></insurance>");				
				xmlBuff.append("<eff_date><![CDATA[" + sdf.format(effDate) + "]]></eff_date>");				
				xmlBuff.append("<confirmed><![CDATA[" + "N" + "]]></confirmed>");
				xmlBuff.append("<status><![CDATA[" + "I" + "]]></status>");
				xmlBuff.append("<chg_user><![CDATA[" + chgUser + "]]></chg_user>");
				xmlBuff.append("<chg_date><![CDATA[" + sdf.format(chgDate) + "]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA[" + chgTerm + "]]></chg_term>");
				xmlBuff.append("<lr_date><![CDATA[" + sdf.format(despDateTs) + "]]></lr_date>");
				xmlBuff.append("<gp_date><![CDATA[" + sdf.format(gpDate) + "]]></gp_date>");
				xmlBuff.append("<gp_ser><![CDATA[" + gpSer + "]]></gp_ser>");
				xmlBuff.append("<gp_no><![CDATA[" + gpNo + "]]></gp_no>");
				xmlBuff.append("<benefit_type><![CDATA[" + "FS" + "]]></benefit_type>");				
				if(tempMap.get("remarks") != null)
				{
					xmlBuff.append("<remarks><![CDATA[" + tempMap.get("remarks")  + "]]></remarks>");
				}else {
					xmlBuff.append("<remarks><![CDATA[" + "" + "]]></remarks>");
				}	
				//Pavan Rane 11jun19 start [to store the channel partner flag]
				cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
				if(cpFlag)
				{
					xmlBuff.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					//xmlBuff.append("<conf_passwd><![CDATA[" + confPasswd + "]]></conf_passwd>");
				}else 
				{
					xmlBuff.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");
					//xmlBuff.append("<conf_passwd><![CDATA[ ]]></conf_passwd>");
				}
				////Pavan Rane 11jun19 start			
				xmlBuff.append("</Detail1>");				
				//Pavan Rane 11mar19 end
				sumNetAmt = 0.0D;
				lineNum = 0;
				for (int itemCtr = 0; itemCtr < tempList.size(); itemCtr++)
				{
					//templist contain records as per spiltcode of specific size
					newTempMap=(HashMap) tempList.get(itemCtr);
					//System.out.println("newTempMap Value===="+newTempMap);

					//newTempMap contain list of detailMap with key "detailList"
					sordDetList=(ArrayList) newTempMap.get("detailList");

					//System.out.println("sordDetList===="+sordDetList);
					//System.out.println("sordDetList size===="+sordDetList.size());
					for (int sordCtr = 0; sordCtr < sordDetList.size(); sordCtr++)
					{
						lineNum++;
						//System.out.println("lineNum======"+lineNum);
						String lineNumber=String.valueOf(lineNum);
						lineNumber = "    " + lineNumber;
						lineNumber = lineNumber.substring(lineNumber.length() - 3,lineNumber.length());

						//System.out.println("lineNumber======"+lineNumber);
						sordDetMap=new HashMap();
						sordDetMap=(HashMap) sordDetList.get(sordCtr);
						//System.out.println("@@@@@@@@@@@sordDetMap values======[[["+sordDetMap+"]]]"); 						

						if( (Double)sordDetMap.get("quantity") == 0)
						{
							System.out.println("@@@@@@@@@@@@@@@@@@@ test continue.........");
							continue;
						} //test purpose
						/*lc_qty_std = desp_det.getitemnumber(ls_ctr, "quantity__stduom")
									lc_rate_std = desp_det.getitemnumber(ls_ctr, "rate__stduom")
									lc_tot_amt = lc_tot_amt + (lc_rate_std * lc_qty_std)*/


						//System.out.println("quantity__stduom values======"+(Double)sordDetMap.get("quantity__stduom")); 
						//System.out.println("quantity__stduom values======"+(Double)sordDetMap.get("rate__stduom"));   
						//sumNetAmt = 0.0D; commented and added before loop at Lone1607 by Pavan R on 07/NOV/17
						//System.out.println("Sum Amount 1 @@@===" + sumNetAmt);
						sumNetAmt+=((Double)sordDetMap.get("quantity__stduom")*(Double)sordDetMap.get("rate__stduom"));

						//System.out.println("Sum Amount After "+lineNumber+" @@@===" + sumNetAmt);
						//Added by nandkumar gadkari on 27/06/18-----------Start---------------
						sql = "select stk_opt from item where item_code = ? ";
						pstmt3 =  conn.prepareStatement(sql);
						pstmt3.setString(1,checkNull(sordDetMap.get("item_code").toString()));
						rs3 = pstmt3.executeQuery();
						if(rs3.next())
						{
							stkOpt =  rs3.getString("stk_opt");

						}
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;
						//Added by nandkumar gadkari on 27/06/18-----------end---------------
						//Pavan Rane 11mar19 start [despatch generation instead of dire insert xml to be generated and processrequest to be called]
						/*sql="insert into despatchdet(DESP_ID,LINE_NO,SORD_NO,LINE_NO__SORD,EXP_LEV,ITEM_CODE__ORD,ITEM_CODE," +
								"LOT_NO ,LOT_SL,QUANTITY__ORD,QUANTITY,LOC_CODE ,STATUS," +
								"CONV__QTY_STDUOM,UNIT__STD,UNIT,QUANTITY__STDUOM,QUANTITY_REAL," +
								"RATE__STDUOM,NO_ART ,SITE_CODE,MFG_DATE,EXP_DATE,SITE_CODE__MFG," +
								"RATE__CLG,DISC_AMT,COST_RATE,CUST_ITEM__REF,RETEST_DATE,CONF_DIFF_AMT,CONV__RTUOM_STDUOM,RATE__STD,TAX_CLASS,TAX_CHAP,TAX_ENV) " +
								"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						pstmtdDet = conn.prepareStatement(sql);
						pstmtdDet.setString(1,tranId);

						pstmtdDet.setString(2,lineNumber);//
						//pstmt.setString(2,sordDetMap.get("line_no").toString());//
						pstmtdDet.setString(3,checkNull(sordDetMap.get("sord_no").toString()));
						//pstmtdDet.setString(3,checkNull(tempMap.get("sord_no").toString()));
						pstmtdDet.setString(4,checkNull(sordDetMap.get("line_no__sord").toString()));
						pstmtdDet.setString(5,checkNull(sordDetMap.get("exp_lev").toString()));//
						pstmtdDet.setString(6,checkNull(sordDetMap.get("item_code__ord").toString()));
						pstmtdDet.setString(7,checkNull(sordDetMap.get("item_code").toString()));
						//Added by nandkumar gadkari on 27/06/18
						if(!"0".equals(stkOpt))
						{
							pstmtdDet.setString(8,checkNull(sordDetMap.get("lot_no").toString()));
							pstmtdDet.setString(9,checkNull(sordDetMap.get("lot_sl").toString()));	
							pstmtdDet.setString(12,sordDetMap.get("loc_code").toString());
							pstmtdDet.setDate(22,(Date) sordDetMap.get("mfg_date"));
							pstmtdDet.setDate(23,(Date) sordDetMap.get("exp_date"));//			
							pstmtdDet.setString(24,sordDetMap.get("site_code__mfg").toString());
						}
						else
						{
							pstmtdDet.setString(8," ");
							pstmtdDet.setString(9," ");	
							pstmtdDet.setString(12," ");
							pstmtdDet.setDate(22,null);
							pstmtdDet.setDate(23,null);//			
							pstmtdDet.setString(24," ");
						}
						pstmtdDet.setString(8,checkNull(sordDetMap.get("lot_no").toString()));
						pstmtdDet.setString(9,checkNull(sordDetMap.get("lot_sl").toString()));	//commented by nandkumar gadkari on 27/06/18
						pstmtdDet.setDouble(10,(Double)sordDetMap.get("quantity__ord"));
						pstmtdDet.setDouble(11,(Double)sordDetMap.get("quantity"));
						//pstmtdDet.setString(12,sordDetMap.get("loc_code").toString());//commented by nandkumar gadkari on 27/06/18
						pstmtdDet.setString(13,sordDetMap.get("status").toString());
						pstmtdDet.setDouble(14,(Double)sordDetMap.get("conv__qty_stduom"));
						pstmtdDet.setString(15,sordDetMap.get("unit__std").toString());								
						pstmtdDet.setString(16,sordDetMap.get("unit").toString());

						pstmtdDet.setDouble(17,(Double)sordDetMap.get("quantity__stduom"));
						pstmtdDet.setDouble(18,(Double)sordDetMap.get("quantity_real"));	
						pstmtdDet.setDouble(19,(Double)sordDetMap.get("rate__stduom"));
						pstmtdDet.setDouble(20,(Double)sordDetMap.get("no_art"));//
						pstmtdDet.setString(21,sordDetMap.get("site_code").toString());
						pstmtdDet.setDate(22,(Date) sordDetMap.get("mfg_date"));
						pstmtdDet.setDate(23,(Date) sordDetMap.get("exp_date"));//			
						pstmtdDet.setString(24,sordDetMap.get("site_code__mfg").toString());//commented by nandkumar gadkari on 27/06/18

						pstmtdDet.setDouble(25,(Double)sordDetMap.get("rate__clg"));
						//pstmt.setDouble(26, checkDoubleNullVal((Double) sordDetMap.get("disc_amt")));
						pstmtdDet.setDouble(26,((Double)sordDetMap.get("disc_amt")));
						pstmtdDet.setDouble(27,(Double)sordDetMap.get("cost_rate"));	
						pstmtdDet.setString(28,sordDetMap.get("cust_item__ref").toString());
						pstmtdDet.setDate(29,(Date)sordDetMap.get("retest_date"));//
						pstmtdDet.setDouble(30,(Double)sordDetMap.get("conf_diff_amt"));
						pstmtdDet.setDouble(31,(Double)sordDetMap.get("conv__rtuom_stduom"));
						pstmtdDet.setDouble(32,(Double)sordDetMap.get("rate__std"));
						pstmtdDet.setString(33,sordDetMap.get("tax_class").toString());
						pstmtdDet.setString(34,sordDetMap.get("tax_chap").toString());
						pstmtdDet.setString(35,sordDetMap.get("tax_env").toString());

						pstmtdDet.addBatch();
						pstmtdDet.executeBatch();
						pstmtdDet.clearParameters();
						isDetailExist=true;
						pstmtdDet.close();
						pstmtdDet = null;//[pstmtdDet closed and nulled by Pavan R]
						 */						/*insertCntDet = pstmt.executeUpdate();
						pstmt.close();
						pstmt=null;
						System.out.println("Despatch Detail Count===="+insertCntDet);*/
						System.out.println("Pavan R Detail lineNumber:" + lineNumber);
						xmlBuff.append("<Detail2 dbID=\"\" domID=\""+Integer.parseInt(lineNumber.trim())+"\" objContext=\"2\" objName=\"despatch\">");		
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
						xmlBuff.append("<desp_id/>");

						xmlBuff.append("<line_no><![CDATA[" + lineNumber + "]]></line_no>");
						xmlBuff.append("<sord_no><![CDATA[" + checkNull(sordDetMap.get("sord_no").toString()) + "]]></sord_no>");
						xmlBuff.append("<line_no__sord><![CDATA[" + checkNull(sordDetMap.get("line_no__sord").toString()) + "]]></line_no__sord>");
						xmlBuff.append("<exp_lev><![CDATA[" + checkNull(sordDetMap.get("exp_lev").toString()) + "]]></exp_lev>");
						xmlBuff.append("<item_code__ord><![CDATA[" + checkNull(sordDetMap.get("item_code__ord").toString()) + "]]></item_code__ord>");						
						xmlBuff.append("<item_code><![CDATA[" + checkNull(sordDetMap.get("item_code").toString()) + "]]></item_code>");
						//Added by nandkumar gadkari on 27/06/18
						if(!"0".equals(stkOpt))
						{
							xmlBuff.append("<lot_no><![CDATA[" + checkNull(sordDetMap.get("lot_no").toString()) + "]]></lot_no>");
							xmlBuff.append("<lot_sl><![CDATA[" + checkNull(sordDetMap.get("lot_sl").toString()) + "]]></lot_sl>");
							xmlBuff.append("<loc_code><![CDATA[" + sordDetMap.get("loc_code").toString() + "]]></loc_code>");
							if(sordDetMap.get("mfg_date") != null)
							{
								xmlBuff.append("<mfg_date><![CDATA[" + sdf.format((Date) sordDetMap.get("mfg_date")) + "]]></mfg_date>");
							}else {
								xmlBuff.append("<mfg_date><![CDATA[" + "" +"]]></mfg_date>");
							}
							if(sordDetMap.get("exp_date") != null)
							{
								xmlBuff.append("<exp_date><![CDATA[" + sdf.format((Date) sordDetMap.get("exp_date")) + "]]></exp_date>");
							}else {
								xmlBuff.append("<exp_date><![CDATA[" + "" + "]]></exp_date>");
							}
							xmlBuff.append("<site_code__mfg><![CDATA[" + sordDetMap.get("site_code__mfg").toString() + "]]></site_code__mfg>");
						}
						else
						{
							xmlBuff.append("<lot_no><![CDATA[" + " " + "]]></lot_no>");
							xmlBuff.append("<lot_sl><![CDATA[" + " " + "]]></lot_sl>");
							xmlBuff.append("<loc_code><![CDATA[" + " " + "]]></loc_code>");
							xmlBuff.append("<mfg_date><![CDATA[" + "" + "]]></mfg_date>");
							xmlBuff.append("<exp_date><![CDATA[" + "" + "]]></exp_date>");
							xmlBuff.append("<site_code__mfg><![CDATA[" + " " + "]]></site_code__mfg>");													
						}
						xmlBuff.append("<quantity__ord><![CDATA[" + (Double)sordDetMap.get("quantity__ord") + "]]></quantity__ord>");
						xmlBuff.append("<quantity><![CDATA[" + (Double)sordDetMap.get("quantity")  + "]]></quantity>");						
						xmlBuff.append("<status><![CDATA[" + sordDetMap.get("status").toString() + "]]></status>");
						xmlBuff.append("<conv__qty_stduom><![CDATA[" + (Double)sordDetMap.get("conv__qty_stduom") + "]]></conv__qty_stduom>");						
						xmlBuff.append("<unit__std><![CDATA[" + sordDetMap.get("unit__std").toString() + "]]></unit__std>");
						xmlBuff.append("<unit><![CDATA[" + sordDetMap.get("unit").toString() + "]]></unit>");

						xmlBuff.append("<quantity__stduom><![CDATA[" + (Double)sordDetMap.get("quantity__stduom") + "]]></quantity__stduom>");
						xmlBuff.append("<quantity_real><![CDATA[" + (Double)sordDetMap.get("quantity_real") + "]]></quantity_real>");
						xmlBuff.append("<rate__stduom><![CDATA[" + (Double)sordDetMap.get("rate__stduom") + "]]></rate__stduom>");
						xmlBuff.append("<no_art><![CDATA[" + (Double)sordDetMap.get("no_art") + "]]></no_art>");

						xmlBuff.append("<site_code><![CDATA[" + sordDetMap.get("site_code").toString() + "]]></site_code>");
						xmlBuff.append("<rate__clg><![CDATA[" + (Double)sordDetMap.get("rate__clg") + "]]></rate__clg>");
						xmlBuff.append("<disc_amt><![CDATA[" + (Double)sordDetMap.get("disc_amt") + "]]></disc_amt>");
						//Added By Pavan Rane 22OCT19 start [set discount in despatchdet]
						xmlBuff.append("<discount><![CDATA[" + (Double)sordDetMap.get("discount") + "]]></discount>");
						//Pavan Rane 22OCT19 end [set discount in despatchdet]
						xmlBuff.append("<cost_rate><![CDATA[" + (Double)sordDetMap.get("cost_rate") + "]]></cost_rate>");
						xmlBuff.append("<cust_item__ref><![CDATA[" + sordDetMap.get("cust_item__ref").toString() + "]]></cust_item__ref>");
						if(sordDetMap.get("retest_date") != null)
						{
							xmlBuff.append("<retest_date><![CDATA[" + sdf.format((Date)sordDetMap.get("retest_date")) + "]]></retest_date>");
						}else {
							xmlBuff.append("<retest_date><![CDATA[" + "" + "]]></retest_date>");
						}
						xmlBuff.append("<conf_diff_amt><![CDATA[" + (Double)sordDetMap.get("conf_diff_amt") + "]]></conf_diff_amt>");
						xmlBuff.append("<conv__rtuom_stduom><![CDATA[" + (Double)sordDetMap.get("conv__rtuom_stduom") + "]]></conv__rtuom_stduom>");
						xmlBuff.append("<rate__std><![CDATA[" + (Double)sordDetMap.get("rate__std") + "]]></rate__std>");
						xmlBuff.append("<tax_class><![CDATA[" + sordDetMap.get("tax_class").toString() + "]]></tax_class>");
						xmlBuff.append("<tax_chap><![CDATA[" + sordDetMap.get("tax_chap").toString() + "]]></tax_chap>");
						xmlBuff.append("<tax_env><![CDATA[" + sordDetMap.get("tax_env").toString() + "]]></tax_env>");
						xmlBuff.append("<tax_amt><![CDATA[" + "0" + "]]></tax_amt>");
						//ADDED BY NANDKUMAR GADKARI ON 06/08/19------------------START----------------------------------
						xmlBuff.append("<line_type>").append("<![CDATA["+sordDetMap.get("line_type").toString()+"]]>").append("</line_type>");
						//ADDED BY NANDKUMAR GADKARI ON 06/08/19------------------END----------------------------------
						xmlBuff.append("</Detail2>");		
						isDetailExist = true;
						//Pavan R [to deallocate quantity in invalloc_trace for S-ORD] start 
						if(!"0".equals(stkOpt)) //Added By PriyankaC on 22July2019.
						{
							invAllocTraceMap = new HashMap();
							invBean = new InvAllocTraceBean(); 
							invAllocTraceMap.put("ref_ser","S-ORD");
							invAllocTraceMap.put("ref_id", checkNull(sordDetMap.get("sord_no").toString()));
							invAllocTraceMap.put("ref_line", checkNull(sordDetMap.get("line_no__sord").toString()));
							invAllocTraceMap.put("site_code", siteCode);
							invAllocTraceMap.put("item_code", checkNull(sordDetMap.get("item_code").toString()));
							invAllocTraceMap.put("loc_code", checkNull(sordDetMap.get("loc_code").toString()));
							invAllocTraceMap.put("lot_no", checkNull(sordDetMap.get("lot_no").toString()));
							invAllocTraceMap.put("lot_sl", checkNull(sordDetMap.get("lot_sl").toString()));
							invAllocTraceMap.put("alloc_qty",(Double)sordDetMap.get("quantity__stduom") * -1);
							invAllocTraceMap.put("chg_user",chgUser);
							invAllocTraceMap.put("chg_term",chgTerm);
							invAllocTraceMap.put("chg_win","W_SORDER");		
							//added by nandkumar gadkari on 17/04/19-------start=----------
							String logMsg=checkNull(sordDetMap.get("sord_no").toString()) + " " + checkNull(sordDetMap.get("exp_lev").toString()) +" "+ checkNull(sordDetMap.get("line_no__sord").toString()) + " "+"Deallocation of stock from PostOrdDespatchGen";
							invAllocTraceMap.put("alloc_ref",logMsg);	
							//added by nandkumar gadkari on 17/04/19-------end=----------
							errString = invBean.updateInvallocTrace(invAllocTraceMap,conn);
							System.out.println("deallocating.......");
							if(errString != null && errString.trim().length() > 0)
							{
								errString = itmDBAccessEJB.getErrorString("VTSTKNOAVL",errString,"","",conn);
								return errString;
							}else //Pavan R 10apr19[to deallocate in sordalloc and sorditem] start 
							{
								//added by nandkumar gadkari on 17/04/19-------start=----------
								String allocRef="";
								sql = "select alloc_ref from stock where item_code= ? and site_code= ? and lot_no= ? and loc_code=? and lot_sl=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,checkNull(sordDetMap.get("item_code").toString()));
								pstmt.setString(2,siteCode);
								pstmt.setString(3,checkNull(sordDetMap.get("lot_no").toString()));
								pstmt.setString(4,checkNull(sordDetMap.get("loc_code").toString()));
								pstmt.setString(5,checkNull(sordDetMap.get("lot_sl").toString()));
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
									errString = itmDBAccessEJB.getErrorString("VTSTKNOAVL",errString,"","",conn);
									return errString;
								}
								//added by nandkumar gadkari on 17/04/19-------end=----------

								double qtyAlloc = 0;
								double qtyStduom = (Double)sordDetMap.get("quantity__stduom");							 
								int updateCnt = 0;
								sql = "SELECT QTY_ALLOC AS COUNT FROM SORDALLOC " 
										+"WHERE SALE_ORDER = ? "
										+"AND LINE_NO = ? "
										+"AND EXP_LEV = ? "
										+"AND ITEM_CODE__ORD = ? "
										+"AND ITEM_CODE = ? "
										+"AND LOT_NO = ? "
										+"AND LOT_SL = ? "
										+"AND LOC_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,checkNull(sordDetMap.get("sord_no").toString()));
								pstmt.setString(2,checkNull(sordDetMap.get("line_no__sord").toString()));
								pstmt.setString(3,checkNull(sordDetMap.get("exp_lev").toString()));
								pstmt.setString(4,checkNull(sordDetMap.get("item_code__ord").toString()));
								pstmt.setString(5,checkNull(sordDetMap.get("item_code").toString()));
								pstmt.setString(6,checkNull(sordDetMap.get("lot_no").toString()));
								pstmt.setString(7,checkNull(sordDetMap.get("lot_sl").toString()));
								pstmt.setString(8,checkNull(sordDetMap.get("loc_code").toString()));
								rs = pstmt.executeQuery();
								if(rs.next())
								{									
									qtyAlloc = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Postord Length of for loop>>"+lineNumber+"]");
								System.out.println("Postord qtyallocation>>"+qtyAlloc);								
								System.out.println("Postord Qtystduom"+qtyStduom);
								if((qtyAlloc >= qtyStduom) && (qtyAlloc > 0))//deallocation																		
								{
									if (qtyAlloc - qtyStduom <= 0)
									{
										sqlSord = "DELETE FROM SORDALLOC WHERE SALE_ORDER = ? "
												+"AND LINE_NO = ? "
												+"AND EXP_LEV = ? " 
												+"AND ITEM_CODE__ORD = ? " 
												+"AND ITEM_CODE = ? " 
												+"AND LOT_NO = ? " 
												+"AND LOT_SL = ? "
												+"AND LOC_CODE = ? ";
										System.out.println("Postord Delete sql sqlSord :"+sqlSord);
										pstmt = conn.prepareStatement(sqlSord);
										pstmt.setString(1,checkNull(sordDetMap.get("sord_no").toString()));
										pstmt.setString(2,checkNull(sordDetMap.get("line_no__sord").toString()));
										pstmt.setString(3,checkNull(sordDetMap.get("exp_lev").toString()));
										pstmt.setString(4,checkNull(sordDetMap.get("item_code__ord").toString()));
										pstmt.setString(5,checkNull(sordDetMap.get("item_code").toString()));
										pstmt.setString(6,checkNull(sordDetMap.get("lot_no").toString()));
										pstmt.setString(7,checkNull(sordDetMap.get("lot_sl").toString()));
										pstmt.setString(8,checkNull(sordDetMap.get("loc_code").toString()));
										updateCnt = pstmt.executeUpdate();
										pstmt.close();
										pstmt = null;
										System.out.println("Postord Deleted the no of records are : updateCnt :"+updateCnt);
									}
									else
									{
										sqlSord = "UPDATE SORDALLOC SET QTY_ALLOC = QTY_ALLOC - ? "
												+"WHERE SALE_ORDER = ? "
												+"AND LINE_NO = ? "
												+"AND EXP_LEV = ? "
												+"AND ITEM_CODE__ORD = ? "
												+"AND ITEM_CODE = ? "
												+"AND LOT_NO = ? "
												+"AND LOT_SL = ? "
												+"AND LOC_CODE = ? ";
										System.out.println("Postord Update sql sqlSord :"+sqlSord);
										pstmt = conn.prepareStatement(sqlSord);
										pstmt.setDouble(1,qtyStduom);
										pstmt.setString(2,checkNull(sordDetMap.get("sord_no").toString()));
										pstmt.setString(3,checkNull(sordDetMap.get("line_no__sord").toString()));
										pstmt.setString(4,checkNull(sordDetMap.get("exp_lev").toString()));
										pstmt.setString(5,checkNull(sordDetMap.get("item_code__ord").toString()));
										pstmt.setString(6,checkNull(sordDetMap.get("item_code").toString()));
										pstmt.setString(7,checkNull(sordDetMap.get("lot_no").toString()));
										pstmt.setString(8,checkNull(sordDetMap.get("lot_sl").toString()));
										pstmt.setString(9,checkNull(sordDetMap.get("loc_code").toString()));
										updateCnt = pstmt.executeUpdate();
										pstmt.close();
										pstmt = null;
										System.out.println("Postord Updated the no of records are : updateCnt :"+updateCnt);
									}
									sqlSord = "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC - ? "
											+"WHERE SALE_ORDER = ? "
											+"AND LINE_NO = ? "
											+"AND EXP_LEV = ? ";
									System.out.println("Postord Update sql sqlSord :"+sqlSord);
									pstmt = conn.prepareStatement(sqlSord);									
									pstmt.setDouble(1,qtyStduom);
									pstmt.setString(2,checkNull(sordDetMap.get("sord_no").toString()));
									pstmt.setString(3,checkNull(sordDetMap.get("line_no__sord").toString()));
									pstmt.setString(4,checkNull(sordDetMap.get("exp_lev").toString()));
									updateCnt = pstmt.executeUpdate();
									pstmt.close();
									pstmt = null;
									System.out.println("Postord Updated the no of records : updateCnt :"+updateCnt);
								}
							}
							//Pavan R 10apr19 end
						}					
					}
				}
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlInvString=xmlBuff.toString();
				//System.out.println("XML generated  :- ["+xmlInvString+"]");				
				/**
				 * Master statefull
				 * Save data
				 * */
				if(lineNum > 0)
				{
					retString = saveData(loginSite, xmlInvString, xtraParams, conn);
				}				
				System.out.println("Pavan Rane Return string after save data :- ["+retString+"]");
				if (retString.indexOf("Success") > -1)
				{
					String[] arrayForTranIdIssue = retString.split("<TranID>");
					int endIndexIssue = arrayForTranIdIssue[1].indexOf("</TranID>");
					tranId = arrayForTranIdIssue[1].substring(0, endIndexIssue);
					System.out.println("@V@ Tran id :- [" + tranId + "]");																			
					/*sql = "UPDATE DESPATCH SET TOT_VALUE= ? WHERE DESP_ID= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, sumNetAmt);
					pstmt.setString(2, tranId);			              
					pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;*/
					/*if(isDetailExist)
					{
						pstmtdDet.executeBatch();
					}*/
					//if(insertCntHdr>0 && insertCntDet>0 )
					//if(insertCntHdr>0 && isDetailExist  )
					//{
						//generatedDesp++;
					//generatedId+=tranId+"@"+tempMap.get("sord_no").toString()+",";
					//Pavan Rane 11mar19 start [to updated tot_value and so as not updated from post_save] 
					sql = " select sord_no,line_no__sord ,quantity__stduom,rate__stduom ,disc_schem_offinv_amt,tax_amt from despatchdet where desp_id  = ? ";
					pstmt1= conn.prepareStatement(sql);
					pstmt1.setString( 1, tranId );
					rs1 = pstmt1.executeQuery();
					while (rs1.next())
					{
						saleOrder = rs1.getString("sord_no");
						lineNoDet = rs1.getInt("line_no__sord");
						quantityStduom = rs1.getDouble("quantity__stduom");
						rateStduoms = rs1.getDouble("rate__stduom");
						offinvAmtDet = rs1.getDouble("disc_schem_offinv_amt");
						taxAmtDet = rs1.getDouble("tax_amt");
						
						sql = " select  discount  from sorddet where sale_order = ? and line_no = ? ";
						pstmt= conn.prepareStatement(sql);
						pstmt.setString( 1, saleOrder );
						pstmt.setInt( 2, lineNoDet );
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							discount = rs.getDouble("discount");
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//[(quantity__stduom*rate__stduom)-((quantity__stduom*rate__stduom*discount)/100)-disc_schem_offinv_amt+tax_amt]
						amount = (quantityStduom*rateStduoms) - ((quantityStduom*rateStduoms*discount)/100) - offinvAmtDet + taxAmtDet;
						System.out.println("amount="+amount);
						totAmt = totAmt + amount;
					}	
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;	
					System.out.println("tot amt = "+totAmt);

					sql = "select sum(gross_weight),sum(tare_weight) ,sum(nett_weight) ,sum(no_art) ,sum(disc_schem_offinv_amt) ,sum(disc_schem_billback_amt) "
							+" from despatchdet where desp_id = ?  ";
					pstmt= conn.prepareStatement(sql);
					pstmt.setString( 1, tranId );
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						grossWeight = rs.getDouble(1);
						tareWeight = rs.getDouble(2);
						netWeight = rs.getDouble(3);
						noAart = rs.getDouble(4);
						offinvAmt = rs.getDouble(5);
						billbackAmt = rs.getDouble(6);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("sum ="+grossWeight+"  "+tareWeight+"    "+netWeight+"    "+noAart+"    "+offinvAmt+"    "+billbackAmt);
					
					sql = " update despatch set gross_weight = ? , tare_weight = ?,nett_weight = ?,no_art = ?, "
							+" disc_offinv_amt_det = ?, disc_billback_amt_det = ? ,tot_value = ? where desp_id =  ? " ;
					pstmt= conn.prepareStatement( sql );
					pstmt.setDouble( 1, grossWeight );
					pstmt.setDouble( 2, tareWeight );
					pstmt.setDouble( 3, netWeight );
					pstmt.setDouble( 4, noAart );
					pstmt.setDouble( 5, offinvAmt );
					pstmt.setDouble( 6, billbackAmt );
					pstmt.setDouble( 7, totAmt );
					pstmt.setString( 8, tranId );

					pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					//Pavan Rane 11mar19 end
					if("D".equalsIgnoreCase(postUpto) && "N".equalsIgnoreCase(confDespOnPost))
					{
						sordDisList.add(tempMap.get("sord_no").toString());
						errString="";
						//conn.commit(); //commented  by nandkumar gadkari and commit in post order process comp on 08/05/19
					}
					else
					{
						//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
						DespatchConfirm despatchConfirm = new DespatchConfirm();						
						String forcedFlag="N";
						connCP=chaneParnerExist(tranId,xtraParams,conn);						
						if(connCP!=null )
						{    //Modified by Azhar K. on [07-05-2019][Start]
							//errString=despatchConfirm.confirm(tranId, xtraParams, forcedFlag,conn,connCP);
							errString=despatchConfirm.confirm(tranId, xtraParams, forcedFlag,conn,connCP,additionalMap);
							//Modified by Azhar K. on [07-05-2019][End]
							despatchConfirm = null;
						}
						else
						{  
							System.out.println("@@@@@@@@@@@@@@iNSIDE 2294 @@@@@@@@@");
							//Modified by Azhar K. on [07-05-2019][Start]
							//errString=despatchConfirm.confirm(tranId, xtraParams, forcedFlag,conn,connCP);
							errString=despatchConfirm.confirm(tranId, xtraParams, forcedFlag,conn,conn,additionalMap);
							//Modified by Azhar K. on [07-05-2019][End]
							despatchConfirm = null;
						}
						//System.out.println("despatchConfirm return string >>>>"+errString);
						
						if(errString == null || errString.trim().length()==0 || "".equalsIgnoreCase(errString) || errString.contains("CONFSUCC")|| errString.contains("VTPOSTDES") )
						{
							errString = "";
							sordDisList.add(tempMap.get("sord_no").toString());
							if(connCP!=null )
							{
								//connCP.commit();	//connCP.commit();//commented  by nandkumar gadkari and commit in post order process comp on 08/05/19
							}
							
							//conn.commit();//commented  by nandkumar gadkari and commit in post order process comp on 08/05/19
						}
						else
						{
							if(connCP!=null )
							{
								connCP.rollback();	
							}
							
							conn.rollback();
						}
					}
					
					
				}
				else
				{
					//[changed to return errormsg from process req]
					if(lineNum > 0)
					{
						errString = retString;
					}else
					{
						errString = itmDBAccessEJB.getErrorString("","VTDTNTEXST","","",conn);
					}
					//errString = itmDBAccessEJB.getErrorString("","VTDTNTEXST","","",conn);
					conn.rollback();
				}
				
				
			}
			//System.out.println("insertCntHdr==="+insertCntHdr+"@=====@"+"insertCntDet=="+insertCntDet);
			//if(insertCntHdr>0 && insertCntDet>0)
			/*if(splitCodeSize==generatedDesp)
			{
				//VTCONSUCF
				System.out.println(">>The selected transaction is getting saved!!!!");
				errString=	"<?xml version='1.0'?><Root><message>VTCONPARM</message><TranID>"+generatedId+"</TranID></errors></Root>";
				System.out.println("@@@@@2: retString:" + errString);
				if (errString.indexOf("VTCONPARM") > -1)
				{
					System.out.println("retString.indexOf(Success) > -1)");


					String[] arrayForTranId = errString.split("<TranID>");
					System.out.println(">>>In Post order Despatch generation arrayForTranId:[" + arrayForTranId.toString() + "]arrayForTranId.length[" + arrayForTranId.length + "]");
					if (arrayForTranId.length > 1)
					{
						int endIndex = arrayForTranId[1].indexOf("</TranID>");
						tranIdDespatch = arrayForTranId[1].substring(0, endIndex);
					}
					System.out.println("tranIdDespatch for Despatch Creation===="+tranIdDespatch);

					sql = "UPDATE DESPATCH SET TOT_VALUE= ? WHERE DESP_ID= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, sumNetAmt);
					pstmt.setString(2, tranIdDespatch);			              
					pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;

					//conn.commit();
				}
				else
				{
					// conn.rollback();
					System.out.println("RollBack!!!!!!!!!!");
				}


			}
			else
			{
				System.out.println("Record not found!!!!!!!");
			}*/

			// }
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend despatch generation [" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");

		}	

		catch (Exception e) 
		{
			System.out.println("Inside Catch@@@@@@@");
			conn.rollback();

			errString = itmDBAccessEJB.getErrorString("","VTDESPF","","",conn);			
			System.out.println("Exception ::"+e.getMessage());
			//errString = GenericUtility.getInstance().createErrorString(e);
			e.printStackTrace();

			throw new ITMException(e);

			// throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println(">>>>>In finally errString:"+errString);
				/*if(errString != null && errString.trim().length() > 0)
				{
					if(errString.indexOf("VTCONSUCF") > -1)
					{
						conn.commit();
						System.out.println("Commit Completed");
					}
					else
					{
						conn.rollback();
					}
				}*/
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
				if(pstmtdDet !=null)
				{
					pstmtdDet.close();
					pstmtdDet=null;
				}
				if(connCP!=null)
				{
					connCP.close();
					connCP=null;
					
				}
				//conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}


		//return sordDisList;

		return errString;

			}



	private static String generateTranId(String windowName, String userId, Connection conn) throws Exception
	{ 
		//Statement stmt = null;//23feb19[stmt to pstmt changed]
		PreparedStatement pstmt;
		ResultSet rs = null;
		String sql = "";
		String tranId = "";
		String newKeystring = "";
		CommonConstants commonConstants = new CommonConstants();
		try
		{
			//sql = "SELECT KEY_STRING, TRAN_ID_COL, nvl(REF_SER,'PEXRE')  FROM TRANSETUP WHERE TRAN_WINDOW = '"+windowName+"'";
			sql = "SELECT KEY_STRING, TRAN_ID_COL, nvl(REF_SER,'PEXRE')  FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			//stmt = conn.createStatement();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, windowName);
			rs = pstmt.executeQuery();
		//	System.out.println("keyString :"+rs.toString());
			String keyString = "";
			String keyCol = "";
			String tranSer ="";
			if (rs.next())
			{
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer = rs.getString(3);
			}
			rs.close();//23feb19[to close the cursor and pstmt while retuing string]
			rs = null;
			pstmt.close();
			pstmt = null;
			String xmlValues = "";
			//System.out.println("keyCol"+keyCol);
			//System.out.println("keyString"+keyString);
			//System.out.println("tranSer"+tranSer);
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<tran_id></tran_id>" ;
			xmlValues = xmlValues + "</Detail1></Root>";
			//System.out.println("xmlValues for tranid  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, userId, commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);

		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new Exception(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new Exception(e);
		}
	//	System.out.println("tranId :- ["+tranId+"]"); 
		return tranId;
	}

	private String checkNull(String str)
	{
		if (str == null)
		{
			return "";
		}

		return str;
	}

	private double checkDoubleNull(String str)
	{
		if(str == null || str.trim().length() == 0)
		{
			return 0.0;
		}
		else
		{
			return Double.parseDouble(str) ;
		}

	}


	private double checkDoubleNullVal(double str)
	{
		if(str==0)
		{
			return 0.0;
		}

		return str;
	}


	public double calcRate(double rate,double plistDisc)
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
			e.printStackTrace();
		}


		return rate;
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

				sql = "    select case when rate is null then 0 else rate end as rate" +
						" from    pricelist where price_list    = ? and " +
						"    item_code     = ? and unit = ? " +
						" and    list_type    IN    ('M','N') " +
						" and    case when min_qty is null then 0 else min_qty end     <=    ? " +
						" and    ((case when max_qty is null then 0 else max_qty end    >=    ? ) " +
						" OR  (case when max_qty is null then 0 else max_qty end    =0)) and eff_from <=    ?  " +
						" and    valid_upto >=    ? ";
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
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}


		return discPerc;
	}
	// lc_offinv_rate = nvo_fin_inv.gbf_calc_discount_rate(as_sordno,ls_linenoord,ls_itemcode,lc_rtconv * mrate,'O')

	public double getDiscountRate(String saleOrder,String lineNoOrd,String itemCode,double rate,String value,Connection conn) throws SQLException, ITMException
	{


		String schemeCode = "", promoTerm = "",priceVar="",sql="";
		double mrate=0.0, lc_disc=0.0,discPerc=0.0;
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		try
		{
			sql="select scheme_code from sorderdet_scheme where tran_id = ? and line_no_form = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,saleOrder);
			pstmt.setString(2,lineNoOrd);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				schemeCode = rs.getString("scheme_code");
				//System.out.println(" schemeCode========="+ schemeCode);
				if( schemeCode!=null &&  schemeCode.trim().length()>0)
				{
					sql="select promo_term,price_var  from bom  where bom_code = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,saleOrder);
					pstmt1.setString(2,lineNoOrd);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						promoTerm= rs1.getString("promo_term");
						priceVar= rs1.getString("price_var");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					if("D".equalsIgnoreCase("priceVar"))
					{
						mrate= -1;

					}
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}
		return discPerc;
	}



	private String getCurrdateAppFormat() throws ITMException
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//System.out.println(genericUtility.getDBDateFormat());
			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}
		return s;
	}	
	private Connection chaneParnerExist(String despId,String xtraParams,Connection conn)
    {
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String sql="",retString="";
		String purIntegrate="",siteCode="",custCode="";
		String chPartner="",disLink="",dataStr="";
		String filename="",jbossHome="",chanelPartnerFile="";
		int ediOption=0;
		Connection connCP=null;
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = new DistCommon();
		try
		{
			purIntegrate=distCommon.getDisparams("999999", "PUR_INTEGRATED", conn);
			sql="select edi_option from transetup where tran_window = 'w_despatch'";
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				ediOption=rs.getInt(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if("Y".equalsIgnoreCase(purIntegrate) || ediOption > 0)
			{
				sql="select cust_code,site_code from despatch where desp_id=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,despId);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					siteCode=rs.getString("site_code");
					custCode=rs.getString("cust_code");
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				sql="select channel_partner, dis_link "
						+ " from site_customer "
						+ " where cust_code = ? "
						+ " and site_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setString(2,siteCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					chPartner=checkNull(rs.getString("channel_partner"));
					disLink=checkNull(rs.getString("dis_link"));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(chPartner.trim().length()==0)
				{
					sql="select channel_partner, dis_link  from customer "
							+ " where cust_code = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						chPartner=checkNull(rs.getString("channel_partner"));
						disLink=checkNull(rs.getString("dis_link"));
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}
					if("Y".equalsIgnoreCase(chPartner)|| ediOption > 0)
					{
						if (("A".equalsIgnoreCase(disLink)|| "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink) ) && "Y".equalsIgnoreCase(purIntegrate))
						{
							
							String dirPath="";
							if ( CommonConstants.APPLICATION_CONTEXT != null )
							{
								dirPath = CommonConstants.APPLICATION_CONTEXT + CommonConstants.SETTINGS;
								//System.out.println("dirPath1>>>>"+dirPath);
							}
							else
							{
								dirPath = CommonConstants.JBOSSHOME + File.separator + "server" + File.separator + "default" + File.separator + "deploy" + File.separator + "ibase.ear" + File.separator + "ibase.war" + File.separator + CommonConstants.SETTINGS;
								
								//System.out.println("dirPath2>>>>>>"+dirPath);
							}
							File xmlFile = new File( dirPath + File.separator + "DriverITMCP" + ".xml" );
							System.out.println("xmlFile>>>>>"+xmlFile);
							if(xmlFile.exists())
							{
								//if(connCP !=null)
								{
									//System.out.println("file exist new connection is creating");
									connCP=connDriver.getConnectDB("DriverITMCP");
									return connCP;
								}
								
							}
							
						}
							
					}
			
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return connCP;
    }
	private String generateGpNo( String windowName, String siteCode,String salOrder, Connection conn )throws ITMException
    {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "",ordType="",ordTypes="",ordType1="";
		String keyCol = "";
		String xmlValues = "";
		String paySiteCode = "";
		String effectiveDate = "";
		java.sql.Timestamp currDate = null;
		java.sql.Date effDate = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		DistCommon distCommon = new DistCommon();
		 try
         {

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);
			
			selSql = "select order_type from sorder where  sale_order =? ";
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, salOrder );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
					ordType = rs.getString("order_type");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			ordTypes=distCommon.getDisparams("999999", "GP_NO", conn);
			if("NULLFOUND".equalsIgnoreCase(ordTypes))
			{
				ordTypes="";
			}else
			{
				String[] arrStr =ordTypes.split(",");
				int len =arrStr.length;
				for(int i =0;i<len;i++)
				{
					ordType1 =arrStr[i];
					if(ordType1.trim().equalsIgnoreCase(ordType.trim()))
					{
						return tranId;
					}
				}
			}
			
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
			xmlValues = xmlValues +        "<gp_no></gp_no>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
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
     }//generateTranTd()
	//Pavan R 11mar19 processreq start
	/**
	 * Save XML data
	 * @param siteCode
	 * @param xmlString
	 * @param xtraParams
	 * @param conn
	 * @return String
	 * @throws ITMException
	 */
	private String saveData(String siteCode, String xmlString, String xtraParams, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		ibase.utility.UserInfoBean userInfo = new UserInfoBean();
		String chgUser = "", chgTerm = "";
		String loginCode = "", loginEmpCode = "", loginSiteCode = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("xtraParams>>>>" + xtraParams);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userInfo.setEmpCode(loginEmpCode);
			userInfo.setRemoteHost(chgTerm);
			userInfo.setSiteCode(loginSiteCode);
			userInfo.setLoginCode(loginCode);
			userInfo.setEntityCode(loginEmpCode);
			String[] authencate = new String[2];
			authencate[0] = loginCode;
			authencate[1] = "";
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
		} catch (ITMException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}
	//Pavan R 11mar19 processreq end
	//Pavan Rane 11jun19 start [to validate Channel Partner customer]
	private boolean isChannelPartnerCust(String custCode, String siteCode, Connection conn) throws ITMException
	{
		
		String sql = "";
		String disLink = "";
		String chPartner = "";
		boolean cpFlag = false;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try
		{
			sql="select channel_partner, dis_link from site_customer "
					+ " where cust_code = ? and site_code = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,custCode);
			pstmt.setString(2,siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				chPartner=checkNull(rs.getString("channel_partner"));
				disLink=checkNull(rs.getString("dis_link"));
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if(chPartner.trim().length()==0)
			{
				sql="select channel_partner, dis_link  from customer "
						+ " where cust_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					chPartner=checkNull(rs.getString("channel_partner"));
					disLink=checkNull(rs.getString("dis_link"));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}	
			if("Y".equalsIgnoreCase(chPartner))
			{
				if (("A".equalsIgnoreCase(disLink)|| "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink) ))
				{
					cpFlag = true;
				}
			}
			
		} catch (SQLException se)
		{			
			BaseLogger.log("0", null, null, "SQLException :PostOrdDespatchGen :IsCustChannelP()::" + se.getMessage());
			se.printStackTrace();			
		}

		catch (Exception e)
		{
			BaseLogger.log("0", null, null, "Exception :PostOrdDespatchGen :IsCustChannelP()::" + e.getMessage());
			e.printStackTrace();		
		} finally
		{
			try
			{
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
			} catch (Exception e)
			{
			}
		}
		return cpFlag;
	}
	//Pavan Rane 11jun19 end
}