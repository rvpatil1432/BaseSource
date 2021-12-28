/*
 * Request Id=DI2ASUN035
 * 
 */
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
@javax.ejb.Stateless
public class SalePersonIC extends ValidatorEJB implements SalePersonICRemote,SalePersonICLocal//implements SessionBean
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	java.sql.PreparedStatement pstmt=null;
	java.sql.ResultSet rs = null;

	String sql = null;
	String sundryType = null;
	String  errString = null;
	String loginSite = "" ;
	String xmlHeader = null;

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
			xmlHeader = xmlString1;
			dom = parseString(xmlString);
			System.out.println("xmlString " + xmlString);
			System.out.println("xmlString1 " + xmlString1);
			System.out.println("xmlString2 " + xmlString2);
			System.out.println("[SalesPersLEJB]itemChanged Called...........");
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SalesPersLEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
        return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		boolean isFound=false;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String xmlStringDom = null;
		int ctr=0;
		String columnValue = null;
		String childNodeName = null;
		Connection conn = null;
		String chqName = null;
		String countCode = null;
		String currCode = null;
		String descr = null;
		String name = "";
		String shName = "",contPers="",contPfx="",addr1="",addr2="",stancode="",telNo1="",telNo2="",telNo3="",add1="",add2="",add3="",cpin="";
		String city = "",pin="",stateCode="",tele1="",tele2="",tele3="",teleExt="",fax="",firstName="",lastName="",mobileNo="",stanCode="";
	
		int currentFormNo = 0;
		String emailId="";
		String spFName = "";
		String spMName = "";
		String spLName = "";
		

		try
		{

			String currAppdate ="";
			java.sql.Timestamp currDate = null;
			currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();

			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out.println("[SalesPersLEJB]connection 2 is opened......");
			String loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			String loginEmpCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");

			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			//System.out.println("FORM NO:::"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");

			switch(currentFormNo)
			{
				case 1 :
					valueXmlString.append("<Detail1>\r\n");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					int childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					
					
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
										
							valueXmlString.append("<lock_status>").append("<![CDATA[1]]>").append("</lock_status>");					
					
					}
					if (currentColumn.trim().equals("sp_name"))
					{
						if (columnValue != null)
						{
							chqName = getColumnValue("chq_name",dom);
							if (chqName == null || chqName.trim().length() == 0 )
							{
								valueXmlString.append("<chq_name>").append("<![CDATA["+columnValue+"]]>").append("</chq_name>");

							}
						}
					}
					if (currentColumn.trim().equals("emp_code"))
					{
						

						if (columnValue != null)
						{
							sql="SELECT EMP_FNAME,EMP_LNAME,EMP_MNAME,CUR_ADD1,CUR_ADD2,CUR_ADD3,CUR_PIN,CUR_STATE,STAN_CODE,MOBILE_NO,CUR_TEL1,CUR_TEL2,CUR_TEL3,EMAIL_ID_OFF FROM EMPLOYEE WHERE EMP_CODE='"+columnValue.trim()+"'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								System.out.println("sumit inside rs.next()");
								firstName=rs.getString("EMP_FNAME");
								lastName=rs.getString("EMP_LNAME");
								String middleName=rs.getString("EMP_MNAME");
								mobileNo=rs.getString("MOBILE_NO");
								telNo1=rs.getString("CUR_TEL1");
								telNo2=rs.getString("CUR_TEL2");
								telNo3=rs.getString("CUR_TEL3");
								add1=rs.getString("CUR_ADD1");
								add2=rs.getString("CUR_ADD2");
								add3=rs.getString("CUR_ADD3");
								stancode=rs.getString("STAN_CODE");
								cpin=rs.getString("CUR_PIN");
								emailId=rs.getString("EMAIL_ID_OFF");
								stanCode=rs.getString("CUR_STATE");
								stancode=rs.getString("STAN_CODE");

								
								spFName = ( checkNull(firstName).trim().length() > 0 )? firstName : "";
								spMName = ( checkNull(middleName).trim().length() > 0 )? (spFName+" "+middleName) : spFName;
								spLName = ( checkNull(lastName).trim().length() > 0 )? (spMName+" "+lastName) : spMName;

								
								valueXmlString.append("<sp_name><![CDATA["+ spLName +"]]></sp_name>");
								

								valueXmlString.append("<sh_name><![CDATA["+checkNull(firstName)+"  "+checkNull(lastName)+"]]></sh_name>");
								valueXmlString.append("<addr1><![CDATA["+checkNull(add1)+"]]></addr1>");
								valueXmlString.append("<addr2><![CDATA["+checkNull(add2)+"]]></addr2>");
								valueXmlString.append("<addr3><![CDATA["+checkNull(add3)+"]]></addr3>");
								valueXmlString.append("<tele1><![CDATA["+checkNull(telNo1)+"]]></tele1>");
								valueXmlString.append("<tele2><![CDATA["+checkNull(telNo2)+"]]></tele2>");
								valueXmlString.append("<tele3><![CDATA["+checkNull(telNo3)+"]]></tele3>");
								valueXmlString.append("<pin><![CDATA["+checkNull(cpin)+"]]></pin>");
								valueXmlString.append("<email_addr><![CDATA["+checkNull(emailId)+"]]></email_addr>");
								valueXmlString.append("<state_code><![CDATA["+checkNull(stanCode)+"]]></state_code>");
								valueXmlString.append("<station_stan_code><![CDATA["+checkNull(stancode)+"]]></station_stan_code>");
							}
							else
							{
								valueXmlString.append("<sp_name></sp_name>");
								valueXmlString.append("<sh_name></sh_name>");
								valueXmlString.append("<addr1></addr1>");
								valueXmlString.append("<addr2></addr2>");
								valueXmlString.append("<addr3></addr3>");
								valueXmlString.append("<tele1></tele1>");
								valueXmlString.append("<tele2></tele2>");
								valueXmlString.append("<tele3></tele3>");
								valueXmlString.append("<pin></pin>");
								valueXmlString.append("<email_addr></email_addr>");
								valueXmlString.append("<state_code></state_code>");
								valueXmlString.append("<station_stan_code></station_stan_code>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							
							
							
							
							String getState="SELECT DESCR,COUNT_CODE FROM STATE WHERE STATE_CODE='"+stanCode+"'";
							String stateDescr="";
							String country="";
							pstmt = conn.prepareStatement(getState);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								stateDescr = rs.getString("DESCR");
								country=rs.getString("COUNT_CODE");
								valueXmlString.append("<state_descr><![CDATA["+checkNull(stateDescr)+"]]></state_descr>");
								valueXmlString.append("<count_code><![CDATA["+checkNull(country)+"]]></count_code>");
							}
							else
							{
								valueXmlString.append("<state_descr></state_descr>");
								valueXmlString.append("<count_code></count_code>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							
							
							
							String getStation="SELECT DESCR FROM STATION WHERE STAN_CODE='"+stancode+"'";
							System.out.println("Sumit getStation "+getStation);
							String stationDescr="";
							pstmt = conn.prepareStatement(getStation);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								System.out.println("Sumit inside state ");
								stationDescr = rs.getString("DESCR");
								valueXmlString.append("<station_descr><![CDATA["+checkNull(stationDescr)+"]]></station_descr>");
							}
							else
							{
								valueXmlString.append("<station_descr></station_descr>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							
							
						}
						else
							{
								valueXmlString.append("<sp_name></sp_name>");
								valueXmlString.append("<sh_name></sh_name>");
								valueXmlString.append("<addr1></addr1>");
								valueXmlString.append("<addr2></addr2>");
								valueXmlString.append("<addr3></addr3>");
								valueXmlString.append("<tele1></tele1>");
								valueXmlString.append("<tele2></tele2>");
								valueXmlString.append("<tele3></tele3>");
								valueXmlString.append("<pin></pin>");
								valueXmlString.append("<email_addr></email_addr>");
								valueXmlString.append("<state_code></state_code>");
								valueXmlString.append("<station_stan_code></station_stan_code>");
								valueXmlString.append("<state_descr></state_descr>");
								valueXmlString.append("<count_code></count_code>");
								valueXmlString.append("<station_descr></station_descr>");
							}
							if(pstmt != null)
							{
								pstmt.close();
							}
					}
					

					else if (currentColumn.trim().equals("state_code"))
					{

						if (columnValue != null)
						{
					
							sql ="SELECT A.COUNT_CODE COUNT_CODE,  B.CURR_CODE CURR_CODE, C.DESCR DESCR FROM  STATE A ,COUNTRY B, CURRENCY C WHERE  A.COUNT_CODE = B.COUNT_CODE (+) AND B.CURR_CODE = C.CURR_CODE  (+) AND A.STATE_CODE = '" +columnValue.trim()+"'";

					

						System.out.println("[SalesPersLEJB]sql=>"+sql);

						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							countCode = rs.getString("COUNT_CODE")==null?"":rs.getString("COUNT_CODE").trim();
							currCode= rs.getString("CURR_CODE")==null?"":rs.getString("CURR_CODE").trim();
							descr = rs.getString("DESCR")==null?"":rs.getString("DESCR").trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						}
						valueXmlString.append("<count_code><![CDATA["+checkNull(countCode)+"]]></count_code>");
						valueXmlString.append("<curr_code><![CDATA["+checkNull(currCode)+"]]></curr_code>");
						valueXmlString.append("<currency_descr><![CDATA["+checkNull(descr)+"]]></currency_descr>");

					}
					else if (currentColumn.trim().equals("curr_code"))
					{

						if (columnValue != null)
						{
							sql="SELECT DESCR FROM CURRENCY WHERE CURR_CODE = '" + columnValue.trim() + "'";

							System.out.println("[SalesPersLEJB]sql=>"+sql);

							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								descr = rs.getString("DESCR")==null?"":rs.getString("DESCR").trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else
						{
							descr = "";
						}
						valueXmlString.append("<currency_descr><![CDATA["+checkNull(descr)+"]]></currency_descr>");

					}
					else if (currentColumn.trim().equals("cr_term"))
					{

						if (columnValue != null)
						{
							sql="SELECT DESCR FROM CRTERM WHERE CR_TERM = '" + columnValue.trim() + "'";

							System.out.println("[SalesPersLEJB]sql=>"+sql);

							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								descr = rs.getString("DESCR")==null?"":rs.getString("DESCR").trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else
						{
							descr = "";
						}
						valueXmlString.append("<crterm_descr><![CDATA["+checkNull(descr)+"]]></crterm_descr>");
					}
					else if (currentColumn.trim().equals("contact_code"))
					{
						if (columnValue != null)
						{
							sql = "SELECT NAME, SH_NAME, CONT_PERS, CONT_PFX, ADDR1, ADDR2, CITY, PIN, STATE_CODE, "
         	 					+ " COUNT_CODE, TELE1, TELE2, TELE3, TELE_EXT, FAX "
							    + "	FROM CONTACT WHERE CONTACT_CODE = '" + columnValue.trim() + "'";

							System.out.println("[SalesPersLEJB]sql=>"+sql);

							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								name = rs.getString("NAME")==null?"":rs.getString("NAME").trim();
								shName = rs.getString("SH_NAME")==null?"":rs.getString("SH_NAME").trim();
								contPers = rs.getString("CONT_PERS")==null?"":rs.getString("CONT_PERS").trim();
								contPfx = rs.getString("CONT_PFX")==null?"":rs.getString("CONT_PFX").trim();
								addr1 = rs.getString("ADDR1")==null?"":rs.getString("ADDR1").trim();
								addr2 = rs.getString("ADDR2")==null?"":rs.getString("ADDR2").trim();
								city = rs.getString("CITY")==null?"":rs.getString("CITY").trim();
								pin = rs.getString("PIN")==null?"":rs.getString("PIN").trim();
								stateCode = rs.getString("STATE_CODE")==null?"":rs.getString("STATE_CODE").trim();
								countCode = rs.getString("COUNT_CODE")==null?"":rs.getString("COUNT_CODE").trim();
								tele1 = rs.getString("TELE1")==null?"":rs.getString("TELE1").trim();
								tele2 = rs.getString("TELE2")==null?"":rs.getString("TELE2").trim();
								tele3 = rs.getString("TELE3")==null?"":rs.getString("TELE3").trim();
								teleExt = rs.getString("TELE_EXT")==null?"":rs.getString("TELE_EXT").trim();
								fax = rs.getString("FAX")==null?"":rs.getString("FAX").trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						/*else
						{
							name = "";
							shName = "";
							contPers = "";
							contPfx = "";
							addr1 = "";
							addr2 = "";
							city = "";
							pin = "";
							stateCode = "";
							countCode = "";
							tele1 = "";
							tele2 = "";
							tele3 = "";
							teleExt = "";
							fax = "";
						}*/
						valueXmlString.append("<sp_name><![CDATA["+checkNull(name)+"]]></sp_name>");
						valueXmlString.append("<sh_name><![CDATA["+checkNull(shName)+"]]></sh_name>");
						valueXmlString.append("<cont_pers><![CDATA["+checkNull(contPers)+"]]></cont_pers>");
						valueXmlString.append("<cont_pfx><![CDATA["+checkNull(contPfx)+"]]></cont_pfx>");
						valueXmlString.append("<addr1><![CDATA["+checkNull(addr1)+"]]></addr1>");
						valueXmlString.append("<addr2><![CDATA["+checkNull(addr2)+"]]></addr2>");
						valueXmlString.append("<city><![CDATA["+checkNull(city)+"]]></city>");
						valueXmlString.append("<pin><![CDATA["+checkNull(pin)+"]]></pin>");
						valueXmlString.append("<state_code><![CDATA["+checkNull(stateCode)+"]]></state_code>");
						valueXmlString.append("<count_code><![CDATA["+checkNull(countCode)+"]]></count_code>");
						valueXmlString.append("<tele1><![CDATA["+checkNull(tele1)+"]]></tele1>");
						valueXmlString.append("<tele2><![CDATA["+checkNull(tele2)+"]]></tele2>");
						valueXmlString.append("<tele3><![CDATA["+checkNull(tele3)+"]]></tele3>");
						valueXmlString.append("<tele_ext><![CDATA["+checkNull(teleExt)+"]]></tele_ext>");
						valueXmlString.append("<fax><![CDATA["+checkNull(fax)+"]]></fax>");

						descr = getColumnValue("sales_pers",dom);
						if (descr == null || descr.trim().length() == 0  )
						{
							valueXmlString.append("<sales_pers><![CDATA["+columnValue.trim()+"]]></sales_pers>");
						}
					}
					else if (currentColumn.trim().equals("item_ser"))
					{
						if (columnValue != null)
						{
							sql="SELECT DESCR FROM ITEMSER WHERE ITEM_SER = '" + columnValue.trim() + "'";

							System.out.println("[SalesPersLEJB]sql=>"+sql);

							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								descr = rs.getString("DESCR")==null?"":rs.getString("DESCR").trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else
						{
							descr = "";
						}
						valueXmlString.append("<itemser_descr><![CDATA["+checkNull(descr)+"]]></itemser_descr>");
					}
					else if (currentColumn.trim().equals("tran_code"))
					{
						if (columnValue != null)
						{
							sql="SELECT TRAN_NAME FROM TRANSPORTER WHERE TRAN_CODE = '" + columnValue.trim() + "'";

							System.out.println("[SalesPersLEJB]sql=>"+sql);

							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								descr = rs.getString("TRAN_NAME")==null?"":rs.getString("TRAN_NAME").trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else
						{
							descr = "";
						}
						valueXmlString.append("<tran_name><![CDATA["+checkNull(descr)+"]]></tran_name>");
					}
					valueXmlString.append("</Detail1>\r\n");
					break;

			}//end of switch
			valueXmlString.append("</Root>\r\n");

		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					conn.close();
					conn = null;
					System.out.println("[SalesPersLEJB]connection 2 is closed......");
				}
			}catch(Exception d){d.printStackTrace();}
			System.out.println("[SalesPersLEJB] Connection is Closed");
		}
		System.out.println("valueXmlString:::::"+valueXmlString.toString());
		return valueXmlString.toString();
	}//END OF ITEMCHNGE
		private String checkNull( String input )
		{
			if ( input == null )
			{
				input = "";
			}
			return input;
		}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("Validation Start!!!!!!!!");

		try
		{

			System.out.println("xmlString:::"+xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : SalesPersLEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
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
		int cnt = 0;
		int ctr = 0;
		int cntNDC=0;
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
		E12GenericUtility e12GenericUtility = new E12GenericUtility();
		
				int currentFormNo = 0, count = 0, ct = 0 ,periodCnt=0;
		String accPeriodFr = "", siteCode = "",  keyFlag = "",prdCode="",itemCodeNDC="";
		String tranCode = "";
		
		String contactCode="",spType="",empCode="",status="", childNodeValue = "";
		String itemSer="",taxClass="",taxChap="",taxEnv="",priceList="",salesPers="",stateCode="",countCode="",crTerm="",currencyCode="",acctCodeAp="",acctCodeApAdv="",cctrCodeAp="",cctrCodeApAdv="";
		FinCommon finCommon = new FinCommon();
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(e12GenericUtility.getApplDateFormat());
			System.out.println("wfvaldata called!!!!! (SalesPersonIC) ");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
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
					System.out.println("childNodeName :"+childNodeName);

					if (childNodeName.equalsIgnoreCase("contact_code"))
					{
						contactCode = e12GenericUtility.getColumnValue("contact_code", dom);
						/* Comment BY Nasruddin START 22-SEP-16 
						if (contactCode == null || contactCode.trim().length() == 0)
						{
							errCode = "VTCONCDEMP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{ Comment BY Nasruddin  22-SEP-16  END*/
						if (contactCode != null && contactCode.trim().length() > 0)
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM CONTACT WHERE CONTACT_CODE = '" + contactCode.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode = "VMCONTCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					} 
					else if(childNodeName.equalsIgnoreCase("sp_type"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						spType = genericUtility.getColumnValue("sp_type", dom);
						System.out.println("spType :"+spType);
						if(spType == null || spType.trim().length() == 0)
						{	

							errCode = "VMEMTTYPE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					else if (childNodeName.equalsIgnoreCase("emp_code"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						empCode = genericUtility.getColumnValue("emp_code", dom);
						if(empCode != null && empCode.trim().length() > 0 )
						{
							sql=" SELECT STATUS FROM EMPLOYEE WHERE EMP_CODE = '" + empCode.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							errCode = null;
							if(rs.next())
							{
								status = rs.getString("STATUS");
								if ("S".equalsIgnoreCase(status) )
								{
									errCode = "VMSTATUS";
								}
							}
							else
							{
								errCode = "VMEMPCD1";
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(errCode != null && errCode.trim().length() > 0 )
							{

								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = genericUtility.getColumnValue("tran_code", dom);
						System.out.println("tranCode :"+tranCode);
						if(tranCode != null && tranCode.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM TRANSPORTER WHERE TRAN_CODE = '" + tranCode.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("item_ser"))
					{
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						//itmSer = genericUtility.getColumnValue("item_ser", dom);
						if(itemSer != null && itemSer.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM ITEMSER WHERE ITEM_SER = '" + itemSer.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode = "VTITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tax_class"))
					{
						//columnValue = getColumnValue(childNodeName.toLowerCase(),dom);
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						System.out.println("taxClass :"+taxClass);
						if(taxClass != null && taxClass.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM TAXCLASS WHERE TAX_CLASS = '" + taxClass.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{

								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tax_chap"))
					{
						//columnValue = getColumnValue(childNodeName.toLowerCase(),dom);
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						System.out.println("");
						if(taxChap != null && taxChap.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM TAXCHAP WHERE TAX_CHAP = '" + taxChap.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{

								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tax_env"))
					{
						//columnValue = getColumnValue(childNodeName.toLowerCase(),dom);
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						if(taxEnv != null && taxEnv.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM TAXENV WHERE TAX_ENV = '" + taxEnv.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{

								errCode = "VTTAXENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					/*Comment By Nasruddin 22-SEP-16 Start
					else if(childNodeName.equalsIgnoreCase("price_list"))
					{
						//columnValue = getColumnValue(childNodeName.toLowerCase(),dom);
						priceList = genericUtility.getColumnValue("price_list", dom);
						if(priceList != null && priceList.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM pricelist WHERE price_list = '" + priceList.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode = "VTINVPLIST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}Comment By Nasruddin 22-SEP-16 end*/
					// Changed By Nasruddin 22-SEP-16 Start
					//else if(childNodeName.equalsIgnoreCase("sales_pers"))
					else if(childNodeName.equalsIgnoreCase("sales_pers") && (("A").equals(editFlag)))
					{
						System.out.println("Validaion   for field "+childNodeName);
						salesPers = genericUtility.getColumnValue("sales_pers", dom);
						System.out.println("salesPers :"+salesPers);

						if(salesPers != null && salesPers.trim().length() > 0)
						{
							cnt = 0;
							sql = "SELECT COUNT(1) FROM SALES_PERS  WHERE SALES_PERS = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, salesPers);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt =	rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt > 0)
							{
								errCode = "VMPMKY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						/* 
						sql = "select key_flag from transetup where tran_window='w_slpers'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							keyFlag = rs.getString("key_flag");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("keyFlag :"+keyFlag);
						System.out.println("editFlag :"+editFlag);
						pstmt = null;
						if ("M".equalsIgnoreCase(keyFlag) && "A".equalsIgnoreCase(editFlag))
						{
							if(salesPers == null || salesPers.trim().length() == 0)
							{

								errCode = "VTSALPERCO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql="select count(*) as count from sales_pers where sales_pers = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, salesPers.trim());
								rs = pstmt.executeQuery();

								if(rs.next())
								{
									cnt = rs.getInt("count");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									System.out.println("Duplicate " + childNodeName.toLowerCase() + " entered [" + salesPers + "]");
									errCode = "VTDUSALPER";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}

							}


						}

						if("A".equalsIgnoreCase(keyFlag) && "A".equalsIgnoreCase(editFlag))
						{
							if(salesPers != null && salesPers.trim().length() > 0 )
							{
								sql="select count(*) as count from sales_pers where sales_pers = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, salesPers.trim());
								rs = pstmt.executeQuery();

								if(rs.next())
								{
									cnt = rs.getInt("count");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									System.out.println("Duplicate " + childNodeName.toLowerCase() + " entered [" + salesPers + "]");
									errCode = "VTDUSALPER";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}
					Changed By Nasruddin 22-SEP_-16 END*/
					}
					/* Comment By Nasruddin Start 22-SEP-16
					else if(childNodeName.equalsIgnoreCase("sp_type"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						spType = genericUtility.getColumnValue("sp_type", dom);
						System.out.println("spType :"+spType);
						if(spType == null || spType.trim().length() == 0)
						{	

							errCode = "VMEMTTYPE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}/* Comment By Nasruddin END 22-SEP-16 */
					else if(childNodeName.equalsIgnoreCase("state_code"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));
						System.out.println("stateCode :"+stateCode);
						if(stateCode.trim().length() == 0)
						{								

							sql="select count(*) as cnt from state where state_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stateCode);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode="VTSTATE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}


						}
					}
					else if(childNodeName.equalsIgnoreCase("count_code"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
						System.out.println("countCode :"+countCode);

						if(countCode.trim().length() == 0)
						{								

							sql="select count(*) as cnt from country where count_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countCode);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode="VTCONTCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					else if(childNodeName.equalsIgnoreCase("cr_term"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						System.out.println("crTerm :"+crTerm);

						/*Comment By Nasruddin Start 22-SEP-16
						 if(crTerm == null || crTerm.trim().length() == 0)
						{								
							errCode="VTCRTMEMP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{ Comment By Nasruddin END 22-SEP-16*/								

						sql="select count(*) as cnt from crterm where cr_term = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, crTerm);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt==0)
						{
							errCode="VTCRTERM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						//}
					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						currencyCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						System.out.println("currencyCode :"+currencyCode);
						/*Comment By Nasruddin Start 22-SEP-16
						if(currencyCode == null || currencyCode.trim().length() == 0)
						{								
							errCode	="VTCURREMP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{	 Comment By Nasruddin END 22-SEP-16*/							

						sql="select count(*) as cnt from currency where curr_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, currencyCode);
						rs = pstmt.executeQuery();

						if(rs.next())
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt==0)
						{
							errCode="VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//}
					}
					else if(childNodeName.equalsIgnoreCase("acct_code__ap"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						acctCodeAp = checkNull(genericUtility.getColumnValue("acct_code__ap", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						System.out.println("acctCodeAp :"+acctCodeAp);
						System.out.println("site_code :"+siteCode);
						if(acctCodeAp != null && acctCodeAp.trim().length() > 0 )
						{
							errCode = finCommon.isAcctCode(siteCode, acctCodeAp, "", conn);
						}
						/*else
						{
							errCode = "VMACCTCD1";
						}*/

						if(errCode != null && errCode.trim().length() > 0)
						{
							System.out.println("errCode :"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}		

					}
					else if(childNodeName.equalsIgnoreCase("acct_code__ap_adv"))
					{
						System.out.println("Validaion   for field "+childNodeName);
						acctCodeApAdv = checkNull(genericUtility.getColumnValue("acct_code__ap_adv", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						System.out.println("acctCodeAp :"+acctCodeAp);
						System.out.println("site_code :"+siteCode);
						if(acctCodeApAdv != null && acctCodeApAdv.trim().length() > 0 )
						{
							errCode = finCommon.isAcctCode(siteCode, acctCodeApAdv, "", conn);
						}
					/*	else
						{
							errCode = "VMACCTCD1";
						}*/

						if(errCode != null && errCode.trim().length() > 0)
						{
							System.out.println("errCode :"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	

					}
					else if(childNodeName.equalsIgnoreCase("cctr_code__ap"))
					{
						cctrCodeAp = genericUtility.getColumnValue("cctr_code__ap", dom); 
						acctCodeAp = genericUtility.getColumnValue("acct_code__ap", dom); 
						if(cctrCodeAp != null  && cctrCodeAp.trim().length() > 0)
						{
							errCode = finCommon.isCctrCode(acctCodeAp, cctrCodeAp, "", conn);
						}
						/*else
						{
							errCode = "VMCCTRCD1";
						}*/
						if(errCode != null && errCode.trim().length() > 0)
						{
							System.out.println("errCode :"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code__ap_adv"))
					{
						cctrCodeApAdv = genericUtility.getColumnValue("cctr_code__ap_adv", dom); 
						acctCodeApAdv = genericUtility.getColumnValue("acct_code__ap_adv", dom); 
						if(cctrCodeApAdv != null  && cctrCodeApAdv.trim().length() > 0)
						{
							errCode = finCommon.isCctrCode(acctCodeApAdv, cctrCodeApAdv, "", conn);
						}
						/*else
						{
							errCode = "VMCCTRCD1";
						}*/
						if(errCode != null && errCode.trim().length() > 0)
						{
							System.out.println("errCode :"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					/*Comment By Nasruddin 22-SEP-16 STARt
					else if(childNodeName.equalsIgnoreCase("site_code__pay"))   
					{
						siteCodePay = genericUtility.getColumnValue("site_code__pay", dom);
						System.out.println("siteCodePay :"+siteCodePay);
						if(siteCodePay != null && siteCodePay.trim().length() > 0 )
						{
							sql=" SELECT COUNT(1) AS COUNTER FROM site WHERE site_code = '" + siteCodePay.trim() + "'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt = rs.getInt("COUNTER");
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

					} Comment By Nasruddin 22-SEP-16 END*/

					else if(childNodeName.equalsIgnoreCase("rep_cont_chk_days") || childNodeName.equalsIgnoreCase("allwd_exp_prd") || childNodeName.equalsIgnoreCase("exp_darchk_days"))
					{
						childNodeValue = genericUtility.getColumnValue(childNodeName, dom);   //childNodeValue.trim().length() > 0
						if(childNodeValue == null || childNodeValue.trim().length() == 0 )   // changed by Pavan Rane On 1/JUN/2017
						{
							errCode = "VMNEGATIVE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}// end for
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
					System.out.println("errCode .........." + errCode);
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
			} 
			else
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
	
}