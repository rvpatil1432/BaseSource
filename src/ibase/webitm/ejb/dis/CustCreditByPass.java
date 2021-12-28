package ibase.webitm.ejb.dis;
/* Code By Dhiraj Chavan*/

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
public class CustCreditByPass extends ValidatorEJB implements CustCreditByPassLocal,CustCreditByPassRemote{	
	
	//Comment By Nasruddin 07-10-16 GenericUtility
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
			System.out
					.println("Exception : CrlimitValid : wfValData(String xmlString) : ==>\n"
							+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
		String siteCode = "";
		String custcode ="";
		String eff_from="";
		String valid_upto="";
		String reasCode="";
		
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
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					int seriCount = 0;
				      siteCode = genericUtility.getColumnValue("site_code", dom);
				      custcode = genericUtility.getColumnValue("cust_code", dom);
				      eff_from=checkNull(genericUtility.getColumnValue("eff_from", dom));
				      valid_upto=checkNull(genericUtility.getColumnValue("valid_upto", dom));
				      reasCode=genericUtility.getColumnValue("reas_code", dom);
				    
				  	
					if(eff_from !=null && eff_from.trim().length()>0 )
					{
					 effDate = Timestamp.valueOf(genericUtility.getValidDateString(eff_from, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					if(valid_upto !=null && valid_upto.trim().length()>0 )
					{
					 validDate = Timestamp.valueOf(genericUtility.getValidDateString(valid_upto, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					 System.out.println("siteCode"+siteCode+"custcode"+custcode+"eff_from"+eff_from+"valid_upto"+valid_upto);
				      
				      System.out.println("effDate@@@"+effDate+"validDate@@@"+validDate);
				  
				     String Sql = "SELECT COUNT(1) AS COUNT FROM cust_bypass_crchk WHERE site_code=? AND cust_code=? and eff_from=? and valid_upto=?";
			         pstmt = conn.prepareStatement(Sql);
			         pstmt.setString(1,siteCode);
			         pstmt.setString(2,custcode);
			         pstmt.setTimestamp(3,effDate);	         
			         pstmt.setTimestamp(4,validDate);
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
				    if (childNodeName.equalsIgnoreCase("site_code")) 
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
					} else if (childNodeName.equalsIgnoreCase("cust_code")) 
					{

						custcode = genericUtility.getColumnValue("cust_code", dom);
						
						if (custcode == null) 
					     {
					    	 errString = getErrorString(" ", "CRBYPLIMC", userId);
								break; 
					     }
						if(custcode != null && custcode.trim().length() > 0)
						{
							sql = "select count(1) from customer where cust_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custcode);
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
								errCode = "VMINVCD";
								errString = getErrorString("cust_code",
										errCode, userId);
								break;
							}

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
                    else if (childNodeName.equalsIgnoreCase("reas_code")) 
					{

						reasCode = genericUtility.getColumnValue("reas_code", dom);
						
						if (reasCode == null) 
					     {
					    	 errString = getErrorString(" ", "VMRESC", userId);
								break; 
					     }					
						
						else if (reasCode != null && reasCode.trim().length() > 0) {
							
							System.out.println("reason code"+reasCode);
								sql = "select count(*) from gencodes where fld_value = ? AND mod_name = 'W_CR_BYPASS_CHK' AND  fld_Name = 'REAS_CODE'" ;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, reasCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {

								cnt = rs.getInt(1);
								}
								System.out.println("count for reas code"+cnt);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0) {
																
								errString = itmDBAccessEJB.getErrorString("","VTRESKCD ",userId,"",conn);
	                            break ;
								}
								}
								
						
						
						
						
						
					}
			     System.out.println("Value of itemcode is:*************************"+siteCode);
				     System.out.println("Value ofcust code is:*********************"+custcode);
				  
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
			System.out.println(" < CrLimitValidEJB > CONNECTION IS CLOSED");
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
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
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
					.println("Exception :CrlimitValid:itemChanged(String,String,String,String,String,String):"
							+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		System.out.println("returning from CrLimitValid itemChanged");
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
		E12GenericUtility genericUtility= null;
		//GenericUtility genericUtility;
		String siteCode = "", sitedescr = "";
		String custCode = "", custname = "";
		String descr=null;
		String reasCode="";
		try {
			//genericUtility = GenericUtility.getInstance();
			 genericUtility= new  E12GenericUtility();
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
				if (currentColumn.trim().equalsIgnoreCase("site_code")) {
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
					valueXmlString.append("<site_descr >").append(
							"<![CDATA[" + sitedescr + "]]>").append(
							"</site_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("cust_code")) {
					custCode = genericUtility
							.getColumnValue("cust_code", dom);

					sql = " SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE= ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, custCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						custname = rs.getString("cust_name") == null ? "" : rs
								.getString("cust_name");
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					custname = (custname == null) ? "" : custname.trim();
					System.out.println("Customer name" + custname);
					valueXmlString.append("<cust_name>").append(custname)
							.append("</cust_name>\r\n");

				}
				
				else if (currentColumn.trim().equalsIgnoreCase("reas_code")) {
				reasCode = genericUtility.getColumnValue("reas_code", dom);
				reasCode = reasCode == null ? "" : reasCode.trim();
                sql = "select descr from gencodes  where fld_value =? AND mod_name = 'W_CR_BYPASS_CHK' AND  fld_Name = 'REAS_CODE'";
                pStmt = conn.prepareStatement(sql);
                pStmt.setString(1, reasCode);
                rs = pStmt.executeQuery();
                if (rs.next())
                {
                    descr = rs.getString(1);

                }
                rs.close();
                rs = null;
                pStmt.close();
                pStmt = null;

                descr = descr == null ? "" : descr.trim();
                System.out.println("description--"+descr);

                valueXmlString.append("<descr>").append(descr).append("</descr>\r\n");
				}
				
				
				valueXmlString.append("</Detail1 >");
				break;
			}
			valueXmlString.append("</Root>\r\n");
		} catch (Exception e) {
			System.out
					.println("Exception :CrLimitValid•@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@•••(Document,String):"
							+ e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
