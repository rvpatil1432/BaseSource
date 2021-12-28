package ibase.webitm.ejb.dis;



import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import javax.ejb.Stateless;

@Stateless

public class PriceListTranConf extends ActionHandlerEJB implements PriceListTranConfRemote, PriceListTranConfLocal {
	
	//changed by nasruddin 07-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException {
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		String sql = "", empCode = "";
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = "", itemCode = "", priceList = "", unit = "", listType = "", userId = "", lotNoFrom = "", lotNoTo = "";
		String confirmedHdr = "", rate = "", rateType = "", minRate = "", maxRate = "", chgRefNo = "", autoExpiry = "N";
		String priceListParent = "", orderType = "", manageType = "", minQty = "", maxQty = "", lineNo = "", refNo = "", calcBasis = "", lotFrBrow = "", lotNoToBrow = "";
		String lsLotNoFrom = "", refNoOld = "",  termID = "";
		int slabNo = 0;
		java.sql.Date validUpto = null, effFrom = null,  effDate = null;
		java.sql.Date validUpTo = null;
		double  minQtyBrow = 0.0, maxQtyBrow = 0.0;
		Date tranDate = null, confDate = null;
		int updCnt = 0;
		String confirmed = "";
		Timestamp endDate = null;
		Double approxCost = 0.0;
		try {
			System.out.println("helloconfirm***********************");
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
			termID = genericUtility.getValueFromXTRA_PARAMS(xtraParams, termID);
			
			sql = "select confirmed from pricelist_hdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs = pstmt.executeQuery();
			if(rs.next()){
				confirmed = rs.getString("confirmed");
			}
			
			confirmed = confirmed == null ? "" : confirmed.trim();
			System.out.println("confirmed::::" + confirmed);
			if(confirmed != null){
			if(confirmed.equalsIgnoreCase("Y")){
				errString = itmDBAccessEJB.getErrorString("", "VTMCONF1", userId,"",conn);
				return errString;
			}
			}
			sql = "select tran_date, price_list, confirmed, conf_date from pricelist_hdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs = pstmt.executeQuery();
			if(rs.next()){
				
				tranDate = rs.getDate("tran_date");
				//itemCode = rs.getString("item_code");
				priceList = rs.getString("price_list");
				confirmedHdr = rs.getString("confirmed");
				confDate = rs.getDate("conf_date");
				//unit = rs.getString("unit");
				//listType = rs.getString("list_type");
			}
			//tranDate = tranDate == null ? "" : tranDate.trim();
			priceList = priceList == null ? "" : priceList.trim();
			confirmedHdr = confirmedHdr == null ? "" : confirmedHdr.trim();
			//confDate = confDate == null ? "" : confDate.trim();
			
			close(pstmt, rs);
			
			sql = "SELECT d.item_code,   d.lot_no__from,   d.lot_no__to,  d.rate,  d.rate_type,  d.min_rate,  d.max_rate,  d.chg_ref_no," +
			"  d.eff_from, d.valid_upto,  m.price_list,  m.list_type,  m.price_list__parent,  m.order_type,  m.manage_type,  d.min_qty, " +
			" d.max_qty, d.unit,  d.line_no,  h.ref_no,  ref_no_old,  h.calc_basis from PRICELIST_HDR H,  PRICELIST_DET D, " +
			" PRICELIST_MST M where h.tran_id  = d.tran_id and h.price_list = m.price_list and h.tran_id    = ? ";
			
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			/*pstmt.setString(2, itemCode);
			pstmt.setString(3, unit);
			pstmt.setString(4, listType);*/
			rs = pstmt.executeQuery();
			if(rs.next()){
				
				itemCode = checknull(rs.getString("item_code"));
				lotNoFrom = checknull(rs.getString("lot_no__from"));
				lotNoTo = checknull(rs.getString("lot_no__to"));
				rate = checknull(rs.getString("rate"));
				rateType  = checknull(rs.getString("rate_type"));
				minRate = checknull(rs.getString("min_rate"));
				maxRate = checknull(rs.getString("max_rate"));
				chgRefNo = checknull(rs.getString("chg_ref_no"));
				effFrom = rs.getDate("eff_from");
				validUpto = rs.getDate("valid_upto");
				priceList = checknull(rs.getString("price_list"));
				listType = checknull(rs.getString("list_type"));
				priceListParent = checknull(rs.getString("price_list__parent"));
				orderType = checknull(rs.getString("order_type"));
				manageType = checknull(rs.getString("manage_type"));
				minQty = checknull(rs.getString("min_qty"));
				maxQty = checknull(rs.getString("max_qty"));
				unit = checknull(rs.getString("unit"));
				lineNo = checknull(rs.getString("line_no"));
				refNo = checknull(rs.getString("ref_no"));
				refNoOld = checknull(rs.getString("ref_no_old"));
				calcBasis = checknull(rs.getString("rate"));
				
			}
			
			
			/*itemCode = itemCode == null ? "" : itemCode.trim();
			lotNoFrom = lotNoFrom == null ? "" : lotNoFrom.trim();
			lotNoTo = lotNoTo == null ? "" : lotNoTo.trim();
			rate = rate == null ? "" : rate.trim();
			rateType = rateType == null ? "" : rateType.trim();
			minRate = minRate == null ? "" : minRate.trim();
			maxRate = maxRate == null ? "" : maxRate.trim();
			chgRefNo = chgRefNo == null ? "" : chgRefNo.trim();
			priceList = priceList == null ? "" : priceList.trim();
			listType = listType == null ? "" : listType.trim();
			priceListParent = priceListParent == null ? "" : priceListParent.trim();
			orderType = orderType == null ? "" : orderType.trim();
			manageType = manageType == null ? "" : manageType.trim();
			manageType = manageType == null ? "" : manageType.trim();
			manageType = manageType == null ? "" : manageType.trim();
			manageType = manageType == null ? "" : manageType.trim();
			manageType = manageType == null ? "" : manageType.trim();*/
			
			close(pstmt, rs);
			
			sql = "select var_value  from disparm where prd_code = '999999' and var_name = 'AUTO_EXPIRE_PL' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				autoExpiry = checknull(rs.getString(1));
			}
			
