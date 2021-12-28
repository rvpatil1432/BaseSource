package ibase.webitm.ejb.dis.adv;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;

import ibase.system.config.*;
import ibase.utility.CommonConstants;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import org.w3c.dom.*;

import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;

import java.text.SimpleDateFormat;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ChargeBackLocAct extends ActionHandlerEJB implements DistIssueActLocal, DistIssueActRemote
{
	CommonConstants commonConstants = new CommonConstants();
	DistCommon distCommon= new DistCommon();
	boolean isDistOrderValuedSet = false;

	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}
	//public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null,dom2=null;
		String  retString = null;
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString); 			
			}
			/*if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = genericUtility.parseString(xmlString1);			
			}*/
			if (actionType.equalsIgnoreCase("Default"))
			{
				retString = actionDefault(xmlString,objContext,xtraParams);
				//retString = actionDefault(xmlString,xmlString1,objContext,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :ChargeBackLocAct :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from ChargeBackLocAct : actionHandler"+retString);
		return retString;
	}
	//private String actionDefault(String xmlString,String xmlString1, String objContext,String xtraParams) throws RemoteException, ITMException
	private String actionDefault(String xmlString, String objContext,String xtraParams) throws RemoteException, ITMException
	{
		//System.out.println("xmlString from ChargeBackLocAct :"+xmlString+" \n xmlString1 :"+xmlString1);
		System.out.println("xmlString from ChargeBackLocAct :"+xmlString);
		String sql = "",sql1="",sql2="",sql3="", distOrder = "", locCode = "", itemChngXmlString = "", returnValue = "", childNodeName = "", childNodeName1 = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root>");
		String finalStr = "";

		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		PreparedStatement pstmt4 = null;
		Document dom = null; //dom1 = null,domItmChng = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		String  toDate = "",frDate="",sorderNo="",contractNo="";
		String siteCode = null,siteCodeSs=null;
		String custCode = null;
		double quantity=0,rateSell=0,endCustRate=0;
		System.out.println("Action Default Entry time :: " );
		String retString = "",itemDescr="",itemSer="",taxChap="",taxEnv="",taxClass="",mstanCode="",siteCodeCr="",mstanCodeSite="";
		double amount = 0.0,netAmt=0.0,discount=0.0,taxAmt=0.0;
		String lineNoS="", itemCodeS="",invoiceId="",lotNoS="",endCustCode="",invoiceIdSell="",invoiceIdEndRate="";
		double rateS=0,rateS1=0,quantityS=0;
		double rateDiff1=0, rate=0.0,discAmt=0.0,discPerUnit=0.0,saleQty=0,saleRetQty=0,confQty=0,minEndRate=0,minRateSell=0;
		boolean flag = false;
		boolean min = false;
		try
		{
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString); 			
			}
			else 
			{
				System.out.println("The xmlString found null");
			}
