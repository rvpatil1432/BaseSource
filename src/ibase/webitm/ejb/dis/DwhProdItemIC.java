package ibase.webitm.ejb.dis;
/*Done by Dhiraj Chavan on 17/Mar/2016*/
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless;
@Stateless

public class DwhProdItemIC extends ValidatorEJB implements DwhProdItemICLocal,DwhProdItemICRemote {	
	

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData() throws RemoteException, ITMException {
		return "";
	}

	public String itemChanged() throws RemoteException, ITMException {
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		System.out.println("Validation Start..........");
		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : DwhProdItemIC : wfValData(String xmlString) : ==>\n"+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errCode = null;
		String userId = null;
		int cnt = 0;
		int ctr = 0;
		String itemCode = "",itemFamily="";
		String itemUnit ="";
		String itemParent="";
		String itemParentUnit="";
		
		String itemFamilyUnit="";
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		
		
		ConnDriver connDriver = new ConnDriver();
		try {
			System.out.println("wfValData called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					int seriCount = 0;
				      itemCode = genericUtility.getColumnValue("item_code", dom);
				      itemUnit=genericUtility.getColumnValue("item_unit", dom);
				      itemParent = genericUtility.getColumnValue("item_parent", dom);
				      itemParentUnit=genericUtility.getColumnValue("item_parent_unit", dom);
				      itemFamilyUnit=genericUtility.getColumnValue("item_family_unit", dom);
				     
				  	
					
				     String Sql = "SELECT COUNT(1) AS COUNT FROM dwh_production_item WHERE item_code=? and item_parent=?";
			         pstmt = conn.prepareStatement(Sql);
			         pstmt.setString(1,itemCode);
			         pstmt.setString(2,itemParent);
			         rs=pstmt.executeQuery();
				     System.out.println("Sql@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + Sql);
				     System.out.println("itemCode--->"+itemCode+"itemParent------>"+itemParent);
				     if (rs.next()) 
				     {
				    	 seriCount = rs.getInt("COUNT");
				     }
				     System.out.println("COunt is@@@@@@@@@dhiraj$$$$$$$$$"+seriCount);
				    if (!editFlag.equalsIgnoreCase("E")) 
				     {
				    	   System.out.println("@@@@@Start point@@@@I am at --(!editFlag.equalsIgnoreCase----)$$$$$$$$$");
				       if (seriCount > 0) 
				       {
				    	   System.out.println("@@@@@middle point@@@@I am at --(!editFlag.equalsIgnoreCase----)$$$$$$$$$");
					errString = getErrorString(" ", "VMDUPREC1", userId);				
					 System.out.println("@@@@@before break point@@@@I am at --(!editFlag.equalsIgnoreCase----)$$$$$$$$$");
					break;
					
				       }
				     }
				    if (childNodeName.equalsIgnoreCase("item_code")) 
					{
				    	itemCode = genericUtility.getColumnValue("item_code",dom);
				    	if (itemCode == null) 
					     {
					    	 errString = getErrorString(" ", "VMITMB", userId);
								break;
					     }
				    	
				    	if(itemCode != null && itemCode.trim().length() > 0)
						{

							sql = "select count(1) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMITMBC";
								errString = getErrorString("item_code",
								errCode, userId);
								break;
							}
						}
					} else if (childNodeName.equalsIgnoreCase("item_unit")) 
					{

						itemUnit = genericUtility.getColumnValue("item_unit", dom);
						
						if (itemUnit == null) 
					     {
					    	 errString = getErrorString(" ", "VMITMUB", userId);
								break; 
					     }
						if(itemUnit != null && itemUnit.trim().length() > 0)
						{

							sql = "select count(1) from uom where unit = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemUnit);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMITMUC";
								errString = getErrorString("item_unit",
								errCode, userId);
								break;
							}
						}
						
						
						
						
					}
				   
					else if(childNodeName.equalsIgnoreCase("item_parent"))
                    {
                       
                        itemParent = genericUtility.getColumnValue("item_parent",dom);
                        System.out.println(" item_parent " +itemParent);
                        if(itemParent == null || itemParent.trim().length() == 0)
                        {
                            errString = itmDBAccessEJB.getErrorString("","VMITMP ",userId,"",conn);
                            break ;
                        }
                        if(itemParent != null && itemParent.trim().length() > 0)
						{

							sql = "select count(1) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemParent);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMITMPC";
								errString = getErrorString("item_parent",
								errCode, userId);
								break;
							}
						}
                        
                        
                        
                    }
					else if (childNodeName.equalsIgnoreCase("item_parent_unit")) 
					{

						itemParentUnit = genericUtility.getColumnValue("item_parent_unit", dom);
						
						if (itemParentUnit == null) 
					     {
					    	 errString = getErrorString(" ", "VMITMPUCB", userId);
								break; 
					     }
						if(itemParentUnit != null && itemParentUnit.trim().length() > 0)
						{

							sql = "select count(1) from uom where unit = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemParentUnit);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMITMPUC";//item_family_unit
								errString = getErrorString("item_parent_unit",
								errCode, userId);
								break;
							}
						}
							
					}else if (childNodeName.equalsIgnoreCase("item_family")) 
					{

						itemFamily = genericUtility.getColumnValue("item_family", dom);
						
						/*if (item_family == null) 
					     {
					    	 errString = getErrorString(" ", "VMITMPUCB", userId);
								break; 
					     }*/
						if(itemFamily != null && itemFamily.trim().length() > 0)
						{

							sql = "select count(1) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemFamily);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMITMFC";//item_family
								errString = getErrorString("item_family",
								errCode, userId);
								break;
							}
						}
							
					}
				    
				    
					else if (childNodeName.equalsIgnoreCase("item_family_unit")) 
					{

						itemFamilyUnit = genericUtility.getColumnValue("item_family_unit", dom);
						
						if (itemFamilyUnit == null) 
					     {
					    	 errString = getErrorString(" ", "VMITMFUCB", userId);
								break; 
					     }
						if(itemFamilyUnit != null && itemFamilyUnit.trim().length() > 0)
						{

							sql = "select count(1) from uom where unit = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemFamilyUnit);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMITMFUC";
								errString = getErrorString("item_family_unit",
								errCode, userId);
								break;
							}
						}
							
					}
                  
				    
				}
				break;
				}
			

		} catch (Exception e) {
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			errCode = "VALEXCEP";
			errString = getErrorString("", errCode, userId);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		} finally {
			try {
				if (conn != null) {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}

					if (rs != null) {
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
			System.out.println(" < DwhProductItemIC > CONNECTION IS CLOSED");
		}
		System.out.println("@@@@@@@@Error String@@@@@@@@@@ErrString ::" + errString);

		return errString;
	}// END OF VALIDATION

	@Override
	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
			
		Document dom1 = null;
		Document dom = null;
		Document dom2 = null;
		String valueXmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0) {
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception :DwhProdItemIC:itemChanged(String,String,String,String,String,String):"+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("returning from DwhProdItemIC itemChanged");
		return (valueXmlString);
	}

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		StringBuffer valueXmlString = new StringBuffer();

		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		int currentFormNo = 0;

		//GenericUtility genericUtility;
		String itemCode = "",itemFamily="",item_parntDesc2="", itemdescr = "",itemParent="",item_parntDesc="",itemUnitDesc="",itemUnitDesc1="",item_parntDesc1 ="",itemUnit="",itemUnitDescUom="",item_parntDescParent="";
		
		try {
			//genericUtility = GenericUtility.getInstance();
			ConnDriver conndriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			conndriver = null;
			if (objContext != null && objContext.trim().length() > 0)
				currentFormNo = Integer.parseInt(objContext);

			currentColumn = currentColumn == null ? "" : currentColumn.trim();
			System.out.println("currentColumn : " + currentColumn);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("current form no: " + currentFormNo);
			System.out.println("dom:::::::::: : "+ genericUtility.serializeDom(dom));
			System.out.println("dom11111111111111:::::::::: : "+ genericUtility.serializeDom(dom1));
			System.out.println("dom222222222222222:::::::::: : "+ genericUtility.serializeDom(dom2));

			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				if ("itm_default".equalsIgnoreCase(currentColumn)) {
					System.out.println("itm_default : ");
					System.out.println("Content of xtraParams ..> "	+ xtraParams);
					
				}
				
				if (currentColumn.trim().equalsIgnoreCase("item_code")) {
					itemCode = genericUtility.getColumnValue("item_code", dom);
					itemUnit = genericUtility.getColumnValue("item_unit", dom);
					sql = " select descr,unit,item_parnt from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCode);
					//pStmt.setString(1, itemUnit);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						itemdescr = (rs.getString("descr"));
						itemUnitDesc= (rs.getString("unit"));
						item_parntDesc = (rs.getString("item_parnt"));
					}
					System.out.println("Site Description" + itemdescr);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					
					sql = " select descr from uom where unit = ?   ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemUnitDesc);
					
					rs = pStmt.executeQuery();
					if (rs.next()) {
						itemUnitDescUom= (rs.getString("descr"));
					}
					System.out.println("item Unit Description" + itemUnitDesc);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					
					sql = " select descr from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, item_parntDesc);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						item_parntDescParent = (rs.getString("descr"));
					}
					System.out.println("item parent Description" + item_parntDescParent);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					
					itemdescr = (itemdescr == null) ? "" : itemdescr.trim();
					valueXmlString.append("<descr >").append("<![CDATA[" + itemdescr + "]]>").append("</descr>");
					
					itemUnitDesc = (itemUnitDesc == null) ? "" : itemUnitDesc.trim();
					valueXmlString.append("<item_unit >").append("<![CDATA[" + itemUnitDesc + "]]>").append("</item_unit>");
					
					itemUnitDescUom = (itemUnitDescUom == null) ? "" : itemUnitDescUom.trim();
					valueXmlString.append("<uom_descr >").append("<![CDATA[" + itemUnitDescUom + "]]>").append("</uom_descr>");
					
					item_parntDesc = (item_parntDesc == null) ? "" : item_parntDesc.trim();
					valueXmlString.append("<item_parent >").append("<![CDATA[" + item_parntDesc + "]]>").append("</item_parent>");
					
					item_parntDescParent = (item_parntDescParent == null) ? "" : item_parntDescParent.trim();
					valueXmlString.append("<item_descr >").append("<![CDATA[" + item_parntDescParent + "]]>").append("</item_descr>");
					
					itemUnitDesc = (itemUnitDesc == null) ? "" : itemUnitDesc.trim();
					valueXmlString.append("<item_family_unit >").append("<![CDATA[" + itemUnitDesc + "]]>").append("</item_family_unit>");
					
					itemUnitDesc = (itemUnitDesc == null) ? "" : itemUnitDesc.trim();
					valueXmlString.append("<item_parent_unit >").append("<![CDATA[" + itemUnitDesc + "]]>").append("</item_parent_unit>");
			
					valueXmlString.append("<item_family >").append("<![CDATA[" + itemCode + "]]>").append("</item_family>");
					valueXmlString.append("<item_family_name >").append("<![CDATA[" + item_parntDescParent + "]]>").append("</item_family_name>");
					
				}else if(currentColumn.trim().equalsIgnoreCase("item_unit")){


					itemUnit = genericUtility.getColumnValue("item_unit", dom);
					sql = " select descr from uom where unit = ?   ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemUnit);
					
					rs = pStmt.executeQuery();
					if (rs.next()) {
						itemUnitDesc1= (rs.getString("descr"));
					}
					System.out.println("item Unit Description" + itemUnitDesc);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					itemUnitDesc = (itemUnitDesc == null) ? "" : itemUnitDesc.trim();
					valueXmlString.append("<uom_descr >").append("<![CDATA[" + itemUnitDesc1 + "]]>").append("</uom_descr>");
				
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("item_parent"))
				{

					itemParent = genericUtility.getColumnValue("item_parent", dom);
					sql = " select descr from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemParent);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						item_parntDesc1 = (rs.getString("descr"));
					}
					System.out.println("item parent Description" + item_parntDesc1);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					item_parntDesc1 = (item_parntDesc1 == null) ? "" : item_parntDesc1.trim();
					valueXmlString.append("<item_descr >").append("<![CDATA[" + item_parntDesc1 + "]]>").append("</item_descr>");
				
					
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("item_family"))
				{

					itemFamily = genericUtility.getColumnValue("item_family", dom);
					sql = " select descr from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemFamily);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						item_parntDesc2 = (rs.getString("descr"));
					}
					System.out.println("item parent Description" + item_parntDesc2);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					item_parntDesc2 = (item_parntDesc2 == null) ? "" : item_parntDesc2.trim();
					valueXmlString.append("<item_family_name >").append("<![CDATA[" + item_parntDesc2 + "]]>").append("</item_family_name>");
				
					
					
				}

				valueXmlString.append("</Detail1 >");
				break;
			}
			valueXmlString.append("</Root>\r\n");
		} catch (Exception e) {
			System.out
					.println("Exception :DwhProdItemIC•@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@••••••(Document,String):"+ e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());

			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}
		}
		System.out.println("\n***** ValueXmlString :" + valueXmlString+ ":*******");
		return valueXmlString.toString();
	}
	
	 private String checkNull(String input) 
	   {
		  if(input == null)
		  {
			 input = "";
		  }
		return input;
	   }
	 

	 
}
