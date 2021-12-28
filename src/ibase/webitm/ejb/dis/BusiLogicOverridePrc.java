/********************************************************
	override Failed business logic [ req id : D16DSER001 ]
********************************************************/
package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
import java.sql.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.w3c.dom.*;

import java.util.Calendar;
import java.util.HashMap;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import javax.xml.rpc.ParameterMode;

import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class BusiLogicOverridePrc extends ProcessEJB implements BusiLogicOverridePrcLocal,BusiLogicOverridePrcRemote 
{
	BusinessLogicChkOverride businesslogicchkoverride=new BusinessLogicChkOverride();
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Create Method Called....");
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}
	 */
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{	
		System.out.println(" :getData() function called");
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;	

		E12GenericUtility genericUtility= new  E12GenericUtility();


		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 				
			}

			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :Xform :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();			
		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String sql="",fromDateStr="",toDateStr="",reasonCode="",tranId="",tranType="";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		StringBuffer xmlStringBuffer = new StringBuffer();			
		ConnDriver connDriver = new ConnDriver();	
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		connDriver = new ConnDriver();
		Connection conn = null;

		Timestamp fromDate=null,toDate=null,orderDate=null, aprvDate=null;
		String saleOrder="",crPolicy="", descr="", aprvStat="", empCodeAprv="", custCode="", itemSer="", siteCode="", amdNo="";
		String userId = "";
		double aprvAmt=0, usedAmt=0;
		int count=0;
		String errString="";

		try
		{
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;

			fromDateStr = genericUtility.getColumnValue("from_date",headerDom);
			toDateStr = genericUtility.getColumnValue("to_date",headerDom);
			reasonCode = genericUtility.getColumnValue("reas_code",headerDom);
			System.out.println("@@@@@@ reasonCode["+reasonCode+"]fromDateStr["+fromDateStr+"]toDateStr["+toDateStr+"]");

			// date validation
			if(fromDateStr==null || fromDateStr.trim().length()==0)
			{
				errString = itmDBAccessEJB.getErrorString("from_date","VMFRMDTNUL",userId,"",conn);
				return errString;
			}
			else
			{
				if(toDateStr == null || toDateStr.trim().length()==0)
				{
					errString = itmDBAccessEJB.getErrorString("to_date","VMTODTNUL",userId,"",conn);
					return errString;
				}
				fromDate= Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				toDate= Timestamp.valueOf(genericUtility.getValidDateString(toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

				System.out.println("@@@@@@ fromDate["+fromDate+"]toDate["+toDate+"]");

				if( toDate.before(fromDate) && !( toDate.equals(fromDate) ) )
				{
					System.out.println("toDate before fromDate........");
					errString = itmDBAccessEJB.getErrorString("to_date","INVFRTDATE",userId,"",conn);
					return errString;
				}							
			}

			// validate reason code
			if(reasonCode==null || reasonCode.trim().length()==0)
			{
				errString = itmDBAccessEJB.getErrorString("reas_code","VMREASNCNU",userId,"",conn);
				return errString;
			}
			else
			{

				sql = " select count(1) from gencodes " +
						" where fld_name = 'REAS_CODE' " +
						" and mod_name = 'W_BUSINESS_LOGIC_OVERRIDE' and  fld_value = ?   ";    
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, reasonCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					count=rs.getInt(1);
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;

				if( count == 0)
				{
					errString = itmDBAccessEJB.getErrorString("reas_code","VMINVREASC",userId,"",conn);
					return errString;
				}
			}


			xmlStringBuffer = new StringBuffer("<?xml version = \"1.0\"?>");
			xmlStringBuffer.append("<DocumentRoot>");
			xmlStringBuffer.append("<description>").append("Datawindow Root").append("</description>");
			xmlStringBuffer.append("<group0>");
			xmlStringBuffer.append("<description>").append("Group0 description").append("</description>");
			xmlStringBuffer.append("<Header0>");


			sql = "  SELECT BUSINESS_LOGIC_CHECK.TRAN_ID, " +
					"  BUSINESS_LOGIC_CHECK.TRAN_TYPE," +
					"  BUSINESS_LOGIC_CHECK.SALE_ORDER," +
					"  SORDER.ORDER_DATE," +
					"  BUSINESS_LOGIC_CHECK.CR_POLICY," +
					"  BUSINESS_LOGIC_CHECK.DESCR," +
					"  BUSINESS_LOGIC_CHECK.APRV_STAT,    " +
					"   BUSINESS_LOGIC_CHECK.APRV_DATE,    " +
					"  BUSINESS_LOGIC_CHECK.EMP_CODE__APRV,    " +
					"  BUSINESS_LOGIC_CHECK.APRV_AMT,    " +
					"  BUSINESS_LOGIC_CHECK.USED_AMT,   " + 
					"  SORDER.CUST_CODE,   " + 
					"  SORDER.ITEM_SER,   " + 
					"  SORDER.SITE_CODE,   " + 
					"  BUSINESS_LOGIC_CHECK.AMD_NO, " + 
					"  SORDER.ORDER_DATE," +  
					"  BUSINESS_LOGIC_CHECK.REAS_CODE"+  
					"  FROM BUSINESS_LOGIC_CHECK ,    " +
					"  SORDER   " +
					"  WHERE ( BUSINESS_LOGIC_CHECK.SALE_ORDER = SORDER.SALE_ORDER ) and " +  
					"  (  SORDER.ORDER_DATE >= ?  AND   SORDER.ORDER_DATE <= ?  ) " +
					"  and  case when aprv_stat is null THEN 'F' else APRV_STAT end = 'F' " ;


			pstmt=conn.prepareStatement(sql);
			pstmt.setTimestamp(1, fromDate);
			pstmt.setTimestamp(2, toDate);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				tranId= checkNull(rs.getString("TRAN_ID"));
				tranType= checkNull(rs.getString("TRAN_TYPE"));
				saleOrder= checkNull(rs.getString("SALE_ORDER"));
				orderDate=rs.getTimestamp("ORDER_DATE");
				crPolicy= checkNull(rs.getString("CR_POLICY"));
				descr= checkNull(rs.getString("DESCR"));
				aprvStat= checkNull(rs.getString("APRV_STAT"));
				aprvDate=rs.getTimestamp("APRV_DATE");
				empCodeAprv= checkNull(rs.getString("EMP_CODE__APRV"));
				aprvAmt=rs.getDouble("APRV_AMT");
				usedAmt=rs.getDouble("USED_AMT"); 
				custCode= checkNull(rs.getString("CUST_CODE")); 
				itemSer= checkNull(rs.getString("ITEM_SER")); 
				siteCode= checkNull(rs.getString("SITE_CODE")); 
				amdNo= checkNull(rs.getString("AMD_NO"));


				xmlStringBuffer.append("<Detail2>");

				xmlStringBuffer.append("<tran_id>").append("<![CDATA[" +tranId+"]]>").append("</tran_id>");
				xmlStringBuffer.append("<tran_type>").append("<![CDATA[" +tranType+"]]>").append("</tran_type>");
				xmlStringBuffer.append("<sale_order>").append("<![CDATA[" +saleOrder+"]]>").append("</sale_order>");
				if( orderDate != null )
				{
					xmlStringBuffer.append("<order_date>").append("<![CDATA[" +(genericUtility.getValidDateString(orderDate.toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()))+"]]>").append("</order_date>");
				}
				else
				{
					xmlStringBuffer.append("<order_date>").append("").append("</order_date>");
				}
				xmlStringBuffer.append("<reas_code>").append("<![CDATA[" +reasonCode+"]]>").append("</reas_code>");
				xmlStringBuffer.append("<amd_no>").append("<![CDATA[" +amdNo+"]]>").append("</amd_no>");
				xmlStringBuffer.append("<site_code>").append("<![CDATA[" +siteCode+"]]>").append("</site_code>");
				xmlStringBuffer.append("<cust_code>").append("<![CDATA[" +custCode+"]]>").append("</cust_code>");
				xmlStringBuffer.append("<item_ser>").append("<![CDATA[" +itemSer+"]]>").append("</item_ser>");
				xmlStringBuffer.append("<cr_policy>").append("<![CDATA[" +crPolicy+"]]>").append("</cr_policy>");
				xmlStringBuffer.append("<descr>").append("<![CDATA[" +descr+"]]>").append("</descr>");
				xmlStringBuffer.append("<aprv_stat>").append("<![CDATA[" +aprvStat+"]]>").append("</aprv_stat>");

				if( aprvDate != null )
				{
					xmlStringBuffer.append("<aprv_date>").append("<![CDATA[" +(genericUtility.getValidDateString(aprvDate.toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()))+"]]>").append("</aprv_date>");
				}
				else
				{
					xmlStringBuffer.append("<aprv_date>").append("").append("</aprv_date>");
				}

				xmlStringBuffer.append("<emp_code__aprv>").append("<![CDATA[" +empCodeAprv+"]]>").append("</emp_code__aprv>");
				xmlStringBuffer.append("<aprv_amt>").append("<![CDATA[" +aprvAmt+"]]>").append("</aprv_amt>");
				xmlStringBuffer.append("<used_amt>").append("<![CDATA[" +usedAmt+"]]>").append("</used_amt>");

				xmlStringBuffer.append("</Detail2>");
				count++;
				System.out.println("#####Counter:["+count+"]");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;


			xmlStringBuffer.append("</Header0>");
			xmlStringBuffer.append("</group0>");
			xmlStringBuffer.append("</DocumentRoot>");

			if (count > 0)
			{
				System.out.println("@@@@@@@@@@xmlStringBuffer.toString()[[["+xmlStringBuffer.toString()+"]]]");
				return xmlStringBuffer.toString();	
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
				return errString;
			}

		}
		catch(Exception e)
		{	
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{

				if(conn != null)
				{					
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					conn.close();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}			
		}
		
	}//END OF GETDATA(2)
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException,ITMException
			{
		Document detailDom = null;
		Document headerDom = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		String retStr = "";
		Connection conn=null;
		try
		{	
			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);

			ConnDriver connDriver = new ConnDriver();
			if(conn==null)
			{
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				conn.setAutoCommit(false);	
			}

			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams,conn);
		}
		catch (Exception e)
		{			
			System.out.println("Exception : :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
		}
		return retStr;
			}//END OF PROCESS (1)



	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams,Connection conn) throws RemoteException,ITMException
	{		
		String retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "" ,errString = "";
		String reasonCode = "" ,tranId="";
		int count =0;
		int parentNodeListLength = 0;		
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		String childNodeName = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String forcedFlag="N",userId="";
		boolean overrideFlag = true;
		ArrayList<String> overrideFailed = new ArrayList<String>();
		ArrayList<String> overridePassed = new ArrayList<String>();
		try
		{

			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength(); 
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				overrideFlag = true;
				parentNode = parentNodeList.item(selectedRow);

				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength---->>> "+ childNodeListLength);
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName---->>> "+ childNodeName);

					if (childNodeName.equals("tran_id") && childNode.getFirstChild() != null)
					{
						tranId = childNode.getFirstChild().getNodeValue();
					}
					
					if (childNodeName.equals("reas_code") && childNode.getFirstChild() != null)
					{
						reasonCode=childNode.getFirstChild().getNodeValue();
					}
				}	

				System.out.println("@@==>> selectedRow["+selectedRow+"]tranId["+tranId+"]reasonCode["+reasonCode+"]");

				if(reasonCode==null || reasonCode.trim().length()==0)
				{
					//errString = itmDBAccessEJB.getErrorString("reas_code","VMINVREASN",userId,"",conn);
					//return errString;
					overrideFlag = false;
				}
				else
				{

					sql = " select count(1) from gencodes " +
							" where fld_name = 'REAS_CODE' " +
							" and mod_name = 'W_BUSINESS_LOGIC_OVERRIDE' and  fld_value = ?   ";    
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, reasonCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						count=rs.getInt(1);
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					if( count == 0)
					{
						//errString = itmDBAccessEJB.getErrorString("reas_code","VMINVREASC",userId,"",conn);
						//return errString;
						overrideFlag = false;
					}
				}
				System.out.println("@@ tranId["+tranId+"]overrideFlag["+overrideFlag+"]");

				if(overrideFlag)
				{
					//retStr =  businessLogicOverride("business_logic_override",tranId,xtraParams,forcedFlag,"pre_override",conn);  //commented by manish mhatre on 21-may-2020
					retStr=  businesslogicchkoverride.override(tranId, xtraParams, forcedFlag, conn);   //added by manish mhatre on 21-may-2020
					System.out.println("@@ tranId["+tranId+"]retStr["+retStr+"]overrideFlag["+overrideFlag+"]");

					//if( retStr != null && retStr.contains("VTSUCC1") && !retStr.contains("DS000ERROR") ) //commented by nandkumar gadkari on 21-05-2020
					if( retStr != null && retStr.contains("OVRRIDSUCC") && !retStr.contains("DS000ERROR") )
					{
						sql = " update BUSINESS_LOGIC_CHECK set REAS_CODE = ? where  tran_id = ?" ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, reasonCode);
						pstmt.setString(2, tranId);
						int updCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;	
						System.out.println("@@ tranId["+tranId+"]updCnt["+updCnt+"]");

						if( updCnt > 0 )
						{
							System.out.println("@@ tranId["+tranId+"]...conn.commit()......");
							conn.commit();
						}

						overridePassed.add(tranId);

					}
					else
					{
						overrideFailed.add(tranId);
					}
				}
				else
				{
					overrideFailed.add(tranId);
				}

			}
			System.out.println("@@@@@@@ overridePassed.size["+overridePassed.size()+"]overrideFailed.size["+overrideFailed.size()+"]");
			if( overridePassed.size() > 0  &&  overrideFailed.size() == 0 )
			{
				errString = itmDBAccessEJB.getErrorString("tran_id","OVRRIDSUCC",userId,"",conn);
			}
			if( overridePassed.size() == 0  &&  overrideFailed.size() > 0 )
			{
				errString = itmDBAccessEJB.getErrorString("tran_id","OVRRIDFAIL",userId,"",conn);
			}
			if( overridePassed.size() > 0  &&  overrideFailed.size() > 0 )
			{
				errString = itmDBAccessEJB.getErrorString("tran_id","OVRPARTIAL",userId,"",conn);
				
				 System.out.println("errString@@@>>>["+errString+"]");
				 String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
				 String endPart = errString.substring( errString.indexOf("</trace>"));
				 String mainStr=", Override Failed for Transaction Id :"+overrideFailed+"";
				
				 mainStr=begPart+mainStr+endPart;
				
				 errString = mainStr;
				 System.out.println("@@@@@ modify errString["+errString+"]");
			}
			
		}
		catch(Exception e)
		{
			try{
				conn.rollback();
			}catch(Exception e1)
			{e1.printStackTrace();}

			System.out.println("Exception in ..."+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		}		
		finally
		{
			try
			{
				System.out.println("@@@@@@@@ overridePassed["+overridePassed+"]overrideFailed["+overrideFailed+"]");
				if(rs != null)rs.close();
				rs = null;
				if(pstmt != null)pstmt.close();
				pstmt = null;
				if(conn != null)conn.close();
				conn = null;	
			}
			catch(SQLException sqle)
			{
				sqle.printStackTrace();
				throw new ITMException(sqle);
			}
		}
		System.out.println("final errString["+errString+"]");
		return errString;
	} //end process

	public String businessLogicOverride(String businessObj,String tranId,String xtraParams,String forcedFlag,String eventCode ,Connection conn) throws Exception, ITMException
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

		try
		{

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,businessObj);
			pstmt.setString(2,eventCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println(">>>BUSINESS_LOGIC_CHECK serviceCode = "+serviceCode+" compName "+compName);
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,serviceCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println(">>>BUSINESS_LOGIC_CHECK serviceURI = "+serviceURI+" compName = "+compName);
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
			aobj[1] = new String(tranId);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String(forcedFlag);

			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);

			System.out.println(">>>>Return string from NVO is:==>["+retString+"]");
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

	public String checkNull(String s)
	{
		if( s==null )
			return " ";
		else
			return s;
	}

}//end class