			if("Y".equalsIgnoreCase(autoExpiry)) {
				sql = "SELECT pricelist.price_list, pricelist.item_code, pricelist.unit, pricelist.list_type, pricelist.slab_no, pricelist.chg_ref_no, " +
						" pricelist.eff_from, pricelist.valid_upto, pricelist.min_qty, pricelist.max_qty, pricelist.rate, pricelist.rate_type, " +
						" pricelist.chg_date, pricelist.chg_user, pricelist.chg_term, pricelist.lot_no__from, pricelist.lot_no__to, pricelist.min_rate, " +
						" '' as status, pricelist.max_rate, pricelist.order_type, pricelist.price_list__parent, pricelist.ref_no, pricelist.calc_basis, " +
						" pricelist.ref_no_old FROM pricelist WHERE (PRICELIST.PRICE_LIST = ?) and ( PRICELIST.ITEM_CODE = ?) " +
						" and ( PRICELIST.UNIT = ? ) and ( PRICELIST.LIST_TYPE = ? )";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, priceList);
				pstmt1.setString(2, itemCode);
				pstmt1.setString(3, unit);
				pstmt1.setString(4, listType);
				rs1 = pstmt1.executeQuery();
				
				while (rs1.next()) {
					validUpTo = rs1.getDate("valid_upto");
					effDate = rs1.getDate("eff_from");
					//slabNo = checknull(rs1.getString("slab_no"));
					lotFrBrow = checknull(rs1.getString("lot_no__from"));
					lotNoToBrow = checknull(rs1.getString("lot_no__to"));
					minQtyBrow = rs1.getDouble("min_qty");
					maxQtyBrow = rs1.getDouble("max_qty");
					
				}
			}
			close(pstmt1, rs1);
			
			
			sql = "select max (slab_no) as slab_no from pricelist    where price_list = ?  AND item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			rs = pstmt.executeQuery();
			if(rs.next()){
				String strSlabNo = rs.getString("slab_no");
				slabNo = strSlabNo == null ? 0 : Integer.parseInt(strSlabNo);
				strSlabNo = null;
			}
			System.out.println("slab no ::" + slabNo);
			
			
			
			
			
			/*sql = "update pricelist set valid_upto = ? where price_list = ? and item_code = ? and unit = ? and list_type = ? and slab_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1, validUpTo);
			pstmt.setString(2, priceList);
			pstmt.setString(3, itemCode);
			pstmt.setString(4, unit);
			pstmt.setString(5, listType);
			pstmt.setDouble(5, slabNo);
			updCnt = pstmt.executeUpdate();
			
			close(pstmt, rs);
			
			if("B".equalsIgnoreCase(listType)){
				lsLotNoFrom = lotFrBrow;
				
				sql = "select count(*) as count from pricelist where price_list = ? and item_code = ? and unit = ?" +
					" and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and" +
					" max_qty >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, priceList);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, unit);
				pstmt.setString(4, listType);
				pstmt.setString(5, lotFrBrow);
				pstmt.setString(6, lotFrBrow);
				pstmt.setDouble(7, minQtyBrow);
				pstmt.setDouble(8, maxQtyBrow);
			}*/
			
			sql = "insert into pricelist (price_list,item_code,unit,list_type,slab_no,eff_from,valid_upto,lot_no__from," +
				"lot_no__to,min_qty,max_qty,rate,rate_type,min_rate,chg_date,chg_user,chg_term,max_rate,order_type,price_list__parent," +
				"chg_ref_no,ref_no ,ref_no_old) values (?,?,?,?,?,?,?,?," +
				" ?,?,?,?,?,?,sysdate,?,?,?,?,?,?,?,?)";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, unit);
			pstmt.setString(4, listType);
			pstmt.setInt(5, slabNo);
			pstmt.setDate(6, effFrom);
			pstmt.setDate(7, validUpto);
			pstmt.setString(8, lotNoFrom);
			pstmt.setString(9, lotNoTo);
			pstmt.setString(10, minQty);
			pstmt.setString(11, maxQty);
			pstmt.setString(12, rate);
			pstmt.setString(13, rateType);
			pstmt.setString(14, minRate);
			pstmt.setString(15, userId);
			pstmt.setString(16, termID);
			pstmt.setString(17, maxRate);
			pstmt.setString(18, orderType);
			pstmt.setString(19, priceListParent);
			pstmt.setString(20, chgRefNo);
			pstmt.setString(21, refNo);
			pstmt.setString(22, refNoOld);
			updCnt = pstmt.executeUpdate();
			
			
			close(pstmt, rs);
			
			sql = "update pricelist set chg_user = ?, chg_term = ? ,chg_date = sysdate where price_list = ? and item_code = ? and unit = ? and list_type = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, termID);
			pstmt.setString(3, priceList);
			pstmt.setString(4, itemCode);
			pstmt.setString(5, unit);
			pstmt.setString(6, listType);
			updCnt = pstmt.executeUpdate();
			
			close(pstmt, rs);
			
			
			sql = "select calc_method,price_list__tar,price_list__parent  from pricelist_mst_det where price_list = ? order by line_no ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			
			close(pstmt, rs);
			
			sql = "select emp_code  from users where code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,userId);
			rs = pstmt.executeQuery();
			if(rs.next()){
				empCode = checknull(rs.getString("emp_code"));
			}
			
			close(pstmt, rs);
			
			sql = "update pricelist_hdr set confirmed = 'Y',conf_date = sysdate ,emp_code__aprv = ? where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCode);
			pstmt.setString(2, tranID);
			updCnt = pstmt.executeUpdate();
			
			if(updCnt > 0){
				errString = itmDBAccessEJB.getErrorString("", "VTSUCC1", userId,"",conn);
				conn.commit();
				return errString;
			}
			
		}catch(Exception e){
			System.out.println(":::" + this.getClass().getSimpleName() + "::" + e.getMessage());
			e.getMessage();
		}
		return errString;
	}

	private String checknull(String value) {

		return value == null ? "" : value.trim(); 
	}

	private void close(PreparedStatement pstmt, ResultSet rs) {
		try {
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
