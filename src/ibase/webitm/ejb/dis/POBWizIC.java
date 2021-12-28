/********************************************************
	Title : POBWizIC[D15ESUN017]
	Date  : 10/09/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.mfg.MfgCommon;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import ibase.utility.E12GenericUtility;
import java.text.DecimalFormat;
import org.apache.xerces.dom.AttributeMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class POBWizIC extends ValidatorEJB implements POBWizICLocal, POBWizICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = null;
	ValidatorEJB valEjb = null;

	public static  String staticSiteCode="";
	public static  String staticCustCode="";
	public static  String staticOrderDate="";
	public static  String staticPriceListGrp="";
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
				System.out.println("xmlString[" + xmlString + "]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1[" + xmlString1 + "]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2[" + xmlString2 + "]");
			}

			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [POBWizIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String custCode = "", bankCode = "", lineNo = "";
		String itemSer = "", orderType = "", scCode = "", retailerCode = "";
		String scMobileNo1 = "", scMobileNo2 = "", retMobileNo1 = "", retMobileNo2 = "", itemCode = "";
		String quantity = "", rate = "", freeQtyStr = "", discountStr = "", pobNo = "", loginEmpCode = "";
		String pobOrdType = "", OrdType1 = "", OrdTypeSplit = "", resultOrdType = "";
		String siteCode="",priceListHdr="",priceListGrp="";
		String scTele1="",scTele2="",retTele1="",retTele2="";
		int cnt = 0;
		int ctr = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		int currentFormNo = 0;
		int count = 0;
		double quantityDbl = 0, rateDbl = 0;
		Boolean isNumber = true;
		Boolean isOrdType = false;

		NodeList parentList = null;
		NodeList childList = null;
		try
		{
			System.out.println("@@@@@@@@ wfvaldata called");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection() ;
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");//Added by Jaffar S. for getting site code [21-11-18]
			DistCommon distCommon = new DistCommon();
			finCommon = new FinCommon();
			valEjb =new ValidatorEJB();
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
				ArrayList filter = new ArrayList();
				filter.add(0, "item_ser");
				filter.add(1, "order_type");
				filter.add(2, "cust_code");
				filter.add(3, "sc_code");
				filter.add(4, "retailer_code");
				for (int fld = 0; fld < filter.size(); fld++)
				{
					childNodeName = (String) filter.get(fld);
					if (childNodeName.equalsIgnoreCase("item_ser"))
					{
						itemSer = this.genericUtility.getColumnValue("item_ser", dom);
						if (itemSer != null && itemSer.trim().length() > 0)
						{

							if (!(isExist(conn, "itemser", "item_ser", itemSer)))
							{
								errCode = "VTITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} else
						{
							errCode = "VMITSER";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} else if (childNodeName.equalsIgnoreCase("order_type"))
					{
						orderType = this.genericUtility.getColumnValue("order_type", dom);
						if (orderType != null && orderType.trim().length() > 0)
						{

							if (!(isExist(conn, "sordertype", "order_type", orderType)))
							{
								errCode = "VTORDTYNF1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else
							{
								pobOrdType = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
								if (pobOrdType == null || "NULLFOUND".equalsIgnoreCase(pobOrdType))
								{
									pobOrdType = "";
									errCode = "VTPOBDISPM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if (pobOrdType != null && (pobOrdType.trim().length() > 0))
								{
									String[] arrStr = pobOrdType.split(",");
									int len = arrStr.length;
									for (int i = 0; i < len; i++)
									{
										OrdType1 = arrStr[i];
										if (orderType.trim().equalsIgnoreCase(OrdType1.trim()))
										{
											isOrdType = true;
										}
									}
									if (!isOrdType)
									{
										errCode = "VTDISORTY";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}

							}

						} else
						{
							errCode = "VMORTYBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} else if (childNodeName.equalsIgnoreCase("cust_code"))
					{
						custCode = this.genericUtility.getColumnValue("cust_code", dom);
						if (custCode != null && custCode.trim().length() > 0)
						{
							if (!(isExist(conn, "sprs_stockist", "cust_code", custCode)))
							{
								errCode = "VTSTRGNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else
							{
								sql = "select count(*) as cnt from sprs_stockist where sprs_code =? and cust_code=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, loginEmpCode);
								pstmt.setString(2, custCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									count = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (count == 0)
								{
									errCode = "VTSTCUSTCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									errCode = valEjb.isCustomer(siteCode, custCode, "",  conn);
									System.out.println("errCode>>>>>>>>"+errCode);
									if (errCode != null && errCode.trim().length() > 0 )
									{
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						} else
						{
							errCode = "VMCUSTCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} else if (childNodeName.equalsIgnoreCase("sc_code"))
					{
						scCode = this.genericUtility.getColumnValue("sc_code", dom);
						itemSer = this.genericUtility.getColumnValue("item_ser", dom);
						System.out.println(">>>itemSer:"+itemSer);
						if (scCode != null && scCode.trim().length() > 0)
						{
							if (!(isExist(conn, "strg_customer", "sc_code", scCode)))
							{
								errCode = "VMSTRGCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{
								//Condition Added by sagar on 13/10/15 validate Customer/Pharmacy in strg_series table
								count= 0;
								sql = " select count(*) as cnt from strg_series a, strg_customer b where a.sc_code = b.sc_code and a.item_ser= ? and a.sales_pers= ? and a.sc_code= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								pstmt.setString(2, loginEmpCode);
								pstmt.setString(3, scCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									count = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println(">>>>strg_series count:"+ count);
								if (count == 0)
								{
									errCode = "VMSTRGSRCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						} 
						else
						{
							errCode = "VMSTRGCDBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} else if (childNodeName.equalsIgnoreCase("retailer_code"))
					{
						retailerCode = this.genericUtility.getColumnValue("retailer_code", dom);
						if (retailerCode != null && retailerCode.trim().length() > 0)
						{
							if (!(isExist(conn, "retailer", "retailer_code", retailerCode)))
							{
								errCode = "VMRETCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								if (!(isExist(conn, "retailer_doctor", "retailer_code", retailerCode)))
								{
									errCode = "VMDOCRETCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
						} else
						{
							errCode = "VMRETCDBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				break; // case 1 end
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("@@@@@@@@@@@@childNodeListLength[" + childNodeListLength + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("cust_code"))
					{
						custCode = this.genericUtility.getColumnValue("cust_code", dom);
						siteCode = this.genericUtility.getColumnValue("site_code", dom);
						if (custCode != null && custCode.trim().length() > 0)
						{
							sql = "select price_list from site_customer where site_code = ? and cust_code= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, custCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								priceListHdr = checkNull(rs.getString("price_list"));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (priceListHdr == null || priceListHdr.trim().length() == 0)
							{
								sql = "select price_list from customer where cust_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									priceListHdr = checkNull(rs.getString("price_list"));

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("priceListHdr from customer =" + priceListHdr);
							}
							if (priceListHdr == null || priceListHdr.trim().length() == 0)
							{
								errCode = "VTPRCLIST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					}else if (childNodeName.equalsIgnoreCase("sc_mobile_no_1"))
					{
						scMobileNo1 = this.genericUtility.getColumnValue("sc_mobile_no_1", dom);
						if (scMobileNo1 != null && scMobileNo1.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!scMobileNo1.matches(pattern))
							{
								errCode = "VTSCMOBNO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("sc_mobile_no_2"))
					{

						scMobileNo2 = this.genericUtility.getColumnValue("sc_mobile_no_2", dom);
						if (scMobileNo2 != null && scMobileNo2.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!scMobileNo2.matches(pattern))
							{
								errCode = "VTSCMOBNO2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					} else if (childNodeName.equalsIgnoreCase("ret_mobile_no_1"))
					{
						retMobileNo1 = this.genericUtility.getColumnValue("ret_mobile_no_1", dom);
						if (retMobileNo1 != null && retMobileNo1.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!retMobileNo1.matches(pattern))
							{
								errCode = "VTREMOBNO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} else if (childNodeName.equalsIgnoreCase("sc_tele_1"))
					{
						scTele1 = this.genericUtility.getColumnValue("sc_tele_1", dom);
						if (scTele1 != null && scTele1.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!scTele1.matches(pattern))
							{
								errCode = "VTSTRGTEL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}else if (childNodeName.equalsIgnoreCase("sc_tele_2"))
					{

						scTele2 = this.genericUtility.getColumnValue("sc_tele_2", dom);
						if (scTele2 != null && scTele2.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!scTele2.matches(pattern))
							{
								errCode = "VTSTRGTEL2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}else if (childNodeName.equalsIgnoreCase("ret_tele_1"))
					{

						retTele1 = this.genericUtility.getColumnValue("ret_tele_1", dom);
						if (retTele1 != null && retTele1.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!retTele1.matches(pattern))
							{
								errCode = "VTRETTEL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}else if (childNodeName.equalsIgnoreCase("ret_tele_2"))
					{
						retTele2 = this.genericUtility.getColumnValue("ret_tele_2", dom);
						if (retTele2 != null && retTele2.trim().length() > 0)
						{
							String pattern = "^[0-9]*$";
							if (!retTele2.matches(pattern))
							{
								errCode = "VTRETTEL2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
				} // end for
				break; // case 2 end
			case 3:

				parentList = dom.getElementsByTagName("Detail3");
				int parentNodeListLength = parentList.getLength();
				for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr--)
				{
					parentNode = parentList.item(prntCtr - 1);
					childList = parentNode.getChildNodes();
					for (int ctr1 = 0; ctr1 < childList.getLength(); ctr1++)
					{
						childNode = childList.item(ctr1);
						if (childNode != null && childNode.getNodeName().equalsIgnoreCase("attribute"))
						{
							String updateFlag = "";
							updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
							if (updateFlag.equalsIgnoreCase("D"))
							{
								System.out.println("Break from here as the record is deleted");
								break;
							}
						}
						//if (childNode != null && childNode.getFirstChild() != null && childNode.getNodeName().equalsIgnoreCase("item_code"))
						if (childNode.getNodeName().equalsIgnoreCase("item_code"))
						{
							itemCode = this.genericUtility.getColumnValue("item_code", dom);
							pobNo = this.genericUtility.getColumnValue("tran_id", dom);
							lineNo = this.genericUtility.getColumnValue("line_no", dom);
							if (itemCode == null || itemCode.trim().length() == 0)
							{
								errCode = "VMITEMNULL";
								errList.add(errCode);
								errFields.add(childNode.getNodeName().toLowerCase());
							} else
							{
								if (!(isExist(conn, "item", "item_code", itemCode)))
								{
									errCode = "VMITEMNOT";
									errList.add(errCode);
									errFields.add(childNode.getNodeName().toLowerCase());
								} else
								{
									if (isDulplicateFrmDom(dom2, itemCode, lineNo))
									{
										errCode = "VTDUPITM";
										errList.add(errCode);
										errFields.add(childNode.getNodeName().toLowerCase());
									}

								}
							}
						} else if (childNode != null && childNode.getFirstChild() != null && childNode.getNodeName().equalsIgnoreCase("rate"))
						{
							rate = this.genericUtility.getColumnValue("rate", dom);
							if (rate != null && rate.trim().length() > 0)
							{
								isNumber = isDoubleValid(rate);
								if (isNumber == false)
								{
									errCode = "INVLDRATE";
									errList.add(errCode);
									errFields.add(childNode.getNodeName().toLowerCase());

								} else
								{
									rateDbl = Double.parseDouble(rate);
									/*if (rateDbl <= 0)
									{
										errCode = "VTRATELESS";
										errList.add(errCode);
										errFields.add(childNode.getNodeName().toLowerCase());
									}*/
								}
							} else
							{
								errCode = "VTRATEBLK";
								errList.add(errCode);
								errFields.add(childNode.getNodeName().toLowerCase());
							}
						} else if (childNode != null && childNode.getFirstChild() != null && childNode.getNodeName().equalsIgnoreCase("quantity"))
						{
							quantity = this.genericUtility.getColumnValue("quantity", dom);
							if (quantity != null && quantity.trim().length() > 0)
							{
								isNumber = isDoubleValid(quantity);
								if (isNumber == false)
								{
									errCode = "INVLDQTY";
									errList.add(errCode);
									errFields.add(childNode.getNodeName().toLowerCase());

								} else
								{
									quantityDbl = Double.parseDouble(quantity);
									if (quantityDbl <= 0)
									{
										errCode = "VTQTYLESS";
										errList.add(errCode);
										errFields.add(childNode.getNodeName().toLowerCase());
									}
								}
							} else
							{
								errCode = "VTQTYBLK";
								errList.add(errCode);
								errFields.add(childNode.getNodeName().toLowerCase());
							}
						} else if (childNode != null && childNode.getFirstChild() != null && childNode.getNodeName().equalsIgnoreCase("free_qty"))
						{
							freeQtyStr = this.genericUtility.getColumnValue("free_qty", dom);
							if (freeQtyStr != null && freeQtyStr.trim().length() > 0)
							{
								isNumber = isDoubleValid(freeQtyStr);
								if (isNumber == false)
								{
									errCode = "INVLDFRQTY";
									errList.add(errCode);
									errFields.add(childNode.getNodeName().toLowerCase());

								}

							}
						}
						//added by rupali on 08/04/2021 [start]
						/*
						else if (childNode.getNodeName().equalsIgnoreCase("scheme_code") && childNode != null && childNode.getFirstChild() != null )
						{
							String schemeCode = checkNull(this.genericUtility.getColumnValue("scheme_code", dom)).trim();
							itemCode = checkNull(this.genericUtility.getColumnValue("item_code", dom)).trim();
							System.out.println("schemeCode:::::"+schemeCode);
							if (schemeCode != null && schemeCode.length() > 0)
							{
								int schemeCnt = 0;
								// sql = "select count(*) from item item, scheme_applicability scheme where scheme.scheme_code = 'P2MF00024' and item.item_code = 'FG00000001' and  scheme.scheme_code = item.bom_code ";
								sql = "select count(*) as scheme_count from scheme_applicability where scheme_code = ? and item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									schemeCnt = rs.getInt("scheme_count");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(schemeCnt == 0)
								{
									errCode = "VTINVSCHEM";
									errList.add(errCode);
									errFields.add(childNode.getNodeName().toLowerCase());
								}
							} 
						}
						*/
						//added by rupali on 08/04/2021 [end]
					}// internal for loop

				}// for loop

				break; // case 3 end
			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0)
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else
			{
				errStringXml = new StringBuffer("");
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	// end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............." + xmlString);
		System.out.println("xmlString1............" + xmlString1);
		System.out.println("xmlString2............" + xmlString2);
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [EcollectionIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String sql = "", sql1 = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		java.util.Date currDate = new java.util.Date();
		ConnDriver connDriver = new ConnDriver();

		int currentFormNo = 0, childNodeListLength = 0;
		String columnValue = "", userId = "", termId = "";
		String orderType = "", itemSer = "", doctor = "", retailerCode = "";
		String pobNo = "", loginSiteCode = "", siteDescr = "", custCode = "", custName = "";
		String firstName = "", orderTypeDescr = "", middleName = "", lastName = "", scCode = "", scName = "";
		String retailerName = "", itemserDescr = "", itemser = "", stanCodeDescr = "", strgEmailAddr = "";
		String strgCity = "", strgStanCode = "", strgTele1 = "", strgTele2 = "", strgMobileNo = "", strgName = "";
		String retailerStanCode = "", retailerCity = "", retailerMobileNo = "", retailerEmailAddr = "", retailerTele1 = "", retailerTele2 = "";
		String pobDate = "", siteCocde = "", strgMobileNo1 = "", strgMobileNo2 = "", retailerMobileNo1 = "", retailerMobileNo2 = "";
		String empCodeOrd = "", orderedBy = "", dlvCfa = "", lastOrdValue = "", lastOrdPod = "";
		String wfStatus = "", confiemd = "", remarks = "", empCodeAprv = "", addDate = "";
		String addUser = "", addTerm = "", chgDate = "", chgUser = "", chgTerm = "", pharmacyOwn = "";
		String itemDescr = "", unit = "", itemCode = "";
		String rateStr = "", quantityStr = "", freeQtyStr = "", loginEmpCode = "", confirmed = "";
		String pobOrdType = "", OrdType1 = "", OrdTypeSplit = "", resultOrdType = "";
		String preFormDet = "",priceListHdr="",siteCode="",priceListGrp="",orderDate="";
		String previousPOB="",lastOrdPOB="";
		String line_no="";
		int pobLineNo = 0;
		int parentNodeListLength = 0;
		int lineNo = 0;
		Timestamp frDate = null, toDate = null;
		double quantity = 0.0, rate = 0.0, freeQty = 0.0, totalQty = 0.0, discount = 0.0, netAmt = 0.0;
		double pickRate=0.0;
		double lastPOBAmt=0.0;
		java.util.Date tranDate = null, dlvCfaDt = null, dueDate = null, lastOrdDt = null ;
		java.util.Date statusDate = null, confDate = null, addDate1 = null, chgDate1 = null,lastPOBDate=null;
		Boolean islastOrdPod=false;
		String schemeCode = "";
		try
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection() ;
			conn.setAutoCommit(false);
			connDriver = null;
			DistCommon distCommon = new DistCommon();
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("Now the date is :=>  " + sysDate);

			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");

			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo + "**************");
			switch (currentFormNo)
			{
			case 1:
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					pobOrdType = distCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
					if (pobOrdType == null || "NULLFOUND".equalsIgnoreCase(pobOrdType))
					{
						pobOrdType = "";
					}
					if (pobOrdType != null && (pobOrdType.trim().length() > 0))
					{
						String[] arrStr = pobOrdType.split(",");
						int len = arrStr.length;
						for (int i = 0; i < len; i++)
						{
							OrdType1 = arrStr[i];
							OrdTypeSplit = OrdTypeSplit + "'" + OrdType1 + "',";
						}
						resultOrdType = OrdTypeSplit.substring(0, OrdTypeSplit.length() - 1);
					}
					sql = "select item_ser from department where dept_code in(select dept_code from employee where emp_code=? )";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginEmpCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemser = checkNull(rs.getString("item_ser"));
						System.out.println("itemser :" + itemser);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = " select descr from itemser where item_ser=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemser);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemserDescr = checkNull(rs.getString("descr"));
						System.out.println("itemserDescr :" + itemserDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<order_type>").append("").append("</order_type>\r\n");
					valueXmlString.append("<item_ser>").append("<![CDATA[" + itemser + "]]>").append("</item_ser>\r\n");
					valueXmlString.append("<cust_code>").append("").append("</cust_code>\r\n");
					valueXmlString.append("<sc_code>").append("").append("</sc_code>\r\n");
					valueXmlString.append("<retailer_code>").append("").append("</retailer_code>\r\n");
					valueXmlString.append("<order_type_descr>").append("").append("</order_type_descr>\r\n");
					valueXmlString.append("<cust_name>").append("").append("</cust_name>\r\n");
					valueXmlString.append("<sc_name>").append("").append("</sc_name>\r\n");
					valueXmlString.append("<login_code>").append("<![CDATA[" + loginEmpCode.trim() + "]]>").append("</login_code>\r\n");
					valueXmlString.append("<retailer_name>").append("").append("</retailer_name>\r\n");
					valueXmlString.append("<item_ser_descr>").append("<![CDATA[" + itemserDescr + "]]>").append("</item_ser_descr>\r\n");
					valueXmlString.append("<pob_order_type>").append("<![CDATA[" + resultOrdType + "]]>").append("</pob_order_type>\r\n");
					valueXmlString.append("</Detail1>");
				} else if (currentColumn.trim().equals("item_ser"))
				{
					itemser = checkNull(genericUtility.getColumnValue("item_ser", dom));
					System.out.println("itemser :" + itemser);

					sql = " select descr from itemser where item_ser=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemser);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemserDescr = checkNull(rs.getString("descr"));
						System.out.println("itemserDescr :" + itemserDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<item_ser>").append("<![CDATA[" + itemser + "]]>").append("</item_ser>\r\n");
					valueXmlString.append("<item_ser_descr>").append("<![CDATA[" + itemserDescr + "]]>").append("</item_ser_descr>\r\n");
					valueXmlString.append("</Detail1>");

				} else if (currentColumn.trim().equals("order_type"))
				{
					orderType = checkNull(genericUtility.getColumnValue("order_type", dom));
					System.out.println("orderType :" + orderType);

					sql = " select descr from sordertype where order_type=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						orderTypeDescr = checkNull(rs.getString("descr"));
						System.out.println("orderTypeDescr :" + orderTypeDescr);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<order_type>").append("<![CDATA[" + orderType + "]]>").append("</order_type>\r\n");
					valueXmlString.append("<order_type_descr>").append("<![CDATA[" + orderTypeDescr + "]]>").append("</order_type_descr>\r\n");
					valueXmlString.append("</Detail1>");

				} else if (currentColumn.trim().equals("cust_code"))
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					System.out.println("custCode :" + custCode);

					sql = " select cust_name from customer where cust_code=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						custName = checkNull(rs.getString("cust_name"));
						System.out.println("custName :" + custName);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<cust_code>").append("<![CDATA[" + custCode + "]]>").append("</cust_code>\r\n");
					valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>\r\n");
					valueXmlString.append("</Detail1>");
				} else if (currentColumn.trim().equals("sc_code"))
				{
					scCode = checkNull(genericUtility.getColumnValue("sc_code", dom));
					System.out.println("scCode :" + scCode);

					sql = " select first_name,middle_name,last_name from strg_customer where sc_code=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, scCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						firstName = checkNull(rs.getString("first_name"));
						middleName = checkNull(rs.getString("middle_name"));
						lastName = checkNull(rs.getString("last_name"));
						scName = firstName + " " + middleName + " " + lastName;
						System.out.println("scName :" + scName);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<sc_code>").append("<![CDATA[" + scCode + "]]>").append("</sc_code>\r\n");
					valueXmlString.append("<sc_name>").append("<![CDATA[" + scName + "]]>").append("</sc_name>\r\n");
					valueXmlString.append("</Detail1>");
				} else if (currentColumn.trim().equals("retailer_code"))
				{
					retailerCode = checkNull(genericUtility.getColumnValue("retailer_code", dom));
					System.out.println("retailerCode :" + retailerCode);

					sql = " select retailer_name from retailer where retailer_code=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, retailerCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						retailerName = checkNull(rs.getString("retailer_name"));
						System.out.println("retailerName :" + retailerName);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					valueXmlString.append("<retailer_code>").append("<![CDATA[" + retailerCode + "]]>").append("</retailer_code>\r\n");
					valueXmlString.append("<retailer_name>").append("<![CDATA[" + retailerName + "]]>").append("</retailer_name>\r\n");
					valueXmlString.append("</Detail1>");
				}
				break;
			case 2:
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					orderType = this.genericUtility.getColumnValue("order_type", dom1);
					itemSer = this.genericUtility.getColumnValue("item_ser", dom1);
					custCode = this.genericUtility.getColumnValue("cust_code", dom1);
					doctor = this.genericUtility.getColumnValue("sc_code", dom1);
					retailerCode = this.genericUtility.getColumnValue("retailer_code", dom1);

					sql = "select cust_name from customer where cust_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						custName = rs.getString("cust_name");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = " select descr from sordertype where order_type=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						orderTypeDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select descr from site where site_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						siteDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select descr from itemser where item_ser=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemserDescr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "SELECT first_name,middle_name,last_name,city,stan_code,tele1,tele2,mobile_no,email_addr " + " FROM strg_customer WHERE sc_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, doctor);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						firstName = rs.getString("first_name");
						middleName = rs.getString("middle_name");
						lastName = rs.getString("last_name");
						strgCity = rs.getString("city");
						strgStanCode = rs.getString("stan_code");
						strgTele1 = rs.getString("tele1");
						strgTele2 = rs.getString("tele2");
						strgMobileNo = rs.getString("mobile_no");
						strgEmailAddr = rs.getString("email_addr");
						strgName = firstName + "  " + middleName + "  " + lastName;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = " select descr from station where stan_code=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, strgStanCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stanCodeDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = " select retailer_name,stan_code,city,mobile_no,email_addr,tele1,tele2 " + " from retailer where retailer_code=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, retailerCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						retailerName = checkNull(rs.getString("retailer_name"));
						retailerStanCode = checkNull(rs.getString("stan_code"));
						retailerCity = checkNull(rs.getString("city"));
						retailerMobileNo = checkNull(rs.getString("mobile_no"));
						retailerEmailAddr = checkNull(rs.getString("email_addr"));
						retailerTele1 = checkNull(rs.getString("tele1"));
						retailerTele2 = checkNull(rs.getString("tele2"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select fr_date,to_date from acctprd where sysdate between fr_date and to_date";
					pstmt = conn.prepareStatement(sql);
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
					System.out.println("frDate>>>>" + frDate + "toDate>>>>>>>" + toDate);

					sql1 = "select * from pob_hdr " + "where order_type=? and item_ser=? and cust_code=? and  sc_code=? and " 
					+ " retailer_code=? and wf_status='O' and tran_date BETWEEN  ? AND ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, orderType);
					pstmt1.setString(2, itemSer);
					pstmt1.setString(3, custCode);
					pstmt1.setString(4, doctor);
					pstmt1.setString(5, retailerCode);
					pstmt1.setTimestamp(6, frDate);
					pstmt1.setTimestamp(7, toDate);

					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						System.out.println("Edit mode>>>>>>>>>");
						pobNo = rs1.getString("tran_id");
						tranDate = rs1.getDate("tran_date");
						siteCocde = checkNull(rs1.getString("site_code"));
						itemSer = checkNull(rs1.getString("item_ser"));
						orderType = checkNull(rs1.getString("order_type"));
						custCode = checkNull(rs1.getString("cust_code"));
						scCode = checkNull(rs1.getString("sc_code"));
						strgMobileNo1 = checkNull(rs1.getString("sc_mobile_no_1"));
						strgMobileNo2 = checkNull(rs1.getString("sc_mobile_no_2"));
						strgTele1 = checkNull(rs1.getString("sc_tele_1"));
						strgTele2 = checkNull(rs1.getString("sc_tele_2"));
						strgEmailAddr = checkNull(rs1.getString("sc_email_addr"));
						retailerCode = checkNull(rs1.getString("retailer_code"));
						retailerMobileNo1 = checkNull(rs1.getString("ret_mobile_no_1"));
						retailerMobileNo2 = checkNull(rs1.getString("ret_mobile_no_2"));
						retailerTele1 = checkNull(rs1.getString("ret_tele_1"));
						retailerTele2 = checkNull(rs1.getString("ret_tele_2"));
						empCodeOrd = checkNull(rs1.getString("emp_code__ord"));
						orderedBy = checkNull(rs1.getString("ordered_by"));
						dlvCfa = checkNull(rs1.getString("dlv_cfa"));
						dlvCfaDt = rs1.getDate("dlv_cfa_dt");
						dueDate = rs1.getDate("due_date");
						lastOrdDt = rs1.getDate("last_ord_dt");
						lastOrdValue = checkNull(rs1.getString("last_ord_value"));
						lastOrdPod = checkNull(rs1.getString("last_ord_pod"));
						wfStatus = checkNull(rs1.getString("wf_status"));
						statusDate = rs1.getDate("status_date");
						confirmed = checkNull(rs1.getString("confirmed"));
						confDate = rs1.getDate("conf_date");
						remarks = checkNull(rs1.getString("remarks"));
						empCodeAprv = checkNull(rs1.getString("emp_code__aprv"));
						addDate1 = rs1.getDate("add_date");
						addUser = checkNull(rs1.getString("add_user"));
						addTerm = checkNull(rs1.getString("add_term"));
						chgDate1 = rs1.getDate("chg_date");
						chgUser = checkNull(rs1.getString("chg_user"));
						chgTerm = checkNull(rs1.getString("chg_term"));
						pharmacyOwn = checkNull(rs1.getString("pharmacy_own"));

						valueXmlString.append("<Detail2 domID='1' selected=\"N\">\r\n");
						valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"E\"  status=\"O\" pkNames=\"\" />\r\n");
						valueXmlString.append("<tran_id><![CDATA[" + pobNo + "]]></tran_id>");
						valueXmlString.append("<tran_date><![CDATA[" + sdf.format(tranDate).toString() + "]]></tran_date>");
						valueXmlString.append("<item_ser protect='1'><![CDATA[" + itemSer + "]]></item_ser>");
						valueXmlString.append("<itemser_descr>").append("<![CDATA[" + itemserDescr + "]]>").append("</itemser_descr>\r\n");
						valueXmlString.append("<order_type protect='1'><![CDATA[" + orderType + "]]></order_type>");
						valueXmlString.append("<sordertype_descr>").append("<![CDATA[" + orderTypeDescr + "]]>").append("</sordertype_descr>\r\n");
						valueXmlString.append("<cust_code protect='1'><![CDATA[" + custCode + "]]></cust_code>");
						valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>\r\n");
						valueXmlString.append("<site_code>").append("<![CDATA[" + loginSiteCode + "]]>").append("</site_code>\r\n");
						valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>\r\n");
						valueXmlString.append("<sc_code protect='1'><![CDATA[" + scCode + "]]></sc_code>");
						valueXmlString.append("<sc_mobile_no_1><![CDATA[" + strgMobileNo1 + "]]></sc_mobile_no_1>");
						valueXmlString.append("<sc_mobile_no_2><![CDATA[" + strgMobileNo2 + "]]></sc_mobile_no_2>");
						valueXmlString.append("<sc_tele_1><![CDATA[" + strgTele1 + "]]></sc_tele_1>");
						valueXmlString.append("<sc_tele_2><![CDATA[" + strgTele2 + "]]></sc_tele_2>");
						valueXmlString.append("<sc_email_addr><![CDATA[" + strgEmailAddr + "]]></sc_email_addr>");
						valueXmlString.append("<stan_code>").append("<![CDATA[" + strgStanCode + "]]>").append("</stan_code>\r\n");
						valueXmlString.append("<station_descr>").append("<![CDATA[" + stanCodeDescr + "]]>").append("</station_descr>\r\n");
						valueXmlString.append("<city>").append("<![CDATA[" + strgCity + "]]>").append("</city>\r\n");
						valueXmlString.append("<strg_cust_name>").append("<![CDATA[" + strgName + "]]>").append("</strg_cust_name>\r\n");
						valueXmlString.append("<retailer_code protect='1'><![CDATA[" + retailerCode + "]]></retailer_code>");
						valueXmlString.append("<retailer_name>").append("<![CDATA[" + retailerName + "]]>").append("</retailer_name>\r\n");
						valueXmlString.append("<ret_mobile_no_1><![CDATA[" + retailerMobileNo1 + "]]></ret_mobile_no_1>");
						valueXmlString.append("<ret_mobile_no_2><![CDATA[" + retailerMobileNo2 + "]]></ret_mobile_no_2>");
						valueXmlString.append("<ret_tele_1><![CDATA[" + retailerTele1 + "]]></ret_tele_1>");
						valueXmlString.append("<ret_tele_2><![CDATA[" + retailerTele2 + "]]></ret_tele_2>");
						valueXmlString.append("<pharmacy_own><![CDATA[" + pharmacyOwn + "]]></pharmacy_own>");
						valueXmlString.append("<emp_code__ord><![CDATA[" + empCodeOrd + "]]></emp_code__ord>");
						valueXmlString.append("<ordered_by><![CDATA[" + orderedBy + "]]></ordered_by>");
						valueXmlString.append("<dlv_cfa><![CDATA[" + dlvCfa + "]]></dlv_cfa>");
						if (dlvCfaDt != null)
						{
							valueXmlString.append("<dlv_cfa_dt><![CDATA[" + sdf.format(dlvCfaDt).toString() + "]]></dlv_cfa_dt>");
						} else
						{
							valueXmlString.append("<dlv_cfa_dt><![CDATA[ ]]></dlv_cfa_dt>");
						}
						if (dueDate != null)
						{
							valueXmlString.append("<due_date><![CDATA[" + sdf.format(dueDate).toString() + "]]></due_date>");
						} else
						{
							valueXmlString.append("<due_date><![CDATA[ ]]></due_date>");
						}

						if (lastOrdDt != null)
						{
							valueXmlString.append("<last_ord_dt><![CDATA[" + sdf.format(lastOrdDt).toString() + "]]></last_ord_dt>");
						} else
						{
							valueXmlString.append("<last_ord_dt><![CDATA[ ]]></last_ord_dt>");
						}
						valueXmlString.append("<last_ord_value><![CDATA[" + lastOrdValue + "]]></last_ord_value>");
						valueXmlString.append("<last_ord_pod><![CDATA[" + lastOrdPod + "]]></last_ord_pod>");
						valueXmlString.append("<wf_status><![CDATA[" + wfStatus + "]]></wf_status>");
						if (statusDate != null)
						{
							valueXmlString.append("<status_date><![CDATA[" + sdf.format(statusDate).toString() + "]]></status_date>");
						} else
						{
							valueXmlString.append("<status_date><![CDATA[ ]]></status_date>");
						}
						valueXmlString.append("<confirmed><![CDATA[" + confirmed + "]]></confirmed>");
						if (confDate != null)
						{
							valueXmlString.append("<conf_date><![CDATA[" + sdf.format(confDate).toString() + "]]></conf_date>");
						} else
						{
							valueXmlString.append("<conf_date><![CDATA[ ]]></conf_date>");
						}

						valueXmlString.append("<remarks><![CDATA[" + remarks + "]]></remarks>");
						valueXmlString.append("<emp_code__aprv><![CDATA[" + empCodeAprv + "]]></emp_code__aprv>");
						if (addDate1 != null)
						{
							valueXmlString.append("<add_date><![CDATA[" + sdf.format(addDate1).toString() + "]]></add_date>");
						} else
						{
							valueXmlString.append("<add_date><![CDATA[ ]]></add_date>");
						}
						valueXmlString.append("<add_user>").append("<![CDATA[" + addUser + "]]>").append("</add_user>\r\n");
						valueXmlString.append("<add_term>").append("<![CDATA[" + addTerm + "]]>").append("</add_term>\r\n");
						valueXmlString.append("<chg_date><![CDATA[" + sdf.format(currDate).toString() + "]]></chg_date>");
						valueXmlString.append("<chg_user>").append("<![CDATA[" + userId + "]]>").append("</chg_user>\r\n");
						valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>\r\n");
						valueXmlString.append("</Detail2>\r\n");

					} else
					{

						System.out.println("**********Add mode*********" + currDate);

						sql = "select max(tran_id)  as old_tranid from pob_hdr where order_type= ?" +
								" and item_ser=? and cust_code=?  and  sc_code=? and  retailer_code=? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, orderType);
						pstmt.setString(2, itemSer);
						pstmt.setString(3, custCode);
						pstmt.setString(4, doctor);
						pstmt.setString(5, retailerCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							previousPOB = checkNull(rs.getString("old_tranid"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(previousPOB!= null && previousPOB.trim().length()>0)
						{
							islastOrdPod=true;
							sql = " select tran_date from pob_hdr where tran_id =? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, previousPOB);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lastPOBDate = rs.getDate("tran_date");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = " select sum(net_amt) as netAmt from pob_det where tran_id=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, previousPOB);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lastPOBAmt = rs.getDouble("netAmt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						valueXmlString.append("<Detail2 domID='1' objContext=\"2\" selected=\"N\">\r\n");
						valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						valueXmlString.append("<tran_id protect='1'><![CDATA[]]></tran_id>");
						valueXmlString.append("<tran_date><![CDATA[" + sdf.format(currDate).toString() + "]]></tran_date>");
						valueXmlString.append("<item_ser protect='1'>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>\r\n");
						valueXmlString.append("<itemser_descr>").append("<![CDATA[" + itemserDescr + "]]>").append("</itemser_descr>\r\n");
						valueXmlString.append("<order_type protect='1'>").append("<![CDATA[" + orderType + "]]>").append("</order_type>\r\n");
						valueXmlString.append("<sordertype_descr>").append("<![CDATA[" + orderTypeDescr + "]]>").append("</sordertype_descr>\r\n");
						valueXmlString.append("<cust_code protect='1'>").append("<![CDATA[" + custCode + "]]>").append("</cust_code>\r\n");
						valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>\r\n");
						valueXmlString.append("<site_code>").append("<![CDATA[" + loginSiteCode + "]]>").append("</site_code>\r\n");
						valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>\r\n");

						valueXmlString.append("<sc_code protect='1'>").append("<![CDATA[" + doctor + "]]>").append("</sc_code>\r\n");
						valueXmlString.append("<stan_code>").append("<![CDATA[" + strgStanCode + "]]>").append("</stan_code>\r\n");
						valueXmlString.append("<station_descr>").append("<![CDATA[" + stanCodeDescr + "]]>").append("</station_descr>\r\n");
						valueXmlString.append("<city>").append("<![CDATA[" + strgCity + "]]>").append("</city>\r\n");
						valueXmlString.append("<strg_cust_name>").append("<![CDATA[" + strgName + "]]>").append("</strg_cust_name>\r\n");
						valueXmlString.append("<sc_mobile_no_1>").append("<![CDATA[" + strgMobileNo + "]]>").append("</sc_mobile_no_1>\r\n");
						valueXmlString.append("<sc_mobile_no_2>").append("").append("</sc_mobile_no_2>\r\n");
						valueXmlString.append("<sc_tele_1>").append("<![CDATA[" + strgTele1 + "]]>").append("</sc_tele_1>\r\n");
						valueXmlString.append("<sc_tele_2>").append("<![CDATA[" + strgTele2 + "]]>").append("</sc_tele_2>\r\n");
						valueXmlString.append("<sc_email_addr>").append("<![CDATA[" + strgEmailAddr + "]]>").append("</sc_email_addr>\r\n");

						valueXmlString.append("<retailer_code protect='1'>").append("<![CDATA[" + retailerCode + "]]>").append("</retailer_code>\r\n");
						valueXmlString.append("<retailer_name>").append("<![CDATA[" + retailerName + "]]>").append("</retailer_name>\r\n");
						valueXmlString.append("<pharmacy_own>").append("").append("</pharmacy_own>\r\n");
						valueXmlString.append("<ret_mobile_no_1>").append("<![CDATA[" + retailerMobileNo + "]]>").append("</ret_mobile_no_1>\r\n");
						valueXmlString.append("<ret_mobile_no_2>").append("").append("</ret_mobile_no_2>\r\n");
						valueXmlString.append("<ret_tele_1>").append("<![CDATA[" + retailerTele1 + "]]>").append("</ret_tele_1>\r\n");
						valueXmlString.append("<ret_tele_2>").append("<![CDATA[" + retailerTele2 + "]]>").append("</ret_tele_2>\r\n");
						valueXmlString.append("<emp_code__ord>").append("<![CDATA[" + loginEmpCode + "]]>").append("</emp_code__ord>\r\n");
						valueXmlString.append("<ordered_by>").append("").append("</ordered_by>\r\n");
						valueXmlString.append("<dlv_cfa>").append("").append("</dlv_cfa>\r\n");
						valueXmlString.append("<dlv_cfa_dt>").append("").append("</dlv_cfa_dt>\r\n");
						valueXmlString.append("<due_date>").append("").append("</due_date>\r\n");
						valueXmlString.append("<last_ord_value>").append("<![CDATA[" + lastPOBAmt + "]]>").append("</last_ord_value>\r\n");
						if (lastPOBDate != null)
						{
						valueXmlString.append("<last_ord_dt>").append("<![CDATA[" + sdf.format(lastPOBDate).toString() +"]]>").append("</last_ord_dt>\r\n");
						}else
						{
							valueXmlString.append("<last_ord_dt>").append("").append("</last_ord_dt>\r\n");
						}
						if(islastOrdPod)
						{
						valueXmlString.append("<last_ord_pod><![CDATA[Y]]></last_ord_pod>");
						}else
						{
							valueXmlString.append("<last_ord_pod><![CDATA[N]]></last_ord_pod>");
						}
						valueXmlString.append("<wf_status>").append("O").append("</wf_status>\r\n");
						valueXmlString.append("<status_date>").append("").append("</status_date>\r\n");
						valueXmlString.append("<confirmed>").append("N").append("</confirmed>\r\n");
						valueXmlString.append("<conf_date>").append("").append("</conf_date>\r\n");
						valueXmlString.append("<emp_code__aprv>").append("").append("</emp_code__aprv>\r\n");
						valueXmlString.append("<emp_fname>").append("").append("</emp_fname>\r\n");
						valueXmlString.append("<emp_mname>").append("").append("</emp_mname>\r\n");
						valueXmlString.append("<emp_lname>").append("").append("</emp_lname>\r\n");
						valueXmlString.append("<remarks>").append("").append("</remarks>\r\n");
						valueXmlString.append("<add_date><![CDATA[" + sdf.format(currDate).toString() + "]]></add_date>");
						valueXmlString.append("<add_user>").append("<![CDATA[" + userId + "]]>").append("</add_user>\r\n");
						valueXmlString.append("<add_term>").append("<![CDATA[" + termId + "]]>").append("</add_term>\r\n");
						valueXmlString.append("<chg_date><![CDATA[" + sdf.format(currDate).toString() + "]]></chg_date>");
						valueXmlString.append("<chg_user>").append("<![CDATA[" + userId + "]]>").append("</chg_user>\r\n");
						valueXmlString.append("<chg_term>").append("<![CDATA[" + termId + "]]>").append("</chg_term>\r\n");
						valueXmlString.append("<pharmacy_own>").append("C").append("</pharmacy_own>\r\n");

						valueXmlString.append("</Detail2>\r\n");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
				System.out.println("valueXmlString from case 2 :" + valueXmlString);
				break;

			case 3:
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					Node detail1Node = dom2.getElementsByTagName("Detail2").item(0);
					pobNo = checkNull(genericUtility.getColumnValueFromNode("tran_id", detail1Node));
					staticSiteCode = checkNull(genericUtility.getColumnValueFromNode("site_code", detail1Node));
					staticCustCode = checkNull(genericUtility.getColumnValueFromNode("cust_code", detail1Node));
					staticOrderDate = checkNull(genericUtility.getColumnValueFromNode("tran_date", detail1Node));
					System.out.println("Detail pobNo>>>>" + pobNo);
					sql = "select price_list from site_customer where site_code = ? and cust_code= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, staticSiteCode);
					pstmt.setString(2, staticCustCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceListHdr = checkNull(rs.getString("price_list"));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (priceListHdr == null || priceListHdr.trim().length() == 0)
					{
						sql = "select price_list from customer where cust_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, staticCustCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							priceListHdr = checkNull(rs.getString("price_list"));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("priceListHdr from customer =" + priceListHdr);
					}
					if (priceListHdr != null && priceListHdr.trim().length() > 0)
					{
						staticPriceListGrp = priceListHdr;
					}
					if (pobNo != null && pobNo.trim().length() > 0)
					{
						sql = "select * from pob_det where tran_id=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, pobNo);
						rs = pstmt.executeQuery();
						while (rs.next())
						{
							pobNo = checkNull(rs.getString("tran_id"));
							pobLineNo = rs.getInt("line_no");
							itemCode = checkNull(rs.getString("item_code"));
							rate = rs.getDouble("rate");
							quantity = rs.getDouble("quantity");
							freeQty = rs.getDouble("free_qty");
							totalQty = rs.getDouble("tot_qty");
							discount = rs.getDouble("discount");
							netAmt = rs.getDouble("net_amt");
							schemeCode = checkNull(rs.getString("scheme_code"));

							sql1 = "select descr,unit from item where item_code=?";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, itemCode);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								itemDescr = rs1.getString("descr");
								unit = rs1.getString("unit");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							valueXmlString.append("<Detail3 domID='" + pobLineNo + "' objContext=\"3\" selected=\"Y\">\r\n");
							valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"E\"  status=\"O\" pkNames=\"\" />\r\n");
							valueXmlString.append("<tran_id><![CDATA[" + pobNo + "]]></tran_id>");
							valueXmlString.append("<line_no><![CDATA[" + pobLineNo + "]]></line_no>");
							valueXmlString.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
							valueXmlString.append("<descr><![CDATA[" + itemDescr + "]]></descr>");
							valueXmlString.append("<unit><![CDATA[" + unit + "]]></unit>");
							valueXmlString.append("<quantity><![CDATA[" + quantity + "]]></quantity>");
							valueXmlString.append("<free_qty><![CDATA[" + freeQty + "]]></free_qty>");
							valueXmlString.append("<rate><![CDATA[" + rate + "]]></rate>");
							valueXmlString.append("<discount><![CDATA[" + discount + "]]></discount>");
							valueXmlString.append("<tot_qty><![CDATA[" + totalQty + "]]></tot_qty>");
							valueXmlString.append("<net_amt><![CDATA[" + netAmt + "]]></net_amt>");
							valueXmlString.append("<scheme_code><![CDATA[" + schemeCode + "]]></scheme_code>");
							valueXmlString.append("</Detail3>");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else
					{
						System.out.println("empty tran_id>>>>>>" + pobNo);
						if (preDomExists(dom2, Integer.toString(currentFormNo)))
						{
							System.out.println("caseInside dom exist in case 3");

							valueXmlString.append("<Detail3 domID='0' objContext=\"3\" >\r\n");
							valueXmlString.append("</Detail3>");
						}
					}

				} else if (currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					System.out.println("itm_default_add  called");

					Node detail1Node = dom2.getElementsByTagName("Detail2").item(0);
					pobNo = checkNull(genericUtility.getColumnValueFromNode("tran_id", detail1Node));
					if (pobNo != null && pobNo.trim().length() > 0)
					{
						NodeList parentList = dom2.getElementsByTagName("Detail3");
						parentNodeListLength = parentList.getLength();
						System.out.println("Total Detail 3"+parentNodeListLength);
						System.out.println((new StringBuilder("parentNodeListLength[")).append(parentNodeListLength).append("]").toString());
						for (int prntCtr = 0; prntCtr < parentNodeListLength; prntCtr++)
						{
							Node parentNode1 = parentList.item(prntCtr);

							AttributeMap attrMap = (AttributeMap) parentNode1.getAttributes();
							System.out.println("[" + prntCtr + "] Node dbID [" + attrMap.getNamedItem("domID").getLocalName() + ":" + attrMap.getNamedItem("domID").getNodeValue() + "]");
							lineNo = Integer.parseInt(attrMap.getNamedItem("domID").getNodeValue());
							System.out.println("lineNo----" + lineNo);
						}

						valueXmlString.append("<Detail3 domID='" + lineNo + "' objContext=\"3\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						valueXmlString.append("<tran_id>").append("<![CDATA[" + pobNo + "]]>").append("</tran_id>\r\n");
						valueXmlString.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>\r\n");
						valueXmlString.append("<item_code>").append("").append("</item_code>\r\n");
						valueXmlString.append("<descr>").append("").append("</descr>\r\n");
						valueXmlString.append("<unit>").append("").append("</unit>\r\n");
						valueXmlString.append("<quantity>").append("").append("</quantity>\r\n");
						valueXmlString.append("<free_qty>").append("").append("</free_qty>\r\n");
						valueXmlString.append("<rate>").append("0.0").append("</rate>\r\n");
						valueXmlString.append("<discount>").append("").append("</discount>\r\n");
						valueXmlString.append("<tot_qty>").append("0.0").append("</tot_qty>\r\n");
						valueXmlString.append("<net_amt>").append("0.0").append("</net_amt>\r\n");
						valueXmlString.append("<item_ser visible='0'>").append("").append("</item_ser>\r\n");
						valueXmlString.append("</Detail3>");
					} else
					{
						NodeList parentList = dom2.getElementsByTagName("Detail3");
						parentNodeListLength = parentList.getLength();
						
						/*for (int prntCtr = 0; prntCtr < parentNodeListLength; prntCtr++)
						{
							Node parentNode1 = parentList.item(prntCtr);

							AttributeMap attrMap = (AttributeMap) parentNode1.getAttributes();
							System.out.println("[" + prntCtr + "] Node domID [" + attrMap.getNamedItem("domID").getLocalName() + ":" + attrMap.getNamedItem("domID").getNodeValue() + "]");
							lineNo = Integer.parseInt(attrMap.getNamedItem("domID").getNodeValue());
							System.out.println("lineNo----" + lineNo);
						}*/
						
						if(parentNodeListLength > 0)
						{
							lineNo = parentNodeListLength;
						}
						else
						{
							lineNo = 1;
						}
						System.out.println("Current len of Detail3 ["+lineNo+"]");
						valueXmlString.append("<Detail3 domID='" + lineNo + "' objContext=\"3\" selected=\"N\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						valueXmlString.append("<tran_id>").append("").append("</tran_id>\r\n");
						valueXmlString.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>\r\n");
						valueXmlString.append("<item_code>").append("").append("</item_code>\r\n");
						valueXmlString.append("<descr>").append("").append("</descr>\r\n");
						valueXmlString.append("<unit>").append("").append("</unit>\r\n");
						valueXmlString.append("<quantity>").append("").append("</quantity>\r\n");
						valueXmlString.append("<free_qty>").append("").append("</free_qty>\r\n");
						valueXmlString.append("<rate>").append("0.0").append("</rate>\r\n");
						valueXmlString.append("<discount>").append("").append("</discount>\r\n");
						valueXmlString.append("<tot_qty>").append("0.0").append("</tot_qty>\r\n");
						valueXmlString.append("<net_amt>").append("0.0").append("</net_amt>\r\n");
						valueXmlString.append("</Detail3>");
					}

				} else if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					System.out.println("lineNo ["+lineNo+"]");
					//Node detail1Node = dom2.getElementsByTagName("Detail3").item(0);
					Node detail1Node = dom.getElementsByTagName("Detail3").item(0);
					itemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", detail1Node));
					line_no =checkNull(genericUtility.getColumnValueFromNode("line_no", detail1Node));
					System.out.println("line_no In Dom["+line_no+"]");
					sql = "select descr,unit from item where item_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemDescr = rs.getString("descr");
						unit = rs.getString("unit");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					//valueXmlString.append("<Detail3 domID='1' objContext='3'>");
					valueXmlString.append("<Detail3 domID='"+line_no+"' objContext='3'>");	//Added condition by Abhijit
					valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>\r\n");
					valueXmlString.append("<descr>").append("<![CDATA[" + itemDescr + "]]>").append("</descr>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>\r\n");
					valueXmlString.append("</Detail3>");

				} else if ( currentColumn.trim().equalsIgnoreCase("quantity") || currentColumn.trim().equalsIgnoreCase("free_qty"))
				{
					Node detail1Node = dom.getElementsByTagName("Detail3").item(0);
					
					rateStr = checkNull(genericUtility.getColumnValueFromNode("rate", detail1Node));
					quantityStr = checkNull(genericUtility.getColumnValueFromNode("quantity", detail1Node));
					freeQtyStr = checkNull(genericUtility.getColumnValueFromNode("free_qty", detail1Node));
					itemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", detail1Node));
					line_no =checkNull(genericUtility.getColumnValueFromNode("line_no", detail1Node));
					if (quantityStr != null && quantityStr.trim().length() > 0)
					{
						if (isDoubleValid(quantityStr))
						{
							quantity = Double.parseDouble(quantityStr);
						}
					}
					if (rateStr != null && rateStr.trim().length() > 0)
					{
						if (isDoubleValid(rateStr))
						{
							rate = Double.parseDouble(rateStr);
						}
					}
					if (freeQtyStr != null && freeQtyStr.trim().length() > 0)
					{
						if (isDoubleValid(freeQtyStr))
						{
							freeQty = Double.parseDouble(freeQtyStr);
						}
					}
					if (freeQty > 0 && quantity > 0)
					{
						discount = 100.0 * freeQty / quantity;
					}
					pickRate = distCommon.pickRate(staticPriceListGrp, staticOrderDate, itemCode, "", "L", quantity, conn);
					System.out.println("pickRate>>>>>>>>>"+pickRate);
					//valueXmlString.append("<Detail3 domID='1' objContext='3'>");
					valueXmlString.append("<Detail3 domID='"+line_no+"' objContext='3'>");
					if (pickRate < 0)
					{
						valueXmlString.append("<rate>").append("<![CDATA[" + 0 + "]]>").append("</rate>");
					} else
					{
						valueXmlString.append("<rate>").append("<![CDATA[" + pickRate + "]]>").append("</rate>");
					}
					if (quantity > 0)
					{
						valueXmlString.append("<quantity>").append("<![CDATA[" + getRequiredDecimal(quantity,3) + "]]>").append("</quantity>\r\n");
					} else
					{
						valueXmlString.append("<quantity>").append("0").append("</quantity>\r\n");
					}
					if (freeQty > 0)
					{
						valueXmlString.append("<free_qty>").append("<![CDATA[" + getRequiredDecimal(freeQty,3) + "]]>").append("</free_qty>\r\n");
					} else
					{
						valueXmlString.append("<free_qty>").append("0").append("</free_qty>\r\n");
					}
					valueXmlString.append("<discount>").append("<![CDATA[" + getRequiredDecimal(discount, 3) + "]]>").append("</discount>\r\n");
					if(quantity<=0)
					{
						quantity=0;
					}
					if(freeQty<=0)
					{
						freeQty=0;
					}
					valueXmlString.append("<tot_qty>").append("<![CDATA[" + getRequiredDecimal((quantity + freeQty), 3) + "]]>").append("</tot_qty>\r\n");
					if (pickRate < 0)
					{
						valueXmlString.append("<net_amt>").append("<![CDATA["+0+ "]]>").append("</net_amt>\r\n");
					}else
					{
						valueXmlString.append("<net_amt>").append("<![CDATA[" + getRequiredDecimal((quantity * pickRate), 3) + "]]>").append("</net_amt>\r\n");
					}
					valueXmlString.append("</Detail3>");

				}

				break;
			}
			valueXmlString.append("</Root>");
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;

					}
					conn.close();
				}
				conn = null;
			} catch (Exception d)
			{
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}

	public String getRequiredDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return decFormat.format(actVal);
	}

	public static java.util.Date relativeDate(java.util.Date date, int days)
	{
		java.util.Date calculatedDate = null;
		if (date != null)
		{
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, days);
			calculatedDate = new java.util.Date(calendar.getTime().getTime());
		}
		return calculatedDate;
	}

	private String errorType(Connection conn, String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}

	private boolean isExist(Connection conn, String tableName, String columnName, String value) throws ITMException, RemoteException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		boolean status = false;
		try
		{
			sql = "SELECT count(*) from " + tableName + " where " + columnName + "  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();

			if (rs.next())
			{
				if (rs.getBoolean(1))
				{
					status = true;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e)
		{
			System.out.println("Exception in isExist ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from isExist ");
		return status;
	}

	public String getAttributesAboutNode(Node node)
	{
		StringBuffer strValue = new StringBuffer();
		short type = node.getNodeType();
		switch (type)
		{
		case 1:
		{
			NamedNodeMap attrs = node.getAttributes();
			int len = attrs.getLength();
			for (int i = 0; i < len; ++i)
			{
				Attr attr = (Attr) attrs.item(i);
				strValue.append(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
			}
		}
		}
		return strValue.toString();
	}

	public boolean isDoubleValid(String number) throws ITMException, Exception
	{

		Boolean isReult = true;
		double validNumber = 0.0;
		try
		{
			validNumber = Double.parseDouble(number);

		} catch (NumberFormatException e)
		{

			isReult = false;
		}
		return isReult;

	}

	private boolean isDulplicateFrmDom(Document dom, String itemCode, String lineNo) throws ITMException
	{
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;

		String lineNoDom = "";
		String updateFlag="";
		boolean isDulplicate = false;
		String itemCodeDom = "";
		
		try
		{
			parentList = dom.getElementsByTagName("Detail3");
			int parentNodeListLength = parentList.getLength();
			for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr--)
			{
				parentNode = parentList.item(prntCtr - 1);
				
				//Commented and changes done by Jaffar S. on 06-12-18 for getting porper line no and item code
				lineNoDom = genericUtility.getColumnValueFromNode("line_no", parentNode);
				if (!lineNo.equalsIgnoreCase(lineNoDom))
				{
					itemCodeDom = genericUtility.getColumnValueFromNode("item_code", parentNode);
					if (itemCode.trim().equalsIgnoreCase(itemCodeDom.trim()))
					{
						System.out.println("-----------isDulplicate = true------");
						System.out.println("LineNo ---- "+lineNo );
						System.out.println("LineNoDom >>> "+lineNoDom );
						System.out.println("ITEMCODE ==== "+itemCode);
						System.out.println("ITEMCODEDOM ==== "+itemCodeDom);
						isDulplicate = true;
						break;
					}

				}
				/*
				childList = parentNode.getChildNodes();
				//for (int ctr = 0; ctr < childList.getLength(); ctr++)
				for (int ctr = childList.getLength()-1; ctr >= 0; ctr--)
				{
					childNode = childList.item(ctr);
					if (childNode != null && childNode.getNodeName().equalsIgnoreCase("attribute"))
					{
						updateFlag = "";
						updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
						if (updateFlag.equalsIgnoreCase("D"))
						{
							//itemCodeDom="";
							System.out.println("Break from here as the record is deleted");
							break;
						}
					}
					if (childNode != null && childNode.getFirstChild() != null && childNode.getNodeName().equalsIgnoreCase("line_no"))
					{
						lineNoDom = childNode.getFirstChild().getNodeValue().trim();
						System.out.println("LineNoDom ==> "+lineNoDom );
						if (lineNo.equalsIgnoreCase(lineNoDom))
						{
							System.out.println("From Line No Node --- LineNo ---- "+lineNo );
							System.out.println("From Line No Node --- LineNoDom >>> "+lineNoDom );
							System.out.println("From Line No Node --- ITEMCODE ==== "+itemCode);
							System.out.println("From Line No Node --- ITEMCODEDOM ==== "+itemCodeDom);
							System.out.println("Break from here as line No match");
							itemCodeDom = "";
							break;
						}
					}

					if (childNode != null && childNode.getFirstChild() != null && childNode.getNodeName().equalsIgnoreCase("item_code"))
					{
						itemCodeDom = childNode.getFirstChild().getNodeValue().trim();
						System.out.println("From Item Code Node --- itemCode ==== "+itemCode);
						System.out.println("From Item Code Node --- itemCodeDom ==== "+itemCodeDom);
					}
				}
				if (itemCode.trim().equalsIgnoreCase(itemCodeDom.trim()))
				{
					System.out.println("-----------isDulplicate = true------");
					System.out.println("LineNo ---- "+lineNo );
					System.out.println("LineNoDom >>> "+lineNoDom );
					System.out.println("ITEMCODE ==== "+itemCode);
					System.out.println("ITEMCODEDOM ==== "+itemCodeDom);
					isDulplicate = true;
					break;
				}
				*/
				
				//------------[End]

			}// for loop

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally
		{
			try
			{
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// System.out.println("isDulplicate>>>>>> ["+isDulplicate+"]");
		return isDulplicate;
	}

	public boolean preDomExists(Document dom, String currentFormNo) throws ITMException
	{
		System.out.println("Inside preDomExists method::[" + dom + "]");
		NodeList parentList = null;
		NodeList childList = null;
		Node childNode = null;
		boolean selected = false;

		try
		{
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);
			if (parentList == null || parentList.getLength() == 0)
			{
				System.out.println("Inside preDomExists method parentList null::");
				return selected;
			}

			if (parentList.item(0) != null)
			{
				childList = parentList.item(0).getChildNodes();
				System.out.println("Inside preDomExists method childList ::" + childList);
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);
					System.out.println("Inside preDomExists method childNode ::" + childNode);
					if ((childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null))
					{
						System.out.println("Column found!!!" + childNode.getNodeName());
						selected = true;
						break;
					}
				}
			}

		} catch (Exception e)
		{
			System.out.println("Exception :SavexWizardEJB :preDomExists :==>\n" + e.getMessage());
			throw new ITMException(e);
		}

		return selected;
	}
}	
