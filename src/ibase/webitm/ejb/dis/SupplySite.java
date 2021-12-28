/********************************************************
	Title : SupplySiteEJB
	Date Of Modification : 17/3/08 
	Name of Developer : Nisar S. Khatib

********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import java.text.NumberFormat;
import java.io.*;

//import javax.ejb.SessionBean;
import javax.ejb.CreateException;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class SupplySite extends ValidatorEJB //implements SessionBean
{
	//GenericUtility genericUtility = GenericUtility.getInstance(); 
	E12GenericUtility genericUtility = new E12GenericUtility();
	java.sql.PreparedStatement pstmt=null;
	java.sql.ResultSet rs = null;

	String sql = null;
	String sundryType = null;
	String  errString = "";
	String loginSite = "";

	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("SupplySiteEJB is in Process..........");
	}
	public void ejbRemove()
	{
		System.out.println("SupplySiteEJB is Removed from memory..........");
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;	
		System.out.println("Validation Start..........");

		try
		{
			System.out.println("xmlString:::"+xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);			
		}
		catch(Exception e)
		{
			System.out.println("Exception : SupplySiteEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		
		String columnValue = null;
		String childNodeName = null;
		String errString = "";
		String errCode = "";		
		String sql = "";
		String userId = ""; 		
		String custCode=null;
		String itemType=null,sampleDate=null,designDate=null;	
		String siteCode=null,siteCodeSupp = null;
		
		int ctr=0;
		int currentFormNo=0;
		int childNodeListLength;
		int counter = 0,counter1 = 0;
		int prefNo = 0;
		
		try
		{
			ConnDriver connDriver = new ConnDriver();	
			//Changes and Commented By Poonam on 08-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Poonam on 08-06-2016 :END

			connDriver = null;
			System.out.println("[SupplySiteEJB]connection 1 is opened......");
			stmt = conn.createStatement();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
		
			//GenericUtility genericUtility = GenericUtility.getInstance(); 
			E12GenericUtility genericUtility = new E12GenericUtility();
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
						
						if (childNodeName.equalsIgnoreCase("site_code"))
						{
							siteCode = getColumnValue("site_code",dom)==null?"": getColumnValue("site_code",dom).trim();

							if(siteCode!=null && siteCode.trim().length() == 0)
							{
								System.out.println("[SupplySiteEJB]------Site Cannot be Empty!---VMSITECD1  -----------");
								errString = getErrorString("siteCode","VMSITECD1 ",userId);
								break;
							}
						}
						else if (editFlag.equals("A") && childNodeName.equalsIgnoreCase("site_code__supp"))
						{
							siteCode = getColumnValue("site_code",dom)==null?"": getColumnValue("site_code",dom).trim();
							siteCodeSupp = getColumnValue("site_code__supp",dom)==null?"":getColumnValue("site_code__supp",dom).trim();
							prefNo = Integer.parseInt(getColumnValue("pref_no",dom)==null?"0":getColumnValue("pref_no",dom).trim());


							if(siteCodeSupp!=null && siteCodeSupp.trim().length() == 0)
							{
								System.out.println("[SupplySiteEJB]------Site Cannot be Empty!---VMSITECD1  -----------");
								errString = getErrorString("site_code__supp","VMSITECD1 ",userId);
								break;
							}

							sql="SELECT COUNT(1) AS COUNTER FROM SITE WHERE SITE_CODE='"+siteCodeSupp+"'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								counter = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(counter==0)
							{
								System.out.println("[SupplySiteEJB]-------Invalid site!---VTDESP3 -----------");
								errString = getErrorString("site_code__supp","VTDESP3   ",userId);
								break;
							}

							sql="SELECT COUNT(1) AS COUNTER FROM SUPPLY_SITES "
							+" WHERE site_code='"+siteCode+"' AND SITE_CODE__SUPP='"+siteCodeSupp+"'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								counter = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql="SELECT COUNT(1) AS COUNTER FROM SUPPLY_SITES "
							+" WHERE site_code='"+siteCode+"' AND pref_no="+prefNo;
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								counter1 = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							if(counter > 0)
							{
								System.out.println("[SupplySiteEJB]------Can not duplicate site code and site code supp----------");
								errString = getErrorString("site_code__supp","VTSUPSI   ",userId);
								break;
							}
							if(counter1 > 0)
							{
								System.out.println("[SupplySiteEJB]------Preference no. can't be same for same Site----------");
								errString = getErrorString("pref_no","VTPREFNO1",userId);
								break;
							}
								
						}
						else if (editFlag.equals("E") && childNodeName.equalsIgnoreCase("site_code__supp"))
						{
							siteCode = getColumnValue("site_code",dom)==null?"": getColumnValue("site_code",dom).trim();
							prefNo = Integer.parseInt(getColumnValue("pref_no",dom)==null?"0":getColumnValue("pref_no",dom).trim());

							sql="SELECT COUNT(1) AS COUNTER FROM SUPPLY_SITES "
							+" WHERE site_code='"+siteCode+"' AND pref_no="+prefNo;
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								counter1 = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							if(counter1 > 0)
							{
								System.out.println("[SupplySiteEJB]------Preference no. can't be same for same Site----------");
								errString = getErrorString("pref_no","VTPREFNO1",userId);
								break;
							}
						}
					}break;//END FOR	
			}//END SWITCH
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					conn.close();
					conn = null;
					System.out.println("[SupplySiteEJB]connection 1 is closed......");
				}
			}catch(Exception d){d.printStackTrace();}			
			System.out.println("[SupplySiteEJB] Connection is Closed");
		}
		System.out.println("ErrString ::"+errString);
		return errString;
	}//END OF VALIDATION 

	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";

		try
		{
			dom = parseString(xmlString); 
			System.out.println("xmlString" + xmlString);
			System.out.println("[SupplySiteEJB]itemChanged Called...........");
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SupplySiteEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
        return valueXmlString; 
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		int currentFormNo = 0,ctr = 0;
		int prefNo = 0;
		String columnValue = null;
		String descr = null;
		String childNodeName = null;
		String siteCode = null;

		StringBuffer valueXmlString = new StringBuffer();
		
		try
		{
			ConnDriver connDriver = new ConnDriver();	
			//Changes and Commented By Poonam on 08-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Poonam on 08-06-2016 :END

			System.out.println("[SupplySiteEJB]connection 2 is opened......");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			siteCode = getValueFromXTRA_PARAMS(xtraParams, "site_code");
		
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO:::"+currentFormNo);	
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			valueXmlString.append("<Detail>");	

			switch(currentFormNo)
			{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					int childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println("[SupplySiteEJB]Column Value=>"+columnValue);
					System.out.println("[SupplySiteEJB]currentColumn=>"+currentColumn);

					if (currentColumn.trim().equals("itm_default"))
					{
						System.out.println("**************************ITMDEFAULT CALLING********************************** ");	
						if("-1".equals(siteCode))
						{
							loginSite = loginSite;
						}
						else
						{
							loginSite = siteCode;
						}
						valueXmlString.append("<site_code>").append("<![CDATA["+loginSite+"]]>").append("</site_code>"); 			
						sql="select descr from site where site_code='"+loginSite+"'";
						System.out.println("--------sql->"+sql);
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							descr = rs.getString("descr")==null?"":rs.getString("descr").trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql="SELECT MAX(PREF_NO)+1 AS PREF_NO FROM SUPPLY_SITES WHERE SITE_CODE='"+loginSite+"'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							prefNo = rs.getInt("PREF_NO");
						}
						valueXmlString.append("<pref_no>").append("<![CDATA["+prefNo+"]]>").append("</pref_no>");
						valueXmlString.append("<site_descr>").append("<![CDATA["+descr+"]]>").append("</site_descr>");
					}
					else if(currentColumn.trim().equals("site_code"))
					{
						sql="select descr from site where site_code='"+columnValue+"'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							descr = rs.getString("descr")==null?"":rs.getString("descr").trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_descr>").append("<![CDATA["+descr+"]]>").append("</site_descr>");
					}
					else if(currentColumn.trim().equals("site_code__supp"))
					{
						sql="select descr from site where site_code='"+columnValue+"'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							descr = rs.getString("descr")==null?"":rs.getString("descr").trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_descr1>").append("<![CDATA["+descr+"]]>").append("</site_descr1>");
					}
					else if(currentColumn.trim().equals("itm_defaultedit"))
					{
						columnValue = getColumnValue("site_code__supp",dom);
						sql="select descr from site where site_code='"+columnValue+"'";
						System.out.println("sql==>"+sql);
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							descr = rs.getString("descr")==null?"":rs.getString("descr").trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_descr1>").append("<![CDATA["+descr+"]]>").append("</site_descr1>");
					}
					valueXmlString.append("</Detail>");
				break;			
			}//end of switch
			valueXmlString.append("</Root>");
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					conn.close();					
					conn = null;
					System.out.println("[SupplySiteEJB]connection 2 is closed......");
				}
			}catch(Exception d){d.printStackTrace();}			
			System.out.println("[SupplySiteEJB] Connection is Closed");
		}
		System.out.println("valueXmlString:::::"+valueXmlString.toString());
		return valueXmlString.toString();
	}//END OF ITEMCHNGE	
}