package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.sql.*;

import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class InvHoldRelIC extends ValidatorEJB implements InvHoldRelICLocal , InvHoldRelICRemote // SessionBean 
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("EpaymentICEJB is in Process..........");
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
		System.out.println("Validation Start..........");
		try
		{
			System.out.println( "xmlString::: " + xmlString );
			System.out.println( "xmlString1:::" + xmlString1 );
			System.out.println( "xmlString2:::" + xmlString2 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : EpaymentICEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
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
		String childNodeName = null;
		
		String errCode = null;
		String userId = null;
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		
		String tranIdHold = null;
		String lineNo = null;
		String siteCode = null;
		
	    ConnDriver connDriver = new ConnDriver();
    	try
		{
			System.out.println( "wfValData called" );
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//genericUtility = GenericUtility.getInstance(); 
			if(objContext != null && objContext.length()>0)
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
						if ( childNodeName.equalsIgnoreCase( "site_code" ) )
						{
							
							//Commented and changed the "cust_code" to "site_code" - Gulzar - 02/12/11
							//siteCode = genericUtility.getColumnValue( "cust_code", dom, "1" );
							siteCode = genericUtility.getColumnValue( "site_code", dom, "1" );
							//End changes by gulzar - 02/12/11
							
							if(	siteCode != null )
							{
								//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
								/*
								sql = " SELECT COUNT(*) FROM site WHERE site_code ='" + siteCode.trim() + "'";
								System.out.println( " SQL FOR custCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								*/
								sql = " SELECT COUNT(*) FROM site WHERE site_code = ?";
								System.out.println( " SQL FOR custCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,siteCode);
								rs = pstmt.executeQuery();
								//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
								
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								System.out.println(" COUNT =====> [" + cnt + "]");
								if( cnt == 0 )
								{
									System.out.println(" ====> Site invalid or not found <==== ");
									errCode = "VMINVSITE";
									errString = getErrorString( "SITE_CODE", errCode, userId );
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "emp_code__aprv" ) )
						{
							String empCode = null;
							empCode = genericUtility.getColumnValue( "emp_code__aprv", dom, "1" );
							if(	empCode != null && empCode.length() > 0 )
							{
								//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
								/*
								sql = " SELECT COUNT(*) FROM Employee WHERE emp_code ='" + empCode.trim() + "'";
								System.out.println( " SQL FOR empCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								*/
								sql = " SELECT COUNT(*) FROM Employee WHERE emp_code = ?";
								System.out.println( " SQL FOR custCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,empCode);
								rs = pstmt.executeQuery();
								//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								System.out.println(" COUNT =====> [" + cnt + "]");
								if( cnt == 0 )
								{
									System.out.println(" ====> Employee invalid or not found <==== ");
									errCode = "VMINVEMP";
									errString = getErrorString( "EMP_CODE", errCode, userId );
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
							} 
						}	
					} //END OF CASE1
					break;
					case 2 :
						System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
						parentNodeList = dom.getElementsByTagName("Detail2");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for(ctr = 0; ctr < childNodeListLength; ctr++)
						{
							cnt = 0;
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();

							errCode = null;
							
							if( childNodeName.equalsIgnoreCase( "tran_id__hold" ) || childNodeName.equalsIgnoreCase( "LINE_NO__HOLD" ) )
							{
								siteCode = genericUtility.getColumnValue( "site_code", dom1, "1" );//Added By Mahesh Patidar on 28/06/12
								tranIdHold = genericUtility.getColumnValue( "tran_id__hold", dom, "2" );
								lineNo = genericUtility.getColumnValue( "line_no__hold", dom, "2" );
								if( tranIdHold != null && tranIdHold.length() > 0 )
								{
									//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
									/*
									sql = "select count( 1 ) cnt from inv_hold_det where tran_id = '" + tranIdHold + "' ";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									*/
									sql = "select count( 1 ) cnt from inv_hold_det where tran_id = ?";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,tranIdHold);
									rs = pstmt.executeQuery();
									//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
									
									if( rs.next() )
									{
										cnt = rs.getInt( "cnt" );
									}
									
									rs.close();
									rs = null;
									
									pstmt.close();
									pstmt = null;
									
									if( cnt == 0 )
									{
										errCode = "VTTRANINV";
									}
									if( errCode == null )
									{
										//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
										/*							
										sql = "select count( 1 ) cnt from inv_hold where tran_id = '" + tranIdHold + "' and confirmed = 'Y' ";
										pstmt = conn.prepareStatement( sql );
										rs = pstmt.executeQuery();
										*/
										sql = "select count( 1 ) cnt from inv_hold where tran_id = ? and confirmed = 'Y' ";
										pstmt = conn.prepareStatement( sql );
										pstmt.setString(1,tranIdHold);
										rs = pstmt.executeQuery();
										//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
										
										if( rs.next() )
										{
											cnt = rs.getInt( "cnt" );
										}
										
										rs.close();
										rs = null;
										
										pstmt.close();
										pstmt = null;
										
										if( cnt == 0 )
										{
											errCode = "VTTRANUNC";
										}
									}
									//Added By Mahesh Patidar on 28/06/12
									if(siteCode != null && siteCode.trim().length() > 0)
									{
										String siteCode1 = "";
										sql = "select site_code from inv_hold where tran_id = ?";
										pstmt = conn.prepareStatement( sql );
										pstmt.setString(1,tranIdHold);
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											siteCode1 = rs.getString(1)==null?"":rs.getString(1).trim();        // trim() Added By Manish on 30/09/15
										}
										      
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										System.out.println("siteCode :"+siteCode);
										System.out.println("siteCode1 :"+siteCode1);
										if(! (siteCode.equals(siteCode1)))
										{
											errCode = "VTSITEINV";
										}
									}
									//Ended By Mahesh Patidar
								}
								else
								{
									errCode = "VTTRANBLK";
								}
								if( errCode != null )
								{
									errString = getErrorString( "tran_id__hold", errCode, userId );	
								}else if( lineNo != null && lineNo.length() > 0 )
								{
									//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
									/*
									sql = "select count( 1 ) cnt from inv_hold_det where tran_id = '" + tranIdHold + "' and line_no = " + lineNo;
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									*/
									sql = "SELECT COUNT( 1 ) CNT FROM INV_HOLD_DET WHERE TRAN_ID = ? AND LINE_NO = ?";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,tranIdHold);
									pstmt.setString(2,lineNo);
									rs = pstmt.executeQuery();
									//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
									if( rs.next() )
									{
										cnt = rs.getInt( "cnt" );
									}
									
									rs.close();
									rs = null;
									
									pstmt.close();
									pstmt = null;
									
									if( cnt == 0 )
									{
										errCode = "VTLINEINV";
									}
								}
								else
								{
									errCode = "VTLINEBLK";
								}
								if( errCode != null )
								{
									errString = getErrorString( "line_no__hold", errCode, userId );	
								}else if ( ( tranIdHold != null && tranIdHold.length() > 0 ) && ( lineNo != null && lineNo.length() > 0 ) )
								{
									//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
									/*
									sql = "select count( 1 ) cnt from inv_hold_det where tran_id = '" + tranIdHold + "' and line_no = " + lineNo + " and HOLD_STATUS = 'R'";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									*/
									sql = "SELECT COUNT( 1 ) CNT FROM INV_HOLD_DET WHERE TRAN_ID = ? AND LINE_NO = ? AND HOLD_STATUS = 'R'";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,tranIdHold);
									pstmt.setString(2,lineNo);
									rs = pstmt.executeQuery();
									//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
									
									if( rs.next() )
									{
										cnt = rs.getInt( "cnt" );
									}
									
									rs.close();
									rs = null;
									
									pstmt.close();
									pstmt = null;
									
									if( cnt > 0 )
									{
										errCode = "VTTRNCNFED";
									}
								}
								if( errCode != null )
								{
									errString = getErrorString( "line_no__hold", errCode, userId );	
								}
								else
								{
									NodeList allLines = null;
									NodeList allTranIds = null;
									
									String tranIdH = null;
									String lineNoH = null;
									int recCnt = 0;
									System.out.println( " tranIdHold, lineNoHold :: ( " + tranIdHold + ", " + lineNo + " )" );
									allLines = dom2.getElementsByTagName("line_no__hold");
									allTranIds = dom2.getElementsByTagName("tran_id__hold");
						
									int noOfLines = allLines.getLength();
									
									for( int idx = 0; idx < noOfLines ; idx++ )
									{
										if ( allTranIds.item( idx ).getFirstChild() != null )
										{
											tranIdH = ( allTranIds.item( idx ).getFirstChild() ).getNodeValue();
										}
										if ( allLines.item( idx ).getFirstChild() != null )
										{
											lineNoH = ( allLines.item( idx ).getFirstChild() ).getNodeValue();
										}
										System.out.println( " tranIdH :: ( " + tranIdH + ", " + lineNoH + " )" );
										if( tranIdH.equals( tranIdHold ) && lineNoH.equals( lineNo ) )
										{
											recCnt++;
										}
									}
									if( recCnt > 1 )
									{
										errCode = "VTDUPENTRY";
										errString = getErrorString( "line_no__hold", errCode, userId );	
									}
								}
							}
						}//END FOR OF CASE2
		     			break;
			}//END SWITCH
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::" +e);
			e.printStackTrace();
			/*errString = e.getMessage();*/ //Commented By Mukesh Chauhan on 07/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
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
			System.out.println(" < ExprProcessIcEJB > CONNECTION IS CLOSED");
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
			dom = parseString(xmlString); 
			System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1); 
			System.out.println("xmlString1" + xmlString1);
			if (xmlString2.length() > 0 )
			{
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [EpaymentICEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement vPStmt = null;
		ResultSet vRs = null;
		String sql = null;
		String itemDescr=null;
		String lockDescr=null;		
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
		int line_no_hold=0;
		String tran_id_hold=null;
		String itemCode=null;
		String locCode=null;
		String lotNo=null;
		String lotSl=null;
		int lineNoSl=0;
		String lineNoHold="";
		String lockCode="";// added by priyanka on  19/08/14
		
		String  loginSite = null; 
		
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if(objContext != null && objContext.length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			
			System.out.println("[EpaymentICEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			System.out.println("Current Form No 19 APr 14!!!["+currentFormNo+"]");							
			switch (currentFormNo)
			{
				case 1:
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail1>");	
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
								columnValue=childNode.getFirstChild().getNodeValue();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					if (currentColumn.equals( "itm_default" ))
					{
						String siteDescription = null;
						
						loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
						/*
						sql = "select s.descr site_descr from site s "
							+" where s.site_code = '" + loginSite + "'";

						vPStmt = conn.prepareStatement( sql );
						vRs = vPStmt.executeQuery();
						*/
						sql = "SELECT S.DESCR SITE_DESCR FROM SITE S WHERE S.SITE_CODE = ?";
						vPStmt = conn.prepareStatement( sql );
						vPStmt.setString(1,loginSite);
						vRs = vPStmt.executeQuery();
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
						
						if( vRs.next() )
						{
							siteDescription = vRs.getString( "site_descr" );
						}
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
	
						
						valueXmlString.append("<tran_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</tran_date>");
						valueXmlString.append("<confirmed>").append("<![CDATA[" + "N" + "]]>").append("</confirmed>");
						valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
						valueXmlString.append("<site_descr>").append("<![CDATA[" + ( siteDescription != null ? siteDescription : ""  )+ "]]>").append("</site_descr>");
					}
					
					if (currentColumn.equals( "site_code" ))
					{
						//site bank 
						String vSql = null;
						String siteDescr = null;
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.start
						/*
						vSql = "select descr from site where site_code = '" + ( columnValue != null ? columnValue.trim() : "" ) + "'";
						vPStmt = conn.prepareStatement( vSql );
						vRs = vPStmt.executeQuery();
						*/
						vSql = "select descr from site where site_code = ?";
						vPStmt = conn.prepareStatement(vSql);
						if(columnValue != null)
						{
							vPStmt.setString(1,columnValue);
						}
						else
						{
							vPStmt.setString(1,"");
						}
						vRs = vPStmt.executeQuery();
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
						
						if( vRs.next() )
						{
							siteDescr = vRs.getString( "descr" );
						}
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						//end getting site bank
						valueXmlString.append("<site_descr>").append("<![CDATA[" + ( siteDescr != null ? siteDescr : ""  )+ "]]>").append("</site_descr>");
					}	
					
					valueXmlString.append("</Detail1>");
					break;
					
				case 2:
					System.out.println("case 2");
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail2>");
					childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					
					//added by priyanka as per manoj sharma instruction on 19/08/14
					// on itemchanged of tran_id__hold,values of all field should get blank
					if (currentColumn.equals("tran_id__hold"))
					{
						System.out.println("tran_id__hold itemChanged *************");
						tran_id_hold=checkNull(genericUtility.getColumnValue("tran_id__hold", dom));
						System.out.println("tran_id_hold :"+tran_id_hold);
						valueXmlString.append("<line_no__hold>").append("<![CDATA[" +"" + "]]>").append("</line_no__hold>");						
						valueXmlString.append("<item_code>").append("<![CDATA[" +"" + "]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" +""+ "]]>").append("</item_descr>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" + "" + "]]>").append("</loc_code>");
						valueXmlString.append("<descr>").append("<![CDATA[" + "" + "]]>").append("</descr>");
						valueXmlString.append("<lot_no>").append("<![CDATA[" +"" + "]]>").append("</lot_no>");
						valueXmlString.append("<lot_sl>").append("<![CDATA[" + "" + "]]>").append("</lot_sl>");
						valueXmlString.append("<line_no_sl>").append("<![CDATA[" + "" + "]]>").append("</line_no_sl>");
						valueXmlString.append("<lock_code>").append("<![CDATA[" + "" + "]]>").append("</lock_code>");
					}
					
					else if (currentColumn.equals("line_no__hold"))
					{
						System.out.println("currentColumn======="+currentColumn);
						System.out.println("columnValue======="+columnValue);
						
						System.out.println("entering in line_no__hold !!!");
						//
						lineNoHold=checkNull(genericUtility.getColumnValue("line_no__hold", dom));
						
						System.out.println("lineNoHold===== :"+lineNoHold);
						tran_id_hold=checkNull(genericUtility.getColumnValue("tran_id__hold", dom));
						System.out.println("tran_id_hold :"+tran_id_hold);		
										    
					    System.out.println("line_no_hold========>"+line_no_hold);
						sql = "select item_code,loc_code,lot_no,lot_sl,line_no_sl from inv_hold_det where tran_id=? and line_no=?";
						vPStmt = conn.prepareStatement( sql );
						vPStmt.setString(1,tran_id_hold);
						vPStmt.setString(2,lineNoHold);
						vRs = vPStmt.executeQuery();
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
						
						if( vRs.next() )
						{
							itemCode = checkNull(vRs.getString("item_code"));
							locCode= checkNull(vRs.getString("loc_code"));
							lotNo=checkNull(vRs.getString("lot_no"));
							lotSl=checkNull(vRs.getString("lot_sl"));
							lineNoSl=vRs.getInt("line_no_sl");
						}
						System.out.println("itemCode  "+itemCode);
						System.out.println("locCode  "+locCode);
						System.out.println("lotNo  "+lotNo);
						System.out.println("lineNoSl  "+lineNoSl);
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						
						
						sql = "select DESCR from item where  ITEM_CODE=?";
						vPStmt = conn.prepareStatement( sql );
						vPStmt.setString(1,itemCode);
						
						vRs = vPStmt.executeQuery();
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
						
						if( vRs.next() )
						{
							itemDescr=vRs.getString("DESCR");
						}
						System.out.println("itemDescr  "+itemDescr);
						
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						
						
						sql = "select DESCR from location where LOC_CODE =?";
						vPStmt = conn.prepareStatement( sql );
						vPStmt.setString(1,locCode);
						
						vRs = vPStmt.executeQuery();
						//Changed by Dharmesh on 09/08/11 [WM1ESUN004]  to bind variable dynamically instead of statically.end
						
						if( vRs.next() )
						{
							lockDescr=vRs.getString("DESCR");
						}
						System.out.println("lockDescr  "+lockDescr);
						
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						
						//added by priyanka as per manoj sharma instruction on 19/08/14
						//on itemchanged of line_no__hold value of lock_code should get set from hold inventory
						
						sql="select lock_code from inv_hold where tran_id=?";
						vPStmt = conn.prepareStatement( sql );
						vPStmt.setString(1,tran_id_hold);						
						vRs = vPStmt.executeQuery();					
						if( vRs.next() )
						{
							lockCode = vRs.getString("lock_code");							
						}
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						System.out.println("lockCode  "+lockCode);
						
						valueXmlString.append("<item_code >").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr + "]]>").append("</item_descr>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
						valueXmlString.append("<descr>").append("<![CDATA[" + lockDescr + "]]>").append("</descr>");
						valueXmlString.append("<lot_no>").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
						valueXmlString.append("<lot_sl>").append("<![CDATA[" + lotSl + "]]>").append("</lot_sl>");
						valueXmlString.append("<line_no_sl>").append("<![CDATA[" + lineNoSl + "]]>").append("</line_no_sl>");
						valueXmlString.append("<lock_code>").append("<![CDATA[" + lockCode + "]]>").append("</lock_code>");
											
				}
					valueXmlString.append("</Detail2>");
					break;
					
					
								
			}//END OF switch
			valueXmlString.append("</Root>");	
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
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
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}			
		}
		
		System.out.println("@@@@@@ test  valueXmlString["+ valueXmlString.toString()+"]");
		
		return valueXmlString.toString();
	}//END OF ITEMCHANGE
	private String getCurrdateAppFormat() throws ITMException
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(genericUtility.getDBDateFormat());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		}
		catch(Exception exception)
		{
			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
			throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
		}
		return s;
	}	
	
	
	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}
	
	
	
	
}