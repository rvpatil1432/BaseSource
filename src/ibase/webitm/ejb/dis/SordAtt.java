 /*
	Developed by :Jiten
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 27-Jul-05
*/
 
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
//import java.util.Date;
import java.text.*;
import java.sql.*;
import org.w3c.dom.*;
//import javax.xml.parsers.*;
import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.GenVal;


//public class SordAttEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class SordAtt extends ValidatorEJB implements SordAttLocal, SordAttRemote
{
	double theoriticalWgtprPiece;
	HashMap hashAttribute = null;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
/*
	public void ejbCreate() throws RemoteException, CreateException 
	{
	}
	public void ejbRemove()
	{ 
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		try
		{
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
			System.out.println("Exception : SordAttEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		String errCode = "";
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String userId = ""; 
		String loginSite = "";
		String siteCode = "";
		String itemSer = "",columnValue = "",custCode="",orderDate = "";
		String errFldName="";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = ""; 
		int ctr,currentFormNo=0;
		int childNodeListLength;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		HashMap<String , String > errorMap= new HashMap<String, String>();
		try
		{
			conn = getConnection(); 
			
			userId = getValueFromXTRA_PARAMS(xtraParams,"userId");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//GenericUtility genericUtility = GenericUtility.getInstance(); 
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();

					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if (childNodeName.equals("ord_date"))
						{
							siteCode = getColumnValue("site_code",dom);
							if (childNode.getFirstChild() == null)
							{
								errString = itmDBAccess.getErrorString("ord_date", "VMDTNUL1", userId,"",conn);
								break;
							}
							//Changes and Commented By Ajay on 20-12-2017 :START
						    //errCode = nfCheckPeriod("SAL",genericUtility.getDateObject(childNode.getFirstChild().getNodeValue().toString().trim()),siteCode); //Checking the Period
							errCode=finCommon.nfCheckPeriod("SAL",genericUtility.getDateObject(childNode.getFirstChild().getNodeValue().toString().trim()),siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							if (errCode.trim().length() > 0)
							{
								errString = itmDBAccess.getErrorString("ord_date",errCode,userId,"",conn);
								break;
							}
						}
						/*else if (childNodeName.equals("order_type"))
						{
							if (childNode.getFirstChild() == null)
							{
								errCode = "VTORDNL";
								errString = getErrorString("order_type",errCode,userId);
								break;
							}
						} */
						//Added for Sorder Header
						else if (childNodeName.equals("contract_no"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue != null && columnValue.length() > 0)
								{
									sql = "SELECT CONFIRMED, STATUS FROM SCONTRACT WHERE CONTRACT_NO = '"+columnValue+"'";
									
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getString(1).equals("Y"))
										{
											errString = itmDBAccess.getErrorString("contract_no","VTISCNO",userId,"",conn);
										}
										else if(rs.getString(1).equals("X"))
										{
											errString = itmDBAccess.getErrorString("contract_no","VSCCAN",userId,"",conn);
										}							
									}
								}
							}
							stmt.close();
						}
						else if (childNodeName.equals("bank_code"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{	
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue != null && columnValue.length() > 0)
								{
									sql ="SELECT COUNT(1) FROM BANK WHERE BANK_CODE = '"+columnValue+"'";
									stmt = conn.createStatement();
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("bank_code","VMBANK1",userId,"",conn);
										}
									}
								}
							}
							stmt.close();
						}
						else if (childNodeName.equals("curr_code__frt")) 
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="select count(*)from currency where curr_code  = '"+columnValue+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("curr_code__frt","VTCURRCD1",userId,"",conn);
									}
								}
							}
							stmt.close();
						}
						else if (childNodeName.equals("curr_code__ins")) 
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="select count(*)from currency where curr_code  = '"+columnValue+"'";
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("curr_code__ins","VTCURRCD1",userId,"",conn);
									}
								}
							}
							stmt.close();
						}
						else if (childNodeName.equals("sales_pers"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								siteCode = genericUtility.getColumnValue("site_code",dom);
								if(columnValue.length() != 0)
								{
									sql = "select var_value from disparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_SPERS'";
									
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getString(1).equals("Y"))
										{
											sql="select count(*) from site_sales_pers where site_code ='"+siteCode+"' and sales_pers = '"+columnValue+"'";
											rs = stmt.executeQuery(sql);
											if(rs.next())
											{
												if(rs.getInt(1) == 0)
												{
													errString = itmDBAccess.getErrorString("sales_pers","VTSLPERS2",userId,"",conn);
												}
											}
										}
									}
									
									sql="select count(*) from sales_pers where sales_pers = '"+columnValue+"'";

									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("sales_pers","VTSLPERS1",userId,"",conn);
										}
									}
								}
								else
								{
									itemSer = genericUtility.getColumnValue("item_ser",dom);
									sql="SELECT SALES_PERS_YN FROM ITEMSER WHERE ITEM_SER ='"+itemSer+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getString(1).equals("Y"))
										{
											errString = itmDBAccess.getErrorString("sales_pers","VMSLPERS1",userId,"",conn);
										}
									}
								}
							}
							stmt.close();
						} 
						else if (childNodeName.equals("dist_route"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() > 0)
								{
									sql = "SELECT COUNT(*) FROM DISTROUTE WHERE DIST_ROUTE = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("dist_route","VTDISTRT1",userId,"",conn);
										}
										else if (rs.getInt(1) == 1)
										{
											String stanCode = genericUtility.getColumnValue("stan_code",dom);
											sql ="SELECT STAN_CODE__TO FROM DISTROUTE WHERE DIST_ROUTE = '"+columnValue+"'";
											rs = stmt.executeQuery(sql);
											if(rs.next())
											{
												if(rs.getString(1).equals(stanCode))
												{
													errString = itmDBAccess.getErrorString("dist_route","VMSTANMIS1",userId,"",conn);
												}
											}
										}
									}
								}
							}
							stmt.close();
						} 
						else if (childNodeName.equals("sales_pers__1"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() != 0)
								{
									sql ="SELECT COUNT(*)FROM SALES_PERS WHERE SALES_PERS  = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("sales_pers__1","VMSLPERS1",userId,"",conn);
										}
									}
								}
							}
							stmt.close();
						} 
						else if (childNodeName.equals("sales_pers__2"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() != 0)
								{
									sql ="SELECT COUNT(*)FROM SALES_PERS WHERE SALES_PERS  = '"+columnValue+"'";

									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("sales_pers__2","VMSLPERS1",userId,"",conn);
										}
									}
								}
							}
							stmt.close();
						} 
						else if (childNodeName.equals("adv_perc"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								int advPercInt = Integer.parseInt(columnValue);
								if(advPercInt > 100)
								{
									errString = itmDBAccess.getErrorString("adv_perc","VMADVPERC1",userId,"",conn);
								}
							}
							stmt.close();
						} 
						else if (childNodeName.equals("quot_no"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue != null && columnValue.length() > 0)
								{
									sql ="SELECT COUNT(1) FROM SALES_QUOT WHERE QUOT_NO = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("quot_no","VTQUOT1",userId,"",conn);
										}
									}
									sql ="SELECT CUST_CODE FROM SALES_QUOT WHERE QUOT_NO = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										custCode = genericUtility.getColumnValue("cust_code",dom);
										if(!rs.getString(1).equals(custCode))
										{
											errString = itmDBAccess.getErrorString("quot_no","VTQUOT2",userId,"",conn);
										}
									}
								}
							}
							stmt.close();
						} 
						else if (childNodeName.equals("cust_code") || childNodeName.equals("cust_code__dlv") || childNodeName.equals("cust_code__bil"))
						{
							stmt = conn.createStatement();
							String black_listed_yn = "",ls_stop_business = "";
							siteCode = getColumnValue("site_code",dom);
							orderDate = getColumnValue("ord_date",dom);
							itemSer = getColumnValue("item_ser",dom);
							if (childNode.getFirstChild() == null)	     
							{
								errCode = "VECUST2";
								errString = getErrorString("cust_code",errCode,userId);
								break;
							}
							custCode = childNode.getFirstChild().getNodeValue().trim();
							sql = "SELECT BLACK_LISTED FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"'AND ITEM_SER ='"+itemSer+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								black_listed_yn = rs.getString("BLACK_LISTED");
							}
							if (black_listed_yn.equalsIgnoreCase("Y"))
							{
								errCode = "VTCUSTCD3";
								errString = getErrorString("cust_code",errCode,userId);
								break;
							}
							else
							{
								errCode = isCustomer(siteCode,custCode,"",conn);
								if (errCode.trim().length() == 0)
								{
									sql = "SELECT STOP_BUSINESS FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										ls_stop_business = rs.getString("STOP_BUSINESS");
										if (ls_stop_business.equalsIgnoreCase("Y"))
										{
											errCode = "VTICC";
											errString = getErrorString("cust_code",errCode,userId);
											break;
										}
									} 									
								}
								else
								{
									errString = getErrorString("cust_code",errCode,userId);
									break;
								}
							}
							stmt.close();
						}
						else if (childNodeName.equals("tran_code"))
						{
							stmt = conn.createStatement();
							int cnt;
							String tranCode = "";
							if (childNode.getFirstChild() != null)
							{
								tranCode = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT COUNT(*) AS COUNT FROM TRANSPORTER WHERE TRAN_CODE = '"+tranCode+"'";
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									cnt = rs.getInt("COUNT");
									if (cnt == 0)
									{
										errCode = "VTTRANCD1";
										errString = getErrorString("tran_code",errCode,userId);
										break;
									}
								}
							}
							stmt.close();
						}						
						else if (childNodeName.equals("curr_code"))
						{
							stmt = conn.createStatement();
							int cnt;
							String currCode = "";
							if (childNode.getFirstChild() == null)
							{
								errCode = "VTCURR";
								errString = getErrorString("curr_code",errCode,userId);
								break;
							}
							currCode = childNode.getFirstChild().getNodeValue().trim();
							sql = "SELECT COUNT(*) AS COUNT FROM CURRENCY WHERE CURR_CODE = '"+currCode+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								cnt = rs.getInt("COUNT");
								if (cnt == 0)
								{
									errCode = "VTCURRCD1";
									errString = getErrorString("curr_code",errCode,userId);
									break;
								}
							}
							stmt.close();
						}
						else if (childNodeName.equals("site_code"))
						{
							stmt = conn.createStatement();
							int cnt;
							if (childNode.getFirstChild() == null)
							{
								errCode = "VMSITECD1";
								errString = getErrorString("site_code",errCode,userId);
								break;
							}
							siteCode = childNode.getFirstChild().getNodeValue().trim();
							sql = "SELECT COUNT(*) AS COUNT FROM SITE WHERE SITE_CODE = '"+siteCode+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								cnt = rs.getInt("COUNT");
								if (cnt == 0)
								{
									errCode = "VMSITE1";
									errString = getErrorString("site_code",errCode,userId);
									break;
								}
							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("site_code__ship"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="SELECT COUNT(*) FROM SITE WHERE SITE_CODE = '"+columnValue+"'";
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("site_code__ship","VMSITE1",userId,"",conn);
									}
								}
 							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("count_code__dlv"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue != null && columnValue.length() != 0)
								{
									sql ="SELECT COUNT(*) FROM COUNTRY WHERE COUNT_CODE = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
										errString = itmDBAccess.getErrorString("count_code_dlv","VTCONT1",userId,"",conn);
										}
									}									
								}
							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("stan_code"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() != 0)
								{
									sql ="SELECT COUNT(1) FROM STATION WHERE STAN_CODE = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("stan_code","VTSTAN1",userId,"",conn);
										}
									}									
								}
							}
							stmt.close();
							stmt = null;
						} 
						else if (childNodeName.equals("stan_code__init"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() != 0)
								{
									sql ="SELECT COUNT(1) FROM STATION WHERE STAN_CODE = '"+columnValue+"'";
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("stan_code__init","VTSTAN1",userId,"",conn);
										}
									}									
								}
							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("cr_term"))
						{			
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() != 0)
								{
									sql ="SELECT COUNT(*) FROM CRTERM WHERE CR_TERM = '"+columnValue+"'";
									
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("cr_term","VTCRTERM1",userId,"",conn);
										}
									}									
								}
							}
							stmt.close();
							stmt = null;
						} 
						else if (childNodeName.equals("dlv_term"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue.length() != 0)
								{
									sql ="SELECT COUNT(*)FROM DELIVERY_TERM WHERE DLV_TERM  = '"+columnValue+"'";
									
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("dlv_term","VMDLVTERM1",userId,"",conn);
										}
									}									
								}
							} 
							stmt.close();
							stmt = null;
						} 
						else if (childNodeName.equals("emp_code__ord"))
						{
							stmt = conn.createStatement();
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="SELECT COUNT(*) FROM EMPLOYEE WHERE EMP_CODE  = '"+columnValue+"'";
								
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("emp_code__ord","VMEMPORD2",userId,"",conn);
									}
								}								
							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("price_list__disc"))
						{
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="SELECT DISTINCT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = '"+columnValue+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								while(rs.next())
								{
									if((!rs.getString(1).equals("M")) || (!rs.getString(1).equals("N")))
									{
										errString = itmDBAccess.getErrorString("price_list_disc","VPLSTYPE",userId,"",conn);
									}
								}
								stmt.close();
								stmt = null;
							}
						} 
						else if (childNodeName.equals("sn_code"))
						{
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								String hazardYn = genericUtility.getColumnValue("hazard_yn",dom);
								if(hazardYn.equals("Y"))
								{
									sql="SELECT COUNT(*) FROM SAFETY_NORM WHERE SN_CODE = '"+columnValue+"'";
									stmt = conn.createStatement();
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("sn_code","VTSNCODE",userId,"",conn);
										}
									}
									stmt.close();
									stmt = null;
								}
							}
						} 
						else if (childNodeName.equals("acct_code__sal"))
						{
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								if(columnValue != null && columnValue.length() > 0)
								{
									errCode = super.isAcctCode(columnValue,siteCode,"w_sorder");
									errString = itmDBAccess.getErrorString("acct_code__sal",errCode,userId,"",conn);
								}
							}
						}
						else if (childNodeName.equals("tax_class"))
						{
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="SELECT COUNT(1) FROM TAXCLASS WHERE TAX_CLASS = '"+columnValue+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("tax_class","VTTCLASS1",userId,"",conn);
									}
								}
								stmt.close();
								stmt = null;
							}
						} 
						else if (childNodeName.equals("tax_chap"))
						{
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="SELECT COUNT(*) FROM TAXCHAP WHERE TAX_CHAP = '"+columnValue+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("tax_chap","VTTCHAP1",userId,"",conn);
									}
								}
								stmt.close();
								stmt = null;
							}
						} 
						else if (childNodeName.equals("tax_env"))
						{
							if(childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT COUNT(*) FROM TAXENV WHERE TAX_ENV ='"+columnValue+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getInt(1) == 0)
									{
										errString = itmDBAccess.getErrorString("tax_env","VTTENV1",userId,"",conn);
									}
								}
								stmt.close();
								stmt = null;
							}
							
						}
						else if (childNodeName.equals("item_ser"))
						{
							stmt = conn.createStatement();
							int cnt;
							if (childNode.getFirstChild() == null)
							{
								errCode = "VTITEMSER5";
								errString = getErrorString("item_ser",errCode,userId);
								break;
							}
							itemSer = childNode.getFirstChild().getNodeValue().trim();
							sql = "SELECT COUNT(*) AS COUNT FROM ITEMSER WHERE ITEM_SER = '"+itemSer+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								cnt = rs.getInt("COUNT");
								if (cnt == 0)
								{
									errCode = "VTITEMSER1";
									errString = getErrorString("item_ser",errCode,userId);
									break;
								}
								else 
								{
									custCode = genericUtility.getColumnValue("cust_code",dom);
									sql = "SELECT COUNT(1) FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"' AND ITEM_SER = '"+itemSer+"'";
									System.out.println("SQL :: "+sql);
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										if(rs.getInt(1) == 0)
										{
											errString = itmDBAccess.getErrorString("item_ser","VTITEMSER4",userId,"",conn);
										}
									}
								}
							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("price_list"))
						{
							String priceList = "",listType = "";
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue().trim().length() > 0)
							{
								stmt = conn.createStatement();
								priceList = getColumnValue("price_list",dom);
								if (priceList.trim().length() > 0)
								{
									sql = "SELECT DISTINCT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"'";
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										listType = rs.getString("LIST_TYPE");
										if (!(listType.equalsIgnoreCase("B") || listType.equalsIgnoreCase("L") || listType.equalsIgnoreCase("I")))
										{
											errCode = "VPLSTYPE";
											errString = getErrorString("price_list",errCode,userId);
											break;
										}
									}
								}
								stmt = conn.createStatement();
								stmt = null;
							}
						}
						else if (childNodeName.equals("price_list__clg"))
						{
							/*if (childNode.getFirstChild() == null)
							{
								errCode = "VTPLISTCLG";
								errString = getErrorString("price_list__clg",errCode,userId);
								break;
							} */
							if (childNode.getFirstChild() != null)
							{
								//Add  Code
							}
						}						 
						else if (childNodeName.equals("cust_pord"))
						{
							String tranId = "",pordDate = "",custPord = "";
							//java.util.Date poDate = new java.util.Date();
							int cnt;
							/*if (childNode.getFirstChild() == null)
							{
								errCode = "VMCUSTCD1";
								errString = getErrorString("cust_pord",errCode,userId);
								break;
							} */
							if (childNode.getFirstChild() != null)
							{
								custPord = childNode.getFirstChild().getNodeValue();
								pordDate = getColumnValue("pord_date",dom);
								if (pordDate == null)
								{
									errCode = "VMDTNUL1";
									errString = getErrorString("cust_pord",errCode,userId);
									break;
								}
								tranId = getColumnValue("tran_id",dom);
								if (tranId == null || tranId.trim().equals(""))
								{
									tranId = " ";
								}
								sql = "SELECT COUNT(*) AS COUNT FROM SORDFORM_ATT WHERE CUST_PORD = ? AND PORD_DATE = ? AND TRAN_ID != ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custPord);
								pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(pordDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));
								pstmt.setString(3,tranId);
								rs = pstmt.executeQuery();	
								if (rs.next())
								{
									cnt = rs.getInt("COUNT");
									if (cnt > 0)
									{
										errCode = "VTCUSTPODT";
										errString = getErrorString("cust_pord",errCode,userId);
										break;
									}
								}
								//below codes added as to make it sorder Header
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									if(rs.getString(1) == null && errString.length() ==0)
									{
										sql="SELECT CHANNEL_PARTNER,DIS_LINK FROM CUSTOMER WHERE CUST_CODE='"+custCode+"'";
										rs =stmt.executeQuery(sql);
										if(rs.next())
										{
											if(rs.getString(1).equals("Y") && rs.getString(2).equals("A"))
											{
												if(columnValue.length() > 0 && columnValue != null)
												{
													sql ="SELECT STATUS FROM PORDER WHERE PURC_ORDER ='"+columnValue+"' AND	CONFIRMED  = 'Y'";
													rs =stmt.executeQuery(sql);
													if(rs.next())
													{
														if(rs.getInt(1) != 0)
														{
															errString = itmDBAccess.getErrorString("cust_pord","VTPONF",userId,"",conn);						
														}
													}
												}
											}
										}
									}
								}
								pstmt.close();
								stmt.close();
								stmt = null;
							}							 
						}
						else if (childNodeName.equals("pord_date"))
						{
							if (childNode.getFirstChild() != null)
							{
								String pordDate = "",ordDate = "";
								pordDate = childNode.getFirstChild().getNodeValue().trim();
								ordDate = getColumnValue("ord_date",dom);
								java.util.Date pordDate1 = new java.util.Date();
								java.util.Date ordDate1 = new java.util.Date();
								pordDate1 = new SimpleDateFormat(genericUtility.getApplDateFormat()).parse(pordDate);
								ordDate1 = new SimpleDateFormat(genericUtility.getApplDateFormat()).parse(ordDate);
								if (pordDate1.compareTo(ordDate1) > 0)
								{
									errCode = "VTPODATE";
									errString = getErrorString("pord_date",errCode,userId);
									break;
								}								
							}
						}
						else if (childNodeName.equals("trans_mode"))  //Transporter Mode can't be blank
						{
							if (childNode.getFirstChild() == null)
							{
								errCode = "VMTRMOD1";
								errString = getErrorString("trans_mode",errCode,userId);
								break;
							}
						}
						else if (childNodeName.equals("due_date")) //Due date should not be less than Order Date
						{
							String ordDate = "",dueDate = "";
							ordDate = getColumnValue("ord_date",dom);
							dueDate = childNode.getFirstChild().getNodeValue();
							java.util.Date ordDate1 = new java.util.Date();
							java.util.Date dueDate1 = new java.util.Date();
							ordDate1 = new SimpleDateFormat(genericUtility.getApplDateFormat()).parse(ordDate);
							dueDate1 = new SimpleDateFormat(genericUtility.getApplDateFormat()).parse(dueDate);
							if (dueDate1.compareTo(ordDate1) < 0)
							{
								errCode = "VTSCH1";
								errString = getErrorString("due_date",errCode,userId);
								break;
							}
						}						
					}	      				
					break;
				case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
										
					String itemTypeStr = "",totalColname="";
					for (int i = 0;i < childNodeListLength ;i++ )
					{
						childNode = childNodeList.item(i);
						childNodeName = childNode.getNodeName();
						if (childNodeName.equalsIgnoreCase("item_type"))
						{
							
							if (childNode.getFirstChild() != null)
							{
								itemTypeStr = childNode.getFirstChild().getNodeValue().trim();
							}						
							break;
						}
					}
					
					if (itemTypeStr != null && itemTypeStr.trim().length() > 0)
					{
						totalColname = getAttributeColumn("Total Qty",itemTypeStr,conn);
					}					
					
					GenVal genValidate = new GenVal();
					//errCode = genValidate.genVal("W_SORDFORM_ATT" ,objContext,dom ,conn);
					//comment added by sagar on 17/03/15
					/*errCode = genValidate.genVal("W_SORDFORM_ATT" ,objContext,dom );//Conn argument removed by manoj dtd 28/11/2014 
					if (errCode != null && errCode.trim().length() > 0)
					{
						errString = getErrorString("", errCode, userId);
						break;
					}*/
					//Added by sagar on 17/03/15 , As per discussion with P.Sali , Start..
					//errorMap = genValidate.genVal("W_SORDFORM_ATT" ,objContext,dom );//code added by sagar on 17/03/15
					if(!errorMap.isEmpty())
					{
						System.out.println(">>>>>>>>Return Non empty errorMap in SordAtt:"+errorMap.size());
						Iterator iterator = errorMap.entrySet().iterator();
						while (iterator.hasNext()) 
						{
							Map.Entry mapEntry = (Map.Entry) iterator.next();
							errCode= (String) mapEntry.getKey();
							errFldName= (String) mapEntry.getValue();
							System.out.println(">>>>>>>>errFldName:"+errFldName);
							System.out.println(">>>>>>>>errCode:"+errCode);
							if (errCode != null && errCode.trim().length() > 0)
							{
								errString = getErrorString(errFldName, errCode, userId);
								break;
							}
						}
					}
					else
					{
						System.out.println(">>>>In SordAtt errorMap is empty:"+errorMap);
					}
					//Added by sagar on 17/03/15 , As per discussion with P.Sali , End.
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName :: "+childNodeName);
						if (childNodeName.equals("price_list__disc"))
						{
							stmt = conn.createStatement();
							String priceListDisc = "",listType = "";
							if (childNode.getFirstChild() != null)
							{
								priceListDisc = childNode.getFirstChild().getNodeValue().trim();
								if (priceListDisc.trim().length() > 0)
								{
									sql = "SELECT DISTINCT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = '"+priceListDisc+"'";
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										listType = rs.getString("LIST_TYPE");
										if (!(listType.equalsIgnoreCase("M") || listType.equalsIgnoreCase("N")))
										{
											errCode = "VPLSTYPE";
											errString = getErrorString("price_list__disc",errCode,userId);
											break;
										}										
									}
								}
							}
							stmt.close();
							stmt = null;
						}
						/*else if (childNodeName.equals("no_art"))
						{
							if (childNode.getFirstChild() == null)
							{
								errCode = "VTNOARTNL";
								errString = getErrorString("no_art",errCode,userId);
								break;
							}
						}*/
						else if (childNodeName.equals("rate__clg"))
						{
							if (childNode.getFirstChild() != null)
							{
								String rateClg = "";
								rateClg = childNode.getFirstChild().getNodeValue().trim();
								if (Double.parseDouble(rateClg) < 0)
								{
									errCode = "VTNCLGRAT";
									errString = getErrorString("rate__clg",errCode,userId);
									break;
								}
							}

						}
						else if (childNodeName.equals("quantity") || childNodeName.equals("rate"))
						{
							if (childNode.getFirstChild() != null)
							{
								String quanRate = "";
								quanRate = childNode.getFirstChild().getNodeValue().trim();
								if (Double.parseDouble(quanRate) < 0)
								{
									errCode = "VTNEGQTY1";
									errString = getErrorString(childNodeName,errCode,userId);
									break;
								}
							}

						}
						else if (childNodeName.equals("unit") || childNodeName.equals("unit__rate"))
						{
							stmt = conn.createStatement();
							String unit = "",unitStd = "";
							int cnt;
							if (childNode.getFirstChild() == null)
							{
								errCode = "VMUNIT1";
								errString = getErrorString(childNodeName,errCode,userId);
								break;
							}
							unit = childNode.getFirstChild().getNodeValue().trim();
							unitStd = getColumnValue("unit__std",dom,"2");
							sql = "SELECT COUNT(*) AS COUNT FROM UOM WHERE UNIT = '"+unit+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								cnt = rs.getInt("COUNT");
								if (cnt == 0)
								{
									errCode = "VTUNIT1";
									errString = getErrorString(childNodeName,errCode,userId);
									break;
								}
								else
								{
									if (!(unit.equalsIgnoreCase(unitStd)))
									{
										sql = "SELECT COUNT(*) AS COUNT FROM UOMCONV WHERE UNIT__FR = '"+unit+"' AND UNIT__TO = '"+unitStd+"'";
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											cnt = rs.getInt("COUNT");
											if (cnt == 0)
											{
												errCode = "VTUOMCONV";
												errString = getErrorString(childNodeName,errCode,userId);
												break;
											}
										}
									}
								}
							}
							stmt.close();
							stmt = null;
						}
						else if (childNodeName.equals("item_code"))
						{
							String black_listed_yn = "",ls_stop_business = "",itemCode = "",phyItemType = "",itemType = "";
							siteCode = getColumnValue("site_code",dom1);
							custCode = getColumnValue("cust_code",dom1);
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue().trim().length() > 0)
							{
								stmt = conn.createStatement();
								itemCode = childNode.getFirstChild().getNodeValue().toString().trim();
								sql = "SELECT ITEM_TYPE FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									phyItemType = rs.getString("ITEM_TYPE");
									if (phyItemType == null)
									{
										phyItemType = "";
									}
								}
								itemType = getColumnValue("item_type",dom,"2");

								if (itemType == null)
								{
									itemType = "";
								}
								if (!itemType.equals(phyItemType))
								{
									errCode = "VTITEMTYPE";
									errString = getErrorString("item_code",errCode,userId);
									break;

								}
								sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									itemSer = rs.getString("ITEM_SER");
								}
								sql = "SELECT BLACK_LISTED FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"' AND ITEM_SER = '"+itemSer+"'";
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
								   black_listed_yn = rs.getString("BLACK_LISTED");
								   if (black_listed_yn.equalsIgnoreCase("Y"))
								   {
									   errCode = "VTCUSTCD3";
									   errString = getErrorString("item_code",errCode,userId);
									   break;
								   }
								   else
								   {
									   errCode = isCustomer(siteCode,custCode,"",conn);
									   if (errCode.trim().length() == 0)
									   {
										   sql = "SELECT STOP_BUSINESS FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
										   rs = stmt.executeQuery(sql);
										   if (rs.next())
										   {
												ls_stop_business  = rs.getString("STOP_BUSINESS");
												if (ls_stop_business.equalsIgnoreCase("Y"))
												{
													errCode = "VTICC";
													errString = getErrorString("item_code",errCode,userId);
													break;
												}
										   }
									   }
									   else
									   {
										   errString = getErrorString("item_code",errCode,userId);
										   break;
									   }

									}
								}
								if (errCode.trim().length() == 0)
								{
									errCode = isItem(siteCode,itemCode,"",conn);
									if (errCode.trim().length() > 0)
									{
										errString = getErrorString("item_code",errCode,userId);
										break;
									}
								}
								stmt.close();
								stmt = null;
							}
						}
						else if (childNodeName.equals("item_type"))
						{
							if (childNode.getFirstChild() == null)
							{
								errCode = "VUITEM";
								errString = getErrorString("item_type",errCode,userId);
								break;
							}	    
						}
						/*else if (childNodeName.equals("phy_attrib_12"))
						{
							if (childNode.getFirstChild() == null)
							{
								errCode = "VTCOLNULL";
								errString = getErrorString("phy_attrib_12",errCode,userId);
								break;
							}
						}*/
						else if (childNodeName.equals("phy_attrib_13") || childNodeName.equals("phy_attrib_14") || childNodeName.equals("phy_attrib_15"))
						{
							String colorPref = "",altColor1 = "",altColor2 = "",altColor3 = "";
							colorPref = getColumnValue("phy_attrib_12",dom,"2");
							altColor1 = getColumnValue("phy_attrib_13",dom,"2");
							altColor2 = getColumnValue("phy_attrib_14",dom,"2");
							altColor3 = getColumnValue("phy_attrib_15",dom,"2");
							if (colorPref != null && colorPref.trim().equals("S"))
							{
								if (altColor1 != null && altColor1.trim().length() >0)
								{
									errCode = "VTALTCOL";
									errString = getErrorString("phy_attrib_13",errCode,userId);
									break;
								}
								else if (altColor2 != null && altColor2.trim().length() > 0)
								{
									errCode = "VTALTCOL";
									errString = getErrorString("phy_attrib_14",errCode,userId);
									break;
								}
								else if (altColor3 != null && altColor3.trim().length() > 0)
								{
									 errCode = "VTALTCOL";
									 errString = getErrorString("phy_attrib_15",errCode,userId);
									 break;
								}
							}
						}
						else if (totalColname != null && totalColname.trim().length() > 0 && totalColname.equalsIgnoreCase(childNodeName))
						{
							String blueColName,yellowColName,otherColName;
							String totalQty,yellowQty,blueQty,othQty;
							double total = 0,blue = 0,yellow = 0,other = 0;
							
							System.out.println("Inside Validation For Total Qty :: "+totalColname+" Child Node Name "+childNodeName);
							totalQty = getColumnValue(totalColname.toLowerCase(),dom,"2");
							
							if (totalQty != null && totalQty.trim().length() > 0)
							{
								total = Double.parseDouble(totalQty);
							}								
							blueColName = getAttributeColumn("Blue",itemTypeStr,conn);
							if (blueColName != null && blueColName.trim().length() > 0)
							{
								blueQty = getColumnValue(blueColName.toLowerCase(),dom,"2");
								if (blueQty != null && blueQty.trim().length() > 0)
								{
									blue = Double.parseDouble(blueQty);
								}
							}
							yellowColName = getAttributeColumn("YELLOW",itemTypeStr,conn);
							if (yellowColName != null && yellowColName.trim().length() > 0)
							{
								yellowQty = getColumnValue(yellowColName.toLowerCase(),dom,"2");
								if (yellowQty != null && yellowQty.trim().length() > 0)
								{
									yellow = Double.parseDouble(yellowQty);
								}
							}
							otherColName = getAttributeColumn("OTHER",itemTypeStr,conn);
							if (otherColName != null && otherColName.trim().length() > 0)
							{
								othQty = getColumnValue(otherColName.toLowerCase(),dom,"2");
								if (othQty != null && othQty.trim().length() > 0)
								{
									other = Double.parseDouble(othQty);
								}
							}
							double totOfQty = blue+yellow+other;
							System.out.println("totOfQty "+totOfQty);
							System.out.println("total "+total);
							if (total != totOfQty)
							{
								errCode = "VTTOTNTMH";
								errString = getErrorString(totalColname,errCode,userId);
								break;
							}
						}
						else if (childNodeName.equals("phy_attrib_1") || childNodeName.equals("phy_attrib_2") || childNodeName.equals("phy_attrib_3") || childNodeName.equals("phy_attrib_4") || childNodeName.equals("phy_attrib_5") || childNodeName.equals("phy_attrib_6") || childNodeName.equals("phy_attrib_7") || childNodeName.equals("phy_attrib_8") || childNodeName.equals("phy_attrib_9") || childNodeName.equals("phy_attrib_10") || childNodeName.equals("phy_attrib_11") || childNodeName.equals("phy_attrib_12") || childNodeName.equals("phy_attrib_13") || childNodeName.equals("phy_attrib_14") || childNodeName.equals("phy_attrib_15") || childNodeName.equals("phy_attrib_16") || childNodeName.equals("phy_attrib_17") || childNodeName.equals("phy_attrib_18"))
						{
							String otherColumnValue = "",otherColumnName = "",otherColValue = "",otherCol = "";
							otherCol = getAttributeColumn("OTHER",itemTypeStr,conn);
							if (otherCol != null)
							{
								otherColValue = getColumnValue(otherCol.toLowerCase(),dom,"2");
							}
							otherColumnName = getAttributeColumn("Other Col",itemTypeStr,conn);
							if (otherCol != null)
							{
								otherColumnValue = getColumnValue(otherColumnName.toLowerCase(),dom,"2");
							}
							System.out.println("otherCol "+otherCol);
							System.out.println("otherColValue "+otherColValue);
							System.out.println("otherColumnName "+otherColumnName);
							System.out.println("otherColumnValue "+otherColumnValue);
							
							if (otherColValue != null && otherColValue.trim().length() > 0 && Integer.parseInt(otherColValue) > 0)
							{
								if (otherColumnValue == null || otherColumnValue.trim().length() == 0)
								{
									errCode = "VTNLCOLR";
									errString = getErrorString(otherColumnName,errCode,userId);
									break;
								}
							}
							String strictFlexAnyCol = "",strictFlexAnyValue = "",altColor1Col = "",altColor1Value ="";
							strictFlexAnyCol = getAttributeColumn("Strict/Flex/Any",itemTypeStr,conn);
							if (strictFlexAnyCol != null)
							{
								strictFlexAnyValue = getColumnValue(strictFlexAnyCol.toLowerCase(),dom,"2");
							}
							if (strictFlexAnyValue != null && strictFlexAnyValue.trim().length() > 0 && strictFlexAnyValue.equalsIgnoreCase("F"))
							{
								altColor1Col = getAttributeColumn("Alt Color 1",itemTypeStr,conn);
								if (altColor1Col != null)
								{
									altColor1Value = getColumnValue(altColor1Col.toLowerCase(),dom,"2");
									if (altColor1Value == null || altColor1Value.trim().length() == 0)
									{
										errCode = "VTALTCOL";
										errString = getErrorString(otherColumnName,errCode,userId);
										break;
									}
								}
							}
							
							/*GenVal genValidate = new GenVal();
							errCode = genValidate.genVal("W_SORDFORM_ATT" ,objContext,dom ,conn);
							if (errCode != null && errCode.trim().length() > 0)
							{
								errString = getErrorString("", errCode, userId);
								break;
							}*/
						}						
					}					
					break;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception se){}
		}
		System.out.println("ErrString ::"+errString);
		return errString;
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
			dom = parseString(xmlString); //returns the DOM Object for the passed XML Stirng
			dom1 = parseString(xmlString1); //returns the DOM Object for the passed XML Stirng

			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SordAttEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		int n;
		int currentFormNo = 0;
		Connection connSatt = null;
		Statement stmtSatt = null;
		ResultSet rsSatt = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		StringBuffer valueXmlString = new StringBuffer();
		String sql = "";
		String columnValue = "";
		String retValue1 = "";
		String retValue2 = "";
		String retValue3 = "";
		String loginSite = "";
		ArrayList acctCodeSal = new ArrayList();
		String format = "",mempCode="",currCode="",exchRate="",custCode = "";
		String addr1 = "",addr2 = "",addr3 = "",city = "",pin = "",countCode = "";
		String stateCode = "",tele1 = "",tele2 = "",tele3 = "",fax = "",stanCode = "";
		String bankCode,transMode,rcpMode,tranCode,locGrp;
		String cctrCodeSal = "",itemSer="",crTerm = "",crTermDescr = "",siteCode = "";
		String orderType = "",priceList = "",priceListDesc = "",orderDateStr = "",contactNo = "";
		String dlvTerm = "",frtTerm = "",custCodeBil = "",custBilName = "",returnValue = "";
		String plistOrderType = "",priceListClg = "",contractNo = "",spName2 = "",spName3 = "";
		String salesPers = "",priceListDiscount = "",pListDiscount = "",orderDate = "";
		double exchRateValue = 0,num = 0;
		String spName1="";
		NodeList parentNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		NodeList childNodeList = null;
		String childNodeName = "";
		int ctr = 0;

		NodeList detCtxNodeList = null;
		NodeList detCtxChildNodeList = null;
		Node detCtxChildNode = null;
		String detchildNodeName = "";
		int nextInstallNo = 0;
		int nextInstallNoDb = 0;
		int nextInstallNoDom = 0;
		String prevRefNo = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon disCommon = new DistCommon();
		DistDiscount distDiscountEJB = new DistDiscount();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			format= getApplDateFormat();
		}
		catch(Exception e)
		{
			System.out.println("Exception :[SordAttEJB]While Getting Date Format"+e.getMessage());		
		}
		
		java.util.Date DateX = new java.util.Date();
		java.text.SimpleDateFormat dtf= new SimpleDateFormat(format);
		String userId = getValueFromXTRA_PARAMS(xtraParams,"userId");
		String empCode = getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
		loginSite = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");

		try
		{
			connSatt = getConnection(); //This function is to connect with oracle....
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}

			System.out.println("[SordAttEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</header>\r\n");
											
			switch (currentFormNo)
			{
				case 1:
					
					/* Searching the dom for the incoming column value start*/
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
					/* Searching the dom for the incoming column value end*/
				
					valueXmlString.append("<Detail>\r\n");
					
					if (currentColumn.trim().equals("itm_default"))
					{
						stmtSatt = connSatt.createStatement();
						valueXmlString.append("<emp_code__ord>").append(empCode).append("</emp_code__ord\r\n>");
						sql="SELECT CURR_CODE__PURC FROM  PURCCTRL";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							currCode = rs.getString("CURR_CODE__PURC");
							if (currCode != null && currCode.trim().length() > 0)
							{
								sql = "SELECT STD_EXRT FROM CURRENCY WHERE CURR_CODE = '"+currCode+"'";	
								rs =  stmtSatt.executeQuery(sql);
								if (rs.next())
								{
									exchRateValue = rs.getDouble("STD_EXRT");
								}
							}
							valueXmlString.append("<curr_code>").append(currCode).append("</curr_code\r\n>");
							valueXmlString.append("<exch_rate>").append(exchRateValue).append("</exch_rate\r\n>");
						}
						valueXmlString.append("<site_code>").append(loginSite).append("</site_code>");
						valueXmlString.append("<site_code__ship>").append(loginSite).append("</site_code__ship>");
						valueXmlString.append("<tax_date>").append(dtf.format(DateX)).append("</tax_date>");
						valueXmlString.append("<status_date>").append(dtf.format(DateX)).append("</status_date>");
						valueXmlString.append("<ord_date>").append(dtf.format(DateX)).append("</ord_date>");
						valueXmlString.append("<pl_date>").append(dtf.format(DateX)).append("</pl_date>");
						valueXmlString.append("<due_date>").append(dtf.format(DateX)).append("</due_date>");
			
						valueXmlString.append("<price_list protect=\"0\"></price_list>\r\n");	
						valueXmlString.append("<cust_code protect=\"0\"></cust_code>\r\n");	
						valueXmlString.append("<cust_code__bil protect=\"0\"></cust_code__bil>\r\n");	
						valueXmlString.append("<item_ser protect=\"0\"></item_ser>\r\n");	
						valueXmlString.append("<price_list__disc protect=\"0\"></price_list__disc>\r\n");	
						valueXmlString.append("<order_type protect=\"0\"></order_type>\r\n");	
						
						String dueDays = "",dueDate = "";
						dueDays = disCommon.getDisparams("999999","DELV_DAYS",connSatt);
						if (!(dueDays.equalsIgnoreCase("NULLFOUND")))
						{
							int noOfDay = Integer.parseInt(dueDays);
							if (noOfDay > 0)
							{
								dueDate = getRelativeDate(noOfDay);
								valueXmlString.append("<due_date>").append(dueDate).append("</due_date>");
							}
						}						
						valueXmlString.append("<confirmed>").append("N").append("</confirmed>");
						valueXmlString.append("<tax_opt>").append("L").append("</tax_opt>"); // To be changed, in datawindow it is set.
					}
					if (currentColumn.trim().equals("itm_defaultedit"))
					{
						String priceListEdit = "",custCodeEdit = "",custCodeBilEdit = "",itemSerEdit = "",priceListDiscEdit = "";
						String orderTypeEdit = "";
						priceListEdit = getColumnValue("price_list",dom);
						custCodeEdit = getColumnValue("cust_code",dom);
						custCodeBilEdit = getColumnValue("cust_code__bil",dom);
						itemSerEdit = getColumnValue("item_ser",dom);
						priceListDiscEdit = getColumnValue("price_list__disc",dom);
						orderTypeEdit = getColumnValue("order_type",dom);
						valueXmlString.append("<price_list protect=\"1\">").append(priceListEdit+"</price_list>\r\n");	
						valueXmlString.append("<cust_code protect=\"1\">").append(custCodeEdit+"</cust_code>\r\n");	
						valueXmlString.append("<cust_code__bil protect=\"1\">").append(custCodeBilEdit+"</cust_code__bil>\r\n");	
						valueXmlString.append("<item_ser protect=\"1\">").append(itemSerEdit+"</item_ser>\r\n");	
						valueXmlString.append("<price_list__disc protect=\"1\">").append(priceListDiscEdit+"</price_list__disc>\r\n");	
						valueXmlString.append("<order_type protect=\"1\">").append(orderTypeEdit+"</order_type>\r\n");	
					}
					else if (currentColumn.trim().equals("item_ser"))
					{
						String itemDescr = "";
						if(columnValue != null)
						{
							try
							{
								sql = "SELECT DESCR FROM ITEMSER WHERE ITEM_SER ='"+columnValue+"' ";
								stmtSatt = connSatt.createStatement();
								rsSatt = stmtSatt.executeQuery(sql);
								while (rsSatt.next())
								{
									itemDescr = rsSatt.getString(1);
								}
								rsSatt.close();
								stmtSatt.close();
								valueXmlString.append("<itemser_descr>").append(itemDescr).append("</itemser_descr>");
							}
							catch(Exception e)
							{
								System.out.println("Exception :[SordAttEJB][itemChanged::case 1::item_ser] :==>\n"+e.getMessage());
								throw new ITMException(e);
							}
					
						}
					}
					//Cust code case added from sorder 29/06/06
					else if(currentColumn.trim().equals("cust_code"))
					{
						stmtSatt = connSatt.createStatement();
						itemSer =  genericUtility.getColumnValue("item_ser",dom);
						custCode = genericUtility.getColumnValue(currentColumn,dom);
						String terrCode = "",terrDescr = "";
						sql = "SELECT TERR_CODE FROM CUSTOMER_SERIES WHERE CUST_CODE ='"+custCode+"'";
						System.out.println("SQL :: "+sql);
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{								
							terrCode = rs.getString("TERR_CODE");
							if (terrCode == null || terrCode.trim().length() == 0)
							{
								sql = "SELECT TERR_CODE FROM SITE_CUSTOMER WHERE CUST_CODE ='"+custCode+"'";
								rs = stmtSatt.executeQuery(sql);
								if (rs.next())
								{
									terrCode = rs.getString("TERR_CODE");	
									if (terrCode == null || terrCode.trim().length() == 0)
									{
										sql = "SELECT TERR_CODE FROM CUSTOMER WHERE CUST_CODE ='"+custCode+"'";
										rs = stmtSatt.executeQuery(sql);
										if (rs.next())
										{
											terrCode = rs.getString("TERR_CODE");		
										}
									}
								}
							}
						}
						if (terrCode != null && terrCode.trim().length() > 0)
						{
							sql = "SELECT DESCR FROM TERRITORY WHERE TERR_CODE ='"+terrCode+"'";
							rs = stmtSatt.executeQuery(sql);
							if (rs.next())
							{
								terrDescr = rs.getString("DESCR");								
							}
							valueXmlString.append("<terr_code>").append(terrCode).append("</terr_code>");
							valueXmlString.append("<territory_descr>").append(terrDescr).append("</territory_descr>");
						}	
						String salesPers1 = "",salesPers2 = "";
						sql="SELECT CR_TERM ,SALES_PERS , SALES_PERS__1,SALES_PERS__2 FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"' AND ITEM_SER  = '"+itemSer+"'";
						System.out.println("SQL :: "+sql);
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							crTerm = rs.getString("CR_TERM");
							salesPers = rs.getString("SALES_PERS");
							salesPers1 = rs.getString("SALES_PERS__1");
							salesPers2 = rs.getString("SALES_PERS__2");
						}
						String custCrTerm = "",custSalesPers = "",custSalesPers1 = "",custSalesPers2 = "";
						String locGroup = "",ordType = "";
						sql = "SELECT CR_TERM ,SALES_PERS , SALES_PERS__1,SALES_PERS__2,LOC_GROUP,ORDER_TYPE FROM CUSTOMER WHERE CUST_CODE ='"+custCode+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							custCrTerm = rs.getString("CR_TERM");
							custSalesPers = rs.getString("SALES_PERS");
							custSalesPers1 = rs.getString("SALES_PERS__1");
							custSalesPers2 = rs.getString("SALES_PERS__2");
							locGroup = rs.getString("LOC_GROUP");
							ordType = rs.getString("ORDER_TYPE");
						}

						if (crTerm == null || crTerm.trim().length() == 0)
						{
							crTerm = custCrTerm;							
						}
						if (salesPers == null || salesPers.trim().length() == 0)
						{
							salesPers = custSalesPers;
						}
						if (salesPers1 == null || salesPers1.trim().length() == 0)
						{
							salesPers1 = custSalesPers1;
						}
						if (salesPers2 == null || salesPers2.trim().length() == 0)
						{
							salesPers2 = custSalesPers2;
						}						
						
						if (locGroup != null)
						{
							valueXmlString.append("<loc_group>").append(locGroup).append("</loc_group>\r\n");
						}
						if (ordType != null)
						{
							String ordTDescr = "";
							//itemchange of order_type to be called
							sql = "SELECT DESCR FROM GENCODES WHERE FLD_NAME = 'ORDER_TYPE' AND MOD_NAME = 'W_SORDFORM_ATT'"+
								"AND FLD_VALUE = '"+ordType+"'";
							stmtSatt = connSatt.createStatement();
							rsSatt = stmtSatt.executeQuery(sql);
							if (rsSatt.next())
							{
								ordTDescr = rsSatt.getString(1);
							}
							valueXmlString.append("<order_type>").append(ordType).append("</order_type>\r\n");
							valueXmlString.append("<descr>").append(ordTDescr).append("</descr>");
						}

						if (ordType != null && ordType.trim().length() > 0)
						{
							String crTermMap = "";
							sql = "SELECT CR_TERM_MAP FROM CR_TERM_MAPPING WHERE CR_TERM = '"+crTerm+"' "+ 
									"AND ORD_TYPE = '"+ordType+"'";
							rs =  stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								crTermMap = rs.getString("CR_TERM_MAP");
								if (crTermMap != null && crTermMap.trim().length() > 0)
								{
									crTerm = crTermMap;
								}								
							}
						}
						if (crTerm != null && crTerm.trim().length() > 0)
						{
							String crtermDescr = "";
							sql = "SELECT DESCR FROM CRTERM WHERE CR_TERM = '"+crTerm+"'";
						 	rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								crtermDescr = rs.getString("DESCR");
							}
							valueXmlString.append("<cr_term>").append(crTerm).append("</cr_term\r\n>");
							valueXmlString.append("<crterm_descr>").append(crtermDescr).append("</crterm_descr\r\n>");
						}
						siteCode = genericUtility.getColumnValue("site_code",dom);
						priceList = distDiscountEJB.priceListSite(siteCode,custCode,connSatt);
						
						valueXmlString.append("<price_list>").append(priceList).append("</price_list\r\n>");
																
						String custCodeBill = "";
						sql ="SELECT CUST_NAME, CUST_CODE__BIL, TAX_CHAP, TAX_CLASS FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
						rs =  stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							valueXmlString.append("<cust_name>").append(rs.getString(1)).append("</cust_name\r\n>");
							valueXmlString.append("<dlv_to>").append(rs.getString(1)).append("</dlv_to\r\n>");
							valueXmlString.append("<cust_code__bil>").append(rs.getString(2)).append("</cust_code__bil\r\n>");
							if(rs.getString(3) != null)
								valueXmlString.append("<tax_chap>").append(rs.getString(3) == null ? "":rs.getString(3)).append("</tax_chap\r\n>");
							valueXmlString.append("<tax_class>").append(rs.getString(4) == null ? "":rs.getString(4)).append("</tax_class\r\n>");
							custCodeBill = rs.getString(2);
							sql ="SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = '"+custCodeBill+"'";
							rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								valueXmlString.append("<customer_cust_name>").append(rs.getString(1)).append("</customer_cust_name\r\n>");
								valueXmlString.append("<cust_name__bil>").append(rs.getString(1)).append("</cust_name__bil\r\n>");
							} 
						}
					 	
						sql="SELECT	CUST_NAME, ADDR1, ADDR2,ADDR3,CITY, PIN, COUNT_CODE, STAN_CODE,TRAN_CODE, STATE_CODE,CURR_CODE,BANK_CODE, TRANS_MODE, RCP_MODE, TELE1, TELE2, TELE3,FAX,LOC_GROUP FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							addr1 = rs.getString(2);
							addr2 = rs.getString(3);
							addr3 = rs.getString(4);
							city= rs.getString(5);
							pin = rs.getString(6);
							countCode = rs.getString(7);
							stateCode = rs.getString(10);
							tele1 = rs.getString(15);
							tele2 = rs.getString(16);
							tele3 = rs.getString(17);
							fax = rs.getString(18);;
							stanCode = rs.getString(8);
							currCode = rs.getString(11);
							bankCode = rs.getString(12);
							transMode = rs.getString(13);
							rcpMode = rs.getString(14);
							tranCode = rs.getString(9);
							locGrp =  rs.getString(19);
							valueXmlString.append("<cust_code__dlv>").append(custCode).append("</cust_code__dlv>\r\n");
							valueXmlString.append("<dlv_to>").append(rs.getString(1)).append("</dlv_to>\r\n");
							if(addr1 != null)
								valueXmlString.append("<dlv_add1>").append(addr1).append("</dlv_add1>\r\n");
							if(addr2 != null)
								valueXmlString.append("<dlv_add2>").append(addr2).append("</dlv_add2>\r\n");
							if(addr3 != null)
								valueXmlString.append("<dlv_add3>").append(addr3).append("</dlv_add3>\r\n");
							if(city != null)
								valueXmlString.append("<dlv_city>").append(city).append("</dlv_city>\r\n");
							if(pin != null)
								valueXmlString.append("<dlv_pin>").append(pin).append("</dlv_pin>\r\n");
							if(countCode != null)
								valueXmlString.append("<count_code__dlv>").append(countCode).append("</count_code__dlv>\r\n");
							if(tranCode != null)
								valueXmlString.append("<tran_code>").append(tranCode).append("</tran_code>\r\n");
							if(stanCode != null)
								valueXmlString.append("<stan_code>").append(stanCode).append("</stan_code>\r\n");
							if(stateCode != null)
								valueXmlString.append("<state_code__dlv>").append(stateCode).append("</state_code__dlv>\r\n");
							if(tele1 != null)
								valueXmlString.append("<tel1__dlv>").append(tele1).append("</tel1__dlv>\r\n");
							if(tele2 != null)
								valueXmlString.append("<tel2__dlv>").append(tele2).append("</tel2__dlv>\r\n");
							if(tele3 != null )
								valueXmlString.append("<tel3__dlv>").append(tele3).append("</tel3__dlv>\r\n");	
							if(fax != null)
								valueXmlString.append("<fax__dlv>").append(fax).append("</fax__dlv>\r\n");
							if(currCode != null)
							{
								valueXmlString.append("<curr_code>").append(currCode).append("</curr_code>\r\n");
								valueXmlString.append("<curr_code__ins>").append(currCode).append("</curr_code__ins>\r\n");
								valueXmlString.append("<curr_code__frt>").append(currCode).append("</curr_code__frt>\r\n");
							}
							if(bankCode != null)
								valueXmlString.append("<bank_code>").append(bankCode).append("</bank_code>\r\n");
							if(transMode != null)
								valueXmlString.append("<trans_mode>").append(transMode).append("</trans_mode>\r\n");
							if(rcpMode != null)
								valueXmlString.append("<rcp_mode>").append(rcpMode).append("</rcp_mode>\r\n");
							if(locGrp != null)
							valueXmlString.append("<loc_group>").append(locGrp).append("</loc_group>\r\n"); 

							sql="SELECT DESCR FROM CURRENCY WHERE CURR_CODE = '"+currCode+"'";
							rs= stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								valueXmlString.append("<currency_descr>").append(currCode).append("</currency_descr>\r\n");
							}
							String tranDate = "";
							tranDate =genericUtility.getColumnValue("ord_date",dom);
							exchRateValue = getDailyExchRateSellBuy(currCode, "", siteCode, tranDate, "S");
							valueXmlString.append("<exch_rate>").append(exchRateValue).append("</exch_rate>\r\n");
							valueXmlString.append("<exch_rate__frt>").append(exchRateValue).append("</exch_rate__frt>\r\n");
							valueXmlString.append("<exch_rate__ins>").append(exchRateValue).append("</exch_rate__ins>\r\n");
							sql ="SELECT DESCR FROM STATION WHERE STAN_CODE ='"+stanCode+"'";
							rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								valueXmlString.append("<station_descr>").append(rs.getString(1)).append("</station_descr>\r\n");
							}
							sql="SELECT TRAN_NAME, (CASE WHEN FRT_TERM IS NULL THEN 'b' ELSE FRT_TERM END) ,CURR_CODE FROM TRANSPORTER WHERE TRAN_CODE = '"+tranCode+"'";
							rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								valueXmlString.append("<tran_name>").append(rs.getString(1)).append("</tran_name>\r\n");
								valueXmlString.append("<frt_term>").append(rs.getString(2)).append("</frt_term>\r\n");
								valueXmlString.append("<curr_code__frt>").append(rs.getString(3)).append("</curr_code__frt>\r\n");
							}
						}
						siteCode = genericUtility.getColumnValue("site_code",dom);
						if (siteCode != null)
							valueXmlString.append("<site_code__ship>").append(siteCode).append("</site_code__ship>\r\n"); 
						custCode = genericUtility.getColumnValue("cust_code",dom);
						orderType = genericUtility.getColumnValue("order_type",dom);
						String priceListDisc = "";
						sql="SELECT PRICE_LIST__DISC FROM SITE_CUSTOMER WHERE CUST_CODE = '"+custCode+"' and site_code = '"+siteCode+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							priceListDisc = rs.getString(1);
							if(rs.getString(1) == null || rs.getString(1).length() == 0)
							{
								 sql ="SELECT PRICE_LIST__DISC FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
								 rs = stmtSatt.executeQuery(sql);
								 if(rs.next());
								 {
									priceListDisc = rs.getString(1);
								 }
							}
						}
						sql="SELECT LTRIM(RTRIM(ORDER_TYPE)) FROM PRICELIST WHERE PRICE_LIST = '"+priceListDisc+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							plistOrderType = rs.getString(1);
							valueXmlString.append("<price_list__disc>").append(priceListDisc).append("</price_list__disc>\r\n");
						}
						sql ="SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='PRICE_LIST__CLG'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							priceListClg = rs.getString(1);
						}
						// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
						if ( stmtSatt != null )
						{
							stmtSatt.close();
							stmtSatt = null;
						}
						if ( rs != null )
						{
							rs.close();
							rs = null;
						}
						// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
						
						if(priceListClg != null)
						{
							valueXmlString.append("<price_list__clg>").append(priceListClg).append("</price_list__clg>\r\n");
						} 
						sql="SELECT MARKET_REG,EMAIL_ADDR FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							String mReg = rs.getString(1);
							String emailAdd = rs.getString(2);
							if (mReg != null && mReg.trim().length() > 0)
							{
								valueXmlString.append("<market_reg>").append(mReg).append("</market_reg>\r\n");
							}
							if (emailAdd != null)
							{
								valueXmlString.append("<email_addr>").append(emailAdd).append("</email_addr>\r\n");
							}
						}
						sql="SELECT DLV_TERM FROM CUSTOMER_SERIES WHERE CUST_CODE ='"+custCode+"' AND ITEM_SER = '"+itemSer+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							dlvTerm = rs.getString("DLV_TERM");
							if (dlvTerm == null || dlvTerm.trim().length() == 0)
							{
								sql="SELECT DLV_TERM FROM CUSTOMER WHERE  CUST_CODE = '"+custCode+"'";
								rs = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									dlvTerm = rs.getString("DLV_TERM");
									if(dlvTerm == null || dlvTerm.trim().length() == 0) 
									{
										dlvTerm = "NA";
									}
								}
							}							
						}
						else
						{
							dlvTerm = "NA";
						}
						valueXmlString.append("<dlv_term>").append(dlvTerm).append("</dlv_term>\r\n");
						itemSer = genericUtility.getColumnValue("item_ser",dom);
						sql="SELECT CONTRACT_NO FROM SCONTRACT WHERE SITE_CODE = '"+siteCode+"' AND CUST_CODE = '"+custCode+"' AND ITEM_SER = '"+itemSer+"' AND CONFIRMED = 'Y' AND (CASE WHEN STATUS IS NULL THEN ' ' ELSE STATUS END) <> 'x' ORDER BY CONTRACT_NO";
						rs = stmtSatt.executeQuery(sql); 
						if(rs.next())
						{
							contractNo = rs.getString(1);
						}
						if (contractNo.trim().length() > 0)
						{
							valueXmlString.append("<contract_no>").append(contractNo).append("</contract_no>\r\n");
						}
						stmtSatt.close();
					}
					//Case added from sorder 29/06/06
					else if(currentColumn.trim().equals("cust_code__dlv"))
					{   
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						sql="SELECT	CUST_NAME, ADDR1, ADDR2,ADDR3, CITY, PIN, COUNT_CODE, STAN_CODE,TRAN_CODE, STATE_CODE, TELE1, TELE2, TELE3,FAX FROM CUSTOMER WHERE CUST_CODE = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							addr1 = rs.getString(2);
							addr2 = rs.getString(3);
							addr3 = rs.getString(4);
							city= rs.getString(5);
							pin = rs.getString(6);
							countCode = rs.getString(7);
							stateCode = rs.getString(10);
							tele1 = rs.getString(11);
							tele2 = rs.getString(12);
							tele3 = rs.getString(13);
							fax = rs.getString(14);;
							stanCode = rs.getString(8);
							tranCode = rs.getString(9);
							valueXmlString.append("<dlv_to>").append(rs.getString(1)).append("</dlv_to>\r\n");
							if(addr1 != null)
								valueXmlString.append("<dlv_add1>").append(addr1).append("</dlv_add1>\r\n");
							if(addr2 != null)
								valueXmlString.append("<dlv_add2>").append(addr2).append("</dlv_add2>\r\n");
							if(addr3 != null )
								valueXmlString.append("<dlv_add3>").append(addr3).append("</dlv_add3>\r\n");
							if(city != null )
								valueXmlString.append("<dlv_city>").append(city).append("</dlv_city>\r\n");
							if(pin != null)
								valueXmlString.append("<dlv_pin>").append(pin).append("</dlv_pin>\r\n");
							if(countCode != null)
								valueXmlString.append("<count_code__dlv>").append(countCode).append("</count_code__dlv>\r\n");
							if(tranCode != null)
								valueXmlString.append("<tran_code>").append(tranCode).append("</tran_code>\r\n");
							if(stanCode != null)
								valueXmlString.append("<stan_code>").append(stanCode).append("</stan_code>\r\n");
							if(stateCode != null)
								valueXmlString.append("<state_code__dlv>").append(stateCode).append("</state_code__dlv>\r\n");
							if(tele1 != null)
								valueXmlString.append("<tel1__dlv>").append(tele1).append("</tel1__dlv>\r\n");
							if(tele2 != null)
								valueXmlString.append("<tel2__dlv>").append(tele2).append("</tel2__dlv>\r\n");
							if(tele3 != null)
								valueXmlString.append("<tel3__dlv>").append(tele3).append("</tel3__dlv>\r\n");	
							if(fax != null)
								valueXmlString.append("<fax__dlv>").append(fax).append("</fax__dlv>\r\n");
														
							sql ="SELECT DESCR FROM STATION WHERE STAN_CODE ='"+stanCode+"'";
							rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								valueXmlString.append("<station_descr>").append(rs.getString(1)).append("</station_descr>\r\n");
							}
							else
							{
								valueXmlString.append("<station_descr>").append("").append("</station_descr>\r\n");
							}
						
							sql="SELECT TRAN_NAME, FRT_TERM FROM TRANSPORTER WHERE TRAN_CODE = '"+tranCode+"'";
							rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								valueXmlString.append("<tran_name>").append(rs.getString(1)).append("</tran_name>\r\n");
								valueXmlString.append("<frt_term>").append(rs.getString(2)).append("</frt_term>\r\n");
							}
							else
							{
								valueXmlString.append("<tran_name>").append("").append("</tran_name>\r\n");
								valueXmlString.append("<frt_term>").append("").append("</frt_term>\r\n");
							}
						}
						else
						{
							valueXmlString.append("<dlv_to>").append("").append("</dlv_to>\r\n");
							valueXmlString.append("<dlv_add1>").append("").append("</dlv_add1>\r\n");
							valueXmlString.append("<dlv_add2>").append("").append("</dlv_add2>\r\n");
							valueXmlString.append("<dlv_add3>").append("").append("</dlv_add3>\r\n");
							valueXmlString.append("<dlv_city>").append("").append("</dlv_city>\r\n");
							valueXmlString.append("<dlv_pin>").append("").append("</dlv_pin>\r\n");
							valueXmlString.append("<count_code__dlv>").append("").append("</count_code__dlv>\r\n");
							valueXmlString.append("<tran_code>").append("").append("</tran_code>\r\n");
							valueXmlString.append("<stan_code>").append("").append("</stan_code>\r\n");
							valueXmlString.append("<state_code__dlv>").append("").append("</state_code__dlv>\r\n");
							valueXmlString.append("<tel1__dlv>").append("").append("</tel1__dlv>\r\n");
							valueXmlString.append("<tel2__dlv>").append("").append("</tel2__dlv>\r\n");
							valueXmlString.append("<tel3__dlv>").append("").append("</tel3__dlv>\r\n");	
							valueXmlString.append("<fax__dlv>").append("").append("</fax__dlv>\r\n");
						}
						stmtSatt.close();
					}
					else if (currentColumn.trim().equals("order_type"))
					{
						String ordTDescr = "";
						if(columnValue != null)
						{
							try
							{   
								sql = "SELECT DESCR FROM GENCODES WHERE FLD_NAME = 'ORDER_TYPE' AND MOD_NAME = 'W_SORDFORM_ATT'"+
									"AND FLD_VALUE = '"+columnValue+"'";
								stmtSatt = connSatt.createStatement();
								rsSatt = stmtSatt.executeQuery(sql);
								if (rsSatt.next())
								{
									ordTDescr = rsSatt.getString(1);
								}
								valueXmlString.append("<descr>").append(ordTDescr).append("</descr>");

								//Added later as form changed to sordform 29/06/06

								custCode = genericUtility.getColumnValue("cust_code",dom);
								cctrCodeSal = acctDetrInvoice("", itemSer, "S-INV", orderType,"SAL");
								System.out.println("cctrCodeSal :----------");
								System.out.println(cctrCodeSal);
								acctCodeSal = genericUtility.getTokenList(cctrCodeSal,"~t");
								System.out.println("cctrCodeSal :----------");
								System.out.println(cctrCodeSal);
							//	if(acctCodeSal != null)
								//	valueXmlString.append("<cctr_code__sal>").append(cctrCodeSal).append("</cctr_code__sal\r\n>");
							//	if(cctrCodeSal != null)
								//	valueXmlString.append("<acct_code__sal>").append(acctCodeSal).append("</acct_code__sal\r\n>");
								crTerm =  genericUtility.getColumnValue("cr_term",dom);
								sql ="SELECT CR_TERM_MAPPING.CR_TERM_MAP FROM CR_TERM_MAPPING WHERE (CR_TERM_MAPPING.CR_TERM='"+crTerm+"') AND  ( CR_TERM_MAPPING.ORD_TYPE ='"+orderType+"')";
								rs = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									crTerm = rs.getString(1);
									if(crTerm == null)
									{
										sql ="SELECT CR_TERM FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"' AND ITEM_SER  = '"+itemSer+"'";
										rs = stmtSatt.executeQuery(sql);
										if(rs.next())
										{
											crTerm = rs.getString(1);
										}
									}
								}
								valueXmlString.append("<cr_term>").append(crTerm).append("</cr_term\r\n>");

								sql="SELECT DESCR FROM CRTERM WHERE CR_TERM = '"+crTerm+"'";
								rs = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									crTermDescr = rs.getString(1);
									valueXmlString.append("<crterm_descr>").append(crTermDescr).append("</crterm_descr\r\n>");
								}
								else
								{
									valueXmlString.append("<crterm_descr>").append("").append("</crterm_descr\r\n>");
								}
								siteCode = genericUtility.getColumnValue("site_code",dom);
								sql ="SELECT PRICE_LIST__DISC FROM SITE_CUSTOMER WHERE CUST_CODE ='"+custCode+"'AND SITE_CODE ='"+siteCode+"'"; 
								rs  = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									priceListDesc = rs.getString(1);
									if(priceListDesc == null || priceListDesc.length() == 0)
									{
										sql = "SELECT PRICE_LIST__DISC FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
										rs  = stmtSatt.executeQuery(sql);
										if(rs.next())
										{
											priceListDesc = rs.getString(1);
										}
									}
								}

								sql ="SELECT LTRIM(RTRIM(ORDER_TYPE)) FROM PRICELIST WHERE PRICE_LIST = '"+priceList+"'";
								rs = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									if (rs.getString(1) == "NE")
									{
										valueXmlString.append("<price_list__disc>").append(priceListDesc).append("</price_list__disc\r\n>");
									}
								}
								//End adding 29/06/06
								rsSatt.close();
								stmtSatt.close();
							}
							catch(Exception e)
							{
								System.out.println("Exception :[SordAttEJB][itemChanged::case 1::order_type] :==>\n"+e.getMessage());
								throw new ITMException(e);
							}
					
						}						
					}
					else if (currentColumn.trim().equals("site_code"))
					{
						String siteDescr = "";
						String ls_cust_code = "";
						String ls_price_list = "";
						if(columnValue != null)
						{
							ls_cust_code = getColumnValue("cust_code",dom);
							try
							{
								stmtSatt = connSatt.createStatement();
								sql = "SELECT DESCR FROM SITE WHERE SITE_CODE ='"+columnValue+"' ";
								rsSatt = stmtSatt.executeQuery(sql);
								if (rsSatt.next())
								{
									siteDescr = rsSatt.getString(1);
								}
								ls_price_list = distDiscountEJB.priceListSite(columnValue,ls_cust_code,connSatt);
								
								valueXmlString.append("<site_descr>").append(siteDescr).append("</site_descr>");
								valueXmlString.append("<price_list>").append(ls_price_list).append("</price_list>");

								//Added from SorderEJB as form changed 29/06/06
								custCode = genericUtility.getColumnValue("cust_code",dom);
								orderDateStr = genericUtility.getColumnValue("ord_date",dom);
								//orderDate2 = genericUtility.getDateObject(orderDateStr);
								itemSer =  genericUtility.getColumnValue("item_Ser",dom);
								sql ="SELECT PRICE_LIST__DISC FROM SITE_CUSTOMER WHERE CUST_CODE ='"+custCode+"'AND SITE_CODE ='"+siteCode+"'"; 
								rs  = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									priceListDesc = rs.getString(1);
									if(priceListDesc == null || priceListDesc.length() == 0)
									{
										sql = "SELECT PRICE_LIST__DISC FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
										rs  = stmtSatt.executeQuery(sql);
										if(rs.next())
										{
											priceListDesc = rs.getString(1);
											valueXmlString.append("<price_list__disc>").append(priceListDesc).append("</price_list__disc\r\n>");
										}
										else
										{
											valueXmlString.append("<price_list__disc>").append("").append("</price_list__disc\r\n>");
										}
									}
									else
									{
										valueXmlString.append("<price_list__disc>").append(priceListDesc).append("</price_list__disc\r\n>");
									}
								}	
								sql="SELECT CONTRACT_NO FROM SCONTRACT WHERE SITE_CODE = '"+siteCode+"' AND CUST_CODE = '"+custCode+"' AND ITEM_SER ='"+itemSer+"' AND CONFIRMED = 'Y'";
								rs  = stmtSatt.executeQuery(sql);
								if(rs.next())
								{
									contractNo = rs.getString(1);
									valueXmlString.append("<contract_no>").append(contractNo).append("</contract_no\r\n>"); 
								}
								else
								{
									valueXmlString.append("<contract_no>").append("").append("</contract_no\r\n>"); 
								}
								//End 
								valueXmlString.append("<site_code__ship>").append(columnValue).append("</site_code__ship\r\n>");
								stmtSatt.close();
							}
							catch(Exception e)
							{
								System.out.println("Exception :[SordAttEJB][itemChanged::case 1::site_code] :==>\n"+e.getMessage());
								throw new ITMException(e);
							}					
						}
					}
					else if (currentColumn.trim().equals("curr_code"))
					{
						stmtSatt = connSatt.createStatement();
						String currDescr = "";
						double ls_exch_rate = 0.00;
						if (columnValue != null)
						{
							try					 
							{
								sql = "SELECT DESCR FROM CURRENCY WHERE CURR_CODE ='" +columnValue+ "'";
								rsSatt = stmtSatt.executeQuery(sql);							
								if (rsSatt.next())
								{
									currDescr = rsSatt.getString(1);
								}
								ls_exch_rate = getDailyExchRateSellBuy(columnValue,"",getColumnValue("site_code",dom),getColumnValue("ord_date",dom),"S");
								//ls_exch_rate = itmDBAccessEJB.getDailyExchRateSellBuy(columnValue,"",getColumnValue("site_code",dom),new java.util.Date(getColumnValue("ord_date",dom)),'S',connSatt);								
							}
							catch(Exception e)
							{
								System.out.println("Exception :[SordAttEJB][itemChanged::case 1::curr_code] :==>\n"+e.getMessage());
								throw new ITMException(e);
							}
						}
						valueXmlString.append("<currency_descr>").append(currDescr).append("</currency_descr>\r\n");
						valueXmlString.append("<exch_rate>").append(Double.toString(ls_exch_rate)).append("</exch_rate>\r\n");
						rsSatt.close();
						stmtSatt.close();
					}
					else if (currentColumn.trim().equals("ord_date"))
					{
						stmtSatt = connSatt.createStatement();
						valueXmlString.append("<pl_date>").append(columnValue).append("</pl_date>\r\n");
						valueXmlString.append("<due_date>").append(columnValue).append("</due_date>\r\n");
						valueXmlString.append("<tax_date>").append(columnValue).append("</tax_date>\r\n");
						valueXmlString.append("<prom_date>").append(columnValue).append("</prom_date>\r\n");
						stmtSatt.close();
					}
					else if (currentColumn.trim().equals("cust_pord"))
					{
						stmtSatt = connSatt.createStatement();
						java.util.Date ls_podate = new java.util.Date();
						String po_date = "";
						if (columnValue != null)
						{
							sql = "SELECT ORD_DATE FROM	PORDER WHERE PURC_ORDER = '"+columnValue+"' AND CONFIRMED ='Y' AND STATUS ='O'";
							
							rsSatt = stmtSatt.executeQuery(sql);
							if (rsSatt.next())
							{
								//ls_podate =  new java.util.Date(java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(rsSatt.getDate(1)));	
								ls_podate =  rsSatt.getDate(1);	
								po_date = dtf.format(ls_podate);
							}
						}
						valueXmlString.append("<pord_date>").append(po_date).append("</pord_date>\r\n");
						stmtSatt.close();
					}
					else if (currentColumn.trim().equals("tran_code"))
					{
						stmtSatt = connSatt.createStatement();
						String tranName = "";
						if (columnValue != null)
						{
							try
							{
								sql = "SELECT TRAN_NAME FROM TRANSPORTER WHERE TRAN_CODE ='" + columnValue + "'";
								rsSatt = stmtSatt.executeQuery(sql);
								
								if (rsSatt.next())
								{
									tranName = rsSatt.getString(1);
								}
								rsSatt.close();
								stmtSatt.close();
							}
							catch(Exception e)
							{
								System.out.println("Exception :[SordAttEJB][itemChanged::case 1::tran_code] :==>\n"+e.getMessage());
								throw new ITMException(e);
							}
						}
						valueXmlString.append("<tran_name>").append(tranName).append("</tran_name>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("dlv_term"))
					{ 
						stmtSatt = connSatt.createStatement();
						dlvTerm = genericUtility.getColumnValue(currentColumn,dom);
						if(dlvTerm != null && dlvTerm.length() > 0)
						{
							sql="SELECT FREIGHT FROM DELIVERY_TERM WHERE DLV_TERM = '"+dlvTerm+"'";
							rs = stmtSatt.executeQuery(sql);
							if(rs.next())
							{
								if(rs.getString(1) == null)
								{
									valueXmlString.append("<frt_term protect=\"0\"></frt_term>\r\n");	
								}
								else
								{
									valueXmlString.append("<frt_term protect=\"1\"></frt_term>\r\n");	
								}
								frtTerm = rs.getString(1);
							}
						}
						valueXmlString.append("<frt_term>").append(frtTerm).append("</frt_term\r\n>");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("cust_code__bil"))
					{
						stmtSatt = connSatt.createStatement();
						custCodeBil = genericUtility.getColumnValue(currentColumn,dom);
						sql="SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = '"+custCodeBil+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							custBilName = rs.getString(1);
							valueXmlString.append("<cust_name__bil>").append(custBilName).append("</cust_name__bil>");
						}
						stmtSatt.close();
					}
					//Added from sorder
					else if(currentColumn.trim().equals("dept_code"))
					{
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						sql = "SELECT DESCR FROM DEPARTMENT WHERE DEPT_CODE = '" +columnValue+ "'";
						System.out.println("query is "+sql);
						rs = stmtSatt.executeQuery(sql);
						if(rs.next() != false )
						{
							returnValue = rs.getString(1);
						}
						System.out.println("descr is "+returnValue);
						valueXmlString.append("<department_descr>").append(returnValue).append("</department_descr>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("cr_term"))
					{   
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						sql = "SELECT DESCR FROM crterm WHERE  cr_term = '" +columnValue+ "'";
						System.out.println("query is "+sql);
						rs = stmtSatt.executeQuery(sql);
						if(rs.next() != false )
						{
							returnValue = rs.getString(1);
						}
						System.out.println("descr is "+returnValue);
						valueXmlString.append("<crterm_descr>").append(returnValue).append("</crterm_descr>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("stan_code"))
					{  
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						sql = "SELECT DESCR FROM station WHERE stan_code = '" +columnValue+ "'";
						System.out.println("query is "+sql);
						rs = stmtSatt.executeQuery(sql);
						if(rs.next() != false )
						{
							returnValue = rs.getString(1);
						}
						System.out.println("descr is "+returnValue);
						valueXmlString.append("<station_descr>").append(returnValue).append("</station_descr>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("curr_code"))
					{  
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						orderDateStr = genericUtility.getColumnValue("ord_date",dom);
						exchRateValue = getDailyExchRateSellBuy(columnValue,"",siteCode,orderDateStr,"S");

						sql="SELECT DESCR FROM CURRENCY WHERE CURR_CODE = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next() != false )
						{
							returnValue = rs.getString(1);
						}
						valueXmlString.append("<currency_descr>").append(returnValue).append("</currency_descr>\r\n");
						valueXmlString.append("<curr_code__frt>").append(columnValue).append("</curr_code__frt>\r\n");
						valueXmlString.append("<curr_code__ins>").append(columnValue).append("</curr_code__ins>\r\n");
						valueXmlString.append("<exch_rate>").append(exchRateValue).append("</exch_rate>\r\n");
						valueXmlString.append("<exch_rate__frt>").append(exchRateValue).append("</exch_rate__frt>\r\n");
						stmtSatt.close();
					} 
					else if(currentColumn.trim().equals("sales_pers"))
					{ 
						String descr1 = "",spName = "";
						stmtSatt = connSatt.createStatement();

						columnValue = genericUtility.getColumnValue(currentColumn,dom);
					
						sql = "SELECT (CASE WHEN EMP_CODE IS NULL THEN '' ELSE EMP_CODE END) FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						
						if(rs.next())
						{
							empCode = rs.getString(1);
							if (empCode != null && empCode.trim().length() > 0)
							{
								cctrCodeSal = genericUtility.getColumnValue("cctr_code__sal",dom);		
								if (cctrCodeSal == null || cctrCodeSal.trim().length() == 0)
								{
									sql = "SELECT (CASE WHEN CCTR_CODE__SAL IS NULL THEN '' ELSE CCTR_CODE__SAL END) FROM EMPLOYEE WHERE EMP_CODE = '"+empCode+"'";
									rs = stmtSatt.executeQuery(sql);
									if (rs.next())
									{
										cctrCodeSal = rs.getString("CCTR_CODE__SAL");
										if (cctrCodeSal != null)
										{
											valueXmlString.append("<cctr_code__sal>").append(cctrCodeSal).append("</cctr_code__sal>\r\n");
										}
									}
								}
							}
						}
						else
						{
							empCode = "";
						}
						valueXmlString.append("<comm_perc protect=\"0\"></comm_perc>\r\n");	
						valueXmlString.append("<comm_perc__on protect=\"0\"></comm_perc__on>\r\n");	
						valueXmlString.append("<curr_code__comm protect=\"0\"></curr_code__comm>\r\n");	
						valueXmlString.append("<exch_rate__comm protect=\"0\"></exch_rate__comm>\r\n");	
											
						custCode = genericUtility.getColumnValue("cust_code",dom);
						itemSer = genericUtility.getColumnValue("item_ser",dom);
						sql = "SELECT (CASE WHEN COMM_PERC IS NULL THEN 0 ELSE COMM_PERC END),COMM_PERC__ON FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"' AND ITEM_SER  = '"+itemSer+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							num = rs.getDouble(1);
							descr1 = rs.getString(2);
						}
						if (num == 0)
						{
							sql = "SELECT COMM_PERC FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
							rs = stmtSatt.executeQuery(sql);
							if (rs.next())
							{
								num = rs.getDouble("COMM_PERC");
							}
						}
						if (descr1 == null || descr1.trim().length() == 0)
						{
							sql = "SELECT COMM_PERC__ON FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
							rs = stmtSatt.executeQuery(sql);
							if (rs.next())
							{			 
								descr1 = rs.getString("COMM_PERC__ON");
							}
						}
						sql = "SELECT SP_NAME, CURR_CODE FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							spName = rs.getString("SP_NAME");
							currCode = rs.getString("CURR_CODE");
						}
						valueXmlString.append("<sp_name>").append(spName != null ? spName:" ").append("</sp_name>\r\n");
						valueXmlString.append("<comm_perc>").append(num).append("</comm_perc>\r\n");
						valueXmlString.append("<comm_perc__on>").append(descr1 == null ? "":descr1).append("</comm_perc__on>\r\n");
						if (currCode != null && currCode.trim().length() > 0)
						{
							siteCode = genericUtility.getColumnValue("site_code",dom);
							orderDateStr = genericUtility.getColumnValue("ord_date",dom);
							num = getDailyExchRateSellBuy(currCode,"",siteCode,orderDateStr,"S"); 
							valueXmlString.append("<curr_code__comm>").append(currCode).append("</curr_code__comm>\r\n");
							valueXmlString.append("<exch_rate__comm>").append(num).append("</exch_rate__comm>\r\n");
						}					
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("sales_pers__1"))
					{  
						String descr1 = "";
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);

						sql = "SELECT (CASE WHEN EMP_CODE IS NULL THEN '' ELSE EMP_CODE END) FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							empCode = rs.getString(1);
							if (empCode != null && empCode.trim().length() > 0)
							{
								cctrCodeSal = genericUtility.getColumnValue("cctr_code__sal",dom);
								if (cctrCodeSal == null || cctrCodeSal.trim().length() == 0)
								{
									sql = "SELECT (CASE WHEN CCTR_CODE__SAL IS NULL THEN '' ELSE CCTR_CODE__SAL END) FROM EMPLOYEE WHERE EMP_CODE = '"+empCode+"'";
									rs = stmtSatt.executeQuery(sql);
									if (rs.next())
									{
										cctrCodeSal = rs.getString("CCTR_CODE__SAL");
										valueXmlString.append("<cctr_code__sal>").append(cctrCodeSal).append("</cctr_code__sal>\r\n");
									}
								}
							}
						}
						else
						{
							empCode = "";
						}
						valueXmlString.append("<comm_perc_1 protect=\"0\"></comm_perc_1>\r\n");	
						valueXmlString.append("<comm_perc__on_1 protect=\"0\"></comm_perc__on_1>\r\n");	
						valueXmlString.append("<curr_code__comm_1 protect=\"0\"></curr_code__comm_1>\r\n");	
						valueXmlString.append("<exch_rate__comm_1 protect=\"0\"></exch_rate__comm_1>\r\n");	
						
						custCode = genericUtility.getColumnValue("cust_code",dom);
						itemSer = genericUtility.getColumnValue("item_ser",dom);

						sql = "SELECT (CASE WHEN COMM_PERC__1 IS NULL THEN 0 ELSE COMM_PERC__1 END),COMM_PERC__ON_1 FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+custCode+"' AND ITEM_SER  = '"+itemSer+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							num = rs.getDouble(1);
							descr1 = rs.getString(2);
						}
						if (num == 0)
						{
							sql = "SELECT COMM_PERC FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
							rs = stmtSatt.executeQuery(sql);
							if (rs.next())
							{
								num = rs.getDouble("COMM_PERC");
							}
						}
						if (descr1 == null || descr1.trim().length() == 0)
						{
							sql = "SELECT COMM_PERC__ON FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
							rs = stmtSatt.executeQuery(sql);
							if (rs.next())
							{
								descr1 = rs.getString("COMM_PERC__ON");
							}
						}
						sql = "SELECT SP_NAME,CURR_CODE FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							spName1 = rs.getString("SP_NAME");
							currCode = rs.getString("CURR_CODE");
						}
						valueXmlString.append("<sales_pers_sp_name>").append(spName1).append("</sales_pers_sp_name>\r\n");
						valueXmlString.append("<comm_perc_1>").append(num).append("</comm_perc_1>\r\n");
						valueXmlString.append("<comm_perc_on_1>").append(descr1 != null ? descr1:"").append("</comm_perc_on_1>\r\n");
						if (currCode != null && currCode.trim().length() > 0)
						{
							siteCode = genericUtility.getColumnValue("site_code",dom);
							orderDateStr = genericUtility.getColumnValue("ord_date",dom);

							num = getDailyExchRateSellBuy(currCode,"",siteCode,orderDateStr,"S"); 
							valueXmlString.append("<curr_code__comm_1>").append(currCode).append("</curr_code__comm_1>\r\n");
							valueXmlString.append("<exch_rate__comm_1>").append(num).append("</exch_rate__comm_1>\r\n");
						}
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("sales_pers__2"))
					{ 
						String descr1 = "";
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						
						sql="SELECT  EMP_CODE,SP_NAME FROM SALES_PERS WHERE SALES_PERS = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							empCode = rs.getString(1);
							spName3 = rs.getString(2);
						}	
						if (empCode != null)
						{
							sql="SELECT COUNT(*) FROM EMPLOYEE WHERE EMP_CODE = '"+empCode+"' AND RESI_DATE IS NULL";
							rs1 = stmtSatt.executeQuery(sql);
							if(rs1.next() && rs1.getInt(1) > 0)
							{
								valueXmlString.append("<comm_perc_2 protect=\"0\"></comm_perc_2>\r\n");	
								valueXmlString.append("<comm_perc__on_2 protect=\"0\"></comm_perc__on_2>\r\n");	
								valueXmlString.append("<curr_code__comm_2 protect=\"0\"></curr_code__comm_2>\r\n");	
								valueXmlString.append("<exch_rate__comm_2 protect=\"0\"></exch_rate__comm_2>\r\n");	
							}
						}												
						valueXmlString.append("<sales_pers_sp_name_1>").append(spName3).append("</sales_pers_sp_name_1>\r\n");
										
						custCode = genericUtility.getColumnValue("cust_code",dom);
						itemSer = genericUtility.getColumnValue("item_ser",dom);
						sql="SELECT COMM_PERC__2,COMM_PERC__ON_2 FROM CUSTOMER_SERIES WHERE CUST_CODE ='"+custCode+"' AND ITEM_SER  ='"+itemSer+"'";
						System.out.println("SQL :: "+sql);
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							num = rs.getDouble(1);
							descr1 = rs.getString(2);
						}
						if(num == 0)
						{
							sql ="SELECT COMM_PERC,SP_NAME FROM SALES_PERS WHERE SALES_PERS ='"+columnValue+"'";
							System.out.println("SQL :: "+sql);
							rs1 = stmtSatt.executeQuery(sql);
							if(rs1.next())
							{
								num = rs1.getDouble(1);
								spName3 = rs1.getString(2);
								valueXmlString.append("<sales_pers_sp_name_1>").append(spName3).append("</sales_pers_sp_name_1>\r\n");
								valueXmlString.append("<comm_perc_2>").append(num).append("</comm_perc_2>\r\n");
							}
						}
						if (descr1 == null || descr1.trim().length() == 0)
						{
							sql ="SELECT COMM_PERC__ON,SP_NAME FROM SALES_PERS WHERE SALES_PERS ='"+columnValue+"'";
							rs1 = stmtSatt.executeQuery(sql);
							if(rs1.next())
							{
								descr1 = rs1.getString(1);
								spName3 = rs1.getString(2);
								valueXmlString.append("<sales_pers_sp_name_1>").append(spName3).append("</sales_pers_sp_name_1>\r\n");
								valueXmlString.append("<comm_perc__on_2>").append(descr1).append("</comm_perc__on_2>\r\n");
							}
						}
						sql="SELECT SP_NAME,CURR_CODE FROM SALES_PERS WHERE SALES_PERS ='"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							valueXmlString.append("<sales_pers_sp_name_1>").append(rs.getString(1)).append("</sales_pers_sp_name_1>\r\n");
							valueXmlString.append("<comm_perc_2>").append(num).append("</comm_perc_2>\r\n");
							valueXmlString.append("<comm_perc__on_2>").append(descr1).append("</comm_perc__on_2>\r\n");
							
							currCode = rs.getString(2);
							
							siteCode = genericUtility.getColumnValue("site_code",dom);
							orderDateStr = genericUtility.getColumnValue("ord_date",dom);
							
							num = getDailyExchRateSellBuy(currCode,"",siteCode,orderDateStr,"S"); 
														
							valueXmlString.append("<curr_code__comm_2>").append(currCode).append("</curr_code__comm_2>\r\n");
							valueXmlString.append("<exch_rate__comm_2>").append(num).append("</exch_rate__comm_2>\r\n");							
						}						
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("curr_code__frt"))
					{ 
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						orderDateStr = genericUtility.getColumnValue("ord_date",dom);
						num = getDailyExchRateSellBuy(columnValue,"",siteCode,orderDateStr,"S"); 
						valueXmlString.append("<exch_rate__frt>").append(num).append("</exch_rate__frt>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("curr_code__ins"))
					{
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						orderDateStr = genericUtility.getColumnValue("ord_date",dom);
						num = getDailyExchRateSellBuy(columnValue,"",siteCode,orderDateStr,"S"); 
						valueXmlString.append("<exch_rate__ins>").append(num).append("</exch_rate__ins>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("curr_code__comm"))
					{  
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						orderDateStr = genericUtility.getColumnValue("ord_date",dom);
						num = getDailyExchRateSellBuy(columnValue,"",siteCode,orderDateStr,"S"); // to write function
						valueXmlString.append("<exch_rate__comm>").append(num).append("</exch_rate__comm>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("curr_code__comm_1"))
					{   
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						orderDateStr = genericUtility.getColumnValue("ord_date",dom);
						num =getDailyExchRateSellBuy(columnValue,"",siteCode,orderDateStr,"S");
						valueXmlString.append("<exch_rate__comm_1>").append(num).append("</exch_rate__comm_1>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("curr_code__comm_2"))
					{  
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						orderDateStr = genericUtility.getColumnValue("ord_date",dom);
						num = getDailyExchRateSellBuy(columnValue,"",siteCode,orderDateStr,"S");
						valueXmlString.append("<exch_rate__comm_2>").append(num).append("</exch_rate__comm_2>\r\n");
						stmtSatt.close();
					}
					else if(currentColumn.trim().equals("quot_no"))
					{   
						stmtSatt = connSatt.createStatement();
						columnValue = genericUtility.getColumnValue(currentColumn,dom);
						sql="SELECT CUST_CODE, SALES_PERS FROM SALES_QUOT WHERE QUOT_NO ='"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if(rs.next())
						{
							custCode = rs.getString(1);
							salesPers = rs.getString(2);
							if(salesPers != null && salesPers.length() >0)
							{
								valueXmlString.append("<sales_pers>").append(salesPers).append("</sales_pers>\r\n");
							}
						}
						stmtSatt.close();
					} 
					//End adding
					valueXmlString.append("</Detail>\r\n");
					break;
				case 2:
				
					/* Searching the dom for the incoming column value start*/
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					int childListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue();
							}
						}
						ctr++;
					}while(ctr < childListLength && !childNodeName.equals(currentColumn));
					/* Searching the dom for the incoming column value end*/
							
					valueXmlString.append("<Detail>\r\n");

					if (currentColumn.trim().equals("itm_default"))
					{
						String taxEnv = "";
						siteCode = getColumnValue("site_code",dom1);
						custCode = getColumnValue("cust_code",dom1);
						orderType = getColumnValue("order_type",dom1);
						if (siteCode != null && siteCode.trim().length() > 0 && custCode != null && custCode.trim().length() > 0)
						{
							pListDiscount = distDiscountEJB.priceListDiscount(siteCode,custCode,connSatt);	
							valueXmlString.append("<price_list__disc>").append(pListDiscount).append("</price_list__disc>\r\n");
						}
						valueXmlString.append("<quantity>").append("0").append("</quantity>\r\n");
						valueXmlString.append("<conv__qty_stduom>").append("0").append("</conv__qty_stduom>\r\n");
						valueXmlString.append("<rate>").append("0").append("</rate>\r\n");
						valueXmlString.append("<conv__rtuom_stduom>").append("0").append("</conv__rtuom_stduom>\r\n");
						valueXmlString.append("<quantity__stduom>").append("0").append("</quantity__stduom>\r\n");
						valueXmlString.append("<rate__stduom>").append("0").append("</rate__stduom>\r\n");
						valueXmlString.append("<discount>").append("0.00").append("</discount>\r\n");
						sql = "SELECT UDF_STR1 FROM GENCODES WHERE FLD_NAME = 'ORDER_TYPE' AND MOD_NAME = 'W_SORDFORM_ATT' AND FLD_VALUE = '"+orderType+"'";
						stmtSatt = connSatt.createStatement();
						rsSatt = stmtSatt.executeQuery(sql);
						if (rsSatt.next())
						{
							taxEnv = rsSatt.getString("UDF_STR1");
						}
						valueXmlString.append("<tax_env>").append(taxEnv).append("</tax_env>\r\n");
						taxEnv = null;						
					}
					if (currentColumn.trim().equals("itm_defaultedit"))
					{
						String itemType = "";
						itemType = getColumnValue("item_type",dom,"2");						
					}
					if (currentColumn.trim().equals("item_type"))
					{
						hashAttribute = null;
						sql = "SELECT PHY_ATTRIB_1, PHY_ATTRIB_2, PHY_ATTRIB_3, PHY_ATTRIB_4, PHY_ATTRIB_5, PHY_ATTRIB_6,"+
							  "PHY_ATTRIB_7, PHY_ATTRIB_8, PHY_ATTRIB_9, PHY_ATTRIB_10,PHY_ATTRIB_11,PHY_ATTRIB_12,PHY_ATTRIB_13, "+
							  "PHY_ATTRIB_14,PHY_ATTRIB_15,PHY_ATTRIB_16,PHY_ATTRIB_17, "+
							  "PHY_ATTRIB_18,PHY_ATTRIB_19,PHY_ATTRIB_20,PHY_ATTRIB_21,PHY_ATTRIB_22,UNIT,UNIT__RATE,UNIT__STD,TAX_CLASS,TAX_CHAP,DESCR,ITEM_CODE__RATE  "+
							  "FROM ITEM_TYPE WHERE ITEM_TYPE  = '"+columnValue+"'";
						stmtSatt = connSatt.createStatement();
						rsSatt = stmtSatt.executeQuery(sql);
						if (rsSatt.next())
						{
							valueXmlString.append("<phy_attrib__lab1>").append((rsSatt.getString(1) == null ) ? " ":rsSatt.getString(1) ).append("</phy_attrib__lab1>\r\n");
							valueXmlString.append("<phy_attrib__lab2>").append((rsSatt.getString(2) == null ) ? " ":rsSatt.getString(2)).append("</phy_attrib__lab2>\r\n");
							valueXmlString.append("<phy_attrib__lab3>").append((rsSatt.getString(3) == null ) ? " ":rsSatt.getString(3)).append("</phy_attrib__lab3>\r\n");
							valueXmlString.append("<phy_attrib__lab4>").append((rsSatt.getString(4) == null ) ? " ":rsSatt.getString(4)).append("</phy_attrib__lab4>\r\n");
							valueXmlString.append("<phy_attrib__lab5>").append((rsSatt.getString(5) == null ) ? " ":rsSatt.getString(5)).append("</phy_attrib__lab5>\r\n");
							valueXmlString.append("<phy_attrib__lab6>").append((rsSatt.getString(6) == null ) ? " ":rsSatt.getString(6)).append("</phy_attrib__lab6>\r\n");
							valueXmlString.append("<phy_attrib__lab7>").append((rsSatt.getString(7) == null ) ? " ":rsSatt.getString(7)).append("</phy_attrib__lab7>\r\n");
							valueXmlString.append("<phy_attrib__lab8>").append((rsSatt.getString(8) == null ) ? " ":rsSatt.getString(8)).append("</phy_attrib__lab8>\r\n");
							valueXmlString.append("<phy_attrib__lab9>").append((rsSatt.getString(9) == null ) ? " ":rsSatt.getString(9)).append("</phy_attrib__lab9>\r\n");
							valueXmlString.append("<phy_attrib__lab10>").append((rsSatt.getString(10) == null ) ? " ":rsSatt.getString(10)).append("</phy_attrib__lab10>\r\n");
							valueXmlString.append("<phy_attrib__lab11>").append((rsSatt.getString(11) == null ) ? " ":rsSatt.getString(11)).append("</phy_attrib__lab11>\r\n");
							valueXmlString.append("<phy_attrib__lab12>").append((rsSatt.getString(12) == null ) ? " ":rsSatt.getString(12)).append("</phy_attrib__lab12>\r\n");
							valueXmlString.append("<phy_attrib__lab13>").append((rsSatt.getString(13) == null ) ? " ":rsSatt.getString(13)).append("</phy_attrib__lab13>\r\n");
							valueXmlString.append("<phy_attrib__lab14>").append((rsSatt.getString(14) == null ) ? " ":rsSatt.getString(14)).append("</phy_attrib__lab14>\r\n");
							valueXmlString.append("<phy_attrib__lab15>").append((rsSatt.getString(15) == null ) ? " ":rsSatt.getString(15)).append("</phy_attrib__lab15>\r\n");
							valueXmlString.append("<phy_attrib__lab16>").append((rsSatt.getString(16) == null ) ? " ":rsSatt.getString(16)).append("</phy_attrib__lab16>\r\n");
							valueXmlString.append("<phy_attrib__lab17>").append((rsSatt.getString(17) == null ) ? " ":rsSatt.getString(17)).append("</phy_attrib__lab17>\r\n");
							valueXmlString.append("<phy_attrib__lab18>").append((rsSatt.getString(18) == null ) ? " ":rsSatt.getString(18)).append("</phy_attrib__lab18>\r\n");
							valueXmlString.append("<phy_attrib__lab19>").append((rsSatt.getString(19) == null ) ? " ":rsSatt.getString(19)).append("</phy_attrib__lab19>\r\n");
							valueXmlString.append("<phy_attrib__lab20>").append((rsSatt.getString(20) == null ) ? " ":rsSatt.getString(20)).append("</phy_attrib__lab20>\r\n");
							valueXmlString.append("<phy_attrib__lab21>").append((rsSatt.getString(21) == null ) ? " ":rsSatt.getString(21)).append("</phy_attrib__lab21>\r\n");
							valueXmlString.append("<phy_attrib__lab22>").append((rsSatt.getString(22) == null ) ? " ":rsSatt.getString(22)).append("</phy_attrib__lab22>\r\n");
							valueXmlString.append("<unit>").append((rsSatt.getString(23) == null ) ? " ":rsSatt.getString(23)).append("</unit>\r\n");
							valueXmlString.append("<unit__rate>").append((rsSatt.getString(24) == null ) ? " ":rsSatt.getString(24)).append("</unit__rate>\r\n");
							valueXmlString.append("<unit__std>").append((rsSatt.getString(25) == null ) ? " ":rsSatt.getString(25)).append("</unit__std>\r\n");
							valueXmlString.append("<tax_class>").append((rsSatt.getString(26) == null ) ? " ":rsSatt.getString(26)).append("</tax_class>\r\n");
							valueXmlString.append("<tax_chap>").append((rsSatt.getString(27) == null ) ? " ":rsSatt.getString(27)).append("</tax_chap>\r\n");
							String unitOfLenCol,unitOfWidthCol,unitOfHeightCol,unitOfCenHCol,strictFlexAnyCol,cutYNCol; 
							unitOfLenCol = getAttributeColumn("Unit (L)",columnValue,connSatt);
							unitOfWidthCol = getAttributeColumn("Unit (W)",columnValue,connSatt);
							unitOfHeightCol = getAttributeColumn("Unit (H)",columnValue,connSatt);
							unitOfCenHCol = getAttributeColumn("Unit (CH)",columnValue,connSatt);
							strictFlexAnyCol = getAttributeColumn("Strict/Flex/Any",columnValue,connSatt);
							cutYNCol = getAttributeColumn("Cut/Fin",columnValue,connSatt);
							
							if (strictFlexAnyCol != null && strictFlexAnyCol.trim().length() > 0)
							{
								valueXmlString.append("<"+strictFlexAnyCol.toLowerCase()+">").append("S").append("</"+strictFlexAnyCol.toLowerCase()+">\r\n");
							}
							if (cutYNCol != null && cutYNCol.trim().length() > 0)
							{
								valueXmlString.append("<"+cutYNCol.toLowerCase()+">").append("C").append("</"+cutYNCol.toLowerCase()+">\r\n");
							}
							valueXmlString.append("<item_type_descr>").append((rsSatt.getString(28) == null ) ? " ":rsSatt.getString(28)).append("</item_type_descr>\r\n");
							String itemCodeRate = rsSatt.getString(29);
							ResultSet rs2 = null;
							if (itemCodeRate != null && itemCodeRate.trim().length() > 0)
							{
								sql = "SELECT "+unitOfLenCol+","+unitOfWidthCol+","+unitOfHeightCol+","+unitOfCenHCol+" FROM ITEM WHERE ITEM_CODE = '"+itemCodeRate+"'";
								rs2 = stmtSatt.executeQuery(sql);
								System.out.println(" SQL  itemCode Rate : "+sql);
								if (rs2.next())
								{
									if (unitOfLenCol != null && unitOfLenCol.trim().length() > 0)
									{
										valueXmlString.append("<"+unitOfLenCol.toLowerCase()+">").append((rs2.getString(1) == null ) ? " ":rs2.getString(1)).append("</"+unitOfLenCol.toLowerCase()+">\r\n");
									}
									if (unitOfWidthCol != null && unitOfWidthCol.trim().length() > 0)
									{
										valueXmlString.append("<"+unitOfWidthCol.toLowerCase()+">").append((rs2.getString(2) == null ) ? " ":rs2.getString(2)).append("</"+unitOfWidthCol.toLowerCase()+">\r\n");
									}
									if (unitOfHeightCol != null && unitOfHeightCol.trim().length() > 0)
									{
										valueXmlString.append("<"+unitOfHeightCol.toLowerCase()+">").append((rs2.getString(3) == null ) ? " ":rs2.getString(3)).append("</"+unitOfHeightCol.toLowerCase()+">\r\n");
									}
									if (unitOfCenHCol != null && unitOfCenHCol.trim().length() > 0)
									{
										valueXmlString.append("<"+unitOfCenHCol.toLowerCase()+">").append((rs2.getString(4) == null ) ? " ":rs2.getString(4)).append("</"+unitOfCenHCol.toLowerCase()+">\r\n");
									}

								}
								rs2.close();
							}							
						}
						else
						{
							valueXmlString.append("<phy_attrib__lab1>").append(" ").append("</phy_attrib__lab1>\r\n");
							valueXmlString.append("<phy_attrib__lab2>").append(" ").append("</phy_attrib__lab2>\r\n");
							valueXmlString.append("<phy_attrib__lab3>").append(" ").append("</phy_attrib__lab3>\r\n");
							valueXmlString.append("<phy_attrib__lab4>").append(" ").append("</phy_attrib__lab4>\r\n");
							valueXmlString.append("<phy_attrib__lab5>").append(" ").append("</phy_attrib__lab5>\r\n");
							valueXmlString.append("<phy_attrib__lab6>").append(" ").append("</phy_attrib__lab6>\r\n");
							valueXmlString.append("<phy_attrib__lab7>").append(" ").append("</phy_attrib__lab7>\r\n");
							valueXmlString.append("<phy_attrib__lab8>").append(" ").append("</phy_attrib__lab8>\r\n");
							valueXmlString.append("<phy_attrib__lab9>").append(" ").append("</phy_attrib__lab9>\r\n");
							valueXmlString.append("<phy_attrib__lab10>").append(" ").append("</phy_attrib__lab10>\r\n");
							valueXmlString.append("<phy_attrib__lab11>").append(" ").append("</phy_attrib__lab11>\r\n");
							valueXmlString.append("<phy_attrib__lab12>").append(" ").append("</phy_attrib__lab12>\r\n");
							valueXmlString.append("<phy_attrib__lab13>").append(" ").append("</phy_attrib__lab13>\r\n");
							valueXmlString.append("<phy_attrib__lab14>").append(" ").append("</phy_attrib__lab14>\r\n");
							valueXmlString.append("<phy_attrib__lab15>").append(" ").append("</phy_attrib__lab15>\r\n");
							valueXmlString.append("<phy_attrib__lab16>").append(" ").append("</phy_attrib__lab16>\r\n");
							valueXmlString.append("<phy_attrib__lab17>").append(" ").append("</phy_attrib__lab17>\r\n");
							valueXmlString.append("<phy_attrib__lab18>").append(" ").append("</phy_attrib__lab18>\r\n");
							valueXmlString.append("<phy_attrib__lab19>").append(" ").append("</phy_attrib__lab19>\r\n");
							valueXmlString.append("<phy_attrib__lab20>").append(" ").append("</phy_attrib__lab20>\r\n");
							valueXmlString.append("<phy_attrib__lab21>").append(" ").append("</phy_attrib__lab21>\r\n");
							valueXmlString.append("<phy_attrib__lab22>").append(" ").append("</phy_attrib__lab22>\r\n");
							valueXmlString.append("<unit>").append(" ").append("</unit>\r\n");
							valueXmlString.append("<unit__rate>").append(" ").append("</unit__rate>\r\n");
							valueXmlString.append("<unit__std>").append(" ").append("</unit__std>\r\n");
							valueXmlString.append("<tax_class>").append(" ").append("</tax_class>\r\n");
							valueXmlString.append("<tax_chap>").append(" ").append("</tax_chap>\r\n");
						}
						custCode = getColumnValue("cust_code",dom1,"1");
						sql = "SELECT DESCR FROM CUST_ITEM_TYPE WHERE CUST_CODE = '"+custCode+"' AND ITEM_TYPE = '"+columnValue+"'";
						rs = stmtSatt.executeQuery(sql);
						if (rs.next())
						{
							String sordItemDescr = "";
							sordItemDescr = rs.getString(1);
							if (sordItemDescr != null)
							{
								valueXmlString.append("<item_descr>").append(sordItemDescr).append("</item_descr>\r\n");
							}

						}
					}
					if (currentColumn.trim().equals("phy_attrib_1") || currentColumn.trim().equals("phy_attrib_2") || currentColumn.trim().equals("phy_attrib_3") || currentColumn.trim().equals("phy_attrib_4") || currentColumn.trim().equals("phy_attrib_5") || currentColumn.trim().equals("phy_attrib_6") || currentColumn.trim().equals("phy_attrib_7") || currentColumn.trim().equals("phy_attrib_8") || currentColumn.trim().equals("phy_attrib_9") || currentColumn.trim().equals("phy_attrib_10") || currentColumn.trim().equals("phy_attrib_11") || currentColumn.trim().equals("phy_attrib_12") || currentColumn.trim().equals("phy_attrib_13") || currentColumn.trim().equals("phy_attrib_14") || currentColumn.trim().equals("phy_attrib_16") || currentColumn.trim().equals("phy_attrib_17") || currentColumn.trim().equals("phy_attrib_18"))
					{
						String itemCode = "";
						String itemDescr = "";
						String mfgType = "";
						String itemType = "";
						String phyAttrib1 = "";			   
						String phyAttrib2 = "";
						String phyAttrib3 = "";
						String phyAttrib4 = "";
						String phyAttrib5 = "";
						String phyAttrib6 = "";
						String phyAttrib7 = "";
						String phyAttrib8 = "";
						String phyAttrib9 = "";
						String phyAttrib10 = "";


						String taxchap = " ",taxclass = " ",taxenv = " ";
						String 	frStation = "",toStation = "";
						String itemFlag = "N",unitStd = "",unit = "",unitRate = "",locType = "";
						java.util.Date ordDate = new java.util.Date();
						java.util.Date plDate = new java.util.Date();
						double rate = 0;
						
						//ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
						if (columnValue != null)
						{
							itemType = getColumnValue("item_type",dom,"2");
						 	try
							{
								ArrayList getAttNo = null;
								String genAttribNo = "";
								stmtSatt = connSatt.createStatement();
								sql = "SELECT GEN_ATTRIB FROM ITEM_TYPE WHERE ITEM_TYPE = '"+itemType+"'";
								rsSatt = stmtSatt.executeQuery(sql);
								if (rsSatt.next())
								{
									genAttribNo = rsSatt.getString(1);
									if (genAttribNo != null && genAttribNo.trim().length() > 0)
									{
										getAttNo = genericUtility.getTokenList(genAttribNo,",");
									}
								}

								StringBuffer sql1 = new StringBuffer();
								sql1.append("SELECT ITEM_CODE,DESCR,MFG_TYPE FROM ITEM WHERE ITEM_TYPE = '").append(itemType).append("' ");
								
								for (int i = 0;i < getAttNo.size() ;i++ )
								{
									String attNo = getAttNo.get(i).toString();
									String attValue = getColumnValue("phy_attrib_"+attNo,dom,"2");
									if (attValue != null && attValue.trim().length() > 0)
									{
										sql1.append("AND PHY_ATTRIB_"+attNo+" = '").append(attValue).append("' ");
									}
									else
									{
										sql1.append("AND PHY_ATTRIB_"+attNo+" IS NULL ");
									}
								}
								
								System.out.println("Item Code SQL :: "+sql1.toString());
								
								stmtSatt = connSatt.createStatement();
								rsSatt = stmtSatt.executeQuery(sql1.toString());
								if (rsSatt.next())
								{
									itemCode = rsSatt.getString(1);
									itemDescr = rsSatt.getString(2); 
									mfgType = rsSatt.getString(3);
							  	}
								//itemCode = getColumnValue("item_code",dom,"2");
								String ls_ordDate = "",quantity ="",mrate ="",priceListDisc = "",ls_unit = "",ls_plDate = "";
								double discount = 0.00;
								priceList = getColumnValue("price_list",dom1);
								ls_ordDate = getColumnValue("ord_date",dom1);
								quantity = getColumnValue("quantity",dom,"2");
								ordDate = getDateObject(ls_ordDate);
								mrate = getColumnValue("rate",dom,"2");
								ls_unit = getColumnValue("unit",dom,"2");
								priceListDisc = getColumnValue("price_list__disc",dom,"2");
								ls_plDate = getColumnValue("pl_date",dom1);
								plDate = getDateObject(ls_plDate);
								int integralQty=1;

								if (itemCode.trim().length() > 0)
								{
									itemFlag = "E";
									itemSer =  getColumnValue("item_ser",dom1);
									custCode = getColumnValue("cust_code",dom1);
									siteCode = getColumnValue("site_code",dom1);

									sql = "SELECT STAN_CODE FROM SITE WHERE SITE_CODE = '"+siteCode+"'";
									rsSatt = stmtSatt.executeQuery(sql);
									if (rsSatt.next())
									{
										frStation = rsSatt.getString("STAN_CODE");
									}
									sql = "SELECT STAN_CODE FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
									rsSatt = stmtSatt.executeQuery(sql);
									if (rsSatt.next())
									{
										toStation = rsSatt.getString("STAN_CODE");
									}
									sql = "SELECT INTEGRAL_QTY,LOC_TYPE,UNIT,UNIT__SAL,UNIT__RATE FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									rsSatt = stmtSatt.executeQuery(sql);
									if (rsSatt.next())
									{
										integralQty = rsSatt.getInt("INTEGRAL_QTY");
										if (integralQty == 0)
										{
											integralQty = 1;
										}
										locType = rsSatt.getString("LOC_TYPE");
										unitStd = rsSatt.getString("UNIT");
										unit = rsSatt.getString("UNIT__SAL");
										unitRate = rsSatt.getString("UNIT__RATE");
									}
									//Tax chap & Tax class commented as this will come from item_type table.
									//taxchap = itmDBAccessEJB.getTaxChapter(itemCode,itemSer,'C',custCode,siteCode,connSatt);
									//taxclass = itmDBAccessEJB.getTaxClass('C',custCode,itemCode,siteCode,connSatt); 
									//taxenv =  itmDBAccessEJB.getTaxEnv(frStation,toStation,taxchap,taxclass,siteCode,connSatt); // tax environment will set from order type from udf_str1 column of gencodes in itm default
									//if (mrate != null && mrate.trim().length() > 0 && Double.parseDouble(mrate) == 0)
									if (mrate != null && mrate.trim().length() > 0)
									{
										if (priceList != null && priceList.trim().length() > 0)
										{
											rate = distDiscountEJB.pickRate(priceList,ordDate,itemCode,"","L",Double.parseDouble(quantity),connSatt);
											System.out.println("Rate "+rate);
											if (rate <= 0 && mfgType != null && mfgType.equalsIgnoreCase("D"))
											{
												String itemCodeRate = "",gsmColumn = "",gsmValue = "";
												double itemrate = 0,gsm = 0,sqrFoot;
												sql = "SELECT ITEM_CODE__RATE FROM ITEM_TYPE WHERE ITEM_TYPE='"+itemType+"'";
												stmtSatt = connSatt.createStatement();
												rsSatt = stmtSatt.executeQuery(sql);
												if (rsSatt.next())
												{
													itemCodeRate = rsSatt.getString("ITEM_CODE__RATE");
												}
												gsmColumn = getAttributeColumn("GSM",itemType,connSatt);
												System.out.println("gsmColumn :: "+gsmColumn);
												gsmValue = getColumnValue(gsmColumn.toLowerCase(),dom,"2");
												System.out.println("GSM :: "+gsmValue);
												if (gsmValue != null && gsmValue.trim().length() > 0)
												{
													gsm = Double.parseDouble(gsmValue);

													DistCommon distCommon = new DistCommon();
													itemrate = distCommon.pickRateGSM(priceList,ls_plDate,itemCodeRate,"","",gsm,connSatt);
													System.out.println("itemrate :: "+itemrate);
													sqrFoot = calculateSquareFoot(currentColumn,itemType,dom,connSatt);
													System.out.println("sqrFoot :: "+sqrFoot);
													rate = Math.round(sqrFoot * itemrate);
													String cutYNCol = "",cutYNValue = "";
													cutYNCol = getAttributeColumn("Cut/Fin",itemType,connSatt);
													cutYNValue = getColumnValue(cutYNCol.toLowerCase(),dom,"2");
													if (cutYNValue != null && cutYNValue.equalsIgnoreCase("F") && mfgType != null && mfgType.equalsIgnoreCase("D"))
													{
														rate = rate + (rate * 0.12);
														System.out.println("Finish Size rate is "+rate);
													}
													valueXmlString.append("<rate>").append(Double.toString(rate)).append("</rate>\r\n");
												}
											}
										}
										valueXmlString.append("<rate>").append(Double.toString(rate)).append("</rate>\r\n");

										//
										String ls_unitRate = "",ls_unitStd = "",ls_rate = "",ls_convRate = "";
										double convRate = 0.00,rateStd = 0.00;
										ArrayList rateValue = new ArrayList();

										//itemCode = getColumnValue("item_code",dom,"2");
										ls_unitRate = getColumnValue("unit__rate",dom,"2");
										ls_unitStd = getColumnValue("unit__std",dom,"2");
										//ls_rate = getColumnValue("rate",dom,"2");
										ls_convRate = getColumnValue("conv__rtuom_stduom",dom,"2");
										convRate = Double.parseDouble(ls_convRate);
										rateValue = disCommon.convQtyFactor(ls_unitRate,ls_unitStd,itemCode,rate,convRate,connSatt);	
										valueXmlString.append("<conv__rtuom_stduom>").append(rateValue.get(0).toString()).append("</conv__rtuom_stduom>\r\n");
										valueXmlString.append("<rate__stduom>").append(rateValue.get(1).toString()).append("</rate__stduom>\r\n");
									}
									if (priceListDisc != null && priceListDisc.trim().length() > 0)
									{
										discount = distDiscountEJB.getDiscount(priceListDisc,ordDate,custCode,siteCode,itemCode,ls_unit,0.00,plDate,Double.parseDouble(quantity),connSatt);
										valueXmlString.append("<discount>").append(Double.toString(discount)).append("</discount>\r\n");
									}
									valueXmlString.append("<loc_type>").append((locType == null)? " ":locType).append("</loc_type>\r\n");
									valueXmlString.append("<unit>").append((unit == null)? " ":unit).append("</unit>\r\n");
									valueXmlString.append("<unit__rate>").append((unitRate == null)? " ":unitRate).append("</unit__rate>\r\n");
									//valueXmlString.append("<tax_class>").append((taxclass == null)? "":taxclass).append("</tax_class>\r\n");
									//valueXmlString.append("<tax_chap>").append((taxchap == null)? " ":taxchap).append("</tax_chap>\r\n");
									//valueXmlString.append("<tax_env>").append((taxenv == null)? " ":taxenv).append("</tax_env>\r\n");
									valueXmlString.append("<unit__std>").append(unitStd).append("</unit__std>\r\n");
									valueXmlString.append("<item_code>").append(itemCode).append("</item_code>\r\n");
									valueXmlString.append("<item_flag>").append(itemFlag).append("</item_flag>\r\n");
									valueXmlString.append("<item_code_descr>").append(itemDescr).append("</item_code_descr>\r\n");
								}
								else
								{
									String itemCodeRate = "",gsmColumn = "",gsmValue = "";
									double itemrate = 0,gsm = 0,sqrFoot;
									sql = "SELECT ITEM_CODE__RATE FROM ITEM_TYPE WHERE ITEM_TYPE='"+itemType+"'";
									stmtSatt = connSatt.createStatement();
									rsSatt = stmtSatt.executeQuery(sql);
									if (rsSatt.next())
									{
										itemCodeRate = rsSatt.getString("ITEM_CODE__RATE");
									}
									gsmColumn = getAttributeColumn("GSM",itemType,connSatt);
									System.out.println("gsmColumn :: "+gsmColumn);
									gsmValue = getColumnValue(gsmColumn.toLowerCase(),dom,"2");
									System.out.println("GSM :: "+gsmValue);
									if (gsmValue != null && gsmValue.trim().length() > 0)
									{
										gsm = Double.parseDouble(gsmValue);

										DistCommon distCommon = new DistCommon();
										itemrate = distCommon.pickRateGSM(priceList,ls_plDate,itemCodeRate,"","",gsm,connSatt);
										System.out.println("itemrate :: "+itemrate);
										
										sqrFoot = calculateSquareFoot(currentColumn,itemType,dom,connSatt);
										System.out.println("sqrFoot :: "+sqrFoot);
										
										rate = Math.round(sqrFoot * itemrate);
										
										String cutYNCol = "",cutYNValue = "";
										cutYNCol = getAttributeColumn("Cut/Fin",itemType,connSatt);
										cutYNValue = getColumnValue(cutYNCol.toLowerCase(),dom,"2");
										if (cutYNValue != null && cutYNValue.equalsIgnoreCase("F"))
										{
											rate = rate + (rate * 0.12);
											System.out.println("Finish Size Rate :: "+rate);
										}
										valueXmlString.append("<rate>").append(Double.toString(rate)).append("</rate>\r\n");
										
										//
										String ls_unitRate = "",ls_unitStd = "",ls_rate = "",ls_convRate = "";
										double rate1	= 0.00,	convRate = 0.00,rateStd = 0.00;
										ArrayList rateValue = new ArrayList();

										//itemCode = getColumnValue("item_code",dom,"2");
										ls_unitRate = getColumnValue("unit__rate",dom,"2");
										ls_unitStd = getColumnValue("unit__std",dom,"2");
										//ls_rate = getColumnValue("rate",dom,"2");
										ls_convRate = getColumnValue("conv__rtuom_stduom",dom,"2");
										rate1 = Math.round(sqrFoot * itemrate);
										convRate = Double.parseDouble(ls_convRate);
										rateValue = disCommon.convQtyFactor(ls_unitRate,ls_unitStd,"X",rate1,convRate,connSatt);	
										valueXmlString.append("<conv__rtuom_stduom>").append(rateValue.get(0).toString()).append("</conv__rtuom_stduom>\r\n");
										valueXmlString.append("<rate__stduom>").append(rateValue.get(1).toString()).append("</rate__stduom>\r\n");
										//

									}
									valueXmlString.append("<item_code>").append(" ").append("</item_code>\r\n");
									valueXmlString.append("<item_flag>").append(itemFlag).append("</item_flag>\r\n");
									valueXmlString.append("<item_code_descr>").append(" ").append("</item_code_descr>\r\n");
									//valueXmlString.append("<tax_env>").append(" ".trim()).append("</tax_env>\r\n");
								}
								//valueXmlString.append("<item_flag>").append(itemFlag).append("</item_flag>\r\n");
								String columnName ="",totValue = "";
								columnName = getAttributeColumn("Total Qty",itemType,connSatt);
								totValue = getColumnValue(columnName.toLowerCase(),dom,"2");
								//int qtyPerPacket;
								double qtyPerPacket;
								if (columnName != null && columnName.trim().length() > 0 && currentColumn.equalsIgnoreCase(columnName))
								{																
									if (itemFlag.equalsIgnoreCase("N"))
									{
										String totString;
										System.out.println("totValue :: "+totValue);
										if(totValue != null && totValue.trim().length() > 0 && totValue.endsWith("K"))
										{
											totValue = totValue.substring(0,totValue.length()-1);
											qtyPerPacket = Math.floor((new Double(25/theoriticalWgtprPiece)).doubleValue());
											System.out.println("theoriticalWgtprPiece :: "+theoriticalWgtprPiece);

											System.out.println("qtyPerPacket :: "+qtyPerPacket);
											valueXmlString.append("<no_art>").append(totValue).append("</no_art>\r\n");
											
											valueXmlString.append("<quantity>").append(Integer.parseInt(totValue) * new Double(qtyPerPacket).intValue()).append("</quantity>\r\n");
											
											//
											String unitstd = "",quan = "",convqty="";
											double convStd=0,quantity1 = 0;
											unit = getColumnValue("unit",dom,"2"); 
											if (unit == null)
											{
												unit = "";
											}
											unitstd	= getColumnValue("unit__std",dom,"2");
											if (unitstd == null)
											{
												unitstd = "";
											}
											//quan  =  getColumnValue("quantity",dom,"2");
											convqty	= getColumnValue("conv__qty_stduom",dom,"2"); 
											
											ArrayList convValue = new ArrayList();
											quantity1 = 	Double.parseDouble(totValue) * qtyPerPacket;
											convStd = Double.parseDouble(convqty); 
											
											convValue = disCommon.convQtyFactor(unit,unitstd,"",quantity1,convStd,connSatt);
											valueXmlString.append("<conv__qty_stduom>").append(convValue.get(0).toString()).append("</conv__qty_stduom>\r\n");
											valueXmlString.append("<quantity__stduom>").append(convValue.get(1).toString()).append("</quantity__stduom>\r\n");
										
											//
											System.out.println("qtyPerPacket :: "+Integer.parseInt(totValue) * new Double(qtyPerPacket).intValue());

											valueXmlString.append("<"+columnName.toLowerCase()+">").append(Integer.parseInt(totValue) * new Double(qtyPerPacket).intValue()).append("</"+columnName.toLowerCase()+">\r\n");
											Node tempNode = dom.getElementsByTagName(columnName.toLowerCase()).item(0);
											System.out.println("Node name "+tempNode.getNodeName());
											tempNode.getFirstChild().setNodeValue(String.valueOf(Integer.parseInt(totValue) * new Double(qtyPerPacket).intValue()));
										}
										else
										{
											int noArt=0,intGQuantity;
											intGQuantity = (new Double(25/theoriticalWgtprPiece)).intValue();  
											System.out.println("intGQuantity :: "+intGQuantity);
											System.out.println("theoriticalWgtprPiece :: "+theoriticalWgtprPiece);
											if (intGQuantity > 0)
											{
												noArt = Integer.parseInt(totValue)/intGQuantity;
											}											
											System.out.println("noArt :: "+noArt);
											valueXmlString.append("<no_art>").append(noArt).append("</no_art>\r\n");
											System.out.println("quantity (noArt * intGQuantity) :: "+noArt * intGQuantity);


											//valueXmlString.append("<quantity>").append(noArt * intGQuantity).append("</quantity>\r\n");
											valueXmlString.append("<quantity>").append(totValue).append("</quantity>\r\n"); // Added on 22/07/06 at Supreme

											//
											String unitstd = "",quan = "",convqty = "";
											double convStd=0,quantity1 = 0;
											unit = getColumnValue("unit",dom,"2"); 
											if (unit == null)
											{
												unit = "";
											}
											unitstd	= getColumnValue("unit__std",dom,"2");
											if (unitstd == null)
											{
												unitstd = "";
											}
											//quan  =  getColumnValue("quantity",dom,"2");
											convqty	= getColumnValue("conv__qty_stduom",dom,"2"); 
											
											ArrayList convValue = new ArrayList();
											//quantity1 = noArt * intGQuantity;
											quantity1 = Double.parseDouble(totValue);
											convStd = Double.parseDouble(convqty); 
											
											convValue = disCommon.convQtyFactor(unit,unitstd,"",quantity1,convStd,connSatt);
											valueXmlString.append("<conv__qty_stduom>").append(convValue.get(0).toString()).append("</conv__qty_stduom>\r\n");
											valueXmlString.append("<quantity__stduom>").append(convValue.get(1).toString()).append("</quantity__stduom>\r\n");
										
											//
										}
									}
									else
									{
										int tot = Integer.parseInt(totValue)/integralQty;
										valueXmlString.append("<no_art>").append(tot).append("</no_art>\r\n");
										//valueXmlString.append("<quantity>").append(tot * integralQty).append("</quantity>\r\n"); // Added 22/07/06 at Supreme.
										valueXmlString.append("<quantity>").append(totValue).append("</quantity>\r\n");
										
										//For conv Stduom,quantity stduom
										String unitstd = "",quan = "",convqty="";
										double convStd=0,quantity1 =0;
										unit = getColumnValue("unit",dom,"2"); 
										if (unit == null)
										{
											unit = "";
										}
										unitstd	= getColumnValue("unit__std",dom,"2");
										if (unitstd == null)
										{
											unitstd = "";
										}
										//quan  =  getColumnValue("quantity",dom,"2");
										convqty	= getColumnValue("conv__qty_stduom",dom,"2"); 
										
										ArrayList convValue = new ArrayList();
										quantity1 = tot * integralQty;
										convStd = Double.parseDouble(convqty); 
										
										convValue = disCommon.convQtyFactor(unit,unitstd,itemCode,quantity1,convStd,connSatt);
										valueXmlString.append("<conv__qty_stduom>").append(convValue.get(0).toString()).append("</conv__qty_stduom>\r\n");
										valueXmlString.append("<quantity__stduom>").append(convValue.get(1).toString()).append("</quantity__stduom>\r\n");
										//

									}
								}
								String qtyValue = setQuantity(currentColumn,itemType,dom,connSatt);
								System.out.println("qtyValue :: "+qtyValue);
								if (columnName != null && columnName.trim().length() > 0)
								{
									if (currentColumn.equalsIgnoreCase(columnName))
									{
										String bluecolName = getAttributeColumn("Blue",itemType,connSatt);
										valueXmlString.append("<"+bluecolName.toLowerCase()+">").append(qtyValue).append("</"+bluecolName.toLowerCase()+">\r\n");
										//valueXmlString.append("<quantity>").append(columnValue).append("</quantity>\r\n");
									}
									
								}
								columnName = getAttributeColumn("Blue",itemType,connSatt);
								if (columnName != null && columnName.trim().length() > 0)
								{
									if (currentColumn.equalsIgnoreCase(columnName) && totValue != null && totValue.trim().length() > 0)
									{ 
										String bluecolName = getAttributeColumn("YELLOW",itemType,connSatt);
										valueXmlString.append("<"+bluecolName.toLowerCase()+">").append(qtyValue).append("</"+bluecolName.toLowerCase()+">\r\n");
									}									
								}
								columnName = getAttributeColumn("yellow",itemType,connSatt);
								if (columnName != null && columnName.trim().length() > 0)
								{
									if (currentColumn.equalsIgnoreCase(columnName) && totValue != null && totValue.trim().length() > 0)
									{
										String bluecolName = getAttributeColumn("other",itemType,connSatt);
										valueXmlString.append("<"+bluecolName.toLowerCase()+">").append(qtyValue).append("</"+bluecolName.toLowerCase()+">\r\n");
									}
									
								}	
								rsSatt.close();
								stmtSatt.close();
							}
							catch(Exception e)
							{
								System.out.println("Exception :[SordAttEJB][itemChanged::case 2::Item Type] :==>\n"+e.getMessage());
								throw new ITMException(e);
							}
						}
						String attribute = calculateTheoriticalWeight(currentColumn,itemType,dom,connSatt);						
						if (attribute != null)
						{
							String columnName = getAttributeColumn("Theoritical Wgt",itemType,connSatt);
					   		if (columnName != null && columnName.trim().length() > 0)
					   		{
					   			if(attribute != null)
					   			{
					   				System.out.println("Actual Thearotical Weight :: "+attribute);
					   				double d = getRequiredDecimal(Double.parseDouble(attribute), 0);
					   				valueXmlString.append("<"+columnName.toLowerCase()+">").append((attribute == null)? " ":Double.toString(d)).append("</"+columnName.toLowerCase()+">\r\n");
					   			}
					   		}
						}
						String setValue = setTotalQty(dom,currentColumn,itemType,connSatt);
						valueXmlString.append(setValue);
						
					}
					else if (currentColumn.trim().equals("unit") || currentColumn.trim().equals("quantity") || currentColumn.trim().equals("conv__qty_stduom"))
					{
						String itemFlag = "",itemCode = "";
						double quanStd = 0.00,quantity = 0.00,convStd = 0.00;
						boolean flag = false;
						String unit = "",unitstd = "",quan = "",convqty = "";
						itemCode = getColumnValue("item_code",dom,"2");
						
						if (currentColumn.trim().equals("unit"))
						{
							itemFlag = getColumnValue("item_flag",dom,"2");
							if (itemCode == null || itemCode.trim().length() == 0)
							{
								flag = true;
								valueXmlString.append("<unit__std>").append(columnValue).append("</unit__std>\r\n");
							}
						}
						unit = getColumnValue("unit",dom,"2"); 
						if (unit == null)
						{
							unit = "";
						}
						unitstd	= getColumnValue("unit__std",dom,"2");
						if (unitstd == null)
						{
							unitstd = "";
						}
						quan  =  getColumnValue("quantity",dom,"2");
						convqty	= getColumnValue("conv__qty_stduom",dom,"2"); 
						if (flag)
						{
							unitstd = unit;							
						}
						ArrayList convValue = new ArrayList();
						quantity = 	Double.parseDouble(quan);
						convStd = Double.parseDouble(convqty); 
						//convValue = itmDBAccessEJB.getConvQuantityFact(unit,unitstd,itemCode,quantity,convStd,connSatt);
						convValue = disCommon.convQtyFactor(unit,unitstd,itemCode,quantity,convStd,connSatt);
						//convQtyFactor(unitFrom,unitTo,itemCode,toConvert,conn); //Added st Supreme
						valueXmlString.append("<conv__qty_stduom>").append(convValue.get(0).toString()).append("</conv__qty_stduom>\r\n");
						valueXmlString.append("<quantity__stduom>").append(convValue.get(1).toString()).append("</quantity__stduom>\r\n");
						if (currentColumn.trim().equals("quantity"))
						{
							double rateClg = 0,rate = 0;
							java.util.Date ordDate= new java.util.Date();
							priceList = getColumnValue("price_list",dom1);
							priceListClg = getColumnValue("price_list__clg",dom1);
							orderDate = getColumnValue("ord_date",dom1);
							ordDate = new SimpleDateFormat(genericUtility.getApplDateFormat()).parse(orderDate);
							itemCode = getColumnValue("item_code",dom,"2");
							if (itemCode == null)
							{
								itemCode = "";
							}
							if (priceList != null && priceList.trim().length() > 0 && itemCode.trim().length() > 0)
							{
								rate = distDiscountEJB.pickRate(priceList,ordDate,itemCode,"","L",Double.parseDouble(columnValue),connSatt);
								valueXmlString.append("<rate>").append(Double.toString(rate)).append("</rate>\r\n");
							}
							if (priceListClg != null && priceListClg.trim().length() > 0 && itemCode.trim().length() > 0)
							{
								rateClg = distDiscountEJB.pickRate(priceListClg,ordDate,itemCode,"","L",Double.parseDouble(columnValue),connSatt);
								valueXmlString.append("<rate__clg>").append(Double.toString(rateClg)).append("</rate__clg>\r\n");
							}						
						}						
					}
					else if (currentColumn.trim().equals("rate") || currentColumn.trim().equals("unit__rate") || currentColumn.trim().equals("conv__rtuom_stduom"))
					{
						String ls_unitRate = "",ls_unitStd = "",ls_rate = "",ls_convRate = "",itemCode = "";
						double rate	= 0.00,	convRate = 0.00,rateStd = 0.00;
						ArrayList rateValue = new ArrayList();

						itemCode = getColumnValue("item_code",dom,"2");
						ls_unitRate = getColumnValue("unit__rate",dom,"2");
						ls_unitStd = getColumnValue("unit__std",dom,"2");
						ls_rate = getColumnValue("rate",dom,"2");
						ls_convRate = getColumnValue("conv__rtuom_stduom",dom,"2");
						rate = Double.parseDouble(ls_rate);
						convRate = Double.parseDouble(ls_convRate);
						//rateValue = itmDBAccessEJB.getConvQuantityFact(ls_unitRate,ls_unitStd,itemCode,rate,convRate,connSatt);	
						rateValue = disCommon.convQtyFactor(ls_unitRate,ls_unitStd,itemCode,rate,convRate,connSatt);	
						valueXmlString.append("<conv__rtuom_stduom>").append(rateValue.get(0).toString()).append("</conv__rtuom_stduom>\r\n");
						valueXmlString.append("<rate__stduom>").append(rateValue.get(1).toString()).append("</rate__stduom>\r\n");
					}
					else if (currentColumn.trim().equals("item_code")) 
					{
						String itemCode = "",descr = "";
						if (childNode.getFirstChild() != null)
						{
							itemCode = childNode.getFirstChild().getNodeValue().trim();
						}															   						
						if (itemCode.trim().length() > 0)
						{
							stmtSatt = connSatt.createStatement();
							sql = "SELECT PHY_ATTRIB_1,PHY_ATTRIB_2,PHY_ATTRIB_3,PHY_ATTRIB_4,PHY_ATTRIB_5,PHY_ATTRIB_6,PHY_ATTRIB_7,PHY_ATTRIB_8,PHY_ATTRIB_9,PHY_ATTRIB_10, "+
								  "PHY_ATTRIB_16,PHY_ATTRIB_17,PHY_ATTRIB_18,PHY_ATTRIB_19,PHY_ATTRIB_20,PHY_ATTRIB_21,PHY_ATTRIB_22 FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
							rsSatt = stmtSatt.executeQuery(sql);
							if (rsSatt.next())
							{
								valueXmlString.append("<phy_attrib_1>").append(rsSatt.getString(1)== null ? " ":rsSatt.getString(1)).append("</phy_attrib_1>");
								valueXmlString.append("<phy_attrib_2>").append(rsSatt.getString(2)== null ? " ":rsSatt.getString(2)).append("</phy_attrib_2>");
								valueXmlString.append("<phy_attrib_3>").append(rsSatt.getString(3)== null ? " ":rsSatt.getString(3)).append("</phy_attrib_3>");
								valueXmlString.append("<phy_attrib_4>").append(rsSatt.getString(4)== null ? " ":rsSatt.getString(4)).append("</phy_attrib_4>");
								valueXmlString.append("<phy_attrib_5>").append(rsSatt.getString(5)== null ? " ":rsSatt.getString(5)).append("</phy_attrib_5>");
								valueXmlString.append("<phy_attrib_6>").append(rsSatt.getString(6)== null ? " ":rsSatt.getString(6)).append("</phy_attrib_6>");
								valueXmlString.append("<phy_attrib_7>").append(rsSatt.getString(7)== null ? " ":rsSatt.getString(7)).append("</phy_attrib_7>");
								valueXmlString.append("<phy_attrib_8>").append(rsSatt.getString(8)== null ? " ":rsSatt.getString(8)).append("</phy_attrib_8>");
								valueXmlString.append("<phy_attrib_9>").append(rsSatt.getString(9)== null ? " ":rsSatt.getString(9)).append("</phy_attrib_9>");
								valueXmlString.append("<phy_attrib_10>").append(rsSatt.getString(10)== null ? " ":rsSatt.getString(10)).append("</phy_attrib_10>");
								valueXmlString.append("<phy_attrib_16>").append(rsSatt.getString(11)== null ? " ":rsSatt.getString(11)).append("</phy_attrib_16>");
								valueXmlString.append("<phy_attrib_17>").append(rsSatt.getString(12)== null ? " ":rsSatt.getString(12)).append("</phy_attrib_17>");
								valueXmlString.append("<phy_attrib_18>").append(rsSatt.getString(13)== null ? " ":rsSatt.getString(13)).append("</phy_attrib_18>");
								valueXmlString.append("<phy_attrib_19>").append(rsSatt.getString(14)== null ? " ":rsSatt.getString(14)).append("</phy_attrib_19>");
								valueXmlString.append("<phy_attrib_20>").append(rsSatt.getString(15)== null ? " ":rsSatt.getString(15)).append("</phy_attrib_20>");
								valueXmlString.append("<phy_attrib_21>").append(rsSatt.getString(16)== null ? " ":rsSatt.getString(16)).append("</phy_attrib_21>");
								valueXmlString.append("<phy_attrib_22>").append(rsSatt.getString(17)== null ? " ":rsSatt.getString(17)).append("</phy_attrib_22>");
							}
							sql = "SELECT COUNT(*) FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
							rsSatt = stmtSatt.executeQuery(sql);
							if (rsSatt.next())
							{
								if (rsSatt.getInt(1) > 0 )
								{
									valueXmlString.append("<item_flag>E</item_flag>");
								}
							}
							valueXmlString.append(getItemDetails(itemCode,dom,dom1,connSatt));						
						}						
					}					
					valueXmlString.append("</Detail>\r\n");
					break;
			}
			valueXmlString.append("</Root>\r\n");	
		}
		catch(Exception e)
		{
			System.out.println("Exception :[SordAttEJB][itemChanged] :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				connSatt.close();
				connSatt = null;
				System.out.println("finally Connection Closed.......");
			}
			catch(SQLException e)
			{
				System.out.println("Exception :[SordAttEJB][itemChanged::finally] :==>\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		return valueXmlString.toString();
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
		if (as_curr_code.equalsIgnoreCase(as_curr_code__to))
		{
			return 1.00;	
		}
		if (as_curr_code__to.equals(""))
		{
			try
			{
				conn = getConnection();
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
				System.out.println("Exception :: [SordAttEJB][getDailyExchRateSellBuy] ::"+e);
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
			catch(Exception e)
			{
				System.out.println("Exception :: [SordAttEJB][getDailyExchRateSellBuy]"+e);
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
			catch(Exception e)
			{
				System.out.println("Exception :: [SordAttEJB][getDailyExchRateSellBuy][type 'S']"+e);
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
				System.out.println("Executing SQL ::"+sql);
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
			catch(Exception e)
			{
				System.out.println("Exception ::[SordAttEJB][getDailyExchRateSellBuy]"+e);
			}
	   	}
		if (lc_exch_rate == 0)
		{
			try
			{
				stmt = conn.createStatement();
				sql = "SELECT RTRIM(CASE WHEN VAR_VALUE IS NULL THEN 'Y' ELSE VAR_VALUE END) FROM FINPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'EXCRT_CURR'";
				System.out.println("Executing SQL ::"+sql);
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
					System.out.println("Executing SQL ::"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						lc_exch_rate = rs.getDouble(1);
					}
				}
				rs.close();
				stmt.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception [SordAttEJB][getDailyExchRateSellBuy]"+e);
			}
		}
		try
		{
			conn.close();
			conn = null;
		}
		catch(Exception se){}
		return lc_exch_rate; 
	}

	public String acctDetrInvoice(String asItemCode,String asItemSer,String asPerpous,String asTransType,String asAcctType)
	{
		String lsItemSer = null;
		String lsAcctCode = null;
		String lsCctrCode = null;
		
		Connection connectionObject = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		ResultSet rs5 = null;
		String sql = null;
		String sql1 = null;
		
		String lsFinEntity = null;
		StringBuffer valueXmlString = new StringBuffer();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			
			connectionObject = itmDBAccess.getConnection(); 
			stmt = connectionObject.createStatement();
			if(asItemCode == null || asItemCode.length() == 0) asItemCode = ""; 
			if(asPerpous.equals("S-INV"))
			{
				if(asAcctType.equals("SAL"))
				{
					sql ="select acct_code__sal,cctr_code__sal from item_acct_detr "+
					"where item_code = '"+asItemCode+"' and item_ser = '"+asItemSer+"' and tran_type = '"+asTransType+"'";
					rs = stmt.executeQuery(sql);
					if((rs.next() == false) || rs.getString(1) == null)
					{
						sql="select acct_code__sal,cctr_code__sal from item_acct_detr "+ 
						"where item_ser = ' ' and item_code ='"+asItemCode+"' and tran_type = '"+asTransType+"'";
						rs1 = stmt.executeQuery(sql);
						if((rs1.next() == false) || rs1.getString(1) == null)
						{
							sql ="select acct_code__sal,cctr_code__sal from item_acct_detr "+
							"where item_ser = ' ' and item_code ='"+asItemCode+"' and tran_type = ' '";
							rs2 = stmt.executeQuery(sql);
							if((rs2.next() == false) || rs2.getString(1) == null)
							{
								if(asItemCode == null && asItemCode.length() == 0)
								{
										sql="select item_ser from item where item_code = '"+asItemCode+"'";
								}
								else
								{
									lsItemSer = asItemSer;
								}
								sql="select acct_code__sal, cctr_code__sal from item_acct_detr "+
									"where item_ser = '"+lsItemSer+"'  and item_code = ' ' and tran_type = '"+asTransType+"'";
								rs3 =  stmt.executeQuery(sql);
								if((rs3.next() == false) || rs3.getString(1) == null)
								{
									sql="select acct_code__sal, cctr_code__sal from item_acct_detr "+
									"where item_ser = '"+lsItemSer+"'  and item_code = ' ' and tran_type = ''";
									rs4 =  stmt.executeQuery(sql);
									if((rs4.next() == false) || rs4.getString(1) == null)
									{
										sql ="select acct_code__ar,cctr_code__ar from itemser where item_ser ='"+lsItemSer+"'";
										rs5 =  stmt.executeQuery(sql);
										if(rs5.next())
										{
											lsAcctCode = rs5.getString(1);
											lsCctrCode = rs5.getString(2);			
										}
									}
									else
									{
										lsAcctCode = rs4.getString(1);
										lsCctrCode = rs4.getString(2);	
									}
								}
								else
								{
									lsAcctCode = rs3.getString(1);
									lsCctrCode = rs3.getString(2);	
								}
							}
							else
							{
								lsAcctCode = rs2.getString(1);
								lsCctrCode = rs2.getString(2);	
							}
						}
						else
						{
							lsAcctCode = rs1.getString(1);
							lsCctrCode = rs1.getString(2);	
						}
					}
					else
					{
						lsAcctCode = rs.getString(1);
						lsCctrCode = rs.getString(2);	
					}
				}
			}
			if(lsAcctCode == null) lsAcctCode ="";
			if(lsCctrCode == null) lsCctrCode ="";
		}
		catch(Exception e)
		{
			System.out.println("Exception :SorderEJB :itemChanged(Document,String):" + e.getMessage() + ":");
			valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));
		}
		return (lsAcctCode + "~t" + lsCctrCode);
	}

	private String getItemDetails(String itemCode,Document dom, Document dom1,Connection conn)
	{
		StringBuffer retString = new StringBuffer();
		String itemSer = "",custCode = "",siteCode = "";
		String priceList = "",ls_ordDate = "",quantity ="",mrate ="",priceListDisc = "",ls_unit = "",ls_plDate = "";
		String taxchap = "",taxclass = "",taxenv = "",unitStd = "",locType = "",unit = "",unitRate = "";
		double discount = 0.00,rate = 0;
		boolean connState = false;
		Statement stmt = null;
		String sql = "",frStation = "",toStation = "",itemDescr = "";
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistDiscount distDiscountEJB = new DistDiscount();
		java.util.Date ordDate = new java.util.Date();
		java.util.Date plDate = new java.util.Date();
		ConnDriver connDrv = new ConnDriver();
		try
		{
			if (conn == null)
			{
				//Changes and Commented By Bhushan on 13-06-2016 :START
				//conn = connDrv.getConnectDB("Driver");
				conn = getConnection();
				//Changes and Commented By Bhushan on 13-06-2016 :END
				connState = true;
			}
			stmt = conn.createStatement();
			itemSer =  genericUtility.getColumnValue("item_ser",dom1);
			custCode = genericUtility.getColumnValue("cust_code",dom1);
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				itemDescr = rs.getString("DESCR");	
			}
			sql = "SELECT STAN_CODE FROM SITE WHERE SITE_CODE = '"+siteCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				frStation = rs.getString("STAN_CODE");
			}
			sql = "SELECT STAN_CODE FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				toStation = rs.getString("STAN_CODE");
			}
			sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				unitStd = rs.getString("UNIT");	 
			}
			sql = "SELECT LOC_TYPE,UNIT__SAL,UNIT__RATE FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				locType = rs.getString("LOC_TYPE");
				unit = rs.getString("UNIT__SAL");
				unitRate = rs.getString("UNIT__RATE");
			}
			taxchap = itmDBAccessEJB.getTaxChapter(itemCode,itemSer,'C',custCode,siteCode,conn);
			taxclass = itmDBAccessEJB.getTaxClass('C',custCode,itemCode,siteCode,conn); 
			taxenv =  itmDBAccessEJB.getTaxEnv(frStation,toStation,taxchap,taxclass,siteCode,conn);
			priceList = genericUtility.getColumnValue("price_list",dom1);
			ls_ordDate = genericUtility.getColumnValue("ord_date",dom1);
			quantity = genericUtility.getColumnValue("quantity",dom,"2");
			ordDate = getDateObject(ls_ordDate);
			mrate = genericUtility.getColumnValue("rate",dom,"2");
			ls_unit = genericUtility.getColumnValue("unit",dom,"2");
			priceListDisc = genericUtility.getColumnValue("price_list__disc",dom,"2");
			ls_plDate = genericUtility.getColumnValue("pl_date",dom1);
			plDate = getDateObject(ls_plDate);
			if (mrate != null && mrate.trim().equals("0"))
			{
				if (priceList != null && priceList.trim().length() > 0)
				{
					rate = distDiscountEJB.pickRate(priceList,ordDate,itemCode,"","L",Double.parseDouble(quantity),conn);
				}
				retString.append("<rate>").append(Double.toString(rate)).append("</rate>");
			}
			if (priceListDisc != null && priceListDisc.trim().length() > 0)
			{
				discount = distDiscountEJB.getDiscount(priceListDisc,ordDate,custCode,siteCode,itemCode,ls_unit,0.00,plDate,Double.parseDouble(quantity),conn);
				retString.append("<discount>").append(Double.toString(discount)).append("</discount>");
			}
			retString.append("<loc_type>").append((locType == null)? " ":locType).append("</loc_type>");
			retString.append("<unit>").append((unit == null)? " ":unit).append("</unit>");
			retString.append("<unit__rate>").append((unitRate == null)? " ":unitRate).append("</unit__rate>");
			retString.append("<tax_class>").append((taxclass == null)? "":taxclass).append("</tax_class>");
			retString.append("<tax_chap>").append((taxchap == null)? " ":taxchap).append("</tax_chap>");
			retString.append("<tax_env>").append((taxenv == null)? " ":taxenv).append("</tax_env>");
			retString.append("<unit__std>").append(unitStd).append("</unit__std>");
			retString.append("<item_code>").append(itemCode).append("</item_code>");
			retString.append("<item_code_descr>").append(itemDescr).append("</item_code_descr>");
		}
		catch(SQLException se)
		{
			System.out.println("Exception ::"+se.getMessage());
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
		}
		finally
		{
			if (connState = true)
			{
				try
				{
					conn.close();
					conn = null;
				}catch(Exception s){}
			}
		}
		return retString.toString();
	}

	private String getAttributeColumn(String attributeName,String itemTYpe,Connection conn)
	{
		Statement stmt = null; 
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String columnName = "",columnValue = "";
		int noOfColumn;
		try
		{
			System.out.println("Attribute Name "+attributeName);
			if (hashAttribute == null)
			{
				stmt = conn.createStatement();
				hashAttribute = new HashMap();
				String sql = "SELECT PHY_ATTRIB_1,PHY_ATTRIB_2,PHY_ATTRIB_3,PHY_ATTRIB_4,PHY_ATTRIB_5, "+
					"PHY_ATTRIB_6,PHY_ATTRIB_7,PHY_ATTRIB_8,PHY_ATTRIB_9,PHY_ATTRIB_10,PHY_ATTRIB_11, "+
					"PHY_ATTRIB_12,PHY_ATTRIB_13,PHY_ATTRIB_14,PHY_ATTRIB_15,PHY_ATTRIB_16,PHY_ATTRIB_17, "+
					"PHY_ATTRIB_18,PHY_ATTRIB_19,PHY_ATTRIB_20,PHY_ATTRIB_21,PHY_ATTRIB_22 "+
					"FROM ITEM_TYPE WHERE ITEM_TYPE = '"+itemTYpe+"'";
				rs = stmt.executeQuery(sql);	
				rsmd = rs.getMetaData();
				
				if (rs.next())
				{
					noOfColumn = rsmd.getColumnCount();
					for (int i=1;i<=noOfColumn ;i++ )
					{
						columnValue = rs.getString(i);
						columnName = rsmd.getColumnName(i);	
						if (columnValue != null)
						{
							hashAttribute.put(columnValue.toLowerCase(),columnName);		
						}
					}
				}
			}
			columnName = (hashAttribute.get(attributeName.toLowerCase())).toString();
			if (columnName == null)
			{
				columnName = "";
			}
			System.out.println("Attribute Value "+attributeName+" Field Name "+columnName);	
		}catch(Exception e)
		{
			System.out.println("Exception e"+e);
			e.printStackTrace();
		}
		return columnName;
	}


	private String calculateTheoriticalWeight(String columnName,String itemType,Document dom,Connection conn)
	{
		double height = 0,len = 0,width =0,gsm = 0,result,quantity=1;
			
		String columnValue = null,lenColumn,widthColumn,heightColumn,gsmColumn,unitHeightCol,unitWidthCol,unitLenCol,quanityStr;
		String cutYNCol;
		String phyHeight,phyLen,phyWidth,phyGsm;
		String heightUnitCol,lenUnitCol,widthUnitColumn;
		String unitOfHeight,unitOfLength,unitOfWidth;
		ArrayList attributeValue = null;
		boolean flag = false;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon disCommon = new DistCommon();
		try
		{
			heightColumn = getAttributeColumn("Height",itemType,conn);		
			lenColumn = getAttributeColumn("Length",itemType,conn);		
			widthColumn = getAttributeColumn("Width",itemType,conn);		
			gsmColumn = getAttributeColumn("GSM",itemType,conn);		
			if (gsmColumn == null || gsmColumn.trim().length() == 0)
			{
				gsmColumn = getAttributeColumn("Micron",itemType,conn);
				if (gsmColumn != null && gsmColumn.trim().length() > 0)
				{
					flag = true;
				}
			}
			unitHeightCol = getAttributeColumn("Unit (H)",itemType,conn);		
			unitLenCol = getAttributeColumn("Unit (L)",itemType,conn);		
			unitWidthCol = getAttributeColumn("Unit (W)",itemType,conn);
			quanityStr = getColumnValue("quantity",dom,"2");
			cutYNCol = getAttributeColumn("Cut/Fin",itemType,conn);
			if (quanityStr != null && quanityStr.trim().length() > 0)
			{
				quantity = Double.parseDouble(quanityStr);
			}
			if (columnName.equalsIgnoreCase(heightColumn) || columnName.equalsIgnoreCase(lenColumn) || columnName.equalsIgnoreCase(widthColumn) || columnName.equalsIgnoreCase(gsmColumn) || columnName.equalsIgnoreCase(unitHeightCol) || columnName.equalsIgnoreCase(unitLenCol) || columnName.equalsIgnoreCase(unitWidthCol) || columnName.equalsIgnoreCase(cutYNCol))
			{
				if (heightColumn != null && heightColumn.trim().length() > 0)
				{
					phyHeight = getColumnValue(heightColumn.toLowerCase(),dom,"2");		
					if (phyHeight != null && phyHeight.trim().length() > 0)
					{
						height = Double.parseDouble(phyHeight);
					}
					heightUnitCol = getAttributeColumn("Unit (H)",itemType,conn);
					if (heightUnitCol != null && heightUnitCol.trim().length() > 0)
					{
						unitOfHeight = getColumnValue(heightUnitCol.toLowerCase(),dom,"2");
						if (unitOfHeight != null && !(unitOfHeight.equalsIgnoreCase("FTS")))
						{
							//attributeValue = itmDBAccessEJB.getConvQuantityFact(unitOfHeight,"FTS", "", height, 0,conn);
							//height = Double.parseDouble(attributeValue.get(1).toString());
							height = disCommon.convQtyFactor(unitOfHeight,"FTS","X",height,conn);
						}
					}
				}
				if (lenColumn != null && lenColumn.trim().length() > 0)
				{
					phyLen = getColumnValue(lenColumn.toLowerCase(),dom,"2");		
					if (phyLen != null && phyLen.trim().length() > 0)
					{
						len = Double.parseDouble(phyLen);
					}
					lenUnitCol = getAttributeColumn("Unit (L)",itemType,conn);
					if (lenUnitCol != null && lenUnitCol.trim().length() > 0)
					{
						unitOfLength = getColumnValue(lenUnitCol.toLowerCase(),dom,"2");
						if (unitOfLength != null && !(unitOfLength.equalsIgnoreCase("FTS")))
						{
							//attributeValue = itmDBAccessEJB.getConvQuantityFact(unitOfLength,"FTS", "", len, 0,conn);
							//len = Double.parseDouble(attributeValue.get(1).toString());
							len = disCommon.convQtyFactor(unitOfLength,"FTS","X",len,conn);
						}
					}
				}
				if (widthColumn != null && widthColumn.trim().length() > 0)
				{
					phyWidth = getColumnValue(widthColumn.toLowerCase(),dom,"2");		
					if (phyWidth != null && phyWidth.trim().length() > 0)
					{
						width = Double.parseDouble(phyWidth);
					}
					widthUnitColumn = getAttributeColumn("Unit (W)",itemType,conn);
					if (widthUnitColumn != null && widthUnitColumn.trim().length() > 0)
					{
						unitOfWidth = getColumnValue(widthUnitColumn.toLowerCase(),dom,"2");
						if (unitOfWidth != null && !(unitOfWidth.equalsIgnoreCase("FTS")))
						{
							//attributeValue = itmDBAccessEJB.getConvQuantityFact(unitOfWidth,"FTS", "", width, 0,conn);
							//width = Double.parseDouble(attributeValue.get(1).toString());
							width = disCommon.convQtyFactor(unitOfWidth,"FTS","X",width,conn);
						}
					}
				}
				if (gsmColumn != null && gsmColumn.trim().length() > 0)
				{
					phyGsm = getColumnValue(gsmColumn.toLowerCase(),dom,"2");		
					if (phyGsm != null && phyGsm.trim().length() > 0)
					{
						if (flag)
						{
							gsm = 0.928 * Double.parseDouble(phyGsm);							 
						}
						else
						{
							gsm = Double.parseDouble(phyGsm);
						}
					}
					
				}
				if (height == 0)
				{
					result = (len * width / 10.7584) * (gsm / 1000);	
				}
				else
				{
					 result =( ( (len * width) + (2 *(len * height)) + (2 * (width * height)) ) / 10.7584 ) * (gsm / 1000);
				}
				//result = itmDBAccessEJB.getRndamt(result,'P' , 0.001);
				
				if((result % 0.001) < (0.001 / 2))
				{
					result = (result - (result % 0.001));
				}
				else
				{
					result = (result - (result % 0.001) + 0.001);
				}
				if (cutYNCol != null && cutYNCol.trim().length() > 0)
				{
					String cutYNValue = getColumnValue(cutYNCol.toLowerCase(),dom,"2");
					if (cutYNValue != null && cutYNValue.trim().length() > 0 && cutYNValue.equalsIgnoreCase("F"))
					{
						result = result + result * 0.12;
					}
				}
				theoriticalWgtprPiece = result;
				//columnValue = Double.toString(result * quantity);
				columnValue = Double.toString(result);
			}
		}
		catch(Exception e){}

		return columnValue;
	}

	private double calculateSquareFoot(String columnName,String itemType,Document dom,Connection conn)
	{
		double height = 0,len = 0,width =0,result = 0;

		String columnValue = "",lenColumn,widthColumn,heightColumn,unitHeightCol,unitWidthCol,unitLenCol,gsmColumn;
		String phyHeight,phyLen,phyWidth;
		String heightUnitCol,lenUnitCol,widthUnitColumn;
		String unitOfHeight,unitOfLength,unitOfWidth;
		ArrayList attributeValue = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon disCommon = new DistCommon();
		try
		{
			heightColumn = getAttributeColumn("Height",itemType,conn);		
			lenColumn = getAttributeColumn("Length",itemType,conn);		
			widthColumn = getAttributeColumn("Width",itemType,conn);		
			gsmColumn = getAttributeColumn("GSM",itemType,conn);	
			unitHeightCol = getAttributeColumn("Unit (H)",itemType,conn);		
			unitLenCol = getAttributeColumn("Unit (L)",itemType,conn);		
			unitWidthCol = getAttributeColumn("Unit (W)",itemType,conn);
			
			System.out.println("Current Column "+columnName+"gsmColumn  "+gsmColumn);
			//if (columnName.equalsIgnoreCase(heightColumn) || columnName.equalsIgnoreCase(lenColumn) || columnName.equalsIgnoreCase(widthColumn) || columnName.equalsIgnoreCase(gsmColumn) || columnName.equalsIgnoreCase(unitHeightCol) || columnName.equalsIgnoreCase(unitLenCol) || columnName.equalsIgnoreCase(unitWidthCol))
			//{
			
				if (heightColumn != null && heightColumn.trim().length() > 0)
				{
					phyHeight = getColumnValue(heightColumn.toLowerCase(),dom,"2");		
					if (phyHeight != null && phyHeight.trim().length() > 0)
					{
						height = Double.parseDouble(phyHeight);
					
						heightUnitCol = getAttributeColumn("Unit (H)",itemType,conn);
						if (heightUnitCol != null && heightUnitCol.trim().length() > 0)
						{
							unitOfHeight = getColumnValue(heightUnitCol.toLowerCase(),dom,"2");
							if (unitOfHeight != null && !(unitOfHeight.equalsIgnoreCase("FTS")))
							{
								//attributeValue = itmDBAccessEJB.getConvQuantityFact(unitOfHeight,"FTS", "", height, 0,conn);
								//height = Double.parseDouble(attributeValue.get(1).toString());
								height = disCommon.convQtyFactor(unitOfHeight,"FTS","X",height,conn);
							}
						}
					}
				}
				if (lenColumn != null && lenColumn.trim().length() > 0)
				{
					phyLen = getColumnValue(lenColumn.toLowerCase(),dom,"2");		
					if (phyLen != null && phyLen.trim().length() > 0)
					{
						len = Double.parseDouble(phyLen);

						lenUnitCol = getAttributeColumn("Unit (L)",itemType,conn);
						if (lenUnitCol != null && lenUnitCol.trim().length() > 0)
						{
							unitOfLength = getColumnValue(lenUnitCol.toLowerCase(),dom,"2");
							if (unitOfLength != null && !(unitOfLength.equalsIgnoreCase("FTS")))
							{
								//attributeValue = itmDBAccessEJB.getConvQuantityFact(unitOfLength,"FTS", "", len, 0,conn);
								//len = Double.parseDouble(attributeValue.get(1).toString());
								len = disCommon.convQtyFactor(unitOfLength,"FTS","X",len,conn);
							}
						}
						
					}
				}
				if (widthColumn != null && widthColumn.trim().length() > 0)
				{
					phyWidth = getColumnValue(widthColumn.toLowerCase(),dom,"2");		
					
					if (phyWidth != null && phyWidth.trim().length() > 0)
					{
						width = Double.parseDouble(phyWidth);

						widthUnitColumn = getAttributeColumn("Unit (W)",itemType,conn);
						if (widthUnitColumn != null && widthUnitColumn.trim().length() > 0)
						{
							unitOfWidth = getColumnValue(widthUnitColumn.toLowerCase(),dom,"2");
							if (unitOfWidth != null && !(unitOfWidth.equalsIgnoreCase("FTS")))
							{
								//attributeValue = itmDBAccessEJB.getConvQuantityFact(unitOfWidth,"FTS", "", width, 0,conn);
								//width = Double.parseDouble(attributeValue.get(1).toString());
								width = disCommon.convQtyFactor(unitOfWidth,"FTS","X",width,conn);
							}
						}
					}
				}
				System.out.println("Length : "+len+" Width : "+width+" Height : "+height);
				if (height == 0)
				{
					result = (len * width );	
				}
				else
				{
					result = ( (len * width) + (2 *(len * height)) + (2 * (width * height)) );
				}
			//}
		}
		catch(Exception e)
		{
			System.out.println("Exception : calculateSquareFoot "+e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	private String setQuantity(String columnName,String itemType,Document dom,Connection conn)
	{
		int yellow = 0,total = 0,blue =0,other = 0, result;

		String columnValue = null,totalQtyCol,blueQtyCol,yellowQtyCol,otherQtyCol;
		String yellowQty,totalQty,blueQty,otherQty;
		ArrayList attributeValue = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon disCommon = new DistCommon();
		try
		{
			yellowQtyCol = getAttributeColumn("Yellow",itemType,conn);		
			totalQtyCol = getAttributeColumn("Total Qty",itemType,conn);		
			blueQtyCol = getAttributeColumn("Blue",itemType,conn);		
			otherQtyCol = getAttributeColumn("Other",itemType,conn);		
			
			if (columnName.equalsIgnoreCase(yellowQtyCol) || columnName.equalsIgnoreCase(totalQtyCol) || columnName.equalsIgnoreCase(blueQtyCol) || columnName.equalsIgnoreCase(otherQtyCol))
			{
				if (yellowQtyCol != null && yellowQtyCol.trim().length() > 0)
				{
					yellowQty = getColumnValue(yellowQtyCol.toLowerCase(),dom,"2");		
					if (yellowQty != null && yellowQty.trim().length() > 0)
					{
						yellow = Integer.parseInt(yellowQty);
					}
				}
				if (totalQtyCol != null && totalQtyCol.trim().length() > 0)
				{
					totalQty = getColumnValue(totalQtyCol.toLowerCase(),dom,"2");		
					if (totalQty != null && totalQty.trim().length() > 0)
					{
						System.out.println("totalQty in Set Qty "+totalQty);
						total = Integer.parseInt(totalQty);
					}
				}
				if (blueQtyCol != null && blueQtyCol.trim().length() > 0)
				{
					blueQty = getColumnValue(blueQtyCol.toLowerCase(),dom,"2");		
					if (blueQty != null && blueQty.trim().length() > 0)
					{
						blue = Integer.parseInt(blueQty);
					}
				}
				if (otherQtyCol != null && otherQtyCol.trim().length() > 0)
				{
					otherQty = getColumnValue(otherQtyCol.toLowerCase(),dom,"2");		
					if (otherQty != null && otherQty.trim().length() > 0)
					{
						other = Integer.parseInt(otherQty);
					}
					
				}
				if (columnName.equalsIgnoreCase(totalQtyCol))
				{
					result = total - yellow - other;
					columnValue = Integer.toString(result);
				}
				else if (columnName.equalsIgnoreCase(blueQtyCol))
				{
					result = total - blue - other;
					columnValue = Integer.toString(result);
				}
				else if (columnName.equalsIgnoreCase(yellowQtyCol))
				{
					result = total - blue - yellow;
					columnValue = Integer.toString(result);
				}				
			}
		}
		catch(Exception e){}
		return columnValue;
	}

	private String getRelativeDate(int no) throws RemoteException , ITMException
	{
		String relativeDate = "";
		Calendar  calObject = Calendar.getInstance();
		java.util.Date  dateId =new java.util.Date();
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			calObject.setTime(dateId);
			calObject.add(Calendar.DATE,no);
			dateId = calObject.getTime();
			System.out.println("getRelativeDate():dateId:"+dateId.toString());
			relativeDate = sdf.format(dateId);
		}catch(Exception e)
		{
			System.out.println("Exception :SordAttEJB:getRelativeDate() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("getRelativeDate():relativeDate:"+relativeDate);
		return relativeDate;
	}
	
	private double getRequiredDecimal(double actVal, int prec)
	{
		NumberFormat numberFormat = NumberFormat.getIntegerInstance ();
		Double DoubleValue = new Double (actVal);
		numberFormat.setMaximumFractionDigits(3);
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",","");
		double reqVal = Double.parseDouble(strValue);
		return reqVal;
	}
	
	private String setTotalQty(Document dom,String currentColumn,String itemType,Connection conn)
	{
		String blueCol = "",blueQty = "",yellowCol = "",yellowQty = "",otherCol = "",otherQty = "",totalQtyCol = "",totalQty ="";
		int blueQuantity = 0,yellowQuantity = 0,otherQuantity = 0,totalQuantity = 0;
		StringBuffer resultString = new StringBuffer("");
		double qtyPerPacket = 0;
		DistCommon disCommon = new DistCommon();
		try
		{
			blueCol = getAttributeColumn("BLUE",itemType,conn);
			yellowCol = getAttributeColumn("YELLOW",itemType,conn);
			otherCol = getAttributeColumn("OTHER",itemType,conn);
			totalQtyCol = getAttributeColumn("Total Qty",itemType,conn);
			if (currentColumn.equalsIgnoreCase(blueCol) || currentColumn.equalsIgnoreCase(yellowCol) || currentColumn.equalsIgnoreCase(otherCol))
			{
				blueQty = getColumnValue(blueCol.toLowerCase(),dom,"2");
				yellowQty = getColumnValue(yellowCol.toLowerCase(),dom,"2");
				otherQty = getColumnValue(otherCol.toLowerCase(),dom,"2");
				totalQty = getColumnValue(totalQtyCol.toLowerCase(),dom,"2");
				qtyPerPacket = Math.floor((new Double(25/theoriticalWgtprPiece)).doubleValue());
				
				System.out.println("theoriticalWgtprPiece In Set Total Qty :: "+theoriticalWgtprPiece);
				System.out.println("qtyPerPacket :: "+qtyPerPacket);
				
				if (blueQty != null && blueQty.trim().length() > 0)
				{
					if (blueQty.endsWith("K"))
					{
						blueQty = blueQty.substring(0,blueQty.length() - 1);
						blueQuantity = Integer.parseInt(blueQty);
						blueQuantity = blueQuantity * new Double(qtyPerPacket).intValue();
						resultString.append("<"+blueCol.toLowerCase()+">"+blueQuantity+"</"+blueCol.toLowerCase()+">");
					}
					else
					{
						blueQuantity = Integer.parseInt(blueQty);
					}					
				}
				if (yellowQty != null && yellowQty.trim().length() > 0)
				{
					if(yellowQty.endsWith("K"))
					{
						yellowQty = yellowQty.substring(0,yellowQty.length() - 1);
						yellowQuantity = Integer.parseInt(yellowQty);
						yellowQuantity = yellowQuantity * new Double(qtyPerPacket).intValue();
						resultString.append("<"+yellowCol.toLowerCase()+">"+yellowQuantity+"</"+yellowCol.toLowerCase()+">");
					}
					else
					{
						yellowQuantity = Integer.parseInt(yellowQty);
					}
					
					
				}
				if (otherQty != null && otherQty.trim().length() > 0)
				{
					if(otherQty.endsWith("K"))
					{
						otherQty = otherQty.substring(0,otherQty.length() - 1);
						otherQuantity = Integer.parseInt(otherQty);
						otherQuantity = otherQuantity * new Double(qtyPerPacket).intValue();
						resultString.append("<"+otherCol.toLowerCase()+">"+otherQuantity+"</"+otherCol.toLowerCase()+">");
					}
					else 
					{
						otherQuantity = Integer.parseInt(otherQty);
					}					
				}
				if( (currentColumn.equalsIgnoreCase(otherCol)) && ((totalQty == null) || (totalQty.trim().length() == 0) || Integer.parseInt(totalQty) == 0))
				{
					totalQuantity  = blueQuantity+yellowQuantity+otherQuantity;
					resultString.append("<"+totalQtyCol.toLowerCase()+">"+totalQuantity+"</"+totalQtyCol.toLowerCase()+">");
					resultString.append("<no_art>").append(totalQuantity / new Double(qtyPerPacket).intValue()).append("</no_art>\r\n");
					resultString.append("<quantity>").append(totalQuantity).append("</quantity>\r\n");
					
					String unit = "",unitstd = "",quan = "",convqty="";
					double convStd=0,quantity1 = 0;
					unit = getColumnValue("unit",dom,"2"); 
					if (unit == null)
					{
						unit = "";
					}
					unitstd	= getColumnValue("unit__std",dom,"2");
					if (unitstd == null)
					{
						unitstd = "";
					}
					//quan  =  getColumnValue("quantity",dom,"2");
					convqty	= getColumnValue("conv__qty_stduom",dom,"2"); 
					
					ArrayList convValue = new ArrayList();
					quantity1 = new Double(totalQuantity).doubleValue();
					convStd = Double.parseDouble(convqty); 
					
					convValue = disCommon.convQtyFactor(unit,unitstd,"",quantity1,convStd,conn);
					resultString.append("<conv__qty_stduom>").append(convValue.get(0).toString()).append("</conv__qty_stduom>\r\n");
					resultString.append("<quantity__stduom>").append(convValue.get(1).toString()).append("</quantity__stduom>\r\n");
				
					
				}	
				
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception In SetTotal Quantity "+e.getMessage());
			e.printStackTrace();
		}
		return resultString.toString();
	}	
	// End Jiten 
} 