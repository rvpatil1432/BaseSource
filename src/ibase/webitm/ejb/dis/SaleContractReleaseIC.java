/*Author : Priyanka Chavan.
 * Date : 31JAN2018
 * */
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
@Stateless
public class SaleContractReleaseIC extends ValidatorEJB implements SaleContractReleaseICLocal, SaleContractReleaseICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			/*System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}*/
			System.out.println( "xmlString inside wfValData :::::::" + xmlString);
			System.out.println( "xmlString1 inside wfValData :::::::" + xmlString1);
			System.out.println( "xmlString2 inside wfValData :::::::" + xmlString2);
			
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
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
		int currentFormNo = 0;
		String errString ="";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{
			conn = getConnection();
		
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			    case 1 :
				break;
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
					conn = null;
				}
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
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		//errString = errStringXml.toString();
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
			System.out.println("Exception : [ItemRegNo][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int currentFormNo = 0;
		String loginSite = "";
		NodeList parentList = null;
		int childNodeLength = 0,ctr = 0;
		String childNodeName = "",objName = "";
		String columnValue="";
		int childNodeListLength = 0,cnt=0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		E12GenericUtility genericUtility = new E12GenericUtility();
		String contractNo = "",sql="",custCode ="",custName = "";
		
		try
		{
			conn = getConnection();
		//	sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("Inside ItemChange : ");
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("[IC] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			switch(currentFormNo)
			{
				case 1 :
				{					
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentList = dom.getElementsByTagName( "Detail" + currentFormNo );
	    			//objName = getObjNameFromDom( dom, "objName", "1" );
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					valueXmlString.append("<Detail1>");
					childNodeListLength = childNodeList.getLength();
					System.out.println("currentColumn-------->>[" + currentColumn + "]"+"objName["+objName+"]");
				
					if("itm_default".equalsIgnoreCase(currentColumn.trim()))
					{
						
					}
					else if("contract_no".equalsIgnoreCase(currentColumn.trim()))
					{
						contractNo = genericUtility.getColumnValue("contract_no", dom);
						System.out.println("contractNo : " +contractNo);
						sql = "select CUST_CODE from scontract where CONTRACT_NO  = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, contractNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custCode = rs.getString(1)==null?"":rs.getString(1);							
						}
						System.out.println("custCode : " +custCode);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<cust_code >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code>");
						
						sql = " select cust_name from customer where cust_code   = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custName = rs.getString(1)==null?"":rs.getString(1);							
						}
						System.out.println("custName : " +custName);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<cust_name >").append("<![CDATA[" +  custName + "]]>").append("</cust_name>");
					
					}
					valueXmlString.append("</Detail1>");
					break;
				}
			}
			         valueXmlString.append("</Root>");
		}
		
		catch(Exception e)
		{
			System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} 
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		
		return valueXmlString.toString();
	}
}
