package ibase.webitm.ejb.dis.adv;

import java.io.File;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.utility.MailInfo;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.DistCommon;
//import ibase.webitm.ejb.dis.adv.ItemDetails;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

@Stateless
public class EwaybillPrc extends ProcessEJB implements EwaybillPrcLocal,EwaybillPrcRemote {

	private String generatedTransactionId="";
	private String siteCodes="";
	private String loginCode="";
	private String logFileNames="", mailLogFileName = "";
	private String logDires="";
	private List<Transaction> tranIdList=new ArrayList<Transaction>();	
	SimpleDateFormat sdf=null;
	String logPathPrintL="";//added by nandkumar gadkari on 05/12/19
	@Override
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException, ITMException {
		String rtrStr="";
		Document headerDom=null;
		Document detailDom=null;
	//	GenericUtility genericUtility = GenericUtility.getInstance();
		
		System.out.println("Calling getData method......");
		try{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			if ((xmlString != null) && (xmlString.trim().length() != 0)) 
			{
				System.out.println("XML String :" + xmlString);
				headerDom = genericUtility.parseString(xmlString);
			}
			if ((xmlString2 != null) && (xmlString2.trim().length() != 0)) 
			{
				System.out.println("XML String :" + xmlString2);
				detailDom = genericUtility.parseString(xmlString2);
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e) 
		{
			 System.out
			.println("Exception :getData(String xmlString, String xmlString2, String windowName, String xtraParams):"
					+ e.getMessage() + ":");
			rtrStr = e.getMessage();
		}
		return rtrStr;
	}
	public String getData(Document dom, Document dom2, String windowName, String xtraParams)
			throws RemoteException, ITMException 
	{
		String  resultString="";
		String sql="",objName="",argumentString="",jrxmlName="",tranSerOld=""; 
		String sqlExpr="",sqlExpr1="",sqlExpr2="",sqlExpr3="",sqlExpr4="",sqlExpr5="",sqlExpr6="",errfilePath="";
		String compSql="";
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null ;
		String balGroup="EWAY",dateFromString="",dateToString="";
		Connection conn = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		try {
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
		Timestamp dateFrom = null;
		Timestamp dateTo = null;
		StringBuffer xmlStringBuffer = new StringBuffer();	
		//screen retrieval values
		String siteCode="",tranSer="",sundryCodeFr="",sundryCodeTo="",tranDateFr="",tranDateTo="",invValue="",supplyType="";
		//display screen values
		String tranSerRet="",userId="",tranDate = "",tranId="",gpNp="",toPartyDesc="",toPlace="",pincode="",gstnno="",docType="";
		String transMode = "",vehicleType = "",transPorterName="";
		Timestamp tran_date = null;
		double invAmt=0.0d,asseblValue = 0.0d,distance = 0.0d;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String errString = "";		
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "user_id");
		loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		siteCode=genericUtility.getColumnValue("site_code", dom2);
		siteCodes = siteCode;
		tranSer=genericUtility.getColumnValue("tran_ser", dom2);
		sundryCodeFr=genericUtility.getColumnValue("sundry_code_fr", dom2);
		sundryCodeTo=genericUtility.getColumnValue("sundry_code_to", dom2);
		tranDateFr=genericUtility.getColumnValue("tran_date_fr", dom2);
		tranDateTo=genericUtility.getColumnValue("tran_date_to", dom2);
		invValue=genericUtility.getColumnValue("inv_value", dom2);
		supplyType=genericUtility.getColumnValue("state", dom2);
		String lrNo="",lorryNo="",lrdateStr="";
		Timestamp lrDate =null;
		int cnt=0;
		String gstnNo = "";
		try
		{
			/*ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB();*/
			conn = getConnection();
			//added by nandkumar gadkari on 05/12/19-
			if(siteCode!=null && siteCode.trim().length()>0)
			{
				sql="select COUNT(*) from site where site_code= ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);
				}
				rs.close();
				rs = null;
			    pstmt.close();
				pstmt = null;
				if(cnt==0)
				{
					errString = itmDBAccessEJB.getErrorString("","VMSITE1",userId,"",conn);
					return errString;
				}
				else
				{
					sql="select ddf_get_siteregno(?,'GSTIN_NO') AS GSTNNO from dual";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						gstnNo=rs.getString("GSTNNO");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(gstnNo == null || gstnNo.trim().length() == 0 || gstnNo.equalsIgnoreCase("null"))
					{
						errString = itmDBAccessEJB.getErrorString("","VTEW017",userId,"",conn);
						return errString;
					}
				}
			}
			//added by nandkumar gadkari on 05/12/19--end-----
			xmlStringBuffer = new StringBuffer("<?xml version = \"1.0\"?>");
			xmlStringBuffer.append("<DocumentRoot>");
			xmlStringBuffer.append("<description>").append("Datawindow Root").append("</description>");
			xmlStringBuffer.append("<group0>");
			xmlStringBuffer.append("<description>").append("Group0 description").append("</description>");
			xmlStringBuffer.append("<Header0>");
			SimpleDateFormat sdf  = new SimpleDateFormat(genericUtility.getApplDateFormat());
			java.util.Date currentDate = new java.util.Date();
			String todaysDate=sdf.format(currentDate);
			System.out.println("Current date is :"+todaysDate);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			tranDateFr = genericUtility.getValidDateString(tranDateFr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dateFrom = java.sql.Timestamp.valueOf(tranDateFr + " 00:00:00");
			dateFromString = simpleDateFormat.format(dateFrom);
			tranDateTo = genericUtility.getValidDateString(tranDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dateTo = java.sql.Timestamp.valueOf(tranDateTo + " 00:00:00");
			tranDateTo = simpleDateFormat.format(dateTo);
			String[] tranSerArray=tranSer.split(",");
			for(int i=0;i<tranSerArray.length;i++){
				if(i==tranSerArray.length-1)
				{
					tranSerOld=tranSerOld+("'"+tranSerArray[i].trim()+"'");
				}
				else
				{
					tranSerOld=tranSerOld+("'"+tranSerArray[i].trim()+"',");
				}
			}
			int dataCounter=0;
			sql="select line_no,sql_expr,sql_input,sql_expr1,sql_expr2,sql_expr3,sql_expr4,"
					+ " sql_expr5,sql_expr6 from tax_bal_grp_det where bal_group=? and ref_ser in ("+tranSerOld.trim()+")";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, balGroup);
			rs=pstmt.executeQuery();
			while(rs.next()){
				sqlExpr= checkNull(rs.getString("sql_expr"));
				sqlExpr1= checkNull(rs.getString("sql_expr1"));
				sqlExpr2= checkNull(rs.getString("sql_expr2"));
				sqlExpr3= checkNull( rs.getString("sql_expr3"));
				sqlExpr4= checkNull(rs.getString("sql_expr4"));
				sqlExpr5= checkNull(rs.getString("sql_expr5"));
				sqlExpr6= checkNull(rs.getString("sql_expr6"));  


				compSql= sqlExpr+" "+sqlExpr1+" "+sqlExpr2+" "+sqlExpr3+" "+sqlExpr4+" "+sqlExpr5+" "+sqlExpr6;
				System.out.println("Combine SQL::::"+compSql);

				if(compSql!=null && compSql.trim().length() > 0 )
				{
					pstmt1=conn.prepareStatement(compSql);
					pstmt1.setString(1, siteCode);
					pstmt1.setTimestamp(2, dateFrom);
					pstmt1.setTimestamp(3, dateTo);
					pstmt1.setString(4, sundryCodeFr);
					pstmt1.setString(5, sundryCodeTo);
					pstmt1.setInt(6, Integer.parseInt(invValue));
					pstmt1.setString(7, supplyType);
					pstmt1.setString(8,supplyType);

					rs1=pstmt1.executeQuery();
					while(rs1.next()){

						tranSerRet=rs1.getString("tran_ser");
						tran_date=rs1.getTimestamp("tran_date");
						if(tran_date!=null){
							tranDate=genericUtility.getValidDateString(tran_date.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
						}
						else{
							tranDate="";
						}
						tranId=rs1.getString("tran_id");
						gpNp=rs1.getString("gp_no");
						toPartyDesc=rs1.getString("to_party_descr");
						invAmt=rs1.getDouble("inv_amt");
						asseblValue=rs1.getDouble("assesable_value");
						distance=rs1.getDouble("distance");
						pincode=rs1.getString("pincode");
						gstnno=rs1.getString("gstnno");
						/*if(gstnno == null || gstnno.trim().length() == 0 || "null".equalsIgnoreCase(gstnno))
						{
							continue;
						}*/
						docType=rs1.getString("doctype");
						transMode=rs1.getString("trans_mode");
						vehicleType=rs1.getString("vehicle_type");
						transPorterName=rs1.getString("transporter_name");
						//added by nandkumar gadkari on 05/12/19
						lrNo=rs1.getString("LR_NO");
						lrDate=rs1.getTimestamp("LR_DATE");
						lorryNo=rs1.getString("LORRY_NO");
						lrNo = lrNo==null ? "" :lrNo;
						lorryNo= lorryNo== null ? "" :lorryNo;
						xmlStringBuffer.append("<Detail2>");
						xmlStringBuffer.append("<tran_ser>").append("<![CDATA[").append(tranSerRet).append("]]>").append("</tran_ser>");
						xmlStringBuffer.append("<tran_date>").append("<![CDATA[").append(tranDate).append("]]>").append("</tran_date>");
						xmlStringBuffer.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>");
						xmlStringBuffer.append("<gp_no>").append("<![CDATA[").append(gpNp).append("]]>").append("</gp_no>");
						xmlStringBuffer.append("<to_party_descr>").append("<![CDATA[").append(toPartyDesc).append("]]>").append("</to_party_descr>");
						//xmlStringBuffer.append("<to_place>").append("<![CDATA[").append(toPlace).append("]]>").append("</to_place>");
						xmlStringBuffer.append("<trans_name>").append("<![CDATA[").append("").append("]]>").append("</trans_name>");
						xmlStringBuffer.append("<trans_mode>").append("<![CDATA[").append(transMode==null?"R":transMode).append("]]>").append("</trans_mode>");
						xmlStringBuffer.append("<trans_doc_id>").append("<![CDATA[").append(lrNo).append("]]>").append("</trans_doc_id>");
						if(lrDate!=null)
						{
							xmlStringBuffer.append("<trans_doc_date>").append("<![CDATA[").append(sdf.format(lrDate)).append("]]>").append("</trans_doc_date>");
						}
						else
						{
							xmlStringBuffer.append("<trans_doc_date>").append("<![CDATA[").append(todaysDate).append("]]>").append("</trans_doc_date>");
						}
						xmlStringBuffer.append("<vehicle_no>").append("<![CDATA[").append(lorryNo).append("]]>").append("</vehicle_no>");
						xmlStringBuffer.append("<distance>").append("<![CDATA[").append(distance).append("]]>").append("</distance>");
						xmlStringBuffer.append("<invoice_amt>").append("<![CDATA[").append(invAmt).append("]]>").append("</invoice_amt>");
						xmlStringBuffer.append("<assesable_value>").append("<![CDATA[").append(asseblValue).append("]]>").append("</assesable_value>");
						xmlStringBuffer.append("<pincode>").append("<![CDATA[").append(pincode).append("]]>").append("</pincode>");
						xmlStringBuffer.append("<gstnno>").append("<![CDATA[").append(gstnno).append("]]>").append("</gstnno>");
						xmlStringBuffer.append("<doc_type>").append("<![CDATA[").append(docType).append("]]>").append("</doc_type>");
						xmlStringBuffer.append("</Detail2>");
						dataCounter++;
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;

				}

			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;



			if(dataCounter==0){
				printLog("No Data Found", "Please Enter Valid Query to Return data","ERROR"); //this will write on log file for no data is found
				errString = itmDBAccessEJB.getErrorString("","VTNODATA",userId,"",conn);
				System.out.println("process completed .."+errString);
				return errString;
			}

			xmlStringBuffer.append("</Header0>");
			xmlStringBuffer.append("</group0>");
			xmlStringBuffer.append("</DocumentRoot>");
			resultString = xmlStringBuffer.toString();
			System.out.println("@narendra Final xml string : "+resultString);

		}
		catch (SQLException e)
		{
			printLog("E-way Bill Failed", "SQL Sytax is Incorrect for Ref-Ser :"+tranSerOld,"ERROR");
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception e){}
		}		

		return resultString;
	}
	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input.trim();
	}

	@Override
	public String process(String xmlString, String xmlString1, String windowName, String xtraParams)
			throws RemoteException, ITMException {
		System.out.println("Inside process method");

		// TODO Auto-generated method stub

		//calling values inside the test process.... done		

		//make log file which will shows the log generated while processing the data.... done

		//while processing the data insert the data inside the GST_DATA_HDR as well as inside the GST_DATA_DET

		//query is already created we just have to use that built in query inside the code..

		//json file will be generated using that sql which will be inside the tax bal grp det table

		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
	//	GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println(
				"below genericUtility--------------->>>>>>>>>");
		try {
			E12GenericUtility genericUtility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			System.out.println("Process Method xmlString = " + xmlString);
			System.out.println("Process Method xmlString1 = " + xmlString1);
			if ((xmlString != null) && (xmlString.trim().length() != 0)) 
			{
				headerDom = genericUtility.parseString(xmlString);
				System.out.println("headerDom === " + headerDom);
			}
			if ((xmlString1 != null) && (xmlString1.trim().length() != 0)) 
			{
				detailDom = genericUtility.parseString(xmlString1);
				System.out.println("detailDom === " + detailDom);
			}

			retStr = process(headerDom, detailDom, windowName, xtraParams);
		} catch (Exception e) 
		{
			printLog("E-way Bill Failed", "Error occured during Processing the data","ERROR");
			System.out
			.println("Exception :process(String xmlString, String xmlString2, String windowName, String xtraParams):"
					+ e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
			throw new ITMException(e);
		}
		return retStr;
	}
	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams)
			throws RemoteException, ITMException 
	{
		// TODO Auto-generated method stub
	//	GenericUtility genericUtility = GenericUtility.getInstance();
		String errString="";
		String errCode="";
		Connection conn = null;
		String refSer="",tranId="";

		try{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			/*ConnDriver connDriver = new ConnDriver();
			conn=connDriver.getConnectDB("DriverITM");
			conn.setAutoCommit(false);*/
			conn = getConnection();
			refSer=genericUtility.getColumnValue("tran_ser", dom2);
			tranId=genericUtility.getColumnValue("tran_id",dom2);
			System.out.println("@ref ser:"+refSer+" @invoiceId:"+tranId);
			DistCommon distCommon = new DistCommon();
			logPathPrintL=distCommon.getDisparams("999999", "EWAY_LOG_PATH", conn);//added by nandkumar gadkari on 05/12/19
			loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			errString=executeGSTHdrAndGSTDetQuery(dom,dom2,xtraParams,conn);

		}catch (SQLException e)
		{
			System.out.println("Exception :BankReconciliationEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :BankReconciliationEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception e){}
		}		

		return errString;	
	}


	public String executeGSTHdrAndGSTDetQuery(Document dom,Document dom2,String xtraParams,Connection conn){
		String errString="";
		Node currDetail = null;
		int noOfDetails = 0;
		NodeList detailList = null;
		NodeList detailNodeList = null;
		Node detailNode = null;
		int lltrue = 0;
		int nodeListLength=0;
		String tranId="";
		String refSer="";
		String distance="";
		String transporterName="";
		String transId="";
		String transMode="";
		String transDocNo="";
		String transDocDate="";
		String vehicleType="";
		String vehicleNo="";
		//E12GenericUtility genericUtility = E12GenericUtility.getInstance();
		ArrayList dataList=new ArrayList();
		HashMap<String,String> transDetailMap=new HashMap<String,String>();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String siteCode="";
		File logFilePath=null;
		File copyFilePath=null;
		File copyLogFilePath=null;
		boolean mailSent;
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null ;
		String sql="";
		String emailAddr="";
		
		try{
			DBAccessEJB dbAccess = new DBAccessEJB();//added by nandkumar gadkari on 05/12/19
			E12GenericUtility genericUtility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			DistCommon distCommon = new DistCommon();//Modified by Ahmed on 13/06/2019 to set log path
			siteCode=genericUtility.getColumnValue("site_code", dom);
			siteCodes=siteCode;
			setLogFile(conn);//Modified by Ahmed on 13/06/2019 to set log path
			detailList = dom2.getElementsByTagName( "Detail2" );
			noOfDetails	= detailList.getLength();

			detailNodeList = dom2.getElementsByTagName("Detail2");
			nodeListLength = detailNodeList.getLength();
			System.out.println("nodeListLength" + nodeListLength);
			for(int n = 0;n < nodeListLength;n++)
			{
				try
				{
					detailNode = detailNodeList.item(n);
					tranId=genericUtility.getColumnValueFromNode("tran_id", detailNode);
					refSer=genericUtility.getColumnValueFromNode("tran_ser", detailNode);
					distance=genericUtility.getColumnValueFromNode("distance", detailNode);
					transporterName=genericUtility.getColumnValueFromNode("trans_name", detailNode);
					transMode=genericUtility.getColumnValueFromNode("trans_mode", detailNode);
					transId=genericUtility.getColumnValueFromNode("trans_id", detailNode);
					transDocNo=genericUtility.getColumnValueFromNode("trans_doc_id", detailNode);
					transDocDate=genericUtility.getColumnValueFromNode("trans_doc_date", detailNode);
					vehicleType="R"; //default value for the vehicle is regular holding suffix variable as the 'R'
					vehicleNo=genericUtility.getColumnValueFromNode("vehicle_no", detailNode);
					transDetailMap.put("distance", distance);
					transDetailMap.put("trans_name", transporterName);
					transDetailMap.put("trans_mode", transMode);
					transDetailMap.put("trans_id", getTransId(transporterName,conn));
					transDetailMap.put("trans_doc", transDocNo);
					transDetailMap.put("trans_doc_date", transDocDate);
					transDetailMap.put("vehicle_type", vehicleType);
					transDetailMap.put("vehicle_no", vehicleNo);
					dataList.add(transDetailMap);
					errString=insertDataInGstHdrAndDetail(tranId,refSer,dataList, conn,xtraParams);//xtraParams added by nandkumar on 05/12/19
					System.out.println(genericUtility.getColumnValueFromNode("tran_id", detailNode));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			dataList.clear();
			//generating json file eway bill
			/*if(errString.indexOf("CONFSUCCES") > -1 || errString.indexOf("Success") > -1 )
			{*/
			System.out.println("inside after insertion of the data inside HDR and DET");
			System.out.println("Generated Transaction List id is:"+generatedTransactionId);
			if(tranIdList.size()>0){
				errString= generateJSONFileForEway(tranIdList,siteCode,xtraParams,conn);
			}
			else{
				try{
					sql="select email_id from users where code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, loginCode.trim());
					rs=pstmt.executeQuery();
					while(rs.next()){
						emailAddr=rs.getString("email_id");
					}

				}
				catch(SQLException e){
					e.printStackTrace();
				}
				if(rs!=null){
					rs.close();
					rs=null;
				}
				if(pstmt!=null){
					pstmt.close();
					pstmt=null;
				}
				
				System.out.println("Log Dire:"+logDires+" Log File:"+logFileNames); 
				
				copyLogFilePath = new File(logDires + File.separator +"temp");
				if(!copyLogFilePath.exists()) {
					copyLogFilePath.mkdir();
				}
				else if(copyLogFilePath.exists())
				{
					try
					{
						FileUtils.cleanDirectory(copyLogFilePath);
					}
					catch(Exception exp)
					{
						exp.printStackTrace();
					}
				}
				logFilePath=new File(logDires+File.separator+logFileNames+".log");
				//logFilePath=new File(logDires+File.separator+mailLogFileName+".log");//Modified by Ahmed on 13/06/2019
				try
				{
					
					UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);//added by nandkumar gadkari on 05/12/19
					FileUtils.copyFileToDirectory(logFilePath, copyLogFilePath);
					System.out.println("copyFileToDirectory:"+(copyLogFilePath));
					mailSent= sendMailLog("",copyLogFilePath+File.separator+logFileNames+".log",getDomString(emailAddr),false,userInfo);//userInfo added by nandkumar gadkari on 05/12/19
					//mailSent= sendMailLog("",copyLogFilePath+File.separator+mailLogFileName+".log",getDomString(emailAddr),false);//Modified by Ahmed on 13/06/2019
					if(mailSent)
					{
						printMailLog("Log file Mail Send succesfully to "+emailAddr +" to Login Code-"+loginCode, "Mail Send Successfully!"+refSer,"SUCCESS");
				        System.out.println("Succesfully send the mail");
					}else
					{
						printMailLog("Mail Send Failed to "+loginCode, "Mail Send Failed!"+refSer,"FAILED");//GENERATE LOG
				        System.out.println("failure while sending the mail");
					}
				}
				catch(Exception e)
				{
					System.out.println("failure while sending the mail EXCEPTION ");
					e.printStackTrace();
				}
			}
			System.out.println("error message return by the data:"+errString);
			if(errString.indexOf("CONFSUCCES") > -1 || errString.indexOf("Success") > -1 )
			{
				tranIdList.clear();
				conn.commit();
				errString = itmDBAccessEJB.getErrorString("","VPSUCC1","","",conn);
				System.out.println("inside the successfully insertion of the data");
			}
			else
			{
				//generatedTransactionId.clear();
				//itemList.clear();
				conn.rollback();
				System.out.println("#####Error String is" + errString);
				errString = itmDBAccessEJB.getErrorString("","VTEPFAIL","","",conn);	
			}
			/*}
			else
			{
				//generatedTransactionId.clear();
				conn.rollback();
				System.out.println("#####Error String is" + errString);
				errString = itmDBAccessEJB.getErrorString("","VTEPFAIL","","",conn);	
			}*/

		}
		catch (Exception sqx)
		{
			System.out.println("The SQLException occurs in EWayBillPrc GST HDR detail insertion :"+sqx);
			sqx.printStackTrace();
			//throw new ITMException(sqx);
		}
		finally{
			try{

			}
			catch(Exception e){}
		}

		detailList = dom2.getElementsByTagName( "Detail2" );
		noOfDetails	= detailList.getLength();


		return errString;
	}

	public String insertDataInGstHdrAndDetail(String tranId,String refSer,ArrayList list,Connection conn,String xtraParams)throws RemoteException, ITMException
	{
		String errString="";
		String sql="";
		//inserting data query inside GST hdr and GST detail so that we can use that data for JSON formation
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null,pstmt3=null;
		ResultSet rs = null,rs1=null,rs3=null ;
		String sqlExpr="",sqlExpr1="",sqlExpr2="",sqlExpr3="",sqlExpr4="",sqlExpr5="",sqlExpr6="",errfilePath="";
		String compSql="";
		String tranID="";//this variable holds the whole transaction id
		String windowName="w_ewaybill_prc";
		String keyString="",tranSer="",keyCol="";
		//GST_DATA_HDR variables...
		String tran_id="",tran_id__ref="",site_code="",prd_code="",rec_type="",
				tran_type="",cust_code="",cust_name="",submission_type="",doc_checksum="",doc_type="",doc_no="",reverse_chrg="",lr_no="",
				reas_code="",ref_id__inv="",prov_assmnt="",order_no="",remarks="",submit_status="",add_user="",
				add_term="",chg_user="",chg_term="",tax_reg_no="",reco_status="",ecom_reg_no="",ref_ser="",gst_code="",gst_type="",ref_id="";
		double amount=0.0;
		Timestamp docDate=null,lr_date=null,ref_date__inv=null,order_date=null,submit_date=null;
		Timestamp add_date=null,chg_date=null;
		String distance="",transporterName="",transId="",transMode="",transModes="",transDocNo="",transDocDate="",vehicleType="",vehicleNo="";
		Timestamp docDates=null;
		//GST_DATA_DET variables...
		String status="",line_type="",gs_code="",gs_descr="",unit="",supply_type="",itc_type="";
		int sr_no__old=0,line_no;		
		double taxable_amt=0.0,igst_perc=0.0,igst_amt=0.0,cgst_perc=0.0,cgst_amt=0.0,sgst_perc=0.0,sgst_amt=0.0;
		double quantity=0.0,cess_perc=0.0,cess_amt=0.0,itc_igst=0.0,itc_cgst=0.0,itc_sgst=0.0,itc_cess=0.0,gst_rate=0.0;
		String product_name="",productDesc="",hsnCode="",qtyUnit="";
		double prodtQty=0.0,taxableAmount=0.0,cessNonAdvol=1.0;//default value setted for cess non advol
		int  returnHdr=0,returnData=0;
		//GenericUtility genericUtility=GenericUtility.getInstance();
		int lineno=0;
		Transaction transaction=null;
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf  = null;
			java.util.Date currentDate = new java.util.Date();
			sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdfBillDate = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp tranDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
			System.out.println("Process for Transaction id ::"+tranId+" : "+refSer);
			System.out.println(list.size());
			HashMap transMap = (HashMap)list.get(0);
			Iterator<String> itr = transMap.keySet().iterator();
			while (itr.hasNext())
			{
				String key = itr.next();
				String value = (String) transMap.get(key);
				System.out.println(key + "=" + value);
			}
			distance=checkNull((String)transMap.get("distance"));
			transporterName=checkNull((String)transMap.get("trans_name"));
			transMode=checkNull((String)transMap.get("trans_mode"));
			transId=checkNull((String)transMap.get("trans_id"));
			transDocNo=checkNull((String)transMap.get("trans_doc"));
			transDocDate=checkNull((String)transMap.get("trans_doc_date"));
			vehicleType=checkNull((String)transMap.get("vehicle_type"));
			vehicleNo=checkNull((String)transMap.get("vehicle_no"));
			System.out.println("retrived value of the date for transaction is as:"+new SimpleDateFormat("dd/MM/yy").parse(transDocDate).toString());
			//converting value of transaction date into database formate form.
			Timestamp transsDocDate=java.sql.Timestamp.valueOf( sdf.format(new SimpleDateFormat("dd/MM/yy").parse(transDocDate))+" 00:00:00.0");
			System.out.println("transaction document date:"+transsDocDate);
			sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<site_code>" +site_code +"</site_code>";
			xmlValues = xmlValues + "<tran_date>"+tranDate +"</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranID = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			generatedTransactionId=tranID;
			System.out.println("tranId :"+tranID);
			transaction=new Transaction(tranID, refSer);
			

			if(refSer.trim().equals("S-INV")){
				lineno=1;
			}
			if(refSer.trim().equals("D-ISS")){
				lineno=2;
			}
			if(refSer.trim().equals("P-RET")){
				lineno=3;
			}
			if(refSer.trim().equals("C-ISS")){
				lineno=4;
			}

			sql="select line_no,sql_expr,sql_input,sql_expr1,sql_expr2,sql_expr3,sql_expr4,"
					+ " sql_expr5,sql_expr6 from tax_bal_grp_det where bal_group=? and ref_ser = '"+refSer+"' and line_no="+lineno+"";


			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, "EWAYH");
			rs=pstmt.executeQuery();
			while(rs.next()){
				sqlExpr= checkNull(rs.getString("sql_expr"));
				sqlExpr1= checkNull(rs.getString("sql_expr1"));
				sqlExpr2= checkNull(rs.getString("sql_expr2"));
				sqlExpr3= checkNull( rs.getString("sql_expr3"));
				sqlExpr4= checkNull(rs.getString("sql_expr4"));
				sqlExpr5= checkNull(rs.getString("sql_expr5"));
				sqlExpr6= checkNull(rs.getString("sql_expr6"));  


				compSql= sqlExpr+" "+sqlExpr1+" "+sqlExpr2+" "+sqlExpr3+" "+sqlExpr4+" "+sqlExpr5+" "+sqlExpr6;
				System.out.println("Combine SQL GST HDR ::::"+compSql);

				if(compSql!=null && compSql.trim().length() > 0 )
				{

					System.out.println("inside the insert GST HDR details");
					pstmt1=conn.prepareStatement(compSql);
					pstmt1.setString(1, tranId);
					rs1=pstmt1.executeQuery();
					while(rs1.next()){
						tran_id=rs1.getString("tran_id");
						tran_id__ref=rs1.getString("tran_id__ref");
						site_code=rs1.getString("site_code");
						prd_code=rs1.getString("prd_code");
						rec_type=rs1.getString("rec_type");
						gst_type=rs1.getString("gst_type");
						tran_type=rs1.getString("tran_type");
						cust_code=rs1.getString("cust_code");
						cust_name=rs1.getString("cust_name");
						gst_code=rs1.getString("gst_code");
						submission_type=rs1.getString("submission_type");
						doc_checksum=rs1.getString("doc_checksum");
						doc_type=rs1.getString("doc_type");
						doc_no=rs1.getString("doc_no");
						amount=rs1.getDouble("amount");
						reverse_chrg=rs1.getString("reverse_chrg");
						lr_no=rs1.getString("lr_no");
						reas_code=rs1.getString("reas_code");
						ref_id__inv=rs1.getString("ref_id__inv");
						prov_assmnt=rs1.getString("prov_assmnt");
						order_no=rs1.getString("order_no");
						remarks=rs1.getString("remarks");
						ref_id=rs1.getString("ref_id");
						submit_status=rs1.getString("submit_status");
						add_user=rs1.getString("add_user");
						add_term=rs1.getString("add_term");
						chg_user=rs1.getString("chg_user");
						chg_term=rs1.getString("chg_term");
						tax_reg_no=rs1.getString("tax_reg_no");
						reco_status=rs1.getString("reco_status");
						ecom_reg_no=rs1.getString("ecom_reg_no");
						ref_ser=rs1.getString("ref_ser");
						docDates=rs1.getTimestamp("doc_date");

						//mapping attribute with DocType
						sql="select descr from genmst where fld_name=?";
						pstmt3 = conn.prepareStatement(sql);
						pstmt3.setString(1, doc_type.trim());
						rs3=pstmt3.executeQuery();
						if(rs3.next()){
							doc_type=checkNull(rs3.getString("descr"));
						}   
						pstmt3.close();
						pstmt3=null;
						rs3.close();
						rs3=null;

						//mapping attribute with transMode
						sql="select descr from genmst where fld_name=? ";
						pstmt3 = conn.prepareStatement(sql);
						pstmt3.setString(1, transMode.trim());
						rs3=pstmt3.executeQuery();
						if(rs3.next()){
							transModes=checkNull(rs3.getString("descr"));
						}
						pstmt3.close();
						pstmt3=null;
						rs3.close();
						rs3=null;

						//inserting above details inside GST_DATA_HDR tables....

						sql="insert into GST_DATA_HDR(tran_id,tran_date,tran_id__ref,site_code,prd_code,rec_type,"
								+ "tran_type,cust_code,cust_name,submission_type,doc_checksum,doc_type,doc_no,doc_date,amount,reverse_chrg,lr_no,"
								+ "lr_date,reas_code,ref_id__inv,ref_date__inv,prov_assmnt,order_no,order_date,remarks,submit_status,submit_date,add_user,add_date,"
								+ "add_term,chg_user,chg_date,chg_term,tax_reg_no,reco_status,ecom_reg_no,ref_ser,gst_code,gst_type,ref_id,distance,trans_code,trans_mode,trans_id,trans_doc_id,vehicle_type,trans_doc_date,vehicle_no"
								+ ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

						System.out.println("insertSql : "+sql);
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, tranID);
						pstmt2.setTimestamp(2, tranDate);
						pstmt2.setString(3, tran_id__ref);
						pstmt2.setString(4, site_code);
						pstmt2.setString(5, prd_code);
						pstmt2.setString(6, rec_type);
						pstmt2.setString(7, tran_type);
						pstmt2.setString(8, cust_code);
						pstmt2.setString(9, cust_name);
						pstmt2.setString(10, submission_type);
						pstmt2.setString(11, doc_checksum);
						pstmt2.setString(12, doc_type);
						pstmt2.setString(13, doc_no);
						pstmt2.setTimestamp(14, docDates);
						pstmt2.setDouble(15, amount);
						pstmt2.setString(16, reverse_chrg);
						pstmt2.setString(17, lr_no);
						pstmt2.setTimestamp(18, lr_date);
						pstmt2.setString(19, reas_code);
						pstmt2.setString(20, ref_id__inv);
						pstmt2.setTimestamp(21, ref_date__inv);
						pstmt2.setString(22, prov_assmnt);
						pstmt2.setString(23, order_no);
						pstmt2.setTimestamp(24, order_date);
						pstmt2.setString(25, remarks);
						pstmt2.setString(26, submit_status);
						pstmt2.setTimestamp(27, submit_date);
						pstmt2.setString(28, add_user);
						pstmt2.setTimestamp(29, add_date);//add_term,chg_user,chg_date,chg_term,tax_reg_no,reco_status,ecom_reg_no,ref_ser,gst_code,gst_type,ref_id
						pstmt2.setString(30, add_term);
						pstmt2.setString(31, chg_user);
						pstmt2.setTimestamp(32, chg_date);
						pstmt2.setString(33, chg_term);
						pstmt2.setString(34, tax_reg_no);
						pstmt2.setString(35, reco_status);
						pstmt2.setString(36, ecom_reg_no);
						pstmt2.setString(37, ref_ser);
						pstmt2.setString(38, gst_code);
						pstmt2.setString(39, gst_type);
						pstmt2.setString(40,ref_id);
						pstmt2.setDouble(41, Double.parseDouble(distance));
						pstmt2.setString(42,transporterName);
						pstmt2.setString(43,checkNull(transModes));
						pstmt2.setString(44,transId);
						pstmt2.setString(45,transDocNo);
						pstmt2.setString(46,vehicleType);
						pstmt2.setTimestamp(47, transsDocDate); 	
						pstmt2.setString(48,vehicleNo);

						int a= pstmt2.executeUpdate();
						if(a==0){
							System.out.println("Process failed INSERT into ..................");
							errString="VTEPFAIL";
							//return errString;
						}
						else{
							System.out.println("Process sucessfully INSERT into ..................");
							errString="CONFSUCCES";
						}
						//closing the prepared statment here for both the insert statement...
						pstmt2.close();
						pstmt2=null;

						returnHdr++;
						System.out.println("returnHdr::::"+returnHdr);
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;

					/*if(returnHdr<=0){
						printLog("E-way Bill Failed", "No Data Found for Inserting in GST HEADER details for Transaction Id:"+tranId,"ERROR");
					}*/

				}
				/*sql="insert into GST_DATA_HDR(tran_id,tran_date,tran_id__ref,site_code,prd_code,rec_type,"
					+ "tran_type,cust_code,cust_name,submission_type,doc_checksum,doc_type,doc_no,doc_date,amount,reverse_chrg,lr_no,"
					+ "lr_date,reas_code,ref_id__inv,ref_date__inv,prov_assmnt,order_no,order_date,remarks,submit_status,submit_date,add_user,add_date,"
					+ "add_term,chg_user,chg_date,chg_term,tax_reg_no,reco_status,ecom_reg_no,ref_ser,gst_code,gst_type,ref_id"
					+ ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			sql="insert into GST_DATA_DET() values()";*/

			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if(returnHdr<=0){
				printLog("E-way Bill Failed", "No Data Found for Inserting in GST HEADER details for Transaction Id:"+tranId,"ERROR");
			}

			//DETAIL BLOCKS START FROM HERE....
		if(returnHdr>0)
		{
			sql="select line_no,sql_expr,sql_input,sql_expr1,sql_expr2,sql_expr3,sql_expr4,"
					+ " sql_expr5,sql_expr6 from tax_bal_grp_det where bal_group=? and ref_ser = '"+refSer+"' and line_no="+lineno+"";

			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, "EWAYD");
			rs=pstmt.executeQuery();
			while(rs.next()){
				sqlExpr= checkNull(rs.getString("sql_expr"));
				sqlExpr1= checkNull(rs.getString("sql_expr1"));
				sqlExpr2= checkNull(rs.getString("sql_expr2"));
				sqlExpr3= checkNull( rs.getString("sql_expr3"));
				sqlExpr4= checkNull(rs.getString("sql_expr4"));
				sqlExpr5= checkNull(rs.getString("sql_expr5"));
				sqlExpr6= checkNull(rs.getString("sql_expr6"));  


				compSql= sqlExpr+" "+sqlExpr1+" "+sqlExpr2+" "+sqlExpr3+" "+sqlExpr4+" "+sqlExpr5+" "+sqlExpr6;
				System.out.println("Combine SQL GST DET ::::"+compSql);

				if(compSql!=null && compSql.trim().length() > 0 )
				{
					System.out.println("inside the insert GST DET details");
					pstmt1=conn.prepareStatement(compSql);
					pstmt1.setString(1, tranId);
					rs1=pstmt1.executeQuery();
					while(rs1.next()){
						tran_id=rs1.getString("tran_id");
						line_no=rs1.getInt("line_no");
						status=rs1.getString("status");
						sr_no__old=rs1.getInt("sr_no__old");
						line_type=rs1.getString("line_type");
						gs_code=rs1.getString("gs_code");
						taxable_amt=rs1.getDouble("taxable_amt");
						igst_perc=rs1.getDouble("igst_perc");
						igst_amt=rs1.getDouble("igst_amt");
						cgst_perc=rs1.getDouble("cgst_perc");
						cgst_amt=rs1.getDouble("cgst_amt");
						sgst_perc=rs1.getDouble("sgst_perc");
						sgst_amt=rs1.getDouble("sgst_amt");
						gs_descr=rs1.getString("gs_descr");
						unit=rs1.getString("unit");
						quantity=rs1.getDouble("quantity");
						supply_type=rs1.getString("supply_type");
						cess_perc=rs1.getDouble("cess_perc");
						cess_amt=rs1.getDouble("cess_amt");
						remarks=rs1.getString("remarks");
						gst_rate=rs1.getDouble("gst_rate");
						product_name=rs1.getString("item_name");
						productDesc=rs1.getString("item_name");
						hsnCode=rs1.getString("hsn_code");
						/*prodtQty=rs1.getDouble("item_quantity");
								qtyUnit=rs1.getString("unit");
								taxableAmount=rs1.getDouble("taxable_amount");*/


						//inserting the value inside GST_DATA_DET table..... 
						sql="insert into GST_DATA_DET(tran_id,line_no,status,sr_no__old,line_type,gs_code,taxable_amt,igst_perc,igst_amt,cgst_perc,cgst_amt,sgst_perc,sgst_amt,"
								+ "gs_descr,unit,quantity,supply_type,cess_perc,cess_amt,remarks,itc_type,itc_igst,itc_cgst,itc_sgst,itc_cess,gst_rate)"
								+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						pstmt2 = conn.prepareStatement(sql);
						System.out.println("insertSql : "+sql);
						pstmt2.setString(1, tranID);
						pstmt2.setInt(2, line_no);
						pstmt2.setString(3, status);
						pstmt2.setInt(4, sr_no__old);
						pstmt2.setString(5,line_type);
						pstmt2.setString(6,gs_code);
						pstmt2.setDouble(7,taxable_amt);
						pstmt2.setDouble(8,igst_perc);
						pstmt2.setDouble(9,igst_amt);
						pstmt2.setDouble(10,cgst_perc);
						pstmt2.setDouble(11,cgst_amt);
						pstmt2.setDouble(12,sgst_perc);
						pstmt2.setDouble(13,sgst_amt);
						pstmt2.setString(14,gs_descr);
						pstmt2.setString(15,unit);
						pstmt2.setDouble(16,quantity);
						pstmt2.setString(17,supply_type);
						pstmt2.setDouble(18,cess_perc);
						pstmt2.setDouble(19,cess_amt);
						pstmt2.setString(20,remarks);
						pstmt2.setString(21,itc_type);
						pstmt2.setDouble(22,itc_igst);
						pstmt2.setDouble(23,itc_cgst);
						pstmt2.setDouble(24,itc_sgst);
						pstmt2.setDouble(25,itc_cess);
						pstmt2.setDouble(26,gst_rate);

						int a= pstmt2.executeUpdate();
						if(a==0){
							System.out.println("Process failed INSERT into ..................");
							errString="VTEPFAIL";
							//return errString;
						}
						else{
							System.out.println("Process sucessfully INSERT into ..................");
							errString="CONFSUCCES";
						}
						//closing the prepared statment here for both the insert statement...
						pstmt2.close();
						pstmt2=null;

						returnData++;   
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;


					if(returnData<=0){
						printLog("E-way Bill Failed", "No Data Found for Inserting in GST DETAIL details for Transaction Id:"+tranId,"ERROR");
						errString="VTEPFAIL";
					}
					else{
						if(validateJSONFileForEway(transaction,site_code,conn,xtraParams)){//xtraParams added bu nandkumar on 05/12/19
							tranIdList.add(transaction);
							conn.commit();
						}
						else{
							conn.rollback();
						}
					}
				}
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
		}
		else{
			printLog("E-way Bill Failed", "No Data Found for Inserting in GST DETAIL Detail for Transaction Id:"+tranId,"ERROR");
		}
			//closing result set and prepared statement

		}
		catch (SQLException e)
		{
			printLog("E-way Bill Failed", "SQL Sytax is Incorrect for Ref-Ser :"+refSer,"ERROR");
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try{
				if(errString.equals("VTEPFAIL")){
					conn.rollback();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null)//if codn added by Jiten 19/05/06
				{
					rs.close();
					rs = null;
				}
				if(pstmt2!=null){
					pstmt2.close();
					pstmt2 = null;
				}
				if (rs1 != null)//if codn added by Jiten 19/05/06
				{
					rs1.close();
					rs1 = null;
				}
			}
			catch(Exception e)
			{
				System.err.println("Exception :DBAccessEJB :getITMVersion :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}


		return errString;
	}

	//generating JSON file for EWAY-BILL
	public String generateJSONFileForEway(List<Transaction> tranId,String siteCode,String xtraParams,Connection con)throws RemoteException, ITMException{

		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null ;
		String sql="",refSer="E-WAY",refId="";
		String sqlExpr="",sqlExpr1="",sqlExpr2="",sqlExpr3="",sqlExpr4="",sqlExpr5="",sqlExpr6="",errfilePath="";
		String compSql="",compSql1="",compSql2="",compSql3="";
		//variables which are holding the data for displaying the 
		String userGstin="",supplyType = "", docType="",docNo="",fromGstin="",toGstin="",fromTrdName="",
				fromAddr1="",fromAddr2="",fromPlace="",docDateValue="";
		String toTrdName="",toAddr1="",toAddr2="",toPlace="";
		Timestamp docDate=null,transDocDate=null;
		int frompinCode=0,toPincode=0,transType=0,subSupplyType = 0,actualFromStateCode=0,tostateCode=0,actualToStateCode=0,transMode=0,mainHsnCode=0;
		
		double transDistance=0.0;
		String sundryType="",transporterName="",transporterId="",transDocNo="",vehicleNo="",vehicleType="",assesable_value="";
		//item details data holder values
		int itemNo=0,quantity=0,cessNonAdvol=0,fromstateCode=0;
		String productName="",productDescr="",qtyUnit="",hsnCode = "";
		double sgstRate=0.0,cgstRate=0.0,igstRate=0.0,cessRate=0.0,taxableAmount=0.0;
		double totalAssesableValue=0.0,totalValue=0.0,cgstValue=0.0,sgstValue=0.0,igstValue=0.0,cessValue=0.0,totNonAdvolVal=0.0,othValue=0.0,totInvValue=0,distance=0.0;
		String errString="";
		//data holder list which will use in creation of the JSON file data
		List<EwayBillValue> list=new ArrayList<EwayBillValue>();
		int valueCounter=0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		int index=0;
		String transIdComma="";
		String siteCodeArr[] = null;
		String parameters = "";
		boolean flag=true;
		String groupByQuery="";
		//lets use some dummy data for getting values inside the json file....
		DistCommon distCommon = new DistCommon();
		String dateFormat="";
		String filePath="";
		String logPath="";
		String jsonFileVersion="";
		String count="";
		String transactionDocDate="";
		String transporterid="";
		File orgFilePath=null;
		File logFilePath=null;
		File copyFilePath=null;
		File copyLogFilePath=null;
		//String loginCode="";
		String transInfo="";
		String emailAddr="";
		boolean mailSent;
		//List<ItemDetails> itemList=new ArrayList<ItemDetails>();
		Map<String,Boolean> validatorMap=new HashMap<String,Boolean>();
		List<Boolean> validatorList=new ArrayList<Boolean>();
		try{
			
			E12GenericUtility genericUtility= new  E12GenericUtility();
			DBAccessEJB dbAccess = new DBAccessEJB();//added by nandkumar gadkari on 05/12/19
			UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);//added by nandkumar gadkari on 05/12/19
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			//getting date format for ewaybill
			dateFormat=distCommon.getDisparams("999999", "EWAY_DATE_FORM", con);
			//getting file path for ewaybill
			filePath=distCommon.getDisparams("999999", "EWAY_FILE_PATH", con);
			//getting version of the json file
			jsonFileVersion=distCommon.getDisparams("999999", "EWAY_JSON_VERSION", con);
			
			setLogFile(con);//Modified by Ahmed on 13/06/2019 to set log path


			System.out.println("Ewaybill date Format is:"+dateFormat);

			System.out.println("generatedTransactionId:"+tranId);

			for(int i=0;i<tranId.size();i++){
				if(i==tranId.size()-1)
				{
					transIdComma=transIdComma+(""+tranId.get(i).getTranId()+"");
				}
				else
				{
					transIdComma=transIdComma+(""+tranId.get(i).getTranId()+",");
				}
			}

			//System.out.println("generatedTransactionId:list:"+generatedTransactionId+" "+" "+ generatedTransactionId.size()+" "+ generatedTransactionId.get(index));

			StringBuffer siteCodeBuffr = new StringBuffer("(");
			int siteCount=0;
			if(transIdComma.length()>0){
				siteCodeArr	= transIdComma.split(",");
				for( String userSiteArr : siteCodeArr )
				{
					siteCount++;
					System.out.println("EwayBillPrc:::wfValData:::siteCodeArr is "+userSiteArr);
					siteCodeBuffr.append("?,");
				}

				if(siteCodeBuffr.lastIndexOf(",") == siteCodeBuffr.length() - 1)
				{
					siteCodeBuffr.deleteCharAt( siteCodeBuffr.length() - 1 );
					parameters = siteCodeBuffr.append("").toString();
					System.out.println("EwayBillPrc:::parameters:::"+parameters);
				}
				else
				{
					parameters = siteCodeBuffr.append("").toString();
				}
			}

			System.out.println("Parameters is:"+parameters);


			for(int i=0;i<tranId.size();i++){
				try
				{


					sql="select line_no,sql_expr,sql_input,sql_expr1,sql_expr2,sql_expr3,sql_expr4,"
							+ " sql_expr5,sql_expr6 from tax_bal_grp_det where bal_group=? and ref_ser = '"+tranId.get(i).getRefSer()+"'";

					pstmt=con.prepareStatement(sql);
					pstmt.setString(1, "EWAYJ");
					rs=pstmt.executeQuery();
					while(rs.next()){
						sqlExpr= checkNull(rs.getString("sql_expr"));
						sqlExpr1= checkNull(rs.getString("sql_expr1"));
						sqlExpr2= checkNull(rs.getString("sql_expr2"));
						sqlExpr3= checkNull( rs.getString("sql_expr3"));
						sqlExpr4= checkNull(rs.getString("sql_expr4"));
						sqlExpr5= checkNull(rs.getString("sql_expr5"));
						sqlExpr6= checkNull(rs.getString("sql_expr6"));  


						compSql= sqlExpr+" "+sqlExpr1+" "+sqlExpr2+" "+sqlExpr3+" "+sqlExpr4+" "+sqlExpr5+" "+sqlExpr6;



						//parameters=parameters+groupByQuery();

						compSql1=compSql.substring(0, compSql.indexOf("group"));
						compSql2=compSql.substring(compSql.indexOf("group"));

						compSql=compSql1+(" = '"+tranId.get(i).getTranId())+"'"+compSql2;
						System.out.println("Combine SQL JSON FILE::::"+(compSql));
						String temp="";
						if(compSql != null && compSql.trim().length() > 0 )
						{
							System.out.println("inside JSON retrieval query"+compSql);
							pstmt1=con.prepareStatement(compSql);
							//pstmt1.setString(1, tranId.get(0));
							/*
							int indexi = 1;
							if(transIdComma.contains(","))
							{
								for(String userSiteArr : siteCodeArr)
								{
									System.out.println("Withhold:::siteCode:::"+userSiteArr);
									pstmt1.setString(indexi++, userSiteArr.trim());
								}
							}
							else
							{*/
							//pstmt1.setString(1, tranId.get(i).getTranId().trim());

							rs1 = pstmt1.executeQuery();
							List<ItemDetails> itemList=null;
							while(rs1.next())
							{
								//getting data from GST HDR and GST DET tables for generating the JSON file for EWAY
								//from detail part
								System.out.println("inside the loop of EWAY JSON file");

								if(!temp.equalsIgnoreCase(rs1.getString("ref_id"))){
									itemList=new ArrayList<ItemDetails>();
									valueCounter=0;
								}



								refSer=rs1.getString("ref_ser");
								refId=rs1.getString("ref_id");
								userGstin=rs1.getString("from_gstin");
								supplyType=rs1.getString("supply_type");
								//subSupplyType=rs1.getString("sub_type");
								subSupplyType=rs1.getInt("sub_type");
								docType=rs1.getString("doc_type");
								docNo=rs1.getString("doc_no");
								docDate=rs1.getTimestamp("doc_date"); //this will be in date format
								transType = rs1.getInt("trans_type");
								fromGstin=rs1.getString("from_gstin");
								fromTrdName=rs1.getString("from_party_descr");//from party description.
								fromAddr1=rs1.getString("from_address1");
								fromAddr2=rs1.getString("from_address2");
								fromPlace=rs1.getString("from_place");
								frompinCode=rs1.getInt("from_pincode");
								fromstateCode=rs1.getInt("from_state_code");
								actualFromStateCode=rs1.getInt("from_state_code");

								//to detail part
								toGstin=rs1.getString("to_gstin");
								toTrdName=rs1.getString("to_party_descr");
								toAddr1=rs1.getString("to_address1");
								toAddr2=rs1.getString("to_address2");
								toPlace=rs1.getString("to_place");
								toPincode=rs1.getInt("to_pin_code");
								tostateCode=rs1.getInt("to_state_code");
								actualToStateCode=rs1.getInt("to_state_code");
								totInvValue=rs1.getDouble("total_invoice_amt");//here taken assessable value as int instead of double
								cgstValue=rs1.getDouble("cgst_amt");
								sgstValue=rs1.getDouble("sgst_amt");
								igstValue=rs1.getDouble("igst_amt");
								cessValue=rs1.getDouble("cess_amt");
								totalValue=rs1.getDouble("assessable_value");
								totalAssesableValue=rs1.getDouble("tot_assessable_value");
								transMode=rs1.getInt("trans_mode");
								//transporterName=rs1.getString("trans_code");
								//transporterName="ABC";
								transporterName=(rs1.getString("trans_code")==null || rs1.getString("trans_code").trim().length() == 0) ? "ABC" : rs1.getString("trans_code").trim();
								transporterid=rs1.getString("trans_id");
								transDocNo=rs1.getString("trans_doc_id");
								transDocDate=rs1.getTimestamp("trans_doc_date");
								sundryType=rs1.getString("sundry_type");
								vehicleType=rs1.getString("vehicle_type");
								vehicleNo=rs1.getString("vehicle_no");
								mainHsnCode=rs1.getInt("hsn");
								transDistance=rs1.getDouble("distance");
								cgstRate=rs1.getDouble("cgst_perc");
								sgstRate=rs1.getDouble("sgst_perc");
								igstRate=rs1.getDouble("igst_perc");
								cessRate=rs1.getDouble("cess_perc");

								transactionDocDate = genericUtility.getValidDateString(transDocDate.toString(), genericUtility.getDBDateFormat(), dateFormat);



								docDateValue=genericUtility.getValidDateString(docDate.toString(), genericUtility.getDBDateFormat(), dateFormat);								


								valueCounter++;	

								ItemDetails itemDetails=new ItemDetails(valueCounter,"","", mainHsnCode, 0, "",totalValue,sgstRate,cgstRate, igstRate, cessRate,0.0);

								itemList.add(itemDetails);
								System.out.println("counter:"+valueCounter+" Size ItemList:"+list.size());



								EwayBillValue ewayBillValue=new EwayBillValue(
										userGstin, supplyType, subSupplyType, docType, docNo, docDateValue,transType, fromGstin, fromTrdName,
										fromAddr1,fromAddr2,fromPlace,frompinCode,fromstateCode,actualFromStateCode,
										toGstin,toTrdName,toAddr1,toAddr2,toPlace,toPincode,tostateCode,actualToStateCode,totalAssesableValue,
										cgstValue,sgstValue,igstValue,cessValue,0,0,totInvValue,transMode,transDistance,transporterName,transporterid,transDocNo,transactionDocDate,sundryType,vehicleType,vehicleNo,mainHsnCode,itemList,refSer,refId);

								if(temp.equalsIgnoreCase(refId))
								{
									list.remove(list.size()-1);
								}
								list.add(ewayBillValue);
								//validatorList.add(ewayBillValue.validateFields(refSer, refId));
								System.out.println("Validation flag is:"+flag);
								System.out.println("counter:"+valueCounter+" Size ValueList:"+list.size());
								temp=rs1.getString("ref_id");
								
							}
							rs1.close();
							rs1=null;
							pstmt1.close();
							pstmt1=null;
							//tranId.clear();
							//itemList.clear();
							//generatedTransactionId.clear();
						}else{
							//tranId.clear();
							//itemList.clear();
							// generatedTransactionId.clear();
						}

					}
				}
				catch(Exception e)
				{
					System.out.println("counter JSON FILE exception");
					e.printStackTrace();
					throw new ITMException(e);
					
				}
			}
			if(rs!=null){
				rs.close();
				rs=null;
			}
			if(pstmt!=null){
				pstmt.close();
				pstmt=null;
			}

			System.out.println("counter value is :"+valueCounter);


			if(valueCounter>0){
				SimpleDateFormat sdf  = new SimpleDateFormat(genericUtility.getDBDateFormat());
				java.util.Date currentDate = new java.util.Date();
				String todaysDate=sdf.format(currentDate);

				System.out.println("File date is :"+todaysDate);

				Calendar cal = Calendar.getInstance();
				count=cal.get(Calendar.YEAR)+""+cal.get(Calendar.DATE)+""+((cal.get(Calendar.MONTH))+1)+""+cal.get(Calendar.HOUR)+""+cal.get(Calendar.MINUTE)
				+""+cal.get(Calendar.SECOND);

				String filename=siteCode+"_"+count;
				ObjectMapper mapper = new ObjectMapper();
//added by nandkumar 
				try{
					File fileFolder=new File( CommonConstants.JBOSSHOME +File.separator+filePath+File.separator);//  CommonConstants.JBOSSHOME + added by nandkumar gadkari on 05/12/19
					//check for file validation...this will check the provided file path is correct or not
					if (! fileFolder.exists()){
						/*errString="VTEPFAIL";
						printLog("E-way Bill Failed", "Provided File Path is Incorrect ","ERROR");
						con.rollback();
						return errString;*/ //commneted by nandkumar gadkari on 05/12/19
						printLog("E-way Bill Failed", "Provided File Path is Incorrect ","ERROR");
						fileFolder.mkdirs();
						
					}
				}
				catch(Exception e){
					printLog("E-way Bill Failed", "Provided File Path is Incorrect ","ERROR");
				}
				
				File file = new File( CommonConstants.JBOSSHOME +File.separator+filePath+File.separator+filename+".json");//bring this path from disparm value//  CommonConstants.JBOSSHOME + added by nandkumar gadkari on 05/12/19

				/*try{
					File fileFolder=new File( CommonConstants.JBOSSHOME +File.separator+filePath+File.separator);//  CommonConstants.JBOSSHOME + added by nandkumar gadkari on 05/12/19
					//check for file validation...this will check the provided file path is correct or not
					if (! fileFolder.exists()){
						errString="VTEPFAIL";
						printLog("E-way Bill Failed", "Provided File Path is Incorrect ","ERROR");
						con.rollback();
						return errString; //commneted by nandkumar gadkari on 05/12/19
						printLog("E-way Bill Failed", "Provided File Path is Incorrect ","ERROR");
						fileFolder.mkdirs();
						
					}
				}
				catch(Exception e){
					printLog("E-way Bill Failed", "Provided File Path is Incorrect ","ERROR");
				}*/// commented by nandkumar gadkari on 05/12/19
				
				//check for field validation..flag will returns the false value if any invalid details found

				System.out.println("Validation List:"+validatorList);


			/*	if(validatorList.contains(false)){
					errString="VTEPFAIL";
					printLog("E-way Bill Failed", "Please Enter Valid Details"+refSer,"ERROR");
					con.rollback();
					return errString;
				}*/	 

				EwayBillJson json=new EwayBillJson(jsonFileVersion, list);  
				//Modified by Ahmed on 10-08-2019[to display indented json and replace .0 value][start]
				//mapper.writeValue(new File(filePath+File.separator+filename+".json"), json);
				String jsonString = mapper.writeValueAsString(json);
				jsonString = jsonString.replace(".0,", ",");
				jsonString = jsonString.replace(".0}", "}");
				//Object jsonObject = mapper.readValue(jsonString, Object.class);
				//jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
				//String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
				//jsonString = jsonString.replace(".0", "");
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(jsonString);
				if (fileWriter != null) 
				{
					fileWriter.flush();
					fileWriter.close();
				}
				//Modified by Ahmed on 10-08-2019[end]
				errString="CONFSUCCES";
				//itemList.clear();


				if(errString.equals("CONFSUCCES"))
				{
					loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
					System.out.println("loginCode:"+loginCode);
					
					//getting email id
					try{
						sql="select email_id from users where code= ? ";
						pstmt=con.prepareStatement(sql);
						pstmt.setString(1, loginCode.trim());
						rs=pstmt.executeQuery();
						while(rs.next()){
							emailAddr=rs.getString("email_id");
						}
	
					}
					catch(SQLException e){
						e.printStackTrace();
					}
					if(rs!=null){
						rs.close();
						rs=null;
					}
					if(pstmt!=null){
						pstmt.close();
						pstmt=null;
					}
					
					
					if(emailAddr!=null && emailAddr.trim().length()>0){
						transInfo = getDomString(emailAddr);

						copyFilePath = new File(CommonConstants.JBOSSHOME +File.separator+filePath + File.separator +"temp");//CommonConstants.JBOSSHOME + added by nandkumar gadkari on 05/12/19
						if(!copyFilePath.exists()) {
							copyFilePath.mkdir();
						}
						else if(copyFilePath.exists())
						{
							try
							{
								FileUtils.cleanDirectory(copyFilePath);
							}
							catch(Exception exp)
							{
								exp.printStackTrace();
							}
						}
						
						//log file
						copyLogFilePath = new File(CommonConstants.JBOSSHOME +File.separator+logDires + File.separator +"temp");
						if(!copyLogFilePath.exists()) {
							copyLogFilePath.mkdir();
						}
						else if(copyLogFilePath.exists())
						{
							try
							{
								FileUtils.cleanDirectory(copyLogFilePath);
							}
							catch(Exception exp)
							{
								exp.printStackTrace();
							}
						}
						for(int i=0;i<tranId.size();i++){
							printLog("E-way Bill Generated", "E_WAY bill Generated for Id "+tranId.get(i).getTranId()+" Of Ref-Ser:"+tranId.get(i).getRefSer(),"SUCCESS");
						}
						
						
						
						if(filename != null && filename.trim().length() > 0)
						{
							orgFilePath=new File(CommonConstants.JBOSSHOME +File.separator+filePath+File.separator+filename+".json");
							
							FileUtils.copyFileToDirectory(orgFilePath, copyFilePath);
							
							/*logFilePath=new File(logDires+File.separator+logFileNames+".log");
							
							FileUtils.copyFileToDirectory(logFilePath, copyLogFilePath);*/
							
							//send JSon File....
							mailSent= sendMail("",copyFilePath+File.separator+filename+".json",transInfo,true,userInfo);//userInfo added by nandkumar gadkari on 05/12/19
							
							if(mailSent){
								printMailLog("JSON file Mail Send succesfully to "+emailAddr, "Mail Send Successfully!"+refSer,"SUCCESS");
						        System.out.println("Succesfully send the mail");
							}else{
								printMailLog("Mail Send Failed to "+emailAddr, "Mail Send Failed!"+refSer,"ERROR");//GENERATE LOG
						        System.out.println("failure while sending the mail");
							}
							
							//send Log File....
							
							/*mailSent= sendMailLog("",copyLogFilePath+File.separator+logFileNames+".log",transInfo,true);
							
							if(mailSent){
								printMailLog("Log file Mail Send succesfully to "+emailAddr, "Mail Send Successfully!"+refSer,"SUCCESS");
						        System.out.println("Succesfully send the mail");
							}else{
								printMailLog("Mail Send Failed to "+emailAddr, "Mail Send Failed!"+refSer,"ERROR");//GENERATE LOG
						        System.out.println("failure while sending the mail");
							}*/
							
							
						}
						else{
							printMailLog("File Not Found", "File Not Found for Copying the Files into Destination folder "+refSer,"ERROR");
						}
						
						//send mail for log file
						logFilePath=new File(CommonConstants.JBOSSHOME +logDires+File.separator+logFileNames+".log");
						//logFilePath=new File(logDires+File.separator+mailLogFileName+".log");
						try{
							FileUtils.copyFileToDirectory(logFilePath, copyLogFilePath);
							
							mailSent= sendMailLog("",copyLogFilePath+File.separator+logFileNames+".log",transInfo,false,userInfo);//userInfo added by nandkumar gadkari on 05/12/19
							//mailSent= sendMailLog("",copyLogFilePath+File.separator+mailLogFileName+".log",transInfo,false);
							
							if(mailSent){
								printMailLog("Log file Mail Send succesfully to "+emailAddr +" to Login Code-"+loginCode, "Mail Send Successfully!"+refSer,"SUCCESS");
						        System.out.println("Succesfully send the mail");
							}else{
								printMailLog("Mail Send Failed to "+loginCode, "Mail Send Failed!"+refSer,"FAILED");//GENERATE LOG
						        System.out.println("failure while sending the mail");
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					else{
						printMailLog("Email Id Not Found for Login Id-"+loginCode, "Please Enter the email id for given Login Code!"+refSer,"FAILED");
					}
				}
				
				/*for(int i=0;i<tranId.size();i++){
					printLog("E-way Bill Generated", "E_WAY bill Generated for Id "+tranId.get(i).getTranId()+" Of Ref-Ser:"+tranId.get(i).getRefSer(),"SUCCESS");
				}*/


				//clear the list
				tranId.clear();
			}
		}
		catch (SQLException e)
		{
			printLog("E-way Bill Failed", "SQL Sytax is Incorrect for Ref-Ser :"+refSer,"ERROR");
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally {
			tranId.clear();
		}

		return errString;
	}

	
	public boolean validateJSONFileForEway(Transaction tranId,String siteCode,Connection con,String xtraParams)throws RemoteException, ITMException{

		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null ;
		String sql="",refSer="E-WAY",refId="";
		String sqlExpr="",sqlExpr1="",sqlExpr2="",sqlExpr3="",sqlExpr4="",sqlExpr5="",sqlExpr6="",errfilePath="";
		String compSql="",compSql1="",compSql2="",compSql3="";
		//variables which are holding the data for displaying the 
		String userGstin="",supplyType="",docType="",docNo="",fromGstin="",toGstin="",fromTrdName="",
				fromAddr1="",fromAddr2="",fromPlace="",docDateValue="";
		String toTrdName="",toAddr1="",toAddr2="",toPlace="";
		Timestamp docDate=null,transDocDate=null;
		int frompinCode=0,toPincode=0,transType=0,fromstateCode=0,actualFromStateCode=0,tostateCode=0,actualToStateCode=0,mainHsnCode=0,transMode=0;
		
		double transDistance=0.0;
		String sundryType="",transporterName="",transporterId="",transDocNo="",vehicleNo="",vehicleType="",assesable_value="";
		//item details data holder values
		int itemNo=0,quantity=0,cessNonAdvol=0,subSupplyType=0;
		String productName="",productDescr="",qtyUnit="",hsnCode = "";
		double sgstRate=0.0,cgstRate=0.0,igstRate=0.0,cessRate=0.0,taxableAmount=0.0;
		double totalAssesableValue=0.0,totalValue=0.0,cgstValue=0.0,sgstValue=0.0,igstValue=0.0,cessValue=0.0,totNonAdvolVal=0.0,othValue=0.0,totInvValue=0,distance=0.0;
		String errString="";
		//data holder list which will use in creation of the JSON file data
		List<EwayBillValue> list=new ArrayList<EwayBillValue>();
		int valueCounter=0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		int index=0;
		String transIdComma="";
		String siteCodeArr[] = null;
		String parameters = "";
		boolean flag=true;
		String groupByQuery="";
		//lets use some dummy data for getting values inside the json file....
		DistCommon distCommon = new DistCommon();
		String dateFormat="";
		String filePath="";
		String logPath="";
		String jsonFileVersion="";
		String count="";
		String transactionDocDate="";
		String transporterid="";
		//List<ItemDetails> itemList=new ArrayList<ItemDetails>();
		Map<String,Boolean> validatorMap=new HashMap<String,Boolean>();
		List<Boolean> validatorList=new ArrayList<Boolean>();
		try{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			//getting date format for ewaybill
			dateFormat=distCommon.getDisparams("999999", "EWAY_DATE_FORM", con);
			//getting file path for ewaybill
			filePath=distCommon.getDisparams("999999", "EWAY_FILE_PATH", con);
			//getting version of the json file
			jsonFileVersion=distCommon.getDisparams("999999", "EWAY_JSON_VERSION", con);

			System.out.println("Ewaybill date Format is:"+dateFormat);

			System.out.println("generatedTransactionId:"+tranId);

			/*for(int i=0;i<tranId.size();i++){
				if(i==tranId.size()-1)
				{
					transIdComma=transIdComma+(""+tranId.get(i).getTranId()+"");
				}
				else
				{
					transIdComma=transIdComma+(""+tranId.get(i).getTranId()+",");
				}
			}*/

			//System.out.println("generatedTransactionId:list:"+generatedTransactionId+" "+" "+ generatedTransactionId.size()+" "+ generatedTransactionId.get(index));


		
				try
				{


					sql="select line_no,sql_expr,sql_input,sql_expr1,sql_expr2,sql_expr3,sql_expr4,"
							+ " sql_expr5,sql_expr6 from tax_bal_grp_det where bal_group=? and ref_ser = '"+tranId.getRefSer()+"'";

					pstmt=con.prepareStatement(sql);
					pstmt.setString(1, "EWAYJ");
					rs=pstmt.executeQuery();
					while(rs.next()){
						sqlExpr= checkNull(rs.getString("sql_expr"));
						sqlExpr1= checkNull(rs.getString("sql_expr1"));
						sqlExpr2= checkNull(rs.getString("sql_expr2"));
						sqlExpr3= checkNull( rs.getString("sql_expr3"));
						sqlExpr4= checkNull(rs.getString("sql_expr4"));
						sqlExpr5= checkNull(rs.getString("sql_expr5"));
						sqlExpr6= checkNull(rs.getString("sql_expr6"));  


						compSql= sqlExpr+" "+sqlExpr1+" "+sqlExpr2+" "+sqlExpr3+" "+sqlExpr4+" "+sqlExpr5+" "+sqlExpr6;



						//parameters=parameters+groupByQuery();

						compSql1=compSql.substring(0, compSql.indexOf("group"));
						compSql2=compSql.substring(compSql.indexOf("group"));

						compSql=compSql1+(" = '"+tranId.getTranId())+"'"+compSql2;
						System.out.println("Combine SQL JSON FILE::::"+(compSql));
						String temp="";
						if(compSql != null && compSql.trim().length() > 0 )
						{
							System.out.println("inside JSON retrieval query"+compSql);
							pstmt1=con.prepareStatement(compSql);
							//pstmt1.setString(1, tranId.get(0));
							/*
							int indexi = 1;
							if(transIdComma.contains(","))
							{
								for(String userSiteArr : siteCodeArr)
								{
									System.out.println("Withhold:::siteCode:::"+userSiteArr);
									pstmt1.setString(indexi++, userSiteArr.trim());
								}
							}
							else
							{*/
							//pstmt1.setString(1, tranId.get(i).getTranId().trim());

							rs1 = pstmt1.executeQuery();
							List<ItemDetails> itemList=null;
							while(rs1.next())
							{
								//getting data from GST HDR and GST DET tables for generating the JSON file for EWAY
								//from detail part
								System.out.println("inside the loop of EWAYJ JSON file");

								if(!temp.equalsIgnoreCase(rs1.getString("ref_id"))){
									itemList=new ArrayList<ItemDetails>();
									valueCounter=0;
								}



								refSer=rs1.getString("ref_ser");
								refId=rs1.getString("ref_id");
								userGstin=rs1.getString("from_gstin");
								supplyType=rs1.getString("supply_type");
								//subSupplyType=rs1.getString("sub_type");
								subSupplyType=rs1.getInt("sub_type");
								docType=rs1.getString("doc_type");
								docNo=rs1.getString("doc_no");
								docDate=rs1.getTimestamp("doc_date"); //this will be in date format 
								transType=rs1.getInt("trans_type");
								fromGstin=rs1.getString("from_gstin");
								fromTrdName=rs1.getString("from_party_descr");//from party description.
								fromAddr1=rs1.getString("from_address1");
								fromAddr2=rs1.getString("from_address2");
								fromPlace=rs1.getString("from_place");
								frompinCode=rs1.getInt("from_pincode");
								fromstateCode=rs1.getInt("from_state_code");
								actualFromStateCode=rs1.getInt("from_state_code");

								//to detail part
								toGstin=rs1.getString("to_gstin");
								toTrdName=rs1.getString("to_party_descr");
								toAddr1=rs1.getString("to_address1");
								toAddr2=rs1.getString("to_address2");
								toPlace=rs1.getString("to_place");
								toPincode=rs1.getInt("to_pin_code");
								tostateCode=rs1.getInt("to_state_code");
								actualToStateCode=rs1.getInt("to_state_code");
								totInvValue=rs1.getDouble("total_invoice_amt");//here taken assessable value as int instead of double
								cgstValue=rs1.getDouble("cgst_amt");
								sgstValue=rs1.getDouble("sgst_amt");
								igstValue=rs1.getDouble("igst_amt");
								cessValue=rs1.getDouble("cess_amt");
								totalValue=rs1.getDouble("assessable_value");
								totalAssesableValue=rs1.getDouble("tot_assessable_value");
								transMode=rs1.getInt("trans_mode");
								//transporterName=rs1.getString("trans_code");
								//transporterName="ABC";
								transporterName=(rs1.getString("trans_code")==null || rs1.getString("trans_code").trim().length() == 0) ? "ABC" : rs1.getString("trans_code").trim();
								transporterid=rs1.getString("trans_id");
								transDocNo=rs1.getString("trans_doc_id");
								transDocDate=rs1.getTimestamp("trans_doc_date");
								sundryType=rs1.getString("sundry_type");
								vehicleType=rs1.getString("vehicle_type");
								vehicleNo=rs1.getString("vehicle_no");
								mainHsnCode=rs1.getInt("hsn");
								transDistance=rs1.getDouble("distance");
								cgstRate=rs1.getDouble("cgst_perc");
								sgstRate=rs1.getDouble("sgst_perc");
								igstRate=rs1.getDouble("igst_perc");
								cessRate=rs1.getDouble("cess_perc");

								transactionDocDate = genericUtility.getValidDateString(transDocDate.toString(), genericUtility.getDBDateFormat(), dateFormat);



								docDateValue=genericUtility.getValidDateString(docDate.toString(), genericUtility.getDBDateFormat(), dateFormat);								


								valueCounter++;	

								ItemDetails itemDetails=new ItemDetails(valueCounter,"","", mainHsnCode, 0, "",totalValue,sgstRate,cgstRate, igstRate, cessRate,0.0);

								itemList.add(itemDetails);
								System.out.println("counter:"+valueCounter+" Size ItemList:"+list.size());



								EwayBillValue ewayBillValue=new EwayBillValue(
										userGstin, supplyType, subSupplyType, docType, docNo, docDateValue,transType, fromGstin, fromTrdName,
										fromAddr1,fromAddr2,fromPlace,frompinCode,fromstateCode,actualFromStateCode,
										toGstin,toTrdName,toAddr1,toAddr2,toPlace,toPincode,tostateCode,actualToStateCode,totalAssesableValue,
										cgstValue,sgstValue,igstValue,cessValue,0,0,totInvValue,transMode,transDistance,transporterName,transporterid,transDocNo,transactionDocDate,sundryType,vehicleType,vehicleNo,mainHsnCode,itemList,refSer,refId);
//xtraParams added by nandkumar on 05/12/19
								if(temp.equalsIgnoreCase(refId)){
									list.remove(list.size()-1);
								}

								list.add(ewayBillValue);
								logPath = distCommon.getDisparams("999999", "EWAY_LOG_PATH", con);//ADDED BY NANDKUMAR GADKARI ON05/12/19
								flag=ewayBillValue.validateFields(refSer, refId,siteCode,logPath);
								System.out.println("Validation flag is:"+flag);
								System.out.println("counter:"+valueCounter+" Size ValueList:"+list.size());
								temp=rs1.getString("ref_id");
								
							}
							rs1.close();
							rs1=null;
							pstmt1.close();
							pstmt1=null;
							//tranId.clear();
							//itemList.clear();
							//generatedTransactionId.clear();
						}else{
							//tranId.clear();
							//itemList.clear();
							// generatedTransactionId.clear();
						}

					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			
		}
		catch (SQLException e)
		{
			printLog("E-way Bill Failed", "SQL Sytax is Incorrect for Ref-Ser :"+refSer,"ERROR");
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally {
			list.clear();
		}

		if(!flag){
			System.out.println("inside failed transaction");
			printLog("E-way Bill Failed", "Please Check Entered Values for given transaction"+refSer,"ERROR");
		}
		
		return flag;
	}
	
	class TransModeData{

		double distance;
		String transporterName;
		String transId;
		String transMode;
		String transDocNo;
		String transDocDate;
		String vehicleType;
		public TransModeData(double distance, String transporterName, String transId, String transMode,
				String transDocNo, String transDocDate, String vehicleType) {
			super();
			this.distance = distance;
			this.transporterName = transporterName;
			this.transId = transId;
			this.transMode = transMode;
			this.transDocNo = transDocNo;
			this.transDocDate = transDocDate;
			this.vehicleType = vehicleType;
		}
		public double getDistance() {
			return distance;
		}
		public void setDistance(double distance) {
			this.distance = distance;
		}
		public String getTransporterName() {
			return transporterName;
		}
		public void setTransporterName(String transporterName) {
			this.transporterName = transporterName;
		}
		public String getTransId() {
			return transId;
		}
		public void setTransId(String transId) {
			this.transId = transId;
		}
		public String getTransMode() {
			return transMode;
		}
		public void setTransMode(String transMode) {
			this.transMode = transMode;
		}
		public String getTransDocNo() {
			return transDocNo;
		}
		public void setTransDocNo(String transDocNo) {
			this.transDocNo = transDocNo;
		}
		public String getTransDocDate() {
			return transDocDate;
		}
		public void setTransDocDate(String transDocDate) {
			this.transDocDate = transDocDate;
		}
		public String getVehicleType() {
			return vehicleType;
		}
		public void setVehicleType(String vehicleType) {
			this.vehicleType = vehicleType;
		}
	}	

	private String getTransId(String transCode,Connection conn)throws RemoteException, ITMException
	{
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String transId="";
		int count=0;
		try{
			sql="select count(*) from transporter where tran_code=? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, transCode);
			rs=pstmt.executeQuery();
			while(rs.next()){
				count=rs.getInt(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;

			if(count>0){
				sql="select tax_reg_2 from transporter where tran_code=? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, transCode);
				rs=pstmt.executeQuery();
				while(rs.next()){
					transId=rs.getString(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
			else{
				transId="";
			}
		}
		catch (SQLException e)
		{
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
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
				if (rs != null)//if codn added by Jiten 19/05/06
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				System.err.println("Exception :DBAccessEJB :getITMVersion :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}


		return transId;
	}
	
	private void setLogFile(Connection conn) 
	{
		try
		{
			String logPath = "", logDir = "", logFile = ""; 
			File logFileDir = null;
			DistCommon distCommon = new DistCommon();
			String logFileName = "";
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			logFileName = "EWAYBILL_"+siteCodes+"_"+sdf.format(new Date());
	
			System.out.println("Inside the print log method");
	
			logPath=distCommon.getDisparams("999999", "EWAY_LOG_PATH", conn);
			logDir = CommonConstants.JBOSSHOME +File.separator+logPath + File.separator  + "EWAYBILL"; //CommonConstants.JBOSSHOME + added by nandkumar 
			System.out.println("Log direction: "+logDir);
	
			logFileDir = new File(logDir);
	
			if (!logFileDir.exists()) 
			{
				logFileDir.mkdirs();
			}
	
			logFile = CommonConstants.JBOSSHOME +File.separator+logDir + File.separator + logFileName + ".log";
			
			logFileNames=logFileName;
			logDires=logDir;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//this method will helps to write the log based upon the number of actions taken on the files while processing the eway bill
	/**
	 * @param title
	 * @param msg
	 * This method is used for printing the logs in log file
	 */
	private void printLog(String title, String msg,String text) 
	{
		String logFile = "";
		String logDir = "";
		File logFileDir = null;
		FileWriter fileWriter = null;
		String logPath="";
		DistCommon distCommon = new DistCommon();
		//Connection conn = null;
		try
		{
			/*ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB();*/
			//conn = getConnection();/ commented by nandkumar gadkari on 05/12/19
			logPath=logPathPrintL;//added by nandkumar gadkari on 05/12/19
			String logFileName = "";
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			logFileName = "EWAYBILL_"+siteCodes+"_"+sdf.format(new Date());

			System.out.println("Inside the print log method");

			//logPath=distCommon.getDisparams("999999", "EWAY_LOG_PATH", conn);
			logDir = CommonConstants.JBOSSHOME +File.separator+logPath + File.separator  + "EWAYBILL";
			System.out.println("Log direction: "+logDir);

			logFileDir = new File(logDir);

			if (!logFileDir.exists()) 
			{
				logFileDir.mkdirs();
			}

			logFile = CommonConstants.JBOSSHOME +File.separator+logDir + File.separator + logFileName + ".log";
			
			logFileNames=logFileName;
			logDires=logDir;

			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			java.util.Date now = new java.util.Date();
			String strDate = sdfDate.format(now);

			fileWriter = new FileWriter(logFile, true);

			fileWriter.write("\r\n");
			fileWriter.write(strDate+" "+"["+text+"] "+msg);
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			//printLog("STDERR", ex);
		//	printLog("STDOUT","Inside Exception [getLog]>>" + ex.toString(),"ERROR");
		} 
		finally 
		{
			try
			{
				/*conn.close();
				conn = null;*/
			}
			catch(Exception e){}
			try 
			{
				if (fileWriter != null) 
				{
					fileWriter.flush();
					fileWriter.close();
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				//printLog("STDERR", e);
			}
		}
	}
	//email log file
	private void printMailLog(String title, String msg,String text) 
	{
		String logFile = "";
		String logDir = "";
		File logFileDir = null;
		FileWriter fileWriter = null;
		String logPath="";
		DistCommon distCommon = new DistCommon();
		Connection conn = null;
		try
		{
			/*ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB();*/
			conn = getConnection();
			String logFileName = "";
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			logFileName = "EWAYBILL_MAIL_"+siteCodes+"_"+sdf.format(new Date());
			mailLogFileName = logFileName;

			System.out.println("Inside the print log method");

			logPath=distCommon.getDisparams("999999", "EWAY_LOG_PATH", conn);
			logDir = CommonConstants.JBOSSHOME +File.separator+logPath + File.separator  + "EWAYBILL";
			System.out.println("Log direction: "+logDir);

			logFileDir = new File(logDir);

			if (!logFileDir.exists()) 
			{
				logFileDir.mkdirs();
			}

			logFile = CommonConstants.JBOSSHOME +File.separator+logDir + File.separator + logFileName + ".log";

			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			java.util.Date now = new java.util.Date();
			String strDate = sdfDate.format(now);

			fileWriter = new FileWriter(logFile, true);

			fileWriter.write("\r\n");
			fileWriter.write(strDate+" "+"["+text+"] "+msg);
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			//printLog("STDERR", ex);
			printLog("STDOUT","Inside Exception [getEmailLog]>>" + ex.toString(),"ERROR");
		} 
		finally 
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception e){}
			try 
			{
				if (fileWriter != null) 
				{
					fileWriter.flush();
					fileWriter.close();
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				//printLog("STDERR", e);
			}
		}
	}
	
	//this class will hold the generated transaction details inside transaction
	public class Transaction{
		private String tranId;
		private String refSer;
		public Transaction(String tranId, String refSer) {
			super();
			this.tranId = tranId;
			this.refSer = refSer;
		}
		public String getTranId() {
			return tranId;
		}
		public void setTranId(String tranId) {
			this.tranId = tranId;
		}
		public String getRefSer() {
			return refSer;
		}
		public void setRefSer(String refSer) {
			this.refSer = refSer;
		}
	}
	
	//send email id option...
	private boolean sendMail(String jsonFilePath,String attachment, String transInfo,boolean flag,UserInfoBean userInfo)throws ITMException//UserInfoBean userInfo added by nandkumar gadkari 
	{
		
		boolean sentMail = false;
		
		String lineNo = "1";
		String emailType = "page";
		StringBuffer valueXmlString=null;
		String retValue="";
		
		try{
			
		valueXmlString = new StringBuffer("<ROOT>");
		/*valueXmlString = new StringBuffer("<ROOT><TRANS_INFO>");
		valueXmlString.append("<OBJ_NAME>").append("<![CDATA[" + "" + "]]>").append("</OBJ_NAME>");
		valueXmlString.append("<REF_SER>").append("<![CDATA[" + "" + "]]>").append("</REF_SER>");
		valueXmlString.append("<REF_ID>").append("<![CDATA[" + "" + "]]>").append("</REF_ID>");
		valueXmlString.append("<LINE_NO>").append("<![CDATA[" + lineNo + "]]>").append("</LINE_NO>");
		valueXmlString.append("</TRANS_INFO>");

		valueXmlString.append("<MAIL>");
		valueXmlString.append("<EMAIL_TYPE>").append("<![CDATA[" + emailType + "]]>").append("</EMAIL_TYPE>");
		valueXmlString.append("<ENTITY_CODE>").append("<![CDATA[" + "SUN" + "]]>").append("</ENTITY_CODE>");
		valueXmlString.append("<ENTITY_TYPE>").append("<![CDATA[" + "" + "]]>").append("</ENTITY_TYPE>");
		valueXmlString.append("<FORMAT_CODE>").append("<![CDATA[" + "E" +"]]>").append("</FORMAT_CODE>");
		valueXmlString.append("<TO_ADD>").append("<![CDATA[" +mailAddress +"]]>").append("</TO_ADD>");
		valueXmlString.append("<CC_ADD>").append("<![CDATA[" + mailAddress+"]]>").append("</CC_ADD>");
		valueXmlString.append("<SUBJECT>").append("<![CDATA[" + "Reports" +"]]>").append("</SUBJECT>");
		valueXmlString.append("<BODY_TEXT>").append("<![CDATA[" + "PFA of Report(s)" +"]]>").append("</BODY_TEXT>");
		valueXmlString.append("<MESSAGE>").append("<![CDATA[" + "PFA of Report(s)" +"]]>").append("</MESSAGE>");
		valueXmlString.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" + reportFilePath + "]]>").append("</XML_DATA_FILE_PATH>");
		valueXmlString.append("</MAIL>");
		valueXmlString.append("</ROOT>");*/
			
			//valueXmlString.append("<ROOT>");
			valueXmlString.append("<MAILINFO>");
			valueXmlString.append("<EMAIL_TYPE>").append("page").append("</EMAIL_TYPE>");
			valueXmlString.append("<FORMAT_CODE>").append("<![CDATA[DOWN_JSON]]>").append("</FORMAT_CODE>");
			//commInfo.append("<TO_ADD>").append("<![CDATA[" + emailTo + "]]>").append("</TO_ADD>");
			if(flag){
				valueXmlString.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" +attachment+"]]>").append("</XML_DATA_FILE_PATH>");
			}
			else{
				valueXmlString.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" + attachment + "]]>").append("</XML_DATA_FILE_PATH>");
			}
			valueXmlString.append("</MAILINFO>");
			valueXmlString.append("<XML_DATA>").append(transInfo).append("</XML_DATA>");
			valueXmlString.append("</ROOT>");
			
		
		String mailDomStr = valueXmlString.toString();

		System.out.println("@@## [Generic Report]  :sendMail mailDomStr :-->" + mailDomStr);
		EMail email = new EMail();
		MailInfo info=new MailInfo();
		retValue=email.sendMail(mailDomStr, "ITM",userInfo);// userInfo
		if("S".equalsIgnoreCase(retValue))
		{
			//printLog("STDOUT","ReportCreationScheduler : sendMail => Mail sent successfully" );
			sentMail = true;
		}
		System.out.println("[JSON]  mail delivered successfully" );  
		}
		catch (ITMException itme)
		{
			System.out.println("@@## ITMException [[Generic Report]] :sendMail  :sendMail :sendMail() :==>\n");
			throw itme;
		}
		catch (Exception e)
		{
			System.out.println("Exception :[[Generic Report]] :sendMail() :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return sentMail;
	}
	
	//send mail log files...
	private boolean sendMailLog(String jsonFilePath,String attachment, String transInfo,boolean flag ,UserInfoBean userInfo)throws ITMException //UserInfoBean userInfo added by nandkumar gadkari 
	{
		
		boolean sentMail = false;
		
		String lineNo = "1";
		String emailType = "page";
		StringBuffer valueXmlString=null;
		String retValue="";
		
		try{
			
		valueXmlString = new StringBuffer("<ROOT>");
		/*valueXmlString = new StringBuffer("<ROOT><TRANS_INFO>");
		valueXmlString.append("<OBJ_NAME>").append("<![CDATA[" + "" + "]]>").append("</OBJ_NAME>");
		valueXmlString.append("<REF_SER>").append("<![CDATA[" + "" + "]]>").append("</REF_SER>");
		valueXmlString.append("<REF_ID>").append("<![CDATA[" + "" + "]]>").append("</REF_ID>");
		valueXmlString.append("<LINE_NO>").append("<![CDATA[" + lineNo + "]]>").append("</LINE_NO>");
		valueXmlString.append("</TRANS_INFO>");

		valueXmlString.append("<MAIL>");
		valueXmlString.append("<EMAIL_TYPE>").append("<![CDATA[" + emailType + "]]>").append("</EMAIL_TYPE>");
		valueXmlString.append("<ENTITY_CODE>").append("<![CDATA[" + "SUN" + "]]>").append("</ENTITY_CODE>");
		valueXmlString.append("<ENTITY_TYPE>").append("<![CDATA[" + "" + "]]>").append("</ENTITY_TYPE>");
		valueXmlString.append("<FORMAT_CODE>").append("<![CDATA[" + "E" +"]]>").append("</FORMAT_CODE>");
		valueXmlString.append("<TO_ADD>").append("<![CDATA[" +mailAddress +"]]>").append("</TO_ADD>");
		valueXmlString.append("<CC_ADD>").append("<![CDATA[" + mailAddress+"]]>").append("</CC_ADD>");
		valueXmlString.append("<SUBJECT>").append("<![CDATA[" + "Reports" +"]]>").append("</SUBJECT>");
		valueXmlString.append("<BODY_TEXT>").append("<![CDATA[" + "PFA of Report(s)" +"]]>").append("</BODY_TEXT>");
		valueXmlString.append("<MESSAGE>").append("<![CDATA[" + "PFA of Report(s)" +"]]>").append("</MESSAGE>");
		valueXmlString.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" + reportFilePath + "]]>").append("</XML_DATA_FILE_PATH>");
		valueXmlString.append("</MAIL>");
		valueXmlString.append("</ROOT>");*/
			
			//valueXmlString.append("<ROOT>");
			valueXmlString.append("<MAILINFO>");
			valueXmlString.append("<EMAIL_TYPE>").append("page").append("</EMAIL_TYPE>");
			valueXmlString.append("<FORMAT_CODE>").append("<![CDATA[DOWN_LOG]]>").append("</FORMAT_CODE>");
			//commInfo.append("<TO_ADD>").append("<![CDATA[" + emailTo + "]]>").append("</TO_ADD>");
			if(flag){
				valueXmlString.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" +attachment+"]]>").append("</XML_DATA_FILE_PATH>");
			}
			else{
				valueXmlString.append("<XML_DATA_FILE_PATH>").append("<![CDATA[" + attachment + "]]>").append("</XML_DATA_FILE_PATH>");
			}
			valueXmlString.append("</MAILINFO>");
			valueXmlString.append("<XML_DATA>").append(transInfo).append("</XML_DATA>");
			valueXmlString.append("</ROOT>");
			
		
		String mailDomStr = valueXmlString.toString();

		System.out.println("@@## [Generic Report]  :sendMail mailDomStr :-->" + mailDomStr);
		EMail email = new EMail();
		MailInfo info=new MailInfo();
		retValue=email.sendMail(mailDomStr, "ITM",userInfo);// userInfo
		if("S".equalsIgnoreCase(retValue))
		{
			//printLog("STDOUT","ReportCreationScheduler : sendMail => Mail sent successfully" );
			sentMail = true;
		}
		System.out.println("[JSON]  mail delivered successfully" );  
		}
		catch (ITMException itme)
		{
			System.out.println("@@## ITMException [[Generic Report]] :sendMail  :sendMail :sendMail() :==>\n");
			throw itme;
		}
		catch (Exception e)
		{
			System.out.println("Exception :[[Generic Report]] :sendMail() :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return sentMail;
	}
	
	
	
	private String getDomString(String emailIdPer) throws ITMException
	{
		String retString = "";
		StringBuffer valueXmlString = new StringBuffer();
		try
		{
			valueXmlString.append("<ROOT>");
			valueXmlString.append("<Detail1>");
			valueXmlString.append("<email_id>").append("<![CDATA[" + emailIdPer + "]]>").append("</email_id>");
			valueXmlString.append("</Detail1>");
			valueXmlString.append("</ROOT>");
			retString = valueXmlString.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			printLog("ERROR", "Exception occured while sending an email","");
		}
		return retString;
	}
	
	
}