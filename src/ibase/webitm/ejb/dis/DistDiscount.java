/*
	Version 1.0
	Converted for nvo_business_object_dist_discount

*/ 
package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.ConnDriver;

//import java.io.PrintStream;
//import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
import java.sql.*;
import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.Random;
import ibase.webitm.utility.ITMException;
import java.text.SimpleDateFormat;

import javax.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3


//public class DistDiscountEJB extends ValidatorEJB implements SessionBean 
@Stateless // added for ejb3
public class DistDiscount extends ValidatorEJB implements DistDiscountLocal, DistDiscountRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*SessionContext cSessionContext;

  	public void ejbCreate() throws RemoteException, CreateException 
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

	public void setSessionContext(SessionContext mSessionContext) 
	{
		this.cSessionContext = mSessionContext;
	}*/

	public Connection getConnection()throws RemoteException,ITMException
	{
		Connection conn = null;
		try
		{
			
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistDiscountEJB :getConnection:"+e.getMessage() + ":");
			throw new ITMException(e);
		}
		return conn;
	}

	/* Sisir 20/08/2005 - Transfer of code from gbf_price_list_discount of nvo_business_object_dist_discount */
  	public String priceListDiscount (String siteCode, String custCode, Connection connectionObject)throws RemoteException,ITMException
	{
		String pListDisc = "";
		String errCode="";
		String sql = "";
		Statement stmt;
		ResultSet rs = null;

 		boolean connectionState = false;

		try
		{
			if(connectionObject == null)
			{
				connectionObject = getConnection();
				connectionState = true;
			}
			stmt = connectionObject.createStatement();
			sql = "SELECT PRICE_LIST__DISC FROM SITE_CUSTOMER WHERE CUST_CODE='"+custCode+"' AND SITE_CODE='"+siteCode+"'";
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				pListDisc=rs.getString(1);
				if(pListDisc == null || pListDisc.trim().length() == 0)
				{
					sql = "SELECT PRICE_LIST__DISC FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						pListDisc = rs.getString(1);
						if (pListDisc == null)
						{
							pListDisc = "";
						}
					}												
				}
			}
			else
			{
				sql = "SELECT PRICE_LIST__DISC FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					pListDisc = rs.getString(1);
					if (pListDisc == null)
					{
						pListDisc = "";
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception: DistDiscountEJB: priceListDiscount:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (connectionState)
				{
					connectionObject.close();
					connectionObject = null;
				}
			}catch(Exception se){}
		}
		return pListDisc;
	}
	/* Sisir 22/08/2005 - Transfer of code from gbf_price_list_site of nvo_business_object_dist_discount */
 	public String priceListSite (String siteCode, String custCode, Connection connectionObject)throws RemoteException,ITMException
	{
		String priceList = "";
		String errCode= "";
		String sql = "";
		long mCnt;
		Statement stmt;
		ResultSet rs = null;

 		boolean connectionState = false;

		try
		{
			if(connectionObject == null)
			{
				connectionObject = getConnection();
				connectionState = true;
			}
			stmt = connectionObject.createStatement();
			sql = "SELECT PRICE_LIST FROM SITE_CUSTOMER WHERE CUST_CODE='"+custCode+"' AND SITE_CODE='"+siteCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				priceList=rs.getString(1);
				if(priceList == null || priceList.trim().length() == 0)
				{
					sql = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						priceList=rs.getString(1);
						if (priceList == null)
						{
							priceList = "";
						}
					}					
				}
			}
			else
			{
				sql = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					priceList=rs.getString(1);
					if (priceList == null)
					{
						priceList = "";
					}
				}	
			}
			rs.close();
			stmt.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistDiscountEJB :priceListSite:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (connectionState)
				{
					connectionObject.close();
					connectionObject = null;
				}
			}catch(Exception se){}
		}
		return priceList;
	}

	/* Sisir 23/08/2005 - Transfer of code from gbf_get_discount of nvo_business_object_dist_discount */
	public double getDiscount(String plistDisc, java.util.Date orderDate, String custCode, String siteCode, String itemCode, String unit, double discMerge, java.util.Date plistDate, double qty, Connection connectionObject) throws RemoteException,ITMException
	{

		System.out.println("Inside getDiscount........");
		double disc = 0;
		double rate = 0;

		String itemSer = "";
		String listType = "";

		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		ResultSet rs5 = null;
		Statement stmt;
		PreparedStatement pstmt = null;
		String sql = "";

 		boolean connectionState = false;

		try
		{
			if(connectionObject == null)
			{
				connectionObject = getConnection();
				connectionState = true;
			}
			stmt = connectionObject.createStatement();
			if (plistDisc.trim().length() > 0)
			{
				sql = "SELECT CASE WHEN RATE IS NULL THEN 0 ELSE RATE END FROM PRICELIST WHERE PRICE_LIST = ? "+
						"AND ITEM_CODE= ? AND UNIT= ? AND LIST_TYPE IN ('M','N') AND CASE WHEN MIN_QTY IS NULL THEN 0 "+
						"ELSE MIN_QTY END <= ? AND ((CASE WHEN MAX_QTY IS NULL THEN 0 ELSE MAX_QTY END  >= ? ) "+
							"OR (CASE WHEN MAX_QTY IS NULL THEN 0 ELSE MAX_QTY END	= 0))"+	 
							"AND EFF_FROM <= ? AND VALID_UPTO >= ? ";
				pstmt = connectionObject.prepareStatement(sql);
				pstmt.setString(1,plistDisc);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,unit);
				pstmt.setDouble(4,qty);
				pstmt.setDouble(5,qty);
				pstmt.setTimestamp(6,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(new SimpleDateFormat("dd/MM/yy").format(plistDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
				pstmt.setTimestamp(7,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(new SimpleDateFormat("dd/MM/yy").format(plistDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					rate = rs1.getDouble(1);
					disc = rate;
				}
				else
				{
					return 0;
				}
			}

			if ((listType.equalsIgnoreCase("M")) || (plistDisc.length() == 0) || (plistDisc.trim().length() == 0) || (rate == 0))
			{
 				sql="SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				rs2 = stmt.executeQuery(sql);
				if(!rs2.next())
				{
					disc = 0;
				}
				else
				{
					itemSer=rs2.getString(1);
				}
				// find in customer_series
				sql="SELECT DISC_PERC FROM CUSTOMER_SERIES WHERE CUST_CODE='"+custCode+"' AND ITEM_SER='"+itemSer+"'";
				rs3 = stmt.executeQuery(sql);
				if(rs3.next())
				{		
					disc = rs3.getDouble(1);
				}
				else
				{
					// find in site_customer
					sql = "SELECT DISC_PERC FROM SITE_CUSTOMER WHERE SITE_CODE = '"+siteCode+"' AND CUST_CODE='"+custCode+"'";
					rs4 = stmt.executeQuery(sql);
					if (rs4.next())
					{
						//disc = 0;
						disc=rs4.getDouble(1);
					}
					else
					{
						//disc=rs4.getDouble(1);
						
						System.out.println("disc SITE_CUSTOMER......"+disc);
						
						// find in customer
						sql = "SELECT DISC_PERC FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
						rs5 = stmt.executeQuery(sql);
						if (!rs5.next())
						{
							disc = 0;
						}
						else
						{
							disc=rs5.getDouble(1);
						}
						
					}
				
				}
				/*System.out.println("disc. CUSTOMER_SERIES....."+disc + "rs3.next()"+rs3.next());
				if (rs3.next() == false || (disc == 0))
				{
					// find in site_customer
					sql = "SELECT DISC_PERC FROM SITE_CUSTOMER WHERE SITE_CODE = '"+siteCode+"' AND CUST_CODE='"+custCode+"'";
					rs4 = stmt.executeQuery(sql);
					if (!rs4.next())
					{
						disc = 0;
					}
					else
					{
						disc=rs4.getDouble(1);
						
						System.out.println("disc SITE_CUSTOMER......"+disc);
						// find in customer
						sql = "SELECT DISC_PERC FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
						rs5 = stmt.executeQuery(sql);
						if (!rs5.next())
						{
							disc = 0;
						}
						else
						{
							disc=rs5.getDouble(1);
						}
					}
				}
				else
				{
					disc=rs3.getDouble(1);
				}*/
				if (listType.equalsIgnoreCase("N"))	
				{
					// merge with rate
					discMerge = disc;
					if (rate != 0)
					{
						disc = rate;
					}
				}
				else
				{
					// not to be merged, set in the discount field
					discMerge = 0;
				}
			}
			if (itemCode.trim().length() == 0)
			{
				disc = 0;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistDiscountEJB :getDiscount:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (connectionState)
				{
					connectionObject.close();
					connectionObject = null;
				}
			}catch(Exception se){}
		}
		return disc;
	}

	/* Sisir 24/08/2005 - Transfer of code from gbf_pick_rate (with 5 arguments) of nvo_business_object_dist */
	public double pickRate(String priceList, java.util.Date tranDate, String itemCode, String lotNo, String type, Connection connectionObject) throws RemoteException,ITMException
	{
		double rate = 0;
		String siteCode = "";
		String locCode = "";
		String lotSl = "";

		String errCode = "";
		Statement stmt;
		ResultSet rs = null;
		String sql = "";

		boolean connectionState = false;
		try
		{
			if(connectionObject == null)
			{
				connectionObject = getConnection();
				connectionState = true;
			}
			stmt = connectionObject.createStatement();
			sql = "SELECT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"'";
			rs = stmt.executeQuery(sql);
			if (!rs.next())
			{
				return -1;
			}
			else
			{
				type = rs.getString(1);
			}
			if (type.equalsIgnoreCase("L"))	// List Price
			{
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = ''"+itemCode+"' AND LIST_TYPE = 'L' AND EFF_FROM <= '"+tranDate+"' AND VALID_UPTO >= '"+tranDate+"'";
				rs = stmt.executeQuery(sql);
				if (!rs.next())
				{
					return -1;	 // Denotes Error
				}
				else
				{
					rate = rs.getDouble(1);
				}
			}
			else if (type.equalsIgnoreCase("D"))	// Despatch
			/* Selecting rate from pricelist  for L, if not found picking up from batch */
			{
				try
				{
					sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'L'	AND EFF_FROM <= '"+tranDate+"' AND VALID_UPTO >= '"+tranDate+"'";
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						rate = 0;
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'B' AND LOT_NO__FROM <= '"+lotNo+"' AND LOT_NO__TO >= '"+lotNo+"' AND EFF_FROM <= '"+tranDate+"' AND VALID_UPTO >= '"+tranDate+"'";
						rs = stmt.executeQuery(sql);
						if (!rs.next())
						{
							return -1;	 // Denotes Error
						}
						else
						{
							rate = rs.getDouble(1);
						}
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}
				catch(SQLException ie)
				{
					System.out.println("Exception: DistDiscountEJB: pickRate: type D: ==>"+ie);
					ie.printStackTrace();
					return -1;
				}
			}
			else if (type.equalsIgnoreCase("B"))	// Batch Price
			{
				rate = 0;
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'B' AND LOT_NO__FROM <= '"+lotNo+"' AND LOT_NO__TO >= '"+lotNo+"' AND EFF_FROM <= '"+tranDate+"' AND VALID_UPTO >= '"+tranDate+"'";
				rs = stmt.executeQuery(sql);
				if (!rs.next())
				{
					return -1;	 // Denotes Error
				}
				else
				{
					rate = rs.getDouble(1);
				}
			}
			else if (type.equalsIgnoreCase("M") || type.equalsIgnoreCase("N"))	// Discount Price
			{
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = '"+type+"' AND EFF_FROM <= '"+tranDate+"' AND VALID_UPTO >= '"+tranDate+"'";
				rs = stmt.executeQuery(sql);
				if (!rs.next())
				{
					return -1;	 // Denotes Error
				}
				else
				{
					rate = rs.getDouble(1);
				}
			}
			else if (type.equalsIgnoreCase("I"))	// Inventory
			{
				/*	------------------Code not transferred, it's calling an external function
				siteCode = 
				locCode = 
				lotNo = 
				lotSl = 
				-------------------*/
				rate = 0;
				//	To check is lot no is null or not and fetch accordingly
				if (lotSl.trim().length() == 0 || lotSl.length() == 0)
				{
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = '"+itemCode+"' AND SITE_CODE = '"+siteCode+"' AND LOC_CODE = '"+locCode+"' AND LOT_NO = '"+lotNo+"'";
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						return -1;
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}
				else
				{
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = '"+itemCode+"' AND SITE_CODE = '"+siteCode+"' AND LOC_CODE = '"+locCode+"' AND LOT_NO = '"+lotNo+"' AND LOT_SL '"+lotSl+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						rate = rs.getDouble(1);
					}
					else
					{
						return -1;
					}
				}
			}
			if (rs != null) 
			{
				rs.close();
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistDiscountEJB :pickRate:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (connectionState)
				{
					connectionObject.close();
					connectionObject = null;
				}				
			}catch(Exception se){}
		}
		return rate;
    }

	/* Sisir 25/08/2005 - Transfer of code from gbf_pick_rate (with 6 arguments) of nvo_business_object_dist */
	public double pickRate(String priceList, java.util.Date tranDate, String itemCode, String lotNo, String type, double qty, Connection connectionObject ) throws RemoteException,ITMException
	{

		double rate = 0;
		String siteCode = "";
		String locCode = "";
		String priceListParent = "";
		String lotSl = "";

		String errCode = "";
		Statement stmt;
		PreparedStatement pstmt;
		ResultSet rs = null;
		String sql = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		

		boolean connectionState = false;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());	
			if(connectionObject == null)
			{
				connectionObject = getConnection();
				connectionState = true;
			}
			stmt = connectionObject.createStatement();
			sql = "SELECT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"'";
			System.out.println("SQL :: "+sql);
			rs = stmt.executeQuery(sql);
			if (!rs.next())
			{
				return -1;
			}
			else
			{
				type = rs.getString(1); 
			}
			if (type.equalsIgnoreCase("L"))	// List Price
			{
				try
				{
					sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'L' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
					pstmt = connectionObject.prepareStatement(sql);
					pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					System.out.println("SQL :: "+sql);
					rs = pstmt.executeQuery();
					if (!rs.next())
					{
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END)FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND LIST_TYPE = 'L'";
						System.out.println("SQL :: "+sql);
						rs = stmt.executeQuery(sql);
						if (!rs.next())
						{
							return -1;
						}
						else
						{
							priceListParent = rs.getString(1);
						}
						if (priceListParent.trim().length() == 0)
						{
							return -1;
						}
						if (priceListParent.trim().length() >= 0)
						{
							sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceListParent+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'L' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
							System.out.println("SQL :: "+sql);
							pstmt = connectionObject.prepareStatement(sql);
							pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							rs = pstmt.executeQuery();
						}
						else
						{
							rate = rs.getDouble(1);
						}						
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}	
				catch(SQLException ie)
				{
					System.out.println("Exception: DistDiscountEJB: pickRate: type L: ==>"+ie);
					ie.printStackTrace();
					return -1;
				}
			}
			else if (type.equalsIgnoreCase("D"))	// Despatch
			/* Selecting rate from pricelist  for L, if not found picking up from batch */
			{
				try
				{
					sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'L'	AND EFF_FROM <= ? AND VALID_UPTO >= ? MIN_QTY <= "+qty+" MAX_QTY >= "+qty+"";
					pstmt = connectionObject.prepareStatement(sql);
					pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					rs = pstmt.executeQuery();
					if (!rs.next())
					{
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END ) FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND LIST_TYPE = 'L'";
						if (!rs.next())
						{
							return -1;	 // Denotes Error
						}
						else
						{
							priceListParent = rs.getString(1);
						}
						if (priceListParent.trim().length() > 0)
						{
							sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceListParent+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'L' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY = "+qty+"";
							pstmt = connectionObject.prepareStatement(sql);
							pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							rs = pstmt.executeQuery();
							if (!rs.next())
							{
								return -1;
							}
							else
							{
								rate = rs.getDouble(1);
							}
						}
						else
						{
							rate = 0;
							sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'B' AND LOT_NO__FORM <= '"+lotNo+"' AND LOT_NO__TO >= '"+lotNo+"' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
							pstmt = connectionObject.prepareStatement(sql);
							pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							rs = pstmt.executeQuery();
							if (!rs.next())
							{
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END) FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND LIST_TYPE = 'B'";
								rs = stmt.executeQuery(sql);
								if (!rs.next())
								{
									return -1;
								}
								else
								{
									priceListParent = rs.getString(1);
								}
								if (priceListParent.trim().length() == 0)
								{
									return -1;
								}
								if (priceListParent.trim().length() > 0)
								{
									sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceListParent+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'B' AND LOT_NO__FORM <= '"+lotNo+"' AND LOT_NO__TO >= '"+lotNo+"' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
									pstmt = connectionObject.prepareStatement(sql);
									pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
									pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
									rs = pstmt.executeQuery();
									if (!rs.next())
									{
										return -1;
									}
									else
									{
										rate = rs.getDouble(1);
									}
								}
							}
							else
							{
								rate = rs.getDouble(1);
							}
						}
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}
				catch(SQLException ie)
				{
					System.out.println("Exception: DistDiscountEJB: pickRate: type D: ==>"+ie);
					ie.printStackTrace();
					return -1;
				}
			}
			else if (type.equalsIgnoreCase("B"))	// Batch Price
			{
				try
				{
					rate = 0;
					sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'B' AND LOT_NO__FROM <= '"+lotNo+"' AND LOT_NO__TO >= '"+lotNo+"' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
					pstmt = connectionObject.prepareStatement(sql);
					pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					rs = pstmt.executeQuery();
					if (!rs.next())
					{
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END) FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND LIST_TYPE = 'B'";
						rs = stmt.executeQuery(sql);
						if (!rs.next())
						{
							return -1;
						}
						else
						{
							priceListParent = rs.getString(1);
						}
						if (priceListParent.trim().length() == 0)
						{
							return -1;
						}
						if (priceListParent.trim().length() > 0)
						{
							sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceListParent+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = 'B' AND LOT_NO__FORM <= '"+lotNo+"' AND LOT_NO__TO >= '"+lotNo+"' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
					   		pstmt = connectionObject.prepareStatement(sql);
							pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),getDBDateFormat())));
							pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),getDBDateFormat())));
							rs = pstmt.executeQuery();
							if (!rs.next())
							{
								return -1;
							}
							else
							{
								rate = rs.getDouble(1);
							}
						}
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}
				catch(SQLException ie)
				{
					System.out.println("Exception: DistDiscountEJB: pickRate: type D: ==>"+ie);
					ie.printStackTrace();
					return -1;
				}
			}
			else if (type.equalsIgnoreCase("M") || type.equalsIgnoreCase("N"))	// Discount Price
			{
				try
				{
					sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = '"+type+"' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
					pstmt = connectionObject.prepareStatement(sql);
					pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					rs = pstmt.executeQuery();
					if (!rs.next())
					{
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END) FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"' AND LIST_TYPE = '"+type+"'";
						rs = stmt.executeQuery(sql);
						if (!rs.next())
						{
							return -1;
						}
						else
						{
							priceListParent = rs.getString(1);
						}
						if (priceListParent.trim().length() == 0)
						{
							return -1;
						}
						if (priceListParent.trim().length() > 0)
						{
							sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = '"+priceListParent+"' AND ITEM_CODE = '"+itemCode+"' AND LIST_TYPE = '"+type+"' AND EFF_FROM <= ? AND VALID_UPTO >= ? AND MIN_QTY <= "+qty+" AND MAX_QTY >= "+qty+"";
							pstmt = connectionObject.prepareStatement(sql);
							pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(sdf.format(tranDate),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
							rs = pstmt.executeQuery();
							if (!rs.next())
							{
								return -1;
							}
							else
							{
								rate = rs.getDouble(1);
							}
						}
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}
				catch(SQLException ie)
				{
					System.out.println("Exception: DistDiscountEJB: pickRate: type D: ==>"+ie);
					ie.printStackTrace();
					return -1;
				}
			}
			else if (type.equalsIgnoreCase("I"))	// Inventory
			{
				/* ---------------Code not transferred, it's calling an external function
				siteCode = 
				locCode = 
				lotNo = 
				lotSl = 
				-------------------*/
				rate = 0;
				//	To check is lot no is null or not and fetch accordingly
				if (lotSl.trim().length() == 0 || lotSl.length() == 0)
				{
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = '"+itemCode+"' AND SITE_CODE = '"+siteCode+"' AND LOC_CODE = '"+locCode+"' AND LOT_NO = '"+lotNo+"'";
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						return -1;
					}
					else
					{
						rate = rs.getDouble(1);
					}
				}
				else
				{
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = '"+itemCode+"' AND SITE_CODE = '"+siteCode+"' AND LOC_CODE = '"+locCode+"' AND LOT_NO = '"+lotNo+"' AND LOT_SL '"+lotSl+"'";
					if (rs.next())
					{
						rate = rs.getDouble(1);
					}
					else
					{
						return -1;
					}
				}
			}
			if (rs != null) 
			{
				rs.close();
			}			
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistDiscountEJB :pickRate:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (connectionState)
				{
					connectionObject.close();
					connectionObject = null;
				}
			}catch(Exception se){}
		}
		return rate;
    }
}