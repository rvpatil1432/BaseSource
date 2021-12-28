/********************************************************
	Title : SiteItemUpdatePrc[D14ISUN010]
	Date  : 09/03/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
//import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.*;


//import java.text.SimpleDateFormat; 
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SiteItemUpdatePrc extends ProcessEJB implements SiteItemUpdatePrcLocal,SiteItemUpdatePrcRemote
{	
String loginSiteCode = null;
//GenericUtility genericUtility = GenericUtility.getInstance();
E12GenericUtility genericUtility= new  E12GenericUtility();
String currDateTs = null;
//String chgUser = "";
//String chgTerm = ""; 
ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
	throws RemoteException,ITMException
{
	Document detailDom = null;
	Document headerDom = null;
	String retStr = "";
	System.out.println("Process method called......");
	
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
		System.out.println("Exception :SiteItemUpdatePrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
		e.printStackTrace();
		throw new ITMException(e);
	}
	return retStr;
}//END OF PROCESS (1)

public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
{
	Connection conn = null;	
	String resultString = "", errString = "";
	boolean isError = false;
	PreparedStatement pstmt = null;	
	PreparedStatement pstmt1 = null;	
	ResultSet rs = null;
	ResultSet rs1 = null;
	String sql1="";	
	String siteCode = "";	
	String finEntity = "";
	String chgTermm1="";
	String siteCodeFr="", siteCodeTo ="", itemCodeFr="",itemCodeTo = "",itemCode = "";
	String siteCodeSupp = "", suppSour = "" ,itemSer = "",reoQty = "", integralQty = "";
	int updCnt=0;
	String sysDate ="";
	String  chguser = null;
	String  chgterm = null,chgTerm1=null;
	java.sql.Timestamp todays = null;
	java.sql.Timestamp todays1 = null;
	Timestamp sysDate1 = null,sysDate2=null;
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();	
	String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
	loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
	//chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");

	
	//chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"CHG_TERM");
	
	
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
			SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
			sysDate=sdf.format(today);
			System.out.println("sysDate****=========="+sysDate);
			//chguser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgUser");
			//chgterm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
			//chgTerm1 = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");	
			chguser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgterm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			
			java.util.Date date= new java.util.Date();
			Timestamp ts_now = new Timestamp(date.getTime());
			System.out.println("ts_now*********"+ts_now);
						
			System.out.println("Change User================"+chguser);
			System.out.println("chgTermm1================"+chgterm);
			finEntity = genericUtility.getColumnValue("fin_entity", headerDom);
			siteCodeFr = genericUtility.getColumnValue("site_code__fr", headerDom);
			siteCodeTo = genericUtility.getColumnValue("site_code__to", headerDom);
			itemCodeFr = genericUtility.getColumnValue("item_code__fr", headerDom);
			itemCodeTo = genericUtility.getColumnValue("item_code__to", headerDom);
			integralQty = genericUtility.getColumnValue("integral_qty", headerDom);
			reoQty = genericUtility.getColumnValue("reo_qty", headerDom);
			itemSer = genericUtility.getColumnValue("item_ser", headerDom);
			suppSour = genericUtility.getColumnValue("supp_sour", headerDom);
			siteCodeSupp = genericUtility.getColumnValue("site_code__supp", headerDom);
			System.out.println("siteCodeFr["+siteCodeFr+"]");
			System.out.println("siteCodeTo["+siteCodeTo+"]");
			System.out.println("itemCodeFr["+itemCodeFr+"]");
			System.out.println("itemCodeTo["+itemCodeTo+"]");
			System.out.println("integralQty["+integralQty+"]");
			System.out.println("reoQty["+reoQty+"]");
			System.out.println("itemSer["+itemSer+"]");
			System.out.println("suppSour["+suppSour+"]");
			System.out.println("siteCodeSupp["+siteCodeSupp+"]");
			
			System.out.println("Change date================"+sysDate);
			//System.out.println("Change date=TimeStamp==============="+sysDate1);
			//added by priyanka on 5/05/15 as per manoj sharma instruction
			
			if(integralQty!=null && integralQty.trim().length()>0)
			{
				if(integralQty.equalsIgnoreCase("0"))
				{
					System.out.println("integralQty before==========="+integralQty);
					
					integralQty=null;
					System.out.println("integralQty after==========="+integralQty);
				}
			}
			
			if(reoQty!=null && reoQty.trim().length()>0)
			{
				if(reoQty.equalsIgnoreCase("0"))
				{
					System.out.println("reoQty before==========="+reoQty);
					reoQty=null;
					System.out.println("reoQty after==========="+reoQty);
				}
			}
			
			if(suppSour.equalsIgnoreCase("N") )
			{
				
					System.out.println("suppSour before==========="+suppSour);
					suppSour=null;
					System.out.println("suppSour after==========="+suppSour);
				
			}
			
			if(checkNull(integralQty).trim().length() == 0 && checkNull(reoQty).trim().length() == 0 && checkNull(itemSer).trim().length() == 0 &&
					checkNull(suppSour).trim().length() == 0 && checkNull(siteCodeSupp).trim().length() == 0 )
			{
				resultString = itmDBAccessEJB.getErrorString("","PROCFAILED",userId,"",conn);
				return resultString;
			}
			sql1 = "select site_code,item_code from siteitem where site_code >= ? and site_code < = ?  and item_code >= ? and item_code < = ?";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, siteCodeFr);
			pstmt1.setString(2, siteCodeTo);
			pstmt1.setString(3, itemCodeFr);
			pstmt1.setString(4, itemCodeTo);
			rs1 = pstmt1.executeQuery();
			while (rs1.next())
			{
				siteCode = rs1.getString( "site_code" ).trim();
				itemCode = rs1.getString( "item_code" );
				String sql="",subQuery="",finalSiteCodeSupp="";
				sql = "update siteitem set ";
				if(integralQty != null && integralQty.trim().length()>0 )
				{	
					subQuery=" integral_qty='"+integralQty+"'";
					sql = sql +subQuery;
				}
				if(reoQty != null)
				{
					if (subQuery.trim().length() == 0)
					{
						subQuery = " reo_qty='" + reoQty+"'";
						sql = sql + subQuery;
					} else
					{
						subQuery = " , reo_qty='" +reoQty+"'";
						sql = sql + subQuery;
					}
				}
				if(itemSer != null)
				{
					if (subQuery.trim().length() == 0)
					{
						subQuery = " item_ser='" + itemSer+"'";
						sql = sql + subQuery;
					} else
					{
						subQuery = " , item_ser='" + itemSer+"'";
						sql = sql + subQuery;
					}
				}
				if(suppSour != null)
				{
					if (subQuery.trim().length() == 0)
					{
						subQuery = " supp_sour='" + suppSour+"'";
						sql = sql + subQuery;
					} else
					{
						subQuery = " , supp_sour='" + suppSour+"'";
						sql = sql + subQuery;
					}
				}
				if(siteCodeSupp != null)
				{
					if (subQuery.trim().length() == 0)
					{
						if("P".equalsIgnoreCase(suppSour))
						{
							finalSiteCodeSupp = siteCodeSupp.trim()+siteCode.substring(2, siteCode.length());
							System.out.println("finalSiteCodeSupp["+finalSiteCodeSupp+"]");
							subQuery = " site_code__supp='" +finalSiteCodeSupp+"'";
							sql = sql + subQuery;
						}
						else
						{
							subQuery = " site_code__supp='" + siteCodeSupp + "'";
							sql = sql + subQuery;
						}
					} else
					{
						if("P".equalsIgnoreCase(suppSour))
						{
							finalSiteCodeSupp = siteCodeSupp.trim()+siteCode.substring(2, siteCode.length());
							System.out.println("finalSiteCodeSupp>>>["+finalSiteCodeSupp+"]");
							subQuery = " , site_code__supp='" + finalSiteCodeSupp+"'";
							sql = sql + subQuery;
						}
						else
						{
							subQuery = " , site_code__supp='" + siteCodeSupp + "'";
							sql = sql + subQuery;
						}
					}
				}
				//sql=sql+ ",CHG_DATE= '"+sysDate1+"' ,CHG_USER= '"+chgUser+"', CHG_TERM= '"+chgTerm+"'";
			sql=sql+ " , CHG_DATE=?,CHG_USER=?,CHG_TERM=? ";
				
				sql = sql + " where site_code= ? and item_code = ?";
				System.out.println("final sql>>>>"+sql);
				pstmt = conn.prepareStatement( sql );
				pstmt.setTimestamp(1,ts_now);
				pstmt.setString(2,chguser);			
				pstmt.setString(3,chgterm);
				pstmt.setString(4,siteCode);				
				pstmt.setString(5,itemCode);					
				updCnt = pstmt.executeUpdate();
				{
					System.out.println("update count"+updCnt);
				}
				pstmt.close();
				pstmt = null;
			}
			pstmt1.close();
			pstmt1 = null;
			rs1.close();
			rs1 = null;

		} // end of try code 
   	catch(Exception e)
	{
		isError = true;
		e.printStackTrace();
		errString = e.getMessage();
		throw  new ITMException(e);
	}		
	finally
	{
		try
		{
			if(rs != null)rs.close();
			rs = null;
			if(pstmt != null)pstmt.close();
			pstmt = null;				
			if(conn != null)
			{
				if(isError)
				{
					conn.rollback();
					System.out.println("connection rollback.............");
					resultString = itmDBAccessEJB.getErrorString("","PROCFAILED",userId,"",conn);
				}	
				else
				{
					conn.commit();
					System.out.println("commiting connection.............");
					if(errString.equals(""))
					{
						errString = "PROCSUCC";
					}
					resultString = itmDBAccessEJB.getErrorString("",errString,userId,"",conn);
					
				}
				if(conn != null)
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
	return input;
}



}

 