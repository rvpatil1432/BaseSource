/***
 * Author:Santosh
 * Date:24-APR-2019
 */
package ibase.webitm.ejb.dis;

import java.io.File;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

@Stateless
public class SalesConsolidationRulePrc extends ProcessEJB
		implements SalesConsolidationRulePrcLocal, SalesConsolidationRulePrcRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException, ITMException {
		String rtStr = "";
		Document dom = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
				System.out.println("Process Dom::::::::::::::::" + dom);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
				System.out.println("Process Dom2::::::::::::::::" + dom2);
			}
			rtStr = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::" + this.getClass().getSimpleName() + "::processDataString" + e.getMessage());
			e.printStackTrace();
		}
		return rtStr;
	}

	@SuppressWarnings("null")
	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams)
			throws RemoteException, ITMException {

		String errString = "", userID = "", prdCode = "", itemSer = "", tranId = "", loginSiteCode = "", sql = "",
				sql1 = "", userId = "", xmlInEditMode = "", unit = "", empCode = "", posCode = "", custCode = "",
				itemCode = "", tranDateStr = "", retString = "", xmlParseStr = "", versionId = "", stanCode = "",
				stanCodeNew = "", terrCode = "", terrDescr = "", lastYrPrdCode = "";
		double opStock = 0.0, clStock = 0.0, OpValue = 0.0, clValue = 0.0, cyslsSecQty = 0.0, cyslsSecVal = 0.0,
				rcpBillQty = 0.0, rcpBillVal = 0.0, tranBillQty = 0.0, tranBillVal = 0.0, retQty = 0.0, retVal = 0.0,
				tranRepQty = 0.0, tranRepVal = 0.0, tranBonusQty = 0.0, lastYrsSale = 0.0, lastYrsSaleVal = 0.0,
				tranBonusVal = 0.0, rcpBonusQty = 0.0, rcpBonusVal = 0.0, rcpRplQty = 0.0, rcpRplVal = 0.0,
				grossQty = 0.0, grossRate = 0.0, grossVal = 0.0;
		Connection conn = null;
		String balGroup = "", descr = "", sqlExpr = "", sqlExpr1 = "", sqlExpr2 = "", sqlExpr3 = "", sqlExpr4 = "",
				sqlExpr5 = "", sqlExpr6 = "", sqlInput = "", FileName = "";
		String sqlExprMain = "";
		int lineNo = 0;
		String calMomnth = "", prdCodeFrom = "", prdCodeTo = "", acctPrd = "", source = "", ruleCode = "";// added by
																											// santosh
																											// on
																											// 09-MAY2019
		String itemSerNew = "", remarks = "", udfStr1 = "", udfStr2 = "", udfStr3 = "";
		double grossSalesQty = 0, grossSalesVal = 0, cyslsPriQty = 0, cyslsPriVal = 0, lyslsPriQty = 0, lyslsPriVal = 0,
				lyslsSecQty = 0, lyslsSecVal = 0;
		int ctr = 0, count = 0, cnt = 0, updCnt = 0, period = 0;
		String chgTerm = "", chgUser = "", overWrite = "", countryCode = "";
		PreparedStatement pstmt = null, pstmt1 = null,pstmt0 = null;
		ResultSet rs = null, rs1 = null, rs0 = null;
		Timestamp tranDate = null, frDate = null, toDate = null;
		StringBuffer xmlBuff = null;
		System.out.println("Current DOM [" + genericUtility.serializeDom(dom) + "]");
		System.out.println("Header DOM [" + genericUtility.serializeDom(dom2) + "]");
		java.sql.Date sysDate = null;
		LinkedList previousYearList = new LinkedList();
		String finalPairArray[] = null;
		String finalCustCode = "", finalItemCode = "", finalItemSer = "", finalPosCode = "";
		String currLastDiff = "";
		int counter = 0;
		java.sql.Timestamp effDateMax = null;
		String itemSerOld = "", unitChange = "", stanCodeChange = "";
		boolean connBollen = false;
		String previousYearPair = "", currentYearPair = "";
		SalesConsolidateDetail SalesConsolidateDetail = null;
		ArrayList<SalesConsolidateDetail> al = new ArrayList<SalesConsolidateDetail>();
		ConnDriver connDriver = new ConnDriver();
		Connection connection=null;
		PreparedStatement preparedStatement=null;
		ResultSet resultSet=null;
		PreparedStatement preparedStatement1=null;
		ResultSet resultSet1=null;
		List<HashMap<String, String>> ruleList1=null;
		HashMap<String, String> dwhMap1=null;
		List<HashMap<String, String>> ruleList2=null;
		HashMap<String, String> dwhMap2=null;
		try {
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");
			java.util.Date now = new java.util.Date();
			String strDate = sdfDate.format(now);
			System.out.println("@S@>>>strDate[" + strDate + "]");
			System.out.println("In process Secondary and Primary Sales Consolidation:::");
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userID"));
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			// prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
			prdCodeFrom = checkNull(genericUtility.getColumnValue("prd_code_from", dom));
			prdCodeTo = checkNull(genericUtility.getColumnValue("prd_code_to", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			versionId = checkNull(genericUtility.getColumnValue("version_id", dom));
			acctPrd = checkNull(genericUtility.getColumnValue("acct_prd", dom));
			overWrite = checkNull(genericUtility.getColumnValue("overwrite", dom));
			System.out.println("versionId>>>" + versionId + "acctPrd" + acctPrd + "]");
			System.out.println("prdCodeFrom>>>" + prdCodeFrom + "prdCodeTo" + prdCodeTo + ">>itemSer>>" + itemSer
					+ ">>overWrite>>" + overWrite);
			System.out.println("userId : " + userId);
			System.out.println("xtraParams ::: " + xtraParams);
			ConnDriver con = new ConnDriver();
			/* conn = con.getConnectDB("DriverITM"); */
			conn = getConnection();
			connection=con.getConnectDB("DWH");
			System.out.println("connection===>"+connection);
			
			sql1 = "select sysdate from dual";
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				sysDate = rs.getDate(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select count_code from state where "
					+ "state_code in (select state_code from site where site_code=?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginSiteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				countryCode = checkNull(rs.getString("count_code")).trim();
				System.out.println("countryCode >>> :" + countryCode);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			/**
			 * sql = " SELECT FR_DATE,TO_DATE FROM PERIOD_TBL WHERE PRD_CODE=? AND
			 * PRD_TBLNO=? "; pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			 * prdCode); pstmt.setString(2, countryCode+"_"+itemSer.trim()); rs =
			 * pstmt.executeQuery(); if(rs.next()) { frDate = rs.getTimestamp("FR_DATE");
			 * toDate = rs.getTimestamp("TO_DATE"); } rs.close(); rs = null; pstmt.close();
			 * pstmt = null; System.out.println("frDate :::"+frDate+">>toDate :::"+toDate);
			 * 
			 * sql = "SELECT VERSION_ID FROM VERSION WHERE EFF_FROM < = ? AND VALID_UPTO > =
			 * ?"; pstmt = conn.prepareStatement(sql); pstmt.setTimestamp(1, frDate);
			 * pstmt.setTimestamp(2, toDate); rs = pstmt.executeQuery(); if(rs.next()) {
			 * versionId = checkNull(rs.getString("VERSION_ID"));
			 * System.out.println("versionId ::: "+versionId); } rs.close(); rs = null;
			 * pstmt.close(); pstmt = null;
			 */
			/**
			 * Delete the Record if overWrite is select as Y for the particular Period
			 */
			
			if ("Y".equalsIgnoreCase(overWrite)) {
				cnt = 0;

				/*
				 * Commented by santosh on 09-MAY-2019 to delete data on the bases of range of
				 * period sql =
				 * "DELETE FROM SALES_CONS_RULEWISE WHERE PRD_CODE = ? AND ITEM_SER=? AND SOURCE='E'"
				 * ; pstmt = conn.prepareStatement(sql); pstmt.setString(1, prdCode);
				 * pstmt.setString(2, itemSer); cnt=pstmt.executeUpdate();
				 * System.out.println("Delete count>>"+cnt); pstmt.close(); pstmt = null;
				 */
				sql = "select distinct cal_month as cal_month from SALES_CONS_RULEWISE where cal_month between ? and ?  AND ITEM_SER = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCodeFrom);
				pstmt.setString(2, prdCodeTo);
				pstmt.setString(3, itemSer);
				rs = pstmt.executeQuery();
				System.out.println("outside delete");
				while (rs.next()) {
					calMomnth = checkNull(rs.getString("cal_month"));
					System.out.println("inside delete");
					sql1 = "DELETE FROM SALES_CONS_RULEWISE WHERE cal_month = ? AND ITEM_SER=? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, calMomnth);
					pstmt1.setString(2, itemSer);
					cnt = pstmt1.executeUpdate();
					System.out.println("Delete count>>" + cnt);
					pstmt1.close();
					pstmt1 = null;
					System.out.println("Delete count>>" + cnt);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}
			
			
			// FileName=itemSer+"_"+prdCode+"_"+strDate;
			FileName = itemSer + "_" + prdCodeFrom + "_" + prdCodeTo + "_" + strDate;
			System.out.println("@S@>>FileName[" + FileName + "]");
			sql = "select a.bal_group,b.line_no,a.descr ,b.sql_expr,b.sql_expr1,b.sql_expr2,b.sql_expr3,b.sql_expr4,b.sql_expr5,b.sql_expr6,b.sql_input "
					+ "from tax_balance_grp a ,tax_bal_grp_det b where  a.bal_group=b.bal_group and b.ref_ser='D-ES3'  and b.bal_group not like '%R000%'  order by line_no ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {

				balGroup = checkNull(rs.getString(1));
				lineNo = rs.getInt(2);
				descr = checkNull(rs.getString(3));
				sqlExpr = checkNull(rs.getString(4));
				sqlExpr1 = checkNull(rs.getString(5));
				sqlExpr2 = checkNull(rs.getString(6));
				sqlExpr3 = checkNull(rs.getString(7));
				sqlExpr4 = checkNull(rs.getString(8));
				sqlExpr5 = checkNull(rs.getString(9));
				sqlExpr6 = checkNull(rs.getString(10));
				sqlInput = checkNull(rs.getString(11));
				// Combining
				// sqlExprMain=sqlExpr+sqlExpr1+sqlExpr2+sqlExpr3+sqlExpr4+sqlExpr5+sqlExpr6;
				// sqlExprMain=sqlExpr2+sqlExpr3+sqlExpr4+sqlExpr5+sqlExpr6; Commented by
				// santosh on 13-MAY-2019
				// sqlExprMain=sqlExpr2+sqlExpr3+sqlExpr4+sqlExpr5;
				/*
				 * int num2 =ApplyOn1Sub.indexOf('[');
				 * 
				 * String ApplyOn2 = ApplyOn1Sub.substring(num2+1, ApplyOn1Sub.length()-1);
				 * System.out.println(ApplyOn2+">>>");
				 * System.out.println("@S@ sqlExprMain["+sqlExprMain+"]"); sqlExprMain =
				 * getSqlWithValue(sqlExprMain,sqlExpr6,);
				 */
				/*
				 * System.out.println(sqlExpr); System.out.println(sqlExpr1);
				 * System.out.println(sqlExpr2); System.out.println(sqlExpr3);
				 * System.out.println(sqlExpr4); System.out.println(sqlExpr5);
				 */

				// sqlExprMain = sqlExpr + sqlExpr1 + sqlExpr2 + sqlExpr3 + sqlExpr4 + sqlExpr5
				// + sqlExpr6 + sqlInput;
				sqlExprMain = sqlExpr + sqlExpr1;
				sql1 = sqlExprMain;
				System.out.println("sql1... +" + sql1);
				if (sql1 != null && sql1.trim().length() > 0) {
					pstmt1 = conn.prepareStatement(sql1);
					if ("ES3R0".equalsIgnoreCase(balGroup)) {
						/*
						 * pstmt1.setString(1,prdCode); pstmt1.setString(2,itemSer);
						 * pstmt1.setString(3,prdCode); pstmt1.setString(4,itemSer);
						 */
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, itemSer);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);
						pstmt1.setString(6, itemSer);

					}
					
					
					// added by adnan
					if ("ES3R1".equalsIgnoreCase(balGroup)) {
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, acctPrd);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);

					}

					if ("ES3R2".equalsIgnoreCase(balGroup)) {
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, acctPrd);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);

					}

					if ("ES3R3".equalsIgnoreCase(balGroup)) {
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, acctPrd);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);
						pstmt1.setString(6, prdCodeFrom);
						pstmt1.setString(7, prdCodeTo);
						pstmt1.setString(8, acctPrd);
						pstmt1.setString(9, prdCodeFrom);
						pstmt1.setString(10, prdCodeTo);

					}

					if ("ES3R4".equalsIgnoreCase(balGroup)) {
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, acctPrd);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);
						pstmt1.setString(6, prdCodeFrom);
						pstmt1.setString(7, prdCodeTo);
						pstmt1.setString(8, acctPrd);
						pstmt1.setString(9, prdCodeFrom);
						pstmt1.setString(10, prdCodeTo);

					}

					if ("ES3R5".equalsIgnoreCase(balGroup)) {
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, acctPrd);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);

					}

					if ("ES3R6".equalsIgnoreCase(balGroup)) {
						pstmt1.setString(1, prdCodeFrom);
						pstmt1.setString(2, prdCodeTo);
						pstmt1.setString(3, acctPrd);
						pstmt1.setString(4, prdCodeFrom);
						pstmt1.setString(5, prdCodeTo);

					}

					/*
					 * if ("ES3R7".equalsIgnoreCase(balGroup)) { pstmt1.setString(1, prdCodeFrom);
					 * pstmt1.setString(2, prdCodeTo); pstmt1.setString(3, prdCodeFrom);
					 * pstmt1.setString(4, prdCodeTo);
					 * 
					 * }
					 */
					/*
					 * if ("ES3R7".equalsIgnoreCase(balGroup)) { pstmt1.setString(1, prdCodeFrom);
					 * pstmt1.setString(2, prdCodeTo); }
					 */
					
					
					if ("ES3R7".equalsIgnoreCase(balGroup)) {
						System.out.println("ES3R7=======>");
						/*
						 * String sql2 = "SELECT sales_rule.acct_prd AS acct_prd, sales_rule.cal_month"
						 * + " AS cal_month, es3_rule_hdr.item_ser_from  as item_ser_from, " +
						 * "es3_rule_hdr.item_ser_to as item_ser_to," +
						 * " 'ES3R7' AS rule_code, sales_rule.item_code AS item_code, sales_rule.unit" +
						 * " AS unit, sales_rule.item_ser AS item_ser, sales_rule.cust_code AS" +
						 * " cust_code, sales_rule.stan_code AS stan_code, sales_rule.terr_code AS" +
						 * " terr_code, es3_rule_hdr.carry_data AS carry_data, 0 AS cysls_pri_qty," +
						 * " 0 AS cysls_pri_val, 0 AS lysls_pri_qty, 0 AS lysls_pri_val, 0 AS lysls_sec_qty,"
						 * + " 0 AS lysls_sec_val, NULL AS remarks, NULL AS udf_str1, NULL AS udf_str2,"
						 * + " NULL AS udf_str3, NULL AS add_user, NULL AS add_date, NULL AS add_term,"
						 * + " NULL AS chg_user, NULL AS chg_date, NULL AS chg_term FROM es3_rule_hdr,"
						 * +
						 * " es3_rule_det, sales_cons_rulewise sales_rule, ( SELECT MIN(fr_date) AS para_fr_dt,"
						 * + " MAX(to_date) AS para_to_dt FROM period WHERE code IN ( ?, ? ) )" +
						 * " WHERE es3_rule_hdr.tran_id = es3_rule_det.tran_id AND" +
						 * " es3_rule_hdr.rule_code = 'ES3R2' AND es3_rule_hdr.active = 'Y'" +
						 * " AND TRIM(sales_rule.item_code) = es3_rule_det.apply_on_from AND" +
						 * " sales_rule.rule_code = 'OPENG' AND sales_rule.acct_prd = ? AND" +
						 * " sales_rule.cal_month BETWEEN ? AND ? AND ( para_fr_dt BETWEEN" +
						 * " es3_rule_det.valid_from AND es3_rule_det.valid_to OR para_to_dt BETWEEN" +
						 * " es3_rule_det.valid_from AND es3_rule_det.valid_to OR" +
						 * " ( para_fr_dt < es3_rule_det.valid_from AND es3_rule_det.valid_to < para_to_dt ) )"
						 * ;
						 */
						/*String sql2 = "SELECT "
								+ "    sales_rule.acct_prd          AS acct_prd, "
								+ "    sales_rule.cal_month         AS cal_month, "
								+ "    es3_rule_hdr.item_ser_from   AS item_ser_from, "
								+ "    es3_rule_hdr.item_ser_to     AS item_ser_to, "
								+ "    'ES3R7' AS rule_code, "
								+ "    sales_rule.item_code         AS item_code, "
								+ "    sales_rule.unit              AS unit, "
								+ "    sales_rule.item_ser          AS item_ser, "
								+ "    sales_rule.cust_code         AS cust_code, "
								+ "    sales_rule.stan_code         AS stan_code, "
								+ "    sales_rule.terr_code         AS terr_code, "
								+ "    es3_rule_hdr.carry_data      AS carry_data, "
								+ "    0 AS cysls_pri_qty, "
								+ "    0 AS cysls_pri_val, "
								+ "    0 AS lysls_pri_qty, "
								+ "    0 AS lysls_pri_val, "
								+ "    0 AS lysls_sec_qty, "
								+ "    0 AS lysls_sec_val, "
								+ "    NULL AS remarks, "
								+ "    NULL AS udf_str1, "
								+ "    NULL AS udf_str2, "
								+ "    NULL AS udf_str3, "
								+ "    NULL AS add_user, "
								+ "    NULL AS add_date, "
								+ "    NULL AS add_term, "
								+ "    NULL AS chg_user, "
								+ "    NULL AS chg_date, "
								+ "    NULL AS chg_term "
								+ "FROM "
								+ "    es3_rule_hdr, "
								+ "    es3_rule_det, "
								+ "    sales_cons_rulewise sales_rule, "
								+ "    ( "
								+ "        SELECT "
								+ "            MIN(fr_date) AS para_fr_dt, "
								+ "            MAX(to_date) AS para_to_dt "
								+ "        FROM "
								+ "            period "
								+ "        WHERE "
								+ "            code IN ( "
								+ "                ?, "
								+ "                ? "
								+ "            ) "
								+ "    ) "
								+ "WHERE "
								+ "    es3_rule_hdr.tran_id = es3_rule_det.tran_id "
								+ "    AND es3_rule_hdr.rule_code = 'ES3R2' "
								+ "    AND es3_rule_hdr.active = 'Y' "
								+ "    AND TRIM(sales_rule.item_code) = es3_rule_det.apply_on_from "
								+ "    AND sales_rule.rule_code = 'OPENG' "
								+ "    AND sales_rule.acct_prd = ? "
								+ "    AND sales_rule.cal_month BETWEEN ? AND ? "
								+ "    AND ( para_fr_dt BETWEEN es3_rule_det.valid_from AND es3_rule_det.valid_to "
								+ "          OR para_to_dt BETWEEN es3_rule_det.valid_from AND es3_rule_det.valid_to "
								+ "          OR ( para_fr_dt < es3_rule_det.valid_from "
								+ "               AND es3_rule_det.valid_to < para_to_dt ) )";*/
						
						preparedStatement=conn.prepareStatement(sql1);
						preparedStatement.setString(1, prdCodeFrom);
						preparedStatement.setString(2, prdCodeTo);
						preparedStatement.setString(3, acctPrd);
						preparedStatement.setString(4, prdCodeFrom);
						preparedStatement.setString(5, prdCodeTo);
						resultSet=preparedStatement.executeQuery();
						while(resultSet.next()) {
							
							System.out.println("inside while loop");
							SalesConsolidateDetail = new SalesConsolidateDetail();
							// SalesConsolidateDetail.setTranId(tranId);
							SalesConsolidateDetail.setRuleCode(resultSet.getString("rule_code"));
							SalesConsolidateDetail.setPrdCode(resultSet.getString("CAL_MONTH"));
							SalesConsolidateDetail.setPrdCodeFrom(prdCodeFrom);
							SalesConsolidateDetail.setPrdCodeTo(prdCodeTo);
							SalesConsolidateDetail.setCustCode(resultSet.getString("CUST_CODE"));
							SalesConsolidateDetail.setItemCode(resultSet.getString("item_code"));
							SalesConsolidateDetail.setItemSer(resultSet.getString("item_ser"));
							SalesConsolidateDetail.setUnit(resultSet.getString("unit"));
							SalesConsolidateDetail.setStanCode(resultSet.getString("stan_code"));
							/*
							 * SalesConsolidateDetail.setCyslsSecQty(cyslsSecQty);
							 * SalesConsolidateDetail.setCyslsSecVal(cyslsSecVal);
							 */
							SalesConsolidateDetail.setCyslsPriQty(resultSet.getDouble("cysls_pri_qty"));
							SalesConsolidateDetail.setCyslsPriVal(resultSet.getDouble("cysls_pri_val"));
							SalesConsolidateDetail.setLyslsPriQty(resultSet.getDouble("lysls_pri_qty"));
							SalesConsolidateDetail.setLyslsPriVal(resultSet.getDouble("lysls_pri_val"));
							SalesConsolidateDetail.setLyslsSecQty(resultSet.getDouble("lysls_sec_qty"));
							SalesConsolidateDetail.setLyslsSecVal(resultSet.getDouble("lysls_sec_val"));
							SalesConsolidateDetail.setRemarks(resultSet.getString("remarks"));
							SalesConsolidateDetail.setUdfStr1(resultSet.getString("udf_str1"));
							SalesConsolidateDetail.setUdfStr2(resultSet.getString("udf_str2"));
							SalesConsolidateDetail.setUdfStr3(resultSet.getString("udf_str3"));
							SalesConsolidateDetail.setTerrCode(resultSet.getString("terr_code"));
							SalesConsolidateDetail.setVersionId(versionId);
							SalesConsolidateDetail.setAcctPrd(acctPrd);
							SalesConsolidateDetail.setSource(source);
							
							/*
							 * sql2="SELECT sales_rule.fls_qty * ( - 1 ) AS cysls_sec_qty," +
							 * " sales_rule.fls_value * ( - 1 ) AS cysls_sec_val" +
							 * " FROM ADJUST_SALES_CREDIT sales_rule WHERE sales_rule.item_ser" +
							 * " BETWEEN ? AND ? AND sales_rule.prd_code BETWEEN ? AND ?" +
							 * " and TRIM(sales_rule.cust_code) =? and sales_rule.terr_code=?" +
							 * " AND sales_rule.item_ser = 'MM' and item_code=? and stan_code=?" +
							 * " and prd_code=?";
							 */			
							/*
							 * sql2="SELECT sales_rule.fls_qty * ( - 1 ) AS cysls_sec_qty, " +
							 * "sales_rule.fls_value * ( - 1 ) AS" +
							 * " cysls_sec_val FROM adjust_sales_credits sales_rule" +
							 * " WHERE sales_rule.item_ser =? AND TRIM(sales_rule.cust_code) = ?" +
							 * " AND sales_rule.terr_code = ? AND item_code = ? AND stan_code = ?" +
							 * " AND prd_code = ?";
							 */
							
							if(SalesConsolidateDetail!=null){
								
								//sqlExpr3 = "SELECT sales_rule.fls_qty * ( - 1 ) AS cysls_sec_qty, sales_rule.fls_value * ( - 1 ) AS cysls_sec_val FROM adjust_sales_credit sales_rule WHERE sales_rule.item_ser =? AND TRIM(sales_rule.cust_code) = ? AND sales_rule.terr_code = ? AND item_code = ? AND stan_code = ? AND prd_code = ?";	
								preparedStatement1=connection.prepareStatement(sqlExpr3);	
								preparedStatement1.setString(1, resultSet.getString("item_ser"));
								preparedStatement1.setString(2, resultSet.getString("CUST_CODE"));
								preparedStatement1.setString(3, resultSet.getString("terr_code"));
								preparedStatement1.setString(4, resultSet.getString("item_code"));
								preparedStatement1.setString(5, resultSet.getString("stan_code"));
								preparedStatement1.setString(6, resultSet.getString("CAL_MONTH"));
								resultSet1=preparedStatement1.executeQuery();
									
									if (resultSet1.next()) {
										SalesConsolidateDetail.setCyslsSecQty(resultSet1.getDouble("cysls_sec_qty"));
										SalesConsolidateDetail.setCyslsSecVal(resultSet1.getDouble("cysls_sec_val"));
									}
									
									//al.add(SalesConsolidateDetail);
							}
							errString = insertToPriSecSalesConsolidation(FileName, conn, xtraParams, sysDate, lastYrPrdCode,
									SalesConsolidateDetail);
							
						
						}
						
						
						 if(preparedStatement1!=null){
						    	preparedStatement1.close();
								preparedStatement1=null;
						    }
						    if(resultSet1!=null){
						    	resultSet1.close();
								resultSet1=null;
						    }
							if(preparedStatement!=null){
								 preparedStatement.close();
									preparedStatement=null;
							}
							 if(resultSet!=null){
								 resultSet.close();
									resultSet=null;
							    }
					}
					
					if ("ES3R8".equalsIgnoreCase(balGroup)) {
						System.out.println("ES3R8=======>");
						//String sql2="SELECT sales_rule.acct_prd AS acct_prd, sales_rule.cal_month AS cal_month, sales_rule.item_code AS item_code, sales_rule.unit AS unit, sales_rule.item_ser AS item_ser, sales_rule.cust_code AS cust_code, sales_rule.stan_code AS stan_code, sales_rule.terr_code AS terr_code, sales_rule.cysls_sec_qty * ( - 1 ) AS cysls_sec_qty, sales_rule.cysls_sec_val AS cysls_sec_val, sales_rule.lysls_sec_qty * ( - 1 ) AS lysls_sec_qty, sales_rule.lysls_sec_val AS lysls_sec_val, 'ES3R8' AS rule_code, ( SELECT bal_group_type FROM tax_balance_grp WHERE bal_group = 'ES3R08' ) AS source, es3_rule_det.apply_on_from AS stan_code, es3_rule_hdr.carry_data AS carry_data, 0 AS cysls_pri_qty, 0 AS cysls_pri_val, 0 AS lysls_pri_qty, 0 AS lysls_pri_val, 0 AS lysls_sec_qty, 0 AS lysls_sec_val, NULL AS remarks, NULL AS udf_str1, NULL AS udf_str2, NULL AS udf_str3, NULL AS add_user, NULL AS add_date, NULL AS add_term, NULL AS chg_user, NULL AS chg_date, NULL AS chg_term FROM es3_rule_hdr, es3_rule_det, sales_cons_rulewise sales_rule, ( SELECT MIN(fr_date) AS para_fr_dt, MAX(to_date) AS para_to_dt FROM period WHERE code IN ( ?, ? ) ) WHERE es3_rule_hdr.tran_id = es3_rule_det.tran_id AND es3_rule_hdr.rule_code = 'ES3R08' AND es3_rule_hdr.active = 'Y' AND TRIM(sales_rule.item_code) = es3_rule_det.apply_on_from AND sales_rule.rule_code = 'OPENG' AND sales_rule.acct_prd = ? AND sales_rule.cal_month BETWEEN ? AND ? AND ( para_fr_dt BETWEEN es3_rule_det.valid_from AND es3_rule_det.valid_to OR para_to_dt BETWEEN es3_rule_det.valid_from AND es3_rule_det.valid_to OR ( para_fr_dt < es3_rule_det.valid_from AND es3_rule_det.valid_to < para_to_dt ) ) UNION SELECT sales_rule.acct_prd AS acct_prd, sales_rule.cal_month AS cal_month, sales_rule.item_code AS item_code, sales_rule.unit AS unit, sales_rule.item_ser AS item_ser, sales_rule.cust_code AS cust_code, sales_rule.stan_code AS stan_code, sales_rule.terr_code AS terr_code, sales_rule.cysls_sec_qty * ( - 1 ) AS cysls_sec_qty, sales_rule.cysls_sec_val AS cysls_sec_val, sales_rule.lysls_sec_qty * ( - 1 ) AS lysls_sec_qty, sales_rule.lysls_sec_val AS lysls_sec_val, 'ES3R8' AS rule_code, ( SELECT bal_group_type FROM tax_balance_grp WHERE bal_group = 'ES3R08' ) AS source, es3_rule_det.apply_on_to AS stan_code, es3_rule_hdr.carry_data AS carry_data, 0 AS cysls_pri_qty, 0 AS cysls_pri_val, 0 AS lysls_pri_qty, 0 AS lysls_pri_val, 0 AS lysls_sec_qty, 0 AS lysls_sec_val, NULL AS remarks, NULL AS udf_str1, NULL AS udf_str2, NULL AS udf_str3, NULL AS add_user, NULL AS add_date, NULL AS add_term, NULL AS chg_user, NULL AS chg_date, NULL AS chg_term FROM es3_rule_hdr, es3_rule_det, sales_cons_rulewise sales_rule, ( SELECT MIN(fr_date) AS para_fr_dt, MAX(to_date) AS para_to_dt FROM period WHERE code IN ( ?, ? ) ) WHERE es3_rule_hdr.tran_id = es3_rule_det.tran_id AND es3_rule_hdr.rule_code = 'ES3R08' AND es3_rule_hdr.active = 'Y' AND TRIM(sales_rule.item_code) = es3_rule_det.apply_on_from AND sales_rule.rule_code = 'OPENG' AND sales_rule.acct_prd = ? AND sales_rule.cal_month BETWEEN ? AND ? ";
						preparedStatement=conn.prepareStatement(sql1);
						preparedStatement.setString(1, prdCodeFrom);
						preparedStatement.setString(2, prdCodeTo);
						preparedStatement.setString(3, acctPrd);
						preparedStatement.setString(4, prdCodeFrom);
						preparedStatement.setString(5, prdCodeTo);
						preparedStatement.setString(6, prdCodeFrom);
						preparedStatement.setString(7, prdCodeTo);
						preparedStatement.setString(8, acctPrd);
						preparedStatement.setString(9, prdCodeFrom);
						preparedStatement.setString(10, prdCodeTo);
					    resultSet=preparedStatement.executeQuery();
					    while(resultSet.next()) {
					    	SalesConsolidateDetail = new SalesConsolidateDetail();
							// SalesConsolidateDetail.setTranId(tranId);
							SalesConsolidateDetail.setRuleCode(resultSet.getString("rule_code"));
							SalesConsolidateDetail.setPrdCode(resultSet.getString("CAL_MONTH"));
							SalesConsolidateDetail.setPrdCodeFrom(prdCodeFrom);
							SalesConsolidateDetail.setPrdCodeTo(prdCodeTo);
							SalesConsolidateDetail.setCustCode(resultSet.getString("CUST_CODE"));
							SalesConsolidateDetail.setItemCode(resultSet.getString("item_code"));
							SalesConsolidateDetail.setItemSer(resultSet.getString("item_ser"));
							SalesConsolidateDetail.setUnit(resultSet.getString("unit"));
							SalesConsolidateDetail.setStanCode(resultSet.getString("stan_code"));
							SalesConsolidateDetail.setCyslsPriQty(resultSet.getDouble("cysls_pri_qty"));
							SalesConsolidateDetail.setCyslsPriVal(resultSet.getDouble("cysls_pri_val"));
							SalesConsolidateDetail.setLyslsPriQty(resultSet.getDouble("lysls_pri_qty"));
							SalesConsolidateDetail.setLyslsPriVal(resultSet.getDouble("lysls_pri_val"));
							SalesConsolidateDetail.setLyslsSecQty(resultSet.getDouble("lysls_sec_qty"));
							SalesConsolidateDetail.setLyslsSecVal(resultSet.getDouble("lysls_sec_val"));
							SalesConsolidateDetail.setRemarks(resultSet.getString("remarks"));
							SalesConsolidateDetail.setUdfStr1(resultSet.getString("udf_str1"));
							SalesConsolidateDetail.setUdfStr2(resultSet.getString("udf_str2"));
							SalesConsolidateDetail.setUdfStr3(resultSet.getString("udf_str3"));
							SalesConsolidateDetail.setTerrCode(resultSet.getString("terr_code"));
							SalesConsolidateDetail.setVersionId(versionId);
							SalesConsolidateDetail.setAcctPrd(acctPrd);
							SalesConsolidateDetail.setSource(resultSet.getString("source"));
							/*
							 * sql2="SELECT sales_rule.fls_qty * ( - 1 ) AS cysls_sec_qty," +
							 * " sales_rule.fls_value * ( - 1 ) AS cysls_sec_val" +
							 * " FROM ADJUST_SALES_CREDIT sales_rule WHERE sales_rule.prd_code" +
							 * " BETWEEN ? AND ? AND TRIM(sales_rule.cust_code) = ? AND" +
							 * " sales_rule.terr_code =? AND sales_rule.item_ser = 'AP' AND" +
							 * " item_code = ? AND stan_code = ? AND prd_code = ?";
							 */
							//sql2="SELECT sales_rule.fls_qty * ( - 1 ) AS cysls_sec_qty, sales_rule.fls_value * ( - 1 ) AS cysls_sec_val FROM adjust_sales_credits sales_rule WHERE sales_rule.prd_code='' AND TRIM(sales_rule.cust_code) = ? AND sales_rule.terr_code = ? AND sales_rule.item_ser = ? AND item_code = ? AND stan_code = ?";
						
							
							if(SalesConsolidateDetail!=null){
								
								//sqlExpr3 = "SELECT sales_rule.fls_qty * ( - 1 ) AS cysls_sec_qty, sales_rule.fls_value * ( - 1 ) AS cysls_sec_val FROM adjust_sales_credit sales_rule WHERE sales_rule.item_ser =? AND TRIM(sales_rule.cust_code) = ? AND sales_rule.terr_code = ? AND item_code = ? AND stan_code = ? AND prd_code = ?";	
								preparedStatement1=connection.prepareStatement(sqlExpr3);	
								preparedStatement1.setString(1, resultSet.getString("CAL_MONTH"));
								preparedStatement1.setString(2, resultSet.getString("CUST_CODE"));
								preparedStatement1.setString(3, resultSet.getString("terr_code"));
								preparedStatement1.setString(4, resultSet.getString("item_ser"));
								preparedStatement1.setString(5, resultSet.getString("item_code"));
								preparedStatement1.setString(6, resultSet.getString("stan_code"));
								resultSet1=preparedStatement1.executeQuery();
									if (resultSet1.next()) {
										SalesConsolidateDetail.setCyslsSecQty(resultSet1.getDouble("cysls_sec_qty"));
										SalesConsolidateDetail.setCyslsSecVal(resultSet1.getDouble("cysls_sec_val"));
									}
									//al.add(SalesConsolidateDetail);	
							}
							errString = insertToPriSecSalesConsolidation(FileName, conn, xtraParams, sysDate, lastYrPrdCode,
									SalesConsolidateDetail);
							
								
					    }
					    if(preparedStatement1!=null){
					    	preparedStatement1.close();
							preparedStatement1=null;
					    }
					    if(resultSet1!=null){
					    	resultSet1.close();
							resultSet1=null;
					    }
						if(preparedStatement!=null){
							 preparedStatement.close();
								preparedStatement=null;
						}
						 if(resultSet!=null){
							 resultSet.close();
								resultSet=null;
						    }
						
						
						
						/*
						 * pstmt1.setString(1, prdCodeFrom); pstmt1.setString(2, prdCodeTo);
						 * pstmt1.setString(3, balGroup); pstmt1.setString(4, prdCodeFrom);
						 * pstmt1.setString(5, prdCodeTo); pstmt1.setString(6, prdCodeFrom);
						 * pstmt1.setString(7, prdCodeTo); pstmt1.setString(8, balGroup);
						 * pstmt1.setString(9, prdCodeFrom); pstmt1.setString(10, prdCodeTo);
						 */
					}

				
					if (!"ES3R7".equalsIgnoreCase(balGroup) && !"ES3R8".equalsIgnoreCase(balGroup)) {
						rs1 = pstmt1.executeQuery();
						while (rs1.next()) {
							System.out.println("inside while loop");
							// tranId = checkNull(rs1.getString("TRAN_ID"));//
							prdCode = checkNull(rs1.getString("CAL_MONTH")); // as prd_code
							custCode = checkNull(rs1.getString("CUST_CODE"));//
							itemSer = checkNull(rs1.getString("ITEM_SER"));
							itemCode = checkNull(rs1.getString("ITEM_CODE"));
							unit = checkNull(rs1.getString("UNIT"));
							ruleCode = checkNull(rs1.getString("RULE_CODE"));
							stanCode = checkNull(rs1.getString("STAN_CODE"));
							cyslsSecQty = rs1.getDouble("CYSLS_SEC_QTY");// sales ->Sec curr sale
							cyslsSecVal = rs1.getDouble("CYSLS_SEC_VAL");// sales value->Sec curr sale value
							cyslsPriQty = rs1.getDouble("CYSLS_PRI_QTY");// netSalesQty-> curr year pri sale
							cyslsPriVal = rs1.getDouble("CYSLS_PRI_VAL");// netSalesVal->curr year pri value
							lyslsPriQty = rs1.getDouble("LYSLS_PRI_QTY");// lycmSalesQty -> last year pri sale
							lyslsPriVal = rs1.getDouble("LYSLS_PRI_VAL"); // lycmSalesVal -> last year pri sale value
							lyslsSecQty = rs1.getDouble("LYSLS_SEC_QTY");// lyslsSalesQty -> last year Sec sale
							lyslsSecVal = rs1.getDouble("LYSLS_SEC_VAL");// lyslsSalesVal-> last year Sec sales value
							terrCode = checkNull(rs1.getString("TERR_CODE"));
							source = checkNull(rs1.getString("SOURCE"));
							remarks = checkNull(rs1.getString("REMARKS"));
							udfStr1 = checkNull(rs1.getString("UDF_STR1"));
							udfStr2 = checkNull(rs1.getString("UDF_STR2"));
							udfStr3 = checkNull(rs1.getString("UDF_STR3"));

							// Added To Bean
							SalesConsolidateDetail = new SalesConsolidateDetail();
							// SalesConsolidateDetail.setTranId(tranId);
							SalesConsolidateDetail.setRuleCode(ruleCode);
							SalesConsolidateDetail.setPrdCode(prdCode);
							SalesConsolidateDetail.setPrdCodeFrom(prdCodeFrom);
							SalesConsolidateDetail.setPrdCodeTo(prdCodeTo);
							SalesConsolidateDetail.setCustCode(custCode);
							SalesConsolidateDetail.setItemCode(itemCode);
							SalesConsolidateDetail.setItemSer(itemSer);
							SalesConsolidateDetail.setUnit(unit);
							SalesConsolidateDetail.setStanCode(stanCode);
							SalesConsolidateDetail.setCyslsSecQty(cyslsSecQty);
							SalesConsolidateDetail.setCyslsSecVal(cyslsSecVal);
							SalesConsolidateDetail.setCyslsPriQty(cyslsPriQty);
							SalesConsolidateDetail.setCyslsPriVal(cyslsPriVal);
							SalesConsolidateDetail.setLyslsPriQty(lyslsPriQty);
							SalesConsolidateDetail.setLyslsPriVal(lyslsPriVal);
							SalesConsolidateDetail.setLyslsSecQty(lyslsSecQty);
							SalesConsolidateDetail.setLyslsSecVal(lyslsSecVal);
							SalesConsolidateDetail.setRemarks(remarks);
							SalesConsolidateDetail.setUdfStr1(udfStr1);
							SalesConsolidateDetail.setUdfStr2(udfStr2);
							SalesConsolidateDetail.setUdfStr3(udfStr3);
							SalesConsolidateDetail.setTerrCode(terrCode);
							SalesConsolidateDetail.setVersionId(versionId);
							SalesConsolidateDetail.setAcctPrd(acctPrd);
							SalesConsolidateDetail.setSource(source);
							//al.add(SalesConsolidateDetail);
							errString = insertToPriSecSalesConsolidation(FileName, conn, xtraParams, sysDate, lastYrPrdCode,
									SalesConsolidateDetail);

						}
					}

					
					
					

			}
			

				
			

		}
			System.out.println("errStringTest==>"+errString);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}catch (Exception e) {
			System.out.println("::::Exception::::" + this.getClass().getSimpleName() + ":::::" + e.getMessage());
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "", "", conn);
			printLog(FileName, "Data Outer Execption", "Processing for Tran_Id :: | Status-| " + errString + "", conn);
			connBollen = true;
			throw new ITMException(e);
		} finally {
			try {

				if ((errString == null || errString.trim().length() == 0) && !connBollen) {
					System.out.println("Connection Commited");
					errString = itmDBAccessEJB.getErrorString("", "VTDATASUCC", "", "", conn);
					printLog(FileName, "Inside finally Commited",
							"Processing for Tran_Id :: | Status-| " + errString + "", conn);
					conn.commit();

				}else if(errString.equalsIgnoreCase("nodatafound")){
					errString = itmDBAccessEJB.getErrorString("", "NODATAFND", "", "", conn);
					printLog(FileName, "Inside finally No data found", "Processing for Tran_Id :: | Status-| " + errString + "",
							conn);
				} 
				else {
					errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "", "", conn);
					printLog(FileName, "Inside finally fail", "Processing for Tran_Id :: | Status-| " + errString + "",
							conn);
					conn.rollback();
				}
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
				if (connection != null) {
					connection.close();
					connection=null;
					
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return errString;
	}

	// Data To Be inserted in New sales consolidation Table
	private String insertToPriSecSalesConsolidation(String fileName, Connection conn, String xtraParams, Date sysDate,
			String lastYrPrdCode, SalesConsolidateDetail salesConsolidateDetail) throws RemoteException, ITMException {
		String terrCode = "", terrDescr = "", tranId = "", errString = "";
		String chgTerm = "", chgUser = "", loginSiteCode = "";
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String sql1 = "";
		int period = 0, updCnt = 0, lineNo = 0;
		double lastYrsSale = 0.0, lastYrsSaleVal = 0.0;
		//SalesConsolidateDetail SalesConsolidateDetail = new SalesConsolidateDetail();
		try {
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			System.out.println("@S@ inside insertToSalesConsolidation method");
			terrCode = "";
			terrDescr = "";
				// tranId=salesValueFormList.getTranId();
				tranId = generateTranIDForSalesConsolidationProcess("sales_consolidation_rule", loginSiteCode,
						salesConsolidateDetail.getItemSer(), conn);
				printLog(fileName, "Inside Insert",
						"Processing for Tran_Id :: <" + tranId + "> | Status-| " + errString + "", conn);

				sql1 = "insert into SALES_CONS_RULEWISE(TRAN_ID,TRAN_DATE,ACCT_PRD,CUST_CODE,CAL_MONTH,PRD_CODE_FROM,PRD_CODE_TO,"
						+ "TERR_CODE,VERSION_ID,SOURCE,ITEM_CODE,UNIT,ITEM_SER,STAN_CODE,"
						+ "CYSLS_SEC_QTY,CYSLS_SEC_VAL,"
						+ "LYSLS_SEC_QTY,LYSLS_SEC_VAL,CYSLS_PRI_QTY,CYSLS_PRI_VAL,LYSLS_PRI_QTY,LYSLS_PRI_VAL"
						+ ",REMARKS,UDF_STR1,UDF_STR2,UDF_STR3,RULE_CODE,"
						+ "ADD_DATE,ADD_USER,ADD_TERM,CHG_DATE,CHG_USER,CHG_TERM)"
						+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";// ,?,?,?)";//,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, tranId);
				pstmt1.setDate(2, sysDate);
				pstmt1.setString(3, salesConsolidateDetail.getAcctPrd());
				pstmt1.setString(4, salesConsolidateDetail.getCustCode());
				pstmt1.setString(5, salesConsolidateDetail.getPrdCode());
				pstmt1.setString(6, salesConsolidateDetail.getPrdCodeFrom());
				pstmt1.setString(7, salesConsolidateDetail.getPrdCodeTo());
				pstmt1.setString(8, salesConsolidateDetail.getTerrCode());
				pstmt1.setString(9, salesConsolidateDetail.getVersionId());
				pstmt1.setString(10, salesConsolidateDetail.getSource());
				pstmt1.setString(11, salesConsolidateDetail.getItemCode());
				pstmt1.setString(12, salesConsolidateDetail.getUnit());
				pstmt1.setString(13, salesConsolidateDetail.getItemSer());
				pstmt1.setString(14, salesConsolidateDetail.getStanCode());
				pstmt1.setDouble(15, salesConsolidateDetail.getCyslsSecQty());// Secondary Current Year Qty
				pstmt1.setDouble(16, salesConsolidateDetail.getCyslsSecVal());// Secondary Current Year Value
				pstmt1.setDouble(17, salesConsolidateDetail.getLyslsSecQty());// Secondary Last Year Qty
				pstmt1.setDouble(18, salesConsolidateDetail.getLyslsSecVal());// Secondary Last Year Value
				pstmt1.setDouble(19, salesConsolidateDetail.getCyslsPriQty());// Primary Current Year Qty
				pstmt1.setDouble(20, salesConsolidateDetail.getCyslsPriVal());// Primary Current Year value
				pstmt1.setDouble(21, salesConsolidateDetail.getLyslsPriQty());// Primary Last Year Qty
				pstmt1.setDouble(22, salesConsolidateDetail.getLyslsPriVal());// Primary Last Year value
				pstmt1.setString(23, salesConsolidateDetail.getRemarks());
				pstmt1.setString(24, salesConsolidateDetail.getUdfStr1());
				pstmt1.setString(25, salesConsolidateDetail.getUdfStr2());
				pstmt1.setString(26, salesConsolidateDetail.getUdfStr3());
				pstmt1.setString(27, salesConsolidateDetail.getRuleCode());
				pstmt1.setDate(28, sysDate);
				pstmt1.setString(29, chgUser);
				pstmt1.setString(30, chgTerm);
				pstmt1.setDate(31, sysDate);
				pstmt1.setString(32, chgUser);
				pstmt1.setString(33, chgTerm);
				updCnt = pstmt1.executeUpdate();
				if (updCnt > 0) {
					errString = "";
					System.out.println("Data inserted!!!");
					conn.commit();
					printLog(fileName, "Data Insert Successs",
							"Processing for Tran_Id :: <" + tranId + "> | Status-| " + errString + "", conn);
					
				} else {
					System.out.println("Data insertion fail!!!");
					errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "", "", conn);
					printLog(fileName, "Data Insert Fail",
							"Processing for Tran_Id :: <" + tranId + "> | Status-| " + errString + "", conn);
				}
				
				pstmt1.close();
				pstmt1 = null;

			
			//conn.commit();
			/*
			 * if(al.size()==0){
			 * 
			 * errString="nodatafound"; }
			 */

		} catch (Exception e) {

			System.out.println("::::Exception::::" + this.getClass().getSimpleName() + ":::::" + e.getMessage());
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "", "", conn);
			printLog(fileName, "Data Insert Fail Execption",
					"Processing for Tran_Id :: <" + tranId + "," + lineNo + "> | Status-| " + errString + "", conn);

		}
		return errString;
	}

	// Modified by santosh to get last year data D18CSUN007.end
	public String generateTranIDForSalesConsolidationProcess(String objName, String loginSiteCode, String itemSer,
			Connection conn) throws ITMException {
		String retString = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String keyString = "", refSer = "", sysDate = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(new java.util.Date());
			System.out.println("SalesConsolidationProcess-ES3 :: objName =>" + objName);
			HashMap<String, String> transetupMap = new HashMap<String, String>();
			transetupMap = getTransetupMap("w_" + objName, conn);
			keyString = (String) transetupMap.get("key_string");
			refSer = (String) transetupMap.get("ref_ser");
			String xmlValues = "";
			xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<TRAN_ID></TRAN_ID>";
			xmlValues = xmlValues + "<TRAN_DATE>" + sysDate + "</TRAN_DATE>";
			xmlValues = xmlValues + "<SITE_CODE>" + loginSiteCode + "</SITE_CODE>";
			xmlValues = xmlValues + "<ITEM_SER>" + itemSer + "</ITEM_SER>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues for Sales Consolidation :[" + xmlValues + "]");
			System.out.println("keyString>>>>" + keyString + ">>>refSer>>>" + refSer);
			TransIDGenerator tranIdGenerator = new TransIDGenerator(xmlValues, "SYSTEM", CommonConstants.DB_NAME);
			String tranIdGenerated = tranIdGenerator.generateTranSeqID(refSer, "tran_id", keyString, conn);
			System.out.println("tranIdGenerated for SalesConsolidationProcess-ES3 => " + tranIdGenerated);
			retString = tranIdGenerated;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
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
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return retString;
	}

	private HashMap<String, String> getTransetupMap(String winName, Connection conn) throws ITMException {
		String keyString = "";
		String refSer = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, String> transetupMap = null;
		try {
			sql = "SELECT KEY_STRING, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, winName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				keyString = rs.getString("KEY_STRING");
				refSer = rs.getString("REF_SER");
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			System.out.println("ITWizardBean :: getKeyString :: keyString =>" + keyString);
			System.out.println("ITWizardBean :: getKeyString :: refSer =>" + refSer);
			transetupMap = new HashMap<String, String>();
			transetupMap.put("key_string", keyString);
			transetupMap.put("ref_ser", refSer);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
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
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return transetupMap;
	}

	private String checkNull(String input) {
		return input == null ? "" : input.trim();
	}

	// Modified by santosh
	public static class SalesConsolidateDetail {
		public String prdCode = null;
		public String prdCodeFrom = null;
		public String prdCodeTo = null;
		public String custCode = null;
		public String itemSer = null;
		public String itemCode = null;
		public String unit = null;
		public double cyslsSecQty = 0.0;
		public double cyslsSecVal = 0.0;
		public String terrCode = null;
		public String versionId = null;
		public String source = null;
		public Double cyslsPriQty = 0.0;
		public Double cyslsPriVal = 0.0;
		public Double lyslsPriQty = 0.0;
		public Double lyslsPriVal = 0.0;
		public Double lyslsSecQty = 0.0;
		public Double lyslsSecVal = 0.0;
		public String remarks = null;
		public String udfStr1 = null;
		public String udfStr2 = null;
		public String udfStr3 = null;
		public String tranId = null;
		public String ruleCode = null;
		public String acctPrd = null;
		public String stanCode = null;

		public String getPrdCode() {
			return prdCode;
		}

		public void setPrdCode(String prdCode) {
			this.prdCode = prdCode;
		}

		public String getPrdCodeFrom() {
			return prdCodeFrom;
		}

		public void setPrdCodeFrom(String prdCodeFrom) {
			this.prdCodeFrom = prdCodeFrom;
		}

		public String getPrdCodeTo() {
			return prdCodeTo;
		}

		public void setPrdCodeTo(String prdCodeTo) {
			this.prdCodeTo = prdCodeTo;
		}

		public String getCustCode() {
			return custCode;
		}

		public void setCustCode(String custCode) {
			this.custCode = custCode;
		}

		public String getItemSer() {
			return itemSer;
		}

		public void setItemSer(String itemSer) {
			this.itemSer = itemSer;
		}

		public String getItemCode() {
			return itemCode;
		}

		public void setItemCode(String itemCode) {
			this.itemCode = itemCode;
		}

		public String getUnit() {
			return unit;
		}

		public void setUnit(String unit) {
			this.unit = unit;
		}

		public double getCyslsSecQty() {
			return cyslsSecQty;
		}

		public void setCyslsSecQty(double cyslsSecQty) {
			this.cyslsSecQty = cyslsSecQty;
		}

		public double getCyslsSecVal() {
			return cyslsSecVal;
		}

		public void setCyslsSecVal(double cyslsSecVal) {
			this.cyslsSecVal = cyslsSecVal;
		}

		public String getTerrCode() {
			return terrCode;
		}

		public void setTerrCode(String terrCode) {
			this.terrCode = terrCode;
		}

		public String getVersionId() {
			return versionId;
		}

		public void setVersionId(String versionId) {
			this.versionId = versionId;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public Double getCyslsPriQty() {
			return cyslsPriQty;
		}

		public void setCyslsPriQty(Double cyslsPriQty) {
			this.cyslsPriQty = cyslsPriQty;
		}

		public Double getCyslsPriVal() {
			return cyslsPriVal;
		}

		public void setCyslsPriVal(Double cyslsPriVal) {
			this.cyslsPriVal = cyslsPriVal;
		}

		public Double getLyslsPriQty() {
			return lyslsPriQty;
		}

		public void setLyslsPriQty(Double lyslsPriQty) {
			this.lyslsPriQty = lyslsPriQty;
		}

		public Double getLyslsPriVal() {
			return lyslsPriVal;
		}

		public void setLyslsPriVal(Double lyslsPriVal) {
			this.lyslsPriVal = lyslsPriVal;
		}

		public Double getLyslsSecQty() {
			return lyslsSecQty;
		}

		public void setLyslsSecQty(Double lyslsSecQty) {
			this.lyslsSecQty = lyslsSecQty;
		}

		public Double getLyslsSecVal() {
			return lyslsSecVal;
		}

		public void setLyslsSecVal(Double lyslsSecVal) {
			this.lyslsSecVal = lyslsSecVal;
		}

		public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

		public String getUdfStr1() {
			return udfStr1;
		}

		public void setUdfStr1(String udfStr1) {
			this.udfStr1 = udfStr1;
		}

		public String getUdfStr2() {
			return udfStr2;
		}

		public void setUdfStr2(String udfStr2) {
			this.udfStr2 = udfStr2;
		}

		public String getUdfStr3() {
			return udfStr3;
		}

		public void setUdfStr3(String udfStr3) {
			this.udfStr3 = udfStr3;
		}

		public String getTranId() {
			return tranId;
		}

		public void setTranId(String tranId) {
			this.tranId = tranId;
		}

		public String getRuleCode() {
			return ruleCode;
		}

		public void setRuleCode(String ruleCode) {
			this.ruleCode = ruleCode;
		}

		public String getAcctPrd() {
			return acctPrd;
		}

		public void setAcctPrd(String acctPrd) {
			this.acctPrd = acctPrd;
		}

		public String getStanCode() {
			return stanCode;
		}

		public void setStanCode(String stanCode) {
			this.stanCode = stanCode;
		}

		@Override
		public String toString() {
			return "SalesConsolidateDetail [prdCode=" + prdCode + ", prdCodeFrom=" + prdCodeFrom + ", prdCodeTo="
					+ prdCodeTo + ", custCode=" + custCode + ", itemSer=" + itemSer + ", itemCode=" + itemCode
					+ ", unit=" + unit + ", cyslsSecQty=" + cyslsSecQty + ", cyslsSecVal=" + cyslsSecVal + ", terrCode="
					+ terrCode + ", versionId=" + versionId + ", source=" + source + ", cyslsPriQty=" + cyslsPriQty
					+ ", cyslsPriVal=" + cyslsPriVal + ", lyslsPriQty=" + lyslsPriQty + ", lyslsPriVal=" + lyslsPriVal
					+ ", lyslsSecQty=" + lyslsSecQty + ", lyslsSecVal=" + lyslsSecVal + ", remarks=" + remarks
					+ ", udfStr1=" + udfStr1 + ", udfStr2=" + udfStr2 + ", udfStr3=" + udfStr3 + ", tranId=" + tranId
					+ ", ruleCode=" + ruleCode + ", acctPrd=" + acctPrd + ", stanCode=" + stanCode + "]";
		}

	}

	// Modified by santosh to set last year data D18CSUN007 .END
	private void printLog(String fileName, String title, String msg, Connection conn) {
		String logFile = "";
		String logDir = "", filePath = "";
		File logFileDir = null;
		FileWriter fileWriter = null;

		try {

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			String logFileName = "";
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			// logFileName =
			// "IN_"+fileName+"_"+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+""+calendar.get(Calendar.SECOND);
			logFileName = fileName;

			ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
			System.out.println("@S@ inside printLog logFileName[" + logFileName + "]");
			logDir = CommonConstants.JBOSSHOME + File.separator + "applnlog" + File.separator + "salesConsRulewise";

			// filePath=finCommon.getFinparams("999999", "DAILY_NACH_ERR_GEN", conn);
			/*
			 * System.out.println("File Path::::"+filePath); logDir = filePath;
			 */

			logFileDir = new File(logDir);

			if (!logFileDir.exists()) {
				logFileDir.mkdirs();
			}

			logFile = logDir + File.separator + logFileName + ".log";

			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			java.util.Date now = new java.util.Date();
			String strDate = sdfDate.format(now);

			fileWriter = new FileWriter(logFile, true);

			fileWriter.write("\r\n");
			fileWriter.write(strDate + " " + "INFO" + " " + "[" + title + "] : " + msg);
		} catch (Exception ex) {
			ex.printStackTrace();
			// printLog("STDERR", ex);
			printLog(fileName, "STDOUT", "Inside Exception [getLog]>>" + ex.toString(), conn);
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.flush();
					fileWriter.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				// printLog("STDERR", e);
			}
		}
	}

}
