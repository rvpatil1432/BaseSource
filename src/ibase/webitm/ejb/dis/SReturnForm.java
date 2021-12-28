package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.SysCommon;
import ibase.bi.ejb.session.VarSaxTransformer;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.sys.UtilMethods;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.sql.Timestamp;
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

public class SReturnForm extends ValidatorEJB implements SReturnFormLocal, SReturnFormRemote 
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String winName = null;
	UtilMethods utilMethods = UtilMethods.getInstance();
	FinCommon finCommon = new FinCommon();
	
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
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
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			System.out.println("xmlString" + xmlString);
			System.out.println("xmlString1" + xmlString1);
			System.out.println("C" + xmlString1);
			
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
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null ;
		String childNodeName = null;
		String columnValue = null;
		int currentFormNo = 0 ,cnt = 0,mcount1=0;
		String sql = "",descr="",sqlStr="";
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = null;
		distCommon = new DistCommon();	
		SysCommon sysCommon = null;
		
		int ctr = 0, mcount = 0,mdiffDays=0 , mdays=0;
		String tranId="",retOpt="",loginSiteCode="",loginSiteDescr="",priceListClgYn ="";
		String priceListClg="",taxClasss="";
		double mdisc = 0,mminShlife=0,exchRate =0;
		String tranDate = "",mvarValue="",locDescr="",lsDamagedtype="",tranType=""; 
		String invoiceId ="",tranMode="",siteCode="",custCodeBil="",currCode="",siteCodeDlv="";
		String analCode ="",itemSer ="",invDate ="",salesPers ="",custCode ="",custCodeDlv=""; 
		String saleOrder ="",salesPers1 ="",salesPers2 ="";
		String mSiteDescr="",custName="",tranCode="",tranName="",custNameDlv="";
		java.sql.Timestamp mtranDate = null,ldtLrdate=null,mchkDate=null;
		String mCurr="",mlocCode="",mloc="",mitemCode="";  
		double mshlife=0;
		java.sql.Timestamp tsTranDate = null,mmfgDate=null;
		java.sql.Timestamp expDate = null, mexpDate = null,mfgDate=null;
		String reStr = null,priceList="";
		int pos=0, mlineNoInv=0;
		String siteCodeMfg = "",channelPartner = "",mstatus="",lsSretLocCode="",mvarName="" ;
		String mCode="",mCode1="",lsInvoiceId="",lsInvoiceItem="";
		double totAmt=0,adjAmt=0,amount=0,sumAdj=0;
		String fullRet="";String varValue="";
		String retReplFlag="",itemCode="",unit="",unitRate="",itemStru="",packInstr="";
		String valStr="",despId="",invoiceItem="",despLineNo="",lotNo="",lotSl="";
		double costRate =0 ;
		String rateType="",lsLocCode="",lsInvStat="",mstkOpt="",lsRetRepFlag="";
		String frStation="",toStation="",taxEnv="";
		String taxChap="",taxClass="";
		double minRate= 0 ,lcDiscount=0 ;
		String lineNoInvStr ="",mtranDateStr="";
		String sRate = "0",lsDespId="",lsDespLineNo="",lsIinvoiceId="";
		int lineNoInv=0,cnt5=0;
		double qtyStdUom=0,rate =0,fact=0; 
		String locCode ="",taxChapHdr="",taxClassHdr="",taxEnvHdr="";
		String lineNoTrace="",qtyStr="",unitStd="";
		int iLineNoTrace=0;
		double effAmt = 0,quantity=0,sQuantity=0;
		double convFact=0;
		ArrayList convAr = null;
		double convRateStdUom=0,rateClg=0;
		double rateStduom = 0; //added by kunal on 10/oct/13
		String temp = "";//added by kunal on 10/oct/13
		String listType="";
		String mcode="",mitem="";
		String packCode = "";
		double mNum1=0,mNum2=0,mNum=0;
		double mVal1=0,mrate=0,mconvRtuom=0,mrateStd=0;
		double rateStd=0,totFreeCost=0,discount=0;
		String sorder ="",sordLineNo="";
		String pickLowerRate = "";
		String reasonCode = "";//added by kunal on 24/12/12
		double priceListRate = 0,sRateClg=0;
		HashMap infoMap = null;
		String sretLocCode="",taxCal="",itemCodeOrd="";
		double effCost=0; 
		SimpleDateFormat simpleDateFormat = null;
		int months = 0;
		java.util.Date defaultExpDate = null;
		
		String contractNo="";//added by priyanka on 15/06/15
		double contractRate = 0.0;
		int cntItemLotInfo=0; // added by nandkumar gadkari on 28/01/19
		String expDateStrg="",mfgDateStrg="";// added by nandkumar gadkari on 28/01/19
		Timestamp trDate = null;
		double srDQuantity =0,adjQty=0,domTotalQty=0;
		int mrhCnt=0;// added by nandkumar gadkari on 13/09/19
		try
		{
			ITMDBAccessEJB dbEjb = new ITMDBAccessEJB();
			sysCommon = new SysCommon();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("currentColumn is latest::"+currentColumn);

			System.out.println("currentFormNo is  :::"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver=null;
			simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
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
					}
					while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					if (currentColumn.trim().equals("itm_defaultedit") )			
					{
						tranId = genericUtility.getColumnValue("tran_id",dom);
						retOpt = genericUtility.getColumnValue("ret_opt",dom);
						
						sql =" select count(1) from sreturn_form_det where tran_id = ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,tranId );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt = rs.getInt(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						//-----------------------Changes by --Nandkumar Gadkari on 30/10/18-Start -----------------to protect cust code 
						custCode = getAbsString(genericUtility.getColumnValue("cust_code",dom));
						if (cnt > 0 )
						{
							valueXmlString.append("<ret_opt protect =\"1\">").append(retOpt).append("</ret_opt>");	
							valueXmlString.append("<cust_code protect =\"1\">").append(getAbsString(custCode)).append("</cust_code>");
						}
						else
						{
							valueXmlString.append("<ret_opt protect =\"0\">").append(retOpt).append("</ret_opt>");	
							valueXmlString.append("<cust_code protect =\"0\">").append(getAbsString(custCode)).append("</cust_code>");	
						}
						
						
				
						//-------------------------Nandkumar Gadkari on 30/10/18-END -----------------to protect cust code 
						
					}
					if (currentColumn.trim().equals("itm_default") )			
					{
						loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
						siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom);
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
						// 24/05/13 manoharan if site_code__dlv is not set through resource pack set the same as login site 73-N
						if (siteCodeDlv == null || siteCodeDlv.trim().length() == 0)
						{
							valueXmlString.append("<site_code__dlv protect =\"0\">").append("<![CDATA["+loginSiteCode.trim()+"]]>").append("</site_code__dlv>");
						}
						
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
						//valueXmlString.append("<ret_opt protect =\"0\">").append("").append("</ret_opt>");	
						valueXmlString.append("<exch_rate protect =\"0\">").append("0").append("</exch_rate>");
						
						
						//sql =" select rtrim(fld_value),descr from gencodes where fld_name='P_TYPE' and udf_str1 = 'D' ";
						sql =  " SELECT RTRIM(FLD_VALUE)R FROM GENCODES WHERE FLD_NAME='TRAN_TYPE' AND MOD_NAME='W_SALESRETURN' AND UDF_STR1='D' "; //change done by kunal on 26/12/12 as manoj instruction 
						pstmt= conn.prepareStatement( sql );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							tranType = rs.getString(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						//added by kunal on 24/12/12
						valueXmlString.append("<tran_type protect =\"0\">").append(tranType).append("</tran_type>");
	
						
						
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
								+ " cust_code, sale_order, sales_pers__1, sales_pers__2,tran_code "
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
								tranCode=rs.getString("tran_code");//Added by manoj sharma 05112012 to set transporter from invoice
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
							
							sql =" select count(distinct SORD_NO) "
								+" from invdet "
								+" where invoice_id = ? " ;
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
								sql =" select price_list,price_list__clg, proj_code from sorder where sale_order = ? ";
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, saleOrder );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									priceList = rs.getString(1); 
									priceListClg = rs.getString(2);
									
									valueXmlString.append("<price_list protect =\"1\">").append( getAbsString( priceList ) ).append("</price_list>");	
									setNodeValue( dom, "price_list", getAbsString( priceList ) );
									valueXmlString.append("<price_list__clg protect =\"0\">").append( getAbsString( priceListClg ) ).append("</price_list__clg>");
									setNodeValue( dom, "price_list__clg", getAbsString( priceListClg ) );
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}
							
							valueXmlString.append("<tran_mode>").append("<![CDATA["+ getAbsString( tranMode ) +"]]>").append("</tran_mode>");
							System.out.println( "tranMode2 :: " + tranMode );
							setNodeValue( dom, "tran_mode", getAbsString( tranMode ) );
						
							valueXmlString.append("<site_code>").append("<![CDATA["+ getAbsString( siteCode ) +"]]>").append("</site_code>");
							setNodeValue( dom, "site_code", getAbsString( siteCode ) );
							valueXmlString.append("<cust_code protect =\"1\">").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
							setNodeValue( dom, "cust_code", getAbsString( custCode ) );
						
							
							//PICKING SITE 
							sql =" select descr from site where site_code = ?";
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
						
							sql =" select cust_name, tran_code from customer where cust_code  = ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, custCode );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								custName = rs.getString(1); 
								tranCode = tranCode == null ?rs.getString(2) : tranCode.trim();//Added by manoj sharma 05112012 to set transporter from invoice
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							valueXmlString.append("<cust_name>").append("<![CDATA["+getAbsString(custName)+"]]>").append("</cust_name>");
							valueXmlString.append("<tran_code>").append("<![CDATA["+getAbsString(tranCode)+"]]>").append("</tran_code>");
							valueXmlString.append("<curr_code>").append("<![CDATA["+getAbsString(currCode)+"]]>").append("</curr_code>");
							
							//PICKING TRANSPORTER NAME
							sql =" select tran_name from transporter where tran_code  =  ?";
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
							
							sql =" select descr from currency where curr_code  =  ? ";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, currCode );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								mCurr = rs.getString(1); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							valueXmlString.append("<currency_descr>").append("<![CDATA["+ getAbsString( mCurr ) +"]]>").append("</currency_descr>");
							valueXmlString.append("<curr_code__bc>").append("<![CDATA["+getAbsString(currCode)+"]]>").append("</curr_code__bc>");
							valueXmlString.append("<exch_rate>").append("<![CDATA["+ exchRate +"]]>").append("</exch_rate>");
							valueXmlString.append("<anal_code>").append("<![CDATA["+getAbsString(analCode)+"]]>").append("</anal_code>");
							valueXmlString.append("<item_ser>").append("<![CDATA["+getAbsString(itemSer)+"]]>").append("</item_ser>");
							
							//CHECKING IN RECEVABLES IF ANY DUES ARE O/S FOR CUST CODE OR NOT
							//IF YES THEN ISSUE CREDIT NOTE ELSE REPLACE THE GOODS
							
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
								retOpt =  "C";
							}
							else
							{
								retOpt = "R";
							}
							valueXmlString.append("<ret_opt>").append("<![CDATA[" + getAbsString( retOpt ) + "]]>").append("</ret_opt>");
							setNodeValue( dom, "ret_opt", getAbsString( retOpt ) );
													
						}
					}
					else if (currentColumn.trim().equals("cust_code"))
					{
						custCode = genericUtility.getColumnValue("cust_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						String tranDateStr = genericUtility.getColumnValue("tran_date",dom);
						custCodeDlv = genericUtility.getColumnValue("cust_code__dlv",dom);
												
						tsTranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0" ) ;
						invoiceId = genericUtility.getColumnValue("invoice_id",dom);
						
						sql =" select c.cust_name, c.tran_code, c.curr_code, " 
							+" c.trans_mode, c.cust_code__bil, t.tran_name "
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
						valueXmlString.append("<curr_code>").append("<![CDATA[" + getAbsString( currCode ) + "]]>").append("</curr_code>");
						setNodeValue( dom, "curr_code", getAbsString( currCode ) );
						valueXmlString.append("<trans_mode>").append("<![CDATA[" + getAbsString( tranMode ) + "]]>").append("</trans_mode>");
						setNodeValue( dom, "trans_mode", getAbsString( tranMode ) );
						valueXmlString.append("<curr_code__bc>").append("<![CDATA[" + getAbsString( currCode ) + "]]>").append("</curr_code__bc>");
						setNodeValue( dom, "curr_code__bc", getAbsString( currCode ) );
						//cust_code__bill Added by Nandkumar Gadkari on 30OCT2018-----------start ----------
						valueXmlString.append("<cust_code__bill>").append("<![CDATA[" + getAbsString( custCodeBil ) + "]]>").append("</cust_code__bill>");
						setNodeValue( dom, "cust_code__bill", getAbsString( custCodeBil ) );
						//cust_code__bill Added by Nandkumar Gadkari on 30OCT2018-----------END ----------
						sql =" select std_exrt, descr from currency where curr_code = ? "; 
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
						
						descr = null;
						sql =" select tran_name from transporter where tran_code = ? "; 
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
						
						//CHECKING IN RECEVABLES IF ANY DUES ARE O/S FOR CUST CODE OR NOT
						//IF YES THEN ISSUE CREDIT NOTE ELSE REPLACE THE GOODS
						
						//committed by akhilesh on 25/12/12 as discuss with kbaghar by manoj this logic is not use in taro-edi
						/*sql =" select count(1) from receivables " 
							+" where tran_ser in ('S-INV','DRNRCP', 'CRNRCP') "
							+" and cust_code = ? "
							+" and item_ser  = ? "
							+" and ( tot_amt - adj_amt ) > 0 "
							+" and due_date <= ? ";
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
						*/
						/*priceListClg = distCommon.getDisparams("999999","PRICE_LIST__CLG",conn);//comment added by sagar on 19/08/14	
						if((priceListClg == null) || (priceListClg.trim().length() == 0))
						{
							sql =" select price_list__clg from site_customer where cust_code = ? and site_code = ?"; 
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
								sql =" select price_list__clg from customer where cust_code = ? "; 
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
							
						}
						if (priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
						{
							valueXmlString.append("<price_list__clg>").append("<![CDATA[" + getAbsString( priceListClg ) + "]]>").append("</price_list__clg>");
							setNodeValue( dom, "price_list__clg", getAbsString( priceListClg ) );
						} */
						//reStr = itemChanged(dom, dom1, dom2, objContext, "ret_opt", editFlag, xtraParams);//comment added by sagar on 05/08/14
						//Added by sagar on 05/08/14 start
												
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + getAbsString( custCode ) + "]]>").append("</cust_code__dlv>");
						setNodeValue( dom, "cust_code__dlv", getAbsString( custCode ) );
						reStr = itemChanged(dom, dom1, dom2, objContext, "cust_code__dlv", editFlag, xtraParams);
						//Added by sagar on 05/08/14 end
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					else if (currentColumn.trim().equals("curr_code") )			
					{
						currCode = genericUtility.getColumnValue("curr_code",dom);
						sql =" select descr, std_exrt from currency where curr_code = ?"; 
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
					}
					else if (currentColumn.trim().equals("tran_code") )			
					{
						tranCode = genericUtility.getColumnValue("tran_code",dom);
						sql =" select tran_name from transporter where tran_code = ?"; 
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
						sql =" select descr from site where site_code = ?"; 
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
						
						reStr = itemChanged(dom, dom1, dom2, objContext, "ret_opt", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					else if (currentColumn.trim().equals("ret_opt") )			
					{
						System.out.println("ret_opt itemchanged@@@@@@@@");
						retOpt = genericUtility.getColumnValue("ret_opt",dom);
						custCode = genericUtility.getColumnValue("cust_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						
						priceList = "";
						//commented by priyanka on 23/06/15
						
						/*sql = " select price_list from site_customer where cust_code = ? and site_code = ?"; 
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
						}*/	//commented end on 23/06/15
						
						//pricelist should set from below function
						//Added	 by priyanka on 23/06/15 as per manoj sharma instruction				
						priceList = getPriceLstVal(custCode, custCodeDlv, siteCode, contractNo, conn);
						System.out.println("Getting Price List from ret_opt=====" + priceList);
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
					else if (currentColumn.trim().equals("cust_code__dlv"))	//condition added by sagar on 05/08/14		
					{
						System.out.println("Cust Code Dlv itemChanged called********");
					    custCodeDlv = genericUtility.getColumnValue("cust_code__dlv",dom);
					    custCode = genericUtility.getColumnValue("cust_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						System.out.println(">>>>>>>Head.. In Itemchange of cust_code__dlv:"+custCodeDlv);
						sql = " select cust_name from customer where cust_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCodeDlv);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							custNameDlv = rs.getString(1); 
							System.out.println(">>>>>>>Found custNameDlv:"+custNameDlv);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						valueXmlString.append("<cust_name_dlv>").append("<![CDATA[" + getAbsString( custNameDlv ) + "]]>").append("</cust_name_dlv>");
						setNodeValue( dom, "cust_name_dlv", getAbsString( custNameDlv ) );
												
						priceList = "";//comment start
						/*sql = " select price_list from site_customer where cust_code = ? and site_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, custCode);
						pstmt.setString( 2, siteCode);
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
							pstmt.setString( 1, custCode);
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								priceList = rs.getString(1); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							//check price list value for customer code delivery... on 14/08/14 start
							if (priceList == null || priceList.trim().length() == 0)
							{
								sql = " select price_list from site_customer where cust_code = ? and site_code = ?"; 
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, custCodeDlv);
								pstmt.setString( 2, siteCode);
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									priceList = rs.getString(1); 
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if (priceList == null || priceList.trim().length() == 0)
								{
									sql = " select price_list from customer where cust_code = ?"; 
									pstmt= conn.prepareStatement( sql );
									pstmt.setString( 1, custCodeDlv);
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
							}
							//check price list value for customer code delivery... on 14/08/14 end						
						 }		
						 valueXmlString.append("<price_list>").append("<![CDATA[" + getAbsString(priceList) + "]]>").append("</price_list>");*///comment end
						//price list should set from contract no if contract no is entered else follow the existing hierachy
						
						//added by priyanka on 22/06/15
						contractNo = genericUtility.getColumnValue("contract_no", dom);
						System.out.println(">>>>>>>>>>>>>contract no===========" + contractNo);
						priceList = getPriceLstVal(custCode, custCodeDlv, siteCode, contractNo, conn);
						System.out.println("Getting Price List======" + priceList);						
						valueXmlString.append("<price_list>").append("<![CDATA[" + getAbsString(priceList) + "]]>").append("</price_list>");
						
						
						
						
						//Price List Clg value set on cust_code_dlv item change instead of cust_code, added by sagar on 19/08/14....
						 priceListClg = distCommon.getDisparams("999999","PRICE_LIST__CLG",conn);
						 if((priceListClg == null) || (priceListClg.trim().length() == 0))
						 {
							 sql =" select price_list__clg from site_customer where cust_code = ? and site_code = ?"; 
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
								 sql =" select price_list__clg from customer where cust_code = ? "; 
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
								 //Check price_list__clg for customer code delivery ..Added by sagar on 19/08/14 ..start
								 if( priceListClg == null || priceListClg.trim().length() == 0 )
								 {
									 sql =" select price_list__clg from site_customer where cust_code = ? and site_code = ?"; 
									 pstmt= conn.prepareStatement( sql );
									 pstmt.setString( 1, custCodeDlv );
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
									 if( priceListClg == null || priceListClg.trim().length() == 0 )
									 {
										 sql =" select price_list__clg from customer where cust_code = ? "; 
										 pstmt= conn.prepareStatement( sql );
										 pstmt.setString( 1, custCodeDlv );
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
								   }
									//Check price_list__clg for customer code delivery ..Added by sagar on 19/08/14 ..end
								}
								priceListClg = priceListClg == null ?"" : priceListClg.trim();
						 }
						 if (priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
						 {
							 valueXmlString.append("<price_list__clg>").append("<![CDATA[" + getAbsString( priceListClg ) + "]]>").append("</price_list__clg>");
							 setNodeValue( dom, "price_list__clg", getAbsString( priceListClg ) );
						 }
					}
					
					
					//added by priyanka on 15/06/15 for contract no
					//price list should set from contract no if contract no is entered else follow the existing hierachy
					else if (currentColumn.trim().equalsIgnoreCase("contract_no"))
					{
						custCode = genericUtility.getColumnValue("cust_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractNo = genericUtility.getColumnValue("contract_no", dom);
						custCodeDlv = genericUtility.getColumnValue("cust_code__dlv", dom);

						System.out.println("@@@@ custCode[" + custCode + "]");
						System.out.println("@@@@ siteCode[" + siteCode + "]");
						System.out.println("@@@@ contractNo[" + contractNo + "]");
						System.out.println("@@@@ custCodeDlv[" + custCodeDlv + "]");
					
						priceList = getPriceLstVal(custCode, custCodeDlv, siteCode, contractNo, conn);
						System.out.println("Getting Price List from contract_number=====" + priceList);
						valueXmlString.append("<price_list>").append("<![CDATA[" + priceList + "]]>").append("</price_list>");
					}
					valueXmlString.append("</Detail1>");					
					break;
				
				case 2:
				
					parentNodeList = dom1.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					winName = getObjName(parentNode);
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					//-------------------------Nandkumar Gadkari on 30/10/18-Start -----------------to protect cust code 
					reStr = itemChanged(dom1, dom1, dom2, "1","itm_defaultedit", editFlag, xtraParams);
					System.out.println("CurrentColumn:["+currentColumn+"]");
					reStr=reStr.substring(reStr.indexOf("<Detail1>"), reStr.indexOf("</Detail1>"));
					System.out.println("Detail 1String"+reStr);
					valueXmlString = new StringBuffer(
							"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
					valueXmlString.append(editFlag).append("</editFlag> </header>");
					valueXmlString.append(reStr);
					
					custCode = getAbsString(genericUtility.getColumnValue("cust_code",dom1));
					
					valueXmlString.append("<cust_code protect =\"1\">").append(getAbsString(custCode)).append("</cust_code>");	
					
					valueXmlString.append("</Detail1>");
					//-------------------------Nandkumar Gadkari on 30/10/18-End -----------------to protect cust code 
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
						   
					if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
					{
						System.out.println("inside case 2 itemdefaultedit....");
						tranId = genericUtility.getColumnValue("tran_id",dom1);
						// 11/12/13 manoharan ret_opt is in header not detail, also check fo null and initialise with empty string
						//retOpt = genericUtility.getColumnValue("ret_opt",dom);
						retOpt = genericUtility.getColumnValue("ret_opt",dom1);
						taxEnv = genericUtility.getColumnValue("tax_env",dom);
						taxChap = genericUtility.getColumnValue("tax_chap",dom);
						taxClass = genericUtility.getColumnValue("tax_class",dom);
						retOpt = retOpt == null ?"" : retOpt.trim();
						taxEnv = taxEnv == null ?"" : taxEnv.trim();
						taxChap = taxChap == null ?"" : taxChap.trim();
						taxClass = taxClass == null ?"" : taxClass.trim();
						// end 11/12/13 manoharan
						sql = " select count(1) from SRETURN_FORM_DET where tran_id = ? ";
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
							//ADDED BY NANDKUMAR GADKARI ON 28/01/19--start
							mfgDateStrg = genericUtility.getColumnValue("mfg_date",dom);
							expDateStrg = genericUtility.getColumnValue("exp_date",dom);
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
							if(expDateStrg !=null && expDateStrg.trim().length()>0)
							{
								valueXmlString.append("<exp_date  protect =\"1\">").append("<![CDATA[" + (expDateStrg) + "]]>").append("</exp_date>");
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
						}
						else
						{
							valueXmlString.append("<ret_opt protect =\"0\">").append(retOpt).append("</ret_opt>");	
						}
						
						if ("R".equalsIgnoreCase(retOpt))
						{
							valueXmlString.append("<tax_chap protect =\"1\">").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"1\">").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"1\">").append("<![CDATA["+taxEnv+"]]>").append("</tax_env>");
						}
						else
						{
							valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA["+taxEnv+"]]>").append("</tax_env>");
						}
						
						//added by rupali on 01/04/2021 [start]
						sRate = genericUtility.getColumnValue( "rate", dom );
						String docKeyNew =  genericUtility.getColumnValue("doc_key",dom);
						System.out.println("docKeyNew latest:::::"+docKeyNew);
						String historyType = ""; 
						sql = "SELECT HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
							+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, docKeyNew);
						rs = pstmt.executeQuery();
						
						if(rs.next())
						{
							historyType = checkNullandTrim(rs.getString( "HISTORY_TYPE" )); 
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
						System.out.println("historyType in edit latest is::::::::::::::"+historyType);
						if("S".equalsIgnoreCase(historyType))
						{
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
						}
						else
						{
							valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + sRate + "]]>").append("</rate>");
						}
						//added by rupali on 01/04/2021 [end]
					}
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						
						/***dw_detedit[ii_currformno].SetItem(1,"tran_id",dw_edit.GetItemString(1,"tran_id"))
						li_line_no = long(gbf_get_argval(is_extra_arg, "line_no"))
						if li_line_no > 0 then
							dw_detedit[ii_currformno].setitem(1, "line_no", li_line_no)
						end if***/
						
						invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
						fullRet = genericUtility.getColumnValue("full_ret",dom1);
						retOpt = genericUtility.getColumnValue("ret_opt",dom1);
						
						reasonCode = genericUtility.getColumnValue("reas_code",dom1);
						
						
						if (invoiceId == null || invoiceId.trim().length() == 0 || "null".equalsIgnoreCase( invoiceId.trim() ) )
						{
								
							valueXmlString.append("<line_no__inv protect =\"1\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");
							valueXmlString.append("<item_code protect =\"0\">").append("<![CDATA["+""+"]]>").append("</item_code>");
						}
						else
						{
							valueXmlString.append("<invoice_id protect =\"1\">").append("<![CDATA[" + invoiceId + "]]>").append("</invoice_id>");
							valueXmlString.append("<line_no__inv protect =\"0\">").append("<![CDATA["+""+"]]>").append("</line_no__inv>");
							valueXmlString.append("<item_code protect =\"1\">").append("<![CDATA["+""+"]]>").append("</item_code>");
						}
						//valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
						valueXmlString.append("<cost_rate protect =\"1\">").append("<![CDATA["+""+"]]>").append("</cost_rate>");
						
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
						if("Y".equalsIgnoreCase(fullRet))
						{
							valueXmlString.append("<full_ret protect =\"1\">").append("<![CDATA[" + fullRet + "]]>").append("</full_ret>");
						}
						else
						{
							valueXmlString.append("<full_ret protect =\"0\">").append("<![CDATA[" + fullRet + "]]>").append("</full_ret>");
						}
						
						//PICKING UP LOCATION FROM DISPARM
						
						varValue = distCommon.getDisparams("999999","ALLOC_FGLOC",conn);
						valueXmlString.append("<loc_code>").append("<![CDATA[" + varValue + "]]>").append("</loc_code>");
						
						
						sql =" select descr from location where loc_code = ? "; 
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
						String reasCode="";
						
						valueXmlString.append("<location_descr>").append("<![CDATA[" + descr + "]]>").append("</location_descr>");
						reasCode = genericUtility.getColumnValue("reas_code", dom1);
						valueXmlString.append("<reas_code>").append("<![CDATA[" + getAbsString(reasCode) + "]]>").append("</reas_code>");
						priceList = genericUtility.getColumnValue("price_list", dom1);
						
						if ("L".equalsIgnoreCase(priceList))
						{
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[0]]>").append("</rate>");
						}
						else
						{
							valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[0]]>").append("</rate>");
						}
						//valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+""+"]]>").append("</status>");
						
						retOpt = retOpt == null ?"" : retOpt.trim();						
						if ("D".equals(retOpt))
						{
							valueXmlString.append("<ret_rep_flag protect =\"0\">").append("<![CDATA[P]]>").append("</ret_rep_flag>");
							varValue = distCommon.getDisparams("999999","CALC_TAX_ON_REPLACE",conn);
							if ("N".equals(varValue) )
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
							}
						}
						else if ("R".equals(retOpt))
						{
							valueXmlString.append("<ret_rep_flag protect =\"0\">").append("<![CDATA["+""+"]]>").append("</ret_rep_flag>");
							valueXmlString.append("<tax_chap protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"1\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
						}
						else
						{
							valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA["+""+"]]>").append("</ret_rep_flag>");
							valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_class>");
							valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + ""+"]]>").append("</tax_env>");
						}
						valueXmlString.append("<ret_rep_flag protect =\"1\">").append("<![CDATA[R]]>").append("</ret_rep_flag>");
						valueXmlString.append("<status>").append("<![CDATA[S]]>").append("</status>"); //added by kunal on 7/12/12 as per Manoj instruction
						
						//months = Integer.parseInt(distCommon.getDisparams("999999","DEFAULT_EXP_MONTHS",conn)==null?"0":distCommon.getDisparams("999999","DEFAULT_EXP_MONTHS",conn)) ;
						//change done by kunal on 11/oct/13 bug fixing for get value from  disparam
						valStr = distCommon.getDisparams("999999","DEFAULT_EXP_MONTHS",conn);
						if (valStr == null || valStr.trim().length() == 0 || valStr.equals("NULLFOUND"))
						{
							months = 0;
						}
						else
						{
							months = Integer.parseInt(valStr.trim());
							System.out.println("months = "+months );
						}
						
						// 12/12/13 manoharan if this variable is defined then only set the dates by default
						if (!"NULLFOUND".equals(valStr))
						{
						
							//valueXmlString.append("<mfg_date>").append("<![CDATA["+simpleDateFormat.format(new Date(2012, 00,01))+"]]>").append("</mfg_date>");//added by kunal on 7/12/12 as per Manoj instruction
							//valueXmlString.append("<exp_date>").append("<![CDATA["+simpleDateFormat.format(addMonths(new Date(2012, 00,01),months))+"]]>").append("</exp_date>");//added by kunal on 7/12/12 as per Manoj instruction
							valueXmlString.append("<mfg_date>").append("<![CDATA["+simpleDateFormat.format((Timestamp)java.sql.Timestamp.valueOf("2012-01-01 00:00:00.000"))+"]]>").append("</mfg_date>");//added by kunal on 7/12/12 as per Manoj instruction
							valueXmlString.append("<exp_date>").append("<![CDATA["+simpleDateFormat.format(addMonths((Timestamp)java.sql.Timestamp.valueOf("2012-01-01 00:00:00.000"),months))+"]]>").append("</exp_date>");//added by kunal on 7/12/12 as per Manoj instruction
						}
						
						System.out.println("reas_code"+reasonCode);
						valueXmlString.append("<reas_code>").append("<![CDATA["+reasonCode+"]]>").append("</reas_code>"); //added by kunal on 24/12/12 as per Manoj instruction
						setNodeValue( dom, "reas_code", reasonCode );
						reStr = itemChanged(dom, dom1, dom2, objContext, "reas_code", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					else if(currentColumn.trim().equalsIgnoreCase("item_code"))
					{
					
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
						//priceList = genericUtility.getColumnValue("price_list",dom1); //comment added by sagar on 18/08/14..
						sRate = genericUtility.getColumnValue( "rate", dom );
						System.out.println("rate--==========="+rate);
						itemSer = distCommon.getItemSer(itemCode,siteCode,Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"),custCode,"C",conn);
						valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
						sql =" select descr, unit, unit__rate, item_stru, pack_instr from item where item_code = ?"; 
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							descr = rs.getString(1); 
							unit = rs.getString(2) == null ? "" : rs.getString(2);
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
						if( lineNoInvStr != null )
						{
							pos = lineNoInvStr.indexOf(".");
						}
						if (pos > 0)
						{
							lineNoInvStr = lineNoInvStr.substring(0,pos);
						}
						System.out.println( "lineNoInvStr after:: " + lineNoInvStr ); 
						lineNoInv = Integer.parseInt( getNumString( lineNoInvStr ) );
											
						valStr =  genericUtility.getColumnValue( "quantity__stduom", dom );
						valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();
						qtyStdUom = Double.parseDouble( valStr );
						
						locCode = genericUtility.getColumnValue("loc_code",dom);
						lotNo = genericUtility.getColumnValue("lot_no",dom);
						lotSl =  genericUtility.getColumnValue("lot_sl",dom);
						String docKeyNew =  genericUtility.getColumnValue("doc_key",dom);
						System.out.println("docKeyNew:::::"+docKeyNew);
						
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
						priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
						System.out.println(">>>>>>>>>>Check priceList for getCost Rate 1:"+priceList);
						infoMap.put( "price_list", priceList);
						costRate = getCostRate( infoMap, conn );
						infoMap = null;
						
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
									sql =" select stan_code from site where site_code = ?";
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
									sql =" select stan_code from customer where cust_code = ?";
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
						
						//added by priyanka 
						//if contract no is defined set rate based on contracr else follow existing hierachy
						trDate= Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
						System.out.println(">>>>>>>>>Tran Date:"+trDate);
						contractNo = genericUtility.getColumnValue("contract_no", dom1);
						
						System.out.println("Getting Contract No======"+contractNo);
						if (contractNo != null && contractNo.trim().length() > 0)
						{
							sql = "select  rate from scontractdet where contract_no= ? and item_code=?  and ? between eff_from and valid_upto";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, contractNo);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, new java.sql.Timestamp(trDate.getTime()));
							
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								contractRate = rs1.getDouble("rate");										
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							System.out.println("contractRate====" + contractRate);

							if(contractRate==0)
							{
								priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
							}
							else
							{
								
								rate=contractRate;
							}
						}
						else
						{
							priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
						}
						//priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
						System.out.println(">>>>>>>>>>Check contract rate=====:"+contractRate);
						System.out.println(">>>>>>>>>>Check priceList >>>>>>>>"+priceList);
						
						
						System.out.println("Setting rate===="+rate);
						//if (priceList == null || priceList.trim().length() == 0)
						//{
						//added by priyanka on 23/06/15 
						//rate should set based on contract no,if rate define in scontractdet then set rate from scontractdet else set rate based on pricelist defined for entered contractno in scontract
						
						System.out.println("Price List for pickrate==="+priceList);
						if (contractNo != null && contractNo.trim().length() > 0)
						{
							System.out.println("Getting contractRate======="+contractRate);
							if(contractRate!=0)
							{
									System.out.println("rate=====*****"+rate);							
							
							//valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ (sRate == null || sRate.trim().length() == 0 ? "0" : sRate ) + "]]>").append("</rate>");
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ rate+ "]]>").append("</rate>");
							
							}
							else
							{
								System.out.println("Rate from contract pricelist");
								rate = distCommon.pickRate(priceList,tranDate,itemCode," ","L",qtyStdUom, conn);
								valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+ rate+ "]]>").append("</rate>");
							}
							//added now to test
							System.out.println("Final Rate from contract====="+rate);
							
							setNodeValue( dom, "rate", rate );
							//mrate = Double.parseDouble( rate );
							mrate=rate;
							System.out.println("Mrate======="+mrate);
																					
							//added by kunal on 10/oct/13 set rate con. FOR issue tracker point 157-N
							if (unitRate != null && !unitRate.trim().equals(unit.trim()))
							{

								convAr = distCommon.convQtyFactor(unit, unitRate, itemCode,mrate, 0, conn);
								convRateStdUom = Double.parseDouble(convAr.get(0).toString());
								rateStduom = Double.parseDouble(convAr.get(1).toString());
								valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// added by nandkumar gadkari on 29/01/19
							} 
							else
							{
								convRateStdUom = 1;
								rateStduom = mrate;
								valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// added by nandkumar gadkari on 29/01/19
							}
							if (rateStduom == -9.99999999E8)
							{
								rateStduom = -999999999;
								temp = "-999999999";
							} else
							{
								temp = String.valueOf(rateStduom);
							}
							System.out.println("rate kunal ="+mrate+"  conv__rtuom_stduom= "+convRateStdUom +"   rateSTDUOm="+temp);
							//valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>"); // // commented by nandkumar gadkari on 29/01/19
							setNodeValue( dom, "conv__rtuom_stduom", round(convRateStdUom, 3) );
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + temp + "]]>").append("</rate__stduom>");
							setNodeValue( dom, "rate__stduom", temp );
							
							//added by kunal on 10/oct/13 set rate con. end
							
							//added by kunal on 20/AUG/13 called rate item change
							reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
					    	
						}
						else
						{
							System.out.println("contract no is null");
							tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;
							System.out.println("Getting rate if pricelist is not null===="+rate);
						   	//if(rate<=0)
							// {
								rate = distCommon.pickRate(priceList,tranDate,itemCode," ","L",qtyStdUom, conn);
							// }
								System.out.println(">>>>>>>>>>Check rate in item_code:"+rate);
							varValue = distCommon.getPriceListType(priceList,conn);
							varValue = varValue == null ?"" : varValue.trim();
							
							if ("B".equals(varValue) && rate < 0)
							{
								rate = 0;
							}
							if ("B".equals(varValue) || "F".equals(varValue) || "Y".equals(fullRet))
							{
								//added by rupali on 01/04/2021 [start]
								String historyType = ""; 
								sql = "SELECT HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
									+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, docKeyNew);
								rs = pstmt.executeQuery();
								
								if(rs.next())
								{
									historyType = checkNullandTrim(rs.getString( "HISTORY_TYPE" )); //added by rupali on 01/04/2021
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
								System.out.println("historyType in edit is::::::::::::::"+historyType);
								if("S".equalsIgnoreCase(historyType))
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
								}
								//added by rupali on 01/04/2021 [end]
								else
								{
									valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
								}
								setNodeValue( dom, "rate", rate );
								//mrate = Double.parseDouble( sRate );
								
								//added by kunal on 10/oct/13 set rate con. FOR issue tracker point 157-N
								if (unitRate != null && !unitRate.trim().equals(unit.trim()))
								{

									convAr = distCommon.convQtyFactor(unit, unitRate, itemCode,rate, 0, conn);
									convRateStdUom = Double.parseDouble(convAr.get(0).toString());
									rateStduom = Double.parseDouble(convAr.get(1).toString());
									valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// added by nandkumar gadkari on 29/01/19
								} 
								else
								{
									convRateStdUom = 1;
									rateStduom = rate;
									valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// added by nandkumar gadkari on 29/01/19
								}
								if (rateStduom == -9.99999999E8)
								{
									rateStduom = -999999999;
									temp = "-999999999";
								} else
								{
									temp = String.valueOf(rateStduom);
								}
								System.out.println("rate kunal ="+rate+"  conv__rtuom_stduom= "+convRateStdUom +"   rateSTDUOm="+temp);
								//valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// commented by nandkumar gadkari on 29/01/19
								setNodeValue( dom, "conv__rtuom_stduom", round(convRateStdUom, 3) );
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + temp + "]]>").append("</rate__stduom>");
								setNodeValue( dom, "rate__stduom", temp );
								
								//added by kunal on 10/oct/13 set rate con. end
								//added by kunal on 20/AUG/13 called rate item change
								reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
							}
							else
							{
								valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
								setNodeValue( dom, "rate", rate );
								//mrate = Double.parseDouble( sRate );
								
								//added by kunal on 10/oct/13 set rate con. FOR issue tracker point 157-N
								if (unitRate != null && !unitRate.trim().equals(unit.trim()))
								{

									convAr = distCommon.convQtyFactor(unit, unitRate, itemCode,rate, 0, conn);
									convRateStdUom = Double.parseDouble(convAr.get(0).toString());
									rateStduom = Double.parseDouble(convAr.get(1).toString());
									valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// added by nandkumar gadkari on 29/01/19
								} 
								else
								{
									convRateStdUom = 1;
									rateStduom = rate;
									valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// added by nandkumar gadkari on 29/01/19
								}
								if (rateStduom == -9.99999999E8)
								{
									rateStduom = -999999999;
									temp = "-999999999";
								} else
								{
									temp = String.valueOf(rateStduom);
								}
								System.out.println("rate kunal ="+rate+"  conv__rtuom_stduom= "+convRateStdUom +"   rateSTDUOm="+temp);
								//valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[" + round(convRateStdUom, 3) + "]]>").append("</conv__rtuom_stduom>");// commented by nandkumar gadkari on 29/01/19
								setNodeValue( dom, "conv__rtuom_stduom", round(convRateStdUom, 3) );
								valueXmlString.append("<rate__stduom>").append("<![CDATA[" + temp + "]]>").append("</rate__stduom>");
								setNodeValue( dom, "rate__stduom", temp );
								
								//added by kunal on 10/oct/13 set rate con. end
								//added by kunal on 20/AUG/13 called rate item change
								reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
							}
						}
						//}//added at 2.14
						/* comment by kunal on 20/AUG/13
						varValue = 	genericUtility.getColumnValue("conv__rtuom_stduom", dom);
						varValue = varValue == null ?"0" : varValue.trim();
						if (varValue == null || Double.parseDouble(varValue) <= 0)
						{
							valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");
							setNodeValue( dom, "conv__rtuom_stduom", "1" );
							System.out.println("manohar rate__stduom 1 [" + rate + "]");
							valueXmlString.append("<rate__stduom>").append("<![CDATA[" + rate + "]]>").append("</rate__stduom>");
							setNodeValue( dom, "rate__stduom", rate );
						}
						*/
						invoiceId = 	genericUtility.getColumnValue("invoice_id", dom);
						retReplFlag = 	genericUtility.getColumnValue("ret_rep_flag", dom);
						if ( !"P".equals( retReplFlag ) )
						{
							if (invoiceId != null && invoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( invoiceId.trim() ) )
							{
								lineNoTrace = genericUtility.getColumnValue( "line_no__inv", dom );
								lineNoTrace = lineNoTrace == null ?"0" : lineNoTrace.trim();
								iLineNoTrace = Integer.parseInt( lineNoTrace ) ;
								
								sql =" select item_code from invoice_trace where invoice_id  = ? and line_no = ?"; 
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
						/*
						 ls_errcode = gbf_scheme_history(mrate,colname)
						 gbf_getsetlastinvrate()
						 */
					} //end of item_code
					
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
							
							priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
							System.out.println(">>>>>>>>>>Check priceList for itemValue2Quantity:"+priceList);
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
					}//end of ret_rep_flag
					else if (currentColumn.trim().equals("quantity") )			
					{
						qtyStr =genericUtility.getColumnValue("quantity", dom);
						sQuantity = Double.parseDouble( qtyStr == null || qtyStr.trim().length() == 0 ? "0" : qtyStr.trim() );
						itemCode = genericUtility.getColumnValue("item_code", dom);
						unit = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
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
						if ( unit != null && !unit.trim().equals( unitStd.trim() ) )
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
						//added  by nandkumar gadkari- on 30/10/18 start ------------
						lotNo =genericUtility.getColumnValue("lot_no", dom);
						if (lotNo!=null && lotNo.trim().length()>0)
						{
						reStr = itemChanged(dom, dom1, dom2, objContext, "lot_no", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						}
						//added  by nandkumar gadkari---on 30/10/18 end----------
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
						
						/*
						priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);
						sRate = genericUtility.getColumnValue("rate__stduom", dom);
						if (sRate == null || sRate.equals("null"))
						{
							sRate = "0";
						}
						rateStdUom = Double.parseDouble(sRate);
						*/
						//change done by kunal on 20/AUG/13 RATE CLAG NOT GET FROM DOM
						/*
						sRate = genericUtility.getColumnValue("rate__clg", dom);
						if (sRate == null || sRate.equals("null"))
						{
							sRate = "0";
						}
						
						rateClg = Double.parseDouble(sRate);
						*/
						if (priceListClg == null || priceListClg.trim().length() == 0)
						{
							/*
							valueXmlString.append("<rate__clg protect =\"0\">").append("<![CDATA[" + sRate + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", sRate );
							*/
							//	change by kunal on 20/AUG/13
							rateClg = 0;
							valueXmlString.append("<rate__clg protect =\"0\">").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
							setNodeValue( dom, "rate__clg", rateClg );
						}
						else
						{
							listType = distCommon.getPriceListType(priceListClg, conn);
							if (!"B".equals(listType) && !"F".equals(listType))
							{
								
								tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;	
								System.out.println(">>>>>>>>>>Check priceListClg in rate:"+priceListClg);
								rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode," ","L",qtyStdUom, conn);
								System.out.println(">>>>>>>>>>Check rateClg in rate:"+rateClg);
								System.out.println("rateClg from pick rate="+rateClg);
								//change by kunal on 20/AUG/13 get rateClg from pick rate method  
								/*if (rateClg <= 0 && priceListClg.trim().length() > 0)
								{
									tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;								
									rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode," ","L",qtyStdUom, conn);
								}*/
							}
						}
						if (rateClg <= 0 && rate > 0)
						{
							rateClg = rate;
						}
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
					} // end of rate
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
					} // end of unit
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

						valStr = genericUtility.getColumnValue( "unit__std", dom  );
						valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();
						
						mVal1 = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].GetItemString(1, "unit__std")
						mitem = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].GetItemString(1, "item_code")
						
						valStr = genericUtility.getColumnValue( "rate", dom );
						valStr = valStr == null || valStr.trim().length() == 0 ? "0" : valStr.trim();
						
						mrate = Double.parseDouble( valStr ); //dw_detedit[ii_currformno].getitemnumber(1, "rate")
						mconvRtuom = 0;
						
						valStr = distCommon.convQtyFactor(Double.toString( mVal1 ), mcode, mitem, mrate, mconvRtuom, conn).get(1).toString();
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
					else if (currentColumn.trim().equals("line_no__inv") )		
					{
						String taxClassVal = null;
						String taxChapVal = null;
						String taxEnvVal = null;

						lineNoTrace = genericUtility.getColumnValue("line_no__inv", dom);
						lineNoTrace = lineNoTrace == null ?"0" : lineNoTrace.trim();
						iLineNoTrace = Integer.parseInt( lineNoTrace ) ;
						invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
						retOpt = genericUtility.getColumnValue("ret_opt", dom1);
						
						if (invoiceId != null && invoiceId.trim().length() > 0 && lineNoTrace != null && lineNoTrace.trim().length() > 0 )
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
							
							sql = " select item_code, quantity, tax_class, tax_chap, tax_env, rate, rate__clg "
								+ " from invdet where invoice_id = ? and line_no = ? ";	
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1, invoiceId );
							pstmt.setString( 2, lineNoTrace );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								itemCode = rs.getString(1);
								quantity = rs.getDouble(2);
								taxClasss = rs.getString(3);
								taxChap = rs.getString(4);
								taxEnv = rs.getString(5);
								rate = rs.getDouble(6);
								rateClg = rs.getDouble(7);
								
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
								
								siteCode = genericUtility.getColumnValue("site_code", dom1);
								custCode = genericUtility.getColumnValue("cust_code", dom);
								tranDate = genericUtility.getColumnValue("tran_date", dom1);
								
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
									
								itemSer = distCommon.getItemSer(itemCode,siteCode,
										Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"),
										custCode,"C",conn);
								valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
								setNodeValue( dom, "item_ser", itemSer );
								sql = " select descr, unit, unit__rate from item where item_code = ? ";	
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, itemCode );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									descr = rs.getString(1);
									unit = rs.getString(2);
									unitRate = rs.getString(3);
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
								pickLowerRate = distCommon.getDisparams("999999","PICK_LOWER_RATE", conn);
								System.out.println( "pickLowerRate :: " + pickLowerRate ); 
								if (pickLowerRate.equals("NULLFOUND"))
								{
									pickLowerRate = "N";
								}
								if (pickLowerRate.equals("Y"))
								{
									priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
									System.out.println(">>>>>>>>>>Check priceList in line_no__inv:"+priceList);
									
									if (priceList != null && priceList.trim().length() > 0 && !"null".equalsIgnoreCase( priceList.trim() ) )
									{
										tranDate = tranDate == null ? ( new Timestamp( System.currentTimeMillis() ) ).toString() : tranDate;
										itemCode = itemCode == null ? "" : itemCode;
										lotNo = lotNo == null ? "" : lotNo;
										priceListRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"L", quantity, conn);
										System.out.println(">>>>>>>>>>Check priceListRate in line_no__inv:"+priceListRate);
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
								
								rateClg = sRateClg ;
								priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);//added by sagar on 19/08/14..
								priceListClg = priceListClg == null ?"" : priceListClg.trim();
								if (rateClg <= 0 )
								{
									if (priceListClg.trim().length() > 0)
									{
										tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString().toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;									
										itemCode = itemCode == null ? "" : itemCode;
										lotNo = lotNo == null ? "" : lotNo;
										System.out.println(">>>>>>>>>>Check priceListClg in line_no__inv 2:"+priceListClg);
										rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode,lotNo,"L", quantity, conn);
										System.out.println(">>>>>>>>>>Check rateClg in line_no__inv 2:"+rateClg);
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
									
								valueXmlString.append("<conv__qty_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__qty_stduom>"); // column protected by nandkumar gadkari on 29/01/19
								setNodeValue( dom, "conv__qty_stduom", "1" );
								unit = unit == null ?"" : unit.trim();
								unitRate = unitRate == null ?"" : unitRate.trim();
								if ( unit.trim().equals(unitRate.trim()))
								{
									System.out.println("manohar rate__stduom 3 [" + getRequiredDecimal( rate, 4 ) + "]");
									valueXmlString.append("<rate__stduom>").append("<![CDATA[" + getRequiredDecimal( rate, 4 ) + "]]>").append("</rate__stduom>");
									setNodeValue( dom, "rate__stduom", getRequiredDecimal( rate, 4 ) );
									valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");// column protected by nandkumar gadkari on 29/01/19
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
										
								sql =" select site_code__mfg, mfg_date, exp_date, lot_no, lot_sl, desp_id, "
									+" desp_line_no, rate__std " 
									+" from invoice_trace where invoice_id = ? and line_no = ? ";	
								pstmt= conn.prepareStatement( sql );
								pstmt.setString( 1, invoiceId );
								pstmt.setInt( 2, iLineNoTrace );
								rs = pstmt.executeQuery(); 
								if( rs.next() )
								{
									siteCodeMfg = rs.getString("site_code__mfg");
									mfgDate = rs.getTimestamp("mfg_date");
									expDate = rs.getTimestamp("exp_date");
									lotNo = rs.getString("lot_no");
									lotSl = rs.getString("lot_sl");
									despId = rs.getString("desp_id");
									despLineNo = rs.getString("desp_line_no");
									rateStd = rs.getDouble("rate__std");
									
									valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + getAbsString(siteCodeMfg) + "]]>").append("</site_code__mfg>");
									setNodeValue( dom, "site_code__mfg", siteCodeMfg );
									
									valueXmlString.append("<mfg_date>").append("<![CDATA[" + genericUtility.getValidDateString( mfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) + "]]>").append("</mfg_date>");
									setNodeValue( dom, "mfg_date", genericUtility.getValidDateString( mfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) );
									
									valueXmlString.append("<exp_date>").append("<![CDATA[" + genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() )  + "]]>").append("</exp_date>");
									setNodeValue( dom, "exp_date", genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) );
									
									valueXmlString.append("<lot_no>").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
									setNodeValue( dom, "lot_no", lotNo );
										
									valueXmlString.append("<rate__std>").append("<![CDATA[" + rateStd + "]]>").append("</rate__std>");
									setNodeValue( dom, "rate__std", Double.toString( rateStd ) );
									
									valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
									setNodeValue( dom, "cost_rate", Double.toString( costRate ) );
									
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								sql = " select a.loc_code, b.descr from despatchdet a, location b "
									+ " where a.loc_code = b.loc_code "
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
								reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
							}//end if invoice id
						}// end of line_no__inv
						//Added by kunal for itemchange of line_no__invtrace D18CKOY001 
						else if (currentColumn.trim().equals("line_no__invtrace") )
						{

							String taxClassVal = null;
							String taxChapVal = null;
							String taxEnvVal = null;

							lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
							invoiceId = genericUtility.getColumnValue("invoice_id", dom);
							retOpt = genericUtility.getColumnValue("ret_opt", dom1);
							if (invoiceId != null && invoiceId.trim().length() > 0)
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
									if (invoiceId != null && invoiceId.trim().length() > 0 && lineNoTrace != null && lineNoTrace.trim().length() > 0 )
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
									//lineType = rs.getString("line_type");
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
									
									System.out.println("IC1 ItemCodeBefore"+valueXmlString);
									
									reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
									pos = reStr.indexOf("<Detail2>");
									reStr = reStr.substring(pos + 9);
									pos = reStr.indexOf("</Detail2>");
									reStr = reStr.substring(0,pos);
									valueXmlString.append(reStr);
									
									System.out.println("IC1 ItemCode"+valueXmlString);
									
									valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + getAbsString(siteCodeMfg) + "]]>").append("</site_code__mfg>");
									setNodeValue( dom, "site_code__mfg", siteCodeMfg );
									if (mfgDate != null)
									{
										valueXmlString.append("<mfg_date>").append("<![CDATA[" + genericUtility.getValidDateString( mfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) + "]]>").append("</mfg_date>");
										setNodeValue( dom, "mfg_date", genericUtility.getValidDateString( mfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) );
									}
									
									if (expDate != null)
									{
										valueXmlString.append("<exp_date>").append("<![CDATA[" + genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() )  + "]]>").append("</exp_date>");
										setNodeValue( dom, "exp_date", genericUtility.getValidDateString( expDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) );
									}
									
									valueXmlString.append("<lot_no>").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
									setNodeValue( dom, "lot_no", lotNo );
									
									valueXmlString.append("<rate__std>").append("<![CDATA[" + rateStd + "]]>").append("</rate__std>");
									setNodeValue( dom, "rate__std", Double.toString( rateStd ) );
									
									valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
									setNodeValue( dom, "cost_rate", Double.toString( costRate ) );
									/*lineType = getAbsString( lineType );
									valueXmlString.append("<line_type>").append("<![CDATA[" + lineType + "]]>").append("</line_type>");
									setNodeValue( dom, "line_type", lineType );*/
									
									valueXmlString.append("<rate__clg>").append("<![CDATA[" + sRateClg + "]]>").append("</rate__clg>");
									setNodeValue( dom, "rate__clg", Double.toString( sRateClg ) );
									
									valueXmlString.append("<lot_sl>").append("<![CDATA[" + lotSl + "]]>").append("</lot_sl>");
									setNodeValue( dom, "lot_sl", lotSl );
									
									valueXmlString.append("<quantity>").append("<![CDATA[" + getRequiredDecimal( sQuantity, 3 ) + "]]>").append("</quantity>");
									setNodeValue( dom, "quantity", getRequiredDecimal( sQuantity, 3 ) );
									
									retReplFlag = genericUtility.getColumnValue("ret_repl_flag", dom);
									siteCode = genericUtility.getColumnValue("site_code", dom1);
									locCode = genericUtility.getColumnValue("loc_code", dom);
									tranDate = genericUtility.getColumnValue("tran_date", dom1);
									//invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
									invoiceId = genericUtility.getColumnValue("invoice_id", dom);
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

									/*sql = " select sum((case when quantity is null then 0 else quantity end) * (case when rate is null then 0 else rate end)), "
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
									rs = null;*/
					
								/*	sql = " select sum(case when a.drcr_flag = 'D' then (case when b.drcr_amt is null then 0 else b.drcr_amt end) "
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
*/									
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
										valueXmlString.append("<conv__rtuom_stduom protect =\"1\">").append("<![CDATA[1]]>").append("</conv__rtuom_stduom>");// column protected by nandkumar gadkari on 29/01/19
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
									System.out.println("IC2 LotNoBefore"+valueXmlString);
									reStr = itemChanged(dom, dom1, dom2, objContext, "lot_no", editFlag, xtraParams);
									pos = reStr.indexOf("<Detail2>");
									reStr = reStr.substring(pos + 9);
									pos = reStr.indexOf("</Detail2>");
									reStr = reStr.substring(0,pos);
									valueXmlString.append(reStr);
									
									System.out.println("IC2 LotNo"+valueXmlString);
									
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
								System.out.println("IC3 quantityBefore"+valueXmlString);
								reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0,pos);
								valueXmlString.append(reStr);
								System.out.println("IC3 quantity"+valueXmlString);

							}
						
							
							
						}
						else if (currentColumn.trim().equals("lot_no") )			
						{
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
							if(invoiceId==null || invoiceId.trim().length()==0)
							{
								invoiceId = genericUtility.getColumnValue("invoice_id", dom);
							}
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							unitRate = unitRate == null ? "" : unitRate;
							unitStd = genericUtility.getColumnValue("unit__std", dom);
							retReplFlag = genericUtility.getColumnValue("ret_rep_flag", dom);
							String iValStr = genericUtility.getColumnValue("line_no__inv", dom);
							lineNoTrace=genericUtility.getColumnValue("line_no__invtrace", dom);
							if (iValStr != null && iValStr.indexOf(".") > 0)
							{
								iValStr = iValStr.substring(0,iValStr.indexOf("."));
							}
							sQuantity=checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom));//added by nandkumar gadkari on 18/07/19
							tranId = genericUtility.getColumnValue("tran_id", dom);
						/*	lineNoInv = Integer.parseInt( getNumString( iValStr ) );
							iValStr = genericUtility.getColumnValue( "quantity__stduom", dom);
							qtyStdUom = Double.parseDouble( getNumString( iValStr ) );
	
							lotSl =  genericUtility.getColumnValue("lot_sl", dom);
							locCode = genericUtility.getColumnValue("loc_code", dom);*/
							
							
							//Added by Nandkumar Gadkatri  on 23/10/2018  to append invoice id to itemChange retStr [Start]
							String invoiceQty = "";
							HashMap<String, String> curFormItemLotHMap = new HashMap<String, String>();
							HashMap<String, String> curRecordItemLotHMap = new HashMap<String, String>();
							String sreturnAdjOpt = "", orderByStr = "",slineNo="";
							int lineNo=0;
							String minRateDocKey = "",sql1="";
							boolean isMinHisRateSet = false;
							String applDateFormat = genericUtility.getApplDateFormat();
							SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
							String invRefId = "",invRefDate="" ,tempInvoiceId=null; 
							String shExpDiscAppl="";
							ArrayList<String> dokkeyList=null; 
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
								
								if( checkNull(invoiceId).trim().length() == 0)
								{
									curFormItemLotHMap.put("cust_code", genericUtility.getColumnValue("cust_code", dom1));
									curFormItemLotHMap.put("item_code", itemCode);
									curFormItemLotHMap.put("lot_no", lotNo);
									curFormItemLotHMap.put("site_code", siteCode);
									curFormItemLotHMap.put("quantity", genericUtility.getColumnValue("quantity", dom));
									
									custCode = genericUtility.getColumnValue("cust_code", dom1);
									slineNo =  genericUtility.getColumnValue("line_no", dom);
								
									
									/*sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
										+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
										+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
										
										+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
										+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
										
										+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
										+ " AND MRH.QUANTITY IS NOT NULL"
										+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
										+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0"*/
									//sql added for remove join with sreturnDet table  by nandkumar gadkari on 18/07/19
									sql =" SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
										+ "	CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END AS QTY_ADJ, MRH.EFF_COST,MRH.DOC_KEY "// MRH.DOC_KEY added by nandkumar on 21/08/19
										+ " FROM MIN_RATE_HISTORY MRH "
										+ " WHERE  MRH.CUST_CODE = ?"
										+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
										+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END >= ?  "
										+ " AND MRH.QUANTITY IS NOT NULL"
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
										rate = rs.getDouble("EFF_COST");
										
										curRecordItemLotHMap.put("cust_code", checkNull(rs.getString("CUST_CODE")));
										curRecordItemLotHMap.put("item_code", checkNull(rs.getString("ITEM_CODE")));
										curRecordItemLotHMap.put("lot_no", checkNull(rs.getString("LOT_NO")));
										curRecordItemLotHMap.put("site_code", checkNull(rs.getString("SITE_CODE")));
										curRecordItemLotHMap.put("quantity", invoiceQty);
									
											dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);
										
										
										int size = dokkeyList.size();
										System.out.println("dokkk key size: " +size);
										for(int i=0;i<=1;i++)
										{
											cnt=0;
												minRateDocKey = dokkeyList.get(i);
												
												String docKeyvalue="";
												if (minRateDocKey.trim().length() > 0) {
													
													String[] docKeyStr = minRateDocKey.split(",");
													
													for(int j=0; j<docKeyStr.length; j++)
													{	
														
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
													//sql1 = "SELECT COUNT(*) FROM MIN_RATE_HISTORY WHERE DOC_KEY =? AND QUANTITY - CASE WHEN QUANTITY_ADJ IS NULL THEN 0 ELSE QUANTITY_ADJ END > 0 ";commented and sql changed by nandkumar gadkari on 26/07/19
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
									
									
									if (tempInvoiceId == null || tempInvoiceId.trim().length() == 0)
									{
										sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
												//+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, " //COMMENTED BY NANDKUMAR GADKARI ON 28/01/19
												+ " MRH.EFF_COST"
												+ " FROM MIN_RATE_HISTORY MRH"
												//+ ",  SRETURNDET SRDET" //COMMENTED BY NANDKUMAR GADKARI ON 28/01/19
												+ " WHERE "
												//+ "MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?" //COMMENTED BY NANDKUMAR GADKARI ON 28/01/19
												//+ " AND "
												+ " MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
											
											//	+ " AND MRH.SITE_CODE = ? "commented by nandkumar gadkari on 26/07/19
											//	+ " AND MRH.QUANTITY IS NOT NULL" //COMMENTED BY NANDKUMAR GADKARI ON 28/01/19
												+ " AND CASE WHEN MRH.STATUS IS NULL THEN 'A' ELSE MRH.STATUS END <> 'X' "// added by nandkumar gadkari on 30/12/19
												+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE "
												
												+ orderByStr;
											pstmt = conn.prepareStatement(sql);
											//pstmt.setString(1, custCode);
											pstmt.setString(1, itemCode);
											pstmt.setString(2, lotNo);
											//pstmt.setString(3, siteCode);commented by nandkumar gadkari on 26/07/19
											rs = pstmt.executeQuery();
											
											while(rs.next())
											{
												invoiceId = checkNull(rs.getString("INVOICE_ID"));
												invoiceQty = checkNull(rs.getString("QUANTITY"));
												//adjQty = checkNull(rs.getString("QTY_ADJ")); //COMMENTED BY NANDKUMAR GADKARI ON 28/01/19
												
												rate = rs.getDouble("EFF_COST");
												
												curRecordItemLotHMap.put("cust_code", checkNull(rs.getString("CUST_CODE")));
												curRecordItemLotHMap.put("item_code", checkNull(rs.getString("ITEM_CODE")));
												curRecordItemLotHMap.put("lot_no", checkNull(rs.getString("LOT_NO")));
												curRecordItemLotHMap.put("site_code", checkNull(rs.getString("SITE_CODE")));
												curRecordItemLotHMap.put("quantity", invoiceQty);
											
													dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);
												
												int size = dokkeyList.size();
												System.out.println("dokkk key size: " +size);
												for(int i=0;i<=size-1;i++)
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
									
									
									if( invoiceId != null && invoiceId.trim().length() > 0 )
									{
										String historyType = ""; //added by rupali on 01/04/2021
										isMinHisRateSet = true;
										valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
										valueXmlString.append("<doc_key>").append("<![CDATA[" + minRateDocKey + "]]>").append("</doc_key>");
									
										setNodeValue( dom, "doc_key",  minRateDocKey  );
										String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
										System.out.println("dokkk key : " +docKey);
										System.out.println("docKey " +docKey);
										mrhCnt=0; //added by Nandkumar Gadkari on 14/09/19
										if(docKey.trim().length() > 0 || docKey != null )
										{
											System.out.println("inside dok key1 : " +docKey);
											sqlStr = " select INVOICE_ID , INVOICE_DATE ,EFF_COST , HISTORY_TYPE  from MIN_RATE_HISTORY where DOC_KEY = ? " //trim(:ls_itemcode);
													+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
											pstmt = conn.prepareStatement( sqlStr );
											pstmt.setString( 1, docKey );
											rs = pstmt.executeQuery();
											if( rs.next() )
											{
												mrhCnt++;//added by Nandkumar Gadkari on 14/09/19
												invRefId = checkNullandTrim(rs.getString( "INVOICE_ID" ));
												invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
												rate = rs.getDouble( "EFF_COST" );
												historyType = checkNullandTrim(rs.getString( "HISTORY_TYPE" )); //added by rupali on 01/04/2021
											}
											if( rs != null )
												rs.close();
											rs = null;
											if( pstmt != null )
												pstmt.close();
											pstmt = null;
											
											System.out.println("inside historyType:::::"+historyType);
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
												//added by rupali on 01/04/2021 [start]
												//valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
												if("S".equalsIgnoreCase(historyType))
												{
													valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
												}
												else
												{
													valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + rate + "]]>").append("</rate>");
												}
												//added by rupali on 01/04/2021 [end]
												System.out.println("inside dok key1 invoiceId : " +invRefId);
												if(docKey.indexOf(invRefId) != -1)//if added by Nandkumar Gadkari on 14/09/19
												{
													valueXmlString.append("<invoice_ref>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_ref>");//added by Nandkumar Gadkari on 14/09/19
													if(invRefDate != null && invRefDate.trim().length() >0)
													{
														valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
													}
												}
												else//else added by Nandkumar Gadkari on 14/09/19
												{
													valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
													valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
												}
											}
											//commented set invoice id and line_no inv trace by nandkumar gadkari on 29/08/19
										/*	//Added by Nandkumar Gadkari on 30/10/18--------start-----------for set invoice id and line_no inv trace 
											cnt=0;
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
											if(cnt ==5)
											{
												sqlStr = "select Count(*) cnt from INVOICE_TRACE  where invoice_id = ? "; //trim(:ls_itemcode);
												pstmt = conn.prepareStatement( sqlStr );
												pstmt.setString( 1, invRefId );
												rs = pstmt.executeQuery();
												if( rs.next() )
												{
													cnt = rs.getInt(1); 
												}
												if( rs != null )
													rs.close();
												rs = null;
												if( pstmt != null )
													pstmt.close();
												pstmt = null;
												if(cnt > 0)
												{	
													int lineNoTraceref =0 , lineNoSR=0;
													valueXmlString.append("<invoice_id>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_id>");
													
													sqlStr = " select INV_LINE_NO , LINE_NO from INVOICE_TRACE WHERE INVOICE_ID= ? AND ITEM_CODE=? AND LOT_NO= ? ";//LINE_NO  COLUMN ADDED BY NANDKUMAR GADKARI ON 12/03/19
													if(lotSl!=null && lotSl.trim().length() >0 )
													{
													sqlStr = sqlStr		+ "AND LOT_SL= ? "; 
													}
													pstmt = conn.prepareStatement( sqlStr );
													pstmt.setString( 1, invRefId );
													pstmt.setString( 2, itemCode );
													pstmt.setString( 3, lotNo );
													if(lotSl!=null && lotSl.trim().length() >0 )
													{
													pstmt.setString( 4, lotSl );
													}
													rs = pstmt.executeQuery();
													if( rs.next() )
													{
														lineNoTraceref = rs.getInt(1); 
														lineNoSR = rs.getInt(2); 
														valueXmlString.append("<line_no__inv>").append("<![CDATA["+ lineNoTraceref +"]]>").append("</line_no__inv>");
														valueXmlString.append("<line_no__invtrace>").append("<![CDATA["+ lineNoSR +"]]>").append("</line_no__invtrace>");//lineNoSR set by  NANDKUMAR GADKARI ON 12/03/19
													}
													if( rs != null )
														rs.close();
													rs = null;
													if( pstmt != null )
														pstmt.close();
													pstmt = null;
												}
											
											}
											else
											{
												valueXmlString.append("<line_no__inv>").append("<![CDATA[]]>").append("</line_no__inv>");
												valueXmlString.append("<line_no__invtrace>").append("<![CDATA[]]>").append("</line_no__invtrace>");
											}
											//Added by Nandkumar Gadkari on 30/10/18--------end-----------for set invoice id and line_no inv trace 
*/											
										}
										
										invoiceId = null;
										
									}
									else
									{
										rate=0;//added by Nandkumar Gadkari on 14/09/19
										valueXmlString.append("<doc_key>").append("<![CDATA[]]>").append("</doc_key>");
										valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
										valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
										
									}
								}
								else if(invoiceId != null && invoiceId.trim().length()>0)
								{
									
									dokkeyList= generateDocKey(dom1, dom, invoiceId, conn);
									curFormItemLotHMap.put("cust_code", genericUtility.getColumnValue("cust_code", dom1));
									curFormItemLotHMap.put("item_code", itemCode);
									curFormItemLotHMap.put("lot_no", lotNo);
									curFormItemLotHMap.put("site_code", siteCode);
									curFormItemLotHMap.put("quantity", genericUtility.getColumnValue("quantity", dom));
									custCode = genericUtility.getColumnValue("cust_code", dom1);
									slineNo = genericUtility.getColumnValue("line_no", dom);
									lineNo = Integer.parseInt(slineNo);

									int size = dokkeyList.size();
									for(int i=0;i<=size-1;i++)
									{
										minRateDocKey = dokkeyList.get(i);
										  System.out.println("@@@@@@@minRateDocKey ..........[" + minRateDocKey+"]");
									
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
									}
									if (tempInvoiceId != null && tempInvoiceId.trim().length() > 0)
									{
										break;
									}
									
									

								}
								
									String historyType = ""; //added by rupali on 01/04/2021
									sql = "SELECT DOC_KEY,EFF_COST,HISTORY_TYPE FROM MIN_RATE_HISTORY WHERE DOC_KEY = ?"
										+ " AND CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END <> 'X' ";// added by nandkumar gadkari on 30/12/19
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, minRateDocKey);
									rs = pstmt.executeQuery();
									
									if(rs.next())
									{
										minRateDocKey = checkNull(rs.getString("DOC_KEY"));
										rate = rs.getDouble("EFF_COST");
										historyType = checkNullandTrim(rs.getString( "HISTORY_TYPE" )); //added by rupali on 01/04/2021
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
									System.out.println("historyType::::::::::::::"+historyType);
									isMinHisRateSet = true;
									//added by rupali on 01/04/2021 [start]
									//valueXmlString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
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
									
									setNodeValue( dom, "doc_key",  minRateDocKey  );
									String docKey = checkNull(genericUtility.getColumnValue( "doc_key", dom ));
									System.out.println(" dok key : " +docKey);
									mrhCnt=0; //added by Nandkumar Gadkari on 14/09/19
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
											mrhCnt++;//added by Nandkumar Gadkari on 14/09/19
											invRefId = checkNullandTrim(rs.getString( "INVOICE_ID" ));
											invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
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
												if(invRefDate != null && invRefDate.trim().length() >0)
												{
													valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
												}
											}
											else
											{
												rate=0;
												valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
												valueXmlString.append("<doc_key>").append("<![CDATA[]]>").append("</doc_key>");
												valueXmlString.append("<invoice_ref>").append("<![CDATA[]]>").append("</invoice_ref>");
												valueXmlString.append("<inv_ref_date>").append("<![CDATA[]]>").append("</inv_ref_date>");
											}
										}
									}
									
									invoiceId = null;
								}
								//Added by Nandkumar Gadkatri  on 23/10/2018  to check if invoice_id reference is available in Min_rate_history [End]
							}
							//Added by Nandkumar Gadkatri  on 23/10/2018 to append invoice id to itemChange retStr [End]
							
							
							
							
							/*quantity = qtyStdUom;
	
							infoMap = new HashMap();*/ //COMMENTED BY NANDKUMAR GADKARI ON 05/11/18
						// ADDED BY NANDKUMAR GADKARI ON 05/11/18--------------------START ----------------
							infoMap = new HashMap();
							// 
							iValStr = iValStr == null || iValStr.trim().length() == 0 ? "0" : iValStr.trim();
							lineNoInv = Integer.parseInt( getNumString( iValStr ) );
							iValStr = genericUtility.getColumnValue( "quantity__stduom", dom);
							qtyStdUom = Double.parseDouble( getNumString( iValStr ) );
							System.out.println( " qtyStdUom :1: " + qtyStdUom );
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
							// ADDED BY NANDKUMAR GADKARI ON 05/11/18--------------------END ----------------
							infoMap.put("ret_repl_flag",retReplFlag);
							infoMap.put("item_code", itemCode);
							infoMap.put("site_code", siteCode);
							infoMap.put("loc_code",locCode);
							infoMap.put("lot_no", lotNo);
							infoMap.put("lot_sl", lotSl);
							infoMap.put("tran_date", tranDate);
							infoMap.put("invoice_id", invoiceId);
						//	infoMap.put("line_no__invtrace",lineNoTrace);
						//	infoMap.put( "quantity__stduom", new Double( -1 * quantity ) ); commented by nandkumar gadkari on 6/11/18 for non invoice id cost rate set as -1  due to quantity in negative
							infoMap.put( "quantity__stduom", new Double(quantity ) );
							priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 18/08/14..
							System.out.println(">>>>>>>>>>Check priceList for getCost Rate 2:"+priceList);
							infoMap.put( "price_list", priceList);
							costRate = getCostRate(infoMap, conn);
							infoMap = null;
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
									}
								} // retReplFlag = R
								else
								{
									priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
									System.out.println(">>>>>>>>>>Check priceList in lot_no:"+priceList);
									if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
									{
										tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
										itemCode = itemCode == null ? "" : itemCode;
										lotNo = lotNo == null ? "" : lotNo;
										rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
										System.out.println(">>>>>>>>>>Check rate in lot_no:"+rate);
										System.out.println( "rate pic rate LOt NO :: " + rate );
									}
								}
							} //invoice_id
							else
							{
								System.out.println( "retReplFlag :2 : " + retReplFlag );
								//Changed by Nandkumat Gadkari  on 23/10/2018 to not to set rate if already set from min_rate_history
								//if (retReplFlag != null && retReplFlag.equals("R"))
								if (retReplFlag != null && retReplFlag.equals("R")  && !isMinHisRateSet)
								{	
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
									priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
									System.out.println(">>>>>>>>>>Check priceList in lot_no 2:"+priceList);
									
									if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
									{
										tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
										itemCode = itemCode == null ? "" : itemCode;
										lotNo = lotNo == null ? "" : lotNo;
										rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
										System.out.println(">>>>>>>>>>Check rate in lot_no 2:"+rate);
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
							priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
							System.out.println(">>>>>>>>>>Check priceList in lot_no 3:"+priceList);
						
							if (priceList !=null && priceList.trim().length() >  0)
							{
								tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;							
								itemCode = itemCode == null ? "" : itemCode;
								lotNo = lotNo == null ? "" : lotNo;
								priceListRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D", quantity, conn);
								System.out.println(">>>>>>>>>>Check priceListRate in lot_no 3:"+priceListRate);
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
							priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);//added by sagar on 19/08/14..
							System.out.println(">>>>>>>>>>Check priceListClg in lot_no 4:"+priceListClg);
							if ( priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
							{
								tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
								itemCode = itemCode == null ? "" : itemCode;
								lotNo = lotNo == null ? "" : lotNo;								
								rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode,lotNo,"L", quantity, conn);
								System.out.println(">>>>>>>>>>Check rateClg in lot_no 4:"+rateClg);
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
						valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
						setNodeValue( dom, "discount", discount );
						System.out.println("END getMinRate start");
						
						//Pavan R on 24Jul19 start [to set lot_sl as per same from SalesReturn]
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
				        		setNodeValue( dom, "lot_sl","2S");
				        	}
				        	else
				        	{
				        		System.out.println("lsSretLotsl["+lsSretLotsl+"]");
				        		valueXmlString.append("<lot_sl>").append("<![CDATA[" + lsSretLotsl + "]]>").append("</lot_sl>");
				        		setNodeValue( dom, "lot_sl",lsSretLotsl);
				        	}
				        }
				        else
				        {
				        	valueXmlString.append("<lot_sl>").append("<![CDATA[" + lslotsl + "]]>").append("</lot_sl>");
				        	setNodeValue( dom, "lot_sl",lslotsl);
				        }
				        //Pavan R on 24Jul19 end
						
						
						//Changed by Nandkumar Gadkari  on 23/10/2018 to not to set rate if already set from min_rate_history [Start]
						//StringBuffer minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn );
					//	StringBuffer minRateBuff = null;
						if(!isMinHisRateSet)
						{
							
							StringBuffer minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ); 
							System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );
							valueXmlString = minRateBuff;
						}
						//Changed by Nandkumar Gadkari  on 23/10/2018 to not to set rate if already set from min_rate_history [End]
						//valueXmlString = minRateBuff;
						
						//Pavan Rane 22Jul19[to set mfg_date from item_lot_info as per same logic from salesReturn]						
						valueXmlString = (gbfIcExpMfgDate(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,
								conn));
						System.out.println("lslotsl["+lslotsl+"]");
						
						//Pavan Rane 22Jul19 end 
						
						reStr = itemChanged(dom, dom1, dom2, objContext, "lot_sl", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
					}//end of lot_no
					else if (currentColumn.trim().equals("lot_sl") )		
					{
						//Pavan Rane 22Jul19[to set mfg_date from item_lot_info as per same logic from salesReturn]
						valueXmlString = (gbfIcExpMfgDate(valueXmlString, dom, dom1, dom2, editFlag, xtraParams, objContext,conn));						
						//Pavan Rane 22Jul19 end
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						locCode = genericUtility.getColumnValue("loc_code", dom);
						lotNo = genericUtility.getColumnValue("lot_no", dom);
						lotSl = genericUtility.getColumnValue("lot_sl", dom);
						custCode = genericUtility.getColumnValue("cust_code", dom1);
						
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
						//Changed by nandkumar gahdari  28/01/2019 to set values from item_lot_info
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
							System.out.println("4008 cnt...["+cnt+"]");
							if (cnt == 0 )
							{	
								//valueXmlString.append("<exp_date  protect =\"0\">").append("<![CDATA[" + ( expDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString()  : "" ) + "]]>").append("</exp_date>");
								valueXmlString.append("<pack_code  protect =\"0\">").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
								valueXmlString.append("<site_code__mfg  protect =\"0\">").append("<![CDATA[" + siteCodeMfg + "]]>").append("</site_code__mfg>");
							}
							else
							{
								//Pavan Rane 22Jul19[to set mfg_date from item_lot_info as per same logic from salesReturn]
								//valueXmlString.append("<mfg_date  protect =\"1\">").append("<![CDATA[" + ( mfgDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(mfgDate).toString() : "" ) + "]]>").append("</mfg_date>");
								//valueXmlString.append("<exp_date  protect =\"1\">").append("<![CDATA[" + ( expDate != null ? new SimpleDateFormat(genericUtility.getApplDateFormat()).format(expDate).toString() : "" ) + "]]>").append("</exp_date>");
								valueXmlString.append("<pack_code  protect =\"1\">").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
								valueXmlString.append("<site_code__mfg  protect =\"1\">").append("<![CDATA[" + siteCodeMfg + "]]>").append("</site_code__mfg>");
							}
							mtranDateStr = genericUtility.getColumnValue( "tran_date", dom );
							mtranDate = Timestamp.valueOf(genericUtility.getValidDateString( ( mtranDateStr == null ? getCurrdateInAppFormat() : mtranDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							String mexpDateStr = genericUtility.getColumnValue( "exp_date", dom );
							System.out.println( "SRFmexpDateStr :: " + mexpDateStr );
							// 20/05/10 manoharan commented as expiry date is already fetched from stock
							//if( mexpDateStr != null )
							//{
							//	mexpDate = Timestamp.valueOf(genericUtility.getValidDateString( ( mexpDateStr == null ? getCurrdateInAppFormat() : mexpDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							//}
							//else
							//{
							//Commented By Mahesh Patidar on 04/05/2012 for set expire date from dom if not found in stock
								//mexpDate = expDate; //genericUtility.getValidDateString( ( mexpDateStr == null ?  : mexpDateStr ), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							//}
							
							//Added By Mahesh Patidar on 04/05/2012 for  set expire date from dom if not found in stock
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
							sqlStr =" select channel_partner " // into :ls_channel_partner
								   +" from site_customer "
								   +" where cust_code = ? " //:ls_cust_code
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
							System.out.println("site_customer channelPartner [" + channelPartner + "]"); 

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
								System.out.println("customer channelPartner [" + channelPartner + "]"); 

							}
							if ( (channelPartner != null && !"Y".equals( channelPartner )) || (channelPartner == null || "N".equals( channelPartner )) )
							{
								
								sqlStr =" select deduct_perc " //into :mdisc 
									+" from sreturn_norms "
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
							// else part added by Mahesh Patidar on 28/03/12 for discount value change according to lot_sl
							else
							{
								itemCode = genericUtility.getColumnValue("item_code", dom);
								invoiceId = genericUtility.getColumnValue("invoice_id", dom1);
								retReplFlag = genericUtility.getColumnValue("ret_rep_flag", dom);
								lineNoTrace = genericUtility.getColumnValue("line_no__invtrace", dom);
								iLineNoTrace =  Integer.parseInt( getNumString( lineNoTrace ) );
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
								valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
								setNodeValue( dom, "discount", discount );
							} // ended else by Mahesh
							
							mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 );
							mtranDate	=  Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//dw_header.getitemdatetime(1,"tran_date")
							sqlStr = "select count(*) mcount1 " //into :mcount1
									+" from stock "
									+" where item_code = ? " //:mitem_code 
									+"   and site_code = ? " //:msite_code
								//	+"   and loc_code = ? "//:mloc_code commented by nandkumar gadkari on 12/04/19 
									+"   and lot_no = ? " //:mlot_no
									+"   and lot_sl = ? " //:mlot_sl
									+"   and exp_date <= ? "; //:mtran_date;
								 
							pstmt = conn.prepareStatement( sqlStr );
							
							pstmt.setString( 1, itemCode );
							pstmt.setString( 2, siteCode );
							//pstmt.setString( 3, locCode );//:mloc_code commented by nandkumar gadkari on 12/04/19 
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

									//commented by nandkumar gadkari on 18/11/19
									/*mchkDate = distCommon.CalcExpiry( mtranDate, mminShlife );
									
									System.out.println("mchkDate["+mchkDate+"] mexpDate["+mexpDate+"]");
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
							mlineNoInv = Integer.parseInt( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) );//dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),"line_no__inv")
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
							
							if( lsInvoiceItem != null && lsInvoiceItem.trim().equals(mcode.trim() ) && lsInvoiceId.trim().length() > 0 )
							{
								valueXmlString.append("<stk_opt>").append("<![CDATA[" + "N" + "]]>").append("</stk_opt>");
								setNodeValue( dom, "stk_opt", "N" );
								//tempNode = dom.getElementsByTagName("stk_opt").item(0);
								//tempNode.getFirstChild().setNodeValue( "N" );
								//tempNode = null;

								//dw_detedit[ii_currformno].setitem(1,"stk_opt",'N')
							}
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
							// ADDED BY NANDKUMAR GADKARI ON 21/09/19
							reStr = itemChanged(dom, dom1, dom2, objContext, "status", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							
						
					} // end of lot_sl
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
					} // end of loc_code
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
								setNodeValue( dom, "exp_date", "" ); //Added By Mahesh Patidar on 04/05/2012 for set expiry date in node
							}
							else
							{	
								mexpDate  = distCommon.CalcExpiry( mmfgDate, mshlife );
								valueXmlString.append("<exp_date>").append("<![CDATA[" + genericUtility.getValidDateString( mexpDate.toString(),genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) + "]]>").append("</exp_date>");
								setNodeValue( dom, "exp_date",  genericUtility.getValidDateString( mexpDate.toString(),genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ));//Added By Mahesh Patidar on 04/05/2012 for set expiry date in node
							}
						}
						else
						{
							mexpDate = null; //setnull(mexp_date)
							valueXmlString.append("<exp_date>").append("<![CDATA["+""+"]]>").append("</exp_date>");
							setNodeValue( dom, "exp_date", "" );//Added By Mahesh Patidar on 04/05/2012 for set expiry date in node
						}
						
						//dw_detedit[ii_currformno].setitem(1,"exp_date",mexp_date)		
						
						if((mexpDate != null ) && (!(mexpDate.equals(""))))
						{
							System.out.println("Done   ===========");
							mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 ); // dw_header.getitemdatetime(1,"tran_date")
							mtranDate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");// Added By Mahesh Patidar on 04/05/12 for convert the trandate string to tran date
							mtranDateStr = genericUtility.getColumnValue( "exp_date", dom ); //Added By Mahesh on 04/05/12 for getting the expiry date
							mexpDate  = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							
							mtranDateStr = genericUtility.getColumnValue( "lr_date", dom1 ); // dw_header.getitemdatetime(1,"lr_date")
							//mtranDate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//Commented By Mahesh Patidar on 04/05/12
							// 17/06/10 manoharan commented and put in else condition as null is to be taken care off
							//ldtLrdate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							//Commented By Mahesh Patidar on 04/05/12 for checking the lr date is null or not i null then set tran date as a lr date  
							//if( ldtLrdate == null || "19000101".equals( genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), "yyyymmdd" ) ) ) //Isnull(ldt_lrdate) or string(ldt_lrdate,"yyyymmdd") = "19000101" then
							//{	
							//	ldtLrdate = mtranDate;
							//}
							//Ended By Mahesh Patidar
							//Added By Mahesh Patidar on 04/05/12 for checking lr date is null or not
							if( mtranDateStr == null || !(mtranDateStr.trim().length() > 0) || "19000101".equals( genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), "yyyymmdd" ) ) ) //Isnull(ldt_lrdate) or string(ldt_lrdate,"yyyymmdd") = "19000101" then
							{	
								ldtLrdate = mtranDate;
							}
							//Ended By Mahesh Patidar
							else
							{
								ldtLrdate = Timestamp.valueOf(genericUtility.getValidDateString( mtranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

							}
							// end 17/06/10 manoharan
							mdiffDays = (int)utilMethods.DaysAfter( ldtLrdate, mexpDate );
							mdays = mdiffDays;
							custCode = genericUtility.getColumnValue( "cust_code", dom1 ); //dw_header.getitemstring(1,"cust_code")
							siteCode = genericUtility.getColumnValue( "site_code", dom1 ); // dw_header.getitemstring(1,"site_code")
							
							sqlStr = " select channel_partner ls_channel_partner " //into :ls_channel_partner
									+"	from site_customer "
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
							
							if( !"Y".equalsIgnoreCase( channelPartner ) )
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
					
					
						// added by cpatil on 3-SEP-12 for status start
						mitemCode = genericUtility.getColumnValue( "item_code", dom ); 
						String expDateStr = genericUtility.getColumnValue( "exp_date", dom );
						lotNo	= genericUtility.getColumnValue( "lot_no", dom ); 
						lotSl	= genericUtility.getColumnValue( "lot_sl", dom );
						if(lotNo != null && lotSl != null )
						{
						sqlStr = " select count(*) from stock where lot_no = ? and lot_sl= ?  and item_code = ? " ;
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, lotNo );
						pstmt.setString( 2, lotSl );
						pstmt.setString( 3, mitemCode );
						
						rs = pstmt.executeQuery();
						
						if( rs.next() )
						{
							cnt5 = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(cnt5 == 0)
						{	
						if( dtStr!= null &&  expDateStr != null)       //mmfgDate
						{
							expDate = Timestamp.valueOf(genericUtility.getValidDateString(expDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						
						if( expDateStr != null )
						{	
							if(expDate.after(ldtLrdate))
							{
								valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
							}
							else if ( expDate.before(ldtLrdate) )
							{
								
							sqlStr = " select ( case when shelf_life is null then 0 else shelf_life end ) mshlife "
									+ " from item_lot_packsize where item_code = ?  " ;
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
							
							if( mshlife == 0 )
							{
								mshlife = 0;
								sqlStr = " select loc_code, shelf_life mshlife from item  where item_code = ? "; 
							
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, mitemCode );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mlocCode = rs.getString ("loc_code");
									mshlife = rs.getDouble( "mshlife" );
								}
								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							System.out.println("@@@@@ :mshlife:["+mshlife+"]:::mlocCode:["+mlocCode+"]");
							if( mshlife == 0 )
							{
								valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"E"+"]]>").append("</status>");
							}
							else
							  {
								/*if( "NEXP".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"N"+"]]>").append("</status>");
								}
								else*/  //commented by nandkumar gadkari on 18/11/19
								if( "HOLD".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"H"+"]]>").append("</status>");
								}
								else if( "DMGD".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"D"+"]]>").append("</status>");
								}
								else if( "EXP".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"E"+"]]>").append("</status>");
								}
								else if( "FRSH".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
								}
								else
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
								}
							 }
						   }
						else if ( expDate.equals(ldtLrdate) )
						{
							valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
						}
						}
						}												
						
						}	
					//	added by cpatil on 3-SEP-12 End
					
					
					
					
					
					
					
					
					
					}//end of mfg_date.
					else if (currentColumn.trim().equals( "status" ) )
					{
					
						mstatus = genericUtility.getColumnValue( "status", dom );// dw_detedit[ii_currformno].getitemstring(1,"status")
						System.out.println( " mstatus :1: " + mstatus ); 
						lsSretLocCode = distCommon.getDisparams( "999999", "SRET_LOC_CODE", conn );
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
								
								lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom ); //dw_header.getitemstring(1, "invoice_id")
								System.out.println( "lsInvoiceId :157: " + lsInvoiceId ); 
								if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
								{
									boolean recordNotFound = true;
									mlineNoInv = Integer.parseInt( getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) ) ); //dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].getrow(),'line_no__inv')
									
									System.out.println( "recordNotFound :177: " + recordNotFound ); 
									sqlStr = " select desp_id, desp_line_no " //  into :ls_desp_id, :ls_desp_line_no
											+"		from invoice_trace " 
											+" where invoice_id = ? " //:ls_invoice_id
											+"	and line_no = ? "; //:ll_linenotrace;
									
									pstmt = conn.prepareStatement( sqlStr );
									
									pstmt.setString( 1, lsInvoiceId );
									pstmt.setInt( 2, mlineNoInv );
										
									rs = pstmt.executeQuery();
									if( rs.next() )
									{ 
										lsDespId = rs.getString( "desp_id" );
										lsDespLineNo = rs.getString( "desp_line_no" );
										recordNotFound = false;
									}
									else
									{
										System.out.println( "recordNotFound :10: " + recordNotFound ); 
										lsDespLineNo = ("   "+ mlineNoInv).substring( 0, 3 );
										
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
						mcode = genericUtility.getColumnValue( "item_code", dom ); // dw_detedit[ii_currformno].getitemstring(dw_detedit[ii_currformno].getrow(),"item_code")
						System.out.println( "lsRetRepFlag :7: " + lsRetRepFlag ); 
						if( !"P".equalsIgnoreCase( lsRetRepFlag ) )
						{	
							sqlStr = " select item_code ls_invoice_item " 
								  +" from invoice_trace "
								  +" where invoice_id  = ? " //:ls_invoice_id
								  +"	and inv_line_no = ? "; //:mline_no_inv;
						
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
					////added by nandkumar gadkari on 23/09/19--------Start-----------
						sqlStr = " Select reason_descr mreason_descr "
								+ " ,reason_code, loc_code  "
								+"	from sreturn_reason "
								+"	where status = ? "; 
						
						pstmt = conn.prepareStatement( sqlStr );
						
						pstmt.setString( 1, mstatus );
						
						rs = pstmt.executeQuery();
						
						if( rs.next() )
						{
							
							reasonCode = rs.getString( "reason_code" );
							locCode = rs.getString( "loc_code" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						

						if( locCode!=null && locCode.trim().length() > 0 &&  reasonCode!=null && reasonCode.trim().length() > 0 )
						{
							valueXmlString.append("<reas_code>").append("<![CDATA[" + reasonCode + "]]>").append("</reas_code>");
							
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
					
						
						////added by nandkumar gadkari on 123/09/19--------end
					}//end of status
					//Added By Mahesh Patidar on 07/05/12 add item change on exp_date
					else if (currentColumn.trim().equals( "exp_date" ) )
					{
						System.out.println("@@@@@ expiry date item change code executed before my code.... ");
						reStr = itemChanged(dom, dom1, dom2, objContext, "lot_sl", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);	
					
						// cpatil start	
						
						System.out.println("@@@@@ expiry date item change code executed my code.... ");
						String ldExpDateStr =  genericUtility.getColumnValue( "exp_date", dom );
						System.out.println("@@@@@  in item change ldExpDateStr"+ldExpDateStr);
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
							valueXmlString.append("<mfg_date>").append("<![CDATA["+ genericUtility.getValidDateString(mmfgDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) +"]]>").append("</mfg_date>");

							mtranDateStr = genericUtility.getColumnValue( "tran_date", dom1 );
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
							
							if( channelPartner != null && !"Y".equalsIgnoreCase( channelPartner ) )
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
						valueXmlString.append("<exp_date>").append("<![CDATA["+ ldExpDateStr +"]]>").append("</exp_date>");
						System.out.println("@@@@@ expiry date item change code end .... ");
					//	cpatil end
					
					// added by cpatil on 3-SEP-12 for status start
						System.out.println("@@@@@ expiry date item change code  status end .... ");
						mitemCode = genericUtility.getColumnValue( "item_code", dom ); 
						String expDateStr = genericUtility.getColumnValue( "exp_date", dom );
						lotNo	= genericUtility.getColumnValue( "lot_no", dom ); 
						lotSl	= genericUtility.getColumnValue( "lot_sl", dom );
						if( lotNo != null && lotSl != null )
						{	
						sqlStr = " select count(*) from stock where lot_no = ? and lot_sl= ?  " ;
						pstmt = conn.prepareStatement( sqlStr );
						pstmt.setString( 1, lotNo );
						pstmt.setString( 2, lotSl );
						rs = pstmt.executeQuery();
						
						if( rs.next() )
						{
							cnt5 = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(cnt5 == 0)
						{	
						if(expDateStr != null)
						{
							expDate = Timestamp.valueOf(genericUtility.getValidDateString(expDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						
						if( expDateStr != null )
						{	
							if(expDate.after(ldtLrdate))
							{
								valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
							}
							else if ( expDate.before(ldtLrdate) )
							{
								
							sqlStr = " select ( case when shelf_life is null then 0 else shelf_life end ) mshlife "
									+ " from item_lot_packsize where item_code = ?  " ;
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
							
							if( mshlife == 0 )
							{
								mshlife = 0;
								sqlStr = " select loc_code, shelf_life mshlife from item  where item_code = ? "; 
							
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, mitemCode );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mlocCode = rs.getString ("loc_code");
									mshlife = rs.getDouble( "mshlife" );
								}
								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							System.out.println("@@@@@ :mshlife:["+mshlife+"]:::mlocCode:["+mlocCode+"]");
							if( mshlife == 0 )
							{
								valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"E"+"]]>").append("</status>");
							}
							else
							  {
								/*if( "NEXP".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"N"+"]]>").append("</status>");
								} //commented by nandkumar gadkari on 18/11/19
								else*/ if( "HOLD".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"H"+"]]>").append("</status>");
								}
								else if( "DMGD".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"D"+"]]>").append("</status>");
								}
								else if( "EXP".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"E"+"]]>").append("</status>");
								}
								else if( "FRSH".equalsIgnoreCase(mlocCode.trim()) )
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
								}
								else
								{
									valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
								}
								
							 }
						   }
						  else if ( expDate.equals(ldtLrdate) )
						  {
							valueXmlString.append("<status protect =\"0\">").append("<![CDATA["+"S"+"]]>").append("</status>");
						  }
						}
						}												
					}	
						
					//	added by cpatil on 3-SEP-12 End
						
					}
					//ended By Mahesh Patidar
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
					// Added By PriyankaC to set Reference Date.  on 14JUNE2018
					
					else if (currentColumn.trim().equals( "invoice_ref" ))
					{	
						
						//Nandkumar Gadkari on 11/10/18--------------------------------------------(Start)---------------------------------
						String invoiceQty = "";
						HashMap<String, String> curFormItemLotHMap = new HashMap<String, String>();
						HashMap<String, String> curRecordItemLotHMap = new HashMap<String, String>();
						String sreturnAdjOpt = "", orderByStr = "",slineNo="";
						int lineNo=0;
						String minRateDocKey = "",sql1="",minRateDocKeyCnt="";
						boolean isMinHisRateSet = false;
						String applDateFormat = genericUtility.getApplDateFormat();
						SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
						String invRefId = "",invRefDate="" ,tempInvoiceId=null; 
						String shExpDiscAppl="";
						ArrayList<String> dokkeyList=null; 
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

								int size = dokkeyList.size();
								for(int i=0;i<=size-1;i++)
								{
								
									cnt=0;
									minRateDocKeyCnt = dokkeyList.get(i);
									
									String docKeyvalue="";
									if (minRateDocKeyCnt.trim().length() > 0) {
										
										String[] docKeyStr = minRateDocKeyCnt.split(",");
										
										for(int j=0; j<docKeyStr.length; j++)
										{	
											
											System.out.println( "docKeyStrlength :: " + docKeyStr.length);
											cnt++;
										}
									}
										System.out.println( "cnt :: " + cnt );
								if(cnt == 5)
								{
									
										minRateDocKey = dokkeyList.get(i);
										  System.out.println("@@@@@@@minRateDocKey .......... test 123 [" + minRateDocKey+"]");
										  System.out.println("@@@@@@@count1 .......... test 123");
									sql = " SELECT MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
											+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
											+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
											+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
											+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
											+ " AND MRH.SITE_CODE = ? "
										//	+ "AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
											+ " AND MRH.QUANTITY IS NOT NULL AND MRH.INVOICE_ID= ? AND MRH.DOC_KEY = ? "
											+ " AND CASE WHEN MRH.STATUS IS NULL THEN 'A' ELSE MRH.STATUS END <> 'X' "// added by nandkumar gadkari on 30/12/19
											+ " GROUP BY MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
											//+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0"
											+ orderByStr;
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, itemCode);
									pstmt.setString(3, lotNo);
									pstmt.setString(4, siteCode);
									pstmt.setString(5, invoiceId);
									pstmt.setString(6, minRateDocKey);
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
									
										//invoiceId = getAvailableInvId(dom,dom2, curFormItemLotHMap, curRecordItemLotHMap,invoiceId, minRateDocKey, adjQty);
										tempInvoiceId =invoiceId;	
										if (invoiceId != null && invoiceId.trim().length() > 0)
										{
											// added by rupali on 27/04/2021 [start]
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
											// added by rupali on 27/04/2021 [end]
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
									if (tempInvoiceId != null && tempInvoiceId.trim().length() > 0)
									{
										break;
									}
									
								}
							}
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
										invRefDate = checkNull(sdf.format( rs.getTimestamp( "INVOICE_DATE" )));
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
									if(invRefDate != null && invRefDate.trim().length() >0)
									{
										isInvRefDateSet= true;
										valueXmlString.append("<inv_ref_date>").append("<![CDATA["+ invRefDate+"]]>").append("</inv_ref_date>");
									}
									//commented set invoice id and line_no inv trace by nandkumar gadkari on 29/08/19
									/*//Added by Nandkumar Gadkari on 30/10/18--------start-----------for set invoice id and line_no inv trace 
									sqlStr = "select Count(*) cnt from INVOICE_TRACE  where invoice_id = ? "; //trim(:ls_itemcode);
									pstmt = conn.prepareStatement( sqlStr );
									pstmt.setString( 1, invRefId );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt(1); 
									}
									if( rs != null )
										rs.close();
									rs = null;
									if( pstmt != null )
										pstmt.close();
									pstmt = null;
									if(cnt > 0)
									{	
										int lineNoTraceref =0,lineNoSR=0;
										valueXmlString.append("<invoice_id>").append("<![CDATA["+ invRefId +"]]>").append("</invoice_id>");
										
										sqlStr = " select INV_LINE_NO,LINE_NO from INVOICE_TRACE WHERE INVOICE_ID= ? AND ITEM_CODE=? AND LOT_NO= ? ";//LINE NO COLUMN ADDED BY NANDKUMAR GADKARI ON 12/03/19
										if(lotSl!=null && lotSl.trim().length() >0 )
										{
										sqlStr = sqlStr		+ "AND LOT_SL= ? "; 
										}
										pstmt = conn.prepareStatement( sqlStr );
										pstmt.setString( 1, invRefId );
										pstmt.setString( 2, itemCode );
										pstmt.setString( 3, lotNo );
										if(lotSl!=null && lotSl.trim().length() >0 )
										{
										pstmt.setString( 4, lotSl );
										}
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											lineNoTraceref = rs.getInt(1); 
											lineNoSR = rs.getInt(2); 
											valueXmlString.append("<line_no__inv>").append("<![CDATA["+ lineNoTraceref +"]]>").append("</line_no__inv>");
											valueXmlString.append("<line_no__invtrace>").append("<![CDATA["+ lineNoSR +"]]>").append("</line_no__invtrace>");
										}
										if( rs != null )
											rs.close();
										rs = null;
										if( pstmt != null )
											pstmt.close();
										pstmt = null;
									}
									else
									{	
										valueXmlString.append("<invoice_id>").append("<![CDATA[]]>").append("</invoice_id>");
										valueXmlString.append("<line_no__inv>").append("<![CDATA[]]>").append("</line_no__inv>");
										valueXmlString.append("<line_no__invtrace>").append("<![CDATA[]]>").append("</line_no__invtrace>");
									}
									
									
									//Added by Nandkumar Gadkari on 30/10/18--------end-----------for set invoice id and line_no inv trace 
*/									
								}
								
								//invoiceId = null;
							}
						}
						// ADDED BY NANDKUMAR GADKARI ON 05/11/18--------------------START ----------------
						infoMap = new HashMap();
						// 
						iValStr = iValStr == null || iValStr.trim().length() == 0 ? "0" : iValStr.trim();
						lineNoInv = Integer.parseInt( getNumString( iValStr ) );
						iValStr = genericUtility.getColumnValue( "quantity__stduom", dom);
						qtyStdUom = Double.parseDouble( getNumString( iValStr ) );
						System.out.println( " qtyStdUom :1: " + qtyStdUom );
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
						// ADDED BY NANDKUMAR GADKARI ON 05/11/18--------------------END ----------------
						
						infoMap.put("ret_repl_flag",retReplFlag);
						infoMap.put("item_code", itemCode);
						infoMap.put("site_code", siteCode);
						infoMap.put("loc_code",locCode);
						infoMap.put("lot_no", lotNo);
						infoMap.put("lot_sl", lotSl);
						infoMap.put("tran_date", tranDate);
						infoMap.put("invoice_id", invoiceId);
					//	infoMap.put("line_no__invtrace",lineNoTrace);
						infoMap.put( "quantity__stduom", new Double( -1 * quantity ) );
						priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 18/08/14..
						System.out.println(">>>>>>>>>>Check priceList for getCost Rate 2:"+priceList);
						infoMap.put( "price_list", priceList);
						costRate = getCostRate(infoMap, conn);
						infoMap = null;
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
								}
							} // retReplFlag = R
							else
							{
								priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
								System.out.println(">>>>>>>>>>Check priceList in lot_no:"+priceList);
								if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
								{
									tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
									itemCode = itemCode == null ? "" : itemCode;
									lotNo = lotNo == null ? "" : lotNo;
									rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
									System.out.println(">>>>>>>>>>Check rate in lot_no:"+rate);
									System.out.println( "rate pic rate LOt NO :: " + rate );
								}
							}
						} //invoice_id
						else
						{
							System.out.println( "retReplFlag :2 : " + retReplFlag );
							//Changed by Nandkumat Gadkari  on 23/10/2018 to not to set rate if already set from min_rate_history
							//if (retReplFlag != null && retReplFlag.equals("R"))
							if (retReplFlag != null && retReplFlag.equals("R")  && !isMinHisRateSet)
							{	
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
								priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
								System.out.println(">>>>>>>>>>Check priceList in lot_no 2:"+priceList);
								
								if (priceList != null && !priceList.equals("null") && priceList.trim().length() > 0)
								{
									tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
									itemCode = itemCode == null ? "" : itemCode;
									lotNo = lotNo == null ? "" : lotNo;
									rate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
									System.out.println(">>>>>>>>>>Check rate in lot_no 2:"+rate);
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
						priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
						System.out.println(">>>>>>>>>>Check priceList in lot_no 3:"+priceList);
					
						if (priceList !=null && priceList.trim().length() >  0)
						{
							tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;							
							itemCode = itemCode == null ? "" : itemCode;
							lotNo = lotNo == null ? "" : lotNo;
							priceListRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D", quantity, conn);
							System.out.println(">>>>>>>>>>Check priceListRate in lot_no 3:"+priceListRate);
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
						priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);//added by sagar on 19/08/14..
						System.out.println(">>>>>>>>>>Check priceListClg in lot_no 4:"+priceListClg);
						if ( priceListClg != null && priceListClg.trim().length() > 0 && !"null".equalsIgnoreCase( priceListClg.trim() ) )
						{
							tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;								
							itemCode = itemCode == null ? "" : itemCode;
							lotNo = lotNo == null ? "" : lotNo;								
							rateClg = distCommon.pickRate(priceListClg,tranDate,itemCode,lotNo,"L", quantity, conn);
							System.out.println(">>>>>>>>>>Check rateClg in lot_no 4:"+rateClg);
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
					valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
					setNodeValue( dom, "discount", discount );
					System.out.println("END getMinRate start");
					//Changed by Nandkumar Gadkari  on 23/10/2018 to not to set rate if already set from min_rate_history [Start]
					//StringBuffer minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn );
				//	StringBuffer minRateBuff = null;
					if(!isMinHisRateSet)
					{
						
						StringBuffer minRateBuff = getMinRate( dom, dom1, currentColumn.trim(), valueXmlString, conn ); 
						System.out.println( "minRateBuff2 :: " + minRateBuff.toString() );
						valueXmlString = minRateBuff;
					}
					//Changed by Nandkumar Gadkari  on 23/10/2018 to not to set rate if already set from min_rate_history [End]
					//valueXmlString = minRateBuff;

					reStr = itemChanged(dom, dom1, dom2, objContext, "lot_sl", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
					}
					////added by nandkumar gadkari on 23/09/19--------start
					else if (currentColumn.trim().equals( "reas_code" ) )
					{
						reasonCode =	genericUtility.getColumnValue( "reas_code", dom );
						
						sqlStr = " Select reason_descr mreason_descr "
								+ " ,status, loc_code  "
								+"	from sreturn_reason "
								+"	where reason_code = ? "; //:mreas_code ;
						
						pstmt = conn.prepareStatement( sqlStr );
						
						pstmt.setString( 1, reasonCode );
						
						rs = pstmt.executeQuery();
						
						if( rs.next() )
						{
							
							mstatus = rs.getString( "status" );
							locCode = rs.getString( "loc_code" );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(mstatus !=null && mstatus.trim().length() > 0 && locCode!=null && locCode.trim().length() > 0)
						{
							valueXmlString.append("<status>").append("<![CDATA["+ mstatus +"]]>").append("</status>");
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
					//added by nandkumar gadkari on 123/09/19--------end
					valueXmlString.append("</Detail2>");
					break;
			case 3:
					
					parentNodeList = dom1.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					winName = getObjName(parentNode);
					System.out.println("winName 2nd :::"+winName);
					parentNodeList = dom.getElementsByTagName("Detail3");
					System.out.println("parent node ka size:::"+parentNodeList.getLength());
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
						   
					if(currentColumn.trim().equalsIgnoreCase("ref_no"))
					{
						
						mCode = genericUtility.getColumnValue("ref_no",dom);
						mCode1 = genericUtility.getColumnValue("ref_ser",dom);
						tranId = genericUtility.getColumnValue("tran_id",dom);
						
						sql =" select case when tot_amt is null then 0 else tot_amt end , " 
							+" case when adj_amt is null then 0 else adj_amt end " 
							+" from receivables "
							+" where  TRAN_SER = ? and ref_no = ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1, mCode1 );
						pstmt.setString( 2, mCode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							totAmt = rs.getDouble(1); 
							adjAmt = rs.getDouble(2); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						
						sql =" select case when net_amt is null then 0 else net_amt end " 
							+" from sreturn	where tran_id = ? ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,tranId );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							amount = rs.getDouble(1); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						
						if ( (totAmt - adjAmt ) > amount )
						{
							valueXmlString.append("<REF_BAL_AMT>").append("<![CDATA[" +(totAmt-adjAmt)+ "]]>").append("</REF_BAL_AMT>");
							
							sql =" select case when sum(adj_amt) is null then 0 else sum(adj_amt) end " 
								+" from sreturn_inv	where tran_id = ? " ;
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1,tranId );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								sumAdj = rs.getDouble(1); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
							valueXmlString.append("<adj_amt>").append("<![CDATA[" +(amount-sumAdj)+ "]]>").append("</adj_amt>");
						}
						else
						{
							valueXmlString.append("<REF_BAL_AMT>").append("<![CDATA[" +(totAmt-adjAmt)+ "]]>").append("</REF_BAL_AMT>");
							valueXmlString.append("<adj_amt>").append("<![CDATA[" +(totAmt-adjAmt)+ "]]>").append("</adj_amt>");
						}	
					}
					valueXmlString.append("</Detail3>");
					break;
	
			} // end of switch
			valueXmlString.append("</Root>");
			distCommon = null;	
		} // END TRY
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
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB dbEjb = null;
		
		String userId = "",objName="";
		int currentFormNo=0,childNodeListLength=0;
		java.sql.Timestamp mdate1 = null;
		String siteCode="",lsInvoiceId="";
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();

		String sqlStr="",mVal="",lsItemser="",blackListedYn="",lsStopBusiness="",itemCode1="";
		java.sql.Timestamp ldInvoiceDate = null;
		int cnt=0;
		String exchRateStr="",currCode="",mVal1="",custCode="",custCodeDlv=""; 
		double exchRate =0;
		int mlineNo=0; 
		java.sql.Timestamp mtrandate = null;
		DistCommon distCommon = null;
		distCommon = new DistCommon();	
		String mreasCode="",itemCode="",lsLotNo="",lsLotSl="",lsPricelist="",llLineNoInvtraceStr=""; 
		int mcount,llLineNoInvtrace;
		double lcRate =0,lcQtystd=0,lcRateInv=0;
		java.sql.Timestamp ldTaxdate = null;
		String saleOrder="",lineNoSord="",spaces="",siteCodeMfg="",lsSuppSour="";
		double lcInvRate=0;
		String lsTrackShelfLife="",lsFlag="",lsRetopt="",lsSer="",lsCust="",lsFullRet="";
		String lsTranId ="",lsRetOpt="";
		double ldCurrQty=0,lcInvqtyLot=0;
		int llLineNo=0,llLineNoInv=0;
		double lcSrqtyLot=0,lcSrqty=0,lcSrqtyPrev=0,mminShlife=0;
		java.sql.Timestamp mexpDate = null;
		String lsStatus="",mitemCode="";
		java.sql.Timestamp mchkDate = null;
		int llCount=0;
		double lcQty=0;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		String errorType = "";
		String tranType = "";
		String invoiceRef="",fullRet="",docKeyS="";//added by nandkumar gadkari on 06/09/19
		double minRate=0;
		String dockey = "";
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
						if( childNodeName.equalsIgnoreCase( "tran_date" ) )
						{ 
						 
							String mdateStr = genericUtility.getColumnValue( "tran_date", dom );// dw_edit.getitemdatetime(1,fldname)
							mdate1 = Timestamp.valueOf(genericUtility.getValidDateString( mdateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//getDateInAppFormat( mdateStr ); 
							siteCode = genericUtility.getColumnValue( "site_code", dom ); //dw_edit.getitemstring(1,"site_code")
							lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom ); //dw_edit.getitemstring(1,"invoice_id")
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = SysCommon.nfCheckPeriod( "SAL", mdate1, siteCode, conn ); 
							errCode=finCommon.nfCheckPeriod("SAL", mdate1, siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							if( errCode != null && errCode.trim().length() > 0 )
							{
								errList.add( "VMPRD1" );
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
						} // end of tran_date
						if( childNodeName.equalsIgnoreCase( "cust_code" ) || childNodeName.equalsIgnoreCase( "cust_code__bill" ))// cust_code__bill condition added by Nandkumar gadkari on 30/10/18  
						{
							errCode = null;
							
							mVal = genericUtility.getColumnValue( childNodeName.trim(), dom ); //dw_edit.GetItemString(dw_edit.GetRow(),fldname)           
							System.out.println( "mVal :: " + mVal + "  :childNodeName: " + childNodeName );
							
							siteCode = genericUtility.getColumnValue( "site_code", dom ); //dw_edit.getitemstring(1,"site_code")
							lsItemser = genericUtility.getColumnValue( "item_ser", dom ); //dw_edit.getItemString(1,"item_ser")			

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
							if( mVal != null && mVal.trim().length() > 0 )
							{
								sqlStr = " select Count(*) cnt from invoice where invoice_id = ? "; //:mVal;
							
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
									errCode = "VTSRET1";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									
										errCode = "VTNODETINV";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									
									
									
								}
							}
						}
						if( childNodeName.equalsIgnoreCase( "site_code" ) || childNodeName.equalsIgnoreCase( "site_code__dlv" ))
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
							System.out.println("inside item_ser:::");
							System.out.println("itemSer IC::"+mVal);
							if(mVal!=null && mVal.trim().length()>0)
							{
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
									System.out.println("inside VTITMSER1:::");
									errCode = "VTITMSER1";
									//errString = getErrorString( childNodeName, errCode, userId ); 
									//break;
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
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
						if( childNodeName.equalsIgnoreCase( "exch_rate" ) )
						{
							exchRateStr = genericUtility.getColumnValue( "exch_rate", dom );
							currCode = genericUtility.getColumnValue( "curr_code", dom );
							exchRate = Double.parseDouble(exchRateStr);
							sqlStr = " Select Count(*) cnt from currency where curr_code = ? and std_exrt  = ? ";
							pstmt = conn.prepareStatement( sqlStr );
							pstmt.setString( 1,currCode );
							pstmt.setDouble( 2,exchRate );
														
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
								errCode = "VTSRET2";
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
						if( childNodeName.equalsIgnoreCase( "reas_code" )  )//added by kunal on 24/12/12
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
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}							 
						}
						if( childNodeName.equalsIgnoreCase("cust_code__dlv")  )//added by Sagar on 05/08/14
						{ 
							custCodeDlv = getAbsString(genericUtility.getColumnValue( "cust_code__dlv", dom ));
                            System.out.println(">>>>>>cust_code__dlv validation:"+custCodeDlv); 
							cnt=0;
							if(custCodeDlv==null || custCodeDlv.trim().length()==0)
							{
								errCode = "VTCUSTDLBK";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sqlStr = "select count(*) cnt from customer where cust_code = ? ";
								pstmt = conn.prepareStatement( sqlStr );
								pstmt.setString( 1, custCodeDlv );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if( cnt == 0 )
								{
									errCode = "VTCUSTDLVD";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}						
							}
						}
					}//end for	
					break;
				case 2 :
					System.out.println( "Detail 2 Validation called " );
					
					NodeList detail3List = dom2.getElementsByTagName("Detail2");
					
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					String itemValue = "",lineNo = "",lineValue= "",updateFlag = "";
					int lineNoInt = 0,lineValueInt = 0;
					//NodeList itemNodeList = null,lineNoList = null,detail2List = null,childDetilList = null;
					
					//Node itemNode = null,lineNoNode = null,detailNode = null,chidDetailNode = null;
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						
						//D18CKOY001 added validation on invoice_trace line no
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
						}
						
						if( childNodeName.equalsIgnoreCase( "line_no__inv" ) )
						{		
							String mlineNoStr = getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) );
							mlineNo = Integer.parseInt( mlineNoStr.trim() );  
							mVal1 = genericUtility.getColumnValue( "invoice_id", dom );
							if ( mlineNo != 0 )
							{
								if( mlineNoStr != null && mlineNoStr.trim().length() > 0 && !"null".equalsIgnoreCase( mlineNoStr.trim() ) )
								{
									sqlStr = " select count(*) cnt from invdet		 " 
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
						}
						if( childNodeName.equalsIgnoreCase( "item_code" ) )
						{		
							mVal = genericUtility.getColumnValue( "item_code", dom);
							System.out.println("itemCode= Mval==========>>>"+mVal);
							//Validation for duplicate item code.If duplicate itemcode found it display error
							//Added by Priyanka on 16/07/14 as per manoj sir instruction
							
							siteCode = genericUtility.getColumnValue( "site_code", dom1 );
							String mtrandateStr = genericUtility.getColumnValue( "tran_date", dom1 );
							mtrandate = Timestamp.valueOf(genericUtility.getValidDateString( mtrandateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							custCode = genericUtility.getColumnValue( "cust_code", dom1 );
							errCode = isItem( siteCode, mVal, "SAL", conn );
							//errCode = i_nvo_gbf_func.gbf_item(msite_code,mVal,transer)
							if( errCode != null && errCode.trim().length() > 0 )
							{
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							// Commented by Mahesh Saggam on 14-June-2019 [Start]
							/*else
							{
								int length = dom2.getElementsByTagName("Detail2").getLength();
								System.out.println("length="+length);
								int cntItem=0;
								for(int i =0; i< dom2.getElementsByTagName("Detail2").getLength();i++)
								{
									System.out.println("Enter in For loop ");
									itemCode1 =  checkNull(genericUtility.getColumnValueFromNode("item_code",dom2.getElementsByTagName("Detail2").item(i))).trim();
									System.out.println("itemCode1======>"+itemCode1);
									if(mVal.equalsIgnoreCase(itemCode1))
									{					
										cntItem=cntItem+1;
										System.out.println("cntItem=="+cntItem);							       								
									}
								}							
								if(cntItem>1)
								{
									 System.out.println("item code Duplicate");
										errCode = "VTDUPITMCD";//Duplicate Item Code for Sales Return.
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());	
								}
								System.out.println("For loop over");
							}*/
							// Commented by Mahesh Saggam on 14-June-2019 [End]
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
							System.out.println("SRF TaxEnv["+mVal+"]");
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
							lsPricelist=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
							System.out.println(">>>>>>>>>>Check lsPricelist in rate & rate_studom val:"+lsPricelist);
							
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
								System.out.println(">>>>>>>>>>Check lcRateInv in rate & rate_studom val:"+lcRateInv);
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
									lcRate = getRequiredDecimal(lcRate, 3);
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
										System.out.println("7715 historyType::["+historyType+"] minRate :::["+minRate+"] lcRate:::["+lcRate+"]");
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
									llLineNoInvtraceStr = genericUtility.getColumnValue( "line_no__invtrace", dom );//line_no__inv removed and added line_no__invtrace by nandkumar gadkari on 12/03/19
									if (llLineNoInvtraceStr != null && llLineNoInvtraceStr.indexOf(".") > 0)
									{
										llLineNoInvtraceStr = llLineNoInvtraceStr.substring(0,llLineNoInvtraceStr.indexOf("."));
									}
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
										/*	+"	and SORD_NO = ? " //
											+"	and SORD_LINE_NO 	 = ? " ;*/ //commented and added + " and line_no = ? " by nandkumar gadkari on 12/03/19; 
											+ " and line_no = ? "; 
									pstmt = conn.prepareStatement( sqlStr );
									pstmt.setString( 1, lsInvoiceId );
									/*pstmt.setString( 2, saleOrder );
									pstmt.setString( 3, lineNoSord );*///commented and added + " and line_no = ? " by nandkumar gadkari on 12/03/19; 
									pstmt.setInt( 2, llLineNoInvtrace );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lcInvRate = rs.getDouble( "lc_inv_rate" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									lcInvRate = getRequiredDecimal(lcInvRate, 3);
									lcRate = getRequiredDecimal(lcRate, 3);
									System.out.println("manohar lcRateStr [" + lcRateStr + "] lcRate [" + lcRate + "] > lcInvRate [" + lcInvRate + "]");
									if ( lcRate > lcInvRate && lcInvRate > 0 )
									{
										// added by rupali on 27/04/2021 to validate rate greater than effective cost [start]
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
											System.out.println("7773 historyType::["+historyType+"] minRate :::["+minRate+"] lcRate:::["+lcRate+"]");
											if(lcRate>minRate && !("S".equalsIgnoreCase(historyType) && minRate == 0))
											{
												errCode = "VTINVRATE1";
												errList.add( errCode );
												errFields.add( childNodeName.toLowerCase() );
											}
										}
										// added by rupali on 27/04/2021 to validate rate greater than effective cost [end]
										else
										{
											errCode = "VTINVRATE1";
											//errString = getErrorString( childNodeName, errCode, userId ); 
											//break;	
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
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
							System.out.println("@@@@@@ in validation mdateStr"+mdateStr);
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
							System.out.println("@@@@@ mdateStr out : ["+mdateStr+"]");
							if( lsTrackShelfLife != null && "Y".equalsIgnoreCase( lsTrackShelfLife.trim() ) )
							{
								if( mdateStr == null || mdateStr.trim().length() == 0 )
								{
									//System.out.println("@@@@@ mdateStr in if :["+mdateStr+"]["+mdateStr.length()); // COMENTED BY NANDKUMAR GADKARI ON 30/10/18
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
						}
						if( childNodeName.equalsIgnoreCase( "quantity__stduom" )  )
						{ 		
							ldCurrQty = 0;
							itemCode  = genericUtility.getColumnValue( "item_code", dom ); //dw_detedit[ii_currformno].getitemstring(1,"item_code")
							lsInvoiceId = genericUtility.getColumnValue( "invoice_id", dom );//dw_detedit[ii_currformno].getitemstring(1,"invoice_id")
							lsFullRet  = genericUtility.getColumnValue( "full_ret", dom );//dw_detedit[ii_currformno].getitemstring(1,"full_ret")
							lsLotNo 	= genericUtility.getColumnValue( "lot_no", dom );//dw_detedit[ii_currformno].getitemstring(1,"lot_no")
							lsLotSl 	= genericUtility.getColumnValue( "lot_sl", dom );//dw_detedit[ii_currformno].getitemstring(1,"lot_sl")	
							String ldCurrQtyStr   = genericUtility.getColumnValue( "quantity__stduom", dom );//dw_detedit[ii_currformno].getitemnumber(1,fldname)	
							ldCurrQty = Double.parseDouble( ldCurrQtyStr == null || ldCurrQtyStr.trim().length() == 0 ? "0" : ldCurrQtyStr.trim() );
							lsTranId    = genericUtility.getColumnValue( "tran_id", dom );//dw_detedit[ii_currformno].getitemstring(1,"tran_id")
							String llLineNoStr = getNumString( genericUtility.getColumnValue( "line_no", dom ) );//dw_detedit[ii_currformno].getitemnumber(1,"line_no")
							llLineNo = Integer.parseInt( llLineNoStr == null || llLineNoStr.trim().length() == 0 ? "0" : llLineNoStr.trim() );
							lsRetOpt = genericUtility.getColumnValue( "ret_opt", dom1 );
							String llLineNoInvStr  = getNumString(genericUtility.getColumnValue( "line_no__inv", dom ));//dw_detedit[ii_currformno].getitemnumber(1,"line_no__inv")
							llLineNoInv = Integer.parseInt( llLineNoInvStr == null || llLineNoInvStr.trim().length() == 0 ? "0" : llLineNoInvStr.trim() );
							llLineNoInvtraceStr = getNumString(genericUtility.getColumnValue( "line_no__invtrace", dom ));// dw_detedit[ii_currformno].getitemnumber(1,"line_no__invtrace")// uncommented by nandkumar gadkari on 1-07-19
							llLineNoInvtrace = Integer.parseInt( llLineNoInvtraceStr == null || llLineNoInvtraceStr.trim().length() == 0 ? "0" : llLineNoInvtraceStr.trim() );// uncommented by nandkumar gadkari on 1-07-19
							System.out.println( " ldCurrQty :: " + ldCurrQty );
							if( ldCurrQty == 0 )
							{
								errCode = "VTISS1";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;					
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							System.out.println( " lsInvoiceId :val1: " + lsInvoiceId );
							if( lsInvoiceId != null && lsInvoiceId.trim().length() > 0 && !"null".equalsIgnoreCase( lsInvoiceId.trim() ) )
							{
								System.out.println( " llLineNoInvtrace :val1: " + llLineNoInv );
								//if ( llLineNoInv == 0 ) condition change by nandkumar gadkari on 01-07-19 
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
										+" 	and	line_no 	 = ? "; //:ll_line_no__invtrace ;// uncommented by nandkumar gadkari on 1-07-19
										
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
								String llLineNoStr = getNumString( genericUtility.getColumnValue( "line_no__inv", dom ) );//dw_detedit[ii_currformno].getitemnumber(dw_detedit[ii_currformno].GetRow(),"line_no__inv")
								if (llLineNoStr != null && llLineNoStr.indexOf(".") > 0)// if condition added by nandkumar gadkari on 05/11/18
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
										llCount = rs.getInt(1);
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
								//validation added by Nandkumar gadkari on 28/01/19-------------------------Start---------------------
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
								//validation added by Nandkumar gadkari on 28/01/19-------------------------end---------------------
							}
						}	
						if( childNodeName.equalsIgnoreCase( "quantity" ) )
						{
							if( lcQty < 0 )
							{
								errCode = "VTQTY01";
								//errString = getErrorString( childNodeName, errCode, userId ); 
								//break;													
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							//Added by Nandkumar gadkari on 02/12/2016 to split quantity in multiple line as per min_rate_history[Start]
						
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
							double sdetQtyAdj = 0.0,sdetFQtyAdj = 0.0, domTotalQty = 0.0;
							int nLineNo = Integer.parseInt(lineNo);
							
							
							String   tranId = genericUtility.getColumnValue("tran_id", dom); 
							
							//Added by Santosh on 02/05/2017 to get unconfirmed adjusted quantity [End]
							
							//Added by Santosh on 10/05/2017
							String sreturnAdjOpt = distCommon.getDisparams("999999", "SRETURN_ADJ_OPT", conn);
							//Added by Santosh on 23/05/2017
							boolean isValidInvoiceId = true;
							
							
							//Changed by Nandkumar gadkari on 10/05/2017 to not to validate split quantity if SRETURN_ADJ_OPT is not defined
							if(! "NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
							{
								double invoiceQty = 0.0, qtyAdj = 0.0;
								
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
									}*/// commented by nandkumar gadkari on 27/07/19
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
									
									//-------------NANDKUMAR GADKARI ON 15/10/18---------------START----------
									String docKeyvalue="";
									if (docKey.trim().length() > 0) {
										cnt = 0;
										/*do {
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
//									if(isValidInvoiceId && (lcQty+(qtyAdj+sdetQtyAdj+domTotalQty)) > invoiceQty)
//									{
//										return getError((invoiceQty - (qtyAdj+sdetQtyAdj+domTotalQty)), "VTSPLTQTY", conn);
//									}//commented by nandkumar gadkari on 24/03/20
									
									
									// Added  By mukesh Chauhan on 20/02/2020---- Start---- Not To allow duplicate doc key if not split
									//System.out.println("sales return fro>>>>>>>>>>>>>>>>");
									sql = "SELECT SDET.INVOICE_ID, SUM(SDET.QUANTITY) AS QTY_ADJ" // Added  By mukesh Chauhan on 20/02/2020 Start checking duplicate from salesreturnfrom
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
									 tranId = tranId == null || tranId.trim().length() == 0 ? "  " : tranId;
									 pstmt.setString(6, tranId);
									 rs = pstmt.executeQuery();
										
									 //System.out.println( "lcQty salesform :: " + lcQty );
									// System.out.println( "qtyAdj salesform:: " + qtyAdj );
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
									 //double Result=(lcQty+(qtyAdj+sdetQtyAdj+domTotalQty+sdetFQtyAdj));
									 System.out.println("lcQty :"+lcQty+" "+"qtyAdj :"+qtyAdj+" "+"sdetQtyAdj :"+sdetQtyAdj+" "+"domTotalQty :"+domTotalQty+" "+"sdetFQtyAdj :"+sdetFQtyAdj);
									 //System.out.println("Result>>>>>>>>>>>"+Result);
									 if(isValidInvoiceId && (lcQty+(qtyAdj+sdetQtyAdj+domTotalQty+sdetFQtyAdj)) > invoiceQty)
									 {
										//System.out.println("Inside Error code");
										return getError((invoiceQty - (qtyAdj+sdetQtyAdj+domTotalQty+sdetFQtyAdj)), "VTSPLTQTY", conn);
									 }
									// Added By mukesh Chauhan on 20/02/2020--------- END--------------------------------------------
									
								}
							}
							//Added by Nandkumar gadkari  on 02/12/2016 to split quantity in multiple line as per min_rate_history[End]
							
						}
						//added by nandkumar gadkari on 06/09/19----------start------------
						if( childNodeName.equalsIgnoreCase( "invoice_ref" ) )
						{
							invoiceRef = checkNullandTrim(genericUtility.getColumnValue("invoice_ref", dom));
							lsInvoiceId = checkNullandTrim(genericUtility.getColumnValue( "invoice_id", dom ));
							
							if(lsInvoiceId.trim().length() > 0)
							{
								if(!lsInvoiceId.equalsIgnoreCase(invoiceRef))
								{
									errCode = "VTINVINRE";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							
						}
						//added by nandkumar gadkari on 06/09/19----------end------------
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
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(cnt > 0)
								{
									errCode = "VTDOCKSTCL";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							
						}
						////validation added by Nandkumar gadkari on 13/01/20-------------------------end---------------------
						
					} // end for
					
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
					+ " from invoice_trace where invoice_id = ? and line_no = ?";
				lineNoTrace = lineNoTrace == null ? "0" : lineNoTrace.trim();
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
				pstmt.close();
				pstmt = null;
				rs = null;
				if (despId.trim().length() > 0 )
				{
					sql = " select loc_code, cost_rate "
						+ " from despatchdet where desp_id 	= ? and line_no = ?";

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
				
				priceList = ( infoMap.get("price_list") == null ? null : infoMap.get("price_list").toString() ); //Added by sagar on 19/08/14
				System.out.println(">>>>>>>>>>Check priceList in getCostRate:"+priceList);
				if (priceList != null && !"NULLFOUND".equals(priceList) && priceList.trim().length() > 0)
				{
					tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;
					itemCode = itemCode == null ? "" : itemCode;
					lotNo = lotNo == null ? "" : lotNo;
					costRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"L",qtyStdUom, conn);
					System.out.println(">>>>>>>>>>Check costRate in getCostRate:"+costRate);
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
					System.out.println(">>>>>>>>>>Check rate in itemValue2Quantity:"+rate);
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
	private StringBuffer getMinRate(Document dom, Document dom1, String currCol, StringBuffer sBuff, Connection conn) throws Exception
	{
		DistCommon distCommon = new DistCommon();	
		String invoiceId = null, retReplFlag = null, sql = "";
		String itemCode = null,custCode="",custCodeDlv="";
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
		   
			invoiceId = genericUtility.getColumnValue("invoice_id",dom1);
			/*if(invoiceId==null || invoiceId.trim().length()==0)
			{
				invoiceId = genericUtility.getColumnValue("invoice_id",dom);
			}*/
			System.out.println( "invoiceId :: " + invoiceId );
			if (invoiceId != null && invoiceId.trim().length() > 0 )
			{
				return sBuff;
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
			qtyStdUom = Double.parseDouble(genericUtility.getColumnValue("quantity__stduom",dom));
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
					varValue = distCommon.getDisparams("999999",schemeKey,conn);
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
					//Changed by kunal D18CKOY001
					//if ( "site_code".equalsIgnoreCase(colName.trim()) || "invoice_id".equalsIgnoreCase(colName.trim()) || "cust_code".equalsIgnoreCase(colName.trim()) )
					if ( "site_code".equalsIgnoreCase(colName.trim())  || "cust_code".equalsIgnoreCase(colName.trim()) )
					{
						docValue = 	genericUtility.getColumnValue(colName.trim(),dom1);
					}
					else
					{
						docValue = 	genericUtility.getColumnValue(colName.trim(),dom);
					}
					if (docKey != null && docKey.trim().length() > 0)
					{
						docKey = docKey + "," + ( docValue == null || docValue.trim().length() == 0 ? "" : docValue.trim() ); // 13/05/10 manoharan docValue trim() added
					}
					else
					{
						//Changed by kunal D18CKOY001
						docKey = docValue.trim();
					}
                }
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
					break;
				}
			}
			if (minRate == 0)
			{
				tranDate = tranDate == null ? ( genericUtility.getValidDateString( new Timestamp( System.currentTimeMillis() ).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) ).toString() : tranDate;
				itemCode = itemCode == null ? "" : itemCode;
				lotNo = lotNo == null ? "" : lotNo;
				
				priceList=getPriceList(dom1,dom,conn);//Getting PriceList Value, method added by sagar on 19/08/14..
				System.out.println(">>>>>>>>>>Check priceList in getMinRate:"+priceList);
				minRate = distCommon.pickRate(priceList,tranDate,itemCode,lotNo,"D",qtyStdUom, conn);
				System.out.println(">>>>>>>>>>Check minRate in getMinRate:"+minRate);
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
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		System.out.println( "return 4");
		System.out.println( "sBuff :: " + sBuff.toString() ); 
		return sBuff ;
	}
	private String getTagValue( String xmlStr, String tag ) throws Exception
	{
		Document dom = genericUtility.parseString( xmlStr + "</Detail2></Root>" );
		
		String value = genericUtility.getColumnValue( tag, dom );
		
		return value;
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
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
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
	private java.util.Date addMonths(java.util.Date date, int months) throws Exception ,ITMException //added by Kunal on 10/12/12 
	{
		java.util.Date calculatedDate = null;

		if (date != null) {
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.MONTH, months);
			calculatedDate = new java.util.Date(calendar.getTime().getTime());
		}

		return calculatedDate;
	}
	private double round(double round, int scale) throws ITMException
	{
		return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
	}
	// isItem method added by sagar on 12/08/14
	public String isItem(String siteCode, String itemCode, String modName,Connection conn) throws RemoteException,ITMException
	{
		String errCode = ""; 
		String active = ""; 
		String sql1 = "",sql2=""; 
		PreparedStatement pstmt1 = null,pstmt2=null;
		ResultSet rs1 =null,rs2=null;
		
		try
		{
			sql1 = "SELECT ACTIVE FROM ITEM WHERE ITEM_CODE = ? ";
			pstmt1= conn.prepareStatement(sql1);
			pstmt1.setString(1,itemCode);
			rs1 = pstmt1.executeQuery();
			if (rs1.next())
			{
				active = rs1.getString(1);
			}
			else
			{
				errCode = "VTITEM1"; 
			}
			if ( active == null || active.trim().length() == 0 )
			{
				active = "Y"; 
			}
			if(active.equals("N")) 
			{
				errCode ="VTITEM4"; 
			}
			else if(active.equals("D")) 
			{
				errCode ="VTITMONALR"; 
			}
			rs1.close();
			pstmt1.close();
			pstmt1 = null;
			rs1 = null;
			sql1 = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' and VAR_NAME = 'SITE_SPECIFIC_ITEM'";
			pstmt1= conn.prepareStatement(sql1);
			rs1 = pstmt1.executeQuery();
			if (rs1.next())
			{
				if (rs1.getString(1).equals("Y")) 
				{
					sql2="SELECT CASE WHEN ACTIVE IS NULL THEN 'Y' ELSE ACTIVE END FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE= ? ";
					pstmt2= conn.prepareStatement(sql2);
					pstmt2.setString(1,siteCode);
					pstmt2.setString(2,itemCode);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						if (rs2.getString(1).equals("N")) 
						{
							errCode = "VTITEM4"; 
						}
					}
					else
					{
						errCode = "VTITEM3"; 
					}
					rs2.close();
					pstmt2.close();
					pstmt2 = null;
					rs2 = null;
				}
				else
				{
					sql2 = "SELECT CASE WHEN COUNT(1) IS NULL THEN 0 ELSE COUNT(1) END FROM ITEM WHERE ITEM_CODE = ? "; 
					pstmt2= conn.prepareStatement(sql2);
					pstmt2.setString(1, itemCode);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						if (rs2.getInt(1)==0)
						{
							errCode="VTITEM1"; 
						}
					}
					rs2.close();
					pstmt2.close();
					pstmt2 = null;
					rs2 = null;
				}
			}
			rs1.close();
			pstmt1.close();
			pstmt1 = null;
			rs1 = null;
		}
		catch(Exception e)
		{
			System.out.println("Exception in isItem :==>\n"+e.getMessage()); 
			e.printStackTrace();
			errCode = e.getMessage();
			throw new ITMException(e);
		}
		return errCode;
	}
	private String getPriceList(Document dom1, Document dom, Connection conn) throws ITMException 
	{
		PreparedStatement pstmt2 = null, pstmt1 = null;
		ResultSet rs2 = null, rs1 = null ;
		String sql2="";
		Timestamp trDate = null;
		String custCode="",custCodeDlv="",tranDate="",siteCode="",itemCode="",priceList="",contractNo="";
		
		try
		{
			System.out.println(">>>>>>>>>>>>>Calling Dom getPriceList ");
			custCode = genericUtility.getColumnValue("cust_code",dom1);
			custCodeDlv = genericUtility.getColumnValue("cust_code__dlv",dom1);
			tranDate = genericUtility.getColumnValue("tran_date", dom1);
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			itemCode = genericUtility.getColumnValue("item_code",dom);
			contractNo = genericUtility.getColumnValue("contract_no",dom1);//added by priyanka
			priceList = genericUtility.getColumnValue("price_list",dom1);//added by NANDKUMAR	GADKARI ON 11/06/2020
			System.out.println(">>>>>>>>>>>custCode:"+custCode);
			System.out.println(">>>>>>>>>>>custCodeDlv:"+custCodeDlv);
			System.out.println(">>>>>>>>>>>tranDate:"+tranDate);
			System.out.println(">>>>>>>>>>>siteCode:"+siteCode);
			System.out.println(">>>>>>>>>>>itemCode:"+itemCode);
			System.out.println(">>>>>>>>>>>contractNo:"+contractNo);
			trDate= Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println(">>>>>>>>>Tran Date:"+trDate);
			
			if (priceList != null && priceList.trim().length() > 0)//added by NANDKUMAR	GADKARI ON 11/06/2020
			{
			//condition added by priyanka on 17/06/15
			//if contract is entered and price list is defined against entered contract no then set pricelist based on contract no,else follow existing hierachy
			if (contractNo != null && contractNo.trim().length() > 0)
			{
				System.out.println("Contract no is not null************");
				
				sql2="select price_list from scontract where contract_no=?";
			
				pstmt1 = conn.prepareStatement(sql2);
				pstmt1.setString(1, contractNo);						
				
				
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					priceList = rs1.getString("price_list");										
				}
				
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("priceList from contract no====="+priceList);
			}
			else
			{
			
			System.out.println("Contract no is null****************");
			
			
			
			sql2 = " select  g.price_list from group_pricelist g,pricelist p where  " +
					" g.cust_code = ? " +
					" and p.item_code  = ? " +
					" and (? between to_date(g.eff_from) and to_date(g.valid_upto)) ";

			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setString(1,custCode);
			pstmt2.setString(2,itemCode);
			pstmt2.setTimestamp(3,trDate);
			rs2 = pstmt2.executeQuery();
			if(rs2.next())
			{
				priceList = rs2.getString("price_list");
			}
			
			pstmt2.close();
			pstmt2 = null;					
			rs2.close();
			rs2 = null;
			if(priceList == null || priceList.trim().length() == 0)
			{
				//sql2 = "select price_list from site_customer where site_code = ? and cust_code=?";
				sql2 = "select price_list from site_customer where site_code = ? and cust_code=?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1,siteCode);
				pstmt2.setString(2,custCode);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					priceList = rs2.getString(1);
					if(priceList==null)
					{
						priceList="";
					}
				}
				pstmt2.close();
				pstmt2 = null;					
				rs2.close();
				rs2 = null;
				if(priceList == null || priceList.trim().length() == 0)
				{
					sql2 = "select price_list from customer where cust_code = ?";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1,custCode);
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						priceList = rs2.getString(1);
					}
					pstmt2.close();
					pstmt2 = null;					
					rs2.close();
					rs2 = null;
					if(priceList == null || priceList.trim().length() == 0)
					{
						System.out.println(">>>>>>>>>>>in Getting priceList Null found for Customer Code:"+priceList);
						sql2 = " select  g.price_list from group_pricelist g,pricelist p where  " +
								" g.cust_code = ? " +
								" and p.item_code  = ? " +
								" and (? between to_date(g.eff_from) and to_date(g.valid_upto)) ";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1,custCodeDlv);
						pstmt2.setString(2,itemCode);
						pstmt2.setTimestamp(3,trDate);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							priceList = rs2.getString("price_list");
						}
						pstmt2.close();
						pstmt2 = null;					
						rs2.close();
						rs2 = null;
						if(priceList == null || priceList.trim().length() == 0)
						{
							sql2 = "select price_list from site_customer where site_code = ? and cust_code=?";
							pstmt2 = conn.prepareStatement(sql2);
							pstmt2.setString(1,siteCode);
							pstmt2.setString(2,custCodeDlv);
							rs2 = pstmt2.executeQuery();
							if(rs2.next())
							{
								priceList = rs2.getString(1);
							}
							pstmt2.close();
							pstmt2 = null;					
							rs2.close();
							rs2 = null;
							if(priceList == null || priceList.trim().length() == 0)
							{
								sql2 = "select price_list from customer where cust_code = ?";
								pstmt2 = conn.prepareStatement(sql2);
								pstmt2.setString(1,custCodeDlv);
								rs2 = pstmt2.executeQuery();
								if(rs2.next())
								{
									priceList = rs2.getString(1);
								}
								pstmt2.close();
								pstmt2 = null;					
								rs2.close();
								rs2 = null;
							}
						 }
					} 
				}
			}
		}//added by priyanaka
		}
			if(priceList == null || priceList.trim().length() == 0)
			{
				priceList="";
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in getPriceList:==>\n"+e.getMessage()); 
			e.printStackTrace();
			throw new ITMException( e );
		}
		System.out.println(">>>>return priceList:"+priceList);
	    return priceList;
	}
	
	
	private String getPriceLstVal(String custCd, String custCdDlv, String siteCd, String contrNo, Connection conn) throws ITMException
	{

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String priceLst = "", sql = "";

		try
		{
			System.out.println(">>>>>>>>>Enter in getPriceLstVal method");
			System.out.println(">>>>>>>>custCd===" + custCd);
			System.out.println(">>>>>>>>custCdDlv===" + custCdDlv);
			System.out.println(">>>>>>>>siteCd===" + siteCd);
			System.out.println(">>>>>>>>contractNo===" + contrNo);

			if (contrNo != null && contrNo.trim().length() > 0)
			{
				sql = "select price_list from scontract where contract_no=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, contrNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					priceLst = checkNull(rs.getString("price_list"));
					System.out.println("Price list from contract no===========" + priceLst);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (priceLst == null || priceLst.trim().length() == 0)
				{
					sql = "select price_list from site_customer where site_code = ? and cust_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCd);
					pstmt.setString(2, custCd);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceLst = checkNull(rs.getString("price_list"));
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if (priceLst == null || priceLst.trim().length() == 0)
					{
						sql = "select price_list from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCd);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							priceLst = checkNull(rs.getString("price_list"));
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						if (priceLst == null || priceLst.trim().length() == 0)
						{
							sql = "select price_list from site_customer where site_code = ? and cust_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCd);
							pstmt.setString(2, custCdDlv);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								priceLst = checkNull(rs.getString("price_list"));
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							if (priceLst == null || priceLst.trim().length() == 0)
							{
								sql = "select price_list from customer where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCdDlv);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									priceLst = checkNull(rs.getString("price_list"));
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}
						}
					}
				}
			} 
			else
			{
				sql = "select price_list from site_customer where site_code = ? and cust_code=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCd);
				pstmt.setString(2, custCd);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					priceLst = checkNull(rs.getString("price_list"));
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;

				if (priceLst == null || priceLst.trim().length() == 0)
				{
					sql = "select price_list from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCd);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceLst = checkNull(rs.getString("price_list"));
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if (priceLst == null || priceLst.trim().length() == 0)
					{
						sql = "select price_list from site_customer where site_code = ? and cust_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCd);
						pstmt.setString(2, custCdDlv);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							priceLst = checkNull(rs.getString("price_list"));
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						System.out.println("Get  Customer==="+custCdDlv);
						System.out.println("Get  priceLst lenght==="+priceLst.trim().length());
						System.out.println("Get  priceLst==="+priceLst);
						if (priceLst == null || priceLst.trim().length() == 0)
						{
							sql = "select price_list from customer where cust_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCdDlv);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								priceLst = checkNull(rs.getString("price_list"));
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						}
					}
				}
			}

			System.out.println("Final Price List =======" + priceLst);

		} catch (Exception e)
		{
			System.out.println("Exception ::" + e);
			throw new ITMException(e);
		}

		return priceLst;

	}
	//added by Nandkumar Gadkari on 23/10/18-------------------Start--------------------------
	private String getAvailableInvId(Document dom,Document allDom, HashMap<String, String> curFormItemLotHMap, HashMap<String, String> curRecordItemLotHMap, String invoiceId, String domDocKey,  double dQtyAdj) // AdjQty change string to double by nandkumar gadkari on 26/07/19
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
			/*dQtyAdj = Double.parseDouble(adjQty);
			dCurRecInvQty = Double.parseDouble(curRecInvQuantity);*/
			//dQtyAdj = adjQty == null || adjQty.trim().length()== 0 ? 0 :Double.parseDouble(adjQty);commented by nandkumar  on 26/07/19
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
						docValue = checkNull(genericUtility.getColumnValue("cust_code__bill", headerDom)).trim();
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
					&& !"D".equalsIgnoreCase(updateFlag))//added by nandkumar gadkari on 24-03-20
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
	private String checkNullTrim(String input)	
	{
		System.out.println("input Date : " +input);
		if (input == null)
		{
			input="";
		}
		return input;
	}
	//added by Nandkumar Gadkari on 23/10/18-------------------end--------------------------
	
	//Pavan Rane 22Jul19[to set mfg_date from item_lot_info as per same logic from salesReturn]
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
	//Pavan Rane 22Jul19 end
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
	private double checkDoubleNull(String input)	
	{
		double var=0.0;
		if (input != null && input.trim().length() > 0)
		{
			var =Double.parseDouble(input);
		}
		return var;
	}
	
}