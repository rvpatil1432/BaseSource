package ibase.webitm.ejb.dis;

import java.sql.*;
import java.util.*;
import org.w3c.dom.*;
import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.sys.*;
import ibase.webitm.ejb.dis.*;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless;

@Stateless
public class InvPackUndo extends ProcessEJB implements InvPackUndoLocal,
		InvPackUndoRemote {
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String process(String xmlString, String xmlString2,
			String windowName, String xtraParams) throws RemoteException,
			ITMException {

		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		System.out.println("xmlString2-->" + xmlString2);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try {

			System.out.println("xmlString   [" + xmlString + "]");
			System.out.println("xmlString2   [" + xmlString2 + "]");
			System.out.println("windowName   [" + windowName + "]");
			System.out.println("xtraParams   [" + xtraParams + "]");
			if (xmlString != null && xmlString.trim().length() != 0) {
				headerDom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				detailDom = genericUtility.parseString(xmlString2);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}

	public String process(Document headerDom, Document detailDom,
			String windowName, String xtraParams) throws RemoteException,
			ITMException {
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null, pstmt2 = null, pstmt4 = null, pstmt5 = null, pstmtInsert = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs4 = null;
		ResultSet rs5 = null;
		String tranId = null;
		String confirm = "";
		String errString = "";
		String chgUser = "";
		String userId = null;
		String chgTerm = "";
		String lineno = "";
		String linenorcp = "";
		String itemcode = "";
		String itemcodercp = "";
		String loccode = "";
		String loccodercp = "";
		String lotno = "";
		String lotnorcp = "";
		String lotsl = "", unit = "", orderno = "", packcode = "", packinstr = "", acctcodeinv = "", cctrcodeinv = "", grade = "", linenoord = "";
		String lotslrcp = "", unitrcp = "", ordernorcp = "", packcodercp = "", packinstrrcp = "", acctcodeinvrcp = "", cctrcodeinvrcp = "", gradercp = "", linenoordrcp = "";
		boolean result = false;
		boolean updateFlag = false;
		String[] douhold = null;
		String loccodeiss = "";
		String lslotsliss = "";
		String sql = null;
		String siteCode = "", oldcompstr = "";
		String xmlString = "", sitecodemfg = "", unitalt = "", loccodeexcessshort = "";
		double grossweight = 0, tareweight = 0, netweight = 0, potencyperc = 0, rate = 0, grossrate = 0;
		double quantityrcp = 0, grossweightrcp = 0, tareweightrcp = 0, netweightrcp = 0, potencypercrcp = 0, ratercp = 0, grossratercp = 0;
		double convqtystduom = 0, holdqty = 0, exshtqty = 0, palletwt = 0;
		int count = 0, noart = 0, i = 0, noartrcp = 0, newSl = 0;
		HashMap invrevUpdMap = null;
		HashMap invrevUpdMap1 = null;
		String newLotSl = "", dimensionrcp = "";
		java.sql.Timestamp dueDate = null, mfgDate = null, mfgDatercp = null, expDate = null, expDatercp = null, retestdate = null, mfgDateWO = null, expDateWO = null, completionDate = null, trandate = null, lastphycdate = null;
		// oracle.jdbc.OracleConnection oracleConnection = null; change done by
		// kunal on 12/Sep/13
		String sitecodemfgrcp = "", retestdatercp = "";
		String suppcodemfg = "";
		double rateoh = 0;
		String acctcodeoh = "";
		String cctrcodeoh = "";
		String unitaltercp = "";
		double conv = 0;
		double sumqty = 0;
		String batchno = "";
		String quarantineLock = "";
		double actualrate = 0, batchsize = 0;
		String sitecodemfgrcpstk = "";
		java.sql.Timestamp retestdatercpstk = null;
		double potencypercrcpstk = 0, convqtystduomrcpstk = 0, actualratercpstk = 0, batchsizercpstk = 0;
		String batchnorcpstk = "", unitaltrcpstk = "";
		int ratercpstk = 0, rateohrcpstk = 0, grossratercpstk = 0, checkCount = 0;
		String suppcodemfgrcpstk = "", acctcodeinvrcpstk = "", cctrcodeinvstk = "", acctcodeohrcpstk = "", cctrcodeohrcpstk = "", gradercpstk = "";
		String linenotemp = "", itemcodetemp = "", loccodetemp = "", lotnotemp = "", lotsltemp = "";
		double quantitytemp = 0, alocqty = 0, stkQty = 0, checkqty = 0;
		double quantityiss = 0;
		try {
			System.out.println("Enter in process ======================");
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			// oracleConnection = new
			// ConnDriver().getOracleConnection("DriverITM");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,
					"loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,
					"termId");
			userId = chgUser;
			DistCommon distCommon = new DistCommon();
			invrevUpdMap = new HashMap();
			invrevUpdMap1 = new HashMap();
			ibase.webitm.ejb.dis.StockUpdate stkUpd = new ibase.webitm.ejb.dis.StockUpdate();
			tranId = genericUtility.getColumnValue("tran_id", headerDom);
			System.out.println("tran_id========" + tranId);
			sql = "select count(1) from inv_pack where tran_id=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				checkCount = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if ((tranId == null || tranId.trim().length() == 0 || checkCount == 0)) {
				System.out.println("NOt COnfirm");
				errString = "VTTRNUNDO";
				errString = itmDBAccessEJB.getErrorString("", errString, "",
						"", conn);
				return errString;

			}
			sql = "select confirmed,site_code,tran_date from inv_pack where tran_id=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				confirm = rs.getString("confirmed");
				siteCode = rs.getString("site_code");
				trandate = rs.getTimestamp("tran_date");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (confirm.equalsIgnoreCase("Y")) {
				System.out.println("Tran id is confirmed");
				sql = "select line_no,item_code,loc_code,lot_no,lot_sl, quantity from  inv_pack_rcp where tran_id =  ?";
				pstmt4 = conn.prepareStatement(sql);
				pstmt4.setString(1, tranId);
				rs4 = pstmt4.executeQuery();
				while (rs4.next()) {
					linenotemp = rs4.getString(1);
					itemcodetemp = rs4.getString(2);
					loccodetemp = rs4.getString(3);
					lotnotemp = rs4.getString(4);
					lotsltemp = rs4.getString(5);
					quantitytemp = rs4.getDouble(6);
					System.out.println("QUANTITY IS " + quantitytemp);
					sql = "select alloc_qty,quantity from stock where site_code = ? and item_code = ? and loc_code  = ? and lot_no    = ? and lot_sl    = ?";
					pstmt5 = conn.prepareStatement(sql);
					pstmt5.setString(1, siteCode);
					pstmt5.setString(2, itemcodetemp);
					pstmt5.setString(3, loccodetemp);
					pstmt5.setString(4, lotnotemp);
					pstmt5.setString(5, lotsltemp);
					rs5 = pstmt5.executeQuery();
					if (rs5.next()) {
						alocqty = rs5.getDouble(1);
						stkQty = rs5.getDouble(2);
						System.out.println("ALLOC QTY is" + alocqty);
						System.out.println("STOCK QTY is" + stkQty);

					}
					rs5.close();
					rs5 = null;
					pstmt5.close();
					pstmt5 = null;
					checkqty = stkQty - alocqty;

					sql = "select    sum(a.quantity - a.alloc_qty - case when a.hold_qty is null then 0 else a.hold_qty end) from    stock a, location b, invstat c  w"
							+ "here a.loc_code = b.loc_code and b.inv_stat = c.inv_stat and a.site_code = ? and a.item_code = ? and a.loc_code  = ?"
							+ " and a.lot_no    = ? and a.lot_sl    = ? and"
							+ " c.stat_type <> 'S'";
					pstmt5 = conn.prepareStatement(sql);
					pstmt5.setString(1, siteCode);
					pstmt5.setString(2, itemcodetemp);
					pstmt5.setString(3, loccodetemp);
					pstmt5.setString(4, lotnotemp);
					pstmt5.setString(5, lotsltemp);
					rs5 = pstmt5.executeQuery();
					if (rs5.next()) {
						sumqty = rs5.getDouble(1);
						System.out.println("SUMQTY is" + sumqty);

					}
					rs5.close();
					rs5 = null;
					pstmt5.close();
					pstmt5 = null;

					System.out.println("QTy1" + quantitytemp + "QTY2"
							+ checkqty);
					if (sumqty >= quantitytemp && quantitytemp == checkqty) {
						System.out.println("Not issue");
					} else {
						System.out.println("NOt confirm");
						errString = "VTINVUNDO";
						errString = itmDBAccessEJB.getErrorString("",
								errString, "", "", conn);
						return errString;
					}
				}
				rs4.close();
				rs4 = null;
				pstmt4.close();
				pstmt4 = null;

				quarantineLock = distCommon.getDisparams("999999",
						"QUARNTINE_LOCKCODE", conn);
				System.out.println("quarantineLock:::::" + quarantineLock);
				sql = "select line_no,item_code,loc_code,lot_no,lot_sl,quantity from  inv_pack_iss where tran_id =  ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranId);
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {

					lineno = rs1.getString("line_no");
					itemcode = rs1.getString("item_code");
					loccode = rs1.getString("loc_code");
					lotno = rs1.getString("lot_no");
					lotsl = rs1.getString("lot_sl");
					quantityiss = rs1.getDouble("quantity");
					System.out.println("Line_no is" + linenorcp);
					System.out.println("Item code is" + itemcode);
					System.out.println("Location code is" + loccode);
					System.out.println("Lot no  is" + lotno);
					System.out.println("Lot sl is" + lotsl);
					System.out.println("@@@@@@@@@Quantity @@@@@@@@@ "
							+ quantityiss);

					sql = "select order_no,line_no__ord,unit, "
							+ "pack_code,pack_instr, "
							+ " gross_weight, tare_weight,net_weight,loc_code__excess_short, nvl(excess_short_qty,0),no_art "
							+ "from inv_pack_iss where tran_id = ? and line_no=? and item_code=? and loc_code=? and lot_no=? and lot_sl = ? and quantity =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					pstmt.setString(2, lineno);
					pstmt.setString(3, itemcode);
					pstmt.setString(4, loccode);
					pstmt.setString(5, lotno);
					pstmt.setString(6, lotsl);
					pstmt.setDouble(7, quantityiss);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						// lineno = rs.getString(1);
						orderno = rs.getString(1);
						linenoord = rs.getString(2);
						// itemcode = rs.getString(4);
						unit = rs.getString(3);
						// loccode = rs.getString(6);
						packcode = rs.getString(4);
						packinstr = rs.getString(5);
						// lotno = rs.getString(9);
						// lotsl = rs.getString(10);
						grossweight = rs.getDouble(6);
						tareweight = rs.getDouble(7);
						netweight = rs.getDouble(8);
						loccodeexcessshort = rs.getString(9);
						exshtqty = rs.getDouble(10);
						noart = rs.getInt(11);
						// quantity=rs.getDouble(12);

						System.out.println("Exchange Quantity is: " + exshtqty);
						sql = "select site_code__mfg,mfg_date,exp_date,retest_date,potency_perc, rate,gross_rate,"
								+ " unit__alt, conv__qty_stduom, acct_code__inv, cctr_code__inv, grade , hold_qty"
								+ ",BATCH_SIZE,last_phyc_date "
								+ "from stock where site_code = ? and item_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, siteCode);
						pstmt2.setString(2, itemcode);
						pstmt2.setString(3, loccode);
						pstmt2.setString(4, lotno);
						pstmt2.setString(5, lotsl);
						rs2 = pstmt2.executeQuery();
						if (rs2.next()) {
							sitecodemfg = rs2.getString("site_code__mfg");
							mfgDate = rs2.getTimestamp("mfg_date");
							expDate = rs2.getTimestamp("exp_date");
							retestdate = rs2.getTimestamp("retest_date");
							potencyperc = rs2.getDouble("potency_perc");
							rate = rs2.getDouble("rate");
							grossrate = rs2.getDouble("gross_rate");
							unitalt = rs2.getString("unit__alt");
							convqtystduom = rs2.getDouble("conv__qty_stduom");
							acctcodeinv = rs2.getString("acct_code__inv");
							cctrcodeinv = rs2.getString("cctr_code__inv");
							grade = rs2.getString("grade");
							holdqty = rs2.getDouble("hold_qty");
							batchsize = rs2.getDouble("BATCH_SIZE");
							lastphycdate = rs2.getTimestamp("last_phyc_date");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;

						invrevUpdMap.put("acct_code__inv", acctcodeinv);
						invrevUpdMap.put("cctr_code__inv", cctrcodeinv);
						invrevUpdMap.put("grade", grade);
						invrevUpdMap.put("tran_date", trandate);
						invrevUpdMap.put("tran_id", tranId);
						invrevUpdMap.put("tran_ser", "I-PKR");
						invrevUpdMap.put("sorder_no", orderno);
						invrevUpdMap.put("item_code", itemcode);
						invrevUpdMap.put("site_code", siteCode);
						invrevUpdMap.put("loc_code", loccode);
						invrevUpdMap.put("unit", unit);
						invrevUpdMap.put("lot_no", lotno);
						invrevUpdMap.put("lot_sl", lotsl);
						System.out.println("QUANTITY IN MAP ######### "
								+ quantityiss);
						invrevUpdMap.put("quantity", quantityiss);
						System.out.println("QTY STDUOM");
						invrevUpdMap.put("qty_stduom", quantityiss);
						invrevUpdMap.put("rate", rate);
						invrevUpdMap.put("gross_rate", grossrate);
						invrevUpdMap.put("tran_type", "R");
						invrevUpdMap.put("pack_code", packcode);
						invrevUpdMap.put("gross_weight", grossweight);
						invrevUpdMap.put("tare_weight", tareweight);
						invrevUpdMap.put("net_weight", netweight);
						invrevUpdMap.put("pack_instr", packinstr);
						invrevUpdMap.put("mfg_date", mfgDate);
						invrevUpdMap.put("exp_date", expDate);
						invrevUpdMap.put("retest_date", retestdate);
						invrevUpdMap.put("unit__alt", unitalt);
						invrevUpdMap.put("conv__qty_stduom", convqtystduom);
						invrevUpdMap.put("no_art", noart);
						invrevUpdMap.put("BATCH_SIZE", batchsizercpstk);
						invrevUpdMap.put("last_phyc_date", lastphycdate);
						errString = stkUpd.updateStock(invrevUpdMap,
								xtraParams, conn);
						System.out.println(":: ErrorString" + errString);
						if (errString != null && errString.trim().length() > 0) {
							System.out.println("Returning Result " + errString);
							return errString;
						}

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				/*
				 * } rs4.close(); rs4 = null; pstmt4.close(); pstmt4 = null;
				 */

				errString = " ";
				System.out.println("IN RCp");
				sql = "select line_no,item_code,loc_code,lot_no,lot_sl from  inv_pack_rcp where tran_id =  ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					linenorcp = rs.getString("line_no");
					itemcodercp = rs.getString("item_code");
					loccodercp = rs.getString("loc_code");
					lotnorcp = rs.getString("lot_no");
					lotslrcp = rs.getString("lot_sl");
					System.out.println("Line_no is" + linenorcp);
					System.out.println("Item code is" + itemcodercp);
					System.out.println("Location code is" + loccodercp);
					System.out.println("Lot no  is" + lotnorcp);
					System.out.println("Lot sl is" + lotslrcp);

					sql = "select order_no,line_no__ord,  quantity,unit, "
							+ "pack_code,pack_instr, "
							+ "gross_weight, tare_weight,net_weight,dimension,no_art,mfg_date, exp_date,"
							+ "pallet_wt "
							+ "from inv_pack_rcp where tran_id = ? and line_no=? and item_code=? and loc_code=? and lot_no=? and lot_sl = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setString(2, linenorcp);
					pstmt1.setString(3, itemcodercp);
					pstmt1.setString(4, loccodercp);
					pstmt1.setString(5, lotnorcp);
					pstmt1.setString(6, lotslrcp);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) {
						ordernorcp = rs1.getString("order_no");
						linenoordrcp = rs1.getString("line_no__ord");
						quantityrcp = rs1.getDouble("quantity");
						unitrcp = rs1.getString("unit");
						packcodercp = rs1.getString("pack_code");
						packinstrrcp = rs1.getString("pack_instr");
						grossweightrcp = rs1.getDouble("tare_weight");
						netweightrcp = rs1.getDouble("net_weight");
						dimensionrcp = rs1.getString("dimension");
						noartrcp = rs1.getInt("no_art");
						mfgDatercp = rs1.getTimestamp("mfg_date");
						expDatercp = rs1.getTimestamp("exp_date");
						palletwt = rs1.getDouble("pallet_wt");
						if (mfgDatercp == null) {
							sql = "SELECT mfg_date FROM stock WHERE site_code = ? and item_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, siteCode);
							pstmt2.setString(2, itemcodercp);
							pstmt2.setString(3, loccodercp);
							pstmt2.setString(4, lotnorcp);
							pstmt2.setString(5, lotslrcp);
							rs2 = pstmt2.executeQuery();
							if (rs2.next()) {
								mfgDatercp = rs2.getTimestamp("mfg_date");
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;

						}
						if (expDatercp == null) {
							sql = "SELECT exp_date FROM stock WHERE site_code = ? and item_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, siteCode);
							pstmt2.setString(2, itemcodercp);
							pstmt2.setString(3, loccodercp);
							pstmt2.setString(4, lotnorcp);
							pstmt2.setString(5, lotslrcp);
							rs2 = pstmt2.executeQuery();
							if (rs2.next()) {
								expDatercp = rs2.getTimestamp("exp_date");
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;

						}

						sql = "select site_code__mfg ,retest_date,potency_perc,rate,supp_code__mfg,acct_code__inv, cctr_code__inv,rate__oh,acct_code__oh,cctr_code__oh,site_code__mfg, grade, gross_rate, unit__alt,"
								+ " conv__qty_stduom, batch_no, ACTUAL_RATE, BATCH_SIZE from stock where site_code = ? and item_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, siteCode);
						pstmt2.setString(2, itemcode);
						pstmt2.setString(3, loccodercp);
						pstmt2.setString(4, lotno);
						pstmt2.setString(5, lotslrcp);
						rs2 = pstmt2.executeQuery();
						if (rs2.next()) {
							sitecodemfgrcpstk = rs2.getString("site_code__mfg");
							retestdatercpstk = rs2.getTimestamp("retest_date");
							potencypercrcpstk = rs2.getDouble("potency_perc");
							ratercpstk = rs2.getInt("rate");
							suppcodemfgrcpstk = rs2.getString("supp_code__mfg");
							acctcodeinvrcpstk = rs2.getString("acct_code__inv");
							cctrcodeinvstk = rs2.getString("cctr_code__inv");
							rateohrcpstk = rs2.getInt("rate__oh");
							acctcodeohrcpstk = rs2.getString("acct_code__oh");
							cctrcodeohrcpstk = rs2.getString("cctr_code__oh");
							sitecodemfgrcpstk = rs2.getString("site_code__mfg");
							gradercpstk = rs2.getString("grade");
							grossratercpstk = rs2.getInt("gross_rate");
							unitaltrcpstk = rs2.getString("unit__alt");
							convqtystduomrcpstk = rs2
									.getDouble("conv__qty_stduom");
							batchnorcpstk = rs2.getString("batch_no");
							actualratercpstk = rs2.getDouble("ACTUAL_RATE");
							batchsizercpstk = rs2.getDouble("BATCH_SIZE");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						invrevUpdMap1.put("tran_date", trandate);
						invrevUpdMap1.put("tran_id", tranId);
						invrevUpdMap1.put("tran_ser", "I-PKI");
						invrevUpdMap1.put("sorder_no", orderno);
						invrevUpdMap1.put("item_code", itemcodercp);
						invrevUpdMap1.put("site_code", siteCode);
						invrevUpdMap1.put("loc_code", loccodercp);
						invrevUpdMap1.put("unit", unit);
						invrevUpdMap1.put("lot_no", lotnorcp);// take in issue
																// value
						invrevUpdMap1.put("lot_sl", lotslrcp);
						invrevUpdMap1.put("quantity", quantityrcp);
						invrevUpdMap1.put("qty_stduom", quantityrcp);
						invrevUpdMap1.put("site_code__mfg", sitecodemfgrcpstk);
						invrevUpdMap1.put("mfg_date", mfgDate);
						invrevUpdMap1.put("exp_date", expDate);
						invrevUpdMap1.put("retest_date", retestdatercpstk);
						invrevUpdMap1.put("potency_perc", potencypercrcpstk);
						invrevUpdMap1.put("tran_type", "I");
						invrevUpdMap1.put("pack_code", packcodercp);
						invrevUpdMap1.put("gross_weight", grossweightrcp);
						invrevUpdMap1.put("tare_weight", tareweightrcp);
						invrevUpdMap1.put("net_weight", netweightrcp);
						invrevUpdMap1.put("pack_instr", packinstrrcp);
						invrevUpdMap1.put("dimension", dimensionrcp);
						invrevUpdMap1.put("rate", ratercpstk);
						invrevUpdMap1.put("gross_rate", grossratercpstk);
						invrevUpdMap1.put("grade", gradercpstk);
						invrevUpdMap1.put("gross_rate", grossratercpstk);
						invrevUpdMap1.put("unit__alt", unitaltrcpstk);
						invrevUpdMap1.put("conv__qty_stduom",
								convqtystduomrcpstk);
						invrevUpdMap1.put("batch_no", batchnorcpstk);
						invrevUpdMap1.put("ACTUAL_RATE", actualratercpstk);// pallet_wt
						invrevUpdMap1.put("BATCH_SIZE", batchsizercpstk);
						invrevUpdMap1.put("pallet_wt", palletwt);
						errString = stkUpd.updateStock(invrevUpdMap1,
								xtraParams, conn);
						if (errString != null && errString.trim().length() > 0) {
							System.out.println("Returning Result " + errString);
						}
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql = "update inv_pack  set status = 'X' "
						+ " where tran_id = ? ";

				pstmtInsert = conn.prepareStatement(sql);
				pstmtInsert.setString(1, tranId);
				count = pstmtInsert.executeUpdate();
				if (count > 0) {
					System.out.println(">>>>>>>>>>>>>>>>Count from update:"
							+ count);
					updateFlag = true;
				}
				pstmtInsert.close();
				pstmtInsert = null;
			} else {
				System.out.println("NOt COnfirm");
				errString = "VTTRANCNF";
				errString = itmDBAccessEJB.getErrorString("", errString, "",
						"", conn);
				return errString;

			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("Exception in Process1 :: " + ex.getMessage());
			errString = itmDBAccessEJB.getErrorString("", "VTSQLEXC", userId,"",conn);
			return errString;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in Process2 :: " + e.getMessage());
			errString = itmDBAccessEJB.getErrorString("", "VTPRCERR", userId,"",conn);
			return errString;
		} finally {
			try {
				if (errString == null || errString.trim().length() == 0) {
					conn.commit();
					System.out
							.println("Transaction Commit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					errString = itmDBAccessEJB.getErrorString("", "VTCOMPL",
							userId,"",conn);
				} else if (errString != null && errString.trim().length() > 0
						&& result == true && updateFlag == true) {
					System.out
							.println(">>>>>>>>>>>>>>>>>>>>If result is true ,commit");
					conn.commit();
				} else {
					conn.rollback();
					System.out
							.println("Transaction RollBack!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				}

				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("Error In closing connection::==> " + e);
				e.printStackTrace();
			}
		}

		return errString;
	}
}
