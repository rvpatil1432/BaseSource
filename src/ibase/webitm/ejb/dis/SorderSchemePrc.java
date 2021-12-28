
package ibase.webitm.ejb.dis;
/*
 * Author:Wasim Ansari
 * Date:09-DEC-16
 * Request ID:D16HVHB003 (Zero rate sales order from VHB to super stockist)
 */
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@javax.ejb.Stateless
public class SorderSchemePrc extends ProcessEJB implements SorderSchemePrcLocal, SorderSchemePrcRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	MasterStatefulLocal masterStatefulLocal = null;
	DistCommon distCommon = new DistCommon();
	
	public String process(String xmlString, String xmlString2, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("Entered in SorderSchemePrc process.");
		
		System.out.println("xmlString-->["+xmlString+"]");
		System.out.println("xmlString2-->["+xmlString2+"]");
		System.out.println("objContext-->["+objContext+"]");
		System.out.println("xtraParams-->["+xtraParams+"]");
		
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;	
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
			}
			retStr = process(headerDom, detailDom, objContext, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception SorderSchemePrc Main " + e.getMessage());
			e.printStackTrace();
			retStr = e.getMessage();
		}
		return retStr;
	}
	
	public String process(Document dom, Document dom2, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		String loginEmpCode = "",userId = "",loginSite = "",chgTerm = "",userInfo = "";
		String errString = "";
		Connection conn = null;
		String sql = "";
		ResultSet rs = null,rsData = null,rsSql = null;
		PreparedStatement pstmt = null,pstmtData = null,pstmtSql = null;
		boolean isError = false;
		String saleOrder = "";
		ConnDriver connDriver = null;
		String custCodeFr = "",custCodeTo = "",siteCode = "",dateFrom = "",dateTo = "",itemSer = "",orderDate = "";
		Timestamp fromDateTimeStmp = null,toDateTimeStmp = null;
		HashMap tempMap = new HashMap();
		HashMap splitCodeWiseMap = new HashMap();
		
		HashSet sorderSet = new HashSet();
		
		try
		{
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection() ;
			connDriver = null;
			//conn = getConnection();
			orderDate = getCurrdateAppFormat();
			java.sql.Timestamp currDate = new java.sql.Timestamp(System.currentTimeMillis());
			
			AppConnectParm appConnect = new AppConnectParm();
			Properties p = appConnect.getProperty();
			InitialContext ctx = new InitialContext(p);
			
			loginEmpCode = checkNullAndTrim(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode"));
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = checkNullAndTrim((genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode")));
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			
			if(xtraParams == null)
            {
            	xtraParams = getXtraParams(userId,loginSite,userId,conn);
            }
            userInfo  = getUserInfo(xtraParams);
            
            String applDateFormat = genericUtility.getApplDateFormat();
			String dbDateFormat = genericUtility.getDBDateFormat();
			
			SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
			
			dateFrom = genericUtility.getColumnValue("prd_from",dom);
			if ( dateFrom == null || dateFrom.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","PRBLKFRDT","","",conn);
				return retString;
			}
			
			dateTo = genericUtility.getColumnValue("prd_to",dom);
			if ( dateTo == null || dateTo.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","PRBLKFRDT","","",conn);
				return retString;
			}
			
			Date date1 = sdf.parse(dateFrom);
			Date date2 = sdf.parse(dateTo);
			
			System.out.println("From Date["+date1+"] and To Date["+date2+"]");
			if(date2.before(date1))
			{
				retString = itmDBAccessEJB.getErrorString("","PRINVDTPRD","","",conn);
				return retString;
			}
			
			siteCode = genericUtility.getColumnValue("site_code",dom);
			if ( siteCode == null || siteCode.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","BLKSITECD","","",conn);
				return retString;
			}
			
			custCodeFr = genericUtility.getColumnValue("cust_code__fr",dom);
			if ( custCodeFr == null || custCodeFr.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","PRNULCUSFR","","",conn);
				return retString;
			}
			custCodeTo = genericUtility.getColumnValue("cust_code__to",dom);
			if ( custCodeTo == null || custCodeTo.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","PRNULCUSTO","","",conn);
				return retString;
			}
			
			System.out.println("@@Cust Code Fr["+custCodeFr+"]  Cust Code To["+custCodeTo+"] SiteCode["+siteCode+"] FromDate["+dateFrom+"] ToDate["+dateTo+"]");
			
            fromDateTimeStmp = Timestamp.valueOf(genericUtility.getValidDateString( dateFrom , applDateFormat, dbDateFormat)+" 00:00:00");
            toDateTimeStmp   = Timestamp.valueOf(genericUtility.getValidDateString( dateTo , applDateFormat, dbDateFormat)+" 00:00:00");
            
            String customerCode = "",itemCode = "",custCode = "",custCodeDlv = "",custCodeBil = "", dlvTerm = "",remarks = "",
					crTerm = "",currCode = "",stanCode = "",tranCode = "",transMode = "",currCodeFrt = "",currCodeIns = "",orderType = "",random = "",
					countCode = "",stateCode = "",stationFrom = "";
            
			String itemDescr = "",unit = "",unitRate = "",taxClass = "",taxChap = "",taxEnv = "",locType = "",siteCodeCh = "",tempSplitCode = "",acctCodeSal = "", 
					cctrCodeSal = "",schemeQuantity = "",schemeCode = "",groupCode = "",itemFlg = "",itemSru = "",addr1 = "",addr2 = "",addr3 = "",city = "",pin = "",tele1 = "",tele2 = "";
            
            ArrayList tempList = new ArrayList();
            
            
            sql = " SELECT STAN_CODE FROM SITE WHERE  SITE_CODE =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				stationFrom = checkNullAndTrim(rs.getString("STAN_CODE"));
			}
			if(rs != null)
			{
				rs.close();rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();pstmt = null;
			}
          
           /* sql = " SELECT DISTINCT SITE_CODE__CH FROM SITE_CUSTOMER WHERE SITE_CODE = ? AND CHANNEL_PARTNER = ? ";
			pstmtData = conn.prepareStatement(sql);	
			pstmtData.setString(1,siteCode);
			pstmtData.setString(2,"Y");	*/
			sql = " SELECT SITE_CODE__CH,CUST_CODE FROM SITE_CUSTOMER WHERE SITE_CODE = ? AND CHANNEL_PARTNER = ? "
				+ " AND CUST_CODE >= ? AND CUST_CODE <= ? "
				+ " UNION ALL "
				+ " SELECT SITE_CODE,CUST_CODE FROM CUSTOMER WHERE CUST_CODE NOT IN ( SELECT CUST_CODE "
				+ " FROM SITE_CUSTOMER WHERE SITE_CODE = ? AND CHANNEL_PARTNER= ? AND CUST_CODE >= ? AND CUST_CODE <= ? ) "
				+ " AND CUST_CODE >= ? AND CUST_CODE <= ? AND CHANNEL_PARTNER = ? ";
			pstmtData = conn.prepareStatement(sql);	
			pstmtData.setString(1,siteCode);
			pstmtData.setString(2,"Y");
			pstmtData.setString(3,custCodeFr);
			pstmtData.setString(4,custCodeTo);	
			pstmtData.setString(5,siteCode);	
			pstmtData.setString(6,"Y");	
			pstmtData.setString(7,custCodeFr);	
			pstmtData.setString(8,custCodeTo);	
			pstmtData.setString(9,custCodeFr);	
			pstmtData.setString(10,custCodeTo);	
			pstmtData.setString(11,"Y");
			rsData = pstmtData.executeQuery();
			
			while(rsData.next())
			{
				siteCodeCh = checkNullAndTrim(rsData.getString("SITE_CODE__CH"));
				customerCode = checkNullAndTrim(rsData.getString("CUST_CODE"));
				
				System.out.println("Site Code Channel["+siteCodeCh+"] Customer Code ["+customerCode+"]");
				
				sql = " SELECT C.CR_TERM,C.DLV_TERM,C.CURR_CODE,C.TRAN_CODE,C.TRANS_MODE,C.CURR_CODE__FRT,C.CURR_CODE__INS,C.STAN_CODE,C.STATE_CODE,"
					 +" C.COUNT_CODE,C.LOC_GROUP, C.ADDR1,C.ADDR2,C.ADDR3,C.CITY,C.PIN,C.CUST_CODE__BIL,C.TELE1,C.TELE2,C.TAX_CLASS,C.TAX_CHAP "
					 +" FROM CUSTOMER C WHERE C.CUST_CODE = ? ";
				pstmtSql = conn.prepareStatement(sql);	
				pstmtSql.setString(1,customerCode);
	  			rsSql = pstmtSql.executeQuery();
	  			if(rsSql.next())
	  			{
	  				crTerm = rsSql.getString("CR_TERM");
	  				dlvTerm = rsSql.getString("DLV_TERM");
	  				currCode = rsSql.getString("CURR_CODE");
	  				tranCode = rsSql.getString("TRAN_CODE");
	  				transMode = rsSql.getString("TRANS_MODE");
	  				currCodeFrt = rsSql.getString("CURR_CODE__FRT");
	  				currCodeIns = rsSql.getString("CURR_CODE__INS");
	  				stanCode = rsSql.getString("STAN_CODE");
	  				stateCode = rsSql.getString("STATE_CODE");
	  				countCode = rsSql.getString("COUNT_CODE");
	  				groupCode = rsSql.getString("LOC_GROUP");
	  				addr1 = checkNullAndTrim(rsSql.getString("ADDR1"));
	  				addr2 = checkNullAndTrim(rsSql.getString("ADDR2"));
	  				addr3 = checkNullAndTrim(rsSql.getString("ADDR3"));
	  				city = checkNullAndTrim(rsSql.getString("CITY"));
	  				pin = checkNullAndTrim(rsSql.getString("PIN"));
	  				custCodeBil = checkNullAndTrim(rsSql.getString("CUST_CODE__BIL"));
	  				tele1 = checkNullAndTrim(rsSql.getString("TELE1"));
	  				tele2 = checkNullAndTrim(rsSql.getString("TELE2"));
	  				taxClass = checkNullAndTrim(rsSql.getString("TAX_CLASS"));
	  				taxChap = checkNullAndTrim(rsSql.getString("TAX_CHAP"));
	  			}
	  			if(rsSql != null)
				{
	  				rsSql.close();rsSql = null;
				}	
				if(pstmtSql != null)
				{	
					pstmtSql.close();pstmtSql = null;
				}
	  			
				   sql = " SELECT S.SALE_ORDER,H.SITE_CODE,D.ITEM_CODE,I.DESCR,I.UNIT,I.UNIT__RATE,I.ITEM_STRU,ISR.ACCT_CODE__SAL,ISR.CCTR_CODE__SAL, "
						+" I.ITEM_SER, (CASE WHEN I.LOC_TYPE__PARENT IS NULL THEN I.LOC_TYPE ELSE I.LOC_TYPE__PARENT END ) AS LOC_TYPE "
						+" FROM INVOICE H, INVOICE_TRACE D, ITEM I, ITEMSER ISR, SORDER S "
						+" WHERE H.INVOICE_ID = D.INVOICE_ID "
						+" AND S.SALE_ORDER = H.SALE_ORDER "
						+" AND I.ITEM_CODE = D.ITEM_CODE "
						+" AND ISR.ITEM_SER = I.ITEM_SER "
						+" AND S.SITE_CODE = ? "
						+" AND S.ORDER_DATE BETWEEN ? AND ? "  
						+" AND D.LINE_TYPE = ? "
						+" AND (CASE WHEN S.UDF__STR1 IS NULL THEN 'N' ELSE S.UDF__STR1 END ) ! = ? " ;
				
		  			pstmt = conn.prepareStatement(sql);	
		  			pstmt.setString(1,siteCodeCh); 
		  			pstmt.setTimestamp(2,fromDateTimeStmp);	
		  			pstmt.setTimestamp(3,toDateTimeStmp);
		  			pstmt.setString(4,"F");
		  			pstmt.setString(5,"Y");
		  			
		  			rs = pstmt.executeQuery();
		  			while(rs.next())
		  			{
		  				itemCode = rs.getString("ITEM_CODE");
		  				itemDescr = rs.getString("DESCR");
		  				unit = rs.getString("UNIT");
		  				unitRate = rs.getString("UNIT__RATE");
		  				itemSru = checkNullAndTrim(rs.getString("ITEM_STRU"));
		  				itemSer = checkNullAndTrim(rs.getString("ITEM_SER"));
		  				acctCodeSal = checkNullAndTrim(rs.getString("ACCT_CODE__SAL"));
		  				cctrCodeSal = checkNullAndTrim(rs.getString("CCTR_CODE__SAL"));
		  				locType = rs.getString("LOC_TYPE");
		  				
		  				if ("F".equalsIgnoreCase(itemSru))
						{
							itemFlg = "B";
						}
						else
						{
							itemFlg = "I";
						}
		  				
		  				sql = " SELECT ORDER_TYPE FROM SORDERTYPE WHERE LOC_TYPE__PARENT = ? ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, locType);
						rsSql = pstmtSql.executeQuery();
						if (rsSql.next())
						{
							orderType = checkNullAndTrim((rsSql.getString("ORDER_TYPE")));
						}
						if ( rsSql != null )
						{
							rsSql.close();rsSql = null;
						}
						if( pstmtSql != null )
						{
							pstmtSql.close();pstmtSql = null;
						}
						
						sql = " SELECT SCHEME_CODE FROM SCHEME_APPLICABILITY WHERE ITEM_CODE = ? AND ? BETWEEN APP_FROM AND VALID_UPTO ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, itemCode);
						pstmtSql.setTimestamp(2, currDate);
						rsSql = pstmtSql.executeQuery();
						if (rsSql.next())
						{
							schemeCode = checkNullAndTrim((rsSql.getString("SCHEME_CODE")));
						}
						if ( rsSql != null )
						{
							rsSql.close();rsSql = null;
						}
						if( pstmtSql != null )
						{
							pstmtSql.close();pstmtSql = null;
						}
						
						sql = " SELECT QTY_PER FROM BOMDET WHERE ITEM_CODE = ? AND NATURE = ? AND BOM_CODE = ?";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, itemCode);
						pstmtSql.setString(2, "F");
						pstmtSql.setString(3, schemeCode);
						rsSql = pstmtSql.executeQuery();
						if (rsSql.next())
						{
							schemeQuantity = checkNullAndTrim((rsSql.getString("QTY_PER")));
						}
						if ( rsSql != null )
						{
							rsSql.close();rsSql = null;
						}
						if( pstmtSql != null )
						{
							pstmtSql.close();pstmtSql = null;
						}
						
						if(taxChap.length() == 0)
						{	
							taxChap  = checkNullAndTrim(distCommon.getTaxChap(itemCode, itemSer, "C", custCode, siteCode, conn));
						}
						if(taxClass.length() == 0)
						{
							taxClass = checkNullAndTrim(distCommon.getTaxClass("C", custCodeDlv, itemCode, siteCode, conn));
						}
						taxEnv   = checkNullAndTrim(distCommon.getTaxEnv(stationFrom, stanCode, taxChap, taxClass, siteCode, conn));
						
		  			
		  				tempSplitCode = itemSer + "," + customerCode.trim() + "@" + crTerm.trim() + "." + locType.trim();
		  				
		  				System.out.println("tempSplitCode ["+tempSplitCode+"]");
		  				
		  				if (splitCodeWiseMap.containsKey(tempSplitCode))
						{
							tempList = (ArrayList) splitCodeWiseMap.get(tempSplitCode);
						} 
		  				else
						{
							tempList = new ArrayList();
						}
		  				
		  				tempMap = new HashMap();
		  				tempMap.put("cust_code", customerCode);
		  				tempMap.put("site_code", siteCode);
		  				tempMap.put("order_type", orderType);
		  				tempMap.put("item_ser", itemSer);
		  				tempMap.put("dlv_term", dlvTerm);
		  				tempMap.put("cr_term", crTerm);
		  				tempMap.put("curr_code", currCode);
		  				tempMap.put("tran_code", tranCode);
		  				tempMap.put("trans_mode", transMode);
		  				tempMap.put("curr_code__frt", currCodeFrt);
		  				tempMap.put("curr_code__ins", currCodeIns);
		  				tempMap.put("stan_code", stanCode);
		  				tempMap.put("state_code__dlv", stateCode);
		  				tempMap.put("count_code__dlv", countCode);
		  				tempMap.put("group_code", groupCode);
		  				tempMap.put("dlv_add1", addr1);
		  				tempMap.put("dlv_add2", addr2);
		  				tempMap.put("dlv_add3", addr3);
		  				tempMap.put("dlv_city", city);
		  				tempMap.put("dlv_pin", pin);
		  				tempMap.put("cust_code__bil", custCodeBil);
		  				tempMap.put("tel1__dlv", tele1);
		  				tempMap.put("tel2__dlv", tele2);
		  				tempMap.put("site_code__ship", siteCodeCh);
		  				
		  				tempMap.put("item_code", itemCode);
		  				tempMap.put("quantity", schemeQuantity);
		  				tempMap.put("descr", itemDescr);
		  				tempMap.put("loc_type", locType);
		  				tempMap.put("unit", unit);
		  				tempMap.put("unit__rate", unitRate);
		  				tempMap.put("tax_class", taxClass);
		  				tempMap.put("tax_chap", taxChap);
		  				tempMap.put("tax_env", taxEnv);
		  				tempMap.put("acct_code__sal", acctCodeSal);
		  				tempMap.put("cctr_code__sal", cctrCodeSal);
		  				tempMap.put("item_flg", itemFlg);
		  				tempList.add(tempMap);

						if (splitCodeWiseMap.containsKey(tempSplitCode))
						{
							splitCodeWiseMap.put(tempSplitCode, tempList);
						} 
						else
						{
							splitCodeWiseMap.put(tempSplitCode, tempList);
						}
						
						saleOrder = rs.getString("SALE_ORDER");
						sorderSet.add(saleOrder);
		  			}
		  			if ( rs != null )
					{
						rs.close();rs = null;
					}
					if( pstmt != null )
					{
						pstmt.close();pstmt = null;
					}
			}
			if(rsData != null)
			{
				rsData.close();rsData = null;
			}	
			if(pstmtData != null)
			{	
				pstmtData.close();pstmtData = null;
			}
			
			System.out.println("Total Map Size="+splitCodeWiseMap.size());
			
			if(splitCodeWiseMap.size() == 0)
			{
				retString = itmDBAccessEJB.getErrorString("","PRNODATAFD","","",conn);
				return retString;
			}
			
			String splitCode = "",lineNoOrd = "";
			int domID = 0,totalSorder = 0;
			
			Set setItem = splitCodeWiseMap.entrySet();
			tempList = null;
			Iterator itrItem = setItem.iterator();
			HashMap tempMapHdr = new HashMap();
			
			StringBuffer xmlStringHdr = new StringBuffer();
			StringBuffer xmlStringDet = new StringBuffer();
			StringBuffer xmlString = new StringBuffer();
			
			while (itrItem.hasNext())
			{
				Map.Entry itemMapEntry = (Map.Entry) itrItem.next();
	            splitCode = (String) itemMapEntry.getKey();
	            tempList = (ArrayList) splitCodeWiseMap.get(splitCode);
	            
	            for (int itemCtr = 0; itemCtr < tempList.size(); itemCtr = tempList.size()) //will execute only once for making header XML
				{
					tempMapHdr = (HashMap) tempList.get(itemCtr);
					
					xmlStringHdr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					xmlStringHdr.append("<DocumentRoot><description>Datawindow Root</description><group0><description>Group0 escription</description>");
					xmlStringHdr.append("<Header0>");
					xmlStringHdr.append("<description>Header0 members</description>");
					xmlStringHdr.append("<objName><![CDATA[").append("sorder").append("]]></objName>");
		            xmlStringHdr.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
		            xmlStringHdr.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
		            xmlStringHdr.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
		            xmlStringHdr.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
		            xmlStringHdr.append("<action><![CDATA[").append("SAVE").append("]]></action>");
		            xmlStringHdr.append("<elementName><![CDATA[").append("").append("]]></elementName>");
		            xmlStringHdr.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
		            xmlStringHdr.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
		            xmlStringHdr.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
		            xmlStringHdr.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
		            xmlStringHdr.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
		            
		            xmlStringHdr.append("<Detail1 dbID='' domID=\"1\" objName=\"sorder\" objContext=\"1\">");
		            xmlStringHdr.append("<attribute pkNames=\"sale_order:\" status=\"N\" updateFlag=\"A\" selected=\"N\" />");
		            
		            xmlStringHdr.append("<sale_order><![CDATA[]]></sale_order>");
		            xmlStringHdr.append("<order_date><![CDATA[").append(orderDate).append("]]></order_date>");
		            
		            if (tempMapHdr.get("order_type") != null)
					{
		            	 xmlStringHdr.append("<order_type><![CDATA[").append(tempMapHdr.get("order_type")).append("]]></order_type>");
					}
		            if (tempMapHdr.get("site_code") != null)
					{
		            	 xmlStringHdr.append("<site_code><![CDATA[").append(tempMapHdr.get("site_code")).append("]]></site_code>");
					}
		            if (tempMapHdr.get("site_code__ship") != null)
					{
		            	 xmlStringHdr.append("<site_code__ship><![CDATA[").append(tempMapHdr.get("site_code__ship")).append("]]></site_code__ship>");
					}
		            if (tempMapHdr.get("item_ser") != null)
					{
		            	 xmlStringHdr.append("<item_ser><![CDATA[").append(tempMapHdr.get("item_ser")).append("]]></item_ser>");
					}
		            if (tempMapHdr.get("cust_code") != null)
					{
		            	 xmlStringHdr.append("<cust_code><![CDATA[").append(tempMapHdr.get("cust_code")).append("]]></cust_code>");
		            	 xmlStringHdr.append("<cust_code__dlv><![CDATA[").append(tempMapHdr.get("cust_code")).append("]]></cust_code__dlv>");
					}
		            if (tempMapHdr.get("cust_code__bil") != null)
					{ 
				         xmlStringHdr.append("<cust_code__bil><![CDATA[").append(tempMapHdr.get("cust_code__bil")).append("]]></cust_code__bil>");
					}
		            if (tempMapHdr.get("dlv_term") != null)
					{
		            	xmlStringHdr.append("<dlv_term><![CDATA[").append(tempMapHdr.get("dlv_term")).append("]]></dlv_term>");
					}
		            if (tempMapHdr.get("cr_term") != null)
					{
		            	 xmlStringHdr.append("<cr_term><![CDATA[").append(tempMapHdr.get("cr_term")).append("]]></cr_term>");
					}
		            if (tempMapHdr.get("curr_code") != null)
					{
		            	xmlStringHdr.append("<curr_code><![CDATA[").append(tempMapHdr.get("curr_code")).append("]]></curr_code>");
					}
		            if (tempMapHdr.get("tran_code") != null)
					{
		            	xmlStringHdr.append("<tran_code><![CDATA[").append(tempMapHdr.get("tran_code")).append("]]></tran_code>");
					}
		            if (tempMapHdr.get("trans_mode") != null)
					{
		            	xmlStringHdr.append("<trans_mode><![CDATA[").append(tempMapHdr.get("trans_mode")).append("]]></trans_mode>");
					}
		            if (tempMapHdr.get("curr_code__frt") != null)
					{
		            	xmlStringHdr.append("<curr_code__frt><![CDATA[").append(tempMapHdr.get("curr_code__frt")).append("]]></curr_code__frt>");
					}
		            if (tempMapHdr.get("curr_code__ins") != null)
					{
		            	xmlStringHdr.append("<curr_code__ins><![CDATA[").append(tempMapHdr.get("curr_code__ins")).append("]]></curr_code__ins>");
					}
		            if (tempMapHdr.get("acct_code__sal") != null)
					{
		            	xmlStringHdr.append("<acct_code__sal><![CDATA[").append(tempMapHdr.get("acct_code__sal")).append("]]></acct_code__sal>");
					}
		            if (tempMapHdr.get("cctr_code__sal") != null)
					{
		            	xmlStringHdr.append("<cctr_code__sal><![CDATA[").append(tempMapHdr.get("cctr_code__sal")).append("]]></cctr_code__sal>");
					}
		            if (tempMapHdr.get("stan_code") != null)
					{
		            	xmlStringHdr.append("<stan_code><![CDATA[").append(tempMapHdr.get("stan_code")).append("]]></stan_code>");
					}
		            if (tempMapHdr.get("state_code__dlv") != null)
					{
		            	xmlStringHdr.append("<state_code__dlv><![CDATA[").append(tempMapHdr.get("state_code__dlv")).append("]]></state_code__dlv>");
					}
		            if (tempMapHdr.get("count_code__dlv") != null)
					{
		            	xmlStringHdr.append("<count_code__dlv><![CDATA[").append(tempMapHdr.get("count_code__dlv")).append("]]></count_code__dlv>");
					}
		            if (tempMapHdr.get("group_code") != null)
					{
		            	xmlStringHdr.append("<loc_group><![CDATA[").append(tempMapHdr.get("group_code")).append("]]></loc_group>");
					}
		            if (tempMapHdr.get("dlv_to") != null)
					{
		            	xmlStringHdr.append("<dlv_to><![CDATA[").append(tempMapHdr.get("dlv_to")).append("]]></dlv_to>");
					}
		            if (tempMapHdr.get("dlv_add1") != null)
					{
		            	xmlStringHdr.append("<dlv_add1><![CDATA[").append(tempMapHdr.get("dlv_add1")).append("]]></dlv_add1>");
					}
		            if (tempMapHdr.get("dlv_add2") != null)
					{
		            	xmlStringHdr.append("<dlv_add2><![CDATA[").append(tempMapHdr.get("dlv_add2")).append("]]></dlv_add2>");
					}
		            if (tempMapHdr.get("dlv_add3") != null)
					{
		            	xmlStringHdr.append("<dlv_add3><![CDATA[").append(tempMapHdr.get("dlv_add3")).append("]]></dlv_add3>");
					}
		            if (tempMapHdr.get("dlv_city") != null)
					{
		            	xmlStringHdr.append("<dlv_city><![CDATA[").append(tempMapHdr.get("dlv_city")).append("]]></dlv_city>");
					}
		            if (tempMapHdr.get("dlv_pin") != null)
					{
		            	xmlStringHdr.append("<dlv_pin><![CDATA[").append(tempMapHdr.get("dlv_pin")).append("]]></dlv_pin>");
					}
		            if (tempMapHdr.get("tel1__dlv") != null)
					{
		            	xmlStringHdr.append("<tel1__dlv><![CDATA[").append(tempMapHdr.get("tel1__dlv")).append("]]></tel1__dlv>");
					}
		            if (tempMapHdr.get("tel2__dlv") != null)
					{
		            	xmlStringHdr.append("<tel2__dlv><![CDATA[").append(tempMapHdr.get("tel2__dlv")).append("]]></tel2__dlv>");
					}
		            
		            xmlStringHdr.append("<due_date>").append("<![CDATA["+orderDate+"]]>").append("</due_date>\r\n");
		            xmlStringHdr.append("<exch_rate>").append("<![CDATA["+1+"]]>").append("</exch_rate>\r\n");
		            xmlStringHdr.append("</Detail1>");
				}
				
	            domID = 0;
	            lineNoOrd = "";
	            
	            for (int itemCtr = 0; itemCtr < tempList.size(); itemCtr++) //loop for making detail XML
				{
	            	domID++;

					tempMap = (HashMap) tempList.get(itemCtr);
					
		         	xmlStringDet.append("<Detail2 dbID=':' domID='"+ domID +"' objName='sorder' objContext='2'>");
				    xmlStringDet.append("<attribute pkNames='sale_order:line_no:' status='N' updateFlag='A' selected='N'/>");
			        
					lineNoOrd = "   " + domID;
					lineNoOrd = lineNoOrd.substring( lineNoOrd.length()-3 );
						
					xmlStringDet.append("<sale_order><![CDATA[]]></sale_order>");
					xmlStringDet.append("<line_no><![CDATA[").append(lineNoOrd).append("]]></line_no>");
					
					if (tempMap.get("item_code") != null)
					{
						xmlStringDet.append("<item_code><![CDATA[").append(tempMap.get("item_code")).append("]]></item_code>");
						xmlStringDet.append("<item_code__ord><![CDATA[").append(tempMap.get("item_code")).append("]]></item_code__ord>");
					}
					if (tempMap.get("quantity") != null)
					{
						xmlStringDet.append("<quantity><![CDATA[").append(tempMap.get("quantity")).append("]]></quantity>");
					}
					if (tempMap.get("item_code__ord") != null)
					{
						xmlStringDet.append("<item_code__ord><![CDATA[").append(tempMap.get("item_code__ord")).append("]]></item_code__ord>");
					}
					if (tempMap.get("item_descr") != null)
					{
						xmlStringDet.append("<item_descr><![CDATA[").append(tempMap.get("item_descr")).append("]]></item_descr>");
					}
					if (tempMap.get("site_code") != null)
					{
						xmlStringDet.append("<site_code><![CDATA[").append(tempMap.get("site_code")).append("]]></site_code>");
					}
					if (tempMap.get("unit") != null)
					{
						xmlStringDet.append("<unit><![CDATA[").append(tempMap.get("unit")).append("]]></unit>");
						xmlStringDet.append("<unit__std><![CDATA[").append(tempMap.get("unit")).append("]]></unit__std>");
					}
					if (tempMap.get("unit__rate") != null)
					{
						xmlStringDet.append("<unit__rate><![CDATA[").append(tempMap.get("unit__rate")).append("]]></unit__rate>");
					}
					if (tempMap.get("tax_chap") != null)
					{
						xmlStringDet.append("<tax_chap><![CDATA[").append(tempMap.get("tax_chap")).append("]]></tax_chap>");
					}
					if (tempMap.get("tax_class") != null)
					{
						xmlStringDet.append("<tax_class><![CDATA[").append(tempMap.get("tax_class")).append("]]></tax_class>");
					}
					if (tempMap.get("tax_env") != null)
					{
						xmlStringDet.append("<tax_env><![CDATA[").append(tempMap.get("tax_env")).append("]]></tax_env>");
					}
					if (tempMap.get("item_flg") != null)
					{
						xmlStringDet.append("<item_flg><![CDATA[").append(tempMap.get("item_flg")).append("]]></item_flg>");
					}
					
					xmlStringDet.append("<rate><![CDATA[").append(0).append("]]></rate>");
					xmlStringDet.append("<rate__std><![CDATA[").append(0).append("]]></rate__std>");
					xmlStringDet.append("<rate__clg><![CDATA[").append(0).append("]]></rate__clg>");
					xmlStringDet.append("<rate__stduom><![CDATA[").append(0).append("]]></rate__stduom>");
					xmlStringDet.append("<conv__qty_stduom>").append("<![CDATA[" + 1 + "]]>").append("</conv__qty_stduom>");
					xmlStringDet.append("<conv__rtuom_stduom>").append("<![CDATA[" + 1 + "]]>").append("</conv__rtuom_stduom>");
					xmlStringDet.append("<dsp_date>").append("<![CDATA["+orderDate+"]]>").append("</dsp_date>");
					xmlStringDet.append("<nature>").append("<![CDATA[F]]>").append("</nature>");
					
					xmlStringDet.append("</Detail2>");
				}		
	            
	            xmlString.append(xmlStringHdr);
	            xmlString.append(xmlStringDet);
				xmlString.append("</Header0></group0></DocumentRoot>");
				
				System.out.println("@@xmlString For generation Sale Order:: "+xmlString.toString());
	            masterStatefulLocal = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local"); 
	            
	            retString = masterStatefulLocal.processRequest(userInfo, xmlString.toString(), true, conn); 
	            
	            System.out.println("Sale Order return string"+retString);
				
				if( retString.indexOf("Success") > -1 && retString.indexOf("<TranID>") > 0 )
				{
					String tranIDSorder = retString.substring( retString.indexOf("<TranID>")+8, retString.indexOf("</TranID>"));
					System.out.println("Sales Order has been created successfully for itemSeris ["+itemSer+"] Sales Order["+tranIDSorder+"]");
					
					totalSorder++;
				}
				else
				{
					return retString;
				}
				
				xmlStringHdr.delete(0, xmlStringHdr.length());
				xmlStringDet.delete(0, xmlStringDet.length());
				xmlString.delete(0, xmlString.length());
			}
			
			System.out.println("totalSorder["+totalSorder+"] and Map Size["+splitCodeWiseMap.size()+"] SorderSet["+sorderSet+"]");
			if(totalSorder == splitCodeWiseMap.size() && splitCodeWiseMap.size() != 0)
			{
				sql = " UPDATE SORDER SET UDF__STR1 = ? WHERE SALE_ORDER = ? ";
				pstmt = conn.prepareStatement(sql);
				
				Iterator iter = sorderSet.iterator();
				while (iter.hasNext()) 
				{
					saleOrder = (String) iter.next();
					
					pstmt.setString(1, "Y");
					pstmt.setString(2, saleOrder);
					pstmt.addBatch();
					pstmt.clearParameters();
				}
				pstmt.executeBatch();
				if( pstmt != null )
				{
					pstmt.close();pstmt = null;
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Inside catch SorderSchemePrc="+e.getMessage());
			isError = true;
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(isError)
				{
					System.out.println("Rollbacking.................");
					conn.rollback();
				}
				else
				{
					System.out.println("commiting.................");
					conn.commit();
					
					retString = itmDBAccessEJB.getErrorString("","PRCSTATUS","","",conn);
				}
				if(rsData != null)
				{
					rsData.close();rsData = null;
				}	
				if(pstmtData != null)
				{	
					pstmtData.close();pstmtData = null;
				}
				if ( rs != null )
				{
					rs.close();rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();pstmt = null;
				}
				if( conn != null )
				{
					conn.close();conn = null;
				}
			}
			catch( Exception ex)
			{
				ex.printStackTrace();
				throw new ITMException(ex);
			}
		}
		return retString;
	}

	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}
	
	public String getUserInfo( String xtraParams )throws ITMException
	{
		StringBuffer userInfoStr = new StringBuffer();
		String userId = "";
		String loginEmpCode = "";
		String loginSiteCode = "";
		String entityCode = "";
		String profileId = "";
		String userType = "";
		
		String chgTerm = "";
		try
		{
			E12GenericUtility genericUtility = new E12GenericUtility();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			entityCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"entityCode");
			profileId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"profileId");
			userType = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userType");
			
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
			
			System.out.println("xtraParams  is @@@@@ " + xtraParams);

			userInfoStr.append("<UserInfo>");
			userInfoStr.append("<loginCode>").append("<![CDATA["+userId+"]]>").append("</loginCode>\r\n");
			userInfoStr.append("<empCode>").append("<![CDATA["+loginEmpCode+"]]>").append("</empCode>\r\n");
			userInfoStr.append("<siteCode>").append("<![CDATA["+loginSiteCode+"]]>").append("</siteCode>\r\n");
			userInfoStr.append("<entityCode>").append("<![CDATA["+entityCode+"]]>").append("</entityCode>\r\n");
			userInfoStr.append("<profileId>").append("<![CDATA["+profileId+"]]>").append("</profileId>\r\n");
			userInfoStr.append("<userType>").append("<![CDATA["+userType+"]]>").append("</userType>\r\n");
			
			userInfoStr.append("<remoteHost>").append("<![CDATA["+chgTerm+"]]>").append("</remoteHost>\r\n");
			userInfoStr.append("</UserInfo>");
		}
		catch ( Exception e )
		{
			throw new ITMException(e);
		}
		return userInfoStr.toString();
	}
	
	public String getXtraParams( String loginCode, String siteCode,String empCode,Connection  conn)throws Exception
	{
		String remotehost = "";
		String entityCode = "";
		String profileId = "";
		String userType = "";
		String userLang = "";
		String userCountry = "";	
		StringBuffer xtraParamsBuff = new StringBuffer();
		
		String sql = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
			
		
		try
		{
				sql = "SELECT USR_LEV,USER_TYPE,ENTITY_CODE,PROFILE_ID,LOGGER_TYPE,USER_LANG,"+
					  "USER_COUNTRY,TRANS_DB, USER_THEME FROM USERS WHERE CODE = ? ";
				pstmt = conn.prepareStatement(sql);			
				pstmt.setString(1,loginCode);			
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					entityCode = rs.getString("ENTITY_CODE");
					profileId  = rs.getString("PROFILE_ID");
					userType   = rs.getString("USER_TYPE");
					userLang   = rs.getString("USER_LANG");
					userCountry= rs.getString("USER_COUNTRY");
				}			
				
				remotehost =  InetAddress.getLocalHost().getHostAddress();
				String XTRA_PARAMS_SEPARATOR = "~~";
				
				xtraParamsBuff.append("loginCode=" + loginCode);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("loginEmpCode=" + empCode);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("loginSiteCode=" + siteCode);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("entityCode=" + entityCode);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("profileId=" + profileId);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("userType=" + userType);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("runMode=I");
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("user_lang=" + userLang);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("user_country=" + userCountry);
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("charEnc=" + CommonConstants.ENCODING);
				
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("termId="+remotehost);
		
				String chgTerm = remotehost;
				if( chgTerm != null && chgTerm.length() > 15 )
				{
					chgTerm = chgTerm.substring(0, 15);
				}
				else if( chgTerm == null || chgTerm.length() == 0 )
				{
					chgTerm = remotehost;
				}
				xtraParamsBuff.append(XTRA_PARAMS_SEPARATOR).append("chgTerm=" + chgTerm );
				
				System.out.println("xtraParamsBuff ["+xtraParamsBuff+"]");
				
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return xtraParamsBuff.toString();
	}
	
	private String getCurrdateAppFormat() 
	{
		String s = "";
		GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(timestamp).toString();
		}
		catch (Exception localException)
		{
		}

		return s;
	}

	@Override
	public String process() throws RemoteException, ITMException 
	{
		// TODO Auto-generated method stub
		return null;
	}
}

