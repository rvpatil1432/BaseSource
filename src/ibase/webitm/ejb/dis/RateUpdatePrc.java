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

//public class RateUpdatePrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class RateUpdatePrc extends ProcessEJB implements RateUpdatePrcLocal, RateUpdatePrcRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	DistCommon distCommon = new DistCommon();
	
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
	
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("RateUpdatePrcEJB ejbCreate called.........");
			
		}
		catch (Exception e)
		{
			System.out.println("Exception :RateUpdatePrcEJB :ejbCreate :==>"+e);
			throw new CreateException();
		}
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
				System.out.println("XML String *.....*:"+xmlString);
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
			System.out.println("Exception :RateUpdatePrcEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
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
		java.sql.Timestamp trDate = new java.sql.Timestamp(System.currentTimeMillis());
		String sql = "";
		String errCode = "";
		String errString = "";
		String resultString = "";
		String listType = "";
		String tranDate = "";
		String itemCode = "";
		double newRate = 0.0;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out.println("xtraParams $$$$$$$$$$$$$$$$$$$$$$$$ "+xtraParams);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			
			Object date = null;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(trDate.toString());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());	
			tranDate = sdf1.format(date).toString();
			System.out.println("tranDate............."+tranDate);;
			
			priceList = genericUtility.getColumnValue("price_list",headerDom);
			System.out.println("priceList :::- "+ priceList);
			if(priceList == null || priceList.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTPLIST00","","",conn);
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
						errString = itmDBAccessEJB.getErrorString("price_list",errCode,userId,"",conn);
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
				errString = itmDBAccessEJB.getErrorString("","VTMSG","","",conn);
				return errString;
			}
	
			custCodeTo = genericUtility.getColumnValue("cust_code__to",headerDom);
			System.out.println("custCodeTo is :::"+custCodeTo);
			if(custCodeTo == null || custCodeTo.trim().length() ==0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTMSG","","",conn);
				return errString;
			}

			sorderDateFrom = genericUtility.getColumnValue("order_date__from",headerDom);
			System.out.println("order_date__from is :::"+sorderDateFrom);
			if ( sorderDateFrom == null || sorderDateFrom.trim().length() == 0 )
			{
				sorderDateFrom = "";
				System.out.println("order_date__from is null.............");
				errString = itmDBAccessEJB.getErrorString("","INVDT1","","",conn);
				return errString;
			} 
			sorderDateFrom = genericUtility.getValidDateString(sorderDateFrom, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			orderDateFrom = java.sql.Timestamp.valueOf(sorderDateFrom + " 00:00:00");
			System.out.println("Order Date From **.........................**:"+orderDateFrom);
			
			sorderDateTo = genericUtility.getColumnValue("order_date__to",headerDom);
			System.out.println("order_date__to is :::"+sorderDateTo);
			if ( sorderDateTo == null || sorderDateTo.trim().length() == 0 )
			{
				sorderDateTo = "";
				System.out.println("order_date__to is null.............");
				errString = itmDBAccessEJB.getErrorString("","INVDT1","","",conn);
				return errString;
			} 		
			sorderDateTo = genericUtility.getValidDateString(sorderDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			orderDateTo = java.sql.Timestamp.valueOf(sorderDateTo + " 00:00:00");
			System.out.println("Order Date To **..........................**:"+orderDateTo);
			
			sdueDateFrom = genericUtility.getColumnValue("due_date__from",headerDom);
			System.out.println("due_date__from is :::"+sdueDateFrom);
			if ( sdueDateFrom == null || sdueDateFrom.trim().length() == 0 )
			{
				sdueDateFrom = "";
				System.out.println("due_date__from is null.............");
				errString = itmDBAccessEJB.getErrorString("","INVDT1","","",conn);
				return errString;
			} 
			sdueDateFrom = genericUtility.getValidDateString(sdueDateFrom, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dueDateFrom = java.sql.Timestamp.valueOf(sdueDateFrom + " 00:00:00");
			System.out.println("Due Date From **..........................**:"+dueDateFrom);
			
			sdueDateTo = genericUtility.getColumnValue("due_date__to",headerDom);
			System.out.println("due_date__to is ::: "+sdueDateTo);
			if ( sdueDateTo == null || sdueDateTo.trim().length() == 0 )
			{
				sdueDateTo = "";
				System.out.println("due_date__to is null.............");
				errString = itmDBAccessEJB.getErrorString("","INVDT1","","",conn);
				return errString;
			} 
			sdueDateTo = genericUtility.getValidDateString(sdueDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			dueDateTo = java.sql.Timestamp.valueOf(sdueDateTo + " 00:00:00");
			System.out.println("Due Date To **..........................**:"+dueDateTo);
			
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
				
				System.out.println("The getDataSQL becomes .................:"+sql);	
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
					itemCode = rs.getString(5);
					
					retTabSepStrBuff.append(itemCode).append("\t");
					//ITEM_DESCR
					retTabSepStrBuff.append(rs.getString(6)).append("\t");	
					//QUANTITY
					retTabSepStrBuff.append(rs.getDouble(7)).append("\t");
					//RATE
					retTabSepStrBuff.append(rs.getDouble(8)).append("\t");
					
					listType = distCommon.getPriceListType(priceList,conn);
					System.out.println("The list type is :::- "+listType);
					
					newRate = distCommon.pickRate(priceList,tranDate,itemCode,"",listType,conn);
					System.out.println("The rate in the price list is  :::- "+newRate);
					
					//NEW_RATE
					retTabSepStrBuff.append(newRate).append("\n");
				}//while
			}//try
			catch (SQLException e)
			{
				System.out.println("SQLException ::RateUpdatePrcEJB :" +sql+ e.getMessage() + ":");
				throw new ITMException(e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception []::RateUpdatePrcEJB :"+ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex);
			}
			resultString = retTabSepStrBuff.toString();	
			if (!errCode.equals(""))
			{
				resultString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				System.out.println("resultString.........: " + resultString);
			}
		}//outer try
		catch (SQLException e)
		{
			System.out.println("SQLException ::RateUpdatePrcEJB :" +sql+ e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch(Exception ex)
		{
			System.out.println("Exception []::RateUpdatePrcEJB :"+ex.getMessage());
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
			System.out.println("Exception :RateupdatePrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
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
		String returnString = "";
		String sql = "",sql2 = "";
		String saleOrder = "",lineNo = "",srate = "",itemCode = "",unitRate = "",unitStd = "" ;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;	
		String childNodeName = "";
		double rate = 0;
		double convRtuomStd = 0;
		double rateStduom = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("ParentNodeListLength....::-  "+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("ChildNodeListLength.........:-"+ childNodeListLength);
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
					if (childNodeName.equals("item_code"))
					{
						itemCode = (childNode.getFirstChild().getNodeValue()).trim();
						System.out.println("item_code:::::"+ itemCode);	
					}
					if (childNodeName.equals("new_rate"))
					{
						srate = (childNode.getFirstChild().getNodeValue()).trim();
						if(srate == null)
						{
							srate = "";
						}
						rate = Double.parseDouble(srate);
						System.out.println("rate :::::"+ rate);	
					} 
				}//inner for
				lineNo = (("    "+lineNo).substring(("    "+lineNo).length()-3));
				try
				{	
					sql = "SELECT UNIT__RATE,UNIT__STD FROM SORDDET WHERE SALE_ORDER = '"+saleOrder+"' AND LINE_NO = '"+lineNo+"'";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					System.out.println("SQL......."+sql);
					if (rs.next())
					{
						unitRate = rs.getString(1);
						unitStd = rs.getString(2);
					}
					if(unitRate == null || unitRate.equals("null"))
					{
						unitRate = "";
						System.out.println("UNIT__RATE is null ...................");
					}
					if(unitStd == null || unitStd.equals("null"))
					{
						unitStd = "";
					}
					System.out.println("UNIT__RATE ::- "+unitRate);
					System.out.println("UNIT__STD ::- "+unitStd);
					stmt.close();  
					stmt = null;
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
				try
				{
					ArrayList  rateValue = new ArrayList();
					rateValue = itmDBAccessEJB.getConvQuantityFact(unitRate,unitStd,itemCode,rate,convRtuomStd,conn);	
					System.out.println("conv__rtuom_stduom.........."+rateValue.get(0));
					System.out.println("rate__stduom................"+rateValue.get(1));
					String sconvRtuomStd = (String)rateValue.get(0);
					String srateStduom = (String)rateValue.get(1);
					convRtuomStd = Double.parseDouble(sconvRtuomStd);
					rateStduom = Double.parseDouble(srateStduom);
				}
				catch (Exception e)
				{
					System.out.println("Exception ::" + e.getMessage() + ":");
					e.printStackTrace();
					throw new ITMException(e);
					
				}
				try
				{
					int cnt = 0;
					sql = "UPDATE SORDDET SET RATE = ?, RATE__STDUOM = ? WHERE SALE_ORDER = '"+saleOrder+"' AND LINE_NO = '"+lineNo+"'";
					System.out.println("Update sql ::-"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1,rate);
					pstmt.setDouble(2,rateStduom);
					cnt = pstmt.executeUpdate();
					System.out.println("No of records updated in SORDDET is : "+cnt);
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
			}//outer for
				conn.commit();
				System.out.println("Transaction commited...............");
				returnString= itmDBAccessEJB.getErrorString("","VTRUPD01","","",conn);
		}//outer try
		catch (Exception e)
		{
			System.out.println("Exception ::RateUpdatePrcEJB ::-process():" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			System.out.println("Closing Connection2....");
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
	

	