/*			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = genericUtility.parseString(xmlString1);			
			}
			else
			{
				System.out.println("The xmlString1 found null");
			}*/
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			stmt = conn.createStatement();
			siteCode = genericUtility.getColumnValue("site_code",dom);
			siteCodeCr = genericUtility.getColumnValue("site_code__cr",dom);
			custCode = genericUtility.getColumnValue("cust_code",dom);
			itemSer = genericUtility.getColumnValue("item_ser",dom);
			//siteCodeSs = genericUtility.getColumnValue("site_code__cr",dom1);
			String tranDate = genericUtility.getColumnValue("tran_date",dom);

			String itemCode = "",lotNo="";

			int lineCntr=0;
			//Timestamp todate = null;
		//	Timestamp frdate = null;
			//java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );
			Timestamp frmDate = null,frdate1=null,todate1=null;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			tranDate = genericUtility.getColumnValue("tran_date",dom);
			System.out.println("tranDate:"+tranDate);
			Calendar preCalc = Calendar.getInstance();	
			System.out.println("preCalc::"+preCalc);
			preCalc.setTime( getDateObject( tranDate ) );
			preCalc.add( Calendar.MONTH , -1);
			java.util.Date prvDate = preCalc.getTime();	
			System.out.println("prvDate:"+prvDate);
			String trandate = simpleDateFormat.format( prvDate );
			String trandate1 = genericUtility.getValidDateString(trandate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
			frmDate = java.sql.Timestamp.valueOf(trandate1 + " 00:00:00.00");
			System.out.println("trandate:"+trandate);
			sql="select site_code from customer where cust_code=?";
			System.out.println("sqlCnt : [" +sql+ "]");
			pstmt3 = conn.prepareStatement(sql);
			pstmt3.setString(1,custCode);
			rs3 = pstmt3.executeQuery();
			if ( rs3.next())
			{
				siteCodeSs = rs3.getString("site_code"); 
			}
			pstmt3.close();
			pstmt3 = null;
			rs3.close();
			rs3 = null;
			
			
			System.out.println("Site_Code SS::::"+siteCodeSs);
			
			sql = "SELECT FR_DATE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE " ;
			System.out.println("sqlCnt : [" +sql+ "]");
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,frmDate);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				frdate1 = rs.getTimestamp("FR_DATE"); 
				System.out.println("FR_DATE::::"+frdate1);
				sql1 = "SELECT TO_DATE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE " ;
				System.out.println("sqlCnt : [" +sql1+ "]");
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setTimestamp(1,frdate1);
				rs1 = pstmt1.executeQuery();
				if ( rs1.next())
				{
					todate1 = rs1.getTimestamp("TO_DATE"); 
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
				System.out.println("TO_DATE::::"+todate1);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			
			sql2 = "select b.item_code,b.lot_no,b.quantity,b.rate,b.discount,a.tax_amt, a.cust_code,a.invoice_id,b.sord_no  from invoice a, invoice_trace b  where a.invoice_id = b.invoice_id " +
					"and a.site_code = ? and a.tran_date >= ?  and a.tran_date <= ?  and a.confirmed = 'Y'";
			pstmt2 = conn.prepareStatement(sql2);
			//pstmt2.setString(1,custCode);
			pstmt2.setString(1,siteCodeSs);
			pstmt2.setTimestamp(2,frdate1);
			pstmt2.setTimestamp(3,todate1);
			rs2 = pstmt2.executeQuery();
			while (rs2.next())
			{
				lineCntr++;
				System.out.println("lineCntr["+lineCntr+"]");
				itemCode=rs2.getString(1);
				lotNo=rs2.getString(2);
				quantity=rs2.getDouble(3);
				endCustRate=rs2.getDouble(4);
				discount=rs2.getDouble(5);
				taxAmt=rs2.getDouble(6);
				endCustCode=rs2.getString(7);
				invoiceIdEndRate =rs2.getString(8);
				sorderNo=rs2.getString(9);
				
				System.out.println("discount>>>>>>>>["+discount+"] taxAmt>>>>>>>>>>["+taxAmt+"] sorderNo["+sorderNo+"]");
				System.out.println("itemCode>>>>>>>>>>>>>>>>  [" + itemCode + "] lotNo>>>>>>>>>>> [" + lotNo +"] quantity["+quantity + "] endCustRate["+endCustRate+"]");
				sql3 = "	select stan_code from customer where cust_code = ?";	
				pstmt3 = conn.prepareStatement(sql3);
				pstmt3.setString(1, custCode);
				rs3 = pstmt3.executeQuery();
				if(rs3.next())
				{
					mstanCode = checkNull(rs3.getString("stan_code"));
				}
				rs3.close();
				rs3 = null;
				pstmt3.close();
				pstmt3 = null;
				
				System.out.println("mstanCode["+mstanCode+"]");
				sql3 = "	select stan_code  from site where site_code = ?";	
				pstmt3 = conn.prepareStatement(sql3);
				pstmt3.setString(1, siteCodeCr);
				rs3 = pstmt3.executeQuery();
				if(rs3.next())
				{
					mstanCodeSite = checkNull(rs3.getString("stan_code"));
				}
				rs3.close();
				rs3 = null;
				pstmt3.close();
				pstmt3 = null;
				System.out.println("mstanCodeSite["+mstanCodeSite+"]");
				taxClass = distCommon.getTaxClass("C", custCode, itemCode, siteCodeCr,conn);
				System.out.println("taxClass["+taxClass+"]");
				taxChap = distCommon.getTaxChap(itemCode, itemSer, "C", custCode, siteCodeCr,conn);
				System.out.println("taxChap["+taxChap+"]");
				taxEnv = distCommon.getTaxEnv(mstanCodeSite, mstanCode, taxChap, taxClass, siteCodeCr ,conn);
				System.out.println("taxEnv["+taxEnv+"]");

				sql3 = "select b.rate,a.invoice_id from invoice a, invoice_trace b  where a.invoice_id = b.invoice_id " +
				"and a.site_code = ? and a.tran_date >= ?  and a.tran_date <= ?  and a.confirmed = 'Y'";
				pstmt3 = conn.prepareStatement(sql3);
				//pstmt3.setString(1,custCode);
				pstmt3.setString(1,siteCode);
				pstmt3.setTimestamp(2,frdate1);
				pstmt3.setTimestamp(3,todate1);
				rs3 = pstmt3.executeQuery();
				if (rs3.next())
				{
					rateSell =rs3.getDouble(1);
					invoiceIdSell =rs3.getString(2);
				}
				pstmt3.close();
				pstmt3 = null;
				rs3.close();
				rs3 = null;
				
				System.out.println("rateSell["+rateSell+"]");
				sql1 = "select DESCR, item_ser from item " + "where item_code = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCode.trim());
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					itemDescr = rs1.getString("DESCR");
					itemSer = rs1.getString("item_ser");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				sql = "select sum(b.quantity) as saleQty from invoice a, invoice_trace b where a.invoice_id = b.invoice_id " +
						" and	a.cust_code = ?  and a.site_code = ? and a.tran_date >= ?  and a.tran_date <= ? and	b.item_code = ? and	b.lot_no = ?  and	a.confirmed = 'Y'";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode);
				pstmt1.setString(2, siteCodeSs);
				pstmt1.setTimestamp(3,frdate1);
				pstmt1.setTimestamp(4,todate1);
				pstmt1.setString(5, itemCode);
				pstmt1.setString(6, lotNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					saleQty = rs1.getDouble("saleQty");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				sql = "select sum(case when B.ret_rep_flag = 'R' then b.quantity else -b.quantity end) as saleRetQty  from sreturn a, sreturndet b " +
						" where a.tran_id = b.tran_id  and	a.cust_code = ?  and	" +
						"a.site_code = ? and a.tran_date >= ?  and a.tran_date <= ?  and	b.item_code = ? " + " and   b.lot_no 	= ? " + " and	a.confirmed = 'Y'";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode);
				pstmt1.setString(2, siteCodeSs);
				pstmt1.setTimestamp(3,frdate1);
				pstmt1.setTimestamp(4,todate1);
				pstmt1.setString(5, itemCode);
				pstmt1.setString(6, lotNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					saleRetQty = rs1.getDouble("saleRetQty");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				sql = "select sum(case when b.quantity is null then 0 else b.quantity end) as confQty " + " from charge_back a, charge_back_det b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.confirmed = 'Y' " + " and	b.item_code = ? " + " and   b.lot_no 	= ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode);
				pstmt1.setString(2, siteCode);
				pstmt1.setString(3, itemCode);
				pstmt1.setString(4, lotNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					confQty = rs1.getDouble("confQty");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(siteCode != null && siteCode.trim().length() >0 )
				{
				minRateSell = getMinRate(dom,"lot_no",itemCode,lotNo,quantity ,custCode,siteCode,conn);
				min =  true;
				System.out.println("Min true condition@@@@["+min+"]");
				System.out.println("minRateSell["+minRateSell+"]");
				}
				/*if(siteCodeSs != null && siteCodeSs.trim().length() > 0)
				{
					minEndRate =  getMinRate(dom,"lot_no",min,itemCode,lotNo,quantity,endCustCode, invoiceIdEndRate,conn);
					min =  false;
					System.out.println("Min false condition@@@@["+min+"]");
					System.out.println("minEndRate["+minEndRate+"]");
				}*/
				   rateDiff1 =minRateSell-endCustRate;
				System.out.println("rateDiff1["+rateDiff1+"]= minRateSell["+minRateSell+"] - endCustRate["+endCustRate+"]");
				sql="select contract_no from sorder where sale_order= ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, sorderNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					contractNo = rs1.getString(1);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("ContractNo["+contractNo+"]");
				if(contractNo != null && contractNo.trim().length() > 0)
				{
					System.out.println("Contract Number !!!!!!!!!!!!!!!!!!!!!!");
				if(rateDiff1 > 0 || rateDiff1 < 0)
				{
					flag = true;
				}
				else
				{
					flag = false;
				}
				if(flag == true)
				{	
				System.out.println("Flag true");

				if (endCustRate <= 0)
				{
					 rate = minRateSell;
					System.out.println("if <0"+rate);
				} else
				{
					rate = Math.min(endCustRate, minRateSell);
					//rateDiff = rateSellDbl - endCustRateDbl;
					System.out.println("Else"+rate);
				}
					discAmt=(quantity * rate * discount) / 100;
					System.out.println("discAmt["+discAmt+"] = quantity["+quantity+"] * rate ["+rate+"] * discount "+ discount);
					amount = rateDiff1 * quantity;
					
					System.out.println("amount["+amount+"] = rateDiff1["+rateDiff1+"] * quantity ["+quantity+"]");
					System.out.println("netAmt["+netAmt+"]");
					netAmt = amount + discAmt + taxAmt;
					System.out.println("netAmt["+netAmt+"] = amount["+amount+"] +  discAmt ["+discAmt+"] + taxAmt[ "+taxAmt+"]");
				discPerUnit = (rate * discount) / 100;
				System.out.println("discPerUnit["+discPerUnit+"] = (rate["+rate+"] * discount ["+discount+"]) / 100;");
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<line_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lineCntr).append("]]>").append("</line_no>\r\n");
				valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA["+ itemCode + "]]>").append("</item_code>\r\n");
				valueXmlString.append("<item_descr>").append("<![CDATA[" + (itemDescr) + "]]>").append("</item_descr>");
				valueXmlString.append("<item_ser>").append("<![CDATA[" + (itemSer) + "]]>").append("</item_ser>");
				valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA["+ lotNo + "]]>").append("</lot_no>\r\n");
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA["+ quantity + "]]>").append("</quantity>\r\n");
				valueXmlString.append("<rate__sell isSrvCallOnChg='0'>").append("<![CDATA["+ minRateSell + "]]>").append("</rate__sell>\r\n");
				//System.out.println("endCustRate::::["+endCustRate+"]");
				valueXmlString.append("<rate__contr isSrvCallOnChg='0'>").append("<![CDATA["+ endCustRate + "]]>").append("</rate__contr>\r\n");
				valueXmlString.append("<discount_amt>").append("<![CDATA[" + getRequiredDecimal(discAmt, 3) + "]]>").append("</discount_amt>");
				valueXmlString.append("<amount>").append("<![CDATA[" + getRequiredDecimal(amount, 3) + "]]>").append("</amount>");
				valueXmlString.append("<net_amt>").append("<![CDATA[" + getRequiredDecimal(netAmt, 3) + "]]>").append("</net_amt>");
				valueXmlString.append("<discount_per_unit>").append("<![CDATA[" + getRequiredDecimal(discPerUnit, 3) + "]]>").append("</discount_per_unit>");
				valueXmlString.append("<rate__diff>").append("<![CDATA[" + getRequiredDecimal(rateDiff1, 3) + "]]>").append("</rate__diff>");
				valueXmlString.append("<sale_qty>").append("<![CDATA[" + saleQty + "]]>").append("</sale_qty>");
				valueXmlString.append("<sale_ret_qty>").append("<![CDATA[" + saleRetQty + "]]>").append("</sale_ret_qty>");
				valueXmlString.append("<conf_claimed>").append("<![CDATA[" + getRequiredDecimal(confQty, 3) + "]]>").append("</conf_claimed>");
				valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
				valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
				valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
				valueXmlString.append("</Detail>\r\n");
				}
				else
				{
					System.out.println("Flag False@@@@@@@@");
					/*valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no>").append("").append("</line_no>\r\n");
					valueXmlString.append("<item_code >").append("").append("</item_code>\r\n");
					valueXmlString.append("<item_descr>").append("").append("</item_descr>");
					valueXmlString.append("<item_ser>").append("").append("</item_ser>");
					valueXmlString.append("<lot_no >").append("").append("</lot_no>\r\n");
					valueXmlString.append("<quantity>").append("0").append("</quantity>\r\n");
					valueXmlString.append("<rate__sell>").append("").append("</rate__sell>\r\n");
					System.out.println("endCustRate else::::["+endCustRate+"]");
					valueXmlString.append("<rate__contr>").append("<![CDATA[]]>").append("</rate__contr>\r\n");*/
				}
