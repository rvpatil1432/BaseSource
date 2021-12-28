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


//public class RateUpdateDisOrdPrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class RateUpdateDisOrdPrc extends ProcessEJB implements RateUpdateDisOrdPrcLocal, RateUpdateDisOrdPrcRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	DistCommon distCommon = new DistCommon();
	ConnDriver connDriver = new ConnDriver();
	
	String userId = null;
	String priceList = null;
	String siteCodeFrom = null;
	String siteCodeTo = null;
	String sorderDateFrom = null;
	String sorderDateTo = null;
	String sdueDateFrom = null;
	String sdueDateTo = null;
	java.sql.Timestamp orderDateFrom =  null;
	java.sql.Timestamp orderDateTo =  null;
	java.sql.Timestamp dueDateFrom =  null;
	java.sql.Timestamp dueDateTo =  null;
	Connection conn=null;
	PreparedStatement pstmt=null;
	Statement stmt = null;
	ResultSet rs=null;
	java.sql.Timestamp trDate = new java.sql.Timestamp(System.currentTimeMillis());
	/*
	public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("RateUpdateDisOrdPrcEJB ejbCreate called.........");
			
		}
		catch (Exception e)
		{
			System.out.println("Exception :RateUpdateDisOrdPrcEJB :ejbCreate :==>"+e);
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
				System.out.println("XML String :::"+xmlString);
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
			System.out.println("Exception :RateUpdateDisOrdPrcEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return rtrStr; 
	}//getData()
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		
		
		StringBuffer retTabSepStrBuff = new StringBuffer();
		String sql = null;
		String errCode = ""; 
		String errString = "";
		String resultString ="";
		String listType = null;
		String tranDate = null;
		String itemCode = null;
		double newRate = 0.0;
		int count=0;
		try
		{
			if(conn==null)
			{
			
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				System.out.println("Connection established **************");
			}
			System.out.println("xtraParams $$$$$$$$$$$$$$$$$$$$$$$$ "+xtraParams);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			if(userId!=null)
			System.out.println("UserId::::["+userId+"]");
			
			tranDate = getApplicationDate(trDate);
			System.out.println("Transaction  Date:::["+tranDate+"]");
			
			priceList = genericUtility.getColumnValue("price_list",headerDom);
			
			if(priceList == null || priceList.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTPLIST00","","",conn);
				return errString;
			}
			if(priceList != null && priceList.trim().length() != 0)
			{
				System.out.println("priceList :::- ["+ priceList+"]");
				try
				{
					count = 0;
					sql = "SELECT COUNT(1) AS COUNT FROM PRICELIST WHERE PRICE_LIST = '"+priceList.trim()+"'";
					System.out.println("Price List Master Sql ::-["+sql+"]" );
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						count = rs.getInt("COUNT");
						System.out.println("PRICE_LIST count is ::-["+count+"]" );
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
			siteCodeFrom = genericUtility.getColumnValue("site_code__from",headerDom);
			System.out.println("siteCodeFrom:::-["+siteCodeFrom+"]");
			if(siteCodeFrom == null || siteCodeFrom.trim().length() ==0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTMSG","","",conn);
				return errString;
			}
	
			siteCodeTo = genericUtility.getColumnValue("site_code__to",headerDom);
			System.out.println("siteCodeTo is :::["+siteCodeTo+"]");
			
			if(siteCodeTo == null || siteCodeTo.trim().length() ==0)
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
				sql = "SELECT B.DIST_ORDER,B.LINE_NO,B.ITEM_CODE,C.DESCR,A.SITE_CODE__SHIP,A.SITE_CODE__DLV,"
					+ " B.RATE "
					+ "FROM DISTORDER A,DISTORDER_DET B,ITEM C "
					+ "WHERE A.DIST_ORDER=B.DIST_ORDER "
					+ "AND B.ITEM_CODE = C.ITEM_CODE "
					+ "AND A.SITE_CODE__SHIP >= ? "
					+ "AND A.SITE_CODE__SHIP <= ? "
					+ "AND A.ORDER_DATE >= ? "
					+ "AND A.ORDER_DATE <= ? "
					+ "AND A.DUE_DATE >= ? "
					+ "AND A.DUE_DATE <= ? "
					+ "AND A.STATUS = 'P' "
					+ "AND A.PRICE_LIST = ? ";
					
				pstmt = conn.prepareStatement(sql);
	
				pstmt.setString(1,siteCodeFrom.trim());
				pstmt.setString(2,siteCodeTo.trim());
				pstmt.setTimestamp(3,orderDateFrom);
				pstmt.setTimestamp(4,orderDateTo);
				pstmt.setTimestamp(5,dueDateFrom);
				pstmt.setTimestamp(6,dueDateTo);
				pstmt.setString(7,priceList.trim()); 
				
				System.out.println("The GetDataSQL becomes :::-"+sql);	
				rs = pstmt.executeQuery();
	    	        	
	       		while (rs.next())
				{
					//DIST_ORDER
					retTabSepStrBuff.append((rs.getString(1)==null?"":rs.getString(1))).append("\t");
					//LINE_NO
					retTabSepStrBuff.append((rs.getString(2)==null?"":rs.getString(2))).append("\t");
					//ITEM_CODE 
					itemCode = rs.getString(3);
					retTabSepStrBuff.append(itemCode).append("\t");
					//ITEM_DESCR
					retTabSepStrBuff.append((rs.getString(4)==null?"":rs.getString(4))).append("\t");	
					//SITE_CODE__SHIP
					retTabSepStrBuff.append((rs.getString(5)==null?"":rs.getString(5))).append("\t");
					//SITE_CODE__DLV
					retTabSepStrBuff.append((rs.getString(6)==null?"":rs.getString(6))).append("\t");
					//RATE
					retTabSepStrBuff.append(rs.getDouble(7)).append("\t");
					
					listType = distCommon.getPriceListType(priceList,conn);
					System.out.println("The list type is :::- ["+listType+"]");
					newRate = distCommon.pickRate(priceList,tranDate,itemCode,"",listType,conn);
					System.out.println("The rate in the price list is (newRate) :::- ["+newRate+"]");
					//NEW_RATE
					retTabSepStrBuff.append(newRate).append("\n");
				}//while
			}//try
			catch (SQLException e)
			{
				System.out.println("SQLException ::RateUpdateDisOrdPrcEJB :" +sql+ e.getMessage() + ":");
				throw new ITMException(e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception []::RateUpdateDisOrdPrcEJB :"+ex.getMessage());
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
			System.out.println("SQLException ::RateUpdateDisOrdPrcEJB :" +sql+ e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch(Exception ex)
		{
			System.out.println("Exception []::RateUpdateDisOrdPrcEJB :"+ex.getMessage());
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
		System.out.println("xmlString2::::::::======>"+ xmlString2);
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
			System.out.println("Exception :RateUpdateDisOrdPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}//process()
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;	
	
	
		String returnString = "";
		String sql = null;
		String sql2 = null;
		String distOrder = null;
		String lineNo = null;
		String srate = null;
		String itemCode = null;
		String unitRate = null;
		String unitStd = null;
		String childNodeName = null;
		
		double rate = 0;
		double convRtuomStd = 0;
		double rateStduom = 0;
		
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		int lineNoInt=0;
		
		try
		{
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
			}
			
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
					
					if (childNodeName.equals("dist_order"))
					{
						if(childNode.getFirstChild()!=null)
						{
							distOrder = childNode.getFirstChild().getNodeValue().trim();
							System.out.println("distOrder:::::"+ distOrder);	
						}
					}
					if (childNodeName.equals("line_no"))
					{
						if(childNode.getFirstChild()!=null)
						{
							lineNo = (childNode.getFirstChild().getNodeValue()).trim();
							System.out.println("line_no:::::"+ lineNo);		
						}
					}
					if (childNodeName.equals("item_code"))
					{
						if(childNode.getFirstChild()!=null)
						{
							itemCode = (childNode.getFirstChild().getNodeValue()).trim();
							System.out.println("item_code:::::"+ itemCode);	
						}
						
					}
					if (childNodeName.equals("new_rate"))
					{
						if(childNode.getFirstChild()!=null)
						{
							srate = (childNode.getFirstChild().getNodeValue()).trim();
							System.out.println("item_code:::::"+ itemCode);	
						}
						else
						{
							srate="0";
						}	
						rate = Double.parseDouble(srate);
						System.out.println("rate :::::"+ rate);	
					} 
				}//inner for
				//lineNo = (("    "+lineNo).substring(("    "+lineNo).length()-3));
				 lineNoInt = Integer.parseInt(lineNo);
				
				try
				{
					int cnt = 0;
					sql = "UPDATE  DISTORDER_DET SET RATE = ? WHERE DIST_ORDER = '"+distOrder+"' AND LINE_NO = "+lineNoInt;
					System.out.println("Update sql ::-"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1,rate);
					System.out.println("SETTING RATE :::["+rate+"]");
					cnt = pstmt.executeUpdate();
					System.out.println("No of records updated in DISTORDER is : "+cnt);
				}
				catch (SQLException ex)
				{
					returnString="EXCEPTION";
					System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
					ex.printStackTrace();
					throw new ITMException(ex);
					
				}
				catch (Exception e)
				{
					returnString="EXCEPTION";
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
			returnString="EXCEPTION";
			System.out.println("Exception ::RateUpdatePrcEJB ::-process():" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			System.out.println("Closing Connection2....");
			try
			{
				if(returnString.equals("EXCEPTION"))
				{
					conn.rollback();
					System.out.println("TRANSACTION ROLLBACK@@@@@@@@@@@@@@@@@@");
				}
				else
				{
					conn.commit();
					System.out.println("TRANSACTION COMMIT@@@@@@@@@@@@@@@@@@");
					
				}
			}
			catch(Exception se){}
		}//
		return returnString;
	}//process()
	
	
	
	private String getApplicationDate(java.sql.Timestamp currDate) throws ITMException
	{
		String tranDate=null;
		try
		{
			System.out.println("Current Date in Dbdate Format:::["+currDate+"]");
		
			Object date = null;
			SimpleDateFormat dbDateFormat = null;
			SimpleDateFormat applicationDateFormat=null;
			dbDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = dbDateFormat.parse(currDate.toString());
			applicationDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());	
			tranDate = applicationDateFormat.format(date).toString();
			System.out.println("Current Date in ApplicationDate Format:::["+tranDate+"]");
			
		}
		catch(Exception ex)
		{
			System.out.println("Exception in Converting The  DB date to Application date:::::");
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
		}
		return tranDate;
	}
}//class 
	

	