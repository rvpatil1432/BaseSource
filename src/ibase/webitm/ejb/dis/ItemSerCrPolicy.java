/********************************************************
	Title : Item-Series Credit Policy EJB
	Date  : 10 - FEB - 2014
	Author: Sandeep Kumbhar

 ********************************************************/
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
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.ejb.Stateless;

@Stateless
public class ItemSerCrPolicy extends ValidatorEJB implements ItemSerCrPolicyLocal, ItemSerCrPolicyRemote {

	
    public ItemSerCrPolicy() {
	System.out.println("^^^^^^^ inside ItemSerCrPolicy ^^^^^^^");
    }

    public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";
	
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";
		
		try
		{
			System.out.println("xmlString ..> " + xmlString);
			System.out.println("xmlString1 ..> " + xmlString1);
			System.out.println("xmlString2 ..> " + xmlString2);

			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			if (objContext != null && Integer.parseInt(objContext) == 1) 
			{
				parentNodeList = dom2.getElementsByTagName("Header0");
				parentNode = parentNodeList.item(1);
				childNodeList = parentNode.getChildNodes();
				for (int x = 0; x < childNodeList.getLength(); x++) 
				{
					childNode = childNodeList.item(x);
					childNodeName = childNode.getNodeName();
					System.out.println("childnodename ..> " + childNodeName);
					if (childNodeName.equalsIgnoreCase("Detail1")) 
					{
						errString = wfValData(dom, dom1, dom2, "1", editFlag, xtraParams);
						if (errString != null && errString.trim().length() > 0)
							break;
					} 
					else if (childNodeName.equalsIgnoreCase("Detail2"))
					{
						errString = wfValData(dom, dom1, dom2, "2", editFlag, xtraParams);
						break;
					}
				}
			}
			else 
			{
				errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			}
		} catch (Exception e) {
		    System.out.println("Exception : Inside ItemSerCrPolicy wfValData Method ..> " + e.getMessage());
		    throw new ITMException(e);
		}
		return (errString);
    }

    public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
    {
    	System.out.println("inside wfValData ====================");
    	//GenericUtility genericUtility;
    	NodeList parentNodeList = null;
    	NodeList childNodeList = null;
    	Node parentNode = null;
    	Node childNode = null;
    	String childNodeName = null;
    	String errString = "";
    	String errCode = "";
    	Connection conn = null;
    	PreparedStatement pStmt = null;
    	ResultSet rs = null;
    	String sql = "";
    	String userId = "";
    	Date dateOfBirth;
    	E12GenericUtility genericUtility= new  E12GenericUtility();
    	int ctr = 0, currentFormNo = 0, childNodeListLength = 0, cnt = 0;
    	String itemSeries = "", crPolicy = "", policyType = "", descr = ""; 
    	String policySql = "", policyResult = "", policyInput = "", policyCond = "", title = "";
    	try {
    		ConnDriver connDriver = new ConnDriver();
    		//Changes and Commented By Bhushan on 09-06-2016 :START
    		//conn = connDriver.getConnectDB("DriverITM");
    		conn = getConnection();
    		//Changes and Commented By Bhushan on 09-06-2016 :END 
    		conn.setAutoCommit(false);
    		connDriver = null;

    		userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
    		//genericUtility = GenericUtility.getInstance();
    	//	simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());

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
    				childNodeName = childNodeName == null ? "" : childNodeName.trim();
    				// Changed By Nasruddin 22-SEP-16 START
    				if (childNodeName.equalsIgnoreCase("cr_policy")) 
    				{
    					crPolicy = genericUtility.getColumnValue("cr_policy", dom);
    					itemSeries = genericUtility.getColumnValue("item_ser", dom);
    					
    					if( crPolicy == null || crPolicy.trim().length() == 0)
    					{
    						errCode = "VMICP";
    						errString = getErrorString("cr_policy", errCode, userId);
    						break;
    					}
    					else
    					{
    						cnt = 0;
    						sql = "SELECT COUNT(1) FROM ITEMSER_CR_POLICY 	WHERE ITEM_SER = ? 	AND	CR_POLICY = ?";
    						pStmt = conn.prepareStatement(sql);
    						pStmt.setString(1, itemSeries);
    						pStmt.setString(2, crPolicy);
    						rs = pStmt.executeQuery();
    						if (rs.next())
    						{
    							cnt = rs.getInt(1);
    						}
    						rs.close();
    						rs = null;
    						pStmt.close();
    						pStmt = null;
    						if ((cnt > 1)) 
    						{
    							errCode = "VMUCP";
    							errString = getErrorString("cr_policy", errCode, userId);
    							break;
    						}
    					}
    					/*
    					policyType = genericUtility.getColumnValue("policy_type", dom);
    					crPolicy = crPolicy == null ? "" : crPolicy.trim();
    					itemSeries = itemSeries == null ? "" : itemSeries.trim();
    					policyType = policyType == null ? "" : policyType.trim();

    					if (crPolicy.length() == 0) 
    					{
    						errCode = "VMICP";
    						errString = getErrorString("cr_policy", errCode, userId);
    						break;
    					} 
    					else
    					{
    						sql = "select nvl(count(1),0) from itemser_cr_policy where item_ser = ? and cr_policy = ?";
    						pStmt = conn.prepareStatement(sql);
    						pStmt.setString(1, itemSeries);
    						pStmt.setString(2, crPolicy);
    						rs = pStmt.executeQuery();
    						if (rs.next())
    						{
    							cnt = rs.getInt(1);
    						}
    						rs.close();
    						rs = null;
    						pStmt.close();
    						pStmt = null;
    						if ((cnt > 1) && ("E".equalsIgnoreCase(editFlag))) 
    						{
    							errCode = "VMUCP";
    							errString = getErrorString("cr_policy", errCode, userId);
    							break;
    						}
    						else if ((cnt > 0) && (!"E".equalsIgnoreCase(editFlag))) 
    						{
    							errCode = "VMUCP";
    							errString = getErrorString("cr_policy", errCode, userId);
    							break;
    						}
    						sql = null; 
    						sql= "";
    						cnt = 0;
    						sql = "select nvl(count(1),0) from gencodes where fld_name = ? and fld_value = ?";
    						pStmt = conn.prepareStatement(sql);
    						pStmt.setString(1, "CR_POLICY");
    						pStmt.setString(2, crPolicy);
    						rs = pStmt.executeQuery();
    						if (rs.next()) 
    						{
    							cnt = rs.getInt(1);
    						}
    						rs.close();
    						rs = null;
    						pStmt.close();
    						pStmt = null;
    						Comment By Nasruddin Start [20-SEP=-16] START
    						if("U".equalsIgnoreCase(policyType) && cnt > 0)
		    				{
		    					errCode = "VISCRPEGC";
		    					errString = getErrorString("policy_type", errCode, userId);
		    					break;
		    				}
		    				else if ("S".equalsIgnoreCase(policyType) && cnt <= 0) 
		    				{
		    					errCode = "VISCRPNEGC";
		    					errString = getErrorString("cr_policy", errCode, userId);
		    					break;
		    				}
    					}
    					Comment By Nasruddin Start [20-SEP=-16] END*/
    				
    				} 
    				/* Comment By Nasruddin 20-SEP-16 START
			    else if (childNodeName.equalsIgnoreCase("item_ser")) 
			    {
					itemSeries = genericUtility.getColumnValue("item_ser", dom);
					itemSeries = itemSeries == null ? "" : itemSeries.trim();

					if (itemSeries.length() > 0) 
					{
					    sql = "select nvl(count(1),0) from itemser where item_ser = ?";
					    pStmt = conn.prepareStatement(sql);
					    pStmt.setString(1, itemSeries);
					    rs = pStmt.executeQuery();
					    if (rs.next()) 
					    {
					    	cnt = rs.getInt(1);
					    }
					    rs.close();
					    rs = null;
					    pStmt.close();
					    pStmt = null;
					    if (cnt == 0) 
					    {
						    errCode = "VTITEMSER1";
						    errString = getErrorString("item_ser", errCode, userId);
						    break;
						}
					} 
					else 
					{
						errCode = "VTITEMSER5";
					    errString = getErrorString("item_ser", errCode, userId);
					    break;
					}
			    } 
			    else if (childNodeName.equalsIgnoreCase("title")) 
			    {
			    	title = genericUtility.getColumnValue("title", dom);
			    	title = title == null ? "" : title.trim();

					if (title.length() == 0) 
					{
					    errCode = "VISCRPTLNL";
					    errString = getErrorString("title", errCode, userId);
					    break;
					}
			    } 
			    else if (childNodeName.equalsIgnoreCase("descr")) 
			    {
					crPolicy = genericUtility.getColumnValue("cr_policy", dom);
					descr = genericUtility.getColumnValue("descr", dom);
					policyType = genericUtility.getColumnValue("policy_type", dom);
					crPolicy = crPolicy == null ? "" : crPolicy.trim();
					descr = descr == null ? "" : descr.trim();
					policyType = policyType == null ? "" : policyType.trim();

					if (descr.length() == 0) 
					{
					    errCode = "DESCBLK";
					    errString = getErrorString("descr", errCode, userId);
					    break;
					}

					else 
					{
						cnt = 0;
						if("S".equalsIgnoreCase(policyType))
					    {
							sql = "select nvl(count(1),0) from gencodes where fld_name = ? and fld_value = ? and descr = ? ";
						    pStmt = conn.prepareStatement(sql);
						    pStmt.setString(1, "CR_POLICY");
						    pStmt.setString(2, crPolicy);
						    pStmt.setString(3, descr);
						    rs = pStmt.executeQuery();
						    if (rs.next()) 
						    {
						    	cnt = rs.getInt(1);
						    }
						    rs.close();
						    rs = null;
						    pStmt.close();
						    pStmt = null;

						    if (cnt == 0)
						    {
							    errCode = "VISCRPDRM";
							    errString = getErrorString("descr", errCode, userId);
							    break;
							}
					    }
					}
			    } 
			    else if (childNodeName.equalsIgnoreCase("policy_sql")) 
			    {
			    	policySql = genericUtility.getColumnValue("policy_sql", dom);
					policySql = policySql == null ? "" : policySql.trim();

					if (policySql.length() != 0)
					{
						policyResult = genericUtility.getColumnValue("policy_result", dom);
						policyInput = genericUtility.getColumnValue("policy_input", dom);
						policyCond = genericUtility.getColumnValue("policy_condition", dom);
						policyResult = policyResult == null ? "" : policyResult.trim();
						policyInput = policyInput == null ? "" : policyInput.trim();
						policyCond = policyCond == null ? "" : policyCond.trim();

						if (policyResult.length() == 0) {
						    errCode = "VISCRPRNL";
						    errString = getErrorString("policy_result", errCode, userId);
						    break;
						} else if (policyInput.length() == 0) {
						    errCode = "VISCRPINL";
						    errString = getErrorString("policy_input", errCode, userId);
						    break;
						} else if (policyCond.length() == 0) {
						    errCode = "VISCRPCNL";
						    errString = getErrorString("policy_condition", errCode, userId);
						    break;
						}
					 }
			    	}Comment By Nasruddin 20-SEP-16 END*/
    			}
    			break;
    		}
    	}
    	catch (Exception e)
    	{
    		System.out.println("^^^^^^^ ItemSerCrPolicy Validation Error ^^^^^^^");
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
    				// Changed By Nasruddin START 22-SEP-16
    				if(pStmt != null)
    				{
    					pStmt.close();
    					pStmt = null;
    				}
    				if(rs != null)
    				{
    					rs.close();
    					rs = null;
    				}
    				// Changed By Nasruddin END 22-SEP-16
    				conn.close();
    			}
    			conn = null;
    			genericUtility = null;
    			parentNodeList = null;
    			childNodeList = null;
    			parentNode = null;
    			childNode = null;
    			childNodeName = null;
    			errCode = null;
    			sql = null;
    			userId = null;
    			dateOfBirth = null;
    			itemSeries = null;
    			crPolicy = null;
    		} 
    		catch (Exception d) 
    		{
    			d.printStackTrace();
    			throw new ITMException(d);
    		}
    	}
    	return errString;
    }

    public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
		    dom = parseString(xmlString);
		    dom1 = parseString(xmlString1);
		    if (xmlString2.trim().length() > 0) {
		    	dom2 = parseString(xmlString2);
		    }
		    valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
		    System.out.println("Exception : [ItemSerCrPolicyEJB][itemChanged(String,String)] :==>\n" + e.getMessage());
		    throw new ITMException(e);
		}
		return valueXmlString;
    }

    public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
    	System.out.println("inside itemChanged ====================");
    	//GenericUtility genericUtility;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		int currentFormNo = 0, crpolicy = 0;
		SimpleDateFormat sdf ;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		String crPolicy = "", policySql = "", policyInput = "", policyResult = "", policyCondition = "", itemSeries = "";
		try {
		   // genericUtility = GenericUtility.getInstance();
		    ConnDriver conndriver = new ConnDriver();
		    //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		    conn.setAutoCommit(false);
		    conndriver = null;
		    System.out.println("-------------- ITEMcHANGE----------------------");
			System.out.println("hdrDataDom------->>["+genericUtility.serializeDom(dom)+"]");	
			System.out.println("currFormDataDom------>>["+genericUtility.serializeDom(dom1)+"]");
			System.out.println("allFormDataDom------>>["+genericUtility.serializeDom(dom2)+"]");
	
		   // genericUtility = GenericUtility.getInstance();
		   //Comment By Nasruddin 05-10-16 Start
		   // sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		    currentColumn = currentColumn == null ? "" : currentColumn.trim();
		    
		    if (objContext != null && objContext.trim().length() > 0)
		    {
		    	currentFormNo = Integer.parseInt(objContext);
		    }
		    valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
		    valueXmlString.append(editFlag).append("</editFlag></header>");
		    switch (currentFormNo) {
		    case 1:
				valueXmlString.append("<Detail1>");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					
				    itemSeries = getValueFromXTRA_PARAMS(xtraParams, "item_ser");
				    itemSeries = itemSeries == null ? "" : itemSeries.trim();
				    
				    valueXmlString.append("<itemSeries protect=\"1\">").append(itemSeries).append("</itemSeries>\r\n");
				    valueXmlString.append("<cr_policy protect=\"0\">").append(crPolicy).append("</cr_policy>\r\n");
				    
				} 
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					crPolicy = genericUtility.getColumnValue("cr_policy", dom);
					policySql = genericUtility.getColumnValue("policy_sql", dom);
					policyInput = genericUtility.getColumnValue("policy_input", dom);
					policyResult = genericUtility.getColumnValue("policy_result", dom);
					policyCondition = genericUtility.getColumnValue("policy_condition", dom);

					crPolicy = crPolicy == null ? "" : crPolicy.trim();
					policySql = policySql == null ? "" : policySql.trim();
					policyInput = policyInput == null ? "" : policyInput.trim();
					policyResult = policyResult == null ? "" : policyResult.trim();
					policyCondition = policyCondition == null ? "" : policyCondition.trim();

					if(crPolicy.startsWith("P") && crPolicy.length() >= 3)
					{
						System.out.println("P = "+crPolicy.substring(1,3));
						crpolicy = Integer.parseInt(crPolicy.substring(1,3));
					}
					if (crpolicy >= 90 && crpolicy <= 99) 
					{
						valueXmlString.append("<policy_sql protect=\"0\">").append(policySql).append("</policy_sql>\r\n");
						valueXmlString.append("<policy_input protect=\"0\">").append(policyInput).append("</policy_input>\r\n");
						valueXmlString.append("<policy_result protect=\"0\">").append(policyResult).append("</policy_result>\r\n");
						valueXmlString.append("<policy_condition protect=\"0\">").append(policyCondition).append("</policy_condition>\r\n");
					} 
					else 
					{
						valueXmlString.append("<policy_sql protect=\"1\">").append("</policy_sql>\r\n");
						valueXmlString.append("<policy_input protect=\"1\">").append("</policy_input>\r\n");
						valueXmlString.append("<policy_result protect=\"1\">").append("</policy_result>\r\n");
						valueXmlString.append("<policy_condition protect=\"1\">").append("</policy_condition>\r\n");
					}
					System.out.println("crPolicy ["+ crPolicy + "]");
					valueXmlString.append("<cr_policy protect=\"1\">").append(crPolicy).append("</cr_policy>\r\n");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("cr_policy")) 
				{
					System.out.println("==== iNSIDE cr_policy ");
					crPolicy = genericUtility.getColumnValue("cr_policy", dom);
					crPolicy = crPolicy == null ? "" : crPolicy.trim();
					System.out.println("==== iNSIDE cr_policy ["+crPolicy+"]");
					if(crPolicy.startsWith("P") && crPolicy.length() >= 3)
					{
						crpolicy = Integer.parseInt(crPolicy.substring(1,3));
					}
					if (crpolicy >= 90 && crpolicy <= 99)
					{
						valueXmlString.append("<policy_sql protect=\"0\">").append("</policy_sql>\r\n");
						valueXmlString.append("<policy_input protect=\"0\">").append("</policy_input>\r\n");
						valueXmlString.append("<policy_result protect=\"0\">").append("</policy_result>\r\n");
						valueXmlString.append("<policy_condition protect=\"0\">").append("</policy_condition>\r\n");
					} 
					else 
					{
						valueXmlString.append("<policy_sql protect=\"1\">").append("</policy_sql>\r\n");
						valueXmlString.append("<policy_input protect=\"1\">").append("</policy_input>\r\n");
						valueXmlString.append("<policy_result protect=\"1\">").append("</policy_result>\r\n");
						valueXmlString.append("<policy_condition protect=\"1\">").append("</policy_condition>\r\n");
					}
				}
				valueXmlString.append("</Detail1 >");
			break;
		    }
		    valueXmlString.append("</Root>");
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new ITMException(e);
		} finally {
		    try {
				if (conn != null) {
				    conn.close();
				}
				conn = null; genericUtility = null; pStmt = null; rs = null; sql = null; sdf = null;
				crPolicy = null; policySql = null; policyInput = null; policyResult = null; policyCondition = null; itemSeries = null;
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }
		}
		return valueXmlString.toString();
    }
}