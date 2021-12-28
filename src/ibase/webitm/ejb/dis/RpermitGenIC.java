/**
 * DEVELOP BY RITESH ON 02/JAN/14
 * PURPOSE: ROAD PERMIT RECORDS GENERATION PROCESS (DI3ISUN015)
 */
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// added for ejb3
@Stateless
public class RpermitGenIC extends ValidatorEJB implements RpermitGenICLocal,RpermitGenICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ itemChanged called");
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
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception :[itemChanged( String, String )] :==>\n"+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return valueXmlString;

	}

	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("@@@@@@@ itemChanged called");
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Node parentNode1 = null;
		String childNodeName = null;
		String sql = "";
		String loginSite = "",stateCodeTo="",finEntity="";
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = null;
		ConnDriver connDriver = null;
		try
		{
			//genericUtility = GenericUtility.getInstance();
			connDriver =  new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDate = sdf.format(currentDate.getTime());

			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			 case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				//int childNodeListLength = childNodeList.getLength();
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("::itm_default:: - ");
					sql = " select fin_entity, state_code from site where site_code = ? "; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						finEntity = rs.getString("fin_entity");
						stateCodeTo = rs.getString("state_code");
					}
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					if (rs != null)
						rs.close();
					rs = null;
					valueXmlString.append("<fin_entity>").append("<![CDATA["+ finEntity +"]]>").append("</fin_entity>");
					valueXmlString.append("<state_code__to>").append("<![CDATA["+ stateCodeTo +"]]>").append("</state_code__to>");
					valueXmlString.append("<eff_date>").append("<![CDATA["+ currDate +"]]>").append("</eff_date>");
					valueXmlString.append("<valid_upto>").append("<![CDATA["+ currDate +"]]>").append("</valid_upto>");

				}
				valueXmlString.append("</Detail1>");
		    }
			valueXmlString.append("</Root>");

		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
				{
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					if (rs != null)
						rs.close();
					rs = null;
					conn.close();
					conn = null;
				}
			} catch (Exception d)
			{
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}
}
