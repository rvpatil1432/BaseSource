package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class SalesConsolidateIC extends ValidatorEJB implements SalesConsolidateICLocal , SalesConsolidateICRemote{

	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("In wfValData");
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = "";
		try 
		{
			System.out.println("currFrmXmlStr..." + currFrmXmlStr);
			System.out.println("hdrFrmXmlStr..." + hdrFrmXmlStr);
			System.out.println("allFrmXmlStr..." + allFrmXmlStr);
			if ((currFrmXmlStr != null) && (currFrmXmlStr.trim().length() != 0)) 
			{
				currDom = parseString(currFrmXmlStr);
			}
			if ((hdrFrmXmlStr != null) && (hdrFrmXmlStr.trim().length() != 0)) 
			{
				hdrDom = parseString(hdrFrmXmlStr);
			}
			if ((allFrmXmlStr != null) && (allFrmXmlStr.trim().length() != 0)) 
			{
				allDom = parseString(allFrmXmlStr);
			}
			errString = wfValData(currDom, hdrDom, allDom, objContext, editFlag, xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : [SalesConsolidateIC][wfValData(String currFrmXmlStr)] : ==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return errString;
	}
	
	public String wfValData(Document currDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int count = 0;
		String errString = "" , loginSiteCode = "" , userId ="" ,  errCode = "" , errorType = "" ;
		String  custCode = "" , itemCode = "" , unit = "" , posCode = "" , versionId = "" , terrCode = "" , empCode = ""   ;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String childNodeName = "";
		String sql = "";
		int noOfChilds = 0;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		int currentFormNo = 0;
		int cnt = 0;
		ConnDriver connDriver = null;
		Node childNode = null;
		String itemSer="" , prdCode = "" ;
		System.out.println("Current DOM [" + genericUtility.serializeDom(currDom) + "]");
		System.out.println("Header DOM [" + genericUtility.serializeDom(hdrDom) + "]");
		System.out.println("Dom All [" + genericUtility.serializeDom(allDom) + "]");
		try {
			System.out.println("************xtraParams*************" + xtraParams);
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			System.out.println("In wfValData Secondary Sales Consolidate:::");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
			System.out.println("**************loginCode************" + userId);
			
			if ((objContext != null) && (objContext.trim().length() > 0)) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			NodeList parentList = currDom.getElementsByTagName("Detail1");
			NodeList childList = null;
			System.out.println("hdrDom..." + hdrDom.toString());
			switch (currentFormNo)
			{
			case 1:
			{
				childList = parentList.item(0).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++) 
				{
					childNode = childList.item(ctr);
					if (childNode.getNodeType() != 1) 
					{
						continue;
					}
					childNodeName = childNode.getNodeName();
					System.out.println("Editflag =" + editFlag);
					System.out.println("parentList = " + parentList);
					System.out.println("childList = " + childList);
					
					if("cust_code".equalsIgnoreCase(childNodeName))
					{
						custCode = genericUtility.getColumnValue("cust_code", currDom);
						System.out.println("CustCode :::::: "+custCode);
						if(custCode == null || custCode.trim().length() == 0)
						{
							errList.add("VTNULLCUST");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM CUSTOMER WHERE CUST_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTERRCUST");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if ("prd_code".equalsIgnoreCase(childNodeName))
					{
						prdCode = genericUtility.getColumnValue("prd_code", currDom);
						System.out.println("Period Code :::::::: "+prdCode);
						if(prdCode==null || prdCode.trim().length()==0)
						{
							errList.add("VMNULLPRD"); 
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM PERIOD WHERE CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, prdCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTERRPRD");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("emp_code".equalsIgnoreCase(childNodeName))
					{
						empCode = genericUtility.getColumnValue("emp_code", currDom);
						System.out.println("empCode :::::: "+empCode);
						if(empCode == null || empCode.trim().length() == 0)
						{
							errList.add("VTEMPNUL");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTERREMP");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("item_ser".equalsIgnoreCase(childNodeName))
					{
						itemSer = genericUtility.getColumnValue("item_ser", currDom);
						System.out.println("itemSer ::::::: "+itemSer);
						if(itemSer == null || itemSer.trim().length()==0 )
						{
							errList.add("VMNULLSER"); 
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql = "SELECT COUNT(*) AS COUNT FROM ITEMSER WHERE ITEM_SER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTINVDIV");
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}
					else if("version_id".equalsIgnoreCase(childNodeName))
					{
						versionId = checkNull(genericUtility.getColumnValue("version_id", currDom));
						posCode = genericUtility.getColumnValue("pos_code", currDom);
						itemSer = genericUtility.getColumnValue("item_ser", currDom);
						System.out.println("versionId :::::: "+versionId);
						if(versionId == null || versionId.length() == 0 )
						{
							errList.add("VTNULVERID");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM VERSION WHERE VERSION_ID = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, versionId);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTERRVERID");
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								System.out.println("posCode :::::: "+posCode);
								System.out.println("itemSer :::::: "+itemSer);
								
								if(posCode != null  && itemSer != null)
								{
								sql = "SELECT COUNT(*) AS COUNT FROM ORG_STRUCTURE WHERE VERSION_ID = ? AND POS_CODE = ? AND TABLE_NO = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, versionId);
								pstmt.setString(2, posCode);
								pstmt.setString(3, itemSer);
								rs = pstmt.executeQuery();
									if (rs.next()) 
									{
									count = rs.getInt("COUNT");
									}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Count: " + count);
									if (count == 0) 
									{
									errList.add("VTNULLRCD");
									errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}
					
					else if("terr_code".equalsIgnoreCase(childNodeName))
					{
						terrCode = checkNull(genericUtility.getColumnValue("terr_code", currDom));
						versionId = checkNull(genericUtility.getColumnValue("version_id", currDom));
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
						System.out.println("terrCode>>"+terrCode+">>versionId>>"+versionId+">>itemSer"+itemSer);
						System.out.println("terrCode :::::: "+terrCode);
						if(terrCode == null || terrCode.trim().length() == 0)
						{
							errList.add("VTNULTERR");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM HIERARCHY WHERE LEVEL_CODE = ? AND VERSION_ID=? AND TABLE_NO=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, terrCode);
							pstmt.setString(2, versionId);
							pstmt.setString(3, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTNERRTERR");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					else if("item_code".equalsIgnoreCase(childNodeName))
					{
						itemCode = genericUtility.getColumnValue("item_code", currDom);
						System.out.println("itemCode :::::: "+itemCode);
						if(itemCode == null || itemCode.trim().length() == 0)
						{
							errList.add("VTNULLITCD");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM ITEM WHERE ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTERRITCD");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					else if("unit".equalsIgnoreCase(childNodeName))
					{
						unit = genericUtility.getColumnValue("unit", currDom);
						System.out.println("unit :::::: "+unit);
						if(unit == null || unit.trim().length() == 0)
						{
							errList.add("VTUNTNUL");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM ITEM WHERE UNIT = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unit);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count: " + count);
							if (count == 0) 
							{
								errList.add("VTUNTINV");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
			}
			break;
			
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = "";
			if ((errList != null) && (errListSize > 0)) 
			{
				for (cnt = 0; cnt < errListSize; cnt++) 
				{
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........." + errStringXml);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) 
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
				errStringXml = new StringBuffer("");
			}
			errString = errStringXml.toString();
		}
		catch (Exception e) 
		{
			System.out.println("Exception in "+this.getClass().getSimpleName()+"  == >");
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
				if ((conn != null) && (!conn.isClosed()))
					conn.close();
			}
			catch (Exception e) 
			{
				System.out.println("Exception :"+this.getClass().getSimpleName()+":wfValData :==>\n" + e.getMessage());
				throw new ITMException(e);
			}
		}
		return errString;
	}
	
	private String errorType(Connection conn, String errorCode) throws ITMException 
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next())
				msgType = rs.getString("MSG_TYPE");
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
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
			catch (Exception e) 
			{
				e.printStackTrace();
			}
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
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		return msgType;
	}
	
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("XmlString :::::::::: "+xmlString);
		System.out.println("XmlString1 :::::::::: "+xmlString1);
		System.out.println("XmlString2 :::::::::: "+xmlString2);
		try 
		{
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				dom = parseString(xmlString);
				System.out.println("Dom ::::::: "+dom);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("Dom1 ::::::: "+dom1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("Dom2 ::::::: "+dom2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [SalesConsolidate] :==>\n"+ e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}
	
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException
	{

int currentFormNo = 0;
StringBuffer valueXmlString = null;
String chgDate = "" , chgUser = "" , chgTerm = "";
NodeList parentNodeList = null;
NodeList childNodeList = null;
Node parentNode = null;
Node childNode = null;
String childNodeName = null,  columnValue = "";
int ctr = 0, childNodeListLength = 0 ; 
String  empCode = "" , tranDate = "" , unit="", custName = "" ,  empName="" , custCode = "" , itemCode = "" , terrCode="" , terrDescr="" , itemDescr = "" , sql = "";
String versionId="",itemSer="";
Timestamp sysDate = null ;
ResultSet rs = null;
PreparedStatement pstmt = null;
Connection conn = null;
ConnDriver connDriver = null;

try {
	Calendar currentDate = Calendar.getInstance();
	SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
	String sysDateStr = sdf1.format(currentDate.getTime());
	sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
	+ " 00:00:00.0");
	System.out.println("Current DOM [" + genericUtility.serializeDom(dom) + "]");
	System.out.println("Header DOM [" + genericUtility.serializeDom(dom1) + "]");
	System.out.println("Dom All [" + genericUtility.serializeDom(dom2) + "]");
	System.out.println("CURRENT COLUMN:::::" + currentColumn);
	SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
	java.util.Date currDate = new java.util.Date();
	chgDate = sdf.format(currDate);
	System.out.println("chgDate...[" + chgDate + "");
	chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
	chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
	tranDate = sdf.format(currDate);
	if (objContext != null && objContext.trim().length() > 0) {
		currentFormNo = Integer.parseInt(objContext);
	}
	currentColumn = checkNull(currentColumn).trim();
	connDriver = new ConnDriver();
	conn = connDriver.getConnectDB("Driver");

	valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
	valueXmlString.append(editFlag).append("</editFlag></header>");
	switch (currentFormNo) {

	
	case 1:
	{
		System.out.println("Inside Case 1 Of Itemchange");
		parentNodeList = dom.getElementsByTagName("Detail1");
		parentNode = parentNodeList.item(0);
		childNodeList = parentNode.getChildNodes();
		ctr = 0;
		valueXmlString.append("<Detail1>");
		childNodeListLength = childNodeList.getLength();

		do {
			childNode = childNodeList.item(ctr);
			childNodeName = childNode.getNodeName();

			if (childNodeName.equals(currentColumn)) {
				if (childNode.getFirstChild() != null) {
					columnValue = childNode.getFirstChild().getNodeValue().trim();
				}
			}
			ctr++;
		} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
		System.out.println("current form::::::::::::" + currentFormNo);

		
		if ( "itm_default".equalsIgnoreCase(currentColumn)) 
		{
					valueXmlString.append("<chg_date>").append("<![CDATA[" + chgDate + "]]>").append("</chg_date>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
					valueXmlString.append("<tran_date>").append("<![CDATA[" + tranDate + "]]>").append("</tran_date>");
		}
		else if("emp_code".equalsIgnoreCase(currentColumn))
		{
			empCode = checkNull(genericUtility.getColumnValue("emp_code", dom));
			System.out.println("empCode ::::: "+empCode);
			if( empCode.length() > 0 )
			{
				sql = " SELECT EMP_FNAME||' '||EMP_MNAME||' '||EMP_LNAME AS EMP_NAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, empCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					empName = checkNull(rs.getString("EMP_NAME"));
					System.out.println("empName  :" +empName );
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<emp_name>").append("<![CDATA[" + empName + "]]>").append("</emp_name>");
			}
			else
			{
					valueXmlString.append("<emp_name>").append("<![CDATA[]]>").append("</emp_name>");
			}
		}
		else if ("cust_code".equalsIgnoreCase(currentColumn))
		{
			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
			System.out.println("custCode ::::: "+custCode);
			if( custCode.length() > 0 )
			{
				sql = " SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					custName = checkNull(rs.getString("CUST_NAME"));
					System.out.println("custName  :" +custName );
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");
			}
			else
			{
				valueXmlString.append("<cust_name>").append("<![CDATA[]]>").append("</cust_name>");
			}
		}
		else if ("item_code".equalsIgnoreCase(currentColumn))
		{
			itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
			System.out.println("itemCode ::::: "+itemCode);
			if( itemCode.length() > 0 )
			{
				sql = " SELECT DESCR,UNIT FROM ITEM WHERE ITEM_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					itemDescr = checkNull(rs.getString("DESCR"));
					unit = checkNull(rs.getString("UNIT"));
					System.out.println("itemDescr  :" +itemDescr );
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr + "]]>").append("</item_descr>");
				valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
			}
			else
			{
				valueXmlString.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>");
				valueXmlString.append("<unit>").append("<![CDATA[]]>").append("</unit>");
			}
		}
		else if("terr_code".equalsIgnoreCase(currentColumn))
		{
			terrCode = checkNull(genericUtility.getColumnValue("terr_code", dom));
			versionId = checkNull(genericUtility.getColumnValue("version_id", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			System.out.println("terrCode>>"+terrCode+">>versionId>>"+versionId+">>itemSer"+itemSer);
			if(terrCode.length()>0){
			sql = "SELECT LEVEL_DESCR FROM HIERARCHY WHERE LEVEL_CODE =? AND VERSION_ID=? AND TABLE_NO=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, terrCode);
			pstmt.setString(2, versionId);
			pstmt.setString(3, itemSer);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				terrDescr = checkNull(rs.getString("LEVEL_DESCR"));
			}
			System.out.println("terrCode ::: "+terrCode+ " terrDescr :::: "+terrDescr);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			valueXmlString.append("<terr_descr>").append("<![CDATA["+terrDescr+"]]>").append("</terr_descr>");
			}
			else
			{
				valueXmlString.append("<terr_descr>").append("<![CDATA[]]>").append("</terr_descr>");
			}
		}
	}
	valueXmlString.append("</Detail1>");
	break;
	}
	
	
				
} catch (Exception e) {
	try {
		e.printStackTrace();
		throw new ITMException(e);
	} catch (Exception ex) {
		ex.printStackTrace();
		throw new ITMException(ex);
	}
} finally {
	try {

		if (conn != null) {
			conn.close();
			conn = null;
		}
		if(rs != null )
		{
			rs.close();
			rs = null;
		}
		if (pstmt != null) {
			pstmt.close();
			pstmt = null;

		}
	} catch (Exception e) {
		e.printStackTrace();
		throw new ITMException(e);
	}
}
valueXmlString.append("</Root>\r\n");
return valueXmlString.toString();
	}
}
