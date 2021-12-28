

/********************************************************
	Title : SReturnAmd
	Date  : 09/01/12
	Developer: Kunal Mandhre

********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

public class SReturnAmd extends ValidatorEJB implements SReturnAmdLocal,SReturnAmdRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
    {
		System.out.println("wfValData() called from SReturnEjb... ");
	    String errString = "";
	    Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
	    }
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
    }
	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
    {
		String refId = "";
		
		String siteCode = ""; 
		String custCode = "";
		String tranCode="";
	String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String tranDate = "";
		Date refDate = null;
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/mm/yy");
		int cnt = 0;
		int ctr=0;
		int currentFormNo = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		System.out.println("Station Val Start");
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
						parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for(ctr = 0; ctr < childNodeListLength; ctr++)
						{
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							if(childNodeName.equalsIgnoreCase("ref_id"))//START
							{
								refId = checkNull(genericUtility.getColumnValue("ref_id", dom));
								System.out.println("Ref Id="+refId);
								if(refId == null || refId.trim().length() == 0)
								{
									System.out.println("REF ID BLANK");
									errCode = "VMREFIDBK ";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}
								else if(refId != null && refId.trim().length() > 0)
								{
									sql = "SELECT count(*) FROM SRETURN where tran_id = ? and status <> 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,refId);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt =  rs.getInt(1);
										System.out.println("COUNT = "+cnt);
									    
									}
									    if(cnt == 0) 
									    {
									    	errCode = "VMREFIDCN";
									    	errList.add(errCode);
									    	errFields.add(childNodeName.toLowerCase());
									    }
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "SELECT count(*) FROM SRETURN where tran_id = ? and status <> 'X' and confirmed = 'Y'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,refId);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt =  rs.getInt(1);
										System.out.println("COUNT = "+cnt);
									 
									}
									    if(cnt == 0) 
									    {
									    	errCode = "VMREFIDCON";
									    	errList.add(errCode);
									    	errFields.add(childNodeName.toLowerCase());
									    }
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "SELECT count(*) FROM SRETURN where tran_id = ? and status <> 'X' and confirmed = 'Y' and ret_opt = 'R' ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,refId);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt =  rs.getInt(1);
										System.out.println("COUNT = "+cnt);
									    
									}
									    if(cnt == 0) 
									    {
									    	errCode = "VMREFIDREP";
									    	errList.add(errCode);
									    	errFields.add(childNodeName.toLowerCase());
									    }
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
						
							}
							/*else if(childNodeName.equalsIgnoreCase("tran_id"))
							{
								tranId = checkNull(genericUtility.getColumnValue("tran_id", dom));
								System.out.println("TRAN Id="+tranId);
								if(tranId == null || tranId.trim().length() == 0)
								{
									errCode = "VMTRANID";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}
							}*/
							else if(childNodeName.equalsIgnoreCase("tran_date"))
							{
								/*
								try
								{
									tranDate = sdf2.parse(genericUtility.getColumnValue("tran_date", dom));
									System.out.println("tranDate="+tranDate);
								}
								catch (Exception e) 
								{
									errCode = "VMTRANDT";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}*/
								
								
								
								//tranDate = sdf2.parse(genericUtility.getColumnValue("tran_date", dom));
								tranDate= genericUtility.getColumnValue("tran_date", dom);
								//tranDate1 = getDate(genericUtility.getColumnValue("tran_date", dom));
								//System.out.println("tranDate="+tranDate1);
								if(tranDate == null)
								{
									errCode = "VMTRANDT";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}
								
							}
							else if(childNodeName.equalsIgnoreCase("ref_date"))
							{
								try
								{
									refDate = sdf2.parse(genericUtility.getColumnValue("ref_date", dom));
									System.out.println("ref_date="+refDate);
								}
								catch (Exception e) 
								{
									errCode = "VTSUBRRDN ";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}
								/*
								refDate = sdf2.parse(genericUtility.getColumnValue("ref_date", dom));
								System.out.println("ref_date="+refDate);
								if(refDate == null)
								{
									errCode = "VTSUBRRDN ";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}*/
							}
							else if(childNodeName.equalsIgnoreCase("site_code"))
							{
								siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
								System.out.println("sitecode="+siteCode);
								if(siteCode == null || siteCode.trim().length() == 0)
								{
									errCode = "VTMNULSITE";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}
							}
							else if(childNodeName.equalsIgnoreCase("cust_code"))
							{
								custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
								System.out.println("custCode="+custCode);
								if(custCode == null || custCode.trim().length() == 0)
								{
									errCode = "VMCUSTCD  ";
							    	errList.add(errCode);
							    	errFields.add(childNodeName.toLowerCase());
								}
							}
							
							//added by priyanka on 04/03/15 as per manoj sharma instruction
							else if(childNodeName.equalsIgnoreCase("tran_code"))
							{
								tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
								System.out.println("tranCode="+tranCode);
								if(tranCode != null && tranCode.trim().length() > 0)
								{
									sql = "SELECT count(*) FROM transporter where tran_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,tranCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt =  rs.getInt(1);
										System.out.println("COUNT = "+cnt);
									    
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
								    if(cnt == 0) 
								    {
								    	errCode = "INVTRANCOD";
								    	errList.add(errCode);
								    	errFields.add(childNodeName.toLowerCase());
								    }
									
								}
							}
							
						}
				break;
			}
			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					System.out.println("errCode="+errCode);
					System.out.println("errFldName="+errFldName);
					System.out.println("userId="+userId);
 					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
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
					conn.close();
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("itemChanged() SReturnAmd called ..........................");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SReturnAmd][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
        return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String refId = "";
		String custCode = ""; 
		String siteCode = "";
		String descr = "";
		String custName = "";
		String lrNo = "";
		String transMode = "";
		String lorryNo = "";
		String frtType = "";
		String frtAmt = "";
		String tranCode="";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int currentFormNo = 0;
		Date today = new Date();
		Date refDate = null;
		Date lrDate = null;
		SimpleDateFormat sdf2 = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
				case 1 : 
						parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						valueXmlString.append("<Detail1>");
						int childNodeListLength = childNodeList.getLength();
						do
						{   
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							ctr ++;
					    }while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
						if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					    {
							valueXmlString.append("<tran_date>").append("<![CDATA[" +  sdf2.format(today) + "]]>").append("</tran_date>");
							
						}
						else if(currentColumn.trim().equalsIgnoreCase("ref_id"))
						{
							refId = checkNull(genericUtility.getColumnValue("ref_id", dom));
							System.out.println("Ref Id ="+refId);
							sql = "select  tran_date,(case when site_code is null then ' ' else site_code end) as site_code,"+
								  "(case when cust_code is null then ' ' else cust_code end) as cust_code,lr_date,"+
								  "(case when lr_no is null then ' ' else lr_no end) as lr_no,"+
								  "(case when trans_mode is null then ' ' else trans_mode end) as trans_mode,"+
								  "(case when lorry_no is null then ' ' else lorry_no end) as lorry_no,"+
								  "(case when frt_type is null then ' ' else frt_type end) as frt_type,"+
								  "(case when frt_amt is null then 0 else frt_amt end) as frt_amt," +
								  "(case when tran_code is null then ' ' else tran_code end) as tran_code" +
								  " from sreturn where tran_id = ?";
							
							System.out.println("sql for tran date["+sql+"]");
							pstmt =conn.prepareStatement(sql);
							pstmt.setString(1,refId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								refDate = rs.getDate("tran_date");
								siteCode = rs.getString("site_code");
								custCode = rs.getString("cust_code");
								lrDate = rs.getDate("lr_date");
								lrNo = rs.getString("lr_no");
								transMode = rs.getString("trans_mode");
								lorryNo = rs.getString("lorry_no");
								frtType = rs.getString("frt_type");
								frtAmt = rs.getString("frt_amt");
								tranCode = rs.getString("tran_code");//added by priyanka on 03/03/15 as per manoj sharma instruction
								System.out.println("Value come ffrom item ch:");
								System.out.println("ref id ="+refId);
								System.out.println("ref date ="+refDate);
								System.out.println("site code ="+siteCode);
								System.out.println("cust code ="+custCode);
								System.out.println("lr date "+lrDate);
								System.out.println("lr no ="+lrNo);
								System.out.println("trans mode ="+transMode);
								System.out.println("lorry no ="+lorryNo);
								System.out.println("frt type ="+frtType);
								System.out.println("frt amd ="+frtAmt);
								System.out.println("tranCode ="+tranCode);//added by priyanka on 03/03/15 as per manoj sharma instruction
								
								valueXmlString.append("<ref_date>").append("<![CDATA[" + sdf2.format(refDate) +"]]>").append("</ref_date>");
								valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode +"]]>").append("</site_code>");
								valueXmlString.append("<cust_code>").append("<![CDATA[" + custCode +"]]>").append("</cust_code>");
								
								if(lrDate != null)
								{
									valueXmlString.append("<lr_date__o>").append("<![CDATA[" + sdf2.format(lrDate) +"]]>").append("</lr_date__o>");
									valueXmlString.append("<lr_date>").append("<![CDATA[" + sdf2.format(lrDate) +"]]>").append("</lr_date>");
								}
								else 
								{
									valueXmlString.append("<lr_date__o>").append("<![CDATA[]]>").append("</lr_date__o>");
									valueXmlString.append("<lr_date>").append("<![CDATA[]]>").append("</lr_date>");
								}
								
								valueXmlString.append("<lr_no__o>").append("<![CDATA[" + lrNo +"]]>").append("</lr_no__o>");
								valueXmlString.append("<lr_no>").append("<![CDATA[" + lrNo +"]]>").append("</lr_no>");
								valueXmlString.append("<trans_mode__o>").append("<![CDATA[" + transMode +"]]>").append("</trans_mode__o>");
								valueXmlString.append("<trans_mode>").append("<![CDATA[" + transMode +"]]>").append("</trans_mode>");
								valueXmlString.append("<lorry_no__o>").append("<![CDATA[" + lorryNo +"]]>").append("</lorry_no__o>");
								valueXmlString.append("<lorry_no>").append("<![CDATA[" + lorryNo +"]]>").append("</lorry_no>");
								valueXmlString.append("<frt_type>").append("<![CDATA[" + frtType +"]]>").append("</frt_type>");
								valueXmlString.append("<frt_amt>").append("<![CDATA[" + frtAmt +"]]>").append("</frt_amt>");
								valueXmlString.append("<frt_type__o>").append("<![CDATA[" + frtType +"]]>").append("</frt_type__o>");
								valueXmlString.append("<frt_amt__o>").append("<![CDATA[" + frtAmt +"]]>").append("</frt_amt__o>");
								
								
								//added by priyanka on 03/03/15 as per manoj sharma instruction
								
								valueXmlString.append("<tran_code__o>").append("<![CDATA[" + tranCode +"]]>").append("</tran_code__o>");
								valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode +"]]>").append("</tran_code>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("SITE CODE="+siteCode);
							sql = "select descr from site where site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1);
								valueXmlString.append("<descr>").append("<![CDATA[" + descr +"]]>").append("</descr>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("CUST CODE="+custCode);
							sql = "select cust_name from customer where cust_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custName = rs.getString(1);							
								valueXmlString.append("<cust_name>").append("<![CDATA[" + custName +"]]>").append("</cust_name>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						/*else if(currentColumn.trim().equalsIgnoreCase("site_code"))
					    {
							System.out.println("case for site code");
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							System.out.println("274 SiteCode"+siteCode);
							sql = "select descr from site where site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1);
								valueXmlString.append("<site_descr>").append("<![CDATA[" + descr +"]]>").append("</site_descr>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						else if (currentColumn.trim().equalsIgnoreCase("cust_code")) 
						{
							custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
							System.out.println("Cusst Code="+custCode);
							sql = "select cust_name from customer where cust_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custName = rs.getString(1);							
								valueXmlString.append("<customer_cust_name>").append("<![CDATA[" + custName +"]]>").append("</customer_cust_name>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}*/
						
						valueXmlString.append("</Detail1>");
				break;       
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if(pstmt != null)
					pstmt.close();
					if(rs != null)
					rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}	
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private Date getDate(String input)
	{
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yy");
			return (sdf.parse(input));
		}
		catch(Exception e) 
		{
			return null;
		}
	}
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
}	
