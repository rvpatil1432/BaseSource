package ibase.webitm.ejb.dis;


import ibase.utility.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.text.*;
import java.io.File;
import javax.ejb.Stateless; 
// added for ejb3
@Stateless 
// added for ejb3

public class CustStockItem extends ValidatorEJB //implements SessionBean
{
	ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
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
    public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			//System.out.println("xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : SOrderFormEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			//System.out.println("xmlString:-" + xmlString );
			//long startTime = System.currentTimeMillis();
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			//long endTime = System.currentTimeMillis();
			//long totalTime = endTime - startTime;
			//System.out.println(xmlString2);
			//System.out.println("start Time Spend :: "+startTime+" Milliseconds");
			//System.out.println("End Time Spend :: "+endTime+" Milliseconds");
			//System.out.println("Total Time Spend :: "+totalTime+" Milliseconds");
			
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : SOrderFormEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		boolean checkNextCol = true;
		String columnName = "";
		String columnValue = "";
		String userId = "";
		String errString = "";
		Connection conn = null;
		StringBuffer selQueryBuff = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;
		String errCode = "";
		String siteCode ="";
		String tranDate = "";
		String sqlCnt = "";
		String tranId = "";
		String frdate1 = "";
		String tranDate1 = "";
		String lineNo = "";
		String custCode = "";
		String itemSer = "";
		String itemCode = "";
		//Changed by Dayanand on 3/2/2009[ Declared objName req id=WS89SUN078]start
		String objName = "";	
		int currentFormNo =0;
		java.util.Date frdate2 = null;
		java.util.Date tranDate2 = null;
		java.util.Date todate = null;
		java.sql.Date restUpto1 = null;
		java.util.Date restUpto2 = null;		
		SimpleDateFormat sdfFormat=null;
		// 11/09/09 manoharan
 		String begPart = null;
		String endPart = null;
		String itemDescr = null;
		// end 11/09/09 manoharan

		//ITMDBAccessHome itmDBAccessHome = null;
		//ITMDBAccessLocal itmDBAccess = null;
		
		try
		{
			//System.out.println("CustStockEJB validation");
			/*AppConnectParm appConnect = new AppConnectParm();
			Properties p = appConnect.getProperty();
			InitialContext ctx = new InitialContext(p);
			
			*/
			//itmDBAccess = itmDBAccessHome.create();

			//ibase.webitm.utility.GenericUtility genericUtility = new ibase.webitm.utility.GenericUtility();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			conn = getConnection();
			userId = genericUtility.getColumnValue( "user_id", dom );
			currentFormNo = Integer.parseInt( objContext );
			//Changed by dayanand on 2/28/2009[If obj_name is cust_stock (UploadFile) and form_no is 3 than pass currentFormNo = 2 req id=WS89SUN078]start				
			objName = getObjNameFromDom(dom,"objName");
			if ( objName.equals("cust_stock") && currentFormNo == 3 )
			{								
				currentFormNo = 2;
			}
			//Changed by dayanand on 2/28/2009[If obj_name is cust_stock (UploadFile) and form_no is 3 than pass currentFormNo = 2 req id=WS89SUN078]end
			
			switch ( currentFormNo )
			{
				case 1:
					columnValue = genericUtility.getColumnValue( "tran_date", dom );
					if(columnValue != null)
					{
						siteCode = genericUtility.getColumnValue( "site_code", dom );
						if ( columnValue.length() != 0 && checkNextCol )
						{
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = nfCheckPeriod("SAL",getDateObject(columnValue),siteCode);
							errCode=finCommon.nfCheckPeriod("SAL",getDateObject(columnValue),siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							if (errCode.length() != 0)
							{
								//errString = getErrorString("",errCode,userId);
								//checkNextCol = false;
								errString = getErrorString("tran_date",errCode,userId);//,null,conn); 
								break;                                
							}
						}
						int cnt = 0;						
						String tranIdStr = genericUtility.getColumnValue( "tran_id", dom );
						//Changed by dayanand on 2/28/2009 [ Add if condition if tran_id is not null than go ahead req id=WS89SUN078] start
						if ( tranIdStr != null )
						{
							//Changed by dayanand on 2/28/2009 [ Add if condition if tran_id is not null than go ahead req id=WS89SUN078] end
							//next line changed by msalam for dynamic binding
							//sqlCnt = "SELECT COUNT( 1 ) cnt FROM cust_stock WHERE tran_id = '" + tranIdStr + "' and transit_confirmed = 'Y'";
							sqlCnt = "SELECT COUNT( 1 ) cnt FROM cust_stock WHERE tran_id = ? and transit_confirmed = 'Y'";
							//System.out.println( "Transit confirm sql = " + sqlCnt );
							stmt = conn.prepareStatement( sqlCnt );
							stmt.setString( 1, tranIdStr );
							
							rs = stmt.executeQuery( );
							
							if ( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							if ( cnt == 0 )
							{
								errString = getErrorString( "tran_id", "VTUNCONF", userId);//, null, conn ); 
								break;
							}
							//Changed by dayanand on 2/28/2009 [ Add if condition if tran_id is not null than go ahead req id=WS89SUN078] start
						}
						//Changed by dayanand on 2/28/2009 [ Add if condition if tran_id is not null than go ahead req id=WS89SUN078] end
					}
					columnValue = genericUtility.getColumnValue("cust_code",dom);
					if(columnValue != null)
					{						
						siteCode = genericUtility.getColumnValue("site_code",dom);
						//System.out.println("siteCode [" +siteCode+ "]");
						tranDate = genericUtility.getColumnValue("tran_date",dom);
						if (columnValue.length() != 0 && checkNextCol)
						{
							errCode = isCustomer(siteCode,columnValue,"S-CSTK",conn);
							//System.out.println("errCode [" +errCode+ "]");
							if (errCode.length() !=0 )
							{								
								//errString = getErrorString("cust_code",errCode,userId); 
								//checkNextCol = false;									
								errString = getErrorString("cust_code",errCode,userId);//,null,conn); 
								break;
							}
						}						
						if (checkNextCol)
						{							
							selQueryBuff = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = ? ");
							selQueryBuff.append(" AND SITE_CODE = ? ");
							selQueryBuff.append(" AND NVL(ACTIVE_YN,'Y') = 'Y'");
							stmt = conn.prepareStatement( selQueryBuff.toString() );
							stmt.setString( 1, columnValue );
							stmt.setString( 2, siteCode );
							rs = stmt.executeQuery();
							//System.out.println("sql1 :\n" + selQueryBuff.toString());
							if (rs.next())
							{
								if (rs.getInt(1) == 0)
								{
									//errString = getErrorString("cust_code","VTSITECUST",userId);
									//checkNextCol = false;									
									errString = getErrorString("cust_code","VTSITECUST",userId);//,null,conn); 
									break;
								}
							}
						}
						else
						{
							/*Select count(1) into :cnt
							From	 customer
							Where  cust_code = :mval
							And	 (case when channel_partner is null then 'N' else channel_partner end ) = 'N';
							If cnt = 0 Then errcode = "VTCUST"*/
							//changed for dynamic binding on 190309
							//sqlCnt = "SELECT COUNT(1) COUNT FROM CUSTOMER WHERE CUST_CODE = '" + columnValue + "' And (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END ) = 'N'";
							sqlCnt = "SELECT COUNT(1) COUNT FROM CUSTOMER WHERE CUST_CODE = ? And (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END ) = 'N'";
							//System.out.println("sql :\n"+sqlCnt);
							stmt = conn.prepareStatement( sqlCnt );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next() && rs.getInt( "COUNT" ) == 0)
							{
								//errCode = "VTCUST";								
								errString = getErrorString("cust_code","VTCUST",userId);//,null,conn); 
								break;
							}
						}
						if (stmt != null)
						{
							stmt.close();
							stmt = null;
						}							
					}
					columnValue = genericUtility.getColumnValue("site_code",dom);					
					if(columnValue != null)
					{
						custCode = genericUtility.getColumnValue("cust_code",dom);
						if (checkNextCol)
						{	
							//changed for dynamic binding on 190309
							//sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = '"+columnValue+"'";
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = ? ";
							//System.out.println("sqlCnt :\n"+sqlCnt);
							stmt = conn.prepareStatement( sqlCnt );
							stmt.setString( 1, columnValue );
							
							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1)==0)
								{
									//errString = getErrorString("site_code","VTSITECD1",userId);
									//checkNextCol = false;

									errString = getErrorString("site_code","VTSITECD1",userId);//,null,conn); 
									break;
								}
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							if( rs != null )
							{
								rs.close();
								rs = null;
							}
						}
						if (checkNextCol)
						{
							//Changed for dynamic binding on 190309
							selQueryBuff = new StringBuffer();
							//selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = '");
							//selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
							//selQueryBuff.append(columnValue).append("' AND NVL(ACTIVE_YN,'Y') = 'Y'");
							
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = ? ");
							selQueryBuff.append(" AND SITE_CODE = ? ");
							selQueryBuff.append(" AND NVL(ACTIVE_YN,'Y') = 'Y'");

							//System.out.println("sql13 :\n"+selQueryBuff.toString());
							stmt = conn.prepareStatement( selQueryBuff.toString() );
							stmt.setString( 1, custCode );
							stmt.setString( 2, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1)==0)
								{
									//errString = getErrorString("site_code","VTSITECUST",userId);
									//checkNextCol = false;

									errString = getErrorString("site_code","VTSITECUST",userId);//,null,conn); 
									break;
								}
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							if( rs != null )
							{
								rs.close();
								rs = null;
							}							
						}
					}
					
