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
import java.text.*;
import java.io.File;
import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

public class CustStockTransit extends ValidatorEJB //implements SessionBean
{
	ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
  /* public void ejbCreate() throws RemoteException, CreateException 
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
	/*public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	//public String wfValData(String xmlString, String formNo, String editFlag) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		//System.out.println("Validation Start..........");
		try
		{
			//System.out.println("xmlString:::"+xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams );
		}catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :wfValData :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	//public String wfValData(String xmlString, String formNo, String editFlag) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		//System.out.println("Validation Start..........");
		try
		{
			//System.out.println("xmlString:::"+xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams );
		}catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :wfValData :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom2 = null;
		String  errString = null;
		try
		{
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams );
		}catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :wfValData :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	//public String wfValData(Document dom, String formNo, String editFlag) throws RemoteException,ITMException
	*/
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
		Statement stmt = null;
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

		//ITMDBAccessHome itmDBAccessHome = null;
		//ITMDBAccessLocal itmDBAccess = null;
		
		try
		{
			//System.out.println("CustStockEJB validation");			
			//ibase.webitm.utility.GenericUtility genericUtility = new ibase.webitm.utility.GenericUtility();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			conn = getConnection();
			userId = getColumnValue( "user_id", dom );
			currentFormNo = Integer.parseInt( objContext );
			switch ( currentFormNo )
			{
				case 1:
					columnValue = getColumnValue( "tran_date", dom );
					if(columnValue != null)
					{
						siteCode = getColumnValue( "site_code", dom );
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
					}
					columnValue = getColumnValue("cust_code",dom);
					if(columnValue != null)
					{						
						siteCode = getColumnValue("site_code",dom);
						//System.out.println("siteCode [" +siteCode+ "]");
						tranDate = getColumnValue("tran_date",dom);
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
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = '");
							selQueryBuff.append(columnValue).append("' AND SITE_CODE = '");
							selQueryBuff.append(siteCode).append("' AND NVL(ACTIVE_YN,'Y') = 'Y'");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
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
							sqlCnt = "SELECT COUNT(1) COUNT FROM CUSTOMER WHERE CUST_CODE = '" + columnValue + "' And (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END ) = 'N'";
							//System.out.println("sql :\n"+sqlCnt);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
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
					columnValue = getColumnValue("site_code",dom);					
					if(columnValue != null)
					{
						custCode = getColumnValue("cust_code",dom);
						if (checkNextCol)
						{							
							sqlCnt = "SELECT NVL(COUNT(*),0) FROM SITE WHERE SITE_CODE = '"+columnValue+"'";
							//System.out.println("sqlCnt :\n"+sqlCnt);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
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
						}
						if (checkNextCol)
						{
							selQueryBuff = new StringBuffer();
							selQueryBuff.append("SELECT NVL(COUNT(*),0) FROM SITE_CUSTOMER WHERE CUST_CODE = '");
							selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
							selQueryBuff.append(columnValue).append("' AND NVL(ACTIVE_YN,'Y') = 'Y'");
							//System.out.println("sql13 :\n"+selQueryBuff.toString());
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQueryBuff.toString());
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
						}
					}
					
					//SHWETA 2/14/2005 --Not required at the time of validation
					columnValue = getColumnValue("tran_id__last",dom);					
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
							//System.out.println("sq114 :\n"+sq114.toString());
							stmt14 = conn.createStatement();
							rs14 = stmt14.executeQuery(sq114.toString());
							if (rs14.next())
							{
								tranId = rs14.getString(1);
							}
							if (itemSer.trim().length() > 0 )
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
							//System.out.println("sql5 :\n"+sql5.toString());
							stmt5 = conn.createStatement();
							rs5 = stmt5.executeQuery(sql5.toString());
							if (rs5.next())
							{
								String tranIdLast = rs5.getString(1);
							}							
						 }
					 }
					columnValue = getColumnValue("to_date",dom);					
					if (columnValue != null) 
					{
						frdate1 = getColumnValue("from_date",dom);
						tranDate1 = getColumnValue("tran_date",dom);												
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
								custCode = getColumnValue("cust_code",dom);
								siteCode =  getColumnValue("site_code",dom);
								itemSer =  getColumnValue("item_ser",dom);
								if (itemSer.length() == 0)
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  IS NULL	AND ('");
									selQueryBuff.append(frdate1).append("' BETWEEN FROM_DATE AND TO_DATE OR '");
									selQueryBuff.append(columnValue).append("' BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
								}
								else
								{
									selQueryBuff = new StringBuffer();
									selQueryBuff.append("SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE = '");
									selQueryBuff.append(custCode).append("' AND SITE_CODE = '");
									selQueryBuff.append(siteCode).append("' AND ITEM_SER  = '").append(itemSer).append("'	AND (TO_DATE('");
									selQueryBuff.append(frdate1).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE OR TO_DATE('");
									selQueryBuff.append(columnValue).append("', '"+getDBDateFormat()+"') BETWEEN FROM_DATE AND TO_DATE) AND NVL(STATUS, 'N') != 'X'");
								}
								//System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
								stmt = conn.createStatement();
								rs = stmt.executeQuery(selQueryBuff.toString());
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
							//System.out.println("sqlCnt :\n"+sqlCnt);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
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
									custCode = getColumnValue("cust_code",dom);
									itemSer = getColumnValue("item_ser",dom);
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
									//System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
									stmt2 = conn.createStatement();
									rs2 = stmt2.executeQuery(selQueryBuff.toString());
									if (rs2.next())
									{
										if (rs2.getInt(1) == 0)
										{
											//errCode = "VTITEMSER4";
											errString = getErrorString("item_ser","VTITEMSER4",userId);//,null,conn); 
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
					columnValue = getColumnValue("sch_date__1",dom);					
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
					columnValue = getColumnValue("sch_date__2",dom);					
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
					columnValue = getColumnValue("sch_date__3",dom);					
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
					columnValue = getColumnValue("sch_date__4",dom);					
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
					columnValue = getColumnValue("sch_date__5",dom);					
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
					columnValue = getColumnValue("sch_date__6",dom);					
					if(columnValue != null) 
				    {						
						sdfFormat = new SimpleDateFormat(getApplDateFormat());														
						java.util.Date schDate6 = getDateObject( columnValue );						
						if ( schDate6.compareTo(getDateObject(sdfFormat.format( new Date()))) < 0  )
						{
							//errCode = "VTINVSCHDT";
							errString = getErrorString("sch_date__6","VTINVSCHDT",userId);//,null,conn); 
							break;
						}
					}
				break;
				case 2:
					columnValue = getColumnValue("invoice_id",dom);				    
				    if (columnValue != null) 
				    {						
						java.text.SimpleDateFormat dtf = new SimpleDateFormat( "dd-MMM-yyyy" );
						tranId = getColumnValue("tran_id",dom);
						custCode = getColumnValue("cust_code",dom);						
						String fromDate =  getColumnValue("from_date",dom) ;
						String toDate = getColumnValue("to_date",dom);
						
						Calendar preCalc = Calendar.getInstance();	
						preCalc.setTime( getDateObject( fromDate ) );
						preCalc.add( Calendar.DATE , -1 );
						java.util.Date prvDate = preCalc.getTime();						
						String fromdate = dtf.format( prvDate );													
						sqlCnt = "SELECT FR_DATE FROM PERIOD WHERE '" +dtf.format( prvDate )+ "' BETWEEN FR_DATE AND TO_DATE " ;
						//System.out.println("sqlCnt : [" +sqlCnt+ "]");
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sqlCnt);
						if (rs.next())
						{
							String frDate = rs.getString("FR_DATE"); 
							sqlCnt = " SELECT COUNT(*) COUNT FROM INVOICE WHERE INVOICE_ID = '" + columnValue + " ' AND CUST_CODE = '" + custCode + "' AND TRAN_DATE >= '" + dtf.format(frDate) + " ' AND TRAN_DATE <= '" +  dtf.format( toDate ) + " ' AND CONFIRMED ='Y' ";
							//System.out.println("sqlCnt : [" +sqlCnt+ "]");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlCnt);
							if ( rs.next() && rs.getInt("COUNT") == 0 )
							{
								errString = getErrorString("invoice_id","VTSSDINV",userId);//,null,conn); 
								break;
							}
						}
					}																							
				break;				
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
		String month = "";
		int year =0 ;
		String firstDate = "";
		String lastDate = "";
		java.util.Date trnDate = null;
		double saless = 0;
		double clStk = 0;
		int currentFormNo = 0;
		int mon = 0;
		int days =0;

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
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String custCode = null,rSiteCode = null, locCode = null, lotNo = null, lotSl = null, retRepFlag = null;
		java.sql.Timestamp preFromDate = null, preToDate = null;
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
			if (currentColumn.equalsIgnoreCase("itm_default"))
			{
				columnValue = currentColumn;
			}
			else if (currentColumn.equalsIgnoreCase("op_stock") || currentColumn.equalsIgnoreCase("cl_stock"))
			{
				columnValue = genericUtility.getColumnValue(currentColumn, dom, formNo, domID);
			}
			else
			{
				columnValue = genericUtility.getColumnValue(currentColumn, dom, formNo);
			}
			//System.out.println("columnValue :"+columnValue+":");

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
							selQuery = "SELECT DESCR FROM SITE WHERE SITE_CODE = '" + loginSite + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
							if (rs.next())
							{					
								valueXmlString.append("<descr><![CDATA[").append( rs.getString(1) ).append("]]></descr>\r\n");
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

							valueXmlString.append("<itemser_descr><![CDATA[").append(itemDescr).append("]]></itemser_descr>\r\n");
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
							/*// 04/06/09 manoharan set cust_stockmode
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
						else if ( currentColumn.trim().equals("cust_code") )						
						{ 
							frdate1 = getColumnValue("from_date",dom1);
							siteCode = getColumnValue("site_code",dom1);
							selQuery = "SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = '" + columnValue + "'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(selQuery);
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
							
							if (frdate1.trim().equals(""))
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
								selQueryBuff.append("WHERE CUST_CODE = '").append(columnValue + "'");
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

							valueXmlString.append("<item_ser><![CDATA[").append(itemSer).append("]]></item_ser>\r\n");

							//SHWETA 2/14/2005 --Start
							tranIdLast = getColumnValue("tran_id__last",dom1);
							//System.out.println("tran_id__last :"+tranIdLast+":");
							if (tranIdLast == null || tranIdLast.equalsIgnoreCase("null") || tranIdLast.length() == 0)
							{
								if (itemSer.trim().length() > 0 )
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
								//System.out.println("selQueryBuff :\n"+selQueryBuff.toString());
								stmt = conn.createStatement();
								rs = stmt.executeQuery(selQueryBuff.toString());
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

							valueXmlString.append("<descr><![CDATA[").append(siteDescr).append("]]></descr>\r\n");
							if (frdate1.trim().equals(""))
							{
								selQueryBuff = new StringBuffer();
								selQueryBuff.append("SELECT MAX(TO_DATE) + 1 FROM CUST_STOCK ");
								selQueryBuff.append("WHERE CUST_CODE = '").append(custCodeHeader);
								selQueryBuff.append("' AND SITE_CODE = '").append(columnValue).append("'");
								//System.out.println("selQueryBuff :"+selQueryBuff.toString());
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
							trnDate = getDateObject(tranDate);
							Calendar calendar = new GregorianCalendar();
							calendar.setTime(trnDate);
							mon = calendar.get(Calendar.MONTH);
							year = calendar.get(Calendar.YEAR);
							columnValue = genericUtility.getValidDateString( columnValue , getApplDateFormat() , "dd-MMM-yyyy" );
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
							valueXmlString.append("<tran_date>").append(lastDate).append("</tran_date>\r\n");	
							valueXmlString.append("<from_date>").append(firstDate).append("</from_date>\r\n");	
							valueXmlString.append("<to_date>").append(lastDate).append("</to_date>\r\n");			
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
							//System.out.println("selQuery "+selQuery);
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
		//System.out.println("[CustStockEJB]Calcuating rate for the custCode :"+custCode+": tranDate :"+tranDate+": itemCode :"+itemCode+":");
		Connection conn = null;		
		Statement stmt = null;
		ResultSet rs = null;
		String priceList = null;
		float retRate = 0f;
		try
		{			
			String selQuery = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
			//System.out.println("[CustStockEJB]Getting PRICE_LIST value, query :\n"+selQuery);
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
			selQuery = "SELECT DDF_PICK_MAX_RATE('"+priceList+"', TO_DATE('"+tranDate+"', '"+getDBDateFormat()+"'), '"+itemCode+"') FROM DUAL";
			//System.out.println("[CustStockEJB]Getting rate value, query :\n"+selQuery);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selQuery);
			while (rs.next())
			{
				retRate = rs.getFloat(1);
				//System.out.println("retRate [" +retRate+ "]");				
			}
			if (stmt != null)
			{
				stmt.close();
			}						
		}
		catch(Exception e)
		{
			//System.out.println("Exception :CustStockEJB :getRate :==>\n"+e.getMessage());
			throw new ITMException(e);
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
				+" where inv.tran_id = '" + tranId + "'"
				+"	and inv.DLV_FLG = 'Y' "
				+"	and inv.INVOICE_DATE between ? and ? ";
				
			pstmt = conn.prepareStatement( sql );
			
			pstmt.setTimestamp( 1, fromDate );
			pstmt.setTimestamp( 2, toDate );
			
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
				+"							where dtl.tran_id = '" + tranId +"'"
				+"								and dtl.dlv_flg = 'N' )"
				+"	AND iv.ITEM_CODE = '" + itemCode +"'";
			
			pstmt = conn.prepareStatement( sql );

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
}