/*  DEVELOP BY RITESH ON 04-FEB-16 
 * PURPOSE: To Migrate  A. Scheme Applicability
 * */
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.*;
//import java.util.regex.Pattern;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
//import java.sql.Timestamp;
@Stateless  

public class SreturnNormsIC extends ValidatorEJB implements SreturnNormsICLocal, SreturnNormsICRemote
{
	    //changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("wfValdata() called for SreturnNormsIC");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
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
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int cnt = 0;
		int ctr=0;
		int childNodeListLength =0 ;NodeList parentNodeList = null;
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
		String keyFlag = "";
		int currentFormNo =0,recCnt=0,count= 0;
		int count2= 0;
		String empCode = "";
		try
		{	
			System.out.println("@@@@@@@@ wfvaldata called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
//			sql = " select key_flag from transetup where tran_window = 'w_scheme_appl' ";
//			pstmt = conn.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//			if(rs.next())
//			{
//				keyFlag = rs.getString(1)==null?"": rs.getString(1);
//			}
//			rs.close();
//			rs = null;
//			pstmt.close();
//			pstmt = null;
			if(objContext != null && objContext.trim().length()>0)
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
					if( childNodeName.equalsIgnoreCase("days_from") && "A".equals(editFlag)  )
					{
						String daysFrom = checkNull(genericUtility.getColumnValue("days_from", dom));
						/* Comment By Nasruddin 21-SEP-16 Start 
						String daysTo = checkNull(genericUtility.getColumnValue("days_to", dom));
						String deductPerc = checkNull(genericUtility.getColumnValue("deduct_perc", dom));
						if(daysFrom.trim().length() <= 0)
						{
							errCode = "VMDAYFM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(daysTo.trim().length() <= 0)
						{
							errCode = "VMDAYFTO1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(deductPerc.trim().length() <= 0)
						{
							errCode = "VMDEDPERC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(Pattern.matches("[0-9]+", daysFrom) == false)
						{
							errCode = "VMDAYFM2"; // Only numeric allow
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (Integer.parseInt(daysTo) < Integer.parseInt(daysFrom))
						{
							errCode = "VMNORMS01";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{*/
						    count2 = 0;
							sql = " select count(*) from sreturn_norms where days_from <= ? and days_to >= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setInt(1,Integer.parseInt(daysFrom));
							pstmt.setInt(2,Integer.parseInt(daysFrom));
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count2 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(count2 > 0)
							{
								errCode = "VMNORMS02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						//} Comment By Nasruddin 21-SEP-16 END
						
					}
					if(childNodeName.equalsIgnoreCase("days_to ")  && "A".equals(editFlag) )
					{
						String daysTo = checkNull(genericUtility.getColumnValue("days_to", dom));
						String daysFrom = checkNull(genericUtility.getColumnValue("days_from", dom));
						/* Comment By Nasruddin 21-SEP-16 START
						String deductPerc = checkNull(genericUtility.getColumnValue("deduct_perc", dom));
						if(daysTo.trim().length() <= 0)
						{
							errCode = "VMDAYFTO1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (daysFrom.trim().length() <= 0)
						{
							errCode = "VMDAYFM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(deductPerc.trim().length() <= 0)
						{
							errCode = "VMDEDPERC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(Pattern.matches("[0-9]+", daysTo) == false)
						{
								errCode = "VMDAYFM2"; // Only numeric allow
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
						}
						 Comment By Nasruddin 21-SEP-16 END */
						if (Integer.parseInt(daysTo) < Integer.parseInt(daysFrom))
						{
							errCode = "VMNORMS01";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = " select count(*) from sreturn_norms where days_from <= ? and days_to >= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setInt(1,Integer.parseInt(daysTo));
							pstmt.setInt(2,Integer.parseInt(daysTo));
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count2 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(count2 > 0)
							{
								errCode = "VMNORMS02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}

					}
					
				} // end for
				//break;  // case 1 end
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
					errString = getErrorString(errFldName, errCode, userId);
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
	}//end of validation


//	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
//	{
//		Document dom = null;
//		Document dom1 = null;
//		Document dom2 = null;
//		System.out.println("itemChanged() called for SreturnNormsIC");
//		String valueXmlString = "";
//		try
//		{   
//			System.out.println("xmlString::060515--"+xmlString);
//			if(xmlString != null && xmlString.trim().length() > 0)
//			{
//				dom = parseString(xmlString);
//			}
//			if(xmlString1 != null && xmlString1.trim().length() > 0)
//			{
//				dom1 = parseString(xmlString1);
//			}
//			if(xmlString2 != null && xmlString2.trim().length() > 0)
//			{
//				dom2 = parseString(xmlString2);
//			}
////			if(dom != null)
////			{
//			System.out.println("if(dom != null) :: N");
//			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
////			}
//		}
//		catch(Exception e)
//		{
//			System.out.println("Exception : [SreturnNormsIC][itemChanged( String, String )] :==>\n" + e.getMessage());
//		}
//		return valueXmlString;
//	}

	// method for item change
//	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
//	{
//		String childNodeName = null;
//		String sql = "";
//		StringBuffer valueXmlString = new StringBuffer();
//		int ctr = 0;
//		NodeList parentNodeList = null;
//		NodeList childNodeList = null;
//		Node parentNode = null;
//		Node childNode = null;
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null ;
//		GenericUtility genericUtility = GenericUtility.getInstance();
//		ConnDriver connDriver = new ConnDriver();
//		String columnValue="";
//		int currentFormNo =0;
//		String sitecdFmDescr = "",finEntity = "",finADescr= "",finBDescr="",sitecdToDescr="";
//		String bankDescr = "",acctDescr = "";
//		String empLname = "",empFname= "";
//		String empCodeLogin = "",loginSite="";
//		String ordetType = "",schemeCode="",itemCode="",itemDescr="",slabOn="",lineNo="",siteCodeDescr="",siteCode="";
//		String stateCode="",stateCodeDescr = "",countCode="",countCodeDescr="";
//		try
//		{
//			Calendar currentDate = Calendar.getInstance();
//			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
////			String sysDate = sdf.format(currentDate.getTime());
//			String sysDate = sdf.format(new Date());
//			System.out.println("Current Date ::["+new Date()+"]");
//			System.out.println("Application date format is :=>  " + sysDate);
//
//			conn = connDriver.getConnectDB("DriverITM");
//			conn.setAutoCommit(false);
//			connDriver = null;
//
//			if(objContext != null && objContext.trim().length()>0)
//			{
//				currentFormNo = Integer.parseInt(objContext.trim());
//			}
//			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
//			valueXmlString.append(editFlag).append("</editFlag> </header>");
//
//			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
//			switch(currentFormNo)
//			{
//			case 1 :
//			
//				parentNodeList = dom.getElementsByTagName("Detail1");
//				parentNode = parentNodeList.item(0);
//				childNodeList = parentNode.getChildNodes();
//
//				ctr = 0; 
//				valueXmlString.append("<Detail1>");
//				int childNodeListLength = childNodeList.getLength();
//				do
//				{ 
//					childNode = childNodeList.item(ctr);
//					childNodeName = childNode.getNodeName();
//					if(childNodeName.equals(currentColumn))
//					{
//						if (childNode.getFirstChild()!= null)
//						{
//							columnValue = childNode.getFirstChild().getNodeValue().trim();
//						}
//					}
//					ctr++;
//				}
//				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
//				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
//
//				String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
//				
//				loginSite = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
//
//				String chgUser = this.genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
//			     
//			    String chgTerm = this.genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
//			    
//				
//				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
//				{
//					System.out.println("@@@@@@@@ itm_default called @@@@@@@@");
//					//ordetType = checkNull(genericUtility.getColumnValue("order_type", dom));
//					ordetType = new DistCommon().getDisparams("999999", "SCHEME_ORDTYPE", conn);
//					if(!"NULLFOUND".equals(ordetType))
//						valueXmlString.append("<order_type>").append("<![CDATA["+ordetType+"]]>").append("</order_type>");
//				}
//				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
//				{
//					schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
//					valueXmlString.append("<scheme_code protect = \"1\">").append("<![CDATA["+schemeCode+"]]>").append("</scheme_code>");
//				}
////				
//						
//
//				valueXmlString.append("</Detail1>");
//				break;
//				// case 2 start
//		
//			}	// case 2 end
//			valueXmlString.append("</Root>");
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			System.out.println("Exception ::"+ e.getMessage());
//			throw new ITMException(e);
//		}
//		finally
//		{
//			try
//			{
//				if(conn != null)
//				{
//					if(rs != null) 
//					{
//						rs.close();
//						rs = null;
//					}
//					if(pstmt != null) 
//					{
//						pstmt.close();
//						pstmt = null;
//
//					}
//					conn.close();
//				}
//				conn = null;	
//			}
//			catch(Exception d)
//			{
//				d.printStackTrace();
//			}			
//		}
//		return valueXmlString.toString();
//	}
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
}