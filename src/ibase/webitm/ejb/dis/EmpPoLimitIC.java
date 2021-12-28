 
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

public class EmpPoLimitIC extends ValidatorEJB implements EmpPoLimitICLocal,EmpPoLimitICRemote{
	
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
			System.out.println("Exception : EmpPoLimit : wfValData(String xmlString) : ==>\n"+ e.getMessage());
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
		String empCode = "",empPoLimit="";
		String itemSeries ="";
		String eff_from="";
		String valid_upto="";
		
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Timestamp effDate=null;
		Timestamp validDate=null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		
		//Timestamp eff_from1=null, valid_upto1=null;
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
					System.out.println("COUNT OF CHILD NODE LIST"+childNodeListLength);
					System.out.println("COUNT OF CHILD NODE LIST(ctr<childnodelist)"+ctr);
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNode@@"+childNode);
					int seriCount = 0;
				      empCode = genericUtility.getColumnValue("emp_code", dom);
				      itemSeries = genericUtility.getColumnValue("item_ser", dom);
				      eff_from=checkNull(genericUtility.getColumnValue("eff_from", dom));
				      valid_upto=checkNull(genericUtility.getColumnValue("valid_upto", dom));
				    				  	
					if(eff_from !=null && eff_from.trim().length()>0 )
					{
					 effDate = Timestamp.valueOf(genericUtility.getValidDateString(eff_from, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					if(valid_upto !=null && valid_upto.trim().length()>0 )
					{
					 validDate = Timestamp.valueOf(genericUtility.getValidDateString(valid_upto, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					 System.out.println("siteCode"+empCode+"custcode"+itemSeries+"eff_from"+eff_from+"valid_upto"+valid_upto);
				      
				      System.out.println("effDate@@@"+effDate+"validDate@@@"+validDate);
				  
				     String Sql = "SELECT COUNT(1) AS COUNT FROM emp_po_limit WHERE emp_code=? AND item_ser=? and eff_from=? and valid_upto=?";
			         pstmt = conn.prepareStatement(Sql);
			         pstmt.setString(1,empCode);
			         pstmt.setString(2,itemSeries);
			         pstmt.setTimestamp(3,effDate);	         
			         pstmt.setTimestamp(4,validDate);
			         rs = pstmt.executeQuery();
			         System.out.println("eff_date@@@@for testing"+effDate);
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
				    if (childNodeName.equalsIgnoreCase("emp_code")) 
					{
				    	empCode = genericUtility.getColumnValue("emp_code",dom);
				    	if (empCode == null) 
					     {
					    	 errString = getErrorString(" ", "VMEMPB", userId);
								break;
					     }
				    	
				    	if(empCode != null && empCode.trim().length() > 0)
						{

							sql = "select count(1) from employee where emp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "VMEMPBC";
								errString = getErrorString("emp_code",
								errCode, userId);
								break;
							}
						}
					} else if (childNodeName.equalsIgnoreCase("item_ser")) 
					{

						itemSeries = genericUtility.getColumnValue("item_ser", dom);
						
						if (itemSeries == null) 
					     {
					    	 errString = getErrorString(" ", "VMITMSB", userId);
								break; 
					     }
						if(itemSeries != null && itemSeries.trim().length() > 0)
						{
							sql = "select count(1) from item where item_ser = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSeries);
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
								errCode = "VMITMSBC";
								errString = getErrorString("item_ser",
										errCode, userId);
								break;
							}

						}
					}
					else if(childNodeName.equalsIgnoreCase("emp_po_limit"))
                    {
                        System.out.println("validation EFF_FROM  executed");
                        empPoLimit = genericUtility.getColumnValue("emp_po_limit",dom);
                        System.out.println(" emp_po_limit " +empPoLimit);
                        if(empPoLimit == null || empPoLimit.trim().length() == 0)
                        {
                            errString = itmDBAccessEJB.getErrorString("","VMEPLB ",userId,"",conn);
                            break ;
                        }
                    }
					else if(childNodeName.equalsIgnoreCase("eff_from"))
                    {
                        System.out.println("validation EFF_FROM  executed");
                        eff_from = genericUtility.getColumnValue("eff_from",dom);
                        System.out.println(" EFF_FROM " +eff_from);
                        if(eff_from == null || eff_from.trim().length() == 0)
                        {
                            errString = itmDBAccessEJB.getErrorString("","VMCREFRMB ",userId,"",conn);
                            break ;
                        }
                    }
                    else if(childNodeName.equalsIgnoreCase("valid_upto"))
                    {
                        System.out.println("validation VALID_UPTO  executed");
                        valid_upto = checkNull(genericUtility.getColumnValue("valid_upto",dom));
                        eff_from = checkNull(genericUtility.getColumnValue("eff_from",dom));
                        System.out.println(" EFF_FROM " +eff_from);
                        System.out.println(" valid_upto " +valid_upto);
                        if(valid_upto == null || valid_upto.trim().length() == 0)
                        {
                            errString = itmDBAccessEJB.getErrorString("","VMCRVTOB ",userId,"",conn);
                            break ;
                        }
                        else if(eff_from !=null && eff_from.trim().length() > 0)
                        {
                            System.out.println("eff_from"+eff_from);
                            System.out.println("valid_upto"+validDate);
                       
                            effDate = Timestamp.valueOf(genericUtility.getValidDateString(eff_from, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
                            validDate = Timestamp.valueOf(genericUtility.getValidDateString(valid_upto, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
                            System.out.println("effDate"+effDate);
                            System.out.println("effDate"+validDate);
                            if(validDate != null && validDate.before(effDate))
                            {
                            	System.out.println("In COndition");
                                errString = itmDBAccessEJB.getErrorString("","VTDATE6 ",userId,"",conn);
                                break ;
                            }
                        }
                               
                    }
				    
				    if (!editFlag.equalsIgnoreCase("E")) {
				    	
				    	System.out.println("Comming to !editFlag condition");
				    sql=null;
				    sql=" select COUNT(1) from emp_po_limit where emp_code=? and item_ser=? and ( ( ? BETWEEN eff_from AND valid_upto ) OR (? BETWEEN eff_from AND valid_upto ))"; 
                  	pstmt = conn.prepareStatement(sql);
					 pstmt.setString(1,empCode);
			         pstmt.setString(2,itemSeries);
			         pstmt.setTimestamp(3,effDate);	         
			         pstmt.setTimestamp(4,validDate);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}					
					 System.out.println("CHECKING DUPLICATE PERIOD"+cnt);
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if(cnt > 0) {
						
						  errString = itmDBAccessEJB.getErrorString("","VMRECD ",userId,"",conn);
                            break ;
					}
				    }
				        
			     System.out.println("Value of itemcode is:*************************"+empCode);
				     System.out.println("Value ofcust code is:*********************"+itemSeries);
				  
				     System.out.println("Value of effective date is:*********************"+eff_from);

				     System.out.println("Value of Valid upto date is:*********************"+valid_upto);
					
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
			System.out.println(" < EmpPoLimitValidEJB > CONNECTION IS CLOSED");
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
					.println("Exception :EmpPoLimitValid:itemChanged(String,String,String,String,String,String):"
							+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("returning from EmpPoLimitValid itemChanged");
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
		String siteCode = "", sitedescr = "";
		String custname = "";
		
		String itemSerDescr="",empCode="",descr="",empCode1="";
		try {
			//genericUtility = genericUtility.getInstance();
			// siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

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
					// tranDate = (sdf.format(timestamp).toString()).trim();
				}
				
				 if (currentColumn.trim().equalsIgnoreCase("emp_code")) {
					empCode = genericUtility.getColumnValue("emp_code", dom);

					sql = " select emp_code from employee where emp_code=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, empCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						empCode1 = rs.getString("emp_code") == null ? "" : rs.getString("emp_code");
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					
					sql = " select FN_SUNDRY_NAME('E','"+empCode1+"','') as descr from dual";
					pStmt = conn.prepareStatement(sql);
					//pStmt.setString(1, empCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr") == null ? "" : rs.getString("descr");
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					descr = (descr == null) ? "" : descr.trim();
					System.out.println("employee name" + descr);
					valueXmlString.append("<descr>").append(descr)
							.append("</descr>\r\n");

				}
				
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
		System.out.println("\n***** ValueXmlString :" + valueXmlString
				+ ":*******");
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
