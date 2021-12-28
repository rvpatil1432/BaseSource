package ibase.webitm.ejb.dis;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;


@javax.ejb.Stateless
public class CustBillPrefIC extends ValidatorEJB implements CustBillPrefICRemote,CustBillPrefICLocal{

	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		System.out.println("WFVALDATA 111111");
		try
		{
			System.out.println("xmlString [" + xmlString + "]");
			System.out.println("xmlString1 [" + xmlString1 + "]");
			System.out.println("xmlString2 [" + xmlString2 + "]");
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
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams );
			System.out.println ( "ErrString: " + errString);
		}
		catch(Exception e)
		{
			System.out.println ( "Exception: CustBillPrefIC: wfValData(String xmlString): " + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
		}
		System.out.println ( "Returning from CustBillPrefIC wfValData" );
		return ( errString ); 
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext,String editFlag, String xtraParams ) throws RemoteException, ITMException
	{
		String errString = "";
		Statement stmt = null;
		ResultSet rs = null;
	//	Connection conn = null;
		PreparedStatement pstmt = null;

		E12GenericUtility genericUtility = new E12GenericUtility();

		DistCommon distComm = new DistCommon();

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String errorType = "", errCode = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		
		
		NodeList parentList = null;
		NodeList childList = null;
		int noOfChilds ;
		String childNodeName = "";
		Node childNode =null;
		String userId = "";

		
		
		

		java.util.Date currDate = null;

		
		
		String dbDateFormat = "";
		String applDateFormat = "";
		DateFormat dateFormat = null;
		String currDateStr = "";
		int cnt = 0;
		String columnValue = "";
		String sql = "";			
		int currentFormNo = 0;			
		//ConnDriver connDriver = null;
		
		String prefOrderFromTable="";
		String prefOrder="";
		ArrayList<String> list=null;
		String custcodebillalt="";
		String custCode="";
		String remarks="";
		String preforder="";
		String preforderDom="";
		Connection conn = null;

		try{			
			
//			connDriver = new ConnDriver();
//			conn = connDriver.getConnectDB("DriverITM");
//			connDriver = null;
			
			ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("Driver");
			conn = getConnection();
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );

			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new java.util.Date();
			dbDateFormat = genericUtility.getDBDateFormat();
			applDateFormat = genericUtility.getApplDateFormat();
			dateFormat = new SimpleDateFormat(applDateFormat);
			currDateStr = dateFormat.format(currDate);
			SimpleDateFormat simpleDateFormatDB = new SimpleDateFormat(genericUtility.getDBDateFormat());
			Timestamp timestamp = Timestamp.valueOf(simpleDateFormatDB.format(currDate).toString() + " 00:00:00.0");
			System.out.println("objContext::"+objContext);

			if ( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			switch(currentFormNo)
			{
			case 1 :
			{
				parentList = dom.getElementsByTagName( "Detail" + currentFormNo );
				System.out.print("currentFormNo::"+currentFormNo);
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++)
				{	
					childNode = childList.item( ctr );
					childNodeName = childNode.getNodeName();
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
					if ( "cust_code".equalsIgnoreCase( childNodeName ) )
					{
						System.out.println("Customer Code::");
						custCode=genericUtility.getColumnValue( "cust_code", dom );
						System.out.println("Customer Code::"+custCode);
						if ( custCode == null || custCode.trim().length() == 0)
						{

							errList.add( "VTINVCUSTI" );//CUST CODE NOT BLANK
							//errList.add(errCode);
							errFields.add( childNodeName.toLowerCase() );
						}	
						else
						{

							System.out.println("cust Code Exist::"+custCode);
							sql="select count(1) AS CNT from customer where cust_code=?";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);



							rs = pstmt.executeQuery();
							if( rs.next() )
							{		
								cnt = rs.getInt("CNT");
							}
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
							if( cnt == 0 )
							{

								errList.add( "VTINVCUSTC" );//CUST CODE DOES NOT EXIST
								errFields.add( childNodeName.toLowerCase() );

							}
						}

					}
					else if ( "cust_code__bil".equalsIgnoreCase( childNodeName ) )
					{
						System.out.println("cust_code__bil::");
						custcodebillalt=genericUtility.getColumnValue( "cust_code__bil", dom );
						System.out.println("cust_code__bil::"+custcodebillalt);
						if ( custcodebillalt == null || custcodebillalt.trim().length() == 0 )
						{

							errList.add( "VTINVBILAT" );//BIL ALT NOT BLANK
							//errList.add(errCode);
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{

							System.out.println("cust Code Exist::"+custcodebillalt);
							sql="select count(1) AS CNT from customer where cust_code=?";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custcodebillalt);



							rs = pstmt.executeQuery();
							if( rs.next() )
							{		
								cnt = rs.getInt("CNT");
							}
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
							if( cnt == 0 )
							{

								errList.add( "VTINVBILAN" );//BILL ALT DOESNOT EXIST
								errFields.add( childNodeName.toLowerCase() );

							}
							else
							{
								if (  editFlag != null && "A".equalsIgnoreCase(editFlag.trim())  )
								{ //add mode
									System.out.println("Add Mode::");
									sql="select COUNT(1)  AS CNT from customer_bill where cust_code=? and cust_code__bil=? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, custcodebillalt);
									rs = pstmt.executeQuery();

									if( rs.next() )
									{
										cnt = rs.getInt("CNT");
									}
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
									if( cnt > 0 )
									{
										errList.add( "VTINVPRNOV" );// CUST_CODE__BIL NOT VALID
										errFields.add( childNodeName.toLowerCase());
									}
								}
							}

						}
					}	

					else if ( "pref_order".equalsIgnoreCase( childNodeName ) )
					{



						System.out.println("pref_order::");
						preforderDom=genericUtility.getColumnValue( "pref_order", dom );

						custCode = genericUtility.getColumnValue( "cust_code", dom );
						custcodebillalt=genericUtility.getColumnValue( "cust_code__bil", dom );

						System.out.println("pref_order::"+preforderDom);
						if ( preforderDom == null || preforderDom.trim().length() == 0 || preforderDom.trim().equalsIgnoreCase("0"))
						{

							errList.add( "VTINVPRORD" );//PREF_ORDER NOT BLANK > 0.
							//errList.add(errCode);
							errFields.add( childNodeName.toLowerCase() );
						}
						else {

							System.out.println("Flag Status::"+editFlag);
							list=new ArrayList<String>();
							System.out.println("preforderDom in add/edit::"+preforderDom);
							custCode=genericUtility.getColumnValue( "cust_code", dom );
							System.out.println("cust_code in add/edit::"+custCode);
							custcodebillalt=genericUtility.getColumnValue( "cust_code__bil", dom );
							System.out.println("cust_code__bil in add/edit::"+custcodebillalt);
							if ("A".equalsIgnoreCase(editFlag.trim()))
							{
								sql="select pref_order from customer_bill where cust_code=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();

								while( rs.next() )
								{
									prefOrder=rs.getString("pref_order");
									list.add(prefOrder);
								}
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
								if(list.size()>0)
								{
									if(list.contains(preforderDom))
									{
										System.out.println("Pref_Order Already Exist for respective Cust_Code"+preforderDom);
										errList.add( "VTINVPRNOE" );// PREF NOT VALID
										errFields.add( childNodeName.toLowerCase());
									}
								}
							}
							else
							{
								
								sql="select pref_order from customer_bill where cust_code=? and cust_code__bil=? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,custcodebillalt);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									prefOrderFromTable=rs.getString("pref_order");
									
									
								}
								pstmt.close();
								pstmt=null;
								rs.close();
								rs=null;
								if(!preforderDom.equals(prefOrderFromTable))
								{
									sql="select pref_order from customer_bill where cust_code=? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									rs = pstmt.executeQuery();

									while( rs.next() )
									{
										prefOrder=rs.getString("pref_order");
										list.add(prefOrder);
									}
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
									if(list.contains(preforderDom))
									{
										System.out.println("Pref_Order Already Exist for respective Cust_Code"+preforderDom);
										errList.add( "VTINVPRNOE" );// PREF NOT VALID
										errFields.add( childNodeName.toLowerCase());
									}
									
								}
								
							}
							/*else
							{
								
								list=new ArrayList<String>();
								preforder=genericUtility.getColumnValue( "pref_order", dom );
								System.out.println("pref_order in add/edit::"+preforder);
								custCode=genericUtility.getColumnValue( "cust_code", dom );
								System.out.println("cust_code in add/edit::"+custCode);
								custcodebillalt=genericUtility.getColumnValue( "cust_code__bil", dom );
								System.out.println("cust_code__bil in add/edit::"+custcodebillalt);
								sql="select pref_order from customer_bill where cust_code=? and cust_code__bil=? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								pstmt.setString(2,custcodebillalt);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									prefOrder=rs.getString("pref_order");
									list.add(prefOrder);
									
								}
								pstmt.close();
								pstmt=null;
								rs.close();
								rs=null;
								
								if(!list.contains(preforder))
								{
									System.out.println("Pref_Order Already Exist for respective Cust_Code & Cust_Code_Bil"+preforder);
									errList.add( "VTINVPROR" );// PREF NOT VALID
									errFields.add( childNodeName.toLowerCase());
									
								}
							}*/
							

						}
					}
					else if ( "remarks".equalsIgnoreCase( childNodeName ) )
					{
						System.out.println("remarks::");
						remarks=genericUtility.getColumnValue( "remarks", dom );
						System.out.println("CONN_STRING::"+remarks);
						
						
						if ( remarks == null || remarks.trim().length() == 0 )
						{
							errList.add( "VTINVREMRK" );//REMARKS NOT BLANK
							//errList.add(errCode);
							errFields.add( childNodeName.toLowerCase() );
						}
						
					
					}

				}
			}	
			break;

			} 

			int errListSize = errList.size();
			cnt =0;
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


		}catch(Exception e)
		{
			System.out.println ( "Exception: CustBillPrefIC: wfValData(String xmlString): " + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if( conn != null && ! conn.isClosed() )
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
				//System.out.println("------------------------INSIDE FINALLY-------------------");
			}
			catch(Exception e)
			{
				System.out.println( "Exception :CustBillPrefHdr:itemChanged :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		System.out.println ( "Returning from CustBillPrefIC wfValData" );
		return ( errString );
	}
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
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			if (xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if (xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if (xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams );
			System.out.println ( "ErrString :" + errString);
		}
		catch (Exception e)
		{
			System.out.println ( "Exception :CustBillPrefIC :itemChanged(String,String):" + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
		}

		System.out.println ( "returning from CustBillPrefIC itemChanged" );

		return errString;
	}
	public String itemChanged( Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		String chgTerm ="";
		ResultSet rs = null;
		
		String columnValue = "";
		String siteCode = "";
		
		
		
		
		String custcode="";
		String custname="";
		String sql1="";
		String bilaltcustname1="";
		String custcodebilalt="";
		Connection conn = null;
		
		
		
		

		//Connection conn = null;
		PreparedStatement pstmt = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		E12GenericUtility genericUtility = new E12GenericUtility();

		try
		{
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			columnValue = genericUtility.getColumnValue( currentColumn, dom );
			InetAddress ownIP=InetAddress.getLocalHost();
			chgTerm = ownIP.getHostAddress();

			System.out.println("<!@#> ItemChange  for CustBillPref is Called objContext " +objContext);

//			ConnDriver connDriver = null;
//			connDriver = new ConnDriver();
//			conn = connDriver.getConnectDB("DriverValidator");
			
			ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("Driver");
			conn = getConnection();
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );

			switch ( currentFormNo )
			{
			case 1:
			{
				valueXmlString.append( "<Detail1>\r\n" );

				if ("cust_code".equalsIgnoreCase(currentColumn.trim()) )
				{
					custcode = checkNull(genericUtility.getColumnValue("cust_code",dom));
					System.out.println("Cust Code::"+custcode);


					sql1="select cust_name from customer where cust_code=?";

					pstmt = conn.prepareStatement(sql1);
					pstmt.setString(1, custcode);



					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						custname = rs.getString("cust_name");
					}
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
					valueXmlString.append("<cust_name>").append("<![CDATA["+custname+"]]>").append("</cust_name>\r\n");


				}
				else if("cust_code__bil".equalsIgnoreCase(currentColumn.trim() ))
				{
					custcodebilalt = checkNull(genericUtility.getColumnValue("cust_code__bil",dom));
					System.out.println("custcodebilalt::"+custcodebilalt);


					sql1="select cust_name from customer where cust_code=?";

					pstmt = conn.prepareStatement(sql1);
					pstmt.setString(1, custcodebilalt);



					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						bilaltcustname1 = rs.getString("cust_name");
					}
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
					valueXmlString.append("<cust_name_1>").append("<![CDATA["+bilaltcustname1+"]]>").append("</cust_name_1>\r\n");
				}


			}
			valueXmlString.append( "</Detail1>\r\n" );
			break;
			}
		}
		catch(Exception e)
		{
			System.out.println( "Exception :CustBillPrefHdr :itemChanged(Document,String):" + e.getMessage() + ":" );
			valueXmlString = valueXmlString.append( genericUtility.createErrorString( e ) );
		}
		finally
		{
			try
			{
				if( conn != null && ! conn.isClosed() )
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
					conn=null;
				}
				//System.out.println("------------------------INSIDE FINALLY-------------------");
			}
			catch(Exception e)
			{
				System.out.println( "Exception :CustBillPrefHdr:itemChanged :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append( "</Root>\r\n" );	
		System.out.println( "\n****ValueXmlString :" + valueXmlString.toString() + ":********" );
		return valueXmlString.toString();
	}
	private String errorType( Connection conn , String errorCode )
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
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
