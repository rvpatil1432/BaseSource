package ibase.webitm.ejb.dis.adv;

//import java.awt.geom.Arc2D.Double;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import ibase.ejb.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.reports.utility.ArgumentHandler;
import ibase.webitm.reports.utility.JasperReportGenerator;
import ibase.webitm.reports.utility.ResourceConstants;
import ibase.webitm.reports.utility.XSDParser;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

public class ReportRequestConf 
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	String errString="";
	//confirm() method added for call the sendMailReport() method   
	public String confirm(String tranId, String empCodeAprv, String xmlDataAll, String keyFlag)
			throws RemoteException, ITMException 
	{
		System.out.println(">>>>> ReportRequestConf confirm called <<<<<");
		System.out.println(">>> ReportRequestConf Parameters tranId:"+tranId);
		System.out.println(">>> ReportRequestConf Parameters empCodeAprv:"+ empCodeAprv);
		System.out.println(">>>>ReportRequestConf confirm keyFlag:"+ keyFlag);
		System.out.println(">>>>ReportRequestConf confirm xmlDataAll:"+ xmlDataAll);
		
		String loginCode = "";
		String siteCode = "";
		String loginEmpCode = "";
		String activeYn="";
		String xtraParams="";
		String retString ="";
		
		int updCnt=0;
		
		boolean isError= false;
		Timestamp currDate = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		ConnDriver connDriver = new ConnDriver();
		try 
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			connDriver = null;
			conn.setAutoCommit(false);
			SimpleDateFormat sdfDB = new SimpleDateFormat(genericUtility.getDBDateFormat());
			currDate =  java.sql.Timestamp.valueOf(sdfDB.format(new java.util.Date()).toString() + " 00:00:00.0");
			System.out.println(">>>>>>currDate:"+currDate);
			if(empCodeAprv!=null && empCodeAprv.trim().length() > 0)
			{
				empCodeAprv= empCodeAprv.trim();
			}
			sql = " select code from users where emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCodeAprv);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				loginCode = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			loginCode = loginCode == null ? "" : loginCode.trim();
			
			sql = " select site_code from report_req where tran_id= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				siteCode = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			xtraParams = "loginCode=" + loginCode + "~~" + "loginSiteCode=" + siteCode + "~~" + "loginEmpCode=" + empCodeAprv;
			System.out.println(">>>xtraParams:"+xtraParams);
			
			if(keyFlag!= null && "REQCONFIRM".equals(keyFlag))
			{
				//Check this condition for keyFlag, If Request is approved then confirmation will sent and WF_STATUS will update as 'C' (Closed)
				ReportRequestConf reportObj=new ReportRequestConf();
				System.out.println(">>>Before Calling ReportRequestConf sendMailReport:"+tranId);
				retString= reportObj.sendMailReport(tranId, xtraParams, conn);
				//retString="REPORTSUCC";
				System.out.println(">>>After Calling ReportRequestConf sendMailReport retString:"+retString);
				if(retString.indexOf("REPORTSUCC") > -1) 
				{
					
					//sql = " update pob_hdr set confirmed = 'Y', conf_date = ?, " +
					//		"emp_code__aprv = ?, wf_status='C', status_date= ? where tran_id = ? ";
					System.out.println(">>>In ReportRequestConf Transaction Successful");
					System.out.println(">>>Before update wf_status as Closed<<<");
					sql= "update report_req set wf_status='C', status_date= ?, emp_code__aprv = ? where tran_id= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setTimestamp(1, currDate);
					pstmt.setString(2, empCodeAprv);
					pstmt.setString(3, tranId);
					updCnt= pstmt.executeUpdate();
					pstmt.close();
					pstmt= null;
					System.out.println(">>>After REPORTSUCC report_req update updCnt:"+ updCnt);
					if(updCnt > 0)
					{
						System.out.println(">>>In ReportRequestConf Transaction Rejected Successfuly");
						retString = "Y";
					}
					else
					{
						isError=true;
					}
				}
				else
				{
					isError=true;
				}
			}
			else if(keyFlag!= null && "REQREJECT".equals(keyFlag))
			{
				//Check this condition for keyFlag, If Request is rejected then Rejection mail will sent and WF_STATUS will update as 'R' (Rejected)
				if(tranId!= null && tranId.trim().length() > 0)
				{
					System.out.println(">>>Before update wf_status as Reject<<<");
					sql= "update report_req set wf_status='R',status_date= ?,emp_code__aprv = ? where tran_id= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setTimestamp(1, currDate);
					pstmt.setString(2, empCodeAprv);
					pstmt.setString(3, tranId);
					updCnt= pstmt.executeUpdate();
					pstmt.close();
					pstmt= null;
					System.out.println(">>>After reject update report_req updCnt:"+updCnt);
					if(updCnt > 0)
					{
						System.out.println(">>>In ReportRequestConf Transaction Rejected Successfuly");
						retString = "Y";
					}
					else
					{
						isError=true;
					}
				}
			}
		} 
		catch (Exception e)
		{
			try 
			{
				isError= true;
				conn.rollback();
			} 
			catch (SQLException e1) 
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
			System.out.println(">>>Exeption occured");

			throw new ITMException(e);
		}
		finally 
		{
			try
			{
				System.out.println(">>In finally ReportRequestConf Check isError:"+isError);
				if(!isError)
				{
					conn.commit();
					System.out.println(">>>>ReportRequestConf Connection commit successfuly");
				}
				else
				{
					conn.rollback();
					System.out.println(">>>>ReportRequestConf Connection rollback successfuly");
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
					System.out.println(">>>Close connection Successfuly");
				}
			}
			catch(Exception e)
			{
				System.out.println("In finally Exception :"+e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println(">>>ReportRequestConf confirm final retString:"+retString);
		return retString;
	}

	/*public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		System.out.println("Inside Confirm Method");
		Connection conn = null;
		ConnDriver connDriver = null;
		String empCodeAprv="" ;
		String errString = ""; 
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			connDriver = new ConnDriver();
			conn = connDriver.getConnectDB("DriverITM");
			connDriver = null;
			conn.setAutoCommit(false);
			empCodeAprv = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("empCodeAprv@@@@@@@"+empCodeAprv);
				
			errString= sendMailReport(tranId,xtraParams,conn);
			System.out.println(">>>>In ReportRequestConf after sendMailReport() errString:"+errString);
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			errString = GenericUtility.getInstance().createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}*/
	
	public String sendMailReport(String tranId,String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("Inside sendMailReport Method");
		Document dom = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int ctr=0;
		
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
//		String siteCode="";
//		ConnDriver connDriver = null;
		String loginEmpCode = "";
		String empCodeAprv="" ;
		ValidatorEJB validatorEJB = null;
		DistCommon distCommom = null; 
		ITMDBAccessEJB itmDBAccessEJB = null;
		String retString="";
		String xmlString="",reportName="d_",reportArgs="",reportArgsFinal="",reportType="",formatCode="";
		
		String winName="",siteCode="",argXml="",name="",type="",value="", entityCode="";
		String objName="",colName="",colValue="",xmlTag="",xmlTagValue="";
		int childNodeListLength;
		empCodeAprv = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
		System.out.println("empCodeAprv@@@@@@@"+empCodeAprv);
//		DistCommon distCommom = null;
		try
		{
			System.out.println(">>>>In sendMailReport method>>>");
			
			//sql = "select win_name,site_code,arg_xml from report_req where tran_id= ? ";
			sql = "select win_name,site_code,arg_xml,entity_code from report_req where tran_id= ? "; //added by sagar
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				winName = rs.getString("win_name");
				siteCode = rs.getString("site_code");
				argXml = rs.getString("arg_xml");
				entityCode = rs.getString("entity_code");
				
				System.out.println(">>winName["+winName+"]");
				System.out.println(">>siteCode["+siteCode+"]");
				System.out.println(">>argXml["+argXml+"]");
				System.out.println(">>entityCode["+entityCode+"]");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			objName=winName.substring(2);
			reportName=reportName+objName;
			
			System.out.println("objName>>>"+objName);
			
			if (argXml != null && argXml.trim().length() > 0)
			{
				dom = genericUtility.parseString(argXml);
				System.out.println("Dom>>"+dom);
			}
			StringBuffer xmlBuff = null;
			xmlBuff = new StringBuffer();
					
			xmlBuff.append("<?xml version='1.0' encoding='UTF-8'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append(objName).append("]]></objName>");  
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
			xmlBuff.append("<Detail1 objContext=\"1\" objName=\""+objName+"\" domID=\"\" dbID=\"\">");
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			
			parentNodeList = dom.getElementsByTagName("Argument");
			System.out.println("parentNodeList>>"+parentNodeList);
            int parentNodeListLength = parentNodeList.getLength(); 
            System.out.println("parentNodeListLength>>"+parentNodeListLength);
            for (int lineCount = 0; lineCount < parentNodeListLength; lineCount++) 
            {
            	System.out.println("Inside main for loop>>>"+lineCount);
                parentNode = parentNodeList.item(lineCount); 
                childNodeList = parentNode.getChildNodes(); 
                
                for (int detField = 0; detField < childNodeList.getLength(); detField++) 
                { 
                	System.out.println("Inside 2nd for loop$$$$$$$"+detField);
                    childNode = childNodeList.item(detField); 
                    if ( childNode != null && childNode.getFirstChild() != null &&   
                         childNode.getNodeName().equalsIgnoreCase("name") )  
                    {
                    	
                    	name = (childNode.getFirstChild().getNodeValue().trim()); 
                        System.out.println(">>>colName:"+name); 
                         
                    }
                    else if ( childNode != null && childNode.getFirstChild() != null &&   
                            childNode.getNodeName().equalsIgnoreCase("argType") )  
                    {
                    	
                       	type = (childNode.getFirstChild().getNodeValue().trim()); 
                        System.out.println(">>>String type:"+type); 
                           
                            
                    }
                    else if ( childNode != null && childNode.getFirstChild() != null &&   
                            childNode.getNodeName().equalsIgnoreCase("value") )  
                    {
                    	value = (childNode.getFirstChild().getNodeValue().trim()); 
                        System.out.println(">>>String value:"+value); 
                            
                    }
                    
                } 
                reportArgs += ""+name+"."+type+":"+name+",";
                System.out.println("report Args>>>"+reportArgs);
                xmlTag=name;
                xmlTagValue=value;
                System.out.println("xmlTag>>>"+xmlTag);
                System.out.println("xmlTagValue>>>"+xmlTagValue);
                xmlBuff.append("<"+xmlTag+"><![CDATA["+ xmlTagValue +"]]></"+xmlTag+">");
            }
            xmlBuff.append("<supp_code><![CDATA["+ entityCode +"]]></supp_code>"); //added by sagar
            xmlBuff.append("<tran_id_req><![CDATA["+ tranId +"]]></tran_id_req>");
            xmlBuff.append("</Detail1>");
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString=xmlBuff.toString();
			
			System.out.println(">>>>>Final xmlString :"+xmlString);
			reportArgsFinal=removeLastChar(reportArgs);
            System.out.println(">>>reportArgsFinal : "+reportArgsFinal);
			reportType="PDF";
			String setPassword="Y";
			//formatCode="DRCPSH";
			formatCode="REQMAIL";
			
			//added by sagar

			System.out.println(">>reportName>>>"+reportName);
			System.out.println(">>reportArgsFinal>>>"+reportArgsFinal);
			System.out.println(">>reportType>>>"+reportType);
			System.out.println(">>formatCode>>>"+formatCode);
			System.out.println(">>XtraParam>>>"+xtraParams);
			
//
			E12ReportComp e12ReportComp=new E12ReportComp();
			retString= e12ReportComp.sendReport(xmlString, reportName, reportArgsFinal, reportType, formatCode, xtraParams, setPassword);
			System.out.println(">>>retString from E12ReportComp sendReport():"+retString);
		}
		catch(Exception e)
		{
			retString = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
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
				//conn.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return retString;
	}
	private String getValueFromXTRA_PARAMS(String xtraParams, String string) {
	// TODO Auto-generated method stub
		return null;
	}
	private static String removeLastChar(String str) 
    {
        return str.substring(0,str.length()-1);
    }
	
}//end of class
