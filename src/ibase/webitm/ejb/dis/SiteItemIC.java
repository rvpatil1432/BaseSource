
/********************************************************
	Title : SiteItemIC
	Date  : 04/10/11
	Developer: Kunal Mandhre

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless;

@Stateless

public class SiteItemIC extends ValidatorEJB implements SiteItemICLocal, SiteItemICRemote {
	// changed by nasruddin 05-10-16
	// GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {

			if (xmlString != null && xmlString.trim().length() > 0) {
				System.out.println("xmlString [" + xmlString + "]");
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				System.out.println("xmlString [" + xmlString1 + "]");
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				System.out.println("xmlString [" + xmlString2 + "]");
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String itemCode = "";
		String active = "";
		String itemSer = "";
		String empCodePln = "";
		String siteCode = "";
		String supplierCodePref = "";
		String roleCodeQcaprv = "";
		String roleCodeIndaprv = "";
		String masterSch = "";
		String suppilerSour = "";
		String siteCodeSuppiler = "";
		String siteCodePlan = "";
		String siteCodeShip = "";
		String appSupp = "";
		String bomCode = "";
		String locCodeAprv = "";
		String eou = "";
		String avail = "";
		String lopReqd = "";
		String locCodeRej = "";
		String locCodeInsp = "";
		String qcReqd = "";
		String stkOpt = "";
		String batchQtyType = "";
		String mfgType = "";
		String autoReqc = "";
		String orderOpt = "";
		String packCode = "";
		String purcOrder = "";
		String qcReqdType = "";
		String varValue = "";
		String blackList = "";
		String empCodeApr = "";
		String empCodePur = "";
		String taxChap = "";
		String rg1Ser = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String sql2 = "";
		String sql3 = "";
		String sql4 = "";
		String errorType = "";
		String shelfLifeType = ""; // added by manish mhatre on 9-sep-2019
		String itemShelfLifeType = ""; // added by manish mhatre on 9-sep-2019
		int count = 0;
		int cnt = 0;
		int purLeadTime = 0;
		int mfgPerc = 0;
		int mfgLeadTime = 0;
		int qcLeadTime = 0;
		int yieldPerc = 0;
		int ctr = 0;
		int childNodeListLength;
		java.util.Date today = null;
		java.util.Date validUpto = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String isFaciLocCode = "", isFaciSiteCode = "", lockGroup = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		PreparedStatement pstmt4 = null;
		PreparedStatement pstmt5 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		ResultSet rs5 = null;
		ConnDriver connDriver = new ConnDriver();
		FinCommon finCommon = new FinCommon(); // Changed By Nasruddin [20-SEP-16]
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try {
			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for (ctr = 0; ctr < childNodeListLength; ctr++) {
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if (childNodeName.equalsIgnoreCase("item_code")) {
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					/*
					 * Comment By Nasruddin [20-SEP-16] START if(itemCode == null ||
					 * itemCode.trim().length() == 0) { errCode = "VMITEMCD1 ";
					 * errList.add(errCode); errFields.add(childNodeName.toLowerCase()); } Comment
					 * By Nasruddin [20-SEP-16] END
					 */
					if (itemCode != null && itemCode.trim().length() > 0) {

						if (editFlag.equalsIgnoreCase("A")) {
							// Changed By Nasruddin [20-SEP-16]
							siteCode = genericUtility.getColumnValue("site_code", dom);
							// sql = "select count(*) from siteitem where item_code = ?";
							sql = "select count(1)  from siteitem where item_code = ? and site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count > 0) {
								errCode = "VMDUPL1 ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						sql = "select count(*)  from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VMITEM1   ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else {
							sql = "select active from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								active = rs.getString(1);
							}
							if (active == null || active.trim().length() == 0) {
								active = "Y";
							}
							if (active != null && !(active.trim().equalsIgnoreCase("Y"))) {
								errCode = "VTITEM4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

				} else if (childNodeName.equalsIgnoreCase("item_ser")) {
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					if (itemSer == null || itemSer.trim().length() == 0) {
						errCode = "VMITMSERBK";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else {
						// errcode = nvo_dis.gbf_itemser(mval,transer)
						sql = "select count(*)  from itemser where item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VTITEMSER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("emp_code__pln")) {
					empCodePln = checkNull(genericUtility.getColumnValue("emp_code__pln", dom));
					 siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));   //commented by manish mhatre on 18-dec-2019
					if (empCodePln != null && empCodePln.trim().length() > 0) {
						// Comment By Nasruddin Start 20-SEP-16
						/*
						 * errcode = nvo_dis.gbf_employee(mval1,mval,transer) sql =
						 * "select count(*)  from employee where emp_code = ?"; pstmt =
						 * conn.prepareStatement(sql); pstmt.setString(1,empCodePln); rs =
						 * pstmt.executeQuery(); if(rs.next()) { cnt = rs.getInt(1); } if(cnt == 0) {
						 * errCode = "VMEMP1"; errList.add(errCode);
						 * errFields.add(childNodeName.toLowerCase()); } rs.close(); rs = null;
						 * pstmt.close(); pstmt = null; Comment By Nasruddin 20-SEP-16 END
						 */

						errCode = finCommon.isEmployee(siteCode, empCodePln, "", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} else if (childNodeName.equalsIgnoreCase("emp_code__iapr")) {
					empCodeApr = checkNull(genericUtility.getColumnValue("emp_code__iapr", dom));
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode")); //commented by manish mhatre
					 siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
					if (empCodeApr != null && empCodeApr.trim().length() > 0) {
						/*
						 * Comment By Nasruddin Start 20-SEP-16 errcode =
						 * nvo_dis.gbf_employee(mval1,mval,transer) sql =
						 * "select count(*) from employee where emp_code = ?"; pstmt =
						 * conn.prepareStatement(sql); pstmt.setString(1,empCodeApr); rs =
						 * pstmt.executeQuery(); if(rs.next()) { cnt = rs.getInt(1); } if(cnt == 0) {
						 * errCode = "VMEMP1"; errList.add(errCode);
						 * errFields.add(childNodeName.toLowerCase()); } rs.close(); rs = null;
						 * pstmt.close(); pstmt = null; Comment By Nasruddin 20-SEP-16 END
						 */
						errCode = finCommon.isEmployee(siteCode, empCodeApr, "", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				/*
				 * Comment by Nasruddin [20-SEP-16] Start else
				 * if(childNodeName.equalsIgnoreCase("emp_code__pur")) { empCodePur =
				 * checkNull(genericUtility.getColumnValue("emp_code__pur", dom)); if(empCodePur
				 * != null && empCodePur.trim().length() > 0) { //errcode =
				 * nvo_dis.gbf_employee(mval1,mval,transer) sql =
				 * "select count(*) from employee where emp_code = ?"; pstmt =
				 * conn.prepareStatement(sql); pstmt.setString(1,empCodePur); rs =
				 * pstmt.executeQuery(); if(rs.next()) { cnt = rs.getInt(1); } if(cnt == 0) {
				 * errCode = "VMEMP1"; errList.add(errCode);
				 * errFields.add(childNodeName.toLowerCase()); } rs.close(); rs = null;
				 * pstmt.close(); pstmt = null; } } Comment by Nasruddin [20-SEP-16] End
				 */
				else if (childNodeName.equalsIgnoreCase("supp_code__pref")) {
					supplierCodePref = checkNull(genericUtility.getColumnValue("supp_code__pref", dom));
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));   //commented by manish mhatre
					 siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
					if (supplierCodePref != null && supplierCodePref.length() > 0) {
						// errcode = nvo_dis.gbf_supplier(mval1,mval,transer)
						sql = "select var_value  from disparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_SUPP'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							varValue = checkNull(rs.getString(1));
							if (varValue != null && varValue.equalsIgnoreCase("Y")) {
								sql2 = "select count(*) from site_supplier where site_code = ? and supp_code = ?";
								pstmt2 = conn.prepareStatement(sql2);
								pstmt2.setString(1, siteCode);
								pstmt2.setString(2, supplierCodePref);
								rs2 = pstmt2.executeQuery();
								if (rs2.next()) {
									count = rs2.getInt(1);
								}
								if (count == 0) {
									errCode = "VTSUPP2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
							} else {
								sql2 = "select count(*) from supplier where supp_code = ?";
								pstmt2 = conn.prepareStatement(sql2);
								pstmt2.setString(1, supplierCodePref);
								rs2 = pstmt2.executeQuery();
								if (rs2.next()) {
									count = rs2.getInt(1);
								}
								if (count == 0) {
									errCode = "VTSUPP1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
							}
							if (errCode == null || errCode.trim().length() == 0) {
								sql2 = "select case when  black_list is null then 'N' else black_list end  from supplier where supp_code = ?";
								pstmt2 = conn.prepareStatement(sql2);
								pstmt2.setString(1, supplierCodePref);
								rs2 = pstmt2.executeQuery();
								if (rs2.next()) {
									blackList = rs2.getString(1);

								}
								if (blackList.equalsIgnoreCase("Y")) {
									errCode = "VTSUPPBL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
							}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("role_code__qcaprv")) {
					roleCodeQcaprv = checkNull(genericUtility.getColumnValue("role_code__qcaprv", dom));
					if (roleCodeQcaprv != null && roleCodeQcaprv.trim().length() > 0) {
						// errcode = nvo_dis.gbf_rolecode(mval)
						sql = "select	count(*) from	wf_role	where	role_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, roleCodeQcaprv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VINVROLECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("role_code__indaprv")) {
					roleCodeIndaprv = checkNull(genericUtility.getColumnValue("role_code__indaprv", dom));
					if (roleCodeIndaprv != null && roleCodeIndaprv.trim().length() > 0) {
						// errcode = nvo_dis.gbf_rolecode(mval)
						sql = "select	count(*) from	wf_role	where	role_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, roleCodeIndaprv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VINVROLECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("master_sch")) {
					masterSch = checkNull(genericUtility.getColumnValue("master_sch", dom));
					if (masterSch == null || masterSch.trim().length() == 0) {
						errCode = "VMMASSCH";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				} else if (childNodeName.equalsIgnoreCase("supp_sour")) {
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour == null || suppilerSour.trim().length() == 0
							|| suppilerSour.equalsIgnoreCase("null")) {
						errCode = "VMSUPPSR";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				} else if (childNodeName.equalsIgnoreCase("site_code__supp")) {
					siteCodeSuppiler = checkNull(genericUtility.getColumnValue("site_code__supp", dom));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));    //commented by manish mhatre
					 siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
					if (siteCodeSuppiler != null && siteCodeSuppiler.trim().length() > 0) {
						sql = "select count(*)  from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeSuppiler);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VMSITE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//commented by manish mhatre on 27-dec-2019
					/*	if (siteCodeSuppiler != null && siteCodeSuppiler.equalsIgnoreCase(siteCode)) {
							errCode = "VTSUPSITE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}

					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("D")) {
						if (siteCodeSuppiler == null || suppilerSour.trim().length() == 0) {
							errCode = "VMSITSUPP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (siteCodeSuppiler != null && siteCodeSuppiler.equalsIgnoreCase(siteCode)) {
							errCode = "VTSUPSITE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (siteCodeSuppiler != null && siteCodeSuppiler.trim().length() > 0) {
							sql = "select count(*)  from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeSuppiler);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							if (cnt == 0) {
								errCode = "VMSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					//commented by manish mhatre on 27-dec-2019
				/*	if (suppilerSour != null && suppilerSour.equalsIgnoreCase("P")) {
						if (siteCodeSuppiler != null && siteCodeSuppiler.equalsIgnoreCase(siteCode)) {
							errCode = "VTSUPSITE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} else if (siteCodeSuppiler != null && siteCodeSuppiler.trim().length() > 0) {
							sql = "select count(*)  from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeSuppiler);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
								if (cnt == 0) {
									errCode = "VMSITE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}*/
				} else if (childNodeName.equalsIgnoreCase("site_code__plan")) {
					siteCodePlan = checkNull(genericUtility.getColumnValue("site_code__plan", dom));
					if (siteCodePlan != null && siteCodePlan.length() > 0) {
						sql = "select count(*) from site where  site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodePlan);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VMSITE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("site_code__ship")) {
					siteCodeShip = checkNull(genericUtility.getColumnValue("site_code__ship", dom));
					if (siteCodeShip != null && siteCodeShip.length() > 0) {
						// nvo_dis.gbf_site(mval,transer)
						sql = "select	count(*) from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeShip);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("pur_lead_time")) {
					purLeadTime = convertInt(checkNull(genericUtility.getColumnValue("pur_lead_time", dom)));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("P")) {
						if (purLeadTime <= 0) {
							errCode = "VTLIDGRZR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					/*
					 * Comment by nASRUDDIN [20-sep-16] start if(suppilerSour != null &&
					 * suppilerSour.equalsIgnoreCase("M")) { if(purLeadTime < 0) { errCode =
					 * "VTLIDLSZR"; errList.add(errCode);
					 * errFields.add(childNodeName.toLowerCase()); } }Comment by nASRUDDIN
					 * [20-sep-16] end
					 */
				} else if (childNodeName.equalsIgnoreCase("appr_supp")) {
					appSupp = checkNull(genericUtility.getColumnValue("appr_supp", dom));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour != null && suppilerSour == "P") {
						if (appSupp == null || appSupp.trim().length() == 0) {
							errCode = "VMAPRSUPP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("M")) {
						if (appSupp == null || appSupp.trim().length() == 0) {
							if (appSupp != null && appSupp.equalsIgnoreCase("Y")) {
								errCode = "VTSUPPISNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				} else if (childNodeName.equalsIgnoreCase("bom_code")) {
					bomCode = checkNull(genericUtility.getColumnValue("bom_code", dom));
					today = new Date();
					// suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					System.out.println("  bomCode ====>>>>>[" + bomCode + "]");
					if (bomCode != null && bomCode.trim().length() > 0) {
						sql = "select count(*) from bom where bom_code = ? and active = 'Y' and confirmed = 'Y' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, bomCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
							if (cnt == 0) {
								errCode = "VTBOMUCON";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else {
								// siteCode = genericUtility.getColumnValue("site_code", dom).trim();
//								siteCode = checkNull(
//										genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));   //commented by manish mhatre 
								 siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
								sql2 = "Select case when eou is null then 'N' else eou end  From site Where site_code = ?";
								pstmt2 = conn.prepareStatement(sql2);
								pstmt2.setString(1, siteCode);
								rs2 = pstmt2.executeQuery();
								if (rs2.next()) {
									eou = checkNull(rs2.getString(1));
									if (eou.equalsIgnoreCase("Y")) {
										sql3 = "Select Item_code from bom where bom_code = ?";
										pstmt3 = conn.prepareStatement(sql3);
										pstmt3.setString(1, bomCode);
										rs3 = pstmt3.executeQuery();
										if (rs3.next()) {
											itemCode = checkNull(rs3.getString(1));
											if (itemCode.trim().length() == 0 || itemCode == null) {
												itemCode = "";
												sql4 = "select b.lop_reqd from item a, itemser b where a.item_ser = b.item_ser  and a.item_code = ?";
												pstmt4 = conn.prepareStatement(sql4);
												pstmt4.setString(1, itemCode);
												rs4 = pstmt4.executeQuery();
												if (rs4.next()) {
													lopReqd = checkNull(rs4.getString(1));
													if (lopReqd.equalsIgnoreCase("Y") && itemCode.trim().length() > 0) {
														sql4 = "Select count(1)  from lop_hdr a, lop_det b where a.lop_ref_no = b.lop_ref_no and a.site_code = ? and a.confirmed = 'Y' and b.item_code = ? and b.item_status ='A' and ? >= a.valid_from and ? <= a.valid_to and b.buy_sell_flag in ('S','B')";
														pstmt5 = conn.prepareStatement(sql4);
														pstmt5.setString(1, siteCode);
														pstmt5.setString(2, itemCode);
														pstmt5.setDate(3, new java.sql.Date(today.getTime()));
														rs5 = pstmt5.executeQuery();
														if (rs5.next()) {
															cnt = rs5.getInt(1);

														}
														if (cnt == 0) {
															errCode = "VTLOPITEM1";
															errList.add(errCode);
															errFields.add(childNodeName.toLowerCase());
														}
														rs5.close();
														rs5 = null;
														pstmt5.close();
														pstmt5 = null;
													}
												}
												rs4.close();
												rs4 = null;
												pstmt4.close();
												pstmt4 = null;
											}
										}
										rs3.close();
										rs3 = null;
										pstmt3.close();
										pstmt3 = null;
									}

									sql3 = "select valid_upto from bom where bom_code = ?";
									pstmt3 = conn.prepareStatement(sql3);
									pstmt3.setString(1, bomCode);
									rs3 = pstmt3.executeQuery();
									if (rs3.next()) {
										validUpto = rs3.getDate(1);
										if (validUpto != null && validUpto.before(today)) {
											errCode = "VTBOMDT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
									rs3.close();
									rs3 = null;
									pstmt3.close();
									pstmt3 = null;
								}

								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
							}

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("mfg_perc")) {
					mfgPerc = convertInt(checkNull(genericUtility.getColumnValue("mfg_perc", dom)));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("M")) {
						if (mfgPerc == 0) {
							errCode = "VTMFGPRC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} else if (childNodeName.equalsIgnoreCase("mfg_lead_time")) {
					// mfgLeadTime =
					// Integer.parseInt(checkNull(genericUtility.getColumnValue("mfg_lead_time",
					// dom)));
					mfgLeadTime = convertInt(checkNull(genericUtility.getColumnValue("mfg_lead_time", dom)));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("M")) {
						if (mfgLeadTime == 0) {
							// Added and replace by sarita on 3rd JAN 2018 to change wrong error code
							// errCode = "VTLIDGRZR";
							errCode = "VTLIDLSZR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} else if (childNodeName.equalsIgnoreCase("loc_code__aprv")) {
					locCodeAprv = checkNull(genericUtility.getColumnValue("loc_code__aprv", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));   //commented by manish mhatre 
					 siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
					if (locCodeAprv != null && locCodeAprv.trim().length() > 0) {
						// Changed By Nasruddin [21-SEP-16] STARt
						// sql = "select case when available is null then 'Y' else available end from
						// invstat , location where invstat.inv_stat = location.inv_stat and
						// location.loc_code = ?";
						sql = "select case when a.available is null then 'Y' else a.available end, b.facility_code from   invstat	a	,  location b		WHERE  A.INV_STAT 	= B.INV_STAT	and 	 b.loc_code   = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, locCodeAprv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							avail = checkNull(rs.getString(1));
							isFaciLocCode = checkNull(rs.getString(2));
						}
						if (avail != null && avail.equalsIgnoreCase("N")) {
							errCode = "VMLOCAPRV";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (errCode == null || errCode.trim().length() == 0) {
							sql = "select FACILITY_CODE  from SITE where SITE_CODE = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								isFaciSiteCode = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (isFaciLocCode != null && isFaciLocCode.trim().length() > 0 && isFaciSiteCode != null
									&& isFaciSiteCode.trim().length() > 0) {
								if (!(isFaciLocCode).equalsIgnoreCase(isFaciLocCode)) {
									errCode = "VMFACI2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						// Changed By Nasruddin [21-SEP-16] END
					}
				} else if (childNodeName.equalsIgnoreCase("loc_code__rej")) {
					locCodeRej = checkNull(genericUtility.getColumnValue("loc_code__rej", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));   //commented by manish mhatre
					siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
					if (locCodeRej != null && locCodeRej.trim().length() > 0) {
						// Changed By Nasruddin [21-SEP-16] STARt
						// sql = "select case when available is null then 'N' else available end from
						// invstat,location where invstat.inv_stat = location.inv_stat and
						// location.loc_code =?";
						sql = "select case when a.available is null then 'N' else a.available end, b.facility_code from   invstat	a	,  location b		WHERE  A.INV_STAT 	= B.INV_STAT	and 	 b.loc_code   = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, locCodeRej);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							avail = checkNull(rs.getString(1));
							isFaciLocCode = checkNull(rs.getString(2));
							if (avail.equalsIgnoreCase("Y")) {
								errCode = "VMLOCREJ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (errCode == null || errCode.trim().length() == 0) {
							sql = "select FACILITY_CODE  from SITE where SITE_CODE = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								isFaciSiteCode = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (isFaciLocCode != null && isFaciLocCode.trim().length() > 0 && isFaciSiteCode != null
									&& isFaciSiteCode.trim().length() > 0) {
								if (!(isFaciLocCode).equalsIgnoreCase(isFaciLocCode)) {
									errCode = "VMFACI2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						// Changed By Nasruddin [21-SEP-16] END
					}
				} else if (childNodeName.equalsIgnoreCase("loc_code__insp")) {
					locCodeInsp = checkNull(genericUtility.getColumnValue("loc_code__insp", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
//					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));     //commented by manish mhatre
					siteCode = genericUtility.getColumnValue("site_code", dom);     //added by manish mhatre on 18-dec-2019
					if (locCodeInsp != null && locCodeInsp.trim().length() > 0) {
						// Changed By Nasruddin [21-SEP-16] STARt
						// sql = "select (case when available is null then 'N' else available end) from
						// invstat,location where invstat.inv_stat = location.inv_stat and
						// location.loc_code = ?";
						sql = "select case when a.available is null then 'N' else a.available end, b.facility_code from   invstat	a	,  location b		WHERE  A.INV_STAT 	= B.INV_STAT	and 	 b.loc_code   = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, locCodeInsp);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							avail = rs.getString(1);
							isFaciLocCode = rs.getString(2);
							if (avail.equalsIgnoreCase("Y")) {
								errCode = "VMLOCINSP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (errCode == null || errCode.trim().length() == 0) {
							sql = "select FACILITY_CODE  from SITE where SITE_CODE = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								isFaciSiteCode = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (isFaciLocCode != null && isFaciLocCode.trim().length() > 0 && isFaciSiteCode != null
									&& isFaciSiteCode.trim().length() > 0) {
								if (!(isFaciLocCode).equalsIgnoreCase(isFaciLocCode)) {
									errCode = "VMFACI2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						// Changed By Nasruddin [21-SEP-16] END
					}
				} else if (childNodeName.equalsIgnoreCase("qc_reqd")) {
					qcReqd = checkNull(genericUtility.getColumnValue("qc_reqd", dom));
					stkOpt = checkNull(genericUtility.getColumnValue("stk_opt", dom));
					if (qcReqd == null || qcReqd.trim().length() == 0) {
						errCode = "VTQCREQERR";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else {
						if (qcReqd != null && qcReqd.equalsIgnoreCase("Y")) {
							//if (stkOpt != null && !(stkOpt.trim().equalsIgnoreCase("2"))) { Pavan Rane 13jan20[to validate QC Reqd is 'Y' then stk opt should not '0']
							if (stkOpt != null && "0".equalsIgnoreCase(stkOpt.trim())) {
								errCode = "VMSTKOPT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				} else if (childNodeName.equalsIgnoreCase("qc_lead_time")) {
					qcLeadTime = convertInt(checkNull(genericUtility.getColumnValue("qc_lead_time", dom)));
					qcReqd = checkNull(genericUtility.getColumnValue("qc_reqd", dom));
					if (qcReqd != null && qcReqd.equalsIgnoreCase("Y")) {
						if (qcLeadTime <= 0) {
							errCode = "VMQCLTIME";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} else if (childNodeName.equalsIgnoreCase("yield_perc")) {
					// yieldPerc =
					// Integer.parseInt(checkNull(genericUtility.getColumnValue("yield_perc",
					// dom)));
					yieldPerc = convertInt(checkNull(genericUtility.getColumnValue("yield_perc", dom)));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("M")) {
						if (yieldPerc == 0) {
							errCode = "VTYELGRZR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} else if (childNodeName.equalsIgnoreCase("batch_qty_type")) {
					batchQtyType = checkNull(genericUtility.getColumnValue("batch_qty_type", dom));
					if (batchQtyType == null || batchQtyType.trim().length() < 0) {
						errCode = "VTBATQTY";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				} else if (childNodeName.equalsIgnoreCase("mfg_type")) {
					mfgType = checkNull(genericUtility.getColumnValue("mfg_type", dom));
					suppilerSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
					if (suppilerSour != null && suppilerSour.equalsIgnoreCase("M")) {
						if (mfgType == null || mfgType.trim().length() < 0) {
							errCode = "VTMFGTYP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} else if (childNodeName.equalsIgnoreCase("auto_reqc")) {
					autoReqc = checkNull(genericUtility.getColumnValue("auto_reqc", dom));
					if (autoReqc == null || autoReqc.trim().length() == 0) {
						errCode = "VMAUTORT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				} else if (childNodeName.equalsIgnoreCase("order_opt")) {
					orderOpt = checkNull(genericUtility.getColumnValue("order_opt", dom));
					if (orderOpt == null || orderOpt.trim().length() == 0 || orderOpt.equalsIgnoreCase("null")) {
						System.out.println("order opt null");
						errCode = "VMORDOPT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}

				} else if (childNodeName.equalsIgnoreCase("stk_opt")) {
					stkOpt = checkNull(genericUtility.getColumnValue("stk_opt", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					sql = "select qc_reqd_type from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						qcReqdType = checkNull(rs.getString(1));
						if (stkOpt == null || stkOpt.trim().length() == 0) {
							/*
							 * errCode = "VMSTKOPT"; errList.add(errCode);
							 * errFields.add(childNodeName.toLowerCase());
							 */
						} else {
							if (stkOpt != null && stkOpt.trim().equalsIgnoreCase("1")) {
								if (!(qcReqdType.equalsIgnoreCase("I"))) {
									errCode = "VIWRSTKOPT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else if (stkOpt != null && stkOpt.trim().equalsIgnoreCase("2")) {
								if (!(qcReqdType.equalsIgnoreCase("L")) && !(qcReqdType.equalsIgnoreCase("S"))
										&& !(qcReqdType.equalsIgnoreCase("U"))) {
									errCode = "VIWRSTKOPT";
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
				} else if (childNodeName.equalsIgnoreCase("pack_code")) {
					packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
					if (packCode != null && packCode.trim().length() > 0) {
						sql = "select count(*)  from packing where pack_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, packCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VMPACKCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} else if (childNodeName.equalsIgnoreCase("purc_order")) {
					purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
					if (purcOrder != null && purcOrder.trim().length() > 0) {
						sql = "select count(1) from porder where purc_order = ? and	(case when confirmed is null then 'N' else confirmed end) = 'Y' and	status = 'O' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VTPURCORD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				/*
				 * Changed By Nasruddin Start [21-SEP-16] Start else
				 * if(childNodeName.equalsIgnoreCase("tax_chap")) { taxChap =
				 * checkNull(genericUtility.getColumnValue("tax_chap", dom));
				 * System.out.println("1197 tax_chap="+taxChap); if(taxChap != null &&
				 * taxChap.trim().length() > 0) { sql =
				 * "select count(*) from taxchap where tax_chap = ?"; pstmt =
				 * conn.prepareStatement(sql); pstmt.setString(1,taxChap); rs =
				 * pstmt.executeQuery(); if(rs.next()) { cnt = rs.getInt(1); } if(cnt == 0) {
				 * errCode = "VTTAXCHAP1"; errList.add(errCode);
				 * errFields.add(childNodeName.toLowerCase()); } rs.close(); rs = null;
				 * pstmt.close(); pstmt = null; } } else
				 * if(childNodeName.equalsIgnoreCase("item_ser__rg1")) { rg1Ser =
				 * checkNull(genericUtility.getColumnValue("item_ser__rg1", dom)); if(rg1Ser !=
				 * null && rg1Ser.trim().length() > 0) { sql =
				 * "select count(*)  from itemser where item_ser = ?"; pstmt =
				 * conn.prepareStatement(sql); pstmt.setString(1,rg1Ser); rs =
				 * pstmt.executeQuery(); if(rs.next()) { cnt = rs.getInt(1); } if(cnt == 0) {
				 * errCode = "VTRGSERV"; errList.add(errCode);
				 * errFields.add(childNodeName.toLowerCase()); } rs.close(); rs = null;
				 * pstmt.close(); pstmt = null; } }
				 */
				else if (childNodeName.equalsIgnoreCase("lock_group")) {
					lockGroup = checkNull(genericUtility.getColumnValue("lock_group", dom));
					if (lockGroup != null && lockGroup.trim().length() > 0) {
						sql = "SELECT COUNT(1)  FROM LOCK_GROUP WHERE LOCK_GROUP = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lockGroup);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VTLOCGRP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				// Changed By Nasruddin 21-SEP-16 END

				// added by manish mhatre on 9-sep-2019
				// start manish
				else if (childNodeName.equalsIgnoreCase("shelf_life_type")) {
					System.out.println("Inside SHELF_LIFE_TYPE");
					shelfLifeType = checkNull(genericUtility.getColumnValue("shelf_life_type", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));

					if (shelfLifeType != null && shelfLifeType.trim().length() > 0) {
						sql = "SELECT SHELF_LIFE__TYPE FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							itemShelfLifeType = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (!(itemShelfLifeType).equalsIgnoreCase(shelfLifeType)) {
							errCode = "VTSHELF02";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				// end manish
			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) {
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext,
			String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [SiteItemIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException {
		String packCode = "";
		String descr = "";
		String apprSupp = "";
		String disapprvOnReg = "";
		String mfgType = "";
		String supplierSour = "";
		String siteCode = "";
		String itemCode = "";
		String itemSer = "";
		String minQty = "";
		String maxQty = "";
		String minOrderQty = "";
		String maxOrderQty = "";
		String reoQty = "";
		String reoLev = "";
		String qcLead = "";
		String mfgLead = "";
		String purLead = "";
		String stkOpt = "";
		String specReqd = "";
		String bomCode = "";
		String empCodePln = "";
		String regPrice = "";
		String binNo = "";
		String cycleCount = "";
		String packInstr = "";
		String poVal = "";
		String tolBef = "";
		String tolAft = "";
		String qtyTol = "";
		String yield = "";
		String orderOpt = "";
		String inteQty = "";
		String qcReqd = "";
		String autoReqc = "";
		String techDescr = "";
		String varValue = "";
		String masterSch = "";
		String stkOptIFlg = "";
		String stkOptSFlg = "";
		String suppSourIFlg = "";
		String suppSourSFlg = "";
		String mfgTypeIFlg = "";
		String mfgTypeSFlg = "";
		String qcReqdIFlg = "";
		String qcReqdSFlg = "";
		String qcReqdType = "";
		String qcReqdTypeIFlg = "";
		String qcReqdTypeSFlg = "";
		String yieldPerc = "";
		String yieldPercIFlg = "";
		String yieldPercSFlg = "";
		String potencyPerc = "";
		String potencyPercSFlg = "";
		String potencyPercIFlg = "";
		String trackSLife = "";
		String trackSLifeIFlg = "";
		String trackSLifeSFlg = "";
		String shelfLife = "";
		String shelfLifeIFlg = "";
		String shelfLifeSFlg = "";
		String minSLife = "";
		String minSLifeIFlg = "";
		String minSLifeSFlg = "";
		String scanBar = "";
		String scanBarIFlg = "";
		String scanBarSFlg = "";
		String indentOpt = "";
		String indentOptIFlg = "";
		String indentOptSFlg = "";
		String issCrit = "";
		String issCritIFlg = "";
		String issCritSFlg = "";
		String empCodeIapr = "";
		String empCodeIaprIFlg = "";
		String empCodeIaprSFlg = "";
		String roleCodeIndapr = "";
		String roleCodeIndaprIFlg = "";
		String roleCodeIndaprSFlg = "";
		String empCodeQcapr = "";
		String empCodeQcaprIFlg = "";
		String empCodeQcaprSFlg = "";
		String roleCodeQcapr = "";
		String roleCodeQcaprIFlg = "";
		String roleCodeQcaprSFlg = "";
		String type = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// changed by nasruddin 05-10-16
		// GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try {
			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
			conn.setAutoCommit(false);
			connDriver = null;
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			valueXmlString.append("<Detail1>");
			int childNodeListLength = childNodeList.getLength();
			do {
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				ctr++;
			} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));
			if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))// Start
			{
				apprSupp = checkNull(genericUtility.getColumnValue("appr_supp", dom));
				System.out.println("App Supp = " + apprSupp);
				if (apprSupp != null && apprSupp.equalsIgnoreCase("Y")) {
					valueXmlString.append("<no_cons_unaprv protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_cons_unaprv>");
					valueXmlString.append("<disapprv_on_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</disapprv_on_rej>");
					disapprvOnReg = checkNull(genericUtility.getColumnValue("disapprv_on_rej", dom));
					if (disapprvOnReg != null && disapprvOnReg.equalsIgnoreCase("Y")) {
						valueXmlString.append("<no_of_rej protect = \"0\">").append("<![CDATA[" + "" + "]]>")
								.append("</no_of_rej>");
					} else {
						valueXmlString.append("<no_of_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
								.append("</no_of_rej>");
					}
				} else {
					valueXmlString.append("<no_cons_unaprv protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_cons_unaprv>");
					valueXmlString.append("<disapprv_on_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</disapprv_on_rej>");
					valueXmlString.append("<no_of_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_of_rej>");
				}
				mfgType = checkNull(genericUtility.getColumnValue("mfg_type", dom));
				supplierSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
				String stockOpt = checkNull(genericUtility.getColumnValue("stk_opt", dom));
				if (mfgType != null && mfgType.trim().equalsIgnoreCase("D") && supplierSour != null
						&& supplierSour.trim().equalsIgnoreCase("M")) {
					valueXmlString.append("<extra_mfg_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
							.append("</extra_mfg_perc>");
				} else {
					valueXmlString.append("<extra_mfg_perc protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</extra_mfg_perc>");
					valueXmlString.append("<extra_mfg_perc>").append("<![CDATA[" + 0 + "]]>")
							.append("</extra_mfg_perc>");
				}
				valueXmlString.append("<stk_opt protect = \"1\">").append("<![CDATA[" + stockOpt + "]]>")
						.append("</stk_opt>");
				// valueXmlString.append("<tech_descr protect = \"1\">").append("<![CDATA[" + ""
				// + "]]>").append ("</tech_descr>");
			} else if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
				// siteCode = genericUtility.getColumnValue("site_code", dom);
				siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
				valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");
				valueXmlString.append("<site_code__plan>").append("<![CDATA[" + siteCode + "]]>")
						.append("</site_code__plan>");
				valueXmlString.append("<no_cons_unaprv protect = \"1\">").append("<![CDATA[" + "" + "]]>")
						.append("</no_cons_unaprv>");
				valueXmlString.append("<disapprv_on_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
						.append("</disapprv_on_rej>");
				valueXmlString.append("<no_of_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
						.append("</no_of_rej>");
				mfgType = checkNull(genericUtility.getColumnValue("mfg_type", dom));

				supplierSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
				if (mfgType != null && mfgType.trim().equalsIgnoreCase("D") && supplierSour != null
						&& supplierSour.trim().equalsIgnoreCase("M")) {
					valueXmlString.append("<extra_mfg_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
							.append("</extra_mfg_perc>");
				} else {
					valueXmlString.append("<extra_mfg_perc protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</extra_mfg_perc>");
					valueXmlString.append("<extra_mfg_perc>").append("<![CDATA[" + 0 + "]]>")
							.append("</extra_mfg_perc>");
				}
			} else if (currentColumn.trim().equalsIgnoreCase("item_code")) {
				itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
				if (itemCode != null && itemCode.trim().length() > 0) {
					sql = "Select descr," + "(case when min_qty is null then 0 else min_qty end)as min_qty,"
							+ "(case when max_qty is null then 0 else max_qty end)as max_qty,"
							+ "(case when min_order_qty is null then 0 else min_order_qty end) as min_order,"
							+ "(case when max_order_qty is null then 0 else max_order_qty end) as max_order,"
							+ "(case when reo_qty is null then 0 else reo_qty end) as reo_qty,"
							+ "(case when reo_lev is null then 0 else reo_lev end) as reo_lev,"
							+ "(case when qc_lead_time is null then 0 else qc_lead_time end)as qc_lead_time,"
							+ "(case when mfg_lead is null then 0 else mfg_lead end) as mfg_lead,"
							+ "(case when pur_lead_time is null then 0 else pur_lead_time end)as pur_lead,"
							+ "stk_opt,appr_supp,spec_reqd, bom_code,emp_code__pln,regulated_price,supp_sour,bin_no,"
							+ "cycle_count, pack_instr, poval_var,dlv_prd_tol_bef,dlv_prd_tol_aft,qty_tol_perc,item_ser,"
							+ "(case when yield_perc is null then 0 else yield_perc end) as yield_perc,"
							+ "order_opt ,item_ser,integral_qty, pack_code , qc_reqd, auto_reqc ,tech_descr "
							+ "from 	item " + "where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
						minQty = rs.getString("min_qty");
						maxQty = rs.getString("max_qty");
						minOrderQty = rs.getString("min_order");
						maxOrderQty = rs.getString("max_order");
						reoQty = rs.getString("reo_qty");
						reoLev = rs.getString("reo_lev");
						qcLead = rs.getString("qc_lead_time");
						mfgLead = rs.getString("mfg_lead");
						purLead = rs.getString("pur_lead");
						stkOpt = rs.getString("stk_opt");
						apprSupp = checkNull(rs.getString("appr_supp"));
						specReqd = checkNull(rs.getString("spec_reqd"));
						bomCode = checkNull(rs.getString("bom_code"));
						empCodePln = checkNull(rs.getString("emp_code__pln"));
						regPrice = checkNull(rs.getString("regulated_price"));
						supplierSour = checkNull(rs.getString("supp_sour"));
						binNo = checkNull(rs.getString("bin_no"));
						cycleCount = checkNull(rs.getString("cycle_count"));
						packInstr = checkNull(rs.getString("pack_instr"));
						poVal = checkNull(rs.getString("poval_var"));
						tolBef = checkNull(rs.getString("dlv_prd_tol_bef"));
						tolAft = checkNull(rs.getString("dlv_prd_tol_aft"));
						qtyTol = checkNull(rs.getString("qty_tol_perc"));
						itemSer = rs.getString("item_ser");
						yield = rs.getString("yield_perc");
						orderOpt = checkNull(rs.getString("order_opt"));
						// itemSer = rs.getString("item_ser");//change
						inteQty = rs.getString("integral_qty");
						packCode = checkNull(rs.getString("pack_code"));
						qcReqd = rs.getString("qc_reqd");
						autoReqc = rs.getString("auto_reqc");// change
						techDescr = rs.getString("tech_descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
				valueXmlString.append("<min_qty>").append("<![CDATA[" + minQty + "]]>").append("</min_qty>");
				valueXmlString.append("<max_qty>").append("<![CDATA[" + maxQty + "]]>").append("</max_qty>");
				valueXmlString.append("<min_order_qty>").append("<![CDATA[" + minOrderQty + "]]>")
						.append("</min_order_qty>");
				valueXmlString.append("<max_order_qty>").append("<![CDATA[" + maxOrderQty + "]]>")
						.append("</max_order_qty>");
				valueXmlString.append("<reo_qty>").append("<![CDATA[" + reoQty + "]]>").append("</reo_qty>");
				valueXmlString.append("<reo_lev>").append("<![CDATA[" + reoLev + "]]>").append("</reo_lev>");
				valueXmlString.append("<qc_lead_time>").append("<![CDATA[" + qcLead + "]]>").append("</qc_lead_time>");
				valueXmlString.append("<pur_lead_time>").append("<![CDATA[" + purLead + "]]>")
						.append("</pur_lead_time>");
				valueXmlString.append("<mfg_lead_time>").append("<![CDATA[" + mfgLead + "]]>")
						.append("</mfg_lead_time>");
				valueXmlString.append("<stk_opt>").append("<![CDATA[" + stkOpt + "]]>").append("</stk_opt>");
				valueXmlString.append("<appr_supp>").append("<![CDATA[" + apprSupp + "]]>").append("</appr_supp>");
				valueXmlString.append("<spec_reqd>").append("<![CDATA[" + specReqd + "]]>").append("</spec_reqd>");
				valueXmlString.append("<bom_code>").append("<![CDATA[" + bomCode + "]]>").append("</bom_code>");
				valueXmlString.append("<emp_code__pln>").append("<![CDATA[" + empCodePln + "]]>")
						.append("</emp_code__pln>");
				valueXmlString.append("<regulated_price>").append("<![CDATA[" + regPrice + "]]>")
						.append("</regulated_price>");
				valueXmlString.append("<supp_sour>").append("<![CDATA[" + supplierSour + "]]>").append("</supp_sour>");
				valueXmlString.append("<bin_no>").append("<![CDATA[" + binNo + "]]>").append("</bin_no>");
				valueXmlString.append("<cycle_count>").append("<![CDATA[" + cycleCount + "]]>")
						.append("</cycle_count>");
				valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr>");
				valueXmlString.append("<yield_perc>").append("<![CDATA[" + yield + "]]>").append("</yield_perc>");
				valueXmlString.append("<order_opt>").append("<![CDATA[" + orderOpt + "]]>").append("</order_opt>");
				valueXmlString.append("<poval_var>").append("<![CDATA[" + poVal + "]]>").append("</poval_var>");
				valueXmlString.append("<dlv_prd_tol_bef>").append("<![CDATA[" + tolBef + "]]>")
						.append("</dlv_prd_tol_bef>");
				valueXmlString.append("<dlv_prd_tol_aft>").append("<![CDATA[" + tolAft + "]]>")
						.append("</dlv_prd_tol_aft>");
				valueXmlString.append("<qty_var_perc>").append("<![CDATA[" + qtyTol + "]]>").append("</qty_var_perc>");
				valueXmlString.append("<master_sch>").append("<![CDATA[N]]>").append("</master_sch>");
				valueXmlString.append("<integral_qty>").append("<![CDATA[" + inteQty + "]]>").append("</integral_qty>");
				valueXmlString.append("<pack_code>").append("<![CDATA[" + packCode + "]]>").append("</pack_code>");
				valueXmlString.append("<qc_reqd>").append("<![CDATA[" + qcReqd + "]]>").append("</qc_reqd>");
				valueXmlString.append("<auto_reqc>").append("<![CDATA[" + autoReqc + "]]>").append("</auto_reqc>");
				valueXmlString.append("<stk_opt protect = \"1\">").append("<![CDATA[" + stkOpt + "]]>")
						.append("</stk_opt>");
				// valueXmlString.append("<tech_descr>").append("<![CDATA[" + techDescr
				// +"]]>").append("</tech_descr>");
				valueXmlString.append("<tech_descr protect = \"1\">").append("<![CDATA[" + techDescr + "]]>")
						.append("</tech_descr>");

				sql = "select	var_value from disparm where	var_name='LOC_CODE__APRV' AND PRD_CODE='999999' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					varValue = checkNull(rs.getString(1));
					valueXmlString.append("<loc_code__aprv>").append("<![CDATA[" + varValue + "]]>")
							.append("</loc_code__aprv>");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = "select	var_value from disparm where	var_name='LOC_CODE__REJ' AND PRD_CODE='999999' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					varValue = checkNull(rs.getString(1));
					valueXmlString.append("<loc_code__rej>").append("<![CDATA[" + varValue + "]]>")
							.append("</loc_code__rej>");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = "select	var_value from disparm where	var_name='LOC_CODE__INSP' AND PRD_CODE='999999' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					varValue = checkNull(rs.getString(1));
					valueXmlString.append("<loc_code__insp>").append("<![CDATA[" + varValue + "]]>")
							.append("</loc_code__insp>");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} else if (currentColumn.trim().equalsIgnoreCase("item_ser")) {
				itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
				System.out.println("Item Ser=" + itemSer);
				// gbf_itemser_rules(dw_edit,'siteitem',mcode)
				sql = "Select 	stk_opt,stk_opt_iflg,stk_opt_sflg,supp_sour,supp_sour_iflg,"
						+ "supp_sour_sflg,mfg_type,mfg_type_iflg,mfg_type_sflg,qc_reqd,"
						+ "qc_reqd_iflg,qc_reqd_sflg,qc_reqd_type,qc_reqd_type_iflg,"
						+ "qc_reqd_type_sflg,yield_perc,yield_perc_iflg,yield_perc_sflg,"
						+ "potency_perc,potency_perc_iflg,potency_perc_sflg,track_s_life,"
						+ "track_s_life_iflg,track_s_life_sflg,shelf_life,shelf_life_iflg,"
						+ "shelf_life_sflg,min_s_life,min_s_life_iflg,min_s_life_sflg,"
						+ "scan_bar,scan_bar_iflg,scan_bar_sflg,indent_opt,indent_opt_iflg,"
						+ "indent_opt_sflg,iss_crit,iss_crit_iflg,iss_crit_sflg,emp_code__iapr,"
						+ "emp__iapr_iflg,emp__iapr_sflg,role_code__indapr,role__indapr_iflg,"
						+ "role__indapr_sflg,emp_code__qcapr,emp__qcapr_iflg,emp__qcapr_sflg,"
						+ "role_code__qcapr,role__qcapr_iflg,role__qcapr_sflg " + "From	itemser Where item_ser = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemSer);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					stkOpt = rs.getString("stk_opt");
					stkOptSFlg = rs.getString("stk_opt_iflg");
					stkOptSFlg = rs.getString("stk_opt_sflg");
					supplierSour = checkNull(rs.getString("supp_sour"));
					suppSourIFlg = rs.getString("supp_sour_iflg");
					suppSourSFlg = rs.getString("supp_sour_sflg");
					mfgType = checkNull(rs.getString("mfg_type"));
					mfgTypeIFlg = rs.getString("mfg_type_iflg");
					mfgTypeSFlg = rs.getString("mfg_type_sflg");
					qcReqd = checkNull(rs.getString("qc_reqd"));
					qcReqdIFlg = rs.getString("qc_reqd_iflg");
					qcReqdSFlg = rs.getString("qc_reqd_sflg");
					qcReqdType = rs.getString("qc_reqd_type");
					qcReqdTypeIFlg = rs.getString("qc_reqd_type_iflg");
					qcReqdTypeSFlg = rs.getString("qc_reqd_type_sflg");
					yieldPerc = rs.getString("yield_perc");
					yieldPercIFlg = rs.getString("yield_perc_iflg");
					yieldPercSFlg = rs.getString("yield_perc_sflg");
					potencyPerc = rs.getString("potency_perc");
					potencyPercIFlg = rs.getString("potency_perc_iflg");
					potencyPercSFlg = rs.getString("potency_perc_sflg");
					trackSLife = rs.getString("track_s_life");
					trackSLifeIFlg = rs.getString("track_s_life_iflg");
					trackSLifeSFlg = rs.getString("track_s_life_sflg");
					shelfLife = rs.getString("shelf_life");
					shelfLifeIFlg = rs.getString("shelf_life_iflg");
					shelfLifeSFlg = rs.getString("shelf_life_sflg");
					minSLife = rs.getString("min_s_life");
					minSLifeIFlg = rs.getString("min_s_life_iflg");
					minSLifeSFlg = rs.getString("min_s_life_sflg");
					scanBar = rs.getString("scan_bar");
					scanBarIFlg = rs.getString("scan_bar_iflg");
					scanBarSFlg = rs.getString("scan_bar_sflg");
					indentOpt = rs.getString("indent_opt");
					indentOptIFlg = rs.getString("indent_opt_iflg");
					indentOptSFlg = rs.getString("indent_opt_sflg");
					issCrit = rs.getString("iss_crit");
					issCritIFlg = rs.getString("iss_crit_iflg");
					issCritSFlg = rs.getString("iss_crit_sflg");
					empCodeIapr = rs.getString("emp_code__iapr");
					empCodeIaprIFlg = rs.getString("emp__iapr_iflg");
					empCodeIaprSFlg = rs.getString("emp__iapr_sflg");
					roleCodeIndapr = rs.getString("role_code__indapr");
					roleCodeIndaprIFlg = rs.getString("role__indapr_iflg");
					roleCodeIndaprSFlg = rs.getString("role__indapr_sflg");
					empCodeQcapr = rs.getString("emp_code__qcapr");
					empCodeQcaprIFlg = rs.getString("emp__qcapr_iflg");
					empCodeQcaprSFlg = rs.getString("emp__qcapr_sflg");
					roleCodeQcapr = rs.getString("role_code__qcapr");
					roleCodeQcaprIFlg = rs.getString("role__qcapr_iflg");
					roleCodeQcaprSFlg = rs.getString("role__qcapr_sflg");
					if (type != null && type.equalsIgnoreCase("item")) {
						if (stkOptIFlg != null && stkOptIFlg.equals("2")) {
							valueXmlString.append("<stk_opt>").append("<![CDATA[" + stkOpt + "]]>")
									.append("</stk_opt>");
							valueXmlString.append("<stk_opt protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</stk_opt>");
						} else if (stkOptIFlg != null && stkOptIFlg.equals("0")) {
							valueXmlString.append("<stk_opt>").append("<![CDATA[" + stkOpt + "]]>")
									.append("</stk_opt>");
							valueXmlString.append("<stk_opt protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</stk_opt>");
						} else {
							valueXmlString.append("<stk_opt protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</stk_opt>");
						}
						if (suppSourIFlg != null && suppSourIFlg.equals("2")) {
							valueXmlString.append("<supp_sour>").append("<![CDATA[" + supplierSour + "]]>")
									.append("</supp_sour>");
							valueXmlString.append("<supp_sour protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</supp_sour>");
						} else if (suppSourIFlg != null && suppSourIFlg == "0") {
							valueXmlString.append("<supp_sour>").append("<![CDATA[" + mfgType + "]]>")
									.append("</supp_sour>");
							valueXmlString.append("<supp_sour protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</supp_sour>");
						} else {
							valueXmlString.append("<supp_sour protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</supp_sour>");
						}
						if (mfgTypeIFlg != null && mfgTypeIFlg.equals("2")) {
							valueXmlString.append("<mfg_type>").append("<![CDATA[" + mfgType + "]]>")
									.append("</mfg_type>");
							valueXmlString.append("<mfg_type protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</mfg_type>");
						} else if (mfgTypeIFlg != null && mfgTypeIFlg.equals("0")) {
							valueXmlString.append("<mfg_type>").append("<![CDATA[" + mfgType + "]]>")
									.append("</mfg_type>");
							valueXmlString.append("<mfg_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</mfg_type>");
						} else {
							valueXmlString.append("<mfg_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</mfg_type>");
						}
						if (qcReqdIFlg != null && qcReqdIFlg.equals("2")) {
							valueXmlString.append("<qc_reqd>").append("<![CDATA[" + qcReqd + "]]>")
									.append("</qc_reqd>");
							valueXmlString.append("<qc_reqd protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd>");
						} else if (qcReqdIFlg != null && qcReqdIFlg == "0") {
							valueXmlString.append("<qc_reqd>").append("<![CDATA[" + qcReqd + "]]>")
									.append("</qc_reqd>");
							valueXmlString.append("<qc_reqd protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd>");
						} else {
							valueXmlString.append("<qc_reqd protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd>");
						}
						if (qcReqdTypeIFlg != null && qcReqdTypeIFlg.equals("2")) {
							valueXmlString.append("<qc_reqd_type>").append("<![CDATA[" + qcReqdType + "]]>")
									.append("</qc_reqd_type>");
							valueXmlString.append("<qc_reqd_type protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd_type>");
						} else if (qcReqdTypeIFlg != null && qcReqdTypeIFlg.equals("0")) {
							valueXmlString.append("<qc_reqd_type>").append("<![CDATA[" + qcReqdType + "]]>")
									.append("</qc_reqd_type>");
							valueXmlString.append("<qc_reqd_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd_type>");
						} else {
							valueXmlString.append("<qc_reqd_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd_type>");
						}
						if (yieldPercIFlg != null && yieldPercIFlg.equals("2")) {
							valueXmlString.append("<yield_perc>").append("<![CDATA[" + yieldPerc + "]]>")
									.append("</yield_perc>");
							valueXmlString.append("<yield_perc protect = \"1\">").append("</yield_perc>");
						} else if (yieldPercIFlg != null && yieldPercIFlg.equals("0")) {
							valueXmlString.append("<yield_perc>").append("<![CDATA[" + yieldPerc + "]]>")
									.append("</yield_perc>");
							valueXmlString.append("<yield_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</yield_perc>");
						} else {
							valueXmlString.append("<yield_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</yield_perc>");
						}
						if (potencyPercIFlg != null && potencyPercIFlg.equals("2")) {
							valueXmlString.append("<potency_perc>").append("<![CDATA[" + potencyPerc + "]]>")
									.append("</potency_perc>");
							valueXmlString.append("<potency_perc protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</potency_perc>");
						} else if (potencyPercIFlg != null && potencyPercIFlg.equals("0")) {
							valueXmlString.append("<potency_perc>").append("<![CDATA[" + potencyPerc + "]]>")
									.append("</potency_perc>");
							valueXmlString.append("<potency_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</potency_perc>");
						} else {
							valueXmlString.append("<potency_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</potency_perc>");
						}
						if (trackSLifeIFlg != null && trackSLifeIFlg.equals("2")) {
							valueXmlString.append("<track_shelf_life>").append("<![CDATA[" + trackSLife + "]]>")
									.append("</track_shelf_life>");
							valueXmlString.append("<track_shelf_life protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</track_shelf_life>");
						} else if (trackSLifeIFlg != null && trackSLifeIFlg.equals("0")) {
							valueXmlString.append("<track_shelf_life>").append("<![CDATA[" + trackSLife + "]]>")
									.append("</track_shelf_life>");
							valueXmlString.append("<track_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</track_shelf_life>");
						} else {
							valueXmlString.append("<track_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</track_shelf_life>");
						}
						if (minSLifeIFlg != null && minSLifeIFlg.equals("2")) {
							valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minSLife + "]]>")
									.append("</min_shelf_life>");
							valueXmlString.append("<min_shelf_life protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</min_shelf_life>");
						} else if (minSLifeIFlg != null && minSLifeIFlg.equals("0")) {
							valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minSLife + "]]>")
									.append("</min_shelf_life>");
							valueXmlString.append("<min_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</min_shelf_life>");
						} else {
							valueXmlString.append("<min_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</min_shelf_life>");
						}
						if (shelfLifeIFlg != null && shelfLifeIFlg.equals("2")) {
							valueXmlString.append("<shelf_life>").append("<![CDATA[" + shelfLife + "]]>")
									.append("</shelf_life>");
							valueXmlString.append("<shelf_life protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</shelf_life>");
						} else if (shelfLifeIFlg != null && shelfLifeIFlg.equals("0")) {
							valueXmlString.append("<shelf_life>").append("<![CDATA[" + shelfLife + "]]>")
									.append("</shelf_life>");
							valueXmlString.append("<shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</shelf_life>");
						} else {
							valueXmlString.append("<shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</shelf_life>");
						}
						if (scanBarIFlg != null && scanBarIFlg.equals("2")) {
							valueXmlString.append("<scanned_barcode>").append("<![CDATA[" + scanBar + "]]>")
									.append("</scanned_barcode>");
							valueXmlString.append("<scanned_barcode protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</scanned_barcode>");
						} else if (scanBarIFlg != null && scanBarIFlg.equals("0")) {
							valueXmlString.append("<scanned_barcode>").append("<![CDATA[" + scanBar + "]]>")
									.append("</scanned_barcode>");
							valueXmlString.append("<scanned_barcode protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</scanned_barcode>");
						} else {
							valueXmlString.append("<scanned_barcode = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</scanned_barcode>");
						}
						if (indentOptIFlg != null && indentOptIFlg.equals("2")) {
							valueXmlString.append("<indent_opt>").append("<![CDATA[" + indentOpt + "]]>")
									.append("</indent_opt>");
							valueXmlString.append("<indent_opt protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</indent_opt>");
						} else if (indentOptIFlg != null && indentOptIFlg.equals("0")) {
							valueXmlString.append("<indent_opt>").append("<![CDATA[" + indentOpt + "]]>")
									.append("</indent_opt>");
							valueXmlString.append("<indent_opt protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</indent_opt>");
						} else {
							valueXmlString.append("<indent_opt = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</indent_opt>");
						}
						if (issCritIFlg != null && issCritIFlg.equals("2")) {
							valueXmlString.append("<iss_criteria>").append("<![CDATA[" + issCrit + "]]>")
									.append("</iss_criteria>");
							valueXmlString.append("<iss_criteria protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</iss_criteria>");
						} else if (issCritIFlg != null && issCritIFlg.equals("0")) {
							valueXmlString.append("<iss_criteria>").append("<![CDATA[" + issCrit + "]]>")
									.append("</iss_criteria>");
							valueXmlString.append("<iss_criteria protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</iss_criteria>");
						} else {
							valueXmlString.append("<iss_criteria = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</iss_criteria>");
						}
						if (empCodeIaprIFlg != null && empCodeIaprIFlg.equals("2")) {
							valueXmlString.append("<emp_code__iapr>").append("<![CDATA[" + empCodeIapr + "]]>")
									.append("</emp_code__iapr>");
							valueXmlString.append("<emp_code__iapr protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__iapr>");
						} else if (empCodeIaprIFlg != null && empCodeIaprIFlg.equals("0")) {
							valueXmlString.append("<emp_code__iapr>").append("<![CDATA[" + empCodeIapr + "]]>")
									.append("</emp_code__iapr>");
							valueXmlString.append("<emp_code__iapr protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__iapr>");
						} else {
							valueXmlString.append("<emp_code__iapr = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__iapr>");
						}
						if (roleCodeIndaprIFlg != null && roleCodeIndaprIFlg.equals("2")) {
							valueXmlString.append("<role_code__indaprv>").append("<![CDATA[" + roleCodeIndapr + "]]>")
									.append("</role_code__indaprv>");
							valueXmlString.append("<role_code__indaprv protect = \"1\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__indaprv>");
						} else if (roleCodeIndaprIFlg != null && roleCodeIndaprIFlg.equals("0")) {
							valueXmlString.append("<role_code__indaprv>").append("<![CDATA[" + roleCodeIndapr + "]]>")
									.append("</role_code__indaprv>");
							valueXmlString.append("<role_code__indaprv protect = \"0\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__indaprv>");
						} else {
							valueXmlString.append("<role_code__indaprv = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</role_code__indaprv>");
						}
						if (empCodeQcaprIFlg != null && empCodeQcaprIFlg.equals("2")) {
							valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeQcapr + "]]>")
									.append("</emp_code__qcaprv>");
							valueXmlString.append("<emp_code__qcaprv protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__qcaprv>");
						} else if (empCodeQcaprIFlg != null && empCodeQcaprIFlg.equals("0")) {
							valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeQcapr + "]]>")
									.append("</emp_code__qcaprv>");
							valueXmlString.append("<emp_code__qcaprv protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__qcaprv>");
						} else {
							valueXmlString.append("<emp_code__qcaprv = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__qcaprv>");
						}
						if (roleCodeQcaprIFlg != null && roleCodeQcaprIFlg.equals("2")) {
							valueXmlString.append("<role_code__qcaprv>").append("<![CDATA[" + roleCodeQcapr + "]]>")
									.append("</role_code__qcaprv>");
							valueXmlString.append("<role_code__qcaprv protect = \"1\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__qcaprv>");
						} else if (roleCodeIndaprIFlg != null && roleCodeQcaprIFlg.equals("0")) {
							valueXmlString.append("<role_code__qcaprv>").append("<![CDATA[" + roleCodeQcapr + "]]>")
									.append("</role_code__qcaprv>");
							valueXmlString.append("<role_code__qcaprv protect = \"0\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__qcaprv>");
						} else {
							valueXmlString.append("<role_code__qcaprv = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</role_code__qcaprv>");
						}
					}
					if (type != null && type.equalsIgnoreCase("siteitem")) {
						if (stkOptSFlg == "2") {
							valueXmlString.append("<stk_opt>").append("<![CDATA[" + stkOpt + "]]>")
									.append("</stk_opt>");
							valueXmlString.append("<stk_opt protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</stk_opt>");
						} else if (stkOptSFlg == "0") {
							valueXmlString.append("<stk_opt>").append("<![CDATA[" + stkOpt + "]]>")
									.append("</stk_opt>");
							valueXmlString.append("<stk_opt protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</stk_opt>");
						} else {
							valueXmlString.append("<stk_opt protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</stk_opt>");
						}
						if (suppSourSFlg == "2") {
							valueXmlString.append("<supp_sour>").append("<![CDATA[" + supplierSour + "]]>")
									.append("</supp_sour>");
							valueXmlString.append("<supp_sour protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</supp_sour>");
						} else if (suppSourSFlg == "0") {
							valueXmlString.append("<supp_sour>").append("<![CDATA[" + mfgType + "]]>")
									.append("</supp_sour>");
							valueXmlString.append("<supp_sour protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</supp_sour>");
						} else {
							valueXmlString.append("<supp_sour protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</supp_sour>");
						}
						if (mfgTypeSFlg == "2") {
							valueXmlString.append("<mfg_type>").append("<![CDATA[" + mfgType + "]]>")
									.append("</mfg_type>");
							valueXmlString.append("<mfg_type protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</mfg_type>");
						} else if (mfgTypeSFlg == "0") {
							valueXmlString.append("<mfg_type>").append("<![CDATA[" + mfgType + "]]>")
									.append("</mfg_type>");
							valueXmlString.append("<mfg_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</mfg_type>");
						} else {
							valueXmlString.append("<mfg_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</mfg_type>");
						}
						if (qcReqdSFlg == "2") {
							valueXmlString.append("<qc_reqd>").append("<![CDATA[" + qcReqd + "]]>")
									.append("</qc_reqd>");
							valueXmlString.append("<qc_reqd protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd>");
						} else if (qcReqdSFlg == "0") {
							valueXmlString.append("<qc_reqd>").append("<![CDATA[" + qcReqd + "]]>")
									.append("</qc_reqd>");
							valueXmlString.append("<qc_reqd protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd>");
						} else {
							valueXmlString.append("<qc_reqd protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd>");
						}
						if (qcReqdTypeSFlg == "2") {
							valueXmlString.append("<qc_reqd_type>").append("<![CDATA[" + qcReqdType + "]]>")
									.append("</qc_reqd_type>");
							valueXmlString.append("<qc_reqd_type protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd_type>");
						} else if (qcReqdTypeSFlg == "0") {
							valueXmlString.append("<qc_reqd_type>").append("<![CDATA[" + qcReqdType + "]]>")
									.append("</qc_reqd_type>");
							valueXmlString.append("<qc_reqd_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd_type>");
						} else {
							valueXmlString.append("<qc_reqd_type protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</qc_reqd_type>");
						}
						if (yieldPercSFlg == "2") {
							valueXmlString.append("<yield_perc>").append("<![CDATA[" + yieldPerc + "]]>")
									.append("</yield_perc>");
							valueXmlString.append("<yield_perc protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</yield_perc>");
						} else if (yieldPercSFlg == "0") {
							valueXmlString.append("<yield_perc>").append("<![CDATA[" + yieldPerc + "]]>")
									.append("</yield_perc>");
							valueXmlString.append("<yield_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</yield_perc>");
						} else {
							valueXmlString.append("<yield_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</yield_perc>");
						}
						if (potencyPercSFlg == "2") {
							valueXmlString.append("<potency_perc>").append("<![CDATA[" + potencyPerc + "]]>")
									.append("</potency_perc>");
							valueXmlString.append("<potency_perc protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</potency_perc>");
						} else if (potencyPercSFlg == "0") {
							valueXmlString.append("<potency_perc>").append("<![CDATA[" + potencyPerc + "]]>")
									.append("</potency_perc>");
							valueXmlString.append("<potency_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</potency_perc>");
						} else {
							valueXmlString.append("<potency_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</potency_perc>");
						}
						if (trackSLifeSFlg == "2") {
							valueXmlString.append("<track_shelf_life>").append("<![CDATA[" + trackSLife + "]]>")
									.append("</track_shelf_life>");
							valueXmlString.append("<track_shelf_life protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</track_shelf_life>");
						} else if (trackSLifeSFlg == "0") {
							valueXmlString.append("<track_shelf_life>").append("<![CDATA[" + trackSLife + "]]>")
									.append("</track_shelf_life>");
							valueXmlString.append("<track_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</track_shelf_life>");
						} else {
							valueXmlString.append("<track_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</track_shelf_life>");
						}
						if (minSLifeSFlg == "2") {
							valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minSLife + "]]>")
									.append("</min_shelf_life>");
							valueXmlString.append("<min_shelf_life protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</min_shelf_life>");
						} else if (minSLifeSFlg == "0") {
							valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minSLife + "]]>")
									.append("</min_shelf_life>");
							valueXmlString.append("<min_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</min_shelf_life>");
						} else {
							valueXmlString.append("<min_shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</min_shelf_life>");
						}
						if (shelfLifeSFlg == "2") {
							valueXmlString.append("<shelf_life>").append("<![CDATA[" + shelfLife + "]]>")
									.append("</shelf_life>");
							valueXmlString.append("<shelf_life protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</shelf_life>");
						} else if (shelfLifeSFlg == "0") {
							valueXmlString.append("<shelf_life>").append("<![CDATA[" + shelfLife + "]]>")
									.append("</shelf_life>");
							valueXmlString.append("<shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</shelf_life>");
						} else {
							valueXmlString.append("<shelf_life protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</shelf_life>");
						}
						if (scanBarSFlg != null && scanBarSFlg.equals("2")) {
							valueXmlString.append("<scanned_barcode>").append("<![CDATA[" + scanBar + "]]>")
									.append("</scanned_barcode>");
							valueXmlString.append("<scanned_barcode protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</scanned_barcode>");
						} else if (scanBarSFlg != null && scanBarSFlg.equals("0")) {
							valueXmlString.append("<scanned_barcode>").append("<![CDATA[" + scanBar + "]]>")
									.append("</scanned_barcode>");
							valueXmlString.append("<scanned_barcode protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</scanned_barcode>");
						} else {
							valueXmlString.append("<scanned_barcode = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</scanned_barcode>");
						}
						if (indentOptSFlg != null && indentOptSFlg.equals("2")) {
							valueXmlString.append("<indent_opt>").append("<![CDATA[" + indentOpt + "]]>")
									.append("</indent_opt>");
							valueXmlString.append("<indent_opt protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</indent_opt>");
						} else if (indentOptSFlg != null && indentOptSFlg.equals("0")) {
							valueXmlString.append("<indent_opt>").append("<![CDATA[" + indentOpt + "]]>")
									.append("</indent_opt>");
							valueXmlString.append("<indent_opt protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</indent_opt>");
						} else {
							valueXmlString.append("<indent_opt = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</indent_opt>");
						}
						if (issCritSFlg != null && issCritSFlg.equals("2")) {
							valueXmlString.append("<iss_criteria>").append("<![CDATA[" + issCrit + "]]>")
									.append("</iss_criteria>");
							valueXmlString.append("<iss_criteria protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</iss_criteria>");
						} else if (issCritSFlg != null && issCritSFlg.equals("0")) {
							valueXmlString.append("<iss_criteria>").append("<![CDATA[" + issCrit + "]]>")
									.append("</iss_criteria>");
							valueXmlString.append("<iss_criteria protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</iss_criteria>");
						} else {
							valueXmlString.append("<iss_criteria = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</iss_criteria>");
						}
						if (empCodeIaprSFlg != null && empCodeIaprSFlg.equals("2")) {
							valueXmlString.append("<emp_code__iapr>").append("<![CDATA[" + empCodeIapr + "]]>")
									.append("</emp_code__iapr>");
							valueXmlString.append("<emp_code__iapr protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__iapr>");
						} else if (empCodeIaprSFlg != null && empCodeIaprSFlg.equals("0")) {
							valueXmlString.append("<emp_code__iapr>").append("<![CDATA[" + empCodeIapr + "]]>")
									.append("</emp_code__iapr>");
							valueXmlString.append("<emp_code__iapr protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__iapr>");
						} else {
							valueXmlString.append("<emp_code__iapr = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__iapr>");
						}
						if (roleCodeIndaprSFlg != null && roleCodeIndaprSFlg.equals("2")) {
							valueXmlString.append("<role_code__indaprv>").append("<![CDATA[" + roleCodeIndapr + "]]>")
									.append("</role_code__indaprv>");
							valueXmlString.append("<role_code__indaprv protect = \"1\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__indaprv>");
						} else if (roleCodeIndaprSFlg != null && roleCodeIndaprSFlg.equals("0")) {
							valueXmlString.append("<role_code__indaprv>").append("<![CDATA[" + roleCodeIndapr + "]]>")
									.append("</role_code__indaprv>");
							valueXmlString.append("<role_code__indaprv protect = \"0\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__indaprv>");
						} else {
							valueXmlString.append("<role_code__indaprv = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</role_code__indaprv>");
						}
						if (empCodeQcaprSFlg != null && empCodeQcaprSFlg == "2") {
							valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeQcapr + "]]>")
									.append("</emp_code__qcaprv>");
							valueXmlString.append("<emp_code__qcaprv protect = \"1\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__qcaprv>");
						} else if (empCodeQcaprSFlg != null && empCodeQcaprSFlg.equalsIgnoreCase("0")) {
							valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[" + empCodeQcapr + "]]>")
									.append("</emp_code__qcaprv>");
							valueXmlString.append("<emp_code__qcaprv protect = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__qcaprv>");
						} else {
							valueXmlString.append("<emp_code__qcaprv = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</emp_code__qcaprv>");
						}
						if (roleCodeQcaprSFlg != null && roleCodeQcaprSFlg.equalsIgnoreCase("2")) {
							valueXmlString.append("<role_code__qcaprv>").append("<![CDATA[" + roleCodeQcapr + "]]>")
									.append("</role_code__qcaprv>");
							valueXmlString.append("<role_code__qcaprv protect = \"1\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__qcaprv>");
						} else if (roleCodeQcaprSFlg != null && roleCodeQcaprSFlg.equalsIgnoreCase("0")) {
							valueXmlString.append("<role_code__qcaprv>").append("<![CDATA[" + roleCodeQcapr + "]]>")
									.append("</role_code__qcaprv>");
							valueXmlString.append("<role_code__qcaprv protect = \"0\">")
									.append("<![CDATA[" + "" + "]]>").append("</role_code__qcaprv>");
						} else {
							valueXmlString.append("<role_code__qcaprv = \"0\">").append("<![CDATA[" + "" + "]]>")
									.append("</role_code__qcaprv>");
						}
					}

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} else if (currentColumn.trim().equalsIgnoreCase("order_opt")) {
				orderOpt = checkNull(genericUtility.getColumnValue("order_opt", dom));
				masterSch = checkNull(genericUtility.getColumnValue("master_sch", dom));
				if (orderOpt != null && orderOpt.equalsIgnoreCase("P")) {
					valueXmlString.append("<master_sch>").append("<![CDATA[Y]]>").append("</master_sch>");
				}
			} else if (currentColumn.trim().equalsIgnoreCase("appr_supp")) {
				apprSupp = checkNull(genericUtility.getColumnValue("appr_supp", dom));
				if (apprSupp != null && apprSupp.equalsIgnoreCase("Y")) {
					valueXmlString.append("<no_cons_unaprv protect = \"0\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_cons_unaprv>");
					valueXmlString.append("<disapprv_on_rej protect = \"0\">").append("<![CDATA[" + "" + "]]>")
							.append("</disapprv_on_rej>");
				} else {
					valueXmlString.append("<no_cons_unaprv protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_cons_unaprv>");
					valueXmlString.append("<disapprv_on_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</disapprv_on_rej>");
					valueXmlString.append("<no_of_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_of_rej>");
				}
			} else if (currentColumn.trim().equalsIgnoreCase("disapprv_on_rej")) {
				disapprvOnReg = checkNull(genericUtility.getColumnValue("disapprv_on_rej", dom));
				if (disapprvOnReg != null && disapprvOnReg.equalsIgnoreCase("Y")) {
					valueXmlString.append("<no_of_rej protect = \"0\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_of_rej>");
				} else {
					valueXmlString.append("<no_of_rej protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</no_of_rej>");
				}
			} else if (currentColumn.trim().equalsIgnoreCase("mfg_type")) {
				mfgType = checkNull(genericUtility.getColumnValue("mfg_type", dom));
				supplierSour = checkNull(genericUtility.getColumnValue("supp_sour", dom));
				if (mfgType != null && mfgType.trim().equalsIgnoreCase("D") && supplierSour != null
						&& supplierSour.trim().equalsIgnoreCase("M")) {
					valueXmlString.append("<extra_mfg_perc protect = \"0\">").append("<![CDATA[" + "" + "]]>")
							.append("</extra_mfg_perc>");
				} else {
					valueXmlString.append("<extra_mfg_perc protect = \"1\">").append("<![CDATA[" + "" + "]]>")
							.append("</extra_mfg_perc>");
					valueXmlString.append("<extra_mfg_perc>").append("<![CDATA[" + 0 + "]]>")
							.append("</extra_mfg_perc>");
				}
			}
			valueXmlString.append("</Detail1>");
			if (supplierSour != null && supplierSour.equalsIgnoreCase("M")) {
				valueXmlString.append("<pur_lead_time>").append("<![CDATA[" + 0 + "]]>").append("</pur_lead_time>");
				valueXmlString.append("<mfg_lead_time>").append("<![CDATA[" + 0 + "]]>").append("</mfg_lead_time>");
				valueXmlString.append("<qc_lead_time>").append("<![CDATA[" + 0 + "]]>").append("</qc_lead_time>");
				valueXmlString.append("<yield_perc>").append("<![CDATA[" + 0 + "]]>").append("</yield_perc>");
				valueXmlString.append("<master_sch>").append("<![CDATA[N]]>").append("</master_sch>");
				valueXmlString.append("<site_code__supp>").append("<![CDATA[" + siteCode + "]]>")
						.append("</site_code__supp>");
				valueXmlString.append("<site_code__plan>").append("<![CDATA[" + siteCode + "]]>")
						.append("</site_code__plan>");
			}
			valueXmlString.append("</Root>");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (pstmt != null)
						pstmt.close();
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String input) {
		if (input == null || input.equalsIgnoreCase("null")) {
			input = "";
		}
		return input;
	}

	private int convertInt(String input) {
		if (input.trim().length() == 0) {
			return 0;
		} else {
			return Integer.parseInt(input);
		}
	}

	private String errorType(Connection conn, String errorCode) {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msgType;
	}
}
