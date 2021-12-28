package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import javax.ejb.SessionBean;// commented for ejb3
import javax.ejb.CreateException;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SaleTermTableIC extends ValidatorEJB implements SaleTermTableICLocal , SaleTermTableICRemote //SessionBean // commented for ejb3
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	/*public void ejbCreate() throws RemoteException, CreateException 
	{
	}
	public void ejbRemove()
	{
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
		String  errString = null;
		
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String columnValue = null;
		String childNodeName = null;
		String errCode = null;
		String userId = null,loginSite = null;
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		ConnDriver connDriver = new ConnDriver();
    	try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//genericUtility = GenericUtility.getInstance(); 
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
					for( ctr = 0; ctr < childNodeListLength; ctr++ )
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if ( childNodeName.equalsIgnoreCase ( "term_code" ) )
						{
							String termCode = null;
							termCode = genericUtility.getColumnValue( "term_code", dom );
												
							if(	termCode == null || termCode.trim().length() == 0 )
							{
								errCode = "TCODEBLK";
								errString = getErrorString( "term_code", errCode, userId );
							}
							if(	termCode != null )
							{
								sql = " SELECT COUNT(*) FROM sale_term WHERE term_code = ? ";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,termCode.trim());
								rs = pstmt.executeQuery();
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								if( cnt == 0 )
								{
									errCode = "INTERMCODE";
									errString = getErrorString( "term_code", errCode, userId );
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
							}
						}
						if ( childNodeName.equalsIgnoreCase ( "term_table" ) )
						{
							String termTable = null;
							String termCode = null;
							termTable = genericUtility.getColumnValue( "term_table", dom );
							termCode = genericUtility.getColumnValue( "term_code", dom );
							
							if(	termTable == null || termTable.trim().length() == 0 )
							{
								errCode = "TERMTABBLK";
								errString = getErrorString( "term_table", errCode, userId );
							}
							if( editFlag.equals( "A" ) )
							{
								if( termCode != null && termTable != null )
								{
									sql = "select count(*)  from sale_term_table where term_code = ? and term_table = ? ";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,termCode.trim());
									pstmt.setString(2,termTable.trim());
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );
									}
									if( cnt > 0 )
									{
										errCode = "ALTERMCODE";
										errString = getErrorString( "term_code", errCode, userId );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						} 
					} //END OF CASE1
					break;
			}//END SWITCH 
		}//END TRY
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pstmt != null )
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
				throw new ITMException ( d );
			}			
		}
		return errString;
	}//END OF VALIDATION 

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try
		{
			dom = parseString(xmlString); 
			System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement vPStmt = null;
		ResultSet vRs = null;
		String vSql = null;
		int currentFormNo = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = null;
		NodeList parentNodeList = null;
		Node parentNode = null; 
		Node childNode = null;
		NodeList childNodeList = null;
		String childNodeName = null;
		int childNodeListLength = 0;
		int ctr = 0;
		
		String  loginSite = null; 
		
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			switch (currentFormNo)
			{
				case 1:
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
					valueXmlString.append("<Detail1>");	
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					childNodeListLength = childNodeList.getLength();
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
					if (currentColumn.trim().equals( "itm_default" ))
					{	
						String termTable = null;
						String mandatory = null;
						termTable = genericUtility.getColumnValue( "term_table", dom1 );
						valueXmlString.append("<term_table protect =\"1\">").append("<![CDATA[" + ( termTable != null ? termTable.trim() : ""  )+ "]]>").append("</term_table>");
						valueXmlString.append("<mandatory>").append( "Y" ).append("</mandatory>");
					}
					if (currentColumn.trim().equals( "term_code" ))
					{
						String termDescr = null;
						String mandatory = null;
						String termCode = null;
						termCode = genericUtility.getColumnValue( "term_code", dom1 );
						if( termCode != null && termCode.trim().length() > 0 )
						{
							vSql = "select descr,mandatory from sale_term where term_code = ? ";
							vPStmt = conn.prepareStatement( vSql );
							vPStmt.setString(1,termCode.trim());
							vRs = vPStmt.executeQuery();
							if( vRs.next() )
							{
								termDescr = vRs.getString( "descr" );
								mandatory = vRs.getString( "mandatory" );
							}
							vRs.close();
							vRs = null;
							vPStmt.close();
							vPStmt = null;
							
							valueXmlString.append("<descr>").append("<![CDATA[" + ( termDescr != null ? termDescr.trim() : ""  )+ "]]>").append("</descr>");
							valueXmlString.append("<mandatory>").append("<![CDATA[" + ( mandatory != null ? mandatory.trim() : ""  )+ "]]>").append("</mandatory>");
						}
					}
					
					valueXmlString.append("</Detail1>");					
					valueXmlString.append("</Root>");
					break;
					
			}//END OF SWITCH
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		finally
		{
			try
			{
				if ( vRs != null )
				{
					vRs.close();
					vRs = null;
				}
				if ( vPStmt != null )
				{
					vPStmt.close();
					vPStmt = null;					
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException ( e );
			}			
		}
		return valueXmlString.toString();
	}//END OF ITEMCHANGE
}
