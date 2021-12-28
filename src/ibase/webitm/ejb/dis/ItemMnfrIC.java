/********************************************************
        Title : ItemMnfrIC
        Date  : 06/09/12
        Developer: Akhilesh Sikarwar

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ItemMnfrIC extends ValidatorEJB
implements ItemMnfrICLocal, ItemMnfrICRemote
{
	//changed by nasruddin 07-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	String winName = null;
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
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
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String siteCode = "";
		String suppCodeMnfr = "";
		String refSer = "";
		String sql = "";
		String itemCode = "";
		String LockGroup = "";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		java.util.Date effFrom = null;
		java.util.Date TodayDate = null;
		java.util.Date validUpto = null;
		java.util.Date recdDate = null;
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		FinCommon finCommon = new FinCommon();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{ 
			this.finCommon = new FinCommon();
			this.validator = new ValidatorEJB();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
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
				int childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					System.out.println("CURRENT COLUMN IN  VALIDATION ["+childNodeName+"]");
					if (childNodeName.equalsIgnoreCase("supp_code__mnfr"))
					{
						suppCodeMnfr=checkNull(this.genericUtility.getColumnValue("supp_code__mnfr", dom));
						/* Changed By Nasruddin [19-SEP-16] START
						 * siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "site_code"));

						 * if (suppCodeMnfr != null && suppCodeMnfr.trim().length() > 0 )
						{

							errCode = finCommon.isSupplier(siteCode, suppCodeMnfr, refSer, conn);
							if(errCode !=  null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} */
						sql = "SELECT COUNT(1) FROM SUPPLIER WHERE SUPP_CODE = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, suppCodeMnfr);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if( cnt == 0)
						{
							errCode = "VMSUPP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							if("A".equalsIgnoreCase(editFlag))
							{
								itemCode =checkNull(this.genericUtility.getColumnValue("item_code", dom));
								sql = "select COUNT(1) from ITEMMNFR where ITEM_CODE = ? and SUPP_CODE__MNFR = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, suppCodeMnfr);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if( cnt > 0)
								{
									errCode = "VMDUPL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("lock_group"))
					{
						LockGroup = checkNull(this.genericUtility.getColumnValue("lock_group", dom));
						if( LockGroup != null && (!(LockGroup).equalsIgnoreCase("0")))
						{

							sql = "SELECT COUNT(1) FROM LOCK_GROUP WHERE LOCK_GROUP = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, LockGroup);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if( cnt == 0)
							{
								errCode = "VTLOCKGRP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
					}
					// Changed By Nasruddin [19-SEP-16] end
				}
				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.trim().equalsIgnoreCase("licence_no"))
					{
					}
					else if (childNodeName.trim().equalsIgnoreCase("eff_from"))
					{
						TodayDate = new Date();
						if(this.genericUtility.getColumnValue("eff_from", dom) != null &&  !this.genericUtility.getColumnValue("eff_from", dom).equals("DD/MM/YY"))
						{
							effFrom = dateFormat2.parse(this.genericUtility.getColumnValue("eff_from", dom));
							
							if(effFrom.compareTo(TodayDate) >= 0)
							{
								errCode = "VTLICEFTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					else if (childNodeName.trim().equalsIgnoreCase("valid_upto"))
					{
						if(this.genericUtility.getColumnValue("valid_upto", dom) != null &&  !this.genericUtility.getColumnValue("valid_upto", dom).equals("DD/MM/YY"))
						{
							effFrom = dateFormat2.parse(this.genericUtility.getColumnValue("eff_from", dom));
							validUpto = dateFormat2.parse(this.genericUtility.getColumnValue("valid_upto", dom));

				
							if(effFrom.compareTo(validUpto) > 0)
							{
								errCode = "VTLICVLDUP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("recd_date"))
					{
						TodayDate = new Date();
						if(this.genericUtility.getColumnValue("recd_date", dom) != null &&  !this.genericUtility.getColumnValue("recd_date", dom).equals("DD/MM/YY"))
						{
							recdDate = dateFormat2.parse(this.genericUtility.getColumnValue("recd_date", dom));
							if(recdDate.compareTo(TodayDate) > 0)
							{
								errCode = "VTLICRECDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}

				valueXmlString.append("</Detail2>");
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
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
		catch (Exception e)
		{
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


	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)
	throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("hELLO PRINT");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("VALUE HELLO PRINT["+valueXmlString+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA["+valueXmlString+"]");
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
	throws RemoteException, ITMException
	{
		System.out.println("sTART PRINT ");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";	
		String city = "";
		String descr ="";
		String licenceDscr = "";
		String itemCode = "";
		String suppCodeMnfr = "";
		String suppName = "";
		String suppCode = "";
		String licenceNo = "";
		String itemDescr = "";
		String refNo = "";
		String descrefNo = "";
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//comment by nasruddin 07-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		itemCode = "";//Modified by Anjali R. on[01/10/2018]
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			this.finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
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
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN ["+currentColumn+"]");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					itemCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "item_code"));
					valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
					sql = "select descr from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					valueXmlString.append("<item_descr>").append("<![CDATA["+descr+"]]>").append("</item_descr>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("supp_code__mnfr"))
				{
					suppCodeMnfr = checkNull(genericUtility.getColumnValue("supp_code__mnfr", dom));
					sql = "select supp_name,city from supplier where supp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, suppCodeMnfr);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						suppName = checkNull(rs.getString("supp_name"));
						city = checkNull(rs.getString("city"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;	

					valueXmlString.append("<supp_name>").append("<![CDATA["+suppName+"]]>").append("</supp_name>");
					valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");

				} 
				else if (currentColumn.trim().equalsIgnoreCase("supp_code"))
				{
					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					sql = "select supp_name,city from supplier where supp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, suppCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						suppName = checkNull(rs.getString("supp_name"));
						city = checkNull(rs.getString("city"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;	

					valueXmlString.append("<supp_name>").append("<![CDATA["+suppName+"]]>").append("</supp_name>");
					valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");
				}
				//Modified by Anjali R. on[01/10/2018][Added itemchange for column "item_code"][Start]
				else if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					
					sql = "select descr from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = checkNull(rs.getString(1));
					}
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
					System.out.println("itemCode---["+itemCode+"] \n descr---["+descr+"]");
					valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
					valueXmlString.append("<item_descr>").append("<![CDATA["+descr+"]]>").append("</item_descr>");
				}
				//Modified by Anjali R. on[01/10/2018][Added itemchange for column "item_code"][End]
				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.trim().equalsIgnoreCase("itm_default"))
					{
						itemCode = checkNull(this.genericUtility.getColumnValue("item_code", dom1));
						suppCodeMnfr = checkNull(this.genericUtility.getColumnValue("supp_code__mnfr", dom1));

						valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
						valueXmlString.append("<supp_code__mnfr>").append("<![CDATA["+suppCodeMnfr+"]]>").append("</supp_code__mnfr>");

						sql = "select supp_name from supplier where supp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, suppCodeMnfr);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							suppName = checkNull(rs.getString("supp_name"));	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	
						valueXmlString.append("<supp_name>").append("<![CDATA["+suppName+"]]>").append("</supp_name>");
						
						sql = "select descr from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							itemDescr = checkNull(rs.getString("descr"));	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	
						valueXmlString.append("<item_descr>").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");


						licenceNo = checkNull(this.genericUtility.getColumnValue("licence_no", dom));

						sql = "select descr from gencodes where fld_name ='LICENCE_NO' and fld_value = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, licenceNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							licenceDscr = checkNull(rs.getString("descr"));	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	

						valueXmlString.append("<licence_dscr>").append("<![CDATA["+licenceDscr+"]]>").append("</licence_dscr>");

					
					}
					else if (childNodeName.trim().equalsIgnoreCase("itm_defaultedit"))
					{
						itemCode = checkNull(this.genericUtility.getColumnValue("item_code", dom1));
						suppCodeMnfr = checkNull(this.genericUtility.getColumnValue("supp_code__mnfr", dom1));

						valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
						valueXmlString.append("<supp_code__mnfr>").append("<![CDATA["+suppCodeMnfr+"]]>").append("</supp_code__mnfr>");

						licenceNo = checkNull(this.genericUtility.getColumnValue("licence_no", dom));

						sql = "select descr from gencodes where fld_name ='LICENCE_NO' and fld_value = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, licenceNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							licenceDscr = checkNull(rs.getString("descr"));	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	

						valueXmlString.append("<licence_dscr>").append("<![CDATA["+licenceDscr+"]]>").append("</licence_dscr>");

						sql = "select descr from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, licenceNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							itemDescr = checkNull(rs.getString("descr"));	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	

						valueXmlString.append("<item_descr>").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");				
					}

					else if (childNodeName.trim().equalsIgnoreCase("licence_no"))
					{
						licenceNo = checkNull(this.genericUtility.getColumnValue("licence_no", dom));
						sql = "select descr from gencodes where fld_name ='LICENCE_NO' and fld_value = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, licenceNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							licenceDscr = checkNull(rs.getString("descr"));	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	

						valueXmlString.append("<licence_dscr>").append("<![CDATA["+licenceDscr+"]]>").append("</licence_dscr>");

					}

				}
				valueXmlString.append("</Detail2>");

			}
			valueXmlString.append("</Root>");
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
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}
	

	private String errorType(Connection conn, String errorCode)throws ITMException
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
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}

}

