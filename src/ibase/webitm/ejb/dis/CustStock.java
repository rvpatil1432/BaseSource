package ibase.webitm.ejb.dis;


import ibase.system.config.ConnDriver;
import ibase.utility.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;

import org.w3c.dom.*;

import java.text.*;
import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

public class CustStock extends ValidatorEJB //implements SessionBean
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
	}
   */
	
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}
    	// Commented by Chandni Shah 17-Apr-2012
	/*public String wfValData(String xmlString, String formNo, String editFlag) throws RemoteException,ITMException 
	{
		Document dom = null;
		String errString = "";
		try
		{			
			dom = parseString(xmlString);
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :wfValData :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		errString = wfValData(dom, formNo, editFlag);
		return (errString);
	}*/
	// Added by Chandni Shah 17-Apr-2012
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
			System.out.println("cHANDNI 11 xmlString:-" + xmlString );
			System.out.println("cHANDNI 12 xmlString:-" + xmlString1 );
			System.out.println("cHANDNI 12 xmlString2:-" + xmlString2 );
			//long startTime = System.currentTimeMillis();
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("Chandni 13 dom"+dom);
			System.out.println("Chandni 14 dom"+dom1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			System.out.println("Chandni 15 dom"+dom2);
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
	// Commented by Chandni Shah 17-Apr-2012
	//public String wfValData(Document dom, String formNo, String editFlag) throws RemoteException,ITMException
	@SuppressWarnings("resource")
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		// Added by Chandni Shah 17-Apr-2012
		boolean checkNextCol = true;
		String columnValue = "";
		String userId = "";
		String errString = "";
		Connection conn = null;
		StringBuffer selQueryBuff = null;
		Statement stmt = null;
		//Added by shrutika on 27-08-2020 for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		Statement stmt2 = null;
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
		
		String formNo = "";//Changed by poonam on 14-12-2016 temporary
		
		//ITMDBAccessHome itmDBAccessHome = null;
		
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		
		try
		{
			//System.out.println("CustStockEJB validation");			
			//itmDBAccess = itmDBAccessHome.create();

			E12GenericUtility genericUtility = new E12GenericUtility();
			conn = getConnection();
			userId = genericUtility.getColumnValue( "user_id", dom );
			//currentFormNo = Integer.parseInt( formNo );
			//currentFormNo = Integer.parseInt( objContext ); // Added by Chandni Shah 17-Apr-2012
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
				formNo = objContext;
			}
			switch ( currentFormNo )
			{
				case 1:
					System.out.println("currentFormNo:::"+currentFormNo);
					columnValue = genericUtility.getColumnValue("tran_date", dom );
					System.out.println("columnValue 11:"+columnValue);
					if(columnValue != null)
					{
						siteCode = genericUtility.getColumnValue( "site_code", dom );
						System.out.println("columnValue 12:"+columnValue+"_and checkNextCol:"+checkNextCol);
						if ( columnValue.length() != 0 && checkNextCol )
						{
							System.out.println("inside columnValue:::");
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = nfCheckPeriod("SAL",getDateObject(columnValue),siteCode);
							errCode=finCommon.nfCheckPeriod("SAL",getDateObject(columnValue),siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							if (errCode.length() != 0)
							{
								System.out.println("errCode k ander");
								//errString = getErrorString("",errCode,userId);
								//checkNextCol = false;
								//errString = itmDBAccess.getErrorString("tran_date",errCode,userId);//,null,conn); 
								errString = getErrorString("tran_date",errCode,userId);//,null,conn); // Change by Chandni Shah 17-Apr-2012
								break;                                
							}
						}
					}
					//columnValue = getColumnValue("cust_code",dom);
					columnValue = genericUtility.getColumnValue("cust_code",dom); // Change by Chandni Shah 17-Apr-2012
					if(columnValue != null)
					{						
						//siteCode = getColumnValue("site_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						System.out.println("siteCode [" +siteCode+ "]");
						tranDate = genericUtility.getColumnValue("tran_date",dom);
						if (columnValue.length() != 0 && checkNextCol)
						{
							errCode = isCustomer(siteCode,columnValue,"S-CSTK",conn);
							System.out.println("errCode [" +errCode+ "]");
							if (errCode.length() !=0 )
							{								
								errString = getErrorString("cust_code",errCode,userId); 
								//checkNextCol = false;									
								//errString = itmDBAccess.getErrorString("cust_code",errCode,userId);//,null,conn); 
								break;
							}
						}						
						if (checkNextCol)
						{							
							selQueryBuff = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = '");
							selQueryBuff.append(columnValue).append("' AND SITE_CODE = '");
							selQueryBuff.append(siteCode).append("' AND NVL(ACTIVE_YN,'Y') = 'Y'");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
							System.out.println("sql1 :\n" + selQueryBuff.toString());
							if (rs.next())
							{
								if (rs.getInt(1) == 0)
								{
									errString = getErrorString("cust_code","VTSITECUST",userId);
									//checkNextCol = false;									
									//errString = itmDBAccess.getErrorString("cust_code","VTSITECUST",userId);//,null,conn); 
									break;
								}
							}
						}
						else
						{
							/* Select count(1) into :cnt
							From	 customer
							Where  cust_code = :mval
							And	 (case when channel_partner is null then 'N' else channel_partner end ) = 'N';
							If cnt = 0 Then errcode = "VTCUST" */						
							sqlCnt = "SELECT COUNT(1) COUNT FROM CUSTOMER WHERE CUST_CODE = '" + columnValue + "' And (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END ) = 'N'";
							System.out.println("sql :\n"+sqlCnt);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
							if (rs.next() && rs.getInt( "COUNT" ) == 0)
							{
								//errCode = "VTCUST";								
								//errString = itmDBAccess.getErrorString("cust_code","VTCUST",userId);//,null,conn); 
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
					//columnValue = getColumnValue("site_code",dom);					
					columnValue = genericUtility.getColumnValue("site_code",dom);					
					if(columnValue != null)
					{
						custCode = genericUtility.getColumnValue("cust_code",dom);
						if (checkNextCol)
						{							
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = '"+columnValue+"'";
							System.out.println("sqlCnt :\n"+sqlCnt);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
							if (rs.next())
							{
								if (rs.getInt(1)==0)
								{
									errString = getErrorString("site_code","VTSITECD1",userId);
									//checkNextCol = false;

									//errString = itmDBAccess.getErrorString("site_code","VTSITECD1",userId);//,null,conn); 
									break;
								}
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
						}
						if (checkNextCol)
						{
							selQueryBuff = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = '");
							selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
							selQueryBuff.append(columnValue).append("' AND NVL(ACTIVE_YN,'Y') = 'Y'");
							System.out.println("sql13 :\n"+selQueryBuff.toString());
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
							if (rs.next())
							{
								if (rs.getInt(1)==0)
								{
									errString = getErrorString("site_code","VTSITECUST",userId);
									//checkNextCol = false;

									//errString = itmDBAccess.getErrorString("site_code","VTSITECUST",userId);//,null,conn); 
									break;
								}
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
						}
					}
					
					//SHWETA 2/14/2005 --Not required at the time of validation
					columnValue = genericUtility.getColumnValue("tran_id__last",dom);					
					if (columnValue != null) 
					{						
						Statement stmt14 = null;
						Statement stmt5 = null;
						ResultSet rs14 = null;
						ResultSet rs5 = null;
						StringBuffer sql5 = null;
						custCode = getColumnValue("cust_code",dom);
						itemSer = getColumnValue("item_ser",dom);
						if (checkNextCol)
						{
							String sq114 = "SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.TRAN_ID = B.TRAN_ID AND A.CUST_CODE = '"+custCode+"'";
							System.out.println("sq114 :\n"+sq114.toString());
							stmt14 = conn.createStatement();
							rs14 = stmt14.executeQuery(sq114.toString());
							if (rs14.next())
							{
								tranId = rs14.getString(1);
							}
							if (itemSer != null && itemSer.trim().length() > 0 )
							{
								sql5 = new StringBuffer();
								sql5.append("SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.CUST_CODE = '");
								sql5.append(custCode).append("' AND A.TRAN_ID = B.TRAN_ID AND A.ITEM_SER = '");
								sql5.append(itemSer).append("'");
							}
							else
							{
								sql5 = new StringBuffer();
								sql5.append("SELECT MAX(A.TRAN_ID) FROM CUST_STOCK A, CUST_STOCK_DET B WHERE A.CUST_CODE = '");
								sql5.append(custCode).append("' AND A.TRAN_ID = B.TRAN_ID");
							}
							System.out.println("sql5 :\n"+sql5.toString());
							stmt5 = conn.createStatement();
							rs5 = stmt5.executeQuery(sql5.toString());
							if (rs5.next())
							{
								String tranIdLast = rs5.getString(1);
							}							
						 }
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
							if (frdate2 != null && todate != null && todate.compareTo(frdate2) < 0)
							{
								//errCode = "VFRTODATE";
								//errString = itmDBAccess.getErrorString("to_date","VFRTODATE",userId);//,null,conn); 
								errString = getErrorString("to_date","VFRTODATE",userId);//,null,conn); 
								break;
							}
							else if (tranDate2 != null && todate != null && tranDate2.compareTo(todate) < 0)
							{
								//errCode = "VTDATE9";
								//errString = itmDBAccess.getErrorString("to_date","VTDATE9",userId);//,null,conn); 
								errString = getErrorString("to_date","VTDATE9",userId);//,null,conn); 
								break;
							}
							else
							{								
								custCode = genericUtility.getColumnValue("cust_code",dom);
								siteCode = genericUtility.getColumnValue("site_code",dom);
								itemSer =  genericUtility.getColumnValue("item_ser",dom);
								if (itemSer.length() == 0)
								{
									//Change by shrutika on 27-08-2020 [Start] for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
									/*selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  IS NULL	AND ('");
									selQueryBuff.append(frdate1).append("' BETWEEN FROM_DATE AND TO_DATE OR '");
									selQueryBuff.append(columnValue).append("' BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");*/
									
									frdate1  = genericUtility.getValidDateString(frdate1, genericUtility.getApplDateFormat(), genericUtility.getDBDateTimeFormat());
									columnValue  = genericUtility.getValidDateString(columnValue, genericUtility.getApplDateFormat(), genericUtility.getDBDateTimeFormat());
									System.out.println("inside cust stock componant......415["+frdate1+"]columnValue["+columnValue+"]");
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = ?  AND SITE_CODE = ?  AND ITEM_SER  IS NULL	AND ( ? BETWEEN FROM_DATE AND TO_DATE OR ? BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
									
									pstmt1 = conn.prepareStatement(selQueryBuff.toString());
									pstmt1.setString(1,custCode);
									pstmt1.setString(2,siteCode);
									pstmt1.setTimestamp(3,java.sql.Timestamp.valueOf(frdate1));
									pstmt1.setTimestamp(4,java.sql.Timestamp.valueOf(columnValue));
									//Change by shrutika on 27-08-2020 [End] for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
								}
								else
								{
									/*selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  = '").append(itemSer).append("'	AND (TO_DATE('");
									selQueryBuff.append(frdate1).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE OR TO_DATE('");
									selQueryBuff.append(columnValue).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");*/
									//Changed by poonam because by above sql record not fetched..
									
									//Change by shrutika on 27-08-2020 [Start] for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
									/*selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  ='").append(itemSer).append("'   AND (  FROM_DATE = '");
									selQueryBuff.append(frdate1).append("' OR '");
									selQueryBuff.append(columnValue).append("' BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");*/
									
									frdate1  = genericUtility.getValidDateString(frdate1, genericUtility.getApplDateFormat(), genericUtility.getDBDateTimeFormat());
									columnValue  = genericUtility.getValidDateString(columnValue, genericUtility.getApplDateFormat(), genericUtility.getDBDateTimeFormat());
									System.out.println("inside cust stock componant......436["+frdate1+"]columnValue["+columnValue+"]");
									
																		
									selQueryBuff = new StringBuffer();
									selQueryBuff.append(" SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = ? AND SITE_CODE = ? AND ITEM_SER  = ?  AND (  FROM_DATE = ? OR ? BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X' ");
									pstmt1 = conn.prepareStatement(selQueryBuff.toString());
									pstmt1.setString(1,custCode);
									pstmt1.setString(2,siteCode);
									pstmt1.setString(3,itemSer);
									pstmt1.setTimestamp(4,java.sql.Timestamp.valueOf(frdate1));
									pstmt1.setTimestamp(5,java.sql.Timestamp.valueOf(columnValue));
									//Change by shrutika on 27-08-2020 [End] for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
								}
								System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
								
								  
								//Change by shrutika on 27-08-2020 [Start] for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
								//stmt = conn.createStatement();
								//rs = stmt.executeQuery(selQueryBuff.toString());
								rs = pstmt1.executeQuery();
								//Change by shrutika on 27-08-2020 [End] for not a valid month exception occur onNext click of Auto Stock Replenishment menu.
								if (rs.next())
								{
									if (rs.getInt(1) > 0 && editFlag.equals("A"))
									{
										//errCode = "VTDUPREC";
										errString = itmDBAccess.getErrorString("to_date","VTDUPREC",userId,"",conn);//,null,conn); 
										break;
									}
								}
								if (stmt != null)
								{
									stmt.close();
									stmt = null;
								}
								if (pstmt1 != null)
								{
									pstmt1.close();
									pstmt1 = null;
								}
							}
							/*if (errCode.length() !=0 )
							{
								errString = getErrorString("to_date",errCode,userId);
								checkNextCol = false;	
							}*/
						}
					}
					columnValue = getColumnValue("item_ser",dom);					
					if(columnValue != null) 
					{
						if (!columnValue.equals("") && checkNextCol)
						{
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM ITEMSER WHERE ITEM_SER = '" + columnValue + "'";
							System.out.println("sqlCnt :\n"+sqlCnt);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
							if (rs.next())
							{
								if (rs.getInt(1) == 0)
								{
									//errCode = "VTITMSER1";
									//errString = itmDBAccess.getErrorString("item_ser","VTITMSER1",userId);//,null,conn); 
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
									selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMER_SERIES WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND ITEM_SER = '");
									selQueryBuff.append(itemSer).append("'");
									System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
									stmt2 = conn.createStatement();
									rs2 = stmt2.executeQuery(selQueryBuff.toString());
									if (rs2.next())
									{
										if (rs2.getInt(1) == 0)
										{
											//errCode = "VTITEMSER4";
											//errString = itmDBAccess.getErrorString("item_ser","VTITEMSER4",userId);//,null,conn); 
											errString = getErrorString("item_ser","VTITEMSER4",userId);//,null,conn); 
											break;
										}
									}
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
							//errString = itmDBAccess.getErrorString("sch_date__1","VTINVSCHDT",userId);//,null,conn); 
							errString = getErrorString("sch_date__1","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
					columnValue =genericUtility.getColumnValue("sch_date__2",dom);					
				    if(columnValue != null) 
				    {						
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate2 = getDateObject( columnValue );								
						if ( schDate2.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							//errCode = "VTINVSCHDT";
							//errString = itmDBAccess.getErrorString("sch_date__2","VTINVSCHDT",userId);//,null,conn); 
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
							//errString = itmDBAccess.getErrorString("sch_date__3","VTINVSCHDT",userId);//,null,conn); 
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
							//errString = itmDBAccess.getErrorString("sch_date__4","VTINVSCHDT",userId);//,null,conn); 
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
							//errString = itmDBAccess.getErrorString("sch_date__5","VTINVSCHDT",userId);//,null,conn); 
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
							//errCode = "VTINVSCHDT";
							//errString = itmDBAccess.getErrorString("sch_date__6","VTINVSCHDT",userId);//,null,conn); 
							errString = getErrorString("sch_date__6","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
				break;
				case 2:
					System.out.println("currentFormNo:::"+currentFormNo);
					columnValue = getColumnValue("invoice_id",dom,formNo);				    
				    if (columnValue != null) 
				    {						
						/*ls_invoice_id = dw_detedit[ii_currformno].GetItemString(1, fldname)
						ls_tran_id    = dw_edit.GetItemString(1,'tran_id')	
						ls_cust_code  = dw_edit.GetItemString(1,'cust_code')
						ldt_from_date = dw_edit.GetItemDateTime(1,'from_date')
						ldt_to_date   = dw_edit.GetItemDateTime(1,'to_date')*/
						/*ldt_from_date = DateTime(RelativeDate(date(ldt_from_date), -1))
						select  FR_DATE into  :ldt_from_date
						from period where :ldt_from_date between FR_DATE and TO_DATE ;

						select count(*) into	:ll_count from invoice 
						where  invoice_id  = :ls_invoice_id 
						and    cust_code   = :ls_cust_code
						and	 tran_date  >= :ldt_from_date
						and	 tran_date  <= :ldt_to_date					
						and    confirmed 	 = 'Y';
						if ll_count = 0 then
						errcode = "VTSSDINV"
						end if*/
						java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );
						tranId = getColumnValue("tran_id",dom);
						custCode = getColumnValue("cust_code",dom1);						
						String fromDate =  getColumnValue("from_date",dom1) ;
						String toDate = getColumnValue("to_date",dom1);
						
						Calendar preCalc = Calendar.getInstance();	
						preCalc.setTime( getDateObject( fromDate ) );
						preCalc.add( Calendar.DATE , -1 );
						java.util.Date prvDate = preCalc.getTime();						
						String fromdate = dtf.format( prvDate );													
						sqlCnt = "SELECT FR_DATE FROM PERIOD WHERE '" +dtf.format( prvDate )+ "' BETWEEN FR_DATE AND TO_DATE " ;
						System.out.println("sqlCnt : [" +sqlCnt+ "]");
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sqlCnt);
						if (rs.next())
						{
							java.sql.Date frDate = rs.getDate("FR_DATE"); 
							Timestamp frDate1 = rs.getTimestamp("FR_DATE"); 
							System.out.println("frDate>>>>>>>>"+frDate+"[frDate1"+frDate1);
							System.out.println("dtf.format(frDate)>>>>>>>>"+dtf.format(frDate));
							System.out.println("dtf.format(frDate1)>>>>>>>>"+dtf.format(frDate1));
							sqlCnt = " SELECT COUNT(*) COUNT FROM INVOICE WHERE INVOICE_ID = ? " +
									 " AND CUST_CODE = ? AND TRAN_DATE >= ? " + 
									 " AND TRAN_DATE <= ?  AND CONFIRMED ='Y' ";
							System.out.println("sqlCnt : [" +sqlCnt+ "]");
							PreparedStatement pstmt = conn.prepareStatement(sqlCnt);
							pstmt.setString( 1, columnValue );
							pstmt.setString( 2, custCode);
							pstmt.setDate( 3, frDate);
							pstmt.setDate( 4, frDate);
							rs = pstmt.executeQuery();
							//rs = stmt.executeQuery(sqlCnt);
							if ( rs.next() && rs.getInt("COUNT") == 0 )
							{
								//errCode="VTSSDINV";
								errString = getErrorString("invoice_id","VTSSDINV",userId); 
								break;
							}
						}
					}																							
				break;
				case 3:
					System.out.println("currentFormNo:::"+currentFormNo);
					System.out.println("objContext:::"+objContext);
					
					String s = genericUtility.serializeDom(dom) ;
					System.out.println("Inside wfvaldata case 3 ...."+ s);
					columnValue = getColumnValue("site_code",dom ,"1");
					
					System.out.println("Inside wfvaldata case 3 ...."+columnValue);
					if (columnValue != null) 
					{
						if (!columnValue.equals("") && checkNextCol)
						{
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = '" + columnValue + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
							if (rs.next())
							{
								if (rs.getInt(1) == 0)
								{
									/*errCode="VTSITE1";
									errString = getErrorString("site_code",errCode,userId);
									checkNextCol = false;*/
									errString = itmDBAccess.getErrorString("site_code","VTSITE1",userId,null,conn); 
									break;
								}
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
						}
					}
					columnValue = getColumnValue("item_code",dom,formNo);
					//String columnValue1 = genericUtility.getColumnValue("item_code",dom,"3");
					System.out.println("Inside wfvaldata case 3 item_code...."+columnValue);
					//System.out.println("Inside wfvaldata case 3 BY E12...."+columnValue1);
					if (columnValue != null) 
					{
						siteCode = getColumnValue("site_code",dom ,"1");
						custCode =getColumnValue("cust_code",dom ,"1");
						tranId =getColumnValue("tran_id",dom,"1");
						if (!columnValue.equals("") && checkNextCol)
						{
							System.out.println("Inside wfvaldata case 3 item_code. checkNextCol..."+checkNextCol);
							tranDate1 = getColumnValue("tran_date",dom);
							errCode = "";
							errCode = isItem(siteCode,columnValue,"S-CSTK",conn);
							System.out.println("Inside wfvaldata case 3 errCode...."+errCode);
							if(errCode.length()>0)
							{
								/*errString = getErrorString("item_code",errCode,userId);
								checkNextCol = false;*/
								errString = itmDBAccess.getErrorString("item_code",errCode,userId,null,conn);
								// 11/09/09 manoharan to include the item/site code in the log file generated
								sqlCnt = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '" + columnValue + "'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sqlCnt);
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
							selQueryBuff = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0)	FROM CUST_STOCK_DET, CUST_STOCK ");  
							selQueryBuff.append("WHERE ( CUST_STOCK.TRAN_ID = CUST_STOCK_DET.TRAN_ID ) AND ");
							selQueryBuff.append("( ( CUST_STOCK_DET.ITEM_CODE = '").append(columnValue).append("' ) AND "); 
							selQueryBuff.append("( CUST_STOCK_DET.TRAN_ID = '").append(tranId ).append("') AND  ");
							selQueryBuff.append("( CUST_STOCK.CUST_CODE = '").append(custCode).append("' ) ) ");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
							if (rs.next())
							{
								if (rs.getInt(1) == 1)
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT LINE_NO FROM CUST_STOCK_DET, CUST_STOCK WHERE ( CUST_STOCK.TRAN_ID = CUST_STOCK_DET.TRAN_ID ) AND  ");
									selQueryBuff.append("( ( CUST_STOCK_DET.ITEM_CODE = '").append(columnValue).append("' ) AND "); 
									selQueryBuff.append("( CUST_STOCK_DET.TRAN_ID = '").append(tranId).append("' ) AND  ");
									selQueryBuff.append("( CUST_STOCK.CUST_CODE = '").append(custCode).append("' ) ) ");
									stmt2 = conn.createStatement();
									rs2 = stmt.executeQuery(selQueryBuff.toString());
									if (rs2.next())
									{
										lineNo = getColumnValue("line_no",dom ,formNo);
										if (Integer.parseInt(lineNo) != rs2.getInt(1))
										{
											//errCode = "VTDUPIT";
											errString = itmDBAccess.getErrorString("line_no","VTDUPIT",userId,null,conn); 
											break;
										}
									}
									if (stmt2 != null)
									{
										stmt2.close();
										stmt2 = null;
									}
								}
							}
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							/*if (errCode.length() !=0 )
							{
								errString = getErrorString("item_code",errCode,userId);
								checkNextCol = false;
							}*/
						}
					}
					columnValue = getColumnValue("cl_stock",dom,formNo);
					System.out.println("Inside wfvaldata case 3 cl_stock...."+columnValue);
					if (columnValue != null)
					{
						itemCode =getColumnValue("item_code",dom,formNo);
						custCode = getColumnValue("cust_code",dom1);
						tranDate1 = getColumnValue("tran_date",dom1);
						if (!columnValue.equals("") && checkNextCol)
						{
							selQueryBuff  = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMERITEM WHERE CUST_CODE = '");
							selQueryBuff.append(custCode).append("' AND ITEM_CODE = '");
							selQueryBuff.append(itemCode).append("' ");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
							if (rs.next())
							{
								if (rs.getInt(1) > 0)
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT RESTRICT_UPTO FROM CUSTOMERITEM	WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND ITEM_CODE = '");
									selQueryBuff.append(itemCode).append("' ");
									stmt2 = conn.createStatement();
									rs2 = stmt.executeQuery(selQueryBuff.toString());
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
												errString = itmDBAccess.getErrorString("cl_stock","VTRESDT",userId,null,conn); 
												break;
											}

										}
									}
									if (stmt2 != null)
									{
										stmt2.close();
										stmt2 = null;
									}
								}
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
					columnValue = getColumnValue("item_ser",dom,formNo);
					System.out.println("Inside wfvaldata case 3 item_ser...."+columnValue);
					if (columnValue != null) 
					{
						custCode = getColumnValue("cust_code",dom1);
						if (!columnValue.equals("") && checkNextCol)
						{
							selQueryBuff = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM CUSTOMER_SERIES "); 
							selQueryBuff.append("WHERE CUST_CODE = '").append(custCode);
							selQueryBuff.append("' AND ITEM_SER = '").append(columnValue).append("'");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
							
							System.out.println("Inside wfvaldata case 3 item_se EXE...."+selQueryBuff.toString());
							if (rs.next())
							{
								if (rs.getInt(1)==0)
								{
									//errCode = "VTITEM7";
									errString = itmDBAccess.getErrorString("item_ser","VTITEM7",userId,null,conn); 
									break;
								}
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
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :wfValData :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		return errString;
	}

	//wfValData(Document dom) method ends here
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

	//public String itemChanged(String xmlString , String currentColumn, String formNo, String domID) throws RemoteException,ITMException
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		/*//Changed by Danish on 06/02/2007
		Document dom = null;
		String valueXmlString = "";
		try
		{			
			dom = parseString(xmlString); //returns the DOM Object for the passed XML Stirng
			valueXmlString = itemChanged(dom,currentColumn, formNo, domID);
		}
		catch(Exception e)
		{
		System.out.println("Exception :CustStockEJB :itemChanged :==>\n"+e.getMessage());
		}
		return valueXmlString; //calls itemChanged(Document dom,String currentColumn) method and returns string*/

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
			errString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println ("ErrString :" + errString);
		}
		catch (Exception e)
		{
			//System.out.println ("Exception :CustStockEJB :itemChanged(String,String):" + e.getMessage() + ":");
			errString = genericUtility.createErrorString(e);
			throw new ITMException( e );
		}
		System.out.println ("returning from CustStockEJB itemChanged");
		return errString;
	}
	
	//Changed by Danish on 06/02/2007
	//public String itemChanged(Document dom , String currentColumn, String formNo, String domID) throws RemoteException,ITMException
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String domID = "";//Changed by Danish on 06/02/2007 temporary
		String formNo = "";//Changed by Danish on 06/02/2007 temporary
		System.out.println("[CustStockEJB]itemChanged called for column name :"+currentColumn+": formNo :"+formNo+": domID :"+domID+":");
		Connection  conn = null;
		boolean checkNextCol = true;
		String columnName = "";
		String columnValue = "";
		StringBuffer valueXmlString = null;
		StringBuffer selQueryBuff = null;
		Statement stmt = null;
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
		String salesValue = "", plistDisc = "",status = "",purcRcpSale = "" ,sql = "" ,replPeriod = "";
		String dbPattern=null;
		String invStat = "";
		String itemCode = "", purcRcp = null, purcRet = null, prdCode, transitFlg = null;
		double retQty = 0, replQty = 0, quantity = 0, transitQty = 0 ,creditLmt = 0,osAmt = 0 ,custDeposit = 0 ,custRate = 0 ,custDiscount = 0 ,netSales = 0 ,
				purcReturn = 0 ,purcRcpDob = 0 ,salesRet = 0 ,replValue = 0 ,rate = 0; 
		//Changed by Dayanand on 01/01/08 [Declare transQty]
		String transQty ="", purcQty = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		int replPrd = 0 ;
		ResultSet rs1 = null;
		String custCode = null,rSiteCode = null, locCode = null, lotNo = null, lotSl = null, retRepFlag = null;
		java.sql.Timestamp preFromDate = null, preToDate = null,preToDate1 = null ;
		String siteCodeCh="";
		try
		{
			E12GenericUtility genericUtility = new E12GenericUtility();
			DistDiscount distDiscount = new DistDiscount();
			DistCommon distCommon = new DistCommon();
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
				formNo = objContext;
			}

			//currentFormNo = Integer.parseInt(formNo);			//Changed by Danish on 06/02/2007
			if (currentColumn.equalsIgnoreCase("itm_default"))
			{
				columnValue = currentColumn;
			}
			else
			{
				columnValue = getColumnValue(currentColumn, dom, formNo);
			}
			/*else if (currentColumn.equalsIgnoreCase("op_stock") || currentColumn.equalsIgnoreCase("cl_stock"))
			{
				columnValue = getColumnValue(currentColumn, dom, formNo, domID);
			}
			else
			{
				columnValue = getColumnValue(currentColumn, dom, formNo);
			}*/
			System.out.println("columnValue :"+columnValue+":");
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			java.sql.Date currentDate = new java.sql.Date( System.currentTimeMillis() );
			
			//Added by Poonam on 09-12-2016 for GET REPL_PERIOD FROM DATABASE :Start[D16HVHB005]
			
			custCode = getColumnValue("cust_code",dom1);
			itemCode = getColumnValue("item_code",dom,formNo);
			
			System.out.println("custCode... :"+custCode+":itemCode...:"+itemCode+":");
			
			System.out.println("In S astype....");
			sql = "select repl_period from  customeritem where cust_code = ? and item_code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, custCode );
			pstmt.setString( 2, itemCode );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				replPeriod = rs.getString("repl_period");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			sql = "Select item_ser from item where item_code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, itemCode );
			rs = pstmt.executeQuery();	
			if(rs.next())
			{
				itemSer = checkNull(rs.getString("item_ser"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("mvarValue["+replPeriod+"][itemSer"+itemSer+"]");
			
			if(replPeriod == null  || replPeriod.trim().length() == 0)
			{
				sql = "select repl_period from  customer_series where cust_code = ? and item_ser = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				pstmt.setString( 2, itemSer );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					replPeriod = rs.getString("repl_period");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
			}
			
			if(replPeriod == null  || replPeriod.trim().length() == 0)
			{
				sql = "select repl_period from  customer where cust_code = ?  " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, custCode );
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					replPeriod = rs.getString("repl_period");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
			}
			if(replPeriod == null  || replPeriod.trim().length() == 0)
			{
				replPeriod = distCommon.getDisparams("999999", "REPL_PERIOD", conn);
				
				if(replPeriod == null  || replPeriod.trim().length() == 0)
				{
					replPeriod = "1";
				}
				if("NULLFOUND".equalsIgnoreCase(replPeriod))
				{
					replPeriod = "0";
				}
			}
			System.out.println("replPeriod["+replPeriod +"]");
			
			if(replPeriod != null && replPeriod.trim().length() > 0)
			{
				 replPrd = Integer.parseInt(replPeriod);
			}
			System.out.println("replPrd["+replPrd +"]");
			
			//Added by Poonam on 09-12-2016 for GET REPL_PERIOD FROM DATABASE :Start[D16HVHB005]

			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
			if (columnValue != null)
			{
				switch (currentFormNo)
				{
					case 1:
						valueXmlString.append("<Detail>\r\n");					    
					
						if (currentColumn.trim().equals("itm_default"))
						{
							System.out.println("Setting itm defualt values...");

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
							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + loginSite + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							
							java.text.SimpleDateFormat dtf = new SimpleDateFormat( getApplDateFormat() );
						    currentDate = new java.sql.Date( System.currentTimeMillis());
							
						    //Added by Poonam on 09-12-2016 for GET REPL_PERIOD FROM DATABASE :Start[D16HVHB005]
							Calendar cal = Calendar.getInstance();
						    cal.setTime(currentDate);
						    cal.add(Calendar.DATE, -(replPrd * 30)); //minus number would decrement the days
						    System.out.println("Date before "+ replPrd +" Months ::::"+cal.getTime());
						    Date prdDate = cal.getTime();
						    
						    Calendar GCal = new GregorianCalendar();
						    
						    GCal.setTime(prdDate);
						    GCal.set(Calendar.DAY_OF_MONTH,1);
							System.out.println("getApplDateFormat()----"+getApplDateFormat());
							Date gcalFrdt = GCal.getTime();
								
							if(replPeriod != null && replPeriod.trim().length() > 0 && replPrd > 1)
							{
							    System.out.println("fromDt::::::"+ dtf.format( gcalFrdt ));
							    valueXmlString.append( "<from_date>" ).append( dtf.format( gcalFrdt ) ).append( "</from_date>\r\n" );
							    
							}   //Added by Poonam on 09-12-2016 for GET REPL_PERIOD FROM DATABASE :End[D16HVHB005]
							else
							{
								 currentDate = new java.sql.Date( System.currentTimeMillis() );
								 valueXmlString.append( "<from_date>" ).append( dtf.format( currentDate ) ).append( "</from_date>\r\n" );
								
							}
							valueXmlString.append( "<to_date>" ).append( dtf.format( currentDate ) ).append( "</to_date>\r\n" );			            						                        
							valueXmlString.append( "<tran_date>" ).append( dtf.format( currentDate ) ).append( "</tran_date>\r\n" );
							//valueXmlString.append( "<from_date>" ).append( dtf.format( currentDate ) ).append( "</from_date>\r\n" ); //Commented by Poonam for set 3 months back date.

							selQuery = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER = '" + getColumnValue("item_ser",dom1) + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= '" + currDate + "' AND TO_DATE >= '"+ currDate+"' ";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
						}
						else if ( currentColumn.trim().equals("cust_code") )						
						{ 
							frdate1 = getColumnValue("from_date",dom1);
							siteCode = getColumnValue("site_code",dom1);
							selQuery = "SELECT CUST_NAME,CREDIT_LMT FROM CUSTOMER WHERE CUST_CODE = '" + columnValue + "'";//Changed for getting credit_limit
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{
								custName = rs.getString("CUST_NAME");
								creditLmt = rs.getDouble("CREDIT_LMT");
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}
							valueXmlString.append("<name>").append(custName).append("</name>\r\n");

							selQuery = "SELECT SITE_CODE FROM SITE_CUSTOMER WHERE CUST_CODE = '" + columnValue + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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

							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + siteCode + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							
							if (frdate1 != null && frdate1.trim().equals(""))
							{
								selQueryBuff = new StringBuffer();
								selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK WHERE CUST_CODE = '");
								selQueryBuff.append(columnValue).append("' AND SITE_CODE = '");
								selQueryBuff.append(siteCode).append("'");
								stmt = conn.createStatement();
								rs = stmt.executeQuery(selQueryBuff.toString());
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

							itemSer = getColumnValue("item_ser",dom1);
							if (itemSer == null || itemSer.equalsIgnoreCase("null") || itemSer.length() == 0)
							{
								selQueryBuff = new StringBuffer();
								selQueryBuff.append("SELECT ITEM_SER FROM CUSTOMER_SERIES ");
								selQueryBuff.append("WHERE CUST_CODE = '").append(columnValue).append("'");
								//selQueryBuff.append("' AND ROWNUM = 1");
								stmt = conn.createStatement();
								rs = stmt.executeQuery(selQueryBuff.toString());
								if (rs.next())
								{
									itemSer = rs.getString(1);
								}
								rs.close();
								rs = null;
								//if (stmt != null)
								//{
									stmt.close();
									stmt = null;
								//}
							}
							valueXmlString.append("<item_ser>").append(itemSer).append("</item_ser>\r\n");

							//SHWETA 2/14/2005 --Start
							tranIdLast = getColumnValue("tran_id__last",dom1);
							System.out.println("tran_id__last :"+tranIdLast+":");
							if (tranIdLast == null || tranIdLast.equalsIgnoreCase("null") || tranIdLast.length() == 0)
							{
								if (itemSer != null && itemSer.trim().length() > 0 )
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT MAX(TRAN_ID) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(columnValue).append("' AND ITEM_SER = '");
									selQueryBuff.append(itemSer).append("'");
								}
								else
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT MAX(TRAN_ID) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(columnValue).append("'");
								}
								System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
								stmt = conn.createStatement();
								rs = stmt.executeQuery(selQueryBuff.toString());
								if (rs.next())
								{
									tranIdLast = rs.getString(1);
								}
								rs.close();
								rs = null;
								System.out.println("tranIdLast :"+tranIdLast);
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
							
							
							//Added by Poonam on 09-12-2016 for display credit_limit ,deposit and Outstanding :Start[D16HVHB005]
							
							selQuery = "select fn_get_cust_deposit(?,?) ,fn_get_custos(?,?,'T') from dual";
							pstmt = conn.prepareStatement(selQuery);
							pstmt.setString(1, siteCode.toUpperCase());
							pstmt.setString(2, columnValue.toUpperCase());
							pstmt.setString(3, columnValue.toUpperCase());
							pstmt.setString(4, siteCode.toUpperCase());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								custDeposit = rs.getDouble(1);
								osAmt = rs.getDouble(2);
								
							}
							rs.close();
							rs = null;
							if (pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							System.out.println("TestMig case1 os_amt["+osAmt+"]creditLmt["+creditLmt+"]custDeposit["+custDeposit+"]");
							valueXmlString.append("<os_amt>").append(osAmt).append("</os_amt>\r\n");
							valueXmlString.append("<customer_credit_lmt>").append(creditLmt).append("</customer_credit_lmt>\r\n");
							valueXmlString.append("<deposit>").append(custDeposit).append("</deposit>\r\n");
							//Added by Poonam on 09-12-2016 for display credit_limit ,deposit and Outstanding :End[D16HVHB005]
						}
						else if (currentColumn.trim().equals("site_code"))
						{
							frdate1 = getColumnValue("from_date",dom1);
							custCodeHeader = getColumnValue("cust_code",dom1);
							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + columnValue + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							valueXmlString.append("<descr>").append(siteDescr).append("</descr>\r\n");
							if (frdate1.trim().equals(""))
							{
								selQueryBuff = new StringBuffer();
								selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK ");
								selQueryBuff.append("WHERE CUST_CODE = '").append(custCodeHeader);
								selQueryBuff.append("' AND SITE_CODE = '").append(columnValue).append("'");
								System.out.println("selQueryBuff :"+selQueryBuff.toString());
								stmt = conn.createStatement();
								rs = stmt.executeQuery(selQueryBuff.toString());
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
							tranDate = getColumnValue("tran_date",dom1);
							System.out.println("tranDate---"+tranDate);
							trnDate = getDateObject(tranDate);
							Calendar calendar = new GregorianCalendar();
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
							}*/
							/* Commented By Chandni As Date is not setting Properly
							valueXmlString.append("<tran_date>").append(lastDate).append("</tran_date>\r\n");	
							valueXmlString.append("<from_date>").append(firstDate).append("</from_date>\r\n");	
							valueXmlString.append("<to_date>").append(lastDate).append("</to_date>\r\n");
							Comment Ended*/
							//Added by chandni 19-Apr-2012
							
							calendar.setTime(trnDate);
							
							if(replPrd > 1)
							{
								calendar.add(Calendar.DATE,( -( replPrd *30) )); //added by Poonam for set from date and to date 
							}
							calendar.set(Calendar.DAY_OF_MONTH,1);
							System.out.println("getApplDateFormat()----"+getApplDateFormat());
							java.text.SimpleDateFormat dtf = new SimpleDateFormat( getApplDateFormat() );
							valueXmlString.append("<from_date>").append(dtf.format( calendar.getTime() )).append("</from_date>\r\n");
							
							calendar.setTime(trnDate);
							calendar.set(Calendar.DAY_OF_MONTH,1);
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
							custCode = getColumnValue( "cust_code" , dom1);
							String itemSeries = getColumnValue( "item_ser" , dom1);
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

							selQuery = "SELECT TRAN_ID FROM CUST_STOCK WHERE CUST_CODE = '" +custCode+ "' AND TO_DATE = '" +currDate+ "' ";
							System.out.println("selQuery "+selQuery);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							columnValue = genericUtility.getValidDateString( columnValue , getApplDateFormat() , "dd-MMM-yyyy" );
							selQuery = "SELECT CODE FROM PERIOD WHERE FR_DATE <= '" +columnValue+ "' AND TO_DATE >= '" +columnValue+ "' ";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							selQuery = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER = '" + columnValue + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
					//case 2 added by chandni : for itemchange for invoice_id 
					case 2:
						valueXmlString.append("<Detail>\r\n");
						if ( currentColumn.trim().equals("invoice_id") )
						{							
							String invoiceId ="";
							java.sql.Date invoiceDate = null;
							double netAmt = 0.0;
							java.text.SimpleDateFormat dtf = new SimpleDateFormat( getApplDateFormat() );	

							invoiceId = getColumnValue("invoice_id",dom, formNo);
								
							selQuery = "select tran_date ,net_amt from invoice where  invoice_id = '"+invoiceId+"' " ;
							stmt = conn.createStatement();
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{
								invoiceDate = rs.getDate("tran_date");
								netAmt = rs.getDouble("net_amt");
								
								valueXmlString.append( "<invoice_date>" ).append( dtf.format(invoiceDate)).append( "</invoice_date>\r\n" );
								valueXmlString.append("<net_amt>").append(netAmt).append("</net_amt>\r\n");
							}
							rs.close();
							rs = null;
							if (stmt != null)
							{
								stmt.close();
								stmt = null;
							}							
						}
						valueXmlString.append("</Detail>\r\n");
					break;
					//ended by chandni
					case 3: 
						valueXmlString.append("<Detail>\r\n");
						selQuery = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'CUST_STOCK_MODE'";
						System.out.println("Executing Query .............\n" +selQuery);
						stmt = conn.createStatement();
						rs = stmt.executeQuery(selQuery);
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
															
						purReceipt = getColumnValue("purc_rcp",dom, formNo);
						adjQty = getColumnValue("adj_qty",dom, formNo);
						purReturn = getColumnValue("purc_ret",dom, formNo);
						
						System.out.println("Values at end purReceipt["+purReceipt+"]purReturn["+purReturn+"]adjQty["+adjQty+"]");
						
												
						//rateValue = getRate( getColumnValue("cust_code", dom), getColumnValue("tran_date", dom), getColumnValue("item_code",dom, formNo, domID));						
						System.out.println("Calculating rate for cust_code :"+getColumnValue("cust_code", dom1)+": tran_date :"+getColumnValue("tran_date", dom1)+": item_code :"+genericUtility.getColumnValue("item_code",dom)+":");
						rateValue = getRate( getColumnValue("cust_code", dom1), getColumnValue("tran_date", dom1), genericUtility.getColumnValue("item_code",dom));
						System.out.println("Assigned value from rateValue getRate function:"+rateValue);						
                        //Changed by Dayanand on 21/11/07 [Add new field cust_item__ref and add item_code itemchange in this field]start
						////Added to add new elseif option 'A' to Unprotect cl_stock column, Ruchira 22/02/2k7(DI7SUN0038).				
						if ( currentColumn.trim().equals("itm_default") || currentColumn.trim().equals("itm_defaultedit") )
						{											
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
						else if ( currentColumn.trim().equals("cust_item__ref") )
						{							
							System.out.println("Enter in cust_item_code_ref itemchange");
							custCode = getColumnValue("cust_code" , dom1);
							selQuery = "SELECT ITEM_CODE FROM CUSTOMERITEM WHERE  ITEM_CODE__REF = '"+columnValue+"' AND CUST_CODE='"+custCode+"'";
							stmt = conn.createStatement();
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{
								itemCode = rs.getString("ITEM_CODE");
								System.out.println("rs.getString(ITEM_CODE)["+rs.getString("ITEM_CODE")+"]");
								valueXmlString.append("<item_code>").append(itemCode).append("</item_code>\r\n");
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
							siteCode = getColumnValue("site_code" , dom1);
							tranDate = getColumnValue("tran_date" , dom1 );
							tranIdLast = getColumnValue("tran_id__last" ,dom1);							
							System.out.println("itemCode [" +itemCode+ "]cust_code["+custCode+"]siteCode["+siteCode+"]tranDate["+tranDate+"]tranIdLast["+tranIdLast+"]");							
										
							selQuery = "SELECT DESCR, UNIT, LOC_TYPE, ITEM_SER FROM ITEM WHERE  ITEM_CODE = '"+itemCode+"'";
							stmt = conn.createStatement();
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{
								unit = rs.getString("UNIT");
								System.out.println("unit [" +unit+ "]");
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
							selQuery = "SELECT CL_STOCK FROM CUST_STOCK_DET WHERE  TRAN_ID = '"+tranIdLast+"'	AND ITEM_CODE = '"+itemCode+"'";
												 
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{
								opStock = rs.getString("CL_STOCK");
								System.out.println( "opStock [" +opStock+ "]");
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

							String ldtFromDate = getColumnValue("from_date" , dom1);
							System.out.println("ldtFromDate [" +ldtFromDate+ "]");
							String toDate = getColumnValue("to_date" , dom1);
							System.out.println("toDate [" +toDate+ "]");

							//String ldt_to_date = Calendar(ldt_from_date).add(Date,-1);
							//Calendar preCalc = Calendar.getInstance();
							//preCalc.setTime( getDateObject( ldtFromDate ) );
							//preCalc.add( Calendar.DATE, -1 );				
							//java.util.Date ldtToDate = preCalc.getTime();	
							//java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );						    
							//String currDate = dtf.format( ldtToDate );	
							//System.out.println("currDate [" +currDate+ "]");
							//String ldtFromDate = dtf.format( ldFromDate );
							ldtFromDate = genericUtility.getValidDateString( ldtFromDate , getApplDateFormat() , "dd-MMM-yyyy");
							System.out.println("ldtFromDate [" +ldtFromDate+ "]");
							toDate = genericUtility.getValidDateString( toDate , getApplDateFormat() , "dd-MMM-yyyy");
							//toDate = dtf.format( toDate );
						
							//ldt_to_date = DateTime( RelativeDate( date( ldt_from_date ), -1 ) )
					
							/*selQuery = "SELECT  FR_DATE, TO_DATE FROM PERIOD WHERE " + currDate + " BETWEEN FR_DATE AND TO_DATE" ;
							stmt = conn.createStatement();
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);*/
							//commented by chandni 21-mar-2012
							/*
							selQuery = "SELECT SUM(B.QUANTITY__STDUOM) purc_rcp FROM INVOICE A, INVDET B WHERE  A.INVOICE_ID = B.INVOICE_ID  AND 	A.CUST_CODE = '"  + custCode + "' AND 	(A.TRAN_DATE >= '"+ldtFromDate+"' AND A.TRAN_DATE <=  '" +toDate+ "') AND 	B.ITEM_CODE = '" + itemCode + "' ";
							*/
							// added site_code condition by chandni 21-mar-2012
							selQuery = "SELECT SUM(B.QUANTITY__STDUOM) purc_rcp FROM INVOICE A, INVDET B WHERE  A.INVOICE_ID = B.INVOICE_ID  AND 	A.CUST_CODE = '"  + custCode + "' AND 	(A.TRAN_DATE >= '"+ldtFromDate+"' AND A.TRAN_DATE <=  '" +toDate+ "') AND 	B.ITEM_CODE = '" + itemCode + "' AND A.SITE_CODE = '"+siteCode+"'";
							// endded by chandni
							stmt = conn.createStatement();
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);
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
							/// 31/01/09 manoharan commented and aded new code as per ITM
							//String custCode = getColumnValue("cust_code" , dom1);
							//siteCode = getColumnValue("site_code" , dom1);
							//tranDate = getColumnValue("tran_date" , dom1 );
							//tranIdLast = getColumnValue("tran_id__last" ,dom1);
							////itemCode = columnValue;
							//System.out.println("itemCode [" +columnValue+ "]custCode["+custCode+"]siteCode["+siteCode+"]tranDate["+tranDate+"]tranIdLast["+tranIdLast+"]");							
							//		
							//selQuery = "SELECT DESCR, UNIT, LOC_TYPE, ITEM_SER FROM ITEM WHERE  ITEM_CODE = '"+columnValue+"'";
							//stmt = conn.createStatement();
							//System.out.println("selQuery [" +selQuery+ "]");
							//rs = stmt.executeQuery(selQuery);
							//if (rs.next())
							//{			    
							//	unit = rs.getString("UNIT");
							//	System.out.println("unit [" +unit+ "]");
							//    locType = rs.getString("LOC_TYPE");
							//	itemSer = rs.getString("ITEM_SER");
							//	valueXmlString.append("<item_ser>").append(itemSer).append("</item_ser>\r\n");
							//	valueXmlString.append("<unit>").append(unit).append("</unit>\r\n");
							//	valueXmlString.append("<loc_type>").append(locType).append("</loc_type>\r\n");
							//}	
							//else
							//{
							//	valueXmlString.append("<item_ser>").append(opStock).append("</item_ser>\r\n");
							//	valueXmlString.append("<unit>").append(opStock).append("</unit>\r\n");
							//	valueXmlString.append("<loc_type>").append(opStock).append("</loc_type>\r\n");
							//}
							//if (stmt != null)
							//{
							//	stmt.close();
							//	stmt = null;
							//}
							////TAKING PREV TRANSACTION CLOSING BALANCE 
							//selQuery = "SELECT CL_STOCK FROM CUST_STOCK_DET WHERE  TRAN_ID = '"+tranIdLast+"'	AND ITEM_CODE = '"+columnValue+"'";
							//stmt = conn.createStatement();
							//rs = stmt.executeQuery(selQuery);
							//System.out.println("selQuery [" +selQuery+ "]");
							//if (rs.next())
							//{
							//	opStock = rs.getString("CL_STOCK");
							//	System.out.println( "opStock [" +opStock+ "]");								
							//	valueXmlString.append("<op_stock>").append(opStock).append("</op_stock>\r\n");																							
							//}
							//else
							//{
							//	valueXmlString.append( "<op_stock>0</op_stock>\r\n" );
							//}
							//if (stmt != null)
							//{
							//	stmt.close();
							//	stmt = null;
							//}
							//					        							
							//String ldtFromDate = getColumnValue("from_date" , dom1);
							//System.out.println("ldtFromDate [" +ldtFromDate+ "]");
							//String toDate = getColumnValue("to_date" , dom1);
							//System.out.println("toDate [" +toDate+ "]");
							//
							////String ldt_to_date = Calendar(ldt_from_date).add(Date,-1);
							////Calendar preCalc = Calendar.getInstance();
							////preCalc.setTime( getDateObject( ldtFromDate ) );
							////preCalc.add( Calendar.DATE, -1 );				
							////java.util.Date ldtToDate = preCalc.getTime();	
							////java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );						    
							////String currDate = dtf.format( ldtToDate );	
							////System.out.println("currDate [" +currDate+ "]");
							//////String ldtFromDate = dtf.format( ldFromDate );
							//ldtFromDate = genericUtility.getValidDateString( ldtFromDate , getApplDateFormat() , "dd-MMM-yyyy");
							//System.out.println("ldtFromDate [" +ldtFromDate+ "]");
							////toDate = genericUtility.getValidDateString( toDate , getApplDateFormat() , "dd-MMM-yyyy");
							////toDate = dtf.format( toDate );
						
							////ldt_to_date = DateTime( RelativeDate( date( ldt_from_date ), -1 ) )
					
							/*//selQuery = "SELECT  FR_DATE, TO_DATE FROM PERIOD WHERE " + currDate + " BETWEEN FR_DATE AND TO_DATE" ;
							//stmt = conn.createStatement();
							//System.out.println("selQuery [" +selQuery+ "]");
							//rs = stmt.executeQuery(selQuery);*/
							//	
							//selQuery = "SELECT SUM(B.QUANTITY__STDUOM) purc_rcp FROM INVOICE A, INVDET B WHERE  A.INVOICE_ID = B.INVOICE_ID  AND 	A.CUST_CODE = '"  + custCode + "' AND 	(A.TRAN_DATE >= '"+ldtFromDate+"' AND A.TRAN_DATE <=  '" +toDate+ "') AND 	B.ITEM_CODE = '" + columnValue + "' ";
							//stmt = conn.createStatement();
							//System.out.println("selQuery [" +selQuery+ "]");
							//rs = stmt.executeQuery(selQuery);
							//if (rs.next())
							//{			 				
							//	//String purc_rcp = rs.getString("purc_rcp") != null
							//	String purcRcp = rs.getString("purc_rcp");
							//	if ( purcRcp != null )
							//	{
							//		valueXmlString.append("<purc_rcp>").append( purcRcp ).append("</purc_rcp>\r\n");
							//	}
							//	else
							//	{
							//		valueXmlString.append( "<purc_rcp>0</purc_rcp>\r\n" );
							//	}	
							//}														
						//}												
						//Changed by Dayanand on 11/12/07 [Add itemchange for item code] end
						//// 31/01/09 manoharan commented and aded new code as per ITM
						
							//// 31/01/09 manoharan ITM changes ////////////////////////////////////////////////////////////////////////////////////////////////////
							ibase.webitm.ejb.sys.UtilMethods utilMethods = ibase.webitm.ejb.sys.UtilMethods.getInstance();
							custCode = getColumnValue("cust_code" , dom1);
							siteCode = getColumnValue("site_code" , dom1);
							tranDate = getColumnValue("tran_date" , dom1);
							
							Date tranDT = sdf1.parse(tranDate);
							tranIdLast = getColumnValue("tran_id__last" ,dom1);
							prdCode = getColumnValue("prd_code" , dom1 );
							itemCode = columnValue;
	
							//chandni siteCodeCh 09/Apr/12
							System.out.println("hdr.siteCode::"+siteCode+"hdr.custCode::"+custCode);
							selQuery =" select site_code__ch from site_customer where site_code ='"+siteCode+"' and cust_code ='"+custCode+"' and channel_partner = 'Y' " ;
							stmt = conn.createStatement();
							System.out.println("selQuery [" +selQuery+ "]");
							rs = stmt.executeQuery(selQuery);
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
								valueXmlString.append("<item_descr>").append(itemDescr).append("</item_descr>\r\n");
							}	
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
				
							java.sql.Timestamp tsprevDate1 = null;
							
							String fromDate = getColumnValue("from_date" , dom1 );
							System.out.println("fromDate... [" +fromDate+ "]");
							java.sql.Timestamp tsfromDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( fromDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
							
							String toDate = getColumnValue("to_date" , dom1 ); //Added By poonam for set to date 
							String tranDte = getColumnValue("tran_date" , dom1 ); //Added By poonam
							java.sql.Timestamp tstoDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( toDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
							java.sql.Timestamp tstranDte = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( tranDte , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
							
							System.out.println("tsfromDate... [" +tsfromDate+ "]");
							java.sql.Timestamp tsprevDate = utilMethods.RelativeDate(tsfromDate, -1);
							
							System.out.println("tsprevDate... [" +tsprevDate+ "]");
							
							SimpleDateFormat sdtApplDate = new SimpleDateFormat(getApplDateFormat());
							String prevDate = sdtApplDate.format(tsprevDate);
							
							
							selQuery = "SELECT FR_DATE, TO_DATE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE" ;	//added for previous month period code			
							pstmt = conn.prepareStatement(selQuery);
							//pstmt.setTimestamp(1,tsprevDate);
							pstmt.setTimestamp(1,tstranDte);
							rs = pstmt.executeQuery();
							if (rs.next())
							{			    
								preFromDate = rs.getTimestamp("FR_DATE");
								preToDate1 = rs.getTimestamp("TO_DATE");
								tsprevDate1 = utilMethods.RelativeDate(preFromDate, -1);
							}	
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
							
							System.out.println("tsprevDate1["+tsprevDate1+"]tsprevDate["+tsprevDate+"]");
							System.out.println("1 ST PERIODpreToDate1["+preToDate1+"]preFromDate["+preFromDate+"]");
								
							selQuery = "SELECT SUM(DET.CL_STOCK)AS OP_STOCK FROM CUST_STOCK HDR, CUST_STOCK_DET DET "
									+ " WHERE HDR.TRAN_ID = DET.TRAN_ID "
									+ " AND	HDR.CUST_CODE = ? "
									+ " AND	HDR.SITE_CODE = ? "
									+ " AND	HDR.TO_DATE = ? "
									+ " AND DET.ITEM_CODE = ? ";
							
							pstmt = conn.prepareStatement(selQuery);
							pstmt.setString(1,custCode);
							// pstmt.setString(2,siteCode);
							pstmt.setString(2,siteCodeCh); //Added by Chandni 11/Apr/2012
							
							//if(replPrd > 1)
							//{
								pstmt.setTimestamp(3,tsprevDate1);
							//}
							/*else
							{
								pstmt.setTimestamp(3,tsprevDate);
							}*/
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
							System.out.println("opStock [" +opStock+ "]");
							String invListString = "";
							selQuery = "SELECT FR_DATE, TO_DATE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE" ;				
							pstmt = conn.prepareStatement(selQuery);
							//pstmt.setTimestamp(1,tsprevDate);
							pstmt.setTimestamp(1,tsfromDate);
							rs = pstmt.executeQuery();
							if (rs.next())
							{			    
								preFromDate = rs.getTimestamp("FR_DATE");
								preToDate = rs.getTimestamp("TO_DATE");
								//invListString = GetInvList(dom2, preFromDate, preToDate); //Commented by poonam 
							}	
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
							
							System.out.println("2ND preToDate["+preToDate+"]preFromDate["+preFromDate+"]");
							
							if(replPrd > 1)
							{
								invListString = GetInvList(dom2, tsfromDate, preToDate); //Added by Poonam for get 3 months invoice
							}
							else
							{
								invListString = GetInvList(dom2, preFromDate, preToDate); //Commented by poonam 
							}
							
							System.out.println("invListString::::["+invListString+"]");
							
							fromDate = getColumnValue("from_date" , dom1 );
							toDate = getColumnValue("to_date" , dom1 ); //Added By poonam for set to date 
							java.sql.Timestamp tsFromDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( fromDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
							java.sql.Timestamp tsToDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( toDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
							// commented by chandni 21-mar-2012
							 /* 
							selQuery = "SELECT SUM(B.QUANTITY__STDUOM)PURC_RCP FROM INVOICE A, INVDET B "
								+ " WHERE  A.INVOICE_ID = B.INVOICE_ID"
								+ " AND A.CUST_CODE = ? "
								+ " AND B.ITEM_CODE = ? "
								+ " AND ( (A.TRAN_DATE >= ? AND A.TRAN_DATE <= ?) ";
										
							if ( invListString.trim().length() > 0  )
							{
								selQuery += " OR A.INVOICE_ID IN (" + invListString  + ")" ;
							}
							selQuery += ")" ;
								
							pstmt = conn.prepareStatement(selQuery);
							pstmt.setString(1,custCode);
							pstmt.setString(2,itemCode);
							pstmt.setTimestamp(3,tsFromDate);
							pstmt.setTimestamp(4,tsToDate);
								*/
							// added site_code condition by chandni 21-mar-2012
							
							System.out.println("tsFromDate:::"+tsFromDate+"preToDate"+preToDate+"tsToDate"+tsToDate);
							selQuery = "SELECT SUM(B.QUANTITY__STDUOM)PURC_RCP FROM INVOICE A, INVDET B "
								+ " WHERE  A.INVOICE_ID = B.INVOICE_ID"
								+ " AND A.CUST_CODE = ? "
								+ " AND B.ITEM_CODE = ? "
								+ " AND A.SITE_CODE = ? "
								+ " AND ( (A.TRAN_DATE >= ? AND A.TRAN_DATE <= ?) ";
								
							if ( invListString.trim().length() > 0  )
							{
								selQuery += " OR A.INVOICE_ID IN (" + invListString  + ")" ;
							}
							selQuery += ")" ;
								
							pstmt = conn.prepareStatement(selQuery);
							pstmt.setString(1,custCode);
							pstmt.setString(2,itemCode);
							pstmt.setString(3,siteCode);
							pstmt.setTimestamp(4,tsFromDate);
							
							/*if(replPrd > 1)
							{
								pstmt.setTimestamp(5,preToDate);
							}
							else
							{*/
								pstmt.setTimestamp(5,tsToDate);
							//}
							// ended by chandni
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
							System.out.println("purcRcp [" +purcRcp+ "]");							
							
							System.out.println("preFromDate:::"+preFromDate+"preToDate"+preToDate+"preToDate1::"+preToDate1);
							
							selQuery = "SELECT B.QUANTITY__STDUOM QUANTITY, B.ITEM_CODE ITEM_CODE, "
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
								//pstmt.setTimestamp(2,preFromDate);
								//pstmt.setTimestamp(3,preToDate);
								pstmt.setTimestamp(2,preFromDate);
								if(replPrd > 1)
								{
									pstmt.setTimestamp(3,preToDate1);
								}
								else
								{
									pstmt.setTimestamp(3,preToDate);
								}
							
								pstmt.setString(4,itemCode);
								pstmt.setString(5,siteCode);
								
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
	
								pstmt1.setString(1,itemCode);
								pstmt1.setString(2,rSiteCode);
								pstmt1.setString(3,locCode);
								pstmt1.setString(4,lotNo);
								pstmt1.setString(5,lotSl);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									invStat = rs1.getString("INV_STAT");
									if ("R".equalsIgnoreCase(retRepFlag) && "SALE".equalsIgnoreCase(invStat.trim()) )
									{
										retQty += quantity;
									}
									else if ( "P".equalsIgnoreCase(retRepFlag) )
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
							System.out.println("quantity:"+quantity+":retQty:"+retQty+":replQty:"+replQty);
							quantity = retQty - replQty ;
							
							if(replPrd > 1)
							{
								purReturn = Double.toString(quantity); //Added By poonam for Set purchhase return value.
							}
							
							valueXmlString.append("<purc_ret>").append(quantity).append("</purc_ret>\r\n");
							System.out.println("purc_ret [" +quantity+ "]purReturn[ "+purReturn+"]");
							
							transitQty = 0;
							
							transitFlg = getColumnValue("transit_flag" , dom1);
							System.out.println("Chandni transitFlg:"+transitFlg);
							if ( transitFlg == null || transitFlg.trim().length() == 0)
							{
								transitFlg = "N";
							}
							if ("N".equalsIgnoreCase(transitFlg.trim()))
							{
								transitQty = GetTransitQty(dom2, itemCode, conn );
							}
							valueXmlString.append("<transit_qty>").append(transitQty).append("</transit_qty>\r\n");
							System.out.println("transitQty [" +transitQty+ "]");
							prdCode = getColumnValue("prd_code" , dom1);
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
							if (rs.next())
							{			    
								quantity = rs.getDouble("QUANTITY");
								valueXmlString.append("<adhoc_repl_qty>").append(quantity).append("</adhoc_repl_qty>\r\n");
							}	
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
							System.out.println("adhoc_repl_qty [" +quantity+ "]");
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
								valueXmlString.append("<cust_item_ref_descr>").append(custItemRefDescr).append("</cust_item_ref_descr>\r\n");
							}	
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
							/////End 31/01/09 manoharan ///////////////////////////////////////////////////////////
							//Added by Poonam on 09-12-2016 for discount, latest rate  and net amount :Start[D16HVHB005]
							
							double purcRcpSales = Double.parseDouble(purcRcp);
							System.out.println("purcRcpSales["+purcRcpSales+"]purcReturn["+purcReturn);
							
							custRate = getRate(custCode, tranDate, itemCode);
						    plistDisc = distDiscount.priceListDiscount(siteCode, custCode, conn);
						    
						    System.out.println("TestMig Case3 custRate["+custRate+"]plistDisc["+plistDisc+"]");
							if(plistDisc == null || plistDisc.trim().length() == 0)
							{
								plistDisc = "";
							}
							custDiscount = distDiscount.getDiscount(plistDisc, tranDT, custCode, siteCode, itemCode, unit, 0, tranDT, purcRcpSales, conn);
							
							System.out.println("TestMig custDiscount["+custDiscount+"]");
							
					/*		
							selQuery = "SELECT B.QUANTITY__STDUOM QUANTITY, B.ITEM_CODE ITEM_CODE, "
							+ " A.SITE_CODE SITE_CODE, B.LOC_CODE LOC_CODE, "
							+ " B.LOT_NO LOT_NO, B.LOT_SL LOT_SL, B.RET_REP_FLAG RET_REP_FLAG "
							+ " FROM  SRETURN A, SRETURNDET B "
							+ " WHERE A.TRAN_ID = B.TRAN_ID "
							+ " AND A.CUST_CODE = ? "
							+ " AND A.TRAN_DATE >= ? "
							+ " AND A.TRAN_DATE <= ? "
							+ " AND B.ITEM_CODE = ? "
							+ " AND A.SITE_CODE = ? "
							+ " AND B.STATUS = 'S' ";
						pstmt = conn.prepareStatement(selQuery);
						pstmt.setString(1,custCode);
						pstmt.setTimestamp(2,tsFromDate);
						pstmt.setTimestamp(3,preToDate);
						pstmt.setString(4,itemCode);
						pstmt.setString(5,siteCode);
						
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

							pstmt1.setString(1,itemCode);
							pstmt1.setString(2,rSiteCode);
							pstmt1.setString(3,locCode);
							pstmt1.setString(4,lotNo);
							pstmt1.setString(5,lotSl);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								invStat = rs1.getString("INV_STAT");
								if ("R".equalsIgnoreCase(retRepFlag) && "SALE".equalsIgnoreCase(invStat.trim()) )
								{
									retQty += quantity;
								}
								else if ( "P".equalsIgnoreCase(retRepFlag) )
								{
									replQty += quantity;
								}
							}
							rs1.close();
							rs1 = null;

							}	
							pstmt1.close();pstmt1 = null;
							rs.close();	pstmt.close();
							rs = null;pstmt = null;
					
							purcReturn = retQty - replQty ;
					
							System.out.println("purcReturn:"+purcReturn+":retQty:"+retQty+":replQty:"+replQty);
					
							if(purcRcp != null && purcRcp.trim().length() > 0)
							{
								purcRcpSales = Double.parseDouble(purcRcp);
							}
				
							System.out.println("TestMig status["+status+"]purcRcpSales["+purcRcpSales+"]");
					
							netAmount = purcRcpSales - purcReturn;*/
								
							System.out.println("TestMig custRate["+custRate+"]custDiscount["+custDiscount+"]");
							valueXmlString.append("<rate>").append(custRate).append("</rate>\r\n");
							valueXmlString.append("<discount>").append(custDiscount).append("</discount>\r\n");
							//valueXmlString.append("<net_amt>").append(netAmount).append("</net_amt>\r\n");
							//Added by Poonam on 09-12-2016 for discount, latest rate  and net amount :End[D16HVHB005]
						}
						else if (currentColumn.trim().equals("op_stock"))
						{
							opStock = columnValue;
							System.out.println("op_stock currentColumn ["+currentColumn+"]");
							System.out.println("line 1295 opStock["+opStock+"]");
							
							//Added by poonam for null pointer exception removable	
							Double OpStockLc= opStock != null ? Double.parseDouble(opStock) : 0 ;
							Double purReceiptLc= purReceipt != null ? Double.parseDouble(purReceipt) : 0 ;
							Double salesLc= sales != null ? Double.parseDouble(sales) : 0 ;
							Double adjQtyLc= adjQty != null ? Double.parseDouble(adjQty) : 0 ;
							Double purReturnLc= purReturn != null ? Double.parseDouble(purReturn) : 0 ;
							Double clStksLc= clStks != null ? Double.parseDouble(clStks) : 0 ;
							if (stockMode.trim().equals("S"))
							{
								sales = getColumnValue("sales",dom,formNo); //, formNo, domID);
								if(sales != null && sales.trim().length() > 0 )
								{
									salesLc = Double.parseDouble(sales);
								}
								//clStk = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(sales) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								clStk = OpStockLc + purReceiptLc - salesLc + adjQtyLc - purReturnLc;	//Added by poonam for null pointer exception							
								valueXmlString.append("<cl_stock>").append(clStk).append("</cl_stock>\r\n");
							}
							else if (stockMode.trim().equals("C"))
							{
								clStks = getColumnValue("cl_stock",dom,formNo); //, formNo, domID);
								
								if(clStks != null && clStks.trim().length() > 0 )
								{
									 clStksLc = Double.parseDouble(clStks);
								}
								//saless = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(clStks) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								saless = OpStockLc + purReceiptLc - clStksLc + adjQtyLc - purReturnLc; //Added by poonam for null pointer exception	
								
								if(saless > 0)
								{
									valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
								}
								else
								{
									valueXmlString.append("<sales>").append("<![CDATA[" + 0 + "]]>").append("</sales>\r\n");
								}
							}
						}
						else if (currentColumn.trim().equals("cl_stock"))
						{							
							clStks = columnValue;
							opStock = getColumnValue("op_stock",dom, formNo); //, domID);
							
							Double OpStockLc= opStock != null ? Double.parseDouble(opStock) : 0 ;
							Double purReceiptLc= purReceipt != null ? Double.parseDouble(purReceipt) : 0 ;
							Double salesLc= sales != null ? Double.parseDouble(sales) : 0 ;
							Double adjQtyLc= adjQty != null ? Double.parseDouble(adjQty) : 0 ;
							Double purReturnLc= purReturn != null ? Double.parseDouble(purReturn) : 0 ;
							Double clStksLc= clStks != null ? Double.parseDouble(clStks) : 0 ;
							
							System.out.println(" cl_stock currentColumn ["+currentColumn+"]");
							System.out.println("line 1316 opStock["+opStock+"]");

							if (stockMode.trim().equals("S"))
							{
								valueXmlString.append("<cl_stock>").append(columnValue).append("</cl_stock>\r\n");
							}
							else if (stockMode.trim().equals("C"))
							{
								//saless = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(clStks) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								saless = OpStockLc + purReceiptLc - clStksLc + adjQtyLc - purReturnLc; //Added by poonam for null pointer exception	
								//valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
								
								if(saless > 0)
								{
									valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
								}
								else
								{
									valueXmlString.append("<sales>").append("<![CDATA[" + 0 + "]]>").append("</sales>\r\n");
								}
							}
						}//added by poonam for purc_rcp itemchange for set sales quantity.
						else if (currentColumn.trim().equals("purc_rcp"))
						{							
							purcRcp = columnValue;
							opStock = getColumnValue("op_stock",dom, formNo); //, domID);
							String clStock = getColumnValue("cl_stock",dom, formNo); //, domID);
							
							System.out.println("Inside purc_rcp IC purcRcp["+purcRcp+"]opStock["+opStock+"]clStock["+clStock+"]");
							
							Double OpStockLc= opStock != null ? Double.parseDouble(opStock) : 0 ;
							Double purReceiptLc= purcRcp != null ? Double.parseDouble(purcRcp) : 0 ;
							Double adjQtyLc= adjQty != null ? Double.parseDouble(adjQty) : 0 ;
							Double purReturnLc= purReturn != null ? Double.parseDouble(purReturn) : 0 ;
							Double clStksLc= clStks != null ? Double.parseDouble(clStks) : 0 ;
							
							System.out.println(" cl_stock currentColumn ["+currentColumn+"]");
							System.out.println("line 1316 opStock["+opStock+"]");

							if (stockMode.trim().equals("S"))
							{
								valueXmlString.append("<cl_stock>").append(clStock).append("</cl_stock>\r\n");
							}
							else if (stockMode.trim().equals("C"))
							{
								//saless = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(clStks) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
								saless = OpStockLc + purReceiptLc - clStksLc + adjQtyLc - purReturnLc; //Added by poonam for null pointer exception	
								//valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
								
								if(saless > 0)
								{
									valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
								}
								else
								{
									valueXmlString.append("<sales>").append("<![CDATA[" + 0 + "]]>").append("</sales>\r\n");
								}
							}
						}
						//genericUtility.serializeDom( dom );
						//genericUtility.serializeDom( dom1 );
						//genericUtility.serializeDom( dom2 );
						
						
						DecimalFormat df = new DecimalFormat();
						df.applyPattern("##.00");		
						if (!currentColumn.trim().equals("item_code"))				
						{
							opStock = genericUtility.getColumnValue("op_stock", dom,formNo);
							
							//Changed by Dayanand on 8/25/2008 [Change the transit quantity calculation]start (reqId DI89SUN082)
							purReceipt = genericUtility.getColumnValue("purc_rcp", dom,formNo);
							System.out.println( "Inside item_code [" + purReceipt  + "]opStock"+opStock +"]formNo["+formNo +"]");
							//purReceipt = genericUtility.getColumnValue("cust_purc_rcp", dom);
						}
						sales = genericUtility.getColumnValue("sales", dom,formNo);
						clStks = genericUtility.getColumnValue("cl_stock", dom,formNo);						
						String rate1 = genericUtility.getColumnValue("rate", dom,formNo);						
						String discount = genericUtility.getColumnValue("discount", dom,formNo);						
						
						System.out.println( "opStock BEFORE [" + opStock  + "]" );
						System.out.println( "clStks ["  +  clStks  + "]" );
						System.out.println( "sales ["   +  sales   + "]" );
						System.out.println( "adjQty ["+adjQty+ "]" );
						System.out.println( "purReceipt ["+purReceipt+ "]" );
						System.out.println( "purReturn ["+purReturn+ "]" );						
						System.out.println( "rate1 ["+rate1+ "]" );		
						
						if(rate1 !=  null && rate1.trim().length() > 0)
						{
							rate = Double.parseDouble(rate1);
						}
						if(discount !=  null && discount.trim().length() > 0)
						{
							custDiscount = Double.parseDouble(discount);
						}
						System.out.println( "rate:::: ["+rate+ "]" );		
						
						if (opStock == null || opStock == "null" || opStock.trim().length() == 0 )
						{
							opStock = "0";
						}
						if (clStks == null || clStks == "null" || clStks.trim().length() == 0  )
						{
							clStks = "0";
						}
						if (sales == null || sales == "null" || sales.trim().length() == 0  )
						{
							sales = "0";
						}
						if (purReceipt == null || purReceipt == "null" || purReceipt.trim().length() == 0  )
						{
							purReceipt = "0";
						}
						if (adjQty == null || adjQty == "null" || adjQty.trim().length() == 0  )
						{
							adjQty = "0";
						}
						if (purReturn == null || purReturn == "null" || purReturn.trim().length() == 0  )
						{
							purReturn = "0";
						}
						Double OpStockLc= opStock != null ? Double.parseDouble(opStock) : 0 ;
						Double purReceiptLc= purReceipt != null ? Double.parseDouble(purReceipt) : 0 ;
						Double salesLc= sales != null ? Double.parseDouble(sales) : 0 ;
						Double adjQtyLc= adjQty != null ? Double.parseDouble(adjQty) : 0 ;
						Double purReturnLc= purReturn != null ? Double.parseDouble(purReturn) : 0 ;
						Double clStksLc= clStks != null ? Double.parseDouble(clStks) : 0 ;
						if (stockMode.trim().equals("S"))
						{
							sales = getColumnValue("sales",dom,formNo); //, formNo, domID);
							if (sales == null || sales == "null" || sales.trim().length() == 0  )
							{
								sales = "0";
							}
							//clStk = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(sales) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
							clStk = OpStockLc + purReceiptLc - salesLc + adjQtyLc - purReturnLc;	//Added by poonam for null pointer exception
							valueXmlString.append("<cl_stock>").append(clStk).append("</cl_stock>\r\n");
							clStks = clStk + "";
						}
						else if (stockMode.trim().equals("C"))
						{
							clStks = getColumnValue("cl_stock",dom, formNo);//, domID);
							if (clStks == null || clStks == "null" || clStks.trim().length() == 0  )
							{
								clStks = "0";
							}
							System.out.println( "opStock [" + opStock  + "]purReceipt["+purReceipt+"]clStks["+clStks+"]adjQty["+adjQty+"]purReturn["+purReturn+"]");
							//saless = Double.parseDouble(opStock) + Double.parseDouble(purReceipt) - Double.parseDouble(clStks) + Double.parseDouble(adjQty) - Double.parseDouble(purReturn);
							saless = OpStockLc + purReceiptLc - clStksLc + adjQtyLc - purReturnLc; //Added by poonam for null pointer exception	
							
							//valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
							
							if(saless > 0)
							{
								valueXmlString.append("<sales>").append(saless).append("</sales>\r\n");
							}
							else
							{
								valueXmlString.append("<sales>").append("<![CDATA[" + 0 + "]]>").append("</sales>\r\n");
							}
							sales = saless + "";
							
							System.out.println( "Sales Stock [" + saless  + "]");
							clStk = OpStockLc + purReceiptLc - saless + adjQtyLc - purReturnLc;	//Added by poonam for null pointer exception
							valueXmlString.append("<cl_stock>").append(clStk).append("</cl_stock>\r\n");
							clStks = clStk + "";
							
							System.out.println( "Closing Stock [" + clStk  + "]");
						}
						System.out.println( "opStock [" + opStock  + "]" );
						System.out.println( "clStks ["  +  clStks  + "]" );
						System.out.println( "sales after ["   +  sales   + "]" );
						System.out.println( "cust_purc_rcp ["+purReceipt+ "]" );
						System.out.println( "saless ["+ saless+ "]" );
						
						/*  09/02/09 commented by manoharan  
						//Changed by Dayanand on 01/01/08 [ Calculate transQty] start (reqId DI78SUN073)						
						//transQty = df.format(new Float(Float.parseFloat( opStock ) + Float.parseFloat( purReceipt ) - Float.parseFloat( sales )- Float.parseFloat( clStks ))); 
						transQty = df.format(new Float(Float.parseFloat( opStock ) + Float.parseFloat( purReceipt ) - Float.parseFloat( sales )- Float.parseFloat( clStks ))); 
						System.out.println( "transQty["  +  transQty  + "]" );
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
						
						if ( opStock != null && opStock.length() > 0 && opStock != "null")
						{
							opValue = df.format(new Float( Float.parseFloat( opStock ) * rateValue ));
						}
						if ( clStks != null && clStks.length() > 0 && clStks != "null" )
						{
							clValue = df.format(new Float(Float.parseFloat( clStks ) * rateValue));
						}
						if ( sales != null && sales.length() > 0 && sales != "null")
						{
							salesValue = df.format(new Float(Float.parseFloat(String.valueOf( sales )) * rateValue));
						}
						if ( purReceipt != null && purReceipt.length() > 0 && purReceipt != "null" )
						{
							purValue = df.format(new Float( Float.parseFloat( purReceipt ) * rateValue));
						}	
						
						//Added by Poonam on 09-12-2016 for discount, latest rate  and net amount :Start[D16HVHB005]
						
						String mvarValue = "";
						
						double mvarValueDb = 0;
						double msales = 0;
						
						//sales = genericUtility.getColumnValue("sales", dom,formNo);
						itemCode = genericUtility.getColumnValue("item_code", dom,formNo);		
						//clStks = genericUtility.getColumnValue("cl_stock", dom,formNo);		
						//transQty = genericUtility.getColumnValue("transit_qty", dom,formNo);		
						
						Double clStksDb = clStks != null ? Double.parseDouble(clStks) : 0 ;
						
						//Double transQtyDb = clStks != null ? Double.parseDouble(transQty) : 0 ;
						
						System.out.println("In S astype....sales["+sales+"]itemCode["+itemCode+"]clStks["+clStks+"]transQty["+transQty+"]");
						System.out.println("clStksDb.....["+clStksDb+"]transQtyDb["+transitQty+"]");
						sql = "select repl_factor from  customeritem where cust_code = ? and item_code = ? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, custCode );
						pstmt.setString( 2, itemCode );
						rs = pstmt.executeQuery();	
						if(rs.next())
						{
							mvarValue = rs.getString("repl_factor");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						sql = "Select item_ser from item where item_code = ? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, itemCode );
						rs = pstmt.executeQuery();	
						if(rs.next())
						{
							itemSer = checkNull(rs.getString("item_ser"));
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("mvarValue["+mvarValue+"][itemSer"+itemSer+"]");
						
						if(mvarValue == null  || mvarValue.trim().length() == 0)
						{
							sql = "select repl_factor from  customer_series where cust_code = ? and item_ser = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, custCode );
							pstmt.setString( 2, itemSer );
							rs = pstmt.executeQuery();	
							if(rs.next())
							{
								mvarValue = rs.getString("repl_factor");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
						}
						
						if(mvarValue == null  || mvarValue.trim().length() == 0)
						{
							sql = "select repl_factor from  customer where cust_code = ?  " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, custCode );
							rs = pstmt.executeQuery();	
							if(rs.next())
							{
								mvarValue = rs.getString("repl_factor");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							
						}
						if(mvarValue == null  || mvarValue.trim().length() == 0)
						{
							mvarValue = distCommon.getDisparams("999999", "REPL_FACTOR", conn);
							
							if(mvarValue == null  || mvarValue.trim().length() == 0)
							{
								mvarValue = "0";
							}
							if("NULLFOUND".equalsIgnoreCase(mvarValue))
							{
								mvarValue = "0";
							}
						}
					
						if(mvarValue != null && mvarValue.trim().length() > 0)
						{
							mvarValueDb = Double.parseDouble(mvarValue) ;
						}
						if(sales != null && sales.trim().length() > 0)
						{
							salesLc = Double.parseDouble(sales) ;
						}
						
						if(replPrd > 1)
						{
							salesLc = salesLc / replPrd ;
							
							msales =  Math.round(salesLc);
						}
						System.out.println("replPrd["+replPrd +"]msales after["+msales+"]");
						
						msales = msales - clStksDb - transitQty ;
						
						System.out.println("mminQty["+mvarValueDb +"]salesLc["+ msales +"]");
						
						netSales = msales  * mvarValueDb;
						System.out.println("netSales::::::["+netSales +"]");
						
						replValue = netSales * rate ;
						
						System.out.println("replValue::::::["+replValue +"]");
							                    
						if(netSales > 0)
						{
							valueXmlString.append("<net_sales>").append(Math.round(netSales)).append("</net_sales>\r\n"); //Added by Poonam on 09-12-2016 for discount, latest rate  and net amount :End[D16HVHB005]
						}
						else
						{
							valueXmlString.append("<net_sales>").append(0).append("</net_sales>\r\n"); 
						}
						valueXmlString.append("<repl_value>").append(Math.round(replValue)).append("</repl_value>\r\n"); //Added by Poonam on 09-12-2016 for discount, latest rate  and net amount :End[D16HVHB005]
						
						valueXmlString.append("<rate>").append(rate).append("</rate>\r\n");
						valueXmlString.append("<discount>").append(custDiscount).append("</discount>\r\n");
						valueXmlString.append("<op_value>").append(opValue).append("</op_value>\r\n");
						valueXmlString.append("<cl_value>").append(clValue).append("</cl_value>\r\n");
						valueXmlString.append("<sales_value>").append(salesValue).append("</sales_value>\r\n");
						valueXmlString.append("<pur_value>").append(purValue).append("</pur_value>\r\n");
						valueXmlString.append("</Detail>\r\n");
						
						System.out.println(valueXmlString.toString());
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
		return valueXmlString.toString();
	}//itemChanged(Document,String) method ends here   */

	private float getRate(String custCode, String tranDate, String itemCode)throws RemoteException,ITMException
	{
		//ibase.webitm.utility.GenericUtility genericUtility = new ibase.webitm.utility.GenericUtility();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		System.out.println("[CustStockEJB]Calcuating rate for the custCode :"+custCode+": tranDate :"+tranDate+": itemCode :"+itemCode+":");
		Connection conn = null;		
		Statement stmt = null;
		ResultSet rs = null;
		String priceList = null;
		float retRate = 0f;
		try
		{			
			String selQuery = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
			System.out.println("[CustStockEJB]Getting PRICE_LIST value, query :\n"+selQuery);
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selQuery);
			while (rs.next())
			{
				priceList = rs.getString(1);
			}
			if (rs != null)
			{
				rs.close();
			}
			
			////Uncommet by Daynand on 25/08/07 []
			/* Commented by Nazia 21-8-07
			selQuery = "SELECT DDF_PICK_MAX_RATE('"+priceList+"', TO_DATE('"+tranDate+"', '"+getDBDateFormat()+"'), '"+itemCode+"') FROM DUAL";
			System.out.println("[CustStockEJB]Getting rate value, query :\n"+selQuery);
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
			selQuery = "SELECT DDF_PICK_MAX_RATE('"+priceList+"', TO_DATE('"+tranDate+"', '"+getDBDateFormat()+"'), '"+itemCode+"') FROM DUAL";
			System.out.println("[CustStockEJB]Getting rate value, query :\n"+selQuery);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selQuery);
			while (rs.next())
			{
				retRate = rs.getFloat(1);
				System.out.println("retRate [" +retRate+ "]");				
			}
			if (stmt != null)
			{
				stmt.close();
			}
			
			if(conn!= null)
			{
				conn.close() ;
				conn = null ;
			}
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :getRate :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("[CustStockEJB]rate :"+retRate);
		return retRate;
		//System.out.println("returning value from getRate as 1");
		//return 1;
	}
	private String GetInvList(Document dom, java.sql.Timestamp fromDate, java.sql.Timestamp toDate) throws Exception
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
		
		try
		{
			E12GenericUtility genericUtility = new  E12GenericUtility();
			parentNodeList = dom.getElementsByTagName("Detail2");
			int childNodeListLength = parentNodeList.getLength();
			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				System.out.println("ctr  ................"+ctr);
				parentNode = parentNodeList.item(ctr);
				
				invoiceDate = genericUtility.getColumnValueFromNode("invoice_date", parentNode);
				invoiceId = genericUtility.getColumnValueFromNode("invoice_id", parentNode);
				dlvFlg = genericUtility.getColumnValueFromNode("dlv_flg", parentNode);
				tsInvoiceDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( invoiceDate , getApplDateFormat() , getDBDateFormat()) + " 00:00:00.000" );
				if ( "Y".equalsIgnoreCase(dlvFlg) && tsInvoiceDate.compareTo(fromDate) >= 0 && tsInvoiceDate.compareTo(toDate) <= 0)
				{
					if ( invoiceListString.trim().length() == 0 )
					{
						invoiceListString = "'" + invoiceId + "'" ;
					}
					else
					{
						invoiceListString += ",'" + invoiceId + "'" ;
					}
				}
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			//System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		System.out.println("invoiceListString  ................["+invoiceListString+"]");
		return invoiceListString ;
	}
	private double GetTransitQty(Document dom, String itemCode, Connection conn) throws Exception
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
			E12GenericUtility genericUtility = new E12GenericUtility();
			parentNodeList = dom.getElementsByTagName("Detail2");
			int childNodeListLength = parentNodeList.getLength();
			sql = "SELECT SUM(QUANTITY__STDUOM)QUANTITY FROM INVDET "
				+ " WHERE  INVOICE_ID = ? "
				+ " AND ITEM_CODE = ? " ;
			
			pstmt = conn.prepareStatement(sql);
			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				System.out.println("ctr  ................"+ctr);
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
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
//			System.out.println("Exception ::" + exception.getMessage());
			throw new ITMException( exception );
		}
		return transitQty ;
	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input.trim();
	}
}