/*
* the connection was globly declared which is removed and declared in proper method.
* By Jaimin on 29/08/2007 According to the requst-ID :DI78DIS028
*/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.text.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;

import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.TransIDGenerator;//TID
import ibase.utility.CommonConstants;//TID
import ibase.utility.E12GenericUtility;

import javax.ejb.Stateless; // added for 3

@Stateless // added for 3
public class TaxFormRecoMultiIc extends ValidatorEJB implements TaxFormRecoMultiIcLocal, TaxFormRecoMultiIcRemote
{
	java.sql.Connection conn=null; 
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB(); 
	java.sql.PreparedStatement pstmt=null;
	java.sql.ResultSet rs = null;
	String sql = null;
	String sundryType = null;
	//ConnDriver connDriver = new ConnDriver();	
	/*public void Create() throws RemoteException, CreateException 
	{
		System.out.println("TaxFormRecoMultiIc is in Process.ejb..ejb..ejb..ejb..ejb.");
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
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString); 
			System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [TaxFormRecoMultiIc][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
        return valueXmlString; 
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = "";
		String loginSite = "";
		String sundCode = null; //for both to and from
		String sundName = null; //for both to and from
		String taxCode = null; //for both to and from
		String taxCodeDes = null; //for both to and from

	
		String userId = getValueFromXTRA_PARAMS(xtraParams,"userId");
		String empCode = getValueFromXTRA_PARAMS(xtraParams,"empCode");
		loginSite = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
		valueXmlString.append(editFlag).append("</editFlag></header>");
		valueXmlString.append("<Detail1>");	
		ConnDriver connDriver = new ConnDriver();	//Added by Jaimin 29/08/2007 for the connection driver to establish a Connection.requst-ID :DI78DIS028
			
	
		try
		{
			//conn = getConnection(); 
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			sundryType = getColumnValue("sundry_type",dom);	
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					valueXmlString.append("<site_code>").append(loginSite).append("</site_code>");
					valueXmlString.append("<date_fr>").append(getCurrdateAppFormat()).append("</date_fr>");
					valueXmlString.append("<date_to>").append(getCurrdateAppFormat()).append("</date_to>");
					valueXmlString.append("<sundry_type>").append("C").append("</sundry_type>");
					valueXmlString.append("<sund_code_fr>").append("0").append("</sund_code_fr>");
					valueXmlString.append("<sund_code_to>").append("zz").append("</sund_code_to>");
					//valueXmlString.append("<sund_name_fr>").append("0").append("</sund_name_fr>");
					//valueXmlString.append("<sund_name_to>").append("zZz").append("</sund_name_to>");
					valueXmlString.append("<tax_code_fr>").append("0").append("</tax_code_fr>");
					valueXmlString.append("<tax_code_to>").append("zz").append("</tax_code_to>");
					//valueXmlString.append("<tax_code_des_fr>").append("Z").append("</tax_code_des_fr>");
					//valueXmlString.append("<tax_code_des_to>").append("Z").append("</tax_code_des_to>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("sund_code_fr"))
				{
					sundCode = getColumnValue("sund_code_fr",dom);
					sundName = getSundName(sundCode,conn);
					valueXmlString.append("<sund_name_fr>").append(sundName).append("</sund_name_fr>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("sund_code_to"))
				{
					sundCode = getColumnValue("sund_code_to",dom);
					sundName = getSundName(sundCode,conn);
					valueXmlString.append("<sund_name_to>").append(sundName).append("</sund_name_to>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("tax_code_fr"))
				{
					taxCode = getColumnValue("tax_code_fr",dom);
					taxCodeDes = getTaxDescr(taxCode,conn);
					valueXmlString.append("<tax_code_des_fr>").append(taxCodeDes).append("</tax_code_des_fr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("tax_code_to"))
				{
					taxCode = getColumnValue("tax_code_to",dom);
					taxCodeDes = getTaxDescr(taxCode,conn);
					valueXmlString.append("<tax_code_des_to>").append(taxCodeDes).append("</tax_code_des_to>");
				}
				
					valueXmlString.append("</Detail1>");
					valueXmlString.append("</Root>");	
				
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
		}
		finally
		{
			try{conn.close();conn = null;}catch(Exception d){}
			
		}
		System.out.println("valueXmlString:::::"+valueXmlString.toString());
		return valueXmlString.toString();

	}//END OF ITEMCHNGE

	
	private String getCurrdateAppFormat()
	{
		String currAppdate ="";
		java.sql.Timestamp currDate = null;
		try
		{
			Object date = null;
			currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
			System.out.println(genericUtility.getDBDateFormat());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
		System.out.println("Exception in getCurrdateAppFormat"+e.getMessage());
		}
		return (currAppdate);
	}

	private String getSundName(String sCode,Connection conn)
	{
		
		String sName = null; //for returning the value for sundry name
		if (sundryType.equalsIgnoreCase("B"))
		{
			sql = "Select bank_name from bank where bank_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("C"))
		{
			sql = "Select cust_name from customer where cust_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("E"))
		{
			sql = "Select emp_fname||' '||emp_mname||' '||emp_lname from employee where emp_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("S"))
		{
			sql = "Select supp_name from supplier where supp_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("X"))
		{
			sql = "Select tauth_name from tax_authority where tauth_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("L"))
		{
			sql = "Select party_name from loanparty where party_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("T"))
		{
			sql = "Select tran_name from transporter where tran_code = ? ";
		}
		else if (sundryType.equalsIgnoreCase("P"))
		{
			sql = "Select sp_name from sales_pers where sales_pers = ? ";
		}
		System.out.println("sql ::: " + sql);	
		System.out.println("Sundry Code ::: [" + sCode + "]" );
		try
		{
			pstmt = conn.prepareStatement(sql);
			String sCodeWs = sCode.trim()+"          ";
			pstmt.setString(1,sCodeWs.substring(0,10));
		
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				sName = (rs.getString(1)==null?" ":rs.getString(1));
			}
			else
			{
				sName =" ";
			}
			rs.close();
			pstmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
		}
		return sName;
	}

	private String getTaxDescr(String tCode,Connection conn)
	{
		
		String taxDescr = null;
		try
		{
			sql = "select descr from tax where tax_code = '" + tCode+"'"; //Added by Jaimin 29/08/2007 for getting descr for tax_code requst-ID :DI78DIS028
			System.out.println("SQL ::: [" + sql + "]");
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				taxDescr = (rs.getString(1)==null?" ":rs.getString(1));
			}
			else
			{
				taxDescr =" ";
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
		}
			return taxDescr;
	}

}