package ibase.webitm.ejb.dis;
 import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class StatementRcdDateIC extends ValidatorEJB implements StatementRcdDateICLocal , StatementRcdDateICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();	
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("XmlString :::::::::: "+xmlString);
		System.out.println("XmlString1 :::::::::: "+xmlString1);
		System.out.println("XmlString2 :::::::::: "+xmlString2);
		try 
		{
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				dom = genericUtility.parseString(xmlString);
				System.out.println("Dom ::::::: "+dom);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = genericUtility.parseString(xmlString1);
				System.out.println("Dom1 ::::::: "+dom1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = genericUtility.parseString(xmlString2);
				System.out.println("Dom2 ::::::: "+dom2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,xtraParams);
		}
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (errString);
	}
	
	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
	String childNodeName = null , itemSer = "" , terrName = "" , prdCode = "" ,
			 stmtRcdStr = "" ,  city = "" , countryCode = "" , loginSiteCode = "" , subSql = "";
	String errString = "";
	String errCode = "";
	String userId = "";
	String sql = "";
	String errorType = "";
	int cnt = 0 , ctr = 0;
	int childNodeListLength = 0;
	int parentNodeListLength = 0;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	ArrayList<String> errList = new ArrayList<String>();
	ArrayList<String> errFields = new ArrayList<String>();
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	//ConnDriver connDriver = null;
	StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
	int currentFormNo = 0;
	java.sql.Date dtToday = null , stmtRecdDate = null , sysdate = null;
	boolean val = false ;
	try 
	{
		System.out.println("@@@@@@@@ wfvaldata called");
		System.out.println("Current DOM [" + genericUtility.serializeDom(dom) + "]");
		System.out.println("Header DOM [" + genericUtility.serializeDom(dom1) + "]");
		System.out.println("Dom All [" + genericUtility.serializeDom(dom2) + "]");
		//connDriver = new ConnDriver();
		//conn = connDriver.getConnectDB("DriverITM");
		conn = getConnection();
		userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		
		if (objContext != null && objContext.trim().length() > 0) 
		{
			currentFormNo = Integer.parseInt(objContext);
		}
		switch (currentFormNo) 
		{
		case 1:
			NodeList parentList = dom.getElementsByTagName("Detail1");
			NodeList childList = null;
			System.out.println("Dom..." + dom.toString());
			childList = parentList.item(0).getChildNodes();
			int noOfChilds = childList.getLength();
			for (ctr = 0; ctr < noOfChilds; ctr++) 
			{
				childNode = childList.item(ctr);
				if (childNode.getNodeType() != 1) 
				{
					continue;
				}
				childNodeName = childNode.getNodeName();
			if ("prd_code".equalsIgnoreCase(childNodeName))
				{
					prdCode = genericUtility.getColumnValue("prd_code", dom);
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					city =  genericUtility.getColumnValue("city", dom);
					terrName = checkNull(genericUtility.getColumnValue("territory_name", dom));
					System.out.println("Period Code :::::::: "+prdCode+ " item series :::::::: "+itemSer);
					if(prdCode == null || prdCode.trim().length()==0)
					{
						errList.add("VTPRDBLK"); 
						errFields.add(childNodeName.toLowerCase());
					}
					if(itemSer == null || itemSer.trim().length()==0)
					{
						errList.add("VTTBLNULL");//Invalid-Division can not be blank 
						errFields.add(childNodeName.toLowerCase());
					}
					else if((itemSer != null && itemSer.trim().length() > 0) && (prdCode != null && prdCode.trim().length() > 0))
					{
						sql = "SELECT COUNT(*) AS COUNT FROM ITEMSER WHERE ITEM_SER = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt("COUNT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Count: " + cnt);
						if (cnt == 0) 
						{
							errList.add("VTINVDIV");//Invalid-Division Does not exist
							errFields.add(childNodeName.toLowerCase());
						}
						
						sql= "SELECT COUNT_CODE FROM STATE WHERE " +
								"STATE_CODE IN (SELECT STATE_CODE FROM SITE WHERE SITE_CODE=?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, loginSiteCode );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							countryCode = checkNull(rs.getString("count_code")).trim();
							System.out.println("countryCode >>> :"+countryCode);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "SELECT COUNT(*) FROM PERIOD_APPL A,PERIOD_TBL B " +
								"WHERE A.REF_CODE=A.PRD_TBLNO AND A.PRD_TBLNO=B.PRD_TBLNO " +
								" AND B.PRD_CODE = ? " +
								"AND B.PRD_TBLNO=? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,prdCode.trim());
						pstmt.setString(2,countryCode+"_"+itemSer.trim());
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0)
						{
							System.out.println("Error :Period not exist in period_tbl master ");
							errCode = "VMINVPRDTB";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if(city == null || city.trim().length() == 0)
						{
							subSql = "";
						}
						else
						{
							System.out.println("city :::: "+city);
							subSql = "AND UPPER(D.CITY) LIKE '%" +city+ "%'";
						}
						System.out.println("terrName ::::::::: "+terrName);
						sql = "SELECT COUNT(*) " +
								" FROM CUST_STOCK A , ORG_STRUCTURE B , HIERARCHY C , CUSTOMER D WHERE A.POS_CODE = B.POS_CODE AND" +
								" A.ITEM_SER = B.TABLE_NO AND B.VERSION_ID = C.VERSION_ID AND B.TABLE_NO=C.TABLE_NO AND A.CUST_CODE=D.CUST_CODE " +
								" AND B.VERSION_ID IN(SELECT FN_GET_VERSION_ID FROM DUAL ) AND" +
								" A.PRD_CODE = ?  AND A.ITEM_SER = ?  AND UPPER(C.LEVEL_DESCR) LIKE '%"+terrName+"%' AND" +
								" B.POOL_CODE = C.LEVEL_CODE AND A.POS_CODE IS NOT NULL "+subSql ; 
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, prdCode);
						pstmt.setString(2, itemSer);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0)
						{
							System.out.println("Record does not exist ");
							errCode = "VTNULLREC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}					
				else if("territory_name".equalsIgnoreCase(childNodeName))
				{
					terrName = genericUtility.getColumnValue("territory_name", dom);
					if(terrName == null || terrName.trim().length() == 0)
					{
						errList.add("VTNULLTERR");
						errFields.add(childNodeName.toLowerCase());
					}
				}
			}
			
			break;
		case 2:
			parentList = dom2.getElementsByTagName("Detail2");
			parentNodeListLength = parentList.getLength();
			System.out.println("parentNodeListLength :::::::::"+ parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++) {
				
				System.out.println("selectted row:::" + selectedRow);
				parentNode = parentList.item(selectedRow);
				
				if (parentNode != null) 
				{
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
				}
				System.out.println("@@@@@@@@@@@@childNodeListLength["+ childNodeListLength + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName====" + childNodeName);
					
					if ("stmt_recd_date".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null) 
					{
						//stmtRcdStr = genericUtility.getColumnValue("stmt_recd_date", dom2);
						stmtRcdStr = checkNull(childNode.getFirstChild().getNodeValue());
						if(stmtRcdStr == null || stmtRcdStr.trim().length() == 0 )
						{
							errCode = "VTSTMTRCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							val=isValidDate(stmtRcdStr);
							if(!val)
							{
								errString = itmDBAccessEJB.getErrorString("","VTERRDATE","","",conn); 
								return errString;
							}
							else
							{
								stmtRecdDate = new java.sql.Date(sdf.parse(stmtRcdStr).getTime());
								sql="SELECT SYSDATE FROM DUAL";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									sysdate = rs.getDate(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(stmtRecdDate.after(sysdate))
								{
									errCode = "VTSTMTINV";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
							}
						}
						System.out.println("stmtRcdStr from detail==== "+ stmtRcdStr);
						}
					}
				
				 // end for
			}// end of selected row
			break; // case 2 end// case 1 end
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
	} catch (Exception e) {
		e.printStackTrace();
		errString = e.getMessage();
		throw new ITMException(e);
	} finally {
		try {
			if (conn != null) {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				conn.close();
			}
			conn = null;
		} catch (Exception d) {
			System.out.println("Exception :"+this.getClass().getSimpleName()+":wfValData :==>\n" + d.getMessage());
			
		}
	}
	errString = errStringXml.toString();
	return errString;
}
	
	
	
	private String errorType(Connection conn , String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{                        
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);                        
			pstmt.setString(1,errorCode);                        
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}                        
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
		return msgType;
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
			System.out.println("Exception : [StatementRcdDateIC] :==>\n"+ e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}
	
	
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
	int currentFormNo = 0;
	String sql = "" , itemSer = "" , prdCode = "" , custCode = "" , custName = "" , posCode = "" , empCode = "" , tranId = "" , terrName = "" , city = "" ,
			empName = "" , positionDescr = "" , frdatStr = ""  , todatStr = "" , stmtdatStr = "" ,  loginSiteCode = "" , subSql = "" , stmtRecdDateStr = "";
	StringBuffer valueXmlString = new StringBuffer();
	PreparedStatement pstmt = null , pstmt1 = null;
	ResultSet rs = null , rs1 = null;
	Connection conn = null;
	Timestamp frData = null , toData =  null , stmtDate = null , stmtRecdDate = null;
	int cnt=0;
	
	System.out.println("inside itemchange::::::");
	try 
	{
		if (objContext != null && objContext.trim().length() > 0) 
		{
			currentFormNo = Integer.parseInt(objContext.trim());
		}
		loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		System.out.println("loginSiteCode :::::::: "+loginSiteCode);
		System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo+ "**************");
		Date dtToday = Calendar.getInstance().getTime();
		String strToday = genericUtility.getValidDateString(dtToday,genericUtility.getApplDateFormat());
		
		//ConnDriver con = new ConnDriver();
		//conn = con.getConnectDB("DriverITM");
		conn = getConnection();
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
		valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");
		switch (currentFormNo) 
		{
		case 1:
			
			if ("itm_default".equalsIgnoreCase(currentColumn)) 
			{
				System.out.println("inside itm_default case 1 : ");
				valueXmlString.append("<Detail1 domID='1' objContext='1'>\r\n");
				valueXmlString.append("<attribute updateFlag='N' selected='N' />\r\n");
				valueXmlString.append("<prd_code>").append("<![CDATA[]]>").append("</prd_code>\r\n");
				valueXmlString.append("<item_ser>").append("<![CDATA[]]>").append("</item_ser>\r\n");
				valueXmlString.append("<territory_name>").append("<![CDATA[]]>").append("</territory_name>\r\n");
				valueXmlString.append("<city>").append("<![CDATA[]]>").append("</city>\r\n");
				valueXmlString.append("</Detail1>\r\n");
			}
			break;
			
		case 2:
			if ("itm_default".equalsIgnoreCase(currentColumn)) 
			{
				System.out.println("inside item_default");
				itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
				prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom1));
				terrName = checkNull(genericUtility.getColumnValue("territory_name", dom1));
				city = genericUtility.getColumnValue("city", dom1);
				if(city == null || city.trim().length() == 0)
				{
					subSql = "";
				}
				else
				{
					System.out.println("city :::: "+city);
					subSql = "AND UPPER(D.CITY) LIKE '%" +city+ "%'";
				}
				System.out.println("itemSer ::::::::"+itemSer + " prdCode :::::::: " +prdCode);
				
					sql = "SELECT A.PRD_CODE , A.ITEM_SER , C.LEVEL_DESCR , D.CITY , A.CUST_CODE , D.CUST_NAME , A.POS_CODE , A.FROM_DATE , A.TO_DATE , A.STMT_DATE , A.STMT_RECD_DATE , A.EMP_CODE , A.TRAN_ID " +
							" FROM CUST_STOCK A , ORG_STRUCTURE B , HIERARCHY C , CUSTOMER D WHERE A.POS_CODE = B.POS_CODE AND" +
							" A.ITEM_SER = B.TABLE_NO AND B.VERSION_ID = C.VERSION_ID AND B.TABLE_NO=C.TABLE_NO AND A.CUST_CODE=D.CUST_CODE " +
							" AND B.VERSION_ID IN(SELECT FN_GET_VERSION_ID FROM DUAL ) AND" +
							" A.PRD_CODE = ?  AND A.ITEM_SER = ?  AND UPPER(C.LEVEL_DESCR) LIKE '%"+terrName+"%' AND" +
							" B.POOL_CODE = C.LEVEL_CODE AND A.POS_CODE IS NOT NULL "+subSql ; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, prdCode);
					pstmt.setString(2, itemSer);
					//pstmt.setString(3, terrName);
					//pstmt.setString(4, city);
					rs = pstmt.executeQuery();
				
				   while(rs.next()) 
				   {
					prdCode = checkNull(rs.getString("PRD_CODE"));
					itemSer = checkNull(rs.getString("ITEM_SER"));
					custCode = checkNull(rs.getString("CUST_CODE"));
					frData = rs.getTimestamp("FROM_DATE");
					toData = rs.getTimestamp("TO_DATE");
					stmtDate = rs.getTimestamp("STMT_DATE");
					stmtRecdDate = rs.getTimestamp("STMT_RECD_DATE");
					posCode = rs.getString("POS_CODE");
					empCode = rs.getString("EMP_CODE");
					tranId = checkNull(rs.getString("TRAN_ID"));
					terrName = checkNull(rs.getString("LEVEL_DESCR"));
					city = checkNull(rs.getString("CITY"));
					custName = checkNull(rs.getString("CUST_NAME"));
					
					cnt++;
					
					System.out.println("cust_code::::::::"+custCode);
					System.out.println("frData:::::::::::::"+frData);
					System.out.println("toData::::::::"+toData);
					System.out.println("stmtDate::::::::"+stmtDate);
					System.out.println("Date stmtRcdDate::::::::"+strToday);
					System.out.println("empCode::::::::::"+empCode);
					System.out.println("tranId::::::::::"+tranId);
					System.out.println("City::::::::::"+city);
					
					if( posCode != null && posCode.length() > 0 )
					{
					sql = "SELECT FN_GET_POSCODE_DESCR(?) AS DESCR FROM DUAL ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, rs.getString("POS_CODE"));
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						positionDescr = checkNull(rs1.getString("descr"));
						System.out.println("descr :::"+positionDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					}
					
					if( empCode != null && empCode.length() > 0 )
					{
						sql = "SELECT EMP_FNAME||' '||EMP_MNAME||' '||EMP_LNAME AS NAME FROM EMPLOYEE WHERE EMP_CODE=? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, empCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							empName = checkNull(rs1.getString("name"));
							System.out.println("empName  :" + empName);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					
					if( frData != null ){
						frdatStr = checkNull(genericUtility.getValidDateString(frData.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));	
					}
					if( toData != null )
					{
						todatStr = checkNull(genericUtility.getValidDateString(toData.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));	
					}
					if( stmtDate != null )
					{
					stmtdatStr = checkNull(genericUtility.getValidDateString(stmtDate.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
					}
					
					
					//valueXmlString.append("<Detail2 domID='"+cnt+"'>\r\n");
					valueXmlString.append("<Detail2 domID='" + cnt+ "' objContext=\"2\" selected=\"N\">\r\n");
					valueXmlString.append("<attribute updateFlag='N' selected='N' />\r\n");
					valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>\r\n");
					valueXmlString.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>\r\n");
					valueXmlString.append("<prd_code>").append("<![CDATA["+prdCode+"]]>").append("</prd_code>\r\n");
					valueXmlString.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>\r\n");
					valueXmlString.append("<pos_code>").append("<![CDATA["+posCode+"]]>").append("</pos_code>\r\n");
					valueXmlString.append("<position_descr>").append("<![CDATA["+positionDescr+"]]>").append("</position_descr>\r\n");
					valueXmlString.append("<territory_name>").append("<![CDATA["+terrName+"]]>").append("</territory_name>\r\n");
					valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>\r\n");
					valueXmlString.append("<from_date>").append("<![CDATA["+frdatStr+"]]>").append("</from_date>\r\n");
					valueXmlString.append("<to_date>").append("<![CDATA["+todatStr+"]]>").append("</to_date>\r\n");
					valueXmlString.append("<stmt_date>").append("<![CDATA["+stmtdatStr+"]]>").append("</stmt_date>\r\n");
					if(stmtRecdDate == null)
					{
					valueXmlString.append("<stmt_recd_date>").append("<![CDATA["+strToday+"]]>").append("</stmt_recd_date>\r\n");
					}
					else
					{
						stmtRecdDateStr = checkNull(genericUtility.getValidDateString(stmtRecdDate.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
						valueXmlString.append("<stmt_recd_date>").append("<![CDATA["+stmtRecdDateStr+"]]>").append("</stmt_recd_date>\r\n");
					}
					valueXmlString.append("<emp_code>").append("<![CDATA["+empCode+"]]>").append("</emp_code>\r\n");
					valueXmlString.append("<emp_name>").append("<![CDATA["+empName+"]]>").append("</emp_name>\r\n");
					valueXmlString.append("<tran_id>").append("<![CDATA["+tranId+"]]>").append("</tran_id>\r\n");
					valueXmlString.append("</Detail2>\r\n");
					
				}
				}
			
			break;
		}
		valueXmlString.append("</Root>");
	}
		catch (Exception e) {
		e.printStackTrace();
		System.out.println("Exception ::" + e.getMessage());
		throw new ITMException(e);
	} finally {
		try {
			if (conn != null) {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;

				}
				conn.close();
			}
			conn = null;
		} catch (Exception d) {
			d.printStackTrace();
		}
	}
	return valueXmlString.toString();
}

	public boolean isValidDate(String dateString) throws ITMException, Exception {
	    try {
	    	
	    	SimpleDateFormat df = new SimpleDateFormat(genericUtility.getApplDateFormat());
	    	df.setLenient(false);
	        df.parse(dateString);
	        return true;
	    } catch (ParseException e) {
	        return false;
	    }
	}
	
	
}
