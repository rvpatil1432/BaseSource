/*
	indow Name : w_charge_back
*/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.text.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
//import javax.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import java.io.*;
import java.lang.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;	
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.annotation.*;
import javax.ejb.Stateless; // added for ejb3

//public class ChargeBackEJB implements SessionBean // commented for ejb3
@Stateless // added for ejb3
public class ChargeBack implements ChargeBackLocal, ChargeBackRemote // added for ejb3
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	CommonConstants commonConstants;
	//SessionContext cSessionContext; //for ejb3
	//  commented for ejb3
	@PostConstruct
	public void createCall() throws RemoteException	//, CreateException 
	{
		try
		{
			commonConstants  = new CommonConstants();
			System.out.println("Entering into ChargeBackEJB.............");
		}
		catch (Exception e)
		{

			System.out.println("Exception :ChargeBackEJB :ejbCreate :==>"+e);
			e.printStackTrace();
			//throw new CreateException();
		}
	}
	/*public void ejbCreate() throws RemoteException	//, CreateException 
	{
		try
		{
			commonConstants  = new CommonConstants();
			System.out.println("Entering into ChargeBackEJB.............");
		}
		catch (Exception e)
		{

			System.out.println("Exception :ChargeBackEJB :ejbCreate :==>"+e);
			e.printStackTrace();
			//throw new CreateException();
		}
	}*/
	/*public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}	
	public void sessionContextCall(SessionContext mSessionContext) 
	{
		this.cSessionContext = mSessionContext;
	}

	/*public void setSessionContext(SessionContext mSessionContext) 
	{
		this.cSessionContext = mSessionContext;
	}*/	
	public String replaceVal(String xmlString,String targetField,String formNo,String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;

		String valueXmlString = "";
		try
		{	
			try
			{
				dom = genericUtility.parseString(xmlString);
			}
			catch(Exception e)
			{
				System.out.println("Exception while parsing XMlString ] :==>\n"+e.getMessage());
			}
			if((targetField == null) || (targetField.trim().length() == 0))
			{
				dom = replaceAllFields(dom,targetField,formNo,xtraParams);	
			}
			else
			{
				dom = replaceField(dom,targetField,formNo,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ChargeBackEJB][replaceVal(String)] :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		valueXmlString = serializeDom(dom);
		System.out.println("XML String after modification ||||||||||||||| ---> "+valueXmlString);
        return valueXmlString; 
	}
	private Document replaceField(Document dom,String targetField,String currentFormNo,String xtraParams)
	{
		StringBuffer xmlString = new StringBuffer();
		StringBuffer xmlString2 = new StringBuffer();
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "",sql2 ="";
		NodeList parentNodeList = null,childNodeList = null,parentNodeList1 = null,childNodeList1 = null;
		Node parentNode = null,parentNode1 = null,childNode = null,child1 = null,child2 = null;
		int parentNodeListLength = 0,childNodeListLength = 0,ctr = 0,formNo = 0;
		String childNodeName = ""; 
		String tranDate = "",loginSite = "",empCode = "",userId = "",sysDate = "",currCode = "",priceList = "",custCode = "";
		String tranId = "",lineNo = "",sorderDate = "",effDate = "",custCodeBil = "",custNameSh = "",custCodeSh  = "";
		String siteDescr = "",finEntity = "",testPrd = "",pordNo = "",porderDate = "",intVendorNo = "";
		String custName = "",pNameBat = "",custCodeShip = "",vendorNo = "";
		String buyersPrCode = "",contractNo = "",itemDescr = "",unit = "",itemCode = "",squantity = "";
		String srateSell = "",srateCtr = "",srateDiff = "",custCodeEnd = "",lineNoCtr = "";
		double exchRate = 1,quantity = 0.0,rateSell = 0.0,rateCtr = 0.0,rateDiff = 0.0,amount = 0.0;
		
		try
		{
			ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
		}
		catch(Exception e)
		{
			System.out.println("Exception while creating connection......");
		}
		try
		{
			if(currentFormNo != null && currentFormNo.trim().length()>0)
			{
				formNo = Integer.parseInt(currentFormNo);
			}	
			
			parentNodeList = dom.getElementsByTagName("Detail" + formNo);
			System.out.println("formNo ::::::::::- "+formNo);
			switch (formNo)
			{
				case 1:
				{
					try
					{
						xmlString.append("<?xml version='1.0' encoding='UTF-8'?>\n");
						xmlString.append("<Detail1>");	
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						ctr = 0;
						childNodeListLength = childNodeList.getLength();
						//System.out.println("childNodeListLength----"+childNodeListLength);
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{
							childNode = childNodeList.item(childRow);
							childNodeName = childNode.getNodeName();
							if(targetField.toLowerCase().equals(childNodeName.toLowerCase()))
							{
								if(targetField.equalsIgnoreCase("int_vendor_no"))
								{
									if (childNodeName.equals("int_vendor_no"))
									{
										intVendorNo = (childNode.getFirstChild().getNodeValue()).trim();
										childNode.getFirstChild().setNodeValue(intVendorNo.trim());
										try
										{
											sql = "SELECT CUST_NAME,PRICE_LIST,CUST_CODE,CUST_CODE__BIL,CURR_CODE "
												+ "FROM CUSTOMER WHERE EDI_ADDR = ? ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,intVendorNo.trim());
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												custName = rs.getString(1);
												priceList = rs.getString(2);
												custCode = rs.getString(3);
												custCodeBil = rs.getString(4);
												currCode = rs.getString(5);
											}
											rs.close();
											pstmt.close();
											System.out.println("int_vendor_no :::::::::::::::- "+intVendorNo);
											xmlString.append("<cust_code><![CDATA[").append(custCode).append("]]></cust_code>");
											xmlString.append("<price_list><![CDATA[").append(priceList).append("]]></price_list>");
											xmlString.append("<cust_code__credit><![CDATA[").append(custCodeBil).append("]]></cust_code__credit>");
										}
										catch(SQLException se)
										{
											System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql +se.getMessage());
										}	
										catch(Exception ex)
										{
											System.out.println("Exception []::"+ sql +ex.getMessage());
											ex.printStackTrace();
										}
									}
								}
								else if(targetField.equalsIgnoreCase("cust_code__ship"))
								{
									if (childNodeName.equals("cust_code__ship"))
									{
										custCodeShip = (childNode.getFirstChild().getNodeValue()).trim();
										childNode.getFirstChild().setNodeValue(custCodeShip.trim());
										try
										{
											sql = "SELECT CUST_CODE ,CUST_NAME FROM CUSTOMER WHERE EDI_ADDR = ? ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,custCodeShip.trim());
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												custCodeSh = rs.getString(1);
												custNameSh = rs.getString(2);
											}
											rs.close();
											pstmt.close();
											System.out.println("cust_code__ship :::::::::::::::- "+custCodeSh);
											System.out.println("cust_name__ship :::::::::::::::- "+custNameSh);
											xmlString.append("<cust_name__ship><![CDATA[").append(custNameSh.trim()).append("]]></cust_name__ship>");
											childNode.getFirstChild().setNodeValue(custCodeSh.trim());
										}
										catch(SQLException se)
										{
											System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql +se.getMessage());
										}	
										catch(Exception ex)
										{
											System.out.println("Exception []::"+ sql +ex.getMessage());
											ex.printStackTrace();
										}	
									}
								}
							}//if
						}//for
						try
						{
							java.sql.Date currDate = new java.sql.Date(System.currentTimeMillis());
							tranDate = currDate.toString();
							
							Object date = null;
							SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
							date = sdf.parse(tranDate);
							SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());	
							tranDate = sdf1.format(date).toString();
							
							java.sql.Timestamp tSysDate = new java.sql.Timestamp(System.currentTimeMillis());
							date = sdf.parse(tSysDate.toString());
							sysDate = sdf1.format(date).toString();
							
							parentNodeList = dom.getElementsByTagName("tran_date");
							parentNodeListLength = parentNodeList.getLength(); 
							if(parentNodeListLength == 0)
							{
								xmlString.append("<tran_date><![CDATA[").append(tranDate).append("]]></tran_date>");
								System.out.println("tran_date :::::::::::::::- "+tranDate);
							}
							parentNodeList = dom.getElementsByTagName("site_code");
							parentNodeListLength = parentNodeList.getLength(); 
							if(parentNodeListLength == 0)
							{
								try
								{
									sql = "SELECT FIN_ENTITY FROM SITE WHERE SITE_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,loginSite.trim());
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										finEntity = rs.getString(1);
									}
									rs.close();
									pstmt.close();
								}
								catch(SQLException se)
								{
									System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql +se.getMessage());
								}
								try
								{
									sql = "SELECT CURR_CODE FROM FINENT WHERE FIN_ENTITY = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,finEntity);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										currCode = rs.getString(1);
									}
									rs.close();
									pstmt.close();
								}
								catch(SQLException se)
								{
									System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql +se.getMessage());
								}
								exchRate = getDailyExchRateSellBuy(currCode.trim(),"",loginSite.trim(),tranDate.trim(),"S");
								xmlString.append("<site_code><![CDATA[").append(loginSite).append("]]></site_code>");
								xmlString.append("<curr_code><![CDATA[").append(currCode).append("]]></curr_code>");
								xmlString.append("<exch_rate><![CDATA[").append(exchRate).append("]]></exch_rate>");
								System.out.println("site_code :::::::::::::::- "+loginSite);
								System.out.println("curr_code :::::::::::::::- "+currCode);
								System.out.println("exch_rate :::::::::::::::- "+exchRate);
							}
							parentNodeList = dom.getElementsByTagName("emp_code__aprv");
							parentNodeListLength = parentNodeList.getLength(); 
							if(parentNodeListLength == 0)
							{
								xmlString.append("<emp_code__aprv><![CDATA[").append(empCode.trim()).append("]]></emp_code__aprv>");
								System.out.println("emp_code__aprv :::::::::::::::- "+empCode);
							}
							parentNodeList = dom.getElementsByTagName("chg_user");
							parentNodeListLength = parentNodeList.getLength(); 
							if(parentNodeListLength == 0)
							{
								xmlString.append("<chg_user><![CDATA[").append(userId.trim()).append("]]></chg_user>");
								System.out.println("chg_user :::::::::::::::- "+userId);
							}
							parentNodeList = dom.getElementsByTagName("chg_date");
							parentNodeListLength = parentNodeList.getLength(); 
							//System.out.println("parentNodeListLength ::::::::::-->"+parentNodeListLength);
							if(parentNodeListLength == 0)
							{
								xmlString.append("<chg_date><![CDATA[").append(sysDate).append("]]></chg_date>");
								System.out.println("chg_date :::::::::::::::- "+sysDate);
							}    
							     
							xmlString.append("</Detail1>");
							
							DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							DocumentBuilder db = dbf.newDocumentBuilder();
							OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, "UTF-8");
							ByteArrayInputStream baos = new ByteArrayInputStream(xmlString.toString().getBytes());
							Document dom1 = db.parse(baos); 
							
							parentNodeList = dom1.getElementsByTagName("Detail1");
							parentNode = parentNodeList.item(0);
							childNodeList = parentNode.getChildNodes();
							int clength = childNodeList.getLength();
							for (int childRow = 0; childRow < clength; childRow++)
							{
								child1 =  childNodeList.item(childRow);
								child2 = dom.importNode(child1,true); 
								dom.getElementsByTagName("Detail1").item(0).appendChild(child2); //Appending to Dom
							}
						}//try
						catch(Exception ex)
						{
							System.out.println("Exception []::"+ex.getMessage());
							ex.printStackTrace();
						}
					}//try
					catch(Exception ex)
					{
						System.out.println("Exception [while setting values of detail1]::"+ex.getMessage());
						ex.printStackTrace();
					}
				}//case 1
				break;
				case 2:
				{
					try
					{	
						parentNodeListLength = parentNodeList.getLength(); 
						
						for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
						{
							xmlString2 = new StringBuffer();
							DocumentBuilderFactory dbf1 = null;
							DocumentBuilder db1 = null;
							OutputStreamWriter errorWriter1 = null;
							ByteArrayInputStream baos1 = null;
							Document dom2 = null;
							xmlString2.append("<?xml version='1.0' encoding='UTF-8'?>\n");
							xmlString2.append("<Detail2>");
						
							parentNode = parentNodeList.item(selectedRow);
							childNodeList = parentNode.getChildNodes();
							childNodeListLength = childNodeList.getLength();
							//System.out.println("ChildNodeListLength..........."+ childNodeListLength);
							for (int childRow = 0; childRow < childNodeListLength; childRow++)
							{
								childNode = childNodeList.item(childRow);
								childNodeName = childNode.getNodeName();
								if (childNodeName.equals("quantity"))
								{
									squantity = (childNode.getFirstChild().getNodeValue()).trim();
									quantity = Double.parseDouble(squantity);
									squantity = Double.toString(quantity);
									childNode.getFirstChild().setNodeValue(squantity);
								}
								if (childNodeName.equals("rate__sell"))
								{
									srateSell = (childNode.getFirstChild().getNodeValue()).trim();
									rateSell = Double.parseDouble(srateSell);
									srateSell = Double.toString(rateSell);
									childNode.getFirstChild().setNodeValue(srateSell);
								}
								if (childNodeName.equals("rate__contr"))
								{
									srateCtr = (childNode.getFirstChild().getNodeValue()).trim();
									rateCtr = Double.parseDouble(srateCtr);
									srateCtr = Double.toString(rateCtr);
									childNode.getFirstChild().setNodeValue(srateCtr);
								}
								if(targetField.toLowerCase().equals(childNodeName.toLowerCase()))
								{
									if(targetField.equalsIgnoreCase("rate__diff"))
									{
										if (childNodeName.equals("rate__diff"))
										{
											srateDiff = (childNode.getFirstChild().getNodeValue()).trim();
											rateDiff = Double.parseDouble(srateDiff);
											rateDiff = (rateSell - rateCtr);
											srateDiff = Double.toString(rateDiff);
											
											NumberFormat nf=NumberFormat.getInstance();
											nf.setMinimumFractionDigits(3);
											
											Double  RateDiff = new Double (srateDiff);
											srateDiff = nf.format(RateDiff);
											childNode.getFirstChild().setNodeValue(srateDiff);
											
											amount = (quantity * rateDiff);//calculating the amount
											String samount = Double.toString(amount);
											Double  Amount = new Double (samount);
											samount = nf.format(Amount);
											amount = Double.parseDouble(samount);
										}	
										xmlString2.append("<amount><![CDATA[").append(amount).append("]]></amount>");
										xmlString2.append("<net_amt><![CDATA[").append(amount).append("]]></net_amt>");
									}
							
									else if(targetField.equalsIgnoreCase("cust_code__end"))
									{
										if (childNodeName.equals("cust_code__end"))
										{
											custCodeEnd = (childNode.getFirstChild().getNodeValue()).trim();
											try
											{
												sql = "SELECT CUST_CODE FROM CUSTOMER WHERE EDI_ADDR = ? "; 
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1,custCodeEnd);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													custCodeEnd = rs.getString(1);
												}
												rs.close();
												pstmt.close();
												
											}
											catch(SQLException se)
											{
												System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql +se.getMessage());
											}
											childNode.getFirstChild().setNodeValue(custCodeEnd);
										}
									}
									else if(targetField.equalsIgnoreCase("buyers_prod_code"))
									{
										if (childNodeName.equals("buyers_prod_code"))
										{
											buyersPrCode = (childNode.getFirstChild().getNodeValue()).trim();
											childNode.getFirstChild().setNodeValue(buyersPrCode);		
										}
										try
										{
											sql = "SELECT DESCR, UNIT, ITEM_CODE FROM ITEM WHERE ITEM_CODE__NDC = '"+buyersPrCode+"' ";
											stmt = conn.createStatement();
											rs = stmt.executeQuery(sql);
											if (rs.next())
											{
												itemDescr = rs.getString(1);
												unit = rs.getString(2);
												itemCode = rs.getString(3); 
												try
												{
													sql2 = "SELECT SCONTRACTDET.LINE_NO FROM SCONTRACTDET "
														 + "WHERE ( SCONTRACTDET.CONTRACT_NO = ? ) "
														 + "AND ( SCONTRACTDET.ITEM_CODE = ? ) "
														 + "AND ( SCONTRACTDET.SITE_CODE = ? )";
													pstmt1 = conn.prepareStatement(sql2);
													pstmt1.setString(1,contractNo);
													pstmt1.setString(2,itemCode);
													pstmt1.setString(3,loginSite);
													rs1 = pstmt1.executeQuery();
													if(rs1.next())
													{
														lineNoCtr = rs1.getString(1);
													}
													rs1.close();
													pstmt1.close();
												}
												catch(SQLException se)
												{
													System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql2 +se.getMessage());
												}
											}
											rs.close();
											stmt.close();
											xmlString2.append("<item_code><![CDATA[").append(itemCode).append("]]></item_code>");
											xmlString2.append("<line_no__contr><![CDATA[").append(lineNoCtr).append("]]></line_no__contr>");
										}
										catch(SQLException se)
										{
											System.out.println("SQLException []:[ChargeBackEJB][Excuting Query Failed]" + sql +se.getMessage());
										}	
										catch(Exception ex)
										{
											System.out.println("Exception []::"+ex.getMessage());
											ex.printStackTrace();
										}
									}//else
								}//if
							}//inner for
							try
							{
								xmlString2.append("</Detail2>");
					
								dbf1 = DocumentBuilderFactory.newInstance();
								db1 = dbf1.newDocumentBuilder();
								errorWriter1 = new OutputStreamWriter(System.err, "UTF-8");
								baos1 = new ByteArrayInputStream(xmlString2.toString().getBytes());
								dom2 = db1.parse(baos1);
			
								parentNodeList1 = dom2.getElementsByTagName("Detail2");
								parentNode1 = parentNodeList1.item(0);
								childNodeList1 = parentNode1.getChildNodes();
								int clength = childNodeList1.getLength();
								for (int childRow = 0; childRow < clength; childRow++)
								{
									child1 =  childNodeList1.item(childRow);
									child2 = dom.importNode(child1,true); //Importing Node from different Dom
									dom.getElementsByTagName("Detail2").item(selectedRow).appendChild(child2);//Appending to Dom
								}
							}
							catch(Exception ex)
							{
								System.out.println("Exception [while creating dom of detail2]::"+ex.getMessage());
								ex.printStackTrace();
							}
						}//outer for
					}//try
					catch(Exception ex)
					{
						System.out.println("Exception [while setting values of detail2]::"+ex.getMessage());
						ex.printStackTrace();
					}	
				}//case 2
				break;
			}//switch()
		}//outer try
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception s){}
		}
		return dom;
	}//replaceField()	
	
	private Document replaceAllFields(Document dom,String targetField,String formNo,String xtraParams)
	{
		try
		{
			System.out.println("INSIDE  replaceAllFields() :::::::::::::::- ");
			dom = replaceField(dom,"int_vendor_no","1",xtraParams);	
			dom = replaceField(dom,"cust_code__ship","1",xtraParams);
			dom = replaceField(dom,"rate__diff","2",xtraParams);
			dom = replaceField(dom,"cust_code__end","2",xtraParams);
			dom = replaceField(dom,"buyers_prod_code","2",xtraParams);	
			
		}//try
		catch(Exception ex)
		{
			System.out.println("Exception [replaceAllFields()]::"+ex.getMessage());
			ex.printStackTrace();
		}	
		return dom;
	}//replaceAllFields()

	private String serializeDom(Node dom)
	{
		String retString = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
			retString = out.toString();
			out.flush();
			out.close();
			out = null;	
		}
		catch (Exception e)
		{
			System.out.println("Exception : In : serializeDom :"+e);
		}
		return retString;
	}
	
	private double getDailyExchRateSellBuy(String as_curr_code,String as_curr_code__to,String as_login_site,String as_tran_date,String as_type)
	{
		String ls_fin_entity ="",ls_curr_code__to = "",var_value = "";
		double lc_exch_rate = 0.00;
		String sql = "";
		Connection conn = null;
		Statement stmt;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		if (as_curr_code.equalsIgnoreCase(as_curr_code__to))
		{
			return 1.00;	
		}
		if (as_curr_code__to.equals(""))
		{
			try
			{
				ConnDriver connDriver = new ConnDriver();
				conn = connDriver.getConnectDB("DriverITM");
				//conn = getConnection();
				stmt = conn.createStatement();
				sql = "SELECT FIN_ENTITY FROM SITE WHERE SITE_CODE ='"+as_login_site+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					ls_fin_entity = rs.getString(1);
					sql = "SELECT CURR_CODE FROM FINENT WHERE FIN_ENTITY = '"+ls_fin_entity+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						ls_curr_code__to = rs.getString(1);
					}

				}
				rs.close();
				stmt.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception :: [1][getDailyExchRateSellBuy] ::"+e);
			}

		}
		else
		{
			ls_curr_code__to = as_curr_code__to;
		}
		if (as_type.equals("B"))
		{
			try
			{
				sql = "SELECT EXCH_RATE__BUY FROM DAILY_EXCH_RATE_SELL_BUY WHERE CURR_CODE = ? AND CURR_CODE__TO = ? AND ? BETWEEN FROM_DATE AND TO_DATE";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,as_curr_code);
				pstmt.setString(2,ls_curr_code__to);
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(as_tran_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lc_exch_rate = rs.getDouble(1);
					if (lc_exch_rate == 0)
					{
						sql = "SELECT EXCH_RATE	FROM DAILY_EXCH_RATE_SELL_BUY WHERE CURR_CODE = ? AND CURR_CODE__TO = ? AND ? BETWEEN FROM_DATE AND TO_DATE";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_curr_code__to);
						pstmt.setString(2,as_curr_code);
						pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(as_tran_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lc_exch_rate = rs.getDouble(1);
							if (lc_exch_rate != 0)
							{
								lc_exch_rate = (1 / lc_exch_rate);
							}
						}							
					}
				}
				rs.close();
				pstmt.close();
			}
			catch(SQLException e)
			{
				System.out.println("Exception :: [2][getDailyExchRateSellBuy]"+e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception [2a]::"+ex.getMessage());
				ex.printStackTrace();
			}
		}
		else if (as_type.equals("S"))
		{
			try
			{	
				sql = "SELECT EXCH_RATE__SELL FROM DAILY_EXCH_RATE_SELL_BUY WHERE CURR_CODE = ? AND CURR_CODE__TO = ? AND ? BETWEEN FROM_DATE AND TO_DATE";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,as_curr_code);
				pstmt.setString(2,ls_curr_code__to);
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(as_tran_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lc_exch_rate = rs.getDouble(1);
					if (lc_exch_rate == 0)
					{
						sql = "SELECT EXCH_RATE FROM DAILY_EXCH_RATE_SELL_BUY WHERE CURR_CODE = ? AND CURR_CODE__TO = ? AND ? BETWEEN FROM_DATE AND TO_DATE";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_curr_code__to);
						pstmt.setString(2,as_curr_code);
						pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(as_tran_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lc_exch_rate = rs.getDouble(1);
							if (lc_exch_rate != 0)
							{
								lc_exch_rate = (1 / lc_exch_rate);
							}
						}

					}
				}
				rs.close();
				pstmt.close();
			}
			catch(SQLException e)
			{
				System.out.println("Exception :: [3][getDailyExchRateSellBuy][type 'S']"+e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception [3a]::"+ex.getMessage());
				ex.printStackTrace();
			}
		}
		if (lc_exch_rate == 0)
		{
			try
			{
				sql = "SELECT EXCH_RATE FROM DAILY_EXCH_RATE_SELL_BUY WHERE CURR_CODE = ? AND CURR_CODE__TO = ? AND ? BETWEEN FROM_DATE AND TO_DATE";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,as_curr_code);
				pstmt.setString(2,ls_curr_code__to);
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(as_tran_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lc_exch_rate = rs.getDouble(1);
					if (lc_exch_rate == 0)
					{
						sql = "SELECT EXCH_RATE FROM DAILY_EXCH_RATE_SELL_BUY WHERE CURR_CODE = ? AND CURR_CODE__TO = ? AND ? BETWEEN FROM_DATE AND TO_DATE";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_curr_code__to);
						pstmt.setString(2,as_curr_code);
						pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(as_tran_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lc_exch_rate = rs.getDouble(1);
							if (lc_exch_rate != 0)
							{
								lc_exch_rate = (1 / lc_exch_rate);
							}
						}
					}
				}
				rs.close();
				pstmt.close();
			}
			catch(SQLException e)
			{
				System.out.println("Exception ::[4][getDailyExchRateSellBuy]"+e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception [4a]::"+ex.getMessage());
				ex.printStackTrace();
			}
	   	}
		if (lc_exch_rate == 0)
		{
			try
			{
				stmt = conn.createStatement();
				sql = "SELECT RTRIM(CASE WHEN VAR_VALUE IS NULL THEN 'Y' ELSE VAR_VALUE END) FROM FINPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'EXCRT_CURR'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					var_value = rs.getString(1);
					if(var_value.equals("Y"))
					{	
						sql ="SELECT STD_EXRT FROM CURRENCY WHERE CURR_CODE = '"+as_curr_code+"'";	
						System.out.println("Executing SQL ::"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							lc_exch_rate = rs.getDouble(1);
						}					
					}
					else
					{
						return 0.00;
					}
				}
				else
				{
					sql ="SELECT STD_EXRT FROM CURRENCY WHERE CURR_CODE = '"+as_curr_code+"'";	
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						lc_exch_rate = rs.getDouble(1);
					}
				}
				rs.close();
				stmt.close();
			}
			catch(SQLException e)
			{
				System.out.println("Exception [5][getDailyExchRateSellBuy]"+e);
			}
			catch(Exception ex)
			{
				System.out.println("Exception [5a]::"+ex.getMessage());
				ex.printStackTrace();
			}
		}
		try
		{
			conn.close();
			conn = null;
		}
		catch(Exception se){}
		//System.out.println("Exchange Rate is ......"+lc_exch_rate);
		return lc_exch_rate; 
	}
}//class
		
								
						
							
							
						
						
						
						
						
						
						