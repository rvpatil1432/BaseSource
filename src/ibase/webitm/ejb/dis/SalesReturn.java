
/***********************************************************
	Title : SalesReturn
	Date  : 08/06/09
	Author: manoharan

 ***********************************************************/ 
package ibase.webitm.ejb.dis;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.SysCommon;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.sys.UtilMethods;
import java.util.*;
import java.rmi.RemoteException;
import java.util.Date;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3
import ibase.webitm.ejb.ITMDBAccessEJB;
import org.w3c.dom.CDATASection;

import java.text.*; 
import java.sql.*;

@Stateless // added for ejb3
public class SalesReturn extends ValidatorEJB implements SalesReturnLocal, SalesReturnRemote
{
	FinCommon finCommon = new FinCommon();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String winName = null;
	UtilMethods utilMethods = UtilMethods.getInstance();
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		//System.out.println("<======= SOrderForm DISPLAY IS IN PROCESS ! \n Welcome!========>");
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
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("**********VALLABH KADAM New wfValdata() call************");
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;

		try
		{
			//System.out.println("xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : SOrderFormEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("********* wfValData() VALLABH KADAM Previouus **********");
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println(">>>>>>>>>>>>>>>In wfValData 23/01/15:");
			System.out.println("Val xmlString :: " + xmlString );
			System.out.println("Val xmlString1 :: " + xmlString1 );
			System.out.println("Val xmlString2 :: " + xmlString2 );
			//long startTime = System.currentTimeMillis();
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			//long endTime = System.currentTimeMillis();
			//long totalTime = endTime - startTime;
			//System.out.println(xmlString2);
			//System.out.println("start Time Spend :: "+startTime+" Milliseconds");
			//System.out.println("End Time Spend :: "+endTime+" Milliseconds");
			//System.out.println("Total Time Spend :: "+totalTime+" Milliseconds");

			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : SOrderFormEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)  throws RemoteException,ITMException
	{
		System.out.println("********VALLABH KADAM itemChange() Previous********");
		System.out.println(">>>>>>>>>>>>>>>IN String itemChanged currentColumn:"+currentColumn);
		System.out.println("Val xmlString*** :: " + xmlString );
		System.out.println("Val xmlString1******* :: " + xmlString1 );
		System.out.println("Val xmlString2 ****:: " + xmlString2 );
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return valueXmlString;	
	}
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams)  throws RemoteException,ITMException
	{
		System.out.println("******* VALLABH KADAM New itemChange() call******");
		System.out.println("The service code change to poic_default_ejb1 'VALLABH KADAM'");
		System.out.println("IN String itemChanged currentColumn:"+currentColumn);
		System.out.println("Val xmlString:: " + xmlString );
		System.out.println("Val xmlString1:: " + xmlString1 );
		//		System.out.println("Val xmlString2 ****:: " + xmlString2 );
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			//			if (xmlString2.trim().length() > 0 )
			//			{
			//				dom2 = parseString(xmlString2);
			//			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return valueXmlString;	
	}
	/*
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	//public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
        return valueXmlString;
	}
	 */
	/*
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			 if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
			 }
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());

			throw new ITMException(e);
		}
        return valueXmlString;
	}

	public String itemChanged(String xmlString, String xmlString1, String winName, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		String valueXmlString = "";
		try
		{
			System.out.println("xmlString :: " + xmlString );
			dom = parseString(xmlString);
			System.out.println("xmlString1 :: " + xmlString1 );
			dom1 = parseString(xmlString1);
			valueXmlString = itemChanged(dom,dom1, winName,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());

			throw new ITMException(e);
		}
        return valueXmlString;
	}
	 */
	/*public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		return "";
	}*/
	public String itemChanged(Document dom, Document dom1, String winName, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		return "";
	}
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();

		DecimalFormat deciFormater = new DecimalFormat("0.00");

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Node tempNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null,pstmt7=null;
		ResultSet rs = null, rs1 = null,rs7=null ;
		String childNodeName = null;
		String columnValue = null;
		int currentFormNo = 0 ,cnt = 0;
		String sql = "",descr="",sql1="";
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = null;
		distCommon = new DistCommon();	

		//String tempVal = null;
		String reStr = null,lineNoInvStr = "0";
		String siteCode = null;
		String itemDescr = null,itemCode = null;
		String loginSiteCode = null;
		String siteDescr = null;
		String custCode = null;
		String custName = null;
		String priceList = null;
		String priceListClgYn = null, priceListClg = null;
		String pickLowerRate = null;
		String reasCode = null, reasonDescr = null;
		//String winName = null;
		String confirmed = null;
		String currCode = null;
		String status = null;
		String currCodeBase = null;
		String sExchRate = "1";
		String retOpt = null;
		String invoiceId = null,tempInvoiceId=null;
		String lineNoTrace = null;
		int iLineNoTrace = 0 ;
		int lineNoInv = 0;
		String locCode = null;
		String lotNo = null;
		String lotSl = null;
		String tranMode = null; 
		String custCodeBil = null; 
		String custCodeDlv = null; 
		double exchRate = 1,totFreeCost = 0,effCostFree = 0; 
		String analCode = null; 
		String itemSer = null; 
		String tranDate = ""; 
		String invDate = null; 
		String salesPers = null; 
		String saleOrder = null; 
		String salesPers1 = null; 
		String salesPers2 = null;
		String varValue = null;
		String slineNo = null;
		int lineNo = 0;
		String retReplFlag = null;
		String sRate = "0";
		String taxChap = "", taxClass = "", taxEnv = "", stkOpt = "";
		String taxChapHdr = "", taxClassHdr = "", taxEnvHdr = "";
		String frStation = null, toStation = null;
		String unit = null, unitStd = null;
		double effAmt = 0, quantity = 0,convFact = 0,rate = 0, rateStdUom = 0, convRateStdUom = 0, rateClg = 0;
		String physicalQty = null, claimQty = null;
		ArrayList convAr = null;
		double dhipperQty = 0 , integralQty = 0;
		int ctr = 0;
		String loginSiteDescr = null;
		String reasnonDescr = null;
		String fullRet = null;
		String tranId = null;
		String projCode = null;
		String tranCode = null;
		String tranName = null;
		String tranType = null;
		double shipperQty = 0.0;
		String siteCodeMfg = null;
		java.sql.Timestamp mfgDate = null, tsTranDate = null;
		java.sql.Timestamp expDate = null, mexpDate = null, mmfgDate = null,
				mtranDate = null, ldtLrdate = null, mchkDate = null;
		String despId = null;
		String despLineNo = null;
		double rateStd = 0.0;
		double costRate = 0.0;

		double amtDrCr = 0.0;

		double sQuantity = 0.0;
		String taxClasss = null;
		double sRateClg = 0.0;
		String sorder = null;
		String sordLineNo = null;
		String itemCodeOrd = null;
		String lineType = null;
		String itemRef = null;
		String pircleListClg = null, locDescr = null,  mdescr = null, mvarValue = null, mcode = null, lsInvoiceItem = "",mvarValue1 = null ;
		double amount = 0.0, drcrAmount = 0.0, mminShlife = 0.0, mconvRtuom, mVal= 0.0, mNum= 0.0, lcPartQty= 0.0, mquantity= 0.0,
				minShelfLife = 0.0, freeQty = 0.0, discount = 0.0, mrateStd= 0.0, mrate= 0.0, lcPhyQty= 0.0, lcNetWt= 0.0, lcGrossWt= 0.0, lcTareWt= 0.0;
		double qtyStdUom= 0.0, lcDiscount= 0.0, lcQtyPerArt = 0.0, mshlife = 0.0, effCost = 0.0;
		String packCode = null,  lsInvoiceId = null, mstkOpt = null;
		String unitRate = null;
		String itemStru = null;
		String packInstr = null;
		String channelPartner = null,  mlocCode = null;
		String lsDamagedtype = null, mitemCode = null, lserrCode = null;
		String sqlStr = null, mloc = null, lsLocCode = null;
		int mdays = 0, mcount = 0, mcount1 = 0 , mlineNoInv = 0;
		double mdisc = 0.0;
		String mvarName = null, lsSretLocCode = null, lsInvStat = null, sretLocCode = null, mvarName1 = null;
		String mstatus = null, lsDespLineNo = null, lsDespId = null, lsIinvoiceId = null, taxCal = null;
		String mreasonDescr = null, mreasCode = null, freeItemCode = null, mitem = null;
		//String siteCode = null;
		double priceListRate = 0.0;
		double mNum1= 0.0, mNum2= 0.0, mVal1= 0.0, fact= 0.0;
		int llLinenotrace = 0;
		int srNo = 0;
		int pos = 0;
		int mdiffDays;
		int noArt = 0;
		String listType = null;
		SysCommon sysCommon = null;
		String lsRetRepFlag = null;
		double lcQty = 0.0;
		HashMap infoMap = null;
		String valStr = null;
		String siteCode3 = null;
		int cntItemLotInfo=0;
		double qutyAdj=0.0;
		//Added by Santosh on 27/04/2017 to get available invoice Id [Start]
		String invoiceQty = "";// adjQty = "";
		HashMap<String, String> curFormItemLotHMap = new HashMap<String, String>();
		HashMap<String, String> curRecordItemLotHMap = new HashMap<String, String>();
		String sreturnAdjOpt = "", orderByStr = "";
		//Added by Santosh on 27/04/2017 to get available invoice Id [End]
		//Added by Santosh on 16/05/2017 [Start]
		String minRateDocKey = "";
		boolean isMinHisRateSet = false;
		//Added by Santosh on 16/05/2017 [End]
		String invRefId = "",invRefDate="" ; //Added By PriyankaC on 14/06/2018.
		String shExpDiscAppl="";
		Timestamp invRefDateStr=null;// Added by Nandkumar Gadkari on 19MAR2018
		ArrayList<String> dokkeyList=null; // Added by Nandkumar Gadkari on 14/09/18
		String expDateStrg="",mfgDateStrg="";// added by nandkumar gadkari on 28/01/19
		boolean cpFlag = false;
		int mrhCnt=0;// added by nandkumar gadkari on 13/09/19
		double srDQuantity =0,adjQty=0,domTotalQty=0;
		try
		{
			// Changed by Sneha on 22-07-2016 for exchange rate [Start]
			java.util.Date currDate1 = new java.util.Date();
			String applDateFormat = genericUtility.getApplDateFormat();
			String dbDateFormat = genericUtility.getDBDateFormat();
			SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
			String currDateStr = sdf.format(currDate1);

			FinCommon finCommon = new FinCommon();
			// Changed by Sneha on 22-07-2016 for exchange rate [End]

			ITMDBAccessEJB dbEjb = new ITMDBAccessEJB();
			sysCommon = new SysCommon();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			//System.out.println("FORM NO:::"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			//System.out.println(".....call item change ....");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver=null;
			System.out.println(">>>>>>>>>>>In IC currentFormNo:"+currentFormNo);
			switch( currentFormNo )
			{			
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item( 0 );

				winName = getObjName(parentNode);
				System.out.println( "winName :: " + winName );
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				if (currentColumn.trim().equals("itm_default") )			
				{	
					loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
					siteCode3  = getAbsString(genericUtility.getColumnValue("site_code",dom));	  //added by ritesh on 09/07/13 start
					if(loginSiteCode == null || loginSiteCode.trim().length() <= 0)						
					{
						loginSiteCode = siteCode3;
					}																					//added by ritesh on 09/07/13 end												
					//get login site description
					sql = " select descr from site where site_code = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, loginSiteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						loginSiteDescr = rs.getString( "descr" ); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<site_code protect =\"1\">").append("<![CDATA["+loginSiteCode.trim()+"]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA["+loginSiteDescr.trim()+"]]>").append("</site_descr>");

					String currAppdate ="";
					java.sql.Timestamp currDate = null;
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
					valueXmlString.append("<eff_date protect =\"0\">").append("<![CDATA["+currAppdate.trim()+"]]>").append("</eff_date>");
					valueXmlString.append("<tran_date protect =\"0\">").append("<![CDATA["+currAppdate.trim()+"]]>").append("</tran_date>");
					valueXmlString.append("<tax_date protect =\"0\">").append("<![CDATA["+currAppdate.trim()+"]]>").append("</tax_date>");

					priceListClgYn = distCommon.getDisparams("999999","PRICE_LIST__CLG_YN",conn);
					priceListClgYn = priceListClgYn == null ?"" : priceListClgYn.trim();
					if(priceListClgYn.equalsIgnoreCase("NULLFOUND"))
					{
						priceListClgYn="";
					}
					if("Y".equalsIgnoreCase(priceListClgYn))
					{
						priceListClg = distCommon.getDisparams("999999","PRICE_LIST__CLG",conn);
						priceListClg = priceListClg == null ?"" : priceListClg.trim();
						if(priceListClg.equalsIgnoreCase("NULLFOUND"))
						{
							priceListClg="";
						}
						valueXmlString.append("<price_list__clg protect =\"0\">").append(getAbsString(priceListClg)).append("</price_list__clg>");
					}
					else
					{
						valueXmlString.append("<price_list__clg protect =\"0\">").append("").append("</price_list__clg>");	
					}
					if ( "w_salesreturn_retn".equalsIgnoreCase(winName) )
					{
						valueXmlString.append("<ret_opt protect =\"0\">").append("C").append("</ret_opt>");	
					}
					else if ( "w_salesreturn_repl".equalsIgnoreCase(winName) )
					{
						valueXmlString.append("<ret_opt protect =\"1\">").append("R").append("</ret_opt>");	
					}
					else
					{
						valueXmlString.append("<ret_opt protect =\"0\">").append("").append("</ret_opt>");	
					}

					valueXmlString.append("<exch_rate protect =\"0\">").append("0").append("</exch_rate>");	
					valueXmlString.append("<invoice_id protect =\"0\">").append("").append("</invoice_id>");	
					valueXmlString.append("<cust_code protect =\"0\">").append("").append("</cust_code>");	
					valueXmlString.append("<item_ser protect =\"0\">").append("").append("</item_ser>");	
					valueXmlString.append("<full_ret protect =\"0\">").append("N").append("</full_ret>");	


					reasCode = distCommon.getDisparams("999999","DEFAULT_REAS_CODE",conn);
					reasCode = reasCode == null ?"" : reasCode.trim();
					if("NULLFOUND".equalsIgnoreCase(reasCode))
					{
						reasCode = "";
					}
					valueXmlString.append("<reas_code>").append(reasCode).append("</reas_code>");	
					sql = " Select reason_descr from sreturn_reason where reason_code = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, reasCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						reasnonDescr = rs.getString( 1 ); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					if(reasnonDescr == null || reasnonDescr.trim().length() == 0)
					{
						reasnonDescr = "";
					}
					valueXmlString.append("<reason_descr>").append(reasnonDescr).append("</reason_descr>");	

				}
				else if ( currentColumn.trim().equals("itm_defaultedit") )			
				{	

					tranId = genericUtility.getColumnValue("tran_id",dom);
					retOpt = genericUtility.getColumnValue("ret_opt",dom);

					sql = " select sreturn.confirmed, sreturn.site_code, sreturn.curr_code, "
							+ " sreturn.status, finent.curr_code, sreturn.exch_rate "
							+ " from sreturn, site, finent  "
							+ " where site.site_code = sreturn.site_code "
							+ " and finent.fin_entity = site.fin_entity "
							+ " and sreturn.tran_id = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranId );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						confirmed = rs.getString(1); 
						siteCode = rs.getString(2); 
						currCode = rs.getString(3); 
						status = rs.getString(4); 
						currCodeBase = rs.getString(5);
						sExchRate = rs.getString(6);	
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					currCode = currCode == null ?"" : currCode.trim();
					currCodeBase = currCodeBase == null ?"" : currCodeBase.trim();
					if ( !( currCode.trim().equalsIgnoreCase( currCodeBase.trim() ) ) )
					{
						valueXmlString.append("<exch_rate protect =\"0\">").append(sExchRate).append("</exch_rate>");	
					}
					if(currCode.trim().length()>0 && ( currCode.trim().equalsIgnoreCase( currCodeBase.trim() )))//added if condition by nandkumar gadkari on 23/01/20
					{
						valueXmlString.append("<exch_rate protect =\"1\">").append("1").append("</exch_rate>");	
					}

					if ("w_salesreturn_retn".equalsIgnoreCase(winName) || "w_salesreturn_repl".equalsIgnoreCase(winName))
					{
						valueXmlString.append("<ret_opt protect =\"1\">").append(retOpt).append("</ret_opt>");	
					}
					else
					{
						sql = " select count(1) from sreturndet where tran_id = ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, tranId );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt = rs.getInt(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						if (cnt > 0 )
						{
							valueXmlString.append("<ret_opt protect =\"1\">").append(retOpt).append("</ret_opt>");	
						}
						else
						{
							valueXmlString.append("<ret_opt protect =\"0\">").append(retOpt).append("</ret_opt>");	
						}
					}

					sql = " select count(1) from sreturn_inv where tran_id = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranId );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						cnt = rs.getInt(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					String adjMiscCrn = genericUtility.getColumnValue("adj_misc_crn",dom);
					if (cnt > 0 )
					{
						valueXmlString.append("<adj_misc_crn protect =\"1\">").append(adjMiscCrn).append("</adj_misc_crn>");	
					}
					else
					{
						valueXmlString.append("<adj_misc_crn protect =\"0\">").append(adjMiscCrn).append("</adj_misc_crn>");	
					}


					sql = " select count(1) from sreturndet where tran_id = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranId );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						cnt = rs.getInt(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					custCode = getAbsString(genericUtility.getColumnValue("cust_code",dom));
					siteCode = getAbsString(genericUtility.getColumnValue("site_code",dom));
					itemSer = getAbsString(genericUtility.getColumnValue("item_ser",dom));
					priceList = getAbsString(genericUtility.getColumnValue("price_list",dom));
					priceListClg = getAbsString(genericUtility.getColumnValue("price_list__clg",dom));
					invoiceId = getAbsString(genericUtility.getColumnValue("invoice_id",dom));
					fullRet = getAbsString(genericUtility.getColumnValue("full_ret",dom));



					if (cnt > 0 )
					{
						valueXmlString.append("<cust_code protect =\"1\">").append(getAbsString(custCode)).append("</cust_code>");	
						valueXmlString.append("<site_code protect =\"1\">").append(getAbsString(siteCode)).append("</site_code>");	
						valueXmlString.append("<item_ser protect =\"1\">").append(getAbsString(itemSer)).append("</item_ser>");	
						valueXmlString.append("<price_list protect =\"1\">").append(getAbsString(priceList)).append("</price_list>");	
						valueXmlString.append("<price_list__clg protect =\"1\">").append(getAbsString(priceListClg)).append("</price_list__clg>");	
						valueXmlString.append("<invoice_id protect =\"1\">").append(getAbsString(invoiceId)).append("</invoice_id>");	
						valueXmlString.append("<full_ret protect =\"1\">").append(getAbsString(fullRet)).append("</full_ret>");	
					}
					else
					{
						valueXmlString.append("<cust_code protect =\"0\">").append(getAbsString(custCode)).append("</cust_code>");	
						valueXmlString.append("<full_ret protect =\"0\">").append((fullRet)).append("</full_ret>");	

						valueXmlString.append("<site_code protect =\"1\">").append(getAbsString(siteCode)).append("</site_code>");
						valueXmlString.append("<item_ser protect =\"0\">").append(getAbsString(itemSer)).append("</item_ser>");	
						valueXmlString.append("<price_list protect =\"0\">").append(getAbsString(priceList)).append("</price_list>");	
						valueXmlString.append("<price_list__clg protect =\"0\">").append(getAbsString(priceListClg)).append("</price_list__clg>");
					}
					//added by nandkumar gadkari on 02/01/20----------------start-----------------
					// commented by manoharan 17-mar-2021 column renamed to cust_code__bil
					//custCodeBil = getAbsString(genericUtility.getColumnValue("cust_code__bill",dom));
					custCodeBil = getAbsString(genericUtility.getColumnValue("cust_code__bil",dom));
					custCodeDlv = getAbsString(genericUtility.getColumnValue("cust_code__dlv",dom));
					if(invoiceId != null && invoiceId.trim().length() > 0)
					{
						// commented by manoharan 17-mar-2021 column renamed to cust_code__bil
						//valueXmlString.append("<cust_code__bill protect =\"1\">").append(getAbsString(custCodeBil)).append("</cust_code__bill>");	
						valueXmlString.append("<cust_code__bil protect =\"1\">").append(getAbsString(custCodeBil)).append("</cust_code__bil>");
						valueXmlString.append("<cust_code__dlv protect =\"1\">").append(getAbsString(custCodeDlv)).append("</cust_code__dlv>");	
					}
					//added by nandkumar gadkari on 02/01/20----------------end-----------------

				}
				else if (currentColumn.trim().equals("tran_date") )			
				{
					tranDate = genericUtility.getColumnValue("tran_date",dom);
					valueXmlString.append("<eff_date>").append(tranDate).append("</eff_date>");
					invoiceId = genericUtility.getColumnValue("invoice_id",dom);
					if (invoiceId == null || invoiceId.trim().length() == 0)
					{
						valueXmlString.append("<tax_date>").append(tranDate).append("</tax_date>");
					}

				}	
				else if (currentColumn.trim().equals("invoice_id") )			
				{
					invoiceId = genericUtility.getColumnValue("invoice_id",dom);
					tranDate = genericUtility.getColumnValue("tran_date",dom);
					if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) )
					{
						sql = " select tran_mode, site_code, cust_code__bil, curr_code, " 
								+ " exch_rate, anal_code, item_ser, tran_date, sales_pers, "
								+ " cust_code, sale_order, sales_pers__1, sales_pers__2 "
								+ " from invoice where invoice_id = ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, invoiceId );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							tranMode = rs.getString(1); 
							System.out.println( "tranMode1 :: " + tranMode );
							siteCode = rs.getString(2); 
							custCodeBil = rs.getString(3); 
							currCode = rs.getString(4); 
							exchRate = rs.getDouble(5); 
							analCode = rs.getString(6); 
							itemSer = rs.getString(7); 
							invDate = rs.getString(8); 
							salesPers = rs.getString(9); 
							custCode = rs.getString(10); 
							saleOrder = rs.getString(11); 
							salesPers1 = rs.getString(12); 
							salesPers2 = rs.getString(13); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						tranMode = tranMode == null ? "" : tranMode;								
						siteCode = siteCode == null ? "" : siteCode;
						custCodeBil = custCodeBil == null ? "" : custCodeBil;
						currCode = currCode == null ? "" : currCode;
						analCode = analCode == null ? "" : analCode;
						itemSer = itemSer == null ? "" : itemSer;
						invDate = invDate == null ? "" : invDate;
						salesPers = salesPers == null ? "" : salesPers;
						custCode = custCode == null ? "" : custCode;
						saleOrder = saleOrder == null ? "" : saleOrder;
						salesPers1 = salesPers1 == null ? "" : salesPers1;
						salesPers2 = salesPers2 == null ? "" : salesPers2;

						sql = " select count(distinct SORD_NO) "
								+ " from invdet "
								+ " where invoice_id = ? " ;
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, invoiceId );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt = rs.getInt(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						if(cnt > 1 )
						{
							valueXmlString.append("<price_list protect =\"0\">").append("").append("</price_list>");	
							valueXmlString.append("<price_list__clg protect =\"0\">").append("").append("</price_list__clg>");
						}
						else
						{
							sql = " select price_list,price_list__clg, proj_code from sorder where sale_order = ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, saleOrder );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								priceList = rs.getString(1); 
								priceListClg = rs.getString(2);
								projCode =  rs.getString(3);
								valueXmlString.append("<price_list protect =\"1\">").append( getAbsString( priceList ) ).append("</price_list>");	
								setNodeValue( dom, "price_list", getAbsString( priceList ) );
								valueXmlString.append("<price_list__clg protect =\"0\">").append( getAbsString( priceListClg ) ).append("</price_list__clg>");
								setNodeValue( dom, "price_list__clg", getAbsString( priceListClg ) );
								valueXmlString.append("<proj_code>").append(getAbsString(projCode)).append("</proj_code>");
								setNodeValue( dom, "proj_code", getAbsString(projCode) );
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						}
						//Pavan Rane 11jun19 start [to store the channel partner flag]
						cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
						if(cpFlag)
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
						}else 
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");
						}
						//Pavan Rane 11jun19 end				

						//	commented by nandkumar gadkari on 23/01/20
						/*reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
							//System.out.println("modified string [" + reStr + "]");
							//<?xml version="1.0"?><Root><header><editFlag>E</editFlag></header><Detail2><item_ser__prom><![CDATA[CO]]></item_ser__prom><descr><![CDATA[FERTIGYN 5000FERTIGYN]]></descr><item_ser><![CDATA[ZT]]></item_ser><unit><![CDATA[A01]]></unit><loc_type><![CDATA[C1]]></loc_type><curr_stk><![CDATA[200000.0]]></curr_stk><st_scheme><![CDATA[]]></st_scheme><st_scheme><![CDATA[Integral Quantity :4.0]]></st_scheme><cust_item__ref><![CDATA[0132                ]]></cust_item__ref><cust_item_ref_descr><![CDATA[SAWAN TEST ]]></cust_item_ref_descr></Detail2></Root>
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);*/
						//System.out.println("modified string after [" + reStr + "]");

						valueXmlString.append("<tran_mode>").append("<![CDATA["+ getAbsString( tranMode ) +"]]>").append("</tran_mode>");
						System.out.println( "tranMode2 :: " + tranMode );
						setNodeValue( dom, "tran_mode", getAbsString( tranMode ) );
						// 12/02/14 commented by manoharan UAT issue tracker # 250
						//valueXmlString.append("<site_code>").append("<![CDATA["+ getAbsString( siteCode ) +"]]>").append("</site_code>");
						//setNodeValue( dom, "site_code", getAbsString( siteCode ) );
						valueXmlString.append("<sales_pers>").append("<![CDATA["+ getAbsString( salesPers ) +"]]>").append("</sales_pers>");
						setNodeValue( dom, "sales_pers", getAbsString( salesPers ) );
						valueXmlString.append("<sales_pers__1>").append("<![CDATA["+ getAbsString( salesPers1 ) +"]]>").append("</sales_pers__1>");
						setNodeValue( dom, "sales_pers__1", getAbsString( salesPers1 ) );
						valueXmlString.append("<sales_pers__2>").append("<![CDATA["+ getAbsString( salesPers2 )+"]]>").append("</sales_pers__2>");
						setNodeValue( dom, "sales_pers__2", getAbsString( salesPers2 ) );

						reStr = itemChanged(dom, dom1, dom2, objContext, "sales_pers__1", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

						reStr = itemChanged(dom, dom1, dom2, objContext, "sales_pers__2", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

						reStr = itemChanged(dom, dom1, dom2, objContext, "sales_pers", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						// 12/02/14 commented by manoharan UAT issue tracker # 250
						/*sql = " select descr from site where site_code = ?";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, siteCode );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								descr = rs.getString(1); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							valueXmlString.append("<site_descr>").append("<![CDATA["+getAbsString(descr)+"]]>").append("</site_descr>");
							setNodeValue( dom, "site_descr", getAbsString( descr ) );
						 */
						sql = " select cust_code__dlv from sorder where sale_order =  ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, saleOrder );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							custCodeDlv = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<cust_code protect =\"1\">").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
						setNodeValue( dom, "cust_code", getAbsString( custCode ) );
						reStr = itemChanged(dom, dom1, dom2, objContext, "cust_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						// commented by manoharan 17-mar-2021 column renamed to cust_code__bil
						//valueXmlString.append("<cust_code__bill protect =\"1\">").append("<![CDATA["+getAbsString(custCodeBil)+"]]>").append("</cust_code__bill>");//column protected by nandkumar gadkari on 02/01/20
						//setNodeValue( dom, "cust_code__bill",  custCodeBil );
						valueXmlString.append("<cust_code__bil protect =\"1\">").append("<![CDATA["+getAbsString(custCodeBil)+"]]>").append("</cust_code__bil>");//column protected by nandkumar gadkari on 02/01/20
						setNodeValue( dom, "cust_code__bil",  custCodeBil );
						valueXmlString.append("<cust_code__dlv protect =\"1\">").append("<![CDATA["+getAbsString(custCodeDlv)+"]]>").append("</cust_code__dlv>");//column protected by nandkumar gadkari on 02/01/20
						setNodeValue( dom, "cust_code__dlv",  getAbsString(custCodeDlv) );
						sql = " select cust_name from customer where cust_code  =  ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCodeDlv );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							custName = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<cust_name__dlv>").append("<![CDATA["+getAbsString(custName)+"]]>").append("</cust_name__dlv>");
						setNodeValue( dom, "cust_name__dlv",  getAbsString(custName) );
						sql = " select cust_name, tran_code from customer where cust_code  =  ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							custName = rs.getString(1); 
							tranCode = rs.getString(2);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<cust_name>").append("<![CDATA["+getAbsString(custName)+"]]>").append("</cust_name>");
						valueXmlString.append("<tran_code>").append("<![CDATA["+getAbsString(tranCode)+"]]>").append("</tran_code>");
						valueXmlString.append("<curr_code>").append("<![CDATA["+getAbsString(currCode)+"]]>").append("</curr_code>");

						sql = " select tran_name from transporter where tran_code  =  ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, tranCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							tranName = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<tran_name>").append("<![CDATA["+ getAbsString( tranName ) +"]]>").append("</tran_name>");
						// added by nandkumar gadkari on 23/01/20----------start------------

						sql = " select a.curr_code from finent a, site b "
								+ " where a.fin_entity = b.fin_entity "
								+ "  and b.site_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, siteCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							currCodeBase = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						// added by nandkumar gadkari on 23/01/20----------end------------
						currCodeBase = currCodeBase == null ?"" : currCodeBase.trim();
						sql = " select descr from currency where curr_code  =  ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, currCodeBase );//hange currency to baase currency
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							descr = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						valueXmlString.append("<currency_descr>").append("<![CDATA["+getAbsString(descr)+"]]>").append("</currency_descr>");
						//valueXmlString.append("<curr_code__bc>").append("<![CDATA["+getAbsString(currCode)+"]]>").append("</curr_code__bc>");
						valueXmlString.append("<curr_code__bc>").append("<![CDATA["+getAbsString(currCodeBase)+"]]>").append("</curr_code__bc>");//commented and added by nandkumar gadkari on 23/01/20
						// Changed by Sneha on 22-07-2016 for exchange rate [Start]
						Double exchRateFinn = finCommon.getDailyExchRateSellBuy(currCode.trim(),"",siteCode.trim(),currDateStr.trim(),"S", conn);
						//valueXmlString.append("<exch_rate>").append("<![CDATA["+ exchRate +"]]>").append("</exch_rate>");
						valueXmlString.append("<exch_rate>").append("<![CDATA["+ exchRateFinn +"]]>").append("</exch_rate>");
						// Changed by Sneha on 22-07-2016 for exchange rate [End]

						valueXmlString.append("<anal_code>").append("<![CDATA["+getAbsString(analCode)+"]]>").append("</anal_code>");
						valueXmlString.append("<item_ser protect =\"1\">").append("<![CDATA["+getAbsString(itemSer)+"]]>").append("</item_ser>");

						if (!"w_salesreturn_retn".equalsIgnoreCase(winName) && !"w_salesreturn_repl".equalsIgnoreCase(winName))
						{
							sql = " select count(1) from receivables " 
									+ " where tran_ser in ('S-INV','DRNRCP', 'CRNRCP') "
									+ " and cust_code = ? "
									+ " and item_ser  = ? "
									+ " and ( tot_amt - adj_amt ) > 0 "
									+ " and due_date <= ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, custCodeBil );
							pstmt.setString( 2, itemSer );
							//pstmt.setTimestamp( 1, tsTranDate );
							pstmt.setTimestamp( 3, Timestamp.valueOf(genericUtility.getValidDateString( tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0") );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								cnt = rs.getInt(1); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							if (cnt > 0)
							{
								valueXmlString.append("<ret_opt>").append("<![CDATA[C]]>").append("</ret_opt>");
							}
							else
							{
								valueXmlString.append("<ret_opt>").append("<![CDATA[R]]>").append("</ret_opt>");
							}								
						}
						if (invDate != null && invDate.trim().length() > 0 && !"null".equalsIgnoreCase( invDate.trim() ) )
						{
							valueXmlString.append("<tax_date>").append("<![CDATA[" + genericUtility.getValidDateString( invDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) + "]]>").append("</tax_date>");
						}
						else
						{
							valueXmlString.append("<tax_date protect =\"0\">").append("<![CDATA[" + tranDate + "]]>").append("</tax_date>");
						}
					}
					else
					{
						valueXmlString.append("<cust_code protect =\"0\">").append("<![CDATA["+""+"]]>").append("</cust_code>");
						valueXmlString.append("<item_ser protect =\"0\">").append("<![CDATA["+""+"]]>").append("</item_ser>");
						valueXmlString.append("<price_list protect =\"0\">").append("<![CDATA["+""+"]]>").append("</price_list>");
						valueXmlString.append("<tax_date protect =\"0\">").append("<![CDATA[" + tranDate + "]]>").append("</tax_date>");
					}
				}	
				else if (currentColumn.trim().equals("cust_code"))
				{
					custCode = genericUtility.getColumnValue("cust_code",dom);
					siteCode = genericUtility.getColumnValue("site_code",dom);
					String tranDateStr = genericUtility.getColumnValue("tran_date",dom);

					tsTranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0" ) ;
					invoiceId = genericUtility.getColumnValue("invoice_id",dom);
					sql = " select c.cust_name, c.tran_code, c.curr_code, " 
							+"	c.trans_mode, c.cust_code__bil, t.tran_name "
							+" from customer c left outer join transporter t on (c.tran_code = t.tran_code)"
							+" where cust_code = ? ";
					//+"	and c.tran_code = t.tran_code(+) "; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, custCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						custName = rs.getString(1); 
						tranCode = rs.getString(2); 
						currCode = rs.getString(3); 
						tranMode = rs.getString(4); 
						custCodeBil = rs.getString(5);
						tranName = rs.getString(6);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<cust_name>").append("<![CDATA[" + getAbsString( custName ) + "]]>").append("</cust_name>");

					setNodeValue( dom, "cust_name", getAbsString( custName ) );

					valueXmlString.append("<tran_code>").append("<![CDATA[" + getAbsString( tranCode ) + "]]>").append("</tran_code>");
					setNodeValue( dom, "tran_code", getAbsString( tranCode ) );
					valueXmlString.append("<tran_name>").append("<![CDATA[" + getAbsString( tranName ) + "]]>").append("</tran_name>");

					valueXmlString.append("<curr_code>").append("<![CDATA[" + getAbsString( currCode ) + "]]>").append("</curr_code>");
					setNodeValue( dom, "curr_code", getAbsString( currCode ) );

					valueXmlString.append("<trans_mode>").append("<![CDATA[" + getAbsString( tranMode ) + "]]>").append("</trans_mode>");
					setNodeValue( dom, "trans_mode", getAbsString( tranMode ) );

					valueXmlString.append("<curr_code__bc>").append("<![CDATA[" + getAbsString( currCode ) + "]]>").append("</curr_code__bc>");
					setNodeValue( dom, "curr_code__bc", getAbsString( currCode ) );
					// commented by manoharan 17-mar-2021 column renamed to cust_code__bil
					//valueXmlString.append("<cust_code__bill>").append("<![CDATA[" + getAbsString( custCodeBil ) + "]]>").append("</cust_code__bill>");
					//setNodeValue( dom, "cust_code__bill", getAbsString( custCodeBil ) );
					valueXmlString.append("<cust_code__bil>").append("<![CDATA[" + getAbsString( custCodeBil ) + "]]>").append("</cust_code__bil>");
					setNodeValue( dom, "cust_code__bil", getAbsString( custCodeBil ) );

					//valueXmlString.append("<cust_code__bill>").append("<![CDATA[" + getAbsString( custCodeBil ) + "]]>").append("</cust_code__bill>");
					//setNodeValue( dom, "cust_code__bill", getAbsString( custCodeBil ) );

					valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + getAbsString( custCode ) + "]]>").append("</cust_code__dlv>");
					setNodeValue( dom, "cust_code__dlv", getAbsString( custCode ) );

					valueXmlString.append("<cust_name__dlv>").append("<![CDATA[" + getAbsString( custName ) + "]]>").append("</cust_name__dlv>");
					setNodeValue( dom, "cust_name__dlv", getAbsString( custName ) );
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");
					}
					//Pavan Rane 11jun19 end										
					priceListClg = "";
					sql = " select price_list__clg from site_customer where cust_code = ? and site_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, siteCode );
					rs = pstmt.executeQuery(); 
					if ( rs.next() )
					{
						priceListClg = rs.getString(1); 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					priceListClg = priceListClg == null ?"" : priceListClg.trim();
					if( priceListClg == null || priceListClg.trim().length() == 0 )
					{
						sql = " select price_list__clg from customer where cust_code = ? "; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							priceListClg = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
					}
					priceListClg = priceListClg == null ?"" : priceListClg.trim();
					if (priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
					{
						valueXmlString.append("<price_list__clg>").append("<![CDATA[" + getAbsString( priceListClg ) + "]]>").append("</price_list__clg>");
						setNodeValue( dom, "price_list__clg", getAbsString( priceListClg ) );
					}

					sql = " select std_exrt, descr from currency where curr_code = ? "; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, currCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						exchRate = rs.getDouble(1);
						descr = rs.getString(2);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<exch_rate>").append(exchRate).append("</exch_rate>");
					setNodeValue( dom, "exch_rate", Double.toString( exchRate ) );

					valueXmlString.append("<currency_descr>").append("<![CDATA[" + getAbsString( descr ) + "]]>").append("</currency_descr>");
					setNodeValue( dom, "currency_descr", getAbsString( descr ) );

					System.out.println( "Calling curr_code ic :: " );
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);

					descr = null;
					sql = " select tran_name from transporter where tran_code = ? "; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<tran_name>").append("<![CDATA[" + getAbsString( descr ) + "]]>").append("</tran_name>");

					if (!"w_salesreturn_retn".equalsIgnoreCase(winName) && !"w_salesreturn_repl".equalsIgnoreCase(winName))
					{
						sql = " select count(1) from receivables " 
								+ " where tran_ser in ('S-INV','DRNRCP', 'CRNRCP') "
								+ " and cust_code = ? "
								+ " and item_ser  = ? "
								+ " and ( tot_amt - adj_amt ) > 0 "
								+ " and due_date <= ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCodeBil );
						pstmt.setString( 2, itemSer );
						pstmt.setTimestamp( 3, ( tsTranDate == null ? new java.sql.Timestamp(System.currentTimeMillis()) : tsTranDate ) );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt = rs.getInt(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						if (cnt > 0)
						{
							retOpt =  "C";
						}
						else
						{
							retOpt = "R";
						}
						valueXmlString.append("<ret_opt>").append("<![CDATA[" + getAbsString( retOpt ) + "]]>").append("</ret_opt>");
						setNodeValue( dom, "ret_opt", getAbsString( retOpt ) );
					}
					reStr = itemChanged(dom, dom1, dom2, objContext, "ret_opt", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
				}
				else if (currentColumn.trim().equals("sr_no") )			
				{
					srNo = Integer.parseInt( genericUtility.getColumnValue( "sr_no", dom ) );
					sql = " select a.invoice_id from invoice_srnodet A, invoice_srnohdr b " 
							+ " where a.invoice_id = b.invoice_id "
							+ " and  a.sr_no = ? "
							+ " and b.tran_date = (select max(c.tran_date) "
							+ " from invoice_srnohdr c,invoice_srnodet d "
							+ " where d.invoice_id = c.invoice_id "
							+ " and d.sr_no = ? ";							
					pstmt= conn.prepareStatement( sql );
					pstmt.setInt( 1, srNo );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						invoiceId = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<invoice_id>").append(invoiceId).append("</invoice_id>");
					setNodeValue( dom, "invoice_id", invoiceId );

					reStr = itemChanged(dom, dom1, dom2, objContext, "invoice_id", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);

				}	
				else if (currentColumn.trim().equals("cust_code__dlv") )			
				{
					custCodeDlv = genericUtility.getColumnValue("cust_code__dlv",dom);
					sql = " select cust_name from customer where cust_code = ? "; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, custCodeDlv );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						custName = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<cust_name__dlv>").append("<![CDATA[" + getAbsString( custName ) + "]]>").append("</cust_name__dlv>");
				}
				else if (currentColumn.trim().equals("curr_code") )			
				{
					/*currCode = genericUtility.getColumnValue("curr_code",dom);
						sql = " select descr, std_exrt from currency where curr_code = ?"; 
					    pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, currCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							descr = rs.getString(1); 
							exchRate = rs.getDouble(2);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<currency_descr>").append("<![CDATA[" + getAbsString( descr ) + "]]>").append("</currency_descr>");
						valueXmlString.append("<curr_code__bc>").append("<![CDATA[" + getAbsString( currCode ) + "]]>").append("</curr_code__bc>");
						valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
						siteCode = genericUtility.getColumnValue("site_code",dom);
						sql = " select a.curr_code from finent a, site b "
						 	+ " where a.fin_entity = b.fin_entity "
						 	+ "  and b.site_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, siteCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							currCodeBase = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						currCode = currCode == null ?"" : currCode.trim();
						currCodeBase = currCodeBase == null ?"" : currCodeBase.trim();
						if ( currCode != null && !currCode.trim().equalsIgnoreCase(currCodeBase.trim()))
						{
							valueXmlString.append("<exch_rate protect =\"0\">").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
						}
						else
						{
							valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA[1]]>").append("</exch_rate>");
						}
					 */
					//	commented by nandkumar gadkari on  23/01/20

					//added by nandkumar gadkari on 23/01/20

					currCode = genericUtility.getColumnValue("curr_code",dom);
					currCode = currCode == null ?"" : currCode.trim();

					siteCode = genericUtility.getColumnValue("site_code",dom);
					sql = " select a.curr_code from finent a, site b "
							+ " where a.fin_entity = b.fin_entity "
							+ "  and b.site_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						currCodeBase = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					currCodeBase = currCodeBase == null ?"" : currCodeBase.trim();
					tranDate = genericUtility.getColumnValue("tran_date",dom);
					exchRate = finCommon.getDailyExchRateSellBuy(currCode, "", siteCode,tranDate , "S", conn);

					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, currCodeBase);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<currency_descr>").append("<![CDATA[" + getAbsString( descr ) + "]]>").append("</currency_descr>");
					valueXmlString.append("<curr_code__bc>").append("<![CDATA[" + getAbsString( currCodeBase ) + "]]>").append("</curr_code__bc>");


					if ( currCode != null && !currCode.trim().equalsIgnoreCase(currCodeBase.trim()))
					{
						valueXmlString.append("<exch_rate protect =\"0\">").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
					}
					else
					{
						valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA[1]]>").append("</exch_rate>");
					}



				}
				else if (currentColumn.trim().equals("tran_code") )			
				{
					tranCode = genericUtility.getColumnValue("tran_code",dom);
					sql = " select tran_name from transporter where tran_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<tran_name>").append("<![CDATA[" + getAbsString( descr ) + "]]>").append("</tran_name>");
				}
				else if (currentColumn.trim().equals("site_code") )			
				{
					siteCode = genericUtility.getColumnValue("site_code",dom);
					sql = " select descr from site where site_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<site_descr>").append("<![CDATA[" + getAbsString( descr ) + "]]>").append("</site_descr>");
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					custCode = genericUtility.getColumnValue("cust_code",dom);
					cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");
					}
					//Pavan Rane 11jun19 end

					reStr = itemChanged(dom, dom1, dom2, objContext, "ret_opt", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
				}
				else if (currentColumn.trim().equals("ret_opt") )			
				{
					retOpt = genericUtility.getColumnValue("ret_opt",dom);
					custCode = genericUtility.getColumnValue("cust_code",dom);
					siteCode = genericUtility.getColumnValue("site_code",dom);

					priceList = "";
					sql = " select price_list from site_customer where cust_code = ? and site_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, siteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						priceList = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					priceList = priceList == null ?"" : priceList.trim();
					if (priceList == null || priceList.trim().length() == 0)
					{
						sql = " select price_list from customer where cust_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							priceList = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
					}		
					retOpt = retOpt == null ?"" : retOpt.trim();						
					if ("R".equals(retOpt) || "D".equals(retOpt) )
					{
						varValue = distCommon.getDisparams("999999","REPLACE_PLIST",conn);
						varValue = varValue == null ?"" : varValue.trim();
						if ( varValue != null && !"NULLFOUND".equals(varValue) && varValue.trim().length() > 0 )
						{
							priceList = varValue;
						}

					}
					valueXmlString.append("<price_list>").append("<![CDATA[" + getAbsString(priceList) + "]]>").append("</price_list>");
				}
				else if (currentColumn.trim().equals("sales_pers") )			
				{
					salesPers = genericUtility.getColumnValue("sales_pers",dom);
					sql = " select sp_name from sales_pers where sales_pers = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, salesPers );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<sp_name>").append("<![CDATA[" + descr + "]]>").append("</sp_name>");
				}
				else if (currentColumn.trim().equals("cust_code__trf") )			
				{
					custCode = genericUtility.getColumnValue("cust_code__trf",dom);
					sql = " select cust_name from customer where cust_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, custCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						custName = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<customer_cust_name>").append("<![CDATA[" + custName + "]]>").append("</customer_cust_name>");
				}
				else if (currentColumn.trim().equals("sales_pers__1") )			
				{
					salesPers = genericUtility.getColumnValue("sales_pers__1",dom);
					sql = " select sp_name from sales_pers where sales_pers = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, salesPers );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<sales_pers_sp_name>").append("<![CDATA[" + descr + "]]>").append("</sales_pers_sp_name>");
				}
				else if (currentColumn.trim().equals("sales_pers__2") )			
				{
					salesPers = genericUtility.getColumnValue("sales_pers__2",dom);
					sql = " select sp_name from sales_pers where sales_pers = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, salesPers );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<sales_pers_sp_name_1>").append("<![CDATA[" + descr + "]]>").append("</sales_pers_sp_name_1>");
				}
				else if (currentColumn.trim().equals("reas_code") )			
				{
					reasCode = genericUtility.getColumnValue("reas_code",dom);
					reasonDescr = "";
					sql = " Select reason_descr from sreturn_reason where reason_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, reasCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						reasonDescr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<reason_descr>").append("<![CDATA[" + reasonDescr + "]]>").append("</reason_descr>");
				}
				valueXmlString.append("</Detail1>");					
				break;

			case 2:

				System.out.println(">>>>>>>>>>>In IC case 2 currentFormNo:"+currentFormNo);
				parentNodeList = dom1.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				winName = getObjName(parentNode);
				System.out.println( "winName :: " + winName );
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;

				reStr = itemChanged(dom1, dom1, dom2, "1","itm_defaultedit", editFlag, xtraParams);

				reStr=reStr.substring(reStr.indexOf("<Detail1>"), reStr.indexOf("</Detail1>"));
				System.out.println("Detail 1String"+reStr);
				valueXmlString = new StringBuffer(
						"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
				valueXmlString.append(editFlag).append("</editFlag> </header>");
				valueXmlString.append(reStr);

				custCode = getAbsString(genericUtility.getColumnValue("cust_code",dom1));
				siteCode = getAbsString(genericUtility.getColumnValue("site_code",dom1));
				itemSer = getAbsString(genericUtility.getColumnValue("item_ser",dom1));
				priceList = getAbsString(genericUtility.getColumnValue("price_list",dom1));
				priceListClg = getAbsString(genericUtility.getColumnValue("price_list__clg",dom1));
				invoiceId = getAbsString(genericUtility.getColumnValue("invoice_id",dom1));
				fullRet = getAbsString(genericUtility.getColumnValue("full_ret",dom1));


				valueXmlString.append("<cust_code protect =\"1\">").append(getAbsString(custCode)).append("</cust_code>");	
				valueXmlString.append("<site_code protect =\"1\">").append(getAbsString(siteCode)).append("</site_code>");	
				valueXmlString.append("<item_ser protect =\"1\">").append(getAbsString(itemSer)).append("</item_ser>");	
				valueXmlString.append("<price_list protect =\"1\">").append(getAbsString(priceList)).append("</price_list>");	
				valueXmlString.append("<price_list__clg protect =\"1\">").append(getAbsString(priceListClg)).append("</price_list__clg>");	
				valueXmlString.append("<invoice_id protect =\"1\">").append(getAbsString(invoiceId)).append("</invoice_id>");	
				valueXmlString.append("<full_ret protect =\"1\">").append(getAbsString(fullRet)).append("</full_ret>");	
				valueXmlString.append("</Detail1>");

				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();

				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn.trim()))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength && !childNodeName.equals(currentColumn));

				// Added by Nandkumar Gadkari on 19MAR2018-----------start ----------
				shExpDiscAppl = distCommon.getDisparams( "999999", "SH_EXP_DISC_APPL", conn );
				System.out.println("shExpDiscAppl ["+shExpDiscAppl+"]  Disparam Value");
				if(shExpDiscAppl == null || shExpDiscAppl.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(shExpDiscAppl))
				{
					shExpDiscAppl="N";
				}
				// Added by Nandkumar Gadkari on 19MAR2018-----------end----------

				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
					fullRet = genericUtility.getColumnValue("full_ret",dom1);
					retOpt = genericUtility.getColumnValue("ret_opt",dom1);
					if (invoiceId == null || invoiceId.trim().length() == 0 || "null".equalsIgnoreCase( invoiceId.trim() ) )
					{
						valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");
						valueXmlString.append("<line_no__invtrace protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__invtrace>");
						valueXmlString.append("<item_code protect =\"0\">").append("<![CDATA["+""+"]]>").append("</item_code>");
					}
					else
					{
						valueXmlString.append("<invoice_id>").append("<![CDATA[" + invoiceId + "]]>").append("</invoice_id>");
						valueXmlString.append("<line_no__inv protect =\"0\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");
						valueXmlString.append("<line_no__invtrace protect =\"0\">").append("<![CDATA["+""+"]]>").append("</line_no__invtrace>");
						valueXmlString.append("<item_code protect =\"1\">").append("<![CDATA["+""+"]]>").append("</item_code>");
					}
					valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
					valueXmlString.append("<cost_rate protect =\"1\">").append("<![CDATA["+""+"]]>").append("</cost_rate>");
					if ("w_salesreturn_retn".equalsIgnoreCase(winName))
					{
						retOpt = retOpt == null ?"" : retOpt.trim();						
						if ("D".equalsIgnoreCase(retOpt))
						{
							valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[P]]>").append("</ret_rep_flag>");
						}
						else
						{
							valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[R]]>").append("</ret_rep_flag>");
						}
					}
					else if ("w_salesreturn_repl".equalsIgnoreCase(winName))
					{
						valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[P]]>").append("</ret_rep_flag>");
						valueXmlString.append("<status>").append("<![CDATA[S]]>").append("</status>");
						setNodeValue( dom, "status", "S" );
						setNodeValue( dom, "ret_rep_flag", "P" );
						tranId = genericUtility.getColumnValue("tran_id", dom1);
						slineNo = genericUtility.getColumnValue("line_no", dom);

						lineNo = Integer.parseInt( slineNo );
						if (lineNo == 1)
						{
							sql = " select case when max(line_no) is null then 0 else max(line_no) end from sreturndet where tran_id = ?"; 
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, tranId );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								lineNo = rs.getInt(1); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							lineNo++;
							valueXmlString.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>");
							reStr = itemChanged(dom, dom1, dom2, objContext, "ret_rep_flag", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
						//if there is value in invoice id on header screen, 
						//and replaced flag = P, then
						// dont set invoice id protected,
						//itemcode editable
						//inv trace line no non editable
						if( invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase(invoiceId) )
						{
							valueXmlString.append("<invoice_id protect =\"1\">").append("<![CDATA[" + "" + "]]>").append("</invoice_id>");
							valueXmlString.append("<line_no__invtrace protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__invtrace>");
							valueXmlString.append("<item_code protect =\"0\">").append("<![CDATA["+""+"]]>").append("</item_code>");

						}
						valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");							
					}
					else
					{
						retOpt = retOpt == null ?"" : retOpt.trim();						
						if ("C".equalsIgnoreCase(retOpt))
						{
							valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[R]]>").append("</ret_rep_flag>");
						}
						else if ("D".equalsIgnoreCase(retOpt))
						{
							valueXmlString.append("<ret_rep_flag protect =\"0\">").append("<![CDATA[P]]>").append("</ret_rep_flag>");
						}
						else
						{
							valueXmlString.append("<ret_rep_flag protect =\"0\">").append("<![CDATA["+""+"]]>").append("</ret_rep_flag>");
						}
					}
					valueXmlString.append("<full_ret>").append("<![CDATA[" + fullRet + "]]>").append("</full_ret>");
					varValue = distCommon.getDisparams("999999","ALLOC_FGLOC",conn);
					valueXmlString.append("<loc_code>").append("<![CDATA[" + varValue + "]]>").append("</loc_code>");
					sql = " select descr from location where loc_code = ? "; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, varValue );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<location_descr>").append("<![CDATA[" + descr + "]]>").append("</location_descr>");
					reasCode = genericUtility.getColumnValue("reas_code", dom1);
					valueXmlString.append("<reas_code>").append("<![CDATA[" + getAbsString(reasCode) + "]]>").append("</reas_code>");
					sql = " Select reason_descr from sreturn_reason where reason_code = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, reasCode );
					rs = pstmt.executeQuery(); 
					descr = "";
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<reason_descr>").append("<![CDATA[" + descr + "]]>").append("</reason_descr>");
					//valueXmlString.append("<lot_sl>").append("<![CDATA[2S]]>").append("</lot_sl>"); commited by Kunal Mandhre on 24/04/12 for DI2ASER001
					priceList = genericUtility.getColumnValue("price_list", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					tranDate = genericUtility.getColumnValue("tran_date", dom1);
					tranType = genericUtility.getColumnValue("tran_type", dom1);
					sql = " select udf_str1 from gencodes where fld_name='TRAN_TYPE' and mod_name='W_SALESRETURN' and fld_value = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranType );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						varValue = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					varValue = varValue == null ?"" : varValue.trim();						
					if (varValue != null && "Y".equalsIgnoreCase(varValue.trim()) )
					{
						valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[0]]>").append("</rate>");
					}
					else
					{
						varValue = distCommon.getDisparams("999999","SRET_RATE_EDITABLE",conn);
						varValue = varValue == null ?"" : varValue.trim();
						if ("Y".equalsIgnoreCase(varValue.trim()) )
						{
							fullRet = fullRet == null ?"" : fullRet.trim();
							if ("Y".equalsIgnoreCase(fullRet.trim()))
							{
								valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[0]]>").append("</rate>");
							}
							else
							{
								valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[0]]>").append("</rate>");
							}
						}
						else
						{
							fullRet = fullRet == null ?"" : fullRet.trim();
							retOpt = retOpt == null ?"" : retOpt.trim();
							if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) && "Y".equals(fullRet) && "C".equals(retOpt))
							{
								valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[0]]>").append("</rate>");
							}
							else
							{
								if (priceList == null || priceList.trim().length() == 0)
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[0]]>").append("</rate>");
								}
								else
								{
									varValue = distCommon.getPriceListType(priceList,conn);
									varValue = varValue == null ?"" : varValue.trim();
									fullRet = fullRet == null ?"" : fullRet.trim();
									//if ("B".equals(varValue) || "F".equals(varValue) || "Y".equals(fullRet)) condition change by nandkumar gadkari on 25/02/19
									if (!"L".equals(varValue) || "Y".equals(fullRet))
									{
										valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[0]]>").append("</rate>");
									}
									else
									{
										valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[0]]>").append("</rate>");
									}
								}
							}
						}
					}
					//valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+""+"]]>").append("</status>");
					if ("D".equals(retOpt))
					{
						valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[P]]>").append("</ret_rep_flag>");
						varValue = distCommon.getDisparams("999999","CALC_TAX_ON_REPLACE",conn);
						//tax_class, tax_chap and tax_env protect un protect	code commented by nandkumar gadkari on 25/02/19
						/*if ("N".equals(varValue) )
							{
								valueXmlString.append("<tax_chap protect =\"1\">").append("<![CDATA["+ "" +"]]>").append("</tax_chap>");
								valueXmlString.append("<tax_class protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
								valueXmlString.append("<tax_env protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
							}
							else
							{
								valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_chap>");
								valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
								valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
							}*/
					}
					//tax_class, tax_chap and tax_env protect un protect	code commented by nandkumar gadkari on 25/02/19
					/*else if ("R".equals(retOpt))
						{
							//valueXmlString.append("<ret_rep_flag protect =\"0\">").append("<![CDATA["+""+"]]>").append("</ret_rep_flag>");
							valueXmlString.append("<tax_chap protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
						}
						else
						{
							//valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA["+""+"]]>").append("</ret_rep_flag>");
							valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
						}*/
					valueXmlString.append("<stk_opt protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</stk_opt>");

					//itemchnage call  added by nandkumar gadkari on 11/09/19
					setNodeValue( dom, "reas_code", reasCode );
					reStr = itemChanged(dom, dom1, dom2, objContext, "reas_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);

				} // end itm_default

				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{

					retReplFlag = genericUtility.getColumnValue("ret_rep_flag",dom);
					if ("P".equals(retReplFlag))
					{
						valStr = getAbsString( genericUtility.getColumnValue( "site_code__mfg", dom ) ); 
						valueXmlString.append("<site_code__mfg protect =\"1\">").append("<![CDATA["+valStr+"]]>").append("</site_code__mfg>");

						valStr = getAbsString( genericUtility.getColumnValue( "mfg_date", dom ) ); 
						valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA["+valStr+"]]>").append("</mfg_date>");

						valStr = getAbsString( genericUtility.getColumnValue( "pack_code", dom ) ); 
						valueXmlString.append("<pack_code protect =\"1\">").append("<![CDATA["+valStr+"]]>").append("</pack_code>");
					}
					retOpt = genericUtility.getColumnValue("ret_opt",dom1);
					invoiceId = genericUtility.getColumnValue("invoice_id",dom);

					if (invoiceId == null || invoiceId.trim().length() == 0 )
					{
						valueXmlString.append("<line_no__invtrace protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__invtrace>");
					}
					else
					{
						String sLineNo = getNumString( genericUtility.getColumnValue("line_no__invtrace",dom) );	
						valueXmlString.append("<line_no__invtrace protect =\"0\">").append("<![CDATA[" + sLineNo + "]]>").append("</line_no__invtrace>");
					}
					fullRet = genericUtility.getColumnValue("full_ret",dom1);
					tranType = genericUtility.getColumnValue("tran_type",dom1);
					sql = " select udf_str1 from gencodes where fld_name='TRAN_TYPE' and mod_name='W_SALESRETURN' and fld_value = ?"; 
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, tranType );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						varValue = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					sRate = genericUtility.getColumnValue("rate",dom);
					if (varValue != null && "Y".equalsIgnoreCase(varValue.trim()) )
					{
						valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
					}
					else
					{
						//added by nandkumar gadkari on 25/02/19-----------start ---------to protect rate when it is from min_rate_history-----------
						String docKey="";
						boolean minRateHisRate=false;
						docKey = genericUtility.getColumnValue("doc_key",dom);
						sql = " select count(*) from min_rate_history where doc_key = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, docKey );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt = rs.getInt(1); 
							if(cnt > 0 )
							{
								minRateHisRate=true;
							}
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						//added by nandkumar gadkari on 25/02/19-----------end --------------------
						
						//added by rupali on 01/04/2021 [start]
						String historyType = ""; 
						sql = "SELECT DOC_KEY,EFF_COST,HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
								+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, docKey);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							historyType = checkNull(rs.getString("HISTORY_TYPE")); 
						}
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
						//added by rupali on 01/04/2021 [end]

						priceList = genericUtility.getColumnValue("price_list",dom1);
						varValue = distCommon.getDisparams("999999","SRET_RATE_EDITABLE",conn);
						varValue = varValue == null ?"" : varValue.trim();
						priceList = priceList == null ?"" : priceList.trim();
						fullRet = fullRet == null ?"" : fullRet.trim();
						retOpt = retOpt == null ?"" : retOpt.trim();
						if ("Y".equalsIgnoreCase(varValue.trim()) )
						{
							if ("Y".equalsIgnoreCase(fullRet.trim()) || minRateHisRate)// minRateHisRate added by nandkumar gadkari on 25/02/19
							{
								if("S".equalsIgnoreCase(historyType))
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
								}
								else
								{
									valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
								}
							}
							else
							{
								valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
							}
						}
						else
						{
							if (invoiceId != null && invoiceId.trim().length() > 0 && "Y".equals(fullRet) && "C".equals(retOpt))
							{
								if("S".equalsIgnoreCase(historyType))
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
								}
								else
								{
									valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
								}
							}
							else
							{
								if (priceList == null || priceList.trim().length() == 0)
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
								}
								else
								{
									varValue = distCommon.getPriceListType(priceList,conn);
									varValue = varValue == null ?"" : varValue.trim();
									//if ("B".equals(varValue) || "F".equals(varValue) || "Y".equals(fullRet)) condition change by nandkumar gadkari on 25/02/19
									if (!"L".equals(varValue) || "Y".equals(fullRet) ||minRateHisRate)// minRateHisRate added by nandkumar gadkari on 25/02/19
									{
										if("S".equalsIgnoreCase(historyType))
										{
											valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
										}
										else
										{
											valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
										}
									}
									else
									{
										valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
									}
								}
							}
						}
					}
					retReplFlag = getAbsString(genericUtility.getColumnValue("ret_rep_flag",dom));
					taxChap = getAbsString(genericUtility.getColumnValue("tax_chap",dom));
					taxClass = getAbsString(genericUtility.getColumnValue("tax_class",dom));
					taxEnv = getAbsString(genericUtility.getColumnValue("tax_env",dom));
					stkOpt = getAbsString(genericUtility.getColumnValue("stk_opt",dom));

					retReplFlag = getAbsString( retReplFlag );
					taxChap = getAbsString( taxChap );
					taxClass = getAbsString( taxClass );
					taxEnv = getAbsString( taxEnv );
					stkOpt = getAbsString( stkOpt );

					if ("D".equals(retOpt))
					{
						valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[" + retReplFlag + "]]>").append("</ret_rep_flag>");
						varValue = distCommon.getDisparams("999999","CALC_TAX_ON_REPLACE",conn);
						//tax_class, tax_chap and tax_env protect un protect	code commented by nandkumar gadkari on 25/02/19
						/*if ("N".equals(varValue) )
							{
								valueXmlString.append("<tax_chap protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_chap>");
								valueXmlString.append("<tax_class protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
								valueXmlString.append("<tax_env protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
							}
							else
							{
								valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[ " + taxChap + "]]>").append("</tax_chap>");
								valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
								valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
							}*/
					}
					else if ("R".equals(retOpt))
					{
						valueXmlString.append("<ret_rep_flag protect =\"0\">").append("<![CDATA[" + retReplFlag +"]]>").append("</ret_rep_flag>");
						//tax_class, tax_chap and tax_env protect un protect	code commented by nandkumar gadkari on 25/02/19
						/*valueXmlString.append("<tax_chap protect =\"1\">").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"1\">").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"1\">").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");*/
					}
					else
					{
						valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[" + retReplFlag + "]]>").append("</ret_rep_flag>");
						//tax_class, tax_chap and tax_env protect un protect	code commented by nandkumar gadkari on 25/02/19
						/*valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");*/
					}
					valueXmlString.append("<stk_opt protect =\"1\">").append("<![CDATA[" + stkOpt + "]]>").append("</stk_opt>");
					//Added by sarita on 29 JUN 18 [START] on edit mode if exp_date available against lot_no , lot_sl , item_code and site_code in stock then system should not allow to change exp_date
					String expDateStr = "";
					int cntExpDate = 0;
					expDateStr =  genericUtility.getColumnValue("exp_date",dom);
					siteCode = genericUtility.getColumnValue("site_code", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					lotNo = genericUtility.getColumnValue("lot_no", dom);
					lotSl = genericUtility.getColumnValue("lot_sl", dom);
					System.out.println("For  itemdefaultedit Values are expDateStr ["+expDateStr+"] \t siteCode ["+siteCode+"] \t  itemCode ["+itemCode+"] \t locCode ["+locCode+"] \t lotNo ["+lotNo+"] \t lotSl ["+lotSl+"]");
					if(expDateStr != null && expDateStr.trim().length() > 0)
					{
						sql = "select exp_date"
								+ " from stock"
								+ " where item_code = ? "
								+ " and site_code = ? "
								+ " and lot_no = ? "
								+ " and lot_sl = ? ";

						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode);
						pstmt.setString( 2, siteCode);
						pstmt.setString( 3, lotNo);
						pstmt.setString( 4, lotSl);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cntExpDate++;
							expDate = rs.getTimestamp("exp_date");
							System.out.println("[cntExpDate >> ["+cntExpDate+"]] && expDate >> ["+expDate+"]");
						}
						if(cntExpDate > 0)
						{
							valueXmlString.append("<exp_date  protect =\"1\">").append("<![CDATA[" + new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString()  + "]]>").append("</exp_date>");									
						}								
					}
					//Added by sarita on 29 JUN 18 [END] on edit mode if exp_date available against lot_no , lot_sl , item_code and site_code in stock then system should not allow to change exp_date
					//ADDED BY NANDKUMAR GADKARI ON 28/01/19--start
					mfgDateStrg = genericUtility.getColumnValue("mfg_date",dom);
					packCode = genericUtility.getColumnValue("pack_code",dom);
					siteCodeMfg = genericUtility.getColumnValue("site_code__mfg",dom);
					unit = genericUtility.getColumnValue("unit",dom);
					unitRate = genericUtility.getColumnValue("unit__rate",dom);
					unitStd = genericUtility.getColumnValue("unit__std",dom);
					varValue = genericUtility.getColumnValue("conv__qty_stduom",dom);
					valStr = genericUtility.getColumnValue("conv__rtuom_stduom",dom);
					unit = unit == null ?"" : unit.trim();
					unitRate = unitRate == null ?"" : unitRate.trim();
					unitStd = unitStd == null ?"" : unitStd.trim();
					if(mfgDateStrg !=null && mfgDateStrg.trim().length()>0)
					{
						valueXmlString.append("<mfg_date  protect =\"1\">").append("<![CDATA[" + (mfgDateStrg) + "]]>").append("</mfg_date>");
					}

					if(packCode !=null && packCode.trim().length()>0)
					{
						valueXmlString.append("<pack_code  protect =\"1\">").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
					}
					if(siteCodeMfg !=null && siteCodeMfg.trim().length()>0)
					{
						valueXmlString.append("<site_code__mfg  protect =\"1\">").append("<![CDATA[" + siteCodeMfg + "]]>").append("</site_code__mfg>");
					}
					if ( unit.trim().equals(unitRate.trim()) && unit.trim().length()>0 )
					{
						valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA["+valStr+"]]>").append("</conv__rtuom_stduom>");

					}
					if ( unit.trim().equals(unitStd.trim()) && unit.trim().length()>0 )
					{
						valueXmlString.append("<conv__qty_stduom protect =\"1\">").append("<![CDATA["+varValue+"]]>").append("</conv__qty_stduom>");

					}
					//ADDED BY NANDKUMAR GADKARI ON 28/01/19 ---end
				} // end itm_defaultedit
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{    
					System.out.println(">>>>>>>>>>>In item_code ic update flag:"+getCurrentUpdateFlag( dom.getElementsByTagName("Detail2").item( 0 ) ));
					if( !"D".equalsIgnoreCase( getCurrentUpdateFlag( dom.getElementsByTagName("Detail2").item( 0 ) ) ) )
					{
						//System.out.println( "item_code itemchange !!!" ); 
						retReplFlag = genericUtility.getColumnValue("ret_rep_flag",dom);
						retOpt = genericUtility.getColumnValue("ret_opt",dom1);
						if ("P".equals(retReplFlag) && "R".equals(retOpt))
						{
							valueXmlString.append("<invoice_id>").append("<![CDATA["+""+"]]>").append("</invoice_id>");
						}
						itemCode = genericUtility.getColumnValue("item_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom1);
						tranDate = genericUtility.getColumnValue("tran_date",dom1);
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						priceList = genericUtility.getColumnValue("price_list",dom1);
						itemSer = distCommon.getItemSer(itemCode,siteCode,Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"),custCode,"C",conn);
						valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
						sql = " select descr, unit, unit__rate, item_stru, pack_instr from item where item_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							descr = rs.getString(1); 
							unit = rs.getString(2);
							unitRate = rs.getString(3);
							itemStru = rs.getString(4);
							packInstr = rs.getString(5);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
						setNodeValue( dom, "item_descr", descr );
						valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
						setNodeValue( dom, "unit", unit );
						valueXmlString.append("<unit__std>").append("<![CDATA[" + unit + "]]>").append("</unit__std>");
						setNodeValue( dom, "unit__std", unit );
						valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
						setNodeValue( dom, "unit__rate", unitRate );
						invoiceId = genericUtility.getColumnValue("invoice_id",dom);
						invoiceId = invoiceId == null ?"" : invoiceId.trim();
						lineNoInvStr = genericUtility.getColumnValue("line_no__inv",dom);

						System.out.println( "lineNoInvStr before:: " + lineNoInvStr ); 
						pos = 0;
						if( lineNoInvStr != null )
						{
							pos = lineNoInvStr.indexOf(".");
						}
						if (pos > 0)
						{
							lineNoInvStr = lineNoInvStr.substring(0,pos);
						}
						System.out.println( "lineNoInvStr after:: " + lineNoInvStr ); 
						lineNoInvStr = lineNoInvStr == null || lineNoInvStr.trim().length() == 0 ? "0" : lineNoInvStr.trim();
						lineNoInv = Integer.parseInt( lineNoInvStr  );

						valStr =  genericUtility.getColumnValue( "quantity__stduom", dom );
						valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();
						qtyStdUom = Double.parseDouble( valStr );

						locCode = genericUtility.getColumnValue("loc_code",dom);
						lotNo = genericUtility.getColumnValue("lot_no",dom);
						lotSl =  genericUtility.getColumnValue("lot_sl",dom);
						lineNoTrace =  genericUtility.getColumnValue("line_no__invtrace",dom);

						infoMap = new HashMap();

						infoMap.put("ret_repl_flag",retReplFlag);
						infoMap.put("item_code", itemCode);
						infoMap.put("site_code", siteCode);
						infoMap.put("loc_code",locCode);
						infoMap.put("lot_no", lotNo);
						infoMap.put("lot_sl", lotSl);
						infoMap.put("tran_date", tranDate);
						infoMap.put("invoice_id", invoiceId);
						infoMap.put( "line_no__invtrace", genericUtility.getColumnValue( "line_no__invtrace", dom ) );
						infoMap.put( "quantity__stduom", new Double( -1 * qtyStdUom ) );
						costRate = getCostRate( infoMap, conn );
						infoMap = null;
						valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
						setNodeValue( dom, "cost_rate", costRate );
						if ("C".equals(retOpt))
						{
							if ( invoiceId == null || invoiceId.trim().length() == 0 )
							{
								taxChapHdr = genericUtility.getColumnValue("tax_chap",dom1);
								taxClassHdr = genericUtility.getColumnValue("tax_class",dom1);
								taxEnvHdr = genericUtility.getColumnValue("tax_env",dom1);
								if (taxChapHdr == null || taxChapHdr.trim().length() == 0)
								{ 
									System.out.println( "itemCode :: " + itemCode );
									System.out.println( "itemSer :: " + itemSer );
									System.out.println( "custCode :: " + custCode );
									System.out.println( "siteCode :: " + siteCode );
									String supportOrCustCd = "C";
									taxChap = distCommon.getTaxChap(itemCode, itemSer, supportOrCustCd, custCode, siteCode , conn );
								}
								else
								{
									taxChap = taxChapHdr;
								}
								taxChap = getAbsString( taxChap );
								if (taxClassHdr == null || taxClassHdr.trim().length() == 0)
								{ 
									taxClass = distCommon.getTaxClass( "C", custCode, itemCode, siteCode , conn);
								}
								else
								{
									taxClass = taxClassHdr;
								}
								taxClass = getAbsString( taxClass );
								if (taxEnvHdr == null || taxEnvHdr.trim().length() == 0)
								{ 
									sql = " select stan_code from site where site_code = ?";
									pstmt= conn.prepareStatement( sql );
									pstmt.setString( 1, siteCode );
									rs = pstmt.executeQuery(); 
									if( rs.next() )
									{
										frStation = rs.getString(1); 
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									sql = " select stan_code from customer where cust_code = ?";
									pstmt= conn.prepareStatement( sql );
									pstmt.setString( 1, custCode );
									rs = pstmt.executeQuery(); 
									if( rs.next() )
									{
										toStation = rs.getString(1); 
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									taxEnv = distCommon.getTaxEnv(frStation ,toStation, taxChap, taxClass,siteCode, conn);
								}
								else
								{
									taxEnv = taxEnvHdr;
								}
								taxEnv = getAbsString( taxEnv );
								taxClass = getAbsString( taxClass );
								taxChap = getAbsString( taxChap );

								valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
								setNodeValue( dom, "tax_chap", taxChap );
								System.out.println( "taxClass :3: " + taxClass );
								valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
								setNodeValue( dom, "tax_class", taxClass );
								valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
								setNodeValue( dom, "tax_env", taxEnv );
							}								
						}
						tranType = genericUtility.getColumnValue("tran_type", dom1);
						sql = " select udf_str1 from gencodes where fld_name='TRAN_TYPE' and mod_name='W_SALESRETURN' and fld_value = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, tranType );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							varValue = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						sRate = genericUtility.getColumnValue( "rate", dom );

						if (varValue != null && "Y".equalsIgnoreCase(varValue.trim()) )
						{
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
							setNodeValue( dom, "rate", sRate );
						}
						else
						{
							varValue = distCommon.getDisparams("999999","SRET_RATE_EDITABLE",conn);
							varValue = varValue == null ?"" : varValue.trim();
							fullRet = fullRet == null ?"" : fullRet.trim();
							if ( varValue != null && "Y".equalsIgnoreCase(varValue.trim()) )
							{
								if ( fullRet != null && "Y".equalsIgnoreCase(fullRet.trim()))
								{
									valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+ (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
									setNodeValue( dom, "rate", sRate );
								}
								else
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
									setNodeValue( dom, "rate", sRate );
								}
							}
							else
							{
								if (invoiceId != null && invoiceId.trim().length() > 0 && "Y".equals(fullRet) && "C".equals(retOpt))
								{
									valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+ (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
									setNodeValue( dom, "rate", sRate );
								}
								else
								{
									if (priceList == null || priceList.trim().length() == 0)
									{
										valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
										setNodeValue( dom, "rate", sRate );
									}
									else
									{
										tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;
										rate = distCommon.pickRate(priceList,tranDate,itemCode," ","L",qtyStdUom, conn);
										varValue = distCommon.getPriceListType(priceList,conn);
										varValue = varValue == null ?"" : varValue.trim();
										if ("B".equals(varValue) && rate < 0)
										{
											rate = 0;
										}
										//if ("B".equals(varValue) || "F".equals(varValue) || "Y".equals(fullRet)) condition change by nandkumar gadkari on 25/02/19
										if (!"L".equals(varValue) || "Y".equals(fullRet))
										{
											valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
											setNodeValue( dom, "rate", rate );
										}
										else
										{
											valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
											setNodeValue( dom, "rate", rate );
										}
									}

								}
							}
						}
						if (priceList != null && priceList.trim().length() > 0 && !"null".equalsIgnoreCase( priceList.trim() ) )
						{
							varValue = distCommon.getPriceListType(priceList,conn);
							varValue = varValue == null ?"" : varValue.trim();
						}								
						pircleListClg = genericUtility.getColumnValue("price_list__clg", dom1);
						pircleListClg = pircleListClg == null ?"" : pircleListClg.trim();
						varValue = distCommon.getPriceListType(priceListClg,conn);
						varValue = varValue == null ?"" : varValue.trim();
						if (priceListClg == null || priceListClg.trim().length() == 0)
						{
							valueXmlString.append("<rate__clg protect =\"0\">").append("<![CDATA[0]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", "0" );
						}
						else
						{
							if (!"B".equals( varValue ) && !"F".equals(varValue))
							{
								valStr =  genericUtility.getColumnValue("rate__clg", dom);
								valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

								sRateClg = Double.parseDouble( valStr );
								if( sRateClg == 0 )
								{
									tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;								
									rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode," ","L",qtyStdUom, conn);
									if ("B".equals(varValue) && rateClg < 0)
									{
										rateClg = 0;
									}
								}
							} 
							valueXmlString.append("<rate__clg protect =\"0\">").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", rateClg );
						}
						varValue = 	genericUtility.getColumnValue("conv__rtuom_stduom", dom);
						varValue = varValue == null ?"0" : varValue.trim();
						if (varValue == null || Double.parseDouble(varValue) <= 0)
						{
							valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");// column protected by nandkumar gadkari on 29/01/19
							setNodeValue( dom, "conv__rtuom_stduom", "1" );
							System.out.println("manohar rate__stduom 1 [" + rate + "]");
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate + "]]>").append("</rate__stduom>");
							setNodeValue( dom, "rate__stduom", rate );
						}
						invoiceId = 	genericUtility.getColumnValue("invoice_id", dom);
						retReplFlag = 	genericUtility.getColumnValue("ret_rep_flag", dom);
						if ( !"P".equals( retReplFlag ) )
						{
							if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) )
							{
								lineNoTrace = genericUtility.getColumnValue( "line_no__invtrace", dom );
								lineNoTrace = lineNoTrace == null ?"0" : lineNoTrace.trim();

								if( lineNoTrace != null )
								{
									pos = lineNoTrace.indexOf(".");
								}
								if (pos > 0)
								{
									lineNoTrace = lineNoTrace.substring(0,pos);
								}

								iLineNoTrace = Integer.parseInt( lineNoTrace ) ;

								sql = " select item_code from invoice_trace where invoice_id  = ? and line_no = ?"; 
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId );
								pstmt.setInt( 2, iLineNoTrace );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									varValue = rs.getString(1); 
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								varValue = varValue == null ?"" : varValue.trim();
								if (!varValue.trim().equals(itemCode.trim()))
								{
									valueXmlString.append("<discount>").append("<![CDATA[0]]>").append("</discount>");
									setNodeValue( dom, "discount", "0" );

									valueXmlString.append("<status>").append("<![CDATA[D]]>").append("</status>");
									setNodeValue( dom, "status", "D" );
									valueXmlString.append("<stk_opt>").append("<![CDATA[N]]>").append("</stk_opt>");
									setNodeValue( dom, "stk_opt", "N" );
									//tempNode = dom.getElementsByTagName("status").item(0);
									//tempNode.getFirstChild().setNodeValue("D");
									//tempNode = dom.getElementsByTagName("stk_opt").item(0);
									//tempNode.getFirstChild().setNodeValue("N");
									//tempNode = null;

									reStr = itemChanged(dom, dom1, dom2, objContext, "stk_opt", editFlag, xtraParams);
									pos = reStr.indexOf("<Detail2>");
									reStr = reStr.substring(pos + 9);
									pos = reStr.indexOf("</Detail2>");
									reStr = reStr.substring(0,pos);
									valueXmlString.append(reStr);
									varValue = distCommon.getDisparams("999999","DAMAGED_LOC",conn);
									valueXmlString.append("<loc_code>").append("<![CDATA[" + varValue + "]]>").append("</loc_code>");
									setNodeValue( dom, "loc_code", varValue );

								}								
							}
						}
						else if ("P".equals(retReplFlag)) 
						{
							effAmt = Double.parseDouble(genericUtility.getColumnValue("eff_net_amt", dom1));
							tranId = genericUtility.getColumnValue("tran_id", dom);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom1);
							priceList = genericUtility.getColumnValue("price_list", dom1);

							effAmt = getTotEffAmt( tranId, conn ) - getTotAmtForRep( dom2 );
							quantity = itemValue2Quantity(siteCode, itemCode, priceList, effAmt, conn);

							if (quantity > 0)
							{
								valueXmlString.append("<quantity>").append("<![CDATA[" + getRequiredDecimal(quantity, 3) + "]]>").append("</quantity>");

								setNodeValue( dom, "quantity", quantity );
								//	tempNode = dom.getElementsByTagName("quantity").item(0);
								//	tempNode.getFirstChild().setNodeValue("" + quantity);
								//	tempNode = null;

								reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
							}
						}
						System.out.println(">>>>>>>>>>>In item_code before minRateBuff:");
						StringBuffer minRateBuff = getMinRate(dom, dom1, "item_code", valueXmlString, conn);
						System.out.println( "minRateBuff1 :: " + minRateBuff.toString() );
						valueXmlString = minRateBuff;

						valueXmlString = GetSetQcReqd(dom, dom1, valueXmlString, conn);

						sql = " select item_code__ref, descr from customeritem " 
								+ " where cust_code = ? and item_code = ? ";	
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCode );
						pstmt.setString( 2, itemCode );
						rs = pstmt.executeQuery(); 
						itemRef = "";
						descr = "";
						if( rs.next() )
						{
							itemRef = rs.getString(1); 
							descr = rs.getString(2);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						// 20/04/10 manoharan
						itemRef = itemRef == null ?"" : itemRef.trim();
						descr = descr == null ?"" : descr.trim();
						// end 20/04/10 manoharan
						valueXmlString.append("<cust_item__ref>").append("<![CDATA[" + itemRef + "]]>").append("</cust_item__ref>");
						setNodeValue( dom, "cust_item__ref", itemRef );
						valueXmlString.append("<cust_item_ref_descr>").append("<![CDATA[" + descr + "]]>").append("</cust_item_ref_descr>");
						setNodeValue( dom, "cust_item_ref_descr", descr );
					}
				} //  end item_code
				else if (currentColumn.trim().equals("ret_rep_flag") )			
				{
					retReplFlag = genericUtility.getColumnValue("ret_rep_flag",dom);
					if ("P".equals(retReplFlag))
					{
						effAmt = Double.parseDouble(genericUtility.getColumnValue("eff_net_amt", dom1));
						tranId = genericUtility.getColumnValue("tran_id", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						priceList = genericUtility.getColumnValue("price_list", dom1);
						effAmt = getTotEffAmt( tranId, conn ) - getTotAmtForRep( dom2 );
						quantity = itemValue2Quantity(siteCode, itemCode, priceList, effAmt, conn);
						if (quantity > 0)
						{
							valueXmlString.append("<quantity>").append("<![CDATA[" + getRequiredDecimal( quantity, 3 ) + "]]>").append("</quantity>");
							setNodeValue( dom, "quantity", getRequiredDecimal( quantity, 3 ) );

							//tempNode = dom.getElementsByTagName("quantity").item(0);
							//tempNode.getFirstChild().setNodeValue( Double.toString( quantity ) );

							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
						valueXmlString.append("<status>").append("<![CDATA[S]]>").append("</status>");
						setNodeValue( dom, "status", "S" );
						valueXmlString.append("<stk_opt>").append("<![CDATA[U]]>").append("</stk_opt>");
						setNodeValue( dom, "stk_opt", "U" );
						varValue = distCommon.getDisparams("999999","ALLOC_FGLOC",conn);
						valueXmlString.append("<loc_code>").append("<![CDATA[" + varValue + "]]>").append("</loc_code>");
						setNodeValue( dom, "loc_code", varValue );
						valueXmlString.append("<invoice_id>").append("<![CDATA["+""+"]]>").append("</invoice_id>");
						setNodeValue( dom, "invoice_id", "" );
						valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA[" + "" + "]]>").append("</line_no__inv>");
						setNodeValue( dom, "line_no__inv", "" );
						valueXmlString.append("<item_code protect =\"0\">").append("<![CDATA[" + getAbsString(itemCode) + "]]>").append("</item_code>");
						setNodeValue( dom, "item_code", itemCode );
					}
					else
					{
						invoiceId = genericUtility.getColumnValue("invoice_id", dom);
						varValue =  getNumString( genericUtility.getColumnValue("line_no__inv", dom) );
						System.out.println( "invoiceId :1: " + invoiceId );
						if( invoiceId == null || invoiceId.trim().length() == 0 )
						{
							valueXmlString.append("<item_code protect =\"0\">").append("<![CDATA[" + getAbsString(itemCode) + "]]>").append("</item_code>");
							setNodeValue( dom, "item_code", "" );
							//	valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA[" + varValue + "]]>").append("</line_no__inv>");// commented and set blank  by nandkumar gadkari on 13/12/19
							//setNodeValue( dom, "line_no__inv", varValue );
							valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");
							setNodeValue( dom, "line_no__inv", "" );
						}
						else
						{

							valueXmlString.append("<item_code protect =\"1\">").append("<![CDATA[" + getAbsString(genericUtility.getColumnValue("item_code", dom)) +"]]>").append("</item_code>");
							setNodeValue( dom, "item_code", "" );
							//valueXmlString.append("<line_no__inv protect =\"0\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");
							//setNodeValue( dom, "line_no__inv", "" );
							valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA[" + varValue + "]]>").append("</line_no__inv>");// commented and set value   by nandkumar gadkari on 13/12/19
							setNodeValue( dom, "line_no__inv", varValue );
						}
						valueXmlString = GetSetQcReqd(dom, dom1, valueXmlString, conn);
					}

				} // end of ret_rep_flag
				else if (currentColumn.trim().equals("quantity") )			
				{
					String qtyStr =genericUtility.getColumnValue("quantity", dom);
					sQuantity = Double.parseDouble( qtyStr == null || qtyStr.trim().length() == 0 ? "0" : qtyStr.trim() );
					itemCode = genericUtility.getColumnValue("item_code", dom);
					unit = genericUtility.getColumnValue("unit", dom);
					unitStd = genericUtility.getColumnValue("unit__std", dom);

					claimQty = genericUtility.getColumnValue("claim_qty", dom);
					physicalQty = genericUtility.getColumnValue("physical_qty", dom);

					if (unit == null || unit.trim().length() == 0)
					{
						sql = " select unit from item where item_code = ? ";	
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							unit = rs.getString(1);
							valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>"); 
							setNodeValue( dom, "unit", unit );
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
					}
					quantity = sQuantity;
					varValue = genericUtility.getColumnValue("conv__qty_stduom", dom);
					if (varValue == null || varValue.trim().length() == 0)
					{
						varValue = "0";
					}
					convFact = Double.parseDouble(varValue);
					if ( unit != null && unitStd != null && !unit.trim().equals( unitStd.trim() ) )// condition added by nandkumar gadkari on 26/06/19
					{
						convAr = distCommon.getConvQuantityFact(unit, unitStd, itemCode, quantity, convFact, conn);
						convFact = Double.parseDouble( convAr.get(0).toString() );
						quantity = Double.parseDouble( convAr.get(1).toString() );
						//tempNode = dom.getElementsByTagName("quantity__stduom").item(0);
						//tempNode.getFirstChild().setNodeValue("" + quantity);
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + getRequiredDecimal( convFact, 3 ) + "]]>").append("</conv__qty_stduom>");// added by nandkumar gadkari on 29/01/19
					}
					else
					{
						convFact = 1;
						quantity = sQuantity;	
						valueXmlString.append("<conv__qty_stduom protect =\"1\">").append("<![CDATA[" + getRequiredDecimal( convFact, 3 ) + "]]>").append("</conv__qty_stduom>");// added by nandkumar gadkari on 29/01/19
					}
					//valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[" + getRequiredDecimal( convFact, 3 ) + "]]>").append("</conv__qty_stduom>");// commented by nandkumar gadkari on 29/01/19
					setNodeValue( dom, "conv__qty_stduom", getRequiredDecimal( convFact, 3 ) );
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + getRequiredDecimal( quantity, 3 ) + "]]>").append("</quantity__stduom>");
					setNodeValue( dom, "quantity__stduom", getRequiredDecimal( quantity, 3 ) );

					if (claimQty == null)
					{
						valueXmlString.append("<claim_qty>").append("<![CDATA[" + getRequiredDecimal( sQuantity, 3 ) + "]]>").append("</claim_qty>");
						setNodeValue( dom, "claim_qty", getRequiredDecimal( sQuantity, 3 ) );
					}
					// Changed by Sneha on 28-06-2016, to set claim_qty and physical_qty [Start]
					else if(Double.parseDouble(claimQty) != sQuantity)
					{
						valueXmlString.append("<claim_qty>").append("<![CDATA[" + getRequiredDecimal( sQuantity, 3 ) + "]]>").append("</claim_qty>");
						setNodeValue( dom, "claim_qty", getRequiredDecimal( sQuantity, 3 ) );
					}
					// Changed by Sneha on 28-06-2016, to set claim_qty and physical_qty [End]
					if (claimQty == null)
					{
						valueXmlString.append("<physical_qty>").append("<![CDATA[" + getRequiredDecimal( sQuantity, 3 ) + "]]>").append("</physical_qty>");
						setNodeValue( dom, "physical_qty", getRequiredDecimal( sQuantity, 3 ) );
					}
					// Changed by Sneha on 28-06-2016, to set claim_qty and physical_qty [Start]
					else if(Double.parseDouble(physicalQty) != sQuantity)
					{
						valueXmlString.append("<physical_qty>").append("<![CDATA[" + getRequiredDecimal( sQuantity, 3 ) + "]]>").append("</physical_qty>");
						setNodeValue( dom, "physical_qty", getRequiredDecimal( sQuantity, 3 ) );
					}
					// Changed by Sneha on 28-06-2016, to set claim_qty and physical_qty [End]

					infoMap = new HashMap();
					retReplFlag = genericUtility.getColumnValue("ret_repl_flag", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom1);
					// 20/08/14 manoharan cust_code is used below for getting no_art
					custCode = genericUtility.getColumnValue("cust_code", dom1);
					locCode = genericUtility.getColumnValue("loc_code", dom);
					lotNo =  genericUtility.getColumnValue("lot_no", dom);
					lotSl =  genericUtility.getColumnValue("lot_sl", dom);
					tranDate = genericUtility.getColumnValue("tran_date", dom1);
					invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
					lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);

					valStr =  genericUtility.getColumnValue("quantity__stduom", dom);
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					qtyStdUom = Double.parseDouble( valStr );
					quantity = qtyStdUom;
					packCode = genericUtility.getColumnValue("pack_code", dom);

					infoMap.put("ret_repl_flag",retReplFlag);
					infoMap.put("item_code", itemCode);
					infoMap.put("site_code", siteCode);
					infoMap.put("loc_code",locCode);
					infoMap.put("lot_no", lotNo);
					infoMap.put("lot_sl", lotSl);
					infoMap.put("tran_date", tranDate);
					infoMap.put("invoice_id", invoiceId);
					infoMap.put("line_no__invtrace",lineNoTrace);
					infoMap.put( "quantity__stduom", getRequiredDecimal( new Double( -1 * quantity ), 3 ) );
					costRate = getCostRate(infoMap, conn);
					infoMap = null;
					valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
					setNodeValue( dom, "cost_rate", costRate );
					noArt = distCommon.getNoArt( siteCode, custCode, itemCode, packCode, quantity , 'B', shipperQty, integralQty, conn );
					valueXmlString.append("<no_art>").append("<![CDATA[" + noArt + "]]>").append("</no_art>");
					setNodeValue( dom, "no_art", noArt );	
					varValue = genericUtility.getColumnValue("rate__clg", dom);
					if (varValue == null || varValue.equals("null"))
					{
						varValue = "0";
					}

					quantity = sQuantity;
					valueXmlString.append("<mrp_value>").append("<![CDATA[" + getRequiredDecimal( quantity * Double.parseDouble(varValue), 4 ) + "]]>").append("</mrp_value>");
					setNodeValue( dom, "mrp_value", getRequiredDecimal( quantity * Double.parseDouble(varValue), 4 ) );	

					//added  by nandkumar gadkari-------------
					if (lotNo!=null && lotNo.trim().length()>0)
					{
						reStr = itemChanged(dom, dom1, dom2, objContext, "lot_no", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					//added  by nandkumar gadkari-------------
				} // end of quantity 
				else if (currentColumn.trim().equals("rate") )		
				{
					sRate = genericUtility.getColumnValue("rate", dom);
					if (sRate == null || sRate.equals("null"))
					{
						sRate = "0";
					}
					rate = Double.parseDouble(sRate);

					convRateStdUom = Double.parseDouble(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
					System.out.println("manohar rate__stduom 2 [" + rate * convRateStdUom + "]");
					valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate * convRateStdUom  + "]]>").append("</rate__stduom>");
					setNodeValue( dom, "rate__stduom", rate * convRateStdUom );
					tranDate = genericUtility.getColumnValue("tran_date", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);
					sRate = genericUtility.getColumnValue("rate__stduom", dom);
					if (sRate == null || sRate.equals("null"))
					{
						sRate = "0";
					}
					rateStdUom = Double.parseDouble(sRate);
					sRate = genericUtility.getColumnValue("rate__clg", dom);
					if (sRate == null || sRate.equals("null"))
					{
						sRate = "0";
					}
					rateClg = Double.parseDouble(sRate);
					if (priceListClg == null || priceListClg.trim().length() == 0)
					{
						valueXmlString.append("<rate__clg protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate__clg>");
						setNodeValue( dom, "rate__clg", sRate );
					}
					else
					{
						listType = distCommon.getPriceListType(priceListClg, conn);
						if (!"B".equals(listType) && !"F".equals(listType))
						{

							if (rateClg <= 0 && priceListClg.trim().length() > 0)
							{
								tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;								
								rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode," ","L",qtyStdUom, conn);
							}
						}
					}
					if (rateClg <= 0 && rate > 0)
					{
						rateClg = rate;
					}
					valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");

					valStr = genericUtility.getColumnValue("quantity", dom);
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					sQuantity = Double.parseDouble( valStr );
					quantity = sQuantity;
					valueXmlString.append("<mrp_value>").append("<![CDATA[" + rateClg * quantity + "]]>").append("</mrp_value>");

				} //  end of rate
				else if (currentColumn.trim().equals("rate__clg") )		
				{
					valStr = genericUtility.getColumnValue("rate__clg", dom);
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();
					rateClg = Double.parseDouble( valStr );

					valStr = genericUtility.getColumnValue("quantity", dom);
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					quantity = Double.parseDouble( valStr );
					effAmt = quantity * rateClg;
					effAmt = getRequiredDecimal( effAmt, 4 );
					valueXmlString.append("<mrp_value>").append("<![CDATA[" + effAmt + "]]>").append("</mrp_value>");
				} //  end of rate__clg
				else if (currentColumn.trim().equals("line_no__invtrace") )		
				{
					String taxClassVal = null;
					String taxChapVal = null;
					String taxEnvVal = null;

					lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
					invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
					retOpt = genericUtility.getColumnValue("ret_opt", dom1);
					if (invoiceId != null && invoiceId.trim().length() > 0 && lineNoTrace != null && lineNoTrace.trim().length() > 0 )
					{
						lineNoTrace = lineNoTrace == null || lineNoTrace.trim().length() == 0 ? "0" : lineNoTrace.trim();
						if( lineNoTrace != null )
						{
							pos = lineNoTrace.indexOf(".");
						}
						if (pos > 0)
						{
							lineNoTrace = lineNoTrace.substring(0,pos);
						}

						iLineNoTrace =  Integer.parseInt(lineNoTrace);

						sql = " select inv_line_no,site_code__mfg, mfg_date, exp_date, lot_no, "
								+ " lot_sl, desp_id, desp_line_no, rate__std, cost_rate, item_code, quantity, "
								+ " tax_class, tax_chap, tax_env, rate, rate__clg, sord_no, sord_line_no,	"
								+ " item_code__ord, line_type,unit__rate "
								+ " from invoice_trace where invoice_id	= ? and line_no = ? ";	
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, invoiceId );
						pstmt.setInt( 2, iLineNoTrace );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{


							//lineNoInv = Integer.parseInt( getNumString( rs.getString("inv_line_no") ) );

							lineNoInvStr  = getNumString( rs.getString("inv_line_no") ) ;
							lineNoInvStr = lineNoInvStr == null || lineNoInvStr.trim().length() == 0 ? "0" : lineNoInvStr.trim();
							if( lineNoInvStr != null )
							{
								pos = lineNoInvStr.indexOf(".");
							}
							if (pos > 0)
							{
								lineNoInvStr = lineNoInvStr.substring(0,pos);
							}
							lineNoInv = Integer.parseInt(lineNoInvStr);

							valueXmlString.append("<line_no__inv>").append("<![CDATA[" + lineNoInv + "]]>").append("</line_no__inv>"); 
							System.out.println("lineNoInv before >>> " + lineNoInv);
							setNodeValue( dom, "line_no__inv", lineNoInv );
							//lineNoInv = genericUtility.getColumnValue("line_no__inv", dom);
							System.out.println("lineNoInv after >>> " + genericUtility.getColumnValue("line_no__inv", dom));
							if (!"w_salesreturn_retn".equalsIgnoreCase(winName) && !"w_salesreturn_repl".equalsIgnoreCase(winName))
							{
								if ("R".equals(retOpt) || "C".equals(retOpt))
								{
									retReplFlag = "R";
								}
								else
								{
									retReplFlag = "P";
								}
								valueXmlString.append("<ret_rep_flag>").append("<![CDATA[" + retReplFlag + "]]>").append("</ret_rep_flag>");

								setNodeValue( dom, "ret_rep_flag", retReplFlag );
								//tempNode = dom.getElementsByTagName("ret_rep_flag").item(0);
								//tempNode.getFirstChild().setNodeValue(retReplFlag);
							}
							siteCodeMfg = rs.getString("site_code__mfg");
							siteCodeMfg = getAbsString( siteCodeMfg ); 
							mfgDate = rs.getTimestamp("mfg_date");
							expDate = rs.getTimestamp("exp_date");
							lotNo = rs.getString("lot_no");
							lotSl = rs.getString("lot_sl");
							despId = rs.getString("desp_id");
							despLineNo = rs.getString("desp_line_no");
							rateStd = rs.getDouble("rate__std");
							costRate = rs.getDouble("cost_rate");
							itemCode = rs.getString("item_code");
							sQuantity = rs.getDouble("quantity");
							taxClasss = rs.getString("tax_class");
							taxChap = rs.getString("tax_chap");
							taxEnv = rs.getString("tax_env");
							sRate = rs.getString("rate");
							sRateClg = rs.getDouble("rate__clg");
							sorder = rs.getString("sord_no");
							sordLineNo = rs.getString("sord_line_no");
							itemCodeOrd = rs.getString("item_code__ord");
							lineType = rs.getString("line_type");
							unitRate = rs.getString("unit__rate");//Changed by Priyanka Das for unit to be set from invoice_trace
							System.out.println( "taxClasss :1: " + taxClasss ); 

							taxClasss = getAbsString(taxClasss);
							taxClassVal = taxClasss;
							taxChapVal = getAbsString(taxChap);
							taxEnvVal = getAbsString(taxEnv);

							System.out.println( "taxClassVal :1: " + taxClassVal ); 
							System.out.println( "taxChapVal :1: " + taxChapVal ); 
							System.out.println( "taxEnvVal :1: " + taxEnvVal ); 

							rate = Double.parseDouble( ( sRate == null || sRate.trim().length() == 0 ? "0" : sRate.trim() ) );
							valueXmlString.append("<item_code>").append("<![CDATA[" + getAbsString(itemCode) + "]]>").append("</item_code>");
							setNodeValue( dom, "item_code", getAbsString(itemCode) );
							//tempNode = dom.getElementsByTagName("item_code").item(0);
							//tempNode.getFirstChild().setNodeValue(itemCode);

							reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);

							valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + getAbsString(siteCodeMfg) + "]]>").append("</site_code__mfg>");
							setNodeValue( dom, "site_code__mfg", siteCodeMfg );
							if (mfgDate != null)
							{
								valueXmlString.append("<mfg_date>").append("<![CDATA[" + genericUtility.getValidDateString( mfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) + "]]>").append("</mfg_date>");
								setNodeValue( dom, "mfg_date", genericUtility.getValidDateString( mfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) );
							}

							if (expDate != null)
							{
								//Commented and added by sarita if exp_date is found then it should be non-editable on 04 SEPT 2018 [START]
								//valueXmlString.append("<exp_date>").append("<![CDATA[" + genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() )  + "]]>").append("</exp_date>");
								valueXmlString.append("<exp_date protect =\"1\">").append("<![CDATA[" + genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() )  + "]]>").append("</exp_date>");
								//Commented and added by sarita if exp_date is found then it should be non-editable on 04 SEPT 2018 [END]
								setNodeValue( dom, "exp_date", genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) );
							}

							valueXmlString.append("<lot_no>").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
							setNodeValue( dom, "lot_no", lotNo );

							valueXmlString.append("<rate__std>").append("<![CDATA[" + rateStd + "]]>").append("</rate__std>");
							setNodeValue( dom, "rate__std", Double.toString( rateStd ) );

							valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
							setNodeValue( dom, "cost_rate", Double.toString( costRate ) );
							lineType = getAbsString( lineType );
							valueXmlString.append("<line_type>").append("<![CDATA[" + lineType + "]]>").append("</line_type>");
							setNodeValue( dom, "line_type", lineType );

							valueXmlString.append("<rate__clg>").append("<![CDATA[" + sRateClg + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", Double.toString( sRateClg ) );

							valueXmlString.append("<lot_sl>").append("<![CDATA[" + lotSl + "]]>").append("</lot_sl>");
							setNodeValue( dom, "lot_sl", lotSl );
							//changes  by Nandkumar gadkari Start-----on 31/12/18----------------
							//sql1 = "select QUANTITY_ADJ from MIN_RATE_HISTORY where DOC_KEY like '%"+invoiceId+"%' AND ITEM_CODE=? AND LOT_NO= ?";
							String docKey="";
							sql1 = "select DOC_KEY,QUANTITY_ADJ from MIN_RATE_HISTORY where invoice_id = ? AND ITEM_CODE=? AND LOT_NO= ?";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString( 1, invoiceId );
							pstmt1.setString( 2, itemCode );
							pstmt1.setString( 3, lotNo );

							rs1 = pstmt1.executeQuery();

							/*if(rs1.next())
								{
									 qutyAdj= rs1.getDouble(1); 
								}*/
							while(rs1.next())
							{
								docKey= checkNullTrim(rs1.getString(1)); 

								if( docKey.indexOf(invoiceId) != -1)
								{
									qutyAdj= rs1.getDouble(2); 
									break;
								}

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

							sQuantity=Math.abs(sQuantity-qutyAdj) ;
							//chnages  by Nandkumar gadkari end---------------------


							valueXmlString.append("<quantity>").append("<![CDATA[" + getRequiredDecimal( sQuantity, 3 ) + "]]>").append("</quantity>");
							setNodeValue( dom, "quantity", getRequiredDecimal( sQuantity, 3 ) );

							retReplFlag = genericUtility.getColumnValue("ret_repl_flag", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom1);
							locCode = genericUtility.getColumnValue("loc_code", dom);
							tranDate = genericUtility.getColumnValue("tran_date", dom1);
							invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
							lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);

							valStr = genericUtility.getColumnValue("quantity__stduom", dom);
							valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

							qtyStdUom = Double.parseDouble( valStr );
							quantity = qtyStdUom;
							packCode = genericUtility.getColumnValue("pack_code", dom);

							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							itemSer = distCommon.getItemSer(itemCode,siteCode,
									Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"),
									custCode,"C",conn);
							valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
							setNodeValue( dom, "item_ser", itemSer );
							sql = " select descr, unit from item where item_code = ? ";	
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, itemCode );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								descr = rs.getString(1);
								unit = rs.getString(2);
								//	unitRate = rs.getString(3);
								unitStd = unit;
								valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
								setNodeValue( dom, "item_descr", descr );
								valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>"); 
								setNodeValue( dom, "unit", unit );
								valueXmlString.append("<unit__rate>").append("<![CDATA[" + unitRate + "]]>").append("</unit__rate>");
								setNodeValue( dom, "unit__rate", unitRate );
								valueXmlString.append("<unit__std>").append("<![CDATA[" + unit + "]]>").append("</unit__std>"); 
								setNodeValue( dom, "unit__std", unit );
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							valueXmlString.append("<conv__qty_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__qty_stduom>"); // column protected by nandkumar gadkari on 29/01/19
							setNodeValue( dom, "conv__qty_stduom", "1" );

							infoMap = new HashMap();
							infoMap.put("ret_repl_flag",retReplFlag);
							infoMap.put("item_code", itemCode);
							infoMap.put("site_code", siteCode);
							infoMap.put("loc_code",locCode);
							infoMap.put("lot_no", lotNo);
							infoMap.put("lot_sl", lotSl);
							infoMap.put("tran_date", tranDate);
							infoMap.put("invoice_id", invoiceId);
							infoMap.put("line_no__invtrace",lineNoTrace);
							infoMap.put("quantity__stduom",new Double(-1 * quantity));
							costRate = getCostRate(infoMap, conn);
							infoMap = null;
							valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");

							sql = " select sum((case when quantity is null then 0 else quantity end) * (case when rate is null then 0 else rate end)), "
									+ " sum(case when quantity is null then 0 else quantity end) "
									+ " from   invoice_trace "
									+ " where  invoice_id   = ? "
									+ " and	line_no  =? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId);

							if( lineNoTrace != null )
							{
								pos = lineNoTrace.indexOf(".");
							}
							if (pos > 0)
							{
								lineNoTrace = lineNoTrace.substring(0,pos);
							}
							lineNoTrace = lineNoTrace == null || lineNoTrace.trim().length() == 0 ? "0" : lineNoTrace.trim();
							pstmt.setInt( 2, Integer.parseInt(lineNoTrace));
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								amount = rs.getDouble(1);
								quantity = rs.getDouble(2);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							sql = " select sum(case when a.drcr_flag = 'D' then (case when b.drcr_amt is null then 0 else b.drcr_amt end) "
									+ " else ((case when b.drcr_amt is null then 0 else b.drcr_amt end) * -1)  End) "
									+ " from drcr_rcp a, drcr_rdet b "
									+ " where a.tran_id = b.tran_id "
									+ " and b.invoice_id   = ? "
									+ " and b.line_no__inv = ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId);

							if( lineNoTrace != null )
							{
								pos = lineNoTrace.indexOf(".");
							}
							if (pos > 0)
							{
								lineNoTrace = lineNoTrace.substring(0,pos);
							}
							lineNoTrace = lineNoTrace == null || lineNoTrace.trim().length() == 0 ? "0" : lineNoTrace.trim();
							pstmt.setInt( 2, Integer.parseInt( lineNoTrace ) );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								drcrAmount = rs.getDouble(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							//if ( drcrAmount != 0 )
							//{
							System.out.println( "amount :: " + amount );
							System.out.println( "drcrAmount :: " + drcrAmount );								
							System.out.println( "quantity :: " + quantity );								
							rate = (amount + drcrAmount) / quantity;
							//}

							priceList = genericUtility.getColumnValue("price_list", dom1);
							priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);

							pickLowerRate = distCommon.getDisparams("999999","PICK_LOWER_RATE", conn);
							System.out.println( "pickLowerRate :: " + pickLowerRate ); 
							if (pickLowerRate.equals("NULLFOUND"))
							{
								pickLowerRate = "N";
							}
							if (pickLowerRate.equals("Y"))
							{
								if (priceList != null && priceList.trim().length() > 0 && !"null".equalsIgnoreCase( priceList.trim() ) )
								{
									tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;
									itemCode = itemCode == null ? "" : itemCode;
									lotNo = lotNo == null ? "" : lotNo;
									priceListRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"L", quantity, conn);
									System.out.println( "IN priceListRate :: " + priceListRate ); 
									if (priceListRate < rate )
									{
										System.out.println( "IN rate :: " + rate ); 
										rate = priceListRate;
									}
								}
							}
							System.out.println( "out rate :: " + rate ); 								
							valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
							setNodeValue( dom, "rate", rate );
							//tempNode = dom.getElementsByTagName("rate").item(0);
							//tempNode.getFirstChild().setNodeValue("" +rate);

							rateClg = sRateClg ;
							priceListClg = priceListClg == null ?"" : priceListClg.trim();
							if (rateClg <= 0 )
							{
								if (priceListClg.trim().length() > 0)
								{
									tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString().toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;									
									itemCode = itemCode == null ? "" : itemCode;
									lotNo = lotNo == null ? "" : lotNo;

									rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode,lotNo,"L", quantity, conn);
									if (rateClg == -1)
									{
										rateClg = 0 ;
									}
								}
								if (rateClg == 0 )
								{
									valueXmlString.append("<rate__clg>").append("<![CDATA[" + getRequiredDecimal( rate, 4 ) + "]]>").append("</rate__clg>");
									setNodeValue( dom, "rate__clg", getRequiredDecimal( rate, 4 ) );
								}
								else
								{
									valueXmlString.append("<rate__clg>").append("<![CDATA[" + getRequiredDecimal( rateClg, 4 ) + "]]>").append("</rate__clg>");
									setNodeValue( dom, "rate__clg", getRequiredDecimal( rateClg, 4 ) );
								}
							}
							unit = unit == null ?"" : unit.trim();
							unitRate = unitRate == null ?"" : unitRate.trim();
							if ( unit.trim().equals(unitRate.trim()))
							{
								System.out.println("manohar rate__stduom 3 [" + getRequiredDecimal( rate, 4 ) + "]");
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + getRequiredDecimal( rate, 4 ) + "]]>").append("</rate__stduom>");
								setNodeValue( dom, "rate__stduom", getRequiredDecimal( rate, 4 ) );
								valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");// Column protected by nandkumar gadkari on 29/01/19
								setNodeValue( dom, "conv__rtuom_stduom", "1" );
							}
							else
							{
								convAr = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, (double) fact,  conn);
								convFact = Double.parseDouble( convAr.get(0).toString() );
								rateStd = Double.parseDouble( convAr.get(1).toString() );
								System.out.println("manohar rate__stduom 4 [" + rateStd + "]");
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStd + "]]>").append("</rate__stduom>");
								setNodeValue( dom, "rate__stduom", rateStd );

								valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + convFact + "]]>").append("</conv__rtuom_stduom>");
								setNodeValue( dom, "conv__rtuom_stduom", convFact );
							}

							sql = "select a.loc_code, b.descr from despatchdet a, location b "
									+ "where a.loc_code = b.loc_code "
									+ " and a.desp_id = ? and a.line_no = ?";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, despId);
							pstmt.setString( 2, despLineNo);
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								locCode = rs.getString(1);
								locDescr = rs.getString(2);
								valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
								setNodeValue( dom, "loc_code", locCode );
								//tempNode = dom.getElementsByTagName("loc_code").item(0);
								//tempNode.getFirstChild().setNodeValue(locCode);
								valueXmlString.append("<location_descr>").append("<![CDATA[" + locDescr + "]]>").append("</location_descr>");
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							reStr = itemChanged(dom, dom1, dom2, objContext, "lot_no", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);

							sretLocCode = getAbsString( distCommon.getDisparams("999999","SRET_LOC_CODE", conn) );
							if (sretLocCode.equals("D"))
							{
								locCode = distCommon.getDisparams("999999","ALLOC_FGLOC", conn);
								if (!locCode.equals("NULLFOUND"))
								{
									valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
									sql = "select descr from location where loc_code = ? ";
									pstmt= conn.prepareStatement( sql );
									pstmt.setString( 1, locCode);
									rs = pstmt.executeQuery(); 
									if( rs.next() )
									{
										locDescr = rs.getString(1);
										valueXmlString.append("<location_descr>").append("<![CDATA[" + locDescr + "]]>").append("</location_descr>");
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}
							}

							taxCal = distCommon.getDisparams("999999","SRET_TAX_CALC", conn);
							if (taxCal.equals("NULLFOUND"))
							{
								taxCal = "Y";
							}
							sql = "select sorddet.item_code "
									+ " from invoice, invoice_trace, sorddet, item "
									+ " where invoice_trace.invoice_id = invoice.invoice_id " 
									+ " and sorddet.sale_order = invoice.sale_order "
									+ " and sorddet.line_no = invoice_trace.sord_line_no " 
									+ " and invoice.invoice_id = ? "
									+ " and invoice_trace.line_no = ? "
									+ " and sorddet.item_flg = 'B' " 
									+ " and item.item_code = sorddet.item_code " 
									+ " and item.item_stru = 'F' ";

							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId);
							pstmt.setInt( 2, iLineNoTrace);
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								if (!taxCal.equals("N"))
								{
									if("C".equals(retOpt))
									{
										taxClasss = getAbsString(taxClassVal);

										taxChap = getAbsString(  taxChapVal );

										taxEnv = getAbsString( taxEnvVal );
										System.out.println( "taxClass :4: " + taxClass );
										valueXmlString.append("<tax_class>").append("<![CDATA[" +taxClass + "]]>").append("</tax_class>"); 
										valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>"); 
										valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>"); 
									}
								}
								itemCodeOrd = rs.getString(1);
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								sql = "select eff_cost from bomdet  "
										+ " where bom_code = ? "
										+ " and item_code  = ? ";
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, itemCodeOrd);
								pstmt.setString( 2, itemCode);
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									effCost = rs.getDouble(1);
									valueXmlString.append("<rate>").append("<![CDATA[" + effCost + "]]>").append("</rate>");
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}
							else
							{
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if("C".equals(retOpt))
								{
									taxClasss = getAbsString(taxClassVal);

									taxChap = getAbsString(  taxChapVal );

									taxEnv = getAbsString( taxEnvVal );


									System.out.println( "taxClass :5: " + taxClasss );
									valueXmlString.append("<tax_class>").append("<![CDATA[" +taxClasss + "]]>").append("</tax_class>"); 
									valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>"); 
									valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>"); 
								}
							}
						}
						else
						{
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							System.out.println( "taxClass :7: " + taxClass );
							valueXmlString.append("<line_no__inv>").append("<![CDATA[" + "" + "]]>").append("</line_no__inv>"); 
							valueXmlString.append("<tax_class>").append("<![CDATA[" + "" + "]]>").append("</tax_class>"); 
							valueXmlString.append("<tax_chap>").append("<![CDATA[" + "" + "]]>").append("</tax_chap>"); 
							valueXmlString.append("<tax_env>").append("<![CDATA[" + "" + "]]>").append("</tax_env>"); 
							valueXmlString.append("<rate>").append("<![CDATA[" + "" + "]]>").append("</rate>");

							valueXmlString.append("<loc_code>").append("<![CDATA[" + "" + "]]>").append("</loc_code>");
							valueXmlString.append("<location_descr>").append("<![CDATA[" + "" + "]]>").append("</location_descr>");
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + "" + "]]>").append("</rate__stduom>");
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + "" + "]]>").append("</conv__rtuom_stduom>");
							valueXmlString.append("<item_code>").append("<![CDATA[" + "" + "]]>").append("</item_code>");
							valueXmlString.append("<item_descr>").append("<![CDATA[" + "" + "]]>").append("</item_descr>");								
						}

						//unit = genericUtility.getColumnValue("unit", dom);
						//unit = unit == null || unit.trim().length() == 0 ? "" : unit;

						//unitStd = genericUtility.getColumnValue("unit_std", dom);
						//unitStd = unitStd == null || unitStd.trim().length() == 0 ? "" : unitStd;
						/*
							System.out.println( "unit :1: " + unit );
							System.out.println( "unitStd :1: " + unitStd );

							varValue = genericUtility.getColumnValue("conv__qty_stduom", dom);
							if (varValue == null || varValue.trim().length() == 0)
							{
								varValue = "0";
							}
							convFact = Double.parseDouble(varValue);
							System.out.println( "convFact :1: " +convFact );
							System.out.println( "varValue :1: " + varValue );

							if ( unit != null && !unit.trim().equals( unitStd.trim() ) )
							{
								convAr = distCommon.getConvQuantityFact(unit, unitStd, itemCode, quantity, convFact, conn);
								convFact = Double.parseDouble( convAr.get(0).toString() );
								quantity = Double.parseDouble( convAr.get(1).toString() );
								System.out.println( "convFact :2: " +convFact );
								System.out.println( "quantity :2: " + quantity );

							}
							else
							{
								convFact = 1;
								quantity = sQuantity;						
								System.out.println( "convFact :3: " +convFact );
								System.out.println( "quantity :3: " + quantity );

							}	
							System.out.println( "quantity__stduom :3: " + quantity );
							valueXmlString.append("<quantity__stduom>").append("<![CDATA[" + quantity + "]]>").append("</quantity__stduom>");							
						 */
						reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

					}
				} // end of line_no__invtrace
				else if (currentColumn.trim().equals("lot_no") )			
				{
					System.out.println(">>>>>>>>>>In IC lot_no:");
					priceList = genericUtility.getColumnValue("price_list", dom1);
					priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);
					fullRet = genericUtility.getColumnValue("full_ret", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					// 16/04/10 manoharan wrong column value taken
					//lotNo = genericUtility.getColumnValue("item_code", dom);
					lotNo = genericUtility.getColumnValue("lot_no", dom);
					// end 16/04/10 manoharan
					tranDate = genericUtility.getColumnValue("tran_date", dom1);
					siteCode = genericUtility.getColumnValue("site_code", dom1);
					invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
					unitRate = genericUtility.getColumnValue("unit__rate", dom);
					unitRate = unitRate == null ? "" : unitRate;
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					retReplFlag = genericUtility.getColumnValue("ret_rep_flag", dom);
					String iValStr = genericUtility.getColumnValue("line_no__inv", dom);
					sQuantity=checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom));//added by nandkumar gadkari on 18/07/19
					tranId = genericUtility.getColumnValue("tran_id", dom);
					if (iValStr != null && iValStr.indexOf(".") > 0)
					{
						iValStr = iValStr.substring(0,iValStr.indexOf("."));
					}
					//Added by Santosh on 06/12/2016 to append invoice id to itemChange retStr [Start]
					sreturnAdjOpt = distCommon.getDisparams("999999", "SRETURN_ADJ_OPT", conn);
					System.out.println("sreturnAdjOpt:::["+sreturnAdjOpt+"]");
					if ("M".equalsIgnoreCase(sreturnAdjOpt))
					{
						orderByStr = " ORDER BY MRH.EFF_COST ";
					}
					else if("E".equalsIgnoreCase(sreturnAdjOpt))
					{
						orderByStr = " ORDER BY MRH.INVOICE_DATE ASC,MRH.INVOICE_ID ASC ";
					}
					else if("L".equalsIgnoreCase(sreturnAdjOpt))
					{
						orderByStr = " ORDER BY MRH.INVOICE_DATE DESC,MRH.INVOICE_ID DESC ";
					}

					//Changed by Santosh on 10/05/2017 to not set invoice id if SRETURN_ADJ_OPT is not defined
					//if(invoiceId == null || invoiceId.trim().length() == 0)
					//Changed by Santosh on 23/05/2017 to set rate and doc_key if SRETURN_ADJ_OPT is defined for invoice_id ref case
					//if( (invoiceId == null || invoiceId.trim().length() == 0) &&  !"NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
					if( !"NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
					{
						/*int noSchemeHist = 0;
							String schemeKey = "", disParmVarValue = "", docKey = "", docValue = "";
							String sNoSchemeHist = distCommon.getDisparams("999999","SCHEME_HIST_NUM",conn);
							System.out.println( "sNoSchemeHist :: " + sNoSchemeHist );
							noSchemeHist = Integer.parseInt(sNoSchemeHist);
							for (int keyCtr = 1; keyCtr <= noSchemeHist ;  keyCtr++)
							{
								schemeKey = "SCHEME_HIST_KEY" + keyCtr ;
								System.out.println(">>>>>>>>>In for loop schemeKey:"+schemeKey);
								disParmVarValue = distCommon.getDisparams("999999",schemeKey,conn);
								System.out.println(">>>>>>>>>In for loop afer schemeKey varValue:"+disParmVarValue);

								disParmVarValue = disParmVarValue.trim();

								String[] varValueArry = disParmVarValue.split(",");

								for(int i=0; i<varValueArry.length; i++)
								{
									if("cust_code".equalsIgnoreCase(varValueArry[i].trim()))
									{
										docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom1).trim();
									}
									//Added by Santosh on 26/04/2017 for missing site_code condition [Start]
									else if("site_code".equalsIgnoreCase(varValueArry[i].trim()))
									{
										docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom1).trim();
									}
									//Added by Santosh on 26/04/2017 for missing site_code condition [End]
									else if("item_code".equalsIgnoreCase(varValueArry[i].trim()))
									{
										docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom).trim();
									}
									else if("lot_no".equalsIgnoreCase(varValueArry[i].trim()))
									{
										docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom).trim();
									}
									else if("invoice_id".equalsIgnoreCase(varValueArry[i].trim()))
									{
										continue;
									}

									if (docKey != "" && docKey.trim().length() > 0)
									{
										docKey = docKey + "," + ( docValue == null || docValue.trim().length() == 0 ? "" : docValue);
									}
									else
									{
										docKey = docValue;
									}
								}
							}
							System.out.println(">>>>>>>>after for loop docKey:"+docKey);

							sql = " SELECT MRH.INVOICE_ID,SUM(CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END), MRH.EFF_COST "
								+ " FROM MIN_RATE_HISTORY MRH, SRETURNDET SRDET WHERE MRH.INVOICE_ID=SRDET.INVOICE_ID(+) "
								+ " AND MRH.DOC_KEY LIKE '"+docKey+"%' AND MRH.QUANTITY-MRH.QUANTITY_ADJ>0 AND MRH.QUANTITY IS NOT NULL "
								+ " GROUP BY MRH.INVOICE_ID,MRH.QUANTITY, MRH.EFF_COST HAVING MRH.QUANTITY-SUM(CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END)>0 "
								+ " ORDER BY MRH.EFF_COST";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								invoiceId = checkNull(rs.getString("INVOICE_ID"));
								valueXmlString.append("<invoice_id>").append("<![CDATA[" + invoiceId + "]]>").append("</invoice_id>");
								setNodeValue(dom1, "invoice_id", invoiceId);
							}
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

							sql = "SELECT INV_LINE_NO, LINE_NO FROM INVOICE_TRACE WHERE INVOICE_ID = ? AND ITEM_CODE = ? AND LOT_NO = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, invoiceId);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lotNo);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								String liLineNoStr = getNumString( rs.getString("INV_LINE_NO"));
								lineNoInv = Integer.parseInt(liLineNoStr);
								String lineNoInvTrace = getNumString(rs.getString("LINE_NO"));
								int ilineNoInvTrace = Integer.parseInt(lineNoInvTrace);
								valueXmlString.append("<line_no__inv>").append("<![CDATA[" + lineNoInv + "]]>").append("</line_no__inv>"); 
								valueXmlString.append("<line_no__invtrace>").append("<![CDATA[" + ilineNoInvTrace + "]]>").append("</line_no__invtrace>"); 
								System.out.println("lineNoInv before >>> " + lineNoInv);
								setNodeValue( dom, "line_no__inv", lineNoInv );
								setNodeValue( dom, "line_no__invtrace", ilineNoInvTrace );
								invoiceId = null;
							}
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							if(rs != null)
							{
								rs.close();
								rs = null;
							}*/

						//Added by Santosh on 23/05/2017 to check if invoice_id reference is available in Min_rate_history [Start]
						if( checkNull(invoiceId).trim().length() == 0)
						{
							curFormItemLotHMap.put("cust_code", genericUtility.getColumnValue("cust_code", dom1));
							curFormItemLotHMap.put("item_code", itemCode);
							curFormItemLotHMap.put("lot_no", lotNo);
							curFormItemLotHMap.put("site_code", siteCode);
							curFormItemLotHMap.put("quantity", genericUtility.getColumnValue("quantity", dom));

							custCode = genericUtility.getColumnValue("cust_code", dom1);
							slineNo =  genericUtility.getColumnValue("line_no", dom);
							//lineNo = Integer.parseInt(slineNo);

							//sql commented by nandkumar gadkari on 18/04/19

							/*sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
									+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
									+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
									//Changed by Santosh on 16/05/2017
									//+ " WHERE MRH.INVOICE_ID =SRDET.INVOICE_ID(+) AND MRH.CUST_CODE = ?"
									+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
									+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
									//Changed by Santosh on 05/05/2017 to set 0 if qty_adj is null value
									//+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY-MRH.QUANTITY_ADJ > 0"
									+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
									+ " AND MRH.QUANTITY IS NOT NULL"
									+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
									+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0"
									//Changed by Santosh on 23/05/2017 to avoid sql exception if SRETURN_ADJ_OPT value is other than ('E', 'L', 'M')
									//+ " ORDER BY "+orderByStr;
							 */								//sql added for remove join with sreturnDet table  by nandkumar gadkari on 18/07/19
							sql =" SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
									+ "	CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END AS QTY_ADJ, MRH.EFF_COST,MRH.DOC_KEY "// MRH.DOC_KEY added by nandkumar on 21/08/19
									+ " FROM MIN_RATE_HISTORY MRH "
									+ " WHERE  MRH.CUST_CODE = ?"
									+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
									+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END >= ?  "
									+ " AND MRH.QUANTITY IS NOT NULL "
									+ " AND CASE WHEN MRH.STATUS IS NULL THEN 'A' ELSE MRH.STATUS END <> 'X' "// added by nandkumar gadkari on 30/12/19
									+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,MRH.QUANTITY_ADJ,MRH.DOC_KEY "
									+ orderByStr;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lotNo);
							pstmt.setString(4, siteCode);
							pstmt.setDouble(5, sQuantity);// added by nandkumar gadkari on 18/07/19
							rs = pstmt.executeQuery();

							while(rs.next())
							{
								invoiceId = checkNullandTrim(rs.getString("INVOICE_ID"));
								//ADDED BY NANDKUMAR GADKARI ON 21/08/19--------------start---------------
								minRateDocKey = checkNull(rs.getString("DOC_KEY"));
								if(minRateDocKey.indexOf(invoiceId) != -1)
								{

									//ADDED BY NANDKUMAR GADKARI ON 21/08/19--------------end---------------
									invoiceQty = checkNull(rs.getString("QUANTITY"));
									//adjQty = checkNull(rs.getString("QTY_ADJ"));
									adjQty =rs.getDouble("QTY_ADJ");
									//Added by Santosh on 16/05/2017
									rate = rs.getDouble("EFF_COST");

									curRecordItemLotHMap.put("cust_code", checkNull(rs.getString("CUST_CODE")));
									curRecordItemLotHMap.put("item_code", checkNull(rs.getString("ITEM_CODE")));
									curRecordItemLotHMap.put("lot_no", checkNull(rs.getString("LOT_NO")));
									curRecordItemLotHMap.put("site_code", checkNull(rs.getString("SITE_CODE")));
									curRecordItemLotHMap.put("quantity", invoiceQty);
									//	Changes  by Nandkumar Gadkari on 14/09/18--------[Start]---------
									//	minRateDocKey = generateDocKey(dom1, dom, invoiceId, conn); commented by Nandkumar Gadkari on 14/09/18
									//	dokkeyList = new ArrayList<String>();
									/*if ("M".equalsIgnoreCase(sreturnAdjOpt))
									{
										dokkeyList = generateDocKey(dom1, dom," ", conn);
									}
									else {*/
									dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);
									//}

									int size = dokkeyList.size();
									System.out.println("dokkk key size: " +size);
									for(int i=0;i<=1;i++)
									{
										cnt=0;
										minRateDocKey = dokkeyList.get(i);
										/*if(lineNo > 1)
											{
										 */	//Changed by Santosh on 16/05/2017 as invoiceId is not set on itemchange
										//invoiceId = getAvailableInvId(dom2, curFormItemLotHMap, curRecordItemLotHMap, invoiceId, adjQty);
										String docKeyvalue="";
										if (minRateDocKey.trim().length() > 0) {

											String[] docKeyStr = minRateDocKey.split(",");

											for(int j=0; j<docKeyStr.length; j++)
											{	
												//System.out.println( "docKeyStr :: " + docKeyStr[j]);
												System.out.println( "docKeyStrlength :: " + docKeyStr.length);
												cnt++;
											}
										}
										System.out.println( "cnt :: " + cnt );
										if(cnt ==5)
										{
											//ADDED BY NANDKUMAR GADKARI ON 26/07/19--------------start---------------
											domTotalQty=0;
											slineNo = genericUtility.getColumnValue( "line_no", dom );
											lineNo= slineNo == null || slineNo.trim().length() == 0 ? 0 : Integer.parseInt(slineNo.trim()); 
											adjQty =rs.getDouble("QTY_ADJ");
											curFormItemLotHMap.put("doc_key", minRateDocKey);
											curFormItemLotHMap.put("line_no", slineNo);
											sql1 = " SELECT SDET.INVOICE_ID, SUM(SDET.QUANTITY) AS QTY_ADJ"
													+ " FROM SRETURN SRET, SRETURNDET SDET"
													+ " WHERE SRET.TRAN_ID = SDET.TRAN_ID"
													+ " AND SRET.CONFIRMED = 'N'"
													+ " AND SDET.DOC_KEY  = ?"
													+ " AND SDET.ITEM_CODE = ?"
													+ " AND SDET.LOT_NO = ?"
													+ " AND SRET.SITE_CODE = ?"
													+ " AND SRET.CUST_CODE = ?"
													+" AND SDET.TRAN_ID <> ?"
													+ "  GROUP BY SDET.INVOICE_ID";

											pstmt1 = conn.prepareStatement(sql1);
											pstmt1.setString(1, minRateDocKey);
											pstmt1.setString(2, itemCode);
											pstmt1.setString(3, lotNo);
											pstmt1.setString(4, siteCode);
											pstmt1.setString(5, custCode);
											tranId = tranId == null || tranId.trim().length() == 0 ? "  " : tranId;
											pstmt1.setString(6, tranId);

											rs1 = pstmt1.executeQuery();

											if(rs1.next())
											{
												srDQuantity = rs1.getDouble(2); 
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
											domTotalQty = getDomQuantityUsed(dom2, curFormItemLotHMap);
											System.out.println("domTotalQty : " +domTotalQty);

											adjQty=adjQty+srDQuantity+domTotalQty;
											//ADDED BY NANDKUMAR GADKARI ON 26/07/19------------end
											//sql1 = "SELECT COUNT(*) FROM MIN_RATE_HISTORY WHERE DOC_KEY =? AND QUANTITY - CASE WHEN QUANTITY_ADJ IS NULL THEN 0 ELSE QUANTITY_ADJ END > 0 "; commented and sql changed by nandkumar gadkari on 26/07/19
											sql1 = "SELECT COUNT(*) FROM MIN_RATE_HISTORY WHERE DOC_KEY =? AND QUANTITY - (CASE WHEN QUANTITY_ADJ IS NULL THEN 0 ELSE QUANTITY_ADJ END  + ? ) > 0 "
													+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
											pstmt1 = conn.prepareStatement(sql1);
											pstmt1.setString(1, minRateDocKey);
											pstmt1.setDouble(2, srDQuantity);

											rs1 = pstmt1.executeQuery();

											if(rs1.next())
											{
												cnt = rs1.getInt(1); 
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
											if(cnt > 0)
											{
												invoiceId = getAvailableInvId(dom,dom2, curFormItemLotHMap, curRecordItemLotHMap, invoiceId, minRateDocKey, adjQty);// current dom added to parameters by Nandkumar Gadkari on 14/09/18 
												//}
												tempInvoiceId=invoiceId;
												if( invoiceId != null && invoiceId.trim().length() > 0)
												{
													break;
												}
											}
										}
									}
									if (tempInvoiceId != null && tempInvoiceId.trim().length() > 0)
									{
										break;
									}// //	commented   by Nandkumar Gadkari on 15/10/18
									//	Changes  by Nandkumar Gadkari on 14/09/18--------[end]---------

								}
							}
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

							//-----------Nandkumar Gadkari on 15/10/18------------------------------[start]--------------------------------
							if (tempInvoiceId == null || tempInvoiceId.trim().length() == 0)
							{
								sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
										//	+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, " //COMMENTED BY NANDKUMAR GADKARI ON 25/12/18
										+ " MRH.EFF_COST"
										+ " FROM MIN_RATE_HISTORY MRH"
										//	+ ",  SRETURNDET SRDET" //COMMENTED BY NANDKUMAR GADKARI ON 25/12/18
										+ " WHERE "
										//+ "MRH.DOC_KEY =SRDET.DOC_KEY(+) "//COMMENTED BY NANDKUMAR GADKARI ON 25/12/18
										//	+ " AND MRH.CUST_CODE = ?" //COMMENTED BY NANDKUMAR GADKARI ON 17/12/18 TO SET DOC_KAY WITHOUT CUST_CODE
										//	+ " AND "
										+ " MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"

										//	+ " AND MRH.SITE_CODE = ? " commented by nandkumar gadkari on 26/07/19
										//+ " AND MRH.QUANTITY IS NOT NULL"
										+ " AND CASE WHEN MRH.STATUS IS NULL THEN 'A' ELSE MRH.STATUS END <> 'X' "// added by nandkumar gadkari on 30/12/19
										+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE "
										//+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0"

											+ orderByStr;
								pstmt = conn.prepareStatement(sql);
								//	pstmt.setString(1, custCode);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, lotNo);
								//	pstmt.setString(3, siteCode); commented by nandkumar gadkari on 26/07/19
								rs = pstmt.executeQuery();

								while(rs.next())
								{
									invoiceId = checkNull(rs.getString("INVOICE_ID"));
									invoiceQty = checkNull(rs.getString("QUANTITY"));
									//adjQty = checkNull(rs.getString("QTY_ADJ"));//COMMENTED BY NANDKUMAR GADKARI ON 25/12/18

									rate = rs.getDouble("EFF_COST");

									curRecordItemLotHMap.put("cust_code", checkNull(rs.getString("CUST_CODE")));
									curRecordItemLotHMap.put("item_code", checkNull(rs.getString("ITEM_CODE")));
									curRecordItemLotHMap.put("lot_no", checkNull(rs.getString("LOT_NO")));
									curRecordItemLotHMap.put("site_code", checkNull(rs.getString("SITE_CODE")));
									curRecordItemLotHMap.put("quantity", invoiceQty);

									dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);

									int size = dokkeyList.size();
									System.out.println("dokkk key size: " +size);
									for(int i=0;i<=size-1;i++)//08/03/19 change int =1 to 0 by nandkumar gadkari on 08/03/19
									{		
										cnt=0;
										minRateDocKey = dokkeyList.get(i);
										String docKeyvalue="";
										if (minRateDocKey.trim().length() > 0) {

											String[] docKeyStr = minRateDocKey.split(",");

											for(int j=0; j<docKeyStr.length; j++)
											{	
												System.out.println( "docKeyStr :: " + docKeyStr[j]);
												System.out.println( "docKeyStrlength :: " + docKeyStr.length);
												cnt++;
											}
										}
										System.out.println( "cnt :: " + cnt );
										if(cnt !=5)
										{
											sql1 = "SELECT COUNT(*) FROM MIN_RATE_HISTORY WHERE DOC_KEY =? "
													+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
											pstmt1 = conn.prepareStatement(sql1);
											pstmt1.setString(1, minRateDocKey);

											rs1 = pstmt1.executeQuery();

											if(rs1.next())
											{
												cnt = rs1.getInt(1); 
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
											if(cnt > 0)
											{
												invoiceId = getAvailableInvId(dom,dom2, curFormItemLotHMap, curRecordItemLotHMap, invoiceId, minRateDocKey, adjQty);// current dom added to parameters by Nandkumar Gadkari on 14/09/18 
												//}
												tempInvoiceId=invoiceId;
												if( invoiceId != null && invoiceId.trim().length() > 0)
												{
													break;
												}
											}
										}
									}
									if (tempInvoiceId != null && tempInvoiceId.trim().length() > 0)
									{
										break;
									}

								}
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
							//-----------Nandkumar Gadkari on 15/10/18------------------------------[end]--------------------------------

							if( invoiceId != null && invoiceId.trim().length() > 0 )
							{
								//Commented and changed by Santosh on 16/05/2017 to set invoice id in docKey only [Start]
								/*valueXmlString.append("<invoice_id>").append("<![CDATA[" + invoiceId + "]]>").append("</invoice_id>");
									setNodeValue(dom1, "invoice_id", invoiceId);

									sql = "SELECT INV_LINE_NO, LINE_NO FROM INVOICE_TRACE WHERE INVOICE_ID = ? AND ITEM_CODE = ? AND LOT_NO = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, invoiceId);
									pstmt.setString(2, itemCode);
									pstmt.setString(3, lotNo);
									rs = pstmt.executeQuery();

									if(rs.next())
									{
										String liLineNoStr = getNumString( rs.getString("INV_LINE_NO"));
										lineNoInv = Integer.parseInt(liLineNoStr);
										String lineNoInvTrace = getNumString(rs.getString("LINE_NO"));
										int ilineNoInvTrace = Integer.parseInt(lineNoInvTrace);
										valueXmlString.append("<line_no__inv>").append("<![CDATA[" + lineNoInv + "]]>").append("</line_no__inv>"); 
										valueXmlString.append("<line_no__invtrace>").append("<![CDATA[" + ilineNoInvTrace + "]]>").append("</line_no__invtrace>"); 
										System.out.println("lineNoInv before >>> " + lineNoInv);
										setNodeValue( dom, "line_no__inv", lineNoInv );
										setNodeValue( dom, "line_no__invtrace", ilineNoInvTrace );
										invoiceId = null;
									}
									if(pstmt != null)
									{
										pstmt.close();
										pstmt = null;
									}
									if(rs != null)
									{
										rs.close();
										rs = null;
									}*/
								isMinHisRateSet = true;
								valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
								valueXmlString.append("<doc_key>").append("<![CDATA[" + minRateDocKey + "]]>").append("</doc_key>");
								//Set inv_ref and date [STart]...PriyankaC on 26JUNE2018.
								setNodeValue( dom, "doc_key",  minRateDocKey  );
								String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
								System.out.println("dokkk key : " +docKey);
								System.out.println("docKey " +docKey);
								mrhCnt=0; //added by Nandkumar Gadkari on 14/09/19
								if(docKey.trim().length() > 0 || docKey != null )
								{
									System.out.println("inside dok key1 : " +docKey);
									sqlStr = " select INVOICE_ID , INVOICE_DATE ,EFF_COST   from MIN_RATE_HISTORY where DOC_KEY = ? " //trim(:ls_itemcode);
											+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
									pstmt = conn.prepareStatement( sqlStr );
									pstmt.setString( 1, docKey );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										mrhCnt++;//added by Nandkumar Gadkari on 14/09/19
										invRefId = checkNullandTrim(rs.getString( "INVOICE_ID" ));
										//invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
										invRefDateStr = rs.getTimestamp( "INVOICE_DATE" );
										rate = rs.getDouble( "EFF_COST" );
									}
									if( rs != null )
										rs.close();
									rs = null;
									if( pstmt != null )
										pstmt.close();
									pstmt = null;

									if(mrhCnt==0)//added by Nandkumar Gadkari on 14/09/19
									{
										rate=0;
										valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
										valueXmlString.append("<doc_key>").append("<![CDATA[]]>").append("</doc_key>");
										valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
										valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
									}
									else//added by Nandkumar Gadkari on 14/09/19
									{
										//valueXmlString.append("<invoice_ref>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_ref>");//commented by Nandkumar Gadkari on 14/09/19
										valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");// rate protected by nandkumar gadkari on 25/02/18
										/*if(invRefDate != null && invRefDate.trim().length() >0)
											{
												valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
											}*/
										System.out.println("inside dok key1 invoiceId : " +invRefId);
										if(docKey.indexOf(invRefId) != -1)//if added by Nandkumar Gadkari on 14/09/19
										{
											valueXmlString.append("<invoice_ref>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_ref>");//added by Nandkumar Gadkari on 14/09/19
											if(invRefDateStr != null )
											{
												valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ sdf.format(invRefDateStr)+"]]>").append("</inv_ref_date>");
											}
										}
										else//else added by Nandkumar Gadkari on 14/09/19
										{
											valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
											valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
										}
									}

								}
								//Set inv_ref and date [End] PriyankaC on 26 June2018
								invoiceId = null;
								//Commented and changed by Santosh on 16/05/2017 to set invoice id in docKey only [End]
							}
							else
							{
								rate=0;//added by Nandkumar Gadkari on 14/09/19
								valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
								valueXmlString.append("<doc_key>").append("<![CDATA[]]>").append("</doc_key>");
								valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
								valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");

							}

						}
						else if(invoiceId != null && invoiceId.trim().length()>0)
						{
							//minRateDocKey = generateDocKey(dom1, dom, invoiceId, conn); //commented  by Nandkumar Gadkari on 14/09/18

							//Added by Nandkumar Gadkari on 14/09/18----------------[Start]---------------------------------------
							//dokkeyList = new ArrayList<String>();
							dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);
							curFormItemLotHMap.put("cust_code", genericUtility.getColumnValue("cust_code", dom1));
							curFormItemLotHMap.put("item_code", itemCode);
							curFormItemLotHMap.put("lot_no", lotNo);
							curFormItemLotHMap.put("site_code", siteCode);
							curFormItemLotHMap.put("quantity", genericUtility.getColumnValue("quantity", dom));
							custCode = genericUtility.getColumnValue("cust_code", dom1);
							slineNo = genericUtility.getColumnValue("line_no", dom);
							//lineNo = Integer.parseInt(slineNo);

							int size = dokkeyList.size();
							for(int i=0;i<=size-1;i++)
							{
								minRateDocKey = dokkeyList.get(i);
								System.out.println("@@@@@@@minRateDocKey ..........[" + minRateDocKey+"]");
								//sql commented by nandkumar gadkari on 18/04/19
								/*sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
										+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
										+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
										+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
										+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
										+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
										+ " AND MRH.QUANTITY IS NOT NULL AND MRH.DOC_KEY = ? "
										+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
										+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0"*/
								//sql added for remove join with sreturnDet table  by nandkumar gadkari on 18/07/19
								sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
										+ " CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END AS QTY_ADJ, MRH.EFF_COST"
										+ " FROM MIN_RATE_HISTORY MRH "
										+ " WHERE  MRH.CUST_CODE = ?"
										+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
										+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
										+ " AND MRH.QUANTITY IS NOT NULL AND MRH.DOC_KEY = ? "
										+ " AND CASE WHEN MRH.STATUS IS NULL THEN 'A' ELSE MRH.STATUS END <> 'X' "// added by nandkumar gadkari on 30/12/19
										+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,MRH.QUANTITY_ADJ "
										+ orderByStr;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemCode);
								pstmt.setString(3, lotNo);
								pstmt.setString(4, siteCode);
								pstmt.setString(5, minRateDocKey);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									invoiceQty = checkNull(rs.getString("QUANTITY"));
									//adjQty = checkNull(rs.getString("QTY_ADJ"));
									adjQty = rs.getDouble("QTY_ADJ");
									rate = rs.getDouble("EFF_COST");
									curRecordItemLotHMap.put("cust_code", checkNull(rs.getString("CUST_CODE")));
									curRecordItemLotHMap.put("item_code", checkNull(rs.getString("ITEM_CODE")));
									curRecordItemLotHMap.put("lot_no", checkNull(rs.getString("LOT_NO")));
									curRecordItemLotHMap.put("site_code", checkNull(rs.getString("SITE_CODE")));
									curRecordItemLotHMap.put("quantity", invoiceQty);
									System.out.println("@@@@@@@@@@@@ dokkeyList["+dokkeyList+"]");

									invoiceId = getAvailableInvId(dom,dom2, curFormItemLotHMap, curRecordItemLotHMap,invoiceId, minRateDocKey, adjQty);
									tempInvoiceId =invoiceId;	
									if (invoiceId != null && invoiceId.trim().length() > 0)
									{
										break;
									}
								}
								if (pstmt != null) {
									pstmt.close();
									pstmt = null;
								}
								if (rs != null) {
									rs.close();
									rs = null;
								}// closing before break change by nandkumar gadkari on 22/01/19 
								if (tempInvoiceId != null && tempInvoiceId.trim().length() > 0)
								{
									break;
								}



							}
							//Added by Nandkumar Gadkari on 14/09/18----------------[End]---------------------------------------
							String historyType = ""; //added by rupali on 01/04/2021 
							sql = "SELECT DOC_KEY,EFF_COST,HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
									+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, minRateDocKey);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								minRateDocKey = checkNull(rs.getString("DOC_KEY"));
								rate = rs.getDouble("EFF_COST");
								historyType = checkNull(rs.getString("HISTORY_TYPE")); //added by rupali on 01/04/2021 
							}
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
							isMinHisRateSet = true;
							//added by rupali on 01/04/2021 [start]
							//valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");// rate protected by nandkumar gadkari on 25/02/18
							System.out.println("historyType::::["+historyType+"]");
							if("S".equalsIgnoreCase(historyType))
							{
								valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
							}
							else
							{
								valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
							}
							//added by rupali on 01/04/2021 [end]
							valueXmlString.append("<doc_key>").append("<![CDATA[" + minRateDocKey + "]]>").append("</doc_key>");
							//Set inv_ref and date [Start]...PriyankaC on 26JUNE2018.
							setNodeValue( dom, "doc_key",  minRateDocKey  );
							String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
							System.out.println(" dok key : " +docKey);
							mrhCnt=0; //added by Nandkumar Gadkari on 14/09/19
							if(docKey.trim().length() > 0 || docKey != null )
							{
								System.out.println("inside dok key : " +docKey);
								sqlStr = " select INVOICE_ID , INVOICE_DATE  from MIN_RATE_HISTORY where DOC_KEY = ? "
										+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, docKey );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mrhCnt++;//added by Nandkumar Gadkari on 14/09/19
									invRefId = checkNullandTrim(rs.getString( "INVOICE_ID" ));
									//invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
									invRefDateStr = rs.getTimestamp( "INVOICE_DATE" );
								}
								if( rs != null )
									rs.close();
								rs = null;
								if( pstmt != null )
									pstmt.close();
								pstmt = null;

								if(mrhCnt==0)//added by Nandkumar Gadkari on 14/09/19
								{
									rate=0;
									valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
									valueXmlString.append("<doc_key>").append("<![CDATA[]]>").append("</doc_key>");
									valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
									valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
								}
								else//added by Nandkumar Gadkari on 14/09/19
								{	

									if(docKey.indexOf(invRefId) != -1)//if added by Nandkumar Gadkari on 14/09/19
									{

										valueXmlString.append("<invoice_ref>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_ref>");
										/*if(invRefDate != null && invRefDate.trim().length() >0)
											{
												valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
											}*/
										if(invRefDateStr != null )
										{
											valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ sdf.format(invRefDateStr)+"]]>").append("</inv_ref_date>");
										}
									}
									else//else added by Nandkumar Gadkari on 14/09/19
									{
										rate=0;
										valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
										valueXmlString.append("<doc_key>").append("<![CDATA[]]>").append("</doc_key>");
										valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
										valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
									}

								}
							}
							//Set inv_ref and date [End] PriyankaC on 26 June2018
							invoiceId = null;
						}
						//Added by Santosh on 23/05/2017 to check if invoice_id reference is available in Min_rate_history [End]
					}
					//Added by Santosh on 06/12/2016 to append invoice id to itemChange retStr [End]
					infoMap = new HashMap();
					iValStr = iValStr == null || iValStr.trim().length() == 0 ? "0" : iValStr.trim();
					lineNoInv = Integer.parseInt( getNumString( iValStr ) );
					iValStr = genericUtility.getColumnValue( "quantity__stduom", dom);
					qtyStdUom = Double.parseDouble( getNumString( iValStr ) );

					lotSl =  genericUtility.getColumnValue("lot_sl", dom);
					locCode = genericUtility.getColumnValue("loc_code", dom);
					lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
					if (lineNoTrace != null )
					{
						if (lineNoTrace != null && lineNoTrace.indexOf(".") > 0)
						{
							lineNoTrace = lineNoTrace.substring(0,lineNoTrace.indexOf("."));
						}

						iLineNoTrace =  Integer.parseInt( lineNoTrace );
						infoMap.put("line_no__invtrace",lineNoTrace);
					}

					quantity = qtyStdUom;
					// Changes by Nandkumar Gadkari on 16/11/18------------Start-----------to set the cost rate from item_lot_info table 
					sql = "select rate "
							+ " from item_lot_info "
							+ " where item_code = ? "
							+ " and lot_no = ? ";

					cntItemLotInfo=0;
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode);
					pstmt.setString( 2, lotNo);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						cntItemLotInfo++;

						costRate = rs.getDouble(1);

					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if(cntItemLotInfo==0)
					{
						infoMap.put("ret_repl_flag",retReplFlag);
						infoMap.put("item_code", itemCode);
						infoMap.put("site_code", siteCode);
						infoMap.put("loc_code",locCode);
						infoMap.put("lot_no", lotNo);
						infoMap.put("lot_sl", lotSl);
						infoMap.put("tran_date", tranDate);
						infoMap.put("invoice_id", invoiceId);
						infoMap.put( "quantity__stduom", new Double( -1 * quantity ) );

						costRate = getCostRate(infoMap, conn);
					}
					infoMap = null;
					// Changes by Nandkumar Gadkari on 16/11/18------------end-----------to set the cost rate from item_lot_info table 
					valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
					setNodeValue( dom, "cost_rate", Double.toString( costRate ) );
					System.out.println( " invoiceId :1: " + invoiceId );
					if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) )
					{
						System.out.println( " retReplFlag :2: " + retReplFlag );
						if (retReplFlag.equals("R"))
						{
							sql = "select sord_no, sord_line_no "
									+ " from invoice_trace "
									+ " where invoice_id = ? "
									+ " and line_no = ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId );
							pstmt.setInt( 2, iLineNoTrace );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								sorder = rs.getString(1);
								sordLineNo = rs.getString(2);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							sql = "select count(1) from invoice_trace "
									+ " where invoice_id = ? "
									+ " and	item_code = ? " 
									+ " and	sord_no = ? "
									+ " and	sord_line_no = ? "
									+ " and	item_code <> item_code__ord " ;

							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId );
							pstmt.setString( 2, itemCode );
							pstmt.setString( 3, sorder );
							pstmt.setString( 4, sordLineNo );
							rs = pstmt.executeQuery(); 
							cnt = 0;
							if( rs.next() )
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							System.out.println( " cnt :1: " + cnt );
							if (cnt > 0)
							{
								sql = "select sum((case when quantity is null then 0 else quantity end) * " 
										+ " (case when rate is null then 0 else rate end)), "
										+ " sum(case when quantity is null then 0 else quantity end) from invoice_trace "
										+ " where invoice_id = ? "
										+ " and	item_code = ? " 
										+ " and	sord_no = ? "
										+ " and	sord_line_no = ? ";

								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId );
								pstmt.setString( 2, itemCode );
								pstmt.setString( 3, sorder );
								pstmt.setString( 4, sordLineNo );
								rs = pstmt.executeQuery(); 
								cnt = 0;
								if( rs.next() )
								{
									amount = rs.getDouble(1);
									quantity = rs.getDouble(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								totFreeCost = 0;

								sql = "select a.item_code, sum(a.quantity), a.item_code__ord "
										+ " from sorditem a "
										+ " where a.sale_order = ? "
										+ " and a.line_no = ? "
										+ " and a.item_code <> ? "
										+ " and nature = 'F' "
										+ " and not exists (select b.item_code  from sorditem b "
										+ " where b.sale_order = a.sale_order "
										+ " and b.line_no= a.line_no "
										+ " and a.item_code = b.item_code "
										+ " and nature = 'C') "
										+ " group by a.item_code, a.item_code__ord ";

								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, sorder);
								pstmt.setString( 2, sordLineNo);
								pstmt.setString( 3, itemCode);
								rs = pstmt.executeQuery(); 
								cnt = 0;
								while ( rs.next() )
								{
									freeItemCode = rs.getString(1);
									freeQty = rs.getDouble(2);
									itemCodeOrd = rs.getString(3);			

									effCostFree = 0;
									sql = "select count(1), avg(eff_cost) "
											+ " from bomdet " 
											+ " where bom_code = ? "
											+ " and item_code = ? "
											+ " and nature = 'F' ";
									pstmt1 = conn.prepareStatement( sql );
									pstmt1.setString( 1, itemCodeOrd);
									pstmt1.setString( 2, itemCode);
									rs1 = pstmt1.executeQuery(); 
									cnt = 0;
									if( rs1.next() )
									{
										cnt =  rs1.getInt(1);
										effCostFree =  rs1.getDouble(2);
										if (cnt > 0 )
										{
											totFreeCost +=  freeQty * effCostFree;
											totFreeCost = getRequiredDecimal( totFreeCost, 4 );
										}
									}
									rs1.close();
									pstmt1.close();
									pstmt1 = null;
									rs1 = null;
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								amount = amount - totFreeCost;

								sql = "select sum(case when a.drcr_flag = 'C' then " 
										+ " ((case when b.drcr_amt is null then 0 else b.drcr_amt end) * -1) else 0 End) "
										+ " from drcr_rcp a, drcr_rdet b "
										+ " where  a.tran_id = b.tran_id "
										+ " and b.invoice_id  = ? "
										+ " and b.item_code	= ? "
										+ " and  a.sreturn_no is null" ;	
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								pstmt.setString( 2, itemCode);
								rs = pstmt.executeQuery(); 
								cnt = 0;
								if ( rs.next() )
								{
									amtDrCr = rs.getDouble(1);
									rate = (amount + amtDrCr) / quantity; 
									System.out.println( "rate calc db LOt NO :: " + rate );
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}	
							else	
							{
								System.out.println( "else 1 " );
								sql = "select rate, discount, rate__clg "
										+ " from invoice_trace "
										+ " where invoice_id = ? "
										+ " and line_no = ? ";
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								pstmt.setInt( 2, iLineNoTrace);
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									rate = rs.getDouble(1);
									discount = rs.getDouble(2);
									rateClg = rs.getDouble(3);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
								if (lineNoTrace != null )
								{
									if (lineNoTrace != null && lineNoTrace.indexOf(".") > 0)
									{
										lineNoTrace = lineNoTrace.substring(0,lineNoTrace.indexOf("."));
									}

									iLineNoTrace =  Integer.parseInt( lineNoTrace );
								}


								sql = "select sum((case when quantity is null then 0 else quantity end) * "
										+ " (case when rate is null then 0 else rate end)), "
										+ " sum(case when quantity is null then 0 else quantity end) "
										+ " from invoice_trace "
										+ " where  invoice_id   = ? "
										+ " and	line_no = ? ";
								//+ " and	inv_line_no = ? ";
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								//pstmt.setInt( 2, lineNoInv );
								pstmt.setInt( 2, iLineNoTrace );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									amount = rs.getDouble(1);
									quantity = rs.getDouble(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								sql = "select sum(case when a.drcr_flag = 'C' then " 
										+ " ((case when b.drcr_amt is null then 0 else b.drcr_amt end) * -1) else 0 End) "
										+ " from drcr_rcp a, drcr_rdet b "
										+ " where a.tran_id = b.tran_id "
										+ " and b.invoice_id = ? "
										+ " and b.line_no__inv = ? "
										+ " and a.sreturn_no is null" ;
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								pstmt.setInt( 2, lineNoInv );
								rs = pstmt.executeQuery(); 
								amtDrCr = 0;
								if( rs.next() )
								{

									amtDrCr = rs.getDouble(1);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								System.out.println( "amount lot :1: " + amount );
								System.out.println( "quantity lot :1: " + quantity );
								System.out.println( "amtDrCr :1: " + amtDrCr );
								rate = (amount + amtDrCr) / quantity;
								rate = getRequiredDecimal( rate, 4 );
								System.out.println( "rate calc LOt NO :: " + rate );
							}
						}
						else
						{
							System.out.println( "priceList :2 : " + priceList );
							if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
							{
								tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
								itemCode = itemCode == null ? "" : itemCode;
								lotNo = lotNo == null ? "" : lotNo;

								rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
								System.out.println( "rate pic rate LOt NO :: " + rate );
							}
						}
					}
					else
					{
						System.out.println( "retReplFlag :2 : " + retReplFlag );
						//Changed by Santosh on 16/05/2017 to not to set rate if already set from min_rate_history
						//if (retReplFlag != null && retReplFlag.equals("R"))
						if (retReplFlag != null && retReplFlag.equals("R") && !isMinHisRateSet)
						{	
							System.out.println(">>>>>>>>>>>>>In lot_no before getMinRate @ 3313:");
							StringBuffer minRateBuff = getMinRate( dom, dom1, "lot_no", valueXmlString, conn);
							System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );
							//valueXmlString = minRateBuff;

							String rateValStr = getTagValue(  minRateBuff.toString(), "rate" );

							System.out.println( "rateValStr LOt NO :: " + rateValStr );
							rate = getRequiredDecimal( Double.parseDouble( getNumString( rateValStr ) ), 4 );

						}
						System.out.println( "rate before If :1: " + rate );
						if (rate <= 0)
						{
							rate = 0;
							if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
							{
								tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
								itemCode = itemCode == null ? "" : itemCode;
								lotNo = lotNo == null ? "" : lotNo;
								rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
								System.out.println( "rate in  If :1: " + rate );
							}
						}
					}

					pickLowerRate = distCommon.getDisparams("999999","PICK_LOWER_RATE", conn);
					if (pickLowerRate.equals("NULLFOUND"))
					{
						pickLowerRate = "N";
					}
					if ( pickLowerRate != null && pickLowerRate.equals("Y"))
					{
						if (priceList !=null && priceList.trim().length() >  0)
						{
							tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;							
							itemCode = itemCode == null ? "" : itemCode;
							lotNo = lotNo == null ? "" : lotNo;
							priceListRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D", quantity, conn);
							System.out.println( "priceListRate LOt NO :: " + priceListRate );
							if (priceListRate < rate && priceListRate > 0 )
							{
								rate = priceListRate;
								System.out.println( "rate priceListRate < rate && priceListRate > 0 :1: " + rate );
							}
						}
					}
					System.out.println( "Rate LOt NO :: " + rate );
					valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
					setNodeValue( dom, "rate", rate );

					rateClg = sRateClg;
					if (rateClg <= 0 )
					{
						if ( priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
						{
							tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
							itemCode = itemCode == null ? "" : itemCode;
							lotNo = lotNo == null ? "" : lotNo;								
							rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode,lotNo,"L", quantity, conn);
							if (rateClg == -1)
							{
								rateClg = 0 ;
							}
						}
						if (rateClg == 0 )
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", rate );
						}
						else
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", rateClg );
						}
					}
					//listType = distCommon.getPriceListType(priceList, conn);
					unit = genericUtility.getColumnValue( "unit", dom );
					if ( unitRate != null && unit != null && unit.trim().equals( unitRate.trim() ) )
					{
						System.out.println("manohar rate__stduom 5 [" + rate + "]");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate + "]]>").append("</rate__stduom>");
						setNodeValue( dom, "rate__stduom", rate );
						valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");// column protected by nandkumar gadkari on 29/01/19
						setNodeValue( dom, "conv__rtuom_stduom", "1" );
					}
					else if ( unitRate != null && unit != null && !unit.trim().equals( unitRate.trim() ) )
					{	
						System.out.println( "unit :: " + unit );
						System.out.println( "unitRate :: " + unitRate );
						System.out.println( "itemCode :: " + itemCode );
						System.out.println( "rate :: " + rate );
						System.out.println( "fact :: " + fact );
						convAr = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, (double) fact,  conn);
						convFact = Double.parseDouble( convAr.get(0).toString() );
						rateStd = Double.parseDouble( convAr.get(1).toString() );
						System.out.println("manohar rate__stduom 6 [" + rateStd + "]");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStd + "]]>").append("</rate__stduom>");
						setNodeValue( dom, "rate__stduom", rateStd );
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + convFact + "]]>").append("</conv__rtuom_stduom>");
						setNodeValue( dom, "conv__rtuom_stduom", convFact );
					}
					sQuantity = Double.parseDouble( getNumString( genericUtility.getColumnValue("quantity", dom) ) );
					quantity = sQuantity;

					valueXmlString.append("<mrp_value>").append("<![CDATA[" + quantity * rateClg + "]]>").append("</mrp_value>");
					setNodeValue( dom, "mrp_value", quantity * rateClg );
					System.out.println("<<<<<<<<<<DISCOUNT>>>>>>>>>>= "+ discount);

					//valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
					//setNodeValue( dom, "discount", discount );
					System.out.println(">>>>>>>>>>>>>In lot_no before getMinRate @ 3421:");
					/*Added by ABhijit Gaikwas*/
					String lslotsl="";
					System.out.println("site_code is:"+siteCode);
					System.out.println("lotNo is:"+lotNo);
					System.out.println("locCode is:"+locCode);
					System.out.println("itemCode is:"+itemCode);
					sql="select lot_sl from stock where site_code = ? and " +
							"lot_no = ? and item_code = ?";
					pstmt= conn.prepareStatement( sql );//,,,
					pstmt.setString( 1, siteCode);
					pstmt.setString( 2, lotNo );
					pstmt.setString( 3, itemCode );
					rs = pstmt.executeQuery(); 
					//amtDrCr = 0;
					if( rs.next() )
					{

						lslotsl = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					System.out.println("*********Lot sl is:"+lslotsl);
					if (lslotsl == null || lslotsl.trim().length() == 0)
					{
						System.out.println("lslotsl is null in lot_no itemChanged"+lslotsl);
						String lsSretLotsl="";
						lsSretLotsl = distCommon.getDisparams( "999999", "SRETURN_DEFAULT_LOTSL", conn );
						System.out.println("lsSretLotsl ["+lsSretLotsl+"] in lot_no Disparam Value");
						if(lsSretLotsl == null || lsSretLotsl.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(lsSretLotsl))
						{
							valueXmlString.append("<lot_sl>").append("2S").append("</lot_sl>");
						}
						else
						{
							System.out.println("lsSretLotsl["+lsSretLotsl+"]");
							valueXmlString.append("<lot_sl>").append("<![CDATA[" + lsSretLotsl + "]]>").append("</lot_sl>");
						}
					}
					else
					{
						valueXmlString.append("<lot_sl>").append("<![CDATA[" + lslotsl + "]]>").append("</lot_sl>");
					}

					/*End*/

					//Changed by Santosh on 16/05/2017 to not to set rate if already set from min_rate_history [Start]
					//StringBuffer minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn );
					StringBuffer minRateBuff = null;
					if(!isMinHisRateSet)
					{
						minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ); 
						System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );
						valueXmlString = minRateBuff;
					}
					//Changed by Santosh on 16/05/2017 to not to set rate if already set from min_rate_history [End]

					//added by nandkumar Gadkari on 16/11/18--------------Start--------to set mfg date an exp date from ite_lot_info
					valueXmlString = (gbfIcExpMfgDate(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
					//added by nandkumar Gadkari on 16/11/18--------------end--------to set mfg date an exp date from ite_lot_info

					System.out.println("lslotsl["+lslotsl+"]");
					setNodeValue( dom, "lot_sl",lslotsl);
					reStr = itemChanged(dom, dom1, dom2, objContext, "lot_sl", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);

				} // end of lot_no
				else if (currentColumn.trim().equals("lot_sl") )		
				{
					//added by nandkumar Gadkari on 16/11/18--------------Start--------to set mfg date an exp date from ite_lot_info
					valueXmlString = (gbfIcExpMfgDate(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
							conn));
					//added by nandkumar Gadkari on 16/11/18--------------end--------to set mfg date an exp date from ite_lot_info
					siteCode = genericUtility.getColumnValue("site_code", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					locCode = genericUtility.getColumnValue("loc_code", dom);
					lotNo = genericUtility.getColumnValue("lot_no", dom);
					lotSl = genericUtility.getColumnValue("lot_sl", dom);

					if (locCode == null || locCode.equals("null") || locCode.trim().length() == 0 )
					{
						locCode = distCommon.getDisparams("999999","ALLOC_FGLOC", conn);
					}
					if (lotNo == null || lotNo.equals("null"))
					{
						lotNo = "               ";
					}
					if (lotSl == null || lotSl.equals("null"))
					{
						lotSl = "     ";
					}
					//Changed by Manoj dtd 14/04/2017 to set values from item_lot_info
					sql = "select site_code__mfg, mfg_date, exp_date, pack_code "
							+ " from item_lot_info "
							+ " where item_code = ? "
							+ " and lot_no = ? ";

					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode);
					pstmt.setString( 2, lotNo);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						cntItemLotInfo++;
						siteCodeMfg = rs.getString(1);
						mfgDate = rs.getTimestamp(2);
						expDate = rs.getTimestamp(3);
						packCode = rs.getString(4);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if(cntItemLotInfo==0)
					{
						sql = "select a.site_code__mfg, a.mfg_date, a.exp_date, a.pack_code "
								+ " from stock a "
								+ " where a.item_code = ? "
								+ " and a.site_code = ? "
								+ " and a.lot_no = ? "
								+ " and a.lot_sl = ? ";

						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode);
						pstmt.setString( 2, siteCode);
						pstmt.setString( 3, lotNo);
						pstmt.setString( 4, lotSl);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							siteCodeMfg = rs.getString(1);
							mfgDate = rs.getTimestamp(2);
							expDate = rs.getTimestamp(3);
							packCode = rs.getString(4);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
					}
					cntItemLotInfo=0;

					if (siteCodeMfg == null || siteCodeMfg.equals("null") || siteCodeMfg.trim().length() == 0)
					{
						siteCodeMfg = getMfgSitePackCode(itemCode, siteCode, locCode,  lotNo,  lotSl, "M",  conn);
					}

					if (packCode == null || packCode.equals("null") || packCode.trim().length() == 0)
					{
						packCode = getMfgSitePackCode(itemCode, siteCode, locCode,  lotNo,  lotSl, "P",  conn);
					}
					siteCodeMfg = getAbsString( siteCodeMfg ); 
					sql = "select count(1) "
							+ " from stock a, invstat b "
							+ " where a.inv_stat  = b.inv_stat "
							+ " and a.item_code = ? "
							+ " and a.site_code = ? "
							+ " and a.lot_no = ? "
							+ " and a.lot_sl = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode);
					pstmt.setString( 2, siteCode);
					pstmt.setString( 3, lotNo);
					pstmt.setString( 4, lotSl);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						cnt = rs.getInt(1);
					}
					mcount =cnt; // 19/04/10 manoharan
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					if (cnt == 0 )
					{
						//expDate commented by Nandkumar gadkari on 16/11/18 to set the date from item_lot_info table 
						//valueXmlString.append("<exp_date  protect =\"0\">").append("<![CDATA[" + ( expDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString()  : "" ) + "]]>").append("</exp_date>");
						valueXmlString.append("<pack_code  protect =\"0\">").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
						valueXmlString.append("<site_code__mfg  protect =\"0\">").append("<![CDATA[" + siteCodeMfg + "]]>").append("</site_code__mfg>");
					}
					else
					{
						//Added & Commented by sarita on 29 JUN 18 [START][if exp_date available against lot_no , lot_sl , item_code and site_code in stock then system should not allow to change exp_date]
						/*if(expDate != null)
							{
								valueXmlString.append("<exp_date  protect =\"1\">").append("<![CDATA[" + new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString()  + "]]>").append("</exp_date>");
							}
							else
							{
								valueXmlString.append("<exp_date  protect =\"0\">").append("<![CDATA[]]>").append("</exp_date>");
							}*///expDate commented by Nandkumar gadkari on 16/11/18 to set the date from item_lot_info table 


						//valueXmlString.append("<exp_date  protect =\"0\">").append("<![CDATA[" + ( expDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString() : "" ) + "]]>").append("</exp_date>");
						//Added & Commented by sarita on 29 JUN 18 [END] [START][if exp_date available against lot_no , lot_sl , item_code and site_code in stock then system should not allow to change exp_date]
						//mfgDate commented by Nandkumar gadkari on 16/11/18 to set the date from item_lot_info table 
						//	valueXmlString.append("<mfg_date  protect =\"0\">").append("<![CDATA[" + ( mfgDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(mfgDate).toString() : "" ) + "]]>").append("</mfg_date>");		
						valueXmlString.append("<pack_code  protect =\"1\">").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
						valueXmlString.append("<site_code__mfg  protect =\"1\">").append("<![CDATA[" + siteCodeMfg + "]]>").append("</site_code__mfg>");
					}

					String mtranDateStr = genericUtility.getColumnValue( "tran_date", dom );
					mtranDate = Timestamp.valueOf(genericUtility.getValidDateString( ( mtranDateStr == null ? getCurrdateInAppFormat() : mtranDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					String mexpDateStr = genericUtility.getColumnValue( "exp_date", dom );
					System.out.println( "mexpDateStr :: " + mexpDateStr );
					// 20/05/10 manoharan commented as expiry date is already fetched from stock
					//if( mexpDateStr != null )
					//{
					//	mexpDate = Timestamp.valueOf(genericUtility.getValidDateString( ( mexpDateStr == null ? getCurrdateInAppFormat() : mexpDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					//}
					//else
					//{
					//Commented By Mahesh Patidar on 16/04/2012 for request "DISWIN0045" to set expire date from dom if not found in stock
					//mexpDate = expDate; //genericUtility.getValidDateString( ( mexpDateStr == null ?  : mexpDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					//}
					//Added By Mahesh Patidar on 16/04/2012 for request "DISWIN0045" to set expire date from dom if not found in stock
					if(expDate != null)
					{
						mexpDate = expDate;
					}
					else
					{
						mexpDate = Timestamp.valueOf(genericUtility.getValidDateString( ( mexpDateStr == null ? getCurrdateInAppFormat() : mexpDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}//ended By Mahesh Patidar
					String ldtLrdateStr = genericUtility.getColumnValue( "lr_date", dom1 );

					ldtLrdate = ldtLrdateStr == null ? null : Timestamp.valueOf(genericUtility.getValidDateString( ( ldtLrdateStr == null ? getCurrdateInAppFormat() : ldtLrdateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					System.out.println( "ldtLrdate :: " + ldtLrdate );
					if( ldtLrdate == null || "19000101".equals( genericUtility.getValidDateTimeString( ldtLrdateStr, genericUtility.getApplDateFormat(), "yyyymmdd" ) ) )
					{
						ldtLrdate = mtranDate;
					}

					System.out.println( "ldtLrdateStr :: " + ldtLrdateStr );
					System.out.println( "mexpDate :: " + mexpDate );
					if (mexpDate != null )
					{
						mdiffDays = (int)utilMethods.DaysAfter( ldtLrdate, mexpDate );//daysafter(date(ldt_lrdate), date(mexp_date))
					}
					else
					{
						mdiffDays = 0;
					}
					mdays = mdiffDays;

					custCode = genericUtility.getColumnValue( "cust_code", dom1 );//dw_header.getitemstring(1,"cust_code")
					siteCode = genericUtility.getColumnValue( "site_code", dom1 ); //dw_header.getitemstring(1,"site_code")

					sqlStr = " select channel_partner " // into :ls_channel_partner
							+" from site_customer "
							+" 	where cust_code = ? " //:ls_cust_code
							+" and site_code = ? "; //:ls_sitecode ;
					pstmt = conn.prepareStatement( sqlStr );
					pstmt.setString( 1, custCode );
					pstmt.setString( 2, siteCode );
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						channelPartner = rs.getString( "channel_partner" );
					}
					rs.close();
					rs =  null;
					pstmt.close();
					pstmt = null;

					if( channelPartner == null || channelPartner.trim().length() == 0 )
					{
						sqlStr = "select channel_partner " //into :ls_channel_partner 
								+" from customer "
								+"	where cust_code = ? ";//:ls_cust_code;

						pstmt = conn.prepareStatement( sqlStr);

						pstmt.setString( 1, custCode );

						rs = pstmt.executeQuery();

						if( rs.next() )
						{
							channelPartner = rs.getString( "channel_partner" );
						}

						rs.close();
						rs =  null;
						pstmt.close();
						pstmt = null;

					}

					if( channelPartner == null || channelPartner.trim().length() == 0 )//Added by mukesh chauhan on 05/02/2020
					{
						channelPartner="N";
					}
					/*if ( channelPartner != null && !"Y".equals( channelPartner )  || "Y".equalsIgnoreCase(shExpDiscAppl.trim()) )*///changes by Nandkumar Gadkari on 19MAR2018 
					if (( channelPartner != null && !"Y".equals( channelPartner ))  || "Y".equalsIgnoreCase(shExpDiscAppl.trim()) )// Changed by mukesh chauhan on 05/02/2020
					{

						sqlStr = " select deduct_perc " //into :mdisc 
								+" from  sreturn_norms "
								+" where days_from <= ? " //:mdays
								+" and days_to >= ? "; //:mdays;

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setInt( 1, mdays );
						pstmt.setInt( 2, mdays );

						rs = pstmt.executeQuery();

						if( rs.next() )
						{
							mdisc = rs.getDouble( "deduct_perc" );
						}
						else
						{
							rs.close();
							rs =  null;
							pstmt.close();
							pstmt = null;

							sqlStr = " select DISC_PERC " //into :mdisc 
									+" from customer "
									+" where cust_code = ? "; //:ls_cust_code;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, custCode );

							rs = pstmt.executeQuery();

							if( rs.next() )
							{
								mdisc = rs.getDouble( "DISC_PERC" );
							}
							rs.close();
							rs =  null;
							pstmt.close();
							pstmt = null;

						}

						if( rs != null )
						{
							rs.close();
						}
						rs =  null;
						if( pstmt != null )
						{
							pstmt.close();
						}
						pstmt = null;
					}
					String lcDiscountStr = genericUtility.getColumnValue( "discount", dom );//dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),"discount")

					if ( lcDiscountStr != null ) //isnull(lc_discount) then lc_discount = 0
					{
						lcDiscount = Double.parseDouble( lcDiscountStr.trim().length() == 0 ? "0" : lcDiscountStr.trim() );
					}
					if( mdisc != 0 )
					{
						valueXmlString.append("<discount>").append("<![CDATA[" + mdisc + "]]>").append("</discount>");
					}
					// else part added ba Mahesh Patidar on 28/03/12 for discount value change according to lot_sl
					else
					{
						itemCode = genericUtility.getColumnValue("item_code", dom);
						invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
						retReplFlag = genericUtility.getColumnValue("ret_rep_flag", dom);
						lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);

						if (lineNoTrace != null && lineNoTrace.indexOf(".") > 0)
						{
							lineNoTrace = lineNoTrace.substring(0,lineNoTrace.indexOf("."));
						}
						lineNoTrace = lineNoTrace == null || lineNoTrace.trim().length() == 0 ? "0" : lineNoTrace.trim();
						iLineNoTrace =  Integer.parseInt( lineNoTrace );
						if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) )
						{
							System.out.println( " retReplFlag :2: " + retReplFlag );
							if (retReplFlag.equals("R"))
							{
								sql = "select sord_no, sord_line_no "
										+ " from invoice_trace "
										+ " where invoice_id = ? "
										+ " and line_no = ? ";
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId );
								pstmt.setInt( 2, iLineNoTrace );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									sorder = rs.getString(1);
									sordLineNo = rs.getString(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								sql = "select count(1) from invoice_trace "
										+ " where invoice_id = ? "
										+ " and	item_code = ? " 
										+ " and	sord_no = ? "
										+ " and	sord_line_no = ? "
										+ " and	item_code <> item_code__ord " ;

								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId );
								pstmt.setString( 2, itemCode );
								pstmt.setString( 3, sorder );
								pstmt.setString( 4, sordLineNo );
								rs = pstmt.executeQuery(); 
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								System.out.println( " cnt :1: " + cnt );
								if(cnt == 0)	
								{
									System.out.println( "if in " );
									sql = "select discount "
											+ " from invoice_trace "
											+ " where invoice_id = ? "
											+ " and line_no = ? ";
									pstmt= conn.prepareStatement( sql );
									pstmt.setString( 1, invoiceId);
									pstmt.setInt( 2, iLineNoTrace);
									rs = pstmt.executeQuery(); 
									if( rs.next() )
									{
										discount = rs.getDouble(1);
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
								}
							}
						}
						//valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
						valueXmlString.append("<discount>").append("<![CDATA[" + mdisc + "]]>").append("</discount>");// uncommented by nandkumar gadkari on 16/01/19 for set discount 0
						//setNodeValue( dom, "discount", discount );
					} // ended else by Mahesh

					mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 );
					mtranDate	=  Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//dw_header.getitemdatetime(1,"tran_date")
					sqlStr = "select count(*) mcount1 " //into :mcount1
							+" from stock "
							+" where item_code = ? " //:mitem_code 
							+"   and site_code = ? " //:msite_code
							//	+"   and loc_code = ? " //:mloc_code commented by nandkumar gadkari on 12/04/19 
							+"   and lot_no = ? " //:mlot_no
							+"   and lot_sl = ? " //:mlot_sl
							+"   and exp_date <= ? "; //:mtran_date;

					pstmt = conn.prepareStatement( sqlStr );

					pstmt.setString( 1, itemCode );
					pstmt.setString( 2, siteCode );
					//pstmt.setString( 3, locCode ); //:mloc_code commented by nandkumar gadkari on 12/04/19
					pstmt.setString( 3, lotNo );
					pstmt.setString( 4, lotSl );
					pstmt.setTimestamp( 5, mtranDate );

					rs = pstmt.executeQuery();

					if( rs.next() )
					{
						mcount1 = rs.getInt( "mcount1" );
					}
					rs.close();
					rs =  null;
					pstmt.close();
					pstmt = null;
					if( mcount1 > 0 )
					{
						valueXmlString.append("<status>").append("<![CDATA[" + "E" + "]]>").append("</status>");
						setNodeValue( dom, "status", "E" );
						valueXmlString.append("<stk_opt>").append("<![CDATA[" + "N" + "]]>").append("</stk_opt>");

						mvarValue = distCommon.getDisparams( "999999", "EXPIRED_LOC", conn );
						System.out.println("mvarValue is:"+mvarValue);
						valueXmlString.append("<loc_code>").append("<![CDATA[" + mvarValue + "]]>").append("</loc_code>"); //dw_currobj.setitem(1,"loc_code",mvar_value)

						sqlStr = "select descr " //into :ls_loc_descr 
								+" from location "
								+" where loc_code = ? "; //:mvar_value;
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mvarValue );

						rs = pstmt.executeQuery();

						if( rs.next() )
						{
							locDescr = rs.getString( "descr" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<location_descr>").append("<![CDATA[" + locDescr + "]]>").append("</location_descr>");
						// added by nandkumar gadkari on21/09/19
						reStr = itemChanged(dom, dom1, dom2, objContext, "status", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					else
					{
						if( mcount > 0 )
						{
							sqlStr = " select case when min_shelf_life is null then 0 else min_shelf_life end " //into :mmin_shlife 
									+" from item "
									+" where item_code = ? "; //:mitem_code;

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, itemCode );

							rs = pstmt.executeQuery();

							if( rs.next() )
							{
								mminShlife = rs.getDouble( 1 );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							// commented by nandkumar gadkari on 18/11/19
							/*	mchkDate = distCommon.CalcExpiry( mtranDate, mminShlife );
								System.out.println( "mexpDate :: " + mexpDate + "mchkDate" +mchkDate );
								if( mexpDate.before( mchkDate ) )
								{
									valueXmlString.append("<status>").append("<![CDATA[" + "N" + "]]>").append("</status>");
									// 22/04/10 manoharan to set the loc_code accordingly
									setNodeValue( dom, "status", "N" );
									reStr = itemChanged(dom, dom1, dom2, objContext, "status", editFlag, xtraParams);
									pos = reStr.indexOf("<Detail2>");
									reStr = reStr.substring(pos + 9);
									pos = reStr.indexOf("</Detail2>");
									reStr = reStr.substring(0,pos);
									valueXmlString.append(reStr);
									// end 22/04/10 manoharan to set the loc_code accordingly
								}*/
						}else
						{
							lsDamagedtype = distCommon.getDisparams( "999999", "DAMAGED_SRET_TYPE", conn );

							if( !"NULLFOUND".equalsIgnoreCase( lsDamagedtype ) && lsDamagedtype.trim().length() > 0 ) // and len(trim(ls_damagedtype)) > 0 then
							{
								// 08/07/11 manoharan tran_type to be taken from header not detail
								//tranType = genericUtility.getColumnValue( "tran_type", dom ); //dw_header.getitemstring(1,"tran_type")
								tranType = genericUtility.getColumnValue( "tran_type", dom1 ); //dw_header.getitemstring(1,"tran_type")
								// end 08/07/11 manoharan tran_type to be taken from header not detail
								if( lsDamagedtype != null && tranType != null && lsDamagedtype.trim().equalsIgnoreCase( tranType.trim() ) )
								{
									valueXmlString.append("<status>").append("<![CDATA[" + "D" + "]]>").append("</status>");
									// 22/04/10 manoharan to set the loc_code accordingly
									setNodeValue( dom, "status", "D" );
									reStr = itemChanged(dom, dom1, dom2, objContext, "status", editFlag, xtraParams);
									pos = reStr.indexOf("<Detail2>");
									reStr = reStr.substring(pos + 9);
									pos = reStr.indexOf("</Detail2>");
									reStr = reStr.substring(0,pos);
									valueXmlString.append(reStr);
									// end 22/04/10 manoharan to set the loc_code accordingly
									//dw_detedit[ii_currformno].setitem(1,"status",'D')
								}
							}
						}
					}
					lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom );// dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(), "invoice_id")
					//mlineNoInv = Integer.parseInt( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) );//dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),"line_no__inv")
					//Added by Manoj dtd 05/12/12 to cast double to int
					mlineNoInv =(int) Double.parseDouble(( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ));
					mcode = genericUtility.getColumnValue( "item_code", dom );// dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"item_code")

					sqlStr = " select item_code "  //into :ls_invoice_item 
							+" from invoice_trace "
							+" where invoice_id  = ? " //:ls_invoice_id
							+" 	and inv_line_no = ? "; //:mline_no_inv;
					pstmt = conn.prepareStatement( sqlStr );
					pstmt.setString( 1, lsInvoiceId );
					pstmt.setInt( 2, mlineNoInv );

					rs = pstmt.executeQuery();

					if( rs.next() )
					{
						lsInvoiceItem = rs.getString( "item_code" );
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;			

					if(lsInvoiceId != null &&  lsInvoiceItem != null && mcode !=null && lsInvoiceItem.trim().equals(mcode.trim() ) && lsInvoiceId.trim().length() > 0 )
					{
						valueXmlString.append("<stk_opt>").append("<![CDATA[" + "N" + "]]>").append("</stk_opt>");
						setNodeValue( dom, "stk_opt", "N" );
						//tempNode = dom.getElementsByTagName("stk_opt").item(0);
						//tempNode.getFirstChild().setNodeValue( "N" );
						//tempNode = null;

						//dw_detedit[ii_currformno].setitem(1,"stk_opt",'N')
					}
					////////////////////

					reStr = itemChanged(dom, dom1, dom2, objContext, "stk_opt", editFlag, xtraParams);
					System.out.println( "reStr :: " + reStr + "\n :: " + reStr.length() );
					pos = reStr.indexOf("<Detail2>");
					System.out.println( "pos :: " + pos );
					reStr = reStr.substring(pos + 9);
					System.out.println( "reStr1 :: " + reStr + "\n :: " + reStr.length() );
					pos = reStr.indexOf("</Detail2>");
					System.out.println( "pos1 :: " + pos );
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);

					String lcQtyStr = genericUtility.getColumnValue( "quantity", dom );//dw_detedit[ii_currformno].GetItemNumber(1, "quantity")
					lcQty = Double.parseDouble( getNumString( lcQtyStr ) );
					double grwtPerArt = 0, trwtPerArt = 0;
					sqlStr = " select (case when qty_per_art is null then 1 else qty_per_art end), GROSS_WT_PER_ART,TARE_WT_PER_ART         "//into :lc_qty_per_art
							+" from 	 stock "
							+" where  item_code = ? " //:mitem_code
							+" 	and 	 site_code = ? " //:msite_code
							+"  and 	 loc_code  = ? " //:mloc_code
							+"  and 	 lot_no = ? " //:mlot_no
							+"  and 	 lot_sl = ? "; //:mlot_sl ;					 

					pstmt = conn.prepareStatement( sqlStr );

					pstmt.setString( 1, itemCode );
					pstmt.setString( 2, siteCode );
					pstmt.setString( 3, locCode );
					pstmt.setString( 4, lotNo );
					pstmt.setString( 5, lotSl );

					rs = pstmt.executeQuery();

					if( rs.next() )
					{
						lcQtyPerArt = rs.getDouble( 1 );
						grwtPerArt =  rs.getDouble( 2 );
						trwtPerArt =  rs.getDouble( 3 );
					}
					// 20/08/14 manoharan the else condition is not required
					//else
					//{
					if( lcQtyPerArt > 0 )
					{
						int llNoArt = (int)(lcQty / lcQtyPerArt);
						if( llNoArt > 0 )
						{	

							valueXmlString.append("<no_art>").append("<![CDATA[" + llNoArt + "]]>").append("</no_art>");
							valueXmlString.append("<gross_weight>").append("<![CDATA[" + getReqDecimal((llNoArt * grwtPerArt),3) + "]]>").append("</gross_weight>");
							valueXmlString.append("<tare_weight>").append("<![CDATA[" + getReqDecimal((llNoArt * trwtPerArt),3)+ "]]>").append("</tare_weight>");
							valueXmlString.append("<net_weight>").append("<![CDATA[" + getReqDecimal((llNoArt * (grwtPerArt - trwtPerArt) ),3) + "]]>").append("</net_weight>");

						}
					}

					//}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					/*
						String minRateStr = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ).toString();
 						System.out.println( "minRateStr1 :: " + minRateStr ); 

						//////
						pos = minRateStr.indexOf("<Detail2>");
						System.out.println( "pos :: " + pos );
						minRateStr = minRateStr.substring(pos + 9);
						System.out.println( "minRateStr2 :: " + minRateStr );
					 */
					//////
					//valueXmlString.append( minRateStr );// = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ); 
					//StringBuffer minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ); 
					//valueXmlString = minRateBuff;
					//gbf_scheme_history( mrate, currentColumn.trim() );
					//gbf_get_set_costrate();
					// ADDED BY NANDKUMAR GADKARI ON 21/09/19

					reStr = itemChanged(dom, dom1, dom2, objContext, "status", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
				} //  end of lot_sl

				//start of new
				else if (currentColumn.trim().equals("loc_code") )		
				{
					mlocCode = genericUtility.getColumnValue( "loc_code", dom );//dw_detedit[ii_currformno].Getitemstring(1, colname) 


					sqlStr = " select descr mloc from location where loc_code = ? "; //:mloc_code";
					pstmt = conn.prepareStatement( sqlStr );

					pstmt.setString( 1, mlocCode );

					rs = pstmt.executeQuery();

					if( rs.next() )
					{
						mloc = rs.getString( "mloc" );
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");
					//	dw_detedit[ii_currformno].setitem(1,"location_descr",mloc)
				}
				else if (currentColumn.trim().equals( "mfg_date" ) )
				{						
					mitemCode = genericUtility.getColumnValue( "item_code", dom ); //  dw_detedit[ii_currformno].getitemstring(1,"item_code")
					mmfgDate = null;
					String dtStr = genericUtility.getColumnValue( "mfg_date", dom ); //dw_detedit[ii_currformno].getitemdatetime(1,"mfg_date")
					if(dtStr != null)
					{
						mmfgDate = Timestamp.valueOf(genericUtility.getValidDateString(dtStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					lotNo	= genericUtility.getColumnValue( "lot_no", dom ); // dw_detedit[ii_currformno].getitemstring(1,"lot_no")

					if( mmfgDate != null ) //not isnull(mmfgDate) )
					{	
						sqlStr = " select ( case when shelf_life is null then 0 else shelf_life end ) mshlife "
								+" from item_lot_packsize "
								+" where item_code = ? " //:mitem_code 
								+"	and ? between lot_no__from and lot_no__to " ; //:mlot_no between lot_no__from and lot_no__to;

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mitemCode );
						pstmt.setString( 2, lotNo );
						rs = pstmt.executeQuery();
						mshlife = 0;
						if( rs.next() )
						{
							mshlife = rs.getDouble( "mshlife" );
						}

						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( mshlife == 0 )
						{
							sqlStr = " select shelf_life mshlife " //into :mshlife 
									+" from item "
									+" where item_code = ? "; //:mitem_code;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mitemCode );
							rs = pstmt.executeQuery();
							mshlife = 0;
							if( rs.next() )
							{
								mshlife = rs.getDouble( "mshlife" );
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						//	
						if( mshlife == 0 )
						{
							mexpDate = null; //setnull(mexp_date)
							valueXmlString.append("<exp_date>").append("<![CDATA["+""+"]]>").append("</exp_date>");
							setNodeValue( dom, "exp_date", "" ); //Added By Mahesh Patidar on 16/04/2012 for set expiry date in node
						}
						else
						{	
							mexpDate  = distCommon.CalcExpiry( mmfgDate, mshlife );
							System.out.println("AAAA MEXPdATE [ "+mexpDate+" ] ");
							valueXmlString.append("<exp_date>").append("<![CDATA[" + genericUtility.getValidDateString( mexpDate.toString(),genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) + "]]>").append("</exp_date>");
							setNodeValue( dom, "exp_date",  genericUtility.getValidDateString( mexpDate.toString(),genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ));//Added By Mahesh Patidar on 16/04/2012 for set expiry date in node
						}
						System.out.println("AAAA MEXPdATE [ "+mexpDate+" ] ");
					}
					else
					{
						mexpDate = null; //setnull(mexp_date)
						valueXmlString.append("<exp_date>").append("<![CDATA["+""+"]]>").append("</exp_date>");
						setNodeValue( dom, "exp_date", "" );//Added By Mahesh Patidar on 16/04/2012 for set expiry date in node
					}

					//dw_detedit[ii_currformno].setitem(1,"exp_date",mexp_date)		

					if( mexpDate != null )
					{
						String mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 ); // dw_header.getitemdatetime(1,"tran_date")
						mtranDate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

						mtranDateStr = genericUtility.getColumnValue( "exp_date", dom );  //dw_currobj.getitemdatetime(1, "exp_date")
						if (mtranDateStr != null)
						{
							mexpDate  = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("mexpDate [ "+mexpDate+" ]");
							mtranDateStr = genericUtility.getColumnValue( "lr_date", dom1 ); // dw_header.getitemdatetime(1,"lr_date")
							// 17/06/10 manoharan commented and put in else condition as null is to be taken care off
							//ldtLrdate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

							//Commented By Mahesh Patidar on 02/05/2012 for checking the dom string is null or not
							//if( mtranDateStr == null || "19000101".equals( genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), "yyyymmdd" ) ) ) //Isnull(ldt_lrdate) or string(ldt_lrdate,"yyyymmdd") = "19000101" then
							//{	
							//	ldtLrdate = mtranDate;
							//}
							//Ended By Mahesh Patidar

							//Change Condition By Mahesh Patidar on 02/05/2012 for Checking DOM tran Date String is null or not
							if( mtranDateStr == null || "19000101".equals( genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), "yyyymmdd" ) ) ) //Isnull(ldt_lrdate) or string(ldt_lrdate,"yyyymmdd") = "19000101" then
							{	
								ldtLrdate = mtranDate;
							}//ended By Mahesh Patidar
							else
							{
								ldtLrdate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

							}
							System.out.println("lrDate = [ "+ldtLrdate+" ]");
							// end 17/06/10 manoharan
							mdiffDays = (int)utilMethods.DaysAfter( ldtLrdate, mexpDate );
							mdays = mdiffDays;
							custCode = genericUtility.getColumnValue( "cust_code", dom1 ); //dw_header.getitemstring(1,"cust_code")
							siteCode = genericUtility.getColumnValue( "site_code", dom1 ); // dw_header.getitemstring(1,"site_code")

							sqlStr = " select channel_partner ls_channel_partner " //into :ls_channel_partner
									+"		from site_customer "
									+"	where cust_code = ? " //:ls_cust_code
									+"		and site_code = ? "; //:ls_sitecode ;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, custCode );
							pstmt.setString( 2, siteCode );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								channelPartner = rs.getString( "ls_channel_partner" );
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( channelPartner == null )//isnull(ls_channel_partner) then
							{
								sqlStr = "select channel_partner ls_channel_partner " //into :ls_channel_partner
										+" from customer "
										+"	where cust_code = ? ";//:ls_cust_code;
								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, custCode );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									channelPartner = rs.getString( "ls_channel_partner" );
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if( channelPartner == null || channelPartner.trim().length() == 0 )//Added by mukesh chauhan on 05/02/2020
							{
								channelPartner="N";
							}
							if( !"Y".equalsIgnoreCase( channelPartner ) || "Y".equalsIgnoreCase(shExpDiscAppl.trim()) )//changes by Nandkumar Gadkari on 19MAR2018 
							{

								sqlStr = " select deduct_perc mdisc "
										+" 		from sreturn_norms "
										+"	where days_from <= ? " //:mdays
										+"		and days_to >= ? "; //:mdays;
								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setInt( 1, mdays );
								pstmt.setInt( 2, mdays );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mdisc = rs.getInt( "mdisc" );
								}
								else
								{
									mdisc = 0;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							String lcDiscountStr = genericUtility.getColumnValue( "discount", dom );// gedw_detedit[ii_currformno].getitemnumber(1,"discount")
							if( lcDiscountStr == null || lcDiscountStr.trim().length() == 0 )
							{
								lcDiscount = 0;
							}
							else
							{
								lcDiscount = Double.parseDouble( lcDiscountStr.trim() );
							}
							valueXmlString.append("<discount>").append("<![CDATA[" + mdisc + "]]>").append("</discount>");
						}
						//dw_detedit[ii_currformno].setitem(dw_detedit[ii_currformno].getrow(),"discount",mdisc);
					}
				}//end of mfg_date.
				//new ic
				else if (currentColumn.trim().equals( "status" ) )
				{
					mstatus = genericUtility.getColumnValue( "status", dom );// dw_detedit[ii_currformno].getitemstring(1,"status")
					System.out.println( " mstatus :1: " + mstatus );

					lsSretLocCode = distCommon.getDisparams( "999999", "SRET_LOC_CODE", conn );
					itemCode = genericUtility.getColumnValue("item_code", dom);
					lotNo = genericUtility.getColumnValue("lot_no", dom);
					//lotSl = genericUtility.getColumnValue("lot_sl", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom1);
					System.out.println( " lsSretLocCode :1: " + lsSretLocCode );
					if(  "L".equals( mstatus ) )
					{
						mvarName = "LOOSE_LOC";
					}
					else if( "D".equals( mstatus ) )
					{
						mvarName = "DAMAGED_LOC";
					}
					else if( "E".equals( mstatus ) ) // mstatus = 'E' then
					{
						mvarName = "EXPIRED_LOC";
					}
					else if( "N".equals( mstatus ) ) //elseif mstatus = 'N' then
					{
						mvarName = "NEAREXP_LOC";
					}
					else if( "H".equals( mstatus ) ) //elseif mstatus = 'H' then
					{
						mvarName = "HOLD_LOC";
					}
					else if( "S".equals( mstatus ) ) //elseif mstatus = 'S' then
					{
						if( "D".equals( lsSretLocCode.trim() ) )
						{
							mvarName = "ALLOC_FGLOC";
						}
						else
						{
							//
							lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom1 ); //dw_header.getitemstring(1, "invoice_id")
							System.out.println( "lsInvoiceId :157: " + lsInvoiceId ); 
							if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
							{
								boolean recordNotFound = true;
								//mlineNoInv = Integer.parseInt( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ); //dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),'line_no__inv')
								//Added by Manoj dtd 05/12/12 to cast double to int
								mlineNoInv =(int) Double.parseDouble(( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ));
								String llLinenotraceStr = genericUtility.getColumnValue( "line_no__invtrace", dom ); //dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),'line_no__invtrace')
								if (llLinenotraceStr != null && llLinenotraceStr.indexOf(".") > 0)
								{
									llLinenotraceStr = llLinenotraceStr.substring(0,llLinenotraceStr.indexOf("."));
								}
								llLinenotraceStr = llLinenotraceStr == null || llLinenotraceStr.trim().length() == 0 ? "0" : llLinenotraceStr.trim();
								llLinenotrace = Integer.parseInt( llLinenotraceStr == null || llLinenotraceStr.trim().length() == 0 ? "0" : llLinenotraceStr.trim() );

								if( llLinenotrace > 0 )
								{
									System.out.println( "recordNotFound :177: " + recordNotFound ); 
									sqlStr = " select desp_id, desp_line_no " //  into :ls_desp_id, :ls_desp_line_no
											+"		from invoice_trace " 
											+" where invoice_id = ? " //:ls_invoice_id
											+"	and line_no = ? "; //:ll_linenotrace;

									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsInvoiceId );
									pstmt.setInt( 2, llLinenotrace );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsDespId = rs.getString( "desp_id" );
										lsDespLineNo = rs.getString( "desp_line_no" );
										recordNotFound = false;
									}
									/*else
										{
											recordNotFound = true;
										}*/
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								else
								{
									System.out.println( "recordNotFound :10: " + recordNotFound ); 
									lsDespLineNo = ("   " + mlineNoInv).substring( 0, 3 );

									sqlStr = "select desp_id, desp_line_no " //into :ls_desp_id, :ls_desp_line_no
											+"		from invoice_trace " 
											+"	where invoice_id = ? " //:ls_invoice_id
											+"		and inv_line_no= ? " //:mline_no_inv
											+"		and desp_line_no = ? "; //:ls_desp_line_no; 

									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsIinvoiceId );
									pstmt.setInt( 2, mlineNoInv );
									pstmt.setString( 3, lsDespLineNo );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsDespId = rs.getString( "desp_id" );
										lsDespLineNo = rs.getString( "desp_line_no" );
										recordNotFound = false;
									}
									/*else
										{

										}*/
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								System.out.println( "recordNotFound :: " + recordNotFound ); 
								if( !recordNotFound )
								{	
									System.out.println( "recordNotFound :5ftt: " + recordNotFound ); 
									sqlStr = "select loc_code ls_loc_code "
											+" from despatchdet "
											+" where desp_id = ? " //:ls_desp_id
											+"	and	line_no = ? "; //:ls_desp_line_no 
									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsDespId );
									pstmt.setString( 2, lsDespLineNo );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsLocCode = rs.getString( "ls_loc_code" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if (lsLocCode != null && lsLocCode.trim().length() > 0 && !"null".equalsIgnoreCase( lsLocCode.trim() ) )
									{
										System.out.println( "lsLocCode :5tt: " + lsLocCode ); 
										valueXmlString.append("<loc_code>").append("<![CDATA[" + lsLocCode + "]]>").append("</loc_code>");
										setNodeValue( dom, "loc_code", lsLocCode );
										//dw_detedit[ii_currformno].setitem(1,"loc_code", ls_loc_code)
										sqlStr = " select descr ls_loc_descr "
												+" from location "
												+" where loc_code = ? "; //:ls_loc_code;

										pstmt = conn.prepareStatement( sqlStr );

										pstmt.setString( 1, lsLocCode );

										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											locDescr = rs.getString( "ls_loc_descr" );
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										valueXmlString.append("<location_descr>").append("<![CDATA[" + locDescr + "]]>").append("</location_descr>");
									}
								}
							}
							else
							{	
								System.out.println( "lsSretLocCode :3fsd: " + lsSretLocCode ); 
								if( "D".equals( lsSretLocCode ) )
								{
									lsLocCode = distCommon.getDisparams( "999999", "ALLOC_FGLOC", conn );					
									System.out.println( "lsLocCode :4: " + lsLocCode ); 
									valueXmlString.append("<loc_code>").append("<![CDATA[" + lsLocCode + "]]>").append("</loc_code>");
									setNodeValue( dom, "loc_code", lsLocCode );
									//dw_detedit[ii_currformno].setitem(1,"loc_code", ls_loc_code)
									sqlStr = "select descr ls_loc_descr " 
											+" 	from location "
											+" where loc_code = ? "; //:ls_loc_code;

									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsLocCode );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										locDescr = rs.getString( "ls_loc_descr" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println( "locDescr :4: " + locDescr ); 
									valueXmlString.append("<location_descr>").append("<![CDATA[" + locDescr + "]]>").append("</location_descr>");
								}
							}
						}
					}
					else
					{
						System.out.println( "mstatus :4tt: " + mstatus ); 
						if (!"S".equalsIgnoreCase( mstatus ) )
						{
							mvarName = "";
							sqlStr = " select udf_str1 mvar_name "
									+"		from gencodes "
									+" where fld_name = 'STATUS' "
									+" 	and   mod_name = 'W_SALESRETURN' "
									+" 	and   fld_value= ? ";

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mstatus );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mvarName = rs.getString( "mvar_name" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println( "mvarName :4: " + mvarName ); 
						}
					}
					System.out.println( " mvarName :1: " + mvarName );
					//mvarValue = " ";

					if( mvarName != null && mvarName.trim().length() > 0 &&  !"NULLFOUND".equalsIgnoreCase( mvarName ) )
					{
						mvarValue = distCommon.getDisparams( "999999", mvarName, conn );
						System.out.println( "mvarValue :5: " + mvarValue ); 
						valueXmlString.append("<loc_code>").append("<![CDATA[" + mvarValue + "]]>").append("</loc_code>");
					}
					if( !"NULLFOUND".equalsIgnoreCase( mvarValue ) )
					{
					}
					else
					{
						mvarValue = genericUtility.getColumnValue( "loc_code", dom ); //dw_detedit[ii_currformno].getitemstring(1,"loc_code")
						System.out.println( "mvarValue :6: " + mvarValue ); 
					}

					mloc = " ";
					sqlStr = "select descr mloc, inv_stat ls_inv_stat " //into :mloc, :ls_inv_stat 
							+"	from location " 
							+"	where loc_code = ? "; //:mvar_value;

					pstmt = conn.prepareStatement( sqlStr );

					pstmt.setString( 1, mvarValue );

					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						mloc = rs.getString( "mloc" );
						lsInvStat = rs.getString( "ls_inv_stat" );
						valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");							
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println( "mloc :6: " + mloc ); 
					System.out.println( "lsInvStat :6: " + lsInvStat ); 


					sqlStr = " select ( case when usable is null then 'N' else usable end ) mstk_opt "
							+" 	from invstat "
							+" where inv_stat = ?"; //:ls_inv_stat;

					pstmt = conn.prepareStatement( sqlStr );

					pstmt.setString( 1, lsInvStat );

					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						mstkOpt = rs.getString( "mstk_opt" );
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println( "mstkOpt :6: " + mstkOpt ); 
					if( "Y".equalsIgnoreCase( mstkOpt ) )
					{
						mstkOpt = "U";
					}
					mstkOpt = getAbsString( mstkOpt );
					System.out.println( "mstkOpt :7: " + mstkOpt ); 
					valueXmlString.append("<stk_opt>").append("<![CDATA[" + mstkOpt + "]]>").append("</stk_opt>");

					mstkOpt      = genericUtility.getColumnValue( "stk_opt", dom ); //dw_detedit[ii_currformno].getitemstring(1,"stk_opt")
					System.out.println( "mstkOpt :8: " + mstkOpt ); 
					mstatus = genericUtility.getColumnValue( "status", dom ); // dw_detedit[ii_currformno].getitemstring(1,"status")
					lsRetRepFlag = genericUtility.getColumnValue( "ret_rep_flag", dom ); // dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"ret_rep_flag")
					//added check null on 18/NOV/13 By kunal m
					mcode = checkNull( genericUtility.getColumnValue( "item_code", dom )); // dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"item_code")
					System.out.println( "lsRetRepFlag :7: " + lsRetRepFlag ); 
					lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom1 ); //dw_header.getitemstring(1, "invoice_id")
					System.out.println( "lsInvoiceId :157: " + lsInvoiceId ); 
					if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
					{
						boolean recordNotFound = true;
						//mlineNoInv = Integer.parseInt( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ); //dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),'line_no__inv')
						//Added by Manoj dtd 05/12/12 to cast double to int
						mlineNoInv =(int) Double.parseDouble(( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ));
						String llLinenotraceStr = genericUtility.getColumnValue( "line_no__invtrace", dom ); //dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),'line_no__invtrace')
						if (llLinenotraceStr != null && llLinenotraceStr.indexOf(".") > 0)
						{
							llLinenotraceStr = llLinenotraceStr.substring(0,llLinenotraceStr.indexOf("."));
						}
						llLinenotraceStr = llLinenotraceStr == null || llLinenotraceStr.trim().length() == 0 ? "0" : llLinenotraceStr.trim();
						llLinenotrace = Integer.parseInt( llLinenotraceStr == null || llLinenotraceStr.trim().length() == 0 ? "0" : llLinenotraceStr.trim() );

						if( llLinenotrace > 0 )
						{
							System.out.println( "recordNotFound :4519: " + recordNotFound ); 
						}
					}
					if( !"P".equalsIgnoreCase( lsRetRepFlag ) )
					{	
						System.out.println("kunal test lsInvoiceItem=="+lsInvoiceItem);
						sqlStr = " select item_code ls_invoice_item " 
								+" from invoice_trace "
								+" where invoice_id  = ? " //:ls_invoice_id
								+"	and line_no = ? "; //:mline_no_inv;

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, lsInvoiceId );
						pstmt.setInt( 2, llLinenotrace );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lsInvoiceItem = checkNull( rs.getString( "ls_invoice_item" ) );//added check null on 18/NOV/13 By kunal m
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println( "lsInvoiceItem :7: " + lsInvoiceItem ); 
						if( "N".equals( mstkOpt ) && "D".equals( mstatus ) 
								&& !lsInvoiceItem.trim().equals( mcode.trim() ) )
						{	
							sRate = genericUtility.getColumnValue( "rate", dom );
							System.out.println( "sRate :8: " + sRate ); 
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ ( sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) +"]]>").append("</rate>");
						}
						else
						{
							System.out.println( "In else case" ); 							
						}
					}
					else
					{
						sRate = genericUtility.getColumnValue( "rate", dom );
						System.out.println( "sRate :7: " + sRate ); 
						valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ ( sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) +"]]>").append("</rate>");
					}
					//Added by ABhijit Gaikwad
					/*if(  "E".equals( mstatus ) )
						{
							mvarName1 = "ESTATUS_REASON";
						}
						else if(  "D".equals( mstatus ) )
						{
							mvarName1 = "DSTATUS_REASON";
						}
						else if(  "N".equals( mstatus ) )
						{
							mvarName1 = "NSTATUS_REASON";
						}
						else if(  "S".equals( mstatus ) )
						{
							mvarName1 = "LSTATUS_REASON";
						}
						else if(  "Q".equals( mstatus ) )
						{
							mvarName1 = "QSTATUS_REASON";
						}
						else if(  "P".equals( mstatus ) )
						{
							mvarName1 = "PSTATUS_REASON";
						}
						//mvarValue = " ";*/
					System.out.println("Status is"+ mstatus);
					//STATUS_REASON commented by nandkumar gadkari on 23/09/19
					/*if(mstatus != null && mstatus.trim().length() >0)
						{
							mvarName1 = mstatus.trim() + "STATUS_REASON";
							System.out.println("mvarName1 is"+ mvarName1);
						}
						if( mvarName1 != null && mvarName1.trim().length() > 0 &&  !"NULLFOUND".equalsIgnoreCase( mvarName1 ) )
						{
							mvarValue1 = distCommon.getDisparams( "999999", mvarName1, conn );
							System.out.println( "mvarValue Reason Code:" + mvarValue1 ); 
					          if(mvarValue1 == null || mvarValue1.equalsIgnoreCase("NULLFOUND"))
					          {
					        	  mvarValue1="";
					           }
							valueXmlString.append("<reas_code>").append("<![CDATA[" + mvarValue1 + "]]>").append("</reas_code>");
							setNodeValue( dom, "reas_code", mvarValue1);//added by nandkumar gadkari on 11/09/19--------
							sqlStr = " Select reason_descr mreason_descr from sreturn_reason where reason_code = ? "; 

						    pstmt = conn.prepareStatement( sqlStr );
						    pstmt.setString( 1, mvarValue1 );
						    rs = pstmt.executeQuery();
						    mreasonDescr = "";
						    if( rs.next() )
						     {
							   mreasonDescr = rs.getString( "mreason_descr" );
						     }
						    rs.close();
						    rs = null;
						    pstmt.close();
						    pstmt = null;
						    System.out.println("Reson description is");
						valueXmlString.append("<reason_descr>").append("<![CDATA["+ mreasonDescr +"]]>").append("</reason_descr>");
						}*/

					if ( "N".equalsIgnoreCase( mstatus ) ||"E".equalsIgnoreCase( mstatus ) || "D".equalsIgnoreCase( mstatus ) )   //if condition added by manish mhatre on 30-july-20[when status is expiry or near exp or damaged then reason code set as per status]
					{
						sqlStr = " Select reason_descr mreason_descr "
								+ " ,reason_code, loc_code  "////added by nandkumar gadkari on 11/09/19
								+"	from sreturn_reason "
								+"	where status = ? "; //:mreas_code ;

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mstatus );

						rs = pstmt.executeQuery();
						mreasonDescr = "";
						if( rs.next() )
						{
							mreasonDescr = rs.getString( "mreason_descr" );

							mvarValue1 = rs.getString( "reason_code" );
							locCode = rs.getString( "loc_code" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;



						if( locCode!=null && locCode.trim().length() > 0 &&  mvarValue1!=null && mvarValue1.trim().length() > 0 )
						{
							valueXmlString.append("<reas_code>").append("<![CDATA[" + mvarValue1 + "]]>").append("</reas_code>");
							valueXmlString.append("<reason_descr>").append("<![CDATA["+ mreasonDescr +"]]>").append("</reason_descr>");

							valueXmlString.append("<loc_code>").append("<![CDATA["+ locCode +"]]>").append("</loc_code>");
							sqlStr = "select descr mloc " 
									+"	from location " 
									+"	where loc_code = ? ";

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, locCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mloc = rs.getString( "mloc" );
								valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");							
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else
					{
						String fullRetdet="";
						reasCode = genericUtility.getColumnValue("reas_code", dom1);
						mreasonDescr=genericUtility.getColumnValue("reason_descr", dom1);
						fullRet = genericUtility.getColumnValue("full_ret", dom1);
						fullRetdet = genericUtility.getColumnValue("full_ret", dom);
						System.out.println("fullRet"+fullRet+" fullRet1"+fullRetdet);

						System.out.println("6044 reas code>>"+reasCode+"reason descr"+mreasonDescr);
						valueXmlString.append("<reas_code>").append("<![CDATA[" + reasCode + "]]>").append("</reas_code>");
						valueXmlString.append("<reason_descr>").append("<![CDATA["+ mreasonDescr +"]]>").append("</reason_descr>");

						//added by manish mhatre on 24-aug-20[when header and detail full return 'Y' then loc code set from despatch details]
						if( "Y".equalsIgnoreCase( fullRet) &&  "Y".equalsIgnoreCase( fullRetdet))
						{
							lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom1 ); 
							System.out.println( "lsInvoiceId :6051: " + lsInvoiceId ); 
							if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
							{
								mlineNoInv =(int) Double.parseDouble(( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ));
								String llLinenotraceStr = genericUtility.getColumnValue( "line_no__invtrace", dom );
								if (llLinenotraceStr != null && llLinenotraceStr.indexOf(".") > 0)
								{
									llLinenotraceStr = llLinenotraceStr.substring(0,llLinenotraceStr.indexOf("."));
								}
								llLinenotraceStr = llLinenotraceStr == null || llLinenotraceStr.trim().length() == 0 ? "0" : llLinenotraceStr.trim();
								llLinenotrace = Integer.parseInt( llLinenotraceStr == null || llLinenotraceStr.trim().length() == 0 ? "0" : llLinenotraceStr.trim() );

								if( llLinenotrace > 0 )
								{
									System.out.println( "inside if llLinenotrace"); 
									sqlStr = " select desp_id, desp_line_no from invoice_trace " 
											+" where invoice_id = ? " 
											+"	and line_no = ? "; 

									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsInvoiceId );
									pstmt.setInt( 2, llLinenotrace );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsDespId = rs.getString( "desp_id" );
										lsDespLineNo = rs.getString( "desp_line_no" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								else
								{
									System.out.println( "else llLinenotrace "); 
									lsDespLineNo = ("   " + mlineNoInv).substring( 0, 3 );

									sqlStr = "select desp_id, desp_line_no from invoice_trace " 
											+"	where invoice_id = ? " 
											+"		and inv_line_no= ? "
											+"		and desp_line_no = ? "; 

									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsIinvoiceId );
									pstmt.setInt( 2, mlineNoInv );
									pstmt.setString( 3, lsDespLineNo );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsDespId = rs.getString( "desp_id" );
										lsDespLineNo = rs.getString( "desp_line_no" );
									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}

								System.out.println("manish lsDespId"+lsDespId+" lsDespLineNo"+lsDespLineNo);
								sql = " select loc_code from despatchdet where desp_id = ? and line_no = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString( 1, lsDespId );
								pstmt.setString( 2, lsDespLineNo );
								rs = pstmt.executeQuery(); 
								if(rs.next())
								{
									locCode = rs.getString( "loc_code" );
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								sqlStr = "select descr " 
										+"	from location " 
										+"	where loc_code = ? ";

								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, locCode );

								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mloc = rs.getString( "descr" );							
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								valueXmlString.append("<loc_code>").append("<![CDATA["+ locCode +"]]>").append("</loc_code>");
								valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");
							}

						}
						else
						{
							sqlStr = " Select loc_code  "
									+"	from sreturn_reason "
									+"	where status = ? ";

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mstatus );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								locCode = rs.getString( "loc_code" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( locCode!=null && locCode.trim().length() > 0)
							{	
								valueXmlString.append("<loc_code>").append("<![CDATA["+ locCode +"]]>").append("</loc_code>");
								sqlStr = "select descr " 
										+"	from location " 
										+"	where loc_code = ? ";

								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, locCode );

								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mloc = rs.getString( "descr" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");							
							}

						}
					}
					////added by nandkumar gadkari on 123/09/19--------end

				}//end of status

				else if (currentColumn.trim().equals( "stk_opt" ) )
				{
					mstkOpt = genericUtility.getColumnValue( "stk_opt", dom ); //dw_detedit[ii_currformno].getitemstring(1,"stk_opt")
					mstatus = genericUtility.getColumnValue( "status", dom ); //dw_detedit[ii_currformno].getitemstring(1,"status")
					lsRetRepFlag = genericUtility.getColumnValue( "ret_rep_flag", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"ret_rep_flag")
					mcode = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"item_code")

					if ( "P".equalsIgnoreCase( lsRetRepFlag ) )
					{
						sqlStr = " select item_code ls_invoice_item " 
								+"	from invoice_trace "
								+"	where invoice_id  = ? " //:ls_invoice_id
								+" 		and inv_line_no = ? "; //:mline_no_inv;

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, lsInvoiceId );
						pstmt.setInt( 2, mlineNoInv );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lsInvoiceItem = rs.getString( "ls_invoice_item" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if( "N".equals( mstkOpt )&&  "D".equals( mstatus ) && lsInvoiceItem.trim().equals( mcode.trim() ) )
						{
							//gbf_itemchg_modifier_ds(dw_detedit[ii_currformno],"rate","protect","0");
							sRate = genericUtility.getColumnValue( "rate", dom );
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
							//dw_detedit[ii_currformno].modify("rate.background.color='"+ string(RGB(255,255,255))+"'");
						}
						else
						{
							//dw_detedit[ii_currformno].modify("rate.background.color='"+ string(RGB(192,192,192))+"'");
						}
					}
				}
				// End Sanjeev - 20/08/03
				else if (currentColumn.trim().equals( "reas_code" ) )
				{
					mreasCode =	genericUtility.getColumnValue( "reas_code", dom ); //dw_detedit[ii_currformno].getitemstring(1,"reas_code")

					sqlStr = " Select reason_descr mreason_descr "
							+ " ,status, loc_code  "////added by nandkumar gadkari on 11/09/19
							+"	from sreturn_reason "
							+"	where reason_code = ? "; //:mreas_code ;

					pstmt = conn.prepareStatement( sqlStr );

					pstmt.setString( 1, mreasCode );

					rs = pstmt.executeQuery();
					mreasonDescr = "";
					if( rs.next() )
					{
						mreasonDescr = rs.getString( "mreason_descr" );
						//added by nandkumar gadkari on 11/09/19
						status = rs.getString( "status" );
						locCode = rs.getString( "loc_code" );
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<reason_descr>").append("<![CDATA["+ mreasonDescr +"]]>").append("</reason_descr>");

					//added by nandkumar gadkari on 11/09/19--------

					if(status !=null && status.trim().length() > 0 && locCode!=null && locCode.trim().length() > 0)
					{
						valueXmlString.append("<status>").append("<![CDATA["+ status +"]]>").append("</status>");
						valueXmlString.append("<loc_code>").append("<![CDATA["+ locCode +"]]>").append("</loc_code>");
						sqlStr = "select descr mloc " 
								+"	from location " 
								+"	where loc_code = ? ";

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, locCode );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							mloc = rs.getString( "mloc" );
							valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}


				}
				else if (currentColumn.trim().equals( "exp_date" ) )
				{
					String ldExpDateStr = genericUtility.getColumnValue( "exp_date", dom ); 
					if (ldExpDateStr != null)
					{
						java.sql.Timestamp ldExpDate = Timestamp.valueOf(genericUtility.getValidDateString(ldExpDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"); 
						mitemCode = genericUtility.getColumnValue( "item_code", dom ); 
						lotNo = genericUtility.getColumnValue( "lot_no", dom ); 

						sqlStr = "select shelf_life mshlife "
								+" from item_lot_packsize "
								+"	where item_code = ? "
								+"		and lot_no__from  = ? ";

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mitemCode );
						pstmt.setString( 2, lotNo );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							mshlife = rs.getDouble( "mshlife" );
						}
						else
						{
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;

							sqlStr = " select shelf_life mshlife from item where item_code = ? "; //:mitem_code;
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mitemCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mshlife = rs.getDouble( "mshlife" );
							}
						}
						if( rs != null )
							rs.close();
						rs = null;
						if( pstmt != null )
							pstmt.close();
						pstmt = null;

						if( mshlife == 0 )
						{
							mexpDate = null;
						}
						else
						{	
							mmfgDate  = distCommon.CalcExpiry( ldExpDate, mshlife * -1 );
						}

						//Changes by sarita as if mexpDate is null then its giving NullPointerException on 28 JUN 18 [START]
						//valueXmlString.append("<mfg_date>").append("<![CDATA["+ genericUtility.getValidDateString(mmfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) +"]]>").append("</mfg_date>");
						valueXmlString.append("<mfg_date>").append("<![CDATA["+ ( mmfgDate != null ? genericUtility.getValidDateString(mmfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat()) : "") +"]]>").append("</mfg_date>");
						//Changes by sarita as if mexpDate is null then its giving NullPointerException on 28 JUN 18 [END]

						String mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 );
						mtranDate = Timestamp.valueOf(genericUtility.getValidDateString(mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						mtranDateStr = genericUtility.getColumnValue( "exp_date", dom ); // dw_currobj.getitemdatetime(1, "exp_date")

						mexpDate = Timestamp.valueOf(genericUtility.getValidDateString(mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

						mtranDateStr = genericUtility.getColumnValue( "lr_date", dom1 ); // dw_header.getitemdatetime(1,"lr_date")
						if (mtranDateStr == null)
						{
							mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 );
						}
						ldtLrdate = Timestamp.valueOf(genericUtility.getValidDateString(mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						if( ldtLrdate == null || "19000101".equals( genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(),"yyyymmdd" ) )  )
						{
							ldtLrdate = mtranDate;
						}
						if (ldtLrdate != null && mexpDate != null)
						{
							mdiffDays = (int)utilMethods.DaysAfter( ldtLrdate, mexpDate );
						}
						else
						{
							mdiffDays = 0;
						}
						mdays = mdiffDays;
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						//genericUtility.getColumnValue( "cust_code", dom1 ); //dw_header.getitemstring(1,"cust_code")
						siteCode = genericUtility.getColumnValue("site_code",dom1);
						//genericUtility.getColumnValue( "site_code", dom1 ); // dw_header.getitemstring(1,"site_code")
						System.out.println("custCode [" + custCode+ "]");
						System.out.println("siteCode [" + siteCode+ "]");
						if (custCode != null && siteCode != null )
						{
							sqlStr = " select channel_partner ls_channel_partner "
									+"	from site_customer "
									+"	where cust_code = ? " //:ls_cust_code
									+"	and site_code = ? "; // :ls_sitecode ;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, custCode );
							pstmt.setString( 2, siteCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								channelPartner = rs.getString( "ls_channel_partner" );
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;
						}

						if( channelPartner == null && custCode != null)
						{
							sqlStr = " select channel_partner ls_channel_partner "
									+"	from customer "
									+"	where cust_code = ? "; //:ls_cust_code;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, custCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								channelPartner = rs.getString( "ls_channel_partner" );
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;
						}
						if( channelPartner == null || channelPartner.trim().length() == 0 )//Added by mukesh chauhan on 05/02/2020
						{
							channelPartner="N";
						}
						/*if( channelPartner != null && !"Y".equalsIgnoreCase( channelPartner ) || "Y".equalsIgnoreCase(shExpDiscAppl.trim()) )*///changes by Nandkumar Gadkari on 19MAR2018 
						if(( channelPartner != null && !"Y".equalsIgnoreCase( channelPartner )) || "Y".equalsIgnoreCase(shExpDiscAppl.trim()) )//changed by mukesh chauhan on 05/02/2020
						{

							sqlStr = " select deduct_perc mdisc from sreturn_norms "
									+"	where days_from <= ? " //:mdays
									+"		and days_to >= ? "; //:mdays;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setInt( 1, mdays );
							pstmt.setInt( 2, mdays );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mdisc = rs.getDouble( "mdisc" );
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;						  
						}
					}
					String lcDiscountStr = genericUtility.getColumnValue( "discount", dom ); // dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),"discount")

					lcDiscount = Double.parseDouble( lcDiscountStr == null || lcDiscountStr.trim().length() == 0 ? "0" : lcDiscountStr.trim() );

					valueXmlString.append("<discount>").append("<![CDATA["+ mdisc +"]]>").append("</discount>");
				}
				else if (currentColumn.trim().equals( "unit" ) )
				{
					mcode = genericUtility.getColumnValue( currentColumn.trim(), dom ); //dw_detedit[ii_currformno].GetItemString(1, colname)

					unitStd = genericUtility.getColumnValue( "unit__std", dom );
					unitStd = unitStd == null || unitStd.trim().length() == 0 ? "0" : unitStd.trim();

					// 20/04/10 manoharan mVal1 = Double.parseDouble( valStr  ); //dw_detedit[ii_currformno].GetItemString(1, "unit__std")
					mitem = genericUtility.getColumnValue( "item_code", dom ); // dw_detedit[ii_currformno].GetItemString(1, "item_code")

					valStr = genericUtility.getColumnValue( "quantity", dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					mNum1 = Double.parseDouble( valStr ); // dw_detedit[ii_currformno].getitemnumber(1, "quantity")
					mNum2 = 0;

					mNum  = Double.parseDouble( distCommon.convQtyFactor(mcode, unitStd  , mitem, mNum1, mNum2, conn ).get(1).toString() );

					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA["+ mNum2 +"]]>").append("</conv__qty_stduom>");
					//dw_detedit[ii_currformno].SetItem(1,"conv__qty_stduom", mNum2 );
					valueXmlString.append("<quantity__stduom>").append("<![CDATA["+ mNum +"]]>").append("</quantity__stduom>");
					//dw_detedit[ii_currformno].SetItem(1,"quantity__stduom", mNum)
				}
				else if (currentColumn.trim().equals( "conv__qty_stduom" ) )
				{
					valStr = genericUtility.getColumnValue( currentColumn.trim(), dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					mNum  = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].getitemnumber(1, colname)
					String mValStr  =  genericUtility.getColumnValue( "unit", dom ); // dw_detedit[ii_currformno].GetItemString(1, "unit")
					String mValStr1 = genericUtility.getColumnValue( "unit__std", dom ); //dw_detedit[ii_currformno].GetItemString(1, "unit__std")
					mitem = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].GetItemString(1, "item_code")

					valStr = genericUtility.getColumnValue( "quantity", dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					mNum1 = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].getitemnumber(1, "quantity")
					mNum2 = Double.parseDouble( distCommon.getConvQuantityFact(mValStr, mValStr1, mitem, mNum1, mNum, conn ).get(1).toString() );
					valueXmlString.append("<quantity__stduom>").append("<![CDATA["+ mNum2 +"]]>").append("</quantity__stduom>");
					//dw_detedit[ii_currformno].SetItem(1,"quantity__stduom", mNum2)
				}
				else if (currentColumn.trim().equals( "unit__rate" ) )
				{ 
					mcode = genericUtility.getColumnValue( currentColumn.trim(), dom ); //dw_detedit[ii_currformno].GetItemString(1, colname)

					//valStr = genericUtility.getColumnValue( "unit__std", dom  ); // commented by mukesh chauhan on 05/05/2020
					unitStd = genericUtility.getColumnValue( "unit__std", dom  ); // changed by mukesh chauhan on 05/05/2020
					//valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();// commented by mukesh chauhan on 05/05/2020

					//mVal1 = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].GetItemString(1, "unit__std")
					mitem = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].GetItemString(1, "item_code")

					valStr = genericUtility.getColumnValue( "rate", dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();

					mrate = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].getitemnumber(1, "rate")
					mconvRtuom = 0;

					//valStr = distCommon.convQtyFactor(Double.toString( mVal1 ), mcode, mitem, mrate, mconvRtuom, conn).get(1).toString(); // commented by mukesh chauhan on 05/05/2020
					valStr = distCommon.convQtyFactor(unitStd, mcode, mitem, mrate, mconvRtuom, conn).get(1).toString(); // changed by mukesh chauhan on 05/05/2020
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();
					mrateStd = Double.parseDouble( valStr );

					valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA["+ mconvRtuom +"]]>").append("</conv__rtuom_stduom>");

					//dw_detedit[ii_currformno].SetItem(1,"conv__rtuom_stduom", mconv_rtuom)
					System.out.println("manohar rate__stduom 6 [" + mrateStd + "]");
					valueXmlString.append("<rate__stduom>").append("<![CDATA["+ mrateStd +"]]>").append("</rate__stduom>");
					//dw_detedit[ii_currformno].SetItem(1,"rate__stduom", mrate_std)
				}
				else if (currentColumn.trim().equals( "conv__rtuom_stduom" ) )
				{  
					valStr = genericUtility.getColumnValue( currentColumn.trim(), dom );
					System.out.println( "valStr :: " + valStr ); 
					valStr = valStr == null || valStr.trim().length() == 0 || "null".equalsIgnoreCase( valStr ) ? "0" : valStr.trim();

					mconvRtuom  = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].getitemnumber(1, colname)

					valStr =  genericUtility.getColumnValue( "unit__rate", dom );
					valStr = valStr == null || valStr.trim().length() == 0 || "null".equalsIgnoreCase( valStr )  ? "" : valStr.trim();					
					String mValStr  =  valStr; //dw_detedit[ii_currformno].GetItemString(1, "unit__rate");

					valStr =  genericUtility.getColumnValue( "unit__std", dom );
					valStr = valStr == null || valStr.trim().length() == 0 || "null".equalsIgnoreCase( valStr )  ? "" : valStr.trim();					
					String mVal1Str = valStr; // dw_detedit[ii_currformno].GetItemString(1, "unit__std")

					mitem = genericUtility.getColumnValue( "item_code", dom ); // dw_detedit[ii_currformno].GetItemString(1, "item_code")

					valStr = genericUtility.getColumnValue( "rate", dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();										

					mrate = Double.parseDouble( valStr ); // dw_detedit[ii_currformno].getitemnumber(1, "rate")

					valStr = distCommon.getConvQuantityFact( mVal1Str, mValStr, mitem, mrate, mconvRtuom, conn ).get(1).toString();
					valStr = valStr == null || valStr.trim().length() == 0 || "null".equalsIgnoreCase( valStr )  ? "" : valStr.trim();
					String mrateStdStr = valStr;
					System.out.println("manohar rate__stduom 7 [" + mrateStdStr + "]");
					valueXmlString.append("<rate__stduom>").append("<![CDATA["+ mrateStdStr +"]]>").append("</rate__stduom>");
					//dw_detedit[ii_currformno].SetItem(1,"rate__stduom", mrate_std);
				}
				else if (currentColumn.trim().equals( "cust_item__ref" ) )
				{
					mitemCode  = genericUtility.getColumnValue( "item_code", dom ); // dw_detedit[ii_currformno].GetItemString(1, "item_code")
					if( mitemCode == null || mitemCode.trim().length() == 0 )
					{
						itemCode = genericUtility.getColumnValue( currentColumn.trim(), dom );
						custCode	= genericUtility.getColumnValue( "cust_code", dom1 ); //dw_header.getitemstring(1,"cust_code")
						if( itemCode != null && itemCode.trim().length() > 0 )
						{
							sqlStr = " select item_code mitem_code, descr mdescr "
									+" from customeritem " 
									+" where cust_code = ? " //:mcust_code
									+"	and item_code__ref = ? "; //trim(:ls_itemcode);

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, custCode );
							pstmt.setString( 2, itemCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mitemCode = rs.getString( "mitem_code" );
								mdescr = rs.getString( "mdescr" );
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;

							valueXmlString.append("<item_code>").append("<![CDATA["+ getAbsString(mitemCode) +"]]>").append("</item_code>");
							setNodeValue( dom, "item_code", mitemCode );

							//tempNode = dom.getElementsByTagName("item_code").item(0);
							//tempNode.getFirstChild().setNodeValue( mitemCode );
							//tempNode = null;

							reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");//chnage Detail1 to 2 by nandkumar gadkari on 23/01/20
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");//chnage Detail1 to 2 by nandkumar gadkari on 23/01/20
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);

							valueXmlString.append("<cust_item_ref_descr>").append("<![CDATA["+ mdescr +"]]>").append("</cust_item_ref_descr>");
						}
					}
				}
				else if (currentColumn.trim().equals( "part_qty" ) || currentColumn.trim().equals( "physical_qty" ) )
				{
					lsRetRepFlag = genericUtility.getColumnValue( "ret_rep_flag", dom ); // dw_detedit[ii_currformno].getitemstring(1, "ret_rep_flag")		
					if( "R".equalsIgnoreCase( lsRetRepFlag ) )
					{
						String lcPartQtyStr  = genericUtility.getColumnValue( "part_qty", dom ); // dw_detedit[ii_currformno].getitemnumber(1, "part_qty")
						String lcPhyQtyStr = genericUtility.getColumnValue( "physical_qty", dom ); //dw_detedit[ii_currformno].getitemnumber(1, "physical_qty")

						lcPartQty = Double.parseDouble( lcPartQtyStr == null || lcPartQtyStr.trim().length() == 0 ? "0" : lcPartQtyStr.trim() );
						lcPhyQty = Double.parseDouble( lcPhyQtyStr == null || lcPhyQtyStr.trim().length() == 0 ? "0" : lcPhyQtyStr.trim() );

						mquantity = lcPhyQty - lcPartQty;
						valueXmlString.append("<quantity>").append("<![CDATA["+ mquantity +"]]>").append("</quantity>");
						setNodeValue( dom, "quantity", Double.toString( mquantity ) );

						reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");//chnage Detail1 to 2 by nandkumar gadkari on 23/01/20
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");//chnage Detail1 to 2 by nandkumar gadkari on 23/01/20
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
				}
				else if ( currentColumn.trim().equals( "gross_weight" ) || currentColumn.trim().equals( "net_weight" )  )
				{
					valStr = genericUtility.getColumnValue( "gross_weight", dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();															

					lcGrossWt  = Double.parseDouble( valStr );//  dw_detedit[ii_currformno].getitemnumber(1, "gross_weight")

					valStr = genericUtility.getColumnValue( "net_weight", dom );
					valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();															
					lcNetWt    = Double.parseDouble( valStr  ); //dw_detedit[ii_currformno].getitemnumber(1, "net_weight")
					lcTareWt   = lcGrossWt - lcNetWt;

					valueXmlString.append("<tare_weight>").append("<![CDATA["+ lcTareWt +"]]>").append("</tare_weight>");
					//dw_detedit[ii_currformno].SetItem(1,"tare_weight", lc_tare_wt)
				}

				// Added By PriyankaC to set Reference Date.  on 14JUNE2018

				else if (currentColumn.trim().equals( "invoice_ref" ))
				{	

					//Nandkumar Gadkari on 11/10/18--------------------------------------------(Start)---------------------------------
					boolean isInvRefDateSet = false; 
					System.out.println(">>>>>>>>>>In IC lot_no:");
					priceList = genericUtility.getColumnValue("price_list", dom1);
					priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);
					fullRet = genericUtility.getColumnValue("full_ret", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom);

					lotNo = genericUtility.getColumnValue("lot_no", dom);

					tranDate = genericUtility.getColumnValue("tran_date", dom1);
					siteCode = genericUtility.getColumnValue("site_code", dom1);
					invoiceId = genericUtility.getColumnValue("invoice_ref", dom);
					unitRate = genericUtility.getColumnValue("unit__rate", dom);
					unitRate = unitRate == null ? "" : unitRate;
					unitStd = genericUtility.getColumnValue("unit__std", dom);
					retReplFlag = genericUtility.getColumnValue("ret_rep_flag", dom);
					String iValStr = genericUtility.getColumnValue("line_no__inv", dom);
					if (iValStr != null && iValStr.indexOf(".") > 0)
					{
						iValStr = iValStr.substring(0,iValStr.indexOf("."));
					}

					sreturnAdjOpt = distCommon.getDisparams("999999", "SRETURN_ADJ_OPT", conn);
					System.out.println("sreturnAdjOpt:::["+sreturnAdjOpt+"]");
					if ("M".equalsIgnoreCase(sreturnAdjOpt))
					{
						orderByStr = " ORDER BY MRH.EFF_COST ";
					}
					else if("E".equalsIgnoreCase(sreturnAdjOpt))
					{
						orderByStr = " ORDER BY MRH.INVOICE_DATE ASC,MRH.INVOICE_ID ASC ";
					}
					else if("L".equalsIgnoreCase(sreturnAdjOpt))
					{
						orderByStr = " ORDER BY MRH.INVOICE_DATE DESC,MRH.INVOICE_ID DESC ";
					}


					if( !"NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
					{
						if(invoiceId != null && invoiceId.trim().length()>0)
						{

							dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);
							curFormItemLotHMap.put("cust_code", genericUtility.getColumnValue("cust_code", dom1));
							curFormItemLotHMap.put("item_code", itemCode);
							curFormItemLotHMap.put("lot_no", lotNo);
							curFormItemLotHMap.put("site_code", siteCode);
							curFormItemLotHMap.put("quantity", genericUtility.getColumnValue("quantity", dom));
							custCode = genericUtility.getColumnValue("cust_code", dom1);
							slineNo = genericUtility.getColumnValue("line_no", dom);
							//lineNo = Integer.parseInt(slineNo);

							/*int size = dokkeyList.size();
							for(int i=0;i<=size-1;i++)
							{*/
							minRateDocKey = dokkeyList.get(0);
							System.out.println("@@@@@@@minRateDocKey .......... test 123 [" + minRateDocKey+"]");
							System.out.println("@@@@@@@count1 .......... test 123");
							/*sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
									+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
									+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
									+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
									+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
									+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
									+ " AND MRH.QUANTITY IS NOT NULL AND MRH.INVOICE_ID= ? "
									+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
									+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0"*/
							sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
									+ "	CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END AS QTY_ADJ, MRH.EFF_COST"
									+ " FROM MIN_RATE_HISTORY MRH "
									+ " WHERE MRH.CUST_CODE = ?"
									+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
									+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
									+ " AND MRH.QUANTITY IS NOT NULL AND MRH.INVOICE_ID= ? "
									+ " AND CASE WHEN MRH.STATUS IS NULL THEN 'A' ELSE MRH.STATUS END <> 'X' "// added by nandkumar gadkari on 30/12/19
									+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,MRH.QUANTITY_ADJ "
									+ orderByStr;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lotNo);
							pstmt.setString(4, siteCode);
							pstmt.setString(5, invoiceId);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								invoiceQty = checkNull(rs.getString("QUANTITY"));
								//adjQty = checkNull(rs.getString("QTY_ADJ"));
								adjQty = rs.getDouble("QTY_ADJ");
								rate = rs.getDouble("EFF_COST");
								curRecordItemLotHMap.put("cust_code", checkNull(rs.getString("CUST_CODE")));
								curRecordItemLotHMap.put("item_code", checkNull(rs.getString("ITEM_CODE")));
								curRecordItemLotHMap.put("lot_no", checkNull(rs.getString("LOT_NO")));
								curRecordItemLotHMap.put("site_code", checkNull(rs.getString("SITE_CODE")));
								curRecordItemLotHMap.put("quantity", invoiceQty);
								System.out.println("@@@@@@@@@@@@ dokkeyList["+dokkeyList+"]");

								invoiceId = getAvailableInvId(dom,dom2, curFormItemLotHMap, curRecordItemLotHMap,invoiceId, minRateDocKey, adjQty);
								tempInvoiceId =invoiceId;	
								if (invoiceId != null && invoiceId.trim().length() > 0)
								{
									break;
								}
							}


							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
							if (rs != null) {
								rs.close();
								rs = null;
							}

							//}
							//Added by Nandkumar Gadkari on 14/09/18----------------[End]---------------------------------------
							sql = "SELECT DOC_KEY,EFF_COST FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
									+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, minRateDocKey);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								minRateDocKey = checkNull(rs.getString("DOC_KEY"));
								rate = rs.getDouble("EFF_COST");
							}
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
							isMinHisRateSet = true;
							if(minRateDocKey != null && minRateDocKey.trim().length() > 0 )
							{

								valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
								valueXmlString.append("<doc_key>").append("<![CDATA[" + minRateDocKey + "]]>").append("</doc_key>");
								setNodeValue( dom, "doc_key",  minRateDocKey  );

							}

							String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
							System.out.println(" dok key : " +docKey);
							if(docKey.trim().length() > 0 || docKey != null )
							{
								System.out.println("inside dok key : " +docKey);
								sqlStr = " select INVOICE_ID , INVOICE_DATE  from MIN_RATE_HISTORY where DOC_KEY = ? " //trim(:ls_itemcode);
										+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, docKey );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									invRefId = checkNull(rs.getString( "INVOICE_ID" ));
									//invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
									invRefDateStr = rs.getTimestamp( "INVOICE_DATE" );
								}
								if( rs != null )
									rs.close();
								rs = null;
								if( pstmt != null )
									pstmt.close();
								pstmt = null;

								if(invRefId != null && invRefId.trim().length() >0)
								{
									valueXmlString.append("<invoice_ref>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_ref>");
								}
								/*if(invRefDate != null && invRefDate.trim().length() >0)
								{
									isInvRefDateSet= true;
									valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
								}*/
								if(invRefDateStr != null )
								{
									valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ sdf.format(invRefDateStr)+"]]>").append("</inv_ref_date>");
								}
							}

							invoiceId = null;
						}
					}

					infoMap = new HashMap();
					iValStr = iValStr == null || iValStr.trim().length() == 0 ? "0" : iValStr.trim();
					lineNoInv = Integer.parseInt( getNumString( iValStr ) );
					iValStr = genericUtility.getColumnValue( "quantity__stduom", dom);
					qtyStdUom = Double.parseDouble( getNumString( iValStr ) );

					lotSl =  genericUtility.getColumnValue("lot_sl", dom);
					locCode = genericUtility.getColumnValue("loc_code", dom);
					lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
					if (lineNoTrace != null )
					{
						if (lineNoTrace != null && lineNoTrace.indexOf(".") > 0)
						{
							lineNoTrace = lineNoTrace.substring(0,lineNoTrace.indexOf("."));
						}

						iLineNoTrace =  Integer.parseInt( lineNoTrace );
						infoMap.put("line_no__invtrace",lineNoTrace);
					}

					quantity = qtyStdUom;
					// Changes by Nandkumar Gadkari on 16/11/18------------Start-----------to set the cost rate from item_lot_info table 
					sql = "select rate "
							+ " from item_lot_info "
							+ " where item_code = ? "
							+ " and lot_no = ? ";

					cntItemLotInfo=0;
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode);
					pstmt.setString( 2, lotNo);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						cntItemLotInfo++;

						costRate = rs.getDouble(1);

					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if(cntItemLotInfo==0)
					{

						infoMap.put("ret_repl_flag",retReplFlag);
						infoMap.put("item_code", itemCode);
						infoMap.put("site_code", siteCode);
						infoMap.put("loc_code",locCode);
						infoMap.put("lot_no", lotNo);
						infoMap.put("lot_sl", lotSl);
						infoMap.put("tran_date", tranDate);
						infoMap.put("invoice_id", invoiceId);
						infoMap.put( "quantity__stduom", new Double( -1 * quantity ) );
						costRate = getCostRate(infoMap, conn);
					}
					infoMap = null;
					// Changes by Nandkumar Gadkari on 16/11/18------------end-----------to set the cost rate from item_lot_info table 
					valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
					setNodeValue( dom, "cost_rate", Double.toString( costRate ) );
					System.out.println( " invoiceId :1: " + invoiceId );
					if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) )
					{
						System.out.println( " retReplFlag :2: " + retReplFlag );
						if (retReplFlag.equals("R"))
						{
							sql = "select sord_no, sord_line_no "
									+ " from invoice_trace "
									+ " where invoice_id = ? "
									+ " and line_no = ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId );
							pstmt.setInt( 2, iLineNoTrace );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								sorder = rs.getString(1);
								sordLineNo = rs.getString(2);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							sql = "select count(1) from invoice_trace "
									+ " where invoice_id = ? "
									+ " and	item_code = ? " 
									+ " and	sord_no = ? "
									+ " and	sord_line_no = ? "
									+ " and	item_code <> item_code__ord " ;

							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId );
							pstmt.setString( 2, itemCode );
							pstmt.setString( 3, sorder );
							pstmt.setString( 4, sordLineNo );
							rs = pstmt.executeQuery(); 
							cnt = 0;
							if( rs.next() )
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							System.out.println( " cnt :1: " + cnt );
							if (cnt > 0)
							{
								sql = "select sum((case when quantity is null then 0 else quantity end) * " 
										+ " (case when rate is null then 0 else rate end)), "
										+ " sum(case when quantity is null then 0 else quantity end) from invoice_trace "
										+ " where invoice_id = ? "
										+ " and	item_code = ? " 
										+ " and	sord_no = ? "
										+ " and	sord_line_no = ? ";

								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId );
								pstmt.setString( 2, itemCode );
								pstmt.setString( 3, sorder );
								pstmt.setString( 4, sordLineNo );
								rs = pstmt.executeQuery(); 
								cnt = 0;
								if( rs.next() )
								{
									amount = rs.getDouble(1);
									quantity = rs.getDouble(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								totFreeCost = 0;

								sql = "select a.item_code, sum(a.quantity), a.item_code__ord "
										+ " from sorditem a "
										+ " where a.sale_order = ? "
										+ " and a.line_no = ? "
										+ " and a.item_code <> ? "
										+ " and nature = 'F' "
										+ " and not exists (select b.item_code  from sorditem b "
										+ " where b.sale_order = a.sale_order "
										+ " and b.line_no= a.line_no "
										+ " and a.item_code = b.item_code "
										+ " and nature = 'C') "
										+ " group by a.item_code, a.item_code__ord ";

								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, sorder);
								pstmt.setString( 2, sordLineNo);
								pstmt.setString( 3, itemCode);
								rs = pstmt.executeQuery(); 
								cnt = 0;
								while ( rs.next() )
								{
									freeItemCode = rs.getString(1);
									freeQty = rs.getDouble(2);
									itemCodeOrd = rs.getString(3);			

									effCostFree = 0;
									sql = "select count(1), avg(eff_cost) "
											+ " from bomdet " 
											+ " where bom_code = ? "
											+ " and item_code = ? "
											+ " and nature = 'F' ";
									pstmt1 = conn.prepareStatement( sql );
									pstmt1.setString( 1, itemCodeOrd);
									pstmt1.setString( 2, itemCode);
									rs1 = pstmt1.executeQuery(); 
									cnt = 0;
									if( rs1.next() )
									{
										cnt =  rs1.getInt(1);
										effCostFree =  rs1.getDouble(2);
										if (cnt > 0 )
										{
											totFreeCost +=  freeQty * effCostFree;
											totFreeCost = getRequiredDecimal( totFreeCost, 4 );
										}
									}
									rs1.close();
									pstmt1.close();
									pstmt1 = null;
									rs1 = null;
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								amount = amount - totFreeCost;

								sql = "select sum(case when a.drcr_flag = 'C' then " 
										+ " ((case when b.drcr_amt is null then 0 else b.drcr_amt end) * -1) else 0 End) "
										+ " from drcr_rcp a, drcr_rdet b "
										+ " where  a.tran_id = b.tran_id "
										+ " and b.invoice_id  = ? "
										+ " and b.item_code	= ? "
										+ " and  a.sreturn_no is null" ;	
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								pstmt.setString( 2, itemCode);
								rs = pstmt.executeQuery(); 
								cnt = 0;
								if ( rs.next() )
								{
									amtDrCr = rs.getDouble(1);
									rate = (amount + amtDrCr) / quantity; 
									System.out.println( "rate calc db LOt NO :: " + rate );
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}	
							else	
							{
								System.out.println( "else 1 " );
								sql = "select rate, discount, rate__clg "
										+ " from invoice_trace "
										+ " where invoice_id = ? "
										+ " and line_no = ? ";
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								pstmt.setInt( 2, iLineNoTrace);
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									rate = rs.getDouble(1);
									discount = rs.getDouble(2);
									rateClg = rs.getDouble(3);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
								if (lineNoTrace != null )
								{
									if (lineNoTrace != null && lineNoTrace.indexOf(".") > 0)
									{
										lineNoTrace = lineNoTrace.substring(0,lineNoTrace.indexOf("."));
									}

									iLineNoTrace =  Integer.parseInt( lineNoTrace );
								}


								sql = "select sum((case when quantity is null then 0 else quantity end) * "
										+ " (case when rate is null then 0 else rate end)), "
										+ " sum(case when quantity is null then 0 else quantity end) "
										+ " from invoice_trace "
										+ " where  invoice_id   = ? "
										+ " and	line_no = ? ";

								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);

								pstmt.setInt( 2, iLineNoTrace );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									amount = rs.getDouble(1);
									quantity = rs.getDouble(2);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;

								sql = "select sum(case when a.drcr_flag = 'C' then " 
										+ " ((case when b.drcr_amt is null then 0 else b.drcr_amt end) * -1) else 0 End) "
										+ " from drcr_rcp a, drcr_rdet b "
										+ " where a.tran_id = b.tran_id "
										+ " and b.invoice_id = ? "
										+ " and b.line_no__inv = ? "
										+ " and a.sreturn_no is null" ;
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId);
								pstmt.setInt( 2, lineNoInv );
								rs = pstmt.executeQuery(); 
								amtDrCr = 0;
								if( rs.next() )
								{

									amtDrCr = rs.getDouble(1);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								System.out.println( "amount lot :1: " + amount );
								System.out.println( "quantity lot :1: " + quantity );
								System.out.println( "amtDrCr :1: " + amtDrCr );
								if(quantity > 0)// Added by mukesh chauhan on 01/06/2020 Start
								{
									rate = (amount + amtDrCr) / quantity;
									if(rate > 0)
									{
										rate = getRequiredDecimal( rate, 4 );
									}
								}//END
								System.out.println( "rate calc LOt NO :: " + rate );
							}
						}
						else
						{
							System.out.println( "priceList :2 : " + priceList );
							if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
							{
								tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
								itemCode = itemCode == null ? "" : itemCode;
								lotNo = lotNo == null ? "" : lotNo;

								rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
								System.out.println( "rate pic rate LOt NO :: " + rate );
							}
						}
					}
					else
					{
						System.out.println( "retReplFlag :2 : " + retReplFlag );

						if (retReplFlag != null && retReplFlag.equals("R") && !isMinHisRateSet)
						{	
							System.out.println(">>>>>>>>>>>>>In lot_no before getMinRate @ 3313:");
							StringBuffer minRateBuff = getMinRate( dom, dom1, "lot_no", valueXmlString, conn);
							System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );

							String rateValStr = getTagValue(  minRateBuff.toString(), "rate" );

							System.out.println( "rateValStr LOt NO :: " + rateValStr );
							rate = getRequiredDecimal( Double.parseDouble( getNumString( rateValStr ) ), 4 );

						}
						System.out.println( "rate before If :1: " + rate );
						if (rate <= 0)
						{
							rate = 0;
							if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
							{
								tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
								itemCode = itemCode == null ? "" : itemCode;
								lotNo = lotNo == null ? "" : lotNo;
								rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
								System.out.println( "rate in  If :1: " + rate );
							}
						}
					}

					pickLowerRate = distCommon.getDisparams("999999","PICK_LOWER_RATE", conn);
					if (pickLowerRate.equals("NULLFOUND"))
					{
						pickLowerRate = "N";
					}
					if ( pickLowerRate != null && pickLowerRate.equals("Y"))
					{
						if (priceList !=null && priceList.trim().length() >  0)
						{
							tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;							
							itemCode = itemCode == null ? "" : itemCode;
							lotNo = lotNo == null ? "" : lotNo;
							priceListRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D", quantity, conn);
							System.out.println( "priceListRate LOt NO :: " + priceListRate );
							if (priceListRate < rate && priceListRate > 0 )
							{
								rate = priceListRate;
								System.out.println( "rate priceListRate < rate && priceListRate > 0 :1: " + rate );
							}
						}
					}
					System.out.println( "Rate LOt NO :: " + rate );
					valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
					setNodeValue( dom, "rate", rate );

					rateClg = sRateClg;
					if (rateClg <= 0 )
					{
						if ( priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
						{
							tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
							itemCode = itemCode == null ? "" : itemCode;
							lotNo = lotNo == null ? "" : lotNo;								
							rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode,lotNo,"L", quantity, conn);
							if (rateClg == -1)
							{
								rateClg = 0 ;
							}
						}
						if (rateClg == 0 )
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", rate );
						}
						else
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", rateClg );
						}
					}
					unit = genericUtility.getColumnValue( "unit", dom );
					if ( unitRate != null && unit != null && unit.trim().equals( unitRate.trim() ) )
					{
						System.out.println("manohar rate__stduom 5 [" + rate + "]");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate + "]]>").append("</rate__stduom>");
						setNodeValue( dom, "rate__stduom", rate );
						valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");// column protected by nandkumar gadkari on 29/01/19
						setNodeValue( dom, "conv__rtuom_stduom", "1" );
					}
					else if ( unitRate != null && unit != null && !unit.trim().equals( unitRate.trim() ) )
					{	
						System.out.println( "unit :: " + unit );
						System.out.println( "unitRate :: " + unitRate );
						System.out.println( "itemCode :: " + itemCode );
						System.out.println( "rate :: " + rate );
						System.out.println( "fact :: " + fact );
						convAr = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, (double) fact,  conn);
						convFact = Double.parseDouble( convAr.get(0).toString() );
						rateStd = Double.parseDouble( convAr.get(1).toString() );
						System.out.println("manohar rate__stduom 6 [" + rateStd + "]");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rateStd + "]]>").append("</rate__stduom>");
						setNodeValue( dom, "rate__stduom", rateStd );
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + convFact + "]]>").append("</conv__rtuom_stduom>");
						setNodeValue( dom, "conv__rtuom_stduom", convFact );
					}
					sQuantity = Double.parseDouble( getNumString( genericUtility.getColumnValue("quantity", dom) ) );
					quantity = sQuantity;

					valueXmlString.append("<mrp_value>").append("<![CDATA[" + quantity * rateClg + "]]>").append("</mrp_value>");
					setNodeValue( dom, "mrp_value", quantity * rateClg );
					System.out.println("<<<<<<<<<<DISCOUNT>>>>>>>>>>= "+ discount);


					System.out.println(">>>>>>>>>>>>>In lot_no before getMinRate @ 3421:");

					String lslotsl="";
					System.out.println("site_code is:"+siteCode);
					System.out.println("lotNo is:"+lotNo);
					System.out.println("locCode is:"+locCode);
					System.out.println("itemCode is:"+itemCode);
					sql="select lot_sl from stock where site_code = ? and " +
							"lot_no = ? and item_code = ?";
					pstmt= conn.prepareStatement( sql );//,,,
					pstmt.setString( 1, siteCode);
					pstmt.setString( 2, lotNo );
					pstmt.setString( 3, itemCode );
					rs = pstmt.executeQuery(); 

					if( rs.next() )
					{

						lslotsl = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					System.out.println("*********Lot sl is:"+lslotsl);
					if (lslotsl == null || lslotsl.trim().length() == 0)
					{
						System.out.println("lslotsl is null in lot_no itemChanged"+lslotsl);
						String lsSretLotsl="";
						lsSretLotsl = distCommon.getDisparams( "999999", "SRETURN_DEFAULT_LOTSL", conn );
						System.out.println("lsSretLotsl ["+lsSretLotsl+"] in lot_no Disparam Value");
						if(lsSretLotsl == null || lsSretLotsl.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(lsSretLotsl))
						{
							valueXmlString.append("<lot_sl>").append("2S").append("</lot_sl>");
						}
						else
						{
							System.out.println("lsSretLotsl["+lsSretLotsl+"]");
							valueXmlString.append("<lot_sl>").append("<![CDATA[" + lsSretLotsl + "]]>").append("</lot_sl>");
						}
					}
					else
					{
						valueXmlString.append("<lot_sl>").append("<![CDATA[" + lslotsl + "]]>").append("</lot_sl>");
					}

					StringBuffer minRateBuff = null;
					if(!isMinHisRateSet)
					{
						minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ); 
						System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );
						valueXmlString = minRateBuff;
					}



					System.out.println("lslotsl["+lslotsl+"]");
					setNodeValue( dom, "lot_sl",lslotsl);
					reStr = itemChanged(dom, dom1, dom2, objContext, "lot_sl", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);


					//Nandkumar Gadkari on 11/10/18--------------------------------------------(end)---------------------------------






					System.out.println("inside invoice ref : " );
					String invRef = checkNull(genericUtility.getColumnValue( "invoice_ref", dom ));
					invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
					if((invoiceId == null || "null".equals(invoiceId)|| invoiceId.trim().length() == 0 )&& (invRef.trim().length() > 0 || invRef != null ) && !isInvRefDateSet)
					{
						sqlStr = "select tran_date from invoice where INVOICE_ID = ? "; //trim(:ls_itemcode);
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, invRef );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							tranDate = checkNull(sdf.format(rs.getTimestamp("tran_date")));
						}

						System.out.println("tranDate : " +tranDate);
						if( rs != null )
							rs.close();
						rs = null;
						if( pstmt != null )
							pstmt.close();
						pstmt = null;

						if((tranDate != null)  && tranDate.trim().length() >0 )
						{
							valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ checkNullTrim(tranDate)+"]]>").append("</inv_ref_date>");
						}
						else
						{
							valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
						}
					}
					/*	else
					{
						String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
						if(docKey.trim().length() > 0 || docKey != null )
						{
							System.out.println("inside dok key : " +docKey);
							sqlStr = " select INVOICE_ID , INVOICE_DATE  from MIN_RATE_HISTORY where DOC_KEY = ? "; //trim(:ls_itemcode);
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, docKey );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								invRefId = checkNull(rs.getString( "INVOICE_ID" ));
								invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;

							valueXmlString.append("<invoice_ref>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_ref>");
							if(invRefDate != null && invRefDate.trim().length() >0)
							{
								valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
							}
						}
					}*/
				}
				// Added By PriyankaC on 14JUNE2018 [END]

				valueXmlString.append("</Detail2>");
				break;

			case 3:

				parentNodeList = dom1.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				winName = getObjName(parentNode);

				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail3>");
				childNodeListLength = childNodeList.getLength();

				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn.trim()))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength && !childNodeName.equals(currentColumn));

				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
				}

				valueXmlString.append("</Detail3>");
				break;
			}//end of switch
			valueXmlString.append("</Root>");
			distCommon = null;	
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (distCommon != null) { distCommon = null;}
				if(conn!=null)
				{
					if(rs != null)rs.close();
					rs = null;
					if(pstmt != null)pstmt.close();
					pstmt = null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		System.out.println("currentColumn:::::"+currentColumn);
		System.out.println("valueXmlString:::::"+valueXmlString.toString());
		return ( valueXmlString == null || valueXmlString.toString().trim().length() == 0 ? "" : valueXmlString.toString() );
	}//END OF ITEMCHANGE

	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("wfvaldata called for sales return");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null , rs1 = null;
		String sql = "" ,sql1 = "";
		String userId = "";
		String effFromStr = null;
		String validUptoStr = null;
		String itemSer = "";
		int cnt = 0,cnt1 = 0;
		int currentFormNo=0;
		int childNodeListLength;
		ConnDriver connDriver = new ConnDriver();
		int cc, xx, initialField = 1, lastField = 1, fldcase = 0,madvperc, liLineNo; 
		String fldname = "", mVal = null, mVal1 = null, fldnm = null, ItemLocty = null, LocTy= null;
		String itemCode = null, custCode = null, mstan = null, mstan1 = null, mreasCode = null, siteCodeMfg = null, lsRefSer = null, lsRefNo = null;
		String mcode = null, siteCode = null, mactive = null, mitemSerHdr = null, lsTranId = null;
		String mschemeCd = null, msiteCd = null, mstateCd = null, mtype = null, mcurrCode = null, lsStatus = null, lsSordNo = null, lsLineNoSord = null;
		String lsFlag = null, lsSer = null, lsCust = null, lsInvoiceId = null, lsLotNo = null, lsFullRet = null, lsLotSl = null, lsPricelist = null;
		// Variables added by Mahesh Saggam [Start] on 05/07/2019
		String convQtyStduomStr = "", qtyStr = "", qtyStdStr = "";
		double convQtyStduom=0, dqty = 0, dqtyStduom = 0;
		// Variables added by Mahesh Saggam [End] on 05/07/2019
		java.sql.Timestamp mdate1 = null, mdate2 = null, mtrandate = null, morderDt = null, mdate = null, mexpDate = null, mchkDate = null, ldTaxdate = null, ldInvoiceDate = null;
		double mqty = 0.0, mintQty = 0.0, mexchRate = 0.0, lcSrqtyLot = 0.0, lcInvqtyLot = 0.0,lcInvqty = 0.0;
		double ldCurrQty = 0.0, lcSrqtyPrev = 0.0,lcSrqty = 0.0, lcQty = 0.0, lcQtystd = 0.0,lcAdjAmt = 0.0,lcBalAmt = 0.0,lcAmount = 0.0,lcSumAdj = 0.0;
		double lcSrqtyPrevLot = 0.0 ,lcInvtraceAmt = 0.0 ,lcAllocQty = 0.0;
		double lcRate = 0.0, lcInvRate = 0.0, lcRateInv = 0.0,lcPreRate = 0.0;
		int mcount , mitemCnt = 0, mcnt, mlineNo, llLineNo = 0, llCount = 0,llLineNoInv = 0, llLineNoInvtrace;
		long llCurLineno;	
		double mminShlife = 0.0;
		String mitemCode = null, lsOthSer = null, lsTrackShelfLife = null, lsSuppSour = null,lsItemSerCrpolicy = null, lsItemSerCrpolicyHdr = null,lsInvoiceLn = null;
		String blackListedYn  = null,lsStopBusiness = null,lsItemser = null,lsRetopt = null,lsLoccode = null, lsSql = null, lsReturnConfirm = null;
		String lsInvoiceItem = null,lsItemParnt = null,lsItemParntInv = null,lsItemSer = null,lsAdjMiscCrn = null;
		String lsRetRef = null,lsRetOpt = null, sFieldNo = "0", lsAvilable = null, lsQcreqd = null, lsSretinvqty = null, lsCurtranId = null;
		long llCnt, llNoInvtrace, llRowcnt,llRow,llCurlineNoInvtrac;
		double lcVarperc = 0.0, lcPhysicalqty = 0.0, lcStkqty = 0.0;
		double mminShelfLife = 0.0;
		String strSch = null;
		//String siteCode = null;
		DistCommon distCommon = null;
		distCommon = new DistCommon();	
		String sqlStr = null;
		String objName = null,llLineNoInvtraceStr = "", saleOrder = "",lineNoSord = "",spaces = "";
		double lsVarperc = 0.0, amt = 0.0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		ITMDBAccessEJB dbEjb = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		String errorType = "";
		String locCode = "", faciLocCode = "", faciSiteCode = "";
		String  tranId = "";
		//String  invoiceId = ""; //Added By PriyankaC on 15JAN18.
		String retOpt = "";
		String	channelPartner="";
		//Added By PriyankaC on 27MAY2019.
		String fullRet = "" ,ldCurrQtyStr ="", llLineNoStr ="",convQty ="" ,CurrQty ="",docKeyS="",formNo="";
		double varQuatity = 0.0,CurrQtydtl= 0.0, convCurrQty=0.0;
		String dockey="";//addded by nandkumar gadkari on 05/06/2020
		double minRate=0;//addded by nandkumar gadkari on 05/06/2020
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;

			dbEjb = new ITMDBAccessEJB();

			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				objName = getObjName(parentNode);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					// Changed by Manish on 22/12/15 for checking Zero invoice for DDUK[start]
					/*if (childNodeName.equalsIgnoreCase("invoice_id"))
						{
							String invoiceId = getAbsString(genericUtility
									.getColumnValue("invoice_id", dom));
							sql = "SELECT NET_AMT FROM INVOICE_TRACE WHERE INVOICE_ID = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, invoiceId);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								amt = rs.getDouble(1);
								if (amt <= 0) {
									System.out.println("Invoice of Zero amount");

					 * errString =
					 * dbEjb.getErrorString("","VTZEROAMT"
					 * ,"","",conn); isError = true; return
					 * errString;


									errList.add("VTZEROAMT");
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						}*/ //commented by nandkumar gadkari on 10/12/18
					// Changed by Manish on 22/12/15 for checking Zero invoice for DDUK[end]

					if( childNodeName.equalsIgnoreCase( "tran_date" ) )
					{ 
						String mdateStr = genericUtility.getColumnValue( "tran_date", dom );// dw_edit.getitemdatetime(1,fldname)
						// 14/10/13 manoharan check for null tran_date
						if (mdateStr == null) 
						{
							errList.add( "VTTRNDTNUL" );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							mdate1 = Timestamp.valueOf(genericUtility.getValidDateString( mdateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//getDateInAppFormat( mdateStr ); 
							siteCode = genericUtility.getColumnValue( "site_code", dom ); //dw_edit.getitemstring(1,"site_code")
							lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom ); //dw_edit.getitemstring(1,"invoice_id")
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = SysCommon.nfCheckPeriod( "SAL", mdate1, siteCode, conn ); 
							errCode=finCommon.nfCheckPeriod("SAL", mdate1, siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							if( errCode != null && errCode.trim().length() > 0 )
							{
								//errList.add( "VMPRD1" );
								//changes by Dadaso pawar on 28-APR-14 [Start]
								errList.add(errCode.trim());
								//changes by Dadaso pawar on 28-APR-14 [END]
								errFields.add( childNodeName.toLowerCase() );
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;				
							}
							else
							{
								if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
								{
									sqlStr = " select tran_date ld_invoice_date "
											+"	from invoice "
											+"	where invoice_id = ? ";

									pstmt = conn.prepareStatement( sqlStr );

									pstmt.setString( 1, lsInvoiceId );

									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										ldInvoiceDate = rs.getTimestamp( "ld_invoice_date" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( ldInvoiceDate != null && mdate1 != null && mdate1.before( ldInvoiceDate ) )
									{
										errCode = "VTISRDT";
										//errString = getErrorString( childNodeName, errCode, userId ); 
										//break;
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "cust_code" ) ||
							childNodeName.equalsIgnoreCase( "cust_code__bill" ) ||
							childNodeName.equalsIgnoreCase( "cust_code__dlv" ) ||
							childNodeName.equalsIgnoreCase( "cust_code__bil" ) )
					{
						String	invoiceId = genericUtility.getColumnValue( "invoice_id", dom );
						errCode = null;
						mVal = genericUtility.getColumnValue( childNodeName.trim(), dom ); //dw_edit.GetItemString(dw_edit.GetRow(),fldname)           
						System.out.println( "mVal :: " + mVal + "  :childNodeName: " + childNodeName );
						siteCode = genericUtility.getColumnValue( "site_code", dom ); //dw_edit.getitemstring(1,"site_code")
						lsItemser = genericUtility.getColumnValue( "item_ser", dom ); //dw_edit.getItemString(1,"item_ser")	


						// Added by Mahesh Saggam on 18-June-2019 [Start]
						sqlStr = "select channel_partner from site_customer where site_code = ? and cust_code = ?";
						pstmt = conn.prepareStatement(sqlStr);
						pstmt.setString(1, siteCode);
						pstmt.setString( 2, mVal );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							channelPartner = rs.getString( "channel_partner" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(channelPartner == null || channelPartner.trim().length() == 0)
						{
							//Added By PriyankaC on 15JAN18 [START].
							System.out.println( "invoiceId in side custCode :: " + invoiceId );
							sqlStr = "select channel_partner from customer where cust_code = ? ";
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								channelPartner = rs.getString( "channel_partner" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//Added By PriyankaC on 15JAN18.[END.]
						}
						if( "Y".equalsIgnoreCase( channelPartner ) )
						{
							System.out.println( "invoiceId :: " + invoiceId +"channelPartner :: "+channelPartner);
							errCode = "VTPRCRINSR";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						// Added by Mahesh Saggam on 18-June-2019 [End]

						sqlStr = " select black_listed black_listed_yn "
								+"	from customer_series "
								+" where cust_code= ? " //:mval
								+"	and item_ser = ? "; //:ls_itemser;

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mVal );
						pstmt.setString( 2, lsItemser );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							blackListedYn = rs.getString( "black_listed_yn" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( blackListedYn != null && "Y".equalsIgnoreCase( blackListedYn ) )
						{
							errCode = "VTCUSTCD3";
							//								errString = getErrorString( childNodeName, errCode, userId ); 
							//								break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}

						System.out.println( "siteCode :: " + siteCode );
						System.out.println( "mVal :: " + mVal );
						System.out.println( "conn :: " + (conn == null) );
						errCode = isCustomer( siteCode, mVal, "SAL", conn );
						if( errCode != null && errCode.trim().length() > 0 )
						{
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						sqlStr = " select stop_business ls_stop_business "
								+"	from customer "
								+" where cust_code = ? "; //:mVal;

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mVal );


						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lsStopBusiness = rs.getString( "ls_stop_business" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( lsStopBusiness != null && "Y".equalsIgnoreCase( lsStopBusiness ) )
						{
							errCode = "VTICC";

							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}

					}
					if( childNodeName.equalsIgnoreCase( "invoice_id" ) )
					{ 
						mVal = null;
						mVal = genericUtility.getColumnValue( "invoice_id", dom ); //dw_edit.GetItemString(dw_edit.GetRow(),fldname) 
						siteCode = genericUtility.getColumnValue( "site_code", dom );//added by nandkumar gadkari on 25/12/18
						if( mVal != null && mVal.trim().length() > 0 )
						{
							sqlStr = " select Count(*) cnt from invoice where invoice_id = ? and site_code = ? "; //:mVal;// site_code added in sql by nandkumar gadkari on 25/12/18

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );
							pstmt.setString( 2, siteCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTSRET1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					//Added By PriyankaC on 28-MAY-2019 [Start]
					if( childNodeName.equalsIgnoreCase( "full_ret" ) )
					{ 
						mVal = null;
						mVal = genericUtility.getColumnValue( "invoice_id", dom ); 
						fullRet = genericUtility.getColumnValue( "full_ret", dom );
						tranId = genericUtility.getColumnValue( "tran_id", dom );

						if(tranId == null || tranId.trim().length()== 0)
						{
							tranId = "@@@@@@@@@@" ;
						}
						if((mVal != null && mVal.trim().length() > 0) && "Y".equals(fullRet))
						{
							System.out.println("Value of mVal :" +mVal +"fullRet :" +fullRet);

							//sqlStr = " select Count(*) cnt from sreturn  where  invoice_id = ? and  tran_id <> ?  ";    //commented by manish mhatre on 24-aug-20
							sqlStr="select Count(*) cnt from sreturn sr,sreturndet srdet  where sr.tran_id = srdet.tran_id and  sr.invoice_id = ? and  sr.tran_id <> ? ";   //added by manish mhatre on 21-aug-20[For allow the full sale return when item not present in detail]
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							pstmt.setString( 2, tranId );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt > 0 )
							{
								errCode = "VTSRAINV";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					//Added By Priyankac on 28-MAY-2019. [END]
					if( childNodeName.equalsIgnoreCase( "site_code" ) )
					{
						mVal = genericUtility.getColumnValue( childNodeName, dom ); //dw_edit.GetItemString(dw_edit.GetRow(),fldname)           

						sqlStr = "Select Count(*) cnt from site where site_code = ? ";

						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mVal );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VTSITE1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}

						if( objName.equalsIgnoreCase( "w_salesreturn_repl" ) )
						{
							lsTranId = genericUtility.getColumnValue( "tran_id", dom ); 
							if (lsTranId != null && lsTranId.trim().length() > 0 && !"null".equalsIgnoreCase( lsTranId.trim() ) )
							{
								sqlStr = "select ( case when return_confirm is null then 'N'  else  return_confirm end ) ls_return_confirm from sreturn "
										+" where  tran_id = ? ";

								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, lsTranId );

								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsReturnConfirm = rs.getString( "ls_return_confirm" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( lsReturnConfirm != null && "N".equalsIgnoreCase( lsReturnConfirm ) )
								{
									errCode = "VTSRETUNC";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );

								}
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "sales_pers" ) )
					{
						mVal = genericUtility.getColumnValue( "sales_pers", dom );           
						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = "Select Count(*) cnt from sales_pers where sales_pers = ? ";
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VMSLPERS1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}


						}
					}	 
					if( childNodeName.equalsIgnoreCase( "proj_code" ) )
					{
						mVal = genericUtility.getColumnValue( "proj_code", dom );           
						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = "Select Count(*) cnt from project where proj_code = ? ";
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTPROJCD1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}	  
					if( childNodeName.equalsIgnoreCase( "item_ser" ) )
					{
						mVal = genericUtility.getColumnValue( "item_ser", dom );
						sqlStr = " Select Count(*) cnt from itemser where item_ser = ? ";
						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mVal );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VTITMSER1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					if( childNodeName.equalsIgnoreCase( "curr_code" ) || childNodeName.equalsIgnoreCase( "curr_code__bc" ) )
					{

						mVal = genericUtility.getColumnValue( childNodeName.toLowerCase(), dom );
						sqlStr = " Select Count(*) cnt from currency where curr_code = ? ";
						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mVal );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VMCURRCD1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
					}				  
					if( childNodeName.equalsIgnoreCase( "tran_code" ) )
					{ 
						mVal = genericUtility.getColumnValue( "tran_code", dom );
						sqlStr = " Select Count(*) cnt from transporter where tran_code = ? ";
						pstmt = conn.prepareStatement( sqlStr );

						pstmt.setString( 1, mVal );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VMTRAN1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}			
					}	 
					if( childNodeName.equalsIgnoreCase( "price_list" ) )
					{
						mVal = genericUtility.getColumnValue( "price_list", dom );							
						retOpt = genericUtility.getColumnValue("ret_opt",dom1);							
						if(!"C".equalsIgnoreCase(retOpt))
						{	
							sqlStr = " Select Count(*) cnt from pricelist where price_list = ? ";
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTPLIST1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "ret_opt" ) )
					{
						mVal = genericUtility.getColumnValue( "ret_opt", dom );
						if( "D".equalsIgnoreCase( mVal ) )
						{
							lsRetRef = genericUtility.getColumnValue( "ret_ref", dom );
							if( lsRetRef == null )
							{
								errCode = "VTRETREF";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;	
								errList.add( errCode );									
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "ret_ref" ) )
					{ 
						mVal = getAbsString( genericUtility.getColumnValue( "ret_ref", dom ) );
						lsRetOpt = getAbsString( genericUtility.getColumnValue( "ret_opt", dom ) );
						if( "D".equalsIgnoreCase( lsRetOpt ) )
						{
							custCode = genericUtility.getColumnValue( "cust_code", dom1 );

							sqlStr = " Select count(*) cnt from sreturn "
									+"	where tran_id = ? "
									+"	and confirmed = 'Y' "
									+"	and ret_opt = 'C' "
									+"	and	cust_code = ? ";
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );
							pstmt.setString( 2, custCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTRETREF1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}	
							cnt = 0;
							sqlStr = " select count(*) ll_cnt "
									+"	from sreturn "
									+"	where confirmed = 'Y' "
									+"	and ret_ref = ? ";
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "ll_cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt > 0 )
							{
								errCode = "VTRETREF2";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}	
						}							
					}
					if( childNodeName.equalsIgnoreCase( "adj_misc_crn" ) )
					{
						mVal = genericUtility.getColumnValue( "adj_misc_crn", dom );

						if( mVal != null && ( "MI".equalsIgnoreCase( mVal.trim() ) || "AI".equalsIgnoreCase( mVal.trim() ) ) )
						{
							lsItemSer = genericUtility.getColumnValue( "item_ser", dom );
							if( lsItemSer == null || lsItemSer.trim().length() == 0 )
							{
								errCode = "VTITMSER1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "reas_code" ) )
					{
						mVal = getAbsString( genericUtility.getColumnValue( "reas_code", dom ) );
						mcount = 0;
						System.out.println( "mVal :Reas Code: " + mVal );
						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = " select count(1) mcount from sreturn_reason  where reason_code = ? ";
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mcount = rs.getInt( "mcount" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println( "mcount :Reas Code: " + mcount );
							if( mcount == 0 )
							{
								errCode = "VUREASON";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );									
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						else
						{
							errCode = "VTSRREASON";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );									
							errFields.add( childNodeName.toLowerCase() );

						}
					}
					//site_code dlv validation added by nandkumar gadkari on 08/07/19
					if( childNodeName.equalsIgnoreCase( "site_code__dlv" ) )
					{
						mVal = genericUtility.getColumnValue( childNodeName, dom );           
						if(mVal !=null && mVal.trim().length()>0) 
						{
							sqlStr = "Select Count(*) cnt from site where site_code = ? ";

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTSITE1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					//Pavan R 17sept19 start[to validate tax environment]
					if( childNodeName.equalsIgnoreCase( "tax_env" ) )
					{ 
						mVal = genericUtility.getColumnValue( "tax_env", dom );						  
						String mtrandateStr = genericUtility.getColumnValue( "tran_date", dom );
						mtrandate = Timestamp.valueOf(genericUtility.getValidDateString( mtrandateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = " Select count(*) cnt from taxenv where tax_env = ? ";
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if( cnt == 0 )
							{
								errCode = "VTTENV1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}							 
							else
							{																
								errCode = distCommon.getCheckTaxEnvStatus( mVal, mtrandate, "S", conn );								
								if( errCode != null && errCode.trim().length() > 0 )
								{
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					}
					//Pavan R 17sept19 end[to validate tax environment]
				}//end for	
				break;
			case 2 :
				System.out.println( "Detail 2 Validation called " );
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				String itemValue = "",lineNo = "",lineValue= "",updateFlag = "", invTrace = " ";
				int lineNoInt = 0,lineValueInt = 0;
				//NodeList itemNodeList = null,lineNoList = null,detail2List = null,childDetilList = null;

				//Node itemNode = null,lineNoNode = null,detailNode = null,chidDetailNode = null;

				NodeList itemNodeList = null,lineNoList = null,detail2List = null,childDetilList = null; //Variables added by Mahesh Saggam on 17-June-2019
				Node itemNode = null,lineNoNode = null,detailNode = null,chidDetailNode = null;  //Variables added by Mahesh Saggam on 17-June-2019
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if( childNodeName.equalsIgnoreCase( "invoice_id" ) )
					{
						mVal = genericUtility.getColumnValue( "invoice_id", dom );
						if ( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{	
							custCode = genericUtility.getColumnValue( "cust_code", dom1 );
							sqlStr = " select count(*) ll_count "
									+"		from invoice "
									+"	where invoice_id = ? " //:mVal
									+"		and cust_code = ? "; //:mcust_code ;
							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mVal );
							pstmt.setString( 2, custCode );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								llCount = rs.getInt( "ll_count" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( llCount == 0 )
							{
								errCode = "VTINVCUST";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;					
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "line_no__invtrace" ) )
					{		
						String mlineNoStr = getNumString( genericUtility.getColumnValue( "line_no__invtrace", dom ) );
						if (mlineNoStr != null && mlineNoStr.indexOf(".") > 0)
						{
							mlineNoStr = mlineNoStr.substring(0,mlineNoStr.indexOf("."));
						}
						mlineNoStr = mlineNoStr == null || mlineNoStr.trim().length() == 0 ? "0" : mlineNoStr.trim();
						mlineNo = Integer.parseInt( mlineNoStr.trim() );  
						mVal1 = genericUtility.getColumnValue( "invoice_id", dom );
						if ( mlineNo != 0 )
						{
							if( mlineNoStr != null && mlineNoStr.trim().length() > 0 && !"null".equalsIgnoreCase( mlineNoStr.trim() ) )
							{
								sqlStr = " select count(*) cnt from invoice_trace " 
										+"	 where invoice_id = ? " //:mval1 
										+" 	and line_no = ? ";//:mline_no;

								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, mVal1 );
								pstmt.setInt( 2, mlineNo );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
									errCode = "VTSRET1";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;					
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}

						// Added by Mahesh Saggam on 17-June-2019 [Start]
						detail2List = dom2.getElementsByTagName("Detail2");
						invTrace = genericUtility.getColumnValue("line_no__invtrace",dom);
						lineNo = genericUtility.getColumnValue("line_no",dom);
						//System.out.println("Detail 2 List "+detail2List);

						if(lineNo != null && lineNo.trim().length() > 0)
						{
							lineNoInt = Integer.parseInt(lineNo.trim());
						}
						for(int t =0; t < detail2List.getLength(); t++ )
						{
							detailNode = detail2List.item(t);
							childDetilList = detailNode.getChildNodes();
							for(int p =0; p < childDetilList.getLength(); p++ )
							{
								chidDetailNode = childDetilList.item(p);
								//System.out.println("current child node>>>>>>>>>> " + chidDetailNode.getNodeName() );

								if(chidDetailNode.getNodeName().equalsIgnoreCase("line_no") )
								{
									//System.out.println("line node found >>>>>" + chidDetailNode.getNodeName());
									if(chidDetailNode.getFirstChild() != null )
									{
										lineValue = chidDetailNode.getFirstChild().getNodeValue();
										if(lineValue != null && lineValue.trim().length() > 0)
										{
											lineValueInt = Integer.parseInt(lineValue.trim());
										}
									}
								}
								if(chidDetailNode.getNodeName().equalsIgnoreCase("attribute") )
								{
									//System.out.println("operation node found >>>>>" + chidDetailNode.getNodeName());
									updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
									//System.out.println("Update flag is......."+updateFlag);

								}

								if(chidDetailNode.getNodeName().equalsIgnoreCase("line_no__invtrace") )
								{
									//System.out.println("invoiceTrace node found >>>>>" + chidDetailNode.getNodeName());
									if(chidDetailNode.getFirstChild() != null )
									{
										lineValue = chidDetailNode.getFirstChild().getNodeValue();
										if(mlineNoStr !=null && !"0".equalsIgnoreCase(mlineNoStr) && !updateFlag.equalsIgnoreCase("D") && lineNoInt != lineValueInt && lineValue.trim().equalsIgnoreCase(invTrace.trim()))
										{
											//System.out.println("Duplicate item");
											errCode = "VTLININVTR";
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
									}
								}
							}
						}
						// Added by Mahesh Saggam on 17-June-2019 [End]
					}
					if( childNodeName.equalsIgnoreCase( "item_code" ) )
					{		
						mVal = genericUtility.getColumnValue( "item_code", dom );

						siteCode = genericUtility.getColumnValue( "site_code", dom1 );
						String mtrandateStr = genericUtility.getColumnValue( "tran_date", dom1 );
						mtrandate = Timestamp.valueOf(genericUtility.getValidDateString( mtrandateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						custCode = genericUtility.getColumnValue( "cust_code", dom1 );
						lsRetOpt = genericUtility.getColumnValue("ret_opt", dom1);
						System.out.println("5558 rep opt ="+lsRetOpt);
						if(lsRetOpt.equals("R"))
						{
							sqlStr = "select (case when item_ser is null then ' ' else item_ser end) as item_ser from item where item_code = ?";
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								itemSer = rs.getString(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sqlStr = "select count(*) from customer_series where item_ser = ? and cust_code = ?";
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1,itemSer);
							pstmt.setString(2,custCode);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt(1);

							}
							if( cnt == 0 )
							{
								errCode = "VTRELITMS";
								System.out.println("error code="+errCode);
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


						}

						errCode = isItem( siteCode, mVal, "S-RET", conn ) ; //added by kunal on 5/june/14 change mod name argrument "SAL" TO 'S-RET'
						//errCode = isItem( siteCode, mVal, "SAL", conn );//Commented by Manoj dtd 04/06/2014 not required to validate for inactive item
						//errCode = i_nvo_gbf_func.gbf_item(msite_code,mVal,transer)
						if( errCode != null && errCode.trim().length() > 0 )
						{
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						mVal1 = distCommon.getItemSer( mVal, siteCode, mtrandate, custCode, "C", conn );
						//errCode = f_get_token(mval1,"~t")
						mitemSerHdr = genericUtility.getColumnValue( "item_ser", dom1 );// dw_header.getitemstring(1,"item_ser")
						mitemSerHdr = mitemSerHdr == null  ? "" : mitemSerHdr.trim();
						mVal1 = mVal1 == null  ? "" : mVal1.trim();
						if( !mitemSerHdr.trim().equalsIgnoreCase( mVal1.trim() ) )
						{
							sqlStr = " select ( case when oth_series is null then 'N' else oth_series end ) ls_oth_ser "
									+"	from itemser "
									+"	where item_ser = ? "; //:mitem_ser_hdr;

							pstmt = conn.prepareStatement( sqlStr );

							pstmt.setString( 1, mitemSerHdr );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsOthSer = rs.getString( "ls_oth_ser" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(lsOthSer != null && "N".equalsIgnoreCase( lsOthSer ) )
							{
								errCode = "VTITEM2A";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;							
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							else if( ( !mitemSerHdr.trim().equalsIgnoreCase( mVal1.trim() ) ) && "G".equalsIgnoreCase( lsOthSer ) )
							{						
								sqlStr = " select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) ls_item_ser_crpolicy_hdr "
										+"	from itemser "
										+"	where item_ser = ? ";//
								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, mitemSerHdr );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsItemSerCrpolicyHdr = rs.getString( "ls_item_ser_crpolicy_hdr" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sqlStr = " select (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end) ls_item_ser_crpolicy "
										+" from itemser "
										+"	where  item_ser = ? "; //:mVal1;

								pstmt = conn.prepareStatement( sqlStr );

								pstmt.setString( 1, mVal1 );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsItemSerCrpolicy = rs.getString( "ls_item_ser_crpolicy" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								lsItemSerCrpolicyHdr = lsItemSerCrpolicyHdr == null  ? "" : lsItemSerCrpolicyHdr.trim();
								lsItemSerCrpolicy = lsItemSerCrpolicy == null  ? "" : lsItemSerCrpolicy.trim();
								if ( !lsItemSerCrpolicyHdr.trim().equalsIgnoreCase( lsItemSerCrpolicy.trim() ) )
								{
									errCode = "VTITEM2"; //+'~t'+'Item Does Not Belong To Group'
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}//mitem_ser_hdr <> mVal1 
						}//len(trim(merrCode)) = 0 

						lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom1 );
						itemCode  = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"item_code")
						String mlineNoStr = genericUtility.getColumnValue( "line_no__inv", dom );//dw_detedit[ii_currformno].GetItemnumber(dw_detedit[ii_currformno].GetRow(),"line_no__inv")           
						//mlineNo = Integer.parseInt( getNumString( mlineNoStr ) );
						//Added by Manoj dtd 05/12/12 to cast double to int
						mlineNo =(int) Double.parseDouble(( getNumString( mlineNoStr ) ));
						lsFlag = genericUtility.getColumnValue( "ret_rep_flag", dom );// dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"ret_rep_flag")
						llLineNoInvtraceStr = genericUtility.getColumnValue( "line_no__invtrace", dom ); //dw_detedit[ii_currformno].getitemnumber(1,"line_no__invtrace") //Added by jasmina 13.03.2008 - FI78SUN041

						if (llLineNoInvtraceStr != null && llLineNoInvtraceStr.indexOf(".") > 0)
						{
							llLineNoInvtraceStr = llLineNoInvtraceStr.substring(0,llLineNoInvtraceStr.indexOf("."));
						}
						llLineNoInvtraceStr = llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim();
						llLineNoInvtrace = Integer.parseInt( llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim() ); 	

						if( ( !"P".equalsIgnoreCase( lsFlag ) ) && lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) ) 
						{
							if( llLineNoInvtrace == 0 )
							{
								lsSql = "Select item_code ls_invoice_item from invoice_trace where "; 
								lsSql = lsSql + "invoice_id  = '" + lsInvoiceId + "'";
								lsSql = lsSql + " and inv_line_no = " + mlineNo;
							}
							else
							{
								lsSql = "Select item_code ls_invoice_item from invoice_trace where "; 
								lsSql = lsSql + "invoice_id  = '" + lsInvoiceId + "'";
								lsSql = lsSql + " and line_no = " + llLineNoInvtrace;
							}
							sqlStr = lsSql;
							pstmt = conn.prepareStatement( sqlStr );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsInvoiceItem = rs.getString( "ls_invoice_item" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sqlStr = " select ( case when item_parnt is null then ' ' else item_parnt end ) ls_item_parnt "
									+"	from item "
									+"	where item_code = ? ";// :ls_item_code;
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, itemCode );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsItemParnt = rs.getString( "ls_item_parnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sqlStr = " select ( case when item_parnt is null then ' ' else item_parnt end ) ls_item_parnt_inv "
									+"	from item "
									+"	where item_code = ? ";//:ls_invoice_item

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsInvoiceItem );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsItemParntInv = rs.getString( "ls_item_parnt_inv" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							lsItemParnt = lsItemParnt == null  ? "" : lsItemParnt.trim();
							lsItemParntInv = lsItemParntInv == null  ? "" : lsItemParntInv.trim();
							if ( !lsItemParnt.trim().equalsIgnoreCase( lsItemParntInv.trim() ) )
							{
								errCode = "VTINALL";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						// End Sanjeev - 07/08/03 
					}
					if( childNodeName.equalsIgnoreCase( "rate" ) || childNodeName.equalsIgnoreCase( "rate__stduom" ) )
					{ 
						String fldName = childNodeName.toLowerCase();
						String lcRateStr = genericUtility.getColumnValue( childNodeName.toLowerCase(), dom );

						lcRate = Double.parseDouble( lcRateStr == null || lcRateStr.trim().length() == 0 ? "0" : lcRateStr.trim() );
						lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"invoice_id")
						itemCode  = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"item_code")
						lsLotNo	  = genericUtility.getColumnValue( "lot_no", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"lot_no")
						lsLotSl	  = genericUtility.getColumnValue( "lot_sl", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"lot_sl")
						lsPricelist =  genericUtility.getColumnValue( "price_list", dom1 );//dw_header.GetItemString(dw_header.GetRow(),"price_list")     
						String ldTaxdateStr = genericUtility.getColumnValue( "tax_date", dom1 ); //dw_header.getitemdatetime(1,"tax_date")
						ldTaxdate = Timestamp.valueOf(genericUtility.getValidDateString( ldTaxdateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						String lcQtystdStr = genericUtility.getColumnValue( "quantity__stduom", dom ); //dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),"quantity__stduom")
						lcQtystd = Double.parseDouble( lcQtystdStr == null || lcQtystdStr.trim().length() == 0 ? "0" : lcQtystdStr.trim() );
						//added by rupali on 01/04/2021 [start]
						if(childNodeName.equalsIgnoreCase( "rate" ))
						{
							String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
							String historyType = "";
							sql = "SELECT HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
									+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, docKey);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								historyType = checkNull(rs.getString("HISTORY_TYPE"));
							}
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
							System.out.println("inside RATE historyType::::["+historyType+"]");
                            //Commented by Anagha R on 20/04/2021 for case sales return form(Average rate issue), not able to save the salesreturnform transaction START
                            /*if("S".equalsIgnoreCase(historyType))
							{
								int count = 0;
								sql = "select count(*) as count from invoice_trace where item_code = ? and rate = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setDouble(2, lcRate);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}
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
								
								System.out.println("inside rate count is:::["+count+"]");
								if(count == 0)
								{
									errCode = "INVALRATE";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
                            }*/
                            //Commented by Anagha R on 20/04/2021 for case sales return form(Average rate issue), not able to save the salesreturnform transaction END

                            //Added by Anagha R on 21/04/2021 for case sales return form(Average rate issue), not able to save the salesreturnform transaction START
                                    if("S".equalsIgnoreCase(historyType))
                                    {
                                        double maxRate = 0;
                                        sql = "select max(rate) as rate from invoice_trace where item_code = ? ";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setString(1, itemCode);
                                        rs = pstmt.executeQuery();
                                        if(rs.next())
                                        {
                                            maxRate = rs.getDouble("rate");
                                        }
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
                                        System.out.println("inside maxRate is:::["+maxRate+"]");
                                        if(lcRate > maxRate)
                                        {
                                            errCode = "INVALRATE";
                                            errList.add( errCode );
                                            errFields.add( childNodeName.toLowerCase() );
                                        }
                                    }

                                //Added by Anagha R on 21/04/2021 for case sales return form(Average rate issue), not able to save the salesreturnform transaction END
						}
						//added by rupali on 01/04/2021 [end]
						if( lsPricelist != null && lsPricelist.trim().length() > 0 && !"null".equalsIgnoreCase( lsPricelist.trim() ) )
						{
							//lcRateInv = i_nvo_gbf_func.gbf_pick_rate(ls_pricelist,ld_taxdate ,ls_item_code,ls_lot_no,'',lc_qtystd)
							lsLotNo = lsLotNo == null ? "" : lsLotNo;
							itemCode = itemCode == null ? "" : itemCode;								
							lcRateInv = distCommon.pickRate(lsPricelist,ldTaxdateStr,itemCode,lsLotNo,"D",lcQtystd, conn );
						}
						if( lcRate < 0 )
						{
							errCode = "VTRATE2";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;	
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							if( lsInvoiceId == null || lsInvoiceId.trim().length() == 0 )
							{
								lcRate = getReqDecimal(lcRate, 4);
								lcRateInv = getReqDecimal(lcRateInv, 4);
								if ( lcRate == 0 && lcRateInv != 0 )
								{
									errCode = "VTRATE2";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;	
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								//added by nandkumar gadkari on 14/09/19-----start--- For 0 rate not allowed in without invoice case  
								if ( lcRate == 0 )
								{
									errCode = "VTRATE3";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								//added by nandkumar gadkari on 14/09/19-----end
								// added by rupali on 29/04/2021 [start]
								dockey = genericUtility.getColumnValue( "doc_key", dom ); 
								if(dockey !=null && dockey.trim().length() >0)
								{
									String historyType = "";
									sql = " select eff_cost, history_type from min_rate_history where doc_key = ?" ;
									pstmt= conn.prepareStatement(sql);
									pstmt.setString(1,dockey);
									rs = pstmt.executeQuery(); 
									if(rs.next())
									{
										minRate = rs.getDouble(1);
										historyType = checkNull(rs.getString(2)).trim();
									}
									rs.close();
									pstmt.close();
									pstmt = null;
									rs = null;
									System.out.println("8960 historyType::["+historyType+"] minRate :::["+minRate+"] lcRate:::["+lcRate+"]");
									if("S".equalsIgnoreCase(historyType) && lcRate>minRate && minRate != 0)
									{
										errCode = "VTINVRATE1";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
								// added by rupali on 29/04/2021 [end]
							}
							else if( lsInvoiceId.trim().length() > 0 )
							{	
								// 26/10/10 manoharan to set average rate in case invoiced from multiple lot
								llLineNoInvtraceStr = genericUtility.getColumnValue( "line_no__invtrace", dom ); 
								if (llLineNoInvtraceStr != null && llLineNoInvtraceStr.indexOf(".") > 0)
								{
									llLineNoInvtraceStr = llLineNoInvtraceStr.substring(0,llLineNoInvtraceStr.indexOf("."));
								}
								llLineNoInvtraceStr = llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim();
								llLineNoInvtrace = Integer.parseInt( llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim() ); 	
								sqlStr = " select i.SORD_NO as SORD_NO, i.SORD_LINE_NO as LINE_NO__SORD"
										+"	from invoice_trace i "
										+"	where i.invoice_id = ? "//:ls_invoice_id
										+"	and i.line_no = ? ";

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNoInvtrace );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									saleOrder = rs.getString( "SORD_NO" );
									lineNoSord = rs.getString( "LINE_NO__SORD" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (lineNoSord.trim().length() == 1)
								{
									spaces = "  ";
								}
								if (lineNoSord.trim().length() == 2)
								{
									spaces = " ";
								}
								lineNoSord = spaces.concat(lineNoSord.trim());
								//sqlStr = " select max(case when '" + fldName + "' ='rate' then rate else rate__stduom*conv__rtuom_stduom end ) lc_inv_rate "
								//		+"	from invoice_trace "
								//		+"	where invoice_id = ? "//:ls_invoice_id
								//		+"	and item_code = ? " //:ls_item_code
								//		+"	and lot_no 	 = ? " //:ls_lot_no
								//		+"	and lot_sl 	 = ? "; //:ls_lot_sl;

								sqlStr = " select (sum(case when '" + fldName + "' ='rate' then rate else rate__stduom*conv__rtuom_stduom end * quantity) / sum(quantity) ) as lc_inv_rate "
										+"	from invoice_trace "
										+"	where invoice_id = ? "
										+ " and line_no = ? "; 
								//+"	and SORD_NO = ? " //
								//+"	and SORD_LINE_NO 	 = ? " 
								//+ " and lot_no = ? "; // 14/07/14 manoharan consider lot_no as in case of issue from multiple lot_no the rate may become more

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNoInvtrace );
								//pstmt.setString( 2, saleOrder );
								//pstmt.setString( 3, lineNoSord );
								//pstmt.setString( 4, lsLotNo ); // 14/07/14 manoharan consider lot_no as in case of issue from multiple lot_no the rate may become more
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lcInvRate = rs.getDouble( "lc_inv_rate" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								lcInvRate = getRequiredDecimal(lcInvRate, 4);//change 3 to 4 decimals by nandkumar gadkari on 05/06/2020
								lcRate = getRequiredDecimal(lcRate, 4);//change 3 to 4 decimals by nandkumar gadkari on 05/06/2020
								System.out.println("manohar lcRateStr [" + lcRateStr + "] lcRate [" + lcRate + "] > lcInvRate [" + lcInvRate + "]");
								lcInvRate = getReqDecimal(lcInvRate, 4);
								lcRate = getReqDecimal(lcRate, 4);
								if ( lcRate > lcInvRate && lcInvRate > 0 )
								{
									//// 18/10/14 manoharan take care if specific lot rate < average rate
									//StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><Detail2><editFlag>");
									//valueXmlString.append(editFlag).append("</editFlag>");
									//StringBuffer minRateBuff = getMinRate( dom, dom1, "lot_no", valueXmlString, conn);
									//System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );
									////valueXmlString = minRateBuff;

									//String rateValStr = getTagValue(  minRateBuff.toString(), "rate" );
									//System.out.println("manohar lcRateStr [" + lcRateStr + "] lcRate [" + lcRate + "] > lcInvRate [" + lcInvRate + "] avgRate [" + rateValStr + "]");
									//double avgRate = getRequiredDecimal( Double.parseDouble( getNumString( rateValStr ) ), 4 );
									//if (avgRate < lcRate) 
									//{

									//addded by nandkumar gadkari on 05/06/2020-------------------start--------------for avg rate set from min rate history
									dockey = genericUtility.getColumnValue( "doc_key", dom ); 

									if(dockey !=null && dockey.trim().length() >0)
									{
										String historyType = "";
										sql = " select eff_cost, history_type from min_rate_history where doc_key = ?" ;
										pstmt= conn.prepareStatement(sql);
										pstmt.setString(1,dockey);
										rs = pstmt.executeQuery(); 
										if(rs.next())
										{
											minRate = rs.getDouble(1);
											historyType = checkNull(rs.getString(2)).trim();
										}
										rs.close();
										pstmt.close();
										pstmt = null;
										rs = null;
										System.out.println("9012 historyType::["+historyType+"] minRate :::["+minRate+"] lcRate:::["+lcRate+"]");
										//if(lcRate>minRate)
										if(lcRate>minRate && !("S".equalsIgnoreCase(historyType) && minRate == 0))
										{
											errCode = "VTINVRATE1";
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
									}
									else
									{
										//addded by nandkumar gadkari on 05/06/2020-------------------end-------------for avg rate set from min rate history-
										errCode = "VTINVRATE1";
										//errString = getErrorString( childNodeName, errCode, userId ); 
										//break;	
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );

									}
									//}
									// end 18/10/14 manoharan take care if specific lot rate < average rate
								}
								//added by nandkumar gadkari on 14/09/19-----start--- For 0 rate not allowed in  invoice case 
								fullRet	  = (genericUtility.getColumnValue( "full_ret", dom1 ));
								fullRet= fullRet== null || fullRet.trim().length() == 0 ? "N" : (fullRet).trim();
								if(lcRate== 0)
								{

									if(!"Y".equalsIgnoreCase(fullRet))
									{
										errCode = "VTINVRATE1";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
									else
									{
										if(lcInvRate !=0)
										{
											errCode = "VTINVRATE1";
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
									}


								}

								//added by nandkumar gadkari on 14/09/19-----end--- For 0 rate not allowed in  invoice case 
							}//if isnull(ls_invoice_id) or len(trim(ls_invoice_id)) = 0
						}//if lc_rate < 0 
					}
					if( childNodeName.equalsIgnoreCase( "rate__clg" ))// 20/02/14 manoharan rate__clg should not be genative
					{ 
						String fldName = childNodeName.toLowerCase();
						String lcRateStr = genericUtility.getColumnValue( childNodeName.toLowerCase(), dom );

						lcRate = Double.parseDouble( lcRateStr == null || lcRateStr.trim().length() == 0 ? "0" : lcRateStr.trim() );
						if( lcRate < 0 )
						{
							errCode = "VTRATE2";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;	
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					if( childNodeName.equalsIgnoreCase( "unit" )  )
					{ 
						mVal = genericUtility.getColumnValue( "unit", dom );
						mVal1 = genericUtility.getColumnValue( "unit__std", dom );
						mVal = mVal == null || mVal.trim().length() == 0 || "null".equalsIgnoreCase( mVal.trim() ) ? "" : mVal.trim();
						mVal1 = mVal1 == null || mVal1.trim().length() == 0 || "null".equalsIgnoreCase( mVal1.trim() ) ? "" : mVal1.trim();


						sqlStr = " Select Count(*) cnt from uom where unit = ? ";

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mVal );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VTUNIT1";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else if( !mVal.trim().equalsIgnoreCase( mVal1.trim() ) )
						{
							cnt = 0;
							sqlStr = " select count(*) cnt "
									+"	from uomconv "
									+"	where unit__fr = ? "//:mVal
									+"		and unit__to = ? "; //:mVal1 ;	

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							pstmt.setString( 2, mVal1 );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{

								sqlStr = " select count(*) cnt "
										+"	from uomconv "
										+"	where unit__fr = ? "//:mVal
										+"		and unit__to = ? "; //:mVal1 ;	

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, mVal1 );
								pstmt.setString( 2, mVal );

								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
									errCode = "VTUNIT2";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;						
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}

						}
					}
					if( childNodeName.equalsIgnoreCase( "unit__rate" )  )
					{ 
						mVal = genericUtility.getColumnValue( "unit__rate", dom );
						mVal1 = genericUtility.getColumnValue( "unit__std", dom );//dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"unit__std")           
						mVal = mVal == null  ? "" : mVal.trim();
						mVal1 = mVal1 == null  ? "" : mVal1.trim();
						sqlStr = " Select Count(*) cnt from uom where unit = ? "; //:mVal1 ;	

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mVal );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VTUNIT1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;						
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}	  
						else if( !mVal.equalsIgnoreCase( mVal1 ) )
						{
							sqlStr = " select count(*) cnt "
									+"	from uomconv "
									+" where unit__fr = ? " //:mVal 
									+"	and unit__to = ? "; //:mVal1 ;	


							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							pstmt.setString( 2, mVal1 );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								//errCode = "VTUNIT2";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;						
								sqlStr = " select count(*) cnt "
										+" from uomconv "
										+"	where unit__fr = ? " //:mVal1 
										+"		and unit__to = ? "; //:mVal ;	

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, mVal1 );
								pstmt.setString( 2, mVal );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
									errCode = "VTUNIT3";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;						
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}						
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "tax_class" )  )
					{ 
						mVal = genericUtility.getColumnValue( "tax_class", dom );
						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = " Select count(*) cnt "
									+"	from taxclass "
									+" where tax_class = ? ";

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTTCLASS1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;						
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}				
						}
					}

					if( childNodeName.equalsIgnoreCase( "tax_chap" )  )
					{ 
						mVal = genericUtility.getColumnValue( "tax_chap", dom );

						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = " Select count(*) cnt from taxchap where tax_chap = ? ";

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTTCHAP1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;						
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}							 
						}
					}	 
					if( childNodeName.equalsIgnoreCase( "tax_env" )  )
					{ 
						//mVal = genericUtility.getColumnValue( "tax_env", dom );
						mVal = distCommon.getParentColumnValue("tax_env", dom, "2");
						System.out.println("SR TaxEnv["+mVal+"]");
						String mtrandateStr = genericUtility.getColumnValue( "tran_date", dom1 );
						mtrandate = Timestamp.valueOf(genericUtility.getValidDateString( mtrandateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = " Select count(*) cnt from taxenv where tax_env = ? ";
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTTENV1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;						
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}							 
							else
							{
								//Pavan R 17sept19 start[to validate tax environment]
								//errCode = distCommon.getCheckTaxEnvStatus( mVal, mtrandate, conn ); //_gf_check_taxenv_status(mVAL,mtrandate);
								errCode = distCommon.getCheckTaxEnvStatus( mVal, mtrandate, "S", conn );
								//Pavan R 17sept19 end[to validate tax environment]
								if( errCode != null && errCode.trim().length() > 0 )
								{
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					}	 
					if( childNodeName.equalsIgnoreCase( "reas_code" )  )
					{ 
						mreasCode = getAbsString( genericUtility.getColumnValue( "reas_code", dom ) );
						mcount = 0;

						sqlStr = " select count(*) mcount "
								+"  from sreturn_reason "
								+" where reason_code = ? ";

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mreasCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							mcount = rs.getInt( "mcount" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( mcount == 0 )
						{
							errCode = "VUREASON";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;						
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}							 
					}
					if( childNodeName.equalsIgnoreCase( "site_code__mfg" )  )
					{ 

						siteCodeMfg = genericUtility.getColumnValue( "site_code__mfg", dom );
						itemCode = genericUtility.getColumnValue( "item_code", dom );//dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"item_code")
						System.out.println( "itemCode :: " + itemCode );
						sqlStr = " select ( case when supp_sour is null then 'M' else supp_sour end ) ls_supp_sour "
								+" from item where item_code = ? ";
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{

							lsSuppSour = rs.getString( "ls_supp_sour" );
						}
						else
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							errCode = "VTITEM1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;						
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						if( rs != null )
							rs.close();
						rs = null;
						if( pstmt != null )
							pstmt.close();
						pstmt = null;

						System.out.println( "lsSuppSour :: " + lsSuppSour );
						System.out.println( "siteCodeMfg :: " + siteCodeMfg );
						if( "M".equalsIgnoreCase( lsSuppSour ) )
						{
							if( siteCodeMfg == null || siteCodeMfg.trim().length() == 0 || "null".equalsIgnoreCase( siteCodeMfg.trim() ) )
							{
								errCode = "VTSITEMFG1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;											
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "mfg_date" )  )
					{ 
						String mdateStr = genericUtility.getColumnValue( "mfg_date", dom );

						itemCode = genericUtility.getColumnValue( "item_code", dom );

						sqlStr = " select ( case when track_shelf_life is null then 'N' else track_shelf_life end ) ls_track_shelf_life "
								+" from item where item_code = ? "; //:ls_item_code;

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lsTrackShelfLife = rs.getString( "ls_track_shelf_life" );
						}
						else
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							errCode = "VTITEM1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;						
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						if( rs != null )
							rs.close();
						rs = null;
						if( pstmt != null )
							pstmt.close();
						pstmt = null;

						if( lsTrackShelfLife != null 
								&& !"null".equalsIgnoreCase( lsTrackShelfLife )  
								&&  "Y".equalsIgnoreCase( lsTrackShelfLife ) )
						{
							if( mdateStr == null 
									|| mdateStr.trim().length() == 0 
									|| "null".equalsIgnoreCase( mdateStr.trim() ) )
							{
								errCode = "VTMFGDATE3";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "exp_date" )  )
					{ 
						String mdateStr = genericUtility.getColumnValue( "exp_date", dom );

						itemCode = genericUtility.getColumnValue( "item_code", dom );
						sqlStr = " select ( case when track_shelf_life is null then 'N' else track_shelf_life end ) ls_track_shelf_life "
								+"	from item where item_code = ? "; //:ls_item_code;

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lsTrackShelfLife = rs.getString( "ls_track_shelf_life" );
						}
						else
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							errCode = "VTITEM1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;						
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						if( rs != null )
							rs.close();
						rs = null;
						if( pstmt != null )
							pstmt.close();
						pstmt = null;					

						if( lsTrackShelfLife != null && "Y".equalsIgnoreCase( lsTrackShelfLife.trim() ) )
						{
							if( mdateStr == null || mdateStr.trim().length() == 0 )
							{
								errCode = "VTEXPDATE1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;					
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "ret_rep_flag" )  )
					{ 
						lsFlag = genericUtility.getColumnValue( "ret_rep_flag", dom );
						lsRetopt = genericUtility.getColumnValue( "ret_opt", dom );

						if( lsRetopt != null && "C".equalsIgnoreCase( lsRetopt.trim() ) )
						{
							if( lsFlag != null && !"R".equalsIgnoreCase( lsFlag.trim() ) )
							{
								errCode = "VTRETREP2";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;					
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						if( lsFlag != null && "P".equalsIgnoreCase( lsFlag.trim() ) )
						{
							lsSer = genericUtility.getColumnValue( "item_ser", dom1 );
							lsCust = genericUtility.getColumnValue( "cust_code", dom1 );
							lsInvoiceId  = genericUtility.getColumnValue( "invoice_id", dom );

							sqlStr = " select count(*) cnt "
									+"	from customer_series "
									+"	where  cust_code = ? "//:ls_cust 
									+"		and item_ser = ? "; //:ls_ser;

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsCust );
							pstmt.setString( 2, lsSer );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errCode = "VTITEM7";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								String retOptStr = genericUtility.getColumnValue(  "ret_opt", dom1 );
								if( retOptStr != null && "R".equalsIgnoreCase( retOptStr ) )
								{
									if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 )
									{
										errCode = "VTRETREP1";
										//errString = getErrorString( childNodeName, errCode, userId ); 
										//break;
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}
						else if( "R".equalsIgnoreCase( lsFlag.trim() ) )
						{
							String retOptStr = genericUtility.getColumnValue(  "ret_opt", dom1 );
							if( retOptStr != null && "D".equalsIgnoreCase( retOptStr.trim() ) )
							{
								errCode = "VTRETRET";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;					
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "quantity__stduom" )  )
					{ 		
						ldCurrQty = 0;
						itemCode  = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].getitemstring(1,"item_code")
						lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom );//dw_detedit[ii_currformno].getitemstring(1,"invoice_id")
						lsFullRet  = genericUtility.getColumnValue( "full_ret", dom );//dw_detedit[ii_currformno].getitemstring(1,"full_ret")
						lsLotNo 	= genericUtility.getColumnValue( "lot_no", dom );//dw_detedit[ii_currformno].getitemstring(1,"lot_no")
						lsLotSl 	= genericUtility.getColumnValue( "lot_sl", dom );//dw_detedit[ii_currformno].getitemstring(1,"lot_sl")	
						ldCurrQtyStr   = genericUtility.getColumnValue( "quantity__stduom", dom );//dw_detedit[ii_currformno].getitemnumber(1,fldname)	
						ldCurrQty = Double.parseDouble( ldCurrQtyStr == null || ldCurrQtyStr.trim().length() == 0 ? "0" : ldCurrQtyStr.trim() );
						lsTranId    = genericUtility.getColumnValue( "tran_id", dom );//dw_detedit[ii_currformno].getitemstring(1,"tran_id")
						llLineNoStr = getNumString( genericUtility.getColumnValue( "line_no", dom ) );//dw_detedit[ii_currformno].getitemnumber(1,"line_no")
						//Added By PriyankaC on 27May2019 [START].
						convQty   = genericUtility.getColumnValue( "conv__qty_stduom", dom );	
						convCurrQty = Double.parseDouble( convQty == null || convQty.trim().length() == 0 ? "0" : convQty.trim() );
						CurrQty   = genericUtility.getColumnValue( "quantity", dom );	
						CurrQtydtl = Double.parseDouble( CurrQty == null || CurrQty.trim().length() == 0 ? "0" : CurrQty.trim() );
						//Added By PriyankaC on 27May2019 [END].
						if (llLineNoStr != null && llLineNoStr.indexOf(".") > 0)
						{
							llLineNoStr = llLineNoStr.substring(0,llLineNoStr.indexOf("."));
						}
						llLineNoStr = llLineNoStr == null || llLineNoStr.trim().length() == 0 ? "0" : llLineNoStr.trim();
						llLineNo = Integer.parseInt( llLineNoStr == null || llLineNoStr.trim().length() == 0 ? "0" : llLineNoStr.trim() );
						lsRetOpt = genericUtility.getColumnValue( "ret_opt", dom1 );
						String llLineNoInvStr  = getNumString(genericUtility.getColumnValue( "line_no__inv", dom ));//dw_detedit[ii_currformno].getitemnumber(1,"line_no__inv")
						if (llLineNoInvStr != null && llLineNoInvStr.indexOf(".") > 0)
						{
							llLineNoInvStr = llLineNoInvStr.substring(0,llLineNoInvStr.indexOf("."));
						}

						llLineNoInv = Integer.parseInt( llLineNoInvStr == null || llLineNoInvStr.trim().length() == 0 ? "0" : llLineNoInvStr.trim() );
						llLineNoInvtraceStr = getNumString(genericUtility.getColumnValue( "line_no__invtrace", dom ));// dw_detedit[ii_currformno].getitemnumber(1,"line_no__invtrace")
						if (llLineNoInvtraceStr != null && llLineNoInvtraceStr.indexOf(".") > 0)
						{
							llLineNoInvtraceStr = llLineNoInvtraceStr.substring(0,llLineNoInvtraceStr.indexOf("."));
						}
						llLineNoInvtrace = Integer.parseInt( llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim() );
						System.out.println( " ldCurrQty :: " + ldCurrQty + "CurrQtydtl : " +CurrQtydtl + "convCurrQty :" +convCurrQty);
						if( ldCurrQty == 0 )
						{
							errCode = "VTISS1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;					
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						//Added By PriyankaC on 27MAY19 [Start]
						else 
						{
							varQuatity = CurrQtydtl * convCurrQty;
							System.out.println( "varQuatity :: " + varQuatity );
							if(ldCurrQty != varQuatity)
							{
								errCode = "VTISS1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						//Added By PriyankaC on 27MAY19 [END]
						System.out.println( " lsInvoiceId :val1: " + lsInvoiceId );
						if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
						{
							System.out.println( " llLineNoInvtrace :val1: " + llLineNoInvtrace );
							if ( llLineNoInvtrace == 0 )
							{
								sqlStr = " select sum( case when quantity__stduom is null then 0 else quantity__stduom end ) lc_invqty_lot "
										+" from invoice_trace "
										+" where invoice_id  = ? " //:ls_invoice_id
										+" 	and inv_line_no = ? " //:ll_line_no__inv
										+" 	and lot_no = ? " //:ls_lot_no
										+"  and lot_sl = ? "; //:ls_lot_sl;

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNoInv );
								pstmt.setString( 3, lsLotNo );
								pstmt.setString( 4, lsLotSl );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lcInvqtyLot = rs.getDouble( "lc_invqty_lot" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								System.out.println( " lcInvqtyLot :val1: " + lcInvqtyLot );									
							}
							else
							{
								sqlStr = " select sum(case when quantity__stduom is null then 0 else quantity__stduom end) lc_invqty_lot "
										+" from   invoice_trace "
										+" where invoice_id  = ? " //:ls_invoice_id
										+" 	and	inv_line_no = ? " //:ll_line_no__inv			  
										+" 	and	line_no 	 = ? "; //:ll_line_no__invtrace ;

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNoInv );
								pstmt.setInt( 3, llLineNoInvtrace );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lcInvqtyLot = rs.getDouble( "lc_invqty_lot" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;	
								System.out.println( " lcInvqtyLot :val2: " + lcInvqtyLot );																		
							}

							lcSrqtyLot = ldCurrQty;
							lcSrqty = ldCurrQty;
							System.out.println( " lcSrqtyLot :val2: " + lcSrqtyLot );
							System.out.println( " lsFullRet :val2: " + lsFullRet );
							System.out.println( " lsRetOpt :val2: " + lsRetOpt );

							if( lcSrqtyLot > lcInvqtyLot )
							{
								if( lsRetOpt != null && !"D".equalsIgnoreCase( lsRetOpt.trim() ) )
								{
									errCode = "VTSRET7";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;						
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							else
							{
								if( lcSrqtyLot < lcInvqtyLot && "Y".equalsIgnoreCase( lsFullRet.trim() ) )
								{
									errCode = "VTSRET11";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;												
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									if( lcSrqtyPrev == 0 )
									{
										if( lsFullRet != null && lcSrqty == lcInvqtyLot && !"Y".equalsIgnoreCase( lsFullRet.trim() ) )
										{
											errCode = "VTSRET10";
											//errString = getErrorString( childNodeName, errCode, userId ); 
											//break;														
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
										else
										{
											if( lsFullRet != null && lcSrqty != lcInvqtyLot && "Y".equalsIgnoreCase( lsFullRet.trim() ) )
											{
												errCode = "VTSRET11";
												//errString = getErrorString( childNodeName, errCode, userId ); 
												//break;						
												errList.add( errCode );
												errFields.add( childNodeName.toLowerCase() );
											}
										}
									}
									else
									{
										if( lcSrqty != lcInvqtyLot && "Y".equalsIgnoreCase( lsFullRet.trim() ) )
										{
											errCode = "VTSRET11";
											//errString = getErrorString( childNodeName, errCode, userId ); 
											//break;						
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
									}
								}
							}
						}
					}
					if( childNodeName.equalsIgnoreCase( "status" ) )
					{ 
						String mtrandateStr = genericUtility.getColumnValue( "tran_date", dom1 ); 
						mtrandate = Timestamp.valueOf(genericUtility.getValidDateString( mtrandateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

						String mexpDateStr = genericUtility.getColumnValue( "exp_date", dom ); 
						if( mexpDateStr != null && mexpDateStr.trim().length() > 0 )
						{
							mexpDate = Timestamp.valueOf(genericUtility.getValidDateString( mexpDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						lsStatus = genericUtility.getColumnValue( "status", dom );
						mitemCode = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].GetRow(),"item_code")			
						if( mtrandate != null && mexpDate != null && lsStatus != null && mexpDate.after( mtrandate ) && "E".equalsIgnoreCase( lsStatus.trim() ) )
						{
							errCode = "VTSRET8";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}  
						else if( mtrandate != null && mexpDate != null && lsStatus != null && ( mexpDate.before( mtrandate ) || mexpDate == mtrandate ) && ( "N".equalsIgnoreCase( lsStatus.trim() ) || "S".equalsIgnoreCase( lsStatus.trim() ) ) )
						{
							errCode = "VTSRET14";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}else if( mtrandate != null && mexpDate != null && mexpDate.after( mtrandate ) )
						{
							sqlStr = " select ( case when min_shelf_life is null then 0 else min_shelf_life end ) mmin_shlife "
									+" from item "
									+" where item_code = ? ";// :mitem_code;

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mitemCode );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mminShlife = rs.getDouble( "mmin_shlife" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							mchkDate = distCommon.CalcExpiry( mtrandate, mminShlife );
							if( ( mexpDate.before( mchkDate ) || mexpDate == mchkDate ) && ( "E".equalsIgnoreCase( lsStatus ) || "S".equalsIgnoreCase( lsStatus ) ) )
							{
								errCode = "VTSRET15";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							else if( mexpDate.after( mchkDate ) && "N".equalsIgnoreCase( lsStatus ) )
							{
								errCode = "VTSRET16";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}	
					if( childNodeName.equalsIgnoreCase( "lot_no" ) )
					{ 
						lsLotNo = genericUtility.getColumnValue( "lot_no", dom );
						if( lsLotNo == null )
						{
							errCode = "VTLOTEMPTY";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom ); // dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].GetRow(),"invoice_id")
							llLineNoStr = getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) );//dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].GetRow(),"line_no__inv")
							if (llLineNoStr != null && llLineNoStr.indexOf(".") > 0)
							{
								llLineNoStr = llLineNoStr.substring(0,llLineNoStr.indexOf("."));
							}
							llLineNo = Integer.parseInt( llLineNoStr == null || llLineNoStr.trim().length() == 0 ? "0" : llLineNoStr );
							if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
							{
								sqlStr = "select count(*) ll_count from invoice_trace " 
										+" where invoice_id  = ? " //:ls_invoice_id
										+"	and inv_line_no = ? " //:ll_line_no
										+" 	and lot_no      = ? ";//:ls_lot_no;

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNo );
								pstmt.setString( 3, lsLotNo );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									llCount = rs.getInt( "ll_count" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( llCount == 0 )
								{
									errCode = "VTSRET9";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;						
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							//validation added by Nandkumar gadkari on 19/11/18-------------------------Start---------------------
							itemCode = checkNull(genericUtility.getColumnValue( "item_code", dom ));
							llCount=0;
							sqlStr = "select  count(*) ll_count "
									+ " from item_lot_info "
									+ " where item_code = ? "
									+ " and lot_no = ? ";

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, itemCode );
							pstmt.setString( 2, lsLotNo );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								llCount = rs.getInt( "ll_count" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( llCount == 0 )
							{
								errCode = "VTIVLOTINF";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							//validation added by Nandkumar gadkari on 19/11/18-------------------------end---------------------
						}
					}		
					if( childNodeName.equalsIgnoreCase( "lot_sl" ) )
					{ 

						lsLotSl = genericUtility.getColumnValue( "lot_sl", dom ); //dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].GetRow(),fldname)
						if( lsLotSl == null )
						{
							errCode = "VTLOTSL001";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;				
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					if( childNodeName.equalsIgnoreCase( "loc_code" ) )
					{ 
						mVal = genericUtility.getColumnValue( "loc_code", dom );
						itemCode = genericUtility.getColumnValue( "item_code", dom );

						if( mVal != null && mVal.trim().length() > 0 && !"null".equalsIgnoreCase( mVal.trim() ) )
						{
							sqlStr = " select count(*) cnt from location where loc_code = ? ";

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, mVal );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if ( cnt == 0 )
							{
								errCode = "VMLOC1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;				
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							// Added by mahesh on 09-07-2014
							else{

								sql = " Select facility_code from location Where loc_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									faciLocCode = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = " Select facility_code from site Where site_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									faciSiteCode = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								faciLocCode = faciLocCode == null ? "" : faciLocCode.trim();
								faciSiteCode = faciSiteCode == null ? "" : faciSiteCode.trim();

								if(faciLocCode.length() > 0 && faciSiteCode.length() > 0){
									if(!faciLocCode.equalsIgnoreCase(faciSiteCode)){
										errCode = "VMFACI2"; // 'Diffrent Loc Code from location and site

										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}

							// End of code added by mahesh
						}
						else
						{
							errCode = "VMLOC1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;				
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						String locLocType = null;
						String itemLocType = null;
						//itemCode = genericUtility.getColumnValue( "item_code", dom );
						sqlStr = "select loc_type from item where item_code = ? ";
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							itemLocType = rs.getString( "loc_type" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						itemLocType = itemLocType == null ? "" : itemLocType;
						sqlStr = "select loc_type from location where loc_code = ? ";
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mVal );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locLocType = rs.getString( "loc_type" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						locLocType = locLocType == null ? "" : locLocType;
						if( !locLocType.trim().equalsIgnoreCase( itemLocType.trim() ) )
						{
							errCode = "VMLOCTYP1";
							errList.add( errCode );
							errFields.add( "loc_code" );
						}

						lsQcreqd = genericUtility.getColumnValue( "qc_reqd", dom );
						sqlStr = " select a.available ls_avilable "
								+" from  invstat a, location b "
								+" where a.inv_stat = b.inv_stat "
								+"	and b.loc_code = ? ";// :mVal ;

						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, mVal );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lsAvilable = rs.getString( "ls_avilable" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						lsQcreqd = lsQcreqd == null  ? "" : lsQcreqd.trim();
						if( "Y".equalsIgnoreCase( lsQcreqd ) && "Y".equalsIgnoreCase( lsAvilable ) )
						{
							errCode = "VTLOCSL";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;									
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
					}	
					if( childNodeName.equalsIgnoreCase( "quantity" ) )
					{ 
						String lcQtyStr = genericUtility.getColumnValue( "quantity", dom );
						lcQty = Double.parseDouble( lcQtyStr == null || lcQtyStr.trim().length() == 0 ? "0" : lcQtyStr.trim() );
						lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom );
						String lcRateStr = genericUtility.getColumnValue( "rate", dom );
						lcRate = Double.parseDouble( lcRateStr == null || lcRateStr.trim().length() == 0 ? "0" : lcRateStr.trim() );
						String llLineNoInvStr  = getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) );
						//Added by Santosh on 02/05/2017 to get unconfirmed adjusted quantity [Start]
						lineNo = genericUtility.getColumnValue( "line_no", dom ).trim();
						itemCode = genericUtility.getColumnValue( "item_code", dom );
						lsLotNo = genericUtility.getColumnValue( "lot_no", dom );
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						custCode = genericUtility.getColumnValue("cust_code", dom1);
						formNo = genericUtility.getColumnValue("form_no", dom1);
						formNo= formNo==null || formNo.trim().length() ==0 ? "  " : formNo;//added by nandkumar gadkari on 9/04/20
						double sdetQtyAdj = 0.0,sdetFQtyAdj = 0.0, domTotalQty = 0.0;
						int nLineNo = Integer.parseInt(lineNo);

						// added by Pavan Rane on 15/09/17
						tranId = genericUtility.getColumnValue("tran_id", dom); 

						//Added by Santosh on 02/05/2017 to get unconfirmed adjusted quantity [End]

						//Added by Santosh on 10/05/2017
						String sreturnAdjOpt = distCommon.getDisparams("999999", "SRETURN_ADJ_OPT", conn);
						//Added by Santosh on 23/05/2017
						boolean isValidInvoiceId = true;

						//Added by Santosh on 02/12/2016 to split quantity in multiple line as per min_rate_history[Start]
						//Changed by Santosh on 10/05/2017 to not to validate split quantity if SRETURN_ADJ_OPT is not defined
						if(! "NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
						{
							double invoiceQty = 0.0, qtyAdj = 0.0;
							//Commented and changed by Santosh on 16/05/2017 to get docKey from dom [Start]
							/*String[] col = new String[500];
								int noSchemeHist = 0;
								double invoiceQty = 0.0, qtyAdj = 0.0;
								boolean dynamicCol = true, colMatch = false;
								String schemeKey = "", varValue = "", currCol = "", docKey = "", docValue = "";
								String sNoSchemeHist = distCommon.getDisparams("999999","SCHEME_HIST_NUM",conn);
								System.out.println( "sNoSchemeHist :: " + sNoSchemeHist );
								noSchemeHist = Integer.parseInt(sNoSchemeHist);
								for (int keyCtr = 1; keyCtr <= noSchemeHist ;  keyCtr++)
								{
									//Added by Santosh on 10/05/2017 to reset docKey in case of more than 1 doc keys are defined
									docKey = "";
									schemeKey = "SCHEME_HIST_KEY" + keyCtr ;
									System.out.println(">>>>>>>>>In for loop schemeKey:"+schemeKey);
									varValue = distCommon.getDisparams("999999",schemeKey,conn);
									System.out.println(">>>>>>>>>In for loop afer schemeKey varValue:"+varValue);

									varValue = varValue.trim();

									String[] varValueArry = varValue.split(",");

									for(int i=0; i<varValueArry.length; i++)
									{
										if("cust_code".equalsIgnoreCase(varValueArry[i].trim()))
										{
											docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom1).trim();
										}
										//Added by Santosh on 28/04/2017 for missing site_code condition [Start]
										else if("site_code".equalsIgnoreCase(varValueArry[i].trim()))
										{
											docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom1).trim();
										}
										//Added by Santosh on 28/04/2017 for missing site_code condition [End]
										else if("item_code".equalsIgnoreCase(varValueArry[i].trim()))
										{
											docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom).trim();
										}
										else if("lot_no".equalsIgnoreCase(varValueArry[i].trim()))
										{
											docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom).trim();
										}
										else if("invoice_id".equalsIgnoreCase(varValueArry[i].trim()))
										{
											docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom).trim();
										}
										else
										{
											docValue = genericUtility.getColumnValue(varValueArry[i].trim(),dom1).trim();
										}

										if (docKey != "" && docKey.trim().length() > 0)
										{
											docKey = docKey + "," + ( docValue == null || docValue.trim().length() == 0 ? "" : docValue);
										}
										else
										{
											docKey = docValue;
										}
									}
								}
								System.out.println(">>>>>>>>after for loop docKey:"+docKey);*/
							String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
							//Commented and changed by Santosh on 16/05/2017 to get docKey from dom [End]
							if( !"".equalsIgnoreCase(docKey.trim()) && docKey.trim().length() > 0)
							{
								System.out.println("docKey in wfvaldata for quantity ["+docKey+"]");
								HashMap<String,String> curDomDataHMap = new HashMap<String,String>();
								curDomDataHMap.put("cust_code", custCode);
								curDomDataHMap.put("item_code", itemCode);
								curDomDataHMap.put("lot_no", lsLotNo);
								curDomDataHMap.put("site_code", siteCode);
								//Commented and changed by Santosh on 16/05/2017
								//curDomDataHMap.put("invoice_id", lsInvoiceId);
								curDomDataHMap.put("doc_key", docKey);
								//Added by Santosh on 20/09/2017 for bug fix split qty validatoin in edit mode
								curDomDataHMap.put("line_no", lineNo);

								if( nLineNo > 1 )
								{
									domTotalQty = getDomQuantity(dom2, curDomDataHMap);
								}
								System.out.println("editFlag["+editFlag+"]");
								sql = " SELECT SDET.INVOICE_ID, SUM(SDET.QUANTITY) AS QTY_ADJ"
										+ " FROM SRETURN SRET, SRETURNDET SDET"
										+ " WHERE SRET.TRAN_ID = SDET.TRAN_ID"
										+ " AND SRET.CONFIRMED = 'N'"
										//Changed by Santosh on  16/05/2017
										//+ " AND SDET.INVOICE_ID IS NOT NULL"
										//+ "	AND SDET.INVOICE_ID = ?"
										+ " AND SDET.DOC_KEY  = ?"
										+ " AND SDET.ITEM_CODE = ?"
										+ " AND SDET.LOT_NO = ?"
										+ " AND SRET.SITE_CODE = ?"
										+ " AND SRET.CUST_CODE = ?";
								/*if(editFlag != null && "E".equalsIgnoreCase(editFlag))
									{
										sql = sql +" AND SDET.TRAN_ID <> ?" ;
										//Added by Santosh on 20/09/2017 for bug fix split qty validatoin in edit mode
										sql = sql +" AND SDET.LINE_NO <> ?" ;
									}*/ // commented by nandkumar gadkari on 27/07/19
								sql = sql +" AND SDET.TRAN_ID <> ?" ;// added by nandkumar gadkari on 27/07/19
								sql = sql + " GROUP BY SDET.INVOICE_ID";

								pstmt = conn.prepareStatement(sql);

								//Changed by Santosh on  16/05/2017
								//pstmt.setString(1, lsInvoiceId);
								pstmt.setString(1, docKey);
								pstmt.setString(2, itemCode);
								pstmt.setString(3, lsLotNo);
								pstmt.setString(4, siteCode);
								pstmt.setString(5, custCode);
								/*if(editFlag != null && "E".equalsIgnoreCase(editFlag))
									{
										pstmt.setString(6, tranId); 
										//Added by Santosh on 20/09/2017 for bug fix split qty validatoin in edit mode
										pstmt.setString(7, lineNo); 
									}*/// commented by nandkumar gadkari on 27/07/19
								tranId = tranId == null || tranId.trim().length() == 0 ? "  " : tranId;// added by nandkumar gadkari on 27/07/19
								pstmt.setString(6, tranId);// added by nandkumar gadkari on 27/07/19
								rs = pstmt.executeQuery();

								if(rs.next())
								{
									sdetQtyAdj = rs.getDouble("QTY_ADJ");
									//System.out.println("sdetQtyAdj>>>>>>>>>"+sdetQtyAdj);
								}

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

								//Changed by Santosh on 05/05/2017 to set 0 if qty_adj is null
								//sql = "SELECT QUANTITY, QUANTITY_ADJ FROM MIN_RATE_HISTORY WHERE DOC_KEY = ? ";
								sql = "SELECT QUANTITY, CASE WHEN QUANTITY_ADJ IS NULL THEN 0 ELSE QUANTITY_ADJ END AS QUANTITY_ADJ FROM MIN_RATE_HISTORY WHERE DOC_KEY = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, docKey);

								rs = pstmt.executeQuery();

								if(rs.next())
								{
									invoiceQty = rs.getDouble("QUANTITY");
									//System.out.println("INvoice quantity>>>>>>>>>"+invoiceQty);
									qtyAdj = rs.getDouble("QUANTITY_ADJ");
								}

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

								//Added by Santosh on 23/05/2017 to check if enetered inovice id is availbel for salesreturn[Start]
								if(lsInvoiceId != null && lsInvoiceId.trim().length()>0)
								{	
									//if((qtyAdj+sdetQtyAdj+domTotalQty)>0)
									if((qtyAdj+sdetQtyAdj+domTotalQty)> invoiceQty) //commented and added by rupali on 27/04/2021
									{
										errCode = "VTINVADJ";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
										//isValidInvoiceId = false; commented by nandkumar gadkari on 26/07/19
									}
								}
								//Added by Santosh on 23/05/2017 to check if enetered inovice id is availbel for salesreturn[End]
								System.out.println( "lcQty :: " + lcQty );
								System.out.println( "qtyAdj :: " + qtyAdj );
								System.out.println( "sdetQtyAdj :: " + sdetQtyAdj );
								System.out.println( "domTotalQty :: " + domTotalQty );
								System.out.println( "invoiceQty :: " + invoiceQty );
								//Changed by Santosh on 17/05/2017
								//if(lcQty > (invoiceQty - (qtyAdj+sdetQtyAdj+domTotalQty)))
								//Changed by wasim on 17-MAY-17 corrected validation condition
								//if(lcQty > (qtyAdj+sdetQtyAdj+domTotalQty))
								//Changed by Santosh on 18/05/2017 to correct condition
								//if(lcQty - (qtyAdj+sdetQtyAdj+domTotalQty) <= 0)
								//Changed by Santosh on 23/05/2017 to correct condition
								//if(lcQty > invoiceQty)
								//-------------NANDKUMAR GADKARI ON 15/10/18---------------START----------
								String docKeyvalue="";
								if (docKey.trim().length() > 0) {
									/*cnt = 0;
										do {
											docKeyStr = distCommon.getToken(docKey, ",");
											System.out.println( "docKeyStr :: " + docKeyStr );
											cnt++;

										} while (cnt <5);
										System.out.println( "cnt :: " + cnt );*/


									String[] docKeyStr = docKey.split(",");

									for(int i=0; i<docKeyStr.length; i++)
									{	
										//docKeyvalue = ;
										System.out.println( "docKeyStr :: " + docKeyStr[i]);
										System.out.println( "docKeyStrlength :: " + docKeyStr.length);
										cnt++;
									}
									System.out.println( "cnt :: " + cnt );
									if(cnt !=5)
									{
										isValidInvoiceId=false;
									}
								}
								//-------------NANDKUMAR GADKARI ON 15/10/18---------------END ----------
								if(isValidInvoiceId && (lcQty+(qtyAdj+sdetQtyAdj+domTotalQty)) > invoiceQty)
								{
									return getError((invoiceQty - (qtyAdj+sdetQtyAdj+domTotalQty)), "VTSPLTQTY", conn);
								}
								// Added  By mukesh Chauhan on 20/02/2020---- Start---- Not To allow duplicate doc key if not split
								//System.out.println("sales return fro>>>>>>>>>>>>>>>>");
								sql = "SELECT SDET.INVOICE_ID, SUM(SDET.QUANTITY) AS QTY_ADJ" 
										+ " FROM sreturn_form SRET, sreturn_form_det SDET"
										+ " WHERE SRET.TRAN_ID = SDET.TRAN_ID"
										+ " AND SRET.STATUS != 'S'"
										+ " AND SDET.INVOICE_ID IS NOT NULL"
										+ " AND SDET.DOC_KEY  = ?"
										+ " AND SDET.ITEM_CODE = ?"
										+ " AND SDET.LOT_NO = ?"     
										+ " AND SRET.SITE_CODE = ?"
										+ " AND SRET.CUST_CODE = ?"
										+ " AND SDET.TRAN_ID <> ?" 
										+ " GROUP BY SDET.INVOICE_ID";

								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, docKey);
								pstmt.setString(2, itemCode);
								pstmt.setString(3, lsLotNo);
								pstmt.setString(4, siteCode);
								pstmt.setString(5, custCode);
								// tranId = tranId == null || tranId.trim().length() == 0 ? "  " : tranId;
								// pstmt.setString(6, tranId);
								pstmt.setString(6, formNo);
								rs = pstmt.executeQuery();

								// System.out.println( "lcQty salesform :: " + lcQty );
								//System.out.println( "qtyAdj salesform:: " + qtyAdj );
								//System.out.println( "sdetQtyAdj salesform:: " + sdetQtyAdj );
								//System.out.println( "domTotalQty salesform:: " + domTotalQty );
								//System.out.println( "invoiceQty salesform:: " + invoiceQty );
								if(rs.next())
								{
									sdetFQtyAdj = rs.getDouble("QTY_ADJ");
									//System.out.println("sdetFQtyAdj in salesreturn form"+sdetFQtyAdj);
								}

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
								// double Result=(lcQty+(qtyAdj+sdetQtyAdj+domTotalQty+sdetFQtyAdj));
								System.out.println("lcQty :"+lcQty+" "+"qtyAdj :"+qtyAdj+" "+"sdetQtyAdj :"+sdetQtyAdj+" "+"domTotalQty :"+domTotalQty+" "+"sdetFQtyAdj :"+sdetFQtyAdj);
								// System.out.println("Result>>>>>>>>>>>"+Result);
								if(isValidInvoiceId && (lcQty+(qtyAdj+sdetQtyAdj+domTotalQty+sdetFQtyAdj)) > invoiceQty)
								{
									//System.out.println("Inside Error code");
									return getError((invoiceQty - (qtyAdj+sdetQtyAdj+domTotalQty+sdetFQtyAdj)), "VTSPLTQTY", conn);
								}
								// Added By mukesh Chauhan on 20/02/2020 ----- END----------------------------------------------
							}
						}
						//Added by Santosh on 02/12/2016 to split quantity in multiple line as per min_rate_history[End]

						if (llLineNoInvStr != null && llLineNoInvStr.indexOf(".") > 0)
						{
							llLineNoInvStr = llLineNoInvStr.substring(0,llLineNoInvStr.indexOf("."));
						}

						llLineNoInv = Integer.parseInt( llLineNoInvStr.trim() );			
						llLineNoInvtraceStr = getNumString( genericUtility.getColumnValue( "line_no__invtrace", dom ) );
						if (llLineNoInvtraceStr != null && llLineNoInvtraceStr.indexOf(".") > 0)
						{
							llLineNoInvtraceStr = llLineNoInvtraceStr.substring(0,llLineNoInvtraceStr.indexOf("."));
						}
						llLineNoInvtraceStr = llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim();
						llLineNoInvtrace = Integer.parseInt( llLineNoInvtraceStr.trim());
						lsTranId = genericUtility.getColumnValue( "tran_id", dom );
						if( lcQty < 0 )
						{
							errCode = "VTQTY01";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;													
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						lsFlag = getAbsString( genericUtility.getColumnValue( "ret_rep_flag", dom ) );

						if( lsFlag != null && "P".equalsIgnoreCase( lsFlag ) )
						{
							itemCode = genericUtility.getColumnValue( "item_code", dom );
							siteCode = genericUtility.getColumnValue( "site_code", dom1 ); //dw_edit.getitemstring(dw_edit.getrow(), "site_code")
							lsLotNo = genericUtility.getColumnValue( "lot_no", dom );//dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(), "lot_no")
							lsLotSl = genericUtility.getColumnValue( "lot_sl", dom );//dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(), "lot_sl")
							lsLoccode = genericUtility.getColumnValue( "loc_code", dom );//dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(), "loc_code")
							String mexpDateStr = genericUtility.getColumnValue( "exp_date", dom );//dw_detedit[ii_currformno].getitemdatetime(1, "exp_date")//Added by shahid 24/06/2009 for DI89SUN189
							if( mexpDateStr != null && mexpDateStr.trim().length() > 0 )
							{
								mexpDate = Timestamp.valueOf(genericUtility.getValidDateString( mexpDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

								llLineNoStr = getNumString( genericUtility.getColumnValue( "line_no", dom ) );//dw_detedit[ii_currformno].getitemnumber(1, "line_no")//Added by shahid 24/06/2009 for DI89SUN189
								if (llLineNoStr != null && llLineNoStr.indexOf(".") > 0)
								{
									llLineNoStr = llLineNoStr.substring(0,llLineNoStr.indexOf("."));
								}
								llLineNoStr = llLineNoStr == null || llLineNoStr.trim().length() == 0 ? "0" : llLineNoStr.trim();
								llLineNo = Integer.parseInt( llLineNoStr == null || llLineNoStr.trim().length() == 0 ? "0" : llLineNoStr.trim() );
								sqlStr = " Select	(a.quantity - case when a.alloc_qty is null then 0 else a.alloc_qty end - case when a.hold_qty is null then 0 else a.hold_qty end) lc_stkqty "
										+" from		stock a, invstat b "
										+" Where		a.inv_stat  = b.inv_stat "
										+" And		a.item_code = ? " //:ls_item_code
										+" And		a.site_code = ? " //:ls_site_code
										+" And		a.loc_code  = ? " //:ls_loccode
										+" And		a.lot_no    = ? " //:ls_lot_no
										+" And		a.lot_sl	 	= ? " //:ls_lot_sl  
										+" And		b.available = 'Y' ";
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, itemCode );
								pstmt.setString( 2, siteCode );
								pstmt.setString( 3, lsLoccode );
								pstmt.setString( 4, lsLotNo );
								pstmt.setString( 5, lsLotSl );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lcStkqty = rs.getDouble( "lc_stkqty" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( lsTranId == null )
								{
									lsTranId = " ";
								}
								sqlStr = " select ( case when quantity is null then 0 else quantity end ) lc_srqty "
										+" from 	sreturndet "
										+" 	where tran_id 	 	 = ? " //:ls_tran_id										
										+" and 	line_no      = ? "; //:ll_line_no ;

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsTranId );
								pstmt.setInt( 2, llLineNo );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lcSrqty = rs.getDouble( "lc_srqty" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if ( lcStkqty + lcSrqty < lcQty )
								{
									errCode = "VTSTKQTY";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;						
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								String mtrandateStr = genericUtility.getColumnValue( "tran_date", dom1 );
								mtrandate = Timestamp.valueOf(genericUtility.getValidDateString( mtrandateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
								sqlStr = " Select	min_shelf_life	mmin_shelf_life	from	item "
										+" Where item_code	=	? ";//:ls_item_code;

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, itemCode );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mminShelfLife = rs.getDouble( "mmin_shelf_life" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;						
								System.out.println( "mminShelfLife :: " + mminShelfLife ); 
								if( mminShelfLife > 0 )
								{
									mchkDate = distCommon.CalcExpiry( mtrandate, mminShelfLife );
									if( mchkDate.after( mexpDate ) )
									{
										errCode = "VTSHELF01";
										//errString = getErrorString( childNodeName, errCode, userId ); 
										//break;								
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}
						if( lsTranId == null )
						{
							lsTranId = " ";
						}
						if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
						{
							if( llLineNoInvtrace == 0 )
							{
								sqlStr = " select sord_no, sord_line_no " 
										+" from invoice_trace "
										+" where invoice_id	= ? " //:ls_invoice_id
										+"  and inv_line_no	= ? "; //:ll_line_no__inv ;

								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNoInv );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsSordNo = rs.getString( "sord_no" );
									lsLineNoSord = rs.getString( "sord_line_no" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;													
							}
							else
							{
								sqlStr = " select sord_no, sord_line_no "
										+" from invoice_trace "
										+" where invoice_id  = ? "//:ls_invoice_id
										+" and inv_line_no	= ? " //:ll_line_no__inv
										+" and line_no 		= ? ";//:ll_line_no__invtrace;
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, lsInvoiceId );
								pstmt.setInt( 2, llLineNoInv );
								pstmt.setInt( 3, llLineNoInvtrace );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsSordNo = rs.getString( "sord_no" );
									lsLineNoSord = rs.getString( "sord_line_no" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;													
							}
							sqlStr = " select sum((case when quantity is null then 0 else quantity end) * (case when rate is null then 0 else rate end)) lc_invtrace_amt "
									+" from  invoice_trace "
									+" where	invoice_id 	 = ? " //:ls_invoice_id
									+" and	sord_no 		 = ? " //:ls_sord_no
									+" and	sord_line_no = ? "; //:ls_line_no_sord;

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsInvoiceId );
							pstmt.setString( 2, lsSordNo );
							pstmt.setString( 3, lsLineNoSord );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lcInvtraceAmt = rs.getDouble( "lc_invtrace_amt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;													

							lcAdjAmt = 0;
							sqlStr = " select (sum((case when b.quantity is null then 0 else b.quantity end) * (case when b.rate is null then 0 else b.rate end))) lc_adj_amt "
									+" from sreturn a , sreturndet b "
									+" where  a.tran_id    		<> ? "//:ls_tran_id
									+" and	 b.tran_id     	=  a.tran_id "
									+" and   b.invoice_id 		= 	? "//:ls_invoice_id
									+" and	 b.line_no__invtrace	in ( select line_no from invoice_trace "
									+" 	where invoice_id = ? " //:ls_invoice_id
									+" 		and sord_no = ? " //:ls_sord_no
									+" 		and sord_line_no = ? ) "; //:ls_line_no_sord) ;
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsTranId );
							pstmt.setString( 2, lsInvoiceId );
							pstmt.setString( 3, lsInvoiceId );
							pstmt.setString( 4, lsSordNo );
							pstmt.setString( 5, lsLineNoSord );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lcAdjAmt = rs.getDouble( "lc_adj_amt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							System.out.println( "lcAdjAmt :: " + lcAdjAmt );
							System.out.println( "lcQty :: " + lcQty );
							System.out.println( "lcRate :: " + lcRate );
							lcAmount = (lcQty * lcRate) + lcAdjAmt; // 22/04/10 manoharan uncommented
							//lcAmount = getReqDecimal(lcAmount,2); // 23/04/10 manoharan amount is coming in many decimals
							//lcInvtraceAmt = getReqDecimal(lcInvtraceAmt,2); // 15/11/10 manoharan changes done as per mail by KB (2 decimal is ok)
							// changed to 3 decimail as was giving problem in certain cases
							lcAmount = getReqDecimal(lcAmount,3); // 23/04/10 manoharan amount is coming in many decimals
							lcInvtraceAmt = getReqDecimal(lcInvtraceAmt,3); // 15/11/10 manoharan changes done as per mail by KB (2 decimal is ok)
							//lcAmount = getTotAmt( dom2 ) + lcAdjAmt; // 22/04/10 manoharan commented
							System.out.println( "lcInvtraceAmt :: " + lcInvtraceAmt );
							System.out.println( "lcAmount :: " + lcAmount );
							// 13/10/14 manoharan  allow variance of 1 paise
							//if( lcAmount > lcInvtraceAmt )
							//added by rupali on 01/04/2021 [start]
							String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
							String historyType = "";
							sql = "SELECT HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
									+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, docKey);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								historyType = checkNull(rs.getString("HISTORY_TYPE"));
							}
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
							System.out.println("inside historyType::::["+historyType+"]");
							if(!"S".equalsIgnoreCase(historyType))
							//added by rupali on 01/04/2021 [end]
							{
								if( (lcAmount > lcInvtraceAmt) &&  ((lcAmount - lcInvtraceAmt) > .01) )
								{
									errCode = "VTCRAMT";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;	
									errList.add( errCode );											
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					}

					// Added by Mahesh Saggam on 05/07/2019 start [if conv_qty__stduom is 1 then quantiy and quantity__stduom should be same]

					if( childNodeName.equalsIgnoreCase( "conv__qty_stduom" ) )
					{

						convQtyStduomStr = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));

						if(convQtyStduomStr != null && convQtyStduomStr.trim().length() > 0)
						{
							convQtyStduom = convQtyStduomStr == null ? 0 : Double.parseDouble(convQtyStduomStr);
						}

						if(convQtyStduom == 1)
						{

							qtyStr = checkNull(genericUtility.getColumnValue("quantity", dom));
							qtyStdStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));	
							System.out.println("quantity = "+qtyStr+" std quantity = "+qtyStdStr);

							if (qtyStr != null && qtyStr.trim().length() > 0) 
							{
								dqty = Double.parseDouble(qtyStr);	
							}
							if (qtyStdStr != null && qtyStdStr.trim().length() > 0) 
							{
								dqtyStduom = Double.parseDouble(qtyStdStr);
							}
							if(dqty != dqtyStduom)
							{
								System.out.println("Error occured in validating quantity");
								errCode = "VTINVQTTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if( childNodeName.equalsIgnoreCase( "conv__rtuom_stduom" ) ) // [if conv_rate__stduom is 1 then rate and rate__stduom should be same]
					{
						String convRtuomStduomStr = "", rateStr = "", ratestdStr = "";
						double convRtuomStduom = 0,pordRate = 0, stdRate = 0;

						convRtuomStduomStr = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));

						if(convRtuomStduomStr != null && convRtuomStduomStr.trim().length() > 0)
						{
							convRtuomStduom = convRtuomStduomStr == null ? 0: Double.parseDouble(convRtuomStduomStr);
						}

						if(convRtuomStduom == 1)
						{

							rateStr = checkNull(genericUtility.getColumnValue("rate", dom));
							ratestdStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));	
							System.out.println("rate = "+rateStr+" std rate = "+ratestdStr);

							if (rateStr != null && rateStr.trim().length() > 0) 
							{	
								pordRate = Double.parseDouble(rateStr);	
							}
							if (ratestdStr != null && ratestdStr.trim().length() > 0) 
							{
								stdRate = Double.parseDouble(ratestdStr);
							}
							if(pordRate != stdRate)
							{
								System.out.println("Error occured in validating rate");
								errCode = "VTINVRTE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					// Added by Mahesh Saggam on 05/07/2019 [End]

					if( childNodeName.equalsIgnoreCase( "physical_qty" ) )
					{  
						lsSretinvqty = getAbsString( distCommon.getDisparams("999999","SRET_INV_QTY",conn) );//gf_getenv_dis('999999','SRET_INV_QTY')
						//System.out.println( "lsSretinvqty :1: " + lsSretinvqty );
						if( "P".equalsIgnoreCase( lsSretinvqty ) )
						{
							lsFlag = getAbsString( genericUtility.getColumnValue( "ret_rep_flag", dom ) );
							//System.out.println( "lsFlag :89: " + lsFlag );
							if( "R".equalsIgnoreCase( lsFlag ) )
							{
								String lcQtyStr = genericUtility.getColumnValue( "quantity__stduom", dom );
								lcQty = Double.parseDouble( lcQtyStr == null || lcQtyStr.trim().length() == 0 || "null".equalsIgnoreCase( lcQtyStr.trim() ) ? "0" : lcQtyStr.trim() );
								String lcPhysicalqtyStr = genericUtility.getColumnValue( "physical_qty", dom );//dw_detedit[ii_currformno].getitemnumber(1,'physical_qty')
								lcPhysicalqty = Double.parseDouble( lcPhysicalqtyStr == null || lcPhysicalqtyStr.trim().length() == 0 || "null".equalsIgnoreCase( lcPhysicalqtyStr.trim() ) ? "0" : lcPhysicalqtyStr.trim() );
								//System.out.println( "lcPhysicalqty :89: " + lcPhysicalqty );
								//System.out.println( "lcQty :89: " + lcQty );
								if( lcQty != lcPhysicalqty )
								{
									String  lsVarpercVal = distCommon.getDisparams("999999","SRET_PHYQTY_VAR_PERCENT",conn);//gf_getenv_dis('999999','SRET_PHYQTY_VAR_PERCENT')
									//System.out.println( "lcQty :89: " + lcQty );

									//System.out.println( " SRET_PHYQTY_VAR_PERCENT  = "+ lsVarpercVal );

									if( lsVarpercVal == null || lsVarpercVal.trim().length() == 0 )
									{
										System.out.println( " lsVarpercVal == null " );
										lsVarpercVal = "0";
									}
									lsVarperc = Double.parseDouble( lsVarpercVal );
									//System.out.println( " lcQty  = "+ lcQty );
									//System.out.println( " lsVarperc  = "+ lsVarperc);
									//System.out.println( "Math.abs( lcQty - lcPhysicalqty ) = "+ Math.abs( lcQty - lcPhysicalqty ));
									//System.out.println( " lcQty * lsVarperc / 100 = "+ lcQty * lsVarperc / 100);
									if( Math.abs( lcQty - lcPhysicalqty ) > ( lcQty * lsVarperc / 100 ) )
									{
										errCode = "VTPHYQTYVR";
										//errString = getErrorString( childNodeName, errCode, userId ); 
										//break;							
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}					
					}
					else if( childNodeName.equalsIgnoreCase( "cust_item__ref" ) )
					{
						itemCode =	genericUtility.getColumnValue( "cust_item__ref", dom );
						custCode	 = genericUtility.getColumnValue( "cust_code", dom1 );		
						//Changed by Dadaso on 22/04/15 [trim cust code ]
						custCode = custCode == null ? "" : custCode.trim(); 
						if( itemCode != null && itemCode.trim().length() > 0 && !"null".equals(itemCode))
						{
							//Changed by Dadaso on 22/04/15 [trim itemCode ]
							itemCode = itemCode.trim();
							sqlStr = " select count(*) mitem_cnt from customeritem "
									+" where  cust_code 	  = ? " //:custCode
									+" and item_code__ref = ? "; //:ls_item_code	;

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, custCode );							
							pstmt.setString( 2, itemCode );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mitemCnt = rs.getInt( "mitem_cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;																				

							if( mitemCnt == 0 )
							{
								errCode = "VTCUSTITEM";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;							
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );

							}
						}
					}
					//validation added by Nandkumar gadkari on 16/11/18-------------------------Start---------------------
					else if( childNodeName.equalsIgnoreCase( "cost_rate" ) )
					{
						String lcRateStr =	genericUtility.getColumnValue( "cost_rate", dom );
						if(lcRateStr==null || "null".equalsIgnoreCase(lcRateStr.trim()) || lcRateStr.trim().length()==0)
						{
							errCode = "VTITCOSTR";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							lcRate = Double.parseDouble( lcRateStr == null || lcRateStr.trim().length() == 0 ? "0" : lcRateStr.trim() );
						}
						if( lcRate < 0 )
						{
							errCode = "VTITCOSTR";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					////validation added by Nandkumar gadkari on 16/11/18-------------------------end---------------------
					//validation added by Nandkumar gadkari on 13/01/20-------------------------Start---------------------
					else if( childNodeName.equalsIgnoreCase( "doc_key" ) )
					{
						docKeyS =	genericUtility.getColumnValue( "doc_key", dom );
						if(docKeyS !=null &&  docKeyS.trim().length() > 0)
						{

							sqlStr = " select count(*)  from min_rate_history  where  doc_key 	  = ?  and status = ? "; 

							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, docKeyS );							
							pstmt.setString( 2, "X" );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								mitemCnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(mitemCnt > 0)
							{
								errCode = "VTDOCKSTCL";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}

					}
					////validation added by Nandkumar gadkari on 13/01/20-------------------------end---------------------

				} // end for
				break;					
			case 3 :
				System.out.println( "Detail 3 Validation called " );
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				//String itemValue = "",lineNo = "",lineValue= "",updateFlag = "";
				//int lineNoInt = 0,lineValueInt = 0;
				//NodeList itemNodeList = null,lineNoList = null,detail2List = null,childDetilList = null;

				//Node itemNode = null,lineNoNode = null,detailNode = null,chidDetailNode = null;
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if( childNodeName.equalsIgnoreCase( "adj_amt" ) )
					{
						String lcAdjAmtStr = genericUtility.getColumnValue( "adj_amt", dom );
						lcAdjAmt = Double.parseDouble( lcAdjAmtStr == null || lcAdjAmtStr.trim().length() == 0 ? "0" : lcAdjAmtStr.trim() );
						lsRefSer = genericUtility.getColumnValue( "ref_ser", dom ); //dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"ref_ser")
						lsRefNo = genericUtility.getColumnValue( "ref_no", dom ); //dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"ref_no")
						lsItemSer =  genericUtility.getColumnValue( "item_ser", dom1 );// dw_header.getitemString(dw_header.getrow(),"ITEM_SER")
						custCode = genericUtility.getColumnValue( "cust_code", dom1 );//dw_header.getitemString(dw_header.getrow(),"cust_code")
						lsAdjMiscCrn = genericUtility.getColumnValue( "adj_misc_crn", dom1 ); //dw_header.getitemString(dw_header.getrow(),"adj_misc_crn")

						if( "MC".equalsIgnoreCase( lsAdjMiscCrn ) )
						{
							sqlStr = " select (case when tot_amt is null then 0 else tot_amt end) - (case when adj_amt is null then 0 else adj_amt end) lc_bal_amt "
									+"	from receivables "
									+" where tran_ser = ? " //:ls_ref_ser 
									+"	and ref_no = ? " //:ls_ref_no 
									+"	and cust_code = ? "; //:ls_cust_code;
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsRefSer );
							pstmt.setString( 2, lsRefNo );
							pstmt.setString( 3, custCode );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lcBalAmt = rs.getDouble( "lc_bal_amt" );
							}
							else
							{
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								errCode = "VTNSUCHRNA";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;							
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;

						}else if( "MI".equalsIgnoreCase( lsAdjMiscCrn ) )
						{
							sqlStr = " select (case when tot_amt is null then 0 else tot_amt end) - (case when adj_amt is null then 0 else adj_amt end) lc_bal_amt "
									+" from receivables "
									+"	where tran_ser = ? " //:ls_ref_ser 
									+"		and ref_no = ? " //:ls_ref_no 
									+"		and item_ser = ? " //:ls_item_ser 
									+"		and cust_code = ? "; //:ls_cust_code;
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsRefSer );
							pstmt.setString( 2, lsRefNo );
							pstmt.setString( 3, lsItemSer );
							pstmt.setString( 4, custCode );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lcBalAmt = rs.getDouble( "lc_bal_amt" );
							}
							else
							{
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								errCode = "VTNSUCHRNI";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;							
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							if( rs != null )
								rs.close();
							rs = null;
							if( pstmt != null )
								pstmt.close();
							pstmt = null;

						}
						if( lcBalAmt < lcAdjAmt )
						{
							errCode = "VTBAL1";
							//errString = getErrorString( childNodeName, errCode, userId ); 
							//break;						
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						String gsRunMode = "I";
						if ( !"B".equalsIgnoreCase( gsRunMode ) )
						{
							lsTranId = genericUtility.getColumnValue( "tran_id", dom1 );
							String lcAmountStr = genericUtility.getColumnValue( "net_amt", dom1 ); //dw_header.getitemnumber(dw_header.getrow(),"NET_AMT");
							lcAmount = Double.parseDouble( lcAmountStr == null || lcAmountStr.trim().length() == 0 ? "0" : lcAmountStr.trim() );

							String liLineNoStr = getNumString( genericUtility.getColumnValue("line_no", dom ) );
							if (liLineNoStr != null && liLineNoStr.indexOf(".") > 0)
							{
								liLineNoStr = liLineNoStr.substring(0,liLineNoStr.indexOf("."));
							}

							liLineNo = Integer.parseInt( liLineNoStr == null || liLineNoStr.trim().length() == 0 ? "0" : liLineNoStr.trim() );
							sqlStr = " select ( case when sum(adj_amt) is null then 0 else sum(adj_amt) end ) lc_sum_adj "
									+"	from sreturn_inv "
									+" where tran_id = ? " //:ls_tran_id 
									+"	and line_no <> ? ";//:li_line_no;
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1, lsTranId );
							pstmt.setInt( 2, liLineNo );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lcSumAdj = rs.getDouble( "lc_sum_adj" );

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( ( lcSumAdj  + lcAdjAmt ) > lcAmount )
							{
								errCode = "VTHAILTSDA";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;								
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
				}// end for
			} //END switch

			int errListSize = errList.size();
			cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					//String errMsg = hashMap.get(errCode)!=null ? hashMap.get(errCode).toString():"";
					//System.out.println("errMsg .........."+errMsg);
					errString = getErrorString( errFldName, errCode, userId );
					errorType =  errorType( conn , errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString =bifurErrString;//+"<trace>"+errMsg+"</trace>";
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;

				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}
		}//END TRY
		catch(Exception e)
		{
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException( e );
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					if(rs1 != null )rs.close();
					rs = null;
					rs1 = null;
					if(pstmt != null )pstmt.close();
					pstmt =null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException( d );
			}
		}

		errString = errStringXml.toString();

		return errString;
	}//END OF VALIDATION
	private String errorType( Connection conn , String errorCode )
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";

			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, checkNull(errorCode));			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}		
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}

	public String getPriceListType(String priceList, Connection conn) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String listType = null;
		String sql = null;

		try
		{
			sql = "select list_type from pricelist where price_list = ? ";
			//System.out.println("select qry from list_type.." + sql);
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1,priceList);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
				listType = rs.getString(1) == null ? "" : rs.getString(1);
			}
			//System.out.println("priceList .." + priceList);
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
		}
		catch(Exception e)
		{
			//System.out.println("Exception...[getPriceListType] "+sql+e.getMessage());
			e.printStackTrace();
			throw new ITMException( e );
		}
		return listType;
	}
	private String getObjName(Node node) throws Exception
	{
		String objName = null;
		NodeList nodeList = null;
		Node detaulNode = null;
		Node detailNode = null;
		nodeList = node.getChildNodes();
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem( "objName" ).getNodeValue();
		/*
		for(int ctr = 0; ctr < nodeList.getLength(); ctr++ )
		{
			detailNode = nodeList.item(ctr);
			if(detailNode.getNodeName().equalsIgnoreCase("attribute") )
			{
				objName = detailNode.getAttributes().getNamedItem("objName").getNodeValue();
			}
		}
		 */
		return "w_" + objName;

	}
	private double getCostRate(HashMap infoMap, Connection conn) throws Exception
	{

		DistCommon distCommon = new DistCommon();	
		String retReplFlag = null, sql = "";
		String itemCode = null;
		String siteCode = null;
		String locCode = null, locCodeDesp = null, despLine = null;
		String lotNo = null;
		String lotSl = null;
		String priceList = null;
		String rateType = null;
		String tranDate = null;
		String invoiceId = null;
		String lineNoTrace = null;
		int iLineNoTrace = 0;
		double costRate = 0;
		double dCostRate = 0.0;
		double qtyStdUom = 0.0;
		String despId = null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{

			retReplFlag = ( infoMap.get("ret_repl_flag") == null ? null : infoMap.get("ret_repl_flag").toString() );
			itemCode = ( infoMap.get("item_code") == null ? null : infoMap.get("item_code").toString() );
			siteCode = ( infoMap.get("site_code") == null ? null : infoMap.get("site_code").toString() );
			locCode = ( infoMap.get("loc_code") == null ? null : infoMap.get("loc_code").toString() );
			lotNo = ( infoMap.get("lot_no") == null ? null : infoMap.get("lot_no").toString() );
			lotSl = ( infoMap.get("lot_sl") == null ? null : infoMap.get("lot_sl").toString() );
			tranDate = ( infoMap.get("tran_date") == null ? null : infoMap.get("tran_date").toString() ); 
			invoiceId = ( infoMap.get("invoice_id") == null ? null : infoMap.get("invoice_id").toString() ); 
			lineNoTrace = ( infoMap.get("line_no__invtrace") == null ? null : infoMap.get("line_no__invtrace").toString() );  
			qtyStdUom = Double.parseDouble( ( infoMap.get("quantity__stduom") == null ? "0" : infoMap.get("quantity__stduom").toString() ) );


			if ("P".equals(retReplFlag))
			{
				sql = " select rate "
						+"	from stock "
						+"	where item_code = ? "
						+"		and site_code = ? "
						+"		and loc_code = ? "
						+"		and lot_no = ? "
						+"		and lot_sl = ? ";

				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,locCode);
				pstmt.setString(4,lotNo);
				pstmt.setString(5,lotSl);
				rs = pstmt.executeQuery(); 
				if(rs.next())
				{
					costRate = rs.getDouble(1);
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;
			}
			else if (invoiceId != null && invoiceId.trim().length() > 0 )
			{
				sql = " select desp_id, desp_line_no "
						+ "  from invoice_trace where invoice_id 	= ? and line_no = ?";
				lineNoTrace = lineNoTrace == null ? "0" : lineNoTrace.trim();
				if (lineNoTrace != null && lineNoTrace.indexOf(".") > 0)
				{
					lineNoTrace = lineNoTrace.substring(0,lineNoTrace.indexOf("."));
				}
				lineNoTrace = lineNoTrace == null || lineNoTrace.trim().length() == 0 ? "0" : lineNoTrace.trim();
				iLineNoTrace = Integer.parseInt(lineNoTrace);
				despId = "";		
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,invoiceId);
				pstmt.setInt(2,iLineNoTrace);
				rs = pstmt.executeQuery(); 
				if(rs.next())
				{
					despId = rs.getString(1);
					despLine = rs.getString(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (despId.trim().length() > 0 )
				{
					sql = " select loc_code, cost_rate "
							+ "  from 	despatchdet where desp_id 	= ? and line_no = ?";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,despId);
					pstmt.setString(2,despLine);
					rs = pstmt.executeQuery(); 
					if(rs.next())
					{
						locCodeDesp = rs.getString(1);
						costRate = rs.getDouble(2);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					dCostRate = costRate;
					if ( dCostRate == 0 )
					{
						sql = " select rate from stock where item_code = ? and site_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";

						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCode);
						pstmt.setString(3,locCodeDesp);
						pstmt.setString(4,lotNo);
						pstmt.setString(5,lotSl);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
							costRate = rs.getDouble(1);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

					}
				}
			}
			else
			{
				rateType = distCommon.getDisparams("999999","INV_ACCT_RATE",conn);
				if ("S".equals(rateType))
				{
					priceList = distCommon.getDisparams("999999","STD_SO_PL",conn);
				}
				else if  ("A".equals(rateType))
				{
					priceList = distCommon.getDisparams("999999","SRETCOST_PLIST",conn);
				}
				if (priceList != null && !"NULLFOUND".equals(priceList) && priceList.trim().length() > 0)
				{
					tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;
					itemCode = itemCode == null ? "" : itemCode;
					lotNo = lotNo == null ? "" : lotNo;
					costRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"L",qtyStdUom, conn);
				}
			}
			distCommon = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		return costRate;
	}
	private StringBuffer getMinRate(Document dom, Document dom1, String currCol, StringBuffer sBuff, Connection conn) throws Exception
	{
		DistCommon distCommon = new DistCommon();	
		String invoiceId = null, retReplFlag = null, sql = "";
		String itemCode = null;
		String siteCode = null;
		String lotNo = null;
		String priceList = null;
		double minRate = 0;
		int noSchemeHist = 1;
		String sNoSchemeHist = null;
		String schemeKey = null;
		String varValue = null;
		String col[] = new String[500];
		boolean dynamicCol = true;
		boolean colMatch = false;
		String tranDate = null;
		String unitRate = null;
		double qtyStdUom = 0;
		String unitStd = null; 
		String colName = null;
		String docKey = null;
		String docValue = null;
		int pos;			
		String strToken;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			System.out.println(">>>>>>>>>>>>>In getMinRate currCol:"+currCol);
			System.out.println(">>>>>>>>>>>>>In getMinRate sBuff:"+sBuff);
			invoiceId = genericUtility.getColumnValue("invoice_id",dom1);
			System.out.println( "invoiceId :: " + invoiceId );
			if (invoiceId != null && invoiceId.trim().length() > 0 )
			{
				//Commented by Santosh on 06/12/2016 [Start]
				//return sBuff;
			}
			retReplFlag = genericUtility.getColumnValue("ret_rep_flag",dom);
			System.out.println( "retReplFlag :: " + retReplFlag );
			if (!"R".equals(retReplFlag) )
			{
				System.out.println( "return 1");
				return sBuff;
			}
			sNoSchemeHist = distCommon.getDisparams("999999","SCHEME_HIST_NUM",conn);
			System.out.println( "sNoSchemeHist :: " + sNoSchemeHist );

			if ( "NULLFOUND".equals( sNoSchemeHist ) )
			{
				col[0] = "site_code";
				col[1] = "item_code";
				col[2] = "lot_no";
				noSchemeHist = 1;
				dynamicCol = false;
			}
			else
			{
				int colFound = 0;
				noSchemeHist =  Integer.parseInt( sNoSchemeHist );
				for (int ctr = 1; ctr <= noSchemeHist;  ctr++)
				{
					schemeKey = "SCHEME_HIST_KEY" + ctr ;
					varValue = distCommon.getDisparams("999999",schemeKey,conn);
					System.out.println( "currCol :: " + currCol );
					System.out.println( "varValue :: " + varValue );
					if (varValue.indexOf(currCol) > -1 )
					{
						colMatch = true;
						colFound = 1;
						break;
					}
				}
				if (!colMatch)
				{
					System.out.println( "return 2");
					return sBuff;
				}
			}
			priceList = genericUtility.getColumnValue("price_list",dom1);
			itemCode = genericUtility.getColumnValue("item_code",dom);
			lotNo = genericUtility.getColumnValue("lot_no",dom);
			tranDate = genericUtility.getColumnValue("tran_date",dom1);
			//Changed and Added by Santosh on 06/12/2016 for null pointer issue [Start]
			//qtyStdUom = Double.parseDouble(genericUtility.getColumnValue("quantity__stduom",dom));
			String qtyStdUomStr = genericUtility.getColumnValue("quantity__stduom",dom)== null ? "0.0" :genericUtility.getColumnValue("quantity__stduom",dom);
			qtyStdUom = Double.parseDouble(qtyStdUomStr);
			////Changed and Added by Santosh on 06/12/2016 for null pointer issue [End]
			unitRate = genericUtility.getColumnValue("unit__rate",dom);
			unitStd = genericUtility.getColumnValue("unit__std",dom);

			unitRate = unitRate == null ?"" : unitRate.trim();
			unitStd = unitStd == null ?"" : unitStd.trim();

			for (int ctr = 1; ctr <= noSchemeHist ;  ctr++)
			{
				docKey = null;
				int colCount = -1;
				if (dynamicCol)
				{
					colCount = -1;
					schemeKey = "SCHEME_HIST_KEY" + ctr ;
					System.out.println(">>>>>>>>>In for loop schemeKey:"+schemeKey);
					varValue = distCommon.getDisparams("999999",schemeKey,conn);
					System.out.println(">>>>>>>>>In for loop afer schemeKey varValue:"+varValue);
					if ("NULLFOUND".equals( varValue ))
					{
						System.out.println( "return 3");
						return sBuff;
					}
					else
					{
						varValue = varValue.trim();

						while (varValue.trim().length()>0)
						{
							colCount++;
							pos = varValue.indexOf(",");
							if (pos > -1)
							{
								strToken = distCommon.getToken( varValue, "," );
								col[colCount] = strToken ; 
								varValue = varValue.substring(pos+1);
							}
							else
							{
								col[colCount] = varValue;
								break;
							}
						} // populate column list
					}
				}
				else
				{
					colCount = 2;
				}
				for (int colCtr = 0; colCtr <= colCount; colCtr++)
				{
					colName = col[colCtr];		
					System.out.println(">>>>>>>>>>>colName for getting value:"+colName);
					if ( "site_code".equalsIgnoreCase(colName.trim()) || "invoice_id".equalsIgnoreCase(colName.trim()) || "cust_code".equalsIgnoreCase(colName.trim()) )
					{
						docValue = 	genericUtility.getColumnValue(colName.trim(),dom1);
					}
					// commented by manoharan 17-mar-2021 column renamed to cust_code__bil
					//else if("cust_code__bill".equalsIgnoreCase(colName.trim())) //condition added by sagar on 23/01/15
					else if("cust_code__bil".equalsIgnoreCase(colName.trim())) //condition added by sagar on 23/01/15
					{
						docValue = 	genericUtility.getColumnValue(colName.trim(),dom1);
					}
					else
					{
						docValue = 	genericUtility.getColumnValue(colName.trim(),dom);
					}
					System.out.println(">>>>>>>>>>>In for loop docValue:"+docValue);
					if (docKey != null && docKey.trim().length() > 0)
					{
						docKey = docKey + "," + ( docValue == null || docValue.trim().length() == 0 ? "" : docValue.trim() ); // 13/05/10 manoharan docValue trim() added
					}
					else
					{
						//Changed by Santosh on 07/12/2016 to trim
						//docKey = docValue;

						//Modified by Anjali R. on [01/04/2019][To check null for doc value.][Start]
						//docKey = docValue.trim();
						docKey = ( docValue == null  ? "" : docValue.trim() );
						//Modified by Anjali R. on [01/04/2019][To check null for doc value.][End]
					}
				}
				System.out.println(">>>>>>>>>>final docKey:"+docKey);
                //Added and Commented by Rohini T on [18/11/2020][Start]
				System.out.println( "invoiceId from detail dom....:: " + invoiceId );
				if (invoiceId  != null && invoiceId.trim().length() > 0 && docKey.indexOf(invoiceId) != -1)
				{
				    //sql = " select eff_cost from min_rate_history where doc_key = ?" ;//Added and Commented by Rohini T on [18/11/2020]
                    sql = "select eff_cost from min_rate_history where invoice_id = ? and case when history_type is null then 'G' else history_type end in ('S','G') and doc_key = ? ";
				    pstmt= conn.prepareStatement(sql);
				    //pstmt.setString(1,docKey);//Added and Commented by Rohini T on [18/11/2020]
                    pstmt.setString(1,invoiceId);
				    pstmt.setString(2,docKey);
				    rs = pstmt.executeQuery(); 
				    if(rs.next())
				    {
					    minRate = rs.getDouble(1);
				    }
				    rs.close();
				    pstmt.close();
				    pstmt = null;
				    rs = null;
                }
				else
				{
					sql = "select eff_cost from min_rate_history where invoice_id = ? and case when history_type is null then 'G' else history_type end ='G' and doc_key = ? ";
					pstmt= conn.prepareStatement(sql);
					pstmt.setString(1,invoiceId);
					pstmt.setString(2,docKey);
					rs = pstmt.executeQuery(); 
					if(rs.next())
					{
						minRate = rs.getDouble(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				}
				//Added and Commented by Rohini T on [18/11/2020][End]
				if (minRate > 0)
				{
					System.out.println(">>>>>>>>>>Break if (minRate > 0) @ ctr Pos:"+ctr);
					break;
				}
			}
			System.out.println(">>>>>>>>after for loop minRate:"+minRate);

			//Changed by wasim on 10-07-2017 for not allow to take rate from min rate hostory [START]
			String udfStr1 = checkNull(genericUtility.getColumnValue("udf_str1",dom1));
			System.out.println("MIN_RATE_HIST_NO ["+udfStr1+"]");
			if("MIN_RATE_HIST_NO".equalsIgnoreCase(udfStr1))
			{
				minRate = 0;
			}
			//Changed by wasim on 10-07-2017 for not allow to take rate from min rate hostory [END]

			if (minRate == 0)
			{	
				tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;
				itemCode = itemCode == null ? "" : itemCode;
				lotNo = lotNo == null ? "" : lotNo;
				minRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
				System.out.println(">>>>>>>>>>minRate==0 then:"+minRate);
			}
			ArrayList convList = null;


			if ( !unitRate.equalsIgnoreCase( unitStd ) )
			{
				double fact = 1;
				convList = distCommon.getConvQuantityFact(unitStd, unitRate, itemCode, minRate, fact, conn);
			}
			else
			{
				convList = new ArrayList();
				convList.add(Double.toString(1));
				convList.add(Double.toString(minRate));

			}
			distCommon = null;
			sBuff.append("<rate>").append(minRate).append("</rate>");
			sBuff.append("<conv__rtuom_stduom>").append(convList.get(0)).append("</conv__rtuom_stduom>");
			sBuff.append("<rate__stduom>").append(convList.get(1)).append("</rate__stduom>");
			//Commneted by Santosh on 16/05/2017
			//Added by Santosh on 28/04/2017 to store doc_key
			//sBuff.append("<doc_key><![CDATA[").append(docKey).append("]]></doc_key>");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		System.out.println( "return 4");
		System.out.println( ">>> final return sBuff :: " + sBuff.toString() ); 
		return sBuff ;
	}
	private double itemValue2Quantity(String siteCode, String itemCode, String priceList, double netAmt, Connection conn) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double quantity = 0, stkQty = 0, allocQty = 0, balAmt = 0, qtyVal = 0, conv = 0, minShelfLife = 0, rate = 0;		
		int cnt = 0;
		String lotNo = null, lotSl = null, unit = null, unitPack = null;
		java.sql.Timestamp expDate = null, chkDate = null, today = null;
		String asItemCode = null;
		String unitFrom = null, unitTo = null;
		DistCommon distCommon = new DistCommon();	
		java.util.Date date = null;
		ArrayList convList = null;
		double packSize = 0;
		String sql = null;
		int prec = 3;


		try
		{
			today = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(today.toString());
			today = Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

			date = null;

			sql = " select min_shelf_life, unit from item where item_code = ?" ;
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
				minShelfLife = rs.getDouble(1);
				unit = rs.getString(2);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
			sql = " select count(a.item_code) from stock a, invstat b "
					+ " where a.inv_stat = b.inv_stat "
					+ " and a.item_code = ? "
					+ " and a.site_code = ? "
					+ " and a.quantity > 0 "
					+ " and b.available = 'Y' ";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
			if (cnt == 0)
			{
				return 0;
			}
			sql = " select a.lot_no, a.lot_sl, a.quantity, a.exp_date, a.alloc_qty "
					+ " from stock a, invstat b "
					+ " where a.inv_stat = b.inv_stat "
					+ " and a.item_code = ? "
					+ " and a.site_code = ? "
					+ " and a.quantity > 0 "
					+ " and b.available = 'Y' "
					+ " order by a.exp_date, a.lot_no, a.lot_sl ";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery();
			balAmt = netAmt ;
			UtilMethods utilMethods = UtilMethods.getInstance(); 
			while (rs.next())
			{
				lotNo = rs.getString(1);
				lotSl = rs.getString(2);
				stkQty = rs.getDouble(3);
				expDate = rs.getTimestamp(4);
				allocQty = rs.getDouble(5);
				if (minShelfLife > 0)
				{

					chkDate = distCommon.CalcExpiry(today,minShelfLife);
					if (chkDate.compareTo(expDate) > 0)
					{
						continue;
					}
					itemCode = itemCode == null ? "" : itemCode;
					lotNo = lotNo == null ? "" : lotNo;
					rate = distCommon.pickRate(priceList, genericUtility.getValidDateString( today.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ), itemCode, lotNo, "D", conn);
					if (rate <= 0)
					{
						continue;
					}
					System.out.println("netAmt :: "+netAmt);
					System.out.println("rate :: "+rate);
					System.out.println("stkQty :: "+stkQty);
					System.out.println("balAmt :: "+balAmt);
					qtyVal = balAmt / rate;
					if (qtyVal > stkQty)
					{ 
						balAmt = balAmt - (stkQty * rate);
						balAmt = getRequiredDecimal( balAmt, prec );
						quantity = quantity + stkQty;

						quantity = getRequiredDecimal( quantity, prec );
					}
					else
					{
						quantity = quantity + qtyVal;
						quantity = getRequiredDecimal( quantity, prec );
						break;
					}
					System.out.println("quantity :: "+quantity);
				}
			}
			System.out.println("quantity :: "+quantity);
			quantity = getReqDecimal(quantity,prec);
			System.out.println("quantity after :: "+quantity);
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
			sql = " select unit__pack from item_lot_packsize "
					+ " where item_code = ? "
					+ " and lot_no__from <= ? "
					+ " and lot_no__to >= ? " ;
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, lotNo);
			pstmt.setString(3, lotNo);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
				unitPack = rs.getString(1);
				// 21/04/10 manoharan if item lot pack size defined then only convert
				// moved from down
				if (!unit.trim().equals(unitPack.trim()))
				{
					//conv = distCommon.convertBox(itemCode, unitPack, unit);
					//gf_conv_qty( asUnitFrom, asUnitTo, asItemCode,1);
					//quantity = distCommon.convQtyFactor( unit.trim(), unitPack.trim(), itemCode, 1, conn );
					// 21/04/10 manoharan instead of converting the same has to be rounded in multiplies of the conversion factor
					//quantity = distCommon.convQtyFactor( unit.trim(), unitPack.trim(), itemCode, quantity, conn );
					packSize = 0;
					convList =  distCommon.getConvQuantityFact(unitPack.trim(), unit.trim(), itemCode, quantity, packSize, conn);
					packSize = Double.parseDouble((String) convList.get(0));
					System.out.println("packSize :: "+packSize);
					quantity = quantity - (quantity % packSize);
					System.out.println("quantity after rounding :: "+quantity);
					// end 21/04/10 manoharan instead of converting the same has to be rounded in multiplies of the conversion factor
					//quantity = quantity - (quantity % conv);
				} 
				// end 21/04/10 manoharan
			}
			else
			{
				unitPack = unit;
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
			/* 21/04/10 manoharan if item lot pack size defined then only convert
			// moved up
			if (!unit.trim().equals(unitPack.trim()))
			{
				//conv = distCommon.convertBox(itemCode, unitPack, unit);
				//gf_conv_qty( asUnitFrom, asUnitTo, asItemCode,1);
				//quantity = distCommon.convQtyFactor( unit.trim(), unitPack.trim(), itemCode, 1, conn );
				quantity = distCommon.convQtyFactor( unit.trim(), unitPack.trim(), itemCode, quantity, conn );
				//quantity = quantity - (quantity % conv);
			} // end 21/04/10 manoharan*/
			utilMethods = null;
			distCommon = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		return quantity;

	}
	private double getReqDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		String strValue = null;
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return Double.parseDouble(decFormat.format(actVal));
	}

	private StringBuffer GetSetQcReqd(Document dom, Document dom1, StringBuffer sBuff, Connection conn) throws Exception
	{
		String varValue = null, locCode = null, descr = null, retReplFlag = null;
		String retOpt = null, itemCode = null, siteCode = null, qcReqd = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DistCommon distCommon = new DistCommon();	
		try
		{
			retReplFlag = 	genericUtility.getColumnValue("ret_rep_flag",dom);
			retOpt = genericUtility.getColumnValue("ret_opt",dom1);
			itemCode = 	genericUtility.getColumnValue("item_code",dom);
			siteCode	=	genericUtility.getColumnValue("site_code",dom1);

			System.out.println( "itemCode :: " + itemCode );

			qcReqd = getQcRecd(siteCode, itemCode, conn);
			if (qcReqd == null || qcReqd.trim().length() == 0 )
			{
				qcReqd = "N";
			}
			if ("D".equals(retOpt))
			{
				qcReqd = "N";
			}
			if ("P".equals(retReplFlag))
			{
				qcReqd = "N";
			}
			varValue = distCommon.getDisparams("999999","SRET_QC_REQD",conn);

			if (varValue == null | "NULLFOUND".equals(varValue) || varValue.trim().length() == 0)
			{
				varValue = "Y";
			}
			if ("Y".equals(varValue))
			{
				sBuff.append("<qc_reqd>").append(qcReqd).append("</qc_reqd>");
				if ("Y".equals(qcReqd))
				{
					sql="select loc_code__insp "
							+ " from	siteitem "
							+ " where item_code = ? "
							+ " and site_code = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setString(2,siteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						locCode = rs.getString(1);
						sql="select descr from location where loc_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,locCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
						}
						sBuff.append("<loc_code>").append(locCode).append("</loc_code>");
						sBuff.append("<location_descr>").append(descr).append("</location_descr>");
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;	
				}
			}
			distCommon = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		return sBuff ;
	}
	private String getQcRecd( String site, String item, Connection conn ) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sqlStr = "select ( case when t.qc_rqd <> ' ' then t.qc_rqd else t.QC_REQD end) QC_REQD "
				+" from ( select ( case when s.QC_REQD is null then ' ' else s.QC_REQD end ) qc_rqd, m.QC_REQD "
				+" 	from siteitem s, item m " 
				+"  where s.site_code = ? "
				+"	and s.item_code = ? "
				+"	and s.item_code = m.item_code) t ";

		try
		{
			pstmt = conn.prepareStatement( sqlStr );

			sqlStr = null;
			pstmt.setString( 1, site );
			pstmt.setString( 2, item );

			rs = pstmt.executeQuery();

			if( rs.next() )
			{
				sqlStr = rs.getString( "QC_REQD" );
			}

		}catch( Exception ex )
		{
			throw new SQLException( ex );
		}
		finally
		{
			try{
				if( rs != null )
				{
					rs.close();
				}
				rs = null;

				if( pstmt != null )
				{
					pstmt.close();
				}
				pstmt = null;
			}catch( Exception e )
			{
				throw new SQLException( e );
			}
		}
		System.out.println( "sqlStr :: " + sqlStr ); 
		return sqlStr;
	}

	private String getMfgSitePackCode(String itemCode, String siteCode, String locCode, String lotNo, String lotSl, String colType, Connection conn) throws Exception
	{
		DistCommon distCommon = new DistCommon();	
		String sql = "", errCode = null, retVal = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			if (lotNo == null || lotNo.equals("null"))
			{
				lotNo = "               ";
			}
			if (lotSl == null || lotSl.equals("null"))
			{
				lotSl = "     ";
			}

			if (colType.equals("M"))
			{
				sql = "select site_code__mfg from stock "
						+ " where item_code = ? "
						+ " and site_code = ? "
						+ " and loc_code  = ? "
						+ " and lot_no = ? "
						+ " and	lot_sl = ? ";

				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,locCode);
				pstmt.setString(4,lotNo);
				pstmt.setString(5,lotSl);
				rs = pstmt.executeQuery(); 
				if(rs.next())
				{
					retVal = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;

				if (retVal == null || retVal.equals("null") || retVal.trim().length() == 0)
				{
					sql = "select site_code__mfg from item_lot_packsize "
							+ " where item_code = ?  "
							+ " and ? >= lot_no__from  "
							+ " and ? <= lot_no__to ";
					pstmt= conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setString(2,lotNo);
					pstmt.setString(3,lotNo);
					rs = pstmt.executeQuery(); 
					if(rs.next())
					{
						retVal = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				}
				if (retVal == null || retVal.equals("null") || retVal.trim().length() == 0)
				{
					sql = "select site_code from item "
							+ " where item_code = ? " ;
					pstmt= conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery(); 
					if(rs.next())
					{
						retVal = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				}

			}
			else if (colType.equals("P"))
			{
				sql = "select pack_code from stock "
						+ " where item_code = ? "
						+ " and site_code = ? "
						+ " and loc_code  = ? "
						+ " and lot_no = ? "
						+ " and	lot_sl = ? ";

				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,locCode);
				pstmt.setString(4,lotNo);
				pstmt.setString(5,lotSl);
				rs = pstmt.executeQuery(); 
				if(rs.next())
				{
					retVal = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;

				if (retVal == null || retVal.equals("null") || retVal.trim().length() == 0)
				{
					sql = "select pack_code from item "
							+ " where item_code = ? " ;
					pstmt= conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery(); 
					if(rs.next())
					{
						retVal = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				}
			}

			if (errCode != null && errCode.trim().length() > 0 )
			{
				retVal = errCode;
			}
			//return retVal;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		return retVal ;
	}
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}	
	private static void setNodeValue( Document dom, String nodeName, double nodeVal ) throws Exception
	{
		setNodeValue( dom, nodeName, Double.toString( nodeVal ) );
	}
	private static void setNodeValue( Document dom, String nodeName, int nodeVal ) throws Exception
	{
		setNodeValue( dom, nodeName, Integer.toString( nodeVal ) );
	}
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}
	private static String getNumString( String iValStr )
	{
		return ( iValStr == null || iValStr.trim().length() == 0 || "null".equals( iValStr.trim() ) ? "0" : iValStr.trim() );
	}
	private String getCurrdateInAppFormat()
	{
		String currAppdate =null;
		java.sql.Timestamp currDate = null;
		Object date = null;
		SimpleDateFormat DBDate=null;
		try
		{
			currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;

			DBDate= new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = DBDate.parse(currDate.toString());
			currDate =	java.sql.Timestamp.valueOf(DBDate.format(date).toString() + " 00:00:00.0");
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
			System.out.println("Exception in  getCurrdateInAppFormat:::"+e.getMessage());
		}
		return (currAppdate);
	}
	private String getTagValue( String xmlStr, String tag ) throws Exception
	{
		Document dom = genericUtility.parseString( xmlStr + "</Detail2></Root>" );

		String value = genericUtility.getColumnValue( tag, dom );

		return value;
	}

	private String checkNullTrim(String input)	
	{
		System.out.println("input Date : " +input);
		if (input == null)
		{
			input="";
		}
		return input;
	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	private double checkDoubleNull(String input)	
	{
		double var=0.0;
		if (input != null && input.trim().length() > 0)
		{
			var =Double.parseDouble(input);
		}
		return var;
	}
	private double getTotAmt(Document dom) throws Exception
	{
		/* 05/03/108 manoharan
		 * this method will return the total amount from all the details
		 **/
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		double totAmount = 0;
		double quantity = 0;
		String qtyStr =  "";
		String rateStr = "";
		double rate = 0;
		String retFlag = null;

		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			parentNodeList = dom.getElementsByTagName("Detail2");
			int childNodeListLength = parentNodeList.getLength();
			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				System.out.println("ctr ::"+ctr);
				parentNode = parentNodeList.item(ctr);

				qtyStr = genericUtility.getColumnValueFromNode("quantity", parentNode);
				retFlag = genericUtility.getColumnValueFromNode("ret_rep_flag", parentNode);
				quantity = Double.parseDouble( qtyStr == null || qtyStr.trim().length() == 0 || "null".equalsIgnoreCase( qtyStr ) ? "0" : qtyStr.trim() );
				rateStr = genericUtility.getColumnValueFromNode("rate", parentNode);
				rate = Double.parseDouble( rateStr == null || rateStr.trim().length() == 0 || "null".equalsIgnoreCase( rateStr ) ? "0" : rateStr.trim() );
				System.out.println("quantity ::"+quantity);
				System.out.println("rate ::"+rate);
				if( retFlag != null && "R".equalsIgnoreCase( retFlag.trim() ) )
				{
					totAmount = totAmount + (quantity * rate);
				}
				else
				{
					totAmount = totAmount - (quantity * rate);
				}
				totAmount = getRequiredDecimal( totAmount, 4 );
				System.out.println("totAmount ::"+totAmount);

			} // end for
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		return totAmount;
	}

	private double getTotEffAmt( String tranId, Connection conn ) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double totAmt = 0.0;
		String sqlStr = " SELECT sum ( ( case when quantity is null then 0 else quantity end ) * ( case when rate is null then 0 else rate end ) ) tot_amt "
				+"	from sreturndet "
				+"	where tran_id = ? "
				+"	and RET_REP_FLAG = 'R' ";

		pstmt = conn.prepareStatement( sqlStr );
		pstmt.setString( 1, tranId );
		rs = pstmt.executeQuery();

		if( rs.next() )
		{
			totAmt = rs.getDouble( "tot_amt" );
		}

		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		return totAmt;
	}
	private double getTotAmtForRep( Document dom ) throws Exception
	{
		/* 05/03/108 M S Alam 
		 * this method will return the total amount from all the detail
		 **/
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		double totAmount = 0;
		double quantity = 0;
		String qtyStr =  "";
		String rateStr = "";
		double rate = 0;
		String retFlag = null;

		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			parentNodeList = dom.getElementsByTagName("Detail2");
			int childNodeListLength = parentNodeList.getLength();
			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				System.out.println("ctr ::"+ctr);
				parentNode = parentNodeList.item(ctr);

				qtyStr = genericUtility.getColumnValueFromNode("quantity", parentNode);
				retFlag = genericUtility.getColumnValueFromNode("ret_rep_flag", parentNode);
				quantity = Double.parseDouble( qtyStr == null || qtyStr.trim().length() == 0 || "null".equalsIgnoreCase( qtyStr ) ? "0" : qtyStr.trim() );
				rateStr = genericUtility.getColumnValueFromNode("rate", parentNode);
				rate = Double.parseDouble( rateStr == null || rateStr.trim().length() == 0 || "null".equalsIgnoreCase( rateStr ) ? "0" : rateStr.trim() );
				if( retFlag != null && "P".equalsIgnoreCase( retFlag.trim() ) )
				{
					totAmount = totAmount + (quantity * rate);
				}
			} // end for
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		return totAmount;
	}
	public double getRequiredDecimal(double actVal, int prec)
	{
		NumberFormat numberFormat = NumberFormat.getIntegerInstance ();
		Double DoubleValue = new Double (actVal);
		numberFormat.setMaximumFractionDigits(prec);
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",","");
		double reqVal = Double.parseDouble(strValue);
		return reqVal;
	}	

	private String getCurrentUpdateFlag(Node currDetail)
	{
		NodeList currDetailList = null;
		String updateStatus = "",nodeName = "";
		int currDetailListLength = 0;

		currDetailList = currDetail.getChildNodes();
		currDetailListLength = currDetailList.getLength();
		for (int i=0;i< currDetailListLength;i++)
		{
			nodeName = currDetailList.item(i).getNodeName();
			if (nodeName.equalsIgnoreCase("Attribute"))
			{
				updateStatus = currDetailList.item(i).getAttributes().getNamedItem("updateFlag").getNodeValue();
				break;
			}
		}
		return updateStatus;		
	}	

	//Added by Santosh on 06/12/2016 to format error string [Start]
	private String getError(double validQty, String Code, Connection conn)  throws ITMException, Exception
	{
		String mainStr ="";

		try
		{
			String errString = "";
			errString =  new ITMDBAccessEJB().getErrorString("",Code,"","",conn);

			String begPart = errString.substring(0,errString.indexOf("</description>"));
			String endDesc = errString.substring(errString.indexOf("</description>"),errString.length());

			mainStr = begPart + "  ["+validQty+"]. " + endDesc;
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
	//Added by Santosh on 06/12/2016 to format error string [End]
	//Added by Santosh on 27/04/2017 to get next available invoide id [Start]
	//Changed by Santosh on 16/05/2017
	//private String getAvailableInvId(Document allDom, HashMap<String, String> curFormItemLotHMap, HashMap<String, String> curRecordItemLotHMap, String invoiceId, String minRateDocKey, String adjQty)
	private String getAvailableInvId(Document dom,Document allDom, HashMap<String, String> curFormItemLotHMap, HashMap<String, String> curRecordItemLotHMap, String invoiceId, String domDocKey, double dQtyAdj) // AdjQty change string to double by nandkumar gadkari on 26/07/19
	{
		String retInvoiceId = "";
		NodeList detail1NList = null;
		NodeList detail2NList = null;
		Node detail1Node = null;
		String curFormCustCode = "", curFormItemCode = "", curFormLotNo = "", curFormSiteCode = "", curFormQuantity = "";
		String domCustCode = "", domItemCode = "", domLotNo = "", domSiteCode = "", domQuantity = "", domInvoiceId = "";
		String curRecCustCode = "", curRecItemCode = "", curRecLotNo = "", curRecSiteCode = "", curRecInvQuantity = "", curRecInvoiceId = "";
		double  dCurRecInvQty = 0.0, dDomQty = 0.0, dTotalDomQty = 0.0;
		boolean isCurFormDataInDom = false;
		//Added by Santosh on 16/05/2017
		//	String domDocKey = "";

		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();

			curFormCustCode = curFormItemLotHMap.get("cust_code").trim();
			curFormItemCode = curFormItemLotHMap.get("item_code").trim();
			curFormLotNo = curFormItemLotHMap.get("lot_no").trim();
			curFormSiteCode = curFormItemLotHMap.get("site_code").trim();
			curFormQuantity = curFormItemLotHMap.get("quantity").trim();

			curRecCustCode = curRecordItemLotHMap.get("cust_code").trim();
			curRecItemCode = curRecordItemLotHMap.get("item_code").trim();
			curRecLotNo = curRecordItemLotHMap.get("lot_no").trim();
			curRecSiteCode = curRecordItemLotHMap.get("site_code").trim();
			curRecInvQuantity = curRecordItemLotHMap.get("quantity").trim();

			System.out.println("adjQty["+dQtyAdj+"] curRecQuantity["+curRecInvQuantity+"]");
			invoiceId=invoiceId.trim();// trim() by Nandkumar Gadkari on 25/10/18
			//dQtyAdj = adjQty == null || adjQty.trim().length()== 0 ? 0 :Double.parseDouble(adjQty); commented by nandkumar  on 26/07/19
			dCurRecInvQty =  curRecInvQuantity == null || curRecInvQuantity.trim().length()== 0 ? 0 : Double.parseDouble(curRecInvQuantity);

			System.out.println("dQtyAdj["+dQtyAdj+"] dCurRecInvQty["+dCurRecInvQty+"]");

			detail1NList = allDom.getElementsByTagName("Detail1");
			detail1Node = detail1NList.item(0);
			detail2NList = allDom.getElementsByTagName("Detail2");
			for(int i=0; i< detail2NList.getLength(); i++)// removed  detail2NList.getLength() -1 condition from loop by Nandkumar Gadkari on 14/09/18
			{
				/*Node eachDetail2Node = detail2NList.item(i);
				domCustCode = genericUtility.getColumnValueFromNode("cust_code", detail1Node).trim();
				domItemCode = genericUtility.getColumnValueFromNode("item_code", eachDetail2Node).trim();
				domLotNo = genericUtility.getColumnValueFromNode("lot_no", eachDetail2Node).trim();
				domSiteCode = genericUtility.getColumnValueFromNode("site_code", detail1Node).trim();
				domQuantity = genericUtility.getColumnValueFromNode("quantity", eachDetail2Node).trim();*/ //commented by  Nandkumar Gadkari on 14/09/18
				//added by Nandkumar Gadkari on 14/09/18-------[START]---------------
				domCustCode = checkNullTrim(genericUtility.getColumnValueFromNode("cust_code", detail1Node));
				domItemCode = checkNullTrim(genericUtility.getColumnValue("item_code", dom));
				domLotNo = (genericUtility.getColumnValue("lot_no", dom)).trim();
				domSiteCode = checkNullTrim(genericUtility.getColumnValueFromNode("site_code", detail1Node));
				domQuantity = checkNullTrim(genericUtility.getColumnValue("quantity", dom));
				//added by Nandkumar Gadkari on 14/09/18-------[END]---------------
				//Commented and changed by Santosh on 16/05/2017
				//domInvoiceId = genericUtility.getColumnValueFromNode("invoice_id", eachDetail2Node).trim();
				//domDocKey = checkNull(genericUtility.getColumnValueFromNode("doc_key", eachDetail2Node)).trim(); //commented by  Nandkumar Gadkari on 14/09/18

				dDomQty = Double.parseDouble(domQuantity);

				if( curFormCustCode.equalsIgnoreCase(domCustCode) &&
						curFormItemCode.equalsIgnoreCase(domItemCode) &&
						curFormLotNo.equalsIgnoreCase(domLotNo) &&
						curFormSiteCode.equalsIgnoreCase(domSiteCode))
				{
					isCurFormDataInDom = true;
					System.out.println("Record is matching need to check availability");
					//Changed by Santosh on 16/05/2017
					//if( invoiceId.equalsIgnoreCase(domInvoiceId) && 
					if( domDocKey.indexOf(invoiceId) != -1 && 
							curRecCustCode.equalsIgnoreCase(domCustCode) &&
							curRecItemCode.equalsIgnoreCase(domItemCode) && 
							curRecLotNo.equalsIgnoreCase(domLotNo) && 
							curRecSiteCode.equalsIgnoreCase(domSiteCode) )
					{
						dTotalDomQty = dDomQty;
						double totalQty = dQtyAdj + dTotalDomQty;
						System.out.println("totalQty["+totalQty+"]");
						if ( totalQty <= dCurRecInvQty)
						{
							retInvoiceId = invoiceId;
						}
						else if (totalQty >= dCurRecInvQty)
						{
							retInvoiceId = "";
							break;
						}
					}
					else
					{
						System.out.println("giving next availabe invoiceid["+invoiceId+"]");
						retInvoiceId = invoiceId;
					}
				}

			}
			System.out.println("invoiceid["+invoiceId+"]");

			if( !isCurFormDataInDom )
			{
				retInvoiceId = invoiceId;
			}
			System.out.println("retInvoiceId["+retInvoiceId+"]");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("SalesReturn.getAvailableInvId()["+e.getMessage()+"]");
		}
		return retInvoiceId;
	}
	private double getDomQuantity(Document allDom, HashMap<String, String> curDomDataHMap)
	{
		double retDomQty = 0.0, dDomQty = 0.0;
		String curDomCustCode = "", curDomItemCode = "", curDomLotNo = "", curDomSiteCode = "", curDomInvoiceId = "";
		String domCustCode = "", domItemCode = "", domLotNo = "", domSiteCode = "", domQuantity = "", domInvoiceId = "";
		NodeList detail1NList = null, detail2NList = null;
		Node detail1Node = null;
		//Added by Santosh on 16/05/2017
		String curDomDocKey = "", domDocKey = "";
		//Added by Santosh on 20/09/2017
		String domLineNo = "", curDomLineNo = "",updateFlag="";

		try
		{
			curDomCustCode = curDomDataHMap.get("cust_code").trim();
			curDomItemCode = curDomDataHMap.get("item_code").trim();
			curDomLotNo = curDomDataHMap.get("lot_no").trim();
			curDomSiteCode = curDomDataHMap.get("site_code").trim();
			//Commented and changed by Santosh on 16/05/2017
			//curDomInvoiceId = curDomDataHMap.get("invoice_id").trim();
			curDomDocKey = curDomDataHMap.get("doc_key").trim();
			//Added by Santosh on 20/09/2017 for bug fix split qty validatoin in edit mode
			curDomLineNo = curDomDataHMap.get("line_no").trim();
			detail1NList = allDom.getElementsByTagName("Detail1");
			detail1Node = detail1NList.item(0);
			detail2NList = allDom.getElementsByTagName("Detail2");
			for(int i=0; i<= detail2NList.getLength()-1; i++)
			{

				Node eachDetail2Node = detail2NList.item(i);
				domCustCode = genericUtility.getColumnValueFromNode("cust_code", detail1Node).trim();
				domItemCode = genericUtility.getColumnValueFromNode("item_code", eachDetail2Node).trim();
				domLotNo = genericUtility.getColumnValueFromNode("lot_no", eachDetail2Node).trim();
				domSiteCode = genericUtility.getColumnValueFromNode("site_code", detail1Node).trim();
				domQuantity = genericUtility.getColumnValueFromNode("quantity", eachDetail2Node).trim();
				//Commented and changed by Santosh on 16/05/2017
				//domInvoiceId = genericUtility.getColumnValueFromNode("invoice_id", eachDetail2Node).trim();
				domDocKey = genericUtility.getColumnValueFromNode("doc_key", eachDetail2Node).trim();
				//Added by Santosh on 20/09/2017 for bug fix split qty validatoin in edit mode
				domLineNo = genericUtility.getColumnValueFromNode("line_no", eachDetail2Node).trim();

				//added by nandkumar gadkari on 23-03-20
				updateFlag=checkNullandTrim(getCurrentUpdateFlag(eachDetail2Node));

				System.out.println("curDomDocKey["+curDomDocKey+"] domDocKey ["+domDocKey+"]");
				System.out.println("curDomCustCode["+curDomCustCode+"] domCustCode ["+domCustCode+"]");
				System.out.println("curDomItemCode["+curDomItemCode+"] domItemCode ["+domItemCode+"]");
				System.out.println("curDomLotNo["+curDomLotNo+"] domLotNo ["+domLotNo+"]");
				System.out.println("curDomSiteCode["+curDomSiteCode+"] domSiteCode ["+domSiteCode+"]");
				System.out.println("curDomLineNo["+curDomLineNo+"] domLineNo ["+domLineNo+"]");
				System.out.println("retDomQty["+retDomQty+"] domQuantity ["+domQuantity+"]");
				dDomQty = Double.parseDouble(domQuantity);

				//Commented and changed by Santosh on 16/05/2017
				//if( curDomInvoiceId.equalsIgnoreCase(domInvoiceId) && 
				if( curDomDocKey.equalsIgnoreCase(domDocKey) && 
						curDomCustCode.equalsIgnoreCase(domCustCode) &&
						curDomItemCode.equalsIgnoreCase(domItemCode) && 
						curDomLotNo.equalsIgnoreCase(domLotNo) && 
						curDomSiteCode.equalsIgnoreCase(domSiteCode) &&
						//Added by Santosh on 20/09/2017 for bug fix split qty validatoin in edit mode
						!curDomLineNo.equalsIgnoreCase(domLineNo)
						&& !"D".equalsIgnoreCase(updateFlag))

				{
					retDomQty += dDomQty;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("SalesReturn.getDomQuantity()["+e.getMessage()+"]");
		}
		return retDomQty;
	}
	//Added by Santosh on 27/04/2017 to get next available invoide id [End]
	//Added by Santosh on 16/05/2017 to generate minRateDocKey [Start]
	private  ArrayList<String> generateDocKey(Document headerDom, Document curDetDom, String invoiceID, Connection conn) // Change String to array list String by Nandkumar Gadkari on 14/09/18
	{
		//String retDocKey = "";// Commented by Nandkumar Gadkari on 14/09/18
		DistCommon distCommon = new DistCommon();
		String sNoOfKeys = "", schemeKey = "", varValue = "", docKey = null, docValue = "";
		int noOfKeys = 0;
		String col[] = new String[500];
		boolean dynamicCol = true;
		ArrayList<String> retDocKey = new ArrayList<String>();// added by Nandkumar Gadkari on 14/09/18
		try
		{
			sNoOfKeys = distCommon.getDisparams("999999","SCHEME_HIST_NUM",conn);
			if ( "NULLFOUND".equals( sNoOfKeys ) )
			{
				//retDocKey = ""; 
				retDocKey = null; // Changes by Nandkumar Gadkari on 14/09/18
			}
			else
			{
				noOfKeys = Integer.parseInt(sNoOfKeys);
			}

			for(int keyCtr = 1; keyCtr <= noOfKeys; keyCtr++)
			{
				docKey = "";
				schemeKey = "SCHEME_HIST_KEY" + keyCtr ;
				System.out.println(">>>>>>>>>In for loop schemeKey:"+schemeKey);
				varValue = distCommon.getDisparams("999999",schemeKey,conn);
				System.out.println(">>>>>>>>>In for loop afer schemeKey varValue:"+varValue);

				varValue = varValue.trim();

				String[] varValueArry = varValue.split(",");

				for(int i=0; i<varValueArry.length; i++)
				{
					if("cust_code".equalsIgnoreCase(varValueArry[i].trim()))
					{
						docValue = checkNull(genericUtility.getColumnValue(varValueArry[i].trim(),headerDom)).trim();
					}
					else if ("cust_code__bil".equalsIgnoreCase(varValueArry[i].trim())) //if condition added by Nandkumar Gadkari on 14/09/18
					{
						// commented by manoharan 17-mar-2021 column renamed to cust_code__bil
						//docValue = checkNull(genericUtility.getColumnValue("cust_code__bill", headerDom)).trim();
						docValue = checkNull(genericUtility.getColumnValue("cust_code__bil", headerDom)).trim();
					} 
					else if("site_code".equalsIgnoreCase(varValueArry[i].trim()))
					{
						docValue = checkNull(genericUtility.getColumnValue(varValueArry[i].trim(),headerDom)).trim();
					}
					else if("item_code".equalsIgnoreCase(varValueArry[i].trim()))
					{
						docValue = checkNull(genericUtility.getColumnValue(varValueArry[i].trim(),curDetDom)).trim();
					}
					else if("lot_no".equalsIgnoreCase(varValueArry[i].trim()))
					{
						docValue = checkNull(genericUtility.getColumnValue(varValueArry[i].trim(),curDetDom)).trim();
					}
					else if("invoice_id".equalsIgnoreCase(varValueArry[i].trim()))
					{
						docValue = invoiceID.trim();
					}
					else
					{
						docValue = checkNull(genericUtility.getColumnValue(varValueArry[i].trim(),headerDom)).trim();
					}

					if (docKey != "" && docKey.trim().length() > 0)
					{
						docKey = docKey + "," + ( docValue == null || docValue.trim().length() == 0 ? "" : docValue);
					}
					else
					{
						docKey = docValue;
					}
				}
				retDocKey.add(docKey);// added by Nandkumar Gadkari on 14/09/18
			}

			//retDocKey = docKey; // commented by Nandkumar Gadkari on 14/09/18
			System.out.println("retDocKey["+retDocKey+"]");
		}
		catch (Exception e)
		{
			System.out.println("SalesReturn.generateDocKey()["+e.getMessage()+"]");
			e.printStackTrace();
		}
		return retDocKey;
	}
	//Added by Santosh on 16/05/2017 to generate minRateDocKey [End]

	// added by Nandkumar Gadkari on 16/11/18 ------------------start----------------------------------
	private StringBuffer gbfIcExpMfgDate(StringBuffer valueXmlString, Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn) {
		// TODO Auto-generated method stub
		String siteCode = "", itemCode = "",lotNo= "", lotSl= "", sql= "";
		PreparedStatement pstmt = null;
		Timestamp mfgDate=null,expDate=null;
		ResultSet rs = null;
		int cntItemLotInfo = 0;
		try {
			siteCode = genericUtility.getColumnValue("site_code", dom1);
			itemCode = genericUtility.getColumnValue("item_code", dom);

			lotNo = genericUtility.getColumnValue("lot_no", dom);
			lotSl = genericUtility.getColumnValue("lot_sl", dom);

			if (lotSl == null || lotSl.equals("null"))
			{
				lotSl = "     ";
			}
			sql = "select  mfg_date, exp_date "
					+ " from item_lot_info "
					+ " where item_code = ? "
					+ " and lot_no = ? ";

			cntItemLotInfo=0;
			pstmt= conn.prepareStatement( sql );
			pstmt.setString( 1, itemCode);
			pstmt.setString( 2, lotNo);
			rs = pstmt.executeQuery(); 
			if( rs.next() )
			{
				cntItemLotInfo++;

				mfgDate = rs.getTimestamp(1);
				expDate = rs.getTimestamp(2);

			}
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;

			/*if(cntItemLotInfo==0)
				{
					sql = "select  a.mfg_date, a.exp_date"
							+ " from stock a "
							+ " where a.item_code = ? "
							+ " and a.site_code = ? "
							+ " and a.lot_no = ? "
							+ " and a.lot_sl = ? ";

						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode);
						pstmt.setString( 2, siteCode);
						pstmt.setString( 3, lotNo);
						pstmt.setString( 4, lotSl);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{

							mfgDate = rs.getTimestamp(1);
							expDate = rs.getTimestamp(2);

						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
				}*/
			if(expDate != null)
			{
				valueXmlString.append("<exp_date  protect =\"1\">").append("<![CDATA[" + new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString()  + "]]>").append("</exp_date>");
			}
			else
			{
				valueXmlString.append("<exp_date  protect =\"0\">").append("<![CDATA[]]>").append("</exp_date>");
			}

			valueXmlString.append("<mfg_date  protect =\"1\">").append("<![CDATA[" + ( mfgDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(mfgDate).toString() : "" ) + "]]>").append("</mfg_date>");		//protected by nandkumar gadkari on 29/01/19


		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valueXmlString;
	}
	private double getDomQuantityUsed(Document allDom, HashMap<String, String> curDomDataHMap)
	{
		double retDomQty = 0.0, dDomQty = 0.0;
		String curDomCustCode = "", curDomItemCode = "", curDomLotNo = "", curDomSiteCode = "", curDomInvoiceId = "";
		String domCustCode = "", domItemCode = "", domLotNo = "", domSiteCode = "", domQuantity = "", domInvoiceId = "";
		NodeList detail1NList = null, detail2NList = null;
		Node detail1Node = null;
		String curDomDocKey = "", domDocKey = "";
		String domLineNo = "", curDomLineNo = "";
		Node eachDetail2Node = null;
		try
		{
			//curDomCustCode = (curDomDataHMap.get("cust_code"));
			curDomItemCode = checkNullandTrim(curDomDataHMap.get("item_code"));
			curDomLotNo = checkNullandTrim(curDomDataHMap.get("lot_no"));
			//curDomSiteCode = (curDomDataHMap.get("site_code").trim();
			curDomDocKey = checkNullandTrim(curDomDataHMap.get("doc_key"));
			curDomLineNo = checkNullandTrim(curDomDataHMap.get("line_no"));
			detail1NList = allDom.getElementsByTagName("Detail1");
			detail1Node = detail1NList.item(0);
			detail2NList = allDom.getElementsByTagName("Detail2");
			int noOfDetails = detail2NList.getLength();
			System.out.println("inside side loop:"+noOfDetails);
			for(int i=0; i< noOfDetails; i++)
			{
				System.out.println("inside side loop");
				eachDetail2Node = detail2NList.item(i);
				System.out.println("inside side 223");
				//domCustCode = genericUtility.getColumnValueFromNode("cust_code", detail1Node).trim();
				domItemCode = checkNullandTrim(genericUtility.getColumnValueFromNode("item_code", eachDetail2Node));
				domLotNo = checkNullandTrim(genericUtility.getColumnValueFromNode("lot_no", eachDetail2Node));
				//domSiteCode = genericUtility.getColumnValueFromNode("site_code", detail1Node).trim();
				domQuantity =(genericUtility.getColumnValueFromNode("quantity", eachDetail2Node));
				domDocKey = checkNullandTrim(genericUtility.getColumnValueFromNode("doc_key", eachDetail2Node));
				domLineNo = checkNullandTrim(genericUtility.getColumnValueFromNode("line_no", eachDetail2Node));
				System.out.println("curDomDocKey["+curDomDocKey+"] domDocKey ["+domDocKey+"]");
				System.out.println("curDomCustCode["+curDomCustCode+"] domCustCode ["+domCustCode+"]");
				System.out.println("curDomItemCode["+curDomItemCode+"] domItemCode ["+domItemCode+"]");
				System.out.println("curDomLotNo["+curDomLotNo+"] domLotNo ["+domLotNo+"]");
				System.out.println("curDomSiteCode["+curDomSiteCode+"] domSiteCode ["+domSiteCode+"]");
				System.out.println("curDomLineNo["+curDomLineNo+"] domLineNo ["+domLineNo+"]");
				System.out.println("retDomQty["+retDomQty+"] domQuantity ["+domQuantity+"]");
				dDomQty =domQuantity == null || domQuantity.trim().length() == 0 ? 0 : Double.parseDouble(domQuantity.trim());

				if( curDomDocKey.equalsIgnoreCase(domDocKey) && 
						//curDomCustCode.equalsIgnoreCase(domCustCode) &&
						curDomItemCode.equalsIgnoreCase(domItemCode) && 
						curDomLotNo.equalsIgnoreCase(domLotNo) && 
						//	curDomSiteCode.equalsIgnoreCase(domSiteCode) &&

						!curDomLineNo.equalsIgnoreCase(domLineNo))
				{
					retDomQty += dDomQty;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("SalesReturn.getDomQuantityUsed()["+e.getMessage()+"]");
		}
		return retDomQty;
	}
	private String checkNullandTrim(String input) {
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
	// added by Nandkumar Gadkari on 16/11/18 ------------------end----------------------------------
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
			BaseLogger.log("0", null, null, "SQLException :SalesReturn :IsChannelPartnerCust()::" + se.getMessage());
			se.printStackTrace();			
		}

		catch (Exception e)
		{
			BaseLogger.log("0", null, null, "Exception :SalesReturn :IsChannelPartnerCust()::" + e.getMessage());
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
}// END OF MAIN CLASS

