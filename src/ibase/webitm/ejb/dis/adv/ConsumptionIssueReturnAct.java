package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
@Stateless
public class ConsumptionIssueReturnAct extends ActionHandlerEJB implements ConsumptionIssueReturnActLocal,ConsumptionIssueReturnActRemote
{
	E12GenericUtility genericUtility= new E12GenericUtility();
	String act_type=null;
	
	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		
		System.out.println(">>>>>>>>>>>>>In actionHandler actionDefault:");
		Document dom = null;
		Document dom1 = null;
        
		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println(">>>>>>>>>>>>>XML String>:"+xmlString);
				dom = genericUtility.parseString(xmlString); 
				System.out.println(">>>>>>>>>>>>>Dom:"+dom);
			}
			System.out.println(">>>>>>>>>>>>>>>>actionType:"+actionType+":");
						
		
			if (actionType.equalsIgnoreCase("Default"))
			{
				if(xmlString1 != null && xmlString1.trim().length()!=0)
				{
					System.out.println(">>>>>>>>>>>>>>XML String1:"+xmlString1);
					dom1 = genericUtility.parseString(xmlString1);
					System.out.println(">>>>>>>>>>>>>>dom1:"+dom1);
					
				}
				System.out.println("*********************Before Call actionDefault*********************");
				retString = actionDefault(dom1,objContext,xtraParams);//function to fetch data
				
			}
			
		
			 
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :ModelHandler :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from ModelHandler : actionHandler"+retString);
	    return retString;
	}
	
	private String actionDefault( Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String s1="Example",lineNo="",siteCode="",locationCode="",locCode="",consIssue="",consOrder="",itemCode = "",
		lineNoOrd = "",itemDescr = "",lotNo="",lotSl="",unit=null,unitStd=null;
		String sql="";
		int lineCntr=0;
		int totalCnt=0;
		String acctCode="",cctrCode="",acctCodeInv="",cctrCodeInv="",itemSer="",taxClass = "",taxChap = "",taxEnv = "",qcReq="";
		double quantity=0.0d,quantityStd=0.0d,amount=0.0d,quantityStduom = 0d,conv = 0d,noArt = 0d,rate=0.0d,taxAmt=0.0d,rateAmt=0.0d,netAmt=0.0d;
	
		
		String  retString = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Statement stmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		Connection conn = null;
	    //ConnDriver connDriver = new ConnDriver();		
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		NodeList parentNodeList = null;
		Node parentNode=null;
		
		try
		{
			conn = getConnection();
			consIssue = genericUtility.getColumnValue("tran_id__iss",dom1);
			System.out.println("Consumer Issue number::::::::"+consIssue);
			
				
			
			String sql1="  SELECT CONSUME_ISS_DET.CONS_ISSUE, "   
					       +" CONSUME_ISS_DET.LINE_NO, "   
					       +" CONSUME_ISS_DET.CONS_ORDER ,"   
					       +" CONSUME_ISS_DET.LINE_NO__ORD ,"   
					       +" CONSUME_ISS_DET.ITEM_CODE ,"   
					       +" CONSUME_ISS_DET.QUANTITY ,"   
					       +" CONSUME_ISS_DET.UNIT,"   
					       +" CONSUME_ISS_DET.RATE ,"   
					       +" CONSUME_ISS_DET.AMOUNT,"   
					       +" CONSUME_ISS_DET.TAX_CLASS, "   
					       +" CONSUME_ISS_DET.TAX_CHAP  ,"   
					       +" CONSUME_ISS_DET.TAX_ENV ,"   
					       +" CONSUME_ISS_DET.TAX_AMT ,"   
					       +" CONSUME_ISS_DET.NET_AMT ,"   
					       +" CONSUME_ISS_DET.ACCT_CODE ,"   
					       +" CONSUME_ISS_DET.CCTR_CODE ,"   
					       +" CONSUME_ISS_DET.LOC_CODE  ,"   
					       +" CONSUME_ISS_DET.LOT_NO  ,"  
					       +" CONSUME_ISS_DET.LOT_SL  ,"    
					       +" CONSUME_ISS_DET.QUANTITY__STD ,"   
					       +" CONSUME_ISS_DET.UNIT__STD,"   
					       +" CONSUME_ISS_DET.CONV_QTY_STDUOM,"   
					       +" ITEM.DESCR,"  
					       +" CONSUME_ISS_DET.QC_REQD,"   
					       +" CONSUME_ISS_DET.ACCT_CODE__INV,"   
					       +" CONSUME_ISS_DET.CCTR_CODE__INV,"   
					       +" CONSUME_ISS_DET.NO_ART,"  
					       +" CONSUME_ISS_DET.ANAL_CODE "  
					    + " FROM CONSUME_ISS_DET, "   
					         +"ITEM,"   
					         +" CONSUME_ISS " 
					   +"WHERE ( CONSUME_ISS_DET.ITEM_CODE = ITEM.ITEM_CODE ) "
					        +"  and "  
					        +"( CONSUME_ISS_DET.CONS_ISSUE =CONSUME_ISS.CONS_ISSUE  ) "
					        + " and "  
					        + "( ( consume_iss_det.cons_issue='"+consIssue+"') ) "   
					+" ORDER BY CONSUME_ISS_DET.LINE_NO ASC ";
			
			
			stmt=conn.createStatement();//(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs=stmt.executeQuery(sql1);
			while(rs.next())
			{
				
				lineCntr++;
				lineNo=rs.getString("line_no");
				System.out.println("lineNO"+lineNo);
				consOrder=rs.getString("cons_order");
				System.out.println("consOrder"+consOrder);
				lineNoOrd=rs.getString("line_no__ord");
				System.out.println("lineNoOrd"+lineNo);
				itemCode = rs.getString("item_code");
				System.out.println("itemCode"+itemCode);
				quantity=rs.getDouble("quantity");
				System.out.println("quantity"+quantity);
				unit = rs.getString("unit");
				System.out.println("unit"+unit);
				rate=rs.getDouble("rate");
				System.out.println("rate"+rate);
				amount=rs.getDouble("amount");
				System.out.println("amount"+amount);
				taxClass = rs.getString("tax_class");
				System.out.println("lineNO"+lineNo);
				taxChap = rs.getString("tax_chap");
				System.out.println("taxChap"+taxChap);
				taxEnv = rs.getString("tax_env");
				System.out.println("taxEnv"+taxEnv);
				taxAmt = rs.getDouble("tax_amt");
				System.out.println("taxAmt"+taxAmt);
				netAmt = rs.getDouble("net_amt");
				System.out.println("netAmt"+netAmt);
				acctCode = rs.getString("acct_code");
				System.out.println("acctCode"+acctCode);
				cctrCode = rs.getString("cctr_code");
				System.out.println("cctrCode"+cctrCode);
				locCode = rs.getString("loc_code");
				System.out.println("locCode"+locCode);
				lotNo = rs.getString("lot_no");
				System.out.println("lotNo"+lotNo);
				lotSl = rs.getString("lot_sl");
				System.out.println("lotSl"+lotSl);
				quantityStd = rs.getDouble("quantity__std");
				System.out.println("quantityStd"+quantityStd);
				unitStd = rs.getString("unit__std");
				System.out.println("unitStd"+unitStd);
				conv=rs.getDouble("CONV_QTY_STDUOM");
				System.out.println("conv"+conv);
				itemDescr = rs.getString("descr");
				System.out.println("itemDescr"+itemDescr);
				qcReq= rs.getString("qc_reqd");
				System.out.println("qcReq"+qcReq);
				acctCodeInv=rs.getString("acct_code__inv");
				System.out.println("acctCodeInv"+acctCodeInv);
				cctrCodeInv=rs.getString("cctr_code__inv");
				System.out.println("cctrCodeInv"+cctrCodeInv);
				noArt=rs.getDouble("no_art");
				System.out.println("noArt"+noArt);
				
				
				
				
				
				System.out.println("Item no::::"+lineNo);
				
				valueXmlString.append("<Detail>\r\n");
				//valueXmlString.append("<cons_issue>").append("<![CDATA[").append(consIssue).append("]]>").append("</cons_issue>\r\n");
				valueXmlString.append("<line_no>").append("<![CDATA[").append(lineCntr).append("]]>").append("</line_no>\r\n");
				valueXmlString.append("<cons_order>").append("<![CDATA[").append(consOrder).append("]]>").append("</cons_order>\r\n");
				valueXmlString.append("<line_no__ord>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
		    	valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit>").append("<![CDATA[").append(unit.trim()).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
				valueXmlString.append("<amount>").append("<![CDATA[").append(amount).append("]]>").append("</amount>\r\n");
				valueXmlString.append("<tax_class>").append("<![CDATA[").append(taxClass).append("]]>").append("</tax_class>\r\n");
				valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv).append("]]>").append("</tax_env>\r\n");
				valueXmlString.append("<tax_amount>").append("<![CDATA[").append(taxAmt).append("]]>").append("</tax_amount>\r\n");
				valueXmlString.append("<net_amount>").append("<![CDATA[").append(netAmt).append("]]>").append("</net_amount>\r\n");
				valueXmlString.append("<acct_code>").append("<![CDATA[").append(acctCode).append("]]>").append("</acct_code>\r\n");
				valueXmlString.append("<cctr_code>").append("<![CDATA[").append(cctrCode).append("]]>").append("</cctr_code>\r\n");
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<quantity__std>").append("<![CDATA[").append(quantityStd).append("]]>").append("</quantity__std>\r\n");
				valueXmlString.append("<unit__std>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
				valueXmlString.append("<CONV_QTY_STDUOM>").append("<![CDATA[").append(conv).append("]]>").append("</CONV_QTY_STDUOM>\r\n");
				valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr.trim()).append("]]>").append("</item_descr>\r\n");
				valueXmlString.append("<qc_reqd>").append("<![CDATA[").append(qcReq.trim()).append("]]>").append("</qc_reqd>\r\n");
				valueXmlString.append("<acct_code__inv>").append("<![CDATA[").append(acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
				valueXmlString.append("<cctr_code__inv>").append("<![CDATA[").append(cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
				valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
				valueXmlString.append("</Detail>");
		    }
			if(lineCntr==0)
			{
				retString = itmDBAccessEJB.getErrorString("","INVTR","","",conn);
			}
			
			
			System.out.println("*********In Act Default:"+lineCntr);
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			valueXmlString.append("</Root>\r\n");
							
			
			
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in actionDefault actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
			
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}
			catch(Exception e){}
		}
		
		return valueXmlString.toString();
		
		
		
	}

	
	
	
	
	

}
