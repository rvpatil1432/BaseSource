package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

/**
 * Session Bean implementation class CustomerDefaultEJB
 */
@Stateless
public class CustomerDefaultEJB extends ValidatorEJB implements CustomerDefaultRemote, CustomerDefaultLocal {

	private GenericUtility genericUtility = GenericUtility.getInstance();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String sql = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String tranId = "";
		String custCode = "", ignoreCredit = "", ignoreDays = "", acctCodeAdv = "", cctrCodeAdv = "", groupCode = "",
				custCodeBil = "", stanCode = "", terrCode = "";
		String stateCode = "", countCode = "", crTerm = "", currCode = "", currCode1 = "", acctCodeAr = "",
				cctrCodeAr = "", taxClass = "", taxChap = "", priceList = "";
		String salesPers = "", tranCode = "", siteCodeRcp = "", siteCode = "", channelPartner = "", blackListing = "",
				contactCode = "", bankCode = "", priceListDisc = "";
		String salesOption = "", dlvTerm = "", lossPerc = "", adhocReplPerc = "", termTableNo = "", priceListClg = "",
				salesPers1 = "", salesPers2 = "";
		String keyFlag = "", active = "", lsVal3 = "", empCodeOrd = "", empCodeOrd1 = "", custCodePd = "",
				custCodeDisc = "", sgroupCode = "", custCodeAr = "", currCodeFrt = "";
		String blankVar = "", custName = "", shName = "", availableYn = "", creditLmt = "", round = "", roundTo = "",
				currCodeIns = "";
		String regCode = "", validUpto = "", regDate = "";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0, cnt1 = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		String commTable = "";// changed by Nasruddin [19/JUL/16 ]
		String discList = "";
		try {
			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if ((objContext != null) && (objContext.trim().length() > 0)) {
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				tranId = checkNull(this.genericUtility.getColumnValue("tran_id", dom));
				System.out.println("tran id from boqdet --4-->>>>[" + tranId + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("cust_priority")) 
					{
						String custPriority = checkNull(genericUtility.getColumnValue("cust_priority", dom));
						/*
						 * System.out.println("custPriority======>>"+custPriority);
						 * System.out.println("custPriority length======>>"+custPriority.length());
						 */

						if (custPriority.length() > 1) 
						{
							// System.out.println("custPriority found larger value--------->>");
							errCode = "VMCUSTPRIT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						else if (!custPriority.matches("[0-9A-Za-z-]+")) 
						{
							// System.out.println("Special character found for custPriority ---------");
							errCode = "VMCUSTINVD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					else if (childNodeName.equalsIgnoreCase("ignore_credit"))
					{
						ignoreCredit = checkNull(genericUtility.getColumnValue("ignore_credit", dom));

						cnt = 0;
						if (ignoreCredit != null && ignoreCredit.trim().length() > 0) 
						{
							cnt = Integer.parseInt(ignoreCredit.trim());
							if (cnt < 0) 
							{
								errCode = "VMIGCRT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} 
						else 
						{
							errCode = "VMIGCRTNLL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("ignore_days"))
					{
						ignoreDays = checkNull(genericUtility.getColumnValue("ignore_days", dom));

						cnt = 0;
						if (ignoreDays != null && ignoreDays.trim().length() > 0) 
						{
							cnt = Integer.parseInt(ignoreDays.trim());
							if (cnt < 0) 
							{
								errCode = "VMCRDYS";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("acct_code__adv")) 
					{
						acctCodeAdv = checkNull(genericUtility.getColumnValue("acct_code__adv", dom));

						// if (acctCodeAdv != null)
						// {
						cnt = 0;
						sql = "select count(*) as cnt from accounts where acct_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, acctCodeAdv);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt != 0) 
						{
							sql = "select active from accounts where acct_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeAdv);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								active = rs.getString("active");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (!active.equalsIgnoreCase("Y"))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("cctr_code__adv"))
					{
						cctrCodeAdv = checkNull(genericUtility.getColumnValue("cctr_code__adv", dom));
						cnt = 0;
						if (cctrCodeAdv != null && cctrCodeAdv.trim().length() > 0)
						{

							sql = "select count(*) as cnt from costctr where cctr_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCodeAdv);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTCCTRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("cr_term")) 
					{
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));

						if (crTerm != null && crTerm.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from crterm where cr_term =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTCRTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("curr_code")) 
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						cnt = 0;

						sql = "select count(*) as cnt from currency where curr_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, currCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 0)
						{
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						else 
						{
							if (editFlag.equalsIgnoreCase("E")) 
							{
								custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

								sql = "select curr_code from customer where cust_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									currCode1 = rs.getString("curr_code");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!currCode.equalsIgnoreCase(currCode1) && currCode1 != null)
								{
									lsVal3 = "C";
									cnt = 0;

									sql = "select count(distinct curr_code__ac) as ll_count from sundrybal"
											+ " where sundry_code =?" + " and sundry_type =?"
											+ " and (dr_amt != 0 or cr_amt != 0)";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, lsVal3);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										cnt = rs.getInt("ll_count");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									cnt1 = 0;
									sql = "select count(*) as cnt from sundrybal" + " where sundry_code = ?"
											+ " and sundry_type = ?" + " and (dr_amt != 0 or cr_amt != 0)"
											+ " and curr_code__ac = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, lsVal3);
									pstmt.setString(3, currCode1);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										cnt1 = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt1 != 0 && cnt > 1) 
									{
										errCode = "VXCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("acct_code__ar"))
					{
						acctCodeAr = checkNull(genericUtility.getColumnValue("acct_code__ar", dom));

						if (acctCodeAr != null && acctCodeAr.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from accounts where acct_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeAr);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt != 0)
							{
								sql = "select active from accounts where acct_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, acctCodeAr);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									active = rs.getString("active");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!"Y".equalsIgnoreCase(active))
								{
									errCode = "VMACCTA";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("cctr_code__ar"))
					{
						cctrCodeAr = checkNull(genericUtility.getColumnValue("cctr_code__ar", dom));

						if (cctrCodeAr != null && cctrCodeAr.trim().length() > 0) 
						{
							cnt = 0;
							sql = "select count(*) as cnt from costctr where cctr_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCodeAr);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTCCTRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("tax_class")) 
					{
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));

						if (taxClass != null && taxClass.trim().length() > 0) 
						{
							cnt = 0;
							sql = "select count(*) as cnt from taxclass where tax_class =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));

						if (taxChap != null && taxChap.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from taxchap where tax_chap =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("price_list")) 
					{
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom));

						if (priceList != null && priceList.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from pricelist_mst where price_list =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTPLIST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("price_list__clg")) 
					{
						priceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom));

						if (priceListClg != null && priceListClg.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from pricelist_mst where price_list =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceListClg);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTPLIST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tran_code")) 
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));

						if (tranCode != null && tranCode.trim().length() > 0) 
						{
							cnt = 0;
							sql = "select count(*) as cnt from transporter where tran_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("site_code__rcp")) 
					{
						siteCodeRcp = checkNull(genericUtility.getColumnValue("site_code__rcp", dom));

						if (siteCodeRcp != null && siteCodeRcp.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from site where site_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeRcp);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTSITECD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("sales_option")) 
					{
						salesOption = checkNull(genericUtility.getColumnValue("sales_option", dom));

						if (salesOption == null || salesOption.trim().length() <= 0) 
						{
							errCode = "VTSLOPT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("dlv_term"))
					{
						dlvTerm = checkNull(genericUtility.getColumnValue("dlv_term", dom));
						System.out.println("@@@@@@@@@@@@@@@@@ added check null fro dlvterm@@@@@@@@@" + dlvTerm);
						if (dlvTerm != null && dlvTerm.trim().length() > 0) 
						{
							cnt = 0;
							sql = "select count(*) as cnt from delivery_term where dlv_term =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, dlvTerm);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VMDLVTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("adhoc_repl_perc")) 
					{
						adhocReplPerc = checkNull(genericUtility.getColumnValue("adhoc_repl_perc", dom));
						double adhocvalue = 0.00;

						if (adhocReplPerc != null && adhocReplPerc.trim().length() > 0) 
						{

							adhocvalue = Double.valueOf(adhocReplPerc.trim());
							// cnt1=Integer.parseInt(adhocReplPerc.trim());
							// if(cnt1<0 || cnt1>100){
							if (adhocvalue < 0 || adhocvalue > 100) 
							{
								errCode = "VTADH";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("term_table__no")) 
					{
						termTableNo = checkNull(genericUtility.getColumnValue("term_table__no", dom));

						// if( termTableNo != null) Changed By Nasruddin [16-sep-16]
						if (termTableNo != null && termTableNo.trim().length() > 0) 
						{
							sql = "select count(1) as cnt from sale_term_table where term_table  = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, termTableNo);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VMSALETERM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("emp_code__ord")) 
					{
						empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord", dom));

						if (empCodeOrd != null && empCodeOrd.trim().length() > 0) 
						{
							cnt = 0;
							sql = "select count(*) as cnt from employee where emp_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VMEMPORD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					else if (childNodeName.equalsIgnoreCase("emp_code__ord1")) 
					{
						empCodeOrd1 = checkNull(genericUtility.getColumnValue("emp_code__ord1", dom));
						if (empCodeOrd1 != null && empCodeOrd1.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from employee where emp_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCodeOrd1);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VMEMPORD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
				}

				valueXmlString.append("</Detail1>");
				break;
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0)) 
			{
				for (cnt = 0; cnt < errListSize; cnt++) 
				{
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) 
					{
						break;
					}
				}
				errStringXml.append("</Errors> </Root> \r\n");
			} 
			else 
			{
				errStringXml = new StringBuffer("");
			}
		} 
		catch (Exception e) {
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
					conn.close();
					conn = null;
				}
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
			} 
			catch (Exception d) 
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
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("valueXmlString[" + valueXmlString + "]");
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA[" + valueXmlString + "]");
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "", sql1 = "", sql2 = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		System.out.println("currentColumn [" + currentColumn + "]");
		String colName = "", colValue = "", fullName = "", siteCode1 = "";
		String custType = "", priceList = "", countCode1 = "", priceList1 = "", siteCodePay = "", partQty = "",
				siteCode = "", siteCodeRcp = "", priceListDisc = "";
		String resBKList = "", lsDescr = "", lsBKListed = "";
		String lsNull = "", nullVarr = "", ldDate = "";
		String custCodeBill = "", custCode = "", stanCode = "", currCode = "", countCode = "", currCode2 = "";
		String lsStateCode = "", lsCity = "", lsPin = "";
		String custName = "", chqName = "", terrCode = "", terrDesc = "", salesPers = "", spName = "", contactCode = "",
				groupCode = "";
		String bankCode = "", bankName = "";
		String channelPartner = "";
		String crTerm = "", crDays = "";
		String rateRound = "";
		String name = "", shName = "", contPers = "", contPfx = "", addr1 = "", addr2 = "", addr3 = "", stateCode = "",
				tele1 = "", tele2 = "", tele3 = "", teleExt = "", fax = "", emailAddr = "", ediAddr = "";
		String contPfx1 = "", contPers1 = "", Add1 = "", emailAddr1 = "", ediAddr1 = "", keyFlag = "";
		String regCode = "", regDescr = "", empCodeOrd = "", empFname = "", empLname = "", empCodeOrd1 = "",
				deptCode = "";
		int ctr = 0;
		int currentFormNo = 0;
		java.util.Date reqDate = null;
		int childNodeListLength = 0;
		java.util.Date statusDate = null;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		// GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();

		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
			conn.setAutoCommit(false);
			connDriver = null;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0)) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) 
					{
						childNode.getFirstChild();
					}
					ctr++;
				} while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN [" + currentColumn + "]");

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{
					resBKList = checkNull(genericUtility.getColumnValue("reas_code__bklist", dom));
					sql = "select descr from   gencodes where  fld_name = 'REAS_CODE__BKLIST' " + "and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, resBKList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<bklist_reason ><![CDATA[" + lsDescr + "]]></bklist_reason>");

					lsBKListed = checkNull(genericUtility.getColumnValue("black_listed", dom));
					if (lsBKListed.equalsIgnoreCase("N")) 
					{
						valueXmlString.append("<reas_code__bklist protect = \"1\"><![CDATA[" + lsNull + "]]></reas_code__bklist>");
						valueXmlString.append("<bklist_reason ><![CDATA[" + lsNull + "]]></bklist_reason>");
					} 
					else 
					{
						valueXmlString.append("<reas_code__bklist><![CDATA[" + lsNull + "]]></reas_code__bklist>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					resBKList = checkNull(genericUtility.getColumnValue("reas_code__bklist", dom));
					sql = "select descr from   gencodes where  fld_name = 'REAS_CODE__BKLIST' " + "and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, resBKList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<bklist_reason ><![CDATA[" + lsDescr + "]]></bklist_reason>");

					lsBKListed = checkNull(genericUtility.getColumnValue("black_listed", dom));
					if ("N".equalsIgnoreCase(lsBKListed)) 
					{
						valueXmlString.append("<reas_code__bklist protect = \"1\"><![CDATA[" + lsNull + "]]></reas_code__bklist>");
						valueXmlString.append("<bklist_reason ><![CDATA[" + lsNull + "]]></bklist_reason>");
					} 
					else 
					{
						valueXmlString.append("<reas_code__bklist><![CDATA[" + lsNull + "]]></reas_code__bklist>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("channel_partner"))
				{
					channelPartner = checkNull(genericUtility.getColumnValue("channel_partner", dom));

					if (channelPartner.equalsIgnoreCase("Y"))
					{
						valueXmlString.append("<site_code protect = \"0\"><![CDATA[]]></site_code>");
						valueXmlString.append("<fin_link protect = \"0\"><![CDATA[]]></fin_link>");
						valueXmlString.append("<dis_link protect = \"0\"><![CDATA[]]></dis_link>");
					}
					else
					{
						valueXmlString.append("<site_code protect = \"1\"><![CDATA[" + nullVarr + "]]></site_code>");
						valueXmlString.append("<fin_link protect = \"1\"><![CDATA[" + nullVarr + "]]></fin_link>");
						valueXmlString.append("<dis_link protect = \"1\"><![CDATA[" + nullVarr + "]]></dis_link>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("black_listed")) 
				{
					lsBKListed = checkNull(genericUtility.getColumnValue("black_listed", dom));

					System.out.println("black_listed :- " + lsBKListed);
					if (lsBKListed.equalsIgnoreCase("N")) 
					{
						valueXmlString.append(
								"<black_listed_date protect = \"1\"><![CDATA[" + ldDate + "]]></black_listed_date>");
						valueXmlString.append(
								"<reas_code__bklist protect = \"1\"><![CDATA[" + lsNull + "]]></reas_code__bklist>");
						valueXmlString
						.append("<bklist_reason protect = \"1\"><![CDATA[" + lsNull + "]]></bklist_reason>");
					}
					else
					{
						valueXmlString.append("<black_listed_date protect = \"0\"><![CDATA[]]></black_listed_date>");
						valueXmlString.append("<reas_code__bklist protect = \"0\"><![CDATA[]]></reas_code__bklist>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("cr_term"))
				{
					crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));

					sql = "select cr_days from crterm where cr_term =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, crTerm);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						crDays = checkNull(rs.getString("cr_days"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<credit_prd ><![CDATA[" + crDays + "]]></credit_prd>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("rate_round")) 
				{
					rateRound = checkNull(this.genericUtility.getColumnValue("rate_round", dom));

					if (rateRound.equalsIgnoreCase("N")) 
					{
						valueXmlString.append("<rate_round_to protect = \"1\"><![CDATA[]]></rate_round_to>");
					}
					else 
					{
						valueXmlString.append("<rate_round_to protect = \"0\"><![CDATA[]]></rate_round_to>");
					}
				} 
				valueXmlString.append("</Detail1>");
				break;
			}
			valueXmlString.append("</Root>");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} 
		finally 
		{
			try 
			{
				if (conn != null) 
				{
					if (pstmt != null)
						pstmt.close();
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch (Exception d) 
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String str) 
	{
		if (str == null)
		{
			return "";
		} 
		else 
		{
			return str;
		}
	}

	private double checkDoubleNull(String str) 
	{
		if (str == null || str.trim().length() == 0)
		{
			return 0.0;
		} 
		else
		{
			return Double.parseDouble(str);
		}
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
			while (rs.next()) 
			{
				msgType = rs.getString("MSG_TYPE");
			}
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			try {
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
		finally 
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
}