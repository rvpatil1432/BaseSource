package ibase.webitm.ejb.dis;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import javax.ejb.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import javax.naming.InitialContext;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class TaxUpdatePrc extends ProcessEJB implements TaxUpdatePrcLocal, TaxUpdatePrcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
	String userId = "";
	String priceList = "";
	String custCodeFrom = "";
	String custCodeTo = "";
	String sorderDateFrom = "";
	String sorderDateTo = "";
	String sdueDateFrom = "";
	String sdueDateTo = "";
	java.sql.Timestamp orderDateFrom =  null;
	java.sql.Timestamp orderDateTo =  null;
	java.sql.Timestamp dueDateFrom =  null;
	java.sql.Timestamp dueDateTo =  null;
	
	/*public void Create() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("TaxUpdatePrc Create called.ejb..ejb..ejb..ejb..");
			
		}
		catch (Exception e)
		{
			System.out.println("Exception :TaxUpdatePrc :Create :==>"+e);
			throw new CreateException();
		}
	}
	public void Remove()
	{
	}
	public void Activate() 
	{
	}
	public void Passivate() 
	{
	}*/
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0 )
			{
				System.out.println("XML String *.ejb..ejb..*:"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 				
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :TaxUpdatePrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return rtrStr; 
	}//getData()
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();
		String sql = "";
		String errCode = "";
		String errString = "";
		String resultString = "";
	
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			
			priceList = genericUtility.getColumnValue("price_list",headerDom);
			System.out.println("priceList :::- "+ priceList);
			if(priceList == null || priceList.trim().length() == 0)
			{
				errString = itmDBAccess.getErrorString("","VTPLIST00","","",conn);
				return errString;
			}
			if(priceList != null && priceList.trim().length() != 0)
			{
				try
				{
					int count = 0;
					sql = "SELECT COUNT(1) AS COUNT FROM PRICELIST WHERE PRICE_LIST = '"+priceList.trim()+"'";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						count = rs.getInt("COUNT");
						System.out.println("PRICE_LIST count is ::- "+count );
					}
					if(count == 0)
					{
						errCode = "VTPLIST";
						errString = itmDBAccess.getErrorString("price_list",errCode,userId,"",conn);
						return errString;
					}
					stmt.close();
					stmt = null;
				}
				catch(Exception ex)
				{
					System.out.println("Exception []::"+sql+ex.getMessage());
					ex.printStackTrace();
				}
			}	
			custCodeFrom = genericUtility.getColumnValue("cust_code__from",headerDom);
			System.out.println("custCodeFrom is :::"+custCodeFrom);
			if(custCodeFrom == null || custCodeFrom.trim().length() ==0)
			{
				errString = itmDBAccess.getErrorString("","VTMSG","","",conn);
				return errString;
			}
	
			custCodeTo = genericUtility.getColumnValue("cust_code__to",headerDom);
			System.out.println("custCodeTo is :::"+custCodeTo);
			if(custCodeTo == null || custCodeTo.trim().length() ==0)
			{
				errString = itmDBAccess.getErrorString("","VTMSG","","",conn);
				return errString;
			}

			sorderDateFrom = genericUtility.getColumnValue("order_date__from",headerDom);
			System.out.println("order_date__from is :::"+sorderDateFrom);
			if ( sorderDateFrom == null || sorderDateFrom.trim().length() == 0 )
			{
				sorderDateFrom = "";
				System.out.println("order_date__from is null.ejb..ejb..ejb..ejb..ejb..ejb..");
				errString = itmDBAccess.getErrorString("","INVDT1","","",conn);
				return errString;
			} 
			sorderDateFrom = genericUtility.getValidDateString(sorderDateFrom, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			orderDateFrom = java.sql.Timestamp.valueOf(sorderDateFrom + " 00:00:00");
			System.out.println("Order Date From **.ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..**:"+orderDateFrom);
			
			sorderDateTo = genericUtility.getColumnValue("order_date__to",headerDom);
			System.out.println("order_date__to is :::"+sorderDateTo);
			if ( sorderDateTo == null || sorderDateTo.trim().length() == 0 )
			{
				sorderDateTo = "";
				System.out.println("order_date__to is null.ejb..ejb..ejb..ejb..ejb..ejb..");
				errString = itmDBAccess.getErrorString("","INVDT1","","",conn);
				return errString;
			} 		
			sorderDateTo = genericUtility.getValidDateString(sorderDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			orderDateTo = java.sql.Timestamp.valueOf(sorderDateTo + " 00:00:00");
			System.out.println("Order Date To **.ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb.**:"+orderDateTo);
			
			sdueDateFrom = genericUtility.getColumnValue("due_date__from",headerDom);
			System.out.println("due_date__from is :::"+sdueDateFrom);
			if ( sdueDateFrom == null || sdueDateFrom.trim().length() == 0 )
			{
				sdueDateFrom = "";
				System.out.println("due_date__from is null.ejb..ejb..ejb..ejb..ejb..ejb..");
				errString = itmDBAccess.getErrorString("","INVDT1","","",conn);
				return errString;
			} 
			sdueDateFrom = genericUtility.getValidDateString(sdueDateFrom, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dueDateFrom = java.sql.Timestamp.valueOf(sdueDateFrom + " 00:00:00");
			System.out.println("Due Date From **.ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb.**:"+dueDateFrom);
			
			sdueDateTo = genericUtility.getColumnValue("due_date__to",headerDom);
			System.out.println("due_date__to is ::: "+sdueDateTo);
			if ( sdueDateTo == null || sdueDateTo.trim().length() == 0 )
			{
				sdueDateTo = "";
				System.out.println("due_date__to is null.ejb..ejb..ejb..ejb..ejb..ejb..");
				errString = itmDBAccess.getErrorString("","INVDT1","","",conn);
				return errString;
			} 
			sdueDateTo = genericUtility.getValidDateString(sdueDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dueDateTo = java.sql.Timestamp.valueOf(sdueDateTo + " 00:00:00");
			System.out.println("Due Date To **.ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb.**:"+dueDateTo);
			
			try 
			{
				sql = "SELECT B.SALE_ORDER,B.LINE_NO,A.CUST_CODE,C.CUST_NAME,"
					+ "B.ITEM_CODE__ORD,D.DESCR,B.QUANTITY,B.RATE "
					+ "FROM SORDER A,SORDDET B,CUSTOMER C,ITEM D "
					+ "WHERE A.SALE_ORDER=B.SALE_ORDER "
					+ "AND A.CUST_CODE = C.CUST_CODE "
					+ "AND B.ITEM_CODE__ORD = D.ITEM_CODE "
					+ "AND A.CUST_CODE >= ? "
					+ "AND A.CUST_CODE <= ? "
					+ "AND A.ORDER_DATE >= ? "
					+ "AND A.ORDER_DATE <= ? "
					+ "AND A.DUE_DATE >= ? "
					+ "AND A.DUE_DATE <= ? "
					+ "AND A.STATUS = 'P' "
					+ "AND A.PRICE_LIST = ? ";
					
				pstmt = conn.prepareStatement(sql);
	
				pstmt.setString(1,custCodeFrom.trim());
				pstmt.setString(2,custCodeTo.trim());
				pstmt.setTimestamp(3,orderDateFrom);
				pstmt.setTimestamp(4,orderDateTo);
				pstmt.setTimestamp(5,dueDateFrom);
				pstmt.setTimestamp(6,dueDateTo);
				pstmt.setString(7,priceList.trim()); 
				
				System.out.println("The getDataSQL becomes .ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..:"+sql);	
				rs = pstmt.executeQuery();
	    	        	
	       		while (rs.next())
				{
					//SALE_ORDER
					retTabSepStrBuff.append(rs.getString(1)).append("\t");
					//LINE_NO
					retTabSepStrBuff.append(rs.getString(2)).append("\t");
					//CUST_CODE
					retTabSepStrBuff.append(rs.getString(3)).append("\t");
					//CUST_NAME
					retTabSepStrBuff.append(rs.getString(4)).append("\t");
					//ITEM_CODE__ORD
					retTabSepStrBuff.append(rs.getString(5)).append("\t");
					//ITEM_DESCR
					retTabSepStrBuff.append(rs.getString(6)).append("\t");	
					//QUANTITY
					retTabSepStrBuff.append(rs.getDouble(7)).append("\t");
					//RATE
					retTabSepStrBuff.append(rs.getDouble(8)).append("\n");
					
				}//while
			}//try
			catch (SQLException e)
			{
				System.out.println("SQLException ::TaxUpdatePrc :" +sql+ e.getMessage() + ":");
				throw new ITMException(e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception []::TaxUpdatePrc :"+ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex);
			}
			resultString = retTabSepStrBuff.toString();	
			if (!errCode.equals(""))
			{
				resultString = itmDBAccess.getErrorString("", errCode, "", "", conn);
				System.out.println("resultString.ejb..ejb..ejb..ejb..: " + resultString);
			}
		}//outer try
		catch (SQLException e)
		{
			System.out.println("SQLException ::TaxUpdatePrc :" +sql+ e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch(Exception ex)
		{
			System.out.println("Exception []::TaxUpdatePrc :"+ex.getMessage());
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception e)
			{}
		}		
		return resultString;	
	}//getData()
	
	//process()
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		System.out.println("xmlString2--------------*>"+ xmlString2);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
			}
		    retStr = process(headerDom, detailDom, windowName, xtraParams);  
		}
		catch (Exception e)
		{
			System.out.println("Exception :TaxUpdatePrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}//process()
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;	
		String childNodeName = "";	
		String returnString = "";
		String sql = "",taxCode1 = "",taxCode2 = "",taxCode3 = "",taxCode4 = "",taxCode5 = "";
		String saleOrder = "",lineNo = "",snewRate1 = "",snewRate2 = "",snewRate3 = "",snewRate4 = "",snewRate5 = "";
		double newRate1 = 0,newRate2 = 0,newRate3 = 0,newRate4 = 0,newRate5 = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		int cnt = 0;
		boolean flag = false;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			taxCode1 = genericUtility.getColumnValue("tax_code__1",headerDom);
			taxCode2 = genericUtility.getColumnValue("tax_code__2",headerDom);
			taxCode3 = genericUtility.getColumnValue("tax_code__3",headerDom);
			taxCode4 = genericUtility.getColumnValue("tax_code__4",headerDom);
			taxCode5 = genericUtility.getColumnValue("tax_code__5",headerDom);
			
			snewRate1 = genericUtility.getColumnValue("new_rate__1",headerDom);
			snewRate2 = genericUtility.getColumnValue("new_rate__2",headerDom);
			snewRate3 = genericUtility.getColumnValue("new_rate__3",headerDom);
			snewRate4 = genericUtility.getColumnValue("new_rate__4",headerDom);
			snewRate5 = genericUtility.getColumnValue("new_rate__5",headerDom);
			
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("ParentNodeListLength.ejb..ejb.::-  "+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("ChildNodeListLength.ejb..ejb..ejb..ejb..:-"+ childNodeListLength);
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					
					if (childNodeName.equals("sale_order"))
					{
						saleOrder = (childNode.getFirstChild().getNodeValue()).trim();
						System.out.println("sale_order:::::"+ saleOrder);	
					}
					if (childNodeName.equals("line_no"))
					{
						lineNo = (childNode.getFirstChild().getNodeValue()).trim();
						System.out.println("line_no:::::"+ lineNo);	
					}
				}//inner for
				lineNo = (("    "+lineNo).substring(("    "+lineNo).length()-3));
				if(taxCode1 != null && !taxCode1.equals("null"))
				{
					System.out.println("taxCode1 ::: "+taxCode1);
					if(snewRate1 != null && !snewRate1.equals("null"))
					{
						newRate1 = Double.parseDouble(snewRate1);
						System.out.println("newRate1 ::: "+newRate1);
				
						try
						{
							cnt = 0;
							sql = "UPDATE TAXTRAN SET TAX_PERC = ? , CHG_STAT = 'F' "
								+ "WHERE TRAN_CODE = 'S-ORD' "
								+ "AND TRAN_ID = '"+saleOrder+"' "
								+ "AND LINE_NO = '"+lineNo+"' "
								+ "AND TAX_CODE = '"+taxCode1+"'";
								
							System.out.println("Update sql ::-"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,newRate1);
							
							cnt = pstmt.executeUpdate();
							if(cnt == 1)
							{
								flag = true;
							}
							System.out.println("No of records updated in TAXTRAN is : "+cnt);
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
							
						}
						catch (Exception e)
						{
							System.out.println("Exception ::" + e.getMessage() + ":");
							e.printStackTrace();
							throw new ITMException(e);
							
						}
					}//inner if
				}//outer if
				if(taxCode2 != null && !taxCode2.equals("null"))
				{
					System.out.println("taxCode2 ::: "+taxCode2);
					if(snewRate2 != null && !snewRate2.equals("null"))
					{
						newRate2 = Double.parseDouble(snewRate2);
						System.out.println("newRate2 ::: "+newRate2);
				
						try
						{
							cnt = 0;
							sql = "UPDATE TAXTRAN SET TAX_PERC = ?, CHG_STAT = 'F' "
								+ "WHERE TRAN_CODE = 'S-ORD' "
								+ "AND TRAN_ID = '"+saleOrder+"' "
								+ "AND LINE_NO = '"+lineNo+"' "
								+ "AND TAX_CODE = '"+taxCode2+"'";
								
							System.out.println("Update sql ::-"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,newRate2);
							
							cnt = pstmt.executeUpdate();
							if(cnt == 1)
							{
								flag = true;
							}
							System.out.println("No of records updated in TAXTRAN is : "+cnt);
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
							
						}
						catch (Exception e)
						{
							System.out.println("Exception ::" + e.getMessage() + ":");
							e.printStackTrace();
							throw new ITMException(e);
							
						}
					}//inner if
				}//outer if
				if(taxCode3 != null && !taxCode3.equals("null"))
				{
					System.out.println("taxCode3 ::: "+taxCode3);
					if(snewRate3 != null && !snewRate3.equals("null"))
					{
						newRate3 = Double.parseDouble(snewRate3);
						System.out.println("newRate3 ::: "+newRate3);
				
						try
						{
							cnt = 0;
							sql = "UPDATE TAXTRAN SET TAX_PERC = ?, CHG_STAT = 'F' "
								+ "WHERE TRAN_CODE = 'S-ORD' "
								+ "AND TRAN_ID = '"+saleOrder+"' "
								+ "AND LINE_NO = '"+lineNo+"' "
								+ "AND TAX_CODE = '"+taxCode3+"'";
								
							System.out.println("Update sql ::-"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,newRate3);
							
							cnt = pstmt.executeUpdate();
							if(cnt == 1)
							{
								flag = true;
							}
							System.out.println("No of records updated in TAXTRAN is : "+cnt);
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
							
						}
						catch (Exception e)
						{
							System.out.println("Exception ::" + e.getMessage() + ":");
							e.printStackTrace();
							throw new ITMException(e);
							
						}
					}//inner if
				}//outer if
				if(taxCode4 != null && !taxCode4.equals("null"))
				{
					System.out.println("taxCode4 ::: "+taxCode4);
					if(snewRate4 != null && !snewRate4.equals("null"))
					{
						newRate4 = Double.parseDouble(snewRate4);
						System.out.println("newRate4 ::: "+newRate4);
				
						try
						{
							cnt = 0;
							sql = "UPDATE TAXTRAN SET TAX_PERC = ?, CHG_STAT = 'F' "
								+ "WHERE TRAN_CODE = 'S-ORD' "
								+ "AND TRAN_ID = '"+saleOrder+"' "
								+ "AND LINE_NO = '"+lineNo+"' "
								+ "AND TAX_CODE = '"+taxCode4+"'";
								
							System.out.println("Update sql ::-"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,newRate4);
							
							cnt = pstmt.executeUpdate();
							if(cnt == 1)
							{
								flag = true;
							}
							System.out.println("No of records updated in TAXTRAN is : "+cnt);
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
							
						}
						catch (Exception e)
						{
							System.out.println("Exception ::" + e.getMessage() + ":");
							e.printStackTrace();
							throw new ITMException(e);
							
						}
					}//inner if
				}//outer if
				if(taxCode5 != null && !taxCode5.equals("null"))
				{
					System.out.println("taxCode5 ::: "+taxCode5);
					if(snewRate5 != null && !snewRate5.equals("null"))
					{
						newRate5 = Double.parseDouble(snewRate5);
						System.out.println("newRate5 ::: "+newRate5);
				
						try
						{
							cnt = 0;
							sql = "UPDATE TAXTRAN SET TAX_PERC = ?, CHG_STAT = 'F' "
								+ "WHERE TRAN_CODE = 'S-ORD' "
								+ "AND TRAN_ID = '"+saleOrder+"' "
								+ "AND LINE_NO = '"+lineNo+"' "
								+ "AND TAX_CODE = '"+taxCode5+"'";
								
							System.out.println("Update sql ::-"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,newRate5);
							
							cnt = pstmt.executeUpdate();
							if(cnt == 1)
							{
								flag = true;
							}
							System.out.println("No of records updated in TAXTRAN is : "+cnt);
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
							
						}
						catch (Exception e)
						{
							System.out.println("Exception ::" + e.getMessage() + ":");
							e.printStackTrace();
							throw new ITMException(e);
							
						}
					}//inner if
				}//outer if
			}//outer for
			if(flag)
			{
				conn.commit();
				System.out.println("Transaction commited.ejb..ejb..ejb..ejb..ejb..ejb..ejb..");
				returnString= itmDBAccess.getErrorString("","VTTUPD01","","",conn);
			}
		}//outer try
		catch (Exception e)
		{
			System.out.println("Exception ::TaxUpdatePrc ::- process():" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			System.out.println("Closing Connection2.ejb..ejb.");
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception se){}
		}//
		return returnString;
	}//process()
}//class 
	

	