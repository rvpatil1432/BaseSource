/********************************************************
	Title : CustItemUpdatePrc[D14LSUN003]
	Date  : 11/03/15
	Developer: Priyanka Shinde

 ********************************************************/
package ibase.webitm.ejb.dis.adv;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.rmi.RemoteException;
import java.sql.*;

import org.w3c.dom.*;
import java.text.SimpleDateFormat; 
import java.util.Calendar;
import java.util.HashMap;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
             
public class CustItemUpdatePrc extends ProcessEJB implements CustItemUpdatePrcLocal,CustItemUpdatePrcRemote
{	
String loginSiteCode = null;
//GenericUtility genericUtility = GenericUtility.getInstance();
E12GenericUtility genericUtility= new  E12GenericUtility();
String currDateTs = null;
String chgUser = "";
String chgTerm = ""; 
ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
	throws RemoteException,ITMException
{
	Document detailDom = null;
	Document headerDom = null;
	String retStr = "";	
	System.out.println("Process method called@@@@@@@@@@@@@......");	
	try
	{	
		if(xmlString != null && xmlString.trim().length()!=0)
		{
			headerDom = genericUtility.parseString(xmlString); 
			System.out.println("headerDom" + headerDom);
		}
		if(xmlString2 != null && xmlString2.trim().length()!=0)
		{
			detailDom = genericUtility.parseString(xmlString2); 
			System.out.println("detailDom" + detailDom);
		}		 
		retStr = process(headerDom, detailDom, windowName, xtraParams);
	}
	catch (Exception e)
	{			
		System.out.println("Exception :CustItemUpdatePrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
		e.printStackTrace();
		throw new ITMException(e);
	}
	return retStr;
}//END OF PROCESS (1)

public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
{
	System.out.println("Process method called$$$$$$$$$$$..");	
	Connection conn = null;	
	String resultString = "", errString = "";
	boolean isError = false;
	PreparedStatement pstmt = null;	
	PreparedStatement pstmt1 = null;	
	PreparedStatement pstmt2 = null;	
	ResultSet rs = null;
	ResultSet rs1 = null;
	ResultSet rs2 = null;
	String sql = "",sql1="";	
	int updCnt=0;		
	//GenericUtility genericUtility = GenericUtility.getInstance();
	String custCodeFrom="",custCodeTo="",preCustCode="";
	String siteCodeFr="",siteCodeTo="";
	String itemCodeFrom="",itemCodeTo="";
	String integralQty="";
	double intQty=0,minQty=0,intQtyExst=0,temp=0;
	String  chguser = null;
	String  chgterm = null;
	String custCode="",itemCode="",preItemCode="",custCode1="",siteCode="";
	String sysDate ="";
	Timestamp sysDate1 = null;
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();	
	String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
	loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
	//chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
	//chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");	
	HashMap<String,Double>minQtyMap =  new HashMap<String,Double>();
	try
	{
		ConnDriver connDriver = new ConnDriver();
		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		connDriver = null;
		conn.setAutoCommit(false);
		java.util.Date today=new java.util.Date();
		Calendar c = Calendar.getInstance();
		today = c.getTime();
		SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
		System.out.println("SDF==========="+sdf);
		sysDate=sdf.format(today);
		System.out.println("sysDate****=========="+sysDate);
		
		sysDate1= Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
		//long time=sysDate1.getTime();
		
		java.util.Date date= new java.util.Date();
		Timestamp ts_now = new Timestamp(date.getTime());
		System.out.println("ts_now*********"+ts_now);	
		
		
		siteCodeFr = checkNull(genericUtility.getColumnValue("site_code__fr",headerDom ));	
		siteCodeTo= checkNull(genericUtility.getColumnValue("site_code__to",headerDom ));
		itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code__from",headerDom ));
		itemCodeTo = checkNull(genericUtility.getColumnValue("item_code__to",headerDom ));
		integralQty   = checkNull(genericUtility.getColumnValue("integral_qty",headerDom ));
		
		chguser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		chgterm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
		
		System.out.println("Change User================"+chguser);
		System.out.println("Change Term================"+chgterm);
		System.out.println("Change date================"+sysDate);
		System.out.println("Change date=TimeStamp==============="+sysDate1);
		
		
		if(integralQty!=null && integralQty.trim().length()>0)
		{
			//changed by Varsha V on 29-06-19 to resolve number format exception
			//intQty=Double.parseDouble(integralQty);
			intQty=Double.parseDouble(checkInteger(integralQty));
		}
		
		
		System.out.println("siteCodeFr========="+siteCodeFr);
		System.out.println("siteCodeTo========="+siteCodeTo);
		System.out.println("itemCodeFrom========="+itemCodeFrom);
		System.out.println("itemCodeTo========="+itemCodeTo);
		System.out.println("integralQty========="+integralQty);
		System.out.println("intQty========="+intQty);
		
		
		sql="select cust_code,site_code from site_customer where site_code >= ? and site_code <= ? order by site_code ";
		pstmt=conn.prepareStatement(sql);
		pstmt.setString(1,siteCodeFr);
		pstmt.setString(2,siteCodeTo);		
		rs=pstmt.executeQuery();
		while(rs.next())
		{
			custCode1=rs.getString("cust_code");
			siteCode=rs.getString("site_code");
		    System.out.println("Getting cust code from site_customer====="+custCode1);
		    System.out.println("Getting site Code from site_customer====="+siteCode);
    	
    
		 sql="select  item_code, (case when integral_qty is null then 0 else integral_qty end) as integral_qty,cust_code from customeritem where item_code >= ? and item_code <= ? and cust_code= ? order by item_code";
		pstmt1=conn.prepareStatement(sql);
		pstmt1.setString(1,itemCodeFrom);
		pstmt1.setString(2,itemCodeTo);
		pstmt1.setString(3,custCode1);
		//pstmt1.setString(4,custCodeTo);
		rs1=pstmt1.executeQuery();
		while(rs1.next())
		{
			intQtyExst=rs1.getDouble("integral_qty");
			custCode=rs1.getString("cust_code");
			itemCode=rs1.getString("item_code");
			
			
			System.out.println("custCode11========"+custCode);
			System.out.println("itemCode========"+itemCode);
			System.out.println("Intergral Qty existing========"+intQtyExst);
			
			if(minQtyMap.size()!=0)
			{
				System.out.println("$$$$$$$Map is not empty");		
				System.out.println("minQtyMap============="+minQtyMap);
				if(!minQtyMap.containsKey(itemCode))
				{
					System.out.println("!minQtyMap.containsValue(itemCode)%%%%%%%%%%%%%%");
				//sql="select min(case when integral_qty is null then 0 else integral_qty end) as min_qty from customeritem where item_code = ?";
				sql="select min(case when integral_qty is null then 0 else integral_qty end) as min_qty " +
						"from customeritem " +
						"where item_code = ? " +
						"and cust_code in (select cust_code from site_customer  where site_code = ?) ";
				pstmt2=conn.prepareStatement(sql);
				pstmt2.setString(1,itemCode);		
				pstmt2.setString(2,siteCode);
				rs2=pstmt2.executeQuery();
				if(rs2.next())
				{
					minQty=rs2.getDouble("min_qty");	
					
				}
				pstmt2.close();
		    	rs2.close();
		    	pstmt2 = null;
		    	rs2 = null;
		    	System.out.println("Getting mininum existing integral quantity for enter ItemCode======"+minQty);
		    	
		    	minQtyMap.put(itemCode, minQty);
				}
				
			}
			else
			{
				System.out.println("Map is  empty *************");
				sql="select min(case when integral_qty is null then 0 else integral_qty end) as min_qty " +
						"from customeritem " +
						"where item_code = ? " +
						"and cust_code in (select cust_code from site_customer  where site_code = ?) ";
				pstmt2=conn.prepareStatement(sql);
				pstmt2.setString(1,itemCode);		
				pstmt2.setString(2,siteCode);
				rs2=pstmt2.executeQuery();
				if(rs2.next())
				{
					minQty=rs2.getDouble("min_qty");		
					
				}
				pstmt2.close();
		    	rs2.close();
		    	pstmt2 = null;
		    	rs2 = null;
		    	System.out.println("Getting mininum existing integral quantity for enter ItemCode======"+minQty);
		    	minQtyMap.put(itemCode, minQty);
			}
			
			System.out.println("minQtyMap== check==========="+minQtyMap);
			minQty=	minQtyMap.get(itemCode);
	         System.out.println("minQty==from map== ============"+minQty);
	         
			
	    	System.out.println("Getting minimum quantity through map:===="+minQty);
			if(intQtyExst <= minQty)	    	 
			{
				System.out.println("intQtyExst before if======"+intQtyExst);
				System.out.println("minQty======"+minQty);
				intQtyExst=intQty;
				System.out.println("intQtyExst after  ======"+intQtyExst);
			}
			else if(intQtyExst>minQty)
			{
				
				System.out.println("else========");
				System.out.println("intQtyExst before======"+intQtyExst);
				System.out.println("minQty======"+minQty);
				intQtyExst=(intQtyExst/intQty) ;
				System.out.println("intQtyExst after11111111======"+intQtyExst);
				intQtyExst=Math.ceil(intQtyExst)*intQty;
				System.out.println("intQtyExst after1 ceil======"+intQtyExst);
			}
			
			System.out.println("Getting new integral Quantity====="+intQtyExst);
			
			sql1="update customeritem set integral_qty= ?,CHG_DATE= ?,CHG_USER= ?, CHG_TERM= ? where  item_code= ? and cust_code= ?  ";
			System.out.println("sql :" + sql1);
			pstmt2 = conn.prepareStatement(sql1);
			pstmt2.setDouble(1,intQtyExst);
			pstmt2.setTimestamp(2,ts_now);
			pstmt2.setString(3,chguser);			
			pstmt2.setString(4,chgterm);
			pstmt2.setString(5,itemCode);
			pstmt2.setString(6,custCode);
			
			updCnt = pstmt2.executeUpdate();
			System.out.println(updCnt + " Records Updated");
			if(updCnt>0)
			{
				System.out.println("Process Sucessful");				
			}
			
			pstmt2.close();
			pstmt2 = null;

			
		}
		pstmt1.close();
    	rs1.close();
    	pstmt1 = null;
    	rs1 = null;
		
		}
		pstmt.close();
    	rs.close();
    	pstmt = null;
    	rs = null;
	} // end of try code 
   	catch(Exception e)
	{
		try {
			conn.rollback();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		isError = true;
		e.printStackTrace();
		errString = e.getMessage();
		throw  new ITMException(e);
	}		
	finally
	{
		try
		{
			if(rs != null)
			rs.close();
			rs = null;
			if(pstmt != null)
			pstmt.close();
			pstmt = null;				
			if(conn != null)
			{
				if(isError)
				{
					//conn.rollback();
					//System.out.println("connection rollback.............");
					resultString = itmDBAccessEJB.getErrorString("","PROCFAILED",userId,"",conn);
				}	
				else
				{
					
						conn.commit();
						System.out.println("commiting connection.............");
						resultString = itmDBAccessEJB.getErrorString("","PROCSUCC1",userId,"",conn);
					
				}
				if(conn!=null)
				{
					conn.close();
					conn = null;
				}
			}
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
	}	
	System.out.println("returning from     "+resultString);
	return resultString;
	} //end process

	private String checkNull(String input)
	{
		if(input == null)
		{
			input = "";
		}
		return input.trim();
	}
	//Method Added by Varsha V on 29-06-19
	private String checkInteger(String input)
	{
		return (input == null || input.trim().length() ==0)? "0" : input;
	}
		



}

 