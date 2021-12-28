
/********************************************************
Title : LoadClose EJB
Date  : 10 - Sept - 2014
Author: Deepak Sawant.

********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class LoadClose extends ValidatorEJB implements LoadCloseLocal, LoadCloseRemote {

//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
String userId = null;
String chgUser = null;
String chgTerm = null;
NumberFormat nf = null;
boolean isError=false;



public LoadClose() {
	System.out.println("^^^^^^^ inside Load Close ^^^^^^^");
}

public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
	System.out.println("^^^^^^^ inside Load Close 12^^^^^^^");
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	String errString = "";

	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	String childNodeName = "";

	try {
		dom = parseString(xmlString);
		dom1 = parseString(xmlString1);
		if (xmlString2.trim().length() > 0) {
			dom2 = parseString("<Root>" + xmlString2 + "</Root>");
		}
		if (objContext != null && Integer.parseInt(objContext) == 1) {
			parentNodeList = dom2.getElementsByTagName("Header0");
			parentNode = parentNodeList.item(1);
			childNodeList = parentNode.getChildNodes();
			for (int x = 0; x < childNodeList.getLength(); x++) {
				childNode = childNodeList.item(x);
				childNodeName = childNode.getNodeName();
				if (childNodeName.equalsIgnoreCase("Detail1")) {
					errString = wfValData(dom, dom1, dom2, "1", editFlag, xtraParams);
					if (errString != null && errString.trim().length() > 0)
						break;
				} else if (childNodeName.equalsIgnoreCase("Detail2")) {
					errString = wfValData(dom, dom1, dom2, "2", editFlag, xtraParams);
					break;
				}
			}
		} else {
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
	} catch (Exception e) {
		System.out.println("Exception : Inside Load Close wfValData Method ..> " + e.getMessage());
		throw new ITMException(e);
	}
	return (errString);
}

public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
	System.out.println("^^^^^^^ inside Load Close wfvaldata---------------");
	//GenericUtility genericUtility;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	int ctr = 0, currentFormNo = 0, childNodeListLength = 0, cnt = 0;
	String childNodeName = null;
	String errString = "";
	String errCode = "";
	Connection conn = null;
	String userId = "";
	PreparedStatement pstmt = null ;
	ResultSet rs = null;
	String sql = "",locCode ="";
	String shipmentId = "";


	String workOrder = "",squantity="",wotatus="",itemCode="";
	double quantity = 0,noOfArt=0,woquantity=0,detquantity=0,totanquan=0;



	try {

		System.out.println("editFlag>>>>wf"+editFlag);
		System.out.println("xtraParams>>>wf"+xtraParams);


		ConnDriver connDriver = new ConnDriver();
		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		conn.setAutoCommit(false);
		connDriver = null;
		userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		//genericUtility = GenericUtility.getInstance();
		if (objContext != null && objContext.trim().length() > 0) {
			currentFormNo = Integer.parseInt(objContext);
		}
		switch (currentFormNo) {
		case 1:
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();

			for (ctr = 0; ctr < childNodeListLength; ctr++) {


				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();

				if(childNodeName.equalsIgnoreCase("shipment_id1"))
				{
					shipmentId = genericUtility.getColumnValue("shipment_id1",dom);

					if (shipmentId == null || shipmentId.trim().length() == 0)
					{
						errCode = "LOADCLSHNU";
						errString = getErrorString("shipment_id1",errCode,userId);
						break;
					}
					else
					{
						
					sql = "select count(1) from ship_docs where shipment_id = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,shipmentId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt(1);
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if(cnt == 0)
					{
						errCode = "LOADCLSHNE";
						errString = getErrorString("shipment_id1",errCode,userId);
						break;
					}
					else
					{
						
						sql = "SELECT count(*) FROM ship_docs where ref_ser in ('D-ISS','S-DSP') and shipment_id = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,shipmentId);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						if(cnt == 0)
						{
							errCode = "LOADCLSHRS";
							errString = getErrorString("shipment_id1",errCode,userId);
							break;
						}
						
						
					}
					
					}
				}





			}
			break;
		    case 2:


			System.out.println("DOM>>>> Elements>>["+genericUtility.serializeDom(dom).toString()+"]");
			System.out.println("DOM1>> Elements>>["+genericUtility.serializeDom(dom1).toString()+"]");
			System.out.println("DOM2>> Elements>>["+genericUtility.serializeDom(dom2).toString()+"]");	

			parentNodeList = dom2.getElementsByTagName("Detail2");
			parentNode = parentNodeList.item(0);
			System.out.println("parentNode >>>{"+parentNode+"}");
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();

		
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{

				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("value of child node : "+childNode);

				if(childNodeName.equalsIgnoreCase(""))
				{}


			}
			break;
		    case 3:

			parentNodeList = dom2.getElementsByTagName("Detail3");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();

			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{


				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("value of child node : "+childNode);

				if(childNodeName.equalsIgnoreCase(""))
				{}
				

			}
			break;

		}
	} catch (Exception e) {
		e.printStackTrace();			
		errString = e.getMessage();
		try {
			conn.rollback();				
		} catch (Exception d) {
			d.printStackTrace();
		}
		throw new ITMException(e);
	} finally {
		try {
			if (conn != null) {
				conn.close();
			}
			conn = null;
		} catch (Exception d) {
			d.printStackTrace();
		}
	}
	return errString;
}

public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	String valueXmlString = "";
	try {
		System.out.println("currentColumn"+currentColumn);
		System.out.println("editFlag"+editFlag);
		System.out.println("xtraParams"+xtraParams);


		System.out.println("xmlString111>>"+xmlString);
		System.out.println("xmlString222>>"+xmlString1);
		System.out.println("xmlString333>>"+xmlString2);
		dom = parseString(xmlString);
		dom1 = parseString(xmlString1);
		if (xmlString2.trim().length() > 0) {
			dom2 = parseString(xmlString2);
		}
		valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
	} catch (Exception e) {
		System.out.println("Exception : [itemChanged(String,String)] :==>\n" + e.getMessage());
		throw new ITMException(e);
	}
	return valueXmlString;
}

public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
{


	String siteCodeTo = "", siteCodeFr = "", fromTranDate = "", toTranDate = "", insertQuery = "", path = "", objName = "intercomp_reconcile";
	String sundryTypeFr = "", sundryTypeTo = "", sundryCodeFr = "", sundryCodeTo = "",siteCodeFrDescr="",siteCodeToDescr="";
	StringBuffer valueXmlString = null;
	int currentFormNo = 0, lineNo = 0;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Connection conn = null;
	PreparedStatement pstmt = null,pstmt1=null;
	ResultSet rs = null ,rs1 = null;
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ConnDriver connDriver = new ConnDriver();
	SimpleDateFormat simpleDateFormat = null;
	//GenericUtility genutility = new GenericUtility();
	String locCode="",lotNo="",lotsl="",itmdesc="",loginSite="",siteCode="",atranid="";
   
	System.out.println("DOM111 Elements>>["+genericUtility.serializeDom(dom).toString()+"]");
	System.out.println("DOM222 Elements>>["+genericUtility.serializeDom(dom1).toString()+"]");
	System.out.println("DOM322 Elements>>["+genericUtility.serializeDom(dom2).toString()+"]");
	String workOrder = "",itemCode = "",itemDescr="",sql="",unit="",packCode="",qcReqd="",locGrp="",tranType ="",confirmed="",createStab="",unitStd="",locCodeAprv="";
	double totDetQuan = 0.0,totNoArt=0.0,potencyPerc=0.0,convQtyStduom=0.0,operation=0.0,stdQty=0.0;
	java.sql.Timestamp currDate = null,mfgDateStart = null,startDate=null,expDate=null,tdate = null,datCompl=null,confDate=null;
	double quantity = 0,grossWeight = 0,tareWeight = 0,netWeight = 0,noOfArt=0,shippsize = 0;
	SimpleDateFormat sdf = null;
	String currAppdate = "",woSiteCode="",sStartDate="",sExpDate="",sMfgDateStart="",stdate="",sDatCompl="",sConfDate="";
	String tranid ="";
	double rate = 0,conquan,conOldRate,oldAmount,amount,amtdiff,oldRate=0,getquantity=0;
	int cnt = 0;
	TransIDGenerator generator = null;
	String autoTranid = "";
	String scrFlag = "";
	String xmlString = "";
	String todayDate = "";
	String noOfArts = "";
	String shipmentId = "",refID ="",tranCode ="", tranName = "";
	try
	{   

		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		conn.setAutoCommit(false);
		connDriver = null;
		simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userId");

		chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

		if (objContext != null && objContext.trim().length() > 0) {
			currentFormNo = Integer.parseInt(objContext);
		}
		System.out.println("FORM NO IS"+currentFormNo);
		valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
		valueXmlString.append(editFlag).append("</editFlag> </header>");
		loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		switch (currentFormNo) {
		
		case 1 :
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();

			break;
			
		case 2 : 
			parentNodeList = dom.getElementsByTagName("Detail2");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			System.out.println("DOM2 Elements["+genericUtility.serializeDom(dom2).toString()+"]");

			System.out.println("itm_default >>>>> 2>>");
			shipmentId = genericUtility.getColumnValue("shipment_id1", dom1);
		
			
			if(currentColumn.trim().equalsIgnoreCase("itm_default"))
			{
				
					
				sql = "SELECT ref_id,tran_code FROM ship_docs where shipment_id = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,shipmentId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					refID = rs.getString(1) == null ? "":rs.getString(1).trim();
					tranCode = rs.getString(2) == null ? "":rs.getString(2).trim();
				}
				pstmt.close();
				rs.close();
				pstmt = null;
				rs = null;
				if (tranCode != null && tranCode.trim().length() > 0 )
				{
					sql = "SELECT tran_name FROM transporter where tran_code = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,tranCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranName = rs.getString(1) == null ? "":rs.getString(1).trim();
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
				}
				else
				{
					tranName= "";
				}
				
				valueXmlString.append("<Detail2  domID='1' objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
				valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
				valueXmlString.append("<ref_id>").append("<![CDATA[" + refID + "]]>").append("</ref_id>");
				valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
				valueXmlString.append("<tran_name>").append("<![CDATA[" + tranName + "]]>").append("</tran_name>");
				valueXmlString.append("</Detail2>");

			 }
		
				
			break;
		    

		}
		
		valueXmlString.append("</Root>"); 
	}
	catch(Exception e) 
	{
		e.printStackTrace();
		System.out.println("Exception ::"+ e.getMessage()); 
		try {
			conn.rollback();				
		} catch (Exception d) {
			d.printStackTrace();
		}
		throw new ITMException(e); 
	}
	finally 
	{
		try
		{
			if(conn != null)
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
				conn.close(); 
				conn = null;
			}
			
		}
		catch(Exception d)
		{
			d.printStackTrace(); 
		}
	}
	return valueXmlString.toString();

}

}
