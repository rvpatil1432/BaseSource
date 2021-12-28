package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SFrcastSitesModCpy extends ValidatorEJB implements SFrcastSitesModCpyLocal, SFrcastSitesModCpyRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	/* public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("SFrcastSitesModCpyEJB is in Process..........");
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	} */
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
		System.out.println("Validation Start..........");
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
			System.out.println("Exception : SFrcastSitesModCpyEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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
		String columnValue = null;
		String childNodeName = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String errCode = null;
		String userId = null,loginSite = null;
		int cnt = 0;
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		ConnDriver connDriver = new ConnDriver();
		
    	try
		{
			System.out.println( "wfValData called" );
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			String option = genericUtility.getColumnValue( "option", dom );
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
					System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if ( childNodeName.equalsIgnoreCase( "option" ) )
						{
							if ( childNode.getFirstChild() == null )
							{
								errString =itmDBAccessEJB.getErrorString("option","VMINVOPTN",userId,"",conn);
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "prd_from" ) )
						{
							if ( childNode.getFirstChild() == null )
							{
								errString =itmDBAccessEJB.getErrorString("prd_from","VMINVPDCDF",userId,"",conn);
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "prd_to" ) )
						{
							if( option != null && option.equals("C") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("prd_to","VMINVPDCDT",userId,"",conn);
									break;
								}
							}
						}
						
						if ( childNodeName.equalsIgnoreCase( "new_period_plan" ) )
						{
							if ( childNode.getFirstChild() == null )
							{
								errString =itmDBAccessEJB.getErrorString("new_period_plan","VMINVPDCDP",userId,"",conn);
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "new_prd_for" ) )
						{
							if( option != null && option.equals("C") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("new_prd_for","VMINVPDCDF",userId,"",conn);
									break;
								}
							}
						}
						if ( childNodeName.equalsIgnoreCase( "item_ser" ) )
						{
							if ( childNode.getFirstChild() == null )
							{
								errString =itmDBAccessEJB.getErrorString("item_ser","VMINVITMSR",userId,"",conn);
								break;
							}
							else
							{
								String itemSeries = genericUtility.getColumnValue( "item_ser", dom );
								if( itemSeries != null && itemSeries.trim().length() > 0 )
								{
									sql = "SELECT count(1) from itemser "
										 +" WHERE item_ser = ?";
									
									pstmt = conn.prepareStatement( sql );
									pstmt.setString( 1,itemSeries.trim() );
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt( 1 );
									}
									if(cnt == 0)
									{
										errString =itmDBAccessEJB.getErrorString("item_ser","VMINVITEMS",userId,"",conn);
										break ;
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
						}
						if ( childNodeName.equalsIgnoreCase( "item_to" ) )
						{
							if( option != null && option.equals("M") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("item_to","VMINVIMCDT",userId,"",conn);
									break;
								}
								else
								{
									String itemCode = genericUtility.getColumnValue( "item_to", dom );
									if( itemCode != null && itemCode.trim().length() > 0 )
									{
										sql = "SELECT count(1) from item "
											 +" WHERE item_code = ?";
										
										pstmt = conn.prepareStatement( sql );
										pstmt.setString( 1,itemCode.trim() );
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt( 1 );
										}
										if(cnt == 0)
										{
											errString =itmDBAccessEJB.getErrorString("item_to","VMITMCDMST",userId,"",conn);
											break ;
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}
						}
						if ( childNodeName.equalsIgnoreCase( "item_from" ) )
						{
							if( option != null && option.equals("M") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("item_from","VMINVITMCD",userId,"",conn);
									break;
								}
								else
								{
									String itemCode = genericUtility.getColumnValue( "item_from", dom );
									if( itemCode != null && itemCode.trim().length() > 0 )
									{
										sql = "SELECT count(1) from item "
											 +" WHERE item_code = ?";
										
										pstmt = conn.prepareStatement( sql );
										pstmt.setString( 1,itemCode.trim() );
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt( 1 );
										}
										if(cnt == 0)
										{
											errString =itmDBAccessEJB.getErrorString("item_from","VMITMCDMST",userId,"",conn);
											break ;
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}
						}
						
						if ( childNodeName.equalsIgnoreCase( "site_code_from" ) )
						{
							if( option != null && option.equals("M") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("site_code_from","VMINVSTCD",userId,"",conn);
									break;
								}
								else
								{
									String siteCode = genericUtility.getColumnValue( "site_code_from", dom );
									if( siteCode != null && siteCode.trim().length() > 0 )
									{
										sql = "SELECT count(1) from site "
											 +" WHERE site_code = ?";
										
										pstmt = conn.prepareStatement( sql );
										pstmt.setString( 1,siteCode.trim() );
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt( 1 );
										}
										if(cnt == 0)
										{
											errString =itmDBAccessEJB.getErrorString("site_code","VMSTCDNMST",userId,"",conn);
											break ;
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}		
						}
						if ( childNodeName.equalsIgnoreCase( "site_code_to" ) )
						{
							if( option != null && option.equals("M") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("site_code_to","VMINVSTCD",userId,"",conn);
									break;
								}
								else
								{
									String siteCode = genericUtility.getColumnValue( "site_code_to", dom );
									if( siteCode != null && siteCode.trim().length() > 0 )
									{
										sql = "SELECT count(1) from site "
											 +" WHERE site_code = ?";
										
										pstmt = conn.prepareStatement( sql );
										pstmt.setString( 1,siteCode.trim() );
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt( 1 );
										}
										if(cnt == 0)
										{
											errString =itmDBAccessEJB.getErrorString("site_to","VMSTCDNMST",userId,"",conn);
											break ;
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}		
						}						
						if ( childNodeName.equalsIgnoreCase( "chg_by" ) )
						{
							if ( childNode.getFirstChild() == null )
							{
								errString =itmDBAccessEJB.getErrorString("chg_by","VMINVCHGBY",userId,"",conn);
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "chg_by_ddl" ) )
						{
							if( option != null && option.equals("M") )
							{
								if ( childNode.getFirstChild() == null )
								{
									errString =itmDBAccessEJB.getErrorString("chg_by_ddl","VMINVCHGBD",userId,"",conn);
									break;
								}
							}
						}
					} 
					//END OF CASE1
					break;
			}//END SWITCH
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::" +e);
			e.printStackTrace();
			errCode = "VALEXCEP";
			errString = getErrorString( "", errCode, userId );	
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}
					
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			}
			catch(Exception d)
				{
				  d.printStackTrace();
				}			
			System.out.println(" < SFrcastSitesModCpyEJB > CONNECTION IS CLOSED");
		}
		System.out.println("ErrString ::" + errString);

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
			System.out.println("xmlString" + xmlString);
			dom = parseString(xmlString); 
			System.out.println("xmlString1" + xmlString1);
			dom1 = parseString(xmlString1); 

			if (xmlString2.trim().length() > 0 )
			{
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SFrcastSitesModCpyEJB][itemChanged] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
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
		String  chguserhdr = null;
		String  chgtermhdr = null;
		String siteCode = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			chguserhdr = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			System.out.println("Current Form No ["+currentFormNo+"]");							
			switch (currentFormNo)
			{
				case 1:
					valueXmlString.append("<Detail1>");	
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
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
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					
					if(currentColumn.trim().equals( "option" ))
					{
						String option = genericUtility.getColumnValue( "option", dom );
						if( option != null && option.equals("M") )	
						{
							valueXmlString.append("<item_to protect=\"0\" >").append("").append("</item_to>");
							valueXmlString.append("<item_from protect=\"0\" >").append("").append("</item_from>");
							valueXmlString.append("<site_code_from protect=\"0\" >").append("").append("</site_code_from>");
							valueXmlString.append("<site_code_to protect=\"0\" >").append("").append("</site_code_to>");
							valueXmlString.append("<new_period_plan protect=\"0\" >").append("").append("</new_period_plan>");
							valueXmlString.append("<new_prd_for protect=\"1\" >").append("").append("</new_prd_for>");
							valueXmlString.append("<chg_by_ddl protect=\"0\" >").append("").append("</chg_by_ddl>");
							
							valueXmlString.append("<item_from_descr>").append("").append("</item_from_descr>");
							valueXmlString.append("<item_to_descr>").append("").append("</item_to_descr>");
							valueXmlString.append("<site_frm_descr>").append("").append("</site_frm_descr>");
							valueXmlString.append("<site_to_descr>").append("").append("</site_to_descr>");
							valueXmlString.append("<new_period_plan>").append("").append("</new_period_plan>");
							valueXmlString.append("<new_prd_for>").append("").append("</new_prd_for>");
							valueXmlString.append("<prd_from>").append("").append("</prd_from>");
							valueXmlString.append("<prd_to protect=\"1\" >").append("").append("</prd_to>");
							valueXmlString.append("<item_ser>").append("").append("</item_ser>");
							valueXmlString.append("<item_ser_descr>").append("").append("</item_ser_descr>");
							valueXmlString.append("<chg_by>").append("").append("</chg_by>");
						}
						else if( option != null && option.equals("C") )	
						{
							valueXmlString.append("<item_to protect=\"1\" >").append("").append("</item_to>");
							valueXmlString.append("<item_from protect=\"1\" >").append("").append("</item_from>");
							valueXmlString.append("<site_code_from protect=\"1\" >").append("").append("</site_code_from>");
							valueXmlString.append("<site_code_to protect=\"1\" >").append("").append("</site_code_to>");
							valueXmlString.append("<new_period_plan protect=\"0\" >").append("").append("</new_period_plan>");
							valueXmlString.append("<new_prd_for protect=\"0\" >").append("").append("</new_prd_for>");
							valueXmlString.append("<chg_by_ddl protect=\"1\" >").append("P").append("</chg_by_ddl>");
							
							valueXmlString.append("<item_from_descr>").append("").append("</item_from_descr>");
							valueXmlString.append("<item_to_descr>").append("").append("</item_to_descr>");
							valueXmlString.append("<site_frm_descr>").append("").append("</site_frm_descr>");
							valueXmlString.append("<site_to_descr>").append("").append("</site_to_descr>");
							valueXmlString.append("<prd_from>").append("").append("</prd_from>");
							valueXmlString.append("<prd_to protect=\"0\" >").append("").append("</prd_to>");
							valueXmlString.append("<item_ser>").append("").append("</item_ser>");
							valueXmlString.append("<item_ser_descr>").append("").append("</item_ser_descr>");
							valueXmlString.append("<chg_by>").append("").append("</chg_by>");
						}
						else
						{
							valueXmlString.append("<item_to protect=\"0\" >").append("").append("</item_to>");
							valueXmlString.append("<item_from protect=\"0\" >").append("").append("</item_from>");
							
							valueXmlString.append("<item_from_descr>").append("").append("</item_from_descr>");
							valueXmlString.append("<item_to_descr>").append("").append("</item_to_descr>");
							valueXmlString.append("<prd_from>").append("").append("</prd_from>");
							valueXmlString.append("<prd_to>").append("").append("</prd_to>");
							valueXmlString.append("<new_period_plan>").append("").append("</new_period_plan>");
							valueXmlString.append("<new_prd_for>").append("").append("</new_prd_for>");
							valueXmlString.append("<item_ser>").append("").append("</item_ser>");
							valueXmlString.append("<item_ser_descr>").append("").append("</item_ser_descr>");
							valueXmlString.append("<site_frm_descr>").append("").append("</site_frm_descr>");
							valueXmlString.append("<site_to_descr>").append("").append("</site_to_descr>");
							valueXmlString.append("<chg_by>").append("").append("</chg_by>");
							valueXmlString.append("<chg_by_ddl>").append("").append("</chg_by_ddl>");
						}
					}
					if(currentColumn.trim().equals( "item_ser" ))
					{
						String itemSeries = genericUtility.getColumnValue( "item_ser", dom );
						String descr = "";
						if( itemSeries != null && itemSeries.trim().length() > 0 )
						{
							sql = "SELECT descr FROM itemser "
								 +" WHERE item_ser = ? ";
							
							pStmt = conn.prepareStatement( sql );
							pStmt.setString( 1,itemSeries.trim() );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								descr = rs.getString( "descr" );
							}	
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<item_ser_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</item_ser_descr>");
					}
					if(currentColumn.trim().equals( "item_to" ))
					{
						String itemCode = genericUtility.getColumnValue( "item_to", dom );
						String descr = "";
						if( itemCode != null && itemCode.trim().length() > 0 )
						{
							sql = "SELECT descr FROM item "
								 +" WHERE item_code = ? ";
							
							pStmt = conn.prepareStatement( sql );
							pStmt.setString( 1,itemCode.trim() );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								descr = rs.getString( "descr" );
							}	
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<item_to_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</item_to_descr>");
					}
					if(currentColumn.trim().equals( "item_from" ))
					{
						String itemCode = genericUtility.getColumnValue( "item_from", dom );
						String descr = "";
						if( itemCode != null && itemCode.trim().length() > 0 )
						{
							sql = "SELECT descr FROM item "
								 +" WHERE item_code = ? ";
							
							pStmt = conn.prepareStatement( sql );
							pStmt.setString( 1,itemCode.trim() );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								descr = rs.getString( "descr" );
							}	
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<item_from_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</item_from_descr>");
					}					
					if (currentColumn.trim().equals( "site_code_from" ))
					{
						siteCode = genericUtility.getColumnValue( "site_code_from", dom );
						String descr = "";
						if( siteCode != null && siteCode.trim().length() > 0 )
						{
							sql = "SELECT descr FROM site "
								 +" WHERE site_code = ? ";
							
							pStmt = conn.prepareStatement( sql );
							pStmt.setString( 1,siteCode.trim() );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								descr = rs.getString( "descr" );
							}	
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<site_frm_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</site_frm_descr>");
					}
					if (currentColumn.trim().equals( "site_code_to" ))
					{
						siteCode = genericUtility.getColumnValue( "site_code_to", dom );
						String descr = "";
						if( siteCode != null && siteCode.trim().length() > 0 )
						{
							sql = "SELECT descr FROM site "
								 +" WHERE site_code = ? ";
							
							pStmt = conn.prepareStatement( sql );
							pStmt.setString( 1,siteCode.trim() );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								descr = rs.getString( "descr" );
							}	
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<site_to_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</site_to_descr>");
					}
					valueXmlString.append("</Detail1>");
					valueXmlString.append("</Root>");	
					break;
			}//END OF SWITCH
		}//END OF TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pStmt != null )
				{
					pStmt.close();
					pStmt = null;					
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}//END OF ITEMCHANGE	
}
