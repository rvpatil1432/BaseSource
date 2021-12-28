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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// added for ejb3
@Stateless
public class UnitConvIC extends ValidatorEJB implements UnitConvICLocal,UnitConvICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	// GenericUtility genericUtility = GenericUtility.getInstance();
	// FinCommon finCommon = null;
	// ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
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
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e)
		{
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException
	{

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String errorType = "";
		String userId = "";
		String sql = "";
		String unitFr = "";
		String unitTo = "";
		String itemCode = "";
		int ctr = 0;
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = null;
		//GenericUtility genericUtility = null;

		StringBuffer errStringXml = new StringBuffer(
				"<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{
			connDriver = new ConnDriver();
			//genericUtility = GenericUtility.getInstance();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					/* Comment By Nasruddin 21-Sep-16 START
					if (childNodeName.trim().equalsIgnoreCase("unit__fr"))
					{
						unitFr = genericUtility.getColumnValue("unit__fr", dom);
						if (unitFr == null || unitFr.trim().length() <= 0)
						{
							errCode = "VMUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (unitFr != null && unitFr.trim().length() > 0)
						{
							sql = "select count(*) from uom where unit = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unitFr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} Comment By Nasruddin 21-Sep-16 END */
				   if (childNodeName.trim().equalsIgnoreCase("unit__to"))
				   {
					   unitTo = genericUtility.getColumnValue("unit__to", dom);
					   unitFr = genericUtility.getColumnValue("unit__fr", dom);
					   /* Comment By Nasruddin 21-Sep-16  START
						if (unitTo == null || unitTo.trim().length() <= 0)
						{
							errCode = "VMUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} Comment By Nasruddin 21-Sep-16  Nasruddin */
					   if (unitFr.equalsIgnoreCase(unitTo))
					   {
						   errCode = "VTUNIT2";
						   errList.add(errCode);
						   errFields.add(childNodeName.toLowerCase());
					   }
					   // Changed By Nasruddin Start 21-SEP-16
					   else
					   {
						   if("A".equals(editFlag))
						   {
							   itemCode = genericUtility.getColumnValue("item_code", dom);
							   sql ="SELECT COUNT(1)  FROM UOMCONV 	WHERE UNIT__FR = ?	AND UNIT__TO = ?	AND ITEM_CODE = ?";
							   pstmt = conn.prepareStatement(sql);
							   pstmt.setString(1, unitFr);
							   pstmt.setString(2, unitTo);
							   pstmt.setString(3, itemCode);
							   rs = pstmt.executeQuery();
							   if (rs.next())
							   {
								   cnt = rs.getInt(1);
							   }
							   if (cnt > 0)
							   {
								   errCode = "VMDUPL1";
								   errList.add(errCode);
								   errFields.add(childNodeName.toLowerCase());
							   }
							   rs.close();
							   rs = null;
							   pstmt.close();
							   pstmt = null;
						   }
					   } // Changed By Nasruddin Start 21-SEP-16 END


					   /* Comment By Nasruddin 21-Sep-16  START
					   if (unitTo != null && unitTo.trim().length() > 0)
					   {
						   sql = "select count(*) from uom where unit = ? ";
						   pstmt = conn.prepareStatement(sql);
						   pstmt.setString(1, unitTo);
						   rs = pstmt.executeQuery();
						   if (rs.next())
						   {
							   cnt = rs.getInt(1);
						   }
						   if (cnt == 0)
						   {
							   errCode = "VTUNIT1";
							   errList.add(errCode);
							   errFields.add(childNodeName.toLowerCase());
						   }
						   rs.close();
						   rs = null;
						   pstmt.close();
						   pstmt = null;
					   }
					    Comment By Nasruddin 21-Sep-16  END*/
				   }
				   /* Comment By Nasruddin 21-Sep-16 START
					else if (childNodeName.trim().equalsIgnoreCase("item_code"))
					{
						System.out.println(""+""+""+"Change not made in UnitComnIC EJB extending ValidateEJB"+""+""+""+""+""+""+""+""+""+""+""+""+""+""+"");
						itemCode = genericUtility.getColumnValue("item_code",dom);
						unitTo = genericUtility.getColumnValue("unit__to", dom);
						unitFr = genericUtility.getColumnValue("unit__fr", dom);
						if (itemCode == null && itemCode.trim().length() <= 0)
						{
							errCode = "VMITEMCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (itemCode != null && itemCode.trim().length() > 0)
						{
							if (!"X".equalsIgnoreCase(itemCode.trim()))
							{
								sql = "SELECT COUNT(*) FROM ITEM WHERE ITEM_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0)
								{
									errCode = "VMITEMCD1 ";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if (editFlag.equalsIgnoreCase("A"))
							{
								sql = "select count(1) from uomconv where unit__fr = ? and unit__to = ? and item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unitFr.trim());
								pstmt.setString(2, unitTo.trim());
								pstmt.setString(3, itemCode.trim());
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt > 0)
								{
									errCode = "VMDUPL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

						}

					}Comment  by nasriuddin 19-sep-16 END*/
				}// END OF FOR
			}// END OF SWITCH
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0)
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(
								errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString
								+ errString.substring(
										errString.indexOf("</trace>") + 8,
										errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else
			{
				errStringXml = new StringBuffer("");
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
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
					conn.close();
				}
				conn = null;
			} catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException
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
			System.out
					.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n"
							+ e.getMessage());
		}
		return valueXmlString;

	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException
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
		String unitTo = "";
		String unitFr = "";
		String unitDescrTo = "";
		String unitDescrFr = "";
		String round = "";

		String chgTerm = "", chgUser = "";
		int ctr = 0;
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDate);
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,
					"chgTerm");
			System.out.println(":: chg term" + chgTerm);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,
					"chg_user");
			System.out.println(":: chg term" + chgUser);
			valueXmlString = new StringBuffer(
					"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr++;
				} while (ctr < childNodeListLength
						&& !childNodeName.equals(currentColumn));

				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					valueXmlString.append("<chg_user><![CDATA[")
							.append(chgUser).append("]]></chg_user>\r\n");
					valueXmlString.append("<chg_term><![CDATA[")
							.append(chgTerm).append("]]></chg_term>\r\n");
					valueXmlString.append("<chg_date><![CDATA[")
							.append(sysDate).append("]]></chg_date>\r\n");
				} else if (currentColumn.trim().equalsIgnoreCase("unit__fr"))
				{
					unitFr = genericUtility.getColumnValue("unit__fr", dom);
					sql = "select descr from uom where unit =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitFr);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						unitDescrFr = rs.getString(1);
					}
					valueXmlString.append("<uom_from_descr>")
							.append("<![CDATA[" + unitDescrFr + "]]>")
							.append("</uom_from_descr>");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else if (currentColumn.trim().equalsIgnoreCase("unit__to"))
				{
					unitTo = genericUtility.getColumnValue("unit__to", dom);
					sql = "select descr from uom where unit =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitTo);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						unitDescrTo = rs.getString(1);
					}
					valueXmlString.append("<uom_to_descr>")
							.append("<![CDATA[" + unitDescrTo + "]]>")
							.append("</uom_to_descr>");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				else if (currentColumn.trim().equalsIgnoreCase("round"))
				{
					round = genericUtility.getColumnValue("round", dom);
					System.out.println("::round::" + round);
					if (round.equalsIgnoreCase("X")
							|| round.equalsIgnoreCase("P")
							|| round.equalsIgnoreCase("R"))
					{
						System.out.println("::round::1" + round);
						valueXmlString.append("<round_to protect='0'>")
								.append("<![CDATA[1]]>").append("</round_to>");
					}

					if (round.equalsIgnoreCase("N") || round == null)
					{
						System.out.println("::round:N:" + round);
						valueXmlString.append("<round_to protect='1'>")
								.append("<![CDATA[0]]>").append("</round_to>");
						;
					}

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
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
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

	private String errorType(Connection conn, String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return msgType;
	}
}
