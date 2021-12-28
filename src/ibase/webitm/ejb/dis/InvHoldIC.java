package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.util.Calendar;
import java.sql.*;

import org.w3c.dom.*;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class InvHoldIC extends ValidatorEJB implements InvHoldICLocal , InvHoldICRemote // SessionBean 
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
	@Override
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	@Override
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	@Override
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		System.out.println("Validation Start..........");
		try
		{
			System.out.println("xmlString:::"+xmlString);
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
	@Override
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = " ",lockCode="";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		int typeCnt = 0;
		String childNodeName = null;

		String errCode = null;
		String userId = null;
		int cnt = 0,quantity=0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null,pstmtQuant=null;
		ResultSet rs = null,rsQuant=null;
		String sql = null,sqlQuant=null;


		String tranDateStr =  null;

		ConnDriver connDriver = new ConnDriver();
		try
		{
			System.out.println( "wfValData called" );
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//genericUtility = GenericUtility.getInstance(); 
			if(objContext != null && objContext.length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			case 1 :
				System.out.println("VALIDATION FOR DETAIL@@@@ [ 1 ]..........");
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
						String siteCode = null;
						siteCode = genericUtility.getColumnValue( "site_code", dom, "1" );

						if(	siteCode != null )
						{
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
							/*
								sql = " SELECT COUNT(*) FROM site WHERE site_code ='" + siteCode.trim() + "'";
								System.out.println( " SQL FOR custCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
							 */

							sql = " SELECT COUNT(*) FROM site WHERE site_code = ?";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
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
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
							/*
								sql = " SELECT COUNT(*) FROM Employee WHERE emp_code ='" + empCode.trim() + "'";
								System.out.println( " SQL FOR empCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
							 */

							sql = " SELECT COUNT(*) FROM Employee WHERE emp_code = ?";
							System.out.println( " SQL FOR empCode ====> " + sql );
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,empCode);
							rs = pstmt.executeQuery();
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end

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
					if ( childNodeName.equalsIgnoreCase( "role_code" ) )
					{
						String roleCode = null;
						roleCode = genericUtility.getColumnValue( "role_code", dom, "1" );
						if( roleCode != null && roleCode.length() > 0 )
						{
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
							/*
								sql = "select count( 1 ) cnt from wf_role where role_code = '" + roleCode + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
							 */

							sql = "select count( 1 ) cnt from wf_role where role_code = ?";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,roleCode);
							rs = pstmt.executeQuery();
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println(" COUNT =====> [" + cnt + "]");
							if( cnt == 0 )
							{
								System.out.println(" ====> Role code invalid or not found <==== ");
								errCode = "VMINVROLE";
								errString = getErrorString( "role_code", errCode, userId );
							}
						}
					}

					//Changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate lock_code.Start

					if ( childNodeName.equalsIgnoreCase( "lock_code" ) )
					{
						//String lockCode = null;
						lockCode = genericUtility.getColumnValue( "lock_code", dom, "1" );
						if(lockCode == null)//checking for null
						{
							errString = getErrorString("lock_code","VTLOCKCD1",userId);  // CHANGE DONE BY RITESH ON 27/DEC/13
							break; 
						}
						else //To check exist in INV_LOCK master
						{
							sql = "SELECT COUNT(*) COUNT FROM INV_LOCK WHERE LOCK_CODE = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,lockCode);
							rs = pstmt.executeQuery();
							if( rs.next())
							{
								cnt = rs.getInt("COUNT");
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
								errString = getErrorString("lock_code","INVLOCKCD",userId);
								break;
							}

							/*
							 * changes done by mahendra on 29-05-2014
							 * Inventery Hold not allow to enter lock type is 'System managed locks' 
							 * */
							System.out.println("lockCode  "+lockCode);
							sql = "select count(*) as COUNT from inv_lock where lock_code=? and lock_type='3' " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,lockCode);
							rs = pstmt.executeQuery();
							if( rs.next())
							{
								typeCnt = rs.getInt("COUNT");
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
							if( typeCnt > 0 )
							{
								errString = getErrorString("lock_code","INVLKCDTP",userId);
								break;
							}
						}



					}
					//Changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate lock_code.end

				} //END OF CASE1
				break;
			case 2 :
				System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				String lockLevel="";
				lockCode = genericUtility.getColumnValue( "lock_code", dom1, "1" );//added by mahendra
				System.out.println("lockCode !!!"+lockCode);
				sql = " select lock_level from inv_lock where lock_code= ?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,lockCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					lockLevel=rs.getString("lock_level");
				}
				//rs.close();
				//rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("lockLevel  "+lockLevel);

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					cnt = 0;
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to get the value of the LINE_NO_SL.Start
					boolean lineNoSlPrsnt = false;
					String lineNoSl = null;

					lineNoSl = genericUtility.getColumnValue( "line_no_sl", dom, "2" );
					System.out.println("lineNoSl =["+lineNoSl+"]");



					if(	lineNoSl != null && lineNoSl.trim().length() > 0 )
					{
						lineNoSlPrsnt = true;
					}

					//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to get the value of the LINE_NO_SL.End

					if ( childNodeName.equalsIgnoreCase( "item_code" ) )
					{
						String itemCode = null;
						itemCode = genericUtility.getColumnValue( "item_code", dom, "2" );
						errCode = null;
						System.out.println(" itemCode =====> [" + itemCode + "]");
						System.out.println("lockLevel :"+lockLevel);
						if(itemCode == null && (lockLevel.equalsIgnoreCase("I") || lockLevel.equalsIgnoreCase("L")))
						{
							errCode = "VMITEMBLK";
							errString = getErrorString( "item_code", errCode, userId );
							System.out.println("item code cannot be blank!!!!!");
							break; 
						}
						if(	itemCode != null && itemCode.length() > 0 )
						{
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
							/*
								sql = " SELECT COUNT(*) FROM item WHERE item_code ='" + itemCode.trim() + "'";
								System.out.println( " SQL FOR item ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
							 */

							sql = " SELECT COUNT(*) FROM STOCK WHERE item_code = ?";         //modify by cpatil as per requirment. on 10-AUG-12 " stck instead of item "
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery();
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							System.out.println(" COUNT =====> [" + cnt + "]");
							if( cnt == 0 )
							{
								System.out.println(" ====> Item Code Invalid <==== ");
								errCode="VMINVITMST";
								//errCode = "VMINVITEM";
							}
							rs.close();
							rs = null;

							pstmt.close();
							pstmt = null;

							//added by cpatil start on 01/08/12 as per manoharan sir

							String siteCode = genericUtility.getColumnValue( "site_code", dom, "2" );
							String locCode = genericUtility.getColumnValue( "loc_code", dom, "2" );
							String lotNo = genericUtility.getColumnValue( "lot_no", dom, "2" );
							String lotSl = genericUtility.getColumnValue( "lot_sl", dom, "2" );
							int i=2;
							//System.out.println("@@@@@@::siteCode:["+siteCode+"]::locCode["+locCode+"]::lotNo:["+lotNo+"]::lotSl:["+lotSl+"]");
							sqlQuant="select sum(quantity) from stock where item_code= ? ";
							if( siteCode!=null )
							{
								sqlQuant = sqlQuant + " and site_code = ?";
							}
							if( locCode!=null )
							{

								sqlQuant = sqlQuant + " and loc_code = ?";
							}
							if( lotNo !=null )
							{

								sqlQuant = sqlQuant + " and lot_no = ?";
							}
							if( lotSl!=null )
							{

								sqlQuant = sqlQuant + " and lot_sl = ?";
							}

							pstmtQuant = conn.prepareStatement( sqlQuant );

							pstmtQuant.setString(1,itemCode);
							if( siteCode!=null )
							{
								pstmtQuant.setString(i,siteCode);
								i++;
							}
							if( locCode!=null )
							{
								pstmtQuant.setString(i,locCode);
								i++;
							}
							if( lotNo !=null )
							{
								pstmtQuant.setString(i,lotNo);
								i++;
							}
							if( lotSl!=null )
							{
								pstmtQuant.setString(i,lotSl);
							}

							rsQuant = pstmtQuant.executeQuery();
							if( rsQuant.next() )
							{
								quantity = rsQuant.getInt( 1 );
							} 

							rsQuant.close();
							rsQuant = null;
							pstmtQuant.close();
							pstmtQuant = null;

							if(quantity == 0)
							{
								errString = getErrorString( "item_code", "VTINQTY09" , userId );
							}
							// cpatil end

						}							
						else
						{
							errCode = "VMITEMBLK";
						}
						if( errCode != null )
						{
							errString = getErrorString( "item_code", errCode, userId );
						}
					}
					else if( childNodeName.equalsIgnoreCase( "site_code" ) )
					{
						String siteCode = null;
						siteCode = genericUtility.getColumnValue( "site_code", dom, "2" );

						if(	siteCode != null )
						{
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
							/*
								sql = " SELECT COUNT(*) FROM site WHERE site_code ='" + siteCode.trim() + "'";
								System.out.println( " SQL FOR siteCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
							 */

							sql = " SELECT COUNT(*) FROM site WHERE site_code =?";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end

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
						//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate null site code if Line No is present.start
						else
						{
							if(lineNoSlPrsnt)
							{
								//	errString = getErrorString( "SITE_CODE", "NULLSITECD" , userId );  //commented by cpatil on 01/08/12 as per manoharan sir
							}
						}
						//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate null site code if Line No is present.end
					}
					else if( childNodeName.equalsIgnoreCase( "loc_code" ) )
					{
						String locCode = null;

						locCode = genericUtility.getColumnValue( "loc_code", dom, "2" );
						System.out.println("locCode :"+locCode);

						if(locCode != null && (lockLevel.equalsIgnoreCase("L") || lockLevel.equalsIgnoreCase("I")))
						{
							errCode = "VMINVLCCD";
							errString = getErrorString( "lot_no", errCode, userId );
							System.out.println("Location Code blank in Item level or Lot Level!!!!!");
							break; 
						}


						if(	locCode != null )
						{
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
							/*
								sql = " SELECT COUNT(*) FROM Location WHERE loc_code ='" + locCode.trim() + "'";
								System.out.println( " SQL FOR locCode ====> " + sql );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
							 */

							sql = " SELECT COUNT(*) FROM Location WHERE loc_code =?";
							pstmt = conn.prepareStatement( sql );
							System.out.println( " SQL FOR locCode ====> " + sql );
							pstmt.setString(1,locCode);
							rs = pstmt.executeQuery();
							//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
							if( rs.next() )
							{
								cnt = rs.getInt( 1 );
							} 
							System.out.println(" COUNT =====> [" + cnt + "]");
							if( cnt == 0 )
							{
								System.out.println(" ====> Location invalid or not found <==== ");
								errCode = "VMINVLOC";
								errString = getErrorString( "LOC_CODE", errCode, userId );
							}
							rs.close();
							rs = null;

							pstmt.close();
							pstmt = null;


						}
						//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate null location code if Line No is present.start
						else
						{
							if(lineNoSlPrsnt)
							{
								//	errString = getErrorString( "LOC_CODE", "NULLLOCCD" , userId );  // commented by cpatil on 01/08/12 as per manoharan sir
							}

						}
						//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate null location code if Line No is present.end

					}

					//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate null lot no & lot sl if Line No is present.start
					else if( childNodeName.equalsIgnoreCase( "lot_no" ) )
					{
						String lotNo = null;
						lotNo = genericUtility.getColumnValue( "lot_no", dom, "2" );
						System.out.println("lotNo :"+lotNo);
						if(lotNo == null && lockLevel.equalsIgnoreCase("L"))
						{
							errCode = "VMEMTLTNO";
							errString = getErrorString( "lot_no", errCode, userId );
							System.out.println("Lot no cannot be blank!!!!!");
							break; 
						}
						if(lockLevel.equalsIgnoreCase("I") && lotNo != null )
						{
							errCode = "VMINVLTNO";
							errString = getErrorString( "lot_no", errCode, userId );
							System.out.println("Lot no blank in Item level!!!!!");
							break; 
						}

						if( lineNoSlPrsnt )
						{
							if( lotNo == null )
							{
								//	errString = getErrorString( "LOT_NO", "NULLLOTNO", userId );  //commented by cpatil on 01/08/12 as per manoharan sir
							}
						}
					}						
					else if( childNodeName.equalsIgnoreCase( "lot_sl" ) )
					{
						String lotSl = null;
						lotSl = genericUtility.getColumnValue( "lot_sl", dom, "2" );
						System.out.println("lotSl  :"+lotSl);
						if(lotSl != null && (lockLevel.equalsIgnoreCase("L") || lockLevel.equalsIgnoreCase("I")))
						{
							errCode = "VMINVLTSL";
							errString = getErrorString( "lot_sl", errCode, userId );
							System.out.println("Lot SL blank in Item level!!!!!");
							break; 
						}

						if( lineNoSlPrsnt )
						{
							if( lotSl == null )
							{
								//	errString = getErrorString( "LOT_SL", "NULLLOTSL", userId );    //commented by cpatil on 01/08/12 as per manoharan sir
							}
						}
					}
					//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to validate null lot no & lot sl if Line No is present.end

					else if ( childNodeName.equalsIgnoreCase( "sch_rel_date" ) )
					{
						java.sql.Timestamp schRelDate = null;
						java.sql.Timestamp tranDate = null;
						String schRelDateStr = null;
						schRelDateStr = genericUtility.getColumnValue( "sch_rel_date", dom, "2" );
						tranDateStr = genericUtility.getColumnValue( "tran_date", dom1, "1" );

						errCode = null;
						System.out.println( "tranDateStr :: " + tranDateStr );
						if(	schRelDateStr == null || schRelDateStr.length() == 0 )
						{
							errCode = "VMSCHDTBLK";
						}
						else
						{	
							if( tranDateStr != null && tranDateStr.length() > 0 )
							{
								tranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}
							/*
								else
								{
									tranDate = new java.sql.Timestamp( System.currentTimeMillis() - 24 * 3600 );
									//tranDate = Timestamp.valueOf(genericUtility.getValidDateString( tranDate.toString(), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
									System.out.println( "tranDate :: " + tranDate );
								}
							 */
							System.out.println( "tranDate :: " + tranDate );
							schRelDate = Timestamp.valueOf(genericUtility.getValidDateString(schRelDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println( "schRelDate :: " + schRelDate );
							if( schRelDate.compareTo ( tranDate ) < 0 )
							{
								errCode = "VMSCHDTBFR";
							}
						}
						if( errCode != null )
						{
							errString = getErrorString( "sch_rel_date", errCode, userId );									
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
			errCode = "VALEXCEP";
			errString = getErrorString( "", errCode, userId );	
			throw new ITMException(e);
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

	@Override
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
			throw new ITMException(e);
		}
		return valueXmlString; 
	}

	@Override
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		int currentFormNo = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = null,lockCode=null;

		NodeList parentNodeList = null;
		Node parentNode = null; 
		Node childNode = null;
		NodeList childNodeList = null;
		String childNodeName = null;
		int childNodeListLength = 0;
		int ctr = 0;
		int stdDays=0;
		java.util.Date date = null;
		java.sql.Timestamp currDate = null,currDate2 = null;

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

			getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

			System.out.println("[EpaymentICEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
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
							columnValue=childNode.getFirstChild().getNodeValue();
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.equals( "itm_default" ))
				{
					String loginSiteCode = null;
					String loginEmpCode = null;
					String deptCode = null;
					String deptDescr = null;
					String siteDescr = null;
					loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
					loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
					/*
						sql = "select e.dept_code, d.descr dept_descr, s.descr site_descr from employee e, department d, site s "
								+" where e.emp_code = '" + loginEmpCode + "'" 
								+"		and s.site_code = '" + loginSiteCode + "'"
								+"		and e.dept_code = d.dept_code(+) ";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
					 */

					// Changed by Manish on 29/09/15 for ms sql server [start]
					String DB = CommonConstants.DB_NAME;
					if("mssql".equalsIgnoreCase(DB))
					{
						sql = "select e.dept_code, d.descr dept_descr, s.descr site_descr from site s, employee e left outer join department d "
							+" on e.dept_code = d.dept_code "
							+" where e.emp_code = ? " 
							+" and s.site_code = ?" ;
					}
					else
					{
						sql = "select e.dept_code, d.descr dept_descr, s.descr site_descr from employee e, department d, site s "
							+" where e.emp_code = ? " 
							+" and s.site_code = ?"
							+"	and e.dept_code = d.dept_code(+) ";
					}
					// Changed by Manish on 29/09/15 for ms sql server [end]

					pStmt = conn.prepareStatement( sql );
					pStmt.setString(1,loginEmpCode);
					pStmt.setString(2,loginSiteCode);
					rs = pStmt.executeQuery();
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end

					if( rs.next() )
					{
						deptCode = rs.getString( "dept_code" );
						deptDescr = rs.getString( "dept_descr" );
						siteDescr = rs.getString( "site_descr" );
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<tran_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</tran_date>");
					valueXmlString.append("<site_code>").append("<![CDATA[" + ( loginSiteCode != null ? loginSiteCode : ""  )+ "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + ( siteDescr != null ? siteDescr : ""  )+ "]]>").append("</site_descr>");
					valueXmlString.append("<dept_code>").append("<![CDATA[" + ( deptCode != null ? deptCode : ""  )+ "]]>").append("</dept_code>");
					valueXmlString.append("<department_descr>").append("<![CDATA[" + ( deptDescr != null ? deptDescr : ""  )+ "]]>").append("</department_descr>");
				}

				if (currentColumn.equals( "role_code" ))
				{
					String descr = null;
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
					/*
						sql = "select descr from wf_role where role_code = '" + ( columnValue != null ? columnValue.trim() : "" ) + "'";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
					 */
					sql = "select descr from wf_role where role_code = ?";
					pStmt = conn.prepareStatement(sql);
					if(columnValue != null)
					{
						pStmt.setString(1,columnValue);
					}
					else
					{
						pStmt.setString(1,"");
					}
					rs = pStmt.executeQuery();
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end


					if( rs.next() )
					{
						descr = rs.getString( "descr" );
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<wf_role_descr>").append("<![CDATA[" + ( descr != null ? descr : ""  )+ "]]>").append("</wf_role_descr>");
				}

				//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to display descreption of the lock code when lock code is entered.start

				if (currentColumn.equals( "lock_code" ))
				{
					String lockDescr = null;
					sql = "SELECT DESCR FROM INV_LOCK WHERE LOCK_CODE = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1,columnValue);
					rs = pStmt.executeQuery();
					if( rs.next() )
					{
						lockDescr = rs.getString( "DESCR" );
					}

					valueXmlString.append( "<lock_descr><![CDATA[" ).append( lockDescr != null ? lockDescr : "").append( "]]></lock_descr>\r\n" );

					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pStmt != null )
					{
						pStmt.close();
						pStmt = null;
					}
				}

				//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to display descreption of the lock code when lock code is entered.end



				valueXmlString.append("</Detail1>");
				valueXmlString.append("</Root>");	
				break;
				///////////////
			case 2:
				valueXmlString.append("<Detail2>");	
				//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
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
							columnValue=childNode.getFirstChild().getNodeValue();
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.equals( "itm_default" ))
				{
					//add by cpatil start on 28-07-12 as per manoharan sir
					lockCode = genericUtility.getColumnValue( "lock_code", dom1 );
					// 23/05/13 manoharan if lock_code is not empty then only set
					stdDays = 1;
					if (lockCode != null && lockCode.trim().length() > 0)
					{
						sql="select std_days from inv_lock where lock_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1,lockCode);
						rs = pStmt.executeQuery();
						if(rs.next())
						{    
							stdDays = rs.getInt("std_days");
						}
						rs.close();
						rs= null;
						pStmt.close();
						pStmt=null;
					}

					currDate = new java.sql.Timestamp(System.currentTimeMillis());
					Calendar c = Calendar.getInstance();
					c.setTime(currDate);
					System.out.println("@@@@@ currDate:["+currDate+"]::::stdDays:["+stdDays+"]");
					c.add(Calendar.DATE, stdDays);
					currDate2 = new Timestamp(c.getTimeInMillis());
					SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
					date = simpledateformat.parse(currDate2.toString());
					currDate2 = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
					String currDate2Str = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(currDate2).toString();
					System.out.println("@@@@@ currDate2Str :["+currDate2Str+"]");
					valueXmlString.append("<sch_rel_date>").append("<![CDATA[" + currDate2Str + "]]>").append("</sch_rel_date>");   
					//valueXmlString.append("<sch_rel_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</sch_rel_date>");    // added by cpatil on 30-AUG-12 as  per suggestion
					//add by cpatil on 28-07-12 end
					valueXmlString.append("<hold_status>").append("<![CDATA[" + "H" + "]]>").append("</hold_status>");
					valueXmlString.append("<status_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</status_date>");
				}
				if (currentColumn.equals( "item_code" ))
				{
					String descr = null;
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
					/*
						sql = "select descr from item where item_code = '" + ( columnValue != null ? columnValue.trim() : "" ) + "'";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
					 */
					sql = "select descr from item where item_code = ?";
					pStmt = conn.prepareStatement(sql);
					if(columnValue != null)
					{
						pStmt.setString(1,columnValue);
					}
					else
					{
						pStmt.setString(1,"");
					}
					rs = pStmt.executeQuery();
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
					if( rs.next() )
					{
						descr = rs.getString( "descr" );
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					//end getting site bank
					valueXmlString.append("<item_descr>").append("<![CDATA[" + ( descr != null ? descr : ""  )+ "]]>").append("</item_descr>");
				}	
				if (currentColumn.equals( "site_code" ))
				{
					String descr = null;
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
					/*
						sql = "select descr from site where site_code = '" + ( columnValue != null ? columnValue.trim() : "" ) + "'";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
					 */
					sql = "select descr from site where site_code = ?";
					pStmt = conn.prepareStatement(sql);
					if(columnValue != null)
					{
						pStmt.setString(1,columnValue);
					}
					else
					{
						pStmt.setString(1,"");
					}
					rs = pStmt.executeQuery();
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
					if( rs.next() )
					{
						descr = rs.getString( "descr" );
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					//end getting site bank
					valueXmlString.append("<site_descr>").append("<![CDATA[" + ( descr != null ? descr : ""  )+ "]]>").append("</site_descr>");
				}	
				if (currentColumn.equals( "loc_code" ))
				{
					String descr = null;
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.start
					/*
						sql = "select descr from location where loc_code = '" + ( columnValue.trim() != null ? columnValue : "" ) + "'";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
					 */
					sql = "select descr from location where loc_code = ?";
					pStmt = conn.prepareStatement(sql);
					if(columnValue != null)
					{
						pStmt.setString(1,columnValue);
					}
					else
					{
						pStmt.setString(1,"");
					}
					rs = pStmt.executeQuery();
					//Changed by Dharmesh on 09/08/11 [WM1ESUN004] to bind variable dynamically instead of statically.end
					if( rs.next() )
					{
						descr = rs.getString( "descr" );
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					//end getting site bank
					valueXmlString.append("<location_descr>").append("<![CDATA[" + ( descr != null ? descr : ""  )+ "]]>").append("</location_descr>");
				}	
				valueXmlString.append("</Detail2>");					
				valueXmlString.append("</Root>");					
				////////////////
			}//END OF TRY
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
				throw new ITMException(e);
			}			
		}
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
}