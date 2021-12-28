 /**
 * Author : Chaitali Parab
 * Date   : 
 * 
 * */
 
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;

import ibase.system.config.*;
import ibase.webitm.ejb.*; 

//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.dis.*;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

@javax.ejb.Stateless
public class LotStatIC extends ValidatorEJB implements LotStatICLocal, LotStatICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
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
		//GenericUtility genericUtility = GenericUtility.getInstance();
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
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
		int currentFormNo = 0;
		int count = 0;
		String columnName = "";
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pStmt = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		StringBuffer valueXmlString = new StringBuffer();
		String itemCode = "", lotNo = "", lockCode = "";
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String errorType = "", errCode = "";
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();

		try
		{
			SimpleDateFormat sdft = new SimpleDateFormat( genericUtility.getApplDateFormat() );
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

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
							
							if( "site_code".equalsIgnoreCase(columnName) )
							{
								//checking for blank value
								if ( childNode == null || childNode.getFirstChild() == null )
								{
									errList.add( "VTSITEEMT" );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									sql = "select count(*) from site where site_code =?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, columnValue);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									if( count == 0 )
									{
										errList.add( "VTSITE123" );
										errFields.add( childNodeName.toLowerCase() );
									}
									//VTSITE123 
								}
								
							}
							/*
							else if( "emp_code__aprv".equalsIgnoreCase(columnName) )
							{
								if ( childNode == null || childNode.getFirstChild() == null )
								{
									//errString = getErrorString(columnName ,"VTEMPNULL",userId);
									//break;
								}
								else
								{
									sql = "select count(*) from employee where emp_code =? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, columnValue);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									if( count == 0 )
									{
										errString = getErrorString(columnName ,"VMEMPCD1",userId);
										break;
									}
								}
							}
							*/
						}
					}
				}//end of case 1
				break;
				case 2 :
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
							
							if("item_code".equalsIgnoreCase(columnName))
							{
								if ( childNode == null || childNode.getFirstChild() == null )
								{
									errList.add( "VTITMNUL" );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									sql = "select count(*) from item where item_code =?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, columnValue);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									if( count == 0 )
									{
										errList.add( "VTITEMCD1" );
										errFields.add( childNodeName.toLowerCase() );
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									/*
									count = 0;
									lotNo = genericUtility.getColumnValue( "lot_no", dom );
									sql = "select count(*) from asn_det where item_code= ? and lot_no = ? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, columnValue);
									pStmt.setString(2, lotNo);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									if( count == 0 )
									{
										errList.add( "VTASNITEM" );
										errFields.add( childNodeName.toLowerCase() );
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									*/
								}
							}
							if("lock_code".equalsIgnoreCase(columnName)) // 10/04/14 manoharan added validation for loc_code
							{
								if ( childNode == null || childNode.getFirstChild() == null )
								{
									errList.add( "NULLLOCKCD" );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									sql = "select count(*) from inv_lock where lock_code =?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, columnValue);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									if( count == 0 )
									{
										errList.add( "INVLOCKCD" );
										errFields.add( childNodeName.toLowerCase() );
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
								}
							}
							else if("lot_no".equalsIgnoreCase(columnName))
							{
								if ( childNode == null || childNode.getFirstChild() == null )
								{
									errList.add( "VTNULLLOT" );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
								
									itemCode = genericUtility.getColumnValue( "item_code", dom );
									lotNo = genericUtility.getColumnValue( "lot_no", dom );
									lockCode = genericUtility.getColumnValue( "lock_code", dom );
									sql = "select count(*) from inv_hold h, inv_hold_det d "
										+ " where h.tran_id = d.tran_id and h.confirmed = 'Y' and h.lock_code = ? and d.item_code = ? and d.lot_no = ? and d.hold_status = 'H'";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, lockCode);
									pStmt.setString(2, itemCode);
									pStmt.setString(3, lotNo);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null;	
									if (count == 0)
									{
										errList.add( "VTHOLDLOT" );
										errFields.add( childNodeName.toLowerCase() );
									}
								
									/*
									sql = "select count(*) from stock where lot_no = ?";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, columnValue);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									if( count == 0 )
									{
										errString = getErrorString(columnName ,"VTLOTASN",userId);
										break;
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									count = 0;
									
									itemCode = genericUtility.getColumnValue( "item_code", dom );
									sql = "select count(*) from asn_det where item_code = ? and lot_no=? ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, itemCode);
									pStmt.setString(2, columnValue);
									rs = pStmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt(1); 
									}
									rs.close();rs = null;
									pStmt.close();pStmt = null;
									if( count == 0 )
									{
										errList.add( "VTLOTASN1" );
										errFields.add( childNodeName.toLowerCase() );
									}
									*/
								}
							}
						}
					}
				}//end of case 2
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
				System.out.println( "Exception :AsnLotStatIC:wfValData :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}//end of finally block
		return ( errString );
	}//
	/**
	* The method defined with no paramter and returns nothing
	*/
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}
	/**
	 * The public method is used for itemchange of required fields which inturn called overloded method
	 * Returns itemchange string in XML format
	 * @param xmlString contains the current form data in XML format
	 * @param xmlString1 contains always header form data in XML format
	 * @param xmlString2 contains all forms data in XML format 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		System.out.println ( "returning from AsnLotStatIC itemChanged" );
		return (errString);
	}
	/**
	 * The public overloded method is used for itemchange of required fields 
	 * Returns itemchange string in XML format
	 * @param dom contains the current form data 
	 * @param dom1 contains always header form data
	 * @param dom2 contains all forms data 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginCode etc
	 */
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
		
		Date currentDate = null;
		SimpleDateFormat sdf = null;
	
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			columnValue = genericUtility.getColumnValue( currentColumn, dom );

			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverValidator");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );
			switch( currentFormNo )
			{
				case 1:
				{
					valueXmlString.append( "<Detail1>\r\n" );
					if("itm_default".equalsIgnoreCase(currentColumn))
					{
						loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
						sql = "select descr from site where site_code =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, loginSiteCode);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							siteDescr = checkNull(rs.getString("descr")); 
						}
						currentDate = new Date();
						valueXmlString.append( "<tran_date><![CDATA[" ).append(sdf.format(currentDate).toString()).append( "]]></tran_date>\r\n" );
						valueXmlString.append( "<site_code><![CDATA[" ).append(loginSiteCode).append( "]]></site_code >\r\n" );
						valueXmlString.append( "<site_descr><![CDATA[" ).append(siteDescr).append( "]]></site_descr>\r\n" );
						//added on 23 nov 2011
					}
					else if( "site_code".equalsIgnoreCase(currentColumn) )
					{
						sql = "select descr from site where site_code =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, columnValue);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							siteDescr = checkNull(rs.getString("descr")); 
						}
						valueXmlString.append( "<site_descr><![CDATA[" ).append(siteDescr).append( "]]></site_descr>\r\n" );
					}
					else if( "emp_code__aprv".equalsIgnoreCase(currentColumn))
					{
						sql = "select emp_fname, emp_mname, emp_lname from employee where emp_code= ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, columnValue);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							firstName = checkNull(rs.getString("emp_fname"));
							middleName = checkNull(rs.getString("emp_mname"));
							lastName = checkNull(rs.getString("emp_lname"));
						}
						valueXmlString.append( "<emp_name><![CDATA[" ).append(firstName.trim()+" "+middleName.trim()+" "+lastName.trim()).append( "]]></emp_name>\r\n" );
					}
					valueXmlString.append( "</Detail1>\r\n" );
				}//end of case 1
				break;
				case 2 :
				{
					valueXmlString.append( "<Detail2>\r\n" );
					if("item_code".equalsIgnoreCase(currentColumn) )
					{
						sql = "select descr from item where item_code =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, columnValue);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							itemDescr = checkNull(rs.getString("descr"));
						}
						valueXmlString.append( "<item_descr><![CDATA[" ).append(itemDescr).append( "]]></item_descr>\r\n" );
					}
					valueXmlString.append( "</Detail2>\r\n" );
				}//end of case 2
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
				System.out.println( "Exception :AsnLotStatIC:itemChanged :==>\n"+e.getMessage());
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
		return input;
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
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
