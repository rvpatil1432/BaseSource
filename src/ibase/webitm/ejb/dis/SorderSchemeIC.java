/*
 * Author:Wasim Ansari
 * Date:30-08-2016
 * Request ID:S16EBAS006 (Expense Wallet Integration Mobile Wizard)
 */

package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class SorderSchemeIC extends ValidatorEJB implements SorderSchemeICLocal, SorderSchemeICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
    /**
     * Default constructor. 
     */
    public SorderSchemeIC() {
        // TODO Auto-generated constructor stub
    }
    
	@Override
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		
		try
		{
			System.out.println("Inside wfvalData SorderSchemeIC");
			System.out.println("@@xmlString : ["+ xmlString+ "] \nxmlString1 : ["+ xmlString1 +"] \nxmlString2 : ["+ xmlString2 +"]");
			
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
			
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Cpp : wfValData(String xmlString) : ==>\n"+e.getMessage());
		}
		
		return(errString);
	}
	
	
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String columnValue = "";
		String userId = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String sql = "";
		int ctr=0;
		int childNodeListLength;
		int currentFormNo = 0;
		long cnt = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		boolean flag = false; 
		
		int count = 0;
		
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			switch(currentFormNo)
			{
				case 1:
				{
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						if( childNode.getNodeType() != Node.ELEMENT_NODE )
						{
							continue;
						}
						childNodeName = childNode.getNodeName();
						if (childNode != null && childNode.getFirstChild() != null )
						{
							columnValue = childNode.getFirstChild().getNodeValue();
						}
						else
						{
							columnValue = "";
						}
						
						System.out.println("columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
						
					    if (childNodeName.equalsIgnoreCase("site_code")) 
						{
					    	count = 0;
					    	
							if(columnValue == null || columnValue.length() == 0)
							{
								errList.add( "BLKSITECD" );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sql = " SELECT SITE_CODE FROM SITE WHERE SITE_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									count++;
								}
								if ( rs!= null )
								{
									rs.close();rs = null;
								}
								if( pstmt != null )
								{
									pstmt.close();pstmt = null;
								}
								
								if( count == 0 )
								{
									errList.add("VTSITECD1");
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					    else if (childNodeName.equalsIgnoreCase("cust_code__from")) 
						{
					    	count = 0;
					    	
							if(columnValue == null || columnValue.length() == 0)
							{
								errList.add( "PRNULCUSFR" );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sql = " SELECT CUST_CODE FROM CUSTOMER WHERE CUST_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									count++;
								}
								if ( rs!= null )
								{
									rs.close();rs = null;
								}
								if( pstmt != null )
								{
									pstmt.close();pstmt = null;
								}
								
								if( count == 0 )
								{
									errList.add("PRINVCUSFR");
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					    else if (childNodeName.equalsIgnoreCase("cust_code__to")) 
						{
					    	count = 0;
					    	
							if(columnValue == null || columnValue.length() == 0)
							{
								errList.add( "PRNULCUSTO" );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sql = " SELECT CUST_CODE FROM CUSTOMER WHERE CUST_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									count++;
								}
								if ( rs!= null )
								{
									rs.close();rs = null;
								}
								if( pstmt != null )
								{
									pstmt.close();pstmt = null;
								}
								
								if( count == 0 )
								{
									errList.add("PRINVCUSTO");
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					}
				}//End case1
				break;
			}//End Switch
			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
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
		}// End of try
		catch(Exception e)
		{
			System.out.println("Inside Catch SorderSchemeIC wfValData Exception="+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//connDriver = null;
				if(conn != null)
				{
					if(rs != null) 
					{
						rs.close();rs = null;
					}
					if(pstmt != null) 
					{
						pstmt.close();pstmt = null;
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
	
	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		Document dom1 = null;
		Document dom = null;
		Document dom2 = null;
		String valueXmlString = "";
		
		try
		{
			System.out.println("Inside item change SorderSchemeIC");
			System.out.println("xmlString : ["+ xmlString+ "] \nxmlString1 : ["+ xmlString1 +"] \nxmlString2 : ["+ xmlString2 +"]");
			
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
			
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			valueXmlString = genericUtility.createErrorString(e);
		}
		
		return (valueXmlString);
	}
	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "",siteCodeDescr = "",loginSiteCode = "";
		int currentFormNo = 0;
		
		StringBuffer valueXmlString = new StringBuffer();
		//String currDate = null;
		try
		{
			conn = getConnection();
			
			System.out.println("Xtra Params="+xtraParams);
			
			/*userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );*/
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
			
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?>\r\n<Root>\r\n<header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</header>\r\n");
			
			System.out.println("currentColumn["+currentColumn+"] && currentFormNo["+currentFormNo+"]");
			
			switch ( currentFormNo )
			{
				case 1:
				{   
					valueXmlString.append("<Detail1 domID='1' selected = 'N'>\r\n");
					
					if( currentColumn.trim().equalsIgnoreCase( "itm_default" ))
					{
						valueXmlString.append( "<site_code><![CDATA[" ).append( loginSiteCode ).append( "]]></site_code>\r\n" );
						
						sql = " SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, loginSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCodeDescr = checkNullAndTrim((rs.getString("DESCR")));
						}
						if ( rs!= null )
						{
							rs.close();rs = null;
						}
						if( pstmt != null )
						{
							pstmt.close();pstmt = null;
						}
						
						valueXmlString.append( "<site_code__descr><![CDATA[" ).append( siteCodeDescr ).append( "]]></site_code__descr>\r\n" );
					} 
					valueXmlString.append("</Detail1>\r\n");
				}
				break;
			}
		}
		catch(Exception e)
		{
			System.out.println("Inside SorderSchemeIC ItemChange Exception="+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}	
				if(pstmt != null)
				{	
					pstmt.close();pstmt = null;
				}
				if(conn != null)
				{	
					conn.close();conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}			
		}

		valueXmlString.append("</Root>");
		System.out.println("valueXmlString ::"+valueXmlString.toString());

		return valueXmlString.toString();
	}
	
	/**
	 * select error description from MESSAGES
	 * @param conn
	 * @param errorCode
	 * @return
	 */
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
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
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}
}
