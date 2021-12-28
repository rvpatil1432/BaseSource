package ibase.webitm.ejb.dis;
//Done by Dhiraj Chavan on 25/MAR/2016

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ITMDBAccessEJB;
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

public class DwhProdPlanIC extends ValidatorEJB implements DwhProdPlanICLocal,DwhProdPlanICRemote{	
	

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
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : DwhProdPlanIC : wfValData(String xmlString) : ==>\n"+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		String errString = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errCode = null;
		String userId = null;
		int cnt = 0;
		int ctr = 0;
		String siteCode = "";
		String prdCode="";
		String itemCode="";
		String itmUnit="";
		String itemGrade="";
		String acctPrd="";
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
					
					
					  acctPrd = genericUtility.getColumnValue("acct_prd", dom);					
				      siteCode = genericUtility.getColumnValue("site_code", dom);
				      prdCode = genericUtility.getColumnValue("code", dom);
				      itemCode = genericUtility.getColumnValue("item_code", dom);
				      itmUnit = genericUtility.getColumnValue("unit", dom);
				      itemGrade = genericUtility.getColumnValue("grade", dom);
				    
				  	
				     String Sql = "SELECT COUNT(1) AS COUNT FROM dwh_production_plan WHERE acct_prd=? AND site_code=? AND prd_code=? and item_code=? and grade=?";	
				     pstmt = conn.prepareStatement(Sql);
				     pstmt.setString(1,acctPrd);
			         pstmt.setString(2,siteCode);
			         pstmt.setString(3,prdCode);
			         pstmt.setString(4,itemCode);
			         pstmt.setString(5,itemGrade);
			         			         			        
