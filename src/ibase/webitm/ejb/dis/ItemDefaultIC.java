package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.ejb.Stateless;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class ItemDefaultIC extends ValidatorEJB implements ItemDefaultICRemote, ItemDefaultICLocal {

	private GenericUtility genericUtility = GenericUtility.getInstance();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String sql = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String tranId="";
		String itemser="",unit="",unitRate="",taxChap="",siteCode="",itemUsage="",itemType="",locType="",unitPurchase="",bomCode="";
		int currentFormNo = 0;
		int cnt = 0, cnt1 = 0,ctr=0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		try {

			conn = getConnection();

			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if ((objContext != null) && (objContext.trim().length() > 0)) {
				currentFormNo = Integer.parseInt(objContext);
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
				int childNodeListLength = childNodeList.getLength();
				tranId = genericUtility.getColumnValue("tran_id", dom);
				System.out.println("tran id from boqdet --4-->>>>[" + tranId + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("item_ser")) 
					{
						itemser = genericUtility.getColumnValue("item_ser", dom);

						if(itemser==null || itemser.trim().length()==0)
						{
							errCode = "VMITMSRNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*) as cnt from itemser where item_ser =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemser);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}    
						}

					}

					else if (childNodeName.equalsIgnoreCase("item_type")) 
					{
						itemType = genericUtility.getColumnValue("item_type", dom);

						if(itemType==null || itemType.trim().length()==0)
						{
							errCode = "VUITEM    ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*) as cnt from item_type where item_type =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemType);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTITEMTYPE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 

							System.out.println("editflag in item type"+editFlag);
							//if record already exist in item default master for enter item type then throw error  
							if(editFlag.equalsIgnoreCase("A"))
							{
								sql = "select count(*) as cnt from item_default where item_type =?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, itemType);
								rs = pstmt1.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt1.close();
								pstmt1 = null;

								if (cnt > 0)
								{
									errCode = "VTEXISTIT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} 
							}
						}

					}

					else if (childNodeName.equalsIgnoreCase("unit")) 
					{
						unit = genericUtility.getColumnValue("unit", dom);

						if(unit!=null && unit.trim().length()>0)
						{
							sql = "select count(*) as cnt from uom where unit =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unit);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTINVUNT02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}    
						}

					}
					else if (childNodeName.equalsIgnoreCase("loc_type")) 
					{
						locType = genericUtility.getColumnValue("loc_type", dom);

						if(locType!=null && locType.trim().length()>0)
						{
							sql = " SELECT count(*) FROM GENCODES WHERE  FLD_NAME  ='LOC_TYPE' AND FLD_VALUE= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, locType);
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
								errCode = "VMLOCINV  ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}    
						}

					}

					else if (childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap = genericUtility.getColumnValue("tax_chap", dom);

						if (taxChap != null && taxChap.trim().length() > 0)
						{
							sql = "select count(*) as cnt from taxchap where tax_chap =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("unit__rate")) 
					{
						unitRate = genericUtility.getColumnValue("unit__rate", dom);

						if(unitRate!=null && unitRate.trim().length()>0)
						{
							sql = "select count(*) as cnt from uom where unit =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unitRate);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTINVUNT02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}    
						}

					} 
					else if (childNodeName.equalsIgnoreCase("unit__pur")) 
					{
						unitPurchase = genericUtility.getColumnValue("unit__pur", dom);

						if(unitPurchase!=null && unitPurchase.trim().length()>0)
						{
							sql = "select count(*) as cnt from uom where unit =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unitPurchase);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTINVUNT02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}    
						}

					}
					else if (childNodeName.equalsIgnoreCase("site_code")) 
					{
						siteCode = genericUtility.getColumnValue("site_code", dom);
						System.out.println("siteCode: " + siteCode);

						if (siteCode != null && siteCode.trim().length() > 0) 
						{
							sql = "select count(*) as cnt from site where site_code =? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt==0)
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("item_usage")) 
					{
						itemUsage = genericUtility.getColumnValue("item_usage", dom);

						if(itemUsage==null || itemUsage.trim().length()==0)
						{
							errCode = "UVSITEMUSG";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}						
					}
					else if (childNodeName.equalsIgnoreCase("bom_code")) 
					{
						bomCode = genericUtility.getColumnValue("bom_code", dom);
						System.out.println("bomcode: " + bomCode);

						if (bomCode != null && bomCode.trim().length() > 0) 
						{
							sql = "select count(*) as cnt from bom where bom_code =? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, bomCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt==0)
							{
								errCode = "VTBOMINV  ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}

				valueXmlString.append("</Detail1>");
				break;
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					int pos = errCode.indexOf("~");
					System.out.println("pos :"+pos);
					if(pos>-1)
					{
						errCode=errCode.substring(0,pos);
					}

					System.out.println("error code is :"+errCode);
					errFldName = (String)errFields.get(cnt);
					if (errCode != null && errCode.trim().length() > 0) {
						errString = getErrorString(errFldName, errCode, userId);
						errorType = errorType(conn, errCode);
					}

					if (errString != null && errString.trim().length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 
								8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + 
								errString.substring(errString.indexOf("</trace>") + 
										8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} 
		finally 
		{
			try 
			{
				if (conn != null) 
				{
					conn.close();
					conn = null;
				}
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
			} 
			catch (Exception d) 
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString(xmlString2);
			}

			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("valueXmlString[" + valueXmlString + "]");
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : [ItemDefaultIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}

		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "", sql1 = "", sql2 = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		String itemType="",unit="",descr="",siteCode="",siteDescr="",taxChap="",taxChapDescr="",itemSer="",itemSerDescr="";
		String termId="",userId="",loginSite="";
		int ctr = 0;
		int currentFormNo = 0;
		java.util.Date reqDate = null;
		int childNodeListLength = 0;
		java.util.Date statusDate = null;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		String locType="";

		try
		{

			Date date = new Date();
			sdf = new SimpleDateFormat("dd/MM/yy");
			String sysDate = sdf.format(date);
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSite");                    
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0)) 
			{
				currentFormNo = Integer.parseInt(objContext);
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
				childNodeListLength = childNodeList.getLength();
				do 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) 
					{
						childNode.getFirstChild();
					}
					ctr++;
				} while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN [" + currentColumn + "]");

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					/*itemType =checkNullandTrim( genericUtility.getColumnValue("item_type", dom));

					sql = "select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						siteDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("descrSiteCode:: " + siteDescr);


					valueXmlString.append("<item_type><![CDATA[" + itemType + "]]></item_type>");  //change on 17-feb*/
					valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr><![CDATA[" + siteDescr + "]]></site_descr>");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + userId + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>");
					valueXmlString.append("<chg_date>").append("<![CDATA[" + sysDate + "]]>").append("</chg_date>");
				}

				//change on 17-feb
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{
					locType = checkNullandTrim(genericUtility.getColumnValue("loc_type", dom));
					itemType =checkNullandTrim( genericUtility.getColumnValue("item_type", dom));

					/*sql = "select descr from item_type where item_type= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemType);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_type><![CDATA[" + itemType.trim() + "]]></item_type>");
					valueXmlString.append("<descr><![CDATA[" + descr + "]]></descr>");*/

					valueXmlString.append("<loc_type><![CDATA[" + locType.trim() + "]]></loc_type>");
				}


				else if (currentColumn.trim().equalsIgnoreCase("item_type")) 
				{
					itemType =checkNullandTrim(genericUtility.getColumnValue("item_type", dom));

					sql = "select descr,unit from item_type where item_type= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemType);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = rs.getString("descr");
						unit= rs.getString("unit");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<item_type><![CDATA[" + itemType.trim() + "]]></item_type>");
					valueXmlString.append("<item_descr><![CDATA[" + descr + "]]></item_descr>");
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");

					if(unit!=null && unit.trim().length() > 0)
					{
						valueXmlString.append("<unit__pur><![CDATA[" + unit + "]]></unit__pur>");
						valueXmlString.append("<unit__rate><![CDATA[" + unit + "]]></unit__rate>");   
					}

				}

				else if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode= genericUtility.getColumnValue("site_code", dom);

					sql = "select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						siteDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("descrSiteCode:: " + siteDescr);

					valueXmlString.append("<site_descr><![CDATA[" + siteDescr + "]]></site_descr>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("item_ser"))
				{
					itemSer= genericUtility.getColumnValue("item_ser", dom);

					sql = "select descr from itemser where item_ser = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						itemSerDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<itemser_descr><![CDATA[" + itemSerDescr + "]]></itemser_descr>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("tax_chap")) 
				{
					taxChap = genericUtility.getColumnValue("tax_chap", dom);

					System.out.println("taxChap:- " + taxChap);

					sql = "select descr from taxchap where tax_chap= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, taxChap);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						taxChapDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<taxchap_descr><![CDATA[" + taxChapDescr + "]]></taxchap_descr>");                    
				}
				else if (currentColumn.trim().equalsIgnoreCase("unit"))
				{
					unit = genericUtility.getColumnValue("unit", dom);

					valueXmlString.append("<unit__pur><![CDATA[" + unit + "]]></unit__pur>");
					valueXmlString.append("<unit__rate><![CDATA[" + unit + "]]></unit__rate>");
				}

				valueXmlString.append("</Detail1>");
				break;
			}
			valueXmlString.append("</Root>");
			System.out.println("valueXmlString[" + valueXmlString.toString() + "]");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} 
		finally 
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
			}
			catch (Exception d) 
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String str) 
	{
		if (str == null)
		{
			return "";
		} 
		else 
		{
			return str;
		}
	}

	private double checkDoubleNull(String str) 
	{
		if (str == null || str.trim().length() == 0)
		{
			return 0.0;
		} 
		else
		{
			return Double.parseDouble(str);
		}
	}

	private String errorType(Connection conn, String errorCode) throws ITMException
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
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			try {
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
				throw new ITMException(e);
			}
		} 
		finally 
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
				throw new ITMException(e);
			}
		}
		return msgType;
	}
	private String checkNullandTrim(String input) {
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
}