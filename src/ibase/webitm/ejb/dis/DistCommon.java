/*......................     win_name=w_dist_order(d_dist_order_edit/d_distorderdet_brow)
Name :- BaseInfo Pvt Ltd.
Modification:-
		Reason						Date[Like 05052007 all modified code should contain this so that search easier]

1- if listtype is i then find the site code .lotno from lotno parameter [25052007]
2-

3-					
.............................................*/
package ibase.webitm.ejb.dis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

//import com.ibm.db2.jcc.a.p;
import org.json.JSONArray;
import org.json.JSONObject;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;

public class DistCommon {
	E12GenericUtility genericUtility = new E12GenericUtility();

	/**
	 * Gets the String tax_chap, tax_class, tax_env, price_list or price_list__clg
	 * 
	 * @param siteCodeFrom
	 *            The code of from site
	 * @param siteCodeTo
	 *            The code of to site
	 * @param itemCode
	 *            The code of the item
	 * @param tranType
	 *            The type of transaction
	 * @param itemSer
	 *            The division the item belongs to
	 * @param caseString
	 *            This can TAX_CHAP, TAX_CLASS, PRICE_LIST, PRICE_LIST__CLG or
	 *            TAX_ENV
	 * @param conn
	 *            Database connection
	 * @return tax_chap, tax_class, tax_env, price_list or price_list__clg as string
	 *         based on the caseString argument
	 * @exception ITMException
	 */
	public String setPlistTaxClassEnv(String siteCodeFrom, String siteCodeTo, String itemCode, String tranType,
			String itemSer, String caseString, Connection conn) throws ITMException {
		System.out.println("--------------[setPlistTaxClassEnv]---------------------------------");

		System.out.println("DP[siteCodeto/SiteCodeFrom/itemCode/tranType/itemSer/caseString/conn]");
		if (siteCodeFrom != null)
			System.out.println("Siite Code From:-[" + siteCodeFrom + "]");
		if (siteCodeTo != null)
			System.out.println("Site Code To:-[" + siteCodeTo + "]");
		if (itemCode != null)
			System.out.println("Item Code:-[" + itemCode + "]");
		if (tranType != null)
			System.out.println("TranType:-[" + tranType + "]");
		if (caseString != null)
			System.out.println("Case String:-[" + caseString + "]");
		if (itemSer != null)
			System.out.println("Item Ser:-[" + itemSer + "]");
		if (conn != null)
			System.out.println("Connection Found[" + conn + "]");
		System.out.println("-----------------------------------------------");

		String sql = "";
		String sql2 = "";
		String priceList = "";
		String priceListCLG = "";
		String taxClass = "";
		String taxEnv = "";
		String returnVlaue = "";
		// Statement stmt = null;
		// Statement stmt2 = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;

		if (itemSer == null) {
			itemSer = "";
		}
		if (caseString.equals("PRICE_LIST")) {
			int flag = 0;
			try {
				sql = "SELECT PRICE_LIST FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
						+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ? " + "AND TRAN_TYPE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeFrom);
				pstmt.setString(2, siteCodeTo);
				pstmt.setString(3, itemSer);
				pstmt.setString(4, itemCode);
				pstmt.setString(5, tranType);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					priceList = rs.getString(1);
					System.out.println("PriceList1 :-[" + priceList + "]");
					flag = 1;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((flag == 0) || (priceList == null)) {
					flag = 0;
					// rs.close();
					priceList = "";
					sql = "SELECT PRICE_LIST FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
							+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
							+ "AND TRAN_TYPE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeFrom);
					pstmt.setString(2, siteCodeTo);
					pstmt.setString(3, itemCode);
					pstmt.setString(4, tranType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						priceList = rs.getString(1);
						System.out.println("PriceList2 :-[" + priceList + "]");
						flag = 1;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if ((flag == 0) || (priceList == null)) // if 2
					{
						// rs.close();
						flag = 0;
						priceList = "";
						sql = "SELECT PRICE_LIST FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
								+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
								+ "AND TRAN_TYPE = '' ";
						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, siteCodeFrom);
						pstmt.setString(2, siteCodeTo);
						pstmt.setString(3, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceList = rs.getString(1);
							System.out.println("PriceList3 :-[" + priceList + "]");
							flag = 1;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((flag == 0) || (priceList == null)) // if 3
						{
							// rs.close();
							priceList = "";
							sql = "SELECT PRICE_LIST FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
									+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ' ' "
									+ "AND TRAN_TYPE = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeFrom);
							pstmt.setString(2, siteCodeTo);
							pstmt.setString(3, itemSer);
							pstmt.setString(4, tranType);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								priceList = rs.getString(1);
								System.out.println("PriceList4 :-[" + priceList + "]");
								flag = 1;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if ((flag == 0) || (priceList == null)) // if 4
							{
								flag = 0;
								// rs.close();
								priceList = "";
								sql = "SELECT PRICE_LIST FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
										+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ' ' "
										+ "AND TRAN_TYPE = ' '";
								pstmt = conn.prepareStatement(sql);

								pstmt.setString(1, siteCodeFrom);
								pstmt.setString(2, siteCodeTo);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceList = rs.getString(1);
									System.out.println("PriceList5 :-[" + priceList + "]");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if ((flag == 0) || (priceList == null)) // if 5
								{
									flag = 0;
									// rs.close();
									priceList = "";
									sql = "SELECT PRICE_LIST FROM DISTORDER_TYPE " + "WHERE TRAN_TYPE = '" + tranType
											+ "'";
									pstmt = conn.prepareStatement(sql);// stmt changed to pstmt
									rs = pstmt.executeQuery();
									if (rs.next()) {
										priceList = rs.getString(1);
										System.out.println("PriceList6 :-[" + priceList + "]");
									} // if
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								} // if 5
							} // if 4
						} // if 3
					} // if 2
				} // if 1
				// rs.close();
				// pstmt.clearParameters();
			} // try
			catch (SQLException e) {
				System.out.println("SQLException : :" + sql + e.getMessage() + ":");
			} catch (Exception ex) {
				System.out.println("Exception []::" + sql + ex.getMessage());
				ex.printStackTrace();
			}
			if ((priceList == null) || (priceList.trim().length() == 0)) {
				priceList = getDisparams("999999", "DEFAULT_PRICE", conn);
			}
			System.out.println("Returning the priceList From Dist Common------->");
			System.out.println("************** [" + priceList + "]**************");
			returnVlaue = priceList;
		} // if(caseString.equals("PRICE_LIST"))

		else if (caseString.equals("PRICE_LIST__CLG")) {
			int flag = 0;
			try {
				sql = "SELECT PRICE_LIST__EXCISE FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
						+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ? " + "AND TRAN_TYPE = ? ";

				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, siteCodeFrom);
				pstmt.setString(2, siteCodeTo);
				pstmt.setString(3, itemSer);
				pstmt.setString(4, itemCode);
				pstmt.setString(5, tranType);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					priceListCLG = rs.getString(1);
					System.out.println("PriceListCLG1:-[" + priceListCLG + "]");
					flag = 1;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((flag == 0) || (priceListCLG == null)) // if 1
				{
					// rs.close();

					priceListCLG = "";
					// System.out.println("Inside else if 1 CLG");
					sql = "SELECT PRICE_LIST__EXCISE FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
							+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
							+ "AND TRAN_TYPE = ? ";

					pstmt = conn.prepareStatement(sql);

					pstmt.setString(1, siteCodeFrom);
					pstmt.setString(2, siteCodeTo);
					pstmt.setString(3, itemCode);
					pstmt.setString(4, tranType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						priceListCLG = rs.getString(1);
						System.out.println("PriceListCLG2:-[" + priceListCLG + "]");
						flag = 1;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if ((flag == 0) || (priceListCLG == null)) // if 2
					{
						// rs.close();
						priceListCLG = "";
						// System.out.println("Inside else if 2 CLG");
						sql = "SELECT PRICE_LIST__EXCISE FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
								+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
								+ "AND TRAN_TYPE = ' ' ";

						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, siteCodeFrom);
						pstmt.setString(2, siteCodeTo);
						pstmt.setString(3, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListCLG = rs.getString(1);
							System.out.println("PriceListCLG3:-[" + priceListCLG + "]");
							flag = 1;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((flag == 0) || (priceListCLG == null)) // if 3
						{
							// rs.close();
							priceListCLG = "";
							sql = "SELECT PRICE_LIST__EXCISE FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
									+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = '' "
									+ "AND TRAN_TYPE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeFrom);
							pstmt.setString(2, siteCodeTo);
							pstmt.setString(3, itemSer);
							pstmt.setString(4, tranType);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								priceListCLG = rs.getString(1);
								System.out.println("PriceListCLG4:-[" + priceListCLG + "]");
								flag = 1;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if ((flag == 0) || (priceListCLG == null)) // if 4
							{
								// rs.close();
								priceListCLG = "";
								sql = "SELECT PRICE_LIST__EXCISE FROM ITEM_ACCT_DETR_DIST "
										+ "WHERE SITE_CODE__FROM = ? " + "AND SITE_CODE__TO = ? "
										+ "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ' ' " + "AND TRAN_TYPE = ' ' ";

								pstmt = conn.prepareStatement(sql);

								pstmt.setString(1, siteCodeFrom);
								pstmt.setString(2, siteCodeTo);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceListCLG = rs.getString(1);
									System.out.println("PriceListCLG5:-[" + priceListCLG + "]");
									flag = 1;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if ((flag == 0) || (priceListCLG == null)) // if
									// 5
								{
									// rs.close();
									priceListCLG = "";
									sql = "SELECT PRICE_LIST__CLG FROM DISTORDER_TYPE " + "WHERE TRAN_TYPE = '"
											+ tranType + "'";
									pstmt = conn.prepareStatement(sql);// stmt changed to pstmt
									rs = pstmt.executeQuery();
									if (rs.next()) {
										priceListCLG = rs.getString(1);
										System.out.println("PriceListCLG6:-[" + priceListCLG + "]");
										flag = 1;
									} // if
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								} // if 5
							} // if 4
						} // if 3
					} // if 2
				} // if 1
				// pstmt.clearParameters();

			} // try
			catch (SQLException e) {
				System.out.println("SQLException : :" + sql + e.getMessage() + ":");
			} catch (Exception ex) {
				System.out.println("Exception []::" + sql + ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex);
			}
			if ((priceListCLG == null) || (priceListCLG.trim().length() == 0)) {
				priceListCLG = getDisparams("999999", "PRICE_LIST__CLG", conn);
			}
			System.out.println("Returning the priceListCLG From Dist Common------->");
			System.out.println("************** [" + priceListCLG + "]**************");
			returnVlaue = priceListCLG;
		} // else if(caseString.equals("PRICE_LIST__CLG"))

		else if (caseString.equalsIgnoreCase("TAX_CLASS")) {
			int flag = 0;
			try {// try 1
				sql = "SELECT TAX_CLASS FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
						+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ? " + "AND TRAN_TYPE = ?";

				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, siteCodeFrom);
				pstmt.setString(2, siteCodeTo);
				pstmt.setString(3, itemSer);
				pstmt.setString(4, itemCode);
				pstmt.setString(5, tranType);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxClass = rs.getString(1);
					flag = 1;
					// System.out.println("taxClass 1............."+taxClass);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((flag == 0) || taxClass == null || taxClass.trim().length() == 0) // if 1
				{
					flag = 0;
					// rs.close();
					rs = null;
					taxClass = "";
					// System.out.println("Inside else if 1 Class");
					try {// try 2
						sql = "SELECT TAX_CLASS FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
								+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
								+ "AND TRAN_TYPE = ?";

						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, siteCodeFrom);
						pstmt.setString(2, siteCodeTo);
						pstmt.setString(3, itemCode);
						pstmt.setString(4, tranType);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							taxClass = rs.getString(1);
							System.out.println("taxClass 2............." + taxClass);
							flag = 1;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((flag == 0) || taxClass == null || taxClass.trim().length() == 0) // if 2
						{
							flag = 0;
							// rs.close();
							rs = null;

							taxClass = "";
							// System.out.println("Inside else if 2 Class");
							try {// try 3
								taxClass = "";
								sql = "SELECT TAX_CLASS FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
										+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ?  "
										+ "AND TRAN_TYPE = ' ' ";

								pstmt = conn.prepareStatement(sql);

								pstmt.setString(1, siteCodeFrom);
								pstmt.setString(2, siteCodeTo);
								pstmt.setString(3, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									taxClass = rs.getString(1);
									System.out.println("taxClass 3............." + taxClass);
									flag = 1;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if ((flag == 0) || taxClass == null || taxClass.trim().length() == 0) // if
									// 3
								{
									flag = 0;
									// rs.close();
									// rs=null;
									// System.out.println("Inside else if 3 Class");
									taxClass = "";
									try {// try 4
										try {// try 4b
											itemSer = "";
											// rs2.close();
											sql2 = "SELECT ITEM_SER FROM SITEITEM "
													+ " WHERE SITE_CODE = ?  AND ITEM_CODE = ?  ";
											// stmt2 = conn.createStatement();
											pstmt = conn.prepareStatement(sql2);
											pstmt.setString(1, siteCodeFrom.trim());
											pstmt.setString(2, itemCode.trim());
											rs2 = pstmt.executeQuery();
											// rs2 = stmt2.executeQuery(sql2);
											if (rs2.next()) {
												itemSer = rs2.getString(1);
												System.out.println("itemSer 1............." + itemSer);
											}
											rs2.close();
											rs2 = null;
											pstmt.close();
											pstmt = null;
											if ((itemSer == null) || (itemSer.trim().length() == 0)) // if
												// 2
											{
												// System.out.println("Inside else if ...Class ");
												itemSer = "";
												try {
													sql2 = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = ? ";
													// stmt2 =
													// conn.createStatement();
													pstmt = conn.prepareStatement(sql2);
													pstmt.setString(1, itemCode.trim());
													rs2 = pstmt.executeQuery();

													// rs2 =
													// stmt2.executeQuery(sql2);
													if (rs2.next()) {
														itemSer = rs2.getString(1);
														System.out.println("itemSer 2............." + itemSer);
													}
													rs2.close();
													rs2 = null;
													pstmt.close();
													pstmt = null;
												} catch (Exception ex) {
													System.out.println("Exception []::" + sql2 + ex.getMessage());
													ex.printStackTrace();
												}
											} // if 2
											// stmt2.close();
											// stmt2 = null;
										} // try 4b
										catch (Exception ex) {
											System.out.println("Exception []::" + sql2 + ex.getMessage());
											ex.printStackTrace();
										}
										sql = "SELECT TAX_CLASS FROM ITEM_ACCT_DETR_DIST "
												+ "WHERE SITE_CODE__FROM = ? " + "AND SITE_CODE__TO = ? "
												+ "AND ITEM_SER = ? " + "AND ITEM_CODE = ' ' " + "AND TRAN_TYPE = ?";

										pstmt = conn.prepareStatement(sql);

										pstmt.setString(1, siteCodeFrom);
										pstmt.setString(2, siteCodeTo);
										pstmt.setString(3, itemSer);
										pstmt.setString(4, tranType);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											taxClass = rs.getString(1);
											System.out.println("taxClass ............." + taxClass);

											flag = 0;
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if ((flag == 0) || taxClass == null || taxClass.trim().length() == 0) // if
											// 4
										{
											flag = 0;
											// rs.close();

											System.out.println("Inside else if 4 Class");
											taxClass = "";
											try {// try 5
												taxClass = "";
												sql = "SELECT TAX_CLASS FROM ITEM_ACCT_DETR_DIST "
														+ "WHERE SITE_CODE__FROM = ? " + "AND SITE_CODE__TO = ? "
														+ "AND ITEM_SER = ? " + "AND ITEM_CODE = ' ' "
														+ "AND TRAN_TYPE = ' '";

												pstmt = conn.prepareStatement(sql);

												pstmt.setString(1, siteCodeFrom);
												pstmt.setString(2, siteCodeTo);
												pstmt.setString(3, itemSer);
												rs = pstmt.executeQuery();
												if (rs.next()) {
													taxClass = rs.getString(1);
													System.out.println("taxClass 4............." + taxClass);

													flag = 1;
												}
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												if ((flag == 0) || taxClass == null || taxClass.trim().length() == 0) // if
													// 5
												{
													flag = 0;
													// rs.close();
													System.out.println("Inside else if 5 Class");
													taxClass = "";
													try {// try 6
														taxClass = "";
														sql = "SELECT TAX_CLASS FROM ITEM_ACCT_DETR_DIST "
																+ "WHERE SITE_CODE__FROM = ? "
																+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' "
																+ "AND ITEM_CODE = ' ' " + "AND TRAN_TYPE = ' '";

														pstmt = conn.prepareStatement(sql);

														pstmt.setString(1, siteCodeFrom);
														pstmt.setString(2, siteCodeTo);
														rs = pstmt.executeQuery();
														if (rs.next()) {
															taxClass = rs.getString(1);
															System.out.println("taxClass 5............." + taxClass);
															flag = 1;
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
														if ((flag == 0) || taxClass == null
																|| taxClass.trim().length() == 0) // if
															// 6
														{
															flag = 0;
															// rs.close();
															System.out.println("Inside else if 6 Class");
															taxClass = "";
															try {// try 7
																sql = "SELECT TAX_CLASS FROM DISTORDER_TYPE "
																		+ "WHERE TRAN_TYPE = ? ";
																// stmt =
																// conn.createStatement();
																pstmt = conn.prepareStatement(sql);
																pstmt.setString(1, tranType.trim());
																rs = pstmt.executeQuery();
																// rs =
																// stmt.executeQuery(sql);
																if (rs.next()) {
																	taxClass = rs.getString(1);
																	System.out.println(
																			"taxClass 6............." + taxClass);
																} // if
																rs.close();
																rs = null;
																pstmt.close();
																pstmt = null;
																// stmt.close();
																// stmt = null;
															} // try 7
															catch (Exception ex) {
																System.out.println("Exception [1]::" + sql + ex);
																ex.printStackTrace();
															}
														} // if 6
													} // try 6
													catch (Exception ex) {
														System.out.println("Exception [2]::" + sql + ex.getMessage());
														ex.printStackTrace();
													}
												} // if 5
											} // try 5
											catch (Exception ex) {
												System.out.println("Exception [3]::" + sql + ex.getMessage());
												ex.printStackTrace();
											}
										} // if 4
									} // try 4
									catch (Exception ex) {
										System.out.println("Exception [4]::" + sql + ex.getMessage());
										ex.printStackTrace();
									}
								} // if 3
							} // try 3
							catch (Exception ex) {
								System.out.println("Exception [5]::" + sql + ex.getMessage());
								ex.printStackTrace();
								throw new ITMException(ex);
							}
						} // if 2
					} // try 2
					catch (Exception ex) {
						System.out.println("Exception [6]::" + sql + ex.getMessage());
						ex.printStackTrace();
						throw new ITMException(ex);
					}
				} // if 1
				// pstmt.clearParameters();

			} // try 1
			catch (SQLException e) {
				System.out.println("SQLException [7]: :" + sql + e.getMessage() + ":");
				e.printStackTrace();
			} catch (Exception ex) {
				System.out.println("Exception [8]::" + ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex);
			}
			if (taxClass == null)
				taxClass = " ";
			System.out.println("Returning the taxClass From Dist Common------->");
			System.out.println("************** [" + taxClass + "]**************");
			returnVlaue = taxClass;
		} // else if(caseString.equals("TAX_CLASS"))

		else if (caseString.equals("TAX_ENV")) {
			int flag = 0;
			try {
				sql = "SELECT TAX_ENV FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
						+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ? " + "AND TRAN_TYPE = ?";

				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, siteCodeFrom);
				pstmt.setString(2, siteCodeTo);
				pstmt.setString(3, itemSer);
				pstmt.setString(4, itemCode);
				pstmt.setString(5, tranType);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxEnv = rs.getString(1);
					// System.out.println("taxEnv ............."+taxEnv);
					flag = 1;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if ((flag == 0) || taxEnv == null || taxEnv.trim().length() == 0) // if 1
				{
					flag = 0;
					// rs.close();
					taxEnv = "";
					System.out.println("Inside else if 1 Env");
					sql = "SELECT TAX_ENV FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
							+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
							+ "AND TRAN_TYPE = ?";

					pstmt = conn.prepareStatement(sql);

					pstmt.setString(1, siteCodeFrom);
					pstmt.setString(2, siteCodeTo);
					pstmt.setString(3, itemCode);
					pstmt.setString(4, tranType);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString(1);
						// System.out.println("taxEnv ............."+taxEnv);
						flag = 1;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if ((flag == 0) || taxEnv == null || taxEnv.trim().length() == 0) // if 2
					{
						flag = 0;
						// rs.close();
						taxEnv = "";
						System.out.println("Inside else if 2 Env");
						sql = "SELECT TAX_ENV FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
								+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ? "
								+ "AND TRAN_TYPE = ' '";

						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, siteCodeFrom);
						pstmt.setString(2, siteCodeTo);
						pstmt.setString(3, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							taxEnv = rs.getString(1);
							// System.out.println("taxEnv ............."+taxEnv);
							flag = 1;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((flag == 0) || taxEnv == null || taxEnv.trim().length() == 0) // if 3
						{
							flag = 0;
							// rs.close();
							taxEnv = "";
							System.out.println("Inside else if 3 Env");
							try {// try 4b
								itemSer = "";
								sql2 = "SELECT ITEM_SER FROM SITEITEM " + "WHERE SITE_CODE = ?  AND ITEM_CODE = ? ";
								// stmt2 = conn.createStatement();
								pstmt = conn.prepareStatement(sql2);
								pstmt.setString(1, siteCodeFrom);
								pstmt.setString(2, itemCode.trim());
								rs2 = pstmt.executeQuery();

								// rs2 = stmt2.executeQuery(sql2);
								if (rs2.next()) {
									itemSer = rs2.getString(1);
									System.out.println("itemSer 1 in env............." + itemSer);
								}
								rs2.close();
								rs2 = null;
								pstmt.close();
								pstmt = null;
								if ((itemSer == null) || (itemSer.trim().length() == 0)) // if
									// 2
								{
									System.out.println("Inside else if ...Env ");
									itemSer = "";
									try {
										sql2 = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = ? ";
										// stmt2 = conn.createStatement();
										pstmt = conn.prepareStatement(sql2);
										pstmt.setString(1, itemCode.trim());
										rs2 = pstmt.executeQuery();
										// rs2 = stmt2.executeQuery(sql2);
										if (rs2.next()) {
											itemSer = rs2.getString(1);
											System.out.println("itemSer 2 env............." + itemSer);
										}
										rs2.close();
										rs2 = null;
										pstmt.close();
										pstmt = null;
									} catch (Exception ex) {
										System.out.println("Exception []::" + sql2 + ex.getMessage());
										ex.printStackTrace();
									}
								} // if 2
								// stmt2.close();
								// stmt2 = null;
							} // try 4b
							catch (Exception ex) {
								System.out.println("Exception []::" + sql2 + ex.getMessage());
								ex.printStackTrace();
							}
							sql = "SELECT TAX_ENV FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
									+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ' ' "
									+ "AND TRAN_TYPE = ?";

							pstmt = conn.prepareStatement(sql);

							pstmt.setString(1, siteCodeFrom);
							pstmt.setString(2, siteCodeTo);
							pstmt.setString(3, itemSer);
							pstmt.setString(4, tranType);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								taxEnv = rs.getString(1);
								System.out.println("taxEnv ............." + taxEnv);
								flag = 1;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if ((flag == 0) || taxEnv == null || taxEnv.trim().length() == 0) // if 4
							{
								flag = 0;
								// rs.close();
								taxEnv = "";
								System.out.println("Inside else if 4 Env");
								sql = "SELECT TAX_ENV FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
										+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ? " + "AND ITEM_CODE = ' ' "
										+ "AND TRAN_TYPE = ' '";

								pstmt = conn.prepareStatement(sql);

								pstmt.setString(1, siteCodeFrom);
								pstmt.setString(2, siteCodeTo);
								pstmt.setString(3, itemSer);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									taxEnv = rs.getString(1);
									// System.out.println("taxEnv ............."+taxEnv);
									flag = 1;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if ((flag == 0) || taxEnv == null || taxEnv.trim().length() == 0) // if 5
								{
									flag = 0;
									// rs.close();
									taxEnv = "";
									System.out.println("Inside else if 5 Env");
									sql = "SELECT TAX_ENV FROM ITEM_ACCT_DETR_DIST " + "WHERE SITE_CODE__FROM = ? "
											+ "AND SITE_CODE__TO = ? " + "AND ITEM_SER = ' ' " + "AND ITEM_CODE = ' ' "
											+ "AND TRAN_TYPE = ' '";

									pstmt = conn.prepareStatement(sql);

									pstmt.setString(1, siteCodeFrom);
									pstmt.setString(2, siteCodeTo);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										taxEnv = rs.getString(1);
										// System.out.println("taxEnv ............."+taxEnv);
										flag = 1;
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if ((flag == 0) || taxEnv == null || taxEnv.trim().length() == 0) // if
										// 6
									{
										flag = 0;
										// rs.close();
										taxEnv = "";
										System.out.println("Inside else if 6 Env");
										sql = "SELECT TAX_ENV FROM DISTORDER_TYPE " + "WHERE TRAN_TYPE = ? ";
										// stmt = conn.createStatement();
										// comment by manazir on 23-05-09
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, tranType.trim());
										rs = pstmt.executeQuery();
										// rs = stmt.executeQuery(sql);
										if (rs.next()) {
											taxEnv = rs.getString(1);
											System.out.println("taxEnv ............." + taxEnv);
										} // if
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									} // if 6
								} // if 5
							} // if 4
						} // if 3
					} // if 2
				} // if 1
				// pstmt.clearParameters();

			} // try
			catch (SQLException e) {
				System.out.println("SQLException : :" + sql + e.getMessage() + ":");
				e.printStackTrace();
			} catch (Exception ex) {
				System.out.println("Exception []::" + sql + ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex);
			}
			if (taxEnv == null)
				taxEnv = " ";
			System.out.println("Returning the taxEnv From Dist Common------->");
			System.out.println("************** [" + taxEnv + "]**************");
			returnVlaue = taxEnv;
		} // else if(caseString.equals("TAX_ENV"))
		return returnVlaue;

	}// setPlistTaxClassEnv
	// Culculate the trantype value from disparm

	/**
	 * Gets  the string of disparm variable if not defined returns "NULLFOUND"
	 * 
	 * @param prdCode
	 *            The period code ususaaly "999999" (which means applicable to all
	 *            period)
	 * @param varName
	 *            The name of the variable for which the value is to be obtained
	 * @param conn
	 *            JDBC Database connection
	 * @return the string value of the disparm
	 * @exception ITMException
	 * @see
	 */
	public String getDisparams(String prdCode, String varName, Connection conn) {
		String retVal = null;
		String sql = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;// stmt changed to pstmt
		try {
			System.out.println(
					"Finding the variable value from Disparam for parameters[" + prdCode + "," + varName + "]");
			// sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='" + prdCode + "' AND
			// VAR_NAME ='" + varName + "'";
			sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
			pstmt = conn.prepareStatement(sql);// stmt changed to pstmt
			pstmt.setString(1, prdCode);
			pstmt.setString(2, varName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				retVal = (rs.getString(1) == null ? " " : rs.getString(1));
			} else {
				retVal = "NULLFOUND";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (retVal);
	}// End of disparm

	/**
	 * Gets the price list type as a string
	 * 
	 * @param priceList
	 *            The price list for which type is to be identified
	 * @param conn
	 *            JDBC Database connection
	 * @return The price list list type as string
	 * @exception ITMException
	 * @see
	 */
	public String getPriceListType(String priceList, Connection conn) throws ITMException {
		String listType = "";
		String sql = "";
		PreparedStatement pstmt = null; // stmt chnaged to pstmt
		ResultSet rs = null;
		try {
			sql = "SELECT LIST_TYPE FROM PRICELIST_MST WHERE PRICE_LIST = ? "; // '"
			// + priceList + "'";
			System.out.println(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			// System.out.println("LIST_TYPE... sql..."+sql);
			// 25052007
			if (rs.next()) {
				listType = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// System.out.println("listType...................."+listType);
		} catch (Exception e) {
			System.out.println("Exception...[getPriceListType] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return (listType);
	}// getPriceListType

	// ////////////////////////////manazir this has to be change
	// /////////////////////////////////

	/**
	 * Gets the rate as double
	 * 
	 * @param priceList
	 *            The price list for which type is to be identified
	 * @param trDate
	 *            The date on which the price list to be a valid
	 * @param itemCode
	 *            The item code for which the rate is to be obtained
	 * @param aLotNo
	 *            The batch/lot number
	 * @param listType
	 *            The type of the price list
	 * @param conn
	 *            JDBC Database connection
	 * @return the rate as double
	 * @exception ITMException
	 * @see
	 */
	public double pickRate(String priceList, String trDate, String itemCode, String aLotNo, String listType,
			Connection conn) throws ITMException// ---1Method
	{
		// System.out.println("--------------[pickRate]---------------------------------");
		// System.out.println("DP[priceList/trDate/itemCode/tranType/aLotNo/listType/conn]");
		// if(priceList!=null)
		// System.out.println("Price List:-["+priceList+"]");
		// if(trDate!=null)
		// System.out.println("Tran Date:-["+trDate+"]");
		// if(itemCode!=null)
		// System.out.println("Item Code:-["+itemCode+"]");
		// if(aLotNo!=null)
		// System.out.println("Lot No:-["+aLotNo+"]");
		// if(listType!=null)
		// System.out.println("List Type:-["+listType+"]");
		// if(conn!=null)
		// System.out.println("Connection Found["+conn+"]");
		// System.out.println("-----------------------------------------------");

		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		java.sql.Timestamp tranDate = null;
		String sql = "";
		String priceListParent = "";
		String siteCode = "";
		String locCode = "";
		String lotNo = "";
		String lotSl = "";
		String type = "";
		double rate = 0.0;

		type = getPriceListType(priceList, conn);
		// System.out.println("List Type From Price List:::["+type+"]");
		try {
			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");
			// System.out.println("Date in pickRate...."+tranDate);
			// 25052007
			if (type.equalsIgnoreCase("I")) {
				if (aLotNo.indexOf("~t") > 0) {
					String MulStr[] = aLotNo.split("~t");
					// System.out.println("First String ["+MulStr[0]+"]");
					// System.out.println("Second String ["+MulStr[1]+"]");
					// System.out.println("Third String ["+MulStr[2]+"]");
					// System.out.println("Four String ["+MulStr[3]+"]");
					/*
					 * siteCode = getTokenList(aLotNo,"~t"); locCode = getTokenList(aLotNo,"~t");
					 * lotNo = getTokenList(aLotNo,"~t"); lotSl = getTokenList(aLotNo,"~t");
					 */
					System.out.println("MulStr.length----" + MulStr.length);
					siteCode = MulStr[0];
					locCode = MulStr[1];
					lotNo = MulStr[2];
					// Added By Manoj dtd 04062013 to get lotSL
					if (MulStr.length > 3)
						lotSl = MulStr[3];

					// System.out.println("siteCode..**..*>"+siteCode);
					// System.out.println("locCode..**..*>"+locCode);
					// System.out.println("lotNo..**..*>"+lotNo);
					// System.out.println("lotSl..**..*>"+lotSl);
				}
			} else {
				lotNo = aLotNo;
				// System.out.println("lotNo..**..*>"+lotNo);
			}
		} // try
		catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
			// Added by sarita to throw Exception [START]
			throw new ITMException(e);
			// Added by sarita to throw Exception [END]
		}

		if (type.trim().equals("L")) {
			rate = 0;
			System.out.println("Inside type ::-<L>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					// System.out.println("The rate for type (L) is.... "+rate);
					rs.close(); // 22/06/09 manoharan
					pstmt.close(); // 22/06/09 manoharan
					rs = null; // 22/06/09 manoharan
					pstmt = null; // 22/06/09 manoharan
				} else {

					try {// try 2
						rs.close();
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1) == null ? "" : rs.getString(1);
							System.out.println("The priceListParent is .... " + priceListParent);
							// comment by manazir on 10/05/09
							// Uncommented by manoj dtd 01/03/2013 to return if
							// no Pricelistparent found
							if ((priceListParent == null) || priceListParent.trim().length() == 0) {
								priceListParent = "";
								System.out.println("The priceListParent if null  .... " + priceListParent);
								System.out.println("Return -1 if  priceListParent is found null .... ");
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
								return -1;
							}
							if (priceListParent.trim().length() > 0) {
								try {// try 3
									sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent
											+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'L' "
											+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setTimestamp(1, tranDate);
									pstmt2.setTimestamp(2, tranDate);
									rs2 = pstmt2.executeQuery();
									// System.out.println("The priceListParent sql .. "+sql);
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										// System.out.println("The rate inside priceListParent is ..."+rate);
										rs2.close(); // 22/06/09 manoharan
										pstmt2.close(); // 22/06/09 manoharan
										rs2 = null; // 22/06/09 manoharan
										pstmt2 = null; // 22/06/09 manoharan
									} else {
										rs2.close(); // 22/06/09 manoharan
										pstmt2.close(); // 22/06/09 manoharan
										rs2 = null; // 22/06/09 manoharan
										pstmt2 = null; // 22/06/09 manoharan
										return -1;
									}

									if (rate > 0) {
										priceList = priceListParent;
										// break ;
									} else {
										priceList = priceListParent;
										priceListParent = "";
									}

								} // try 3
								catch (Exception e) {
									// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
									e.printStackTrace();
									throw new ITMException(e);
								}
							} // if
							// end of code
						}
						// 22/06/09 manoharan
						if (rs != null)// && !rs.isClosed()) // isclosed is
							// giving problem
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null) // && !pstmt.isClosed())
						{
							pstmt.close();
							pstmt = null;
						}
						// end 22/06/09 manoharan
						/*
						 * if((priceListParent == null) || (priceListParent.trim().length() == 0)) {
						 * priceListParent = ""; System.out.println("The priceListParent if null  .... "
						 * +priceListParent); return -1; }
						 */
						/*
						 * if(priceListParent.trim().length() > 0) { try {//try 3 sql =
						 * "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"+priceListParent+"' " +
						 * "AND ITEM_CODE = '"+itemCode+"' " + "AND LIST_TYPE = 'L' " +
						 * "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?"; pstmt =
						 * conn.prepareStatement(sql); pstmt.setTimestamp(1,tranDate);
						 * pstmt.setTimestamp(2,tranDate); rs2 = pstmt.executeQuery();
						 * System.out.println("The priceListParent sql .. " +sql); if(rs2.next()) { rate
						 * = rs2.getDouble(1); System
						 * .out.println("The rate inside priceListParent is ..." +rate); } else { return
						 * -1; }
						 * 
						 * }//try 3 catch(Exception e) { System.out.println("Exception...[pickRate] "
						 * +sql+e.getMessage()); e.printStackTrace(); throw new ITMException(e); } }//if
						 */
					} // try 2
					catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				} // else
				// 22/06/09 manoharan
				if (rs != null) // && !rs.isClosed())
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) // && !pstmt.isClosed())
				{
					pstmt.close();
					pstmt = null;
				}
				// end 22/06/09 manoharan
				// pstmt.clearParameters();
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("L"))
		if (type.trim().equals("F")) {
			System.out.println("Inside type ::-<F>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'F' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					// System.out.println("The rate for type (F) is.... "+rate);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							// System.out.println("The priceListParent is .... "+priceListParent);
						}
						rs.close();
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							// System.out.println("The priceListParent if null .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'F' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								// System.out.println("The priceListParent sql .. "+sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									// System.out.println("The rate inside priceListParent is ..."+rate);
									rs2.close(); // 22/06/09 manoharan
									pstmt.close(); // 22/06/09 manoharan
									rs2 = null; // 22/06/09 manoharan
									pstmt = null; // 22/06/09 manoharan
								} else {
									rs2.close(); // 22/06/09 manoharan
									pstmt.close(); // 22/06/09 manoharan
									rs2 = null; // 22/06/09 manoharan
									pstmt = null; // 22/06/09 manoharan
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
				// 22/06/09 manoharan
				if (rs != null) // && !rs.isClosed())
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) // && !pstmt.isClosed())
				{
					pstmt.close();
					pstmt = null;
				}
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("F"))

		if (type.trim().equals("D")) // DISPATCH
		{
			rate = 0;
			// System.out.println("Inside type ::-<D>-::");
			// First selecting rate from pricelist for L,if not found
			// picking up from batch
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					rate = rs.getDouble(1);
					// System.out.println("The rate for type (L) is.... "+rate);
					rs.close(); // 22/06/09 manoharan
					pstmt.close(); // 22/06/09 manoharan
					rs = null; // 22/06/09 manoharan
					pstmt = null; // 22/06/09 manoharan
				} else {// else if 1

					try {// try 2
						rs.close(); // 22/06/09 manoharan
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							System.out.println("The priceListParent is .... " + priceListParent);
						}
						rs.close(); // 22/06/09 manoharan
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							// System.out.println("The priceListParent if null .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0)// 1
						{
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'L' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									// System.out.println("The rate inside priceListParent is ..."+rate);
									rs2.close(); // 22/06/09 manoharan
									pstmt.close(); // 22/06/09 manoharan
									rs2 = null; // 22/06/09 manoharan
									pstmt = null; // 22/06/09 manoharan
								} else {// else if 2

									try {// try 4
										rs2.close();
										pstmt.close(); // 22/06/09 manoharan
										rs2 = null; // 22/06/09 manoharan
										pstmt = null; // 22/06/09 manoharan
										sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' "
												+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo
												+ "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											rs2.close();
											pstmt.close(); // 22/06/09 manoharan
											rs2 = null; // 22/06/09 manoharan
											pstmt = null; // 22/06/09 manoharan
											// System.out.println("The rate inside priceListParent for <D> is
											// ..."+rate);
										} else {// else if 3
											// rs2.close();
											try {// try 5
												rs2.close();
												pstmt.close(); // 22/06/09
												// manoharan
												rs2 = null; // 22/06/09
												// manoharan
												pstmt = null; // 22/06/09
												// manoharan
												sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
														+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
												// + "AND LIST_TYPE = 'B'";
												pstmt = conn.prepareStatement(sql);
												rs2 = pstmt.executeQuery();
												if (rs2.next()) {
													priceListParent = rs2.getString(1);
													// System.out.println("The priceListParent for <D> is ....
													// "+priceListParent);
												}
												rs2.close();
												pstmt.close(); // 22/06/09
												// manoharan
												rs2 = null; // 22/06/09
												// manoharan
												pstmt = null; // 22/06/09
												// manoharan
												if ((priceListParent == null)
														|| (priceListParent.trim().length() == 0)) {
													priceListParent = "";
													// System.out.println("The priceListParent if null ....
													// "+priceListParent);
													return -1;
												}
												if (priceListParent.trim().length() > 0)// 2
												{
													try {// try 6
														sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
																+ priceListParent + "' " + "AND ITEM_CODE = '"
																+ itemCode + "' " + "AND LIST_TYPE = 'B' "
																+ "AND LOT_NO__FROM <= '" + lotNo + "' "
																+ "AND LOT_NO__TO >= '" + lotNo + "' "
																+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setTimestamp(1, tranDate);
														pstmt.setTimestamp(2, tranDate);
														rs3 = pstmt.executeQuery();
														// System.out.println("The priceListParent sql .. "+sql);
														if (rs3.next()) {
															rate = rs3.getDouble(1);
															// System.out.println("The rate inside priceListParent for
															// <D> is ..."+rate);
															rs3.close(); // 22/06/09
															// manoharan
															pstmt.close(); // 22/06/09
															// manoharan
															rs3 = null; // 22/06/09
															// manoharan
															pstmt = null; // 22/06/09
															// manoharan
														} else {
															rs3.close(); // 22/06/09
															// manoharan
															pstmt.close(); // 22/06/09
															// manoharan
															rs3 = null; // 22/06/09
															// manoharan
															pstmt = null; // 22/06/09
															// manoharan
															return -1;
														}
													} // try 6
													catch (Exception e) {
														// System.out.println("Exception...[pickRate]
														// "+sql+e.getMessage());
														e.printStackTrace();
														throw new ITMException(e);
													}
												} // if(priceListParent.trim().length()
												// > 0)2
											} // try 5
											catch (Exception e) {
												// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
												e.printStackTrace();
												throw new ITMException(e);
											}
										} // else if 3
										// rs2.close();
									} // try 4
									catch (Exception e) {
										// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // else if 2
								// rs2.close();
							} // try 3
							catch (Exception e) {
								// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						} // if(priceListParent.trim().length() > 0)//1

					} // try 2
					catch (Exception e) {
						// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				} // else if 1
				// 25-dec-15 Commented by saurabh
				if (rs != null) // && !rs.isClosed()) isClosed() is giving
					// problem
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) // && !pstmt.isClosed()) isClosed() is giving
					// problem
				{
					pstmt.clearParameters();
					pstmt.close();
					pstmt = null;
				}
			} // try 1
			catch (Exception e) {
				// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("D"))

		if (type.trim().equals("B"))// BATCH PRICE
		{
			rate = 0;
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'B' " + "AND LOT_NO__FROM <= '" + lotNo + "' "
						+ "AND LOT_NO__TO >= '" + lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					rate = rs.getDouble(1);
					// System.out.println("The rate inside priceListParent for <B> is ..."+rate);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {
					try { // try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'B'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							// System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG"+priceListParent);
							if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
								priceListParent = "";
								// return -1;
							}
							if (priceListParent.trim().length() > 0) {
								try {// try 3
									sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent
											+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' "
											+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo
											+ "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setTimestamp(1, tranDate);
									pstmt.setTimestamp(2, tranDate);
									rs2 = pstmt.executeQuery();
									// System.out.println("The priceListParent sql .. "+sql);
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										rs2.close();
										rs2 = null;
										pstmt.close();
										pstmt = null;
										// System.out.println("The rate inside priceListParent for <B> is ..."+rate);
									} else {
										// rate = rs2.getDouble(1);
										rs2.close();
										rs2 = null;
										pstmt.close();
										pstmt = null;
										return -1;
									}
									if (rate > 0) {
										priceList = priceListParent;
										// break ;
									} else {
										priceList = priceListParent;
										priceListParent = "";
									}
								} // try 3
								catch (Exception e) {
									// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
									e.printStackTrace();
									throw new ITMException(e);
								}
							} // if
						} // end of loop
						// Added by sarita on 13NOV2017
						else {
							if (rs != null) {
								rs.close();
								rs = null;
							}
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
						}

						// rs.close();
					} // try 2
					catch (Exception e) {
						// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
					// Added by sarita on 13-11-2017 [start]
					finally {
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
					}
					// Added by sarita on 13-11-2017 [end]
				} // else
				// rs.close();
			} // try 1
			catch (Exception e) {
				// System.out.println("Exception...[pickRate] "+sql+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("B"))//BATCH PRICE

		type = listType;
		if ((type.trim().equals("M")) || (type.trim().equals("N"))) // Discount
			// PRICE
		{
			rate = 0;
			System.out.println("Inside type ::-<M><N>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = '" + type + "' " + "AND EFF_FROM <= ? "
						+ "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					// System.out.println("The rate for type (F) is.... "+rate);
					rs.close(); // 22/06/09 manoharan
					pstmt.close(); // 22/06/09 manoharan
					rs = null; // 22/06/09 manoharan
					pstmt = null; // 22/06/09 manoharan
				} else {
					try {// try 2
						rs.close(); // 22/06/09 manoharan
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = '"+type.trim()+"'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							// System.out.println("The priceListParent is .... "+priceListParent);
						}
						rs.close(); // 22/06/09 manoharan
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							// System.out.println("The priceListParent if null .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							type = getPriceListType(priceList, conn);
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = '" + type + "' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									// System.out.println("The rate inside priceListParent is ..."+rate);
									rs2.close(); // 22/06/09 manoharan
									pstmt.close(); // 22/06/09 manoharan
									rs2 = null; // 22/06/09 manoharan
									pstmt = null; // 22/06/09 manoharan
								} else {
									rs2.close(); // 22/06/09 manoharan
									pstmt.close(); // 22/06/09 manoharan
									rs2 = null; // 22/06/09 manoharan
									pstmt = null; // 22/06/09 manoharan
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

		if (type.trim().equals("I")) // Inventory
		{
			rate = 0;
			if ((lotSl == null) || (lotSl.trim().length() == 0)) {
				System.out.println("Inside type ::-<I>-::");
				try {// try 1
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						System.out.println("Rate is .*...." + rate);
					}
					rs.close(); // 22/06/09 manoharan
					pstmt.close(); // 22/06/09 manoharan
					rs = null; // 22/06/09 manoharan
					pstmt = null; // 22/06/09 manoharan
				} // try
				catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // if
			else {
				try {// try 1
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "' "
							+ "AND LOT_SL = '" + lotSl + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						// System.out.println("Rate is .*...."+rate);
					}
					rs.close(); // 22/06/09 manoharan
					pstmt.close(); // 22/06/09 manoharan
					rs = null; // 22/06/09 manoharan
					pstmt = null; // 22/06/09 manoharan
				} // try
				catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
				// Add by Ajay Jadhav on 27/04/2018:START
				finally {
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
				// Add by Ajay Jadhav on 27/04/2018:END

			} // else
		} // if(type.trim().equals("I")) //Inventory
		// System.out.println("Rate From DisCommon***:::::["+rate+"]");
		return (rate);
	}// pickRate()

	/**
	 * Gets tokenised string
	 * 
	 * @param tokenString
	 * @param delimiter
	 * @return
	 * @throws ITMException
	 */
	public String getTokenList(String tokenString, String delimiter) throws ITMException {
		String returnList = "";
		if (tokenString != null && tokenString.length() > 0 && tokenString != "") {
			if (tokenString.indexOf(delimiter) != -1) {
				int endIndex = tokenString.indexOf(delimiter);
				returnList = delimiter;
				tokenString = tokenString.substring(endIndex + 1, tokenString.length());
			} else
				returnList = tokenString;
		} else {
			tokenString = "";
			returnList = tokenString;
		}
		return returnList;
	}// getTokenList()

	/**
	 * Gets the rate as double
	 * 
	 * @param priceList
	 *            The price list for which type is to be identified
	 * @param trDate
	 *            The date on which the price list to be a valid
	 * @param itemCode
	 *            The item code for which the rate is to be obtained
	 * @param aLotNo
	 *            The batch/lot number
	 * @param listType
	 *            The type of the price list
	 * @param quantity
	 *            The quantity for which quantity slab to be applied
	 * @param aunit
	 *            The unit of measure for which the rate is spscified
	 * @param conn
	 *            JDBC Database connection
	 * @return the rate as double
	 * @exception ITMException
	 * @see
	 */
	public double pickRate(String priceList, String trDate, String itemCode, String aLotNo, String listType,
			double quantity, String aunit, Connection conn) throws ITMException// Mthod3
	{
		// System.out.println("--------------[pickRate]---------------------------------");
		System.out.println("DP[priceList/trDate/itemCode/tranType/aLotNo/listType/quantity/unit/conn]");
		if (priceList != null)
			System.out.println("Price List:-[" + priceList + "]");
		if (trDate != null)
			System.out.println("Tran Date:-[" + trDate + "]");
		if (itemCode != null)
			System.out.println("Item Code:-[" + itemCode + "]");
		if (aLotNo != null)
			System.out.println("Lot No:-[" + aLotNo + "]");
		if (listType != null)
			System.out.println("List Type:-[" + listType + "]");
		System.out.println("Quantity:-[" + quantity + "]");
		if (aunit != null)
			System.out.println("Unit:-[" + aunit + "]");
		if (conn != null)
			System.out.println("Connection Found[" + conn + "]");
		System.out.println("-----------------------------------------------");
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

		type = getPriceListType(priceList, conn);
		try {
			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");
			System.out.println("Date in pickRate...." + tranDate);
			if (aLotNo.indexOf("~t") > 0) {

				String sepStr[] = aLotNo.split("~t");

				System.out.println(sepStr[0]);
				System.out.println(sepStr[1]);
				System.out.println(sepStr[2]);
				System.out.println(sepStr[3]);

				siteCode = sepStr[0];
				locCode = sepStr[1];
				lotNo = sepStr[2];
				lotSl = sepStr[3];

				/*
				 * siteCode = getTokenList(aLotNo,"~t");S locCode = getTokenList(aLotNo,"~t");
				 * lotNo = getTokenList(aLotNo,"~t"); lotSl = getTokenList(aLotNo,"~t");
				 */
				System.out.println("Separated String :---------------------->");

				System.out.println("siteCode..**..*>" + siteCode);
				System.out.println("locCode..**..*>" + locCode);
				System.out.println("lotNo..**..*>" + lotNo);
				System.out.println("lotSl..**..*>" + lotSl);
			} else {
				lotNo = aLotNo;
				System.out.println("lotNo..**..*>" + lotNo);
			}
		} // try
		catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		if (aunit == null) {
			aunit = "";
		}
		try {
			sql = "SELECT COUNT(*) AS COUNT FROM UOM WHERE UNIT = '" + aunit + "'";
			// stmt = conn.createStatement();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			// rs = stmt.executeQuery(sql);
			if (rs.next()) {
				cnt = rs.getInt("COUNT");
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			rs = null;
			if (cnt == 0) {
				return -1;
			}
		} catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		if (type.trim().equals("L")) // LIST typePRICE
		{
			rate = 0;
			System.out.println("Inside type ::-<L>-::");
			try {// try 1
				sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND UNIT = '" + aunit + "' " + "AND LIST_TYPE = 'L' "
						+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
						+ "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					unit = rs.getString(2);
					System.out.println("The rate for type (L) is.... " + rate);
					System.out.println("The unit for type (L) is.... " + unit);
					rs.close(); // 22/06/09 manoharan
					pstmt.close(); // 22/06/09 manoharan
					rs = null; // 22/06/09 manoharan
					pstmt = null; // 22/06/09 manoharan
				} else {
					try {// try 2
						rs.close(); // 22/06/09 manoharan
						pstmt.close(); // 22/06/09 manoharan
						rs = null; // 22/06/09 manoharan
						pstmt = null; // 22/06/09 manoharan
						sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
								+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= "
								+ quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
								+ "AND VALID_UPTO >= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, tranDate);
						pstmt.setTimestamp(2, tranDate);
						System.out.println("Rate, Unit sql ..." + sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							rate = rs.getDouble(1);
							unit = rs.getString(2);
							System.out.println("The rate for type (L) try 2 is.... " + rate);
							System.out.println("The unit for type (L) try 2 is.... " + unit);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						} else {
							try {// try 3
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
								// + "AND LIST_TYPE = 'L'";
								pstmt = conn.prepareStatement(sql);
								System.out.println("SQL::" + sql);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceListParent = rs.getString(1);
									System.out.println("The priceListParent is .... " + priceListParent);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
									priceListParent = "";
									System.out.println("The priceListParent if null  .... " + priceListParent);
									return -1;
								}
								if (priceListParent.trim().length() > 0) {
									try {// try 4
										sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
												+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND UNIT = '" + aunit + "' " + "AND MIN_QTY <= " + quantity + " "
												// +
												// "AND MIN_QTY >= "+quantity+" "
												// //Commented By Mahesh Patidar
												// on 05/07/12 as per
												// instruction S. Manoharan
												+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar
												// on 05/07/12 as per
												// instruction S.
												// Manoharan
												+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										System.out.println("The priceListParent sql .. " + sql);
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											unit = rs2.getString(2);
											System.out.println("The rate inside priceListParent is ..." + rate);
											rs2.close();
											pstmt.close();
											pstmt = null;
											rs2 = null;
										} else {
											try {// try 5
												rs2.close();
												pstmt.close();
												pstmt = null;
												rs2 = null;
												sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
														+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
														+ "AND MIN_QTY <= " + quantity + " "
														// +
														// "AND MIN_QTY >= "+quantity+" "
														// //Commented By Mahesh
														// Patidar on 05/07/12
														// as per instruction S.
														// Manoharan
														+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh
														// patidar on
														// 05/07/12 as
														// per
														// instruction
														// S. Manoharan
														+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? "
														+ "AND VALID_UPTO >= ?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setTimestamp(1, tranDate);
												pstmt.setTimestamp(2, tranDate);
												rs2 = pstmt.executeQuery();
												System.out.println("The priceListParent sql .. " + sql);
												if (rs2.next()) {
													rate = rs2.getDouble(1);
													unit = rs2.getString(2);
													System.out.println("The rate inside priceListParent is ..." + rate);
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
											} // try 5
											catch (Exception e) {
												System.out.println("Exception...[pickRate] " + sql + e.getMessage());
												e.printStackTrace();
												throw new ITMException(e);
											}
										} // else if
										// rs2.close();
										// pstmt.close();
										// pstmt = null;
										// rs2=null;
									} // try 4
									catch (Exception e) {
										System.out.println("Exception...[pickRate] " + sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // if
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
		} // if(type.trim().equals("L"))

		if (type.trim().equals("F")) // FIXED PRICE ON DATE
		{
			rate = 0;
			System.out.println("Inside type ::-<F>-::");
			try {// try 1
				sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND UNIT = '" + aunit + "' " + "AND LIST_TYPE = 'F' "
						+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
						+ "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					unit = rs.getString(2);
					System.out.println("The rate for type (L) is.... " + rate);
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
						sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
								+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'F' " + "AND MIN_QTY <= "
								+ quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
								+ "AND VALID_UPTO >= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, tranDate);
						pstmt.setTimestamp(2, tranDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							rate = rs.getDouble(1);
							unit = rs.getString(2);
							System.out.println("The rate for type (F) is.... " + rate);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						} else {

							try {// try 3
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
								// + "AND LIST_TYPE = 'L'";
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
									try {// try 4
										sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
												+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND UNIT = '" + aunit + "' " + "AND MIN_QTY <= " + quantity + " "
												// +
												// "AND MIN_QTY >= "+quantity+" "
												// //Commented By Mahesh Patidar
												// on 05/07/12 as per
												// instruction S. Manoharan
												+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar
												// on 05/07/12 as per
												// instruction S.
												// Manoharan
												+ "AND LIST_TYPE = 'F' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										System.out.println("The priceListParent sql .. " + sql);
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											unit = rs2.getString(2);
											System.out.println("The rate inside priceListParent is ..." + rate);
											rs2.close();
											pstmt.close();
											pstmt = null;
											rs2 = null;
										} else {
											try {// try 5
												rs2.close();
												pstmt.close();
												pstmt = null;
												rs2 = null;
												sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
														+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
														+ "AND MIN_QTY <= " + quantity + " "
														// +
														// "AND MIN_QTY >= "+quantity+" "
														// //Commented By Mahesh
														// Patidar on 05/07/12
														// as per instruction S.
														// Manoharan
														+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh
														// patidar on
														// 05/07/12 as
														// per
														// instruction
														// S. Manoharan
														+ "AND LIST_TYPE = 'F' " + "AND EFF_FROM <= ? "
														+ "AND VALID_UPTO >= ?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setTimestamp(1, tranDate);
												pstmt.setTimestamp(2, tranDate);
												rs2 = pstmt.executeQuery();
												System.out.println("The priceListParent sql .. " + sql);
												if (rs2.next()) {
													rate = rs2.getDouble(1);
													unit = rs2.getString(2);
													System.out.println("The rate inside priceListParent is ..." + rate);
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
											} // try 5
											catch (Exception e) {
												System.out.println("Exception...[pickRate] " + sql + e.getMessage());
												e.printStackTrace();
											}
										} // else if
										// rs2.close();
									} // try 4
									catch (Exception e) {
										System.out.println("Exception...[pickRate] " + sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // if
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
				// rs=null;
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("F"))

		if (type.trim().equals("D")) // DESPATCH
		{
			// selecting rate from pricelist for L, if not found
			// picking up from batch
			rate = 0;
			try {// try 1
				sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND UNIT = '" + aunit + "' " + "AND LIST_TYPE = 'L' "
						+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
						+ "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					unit = rs.getString(2);
					System.out.println("The rate for type (L) is.... " + rate);
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
						sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
								+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= "
								+ quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? "
								+ "AND VALID_UPTO >= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, tranDate);
						pstmt.setTimestamp(2, tranDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							rate = rs.getDouble(1);
							unit = rs.getString(2);
							System.out.println("The rate for type (L) is.... " + rate);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						} else {
							try {// try 3
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
								// + "AND LIST_TYPE = 'L'";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceListParent = rs.getString(1);
									System.out.println("The priceListParent is .... " + priceListParent);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
									priceListParent = "";
									System.out.println("The priceListParent if null  .... " + priceListParent);
									return -1;
								}
								if (priceListParent.trim().length() > 0) {
									try {// try 4
										sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
												+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND UNIT = '" + aunit + "' " + "AND MIN_QTY <= " + quantity + " "
												// +
												// "AND MIN_QTY >= "+quantity+" "
												// //Commented By Mahesh Patidar
												// on 05/07/12 as per
												// instruction S. Manoharan
												+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar
												// on 05/07/12 as per
												// instruction S.
												// Manoharan
												+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										System.out.println("The priceListParent sql .. " + sql);
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											unit = rs2.getString(2);
											System.out.println("The rate inside priceListParent is ..." + rate);
											rs2.close();
											pstmt.close();
											pstmt = null;
											rs2 = null;
										} else {
											try {// try 5
												rs2.close();
												pstmt.close();
												pstmt = null;
												rs2 = null;
												sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
														+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
														+ "AND MIN_QTY <= " + quantity + " "
														// +
														// "AND MIN_QTY >= "+quantity+" "
														// //Commented By Mahesh
														// Patidar on 05/07/12
														// as per instruction S.
														// Manoharan
														+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh
														// patidar on
														// 05/07/12 as
														// per
														// instruction
														// S. Manoharan
														+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? "
														+ "AND VALID_UPTO >= ?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setTimestamp(1, tranDate);
												pstmt.setTimestamp(2, tranDate);
												rs2 = pstmt.executeQuery();
												System.out.println("The priceListParent sql .. " + sql);
												if (rs2.next()) {
													rate = rs2.getDouble(1);
													unit = rs2.getString(2);
													// System.out.println("The rate inside priceListParent is
													// ..."+rate);
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
											} // try 5
											catch (Exception e) {
												System.out.println("Exception...[pickRate] " + sql + e.getMessage());
												e.printStackTrace();
												throw new ITMException(e);
											}
										} // else if
										// rs2.close();
									} // try 4
									catch (Exception e) {
										System.out.println("Exception...[pickRate] " + sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // if
								else {
									rate = 0;
									try {// try 6
										sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList
												+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND UNIT = '" + aunit
												+ "' " + "AND MIN_QTY <= " + quantity + " "
												// +
												// "AND MIN_QTY >= "+quantity+" "
												// //Commented By Mahesh Patidar
												// on 05/07/12 as per
												// instruction S. Manoharan
												+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar
												// on 05/07/12 as per
												// instruction S.
												// Manoharan
												+ "AND LIST_TYPE = 'B' " + "AND LOT_NO__FROM <= '" + lotNo + "' "
												+ "AND LOT_NO__TO >= '" + lotNo + "' " + "AND EFF_FROM <= ? "
												+ "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs3 = pstmt.executeQuery();
										System.out.println("Sql .. " + sql);
										if (rs3.next()) {
											rate = rs3.getDouble(1);
											unit = rs3.getString(2);
											System.out.println("The rate inside priceListParent is ..." + rate);
											rs3.close();
											pstmt.close();
											pstmt = null;
											rs3 = null;
										} else {
											try {// try 7
												rs3.close();
												pstmt.close();
												pstmt = null;
												rs3 = null;
												sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
														+ priceList + "' " + "AND ITEM_CODE = '" + itemCode + "' "
														+ "AND MIN_QTY <= " + quantity + " "
														// +
														// "AND MIN_QTY >= "+quantity+" "
														// //Commented By Mahesh
														// Patidar on 05/07/12
														// as per instruction S.
														// Manoharan
														+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh
														// patidar on
														// 05/07/12 as
														// per
														// instruction
														// S. Manoharan
														+ "AND LIST_TYPE = 'B' " + "AND LOT_NO__FROM <= '" + lotNo
														+ "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
														+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setTimestamp(1, tranDate);
												pstmt.setTimestamp(2, tranDate);
												rs3 = pstmt.executeQuery();
												// System.out.println("Sql .. "+sql);
												if (rs3.next()) {
													rate = rs3.getDouble(1);
													unit = rs3.getString(2);
													System.out.println("The rate inside priceListParent is ..." + rate);
													rs3.close();
													pstmt.close();
													pstmt = null;
													rs3 = null;
												} else {
													try {// try 8
														rs3.close();
														pstmt.close();
														pstmt = null;
														rs3 = null;
														sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
																+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList
																+ "' ";
														// +
														// "AND LIST_TYPE = 'B'";
														pstmt = conn.prepareStatement(sql);
														rs3 = pstmt.executeQuery();
														if (rs3.next()) {
															priceListParent = rs3.getString(1);
															System.out.println(
																	"The priceListParent is .... " + priceListParent);
														}
														rs3.close();
														pstmt.close();
														pstmt = null;
														rs3 = null;
														if ((priceListParent == null)
																|| (priceListParent.trim().length() == 0)) {
															priceListParent = "";
															System.out.println("The priceListParent if null  .... "
																	+ priceListParent);
															return -1;
														}
														if (priceListParent.trim().length() > 0) {
															try {// try 9
																sql = "SELECT RATE,UNIT FROM PRICELIST "
																		+ "WHERE PRICE_LIST = '" + priceListParent
																		+ "' " + "AND ITEM_CODE = '" + itemCode + "' "
																		+ "AND UNIT = '" + aunit + "' "
																		+ "AND MIN_QTY <= " + quantity + " "
																		// +
																		// "AND MIN_QTY >= "+quantity+" "
																		// //Commented
																		// By
																		// Mahesh
																		// Patidar
																		// on
																		// 05/07/12
																		// as
																		// per
																		// instruction
																		// S.
																		// Manoharan
																		+ "AND MAX_QTY >= " + quantity + " " // Added
																		// By
																		// Mahesh
																		// patidar
																		// on
																		// 05/07/12
																		// as
																		// per
																		// instruction
																		// S.
																		// Manoharan
																		+ "AND LIST_TYPE = 'B' "
																		+ "AND LOT_NO__FROM <= '" + lotNo + "' "
																		+ "AND LOT_NO__TO >= '" + lotNo + "' "
																		+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
																pstmt = conn.prepareStatement(sql);
																pstmt.setTimestamp(1, tranDate);
																pstmt.setTimestamp(2, tranDate);
																rs4 = pstmt.executeQuery();
																System.out.println("Sql .. " + sql);
																if (rs4.next()) {
																	rate = rs4.getDouble(1);
																	unit = rs4.getString(2);
																	System.out.println(
																			"The rate inside priceListParent is ..."
																					+ rate);
																	rs4.close();
																	pstmt.close();
																	pstmt = null;
																	rs4 = null;
																} else {
																	rs4.close();
																	pstmt.close();
																	pstmt = null;
																	rs4 = null;
																	try {// try
																		// 10
																		sql = "SELECT RATE,UNIT FROM PRICELIST "
																				+ "WHERE PRICE_LIST = '"
																				+ priceListParent + "' "
																				+ "AND ITEM_CODE = '" + itemCode + "' "
																				+ "AND MIN_QTY <= " + quantity + " "
																				// +
																				// "AND MIN_QTY >= "+quantity+" "
																				// //Commented
																				// By
																				// Mahesh
																				// Patidar
																				// on
																				// 05/07/12
																				// as
																				// per
																				// instruction
																				// S.
																				// Manoharan
																				+ "AND MAX_QTY >= " + quantity + " " // Added
																				// By
																				// Mahesh
																				// patidar
																				// on
																				// 05/07/12
																				// as
																				// per
																				// instruction
																				// S.
																				// Manoharan
																				+ "AND LIST_TYPE = 'B' "
																				+ "AND LOT_NO__FROM <= '" + lotNo + "' "
																				+ "AND LOT_NO__TO >= '" + lotNo + "' "
																				+ "AND EFF_FROM <= ? "
																				+ "AND VALID_UPTO >= ? ";
																		pstmt = conn.prepareStatement(sql);
																		pstmt.setTimestamp(1, tranDate);
																		pstmt.setTimestamp(2, tranDate);
																		rs4 = pstmt.executeQuery();
																		System.out.println("Sql .. " + sql);
																		if (rs4.next()) {
																			rate = rs4.getDouble(1);
																			unit = rs4.getString(2);
																			System.out.println(
																					"The rate inside priceListParent is ..."
																							+ rate);
																			rs4.close();
																			pstmt.close();
																			pstmt = null;
																			rs4 = null;
																		} else {
																			rs4.close();
																			pstmt.close();
																			pstmt = null;
																			rs4 = null;
																			return -1;
																		}
																	} // try 10
																	catch (Exception e) {
																		System.out.println("Exception...[pickRate] "
																				+ sql + e.getMessage());
																		e.printStackTrace();
																		throw new ITMException(e);
																	}
																} // else if
																// rs4.close();
															} // try 9
															catch (Exception e) {
																System.out.println("Exception...[pickRate] " + sql
																		+ e.getMessage());
																e.printStackTrace();
																throw new ITMException(e);
															}
														} // if
													} // try 8
													catch (Exception e) {
														System.out.println(
																"Exception...[pickRate] " + sql + e.getMessage());
														e.printStackTrace();
														throw new ITMException(e);
													}
												} // else if
												// rs3.close();
											} // try 7
											catch (Exception e) {
												System.out.println("Exception...[pickRate] " + sql + e.getMessage());
												e.printStackTrace();
												throw new ITMException(e);
											}
										} // else if
										// rs3.close();
									} // try 6
									catch (Exception e) {
										System.out.println("Exception...[pickRate] " + sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // else
							} // try 3
							catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						} // else
						// rs.close();
						// rs=null;
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
				sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
						+ "AND ITEM_CODE = '" + itemCode + "' " + "AND UNIT = '" + aunit + "' " + "AND LIST_TYPE = 'B' "
						+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
						+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
						+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					unit = rs.getString(2);
					System.out.println("The rate for type (B) is.... " + rate);
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
						sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
								+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' " + "AND MIN_QTY <= "
								+ quantity + " " + "AND MAX_QTY >= " + quantity + " " + "AND LOT_NO__FROM <= '" + lotNo
								+ "' " + "AND LOT_NO__TO >= '" + lotNo + "' " + "AND EFF_FROM <= ? "
								+ "AND VALID_UPTO >= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, tranDate);
						pstmt.setTimestamp(2, tranDate);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							rate = rs.getDouble(1);
							unit = rs.getString(2);
							System.out.println("The rate for type (L) is.... " + rate);
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						} else {
							try {// try 3
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
								// + "AND LIST_TYPE = 'B'";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceListParent = rs.getString(1);
									System.out.println("The priceListParent is .... " + priceListParent);
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
								if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
									priceListParent = "";
									System.out.println("The priceListParent if null  .... " + priceListParent);
									return -1;
								}
								if (priceListParent.trim().length() > 0) {
									try {// try 4
										sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
												+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND UNIT = '" + aunit + "' " + "AND LIST_TYPE = 'B' "
												+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity
												+ " " + "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '"
												+ lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										System.out.println("The priceListParent sql .. " + sql);
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											unit = rs2.getString(2);
											System.out.println("The rate inside priceListParent is ..." + rate);
											rs2.close();
											pstmt.close();
											pstmt = null;
											rs2 = null;
										} else {
											try {// try 5
												rs2.close();
												pstmt.close();
												pstmt = null;
												rs2 = null;
												sql = "SELECT RATE,UNIT FROM PRICELIST " + "WHERE PRICE_LIST = '"
														+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
														+ "AND LIST_TYPE = 'B' " + "AND MIN_QTY <= " + quantity + " "
														+ "AND MAX_QTY >= " + quantity + " " + "AND LOT_NO__FROM <= '"
														+ lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
														+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setTimestamp(1, tranDate);
												pstmt.setTimestamp(2, tranDate);
												rs2 = pstmt.executeQuery();
												System.out.println("The priceListParent sql .. " + sql);
												if (rs2.next()) {
													rate = rs2.getDouble(1);
													unit = rs2.getString(2);
													System.out.println("The rate inside priceListParent is ..." + rate);
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
											} // try 5
											catch (Exception e) {
												System.out.println("Exception...[pickRate] " + sql + e.getMessage());
												e.printStackTrace();
												throw new ITMException(e);
											}
										} // else if
										// rs.close();
									} // try 4
									catch (Exception e) {
										System.out.println("Exception...[pickRate] " + sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								} // if
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
				// rs=null;
				// pstmt.clearParameters();
				// pstmt.close();
				// pstmt = null;
			} // try 1
			catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // if(type.trim().equals("B"))
		System.out.println("rate while calling convQtyFactor--[" + rate + "]");
		System.out.println("unit while calling convQtyFactor--[" + unit + "]");
		if (rate != 0) {
			System.out.println("if rate !=0 then aunit::[" + aunit + "]=unit[" + unit + "]");
			if (!aunit.trim().equals(unit.trim())) {
				System.out.println("[aunit:" + aunit + "\nintemCode:" + itemCode + "\nconv:" + conv + "]");
				// rate = convQtyFactor(aunit, unit, itemCode, conv, conn);
				rate = convQtyFactor(aunit, unit, itemCode, rate, conn);
				System.out.println("rate after convQtyFactor::[" + rate + "]");
			}
		}
		System.out.println("Rate From DisCommon***:::::[" + rate + "]");
		return (rate);
	}// pickRate()

	/**
	 * Gets the rate as double
	 * 
	 * @param priceList
	 *            The price list for which type is to be identified
	 * @param trDate
	 *            The date on which the price list to be a valid
	 * @param itemCode
	 *            The item code for which the rate is to be obtained
	 * @param aLotNo
	 *            The batch/lot number
	 * @param listType
	 *            The type of the price list
	 * @param quantity
	 *            The quantity for which quantity slab to be applied
	 * @param conn
	 *            JDBC Database connection
	 * @return The rate as double
	 * @exception ITMException
	 * @see
	 */
	public double pickRate(String priceList, String trDate, String itemCode, String aLotNo, String listType,
			double quantity, Connection conn) throws ITMException// Method 4
	{
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

		type = getPriceListType(priceList, conn);
		try {
			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");
			// System.out.println("Date in pickRate...."+tranDate);
			if (aLotNo.indexOf("~t") > 0) {
				// siteCode = getTokenList(aLotNo,"~t"); // commented by cpatil
				// locCode = getTokenList(aLotNo,"~t"); // commented by cpatil
				// lotNo = getTokenList(aLotNo,"~t"); // commented by cpatil
				// lotSl = getTokenList(aLotNo,"~t"); // commented by cpatil

				// /System.out.println("siteCode..**..*>"+siteCode);
				// System.out.println("locCode..**..*>"+locCode);
				// System.out.println("lotNo..**..*>"+lotNo);
				// System.out.println("lotSl..**..*>"+lotSl);

				// modify by cpatil start
				String MulStr[] = aLotNo.split("~t");
				siteCode = MulStr[0];
				locCode = MulStr[1];
				lotNo = MulStr[2];
				lotSl = MulStr[3];
				// end
			} else {
				lotNo = aLotNo;
				// System.out.println("lotNo..**..*>"+lotNo);
			}
		} // try
		catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("type----" + type);
		if (type.trim().equals("L")) // LIST PRICE
		{
			rate = 0;
			// System.out.println("Inside type ::-<L>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					System.out.println("Pricelist found in pricelist master");
					rate = rs.getDouble(1);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
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
							// System.out.println("The priceListParent if null .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND MIN_QTY <= " + quantity + " "
										// + "AND MIN_QTY >= "+quantity+" "
										// //Commented By Mahesh Patidar on
										// 05/07/12 as per instruction S.
										// Manoharan
										+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar on
										// 05/07/12 as per instruction
										// S. Manoharan
										+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								// System.out.println("The priceListParent sql .. "+sql);
								if (rs2.next()) {

									rate = rs2.getDouble(1);
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
									rate = pickRate(priceListParent, tempDate, itemCode, aLotNo, listType, quantity,
											conn);
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
		} // if(type.trim().equals("L"))

		if (type.trim().equals("F")) // FIXED PRICE ON DATE
		{
			rate = 0;
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'F' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
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
							// System.out.println("The priceListParent if null .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND MIN_QTY <= " + quantity + " "
										// + "AND MIN_QTY >= "+quantity+" "
										// //Commented By Mahesh Patidar on
										// 05/07/12 as per instruction S.
										// Manoharan
										+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar on
										// 05/07/12 as per instruction
										// S. Manoharan
										+ "AND LIST_TYPE = 'F' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
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
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND MIN_QTY <= " + quantity + " "
										// + "AND MIN_QTY >= "+quantity+" "
										// //Commented By Mahesh Patidar on
										// 05/07/12 as per instruction S.
										// Manoharan
										+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar on
										// 05/07/12 as per instruction
										// S. Manoharan
										+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
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
										sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' "
												+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity
												+ " " + "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '"
												+ lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										if (rs2.next()) {
											rate = rs2.getDouble(1);
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
														+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
												// + "AND LIST_TYPE = 'B'";
												pstmt = conn.prepareStatement(sql);
												rs2 = pstmt.executeQuery();
												if (rs2.next()) {
													priceListParent = rs2.getString(1);
													// System.out.println("The priceListParent is ....
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
														sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
																+ priceListParent + "' " + "AND ITEM_CODE = '"
																+ itemCode + "' " + "AND LIST_TYPE = 'B' "
																+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= "
																+ quantity + " " + "AND LOT_NO__FROM <= '" + lotNo
																+ "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
																+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setTimestamp(1, tranDate);
														pstmt.setTimestamp(2, tranDate);
														rs3 = pstmt.executeQuery();
														System.out.println("The priceListParent sql .. " + sql);
														if (rs3.next()) {
															rate = rs3.getDouble(1);
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'B' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND LOT_NO__FROM <= '" + lotNo + "' "
						+ "AND LOT_NO__TO >= '" + lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					rate = rs.getDouble(1);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'B'";
						pstmt = conn.prepareStatement(sql);
						rs2 = pstmt.executeQuery();
						if (rs2.next()) {
							priceListParent = rs2.getString(1);
							// System.out.println("The priceListParent is .... "+priceListParent);
						}
						rs2.close();
						rs2 = null;
						pstmt.close();
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							System.out.println("The priceListParent if null  .... " + priceListParent);
							return 0;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' "
										+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
										+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								// System.out.println("The priceListParent sql .. "+sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									rs2.close();
									rs2 = null;
									pstmt.close();
									pstmt = null;
								} else {
									rs2.close();
									rs2 = null;
									pstmt.close();
									pstmt = null;
									return 0;
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = '" + type + "' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {
					type = getPriceListType(priceList, conn);
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = '"+type+"'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							// System.out.println("The priceListParent is .... "+priceListParent);
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
							type = getPriceListType(priceList, conn);
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = '" + type + "' "
										+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									// System.out.println("The rate inside priceListParent is ..."+rate);
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

		if (type.trim().equals("I")) // Inventory
		{
			rate = 0;
			if ((lotSl == null) || (lotSl.trim().length() == 0)) {
				System.out.println("Inside type ::-<I>-::");
				try {// try 1
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						// System.out.println("Rate is .*...."+rate);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} // try
				catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // if
			else {
				try {// try 1
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "' "
							+ "AND LOT_SL = '" + lotSl + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						// System.out.println("Rate is .*...."+rate);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} // try
				catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // else
		} // if(type.trim().equals("I")) //Inventory
		// System.out.println("Rate From DisCommon***:::::["+rate+"]");
		return (rate);
	}// pickRate()

	/**
	 * Gets the rate as double consiring the GSM
	 * 
	 * @param priceList
	 * @param trDate
	 * @param itemCode
	 * @param aLotNo
	 * @param listType
	 * @param quantity
	 * @param conn
	 * @return
	 * @throws ITMException
	 */
	public double pickRateGSM(String priceList, String trDate, String itemCode, String aLotNo, String listType,
			double quantity, Connection conn) throws ITMException {
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
		try {
			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");
			// System.out.println("Date in pickRate...."+tranDate);
			if (aLotNo.indexOf("~t") > 0) {
				siteCode = getTokenList(aLotNo, "~t");
				locCode = getTokenList(aLotNo, "~t");
				lotNo = getTokenList(aLotNo, "~t");
				lotSl = getTokenList(aLotNo, "~t");
				// System.out.println("siteCode..**..*>"+siteCode);
				// System.out.println("locCode..**..*>"+locCode);
				// System.out.println("lotNo..**..*>"+lotNo);
				// System.out.println("lotSl..**..*>"+lotSl);
			} else {
				lotNo = aLotNo;
				// System.out.println("lotNo..**..*>"+lotNo);
			}
		} // try
		catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}

		type = getPriceListType(priceList, conn);

		if (type.trim().equals("L")) // LIST PRICE
		{
			rate = 0;
			// System.out.println("Inside type ::-<L>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
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
							// System.out.println("The priceListParent if null .... "+priceListParent);
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND MIN_QTY <= " + quantity + " "
										// + "AND MIN_QTY >= "+quantity+" "
										// //Commented By Mahesh Patidar on
										// 05/07/12 as per instruction S.
										// Manoharan
										+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar on
										// 05/07/12 as per instruction
										// S. Manoharan
										+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
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
				} // else if
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'F' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				else {
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
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
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND MIN_QTY <= " + quantity + " "
										// + "AND MIN_QTY >= "+quantity+" "
										// //Commented By Mahesh Patidar on
										// 05/07/12 as per instruction S.
										// Manoharan
										+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar on
										// 05/07/12 as per instruction
										// S. Manoharan
										+ "AND LIST_TYPE = 'F' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
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
				} // else if
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'L' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				// System.out.println("Rate sql ..."+sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				else {
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
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
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND MIN_QTY <= " + quantity + " "
										// + "AND MIN_QTY >= "+quantity+" "
										// //Commented By Mahesh Patidar on
										// 05/07/12 as per instruction S.
										// Manoharan
										+ "AND MAX_QTY >= " + quantity + " " // Added By Mahesh patidar on
										// 05/07/12 as per instruction
										// S. Manoharan
										+ "AND LIST_TYPE = 'L' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
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
										sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' "
												+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity
												+ " " + "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '"
												+ lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setTimestamp(1, tranDate);
										pstmt.setTimestamp(2, tranDate);
										rs2 = pstmt.executeQuery();
										if (rs2.next()) {
											rate = rs2.getDouble(1);
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
														+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
												// + "AND LIST_TYPE = 'B'";
												pstmt = conn.prepareStatement(sql);
												rs2 = pstmt.executeQuery();
												if (rs2.next()) {
													priceListParent = rs2.getString(1);
													// System.out.println("The priceListParent is ....
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
														rs2.close();
														rs2 = null;
														pstmt.close();
														pstmt = null;
														sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
																+ priceListParent + "' " + "AND ITEM_CODE = '"
																+ itemCode + "' " + "AND LIST_TYPE = 'B' "
																+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= "
																+ quantity + " " + "AND LOT_NO__FROM <= '" + lotNo
																+ "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
																+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setTimestamp(1, tranDate);
														pstmt.setTimestamp(2, tranDate);
														rs3 = pstmt.executeQuery();
														System.out.println("The priceListParent sql .. " + sql);
														if (rs3.next()) {
															rate = rs3.getDouble(1);
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = 'B' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND LOT_NO__FROM <= '" + lotNo + "' "
						+ "AND LOT_NO__TO >= '" + lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					rate = rs.getDouble(1);
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'B'";
						pstmt = conn.prepareStatement(sql);
						rs2 = pstmt.executeQuery();
						if (rs2.next()) {
							priceListParent = rs2.getString(1);
							// System.out.println("The priceListParent is .... "+priceListParent);
						}
						// Commented and Added by sarita on 13NOV2017
						/*
						 * rs.close(); rs = null;
						 */
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
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B' "
										+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
										+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo + "' "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
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
				} // else if
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
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = '" + type + "' " + "AND MIN_QTY <= " + quantity + " "
						+ "AND MAX_QTY >= " + quantity + " " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, tranDate);
				pstmt.setTimestamp(2, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {
					type = getPriceListType(priceList, conn);
					try {// try 2
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = '"+type+"'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							System.out.println("The priceListParent is .... " + priceListParent);
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
							type = getPriceListType(priceList, conn);
							try {// try 3
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = '" + type + "' "
										+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									rs2.close();
									rs2 = null;
									pstmt.close();
									pstmt = null;
									// System.out.println("The rate inside priceListParent is ..."+rate);
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

		if (type.trim().equals("I")) // Inventory
		{
			rate = 0;
			if ((lotSl == null) || (lotSl.trim().length() == 0)) {
				System.out.println("Inside type ::-<I>-::");
				try {// try 1
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						// System.out.println("Rate is .*...."+rate);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} // try
				catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // if
			else {
				try {// try 1
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "' "
							+ "AND LOT_SL = '" + lotSl + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						// System.out.println("Rate is .*...."+rate);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} // try
				catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // else
		} // if(type.trim().equals("I")) //Inventory
		return (rate);
	}// pickRateGSM()

	/**
	 * Gets the conversion factor as double
	 * 
	 * @param unitFrom
	 *            The unit of measure from which to convert
	 * @param unitTo
	 *            The unit of measure to which to convert
	 * @param itemCode
	 *            The item code for the conversion to be done
	 * @param toConvert
	 *            The value to be convert
	 * @param conn
	 *            JDBC Database connection
	 * @return the converted double value
	 * @exception ITMException
	 * @see
	 */
	public double convQtyFactor(String unitFrom, String unitTo, String itemCode, double toConvert, Connection conn)
			throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String round = "";
		double fact = 0.0;
		double roundTo = 0.0;
		double newQty = 0.0;
		double errQty = -999999999;
		int count = 0;

		if (unitFrom.trim().equals(unitTo.trim())) {
			return toConvert;
		}
		try {
			sql = "SELECT COUNT(*) AS COUNT FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
					+ "AND ITEM_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, unitFrom);
			pstmt.setString(2, unitTo);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt("COUNT");
				System.out.println("Count........1 " + count);
			}
			rs.close();
			pstmt.close();
			rs = null;
			pstmt = null;
		} // try
		catch (Exception e) {
			System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		if (count == 0) {
			try {
				sql = "SELECT COUNT(*) AS COUNT FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
						+ "AND ITEM_CODE = 'X'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, unitFrom);
				pstmt.setString(2, unitTo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt("COUNT");
					// System.out.println("Count........2 "+count);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
			} catch (Exception e) {
				System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
			if (count == 0) {
				return errQty;
			} else {
				try {
					sql = "SELECT FACT,ROUND,ROUND_TO FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
							+ "AND ITEM_CODE = 'X'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitFrom);
					pstmt.setString(2, unitTo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						fact = rs.getDouble(1);
						round = rs.getString(2);
						roundTo = rs.getDouble(3);
						System.out.println("fact........" + fact);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} catch (Exception e) {
					System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // else
		} // if
		else {
			try {
				sql = "SELECT FACT,ROUND,ROUND_TO FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
						+ "AND ITEM_CODE = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, unitFrom);
				pstmt.setString(2, unitTo);
				pstmt.setString(3, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					fact = rs.getDouble(1);
					round = rs.getString(2);
					roundTo = rs.getDouble(3);
					System.out.println("fact........" + fact);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} catch (Exception e) {
				System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // else
		newQty = (fact * toConvert);
		round = round.toUpperCase();
		if (round.trim().equals("X")) {
			newQty = (newQty - (newQty % roundTo) + roundTo);
		}
		if (round.trim().equals("P")) {
			newQty = (newQty - (newQty % roundTo));
		}
		if (round.trim().equals("R")) {
			if ((newQty % roundTo) < (roundTo / 2)) {
				newQty = (newQty - (newQty % roundTo));
			} else {
				newQty = (newQty - (newQty % roundTo) + roundTo);
			}
		}
		return newQty;
	}// convQtyFactor

	/**
	 * Gets the conversion factor and converted value as an ArrayList(0)
	 * conversion factor ArraList(1) converted value
	 * 
	 * @param unitFrom
	 *            The unit of measure from which to convert
	 * @param unitTo
	 *            The unit of measure to which to convert
	 * @param itemCode
	 *            The item code for the conversion to be done
	 * @param toConvert
	 *            The value to be convert
	 * @param fact
	 *            The conversion factor user inputed
	 * @param conn
	 *            JDBC Database connection
	 * @return The conversion factor and converted value as an ArrayList(0)
	 *         conversion factor ArraList(1) converted value
	 * @exception ITMException
	 * @see
	 */
	public ArrayList getConvQuantityFact(String frUom, String toUom, String forItem, double toConvert, double fact,
			Connection connectionObject) {
		String mRound = "";
		String lsOrder = "NOTFOUND";
		double newQty, mConv = 0, mRndTo = 0, errQty = -999999999;
		int mCnt;
		ArrayList returnValue = new ArrayList();
		String errCode = "";
		String sql = "";
		// Statement stmt;//[changed to prepared Statement by Pavan R]
		PreparedStatement pstmt = null;
		ResultSet conRs = null;
		if (frUom.trim().equals(toUom.trim())) {
			fact = 1;
			returnValue.add(Double.toString(fact));
			returnValue.add(Double.toString(toConvert));
			return returnValue;
		}
		try {

			// [stmt changed to pstmt and commented common sql Pavan R]
			// stmt = connectionObject.createStatement();
			// Check for unit in actual order with item code
			sql = "SELECT FACT, ROUND, ROUND_TO FROM UOMCONV WHERE UNIT__FR= ?"
					// + frUom.trim() + "' AND UNIT__TO='" + toUom.trim()
					// + "' AND ITEM_CODE='" + forItem.trim() + "'";
					+ " AND UNIT__TO = ? " + " AND ITEM_CODE = ? ";
			// + " 'AND ITEM_CODE = ?"; Commented By Jaffar S on 24-JAN-2019 to get UOM No
			// as suggested by Santosh Gupta
			// conRs = stmt.executeQuery(sql);
			pstmt = connectionObject.prepareStatement(sql);
			pstmt.setString(1, frUom.trim());
			pstmt.setString(2, toUom.trim());
			pstmt.setString(3, forItem.trim());
			conRs = pstmt.executeQuery();
			if (conRs.next()) {
				mConv = conRs.getDouble(1);
				mRound = conRs.getString(2);
				mRndTo = conRs.getDouble(3);
				lsOrder = "ACTORD";
			} else {
				/*
				 * sql =
				 * "SELECT FACT, ROUND, ROUND_TO FROM UOMCONV WHERE UNIT__FR='"//[commented by
				 * Pavan R] + toUom.trim() + "' AND UNIT__TO='" + frUom.trim() +
				 * "' AND ITEM_CODE='" + forItem.trim() + "'";
				 */
				pstmt.clearParameters();
				conRs.close();
				conRs = null;
				// conRs = stmt.executeQuery(sql);
				pstmt.setString(1, toUom.trim());
				pstmt.setString(2, frUom.trim());
				pstmt.setString(3, forItem.trim());
				conRs = pstmt.executeQuery();
				if (conRs.next()) {
					mConv = conRs.getDouble(1);
					mRound = conRs.getString(2);
					mRndTo = conRs.getDouble(3);
					lsOrder = "REVORD";
				} else {
					pstmt.clearParameters();
					conRs.close();
					conRs = null;
					/*
					 * sql =
					 * "SELECT FACT, ROUND, ROUND_TO FROM UOMCONV WHERE UNIT__FR='"//[commented by
					 * Pavan R] + frUom.trim() + "' AND UNIT__TO='" + toUom.trim() +
					 * "' AND ITEM_CODE='X'";
					 */
					// conRs = stmt.executeQuery(sql);
					pstmt.setString(1, frUom.trim());
					pstmt.setString(2, toUom.trim());
					pstmt.setString(3, "X");
					conRs = pstmt.executeQuery();
					if (conRs.next()) {
						mConv = conRs.getDouble(1);
						mRound = conRs.getString(2);
						mRndTo = conRs.getDouble(3);
						lsOrder = "ACTORD";
					}

					else {
						pstmt.clearParameters();
						conRs.close();
						conRs = null;
						/*
						 * sql =
						 * "SELECT FACT, ROUND, ROUND_TO FROM UOMCONV WHERE UNIT__FR='"//[commented by
						 * Pavan R] + toUom.trim() + "' AND UNIT__TO='" + frUom.trim() +
						 * "' AND ITEM_CODE='X'";
						 */
						// conRs = stmt.executeQuery(sql);
						pstmt.setString(1, toUom.trim());
						pstmt.setString(2, frUom.trim());
						pstmt.setString(3, "X");
						conRs = pstmt.executeQuery();

						if (conRs.next()) {
							mConv = conRs.getDouble(1);
							mRound = conRs.getString(2);
							mRndTo = conRs.getDouble(3);
							lsOrder = "REVORD";

						} else {
							returnValue.add(Double.toString(fact));
							returnValue.add(Double.toString(errQty));
							conRs.close();// [closed and nulled conRs and pstmt by Pavan R]
							conRs = null;
							pstmt.close();
							pstmt = null;
							return returnValue;
						}
					}
				}
			}
			conRs.close();
			conRs = null;
			// stmt.close();
			// stmt = null;
			pstmt.close();
			pstmt = null;
			// Check whether conversion factor need to be assigned
			if (fact == 0) {
				if (lsOrder.equals("ACTORD")) {
					newQty = mConv * toConvert;
					fact = mConv;
				} else {
					newQty = 1 / mConv * toConvert;
					fact = 1 / mConv;
				}
			} else {
				newQty = fact * toConvert;

			}

			newQty = getRndamt(newQty, mRound, mRndTo);
			returnValue.add(Double.toString(fact));
			returnValue.add(Double.toString(newQty));
		} // end of try
		catch (Exception e) {
			System.out.println("Exception :ITMDBAccessEJB :Conversion:" + e.getMessage() + ":");

		} finally {
			try {
				if (conRs != null) {
					conRs.close();
					conRs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
			}
		}
		return returnValue;
	}

	/**
	 * gets the rounded value as double
	 * 
	 * @param newQty
	 *            The value to be rounded
	 * @param round
	 *            The type of rounding to be done (N - No round, R - Near, X - Next
	 *            and P - Previous)
	 * @param roundTo
	 *            The number of decimal paces the rounding to be done (0.01 - 2
	 *            decimal, 0.001 - 3 decimal,...)
	 * @return The rounded value as double
	 * @see
	 */
	public double getRndamt(double newQty, String round, double roundTo) {
		double lcMultiply = 1;
		try {

			round = round.toUpperCase();
			if (newQty < 0) {
				lcMultiply = -1;
				newQty = Math.abs(newQty);
			} else if (newQty == 0) {
				return newQty;
			} else if (round.trim().equals("N")) {
				return newQty;
			} else if (roundTo == 0) {
				return newQty;
			}
			if (round.trim().equals("X")) {
				newQty = (newQty - (newQty % roundTo) + roundTo);
			}
			if (round.trim().equals("P")) {
				newQty = (newQty - (newQty % roundTo));
			}
			if (round.trim().equals("R")) {
				if ((newQty % roundTo) < (roundTo / 2)) {
					newQty = (newQty - (newQty % roundTo));
				} else {
					newQty = (newQty - (newQty % roundTo) + roundTo);
				}
			}
			System.out.println("newQty[" + newQty + "]");
			System.out.println("lcMultiply[" + lcMultiply + "]");
			newQty = newQty * lcMultiply;
			System.out.println("newQty * lcMultiply[" + newQty + "]");
			// Modified by Pavan Rane on 22NOV19[To format decimal places] start
			if (roundTo == 1) {
				newQty = getRequiredDecimal(newQty, 0);
			} else if (roundTo == .1) {
				newQty = getRequiredDecimal(newQty, 1);
			} else if (roundTo == .01) {
				newQty = getRequiredDecimal(newQty, 2);
			} else if (roundTo == .001) {
				newQty = getRequiredDecimal(newQty, 3);
			} else if (roundTo == .0001) {
				newQty = getRequiredDecimal(newQty, 4);
			}
			return newQty;
		} catch (Exception e) {
			System.out.println("Exception :Conversion Qty ::" + e.getMessage() + ":");

		}
		/*
		 * if (roundTo == 1) { newQty = getRequiredDecimal(newQty, 0); } else if
		 * (roundTo == .1) { newQty = getRequiredDecimal(newQty, 1); } else if (roundTo
		 * == .01) { newQty = getRequiredDecimal(newQty, 2); } else if (roundTo == .001)
		 * { newQty = getRequiredDecimal(newQty, 3); } else if (roundTo == .0001) {
		 * newQty = getRequiredDecimal(newQty, 4); }
		 */
		// Modified by Pavan Rane on 22NOV19[To format decimal places] end
		return newQty;
	}

	/**
	 * gets the rounded value as double
	 * 
	 * @param actVal
	 *            The value to be rounded
	 * @param prec
	 *            The the number of decimal places
	 * @return The rounded values
	 * @see
	 */
	public double getRequiredDecimal(double actVal, int prec) {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		Double DoubleValue = new Double(actVal);
		// Modified by Pavan Rane on 22NOV19[To format decimal places] start
		// numberFormat.setMaximumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(6);
		// Modified by Pavan Rane on 22NOV19[To format decimal places] start
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",", "");
		double reqVal = Double.parseDouble(strValue);
		return reqVal;
	}

	/*
	 * public double convQtyFactor(String unitFrom,String unitTo,String
	 * itemCode,double toConvert,Connection conn)throws ITMException {
	 * PreparedStatement pstmt = null; ResultSet rs = null; String sql = ""; String
	 * round = ""; double fact = 0.0; double roundTo = 0.0; double newQty = 0.0;
	 * double errQty = -999999999; int count = 0;
	 * 
	 * if(unitFrom.trim().equals(unitTo.trim())) { return toConvert; } try { sql =
	 * "SELECT COUNT(*) AS COUNT FROM UOMCONV " + "WHERE UNIT__FR = ? " +
	 * "AND UNIT__TO = ? " + "AND ITEM_CODE = ?"; pstmt =
	 * conn.prepareStatement(sql); pstmt.setString(1,unitFrom);
	 * pstmt.setString(2,unitTo); pstmt.setString(3,itemCode); rs =
	 * pstmt.executeQuery(); if(rs.next()) { count = rs.getInt("COUNT");
	 * System.out.println("Count........1 "+count); } pstmt.close(); }//try
	 * catch(Exception e) {
	 * System.out.println("Exception...[convQtyFactor] "+sql+e.getMessage());
	 * e.printStackTrace(); throw new ITMException(e); } if(count == 0) { try { sql
	 * = "SELECT COUNT(*) AS COUNT FROM UOMCONV " + "WHERE UNIT__FR = ? " +
	 * "AND UNIT__TO = ? " + "AND ITEM_CODE = 'X'"; pstmt =
	 * conn.prepareStatement(sql); pstmt.setString(1,unitFrom);
	 * pstmt.setString(2,unitTo); rs = pstmt.executeQuery(); if(rs.next()) { count =
	 * rs.getInt("COUNT"); System.out.println("Count........2 "+count); }
	 * pstmt.close(); pstmt = null; } catch(Exception e) {
	 * System.out.println("Exception...[convQtyFactor] "+sql+e.getMessage());
	 * e.printStackTrace(); throw new ITMException(e); } if(count == 0) { return
	 * errQty; } else { try { sql = "SELECT FACT,ROUND,ROUND_TO FROM UOMCONV " +
	 * "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? " + "AND ITEM_CODE = 'X'"; pstmt =
	 * conn.prepareStatement(sql); pstmt.setString(1,unitFrom);
	 * pstmt.setString(2,unitTo); rs = pstmt.executeQuery(); if(rs.next()) { fact =
	 * rs.getDouble(1); round = rs.getString(2); roundTo = rs.getDouble(3);
	 * System.out.println("fact........"+fact); } pstmt.close(); pstmt = null; }
	 * catch(Exception e) {
	 * System.out.println("Exception...[convQtyFactor] "+sql+e.getMessage());
	 * e.printStackTrace(); throw new ITMException(e); } }//else }//if else { try {
	 * sql = "SELECT FACT,ROUND,ROUND_TO FROM UOMCONV " + "WHERE UNIT__FR = ? " +
	 * "AND UNIT__TO = ? " + "AND ITEM_CODE = ?"; pstmt =
	 * conn.prepareStatement(sql); pstmt.setString(1,unitFrom);
	 * pstmt.setString(2,unitTo); pstmt.setString(3,itemCode); rs =
	 * pstmt.executeQuery(); if(rs.next()) { fact = rs.getDouble(1); round =
	 * rs.getString(2); roundTo = rs.getDouble(3);
	 * System.out.println("fact........"+fact); } pstmt.close(); pstmt = null; }
	 * catch(Exception e) {
	 * System.out.println("Exception...[convQtyFactor] "+sql+e.getMessage());
	 * e.printStackTrace(); throw new ITMException(e); } }//else newQty = (fact *
	 * toConvert); round = round.toUpperCase(); if(round.trim().equals("X")) {
	 * newQty = (newQty - (newQty % roundTo) + roundTo); }
	 * if(round.trim().equals("P")) { newQty = (newQty - (newQty % roundTo)); }
	 * if(round.trim().equals("R")) { if((newQty % roundTo) < (roundTo / 2)) {
	 * newQty = (newQty - (newQty % roundTo)); } else { newQty = (newQty - (newQty %
	 * roundTo) + roundTo); } } return newQty; }//convQtyFactor
	 */

	/**
	 * Gets the conversion factor and converted value as an ArrayList(0)
	 * conversion factor ArraList(1) converted value
	 * 
	 * @param unitFrom
	 *            The unit of measure from which to convert
	 * @param unitTo
	 *            The unit of measure to which to convert
	 * @param itemCode
	 *            The item code for the conversion to be done
	 * @param toConvert
	 *            The value to be convert
	 * @param fact
	 *            The conversion factor user inputed
	 * @param conn
	 *            JDBC Database connection
	 * @return The conversion factor and converted value as an ArrayList(0)
	 *         conversion factor ArraList(1) converted value
	 * @exception ITMException
	 * @see
	 */
	public ArrayList convQtyFactor(String unitFrom, String unitTo, String itemCode, double toConvert, double fact,
			Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String round = "";
		double roundTo = 0.0;
		double newQty = 0.0;
		double errQty = -999999999;
		int count = 0;
		ArrayList retValue = new ArrayList();

		if (unitFrom.trim().equals(unitTo.trim())) {
			// return toConvert;
			fact = 1;
			retValue.add(new Double(fact));
			retValue.add(new Double(toConvert));
			return retValue;
		}
		try {
			sql = "SELECT COUNT(*) AS COUNT FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
					+ "AND ITEM_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, unitFrom);
			pstmt.setString(2, unitTo);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt("COUNT");
				System.out.println("Count........1 " + count);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} // try
		catch (Exception e) {
			System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		if (count == 0) {
			try {
				sql = "SELECT COUNT(*) AS COUNT FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
						+ "AND ITEM_CODE = 'X'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, unitFrom);
				pstmt.setString(2, unitTo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt("COUNT");
					System.out.println("Count........2 " + count);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} catch (Exception e) {
				System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
			if (count == 0) {
				// return errQty;
				if (fact == 0) {
					retValue.add(new Double(fact));
					retValue.add(new Double(errQty));
					return retValue;
				} else {
					retValue.add(new Double(fact));
					retValue.add(new Double(toConvert * fact));
					return retValue;
				}
			} else {
				try {
					sql = "SELECT FACT,ROUND,ROUND_TO FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
							+ "AND ITEM_CODE = 'X'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitFrom);
					pstmt.setString(2, unitTo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						fact = rs.getDouble(1);
						round = rs.getString(2);
						roundTo = rs.getDouble(3);
						System.out.println("fact........" + fact);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} catch (Exception e) {
					System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} // else
		} // if
		else {
			try {
				sql = "SELECT FACT,ROUND,ROUND_TO FROM UOMCONV " + "WHERE UNIT__FR = ? " + "AND UNIT__TO = ? "
						+ "AND ITEM_CODE = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, unitFrom);
				pstmt.setString(2, unitTo);
				pstmt.setString(3, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					fact = rs.getDouble(1);
					round = rs.getString(2);
					roundTo = rs.getDouble(3);
					System.out.println("fact........" + fact);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} catch (Exception e) {
				System.out.println("Exception...[convQtyFactor] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		} // else
		newQty = (fact * toConvert);
		round = round.toUpperCase();
		if (round.trim().equals("X")) {
			newQty = (newQty - (newQty % roundTo) + roundTo);
		}
		if (round.trim().equals("P")) {
			newQty = (newQty - (newQty % roundTo));
		}
		if (round.trim().equals("R")) {
			if ((newQty % roundTo) < (roundTo / 2)) {
				newQty = (newQty - (newQty % roundTo));
			} else {
				newQty = (newQty - (newQty % roundTo) + roundTo);
			}
		}

		retValue.add(new Double(fact));
		retValue.add(new Double(newQty));
		return retValue;

		// return newQty;
	}

	/**
	 * Gets the integral quantity item, site and customer combination as per
	 * business logic
	 * 
	 * @param custCode
	 *            The customer code
	 * @param itemCode
	 *            The item code
	 * @param siteCode
	 *            The site code
	 * @param conn
	 *            JDBC Database connection
	 * @return The integral quantity as double
	 * @see
	 */
	public double getIntegralQty(String custCode, String itemCode, String siteCode, Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		double intQty = 0.0;
		int cnt = 0;

		try {
			sql = "select integral_qty  from customeritem " + " where cust_code = ? " // '" + custCode + "' "
					+ "and item_code = ? "; // '" + itemCode + "' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				intQty = rs.getDouble(1);
				System.out.println("intQty........" + intQty);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (intQty == 0) {
				sql = "select integral_qty  from siteitem " + " where site_code = ? " // '" + siteCode + "' "
						+ " and item_code = ?"; // '" + itemCode + "' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					intQty = rs.getDouble(1);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (intQty == 0) {
					sql = "select integral_qty  from item " + "where item_code = ?"; // '" + itemCode + "' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						intQty = rs.getDouble(1);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (intQty == 0) {
						sql = "select count(*)  from item where item_code = ? ";// '"
						// + itemCode + "' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0) {
							sql = "select batch_qty  from bom " + " where bom_code =? "; // '" + itemCode + "' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								intQty = rs.getDouble(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

				}
			}

		} catch (Exception e) {
			System.out.println("Exception...[getIntegralQty] " + sql + e.getMessage());
			e.printStackTrace();
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
			}
		}

		return intQty;
	} // GET

	public String getToken(String source, String separator) {

		int p = 0;
		int lenSource = 0;
		int lenSeparator = 0;
		int diffstr = 0;
		String ret = "";

		try {
			// p = Pos(source, separator)
			p = source.indexOf(separator);
			if (p == -1) // if no separator,
			{
				ret = source; // return the whole source string and
				source = ""; // make the original source of zero length
			} else {
				ret = source.substring(0, p);
				lenSource = source.length();
				lenSeparator = separator.length();
				diffstr = lenSource - (p + lenSeparator) + 1;
				source = source.substring(diffstr, lenSource);
			}
		} catch (Exception e) {
			System.out.println("Exception...[getToken] " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * gets the item series (division) as String
	 * 
	 * @param itemCode
	 *            The item code
	 * @param siteCode
	 *            The site code
	 * @param tranDate
	 *            The date on which the division to be valid
	 * @param sudryCode
	 *            The sundry code
	 * @param sundryType
	 *            The sundry type
	 * @param conn
	 *            JDBC Database connection
	 * @return The division as String
	 * @see
	 */
	public String getItemSer(String itemCode, String siteCode, Timestamp tranDate, String sudryCode, String sundryType,
			Connection conn) // Added by pankaj 01/09/2009 to return Itemser
	{
		String itemSer = null;
		String itemSerInv = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// Changes by PankajR on 06-MAY-19 for PreparedStatement
		try {
			/*
			 * sql = "SELECT item_ser FROM siteitem " + "WHERE site_code = '" + siteCode +
			 * "'" + "AND ITEM_CODE = '" + itemCode + "'";
			 */
			sql = "SELECT item_ser FROM siteitem WHERE site_code = ? AND ITEM_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				itemSer = rs.getString(1);
				System.out.println("itemSer........" + itemSer);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			if (itemSer == null || itemSer.trim().length() == 0) {
				/*
				 * sql = "select item_ser from itemser_change " + " where  ITEM_CODE = '" +
				 * itemCode + "'" + " and (( eff_date <= ?" +
				 * " and  valid_upto >= ? ) or (valid_upto is Null))";
				 */
				sql = "select item_ser from itemser_change  where  ITEM_CODE = ? and (( eff_date <= ? and  valid_upto >= ? ) or (valid_upto is Null))";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					itemSer = rs.getString(1);
					System.out.println("itemSer........" + itemSer);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				// CHECKING ITEM SERIES AGAIN FROM ITEMSER CHANGE FOR OLD ITEM
				// SER
				if (itemSer == null || itemSer.trim().length() == 0) {
					/*
					 * sql = " SELECT item_ser__old FROM itemser_change " + " WHERE ITEM_CODE = '" +
					 * itemCode + "'" + " AND (( eff_date >= ?" +
					 * " AND  valid_upto >= ?) or (valid_upto is null ))" +
					 * " AND eff_date = (select min(eff_date) from itemser_change" +
					 * " where item_code = '" + itemCode + "')";
					 */

					sql = " SELECT item_ser__old FROM itemser_change  WHERE ITEM_CODE = ? AND (( eff_date >= ? AND  valid_upto >= ?) "
							+ "or (valid_upto is null )) AND eff_date = (select min(eff_date) from itemser_change where item_code = ?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setTimestamp(2, tranDate);
					pstmt.setTimestamp(3, tranDate);
					pstmt.setString(4, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						itemSer = rs.getString(1);
						System.out.println("itemSer........" + itemSer);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (itemSer == null || itemSer.trim().length() == 0) {
						/*
						 * sql = " SELECT item_ser__old FROM itemser_change " + " WHERE ITEM_CODE = '" +
						 * itemCode + "'" + " AND (( eff_date >= ?" +
						 * " AND valid_upto >= ?) or (valid_upto is null))" +
						 * " AND eff_date = (select min(eff_date) from itemser_change" +
						 * " where item_code = '" + itemCode + "')";
						 */
						/*
						 * sql =
						 * " SELECT item_ser__old FROM itemser_change  WHERE ITEM_CODE = ? AND (( eff_date >= ? AND valid_upto >= ?) "
						 * +
						 * "or (valid_upto is null)) AND eff_date = (select min(eff_date) from itemser_change where item_code = ?)"
						 * ; pstmt = conn.prepareStatement(sql); pstmt.setString(1, itemCode);
						 * pstmt.setTimestamp(2, tranDate); pstmt.setTimestamp(3, tranDate);
						 * pstmt.setString(4, itemCode); rs = pstmt.executeQuery(); if (rs.next()) {
						 * itemSer = rs.getString(1); System.out.println("itemSer........" + itemSer); }
						 * pstmt.close(); pstmt = null; rs.close(); rs = null; if (itemSer == null ||
						 * itemSer.trim().length() == 0) // Condition added by Asikarwar on 06/Sep/2013
						 * as par pb // code {
						 */
						/*
						 * sql = "SELECT item_ser FROM item " + "WHERE ITEM_CODE = '" + itemCode + "'";
						 */
						sql = "SELECT item_ser FROM item WHERE ITEM_CODE = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							itemSer = rs.getString(1);
							System.out.println("itemSer........" + itemSer);
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						// }
					}
				}
			}
			if (sundryType.equalsIgnoreCase("C")) {
				/*
				 * sql = "SELECT item_ser__inv FROM customer_series " + "WHERE cust_code = '" +
				 * sudryCode + "'" + "AND item_ser = '" + itemSer + "'";
				 */
				sql = "SELECT item_ser__inv FROM customer_series WHERE cust_code = ? AND item_ser = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, sudryCode);
				pstmt.setString(2, itemSer);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					itemSerInv = rs.getString(1);
					System.out.println("itemSer........" + itemSer);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (itemSerInv != null && itemSerInv.trim().length() > 0) {
					itemSer = itemSerInv;
				}
			}
		} catch (Exception e) {
			System.out.println("Exception...[getItemSer] " + sql + e.getMessage());
			e.printStackTrace();
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
			}
		}

		return itemSer;
	}// end getItemSer
	// added getNoArt from return ArrayList

	// add new goArt with retur ArrayList
	/**
	 * Gets no. of articles, shipper quantity and integral quantity as an
	 * ArrayList
	 * 
	 * @param siteCode
	 *            The site code
	 * @param custCode
	 *            The customer code
	 * @param itemCode
	 *            The item code
	 * @param packCode
	 *            The packing code
	 * @param acQty
	 *            The quantity
	 * @param type
	 *            I - Integral and S - Shipper
	 * @param acShipperQty
	 * @param acIntegralQty
	 * @param conn
	 *            JDBC Database connection
	 * @return Number of articles, shipper quantity and integral quantity as an
	 *         ArrayList
	 * @see
	 */
	public ArrayList getNoArtAList(String siteCode, String custCode, String itemCode, String packCode, double acQty,
			char type, double acShipperQty, double acIntegralQty, Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		double capacity = 0.0, reoQty = 0.0, shipperQty = 0.0, remainder = 0.0, lcIntegralQty = 0.0;
		double liNoArt1 = 0, liNoArt2 = 0, liNoArt = 0;
		ArrayList returnValue = new ArrayList();
		try {
			switch (type) {
			case 'S':
				sql = "select (case when capacity is null then 0 else capacity end) " + "from packing "
						+ "where pack_code = '" + packCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					capacity = rs.getDouble(1);
					System.out.println("capacity........" + capacity);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				sql = " select reo_qty   from siteitem  where site_code ='" + siteCode + "'" + " and item_code = '"
						+ itemCode + "'";
				System.out.println("sql for retro qty:- " + sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					reoQty = rs.getDouble(1);
					System.out.println("reoQty........" + reoQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (reoQty == 0.0) {
					sql = "select reo_qty  from item " + "where item_code = '" + itemCode + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						reoQty = rs.getDouble(1);
						System.out.println("reoQty........" + reoQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if (capacity > 0)
					shipperQty = capacity;
				else
					shipperQty = reoQty;

				if (shipperQty > 0) {
					liNoArt = (acQty - acQty % shipperQty) / shipperQty;
				}
				break;

			case 'I':
				/*
				 * sql=
				 * "select (case when capacity is null then 0 else capacity end)  from packing "
				 * +"where pack_code = '"+packCode+"' "; pstmt = conn.prepareStatement(sql); rs
				 * = pstmt.executeQuery(); if(rs.next()) { capacity = rs.getDouble(1);
				 * System.out.println("capacity........"+capacity); } pstmt.close(); pstmt =
				 * null; rs.close(); rs = null;
				 * 
				 * sql="select reo_qty   from siteitem " +"where site_code = '"+siteCode+"' "
				 * +"and item_code = '"+itemCode+"'"; pstmt = conn.prepareStatement(sql); rs =
				 * pstmt.executeQuery(); if(rs.next()) { reoQty = rs.getDouble(1);
				 * System.out.println("reoQty........"+reoQty); } pstmt.close(); pstmt = null;
				 * rs.close(); rs = null; if( reoQty==0 ) { sql="select reo_qty	from item "
				 * +"where item_code = '"+itemCode+"' "; pstmt = conn.prepareStatement(sql); rs
				 * = pstmt.executeQuery(); if(rs.next()) { reoQty = rs.getDouble(1);
				 * System.out.println("reoQty........"+reoQty); } pstmt.close(); pstmt = null;
				 * rs.close(); rs = null; } if(capacity > 0) shipperQty = capacity; else
				 * shipperQty = reoQty; if( shipperQty > 0 ) { liNoArt =(acQty -
				 * acQty%shipperQty)/shipperQty; remainder = acQty%shipperQty; }
				 */
				sql = "select integral_qty  from customeritem " + "where cust_code = '" + custCode + "' "
						+ "and item_code = '" + itemCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcIntegralQty = rs.getDouble(1);
					System.out.println("lcIntegralQty........" + lcIntegralQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (lcIntegralQty == 0) {
					sql = "select integral_qty   from siteitem " + "where site_code = '" + siteCode + "' "
							+ "and item_code = '" + itemCode + "' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcIntegralQty = rs.getDouble(1);
						System.out.println("lcIntegralQty........" + lcIntegralQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (lcIntegralQty == 0) {
						sql = "select (case when integral_qty is null then 0 else integral_qty end)"
								+ "from item where item_code = '" + itemCode + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcIntegralQty = rs.getDouble(1);
							System.out.println("lcIntegralQty........" + lcIntegralQty);
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}

				}
				// change done by kunal on 15/jan/14 as per PB Code
				if (lcIntegralQty > 0)
					liNoArt = (acQty - acQty % lcIntegralQty) / lcIntegralQty;

				break;
			case 'B':
				sql = "select (case when capacity is null then 0 else capacity end)  from packing "
						+ "where pack_code = '" + packCode + "' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					capacity = rs.getDouble(1);
					System.out.println("capacity........" + capacity);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				sql = "select reo_qty   from siteitem " + "where site_code = '" + siteCode + "' " + "and item_code = '"
						+ itemCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					reoQty = rs.getDouble(1);
					System.out.println("reoQty........" + reoQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (reoQty == 0) {
					sql = "select reo_qty	from item " + "where item_code = '" + itemCode + "' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						reoQty = rs.getDouble(1);
						System.out.println("reoQty........" + reoQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if (capacity > 0)
					shipperQty = capacity;
				else
					shipperQty = reoQty;
				if (shipperQty > 0) {
					liNoArt1 = (acQty - acQty % shipperQty) / shipperQty;
					remainder = acQty % shipperQty;
				}
				sql = "select integral_qty  from customeritem " + "where cust_code = '" + custCode + "' "
						+ "and item_code = '" + itemCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcIntegralQty = rs.getDouble(1);
					System.out.println("lcIntegralQty........" + lcIntegralQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (lcIntegralQty == 0) {
					sql = "select integral_qty   from siteitem " + "where site_code = '" + siteCode + "' "
							+ "and item_code = '" + itemCode + "' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcIntegralQty = rs.getDouble(1);
						System.out.println("lcIntegralQty........" + lcIntegralQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (lcIntegralQty == 0) {
						sql = "select (case when integral_qty is null then 0 else integral_qty end)"
								+ "from item where item_code = '" + itemCode + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcIntegralQty = rs.getDouble(1);
							System.out.println("lcIntegralQty........" + lcIntegralQty);
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}
				}
				if (lcIntegralQty > 0)
					liNoArt2 = (remainder - remainder % lcIntegralQty) / lcIntegralQty;
				if (liNoArt2 > 0)
					liNoArt2 = 1;
				liNoArt = liNoArt1 + liNoArt2;
				break;
				// change done by kunal on 15/jan/14 as per PB Code end
			}

		} catch (Exception e) {
			System.out.println("Exception...[getNoArt] " + sql + e.getMessage());
			e.printStackTrace();
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
			}
		}

		acShipperQty = shipperQty;
		acIntegralQty = lcIntegralQty;
		returnValue.add(Double.toString(liNoArt));
		returnValue.add(Double.toString(acShipperQty));
		returnValue.add(Double.toString(acIntegralQty));
		return returnValue;
	} // end getNoart()

	// end of getNoArt
	// start added by alam on 020209
	/**
	 * gets no. of articles as int
	 * 
	 * @param siteCode
	 *            The site code
	 * @param custCode
	 *            The customer code
	 * @param itemCode
	 *            The item code
	 * @param packCode
	 *            The packing code
	 * @param acQty
	 *            The quantity
	 * @param type
	 *            I - Integral and S - Shipper
	 * @param acShipperQty
	 * @param acIntegralQty
	 * @param conn
	 *            JDBC Database connection
	 * @return Number of articles, shipper quantity and integral quantity as an
	 *         ArrayList
	 * @see
	 */
	public int getNoArt(String siteCode, String custCode, String itemCode, String packCode, double acQty, char type,
			double acShipperQty, double acIntegralQty, Connection conn) // Added by Manazir
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		double capacity = 0.0, reoQty = 0.0, shipperQty = 0.0, remainder = 0.0, lcIntegralQty = 0.0;
		double liNoArt1 = 0, liNoArt2 = 0, liNoArt = 0;
		try {
			switch (type) {
			case 'B':
				double lcRemainder = 0.0;
				double lcCapacity = 0.0;
				sql = "select (case when capacity is null then 0 else capacity end) lc_capacity " + " 	from packing "
						+ " where pack_code = ? "; // :as_pack_code;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, packCode);

				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcCapacity = rs.getDouble("lc_capacity");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				double lcReoQty = 0.0;
				sql = "select ( case when reo_qty is null then 0 else reo_qty end ) lc_reo_qty " + "	from siteitem "
						+ "	where site_code = ? "// :as_site_code
						+ " 		and item_code = ? "; // :as_item_code;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCode);

				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcReoQty = rs.getDouble("lc_reo_qty");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (lcReoQty == 0) {
					sql = " select  ( case when reo_qty is null then 0 else reo_qty end ) lc_reo_qty " + " from item "
							+ " where item_code = ? "; // :as_item_code;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);

					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcReoQty = rs.getDouble("lc_reo_qty");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}

				double lcShipperQty = 0.0;
				if (lcCapacity > 0) {
					lcShipperQty = lcCapacity;
				} else {
					lcShipperQty = lcReoQty;
				}

				double modResult = 0.0;
				if (lcShipperQty > 0) {
					sql = "select mod(?,?) result from dual";
					pstmt = conn.prepareStatement(sql);

					pstmt.setDouble(1, acQty);
					pstmt.setDouble(2, lcShipperQty);

					rs = pstmt.executeQuery();
					if (rs.next()) {
						modResult = rs.getDouble("result");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					liNoArt1 = (acQty - modResult) / lcShipperQty;
					lcRemainder = modResult;
				}

				int integralQty = 0;
				sql = "select ( case when integral_qty is null then 0 else integral_qty end ) lc_integral_qty "
						+ "	from customeritem " + " where cust_code = ? " // :as_cust_code
						+ "	and item_code = ? ";// :as_item_code;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);

				rs = pstmt.executeQuery();
				if (rs.next()) {
					integralQty = rs.getInt("lc_integral_qty");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (integralQty == 0) {
					sql = " select ( case when integral_qty is null then 0 else integral_qty end ) lc_integral_qty "
							+ "  from siteitem " + " where site_code = ? "// :as_site_code
							+ "	and item_code = ? ";// :as_item_code;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, itemCode);

					rs = pstmt.executeQuery();
					if (rs.next()) {
						integralQty = rs.getInt("lc_integral_qty");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (integralQty == 0) {
						sql = "select (case when integral_qty is null then 0 else integral_qty end) lc_integral_qty "
								+ " from item " + " where item_code = ? ";// :as_item_code;

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);

						rs = pstmt.executeQuery();
						if (rs.next()) {
							integralQty = rs.getInt("lc_integral_qty");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}

				if (lcIntegralQty > 0) {
					sql = "select mod(?,?) result from dual";
					pstmt = conn.prepareStatement(sql);

					pstmt.setDouble(1, lcRemainder);
					pstmt.setDouble(2, lcIntegralQty);

					rs = pstmt.executeQuery();
					if (rs.next()) {
						modResult = rs.getDouble("result");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					liNoArt2 = (lcRemainder - modResult / lcIntegralQty);
				}

				if (liNoArt2 > 0) {
					liNoArt2 = 1;
				}

				liNoArt = liNoArt1 + liNoArt2;
				break;

			case 'S':
				sql = "select (case when capacity is null then 0 else capacity end) " + "from packing "
						+ "where pack_code = '" + packCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					capacity = rs.getDouble(1);
					System.out.println("capacity........" + capacity);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				sql = " select reo_qty   from siteitem  where site_code ='" + siteCode + "'" + " and item_code = '"
						+ itemCode + "'";
				System.out.println("sql for retro qty:- " + sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					reoQty = rs.getDouble(1);
					System.out.println("reoQty........" + reoQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (reoQty == 0.0) {
					sql = "select reo_qty  from item " + "where item_code = '" + itemCode + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						reoQty = rs.getDouble(1);
						System.out.println("reoQty........" + reoQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if (capacity > 0)
					shipperQty = capacity;
				else
					shipperQty = reoQty;

				if (shipperQty > 0) {
					liNoArt = (acQty - acQty % shipperQty) / shipperQty;
				}
				break;

			case 'I':
				sql = "select (case when capacity is null then 0 else capacity end)  from packing "
						+ "where pack_code = '" + packCode + "' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					capacity = rs.getDouble(1);
					System.out.println("capacity........" + capacity);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				sql = "select reo_qty   from siteitem " + "where site_code = '" + siteCode + "' " + "and item_code = '"
						+ itemCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					reoQty = rs.getDouble(1);
					System.out.println("reoQty........" + reoQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (reoQty == 0) {
					sql = "select reo_qty	from item " + "where item_code = '" + itemCode + "' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						reoQty = rs.getDouble(1);
						System.out.println("reoQty........" + reoQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
				}
				if (capacity > 0)
					shipperQty = capacity;
				else
					shipperQty = reoQty;
				if (shipperQty > 0) {
					// liNoArt =(acQty - acQty%shipperQty)/shipperQty;
					// Commented by Jaffar S on 24-JAN-2019 to set liNoArt1 as suggested by Santosh
					// Gupta
					liNoArt1 = (acQty - acQty % shipperQty) / shipperQty;
					remainder = acQty % shipperQty;
				}
				sql = "select integral_qty  from customeritem " + "where cust_code = '" + custCode + "' "
						+ "and item_code = '" + itemCode + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lcIntegralQty = rs.getDouble(1);
					System.out.println("lcIntegralQty........" + lcIntegralQty);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (lcIntegralQty == 0) {
					sql = "select integral_qty   from siteitem " + "where site_code = '" + siteCode + "' "
							+ "and item_code = '" + itemCode + "' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lcIntegralQty = rs.getDouble(1);
						System.out.println("lcIntegralQty........" + lcIntegralQty);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (lcIntegralQty == 0) {
						sql = "select (case when integral_qty is null then 0 else integral_qty end)"
								+ "from item where item_code = '" + itemCode + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lcIntegralQty = rs.getDouble(1);
							System.out.println("lcIntegralQty........" + lcIntegralQty);
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}
					if (lcIntegralQty > 0)
						liNoArt2 = (remainder - remainder % lcIntegralQty) / lcIntegralQty;
					if (liNoArt2 > 0)
						liNoArt2 = 1;
					liNoArt = liNoArt1 + liNoArt2;
				}
				break;
			}

		} catch (Exception e) {
			System.out.println("Exception...[getNoArt] " + sql + e.getMessage());
			e.printStackTrace();
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
			}
		}
		acShipperQty = shipperQty;
		acIntegralQty = lcIntegralQty;
		return (int) liNoArt;

	} // end getNoart()

	// end added by alam on 020209
	/*
	 * public int getNoArt(String siteCode,String custCode,String itemCode , String
	 * packCode, int acQty , char type , double acShipperQty , double acIntegralQty
	 * , Connection conn) // Added by Manazir { PreparedStatement pstmt = null;
	 * ResultSet rs = null; String sql = ""; double capacity
	 * =0.0,reoQty=0.0,shipperQty=0.0,remainder=0.0,lcIntegralQty=0.0; double
	 * liNoArt1=0,liNoArt2=0,liNoArt=0; try { switch(type) { case 'S':
	 * sql="select (case when capacity is null then 0 else capacity end) "
	 * +"from packing " +"where pack_code = '"+packCode+"'"; pstmt =
	 * conn.prepareStatement(sql); rs = pstmt.executeQuery(); if(rs.next()) {
	 * capacity = rs.getDouble(1); System.out.println("capacity........"+capacity);
	 * } pstmt.close(); pstmt = null; rs.close(); rs = null;
	 * sql=" select reo_qty   from siteitem  where site_code ='"+siteCode+"'"
	 * +" and item_code = '" + itemCode + "'";
	 * System.out.println("sql for retro qty:- " + sql); pstmt =
	 * conn.prepareStatement(sql); rs = pstmt.executeQuery(); if(rs.next()) { reoQty
	 * = rs.getDouble(1); System.out.println("reoQty........"+reoQty); }
	 * pstmt.close(); pstmt = null; rs.close(); rs = null; if(reoQty ==0.0) {
	 * sql="select reo_qty  from item " +"where item_code = '" + itemCode + "'";
	 * pstmt = conn.prepareStatement(sql); rs = pstmt.executeQuery(); if(rs.next())
	 * { reoQty = rs.getDouble(1); System.out.println("reoQty........"+reoQty); }
	 * pstmt.close(); pstmt = null; rs.close(); rs = null; } if(capacity > 0)
	 * shipperQty = capacity; else shipperQty = reoQty;
	 * 
	 * if( shipperQty > 0 ) { liNoArt =(acQty - acQty%shipperQty)/shipperQty; }
	 * break;
	 * 
	 * case 'I' : sql=
	 * "select (case when capacity is null then 0 else capacity end)  from packing "
	 * +"where pack_code = '"+packCode+"' "; pstmt = conn.prepareStatement(sql); rs
	 * = pstmt.executeQuery(); if(rs.next()) { capacity = rs.getDouble(1);
	 * System.out.println("capacity........"+capacity); } pstmt.close(); pstmt =
	 * null; rs.close(); rs = null;
	 * 
	 * sql="select reo_qty   from siteitem " +"where site_code = '"+siteCode+"' "
	 * +"and item_code = '"+itemCode+"'"; pstmt = conn.prepareStatement(sql); rs =
	 * pstmt.executeQuery(); if(rs.next()) { reoQty = rs.getDouble(1);
	 * System.out.println("reoQty........"+reoQty); } pstmt.close(); pstmt = null;
	 * rs.close(); rs = null; if( reoQty==0 ) { sql="select reo_qty	from item "
	 * +"where item_code = '"+itemCode+"' "; pstmt = conn.prepareStatement(sql); rs
	 * = pstmt.executeQuery(); if(rs.next()) { reoQty = rs.getDouble(1);
	 * System.out.println("reoQty........"+reoQty); } pstmt.close(); pstmt = null;
	 * rs.close(); rs = null; } if(capacity > 0) shipperQty = capacity; else
	 * shipperQty = reoQty; if( shipperQty > 0 ) { liNoArt =(acQty -
	 * acQty%shipperQty)/shipperQty; remainder = acQty%shipperQty; }
	 * sql="select integral_qty  from customeritem "
	 * +"where cust_code = '"+custCode+"' " +"and item_code = '"+itemCode+"'"; pstmt
	 * = conn.prepareStatement(sql); rs = pstmt.executeQuery(); if(rs.next()) {
	 * lcIntegralQty = rs.getDouble(1);
	 * System.out.println("lcIntegralQty........"+lcIntegralQty); } pstmt.close();
	 * pstmt = null; rs.close(); rs = null; if(lcIntegralQty ==0) {
	 * sql="select integral_qty   from siteitem "
	 * +"where site_code = '"+siteCode+"' " +"and item_code = '"+itemCode+"' ";
	 * pstmt = conn.prepareStatement(sql); rs = pstmt.executeQuery(); if(rs.next())
	 * { lcIntegralQty = rs.getDouble(1);
	 * System.out.println("lcIntegralQty........"+lcIntegralQty); } pstmt.close();
	 * pstmt = null; rs.close(); rs = null; if(lcIntegralQty==0) {
	 * sql="select (case when integral_qty is null then 0 else integral_qty end)"
	 * +"from item where item_code = '"+itemCode+"' "; pstmt =
	 * conn.prepareStatement(sql); rs = pstmt.executeQuery(); if(rs.next()) {
	 * lcIntegralQty = rs.getDouble(1);
	 * System.out.println("lcIntegralQty........"+lcIntegralQty); } pstmt.close();
	 * pstmt = null; rs.close(); rs = null; } if( lcIntegralQty>0 ) liNoArt2 =
	 * (remainder - remainder%lcIntegralQty)/lcIntegralQty ; if( liNoArt2 > 0)
	 * liNoArt2=1; liNoArt = liNoArt1 + liNoArt2; } break ; }
	 * 
	 * } catch(Exception e) {
	 * System.out.println("Exception...[getNoArt] "+sql+e.getMessage());
	 * e.printStackTrace(); } acShipperQty = shipperQty; acIntegralQty =
	 * lcIntegralQty; return (int)liNoArt;
	 * 
	 * } // end getNoart()
	 */
	// added by rajendra on 10/23/2008
	public String stockTransferMulti(String tran_id, String invLink, String xtraParams, Connection conn)
			throws ITMException {
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		String sql = "";
		String siteCode = "", refSerFor = "", refIdFor = "", reasCode = "", mcctr = "";
		String itemCode = "", locCodeFrom = "", locCodeTo = "", lotNo = "", lotNoTo = "", lotSl = "", lotSlTo = "",
				acctCodeCr = "", acctCodeDr = "";
		String cctrCodeDr = "", cctrCodeCr = "";
		String invstat = "", grade = "", suppCodeMfg = "", siteCodeMfg = "", unit = "", packCode = "", unitAlt = "",
				dimension = "";
		String packInstr = "", cctrCodeInv = "", acctCodeInv = "", batchNo = "", postOnLine = "", finEntity = "";
		String itemSer = "", overiss = "", errString = "", macct = "", currCode = "", expLev = "";
		String invacct = "", trfacct = "", errCode = "", invStat = "", ls_inv_online = "";
		String acctCodeXFRX = "", cctrCodeXFRX = "", trantype = "", remarks = "";
		HashMap stkUpdMap = null, glTraceMap = null;
		int lineNo = 0, qccount = 0, noArt, qcCount = 0, upd = 0;
		double effqty = 0, amt = 0;
		Timestamp tranDate = null, mfgDate = null, expDate = null, retestDate = null;
		double qtyorg = 0, qty = 0, rate = 0, grossRate = 0, grossWeight = 0, tareWeight = 0, netWeight = 0,
				convQtyStduom = 0, potencyPerc = 0;
		try {
			stkUpdMap = new HashMap();
			glTraceMap = new HashMap();
			FinCommon finCommon = new FinCommon();
			sql = "SELECT    TRAN_DATE, SITE_CODE,   REF_SER__FOR,REF_ID__FOR, REAS_CODE " + " FROM STOCK_TRANSFER  "
					+ " WHERE  TRAN_ID   = '" + tran_id + "' ";
			System.out.println("sql......................" + sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				tranDate = rs.getTimestamp("TRAN_DATE");
				siteCode = rs.getString("SITE_CODE");
				refSerFor = rs.getString("REF_SER__FOR");
				refIdFor = rs.getString("REF_ID__FOR");
				reasCode = rs.getString("REAS_CODE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select   tran_id,line_no,item_code,quantity,loc_code__fr,loc_code__to,lot_no__fr,  "
					+ " lot_no__to,   lot_sl__fr, lot_sl__to,  remarks, acct_code__cr,acct_code__dr, cctr_code__dr, cctr_code__cr "
					+ "from stock_transfer_det  where tran_id = '" + tran_id + "' ";
			System.out.println("sql............................." + sql);
			pstmt1 = conn.prepareStatement(sql);
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) {
				lineNo = rs1.getInt("line_no");
				itemCode = rs1.getString("item_code");
				effqty = rs1.getDouble("quantity");
				System.out.println("effqty.................." + effqty);
				locCodeFrom = rs1.getString("loc_code__fr");
				locCodeTo = rs1.getString("loc_code__to");
				lotNo = rs1.getString("lot_no__fr");
				lotNoTo = rs1.getString("lot_no__to");
				lotSl = rs1.getString("lot_sl__fr");
				lotSlTo = rs1.getString("lot_sl__to");
				remarks = rs1.getString("remarks");
				acctCodeCr = rs1.getString("acct_code__cr");
				acctCodeDr = rs1.getString("acct_code__dr");
				cctrCodeDr = rs1.getString("cctr_code__dr");
				cctrCodeCr = rs1.getString("cctr_code__cr");
				noArt = 0;
				if (effqty > 0) {
					qcCount = 0;
					sql = "select count(1) from 	 qc_order  where  site_code ='" + siteCode + "'  "
							+ " and item_code ='" + itemCode + "' " + " and lot_no ='" + lotNo + "' " + " and lot_sl ='"
							+ lotSl + "' " + " and status <> 'C' ";
					System.out.println("sql........" + sql);
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						qcCount = rs.getInt(1);
					}
					System.out.println("qcCount.........................." + qcCount);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (qcCount > 0) {
						sql = "update qc_order 	set loc_code = NULL,lot_sl = NULL,tran_id__xfrx = '" + tran_id + "' "
								+ " where  site_code = '" + siteCode + "' and  loc_code ='" + locCodeFrom + "' "
								+ " and	 item_code ='" + itemCode + "'  and lot_no ='" + lotNo + "'  and lot_sl ='"
								+ lotSl + "' " + " and	 status 	 <> 'C' ";
						System.out.println("sql........" + sql);
						pstmt = conn.prepareStatement(sql);
						upd = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
					} else {
						sql = "select count(1) from  qc_order  where  site_code ='" + siteCode + "'  "
								+ " and loc_code ='" + locCodeFrom + "'  and item_code ='" + itemCode + "' "
								+ " and lot_no ='" + lotNo + "' " + " and lot_sl is null " + " and status <> 'C' ";
						System.out.println("sql........" + sql);
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							qcCount = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (qcCount > 0) {
							sql = "update qc_order 	set set loc_code = NULL,tran_id__xfrx = '" + tran_id + "' "
									+ " where  site_code = '" + siteCode + "' and  loc_code ='" + locCodeFrom + "' "
									+ " and	 item_code ='" + itemCode + "'  and lot_no ='" + lotNo
									+ "'  lot_sl is null " + " and	 status 	 <> 'C' ";
							System.out.println("sql........" + sql);
							pstmt = conn.prepareStatement(sql);
							upd = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
						}
					} // else
				} // if(effqty > 0)
				sql = "Select quantity,(quantity - (case when alloc_qty is null then 0 else alloc_qty end)) qty,inv_stat,grade,	mfg_date, 	site_code__mfg,	potency_perc,	pack_code,	supp_code__mfg, unit,	"
						+ " rate, 	gross_rate, 	exp_date, 	gross_weight,tare_weight, 	net_weight,	supp_code__mfg, 	retest_date	, "
						+ " conv__qty_stduom,batch_no, acct_code__inv,cctr_code__inv	,pack_instr,dimension, 	unit__alt "
						+ " From 		stock Where 	item_code = '" + itemCode + "' and site_code = '" + siteCode
						+ "' 	And loc_code  ='" + locCodeFrom + "' " + " and lot_no    = '" + lotNo
						+ "'		And lot_sl    = '" + lotSl + "' ";
				System.out.println("sql............................" + sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					qtyorg = rs.getDouble("quantity");
					qty = rs.getDouble("qty");
					invstat = rs.getString("inv_stat");
					grade = rs.getString("grade");
					rate = rs.getDouble("rate");
					grossRate = rs.getDouble("gross_rate");
					mfgDate = rs.getTimestamp("mfg_date");
					expDate = rs.getTimestamp("exp_date");
					grossWeight = rs.getDouble("gross_weight");
					tareWeight = rs.getDouble("tare_weight");
					netWeight = rs.getDouble("net_weight");
					suppCodeMfg = rs.getString("supp_code__mfg");
					retestDate = rs.getTimestamp("retest_date");
					convQtyStduom = rs.getDouble("conv__qty_stduom");
					siteCodeMfg = rs.getString("site_code__mfg");
					batchNo = rs.getString("batch_no");
					acctCodeInv = rs.getString("acct_code__inv");
					cctrCodeInv = rs.getString("cctr_code__inv");
					packInstr = rs.getString("pack_instr");
					dimension = rs.getString("dimension");
					unitAlt = rs.getString("unit__alt");
					potencyPerc = rs.getDouble("potency_perc");
					packCode = rs.getString("pack_code");
					unit = rs.getString("unit");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (acctCodeDr == null || acctCodeDr.trim().length() == 0) {
					String acctCodeIN = "";
					String cctrCodeIN = "";
					String acctDetrType = finCommon.getFromAcctDetr(itemCode, "", "IN", conn);
					System.out.println("acctDetrType.....IN........" + acctDetrType);
					if (acctDetrType != null && acctDetrType.trim().length() > 0) {
						acctCodeIN = acctDetrType.substring(0, acctDetrType.indexOf(","));
						cctrCodeIN = acctDetrType.substring(acctDetrType.indexOf(",") + 1);
					}
					System.out.println("acctStr.....IN........" + acctCodeIN);
					System.out.println("acctStr.....IN........" + cctrCodeIN);
				}
				if (effqty == 0)
					continue;
				if (!invLink.equalsIgnoreCase("Y")) {
					qtyorg = qty;
				}
				sql = "select overiss from invstat where inv_stat = '" + invstat + "' ";
				System.out.println("sql........" + sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					overiss = rs.getString("overiss");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (effqty > qtyorg && (!overiss.equalsIgnoreCase("Y"))) {
					System.out.println("overiss...1111222333......................." + overiss);
					errCode = "VTOVERISS1";
					return (errCode);
				}
				itemSer = getItemSer(itemCode, siteCode, tranDate, "", "O", conn);
				System.out.println("itemSer........................." + itemSer);
				if (lotNoTo == null || lotNoTo.trim().length() == 0) {
					lotNoTo = "               ";
				}
				if (lotNo == null || lotNo.trim().length() == 0) {
					lotNo = "               ";
				}
				if (lotSlTo == null || lotSlTo.trim().length() == 0) {
					lotNoTo = "     ";
				}
				if (lotSl == null || lotSl.trim().length() == 0) {
					lotNo = "     ";
				}
				stkUpdMap.put("ref_ser__for", refSerFor);
				stkUpdMap.put("ref_id__for", refIdFor);
				stkUpdMap.put("tran_ser", "XFRX");
				stkUpdMap.put("tranid", tran_id);
				if (invLink.equalsIgnoreCase("Y")) {
					trantype = "ID";
				} else {
					trantype = "I";
				}
				stkUpdMap.put("tran_type", trantype);
				stkUpdMap.put("item_code", itemCode);
				stkUpdMap.put("site_code", siteCode);
				stkUpdMap.put("ref_ser__for", refSerFor);
				stkUpdMap.put("loc_code", locCodeFrom);
				stkUpdMap.put("lot_no", lotNo);
				stkUpdMap.put("lot_sl", lotSl);
				stkUpdMap.put("unit", unit);
				stkUpdMap.put("quantity", Double.toString(effqty));
				stkUpdMap.put("qty_stduom", Double.toString(effqty));
				stkUpdMap.put("tran_date", tranDate);
				stkUpdMap.put("acct_code__cr", acctCodeCr);
				stkUpdMap.put("cctr_code__cr", cctrCodeCr);
				stkUpdMap.put("remarks", remarks);

				String acctDetrType = finCommon.getAcctDetrTtype(itemCode, itemSer, "XFRX", " ", conn);
				System.out.println("acctDetrType.....XFRX........" + acctDetrType);
				if (acctDetrType != null && acctDetrType.trim().length() > 0) {
					acctCodeXFRX = acctDetrType.substring(0, acctDetrType.indexOf(","));
					cctrCodeXFRX = acctDetrType.substring(acctDetrType.indexOf(",") + 1);
				}
				System.out.println("acctStr.....XFRX........" + acctCodeXFRX);
				System.out.println("acctStr.....XFRX........" + cctrCodeXFRX);
				if (cctrCodeXFRX.trim().length() > 0) {
					if (acctCodeXFRX == null || acctCodeXFRX.trim().length() == 0) {
						invacct = finCommon.getFinparams("999999", "INVENTORY_ACCT", conn);
						trfacct = finCommon.getFinparams("999999", "INV_ACCT_TRF", conn);
						if (invacct.equalsIgnoreCase("Y") || trfacct.equalsIgnoreCase("Y")) {
							errCode = "VTTRFACT";
							return errCode;
						}
					}
				}
				stkUpdMap.put("acct_code__dr", acctCodeXFRX);
				if (cctrCodeXFRX == null || cctrCodeXFRX.trim().length() == 0) {
					stkUpdMap.put("cctr_code__dr", cctrCodeDr);
				} else {
					stkUpdMap.put("cctr_code__dr", cctrCodeXFRX);
				}
				stkUpdMap.put("line_no", "  1");
				stkUpdMap.put("rate", Double.toString(rate));
				stkUpdMap.put("gross_rate", Double.toString(grossRate));
				stkUpdMap.put("site_code_mfg", siteCodeMfg);
				stkUpdMap.put("potency_perc", Double.toString(potencyPerc));
				stkUpdMap.put("pack_code", packCode);
				stkUpdMap.put("mfg_date", mfgDate);
				stkUpdMap.put("exp_date", expDate);
				stkUpdMap.put("inv_stat", invstat);
				stkUpdMap.put("acct_code_inv", acctCodeInv);
				stkUpdMap.put("cctr_code_inv", cctrCodeInv);
				if (qtyorg != 0) {
					grossWeight = (grossWeight / qtyorg * effqty);
					grossWeight = getRequiredDecimal(grossWeight, 3);
					stkUpdMap.put("gross_weight", Double.toString(grossWeight));
					tareWeight = (tareWeight / qtyorg * effqty);
					tareWeight = getRequiredDecimal(tareWeight, 3);

					stkUpdMap.put("tare_weight", Double.toString(tareWeight));
					netWeight = (netWeight / qtyorg * effqty);
					netWeight = getRequiredDecimal(netWeight, 3);
					stkUpdMap.put("net_weight", Double.toString(netWeight));
				}
				stkUpdMap.put("supp_code__mfg", suppCodeMfg);
				stkUpdMap.put("retest_date", retestDate);
				stkUpdMap.put("grade", grade);
				stkUpdMap.put("conv__qty_stduom", Double.toString(convQtyStduom));
				stkUpdMap.put("batch_no", batchNo);
				stkUpdMap.put("pack_instr", packInstr);
				stkUpdMap.put("dimension", dimension);
				stkUpdMap.put("unit__alt", unitAlt);
				stkUpdMap.put("no_art", Integer.toString(noArt));
				StockUpdate stkUpd = new StockUpdate();
				errString = stkUpd.updateStock(stkUpdMap, xtraParams, conn);
				System.out.println("updateStock................" + errString);
				if (errString != null && errString.trim().length() > 0) {
					System.out.println("Returning Result " + errString);
				}

				sql = "select inv_stat from location where loc_code = '" + locCodeTo + "' ";
				System.out.println("sql........" + sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					invStat = rs.getString("inv_stat");
				} else {
					errCode = "VMLOC1";
					return errCode;

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				// Receipt
				stkUpdMap.put("tran_ser", "XFRX");
				stkUpdMap.put("tranid", tran_id);
				stkUpdMap.put("tran_type", "R");
				stkUpdMap.put("item_code", itemCode);
				stkUpdMap.put("site_code", siteCode);
				stkUpdMap.put("loc_code", locCodeTo);
				stkUpdMap.put("lot_no", lotNoTo);
				stkUpdMap.put("lot_sl", lotSlTo);
				stkUpdMap.put("inv_stat", invStat);
				stkUpdMap.put("quantity", Double.toString(effqty));
				stkUpdMap.put("qty_stduom", Double.toString(effqty));
				stkUpdMap.put("tran_date", tranDate);
				stkUpdMap.put("acct_code__cr", acctCodeXFRX);
				if (cctrCodeXFRX == null || cctrCodeXFRX.trim().length() == 0) {
					stkUpdMap.put("cctr_code__cr", cctrCodeCr);
				}
				stkUpdMap.put("cctr_code__cr", cctrCodeXFRX);
				stkUpdMap.put("acct_code_inv", acctCodeDr);
				stkUpdMap.put("cctr_code_inv", cctrCodeDr);
				stkUpdMap.put("ref_ser__for", refSerFor);
				stkUpdMap.put("ref_id__for", refIdFor);

				errString = stkUpd.updateStock(stkUpdMap, xtraParams, conn);
				System.out.println("updateStock................" + errString);
				if (errString != null && errString.trim().length() > 0) {
					System.out.println("Returning Result " + errString);
					return errString;
				}

				if ("Y".equalsIgnoreCase(invLink)) {
					// ls_errcode =
					// lnvo_fin.gbf_stk_transfer_post(lds_data.Describe("datawindow.data"))
					// Receipt side debit entry
					ls_inv_online = finCommon.getFinparams("999999", "INV_ACCT_TRF", conn);
					if (ls_inv_online.equalsIgnoreCase("NULLFOUND")) {
						// errString =
						// itmDBAccessEJB.getErrorString("","VTFINPARM","","",conn);
						return "VTFINPARM";
					}
					ls_inv_online = postOnLine.substring(0, 1);
					if ("Y_N".indexOf(ls_inv_online) == -1) {
						// errString =
						// itmDBAccessEJB.getErrorString("","VTFINPARM1","","",conn);
						return "VTFINPARM1";
					}
					ls_inv_online = ls_inv_online.trim();
					if (!"Y".equalsIgnoreCase(ls_inv_online)) {
						return "";
					}
					sql = "select fin_entity from site  where site_code= '" + siteCode + "'  ";
					System.out.println("sql........................" + sql);
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						finEntity = rs.getString("fin_entity");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select curr_code from finent where fin_entity= '" + finEntity + "'  ";
					System.out.println("sql........" + sql);
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						currCode = rs.getString("fin_entity");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// for base currency exchange rate will be 1.
					// lc_exchrate = 1;
					sql = "select rate from stock where item_code= '" + itemCode + "'  " + " and site_code = '"
							+ siteCode + "' " + " and loc_code = '" + siteCode + "' " + " and lot_no = '" + siteCode
							+ "' " + " and lot_sl = '" + siteCode + "' ";
					System.out.println("sql........" + sql);
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble("rate");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					amt = Math.abs(effqty) * rate;

					glTraceMap.put("tran_date", tranDate);
					glTraceMap.put("eff_date", tranDate);
					glTraceMap.put("fin_entity", finEntity);
					glTraceMap.put("site_code", siteCode);
					glTraceMap.put("sundry_type", "O");
					glTraceMap.put("acct_code", acctCodeDr);
					glTraceMap.put("cctr_code", cctrCodeDr);
					glTraceMap.put("curr_code", currCode);
					glTraceMap.put("exch_rate", Double.toString(1));
					glTraceMap.put("dr_amt", Double.toString(amt));
					glTraceMap.put("cr_amt", Double.toString(0));
					glTraceMap.put("ref_type", " ");
					glTraceMap.put("ref_ser", "");
					glTraceMap.put("ref_id", "");
					glTraceMap.put("remarks", remarks);

					errString = finCommon.glTraceUpdate(glTraceMap, conn);
					if (errString != null && errString.trim().length() > 0) {
						System.out.println("Returning Result " + errString);
						return errString;
					}
					// Receipt side credit entry
					acctDetrType = finCommon.getAcctDetrTtype(itemCode, " ", "XFRX", " ", conn);
					System.out.println("acctDetrType.....XFRX........" + acctDetrType);
					if (acctDetrType != null && acctDetrType.trim().length() > 0) {
						macct = acctDetrType.substring(0, acctDetrType.indexOf(","));
						mcctr = acctDetrType.substring(acctDetrType.indexOf(",") + 1);
					}
					if (mcctr == null || mcctr.trim().length() == 0) {
						return "VTSTTRF";
					}
					System.out.println("macct.....XFRX........" + macct);
					System.out.println("mcctr.....XFRX........" + mcctr);

					glTraceMap.put("tran_date", tranDate);
					glTraceMap.put("eff_date", tranDate);
					glTraceMap.put("fin_entity", finEntity);
					glTraceMap.put("site_code", siteCode);
					glTraceMap.put("sundry_type", "O");
					glTraceMap.put("acct_code", macct);
					glTraceMap.put("cctr_code", mcctr);
					glTraceMap.put("curr_code", currCode);
					glTraceMap.put("exch_rate", Double.toString(1));
					glTraceMap.put("dr_amt", Double.toString(0));
					glTraceMap.put("cr_amt", Double.toString(amt));
					glTraceMap.put("ref_type", " ");
					glTraceMap.put("ref_ser", "");
					glTraceMap.put("ref_id", "");
					glTraceMap.put("remarks", remarks);
					errString = finCommon.glTraceUpdate(glTraceMap, conn);
					if (errString != null && errString.trim().length() > 0) {
						System.out.println("Returning Result " + errString);
						return errString;
					}

					// Issue side debit entry
					glTraceMap.put("tran_date", tranDate);
					glTraceMap.put("eff_date", tranDate);
					glTraceMap.put("fin_entity", finEntity);
					glTraceMap.put("site_code", siteCode);
					glTraceMap.put("sundry_type", "O");
					glTraceMap.put("acct_code", macct);
					if (mcctr == null || mcctr.trim().length() == 0) {
						mcctr = cctrCodeDr;
					}
					glTraceMap.put("cctr_code", mcctr);
					glTraceMap.put("curr_code", currCode);
					glTraceMap.put("exch_rate", Double.toString(1));
					glTraceMap.put("dr_amt", Double.toString(amt));
					glTraceMap.put("cr_amt", Double.toString(0));
					glTraceMap.put("ref_type", " ");
					glTraceMap.put("ref_ser", "");
					glTraceMap.put("ref_id", "");
					glTraceMap.put("remarks", remarks);
					errString = finCommon.glTraceUpdate(glTraceMap, conn);
					if (errString != null && errString.trim().length() > 0) {
						System.out.println("Returning Result " + errString);
						return errString;
					}

					// Issue side credit entry
					glTraceMap.put("tran_date", tranDate);
					glTraceMap.put("eff_date", tranDate);
					glTraceMap.put("fin_entity", finEntity);
					glTraceMap.put("site_code", siteCode);
					glTraceMap.put("sundry_type", "O");
					glTraceMap.put("acct_code", acctCodeCr);
					glTraceMap.put("cctr_code", cctrCodeCr);
					glTraceMap.put("curr_code", currCode);
					glTraceMap.put("exch_rate", Double.toString(1));
					glTraceMap.put("dr_amt", Double.toString(0));
					glTraceMap.put("cr_amt", Double.toString(amt));
					glTraceMap.put("ref_type", " ");
					glTraceMap.put("ref_ser", "");
					glTraceMap.put("ref_id", "");
					glTraceMap.put("remarks", remarks);
					errString = finCommon.glTraceUpdate(glTraceMap, conn);
					if (errString != null && errString.trim().length() > 0) {
						System.out.println("Returning Result " + errString);
						return errString;
					}
					errString = finCommon.checkGlTranDrCr("XFRX", "", conn); // /??????????????????

				}
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
		} catch (Exception e) {
			System.out.println("Exception...[stockTransferMulti] " + sql + e.getMessage());
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
				if (rs1 != null) {
					rs1.close();
					rs1 = null;
				}
				if (pstmt1 != null) {
					pstmt1.close();
					pstmt1 = null;
				}
			} catch (Exception e) {
			}

		}
		return errString;
	}

	/**
	 * Gets the integral quantity
	 * 
	 * @param lsItemCode
	 * @param lsTranType
	 * @param lcQtyOrder
	 * @param lsSiteCode
	 * @param conn
	 * @return
	 */
	public String gbfSetIntegralQty(String lsItemCode, String lsTranType, double lcQtyOrder, String lsSiteCode,
			Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int llCount = 0;

		String lsCheckIntegralQty = null;
		double integralQty = 0.0;

		double lcIntQty = 0.0, lcIntQtySite = 0.0;

		if (lsItemCode == null) {
			return "";
		}

		try {
			String sql = "Select count(1) cnt From item Where  item_code = '" + lsItemCode.trim() + "'";

			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				llCount = rs.getInt("cnt");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (llCount == 0) {
				return "";
			}

			sql = "Select Check_integral_qty ls_check_integral_qty From distorder_type Where tran_type = '"
					+ lsTranType.trim() + "'";

			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				lsCheckIntegralQty = rs.getString("ls_check_integral_qty");
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (lsCheckIntegralQty == null) {
				lsCheckIntegralQty = "Y";
			}

			if ("Y".equalsIgnoreCase(lsCheckIntegralQty)) {
				String qtySql = "Select integral_qty lc_int_qty_site From siteitem " + " Where site_code = '"
						+ lsSiteCode.trim() + "'" + " 	 and item_code = '" + lsItemCode.trim() + "'";

				pstmt = conn.prepareStatement(qtySql);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					lcIntQtySite = rs.getDouble("lc_int_qty_site");
				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				String itemSql = "select integral_qty lc_int_qty from item " + "	where item_code = '"
						+ lsItemCode.trim() + "'";

				pstmt = conn.prepareStatement(itemSql);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					integralQty = rs.getDouble("lc_int_qty");
				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (lcIntQtySite == 0.0 && lcIntQty != 0.0) {
					lcIntQtySite = lcIntQty;
				}

				if (lcIntQtySite > 0.0) {
					if ((lcQtyOrder % lcIntQtySite) > 0) {
						return "VTINTQTY";
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "SQLERROR";
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
			}
		}

		return "";
	}

	// migration of method getTacClass on 07/05/09
	/**
	 * Gets tax_class as String
	 * 
	 * @param supOrCust
	 *            Supplier or customer (C/S)
	 * @param suppCode
	 *            The supplier or customer code
	 * @param itemCode
	 *            The item code
	 * @param siteCodeShip
	 *            The site code shippment
	 * @param conn
	 *            JDBC Database connection
	 * @return Teh tax class
	 * @exception ITMException
	 * @see
	 */
	public String getTaxClass(String supOrCust, String suppCode, String itemCode, String siteCodeShip, Connection conn)
			throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", taxClass = "";
		try {

			if ("S".equalsIgnoreCase(supOrCust)) {
				sql = "select tax_class " + "	from supplieritem " + " where supp_code = ?  " + "and item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					taxClass = rs.getString("tax_class") != null ? rs.getString("tax_class") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}
			if ("C".equalsIgnoreCase(supOrCust)) {
				sql = "select tax_class " + "	from customeritem " + " where cust_code = ?  " + "and item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxClass = rs.getString("tax_class") != null ? rs.getString("tax_class") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}
			if (taxClass != null && taxClass.trim().length() > 0) {
				return taxClass;
			}
			if ("S".equalsIgnoreCase(supOrCust)) {
				sql = "select tax_class " + "	from supplier " + " where supp_code = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxClass = rs.getString("tax_class") != null ? rs.getString("tax_class") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			} else {
				sql = "	select tax_class  " + "	from site_customer " + "	where site_code = ?  and cust_code= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeShip);
				pstmt.setString(2, suppCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxClass = rs.getString("tax_class") != null ? rs.getString("tax_class") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (taxClass == null || taxClass.trim().length() == 0) {
					sql = "	select tax_class " + "	from customer " + "	where cust_code = ?   ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, suppCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxClass = rs.getString("tax_class") != null ? rs.getString("tax_class") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

			}
			if (itemCode != null && itemCode.trim().length() > 0
					&& (taxClass == null || taxClass.trim().length() == 0)) {
				sql = "	select tax_class " + "	from item " + "	where item_code = ?   ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxClass = rs.getString("tax_class") != null ? rs.getString("tax_class") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
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
			} catch (Exception e) {
			}
		}
		return taxClass;
	}

	// end of code

	// migration of method getTaxEnv() on 07/05/09
	// getTaxENV
	/**
	 * Gets tax_env as String
	 * 
	 * @param stationFrom
	 *            Station code from
	 * @param stationTo
	 *            Station code to
	 * @param taxChap
	 *            Tax chapter
	 * @param taxClass
	 *            Tax class
	 * @param siteCodeShip
	 *            Site code of shippment
	 * @param conn
	 *            JDBC Database connection
	 * @return The tax environment
	 * @exception ITMException
	 * @see
	 */
	public String getTaxEnv(String stationFrom, String stationTo, String taxChap, String taxClass, String siteCodeShip,
			Connection conn) throws ITMException {
		SimpleDateFormat sdf = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", taxEnv = "", taxEnvPrefix = "", stateCodeFrom = "", stateCodeTo = "", countCodeFrom = "",
				countCodeTo = "";
		try {
			System.out.println("stationFrom[" + stationFrom);
			System.out.println("stationTo[" + stationTo);
			System.out.println("taxChap[" + taxChap);
			System.out.println("taxClass[" + taxClass);
			System.out.println("siteCodeShip[" + siteCodeShip);
			E12GenericUtility genericUtility = new E12GenericUtility();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			sql = "select state_code  from station where stan_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, stationFrom);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				stateCodeFrom = rs.getString("state_code") != null ? rs.getString("state_code") : "";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select state_code  from station where stan_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, stationTo);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				stateCodeTo = rs.getString("state_code") != null ? rs.getString("state_code") : "";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "SELECT tax_env " + "	FROM tenvstan " + "WHERE stan_code__fr = ? and " + " stan_code__to = ? and "
					+ " tax_class     = ?  and " + " tax_chap      = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, stationFrom);
			pstmt.setString(2, stationTo);
			pstmt.setString(3, taxClass);
			pstmt.setString(4, taxChap);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (taxEnv == null || taxEnv.trim().length() == 0) {
				sql = "SELECT tax_env " + "	FROM tenvstan " + "WHERE stan_code__fr = ? and " + " stan_code__to = ? and "
						+ " tax_class is null and " + " tax_chap      = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, stationFrom);
				pstmt.setString(2, stationTo);
				pstmt.setString(3, taxChap);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env " + "	FROM tenvstan " + "WHERE stan_code__fr = ? and "
							+ " stan_code__to = ? and " + " tax_class =? and " + " tax_chap   is null   ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stationFrom);
					pstmt.setString(2, stationTo);
					pstmt.setString(3, taxClass);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env " + "	FROM tenvstan " + "WHERE stan_code__fr = ? and "
							+ " stan_code__to = ? and " + " tax_class is null  and " + " tax_chap  is null  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stationFrom);
					pstmt.setString(2, stationTo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
			} // end of taxClass
			// if environment is still not found check on the basis of state
			// code
			if (taxEnv == null || taxEnv.trim().length() == 0) {
				// 03/03/10 manoharan
				/*
				 * sql="select state_code  from station where stan_code = ? " ; pstmt =
				 * conn.prepareStatement(sql); pstmt.setString(1,stationFrom); rs =
				 * pstmt.executeQuery(); if(rs.next()) { stateCodeFrom
				 * =rs.getString("state_code") != null ? rs.getString("state_code") :""; }
				 * rs.close(); rs=null; pstmt.close(); pstmt=null;
				 * sql="select state_code  from station where stan_code = ? " ; pstmt =
				 * conn.prepareStatement(sql); pstmt.setString(1,stationFrom); rs =
				 * pstmt.executeQuery(); if(rs.next()) { stateCodeTo =
				 * rs.getString("state_code") != null ? rs.getString("state_code") :""; }
				 * rs.close(); rs=null; pstmt.close(); pstmt=null;
				 */

				sql = "SELECT tax_env " + "	FROM tenvstan " + "	WHERE state_code__fr = ? and  " // state code from
						+ "	state_code__to = ? and " + "	stan_code__fr = '  ' and " + "	stan_code__to = '  ' and "
						+ "	tax_class     = ? and " + "	tax_chap      =? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, stateCodeFrom);
				pstmt.setString(2, stateCodeTo);
				pstmt.setString(3, taxClass);
				pstmt.setString(4, taxChap);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env " + "	FROM tenvstan " + "	WHERE state_code__fr = ? and "
							+ "	state_code__to = ? and " + "	stan_code__fr = '     ' and "
							+ "	stan_code__to = '     ' and " + "	tax_class = '     ' and " + "	tax_chap      = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stateCodeFrom);
					pstmt.setString(2, stateCodeTo);
					pstmt.setString(3, taxChap);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env " + "	FROM tenvstan " + "	WHERE state_code__fr = ? and "
							+ "	state_code__to = ? and " + "	stan_code__fr = '     ' and "
							+ "	stan_code__to = '     ' and " + "	tax_class = ? and " + "	tax_chap      ='    '";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stateCodeFrom);
					pstmt.setString(2, stateCodeTo);
					pstmt.setString(3, taxClass);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env        FROM tenvstan " + "	WHERE state_code__fr = ?  "
							+ "	and      state_code__to = ?  " + "	and    stan_code__fr = '     ' "
							+ "	and     stan_code__to = '     ' " + "	and     tax_class = '   '  " + "	and  "
							+ "	tax_chap  ='    '  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stateCodeFrom);
					pstmt.setString(2, stateCodeTo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") == null ? "" : rs.getString("tax_env");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
			}
			// added on 28-feb-2019 by nandkumar gadkari -if environment is still not found
			// check on the basis of country code---start----------------
			if (taxEnv == null || taxEnv.trim().length() == 0) {
				sql = "select count_code  from state where state_code  = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, stateCodeFrom);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					countCodeFrom = rs.getString("count_code") != null ? rs.getString("count_code") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql = "select count_code  from state where state_code  = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, stateCodeTo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					countCodeTo = rs.getString("count_code") != null ? rs.getString("count_code") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = "SELECT tax_env " + "	FROM tenvstan " + "	WHERE state_code__fr = '  ' and  " // state code from
						+ "	state_code__to = '  ' and " + "	stan_code__fr = '  ' and " + "	stan_code__to = '  ' and "
						+ " count_code__fr = ? and " + " count_code__to = ? and " + "	tax_class     = ? and "
						+ "	tax_chap      =? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, countCodeFrom);
				pstmt.setString(2, countCodeTo);
				pstmt.setString(3, taxClass);
				pstmt.setString(4, taxChap);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env " + "	FROM tenvstan " + "	WHERE state_code__fr ='     ' and "
							+ "	state_code__to = '     ' and " + "	stan_code__fr = '     ' and "
							+ "	stan_code__to = '     ' and " + " count_code__fr = ? and " + " count_code__to = ? and "
							+ "	tax_class = '     ' and " + "	tax_chap      = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, countCodeFrom);
					pstmt.setString(2, countCodeTo);
					pstmt.setString(3, taxChap);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env " + "	FROM tenvstan " + "	WHERE state_code__fr = '     ' and "
							+ "	state_code__to = '     ' and " + "	stan_code__fr = '     ' and "
							+ "	stan_code__to = '     ' and " + " count_code__fr = ? and " + " count_code__to = ? and "
							+ "	tax_class = ? and " + "	tax_chap      ='    '";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, countCodeFrom);
					pstmt.setString(2, countCodeTo);
					pstmt.setString(3, taxClass);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") != null ? rs.getString("tax_env") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (taxEnv == null || taxEnv.trim().length() == 0) {
					sql = "SELECT tax_env        FROM tenvstan " + "	WHERE state_code__fr = '     '  "
							+ "	and      state_code__to = '     '  " + "	and    stan_code__fr = '     ' "
							+ "	and     stan_code__to = '     ' " + " and count_code__fr = ?  "
							+ " and count_code__to = ?  " + "	and     tax_class = '   '  " + "	and  "
							+ "	tax_chap  ='    '  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, countCodeFrom);
					pstmt.setString(2, countCodeTo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxEnv = rs.getString("tax_env") == null ? "" : rs.getString("tax_env");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}
			}
			// added on 28-feb-2019 by nandkumar gadkari -if environment is still not found
			// check on the basis of country code---end----------------

			String errorString = getCheckTaxEnvStatus(taxEnv, currDateTs, "", conn);
			if (errorString != null && errorString.trim().length() > 0) {
				taxEnv = "";
			}
			sql = " select tax_env_prefix  from site where site_code= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeShip);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				taxEnvPrefix = rs.getString("tax_env_prefix") != null ? rs.getString("tax_env_prefix") : "";
			}
			if (taxEnvPrefix != null && taxEnvPrefix.trim().length() > 0) {
				taxEnv = taxEnv != null ? taxEnv.trim() : "";
				taxEnv = taxEnv + taxEnvPrefix;
				System.out.println("return taxEnv[" + taxEnv + "]");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
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
			} catch (Exception e) {
			}
		}
		return taxEnv;
	}

	// end of code

	// migration of method getCheckTaxEnvStatus() on 07/05/09

	/**
	 * Gets status of tax environment
	 * 
	 * @param taxEnv
	 *            Tax environment
	 * @param currDateTs
	 *            Current date
	 * @param busiType
	 *            Tax environemtn applicable for the business process such as
	 *            sales-S,purchase-P,expense-E, etc' ;
	 * @param conn
	 *            JDBC Database connection
	 * @return check the tax environment status and return error code if closed
	 * @exception ITMException
	 * @see
	 */
	// Pavan R 17sep19 start[Changed below method signature to validate tax
	// environment in all transaction]
	// public String getCheckTaxEnvStatus(String taxEnv, Timestamp
	// currDateTs,Connection conn) throws ITMException {
	public String getCheckTaxEnvStatus(String taxEnv, Timestamp currDateTs, String busiType, Connection conn)
			throws ITMException {
		String errorResturn = "", status = "", busiProcUse = "";
		String sql = "";
		boolean errFlag = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			// java.sql.Timestamp fromDateTms =
			// Timestamp.valueOf(genericUtility.getValidDateString(currDateTs.toString(),
			// genericUtility.getApplDateFormat(),
			// genericUtility.getDBDateFormat()) + " 00:00:00.0");
			/*
			 * sql =
			 * "select (case when \"STATUS\" is null then 'A' else \"STATUS\" end) STATUS "
			 * + "	from   taxenv " + "	where  tax_env      = ? " +
			 * "	and    status_date  <= ? ";
			 */
			sql = "SELECT (CASE WHEN STATUS IS NULL THEN 'A' ELSE STATUS END) FROM TAXENV "
					+ "WHERE TAX_ENV = ? AND STATUS_DATE  <= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, taxEnv);
			pstmt.setTimestamp(2, currDateTs);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				status = rs.getString(1) != null ? rs.getString(1) : "";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if ("C".equalsIgnoreCase(status)) {
				errorResturn = "VTTAXENVCL";
				return errorResturn; // Pavan R 31aug19 start
			}
			// Pavan R 31aug19 start[to validate tax environment in all transaction]
			if (busiType != null && busiType.trim().length() > 0)// Pavan R 18oct19 end[to ignore valdaiton if type is
				// null]
			{
				sql = "select busi_proc_use from taxenv where tax_env = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, taxEnv);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					busiProcUse = rs.getString("busi_proc_use") != null ? rs.getString("busi_proc_use") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("GetCheckTaxEnvStatus::>>busiProcUse[" + busiProcUse + "]");
				String busiProcUseArr[] = busiProcUse.split(",");
				if (busiProcUseArr.length > 0) {
					for (int i = 0; i < busiProcUseArr.length; i++) {
						if (busiType.trim().equalsIgnoreCase(busiProcUseArr[i].trim())) {
							errFlag = true;
						}
					}
				}
				if (!errFlag) {
					errorResturn = "VTTAXENVVL";
					return errorResturn;
				}
			} // end of if(busiType != null)
			// Pavan R 31aug19 end[to validate tax environment in all transaction]
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errorResturn;
	}
	// end of code

	// migration of method getTaxChap() on 07/05/09
	/**
	 * Returns blank if not found else value of column from parent node
	 * 
	 * @param currColumn
	 *            column name
	 * @param dom
	 *            Document
	 * @exception ITMException
	 * @see
	 */
	public String getParentColumnValue(String currColumn, Document dom, String formNo) throws ITMException {

		NodeList parentNodeList = null;
		Node parentNode = null;
		String Value = "";
		try {
			E12GenericUtility genericUtility = new E12GenericUtility();
			parentNodeList = dom.getElementsByTagName("Detail" + formNo);
			int childNodeListLength = parentNodeList.getLength();
			for (int ctr = 0; ctr < childNodeListLength; ctr++) {
				parentNode = parentNodeList.item(ctr);
				Value = genericUtility.getColumnValueFromNode(currColumn, parentNode);
				break;

			} // end for
		} // END TRY
		catch (Exception e) {
			System.out.println("DistCommon::getParentColumnValue::Exception ::" + e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return Value;
	} // get taxchap

	/**
	 * Gets tax_chap as String
	 * 
	 * @param itemCode
	 *            Item Code
	 * @param suppOrCustCode
	 *            Flag to indicate customer or supplier
	 * @param custCode
	 *            Customer Code
	 * @param siteCodeShip
	 *            Shipping site code
	 * @param conn
	 *            JDBC Database connection
	 * @return check the tax chanpter status and return error code if closed
	 * @exception ITMException
	 * @see
	 */
	public String getTaxChap(String itemCode, String itemSer, String suppOrCustCode, String custCode,
			String siteCodeShip, Connection conn) throws ITMException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", taxClass = "", taxChap = "";
		try {
			if ("S".equalsIgnoreCase(suppOrCustCode)) {
				// Modified by Anjali R. on [13/02/2019][Wrong column get][Start]
				/*
				 * sql = "select tax_class " + "	from supplieritem " +
				 * " where supp_code = ?  " + "and item_code = ? ";
				 */
				sql = "select tax_chap	from supplieritem " + " where supp_code = ?  " + "and item_code = ? ";
				// Modified by Anjali R. on [13/02/2019][Wrong column get][End]
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					// Modified by Anjali R. on [13/02/2019][Wrong column get][Start]
					/*
					 * taxClass = rs.getString("tax_class") != null ? rs .getString("tax_class") :
					 * "";
					 */
					taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
					// Modified by Anjali R. on [13/02/2019][Wrong column get][End]
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			} else if ("C".equalsIgnoreCase(suppOrCustCode)) {
				sql = "select tax_chap " + "	from customeritem " + " where cust_code = ?  " + "and item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}
			// Modified by Anjali R. on [13/02/2019][Wrong variable check for tax
			// chapter][Start]
			/*
			 * if (taxClass != null && taxClass.trim().length() > 0) { return taxClass; }
			 */
			if (taxChap != null && taxChap.trim().length() > 0) {
				return taxChap;
			}
			// Modified by Anjali R. on [13/02/2019][Wrong variable check for tax
			// chapter][End]
			sql = " select tax_chap " + "	from siteitem " + "	where site_code = ? " + "	and item_code =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeShip);
			pstmt.setString(2, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (taxChap == null || taxChap.trim().length() == 0) {
				sql = "select tax_chap  from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (taxChap == null || taxChap.trim().length() == 0) {
					sql = "select tax_chap  from itemser where item_ser = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (taxChap == null || taxChap.trim().length() == 0) {
					if ("S".equalsIgnoreCase(suppOrCustCode)) {
						sql = "select tax_chap  from supplier where supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} else {
						sql = "select tax_chap  from customer where cust_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							taxChap = rs.getString("tax_chap") != null ? rs.getString("tax_chap") : "";
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
			}
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
			} catch (Exception e) {
			}
		}
		return taxChap;
	}

	/**
	 * Calculates and get the expiry date
	 * 
	 * @param date
	 *            Date from which expiry date to be calculated
	 * @param shelfLife
	 *            Shelf life in months
	 * @return expiry date
	 * @exception Exception
	 * @see
	 */
	public java.sql.Timestamp CalcExpiry(java.sql.Timestamp date, double shelfLife) throws Exception {
		UtilMethods utilMethods = UtilMethods.getInstance();
		java.sql.Timestamp expDate = null;
		int lastDay = 0, iShelfLife = 0;
		iShelfLife = (int) shelfLife;
		if (iShelfLife < 0) {
			iShelfLife = iShelfLife + 1;
		} else {
			iShelfLife = iShelfLife - 1;
		}
		expDate = utilMethods.AddMonths(date, iShelfLife);
		Calendar cal = Calendar.getInstance();
		cal.setTime(expDate);
		/*lastDay = cal.getMaximum(Calendar.DAY_OF_MONTH);*/ //Commented by Mukesh Chauhan on 15/01/20
		lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);//Added by Mukesh Chauhan on 15/01/20
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		java.util.Date newDate = cal.getTime();
		SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
		expDate = java.sql.Timestamp.valueOf(sdt.format(newDate) + " 00:00:00.000");
		utilMethods = null;
		return expDate;
	}

	// added by cpatil on 10/oct/12 start

	/**
	 * Gets the rate as double
	 * 
	 * @param priceList
	 *            The price list for which type is to be identified
	 * @param trDate
	 *            The date on which the price list to be a valid
	 * @param itemCode
	 *            The item code for which the rate is to be obtained
	 * @param aLotNo
	 *            The batch/lot number
	 * @param conn
	 *            JDBC Database connection
	 * @return the rate as double
	 * @exception ITMException
	 * @see
	 */
	public double pickRate(String priceList, String trDate, String itemCode, String aLotNo, Connection conn)
			throws ITMException, SQLException {
		System.out.println(
				"@@@@@--------------[pickRate]--[(String priceList,String trDate,String itemCode,String aLotNo,Connection conn)]-called------------------------------");

		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		java.sql.Timestamp tranDate = null;
		String sql = "";
		String priceListParent = "";
		String siteCode = "";
		String locCode = "";
		String lotNo = "";
		String lotSl = "";
		String listType = "";
		double rate = 0.0;

		try {
			sql = " select list_type from pricelist_mst where price_list = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			System.out.println("sql:" + sql);
			if (rs.next()) {
				listType = rs.getString("list_type");
				System.out.println("8355 priceList[" + priceList + "] listType:[" + listType + "]");

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");

			if (listType.equalsIgnoreCase("I")) // replace listType instead of
				// list by cpatil on 10-oct-12
			{
				if (aLotNo.indexOf("~t") > 0) {
					String MulStr[] = aLotNo.split("~t");
					siteCode = MulStr[0];
					locCode = MulStr[1];
					lotNo = MulStr[2];
					lotSl = MulStr[3];

				}
			} else {
				lotNo = aLotNo;
			}
		} catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
		}

		if (listType.trim().equals("L")) // replace listType instead of list by
			// cpatil on 10-oct-12
		{
			rate = 0;
			System.out.println("Inside type ::-<L>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					System.out.println("sql::" + sql);
					System.out.println("8410 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
					rate = rs.getDouble(1);
					System.out.println("rate:[" + rate + "]");
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} else {

					try {
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						// + "AND LIST_TYPE = 'L'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						System.out.println("sql:" + sql);
						if (rs.next()) {
							priceListParent = rs.getString(1) == null ? "" : rs.getString(1);
							if (priceListParent.trim().length() > 0) {
								try {
									sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent
											+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
											+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, listType);
									pstmt2.setTimestamp(2, tranDate);
									pstmt2.setTimestamp(3, tranDate);
									System.out.println("sql:" + sql);
									rs2 = pstmt2.executeQuery();
									System.out.println("8452 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										System.out.println("rate:[" + rate + "]");
										rs2.close();
										pstmt2.close();
										rs2 = null;
										pstmt2 = null;
									} else {
										rs2.close();
										pstmt2.close();
										rs2 = null;
										pstmt2 = null;
										return -1;
									}

									if (rate > 0) {
										priceList = priceListParent;
									} else {
										priceList = priceListParent;
										priceListParent = "";
									}

								} catch (Exception e) {

									e.printStackTrace();
									throw new ITMException(e);
								}
							}

						}
						// 25-dec-15 Commented by saurabh
						if (rs != null) // && !rs.isClosed()) isClosed() is
							// giving problem
						{
							rs.close();
							rs = null;
						}
						if (pstmt != null) // && !pstmt.isClosed()) isClosed()
							// is giving problem
						{
							pstmt.close();
							pstmt = null;
						}

					} catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}
				// 25-dec-15 Commented by saurabh
				if (rs != null) // && !rs.isClosed()) isClosed() is giving
					// problem
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) // && !pstmt.isClosed()) isClosed() is giving
					// problem
				{
					pstmt.close();
					pstmt = null;
				}

			} catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		if (listType.trim().equals("F")) // replace listType instead of list by
			// cpatil on 10-oct-12
		{
			System.out.println("Inside type ::-<F>-::");
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("sql:" + sql);
				System.out.println("8549 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("rate:" + rate + "]");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {

					try {
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";

						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						System.out.println("sql:" + sql);
						System.out.println("8574 priceList[" + priceList + "]");
						if (rs.next()) {
							priceListParent = rs.getString(1);
							System.out.println("@@@" + priceListParent);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, listType);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("sql:" + sql);
								System.out
								.println("8605 priceList[" + priceListParent + "] itemCode:[" + itemCode + "]");
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									System.out.println("rate:" + rate);
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
								} else {
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
									return -1;
								}
							} catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}
				// 25-dec-15 Commented by saurabh
				if (rs != null) // && !rs.isClosed()) isClosed() is giving
					// problem
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) // && !pstmt.isClosed()) isClosed() is giving
					// problem
				{
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		if (listType.trim().equals("D")) // DISPATCH // replace listType instead
			// of list by cpatil on 10-oct-12
		{
			rate = 0;
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("sql:" + sql);
				System.out.println("8676 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("rate:" + rate);
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} else {

					try {
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						System.out.println("sql:" + sql);
						System.out.println("8700 priceList[" + priceList + "]");
						if (rs.next()) {
							priceListParent = rs.getString(1);
							System.out.println("The priceListParent is .... " + priceListParent);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0)// 1
						{
							try {
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, listType);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("sql:" + sql);
								System.out.println(
										"8731 priceListParent[" + priceListParent + "] itemCode:[" + itemCode + "]");
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									System.out.println("8735rate:" + rate);
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
								} else {

									try {
										rs2.close();
										pstmt.close();
										rs2 = null;
										pstmt = null;
										sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
												+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo
												+ "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, listType);
										pstmt.setTimestamp(2, tranDate);
										pstmt.setTimestamp(3, tranDate);
										rs2 = pstmt.executeQuery();
										System.out.println("sql:" + sql);
										System.out.println(
												"8764 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											System.out.println("8768rate" + rate);
											rs2.close();
											pstmt.close();
											rs2 = null;
											pstmt = null;

										} else {
											try {
												rs2.close();
												pstmt.close();
												rs2 = null;
												pstmt = null;
												sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
														+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
												pstmt = conn.prepareStatement(sql);
												rs2 = pstmt.executeQuery();
												System.out.println("sql:" + sql);
												System.out.println("8788 priceList[" + priceList + "]");
												if (rs2.next()) {
													priceListParent = rs2.getString(1);
												}
												rs2.close();
												pstmt.close();
												rs2 = null;
												pstmt = null;
												if ((priceListParent == null)
														|| (priceListParent.trim().length() == 0)) {
													priceListParent = "";
													return -1;
												}
												if (priceListParent.trim().length() > 0) {
													try {
														sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
																+ priceListParent + "' " + "AND ITEM_CODE = '"
																+ itemCode + "' " + "AND LIST_TYPE = ? "
																+ "AND LOT_NO__FROM <= '" + lotNo + "' "
																+ "AND LOT_NO__TO >= '" + lotNo + "' "
																+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, listType);
														pstmt.setTimestamp(2, tranDate);
														pstmt.setTimestamp(3, tranDate);
														rs3 = pstmt.executeQuery();
														System.out.println("sql:" + sql);
														System.out.println("8820 priceListParent[" + priceListParent
																+ "] itemCode:[" + itemCode + "]");
														if (rs3.next()) {
															rate = rs3.getDouble(1);
															System.out.println("8824rate" + rate);
															rs3.close();
															pstmt.close();
															rs3 = null;
															pstmt = null;
														} else {
															rs3.close();
															pstmt.close();
															rs3 = null;
															pstmt = null;
															return -1;
														}
													} catch (Exception e) {
														e.printStackTrace();
														throw new ITMException(e);
													}
												}
											} catch (Exception e) {
												e.printStackTrace();
												throw new ITMException(e);
											}
										}

									} catch (Exception e) {
										e.printStackTrace();
										throw new ITMException(e);
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
								throw new ITMException(e);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						throw new ITMException(e);
					}
				}
				// 25-dec-15 Commented by saurabh
				if (rs != null)// && !rs.isClosed()) isClosed() is giving
					// problem
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)// && !pstmt.isClosed()) isClosed() is giving
					// problem
				{
					pstmt.clearParameters();
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		if (listType.trim().equals("B"))// BATCH PRICE // replace listType
			// instead of list by cpatil on
			// 10-oct-12

		{
			rate = 0;
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND LOT_NO__FROM <= '" + lotNo + "' "
						+ "AND LOT_NO__TO >= '" + lotNo + "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("sql:" + sql);
				System.out.println("8916 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("8920rate" + rate);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {
					try {
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						System.out.println("sql:" + sql);
						System.out.println("8938 priceList[" + priceList + "]");
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
								priceListParent = "";
							}
							if (priceListParent.trim().length() > 0) {
								try {
									sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent
											+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
											+ "AND LOT_NO__FROM <= '" + lotNo + "' " + "AND LOT_NO__TO >= '" + lotNo
											+ "' " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, listType);
									pstmt.setTimestamp(2, tranDate);
									pstmt.setTimestamp(3, tranDate);
									rs2 = pstmt.executeQuery();
									System.out.println("sql:" + sql);
									System.out.println("8970 priceListParent[" + priceListParent + "] itemCode:["
											+ itemCode + "]");
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										System.out.println("8974rate" + rate);
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
									if (rate > 0) {
										priceList = priceListParent;
									} else {
										priceList = priceListParent;
										priceListParent = "";
									}
								} catch (Exception e) {
									e.printStackTrace();
									throw new ITMException(e);
								}
							}
						}
						// Added by sarita on 13NOV2017
						else {
							if (rs != null) {
								rs.close();
								rs = null;
							}
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new ITMException(e);
					}
					// Added by sarita on 13NOV2017[start]
					finally {
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
					}
					// Added by sarita on 13NOV2017[end]
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		// type = listType; // commented and replace listType instead of list by
		// cpatil on 10-oct-12

		if ((listType.trim().equals("M")) || (listType.trim().equals("N"))) // Discount
			// PRICE
			// //
			// replace
			// listType
			// instead
			// of
			// list
			// by
			// cpatil
			// on
			// 10-oct-12

		{
			rate = 0;
			System.out.println("Inside type ::-<M><N>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " // replace listType
						// instead of list by
						// cpatil on 10-oct-12
						+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("9074rate" + rate);
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} else {
					try {
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							sql = "SELECT LIST_TYPE FROM PRICELIST_MST WHERE PRICE_LIST = '" + priceListParent + "' ";
							pstmt = conn.prepareStatement(sql);
							rs2 = pstmt.executeQuery();
							if (rs2.next()) {
								listType = rs2.getString(1);
								rs2.close();
								pstmt.close();
								rs2 = null;
								pstmt = null;
							}

							try {
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? " // replace
										// listType
										// instead of
										// list by
										// cpatil on
										// 10-oct-12
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, listType);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									System.out.println("9136rate" + rate);
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
								} else {
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
									return -1;
								}
							} catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}

			} catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		if (listType.trim().equals("I")) // Inventory // replace listType
			// instead of list by cpatil on
			// 10-oct-12

		{
			rate = 0;
			if ((lotSl == null) || (lotSl.trim().length() == 0)) {
				System.out.println("Inside type ::-<I>-::");
				try {
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						System.out.println("Rate is .*...." + rate);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} else {
				try {
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' " + "AND LOT_NO = '" + lotNo + "' "
							+ "AND LOT_SL = '" + lotSl + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						System.out.println("9224rate" + rate);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
		}

		return (rate);
	}

	/**
	 * Gets the rate as double
	 * 
	 * @param priceList
	 * @param trDate
	 * @param itemCode
	 * @param conn
	 * @return
	 * @throws ITMException
	 * @throws SQLException
	 */
	public double pickRate(String priceList, String trDate, String itemCode, Connection conn)
			throws ITMException, SQLException {
		System.out.println(
				"@@@@@--------------[pickRate]--[(String priceList,String trDate,String itemCode,Connection conn)]-called------------------------------");

		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		java.sql.Timestamp tranDate = null;
		String sql = "";
		String priceListParent = "";
		String siteCode = "";
		String locCode = "";
		String lotSl = "";
		String listType = "";
		double rate = 0.0;

		try {
			sql = " select list_type from pricelist_mst where price_list = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				listType = rs.getString("list_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("List Type:-[" + listType + "]");

			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");

		} catch (Exception e) {
			System.out.println("Exception...[pickRate] " + sql + e.getMessage());
			e.printStackTrace();
		}

		if (listType.trim().equals("L")) // replace listType instead of list by
			// cpatil on 10-oct-12
		{
			rate = 0;
			System.out.println("Inside type ::-<L>-::");
			try {// try 1
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("9318Rate" + rate);
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} else {

					try {
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1) == null ? "" : rs.getString(1);
							if (priceListParent.trim().length() > 0) {
								try {
									sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent
											+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
											+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, listType);
									pstmt2.setTimestamp(2, tranDate);
									pstmt2.setTimestamp(3, tranDate);
									rs2 = pstmt2.executeQuery();
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										System.out.println("9358Rate" + rate);
										rs2.close();
										pstmt2.close();
										rs2 = null;
										pstmt2 = null;
									} else {
										rs2.close();
										pstmt2.close();
										rs2 = null;
										pstmt2 = null;
										return -1;
									}

									if (rate > 0) {
										priceList = priceListParent;
									} else {
										priceList = priceListParent;
										priceListParent = "";
									}

								} catch (Exception e) {
									e.printStackTrace();
									throw new ITMException(e);
								}
							}

						}

						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}

					} catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}

				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}

			} catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		if (listType.trim().equals("F")) // replace listType instead of list by
			// cpatil on 10-oct-12
		{
			System.out.println("Inside type ::-<F>-::");
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("sql:" + sql);
				System.out.println("9450 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("9452 Rate" + rate);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {

					try {
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							try {
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, listType);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								rs2 = pstmt.executeQuery();
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									System.out.println("9504 Rate" + rate);
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
								} else {
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
									return -1;
								}
							} catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}

				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		if (listType.trim().equals("D")) // DISPATCH // replace listType instead
			// of list by cpatil on 10-oct-12
		{
			rate = 0;
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " + "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				System.out.println("sql:" + sql);
				System.out.println("9570 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
				rs = pstmt.executeQuery();
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("9575 Rate" + rate);
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} else {

					try {
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							System.out.println("The priceListParent is .... " + priceListParent);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0)// 1
						{
							try {
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, listType);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								rs2 = pstmt.executeQuery();
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									System.out.println("9626 Rate:" + rate);
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
								} else {

									try {
										rs2.close();
										pstmt.close();
										rs2 = null;
										pstmt = null;
										sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
												// +
												// "AND LOT_NO__FROM <= '"+lotNo+"' "
												// //commented by cpatil on
												// 10-oct-12
												// +
												// "AND LOT_NO__TO >= '"+lotNo+"' "
												// //commented by cpatil on
												// 10-oct-12
												+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, listType);
										pstmt.setTimestamp(2, tranDate);
										pstmt.setTimestamp(3, tranDate);
										rs2 = pstmt.executeQuery();
										System.out.println("sql:" + sql);
										System.out.println(
												"9655 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											System.out.println("9659 Rate " + rate);
											rs2.close();
											pstmt.close();
											rs2 = null;
											pstmt = null;
										} else {

											try {
												rs2.close();
												pstmt.close();
												rs2 = null;
												pstmt = null;
												sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
														+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
												pstmt = conn.prepareStatement(sql);
												rs2 = pstmt.executeQuery();
												if (rs2.next()) {
													priceListParent = rs2.getString(1);
												}
												rs2.close();
												pstmt.close();
												rs2 = null;
												pstmt = null;
												if ((priceListParent == null)
														|| (priceListParent.trim().length() == 0)) {
													priceListParent = "";
													return -1;
												}
												if (priceListParent.trim().length() > 0)// 2
												{
													try {
														sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
																+ priceListParent + "' " + "AND ITEM_CODE = '"
																+ itemCode + "' " + "AND LIST_TYPE = ? "
																// +
																// "AND LOT_NO__FROM <= '"+lotNo+"' "
																// //commented
																// by cpatil on
																// 10-oct-12
																// +
																// "AND LOT_NO__TO >= '"+lotNo+"' "
																// //commented
																// by cpatil on
																// 10-oct-12
																+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, listType);
														pstmt.setTimestamp(2, tranDate);
														pstmt.setTimestamp(3, tranDate);
														rs3 = pstmt.executeQuery();
														if (rs3.next()) {
															rate = rs3.getDouble(1);
															System.out.println("9711 Rate " + rate);
															rs3.close();
															pstmt.close();
															rs3 = null;
															pstmt = null;
														} else {
															rs3.close();
															pstmt.close();
															rs3 = null;
															pstmt = null;
															return -1;
														}
													} catch (Exception e) {
														e.printStackTrace();
														throw new ITMException(e);
													}
												}
											} catch (Exception e) {
												e.printStackTrace();
												throw new ITMException(e);
											}
										}

									} catch (Exception e) {
										e.printStackTrace();
										throw new ITMException(e);
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
								throw new ITMException(e);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						throw new ITMException(e);
					}
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.clearParameters();
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		if (listType.trim().equals("B"))// BATCH PRICE // replace listType
			// instead of list by cpatil on
			// 10-oct-12

		{
			rate = 0;
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? "
						// + "AND LOT_NO__FROM <= '"+lotNo+"' " //commented by
						// cpatil on 10-oct-12
						// + "AND LOT_NO__TO >= '"+lotNo+"' " //commented by
						// cpatil on 10-oct-12
						+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("sql:" + sql);
				System.out.println("9802 priceList[" + priceList + "] itemCode:[" + itemCode + "]");
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("9806 Rate " + rate);

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				} else {
					try {
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
								priceListParent = "";
							}
							if (priceListParent.trim().length() > 0) {
								try {
									sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent
											+ "' " + "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? "
											// +
											// "AND LOT_NO__FROM <= '"+lotNo+"' "
											// //commented by cpatil on
											// 10-oct-12
											// +
											// "AND LOT_NO__TO >= '"+lotNo+"' "
											// //commented by cpatil on
											// 10-oct-12
											+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, listType);
									pstmt.setTimestamp(2, tranDate);
									pstmt.setTimestamp(3, tranDate);
									rs2 = pstmt.executeQuery();
									if (rs2.next()) {
										rate = rs2.getDouble(1);
										System.out.println("9856 Rate " + rate);
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
									if (rate > 0) {
										priceList = priceListParent;
									} else {
										priceList = priceListParent;
										priceListParent = "";
									}
								} catch (Exception e) {
									e.printStackTrace();
									throw new ITMException(e);
								}
							}
						}
						// Added by sarita on 13NOV2017
						else {
							if (rs != null) {
								rs.close();
								rs = null;
							}
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new ITMException(e);
					}
					// Added by sarita on 13NOV2017
					finally {
						if (rs != null) {
							rs.close();
							rs = null;
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
					}
				}

			} catch (Exception e) {

				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		// type = listType; // commented and replace listType instead of list by
		// cpatil on 10-oct-12

		if ((listType.trim().equals("M")) || (listType.trim().equals("N"))) // Discount
			// PRICE
			// //
			// replace
			// listType
			// instead
			// of
			// list
			// by
			// cpatil
			// on
			// 10-oct-12

		{
			rate = 0;
			System.out.println("Inside type ::-<M><N>-::");
			try {
				sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' " + "AND ITEM_CODE = '"
						+ itemCode + "' " + "AND LIST_TYPE = ? " // replace listType
						// instead of list by
						// cpatil on 10-oct-12
						+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, listType);
				pstmt.setTimestamp(2, tranDate);
				pstmt.setTimestamp(3, tranDate);
				rs = pstmt.executeQuery();
				System.out.println("Rate sql ..." + sql);
				if (rs.next()) {
					rate = rs.getDouble(1);
					System.out.println("9956 Rate ..." + rate);
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} else {
					try {
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceListParent = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
							priceListParent = "";
							return -1;
						}
						if (priceListParent.trim().length() > 0) {
							// type = getPriceListType(priceList,conn); //
							// commented by cpatil on 10-oct-12
							sql = "SELECT LIST_TYPE FROM PRICELIST_MST WHERE PRICE_LIST = '" + priceListParent + "' ";
							pstmt = conn.prepareStatement(sql);
							rs2 = pstmt.executeQuery();
							if (rs2.next()) {
								listType = rs2.getString(1);
								rs2.close();
								pstmt.close();
								rs2 = null;
								pstmt = null;
							}

							try {
								sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceListParent + "' "
										+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = ? " // replace
										// listType
										// instead of
										// list by
										// cpatil on
										// 10-oct-12
										+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, listType);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								rs2 = pstmt.executeQuery();
								System.out.println("Rate sql 10015" + sql);
								System.out.println("The priceListParent sql .. " + sql);
								if (rs2.next()) {
									rate = rs2.getDouble(1);
									System.out.println("Rate 10020 :: " + rate);
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
								} else {
									rs2.close();
									pstmt.close();
									rs2 = null;
									pstmt = null;
									return -1;
								}
							} catch (Exception e) {
								System.out.println("Exception...[pickRate] " + sql + e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception...[pickRate] " + sql + e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				}

			} catch (Exception e) {
				System.out.println("Exception...[pickRate] " + sql + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		if (listType.trim().equals("I")) // Inventory // replace listType
			// instead of list by cpatil on
			// 10-oct-12

		{
			rate = 0;
			if ((lotSl == null) || (lotSl.trim().length() == 0)) {
				System.out.println("Inside type ::-<I>-::");
				try {
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' ";
					// + "AND LOT_NO = '"+lotNo+"'"; //commented by cpatil on
					// 10-oct-12
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						System.out.println("Rate is .*...." + rate);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			} else {
				try {
					sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '" + itemCode + "' " + "AND SITE_CODE = '"
							+ siteCode + "' " + "AND LOC_CODE = '" + locCode + "' "
							// + "AND LOT_NO = '"+lotNo+"' " //commented by
							// cpatil on 10-oct-12
							+ "AND LOT_SL = '" + lotSl + "'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						rate = rs.getDouble(1);
						System.out.println("Rate 10108 ..." + rate);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				} catch (Exception e) {
					System.out.println("Exception...[pickRate] " + sql + e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
		}
		return (rate);
	}

	// added by cpatil on 10/oct/12 end
	// Start added by chandrashekar on 10-aug-2016
	/**
	 * gets the rate in double considering reference number
	 * 
	 * @param priceList
	 * @param trDate
	 * @param itemCode
	 * @param aRefNo
	 * @param listType
	 * @param quantity
	 * @param conn
	 * @return
	 * @throws ITMException
	 */
	public double pickRateRefnoWise(String priceList, String trDate, String itemCode, String aRefNo, String listType,
			double quantity, Connection conn) throws ITMException {
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

		type = getPriceListType(priceList, conn);
		try {
			trDate = genericUtility.getValidDateString(trDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat());
			tranDate = java.sql.Timestamp.valueOf(trDate + " 00:00:00");

			if (aRefNo.indexOf("~t") > 0) {

				String MulStr[] = aRefNo.split("~t");
				siteCode = MulStr[0];
				locCode = MulStr[1];
				lotNo = MulStr[2];
				lotSl = MulStr[3];

			} else {
				lsRefNo = aRefNo;

			}
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
							// System.out.println("The priceListParent if null .... "+priceListParent);
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
								// System.out.println("The priceListParent sql .. "+sql);
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
							// System.out.println("The priceListParent if null .... "+priceListParent);
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
													// System.out.println("The priceListParent is ....
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
				// added by rupali on 30/04/19 for getting rate slab_no wise if ref_no is geting
				// blank [start]
				if (lsRefNo == null || lsRefNo.trim().length() == 0) {
					String slabNo = getMaxSlabNo(priceList, tempDate, itemCode, lsRefNo, listType, quantity, conn);
					sql = "SELECT RATE,LOT_NO__FROM FROM PRICELIST WHERE PRICE_LIST = '" + priceList
							+ "' AND ITEM_CODE = '" + itemCode + "' AND LIST_TYPE = 'B'" + " and slab_no ='" + slabNo
							+ "' " + "AND MIN_QTY <= " + quantity + " AND MAX_QTY >= " + quantity
							+ " AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
					pstmt = conn.prepareStatement(sql);
				}
				// added by rupali on 30/04/19 for getting rate slab_no wise if ref_no is geting
				// blank [end]
				else {
					sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '" + priceList + "' "
							+ "AND ITEM_CODE = '" + itemCode + "' " + "AND LIST_TYPE = 'B'" + " and ref_no ='" + lsRefNo
							+ "' " + "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
							// + "AND LOT_NO__FROM <= '"+lotNo+"' "
							// + "AND LOT_NO__TO >= '"+lotNo+"' "
							+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
					pstmt = conn.prepareStatement(sql);
				}
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
								+ "FROM pricelist_mst WHERE PRICE_LIST = '" + priceList + "'" + " AND LIST_TYPE = 'B'";
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
								// added by rupali on 30/04/19 for getting rate slab_no wise if ref_no is geting
								// blank [start]
								if (lsRefNo == null || lsRefNo.trim().length() == 0) {
									String slabNo = getMaxSlabNo(priceList, tempDate, itemCode, lsRefNo, listType,
											quantity, conn);
									sql = "SELECT RATE,LOT_NO__FROM FROM PRICELIST WHERE PRICE_LIST = '"
											+ priceListParent + "' AND ITEM_CODE = '" + itemCode
											+ "' AND LIST_TYPE = 'B' AND SLAB_NO = '" + slabNo + "' AND MIN_QTY <= "
											+ quantity + " AND MAX_QTY >= " + quantity
											+ " AND EFF_FROM <= ? AND VALID_UPTO >= ?";
									pstmt = conn.prepareStatement(sql);
								}
								// added by rupali on 30/04/19 for getting rate slab_no wise if ref_no is geting
								// blank [end]
								else {
									sql = "SELECT RATE,lot_no__from FROM PRICELIST " + "WHERE PRICE_LIST = '"
											+ priceListParent + "' " + "AND ITEM_CODE = '" + itemCode + "' "
											+ "AND LIST_TYPE = 'B'" + " and ref_no ='" + lsRefNo + "' "
											+ "AND MIN_QTY <= " + quantity + " " + "AND MAX_QTY >= " + quantity + " "
											// + "AND LOT_NO__FROM <= '"+lotNo+"' "
											// + "AND LOT_NO__TO >= '"+lotNo+"' "
											+ "AND EFF_FROM <= ? " + "AND VALID_UPTO >= ?";
									pstmt = conn.prepareStatement(sql);
								}
								pstmt.setTimestamp(1, tranDate);
								pstmt.setTimestamp(2, tranDate);
								rs2 = pstmt.executeQuery();
								// System.out.println("The priceListParent sql .. "+sql);
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
					type = getPriceListType(priceList, conn);
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
							type = getPriceListType(priceList, conn);
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
		// "select calc_method, method_applicable from pricelist_mst where price_list
		// =?";
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

	// End added by chandrashekar on 10-aug-2016

	// Added By Kaustubh on 14 Nov 2017 start

	/**
	 * Gets the sales pricelist
	 * 
	 * @param custCd
	 * @param custCdDlv
	 * @param siteCd
	 * @param contrNo
	 * @param itmCode
	 * @param orddt
	 * @param conn
	 * @return
	 * @throws ITMException
	 */
	public String getSalesPriceList(String custCd, String custCdDlv, String siteCd, String contrNo, String itmCode,
			String orddt, Connection conn) throws ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String priceLst = "", sql = "";
		Timestamp OrderDate = null;

		try {
			System.out.println(">>>>>>>>>Enter in getPriceList method");
			System.out.println(">>>>>>>>custCd===" + custCd);
			System.out.println(">>>>>>>>custCdDlv===" + custCdDlv);
			System.out.println(">>>>>>>>siteCd===" + siteCd);
			System.out.println(">>>>>>>>contractNo===" + contrNo);
			System.out.println(">>>>>>>>itmCode===" + itmCode);
			System.out.println(">>>>>>>>orddt===" + orddt);

			if (orddt != null && orddt.trim().length() > 0) {
				OrderDate = Timestamp.valueOf(genericUtility.getValidDateString(orddt,
						genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}

			if (contrNo != null && contrNo.trim().length() > 0) {
				sql = "select price_list from scontract where contract_no=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, contrNo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					priceLst = checkNull(rs.getString("price_list"));
					System.out.println("Price list from contract no===========" + priceLst);
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (priceLst == null || priceLst.trim().length() == 0) {
					sql = "select price_list from site_customer where site_code = ? and cust_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCd);
					pstmt.setString(2, custCd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						priceLst = checkNull(rs.getString("price_list"));
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if (priceLst == null || priceLst.trim().length() == 0) {
						sql = "select price_list from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceLst = checkNull(rs.getString("price_list"));
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						if (priceLst == null || priceLst.trim().length() == 0) {
							sql = "select price_list from site_customer where site_code = ? and cust_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCd);
							pstmt.setString(2, custCdDlv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								priceLst = checkNull(rs.getString("price_list"));
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;

							if (priceLst == null || priceLst.trim().length() == 0) {
								sql = "select price_list from customer where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCdDlv);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceLst = checkNull(rs.getString("price_list"));
								}
								rs.close();
								pstmt.close();
								pstmt = null;
								rs = null;
							}
						}
					}
				}

			} else {
				sql = "select price_list from site_customer where site_code = ? and cust_code=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCd);
				pstmt.setString(2, custCd);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					priceLst = checkNull(rs.getString("price_list"));
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;

				if (priceLst == null || priceLst.trim().length() == 0) {
					sql = "select price_list from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCd);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						priceLst = checkNull(rs.getString("price_list"));
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;

					if (priceLst == null || priceLst.trim().length() == 0) {
						sql = "select price_list from site_customer where site_code = ? and cust_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCd);
						pstmt.setString(2, custCdDlv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceLst = checkNull(rs.getString("price_list"));
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						rs = null;

						if (priceLst == null || priceLst.trim().length() == 0) {
							sql = "select price_list from customer where cust_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCdDlv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								priceLst = checkNull(rs.getString("price_list"));
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							rs = null;
						}
					}
				}
			}

			if (contrNo != null && contrNo.trim().length() > 0) {

				sql = "select price_list from scontract where contract_no=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, contrNo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					priceLst = rs.getString("price_list");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (priceLst == null || priceLst.trim().length() == 0) {

					sql = " select  g.price_list from group_pricelist g,pricelist p where  " + " g.cust_code = ? "
							+ " and p.item_code  = ? "
							+ " and (? between to_date(g.eff_from) and to_date(g.valid_upto)) AND g.price_list=p.price_list ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCd);
					pstmt.setString(2, itmCode);
					pstmt.setTimestamp(3, OrderDate);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						priceLst = checkNull(rs.getString("price_list"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (priceLst == null || priceLst.trim().length() == 0) {
						sql = "select price_list from site_customer where site_code = ? and cust_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCd);
						// pstmt2.setString(2,custCd);// as
						// per mail on 29 aug
						pstmt.setString(2, custCd);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							priceLst = checkNull(rs.getString("price_list"));

							if (priceLst == null) {
								priceLst = "";
							}

						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						if (priceLst == null || priceLst.trim().length() == 0) {
							sql = "select price_list from customer where cust_code = ?";
							pstmt = conn.prepareStatement(sql);

							pstmt.setString(1, custCd);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								priceLst = checkNull(rs.getString("price_list"));

							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;

							if (priceLst == null || priceLst.trim().length() == 0) {
								System.out.println(">>>>>>>>>>>priceList Null found for Customer Code:" + priceLst);
								sql = " select  g.price_list from group_pricelist g,pricelist p where  "
										+ " g.cust_code = ? " + " and p.item_code  = ? "
										+ " and (? between to_date(g.eff_from) and to_date(g.valid_upto)) AND g.price_list=p.price_list ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCdDlv);
								pstmt.setString(2, itmCode);
								pstmt.setTimestamp(3, OrderDate);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									priceLst = checkNull(rs.getString("price_list"));
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;

								if (priceLst == null || priceLst.trim().length() == 0) {
									sql = "select price_list from site_customer where site_code = ? and cust_code=?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, siteCd);
									pstmt.setString(2, custCdDlv);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										priceLst = checkNull(rs.getString("price_list"));
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;

									if (priceLst == null || priceLst.trim().length() == 0) {
										sql = "select price_list from customer where cust_code = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, custCdDlv);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											priceLst = checkNull(rs.getString("price_list"));
										}
										pstmt.close();
										pstmt = null;
										rs.close();
										rs = null;

									}
								}
							}
						}
					}

				}

			}

			System.out.println("Final Price List =======" + priceLst);

		} catch (Exception e) {
			System.out.println("Exception ::" + e);
			throw new ITMException(e);
		}

		finally {
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
			}
		}

		return priceLst;

	}

	/**
	 * Gets the sales rate as double
	 * 
	 * @param pricelist
	 * @param SiteCode
	 * @param custCode
	 * @param mOrderDateStr
	 * @param ldtDateStr
	 * @param lsNature
	 * @param ContractNo
	 * @param lsContractNo
	 * @param itemCode
	 * @param mQty
	 * @param conn
	 * @return
	 * @throws ITMException
	 */
	public double getSalesRate(String pricelist, String SiteCode, String custCode, String mOrderDateStr,
			String ldtDateStr, String lsNature, String ContractNo, String lsContractNo, String itemCode, double mQty,
			Connection conn) throws ITMException {
		System.out.println("+++++++++++++++inside getSalesRate++++++++++++++");
		System.out.println("pricelist::" + pricelist);
		System.out.println("SiteCode::" + SiteCode);
		System.out.println("SiteCode::" + custCode);
		System.out.println("mOrderDateStr::" + mOrderDateStr);

		double salesRate = 0, mRate = 0.0, lcRate = 0.0, mquantity = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", lsLineNoContr = "", priceListDiscount = "";
		String lsListType = "", lsPriceListParent = "", lsRefNo = "", lsUnit = "";
		Timestamp ldPlistDate = null, mTranDate = null;
		Timestamp mOrderDate = null, ldtPlDate = null;

		try {

			lsListType = getPriceListType(pricelist, conn);

			sql = "select unit  from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsUnit = rs.getString("unit");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (ldtDateStr == null || ldtDateStr.trim().length() <= 0) {

				ldtDateStr = mOrderDateStr;

			}

			if (mOrderDateStr != null && mOrderDateStr.trim().length() > 0) {
				mOrderDate = Timestamp.valueOf(genericUtility.getValidDateString(mOrderDateStr,
						genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if (ldtDateStr != null && ldtDateStr.length() > 0) {
				ldtPlDate = Timestamp.valueOf(genericUtility.getValidDateString(ldtDateStr,
						genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			}

			if (ContractNo.trim().length() > 0) {
				sql = "select site_code,item_code,item_flg,quantity,unit,dsp_date,"
						+ " rate,discount,tax_amt,tax_class,tax_chap,"
						+ " tax_env, net_amt,remarks,item_descr,unit__rate,"
						+ " pack_code, pack_instr,no_art,quantity__stduom,rate__stduom,"
						+ " unit__std, conv__qty_stduom, conv__rtuom_stduom"
						+ " from scontractdet where  contract_no= ? and line_no =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsContractNo);
				pstmt.setString(2, lsLineNoContr);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mRate = rs.getDouble("rate");
					mquantity = rs.getDouble("quantity");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			if (mRate <= 0) {

				if ("F".equalsIgnoreCase(lsNature) || "B".equalsIgnoreCase(lsNature)
						|| "S".equalsIgnoreCase(lsNature)) {
					mRate = 0;
				} else {

					if (pricelist != null || pricelist.trim().length() > 0) {

						if ("B".equalsIgnoreCase(lsListType) && mRate <= 0) {

							sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
									+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, pricelist);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lsUnit);
							pstmt.setString(4, lsListType);
							pstmt.setTimestamp(5, mOrderDate);
							pstmt.setTimestamp(6, mOrderDate);
							pstmt.setDouble(7, mQty);
							pstmt.setDouble(8, mQty);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								lsRefNo = checkNull(rs.getString(1));
								// lsRefNo = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (lsRefNo != null && lsRefNo.trim().length() > 0) {
								mRate = pickRateRefnoWise(pricelist, ldtDateStr, itemCode, lsRefNo, "L", mQty, conn);
							}
						}

						if (mRate <= 0) {
							mRate = pickRate(pricelist, ldtDateStr, itemCode, "", "L", mQty, conn);
						}
					}
					if (lsListType == null || lsListType.trim().length() == 0) {
						sql = "select price_list__parent  from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, pricelist);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lsPriceListParent = rs.getString("price_list__parent");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (lsPriceListParent != null || lsPriceListParent.trim().length() > 0) {
							lsListType = getPriceListType(lsPriceListParent, conn);
						}
					}
					if ("B".equalsIgnoreCase(lsListType) && mRate < 0) {
						mRate = 0;
					}
				}

			} // if (mRate <= 0)

			if (mRate > 0) {
				sql = "select pricelist.rate rate, "
						+ " case when pricelist.rate_type = 'P' then '%' else 'Fix' end as rate_type, "
						+ "pricelist.price_list price_list" + " from	site_customer ,pricelist "
						+ " where site_customer.price_list__disc = pricelist.price_list "
						+ " and pricelist.list_type = 'M' " + " and site_customer.site_code = ? "
						+ " and site_customer.cust_code = ? " + " and pricelist.item_code = ? "
						+ " and site_customer.price_list__disc is not null";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, SiteCode);
				pstmt.setString(2, custCode);
				pstmt.setString(3, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {

					priceListDiscount = rs.getString("price_list") == null ? "" : rs.getString("price_list");
				}
				rs.close();
				pstmt.close();
				pstmt = null;
				rs = null;

				if (priceListDiscount != null && priceListDiscount.length() > 0) {
					lcRate = getDiscount(priceListDiscount, mOrderDate, custCode, SiteCode, itemCode, lsUnit, 0.0,
							ldtPlDate, mquantity, conn);
				}

				if (lcRate > 0) {
					mRate = mRate - (mRate * lcRate / 100);
				}
			}

		} catch (Exception e) {
			System.out.println("Exception ::" + e);
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
			} catch (Exception e) {
			}
		}
		return mRate;
	}

	/**
	 * Gets the discount as double
	 * 
	 * @param priceList
	 * @param orderDate
	 * @param custCode
	 * @param siteCode
	 * @param itemCode
	 * @param unit
	 * @param discMerge
	 * @param pListDate
	 * @param quantity
	 * @param conn
	 * @return
	 * @throws ITMException
	 */
	public double getDiscount(String priceList, Timestamp orderDate, String custCode, String siteCode, String itemCode,
			String unit, double discMerge, Date pListDate, double quantity, Connection conn) throws ITMException {
		String listType = "", itmSer = "";
		double disc = 0;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		double rate = 0;
		try {
			if (priceList != null && priceList.trim().length() > 0) {
				listType = getPriceListType(priceList, conn);

				sql = " select case when rate is null then 0 else rate end as rate from pricelist  where price_list=? and item_code =? and unit= ? and list_type IN ('M','N')	and	case when min_qty is null then 0 else ? end <=?	and ((case when max_qty is null then 0 else max_qty end	>=	?) OR (case when max_qty is null then 0 else max_qty end=0)) and eff_from<=	?	and	valid_upto	>=	?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, priceList);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, unit);
				pstmt.setDouble(4, quantity);
				pstmt.setDouble(5, quantity);
				pstmt.setDouble(6, quantity);
				pstmt.setTimestamp(7, orderDate);
				pstmt.setTimestamp(8, orderDate);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					rate = rs.getDouble("rate");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if (listType.equals("M") || priceList == null || priceList.trim().length() == 0 || rate == 0) {
				sql = "select item_ser from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					itmSer = rs.getString("item_ser");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql = "select disc_perc from customer_series 	where cust_code = ? and item_ser =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itmSer);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					disc = rs.getDouble("disc_perc");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (disc == 0) {
					sql = "select disc_perc from site_customer where site_code = ? and cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						disc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (disc == 0) {
					sql = "select disc_perc  from customer where cust_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						disc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if (listType.equals("M")) {
					discMerge = disc;
					if (rate != 0) {
						disc = rate;
					}
				} else {
					discMerge = 0;
				}
				if (disc == 0) {
					disc = 0;
				}
				if (itemCode == null || itemCode.trim().length() == 0) {
					disc = 0;
				}
			}
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
			} catch (Exception e) {
			}
		}
		return disc;

	}

	/**
	 * Gets empty string if the argumnet is null
	 * @param input
	 * @return
	 */
	public String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input;
	}

	// Added by kaustubh 14 Nov 2017 End

	// Added by Anjali R. on[13/04/2018][To calculate rate][Start]
	/**
	 * Calculate the rate by evaluating formula expression
	 *  
	 * @param priceListTar
	 * @param calcRate
	 * @param calcMethod
	 * @param errCode
	 * @param conn
	 * @return
	 */
	public HashMap<String, String> calcRate(String priceListTar, HashMap<String, Object> calcRate, String calcMethod,
			String errCode, Connection conn) {

		ArrayList inputVal = null;
		ArrayList temp = null;

		String sqlStmt = "", sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, String> errMap = new HashMap<String, String>();
		String varName = "";
		String varExpr = "";
		String varInput = "";
		String varSrc = "";
		String mustRetRec = "";
		String finalRate = "";
		// HashMap<String,String> sqlInputDataMap = new HashMap<String, String>();
		GenericUtility genericUtility;
		// Added by Pavan Rane on 22NOV19[Var decalred] start
		String exprStr = "", expDecStr = "";
		int decPlac = 0;
		double finalRateD = 0.0;
		boolean executeflag = false;
		// Added by Pavan Rane on 22NOV19[Var decalred] end
		try {
			System.out.println("-----------------------------Inside calc method---------------------------");
			genericUtility = new GenericUtility();
			inputVal = new ArrayList();
			temp = new ArrayList();
			if (errCode.trim().length() == 0 || errCode == null) {
				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine exprsEngine = manager.getEngineByName("js");

				// sqlStmt = "select var_name, var_expr, var_source, var_input from
				// calc_method_var where calc_method = ? order by calc_seq ";
				sqlStmt = "select var_name, var_expr, var_source, var_input, must_ret_rec from calc_method_var where calc_method = ? order by calc_seq ";
				pstmt = conn.prepareStatement(sqlStmt);
				pstmt.setString(1, calcMethod);
				rs = pstmt.executeQuery();
				while (rs.next()) {

					varName = checkNull(rs.getString("var_name"));
					varExpr = checkNull(rs.getString("var_expr"));
					varSrc = checkNull(rs.getString("var_source"));
					varInput = checkNull(rs.getString("var_input"));
					mustRetRec = E12GenericUtility.checkNull(rs.getString("must_ret_rec"));
					if (mustRetRec == null || mustRetRec.trim().length() == 0) {
						mustRetRec = "1";
					}
					String inputValues[] = null;
					System.out.println("varName[" + varName + "]varSrc[" + varSrc + "]varInput[" + varInput
							+ "]varExpr--[" + varExpr + "]");
					if ((varName != null && varName.trim().length() > 0)
							&& (varExpr != null && varExpr.trim().length() > 0)) {
						varExpr = varExpr.trim();
						sql = "";
						inputVal = temp;

						if (varSrc.equalsIgnoreCase("S")) {
							if (varInput != null && varInput.trim().length() > 0) {
								PreparedStatement exprPsmt = null;
								ResultSet exprRs = null;
								exprPsmt = conn.prepareStatement(varExpr);
								inputValues = varInput.split(",");
								int index = 1;
								for (String eachSqlInput : inputValues) {
									System.out.println("[inputValues]------------[" + inputValues + "]");
									String var = eachSqlInput.toUpperCase().trim();
									System.out.println(
											"Variable Name---[" + var + "]Variable Value--[" + calcRate.get(var) + "]");

									if (calcRate.containsKey(var)) {
										if ("EFF_FROM".equalsIgnoreCase(var)) {
											exprPsmt.setTimestamp(index++, (Timestamp) calcRate.get("EFF_FROM"));
										} else if ("VALID_UPTO".equalsIgnoreCase(var)) {
											exprPsmt.setTimestamp(index++, (Timestamp) calcRate.get("VALID_UPTO"));
										} else {
											exprPsmt.setString(index++, (String) calcRate.get(var));
										}
									} else {
										// Pavan R 29may19[to get var from err map] start
										System.out.println("exprsEngine varName [" + varName + "]varInpuet[" + varInput
												+ "]value[" + exprsEngine.get(varInput) + "]");
										if (exprsEngine.get(varInput) != null
												&& exprsEngine.get(varInput).toString().length() > 0) {
											exprPsmt.setString(index++, exprsEngine.get(varInput).toString());
										} else {
											exprPsmt.setString(index++, "");
										}
										// Pavan R end
									}
								}
								exprRs = exprPsmt.executeQuery();
								if (exprRs.next()) {
									finalRate = E12GenericUtility.checkNull(exprRs.getString(1));
									exprsEngine.put(varName, getDoubleValue(finalRate));
								} else {
									exprsEngine.put(varName, 0);
								}
								// Added by Pavan Rane on 03APR19 start[closed open resources]
								if( exprRs != null ) {
									exprRs.close();
									exprRs = null;
								}
								if( exprPsmt != null ) {
									exprPsmt.close();
									exprPsmt = null;
								}
								// Pavan Rane on 03APR19 end[closed open resources]
							}
						} else if ("E".equalsIgnoreCase(varSrc)) {
							if (varExpr.indexOf("ROUND(") != -1) {
								// varExpr = varExpr.replaceAll("ROUND\\(", "Math.round(");
								varExpr = varExpr.replaceAll("ROUND\\(", "");
								System.out.println("in 1st if updated varExpr[" + varExpr + "]");
								// Modified by Pavan Rane on 22NOV19[Removed expre round and set format decimal
								// places of rate] start
								String varExpArr[] = varExpr.split(",");
								if (varExpArr.length > 0) {
									exprStr = varExpArr[0];
									// System.out.println("varExprMain:"+varExprMain.trim());
									finalRate = String.valueOf(exprsEngine.eval(exprStr));
									executeflag = true;
									System.out.println("finalRate from exprStr :" + finalRate);
								}
								if (varExpArr.length > 1) {
									expDecStr = varExpArr[1];
									// System.out.println("varExprDec"+expdecStr);
									expDecStr = expDecStr.replaceAll("\\)", "");
									decPlac = Integer.parseInt(expDecStr.trim());
									System.out.println("expDecStr from exprStr :" + expDecStr);
								}
								if ("NaN".equalsIgnoreCase(finalRate)) {
									finalRateD = 0.0;
								} else {
									finalRateD = Double.parseDouble(finalRate);
								}
								if (decPlac == 0) {
									finalRateD = getRndamt(finalRateD, "R", 1);
								} else if (decPlac == 1) {
									finalRateD = getRndamt(finalRateD, "R", .1);
								} else if (decPlac == 2) {
									finalRateD = getRndamt(finalRateD, "R", .01);
								} else if (decPlac == 3) {
									finalRateD = getRndamt(finalRateD, "R", .001);
								} else if (decPlac == 4) {
									finalRateD = getRndamt(finalRateD, "R", .0001);
								}
								finalRate = String.valueOf(finalRateD);
							} else if (varExpr.indexOf("ROUND (") != -1) {
								// varExpr = varExpr.replaceAll("ROUND \\(", "Math.round(");
								varExpr = varExpr.replaceAll("ROUND \\(", "");
								System.out.println("in 2nd if updated varExpr[" + varExpr + "]");
								String varExpArr[] = varExpr.split(",");
								if (varExpArr.length > 0) {
									exprStr = varExpArr[0];
									// System.out.println("varExprMain:"+varExprMain.trim());
									finalRate = String.valueOf(exprsEngine.eval(exprStr));
									executeflag = true;
									System.out.println("finalRate from exprStr :" + finalRate);
								}
								if (varExpArr.length > 1) {
									expDecStr = varExpArr[1];
									// System.out.println("varExprDec"+expdecStr);
									expDecStr = expDecStr.replaceAll("\\)", "");
									decPlac = Integer.parseInt(expDecStr.trim());
									System.out.println("expDecStr from exprStr :" + expDecStr);
								}
								if ("NaN".equalsIgnoreCase(finalRate)) {
									finalRateD = 0.0;
								} else {
									finalRateD = Double.parseDouble(finalRate);
								}
								if (decPlac == 0) {
									finalRateD = getRndamt(finalRateD, "R", 1);
								} else if (decPlac == 1) {
									finalRateD = getRndamt(finalRateD, "R", .1);
								} else if (decPlac == 2) {
									finalRateD = getRndamt(finalRateD, "R", .01);
								} else if (decPlac == 3) {
									finalRateD = getRndamt(finalRateD, "R", .001);
								} else if (decPlac == 4) {
									finalRateD = getRndamt(finalRateD, "R", .0001);
								}
								finalRate = String.valueOf(finalRateD);
							}
							if (!executeflag) {
								finalRate = String.valueOf(exprsEngine.eval(varExpr));
							}
							// Modified by Pavan Rane on 22NOV19[Removed expre round and set format decimal
							// places of rate] end
							System.out.println("inside E :::" + varName + "=[" + finalRate + "]");

							if ("NaN".equalsIgnoreCase(finalRate)) {
								exprsEngine.put(varName, 0);
							} else {
								exprsEngine.put(varName, getDoubleValue(finalRate));
							}
						}
						System.out.println(
								"calcRate::finalRate[" + getDoubleValue(finalRate) + "]mustRetRec[" + mustRetRec + "]");
						// Pavan R 23may19 start
						if (getDoubleValue(finalRate) <= 0) {
							if ("1".equals(mustRetRec)) {
								errCode = "Result of step # " + varName + " failed with no data. Step Type " + varSrc
										+ ", step data " + varExpr + " ";
								errMap.put("error", errCode);
								return errMap;
							}
						}
						// Pavan R 23may19 end
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				/*
				 * if(getDoubleValue(finalRate) <= 0) { errCode = "VTRATE2"; errMap.put("error",
				 * errCode); }
				 */
			}
		} catch (Exception e) {
			// Pavan Rane 20may19 [to handle exception trace/details in errMap] start
			errCode = varName + "\t" + varExpr + "\t" + varSrc + "\t" + varInput;
			errMap.put("error", errCode);
			// Pavan Rane 20may19 [to handle exception trace/details in errMap] end
			System.out.println("Exception in calcRate-------[" + e.getMessage() + "]");
			e.printStackTrace();
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
			}
		}
		if (errCode == null || errCode.trim().length() == 0) {
			errMap.put("rate", finalRate);
		}
		return errMap;
	}

	// Added by Anjali R. on[13/04/2018][To calculate rate][End]
	// Added by Anjali R. on[13/04/2018][To parse string value into
	// Timestamp][Start]
	// Modified by Anjali R. on [26/11/2018][Change access modifier of getTimeStamp
	// method ]
	// private java.sql.Timestamp getTimeStamp(String dateStr) throws ITMException,
	// Exception
	/**
	 * Gets timestamp from a string
	 * 
	 * @param dateStr
	 * @return
	 * @throws ITMException
	 * @throws Exception
	 */
	public java.sql.Timestamp getTimeStamp(String dateStr) throws ITMException, Exception {
		String dbDateStr = "";
		if (dateStr != null && !dateStr.equals("")) {
			if (dateStr.indexOf(":") != -1) {
				System.out.println("inside logic" + dateStr);
				return java.sql.Timestamp.valueOf(dateStr);
			} else {
				System.out.println("inside ");
				dbDateStr = genericUtility.getValidDateTimeString(dateStr, genericUtility.getApplDateFormat(),
						genericUtility.getDBDateTimeFormat());
				return java.sql.Timestamp.valueOf(dbDateStr);
			}
		} else {
			java.util.Date today = new java.util.Date();
			System.out.println(new java.sql.Timestamp(today.getTime()));
			java.sql.Timestamp dateTime = (new java.sql.Timestamp(today.getTime()));
			return dateTime;
		}
	}

	// Added by Anjali R. on[13/04/2018][To parse string value into Timestamp][End]
	// Added by Anjali R. on[13/04/2018][To parse string value into double][Start]
	/**
	 * Gets double from sting
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	private double getDoubleValue(String input) throws Exception {
		double value = 0.0;
		try {
			if (input == null) {
				value = 0.0;
			} else {
				if (input.trim().length() > 0) {
					value = Double.valueOf(E12GenericUtility.checkNull(input));
				}
			}
			System.out.println("retutn value[" + value + "]");
		} catch (Exception e) {
			System.out.println("PriceListGenEJB.getDoubleValue()[" + e.getMessage() + "]");
			throw e;
		}
		return value;
	}

	// Added by Anjali R. on[13/04/2018][To parse string value into double][End]
	// added by rupali on 30/04/19 for getting rate slab_no wise if ref_no is geting
	// blank [start]
	/**
	 * Gets the maximum tax slab
	 * 
	 * @param priceList
	 * @param trDate
	 * @param itemCode
	 * @param aRefNo
	 * @param listType
	 * @param quantity
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private String getMaxSlabNo(String priceList, String trDate, String itemCode, String aRefNo, String listType,
			double quantity, Connection conn) throws Exception {
		String slabNumber = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String lsUnit = "";
			sql = "select unit  from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lsUnit = rs.getString("unit");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select max(slab_no) from pricelist where price_list = ? and item_code= ? and unit= ? and list_type= ? and eff_from <= ? and valid_upto >= ? and min_qty <= ? and max_qty >= ? and (slab_no is not null)";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, lsUnit);
			pstmt.setString(4, listType);
			pstmt.setTimestamp(5, Timestamp.valueOf(genericUtility.getValidDateTimeString(trDate,
					genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())));
			pstmt.setTimestamp(6, Timestamp.valueOf(genericUtility.getValidDateTimeString(trDate,
					genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())));
			pstmt.setDouble(7, quantity);
			pstmt.setDouble(8, quantity);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				slabNumber = checkNull(rs.getString(1));
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		} catch (Exception e) {
			System.out.println("PriceListGenEJB.getDoubleValue()[" + e.getMessage() + "]");
			throw e;
		}
		return slabNumber;
	}
	// added by rupali on 30/04/19 for getting rate slab_no wise if ref_no is geting
	// blank [end]

	//added by a mhatre on 02-dec-2019
	//start manish
	public String checkDecimal(double quantity, String unit,Connection conn ) throws ITMException
	{
		String errCode="";
		try {
			String[] splitter = String.valueOf(quantity).split("\\.");
			splitter[0].length(); 
			int decimalLength = splitter[1].length();
			String sql="";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			int decPlaces=0;

			sql="Select dec_opt from uom where unit = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, unit);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				decPlaces=rs.getInt("dec_opt");
			}
			//decval for if decimal 0 there are skip the decimal value
			int decVal = Integer.parseInt(splitter[1] == null ? "0": splitter[1]);
			System.out.println("decimalLength["+decimalLength+"]unit["+unit+"]places["+decPlaces+"]"+"]decval["+decVal+"]");	
			if(decVal > 0)
			{
				if(decPlaces > 0)
				{
					if (decimalLength > decPlaces)
					{  
						errCode="VTUOMDEC3";
					}	
				}
				else if(decimalLength > 3)
				{  
					errCode="VTUOMDEC3";
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}catch(Exception e)
		{
			System.out.println("checkDecimal(double, int)::>>" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errCode;
	}
	//end manish

	//added by manish mhatre on 3-nov-20
	//start manish
	public JSONObject GetItemAttributes(JSONObject itemTypeJson,Connection conn) throws ITMException
	{
		JSONObject attributes=new JSONObject();
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String phyAttrib1="",phyAttrib2="",phyAttrib3="",phyAttrib4="",phyAttrib5="",phyAttrib6="",itemType="",itemTypeDescr="",itemCode="",focusCol="",itemDescr="";
		int noOfAttributes=0;
		String phyAttribval1="",phyAttribval2="",phyAttribval3="",phyAttribval4="",phyAttribval5="",phyAttribval6="";
		try
		{
			System.out.println("In distcommon GetItemAttributes" + itemTypeJson);
			//itemCode = itemTypeJson.getString("item_code");
			//itemType = itemTypeJson.getString("item_type");

			System.out.println("In distcommon GetItemAttributes itemCode" + itemCode);
			focusCol = itemTypeJson.getString("focusColumn");
			System.out.println("Focused Column>>>>>" + focusCol);

			if("item_code".equalsIgnoreCase(focusCol))
			{
				itemCode = itemTypeJson.getString("item_code");
				//itemType = itemTypeJson.getString("item_type");
				if(itemCode!=null && itemCode.trim().length()>0)
				{
					sql="select item_type,descr from item where item_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						itemType=rs.getString("item_type");
						itemDescr=rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("Item code not null Item Type>>" +itemType);
					attributes.put("item_type",itemType);
					itemDescr = itemDescr == null ? "" : itemDescr.trim();   //added on 17-feb-2021
					System.out.println("Item descr >>" +itemDescr);
					attributes.put("descr",itemDescr);

					sql="select descr from item_type where item_type= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						itemTypeDescr=rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("Item code not null Item Type Description>>" +itemTypeDescr);
					attributes.put("item_type_descr",itemTypeDescr);

					//for attribute names (attribute get from item_type master)
					sql=" Select phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4,phy_attrib_5,phy_attrib_6 from item_type where item_type = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						phyAttrib1=rs.getString("phy_attrib_1");
						phyAttrib2=rs.getString("phy_attrib_2");
						phyAttrib3=rs.getString("phy_attrib_3");
						phyAttrib4=rs.getString("phy_attrib_4");
						phyAttrib5=rs.getString("phy_attrib_5");
						phyAttrib6=rs.getString("phy_attrib_6");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					//for attribute values
					sql=" Select phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4,phy_attrib_5,phy_attrib_6 from item where item_code = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						phyAttribval1=rs.getString("phy_attrib_1");
						phyAttribval2=rs.getString("phy_attrib_2");
						phyAttribval3=rs.getString("phy_attrib_3");
						phyAttribval4=rs.getString("phy_attrib_4");
						phyAttribval5=rs.getString("phy_attrib_5");
						phyAttribval6=rs.getString("phy_attrib_6");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(phyAttribval1!=null && phyAttribval1.trim().length()>0)
					{
						attributes.put(phyAttrib1,phyAttribval1);
						noOfAttributes++;
					}
					if(phyAttribval2!=null && phyAttribval2.trim().length()>0)
					{
						attributes.put(phyAttrib2,phyAttribval2);
						noOfAttributes++;
					}
					if(phyAttribval3!=null && phyAttribval3.trim().length()>0)
					{
						attributes.put(phyAttrib3,phyAttribval3);
						noOfAttributes++;
					}
					if(phyAttribval4!=null && phyAttribval4.trim().length()>0)
					{
						attributes.put(phyAttrib4,phyAttribval4);
						noOfAttributes++;
					}
					if(phyAttribval5!=null && phyAttribval5.trim().length()>0)
					{
						attributes.put(phyAttrib5,phyAttribval5);
						noOfAttributes++;
					}
					if(phyAttribval6!=null && phyAttribval6.trim().length()>0)
					{
						attributes.put(phyAttrib6,phyAttribval6);
						noOfAttributes++;
					}

					System.out.println("attributes in json 11255>>" +attributes);
					System.out.println("attributes in string 11256>>" +attributes.toString());
					System.out.println(" In item code not null phyAttribval1>>" +phyAttribval1+"phyAttribval2>>"+phyAttribval2+"phyAttribval3>>"+phyAttribval3+"phyAttribval4>>"+phyAttribval4+"phyAttribval5>>"+phyAttribval5+"phyAttribval6>>"+phyAttribval6);
					System.out.println("In item code not null noOfAttributes>>" +noOfAttributes);
					attributes.put("no_of_attributes",noOfAttributes);


					System.out.println("attributes json object in string 11241>>" +attributes.toString());
					System.out.println("attributes json object  11242>>" +attributes);
				}  //item code not null end
				else
				{
					itemDescr= "";   //added on 17-feb-2021
					System.out.println("Item descr >>" +itemDescr);
					attributes.put("descr",itemDescr);

					//itemTypeDescr = "";
					//System.out.println("Item code not null Item Type Description>>" +itemTypeDescr);
					//attributes.put("item_type_descr",itemTypeDescr);

				}
			} //focuscol item code

			if("item_type".equalsIgnoreCase(focusCol))
			{
				itemType = itemTypeJson.getString("item_type");
				if(itemType!=null && itemType.trim().length()>0)
				{
					System.out.println("inside itemtype AND item code condition 11242>>");
					itemType = itemTypeJson.getString("item_type");
					System.out.println("In distcommon GetItemAttributes item code null itemType" + itemType);
					sql=" Select phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4,phy_attrib_5,phy_attrib_6 from item_type where item_type = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						phyAttrib1=rs.getString("phy_attrib_1");
						phyAttrib2=rs.getString("phy_attrib_2");
						phyAttrib3=rs.getString("phy_attrib_3");
						phyAttrib4=rs.getString("phy_attrib_4");
						phyAttrib5=rs.getString("phy_attrib_5");
						phyAttrib6=rs.getString("phy_attrib_6");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					if(phyAttrib1!=null && phyAttrib1.trim().length()>0)
					{
						attributes.put("phy_attrib_1",phyAttrib1);
						noOfAttributes++;
					}

					if(phyAttrib2!=null && phyAttrib2.trim().length()>0)
					{
						attributes.put("phy_attrib_2",phyAttrib2);
						noOfAttributes++;
					}

					if(phyAttrib3!=null && phyAttrib3.trim().length()>0)
					{
						attributes.put("phy_attrib_3",phyAttrib3);
						noOfAttributes++;
					}

					if(phyAttrib4!=null && phyAttrib4.trim().length()>0)
					{
						attributes.put("phy_attrib_4",phyAttrib4);
						noOfAttributes++;
					}

					if(phyAttrib5!=null && phyAttrib5.trim().length()>0)
					{
						attributes.put("phy_attrib_5",phyAttrib5);
						noOfAttributes++;
					}

					if(phyAttrib6!=null && phyAttrib6.trim().length()>0)
					{
						attributes.put("phy_attrib_6",phyAttrib6);
						noOfAttributes++;
					}
					System.out.println("phyAttrib1>>" +phyAttrib1+"phyAttrib2>>"+phyAttrib2+"phyAttrib3>>"+phyAttrib3+"phyAttrib4>>"+phyAttrib4+"phyAttrib5>>"+phyAttrib5+"phyAttrib6>>"+phyAttrib6);
					System.out.println("noOfAttributes>>" +noOfAttributes);
					attributes.put("no_of_attributes",noOfAttributes);

					sql="select descr from item_type where item_type= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemType);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						itemTypeDescr=rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					itemTypeDescr = itemTypeDescr == null ? "" : itemTypeDescr.trim();   //added on 17-feb-2021

					System.out.println("Item Type Description>>" +itemTypeDescr);
					attributes.put("item_type_descr",itemTypeDescr);

					System.out.println("attributes json object in string 11340>>" +attributes.toString());
					System.out.println("attributes json object  11341>>" +attributes);
				}//if end
				else
				{
					itemTypeDescr ="";

					System.out.println("Item Type Description>>" +itemTypeDescr);
					attributes.put("item_type_descr",itemTypeDescr);

				}
			}  //focuscol item_type
		}
		catch(Exception e)
		{
			System.out.println("Inside getattributes>>" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}

		return attributes;
	}


	public JSONObject GetItemByAttribute(JSONObject itemAttributesJson,Connection conn) throws ITMException
	{
		String itemCode="",itemDescr="";
		JSONObject itemData=new JSONObject();
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String errCode="";
		String attrib1="",attrib2="",attrib3="",attrib4="",attrib5="",attrib6="";
		String attrib1value="",attrib2value="",attrib3value="",attrib4value="",attrib5value="",attrib6value="";
		try
		{
			String itemType = itemAttributesJson.getString("item_type");
			System.out.println("In GetItemByAttribute itemAttributesJson in string"+itemAttributesJson.toString());
			System.out.println("In GetItemByAttribute itemAttributesJson in json"+itemAttributesJson);
			int noOfAttributes= itemAttributesJson.getInt("no_of_attributes");
			System.out.println("noattr 11383>>>"+noOfAttributes);

			//added by  manish mhatre on 2-dec-20
			sql=" Select phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4,phy_attrib_5,phy_attrib_6 from item_type where item_type = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemType);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				attrib1=rs.getString("phy_attrib_1");
				attrib2=rs.getString("phy_attrib_2");
				attrib3=rs.getString("phy_attrib_3");
				attrib4=rs.getString("phy_attrib_4");
				attrib5=rs.getString("phy_attrib_5");
				attrib6=rs.getString("phy_attrib_6");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("11358 phyAttrib1>>" +attrib1+"phyAttrib2>>"+attrib2+"phyAttrib3>>"+attrib3+"phyAttrib4>>"+attrib4+"phyAttrib5>>"+attrib5+"phyAttrib6>>"+attrib6);

			if(noOfAttributes==1)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
			}
			if(noOfAttributes==2)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
			}
			if(noOfAttributes==3)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
			}
			if(noOfAttributes==4)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
				attrib4value = itemAttributesJson.getString(attrib4);
			}
			if(noOfAttributes==5)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
				attrib4value = itemAttributesJson.getString(attrib4);
				attrib5value = itemAttributesJson.getString(attrib5);
			}
			if(noOfAttributes==6)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
				attrib4value = itemAttributesJson.getString(attrib4);
				attrib5value = itemAttributesJson.getString(attrib5);
				attrib6value = itemAttributesJson.getString(attrib6);
			}

			System.out.println("11358 attribute values phyAttrib1>>" +attrib1value+"phyAttrib2>>"+attrib2value+"phyAttrib3>>"+attrib3value+"phyAttrib4>>"+attrib4value+"phyAttrib5>>"+attrib5value+"phyAttrib6>>"+attrib6value);
			System.out.println("11359 itemCode>>" +itemCode);
			//end manish

			sql="Select item_code,descr from item where item_type = ? ";
			if(noOfAttributes==1)
			{  
				sql=sql +" and phy_attrib_1=  '"+attrib1value+"'";
			}
			else if(noOfAttributes==2)
			{
				sql=sql+ " and phy_attrib_1=  '"+attrib1value+"' and phy_attrib_2=  '"+attrib2value+"'";
			}
			else if(noOfAttributes==3)
			{
				sql=sql+" and phy_attrib_1=  '"+attrib1value+"' and phy_attrib_2=  '"+attrib2value+"' and phy_attrib_3=  '"+attrib3value+"'";
			}
			else if(noOfAttributes==4)
			{
				sql=sql+" and phy_attrib_1=  '"+attrib1value+"' and phy_attrib_2=  '"+attrib2value+"' and phy_attrib_3=  '"+attrib3value+"'and phy_attrib_4=  '"+attrib4value+"'";
			}
			else if(noOfAttributes==5)
			{
				sql=sql+" and phy_attrib_1=  '"+attrib1value+"' and phy_attrib_2=  '"+attrib2value+"' and phy_attrib_3=  '"+attrib3value+"'and phy_attrib_4=  '"+attrib4value+"'and phy_attrib_5=  '"+attrib5value+"'";
			}
			else if(noOfAttributes==6)
			{
				sql=sql+" and phy_attrib_1=  '"+attrib1value+"' and phy_attrib_2=  '"+attrib2value+"' and phy_attrib_3=  '"+attrib3value+"'and phy_attrib_4=  '"+attrib4value+"'and phy_attrib_5=  '"+attrib5value+"' and phy_attrib_6=  '"+attrib6value+"'";
			}

			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemType);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				itemCode=rs.getString("item_code");
				itemDescr=rs.getString("descr");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(itemCode==null || itemCode.trim().length()==0)
			{
				errCode="Item Code Not found in master";
				itemCode="";
				itemDescr="";
			}
			itemData.put("item_code",itemCode);
			itemData.put("descr",itemDescr);

			itemData.put("error",errCode);
		}
		catch(Exception e)
		{
			System.out.println("Inside getattributes>>" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}

		return itemData;
	}

	public JSONObject CreateItemByAtribute(JSONObject itemAttributesJson,Connection conn) throws ITMException
	{

		String itemCode="",itemDescr="";
		JSONObject itemData=new JSONObject();
		String sql="",insertcolsql="",insertvalsql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String errCode="";
		String itemSer="",unit="",locCode="",unitRate="",siteCode="",siteShortDescr="",itemtypeDescr="",ItemDescription="",genratedItemCode="",remarks="";
		String grpCode="",sgrpCode="",itemStru="",locType="",specTol="",unitPur="";
		String chgUser="",chgTerm="";
		int cnt=0,count=0;
		String attrib1="",attrib2="",attrib3="",attrib4="",attrib5="",attrib6="";
		String attrib1value="",attrib2value="",attrib3value="",attrib4value="",attrib5value="",attrib6value="";
		try
		{
			java.util.Date currentDate = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			Timestamp newsysDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");

			String itemType = itemAttributesJson.getString("item_type");
			int noOfAttributes= itemAttributesJson.getInt("no_of_attributes");

			//added by  manish mhatre on 2-dec-20
			sql=" Select phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4,phy_attrib_5,phy_attrib_6 from item_type where item_type = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemType);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				attrib1=rs.getString("phy_attrib_1");
				attrib2=rs.getString("phy_attrib_2");
				attrib3=rs.getString("phy_attrib_3");
				attrib4=rs.getString("phy_attrib_4");
				attrib5=rs.getString("phy_attrib_5");
				attrib6=rs.getString("phy_attrib_6");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("11358 phyAttrib1>>" +attrib1+"phyAttrib2>>"+attrib2+"phyAttrib3>>"+attrib3+"phyAttrib4>>"+attrib4+"phyAttrib5>>"+attrib5+"phyAttrib6>>"+attrib6);

			if(noOfAttributes==1)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
			}
			if(noOfAttributes==2)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
			}
			if(noOfAttributes==3)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
			}
			if(noOfAttributes==4)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
				attrib4value = itemAttributesJson.getString(attrib4);
			}
			if(noOfAttributes==5)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
				attrib4value = itemAttributesJson.getString(attrib4);
				attrib5value = itemAttributesJson.getString(attrib5);
			}
			if(noOfAttributes==6)
			{
				attrib1value = itemAttributesJson.getString(attrib1);
				attrib2value = itemAttributesJson.getString(attrib2);
				attrib3value = itemAttributesJson.getString(attrib3);
				attrib4value = itemAttributesJson.getString(attrib4);
				attrib5value = itemAttributesJson.getString(attrib5);
				attrib6value = itemAttributesJson.getString(attrib6);
			}

			System.out.println("11595 attribute values phyAttrib1>>" +attrib1value+"phyAttrib2>>"+attrib2value+"phyAttrib3>>"+attrib3value+"phyAttrib4>>"+attrib4value+"phyAttrib5>>"+attrib5value+"phyAttrib6>>"+attrib6value);

			//end manish			
			sql = "select descr,chg_user,chg_term from item_type WHERE item_type = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, itemType );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				itemtypeDescr = rs.getString(1);
				chgUser=rs.getString(2);
				chgTerm=rs.getString(3);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			ItemDescription =  itemtypeDescr +"-"+attrib1value + attrib2value + attrib3value + attrib4value + attrib5value + attrib6value;    


			sql= "select item_ser,grp_code,sgrp_code,unit,loc_code,site_code__own,item_stru,loc_type,unit__rate,spec_tol,unit__pur from item where item_type= ? ";        
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, itemType );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				itemSer=rs.getString("item_ser");
				grpCode=rs.getString("grp_code");
				sgrpCode=rs.getString("sgrp_code");
				unit=rs.getString("unit");
				locCode=rs.getString("loc_code");
				siteCode=rs.getString("site_code__own");
				itemStru=rs.getString("item_stru");
				locType=rs.getString("loc_type");
				unitRate=rs.getString("unit__rate");
				specTol=rs.getString("spec_tol");
				unitPur=rs.getString("unit__pur");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(itemSer==null ||itemSer.trim().length()==0)
			{
				itemSer="FG01";
			}

			genratedItemCode = generateItemCode( "w_item",itemType, attrib1value,attrib2value,attrib3value,attrib4value,attrib5value,attrib6value,siteCode, conn );
			remarks="Auto generated Item Code"+genratedItemCode;

			insertcolsql = " Insert into ITEM (ITEM_CODE,DESCR,SH_DESCR,ITEM_SER,GRP_CODE,SGRP_CODE,UNIT,ITEM_TYPE,LOC_CODE,ACTIVE,LOC_TYPE,CHG_DATE,CHG_USER,CHG_TERM,ITEM_USAGE,REMARKS,SITE_CODE__OWN,SITE_CODE,ITEM_STRU,UNIT__RATE,SPEC_TOL,UNIT__PUR"; 
			insertvalsql = " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";

			if(noOfAttributes>=1)
			{ 
				insertcolsql =  insertcolsql + ",PHY_ATTRIB_1 " ;
				insertvalsql = insertvalsql + ",'" + attrib1value +"'" ;
			}
			if(noOfAttributes>=2)
			{ 
				insertcolsql =  insertcolsql + ",PHY_ATTRIB_2  " ;
				insertvalsql = insertvalsql + ",'"+attrib2value+"' " ;
			}
			if(noOfAttributes>=3)
			{ 
				insertcolsql =  insertcolsql + ",PHY_ATTRIB_3  " ;
				insertvalsql = insertvalsql + ",'"+attrib3value+"' " ;
			}
			if(noOfAttributes>=4)
			{ 
				insertcolsql =  insertcolsql + ",PHY_ATTRIB_4  " ;
				insertvalsql = insertvalsql + ",'"+attrib4value+"' " ;
			}
			if(noOfAttributes>=5)
			{ 
				insertcolsql =  insertcolsql + ",PHY_ATTRIB_5  " ;
				insertvalsql = insertvalsql + ",'"+attrib5value+"' " ;
			}
			if(noOfAttributes>=6)
			{ 
				insertcolsql =  insertcolsql + ",PHY_ATTRIB_6  " ;
				insertvalsql = insertvalsql + ",'"+attrib6value+"' " ;
			}

			insertcolsql =  insertcolsql + ")";
			insertvalsql = insertvalsql + ")";

			sql=insertcolsql+insertvalsql;
			System.out.println("Insert sql 11498>>" +sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,genratedItemCode);
			pstmt.setString(2,ItemDescription); 
			pstmt.setString(3,itemtypeDescr);
			pstmt.setString(4,itemSer);
			pstmt.setString(5,grpCode); 
			pstmt.setString(6,sgrpCode);  
			pstmt.setString(7,unit);      
			pstmt.setString(8,itemType);
			pstmt.setString(9,locCode);
			pstmt.setString(10,"Y");
			pstmt.setString(11,locType);
			pstmt.setTimestamp(12,newsysDate);
			pstmt.setString(13,chgUser);
			pstmt.setString(14,chgTerm);
			pstmt.setString(15,"F");  
			pstmt.setString(16,remarks);
			pstmt.setString(17,siteCode);
			pstmt.setString(18,siteCode);
			pstmt.setString(19,itemStru);
			pstmt.setString(20,unitRate);
			pstmt.setString(21,specTol);
			pstmt.setString(22,unitPur);

			cnt = pstmt.executeUpdate();
			System.out.println("Insert count for new item" + cnt);
			pstmt.close();
			pstmt = null;
			conn.commit();


			itemData.put("item_code",genratedItemCode);
			itemData.put("descr",ItemDescription);
			System.out.println("Itemdata in distcommon in tostring>>" + itemData.toString());
			System.out.println("Itemdata in distcommon >>" + itemData);

		}catch(Exception e)
		{
			System.out.println("Inside getattributes>>" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}

		return itemData;
	}

	//For item code generator
	private String generateItemCode( String windowName, String itemType, String Attribute1,String Attribute2,String Attribute3,String Attribute4,String Attribute5,String Attribute6, String siteCode, Connection conn )throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String itemCode = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		String docpreFix="";

		try
		{

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);

			pstmt=conn.prepareStatement("select  case when doc_prefix is null then '' else  doc_prefix end from site where site_code=?" );
			pstmt.setString(1,siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				docpreFix=(rs.getString(1)).trim();
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<item_type>" + itemType + "</item_type>";
			xmlValues = xmlValues +        "<phy_attrib_1>" + Attribute1 + "</phy_attrib_1>";
			xmlValues = xmlValues +        "<phy_attrib_2>" + Attribute2 + "</phy_attrib_2>";
			xmlValues = xmlValues +        "<phy_attrib_3>" + Attribute3 + "</phy_attrib_3>";
			xmlValues = xmlValues +        "<phy_attrib_4>" + Attribute4 + "</phy_attrib_4>";
			xmlValues = xmlValues +        "<phy_attrib_5>" + Attribute5 + "</phy_attrib_5>";
			xmlValues = xmlValues +        "<phy_attrib_6>" + Attribute6 + "</phy_attrib_6>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);

			itemCode = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("itemCode :"+itemCode);
		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +selSql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
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
			}
			catch(Exception e){}
		}
		return itemCode;
	}//generateTranTd()

	//end manish

	//addded by manish mhatre on 17-3-2021[For calculate qty from dimension and no of articles]
	//start manish
	public double getQuantity(String dimension, double noArt,String unit, Connection conn) throws ITMException 
	{
		double quantity=0,length=0,width=0,height=0;
		String lengthstr="",widthstr = "",heightstr ="";

		try 
		{
			System.out.println("dimension>>"+dimension+"\nno of art>>"+noArt+"\nunit>>"+unit);
			if(noArt==0)
			{
				noArt=1;  //if no of articles are not entered then consider as 1
			}

			if(dimension!=null && dimension.trim().length()>0)
			{
				String dimensionArray[] = dimension.split("X");
				System.out.println("@@@@@ dimensionArray.length[" + dimensionArray.length + "]");
				if (dimensionArray.length == 0) 
				{
					lengthstr="";
					widthstr = "";
					heightstr ="";
					System.out.println(" inside if lengthstr"+lengthstr+"\nwidthstr"+widthstr+"\nheightstr"+heightstr);
				}
				else if (dimensionArray.length == 1) 
				{
					lengthstr= dimensionArray[0];
					widthstr = "";
					heightstr ="";
					System.out.println(" inside else if lengthstr"+lengthstr+"\nwidthstr"+widthstr+"\nheightstr"+heightstr);
				}
				else if (dimensionArray.length == 2) 
				{
					lengthstr = dimensionArray[0];
					widthstr = dimensionArray[1];
					heightstr="";
					System.out.println(" inside 2nd else if lengthstr"+lengthstr+"\nwidthstr"+widthstr+"\nheightstr"+heightstr);
				}
				else if (dimensionArray.length >= 3) 
				{
					lengthstr = dimensionArray[0];
					widthstr = dimensionArray[1];
					heightstr=dimensionArray[2];
					System.out.println(" inside 3rd else if lengthstr"+lengthstr+"\nwidthstr"+widthstr+"\nheightstr"+heightstr);
				}

				if(lengthstr!=null && lengthstr.trim().length()>0)
				{
                    lengthstr = lengthstr.replaceAll("[a-zA-Z]","");     //ignoring the alphabet
				    System.out.println("lengthstr replace all>>>>"+lengthstr);
					length=Double.parseDouble(lengthstr);
				}
				else
				{
					length=0;  //if length not entered then considered as 0
				}

				if(widthstr!=null && widthstr.trim().length()>0)
				{
                    widthstr = widthstr.replaceAll("[a-zA-Z]","");  //ignoring the alphabet
				    System.out.println("widthstr replace all>>>>"+widthstr);
					width=Double.parseDouble(widthstr);
				}
				else
				{
					width=0;   //if width not entered then considered as 0
				}

				if(heightstr!=null && heightstr.trim().length()>0)
				{
                    heightstr = heightstr.replaceAll("[a-zA-Z]","");  //ignoring the alphabet
				    System.out.println("heightstr replace all>>>>"+heightstr);
					height=Double.parseDouble(heightstr);
				}
				else
				{
					height=0;  //if height not entered then considered as 0
				}

				System.out.println("length"+length+"\nwidth"+width+"\nheight"+height);
				System.out.println("unit>>>>"+unit);

				if("CFT".equalsIgnoreCase(unit)) 
				{
					quantity=((length*width*height) * noArt) / 144 ;
				}

				else if("SQM".equalsIgnoreCase(unit)) 
				{
					quantity=(length*width) * noArt;
				}
				System.out.println("quantity in distcommon before rounding>>>"+quantity);
    
                quantity=Double.parseDouble(UtilMethods.getInstance().getReqDecString(quantity, 3));  //rounding the amount

				System.out.println("quantity in distcommon after rounding>>>"+quantity);
			}

		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return quantity;

	}
	//end manish

}// class
