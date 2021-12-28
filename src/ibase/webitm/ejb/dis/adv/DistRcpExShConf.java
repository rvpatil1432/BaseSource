/********************************************************
	Title 	 : DistRcpExShConf
	Date  	 : 13/MAR/15
	Developer: Pankaj R.
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

//import java.awt.geom.Arc2D.Double;
import java.io.File;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.TreeMap;

import ibase.ejb.*;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless;

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


@Stateless

public class DistRcpExShConf extends ActionHandlerEJB implements DistRcpExShConfLocal,DistRcpExShConfRemote //SessionBean
{
	private String objName;
	public String confirm(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("Inside Confirm Method");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "",sql1="";
		String siteCode="";
		ConnDriver connDriver = null;
		String loginEmpCode = "";
		String confirm = "",empCodeAprv="" ;
		String errString = ""; 
		int updateCount=0;
		double shortageAmtHdr=0.0,shortAmtVal=0.0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ValidatorEJB validatorEJB = null;
		DistCommon distCommom = null; 
		ITMDBAccessEJB itmDBAccessEJB = null;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			empCodeAprv = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("empCodeAprv@@@@@@@"+empCodeAprv);
			
			sql = "select confirmed,site_code,shortage_amt from distrcp_exsh_hdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirm = rs.getString("confirmed");
				siteCode = rs.getString("site_code");
				shortageAmtHdr=rs.getDouble("shortage_amt");
			}
			System.out.println("Cconfirm@@@@@@@@"+confirm);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(confirm != null && confirm.equalsIgnoreCase("Y"))
			{
				System.out.println("The Selected transaction is already confirmed");
				errString = itmDBAccessEJB.getErrorString("","VTINVSUB2","","",conn);
				return errString;
			}
			else /*(confirm != null  && confirm.equalsIgnoreCase("N") */
			{
				sql = "update distrcp_exsh_hdr set confirmed = 'Y', conf_date = ?,emp_code__aprv = ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
				pstmt.setString(2, empCodeAprv);
				pstmt.setString(3, tranId);
				updateCount = pstmt.executeUpdate();
				System.out.println("no of row update: "+updateCount);
				System.out.println("Date@@@@@@@@@"+new java.sql.Date(new java.util.Date().getTime()));
				System.out.println("empCodeAprv+empCodeAprv"+empCodeAprv);
				System.out.println("tranId@@@@@@@@@"+tranId);
				pstmt.close();
				pstmt = null;
								
				/*if(updateCoount > 0)// comment added by sagar on 18/05/15 
				{
					errString = itmDBAccessEJB.getErrorString("","VTSTATSUBM ","","",conn);
				}
				*/
				
				if(updateCount > 0) //code added by sagar on 13/05/15
				{
					System.out.println(">>In Submit If update successfully then send intimation mail with report>>>");
					errString= sendMailReport(tranId,siteCode,shortageAmtHdr,xtraParams,conn);
					System.out.println(">>>>In DistRcpExShConf after sendMailReport() errString:"+errString);
				}	
				System.out.println(">>>>>>>Check errString:"+errString);
				if((errString != null) &&  errString.indexOf("REPORTSUCC") > -1)
				{
					errString = itmDBAccessEJB.getErrorString("","VTSTATSUBM","","",conn);
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTFAILSUBM","","",conn);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println(">>>>>In finally errString:"+errString);
				if(errString != null && errString.trim().length() > 0)
				{
					if(errString.indexOf("VTSTATSUBM") > -1)
					{
						conn.commit();
						
					}
					else
					{
						conn.rollback();
					}
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
				conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	
	}//end of confirm method
	
 
	//method added by sagar on 19/05/15 
	public String sendMailReport(String tranId, String siteCode,double shortageAmtHdr,String xtraParams,Connection conn) throws ITMException
	{
		String retString="";
		String xmlString="",reportName="",reportArgs="",reportType="",formatCode="",shortAmtValStr="";
		double shortAmtVal=0.0;
		DistCommon distCommom = null;
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			distCommom = new DistCommon(); 
			System.out.println(">>>>In sendMailReport method>>>");
			//E12ReportComp reportComp= new E12ReportComp();
			reportName="d_distrcp_exc_shrt";
			reportArgs="tran_id.String:tran_id,user_id.String:as_user_id,site_code.String:as_site";
			reportType="PDF";
			//formatCode="DOM_REG";
			shortAmtValStr = checkNull(distCommom.getDisparams("999999", "SHORT_AMT_VAL", conn));
			System.out.println(">>>>>In mail tranId:"+tranId);
			System.out.println(">>>>>In mail siteCode:"+siteCode);
			System.out.println(">>>>>In mail shortageAmtHdr:"+shortageAmtHdr);
			System.out.println(">>>>>In mail shortAmtValStr:"+shortAmtValStr);
			if(shortAmtValStr!=null && shortAmtValStr.trim().length() > 0 && !shortAmtValStr.equals("NULLFOUND") )
			{
			     shortAmtVal = Double.parseDouble(shortAmtValStr);
			     System.out.println(">>>>>>>>Disparm shortAmtVal:"+shortAmtVal);
			}
			if(shortageAmtHdr > shortAmtVal)
			{
				formatCode="DRCPSH_1"; // send mail for 'To' and 'Cc'.
				System.out.println(">>>shortAmtVal is greater than disparm value then formatCode is:"+formatCode);
			}
			else
			{
				formatCode="DRCPSH";// send mail only for 'To'.
				System.out.println(">>>shortAmtVal is less than disparm value then formatCode is:"+formatCode);
			}
			System.out.println(">>>>>>>>formatCode:"+formatCode);
			StringBuffer xmlBuff = null;
			xmlBuff = new StringBuffer();
					
			xmlBuff.append("<?xml version='1.0' encoding='UTF-8'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("distrcp_exsh").append("]]></objName>");  
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
			xmlBuff.append("<Detail1 objContext=\"1\" objName=\"distrcp_exsh\" domID=\"\" dbID=\"\">");
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id><![CDATA["+ tranId +"]]></tran_id>");
			xmlBuff.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
			xmlBuff.append("</Detail1>");
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			
			xmlString = xmlBuff.toString();
			System.out.println(">>>>>>>>>>xmlString for sendReport:"+xmlString);
			retString= this.sendReport(tranId, xmlString, reportName, reportArgs, reportType, formatCode, xtraParams, conn);
			System.out.println(">>>retString from sendReport:"+retString);
		}
		catch(Exception e)
		{
			retString = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	private String checkNull(String input)
	{
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
	
	public String sendReport(String tranId, String xmlString, String reportName, String reportArgs, String reportType, String formatCode, String xtraParams, Connection conn) throws ITMException
	{
		String resposeStr = "";
		String objName = "";
		String argumentString = "";
		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		String messageStr = "SEND_SUCCESS";
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String tranIdRcp="",siteCodeShip="",emailAddr="",retString = "",userType="",empCode="";
		java.util.Date  tranDate= null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		itmDBAccessEJB = new ITMDBAccessEJB();
		StringBuffer commInfo = new StringBuffer();
		boolean isError = false;
		String reportDestination = "";
		String msgNo = "";
		try
		{
			System.out.println("tranId =["+tranId+"]");
			System.out.println("xmlString ::"+xmlString);
			System.out.println("reportName =["+reportName +"]");
			System.out.println("reportArgs =["+reportArgs +"]");
			System.out.println("reportType =["+reportType +"]");
			System.out.println("formatCode =["+formatCode +"]");
			System.out.println("xtraParams =["+xtraParams +"]");
			String argName = "";
			String argType = "";
			String rptArgName = "";
			String argValue = "";
			Document xmlDom = genericUtility.parseString(xmlString);
		
			if(reportName == null || reportName.trim().length() == 0)
			{
				isError = true;
				messageStr = itmDBAccessEJB.getErrorString("", "RPTNAMENF", "","",conn);
				generateLog(messageStr, reportName, reportDestination, formatCode, xtraParams);
			}
			else if(reportType == null || reportType.trim().length() == 0)
			{
				isError = true;
				messageStr = itmDBAccessEJB.getErrorString("", "RPTTYPENF", "","",conn);
				generateLog(messageStr, reportName, reportDestination, formatCode, xtraParams);
			}
			else if(formatCode == null || formatCode.trim().length() == 0)
			{
				isError = true;
				messageStr = itmDBAccessEJB.getErrorString("", "RPTFRMTNF", "","",conn);
				generateLog(messageStr, reportName, reportDestination, formatCode, xtraParams);	
			}
			else
			{
				if(reportArgs != null && reportArgs.trim().length() > 0)
				{
					String[] argNameArray= reportArgs.split(",");
					argumentString= "<Arguments>";
					for ( int cnt = 0 ; cnt < argNameArray.length ; cnt++ )
					{
						argName = argNameArray[cnt].substring( 0 , argNameArray[cnt].lastIndexOf("."));
						if(argNameArray[cnt].lastIndexOf(":") != -1)
						{
							rptArgName = argNameArray[cnt].substring( argNameArray[cnt].lastIndexOf(":") + 1 , argNameArray[cnt].length());
	
							argType = argNameArray[cnt].substring( argNameArray[cnt].lastIndexOf(".")+1 , argNameArray[cnt].lastIndexOf(":"));
						}
						else
						{
							rptArgName = argName;
							argType = argNameArray[cnt].substring( argNameArray[cnt].lastIndexOf(".")+1 );
						}
						if("login_site".equalsIgnoreCase(argName))
						{
							argValue = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
						}
						else if("user_id".equalsIgnoreCase(argName))
						{
							argValue = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
						}
						else if("terminal_id".equalsIgnoreCase(argName))
						{
							argValue = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
						}
						else if("entity_code".equalsIgnoreCase(argName))
						{
							argValue = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"entityCode");
						}
						else
						{
							argValue = genericUtility.getColumnValue(argName, xmlDom, "1");
						}
						
						argumentString += "<Argument id=\"" +  rptArgName + "\" name = \"" + rptArgName + "\">";
						argumentString += "<argType>" + argType +"</argType>"; 
						argumentString += "<value>" + argValue +"</value>";		
						argumentString += "</Argument>" ;
						
					}
					argumentString += "</Arguments>";
				}
				System.out.println("argumentString ::"+argumentString);
				
				String userName =  genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				String outputFilename = reportName + userName + System.currentTimeMillis();
				System.out.println("outputFilename ::"+outputFilename);
			
				if("HTML".equalsIgnoreCase(reportType) || "PDF".equalsIgnoreCase(reportType) || "CSV".equalsIgnoreCase(reportType) || "CSVDATA".equalsIgnoreCase(reportType) || "EXCEL".equalsIgnoreCase(reportType) || "EXCELDATA".equalsIgnoreCase(reportType) || "TEXT".equalsIgnoreCase(reportType) || "ODS".equalsIgnoreCase(reportType) || "EXCEL(XLSX)".equalsIgnoreCase(reportType))
				{
					XSDParser xsdParser = new XSDParser(reportName);
					ArgumentHandler argObj = new ArgumentHandler(xsdParser);
					argObj.init();
					
					UserInfoBean userInfo = new UserInfoBean(xtraParams);
					JasperReportGenerator  jasperReport= new  JasperReportGenerator(userInfo);
					TreeMap argMap = jasperReport.createArgMap(argumentString);
					argMap.put("report_save_type", reportType);
					String rptExt = "";
					if("CSV".equalsIgnoreCase(reportType) || "CSVDATA".equalsIgnoreCase(reportType) )
					{
						rptExt = "csv";
					}
					else if("EXCEL".equalsIgnoreCase(reportType) || "EXCELDATA".equalsIgnoreCase(reportType) || "ODS".equalsIgnoreCase(reportType) )
					{
						rptExt = "xls";
					}
					else if("EXCEL(XLSX)".equalsIgnoreCase(reportType) )
					{
						rptExt = "xlsx";
					}
					else if("TEXT".equalsIgnoreCase(reportType) )
					{
						rptExt = "txt";
					}
					else
					{
						rptExt = reportType.toLowerCase();
					}
					jasperReport.createReport(reportName ,argMap, xsdParser, userName,outputFilename);
					String reportOutputFileName = jasperReport.getOutputFileName();
				
					reportDestination = ResourceConstants.REPORTDESTINATION + File.separator + userName + File.separator + reportOutputFileName + "." + rptExt;
					System.out.println("reportDestination >>>>>>::"+reportDestination);
				}
				if(formatCode != null && formatCode.trim().length() > 0)
				{
					System.out.println("Format Code :"+formatCode);
					
					sql = "select tran_id__rcp,tran_date,emp_code__aprv from distrcp_exsh_hdr where tran_id= ?";
					pstmt = conn.prepareStatement(sql);	
					System.out.println("tranId>>>"+tranId);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranIdRcp = rs.getString("tran_id__rcp");
						tranDate = rs.getTimestamp("tran_date");
						empCode = rs.getString("emp_code__aprv");
						System.out.println("tranIdRcp["+tranIdRcp+"]");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				
					sql = "select site_code__ship from distord_rcp where tran_id= ? ";
					pstmt = conn.prepareStatement(sql);			
					pstmt.setString(1, tranIdRcp);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteCodeShip = rs.getString("site_code__ship");
						System.out.println("siteCodeShip["+siteCodeShip+"]");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select email_addr from site where site_code= ? ";
					pstmt = conn.prepareStatement(sql);			
					pstmt.setString(1, siteCodeShip);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						emailAddr = checkNull(rs.getString("email_addr"));
						System.out.println("emailAddr["+emailAddr+"]");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					commInfo.append("<ROOT>");
					commInfo.append("<MAIL><EMAIL_TYPE>page</EMAIL_TYPE><ENTITY_CODE>BASE</ENTITY_CODE>");
					commInfo.append("<ENTITY_TYPE>"+userType+"</ENTITY_TYPE>");
					commInfo.append("<TO_ADD>"+emailAddr+"</TO_ADD>");
					commInfo.append("<BCC_ADD></BCC_ADD>");
					commInfo.append("<FORMAT_CODE>"+formatCode+"</FORMAT_CODE>");							
					commInfo.append("<ATTACHMENT><BODY></BODY><LOCATION></LOCATION></ATTACHMENT>");
					commInfo.append("</MAIL>");
					commInfo.append("<XML_DATA><ROOT><Detail1><TRAN_ID>"+tranId+"</TRAN_ID><TRAN_DATE>"+tranDate+"</TRAN_DATE><emp_code>"+empCode+"</emp_code>");
					commInfo.append("<cc_to></cc_to></Detail1></ROOT></XML_DATA>");
					commInfo.append("<EMAIL_TYPE>page</EMAIL_TYPE><XML_DATA_FILE_PATH>"+reportDestination+"</XML_DATA_FILE_PATH><ENTITY_CODE></ENTITY_CODE>");
					commInfo.append("</ROOT>");	
					EMail email = new EMail();
					email.sendMail(commInfo.toString(), "ITM"); 
					//retString = "submited";
					System.out.println("  information submited::");
				}
			}
		}
		catch(Exception e)
		{
			isError = true;
			try
			{
				messageStr = itmDBAccessEJB.getErrorString("", "RPTFAILED", "","",conn);
				String errMsg = e.getMessage();
				generateLog("REPORT Sending Failed :"+errMsg, reportName, reportDestination, formatCode, xtraParams);
			}
			catch(Exception se)
			{
				
			}
			System.out.println("Exception ::"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				System.out.println("isError in finally"+isError);
				if(!isError)
				{
					messageStr = itmDBAccessEJB.getErrorString("", "REPORTSUCC", "","",conn);
					System.out.println("Message Str >>> "+messageStr);
					generateLog("Status : REPORT Send Successfully, MSG_NO :REPORTSUCC", reportName, reportDestination, formatCode, xtraParams);
				}
			}
			catch(Exception fe)
			{
				
			}
		}
		System.out.println("messageStr ::"+messageStr);
		return messageStr;
	}
	
	private void generateLog(String responseStr, String reportName, String reportFilePath, String formatCode, String xtraParams)throws RemoteException, ITMException
	{
		ConnDriver mConnDriver = null;
		Connection mConnection = null;
		PreparedStatement pStmt = null;
		ResultSet mRs = null;
		
		String status = "";
		try
		{
			mConnDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//mConnection = mConnDriver.getConnectDB("DriverITM");
			mConnection = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			
			String tranID = generateTranID(mConnection, xtraParams);
			
			if(tranID != null && tranID.trim().length() > 0)
			{
				String insertSql = "INSERT INTO REPORT_LOG (TRAN_ID, OBJ_NAME, REPORT_NAME, REPORT_SEND_DATE, REPORT_FILE_PATH, FORMAT_CODE, STATUS, CHG_DATE,CHG_USER,CHG_TERM) VALUES(?,?,?,?,?,?,?,?,?,?)";
				pStmt = mConnection.prepareStatement(insertSql);
				
				pStmt.setString(1, tranID);
				pStmt.setString(2, objName);
				pStmt.setString(3,reportName);
				pStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
				pStmt.setString(5, reportFilePath);
				pStmt.setString(6, formatCode);
				pStmt.setString(7, responseStr);
				pStmt.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
				pStmt.setString(9,  getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				pStmt.setString(10,  getValueFromXTRA_PARAMS(xtraParams, "termId"));
				
				int insCnt = pStmt.executeUpdate();
				
				if(insCnt > 0)
				{
					mConnection.commit();
				}
			}
			else
			{
				System.out.println("Transaction id is null.......");
			}
		}
		catch (ITMException itme)
		{ 
			System.err.println("ITMException :generateLog :\n"); //$NON-NLS-1$
			throw itme;
		}
		catch (Exception e)
		{ 
			System.err.println("Exception :generateLog :\n"+e.getMessage()); //$NON-NLS-1$
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(mConnection != null)
				{					
					if(mRs != null)
					{
						mRs.close();
						mRs = null;
					}
					if(pStmt != null)
					{
						pStmt.close();
						pStmt = null;
					}
					mConnection.close();
					mConnection = null;
				}
			}
			catch(Exception e)
			{
				System.err.println("Exception :generateLog :\n"+e.getMessage()); //$NON-NLS-1$
				throw new ITMException(e);
			}
		}
	}
	private String generateTranID( Connection conn, String xtraParams )
	{
		String sprsTravelTranID = "";
		PreparedStatement pStmt = null;
		ResultSet rSet = null;
		String refSer="";
		String tranIdCol="";
		String keyString="";
		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		
		try
		{
			String transSql = "SELECT REF_SER,TRAN_ID_COL,KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW  = 'w_report_log'";

			pStmt = conn.prepareStatement(transSql);
			rSet = pStmt.executeQuery();
			
			if( rSet.next() )
			{
				refSer = (rSet.getString("REF_SER") == null) ?"":rSet.getString("REF_SER").trim();
				tranIdCol = (rSet.getString("TRAN_ID_COL") == null) ?"":rSet.getString("TRAN_ID_COL").trim();
				keyString = (rSet.getString("KEY_STRING") == null) ?"":rSet.getString("KEY_STRING").trim();
			}
			System.out.println("refSer["+refSer+"]tranIdCol["+tranIdCol+"]keyString["+keyString+"]");

			TransIDGenerator tranIDGenerator = new TransIDGenerator("<Root></Root>", getValueFromXTRA_PARAMS(xtraParams, "loginCode"), CommonConstants.DB_NAME);
			sprsTravelTranID = tranIDGenerator.generateTranSeqID( refSer,tranIdCol,keyString,conn) ;
		}
		catch( Exception exp )
		{
			System.out.println("Exception In generateTranID......");
			exp.printStackTrace();
		}
		finally
		{
			try
			{
				if( rSet != null )
				{
					rSet.close();
					rSet.close();
				}
				if( pStmt != null )
				{
					pStmt.close();
					pStmt.close();
				}
			}
			catch( Exception expRsc )
			{
				expRsc.printStackTrace();
			}
		}
		return sprsTravelTranID;
	}
	private String getValueFromXTRA_PARAMS(String xtraParams, String string) {
	// TODO Auto-generated method stub
		return null;
	}
}//end of class
	