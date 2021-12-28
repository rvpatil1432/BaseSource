/********************************************************
	Title : DeliveryTerm
	Date  : 07/may/12
	Developer:Rakesh kumar
 ********************************************************/

package ibase.webitm.ejb.dis;
import javax.ejb.Stateless;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
@Stateless  
public class DeliveryTerm extends ValidatorEJB implements DeliveryTermLocal,DeliveryTermRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance(); 

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
			System.out.println("Exception : CustomerJvEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		E12GenericUtility genericUtility = new E12GenericUtility();
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String userId = null;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		String mmax_cramt="";
		PreparedStatement pStmt=null;
		ResultSet rs = null;
		String sql = null;
		String siteCode = "";
		String errCode = "";
		String mval1="";
		String custCode = "";
		String policyNo="";
		String itemParnt="";
		String mdate1="";
		int cnt = 0;
		int ctr = 0;
		int maxDays=0;
		String unit="";
		String aprCode="";
		String locCode="";
		int minDays=0;
		int mminCramt=0;
		int mmaxCramt=0;
		String itemSer = "";
		String taxChap="";
		String maxDay="";
		String mmin_cramt="";
		String mval="";
		String mdate2="";
		String mmin_day="";
		String mmax_day="";
		String minDay="";
		String loginSite="";
		String itemNo="";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		String errorType = "";
		ConnDriver connDriver = new ConnDriver();
		// Changed By Nasruddin [16-SEP-16] START
		String dlvTerm = "";
		String keyFlag = "";
		String modName = "w_dlv_term";
		String itemCode = "";
		Timestamp effDate = null, validUpTo = null;
		String effDateStr = "", validUptoStr = "";
		
		// Changed By Nasruddin [16-SEP-16] END
		try
		{
			System.out.println( "wfValData called" );
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

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
					//Changed By Nasruddin [16-SEP-16] STart
					if ( childNodeName.equalsIgnoreCase( "dlv_term" ) )
					{
						dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
						dlvTerm = dlvTerm == null ? "" : dlvTerm.trim();
						System.out.println(":::: dlv Term [" + dlvTerm + "]");
						sql = " select key_flag from TRANSETUP where TRAN_WINDOW = ? " ;
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, modName);
						rs = pStmt.executeQuery();
						if(rs.next())
						{
							keyFlag = rs.getString("key_flag");
						}
						pStmt.close();
						pStmt = null;
						rs.close();
						rs = null;

						keyFlag = keyFlag ==  null ? "M" : keyFlag.trim();
						if(keyFlag.equalsIgnoreCase("M") && dlvTerm.isEmpty() )
						{
							errCode = "VMCODNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else if("A".equalsIgnoreCase( editFlag ) )
						{
							int count = 0;
							sql = "SELECT COUNT(1)  as count FROM DELIVERY_TERM WHERE DLV_TERM = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, dlvTerm );
							rs = pStmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt("count");
							}

							pStmt.close();
							pStmt = null;
							rs.close();
							rs = null;

							if(count > 0)
							{
								errCode = "VMDUPL1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}//Changed By Nasruddin [16-SEP-16] END
					
					/* Comment By Nasruddin khan [ 16-SEP-16 ] START
					else if ( childNodeName.equalsIgnoreCase( "item_no" ) )
					{
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VMSTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							itemNo = genericUtility.getColumnValue( "item_no", dom);
							sql = " SELECT COUNT(*) FROM item WHERE item_no = ? ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,itemNo);
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{
								errCode = "VMINVSITE";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
					}
					 Comment By Nasruddin khan [ 16-SEP-16 ] END */

					else if ( childNodeName.equalsIgnoreCase( "item_parnt" ) )
					{
						/* Comment By Nasruddin khan [ 16-SEP-16 ] START 
						if ( childNode.getFirstChild() == null )
						{

							errCode = "VMSTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						Comment By Nasruddin khan [ 16-SEP-16 ] END */

						// Changed By Nasruddin [16-SEP-16] START
						itemParnt = genericUtility.getColumnValue( "item_parnt", dom);
						itemCode = genericUtility.getColumnValue( "item_code", dom);

						if( itemParnt != null && itemParnt.trim().length() > 0 && (!(itemParnt).equalsIgnoreCase(itemCode)))
						{
							sql = "SELECT COUNT(1)  FROM ITEM WHERE ITEM_CODE =  ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,itemParnt);
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{

								errCode = "VTITEMP1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;	
						}

						/*  Comment By Nasruddin khan [ 16-SEP-16 ] START
                           //else
						 //{
						    itemParnt = genericUtility.getColumnValue( "item_parnt", dom);
						 	sql = " SELECT COUNT(*) FROM item WHERE item_parnt = ? ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,itemParnt);
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{

								errCode = "VTITEMP1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							Comment By Nasruddin khan [ 16-SEP-16 ] END */

						//}
					}

					else if ( childNodeName.equalsIgnoreCase( "item_ser" ) )
					{
						/*  Comment By Nasruddin khan [ 16-SEP-16 ] START
						if ( childNode.getFirstChild() == null )
						{

							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						 Comment By Nasruddin khan [ 16-SEP-16 ] end */
						//else
						//{
						itemSer = genericUtility.getColumnValue( "item_ser", dom, "1" );		
						sql = " select count(*) from itemser where item_ser = ? ";
						pStmt = conn.prepareStatement( sql );
						pStmt.setString(1,itemSer);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt( 1 );
						} 
						if( cnt == 0 )
						{
							errCode = "VTITSER1";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						//}
					}

					else if ( childNodeName.equalsIgnoreCase( "unit" ) )
					{
						/*  Comment By Nasruddin khan [ 16-SEP-16 ] START
						if ( childNode.getFirstChild() == null )
						{

							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{ Comment By Nasruddin khan [ 16-SEP-16 ] end */
							unit = genericUtility.getColumnValue( "unit", dom);		
						     // Change By Nasruddin [16-sep-16]
							//	sql = " select count(*) from site where site_code = ? ";
							sql = "SELECT COUNT(1) FROM UOM WHERE UNIT = ?";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,unit);
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{
								errCode = "VTUOM1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						//}
					}

					else if ( childNodeName.equalsIgnoreCase( "site_code" ) )
					{
						/*  Comment By Nasruddin khan [ 16-SEP-16 ] START
						if ( childNode.getFirstChild() == null )
						{

							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						  Comment By Nasruddin khan [ 16-SEP-16 ] END 
						else
						{*/
                          						
							siteCode = genericUtility.getColumnValue( "site_code", dom);	
							// Changed By Nasruddin [16-sep-19] START
							if( siteCode.trim().length() > 0)
							{
								sql = " select count(*) from site where site_code = ? ";
								pStmt = conn.prepareStatement( sql );
								pStmt.setString(1,siteCode);
								rs = pStmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								if( cnt == 0 )
								{
									errCode = "VTSITE1";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
							}
					
						//}
					}

					else if ( childNodeName.equalsIgnoreCase( "loc_code" ) )
					{
						/* Comment  By Nasruddin [16-sep-19] START
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{  Comment By Nasruddin [16-sep-19] END */
						locCode = genericUtility.getColumnValue( "loc_code", dom);
						// Changed By Nasruddin [16-sep-19] 
						if(  locCode.trim().length() > 0)
						{
							sql = " select count(*) from location where loc_code = ? ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,locCode);
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{
								errCode = "VTLOC1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
					}
					else if ( childNodeName.equalsIgnoreCase( "apr_code" ) )
					{
						/* Comment  By Nasruddin [16-sep-19] START
						if ( childNode.getFirstChieffDateStrld() == null )
						{

							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{  Comment  By Nasruddin [16-sep-19] END */
						aprCode = genericUtility.getColumnValue( "apr_code", dom);		
						// Changed By Nasruddin [16-sep-19] 
						if(  aprCode.trim().length() > 0)
						{
							sql = " select count(*) from aprlev where apr_code = ? ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,aprCode.trim());
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{
								errCode = "VTAPR1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
					}
					else if ( childNodeName.equalsIgnoreCase( "tax_chap" ) )
					{
						/* Comment  By Nasruddin [16-sep-19] START
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{ Comment  By Nasruddin [16-sep-19] END */
						taxChap = genericUtility.getColumnValue( "tax_chap", dom);	
						//Changed By Nasruddin [16-SEP-16]
						if( taxChap.trim().length() > 0)
						{
							sql = " select count(*) from taxchap where tax_chap = ? ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,taxChap.trim());
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							if( cnt == 0 )
							{
								errCode = "VTTCHAP1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
					}

					else if ( childNodeName.equalsIgnoreCase( "policy_no" ) )
					{
						/* Comment  By Nasruddin [16-sep-19] START
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VMACTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
						 Comment  By Nasruddin [16-sep-19] END */
						policyNo = genericUtility.getColumnValue( "policy_no", dom);
						// Changed By Nasruddin [16-SEP-16]
						if( policyNo != null && policyNo.trim().length() > 0)
						{
							sql = " select count(*) from insurance where policy_no = ? ";
							pStmt = conn.prepareStatement( sql );
							pStmt.setString(1,policyNo.trim());
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );	
							} 
							if( cnt == 0 )
							{
								errCode = "VTPOLI1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							rs.close();										
							rs = null;
							pStmt.close();
							pStmt = null;
						}
					}	

				} 
				//END OF CASE1
				break;

			case 2 :
				System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if ( childNodeName.equalsIgnoreCase( "min_day" ) )
					{
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VTINVSNDCD";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							mval = genericUtility.getColumnValue( "dlv_term", dom1 );
							mval1 = genericUtility.getColumnValue( "line_no", dom1 );
							//Changed By Nasruddin [16-SEP-16]
							//mmin_day = genericUtility.getColumnValue( "fldname", dom1 );
							mmin_day = genericUtility.getColumnValue( "min_day", dom1 );
							minDays=Integer.parseInt(minDay);
							mmax_day = genericUtility.getColumnValue( "max_day", dom1 );
							maxDays=Integer.parseInt(maxDay);								
							mmin_cramt = genericUtility.getColumnValue( "min_cramt", dom1 );
							mmax_cramt = genericUtility.getColumnValue( "max_cramt", dom1 );
							mdate1 = genericUtility.getColumnValue( "eff_date", dom1 );
							mdate2 = genericUtility.getColumnValue( "valid_upto", dom1 );
							if(maxDays < minDays)
							{
								errCode = "VTINVSNDCD";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );	
							}		
							else
							{
								/* Changed By Nasruddin [16-sep-16] Start
								 minDay=genericUtility.getColumnValue( "min_day", dom);		
								 sql="select count(*) from crtermfc where dlv_Term = dlvTerm and mmin_day between min_day and max_day and (mdate1 between eff_date and valid_upto or mdate2 between eff_date and valid_upto) and line_no <> mval1";	
								pStmt = conn.prepareStatement( sql );
								pStmt.setString(1, minDay); Changed By Nasruddin [16-sep-16] Start END*/
								effDate = Timestamp.valueOf(genericUtility.getValidDateString(mdate1, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								validUpTo = Timestamp.valueOf(genericUtility.getValidDateString(mdate2, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								sql = " SELECT COUNT(1)  FROM DLTERMFC " +
									  " WHERE DLV_TERM = ? " +
									  " AND  ? BETWEEN MIN_DAY AND MAX_DAY " +
									  " AND (? BETWEEN EFF_DATE AND VALID_UPTO 	OR ? BETWEEN EFF_DATE AND VALID_UPTO)" +
									  " AND LINE_NO <> ?";
								pStmt = conn.prepareStatement( sql );
								pStmt.setString(1, mval);
								pStmt.setInt(2, minDays);
								pStmt.setTimestamp(3, effDate);
								pStmt.setTimestamp(4, validUpTo);
								pStmt.setString(5, mval1);
								//  Changed By Nasruddin [16-sep-16] END
								rs = pStmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );	
								}
								if( cnt > 0 )
								{
									errCode = "VTCRTERM2";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									/* Changed By Nasruddin [16-sep-16] Start
									sql="select count(*) into cnt from dltermfc where dlv_term = mval  and " +
									"mmin_day between min_day and max_day and (eff_date between mdate1 and mdate2 " +
									"or valid_upto between mdate1 and mdate2) and line_no <> :mval1 ";

									pStmt = conn.prepareStatement( sql );
									pStmt.setString(1,minDay); Changed By Nasruddin [16-sep-16] Start END*/
								    sql = " SELECT COUNT(1) FROM DLTERMFC	" +
								    	  " WHERE DLV_TERM = ?  " +
								    	  " AND 	? BETWEEN MIN_DAY AND MAX_DAY	" +
								    	  " AND (EFF_DATE BETWEEN ? AND ?	OR VALID_UPTO BETWEEN ? AND ?) AND LINE_NO <> ?";
									pStmt = conn.prepareStatement( sql );
									pStmt.setString(1, mval);
									pStmt.setInt(2, minDays);
									pStmt.setTimestamp(3, effDate);
									pStmt.setTimestamp(4, validUpTo);
									pStmt.setTimestamp(5, effDate);
									pStmt.setTimestamp(6, validUpTo);
									pStmt.setString(7, mval1);
									rs = pStmt.executeQuery();
									//  Changed By Nasruddin [16-sep-16] END
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );	
									}
									if( cnt > 0 )
									{
										errCode = "VTDLTERM2";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
						}	
					}
					else if ( childNodeName.equalsIgnoreCase( "eff_date" ) )
					{
						/* Comment BY Nasruddin [16/SEP/16] START
						if ( childNode.getFirstChild() == null )
						{

							errCode = "VEDAT2";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						 Comment BY Nasruddin [16/SEP/16] END */
						
						//Changed BY Nasruddin [16/SEP/16] START
						effDateStr = genericUtility.getColumnValue("eff_date",dom);
						if(effDateStr==null || effDateStr.trim().length()==0)
						{
							errCode = "VEDAT2";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						//Changed BY Nasruddin [16/SEP/16] END
					}
					else if ( childNodeName.equalsIgnoreCase( "valid_upto" ) )
					{
						/* Comment BY Nasruddin [16/SEP/16] START
						if ( childNode.getFirstChild() == null )
						{

							errCode = "VEDAT2";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						 Comment BY Nasruddin [16/SEP/16] END */
						//Changed BY Nasruddin [16/SEP/16] START
						validUptoStr = genericUtility.getColumnValue("valid_upto",dom);
						if(validUptoStr  == null || validUptoStr.trim().length()==0)
						{
							errCode = "VEDAT2";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						//Changed BY Nasruddin [16/SEP/16] END
					}
					else if ( childNodeName.equalsIgnoreCase( "min_cramt" ) )
					{
						//Changed By Nasruddin [16-SEP-16]
						//mmin_cramt=genericUtility.getColumnValue( "fldname", dom);
						mmin_cramt=genericUtility.getColumnValue( "min_cramt", dom);
						mminCramt =Integer.parseInt(mmin_cramt);
						mmax_cramt=genericUtility.getColumnValue( "max_cramt", dom);
						mmaxCramt=Integer.parseInt(mmax_cramt);
						if(mminCramt<0)
						{
							errCode = "VMMINAMT1";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );

						}
						if(mmaxCramt<mminCramt)
						{
							errCode = "VMMINAMT";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );

						}		
					}

				}//END FOR OF CASE2
				break;
			}//END SWITCH
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
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::" +e);
			e.printStackTrace();
			errCode = "VALEXCEP";
			errString = getErrorString( "", errCode, userId );									
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( pStmt != null )
					{
						pStmt.close();
						pStmt = null;
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
		}
		errString = errStringXml.toString();
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
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [CustomerJvEJB][itemChanged] :==>\n"+e.getMessage());
		}
		return valueXmlString; 
	}
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		E12GenericUtility genericUtility = new E12GenericUtility();
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		int currentFormNo = 0;
		String columnValue = null;
		NodeList parentNodeList = null;
		Node parentNode = null; 
		Node childNode = null;
		NodeList childNodeList = null;
		String childNodeName = null;
		int childNodeListLength = 0;
		SimpleDateFormat sdf = null;
		String mcode= "";
		String mtenv="";
		int ctr = 0;
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("Current Form No ["+currentFormNo+"]");							
			switch (currentFormNo)
			{
			case 1:
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
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if(currentColumn.trim().equalsIgnoreCase("insured"))
				{
					mcode =	genericUtility.getColumnValue("insured", dom);
					if (mcode.equals("Y"))
					{
						valueXmlString.append("<insured_for protect=\"1\">").append("<![CDATA[100]]>").append("</insured_for>");	
					}
					else
					{
						valueXmlString.append("<insured_for protect=\"1\">").append("<![CDATA[0.0]]>").append("</insured_for>");
					}	
				}
				valueXmlString.append("</Detail1>");
				valueXmlString.append("</Root>");	
				break;

			case 2:
				valueXmlString.append("<Detail2>");	
				parentNodeList = dom.getElementsByTagName("Detail2");
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
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					mtenv =	genericUtility.getColumnValue("itm_default", dom);
					valueXmlString.append("<dlv_term protect=\"1\">").append("<![CDATA[+ mtenv +]]>").append("</dlv_term>");	
				}
				valueXmlString.append("</Detail2>");					
				valueXmlString.append("</Root>");					
			}//END OF TRY
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+ e.getMessage());
			e.printStackTrace();
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

	private String errorType( Connection conn , String errorCode )
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";

			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, checkNull(errorCode));			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
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
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	
}



























