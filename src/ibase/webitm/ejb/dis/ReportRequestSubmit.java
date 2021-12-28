/********************************************************
Title 	 : ReportRequestSubmit 
ReqId	 : [D15FSUN005]
Date  	 : 01/OCT/15
Developer: Pankaj R.
********************************************************/
package ibase.webitm.ejb.dis;

import java.util.*;
import java.io.File;
import java.sql.*;


import org.w3c.dom.*;

import java.text.SimpleDateFormat;

import javax.naming.InitialContext;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import ibase.webitm.ejb.*;

import ibase.webitm.ejb.dis.InvAllocTraceBean;
import ibase.webitm.ejb.dis.StockAllocationPrc;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;

public class ReportRequestSubmit 
{
	E12GenericUtility e12genericUtility = new E12GenericUtility();
	private String user_lang ="en"; 
	private String user_country = "US";
	public String preSaveForm(String xmlString1 , String siteCode,String entityType, String entityCode, String chgUser , String chgTerm )throws ITMException
	{
		String retStr="";
		Document dom = null;
		System.out.println("Changes Done NOWW ReportRequestSubmit EJB called.>>>>>>>");
		try
		{
			System.out.println("xmlString1>>>>>>>>>["+xmlString1+"]");
			System.out.println("Site Code>>>>>"+siteCode);
			System.out.println("entity Type>>>>>"+entityType);
			System.out.println("entityCode>>>>>"+entityCode);

			System.out.println("Inside bean chgUser>>>>>>>>>["+chgUser+"]::chgTerm["+chgTerm+"]");

			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
//				dom = GenericUtility.getInstance().parseString(xmlString1);
				dom = e12genericUtility.parseString(xmlString1);
				retStr=executepreSaveForm(dom,siteCode,entityType,entityCode,chgUser,chgTerm,xmlString1);
			}			
		}
		catch(Exception e)
		{
			System.out.println("Exception :ReportRequestSubmit ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}

	public String executepreSaveForm(Document dom , String siteCode,String entityType,String entityCode, String chgUser , String chgTerm, String xmlString1)throws ITMException
	{
		PreparedStatement pstmt = null;
		String sql = "";
		ResultSet rs = null;
		String label="",name="",value="",type="";
		String retString="",errString="";
		String argumentString="",reportName="",suppName="";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String winName="",winNameTemp="",siteDescr="",shDescr="",tranID="",detail="",remarks="";
		int parentNodeListLength=0;
		int detField1=0;
		Document dom1 = null;

		Connection conn = null;
		ConnDriver connDriver = new ConnDriver(); 
		java.util.Date currentDate = new java.util.Date();
		java.sql.Date date = new java.sql.Date(currentDate.getTime());
		Timestamp sysDate = null;
		try
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			SimpleDateFormat sdf = new SimpleDateFormat(e12genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println(" Now the date is>>>>>>> " + sysDateStr);
			sysDate = Timestamp.valueOf(e12genericUtility.getValidDateString(sysDateStr, e12genericUtility.getApplDateFormat(),
					e12genericUtility.getDBDateFormat())+ " 00:00:00.0");
			
			System.out.println("TRAN DATE>>>>> :-"+sysDate);
			
			parentNodeList = dom.getElementsByTagName("Detail2"); 
            parentNodeListLength = parentNodeList.getLength(); 
            argumentString= "<Arguments>";
            for (int lineCount = 0; lineCount < parentNodeListLength; lineCount++) 
            {
                parentNode = parentNodeList.item(lineCount); 
                childNodeList = parentNode.getChildNodes(); 
                for (int detField = 0; detField < childNodeList.getLength(); detField++) 
                { 
                    childNode = childNodeList.item(detField); 
                    if ( childNode != null && childNode.getFirstChild() != null &&   
                         childNode.getNodeName().equalsIgnoreCase("label_1_t") )  
                    { 
                    	label = (childNode.getFirstChild().getNodeValue().trim()); 
                        System.out.println(">>>label:"+label); 
                         
                    }
                    else if ( childNode != null && childNode.getFirstChild() != null &&   
                            childNode.getNodeName().equalsIgnoreCase("label_1") )  
                    { 
                       	name = (childNode.getFirstChild().getNodeValue().trim()); 
                           System.out.println(">>>name:"+name); 
                            
                    }
                    else if ( childNode != null && childNode.getFirstChild() != null &&   
                            childNode.getNodeName().equalsIgnoreCase("value_1") )  
                    { 
                       	value = (childNode.getFirstChild().getNodeValue().trim()); 
                           System.out.println(">>>value:"+value); 
                            
                    } 
                    else if ( childNode != null && childNode.getFirstChild() != null &&   
                            childNode.getNodeName().equalsIgnoreCase("type") )  
                    { 
                    	type = (childNode.getFirstChild().getNodeValue().trim()); 
                        System.out.println(">>>type:"+type); 
                        String[] arrType = type.split("\\.");
                        System.out.println("Arr Length>"+arrType.length);
                        type = arrType[2];
                        System.out.println("type>>>"+type);
                    }
                    else if ( childNode != null && childNode.getFirstChild() != null &&   
                            childNode.getNodeName().equalsIgnoreCase("win_name") )  
                    { 
                       	winNameTemp = (childNode.getFirstChild().getNodeValue().trim()); 
                           System.out.println(">>>winName:"+winName); 
                    }
                } 
                argumentString += "<Argument id=\"" +  name + "\">";
				argumentString += "<name>" + name +"</name>"; 
				argumentString += "<argType>" + type +"</argType>"; 
				argumentString += "<value>" + value +"</value>";		
				argumentString += "</Argument>" ;//end detail field for loop 
            }//end detail line no for loop
            argumentString += "</Arguments>";
			System.out.println("argumentString>>>>>> : "+argumentString);
			winName=winNameTemp.substring(2);
			winName="w_"+winName;
			reportName=winNameTemp;
			System.out.println("WinName>>>>>"+winName);
			System.out.println("WinNameTemp>>>>>"+winNameTemp);
			System.out.println("reportName>>>>>"+reportName);
			
			sql = "select sh_descr from gencodes where fld_name='REPORT_NAME' and descr= ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, winName);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				shDescr = rs.getString("sh_descr");
				
				System.out.println("shDescr>>>>>"+shDescr);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select descr from site where site_code = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteDescr = rs.getString("descr");
				
				System.out.println("siteDescr>>>>>"+siteDescr);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = "select supp_name from supplier where supp_code = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, entityCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				suppName = rs.getString("supp_name");
				
				System.out.println("suppName>>>>>"+suppName);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			StringBuffer xmlBuff = null;
			String xmlString = "";
			xmlBuff = new StringBuffer();
					
			xmlBuff.append("<?xml version='1.0' encoding='UTF-8'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			
			xmlBuff.append("<objName><![CDATA[").append("report_req").append("]]></objName>");  
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			
			xmlBuff.append("<description>").append("Header0 members").append("</description>");
			xmlBuff.append("<Detail1 objContext=\"1\" objName=\"report_req\" domID=\"\" dbID=\"\">");
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			      
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_date><![CDATA["+ sdf.format(sysDate) +"]]></tran_date>");	
			xmlBuff.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
			xmlBuff.append("<site_descr><![CDATA["+ siteDescr +"]]></site_descr>");
			xmlBuff.append("<win_name><![CDATA["+ reportName +"]]></win_name>");
			xmlBuff.append("<report_descr><![CDATA["+ shDescr +"]]></report_descr>");
			xmlBuff.append("<arg_xml><![CDATA["+ argumentString +"]]></arg_xml>");
			xmlBuff.append("<user_id><![CDATA["+ chgUser +"]]></user_id>");
			xmlBuff.append("<entity_type><![CDATA["+ entityType +"]]></entity_type>");
			xmlBuff.append("<entity_code><![CDATA["+ entityCode +"]]></entity_code>");
			xmlBuff.append("<supp_name><![CDATA["+ suppName +"]]></supp_name>");
			xmlBuff.append("<remarks><![CDATA["+ remarks +"]]></remarks>");
			xmlBuff.append("<wf_status><![CDATA["+ "S" +"]]></wf_status>");
//			xmlBuff.append("<status_date><![CDATA["+ sdf.format(sysDate) +"]]></status_date>");
//			xmlBuff.append("<emp_code__aprv><![CDATA["+userId+"]]></emp_code__aprv>");
			xmlBuff.append("<add_date><![CDATA["+ sdf.format(sysDate) +"]]></add_date>");
			xmlBuff.append("<add_user><![CDATA["+ chgUser +"]]></add_user>");
			xmlBuff.append("<add_term><![CDATA["+ chgTerm +"]]></add_term>");
			xmlBuff.append("<chg_date><![CDATA["+ sdf.format(sysDate) +"]]></chg_date>");
			xmlBuff.append("<chg_user><![CDATA["+chgUser+"]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA["+chgTerm+"]]></chg_term>");
			xmlBuff.append("</Detail1>");
			
			if(xmlBuff!=null)
			{
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("xmlString>>>>>>>>>>>>:"+xmlBuff.toString());
			}
			System.out.println("just before savdata()?>>>>>>>>>");
            if(xmlString != null && xmlString.trim().length()>0)
            {
            	//Changes and Commented By Ajay on 08-01-2018:START
                String userId = e12genericUtility.getValueFromXTRA_PARAMS(xmlString1, "loginCode");  
                System.out.println("--login code--"+userId);
            	//retString = saveData(siteCode,xmlString,conn);
            	retString = saveData(siteCode,xmlString,userId,conn);
            	//Changes and Commented By Ajay on 08-01-2018:END
            	System.out.println("retString after saveData()>>>>>"+retString);
            	
            	//Added on 20/OCT/15 for tran_id
            	if(retString!=null && retString.trim().length()>0)
            	{
            		dom1 = e12genericUtility.parseString(retString);
            		System.out.println("dom1>>>>>: "+dom1);
            	}
            	parentNodeList = dom1.getElementsByTagName("Root"); 
                parentNodeListLength = parentNodeList.getLength(); 
                for (int lineCount = 0; lineCount < parentNodeListLength; lineCount++) 
                {
	                parentNode = parentNodeList.item(lineCount);
	                childNodeList = parentNode.getChildNodes(); 
	                for (detField1 = 0; detField1 < childNodeList.getLength(); detField1++) 
	                { 
	                    childNode = childNodeList.item(detField1); 
	                    if ( childNode != null && childNode.getFirstChild() != null &&   
	                            childNode.getNodeName().equalsIgnoreCase("Detail") )  
	                    { 
	                       	detail = (childNode.getFirstChild().getNodeValue().trim()); 
	                           System.out.println(">>>Detail : "+detail); 
	                    }
	                    else if ( childNode != null && childNode.getFirstChild() != null &&   
	                            childNode.getNodeName().equalsIgnoreCase("TranID") )  
	                    { 
	                    	tranID = (childNode.getFirstChild().getNodeValue().trim()); 
	                           System.out.println(">>>TranID : "+tranID); 
	                    }
	                } 
	                retString=""+detail+":"+tranID+"";
	                System.out.println("After Parsing Return String>>>>"+retString);
	            }
            	
            }
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ReportRequestSubmit :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in ReportRequestSubmit  :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			try
			{

				if (errString == null || errString.trim().length() == 0)
				{
					System.out.println("@@@@@@commit errString :::"+ errString );
					conn.commit();
					//return errString;
				}

				if (pstmt!= null)
				{
					pstmt = null;
				}
				if( conn != null )
				{
					conn.close();
					conn=null;
				}
			}
			catch(Exception e)
			{
				System.out.println("The SQLException occurs in ReportRequestSubmit  :"+e);			
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; 
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			String [] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			retString = masterStateful.processRequest(authencate, siteCode, true ,xmlString, true, conn);
		}
		catch(ITMException itme)
		{
			throw itme;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	public String getReportName( String reportName,String search ,String dbID) throws ITMException
	{
		System.out.println(">>>>>Inside getReportName>>>>>");
		String reportNameData = "";
		//ReportRequestWizRemote reportReqWizRemote = null;
		ReportRequestWiz reportReqWizRemote = null;
		try
		{
			//InitialContext ctx = new InitialContext( new AppConnectParm().getProperty() );
			reportReqWizRemote = new ReportRequestWiz();
			reportNameData = reportReqWizRemote.getReportName(reportName, search, dbID);
			reportReqWizRemote = null;
			System.out.println("reportNameData String=="+reportNameData);
			
			String xslFileName = getXSLFileName( "report_req_name_set_wiz_" + this.user_lang + "_" + this.user_country + ".xsl" );
			System.out.println("Inside Submit xslFileName >>>>"+xslFileName);
			reportNameData = (e12genericUtility).transformToString( xslFileName, reportNameData, CommonConstants.APPLICATION_CONTEXT + File.separator + "temp", "Output", ".html" );
			System.out.println("reportNameData= After(transformToString) ==>"+reportNameData);
		}
		catch ( Exception e  )
		{
			throw new ITMException(e);
		}
		finally
		{
			if ( reportReqWizRemote != null )
			{
				reportReqWizRemote = null;
			}						
		}
		return reportNameData;
	}
	private String getXSLFileName( String xslFileName )throws ITMException
	{   
		String retFileName = null;
		try
		{
			String defaultPath = null;
			if( CommonConstants.APPLICATION_CONTEXT != null )
			{
				defaultPath = CommonConstants.APPLICATION_CONTEXT + CommonConstants.ITM_CONTEXT + File.separator;
			}
			else
			{
				defaultPath = ".." + File.separator + "webapps" + File.separator + "ibase" + File.separator + CommonConstants.ITM_CONTEXT + File.separator;
			}
			File xslPath = new File( defaultPath + File.separator  + "xsl" + File.separator + CommonConstants.THEME + File.separator + "WIZARD");
			if ( !xslPath.exists() )
			{
				xslPath.mkdir();
			}
			System.out.println( " xslPath [" + xslPath +"] xslFileName ["+xslFileName +"]");
			File xslFile = new File(xslPath , xslFileName);
			if( xslFile.exists() )
			{
				retFileName = xslFile.getAbsolutePath();
			}
			else
			{
				throw new ITMException( new Exception( retFileName + " Wizard XSL file Not Found") );	
			}
		}
		catch (Exception e)
		{
			throw new ITMException(e);
		}
		return retFileName;
	}
}
