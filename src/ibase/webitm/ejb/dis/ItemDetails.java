package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.utility.E12GenericUtility;
import ibase.utility.GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessLocal;
import ibase.webitm.ejb.E12GenerateEDIEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//import org.apache.xerces.xs.LSInputList;

public class ItemDetails extends ValidatorEJB {
	// Added and replace by sarita on 2nd JAN 2018 for setting userinfo [start]
	String usersiteCode = "";

	public ItemDetails(UserInfoBean userInfoBean) {
		setUserInfo(userInfoBean);
		userInfoBean = getUserInfo();
		usersiteCode = userInfoBean.getSiteCode();
		System.out.println("usersiteCode : " + usersiteCode);
	}
	// Added and replace by sarita on 2nd JAN 2018 for setting userinfo [end]

	@SuppressWarnings({ "unchecked", "rawtypes" })
	// Changed By PriyankaC on 18March2018.[START]
	/*
	 * public ArrayList getItemListDetails(String itemCode) {
	 */
	//// Changed By PriyankaC on 18March2018.[END]
	public ArrayList getItemListDetails(String itemCode, String siteCode, String lotNo, String qty)
			throws ITMException {
		System.out.println("itemCode : " + itemCode + "siteCode " + siteCode + "quantity" + qty + "LotNo" + lotNo);// Added
																													// By
																													// PriyankaC
																													// on
																													// 18March2018.
		System.out.println("usersiteCode In ListDetails : " + usersiteCode);

		if (siteCode == null || siteCode.trim().length() == 0) {
			System.out.println("usersiteCode :" + usersiteCode);
			siteCode = usersiteCode;
		}
		String sql = "";
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		ArrayList itemList = new ArrayList();
		Timestamp orderDate = null, ordDate = null;
		String phyAttrib2IT = "", phyAttrib2 = "", phyAttrib6IT = "", phyAttrib6 = "", phyAttrib4IT = "",
				phyAttrib4 = "", itemParent = "", parentDesrc = "", unit = "", hsnNo = "";
		String lsListType = "", lsPriceList = "", lsRefNo = "";
		double lastPurcRate = 0;

		// int quantity = 0, allocQty = 0, holdQty = 0, availQty =
		// 0,llPlcount=0;
		DistCommon distCommon = new DistCommon();
		GenericUtility genericUtility = new GenericUtility();
		double qtyDb = 0;
		// Modified By Aniket C. [12th-March-2021] Add Near Expiry [Start]
		int llPlcount = 0;
		double quantity = 0.0, allocQty = 0.0, holdQty = 0.0, availQty = 0.0;
		String orderDateStr = "";
		String disVarValue = "";

		int quantityIntFrm = 0, allocQtyIntFrm = 0, holdQtyIntFrm = 0, avaiQtyIntFrm = 0;
		int saleableQuantityIntFrm = 0;
		int nearExpiryIntFrm = 0;

		double saleableQuantity = 0.0;
		double nearExpiry = 0.0;

		double tempLife = 0.0;
		double minShelfLife = 0.0;
		double maxLife = 0.0;

		Timestamp ne_check_dt1 = null, ne_check_dt2 = null, cur_date = null;
		// Modified By Aniket C. [12th-March-2021] Add Near Expiry [End]
		try {
			conn = getConnection();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			if (qty != null && qty.trim().length() > 0) {
				qtyDb = Double.parseDouble(qty);
				System.out.println("Inside double");
			}
			/*
			 * sql =
			 * "SELECT I1.ITEM_PARNT, I1.DESCR AS PARENT_DESCR, I1.PHY_ATTRIB_2, I1.PHY_ATTRIB_6, I1.PHY_ATTRIB_4, I.DESCR, "
			 * +
			 * " (SELECT SUM(S.QUANTITY - S.ALLOC_QTY - CASE WHEN S.HOLD_QTY IS NULL THEN 0 ELSE S.HOLD_QTY END )  FROM STOCK S  "
			 * +
			 * " WHERE S.ITEM_CODE = ? GROUP BY S.ITEM_CODE) AS STOCk FROM ITEM I1, ITEM I WHERE I1.ITEM_PARNT = I.ITEM_CODE (+) AND I1.ITEM_CODE = ?"
			 * ;
			 */

			// Changed B PriyankaC remove trim() from where clause. [STart] on
			// 19DEC2018.
			/*
			 * sql =
			 * "SELECT  IT.PHY_ATTRIB_2 as IT_PHY_ATTRIB_2, I.PHY_ATTRIB_2, IT.PHY_ATTRIB_6 AS IT_PHY_ATTRIB_6, I.PHY_ATTRIB_6, "
			 * + " IT.PHY_ATTRIB_4 AS IT_PHY_ATTRIB_4, I.PHY_ATTRIB_4 " +
			 * " FROM  ITEM I,  ITEM_TYPE IT WHERE  I.ITEM_CODE = ? AND TRIM(I.ITEM_TYPE) = TRIM(IT.ITEM_TYPE) "
			 * ;
			 */
			sql = "SELECT  IT.PHY_ATTRIB_2 as IT_PHY_ATTRIB_2, I.PHY_ATTRIB_2, IT.PHY_ATTRIB_6 AS IT_PHY_ATTRIB_6, I.PHY_ATTRIB_6, "
					+ " IT.PHY_ATTRIB_4 AS IT_PHY_ATTRIB_4, I.PHY_ATTRIB_4 "
					+ " FROM  ITEM I,  ITEM_TYPE IT WHERE  I.ITEM_CODE = ? AND I.ITEM_TYPE = IT.ITEM_TYPE ";

			// Changed B PriyankaC remove trim() from where clause. [END] on
			// 19DEC2018.

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, checkNull(itemCode));
			rs = pstmt.executeQuery();
			if (rs.next()) {
				phyAttrib2IT = checkNull(rs.getString("IT_PHY_ATTRIB_2"));
				phyAttrib2 = checkNull(rs.getString("PHY_ATTRIB_2"));
				phyAttrib6IT = checkNull(rs.getString("IT_PHY_ATTRIB_6"));
				phyAttrib6 = checkNull(rs.getString("PHY_ATTRIB_6"));
				phyAttrib4IT = checkNull(rs.getString("IT_PHY_ATTRIB_4"));
				phyAttrib4 = checkNull(rs.getString("PHY_ATTRIB_4"));
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			// Changed By PriyankaC on 05April2018...[START]
			/*
			 * sql = "SELECT I1.ITEM_PARNT, I1.DESCR AS PARENT_DESCR, I.UNIT, "
			 * +
			 * " (SELECT  SUM(S.QUANTITY)  FROM STOCK S WHERE S.ITEM_CODE = ? GROUP BY S.ITEM_CODE) AS QUANTITY, "
			 * +
			 * " (SELECT  SUM(S.ALLOC_QTY)  FROM STOCK S WHERE S.ITEM_CODE = ? GROUP BY S.ITEM_CODE) AS ALLOC_QTY, "
			 * +
			 * " (SELECT  SUM(S.HOLD_QTY)  FROM STOCK S WHERE S.ITEM_CODE = ? GROUP BY S.ITEM_CODE) AS HOLD_QTY, "
			 * +
			 * " (SELECT  SUM(S.QUANTITY - S.ALLOC_QTY - CASE WHEN S.HOLD_QTY IS NULL THEN 0 ELSE S.HOLD_QTY END ) FROM STOCK S WHERE "
			 * + " S.ITEM_CODE = ? GROUP BY S.ITEM_CODE ) AS AVAIL_QTY ," +
			 * " I1.HSN_NO AS HSN_NO"+
			 * " FROM ITEM I1, ITEM I WHERE I1.ITEM_PARNT = I.ITEM_CODE (+) AND I1.ITEM_CODE = ?  "
			 * ;
			 */

			/*
			 * sql ="SELECT I.ITEM_CODE, I.DESCR AS PARENT_DESCR, I.UNIT,  "
			 * +" SUM(a.QUANTITY) AS QUANTITY,  "
			 * +" SUM(a.ALLOC_QTY)  AS ALLOC_QTY,  "
			 * +" SUM(a.HOLD_QTY) AS HOLD_QTY,  "
			 * +" SUM(a.QUANTITY - a.ALLOC_QTY - CASE WHEN a.HOLD_QTY IS NULL THEN 0 ELSE a.HOLD_QTY END ) AVAIL_QTY "
			 * +" FROM STOCK A," +" LOCATION B," +" INVSTAT C," +" ITEM I"
			 * +" WHERE I.item_code = a.item_code "
			 * +" and A.LOC_CODE = B.LOC_CODE  "
			 * +" AND B.INV_STAT   = C.INV_STAT " +" AND A.ITEM_CODE  = ? "
			 * +" AND A.SITE_CODE  = ? " +" AND C.AVAILABLE  = 'Y' "
			 * +" AND C.STAT_TYPE  = 'M' "
			 * +" group by I.ITEM_CODE, I.DESCR , I.UNIT " ;
			 */

			// Modified By Aniket C.[12th-Mar-2021] Add Near Expiry [Start]

			sql = "SELECT min_shelf_life,(CASE WHEN no_sales_month IS NULL THEN 0 ELSE no_sales_month END) AS no_sales_month "
					+ " FROM item WHERE item_code = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, checkNull(itemCode));

			rs = pstmt.executeQuery();
			if (rs.next()) {
				minShelfLife = rs.getDouble("min_shelf_life");
				maxLife = rs.getDouble("no_sales_month");
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (maxLife == 0) {
				disVarValue = checkNull(distCommon.getDisparams("999999", "NEAR_EXP_SHELF_LIFE", conn));
				System.out.println(">>>>>disVarValue:" + disVarValue);
				if ("NULLFOUND".equalsIgnoreCase(disVarValue) || disVarValue == null
						|| disVarValue.trim().length() < 0) {
					disVarValue = "0";
				}
				maxLife = Double.parseDouble(disVarValue);
			}
			tempLife = maxLife;
			maxLife = minShelfLife;
			minShelfLife = tempLife;

			System.out.println("MAXLIFE:" + maxLife);
			System.out.println("MIN SHELF LIFE:" + minShelfLife);

			cur_date = new Timestamp(System.currentTimeMillis());
			cur_date = Timestamp.valueOf(sdf1.format(cur_date) + " 00:00:00.0");

			System.out.println("Current Date:" + cur_date);

			ne_check_dt1 = distCommon.CalcExpiry(cur_date, minShelfLife + 1);
			ne_check_dt2 = distCommon.CalcExpiry(cur_date, maxLife);

			if (ne_check_dt1.compareTo(ne_check_dt2) > 0) {
				Timestamp timestamp = null;
				timestamp = ne_check_dt1;
				ne_check_dt1 = ne_check_dt2;
				ne_check_dt2 = timestamp;
			}

			System.out.println("CHECK DATE 1:" + ne_check_dt1);
			System.out.println("CHECK DATE 2:" + ne_check_dt2);

			/*
			 * sql
			 * =" select m.ITEM_CODE, m.descr as PARENT_DESCR, m.unit, x.QUANTITY, x.ALLOC_QTY, x.HOLD_QTY, x.AVAIL_QTY from item m left outer join"
			 * + " (SELECT a.item_code,SUM(a.QUANTITY) AS QUANTITY, "
			 * +"SUM(a.ALLOC_QTY) AS ALLOC_QTY, "
			 * +"SUM(a.HOLD_QTY) AS HOLD_QTY, "
			 * +"SUM(a.QUANTITY - a.ALLOC_QTY - CASE WHEN a.HOLD_QTY IS NULL THEN 0 ELSE a.HOLD_QTY END ) AVAIL_QTY "
			 * +"FROM STOCK A, " +"LOCATION B, " +"INVSTAT C "
			 * +"WHERE A.LOC_CODE = B.LOC_CODE " +"AND B.INV_STAT = C.INV_STAT "
			 * +"AND A.ITEM_CODE = ? " +"AND A.SITE_CODE = ? "
			 * +"AND C.AVAILABLE = 'Y' " +"AND C.STAT_TYPE = 'M' "
			 * +"group by a.item_code) x on m.item_code = x.item_code where m.item_code = ? "
			 * ;
			 */

			sql = " select m.ITEM_CODE, m.descr as PARENT_DESCR, m.unit, x.QUANTITY, x.ALLOC_QTY, x.HOLD_QTY, x.EXPIRY_DATE, x.AVAIL_QTY, x.NEAR_EXPIRY_QUANTITY, x.SALEABLE_QUANTITY from item m left outer join"
					+ " (SELECT a.item_code, a.QUANTITY AS QUANTITY, " + "a.ALLOC_QTY AS ALLOC_QTY, "
					+ "a.HOLD_QTY AS HOLD_QTY, " + "a.EXP_DATE AS EXPIRY_DATE, "
					+ "(a.QUANTITY - a.ALLOC_QTY - CASE WHEN a.HOLD_QTY IS NULL THEN 0 ELSE a.HOLD_QTY END ) AVAIL_QTY ,"
					+ "(CASE WHEN EXP_DATE BETWEEN ? and ? THEN a.QUANTITY ELSE 0 END ) as NEAR_EXPIRY_QUANTITY,"
					+ "(CASE WHEN a.EXP_DATE > ? THEN a.QUANTITY ELSE 0 END )as SALEABLE_QUANTITY " + " FROM STOCK A, "
					+ "LOCATION B, " + "INVSTAT C " + " WHERE A.LOC_CODE = B.LOC_CODE " + "AND B.INV_STAT = C.INV_STAT "
					+ "AND A.ITEM_CODE = ? " + "AND A.SITE_CODE = ? " + "AND C.AVAILABLE = 'Y' "
					+ "AND C.STAT_TYPE = 'M') " + " x on m.item_code = x.item_code where m.item_code = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, ne_check_dt1);
			pstmt.setTimestamp(2, ne_check_dt2);
			pstmt.setTimestamp(3, ne_check_dt2);
			pstmt.setString(4, checkNull(itemCode));
			pstmt.setString(5, checkNull(siteCode));
			pstmt.setString(6, checkNull(itemCode));
			/*
			 * pstmt.setString(2, checkNull(itemCode)); pstmt.setString(3,
			 * checkNull(itemCode)); pstmt.setString(4, checkNull(itemCode));
			 * pstmt.setString(5, checkNull(itemCode));
			 */

			// Changed By PriyankaC on 05April2018...[END]

			rs = pstmt.executeQuery();

			/*
			 * if ( rs.next()) {
			 * System.out.println("Inside Resultset Dates are:"
			 * +ne_check_dt1+" AND "+ne_check_dt2); itemParent = checkNull(
			 * rs.getString("ITEM_CODE")); parentDesrc = checkNull(
			 * rs.getString("PARENT_DESCR")); unit = checkNull(
			 * rs.getString("UNIT")); quantity = rs.getInt("QUANTITY"); allocQty
			 * = rs.getInt("ALLOC_QTY"); holdQty = rs.getInt("HOLD_QTY");
			 * availQty = rs.getInt("AVAIL_QTY"); //hsnNo =
			 * rs.getString("HSN_NO"); //Changed By Priyankac On 18MARCH2018. }
			 */

			while (rs.next()) {
				itemParent = checkNull(rs.getString("ITEM_CODE"));

				parentDesrc = checkNull(rs.getString("PARENT_DESCR"));

				unit = checkNull(rs.getString("UNIT"));

				quantity = rs.getDouble("QUANTITY") + quantity;

				allocQty = rs.getDouble("ALLOC_QTY") + allocQty;

				holdQty = rs.getDouble("HOLD_QTY") + holdQty;

				availQty = rs.getDouble("AVAIL_QTY") + availQty;

				nearExpiry = rs.getDouble("NEAR_EXPIRY_QUANTITY") + nearExpiry;

				saleableQuantity = rs.getDouble("SALEABLE_QUANTITY") + saleableQuantity;

			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}

			// Modified By Aniket C.[18th-Mar-2021] Add Near Expiry [Start]
			quantityIntFrm = (int) quantity;
			allocQtyIntFrm = (int) allocQty;
			holdQtyIntFrm = (int) holdQty;
			avaiQtyIntFrm = (int) availQty;
			nearExpiryIntFrm = (int) nearExpiry;
			saleableQuantityIntFrm = (int) saleableQuantity;

			// Modified By Aniket C.[18th-Mar-2021] Add Near Expiry [End]
			itemList.add(itemParent);

			itemList.add(parentDesrc);

			itemList.add(phyAttrib2IT);
			itemList.add(phyAttrib2);
			itemList.add(phyAttrib6);
			itemList.add(phyAttrib6IT);
			itemList.add(phyAttrib4IT);
			itemList.add(phyAttrib4);
			itemList.add(unit);

			itemList.add(String.valueOf(quantityIntFrm));

			itemList.add(String.valueOf(allocQtyIntFrm));

			itemList.add(String.valueOf(holdQtyIntFrm));

			itemList.add(String.valueOf(avaiQtyIntFrm));

			// itemList.add(String.valueOf(hsnNo));//Changed By Priyankac On
			// 18MARCH2018.

			// Modified By Aniket C.[12th-Mar-2021] Add Near Expiry [End]
			lsPriceList = distCommon.getDisparams("999999", "MRP", conn);

			lsListType = distCommon.getPriceListType(lsPriceList, conn);

			orderDate = new java.sql.Timestamp(System.currentTimeMillis());

			orderDate = Timestamp.valueOf(sdf1.format(orderDate) + " 00:00:00.0");

			sql = "select count(1)  as llPlcount from pricelist where price_list=?"
					+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
					+ " and (ref_no is not null)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsPriceList);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, unit);
			pstmt.setString(4, lsListType);
			pstmt.setTimestamp(5, orderDate);
			pstmt.setTimestamp(6, orderDate);
			pstmt.setDouble(7, qtyDb);
			pstmt.setDouble(8, qtyDb);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				System.out.println("Inside llPcount");
				llPlcount = rs.getInt("llPlcount");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			double mRate = 0;
			if (!"L".equalsIgnoreCase(lsListType.trim())) {
				sql = "select max(ref_no) as ref_no from pricelist where price_list  = ? and item_code= ? and unit=? and list_type= ?"
						+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsPriceList);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, unit);
				pstmt.setString(4, lsListType);
				pstmt.setTimestamp(5, orderDate);
				pstmt.setTimestamp(6, orderDate);
				pstmt.setDouble(7, qtyDb);
				pstmt.setDouble(8, qtyDb);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsRefNo = rs.getString("ref_no");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				mRate = pickRateRefnoWise(lsPriceList, sdf.format(orderDate), itemCode, lsRefNo, lsListType, qtyDb,
						lotNo, conn);
				System.out.println("mRate::::" + mRate);

			} else {
				mRate = pickRateRefnoWise(lsPriceList, sdf.format(orderDate), itemCode, lsRefNo, "L", qtyDb, "", conn);
				System.out.println("mRate::::" + mRate);
			}
			itemList.add(String.valueOf(mRate));

			// added by monika 28 aug 2019
			lastPurcRate = getLastPurcRate(itemCode, orderDate, siteCode, conn);
			System.out.println("lastPurcRate11::::" + lastPurcRate);
			itemList.add(String.valueOf(lastPurcRate));
			// end
			// Modified By Aniket C.[12th-Mar-2021] Add Near Expiry [Start]
			itemList.add(String.valueOf(nearExpiryIntFrm));
			System.out.println("NEAR EXPIRY QUANTITY  " + nearExpiryIntFrm);

			itemList.add(String.valueOf(saleableQuantityIntFrm));
			System.out.println("SALEABLE QUANTITY " + saleableQuantityIntFrm);

			// Modified By Aniket C.[12th-Mar-2021] Add Near Expiry [End]
		} catch (Exception e) {
			System.out.println("ItemListDetails.getItemListDetails()[" + e.getMessage() + "]");
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
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("ItemListDetails.getItemListDetails()");
				e.printStackTrace();
			}
		}
		System.out.println("ItemListDetails from ItemListDetails =======>>[" + itemList + "]");
		return itemList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	// Added and replace by sarita on 2nd JAN 2018
	// public String getDocId(String itemCode)
	public String getDocId(String itemCode, UserInfoBean userInfoBean) throws ITMException {
		System.out.println("inside getDocIDImages");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String errString = "";
		Connection conn = null;
		ArrayList itemList = new ArrayList();
		try {
			// Added by sarita on 2nd JAN 2018
			setUserInfo(userInfoBean);
			conn = getConnection();

			sql = " SELECT DOC_TRANSACTION_LINK.DOC_ID FROM  DOC_TRANSACTION_LINK DOC_TRANSACTION_LINK, DOC_CONTENTS DOC_CONTENTS, ITEM ITEM "
					+ " WHERE  DOC_TRANSACTION_LINK.DOC_ID = DOC_CONTENTS.DOC_ID AND (TRIM(DOC_TRANSACTION_LINK.REF_ID) = TRIM(ITEM.ITEM_PARNT) OR "
					+ " TRIM(DOC_TRANSACTION_LINK.REF_ID) = TRIM(ITEM.ITEM_CODE)) AND  ITEM.ITEM_CODE =  ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				itemList.add(checkNull(rs.getString("DOC_ID")));
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		} catch (Exception ex) {
			System.out.println("Inside catch getDocIDImages");
			ex.printStackTrace();
			throw new ITMException(ex); // Added By Mukesh Chauhan on 07/08/19
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
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("ItemListDetails.getDocId()[" + e.getMessage() + "]");
			}
		}
		errString = itemList.toString();
		errString = errString.substring(1, errString.length() - 1);
		System.out.println("errString ::::::::[" + errString + "]");
		return errString;
	}

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input.trim();
	}

	public double pickRateRefnoWise(String priceList, String trDate, String itemCode, String aRefNo, String listType,
			double quantity, String lotNoA, Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		java.sql.Timestamp tranDate = null;
		String sql = "";
		String priceListParent = "";
		String unit = "";
		String siteCode = "";
		String locCode = "";
		String lotNo = "";
		String lotSl = "";
		String type = "";
		double rate = 0.0;
		double conv = 0;
		int cnt = 0;
		String tempDate = trDate;
		String lsRefNo = "";
		String lsLotNoFrom = "";
		String lsCalcMethc = "", lsMethAppl = "", lsCalcPlist = "", lsCalcMeth = "", lsCalcMethp = "";
		E12GenericUtility genericUtility = new E12GenericUtility();

		type = listType;
		try {
			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");

			/*
			 * if (aRefNo.indexOf("~t") > 0) {
			 * 
			 * String MulStr[] = aRefNo.split("~t"); siteCode = MulStr[0];
			 * locCode = MulStr[1]; lotNo = MulStr[2]; lotSl = MulStr[3];
			 * 
			 * } else {
			 */
			lsRefNo = aRefNo;

		} catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("type----" + type);
		if (type.trim().equals("L")) {
			rate = 0;

			try {

				sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= " + quantity
						+ " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					System.out.println("Pricelist found in pricelist master");
					rate = rs.getDouble(1);
					lsLotNoFrom = rs.getString(2);
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				} else {
					try {// try 2
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'" + " AND LIST_TYPE = 'L'";
						System.out.println("priceList_mst----" + priceList);
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							System.out.println("priceListParent----" + priceListParent);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							// System.out.println("The priceListParent if null
							// .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
										+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
										+ "AND MIN_QTY <= " + quantity + "" + " AND MAX_QTY >= " + quantity + " "
										+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								// System.out.println("The priceListParent sql
								// .. "+sql);
								if (rs2.next()) {

									rate = rs2.getDouble(1);
									lsLotNoFrom = rs2.getString(2);
									rs2.close();
									pstmt.close();
									pstmt = null;
									rs2 = null;
									System.out.println("get rate for priceListParent----" + rate);
								} else {
									rs2.close();
									pstmt.close();
									pstmt = null;
									rs2 = null;
									// rate =
									// pickRate(priceListParent,tempDate,itemCode,aLotNo,listType,
									// quantity,conn);
									return rate;

									// return -1; //change done by kunal on
									// 29/11/12 as per Manoj Sharma instruction

								}
							} // try 3
							catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						} // if
					} // try 2
					catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}
				// else if
				// rs.close();
				// pstmt.clearParameters();
				// pstmt.close();
				// pstmt = null;
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("L"))

		if (type.trim().equals("F")) // FIXED PRICE ON DATE
		{
			rate = 0;
			try {// try 1
				sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'F' " + "AND MIN_QTY <= " + quantity
						+ " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					lsLotNoFrom = rs.getString(2);
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				}

				else {
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'" + " AND LIST_TYPE = 'L'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							// System.out.println("The priceListParent if null
							// .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
										+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
										+ "AND MIN_QTY <= " + quantity + " "

										+ "AND MAX_QTY >= " + quantity + " " + "AND LIST_TYPE = 'F' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									lsLotNoFrom = rs2.getString(2);
									rs2.close();
									pstmt.close();
									pstmt = null;
									rs2 = null;
								} else {
									rs2.close();
									pstmt.close();
									pstmt = null;
									rs2 = null;
									return -1;
								}
							} // try 3
							catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						} // if
					} // try 2
					catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				} // else if
					// rs.close();
					// pstmt.clearParameters();
					// pstmt.close();
					// pstmt = null;
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("F"))

		if (type.trim().equals("D")) // DESPATCH
		{
			rate = 0;
			try {// try 1
				sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= " + quantity
						+ " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					lsLotNoFrom = rs.getString(2);
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
				}

				else {
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'" + " AND LIST_TYPE = 'L'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
										+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
										+ "AND MIN_QTY <= " + quantity + " "

										+ "AND MAX_QTY >= " + quantity + " " + "AND LIST_TYPE = 'L' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									lsLotNoFrom = rs2.getString(2);
									rs2.close();
									rs2 = null;
									pstmt.close();
									pstmt = null;
								} else {
									rate = 0;

									try {// try 4
										rs2.close();
										rs2 = null;
										pstmt.close();
										pstmt = null;
										sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
												+ priceList + "' " + "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND LIST_TYPE = 'B'" + " and ref_no = '" + lsRefNo + "' "
												+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity
												+ " " + "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '"
												+ lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											lsLotNoFrom = rs2.getString(2);
											rs2.close();
											rs2 = null;
											pstmt.close();
											pstmt = null;
										} else {
											try {// try 5
												rs2.close();
												rs2 = null;
												pstmt.close();
												pstmt = null;
												sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
														+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'"
														+ " AND LIST_TYPE = 'B'";
												pstmt = conn.prepareStatement(sql);
												rs2 = pstmt.executeQuery();
												if (rs2.next()) {
													priceListParent = rs2.getString(1);
													// System.out.println("The
													// priceListParent is ....
													// "+priceListParent);
												}
												rs2.close();
												rs2 = null;
												pstmt.close();
												pstmt = null;
												if ((priceListParent == null)
														|| (priceListParent.trim().length() == 0)) {
													priceListParent = "";
													System.out.println(
															"The priceListParent if null  .... " + priceListParent);
													return -1;
												}
												if (priceListParent.trim().length() > 0) {
													try {// try 6
														sql = "SELECT RATE,lot_no__from FROM PRICELIST "
																+ "WHERE PRICE_LIST = '" + priceListParent + "' "
																+ "AND ITEM_CODE = '" + itemCode + "' "
																+ "AND LIST_TYPE = 'B'" + "  and ref_no = '" + lsRefNo
																+ "' " + "AND MIN_QTY <= " + quantity + " "
																+ "AND MAX_QTY >= " + quantity + " "
																+ "AND LOT_NO__FROM <= '" + lotNo + "' "
																+ "AND LOT_NO__TO >= '" + lotNo + "' "
																+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setTimestamp(1, tranDate);
														pstmt.setTimestamp(2, tranDate);
														rs3 = pstmt.executeQuery();
														System.out.println("The priceListParent sql .. " + sql);
														if (rs3.next()) {
															rate = rs3.getDouble(1);
															lsLotNoFrom = rs3.getString(2);
															rs3.close();
															rs3 = null;
															pstmt.close();
															pstmt = null;
														} else {
															rs3.close();
															rs3 = null;
															pstmt.close();
															pstmt = null;
															return -1;
														}
													} // try 6
													catch (Exception e) {
														System.out.println(
																"Exception...[pickRate] " + sql + e.getMessage());
														e.printStackTrace();
														throw new ITMException(e);
													}
												} // if
											} // try 5
											catch (Exception e) {
												System.out.println("Exception...[pickRate] " + sql + e.getMessage());
												e.printStackTrace();
												throw new ITMException(e);
											}
										} // else if
									} // try 4
									catch (Exception e) {
										System.out.println("Exception...[pickRate] " + sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // else if
									// rs2.close();
							} // try 3
							catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						} // if
					} // try 2
					catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				} // else if
					// rs.close();
					// pstmt.clearParameters();
					// pstmt.close();
					// pstmt = null;
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("D"))

		if (type.trim().equals("B")) // BATCH PRICE
		{
			rate = 0;
			try {// try 1
				if (lotNoA != null && lotNoA.trim().length() > 0) {
					sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
							+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' " + "AND MIN_QTY <= "
							+ quantity + " " + "AND MAX_QTY >= " + quantity + " " + " AND LOT_NO__FROM <= '" + lotNoA
							+ "' " + "AND LOT_NO__TO >= '" + lotNoA + "'" + "AND EFF_FROM <= ? "
							+ "AND VALID_UPTO >= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, tranDate);
					pstmt.setTimestamp(2, tranDate);
					rs = pstmt.executeQuery();

					if (rs.next()) {
						System.out.println("Pricelist found in pricelist master");
						rate = rs.getDouble(1);
						lsLotNoFrom = rs.getString(2);
						// rs.close();
						// pstmt.close();
						// pstmt = null;
						// rs = null;
					}
					rs.close();// [rs and pstmt closed and nulled by Pavan R]
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				if (rate <= 0) {
					sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
							+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B'" + " and ref_no ='" + lsRefNo
							+ "' " + "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
							// + "AND LOT_NO__FROM <= '"+lotNo+"' "
							// + "AND LOT_NO__TO >= '"+lotNo+"' "
							+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, tranDate);
					pstmt.setTimestamp(2, tranDate);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						lsLotNoFrom = rs.getString(2);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						try {// try 2
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
									+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'"
									+ " AND LIST_TYPE = 'B'";
							pstmt = conn.prepareStatement(sql);
							rs2 = pstmt.executeQuery();
							if (rs2.next()) {
								priceListParent = rs2.getString(1);
							}
							rs2.close();
							rs2 = null;
							pstmt.close();
							pstmt = null;
							if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
								priceListParent = "";
								System.out.println("The priceListParent if null  .... " + priceListParent);
								return -1;
							}
							if (priceListParent.trim().length() > 0) {
								try {// try 3
									sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
											+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
											+ "AND LIST_TYPE = 'B'" + " and ref_no ='" + lsRefNo + "' "
											+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
											// + "AND LOT_NO__FROM <=
											// '"+lotNo+"' "
											// + "AND LOT_NO__TO >= '"+lotNo+"'
											// "
											+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setTimestamp(1, tranDate);
									pstmt.setTimestamp(2, tranDate);
									rs2 = pstmt.executeQuery();
									// System.out.println("The priceListParent
									// sql .. "+sql);
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										lsLotNoFrom = rs2.getString(2);
										rs2.close();
										rs2 = null;
										pstmt.close();
										pstmt = null;
									} else {
										rs2.close();
										rs2 = null;
										pstmt.close();
										pstmt = null;
										return -1;
									}
								} // try 3
								catch (Exception e) {
									System.out.println("Exception...[pickRate] " + sql + e.getMessage());
									e.printStackTrace();
									throw new ITMException(e);
								}
							} // if
						} // try 2
						catch (Exception e) {
							System.out.println("Exception...[pickRate] " + sql + e.getMessage());
							e.printStackTrace();
							throw new ITMException(e);
						}
					}
				} // else if
					// rs.close();
					// pstmt.clearParameters();
					// pstmt.close();
					// pstmt = null;
			} // try1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("B"))

		type = listType;
		if ((type.trim().equals("M")) || (type.trim().equals("N"))) // Discount
																	// PRICE
		{
			rate = 0;
			System.out.println("Inside type ::-<M><N>-::");
			try {// try 1
				sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = '" + type + "' " + "AND MIN_QTY <= "
						+ quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
						+ "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					lsLotNoFrom = rs.getString(2);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {
					type = listType;
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'" + " AND LIST_TYPE = '"
								+ type + "'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							System.out.println("The priceListParent if null  .... " + priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							type = listType;
							try {// try 3
								sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
										+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
										+ "AND LIST_TYPE = '" + type + "' " + "AND MIN_QTY <= " + quantity + " "
										+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
										+ "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									lsLotNoFrom = rs2.getString(2);
									rs2.close();
									rs2 = null;
									pstmt.close();
									pstmt = null;
								} else {
									rs2.close();
									rs2 = null;
									pstmt.close();
									pstmt = null;
									return -1;
								}
							} // try 3
							catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						} // if
					} // try 2
					catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				} // else
					// rs.close();
					// pstmt.clearParameters();
					// pstmt.close();
					// pstmt = null;
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if((type.trim().equals("M")) || (type.trim().equals("N")))

		// if(rate>0)
		// {
		// if(priceList!=null && priceList.trim().length()>0)
		// {
		// try
		// {
		// sql = "select calc_method from pricelist_mst where price_list =?";
		// pstmt = conn.prepareStatement(sql);
		// pstmt.setString(1, priceList);
		// rs = pstmt.executeQuery();
		// if (rs.next())
		// {
		// lsCalcMethc = rs.getString("calc_method");
		// //System.out.println("Rate is .*...."+rate);
		// }
		// rs.close();
		// rs = null;
		// pstmt.close();
		// pstmt = null;
		//
		//
		// sql =
		// "select method_applicable from pricelist_mst where price_list = ?";
		// pstmt = conn.prepareStatement(sql);
		// pstmt.setString(1, priceListParent);
		// rs = pstmt.executeQuery();
		// if (rs.next())
		// {
		// lsMethAppl = rs.getString("method_applicable");
		// //System.out.println("Rate is .*...."+rate);
		// }
		// rs.close();
		// rs = null;
		// pstmt.close();
		// pstmt = null;
		//
		// } catch (SQLException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// else
		// {
		// try
		// {
		// sql =
		// "select calc_method, method_applicable from pricelist_mst where
		// price_list =?";
		// pstmt = conn.prepareStatement(sql);
		// pstmt.setString(1, priceList);
		// rs = pstmt.executeQuery();
		// if (rs.next())
		// {
		// lsCalcMethc = rs.getString("calc_method");
		// lsMethAppl = rs.getString("method_applicable");
		// //System.out.println("Rate is .*...."+rate);
		// }
		// rs.close();
		// rs = null;
		// pstmt.close();
		// pstmt = null;
		// } catch (SQLException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// if(!lsMethAppl.equalsIgnoreCase("N"))
		// {
		// if(lsMethAppl.equalsIgnoreCase("C"))
		// {
		// lsCalcPlist=priceList;
		// lsCalcMeth=lsCalcMethc;
		// }
		// else if(lsMethAppl.equalsIgnoreCase("P"))
		// {
		// if(priceList!=null && priceList.trim().length()>0)
		// {
		// try
		// {
		// sql = "select calc_method from pricelist_mst where price_list =?";
		// pstmt = conn.prepareStatement(sql);
		// pstmt.setString(1, priceList);
		// rs = pstmt.executeQuery();
		// if (rs.next())
		// {
		// lsCalcMethp = rs.getString("calc_method");
		// rs.close();
		// rs = null;
		// pstmt.close();
		// pstmt = null;
		// }
		// else
		// {
		// if(lsCalcMethp==null || lsCalcMethp.trim().length()==0)
		// {
		// lsCalcPlist=priceList;
		// lsCalcMeth=lsCalcMethc;
		// }
		// else
		// {
		// lsCalcPlist=priceListParent;
		// lsCalcMeth=lsCalcMethp;
		// }
		// }
		// } catch (SQLException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// else
		// {
		// lsCalcPlist=priceList;
		// lsCalcMeth=lsCalcMethc;
		// }
		// }
		//
		// if(lsCalcMeth!=null && lsCalcMeth.trim().length()>0)
		// {
		// //
		// }
		// }
		// }

		// if(type.trim().equals("I")) //Inventory
		// {
		// rate = 0;
		// if ((lotSl == null) || (lotSl.trim().length() == 0))
		// {
		// System.out.println("Inside type ::-<I>-::");
		// try
		// {//try 1
		// sql = "SELECT RATE FROM STOCK "
		// + "WHERE ITEM_CODE = '"+itemCode+"' "
		// + "AND SITE_CODE = '"+siteCode+"' "
		// + "AND LOC_CODE = '"+locCode+"' "
		// + "AND LOT_NO = '"+lotNo+"'";
		// pstmt = conn.prepareStatement(sql);
		// rs = pstmt.executeQuery();
		// if (rs.next())
		// {
		// rate = rs.getDouble(1);
		// //System.out.println("Rate is .*...."+rate);
		// }
		// rs.close();
		// rs = null;
		// pstmt.close();
		// pstmt = null;
		// }//try
		// catch(Exception e)
		// {
		// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
		// e.printStackTrace();
		// throw new ITMException(e);
		// }
		// }//if
		// else
		// {
		// try
		// {//try 1
		// sql = "SELECT RATE FROM STOCK "
		// + "WHERE ITEM_CODE = '"+itemCode+"' "
		// + "AND SITE_CODE = '"+siteCode+"' "
		// + "AND LOC_CODE = '"+locCode+"' "
		// + "AND LOT_NO = '"+lotNo+"' "
		// + "AND LOT_SL = '"+lotSl+"'";
		// pstmt = conn.prepareStatement(sql);
		// rs = pstmt.executeQuery();
		// if (rs.next())
		// {
		// rate = rs.getDouble(1);
		// //System.out.println("Rate is .*...."+rate);
		// }
		// rs.close();
		// rs = null;
		// pstmt.close();
		// pstmt = null;
		// }//try
		// catch(Exception e)
		// {
		// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
		// e.printStackTrace();
		// throw new ITMException(e);
		// }
		// }//else
		// }//if(type.trim().equals("I")) //Inventory
		// System.out.println("Rate From DisCommon***:::::["+rate+"]");
		return (rate);

	}

	// added by monika 28 august 2019
	private double getLastPurcRate(String itemCode, Timestamp ordDate, String siteCode, Connection conn) {
		double lastPurRate = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		try {
			sql = "SELECT DDF_GET_LASTPURRATE(?,?,?) FROM DUAL ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setTimestamp(2, ordDate);
			pstmt.setString(3, siteCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				lastPurRate = rs.getDouble(1);
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
		return lastPurRate;
	} // end

}
