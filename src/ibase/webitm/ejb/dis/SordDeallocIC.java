
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
import java.sql.Timestamp;
@Stateless  

public class SordDeallocIC extends ValidatorEJB implements SordDeallocICLocal, SordDeallocICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if ( xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;


	}
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null,pstmt2 = null;
		ResultSet rs = null,rs2 = null ;
		Connection conn = null;
		String saleOrderFrom = null;
		String saleOrderTo = null;
		String sql1 ="",custCodeSord = "",waveType = "";
		
		StringBuffer valueXmlString = new StringBuffer();
		ConnDriver connDriver = new ConnDriver();
		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{   

		valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
		valueXmlString.append(editFlag).append("</editFlag> </header>");
		valueXmlString.append("<Detail1>");
		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		System.out.println("CURRENT ["+currentColumn+"]");
		
		if (currentColumn.trim().equalsIgnoreCase("itm_default"))
		{
			
		}
		//added by cpandey on 06/Sep/12
		else if (currentColumn.trim().equalsIgnoreCase("sale_order__from"))
		{
			saleOrderFrom = genericUtility.getColumnValue("sale_order__from",dom);
			saleOrderTo = genericUtility.getColumnValue("sale_order__to",dom);
			System.out.println("sale order from ["+saleOrderFrom+"]");
		
			if(saleOrderTo.equalsIgnoreCase(saleOrderFrom))
			{
				sql1 = " SELECT cust_code__dlv FROM SORDER WHERE SALE_ORDER = ? ";
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,saleOrderFrom);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					custCodeSord = rs2.getString("cust_code__dlv");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				sql1 = " select wave_type from customer where cust_code = ? ";
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,custCodeSord);
			    rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					waveType = rs2.getString("wave_type");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				sql1 = " select case when ACTIVE_PICK_ALLOW is null then 'Y' else ACTIVE_PICK_ALLOW end as ACTIVE_PICK_ALLOW from wave_type where wave_type = ? ";
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,waveType);
			    rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					valueXmlString.append("<active_pick_allow>").append("<![CDATA["+rs2.getString("ACTIVE_PICK_ALLOW") +"]]>").append("</active_pick_allow>");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				
			}
			else
			{
				valueXmlString.append("<active_pick_allow>").append("<![CDATA[ ]]>").append("</active_pick_allow>");

			}

		}
		else if (currentColumn.trim().equalsIgnoreCase("sale_order__to"))
		{
			saleOrderTo = genericUtility.getColumnValue("sale_order__to",dom);
			saleOrderFrom = genericUtility.getColumnValue("sale_order__from",dom);
			System.out.println("sale order to ["+saleOrderTo+"]");
			
			if(saleOrderTo.equalsIgnoreCase(saleOrderFrom))
			{
				sql1 = " SELECT cust_code__dlv FROM SORDER WHERE SALE_ORDER = ? ";
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,saleOrderFrom);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					custCodeSord = rs2.getString("cust_code__dlv");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				sql1 = " select wave_type from customer where cust_code = ? ";
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,custCodeSord);
			    rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					waveType = rs2.getString("wave_type");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				sql1 = " select case when ACTIVE_PICK_ALLOW is null then 'Y' else ACTIVE_PICK_ALLOW end as ACTIVE_PICK_ALLOW from wave_type where wave_type = ? ";
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,waveType);
			    rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					valueXmlString.append("<active_pick_allow>").append("<![CDATA["+rs2.getString("ACTIVE_PICK_ALLOW")+"]]>").append("</active_pick_allow>");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				
			}
			else
			{
				valueXmlString.append("<active_pick_allow>").append("<![CDATA[ ]]>").append("</active_pick_allow>");

			}

		}
		
		valueXmlString.append("</Detail1>");
		valueXmlString.append("</Root>");
	}catch(Exception e)
	{
		e.printStackTrace();
		System.out.println("Exception ::"+ e.getMessage());
		throw new ITMException(e);
	}
	finally
	{
		try
		{
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}

	}
	System.out.println("RETURN STRING--->> "+valueXmlString.toString());          
	return valueXmlString.toString();
	}
}


