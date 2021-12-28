/********************************************************
	override Failed business logic [ req id : D16DSER001 ]
********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 

@Stateless 
public class BusiLogicOverrideIC extends ValidatorEJB implements BusiLogicOverrideICLocal,BusiLogicOverrideICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [FlatBookingIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Connection conn = null;
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String reasonDescr="",reasonCode="" ;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		//Node childNode = null;
		//String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> ");

			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();

				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("--------------------ITM_DEFAULT-----------------------");
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String sysDate = sdf.format(currentDate.getTime());
					System.out.println("Now the date is :=>[" + sysDate+"]");
					
					valueXmlString.append("<from_date>").append("<![CDATA[" + sysDate + "]]>").append("</from_date>");
					valueXmlString.append("<to_date>").append("<![CDATA[" + sysDate + "]]>").append("</to_date>");
				}
				if(currentColumn.trim().equalsIgnoreCase("reas_code"))
				{
					System.out.println("--------------------reason_code-----------------------");
					reasonCode = checkNull(genericUtility.getColumnValue("reas_code", dom));
					if (reasonCode != null && reasonCode.trim().length() > 0)
					{
						sql = " select descr from gencodes " +
								" where fld_name = 'REAS_CODE'  and mod_name = 'W_BUSINESS_LOGIC_OVERRIDE' " +
								" and  fld_value = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, reasonCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							reasonDescr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

					}
					valueXmlString.append("<reason_descr>").append("<![CDATA[" + reasonDescr + "]]>").append("</reason_descr>");
				}

				valueXmlString.append("</Detail1>"); // close tag

				break;

			}//end of switch

			valueXmlString.append("</Root>");


		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{

				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;

				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return valueXmlString.toString();

	}

	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}


}
