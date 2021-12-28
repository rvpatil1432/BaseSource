package ibase.webitm.ejb.dis.adv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class SorderClosePrc extends ProcessEJB 
{	
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	CommonConstants commonConstants = new CommonConstants();
	Connection conn = null;
	long startTime = 0;
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
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
			rtrStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :SorderClosePrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return rtrStr; 
	}		
	
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		String preview = "";
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1=null;
		double demand = 0.0,currentDemand= 0;
		double supply = 0.0,currentSupply= 0;	
		int currentRec = 0,cnt=0;
		String chgUser = "";
		String chgTerm = "";
		String itemCode = "";
		String saleOrder = "";
		String custCodeTo = "";
		String custCodeFrom = "",custCode="";
		String siteCode = "";
	
		String adpQuery = "";
		String orderType = "";
		String siteCodeTo = "";
		String sorderFrom= "";
		String sorderTo = "";
		String sDateFrom = "";
		String sDateTo = "";					
		String itemSerFrom = "";
		String itemSerTo  = "";		
		Timestamp dbDateFrom = null;
		Timestamp dbDateTo = null;		
		String dateFlag = "";
		Timestamp orderDate = null;
		Timestamp chgDate = null;
		ArrayList arrStdOrdType = null;
		HashMap demandSupplyMap = null;
		HashMap<String, String> externalSqlMap = null;
		String sqlFileName = "",sql="";
		try
		{	
			if(conn==null)
			{				
				conn = getConnection();
			}
			genericUtility= new  E12GenericUtility();
			chgDate = new Timestamp(System.currentTimeMillis());
			SorderCancel sorderCancel = new SorderCancel();				

			siteCode= genericUtility.getColumnValue("as_site_code", headerDom);
			
			if (siteCode == null || siteCode.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("", "VTSITCODE", "", "", conn);
				return errString;					
			}						
			orderType = genericUtility.getColumnValue("order_type", headerDom);
			
			if (orderType == null || orderType.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("", "VMORTYBK", "", "", conn);
				return errString;
			}			
			sorderFrom = genericUtility.getColumnValue("as_sorder_from", headerDom);
			
			sorderTo = genericUtility.getColumnValue("as_sorder_to", headerDom);
			
			
			dateFlag = genericUtility.getColumnValue("as_date_flag", headerDom);
			
			
			sDateFrom = genericUtility.getColumnValue("adt_date_from", headerDom);
			if(sDateFrom != null && sDateFrom.trim().length() > 0)
			{
				dbDateFrom=Timestamp.valueOf(genericUtility.getValidDateString(sDateFrom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			sDateTo = genericUtility.getColumnValue("adt_date_to", headerDom);
			if(sDateTo != null && sDateTo.trim().length() > 0)
			{
				dbDateTo=Timestamp.valueOf(genericUtility.getValidDateString(sDateTo, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			itemSerFrom = genericUtility.getColumnValue("as_itemser_from", headerDom);
			
			if (itemSerFrom == null || itemSerFrom.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("", "VMITMSER1 ", "", "", conn);
				return errString;
			}
			itemSerTo = genericUtility.getColumnValue("as_itemser_to", headerDom);
			
			if (itemSerTo == null || itemSerTo.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("", "VMITMSER1 ", "", "", conn);
				return errString;
			}
					
			custCodeFrom = genericUtility.getColumnValue("as_cust_code_from", headerDom);
			
			custCodeTo = genericUtility.getColumnValue("as_cust_code_to", headerDom);
					
			System.out.println("Db Date from :" + dbDateFrom);
			System.out.println("Db Date to   :" + dbDateTo);

			sql="select sale_order from sorder	where site_code 	=  ? "	+   
				" 		 and    order_type 	=	? " + 
				"		 and	item_ser 	>= ? " + 
				"		 and 	item_ser 	<= ? " + 
				"		 and 	sale_order	>= ? " + 
				"		 and 	sale_order 	<= ? " + 
				"		 and    order_date	>= (case when ? = 'Y' then ? else order_date end) " + 
				"		 and 	order_date	<= (case when ? = 'Y' then ? else order_date end)" + 
				"		 and    (due_date		>= (case when ? = 'N' then ? else due_date end) or (due_date is null)) " + 
				"		 and 	(due_date		<= (case when ? = 'N' then ? else due_date end) or (due_date is null))	 " + 
				"		 and 	cust_code	>= ? " + 
				"		 and 	cust_code	<= ? " + 
				"		 and 	status		 = 'P'" + 
				"		 and    confirmed     = 'Y'";
							
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, orderType);
			pstmt.setString(3, itemSerFrom);
			pstmt.setString(4, itemSerTo);
			pstmt.setString(5, sorderFrom);
			pstmt.setString(6, sorderTo);
			pstmt.setString(7, dateFlag);
			pstmt.setTimestamp(8, dbDateFrom);
			pstmt.setString(9, dateFlag);
			pstmt.setTimestamp(10, dbDateTo);
			pstmt.setString(11, dateFlag);
			pstmt.setTimestamp(12, dbDateFrom);
			pstmt.setString(13, dateFlag);
			pstmt.setTimestamp(14, dbDateTo);
			pstmt.setString(15, custCodeFrom);
			pstmt.setString(16, custCodeTo);
			
			
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				
				currentRec ++;
				System.out.println("Total Reocrd ::[] Current ["+currentRec+"]");
				saleOrder = checkNull(rs.getString("sale_order"));
									
				errString=sorderCancel.confirm(saleOrder,saleOrder,  xtraParams, "", conn);
				if(errString != null && errString.trim().length() > 0 )
				{
					conn.rollback();
					System.out.println("conn.rollback():["+saleOrder+"]");
				}
				else
				{
					conn.commit();
				}
			   
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(currentRec == 0)
			{
				errString = itmDBAccessEJB.getErrorString("", "VTSORDER1", "", "", conn);
				return errString;
			}
				
			if(errString == null || errString.trim().length() == 0 )
			{
				errString = itmDBAccessEJB.getErrorString("", "VTCONF ", "", "", conn);
				return errString;
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception :SorderClosePrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{		
				conn.close();
				conn = null;	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();

			}
		}

						
		return errString; 
		
	}
	
	// for check null
	private String checkNullTrim( String input )
	{
		return E12GenericUtility.checkNull(input);
	}
	private String checkNull( String input )
	{
		if( input == null || "null".equals(input) )
		{
			input = "";
		}
		else
		{
			input = input.trim();
		}
		return input;
	}
	

}
	

