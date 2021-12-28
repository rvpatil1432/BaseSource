/**
 * PURPOSE : Validation and Item Change implementation for Stock Transfer component .
 * AUTHOR  : BALU
 * Date    : 19/09/2011
 * 
 */

package ibase.webitm.ejb.dis;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.xml.parsers.*;

import org.w3c.dom.*;

import ibase.ejb.*;
import ibase.system.config.*;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;


import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.InputSource;

import java.net.InetAddress;

@javax.ejb.Stateless
public class StockTransferMultipleIC extends ValidatorEJB implements StockTransferMultipleICRemote, StockTransferMultipleICLocal  
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	// Validation Code. Start	
	/**
	 * The method is defined without any parameter and returns blank string
	 */
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	/**
	 * The public method is used for converting the current form data into a document(DOM)
	 * The currDom is then given as argument to the overloaded function wfValData to perform validation
	 * Returns validation string if exists else returns null in XML format
	 * @param currFrmXmlStr contains the current form data in XML format
	 * @param hdrFrmXmlStr contains all the header information in the XML format
	 * @param allFrmXmlStr contains the data of all the forms in XML format
	 * @param objContext represents the form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */	
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = "";

		try
		{			
			
			System.out.println("currFrmXmlStr : [" +currFrmXmlStr+"]");
					
			if(currFrmXmlStr != null && currFrmXmlStr.trim().length()!=0)
			{
				
				currDom = parseString(currFrmXmlStr); 
			}
			if(hdrFrmXmlStr != null && hdrFrmXmlStr.trim().length()!=0)
			{
				hdrDom = parseString(hdrFrmXmlStr); 
			}
			if(allFrmXmlStr != null && allFrmXmlStr.trim().length()!=0)
			{
				allDom = parseString(allFrmXmlStr);
			}
			errString = wfValData(currDom,hdrDom,allDom,objContext,editFlag,xtraParams);
		}//end of try
		catch(Exception e)
		{
			System.out.println("Exception : [StockTransferMultipleIC][wfValData(String currFrmXmlStr)] : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return (errString); 
	}

	/**
	 * The public overloaded method takes a document as input and is used for the validation of specified fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currDom contains the current form data as a document object model
	 * @param hdrDom contains all the header information
	 * @param allDom contains the field data of all the forms 
	 * @param objContext represents form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */	
	public String wfValData(Document currDom, Document hdrDom, Document allDom, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 	
	{
		System.out.println("Call stocktransfermultipleIc wfValData");
		String errString = "";
		String columnValue = "";
		String sql = "";	
		String refSer = "";
		String locCodeTo = "";
		String tranDate = "";
		String singleLotSl = "";
		String itemLotOpt = "";
		String rowCountStr = "";
		String itemCode = "";
		String siteCode = "";
		String lotNo = "";
		String lpnNo = "";
		String lpnNoTo = "";
		String childNodeName = "";
		String resrvLocInvstat = "";
		String activePickInvstat = "", locCode = "";
		String casePickInvstat = "";
		String invStat = "";
		String locCodeFrom = "";
		String locFrominvStat = "";
		String locToinvStat = "";
		String NoslInvstat = "";
		int locCount = 0;
		String locInvList = "";
		String[] locList = null;
		String invStatLoc = "";
		//changed by Dhanraj on 30-08-14 [W14DSUN001] add interLocXFrXAllow for control stock tranfer to same,cross or not to invstat.
		String interLocXFrXAllow="2";
		boolean isLocToValid = true;
		java.util.Date tranDateDt = null;
		String prdCode = "";
		int noOfChilds = 0;
		int noOfParent = 0;                            
		int count = 0;   
		double quantity = 0d,shipperSize = 0;         
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		DistCommon distCommon = new DistCommon();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String errorType = "", errCode = "", phyHanBasis= "";
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		ArrayList<String> invStatList = new ArrayList<String>();
		String availableFromLoc="",availableToLoc="",disparamInvstat="",invStateTo="";
		//Changed By Pragyan 11-AUG-14 To check and Facility Master implementation.start
        ibase.webitm.ejb.dis.CommonWmsUtil1 commonWmsUtility =ibase.webitm.ejb.dis.CommonWmsUtil1.getInstance();
		try
		{

			int currentFormNo = 0, cnt = 0;		

			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 


			Node childNode =null;
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			//Changes by Dadaso pawar on 19/FEB/15 [Start]	
			CommonWmsUtil1 commonWmsUtility1 = CommonWmsUtil1.getInstance();
			String loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			loginSite = loginSite == null ? "" :loginSite.trim();
			System.out.println("loginSite1111---------------->>["+loginSite+"]");
			//Changes by Dadaso pawar on 19/FEB/15 [End]
			System.out.println("objContext::::::::::::::::::::;;;"+objContext);
			if ( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
            System.out.println("currentFormNo:::::::"+currentFormNo);
			NodeList parentList = (currDom.getElementsByTagName( "Detail" + currentFormNo )) ;
			NodeList childList = null;	
			noOfParent = parentList.getLength();

			switch(currentFormNo)
			{
			case 1 :
			{
				System.out.println("Case 1 Validation");
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++)
				{	
					childNode = childList.item( ctr );
					if( childNode.getNodeType() != Node.ELEMENT_NODE )
					{
						continue;
					}
					childNodeName = childNode.getNodeName();						
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
					if ( "tran_date".equalsIgnoreCase( childNodeName ) )
					{
						if ( childNode.getFirstChild() == null )
						{
							errList.add( "NULLTRANDT" );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					else if ( "ref_ser".equalsIgnoreCase( childNodeName ) )
					{
						if ( childNode.getFirstChild() == null )
						{
							errList.add( "NULLREFSER" );
							errFields.add( childNodeName.toLowerCase() );
						}
						refSer = genericUtility.getColumnValue( "ref_ser", currDom );

						sql = "SELECT COUNT(*) AS COUNT FROM REFSER WHERE REF_SER = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, refSer);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0)
						{
							errList.add( "VTREFSER1" );
							errFields.add( childNodeName.toLowerCase() );
						}						
					}
					else if ( "site_code".equalsIgnoreCase( childNodeName ) )
					{							
						if ( childNode.getFirstChild() == null )
						{
							errList.add( "NULLSITE" );
							errFields.add( childNodeName.toLowerCase() );
						}
						siteCode = genericUtility.getColumnValue( "site_code", currDom );

						sql = "SELECT COUNT(*) AS COUNT FROM SITE WHERE SITE_CODE = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;							
						pstmt.close();
						pstmt = null;

						if(cnt == 0)
						{								
							errList.add( "INVSITECD" );
							errFields.add( childNodeName.toLowerCase() );
						}
						else 
						{

							siteCode = genericUtility.getColumnValue("site_code", currDom);
							cnt = 0; 
							System.out.println("364 site code ="+siteCode);
							if (genericUtility.getColumnValue("tran_date", currDom) != null)
							{
								tranDateDt = sdf1.parse(genericUtility.getColumnValue("tran_date", currDom));
								sql = "select code from period where ?  between fr_date and to_date";
								pstmt = conn.prepareStatement(sql);
								pstmt.setDate(1, new java.sql.Date(tranDateDt.getTime()));
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									prdCode = rs.getString(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select count(*) from period_stat where site_code = ? and prd_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, prdCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0)
								{
									errList.add( "VTSITEPD" );
									errFields.add( childNodeName.toLowerCase() );
									break;
								}
								//Changes and Commented By Ajay on 20-12-2017 :START
								//errCode = nfCheckPeriod("IC", tranDateDt, siteCode);
								errCode=finCommon.nfCheckPeriod("IC", tranDateDt, siteCode, conn);
								//Changes and Commented By Ajay on 20-12-2017 :END
								System.out.println("425 Error Code = " + errCode);
								if (errCode != null && errCode.trim().length() > 0)
								{
									errList.add(errCode);
									errFields.add( childNodeName.toLowerCase() );
								}
							} 							 
						}
					}
					else if ( "reas_code".equalsIgnoreCase( childNodeName ) )
					{
						if ( childNode.getFirstChild() == null )
						{
							errList.add( "NULLREASON" );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
				}
			}
			break;
			case 2 :
			{
				System.out.println("Case 2 Validation");
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++)
				{	
					childNode = childList.item( ctr );
					if( childNode.getNodeType() != Node.ELEMENT_NODE )
					{
						continue;
					}
					childNodeName = childNode.getNodeName();						
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");

					if ( "loc_code__fr".equalsIgnoreCase( childNodeName ) )
					{	
						locCode = genericUtility.getColumnValue( "loc_code__fr", currDom );
						System.out.println("inside loc code::::::::"+ locCode);
						if (locCode == null)
						{

							errList.add( "VMLOC4" );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM LOCATION WHERE LOC_CODE = ? ";

							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
							rs = pstmt.executeQuery();
							if ( rs.next() )
							{
								count = rs.getInt("COUNT");
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if ( count == 0 )
							{
								errList.add( "VMLOC6" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						//Changes by Dadaso pawar on 20/02/15 [Start]
						if(!commonWmsUtility1.isValFacLocation(locCode, loginSite, conn))
						{
							System.out.println("inside facility........");
							errList.add( "VMFACI2" ); // 'Diffrent Loc Code from location and site' (set same error Code @base)
							errFields.add( childNodeName.toLowerCase() );
							break;
						}
						//Changes by Dadaso pawar on 20/02/15 [End]
					}
					/*else if ( "lpn_no".equalsIgnoreCase( childNodeName ) )
						{	
							lpnNo = genericUtility.getColumnValue( "lpn_no", currDom );
							locCode = genericUtility.getColumnValue( "loc_code", currDom );

							if ( childNode.getFirstChild() == null )
							{
								// 26/10/11 manoharan all the errors should be returned so that warnings can be processed
								//errString = getErrorString("lpn_no","NULLLPNNO",userId);
								//break;
								errList.add( "NULLLPNNO" );
								errFields.add( childNodeName.toLowerCase() );
								// end 26/10/11 manoharan all the errors should be returned so that warnings can be processed
							}
							else
							{
								Commented and changes below by gulzar at sun on 1/12/2012
								sql = "SELECT COUNT(*) AS COUNT " +
										"FROM STOCK STOCK, ITEM ITEM, LOCATION LOCATION " +
										"WHERE STOCK.ITEM_CODE = ITEM.ITEM_CODE AND STOCK.LOC_CODE = LOCATION.LOC_CODE "+
										"AND ((CASE WHEN STOCK.QUANTITY IS NULL THEN 0 ELSE STOCK.QUANTITY END) - "+
										"(CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) - "+
										"(CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) > 0 "+
										"AND LOT_SL = ? ";

								sql = "SELECT COUNT(*) AS COUNT " +
										"FROM STOCK STOCK, ITEM ITEM, LOCATION LOCATION " +
										"WHERE STOCK.ITEM_CODE = ITEM.ITEM_CODE AND STOCK.LOC_CODE = LOCATION.LOC_CODE "+
										"AND ((CASE WHEN STOCK.QUANTITY IS NULL THEN 0 ELSE STOCK.QUANTITY END) - (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END)) > 0 "+
										" AND STOCK.LOC_CODE = ?   AND LOT_SL = ? ";
								//End changes by gulzar on 1/12/2012

								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, locCode);
								pstmt.setString(2, lpnNo);
								rs = pstmt.executeQuery();
								if ( rs.next() )
								{
									count = rs.getInt("COUNT");
								}
								rs.close(); rs = null;
								pstmt.close(); pstmt = null;
								if ( count == 0 )
								{
									// 26/10/11 manoharan all the errors should be returned so that warnings can be processed
									//errString = getErrorString("lpn_no","LPNNOTEXT",userId);
									//break;
									errList.add( "LPNNOTEXT" );
									errFields.add( childNodeName.toLowerCase() );
									// end 26/10/11 manoharan all the errors should be returned so that warnings can be processed
								}
							}
							//changed by sankara on 13-02-14 shholud not allow stock transfer if repl is pending start.
							siteCode = genericUtility.getColumnValue("site_code", allDom, "1" );
							System.out.println("siteCode:::::::"+siteCode);
							sql = " SELECT COUNT(DISTINCT D.REPL_ORDER) AS COUNT FROM REPL_ORD_DET D, STOCK S, WAVE_TASK W, WAVE_TASK_DET K WHERE D.REPL_ORDER = K.REF_ID AND W.WAVE_ID = K.WAVE_ID " +
								  " AND W.CANCEL <> 'Y' AND K.STATUS <> 'Y' AND D.CANCEL_MODE IS NULL AND D.QUANTITY > 0 AND D.SITE_CODE = ? AND D.ITEM_CODE = S.ITEM_CODE " +
								  " AND D.LOC_CODE = ? AND D.LOT_NO = S.LOT_NO AND D.LOT_SL = ? " ;
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, siteCode );
							pstmt.setString( 2, locCode );
							pstmt.setString( 3, lpnNo );
							rs = pstmt.executeQuery();
							if( rs.next() )       
							{
								count = rs.getInt("COUNT");
								System.out.println("count:"+count);								
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( count > 0 )
							{
								errList.add( "VTREPLPEND" );
								errFields.add( childNodeName.toLowerCase() );
							}
							//changed by sankara on 13-02-14 shholud not allow stock transfer if repl is pending end.
						} */
				}// for loop end
			}//case 2 end
			break;
			case 3:
			{
				System.out.println("Case 3 Validation");
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for ( int ctr = 0; ctr < noOfChilds; ctr++ )  //Loop for each node of current detail
				{	
					childNode = childList.item( ctr );
					if( childNode.getNodeType() != Node.ELEMENT_NODE )
					{
						continue;
					}
					childNodeName = childNode.getNodeName();
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
					if( "loc_code__to".equalsIgnoreCase( childNodeName ) )
					{
						
						
						/*Added By Dipak On 4 June 2012 Start*/
						
						 //changed by sankara on 22/09/14 not reuired in case3 start.
					/*	activePickInvstat = distCommon.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn);
						casePickInvstat = distCommon.getDisparams("999999","CASE_PICK_INVSTAT",conn);
						resrvLocInvstat = distCommon.getDisparams("999999","RESERV_LOCATION",conn);

						invStatList.add(activePickInvstat);
						invStatList.add(casePickInvstat);
						invStatList.add(resrvLocInvstat);
						Added By Dipak On 4 June 2012 End

						itemCode = genericUtility.getColumnValue("item_code", allDom, "3" );
						siteCode = genericUtility.getColumnValue("site_code", allDom, "1" );
						locCodeTo = checkNull(genericUtility.getColumnValue("loc_code__to", allDom, "3" ));
						lotNo = genericUtility.getColumnValue("lot_no__fr", allDom, "3" );
						lpnNo = genericUtility.getColumnValue("lot_sl__fr", allDom, "3" );
						locCode = genericUtility.getColumnValue("loc_code__fr", allDom, "2" );

						sql ="SELECT COUNT(*) AS LOCCOUNT FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? " +
								" AND LOT_NO = ? AND LOT_SL = ? AND EXP_DATE <= fn_sysdate()";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, siteCode );
						pstmt.setString( 3, locCode );
						pstmt.setString( 4, lotNo );
						pstmt.setString( 5, lpnNo );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							locCount = rs.getInt("LOCCOUNT");

						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs =null;

						if(locCount > 0)
						{
							locInvList = distCommon.getDisparams("999999","NOT_TRANF_INV_STAT",conn);
							System.out.println("locInvList>>>>>>"+locInvList);
							if((!"".equalsIgnoreCase(locInvList.trim()) || locInvList != null) && locInvList.trim().length() > 0 )
							{	
								locList = locInvList.split(",");

								sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ?";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, locCodeTo );
								rs = pstmt.executeQuery();
								if( rs.next() )       
								{

									invStatLoc = checkNull(rs.getString("INV_STAT"));	
								}

								for(int i =0; i < locList.length ; i++)
								{	  
									System.out.println("Comparing values"+invStatLoc+"locList[i]"+locList[i]);
									if(invStatLoc.trim().equalsIgnoreCase(locList[i].trim()))
									{
										errList.add( "VTLOCNTVAL" );
										errFields.add( childNodeName.toLowerCase());
										isLocToValid = false;
										break;
									}
								}
							}
						}
						System.out.println("isLocToValid["+isLocToValid+"]");
						if(isLocToValid)
						{
							sql = "SELECT SINGLE_LOT_SL, ITEM_LOT_OPT, INV_STAT FROM LOCATION WHERE LOC_CODE = ?";//Changes are done by Dipak On 4 June 2012
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, locCodeTo );
							rs = pstmt.executeQuery();
							if( rs.next() )       
							{
								singleLotSl = checkNull(rs.getString("SINGLE_LOT_SL"));
								itemLotOpt = checkNull(rs.getString("ITEM_LOT_OPT"));
								invStat = checkNull(rs.getString("INV_STAT"));//Added By Dipak On 4 June 2012
							}
							else
							{
								errList.add( "VMLOC6" );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;

							if( singleLotSl.trim().equalsIgnoreCase("Y") )
							{
								sql = "SELECT QUANTITY FROM STOCK WHERE SITE_CODE = ?"
										+ " AND LOC_CODE = ? ORDER BY QUANTITY DESC";

								pstmt = conn.prepareStatement( sql );

								pstmt.setString( 1, siteCode );
								pstmt.setString( 2, locCodeTo );
								rs = pstmt.executeQuery();	
								if( rs.next() )
								{
									quantity = rs.getDouble("QUANTITY");
								}
								rs.close(); rs = null;
								pstmt.close(); pstmt = null;

								if( quantity != 0 )
								{
									errList.add( "LOCNOTEMPT" );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							else
							{
								if( itemLotOpt.trim().equals("0") )  
								{
									sql = "SELECT COUNT(*) AS COUNT FROM STOCK WHERE (ITEM_CODE <> ? OR LOT_NO <> ?) AND SITE_CODE = ? AND LOC_CODE = ? AND QUANTITY > 0"; 
									pstmt = conn.prepareStatement( sql );
									pstmt.setString( 1, itemCode );
									pstmt.setString( 2, lotNo );
									pstmt.setString( 3, siteCode );
									pstmt.setString( 4, locCodeTo );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt("COUNT");
									}
									rs.close(); rs = null;
									pstmt.close(); pstmt = null;

									if( count > 0 )
									{
										errList.add( "VTLOCCODE2" );
										errFields.add( childNodeName.toLowerCase() );
									}	
								}
								else if( itemLotOpt.trim().equals("1") )  
								{

									sql = "SELECT COUNT(*) AS COUNT FROM STOCK WHERE  ITEM_CODE <> ? AND SITE_CODE = ? AND LOC_CODE = ? AND QUANTITY > 0";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString( 1, itemCode );
									pstmt.setString( 2, siteCode );
									pstmt.setString( 3, locCodeTo );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt("COUNT");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( count > 0 )
									{
										errList.add( "VTLOCCODE4" );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}
						//changed by sankara on 09-07-13 validation for nosl start.....	
						locCodeFrom = genericUtility.getColumnValue( "loc_code__fr", currDom );						
						sql = " SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeFrom );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locFrominvStat = rs.getString("INV_STAT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						NoslInvstat = distCommon.getDisparams("999999","INV_NO_SL",conn);
					 //Start Dhanraj Code Changes on 30-08-14 [W14DSUN001].
						//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....use only
						sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE= ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeTo );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locToinvStat = rs.getString("INV_STAT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....use only

						if( locFrominvStat.trim().equalsIgnoreCase(NoslInvstat) )
						{	
							//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....hide only
							/*sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE= ? ";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, locCodeTo );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								locToinvStat = rs.getString("INV_STAT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....hide only
							if(!locFrominvStat.equalsIgnoreCase(locToinvStat))
							{
								errList.add( "VTINVTRANF" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}*/
						//changed by sankara on 09-07-13 validation for nosl end.....	
						
						 //changed by sankara on 22/09/14 not reuired in case3 end.
						
						//Start changed by Dhanraj 0n 30-08-14 [W14DSUN001] for contro stock transfer using stock_transfer_type maste data.not allowed.same invstat and any invsta.
					    
					    System.out.println(" loc From invStat====> "+locFrominvStat+" locToinvStat====> "+locToinvStat);
					  /*  sql =" SELECT STT.TRAN_TYPE,STT.EXP_SEG,STT.INTER_LOC_XFRX_ALLOW,IST.INV_STAT FROM STOCK_TRANSFER_TYPE STT,LOCATION LOC,INVSTAT IST "
					    + " WHERE LOC.INV_STAT=IST.INV_STAT AND STT.TRAN_TYPE=IST.INV_STAT_TYPE AND LOC.LOC_CODE= ? ";*/
					  
					   /* sql= " SELECT STT.TRAN_TYPE,STT.EXP_SEG,STT.INTER_LOC_XFRX_ALLOW FROM STOCK_TRANSFER_TYPE STT,INVSTAT IST "
							+" WHERE  STT.TRAN_TYPE=IST.INV_STAT_TYPE AND IST.INV_STAT= ? ";
					    pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locFrominvStat );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							interLocXFrXAllow = rs.getString("INTER_LOC_XFRX_ALLOW");
							System.out.println(" InterLocXFrXAllow "+interLocXFrXAllow);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if("2".equalsIgnoreCase(interLocXFrXAllow))
						{
							System.out.println("Stock transfer allowed to any inv stat");
						}
						else if("0".equalsIgnoreCase(interLocXFrXAllow))
						{
							    //stock tranfer not allowed;
							System.out.println("Stock transfer not allowed ");
								errList.add( "VTTRANFNOT" );
								errFields.add( childNodeName.toLowerCase() );
							
						}
						else if("1".equalsIgnoreCase(interLocXFrXAllow))
						{
							if(!locFrominvStat.equalsIgnoreCase(locToinvStat))
							{
								//stock transfer allowed only same invstat.
								System.out.println("Stock transfer allowed to same inv stat ");
								errList.add( "VTTRANFASA" );
								errFields.add( childNodeName.toLowerCase());
							}
						}*/
						 //End changed by Dhanraj 0n 30-08-14 [W14DSUN001] for contro stock transfer using stock_transfer_type maste data.not allowed.same invstat and any invsta.
				    //End Dhanraj Code Changes on 30-08-14 [W14DSUN001] .
					} //End of case "loc_code__to"

					else if( "lot_sl__to".equalsIgnoreCase( childNodeName ) )//Case added by gulzar on 21/12/11
					{
						lpnNoTo = genericUtility.getColumnValue("lot_sl__to", currDom );
						if ( lpnNoTo == null || lpnNoTo.trim().length() == 0 )
						{
							errList.add( "NULLLPNNO" );
							errFields.add( childNodeName.toLowerCase() );
						}
						//changed by sankara on 19/08/14 for lpn alreay used not reuired start.
						/*sql = " SELECT LOT_SL FROM STOCK WHERE LOT_SL = ? AND SITE_CODE = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString(1, lpnNoTo );
						pstmt.setString(2, siteCode );
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							errList.add( "DUPLOTSLNW" );
							errFields.add( childNodeName.toLowerCase() );
						}*/		
						//changed by sankara on 19/08/14 for lpn alreay used not required end.
					}
					else if( "quantity".equalsIgnoreCase( childNodeName ) )
					{
						itemCode = genericUtility.getColumnValue("item_code", allDom, "3" );
						siteCode = genericUtility.getColumnValue("site_code", allDom, "1" );
						locCode = genericUtility.getColumnValue("loc_code__fr", allDom, "3" );
						lotNo = genericUtility.getColumnValue("lot_no__fr", allDom, "3" );
						lpnNo = genericUtility.getColumnValue("lot_sl__fr", allDom, "3" );
						String sQuantity = genericUtility.getColumnValue("quantity", allDom, "3" );
						String sNoArt = genericUtility.getColumnValue("no_art", allDom, "3" );
						if (sQuantity == null || sQuantity.trim().length() == 0)
						{
							sQuantity = "0";
						}
						if (sNoArt == null || sNoArt.trim().length() == 0)
						{
							sNoArt = "0";
						}
						quantity = Double.parseDouble(sQuantity);
						int noArt = Integer.parseInt(sNoArt);
						int stkNoArt =0;
						double stkQuantity = 0;

						sql = "SELECT shipper_size FROM item_lot_packsize "
								+ " WHERE ITEM_CODE = ? "
								+ " AND LOT_NO__FROM <= ? "
								+ " AND LOT_NO__TO >= ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, lotNo );
						pstmt.setString( 3, lotNo );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							shipperSize = rs.getDouble(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "SELECT (quantity - case when alloc_qty is null then 0 else alloc_qty end)  as quantity,no_art FROM stock "
								+ " WHERE ITEM_CODE = ? "
								+ " AND SITE_CODE = ? "
								+ " AND LOC_CODE = ? "
								+ " AND LOT_NO = ? "
								+ " AND LOT_SL = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, siteCode );
						pstmt.setString( 3, locCode );
						pstmt.setString( 4, lotNo );
						pstmt.setString( 5, lpnNo );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							stkQuantity = rs.getDouble(1);
							stkNoArt = rs.getInt(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						if (noArt > 0 )
						{
							quantity = shipperSize * noArt;
						}
						else if (noArt == 0  && quantity == 0) 
						{
							quantity = stkQuantity;
						}
						sql = "SELECT CASE WHEN PHY_HAN_BASIS IS NULL THEN ' ' ELSE PHY_HAN_BASIS END AS PHY_HAN_BASIS FROM LOCATION WHERE LOC_CODE = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCode );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							phyHanBasis = rs.getString("PHY_HAN_BASIS");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						//changed by sankara on 19/08/14 for lpn alreay used not reuired start.
						/*String stockQuantity = "";
						sql = " SELECT QUANTITY FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? " ;			
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, siteCode );
						pstmt.setString( 3, locCode );
						pstmt.setString( 4, lotNo );
						pstmt.setString( 5, lpnNo );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							stockQuantity = rs.getString(1);
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if(!sQuantity.equalsIgnoreCase(stockQuantity.trim() ) )
						{
							errList.add( "INVQUANTIY" );
							errFields.add( childNodeName.toLowerCase() );
						}*/
						//changed by sankara on 19/08/14 for lpn alreay used not reuired end.
						
					}	
				} //End of inner for loop
			} //case3 end
			break;
			case 4 :
			{
				System.out.println("Case 2 Validation");
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++)
				{	
					childNode = childList.item( ctr );
					if( childNode.getNodeType() != Node.ELEMENT_NODE )
					{
						continue;
					}
					childNodeName = childNode.getNodeName();						
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");

					if ( "loc_code__to".equalsIgnoreCase( childNodeName ) )
					{	
						locCode = genericUtility.getColumnValue( "loc_code__to", currDom );
						
						if (locCode == null || locCode.trim().length() == 0 )
						{

							errList.add( "VMLOC4" );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM LOCATION WHERE LOC_CODE = ? ";

							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
							rs = pstmt.executeQuery();
							if ( rs.next() )
							{
								count = rs.getInt("COUNT");
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if ( count == 0 )
							{
								errList.add( "VMLOC6" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						siteCode = genericUtility.getColumnValue("site_code", allDom, "1" );
						System.out.println("siteCode::::::"+siteCode);
						System.out.println("locCode::::::"+locCode);
						//Changed By Pragyan 11-AUG-14 To check and Facility Master implementation.start
						if(!commonWmsUtility1.isValFacLocation(locCode, siteCode, conn))
						{
							System.out.println("indise facility........");
							errList.add( "VMFACI2" ); // 'Diffrent Loc Code from location and site' (set same error Code @base)
							errFields.add( childNodeName.toLowerCase() );
							break;
						}
						//Changed By Pragyan 11-AUG-14 To check and Facility Master implementation.end
						
						//Start Changed by Dhanraj 03-09-14 [W14DSUN001]
						
						//changed by sankara on 22/09/14 commented as per pragyan start.
						
					/*	locCodeFrom = genericUtility.getColumnValue( "loc_code__fr", currDom );
						
						sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE= ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCode ); //To Locaton_Code
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locToinvStat = rs.getString("INV_STAT"); //To InvStat
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = " SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeFrom );//from Location_code
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locFrominvStat = rs.getString("INV_STAT"); //from invstat
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						 System.out.println(" loc From location====> "+locCodeFrom+" loc location to====> "+locCode);
						 
						 System.out.println(" loc From invStat====> "+locFrominvStat+" locToinvStat====> "+locToinvStat);
						
						sql= " SELECT STT.TRAN_TYPE,STT.EXP_SEG,STT.INTER_LOC_XFRX_ALLOW FROM STOCK_TRANSFER_TYPE STT,INVSTAT IST "
								+" WHERE  STT.TRAN_TYPE=IST.INV_STAT_TYPE AND IST.INV_STAT= ? ";
						    pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, locFrominvStat );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								interLocXFrXAllow = rs.getString("INTER_LOC_XFRX_ALLOW");
								System.out.println(" InterLocXFrXAllow "+interLocXFrXAllow);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if("2".equalsIgnoreCase(interLocXFrXAllow))
							{
								System.out.println("Stock transfer allowed to any inv stat");
							}
							else if("0".equalsIgnoreCase(interLocXFrXAllow))
							{
								    //stock tranfer not allowed;
								System.out.println("Stock transfer not allowed ");
									errList.add( "VTTRANFNOT" );
									errFields.add( childNodeName.toLowerCase() );
								
							}
							else if("1".equalsIgnoreCase(interLocXFrXAllow))
							{
								if(!locFrominvStat.equalsIgnoreCase(locToinvStat))
								{
									//stock transfer allowed only same invstat.
									System.out.println("Stock transfer allowed to same inv stat ");
									errList.add( "VTTRANFASA" );
									errFields.add( childNodeName.toLowerCase());
								}
							}*/
						/*Added By Dipak On 4 June 2012 Start*/
						activePickInvstat = distCommon.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn);
						casePickInvstat = distCommon.getDisparams("999999","CASE_PICK_INVSTAT",conn);
						resrvLocInvstat = distCommon.getDisparams("999999","RESERV_LOCATION",conn);

						invStatList.add(activePickInvstat);
						invStatList.add(casePickInvstat);
						invStatList.add(resrvLocInvstat);
						/*Added By Dipak On 4 June 2012 End*/

						itemCode = genericUtility.getColumnValue("item_code", allDom, "3" );
						siteCode = genericUtility.getColumnValue("site_code", allDom, "1" );
						locCodeTo = checkNull(genericUtility.getColumnValue("loc_code__to", allDom, "4" ));// Changed by Manoj dtd 23/12/14 Get value from Detail4
						lotNo = genericUtility.getColumnValue("lot_no__fr", allDom, "3" );
						lpnNo = genericUtility.getColumnValue("lot_sl__fr", allDom, "3" );
						locCode = genericUtility.getColumnValue("loc_code__fr", allDom, "2" );

						sql ="SELECT COUNT(*) AS LOCCOUNT FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? " +
								" AND LOT_NO = ? AND LOT_SL = ? AND EXP_DATE <= fn_sysdate()";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, siteCode );
						pstmt.setString( 3, locCode );
						pstmt.setString( 4, lotNo );
						pstmt.setString( 5, lpnNo );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							locCount = rs.getInt("LOCCOUNT");

						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs =null;

						if(locCount > 0)
						{
							locInvList = distCommon.getDisparams("999999","NOT_TRANF_INV_STAT",conn);
							System.out.println("locInvList>>>>>>"+locInvList);
							if((!"".equalsIgnoreCase(locInvList.trim()) || locInvList != null) && locInvList.trim().length() > 0 )
							{	
								locList = locInvList.split(",");

								sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ?";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, locCodeTo );
								rs = pstmt.executeQuery();
								if( rs.next() )       
								{

									invStatLoc = checkNull(rs.getString("INV_STAT"));	
								}

								for(int i =0; i < locList.length ; i++)
								{	  
									System.out.println("Comparing values"+invStatLoc+"locList[i]"+locList[i]);
									if(invStatLoc.trim().equalsIgnoreCase(locList[i].trim()))
									{
										errList.add( "VTLOCNTVAL" );
										errFields.add( childNodeName.toLowerCase());
										isLocToValid = false;
										break;
									}
								}
							}
						}
						System.out.println("isLocToValid["+isLocToValid+"]");
						if(isLocToValid)
						{
							sql = "SELECT SINGLE_LOT_SL, ITEM_LOT_OPT, INV_STAT FROM LOCATION WHERE LOC_CODE = ?";//Changes are done by Dipak On 4 June 2012
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, locCodeTo );
							rs = pstmt.executeQuery();
							if( rs.next() )       
							{
								singleLotSl = checkNull(rs.getString("SINGLE_LOT_SL"));
								itemLotOpt = checkNull(rs.getString("ITEM_LOT_OPT"));
								invStat = checkNull(rs.getString("INV_STAT"));//Added By Dipak On 4 June 2012
							}
							else
							{
								errList.add( "VMLOC6" );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
                            System.out.println("singleLotSl:::::::"+singleLotSl);                            
                            System.out.println("locCodeTo:::::::"+locCodeTo);
							if( singleLotSl.trim().equalsIgnoreCase("Y") )
							{
								sql = "SELECT QUANTITY FROM STOCK WHERE SITE_CODE = ?"
										+ " AND LOC_CODE = ? ORDER BY QUANTITY DESC";

								pstmt = conn.prepareStatement( sql );
								pstmt.setString( 1, siteCode );
								pstmt.setString( 2, locCodeTo );
								rs = pstmt.executeQuery();	
								if( rs.next() )
								{
									quantity = rs.getDouble("QUANTITY");
								}
								rs.close(); rs = null;
								pstmt.close(); pstmt = null;

								if( quantity != 0 )
								{
									errList.add( "LOCNOTEMPT" );
									errFields.add( childNodeName.toLowerCase() );
								}
							}
							else
							{
								if( itemLotOpt.trim().equals("0") )  
								{
									sql = "SELECT COUNT(*) AS COUNT FROM STOCK WHERE (ITEM_CODE <> ? OR LOT_NO <> ?) AND SITE_CODE = ? AND LOC_CODE = ? AND QUANTITY > 0"; 
									pstmt = conn.prepareStatement( sql );
									pstmt.setString( 1, itemCode );
									pstmt.setString( 2, lotNo );
									pstmt.setString( 3, siteCode );
									pstmt.setString( 4, locCodeTo );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt("COUNT");
									}
									rs.close(); rs = null;
									pstmt.close(); pstmt = null;

									if( count > 0 )
									{
										errList.add( "VTLOCCODE2" );
										errFields.add( childNodeName.toLowerCase() );
									}	
								}
								else if( itemLotOpt.trim().equals("1") )  
								{

									sql = "SELECT COUNT(*) AS COUNT FROM STOCK WHERE  ITEM_CODE <> ? AND SITE_CODE = ? AND LOC_CODE = ? AND QUANTITY > 0";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString( 1, itemCode );
									pstmt.setString( 2, siteCode );
									pstmt.setString( 3, locCodeTo );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										count = rs.getInt("COUNT");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( count > 0 )
									{
										errList.add( "VTLOCCODE4" );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}
						//changed by sankara on 09-07-13 validation for nosl start.....	
						locCodeFrom = genericUtility.getColumnValue( "loc_code__fr", currDom );						
						sql = " SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeFrom );//from Location_code
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locFrominvStat = rs.getString("INV_STAT"); //from invstat
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						NoslInvstat = distCommon.getDisparams("999999","INV_NO_SL",conn);
					 //Start Dhanraj Code Changes on 30-08-14 [W14DSUN001].
						//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....use only
						sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE= ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeTo );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							locToinvStat = rs.getString("INV_STAT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....use only

						if( locFrominvStat.trim().equalsIgnoreCase(NoslInvstat) )
						{	
							//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....hide only
							/*sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE= ? ";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, locCodeTo );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								locToinvStat = rs.getString("INV_STAT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;*/
							//Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get inv stat multiple time....hide only
							if(!locFrominvStat.equalsIgnoreCase(locToinvStat))
							{
								errList.add( "VTINVTRANF" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						//changed by sankara on 09-07-13 validation for nosl end.....	
						System.out.println(" loc From invStat====> "+locFrominvStat+" locToinvStat====> "+locToinvStat);
						//changed by sankara on 22/09/14 commented as per pragyan end.
						
						//changed by sankara on 22/09/14 for location not emty for different item start.
						itemCode = genericUtility.getColumnValue("item_code", allDom, "3" );
						lotNo = genericUtility.getColumnValue("lot_no__fr", allDom, "3" );	
						System.out.println("itemCode::::::"+itemCode);
						System.out.println("locCode::::::"+locCode);
						System.out.println("lotNo::::::"+lotNo);

						sql = " SELECT COUNT(*) AS COUNT FROM STOCK WHERE ITEM_CODE <> ? AND LOT_NO = ? AND LOC_CODE = ? AND QUANTITY > 0 ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, lotNo );
						pstmt.setString( 3, locCodeTo );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							count = rs.getInt("COUNT");
							System.out.println(" count:::::::::: "+count);
						}
						if(count > 0)
						{
							errList.add( "LOCNOTEMPT" );
							errFields.add( childNodeName.toLowerCase() );
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		
						
						//changed by sankara on 22/09/14 for location not emty for different item end.
						//End Changed by Dhanraj 03-09-14 [W14DSUN001]
						//Start added by chandrashekar on 03-APR-2015
						locCodeFrom = genericUtility.getColumnValue("loc_code__fr", allDom, "2" );
						System.out.println("locCodeFrom>>"+locCodeFrom);
						System.out.println("locCodeTo>>"+locCodeTo);
						sql = "select a.available as available , a.inv_stat as inv_stat_to from   invstat a, location b  where  a.inv_stat = b.inv_stat and    b.loc_code = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeTo );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							availableToLoc = rs.getString("available");
							invStateTo = rs.getString("inv_stat_to");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "select a.available as available from   invstat a, location b  where  a.inv_stat = b.inv_stat and    b.loc_code = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCodeFrom );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							availableFromLoc = rs.getString("available");
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						disparamInvstat = distCommon.getDisparams("999999","STK_XFRX_INVSTAT",conn);
						System.out.println("disparamInvstat>>>>>>>>>>>"+disparamInvstat);
						if (disparamInvstat == null || "NULLFOUND".equalsIgnoreCase(disparamInvstat) || disparamInvstat.trim().length() == 0)
						{
							disparamInvstat="";
						}
						if((availableFromLoc.trim().equalsIgnoreCase(availableToLoc.trim())))
						{
							System.out.println("nothing to do>>..");
						}
						else if(availableFromLoc.equalsIgnoreCase("Y"))
						{
							if((checkNull(invStateTo).trim().equalsIgnoreCase(disparamInvstat.trim())))
							{
								
							}else
							{	
								errList.add( "VMLOCSTAT" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						else if(availableFromLoc.equalsIgnoreCase("N"))
						{
							errList.add( "VMLOCSTAT" );
							errFields.add( childNodeName.toLowerCase() );
						}
						//End Start  by chandrashekar on 03-APR-2015
					}
				}// for loop end
			} //case 4 end
			} //switch end
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
		}//try block end
		catch(Exception e)
		{
			System.out.println("Exception in StockTransferMultipleIC  == >");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
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
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception :StockTransferMultipleIC:wfValData :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		return errString;
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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

	// Item Change Functionality. Start from here....		
	/**
	 * The public method is defined without any parameters and returns blank string
	 */
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}

	/**
	 * The public method is used for converting the current form data into a document(dom)
	 * The currDom is then given as argument to the overloaded function wfValData to perform validation
	 * Returns validation string if exists else returns null in XML format
	 * @param currFrmXmlStr contains the current form data in XML format
	 * @param hdrFrmXmlStr contains all the header information in the XML format
	 * @param allFrmXmlStr contains the data of all the forms in XML format
	 * @param objContext represents the form number
	 * @param currentColumn represents the value of current field.
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr, String allFrmXmlStr, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;

		String errString = null;

		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{
			if (currFrmXmlStr != null && currFrmXmlStr.trim().length()!=0)
			{
				currDom = genericUtility.parseString(currFrmXmlStr); 
			}
			if (hdrFrmXmlStr != null && hdrFrmXmlStr.trim().length()!=0)
			{
				hdrDom = genericUtility.parseString(hdrFrmXmlStr); 
			}
			if (allFrmXmlStr != null && allFrmXmlStr.trim().length()!=0)
			{
				allDom = genericUtility.parseString(allFrmXmlStr); 
			}
			errString = itemChanged( currDom, hdrDom, allDom, objContext, currentColumn, editFlag, xtraParams );
			System.out.println ( "ErrString :" + errString);
		}
		catch (Exception e)
		{
			System.out.println ( "Exception : StockTransferMultipleIC:defaul_ItemChanged(String,String):" + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println ( "returning from StockTransferMultipleIC default_Itemchanged" );
		return errString;
	}	

	/**
	 * The public overloaded method takes a document as input and is used for the validation of required fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currDom contains the current form data as a document object model
	 * @param hdrDom contains all the header information
	 * @param allDom contains the field data of all the forms 
	 * @param objContext represents form number
	 * @param currentColumn represents the current field 
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information
	 * 
	 */
	public String itemChanged( Document currDom, Document hdrDom, Document allDom, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		String retValue = "";
		try
		{
			retValue = default_ItemChanged( currDom, hdrDom, allDom,  objContext, currentColumn, editFlag, xtraParams );
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return retValue;

	}
	public String default_ItemChanged( Document currDom, Document hdrDom, Document allDom, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		System.out.println("Call stocktransfermultipleIc default_ItemChanged");
		String sql = "";
		String itemCode = "";
		String itemDescr = "";
		String tranId = "";		
		String tranDate = "";				
		String columnValue = "";		
		String refSerFor = "";
		String reasCode = "";
		String confirmed = "";
		String confDate = "";
		String siteCode = "";	
		String siteDescr = "";	
		String empCodeAprv = "";
		String fullName = "";
		String lpnNo = "";
		String lotSl = "";
		String noArt = "";
		String locDescr = "";
		String acctCode = "";
		String cctrCode = "";
		String lotNo = "";
		String locCode = "";
		String unit = "", phyHanBasis = "";
		String grlInvstat = "";	
		String locCodeTo = "";
		int currentFormNo = 0;
		int domID = 0;	
		double quantity = 0d; 
		ResultSet rs = null, rs1=null ;
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null ;		
		StringBuffer valueXmlString = new StringBuffer();		
		System.out.println("hello:");
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String invStat = ""; 
		String NoslInvstat = "";
		String locCodeSys = "";
		DistCommon distCommon = new DistCommon();
		NodeList parentNodeList = null;
		Node parentNode = null;
		int parentNodeListLength = 0;
		int ctr = 0, issLineNo = 0;
        String itemlocType = "";
    	int locZonePref = 0;
		try
		{
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );

			columnValue = genericUtility.getColumnValue( currentColumn, currDom );

			DateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String dbDateFormat = genericUtility.getDBDateFormat();
			String applDateFormat = genericUtility.getApplDateFormat();

			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			DistCommon dComm = new DistCommon();

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
				System.out.println("Call stocktransfermultipleIc item changed case 1");
				//Changed by wasim on 31-03-2017 for adding attributes in Detail XML
				//valueXmlString.append( "<Detail1 domID='1'>\r\n" );
				  valueXmlString.append("<Detail1 domID='1' objName=\"stocktransfer_multi_wiz\" selected=\"N\">");
				  valueXmlString.append( "<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>\r\n" );
				  
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{	
					java.util.Date currDate = new java.util.Date();
					SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
					String currDateStr = sdf.format(currDate);

					String defReasCode = dComm.getDisparams("999999","DEFAULT_REAS_CODE",conn);

					String loginCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));
					String chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" ));
					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));
					if ( siteCode.trim().length() == 0 || "INVALID_SITE".equalsIgnoreCase(siteCode.trim()) )
					{
						siteCode = getSiteCode( xtraParams, conn );
					}
					System.out.println("siteCode = ["+siteCode+"]");

					valueXmlString.append( "<tran_id/>" );
					valueXmlString.append( "<tran_date><![CDATA[" ).append( currDateStr ).append( "]]></tran_date>\r\n" );
					valueXmlString.append( "<ref_ser__for><![CDATA[" ).append( "XFRX" ).append( "]]></ref_ser__for>\r\n" );
					valueXmlString.append( "<site_code><![CDATA[" ).append(  checkNull ( siteCode)).append( "]]></site_code>\r\n" );

					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						siteDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
					valueXmlString.append( "<reas_code><![CDATA[" ).append(  checkNull ( defReasCode)).append( "]]></reas_code>\r\n" );
					valueXmlString.append( "<confirmed><![CDATA[" ).append("N").append( "]]></confirmed>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append( loginCode ).append( "]]></chg_user>\r\n" );
					valueXmlString.append( "<chg_date><![CDATA[" ).append( currDateStr ).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
				}
				valueXmlString.append("</Detail1>\r\n");
			}//case 1 end here
			break;
			case 2:
			{
				System.out.println("Call stocktransfermultipleIc item changed case 2");
				//Changed by wasim on 31-03-2017 for adding attributes in Detail XML
				//valueXmlString.append( "<Detail2 domID='1'>\r\n" );
				valueXmlString.append( "<Detail2 domID='"+1+"' objContext = '"+objContext+"' objName=\"stocktransfer_multi_wiz\" selected=\"Y\">\r\n" );
				valueXmlString.append( "<attribute selected=\"Y\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>\r\n" );
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{	
					java.util.Date currDate = new java.util.Date();
					SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
					String currDateStr = sdf.format(currDate);
					String defReasCode = dComm.getDisparams("999999","DEFAULT_REAS_CODE",conn);
					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));
					locCode = checkNull(genericUtility.getColumnValue("loc_code__fr", allDom, "2"));
					System.out.println("locCode::::::::"+locCode);
					valueXmlString.append( "<loc_code><![CDATA[" ).append(  checkNull ( locCode)).append( "]]></loc_code>\r\n" );

				}
				valueXmlString.append("</Detail2>\r\n");
			} //case 1 end here
			break;
			case 3:
			{
				System.out.println("Call stocktransfermultipleIc item changed case 3");
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{
                    int lineNo = 0;
					siteCode = checkNull(genericUtility.getColumnValue("site_code", allDom, "1"));
					locCode = checkNull(genericUtility.getColumnValue("loc_code__fr", allDom, "2"));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", allDom, "3"));
					
					// Start Changed by Dhanraj on 20-SEP-14
					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));
					if ( siteCode.trim().length() == 0 || "INVALID_SITE".equalsIgnoreCase(siteCode.trim()) )
					{
						siteCode = getSiteCode( xtraParams, conn );
					}
					// Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get to inv stat use..
					String tranTypeDesc="";
					/*sql = " SELECT INV_STAT_TYPE,STT.DESCR FROM LOCATION LOC,INVSTAT INV,STOCK_TRANSFER_TYPE STT " 
					      +" WHERE LOC.INV_STAT=INV.INV_STAT AND STT.TRAN_TYPE=INV.INV_STAT_TYPE AND LOC_CODE= ? "; 
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, locCode );
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						 tranTypeDesc=rs.getString("INV_STAT_TYPE")+" - "+rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;*/
				   //End Changed by Dhanraj on 30-08-14 [W14DSUN001] for get to inv stat use..
					
					sql = " SELECT A.ITEM_CODE, A.SITE_CODE, A.LOC_CODE, A.LOT_NO, A.LOT_SL," +
							" C.ITEM_SER, C.DESCR ITEM_DESCR, C.LOC_TYPE, C.LOC_ZONE__PREF, A.QUANTITY, "+
							" D.DESCR LOC_DESCR, A.PACK_CODE, A.ALLOC_QTY, A.MFG_DATE, A.NO_ART, " +
							//changed by sankara on 22/09/14 added unit as per ashish sir.
							//" A.ACCT_CODE__INV, A.CCTR_CODE__INV, D.PHY_HAN_BASIS "+
							" A.ACCT_CODE__INV, A.CCTR_CODE__INV, D.PHY_HAN_BASIS, A.UNIT "+
							" FROM STOCK A,ITEM C, LOCATION D "+
							" WHERE A.LOC_CODE = D.LOC_CODE " +
							" AND A.ITEM_CODE = C.ITEM_CODE " +
							" AND A.QUANTITY > 0 " +
							" AND D.LOC_CODE = ? " +
							//Chaned by Dhanraj for stock pass site_code on 20-SEP-14         
							" AND A.SITE_CODE = ? ";
					pstmt =  conn.prepareStatement(sql);		
					pstmt.setString(1,locCode );
					//Chaned by Dhanraj for stock pass site_code on 20-SEP-14    
					pstmt.setString(2,siteCode );
					rs = pstmt.executeQuery(); 
					
					while (rs.next())
					{
						itemCode = rs.getString("ITEM_CODE");                     
						lotNo = rs.getString("LOT_NO");
						lotSl = rs.getString("LOT_SL");
						quantity = rs.getDouble("QUANTITY");
						noArt = rs.getString("NO_ART");
						itemDescr = rs.getString("ITEM_DESCR");
						locDescr = rs.getString("LOC_DESCR");
						acctCode = rs.getString("ACCT_CODE__INV");
						cctrCode = rs.getString("CCTR_CODE__INV");
						itemlocType = rs.getString("LOC_TYPE"); 
						locZonePref = rs.getInt("LOC_ZONE__PREF");
						unit = rs.getString("UNIT");
						//phyHanBasis = rs.getString("PHY_HAN_BASIS");
						domID++;
						lineNo++;
						System.out.println("lineNo3::::::::"+lineNo);
						//Changed by wasim on 31-03-2017 for adding attributes in Detail XML
						//valueXmlString.append( "<Detail3 domID='"+ domID +"' selected=\"N\">\r\n" );
						valueXmlString.append( "<Detail3 domID='"+domID+"' objContext = '"+objContext+"' objName=\"stocktransfer_multi_wiz\" selected=\"N\">\r\n" );
						valueXmlString.append( "<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>\r\n" );
						
						valueXmlString.append("<tran_id/>\r\n");
						valueXmlString.append("<line_no><![CDATA[").append(lineNo).append("]]></line_no>\r\n");
						valueXmlString.append("<item_code><![CDATA[").append(checkNull(itemCode.trim())).append("]]></item_code>\r\n");
						valueXmlString.append("<item_descr><![CDATA[").append(checkNull(itemDescr)).append("]]></item_descr>\r\n");
						valueXmlString.append("<loc_code__fr><![CDATA[").append(checkNull(locCode.trim())).append("]]></loc_code__fr>\r\n");
						valueXmlString.append("<loc_descr__fr><![CDATA[").append(checkNull(locDescr)).append("]]></loc_descr__fr>\r\n");
						valueXmlString.append("<loc_code__to/>\r\n");
						valueXmlString.append("<lot_no__fr><![CDATA[").append(checkNull(lotNo.trim())).append("]]></lot_no__fr>\r\n");
						valueXmlString.append("<lot_no__to><![CDATA[").append(checkNull(lotNo.trim())).append("]]></lot_no__to>\r\n");								
						valueXmlString.append("<lot_sl__fr><![CDATA[").append(checkNull(lotSl.trim())).append("]]></lot_sl__fr>\r\n");
						valueXmlString.append("<lot_sl__to><![CDATA[").append(checkNull(lotSl.trim())).append("]]></lot_sl__to>\r\n");				
						valueXmlString.append("<quantity><![CDATA[" + quantity + "]]></quantity>\r\n");
						valueXmlString.append("<no_art><![CDATA[" + noArt + "]]></no_art>\r\n");					
						valueXmlString.append("<unit><![CDATA[").append(unit).append("]]></unit>\r\n");
						valueXmlString.append("<acct_code__cr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__cr>\r\n");
						valueXmlString.append("<cctr_code__cr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__cr>\r\n");
						valueXmlString.append("<acct_code__dr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__dr>\r\n");
						valueXmlString.append("<cctr_code__dr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__dr>\r\n");								
						valueXmlString.append("<phy_han_basis><![CDATA[").append(checkNull(phyHanBasis)).append("]]></phy_han_basis>\r\n");
						
						sql =   " SELECT DISTINCT A.LOC_CODE, S.QUANTITY FROM LOCATION A ,INVSTAT B, STOCK S " +
								" WHERE A.INV_STAT = B.INV_STAT AND B.AVAILABLE = 'Y' " +
								" AND (CASE WHEN B.STAT_TYPE IS NULL THEN ' ' ELSE B.STAT_TYPE END) <> 'S' " +
								//changed by sankara on 22/09/14 for location not emty for sufggest loc code
								//" AND A.LOC_CODE = S.LOC_CODE AND A.LOC_CODE <> ? AND S.ITEM_CODE = ? AND S.LOT_NO = ? AND S.SITE_CODE = ? AND S.QUANTITY <= 0 "; 					   
								" AND A.LOC_CODE = S.LOC_CODE AND A.LOC_CODE <> ? AND S.ITEM_CODE = ? AND S.LOT_NO = ? AND S.SITE_CODE = ? "
								//Changed by Dadaso pawar on 20/01/15 [Start]
								+" AND (CASE WHEN S.QUANTITY IS NULL THEN 0 ELSE S.QUANTITY END) > 0 "
								//Changed by Dadaso pawar on 20/01/15 [End]
								//Changed by Dadaso pawar on 03/02/15 [START]	
								//+ "ORDER BY S.QUANTITY DESC ";	
								+ "ORDER BY A.LOC_CODE ";
								//Changed by Dadaso pawar on 03/02/15 [End]
						pstmt1 =  conn.prepareStatement(sql);	
						pstmt1.setString(1,locCode );
						pstmt1.setString(2,itemCode );
						pstmt1.setString(3,lotNo );
						pstmt1.setString(4,siteCode );
						rs1 = pstmt1.executeQuery();          
						if (rs1.next())
						{
							locCodeTo = checkNull(rs1.getString("LOC_CODE"));
							System.out.println("suglocCodeTo:::::::::"+locCodeTo);
						}
					    if ( locCodeTo == null || locCodeTo.trim().length() == 0 )
						{
							System.out.println("inside else if..........................................");
							sql =   " SELECT DISTINCT A.LOC_CODE, S.QUANTITY FROM LOCATION A ,INVSTAT B, STOCK S " +
									" WHERE A.INV_STAT = B.INV_STAT AND B.AVAILABLE = 'Y' " +
									" AND (CASE WHEN B.STAT_TYPE IS NULL THEN ' ' ELSE B.STAT_TYPE END) <> 'S' " +
									//changed by sankara on 22/09/14 for location not emty for sufggest loc code
									//" AND A.LOC_CODE = S.LOC_CODE AND A.LOC_CODE <> ? AND S.ITEM_CODE = ? AND S.LOT_NO <> ? AND S.SITE_CODE = ? AND S.QUANTITY <= 0 "; 					   
									  " AND A.LOC_CODE = S.LOC_CODE AND A.LOC_CODE <> ? AND S.ITEM_CODE = ? AND S.LOT_NO <> ? AND S.SITE_CODE = ?  "
									//Changed by Dadaso pawar on 20/01/15 [Start]
									+" AND (CASE WHEN S.QUANTITY IS NULL THEN 0 ELSE S.QUANTITY END) > 0 "
									//Changed by Dadaso pawar on 20/01/15 [End]	
							     //Changed by Dadaso pawar on 03/02/15 [START]	
								//+ "ORDER BY S.QUANTITY DESC ";	
									+ "ORDER BY A.LOC_CODE ";
								//Changed by Dadaso pawar on 03/02/15 [End]
							pstmt1 =  conn.prepareStatement(sql);	
							pstmt1.setString(1,locCode );
							pstmt1.setString(2,itemCode );
							pstmt1.setString(3,lotNo );
							pstmt1.setString(4,siteCode );
							rs1 = pstmt1.executeQuery();          
							if (rs1.next())
							{
								locCodeTo = checkNull(rs1.getString("LOC_CODE"));
								System.out.println("else if suglocCodeTo:::::::::"+locCodeTo);
							}
						}
					    if ( locCodeTo == null || locCodeTo.trim().length() == 0 )
						{
							System.out.println("inside else ..........................................");
						    locCodeTo = getAvilablePickLocation( conn, itemlocType, locZonePref, locCode, itemCode, siteCode, lotNo, lpnNo);
						    System.out.println("else suglocCodeTo:::::::::"+locCodeTo);
						}
						rs1.close();rs1 = null;						
						pstmt1.close();pstmt1 = null;		

						valueXmlString.append("<loc_code__to><![CDATA[").append(checkNull(locCodeTo.trim())).append("]]></loc_code__to>\r\n");
						valueXmlString.append("<loc_code__sys><![CDATA[").append(checkNull(locCodeTo.trim())).append("]]></loc_code__sys>\r\n");
						//Changed by Dhanraj on 30-08-14 [W14DSUN001] add tab(inv_stat_type) for tran type with desc disp.
						valueXmlString.append("<transaction_type ><![CDATA[").append(checkNull(tranTypeDesc)).append("]]></transaction_type>\r\n");
						valueXmlString.append("</Detail3>\r\n");
					}
					rs.close();rs = null;						
					pstmt.close();pstmt = null;		
				}	
			} // case 3
			break; 
			case 4: 
			{
				System.out.println("Call stocktransfermultipleIc item changed case 4");
				int newInt = 0;
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					parentNodeList = allDom.getElementsByTagName("Detail3");
					parentNodeListLength = parentNodeList.getLength();
                    System.out.println("parentNodeListLength:::::::::::;;;"+parentNodeListLength);
					for(ctr = 0; ctr < parentNodeListLength ; ctr++ )
					{
						System.out.println("TEST LENGTH="+parentNodeListLength);
						parentNode = parentNodeList.item(ctr);
						Element  parentNode1 = (Element)parentNodeList.item(ctr);					
						String selected = getAttribValue(parentNode, "attribute", "selected");
						System.out.println("status ="+getAttribValue(parentNode, "attribute", "status"));
						newInt++;
						locCodeTo = checkNull(genericUtility.getColumnValue("loc_code__to", allDom, "4"));
						System.out.println("locCodeTodom4::::::"+locCodeTo);

						tranId = genericUtility.getColumnValueFromNode("tran_id", parentNode) ;
						int lineNo = Integer.parseInt(genericUtility.getColumnValueFromNode("line_no", parentNode).trim());	
						System.out.println("lineNo4::::::::"+lineNo);
						itemCode =  genericUtility.getColumnValueFromNode("item_code", parentNode);
						itemDescr = genericUtility.getColumnValueFromNode("item_descr", parentNode);
						siteCode = genericUtility.getColumnValueFromNode("site_code", parentNode);
						siteDescr = genericUtility.getColumnValueFromNode("site_descr", parentNode);
						locCode = genericUtility.getColumnValueFromNode("loc_code__fr", parentNode);
						//locCodeTo = genericUtility.getColumnValueFromNode("loc_code__to", parentNode);
						locCodeSys = genericUtility.getColumnValueFromNode("loc_code__sys", parentNode);
						lotNo = genericUtility.getColumnValueFromNode("lot_no__fr", parentNode);
						lotSl = genericUtility.getColumnValueFromNode("lot_sl__fr", parentNode);
						String lotSlNew = genericUtility.getColumnValueFromNode("lot_sl__to", parentNode);
						noArt =  genericUtility.getColumnValueFromNode("no_art", parentNode); 
						String Quantity = genericUtility.getColumnValueFromNode("quantity", parentNode); 
						//changed by sankara for unit 22/09/14
						unit = genericUtility.getColumnValueFromNode("unit", parentNode); 
						System.out.println("itemCode ----- ["+itemCode+"]");
						acctCode = genericUtility.getColumnValueFromNode("acct_code__cr", parentNode);		
						cctrCode = genericUtility.getColumnValueFromNode("cctr_code__cr", parentNode);
						acctCode = genericUtility.getColumnValueFromNode("acct_code__dr", parentNode);
						cctrCode = genericUtility.getColumnValueFromNode("cctr_code__dr", parentNode);
						
						valueXmlString.append( "<Detail4 domID='"+(newInt)+"' objContext = '"+currentFormNo+"' selected=\"N\">\r\n" );
						valueXmlString.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\" />\r\n");
						valueXmlString.append( "<tran_id/>" );
						valueXmlString.append( "<loc_code__to><![CDATA[" ).append(locCodeTo).append( "]]></loc_code__to>\r\n" );
						valueXmlString.append( "<line_no><![CDATA[" ).append( lineNo ).append( "]]></line_no>\r\n" );
						valueXmlString.append( "<item_code><![CDATA[" ).append( itemCode ).append( "]]></item_code>\r\n" );
						valueXmlString.append( "<item_descr><![CDATA[" ).append( itemDescr ).append( "]]></item_descr>\r\n" );
						valueXmlString.append( "<site_code><![CDATA[" ).append( siteCode ).append( "]]></site_code>\r\n" );
						valueXmlString.append( "<site_descr><![CDATA[" ).append( siteDescr ).append( "]]></site_descr>\r\n" );
						valueXmlString.append("<loc_code__fr><![CDATA[").append(checkNull(locCode.trim())).append("]]></loc_code__fr>\r\n");
						valueXmlString.append("<loc_code__sys><![CDATA[").append(checkNull(locCodeSys.trim())).append("]]></loc_code__sys>\r\n");
						valueXmlString.append("<lot_no__fr><![CDATA[").append(checkNull(lotNo.trim())).append("]]></lot_no__fr>\r\n");
						valueXmlString.append("<lot_no__to><![CDATA[").append(checkNull(lotNo.trim())).append("]]></lot_no__to>\r\n");								
						valueXmlString.append("<lot_sl__fr><![CDATA[").append(checkNull(lotSl.trim())).append("]]></lot_sl__fr>\r\n");
						valueXmlString.append("<lot_sl__to><![CDATA[").append(lotSlNew.trim()).append("]]></lot_sl__to>\r\n");
						valueXmlString.append("<quantity><![CDATA[" + Quantity + "]]></quantity>\r\n");
						valueXmlString.append("<no_art><![CDATA[" + noArt + "]]></no_art>\r\n");	
						//changed by sankara for unit 22/09/14
						valueXmlString.append("<unit><![CDATA[").append(unit).append("]]></unit>\r\n");
						valueXmlString.append("<acct_code__cr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__cr>\r\n");
						valueXmlString.append("<cctr_code__cr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__cr>\r\n");
						valueXmlString.append("<acct_code__dr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__dr>\r\n");
						valueXmlString.append("<cctr_code__dr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__dr>\r\n");
						
						 
						// Start Changed by Dhanraj on 30-08-14 [W14DSUN001] for get to inv stat use..
						String tranTypeDesc="";
					/*	sql = " SELECT INV_STAT_TYPE,STT.DESCR FROM LOCATION LOC,INVSTAT INV,STOCK_TRANSFER_TYPE STT " 
						      +" WHERE LOC.INV_STAT=INV.INV_STAT AND STT.TRAN_TYPE=INV.INV_STAT_TYPE AND LOC_CODE= ? "; 
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, locCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							 tranTypeDesc=rs.getString("INV_STAT_TYPE")+" - "+rs.getString("DESCR");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;*/
					        //Changed by Dhanraj on 30-08-14 [W14DSUN001] add tab(inv_stat_type) for tran type with desc disp.
						valueXmlString.append("<transaction_type ><![CDATA[").append(checkNull(tranTypeDesc)).append("]]></transaction_type>\r\n");
					   //End Changed by Dhanraj on 30-08-14 [W14DSUN001] for get to inv stat use..
						valueXmlString.append("</Detail4>\r\n");
					}
				}
				/*String updatedDetailDomStr = addSelectedRecToDom( allDom, valueXmlString.toString() );
				valueXmlString = null;
				valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>").append( editFlag );
				valueXmlString.append("</editFlag>\r\n</Header>\r\n");
				updatedDetailDomStr = updatedDetailDomStr.substring(updatedDetailDomStr.indexOf("<Root>") + "<Root>".length(), updatedDetailDomStr.indexOf("</Root>") );
				valueXmlString.append(updatedDetailDomStr);
				System.out.println("\n*******sumit*******************\n");
				System.out.println("updatedDetailDomStr[4]:>>>>>>>>>>> "+updatedDetailDomStr);	*/
			}


			} //switch  end
		}
		catch(Exception e)
		{
			System.out.println( "Exception :StockTransferMultipleIC :default_ItemChanged(Document,String):" + e.getMessage() + ":" );
			valueXmlString = valueXmlString.append( genericUtility.createErrorString( e ) );
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
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
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception :StockTransferMultipleIC:default_ItemChanged :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append( "</Root>\r\n" );	
		System.out.println( "\n****ValueXmlString :" + valueXmlString.toString() + ":********" );
		return valueXmlString.toString();
	}

	private Object checkNullAndTrim(String locCodeSys) {
		// TODO Auto-generated method stub
		return null;
	}

	private String checkNull( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		return inputVal;
	}
	private String getSiteCode( String xtraParams, Connection connObject ) throws ITMException
	{
		String defaultSite = "";
		String empSite = "";
		String workSite = "";
		String sql = "";
		String loginCode = "";

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			loginCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));

			sql = "SELECT USERS.EMP_CODE, USERS.ENTITY_CODE, USERS.SITE_CODE__DEF, EMP.EMP_SITE, EMP.WORK_SITE "+
					"FROM USERS USERS, EMPLOYEE EMP WHERE USERS.EMP_CODE = EMP.EMP_CODE AND USERS.CODE = ? ";
			pstmt = connObject.prepareStatement( sql );
			pstmt.setString( 1, loginCode );						
			rs = pstmt.executeQuery();	
			if( rs.next() )
			{
				defaultSite = checkNull(rs.getString("SITE_CODE__DEF"));
				empSite = checkNull(rs.getString("EMP_SITE"));
				workSite = checkNull(rs.getString("WORK_SITE"));
			}
			if ( defaultSite.trim().length() == 0 && empSite.trim().length() > 0 )
			{
				defaultSite = empSite;
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
		}
		catch (Exception e)
		{				
			e.printStackTrace();
			throw new ITMException(e);
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
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("defaultSite = ["+defaultSite+"]");
		return defaultSite;
	}
	/*private String addSelectedRecToDom( Document allFormDataDom, String detailString ) throws Exception
	{
		ArrayList selectedList = new ArrayList();
		NodeList detailList = null;
		Node currDetail = null;
		NodeList currDetailList = null;
		String selectedVal = "", nodeName = "", updateStatus = "";
		String domIDVal = "";
		String updatedDetailDomStr = "";
		String attributeVal = "";
		String locCodeTo = "";
		String columnValue = "";

		HashMap toUpdateMap = new HashMap();

		Node elementName = null;

		int currDetailListLength = 0;
		int	detailListLength = 0;

		int selectedValCnt = 0;

		GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{
			detailList = allFormDataDom.getElementsByTagName("Detail2");
			detailListLength = detailList.getLength();
			for ( int ctr = 0; ctr < detailListLength; ctr++ )
			{
				currDetail = detailList.item(ctr);
				currDetailList = currDetail.getChildNodes();
				currDetailListLength = currDetailList.getLength();

				attributeVal = "";
				columnValue = "";

				ArrayList toUpdNodeList = new ArrayList();
				HashMap dataMap = new HashMap();

				for ( int i=0; i< currDetailListLength; i++ )
				{
					Node childNode = currDetailList.item(i);
					nodeName = childNode.getNodeName();
					if (nodeName.equalsIgnoreCase("Attribute"))
					{
						attributeVal = checkNull(currDetailList.item(i).getAttributes().getNamedItem("selected").getNodeValue());
						System.out.println("attributeVal =["+attributeVal+"]");
					}//if (nodeName = Attribute
					else if (nodeName.equalsIgnoreCase("loc_code__to"))
					{
						if ( childNode != null && childNode.getFirstChild() != null )
						{
							locCodeTo = childNode.getFirstChild().getNodeValue();
							//toUpdNodeList.add( 1, childNode );
							dataMap.put("loc_code__to", childNode);
						}
					}
				}//End of inner for loop
				if ( "Y".equalsIgnoreCase(attributeVal.trim()) )
				{
					if ( currDetail.getAttributes().getNamedItem( "domID" ) != null )
					{
						domIDVal = checkNull(currDetail.getAttributes().getNamedItem( "domID" ).getNodeValue());
						System.out.println("domIDVal =["+domIDVal+"]");
					}
					toUpdateMap.put( domIDVal, dataMap );
				}
			}//End of for loop

			System.out.println("toUpdateMap:>>>>>>>>>>>: "+toUpdateMap);

			detailList = null;

			detailString = detailString + "</Root>\r\n" ;	
			System.out.println("detailString:>>>>>>>>>>> "+detailString);
			Document detailDom = genericUtility.parseString(detailString); 

			Element elementAttr = null;

			detailList = detailDom.getElementsByTagName("Detail2");
			detailListLength = detailList.getLength();
			for ( int ctr = 0; ctr < detailListLength; ctr++ )
			{
				currDetail = detailList.item(ctr);

				if ( currDetail.getAttributes().getNamedItem( "selected" ) != null )
				{
					selectedVal = checkNull(currDetail.getAttributes().getNamedItem( "selected" ).getNodeValue());
					System.out.println("selectedVal =["+selectedVal+"]");
					if ( "N".equalsIgnoreCase(selectedVal.trim()) )
					{
						if ( currDetail.getAttributes().getNamedItem( "domID" ) != null )
						{
							//ArrayList aList = null;

							HashMap dataMap = new HashMap();

							domIDVal = checkNull(currDetail.getAttributes().getNamedItem( "domID" ).getNodeValue());
							System.out.println("domIDVal =["+domIDVal+"]");
							System.out.println("toUpdateMap.containsKey(domIDVal) =["+toUpdateMap.containsKey(domIDVal)+"]");
							if ( toUpdateMap.containsKey(domIDVal) )
							{
								elementAttr = (Element)currDetail;
								elementAttr.setAttribute( "selected" , "Y" );

								Node locCodeToNode = null;

								dataMap = (HashMap)toUpdateMap.get(domIDVal);

								if ( dataMap.get("loc_code__to") != null )
								{
									locCodeToNode = (Node)dataMap.get("loc_code__to");
								}

								Node updatedNode = null;

								currDetailList = currDetail.getChildNodes();
								currDetailListLength = currDetailList.getLength();

								for ( int i = 0; i < currDetailListLength; i++ )
								{
									elementName = currDetailList.item(i);
									nodeName = elementName.getNodeName();
									if( elementName.getNodeType() != Node.ELEMENT_NODE || nodeName.equalsIgnoreCase("#text") )
									{
										continue;
									}
									System.out.println("nodeName =["+nodeName+"]");
									if ( nodeName.equalsIgnoreCase("loc_code__to") )
									{
										//System.out.println("LocCodeToNode =["+genericUtility.serializeDom( LocCodeToNode )+"]");
										updatedNode = detailDom.importNode(locCodeToNode, true);
										//System.out.println("updatedNode =["+genericUtility.serializeDom( updatedNode )+"]");
										(detailDom.getElementsByTagName("Detail2").item(ctr)).replaceChild(updatedNode, elementName );
									}
								} //inner for loop
							} //if ( toUpdateMap.containsKey(domIDVal) )
						} //if ( currDetail.getAttributes().getNamedItem( "domID" )
					}//if ( "N".equalsIgnoreCase(selectedVal.trim()) )
				}//if ( currDetail.getAttributes().getNamedItem( "selected" )
			}//outer for loop
			updatedDetailDomStr = genericUtility.serializeDom( detailDom );
			System.out.println("updatedDetailDomStr:>>>>>>>>>>> "+updatedDetailDomStr);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		return updatedDetailDomStr;
	}*/
	private String getAttribValue(Node detailNode, String nodeName, String attribStr) throws ITMException
	{
		String attribValue = "";
		try
		{
			String domStr = genericUtility.serializeDom(detailNode);
			Document dom = genericUtility.parseString(domStr);
			if( dom != null /*&& dom.getAttributes() != null*/)
			{
				Node attributeNode = dom.getElementsByTagName( nodeName ).item(0);
				attribValue = getAttribValue(attributeNode, attribStr);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : getAttribValue :" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return attribValue;
	}

	private String getAttribValue(Node detailNode, String attribStr) throws ITMException
	{
		String attribValue = "";
		try
		{
			if( detailNode != null && detailNode.getAttributes() != null)
			{
				Node attribNode = detailNode.getAttributes().getNamedItem( attribStr );
				if( attribNode != null )
				{
					attribValue = checkNull( attribNode.getNodeValue() );
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : getAttribValue :" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return attribValue;
	}
	private String getAvilablePickLocation(Connection conn,String itemLocType,int locPrefZone, String locCode,String itemCode,String siteCode,String lotNo,String lpnNo) throws ITMException
	{
		long startTime17 = System.currentTimeMillis();
		String sql = "";
		String stockSql = "";
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rsStock = null;
		ResultSet rsOrder = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmtStock = null;
		PreparedStatement pstmtOrder = null;
		PreparedStatement pstmtItemLotOpt0 = null;
		PreparedStatement pstmtItemLotOpt1 = null;
		double stockQty = 0d;
		String suggLocCode = "";
		String singleLotSl = "";
		String itemLotOpt = "";
		ArrayList sugstLocList = new ArrayList();
		String orderSql = "";
		String sqlItemLotOpt0 = "";
		String sqlItemLotOpt1 = "";
		String invStat = "";
		String invStatDisparmVar = "";
		double quantity = 0;
		int count = 0;
		ibase.webitm.ejb.dis.CommonWmsUtil1 commonWmsUtility =ibase.webitm.ejb.dis.CommonWmsUtil1.getInstance();
		try
		{

			     sql =  " SELECT A.LOC_CODE AS LOC_CODE,A.SINGLE_LOT_SL AS  SINGLE_LOT_SL,A.ITEM_LOT_OPT AS ITEM_LOT_OPT"
					+ " FROM LOCATION A, INVSTAT B  WHERE A.INV_STAT = B.INV_STAT AND B.AVAILABLE = 'Y' "
					+ " AND (CASE WHEN B.STAT_TYPE IS NULL THEN ' ' ELSE B.STAT_TYPE END) <> 'S' "
					+ " AND A.LOC_TYPE = ? ORDER BY ABS( A.LOC_ZONE - ?), A.LOC_CODE ";

			stockSql =  " SELECT QUANTITY FROM STOCK WHERE LOC_CODE = ?  AND QUANTITY > 0 " ;

			sqlItemLotOpt0 = " SELECT COUNT(*) AS COUNT FROM STOCK WHERE (ITEM_CODE <> ? OR LOT_NO <> ?) AND SITE_CODE = ? AND LOC_CODE = ? AND QUANTITY > 0 "; 

			sqlItemLotOpt1 = "SELECT COUNT(*) AS COUNT FROM STOCK WHERE  ITEM_CODE <> ? AND SITE_CODE = ? AND LOC_CODE = ? AND QUANTITY > 0";

			System.out.println(" pnd sql -> ["+sql+"]");

			pstmtStock =  conn.prepareStatement(stockSql);
			pstmt =  conn.prepareStatement(sql);
			//pstmtOrder = conn.prepareStatement(orderSql);
			pstmtItemLotOpt0 = conn.prepareStatement( sqlItemLotOpt0 );
			pstmtItemLotOpt1 = conn.prepareStatement( sqlItemLotOpt1);

			pstmt.setString(1, itemLocType);
			pstmt.setInt(2, locPrefZone);
			rs = pstmt.executeQuery();

			while (rs.next())
			{
				suggLocCode = checkNull(rs.getString("LOC_CODE") );
				singleLotSl = checkNull(rs.getString("SINGLE_LOT_SL") );
				itemLotOpt = checkNull(rs.getString("ITEM_LOT_OPT") );

				quantity = 0;
				count = 0;
                System.out.println("suggLocCode::::::::"+suggLocCode);
                System.out.println("singleLotSl::::::::"+singleLotSl);
                System.out.println("itemLotOpt::::::::"+itemLotOpt);
                
                if(!commonWmsUtility.isValFacLocation(suggLocCode, siteCode, conn))
				{
                	suggLocCode = "";
					continue;
				}           
				if( singleLotSl.trim().equalsIgnoreCase("Y") )
				{
					pstmtStock.setString( 1, suggLocCode );
					rs2 = pstmtStock.executeQuery();	
					if( rs2.next() )
					{
						quantity = rs2.getDouble("QUANTITY");
						System.out.println("quantity:::::::"+quantity);
					}
					rs2.close();rs2 = null;
					
					if( quantity != 0 )
					{
						suggLocCode = "";
						continue;
					}
					else
					{
						return suggLocCode;
					}				
				}
				else
				{
					if( itemLotOpt.trim().equals("0") )  
					{	
						pstmtItemLotOpt0.setString( 1, itemCode );	
						pstmtItemLotOpt0.setString( 2, lotNo );
						pstmtItemLotOpt0.setString( 3, siteCode );
						pstmtItemLotOpt0.setString( 4, suggLocCode );	
						rs2 = pstmtItemLotOpt0.executeQuery();
						if( rs2.next() )
						{
							count = rs2.getInt("COUNT");
							System.out.println("count:::::::"+count);
						}
						rs2.close();rs2 = null;
						
						if( count > 0 )
						{
							suggLocCode = "";
							continue;
						}
						else
						{
							return suggLocCode;
						}
					}				
					else if( itemLotOpt.trim().equals("1") )  
					{
						pstmtItemLotOpt1.setString( 1, itemCode );
						pstmtItemLotOpt1.setString( 2, siteCode );
						pstmtItemLotOpt1.setString( 3, suggLocCode );
						rs2 = pstmtItemLotOpt1.executeQuery();
						if( rs2.next() )
						{
							count = rs2.getInt("COUNT");
							System.out.println("count1:::::::"+count);
						}
						rs2.close();rs2 = null;
						
						if( count > 0 )
						{
							suggLocCode = "";
							continue;
						}
						else
						{
							return suggLocCode;
						}
					}
				}
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
			if( pstmtStock != null )
			{
				pstmtStock.close();
				pstmtStock = null;
			}
			if(pstmtItemLotOpt0 != null)
			{
				pstmtItemLotOpt0.close();
				pstmtItemLotOpt0 = null;
			}		
			if(pstmtItemLotOpt1 != null)
			{
				pstmtItemLotOpt1.close();
				pstmtItemLotOpt1 = null;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in  == >"+e);
			e.printStackTrace();
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
				if( rs1 != null )
				{
					rs1.close();
					rs1 = null;
				}
				if( rs2 != null )
				{
					rs2.close();
					rs2 = null;
				}
				if( rsStock != null )
				{
					rsStock.close();
					rsStock = null;
				}
				if( rsOrder != null )
				{
					rsOrder.close();
					rsOrder = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if( pstmtStock != null )
				{
					pstmtStock.close();
					pstmtStock = null;
				}
				if( pstmtOrder != null )
				{
					pstmtOrder.close();
					pstmtOrder = null;
				}
				if(pstmtItemLotOpt0 != null)
				{
					pstmtItemLotOpt0.close();
					pstmtItemLotOpt0 = null;
				}

				if(pstmtItemLotOpt1 != null)
				{
					pstmtItemLotOpt1.close();
					pstmtItemLotOpt1 = null;
				}
			}
			catch (Exception e1)
			{
				throw new ITMException(e1);
			}
		}

		System.out.println("Return Suggested Location will throw error  == >["+suggLocCode+"]");
		long endTime17 = System.currentTimeMillis();
		System.out.println("DIFFERANCE IN TIME FOR getAvilablePickLocation IN SECONDS:::["+(endTime17-startTime17)/1000+"]");
		return suggLocCode;	
	}
}