/*				if (rate1 <= 0)
				{
					rateOrg = rate;
					System.out.println("if <0");
				} else
				{
					rateOrg = Math.min(rate1, rate);
					rateDiff = rate - rate1;
					System.out.println("Else");
				}
				System.out.println("quantityDbl"+quantity);
				System.out.println("rateSellDbl"+rate);
				System.out.println("endCustRateDbl"+rate1);
				System.out.println("rateOrg"+rateOrg);
				
				discAmt = (quantity * rateOrg * discPerDbl) / 100;
				double amount = rateDiff * quantity;
				netAmt = amount + discAmt + taxAmtDbl;
				discPerUnit = (rate * discPerDbl) / 100;
				valueXmlString.append("<discount_amt>").append("<![CDATA[" + getRequiredDecimal(discAmt, 3) + "]]>").append("</discount_amt>");
				valueXmlString.append("<amount>").append("<![CDATA[" + getRequiredDecimal(amount, 3) + "]]>").append("</amount>");
				valueXmlString.append("<net_amt>").append("<![CDATA[" + getRequiredDecimal(netAmt, 3) + "]]>").append("</net_amt>");
				valueXmlString.append("<discount_per_unit>").append("<![CDATA[" + getRequiredDecimal(discPerUnit, 3) + "]]>").append("</discount_per_unit>");
				valueXmlString.append("<rate__diff>").append("<![CDATA[" + getRequiredDecimal(rateDiff, 3) + "]]>").append("</rate__diff>");*/
				
				rateDiff1=0;
				endCustRate=0;
				rateSell=0;
				quantity=0;
				discount=0;
				discAmt=0;
				discPerUnit=0;
				netAmt=0;
				amount=0;
				minRateSell=0;
				rate=0;
				confQty=0;
				}
				else
				{
					System.out.println("Else Contract NO");
					continue;
				}
			}
			pstmt2.close();
			pstmt2 = null;
			rs2.close();
			rs2 = null;
			contractNo="";
			boolean check=false,flag1= false;
			String docKey="",itemDescrS="",itemSerS="",invoiceIdMin="";
			String contractNoMin="",sorderNoMinRate="";
			int cnt =0,lineCntr1=0,cnt1=0;
			double minRateSellSales=0,EndrateSales=0,diff1=0,discountS=0,netAmtS=0,amountS=0,taxAmtS=0,discPerUnitS=0,saleQtyS=0;
			double saleRetQtyS=0,confQtyS=0,discAmtS=0;
			sql2="select d.line_no, d.item_code,d.invoice_id,d.lot_no , d.rate ,d.quantity ,d.doc_key,d.discount from sreturndet d,sreturn e  where  d.tran_id =e.tran_id and e.site_code= ? " +
					"and e.tran_date >= ?  and e.tran_date <= ? and d.ret_rep_flag = 'R' and e.confirmed='Y' order by line_no desc";
			pstmt4 = conn.prepareStatement(sql2);
			pstmt4.setString(1,siteCodeSs);
			pstmt4.setTimestamp(2,frdate1);
			pstmt4.setTimestamp(3,todate1);
			rs4 = pstmt4.executeQuery();
			while(rs4.next())
			{
				lineCntr1++;
				System.out.println("lineCntr1["+lineCntr1+"]");
				if(lineCntr> 0)
				{
					System.out.println("lineCntr["+lineCntr+"]");
					lineCntr1=lineCntr+lineCntr1;
					
				}
				else
				{
					lineCntr1=lineCntr1;
				}
				System.out.println("lineCntr1["+lineCntr1+"");
				lineNoS=rs4.getString(1);
				itemCodeS=rs4.getString(2);
				invoiceId=rs4.getString(3);
				lotNoS=rs4.getString(4);
				EndrateSales=rs4.getDouble(5);
				quantityS=rs4.getDouble(6);
				docKey=rs4.getString(7);
				discountS=rs4.getDouble(8);
				
				sql3 = "	select stan_code from customer where cust_code = ?";	
				pstmt3 = conn.prepareStatement(sql3);
				pstmt3.setString(1, custCode);
				rs3 = pstmt3.executeQuery();
				if(rs3.next())
				{
					mstanCode = checkNull(rs3.getString("stan_code"));
				}
				rs3.close();
				rs3 = null;
				pstmt3.close();
				pstmt3 = null;
				
				System.out.println("mstanCode["+mstanCode+"]");
				sql3 = "	select stan_code  from site where site_code = ?";	
				pstmt3 = conn.prepareStatement(sql3);
				pstmt3.setString(1, siteCodeCr);
				rs3 = pstmt3.executeQuery();
				if(rs3.next())
				{
					mstanCodeSite = checkNull(rs3.getString("stan_code"));
				}
				rs3.close();
				rs3 = null;
				pstmt3.close();
				pstmt3 = null;
				System.out.println("mstanCodeSite["+mstanCodeSite+"]");
				taxClass = distCommon.getTaxClass("C", custCode, itemCodeS, siteCodeCr,conn);
				System.out.println("taxClass["+taxClass+"]");
				taxChap = distCommon.getTaxChap(itemCode, itemSer, "C", custCode, siteCodeCr,conn);
				System.out.println("taxChap["+taxChap+"]");
				taxEnv = distCommon.getTaxEnv(mstanCodeSite, mstanCode, taxChap, taxClass, siteCodeCr ,conn);
				System.out.println("taxEnv["+taxEnv+"]");
				
				sql1 = "select DESCR, item_ser from item " + "where item_code = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCodeS.trim());
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					itemDescrS = rs1.getString("DESCR");
					itemSerS = rs1.getString("item_ser");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				if(invoiceId != null &&  invoiceId.trim().length() >0)
				{
				sql="select sord_no from invoice_trace where invoice_id= ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, invoiceId);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					sorderNo = rs1.getString(1);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("sorderNo["+sorderNo+"]");
				sql="select contract_no from sorder where sale_order= ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, sorderNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					contractNo = rs1.getString(1);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("ContractNo["+contractNo+"]");
				if(contractNo != null && contractNo.trim().length() > 0)
				{
					check = true;
					System.out.println("Check boolean value is "+check);
				}
			}//end invoice id if
				else
				{
					sql="select count(1) from min_rate_history where doc_key= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, docKey);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("cnt value is :["+cnt+"]");
					if(cnt ==1)
					{
						//check= true;
						sql="select invoice_id from min_rate_history where doc_key= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, docKey);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							invoiceIdMin = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(invoiceIdMin != null && invoiceIdMin.trim().length() > 0)
						{
						sql="select sord_no from invoice_trace where invoice_id= ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, invoiceIdMin);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							sorderNoMinRate = rs1.getString(1);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						System.out.println("ContractNo["+contractNo+"]");
						sql="select contract_no from sorder where sale_order= ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, sorderNoMinRate);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							contractNoMin = rs1.getString(1);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						System.out.println("contractNoMin["+contractNoMin+"]");
						if(contractNoMin != null && contractNoMin.trim().length() > 0)
						{
							check = true;
							System.out.println("Check boolean value of  contractNoMin "+check);
						}
						}
						else
						{
							sql="select count(1) from min_rate_history where doc_key= ? and  reas_code IS NOT NULL ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, docKey);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt1 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("cnt1 value is :["+cnt1+"]");
							if(cnt1 ==1)
							{
								check =true;
								System.out.println("reascode cnt1 check value is["+check+"]");
							}
							
						}
						
						
					}
				}
				sql = "select sum(b.quantity) as saleQty from invoice a, invoice_trace b where a.invoice_id = b.invoice_id " +
				" and	a.cust_code = ?  and a.site_code = ? and a.tran_date >= ?  and a.tran_date <= ? and	b.item_code = ? and	b.lot_no = ?  and	a.confirmed = 'Y'";
		pstmt1 = conn.prepareStatement(sql);
		pstmt1.setString(1, custCode);
		pstmt1.setString(2, siteCodeSs);
		pstmt1.setTimestamp(3,frdate1);
		pstmt1.setTimestamp(4,todate1);
		pstmt1.setString(5, itemCode);
		pstmt1.setString(6, lotNo);
		rs1 = pstmt1.executeQuery();
		if (rs1.next())
		{
			saleQtyS = rs1.getDouble("saleQty");
		}
		rs1.close();
		rs1 = null;
		pstmt1.close();
		pstmt1 = null;
		sql = "select sum(case when B.ret_rep_flag = 'R' then b.quantity else -b.quantity end) as saleRetQty  from sreturn a, sreturndet b " +
				" where a.tran_id = b.tran_id  and	a.cust_code = ?  and	" +
				"a.site_code = ? and a.tran_date >= ?  and a.tran_date <= ?  and	b.item_code = ? " + " and   b.lot_no 	= ? " + " and	a.confirmed = 'Y'";
		pstmt1 = conn.prepareStatement(sql);
		pstmt1.setString(1, custCode);
		pstmt1.setString(2, siteCodeSs);
		pstmt1.setTimestamp(3,frdate1);
		pstmt1.setTimestamp(4,todate1);
		pstmt1.setString(5, itemCode);
		pstmt1.setString(6, lotNo);
		rs1 = pstmt1.executeQuery();
		if (rs1.next())
		{
			saleRetQtyS = rs1.getDouble("saleRetQty");
		}
		rs1.close();
		rs1 = null;
		pstmt1.close();
		pstmt1 = null;
		sql = "select sum(case when b.quantity is null then 0 else b.quantity end) as confQty " + " from charge_back a, charge_back_det b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.confirmed = 'Y' " + " and	b.item_code = ? " + " and   b.lot_no 	= ? ";
		pstmt1 = conn.prepareStatement(sql);
		pstmt1.setString(1, custCode);
		pstmt1.setString(2, siteCode);
		pstmt1.setString(3, itemCode);
		pstmt1.setString(4, lotNo);
		rs1 = pstmt1.executeQuery();
		if (rs1.next())
		{
			confQtyS = rs1.getDouble("confQty");
		}
		rs1.close();
		rs1 = null;
		pstmt1.close();
		pstmt1 = null;
				if(siteCode != null && siteCode.trim().length() >0 )
				{
				minRateSellSales = getMinRate(dom,"lot_no",itemCodeS,lotNoS,quantityS ,custCode,siteCode,conn);
				min =  true;
				System.out.println("Min true condition@@@@["+min+"]");
				System.out.println("minRateSell["+minRateSellSales+"]");
				}
				diff1=EndrateSales-minRateSellSales;
				if(check== true || cnt > 0)
				{
					if(diff1 > 0 || diff1 < 0)
					{
						flag1 = true;
					}
					else
					{
						flag1 = false;
					}
					if(flag == true)
					{	
					System.out.println("Flag true");
					if (EndrateSales <= 0)
					{
						 rateS = minRateSellSales;
						System.out.println("if <0"+rateS);
					} else
					{
						rateS = Math.min(EndrateSales, minRateSellSales);
						//rateDiff = rateSellDbl - endCustRateDbl;
						System.out.println("Else"+rateS);
					}
					discAmtS = (quantityS * rateS * discountS) / 100;
					System.out.println("discAmtS["+discAmtS+"] = quantityS["+quantityS+"] * rateS ["+rateS+" * discountS]"+discountS);
					amountS = diff1 * quantityS;
					System.out.println("amount["+amountS+"] = diff1["+diff1+"] * quantity ["+quantityS+"]");
					System.out.println("netAmtS["+netAmtS+"]");
					netAmtS = amountS + discAmtS + taxAmtS;
					System.out.println("netAmtD["+netAmtS+"] = amountS["+amountS+"] +  discAmtS ["+discAmtS+"] + taxAmt[ "+taxAmtS+"]");
					discPerUnitS = (rateS * discountS) / 100;
					System.out.println("discPerUnit["+discPerUnitS+"] = (rate["+rateS+"] * discount ["+discountS+"]) / 100;");
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lineCntr1).append("]]>").append("</line_no>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA["+ itemCodeS + "]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr>").append("<![CDATA[" + (itemDescrS) + "]]>").append("</item_descr>");
					valueXmlString.append("<item_ser>").append("<![CDATA[" + (itemSerS) + "]]>").append("</item_ser>");
					valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA["+ lotNoS + "]]>").append("</lot_no>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA["+ quantityS + "]]>").append("</quantity>\r\n");
					valueXmlString.append("<rate__sell isSrvCallOnChg='0'>").append("<![CDATA["+ minRateSellSales + "]]>").append("</rate__sell>\r\n");
					//System.out.println("endCustRate::::["+endCustRate+"]");
					valueXmlString.append("<rate__contr isSrvCallOnChg='0'>").append("<![CDATA["+ EndrateSales + "]]>").append("</rate__contr>\r\n");
					valueXmlString.append("<discount_amt>").append("<![CDATA[" + getRequiredDecimal(discAmtS, 3) + "]]>").append("</discount_amt>");
					valueXmlString.append("<amount>").append("<![CDATA[" + getRequiredDecimal(amountS, 3) + "]]>").append("</amount>");
					valueXmlString.append("<net_amt>").append("<![CDATA[" + getRequiredDecimal(netAmtS, 3) + "]]>").append("</net_amt>");
					valueXmlString.append("<discount_per_unit>").append("<![CDATA[" + getRequiredDecimal(discPerUnitS, 3) + "]]>").append("</discount_per_unit>");
					valueXmlString.append("<rate__diff>").append("<![CDATA[" + getRequiredDecimal(diff1, 3) + "]]>").append("</rate__diff>");
					valueXmlString.append("<sale_qty>").append("<![CDATA[" + saleQtyS + "]]>").append("</sale_qty>");
					valueXmlString.append("<sale_ret_qty>").append("<![CDATA[" + saleRetQtyS + "]]>").append("</sale_ret_qty>");
					valueXmlString.append("<conf_claimed>").append("<![CDATA[" + getRequiredDecimal(confQtyS, 3) + "]]>").append("</conf_claimed>");
					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
					valueXmlString.append("</Detail>\r\n");
					}
					else
					{
						System.out.println("Flag False@@@@@@@@");
						/*valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<line_no>").append("").append("</line_no>\r\n");
						valueXmlString.append("<item_code >").append("").append("</item_code>\r\n");
						valueXmlString.append("<item_descr>").append("").append("</item_descr>");
						valueXmlString.append("<item_ser>").append("").append("</item_ser>");
						valueXmlString.append("<lot_no >").append("").append("</lot_no>\r\n");
						valueXmlString.append("<quantity>").append("0").append("</quantity>\r\n");
						valueXmlString.append("<rate__sell>").append("").append("</rate__sell>\r\n");
						System.out.println("endCustRate else::::["+endCustRate+"]");
						valueXmlString.append("<rate__contr>").append("<![CDATA[]]>").append("</rate__contr>\r\n");*/
					}
				}
			}
			pstmt4.close();
			pstmt4 = null;
			rs4.close();
			rs4 = null;
			quantityS=0;
			discountS=0;
			discAmtS=0;
			netAmtS=0;
			amountS=0;
			minRateSellSales=0;
			EndrateSales=0;
			diff1=0;
			rateS=0;
			discPerUnitS=0;
			valueXmlString.append("</Root>\r\n");
			System.out.println("valueXmlString..." + valueXmlString.toString());
			
		}//try end
		catch (SQLException sqx)
		{
			//System.out.println("The SQL Exception occurs in DistIssueEJB(Default) :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			//System.out.println("The Exception occurs in DistIssueEJB(Default) :"+e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		//System.out.println("valueXmlString from distIssue :"+valueXmlString);
		System.out.println("Action Default ExIT time :: \n" + valueXmlString.toString() + "\n**************\n" );
		return valueXmlString.toString();
	}

	private String checkNull(String input)
	{
		if (input == null) {
			input = " ";
		}
		return input;
	}
	public java.util.Date getDateObject(String date) throws RemoteException,ITMException
	{
		java.util.Date dat = null;
		DateFormat df = null;
		try
		{			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			dat = simpleDateFormat.parse(date);
			System.out.println("dat::::"+dat);
		}
		catch(Exception e)
		{
			System.out.println("Exception :ValidatorEJB :getDateObject :==>\n"+e.getMessage()); //$NON-NLS-1$
		}
		return dat;
	}
	String getRequiredDecimal(double actVal, int prec)
	{
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		Double DoubleValue = new Double(actVal);
		numberFormat.setMaximumFractionDigits(prec);
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",", "");
		// double reqVal = Double.parseDouble(strValue);
		return strValue; // reqVal;
	}
	private double getMinRate(Document dom, String currCol,String itemCode,String lotNo,double quantity,String custCode,String siteCode ,Connection conn) throws Exception
	{
		DistCommon distCommon = new DistCommon();
		String retReplFlag = null, sql = "", siteCodeCr="";
		//String itemCode = null;
		////String siteCode = null;
		//String lotNo = null;
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
		String invoiceId="";
		try
		{
			/*
			 * invoiceId = genericUtility.getColumnValue("invoice_id",dom1);
			 * System.out.println( "invoiceId :: " + invoiceId ); if (invoiceId
			 * != null && invoiceId.trim().length() > 0 ) { return sBuff; }
			 * retReplFlag = genericUtility.getColumnValue("ret_rep_flag",dom);
			 * System.out.println( "retReplFlag :: " + retReplFlag ); if
			 * (!"R".equals(retReplFlag) ) { System.out.println( "return 1");
			 * return sBuff; }
			 */
			//custCode = genericUtility.getColumnValue("cust_code", dom);
			sql="select invoice_id  from min_rate_history where site_code= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				invoiceId = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("invoiceId value is :["+invoiceId+"]");

			sNoSchemeHist = distCommon.getDisparams("999999", "SCHEME_HIST_NUM", conn);
			System.out.println("sNoSchemeHist :: " + sNoSchemeHist);

			if ("NULLFOUND".equals(sNoSchemeHist))
			{
				col[0] = "site_code";
				col[1] = "item_code";
				col[2] = "lot_no";
				noSchemeHist = 1;
				dynamicCol = false;
			} else
			{
				int colFound = 0;
				noSchemeHist = Integer.parseInt(sNoSchemeHist);
				for (int ctr = 1; ctr <= noSchemeHist; ctr++)
				{
					schemeKey = "SCHEME_HIST_KEY" + ctr;
					varValue = distCommon.getDisparams("999999", schemeKey, conn);
					System.out.println("currCol :: " + currCol);
					System.out.println("varValue :: " + varValue);
					if (varValue.indexOf(currCol) > -1)
					{
						colMatch = true;
						colFound = 1;
						break;
					}
				}
				if (!colMatch)
				{
					System.out.println("return 2");
					return 0;
				}
			}

			priceList = genericUtility.getColumnValue("price_list", dom);
			//itemCode = genericUtility.getColumnValue("item_code", dom);
			//lotNo = genericUtility.getColumnValue("lot_no", dom);
			tranDate = genericUtility.getColumnValue("tran_date", dom);
			String qtyStdUomStr = genericUtility.getColumnValue("quantity", dom);
			if (qtyStdUomStr == null)
			{
				qtyStdUomStr = "0";
			}
			qtyStdUom = Double.parseDouble(qtyStdUomStr);
			for (int ctr = 1; ctr <= noSchemeHist; ctr++)
			{
				docKey = null;
				int colCount = -1;
				if (dynamicCol)
				{
					colCount = -1;
					schemeKey = "SCHEME_HIST_KEY" + ctr;
					varValue = distCommon.getDisparams("999999", schemeKey, conn);
					if ("NULLFOUND".equals(varValue))
					{
						System.out.println("return 3");
						return 0;
					} else
					{
						varValue = varValue.trim();

						while (varValue.trim().length() > 0)
						{
							colCount++;
							pos = varValue.indexOf(",");
							if (pos > -1)
							{
								strToken = distCommon.getToken(varValue, ",");
								col[colCount] = strToken;
								varValue = varValue.substring(pos + 1);
							} else
							{
								col[colCount] = varValue;
								break;
							}
						} // populate column list
					}
				} else
				{
					colCount = 2;
				}
				for (int colCtr = 0; colCtr <= colCount; colCtr++)
				{
					colName = col[colCtr];
					System.out.println("colName["+colName+"]");
					 //siteCode = genericUtility.getColumnValue("site_code", dom);
						 if(colName.equalsIgnoreCase("cust_code"))
						{
							
							docValue = genericUtility.getColumnValue(colName.trim(), dom);
							System.out.println("docValue if genericUtility cust_code["+docValue+"]");
							if(docValue== null || docValue.trim().length() == 0)
							{
								docValue=custCode;
								System.out.println("docValue if["+custCode+"]");
							}
							
						}
						 else if(colName.equalsIgnoreCase("item_code"))
						{
							docValue = genericUtility.getColumnValue(colName.trim(), dom);
							System.out.println("docValue if genericUtility item_code["+docValue+"]");
							if(docValue== null || docValue.trim().length() == 0)
							{
								docValue=itemCode;
								System.out.println("docValue if itemCode["+docValue+"]");
							}
						}
						//else if(colName.equalsIgnoreCase("lot_no"))
						 else if(colName.trim().equalsIgnoreCase("lot_no"))
						{
							
							docValue = genericUtility.getColumnValue(colName.trim(), dom);
							System.out.println("docValue if genericUtility lot_no["+docValue+"]");
							if(docValue== null || docValue.trim().length() == 0)
							{
								docValue=lotNo;
								System.out.println("docValue if lotNo["+docValue+"]");
							}
						}
						else if(colName.equalsIgnoreCase("invoice_id"))
						{
							docValue = genericUtility.getColumnValue(colName.trim(), dom);
							System.out.println("docValue if genericUtility invoice_id["+docValue+"]");
							if(docValue== null || docValue.trim().length() == 0)
							{
								docValue=invoiceId;
								System.out.println("docValue if invoiceId["+docValue+"]");
							}
						}
						
					if (docKey != null && docKey.trim().length() > 0)
					{
						docKey = docKey + "," + (docValue == null || docValue.trim().length() == 0 ? "" : docValue.trim()); // 13/05/10
						// manoharan
						// docValue trim()
						// added
					} else
					{
						docKey = docValue;
					}
					System.out.println("docKey if["+docKey+"]");
				}
				sql = " select eff_cost from min_rate_history where doc_key = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, docKey);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					minRate = rs.getDouble(1);
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;
				if (minRate > 0)
				{
					break;
				}
			}//end for loop
			if (minRate == 0)
			{
				tranDate = tranDate == null ? (genericUtility.getValidDateString(new Timestamp(System.currentTimeMillis()).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat())).toString() : tranDate;
				itemCode = itemCode == null ? "" : itemCode;
				lotNo = lotNo == null ? "" : lotNo;
				System.out.println("Quantity["+quantity+"]");
				minRate = distCommon.pickRate(priceList, tranDate, itemCode, lotNo, "D", qtyStdUom, conn);
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return minRate;
	}
}