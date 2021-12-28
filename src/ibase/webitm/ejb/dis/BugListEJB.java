 /**
 * Author : Wasim Ansari
 * Date   : 24-06-2016
 * Description : Bug Fix Screen [W16CBAS005]
 * */
 
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import ibase.webitm.ejb.*; 
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.utility.E12GenericUtility;

@javax.ejb.Stateless
public class BugListEJB extends ValidatorEJB implements BugListEJBLocal, BugListEJBRemote
{
	/**
	* The method defined with no paramter and returns nothing
	*/
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}
	/**
	 * The public method is used for validation of required fields which inturn called overloded method
	 * Returns validation string in XML format if exist otherwise returns null 
	 * @param xmlString contains the current form data in XML format
	 * @param xmlString1 contains always header form data in XML format
	 * @param xmlString2 contains all forms data in XML format 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginCode etc
	 */
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			System.out.println("xmlString [" + xmlString + "]");
			System.out.println("xmlString1 [" + xmlString1 + "]");
			System.out.println("xmlString2 [" + xmlString2 + "]");
			if( xmlString != null && xmlString.trim().length()!=0 )
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if( xmlString1 != null && xmlString1.trim().length()!=0 )
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if( xmlString2 != null && xmlString2.trim().length()!=0 )
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams );
			System.out.println ( "ErrString: " + errString);
		}
		catch(Exception e)
		{
			System.out.println ( "Exception: AsnLotStatIC: wfValData(String xmlString): " + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		System.out.println ( "Returning from AsnLotStatIC wfValData" );
		return ( errString ); 
	}
	/**
	 * The public overloded method is used for validation of required fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param dom contains the current form data 
	 * @param dom1 contains always header form data
	 * @param dom2 contains all forms data 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contais additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String columnValue = "";
		String errString = "";
		String lotNo = "";
		String itemCode = "";
	
		
		int currentFormNo = 0;
		int count = 0;
		
		
		String columnName = "";
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pStmt = null;
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer();
		
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String errorType = "", errCode = "";
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();

		try
		{
			SimpleDateFormat sdft = new SimpleDateFormat( genericUtility.getApplDateFormat() );
			//ConnDriver connDriver = null;
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();

			NodeList parentList = null;
			NodeList childList = null;
			Node parentNode = null;
			Node childNode = null;
			int noOfChilds = 0;
			
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			if ( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			switch( currentFormNo )
			{
				case 1 :
				{	
					parentList = dom.getElementsByTagName( "Detail" + currentFormNo );
					
					for ( int pctr = 0; pctr < parentList.getLength(); pctr++ )
					{
						parentNode = parentList.item( pctr);
						childList = parentNode.getChildNodes();
						noOfChilds = childList.getLength();	
						for ( int ctr = 0; ctr < childList.getLength(); ctr++ )
						{	
							childNode = childList.item(ctr);
							if( childNode.getNodeType() != Node.ELEMENT_NODE )
							{
								continue;
							}
							String childNodeName = childNode.getNodeName();
							if ( childNode != null && childNode.getFirstChild() != null )
							{
								columnValue = childNode.getFirstChild().getNodeValue();
							}
							columnName = childNode.getNodeName();
							
							if( "obj_name".equalsIgnoreCase(columnName) )
							{
								String objName = checkNull((genericUtility.getColumnValue("obj_name",dom)));
								
								if(objName == null || objName.length() ==0)
								{
									errList.add( "UVFDREQ" );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									sql = "select count(*) from transetup where tran_window =?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, "w_"+columnValue.trim());
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									if( count == 0 )
									{
										errList.add( "VTBLNKOBJ" );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
							else if( "solu_desired".equalsIgnoreCase(columnName) )
							{
								String soluDesired = checkNull((genericUtility.getColumnValue("solu_desired",dom)));
								
								if(soluDesired == null || soluDesired.length() ==0)
								{
									errList.add( "VTSOLDES" );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							else if( "resolved_ver".equalsIgnoreCase(columnName) )
							{
								String resolvedVer = checkNull((genericUtility.getColumnValue("resolved_ver",dom)));
								
								if(resolvedVer == null || resolvedVer.length() ==0)
								{
									errList.add( "VTRESVER" );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							else if( "trans_affected".equalsIgnoreCase(columnName) )
							{
								String transAffected = checkNull((genericUtility.getColumnValue("trans_affected",dom)));
								
								if(transAffected == null || transAffected.length() ==0)
								{
									errList.add( "VTAFFTRAN" );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							else if( "comp_delivered".equalsIgnoreCase(columnName) )
							{
								String compDelivered = checkNull((genericUtility.getColumnValue("comp_delivered",dom)));
								
								if(compDelivered == null || compDelivered.length() ==0)
								{
									errList.add( "VTCOMPDEL" );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
						}
					}
				}//end of case 1
				break;
			}//end of switch
			int errListSize = errList.size();
			int cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					errString = getErrorString( errFldName, errCode, userId );
					errorType =  errorType( conn , errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString =bifurErrString;//+"<trace>"+errMsg+"</trace>";
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				
				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}
			errString = errStringXml.toString();
		}//end of try block
		catch( Exception e )
		{
			throw new ITMException(e);
		}//end of catch block
		finally
		{
			try
			{	
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pStmt != null && !pStmt.isClosed() )
				{
					pStmt.close();
					pStmt = null;
				}
				if( conn != null && !conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch( Exception e )
			{
				System.out.println( "Exception :Bug Fix:wfValData :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}//end of finally block
		return ( errString );
	}//
	
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			if ( xmlString != null && xmlString.trim().length()!=0 )
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if ( xmlString1 != null && xmlString1.trim().length()!=0 )
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if ( xmlString2 != null && xmlString2.trim().length()!=0 )
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams );
			System.out.println ( "ErrString :" + errString);
		}
		catch ( Exception e )
		{
			System.out.println ( "Exception :itemChanged(String,String):" + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		System.out.println ( "returning from Bug Fix itemChanged" );
		return (errString);
	}
	
	public String itemChanged( Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		String sql = "";
		String columnValue = "";
		
		String siteCode = "";
		String siteDescr = "";
		String empCodeAprv = "";
		String firstName = "";
		String middleName = "";
		String lastName = "";
		String itemDescr = "";
		String lotNo = "";
		String itemCode= "";
		String holdStatus = "";
		String loginSiteCode = "";

		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		////Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			System.out.println("Inside bug list itemChanged");
			
			//ConnDriver connDriver = null;
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverValidator");
			conn = getConnection();
			
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
			String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
			
			columnValue = genericUtility.getColumnValue( currentColumn, dom );

			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );
			switch( currentFormNo )
			{
				case 1:
				{
					valueXmlString.append( "<Detail1>\r\n" );
					if("itm_default".equalsIgnoreCase(currentColumn))
					{
						valueXmlString.append("<bug_id><![CDATA[").append(checkNull("")).append("]]></bug_id>\r\n");
						valueXmlString.append("<obj_name><![CDATA[").append(checkNull("")).append("]]></obj_name>\r\n");
						valueXmlString.append("<descr><![CDATA[").append(checkNull("")).append("]]></descr>\r\n");
						valueXmlString.append("<solu_desired><![CDATA[").append(checkNull("")).append("]]></solu_desired>\r\n");
						valueXmlString.append("<resolved_ver><![CDATA[").append(checkNull("")).append("]]></resolved_ver>\r\n");
						valueXmlString.append("<status><![CDATA[").append(checkNull("P")).append("]]></status>\r\n" );
						valueXmlString.append("<trans_affected><![CDATA[").append(checkNull("")).append("]]></trans_affected>\r\n");
						valueXmlString.append("<comp_delivered><![CDATA[").append(checkNull("")).append("]]></comp_delivered>\r\n");
						valueXmlString.append("<add_date><![CDATA[").append(checkNull(sysDate)).append("]]></add_date>\r\n");
						valueXmlString.append("<add_term><![CDATA[").append(checkNull(chgTerm)).append("]]></add_term>\r\n" );
						valueXmlString.append("<add_user><![CDATA[").append(checkNull(userId)).append("]]></add_user>\r\n" );
					}
					else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
					{
						valueXmlString.append( "<chg_user><![CDATA[" ).append(checkNull(userId)).append( "]]></chg_user>\r\n" );
						valueXmlString.append( "<chg_date><![CDATA[" ).append(checkNull(sysDate)).append( "]]></chg_date>\r\n" );
						valueXmlString.append( "<chg_term><![CDATA[" ).append(checkNull(chgTerm)).append( "]]></chg_term>\r\n" );
					}
					valueXmlString.append( "</Detail1>\r\n" );
				}//end of case 1
				break;
			}//end of switch
		}//end of try block
		catch( Exception e )
		{
			throw new ITMException(e);
		}
		finally
		{
			try
			{	
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pStmt != null && !pStmt.isClosed() )
				{
					pStmt.close();
					pStmt = null;
				}
				if( conn != null && !conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch( Exception e )
			{
				System.out.println( "Exception :Bug List :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append( "</Root>\r\n" );	
		return valueXmlString.toString();
	}//end of  itemchange
		
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		return input.trim();
	}
	private String errorType( Connection conn , String errorCode ) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 02/08/19
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
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
}
