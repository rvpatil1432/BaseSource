/***
 * VALLABH KADAM 
 * AutoAllocOrdrSch.java
 * 07/APR/2015
 * Request Id:-[D15ASUN001]
 * */
package ibase.webitm.ejb.dis;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.scheduler.utility.interfaces.Schedule;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

public class AutoAllocOrdrSch implements Schedule
{
	FileOutputStream fos1 = null;
	java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
	java.util.Date endDate = new java.util.Date(System.currentTimeMillis());
	Calendar calendar = Calendar.getInstance();
	String startDateStr = null;
	String endDateStr = null;
	E12GenericUtility genericUtility = new E12GenericUtility();

	@Override
    public String schedule(HashMap arg0) throws Exception
    {
	    // TODO Auto-generated method stub
	    return null;
    }
	

	@Override
    public String schedule(String scheduleParamXML) throws Exception
    {
		int childNodeListLength = 0;
		String childNodeName = null;
		String loginSiteCode="",autoAlloc="";
		ibase.utility.UserInfoBean userInfo = null;
		int noOfParam=0;
		String sysDate ="";
		String retString="";
		String retString1="";
		String xtraParams = "",siteCode="",logRetStr="",strToWrite="";
		NodeList parentNodeList = null,childNodeList = null;
		Node parentNode = null,childNode = null;
		SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());		
		
//		String windowName="w_sordalloc";
		String windowName="w_sordalloc_sh";
		StockAllocationPrc stAlcPrc=new StockAllocationPrc();
		
		System.out.println("scheduleParamXML :- "+scheduleParamXML);
		Document dom = null;
		
		userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
		loginSiteCode = userInfo.getSiteCode();
		autoAlloc="Y";
		try
		{
		xtraParams = "loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+loginSiteCode+
				"~~loginEmpCode="+userInfo.getEmpCode()+"~~autoAlloc="+autoAlloc;		
		System.out.println("XTRAPARAMS:"+xtraParams);
		dom=genericUtility.parseString(scheduleParamXML);
		
		java.util.Date today=new java.util.Date();
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(today); 
		today = cal.getTime();
		SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
		sysDate=sdf.format(today);
		
		NodeList paramList = dom.getElementsByTagName( "SCHEDULE" );
        noOfParam = paramList.getLength();
        
        parentNodeList = dom.getElementsByTagName("ACTUALPARAMETERS");
        parentNode = parentNodeList.item(0);
		childNodeList = parentNode.getChildNodes();
		childNodeListLength = childNodeList.getLength();
		System.out.println("childNodeListLength :-["+childNodeListLength+"]");
		
		for(int ctr = 0; ctr < childNodeListLength; ctr++)
		{
			childNode = childNodeList.item(ctr);
			childNodeName = childNode.getNodeName();
						
			if(childNodeName!=null && !"#text".equalsIgnoreCase(childNodeName))
			{
				if(ctr==0)
				{
					siteCode = childNode.getFirstChild().getNodeValue();
				}	
			}
		}		
		System.out.println("Site code :-"+siteCode);		
		
		/**
		 * Initialize Log here.
		 * before getData(); 
		 * */
		logRetStr=intializingLog("AutoAllocOdrSh_log");
		System.out.println("Log Return String :- ["+logRetStr+"]");		
		
		String xmlString1="<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"sordalloc\">"
				+ "<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>"
						+ "<site_code><![CDATA["+siteCode+"]]></site_code>"														//TA821
						+ "<post_order_flag><![CDATA[N]]></post_order_flag>"													//Strictly N
						+ "<default_qty_flag><![CDATA[Y]]></default_qty_flag>"													//Strictly Y
						+ "<item_ser__from><![CDATA[00]]></item_ser__from><item_ser__to><![CDATA[ZZ]]></item_ser__to>"
						+ "<cust_code__from><![CDATA[00]]></cust_code__from><cust_code__to><![CDATA[ZZ]]></cust_code__to>"
						+ "<sale_order__from><![CDATA[00]]></sale_order__from><sale_order__to><![CDATA[ZZ]]></sale_order__to>"
						+ "<due_date__from><![CDATA["+sysDate+"]]></due_date__from><due_date__to><![CDATA["+sysDate+"]]></due_date__to>"
						+ "<item_code__from><![CDATA[00]]></item_code__from><item_code__to><![CDATA[ZZ]]></item_code__to>"	
						+ "<active_pick_allow/><dis_opt><![CDATA[S]]></dis_opt>"												//Strictly S
						+ "<batch_id/></Detail1>";		
			
		String xmlString2="<DocumentRoot><description>Datawindow Root</description>"
				+ "<group0><description>Group0 description</description><Header0><description>Header0 members</description>"
				+ "<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"sordalloc\">"
						+ "<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>"
								+ "<site_code><![CDATA["+siteCode+"]]></site_code>"
								+ "<post_order_flag><![CDATA[N]]></post_order_flag>"
								+ "<default_qty_flag><![CDATA[Y]]></default_qty_flag>"
								+ "<item_ser__from><![CDATA[00]]></item_ser__from><item_ser__to><![CDATA[ZZ]]></item_ser__to>"
								+ "<cust_code__from><![CDATA[00]]></cust_code__from><cust_code__to><![CDATA[ZZ]]></cust_code__to>"
								+ "<sale_order__from><![CDATA[00]]></sale_order__from><sale_order__to><![CDATA[ZZ]]></sale_order__to>"
								+ "<due_date__from><![CDATA["+sysDate+"]]></due_date__from><due_date__to><![CDATA["+sysDate+"]]></due_date__to>"
								+ "<item_code__from><![CDATA[00]]></item_code__from><item_code__to><![CDATA[ZZ]]></item_code__to>"
								+ "<active_pick_allow/><dis_opt><![CDATA[S]]></dis_opt>"
								+ "<batch_id/></Detail1>"
								+ "</Header0></group0></DocumentRoot>";
		
		System.out.println("xmlString1@@@@@@@@@@@@@@@@:["+xmlString1+"]");
		System.out.println("xmlString2@@@@@@@@@@@@@@@@:["+xmlString2+"]");
				
		retString=stAlcPrc.getData(xmlString1, xmlString2, windowName, xtraParams);		
		System.out.println("Get DATA retString :- ["+retString+"]");
		
		/**
		 * Writing log after getData();
		 * */
		startDate = new java.util.Date(System.currentTimeMillis());
		calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
		startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
		
		strToWrite=" For Site code:- "+siteCode+", Post order:-'N' , Default Qty flag :- 'Y'"
				+", Item Sceries from:- 00 To:- ZZ, Cust Code :- 00 To:- ZZ, Sales Order from:- 00 To:- ZZ,"
				+ " Due Date from:- "+sysDate+" To:- "+sysDate+", Item Code from :- 00 To:- ZZ, Display Option:- 'S';\r\n\r\n"
						+ " selected :-"+retString+" ";		
		
		/**
		 * Append Start date
		 * */
		strToWrite=strToWrite+startDate+"\r\n\r\n";
		
		retString1=stAlcPrc.process(xmlString1, retString, windowName, xtraParams);
		System.out.println("Process String :-["+retString1+"]");
		
		/**
		 * Writing log after process();
		 * */ 
		endDate = new java.util.Date(System.currentTimeMillis());
		calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
		endDateStr = sdf1.format(endDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
		strToWrite=strToWrite+endDateStr;
		strToWrite=" "+strToWrite+retString1+"\r\n\r\n";
		System.out.println("Str write :-----------["+strToWrite+"]");
		fos1.write(strToWrite.getBytes());
		System.out.println("End Time for transaction"+endDateStr+"string to write"+strToWrite);

	    return retString;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		
    }
	private String intializingLog(String fileName) throws ITMException
	{
		String log="intializingLog_Failed";
		String currTime = null;
		try{
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			try
			{
				currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
				currTime = currTime.replaceAll("-","");
				calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
				fileName = fileName+currTime+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+".txt";
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}

		log ="intializingLog_Successesfull";
		return log;
	}
	@Override
    public String schedulePriority(String arg0) throws Exception
    {
	    // TODO Auto-generated method stub
	    return null;
    }
}
