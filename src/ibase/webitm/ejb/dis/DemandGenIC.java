package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.util.Calendar;
import java.sql.*;

import org.w3c.dom.*;

import javax.ejb.*;

import ibase.system.config.*;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.*;

import javax.ejb.Stateless;

@Stateless
public class DemandGenIC extends ValidatorEJB implements DemandGenICRemote,DemandGenICLocal
{
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		String resultString = "";
		Document dom = null,dom1 = null,dom2 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		try{
			if(xmlString != null){
				dom = genericUtility.parseString(xmlString);
			}
			if(xmlString1 != null){
				dom1 = genericUtility.parseString(xmlString1);
			}
			if(xmlString2 != null){
				dom2 = genericUtility.parseString(xmlString2);
			}
			resultString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(ITMException e)
		{
			System.out.println("ITMException "+e);
			e.printStackTrace();
			throw e;
		}
		catch(Exception e)
		{
			System.out.println("Exception in itemchange "+e);
			e.printStackTrace();
			throw new ITMException(e);
		}		
		return resultString;
	} 
	
	public String itemChanged(Document dom,Document dom1,Document dom2,String objContext, String currentColumn, String editFlag, String xtraParams) throws ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql="";
		ConnDriver connDriver = new ConnDriver();
		String siteCode="";
		StringBuffer valueXmlString;
		E12GenericUtility genericUtility = new E12GenericUtility();
		
		Timestamp fromDate=null,toDate=null;
		
		try
		{	
		conn=getConnection();
		siteCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSite");
		System.out.println("SiteCode["+siteCode+"]");
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
		valueXmlString.append(editFlag).append("</editFlag></header>");
		valueXmlString.append("<Detail1>");	
		
		if(currentColumn.equalsIgnoreCase("itm_default"))
		{
			//valueXmlString.append("<site_code><![CDATA[" + siteCode+ "]]></site_code>");
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp tranDate = java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			Calendar currentDate = Calendar.getInstance();
			System.out.println("currDate>>>>>>"+tranDate);
			
			
			sql = "SELECT FR_DATE,TO_DATE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE " ;
			System.out.println("sqlCnt : [" +sql+ "]");
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,tranDate);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				fromDate=rs.getTimestamp("FR_DATE");
				toDate=rs.getTimestamp("TO_DATE");
				
			}
			
			valueXmlString.append("<from_date><![CDATA["+sdf.format(fromDate)+"]]></from_date>");
			valueXmlString.append("<to_date><![CDATA["+sdf.format(toDate)+"]]></to_date>");
		
		}
		valueXmlString.append("</Detail1>");
		valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			throw new ITMException(e);
		}
	finally
	{
		try
		{
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}catch(Exception e2){}
	
	}
		return valueXmlString.toString();
		
		
	}

}