					//SHWETA 2/14/2005 --Not required at the time of validation
					columnValue = genericUtility.getColumnValue("tran_id__last",dom);					
					if (columnValue != null) 
					{						
						PreparedStatement stmt14 = null;
						PreparedStatement stmt5 = null;
						ResultSet rs14 = null;
						ResultSet rs5 = null;
						StringBuffer sql5 = null;
						custCode = genericUtility.getColumnValue("cust_code",dom);
						itemSer = genericUtility.getColumnValue("item_ser",dom);
						/* // 03/03/10 manoharan not used the tran_id just selected 
						if (checkNextCol)
						{
							//changed for dynamic binding on 190309
							//String sq114 = "SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.TRAN_ID = B.TRAN_ID AND A.CUST_CODE = '"+custCode+"'";
							String sq114 = "SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.TRAN_ID = B.TRAN_ID AND A.CUST_CODE = ? ";
							//System.out.println("sq114 :\n"+sq114.toString());
							stmt14 = conn.prepareStatement( sq114.toString() );
							stmt14.setString( 1, custCode );
							rs14 = stmt14.executeQuery();
							if (rs14.next())
							{
								tranId = rs14.getString(1);
							}
							rs14.close();
							rs14 = null;
							stmt14.close();
							stmt14 = null;
							
							if (itemSer.trim().length() > 0 )
							{
								sql5 = new StringBuffer();
								//sql5.append("SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.CUST_CODE = '");
								//sql5.append(custCode).append("' AND A.TRAN_ID = B.TRAN_ID AND A.ITEM_SER = '");
								sql5.append("SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.CUST_CODE = ? ");
								sql5.append(" AND A.TRAN_ID = B.TRAN_ID AND A.ITEM_SER = ? ");
								
								stmt5 = conn.prepareStatement( sql5.toString() );
								
								stmt5.setString( 1, custCode );
								stmt5.setString( 2, itemSer );
							}
							else
							{
								sql5 = new StringBuffer();
								//sql5.append("SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.CUST_CODE = '");
								//sql5.append(custCode).append("' AND A.TRAN_ID = B.TRAN_ID");
								
								sql5.append("SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.CUST_CODE = ? ");
								sql5.append(" AND A.TRAN_ID = B.TRAN_ID ");

								stmt5 = conn.prepareStatement( sql5.toString() );
								
								stmt5.setString( 1, custCode );
							}
							//System.out.println("sql5 :\n"+sql5.toString());
							
							rs5 = stmt5.executeQuery();
							if (rs5.next())
							{
								String tranIdLast = rs5.getString(1);
							}
							rs5.close();
							rs5 = null;
							stmt5.close();
							stmt5 = null;
						 } */ // 03/03/10 manoharan
					 }
					columnValue = genericUtility.getColumnValue("to_date",dom);					
					if (columnValue != null) 
					{
						frdate1 = genericUtility.getColumnValue("from_date",dom);
						tranDate1 = genericUtility.getColumnValue("tran_date",dom);												
						if (!columnValue.equals("") && checkNextCol)
						{
							frdate2 = getDateObject(frdate1);
							tranDate2 = getDateObject(tranDate1);
							todate = getDateObject(columnValue);
							if (todate.compareTo(frdate2) < 0)
							{
								//errCode = "VFRTODATE";
								errString = getErrorString("to_date","VFRTODATE",userId);//,null,conn); 
								break;
							}
							else if (tranDate2.compareTo(todate) < 0)
							{
								//errCode = "VTDATE9";
								errString = getErrorString("to_date","VTDATE9",userId);//,null,conn); 
								break;
							}
							else
							{								
								custCode = genericUtility.getColumnValue("cust_code",dom);
								siteCode = genericUtility.getColumnValue("site_code",dom);
								itemSer = genericUtility.getColumnValue("item_ser",dom);
								if (itemSer.length() == 0)
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  IS NULL	AND ('");
									//selQueryBuff.append(frdate1).append("' BETWEEN FROM_DATE AND TO_DATE OR '");
									//selQueryBuff.append(columnValue).append("' BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
									selQueryBuff.append(genericUtility.getValidDateString( frdate1 , getApplDateFormat() , getDBDateFormat())).append("', '"+getDBDateFormat()+"' ) BETWEEN FROM_DATE AND TO_DATE OR '");
									selQueryBuff.append(genericUtility.getValidDateString( columnValue , getApplDateFormat() , getDBDateFormat())).append("', '"+getDBDateFormat()+"' )  BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
								}
								else
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  = '").append(itemSer).append("'	AND (TO_DATE('");
									//selQueryBuff.append(frdate1).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE OR TO_DATE('");
									//selQueryBuff.append(columnValue).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
									selQueryBuff.append(genericUtility.getValidDateString( frdate1 , getApplDateFormat() , getDBDateFormat())).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE OR TO_DATE('");
									selQueryBuff.append(genericUtility.getValidDateString( columnValue , getApplDateFormat() , getDBDateFormat())).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
								}
								//System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
								stmt = conn.prepareStatement( selQueryBuff.toString() );
								rs = stmt.executeQuery();
								if (rs.next())
								{
									if (rs.getInt(1) > 0 && editFlag.equals("A"))
									{
										//errCode = "VTDUPREC";
										errString = getErrorString("to_date","VTDUPREC",userId);//,null,conn); 
										break;
									}
								}
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
								if( rs != null )
								{
									rs.close();
									rs = null;
								}
							}
							/*if (errCode.length() !=0 )
							{
								errString = getErrorString("to_date",errCode,userId);
								checkNextCol = false;	
							}*/
						}
					}
					columnValue = genericUtility.getColumnValue("item_ser",dom);					
					if(columnValue != null) 
					{
						if (!columnValue.equals("") && checkNextCol)
						{
							//changed for dynamic binding
							//sqlCnt = "SELECT NVL(COUNT(*),0) FROM ITEMSER WHERE ITEM_SER = '" + columnValue + "'";
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM ITEMSER WHERE ITEM_SER = ? ";
							//System.out.println("sqlCnt :\n"+sqlCnt);
							stmt = conn.prepareStatement( sqlCnt );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1) == 0)
								{
									//errCode = "VTITMSER1";
									errString = getErrorString("item_ser","VTITMSER1",userId);//,null,conn); 
									break;
								}
								else
								{
									custCode = genericUtility.getColumnValue("cust_code",dom);
									itemSer = genericUtility.getColumnValue("item_ser",dom);
									//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][Start]
									String serSpecificCust = "";
									DistCommon distCommon = new DistCommon();
									serSpecificCust = distCommon.getDisparams("999999", "SER_SPECIFIC_CUST", conn);
									if("Y".equalsIgnoreCase(serSpecificCust)) 
									{
										//Modified by Anjali R. on [20/02/2019][Customer series validation Will call upon disparam variable value.][End]
										
									selQueryBuff = new StringBuffer();
									//selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMER_SERIES WHERE CUST_CODE = '");
									//selQueryBuff.append(custCode).append("' AND ITEM_SER = '");
									//selQueryBuff.append(itemSer).append("'");
									selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMER_SERIES WHERE CUST_CODE = ? ");
									selQueryBuff.append(" AND ITEM_SER = ?");
									
									//System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
									stmt2 = conn.prepareStatement( selQueryBuff.toString() );
									stmt2.setString( 1, custCode );
									stmt2.setString( 2, itemSer );
									
									rs2 = stmt2.executeQuery();
									if (rs2.next())
									{
										if (rs2.getInt(1) == 0)
										{
											//errCode = "VTITEMSER4";
											errString = getErrorString("item_ser","VTITEMSER4",userId);//,null,conn); 
											break;
										}
									}
									}
									if( rs2 != null )
									{
										rs2.close();
										rs2 = null;
									}
									if (stmt2 != null)
									{
										stmt2.close();
										stmt2 = null;
									}
								}
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
							}
							/*if (errCode.length() !=0 )
							{
								errString = getErrorString("item_ser",errCode,userId);
								checkNextCol = false;
							}*/
						}
					}
					/*Case "sch_date__1","sch_date__2","sch_date__3","sch_date__4","sch_date__5" ,"sch_date__6" 
					ldt_schdt = dw_edit.getitemdatetime(1,fldname)
					mdate1 = datetime(today(),time(00:00:00))

					if (not isnull(ldt_schdt)) then
						if ldt_schdt < mdate1 or ldt_schdt > gf_lastdate(mdate1)then
							errcode = 'VTINVSCHDT'
						end if
					end if*/
					columnValue = genericUtility.getColumnValue("sch_date__1",dom);					
					if(columnValue != null) 
					{                        
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate = getDateObject( columnValue );							
						if ( schDate.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0 )
						{
							//errCode = "VTINVSCHDT";							
							errString = getErrorString("sch_date__1","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
					columnValue = genericUtility.getColumnValue("sch_date__2",dom);					
				    if(columnValue != null) 
				    {						
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate2 = getDateObject( columnValue );								
						if ( schDate2.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							//errCode = "VTINVSCHDT";
							errString = getErrorString("sch_date__2","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
					columnValue = genericUtility.getColumnValue("sch_date__3",dom);					
				    if(columnValue != null) 
				    {												
						sdfFormat = new SimpleDateFormat(getApplDateFormat());								
						java.util.Date schDate3 = getDateObject( columnValue );							
						if ( schDate3.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							//errCode = "VTINVSCHDT";
							errString = getErrorString("sch_date__3","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
					columnValue = genericUtility.getColumnValue("sch_date__4",dom);					
					if(columnValue != null) 
					{						
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate4 = getDateObject( columnValue );						
						if ( schDate4.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							//errCode = "VTINVSCHDT";
							errString = getErrorString("sch_date__4","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
					columnValue = genericUtility.getColumnValue("sch_date__5",dom);					
					if(columnValue != null) 
					{						
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate5 = getDateObject( columnValue );						
						if ( schDate5.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							//errCode = "VTINVSCHDT";
							errString = getErrorString("sch_date__5","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
					columnValue = genericUtility.getColumnValue("sch_date__6",dom);					
					if(columnValue != null) 
				    {						
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate6 = getDateObject( columnValue );						
						if ( schDate6.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							errString = getErrorString( "sch_date__6", "VTINVSCHDT", userId);//,null,conn); 
							break;
						}
					}
				break;
				case 2:
					errString = "";
					columnValue = genericUtility.getColumnValue("site_code",dom);
					if (columnValue != null) 
					{
						if (!columnValue.equals("") && checkNextCol)
						{
							//changed for dynamic pinding
							//sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = '" + columnValue + "'";
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = ? ";
							stmt = conn.prepareStatement( sqlCnt );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1) == 0)
								{
									/*errCode="VTSITE1";
									errString = getErrorString("site_code",errCode,userId);
									checkNextCol = false;*/
									errString = getErrorString("site_code","VTSITE1",userId);//,null,conn); 
									break;
								}
							}
							if( rs != null )
							{
								rs.close();
								rs = null;
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
						}
					}
					columnValue = genericUtility.getColumnValue("item_code",dom);
					if (columnValue != null) 
					{

						siteCode = genericUtility.getColumnValue("site_code",dom1);
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						tranId = genericUtility.getColumnValue("tran_id",dom1);
						//System.out.println( "siteCode:" + siteCode +"custCode:"+custCode+"columnValue:"+columnValue);
						String itemValSql = "select count(1) cnt from item where item_code = ? ";
						int itemCount = 0;
						stmt = conn.prepareStatement( itemValSql );
						stmt.setString( 1, columnValue );

						rs = stmt.executeQuery();
						if (rs.next())
						{
							itemCount = rs.getInt( "cnt" );
						}
						rs.close();
						rs = null;
						stmt.close();
						rs = null;
						if( itemCount == 0 )
						{
							errString = getErrorString("item_code","VMITEM1",userId);//,null,conn); 
							break;
						}
						if (!columnValue.equals("") && checkNextCol)
						{
							tranDate1 = genericUtility.getColumnValue("tran_date",dom1);
							errCode = "";
							
							errCode = isItem(siteCode,columnValue,"S-CSTK",conn);
							if(errCode.length()>0)
							{
								/*errString = getErrorString("item_code",errCode,userId);
								checkNextCol = false;*/
								errString = getErrorString("item_code",errCode,userId);//,null,conn); 
								// 11/09/09 manoharan to include the item/site code in the log file generated
								sqlCnt = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '" + columnValue + "'";
								stmt = conn.prepareStatement( sqlCnt );
								rs = stmt.executeQuery();
								if (rs.next())
								{
									itemDescr =  rs.getString(1);
								}
								rs.close();
								rs = null;
								stmt.close();
								stmt = null;
								if (itemDescr == null)
								{
									itemDescr = " ";
								}
								else
								{
									itemDescr = " - " + itemDescr;
								}
		 						begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
								endPart = errString.substring( errString.indexOf("</trace>"));
								errString = begPart + " Invalid Item : " + columnValue.toString() + itemDescr + " for Site_code : "+siteCode + " " + endPart;
								begPart =null;
								endPart =null;
								// end 11/09/09 manoharan
								break;
							}
						}
						if (checkNextCol)
						{
						
							//validation for duplicate item in detail start
							if( dom2 != null )
							{
								lineNo = genericUtility.getColumnValue("line_no",dom);
								
								if( isItemDuplicate( columnValue, lineNo, dom2 ) )
								{
									errString = getErrorString("item_code","VTDUPITM",userId);
									break;
								}
							}
							//validation for duplicate item in detail end
							/* // 03/03/10 manoharan duplicate checking already done 
							selQueryBuff = new StringBuffer();
							
							//for dynamic binding 190309
							//selQueryBuff.append("SELECT NVL(COUNT(*),0)	FROM CUST_STOCK_DET, CUST_STOCK ");  
							//selQueryBuff.append("WHERE ( CUST_STOCK.TRAN_ID = CUST_STOCK_DET.TRAN_ID ) AND ");
							//selQueryBuff.append("( ( CUST_STOCK_DET.ITEM_CODE = '").append(columnValue).append("' ) AND "); 
							//selQueryBuff.append("( CUST_STOCK_DET.TRAN_ID = '").append(tranId ).append("') AND  ");
							//selQueryBuff.append("( CUST_STOCK.CUST_CODE = '").append(custCode).append("' ) ) ");
							
							selQueryBuff.append("SELECT NVL(COUNT(*),0)	FROM CUST_STOCK_DET, CUST_STOCK ");  
							selQueryBuff.append("WHERE ( CUST_STOCK.TRAN_ID = CUST_STOCK_DET.TRAN_ID ) AND ");
							selQueryBuff.append("( ( CUST_STOCK_DET.ITEM_CODE = ? ").append(" ) AND "); 
							selQueryBuff.append("( CUST_STOCK_DET.TRAN_ID = ? ").append(" ) AND  ");
							selQueryBuff.append("( CUST_STOCK.CUST_CODE = ? ").append(" ) ) ");
							
							stmt = conn.prepareStatement( selQueryBuff.toString() );
							stmt.setString( 1, columnValue );
							stmt.setString( 2, tranId );
							stmt.setString( 3, custCode );

							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1) == 1)
								{
									selQueryBuff = new StringBuffer();
									//selQueryBuff.append("SELECT LINE_NO FROM CUST_STOCK_DET, CUST_STOCK WHERE ( CUST_STOCK.TRAN_ID = CUST_STOCK_DET.TRAN_ID ) AND  ");
									//selQueryBuff.append("( ( CUST_STOCK_DET.ITEM_CODE = '").append(columnValue).append("' ) AND "); 
									//selQueryBuff.append("( CUST_STOCK_DET.TRAN_ID = '").append(tranId).append("' ) AND  ");
									//selQueryBuff.append("( CUST_STOCK.CUST_CODE = '").append(custCode).append("' ) ) ");
									selQueryBuff.append("SELECT LINE_NO FROM CUST_STOCK_DET, CUST_STOCK WHERE ( CUST_STOCK.TRAN_ID = CUST_STOCK_DET.TRAN_ID ) AND  ");
									selQueryBuff.append("( ( CUST_STOCK_DET.ITEM_CODE = ? ").append(" ) AND "); 
									selQueryBuff.append("( CUST_STOCK_DET.TRAN_ID = ? ").append(" ) AND  ");
									selQueryBuff.append("( CUST_STOCK.CUST_CODE = ? ").append(" ) ) ");
									
									stmt2 = conn.prepareStatement( selQueryBuff.toString() );
									stmt2.setString( 1, columnValue );
									stmt2.setString( 2, tranId );
									stmt2.setString( 3, custCode );									
									
									rs2 = stmt2.executeQuery();
									if (rs2.next())
									{
										lineNo = genericUtility.getColumnValue("line_no",dom);
										//System.out.println("line_no [" + lineNo + "]");
										if (Integer.parseInt(lineNo.trim()) != rs2.getInt(1))
										{
											//errCode = "VTDUPIT";
											errString = getErrorString("line_no","VTDUPIT",userId);//,null,conn); 
											break;
										}
									}
									if( rs2 != null )
									{
										rs2.close();
										rs2 = null;
									}
									if (stmt2 != null)
									{
										stmt2.close();
										stmt2 = null;
									}
								}
								
							}
							if( rs != null )
							{
								rs.close();
								rs = null;
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							//if (errCode.length() !=0 )
							//{
							//	errString = getErrorString("item_code",errCode,userId);
							//	checkNextCol = false;
							//}
							*/ // 03/03/10 manoharan duplicate item checking already done before 
						}
					}
					columnValue = genericUtility.getColumnValue("cl_stock",dom);
					if (columnValue != null)
					{
						itemCode = genericUtility.getColumnValue("item_code",dom);
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						tranDate1 = genericUtility.getColumnValue("tran_date",dom1);
						if (!columnValue.equals("") && checkNextCol)
						{
							//changed for dynamic binding on 190309
							selQueryBuff  = new StringBuffer();
							//selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMERITEM WHERE CUST_CODE = '");
							//selQueryBuff.append(custCode).append("' AND ITEM_CODE = '");
							//selQueryBuff.append(itemCode).append("' ");
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMERITEM WHERE CUST_CODE = ? ");
							selQueryBuff.append(" AND ITEM_CODE = ? ");
							
							stmt = conn.prepareStatement( selQueryBuff.toString() );
							stmt.setString( 1, custCode );
							stmt.setString( 2, itemCode );
							
							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1) > 0)
								{
									selQueryBuff = new StringBuffer();
									//selQueryBuff.append("SELECT RESTRICT_UPTO FROM CUSTOMERITEM	WHERE CUST_CODE = '");
									//selQueryBuff.append(custCode).append("' AND ITEM_CODE = '");
									//selQueryBuff.append(itemCode).append("' ");
									selQueryBuff.append("SELECT RESTRICT_UPTO FROM CUSTOMERITEM	WHERE CUST_CODE = ? ");
									selQueryBuff.append(" AND ITEM_CODE = ? ");
									
									stmt2 = conn.prepareStatement( selQueryBuff.toString() );
									stmt2.setString( 1, custCode );
									stmt2.setString( 2, itemCode );
									
									rs2 = stmt2.executeQuery();
									if (rs2.next())
									{
										restUpto1 = (java.sql.Date)rs2.getDate(1);
										if (restUpto1 != null )
										{
											tranDate2 = getDateObject(tranDate1);
											restUpto2 = new java.util.Date(restUpto1.getTime());
											if (tranDate2.compareTo(restUpto2)<=0)
											{
												//errCode = "VTRESDT";
												errString = getErrorString("cl_stock","VTRESDT",userId);//,null,conn); 
												break;
											}

										}
									}
									if( rs2 != null )
									{
										rs2.close();
										rs2 = null;
									}
									if (stmt2 != null)
									{
										stmt2.close();
										stmt2 = null;
									}
								}
							}
							if( rs != null )
							{
								rs.close();
								rs = null;
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							/*if (errCode.length() !=0 )
							{
								errString = getErrorString("cl_stock",errCode,userId);
								checkNextCol = false;
							}*/
						}
					}
					columnValue = genericUtility.getColumnValue("item_ser",dom1);
					if (columnValue != null) 
					{
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						if (!columnValue.equals("") && checkNextCol)
						{
							selQueryBuff = new StringBuffer();
							//selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMER_SERIES "); 
							//selQueryBuff.append("WHERE CUST_CODE = '").append(custCode);
							//selQueryBuff.append("' AND ITEM_SER = '").append(columnValue).append("'");
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMER_SERIES "); 
							selQueryBuff.append("WHERE CUST_CODE = ? ");
							selQueryBuff.append(" AND ITEM_SER = ? ");
							
							stmt = conn.prepareStatement( selQueryBuff.toString() );
							stmt.setString( 1, custCode );
							stmt.setString( 2, columnValue );
							
							rs = stmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1)==0)
								{
									//errCode = "VTITEM7";
									errString = getErrorString("item_ser","VTITEM7",userId);//,null,conn); 
									break;
									
								}
							}
							if( rs != null )
							{
								rs.close();
								rs = null;
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							/*if (errCode.length() !=0 )
							{
								errString = getErrorString("item_ser",errCode,userId);
								checkNextCol = false;
							}*/
						}
					}
			}
			conn.close();
			conn = null;
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :wfValData :==>\n"+e.getMessage());
			e.printStackTrace();
			errString = getErrorString("item_ser","SYSERR",userId);//,null,conn); 
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
				conn = null;
				}
			}catch(Exception d)
			{
			  d.printStackTrace();
			  throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		//System.out.println( "*****errString********::" + errString );
		return errString == null ? "" : errString ;
	}

	//wfValData(Document dom) method ends here
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

	//public String itemChanged(String xmlString , String currentColumn, String formNo, String domID) throws RemoteException,ITMException
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			// if (xmlString2.trim().length() > 0 )
			// {
				// dom2 = parseString(xmlString2);
			// }
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
        return valueXmlString;
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			//System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			 if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
			 }
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : [SOrderFormEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			
			throw new ITMException(e);
		}
        return valueXmlString;
	}
	//public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String domID = "";//Changed by Danish on 06/02/2007 temporary
		String formNo = "";//Changed by Danish on 06/02/2007 temporary
		//System.out.println("[CustStockEJB]itemChanged called for column name :"+currentColumn+": formNo :"+formNo+": domID :"+domID+":");
		Connection  conn = null;
		boolean checkNextCol = true;
		String columnName = "";
		String columnValue = "";
		StringBuffer valueXmlString = null;
		StringBuffer selQueryBuff = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String selQuery = "";
		//DateFormat df = null;
		String frdate1 = "";
		String todate1 = "";
		String custCodeHeader = "";
		String custCodeDetail = "";
		String siteCode = "";
		String itemSer = "";
		String tranDate1 = "";
		String sql11 = "";
		String sql1 = "";
		String itemDescr = "";
		String locType = "";
		String unit = "";
		String tranIdLast = "";
		String stockMode = "";
		String opStock = "";
		String clStks = "";
		String sales = "";
		String purReceipt = "";
		String purReturn = "";
		String adjQty = "";
		//String editFlag = "";//Changed by Danish on 06/02/2007
		String custName = "";
		String siteDescr = "";
		String tranDate = "";
		String month = "";
		int year =0 ;
		String firstDate = "";
		String lastDate = "";
		java.util.Date trnDate = null;
		double saless = 0;
		double clStk = 0;
		int currentFormNo = 0;
		//int mon = 0;
		//int days =0;

		float rateValue = 0f;
		String opValue = "";
		String purValue = "";
		String clValue = "";
		String salesValue = "";
		String dbPattern=null;
		String invStat = "";
		String itemCode = "", purcRcp = null, purcRet = null, prdCode, transitFlg = null;
		double retQty = 0, replQty = 0, quantity = 0, transitQty = 0;
		//Changed by Dayanand on 01/01/08 [Declare transQty]
		String transQty ="", purcQty = null;
		//Changed by Dayanand on 3/2/2009[ Declared obj_name req id=WS89SUN078]start
		String objName = "";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String custCode = null,rSiteCode = null, locCode = null, lotNo = null, lotSl = null, retRepFlag = null;
		java.sql.Timestamp preFromDate = null, preToDate = null;
		java.sql.Timestamp invFromDate = null, invToDate = null;
		ibase.webitm.ejb.sys.UtilMethods utilMethods = null;
		String siteCodeCh ="";
		
		try
		{
			//ibase.webitm.utility.GenericUtility genericUtility = ibase.webitm.utility.GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();

			conn = getConnection();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
				formNo = objContext;
			}

			//currentFormNo = Integer.parseInt(formNo);			//Changed by Danish on 06/02/2007
			if (currentColumn.equalsIgnoreCase("itm_default") || currentColumn.equalsIgnoreCase("itm_defaultedit"))
			{
				columnValue = currentColumn;
			}
			else if (currentColumn.equalsIgnoreCase("op_stock") || currentColumn.equalsIgnoreCase("cl_stock")
					|| currentColumn.equalsIgnoreCase("purc_rcp")
					|| currentColumn.equalsIgnoreCase("purc_ret")
					|| currentColumn.equalsIgnoreCase("adj_qty"))
			{
				columnValue = genericUtility.getColumnValue(currentColumn, dom );//, formNo, domID);
			}
			else
			{
				columnValue = genericUtility.getColumnValue(currentColumn, dom );//, formNo);
			}
			//System.out.println("columnValue :"+columnValue+":");
			//Changed by dayanand on 2/28/2009[ Take obj_name from dom and form no If Detail 3 than set form no = 2 (For UploadFile ) req id=WS89SUN078]start			
			objName = getObjNameFromDom(dom,"objName");			
			if ( objName.equals("cust_stock") && currentFormNo == 3 )
			{
				currentFormNo = 2;
			}
			//Changed by dayanand on 2/28/2009[  Take obj_name from dom and form no If Detail 3 than set form no = 2 (For UploadFile ) req id=WS89SUN078]end
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
			if (columnValue != null)
			{
				switch (currentFormNo)
				{
					case 1:
						valueXmlString.append("<Detail>\r\n");					    
					
						if (currentColumn.trim().equals("itm_default"))
						{
							//System.out.println("Setting itm defualt values...");

							/*ls_login_site = gbf_get_argval(is_extra_arg,"site_code")  
							dw_edit.setitem(1,"site_code",ls_login_site) */
							String loginSite = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
							//if( loginSite == null || (loginSite != null  && loginSite.length() == 0))
							//{
							//	loginSite=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"entityCode");
							//}
							
							valueXmlString.append( "<site_code>" ).append(loginSite).append( "</site_code>\r\n" );								
							/* select descr into :msite_desc from site where site_code = :ls_login_site;
							dw_edit.setitem(1,"descr",msite_desc) */
							//for dynamic binding on 190309
							//selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + loginSite + "'";
							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, loginSite );
							rs = stmt.executeQuery();
							if (rs.next())
							{								
								valueXmlString.append("<descr>").append( rs.getString(1) ).append("</descr>\r\n");
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							/* dw_edit.setitem (1, "tran_date", ldt_today)
							dw_edit.setitem (1, "from_date", ldt_today)
							dw_edit.setitem ( 1, "to_date", ldt_today)  */
							java.sql.Date currentDate = new java.sql.Date( System.currentTimeMillis() );
							java.text.SimpleDateFormat dtf = new SimpleDateFormat( getApplDateFormat() );			            						                        
							valueXmlString.append( "<tran_date>" ).append( dtf.format( currentDate ) ).append( "</tran_date>\r\n" );
							valueXmlString.append( "<from_date>" ).append( dtf.format( currentDate ) ).append( "</from_date>\r\n" );
							valueXmlString.append( "<to_date>" ).append( dtf.format( currentDate ) ).append( "</to_date>\r\n" );
							//for dynamic binding on 190309
							//selQuery = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER = '" + genericUtility.getColumnValue("item_ser",dom1) + "'";
							selQuery = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, genericUtility.getColumnValue( "item_ser", dom1 ) ); 
							rs = stmt.executeQuery();
							if (rs.next())
							{
								itemDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							valueXmlString.append("<itemser_descr>").append(itemDescr).append("</itemser_descr>\r\n");
							valueXmlString.append("<tran_type>H</tran_type>\r\n");
							valueXmlString.append("<tot_op_value>0.0</tot_op_value>\r\n");
							valueXmlString.append("<tot_sales_value>0.0</tot_sales_value>\r\n");
							valueXmlString.append("<tot_pur_value>0.0</tot_pur_value>\r\n");
							valueXmlString.append("<tot_cl_value>0.0</tot_cl_value>\r\n");
							valueXmlString.append("<confirmed>N</confirmed>\r\n");
							//dw_edit.setitem (1, "sch_date__1", ldt_today)
							valueXmlString.append("<sch_date__1>").append( dtf.format( currentDate )).append("</sch_date__1>\r\n");
							//dw_edit.setitem (1, "sch_perc__1", 100)
							valueXmlString.append("<sch_perc__1>100</sch_perc__1>\r\n");

							/*
							select code into :ls_prd_code from period where fr_date <= :ldt_today and to_date >= :ldt_today ;
							dw_edit.setitem (1, "prd_code", ls_prd_code)	
							*/
							//dbPattern = CommonConstants.DB_DATE_FORMAT;
							java.text.SimpleDateFormat dtfo = new SimpleDateFormat( "dd-MMM-yyyy" );
							String currDate = dtfo.format( currentDate ); 
							//for dynamic binding 190309
							//selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= '" + currDate + "' AND TO_DATE >= '"+ currDate+"' ";
							selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= ? AND TO_DATE >= ? ";
							
							stmt = conn.prepareStatement( selQuery );
							stmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
							stmt.setTimestamp( 2, new java.sql.Timestamp( System.currentTimeMillis() ) );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								valueXmlString.append("<prd_code>").append( rs.getString(1) ).append("</prd_code>\r\n");
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							/* // 04/06/09 manoharan set cust_stockmode
							selQuery = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'CUST_STOCK_MODE'";
							//System.out.println("Executing Query .............\n" +selQuery);
							stmt = conn.prepareStatement( selQuery );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								stockMode = rs.getString(1);
							}
							else
							{
								stockMode = "S";
							}
							rs.close();
							rs = null;
							valueXmlString.append("<cust_stock_mode>").append( stockMode ).append("</cust_stock_mode>\r\n");
							// end 04/06/09 manoharan set cust_stockmode*/

						}
						if (currentColumn.trim().equals("itm_defaultedit"))
						{
							String custCodeStr = genericUtility.getColumnValue("cust_code",dom);
							String siteCodeStr = genericUtility.getColumnValue("site_code",dom);
							String tranDateStr = genericUtility.getColumnValue("tran_date",dom);
							String tranIdLastStr = genericUtility.getColumnValue("tran_id__last",dom);
							String fromDateStr = genericUtility.getColumnValue("from_date",dom);
							String toDateStr = genericUtility.getColumnValue("to_date",dom);
							String prdCodeStr = genericUtility.getColumnValue("prd_code",dom);
							String itemSerStr = genericUtility.getColumnValue("item_ser",dom);
							String remarksStr = genericUtility.getColumnValue("remarks",dom);
							String tranTypeStr = genericUtility.getColumnValue("tran_type",dom);
							

							valueXmlString.append("<tran_date protect = '1'>").append( tranDateStr).append("</tran_date>");
							valueXmlString.append("<cust_code protect = '1'>").append( custCodeStr ).append("</cust_code>");
							valueXmlString.append("<site_code protect = '1'>").append( siteCodeStr ).append("</site_code>");
							valueXmlString.append("<from_date protect = '1'>").append( fromDateStr ).append("</from_date>");
							valueXmlString.append("<to_date protect = '1'>").append( toDateStr ).append("</to_date>");

							valueXmlString.append("<prd_code protect = '1'>").append( prdCodeStr ).append("</prd_code>");
							valueXmlString.append("<item_ser protect = '1'>").append( itemSerStr ).append("</item_ser>");
							valueXmlString.append("<remarks protect = '1'>").append( ( remarksStr == null || remarksStr.equalsIgnoreCase("null") )?"":remarksStr ).append("</remarks>");
							valueXmlString.append("<tran_type protect = '1'>").append( tranTypeStr ).append("</tran_type>");
							
						}
						else if ( currentColumn.trim().equals("cust_code") )						
						{ 
							frdate1 = genericUtility.getColumnValue("from_date",dom1);
							siteCode = genericUtility.getColumnValue("site_code",dom1);
							//for dynamic binding on 190309
							//selQuery = "SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = '" + columnValue + "'";
							selQuery = "SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								custName = rs.getString(1);
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}

							valueXmlString.append("<name><![CDATA[").append(custName).append("]]></name>\r\n");

							//for dynamic binding 190309
							//selQuery = "SELECT SITE_CODE FROM SITE_CUSTOMER WHERE CUST_CODE = '" + columnValue + "'";
							selQuery = "SELECT SITE_CODE FROM SITE_CUSTOMER WHERE CUST_CODE = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								siteCode = rs.getString(1);
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							//Changed by dayanand on [10-09-07]
							//valueXmlString.append("<site_code>").append(siteCode).append("</site_code>\r\n");
							//for dynamic binding on 190309
							//selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + siteCode + "'";
							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
							
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, siteCode );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								siteDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							//Changed by dayanand on [10-09-07]
							//valueXmlString.append("<descr>").append(siteDescr).append("</descr>\r\n");
							
							if (frdate1.trim().equals(""))
							{
								selQueryBuff = new StringBuffer();
								// for dynamic binding on 190309
								//selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK WHERE CUST_CODE = '");
								//selQueryBuff.append(columnValue).append("' AND SITE_CODE = '");
								//selQueryBuff.append(siteCode).append("'");
								selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK WHERE CUST_CODE = ? ");
								selQueryBuff.append(" AND SITE_CODE = ? ");
								
								stmt = conn.prepareStatement( selQueryBuff.toString() );
								stmt.setString( 1, columnValue );
								stmt.setString( 2, siteCode );
								
								rs = stmt.executeQuery();
								if (rs.next())
								{
									frdate1 = String.valueOf(rs.getDate(1));
								}
								rs.close();
								rs = null;
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
							}
							valueXmlString.append("<from_date>").append(frdate1).append("</from_date>\r\n");

							itemSer = genericUtility.getColumnValue("item_ser",dom1);
							if (itemSer == null || itemSer.equalsIgnoreCase("null") || itemSer.length() == 0)
							{
								selQueryBuff = new StringBuffer();
								//for dynamic binding on 190309
								//selQueryBuff.append("SELECT ITEM_SER FROM CUSTOMER_SERIES ");
								//selQueryBuff.append("WHERE CUST_CODE = '").append(columnValue);
								//selQueryBuff.append("' AND ROWNUM = 1");
								//Changed by dayanand 3/2/2009[ Not Closing the sql properly req id =WS89SUN078]start
								//selQueryBuff.append("'");
								//Changed by dayanand 3/2/2009[ Not Closing the sql properly req id =WS89SUN078]start
								
								selQueryBuff.append("SELECT ITEM_SER FROM CUSTOMER_SERIES ");
								selQueryBuff.append(" WHERE CUST_CODE = ? ");
								//Changed by dayanand 3/2/2009[ Not Closing the sql properly req id =WS89SUN078]start
								//selQueryBuff.append("' AND ROWNUM = 1");								
								//Changed by dayanand 3/2/2009[ Not Closing the sql properly req id =WS89SUN078]start

								stmt = conn.prepareStatement( selQueryBuff.toString() );
								stmt.setString( 1, columnValue );
								rs = stmt.executeQuery();
								if (rs.next())
								{
									itemSer = rs.getString(1);
								}
								rs.close();
								rs = null;
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
							}
							valueXmlString.append("<item_ser>").append(itemSer).append("</item_ser>\r\n");

							//SHWETA 2/14/2005 --Start
							tranIdLast = genericUtility.getColumnValue("tran_id__last",dom1);
							//System.out.println("tran_id__last :"+tranIdLast+":");
							if (tranIdLast == null || tranIdLast.equalsIgnoreCase("null") || tranIdLast.length() == 0)
							{
								if (itemSer.trim().length() > 0 )
								{
									//for dynamic binding on 190309
									selQueryBuff = new StringBuffer();
									//selQueryBuff.append("SELECT MAX(TRAN_ID) FROM CUST_STOCK WHERE CUST_CODE = '");
									//selQueryBuff.append(columnValue).append("' AND ITEM_SER = '");
									//selQueryBuff.append(itemSer).append("'");
									selQueryBuff.append("SELECT MAX(TRAN_ID) FROM CUST_STOCK WHERE CUST_CODE = ? ");
									selQueryBuff.append(" AND ITEM_SER = ? ");
									
									stmt = conn.prepareStatement( selQueryBuff.toString() );
									stmt.setString( 1, columnValue );
									stmt.setString( 2, itemSer );
								}
								else
								{
									selQueryBuff = new StringBuffer();
									//for dynamic binding on 190309
									//selQueryBuff.append("SELECT MAX(TRAN_ID) FROM CUST_STOCK WHERE CUST_CODE = '");
									//selQueryBuff.append(columnValue).append("'");
									
									selQueryBuff.append("SELECT MAX(TRAN_ID) FROM CUST_STOCK WHERE CUST_CODE = ? ");

									stmt = conn.prepareStatement( selQueryBuff.toString() );
									stmt.setString( 1, columnValue );
								}
								//System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
								
								rs = stmt.executeQuery();
								if (rs.next())
								{
									tranIdLast = rs.getString(1);
								}
								rs.close();
								rs = null;
								//System.out.println("tranIdLast :"+tranIdLast);
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
							}
							valueXmlString.append("<tran_id__last>").append(tranIdLast).append("</tran_id__last>\r\n");
							//(SHWETA) ???????????????
							/*if (columnValue.length()>0 && siteCode.length()>0)
							{
								itemSer = getColumnValue("item_ser",dom);
								if (itemSer == null || itemSer.equalsIgnoreCase("null") || itemSer.length() == 0)
								{
									itemSer = "%";
								}
								else
								{
									itemSer = itemSer + "%";
								}
							}*/
						}
						else if (currentColumn.trim().equals("site_code"))
						{
							frdate1 = genericUtility.getColumnValue("from_date",dom1);
							custCodeHeader = genericUtility.getColumnValue("cust_code",dom1);
							//for dynamic binding on 190309
							//selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + columnValue + "'";
							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								siteDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}

							valueXmlString.append("<descr><![CDATA[").append(siteDescr).append("]]></descr>\r\n");
							if (frdate1.trim().equals(""))
							{
								selQueryBuff = new StringBuffer();
								//for dynamic binding on 190309
								//selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK ");
								//selQueryBuff.append("WHERE CUST_CODE = '").append(custCodeHeader);
								//selQueryBuff.append("' AND SITE_CODE = '").append(columnValue).append("'");
								
								selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK ");
								selQueryBuff.append("WHERE CUST_CODE = ? ");
								selQueryBuff.append(" AND SITE_CODE = ? ");
								
								//System.out.println("selQueryBuff :"+selQueryBuff.toString());
								stmt = conn.prepareStatement( selQueryBuff.toString() );
								stmt.setString( 1, custCodeHeader );
								stmt.setString( 2, columnValue );
								
								rs = stmt.executeQuery();
								if (rs.next())
								{
									frdate1 = String.valueOf(rs.getDate(1));
								}
								rs.close();
								rs = null;
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
							}
							valueXmlString.append("<from_date>").append(frdate1).append("</from_date>\r\n");								
						}
						else if (currentColumn.trim().equals("tran_date"))
						{
							tranDate = genericUtility.getColumnValue("tran_date",dom1);
							trnDate = getDateObject(tranDate);
							Calendar calendar = new GregorianCalendar();
							calendar.setTime(trnDate);
							//mon = calendar.get(Calendar.MONTH);
							//year = calendar.get(Calendar.YEAR);
							columnValue = genericUtility.getValidDateString( columnValue , getApplDateFormat() , "dd-MMM-yyyy" );
							/*  Commented By Chandni As Date is not setting Properly 19-Apr-2012
							if (getApplDateFormat().equalsIgnoreCase("dd-MM-yyyy"))
							{
								if (mon <= 8)
								{
									firstDate = "01" + "-0" + (mon+1) + "-" + year;
								}
								else
								{
									firstDate = "01" + "-" + (mon+1) + "-" + year;
								}
							}
							else if (getApplDateFormat().equalsIgnoreCase("dd-MMM-yyyy"))
							{
								firstDate = "01" + "-" + tranDate.substring(3, 6) + "-" + year;
							}
							if (mon == 0 || mon == 2 || mon == 4 || mon == 6 || mon == 7 || mon == 9 || mon == 11)
							{
								days = 31;
							}
							else if (mon == 3 || mon == 5 || mon == 8 || mon == 10)
							{
								days = 30;
							}
							else if(mon == 1)
							{
								if ((year%4)==0)
								{
									days = 29;
								}
								else
								{
									days = 28;
								}
							}
							if (getApplDateFormat().equalsIgnoreCase("dd-MM-yyyy"))
							{
								if (mon <= 8)
								{
									lastDate = days + "-0" + (mon+1) + "-" + year;
								}
								else
								{
									lastDate = days + "-" + (mon+1) + "-" + year;
								}
							}
							else if (getApplDateFormat().equalsIgnoreCase("dd-MMM-yyyy"))
							{
								lastDate = days + "-" + tranDate.substring(3, 6) + "-" + year;
							}
							*/
							/* Commented By Chandni As Date is not setting Properly
							valueXmlString.append("<tran_date>").append(lastDate).append("</tran_date>\r\n");	
							valueXmlString.append("<from_date>").append(firstDate).append("</from_date>\r\n");	
							valueXmlString.append("<to_date>").append(lastDate).append("</to_date>\r\n");			
							Comment Ended*/
							//Added by chandni 19-Apr-2012
							calendar.setTime(trnDate);
							calendar.set(Calendar.DAY_OF_MONTH,1);
							System.out.println("getApplDateFormat()----"+getApplDateFormat());
							java.text.SimpleDateFormat dtf = new SimpleDateFormat( getApplDateFormat() );
							valueXmlString.append("<from_date>").append(dtf.format( calendar.getTime() )).append("</from_date>\r\n");
							int ld = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
							calendar.set(Calendar.DAY_OF_MONTH,ld);
						        System.out.println("Date     : " + calendar.getTime());
						        System.out.println("Last Date: " + lastDate);
							valueXmlString.append("<tran_date>").append(dtf.format( calendar.getTime() )).append("</tran_date>\r\n");	
								
							valueXmlString.append("<to_date>").append(dtf.format( calendar.getTime() )).append("</to_date>\r\n");
							
							/*select code into :ls_prd_code from period 
							where fr_date <= :ld_tran_date and to_date >= :ld_tran_date ;
							dw_edit.setitem (1, "prd_code", ls_prd_code)*/	
							//Changed by Dayanand on 8/25/2008 [comment Take code as fr date bassis]START REQID:-DI89SUN082
							/*selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= '" +columnValue+ "' AND TO_DATE >= '" +columnValue+ "' ";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{
								valueXmlString.append("<prd_code>").append(rs.getString(1)).append("</prd_code>\r\n");
							}*/
							//Changed by Dayanand on 8/25/2008 [comment Take code as fr date bassis]END REQID:-DI89SUN082
						}
						else if (currentColumn.trim().equals("from_date"))
						{							
							/*mcustcode = dw_edit.getitemstring(1,"cust_code")
							ls_item_ser = dw_edit.getitemstring(1,"item_ser")
							mprev_date = datetime(relativedate(date(ld_from_date),-1))*/
							custCode = genericUtility.getColumnValue( "cust_code" , dom1);
							String itemSeries = genericUtility.getColumnValue( "item_ser" , dom1);
							Calendar preCalc = Calendar.getInstance();
							preCalc.setTime( getDateObject( columnValue ) );
							preCalc.add( Calendar.DATE, -1 );							
							java.util.Date prvDate = preCalc.getTime();															
							java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );
							String currDate = dtf.format( prvDate );							
							/*select tran_id into :mtran_id_last from cust_stock
							where cust_code = :mcustcode
							and	to_date = :mprev_date ;
							if isnull(mtran_id_last) then mtran_id_last = ''
							dw_edit.Setitem(1, "tran_id__last", mtran_id_last)*/

							//for dynamic binding on 190309
							//selQuery = "SELECT TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = '" +custCode+ "' AND TO_DATE = '" +currDate+ "' ";
							selQuery = "SELECT TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = ? AND TO_DATE = ? ";
							//System.out.println("selQuery "+selQuery);
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, custCode );
							stmt.setString( 2, currDate );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								valueXmlString.append("<tran_id__last>").append(rs.getString(1)).append("</tran_id__last>\r\n");
							}
							else
							{
								valueXmlString.append( "<tran_id__last></tran_id__last>\r\n" );
							}
							rs.close();
							rs = null;
							stmt.close();
							stmt = null;
							//Changed by Dayanand on 8/25/2008 [Set period code in header as from from date]start (reqId DI89SUN082)
							//next line commented and changed by alam on 190309 to set it dynamiclly
							//columnValue = genericUtility.getValidDateString( columnValue , getApplDateFormat() , "dd-MMM-yyyy" );
							//next query changed by alam on 190309 to bind variables dynamically
							//selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= '" +columnValue+ "' AND TO_DATE >= '" +columnValue+ "' ";
							selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= ? AND TO_DATE >= ? ";
							
							stmt = conn.prepareStatement( selQuery );
							stmt.setTimestamp( 1, java.sql.Timestamp.valueOf( genericUtility.getValidDateString( columnValue , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" ) );
							stmt.setTimestamp( 2, java.sql.Timestamp.valueOf( genericUtility.getValidDateString( columnValue , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" ) );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								valueXmlString.append("<prd_code>").append(rs.getString(1)).append("</prd_code>\r\n");
							}
							rs.close();
							rs = null;
							stmt.close();
							stmt = null;
							//Changed by Dayanand on 8/25/2008 []end
						}
						else if (currentColumn.trim().equals("item_ser"))
						{
							//for dynamic binding 190309
							//selQuery = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER = '" + columnValue + "'";
							selQuery = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, columnValue );
							rs = stmt.executeQuery();
							if (rs.next())
							{
								itemDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							valueXmlString.append("<itemser_descr>").append(itemDescr).append("</itemser_descr>\r\n");
						}
						valueXmlString.append("</Detail>\r\n");
					break;
					case 2: 

						String adhocReplQty = null;
						String transitQtyStr = null;

						valueXmlString.append("<Detail>\r\n");
						selQuery = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'CUST_STOCK_MODE'";
						//System.out.println("Executing Query .............\n" +selQuery);
						stmt = conn.prepareStatement( selQuery );
						rs = stmt.executeQuery();
						if (rs.next())
						{
							stockMode = rs.getString(1);
						}
						else
						{
							stockMode = "S";
						}
						rs.close();
						rs = null;
						if (stmt != null)
						{
							stmt.close();
							stmt = null;
						}
						opStock = "0";
						clStks = "0";
						sales = "0";
						purReceipt = "0";
						adjQty = "0";
						purReturn = "0";
						
						//ibase.utility.GenericUtility gu = new ibase.utility.GenericUtility();
						//Changed by Dayanand on 8/25/2008 [Comment The SOP For speed reason]START
						////System.out.println(" dom " + genericUtility.serializeDom( dom ) );
						////System.out.println(" dom1 " + genericUtility.serializeDom( dom1 ) );
						////System.out.println(" dom2 " + genericUtility.serializeDom( dom2 ) );
						//Changed by Dayanand on 8/25/2008 [Comment The SOP For speed reason]END
						
						//purReceipt = getColumnValue("purc_rcp",dom, formNo, domID);
						//adjQty = getColumnValue("adj_qty",dom, formNo, domID);
						//purReturn = getColumnValue("purc_ret",dom, formNo, domID);
													
						purReceipt = genericUtility.getColumnValue("purc_rcp",dom);//, formNo);
						adjQty = genericUtility.getColumnValue("adj_qty",dom);//, formNo);
						purReturn = genericUtility.getColumnValue("purc_ret",dom);//, formNo);
						
												
						//rateValue = getRate( getColumnValue("cust_code", dom), getColumnValue("tran_date", dom), getColumnValue("item_code",dom, formNo, domID));						
						//System.out.println("Calculating rate for cust_code :"+getColumnValue("cust_code", dom1)+": tran_date :"+getColumnValue("tran_date", dom1)+": item_code :"+genericUtility.getColumnValue("item_code",dom)+":");
						rateValue = getRate( genericUtility.getColumnValue("cust_code", dom1), genericUtility.getColumnValue("tran_date", dom1), genericUtility.getColumnValue("item_code",dom));
						//System.out.println("Assigned value from rateValue getRate function:"+rateValue);						
                        //Changed by Dayanand on 21/11/07 [Add new field cust_item__ref and add item_code itemchange in this field]start
						////Added to add new elseif option 'A' to Unprotect cl_stock column, Ruchira 22/02/2k7(DI7SUN0038).
						
						//next line changed by msalam on  190309 as itemdefaultedit was making values 0 for line except first line
						//if ( currentColumn.trim().equalsIgnoreCase("itm_default") || currentColumn.trim().equalsIgnoreCase("itm_defaultedit") )
						if ( currentColumn.trim().equalsIgnoreCase("itm_default") )
						{	
							//for monif to catch exception in frame work
							//String str = null;
							//System.out.println( "Str :: " + str.length() );
							//for monif to catch exception in frame work

							if (stockMode.trim().equals("C"))
							{
								valueXmlString.append("<cl_stock protect = '0'>").append("0").append("</cl_stock>");
								valueXmlString.append("<sales protect = '1'>").append("0").append("</sales>");
								valueXmlString.append("<purc_rcp protect = '1'>").append("0").append("</purc_rcp>");
								valueXmlString.append("<transit_qty protect = '1'>").append("0").append("</transit_qty>");
								valueXmlString.append("<adj_qty protect = '1'>").append("0").append("</adj_qty>");
								valueXmlString.append("<purc_ret protect = '1'>").append("0").append("</purc_ret>");
								
							}
							else if (stockMode.trim().equals("A"))
							{
								valueXmlString.append("<cl_stock protect = '0'>").append("0").append("</cl_stock>");
								valueXmlString.append("<sales protect = '0'>").append("0").append("</sales>");
								valueXmlString.append("<purc_rcp protect = '0'>").append("0").append("</purc_rcp>");
								valueXmlString.append("<transit_qty protect = '0'>").append("0").append("</transit_qty>");
								valueXmlString.append("<adj_qty protect = '0'>").append("0").append("</adj_qty>");
								valueXmlString.append("<purc_ret protect = '0'>").append("0").append("</purc_ret>");
							}
							else 
							{
								valueXmlString.append("<cl_stock protect = '1'>").append("0").append("</cl_stock>");
								valueXmlString.append("<sales protect = '0'>").append("0").append("</sales>");
								valueXmlString.append("<purc_rcp protect = '0'>").append("0").append("</purc_rcp>");
								valueXmlString.append("<transit_qty protect = '0'>").append("0").append("</transit_qty>");
								valueXmlString.append("<adj_qty protect = '0'>").append("0").append("</adj_qty>");
								valueXmlString.append("<purc_ret protect = '0'>").append("0").append("</purc_ret>");
							}
						////End Added to add new elseif option 'A' to Unprotect cl_stock column, Ruchira 22/02/2k7(DI7SUN0038).				
						}
						else if ( currentColumn.trim().equalsIgnoreCase("cust_item__ref") )
						{							
							//System.out.println("Enter in cust_item_code_ref itemchange");
							custCode = genericUtility.getColumnValue("cust_code" , dom1);
							//changed for dynamic binding
							//selQuery = "SELECT ITEM_CODE FROM CUSTOMERITEM WHERE  ITEM_CODE__REF = '"+columnValue+"' AND CUST_CODE='"+custCode+"'";
							selQuery = "SELECT ITEM_CODE FROM CUSTOMERITEM WHERE  ITEM_CODE__REF = ? AND CUST_CODE = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, columnValue );
							//stmt.setString( 1, custCode ); 
							stmt.setString( 2, custCode ); // changes done by chandni 21-mar-2012
							//System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery();
							if (rs.next())
							{
								itemCode = rs.getString("ITEM_CODE");
								//System.out.println("rs.getString(ITEM_CODE)["+rs.getString("ITEM_CODE")+"]");

								valueXmlString.append("<item_code><![CDATA[").append(itemCode).append("]]></item_code>\r\n");
							}
							else
							{
								valueXmlString.append( "<item_code></item_code>\r\n" );
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}							
							siteCode = genericUtility.getColumnValue("site_code" , dom1);
							tranDate = genericUtility.getColumnValue("tran_date" , dom1 );
							tranIdLast = genericUtility.getColumnValue("tran_id__last" ,dom1);	
							//System.out.println("itemCode [" +itemCode+ "]cust_code["+custCode+"]siteCode["+siteCode+"]tranDate["+tranDate+"]tranIdLast["+tranIdLast+"]");							
				
							//for dynamic binding on 190309
							//selQuery = "SELECT DESCR, UNIT, LOC_TYPE, ITEM_SER FROM ITEM WHERE  ITEM_CODE = '"+itemCode+"'";
							selQuery = "SELECT DESCR, UNIT, LOC_TYPE, ITEM_SER FROM ITEM WHERE  ITEM_CODE = ? ";
							stmt = conn.prepareStatement( selQuery );
							stmt.setString( 1, itemCode );
							//System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery();
							if (rs.next())
							{
								unit = rs.getString("UNIT");
								//System.out.println("unit [" +unit+ "]");
							    locType = rs.getString("LOC_TYPE");
								itemSer = rs.getString("ITEM_SER");
								valueXmlString.append("<item_ser>").append(itemSer).append("</item_ser>\r\n");
								valueXmlString.append("<unit>").append(unit).append("</unit>\r\n");
								valueXmlString.append("<loc_type>").append(locType).append("</loc_type>\r\n");
							}
							else
							{
								valueXmlString.append("<item_ser>").append(opStock).append("</item_ser>\r\n");
								valueXmlString.append("<unit>").append(opStock).append("</unit>\r\n");
								valueXmlString.append("<loc_type>").append(opStock).append("</loc_type>\r\n");
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}

							//TAKING PREV TRANSACTION CLOSING BALANCE 
							//for dynamic binding on 190309
							//selQuery = "SELECT CL_STOCK FROM CUST_STOCK_DET WHERE  TRAN_ID = '"+tranIdLast+"'	AND ITEM_CODE = '"+itemCode+"'";
							selQuery = "SELECT CL_STOCK FROM CUST_STOCK_DET WHERE  TRAN_ID = ? AND ITEM_CODE = ? ";							
							stmt = conn.prepareStatement( selQuery );
							
							stmt.setString( 1, tranIdLast );
							stmt.setString( 2, itemCode );
							
							rs = stmt.executeQuery();
							if (rs.next())
							{
								opStock = rs.getString("CL_STOCK");
								//System.out.println( "opStock [" +opStock+ "]");
								valueXmlString.append("<op_stock>").append(opStock).append("</op_stock>\r\n");
							}
							else
							{
								valueXmlString.append( "<op_stock>0</op_stock>\r\n" );
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}

							String ldtFromDate = genericUtility.getColumnValue("from_date" , dom1);
							//System.out.println("ldtFromDate [" +ldtFromDate+ "]");
							String toDate = genericUtility.getColumnValue("to_date" , dom1);
							//System.out.println("toDate [" +toDate+ "]");

							//String ldt_to_date = Calendar(ldt_from_date).add(Date,-1);
							//Calendar preCalc = Calendar.getInstance();
							//preCalc.setTime( getDateObject( ldtFromDate ) );
							//preCalc.add( Calendar.DATE, -1 );				
							//java.util.Date ldtToDate = preCalc.getTime();	
							//java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );						    
							//String currDate = dtf.format( ldtToDate );	
							////System.out.println("currDate [" +currDate+ "]");
							//String ldtFromDate = dtf.format( ldFromDate );
							//next line commented by msalam on 190309 as not required
							//ldtFromDate = genericUtility.getValidDateString( ldtFromDate , getApplDateFormat() , "dd-MMM-yyyy");
							//System.out.println("ldtFromDate [" +ldtFromDate+ "]");
							//next line commented by msalam on 190309 as not required
							//toDate = genericUtility.getValidDateString( toDate , getApplDateFormat() , "dd-MMM-yyyy");
							//toDate = dtf.format( toDate );
						
							//ldt_to_date = DateTime( RelativeDate( date( ldt_from_date ), -1 ) )
					
							/*selQuery = "SELECT  FR_DATE, TO_DATE FROM PERIOD WHERE " + currDate + " BETWEEN FR_DATE AND TO_DATE" ;
							stmt = conn.createStatement();
							//System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);*/
							
							PreparedStatement purcPstmt = null;
							//selQuery = "SELECT SUM(B.QUANTITY__STDUOM) purc_rcp FROM INVOICE A, INVDET B WHERE  A.INVOICE_ID = B.INVOICE_ID  AND 	A.CUST_CODE = '"  + custCode + "' AND 	(A.TRAN_DATE >= '"+ldtFromDate+"' AND A.TRAN_DATE <=  '" +toDate+ "') AND 	B.ITEM_CODE = '" + itemCode + "' ";
							// commented by chandni 21-mar-2012
							/*
							selQuery = "SELECT SUM(B.QUANTITY__STDUOM) purc_rcp FROM INVOICE A, INVDET B WHERE  A.INVOICE_ID = B.INVOICE_ID  AND 	A.CUST_CODE = ? AND 	(A.TRAN_DATE >= ? AND A.TRAN_DATE <=  ? ) AND 	B.ITEM_CODE = ? ";
							//stmt = conn.createStatement();
							
							//System.out.println("selQuery [" +selQuery+ "]");
							purcPstmt = conn.prepareStatement( selQuery );
							purcPstmt.setString( 1, custCode );
							purcPstmt.setTimestamp( 2, java.sql.Timestamp.valueOf( genericUtility.getValidDateString( ldtFromDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" ) );
							purcPstmt.setTimestamp( 3, java.sql.Timestamp.valueOf( genericUtility.getValidDateString( toDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" ) );
							purcPstmt.setString( 4, itemCode );

							*/
							// added site_code condition by chandni 21-mar-2012
							selQuery = "SELECT SUM(B.QUANTITY__STDUOM) purc_rcp FROM INVOICE A, INVDET B WHERE  A.INVOICE_ID = B.INVOICE_ID  AND 	A.CUST_CODE = ? AND 	(A.TRAN_DATE >= ? AND A.TRAN_DATE <=  ? ) AND 	B.ITEM_CODE = ? And A.SITE_CODE = ?";
							//stmt = conn.createStatement();
							
							//System.out.println("selQuery [" +selQuery+ "]");
							purcPstmt = conn.prepareStatement( selQuery );
							purcPstmt.setString( 1, custCode );
							purcPstmt.setTimestamp( 2, java.sql.Timestamp.valueOf( genericUtility.getValidDateString( ldtFromDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" ) );
							purcPstmt.setTimestamp( 3, java.sql.Timestamp.valueOf( genericUtility.getValidDateString( toDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" ) );
							purcPstmt.setString( 4, itemCode );
							purcPstmt.setString( 5, siteCode );
							// endded by chandni
							rs = purcPstmt.executeQuery();
							//rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{			 				
								//String purc_rcp = rs.getString("purc_rcp") != null
								purcRcp = rs.getString("purc_rcp");
								if ( purcRcp != null )
								{
									valueXmlString.append("<purc_rcp>").append( purcRcp ).append("</purc_rcp>\r\n");
								}
								else
								{
									valueXmlString.append( "<purc_rcp>0</purc_rcp>\r\n" );
								}	
							}														
							rs.close();
							rs = null;
						}						
						//Changed by Dayanand on 21/11/07 [Add new field cust_stock_item and add item_code itemchange in this field]end
                        //Changed by Dayanand on 11/12/07[ Add itemchange for item code ] start
						else if ( currentColumn.trim().equals("item_code") )
						{
							String tranId = null;
							//// 31/01/09 manoharan commented and aded new code as per ITM
						
							//// 31/01/09 manoharan ITM changes ////////////////////////////////////////////////////////////////////////////////////////////////////
							utilMethods = ibase.webitm.ejb.sys.UtilMethods.getInstance();
							custCode = genericUtility.getColumnValue("cust_code" , dom1);
							siteCode = genericUtility.getColumnValue("site_code" , dom1);
							tranDate = genericUtility.getColumnValue("tran_date" , dom1 );
							tranIdLast = genericUtility.getColumnValue("tran_id__last" ,dom1);
							prdCode = genericUtility.getColumnValue("prd_code" , dom1 );
							tranId = genericUtility.getColumnValue("tran_id" , dom1);
							
							
							//chandni siteCodeCh 09/Apr/12
							System.out.println("hdr.siteCode::"+siteCode+"hdr.custCode::"+custCode);
							selQuery =" select site_code__ch from site_customer where site_code = ? and cust_code = ? and channel_partner = 'Y' " ;
							stmt = conn.prepareStatement(selQuery);
							stmt.setString(1,siteCode);
							stmt.setString(2,custCode);
							
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery();
							if (rs.next())
							{
								siteCodeCh = rs.getString("site_code__ch");
								System.out.println("siteCodeCh 1 ["+siteCodeCh+"]");
								
								if ("null".equalsIgnoreCase(siteCodeCh))
								{
									siteCodeCh = siteCode ;
									System.out.println("siteCodeCh 2 ["+siteCodeCh+"]");
								}
							}
							else
							{
								siteCodeCh = siteCode;
								System.out.println("siteCodeCh 3 ["+siteCodeCh+"]");
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}			
							System.out.println("siteCodeCh 4 ["+siteCodeCh+"]");
							//chandni
							
							
							itemCode = columnValue;
							
							
							if (tranIdLast == null || tranIdLast.trim().length() == 0)
							{
								tranIdLast = " ";
							}
							selQuery = "SELECT ITEM_SER, DESCR, UNIT, CASE WHEN LOC_TYPE__PARENT IS NULL THEN LOC_TYPE ELSE LOC_TYPE__PARENT END LOC_TYPE FROM ITEM WHERE  ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(selQuery);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{			    
								unit = rs.getString("UNIT");
							    locType = rs.getString("LOC_TYPE");
								itemSer = rs.getString("ITEM_SER");
								itemDescr = rs.getString("DESCR");
								
								valueXmlString.append("<item_ser>").append(itemSer).append("</item_ser>\r\n");
								valueXmlString.append("<unit>").append(unit).append("</unit>\r\n");
								valueXmlString.append("<loc_type>").append(locType).append("</loc_type>\r\n");

								valueXmlString.append("<item_descr><![CDATA[").append(itemDescr).append("]]></item_descr>\r\n");
							}	
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
							if ("S".equals(stockMode.trim()) || "A".equals(stockMode.trim()) || "C".equals(stockMode.trim()) )
							{
								String fromDate = genericUtility.getColumnValue("from_date" , dom1 );
								java.sql.Timestamp tsfromDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( fromDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
								
								java.sql.Timestamp tsprevDate = utilMethods.RelativeDate(tsfromDate, -1);
								
								SimpleDateFormat sdtApplDate = new SimpleDateFormat(getApplDateFormat());
								String prevDate = sdtApplDate.format(tsprevDate);
									
								selQuery = "SELECT SUM(DET.CL_STOCK) AS OP_STOCK FROM CUST_STOCK HDR, CUST_STOCK_DET DET "
										+ " WHERE HDR.TRAN_ID = DET.TRAN_ID "
										+ " AND	HDR.CUST_CODE = ? "
										+ " AND	HDR.SITE_CODE = ? "
										+ " AND	HDR.TO_DATE = ? "
										+ " AND DET.ITEM_CODE = ? ";
								
								pstmt = conn.prepareStatement(selQuery);
								pstmt.setString(1,custCode);
								//pstmt.setString(2,siteCode); //commented by chandni 
								pstmt.setString(2,siteCodeCh); // added by chandni Shah 11/Apr/12
								pstmt.setTimestamp(3,tsprevDate);
								pstmt.setString(4,itemCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{			    
									opStock = rs.getString("OP_STOCK");
									if (opStock == null)
									{
										opStock = "0";
									}
									
									valueXmlString.append("<op_stock>").append(opStock).append("</op_stock>\r\n");
								}	
								rs.close();
								pstmt.close();
								rs = null;
								pstmt = null;
								//System.out.println("opStock [" +opStock+ "]");
								String invListString = "";
								
								selQuery = "SELECT  FR_DATE, TO_DATE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE" ;				
								pstmt = conn.prepareStatement(selQuery);
								pstmt.setTimestamp(1,tsprevDate);
								rs = pstmt.executeQuery();
								if (rs.next())
								{			    
									preFromDate = rs.getTimestamp("FR_DATE");
									preToDate = rs.getTimestamp("TO_DATE");
									invFromDate = preFromDate;
									invToDate = preToDate;
									//next line commented as not needed by alam on 190308	
									//invListString = GetInvList( tranId, preFromDate, preToDate, conn);
								}	
								rs.close();
								pstmt.close();
								rs = null;
								pstmt = null;
								
								fromDate = genericUtility.getColumnValue("from_date" , dom1 );
								java.sql.Timestamp tsFromDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( fromDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
								
								String toDate = genericUtility.getColumnValue("to_date" , dom1 );
								java.sql.Timestamp tsToDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( toDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
								//preFromDate = tsFromDate;
								//preToDate = tsToDate;
								
/* 07/06/09 manoharan changed as per Mitesh quantity for period + previous transit
 
 								//check if there is record in cust_stock_inv
								int custInvCount = 0;
								selQuery = " select count( 1 ) cnt "
											+"	from cust_stock_inv inv "
											+" where inv.tran_id = ? ";
							//				+"	and inv.DLV_FLG = 'Y' "
							//				+"	and inv.INVOICE_DATE between ? and ? ";
	
								pstmt = conn.prepareStatement( selQuery );
											
								pstmt.setString( 1, tranId );
							//	pstmt.setTimestamp( 2, invFromDate );
							//	pstmt.setTimestamp( 3, invToDate );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									custInvCount = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								//end check
								
								
								if ( custInvCount == 0 )
								{
									selQuery = "SELECT SUM(B.QUANTITY__STDUOM) PURC_RCP FROM INVOICE A, INVDET B "
										+ " WHERE  A.INVOICE_ID = B.INVOICE_ID "
										+ " AND A.CUST_CODE = ? "
										+ " AND B.ITEM_CODE = ? "
										+ " AND A.TRAN_DATE >= ? AND A.TRAN_DATE <= ? ";
									pstmt = conn.prepareStatement( selQuery );
									pstmt.setString(1,custCode);
									pstmt.setString(2,itemCode);
									pstmt.setTimestamp(3,tsFromDate);
									pstmt.setTimestamp(4,tsToDate);								
									rs = pstmt.executeQuery();
									if (rs.next())
									{			    
										purcRcp = rs.getString("PURC_RCP");
										if (purcRcp == null)
										{
											purcRcp = "0";
										}
										purReceipt = purcRcp;
										valueXmlString.append("<purc_rcp>").append(purcRcp).append("</purc_rcp>\r\n");
									}	
									rs.close();
									pstmt.close();
									rs = null;
									pstmt = null;
								}
								else 
							*/	{
									//selQuery = "SELECT SUM(B.QUANTITY__STDUOM) PURC_RCP FROM INVOICE A, INVDET B "
									//	+ " WHERE A.INVOICE_ID = B.INVOICE_ID "
									//	+ " AND A.CUST_CODE = ? "
									//	+ " AND B.ITEM_CODE = ? "
									//	+ " AND A.INVOICE_ID IN ( select inv.invoice_id "
									//	+ "					from cust_stock_inv inv "
									//	+ "				where inv.tran_id = ? "
									//	+ "					and inv.DLV_FLG = 'Y' "
									//	+ "					and inv.INVOICE_DATE between ? and ? ) ";
									
									/*
									selQuery = "SELECT SUM(QUANTITY__STDUOM) PURC_RCP FROM  INVDET "
										+ " where invoice_id in "
										+ " (select invoice_id from invoice where cust_code = ? "
										+ " and tran_date between ? and ?  "
										+ " and invoice_id not in ( select invoice_id "
											+ " from cust_stock_inv "
											+ " where tran_id = ? ) "
										+ " ) and item_code = ? "; */
								// commented by chandni 21-mar-2012 
								/*	selQuery = "SELECT SUM(QUANTITY__STDUOM) PURC_RCP FROM  INVDET "
										+ " where invoice_id in "
										+ " (select invoice_id from invoice where cust_code = ? "
										+ " and tran_date between ? and ? ) "
										+ " and item_code = ? ";
										
									pstmt = conn.prepareStatement( selQuery );
									pstmt.setString( 1, custCode );
									pstmt.setTimestamp( 2, tsFromDate );
									pstmt.setTimestamp( 3, tsToDate );
									//pstmt.setString( 4, tranId );
									pstmt.setString( 4, itemCode );
									*/
								// added site_code condition by chandni 21-mar-2012
								selQuery = "SELECT SUM(QUANTITY__STDUOM) PURC_RCP FROM  INVDET "
										+ " where invoice_id in "
										+ " (select invoice_id from invoice where cust_code = ? and site_code = ?  "
										+ " and tran_date between ? and ? ) "
										+ " and item_code = ? ";
										
									pstmt = conn.prepareStatement( selQuery );
									pstmt.setString( 1, custCode );
									pstmt.setString( 2, siteCode );
									pstmt.setTimestamp( 3, tsFromDate );
									pstmt.setTimestamp( 4, tsToDate );
									//pstmt.setString( 4, tranId );
									pstmt.setString( 5, itemCode );
									// endded by chandni
									rs = pstmt.executeQuery();
									double rcpCur =0, rcpPre = 0, totRcp = 0;
									if (rs.next())
									{			    
										rcpCur = rs.getDouble("PURC_RCP");
									}	
									rs.close();
									pstmt.close();
									rs = null;
									pstmt = null;
	
									/*
									selQuery = "SELECT SUM(QUANTITY__STDUOM) PURC_RCP FROM  INVDET "
										+ " where invoice_id in "
										+ " ( select invoice_id "
										+ " from cust_stock_inv " 
										+ " where tran_id = ? "
										+ " and DLV_FLG = 'Y' "
										+ " and INVOICE_DATE between ? and ? "
										+ " union all "
										+ " select preinv.invoice_id "
										+ " from cust_stock_inv preinv " 
										+ " where preinv.tran_id = ? "
										+ " and preinv.DLV_FLG = 'N' ) "
										+ " and item_code = ? ";
									pstmt = conn.prepareStatement( selQuery );
									pstmt.setString( 1, tranId );
									pstmt.setTimestamp( 2, invFromDate );
									pstmt.setTimestamp( 3, invToDate );
									pstmt.setString( 4,  tranIdLast);
									pstmt.setString( 5, itemCode );
									*/
									selQuery = "SELECT SUM(QUANTITY__STDUOM) PURC_RCP FROM  INVDET "
										+ " where invoice_id in "
										+ " ( select invoice_id "
										+ " from cust_stock_inv" 
										+ " where tran_id = ? "
										+ " and DLV_FLG = 'N' ) "
										+ " and item_code = ? ";
									pstmt = conn.prepareStatement( selQuery );
									pstmt.setString( 1,  tranIdLast);
									pstmt.setString( 2, itemCode );

									rs = pstmt.executeQuery();
									
									if (rs.next())
									{			    
										rcpPre = rs.getDouble("PURC_RCP");
									}	
									rs.close();
									pstmt.close();
									rs = null;
									pstmt = null;
									
									utilMethods = ibase.webitm.ejb.sys.UtilMethods.getInstance();
									totRcp = rcpCur + rcpPre;
									purcRcp = utilMethods.getReqDecString(totRcp,2);
									utilMethods = null;
									purReceipt = purcRcp;
									valueXmlString.append("<purc_rcp>").append(purcRcp).append("</purc_rcp>\r\n");
								}
	
								//System.out.println("purcRcp [" +purcRcp+ "]");
							    // commented by chandni 21-mar-2012
							    /*  
								selQuery = "SELECT B.QUANTITY__STDUOM QUANTITY, B.ITEM_CODE ITEM_CODE, "
									+ " A.SITE_CODE SITE_CODE, B.LOC_CODE LOC_CODE, "
									+ " B.LOT_NO LOT_NO, B.LOT_SL LOT_SL, B.RET_REP_FLAG RET_REP_FLAG "
									+ " FROM  SRETURN A, SRETURNDET B "
									+ " WHERE A.TRAN_ID = B.TRAN_ID "
									+ " AND A.CUST_CODE = ? "
									+ " AND A.TRAN_DATE >= ? "
									+ " AND A.TRAN_DATE <= ? "
									+ " AND B.ITEM_CODE = ? ";
									
								pstmt = conn.prepareStatement(selQuery);
								pstmt.setString(1,custCode);
								// 11/09/09 manoharan sales return for the period code in header
								// only to be considered
								//pstmt.setTimestamp(2,preFromDate);
								//pstmt.setTimestamp(3,preToDate);
								pstmt.setTimestamp( 2, tsFromDate );
								pstmt.setTimestamp( 3, tsToDate );
								// end 11/09/09 manoharan
								pstmt.setString(4,itemCode);
								*/
								// added site_code condition by chandni 21-mar-2012
						    	selQuery =" SELECT B.QUANTITY__STDUOM QUANTITY, B.ITEM_CODE ITEM_CODE, "
									+ " A.SITE_CODE SITE_CODE, B.LOC_CODE LOC_CODE, "
									+ " B.LOT_NO LOT_NO, B.LOT_SL LOT_SL, B.RET_REP_FLAG RET_REP_FLAG "
									+ " FROM  SRETURN A, SRETURNDET B "
									+ " WHERE A.TRAN_ID = B.TRAN_ID "
									+ " AND A.CUST_CODE = ? "
									+ " AND A.TRAN_DATE >= ? "
									+ " AND A.TRAN_DATE <= ? "
									+ " AND B.ITEM_CODE = ? "
									+ " AND A.SITE_CODE = ? ";
									
								pstmt = conn.prepareStatement(selQuery);
								pstmt.setString(1,custCode);
								// 11/09/09 manoharan sales return for the period code in header
								// only to be considered
								//pstmt.setTimestamp(2,preFromDate);
								//pstmt.setTimestamp(3,preToDate);
								pstmt.setTimestamp( 2, tsFromDate );
								pstmt.setTimestamp( 3, tsToDate );
								// end 11/09/09 manoharan
								pstmt.setString(4,itemCode);
								pstmt.setString(5,siteCode);
								// endded by chandni
								rs = pstmt.executeQuery();
								retQty = 0;
								replQty = 0;
								selQuery = "SELECT INV_STAT FROM STOCK  "
									+ " WHERE ITEM_CODE = ? "
									+ " AND SITE_CODE = ? "
									+ " AND LOC_CODE  = ? "
									+ " AND LOT_NO = ? "
									+ " AND LOT_SL = ? " ;
								pstmt1 = conn.prepareStatement(selQuery);
								while (rs.next())
								{			    
									quantity = rs.getDouble("QUANTITY");
									rSiteCode = rs.getString("SITE_CODE");
									locCode = rs.getString("LOC_CODE");
									lotNo = rs.getString("LOT_NO");
									lotSl = rs.getString("LOT_SL");
									retRepFlag = rs.getString("RET_REP_FLAG");
									retRepFlag = ( retRepFlag == null || retRepFlag.trim().length() == 0 ) ? "" : retRepFlag.trim();
									pstmt1.setString(1,itemCode);
									pstmt1.setString(2,rSiteCode);
									pstmt1.setString(3,locCode);
									pstmt1.setString(4,lotNo);
									pstmt1.setString(5,lotSl);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										invStat = rs1.getString("INV_STAT");
										invStat = ( invStat == null || invStat.trim().length() == 0 ) ? "" : invStat.trim();
										if ("R".equalsIgnoreCase(retRepFlag.trim()) && "SALE".equalsIgnoreCase(invStat.trim()) )
										{
											retQty += quantity;
										}
										else if ( "P".equalsIgnoreCase(retRepFlag.trim()) )
										{
											replQty += quantity;
										}
									}
									rs1.close();
									rs1 = null;
		
								}	
								pstmt1.close();
								pstmt1 = null;
								rs.close();
								pstmt.close();
								rs = null;
								pstmt = null;
								quantity = retQty - replQty ;
								valueXmlString.append("<purc_ret>").append(quantity).append("</purc_ret>\r\n");
								//System.out.println("purc_ret [" +quantity+ "]");
								transitQty = 0;
								
								transitFlg = genericUtility.getColumnValue("transit_flag" , dom1);
								
								if ( transitFlg == null || transitFlg.trim().length() == 0)
								{
									transitFlg = "N";
								}
								
								if ("N".equalsIgnoreCase(transitFlg))
								{
									transitQty = GetTransitQty( tranId, itemCode, conn );
								}
								
								transitQtyStr = transitQty + "";
	
								valueXmlString.append("<transit_qty protect = '1'>").append(transitQty).append("</transit_qty>\r\n");
								//System.out.println("transitQty [" +transitQty+ "]");
								prdCode = genericUtility.getColumnValue("prd_code" , dom1);
								/* 27/03/09 commented as not yet implemented as told by KB full scan
								selQuery = "SELECT SUM(D.QUANTITY) QUANTITY FROM SORDER H, SORDDET D "
									+ " WHERE D.SALE_ORDER = H.SALE_ORDER "
									+ " AND	H.CUST_CODE	= ? "
									+ " AND	H.SITE_CODE	= ? "
									+ " AND	H.UDF__STR2 = ? "
									+ " AND	D.ITEM_CODE__ORD	= ? " ;
								pstmt = conn.prepareStatement(selQuery);
								pstmt.setString(1,custCode);
								pstmt.setString(2,siteCode);
								pstmt.setString(3,prdCode);
								pstmt.setString(4,itemCode);
								rs = pstmt.executeQuery();
								if ( rs.next() )
								{			    
									quantity = rs.getDouble( "QUANTITY" );
									adhocReplQty = quantity + "";
									valueXmlString.append("<adhoc_repl_qty>").append( quantity ).append("</adhoc_repl_qty>\r\n");
								}	
								rs.close();
								pstmt.close();
								rs = null;
								pstmt = null;
								*/
								//System.out.println("adhoc_repl_qty [" +quantity+ "]");
								selQuery = "SELECT ITEM_CODE__REF ,DESCR FROM CUSTOMERITEM "
									+ " WHERE CUST_CODE = ? "
									+ " AND ITEM_CODE = ? ";
								pstmt = conn.prepareStatement(selQuery);
								pstmt.setString(1,custCode);
								pstmt.setString(2,itemCode);
								rs = pstmt.executeQuery();
	
								if (rs.next())
								{			    
									String custItemRef = rs.getString("ITEM_CODE__REF");
									String custItemRefDescr = rs.getString("DESCR");
									valueXmlString.append("<cust_item__ref>").append(custItemRef).append("</cust_item__ref>\r\n");
									valueXmlString.append("<cust_item_ref_descr><![CDATA[").append(custItemRefDescr).append("]]></cust_item_ref_descr>\r\n");
								}	
								rs.close();
								pstmt.close();
								rs = null;
								pstmt = null;
								/////End 31/01/09 manoharan ///////////////////////////////////////////////////////////
							}
						}
						
						else if (currentColumn.trim().equalsIgnoreCase("op_stock"))
						{
							opStock = columnValue;
							//System.out.println(" op_stock currentColumn ["+currentColumn+"]");
							//System.out.println("line 1295 opStock["+opStock+"]");
							/*
							if (stockMode.trim().equals("S"))
							{
								sales = genericUtility.getColumnValue("sales",dom); //, formNo, domID);
								clStk = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(sales) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								valueXmlString.append("<cl_stock>").append(clStk).append("</cl_stock>\r\n");
							}
							else if (stockMode.trim().equals("C"))
							{
								clStks = genericUtility.getColumnValue("cl_stock",dom); //, formNo, domID);
								saless = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(clStks) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
							}
							*/
							
						}
						else if (currentColumn.trim().equalsIgnoreCase("cl_stock"))
						{							
							clStks = columnValue;
							opStock = genericUtility.getColumnValue("op_stock",dom );//, formNo); //, domID);
							
							//System.out.println(" cl_stock currentColumn ["+currentColumn+"]");
							//System.out.println("line 1316 opStock["+opStock+"]");
							/*
							if (stockMode.trim().equals("S"))
							{
								valueXmlString.append("<cl_stock>").append(columnValue).append("</cl_stock>\r\n");
							}
							else if (stockMode.trim().equals("C"))
							{
								saless = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(clStks) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
							}
							*/
						}
						else if (currentColumn.trim().equalsIgnoreCase("purc_rcp"))
						{							
						}
						else if (currentColumn.trim().equalsIgnoreCase("purc_ret"))
						{							
						}
						else if (currentColumn.trim().equalsIgnoreCase("adj_qty"))
						{							
						}
						//genericUtility.serializeDom( dom );
						//genericUtility.serializeDom( dom1 );
						//genericUtility.serializeDom( dom2 );
						
						if ("S".equals(stockMode.trim()) || "A".equals(stockMode.trim()) || "C".equals(stockMode.trim()) )
						{
							System.out.println( "This is the changed jar at 08/04/09 2:09 pm" );
							
							DecimalFormat df = new DecimalFormat();
							df.applyPattern("##.00");		
							if (!currentColumn.trim().equals("item_code"))				
							{
								opStock = genericUtility.getColumnValue("op_stock", dom );//,formNo);
								
								//Changed by Dayanand on 8/25/2008 [Change the transit quantity calculation]start (reqId DI89SUN082)
								purReceipt = genericUtility.getColumnValue("purc_rcp", dom);
								//purReceipt = genericUtility.getColumnValue("cust_purc_rcp", dom);
							}
							if( ! currentColumn.trim().equals( "item_code" ) )
							{
								sales = genericUtility.getColumnValue("sales", dom );//,formNo);
								clStks = genericUtility.getColumnValue("cl_stock", dom );//,formNo);
							}
							if( !currentColumn.trim().equals("item_code"))
							{
								adhocReplQty = genericUtility.getColumnValue( "adhoc_repl_qty", dom );
								transitQtyStr = genericUtility.getColumnValue( "transit_qty", dom );
							}
							if (opStock == null || opStock.equalsIgnoreCase( "null" ) || opStock.trim().length() == 0 )
							{
								opStock = "0";
							}
							if (clStks == null || clStks.equalsIgnoreCase( "null" ) || clStks.trim().length() == 0  )
							{
								clStks = "0";
							}
							if (sales == null || sales.equalsIgnoreCase( "null" ) || sales.trim().length() == 0  )
							{
								sales = "0";
							}
							if (purReceipt == null || purReceipt.equalsIgnoreCase(  "null" ) || purReceipt.trim().length() == 0  )
							{
								purReceipt = "0";
							}
							if (adjQty == null || adjQty.equalsIgnoreCase( "null" ) || adjQty.trim().length() == 0  )
							{
								adjQty = "0";
							}
							if (purReturn == null || purReturn.equalsIgnoreCase( "null" ) || purReturn.trim().length() == 0  )
							{
								purReturn = "0";
							}
							
							//System.out.println( "opStock [" + opStock  + "]" );
							//System.out.println( "clStks ["  +  clStks  + "]" );
							//System.out.println( "sales ["   +  sales   + "]" );
							//System.out.println( "cust_purc_rcp ["+purReceipt+ "]" );
							//System.out.println( "shb transitFlg [" + transitFlg + "]" );
	
							
							transitFlg = genericUtility.getColumnValue( "transit_flag", dom1 );
							
							if ( transitFlg == null || transitFlg.trim().length() == 0)
							{
								transitFlg = "N";
							}
							if ( transitFlg.equalsIgnoreCase( "N" ) )
							{
								if (stockMode.trim().equals("S") )
								{
									if( ! currentColumn.trim().equals( "item_code" ) )
									{
										sales = genericUtility.getColumnValue("sales",dom); //, formNo, domID);
									}
									if (sales == null || sales.equalsIgnoreCase( "null" ) || sales.trim().length() == 0  )
									{
										sales = "0";
									}
									clStk = Double.parseDouble( opStock )
										+ Double.parseDouble( purReceipt )
										- Double.parseDouble( transitQtyStr ) 
										- Double.parseDouble( sales ) 
										+ Double.parseDouble( adjQty ) 
										- Double.parseDouble( purReturn )
										- Double.parseDouble( adhocReplQty );
									valueXmlString.append("<cl_stock>").append(clStk).append("</cl_stock>\r\n");
									clStks = clStk + "";
								}
								else if (stockMode.trim().equals("C") || stockMode.trim().equals("A") )
								{	
									
									clStks = genericUtility.getColumnValue("cl_stock",dom );//, formNo);//, domID);
									if (opStock == null || opStock.equalsIgnoreCase( "null" ) || opStock.trim().length() == 0  )
									{
										opStock = "0";
									}
									if (purReceipt == null || purReceipt.equalsIgnoreCase( "null" ) || purReceipt.trim().length() == 0  )
									{
										purReceipt = "0";
									}
									if (transitQtyStr == null || transitQtyStr.equalsIgnoreCase( "null" ) || transitQtyStr.trim().length() == 0  )
									{
										transitQtyStr = "0";
									}
									if (clStks == null || clStks.equalsIgnoreCase( "null" ) || clStks.trim().length() == 0  )
									{
										clStks = "0";
									}
									if (adjQty == null || adjQty.equalsIgnoreCase( "null" ) || adjQty.trim().length() == 0  )
									{
										adjQty = "0";
									}
									if (purReturn == null || purReturn.equalsIgnoreCase( "null" ) || purReturn.trim().length() == 0  )
									{
										purReturn = "0";
									}
									if (adhocReplQty == null || adhocReplQty.equalsIgnoreCase( "null" ) || adhocReplQty.trim().length() == 0  )
									{
										adhocReplQty = "0";
									}
									saless = Double.parseDouble( opStock ) 
											+ Double.parseDouble( purReceipt )
											- Double.parseDouble( transitQtyStr )									
											- Double.parseDouble( clStks ) 
											+ Double.parseDouble( adjQty ) 
											- Double.parseDouble( purReturn )
											- Double.parseDouble( adhocReplQty );
									valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
									sales = saless + "";
	
									String salesStr = sales;//genericUtility.getColumnValue( "sales", dom );
									double transitQntty = 0.0;
									transitQntty = Double.parseDouble( opStock ) 
											+ Double.parseDouble( purReceipt )
											- Double.parseDouble( salesStr )
											- Double.parseDouble( clStks ) 
											+ Double.parseDouble( adjQty ) 
											- Double.parseDouble( purReturn )
											- Double.parseDouble( adhocReplQty );
									valueXmlString.append("<transit_qty protect = '1'>").append(transitQntty).append("</transit_qty>\r\n");
								}
							}
							else
							{
								String salesStr = genericUtility.getColumnValue( "sales", dom );
								double transitQntty = 0.0;
								transitQntty = Double.parseDouble( opStock ) 
										+ Double.parseDouble( purReceipt )
										- Double.parseDouble( salesStr )
										- Double.parseDouble( clStks ) 
										+ Double.parseDouble( adjQty ) 
										- Double.parseDouble( purReturn )
										- Double.parseDouble( adhocReplQty );
								valueXmlString.append("<transit_qty protect = '1'>").append(transitQntty).append("</transit_qty>\r\n");
								//transitQty = transitQntty + "";
							}
							/*  09/02/09 commented by manoharan  
							//Changed by Dayanand on 01/01/08 [ Calculate transQty] start (reqId DI78SUN073)						
							//transQty = df.format(new Float(Float.parseFloat( opStock ) + Float.parseFloat( purReceipt ) - Float.parseFloat( sales )- Float.parseFloat( clStks ))); 
							transQty = df.format(new Float(Float.parseFloat( opStock ) + Float.parseFloat( purReceipt ) - Float.parseFloat( sales )- Float.parseFloat( clStks ))); 
							//System.out.println( "transQty["  +  transQty  + "]" );
							//Changed by Dayanand on 03/01/08 [ If transit quantity < 0 set 0 else set transQty]start
							if ( Float.parseFloat( transQty )< 0)
							{
								valueXmlString.append("<transit_qty>").append( "0" ).append("</transit_qty>\r\n");
							}
							else
							{
								valueXmlString.append("<transit_qty>").append( transQty ).append("</transit_qty>\r\n");
							}
							//Changed by Dayanand on 03/01/08 [ If transit quantity < 0 set 0 else set transQty]end
	                       //Changed by Dayanand on 01/01/08 [ Calculate transQty] end (reqId DI78SUN073)
							*/
							//System.out.println( "opStock::" + opStock );
							if ( opStock != null && opStock.length() > 0 && !opStock.equalsIgnoreCase( "null" ) )
							{
								opValue = df.format(new Float( Float.parseFloat( opStock ) * rateValue ));
							}
							//System.out.println( "clStks::" + clStks );
							if ( clStks != null && clStks.length() > 0 && !clStks.equalsIgnoreCase( "null" ) )
							{
								clValue = df.format(new Float(Float.parseFloat( clStks ) * rateValue));
							}
							//System.out.println( "sales::" + sales );
							if ( sales != null && sales.length() > 0 && !sales.equalsIgnoreCase( "null") )
							{
								salesValue = df.format(new Float(Float.parseFloat(String.valueOf( sales )) * rateValue));
							}
							//System.out.println( "purReceipt::" + purReceipt );
							if ( purReceipt != null && purReceipt.length() > 0 && !purReceipt.equalsIgnoreCase( "null" ) )
							{
								purValue = df.format(new Float( Float.parseFloat( purReceipt ) * rateValue));
							}			
										
							valueXmlString.append("<op_value>").append(opValue).append("</op_value>\r\n");
							valueXmlString.append("<cl_value>").append(clValue).append("</cl_value>\r\n");
							valueXmlString.append("<sales_value>").append(salesValue).append("</sales_value>\r\n");
							valueXmlString.append("<pur_value>").append(purValue).append("</pur_value>\r\n");
						}
						
						valueXmlString.append("</Detail>\r\n");
						break;
				}//switch (currentFormNo)
			}//if (columnValue != null)
			conn.close();
			
			valueXmlString.append("</Root>\r\n");	
		}
		catch (Exception e)
		{
			//System.out.println("Exception :CustStockEJB :itemChanged :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
				conn = null;
				}
			}catch(Exception d)
			{
			  d.printStackTrace();
			  throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		//System.out.println( "Alam valueXmlString :: " + valueXmlString.toString() );
		return valueXmlString.toString();
	}//itemChanged(Document,String) method ends here   */

	private float getRate(String custCode, String tranDate, String itemCode)throws RemoteException,ITMException
	{
		//ibase.webitm.utility.GenericUtility genericUtility = new ibase.webitm.utility.GenericUtility();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		//System.out.println("[CustStockEJB]Calcuating rate for the custCode :"+custCode+": tranDate :"+tranDate+": itemCode :"+itemCode+":");
		Connection conn = null;		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String priceList = null;
		float retRate = 0f;
		try
		{			
			//for dynamic binding 190309
			//String selQuery = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
			String selQuery = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE = ? ";
			//System.out.println("[CustStockEJB]Getting PRICE_LIST value, query :\n"+selQuery);
			conn = getConnection();
			stmt = conn.prepareStatement( selQuery );
			stmt.setString( 1, custCode );
			rs = stmt.executeQuery();
			while (rs.next())
			{
				priceList = rs.getString(1);
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (stmt != null)
			{
				stmt.close();
				stmt = null;
			}
			
			////Uncommet by Daynand on 25/08/07 []
			/* Commented by Nazia 21-8-07
			selQuery = "SELECT DDF_PICK_MAX_RATE('"+priceList+"', TO_DATE('"+tranDate+"', '"+getDBDateFormat()+"'), '"+itemCode+"') FROM DUAL";
			//System.out.println("[CustStockEJB]Getting rate value, query :\n"+selQuery);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selQuery);
			while (rs.next())
			{
				retRate = rs.getFloat(1);				
			}
			if (stmt != null)
			{
				stmt.close();
			}*/
			tranDate = genericUtility.getValidDateString( tranDate , getApplDateFormat() , getDBDateFormat());
			//for dynamic binding 190309
			//selQuery = "SELECT DDF_PICK_MAX_RATE('"+priceList+"', TO_DATE('"+tranDate+"', '"+getDBDateFormat()+"'), '"+itemCode+"') FROM DUAL";
			selQuery = "SELECT DDF_PICK_MAX_RATE( ?, TO_DATE( ? , ? ), ? ) FROM DUAL ";
			//System.out.println("[CustStockEJB]Getting rate value, query :\n"+selQuery);
			stmt = conn.prepareStatement( selQuery );
			stmt.setString( 1, priceList );
			stmt.setString( 2, tranDate );
			stmt.setString( 3, getDBDateFormat() );
			stmt.setString( 4, itemCode );
			rs = stmt.executeQuery();
			while (rs.next())
			{
				retRate = rs.getFloat(1);
				//System.out.println("retRate [" +retRate+ "]");				
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (stmt != null)
			{
				stmt.close();
				stmt = null;
			}
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :getRate :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception d)
			{
			  d.printStackTrace();
			  throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		//System.out.println("[CustStockEJB]rate :"+retRate);
		return retRate;
		////System.out.println("returning value from getRate as 1");
		//return 1;
	}
	private String GetInvList( String tranId, java.sql.Timestamp fromDate, java.sql.Timestamp toDate, Connection conn ) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceDate = null;
		String invoiceId = null;
		String dlvFlg = null ;
		String invoiceListString = "";
		java.sql.Timestamp tsInvoiceDate = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try
		{
			sql = " select inv.invoice_id "
				+"	from cust_stock_inv inv "
				+" where inv.tran_id = ? " //'" + tranId + "'"
				+"	and inv.DLV_FLG = 'Y' "
				+"	and inv.INVOICE_DATE between ? and ? ";
				
			pstmt = conn.prepareStatement( sql );
			
			pstmt.setString( 1, tranId );
			pstmt.setTimestamp( 2, fromDate );
			pstmt.setTimestamp( 3, toDate );
			
			rs = pstmt.executeQuery();
			
			while( rs.next() )
			{
				invoiceId = rs.getString( "invoice_id" );
				
				if ( invoiceListString.trim().length() == 0 )
				{
					invoiceListString = "'" + ( invoiceId == null ? "" : invoiceId ) + "'" ;
				}
				else
				{
					invoiceListString += ",'" + ( invoiceId == null ? "" : invoiceId ) + "'" ;
				}
			}
			
			rs.close();
			rs = null;
			
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return invoiceListString ;
	}
	private double GetTransitQty(String tranId, String itemCode, Connection conn) throws Exception
	{
		NodeList parentNodeList=null;
		NodeList childList = null;
		Node parentNode=null;
		Node childNode = null;
		String invoiceId = null;
		String dlvFlg = null ;
		double transitQty = 0, quantity = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try
		{		
		
			sql = " SELECT SUM( iv.QUANTITY__STDUOM ) QUANTITY " 
				+"	FROM INVDET iv "
				+" WHERE  iv.INVOICE_ID in ( select dtl.invoice_id "
				+"								from cust_stock_inv dtl " 
				+"							where dtl.tran_id = ? " //'" + tranId.trim() +"'"
				+"								and dtl.dlv_flg = 'N' )"
				+"	AND iv.ITEM_CODE = ? ";//'" + itemCode +"'";
			
			pstmt = conn.prepareStatement( sql );

			pstmt.setString( 1, tranId );
			pstmt.setString( 2, itemCode );

			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				quantity = rs.getDouble("QUANTITY");
			}
			rs.close();
			rs = null;
			
			transitQty = quantity;
			/*
			sql = "SELECT SUM(QUANTITY__STDUOM)QUANTITY FROM INVDET "
				+ " WHERE  INVOICE_ID = ? "
				+ " AND ITEM_CODE = ? " ;
			
			pstmt = conn.prepareStatement(sql);

			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				//System.out.println("ctr  ................"+ctr);
				parentNode = parentNodeList.item(ctr);

				invoiceId = genericUtility.getColumnValueFromNode("invoice_id", parentNode);
				dlvFlg = genericUtility.getColumnValueFromNode("dlv_flg", parentNode);
				if ("N".equalsIgnoreCase(dlvFlg) )
				{
					pstmt.setString(1,invoiceId);
					pstmt.setString(2,itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						quantity = rs.getDouble("QUANTITY");
						transitQty += quantity ;
					}
					rs.close();
					rs = null;

				}
			}
			*/
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return transitQty ;
	}
	//Changed by dayanand on 28/02/09[ Add getObjNameFromDom() method and checkNull() method req id='WS89SUN078']start
	private String getObjNameFromDom(Document dom,String attribute) throws RemoteException,ITMException
	{		
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String objName = "";
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail3");
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
			objName = currDetail.getAttributes().getNamedItem(attribute).getNodeValue();
					
		}
		return objName;
	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	//Changed by dayanand on 28/02/09[ Add getObjNameFromDom() method and checkNull() method req id='WS89SUN078']end
	private String getNodeValue( Node currDet, String fldName, boolean isAttribute )
	{
		String fldValue = null;
		boolean isFound = false;
		NodeList currNodes = currDet.getChildNodes();
		int currDetLen = currNodes.getLength();
		for(int detIdx = 0; detIdx < currDetLen && !isFound ; detIdx++ )
		{
			Node currNode = currNodes.item( detIdx );
			String nodeName = currNode.getNodeName();

			if( isAttribute == true )
			{
				if ( nodeName.equalsIgnoreCase( "attribute" ) )
				{
					fldValue = currNode.getAttributes().getNamedItem( fldName ).getNodeValue();
					isFound = true;
				}				
			}
			else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( fldName ) )
			{
				fldValue = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : null;
				isFound = true;
			}
		}
		return fldValue;
	}
	//added by msalam on 220409 for checking duplicate item
	private boolean isItemDuplicate( String columnValue, String itemLineNo, Document dom )
	{
		String updateFlag = null;
		String fldValue = null;
		String nodeName = null;
		Node currNode = null;
		Node currDet = null;
		NodeList currNodes = null;
		String lineNoStr = "0";
		
		int currNodeLen;
		System.out.println("Checking for duplicate items");
		NodeList detailNodes = dom.getElementsByTagName( "Detail2" );		
		int detLen = detailNodes.getLength();
		
		for( int detIdx = 0; detIdx < detLen ; detIdx++ )
		{
			currDet = detailNodes.item( detIdx );
			
			currNodes = currDet.getChildNodes();
			currNodeLen = currNodes.getLength();
			columnValue = columnValue != null ? columnValue.trim() : "";
			for(int curNodeIdx = 0; curNodeIdx < currNodeLen; curNodeIdx++ )
			{
				currNode = currNodes.item( curNodeIdx );
				nodeName = currNode.getNodeName();

				if ( nodeName.equalsIgnoreCase( "attribute" ) )
				{
					updateFlag = currNode.getAttributes().getNamedItem( "updateFlag" ).getNodeValue();
				}				
				else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( "item_code" ) )
				{
					fldValue = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "";
				}
				else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( "line_no" ) )
				{
					lineNoStr = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "0";
				}
			}
			if( !"D".equalsIgnoreCase( updateFlag ) )
			{
				System.out.println("not duplicate lineNoStr [" + lineNoStr + "] itemLineNo [" + itemLineNo+ "]fldValue [" + fldValue + "] columnValue [" + columnValue + "]");
				if( !lineNoStr.trim().equals( itemLineNo.trim() ) && fldValue.trim().equalsIgnoreCase( columnValue ) )
				{
					System.out.println("lineNoStr1233 [" + lineNoStr + "] itemLineNo [" + itemLineNo+ "]fldValue [" + fldValue + "] columnValue [" + columnValue + "]");
					return true;
				}
			}
		}
		return false;		
	}
	
}