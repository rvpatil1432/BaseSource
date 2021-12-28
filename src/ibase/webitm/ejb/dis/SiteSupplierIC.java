

/********************************************************
	Title : SiteSupplierIC
	Date  : 04/10/11
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
import javax.ejb.Stateless; 
@Stateless 

public class SiteSupplierIC extends ValidatorEJB implements SiteSupplierICLocal, SiteSupplierICRemote
{
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
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
		String channelPartner = "";
		String siteCode = "";
		String supplierCode = "";
		String siteCodeCh = "";
		String siteCodePay = "";
		String crmTerm = "";
		String taxClass = "", lockGroup = "";
		String taxEnv = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "", currCode = "";
		int cnt = 0;
		int ctr=0;
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
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");

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
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					if(siteCode != null && siteCode.trim().length() > 0)
					{
						sql = "SELECT COUNT(*) FROM site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}

				else if(childNodeName.equalsIgnoreCase("supp_code"))
				{    
					supplierCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					if(supplierCode != null && supplierCode.trim().length() > 0 )
					{
						sql = "SELECT COUNT(*) FROM supplier where supp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,supplierCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VMSUPP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("site_code__ch"))
				{    
					channelPartner = checkNull(genericUtility.getColumnValue("channel_partner", dom));
					siteCodeCh = checkNull(genericUtility.getColumnValue("site_code__ch", dom));
					System.out.println("siteCodeCh"+siteCodeCh);
                   // Changed By nasruddin 22-SEP-16 START
					if(channelPartner != null && channelPartner.equalsIgnoreCase("Y"))
					{	
						if(siteCodeCh == null || siteCodeCh.trim().length() < 0)
						{
							errCode = "VTCUSTCD5";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					/*
					if(siteCodeCh != null && siteCodeCh.trim().length() > 0)
					{
						sql = "SELECT COUNT(*) FROM site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeCh);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					Changed by Nasruddin 22-SEP-16 END*/
				}
				else if(childNodeName.equalsIgnoreCase("site_code__pay"))
				{    
					siteCodePay = checkNull(genericUtility.getColumnValue("site_code__pay", dom));
					System.out.println("siteCodePay = "+siteCodePay);
					if(siteCodePay != null && siteCodePay.trim().length() > 0)
					{

						sql = "SELECT COUNT(*) FROM site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCodePay);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
																
						}
						if(cnt == 0) 
						{
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				// Changed By Nasruddin START 22-SEP-16
				else if(childNodeName.equalsIgnoreCase("curr_code"))
				{    
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					if(currCode != null && currCode.trim().length() > 0)
					{
						sql = "SELECT COUNT(1) FROM currency where curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,currCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}/*Changed By Nasruddin END 22-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("cr_term"))
				{    
					crmTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
					System.out.println("crm term ="+crmTerm);
					if(crmTerm != null && crmTerm.trim().length() > 0)
					{
						sql = "select count(*) from crterm where cr_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,crmTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VMCRTER1  ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				
				else if(childNodeName.equalsIgnoreCase("tax_class"))
				{    
					taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
					System.out.println("taxClass ="+taxClass);
					if(taxClass != null && taxClass.trim().length() > 0)
					{
						sql = "select count(*) from taxclass where tax_class = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxClass);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VTTAXCLA1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("tax_env"))
				{    
					taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
					System.out.println("taxEnv ="+taxEnv);
					if(taxEnv != null && taxEnv.trim().length() > 0)
					{
						sql = "select count(*) from taxenv where tax_env = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxEnv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						if(cnt == 0) 
						{
							errCode = "VMTAENV1  ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				*/
				
				//Changed By Nasruddin 22-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("lock_group"))
				{    
					lockGroup = genericUtility.getColumnValue("lock_group", dom);
					
					if(lockGroup != null && lockGroup.trim().length() > 0)
					{
						cnt = 0;
						sql = "SELECT COUNT(1) FROM LOCK_GROUP Where LOCK_GROUP = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lockGroup);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt(1);
							
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						
						if( cnt ==  0 )
						{
							errCode = "VMLOCKCODE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//Changed By Nasruddin 22-SEP-16 END

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

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("itemChanged () called for SiteSupplierIC ");
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SiteSupplierIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String lsNull = null;
		String siteCode = ""; 
		String supplierCode = "";
		String supplierType = "";
		String supplierName = "";
		String channelPartner = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
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
				System.out.println("itm_default K");
				//siteCode = genericUtility.getColumnValue("site_code", dom);
				siteCode =  checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
				System.out.println("Site code="+siteCode);
				valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
				if(siteCode != null &&  siteCode.trim().length() > 0)
				{
					valueXmlString.append("<site_code protect = \"1\">").append("<![CDATA[" + "" + "]]>").append ("</site_code>");
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("supp_code"))
			{
				supplierCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
				if(supplierCode != null && supplierCode.trim().length() > 0)
				{
				sql = " select supp_type,supp_name  from supplier where supp_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,supplierCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					supplierType = rs.getString(1);
					System.out.println("Supp typ="+supplierType);
					supplierName = rs.getString(2);
					System.out.println("supp Name="+supplierName);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				}
				valueXmlString.append("<supp_type>").append("<![CDATA[" + supplierType +"]]>").append("</supp_type>");
				valueXmlString.append("<supp_name>").append("<![CDATA[" + supplierName +"]]>").append("</supp_name>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("channel_partner"))
			{
				channelPartner = checkNull(genericUtility.getColumnValue("channel_partner", dom));
				if(channelPartner != null && channelPartner.equalsIgnoreCase("N"))
				{
					valueXmlString.append("<site_code__ch>").append("<![CDATA[" + lsNull  + "]]>").append("</site_code__ch>");
					valueXmlString.append("<site_code__ch protect = \"1\">").append("<![CDATA[" +  "" + "]]>").append ("</site_code__ch>");

					valueXmlString.append("<dis_link>").append("<![CDATA[" +  lsNull + "]]>").append("</dis_link>");
					valueXmlString.append("<dis_link protect = \"1\">").append("<![CDATA[" +  "" + "]]>").append ("</dis_link>");

				}
				else
				{
					valueXmlString.append("<site_code__ch protect = \"0\">").append("<![CDATA[" +  "" + "]]>").append ("</site_code__ch>");

					valueXmlString.append("<dis_link>").append("<![CDATA[" +  lsNull + "]]>").append("</dis_link>");
					valueXmlString.append("<dis_link protect = \"0\">").append("<![CDATA[" +  "" + "]]>").append ("</dis_link>");

				}

			}
			valueXmlString.append("</Detail1>");
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
}	
