package ibase.webitm.ejb.dis;

/********************************************************
Title :DespatchSchedule[D14LSUN009]
Date  : 13/04/15
Developer: Vishakha

********************************************************/

import ibase.scheduler.utility.interfaces.Schedule;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.rpc.ParameterMode;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import java.text.SimpleDateFormat;
import ibase.utility.E12GenericUtility;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

public class DespatchConfirmScheduler implements Schedule
{

String chgUser = null;
String chgTerm = null;
static long count_records=0;
boolean isError = false;
String loginSiteCode = "";
String retString="";
E12GenericUtility e12GenericUtility = new E12GenericUtility();
FileOutputStream fos1 = null;
java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
Calendar calendar = Calendar.getInstance();
String startDateStr = null;
public static void main(String args[])
{
	DespatchConfirmScheduler ecs=new DespatchConfirmScheduler();
	System.out.println("Main Method Calling******************************************Despatch Confirm");
}

public String schedulePriority( String wrkflwPriority )throws Exception
{
	return "";
}
public String schedule(HashMap map)throws Exception
{
	return "";
}
public String schedule(String scheduleParamXML)throws Exception
{
	String siteCode = "",actualLoginSiteCode="",errString="";
	String xtraParams = "";
	ibase.utility.UserInfoBean userInfo = null;
	try
	{
		System.out.println("************ ["+scheduleParamXML+"]");
		userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
		siteCode = userInfo.getSiteCode();
		xtraParams = "loginEmpCode="+userInfo.getEmpCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+loginSiteCode;
		
		//GenericUtility genericUtility=GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		Document dom=genericUtility.parseString(scheduleParamXML);
		
		NodeList paramList = dom.getElementsByTagName( "SCHEDULE" );
        NodeList parentNodeList = null,childNodeList = null;
		Node parentNode = null,childNode = null;
		int childNodeListLength = 0;
		String childNodeName = null;

		parentNodeList = dom.getElementsByTagName("ACTUALPARAMETERS");

		parentNode = parentNodeList.item(0);
		childNodeList = parentNode.getChildNodes();
		childNodeListLength = childNodeList.getLength();
		for(int ctr = 0; ctr < childNodeListLength; ctr++)
		{
			childNode = childNodeList.item(ctr);
			childNodeName = childNode.getNodeName();
						
			/*if(childNodeName!=null && !"#text".equalsIgnoreCase(childNodeName))
			{
				if(ctr==0)
				{
					actualLoginSiteCode=childNode.getFirstChild().getNodeValue();
				}
							
			}*/
		}
		System.out.println("ActualLoginSiteCode----["+actualLoginSiteCode+"]");
		System.out.println("intializingLog$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$........."+ intializingLog("Desp_conf_sch_log"));
		
		errString = despatchConfirmSch(xtraParams);
			
		
	}
	catch(Exception e)
	{
		throw new Exception(e);
	}
	System.out.println("errString>>>["+errString+"]");
	return errString;
}
public String despatchConfirmSch(String xtraParams) throws Exception
{
	System.out.println("Insiding*****despatchConfirmSch");
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	PreparedStatement pstmt1 = null;
	ResultSet rs1 = null;
	String sql="";
	String tranId="";
	
	String sql1="";
	String errString ="";
	String strToWrite = "";
	String endDateStr = null;
	
	String methodName = "",compName = "",compType="",retString = "",serviceCode = "",serviceURI = "",actionURI = "";
	String forcedFlag="N";
	//E12GenericUtility e12GenericUtility = new E12GenericUtility();
	SimpleDateFormat sdf = new SimpleDateFormat(e12GenericUtility.getApplDateFormat());
	java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
	java.util.Date endDate = new java.util.Date(System.currentTimeMillis());
	try
	{
		ConnDriver connDriver = new ConnDriver();
		conn = connDriver.getConnectDB("DriverITM");
		//conn = getConnection();
		connDriver = null;
		conn.setAutoCommit(false);
		SimpleDateFormat sdf1 = new SimpleDateFormat(e12GenericUtility.getDBDateFormat());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(e12GenericUtility.getApplDateFormat());
		java.util.Calendar cal = java.util.Calendar.getInstance();
		//tranDate = simpleDateFormat.format(cal.getTime());
        String objName = "despatch";
		sql1 = "select desp_id  from despatch where confirmed = 'N' and guid is not null";
		pstmt1 = conn.prepareStatement(sql1);
		rs1 = pstmt1.executeQuery();
		while (rs1.next())
		{
			tranId = rs1.getString("desp_id");

			System.out.println(" tranId:::::::::" + tranId);
			strToWrite = tranId+",";
			startDate = new java.util.Date(System.currentTimeMillis());
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			strToWrite=strToWrite+startDateStr+",";

			System.out.println("STARTING DATE"+startDateStr +"STRING TO WRITE"+strToWrite);

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;
			
			sql = "SELECT SERVICE_CODE,COMP_NAME,COMP_TYPE FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,objName);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
				compType = rs.getString("COMP_TYPE");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			System.out.println("serviceCode = "+serviceCode+" compName "+compName+" compType "+compType);
			
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,serviceCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
			
			Service service = new Service();
			Call call = (Call)service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[5];
			
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_type"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);
			
			aobj[0] = new String(compType);
			aobj[1] = new String(compName);
			aobj[2] = new String(tranId);
			aobj[3] = new String(xtraParams);
			aobj[4] = new String(forcedFlag);
			
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);
			System.out.println(">>>>Confirm Complete Return string from NVO is:==>["+retString+"]");
		
			endDate = new java.util.Date(System.currentTimeMillis());
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			endDateStr = sdf1.format(endDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			System.out.println("**** DESPATCH completed for.....[" + tranId + "] at [" + endDateStr + "]" );
			strToWrite=strToWrite+endDateStr+",";
			strToWrite = strToWrite + retString+"\r\n\r\n";
			fos1.write(strToWrite.getBytes());
			System.out.println("End Time for transaction"+endDateStr+"string to write"+strToWrite);
			
				} 
			
		
		rs1.close();
		rs1 = null;
		pstmt1.close();
		pstmt1 = null;

	}
	catch(SQLException se)
	{
		isError = true;
		System.out.println("Exception : despatch Scheduler" +se.getMessage());
		try
		{
			conn.rollback();
		}
		catch(Exception se1){}
		throw new ITMException(se);
	}finally
	{
			if(conn != null)
			{
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				
				if(rs!=null)
				{
					rs.close();
					rs=null;
				}
			
				conn.close();
				conn= null;
			}
	}

	
	System.out.println("errString........:: " + errString);
	return errString;  	
}


private String intializingLog(String fileName) throws ITMException
{
	String log="intializingLog_Failed";
	String strToWrite = "";
	String currTime = null;
	try{
		SimpleDateFormat sdf1 = new SimpleDateFormat(e12GenericUtility.getDBDateFormat());
		try
		{
			currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
			currTime = currTime.replaceAll("-","");
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			fileName = fileName+currTime+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+".csv";
			fos1 = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+fileName);
		}
		catch(Exception e)
		{
			System.out.println("Exception []::"+e.getMessage());
			e.printStackTrace();
		}
		startDate = new java.util.Date(System.currentTimeMillis());
		calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
		startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
		fos1.write(("Fetching Records Started At " + startDateStr +"\r\n").getBytes());

	}
	catch(Exception e)
	{
		System.out.println("Exception []::"+e.getMessage());
		e.printStackTrace();
		throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
	}

	log ="intializingLog_Successesfull";
	return log;
}




}
