/********************************************************
	Title : ItemRegNoIC
	Date  : 11/04/2012
	Developer: Mahesh Patidar
 ********************************************************/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class ItemRegNoIC extends ValidatorEJB implements ItemRegNoICLocal, ItemRegNoICRemote
{
	//changed by nasruddin 07-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
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
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		int childNodeListLength;
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String siteCode = "";
		String refCode = "";
		String itemCode = "";
		String errFldName = "";
		String userId = "";
		String sql = "";
		long cnt = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("site_code")) 
				{
					siteCode = genericUtility.getColumnValue("site_code", dom);
					if(siteCode == null || siteCode.trim().length() == 0)
					{
						errCode = "VMSITECD1 ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					if(siteCode != null && siteCode.trim().length() > 0)
					{
						sql = " SELECT COUNT(*) FROM site WHERE site_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
							if(cnt == 0)
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				
				}
				/* Comment By Nasruddin 19-SEP-16 [START]
				 * if(childNodeName.equalsIgnoreCase("item_code")) 
				{
					itemCode = genericUtility.getColumnValue("item_code", dom);
					if(itemCode == null || itemCode.trim().length() == 0)
					{
						errCode = "VMITEMCD1 ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				if(childNodeName.equalsIgnoreCase("ref_code")) 
				{
					refCode = genericUtility.getColumnValue("ref_code", dom);
					if(refCode == null || refCode.trim().length() == 0)
					{
						errCode = "VMREFCD1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}Comment By Nasruddin 19-SEP-16 [END]*/
				
			}
			int errListSize = errList.size();
			cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
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
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ItemRegNoIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		int ctr = 0;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String itemCode = "";
		String siteCode = "";
		String siteDescr = "";
		String sql = "";
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
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
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
				itemCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "item_code"));
				valueXmlString.append("<item_code protect=\"1\">").append("<![CDATA[" +  itemCode + "]]>").append("</item_code>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("site_code"))
			{
				siteCode = genericUtility.getColumnValue("site_code", dom);
				sql = " Select descr from site where site_code = ? ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					siteDescr = rs.getString(1);
					valueXmlString.append("<site_descr protect=\"1\">").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");
				}
				else
				{
					valueXmlString.append("<site_descr protect=\"1\">").append("<![CDATA[]]>").append("</site_descr>");
				}
			}
			valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
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

	private String errorType(Connection conn , String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
