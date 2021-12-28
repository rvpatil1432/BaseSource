/********************************************************
	Title : SalesContractIC
	Date  : 03/05/2012
	Developer: Mahesh Patidar
 ********************************************************/

package ibase.webitm.ejb.dis; 

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class SalesContractIC extends ValidatorEJB implements SalesContractICLocal, SalesContractICRemote
{
	//changed by nasruddin 07-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		int qty = 0;
		int intQty = 0;
		int qtyPer = 0;
		int minQty = 0;
		int currentFormNo = 0;
		int advPerc = 0;
		int childNodeListLength;
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String qtyStr = "";
		String itemCode = "";
		String unit = "";
		String unitStd = "";
		String unitRate = "";
		String dlvTerm = "";
		String errCode = "";
		String quotNo = "";
		String advPercStr = "";
		String siteCode = "";
		String transMode = "";
		String taxClass = "";
		String distRoute = "";
		String stanCode = "";
		String salesPers1 = "";
		String salesPers2 = "";
		String itemSer = "";
		String countCodeDlv = "";
		String siteCodeShip = "";
		String custCode = "";
		String custCodeBil = "";
		String custCodeDlv = "";
		String errFldName = "";
		String userId = "";
		String taxChap = "";
		String packCode = "";
		String taxEnv = "";
		String sql = "";
		String empCodeCon = "";
		String tranCode = "";
		String salesPers = "";
		String currCodeComm = "";
		String currCode = "";
		String currCodeFrt = "";
		String crTerm = "";
		String priceList = "";
		String priceListClg = "";
		String stateCode = "";
		String projCode = "";
		int cnt = 0;
		Timestamp date1 = null;
		Timestamp date2 = null;
		Timestamp date3 = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("eff_from")) 
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_from", dom)==null?getCurrdateInAppFormat():genericUtility.getColumnValue("eff_from", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("valid_upto", dom)==null?getCurrdateInAppFormat():genericUtility.getColumnValue("valid_upto", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date3 = Timestamp.valueOf(genericUtility.getValidDateString( getCurrdateInAppFormat() , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(date1 == null)
							{
								errCode = "VFTDT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(date1.after(date2))
							{
								errCode = "VEFFDTVLER";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(date1.before(date3))
							{
								errCode = "VEFFDTTD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						else if(childNodeName.equalsIgnoreCase("valid_upto")) 
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("valid_upto", dom)==null?getCurrdateInAppFormat():genericUtility.getColumnValue("valid_upto", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_from", dom)==null?getCurrdateInAppFormat():genericUtility.getColumnValue("eff_from", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date3 = Timestamp.valueOf(genericUtility.getValidDateString( getCurrdateInAppFormat() , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(date1 == null)
							{
								errCode = "VMVAL_UPT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(date1.before(date2))
							{
								errCode = "VEFFDTVLER";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(date1.before(date3))
							{
								errCode = "VVDTERR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						else if(childNodeName.equalsIgnoreCase("state_code__dlv")) 
						{
							stateCode = genericUtility.getColumnValue("state_code__dlv", dom);
							sql = "select COUNT(*) from STATE where STATE_CODE = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, stateCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "VTSTATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

						else if(childNodeName.equalsIgnoreCase("contract_type")) 
						{
							String contractType = genericUtility.getColumnValue("contract_type", dom);
							sql = "select count(*) from customer where order_type= ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, contractType);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "ERR_PCONT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("contract_date")) 
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("contract_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							sql = "select stat_sal from period where fr_date <= ? and to_date >= ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, date1);
							pstmt.setTimestamp(2, date1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								String statSal = rs.getString(1);
								if(!statSal.equals("Y"))
								{
									errCode = "VTPRDSAL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else
							{
								errCode = "VTSAL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("cust_code")) 
						{
							custCode = genericUtility.getColumnValue("cust_code", dom);
							sql = "Select Count(*) from customer where cust_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt==0)
								{
									errCode = "VTCUSTCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("cust_code__bil")) 
						{
							custCodeBil = genericUtility.getColumnValue("cust_code__bil", dom);
							sql = "Select Count(*) from customer where cust_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCodeBil);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt==0)
								{
									errCode = "VTCUSTCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("cust_code__dlv")) 
						{
							custCodeDlv = genericUtility.getColumnValue("cust_code__dlv", dom);
							sql = "Select Count(*) from customer where cust_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCodeDlv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt==0)
								{
									errCode = "VTCUSTCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("site_code")) 
						{
							siteCode = genericUtility.getColumnValue("site_code", dom);
							if(siteCode != null && siteCode.trim().length() > 0)
							{
								sql = " SELECT COUNT(*) FROM site WHERE site_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt =  rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VMSITE1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("site_code__ship")) 
						{
							siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom);
							if(siteCodeShip != null && siteCodeShip.trim().length() > 0)
							{
								sql = "Select Count(*) from site where site_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCodeShip);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt =  rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTSITECD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							
						}

						else if(childNodeName.equalsIgnoreCase("stan_code")) 
						{
							stanCode = genericUtility.getColumnValue("stan_code", dom);
							sql = "Select Count(*) from station where stan_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTSTAN1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("count_code__dlv")) 
						{
							countCodeDlv = genericUtility.getColumnValue("count_code__dlv", dom);
							sql = "Select Count(*) from country where count_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, countCodeDlv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTCONTCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("item_ser")) 
						{
							itemSer = genericUtility.getColumnValue("item_ser", dom);
							if(itemSer != null && itemSer.trim().length() > 0 )
							{
								sql = "Select Count(*) from itemser where item_ser = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VTITEMSER1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("price_list__disc")) 
						{
							priceList = genericUtility.getColumnValue("price_list__disc", dom);
							if(priceList != null && priceList.trim().length() != 0)
							{
								sql = "Select Count(*) from pricelist where price_list = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VTPLIST3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("price_list")) 
						{
							priceList = genericUtility.getColumnValue("price_list", dom);
							if(priceList != null && priceList.trim().length() != 0)
							{
								sql = "Select Count(*) from pricelist where price_list = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VTPLIST1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("price_list__clg")) 
						{
							priceListClg = genericUtility.getColumnValue("price_list__clg", dom);
							if(priceListClg != null && priceListClg.trim().length() != 0)
							{
								sql = "Select Count(*) from pricelist where PRICE_LIST = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, priceListClg);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VTPLIST2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("trans_mode")) 
						{
							transMode = genericUtility.getColumnValue("trans_mode", dom);
							if(transMode == null || transMode.trim().length() == 0 )
							{
								errCode = "VTITMOD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						else if(childNodeName.equalsIgnoreCase("dist_route")) 
						{
							distRoute = genericUtility.getColumnValue("dist_route", dom);
							String stanCode1 = genericUtility.getColumnValue("stan_code", dom);
							if(distRoute != null && distRoute.trim().length() > 0)
							{
								sql = "Select Count(*) from distroute where dist_route = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, distRoute);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VTDISTRT1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else if(cnt == 1 )
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "Select stan_code__to from distroute where dist_route = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, distRoute);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											stanCode = rs.getString(1);
											if(!(stanCode.trim().equals(stanCode1.trim())))
											{
												errCode = "VMSTANMIS1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("sales_pers__1")) 
						{
							salesPers1 = genericUtility.getColumnValue("sales_pers__1", dom);
							if(salesPers1 != null && salesPers1.trim().length() != 0)
							{
								sql = "Select Count(*) from sales_pers where sales_pers = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, salesPers1);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VMSLPERS1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("sales_pers__2")) 
						{
							salesPers2 = genericUtility.getColumnValue("sales_pers__2", dom);
							if(salesPers2 !=null && salesPers2.trim().length() != 0)
							{
								sql = "Select Count(*) from sales_pers where sales_pers = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, salesPers2);
								rs = pstmt.executeQuery();
								if(rs.next())
								{ 
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										errCode = "VMSLPERS1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("adv_perc")) 
						{
							advPercStr = genericUtility.getColumnValue("adv_perc", dom);
							try 
							{
								advPerc = Integer.parseInt(advPercStr);
							} 
							catch (Exception e) 
							{
								System.out.println("Error line 561");
							}
							if(advPerc > 100)
							{
								errCode = "VMADVPERC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						else if(childNodeName.equalsIgnoreCase("comm_perc__on")) 
						{
							advPercStr = genericUtility.getColumnValue("comm_perc", dom);
							try 
							{
								advPerc = Integer.parseInt(advPercStr);
							} 
							catch (Exception e) 
							{
								System.out.println("Error line 584");
							}
							String commPercOn = genericUtility.getColumnValue("comm_perc__on", dom);
							if(advPerc != 0 && (commPercOn == null || commPercOn.trim().length() == 0 ))
							{
								errCode = "VMCOMMON1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						else if(childNodeName.equalsIgnoreCase("tax_class")) 
						{
							taxClass = genericUtility.getColumnValue("tax_class", dom);
							if(taxClass != null && taxClass.trim().length() != 0 )
							{
								sql = "Select Count(*) from taxclass where tax_class = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxClass);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCLA1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("tax_chap")) 
						{
							taxChap = genericUtility.getColumnValue("tax_chap", dom);
							if(taxChap != null && taxChap.trim().length() != 0 )
							{
								sql = "Select Count(*) from taxchap where tax_chap = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxChap);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCHAP1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("tax_env")) 
						{
							taxEnv = genericUtility.getColumnValue("tax_env", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("contract_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if(taxEnv != null && taxEnv.trim().length() != 0 )
							{
								sql = "Select Count(*) from taxenv where tax_env = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxEnv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXENV1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select (case when status is null then 'A' else status end)" +
												" from taxenv where tax_env = ? and status_date <= ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, taxEnv);
										pstmt.setTimestamp(2, date1);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											String status = rs.getString(1);
											if(status.equals("C"))
											{
												errCode = "VTTAXENV1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("cr_term")) 
						{
							crTerm = genericUtility.getColumnValue("cr_term", dom);
							sql = "Select Count(*) from crterm where cr_term = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTCRTERM1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("curr_code__ins")) 
						{
							currCode = genericUtility.getColumnValue("curr_code__ins", dom);
							sql = "Select Count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTCURRCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("curr_code")) 
						{
							currCode = genericUtility.getColumnValue("curr_code", dom);
							sql = "Select Count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTCURRCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("curr_code__frt")) 
						{
							currCodeFrt = genericUtility.getColumnValue("curr_code__frt", dom);
							sql = "Select Count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, currCodeFrt);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTCURRCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("curr_code__comm")) 
						{
							currCodeComm = genericUtility.getColumnValue("curr_code__comm", dom);
							if(currCodeComm != null && currCodeComm.trim().length() != 0 )
							{
								sql = "Select Count(*) from currency where curr_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, currCodeComm);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("curr_code__comm_1")) 
						{
							currCodeComm = genericUtility.getColumnValue("curr_code__comm_1", dom);
							if(currCodeComm != null && currCodeComm.trim().length() != 0 )
							{
								sql = "Select Count(*) from currency where curr_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, currCodeComm);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("curr_code__comm_2")) 
						{
							currCodeComm = genericUtility.getColumnValue("curr_code__comm_2", dom);
							if(currCodeComm != null && currCodeComm.trim().length() != 0 )
							{
								sql = "Select Count(*) from currency where curr_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, currCodeComm);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("sales_pers")) 
						{
							salesPers = genericUtility.getColumnValue("sales_pers", dom);
							if(salesPers != null && salesPers.trim().length() != 0 )
							{
								sql = "Select Count(*) from sales_pers where sales_pers = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, salesPers);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VMSLPERS1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("tran_code")) 
						{
							tranCode = genericUtility.getColumnValue("tran_code", dom);
							sql = "Select Count(*) from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VMTRAN1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("quot_no")) 
						{
							quotNo = genericUtility.getColumnValue("quot_no", dom);
							if(quotNo != null && quotNo.trim().length() > 0 )
							{
								sql = "Select cust_code from sales_quot where quot_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, quotNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									String custCode1 = rs.getString(1).trim();
									custCode = genericUtility.getColumnValue("cust_code", dom).trim();
									if(! custCode.equals(custCode1))
									{
										errCode = "VTQUOT2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VTQUOT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("proj_code")) 
						{
							projCode = genericUtility.getColumnValue("proj_code", dom);
							if(projCode != null && projCode.trim().length() > 0 )
							{
								sql = "Select Count(*) from project where proj_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTPROJCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("dlv_term")) 
						{
							dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
							if(dlvTerm != null && dlvTerm.trim().length() > 0 )
							{
								sql = "Select Count(*) from delivery_term where dlv_term = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, dlvTerm);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VMDLVTERM1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if(childNodeName.equalsIgnoreCase("acct_code__sal")) 
						{
							String acctCodeSal = genericUtility.getColumnValue("acct_code__sal", dom);
							sql = "Select Count(*) from accounts where acct_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeSal);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VMACTCDMT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("cctr_code__sal")) 
						{
							String cctrCodeSal = genericUtility.getColumnValue("cctr_code__sal", dom);
							sql = "Select Count(*) from accounts where acct_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, cctrCodeSal);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VMACTCDMT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("emp_code__con")) 
						{
							empCodeCon = genericUtility.getColumnValue("emp_code__con", dom);
							sql = "Select Count(*) from employee where emp_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, empCodeCon);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VMEMPORD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					break;
					
				case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("site_code")) 
						{
							siteCode = genericUtility.getColumnValue("site_code", dom);
							sql = " Select Count(*) from site where site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						else if(childNodeName.equalsIgnoreCase("item_code")) 
						{
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "Select Count(*) from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTITEM1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = " Select item_ser from item where item_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										String itemSer1 = rs.getString(1);
										if(itemSer1.trim().equals("Y"))
										{
											itemSer = genericUtility.getColumnValue("Item_ser", dom);
											if(itemSer != null && itemSer.trim().length() > 0 && !(itemSer.trim().equals(itemSer1.trim())))
											{
												errCode = "VTITEM2";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(errList.size() == 0 )
							{
								date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_from", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
								date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("valid_upto", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
								custCode = genericUtility.getColumnValue("item_code", dom1);
								siteCode = genericUtility.getColumnValue("item_code", dom1);
								sql = " select count(*) from scontract a, scontractdet b " +
										"where a.contract_no = b.contract_no and a.site_code = ? " +
										" and a.cust_code = ? and b.item_code = ? and " +
										" (a.eff_from between ? and ? or a.valid_upto between ? and ? " +
										" or ? between a.eff_from and a.valid_upto or ? between a.eff_from and a.valid_upto) " +
										" and a.confirmed = 'Y'	and case when a.status is null then ' ' else a.status end <> 'X'";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, custCode);
								pstmt.setString(3, itemCode);
								pstmt.setTimestamp(4, date1);
								pstmt.setTimestamp(5, date2);
								pstmt.setTimestamp(6, date1);
								pstmt.setTimestamp(7, date2);
								pstmt.setTimestamp(8, date1);
								pstmt.setTimestamp(9, date2);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt > 0 )
									{
										errCode = "VCUSITMDUP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("unit")) 
						{
							unit = genericUtility.getColumnValue("unit", dom);
							unitStd = genericUtility.getColumnValue("unit__std", dom);
							sql = " Select Count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, unit);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTUNIT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(! unit.trim().equals(unitStd.trim()))
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = " select count(*) from uomconv where unit__fr = ? and unit__to = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, unit);
									pstmt.setString(2, unitStd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
										if(cnt == 0 )
										{
											errCode = "VTUNIT2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("rate")) 
						{
							int rate = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
							if(rate < 0)
							{
								errCode = "VTRATE2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						else if(childNodeName.equalsIgnoreCase("unit__rate")) 
						{
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							unitStd = genericUtility.getColumnValue("unit__std", dom);
							sql = "Select Count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, unitRate);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0 )
								{
									errCode = "VTUNIT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(! unitRate.trim().equals(unitStd.trim()))
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, unitRate);
									pstmt.setString(2, unitStd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
										if(cnt == 0 )
										{
											errCode = "VTUNIT2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										else
										{
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											sql = "select count(*) from uomconv where unit__fr = ? and unit__to = ?";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, unitStd);
											pstmt.setString(2, unitRate);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												cnt = rs.getInt(1);
												if(cnt == 0)
												{
													errCode = "VTUNIT3";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
										}
									}
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						else if(childNodeName.equalsIgnoreCase("tax_class")) 
						{
							taxClass = genericUtility.getColumnValue("tax_class", dom);
							if(taxClass != null && taxClass.trim().length() > 0 )
							{
								sql = "Select Count(*) from taxclass where tax_class = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxClass);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCLA1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("tax_chap")) 
						{
							taxChap = genericUtility.getColumnValue("tax_chap", dom);
							if(taxChap != null && taxChap.trim().length() > 0 )
							{
								sql = "Select Count(*) from taxchap where tax_chap = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxChap);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXCHAP1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("tax_env")) 
						{
							taxEnv = genericUtility.getColumnValue("tax_env", dom);
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("contract_date", dom)==null?getCurrdateInAppFormat():genericUtility.getColumnValue("contract_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if(taxEnv != null && taxEnv.trim().length() > 0 )
							{
								sql = "Select Count(*) from taxenv where tax_env = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, taxEnv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTTAXENV1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select (case when status is null then 'A' else status end)" +
												" from taxenv where tax_env = ? and status_date <= ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, taxEnv);
										pstmt.setTimestamp(1, date1);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											String status = rs.getString(1);
											if(status.equals("C"))
											{
												errCode = "VTTAXENV1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("pack_code")) 
						{
							packCode = genericUtility.getColumnValue("pack_code", dom);
							if(packCode != null && packCode.trim().length() > 0 )
							{
								sql = "Select Count(*) from packing where pack_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, packCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errCode = "VTPKCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if(childNodeName.equalsIgnoreCase("quantity")) 
						{
							qtyStr = genericUtility.getColumnValue("quantity", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								qty = Integer.parseInt(qtyStr.trim());
							}
							
							qtyStr = genericUtility.getColumnValue("rel_qty", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								qtyPer = Integer.parseInt(qtyStr.trim());
							}	
							
							qtyStr = genericUtility.getColumnValue("min_qty", dom);
							if(qtyStr != null && qtyStr.trim().length() > 0)
							{
								minQty = Integer.parseInt(qtyStr.trim());
							}							
							if(qty < qtyPer)
							{
								errCode = "VEQTY1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(qty < minQty)
							{
								errCode = "VTMINQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								itemCode = genericUtility.getColumnValue("item_code", dom);
								custCode = genericUtility.getColumnValue("cust_code", dom1);
								siteCode = genericUtility.getColumnValue("site_code", dom1);
								date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
								qtyStr = genericUtility.getColumnValue("quantity", dom);
								if(qtyStr != null && qtyStr.trim().length() > 0)
								{
									qty = Integer.parseInt(qtyStr.trim());
								}
								sql = "select count(*) from customeritem where  cust_code = ? and item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt > 0)
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select integral_qty, restrict_upto from customeritem " +
												" where cust_code = ? and item_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, custCode);
										pstmt.setString(2, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											intQty = rs.getInt(1);
											date3 = rs.getTimestamp(2);
											if(date3 != null)
											{
												if(! date3.before(date1))
												{
													errCode = "VTRESDT";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}	
											if(intQty > 0)
											{
												if(qty % intQty > 0)
												{
													errCode = "VTINTQTY";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
										}
									}
									else if(cnt == 0)
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "select integral_qty from siteitem where site_code = ? and item_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, custCode);
										pstmt.setString(2, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											intQty = rs.getInt(1);
										}
										if(intQty == 0)
										{
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											sql = "select integral_qty from item where item_code = ?";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, itemCode);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												intQty = rs.getInt(1);
											}
											if(intQty == 0)
											{
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												
												sql = "select count(*) from item where item_code = ?";
												pstmt =  conn.prepareStatement(sql);
												pstmt.setString(1, itemCode);
												rs = pstmt.executeQuery();
												if(rs.next())
												{
													cnt = rs.getInt(1);
													if(cnt == 0)
													{
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
														
														sql = "select batch_qty from bom where bom_code = ?";
														pstmt =  conn.prepareStatement(sql);
														pstmt.setString(1, itemCode);
														rs = pstmt.executeQuery();
														if(rs.next())
														{
															intQty = rs.getInt(1);
														}
													}
												}
											}
										}
										if(intQty > 0)
										{
											if(qty % intQty > 0)
											{
												errCode = "VTINTQTY1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}
					break;

				case 3:
					parentNodeList = dom.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("cust_code")) 
						{
							custCode = genericUtility.getColumnValue("cust_code", dom1);
							sql = "Select Count(1) from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									errCode = "VTCUSTCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					break;
			}
			int errListSize = errList.size();
			cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
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
				errStringXml = new StringBuffer("");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				/*if(conn != null)
				{
					conn.close();
				}			
				conn = null;*/
				//Added by sarita on 13-11-2017[start]
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ItemRegNo][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		int ctr = 0;
		int currentFormNo = 0;
		int num = 0;
		int num1 = 0;
		int num2 = 0;
		int num3 = 0;
		int quantity = 0 ;
		int rate = 0;
		int rateClg = 0;
		int pos = 0;
		int prdCdFr = 0;
		int prdCdTo = 0;
		int forecastQtn = 0;
		int despQtn = 0;
		int despValue = 0;
		int balQtn = 0;
		int nespRate = 0;
		int nrpRate = 0;
		int balValue = 0;
		int childNodeListLength = 0;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String siteCode = "";
		String reStr = "";
		String itemSer = "";
		String itemSerInv = "";
		String packCode = "";
		String itemCode = "";
		String contractNo = "";
		String validUpTostr = "";
		String effFromStr = "";
		String currDescr = "";
		String priceList = "";
		String custCode = "";
		String toStation = "";
		String fromStation = "";
		String plistDisc = "";
		String siteCodeShip = "";
		String contractDateStr = "";
		String priceListClg = "";
		String crTerm = "";
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";
		String salesPers = "";
		String salesPers1 = "";
		String salesPers2 = "";
		String pList = "";
		String pListDisc = "";
		String contractType = "";
		String sql = "";
		String currCodeFrt = "";
		String custName = "";
		String frState = "";
		String toState = "";
		String addr1 = "";
		String addr2 = "";
		String addr3 = "";
		String city = "";
		String pin = "";
		String countCode = "";
		String stanCode = "";
		String tranCode = "";
		String transMode = "";
		String stateCode = "";
		String tele1 = "";
		String tele2 = "";
		String tele3 = "";
		String fax = "";
		String dlvTerm = "";
		String currCode = "";
		String crDescr = "";
		String custCodeDlv = "";
		String descr = "";
		String descr1 = "";
		String curr = "";
		String uom = "";
		String unitStd = "";
		String unitRate = "";
		String type = "";
		String pack = "";
		String nrp = "";
		String acctPrd = "";
		Timestamp contractDate = null;
		String ldDate = null;
		Timestamp frDate = null;
		Timestamp toDate = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//changed by nasruddin 07-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
				case 1 : 
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail1>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						ldDate =  getCurrdateInAppFormat(); 
						valueXmlString.append("<emp_code__con >").append("<![CDATA[" +  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode") + "]]>").append("</emp_code__con>");
						valueXmlString.append("<status_date >").append("<![CDATA[" +  ldDate + "]]>").append("</status_date>");
						valueXmlString.append("<tax_date >").append("<![CDATA[" +  ldDate + "]]>").append("</tax_date>");
						valueXmlString.append("<contract_date >").append("<![CDATA[" +  ldDate + "]]>").append("</contract_date>");
						valueXmlString.append("<pl_date >").append("<![CDATA[" +  ldDate + "]]>").append("</pl_date>");
						valueXmlString.append("<eff_from >").append("<![CDATA[" +  ldDate + "]]>").append("</eff_from>");
						valueXmlString.append("<part_qty >").append("<![CDATA[Y]]>").append("</part_qty>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("site_code"))
					{
						siteCode = genericUtility.getColumnValue("site_code", dom);
						custCode = genericUtility.getColumnValue("cust_code", dom);
						sql = "select price_list__disc from site_customer where cust_code = ? and site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							plistDisc = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(plistDisc == null || plistDisc.trim().length() == 0)
						{
							sql = "select price_list__disc from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								plistDisc = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						valueXmlString.append("<price_list__disc >").append("<![CDATA[" +  plistDisc + "]]>").append("</price_list__disc>");
						sql = "select price_list from site_customer where cust_code = ? and site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceList = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(priceList == null || priceList.trim().length() == 0)
						{
							sql = "select price_list from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								priceList = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
						}
						valueXmlString.append("<price_list >").append("<![CDATA[" +  priceList + "]]>").append("</price_list>");
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom);
						if(siteCodeShip == null || !(siteCodeShip.trim().length() > 0))
						{
							valueXmlString.append("<site_code__ship >").append("<![CDATA[" +  siteCode + "]]>").append("</site_code__ship>");
						}
						
					}

					else if(currentColumn.trim().equalsIgnoreCase("contract_date"))
					{
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat());
						valueXmlString.append("<tax_date >").append("<![CDATA[" +  contractDateStr + "]]>").append("</tax_date>");
						valueXmlString.append("<pl_date >").append("<![CDATA[" +  contractDateStr + "]]>").append("</pl_date>");
						valueXmlString.append("<prom_date >").append("<![CDATA[" +  contractDateStr + "]]>").append("</prom_date>");
						valueXmlString.append("<due_date >").append("<![CDATA[" +  contractDateStr + "]]>").append("</due_date>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
					{
						custCode = genericUtility.getColumnValue("cust_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						sql = " select price_list__clg from site_customer where cust_code = ? and site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceListClg = rs.getString(1)==null?"":rs.getString(1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(priceListClg == null || !(priceListClg.trim().length() > 0))
							{
								sql = " select price_list__clg from customer where cust_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									priceListClg = rs.getString(1)==null?"":rs.getString(1);
									if(priceListClg == null || !(priceListClg.trim().length() > 0) || priceListClg.trim().equals("NULLFOUND"))
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, "999999");
										pstmt.setString(2, "PRICE_LIST__CLG");
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											priceListClg = rs.getString(1)==null?"":rs.getString(1);
										}
										else
										{
											priceListClg = "NULLFOUND";
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}
						}
						sql = " select  PRICE_LIST from site_customer where site_code = ? and cust_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceList = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select cr_term, tax_chap, tax_class, sales_pers, sales_pers__1,sales_pers__2, price_list," +
								" price_list__disc , ORDER_TYPE  from   customer where  cust_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							crTerm = rs.getString(1)==null?"":rs.getString(1);
							taxChap = rs.getString(2)==null?"":rs.getString(2);
							taxClass = rs.getString(3)==null?"":rs.getString(3);
							salesPers = rs.getString(4)==null?"":rs.getString(4);
							salesPers1 = rs.getString(5)==null?"":rs.getString(5);
							salesPers2 = rs.getString(6)==null?"":rs.getString(6);
							pList = rs.getString(7)==null?"":rs.getString(7);
							pListDisc = rs.getString(8)==null?"":rs.getString(8);
							contractType = rs.getString(9)==null?"":rs.getString(9);
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select descr from crterm where cr_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, crTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							crDescr = rs.getString(1)==null?"":rs.getString(1);							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(priceList == null || priceList.trim().length()==0)
						{
							priceList = pList;
						}
						valueXmlString.append("<cust_code__bil >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code__bil>");
						valueXmlString.append("<cr_term >").append("<![CDATA[" +  crTerm + "]]>").append("</cr_term>");
						valueXmlString.append("<crterm_descr >").append("<![CDATA[" +  crDescr + "]]>").append("</crterm_descr>");
						valueXmlString.append("<cust_code__dlv >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code__dlv>");
						valueXmlString.append("<tax_chap >").append("<![CDATA[" +  taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class >").append("<![CDATA[" +  taxClass + "]]>").append("</tax_class>");
						valueXmlString.append("<price_list >").append("<![CDATA[" +  priceList + "]]>").append("</price_list>");
						valueXmlString.append("<price_list__disc >").append("<![CDATA[" +  pListDisc + "]]>").append("</price_list__disc>");
						valueXmlString.append("<price_list__clg >").append("<![CDATA[" +  priceListClg + "]]>").append("</price_list__clg>");
						valueXmlString.append("<contract_type >").append("<![CDATA[" +  contractType + "]]>").append("</contract_type>");
						
						sql = "select	cust_name, addr1, addr2,addr3, city, pin, count_code, stan_code, " +
								"tran_code, trans_mode,state_code, tele1, tele2, tele3,fax ,curr_code" +
								" from customer where cust_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custName = rs.getString(1)==null?"":rs.getString(1);
							addr1 = rs.getString(2)==null?"":rs.getString(2);
							addr2 = rs.getString(3)==null?"":rs.getString(3);
							addr3 = rs.getString(4)==null?"":rs.getString(4);
							city = rs.getString(5)==null?"":rs.getString(5);
							pin = rs.getString(6)==null?"":rs.getString(6);
							countCode = rs.getString(7)==null?"":rs.getString(7);
							stanCode = rs.getString(8)==null?"":rs.getString(8);
							tranCode = rs.getString(9)==null?"":rs.getString(9);
							transMode = rs.getString(10)==null?"":rs.getString(10);
							stateCode = rs.getString(11)==null?"":rs.getString(11);
							tele1 = rs.getString(12)==null?"":rs.getString(12);
							tele2 = rs.getString(13)==null?"":rs.getString(13);
							tele3 = rs.getString(14)==null?"":rs.getString(14);
							fax = rs.getString(15)==null?"":rs.getString(15);
							curr = rs.getString(16)==null?"":rs.getString(16);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<cust_name >").append("<![CDATA[" +  custName + "]]>").append("</cust_name>");
						valueXmlString.append("<dlv_to >").append("<![CDATA[" +  custName + "]]>").append("</dlv_to>");
						valueXmlString.append("<dlv_add1 >").append("<![CDATA[" +  addr1 + "]]>").append("</dlv_add1>");
						valueXmlString.append("<dlv_add2 >").append("<![CDATA[" +  addr2 + "]]>").append("</dlv_add2>");
						valueXmlString.append("<dlv_add3 >").append("<![CDATA[" +  addr3 + "]]>").append("</dlv_add3>");
						valueXmlString.append("<dlv_city >").append("<![CDATA[" +  city + "]]>").append("</dlv_city>");
						valueXmlString.append("<dlv_pin >").append("<![CDATA[" +  pin + "]]>").append("</dlv_pin>");
						valueXmlString.append("<count_code__dlv >").append("<![CDATA[" +  countCode + "]]>").append("</count_code__dlv>");
						valueXmlString.append("<tran_code >").append("<![CDATA[" +  tranCode + "]]>").append("</tran_code>");
						valueXmlString.append("<trans_mode >").append("<![CDATA[" +  transMode + "]]>").append("</trans_mode>");
						valueXmlString.append("<stan_code >").append("<![CDATA[" +  stanCode + "]]>").append("</stan_code>");
						valueXmlString.append("<state_code__dlv >").append("<![CDATA[" +  stateCode.trim() + "]]>").append("</state_code__dlv>");
						valueXmlString.append("<tel1__dlv >").append("<![CDATA[" +  tele1 + "]]>").append("</tel1__dlv>");
						valueXmlString.append("<tel2__dlv >").append("<![CDATA[" +  tele2 + "]]>").append("</tel2__dlv>");
						valueXmlString.append("<tel3__dlv >").append("<![CDATA[" +  tele3 + "]]>").append("</tel3__dlv>");
						valueXmlString.append("<fax__dlv >").append("<![CDATA[" +  fax + "]]>").append("</fax__dlv>");
						valueXmlString.append("<curr_code >").append("<![CDATA[" +  curr + "]]>").append("</curr_code>");
						
						sql = "select descr,std_exrt from   currency where  curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, curr);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currDescr = rs.getString(1)==null?"":rs.getString(1); 
							num = rs.getInt(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate >").append("<![CDATA[" +  num + "]]>").append("</exch_rate>");
						valueXmlString.append("<currency_descr >").append("<![CDATA[" +  currDescr + "]]>").append("</currency_descr>");
						
						descr = "                                        ";
						descr1 = "                                        ";
						num = 0;
						
						sql = "select descr from station where  stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, stanCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<station_descr >").append("<![CDATA[" +  descr + "]]>").append("</station_descr>");
						
						descr = "                                        ";
						descr1 = "                                        ";
						num = 0;
						
						sql = "select tran_name, frt_term, curr_code from transporter where tran_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							descr1 = rs.getString(2)==null?"":rs.getString(2);
							curr = rs.getString(3)==null?"":rs.getString(3);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<tran_name >").append("<![CDATA[" +  descr + "]]>").append("</tran_name>");
						valueXmlString.append("<frt_term >").append("<![CDATA[" +  descr1.trim() + "]]>").append("</frt_term>");
						valueXmlString.append("<curr_code__frt >").append("<![CDATA[" +  curr + "]]>").append("</curr_code__frt>");
						valueXmlString.append("<curr_code__ins >").append("<![CDATA[" +  curr + "]]>").append("</curr_code__ins>");
						valueXmlString.append("<exch_rate__frt >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__frt>");
						valueXmlString.append("<exch_rate__ins >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__ins>");
						
						descr = "                                        ";
						descr1 = "                                        ";
						num = 0;
						currCode = "          ";
						
						sql = "select sp_name, comm_perc, comm_perc__on,curr_code from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							num = rs.getInt(2);
							descr1 = rs.getString(3)==null?"":rs.getString(3);
							currCode = rs.getString(4)==null?"":rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<sales_pers >").append("<![CDATA[" +  salesPers + "]]>").append("</sales_pers>");
						valueXmlString.append("<sp_name >").append("<![CDATA[" +  descr + "]]>").append("</sp_name>");
						valueXmlString.append("<comm_perc >").append("<![CDATA[" +  num + "]]>").append("</comm_perc>");
						valueXmlString.append("<curr_code__comm >").append("<![CDATA[" +  currCode + "]]>").append("</curr_code__comm>");
						
						if(descr1 != null)
						{
							valueXmlString.append("<comm_perc__on >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc__on>");
						}
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate__comm >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm>");
						
						descr = "                                        ";
						descr1 = "                                        ";
						num = 0;
						currCode = "          ";

						sql = "select sp_name, comm_perc, comm_perc__on ,curr_code from  sales_pers	where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							num = rs.getInt(2);
							descr1 = rs.getString(3)==null?"":rs.getString(3);
							currCode = rs.getString(4)==null?"":rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<sales_pers__1 >").append("<![CDATA[" +  salesPers1 + "]]>").append("</sales_pers__1>");
						valueXmlString.append("<sp_name_1 >").append("<![CDATA[" +  descr + "]]>").append("</sp_name_1>");
						valueXmlString.append("<comm_perc_1 >").append("<![CDATA[" +  num + "]]>").append("</comm_perc_1>");
						valueXmlString.append("<curr_code__comm_1 >").append("<![CDATA[" +  currCode + "]]>").append("</curr_code__comm_1>");
						
						if(descr1 != null)
						{
							valueXmlString.append("<comm_perc_on_1 >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc_on_1>");
						}
						
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate__comm_1 >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm_1>");
						
						descr = "                                        ";
						descr1 = "                                        ";
						num = 0;
						currCode = "          ";

						sql = "select sp_name, comm_perc, comm_perc__on ,curr_code from  sales_pers	where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers2);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							num = rs.getInt(2);
							descr1 = rs.getString(3)==null?"":rs.getString(3);
							currCode = rs.getString(4)==null?"":rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<sales_pers__2 >").append("<![CDATA[" +  salesPers2 + "]]>").append("</sales_pers__2>");
						valueXmlString.append("<sp_name_2 >").append("<![CDATA[" +  descr + "]]>").append("</sp_name_2>");
						valueXmlString.append("<comm_perc_2 >").append("<![CDATA[" +  num + "]]>").append("</comm_perc_2>");
						valueXmlString.append("<curr_code__comm_2 >").append("<![CDATA[" +  currCode + "]]>").append("</curr_code__comm_2>");
						
						if(descr1 != null)
						{
							valueXmlString.append("<comm_perc_on_2 >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc_on_2>");
						}
						
						siteCode = genericUtility.getColumnValue("site_code", dom);
						itemSer = genericUtility.getColumnValue("item_ser", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate__comm_2 >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm_2>");
						

						sql = "select dlv_term from customer_series where cust_code = ? and item_ser = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemSer);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							dlvTerm = rs.getString(1)==null?"":rs.getString(1);
						} 
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(dlvTerm == null || dlvTerm.trim().length() == 0)
						{
							sql = "select dlv_term from customer where  cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								dlvTerm = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						if(dlvTerm == null || dlvTerm.trim().length() == 0)
						{
							dlvTerm = "NA";
						}
						valueXmlString.append("<dlv_term >").append("<![CDATA[" +  dlvTerm + "]]>").append("</dlv_term>");
						
					}

					else if(currentColumn.trim().equalsIgnoreCase("cust_code__dlv"))
					{
						custCodeDlv = genericUtility.getColumnValue("cust_code__dlv", dom);
						sql = "select	cust_name, addr1, addr2,addr3, city, pin, count_code, stan_code," +
								"tran_code, state_code, tele1, tele2, tele3,fax" +
								" from customer where cust_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCodeDlv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custName = rs.getString(1)==null?"":rs.getString(1);
							addr1 = rs.getString(2)==null?"":rs.getString(2);
							addr2 = rs.getString(3)==null?"":rs.getString(3);
							addr3 = rs.getString(4)==null?"":rs.getString(4);
							city = rs.getString(5)==null?"":rs.getString(5);
							pin = rs.getString(6)==null?"":rs.getString(6);
							countCode = rs.getString(7)==null?"":rs.getString(7);
							stanCode = rs.getString(8)==null?"":rs.getString(8);
							tranCode = rs.getString(9)==null?"":rs.getString(9);
							stateCode = rs.getString(10)==null?"":rs.getString(10);
							tele1 = rs.getString(11)==null?"":rs.getString(11);
							tele2 = rs.getString(12)==null?"":rs.getString(12);
							tele3 = rs.getString(13)==null?"":rs.getString(13);
							fax = rs.getString(14)==null?"":rs.getString(14);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append("<dlv_to >").append("<![CDATA[" +  custName + "]]>").append("</dlv_to>");
						valueXmlString.append("<dlv_add1 >").append("<![CDATA[" +  addr1 + "]]>").append("</dlv_add1>");
						valueXmlString.append("<dlv_add2 >").append("<![CDATA[" +  addr2 + "]]>").append("</dlv_add2>");
						valueXmlString.append("<dlv_add3 >").append("<![CDATA[" +  addr3.trim() + "]]>").append("</dlv_add3>");
						valueXmlString.append("<dlv_city >").append("<![CDATA[" +  city + "]]>").append("</dlv_city>");
						valueXmlString.append("<dlv_pin >").append("<![CDATA[" +  pin + "]]>").append("</dlv_pin>");
						valueXmlString.append("<count_code__dlv >").append("<![CDATA[" +  countCode + "]]>").append("</count_code__dlv>");
						valueXmlString.append("<tran_code >").append("<![CDATA[" +  tranCode + "]]>").append("</tran_code>");
						valueXmlString.append("<stan_code >").append("<![CDATA[" +  stanCode + "]]>").append("</stan_code>");
						valueXmlString.append("<state_code__dlv >").append("<![CDATA[" +  stateCode.trim() + "]]>").append("</state_code__dlv>");
						valueXmlString.append("<tel1__dlv >").append("<![CDATA[" +  tele1.trim() + "]]>").append("</tel1__dlv>");
						valueXmlString.append("<tel2__dlv >").append("<![CDATA[" +  tele2.trim() + "]]>").append("</tel2__dlv>");
						valueXmlString.append("<tel3__dlv >").append("<![CDATA[" +  tele3.trim() + "]]>").append("</tel3__dlv>");
						valueXmlString.append("<fax__dlv >").append("<![CDATA[" +  fax.trim() + "]]>").append("</fax__dlv>");
						
						descr = "                                        ";
						sql = " select descr from station where stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, stanCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<station_descr >").append("<![CDATA[" +  descr + "]]>").append("</station_descr>");
						descr = "                                        ";
						descr1 = "                                        ";
						
						sql = " elect tran_name, frt_term from transporter where  tran_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							descr1 = rs.getString(2)==null?"":rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<tran_name >").append("<![CDATA[" +  descr + "]]>").append("</tran_name>");
						valueXmlString.append("<frt_term >").append("<![CDATA[" +  descr1.trim() + "]]>").append("</frt_term>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("cr_term"))
					{
						crTerm = genericUtility.getColumnValue("cr_term", dom);
						sql = " select descr from crterm where cr_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, crTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						valueXmlString.append("<crterm_descr >").append("<![CDATA[" +  descr + "]]>").append("</crterm_descr>");
					}

					else if(currentColumn.trim().equalsIgnoreCase("stan_code"))
					{
						stanCode = genericUtility.getColumnValue("stan_code", dom);
						sql = " select descr from station where stan_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, stanCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<station_descr >").append("<![CDATA[" +  descr + "]]>").append("</station_descr>");
						
						sql = "select a.state_code,b.city,a.count_code from state a,station b " +
								"where a.state_code = b.state_code and b.stan_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, stanCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							stateCode = rs.getString(1)==null?"":rs.getString(1);
							city = rs.getString(2)==null?"":rs.getString(2);
							countCode = rs.getString(3)==null?"":rs.getString(3);
						}		
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<state_code__dlv >").append("<![CDATA[" +  stateCode + "]]>").append("</state_code__dlv>");
						valueXmlString.append("<dlv_city >").append("<![CDATA[" +  city + "]]>").append("</dlv_city>");
						valueXmlString.append("<count_code__dlv >").append("<![CDATA[" +  countCode + "]]>").append("</count_code__dlv>");
					}

					else if(currentColumn.trim().equalsIgnoreCase("sales_pers"))
					{
						salesPers = genericUtility.getColumnValue("sales_pers", dom);
						sql = "select sp_name,curr_code,comm_perc, comm_perc__on from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							currCode = rs.getString(2)==null?"":rs.getString(2);
							num = rs.getInt(3);
							descr1 = rs.getString(4)==null?"":rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<sp_name >").append("<![CDATA[" +  descr + "]]>").append("</sp_name>");
						valueXmlString.append("<curr_code__comm >").append("<![CDATA[" +  currCode.trim() + "]]>").append("</curr_code__comm>");
						valueXmlString.append("<comm_perc >").append("<![CDATA[" +  num + "]]>").append("</comm_perc>");
						if(descr1 != null)
						{
							valueXmlString.append("<comm_perc__on >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc__on>");
						}
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate__comm >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm>");
						
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("sales_pers__1"))
					{
						salesPers1 = genericUtility.getColumnValue("sales_pers__1", dom);
						sql = "select sp_name,curr_code,comm_perc, comm_perc__on from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers1);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							currCode = rs.getString(2)==null?"":rs.getString(2);
							num = rs.getInt(3);
							descr1 = rs.getString(4)==null?"":rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<sp_name_1 >").append("<![CDATA[" +  descr + "]]>").append("</sp_name_1>");
						valueXmlString.append("<curr_code__comm_1 >").append("<![CDATA[" +  currCode.trim() + "]]>").append("</curr_code__comm_1>");
						valueXmlString.append("<comm_perc_1 >").append("<![CDATA[" +  num + "]]>").append("</comm_perc_1>");
						valueXmlString.append("<comm_perc_on_1 >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc_on_1>");
						
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate__comm_1 >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm_1>");
						
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("sales_pers__2"))
					{
						salesPers2 = genericUtility.getColumnValue("sales_pers__2", dom);
						sql = "select sp_name,curr_code,comm_perc, comm_perc__on from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, salesPers2);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							currCode = rs.getString(2)==null?"":rs.getString(2);
							num = rs.getInt(3);
							descr1 = rs.getString(4)==null?"":rs.getString(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<sp_name_2 >").append("<![CDATA[" +  descr + "]]>").append("</sp_name_2>");
						valueXmlString.append("<curr_code__comm_2 >").append("<![CDATA[" +  currCode.trim() + "]]>").append("</curr_code__comm_2>");
						valueXmlString.append("<comm_perc_2 >").append("<![CDATA[" +  num + "]]>").append("</comm_perc_2>");
						valueXmlString.append("<comm_perc_on_2 >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc_on_2>");
						
						siteCode = genericUtility.getColumnValue("site_code", dom);
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
						valueXmlString.append("<exch_rate__comm_2 >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm_2>");
						
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("curr_code__frt"))
					{
						currCodeFrt = genericUtility.getColumnValue("curr_code__frt", dom);
						sql = "select std_exrt from currency where curr_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, currCodeFrt);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							num = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<exch_rate__frt >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__frt>");
					}

					else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
					{
						currCode = genericUtility.getColumnValue("curr_code", dom);
						sql = "select std_exrt from currency where curr_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, currCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							num = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<exch_rate >").append("<![CDATA[" +  num + "]]>").append("</exch_rate>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
					{
						tranCode = genericUtility.getColumnValue("tran_code", dom);
						sql = "select tran_name from transporter where tran_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<tran_name >").append("<![CDATA[" +  descr + "]]>").append("</tran_name>");
					}
					valueXmlString.append("</Detail1>");
					break;  

				case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail2>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						contractNo = genericUtility.getColumnValue("contract_no", dom1);
						valueXmlString.append("<contract_no >").append("<![CDATA[" +  contractNo + "]]>").append("</contract_no>");
						siteCode = genericUtility.getColumnValue("site_code", dom1); 
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
						if(siteCodeShip == null || siteCodeShip.trim().length() == 0)
						{
							siteCodeShip = siteCode;
						}
						valueXmlString.append("<site_code >").append("<![CDATA[" +  siteCodeShip + "]]>").append("</site_code>");
						valueXmlString.append("<status_date >").append("<![CDATA[" + getCurrdateInAppFormat() + "]]>").append("</status_date>");
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat());
						valueXmlString.append("<dsp_date >").append("<![CDATA[" +  contractDateStr + "]]>").append("</dsp_date>");
						
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						valueXmlString.append("<valid_upto >").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");

						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						valueXmlString.append("<eff_from >").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
										
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("item_code"))
					{
						itemCode = genericUtility.getColumnValue("item_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
						if(siteCodeShip == null || siteCodeShip.trim().length() == 0)
						{
							siteCodeShip = siteCode;
						}
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						custCode = genericUtility.getColumnValue("cust_code", dom1);
						toStation = genericUtility.getColumnValue("stan_code", dom1);
						
						sql = "select item_ser from siteitem where site_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							itemSer = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(itemSer ==null || itemSer.trim().length() == 0)
						{
							sql = "select item_ser from itemser_change" +
									" where item_code = ? and eff_date <= ? and " +
									" (valid_upto >= ? or valid_upto is null)";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setTimestamp(2, contractDate);
							pstmt.setTimestamp(3, contractDate);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(itemSer ==null || itemSer.trim().length() == 0)
						{
							sql = "select item_ser__old ser from itemser_change where item_code = ? " +
									" and eff_date >= ? and (valid_upto >= ? or valid_upto is null) " +
									" and eff_date = (select min(eff_date) from itemser_change where item_code = ?)";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setTimestamp(2, contractDate);
							pstmt.setTimestamp(3, contractDate);
							pstmt.setString(4, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(itemSer ==null || itemSer.trim().length() == 0)
						{
							sql = "select item_ser from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
						sql = "select item_ser__inv from customer_series where cust_code = ? and item_ser = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemSer);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							itemSerInv = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(itemSerInv != null && itemSerInv.trim().length() > 0)
						{
							itemSer = itemSerInv;
						}
						
						sql = "Select descr, unit, item_stru,pack_instr from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							uom = rs.getString(2)==null?"":rs.getString(2);
							type = rs.getString(3)==null?"":rs.getString(3);
							pack = rs.getString(4)==null?"":rs.getString(4);
							valueXmlString.append("<item_descr >").append("<![CDATA[" +  descr + "]]>").append("</item_descr>");
							valueXmlString.append("<unit >").append("<![CDATA[" +  uom + "]]>").append("</unit>");
							valueXmlString.append("<unit__std >").append("<![CDATA[" +  uom + "]]>").append("</unit__std>");
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							if(unitRate == null || unitRate.trim().length() == 0 )
							{
								valueXmlString.append("<unit__rate >").append("<![CDATA[" +  uom + "]]>").append("</unit__rate>");
							}
							if(type.equals("F"))
							{
								valueXmlString.append("<item_flg >").append("<![CDATA[F]]>").append("</item_flg>");	
							}
							else 
							{
								valueXmlString.append("<item_flg >").append("<![CDATA[I]]>").append("</item_flg>");
							}
							valueXmlString.append("<pack_instr >").append("<![CDATA[" +  pack + "]]>").append("</pack_instr>");
						}
						else 
						{
							valueXmlString.append("<item_descr >").append("<![CDATA[" +  descr + "]]>").append("</item_descr>");
							valueXmlString.append("<unit >").append("<![CDATA[" +  uom + "]]>").append("</unit>");
							valueXmlString.append("<unit__std >").append("<![CDATA[" +  uom + "]]>").append("</unit__std>");
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							if(unitRate == null || unitRate.trim().length() == 0 )
							{
								valueXmlString.append("<unit__rate >").append("<![CDATA[" +  uom + "]]>").append("</unit__rate>");
							}
							valueXmlString.append("<item_flg >").append("<![CDATA[B]]>").append("</item_flg>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "SELECT stan_code FROM site WHERE site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeShip);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							fromStation = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select tax_chap from customeritem where cust_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxChap = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from siteitem where site_code = ? and item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from itemser where item_ser = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						sql = "select tax_class from customeritem where cust_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxClass = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(taxClass == null || taxClass.trim().length() == 0 )
						{
							sql = "select tax_class from site_customer where site_code = ? and cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, custCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxClass = rs.getString(1)==null?"":rs.getString(1);
							}
							else
							{
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select tax_class from customer where cust_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									taxClass = rs.getString(1)==null?"":rs.getString(1);
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if((taxClass == null || taxClass.trim().length() == 0) && itemCode != null && itemCode.trim().length() > 0 )
						{
							sql = "select tax_class from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxClass = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class = ? and tax_chap = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, fromStation);
						pstmt.setString(2, toStation);
						pstmt.setString(3, taxClass);
						pstmt.setString(4, taxChap);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxEnv = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class	  = '     ' and tax_chap = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							pstmt.setString(2, toStation);
							pstmt.setString(3, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class = ? and tax_chap = '     '";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							pstmt.setString(2, toStation);
							pstmt.setString(3, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class = '     ' and tax_chap  = '     ' ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							pstmt.setString(2, toStation);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "select state_code from station where stan_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								frState = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "select state_code from station where stan_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, toStation);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								frState = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? and state_code__to = ? and stan_code__fr = '     ' and stan_code__to = '     ' and tax_class = ?  and tax_chap = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							pstmt.setString(3, taxClass);
							pstmt.setString(4, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? and " +
									" state_code__to = ? and stan_code__fr = '     ' and " +
									" stan_code__to = '     ' and tax_class = '     ' and tax_chap      = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							pstmt.setString(3, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? AND state_code__to = ? AND stan_code__fr = '     ' and " +
									" stan_code__to = '     ' and tax_class = ? and tax_chap = '          ' ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							pstmt.setString(3, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? and " +
									"state_code__to = ? and stan_code__fr = '     ' and " +
									" stan_code__to = '     ' and tax_class  = '     ' and tax_chap   = '          '";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						valueXmlString.append("<tax_chap >").append("<![CDATA[" +  taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class >").append("<![CDATA[" +  taxClass + "]]>").append("</tax_class>");
						valueXmlString.append("<tax_env >").append("<![CDATA[" +  taxEnv + "]]>").append("</tax_env>");
						
						priceList = genericUtility.getColumnValue("PRICE_LIST", dom1);
						priceListClg = genericUtility.getColumnValue("PRICE_LIST__CLG", dom1);
						String quantityStr = genericUtility.getColumnValue("QUANTITY", dom).trim();
						try
						{
							quantity = Integer.parseInt(quantityStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 1160 error");
						}
						
						if(priceList != null && priceList.trim().length() > 0 )
						{
							rate = getPickRate(priceList, contractDate,itemCode,"","L",quantity,conn);
							if(rate == -1 && getPriceListType(priceList,conn).equals("B"))
							{
								rate = 0;
							}
							valueXmlString.append("<rate >").append("<![CDATA[" +  rate + "]]>").append("</rate>");
						}
						if(priceListClg != null && priceListClg.trim().length() > 0)
						{
							rate = getPickRate(priceList, contractDate,itemCode,"","L",quantity,conn);
							if(rate == -1 && getPriceListType(priceList,conn).equals("B"))
							{
								rateClg = 0;
							}
							valueXmlString.append("<rate__clg >").append("<![CDATA[" +  rateClg + "]]>").append("</rate__clg>");
						}
						else
						{
							valueXmlString.append("<rate__clg >").append("<![CDATA[" +  rate + "]]>").append("</rate__clg>");
						}
						
						reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
						
						if(itemCode != null && itemCode.trim().length() > 0 )
						{
							contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							sql = "select fr_date, to_date, code from acctprd where fr_date <= ? and to_date >= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, contractDate);
							pstmt.setTimestamp(2, contractDate);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								frDate = rs.getTimestamp(1);
								toDate = rs.getTimestamp(2);
								acctPrd = rs.getString(3)==null?"":rs.getString(3);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "select min(code), max(code) from period where acct_prd = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, acctPrd);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								prdCdFr = rs.getInt(1);
								prdCdTo = rs.getInt(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "select sum(b.quantity) from salesforecast_hdr a, salesforecast_det b" +
									" where a.tran_id = b.tran_id and " +
									"(case when a.confirmed is null then 'N' else a.confirmed end) = 'Y' " +
									"and b.item_code = ? and	b.prd_code__for >= ? " +
									"and b.prd_code__for <= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setInt(2, prdCdFr);
							pstmt.setInt(3, prdCdTo);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								forecastQtn = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "select sum(b.quantity__stduom), sum(b.quantity__stduom * b.rate__stduom) " +
									"from despatch a, despatchdet b	where a.desp_id = b.desp_id" +
									" and (case when a.confirmed is null then 'N' else a.confirmed end) = 'Y' " +
									" and a.conf_date between ? and ?	and	b.item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, frDate);
							pstmt.setTimestamp(2, toDate);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								despQtn = rs.getInt(1);
								despValue = rs.getInt(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "select sum(b.bal_qty_stduom), sum(b.bal_qty_stduom * b.rate) " +
									"from scontract a, scontractdet b where a.contract_no = b.contract_no " +
									"and (case when a.confirmed is null then 'N' else a.confirmed end) = 'Y'" +
									" and a.conf_date between ? and ? and b.item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, frDate);
							pstmt.setTimestamp(2, toDate);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								balQtn = rs.getInt(1);
								balValue = rs.getInt(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							int balForecastQtn = forecastQtn - balQtn - despQtn;
							sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, "999999");
							pstmt.setString(2, "NRP");
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								nrp = rs.getString(1)==null?"":rs.getString(1);
							}
							else
							{
								nrp = "NULLFOUND";
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(nrp.equals("NULLFOUND") || nrp.trim().length()== 0)
							{
								nrpRate = 0;
							}
							else
							{
								priceList = genericUtility.getColumnValue("PRICE_LIST", dom1);
								nrpRate = getPickRate(priceList,nrp,contractDate,itemCode,"","L",conn);
								if(nrpRate < 0)
								{
									nrpRate = 0;
								}
							}
							int nespValue = despValue + balValue +( balForecastQtn * nrpRate );
							int nespQtn =  despQtn + balQtn + balForecastQtn;
							if(nespQtn > 0 && nespValue > 0)
							{
								nespRate = nespValue / nespQtn;
							}
							else
							{
								nespRate = 0;
							}
						}
						else
						{
							nespRate = 0;
						}
						valueXmlString.append("<rate__nesp >").append("<![CDATA[" +  nespRate + "]]>").append("</rate__nesp>");
					}

					else if(currentColumn.trim().equalsIgnoreCase("quantity"))
					{
						try
						{
							quantity = Integer.parseInt(genericUtility.getColumnValue("quantity", dom).trim());
							valueXmlString.append("<bal_qty_stduom >").append("<![CDATA[" +  quantity + "]]>").append("</bal_qty_stduom>");
							uom = genericUtility.getColumnValue("unit", dom);
							unitStd = genericUtility.getColumnValue("unit__std", dom);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							try
							{
								num1 = Integer.parseInt(genericUtility.getColumnValue("conv__qty_stduom", dom).trim());
							}
							catch(Exception e2)
							{
								num1 = 0;
								System.out.println("NUMBER FORMAT EXP"+e2);
							}
							num2 = num1;
							priceList = genericUtility.getColumnValue("PRICE_LIST", dom1);
							priceListClg = genericUtility.getColumnValue("PRICE_LIST__CLG", dom1);
							contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							if(uom == null  || uom.trim().length() == 0)
							{
								sql = "Select unit from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									uom = rs.getString(1)==null?"":rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								num = getConvQuantityFact(uom,unitStd,itemCode,quantity,num1,conn);
								valueXmlString.append("<unit >").append("<![CDATA[" +  uom + "]]>").append("</unit>");
							}
							else
							{
								num = getConvQuantityFact(uom,unitStd,itemCode,quantity,num1,conn);
							}
							if(num2 == 0)
							{
								valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" +  num1 + "]]>").append("</conv__qty_stduom>");
							}
							valueXmlString.append("<quantity__stduom >").append("<![CDATA[" +  num + "]]>").append("</quantity__stduom>");
							if(priceList != null && priceList.trim().length() > 0)
							{
								rate = getPickRate(priceList, contractDate,itemCode,"","L",quantity,conn);
								if(rate == -1 && getPriceListType(priceList,conn).equals("B"))
								{
									rate = 0;
								}
								valueXmlString.append("<rate >").append("<![CDATA[" +  rate + "]]>").append("</rate>");
							}
							if(priceListClg != null && priceListClg.trim().length() > 0)
							{
								rate = getPickRate(priceList, contractDate,itemCode,"","L",quantity,conn);
								if(rate == -1 && getPriceListType(priceList,conn).equals("B"))
								{
									rate = 0;
								}
								valueXmlString.append("<rate__clg >").append("<![CDATA[" +  rateClg + "]]>").append("</rate__clg>");
							}

							reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
						catch(Exception e1)
						{
							System.out.println("line no after 1322 error "+e1);
						}
					}
					 
					else if(currentColumn.trim().equalsIgnoreCase("unit"))
					{
						uom = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						try
						{
							num1 = Integer.parseInt(genericUtility.getColumnValue("quantity", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1407 linr"+e1);
						}
						num2 = 0;
						num = getConvQuantityFact(uom,unitStd,itemCode,quantity,num1,conn);
						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" +  num2 + "]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<quantity__stduom >").append("<![CDATA[" +  num + "]]>").append("</quantity__stduom>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("conv__qty_stduom"))
					{
						try
						{
							num = Integer.parseInt(genericUtility.getColumnValue("conv__qty_stduom", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1422 linr"+e1);
						}
						uom = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						try
						{
							num1 = Integer.parseInt(genericUtility.getColumnValue("quantity", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1433 linr"+e1);
						}
						num = getConvQuantityFact(uom,unitStd,itemCode,quantity,num1,conn);
						valueXmlString.append("<quantity__stduom >").append("<![CDATA[" +  num2 + "]]>").append("</quantity__stduom>");
					}
					
					else if(currentColumn.trim().equalsIgnoreCase("rate"))
					{
						try
						{
							num = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1449 linr"+e1);
						}
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						try
						{
							num1 = Integer.parseInt(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1460 linr"+e1);
						}
						num3 = num1;
						if(unitRate == null || unitRate.trim().length() == 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								unitRate = rs.getString(1)==null?"":rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							num = getConvQuantityFact(unitStd,unitRate,itemCode,num,num1,conn);
							valueXmlString.append("<unit__rate >").append("<![CDATA[" +  unitRate + "]]>").append("</unit__rate>");
						}
						else
						{
							num = getConvQuantityFact(unitStd,unitRate,itemCode,num,num1,conn);
						}
						if(num3 == 0)
						{
							valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" +  num1 + "]]>").append("</conv__rtuom_stduom>");
						}
						valueXmlString.append("<rate__stduom >").append("<![CDATA[" +  num2 + "]]>").append("</rate__stduom>");
						priceListClg = genericUtility.getColumnValue("PRICE_LIST__CLG", dom1);
						if(priceListClg == null || priceListClg.trim().length() == 0)
						{
							valueXmlString.append("<rate__clg >").append("<![CDATA[" +  num + "]]>").append("</rate__clg>");
						}
					}

					else if(currentColumn.trim().equalsIgnoreCase("unit__rate"))
					{
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						try
						{
							num1 = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1460 linr"+e1);
						}
						num2 = 0;
						num = getConvQuantityFact(unitStd,unitRate,itemCode,num1,num2,conn);
						valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" +  num2 + "]]>").append("</conv__rtuom_stduom>");
						valueXmlString.append("<rate__stduom >").append("<![CDATA[" +  num + "]]>").append("</rate__stduom>");
					}

					else if(currentColumn.trim().equalsIgnoreCase("conv__rtuom_stduom"))
					{
						try
						{
							num = Integer.parseInt(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1449 linr"+e1);
						}
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						try
						{
							num1 = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1460 linr"+e1);
						}
						num = getConvQuantityFact(unitStd,unitRate,itemCode,num1,num,conn);
						valueXmlString.append("<rate__stduom >").append("<![CDATA[" +  num2 + "]]>").append("</rate__stduom>");
					}

					else if(currentColumn.trim().equalsIgnoreCase("pack_code"))
					{
						packCode = genericUtility.getColumnValue("pack_code", dom);
						sql = "select descr from packing where pack_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, packCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<pack_instr >").append("<![CDATA[" +  descr + "]]>").append("</pack_instr>");
					}
					valueXmlString.append("</Detail2>");
					break;
					
				case 3:
					parentNodeList = dom.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail3>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

					if(currentColumn.trim().equalsIgnoreCase("site_code"))
					{
						valueXmlString.append("<contract_no >").append("<![CDATA[" +  genericUtility.getColumnValue("contract_no", dom1) + "]]>").append("</contract_no>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
					{
						custCode = genericUtility.getColumnValue("cust_code", dom);
						sql = "SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<cust_name >").append("<![CDATA[" +  descr + "]]>").append("</cust_name>");
					}
					valueXmlString.append("</Detail3>");
					break;       
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}	 

	private int getDailyExchRate(String curr, String string, String siteCode,Timestamp contractDate, String string2, Connection conn) 
	{
		int exchRateSell = 0;
		String sql = "";
		String varValue = "";
		String finEntity = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(curr == null || curr.trim().length() == 0)
		{
			try
			{
				sql = "select fin_entity from site where site_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					finEntity = rs.getString(1)==null?"":rs.getString(1);

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select curr_code from finent where  fin_entity = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						curr = rs.getString(1)==null?"":rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				sql = "select exch_rate__sell from daily_exch_rate_sell_buy where curr_code = ? and " +
						" curr_code__to = ?  and ? between from_date and to_date ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, curr);
				pstmt.setString(2, curr);
				pstmt.setTimestamp(3, contractDate);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					exchRateSell = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(exchRateSell == 0)
				{
					sql = "select exch_rate from daily_exch_rate_sell_buy where curr_code = ? " +
							" and curr_code__to = ? and ? between from_date and to_date";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, curr);
					pstmt.setString(2, curr);
					pstmt.setTimestamp(3, contractDate);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						exchRateSell = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(exchRateSell != 0)
				{
					exchRateSell = 1 / exchRateSell;
				}
				sql = "select rtrim(case when var_value is null then 'Y' else var_value end) " +
						" from finparm where prd_code = '999999' and var_name = 'EXCRT_CURR'";
				pstmt =  conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					varValue = rs.getString(1);
				}
				if(!rs.next() || varValue.equals("Y"))
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select std_exrt from currency where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, curr);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						exchRateSell = rs.getInt(1);
					}
				}
				else
				{
					return 0;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}
			catch (Exception e)
			{
				System.out.println("Exception in getDailyExchRate ........");
			}
		}
		return exchRateSell;
	}

	private int getConvQuantityFact(String uom, String unitStd,String itemCode, int quantity, int num1,Connection conn) 
	{
		int cnt = 0;
		int fact = 0;
		int newQty = 0;
		int roundTo = 0;
		int conv = 0;
		String sql = "";
		String round = "";
		String order = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(uom == null && unitStd == null)
		{
			fact = 1;
			return num1;
		}
		else if(uom != null && unitStd != null && uom.equals(unitStd))
		{
			fact = 1;
			return num1;
		}
		try
		{
			sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and item_code = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, uom);
			pstmt.setString(2, unitStd);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				cnt = rs.getInt(1);
				if(cnt == 0 )
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and item_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, unitStd);
					pstmt.setString(2, uom);
					pstmt.setString(3, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						cnt = rs.getInt(1);
						if(cnt == 0 )
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and  item_code = 'X'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, uom);
							pstmt.setString(2, unitStd);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and  item_code = 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, unitStd);
									pstmt.setString(2, uom);
									pstmt.setString(3, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										if(cnt == 0)
										{
											return -999999999;
										}
										else
										{
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, unitStd);
											pstmt.setString(2, uom);
											rs = pstmt.executeQuery();
											if(rs.next()) 
											{
												conv = rs.getInt(1);
												round = rs.getString(2)==null?"":rs.getString(2);
												roundTo = rs.getInt(3);
												order = "REVORD";
											}											
										}
									}
								}
								else
								{
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, uom);
									pstmt.setString(2, unitStd);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										conv = rs.getInt(1);
										round = rs.getString(2)==null?"":rs.getString(2);
										roundTo = rs.getInt(3);
										order = "ACTORD";
									}			
								}
							}
						}
						else
						{
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, unitStd);
							pstmt.setString(2, uom);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								conv = rs.getInt(1);
								round = rs.getString(2)==null?"":rs.getString(2);
								roundTo = rs.getInt(3);
								order = "REVORD";
							}										
						}
					}
				}
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, uom);
					pstmt.setString(2, unitStd);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						conv = rs.getInt(1);
						round = rs.getString(2)==null?"":rs.getString(2);
						roundTo = rs.getInt(3);
						order = "ACTORD";
					}			
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(fact == 0)
			{
				if(order.equals("ACTORD"))
				{
					newQty = conv * num1;
					fact = conv;
				}
				else
				{
					newQty = 1 / conv * num1;
					fact = 1 / conv;
				}
			}
			else
			{
				newQty = fact * num1;
			}
			newQty = getQuantity(newQty,round,roundTo);
			
		}
		catch (Exception e) 
		{
			System.out.println("Exception in getConvQuantityFact........");
		}
		return newQty;
	}

	private int getQuantity(int newQty, String round, int roundTo) 
	{
		if(newQty < 0)
		{
			newQty = Math.abs(newQty);
		}
		else if(newQty == 0)
		{
			return newQty;
		}
		if(round.equals("N"))
		{
			return newQty;
		}
		if(roundTo == 0)
		{
			return newQty;
		}
		if(round.equals("X"))
		{
			if(newQty % roundTo > 0)
			{
				newQty = newQty - (newQty % roundTo) + roundTo;
			}
		}
		else if(round.equals("P"))
		{
			newQty = newQty - (newQty % roundTo);
		}
		else if(round.equals("R"))
		{
			if(newQty % roundTo < roundTo/2)
			{
				newQty = newQty - (newQty % roundTo);
			}
			else
			{
				newQty = newQty - (newQty % roundTo) + roundTo;
			}
		}
		return newQty;
	}

	private int getPickRate(String priceList,String nrp, Timestamp contractDate,String itemCode, String string, String string2, Connection conn) 
	{
		int nrpRate = 0;
		String sql = "";
		String priceListParent = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try
		{
			sql = "select rate from pricelist where price_list = ? and item_code  = ?" +
				" and list_type = 'L' and eff_from <= ?  and valid_upto >= ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, contractDate);
			pstmt.setTimestamp(4, contractDate);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				nrpRate = rs.getInt(1);				
			}

			else
			{
				String pList = priceList;
				do
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "lect (case when price_list__parent is null  then '' else price_list__parent end ) " +
							" from pricelist_mst where price_list =  ? and list_type = 'L'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, pList);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						priceListParent = rs.getString(1)==null?"":rs.getString(1);
						if(priceListParent.trim().length() > 0)
						{
							sql = "select rate from pricelist where price_list = ? and item_code  = ? " +
									" and list_type = 'L' and eff_from <= ?  and valid_upto >= ?";
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1, priceListParent);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, contractDate);
							pstmt1.setTimestamp(4, contractDate);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()) 
							{
								nrpRate = rs.getInt(1);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							if(nrpRate > 0)
							{
								break;
							}
							else
							{
								pList = priceListParent;
								priceListParent = null;
							}
						}
					}
					else
					{
						return -1;
					}
				}while(true);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e) 
		{
			System.out.println("Exception in Pick rate get.......");
		}
		return nrpRate;
	}

	private String getPriceListType(String priceList,Connection conn) 
	{
		String sql = "";
		String priceListType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = " select list_type from pricelist where price_list  = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				priceListType = rs.getString(1)==null?"":rs.getString(1);
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in getPriceListType ........" );
		}
		
		return priceListType;
	}

	private int getPickRate(String priceList, Timestamp contractDate,String itemCode, String string, String string2, int quantity, Connection conn) 
	{
		int rate = 0;
		String sql = "";
		String priceListParent = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try
		{
			if(getPriceListType(priceList,conn).equals(""))
			{
				return -1;
			}
			sql = "select rate from pricelist where price_list = ? and item_code  = ? and list_type = 'L' " +
					" and eff_from <= ? and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, contractDate);
			pstmt.setTimestamp(4, contractDate);
			pstmt.setInt(5, quantity);
			pstmt.setInt(6, quantity);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				rate = rs.getInt(1);
			}
			else
			{
				String pList = priceList;
				do
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) " +
							" from pricelist_mst where price_list = ? and list_type = 'L'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, pList);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						priceListParent = rs.getString(1)==null?"":rs.getString(1);
						if(priceListParent.trim().length() > 0)
						{
							sql = "select rate from pricelist where price_list = ? " +
									" and item_code  = ?  and list_type = 'L' and eff_from <= ? " +
									" and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1, priceListParent);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, contractDate);
							pstmt1.setTimestamp(4, contractDate);
							pstmt1.setInt(5, quantity);
							pstmt1.setInt(6, quantity);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()) 
							{
								rate = rs.getInt(1);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							if(rate > 0)
							{
								break;
							}
							else
							{
								pList = priceListParent;
								priceListParent = null;
							}
						}
					}
					else
					{
						return -1;
					}
				}while(true);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception exc1)
		{
			System.out.println("GET PICK RATE EXCEPTION////////");
		}
		return rate;
	}

	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, errorCode);			
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
		return msgType;
	}

	private String getCurrdateInAppFormat()
	{
		String currAppdate =null;
		java.sql.Timestamp currDate = null;
		Object date = null;
		SimpleDateFormat DBDate=null;
		try
		{
				currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
				
			 	DBDate= new SimpleDateFormat(genericUtility.getDBDateFormat());
				date = DBDate.parse(currDate.toString());
				currDate =	java.sql.Timestamp.valueOf(DBDate.format(date).toString() + " 00:00:00.0");
				currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
			System.out.println("Exception in  getCurrdateInAppFormat:::"+e.getMessage());
		}
		return (currAppdate);
	}
}
