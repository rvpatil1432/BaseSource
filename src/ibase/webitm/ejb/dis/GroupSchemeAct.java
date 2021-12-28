package ibase.webitm.ejb.dis;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

@Stateless
public class GroupSchemeAct extends ActionHandlerEJB implements GroupSchemeActLocal, GroupSchemeActRemote
{
	
	String DB = CommonConstants.DB_NAME;                  
	ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();
	String currentActionType="",packInstrStock="",packInstrInvPackRcp="";


	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		Document dom = null;
		try
		{
			System.out.println("Call method =Action handler");
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return retString;
	}

	

	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				System.out.println("dom :"+dom);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				System.out.println("dom1 :"+dom1);
			}
			System.out.println("actionType:"+actionType+":");
			currentActionType = actionType;	
			System.out.println("currentActionType:["+currentActionType+"]");

			if (actionType.equalsIgnoreCase("Scheme"))
			{
				retString = actionStock(dom,dom1,objContext,xtraParams);
			}
			
			if (actionType.equalsIgnoreCase("Offer"))
			{
				retString = actionOffer(dom,dom1,objContext,xtraParams);
			}
			



		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from Despatch : actionHandler"+retString);
		return retString;
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
				}
			}
			System.out.println("actionType:"+actionType+":");

			
			
		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		System.out.println("returning String from Despatch.................."); 
		return retString;
	}

	

	

	private String actionStock(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
	
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement stmt1 = null;
		PreparedStatement pstmt3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		String sql = "";
		String errCode = "";
		String purProdCode = "",itemCode = "",quantity = "",siteCode = "",available = "",descr = "";
		String stklotNo = "",stklotSl = "",stkAllocQty = "";
		java.sql.Date stkMfgDate = null;
		String stkMfgDate1 = ""; 
		java.sql.Date stkExpDate = null;
		String stkExpDate1 = "";
		String stkquantity = "";
		String stkLocCode = "";
		String stkNoArt = "" ;
		String stkRate = "";
		String balanceQty = "";
		String trackShelfLife = "";
		int cnt1=0,cnt2=0;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		try
		{	
			
			conn = getConnection();
		
			stmt = conn.createStatement();
			
			purProdCode	= genericUtility.getColumnValue("prod_code__pur",dom1);
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			Timestamp currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdf.format(currDate);
				
			Timestamp toDate= Timestamp.valueOf(genericUtility.getValidDateString(currDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("itemcodecheckDate!!"+toDate);
				
			
			
			
			sql="SELECT "
					+"ITEM_CODE, "   
					+"DESCR "   
					+"FROM ITEM "  
					+"WHERE Product_code= ? " ;
			pstmt3 = conn.prepareStatement(sql);
			pstmt3.setString(1,purProdCode);
			
			rs = pstmt3.executeQuery();
			
			System.out.println(":actionStock:sql:"+sql);
			
	
			while(rs.next())
			{
				itemCode = rs.getString("ITEM_CODE");
				descr	= rs.getString("DESCR");
				
				
				
			
				
					
				sql = " select count (*) from scheme_applicability a ,sch_pur_items b where a.scheme_code =  b. scheme_code and  a.app_from<= ? and a.valid_upto>= ? and a.prod_sch='Y' AND b.item_code = ? ";
				 
				stmt1 = conn.prepareStatement(sql);
				stmt1.setTimestamp(1, toDate);
				stmt1.setTimestamp(2, toDate);
				stmt1.setString(3, itemCode);
					
					rs2 = stmt1.executeQuery();
					if(rs2.next())
					{
						cnt1 = rs2.getInt(1);	
					}
					stmt1.close();
					stmt1 = null;
					rs2.close();
					rs2 = null;
					if(cnt1 == 0)
					{
				
				
			
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
			
					valueXmlString.append("<descr>").append("<![CDATA[").append(descr).append("]]>").append("</descr>\r\n");
					
					valueXmlString.append("</Detail>\r\n");
					}
			
				
			}
			pstmt3.close();
			pstmt3 = null;
			rs.close();
			rs = null;
			valueXmlString.append("</Root>\r\n");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println(" retXmlString 4 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Despatch :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("Despatch:actionStock:Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	
	
	
	
	
	

	private String actionOffer(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
	
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String errCode = "";
		String offProdCode = "",itemCode = "",quantity = "",siteCode = "",available = "",descr = "";
		String stklotNo = "",stklotSl = "",stkAllocQty = "";
		java.sql.Date stkMfgDate = null;
		String stkMfgDate1 = ""; 
		java.sql.Date stkExpDate = null;
		String stkExpDate1 = "";
		String stkquantity = "";
		String stkLocCode = "";
		String stkNoArt = "" ;
		String stkRate = "";
		String balanceQty = "";
		String trackShelfLife = "";
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		try
		{	
			
			conn = getConnection();
		
			stmt = conn.createStatement();
			
			offProdCode	= genericUtility.getColumnValue("prod_code__off",dom1);
			
		
		
			
			
			sql="SELECT "
					+"ITEM_CODE, "   
					+"DESCR "   
					+"FROM ITEM "  
					+"WHERE Product_code= '"+offProdCode+"'" ;
			
			System.out.println("Despatch:actionStock:sql:"+sql);
			rs = stmt.executeQuery(sql);
	
			while(rs.next())
			{
				itemCode = rs.getString("ITEM_CODE");
				descr	= rs.getString("DESCR");
				
				
			
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
			
					valueXmlString.append("<descr>").append("<![CDATA[").append(descr).append("]]>").append("</descr>\r\n");
					
					valueXmlString.append("</Detail>\r\n");
				
			}
			valueXmlString.append("</Root>\r\n");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println(" retXmlString 4 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Despatch :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("Despatch:actionStock:Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	

	
	private String getCurrentUpdateFlag(Document dom)
	{
		NodeList detailList = null;
		Node currDetail = null;
		NodeList currDetailList = null;
		String updateStatus = "",nodeName = "";
		int currDetailListLength = 0;
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail2");
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
		}
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
		System.out.println("updateStatus in [getCurrentUpdateFlag()] :: " + updateStatus);
		return updateStatus;
	}

		

	
	public String blanknull(String s)
	{
		if(s==null)
			return "";
		else
			return s.trim();
	}

	

	

	private String updateMessage(String resultString,String message) throws ITMException
	{
		StringBuffer stbf = new StringBuffer();
		try{
			System.out.println("resultString : "+resultString);
			stbf.append(resultString.substring(0,resultString.indexOf("<trace>")));
			if(message != null && message.trim().length() > 0){
				stbf.append("<trace>"+message+"</trace>");
				stbf.append(resultString.substring(resultString.indexOf("</trace>")+8));
			}else{
				stbf.append(resultString.substring(resultString.indexOf("<trace>")));
			}
			System.out.println("Resulting String : "+stbf.toString());

		}catch(Exception e){
			System.out.println("Exception in updateMessage : "+e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return stbf.toString();   
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

	public double getReqDecimal(double actVal, int prec)
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
	private String serializeDom(Node dom) throws Exception
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
			System.out.println("Exception : In : serializeDom :"+e);
			e.printStackTrace();

		}
		return retString;
	}
	
	


}
