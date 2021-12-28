package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;

import org.w3c.dom.*;

import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless
// added for ejb3
public class ChargeBackFormIc extends ValidatorEJB implements ChargeBackFormIcLocal, ChargeBackFormIcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String loginSite = "";

	/*
	 * public void ejbCreate() throws RemoteException, CreateException {
	 * System.out.println("ChargeBackLocEJB is in Process.........."); } public
	 * void ejbRemove() { } public void ejbActivate() { } public void
	 * ejbPassivate() { }
	 */
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		System.out.println("Validation Start..........");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : AssetRegisterICEJB : wfValData(String xmlString) : ==>\n" + e.getMessage());
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String columnValue = null;
		String childNodeName = null;
		String billNo = "";
		String tranId = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String errCode = null;
		String userId = null;
		String custCode = null;
		String itemCode = "";
		String empCode = "";
		String quantity = "";
		String saleQty = "";
		String saleRetQty = "";
		String unconfQty = "";
		String confQty = "";
		String tranDate = "",errorType = "";
		int cnt = 0;
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pStmt = null,pstmt=null;
		ResultSet rs = null;
		String sql = null;
		String statSal = "";
		ConnDriver connDriver = new ConnDriver();
		String lineNoDet = null;
		String updateFlag = null;
		String siteCode = "";
		String prdCode = "",frDateStr="",toDateStr="";
		java.sql.Timestamp tranDateTs = null, tempTst = null,frDate=null,toDate=null;
		Date bllDate = null;
		String blDate="";
		
		System.out.println("IN VALIDATION wfValData()");
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		
		try
		{
			System.out.println("wfValData called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo)
			{
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
					if (childNodeName.equalsIgnoreCase("cust_code"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("cust_code", "VMCUSTNUL", userId);
//							break;
//							return errString;
							errList.add("VMCUSTNUL");
							errFields.add(childNodeName.toLowerCase());
						} else
						{
							custCode = genericUtility.getColumnValue("cust_code", dom, "1");

							sql = " SELECT COUNT(*) FROM customer WHERE cust_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, custCode.trim());
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
								errCode = "VMINVCUST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
//								errString = getErrorString("cust_code", errCode, userId);
//								break;
//								return errString;
							}
							
						}
					}
					if (childNodeName.equalsIgnoreCase("tran_date"))
					{
						tranDate = genericUtility.getColumnValue("tran_date", dom, "1");
						tranDateTs = Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

						sql = "Select code from period where ? between fr_date and to_date ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setTimestamp(1, tranDateTs);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							prdCode = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						if (prdCode != null && prdCode.trim().length() > 0)
						{
							sql = "Select count(1) from period_stat where prd_code = ? " + " AND site_code = ? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, prdCode.trim());
							pStmt.setString(2, loginSite.trim());
							rs = pStmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
							if (cnt > 0)
							{
								sql = "Select stat_sal from period_stat where prd_code = ? and site_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, prdCode.trim());
								pStmt.setString(2, loginSite.trim());
								rs = pStmt.executeQuery();
								if (rs.next())
								{
									statSal = rs.getString(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;

								if (statSal != null && statSal.trim().equalsIgnoreCase("N"))
								{
//									errString = itmDBAccessEJB.getErrorString("", "VMSTATSNVL", userId);
//									break;
//									return errString;
									errList.add("VMSTATSNVL");
									errFields.add(childNodeName.toLowerCase());
								}
							} else
							{
//								errString = itmDBAccessEJB.getErrorString("", "VMSTATSND", userId);
//								break;
//								return errString;
								errList.add("VMSTATSND");
								errFields.add(childNodeName.toLowerCase());
							}
						} else
						{
//							errString = itmDBAccessEJB.getErrorString("", "VMPRDNTDF", userId);
//							break;
//							return errString;
							errList.add("VMSTATSND");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("tran_type"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("tran_type", "VMTRNTPNUL", userId);
//							break;
//							return errString;
							errList.add("VMTRNTPNUL");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("type"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("type", "VMSDRTPNUL", userId);
//							break;
//							return errString;
							errList.add("VMSDRTPNUL");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("item_ser"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("item_ser", "VMITMSRNUL", userId);
//							break;
//							return errString;
							errList.add("VMITMSRNUL");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("cust_code__credit"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("cust_code__credit", "VMCUSTNUL", userId);
//							break;
//							return errString;
							errList.add("VMCUSTNUL");
							errFields.add(childNodeName.toLowerCase());
						} else
						{
							custCode = genericUtility.getColumnValue("cust_code__credit", dom, "1");
							if (custCode != null && custCode.trim().length() > 0)
							{
								sql = " SELECT COUNT(*) FROM customer WHERE cust_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, custCode.trim());
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
									errCode = "VMINVCUST";
//									errString = getErrorString("cust_code__credit", errCode, userId);
//									break;
//									return errString;
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}								
							}
						}
					}
					// add by akhilesh 01-12 2011 (set employee
					// emp_fname,emp_mname,emp_lname basis of emp_code)
					if (childNodeName.equalsIgnoreCase("emp_code"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("emp_code", "VMECODENULL", userId);
//							break;
//							return errString;
							errList.add("VMECODENULL");
							errFields.add(childNodeName.toLowerCase());
						} else
						{
							empCode = genericUtility.getColumnValue("emp_code", dom, "1");
							if (empCode != null && empCode.trim().length() > 0)
							{
								sql = " SELECT COUNT(1) FROM employee WHERE emp_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, empCode.trim());
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
									errCode = "VMINVECODE";
//									errString = getErrorString("emp_code", errCode, userId);
//									break;
//									return errString;
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}								
							}
						}
					}//

					if (childNodeName.equalsIgnoreCase("site_code"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("site_code", "VMSITENUL", userId);
//							break;
//							return errString;
							errList.add("VMSITENUL");
							errFields.add(childNodeName.toLowerCase());
						} else
						{
							siteCode = genericUtility.getColumnValue("site_code", dom, "1");
							if (siteCode != null && siteCode.trim().length() > 0)
							{
								sql = " SELECT COUNT(1) FROM site WHERE site_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, siteCode.trim());
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
									errCode = "VMINVSITE";
//									errString = getErrorString("site_code", errCode, userId);
//									break;
//									return errString;
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}								
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("site_code__cr"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("site_code__cr", "VMSITENUL", userId);
//							break;
//							return errString;
							errList.add("VMSITENUL");
							errFields.add(childNodeName.toLowerCase());
						} else
						{
							siteCode = genericUtility.getColumnValue("site_code__cr", dom, "1");
							if (siteCode != null && siteCode.trim().length() > 0)
							{
								sql = " SELECT COUNT(1) FROM site WHERE site_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, siteCode.trim());
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
									errCode = "VMINVSITE";
//									errString = getErrorString("site_code__cr", errCode, userId);
//									break;
//									return errString;
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}								
							}
						}
					}
				}
				// END OF CASE1
				break;
			case 2:
				System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
				// String update_flag = null;
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("item_code"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("item_code", "VTITMNUL", userId);
//							break;
//							return errString;
							errList.add("VTITMNUL");
							errFields.add(childNodeName.toLowerCase());
						} 
						else
						{
							itemCode = genericUtility.getColumnValue("item_code", dom);
							if (itemCode != null & itemCode.trim().length() > 0)
							{
								sql = "SELECT count(*) from item" + " where item_code = ? ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, itemCode.trim());
								rs = pStmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								System.out.println(" COUNT =====> [" + cnt + "]");								
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
								if (cnt == 0)
								{
//									errString = itmDBAccessEJB.getErrorString("item_code", "VTINVITM", userId);
//									break;
//									return errString;
									errList.add("VTINVITM");
									errFields.add(childNodeName.toLowerCase());
								}								
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("lot_no"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("lot_no", "VTLOTNUL", userId);
//							break;
//							return errString;
							errList.add("VTLOTNUL");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("porder_no"))
					{
						billNo = genericUtility.getColumnValue("porder_no", dom);
						System.out.println("BILL no$$$$$$$$$$$ :- ["+billNo+"]");
//						if (childNode.getFirstChild() == null)
							if (billNo==null || billNo.trim().length()<=0)
							{
							System.out.println("PORDER_NO null found.........");
//							errString = itmDBAccessEJB.getErrorString("porder_no", "VTBILNONUL", userId);
//							break;
//							return errString;
							errList.add("VTBILNONUL");
							errFields.add(childNodeName.toLowerCase());
						} 
							else
						{
							System.out.println("PORDER_NO not null checking date .........");
							SimpleDateFormat sdf = null;
							sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());

							billNo = genericUtility.getColumnValue("porder_no", dom);
							custCode = genericUtility.getColumnValue("cust_code", dom1);
							tranId = genericUtility.getColumnValue("tran_id", dom1);							
//							blDate=genericUtility.getColumnValue("porder_date", dom);
							blDate=genericUtility.getColumnValue("tran_date", dom1);
							System.out.println("Tran date :- ["+blDate+"]");
							tempTst=Timestamp.valueOf(genericUtility.getValidDateString(blDate, genericUtility.getApplDateFormat(),
									genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("Bill Date is :- [" + tempTst + "]");

							sql = "select fr_date,to_date from acctprd where ?  between fr_date and to_date";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, tempTst);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								frDate = rs.getTimestamp("fr_date");
								toDate = rs.getTimestamp("to_date");
							}							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;		
							
							/**
							 * VALLABH KADAM
							 * Add condition in validation
							 * if porder_date between 
							 * current financial year [28/MAY/15] 
							 * */
//							sql = "Select count(1) from charge_back A, charge_back_det B where" 
//							+ " A.tran_id = B.tran_id " 
//									+ " and A.cust_code = ? "
//							+ " and B.porder_no = ? "
//									+ " and A.tran_id <> '" + tranId + "' ";
							sql = "Select count(1) from charge_back_form A, charge_back_form_det B where"
									+ " A.tran_id = B.tran_id"
									+ " and A.cust_code = ?"
									+ " and B.porder_no = ?"
									+ " AND B.porder_date between ? AND ?"
									+ " and A.tran_id <>'"+tranId+"'";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, custCode.trim());
							pStmt.setString(2, billNo.trim());
							pStmt.setTimestamp(3, frDate);
							pStmt.setTimestamp(4, toDate);;
							rs = pStmt.executeQuery();
							
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							pStmt.close();
							rs.close();
							System.out.println("Count of the DATE query :- ["+cnt+"]");
							if (cnt >= 1)
							{
//								errString = itmDBAccessEJB.getErrorString("porder_no", "VTBILNOPST", userId);
//								break;
//								return errString;
								errList.add("VTBILNOPST");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("porder_date"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("porder_date", "VTBILDTNUL", userId);
//							break;
//							return errString;
							errList.add("VTBILDTNUL");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					/*
					 * if ( childNodeName.equalsIgnoreCase( "discount_per" ) ) {
					 * if ( childNode.getFirstChild() == null ) { errString
					 * =itmDBAccessEJB
					 * .getErrorString("discount_per","VTDISPNUL",userId); break
					 * ; } }
					 */
					if (childNodeName.equalsIgnoreCase("item_ser"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("item_ser", "VTITMSRNUL", userId);
//							break;
//							return errString;
							errList.add("VTITMSRNUL");
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("quantity"))
					{
						if (childNode.getFirstChild() == null)
						{
//							errString = itmDBAccessEJB.getErrorString("quantity", "VTQUANTNUL", userId);
//							break;
//							return errString;
							errList.add("VTQUANTNUL");
							errFields.add(childNodeName.toLowerCase());
						} else
						{
							quantity = genericUtility.getColumnValue("quantity", dom);
							saleQty = genericUtility.getColumnValue("sale_qty", dom);
							saleRetQty = genericUtility.getColumnValue("sale_ret_qty", dom);
							unconfQty = genericUtility.getColumnValue("unconf_claimed", dom);
							confQty = genericUtility.getColumnValue("conf_claimed", dom);
							if (quantity == null || "null".equals(quantity))
							{
								quantity = "0";
							}
							if (saleQty == null || "null".equals(saleQty))
							{
								saleQty = "0";
							}
							if (saleRetQty == null || "null".equals(saleRetQty))
							{
								saleRetQty = "0";
							}
							if (unconfQty == null || "null".equals(unconfQty))
							{
								unconfQty = "0";
							}
							if (confQty == null || "null".equals(confQty))
							{
								confQty = "0";
							}
							double qtyDbl = Double.parseDouble(quantity);
							double saleQtyDbl = Double.parseDouble(saleQty);
							double saleRetQtyDbl = Double.parseDouble(saleRetQty);
							double unconfQtyDbl = Double.parseDouble(unconfQty);
							double confQtyDbl = Double.parseDouble(confQty);

							// 14/06/10 manoharan to consider lines other than
							// current in which
							// the same sales order line is used

							// end 14/06/10 manoharan

							if (qtyDbl > (saleQtyDbl - saleRetQtyDbl - unconfQtyDbl - confQtyDbl))
							{
								System.out.println("Fire worning *********");
//								errString = itmDBAccessEJB.getErrorString("quantity", "VTINVQUANT", userId);
//								break;
//								 return errString;
								errList.add("VTINVQUANT");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("rate__contr"))
					{
						if (childNode.getFirstChild() == null)
						{
							errString = itmDBAccessEJB.getErrorString("rate__contr", "VTRATCRNUL", userId,"",conn);
							break;
//							 return errString;
						} else
						{
							String rateSell = genericUtility.getColumnValue("rate__sell", dom);
							String rateContr = childNode.getFirstChild().getNodeValue();
							if (rateSell == null)
								rateSell = "0";
							if (rateContr == null)
								rateContr = "0";
							/*
							 * if( Double.parseDouble(rateContr) >
							 * Double.parseDouble(rateSell) ) { errString
							 * =itmDBAccessEJB
							 * .getErrorString("rate__contr","EDCSTRTINV"
							 * ,userId); break ; }
							 */
							if (Double.parseDouble(rateContr) < 0)
							{
//								errString = itmDBAccessEJB.getErrorString("rate__contr", "VTSTKBILRT", userId);
//								break;
//								return errString;
								errList.add("VTSTKBILRT");
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}// END FOR OF CASE2
				break;
			}// END SWITCH			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				System.out.println("Error list found");
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					System.out.println("errField .........." + errFldName);
					System.out.println("userId .........." + userId);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
						System.out.println("Error String lrngth :- ["+errString.length()+"]");
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
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
				System.out.println("No error found...");
				errStringXml = new StringBuffer("");
			}
			
		}// END TRY
		catch (Exception e)
		{
			System.out.println("Exception ::" + e);
			e.printStackTrace();
//			errCode = "VALEXCEP";
//			errString = getErrorString("", errCode, userId);
//			errList.add(errCode);
//			errFields.add(childNodeName.toLowerCase());
		} finally
		{
			try
			{
				if (conn != null)
				{
					if (pStmt != null)
					{
						pStmt.close();
						pStmt = null;
					}

					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d)
			{
				d.printStackTrace();
			}
			System.out.println(" < AssetRegisterIcEJB > CONNECTION IS CLOSED");
		}		
		errString = errStringXml.toString();
		System.out.println("ErrString ::" + errString);
		return errString;
	}// END OF VALIDATION
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
		finally
		{
			try
			{
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		System.out.println("Error Type ["+msgType+"]");
		return msgType;
	}
//	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
//	{
//		Document dom = null;
//		Document dom1 = null;
//		Document dom2 = null;
//		String valueXmlString = null;
//		try
//		{
//			System.out.println("xmlString" + xmlString);
//			dom = parseString(xmlString);
//			System.out.println("xmlString1" + xmlString1);
//			dom1 = parseString(xmlString1);
//
//			if (xmlString2.trim().length() > 0)
//			{
//				System.out.println("xmlString2" + xmlString2);
//				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
//			}
//			System.out.println("testing Itemchange");
//			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
//			System.out.println("valueXmlString----" + valueXmlString);
//		} catch (Exception e)
//		{
//			System.out.println("Exception : [AssetRegisterICEJB][itemChanged] :==>\n" + e.getMessage());
//		}
//		return valueXmlString;
//	}

//	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
//	{
//		Connection conn = null;
//		ConnDriver connDriver = new ConnDriver();
//		PreparedStatement pStmt = null;
//		ResultSet rs = null;
//		String sql = null;
//		int currentFormNo = 0;
//		StringBuffer valueXmlString = new StringBuffer();
//		String columnValue = null;
//		NodeList parentNodeList = null;
//		Node parentNode = null;
//		Node childNode = null;
//		NodeList childNodeList = null;
//		String childNodeName = null;
//		int childNodeListLength = 0;
//		int ctr = 0;
//		int cnt = 0;
//		String itemCode = "";
//		String siteCode = "";
//		String lotNo = "";
//		String rateSell = "";
//		String discPer = "";
//		String rateContr = "";
//		String tranId = "";
//		String lineNo = "";
//		String itmchgData = "";
//		String custName = "";
//		String priceList = "";
//		String itemSeries = "";
//		String firstName = "", middleName = "", lastName = "";
//		String chguserhdr = null;
//		String type = "";
//		String chgtermhdr = null;
//		String custCode = "";
//		SimpleDateFormat sdf = null;
//		String siteDescr = "";
//		String currCode = "";
//		String empCode = "";
//		String empFname = "";
//		String empMname = "";
//		String empLname = "";
//		String custCdEnd = "";
//		double exchRate = 0.0;
//		double saleQty = 0.0;
//		double discPerc = 0.0;
//		double saleRetQty = 0.0;
//		double confQty = 0.0, rate = 0.0, discPerUnit = 0.0;
//		double unconfQty = 0.0, rateSellDbl = 0.0, discPerDbl = 0.0, rateContrDbl = 0.0, curTranQty = 0;
//		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
//
//		try
//		{
//			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
//			conn = connDriver.getConnectDB("DriverITM");
//			connDriver = null;
//			if (objContext != null && objContext.trim().length() > 0)
//			{
//				currentFormNo = Integer.parseInt(objContext);
//			}
//
//			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
//			chguserhdr = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
//			chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
//
//			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
//			valueXmlString.append(editFlag).append("</editFlag></header>");
//
//			System.out.println("Current Form No [" + currentFormNo + "]");
//			switch (currentFormNo)
//			{
//			case 1:
//				valueXmlString.append("<Detail1>");
//				// SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
//				parentNodeList = dom.getElementsByTagName("Detail1");
//				parentNode = parentNodeList.item(0);
//				childNodeList = parentNode.getChildNodes();
//				ctr = 0;
//				childNodeListLength = childNodeList.getLength();
//				do
//				{
//					childNode = childNodeList.item(ctr);
//					childNodeName = childNode.getNodeName();
//					if (childNodeName.equals(currentColumn))
//					{
//						if (childNode.getFirstChild() != null)
//						{
//							columnValue = childNode.getFirstChild().getNodeValue().trim();
//						}
//					}
//					ctr++;
//				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
//				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
//				if (currentColumn.trim().equals("itm_default"))
//				{
//					if (loginSite != null || loginSite.trim().length() == 0)
//					{
//						sql = "select descr from site where site_code = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, loginSite);
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							siteDescr = rs.getString("descr");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						sql = "select curr_code from finent where fin_entity = ( Select fin_entity " + " from site where site_code = '" + loginSite + "')";
//						pStmt = conn.prepareStatement(sql);
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							currCode = rs.getString("curr_code");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//						if (currCode != null && currCode.trim().length() > 0)
//						{
//							sql = "select std_exrt from currency where curr_code = ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, currCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								exchRate = rs.getDouble("std_exrt");
//							}
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						}
//
//						valueXmlString.append("<tran_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</tran_date>");
//						valueXmlString.append("<eff_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</eff_date>");
//						valueXmlString.append("<site_code__log>").append("<![CDATA[" + (loginSite) + "]]>").append("</site_code__log>");
//						valueXmlString.append("<site_descr1>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr1>");
//						valueXmlString.append("<site_code>").append("<![CDATA[" + (loginSite) + "]]>").append("</site_code>");
//						valueXmlString.append("<site_descr>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr>");
//						valueXmlString.append("<site_code__cr>").append("<![CDATA[" + (loginSite) + "]]>").append("</site_code__cr>");
//						valueXmlString.append("<site_descr2>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr2>");
//						valueXmlString.append("<type>").append("R").append("</type>");
//						valueXmlString.append("<curr_code>").append("<![CDATA[" + (currCode) + "]]>").append("</curr_code>");
//						valueXmlString.append("<exch_rate>").append("<![CDATA[" + (exchRate) + "]]>").append("</exch_rate>");
//						// valueXmlString.append("<cust_code__end protect=\"1\" >").append("").append("</cust_code__end>");
//						valueXmlString.append("<chg_user>").append("<![CDATA[" + chguserhdr + "]]>").append("</chg_user>");
//						valueXmlString.append("<chg_term>").append("<![CDATA[" + chgtermhdr + "]]>").append("</chg_term>");
//					}
//				}
//				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
//				{
//					custCode = genericUtility.getColumnValue("cust_code__end", dom);
//					type = genericUtility.getColumnValue("type", dom);
//					if (type != null && custCode != null)
//					{
//						if (type.equals("R"))
//						{
//							sql = "select cust_name from customer " + " where cust_code = ? ";
//
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								custName = rs.getString("cust_name");
//							}
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						} else
//						{
//							sql = "Select first_name, middle_name, last_name from strg_customer " + " where sc_code = ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								firstName = rs.getString("first_name");
//								middleName = rs.getString("middle_name");
//								lastName = rs.getString("last_name");
//							}
//							custName = firstName + " " + middleName + " " + lastName;
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						}
//					}
//					valueXmlString.append("<cust_name_bat>").append("<![CDATA[" + (custName) + "]]>").append("</cust_name_bat>");
//					/*
//					 * String siteCodeEdit = getColumnValue("site_code",dom);
//					 * String itemGrpEdit = getColumnValue("item_grp",dom);
//					 * String tranId = genericUtility.getColumnValue( "tran_id",
//					 * dom ); sql = "SELECT count(1) from ASSET_SHIFTDET"
//					 * +" where tran_id = ? "; pStmt = conn.prepareStatement(
//					 * sql ); pStmt.setString(1,tranId); rs =
//					 * pStmt.executeQuery(); if( rs.next() ) { cnt = rs.getInt(
//					 * 1 ); } rs.close(); rs = null; pStmt.close(); pStmt =
//					 * null;
//					 * 
//					 * String grpDescr = null; sql =
//					 * "select fn_get_gen_descr('GRP_CODE','W_ITEM', ? ,'D') descr"
//					 * +" from dual"; pStmt = conn.prepareStatement( sql );
//					 * pStmt.setString(1, itemGrpEdit != null ?
//					 * itemGrpEdit.trim() : "" ); rs = pStmt.executeQuery(); if(
//					 * rs.next() ) { grpDescr = rs.getString( 1 ); } if( cnt ==
//					 * 0 ) {
//					 * valueXmlString.append("<site_code>").append("<![CDATA[" +
//					 * ( siteCodeEdit != null ? siteCodeEdit.trim() : "" )+
//					 * "]]>").append("</site_code>");
//					 * valueXmlString.append("<item_grp>").append("<![CDATA[" +
//					 * ( itemGrpEdit != null ? itemGrpEdit.trim() : "" )+
//					 * "]]>").append("</item_grp>");
//					 * valueXmlString.append("<grp_descr>").append("<![CDATA[" +
//					 * ( grpDescr != null ? grpDescr.trim() : "" )+
//					 * "]]>").append("</grp_descr>"); } else {
//					 * valueXmlString.append
//					 * ("<site_code protect=\"1\" >").append
//					 * ("<![CDATA["+siteCodeEdit+"]]>").append("</site_code>");
//					 * valueXmlString
//					 * .append("<item_grp protect=\"1\" >").append(
//					 * "<![CDATA["+itemGrpEdit+"]]>").append("</item_grp>");
//					 * valueXmlString.append("<grp_descr>").append("<![CDATA[" +
//					 * ( grpDescr != null ? grpDescr.trim() : "" )+
//					 * "]]>").append("</grp_descr>"); }
//					 */
//				}
//
//				if (currentColumn.trim().equals("cust_code"))
//				{
//					custCode = genericUtility.getColumnValue("cust_code", dom);
//					// 15/05/10 manoharan site code should be credit site code
//					siteCode = genericUtility.getColumnValue("site_code__cr", dom);
//					// end 15/05/10 manoharan site code should be credit site
//					// code
//					if (custCode != null && custCode.trim().length() > 0)
//					{
//						sql = "select cust_name from customer " + " where cust_code = ? ";
//
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							custName = rs.getString("cust_name");
//							System.out.println("custName-----------" + custName);
//						}
//
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						sql = "select price_list from site_customer where site_code = ? and cust_code = ?";
//						pStmt = conn.prepareStatement(sql);
//						// 15/05/10 manoharan site code should be credit site
//						// code
//						// pStmt.setString(1,loginSite.trim());
//						pStmt.setString(1, siteCode.trim());
//						// end 15/05/10 manoharan site code should be credit
//						// site code
//						pStmt.setString(2, custCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							priceList = rs.getString("price_list");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//						if (priceList == null || priceList.trim().length() == 0)
//						{
//							sql = "Select price_list from customer where cust_code = ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								priceList = rs.getString("price_list");
//							}
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						}
//
//						sql = "SELECT item_ser__inv FROM customer_series where cust_code = ? " + " AND item_ser__inv IS NOT NULL ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							itemSeries = rs.getString("item_ser__inv");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						if (itemSeries == null || itemSeries.trim().length() == 0)
//						{
//							sql = "SELECT item_ser FROM customer_series where cust_code = ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								itemSeries = rs.getString("item_ser");
//							}
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						}
//					}
//					valueXmlString.append("<cust_name>").append("<![CDATA[" + (custName) + "]]>").append("</cust_name>");
//					valueXmlString.append("<cust_code__credit>").append("<![CDATA[" + (custCode != null ? custCode.trim() : "") + "]]>").append("</cust_code__credit>");
//					valueXmlString.append("<cust_name__credit>").append("<![CDATA[" + (custName) + "]]>").append("</cust_name__credit>");
//					valueXmlString.append("<price_list>").append("<![CDATA[" + (priceList == null ? "" : priceList) + "]]>").append("</price_list>");
//					valueXmlString.append("<item_ser>").append("<![CDATA[" + (itemSeries) + "]]>").append("</item_ser>");
//				}
//				if (currentColumn.trim().equals("type"))
//				{
//					type = genericUtility.getColumnValue("type", dom);
//					valueXmlString.append("<cust_code__end>").append("").append("</cust_code__end>");
//					valueXmlString.append("<cust_name_bat>").append("").append("</cust_name_bat>");
//					/*
//					 * if( type != null ) {
//					 * valueXmlString.append("<cust_code__end protect=\"0\" >"
//					 * ).append("").append("</cust_code__end>"); } else {
//					 * valueXmlString
//					 * .append("<cust_code__end protect=\"1\" >").append
//					 * ("").append("</cust_code__end>"); }
//					 */
//				}
//				if (currentColumn.trim().equals("site_code"))
//				{
//					siteCode = genericUtility.getColumnValue("site_code", dom);
//					System.out.println("site_code is==>" + siteCode);
//					/*
//					 * siteCode = genericUtility.getColumnValue( "site_code",
//					 * dom, "1" );
//					 * System.out.println("site_code is==>"+siteCode); siteCode
//					 * = childNode.getFirstChild().getNodeValue().trim();
//					 * System.out.println("site_code is==>"+siteCode);
//					 */
//					if (siteCode != null && siteCode.trim().length() > 0)
//					{
//						sql = "select descr from site where site_code = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, siteCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							siteDescr = rs.getString("descr");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//					}
//					valueXmlString.append("<site_descr>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr>");
//					valueXmlString.append("<site_code__cr>").append("<![CDATA[" + (siteCode) + "]]>").append("</site_code__cr>");
//					valueXmlString.append("<site_descr2>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr2>");
//				}
//				if (currentColumn.trim().equals("site_code__log"))
//				{
//					siteCode = genericUtility.getColumnValue("site_code__log", dom);
//					System.out.println("site_code is==>" + siteCode);
//					/*
//					 * siteCode = genericUtility.getColumnValue( "site_code",
//					 * dom, "1" );
//					 * System.out.println("site_code is==>"+siteCode); siteCode
//					 * = childNode.getFirstChild().getNodeValue().trim();
//					 * System.out.println("site_code is==>"+siteCode);
//					 */
//					if (siteCode != null && siteCode.trim().length() > 0)
//					{
//						sql = "select descr from site where site_code = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, siteCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							siteDescr = rs.getString("descr");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//					}
//					valueXmlString.append("<site_descr1>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr1>");
//				}
//				if (currentColumn.trim().equals("site_code__cr"))
//				{
//					siteCode = genericUtility.getColumnValue("site_code__cr", dom);
//					System.out.println("site_code__cr is==>" + siteCode);
//					/*
//					 * siteCode = genericUtility.getColumnValue(
//					 * "site_code__cr", dom, "1" );
//					 * System.out.println("site_code__cr is==>"+siteCode);
//					 * siteCode =
//					 * childNode.getFirstChild().getNodeValue().trim();
//					 * System.out.println("site_code__cr is==>"+siteCode);
//					 */
//					if (siteCode != null && siteCode.trim().length() > 0)
//					{
//						sql = "select descr from site where site_code = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, siteCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							siteDescr = rs.getString("descr");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//					}
//					valueXmlString.append("<site_descr2>").append("<![CDATA[" + (siteDescr) + "]]>").append("</site_descr2>");
//					// 15/05/10 manoharan site code should be credit site code
//					custCode = genericUtility.getColumnValue("cust_code", dom);
//					// end 15/05/10 manoharan site code should be credit site
//					// code
//					if (custCode != null && custCode.trim().length() > 0)
//					{
//
//						sql = "select price_list from site_customer where site_code = ? and cust_code = ?";
//						pStmt = conn.prepareStatement(sql);
//						// 15/05/10 manoharan site code should be credit site
//						// code
//						pStmt.setString(1, siteCode.trim());
//						pStmt.setString(2, custCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							priceList = rs.getString("price_list");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//						if (priceList == null || priceList.trim().length() == 0)
//						{
//							sql = "Select price_list from customer where cust_code = ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								priceList = rs.getString("price_list");
//							}
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						}
//						valueXmlString.append("<price_list>").append("<![CDATA[" + (priceList) + "]]>").append("</price_list>");
//					}
//					// end 15/05/10 manoharan site code should be credit site
//					// code
//				}
//				if (currentColumn.trim().equals("cust_code__credit"))
//				{
//					custCode = genericUtility.getColumnValue("cust_code__credit", dom);
//					if (custCode != null && custCode.trim().length() > 0)
//					{
//						sql = "select cust_name from customer " + " where cust_code = ? ";
//
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							custName = rs.getString("cust_name");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//					}
//					valueXmlString.append("<cust_name__credit>").append("<![CDATA[" + (custName) + "]]>").append("</cust_name__credit>");
//				}
//				// add by akhilesh 01-12-2011 item change again of emp_code
//				if (currentColumn.trim().equals("emp_code"))
//				{
//					System.out.println("Entered in EmpCode tem Change");
//					empCode = genericUtility.getColumnValue("emp_code", dom);
//					System.out.println("empCode----" + empCode);
//					if (empCode != null && empCode.trim().length() > 0)
//					{
//						sql = "select emp_fname,emp_mname,emp_lname from employee " + " where emp_code = ? ";
//
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, empCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							System.out.println("empFname---" + empFname);
//							empFname = rs.getString("emp_fname");
//							empMname = rs.getString("emp_mname");
//							empLname = rs.getString("emp_lname");
//						}
//						valueXmlString.append("<emp_fname>").append("<![CDATA[" + (empFname) + "]]>").append("</emp_fname>");
//						valueXmlString.append("<emp_mname>").append("<![CDATA[" + (empMname) + "]]>").append("</emp_mname>");
//						valueXmlString.append("<emp_lname>").append("<![CDATA[" + (empLname) + "]]>").append("</emp_lname>");
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//					}
//
//				}
//
//				if (currentColumn.trim().equals("cust_code__end"))
//				{
//					custCode = genericUtility.getColumnValue("cust_code__end", dom);
//					type = genericUtility.getColumnValue("type", dom);
//					if (type != null)
//					{
//						if (type.equals("R"))
//						{
//							sql = "select cust_name from customer " + " where cust_code = ? ";
//
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								custName = rs.getString("cust_name");
//							}
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						} else
//						{
//							sql = "Select first_name, middle_name, last_name from strg_customer " + " where sc_code = ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, custCode.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								firstName = rs.getString("first_name");
//								middleName = rs.getString("middle_name");
//								lastName = rs.getString("last_name");
//							}
//							custName = firstName + " " + middleName + " " + lastName;
//							rs.close();
//							rs = null;
//							pStmt.close();
//							pStmt = null;
//						}
//					}
//					valueXmlString.append("<cust_name_bat>").append("<![CDATA[" + (custName) + "]]>").append("</cust_name_bat>");
//				}
//				if (currentColumn.trim().equals("curr_code"))
//				{
//					currCode = genericUtility.getColumnValue("curr_code", dom);
//					if (currCode != null && currCode.trim().length() > 0)
//					{
//						sql = "select std_exrt from currency where curr_code = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, currCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							exchRate = rs.getDouble("std_exrt");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//					}
//					valueXmlString.append("<exch_rate>").append("<![CDATA[" + (exchRate) + "]]>").append("</exch_rate>");
//				}
//
//				valueXmlString.append("</Detail1>");
//				valueXmlString.append("</Root>");
//				break;
//			// /////////////
//			case 2:
//				valueXmlString.append("<Detail2>");
//				// SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
//				parentNodeList = dom.getElementsByTagName("Detail2");
//				parentNode = parentNodeList.item(0);
//				childNodeList = parentNode.getChildNodes();
//				ctr = 0;
//				childNodeListLength = childNodeList.getLength();
//				do
//				{
//					childNode = childNodeList.item(ctr);
//					childNodeName = childNode.getNodeName();
//					if (childNodeName.equals(currentColumn))
//					{
//						if (childNode.getFirstChild() != null)
//						{
//							columnValue = childNode.getFirstChild().getNodeValue().trim();
//						}
//					}
//					ctr++;
//				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
//				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
//				if (currentColumn.trim().equals("itm_default"))
//				{
//					itemSeries = genericUtility.getColumnValue("item_ser", dom1);
//					custCdEnd = genericUtility.getColumnValue("cust_code__end", dom1);
//					type = genericUtility.getColumnValue("type", dom1);
//					if (type != null && custCdEnd != null)
//					{
//						if (type.equals("S"))
//						{
//							sql = "select disc_perc from disc_apr_strg where item_ser = ? " + " and	sc_code	= ? " + " and ? between eff_from and valid_upto ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, itemSeries.trim());
//							pStmt.setString(2, custCdEnd.trim());
//							pStmt.setTimestamp(3, getCurrdateTsFormat());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								discPerc = rs.getDouble("disc_perc");
//							}
//						} else
//						{
//							sql = "select disc_perc from customer_series where item_ser = ? " + " and	cust_code	= ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, itemSeries.trim());
//							pStmt.setString(2, custCdEnd.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								discPerc = rs.getDouble("disc_perc");
//							}
//						}
//						valueXmlString.append("<discount_per>").append("<![CDATA[" + discPerc + "]]>").append("</discount_per>");
//					}
//					valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSeries != null ? itemSeries : "" + "]]>").append("</item_ser>");
//					// ///////////////////////////////////////////////////////////////////////////////////////////////////
//					// 27/05/10 manoharan bill_no, bill_date and discount to be
//					// carried forward
//					NodeList detail2List = null;
//					Node detailNode = null;
//					NodeList childDetilList = null;
//					Node chidDetailNode = null;
//
//					detail2List = dom2.getElementsByTagName("Detail2");
//					itemCode = genericUtility.getColumnValue("item_code", dom);
//					lineNo = genericUtility.getColumnValue("line_no", dom);
//					// System.out.println("lineNo in str " + lineNo);
//					// System.out.println("lineNo after trim " + lineNo.trim());
//					if (detail2List.getLength() > 1)
//					{
//						System.out.println("Not 1st detail duplicating..................... ");
//						detailNode = detail2List.item(detail2List.getLength() - 2);
//						childDetilList = detailNode.getChildNodes();
//						String sDiscPer = "0", sDiscPerUnit = "0", porderNo = null, porderDate = null;
//						for (int p = 0; p < childDetilList.getLength(); p++)
//						{
//							chidDetailNode = childDetilList.item(p);
//							// System.out.println("current child node>>>>>>>>>> "
//							// + chidDetailNode.getNodeName() );
//							if (chidDetailNode.getNodeName().equalsIgnoreCase("discount_per"))
//							{
//								// System.out.println("discount_per node found >>>>>"
//								// + chidDetailNode.getNodeName());
//								if (chidDetailNode.getFirstChild() != null)
//								{
//									sDiscPer = chidDetailNode.getFirstChild().getNodeValue();
//									if (sDiscPer == null || sDiscPer.trim().length() == 0)
//									{
//										sDiscPer = "0";
//									}
//								}
//							}
//							if (chidDetailNode.getNodeName().equalsIgnoreCase("discount_per_unit"))
//							{
//								// System.out.println("discount_per_unit node found >>>>>"
//								// + chidDetailNode.getNodeName());
//								if (chidDetailNode.getFirstChild() != null)
//								{
//									sDiscPerUnit = chidDetailNode.getFirstChild().getNodeValue();
//									if (sDiscPerUnit == null || sDiscPerUnit.trim().length() == 0)
//									{
//										sDiscPerUnit = "0";
//									}
//								}
//							}
//							if (chidDetailNode.getNodeName().equalsIgnoreCase("porder_no"))
//							{
//								// System.out.println("porder_no node found >>>>>"
//								// + chidDetailNode.getNodeName());
//								if (chidDetailNode.getFirstChild() != null)
//								{
//									porderNo = chidDetailNode.getFirstChild().getNodeValue();
//									if (porderNo == null || porderNo.trim().length() == 0)
//									{
//										porderNo = " ";
//									}
//								}
//							}
//							if (chidDetailNode.getNodeName().equalsIgnoreCase("porder_date"))
//							{
//								// System.out.println("porder_date node found >>>>>"
//								// + chidDetailNode.getNodeName());
//								if (chidDetailNode.getFirstChild() != null)
//								{
//									porderDate = chidDetailNode.getFirstChild().getNodeValue();
//									if (porderDate == null || porderDate.trim().length() == 0)
//									{
//										porderDate = " ";
//									}
//								}
//							}
//							// append the values in current detail
//						}
//						valueXmlString.append("<discount_per>").append("<![CDATA[" + (sDiscPer) + "]]>").append("</discount_per>");
//						valueXmlString.append("<discount_per_unit>").append("<![CDATA[" + (sDiscPerUnit) + "]]>").append("</discount_per_unit>");
//						if (porderNo != null && !"null".equals(porderNo) && porderNo.trim().length() > 0)
//						{
//							valueXmlString.append("<porder_no>").append("<![CDATA[" + (porderNo) + "]]>").append("</porder_no>");
//						}
//
//						if (porderDate != null && !"null".equals(porderDate) && porderDate.trim().length() > 0)
//						{
//							valueXmlString.append("<porder_date>").append("<![CDATA[" + (porderDate) + "]]>").append("</porder_date>");
//						}
//
//					}
//					// ///////////////////////////////////////////////////////////////////////////////////////////////////
//				}
//				// if (currentColumn.trim().equals( "itm_defaultedit" ))
//				if (currentColumn.trim().equals("lot_no"))
//				{
//					itemCode = genericUtility.getColumnValue("item_code", dom);
//					siteCode = genericUtility.getColumnValue("site_code", dom1);
//					custCode = genericUtility.getColumnValue("cust_code", dom1);
//					lotNo = genericUtility.getColumnValue("lot_no", dom);
//					rateSell = genericUtility.getColumnValue("rate__sell", dom);
//					discPer = genericUtility.getColumnValue("discount_per", dom);
//					rateContr = genericUtility.getColumnValue("rate__contr", dom);
//					tranId = genericUtility.getColumnValue("tran_id", dom);
//					lineNo = genericUtility.getColumnValue("line_no", dom);
//					String itemDescr = "";
//					String itemSer = "";
//
//					if (itemCode != null && itemCode.trim().length() > 0)
//					{
//						sql = "select DESCR, item_ser from item " + "where item_code = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, itemCode.trim());
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							itemDescr = rs.getString("DESCR");
//							itemSer = rs.getString("item_ser");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//					}
//					valueXmlString.append("<item_descr>").append("<![CDATA[" + (itemDescr) + "]]>").append("</item_descr>");
//					valueXmlString.append("<item_ser>").append("<![CDATA[" + (itemSer) + "]]>").append("</item_ser>");
//					if (lotNo != null && lotNo.trim().length() > 0 && itemCode != null && itemCode.trim().length() > 0)
//					{
//						sql = "select sum(b.quantity) as saleQty from invoice a, invoice_trace b " + " where a.invoice_id = b.invoice_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.tran_date <= ? " + " and	b.item_code = ? " + " and	b.lot_no = ? " + " and	a.confirmed = 'Y'";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode);
//						pStmt.setString(2, siteCode);
//						pStmt.setTimestamp(3, getCurrdateTsFormat());
//						pStmt.setString(4, itemCode);
//						pStmt.setString(5, lotNo);
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							saleQty = rs.getDouble("saleQty");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						sql = "select sum(case when B.ret_rep_flag = 'R' then b.quantity else -b.quantity end) as saleRetQty " + " from sreturn a, sreturndet b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.tran_date <= ? " + " and	b.item_code = ? " + " and   b.lot_no 	= ? " + " and	a.confirmed = 'Y'";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode);
//						pStmt.setString(2, siteCode);
//						pStmt.setTimestamp(3, getCurrdateTsFormat());
//						pStmt.setString(4, itemCode);
//						pStmt.setString(5, lotNo);
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							saleRetQty = rs.getDouble("saleRetQty");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						sql = "select sum(case when b.quantity is null then 0 else b.quantity end) as confQty " + " from charge_back a, charge_back_det b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.confirmed = 'Y' " + " and	b.item_code = ? " + " and   b.lot_no 	= ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode);
//						pStmt.setString(2, siteCode);
//						pStmt.setString(3, itemCode);
//						pStmt.setString(4, lotNo);
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							confQty = rs.getDouble("confQty");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						sql = "select sum(case when b.quantity is null then 0 else b.quantity end) as unconfQty " + " from charge_back a, charge_back_det b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	(case when a.confirmed is null then 'N' else a.confirmed end)  = 'N' " + " and	b.item_code = ? " + " and   b.lot_no 	= ? " + " and a.tran_id <> ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, custCode);
//						pStmt.setString(2, siteCode);
//						pStmt.setString(3, itemCode);
//						pStmt.setString(4, lotNo);
//						// 20/07/10 manoharan initialisation done
//						if (tranId == null)
//						{
//							tranId = "@@@";
//						}
//						// end 20/07/10 manoharan initialisation done
//						pStmt.setString(5, tranId);
//						rs = pStmt.executeQuery();
//						if (rs.next())
//						{
//							unconfQty = rs.getDouble("unconfQty");
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//
//						/*
//						 * sql = "select ('"+unconfQty+
//						 * "' - (case when b.quantity is null then 0 else b.quantity end)) as unconfQty "
//						 * +" from charge_back a, charge_back_det b "
//						 * +" where a.tran_id = b.tran_id "
//						 * +" and	b.tran_id = ? " +"	and	b.line_no = ? "
//						 * +"	and	a.cust_code = ? " +" and	a.site_code = ? " +
//						 * "	and	(case when a.confirmed is null then 'N' else a.confirmed end)  = 'N' "
//						 * +"	and	b.item_code = ? " +"	and   b.lot_no 	= ? ";
//						 * pStmt = conn.prepareStatement(sql);
//						 * pStmt.setString(1, tranId); pStmt.setString(2,
//						 * lineNo); pStmt.setString(2, custCode);
//						 * pStmt.setString(3, siteCode); pStmt.setString(4,
//						 * itemCode); pStmt.setString(5, lotNo); rs =
//						 * pStmt.executeQuery(); if( rs.next() ) { unconfQty =
//						 * rs.getDouble("unconfQty"); } rs.close(); rs = null;
//						 * pStmt.close(); pStmt = null;
//						 */
//
//						// saleRetQty = Math.abs(saleRetQty);
//						// 20/07/10 manoharan consider quantity entered for the
//						// same paramter other than the current line
//						System.out.println("manohar unconfQty before [" + unconfQty + "]");
//						unconfQty = unconfQty + getCurTranQty(lineNo, itemCode, lotNo, dom2);
//						System.out.println("manohar unconfQty after [" + unconfQty + "]");
//						// end 20/07/10 manoharan consider quantity entered for
//						// the same paramter other than the current line
//						valueXmlString.append("<sale_qty>").append("<![CDATA[" + saleQty + "]]>").append("</sale_qty>");
//						valueXmlString.append("<sale_ret_qty>").append("<![CDATA[" + saleRetQty + "]]>").append("</sale_ret_qty>");
//						valueXmlString.append("<conf_claimed>").append("<![CDATA[" + confQty + "]]>").append("</conf_claimed>");
//						valueXmlString.append("<unconf_claimed>").append("<![CDATA[" + unconfQty + "]]>").append("</unconf_claimed>");
//						rate = getMinRate(dom, dom1, "lot_no", conn);
//						// saleRetQty = Math.abs(saleRetQty);
//						valueXmlString.append("<rate__sell>").append("<![CDATA[" + rate + "]]>").append("</rate__sell>");
//					}
//
//					if (rateSell != null && rateSell.trim().length() > 0)
//						rateSellDbl = Double.parseDouble(rateSell);
//
//					if (discPer != null && discPer.trim().length() > 0)
//						discPerDbl = Double.parseDouble(discPer);
//
//					if (rateContr != null && rateContr.trim().length() > 0)
//						rateContrDbl = Double.parseDouble(rateContr);
//
//					if (rateContrDbl <= 0)
//						rate = rateSellDbl;
//					else
//						rate = Math.min(rateContrDbl, rateSellDbl);
//
//					discPerUnit = (rate * discPerDbl) / 100;
//
//					valueXmlString.append("<discount_per_unit>").append("<![CDATA[" + discPerUnit + "]]>").append("</discount_per_unit>");
//				}
//				if (currentColumn.trim().equals("item_code"))
//				{
//					itmchgData = getItemChgdata(dom, dom1, dom2, conn); // 14/06/10
//																		// manoharan
//																		// dom2
//																		// added
//					valueXmlString.append(itmchgData);
//				}
//
//				/*
//				 * if (currentColumn.trim().equals("lot_no")) { itmchgData =
//				 * getItemChgdata( dom, dom1, dom2, conn ); // 14/06/10
//				 * manoharan dom2 added valueXmlString.append( itmchgData ); }
//				 */
//
//				if (currentColumn.trim().equals("quantity"))
//				{
//					itmchgData = getCalculations(dom);
//					valueXmlString.append(itmchgData);
//				}
//				if (currentColumn.trim().equals("discount_per"))
//				{
//					itmchgData = getCalculations(dom);
//					valueXmlString.append(itmchgData);
//				}
//				if (currentColumn.trim().equals("rate__sell"))
//				{
//					itmchgData = getCalculations(dom);
//					valueXmlString.append(itmchgData);
//				}
//				if (currentColumn.trim().equals("rate__contr"))
//				{
//					itmchgData = getCalculations(dom);
//					valueXmlString.append(itmchgData);
//				}
//				if (currentColumn.trim().equals("item_ser"))
//				{
//					itemSeries = genericUtility.getColumnValue("item_ser", dom1);
//					custCdEnd = genericUtility.getColumnValue("cust_code__end", dom1);
//					type = genericUtility.getColumnValue("type", dom1);
//					if (type != null && custCdEnd != null)
//					{
//						if (type.equals("S"))
//						{
//							sql = "select disc_perc from disc_apr_strg where item_ser = ? " + " and	sc_code	= ? " + " and ? between eff_from and valid_upto ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, itemSeries.trim());
//							pStmt.setString(2, custCdEnd.trim());
//							pStmt.setTimestamp(3, getCurrdateTsFormat());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								discPerc = rs.getDouble("disc_perc");
//							}
//
//						} else
//						{
//							sql = "select disc_perc from customer_series where item_ser = ? " + " and	cust_code	= ? ";
//							pStmt = conn.prepareStatement(sql);
//							pStmt.setString(1, itemSeries.trim());
//							pStmt.setString(2, custCdEnd.trim());
//							rs = pStmt.executeQuery();
//							if (rs.next())
//							{
//								discPerc = rs.getDouble("disc_perc");
//							}
//						}
//						rs.close();
//						rs = null;
//						pStmt.close();
//						pStmt = null;
//						valueXmlString.append("<discount_per>").append("<![CDATA[" + discPerc + "]]>").append("</discount_per>");
//					}
//				}
//
//				valueXmlString.append("</Detail2>");
//				valueXmlString.append("</Root>");
//				// //////////////
//			}// END OF TRY
//		} catch (Exception e)
//		{
//			System.out.println("Exception ::" + e.getMessage());
//			e.printStackTrace();
//			throw new ITMException(e);
//		} finally
//		{
//			try
//			{
//				if (rs != null)
//				{
//					rs.close();
//					rs = null;
//				}
//				if (pStmt != null)
//				{
//					pStmt.close();
//					pStmt = null;
//				}
//				if (conn != null)
//				{
//					conn.close();
//					conn = null;
//				}
//			} catch (Exception e)
//			{
//				System.out.println("Exception ::" + e);
//				e.printStackTrace();
//				throw new ITMException(e);
//			}
//		}
//		return valueXmlString.toString();
//	}// END OF ITEMCHANGE

//	private String getCurrdateAppFormat() throws Exception, ITMException
//	{
//		String s = "";
//		GenericUtility genericUtility = GenericUtility.getInstance();
//		try
//		{
//			java.util.Date date = null;
//			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//
//			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
//			date = simpledateformat.parse(timestamp.toString());
//			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
//			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
//		} catch (Exception exception)
//		{
//			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
//			throw new ITMException(exception);
//		}
//		return s;
//	}

//	private Timestamp getCurrdateTsFormat() throws Exception, ITMException
//	{
//		String s = "";
//		Timestamp timestamp = null;
//		GenericUtility genericUtility = GenericUtility.getInstance();
//		try
//		{
//			java.util.Date date = null;
//			timestamp = new Timestamp(System.currentTimeMillis());
//
//			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
//			date = simpledateformat.parse(timestamp.toString());
//			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
//		} catch (Exception exception)
//		{
//			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
//			throw new ITMException(exception);
//		}
//		return timestamp;
//	}

//	private String getNodeValue(Node currDet, String fldName, boolean isAttribute)
//	{
//		String fldValue = null;
//		boolean isFound = false;
//		NodeList currNodes = currDet.getChildNodes();
//		int currDetLen = currNodes.getLength();
//		for (int detIdx = 0; detIdx < currDetLen && !isFound; detIdx++)
//		{
//			Node currNode = currNodes.item(detIdx);
//			String nodeName = currNode.getNodeName();
//
//			if (isAttribute == true)
//			{
//				if (nodeName.equalsIgnoreCase("attribute"))
//				{
//					fldValue = currNode.getAttributes().getNamedItem(fldName).getNodeValue();
//					isFound = true;
//				}
//			} else if (currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase(fldName))
//			{
//				fldValue = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : null;
//				isFound = true;
//			}
//		}
//		return fldValue;
//	}

//	private String getItemChgdata(Document dom, Document dom1, Document dom2, Connection conn) throws RemoteException, ITMException
//	{
//		StringBuffer itmChangeString = new StringBuffer("");
//		String sql = "";
//		ResultSet rs = null;
//		PreparedStatement pStmt = null;
//		String tempQty = "0", tempLotNo = "", tempItemCode = "", tempLineNo;
//		double dTempQty = 0;
//		try
//		{
//			String itemDescr = "";
//			String itemSer = "";
//			double discPerc = 0.0;
//			double rate = 0.0;
//			double saleQty = 0.0;
//			double saleRetQty = 0.0;
//			double confQty = 0.0;
//			double unconfQty = 0.0;
//			java.sql.Timestamp tranDateTs = null;
//			String itemCode = genericUtility.getColumnValue("item_code", dom);
//			String lotNo = genericUtility.getColumnValue("lot_no", dom);
//			String rateSell = genericUtility.getColumnValue("rate__sell", dom);
//			String discPerStr = genericUtility.getColumnValue("discount_per", dom);
//			String tranId = genericUtility.getColumnValue("tran_id", dom);
//			String tranDate = genericUtility.getColumnValue("tran_date", dom1);
//			String lineNo = genericUtility.getColumnValue("line_no", dom);
//			String siteCode = genericUtility.getColumnValue("site_code", dom1);
//			String siteCodeCr = genericUtility.getColumnValue("site_code__cr", dom1);
//			String custCode = genericUtility.getColumnValue("cust_code", dom1);
//			String custCdEnd = genericUtility.getColumnValue("cust_code__end", dom1);
//			String type = genericUtility.getColumnValue("type", dom1);
//			String priceList = genericUtility.getColumnValue("price_list", dom1);
//			System.out.println("tranDate++>" + tranDate);
//			tranDateTs = Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
//			System.out.println("tranDateTs++>" + tranDateTs);
//			if (itemCode != null && itemCode.trim().length() > 0)
//			{
//				sql = "select DESCR, item_ser from item " + "where item_code = ? ";
//				pStmt = conn.prepareStatement(sql);
//				pStmt.setString(1, itemCode.trim());
//				rs = pStmt.executeQuery();
//				if (rs.next())
//				{
//					itemDescr = rs.getString("DESCR");
//					itemSer = rs.getString("item_ser");
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//				itmChangeString.append("<item_descr>").append("<![CDATA[" + (itemDescr) + "]]>").append("</item_descr>");
//				itmChangeString.append("<item_ser>").append("<![CDATA[" + (itemSer) + "]]>").append("</item_ser>");
//			}
//			// itmChangeString.append("<item_descr>").append("<![CDATA[" + (
//			// itemDescr )+ "]]>").append("</item_descr>");
//			// itmChangeString.append("<item_ser>").append("<![CDATA[" + (
//			// itemSer )+ "]]>").append("</item_ser>");
//
//			if (type != null && custCdEnd != null && itemSer != null && (discPerStr == null || discPerStr.trim().equals("0.0")))
//			{
//				if (type.equals("S"))
//				{
//					sql = "select disc_perc from disc_apr_strg where item_ser = ? " + " and	sc_code	= ? " + " and ? between eff_from and valid_upto ";
//					pStmt = conn.prepareStatement(sql);
//					pStmt.setString(1, itemSer.trim());
//					pStmt.setString(2, custCdEnd.trim());
//					pStmt.setTimestamp(3, getCurrdateTsFormat());
//					rs = pStmt.executeQuery();
//					if (rs.next())
//					{
//						discPerc = rs.getDouble("disc_perc");
//					}
//				} else
//				{
//					sql = "select disc_perc from customer_series where item_ser = ? " + " and	cust_code	= ? ";
//					pStmt = conn.prepareStatement(sql);
//					pStmt.setString(1, itemSer.trim());
//					pStmt.setString(2, custCdEnd.trim());
//					rs = pStmt.executeQuery();
//					if (rs.next())
//					{
//						discPerc = rs.getDouble("disc_perc");
//					}
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//				itmChangeString.append("<discount_per>").append("<![CDATA[" + discPerc + "]]>").append("</discount_per>");
//			}
//			if ((priceList == null || priceList.trim().length() == 0) && (siteCodeCr != null))
//			{
//				sql = "select price_list from site_customer " + " where site_code = ? " + " and	cust_code = ? ";
//				pStmt = conn.prepareStatement(sql);
//				pStmt.setString(1, siteCodeCr.trim());
//				pStmt.setString(2, custCode.trim());
//				rs = pStmt.executeQuery();
//				if (rs.next())
//				{
//					priceList = rs.getString("price_list");
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//
//				if (priceList == null)
//				{
//					sql = "Select price_list from customer where cust_code = ? ";
//					pStmt = conn.prepareStatement(sql);
//					pStmt.setString(1, custCode.trim());
//					rs = pStmt.executeQuery();
//					if (rs.next())
//					{
//						priceList = rs.getString("price_list");
//					}
//					rs.close();
//					rs = null;
//					pStmt.close();
//					pStmt = null;
//				}
//			}
//			/*
//			 * if( itemCode != null && itemCode.trim().length() > 0 && lotNo !=
//			 * null && lotNo.trim().length() > 0 ) { sql =
//			 * "Select rate from pricelist where " +" price_list = ? "
//			 * +" and item_code  = ? " +" and list_type = 'B' "
//			 * +" and lot_no__from <= ? " +" and lot_no__to   >= ? "; pStmt =
//			 * conn.prepareStatement(sql); pStmt.setString(1, priceList);
//			 * pStmt.setString(2, itemCode); pStmt.setString(3, lotNo);
//			 * pStmt.setString(4, lotNo); rs = pStmt.executeQuery(); if(
//			 * rs.next() ) { rate = rs.getDouble("rate"); } rs.close(); rs =
//			 * null; pStmt.close(); pStmt = null; }
//			 */
//			if (lotNo != null && lotNo.trim().length() > 0)
//			{
//				/*
//				 * sql = "Select min(eff_cost) as rate from min_rate_history "
//				 * +" where item_code = ? " +" and lot_no = ? "
//				 * +" and cust_code = ? " +" and site_code = ?"; pStmt =
//				 * conn.prepareStatement(sql); pStmt.setString(1, itemCode);
//				 * pStmt.setString(2, lotNo); pStmt.setString(3, custCode);
//				 * pStmt.setString(4, siteCode); rs = pStmt.executeQuery(); if(
//				 * rs.next() ) { rate = rs.getDouble("rate"); } rs.close(); rs =
//				 * null; pStmt.close(); pStmt = null;
//				 * 
//				 * if( rate == 0.0 ) { sql = "Select rate from pricelist "
//				 * +" where price_list = ?" +" and item_code = ?"
//				 * +" and lot_no__from <= ?" +" and lot_no__to >= ?" +
//				 * "and eff_from <= ? " + "and valid_upto >= ?"; pStmt =
//				 * conn.prepareStatement(sql); pStmt.setString(1, priceList);
//				 * pStmt.setString(2, itemCode); pStmt.setString(3, lotNo);
//				 * pStmt.setString(4, lotNo); pStmt.setTimestamp(5, tranDateTs);
//				 * pStmt.setTimestamp(6, tranDateTs); rs = pStmt.executeQuery();
//				 * if( rs.next() ) { rate = rs.getDouble("rate"); } rs.close();
//				 * rs = null; pStmt.close(); pStmt = null; }
//				 * 
//				 * if( rate == 0.0 ) { sql =
//				 * "Select min(b.rate) as rate from invoice a, invoice_trace b "
//				 * +" where a.invoice_id = b.invoice_id "
//				 * +" and	a.cust_code = ? " +" and	a.site_code = ? "
//				 * +" and	a.tran_date <= ? " +" and	b.item_code = ? "
//				 * +" and	b.lot_no = ? " +" and	a.confirmed = 'Y'" ; pStmt =
//				 * conn.prepareStatement(sql); pStmt.setString(1, custCode);
//				 * pStmt.setString(2, siteCode); pStmt.setTimestamp(3,
//				 * getCurrdateTsFormat()); pStmt.setString(4, itemCode);
//				 * pStmt.setString(5, lotNo); rs = pStmt.executeQuery(); if(
//				 * rs.next() ) { rate = rs.getDouble("rate"); } rs.close(); rs =
//				 * null; pStmt.close(); pStmt = null; }
//				 */
//				sql = "select sum(b.quantity) as saleQty from invoice a, invoice_trace b " + " where a.invoice_id = b.invoice_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.tran_date <= ? " + " and	b.item_code = ? " + " and	b.lot_no = ? " + " and	a.confirmed = 'Y'";
//				pStmt = conn.prepareStatement(sql);
//				pStmt.setString(1, custCode);
//				pStmt.setString(2, siteCode);
//				pStmt.setTimestamp(3, getCurrdateTsFormat());
//				pStmt.setString(4, itemCode);
//				pStmt.setString(5, lotNo);
//				rs = pStmt.executeQuery();
//				if (rs.next())
//				{
//					saleQty = rs.getDouble("saleQty");
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//				sql = "select sum(case when B.ret_rep_flag = 'R' then b.quantity else -b.quantity end) as saleRetQty " + " from sreturn a, sreturndet b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.tran_date <= ? " + " and	b.item_code = ? " + " and   b.lot_no 	= ? " + " and	a.confirmed = 'Y'";
//				pStmt = conn.prepareStatement(sql);
//				pStmt.setString(1, custCode);
//				pStmt.setString(2, siteCode);
//				pStmt.setTimestamp(3, getCurrdateTsFormat());
//				pStmt.setString(4, itemCode);
//				pStmt.setString(5, lotNo);
//				rs = pStmt.executeQuery();
//				if (rs.next())
//				{
//					saleRetQty = rs.getDouble("saleRetQty");
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//
//				sql = "select sum(case when b.quantity is null then 0 else b.quantity end) as confQty " + " from charge_back a, charge_back_det b " + " where a.tran_id = b.tran_id " + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	a.confirmed = 'Y' " + " and	b.item_code = ? " + " and   b.lot_no 	= ? ";
//				pStmt = conn.prepareStatement(sql);
//				pStmt.setString(1, custCode);
//				pStmt.setString(2, siteCode);
//				pStmt.setString(3, itemCode);
//				pStmt.setString(4, lotNo);
//				rs = pStmt.executeQuery();
//				if (rs.next())
//				{
//					confQty = rs.getDouble("confQty");
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//				// 14/06/10 manoharan not to consider current transaction,
//				// current transaction quantity to be calculated separately
//				if (tranId == null)
//				{
//					tranId = "@@@";
//				}
//				sql = "select sum(case when b.quantity is null then 0 else b.quantity end) as unconfQty " + " from charge_back a, charge_back_det b " + " where a.tran_id = b.tran_id " + " and a.tran_id <> ? " // 14/06/10
//																																																				 // manoharan
//																																																				 // not
//																																																				 // to
//				// consider current
//				// transaction quantity
//				        + " and	a.cust_code = ? " + " and	a.site_code = ? " + " and	(case when a.confirmed is null then 'N' else a.confirmed end)  = 'N' " + " and	b.item_code = ? " + " and   b.lot_no 	= ? ";
//				pStmt = conn.prepareStatement(sql);
//				pStmt.setString(1, tranId); // 14/06/10 manoharan not to
//				// consider current transaction
//				// quantity
//				pStmt.setString(2, custCode);
//				pStmt.setString(3, siteCode);
//				pStmt.setString(4, itemCode);
//				pStmt.setString(5, lotNo);
//				rs = pStmt.executeQuery();
//				if (rs.next())
//				{
//					unconfQty = rs.getDouble("unconfQty");
//				}
//				rs.close();
//				rs = null;
//				pStmt.close();
//				pStmt = null;
//				// ///////////////////////////////////////////////////////////////////////////////////////////////////
//				/*
//				 * // 27/05/10 manoharan bill_no, bill_date and discount to be
//				 * carried forward NodeList detail2List = null; Node detailNode
//				 * = null; NodeList childDetilList = null; Node chidDetailNode =
//				 * null; double curQty = 0; detail2List =
//				 * dom2.getElementsByTagName("Detail2");
//				 * System.out.println("manohar unconfirm quantity abefore [" +
//				 * unconfQty + "] detail2List.getLength() [" +
//				 * detail2List.getLength() + "]"); unconfQty = 0; curQty = 0; if
//				 * (detail2List.getLength() > 1) {
//				 * 
//				 * for(int detCtr =0; detCtr < detail2List.getLength(); detCtr++
//				 * ) {
//				 * 
//				 * detailNode = detail2List.item(detCtr); childDetilList =
//				 * detailNode.getChildNodes(); tempQty = "0"; tempLotNo = "";
//				 * tempItemCode = ""; tempLineNo = ""; dTempQty = 0; for(int p
//				 * =0; p < childDetilList.getLength(); p++ ) { chidDetailNode =
//				 * childDetilList.item(p);
//				 * System.out.println("manohar current child node [" +
//				 * chidDetailNode.getNodeName() + "]");
//				 * if(chidDetailNode.getNodeName().equalsIgnoreCase("item_code")
//				 * ) { //System.out.println("discount_per node found >>>>>" +
//				 * chidDetailNode.getNodeName());
//				 * if(chidDetailNode.getFirstChild() != null ) { tempItemCode =
//				 * chidDetailNode.getFirstChild().getNodeValue();
//				 * if(tempItemCode == null || tempItemCode.trim().length() == 0)
//				 * { tempItemCode = ""; } } }
//				 * if(chidDetailNode.getNodeName().equalsIgnoreCase("lot_no") )
//				 * { //System.out.println("discount_per_unit node found >>>>>" +
//				 * chidDetailNode.getNodeName());
//				 * if(chidDetailNode.getFirstChild() != null ) { tempLotNo =
//				 * chidDetailNode.getFirstChild().getNodeValue(); if(tempLotNo
//				 * == null ) { tempLotNo = ""; } } }
//				 * if(chidDetailNode.getNodeName().equalsIgnoreCase("quantity")
//				 * ) { //System.out.println("porder_no node found >>>>>" +
//				 * chidDetailNode.getNodeName());
//				 * if(chidDetailNode.getFirstChild() != null ) { tempQty =
//				 * chidDetailNode.getFirstChild().getNodeValue(); if(tempQty ==
//				 * null || tempQty.trim().length() == 0) { tempQty = "0"; } } }
//				 * if(chidDetailNode.getNodeName().equalsIgnoreCase("line_no") )
//				 * { //System.out.println("porder_no node found >>>>>" +
//				 * chidDetailNode.getNodeName());
//				 * if(chidDetailNode.getFirstChild() != null ) { tempLineNo =
//				 * chidDetailNode.getFirstChild().getNodeValue(); if(tempLineNo
//				 * == null || tempLineNo.trim().length() == 0) { tempLineNo =
//				 * "0"; } } }
//				 * 
//				 * } // compare the item_code and lot_no if matches add to the
//				 * current quantity
//				 * System.out.println("manohar comparing line_no [ " + lineNo +
//				 * "] item [" + itemCode + "] lot [" + lotNo + "]");
//				 * System.out.println("manohar comparing tempLineNo [ " +
//				 * tempLineNo + "] tempItemCode [" + tempItemCode +
//				 * "] tempLotNo [" + tempLotNo + "]"); if
//				 * (lotNo.trim().equals(tempLotNo.trim()) &&
//				 * itemCode.trim().equals(tempItemCode.trim()) &&
//				 * !tempLineNo.equals(lineNo) ) { dTempQty =
//				 * Double.parseDouble(tempQty); curQty += dTempQty;
//				 * System.out.println("manohar line_no [ " + tempLineNo +
//				 * "] item [" + tempItemCode + "] lot [" + tempLotNo +
//				 * "] qty ["+ dTempQty+ "]"); } // }
//				 * 
//				 * } unconfQty += curQty;
//				 * System.out.println("manohar unconfirm quantity after [" +
//				 * unconfQty + "]");
//				 */
//				// ///////////////////////////////////////////////////////////////////////////////////////////////////
//				/*
//				 * sql = "select ('"+unconfQty+
//				 * "' - (case when b.quantity is null then 0 else b.quantity end)) as unconfQty "
//				 * +" from charge_back a, charge_back_det b "
//				 * +" where a.tran_id = b.tran_id " +" and	b.tran_id = ? "
//				 * +"	and	b.line_no = ? " +"	and	a.cust_code = ? "
//				 * +" and	a.site_code = ? " +
//				 * "	and	(case when a.confirmed is null then 'N' else a.confirmed end)  = 'N' "
//				 * +"	and	b.item_code = ? " +"	and   b.lot_no 	= ? "; pStmt =
//				 * conn.prepareStatement(sql); pStmt.setString(1, tranId);
//				 * pStmt.setString(2, lineNo); pStmt.setString(2, custCode);
//				 * pStmt.setString(3, siteCode); pStmt.setString(4, itemCode);
//				 * pStmt.setString(5, lotNo); rs = pStmt.executeQuery(); if(
//				 * rs.next() ) { unconfQty = rs.getDouble("unconfQty"); }
//				 * rs.close(); rs = null; pStmt.close(); pStmt = null;
//				 */
//				rate = getMinRate(dom, dom1, "lot_no", conn);
//				// saleRetQty = Math.abs(saleRetQty);
//				itmChangeString.append("<rate__sell>").append("<![CDATA[" + rate + "]]>").append("</rate__sell>");
//				itmChangeString.append("<sale_qty>").append("<![CDATA[" + saleQty + "]]>").append("</sale_qty>");
//				itmChangeString.append("<sale_ret_qty>").append("<![CDATA[" + saleRetQty + "]]>").append("</sale_ret_qty>");
//				itmChangeString.append("<conf_claimed>").append("<![CDATA[" + getRequiredDecimal(confQty, 3) + "]]>").append("</conf_claimed>");
//				// itmChangeString.append("<unconf_claimed>").append("<![CDATA["+
//				// getRequiredDecimal(unconfQty,3) +
//				// "]]>").append("</unconf_claimed>");
//			}
//		} catch (Exception e)
//		{
//			System.out.println("Exception :ChargeBackLoc :itemChange(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
//			throw new ITMException(e);
//		}
//		return itmChangeString.toString();
//	}

//	private String getCalculations(Document dom) throws RemoteException, ITMException
//	{
//		StringBuffer itmChangeString = new StringBuffer("");
//		double rate = 0.0;
//		double discAmt = 0.0;
//		double rateDiff = 0.0;
//		double amount = 0.0, endCustRateDbl = 0.0, taxAmtDbl = 0.0;
//		double netAmt = 0.0, quantityDbl = 0.0, rateSellDbl = 0.0, discPerDbl = 0.0;
//		double discPerUnit = 0.0;
//		try
//		{
//			String quantity = genericUtility.getColumnValue("quantity", dom);
//			String rateSell = genericUtility.getColumnValue("rate__sell", dom);
//			String discPer = genericUtility.getColumnValue("discount_per", dom);
//			String endCustRate = genericUtility.getColumnValue("rate__contr", dom);
//			String taxAmt = genericUtility.getColumnValue("tax_amt", dom);
//
//			if (quantity != null && quantity.trim().length() > 0)
//				quantityDbl = Double.parseDouble(quantity);
//
//			if (rateSell != null && rateSell.trim().length() > 0)
//				rateSellDbl = Double.parseDouble(rateSell);
//
//			if (discPer != null && discPer.trim().length() > 0)
//				discPerDbl = Double.parseDouble(discPer);
//
//			if (endCustRate != null && endCustRate.trim().length() > 0)
//				endCustRateDbl = Double.parseDouble(endCustRate);
//
//			if (taxAmt != null && taxAmt.trim().length() > 0)
//				taxAmtDbl = Double.parseDouble(taxAmt);
//
//			if (endCustRateDbl <= 0)
//			{
//				rate = rateSellDbl;
//			} else
//			{
//				rate = Math.min(endCustRateDbl, rateSellDbl);
//				rateDiff = rateSellDbl - endCustRateDbl;
//			}
//
//			discAmt = (quantityDbl * rate * discPerDbl) / 100;
//			amount = rateDiff * quantityDbl;
//			netAmt = amount + discAmt + taxAmtDbl;
//			discPerUnit = (rate * discPerDbl) / 100;
//			
//			netAmt=100;
//
//			itmChangeString.append("<discount_amt>").append("<![CDATA[" + getRequiredDecimal(discAmt, 3) + "]]>").append("</discount_amt>");
//			itmChangeString.append("<amount>").append("<![CDATA[" + getRequiredDecimal(amount, 3) + "]]>").append("</amount>");
//			itmChangeString.append("<net_amt>").append("<![CDATA[" + getRequiredDecimal(netAmt, 3) + "]]>").append("</net_amt>");
//			itmChangeString.append("<discount_per_unit>").append("<![CDATA[" + getRequiredDecimal(discPerUnit, 3) + "]]>").append("</discount_per_unit>");
//			itmChangeString.append("<rate__diff>").append("<![CDATA[" + getRequiredDecimal(rateDiff, 3) + "]]>").append("</rate__diff>");
//		} catch (Exception e)
//		{
//			System.out.println("Exception :ChargeBackLoc :itemChange(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
//			throw new ITMException(e);
//		}
//		return itmChangeString.toString();
//	}
//
//	String getRequiredDecimal(double actVal, int prec)
//	{
//		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
//		Double DoubleValue = new Double(actVal);
//		numberFormat.setMaximumFractionDigits(prec);
//		String strValue = numberFormat.format(DoubleValue);
//		strValue = strValue.replaceAll(",", "");
//		// double reqVal = Double.parseDouble(strValue);
//		return strValue; // reqVal;
//	}

	// 20/07/10 manoharan to get the quantity for same lot other than current
	// line
//	private double getCurTranQty(String curLineNo, String itemCode, String lotNo, Document dom) throws Exception
//	{
//		String updateFlag = null;
//		String fldValue = null;
//		String nodeName = null;
//		Node currNode = null;
//		Node currDet = null;
//		NodeList currNodes = null;
//		String lineNoStr = "0", qtyStr = "0", domLotNo = "", domItemCode = "";
//
//		int currNodeLen;
//		System.out.println("Checking quantity in current transaction");
//		NodeList detailNodes = dom.getElementsByTagName("Detail2");
//		int detLen = detailNodes.getLength();
//		double curTranQty = 0, domQty = 0;
//		try
//		{
//
//			for (int detIdx = 0; detIdx < detLen; detIdx++)
//			{
//				currDet = detailNodes.item(detIdx);
//				System.out.println("manohar 10/12/10 got currDet [" + currDet + "]");
//				currNodes = currDet.getChildNodes();
//				currNodeLen = currNodes.getLength();
//				domQty = 0;
//				for (int curNodeIdx = 0; curNodeIdx < currNodeLen; curNodeIdx++)
//				{
//					currNode = currNodes.item(curNodeIdx);
//					nodeName = currNode.getNodeName();
//					System.out.println("manohar 10/12/10 got nodeName [" + nodeName + "]");
//					if (nodeName.equalsIgnoreCase("attribute"))
//					{
//						updateFlag = currNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
//					} else if (currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase("quantity"))
//					{
//						qtyStr = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "0";
//					} else if (currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase("line_no"))
//					{
//						lineNoStr = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "0";
//					} else if (currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase("item_code"))
//					{
//						domItemCode = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "";
//					} else if (currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase("lot_no"))
//					{
//						domLotNo = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : "";
//					}
//				}
//				System.out.println("manohar updateFlag [" + updateFlag + "] lineNoStr [" + lineNoStr + "] curLineNo [" + curLineNo + "] domItemCode [" + domItemCode + "] itemCode [ " + itemCode + "] domLotNo [" + domLotNo + "] lotNo [ " + lotNo + "] qtyStr[" + qtyStr + "]");
//				if (!"D".equalsIgnoreCase(updateFlag) && (!lineNoStr.trim().equals(curLineNo.trim())) && (domItemCode.trim().equalsIgnoreCase(itemCode.trim())) && (domLotNo.trim().equalsIgnoreCase(lotNo.trim())))
//				{
//					System.out.println("manohar found lineNoStr [" + lineNoStr + "] curLineNo [" + curLineNo + "] domItemCode [" + domItemCode + "] domLotNo [" + domLotNo + "] qtyStr[" + qtyStr + "]");
//					if (qtyStr == null)
//					{
//						qtyStr = "0";
//					}
//					domQty = Double.parseDouble(qtyStr);
//					curTranQty += domQty;
//				}
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			throw new ITMException(e);
//		}
//		return curTranQty;
//	}

	// end 20/07/10 manoharan to get the quantity for same lot other than
	// current line
//	private double getMinRate(Document dom, Document dom1, String currCol, Connection conn) throws Exception
//	{
//		DistCommon distCommon = new DistCommon();
//		String invoiceId = null, retReplFlag = null, sql = "";
//		String itemCode = null;
//		String siteCode = null;
//		String lotNo = null;
//		String priceList = null;
//		double minRate = 0;
//		int noSchemeHist = 1;
//		String sNoSchemeHist = null;
//		String schemeKey = null;
//		String varValue = null;
//		String col[] = new String[500];
//		boolean dynamicCol = true;
//		boolean colMatch = false;
//		String tranDate = null;
//		String unitRate = null;
//		double qtyStdUom = 0;
//		String unitStd = null;
//		String colName = null;
//		String docKey = null;
//		String docValue = null;
//		int pos;
//		String strToken;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		try
//		{
//			/*
//			 * invoiceId = genericUtility.getColumnValue("invoice_id",dom1);
//			 * System.out.println( "invoiceId :: " + invoiceId ); if (invoiceId
//			 * != null && invoiceId.trim().length() > 0 ) { return sBuff; }
//			 * retReplFlag = genericUtility.getColumnValue("ret_rep_flag",dom);
//			 * System.out.println( "retReplFlag :: " + retReplFlag ); if
//			 * (!"R".equals(retReplFlag) ) { System.out.println( "return 1");
//			 * return sBuff; }
//			 */
//			sNoSchemeHist = distCommon.getDisparams("999999", "SCHEME_HIST_NUM", conn);
//			System.out.println("sNoSchemeHist :: " + sNoSchemeHist);
//
//			if ("NULLFOUND".equals(sNoSchemeHist))
//			{
//				col[0] = "site_code";
//				col[1] = "item_code";
//				col[2] = "lot_no";
//				noSchemeHist = 1;
//				dynamicCol = false;
//			} else
//			{
//				int colFound = 0;
//				noSchemeHist = Integer.parseInt(sNoSchemeHist);
//				for (int ctr = 1; ctr <= noSchemeHist; ctr++)
//				{
//					schemeKey = "SCHEME_HIST_KEY" + ctr;
//					varValue = distCommon.getDisparams("999999", schemeKey, conn);
//					System.out.println("currCol :: " + currCol);
//					System.out.println("varValue :: " + varValue);
//					if (varValue.indexOf(currCol) > -1)
//					{
//						colMatch = true;
//						colFound = 1;
//						break;
//					}
//				}
//				if (!colMatch)
//				{
//					System.out.println("return 2");
//					return 0;
//				}
//			}
//
//			priceList = genericUtility.getColumnValue("price_list", dom1);
//			itemCode = genericUtility.getColumnValue("item_code", dom);
//			lotNo = genericUtility.getColumnValue("lot_no", dom);
//			tranDate = genericUtility.getColumnValue("tran_date", dom1);
//			String qtyStdUomStr = genericUtility.getColumnValue("quantity", dom);
//			if (qtyStdUomStr == null)
//			{
//				qtyStdUomStr = "0";
//			}
//			qtyStdUom = Double.parseDouble(qtyStdUomStr);
//
//			for (int ctr = 1; ctr <= noSchemeHist; ctr++)
//			{
//				docKey = null;
//				int colCount = -1;
//				if (dynamicCol)
//				{
//					colCount = -1;
//					schemeKey = "SCHEME_HIST_KEY" + ctr;
//					varValue = distCommon.getDisparams("999999", schemeKey, conn);
//					if ("NULLFOUND".equals(varValue))
//					{
//						System.out.println("return 3");
//						return 0;
//					} else
//					{
//						varValue = varValue.trim();
//
//						while (varValue.trim().length() > 0)
//						{
//							colCount++;
//							pos = varValue.indexOf(",");
//							if (pos > -1)
//							{
//								strToken = distCommon.getToken(varValue, ",");
//								col[colCount] = strToken;
//								varValue = varValue.substring(pos + 1);
//							} else
//							{
//								col[colCount] = varValue;
//								break;
//							}
//						} // populate column list
//					}
//				} else
//				{
//					colCount = 2;
//				}
//				for (int colCtr = 0; colCtr <= colCount; colCtr++)
//				{
//					colName = col[colCtr];
//					if ("site_code".equalsIgnoreCase(colName.trim()) || "invoice_id".equalsIgnoreCase(colName.trim()) || "cust_code".equalsIgnoreCase(colName.trim()))
//					{
//						docValue = genericUtility.getColumnValue(colName.trim(), dom1);
//					} else
//					{
//						docValue = genericUtility.getColumnValue(colName.trim(), dom);
//					}
//					if (docKey != null && docKey.trim().length() > 0)
//					{
//						docKey = docKey + "," + (docValue == null || docValue.trim().length() == 0 ? "" : docValue.trim()); // 13/05/10
//						// manoharan
//						// docValue trim()
//						// added
//					} else
//					{
//						docKey = docValue;
//					}
//				}
//				sql = " select eff_cost from min_rate_history where doc_key = ?";
//				pstmt = conn.prepareStatement(sql);
//				pstmt.setString(1, docKey);
//				rs = pstmt.executeQuery();
//				if (rs.next())
//				{
//					minRate = rs.getDouble(1);
//				}
//				rs.close();
//				pstmt.close();
//				pstmt = null;
//				rs = null;
//				if (minRate > 0)
//				{
//					break;
//				}
//			}
//			if (minRate == 0)
//			{
//				tranDate = tranDate == null ? (genericUtility.getValidDateString(new Timestamp(System.currentTimeMillis()).toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat())).toString() : tranDate;
//				itemCode = itemCode == null ? "" : itemCode;
//				lotNo = lotNo == null ? "" : lotNo;
//				minRate = distCommon.pickRate(priceList, tranDate, itemCode, lotNo, "D", qtyStdUom, conn);
//			}
//
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			throw new ITMException(e);
//		}
//		return minRate;
//	}

}