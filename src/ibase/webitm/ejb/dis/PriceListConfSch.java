/********************************************************
	Title 	 : PriceListConfSch
	Date  	 : 24/DEC/15
	Developer: Priyanka Shinde.
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
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.ejb.*;
import ibase.system.config.*;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.*;
import java.net.InetAddress;

import ibase.webitm.ejb.mfg.MfgCommon;
import ibase.webitm.ejb.sys.UtilMethods;

public class PriceListConfSch implements Schedule
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//FileOutputStream fos1 = null;
	@Override
	public String schedule(String scheduleParamXML)throws Exception
	{
		String siteCode = "",loginSiteCode="",errString="";
		String xtraParams = "";
		ibase.utility.UserInfoBean userInfo = null;
		 ConnDriver connDriver = new ConnDriver();
		 Connection conn = null;
		 PreparedStatement pstmt=null;
		 ResultSet rs=null;
		String runMode="";
		String tranIdFr="",tranIdTo="";
		try
		{
			
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			System.out.println("************ ["+scheduleParamXML+"]");
			userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
			siteCode = userInfo.getSiteCode();
			xtraParams = "loginEmpCode="+userInfo.getEmpCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+loginSiteCode;
			
			userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
			loginSiteCode = userInfo.getSiteCode();
            runMode="I";
			xtraParams = "loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+loginSiteCode+"~~loginEmpCode="+userInfo.getEmpCode()+"~~runMode="+runMode;
			
			
			//GenericUtility genericUtility=GenericUtility.getInstance();
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
							
				if(childNodeName!=null && !"#text".equalsIgnoreCase(childNodeName))
				{
					if(ctr==0)
					{
						tranIdFr=childNode.getFirstChild().getNodeValue();
					}
					if(ctr==1)
					{
						tranIdTo=childNode.getFirstChild().getNodeValue();
					}
								
				}
			}
			System.out.println("tranIdFr----["+tranIdFr+"]");
			System.out.println("tranIdTo----["+tranIdTo+"]");
			
			priceListConfirm(xtraParams, tranIdFr, tranIdTo,conn);
			 
			
		}
		catch(Exception e)
		{
			System.out.println("Exception :SplitSchedularSOrder :schedule :Exception :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				
				
				if(conn!=null)
				{
					conn.commit();
					conn.close();
					conn=null;
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception"+e.getMessage());

			}
		}
		System.out.println("errString>>>["+errString+"]");
		return errString;
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
	
		
	
	 public void priceListConfirm(String xtraParams,String tranIDFrom,String tranIDTo,Connection conn) throws ITMException
	 {
		
		 PreparedStatement pstmt=null;
		 ResultSet rs=null;
		 
		 String sql="";
		 String tranId="",retString="",userId="",loginSite="",sysDate="",termId="",empCode="";
				
		 userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		 loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		 termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );
		 empCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginEmpCode" );
		 int cnt=0;
		 try
		 {	
						
			 java.util.Date today=new java.util.Date();
			 Calendar cal = Calendar.getInstance(); 
			 cal.setTime(today); 
			 today = cal.getTime();
			 SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			 sysDate=sdf.format(today);
			 System.out.println("System date  :- ["+sysDate+"]"); 
			
			 ArrayList<String> tranIdList=new ArrayList<String>();
			 sql="select tran_id from pricelist_hdr where  tran_id between ? and ? " +
			 		"and CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END='N'";
			 pstmt=conn.prepareStatement(sql);
			 pstmt.setString(1,tranIDFrom);
			 pstmt.setString(2,tranIDTo);
			 rs=pstmt.executeQuery();
			 while(rs.next())
			 {
				 tranIdList.add(rs.getString(1)); 
			 }
		
			 rs.close();
			 rs=null;
			 pstmt.close();
			 pstmt=null;
			 System.out.println("tranIdList["+tranIdList+"]");
			 if(tranIdList.size()>0)
			 {
				 for(int k=0;k<tranIdList.size();k++)
				 {
				 tranId=tranIdList.get(k);
				 
				/* pstmt1=conn.prepareStatement("select tran_id from pricelist_hdr where tran_id=?" +
				 		" and CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END='N' for update nowait");
				 */
				 
				 pstmt=conn.prepareStatement("select count(1) from pricelist_hdr where tran_id=?" +
					 		" and CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END='N'");
				 
				 pstmt.setString(1, tranId);
				 rs=pstmt.executeQuery();
				 if(rs.next())
				 {
					 //tranId=rs1.getString(1);
					 cnt=rs.getInt(1);
					 
				 }
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				 System.out.println("Getting tranId======"+tranId);
				 if(cnt==1)
				 {
					 retString=confirmTran("pricelist_tran",tranId,xtraParams,"",conn);
						
						System.out.println("retString from priceListConfirm========="+retString);
						if((retString != null ) && (retString.indexOf("VTSUCC1") > -1))
						{
							conn.commit();
							
							System.out.println(">>>>>>>>>retString.indexOf(VTSUCC1) :"+retString.indexOf("VTSUCC1"));
							System.out.println(">>>>After Commit Confirm Completed:"+tranId);								
													
						}
						else
						{
							System.out.println("["+tranId+"]failed");
						} 
				 }
					
				 //}
				// rs1.close();
				// rs1=null;
				// pstmt1.close();
				// pstmt1=null;
				 
				 }
			 
			 }
						 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);
		 }
		 finally
		 {
			 if(rs!=null)
			 {
				 try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 if(pstmt!=null)
			 {
				 try 
				 {
					pstmt.close();
				}
				 catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			
					 
		 }		 
	 }
	
	 
	 
	 public String confirmTran(String businessObj,String tranId,String xtraParams,String forceFlag,Connection conn) throws ITMException
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
				sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,businessObj);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					serviceCode = rs.getString("SERVICE_CODE");
					compName = rs.getString("COMP_NAME");
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				System.out.println("serviceCode = "+serviceCode+" compName "+compName);
				
				sql = "SELECT SERVICE_URI,METHOD_NAME FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,serviceCode);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					methodName= rs.getString("METHOD_NAME");
					serviceURI = rs.getString("SERVICE_URI");
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				actionURI = "http://NvoServiceurl.org/" + methodName;
				System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
				
				Service service = new Service();
				Call call = (Call)service.createCall();
				call.setTargetEndpointAddress(new java.net.URL(serviceURI));
				call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
				call.setUseSOAPAction(true);
				call.setSOAPActionURI(actionURI);
				Object[] aobj = new Object[4];

				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

				aobj[0] = new String(compName);
				aobj[1] = new String(tranId);
				aobj[2] = new String(xtraParams);
				aobj[3] = new String("");
				
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
