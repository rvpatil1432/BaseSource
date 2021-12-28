package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class SordBulkIC extends ValidatorEJB implements SordBulkICLocal,SordBulkICRemote
{
E12GenericUtility genericUtility = new E12GenericUtility();
FinCommon finCommon = new FinCommon();
public String wfValData(String xmlString, String xmlString1, String xmlString2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
{
	System.out.println("------------ wfvalData method called-----------------");
	System.out.println("xmlString --->>>  [["+xmlString+"  ]]");
	System.out.println("xmlString1 --->>>  [["+xmlString1+"  ]]");
	System.out.println("xmlString2 --->>>  [["+xmlString2+"  ]]");
	System.out.println("editFlag --->>>  [["+editFlag+"  ]]");
	
	String errString = null;
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	try
	{
		dom = parseString(xmlString);
		dom1 = parseString(xmlString1);
		dom2 = parseString(xmlString2);
		errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		
	}
	catch(Exception e)
	{
		System.out.println("Exception : SordBulkIC.java : wfValData(String xmlString) : ==>\n"+e.getMessage());
		throw new ITMException(e);
	}
	
	return errString;
} //end of wfValData 

public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
{		
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	int ctr=0;
	double quantity = 0.0,rate = 0.0 ;
	String childNodeName = null;
	String priceList = "",custCode = "",prdCode="",siteCode="",itemCode="",userId = "",sql="";
	String empCode="",cctrCode="",errCode="";
	String errString="",errorType="",tranDate="",acct_prd="",itemSer="";
	Connection conn = null;
	PreparedStatement pstmt = null;		
	ResultSet rs=null;
	int cnt1=0,cnt=0;		
	int currentFormNo=0;
	int childNodeListLength;
	java.util.Date tranDateL = null;
	ConnDriver connDriver = new ConnDriver();
	ArrayList <String> errList = new ArrayList<String>();
	ArrayList <String>errFields = new ArrayList <String> ();
	StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
	
	try
	{		
		SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(genericUtility.getApplDateFormat());
		//Changes and Commented By Bhushan on 06-06-2016 :START
//conn = connDriver.getConnectDB("DriverITM");
conn = getConnection();
//Changes and Commented By Bhushan on 06-06-2016 :END			
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		System.out.println("user ID form XtraParam : "+userId +"Edit Flag -->>: "+editFlag);
		
		if(objContext != null && objContext.trim().length()>0)
		{
			currentFormNo = Integer.parseInt(objContext);
		}
		switch(currentFormNo)
		{
			case 1 :
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("Child name --->> "+childNodeName);				
				
				if(childNodeName.equalsIgnoreCase("tran_date")) 
				{
					tranDate = checkNull(genericUtility.getColumnValue("tran_date",dom));
					System.out.println("tran_date--->["+tranDate+"]");
					
					if(tranDate.length() == 0)
					{
					errCode = "VMTRNDNN"; 
					errList.add( errCode );
					errFields.add( childNodeName.toLowerCase() );	
					}
				}
				if (childNodeName.equalsIgnoreCase("site_code"))
				{
					tranDate = checkNull(genericUtility.getColumnValue("tran_date",dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code",dom));
					System.out.println("siteCode---->["+siteCode+"]");
					if (siteCode.length()==0)
					{
						errCode = "VTSITENN"; 
						errList.add( errCode );
						errFields.add( childNodeName.toLowerCase() );								
					}
					else
					{
						sql= "select count(*) from site where site_code = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							cnt1=rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Site  code cnt--->"+cnt1);	
						if(cnt1 == 0)
						{
						errCode = "VTSITEND"; 
						errList.add( errCode );
						errFields.add( childNodeName.toLowerCase() );	
						}
						else
						{
							if(tranDate.length() > 0)
							{
							tranDateL=simpleDateFormatObj.parse(tranDate);
							//Changes and Commented By Ajay on 20-12-2017 :START
							//String code = checkNull(this.nfCheckPeriod("FIN", tranDateL, siteCode));
							String code=finCommon.nfCheckPeriod("FIN", tranDateL, siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							System.out.println("Period check111--->>["+code+"]");
							if(code.length() > 0)
							{
								errCode = code; 
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );	
							}
							}
						}
						
					}
				
				}
				if (childNodeName.equalsIgnoreCase("item_ser"))
				{
					itemSer = checkNull(genericUtility.getColumnValue("item_ser",dom));
					System.out.println("Item Series--->["+itemSer+"]");
					if(itemSer == null || itemSer.length()==0)
					{
						errCode = "VTITEMSER5";
						errList.add( errCode );
						errFields.add( childNodeName.toLowerCase() );
					}
					else
					{
					sql= "select count(*) from itemser where item_ser = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,itemSer);
					rs=pstmt.executeQuery();
					if(rs.next()){
						cnt1=rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("Count value for Item Series--->"+cnt1);	
					if(cnt1 == 0)
					{
					errCode = "VMITEMSER1";
					errList.add( errCode );
					errFields.add( childNodeName.toLowerCase() );	
					}
				}
				
				}	
			 } //end for loop  
			break;
			case 2 :
			parentNodeList = dom.getElementsByTagName("Detail2");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if (childNodeName.equalsIgnoreCase("cust_code"))
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
					System.out.println("custCode--->["+custCode+"]");
					if(custCode == null || custCode.length()==0)
					{
						errCode = "NULLCUSTCD";
						errList.add( errCode );
						errFields.add( childNodeName.toLowerCase() );
					}
					else
					{
					sql= "select count(*) from customer where cust_code = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs=pstmt.executeQuery();
					if(rs.next()){
						cnt1=rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("Count value for custCode--->"+cnt1);	
					if(cnt1 == 0)
					{
					errCode = "VTIVCC";
					errList.add( errCode );
					errFields.add( childNodeName.toLowerCase() );	
					}
					}
				}	
				if (childNodeName.equalsIgnoreCase("item_code"))
					//if (childNodeName.equalsIgnoreCase("quantity"))
				{
					itemCode = checkNull(genericUtility.getColumnValue("item_code",dom));
					System.out.println("itemCode--->["+itemCode+"]");
					if(itemCode == null || itemCode.length()==0)
					{
						errCode = "WORDWIICCB";
						errList.add( errCode );
						errFields.add( childNodeName.toLowerCase() );
					}
					else
					{
					sql= "select count(*) from item where item_code = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs=pstmt.executeQuery();
					if(rs.next()){
						cnt1=rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					System.out.println("Count value for itemCOde--->"+cnt1);	
					if(cnt1 == 0)
					{
					errCode = "VTTASK4";
					errList.add( errCode );
					errFields.add( childNodeName.toLowerCase() );	
					}
					}
				}	
				if (childNodeName.equalsIgnoreCase("quantity"))
				{
					quantity = Double.parseDouble(genericUtility.getColumnValue("quantity",dom) == null ? "0" : genericUtility.getColumnValue("quantity",dom));
					System.out.println("quantity--->["+quantity+"]");
					if(quantity < 0 || quantity == 0)
					{
					errCode = "INVQTY"; //quantity should not be zero or less than zero
					errList.add( errCode );
					errFields.add( childNodeName.toLowerCase() );	
					}
				}
			
			}
			break;
										
		} //end switch
		
		int errListSize = errList.size();
		cnt =0;
		String errFldName = null;
		if ( errList != null && errListSize > 0 )
		{
			for (cnt = 0; cnt < errListSize; cnt++ )
			{
				errCode = errList.get(cnt);
				errFldName = errFields.get(cnt);
				System.out.println("errCode .........."+errCode);
				errString = getErrorString( errFldName, errCode, userId );
				errorType =  errorType( conn, errCode );
				if ( errString.length() > 0)
				{
					String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
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
		
	} //end try
	catch (SQLException se) {
		System.out.println("SQLException ::"+se);
		se.printStackTrace();
        throw new ITMException(se);
	}
	catch(Exception e)
	{
		System.out.println("Exception ::"+e);
		e.printStackTrace();
        throw new ITMException(e);
	}
	finally
	{
		try
		{
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}	
			if(conn!=null)
			{
				conn.close();
				conn = null;
			}
			if(connDriver!=null){
				connDriver = null;
			}	
			if(rs !=null)
			{
				rs.close();
				rs=null;
			}				
		}
		catch(Exception d)
		{
		  d.printStackTrace();
		}
	}
	
	System.out.println("ErrString ::[ "+errStringXml.toString()+" ]");
	return errStringXml.toString();
	
}


	
	
public String itemChanged(String xmlString,String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
{
	System.out.println("------------------ itemChanged called------------------");
	System.out.println("xmlString DOM-->>["+xmlString+"]");
	System.out.println("xmlString DOM1-->>["+xmlString1+"]");
	System.out.println("xmlString DOM2-->>["+xmlString2+"]");
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	String valueXmlString = "";
	try
	{   
		dom = parseString(xmlString);
		dom1 = parseString(xmlString1);
		dom2 = parseString(xmlString2);
		valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
	}
	catch(Exception e)
	{
		System.out.println("Exception : [SordBulkIC ][itemChanged(String,String)] :==>\n"+e.getMessage());
	}
	return valueXmlString;
}

public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
{		
	System.out.println("@@@@@@@ itemChanged called");
	StringBuffer valueXmlString = new StringBuffer();
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	double pickRate = 0.0,quantity = 0.0,netAmt = 0.0,rate = 0.0;
	E12GenericUtility genericUtility = new E12GenericUtility();
	ConnDriver connDriver = new ConnDriver();
	String confirm="N",loginSite="",siteDescr1="",orderType="";
	int currentFormNo =0;
	String ItemDescr="",Itemcode="", OrderDate ="", Pricelist="",Itemser=""	,unitDescr="",unit="",custCode="",custDescr="",siteCode="";
	DistCommon distCommon = new DistCommon();
	String columnValue = null;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	DistCommon disComm = new DistCommon();
	try
	{	
		SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(genericUtility.getApplDateFormat());
	   Calendar currentDate = Calendar.getInstance();		
	   String tranDate = simpleDateFormatObj.format(currentDate.getTime());	
		
		//Changes and Commented By Bhushan on 06-06-2016 :START
//conn = connDriver.getConnectDB("DriverITM");
conn = getConnection();
//Changes and Commented By Bhushan on 06-06-2016 :END
		conn.setAutoCommit(false);
		connDriver=null;
		if(objContext != null && objContext.trim().length()>0)
		{
			currentFormNo = Integer.parseInt(objContext.trim());
		}
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
		valueXmlString.append(editFlag).append("</editFlag></header>");
		System.out.println("-------- currentFormNo : "+currentFormNo);
		
		switch(currentFormNo)
		{	
			case 1 :
			valueXmlString.append("<Detail1>");				
			System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
			System.out.println("editFlag =>" +editFlag);
			
			if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
			{					
				System.out.println("------------in itm_default--------->");
				currentDate = Calendar.getInstance();					
				tranDate = simpleDateFormatObj.format(currentDate.getTime());	
				String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
				valueXmlString.append("<add_date>").append("<![CDATA[" + tranDate + "]]>").append("</add_date>");
				valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
				valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
			
				valueXmlString.append("<tran_date>").append("<![CDATA[" + tranDate + "]]>").append("</tran_date>");
				valueXmlString.append("<confirmed>").append("<![CDATA[" + confirm + "]]>").append("</confirmed>");				 
				loginSite = checkNull(getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));					
				System.out.println("loginSite ----->>["+loginSite+"]");
				if(loginSite.length() > 0)
				{
					siteDescr1=getColumnDescr(conn, "DESCR", "site", "SITE_CODE", loginSite);					
				}
				System.out.println("loginSite-->["+loginSite.length()+"]");
				System.out.println("tranDate-->["+tranDate.length()+"]");
				valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
				valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr1 + "]]>").append("</site_descr>");
				orderType = disComm.getDisparams( "999999", "SORD_BULK_ORDER_TYPE", conn );
				System.out.println( "orderType ::" + orderType );
				if( orderType == null || orderType.equalsIgnoreCase( "NULLFOUND" ) || orderType.trim().length() == 0 )
				{
					orderType="";
					valueXmlString.append("<order_type>").append("<![CDATA[" + orderType + "]]>").append("</order_type>");
				}
				else if("SM".equalsIgnoreCase(orderType))
				{
					valueXmlString.append("<order_type>").append("<![CDATA[" + orderType + "]]>").append("</order_type>");
				}
				else if(!"SM".equalsIgnoreCase(orderType))
				{
					valueXmlString.append("<order_type>").append("<![CDATA[" + orderType + "]]>").append("</order_type>");
				}
			}			
		
			else if (currentColumn.trim().equalsIgnoreCase("site_code"))//FOR SITE CODE
			{
				siteCode = checkNull(genericUtility.getColumnValue("site_code",dom));
				System.out.println("siteCode11 ----->>["+siteCode+"]");
				if(siteCode.length() > 0){
				siteDescr1=getColumnDescr(conn, "DESCR", "site", "SITE_CODE", siteCode);
				}
				valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr1 + "]]>").append("</site_descr>");
				
			}
			else if (currentColumn.trim().equalsIgnoreCase("item_ser"))//FOR ITEM SER
			{
				Itemser = checkNull(genericUtility.getColumnValue("item_ser",dom));
				System.out.println("item_ser ----->>["+Itemser+"]");
				if(Itemser.length() > 0){
					ItemDescr=getColumnDescr(conn, "DESCR", "itemser", "ITEM_SER", Itemser);
				}
				System.out.println("ITEM DESCR>>>>"+ItemDescr);
				valueXmlString.append("<itemser_descr>").append("<![CDATA[" + ItemDescr + "]]>").append("</itemser_descr>");
				
			}
			valueXmlString.append("</Detail1>");
			  break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				int childListLength = childNodeList.getLength();	
				
			 if (currentColumn.trim().equalsIgnoreCase("item_code"))//FOR ITEM MASTER
				{
					Itemcode = checkNull(genericUtility.getColumnValue("item_code",dom));
					System.out.println("item_code ----->>["+Itemcode+"]");
					if(Itemcode.length() > 0){
					ItemDescr=getColumnDescr(conn, "DESCR", "item", "ITEM_CODE", Itemcode);
					}
					valueXmlString.append("<item_descr>").append("<![CDATA[" + ItemDescr + "]]>").append("</item_descr>");
				}
			 if (currentColumn.trim().equalsIgnoreCase("cust_code"))//FOR CUSTOMER MASTER
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
					System.out.println("cust_code ----->>["+custCode+"]");
					if(custCode.length() > 0){
						custDescr =getColumnDescr(conn, "CUST_NAME", "customer", "CUST_CODE", custCode);
					}
					valueXmlString.append("<cust_name>").append("<![CDATA[" + custDescr + "]]>").append("</cust_name>");
					
				}
				valueXmlString.append("</Detail2>");
			}// end switch 
		valueXmlString.append("</Root>");			
		
		
	} //end try-------------------------------------------------------------
	catch(Exception e)
	{
		e.printStackTrace();
		System.out.println("Exception ::"+e.getMessage());			
		throw new ITMException(e);
	}
	finally
	{
		try
		{
			if(conn!=null)
			{
				conn.close();
				conn = null;
			}
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
		catch(Exception d)
		{
		  d.printStackTrace();
		}			
	}
	
	return valueXmlString.toString();
}
private String errorType( Connection conn , String errorCode ) throws ITMException
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
		throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
public String checkNull(String input)	
{
	if (input == null)
	{
		input="";
	}
	return input.trim();
}
public String getColumnDescr(Connection conn, String columnName ,String tableName, String columnName2, String value) throws ITMException 
{

		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		String findValue = "";
		try
		{			
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{					
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in getColumnDescr ");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("returning String from getColumnDescr " + findValue);
		return findValue;
	 
}


}
