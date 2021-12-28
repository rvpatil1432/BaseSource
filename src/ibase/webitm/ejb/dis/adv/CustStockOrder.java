/********************************************************
	Title : CustStockOrder[D16EBAS009]
	Date  : 27/08/16
 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.dis.DistDiscount;
import ibase.webitm.ejb.sys.UtilMethods;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;

@Stateless
public class CustStockOrder extends ActionHandlerEJB implements CustStockOrderLocal, CustStockOrderRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon= new DistCommon();
	ValidatorEJB validatorEJB = new ValidatorEJB();
	UtilMethods utilMethods = new UtilMethods();
	DistDiscount distDiscount = new DistDiscount();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
	public String submit(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>CustStockOrder submit called>>>>>>>>>>>>>>>>>>>");
		String sql = "",sql1 = "", mtype = "",channelPartner = "" ,varValue = "" ;
		Connection conn = null;
		PreparedStatement pstmt = null ,pstmt1 = null;
		String errString = null;
		ResultSet rs = null ,rs1 = null;
		String custCode = "", siteCode = "",remarks = "",tranIdLast= "",chgUser= "",
		chgTerm = "",descr = "",custName = "",itemSer = "",orderTpe = "",replDate = "";
		Timestamp  tranDate = null, fromDate = null ,toDate = null ,chgDate = null;
		int split = 0 ,mtotRec = 0;
		HashMap custStockMap = new HashMap();
		try
		{

			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);

			sql = " SELECT DISTINCT CUST_STOCK.TRAN_ID,CUST_STOCK.TRAN_DATE,CUST_STOCK.CUST_CODE,CUST_STOCK.SITE_CODE,CUST_STOCK.REMARKS, " +   
			      " CUST_STOCK.TRAN_ID__LAST,CUST_STOCK.FROM_DATE,CUST_STOCK.TO_DATE,CUST_STOCK.CHG_USER, " +  
			      " CUST_STOCK.CHG_TERM ,SITE.DESCR,CUSTOMER.CUST_NAME,CUST_STOCK.CHG_DATE,CUST_STOCK.ITEM_SER,CUST_STOCK.ORDER_TYPE," +
			      " CUST_STOCK.REPL_RATE FROM CUST_STOCK, SITE, CUSTOMER " + 
			      " WHERE ( CUST_STOCK.SITE_CODE = SITE.SITE_CODE ) and  " +
			      " ( CUST_STOCK.CUST_CODE = CUSTOMER.CUST_CODE ) and   " +
			      " ( ( cust_stock.tran_id >= ? ) AND  " +
			      " ( cust_stock.tran_id <= ? ) AND " + 
			      " ( CUST_STOCK.STATUS = 'S' ) AND  " +
			      " ( cust_stock.confirmed = 'Y' ) )";
				  pstmt = conn.prepareStatement(sql);
				  pstmt.setString(1, tranId);
				  pstmt.setString(2, tranId);
				  rs = pstmt.executeQuery();
				  if (rs.next()) 
				  {
						custCode = checkNull(rs.getString("cust_code"));
						tranId = checkNull(rs.getString("tran_id"));
						tranDate = rs.getTimestamp("tran_date");
						siteCode = checkNull(rs.getString("site_code"));
						remarks = checkNull(rs.getString("remarks"));
						tranIdLast = checkNull(rs.getString("tran_id__last"));
						fromDate = rs.getTimestamp("from_date");
						toDate = rs.getTimestamp("to_date");
						chgUser = checkNull(rs.getString("chg_user"));
						chgTerm = checkNull(rs.getString("chg_term"));
						descr = checkNull(rs.getString("descr"));
						custName = checkNull(rs.getString("cust_name"));
						chgDate = rs.getTimestamp("chg_date");
						itemSer = checkNull(rs.getString("item_ser"));
						orderTpe = checkNull(rs.getString("order_type"));
						replDate = checkNull(rs.getString("repl_rate"));
						
						System.out.println("Cust Code["+custCode + "]");
						
						sql1 = "select case when split_factor is null then 0 else split_factor end," +
							   "case when channel_partner is null then 'N' else channel_partner end  " +
							   "from customer where cust_code = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString( 1, custCode );
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							split = rs1.getInt(1);
							channelPartner = checkNull(rs1.getString(2));
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;
						
						System.out.println("split["+split + "]channelPartner["+channelPartner + "]");
						
						if(split == 0)
						{
							errString = itmDBAccess.getErrorString("", "VTNOSPLIT", "","",conn);
							return errString;
						}
						else if("Y".equalsIgnoreCase(channelPartner))
						{
							mtype = "P";
						}
						else
						{
							varValue = distCommon.getDisparams("999999", "REPL_OPT", conn);
							
							if("M".equalsIgnoreCase(varValue))
							{
								mtype = "M";
							}
							else
							{
								mtype = "S";
							}
						}
						System.out.println("varValue["+varValue + "]mtype["+mtype+"]");
						
						custStockMap.put("cust_code",custCode);
						custStockMap.put("tran_id",tranId);
						custStockMap.put("tran_date",tranDate);
						custStockMap.put("site_code",siteCode);
						custStockMap.put("remarks",remarks);
						custStockMap.put("tran_id__last",tranIdLast);
						custStockMap.put("from_date",fromDate);
						custStockMap.put("to_date",toDate);
						custStockMap.put("chg_user",chgUser);
						custStockMap.put("chg_term",chgTerm);
						custStockMap.put("descr",descr);
						custStockMap.put("cust_name",custName);
						custStockMap.put("chg_date",chgDate);
						custStockMap.put("item_ser",itemSer);
						custStockMap.put("order_type",orderTpe);
						custStockMap.put("repl_rate",replDate);
						
						System.out.println("mtype>>"+mtype);
						errString = gbfCustStockGenerate(custStockMap , mtype , mtotRec ,xtraParams , conn);
						
						System.out.println("outside gbfCustStockGenerate>>"+errString);
						if(errString != null && errString.trim().length() > 0)
						{
							return errString; 
						}
						
				}
				else
				{
					errString = itmDBAccess.getErrorString("", "VTNODAT", "","",conn);
					return errString;
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(errString == null || errString.trim().length() == 0)
				{
					errString = itmDBAccess.getErrorString("", "VTORDSUCC", "","",conn);
					return errString; 
				}
			
			
		} catch (Exception e)
		{
			if(conn!=null)
			{
				try 
				{
					conn.rollback();
				} 
				catch (SQLException ex) 
				{

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
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
				if (errString.contains("VTORDSUCC"))
				{
					conn.commit(); 
					System.out.println("@@@@ Transaction commit... ");
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("Last errString ["+errString+"]");
		return errString;

	}
	
	private String gbfCustStockGenerate(HashMap custStockMap, String asType , int i,  String xtraParams ,Connection conn) throws Exception
	{	
		System.out.println("INSIDE gbfCustStockGenerate ......");
		String errString = "",sql = "" ,sqlDet = "",empCode = "" ,userId = "" ,dlvTerm = "" ,replRate = "",
		keyStr = "",ordModeDisparm = "", orderType = "" , tranId = "" ,custCode = "" ,invoiceItemSerHdr = "" , msiteCd = "" , ordMode = "",morderDtApp = "",
		asTypeSaved = "",descr ="" ,descr1 ="", descr2 ="",maddr3 ="",descr3 ="",descr4 ="",mcountry ="", mstan ="", mtran="",mstate ="",tel1 ="",
		tel2 ="",tel3 ="",fax ="",mtransCd ="",currCode ="",bankCode= "",morgTransMode = "",stDescr = "",tranName = "",frtTerm = "",mcrterm ="" ,
		priceListClg="",mitemCd = "",mlocType  = "",lineNo = "",invoiceItemSer= "",munit = "",lsOrderType = "", tempOrderType = "",custItemRef = "",
		mvarValue= "",itemSer = "", itemSerProm = "" ,itemSerCrPerc = "" ,mdescr= "" ,mschemeCd= "", countCode = "",mstateCd = "",schemeCode = "" , 
		prevScheme= "" ,itemStru = "" , sqlNew = "" , mitCode = "",mitFlg = "" ,sqlin = "" ,sqlinner = "" , mslabOn = "",packCode = "",plistDisc = "",
		mpriceList= "",listType = "",mstanCode = "",typeTaxclass = "",mstanCodeSite = "",mtaxClass = "",mtaxChap = "",mtaxEnv = "",itemserProm = "" ,
		mgenTrans = "",morderDtDtr = "",msetLine = "" ,oldInvoiceItemSer = "" , InvoiceItemSer= "" ,locType = "" , itemSerDesc = "",lineNoo = "",
		remarks = "" ,termId = "" ,SplitCodeNew = "" ,crTerm = "" ,cctrcodeSal = "" , xmlString = "",replPeriod="",tempSplitCode = "",splitCode = "",splitCodeIn="";
		String[] acctcodeSal= null;
		
		HashMap tempMap = null;
		HashMap custStockSoGen = null;
		HashMap NewCustStockMap = new HashMap();
		HashMap custStockSoGenNew = new HashMap();
		HashMap NewCustStockNewMap = new HashMap();
		HashMap NewCustStockSoGen = new HashMap();
		
		ArrayList custStockSoGenList = new ArrayList();
		ArrayList custStockSoGenListNew = new ArrayList();
		ArrayList NewCustStockSoGenList = new ArrayList();
		ArrayList NewCustStockMapList = new ArrayList();
		//ArrayList NewCustStockSoGenList = new ArrayList();
		
		ArrayList tempList = null;
		ArrayList temp1List = new ArrayList();
		
		
		Timestamp morderDt = null ; 
		PreparedStatement pstmt = null,pstmt1 =null ,pstmt2 =null ,pstmtDet = null;
		ResultSet rs = null,rs1 =null ,rs2 = null ,rsDet = null;
		StringBuffer xmlBuff = null ,xmlBuffDet = null  ;
		
		String invoiceItemSerArr[]= null ;
		Date mrestUpto = null ;
		
		ArrayList tschdtList = new ArrayList();
		ArrayList schdtList = new ArrayList();
		ArrayList<Double> schpercList = new ArrayList<Double>();
		ArrayList tschpercList = new ArrayList();
		
		Date dateNew = null;
		int cnt1 = 0 ,mcount1 = 0 ,llCnt =0 ,llSchcnt = 0 ,llCount = 0 , k =0 ,updCnt1 = 0 , updCnt = 0,cnt = 0 ;
			double mminQty = 0.0 ,msales = 0.0 ,mclStock = 0.0 ,integralQty = 0.0 ,mintQty = 0.0 ,mmodqty = 0.0,
		perc = 0.0 , qtyPerc = 0.0 ,lcQty = 0.0 ,batchQty = 0.0 ,discMerge = 0.0 ,lcPlistDisc = 0.0 ,mrate = 0.0 ,sorate = 0.0 ,
		exchRate = 0.0,mrateClg = 0.0,mtotOrdAmt = 0.0,mtotTaxAmt = 0.0,mtotNetAmt = 0.0,lcOrderStock = 0.0 ,minOrderQty = 0.0,demandQty= 0.0,
		clStockOrg= 0.0 ,schpercTmp = 0.0 ,clStock = 0.0 ,freeQty = 0.0 ,mtaxAmt = 0.0 ,mordAmt =0.0 ,mnetAmt = 0.0 ,orderStock = 0.0 ,replPrd = 0.0  ;
		Timestamp date = null;
		date = new java.sql.Timestamp(System.currentTimeMillis()) ;
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
		SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
		System.out.println("Date in custstock "+date);
		try 
		{
			empCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); 
			System.out.println("--login code--"+userId + "empCode..."+empCode);
			termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); 
			System.out.println("--term id--"+termId);
			Timestamp sysdate =  java.sql.Timestamp.valueOf(sdf.format(new java.util.Date()).toString() + " 00:00:00.0");
			System.out.println("==tranDate=="+sysdate );
			System.out.println("custStockMap>>["+custStockMap + "]custStockMap.size()["+custStockMap.size()+"]");
			
			
			//Picking DLV TERM from disparm 
			
			dlvTerm = distCommon.getDisparams("999999", "DLV_TERM", conn);
			
			
	
			sql = "select key_string from transetup where upper(tran_window) = 'W_SORDER'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyStr = checkNull(rs.getString("key_string"));
			}
			else
	  		{
	  			sql = "select key_string from transetup where upper(tran_window) = 'GENERAL'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					keyStr = checkNull(rs.getString(1));
				}
	  		}
	  		rs.close();rs = null;
	  		pstmt.close();pstmt = null;
	  		
	  		asTypeSaved = asType	;
	  		
	  		if("P".equalsIgnoreCase(asType))
	  		{
	  			ordModeDisparm = "C";
	  		}
	  		else
	  		{
	  			ordModeDisparm = "A";
	  		}
	  		orderType = "F" ;
	  		
	  		//for(int cnt3 = 0 ;custStockMap.size() > cnt3 ;cnt3++) 
			//{
	  		tranId 				 = (String) custStockMap.get("tran_id");
	  		custCode 			 = (String) custStockMap.get("cust_code");
	  		invoiceItemSerHdr  	 = (String) custStockMap.get("item_ser");
	  		msiteCd  			 = (String) custStockMap.get("site_code");
	  		morderDt  			 = (Timestamp) custStockMap.get("tran_date");
	  		replRate             = (String) custStockMap.get("repl_rate");
	  		System.out.println("replRate"+replRate+"morderDt"+morderDt +"msiteCd"+msiteCd +"invoiceItemSerHdr"+invoiceItemSerHdr);	
	  		
	  		
	  		if("P".equalsIgnoreCase(asType))
		  	{
	  			if(replRate == null || replRate.trim().length() == 0) 
	  			{
	  					replRate = "0";
	  			}
		  	}
		  	else
		  	{
		  			replRate = "0";
		  	}
	  			
	  	    System.out.println("morderDtmorderDtmorderDt"+morderDt);
	  			
	  		sql = "select order_type from customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, custCode );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				orderType = checkNull(rs.getString("order_type"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if("P".equalsIgnoreCase(asType))
	  		{
				sql = "select order_type from cust_stock where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, tranId );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					orderType = checkNull(rs.getString("order_type"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
	  		}
			
			if(orderType == null || orderType.trim().length() == 0)
  			{
  				orderType = "F" ;
  			}
  			
  			tempOrderType = orderType ;
  			
  			sql = "select repl_opt from  customer_series where cust_code = ? and item_ser  =  ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, custCode );
			pstmt.setString( 2, invoiceItemSerHdr );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				asType = checkNull(rs.getString("repl_opt"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
	  			
			if(asType == null || asType.trim().length() == 0)
  			{
  				sql = "select repl_opt from customer where cust_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					asType = checkNull(rs.getString("repl_opt"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				
				
				if(asType == null  || asType.trim().length() == 0)
				{
					asType = distCommon.getDisparams("999999", "REPL_OPT", conn);
					
					if(asType == null  || asType.trim().length() == 0)
					{
						asType = "S";
					}
					if("NULLFOUND".equalsIgnoreCase(asType))
					{
						mvarValue = "S";
					}
				}
				System.out.println("asType:::::["+asType +"]asType["+asType+"]");
  			}
			if(asType != null && asType.trim().length() > 0)
  			{
  				if("P".equalsIgnoreCase(asType))
  				{
  					ordMode = "C";
  				}
  				else
  				{
  					ordMode = "A";
  				}
  			}
  			else
  			{
  				ordMode = ordModeDisparm;
  				asType = asTypeSaved ;
  			}
			sql = " select cust_name,addr1,addr2,addr3,city,pin,count_code,stan_code,tran_code,state_code,tele1,tele2 ," +
	  			  " tele3 ,fax ,trans_mode ,curr_code,bank_code from customer where cust_code =  ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, custCode );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
					descr = checkNull(rs.getString("cust_name"));
					descr1 = checkNull(rs.getString("addr1"));
					descr2 = checkNull(rs.getString("addr2"));
					maddr3 = checkNull(rs.getString("addr3"));
					descr3 = checkNull(rs.getString("city"));
					descr4 = checkNull(rs.getString("pin"));
					mcountry = checkNull(rs.getString("count_code"));
					mstan = checkNull(rs.getString("stan_code"));
					mtran= checkNull(rs.getString("tran_code"));
					mstate = checkNull(rs.getString("state_code"));
					tel1 = checkNull(rs.getString("tele1"));
					tel2 = checkNull(rs.getString("tele2"));
					tel3 = checkNull(rs.getString("tele3"));
					fax = checkNull(rs.getString("fax"));
					mtransCd = checkNull(rs.getString("trans_mode"));
					currCode = checkNull(rs.getString("curr_code"));
					bankCode= checkNull(rs.getString("bank_code"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			//STORING ORIGINAL TRANS MODE
			morgTransMode = mtransCd  ;
			
			System.out.println("STORING ORIGINAL TRANS MODE>["+ morgTransMode +"]");
			
			////Station description
			sql = "select descr from station where stan_code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, mstan );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				stDescr = checkNull(rs.getString("descr"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			System.out.println("Station description>["+ stDescr +"]");
			
		    ////Transporter details
			
			sql = "select tran_name, (case when frt_term is null then 'B' else frt_term end)" +
				  " from transporter where tran_code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, mtran );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				tranName = checkNull(rs.getString("tran_name"));
				frtTerm = checkNull(rs.getString(2));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			System.out.println("Transporter description>["+ tranName +"]frtTerm"+frtTerm);
			
			////Getting the cr_term pick up from cust_ser
			sql = "select cr_term from customer_series where cust_code = ? and item_ser  = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, custCode );
			pstmt.setString( 2, invoiceItemSerHdr );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				mcrterm = checkNull(rs.getString("cr_term"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(mcrterm == null || mcrterm.trim().length() == 0)
			{
				sql = "select cr_term from customer where cust_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					mcrterm = checkNull(rs.getString("cr_term"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			
			System.out.println("Getting the cr_term pick up from cust_ser>["+ mcrterm +"]");
			sql = "select price_list__clg from site_customer where cust_code = ? and site_code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, custCode );
			pstmt.setString( 2, msiteCd );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				priceListClg = checkNull(rs.getString("price_list__clg"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(priceListClg == null || priceListClg.trim().length() == 0)
			{
				sql = "select price_list__clg from customer where cust_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					priceListClg = checkNull(rs.getString("price_list__clg"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			
			if(priceListClg == null || priceListClg.trim().length() == 0)
			{
				priceListClg = distCommon.getDisparams("999999", "PRICE_LIST__CLG", conn);
				
				if("NULLFOUND".equalsIgnoreCase(priceListClg) || priceListClg == null )
				{
					priceListClg = "";
				}
			}
			System.out.println("Getting priceListClg>["+ priceListClg +"]");
			
			sql = " select sch_date__1,sch_date__2 ,sch_date__3 ,sch_date__4,sch_date__5 ,sch_date__6," +
				  " sch_perc__1, sch_perc__2,sch_perc__3 ,sch_perc__4 ,sch_perc__5 ,sch_perc__6   " +
				  " from cust_stock where  tran_id = ?  " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				schdtList.add( rs.getDate("sch_date__1"));
				schdtList.add( rs.getDate("sch_date__2"));
				schdtList.add( rs.getDate("sch_date__3"));
				schdtList.add( rs.getDate("sch_date__4"));
				schdtList.add( rs.getDate("sch_date__5"));
				schdtList.add( rs.getDate("sch_date__6"));
				
				/*schdtList.set(0,rs.getDate("sch_date__1"));
				schdtList.set(1,rs.getDate("sch_date__2"));
				schdtList.set(2,rs.getDate("sch_date__3"));
				schdtList.set(3,rs.getDate("sch_date__4"));
				schdtList.set(4,rs.getDate("sch_date__5"));
				schdtList.set(5,rs.getDate("sch_date__6"));*/
				
				schpercList.add( rs.getDouble("sch_perc__1"));
				schpercList.add( rs.getDouble("sch_perc__2"));
				schpercList.add( rs.getDouble("sch_perc__3"));
				schpercList.add( rs.getDouble("sch_perc__4"));
				schpercList.add( rs.getDouble("sch_perc__5"));
				schpercList.add( rs.getDouble("sch_perc__6"));
				
				/*schpercList.set(0, rs.getDouble("sch_perc__1"));
				schpercList.set(1, rs.getDouble("sch_perc__2"));
				schpercList.set(2, rs.getDouble("sch_perc__3"));
				schpercList.set(3, rs.getDouble("sch_perc__4"));
				schpercList.set(4, rs.getDouble("sch_perc__5"));
				schpercList.set(5, rs.getDouble("sch_perc__6"));*/
				
				
					
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			System.out.println("Array >>"+schdtList +"schperc" +schpercList);
			System.out.println("Array length>>"+schdtList.size() +"schperc" +schpercList.size());
			System.out.println("Array tschdtList>>"+tschdtList.size() +"schperc" +tschpercList.size());
			
			System.out.println("Array BEFORE>>"+tschdtList.size() +"tschpercList" +tschpercList.size());
			for ( k = 0 ; k < schdtList.size(); k++)
			{
				System.out.println("schdt array:["+schdtList.get(k)+"]");
				System.out.println("schperc array:["+schpercList.get(k)+"]");
				if(schdtList.get(k) != null  && schpercList.get(k) != null )
				{
					tschdtList.add(schdtList.get(k)) ;
					tschpercList.add(schpercList.get(k));
				}
			}
	  			
			System.out.println("Array t>>"+tschdtList +"tschpercList" +tschpercList);
			System.out.println("Array lengtht>>"+tschdtList.size() +"tschpercList" +tschpercList.size());
			
			
			schdtList   = tschdtList ;
			schpercList = tschpercList ;
			
			if(morderDt != null)
			{
				dateNew = new Date(morderDt.getTime());
			}
			
			System.out.println("dateNew>>"+dateNew);	
	  			
			if(schdtList.size() == 0)
			{
				/*schdt[1] = dateNew ;
				schperc[1] = 100 ; 
				tschdt = schdt ; 
				tschperc = schperc ; */
				
				schdtList.add(dateNew)  ;
				schpercList.add((double) 100)  ;
				tschdtList = schdtList;
				tschpercList = schpercList;
			}	
			System.out.println("tschdtList >> "+tschdtList);
			System.out.println("tschpercList >> "+tschpercList);
			System.out.println("schpercList >> "+schpercList);
			System.out.println("schdtList >> "+schdtList);
			System.out.println("astype >> "+asType);
			
					
			sqlDet = " select  item_code , loc_type , line_no , item_ser , unit , sales ,  rate ," +
				     " case when ? = 'P' then cl_stock else (cl_stock + transit_qty) end as mclStock,cust_item__ref " +
				     " from cust_stock_det where tran_id = ?  " ;
			pstmtDet = conn.prepareStatement(sqlDet);
			pstmtDet.setString( 1, asType );
			pstmtDet.setString( 2, tranId );
			rsDet = pstmtDet.executeQuery();	
			while(rsDet.next())
			{
				
				mitemCd = checkNull(rsDet.getString("item_code"));
				mlocType = checkNull(rsDet.getString("loc_type"));
				lineNo = rsDet.getString("line_no");
				invoiceItemSer= checkNull(rsDet.getString("item_ser"));
				munit = checkNull(rsDet.getString("unit"));
				msales = rsDet.getDouble("sales");
				sorate = rsDet.getDouble("rate");
				mclStock = rsDet.getDouble("mclStock");
				custItemRef= checkNull(rsDet.getString("cust_item__ref"));
				
				custStockSoGen = new HashMap();
				
				//Added by Poonam on 09-12-2016 for GET REPL_PERIOD FROM DATABASE :Start[D16HVHB005]
				
				
				
				System.out.println("custCode... :"+custCode+":itemCode...:"+mitemCd+":");
				
				System.out.println("In S astype....");
				sql = "select repl_period from  customeritem where cust_code = ? and item_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				pstmt.setString( 2, mitemCd );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					replPeriod = rs.getString("repl_period");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				sql = "Select item_ser from item where item_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, mitemCd );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					itemSer = checkNull(rs.getString("item_ser"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				System.out.println("mvarValue["+replPeriod+"][itemSer"+itemSer+"]");
				
				if(replPeriod == null  || replPeriod.trim().length() == 0)
				{
					sql = "select repl_period from  customer_series where cust_code = ? and item_ser = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, itemSer );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						replPeriod = rs.getString("repl_period");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
				}
				
				if(replPeriod == null  || replPeriod.trim().length() == 0)
				{
					sql = "select repl_period from  customer where cust_code = ?  " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						replPeriod = rs.getString("repl_period");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
				}
				if(replPeriod == null  || replPeriod.trim().length() == 0)
				{
					replPeriod = distCommon.getDisparams("999999", "REPL_PERIOD", conn);
					
					if(replPeriod == null  || replPeriod.trim().length() == 0)
					{
						replPeriod = "1";
					}
					if("NULLFOUND".equalsIgnoreCase(replPeriod))
					{
						replPeriod = "0";
					}
				}
				System.out.println("replPeriod["+replPeriod +"]");
				
				if(replPeriod != null && replPeriod.trim().length() > 0)
				{
					 replPrd = Integer.parseInt(replPeriod);
				}
				System.out.println("replPrd["+replPrd +"]msales before["+msales+"]");
				
				if(msales != 0 && replPrd > 1)
				{
					msales = msales / replPrd ;
					
					msales= Math.round(msales);
				}
				System.out.println("replPrd["+replPrd +"]msales after["+msales+"]");
				
				//Added by Poonam on 09-12-2016 for GET REPL_PERIOD FROM DATABASE :Start[D16HVHB005]
				
				SplitCodeNew = invoiceItemSer  + "@"  + mlocType.trim();
				System.out.println("SplitCodeNew First >>>"+SplitCodeNew);
				
				System.out.println("custStockSoGenList before["+custStockSoGenList+"]\n");
				System.out.println("NewCustStockMap before "+NewCustStockMap+"]\n");
				if (NewCustStockMap.containsKey(SplitCodeNew))
				{
					custStockSoGenList = (ArrayList) NewCustStockMap.get(SplitCodeNew);
					System.out.println("@@@@@ NewCustStockMap in if....."+tempList);
					
				} else
				{
					custStockSoGenList = new ArrayList();
					System.out.println("@@@@@ NewCustStockMap in else....."+tempList);
				}
				
				System.out.println("custStockSoGenList After["+custStockSoGenList+"]\n");
				
				mvarValue = "";
				if("P".equalsIgnoreCase(asType))
  				{
					invoiceItemSer = distCommon.getItemSer(mitemCd, msiteCd, morderDt, custCode, "", conn);
  				}
				else
				{
					invoiceItemSer = distCommon.getItemSer(mitemCd, msiteCd, morderDt, custCode, "C", conn);
  				}
				System.out.println("invoiceItemSer"+invoiceItemSer+"invoiceItemSerArr"+invoiceItemSerArr);
				
				if("S".equalsIgnoreCase(asType))
				{
					sql = "select count(1) from  customer_series where cust_code = ? and item_ser = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, invoiceItemSer );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						cnt = rs.getInt(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					if(cnt == 0)
					{
						System.out.println("in Continue 2");
						continue;
					}
				}
				
				mminQty = 0 ;
				
				if("M".equalsIgnoreCase(asType))
				{
					System.out.println("In M astype....");
					sql = "select min_qty from  customeritem where cust_code = ? and item_code = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, mitemCd );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						mminQty = rs.getDouble("min_qty");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					
				}
				else if("S".equalsIgnoreCase(asType))
				{
					System.out.println("In S astype....");
					sql = "select repl_factor from  customeritem where cust_code = ? and item_code = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, mitemCd );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						mvarValue = rs.getString("repl_factor");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					sql = "Select item_ser from item where item_code = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, mitemCd );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						itemSer = checkNull(rs.getString("item_ser"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					System.out.println("mvarValue["+mvarValue+"][itemSer"+itemSer+"]");
					
					if(mvarValue == null  || mvarValue.trim().length() == 0)
					{
						sql = "select repl_factor from  customer_series where cust_code = ? and item_ser = ? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, custCode );
						pstmt.setString( 2, itemSer );
						rs = pstmt.executeQuery();	
						if(rs.next())
						{
							mvarValue = rs.getString("repl_factor");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
					}
					
					if(mvarValue == null  || mvarValue.trim().length() == 0)
					{
						sql = "select repl_factor from  customer where cust_code = ?  " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, custCode );
						rs = pstmt.executeQuery();	
						if(rs.next())
						{
							mvarValue = rs.getString("repl_factor");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
					}
					if(mvarValue == null  || mvarValue.trim().length() == 0)
					{
						mvarValue = distCommon.getDisparams("999999", "REPL_FACTOR", conn);
						
						if(mvarValue == null  || mvarValue.trim().length() == 0)
						{
							mvarValue = "0";
						}
						if("NULLFOUND".equalsIgnoreCase(mvarValue))
						{
							mvarValue = "0";
						}
					}
					System.out.println("mvarValue["+mvarValue +"]msales["+msales+"]");
					
					
					if(mvarValue != null && mvarValue.trim().length() > 0)
					{
					mminQty = Double.parseDouble(mvarValue) * msales;
					}
					System.out.println("mminQty["+mminQty +"]");
					
				}
				System.out.println("mminQty: "+mminQty  + "mclStock :"+mclStock);
				
				if(!"P".equalsIgnoreCase(asType))
				{
					System.out.println("In !P astype....");
					if(mminQty - mclStock <= 0)
					{
						System.out.println("in Continue 1");
						continue;
					}
					else
					{
						custStockSoGen.put("cl_stock__org", mclStock);
						
						mclStock = mminQty - mclStock ;						
						
						System.out.println("In !P ELSE mclStock...."+mclStock);
						sql = "update cust_stock_det set demand_qty = ? where  tran_id = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDouble( 1, mclStock );
						pstmt.setString( 2, tranId );
						pstmt.setString( 3, lineNo );
						cnt1 = pstmt.executeUpdate();
						System.out.println("Inside sql"+sql);
						System.out.println("Inside cnt1"+cnt1);
						
						custStockSoGen.put("demand_qty", mclStock);
					}
				}
				else if("P".equalsIgnoreCase(asType))
				{
					if(mclStock <= 0)
					{
						continue;
					}
				}
				
				System.out.println("mclStock aftre delete cl_stock"+mclStock);
				
				sql = "select count(*) from  customeritem where cust_code = ? and 	 item_code = ?  " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				pstmt.setString( 2, mitemCd );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					mcount1 = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				System.out.println("mcount1"+mcount1);
				
				if(mcount1 > 0)
				{
					sql = "select restrict_upto from  customeritem where cust_code = ? and 	 item_code = ?  " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, mitemCd );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						mrestUpto = rs.getDate("restrict_upto");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					if(mrestUpto != null)
					{
						if(morderDt.before( mrestUpto))
						{
							continue;
						}
					}
				}
				
				itemSerProm = distCommon.getItemSer(mitemCd, msiteCd, morderDt, custCode, "O", conn);
				
				sql = "select count(*) from  customer_series where cust_code = ? and item_ser = ?  " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				pstmt.setString( 2, itemSerProm);
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					llCnt = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				System.out.println("llCnt"+llCnt);
				
				if(llCnt == 0)
				{
					sql = " select item_ser from  item_credit_perc where item_code = ? and item_ser in ( select item_ser from customer_series" +
						  " where cust_code = ?  and item_ser  = item_credit_perc.item_ser)" ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, mitemCd );
					pstmt.setString( 2, custCode );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						itemSerCrPerc = checkNull(rs.getString("item_ser"));
						itemSerProm = itemSerCrPerc ;
						
						sql = "select item_ser__inv from  customer_series where cust_code = ? and item_ser = ?  " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, custCode );
						pstmt.setString( 2, itemSerProm);
						rs = pstmt.executeQuery();	
						if(rs.next())
						{
							itemSer = checkNull(rs.getString("item_ser__inv"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("itemSer in lpoo"+itemSer);
						invoiceItemSer = itemSer;
						
						System.out.println("invoiceItemSer in lpoo"+invoiceItemSer);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}
				orderType  =  "";
				
				sql = "select order_type from  sordertype where loc_type__parent = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, mlocType );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					orderType = checkNull(rs.getString("order_type"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(orderType != null && orderType.trim().length() > 0)
				{
					lsOrderType = orderType;
				}
				else
				{
					lsOrderType = tempOrderType ;
				}
				
				System.out.println("lsOrderType..."+lsOrderType+"SplitCodeNew BEFORE...."+SplitCodeNew);	
				
				if(mlocType != null && invoiceItemSer != null)
				{
				//SplitCodeNew = invoiceItemSer.trim()  + "@"  + mlocType.trim();
				}
				
				System.out.println("lsOrderType..."+lsOrderType+"SplitCodeNew aFTER...."+SplitCodeNew);	
				
				custStockSoGen.put("order_type", lsOrderType);
				custStockSoGen.put("invoice_item_ser", invoiceItemSer);
				custStockSoGen.put("loc_type", mlocType);
				custStockSoGen.put("line_no", lineNo);
				custStockSoGen.put("item_ser", itemSerProm);
				custStockSoGen.put("item_code", mitemCd);
				custStockSoGen.put("unit", munit);
				custStockSoGen.put("sales", msales);
				custStockSoGen.put("cl_stock", mclStock);
				custStockSoGen.put("rate", sorate);
				custStockSoGen.put("cust_item__ref", custItemRef);
				custStockSoGen.put("order_stock", orderStock);
				
				System.out.println("Inside for for loop ["+custStockSoGenList.size() + "]lsOrderType "+lsOrderType);	
				
				sql = "Select descr from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, mitemCd );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					mdescr = checkNull(rs.getString("descr"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				custStockSoGen.put("descr", mdescr);
				
				mschemeCd = "";
				countCode= "";
				
				sql = "select state_code, count_code from customer where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					mstateCd = checkNull(rs.getString("state_code"));
					countCode = checkNull(rs.getString("count_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				schemeCode = "";
				//schCnt = 0 ;
				
				
				sqlNew = " select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code	= b.scheme_code" +
					     " and a.item_code = ? and a.app_from <= ? and a.valid_upto >= ? and (b.site_code = ? or b.state_code = ? or b.count_code = ? ) ";
				pstmt2 = conn.prepareStatement(sqlNew);
				pstmt2.setString( 1, mitemCd );
				pstmt2.setTimestamp( 2, morderDt );
				pstmt2.setTimestamp( 3, morderDt );
				pstmt2.setString( 4, msiteCd );
				pstmt2.setString( 5, mstateCd );
				pstmt2.setString( 6, countCode );
				rs2 = pstmt2.executeQuery();	
				while(rs2.next())
				{
					mschemeCd = checkNull(rs2.getString(1));
					
					System.out.println("mschemeCd>>"+mschemeCd);
					
					if("P".equalsIgnoreCase(asType))
					{
						sql = " select count(1) from scheme_applicability A,bom b Where  A.scheme_code = b.bom_code And	 B.bom_code = ? " +
							  " And	 ? between case when b.min_qty is null then 0 else b.min_qty end And" +
							  " case when b.max_qty is null then 0 else b.max_qty end";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, mschemeCd );
						pstmt.setDouble( 2, mclStock );
						rs = pstmt.executeQuery();	
						if(rs.next())
						{
							llCnt = rs.getInt(1);
						}
						
					}
					else
					{
						sql = " select count(1) from scheme_applicability A,bom b Where  A.scheme_code = b.bom_code And	 B.bom_code = ? " +
							  " And	 ? between case when (b.min_qty/2) is null then 0 else (b.min_qty/2) end And" +
							  " case when b.max_qty is null then 0 else b.max_qty end";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, mschemeCd );
							pstmt.setDouble( 2, mclStock );
							rs = pstmt.executeQuery();	
							if(rs.next())
							{
								llCnt = rs.getInt(1);
							}
							
					}
					
					System.out.println("llCnt in while loop"+llCnt);
					
					if(llCnt == 0)
					{
						continue;
					}
					
					schemeCode = getCustSchemeCode(custCode, mschemeCd, orderType, conn);
					System.out.println("fetching scheme code from method"+schemeCode);
					
					if(schemeCode != null)
					{
						llSchcnt ++;
					}
					else if(llSchcnt == 1)
					{
						schemeCode	= prevScheme ;
					}
					System.out.println("llSchcnt["+llSchcnt+"]");
					//continue;
					
					if(schemeCode == null || schemeCode.trim().length() == 0)
					{
						schemeCode = checkNull(rs.getString(1));
					}
					else if(!rs.next())
					{
						schemeCode = "";
					}
					else
					{
						break;
					}
					
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					System.out.println("llSchcnt["+llSchcnt+"]");
					continue;
				}
				rs2.close();rs2 = null;
				pstmt2.close();pstmt2 = null;
				
				sql = "select batch_qty from bom where bom_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, schemeCode );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					integralQty = rs.getDouble("batch_qty");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(integralQty > 0)
				{
					if("P".equalsIgnoreCase(asType))
					{
						if(mclStock < integralQty)
						{
							schemeCode = "";
							llSchcnt = 0;
						}
					}
				}
				else
				{
					if(mclStock < (integralQty/2))
					{
						schemeCode = "";
						llSchcnt = 0;
					}
				}
				
				sql = "select (case when item_stru is null then 'S' else item_stru end) from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, mitemCd );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					itemStru = rs.getString(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				
				if("F".equalsIgnoreCase(itemStru) && schemeCode.trim().length() > 0)
				{
					System.out.println("2>>>");
					sqlNew = "select count(*) from scheme_applicability where  item_code =  ? ";
					pstmt1 = conn.prepareStatement(sqlNew);
					pstmt1.setString(1, mitemCd);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						cnt = rs1.getInt(1);
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
					
					if(cnt > 1)
					{
						mitCode = "";
					}
					else
					{
						mitCode = schemeCode ;
					}
				}
				else if(!"F".equalsIgnoreCase(itemStru) && schemeCode.trim().length() > 0)
				{
					mitCode = schemeCode ;
					mitFlg = "B" ;
				}
				else if(!"F".equalsIgnoreCase(itemStru) && (schemeCode == null || schemeCode.trim().length() == 0))
				{
					System.out.println("6>>>["+llSchcnt+"]");
					
					if(llSchcnt >=1)
					{
						mitFlg = "B" ;
						mitCode = schemeCode ;
					}
					else
					{
						mitCode = mitemCd ;
						mitFlg = "I" ;
					}
				}
				
				System.out.println("mitFlg["+mitFlg+"]mitCode["+mitCode+"]");
				
				custStockSoGen.put("scheme_code", mitCode);
				custStockSoGen.put("item_flag", mitFlg);
				
				
				if("I".equalsIgnoreCase(mitFlg))
				{
					mintQty = 0 ;
					System.out.println("In sql>>>>>>");
					sql = "select integral_qty from customeritem where cust_code = ? and item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, mitemCd );
					rs = pstmt.executeQuery();	
					if(rs.next())
					{
						mintQty = rs.getDouble(1);
					}
					else
					{
						System.out.println("In sql");
						sqlin = "select integral_qty from siteitem where site_code = ? and item_code = ? ";
						pstmt1 = conn.prepareStatement(sqlin);
						pstmt1.setString( 1, msiteCd );
						pstmt1.setString( 2, mitemCd );
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							mintQty = rs1.getDouble(1);
						}
						else
						{
							System.out.println("Inner sql");
							sqlinner = "select integral_qty from item where  item_code = ? ";
							pstmt2 = conn.prepareStatement(sqlinner);
							pstmt2.setString( 1, mitemCd );
							rs2 = pstmt2.executeQuery();
							if(rs2.next())
							{
								mintQty = rs2.getDouble(1);
							}
							rs2.close();rs2 = null;
							pstmt2.close();pstmt2 = null;
							
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					System.out.println("OUTSIDE I mintQty"+mintQty);
					
					if(mintQty > 0)
					{
						if("P".equalsIgnoreCase(asType))
						{
							if(mintQty > 0)
							{
								sql = "Select mod(?,?) from dual";
								pstmt = conn.prepareStatement(sql);
								pstmt.setDouble(1, mclStock);
								pstmt.setDouble(2, mintQty);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									mmodqty = rs.getDouble(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								
								if(mmodqty > 0)
								{
									perc = Math.round((mclStock/mintQty)) + 1;
								}
								else
								{
									perc = Math.round((mclStock/mintQty));
								}
								
								mclStock = perc *  mintQty ;
							}
						}
						else
						{
							System.out.println("OUTSIDE I mclStock["+mclStock+"][mintQty"+(mintQty / 2));
							if(mclStock >= (mintQty / 2))
							{
								sql = "Select mod(?,?) from dual";
								pstmt = conn.prepareStatement(sql);
								pstmt.setDouble(1, mclStock);
								pstmt.setDouble(2, mintQty);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									mmodqty = rs.getDouble(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								
								System.out.println("OUTSIDE I mmodqty["+mmodqty+"][mintQty"+(mintQty / 2));
								if(mmodqty > 0)
								{
									qtyPerc =gfGetQtyPerc(custCode,msiteCd,mitemCd ,conn) ;
									lcQty = mintQty * (qtyPerc/100) ;
									
									sql = "Select mod(?,?) from dual";
									pstmt = conn.prepareStatement(sql);
									pstmt.setDouble(1, mclStock);
									pstmt.setDouble(2, mintQty);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										mmodqty = rs.getDouble(1);
									}
									rs.close();rs = null;
									pstmt.close();pstmt = null;
									
									System.out.println("lcQty::::["+lcQty+"]");
									
									if(mmodqty < lcQty)
									{
										perc = Math.round((mclStock/mintQty)) ;
									}
									else
									{
										perc = Math.round((mclStock/mintQty)) + 1;
									}
								}
								else
								{
									perc = Math.round((mclStock/mintQty)) ;
								}
								
								mclStock = perc * mintQty ;
								System.out.println("OUTSIDE I mclStock["+mclStock+"][perc"+(perc)+"mintQty"+mintQty);
							}
						}
					}
					System.out.println("mintQty FOR integral_qty "+mintQty);
					custStockSoGen.put("integral_qty", mintQty);
				}
				else if("B".equalsIgnoreCase(mitFlg))
				{
					if(!"P".equalsIgnoreCase(asType))
					{
						sql = "Select sum(qty_per) from bomdet where  bom_code  = ? and item_code = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitCode);
						pstmt.setString(2, mitemCd);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							batchQty = rs.getDouble(1);
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						System.out.println("batchQty for integral_qty "+batchQty);
						custStockSoGen.put("integral_qty", batchQty);
					}
				}
				
				if(!"P".equalsIgnoreCase(asType))
				{
					if(mitCode != null && mitCode.trim().length() > 0)
					{
						sql = "select slab_on from scheme_applicability where  scheme_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							mslabOn = rs.getString("slab_on");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if("N".equalsIgnoreCase(mslabOn))
						{
							sql = "select count(item_code) from   bomdet where  bom_code = ? and item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mitCode);
							pstmt.setString(2, mitemCd);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								llCount = rs.getInt(1);
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							System.out.println("llCount in bom "+llCount);
							if(llCount > 1)
							{
								sql = "select sum(qty_per) from bomdet where bom_code  = ? and 	 item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mitCode);
								pstmt.setString(2, mitemCd);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									batchQty = rs.getDouble(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								
								custStockSoGen.put("integral_qty", batchQty);
							}
						}
					}
				}
				custStockSoGen.put("cl_stock", mclStock);
				
				sql = "select pack_code from siteitem where  site_code = ? and    item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, msiteCd);
				pstmt.setString(2, mitemCd);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					packCode = checkNull(rs.getString("pack_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(packCode == null || packCode.trim().length() == 0)
				{
					sql = "select pack_code from item where  item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mitemCd);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						packCode = checkNull(rs.getString("pack_code"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}
				
				System.out.println("packCode>>"+packCode);
				
				custStockSoGen.put("pack_code", packCode);
				
				plistDisc = distDiscount.priceListDiscount(msiteCd, custCode, conn);
				System.out.println("plistDisc>>"+plistDisc);
				custStockSoGen.put("price_list__disc", plistDisc);
				
				
		
				mpriceList = distDiscount.priceListSite(msiteCd, custCode, conn);
				System.out.println("mpriceList>>"+mpriceList);
				custStockSoGen.put("price_list", mpriceList);
				
				
				
				discMerge = 0 ;
				listType = distCommon.getPriceListType(plistDisc, conn);
				
				if("N".equalsIgnoreCase(listType))
				{
					lcPlistDisc = 0 ;
				}
				else
				{
					System.out.println("fordistDiscount loop "+lcPlistDisc);
					//listType = distCommon.getPriceListType(plistDisc, conn);
					
					System.out.println("listTypelistType"+listType + "morderDt["+morderDt+"]");
					//lcPlistDisc = distDiscount.getDiscount(plistDisc, morderDt, custCode, msiteCd, mitemCd, munit, discMerge, morderDt, mclStock, conn);
					
					lcPlistDisc = distDiscount.getDiscount(plistDisc, morderDt, custCode, msiteCd, mitemCd, munit, discMerge, morderDt, mclStock, conn);
				}
				System.out.println("lcPlistDisc in order"+lcPlistDisc);
				
				custStockSoGen.put("plist_disc", lcPlistDisc);
						
				sql = "	select stan_code from customer where cust_code = ?";	
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					mstanCode = checkNull(rs.getString("stan_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(morderDt != null)
				{
					morderDtDtr = sdf1.format(morderDt);
				}
				
				System.out.println("morderDt["+morderDt + "]morderDtDtr>>["+morderDtDtr + "]replRate["+replRate +"]");
				
				if("0".equalsIgnoreCase(replRate))
				{
					mrate = distCommon.pickRate(mpriceList, morderDtDtr, mitemCd, "","L",conn);
					listType = distCommon.getPriceListType(mpriceList, conn);
					
					System.out.println("mrate:["+mrate + "]listType:["+listType+"]");
					if(!"L".equalsIgnoreCase(listType) && mrate == -1)
					{
						mrate = 0;
					}
					if(mrate == -1)
					{
						errString = itmDBAccess.getErrorString("", "VTRATE2", "","",conn);
						return errString;
					}
				}
				else
				{
					mrate = sorate ;
				}
				
				sql = "	select tax_class  from sordertype where order_type = ?";	
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, orderType);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					typeTaxclass = checkNull(rs.getString("tax_class"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				sql = "	select stan_code  from site where site_code = ?";	
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, msiteCd);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					mstanCodeSite = checkNull(rs.getString("stan_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(typeTaxclass == null || typeTaxclass.trim().length() == 0 || replRate == "0")
				{
					mtaxClass = distCommon.getTaxClass("C", custCode, mitemCd, msiteCd,conn);
				}
				else
				{
					mtaxClass = typeTaxclass ;
				}
				mtaxChap = distCommon.getTaxChap(mitemCd, invoiceItemSer, "C", custCode, msiteCd,conn);
				mtaxEnv = distCommon.getTaxEnv(mstanCodeSite, mstanCode, mtaxChap, mtaxClass, msiteCd ,conn);
				
				System.out.println("mtaxEnv["+mtaxEnv+"]mtaxChap["+mtaxChap+"]mtaxClass["+mtaxClass+"]");
				
				if(mtaxEnv == null || mtaxEnv.trim().length() == 0)
				{
					errString = itmDBAccess.getErrorString("", "VMTAXENV", "","",conn);
					return errString;
				}
				
				custStockSoGen.put("rate", mrate);
				custStockSoGen.put("tax_chap", mtaxChap);
				custStockSoGen.put("tax_class", mtaxClass);
				custStockSoGen.put("tax_env", mtaxEnv);
				
				sql = "	select udf_str2  from gencodes where fld_name = 'LOC_TYPE' and 	mod_name = 'X' and fld_value = ?";	
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mlocType);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					mgenTrans = checkNull(rs.getString("udf_str2"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(mgenTrans.trim().length() > 0)
				{
					mtransCd = mgenTrans ;
				}
				else
				{
					mtransCd = morgTransMode ;
				}
				custStockSoGen.put("trans_mode", mtransCd);
				
				exchRate = finCommon.getDailyExchRateSellBuy(currCode, "", msiteCd, morderDtDtr, "S", conn);
				custStockSoGen.put("exch_rate", exchRate);
				
				if(priceListClg.trim().length() > 0)
				{
					mrateClg = distCommon.pickRate(priceListClg, morderDtDtr, mitemCd,"","L",mclStock ,conn);
				}
				
				System.out.println("mrateClg >>>"+mrateClg);
				if(mrateClg == -1)
				{
					mrateClg = 0;
				}
				
				System.out.println("mrateClg["+mrateClg+"]");
				if(mrateClg <= 0)
				{
					if(priceListClg.trim().length() > 0)
					{
						custStockSoGen.put("rate__clg", 0);
					}
					else
					{
						custStockSoGen.put("rate__clg", mrate);
					}
				}
				else
				{
					custStockSoGen.put("rate__clg", mrateClg);
				}
				
				custStockSoGenList.add(custStockSoGen);
				System.out.println("custStockSoGenList.size() inside"+custStockSoGenList.size());
				System.out.println("SplitCodeNew >>> aFTER"+SplitCodeNew);
				
				System.out.println("Inside templist size");
				if (NewCustStockMap.containsKey(SplitCodeNew))
				{
					NewCustStockMap.put(SplitCodeNew, custStockSoGenList);
					
				} else
				{
					NewCustStockMap.put(SplitCodeNew, custStockSoGenList);
				}
				System.out.println("NewCustStockMap nEW["+ NewCustStockMap.size()+"]");
			}
			rsDet.close();rsDet = null;
			pstmtDet.close();pstmtDet = null;
			
			System.out.println("custStockSoGenList outside loop"+custStockSoGenList.size());
			System.out.println("NewCustStockMap["+NewCustStockMap.toString()+"]");
			System.out.println("custStockSoGenListNew["+NewCustStockMap.size()+"]");
			System.out.println("custStockSoGenList before remove row"+custStockSoGenList.toString());
			
				
			if(!"P".equalsIgnoreCase(asType))
			{
				System.out.println("Inside ***********"+custStockSoGenList.size());
				if(NewCustStockMap.size() > 0)
				{
					for(k = 1 ; k< schdtList.size(); k++)
					{
						Iterator it = NewCustStockMap.entrySet().iterator();
						while(it.hasNext())
						{
							NewCustStockSoGenList = new ArrayList();
							Map.Entry pairs = (Map.Entry)it.next();
							splitCodeIn = (String) pairs.getKey();
							System.out.println("splitCode>>>["+splitCodeIn +"]");
							int itemIndex = splitCodeIn.indexOf('@');
							
							System.out.println("temp1List before["+temp1List+"]\n");
							
							
							temp1List = (ArrayList) NewCustStockMap.get(splitCodeIn);							
							System.out.println("temp1List from key>>>>[" + temp1List.size() + "]"+"["+temp1List+"]");
							
							for(int p = 0 ;p < temp1List.size() ; p++)
							{
								NewCustStockSoGen = new HashMap();
								System.out.println("INSIDE IF FOR FOR P LOOP");	
								custStockSoGen = new HashMap();
								custStockSoGen = (HashMap) temp1List.get(p);
								
								//NewCustStockSoGen = custStockSoGen;
								mitemCd = (String) custStockSoGen.get("item_code");
								String mitemSr = (String) custStockSoGen.get("item_ser");
								String mLocType = (String) custStockSoGen.get("loc_type");
								
								minOrderQty = gfGetMinOrderQty(custCode,msiteCd,mitemCd ,conn);
								
								if(minOrderQty > 0)
								{
									demandQty = (Double) custStockSoGen.get("demand_qty");
									clStockOrg = (Double) custStockSoGen.get("cl_stock__org");
									
									System.out.println("demandQty:::["+demandQty+"]clStockOrg:::["+clStockOrg+"]");
									
									if(demandQty < minOrderQty)
									{
										continue;
									}
								}
								integralQty= (Double) custStockSoGen.get("integral_qty");
								mclStock= (Double) custStockSoGen.get("cl_stock");							
								System.out.println("integralQty:::["+integralQty+"] mclStock:::["+mclStock+"]schpercList.get(k)["+schpercList.get(k)+"]");
								
								if( mclStock > integralQty)
								{
									mclStock = mclStock * schpercList.get(k) / 100  ;
									
									System.out.println("mclStock:::::::["+mclStock+"]");
									if (integralQty > 0) 
									{
										sql = "Select mod(?,?) from dual";
										pstmt = conn.prepareStatement(sql);
										pstmt.setDouble(1, mclStock);
										pstmt.setDouble(2, integralQty);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											mmodqty = rs.getDouble(1);
										}
										rs.close();rs = null;
										pstmt.close();pstmt = null;
										
										System.out.println("mmodqty:::["+mmodqty+"]");
										
										if(mmodqty > 0)
										{
											mitFlg = (String) custStockSoGen.get("item_flag");
											
											System.out.println("mitFlg:::["+mitFlg+"]");
											
											if("I".equalsIgnoreCase(mitFlg))
											{
												qtyPerc = gfGetQtyPerc(custCode,msiteCd,mitemCd,conn);
												lcQty = integralQty * (qtyPerc/100);
												
												sql = "Select mod(?,?) from dual";
												pstmt = conn.prepareStatement(sql);
												pstmt.setDouble(1, mclStock);
												pstmt.setDouble(2, integralQty);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													mmodqty = rs.getDouble(1);
												}
												rs.close();rs = null;
												pstmt.close();pstmt = null;
												
												System.out.println("mmodqty:::["+mmodqty+"]lcQty:::["+lcQty+"]");
												if(mmodqty < lcQty)
												{
													perc = Math.round((mclStock/integralQty)) ;
												}
												else
												{
													perc = Math.round((mclStock/integralQty)) + 1;
													System.out.println("perc:::"+perc+"]mclStock:::["+mclStock+"]");
												}
											}
											else
											{
												perc = Math.round((mclStock/integralQty)) + 1 ;
											}
										}
										else
										{
											perc = Math.round((mclStock/integralQty)) ;
										}
										mclStock = (perc * integralQty) ;
										
										System.out.println("mclStock["+mclStock+"] integralQty["+integralQty+"] perc["+perc+"] perc * integralQty["+perc * integralQty+"]");
									}
									
									lcOrderStock = (Double) custStockSoGen.get("order_stock");
									custStockSoGen.put("order_stock", lcOrderStock + mclStock);
									
									System.out.println("lcOrderStock["+lcOrderStock+"] lcOrderStock + mclStock["+lcOrderStock + mclStock+"]");
									System.out.println("mclStock["+mclStock+"]");
								}
								NewCustStockSoGenList.add(custStockSoGen);
								NewCustStockNewMap.put(splitCodeIn, NewCustStockSoGenList) ;
							}
						}
				   }
					
					if(NewCustStockNewMap == null || NewCustStockNewMap.size() == 0)
					{
						System.out.println("IN 2ND LOOP....");
						Iterator it = NewCustStockMap.entrySet().iterator();
						while(it.hasNext())
						{
							NewCustStockSoGenList = new ArrayList();
							Map.Entry pairs = (Map.Entry)it.next();
							splitCodeIn = (String) pairs.getKey();
							System.out.println("splitCode>>>["+splitCodeIn +"]");
							int itemIndex = splitCodeIn.indexOf('@');
							System.out.println("temp1List before 2bd loop["+temp1List+"]\n");
							temp1List = (ArrayList) NewCustStockMap.get(splitCodeIn);							
							System.out.println("temp1List from key else>>>>[" + temp1List.size() + "]"+"["+temp1List+"]");
							
							for(int p = 0 ;p < temp1List.size() ; p++)
							{
								NewCustStockSoGen = new HashMap();
								System.out.println("INSIDE IF FOR FOR P LOOP");	
								custStockSoGen = new HashMap();
								custStockSoGen = (HashMap) temp1List.get(p);
								
								NewCustStockSoGenList.add(custStockSoGen);
								NewCustStockNewMap.put(splitCodeIn, NewCustStockSoGenList) ;
							}
						}
						
					}
					
					
					
					
				}
			}
			System.out.println("NewCustStockSoGenList ["+NewCustStockSoGenList.size()+"]\n["+NewCustStockSoGenList.toString()+"]");
			System.out.println("NewCustStockNewMap ["+NewCustStockNewMap.size()+"]\n["+NewCustStockNewMap.toString()+"]");		
			System.out.println("NewCustStockSoGenList size ["+NewCustStockSoGenList.size()+"]");
			System.out.println("NewCustStockSoGenList ["+NewCustStockNewMap.size()+"]\n["+NewCustStockNewMap.toString()+"]");
				
			if(NewCustStockNewMap.size() > 0)
			{
				schpercTmp = (Double) schpercList.get(0) ;
				
				for(k = 0 ; k < schdtList.size() ; k++)
				{
					HashMap splitCodeWiseMap = new HashMap();
					System.out.println("INSIDE NewCust K LOOP"+ k);	
					
					Iterator it = NewCustStockNewMap.entrySet().iterator();
					
					while(it.hasNext())
					{
						Map.Entry pairs = (Map.Entry)it.next();
						
						splitCodeIn = (String) pairs.getKey();
						//temp1List = (ArrayList) pairs.getValue();
						//System.out.println("temp1List from value>>>["+temp1List +"]");
						System.out.println("splitCode>>>["+splitCodeIn +"]");
						int itemIndex = splitCodeIn.indexOf('@');
						
						itemSer = splitCodeIn.substring(0, itemIndex);
						String locTypeParent = splitCodeIn.substring(itemIndex + 1, splitCodeIn.length());
						System.out.println(">>>>>>>>>locTypeParent===" + locTypeParent);
						
						temp1List = (ArrayList) NewCustStockNewMap.get(splitCodeIn);
						
						System.out.println("temp1List from key in 2nd loop>>>>[" + temp1List.size() + "]"+"["+temp1List+"]");
					
						for(int q = 0 ;q < temp1List.size() ; q++)
						{
						System.out.println("INSIDE NewCust Q LOOP"+ q);	
						custStockSoGen = new HashMap();
						custStockSoGen = (HashMap) temp1List.get(q);
						schdtList   = tschdtList ;			
						schpercList = tschpercList ;
						
						if(custStockSoGen.size() > 0)
						{
						System.out.println("custStockSoGen ... ["+custStockSoGen.toString()+"]");
						}
								
						if(!"P".equalsIgnoreCase(asType))
						{
							mitemCd = (String) custStockSoGen.get("item_code");
							minOrderQty = gfGetMinOrderQty(custCode,msiteCd,mitemCd ,conn);
									
							System.out.println("INSIDE IF FOR FOR K with mitemCd :::["+mitemCd+"]minOrderQty["+minOrderQty+"]K["+k+"]");	
							System.out.println("minOrderQty >>"+minOrderQty);
							demandQty = (Double) custStockSoGen.get("demand_qty");
							clStockOrg = (Double) custStockSoGen.get("cl_stock__org");
									
							System.out.println("demandQty >>"+ demandQty + " clStockOrg>>>"+ clStockOrg);
							System.out.println("schdtList[ >>"+ schdtList + "] schpercList>>>"+ schpercList +"]");
							if(minOrderQty > 0 && demandQty < minOrderQty)
							{
								System.out.println("k IN LOOP"+k);
								if(k <= schdtList.size() )
								{
									System.out.println("minschdtList.size()OrderQty >>"+schdtList.size());
									if(clStockOrg < minOrderQty )
									{
										if(k == 0)
										{
											System.out.println("K=====1 >>"+k);
											schdtList.add(schdtList.get(1)) ;
											schpercList.add((double) 100 ) ;
											System.out.println("in sch12131"+schpercList+schdtList);
										}
										else
										{
											System.out.println("K=====1 else>>"+k);
											continue;
										}
									}
									else
									{
										System.out.println("K=====1 else else>>"+k);
										continue;
									}
								}
								else
								{
									if(k == 0)
									{
										System.out.println("K=====1 >>"+k);
										schdtList.add(schdtList.get(schdtList.size())) ;
										schpercList.add((double) 100);
										System.out.println("in sch234"+schpercList+schdtList);
									}
									else
									{
										System.out.println("K=====1222 else>>"+k);
										if( clStockOrg < minOrderQty) 
										{	
											continue;
										}
										else
										{
											schdtList.set(schdtList.size(),schdtList.get(schdtList.size())) ;
											schpercList.set(schdtList.size(),(double) 100 ) ;
											System.out.println("in sch22"+schpercList+schdtList);
										}
									}
								}
							}
							else
							{
								integralQty = (Double) custStockSoGen.get("integral_qty");
								clStock = (Double) custStockSoGen.get("cl_stock");
								lcOrderStock = (Double) custStockSoGen.get("order_stock");//poonam
								
								System.out.println("clStock >>>"+clStock +"lcOrderStock>>>>"+lcOrderStock+"integralQty>>>"+integralQty);
								
								if(clStock <= integralQty)
								{
									if(k == 0)
									{
										schpercList.add((double)100);
									}
									else
									{
										System.out.println("in Continue 5");
										continue ;
									}
								}
								if(k == 0 && (clStock - lcOrderStock <= 0))
								{
									System.out.println("in Continue 6");
									continue ;
								}
							}
						}
						oldInvoiceItemSer = (String) custStockSoGen.get("invoice_item_ser");
						//oldLocType = (String) custStockSoGen.get("loc_type");
									
						System.out.println("tranDate >>>"+morderDt +"SDF FORMat"+sdf.format(morderDt));
						System.out.println("--XML CREATION --");
							    
						orderType = (String) custStockSoGen.get("order_type");
						
						System.out.println("Before DETAIL2 LOG["+ NewCustStockSoGenList.size()+"]orderType"+orderType);
						
						/*for(int j = q ;j< NewCustStockSoGenList.size(); j++)
						{
							System.out.println("iNSIDE DETAIL2 LOG");
							schdtList   = tschdtList ;			
							schpercList = tschpercList ;
							
							custStockSoGen = new HashMap();
							
							custStockSoGen = (HashMap) NewCustStockSoGenList.get(j);
						*/	
							InvoiceItemSer = (String) custStockSoGen.get("invoice_item_ser");
							locType = (String) custStockSoGen.get("loc_type");
									
							lineNo = (String) custStockSoGen.get("line_no");
							
							System.out.println(" "+lineNo);
							
							mclStock = (Double) custStockSoGen.get("cl_stock");
							mrate = (Double) custStockSoGen.get("rate");
							mlocType = (String) custStockSoGen.get("loc_type");
							itemserProm = (String) custStockSoGen.get("item_ser");
							mitemCd = (String) custStockSoGen.get("item_code");
							munit = (String) custStockSoGen.get("unit");
							mdescr = (String) custStockSoGen.get("descr");
							mitCode = (String) custStockSoGen.get("scheme_code");
							mitFlg = (String) custStockSoGen.get("item_flag");
							packCode = (String) custStockSoGen.get("pack_code");
							mpriceList = (String) custStockSoGen.get("price_list");
							plistDisc = (String) custStockSoGen.get("price_list__disc");
							lcPlistDisc = (Double) custStockSoGen.get("plist_disc");
							mtaxChap = (String) custStockSoGen.get("tax_chap");
							mtaxClass = (String) custStockSoGen.get("tax_class");
							mtaxEnv = (String) custStockSoGen.get("tax_env");
							mtransCd = (String) custStockSoGen.get("trans_mode");
							msales = (Double) custStockSoGen.get("sales");
							integralQty = (Double) custStockSoGen.get("integral_qty");
							exchRate = (Double) custStockSoGen.get("exch_rate");
							mrateClg = (Double) custStockSoGen.get("rate__clg");
							custItemRef = (String) custStockSoGen.get("cust_item__ref");	
							
							System.out.println("integralQtyintegralQtyintegralQty["+integralQty+"]asType["+asType+"]mitFlg["+mitFlg+"]");
							if(!"P".equalsIgnoreCase(asType))
							{
								System.out.println("IN ! P LOOP>>>>>"+mclStock);
								mitemCd = (String) custStockSoGen.get("item_code");
								demandQty = (Double) custStockSoGen.get("demand_qty");
								clStockOrg = (Double) custStockSoGen.get("cl_stock__org");
								minOrderQty = gfGetMinOrderQty(custCode,msiteCd,mitemCd,conn);
								
								if(minOrderQty > 0 && demandQty < minOrderQty)
								{
									if(k < schdtList.size() )
									{
										if(clStockOrg < minOrderQty )
										{
											if(k == 0)
											{
												schdtList.add(schdtList.get(0));
												schpercList.add((double) 100);
												System.out.println("in sch21"+schpercList+schdtList);
											}
											else
											{
												continue;
											}
										}
										else
										{
											continue;
										}
									}
									else
									{
										if(k == 0)
										{
											System.out.println("in sch11");
											schdtList.add(schdtList.get(schdtList.size()));
											schpercList.add((double) 100);
											System.out.println("in sch11"+schpercList+schdtList);
										}
										else
										{
											if( clStockOrg < minOrderQty) 
											{
												System.out.println("in Continue 3");
												continue;
											}
											else
											{
												System.out.println("in sch12");
												schdtList.add(schdtList.get(schdtList.size()));
												schpercList.add((double) 100);
												System.out.println("in sch12"+schpercList+schdtList);
											}
										}
									}
								}
								else
								{
									integralQty = (Double) custStockSoGen.get("integral_qty");
									clStock = (Double) custStockSoGen.get("cl_stock");
									lcOrderStock = (Double) custStockSoGen.get("order_stock");//poonam
									
									System.out.println("integralQty"+integralQty+"clStock"+clStock+"lcOrderStock"+lcOrderStock);
									
									if(clStock <= integralQty)
									{
										if(k == 0)
										{
											schpercList.add((double) 100);
										}
										else
										{
											System.out.println("in Continue 4");
											continue ;
										}
									}
									else
									{
										schpercList.add(schpercTmp);
									}
									System.out.println("schpercList"+schpercList.toString());
								}
								
							}
							
							if("P".equalsIgnoreCase(asType))
							{
								if( k == schdtList.size())
								{
									mclStock = (Double) custStockSoGen.get("cl_stock");
									lcOrderStock = (Double) custStockSoGen.get("order_stock");// poonam
									
									mclStock = mclStock  - lcOrderStock ;
									
									System.out.println("mclStock111>>>>>"+mclStock);
											
								}
								else
								{
									mclStock = mclStock * (schpercList.get(k) / 100);
									System.out.println("mclStock12321>>>>>"+mclStock);
								}
								
								if(integralQty > 0)
								{
									if( k == schdtList.size())
									{
										sql = "Select mod(?,?)  from dual";
										pstmt = conn.prepareStatement(sql);
										pstmt.setDouble(1, mclStock);
										pstmt.setDouble(2, integralQty);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											mmodqty = rs.getDouble(1);
										}
										rs.close();rs = null;
										pstmt.close();pstmt = null;
										
										System.out.println("mmodqty...."+mmodqty);
										if(mmodqty > 0)
										{
											perc = Math.round((mclStock/integralQty))+1 ;
										}
										else
										{
											perc = Math.round((mclStock/integralQty)) ;
										}
										System.out.println("perc...."+perc);
										System.out.println("mclStock...."+mclStock+"integralQty/...."+integralQty);
									}
									else
									{
										perc = Math.round((mclStock/integralQty)) ;
									}
									mclStock = perc * integralQty ;
									System.out.println("mclStock///...."+mclStock+"integralQty...."+integralQty+"perc..."+perc);
								}
								custStockSoGen.put("order_stock", lcOrderStock + mclStock);
							}
							else
							{
								if( k == 0)
								{
									mclStock = (Double) custStockSoGen.get("cl_stock");
									lcOrderStock = (Double) custStockSoGen.get("order_stock");//poonam
									mclStock = mclStock  - lcOrderStock ;
									
									System.out.println("mclStock222>>>>>"+mclStock);
								}
								else
								{
									double m2 =0 ;
									System.out.println("schpercList.get(k).toString()>>>>>"+schpercList.get(k).toString());
									String m1 = schpercList.get(k).toString();
									if(m1 != null && m1.trim().length() >0)
									{
										m2 = Double.parseDouble(m1);
									}
									
									System.out.println("m2>>>>>"+m2);
									
									mclStock = mclStock * (m2/ 100);
									
									System.out.println("mclStock22211>>>>>"+mclStock);
								}
								
								if(integralQty > 0)
								{
									sql = "Select mod(?,?)  from dual";
									pstmt = conn.prepareStatement(sql);
									pstmt.setDouble(1, mclStock);
									pstmt.setDouble(2, integralQty);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										mmodqty = rs.getDouble(1);
									}
									rs.close();rs = null;
									pstmt.close();pstmt = null;
									
									System.out.println("mmodqty111>>>>>"+mmodqty+"mitFlg"+mitFlg);
										
									if(mmodqty > 0)
									{
										if("I".equalsIgnoreCase(mitFlg))
										{
											qtyPerc = gfGetQtyPerc(custCode,msiteCd,mitemCd,conn);
											
											System.out.println("qtyPerc weqwrwe>>"+qtyPerc +"integralQty"+integralQty);
											
											lcQty = integralQty * (qtyPerc/100) ;
											
											System.out.println("lcQty lcQty>>"+lcQty);
											sql = "Select mod(?,?)  from dual";
											pstmt = conn.prepareStatement(sql);
											pstmt.setDouble(1, mclStock);
											pstmt.setDouble(2, integralQty);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												mmodqty = rs.getDouble(1);
											}
											rs.close();rs = null;
											pstmt.close();pstmt = null;
											
											System.out.println("mmodqty mmodqty>>"+mmodqty);
											System.out.println("lcQty.....>>"+lcQty);
											if(mmodqty < lcQty)
											{
												perc = Math.round((mclStock/integralQty)) ;
											}
											else
											{
												perc = Math.round((mclStock/integralQty)) +1 ;
											}
										}
										else
										{
											perc = Math.round((mclStock/integralQty)) +1 ;
										}
					
									}
									else
									{
										perc = Math.round((mclStock/integralQty)) ;
									}
									mclStock = perc * integralQty ;
									System.out.println("mclStock mmodqty>>["+mclStock +"]perc["+perc+"]integralQty["+integralQty+"]");
								}
								
								System.out.println("perc11>>>>>"+perc);
								System.out.println("integralQty111>>>>>"+integralQty);
								System.out.println("mclStock1>>>>>"+mclStock);
							}
							if("B".equalsIgnoreCase(mitFlg))
							{
								sql = "Select sum(qty_per) from bomdet where bom_code = ? and item_code = ?  and nature = 'F' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mitCode);
								pstmt.setString(2, mitemCd);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									freeQty = rs.getDouble(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								
								if(integralQty > 0 && freeQty > 0)
								{
									mclStock =  mclStock - ( mclStock / integralQty * freeQty) ;
								}
								
								System.out.println("mclStock>>>>>"+mclStock);
							}
							
							System.out.println("mclStockoo>>>>>"+mclStock);
							if (mclStock <= 0 )
							{
								continue;
							}
							
							System.out.println("mclStock*****>>>>>"+mclStock);
							
							System.out.println("@@@@@ :Inside else.....");
							System.out.println("@@@@@ :Inside locType.trim().....["+locType.trim()+"]itemserProm["+itemserProm+"]");
							tempSplitCode = itemserProm.trim()  + "@"  + locType.trim();
							
							//splitCodeWiseMap = new HashMap();
							
							System.out.println("tempSplitCode......"+tempSplitCode);
							System.out.println("splitCodeWiseMap"+splitCodeWiseMap.size());
							System.out.println("tempList before["+tempList+"][tempList["+tempList +"]\n");
							if (splitCodeWiseMap.containsKey(tempSplitCode))
							{
								tempList = (ArrayList) splitCodeWiseMap.get(tempSplitCode);
								System.out.println("@@@@@ tempList in if....."+tempList);
								
							} else
							{
								tempList = new ArrayList();
								System.out.println("@@@@@ tempList in else....."+tempList);
							}
							
							System.out.println("tempList after["+tempList+"][tempList["+tempList +"]");
							tempMap = new HashMap();
							
							System.out.println("LINE NO PLAN["+lineNo+"][LINE NO SET["+msetLine +"]");
							
							mclStock = Math.round(mclStock) ;
							
							System.out.println("mclStock IN TEMPMAP*****>>>>>"+mclStock);
							
							System.out.println("lineNo["+lineNo+"]");							
							tempMap.put("item_code", mitemCd);
							tempMap.put("item_ser", itemserProm);
							tempMap.put("loc_type", locType);
							tempMap.put("unit", munit);
							tempMap.put("item_ser__prom", itemSerProm);
							tempMap.put("Site_code__ship", msiteCd); 
							tempMap.put("cust_item__ref", custItemRef);
							tempMap.put("line_no",lineNo );
							tempMap.put("cr_term", mcrterm);
							tempMap.put("site_code", msiteCd); 
							tempMap.put("item_flg", mitFlg);
							tempMap.put("item_code__ord", mitCode);
							tempMap.put("item_descr", mdescr);
							tempMap.put("quantity__stduom", mclStock);
							tempMap.put("discount", lcPlistDisc);
							tempMap.put("unit__std", munit);
							tempMap.put("unit__rate", munit);
							tempMap.put("status", "N");
							tempMap.put("status_date", morderDt);
							tempMap.put("conv__qty_stduom", 1); 
							tempMap.put("conv__rtuom_stduom", "1"); 
							tempMap.put("pack_code", packCode);
							tempMap.put("price_list__disc", plistDisc);
							tempMap.put("rate__clg", mrateClg);
							tempMap.put("rate", mrate);
							tempMap.put("rate__stduom", mrate); 
							tempMap.put("tax_chap", mtaxChap);
							tempMap.put("tax_class", mtaxClass);
							tempMap.put("tax_env", mtaxEnv);
							tempMap.put("net_amt", mnetAmt);
							tempMap.put("ord_amt", mordAmt);
							tempMap.put("dsp_date", schdtList.get(k));
							tempMap.put("chg_user", userId);
							tempMap.put("chg_date", sysdate);
							tempMap.put("chg_term",termId);
							tempMap.put("quantity",mclStock);
							tempMap.put("quantity__stduom",mclStock);

							System.out.println("Print tempMap{"+ tempMap +"}schdtList.get(k)["+schdtList.get(k)+"]");
							System.out.println("sales parson = " + tempMap.size() );

							tempList.add(tempMap);
							
							System.out.println("Print Templist["+ tempList  + "]");
							System.out.println(" Templist["+ tempList.size()  + "]");
							System.out.println(" splitCodeWiseMap   ["+ splitCodeWiseMap + "]");
							
							System.out.println("Inside templist size");
							if (splitCodeWiseMap.containsKey(tempSplitCode))
							{
								splitCodeWiseMap.put(tempSplitCode, tempList);
							} else
							{
								splitCodeWiseMap.put(tempSplitCode, tempList);
							}
							System.out.println("Print splitCodeWiseMap"+ splitCodeWiseMap);
							System.out.println("Print splitCodeWiseMap>>["+ splitCodeWiseMap.size()+"]");
						//}
							
					}
				}
				System.out.println("INSIDE splitCodeWiseMap...."+splitCodeWiseMap.toString());
				System.out.println("Outside splitCodeWiseMap...."+splitCodeWiseMap.size());
				Set setItem = splitCodeWiseMap.entrySet();
				System.out.println("setItemsetItem>>"+setItem.size());
				System.out.println("setItemsetItem  >>"+setItem);
				tempList = null;
				Iterator itrItem = setItem.iterator();
				int cnt2 = 0 ;
				System.out.println("tempList before while >>>["+tempList +"]"+itrItem.toString());
					
				int w = 0 ;
				String ordType = "";
				while (itrItem.hasNext())
				{
						
						System.out.println("COUNT OF WHILE FOR " + w );
						mtotOrdAmt = 0 ;
						mtotTaxAmt = 0 ;
						mtotNetAmt = 0 ;
						
						System.out.println("------------------while(itrItem.hasNext())--------"+w);
						Map.Entry itemMapEntry = (Map.Entry) itrItem.next();
						splitCode = (String) itemMapEntry.getKey();
						
						System.out.println("splitCode>>>["+splitCode +"]");
						
						int itemIndex = splitCode.indexOf('@');
						
						itemSer = splitCode.substring(0, itemIndex);
					
						String locTypeParent = splitCode.substring(itemIndex + 1, splitCode.length());
						System.out.println(">>>>>>>>>locTypeParent===" + locTypeParent);
						
						System.out.println("INSIDE WHILE....");
						System.out.println("SDF DATE"+sdf.format(morderDt));
						System.out.println("SDF1 DATE"+sdf1.format(morderDt));
						
						if(morderDt != null)
						{
							 morderDtApp = sdf1.format(morderDt);
						}
								
						System.out.println("@@@@@@@ [ splitCode[" + splitCode + "]");
						System.out.println("itemSer[" + itemSer + "]");
						System.out.println("crTerm[" + crTerm + "]"+mcrterm);
					
						tempList = (ArrayList) splitCodeWiseMap.get(splitCode);
						
						System.out.println("tempList111>>>>[" + tempList.size() + "]"+"["+tempList+"]");
						
						sql = "  select order_type from sordertype where loc_type__parent=?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, locTypeParent);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							ordType = checkNull(rs1.getString("order_type"));
						}

						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;
						if (ordType == null || ordType.trim().length() == 0)
						{
							ordType = orderType;
						}
						
						System.out.println("ordType>>>>"+ordType);
						
						xmlBuff = null;	
						xmlBuff = new StringBuffer();
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
						xmlBuff.append("<order_type><![CDATA["+ checkNull(ordType)+"]]></order_type>");
						//xmlBuff.append("<sale_order><![CDATA["+ checkNull(msetTranId )+"]]></sale_order>");
						xmlBuff.append("<sale_order/>");
						xmlBuff.append("<order_date><![CDATA["+ morderDtApp +"]]></order_date>");
						xmlBuff.append("<site_code><![CDATA["+ checkNull(msiteCd) +"]]></site_code>");
						xmlBuff.append("<item_ser><![CDATA["+ itemSer +"]]></item_ser>");
						xmlBuff.append("<dlv_to><![CDATA["+ checkNull(descr )+"]]></dlv_to>");
						xmlBuff.append("<dlv_add1><![CDATA["+ checkNull(descr1 )+"]]></dlv_add1>");
						xmlBuff.append("<dlv_add2><![CDATA["+ checkNull(descr2) +"]]></dlv_add2>");
						xmlBuff.append("<dlv_add3><![CDATA["+ checkNull(maddr3) +"]]></dlv_add3>");
						xmlBuff.append("<dlv_city><![CDATA["+ checkNull(descr3) +"]]></dlv_city>");
						xmlBuff.append("<dlv_pin><![CDATA["+ checkNull(descr4) +"]]></dlv_pin>");
						xmlBuff.append("<count_code__dlv><![CDATA["+ checkNull(mcountry) +"]]></count_code__dlv>");
						xmlBuff.append("<tran_code><![CDATA["+ checkNull(mtran) +"]]></tran_code>");
						xmlBuff.append("<stan_code><![CDATA["+ checkNull(mstan) +"]]></stan_code>");
						xmlBuff.append("<state_code__dlv><![CDATA["+ checkNull(mstate) +"]]></state_code__dlv>");
						xmlBuff.append("<tel1__dlv><![CDATA["+ checkNull(tel1) +"]]></tel1__dlv>");
						xmlBuff.append("<tel2__dlv><![CDATA["+ checkNull(tel2) +"]]></tel2__dlv>");
						xmlBuff.append("<tel3__dlv><![CDATA["+ checkNull(tel3) +"]]></tel3__dlv>");
						xmlBuff.append("<fax__dlv><![CDATA["+ checkNull(fax) +"]]></fax__dlv>");
						xmlBuff.append("<station_descr><![CDATA["+ checkNull(stDescr) +"]]></station_descr>");
						xmlBuff.append("<tran_name><![CDATA["+ checkNull(tranName) +"]]></tran_name>");
						xmlBuff.append("<frt_term><![CDATA["+ checkNull(frtTerm) +"]]></frt_term>");
					
						cctrcodeSal = gbfAcctDetrInvoice("",oldInvoiceItemSer, "S-INV",orderType ,"SAL",conn);
						
						String[] acctcodeSalArr = cctrcodeSal.split(",");
						
						System.out.println("cctrcodeSal["+cctrcodeSal+"]acctcodeSal["+acctcodeSal+"][acctcodeSalArr"+acctcodeSalArr[0]+"]");
						xmlBuff.append("<acct_code__sal><![CDATA["+ acctcodeSalArr[0] +"]]></acct_code__sal>");
						xmlBuff.append("<cctr_code__sal><![CDATA["+ acctcodeSalArr[1] +"]]></cctr_code__sal>");
						
						xmlBuff.append("<site_code__ship><![CDATA["+ msiteCd +"]]></site_code__ship>");
						xmlBuff.append("<state_code__dlv><![CDATA["+ mstateCd +"]]></state_code__dlv>");
						xmlBuff.append("<cust_code><![CDATA["+ custCode +"]]></cust_code>");
						xmlBuff.append("<cust_code__dlv><![CDATA["+ custCode +"]]></cust_code__dlv>");
						xmlBuff.append("<cust_code__bil><![CDATA["+ custCode +"]]></cust_code__bil>");
						xmlBuff.append("<cr_term><![CDATA["+ mcrterm +"]]></cr_term>");
						xmlBuff.append("<price_list__clg><![CDATA["+ priceListClg +"]]></price_list__clg>");
						xmlBuff.append("<trans_mode><![CDATA["+ mtransCd +"]]></trans_mode>");
						xmlBuff.append("<curr_code><![CDATA["+ currCode +"]]></curr_code>");
						xmlBuff.append("<curr_code__frt><![CDATA["+ currCode +"]]></curr_code__frt>");
						xmlBuff.append("<curr_code__ins><![CDATA["+ currCode +"]]></curr_code__ins>");
						xmlBuff.append("<exch_rate><![CDATA["+ exchRate +"]]></exch_rate>");
						xmlBuff.append("<status><![CDATA["+ "P" +"]]></status>");
						xmlBuff.append("<confirmed><![CDATA["+ "Y" +"]]></confirmed>");
						xmlBuff.append("<tax_date><![CDATA["+morderDtApp +"]]></tax_date>");
						
						xmlBuff.append("<pl_date><![CDATA["+morderDtApp +"]]></pl_date>");
						xmlBuff.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
						xmlBuff.append("<chg_date><![CDATA["+sdf.format(sysdate) +"]]></chg_date>");
						xmlBuff.append("<chg_term><![CDATA["+termId +"]]></chg_term>");
						xmlBuff.append("<tax_opt><![CDATA["+ "L" +"]]></tax_opt>");
						xmlBuff.append("<order_mode><![CDATA["+ordMode +"]]></order_mode>");
						xmlBuff.append("<status_date><![CDATA["+morderDtApp +"]]></status_date>");
						xmlBuff.append("<emp_code__ord><![CDATA["+ empCode +"]]></emp_code__ord>");
						xmlBuff.append("<dlv_term><![CDATA["+dlvTerm +"]]></dlv_term>");
						if(schdtList != null)
						{
						xmlBuff.append("<due_date><![CDATA["+sdf1.format(schdtList.get(k)) +"]]></due_date>");
						}
						xmlBuff.append("<udf__str1><![CDATA["+ tranId +"]]></udf__str1>");
						xmlBuff.append("<price_list__disc><![CDATA["+plistDisc +"]]></price_list__disc>");
						xmlBuff.append("<price_list><![CDATA["+mpriceList +"]]></price_list>");
						xmlBuff.append("<bank_code><![CDATA["+bankCode +"]]></bank_code>");
						
						System.out.println("@@@@@@@ [splitCode[" + splitCode + "]");
						
						tempList = (ArrayList) splitCodeWiseMap.get(splitCode);
						
						System.out.println("tempList before loop"+tempList.size());
						String lineNoNewStr = "";
					    int  lineNoNew = 1 ;
						
						xmlBuffDet = new StringBuffer();
						for (int itemCtr = 0; itemCtr < tempList.size(); itemCtr++)
						{
							System.out.println("Inside itemCtr >> detail2");
							tempMap = (HashMap) tempList.get(itemCtr);						
								
							lineNoNewStr = getLineNewNo(Integer.toString(lineNoNew));
	
							xmlBuffDet.append("<Detail2 dbID='' domID=\""+lineNoNew +"\" objName=\"sorder\" objContext=\"2\">"); 
							xmlBuffDet.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
							xmlBuffDet.append("<sale_order/>");
							//xmlBuffDet.append("<sale_order><![CDATA["+msetTranId +"]]></sale_order>");
							xmlBuffDet.append("<line_no><![CDATA["+lineNoNewStr +"]]></line_no>");
							xmlBuffDet.append("<site_code><![CDATA["+(String)tempMap.get("site_code") +"]]></site_code>");
							xmlBuffDet.append("<item_code__ord><![CDATA["+(String) tempMap.get("item_code") +"]]></item_code__ord>");
							xmlBuffDet.append("<item_code><![CDATA["+(String) tempMap.get("item_code") +"]]></item_code>");
							xmlBuffDet.append("<item_descr><![CDATA["+(String) tempMap.get("item_descr") +"]]></item_descr>");
							xmlBuffDet.append("<item_ser><![CDATA["+ (String) tempMap.get("item_ser") +"]]></item_ser>");
							xmlBuffDet.append("<item_ser__prom><![CDATA["+itemserProm +"]]></item_ser__prom>");
							
							sql = "Select descr from itemser where item_ser = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemserProm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								itemSerDesc =checkNull(rs.getString("descr"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							xmlBuffDet.append("<itemser_descr><![CDATA["+itemSerDesc +"]]></itemser_descr>");
							xmlBuffDet.append("<quantity><![CDATA["+(Double)tempMap.get("quantity") +"]]></quantity>");
							xmlBuffDet.append("<quantity__stduom><![CDATA["+(Double)tempMap.get("quantity__stduom") +"]]></quantity__stduom>");
							//xmlBuffDet.append("<chg_user><![CDATA["+userId +"]]></chg_user>");
							//xmlBuffDet.append("<chg_term><![CDATA["+ termId +"]]></chg_term>");
							//xmlBuffDet.append("<chg_date><![CDATA["+sdf.format(sysdate) +"]]></chg_date>");
							//xmlBuffDet.append("<chg_date><![CDATA["+morderDtApp +"]]></chg_date>");
							
							xmlBuffDet.append("<item_flg><![CDATA["+(String) tempMap.get("item_flg") +"]]></item_flg>");
							xmlBuffDet.append("<unit><![CDATA["+(String) tempMap.get("unit") +"]]></unit>");
							xmlBuffDet.append("<unit__std><![CDATA["+(String) tempMap.get("unit__std") +"]]></unit__std>");
							xmlBuffDet.append("<unit__rate><![CDATA["+(String) tempMap.get("unit__rate") +"]]></unit__rate>");
							xmlBuffDet.append("<status><![CDATA["+'N' +"]]></status>");
							xmlBuffDet.append("<status_date><![CDATA["+ morderDtApp +"]]></status_date>");
							xmlBuffDet.append("<conv__qty_stduom><![CDATA["+ 1 +"]]></conv__qty_stduom>");
							xmlBuffDet.append("<conv__rtuom_stduom><![CDATA["+ 1 +"]]></conv__rtuom_stduom>");
							xmlBuffDet.append("<pack_code><![CDATA["+(String) tempMap.get("pack_code") +"]]></pack_code>");
							xmlBuffDet.append("<loc_type><![CDATA["+(String) tempMap.get("loc_type") +"]]></loc_type>");
							xmlBuffDet.append("<discount><![CDATA["+(Double) tempMap.get("discount")  +"]]></discount>");
							xmlBuffDet.append("<rate><![CDATA["+(Double) tempMap.get("rate") +"]]></rate>");
							xmlBuffDet.append("<rate__stduom><![CDATA["+(Double) tempMap.get("rate__stduom") +"]]></rate__stduom>");
							xmlBuffDet.append("<rate__clg><![CDATA["+(Double) tempMap.get("rate__clg") +"]]></rate__clg>");
							xmlBuffDet.append("<tax_chap><![CDATA["+(String) tempMap.get("tax_chap") +"]]></tax_chap>");
							xmlBuffDet.append("<tax_class><![CDATA["+(String) tempMap.get("tax_class") +"]]></tax_class>");
							xmlBuffDet.append("<tax_env><![CDATA["+(String) tempMap.get("tax_env") +"]]></tax_env>");
							xmlBuffDet.append("<dsp_date><![CDATA["+ schdtList.get(k) +"]]></dsp_date>");
							xmlBuffDet.append("<cust_item__ref><![CDATA["+(String) tempMap.get("cust_item__ref") +"]]></cust_item__ref>");
							
							remarks = tranId + "  "+lineNoNewStr;
							xmlBuffDet.append("<remarks><![CDATA["+remarks +"]]></remarks>");
								
							//msetLine = getLineNewNo(msetLine);
							//System.out.println("msetLine>>>>"+msetLine);
							
							/*lineNo = (String)tempMap.get("line_no");
							System.out.println("lineNo>>>>"+lineNo);
							
							sql = "update cust_stock_det  set sale_order = ? where tran_id = ?  and line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, msetTranId);	
							pstmt.setString(2, tranId);	
							pstmt.setString(3, lineNo);	
							updCnt1 = pstmt.executeUpdate();
							pstmt.close();pstmt = null;
							
							System.out.println("updCnt1>>"+updCnt1);*/
							
							sql = "Select cr_term_map from cr_term_mapping where ( cr_term = ? )  and    ( ord_type = ? ) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mcrterm);
							pstmt.setString(2, orderType);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								crTerm = rs.getString("cr_term_map");
								mcrterm = crTerm ;
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							xmlBuff.append("<cr_term><![CDATA["+mcrterm +"]]></cr_term>");
									
							mclStock = (Double)tempMap.get("quantity");
							mrate    = (Double)tempMap.get("rate")    ;
								
							System.out.println("(mclStock)"+(mclStock));
							System.out.println("(mrate)"+( mrate))  ;
							System.out.println("(mclStock * mrate)"+(mclStock * mrate));
								
							mordAmt = (mclStock * mrate);
							mnetAmt = mordAmt + mtaxAmt ;
								
							System.out.println("(mordAmt)"+(mordAmt));
							System.out.println("(mtaxAmt* mrate)"+( mtaxAmt));
							System.out.println("(mordAmt + mtaxAmt)"+(mordAmt + mtaxAmt));
								
							mtotOrdAmt = mtotOrdAmt + mordAmt ;
							mtotTaxAmt = mtotTaxAmt + mtaxAmt ;
							mtotNetAmt = mtotNetAmt + mnetAmt ;
								
							System.out.println("mordAmt["+mordAmt+"]mnetAmt["+mnetAmt+"]");
							System.out.println("mtotOrdAmt["+mtotOrdAmt+"]mtotTaxAmt["+mtotTaxAmt+"]mtotNetAmt["+mtotNetAmt+"][");
							xmlBuffDet.append("<tax_amt><![CDATA["+mtaxAmt +"]]></tax_amt>");
							xmlBuffDet.append("<net_amt><![CDATA["+mnetAmt +"]]></net_amt>");
							xmlBuffDet.append("<chg_date><![CDATA["+sdf.format(sysdate) +"]]></chg_date>");
							xmlBuffDet.append("<chg_user><![CDATA["+userId +"]]></chg_user>");
							xmlBuffDet.append("<chg_term><![CDATA["+termId +"]]></chg_term>");
							xmlBuffDet.append("</Detail2>");
							lineNoNew ++;
						}
						
						xmlBuff.append("<ord_amt><![CDATA["+mtotOrdAmt +"]]></ord_amt>");
						xmlBuff.append("<tax_amt><![CDATA["+mtotTaxAmt +"]]></tax_amt>");	
						xmlBuff.append("<tot_amt><![CDATA["+mtotNetAmt +"]]></tot_amt>");	
						xmlBuff.append("</Detail1>");
						
						xmlBuff.append(xmlBuffDet);
						xmlBuff.append("</Header0>");
						xmlBuff.append("</group0>");
						xmlBuff.append("</DocumentRoot>");
						
						
						xmlString = xmlBuff.toString();
						System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
						System.out.println("...............just before savedata()");
						
						System.out.println("Count while loop1"+cnt2);
						errString = saveData(msiteCd,xmlString,xtraParams,conn);
						System.out.println("Count while loop2"+cnt2);
						System.out.println("@@@@@2: retString:"+errString);
						System.out.println("--retString finished--");
						
						if (errString.indexOf("Success") > -1)
						{
							System.out.println("@@@@@@3: Success"+errString);
							Document dom = genericUtility.parseString(errString);
							System.out.println("dom>>>"+dom);
							String drcrTranid = checkNull(genericUtility.getColumnValue("TranID",dom)).trim();
							
							System.out.println("drcrTranid>>>"+drcrTranid);
							lineNo = (String)tempMap.get("line_no");
							System.out.println("lineNo>>>>"+lineNo);
							int lineNoInt = 0,lineNooInt = 0;
							
							if(lineNo != null)
							{
								lineNoInt = Integer.parseInt(lineNo.trim());
							}
							System.out.println("lineNoInt>>>>"+lineNoInt);
							sql = "select max(line_no) from cust_stock_det  where tran_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);	
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								lineNoo = checkNull(rs.getString(1));
								
								if(lineNoo != null)
								{
									lineNooInt = Integer.parseInt(lineNoo);
								}
								
								System.out.println("updCnt1 lineNoo>>"+lineNoo);	
							}
							pstmt.close();pstmt = null;
							rs.close();rs = null;
							
							for(int r = 1 ; r <= lineNooInt ; r++)
							{
								System.out.println("INSIDE FOR LOOP>>"+r);
								if(r == lineNoInt)
								{
									System.out.println("updCnt1 inside>>"+updCnt1);	
									sql = "update cust_stock_det set sale_order = ? where tran_id = ? and line_no = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, drcrTranid);	
									pstmt.setString(2, tranId);	
									pstmt.setString(3, lineNo);	
									updCnt1 = pstmt.executeUpdate();
									pstmt.close();pstmt = null;
								}
							}
							System.out.println("updCnt1>>"+updCnt1);
							errString = "";
						}
						else
						{
							System.out.println("@@@@@@3: inside rollback "+errString);
							conn.rollback();
							System.out.println("[" + errString + "]");	
							return errString;
						}
						mtotOrdAmt = 0  ;
						mtotTaxAmt = 0  ;
						mtotNetAmt = 0  ;
						
						cnt2++;
						System.out.println("Count while loop"+cnt2);
						w++;
						tempList = null;
					}
					System.out.println("Count while loop outside"+cnt2);
				}
			}
			sql = "update cust_stock set status = 'Y' where tran_id = ? and status <> 'Y'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);	
			updCnt = pstmt.executeUpdate();
			pstmt.close();pstmt = null;
			System.out.println("updCnt status>>"+updCnt);
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("errString in method"+errString);
		return errString;
	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input.trim();
	}
	public String getCustSchemeCode(String custCode,String schemeCode,String orderType,Connection conn) throws ITMException
	{
			System.out.println("Inside getCustSchemeCode......"+schemeCode+"orderType"+orderType);
			boolean lbProceed = false;
			String noapplyCustList = "",applicableOrdTypes = "" ,applyCustList = "" ,curscheme = "";
			try 
			{
				curscheme = schemeCode;
				String sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as apply_cust_list,	(case when noapply_cust_list is null then ' ' else noapply_cust_list end)as noapply_cust_list,order_type  from 	scheme_applicability where scheme_code = ?";
				PreparedStatement pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, curscheme);
				ResultSet rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					applyCustList = rs1.getString("apply_cust_list");
					noapplyCustList = rs1.getString("noapply_cust_list");
					applicableOrdTypes = rs1.getString("order_type");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				if("NE".equalsIgnoreCase(orderType.trim()) && (applicableOrdTypes == null || applicableOrdTypes.trim().length() == 0))
				{
					//continue;
				}
				else if(applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
				{

					lbProceed = false ;
					String lsApplicableOrdTypesArr[]= applicableOrdTypes.split(",");
					for(int i=0;i<lsApplicableOrdTypesArr.length;i++)
					{
						System.out.println("lsApplicableOrdTypesArr[i]"+lsApplicableOrdTypesArr[i]);
						if(orderType.trim().equalsIgnoreCase(lsApplicableOrdTypesArr[i]))
						{
							lbProceed=true;
							break;
						}
					}
				
				}
				
				if(applyCustList.trim().length() > 0)
				{
					//custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						String applyCustListArr[]=applyCustList.split(",");
						for(int i=0;i<applyCustListArr.length;i++)
						{
							System.out.println("applyCustListArr[i]"+applyCustListArr[i]);
							if(applyCustListArr[i].equalsIgnoreCase(custCode.trim()))
							{
								schemeCode = curscheme;
								break;
							}
						}
				}
	
				if(noapplyCustList.trim().length() > 0 && schemeCode != null)
				{
					if(noapplyCustList.trim().length() > 0 && schemeCode != null)
					{
						//custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						String noapplyCustListArr[] = noapplyCustList.split(",");
						for(int i=0; i < noapplyCustListArr.length;i++)
						{
							System.out.println("noapplyCustListArr[i]" + noapplyCustListArr[i]);
							if(noapplyCustListArr[i].equalsIgnoreCase(custCode.trim()))
							{
								schemeCode = "";
								break;
							}
						}
					}
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
			System.out.println("errCode OF getCustCodeScheme::" + schemeCode);
			return schemeCode;

	}
	
	///gf_get_qty_perc()
	public double gfGetQtyPerc(String custCode, String msiteCd,String mitemCd ,Connection conn) throws ITMException 
	{
		System.out.println("Inside gfGetQtyPerc...");
		double qtyPerc = 0.0 ;
		String sql = "",qtyPercStr = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null ;
		try 
		{
			
		    sql = "select qty_perc from customeritem where  cust_code = ? and item_code = ?";
		    pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, mitemCd);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				qtyPerc = rs.getDouble(1);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(qtyPerc == 0)
			{
				 sql = "select qty_perc from siteitem where  site_code = ? and item_code = ?";
				    pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteCd);
					pstmt.setString(2, mitemCd);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						qtyPerc = rs.getDouble(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					if(qtyPerc == 0)
					{
						qtyPercStr = distCommon.getDisparams("999999", "QTY_PERC", conn);
						if(qtyPercStr != null && qtyPercStr.trim().length() > 0 && !"NULLFOUND".equalsIgnoreCase(qtyPercStr))
						{
							qtyPerc = Double.parseDouble(qtyPercStr) ;
						}
					}
			}
			System.out.println("qtyPerc"+qtyPerc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("errCode OF qtyPerc::" + qtyPerc);
		return qtyPerc;
	}
	
	///gf_get_min_order_qty()
	public double gfGetMinOrderQty(String custCode, String msiteCd,String mitemCd ,Connection conn) throws ITMException 
	{
		System.out.println("Inside gfGetMinOrderQty...");
			
		double minOrderQty = 0.0 ;
		String sql = "",minOrderQtyStr = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null ;
		try 
		{
				
			    sql = "select min_order_qty from customeritem where  cust_code = ? and item_code = ?";
			    pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, mitemCd);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					minOrderQty = rs.getDouble(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(minOrderQty == 0)
				{
					 	sql = "select min_order_qty from siteitem where  site_code = ? and item_code = ?";
					    pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msiteCd);
						pstmt.setString(2, mitemCd);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							minOrderQty = rs.getDouble(1);
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						if(minOrderQty == 0)
						{
							sql = "select min_order_qty from item where   item_code = ?";
						    pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mitemCd);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								minOrderQty = rs.getDouble(1);
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
							if(minOrderQty == 0)
							{
								minOrderQtyStr = distCommon.getDisparams("999999", "QTY_PERC", conn);
								System.out.println("minOrderQtyStr["+minOrderQtyStr+"]");
								if(minOrderQtyStr != null && minOrderQtyStr.trim().length() > 0 && !"NULLFOUND".equalsIgnoreCase(minOrderQtyStr))
								{
									minOrderQty = Double.parseDouble(minOrderQtyStr ) ;
								}
							}
						}
				}
				System.out.println("minOrderQty"+minOrderQty);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
			System.out.println("errCode OF qtyPerc::" + minOrderQty);
			return minOrderQty;
	}
	private String getLineNewNo(String lineNo)	
	{
		lineNo = lineNo.trim();
        System.out.println("lineNo"+lineNo);
		String lenStr = "   " + lineNo ;
		System.out.println("lenStr"+lenStr);
		String lineNoNew = lenStr.substring(lenStr.length() - 3, lenStr.length());
        	
    	System.out.println("lineNonew["+lineNoNew+"]");
    	
		return lineNoNew;
	}
	
	private String saveData(String siteCode,String xmlString,String xtraParams, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		ibase.utility.UserInfoBean userInfo;
		String chgUser = "", chgTerm = "";
		String loginCode = "", loginEmpCode = "", loginSiteCode = "";
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = "";
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			
			userInfo = new ibase.utility.UserInfoBean();
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
			System.out.println("userInfo>>>>>" + userInfo);

			System.out.println("chgUser :" + chgUser);
			System.out.println("chgTerm :" + chgTerm);
			System.out.println("loginCode :" + loginCode);
			System.out.println("loginEmpCode :" + loginEmpCode);
			
			//retString = masterStateful.processRequest(userInfo, siteCode, true, xmlString,true,conn);
			retString = masterStateful.processRequest(userInfo, xmlString, true, conn);
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
	
	public String gbfAcctDetrInvoice(String itemCode, String itemSer,String purpose,String tranType, String acctType ,Connection conn) throws ITMException
	{
		String acctCode = null, cctrCode = null;
		String sql = "" , itemSerN = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		System.out.println("Inside gbfAcctDetrInvoice --> purpose :: " + purpose + " itemSer :: " + itemSer);
		System.out.println(" acctType :: " + acctType + " tranType :: " + tranType);
		try
		{
			if (purpose.equalsIgnoreCase("S-INV"))
			{
				System.out.println("Inside S-INV loop");
				
				if (acctType.equalsIgnoreCase("SAL"))
				{
					System.out.println("Inside SAL loop");
					sql = " SELECT ACCT_CODE__SAL,CCTR_CODE__SAL FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '" + itemCode + "' "
							+ " AND ITEM_SER = '" + itemSer + "' AND TRAN_TYPE = '" + tranType + "'";
							pstmt = conn.prepareStatement(sql);
							System.out.println("sql ::: " + sql);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								acctCode = rs.getString(1);
								cctrCode = rs.getString(2);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							
							if ((acctCode == null  || acctCode.trim().length() == 0) && (cctrCode == null  || cctrCode.trim().length() == 0))
							{
								sql = " SELECT ACCT_CODE__SAL,CCTR_CODE__SAL FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '" + itemCode + "' "
								+ " AND ITEM_SER = ' ' AND ITEM_CODE = '" + itemCode + "' AND TRAN_TYPE = ' ' ";
								pstmt = conn.prepareStatement(sql);
								System.out.println("sql ::: " + sql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									acctCode = rs.getString(1);
									cctrCode = rs.getString(2);
								}
								rs.close(); rs = null;
								pstmt.close(); pstmt = null;
								
								if ((acctCode == null  || acctCode.trim().length() == 0) && (cctrCode == null  || cctrCode.trim().length() == 0))
								{
									if (itemSer == null || itemSer.trim().length() == 0)
									{
										sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
										pstmt = conn.prepareStatement(sql);
										System.out.println("sql ::: " + sql);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											itemSerN = rs.getString(1);
										}
										rs.close(); rs = null;
										pstmt.close(); pstmt = null;
									}
									else
									{
										itemSerN = itemSer;
									}
									

									sql = " SELECT ACCT_CODE__SAL,CCTR_CODE__SAL FROM ITEM_ACCT_DETR WHERE ITEM_CODE = ' ' "
										+ " AND ITEM_SER = '" + itemSerN + "' AND TRAN_TYPE = '" + tranType + "'";
									pstmt = conn.prepareStatement(sql);
									System.out.println("sql ::: " + sql);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										acctCode = rs.getString(1);
										cctrCode = rs.getString(2);
									}
									rs.close(); rs = null;
									pstmt.close(); pstmt = null;
									if ((acctCode == null  || acctCode.trim().length() == 0) && (cctrCode == null  || cctrCode.trim().length() == 0))
									{
										sql = " SELECT ACCT_CODE__SAL,CCTR_CODE__SAL FROM ITEM_ACCT_DETR WHERE ITEM_CODE = ' ' "
												+ " AND ITEM_SER = '" + itemSerN + "' AND TRAN_TYPE = '  '";
											pstmt = conn.prepareStatement(sql);
											System.out.println("sql ::: " + sql);
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												acctCode = rs.getString(1);
												cctrCode = rs.getString(2);
											}
											rs.close(); rs = null;
											pstmt.close(); pstmt = null;
											if ((acctCode == null  || acctCode.trim().length() == 0) && (cctrCode == null  || cctrCode.trim().length() == 0))
											{
												sql = " SELECT ACCT_CODE__SAL,CCTR_CODE__SAL FROM ITEMSER WHERE ITEM_SER = '" + itemSerN + "'";
												pstmt = conn.prepareStatement(sql);
												System.out.println("sql ::: " + sql);
												rs = pstmt.executeQuery();
												if (rs.next())
												{
													acctCode = rs.getString(1);
													cctrCode = rs.getString(2);
												}
												rs.close(); rs = null;
												pstmt.close(); pstmt = null;
											}
									}
								}
							}
				}
				else if(acctType.equalsIgnoreCase("AR"))
				{
					System.out.println("Inside AR loop");
					sql = " SELECT ACCT_CODE__AR,CCTR_CODE__AR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '" + itemCode + "' "
					+ " AND ITEM_SER = '" + itemSer + "' AND TRAN_TYPE = '" + tranType + "'";
					pstmt = conn.prepareStatement(sql);
					System.out.println("sql ::: " + sql);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						acctCode = rs.getString(1);
						cctrCode = rs.getString(2);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if ((acctCode == null  || acctCode.trim().length() == 0) && (cctrCode == null  || cctrCode.trim().length() == 0))
					{
						sql = " SELECT ACCT_CODE__AR,CCTR_CODE__AR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '" + itemCode + "' "
						+ " AND ITEM_SER = ' ' AND TRAN_TYPE = '" + tranType + "'";
						pstmt = conn.prepareStatement(sql);
						System.out.println("sql ::: " + sql);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							acctCode = rs.getString(1);
							cctrCode = rs.getString(2);
						}
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
						if ((acctCode == null  || acctCode.trim().length() == 0) && (cctrCode == null  || cctrCode.trim().length() == 0))
						{
									sql = " SELECT ACCT_CODE__AR,CCTR_CODE__AR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '" + itemCode + "' "
									+ " AND ITEM_SER = ' ' AND TRAN_TYPE = ' '";
									pstmt = conn.prepareStatement(sql);
									System.out.println("sql ::: " + sql);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										acctCode = rs.getString(1);
										cctrCode = rs.getString(2);
									}
									rs.close(); rs = null;
									pstmt.close(); pstmt = null;
									if (acctCode == null  || acctCode.trim().length() == 0)
									{
										if (itemSer == null || itemSer.trim().length() == 0)
										{
											sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
											pstmt = conn.prepareStatement(sql);
											System.out.println("sql ::: " + sql);
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												itemSerN = rs.getString(1);
											}
											rs.close(); rs = null;
											pstmt.close(); pstmt = null;
										}
										else
										{
											itemSerN = itemSer;
										}

										sql = " SELECT ACCT_CODE__AR,CCTR_CODE__AR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = ' ' "
											+ " AND ITEM_SER = '" + itemSerN + "' AND TRAN_TYPE = '" + tranType + "'";
										pstmt = conn.prepareStatement(sql);
										System.out.println("sql ::: " + sql);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											acctCode = rs.getString(1);
											cctrCode = rs.getString(2);
										}
										rs.close(); rs = null;
										pstmt.close(); pstmt = null;
										if (acctCode == null  || acctCode.trim().length() == 0)
										{
											sql = " SELECT ACCT_CODE__AR,CCTR_CODE__AR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = ' ' "
												+ " AND ITEM_SER = '" + itemSerN + "' AND TRAN_TYPE = ' '";
											pstmt = conn.prepareStatement(sql);
											System.out.println("sql ::: " + sql);
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												acctCode = rs.getString(1);
												cctrCode = rs.getString(2);
											}
											rs.close(); rs = null;
											pstmt.close(); pstmt = null;
											if (acctCode == null  || acctCode.trim().length() == 0)
											{
												sql = " SELECT ACCT_CODE__AR,CCTR_CODE__AR FROM ITEMSER WHERE ITEM_SER = '" + itemSerN + "'";
												pstmt = conn.prepareStatement(sql);
												System.out.println("sql ::: " + sql);
												rs = pstmt.executeQuery();
												if (rs.next())
												{
													acctCode = rs.getString(1);
													cctrCode = rs.getString(2);
												}
												rs.close(); rs = null;
												pstmt.close(); pstmt = null;
											}
										}
									}
								}
							}
					
				}
			}
			
		}
		catch (Exception e) 
		{

			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close(); rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close(); pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception in finally block ::"+e);
				e.printStackTrace();
			}
		}
		System.out.println("From getFromAcctDetrInvoice [" + acctCode + "," + cctrCode + "]");
		return acctCode + "," + cctrCode;
	}
	
	
}