			         rs = pstmt.executeQuery();
				     System.out.println("Sql@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + Sql);
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
				    
				   if(childNodeName.equalsIgnoreCase("acct_prd"))
				   {
					   acctPrd = genericUtility.getColumnValue("acct_prd",dom);
				    	if (acctPrd == null) 
					     {
					    	 errString = getErrorString(" ", "VMACTPRDB", userId);
								break;
					     } 
				    	if(acctPrd != null && acctPrd.trim().length() > 0)
				    	{


							sql = "select count(1) from period where acct_prd = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctPrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMACTPDCB";
								errString = getErrorString("acct_prd",
								errCode, userId);
								break;
							}
						
				    		
				    	}
				   }else if(childNodeName.equalsIgnoreCase("site_code")) 
					{
				    	siteCode = genericUtility.getColumnValue("site_code",dom);
				    	if (siteCode == null) 
					     {
					    	 errString = getErrorString(" ", "VMPSITECD", userId);
								break;
					     }
				    	
				    	if(siteCode != null && siteCode.trim().length() > 0)
						{

							sql = "select count(1) from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VTSITENEX";
								errString = getErrorString("site_code",
								errCode, userId);
								break;
							}
						}
					}
				   else if(childNodeName.equalsIgnoreCase("item_code"))
                   {
                      
                       itemCode = genericUtility.getColumnValue("item_code",dom);
                      
                       if(itemCode == null || itemCode.trim().length() == 0)
                       {
                           errString = itmDBAccessEJB.getErrorString("","VMITMB ",userId,"",conn);
                           break ;
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
								errString = getErrorString("item_code",errCode, userId);
								break;
							}
						}
                       
                   } else if(childNodeName.equalsIgnoreCase("code")) 
					{

						prdCode = genericUtility.getColumnValue("code", dom);
						acctPrd = genericUtility.getColumnValue("acct_prd",dom);
						if (prdCode == null) 
					     {
					    	 errString = getErrorString(" ", "VMPRDCB", userId);
								break; 
					     }
						if(prdCode != null && prdCode.trim().length() > 0)
						{
							sql = "select count(1) from period where code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, prdCode);
						
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							
							 System.out.println("@@@@@count point point@@@@I am at --(!editFlag.equalsIgnoreCase----)$$$$$$$$$"+cnt);
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								 errString = itmDBAccessEJB.getErrorString("","VMPRDCC",userId,"",conn);
								break;
							}//

						}
						if(prdCode != null && prdCode.trim().length() > 0)
						{
							sql = "select count(1) from period where code = ? and acct_prd=?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, prdCode);
							pstmt.setString(2, acctPrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							
							 System.out.println("@@@@@count point point@@@@I am at --(!editFlag.equalsIgnoreCase----)$$$$$$$$$"+cnt+"prd_code"+prdCode+"account code"+acctPrd);
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								 errString = itmDBAccessEJB.getErrorString("","VMPRDNM",userId,"",conn);
								break;
							}//select descr from period where code = ? and acct_prd=? 

						}
						
						
					} else if(childNodeName.equalsIgnoreCase("unit"))
                    {
                        if(itmUnit == null || itmUnit.trim().length() == 0)
                        {
                            errString = itmDBAccessEJB.getErrorString("","VMUNITB ",userId,"",conn);
                            break ;
                        }
                        if(itmUnit != null && itmUnit.trim().length() > 0)
						{
							sql = "select count(1) from uom where unit = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itmUnit);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							
							 System.out.println("@@@@@count point point@@@@I am at --(!editFlag.equalsIgnoreCase----)$$$$$$$$$"+cnt);
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMUNITC";
								errString = getErrorString("unit",errCode, userId);
								break;
							}

						}
                        
                        
                    }
                    else if (childNodeName.equalsIgnoreCase("grade")) 
					{
                    	itemGrade = genericUtility.getColumnValue("grade", dom);
						
						if (itemGrade == null) 
					     {
					    	 errString = getErrorString("", "VMGRDB", userId);
								break; 
					     }					
						
						else if (itemGrade != null && itemGrade.trim().length() > 0) {
							
							
								sql = "select COUNT(1) from grade where grade_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemGrade);
								rs = pstmt.executeQuery();
								if (rs.next()) {

								cnt = rs.getInt(1);
								}
								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0) {
																
								errString = itmDBAccessEJB.getErrorString("","VMGRDCB ",userId,"",conn);
	                            break ;
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
			System.out.println(" < DwhProdPlanIC> CONNECTION IS CLOSED");
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
			System.out.println("Exception :DwhProdPlanIC:itemChanged(String,String,String,String,String,String):"+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("returning from DwhProdPlanIC itemChanged");
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
		String siteCode = "", itemCode = "";
		String itemGrade = "", gradeDescr="";
		
		 String acctPrd="",acctPrdCode="",prdCodeDescr="",prdCode="",prdCodeDescr1="";
		String acctPrdDescr="",itemUnitDesc="";
		String itemDescr="";
		
		String sitedescr="";
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
					
				}
				if(currentColumn.trim().equalsIgnoreCase("acct_prd")){
					acctPrd = genericUtility.getColumnValue("acct_prd", dom);
					sql = " select code from period where acct_prd = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, acctPrd);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						 acctPrdCode = (rs.getString("code"));
					}
					System.out.println("@@@@@"+sql);
					System.out.println("@@@acctPrdCode" + acctPrdCode);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					sql = " select descr from period where code = ? and acct_prd=? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, acctPrdCode);
					pStmt.setString(2, acctPrd);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						 acctPrdDescr = (rs.getString("descr"));
					}
					System.out.println("account Description" + acctPrdDescr);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
						acctPrdCode = (acctPrdCode == null) ? "" : acctPrdCode.trim();
					valueXmlString.append("<descr >").append(
							"<![CDATA[" + acctPrdDescr + "]]>").append(
							"</descr>");
					
					valueXmlString.append("<code >").append(
							"<![CDATA[" + acctPrdCode + "]]>").append(
							"</code>");
					
					
				}else if(currentColumn.trim().equalsIgnoreCase("code")) {
					prdCode = genericUtility.getColumnValue("code", dom);
					System.out.println("@code"+prdCode);
					sql = " select descr from period where code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, prdCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						prdCodeDescr1 = (rs.getString("descr"));
					}
					System.out.println("@@2@@prdCodeDescr" + prdCodeDescr1);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					prdCodeDescr1 = (prdCodeDescr1 == null) ? "" : prdCodeDescr1.trim();
					valueXmlString.append("<descr>").append("<![CDATA[" + prdCodeDescr1 + "]]>").append("</descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code")) {
					siteCode = genericUtility.getColumnValue("site_code", dom);
					sql = " select descr from site where site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						sitedescr = (rs.getString("descr"));
					}
					System.out.println("Site Description" + sitedescr);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					sitedescr = (sitedescr == null) ? "" : sitedescr.trim();
					valueXmlString.append("<site_descr>").append(
							"<![CDATA[" + sitedescr + "]]>").append(
							"</site_descr>");
				}

				else if(currentColumn.trim().equalsIgnoreCase("item_code")) {
				 itemCode = genericUtility.getColumnValue("item_code", dom);

					sql = " SELECT descr,unit FROM item WHERE item_code= ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						itemDescr = rs.getString("descr") == null ? "" : rs.getString("descr");
						itemUnitDesc= rs.getString("unit")==null ? "" :rs.getString("unit");
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					itemDescr = (itemDescr == null) ? "" : itemDescr.trim();
					System.out.println("item description" + itemDescr);
					valueXmlString.append("<item_descr>").append(
							"<![CDATA[" + itemDescr + "]]>").append(
							"</item_descr>");
					itemUnitDesc = (itemUnitDesc == null) ? "" : itemUnitDesc.trim();
					System.out.println("item description" + itemUnitDesc);
					valueXmlString.append("<unit>").append(
							"<![CDATA[" + itemUnitDesc + "]]>").append(
							"</unit>");
					
					
				}	
				
				else if(currentColumn.trim().equalsIgnoreCase("grade")) {
				itemGrade = genericUtility.getColumnValue("grade", dom);
				//itemGrade = itemGrade == null ? "" : itemGrade.trim();
                sql = "select descr from grade where grade_code=?";
                pStmt = conn.prepareStatement(sql);
                pStmt.setString(1, itemGrade);
                rs = pStmt.executeQuery();
                if (rs.next())
                {
                    gradeDescr = rs.getString("descr");

                }
                rs.close();
                rs = null;
                pStmt.close();
                pStmt = null;

                gradeDescr = gradeDescr == null ? "" : gradeDescr.trim();
                System.out.println("grade description--"+gradeDescr);
                valueXmlString.append("<grade_descr>").append(
						"<![CDATA[" + gradeDescr + "]]>").append(
						"</grade_descr>");
          
				}
				
				
				valueXmlString.append("</Detail1 >");
				break;
			}
			valueXmlString.append("</Root>\r\n");
		} catch (Exception e) {
			System.out.println("Exceptio DwhProdPlanIC•@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@••••••(Document,String):"+ e.getMessage() + ":");
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
