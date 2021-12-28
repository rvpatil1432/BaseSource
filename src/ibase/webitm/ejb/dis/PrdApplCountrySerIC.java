/********************************************************
	Title :  Division for ES3.[ Req Id - D15JSUN008 ]
 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.mfg.MfgCommon;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import ibase.utility.E12GenericUtility;
import java.text.DecimalFormat;
import org.apache.xerces.dom.AttributeMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class PrdApplCountrySerIC extends ValidatorEJB implements PrdApplCountrySerICLocal, PrdApplCountrySerICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
				System.out.println("xmlString[" + xmlString + "]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1[" + xmlString1 + "]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2[" + xmlString2 + "]");
			}

			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [POBWizIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String loginEmpCode = "";
		int cnt = 0;
		int ctr = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		int currentFormNo = 0;

		NodeList parentList = null;
		NodeList childList = null;

		String countryCode="",itemSer="",refCode="",country="",countCode="",siteCode="";
		try
		{
			System.out.println("@@@@@@@@ wfvaldata called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
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
				ArrayList filter = new ArrayList();
				filter.add(0, "site_code");
				filter.add(1, "count_code");
				filter.add(2, "ref_code");
				for (int fld = 0; fld < filter.size(); fld++)
				{
					childNodeName = (String) filter.get(fld);
					System.out.println("@@@@@@@@@@ childNodeName["+childNodeName+"]");
					if (childNodeName.equalsIgnoreCase("site_code"))
					{
						siteCode = this.genericUtility.getColumnValue("site_code", dom);
						if (siteCode != null && siteCode.trim().length() > 0)
						{

							if (!(isExist(conn, "site", "site_code", siteCode.trim())))
							{
								errCode = "SITENOTEXT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMSITENUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("count_code"))
					{  
						countCode = this.genericUtility.getColumnValue("count_code", dom);
						if (countCode != null && countCode.trim().length() > 0)
						{

							if (!(isExist(conn, "country", "count_code", countCode.trim())))
							{
								System.out.println("@@@@@@@@@ invalid country...........");
								errCode = "VTCONT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} 
						else
						{
							System.out.println("@@@@@@@@@ null country...........");
							errCode = "VTCONT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if (childNodeName.equalsIgnoreCase("ref_code"))
					{
						refCode = this.genericUtility.getColumnValue("ref_code", dom);
						country = this.genericUtility.getColumnValue("count_code", dom);
						if (refCode != null && refCode.trim().length() > 0)
						{

							System.out.println("@@@@@@@@@@@@ refCode["+refCode+"]");
							if( refCode != null && refCode.trim().length() > 0 )
							{
								String tranId[]=refCode.split("_");
								if( tranId.length > 0 )
								{
									countryCode=tranId[0];
								}
								if( tranId.length > 1 )
								{
									itemSer=tranId[1];
								}
							}
							if( country!= null)
							{
								country=country.trim();
							}
							if( countryCode!= null)
							{
								countryCode=countryCode.trim();
							}
							System.out.println("@@@@@@@@@@@@ country["+country+"]countryCode["+countryCode+"]");
							if( ! country.equalsIgnoreCase(countryCode))
							{
								errCode = "VTCONTINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							sql = " select count(1)  FROM customer_series where item_ser = ? " +
									" and  cust_code in ( select cust_code from customer where count_code = ? ) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							pstmt.setString(2, countryCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null; 
							System.out.println("@@@@@@@@@@@ cnt["+cnt+"]");
							if( cnt==0 )
							{    
								errCode = "VMINVDIVSN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
							cnt=0;
							sql = " select count(1)  FROM period_appl where   ref_code = ?  " +
									" AND case when type is null then 'X' else type end='S' ";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, siteCode);
							pstmt.setString(1, refCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null; 
							System.out.println("@@@ cnt["+cnt+"]");
							if( cnt > 0 )
							{    
								errCode = "VMPMKY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
						else
						{
							errCode = "VMDIVISNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}


				}
				break; // case 1 end
			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0)
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("@@@@@@@@@@@@@@@ errCode cnt["+cnt+"]..........["+errCode+"]");
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
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

	// end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............." + xmlString);
		System.out.println("xmlString1............" + xmlString1);
		System.out.println("xmlString2............" + xmlString2);
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
			System.out.println("Exception : [EcollectionIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String sql = "", sql1 = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null ;
		E12GenericUtility genericUtility = new E12GenericUtility();
		java.util.Date currDate = new java.util.Date();
		ConnDriver connDriver = new ConnDriver();

		int currentFormNo = 0, childNodeListLength = 0;
		String columnValue = "", userId = "", termId = "",refCode="", loginSiteCode = "", siteDescr = "", siteCode = "";
		String loginEmpCode = "";
		int parentNodeListLength = 0, cnt=0;

		String addDate="", addUser="", addTerm="", chgUser="", chgTerm="";
		String countyCode="",countryDescr="";
		try
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
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
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("Now the date is :=>  " + sysDate);

			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");

			System.out.println("**********ITEMCHANGE FOR CASE ["+currentFormNo+"]["+currentColumn+"]**************");
			switch (currentFormNo)
			{
			case 1:
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{


					String prvTranId = checkNull(genericUtility.getColumnValue("prv_tran_id", dom1));	
					System.out.println("@@@@@@@@@@@@ prvTranId["+prvTranId+"]");
					if( prvTranId != null && prvTranId.trim().length() > 0 )
					{
						String tranId[]=prvTranId.split(":");
						if( tranId.length > 0 )
						{
							siteCode=tranId[0];
						}
						if( tranId.length > 1 )
						{
							refCode=tranId[1];
						}

					}
					else
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));		
						refCode = checkNull(genericUtility.getColumnValue("ref_code", dom1));
					}
					System.out.println("@@@@@@@@@@@pk values siteCode["+siteCode+"]refCode["+refCode+"]");

					sql = " select count(1) from period_appl where site_code = ? and ref_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, refCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;  
					System.out.println("@@@@@@@@@@@@ cnt["+cnt+"]");
					if( cnt < 1 )
					{
						sql = " select descr from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, loginSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteDescr = checkNull(rs.getString("descr"));
							System.out.println("siteDescr :" + siteDescr);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;   

						sql = " select count_code,descr from country " +
								"where count_code in ( select count_code from state " +
								"where state_code in ( select state_code from site where site_code = ? )) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, loginSiteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							countyCode = checkNull(rs.getString("count_code"));
							countryDescr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;  


						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						valueXmlString.append("<site_code>").append("<![CDATA[" + loginSiteCode + "]]>").append("</site_code>\r\n");
						valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr + "]]>").append("</site_descr>\r\n");
						valueXmlString.append("<count_code>").append("<![CDATA[" + countyCode + "]]>").append("</count_code>\r\n");
						valueXmlString.append("<country_descr>").append("<![CDATA[" + countryDescr + "]]>").append("</country_descr>\r\n");
						valueXmlString.append("<type>").append("S").append("</type>\r\n");
						valueXmlString.append("<add_date>").append("<![CDATA[" + sysDate + "]]>").append("</add_date>\r\n");
						valueXmlString.append("<add_user>").append("<![CDATA[" + loginEmpCode + "]]>").append("</add_user>\r\n");
						valueXmlString.append("<add_term>").append("<![CDATA[" + termId + "]]>").append("</add_term>\r\n");
						valueXmlString.append("<chg_date>").append("<![CDATA[" + sysDate + "]]>").append("</chg_date>\r\n");
						valueXmlString.append("<chg_user>").append("<![CDATA[" + loginEmpCode + "]]>").append("</chg_user>\r\n");
						valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>\r\n");
						valueXmlString.append("<ref_code>").append("").append("</ref_code>\r\n");
						valueXmlString.append("<prd_tblno>").append("").append("</prd_tblno>\r\n");

						valueXmlString.append("</Detail1>");

					}
					else
					{
						sql = " select add_date,add_user,add_term from period_appl where site_code = ? and ref_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, refCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							//addDate = checkNull(rs.getTimestamp("add_date"));
							addDate = rs.getString("add_date");
							addUser = checkNull(rs.getString("add_user"));
							addTerm = checkNull(rs.getString("add_term"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;  

						sql = " select descr from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteDescr = checkNull(rs.getString("descr"));
							System.out.println("siteDescr :" + siteDescr);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;   

						sql = " select count_code,descr from country " +
								"where count_code in ( select count_code from state " +
								"where state_code in ( select state_code from site where site_code = ? )) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							countyCode = checkNull(rs.getString("count_code"));
							countryDescr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;  

						addDate = genericUtility.getValidDateString(addDate,genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat()) ;
						
						System.out.println("@@@@@@@@@@@addDate["+addDate+"]refCode["+refCode+"]");		

						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"E\"  status=\"O\" pkNames=\"\" />\r\n");
						valueXmlString.append("<site_code protect = \"1\">").append("<![CDATA[" + siteCode + "]]>").append("</site_code>\r\n");
						valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr + "]]>").append("</site_descr>\r\n");
						valueXmlString.append("<count_code>").append("<![CDATA[" + countyCode + "]]>").append("</count_code>\r\n");
						valueXmlString.append("<country_descr>").append("<![CDATA[" + countryDescr + "]]>").append("</country_descr>\r\n");
						valueXmlString.append("<type>").append("S").append("</type>\r\n");
						valueXmlString.append("<add_date>").append("<![CDATA[" + addDate + "]]>").append("</add_date>\r\n");
						valueXmlString.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
						valueXmlString.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
						valueXmlString.append("<chg_date>").append("<![CDATA[" + sysDate + "]]>").append("</chg_date>\r\n");
						valueXmlString.append("<chg_user>").append("<![CDATA[" + loginEmpCode + "]]>").append("</chg_user>\r\n");
						valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>\r\n");
						valueXmlString.append("<ref_code protect = \"1\">").append("<![CDATA[" + refCode + "]]>").append("</ref_code>\r\n");
						valueXmlString.append("<prd_tblno>").append("<![CDATA[" + refCode + "]]>").append("</prd_tblno>\r\n");

						valueXmlString.append("</Detail1>");
					}



				}
				else if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					System.out.println("@@@@@@@@@@@ siteCode["+siteCode+"]");	

					sql = " select descr from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						siteDescr = checkNull(rs.getString("descr"));
						System.out.println("siteDescr :" + siteDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;   

					sql = " select count_code,descr from country " +
							"where count_code in ( select count_code from state " +
							"where state_code in ( select state_code from site where site_code = ? )) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						countyCode = checkNull(rs.getString("count_code"));
						countryDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;  

					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>\r\n");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr + "]]>").append("</site_descr>\r\n");
					valueXmlString.append("<count_code>").append("<![CDATA[" + countyCode + "]]>").append("</count_code>\r\n");
					valueXmlString.append("<country_descr>").append("<![CDATA[" + countryDescr + "]]>").append("</country_descr>\r\n");
					valueXmlString.append("<type>").append("S").append("</type>\r\n");
					valueXmlString.append("<add_date>").append("<![CDATA[" + sysDate + "]]>").append("</add_date>\r\n");
					valueXmlString.append("<add_user>").append("<![CDATA[" + loginEmpCode + "]]>").append("</add_user>\r\n");
					valueXmlString.append("<add_term>").append("<![CDATA[" + termId + "]]>").append("</add_term>\r\n");
					valueXmlString.append("<chg_date>").append("<![CDATA[" + sysDate + "]]>").append("</chg_date>\r\n");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + loginEmpCode + "]]>").append("</chg_user>\r\n");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>\r\n");
					//	valueXmlString.append("<ref_code>").append("<![CDATA[" + refCode + "]]>").append("</ref_code>\r\n");
					//	valueXmlString.append("<prd_tblno>").append("<![CDATA[" + refCode + "]]>").append("</prd_tblno>\r\n");
					valueXmlString.append("<ref_code>").append("").append("</ref_code>\r\n");
					valueXmlString.append("<prd_tblno>").append("").append("</prd_tblno>\r\n");
					valueXmlString.append("</Detail1>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("ref_code"))
				{


					refCode = checkNull(genericUtility.getColumnValue("ref_code", dom1));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					siteDescr = checkNull(genericUtility.getColumnValue("site_descr", dom1));
					countyCode = checkNull(genericUtility.getColumnValue("count_code", dom1));
					countryDescr = checkNull(genericUtility.getColumnValue("country_descr", dom1));
					addDate = checkNull(genericUtility.getColumnValue("add_date", dom1));
					addUser = checkNull(genericUtility.getColumnValue("add_user", dom1));
					addTerm = checkNull(genericUtility.getColumnValue("add_term", dom1));
					sysDate = checkNull(genericUtility.getColumnValue("chg_date", dom1));
					chgUser = checkNull(genericUtility.getColumnValue("chg_user", dom1));
					chgTerm = checkNull(genericUtility.getColumnValue("chg_term", dom1));

					System.out.println("@@@@@@@@@@@ refCode["+refCode+"]");		

					valueXmlString.append("<Detail1 domID='1' objContext='1'>");

					valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>\r\n");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr + "]]>").append("</site_descr>\r\n");
					valueXmlString.append("<count_code>").append("<![CDATA[" + countyCode + "]]>").append("</count_code>\r\n");
					valueXmlString.append("<country_descr>").append("<![CDATA[" + countryDescr + "]]>").append("</country_descr>\r\n");
					valueXmlString.append("<type>").append("S").append("</type>\r\n");
					valueXmlString.append("<add_date>").append("<![CDATA[" + addDate + "]]>").append("</add_date>\r\n");
					valueXmlString.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
					valueXmlString.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
					valueXmlString.append("<chg_date>").append("<![CDATA[" + sysDate + "]]>").append("</chg_date>\r\n");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>\r\n");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>\r\n");
					valueXmlString.append("<ref_code>").append("<![CDATA[" + refCode + "]]>").append("</ref_code>\r\n");
					valueXmlString.append("<prd_tblno>").append("<![CDATA[" + refCode + "]]>").append("</prd_tblno>\r\n");

					valueXmlString.append("</Detail1>");
				}
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

	public String getRequiredDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return decFormat.format(actVal);
	}

	public static java.util.Date relativeDate(java.util.Date date, int days)
	{
		java.util.Date calculatedDate = null;
		if (date != null)
		{
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, days);
			calculatedDate = new java.util.Date(calendar.getTime().getTime());
		}
		return calculatedDate;
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
			if (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}
		return msgType;
	}

	private boolean isExist(Connection conn, String tableName, String columnName, String value) throws ITMException, RemoteException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		boolean status = false;
		int cnt=0;
		try
		{
			sql = "SELECT count(1) from " + tableName + " where " + columnName + "  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@@@@@@@ cnt["+cnt+"]");
			if( cnt > 0 )
			{
				status = true;
			}
		} catch (Exception e)
		{
			System.out.println("Exception in isExist ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from isExist status["+status+"]");
		return status;
	}

	public String getAttributesAboutNode(Node node)
	{
		StringBuffer strValue = new StringBuffer();
		short type = node.getNodeType();
		switch (type)
		{
		case 1:
		{
			NamedNodeMap attrs = node.getAttributes();
			int len = attrs.getLength();
			for (int i = 0; i < len; ++i)
			{
				Attr attr = (Attr) attrs.item(i);
				strValue.append(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
			}
		}
		}
		return strValue.toString();
	}




}	
