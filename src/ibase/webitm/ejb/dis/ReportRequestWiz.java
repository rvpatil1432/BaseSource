/********************************************************
Title 	 : ReportRequestWiz
ReqId 	 : [D15FSUN005]
Date  	 : 21/SEP/15
Developer: Pankaj R.
********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.reports.utility.XSDParser;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import javax.ejb.Stateless; 
import java.sql.Timestamp;
@Stateless 

public class ReportRequestWiz extends ValidatorEJB implements ReportRequestWizRemote,ReportRequestWizLocal
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	//method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("wfValdata() called for ReportRequestWiz>>>>>");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("@@@@@@@@@@@@@@@xmlString["+xmlString+"]@@@@@@@@@@@@@@@@@@");
			System.out.println("@@@@@@@@@@@@@@@xmlString1["+xmlString1+"]@@@@@@@@@@@@@@@@@@");
			System.out.println("@@@@@@@@@@@@@@@xmlString2["+xmlString2+"]@@@@@@@@@@@@@@@@@@");
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String reportName="",winName="";	
		int ctr=0;
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		String sql="";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println(">><<<<<<<<<<<<<<editFlag"+editFlag);
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
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
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					
					if(childNodeName.equalsIgnoreCase("report_name"))
					{
						reportName=genericUtility.getColumnValue("report_name", dom);
						System.out.println("Report Name>>>>>"+reportName);
						if(reportName == null || reportName.trim().length() == 0)
						{
							errCode = "VTREPNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							System.out.println("Inside wfValData report!null");
							reportName=reportName.substring(2);
							sql="select count(*) from gencodes where fld_name='REPORT_NAME' and mod_name='W_REPORT_REQUEST_WIZ' and descr like ? order by descr ";
							System.out.println("sql:::"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, "%"+reportName+"%");
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VTINVREP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				}
						
			}//end switch
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		}//end try
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
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
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	} // end of wfValData
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("itemChanged() called for ReportRequestWiz");
		String valueXmlString = "";
		try
		{   
			System.out.println("xmlString:::"+xmlString);
			System.out.println("xmlString1:::"+xmlString1);
			System.out.println("xmlString2:::"+xmlString2);

			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ReportRequestWiz][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return valueXmlString;
	}
	//start of itemchange
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0 , currentFormNo = 0,childNodeListLength=0 ;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat simpleDateFormat = null;
		
		String xsdName="",xsdNameTemp="",sql="",descr="";
		String loginSiteCode="";
		
		SimpleDateFormat sdf = null;
		Timestamp timestamp = null;
		String currDate="";
		String argValueFinal="w_",argValueTemp="";
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());

			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"); 
			
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());
			currDate = (sdf.format(timestamp).toString()).trim();
			System.out.println("Current Date>>>>>>>>>"+currDate);

			switch(currentFormNo)
			{
				case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				if(currentColumn.trim().equals("itm_default"))
				{
					System.out.println("In Case(1) Itemchange()>>>>");
					xsdName=genericUtility.getColumnValue("report_name", dom);
					System.out.println("XSD File Name>>>"+xsdName);
				}

				valueXmlString.append("</Detail1>");
				break;
			case 2 : 
				parentNodeList = dom1.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					xsdName=genericUtility.getColumnValue("report_name", dom1);
					System.out.println("XSD File Name>>>"+xsdName);
					XSDParser xsdParser = new XSDParser(xsdName);
					String argString=xsdParser.getArgumentMetaDataString();
					 
					System.out.println(">>>argStringBuff.toString():"+argString.toString());
					Document domfinal = parseString(argString.toString());
					NodeList argNodeList = domfinal.getElementsByTagName("argument");
					int noOfArgs = argNodeList.getLength();
					
					xsdNameTemp=xsdName.substring(2);
					xsdNameTemp="w_"+xsdNameTemp;
					System.out.println("Final XSD(Report Win Name>>>) : "+xsdNameTemp);
					
					sql="select sh_descr from gencodes where fld_name='REPORT_NAME' and descr= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, xsdNameTemp);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr= checkNull(rs.getString("sh_descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					for(ctr = 0; ctr < noOfArgs; ctr++)
					{
						Node curArgNode = argNodeList.item(ctr);
						String argLabel=curArgNode.getAttributes().getNamedItem("label").getNodeValue();
						String argName=curArgNode.getAttributes().getNamedItem("name").getNodeValue();
						String argType=curArgNode.getAttributes().getNamedItem("type").getNodeValue();
						String argValue=curArgNode.getAttributes().getNamedItem("value").getNodeValue();
						String argVisible=curArgNode.getAttributes().getNamedItem("visible").getNodeValue();
						String argEditable=curArgNode.getAttributes().getNamedItem("editable").getNodeValue();
						String argSize=curArgNode.getAttributes().getNamedItem("size").getNodeValue();
//						String argReportTitle=curArgNode.getAttributes().getNamedItem("reportTitle").getNodeValue();
						String argDisplayType=curArgNode.getAttributes().getNamedItem("displayType").getNodeValue();
						String argrowNo=curArgNode.getAttributes().getNamedItem("displayType").getNodeValue();
						String argcolNo=curArgNode.getAttributes().getNamedItem("displayType").getNodeValue();
						if(argName.equalsIgnoreCase("design_source"))
						{
							argVisible="Y";
							argValue=removeLastChar(argValue);
						}
						if("login_site".equalsIgnoreCase(argValue) || "Login_site".equalsIgnoreCase(argValue) || "login_Site".equalsIgnoreCase(argValue))
						{
							argValue = loginSiteCode;	
						}
						if("java.util.Date".equalsIgnoreCase(argType))
						{
							argValue = currDate;
						}
						if("Y".equalsIgnoreCase(argVisible))
						{
							valueXmlString.append("<Detail2 dbID=\"\" domID= '"+ (ctr + 1) +"'>");
							valueXmlString.append("<label_1_t>").append("<![CDATA["+argLabel+"]]>").append("</label_1_t>");
							valueXmlString.append("<label_1>").append("<![CDATA["+argName+"]]>").append("</label_1>");
							valueXmlString.append("<value_1>").append("<![CDATA["+argValue+"]]>").append("</value_1>");
							valueXmlString.append("<editable>").append("<![CDATA["+argEditable+"]]>").append("</editable>");
							valueXmlString.append("<type>").append("<![CDATA["+argType+"]]>").append("</type>");
							valueXmlString.append("<win_name>").append("<![CDATA["+xsdName+"]]>").append("</win_name>");
							valueXmlString.append("<descr>").append("<![CDATA["+descr+"]]>").append("</descr>");
							valueXmlString.append("<visible>").append("<![CDATA["+argVisible+"]]>").append("</visible>");
							valueXmlString.append("<size>").append("<![CDATA["+argSize+"]]>").append("</size>");
							valueXmlString.append("<displayType>").append("<![CDATA["+argDisplayType+"]]>").append("</displayType>");
							valueXmlString.append("<rowNo>").append("<![CDATA["+argrowNo+"]]>").append("</rowNo>");
							valueXmlString.append("<colNo>").append("<![CDATA["+argcolNo+"]]>").append("</colNo>");
							valueXmlString.append("</Detail2>");
							}
						}
						System.out.println("xml string::::"+valueXmlString);
					}
				break;
			}
			valueXmlString.append("</Root>"); 
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
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
				}
				conn = null;
			}
			catch(Exception d)
			{
				d.printStackTrace(); 
			}
		}
		return valueXmlString.toString();
	}
	public String getReportName(String reportName, String search, String dbID) throws ITMException 
	{
		System.out.println("reportName >>>>>>>>>>>["+reportName+"]");
		System.out.println("search >>>>>>>>>>>>>>>["+search+"]");
		System.out.println("dbID >>>>>>>>>>>>>>>>>["+dbID+"]");
		String selectSql = "",descr="",shDescr="",reportNameTemp="d_";
		StringBuffer valueXmlString = new StringBuffer("<Root>\r\n");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{	
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if(reportName == null || reportName.trim().length() == 0)
			{	
				selectSql = "select descr,sh_descr from gencodes where fld_name='REPORT_NAME' and mod_name='W_REPORT_REQUEST_WIZ' order by descr";
				pstmt = conn.prepareStatement(selectSql);
				rs = pstmt.executeQuery();
				int num = 1;
				while (rs.next())
				{
					descr= checkNull(rs.getString("descr"));
					shDescr=checkNull(rs.getString("sh_descr"));
					
					descr=descr.substring(2);
					reportName=reportNameTemp+descr;
					
					valueXmlString.append("<Report domID='" + num	+ "' selected = 'N'>\r\n");					
					valueXmlString.append("<report_name>").append("<![CDATA[" + reportName + "]]>").append("</report_name>\r\n");
					valueXmlString.append("<report_descr protect = '1'>").append("<![CDATA[" + shDescr + "]]>").append("</report_descr>\r\n");
					valueXmlString.append("</Report>\r\n");
					num++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			
			}
			else
			{
				/*selectSql = "select descr,sh_descr from gencodes where fld_name='REPORT_NAME' and mod_name='W_REPORT_REQUEST_WIZ' and descr like ? order by descr ";
				pstmt = conn.prepareStatement(selectSql);
				pstmt.setString(1, "%"+reportName+"%");*/
				selectSql = "select descr,sh_descr from gencodes where fld_name='REPORT_NAME' and mod_name='W_REPORT_REQUEST_WIZ' order by descr";
				pstmt = conn.prepareStatement(selectSql);
				rs = pstmt.executeQuery();
				int num = 1;
				while (rs.next())
				{
					descr= checkNull(rs.getString("descr"));
					shDescr=checkNull(rs.getString("sh_descr"));
					
					descr=descr.substring(2);
					reportName=reportNameTemp+descr;
					
					valueXmlString.append("<Report domID='" + num	+ "' selected = 'N'>\r\n");					
					valueXmlString.append("<report_name>").append("<![CDATA[" + reportName + "]]>").append("</report_name>\r\n");
					valueXmlString.append("<report_descr protect = '1'>").append("<![CDATA[" + shDescr + "]]>").append("</report_descr>\r\n");
					valueXmlString.append("</Report>\r\n");
					num++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception :ReportRequestWiz :getReportName(String,String):"	+ e.getMessage() + ":");
			valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		} 
		finally 
		{
			try 
			{
				if (conn != null && !conn.isClosed()) 
				{					
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("Exception :ReportRequestWiz :getReportName(String,String) :==>\n"+ e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("\n****ValueXmlString ::" + valueXmlString.toString()	+ ":********");
		return valueXmlString.toString();
	}
	//end of itemchange
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
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
	}//end of errorType
	private String checkNull(String input)
	{
		if(input == null) 
		{
			input = "";
		}
		return input; 
	}
	private static String removeLastChar(String str) 
    {
        return str.substring(0,str.length()-6);
    }
	
	private String getObjName(Node node) throws Exception
	{
		String objName = "";
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem("objName").getNodeValue();
		System.out.println(" Object Name is-->" + objName);
		return objName;
	}
}
