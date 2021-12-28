package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.reports.utility.JasperReportGenerator;
import ibase.webitm.reports.utility.XSDParser;
import ibase.webitm.utility.ITMException;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TreeMap;

import javax.ejb.Stateless;
import org.w3c.dom.Document;

@Stateless
public class ReminderDueCustPrc extends ProcessEJB implements ReminderDueCustPrcLocal, ReminderDueCustPrcRemote{
	
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmdbAccessEJB = new ITMDBAccessEJB();
	
	private String checkNull(String input)
	{
		return input = input== null ? "" : input.trim(); 
	}

	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{	
		String retString = "";
		Document dom = null;
		Document dom2 = null;

		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
				System.out.println("dom::::::: "+dom);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
				System.out.println("dom2::::::: "+dom2);
			}
			retString = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::"+this.getClass().getSimpleName()+"::processString" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return retString;
	}
	
	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException {

		System.out.println("Inside process method of "+this.getClass().getSimpleName());
		String errString = "", userId = "", userName = "", sql = "",  itemSer = "" , prdCode = "",
				  posCodeLevel4 = "" , empCodeLevel2 = "" , loginSiteCode = "" , countryCode = "" , entryEndDateStr = "" , empCode = "" , ccEmail = "" ;
		int  cnt = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Timestamp frDate = null , toDate = null , entryEndDate = null; 
		String objName = "",  path = "", reportOutputFileName = "", outputFilename = "", argumentString = "";
		ArrayList<String> posCode = new ArrayList<String>();
		HashSet< String> empCode2 = new HashSet<String>();
		StringBuffer retTabSepStrBuff = new StringBuffer();
		String poscode2="";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String date = sdf.format(Calendar.getInstance().getTime());

			//ConnDriver con = new ConnDriver();
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection();
			
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userID"));
			userName = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			
			prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			System.out.println("prdCode ::::::::: "+prdCode+ "itemSer::::::::::"+itemSer);
			
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
				
				sql = "SELECT ENTRY_END_DT,FR_DATE,TO_DATE FROM PERIOD_TBL WHERE PRD_CODE = ? AND PRD_TBLNO = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,prdCode.trim());
				pstmt.setString(2,countryCode+"_"+itemSer.trim());	
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					entryEndDate = rs.getTimestamp("ENTRY_END_DT");
					frDate = rs.getTimestamp("FR_DATE");
					toDate = rs.getTimestamp("TO_DATE");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				entryEndDateStr = sdf.format(entryEndDate);
				
		
			
			sql = "SELECT POS_CODE FROM" +
					" (SELECT POS_CODE FROM ORG_STRUCTURE_CUST WHERE VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) AND TABLE_NO= ? " +
					" AND EFF_DATE <= ?  AND VALID_UPTO >= ?  AND CASE WHEN SOURCE IS NULL THEN 'Y' ELSE SOURCE END <> 'A' " +
					" MINUS " +
					" SELECT POS_CODE FROM CUST_STOCK WHERE PRD_CODE = ? AND ITEM_SER = ? AND POS_CODE IS NOT NULL " +
					" UNION" +
					" SELECT POS_CODE FROM CUST_STOCK WHERE PRD_CODE = ? AND ITEM_SER = ? AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END = 'N' AND POS_CODE IS NOT NULL " +
					")" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemSer);
			pstmt.setTimestamp(2, frDate);
			pstmt.setTimestamp(3, toDate);
			pstmt.setString(4, prdCode);
			pstmt.setString(5, itemSer);
			pstmt.setString(6, prdCode);
			pstmt.setString(7, itemSer);
			rs = pstmt.executeQuery();
			
			while(rs.next())
			{
			posCodeLevel4 = checkNull(rs.getString("POS_CODE"));
			posCode.add(posCodeLevel4);
			cnt++;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt == 0)
			{
				errString = itmdbAccessEJB.getErrorString("", "VTNOPENREC", userId);
			}
			System.out.println("poscode length :::::: "+posCode.size());
			System.out.println("poscode list :::::: "+posCode.toString());
			for(String position : posCode)
			{
				sql = " SELECT EMP_CODE FROM ( SELECT DISTINCT EMP_CODE , LEVEL_NO, VERSION_ID,TABLE_NO FROM ORG_STRUCTURE  START WITH POS_CODE = ? " +
					  " CONNECT BY PRIOR POS_CODE__REPTO = POS_CODE ) WHERE LEVEL_NO = 2 AND TABLE_NO= ? AND VERSION_ID = ( SELECT FN_GET_VERSION_ID FROM DUAL )";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, position);
				pstmt.setString(2, itemSer);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					empCodeLevel2  = checkNull(rs.getString("EMP_CODE"));
					empCode2.add(empCodeLevel2);
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("empCode2 hashsetlist :::::: "+empCode2.toString());
			
			for(String empcode2 : empCode2)
			{
				System.out.println("empcode2:::::: "+empcode2);
				sql = "SELECT POS_CODE FROM EMPLOYEE WHERE EMP_CODE=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, empcode2);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					poscode2  = checkNull(rs.getString("POS_CODE"));
					System.out.println("poscode2:::::::: "+poscode2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				
				sql = "SELECT EMAIL_ID_OFF FROM EMPLOYEE WHERE EMP_CODE IN ( SELECT REPORT_TO FROM EMPLOYEE WHERE EMP_CODE = ? )";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, empcode2);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ccEmail  = checkNull(rs.getString("EMAIL_ID_OFF"));
					System.out.println("ccEmail:::::::: "+ccEmail);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				
				System.out.println("Date ::::::: "+date);
				retTabSepStrBuff.append("<Detail1>\r\n");
				retTabSepStrBuff.append("<prd_code>").append("<![CDATA["+prdCode+"]]>").append("</prd_code>\r\n");
				retTabSepStrBuff.append("<table_no>").append("<![CDATA["+itemSer+"]]>").append("</table_no>\r\n");
				retTabSepStrBuff.append("<pos_code>").append("<![CDATA["+poscode2+"]]>").append("</pos_code>\r\n");
				retTabSepStrBuff.append("<date>").append("<![CDATA["+date+"]]>").append("</date>\r\n");
				retTabSepStrBuff.append("<entry_date>").append("<![CDATA["+entryEndDateStr+"]]>").append("</entry_date>\r\n");
				retTabSepStrBuff.append("</Detail1>\r\n");
				
				System.out.println("string buffer ::::::::: "+retTabSepStrBuff.toString());
					objName = "d_es3_entry_status";
					outputFilename = objName + userName + System.currentTimeMillis();
					
					argumentString = "<Arguments>" +
							"<Argument id=\"site_code\" name=\"site_code\">" +
							"<argType>java.lang.String</argType>" +
							"<value>" + loginSiteCode + "</value>" +
							"</Argument>" +
							"<Argument id=\"prd_code\" name=\"prd_code\">" +
							"<argType>java.lang.String</argType>" +
							"<value>" + prdCode + "</value>" +
							"</Argument>" +
							"<Argument id=\"division_code\" name=\"division_code\">" +
							"<argType>java.lang.String</argType>" +
							"<value>" + itemSer + "</value>" +
							"</Argument>" +
							"<Argument id=\"pos_code\" name=\"pos_code\">" +
							"<argType>java.lang.String</argType>" +
							"<value>" + poscode2 + "</value>" +
							"</Argument>" +
						"</Arguments>";
				
				System.out.println("argumentString:::"+argumentString);
				XSDParser xsdParser = new XSDParser(objName);
				System.out.println(":::::::::::::xsd parsed:::::::::::::");
				UserInfoBean userInfo = new UserInfoBean(xtraParams);
				JasperReportGenerator jasperReport = new JasperReportGenerator(userInfo);
				System.out.println("after generating report");
				TreeMap argMap = jasperReport.createArgMap(argumentString);
				argMap.put("report_save_type", "PDF");
				argMap.put("data_source", "SQL");
				System.out.println("argMap:::" + argMap + "\nxsdParser:::" + xsdParser + "\nuserName:::" + userName + "\noutputFilename:::" + outputFilename);
				jasperReport.createReport(objName, argMap, xsdParser, userName, outputFilename);
				reportOutputFileName = jasperReport.getOutputFileName();
				File f = new File(reportOutputFileName);
				System.out.println("reportOutputFileName:::"+reportOutputFileName);
				String fs = f.getAbsolutePath();
				int index = fs.indexOf(File.separator + "bin");
				System.out.println("fs:::"+fs);
				path = fs.substring(0, index);
				System.out.println("file path:::" + path);
				System.out.println("Output file name" + reportOutputFileName);
				
				
				ArrayList mailInfo = new ArrayList();
				mailInfo.add(empcode2);
				mailInfo.add(ccEmail);
				mailInfo.add(retTabSepStrBuff.toString());
				//mailInfo.add(subject);
				//mailInfo.add(message);
				mailInfo.add(path + "/server/default/deploy/ibase.ear/ibase.war/webitm/reports/finalreport/" + userName + "/" + reportOutputFileName + ".pdf");
				errString = sendingMail(mailInfo);
				if("F".equalsIgnoreCase(errString)) {
					return itmdbAccessEJB.getErrorString("", "VTMAILFAIL", userId);
				}
			}
			errString = itmdbAccessEJB.getErrorString("", "VTMAILSUCC", userId);
		} catch (Exception e) {
			System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		} finally {
			
			try {
				if(conn != null){
					conn.close();
					conn = null;
				}
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
	}
		return errString;
	}

	private String sendingMail(ArrayList<String> mailInfo) throws ITMException {
	    StringBuffer commInfo = new StringBuffer();
	    String mailRetStr = "" , errString = "" , userId = "" , errMail = "" ;
	    commInfo.append("<ROOT>");
	    commInfo.append("<MAILINFO>");
	    commInfo.append("<EMAIL_TYPE>").append("page").append("</EMAIL_TYPE>");
	    //commInfo.append("<TO_ADD>").append("<![CDATA[" + (String)mailInfo.get(0) + "]]>").append("</TO_ADD>");
	    commInfo.append("<ENTITY_CODE>").append("<![CDATA[" + (String)mailInfo.get(0) + "]]>").append("</ENTITY_CODE>");
	    commInfo.append("<CC_ADD>").append("<![CDATA[" + (String)mailInfo.get(1) + "]]>").append("</CC_ADD>");
	    //commInfo.append("<SUBJECT>").append("<![CDATA[" + (String)mailInfo.get(2) + "]]>").append("</SUBJECT>");
	    //commInfo.append("<BODY_TEXT>").append("<![CDATA[" + (String)mailInfo.get(3) + "]]>").append("</BODY_TEXT>");
	    commInfo.append("<FORMAT_CODE>ES3-MailPrc</FORMAT_CODE>");
	    commInfo.append("<XML_DATA>").append( (String)mailInfo.get(2) ).append("</XML_DATA>");
	    commInfo.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" + (String)mailInfo.get(3) + "]]>").append("</XML_DATA_FILE_PATH>");
	    commInfo.append("</MAILINFO>");
	    commInfo.append("</ROOT>");
	    
	    try 
	    {
			    if(((String)mailInfo.get(0) != null && ((String)mailInfo.get(0)).trim().length() > 0))
			    {
			    EMail email = new EMail();
			    mailRetStr = email.sendMail(commInfo.toString(), "ITM");
			    //mailRetStr = itmdbAccessEJB.getErrorString("","VTMAILSUCC", userId);
			    mailRetStr = "S";
			    System.out.println("Mail return String:::>>[" + mailRetStr+ "]");
			    email = null;
			    }
			    else
			    {
			    	//mailRetStr = itmdbAccessEJB.getErrorString("","VTMAILFAIL", userId);
			    	mailRetStr = "F";
			    }
	    } 
	    catch (Exception e)
	    {
		      e.printStackTrace();
		      System.out.println("Exception:sendMail(String ,String)" + 
		        e.getMessage());
		      throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		 }
	    return mailRetStr;
	}

}
