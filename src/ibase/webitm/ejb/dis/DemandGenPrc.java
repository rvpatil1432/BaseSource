package ibase.webitm.ejb.dis;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.lang.*;
import org.w3c.dom.*;
import javax.ejb.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.naming.InitialContext;

import javax.ejb.Stateless;

@Stateless
public class DemandGenPrc extends ProcessEJB implements DemandGenPrcLocal, DemandGenPrcRemote {
	public String process() throws RemoteException, ITMException {
		return "";
	}

	DistCommon distCommon = new DistCommon();

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException, ITMException {
		String retStr = "";
		Document dom = null;
		Document dom2 = null;
		// GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			retStr = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println(
					"Exception : process(String xmlString, String xmlString2, String windowName, String xtraParams)"
							+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}

	public String process(Document Dom, Document Dom2, String windowName, String xtraParams)
			throws RemoteException, ITMException {
		System.out.println("Inside Process method:::::::");

		String errString = null, retString = null, xmlString = null;

		String siteCode = "", stateCode = null, countCode = null, itemCode = "", strQuantity = "", bomCode = "",
				custCode = "", loginSite = "", tranID = null, sysDate = "", fromDT = null, toDT = null, unit = null,
				convQtyStduom1 = null, unitStd = null, prdCodeRef = null;
		Timestamp dueDate = null, fromDate = null, toDate = null, tranDate = null;
		Double quantity = 0.0d, frQty = 0.0d, convQtyStduom = 0.0d, qtyStdUom = 0.0d, indQty = 0.0d;
		Connection conn = null;
		int cnt = 0, cnt1 = 0, cntr = 0, updcnt = 0;
		String sql = null;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		boolean count = false;
		ResultSet rs = null, rs1 = null, rs2 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		ArrayList qtyStdUom1 = new ArrayList();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		HashMap<String, Double> indDemandrow = new HashMap<String, Double>();
		StringBuffer xmlBuff;
		String userId = "";// Added By Pavan R 27/DEC/17
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			fromDT = genericUtility.getColumnValue("from_date", Dom);
			toDT = genericUtility.getColumnValue("to_date", Dom);
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
		    HashMap demandSupplyMap = new HashMap();
			// custCode=genericUtility.getColumnValue("cust_code",Dom);
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			// added by Pavan R on 27/DEC/17 userId passwed to savData() and
			// processRequest()
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("userId::[" + userId + "]");
			tranDate = java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			System.out.println("FromDate[" + fromDate + "]: ToDate[" + toDate + "]");
			Calendar currentDate = Calendar.getInstance();
			System.out.println("currDate>>>>>>" + tranDate);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDate);

