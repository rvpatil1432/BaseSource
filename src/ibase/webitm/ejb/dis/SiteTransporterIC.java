

/********************************************************
	Title : SiteTransporterIC
	Date  : 18/03/13
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

public class SiteTransporterIC extends ValidatorEJB implements SiteTransporterICLocal, SiteTransporterICRemote
{
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
			System.out.println("Exception : [SiteTransporterIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String siteCode = "",tranCode = "",siteCodePay = "", currCode = "" ,crTerm = "", taxClass = "", taxEnv = "" ;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int count = 0;
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
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("site_code"))
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if( siteCode.trim().length() == 0)
						{
							errCode = "VMSITECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(siteCode.trim().length() > 0)
						{
							if(!(isExist(conn, "site", "site_code" ,siteCode)))
							{
								errCode = "VMSITECDX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						if( tranCode.trim().length() == 0)
						{
							errCode = "VMTRANCD21";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(tranCode.trim().length() > 0)
						{
							if(!(isExist(conn, "transporter", "tran_code" ,tranCode)))
							{
								errCode = "VMTRANCD22";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("site_code__pay"))
					{
						siteCodePay = checkNull(genericUtility.getColumnValue("site_code__pay", dom));
						
						if(siteCodePay.trim().length() > 0)
						{
							if(!(isExist(conn, "site", "site_code" ,siteCodePay)))
							{
								errCode = "VMSITECDX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						
						if(currCode.trim().length() > 0)
						{
							if(!(isExist(conn, "currency", "curr_code" ,currCode)))
							{
								errCode = "VMCURCD21";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("cr_term"))
					{
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						
						if(crTerm.trim().length() > 0)
						{
							if(!(isExist(conn, "crterm", "cr_term" ,crTerm)))
							{
								errCode = "VMCRTER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
						
						if(taxClass.trim().length() > 0)
						{
							if(!(isExist(conn, "taxclass", "tax_class" ,taxClass)))
							{
								errCode = "VMTACLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("tax_env"))
					{
						taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						
						if(taxEnv.trim().length() > 0)
						{
							if(!(isExist(conn, "taxenv", "tax_env" ,taxEnv)))
							{
								errCode = "VMTAENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());   
							}
						}

					}
					else if(editFlag != null &&  "A".equalsIgnoreCase(editFlag.trim()))
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						
						sql = " select count(*) from site_transporter where site_code = ? and tran_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,tranCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count > 0) 
						{
							errCode = "VMSITRSVL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
					
					}

				}
				break;


			}
			int errListSize = errList.size();
			count = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(count = 0; count < errListSize; count ++)
				{
					errCode = errList.get(count);
					errFldName = errFields.get(count);
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
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............."+xmlString);
		System.out.println("xmlString1............"+xmlString);
		System.out.println("xmlString2............"+xmlString);
		try
		{   
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
			System.out.println("Exception : [SiteTransporterIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String descr = "",siteCode = "",siteCodePay = "",tranCode = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
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
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

				
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					siteCode  = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
					descr = "";
					descr = findValue(conn, "descr", "site", "site_code",siteCode );
					valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode +"]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + descr +"]]>").append("</site_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode  = checkNull(genericUtility.getColumnValue("site_code", dom));
					descr = "";
					descr = findValue(conn, "descr", "site", "site_code",siteCode );
					valueXmlString.append("<site_descr>").append("<![CDATA[" + descr +"]]>").append("</site_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code__pay"))
				{
					siteCodePay  = checkNull(genericUtility.getColumnValue("site_code__pay", dom));
					descr = "";
					descr = findValue(conn, "descr", "site", "site_code",siteCodePay );
					valueXmlString.append("<site_descr_1>").append("<![CDATA[" + descr +"]]>").append("</site_descr_1>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode  = checkNull(genericUtility.getColumnValue("tran_code", dom));
					descr = "";
					descr = findValue(conn, "tran_name", "transporter", "tran_code",tranCode );
					valueXmlString.append("<tran_name>").append("<![CDATA[" + descr +"]]>").append("</tran_name>");
				}
				
				//Changed by Dadaso pawar on 02/04/15 [Start][W14LSUN006]
				if(currentColumn.trim().equalsIgnoreCase("prono_from"))
				{
					String proNoFrom = "",tranCodeL = "";
					tranCodeL = checkNull(genericUtility.getColumnValue("tran_code", dom));
					proNoFrom = checkNull(genericUtility.getColumnValue("prono_from", dom));
					siteCode  = checkNull(genericUtility.getColumnValue("site_code", dom));
					proNoFrom = proNoFrom == null ? "" : proNoFrom.trim();
					tranCodeL = tranCodeL == null ? "" : tranCodeL.trim();
					
					System.out.println(" item change proNoFrom@@ : ["+proNoFrom+"]");
					System.out.println("tranCodeL@@ : ["+tranCodeL+"]");
					
					if(isClearProNoLast(tranCodeL,proNoFrom,siteCode,conn))
					{
						valueXmlString.append( "<prono_last><![CDATA[]]></prono_last>\r\n" );
						valueXmlString.append( "<prono_to><![CDATA[]]></prono_to>\r\n" );//Changed by Dadaso pawar on 22/04/15 [D15ASUN005] [For make blank prono_to]	
					}		
					
				}
				if(currentColumn.trim().equalsIgnoreCase("prono_to"))
				{
					String proNoTo = "",tranCodeL = "";
					tranCodeL = genericUtility.getColumnValue("tran_code", dom);
					proNoTo = genericUtility.getColumnValue("prono_to", dom);
					siteCode  = checkNull(genericUtility.getColumnValue("site_code", dom));
					proNoTo = proNoTo == null ? "" : proNoTo.trim();
					tranCodeL = tranCodeL == null ? "" : tranCodeL.trim();
					
					System.out.println(" item change proNoTo@@ : ["+proNoTo+"]");
					System.out.println("tranCodeL@@ : ["+tranCodeL+"]");
					
					if(isClearProNoLast(tranCodeL,proNoTo,siteCode,conn))
					{
						valueXmlString.append( "<prono_last><![CDATA[]]></prono_last>\r\n" );
					}
				}
				//Changed by Dadaso pawar on 02/04/15 [End] [W14LSUN006]
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
				throw new ITMException(e);
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
	private boolean isExist(Connection conn, String tableName, String columnName, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		boolean status = false;
		try
		{			
			sql = "SELECT count(*) from " + tableName + " where " + columnName +"  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();

			if(rs.next())
			{					
				if(rs.getBoolean(1))
				{					
					status = true;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in isExist ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from isExist ");
		return status;
	}
	private String findValue(Connection conn, String columnName ,String tableName, String columnName2, String value) throws  ITMException, RemoteException
	{

		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		String findValue = "";

		try
		{			
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"  = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();

			if(rs.next())
			{					
				findValue = rs.getString(columnName);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from findValue ");
		return findValue;
	}
	//Added by Dadadso pawar on 05/14/15 [Start]
	private boolean isClearProNoLast(String tranCode,String proNoFrom ,String siteCode,Connection conn) throws ITMException,Exception
	{
		ResultSet rs1 = null;
		PreparedStatement pstmt1 = null;
		String sql1 = "",proNoFromD = "";
		try
		{
			sql1 = "SELECT PRONO_FROM,PRONO_TO,PRONO_LAST FROM SITE_TRANSPORTER "
					+ "WHERE TRAN_CODE = ? AND SITE_CODE = ?";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, tranCode);
			pstmt1.setString(2, siteCode);
			rs1 = pstmt1.executeQuery();
			if(rs1.next())
			{
				proNoFromD = rs1.getString("PRONO_FROM") == null ? "" : rs1.getString("PRONO_FROM").trim();					
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			System.out.println("isClearProNoLast proNoFromD :["+proNoFromD+"] proNoFrom : ["+proNoFrom+"]");			
			if(proNoFromD.equalsIgnoreCase(proNoFrom))
			{
				return false;
			}
			else
			{
				return true;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			if(rs1 != null)
			{
				rs1.close();
				rs1 = null;
			}
			if(pstmt1 != null)
			{
				pstmt1.close();
				pstmt1 = null;
			}
		}
	}
	//Added by Dadadso pawar on 05/14/15 [End]
	private String errorType(Connection conn , String errorCode) throws ITMException
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
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}		
		return msgType;
	}
}	
