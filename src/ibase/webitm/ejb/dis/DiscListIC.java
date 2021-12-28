
package ibase.webitm.ejb.dis;


import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
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

public class DiscListIC extends ValidatorEJB implements DiscListICLocal,DiscListICRemote{
	
	E12GenericUtility genericUtility=new E12GenericUtility();

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
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : DistListIC : wfValData(String xmlString) : ==>\n"+ e.getMessage());
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
		
		
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String discList="",descr="",shDescr="",calcType="";
		
		//Timestamp eff_from1=null, valid_upto1=null;
		ConnDriver connDriver = new ConnDriver();
		try {
			
			System.out.println("wfValData called");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
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
					System.out.println("COUNT OF CHILD NODE LIST"+childNodeListLength);
					System.out.println("COUNT OF CHILD NODE LIST(ctr<childnodelist)"+ctr);
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNode@@"+childNode);
					int seriCount = 0;
					
					discList = genericUtility.getColumnValue("disc_list", dom);
					descr = genericUtility.getColumnValue("descr", dom);
					shDescr=checkNull(genericUtility.getColumnValue("sh_descr", dom));
					calcType=checkNull(genericUtility.getColumnValue("calc_type", dom));
				    
				     String Sql = "SELECT COUNT(1) AS COUNT FROM disc_list WHERE disc_list=? AND descr=? and sh_descr=? and calc_type=?";
			         pstmt = conn.prepareStatement(Sql);
			         pstmt.setString(1,discList);
			         pstmt.setString(2,descr);
			         pstmt.setString(3,shDescr);	         
			         pstmt.setString(4,calcType);
			         rs = pstmt.executeQuery();
			       
				     System.out.println("Sql@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + Sql);
				     if (rs.next()) 
				     {
				    	 seriCount = rs.getInt("COUNT");
				     }
				     System.out.println("COunt is@@@@@@@@@dhiraj$$$$$$$$$"+seriCount);
				     System.out.println("editFlag"+editFlag);
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
				    
				    
				    if (childNodeName.equalsIgnoreCase("disc_list")) 
					{
				    	discList = genericUtility.getColumnValue("disc_list", dom);
				    	//discList = genericUtility.getColumnValue("disc_list",dom);
				    	if (discList == null || discList.trim().length() == 0) 
					     {
					    	 errString = getErrorString(" ", "VMDISCNE", userId);
								break;
					     }
				    	
				    	if(editFlag.equals("A"))
				    	{
				    	if(discList != null && discList.trim().length() > 0)
						{

							sql = "select count(1) from disc_list where disc_list = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, discList);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 1) {
							
								errString = getErrorString(" ", "VMDISDUP", userId);
								break; 
								
							}
						}}
				    	
					} else if (childNodeName.equalsIgnoreCase("descr")) 
					{

						descr = genericUtility.getColumnValue("descr", dom);
						
						if (descr == null ) 
					     {
					    	 errString = getErrorString(" ", "VMDESCNB", userId);
								break; 
					     }
					
					}
					else if(childNodeName.equalsIgnoreCase("sh_descr"))
                   {
                      
                       shDescr = genericUtility.getColumnValue("sh_descr",dom);
                     
                       if(shDescr == null || shDescr.trim().length() == 0)
                       {
                           errString = itmDBAccessEJB.getErrorString("","VMSDESCNB",userId,"",conn);
                           break ;
                       }
                   }
					
                   else if(childNodeName.equalsIgnoreCase("calc_type"))
                   {
                 
                       calcType = checkNull(genericUtility.getColumnValue("calc_type",dom));
                   
                      
                       System.out.println(" calcType @@" +calcType);
                       if(calcType == null || calcType.trim().length() == 0)
                       {
                           errString = itmDBAccessEJB.getErrorString("","VMCALCT ",userId,"",conn);
                           break ;
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
			System.out.println(" < DistListIC > CONNECTION IS CLOSED");
		}
		System.out.println("@@@@@@@@dhiraj@@@@@@@@@@ErrString ::" + errString);

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
			System.out
					.println("Exception :DistListIC:itemChanged(String,String,String,String,String,String):"
							+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("returning from DistListIC:itemChanged");
		return (valueXmlString);
	}

/*	@Override
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
		String siteCode = "", sitedescr = "";
		String custname = "";
		String descList="",discDesc="";
		String itemSerDescr="",empCode="",descr="",empCode1="";
		try {
			//genericUtility = genericUtility.getInstance();
			// siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

			ConnDriver conndriver = new ConnDriver();
			conn = conndriver.getConnectDB("DriverITM");
			conn.setAutoCommit(false);
			conndriver = null;
			if (objContext != null && objContext.trim().length() > 0)
				currentFormNo = Integer.parseInt(objContext);

			currentColumn = currentColumn == null ? "" : currentColumn.trim();
			System.out.println("currentColumn : " + currentColumn);
			valueXmlString = new StringBuffer(
					"<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("current form no: " + currentFormNo);
			System.out.println("dom:::::::::: : "
					+ genericUtility.serializeDom(dom));
			System.out.println("dom11111111111111:::::::::: : "
					+ genericUtility.serializeDom(dom1));
			System.out.println("dom222222222222222:::::::::: : "
					+ genericUtility.serializeDom(dom2));

			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				if ("itm_default".equalsIgnoreCase(currentColumn)) {
					System.out.println("itm_default : ");
					System.out.println("Content of xtraParams ..> "
							+ xtraParams);
					// tranDate = (sdf.format(timestamp).toString()).trim();
				}
				
				 if (currentColumn.trim().equalsIgnoreCase("disc_list")) {
				 
					 if (!editFlag.equalsIgnoreCase("E")) 
						{
						 String descList="",discDesc="";
						 descList=genericUtility.getColumnValue("disc_list", dom);
						 
						 sql = "select descr from disc_list where disc_list=? ";
						 pStmt = conn.prepareStatement(sql);
						 pStmt.setString(1, descList);
						 rs = pStmt.executeQuery();
						 if (rs.next())
						   {
							  discDesc = rs.getString(1);

							}
						 rs.close();
						 rs = null;
						 pStmt.close();
						 pStmt = null;
						 System.out.println("itm_defaultedit@discDescription@"+discDesc);
						 discDesc = discDesc == null ? "" : discDesc.trim();
						System.out.println("discDesc["+discDesc+"]");
						 
							System.out.println("IN IF EditFlag["+editFlag+"]");
							//valueXmlString.append("<profile_id protect = \"0\" >").append("<![CDATA[" + itmEditprofile + "]]>").append("</profile_id>");
							//valueXmlString.append("<disc_list>").append("<![CDATA[" + descList + "]]>").append("</disc_list>");
							valueXmlString.append("<descr protect = \"0\" >").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							//valueXmlString.append("<descr protect = \"0\" >").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							
							//valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							//valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							
						}
				 }
				 sql = "select descr from disc_list where disc_list=? ";
				 pStmt = conn.prepareStatement(sql);
				 pStmt.setString(1, descList);
				 rs = pStmt.executeQuery();
				 if (rs.next())
				   {
					  discDesc = rs.getString(1);

					}
				 rs.close();
				 rs = null;
				 pStmt.close();
				 pStmt = null;
				 System.out.println("itm_defaultedit@discDescription@"+discDesc);
				 discDesc = discDesc == null ? "" : discDesc.trim();
				System.out.println("discDesc["+discDesc+"]");
				 if (!editFlag.equalsIgnoreCase("E")) 
					{
					 
					 descList=genericUtility.getColumnValue("disc_list", dom);
					 
					 sql = "select descr from disc_list where disc_list=? ";
					 pStmt = conn.prepareStatement(sql);
					 pStmt.setString(1, descList);
					 rs = pStmt.executeQuery();
					 if (rs.next())
					   {
						  discDesc = rs.getString(1);

						}
					 rs.close();
					 rs = null;
					 pStmt.close();
					 pStmt = null;
					 System.out.println("itm_defaultedit@discDescription@"+discDesc);
					 discDesc = discDesc == null ? "" : discDesc.trim();
					System.out.println("discDesc["+discDesc+"]");
					 
						System.out.println("IN IF EditFlag["+editFlag+"]");
						//valueXmlString.append("<profile_id protect = \"0\" >").append("<![CDATA[" + itmEditprofile + "]]>").append("</profile_id>");
						//valueXmlString.append("<disc_list>").append("<![CDATA[" + descList + "]]>").append("</disc_list>");
						valueXmlString.append("<descr protect = \"0\" >").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
						//valueXmlString.append("<descr protect = \"0\" >").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
						
						//valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
						//valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
						
					}
				 valueXmlString.append("<descr protect = \"0\" >").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
				 
				
				valueXmlString.append("</Detail1 >");
				break;
			}
			valueXmlString.append("</Root>\r\n");
		} catch (Exception e) {
			System.out
					.println("Exception :EmpPoLimitIC•@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@••••••(Document,String):"
							+ e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());

			e.printStackTrace();
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
		System.out.println("\n***** ValueXmlString :" + valueXmlString
				+ ":*******");
		return valueXmlString.toString();
	}*/
	
	 private String checkNull(String input) 
	   {
		  if(input == null)
		  {
			 input = "";
		  }
		return input;
	   }
		

}