			if (fromDT == null || fromDT.trim().length() == 0) {
				errString = itmDBAccessEJB.getErrorString("", "VPEDATE1", "", "", conn);
				return errString;
			}
			if (toDT == null || toDT.trim().length() == 0) {
				errString = itmDBAccessEJB.getErrorString("", "VPEDATE1", "", "", conn);
				return errString;

			}
			if (compareDates(fromDT, toDT)) {

				errString = itmDBAccessEJB.getErrorString("", "VFRTODATE", "", "", conn);
				return errString;
			}
			fromDate = Timestamp
					.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("from_date", Dom),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			toDate = Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("to_date", Dom),
					genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			sql = "Select site_code,item_code,due_date,sum(quantity) as quantity,cust_code from PPPL_IND_DEMAND "
					+ "where due_date <= ? and due_date >= ? group by site_code,item_code,due_date,cust_code";
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, toDate);
			pstmt.setTimestamp(2, fromDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				cnt++;
				siteCode = rs.getString(1);
				itemCode = rs.getString(2);
				dueDate = rs.getTimestamp(3);
				quantity = rs.getDouble(4);
				custCode = rs.getString(5);
				System.out.println("Row [" + cnt + "] siteCode[" + siteCode + "]: itemCode[" + itemCode + "]: dueDate["
						+ dueDate + "]: quantity[" + quantity + "]");

				// HashMap <String,Double> indDemandrow =new
				// HashMap<String,Double>();

				// Generating key
				String Key = siteCode.trim() + "," + itemCode.trim() + "," + sdf.format(dueDate);

				String sql3 = "Select state_code,count_code from customer where cust_code= ? ";
				pstmt2 = conn.prepareStatement(sql3);
				pstmt2.setString(1, custCode);
				rs2 = pstmt2.executeQuery();
				if (rs2.next()) {
					stateCode = rs2.getString(1);
					countCode = rs2.getString(2);

				}
				pstmt2.close();
				pstmt2 = null;
				rs2.close();
				rs2 = null;

				// get Scheme code
				//Pavan Rane 29jul19[changed tran_date to due_date  in getBomCode() method signature]
				//bomCode = getBomCode(itemCode, custCode, quantity, tranDate, siteCode, stateCode, countCode, conn);
				bomCode = getBomCode(itemCode, custCode, quantity, dueDate, siteCode, stateCode, countCode, conn);

				// calculate free quantity
				//frQty = getFreeQty(bomCode, quantity, conn);
				frQty = getFreeQty(bomCode, quantity,dueDate, conn);
				
				System.out.println("CustCode [" + custCode + "]BomCode [" + bomCode + "] FreeQty [" + frQty + "]");
				quantity = quantity + frQty;

				// if Already present sum quantity
				if (indDemandrow.containsKey(Key)) {
					double SumQuantity = (indDemandrow.get(Key)) + quantity;
					indDemandrow.put(Key, SumQuantity);
				} else {
					System.out.println("Key is " + Key);
					indDemandrow.put(Key, quantity);

				}

			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
			Set<String> eachKey = indDemandrow.keySet();
			System.out.println("custCodekeys[" + eachKey + "]");
			for (String eachIndDemand : eachKey) {
				// splitting key to get sitecode itemcode and due date
				String[] keyData = eachIndDemand.split(",");

				String sql1 = "Select tran_id,quantity,unit,conv__qty_stduom,quantity__std_uom,unit__std,prd_code__ref from independent_demand where site_code=? and item_code= ? and due_date = ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, keyData[0]);
				pstmt1.setString(2, keyData[1]);
				Timestamp dueDateTimestmp = Timestamp.valueOf(genericUtility.getValidDateString(keyData[2],
						genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
				pstmt1.setTimestamp(3, dueDateTimestmp);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					tranID = rs1.getString("tran_id");
					unit = rs1.getString("unit");
					indQty = rs1.getDouble("quantity");
					convQtyStduom = rs1.getDouble("conv__qty_stduom");
					// qtyStdUom=rs1.getString("quantity__std_uom");
					unitStd = rs1.getString("unit__std");
					prdCodeRef = rs1.getString("prd_code__ref");

				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;

				if (tranID == null || tranID.trim().length() == 0) {
					// cntr++;
					xmlBuff = new StringBuffer();
					xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
					xmlBuff.append("<DocumentRoot>");
					xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
					xmlBuff.append("<group0>");
					xmlBuff.append("<description>").append("Group0 description").append("</description>");
					xmlBuff.append("<Header0>");
					xmlBuff.append("<objName><![CDATA[").append("independent_demand").append("]]></objName>");
					xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
					xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
					xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
					xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
					xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
					xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
					xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
					// xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
					xmlBuff.append("<description>").append("Header0 members").append("</description>");

					// <Detail1 dbID="0000000007" domID="1" objContext="1"
					// objName="independent_demand">
					xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"independent_demand\" objContext=\"1\">");
					xmlBuff.append("<attribute selected=\"N\" updateFlag=\"E\" status=\"N\" pkNames=\"\"/>");
					// <attribute pkNames="tran_id:" selected="N" status="O"
					// updateFlag="N"/>
					// xmlBuff.append("<tran_id><![CDATA[0000000007]]></tran_id>");
					xmlBuff.append("<tran_date><![CDATA[" + sysDate + "]]></tran_date>");
					xmlBuff.append("<due_date><![CDATA[" + keyData[2] + "]]></due_date>");
					xmlBuff.append("<site_code><![CDATA[" + keyData[0] + "]]></site_code>");
					xmlBuff.append("<item_code><![CDATA[" + keyData[1] + "]]></item_code>");
					xmlBuff.append("<quantity><![CDATA[" + indDemandrow.get(eachIndDemand) + "]]></quantity>");
					xmlBuff.append("</Detail1>");
					xmlBuff.append("</Header0>");
					xmlBuff.append("</group0>");
					xmlBuff.append("</DocumentRoot>");
					xmlString = xmlBuff.toString();

					System.out.println("xmlString::" + xmlString);

					retString = saveData(loginSite, xmlString, userId, conn);
					
					/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
					if (retString.indexOf("Success") > -1)
					{
						String[] arrayForTranIdIssue = retString.split("<TranID>");
						int endIndexIssue = arrayForTranIdIssue[1].indexOf("</TranID>");
						String transId = arrayForTranIdIssue[1].substring(0, endIndexIssue);
						System.out.println("@V@ Tran id :- [" + transId + "]");																			
										
					    demandSupplyMap.put("site_code", keyData[0]);
						demandSupplyMap.put("item_code", keyData[1]);		
						demandSupplyMap.put("ref_ser", "I-DEM");
						demandSupplyMap.put("ref_id", transId);
						demandSupplyMap.put("ref_line", "NA");
						demandSupplyMap.put("due_date", dueDateTimestmp);		
						demandSupplyMap.put("demand_qty", indDemandrow.get(eachIndDemand));
						demandSupplyMap.put("supply_qty", 0.0);
						demandSupplyMap.put("change_type", "A");
						demandSupplyMap.put("chg_process", "T");
						demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
					    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
					    demandSupplyMap.clear();
					    if(errString != null && errString.trim().length() > 0)
					    {
					    	System.out.println("errString["+errString+"]");
			                return errString;
					    }
					}
				 /**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
				} else {
					qtyStdUom1 = distCommon.getConvQuantityFact(unit, unitStd, keyData[1], indDemandrow.get(eachIndDemand), convQtyStduom, conn);
					qtyStdUom = Double.parseDouble((String) qtyStdUom1.get(1));

					String sql2 = "Update independent_demand set quantity = ? , quantity__std_uom= ? where tran_id= ? ";
					pstmt1 = conn.prepareStatement(sql2);
					pstmt1.setDouble(1, indDemandrow.get(eachIndDemand));
					pstmt1.setDouble(2, qtyStdUom);
					pstmt1.setString(3, tranID);
					updcnt = pstmt1.executeUpdate();
					System.out.println("updcnt" + updcnt);
					/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
					if(updcnt > 0)
					{
					    demandSupplyMap.put("site_code", keyData[0]);
						demandSupplyMap.put("item_code", keyData[1]);		
						demandSupplyMap.put("ref_ser", "I-DEM");
						demandSupplyMap.put("ref_id", tranID);
						demandSupplyMap.put("ref_line", "1");
						demandSupplyMap.put("due_date", dueDateTimestmp);		
						demandSupplyMap.put("demand_qty", qtyStdUom);
						demandSupplyMap.put("supply_qty", 0.0);
						demandSupplyMap.put("change_type", "C");
						demandSupplyMap.put("chg_process", "T");
						demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
					    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
					    demandSupplyMap.clear();
					    if(errString != null && errString.trim().length() > 0)
					    {
					    	System.out.println("errString["+errString+"]");
			                return errString;
					    }
					}
					/**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/

				}

				if (updcnt > 0 || retString.indexOf("Success") > -1 || retString==null) {
					// conn.commit();
					cntr++;
					System.out.println("Connection Commited");
				} else {
					conn.rollback();
					return retString;

				}
				updcnt = 0;
				tranID = null;
			}

			if (cntr > 0) {
				conn.commit();
				errString = itmDBAccessEJB.getErrorString("", "VMSUCC", "", "", conn);

			} else if (cnt <= 0) {
				errString = itmDBAccessEJB.getErrorString("", "NODATAERR", "", "", conn);
			}

		} catch (Exception e) {
			try {
				System.out.println("@@@@@@@@@ Exception.........conn.rollback().........");
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				System.out.println("Exception ::" + e1.getMessage());
				throw new ITMException(e1);
			}
			;
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		} finally {
			try {
				// conn.rollback();
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e);
			}
		}

		return errString;
	}

	private String saveData(String siteCode, String xmlString, String userId, Connection conn) throws ITMException {
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		// Connection conn=null;
		ConnDriver connDriver = new ConnDriver();
		MasterStatefulLocal masterStateful = null; // for ejb3
		try {

			// conn = getConnection();
			// conn=connDriver.getConnectDB("DriverITM");
			// conn.setAutoCommit(false);
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String[] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
			System.out.println("--retString - -" + retString);
		} catch (ITMException itme) {
			System.out.println("ITMException : Create Independent Demand :saveData :==>");
			throw itme;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception :Create Independent Demand :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}

	public String getBomCode(String lsItemCodeOrd, String custCode, Double quantity, Timestamp tranDate,
			String siteCode, String stateCode, String countCode, Connection conn) throws RemoteException, ITMException {
		String retString = null;

		// String lsSchemeCode=null,lstype=null;
		E12GenericUtility genericUtility = new E12GenericUtility();

		double mQty = 0.00, ldConvQtyStduom = 0.00, mRate = 0.00, mNum = 0.00, ldRate = 0.00, idRateWtDiscount = 0.00,
				acShipperQty = 0.00, acIntegralQty = 0.00, lcRate = 0.00;
		double lcOrdValue = 0.00, lcShipperQty = 0.00, lcIntQty = 0.00, lcBalQty = 0.00, lcLooseQty = 0.00,
				lcIntegralQty = 0.00, lcQty1 = 0.00, lcQtyFc = 0.00;
		String sql = "", mVal = "", mVal1 = "", lsNature = "", lsPriceListParent = "", lsUnitSal = "", lsPriceList = "",
				lsContractNo = "", lsListType = "";
		String lsPlistDisc = "", lsPlistDiscount = "", lsCustCode = "", mSiteCode = "", lsPackCode = "", mStateCd = "",
				lsOrderType = "", lsCountCodeDlv = "", itemStru = "", lstype = "";
		String lsCurscheme = "", lsItemCodeParent = "", lsApplyCustList = "", lsNoapplyCustList = "",
				lsApplicableOrdTypes = "", lsSchemeCode = "", lsPrevscheme = "";
		String lsCustSchemeCode = "", lsItemStru = "", lsDisPobOrdTypeList = "", lsSchemeEdit = "", lsUnit = "",
				lsRefNo = "", lsSiteCodeShip = "", mSlabOn = "", lsDescr = "";
		String lsSalesOrd = "", lsQuotNo = "", reStr = "", ldtDateStr = "";
		int llNoOfArt = 0, cnt = 0, llPlcount = 0, llNoOfArt1 = 0, llNoOfArt2 = 0, pos = 0, schecnt = 0;

		boolean lsDiscFalg = false, lbProceed = false, lbOrdFlag = false;

		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		// String sql=null;

		try {
			sql = "select bom_code,item_stru from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsItemCodeOrd);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsSchemeCode = checkNull(rs.getString("bom_code"));
				lstype = checkNull(rs.getString("item_stru"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (!"C".equalsIgnoreCase(lstype)) {

				sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code= b.scheme_code and a.item_code= ? and a.app_from <= ? and a.valid_upto>= ? and (b.site_code= ? or b.state_code = ?  or b.count_code= ?)";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, lsItemCodeOrd);
				pstmt1.setTimestamp(2, tranDate);
				pstmt1.setTimestamp(3, tranDate);
				pstmt1.setString(4, siteCode);
				pstmt1.setString(5, stateCode);
				pstmt1.setString(6, countCode);
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {
					lsCurscheme = rs1.getString("scheme_code");

					if (lsCurscheme != null || lsCurscheme.trim().length() == 0) {
						sql = "select item_code__parent from item where item_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsItemCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsItemCodeParent = rs.getString("item_code__parent");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsItemCodeParent == null || lsItemCodeParent.trim().length() == 0) {
							sql = "select count(1) as cnt from item where item_code__parent =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsItemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if (cnt > 0) {
							break;
						}
					}
					sql = "Select count(1) as cnt From scheme_applicability A, bom b Where A.scheme_code = b.bom_code And B.bom_code= ?"
							+ " And(? between case when b.min_qty is null then 0 else b.min_qty end"
							+ " And case when b.max_qty is null then 0 else b.max_qty end) and B.promo_term is null";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCurscheme);
					pstmt.setDouble(2, quantity);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (cnt == 0)
						continue;

					/*
					 * if (cnt == 0) { // Goto Nextrec }
					 */
					sql = "select (case when apply_cust_list is null then ' ' else apply_cust_list end) as ls_apply_cust_list,"
							+ " (case when noapply_cust_list is null then ' ' else noapply_cust_list end) as ls_noapply_cust_list, order_type"
							+ " from scheme_applicability where scheme_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsCurscheme);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lsApplyCustList = rs.getString("ls_apply_cust_list");
						lsNoapplyCustList = rs.getString("ls_noapply_cust_list");
						lsApplicableOrdTypes = rs.getString("order_type");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					/*
					 * if ("NE".equalsIgnoreCase(lsOrderType) &&
					 * (lsApplicableOrdTypes == null ||
					 * lsApplicableOrdTypes.trim().length() == 0)) { // goto
					 * Nextrec } else if (lsApplicableOrdTypes != null &&
					 * lsApplicableOrdTypes.trim().length() > 0) {
					 * System.out.println("lsApplicableOrdTypes1"+
					 * lsApplicableOrdTypes+":::"); lbProceed = false; String
					 * lsApplicableOrdTypesArr[] =
					 * lsApplicableOrdTypes.split(","); ArrayList<String>
					 * applicableOrdTypes= new
					 * ArrayList<String>(Arrays.asList(lsApplicableOrdTypesArr))
					 * ; if(applicableOrdTypes.contains(lsOrderType)) {
					 * System.out.println("lbProceed"+lbProceed); lbProceed =
					 * true; // break; } if (!lbProceed) {
					 * System.out.println("Inside lbproceed"); continue; // goto
					 * Nextrec } }
					 */
					lsPrevscheme = lsSchemeCode;
					lsSchemeCode = lsCurscheme;

					if (lsApplyCustList.trim().length() > 0) {
						lsSchemeCode = null;
						System.out.println("lsSchemeCode:::::::1" + lsSchemeCode);
						// lsCustCode =
						// checkNull(genericUtility.getColumnValue("cust_code",
						// dom1));
						lsCustCode = custCode;
						System.out.println("lsCustCode" + lsCustCode);
						String lsApplyCustListArr[] = lsApplyCustList.split(",");
						ArrayList<String> applyCustList = new ArrayList<String>(Arrays.asList(lsApplyCustListArr));
						if (applyCustList.contains(lsCustCode.trim())) {
							System.out.println("Inside applycustList");
							lsSchemeCode = lsCurscheme;
							lsCustSchemeCode = lsCurscheme;
							System.out.println("lsSchemeCode::" + lsSchemeCode + " lsCustSchemeCode::" + lsCurscheme);

							// break;
						}
					}
					if (lsNoapplyCustList.trim().length() > 0 && lsSchemeCode != null) {
						// lsCustCode =
						// checkNull(genericUtility.getColumnValue("cust_code",
						// dom));
						lsCustCode = custCode;
						String lsNoapplyCustListArr[] = lsNoapplyCustList.split(",");
						ArrayList<String> noapplyCustList = new ArrayList<String>(Arrays.asList(lsNoapplyCustListArr));
						if (noapplyCustList.contains(lsCustCode)) {
							lsSchemeCode = "";
							break;
						}
					}
					cnt = 0;
					if (lsSchemeCode != null) {
						schecnt++;
					} else if (schecnt == 1) {
						lsSchemeCode = lsPrevscheme;
						System.out.println("lsSchemeCode:::::::2" + lsSchemeCode);
					}
					// Nextrec:
					// fetch next curscheme into :ls_curscheme;
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				if (schecnt > 1) {
					lsSchemeCode = "";
				} else if (lsCustSchemeCode.trim().length() > 0) {
					System.out.println("lsSchemeCode:::::::3" + lsSchemeCode);
					lsSchemeCode = lsCustSchemeCode;
				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}

		return lsSchemeCode;
	}

	//public double getFreeQty(String schemeCode, Double chrgQty, Connection conn) throws RemoteException, ITMException {
	public double getFreeQty(String schemeCode, Double chrgQty, Timestamp dueDate, Connection conn) throws RemoteException, ITMException {
		Double freeQty = 0.0d, batchQty = 0.0d, qtyPer = 0.0d, minQty = 0.0d, appMinQty = 0.0d, appMaxQty = 0.0d;
		int cnt = 0, validCnt = 0;
		String sql = null, sql1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		try {
			sql1 = "Select count(1) from bom Where  bom_code 	= ?"
					+ "And	? between case when min_qty is null then 0 else min_qty end And"
					+ "	case when max_qty is null then 0 else max_qty end";
			pstmt = conn.prepareStatement(sql1);
			pstmt.setString(1, schemeCode);
			pstmt.setDouble(2, chrgQty);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt(1);

			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			if (cnt != 0) {
				//Pavan R 9jul19 start[to check validity of scheme by duedate]
				sql = "select count(*) from scheme_applicability where scheme_code = ? and ? between app_from and valid_upto ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, schemeCode);
				pstmt1.setTimestamp(2, dueDate);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) {
					validCnt = rs1.getInt(1);
				}
				rs1.close(); rs1 = null;
				pstmt1.close(); pstmt1 = null;
				if(validCnt == 0)
				{
					freeQty = 0.0;
				}
				else 
				{  //Pavan R 9jul19 end
					sql = "select bom.batch_qty,bomdet.qty_per,bomdet.min_qty	,bomdet.app_min_qty,bomdet.app_max_qty "
							+ "from bom, bomdet where bom.bom_code = bomdet.bom_code and bomdet.bom_code 	= ? and bomdet.nature = 'F' ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, schemeCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) {
						batchQty = rs1.getDouble("batch_qty");
						qtyPer = rs1.getDouble("qty_per");
						minQty = rs1.getDouble("min_qty");
						appMinQty = rs1.getDouble("app_min_qty");
						appMaxQty = rs1.getDouble("app_max_qty");
						
						DecimalFormat df = new DecimalFormat("################");
						freeQty = Double.parseDouble(df.format(chrgQty / batchQty)) * qtyPer;
					}
					rs1.close(); rs1 = null;
					pstmt1.close(); pstmt1 = null;
				}
				/*
				 * if(chrgQty >= appMinQty && chrgQty <= appMaxQty) {
				 */
				

				/*
				 * } else { freeQty=0.0; }
				 */
			} else {
				freeQty = 0.0;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return freeQty;

	}

	private String checkNull(String inp) {
		if (inp == null)
			inp = " ";
		return inp.trim();
	}

	public boolean compareDates(String d1, String d2) {
		boolean err = false;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
			java.util.Date date1 = sdf.parse(d1);
			java.util.Date date2 = sdf.parse(d2);
			System.out.println("Date1" + sdf.format(date1));
			System.out.println("Date2" + sdf.format(date2));
			System.out.println();

			if (date1.after(date2)) {
				err = true;
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return err;
	}

}
