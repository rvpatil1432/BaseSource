/********************************************************
	Title 	 : EpayPrcSch
	Date  	 : 20/FEB/15
	Developer: Pankaj R.
 ********************************************************/
package ibase.webitm.ejb.dis;
import ibase.scheduler.utility.interfaces.Schedule;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.omg.CORBA.ORB;
import org.w3c.dom.*;



import java.util.Properties;

import javax.swing.text.NumberFormatter;
import javax.xml.parsers.*;
import javax.xml.rpc.ParameterMode;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.GenerateReceiptPrc;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.BaseException;
import ibase.utility.CommonConstants;
import ibase.utility.UserInfoBean;
import ibase.ejb.*;
import ibase.system.config.*;
import ibase.utility.E12GenericUtility;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.*;
import java.net.InetAddress;

import ibase.webitm.ejb.mfg.MfgCommon;
import ibase.webitm.ejb.sys.UtilMethods;

public class DissIssConfSch implements Schedule
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//FileOutputStream fos1 = null;
	@Override
	public String schedule(String scheduleParamXML)throws Exception
	{
		
		int childNodeListLength = 0;
		String childNodeName = null;
		String tranId="",loginSiteCode="";
		ibase.utility.UserInfoBean userInfo = null;
		int noOfParam=0;
		String sysDate ="";
		String retString="";
		String xmlString4="";
		String retString1="";
		//ITMDBAccessEJB itmDBAccessEJB = null;
		
		String xtraParams = "",siteCode="";
		String sql="";
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;		
		String windowName="w_e_payment";
		String ediAddr="";	
		String distOrder="";
		UtilMethods utilmethod = new UtilMethods();
		Date FrmDate=null;
		int noOfDays=0;
         DistCommon distCommon =new DistCommon();
         String [] ediAddList;
         String ediAddress="",confirmed="";
String runMode="";
		int cnt=0;
		try
		{

			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			conn.setAutoCommit(false);	
			System.out.println("In try@@@@@@@########@@@");
			System.out.println("************ ["+scheduleParamXML+"]");
			Document dom = null;
			//			Node currDetail = null ;

			userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
			loginSiteCode = userInfo.getSiteCode();
            runMode="I";
			xtraParams = "loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+loginSiteCode+"~~loginEmpCode="+userInfo.getEmpCode()+"~~runMode="+runMode;

			System.out.println("XTRAPARAMS@@@@@@@@@@@######:"+xtraParams);
			//GenericUtility genericUtility=GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			System.out.println("scheduleParamXML-----"+scheduleParamXML);
			dom=genericUtility.parseString(scheduleParamXML);

			java.util.Date today=new java.util.Date();
			Calendar c = Calendar.getInstance(); 
			c.setTime(today); 
			today = c.getTime();
			SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate=sdf.format(today);
			java.util.Date todayDate=new java.util.Date();
			System.out.println("todayDate============="+todayDate);
	        System.out.println("Scheduler Started*******");
	        String forcedFlag="N";
	        ediAddr = checkNull(distCommon.getDisparams("999999", "EDI_ADDR", conn));
			System.out.println("----------------ediAddr:"+ediAddr);
			ediAddList=ediAddr.split(",");
			for(int i=0;i<ediAddList.length;i++)
			{
					System.out.println("Getting Sundry Type==="+ediAddList[i]);
                 ediAddress=ediAddList[i];
                 System.out.println("ediAddress==="+ediAddress);
			
                 sql="SELECT DIST_ORDER,tran_id FROM DISTORD_ISS WHERE CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END='N' " +
					"AND SITE_CODE IN(SELECT SITE_CODE FROM SITE WHERE EDI_ADDR IN(?))";
                 pstmt=conn.prepareStatement(sql);
		//	pstmt.setString(1,ediAddr);
                 pstmt.setString(1,ediAddress);
                 rs=pstmt.executeQuery();
				while(rs.next())
				{
					cnt++;
	
					System.out.println("While@@@@@"+cnt);
					distOrder=checkNull(rs.getString("DIST_ORDER"));
					tranId=checkNull(rs.getString("tran_id"));
					System.out.println("distOrder@@----" +distOrder);
					System.out.println("tranId@@----" +tranId);
				//	retString=autoDistIssConf("dist_issue",tranId,xtraParams,forcedFlag,"pre_confirm",conn);
					retString=confirmTranscation("dist_issue", tranId, xtraParams, conn);
					System.out.println("retString from autoConfirmQC========="+retString);
					if((retString != null ) && (retString.indexOf("VTSUCC1") > -1))
					{
						conn.commit();
						
						System.out.println(">>>>>>>>>retString.indexOf(VTSUCC1) :"+retString.indexOf("VTSUCC1"));
						
						System.out.println(">>>>After Commit Confirm Completed:"+distOrder);
						if(retString.indexOf("VTSUCC1") > -1)
						{
							
							System.out.println("["+distOrder+"]success");
						}
						else
						{
							System.out.println("["+distOrder+"]failed");
						}
						
					}
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("distOrder%%======"+distOrder);
			
			}
		}


		catch(Exception e)
		{
			System.out.println(">>>>>>>>>>>>>In catch:");
			System.out.println(e);
		}
		finally
		{
			if(conn!=null)
			{
				//conn.commit();
				conn.close();
				conn=null;
			}
			/*try
			{
				if(fos1!=null)
				{
					fos1.close();
					fos1=null;
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception"+e.getMessage());

			}*/
		}
		System.out.println("");
		return retString;

	}

	@Override
	public String schedule(HashMap arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String schedulePriority(String arg0) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	private String checkNull(String input)
	{
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
	
	
	public String confirmTranscation(String businessObj, String tranIdFr,String xtraParams, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println("confirmVoucher(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");

		try
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,businessObj);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = "+serviceCode+" compName "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,serviceCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
			Service service = new Service();
			Call call = (Call)service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			System.out.println("aobj 0 :"+aobj[0]);
			System.out.println("aobj 1 :"+aobj[1]);
			System.out.println("aobj 2 :"+aobj[2]);
			//aobj[3] = new String(forcedFlag);
			//System.out.println("@@@@@@@@@@loginEmpCode:" +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);
			System.out.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>["+retString+"]");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{		
			try{


				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				/*if( conn != null ){
					conn.close();
					conn = null;
				}*/
			}
			catch(Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try{
					conn.rollback();

				}catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}
		}
		return retString;
	}
}
