package ibase.webitm.ejb.dis;

//import ibase.utility.EMail;
//import ibase.utility.CommonConstants;
//import ibase.webitm.utility.*;
//import ibase.webitm.ejb.*;
/*import ibase.webitm.ejb.mfg.ExplodeBom;
import ibase.webitm.ejb.mfg.adv.RcpBackflushConfirm;
import ibase.webitm.ejb.sys.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;*/

import java.text.*;

import ibase.webitm.ejb.dis.DistCommon;
//import ibase.webitm.ejb.fin.*;
//import ibase.webitm.utility.TransIDGenerator;
//import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.ejb.dis.*;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

/*import javax.ejb.*;
import javax.naming.InitialContext;
import javax.ejb.Stateless;*/

/*import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;*/
//import org.drools.runtime.pipeline.SmooksTransformerProvider;
//import org.w3c.dom.*;

/*import javax.xml.rpc.ParameterMode;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;
*/
/*import java.text.SimpleDateFormat;
import ibase.utility.CommonConstants;
import ibase.utility.GenericUtility;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.utility.ITMException;*/

//import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Stateless;

import ibase.utility.BaseLogger;
//import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

@Stateless
public class PriceListConf extends ActionHandlerEJB implements PriceListConfLocal, PriceListConfRemote 
{
	String userId = "", termId = "", lckGroup = "";
	ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
	ibase.webitm.ejb.sys.UtilMethods utilMethods = ibase.webitm.ejb.sys.UtilMethods.getInstance();

	// overloaded method added to call the confirm method from postsave component -
	// 25/11/11 - Gulzar
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		String retString = "";
		boolean isConn = false;
		Connection conn = null;

		try 
		{
			retString = confirm(tranID, xtraParams, forcedFlag, conn, isConn);

			/*if (retString != null && retString.length() > 0) 
			{
				throw new Exception("Exception while calling confirm for tran  Id:[" + tranID + "]");
			}*/
		} catch (Exception e) {
			//System.out.println("Exception in [InvHoldConfEJB] getCurrdateAppFormat " + exception.getMessage());
			System.out.println("Exception :PriceListconf :confirm() ::" + e.getMessage() + ":"); //Pavan Rane 20may19 [Exception handling to be proper]
			e.printStackTrace();			
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus)
			throws RemoteException, ITMException 
	{
		PreparedStatement pstmtSql = null;
		ResultSet rs = null;
		//FinCommon finCommon = null;
		// GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		ValidatorEJB validatorEJB = null;
		System.out.println("tran id = [" + tranId);
		// boolean connStatus=false;
		String chgUser = "";
		String retString = "";
		String sql = "";
		String conf = "";
		String siteRcp = "";
		String loginEmpCode = "";
		String confirm = "";
		try 
		{
			if (conn == null) 
			{
				//ConnDriver connDriver = null;
				//connDriver = new ConnDriver();
				// conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				conn.setAutoCommit(false);
				//connDriver = null;
				connStatus = true;
			}
			//finCommon = new FinCommon();
			// genericUtility = new GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			loginEmpCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("Printing loginEmpCode---1-----" + loginEmpCode + "--");

			sql = "select confirmed,chg_user from pricelist_hdr where tran_id = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next()) 
			{
				confirm = rs.getString("confirmed") == null ? "N" : rs.getString("confirmed");
				chgUser = rs.getString("chg_user") == null ? " " : rs.getString("chg_user");
			}
			rs.close();
			rs = null;
			pstmtSql.close();
			pstmtSql = null;
			if (chgUser == null || chgUser.trim().length() == 0) 
			{
				chgUser = "SYSTEM";
			}
			if ("null".equalsIgnoreCase(loginEmpCode) || loginEmpCode == null || loginEmpCode.trim().length() == 0) 
			{
				sql = "select emp_code from users where code=?";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, chgUser);

				rs = pstmtSql.executeQuery();
				if (rs.next()) 
				{
					loginEmpCode = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
				System.out.println("Printing loginEmpCode---2-----" + loginEmpCode + "--");

			}

			if (confirm != null && confirm.equalsIgnoreCase("N")) 
			{
				retString = confirmPlist(tranId, conn, xtraParams);
				System.out.println("err String  =" + retString);
				if (retString != null && retString.trim().length() > 0) 
				{
					return retString;
				}
				if (retString != null && retString.trim().length() > 0) 
				{
					return retString;
				}

				if (retString == null || retString.trim().length() == 0) 
				{
					if ("null".equalsIgnoreCase(loginEmpCode) || loginEmpCode == null
							|| loginEmpCode.trim().length() == 0) 
					{
						loginEmpCode = "E03952";
					}
					System.out.println("loginEmpCode--3-" + loginEmpCode);
					sql = "update pricelist_hdr set confirmed = 'Y', conf_date = ?,emp_code__aprv = ? where tran_id = ?";
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
					pstmtSql.setString(2, loginEmpCode);
					pstmtSql.setString(3, tranId);
					int updateCoount = pstmtSql.executeUpdate();
					System.out.println("no of row update = " + updateCoount);
					pstmtSql.close();
					pstmtSql = null;
					if (updateCoount > 0) 
					{
						retString = itmDBAccessEJB.getErrorString("", "VTCICONF3", "", "", conn);
					}
				} 
				else 
				{
					return retString;
				}
			} else 
			{
				System.out.println("The Selected transaction is already confirmed");
				retString = itmDBAccessEJB.getErrorString("", "VTMCONF1", "", "", conn);
				return retString;
			}
			System.out.println("115 err String from confirm method.....");

		}

		/*
		 * try { if ( conn == null ) { ConnDriver connDriver = null; connDriver = new
		 * ConnDriver(); //conn = connDriver.getConnectDB("DriverITM"); conn =
		 * getConnection() ; conn.setAutoCommit(false); connDriver = null; connStatus =
		 * true; }
		 * 
		 * ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB(); userId =
		 * genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); termId =
		 * genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); if (userId ==
		 * null || userId.trim().length() == 0) { userId = "SYSTEM"; } if (termId ==
		 * null || termId.trim().length() == 0) { termId = "SYSTEM"; }
		 * 
		 * sql = "SELECT CONFIRMED FROM pricelist_hdr WHERE TRAN_ID= ?"; pstmtSql =
		 * conn.prepareStatement(sql); pstmtSql.setString(1, tranId); rs =
		 * pstmtSql.executeQuery(); if ( rs.next() ) { conf = rs.getString("CONFIRMED");
		 * }
		 * 
		 * if(pstmtSql != null) { pstmtSql.close(); pstmtSql = null; } if ( rs != null)
		 * { rs.close(); rs = null; } if( conf.equalsIgnoreCase("Y") ) { retString =
		 * itmDBAccessLocal.getErrorString("","VTCONF1",""); return retString; } else {
		 * retString = confirmPlist(tranId,conn, xtraParams); if (retString == null ||
		 * retString.trim().length() == 0) { ////////////////////// EDI creation String
		 * ediOption = ""; String dataStr = ""; sql =
		 * "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_pricelist_tran' ";
		 * pstmtSql = conn.prepareStatement(sql); rs = pstmtSql.executeQuery(); if (
		 * rs.next() ) { ediOption = rs.getString("EDI_OPTION"); } rs.close();rs = null;
		 * pstmtSql.close();pstmtSql = null;
		 * 
		 * if ( "1".equals(ediOption.trim()) ) { CreateRCPXML createRCPXML = new
		 * CreateRCPXML("w_pricelist_tran","tran_id"); dataStr =
		 * createRCPXML.getTranXML( tranId, conn ); System.out.println( "dataStr =[ "+
		 * dataStr + "]" ); Document ediDataDom = genericUtility.parseString(dataStr);
		 * 
		 * E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
		 * retString = e12CreateBatchLoad.createBatchLoad( ediDataDom,
		 * "w_pricelist_tran", "0", xtraParams, conn ); createRCPXML = null;
		 * e12CreateBatchLoad = null;
		 * 
		 * if( retString != null && "SUCCESS".equals(retString) ) {
		 * System.out.println("retString from batchload = ["+retString+"]"); } }
		 * /////////////////////
		 * 
		 * if( connStatus )//Condition added - 25/11/11 - Gulzar as confirm method is
		 * called from post save component { conn.commit(); retString =
		 * itmDBAccessLocal.getErrorString("","VTMCONF2",""); } } else {
		 * conn.rollback(); } } }
		 */
		catch (Exception e) 
		{
			if (conn != null) 
			{
				try 
				{

					conn.rollback();
				}
				/*
				 * catch (SQLException ex) {
				 * 
				 * System.out.println("Exception : "+e); e.printStackTrace(); throw new
				 * ITMException(e);
				 * 
				 * }
				 */ catch (java.sql.SQLException e1) 
				{
					e1.printStackTrace();
				}
			}
			//System.out.println("Exception : " + e);  //Pavan Rane 20may19 [Exception handling to be proper]
			System.out.println("Exception :PriceListconf :confirm(String tranId, String xtraParams...::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		} finally {

			try {

				if (retString != null && retString.trim().length() > 0) 
				{
					if (conn != null && !conn.isClosed() && connStatus) 
					{
						if (retString.indexOf("VTCICONF3") > -1) 
						{
							conn.commit();
						} else {
							conn.rollback();
						}
						conn.close();
						conn = null;
					}
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
				if (pstmtSql != null) 
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				// conn.close();
			} catch (Exception e) {
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}

		}
		return retString;
	}

	private String insertPricelist(String tranId, HashMap PList, ArrayList PricelistGen, Connection conn,
			String xtraParams) throws RemoteException, ITMException 
	{
		PreparedStatement pstmtSql = null, pstmtInsert = null, pstmtInner = null, pstmtLot=null;
		ResultSet rs = null, rsInner = null, rsLot = null;
		String dbName = "";
		String sql = "";
		String retString = "";
		String errCode = "", AutoExpire = "", LotFrBrow = "", LotNoToBrow = "", LotNoFrom = "", OldLotNo = "", Set = "",
				NewStr = "", OrigStr = "", edioption = "";
		int DayDiff = 0;
		java.sql.Timestamp ValidUpTo = null, EffDate = null;
		int LineNo = 0, RowCount = 0, Count = 0, OldLen = 0, New = 0;
		double SlabNo = 0.0;
		double NewLen = 0.0, slabno = 0.0, cnt = 0.0;
		double MinQtyBrow = 0.0;
		double MaxQtyBrow = 0.0;
		double minrate = 0.0;
		double rate1 = 0.0;
		double minqty = 0.0, maxqty = 0.0;
		String ratetype = "";
		String refNo1 = "";
		String refNoold1 = "";
		String chgref1 = "";
		double maxrate1 = 0.0;
		char left = 0;
		String PriceListParent1 = "", orderType1 = "";
		int Ctr = 0, countup = 0, Len = 0, Len1 = 0,pListCtr=0;
		java.sql.Timestamp ChgDate = null, efffromdt = null, validupto = null;
		java.sql.Timestamp chgDate = null, efffrom = null, today = null;
		String lotnofr = "", lotnoto = "", currentlotnofr = "", currentlotnoto = "", nextlotnofr = "", LotNoTo = "",
				currentlotnofrtemp = "", currentlotnototemp = "";
		// String LotNoFrom="",LotNoTo="";
		boolean recordfound;
		// HashMap PlGen=null;
		String plist = "", itemcode = "", unit = "", listtype = "";
		String plist1 = "", refNoold = "";
		int cnt1 = 0, UpdCnt = 0;
		// String LotNoFroma[] = null, LotNoToa[]=null;
		ArrayList<String> LotNoFroma = new ArrayList<String>();
		//ArrayList<String> LotNoToa = new ArrayList<String>();
		char left1 = 0, left2 = 0, left3 = 0, left4 = 0;
		int updCnt = 0;
		String mid1 = "", mid2 = "", mid3 = "", mid4 = "";
		java.text.SimpleDateFormat sdf = null;
		String lotnofrom = "", lotnoto1 = "";
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
		double minqtySplit = 0.0, maxqtySplit = 0.0, rate1Split = 0.0, minRate1Split=0, MaxRate1Split=0;
		String ratetypeSplit = "", PriceListParent1Split = "", refNo1Split = "", refNoold1Split = "";
		String updLotFrom = "", LotNoFromUpd=""; 
		Timestamp efffromSplit = null, ValidUpToSplit = null;
		String priceListOldFr = "",priceListOldto = "";
		
		try 
		{
			System.out.println("----------------------------- Inside Insert Pricelist method ---------------");
			BaseLogger.log("3", null, null, "----------------------------- Inside Insert Pricelist method ---------------");
			DistCommon distCommon = new DistCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			dbName = CommonConstants.DB_NAME;
			AutoExpire = distCommon.getDisparams("999999", "AUTO_EXPIRE_PL", conn);
			//System.out.println("Autoexpiry Value is: [" + AutoExpire + "]");
			BaseLogger.log("9", null, null, "Autoexpiry Value is: [" + AutoExpire + "]");
			if (AutoExpire == null) 
			{
				AutoExpire = "";
			} else if (AutoExpire.equalsIgnoreCase("Y")) 
			{
				/*
				 * Pavan R 6may19 commented and added inside loop System.out.
				 * println("------------------------ Insert Auto Expiry ------------------");
				 * //PricelistGen.add(PList);
				 * System.out.println("List Size------["+PricelistGen.size()+"]");
				 * System.out.println("PList---["+PList+"]"); plist =
				 * (String)PList.get("PRICE_LIST");
				 * System.out.println("Price list value is"+plist); itemcode =
				 * (String)PList.get("ITEM_CODE");
				 * System.out.println("item code in price list"+itemcode); unit =
				 * (String)PList.get("UNIT"); listtype = (String)PList.get("LIST_TYPE");
				 * System.out.println("List size is"+PricelistGen); lotnofrom =
				 * (String)PList.get("LOT_NO__FROM"); System.out.println("lot no from [ "+
				 * lotnofrom + "]"); lotnoto1 = (String)PList.get("LOT_NO__TO");
				 * System.out.println("lot no to ["+lotnoto1 + "]"); efffrom =
				 * (java.sql.Timestamp)PList.get("EFF_FROM");
				 * System.out.println("[efffrom]----["+efffrom+"]"); minqty =
				 * (Double)PList.get("MIN_QTY"); System.out.println("min qty in list"+minqty);
				 * maxqty = (Double)PList.get("MAX_QTY");
				 * System.out.println("[maxqty]----["+maxqty+"]"); rate1 = (Double)
				 * PList.get("RATE"); System.out.println("[rate1]-----["+rate1+"]"); ratetype =
				 * (String)PList.get("RATE_TYPE");
				 * System.out.println("[ratetype]-----["+ratetype+"]"); minrate =
				 * (Double)PList.get("MIN_RATE");
				 * System.out.println("[minrate]----["+minrate+"]"); maxrate1 =
				 * (Double)PList.get("MAX_RATE");
				 * System.out.println("[maxrate1]---["+maxrate1+"]"); refNo1 =
				 * (String)PList.get("REF_NO"); System.out.println("[refNo1]---["+refNo1+"]");
				 * refNoold1 = (String)PList.get("REF_NO_OLD");
				 * System.out.println("refNoold1----["+refNoold1+"]"); PriceListParent1 =
				 * (String)PList.get("PRICE_LIST__PARENT");
				 * System.out.println("[PriceListParent1]------["+PriceListParent1+"]");
				 * orderType1 = (String)PList.get("ORDER_TYPE");
				 * System.out.println("[orderType1]----["+orderType1+"]"); chgref1 =
				 * (String)PList.get("CHG_REF_NO");
				 * System.out.println("[chgref1]----["+chgref1+"]"); Pavan R 6may19 end
				 */

				//sql = "select valid_upto,eff_from,slab_no,lot_no__from,lot_no__to,min_qty,max_qty from pricelist where price_list=? and item_code=? ";
				sql = "select valid_upto,eff_from,slab_no,lot_no__from,lot_no__to,min_qty,max_qty from pricelist where price_list=? and item_code=? and valid_upto >= ?";
				// pstmtSql = conn.prepareStatement(sql); //Pavan R 6may19 to update sql inside
				// PricelistGen loop
				pstmtInner = conn.prepareStatement(sql);
				// pstmtSql.setString(1,plist);
				// pstmtSql.setString(2,itemcode);
				// rs = pstmtSql.executeQuery();
				sql = "select lot_no__from, lot_no__to from pricelist where price_list = ? and item_code = ? "
						//+ " and unit = ? and list_type = ? and lot_no__from <= ? and min_qty <= ? and max_qty >= ? order by lot_no__from";
						//+ " and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ? order by lot_no__from";
						+ " and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? order by lot_no__from";
				pstmtLot = conn.prepareStatement(sql);
				
				for (Ctr = 0; Ctr < PricelistGen.size(); Ctr++) 
				{
					//System.out.println("Insert For loop.....................................");
					// PList=new HashMap<String, Object>();
					PList = (HashMap) PricelistGen.get(Ctr);
					// Pavan R 6may19 to added inside PricelistGen loop
					//System.out.println("------------------------ Insert Auto Expiry ------------------");
					BaseLogger.log("3", null, null, "------------------------ Insert Auto Expiry ------------------");
					// PricelistGen.add(PList);
					//System.out.println("List Size------[" + PricelistGen.size() + "]");
					BaseLogger.log("3", null, null,"List Size------[" + PricelistGen.size() + "]");
					//System.out.println("PList---[" + PList + "]");
					BaseLogger.log("9", null, null, "Auto Expiry PList---[" + PList + "]");
					plist = (String) PList.get("PRICE_LIST");
					//System.out.println("Price list value is" + plist);
					itemcode = (String) PList.get("ITEM_CODE");
					//System.out.println("item code in price list" + itemcode);
					unit = (String) PList.get("UNIT");
					listtype = (String) PList.get("LIST_TYPE");
					//System.out.println("List size is" + PricelistGen);
					lotnofrom = (String) PList.get("LOT_NO__FROM");
					//System.out.println("lot no from [ " + lotnofrom + "]");
					lotnoto1 = (String) PList.get("LOT_NO__TO");
					//System.out.println("lot no to [" + lotnoto1 + "]");
					efffrom = (java.sql.Timestamp) PList.get("EFF_FROM");
					//System.out.println("[efffrom]----[" + efffrom + "]");
					minqty = (Double) PList.get("MIN_QTY");
					//System.out.println("min qty in list" + minqty);
					maxqty = (Double) PList.get("MAX_QTY");
					//System.out.println("[maxqty]----[" + maxqty + "]");
					rate1 = (Double) PList.get("RATE");
					//System.out.println("[rate1]-----[" + rate1 + "]");
					ratetype = (String) PList.get("RATE_TYPE");
					//System.out.println("[ratetype]-----[" + ratetype + "]");
					minrate = (Double) PList.get("MIN_RATE");
					//System.out.println("[minrate]----[" + minrate + "]");
					maxrate1 = (Double) PList.get("MAX_RATE");
					//System.out.println("[maxrate1]---[" + maxrate1 + "]");
					refNo1 = (String) PList.get("REF_NO");
					//System.out.println("[refNo1]---[" + refNo1 + "]");
					refNoold1 = (String) PList.get("REF_NO_OLD");
					//System.out.println("refNoold1----[" + refNoold1 + "]");
					PriceListParent1 = (String) PList.get("PRICE_LIST__PARENT");
					//System.out.println("[PriceListParent1]------[" + PriceListParent1 + "]");
					orderType1 = (String) PList.get("ORDER_TYPE");
					//System.out.println("[orderType1]----[" + orderType1 + "]");
					chgref1 = (String) PList.get("CHG_REF_NO");
					//System.out.println("[chgref1]----[" + chgref1 + "]");		
					BaseLogger.log("3", null, null, "Pricelist insloop 503 plist["+plist+"]  itemCode["+itemcode+"]  lotnofrom["+lotnofrom+"] lotnoto1["+lotnoto1+"] efffrom["+efffrom+"]");
					updLotFrom = lotnofrom;
					pstmtInner.setString(1, plist);
					pstmtInner.setString(2, itemcode);
					pstmtInner.setTimestamp(3, (Timestamp) PList.get("EFF_FROM"));
					rsInner = pstmtInner.executeQuery();
					// Pavan R 6may19 end
					//if (rsInner.next())//Pavan R DEC19
					while (rsInner.next())
					{
						ValidUpTo = rsInner.getTimestamp("valid_upto");
						//System.out.println("Valid upto in price list" + ValidUpTo);
						EffDate = rsInner.getTimestamp("eff_from");
						SlabNo = rsInner.getDouble("slab_no");
						//System.out.println("slab No ----[" + SlabNo + "]");
						LotFrBrow = rsInner.getString("lot_no__from");
						//System.out.println("Lots Number from brow window" + LotFrBrow);
						LotNoToBrow = rsInner.getString("lot_no__to");
						//System.out.println("Lots Number from brow window" + LotNoToBrow);
						MinQtyBrow = rsInner.getDouble("min_qty");
						MaxQtyBrow = rsInner.getDouble("max_qty");
						//System.out.println("Minimum value is@@@@@@@@@" + MinQtyBrow);
						BaseLogger.log("3", null, null, "Auto Expiry @@@1125 ValidUpTo>>["+ValidUpTo+"] EffDate>>["+EffDate+"] LotFrBrow>>["+LotFrBrow+"] LotNoToBrow>>["+LotNoToBrow+"] slab_no>>["+SlabNo+"]");
						//System.out.println("---------------------- Check if Conditon -----------------------");
						DayDiff = (int) utilMethods.DaysAfter(ValidUpTo, EffDate);
						//System.out.println("DayDiff-----------[" + DayDiff + "]");
						BaseLogger.log("3", null, null,"DayDiff-----------[" + DayDiff + "]");
						if (DayDiff <= 0) 
						{
							//System.out.println("In if condition@@@@@");

							//if (LotFrBrow == lotnofrom && LotNoToBrow == lotnoto1) //Pavan R DEC19
							if (LotFrBrow.equals(lotnofrom) && LotNoToBrow.equals(lotnoto1))
							{
								//System.out.println("In if condition ######");
								BaseLogger.log("3", null, null,"In if condition ######");
								ValidUpTo = utilMethods.RelativeDate((java.sql.Timestamp) PList.get("EFF_FROM"), -1);
								BaseLogger.log("3", null, null, "ValidUpTo["+ValidUpTo+"] on effFrom["+PList.get("EFF_FROM")+"]");
								if (dbName.equalsIgnoreCase("db2")) 
								{
									validupto = (ValidUpTo);
									//System.out.println("Valid upto date is:" + ValidUpTo);
									//System.out.println("Converted Valid Upto Date is:" + validupto);									
									sql = "update pricelist set  valid_upto = ? where price_list =? and item_code = ? and unit = ? and list_type = ? and slab_no = ?";
									pstmtSql = conn.prepareStatement(sql);
									pstmtSql.setTimestamp(1, validupto);
									pstmtSql.setString(2, plist);
									pstmtSql.setString(3, itemcode);
									pstmtSql.setString(4, unit);
									pstmtSql.setString(5, listtype);
									pstmtSql.setDouble(6, SlabNo);
									int upcnt1 = pstmtSql.executeUpdate();
									System.out.println("No. of rows updated------>>" + upcnt1);
									if (pstmtSql != null) 
									{
										pstmtSql.close();
										pstmtSql = null;
									}
								} else 
								{
									sql = "update pricelist set  valid_upto = ? where price_list =? and item_code = ? and unit = ? and list_type = ? and slab_no = ?";
									pstmtSql = conn.prepareStatement(sql);
									pstmtSql.setTimestamp(1, ValidUpTo);
									pstmtSql.setString(2, plist);
									pstmtSql.setString(3, itemcode);
									pstmtSql.setString(4, unit);
									pstmtSql.setString(5, listtype);
									pstmtSql.setDouble(6, SlabNo);
									int upcnt1 = pstmtSql.executeUpdate();
									System.out.println("No. of rows updated------>>" + upcnt1);
									if (pstmtSql != null) 
									{
										pstmtSql.close();
										pstmtSql = null;
									}

								}
							}
						}

					}
					rsInner.close();
					rsInner = null; // Pavan R 6may19
					pstmtInner.clearParameters();

					// Pavan R start for 10may19
					// listtype = (String)PList.get("LIST_TYPE");
					//System.out.println("check price listttttttttttttttt type" + listtype);
					BaseLogger.log("3", null, null, "check price list type @@@591 [" + listtype+"]");
					if (listtype.equalsIgnoreCase("B")) 
					{
						LotNoFrom = (String) PList.get("LOT_NO__FROM");
						//System.out.println("@@@@@lot number from[" + LotNoFrom+"]");
						BaseLogger.log("3", null, null, "@@@@@lot number from[" + LotNoFrom+"]");
						//sql = "select count(1) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?";
						sql = "select count(1) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, plist);
						pstmtSql.setString(2, itemcode);
						pstmtSql.setString(3, unit);
						pstmtSql.setString(4, listtype);
						pstmtSql.setString(5, lotnofrom);
						pstmtSql.setString(6, lotnofrom);// lotnoto1
						//pstmtSql.setDouble(7, minqty); //06apr20 qty not consider
						//pstmtSql.setDouble(8, maxqty);
						rs = pstmtSql.executeQuery();
						if (rs.next()) 
						{
							Count = rs.getInt(1);
						}
						System.out.println("llCount-------->>[" + Count + "]");
						if (rs != null) 
						{
							rs.close();
							rs = null;
						}
						if (pstmtSql != null) 
						{
							pstmtSql.close();
							pstmtSql = null;
						}

						if (Count > 0) 
						{

							//Pavan Rane 09DEc19[to hold lot_no__to to update on splited price list] start
							pstmtLot.setString(1, plist);
							pstmtLot.setString(2, itemcode);
							pstmtLot.setString(3, unit);
							pstmtLot.setString(4, listtype);					
							pstmtLot.setString(5, lotnofrom);
							pstmtLot.setString(6, lotnofrom);
							//pstmtLot.setDouble(7, minqty); //06apr20 qty not consider
							//pstmtLot.setDouble(8, maxqty);
							rsLot =  pstmtLot.executeQuery();
							if (rsLot.next()) 
							{
								priceListOldFr = rsLot.getString("lot_no__from");
								priceListOldto = rsLot.getString("lot_no__to");
								BaseLogger.log("3", null, null, "@643 priceListOldFr["+priceListOldFr+"]priceListOldto["+priceListOldto+"]");
							}
							rsLot.close();					
							rsLot = null; 
							pstmtLot.clearParameters();
							//Pavan Rane 09DEc19[to hold lot_no__to to update on splited price list] end
							String s2 = LotNoFrom.trim();
							System.out.println("Lotnumber @@650>> [" + s2.length()+"]");
							left = 0;
							char right = s2.charAt(s2.length() - 1);
							//int diff3 = 0;
							String original = "", result = "", result2 = "";
							int length = LotNoFrom.trim().length();
							/*
							 * System.out.println("insert in count"); Len=LotNoFrom.trim().length(); Ctr=1;
							 * String s2=LotNoFrom.trim(); //String chekStr=LotNoFrom.trim();
							 */
							System.out.println("s2222[" + s2+"]");
							// char right= s2.charAt(s2.length()-1);
							//Pavan Rane 21aug19[to set allow alphbet at right side]....start
							if(!isNumber(right))
							{
								LotNoFrom ="";
								for ( int i = 0;  i <= s2.length()-2 ; i++ )
								{					
									 LotNoFrom = LotNoFrom + s2.charAt(i); 
									 System.out.println("LotNoFrom......["+LotNoFrom+"]");						
								} 																
								int asciiChar = (int)right;		
								System.out.println("--asciiChar--::["+asciiChar+"]");
								char prevChar = (char) (asciiChar-1);
								System.out.println("--Char--::["+asciiChar+"]");
								LotNoFrom = LotNoFrom + String.valueOf(prevChar);
								System.out.println("631 if char LotNoFrom["+LotNoFrom+"]");
							}else
							//if (isNumber(s2.charAt(s2.length() - 1))) 
							{
								cnt1 = 0;
								System.out.println("right s22222[" + right+"]");
								for (int i = length - 1; i >= 0; i--) 
								{
									Character character = LotNoFrom.trim().charAt(i);
									System.out.println("Char[" + character+"]");

									boolean flag = isNumber(character);

									if (!flag) 
									{
										result = result + LotNoFrom.trim().charAt(i);
										break;
									}
									System.out.println("result[" + result+"]");
									cnt1++;
									System.out.println("Count is[" + cnt1+"]");
								}
								System.out.println("Result of entered string is: [" + result+"]  cnt1["+cnt1+"]");
								//System.out.println("Count is:::" + cnt1);
								int testOrginal = 0;
								long sub = 0;//int sub = 0;
								testOrginal = length - cnt1;

								System.out.println("testOrginal[" + testOrginal + "]");
								String testSub1 = LotNoFrom.trim().substring(0, testOrginal);
								System.out.println("testSub1::-[" + testSub1 + "]");
								int testSub = testSub1.length();
								System.out.println("testSub[" + testSub+"]");
								String testSub2 = LotNoFrom.trim().substring(s2.length() - cnt1);
								System.out.println("testSub2::-[" + testSub2+"]");
								sub = Long.parseLong(testSub2);//sub = Integer.parseInt(testSub2);
								int u2 = testSub2.length();
								long OrgSub = sub - 1;//int OrgSub = sub - 1;
								String v1 = String.valueOf(OrgSub);
								int v2 = v1.length();
								System.out.println("V2:[" + v2+"]");
								int z = u2 - v2;
								System.out.println("z:[" + z+"]");
								if (z != 0) 
								{
									NewStr = "";
									for (int s3 = 0; s3 < z; s3++) 
									{
										//System.out.println("For loop if ");
										NewStr = NewStr.concat("0");
										System.out.println("@@@@@@@@@New String:::::::::::[" + NewStr.length()+"]");
									}
								}
								//int q = NewStr.length();
								String a2 = Long.toString(OrgSub);//String a2 = Integer.toString(OrgSub);
								System.out.println("NewStr" + NewStr);
								LotNoFrom = testSub1.trim() + NewStr.trim() + a2;
								System.out.println("LotNoFrom upd @@@@730 LotNoFrom@@@@:" + LotNoFrom);
							}
								//sql = "update pricelist set lot_no__to = ? where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?";
							sql = "update pricelist set lot_no__to = ? where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and lot_no__from < ? ";
								pstmtSql = conn.prepareStatement(sql);
								pstmtSql.setString(1, LotNoFrom);
								pstmtSql.setString(2, plist);
								pstmtSql.setString(3, itemcode);
								pstmtSql.setString(4, unit);
								pstmtSql.setString(5, listtype);
								pstmtSql.setString(6, lotnofrom);
								pstmtSql.setString(7, lotnofrom);
								//pstmtSql.setDouble(8, minqty);
								//pstmtSql.setDouble(9, maxqty);
								pstmtSql.setString(8, LotNoFrom);
								int upcnt1 = pstmtSql.executeUpdate();
								System.out.println("No. of rows updated@@@@@@@@------>>" + upcnt1);
								if (pstmtSql != null) 
								{
									pstmtSql.close();
									pstmtSql = null;
								}
								updLotFrom = LotNoFrom;
							//}
						}//count end
					}//if (listtype.equalsIgnoreCase("B"))
					// Pavan R end for 10may19
				} // for end
				if (pstmtInner != null) {
					pstmtInner.close();
					pstmtInner = null;
				}
				if(pstmtLot != null)
				{
					pstmtLot .close();
					pstmtLot = null;
				}


			} // end autoexpire if
				///////////////
				// astr_PL.list_type = 'B'

			// to expire the pricelist in case of revise circular of old circular.
			// change the valid upto date of price before the eff from date and batch no
			// less then the
			// plist1 = (String)PList.get("price_list");
			//System.out.println("---PricelistGen["+PricelistGen+"]-Ctr["+PricelistGen.size()+"]updLotFrom["+updLotFrom+"]");
			BaseLogger.log("3", null, null, "Batch no....PricelistGen.size()["+PricelistGen.size()+"]updLotFrom["+updLotFrom+"]");
			for (pListCtr = 0; pListCtr < PricelistGen.size(); pListCtr++) 
			{
				PList = (HashMap) PricelistGen.get(pListCtr);
				System.out.println("---PricelistGen-Ctr["+pListCtr+"]");
				// PricelistGen.add(PList);
				System.out.println("List Size------[" + PricelistGen.size() + "]");
				System.out.println("PList---[" + PList + "]");
				plist = (String) PList.get("PRICE_LIST");
				System.out.println("Price list value is[" + plist+"]");
				itemcode = (String) PList.get("ITEM_CODE");
				System.out.println("item code in price list" + itemcode);
				unit = (String) PList.get("UNIT");
				listtype = (String) PList.get("LIST_TYPE");
				System.out.println("List size is" + PricelistGen);
				lotnofrom = (String) PList.get("LOT_NO__FROM");
				System.out.println("lot no from [ " + lotnofrom + "]");
				lotnoto1 = (String) PList.get("LOT_NO__TO");
				System.out.println("lot no to [" + lotnoto1 + "]");
				efffrom = (java.sql.Timestamp) PList.get("EFF_FROM");
				System.out.println("[efffrom]----[" + efffrom + "]");
				minqty = (Double) PList.get("MIN_QTY");
				//System.out.println("min qty in list" + minqty);
				maxqty = (Double) PList.get("MAX_QTY");
				//System.out.println("[maxqty]----[" + maxqty + "]");
				rate1 = (Double) PList.get("RATE");
				//System.out.println("[rate1]-----[" + rate1 + "]");
				ratetype = (String) PList.get("RATE_TYPE");
				//System.out.println("[ratetype]-----[" + ratetype + "]");
				minrate = (Double) PList.get("MIN_RATE");
				//System.out.println("[minrate]----[" + minrate + "]");
				maxrate1 = (Double) PList.get("MAX_RATE");
				//System.out.println("[maxrate1]---[" + maxrate1 + "]");
				refNo1 = (String) PList.get("REF_NO");
				//System.out.println("[refNo1]---[" + refNo1 + "]");
				refNoold1 = (String) PList.get("REF_NO_OLD");
				//System.out.println("refNoold1----[" + refNoold1 + "]");
				PriceListParent1 = (String) PList.get("PRICE_LIST__PARENT");
				//System.out.println("[PriceListParent1]------[" + PriceListParent1 + "]");
				orderType1 = (String) PList.get("ORDER_TYPE");
				//System.out.println("[orderType1]----[" + orderType1 + "]");
				chgref1 = (String) PList.get("CHG_REF_NO");
				//System.out.println("[chgref1]----[" + chgref1 + "]");

				
				
				refNoold = (String) PList.get("REF_NO_OLD");
				//System.out.println("Reference Number old is:" + refNoold);
				if (listtype.equalsIgnoreCase("B")) 
				{
					LotNoFrom = (String) PList.get("LOT_NO__FROM");
					BaseLogger.log("3", null, null, "@@@@@lot 832 number from[" + LotNoFrom+"]");
					if (refNoold != null && refNoold.trim().length() > 0) 
					{
						
						//Modified By Pavan Rane 05dec19 start[issue as previous valid pricelist expired wrongly]
						//sql = "select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from >= ? and list_type = ?   and min_qty >= ? and max_qty <= ? and ref_no  = ?";
						//sql = "select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from <= ? and lot_no__to >= ? and list_type = ?   and min_qty <= ? and max_qty >= ? and ref_no  = ?";
						sql = "select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from <= ? and lot_no__to >= ? and list_type = ? and ref_no  = ?";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, plist);
						pstmtSql.setString(2, itemcode);
						pstmtSql.setString(3, unit);
						pstmtSql.setString(4, lotnofrom);
						/*pstmtSql.setString(5, listtype);
						pstmtSql.setDouble(6, minqty);
						pstmtSql.setDouble(7, maxqty);
						pstmtSql.setString(8, refNoold);*/
						pstmtSql.setString(5, lotnofrom);
						pstmtSql.setString(6, listtype);
						//pstmtSql.setDouble(7, minqty);
						//pstmtSql.setDouble(8, maxqty);
						pstmtSql.setString(7, refNoold);
						rs = pstmtSql.executeQuery();
						System.out.println("COUNT" + countup);
						if (rs.next()) 
						{
							countup = rs.getInt(1);
						}

						rs.close();
						rs = null;
						pstmtSql.close();
						pstmtSql = null;

					} else 
					{
						//Modified By Pavan Rane 05dec19 start[issue as previous valid pricelist expired wrongly]
						//sql = "select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from >= ? and list_type = ?   and min_qty >= ? and max_qty <= ? and ref_no is null ";
						//sql = "select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from <= ? and lot_no__to >= ? and list_type = ?   and min_qty <= ? and max_qty >= ? and ref_no is null ";
						sql = "select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from <= ? and lot_no__to >= ? and list_type = ? and ref_no is null ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, plist);
						pstmtSql.setString(2, itemcode);
						pstmtSql.setString(3, unit);
						pstmtSql.setString(4, lotnofrom);
						/*pstmtSql.setString(5, listtype);
						pstmtSql.setDouble(6, minqty);
						pstmtSql.setDouble(7, maxqty);*/
						pstmtSql.setString(5, lotnofrom);
						pstmtSql.setString(6, listtype);
						//pstmtSql.setDouble(7, minqty);
						//pstmtSql.setDouble(8, maxqty);
						rs = pstmtSql.executeQuery();
						System.out.println("COUNT" + countup);
						if (rs.next()) {
							countup = rs.getInt(1);
						}

						rs.close();
						rs = null;
						pstmtSql.close();
						pstmtSql = null;
					}
					if (countup > 0) 
					{
						if (refNoold != null && refNoold.trim().length() > 0) {
							//Modified By Pavan Rane 05dec19 start[issue as previous valid pricelist expired wrongly]
							//sql = "select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from >= ? and list_type = ? and min_qty >= ? and max_qty <= ? and ref_no  = ?";
							//sql = "select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from <= ? and lot_no__to >= ? and list_type = ? and min_qty <= ? and max_qty >= ? and ref_no  = ?";
							sql = "select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from <= ? and lot_no__to >= ? and list_type = ? and ref_no  = ?";
							pstmtSql = conn.prepareStatement(sql);
							// pstmtSql.setDouble(1, slabno);
							// pstmtSql.setTimestamp(2, efffromdt);
							// pstmtSql.setString(3, LotNoFrom);
							pstmtSql.setString(1, plist);
							pstmtSql.setString(2, itemcode);
							pstmtSql.setString(3, unit);
							pstmtSql.setString(4, lotnofrom);
							/*pstmtSql.setString(5, listtype);
							pstmtSql.setDouble(6, minqty);
							pstmtSql.setDouble(7, maxqty);
							pstmtSql.setString(8, refNoold);*/
							pstmtSql.setString(5, lotnofrom);
							pstmtSql.setString(6, listtype);
							//pstmtSql.setDouble(7, minqty);
							//pstmtSql.setDouble(8, maxqty);
							pstmtSql.setString(7, refNoold);
							rs = pstmtSql.executeQuery();
							if (rs.next()) 
							{
								SlabNo = rs.getDouble(1);
								efffromdt = rs.getTimestamp(2);
								LotNoFrom = rs.getString(3);

							}
							rs.close();
							rs = null;
							pstmtSql.close();
							pstmtSql = null;
						} else 
						{
							//Modified By Pavan Rane 05dec19 start[issue as previous valid pricelist expired wrongly]
							//sql = "select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from >= ? and list_type = ? and min_qty >= ? and max_qty <= ? and ref_no is null";
							//sql = "select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from <= ? and and lot_no__to >= ? and list_type = ? and min_qty <= ? and max_qty >= ? and ref_no is null";
							sql = "select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from <= ? and and lot_no__to >= ? and list_type = ? and ref_no is null";
							pstmtSql = conn.prepareStatement(sql);
							// pstmtSql.setDouble(1, slabno);
							// pstmtSql.setTimestamp(2, efffromdt);
							// pstmtSql.setString(3, lotnofrom);
							pstmtSql.setString(1, plist);
							pstmtSql.setString(2, itemcode);
							pstmtSql.setString(3, unit);
							pstmtSql.setString(4, lotnofrom);
							/*pstmtSql.setString(5, listtype);
							pstmtSql.setDouble(6, minqty);
							pstmtSql.setDouble(7, maxqty);*/
							pstmtSql.setString(5, lotnofrom);
							pstmtSql.setString(6, listtype);
							//pstmtSql.setDouble(7, minqty);
							//pstmtSql.setDouble(8, maxqty);
							rs = pstmtSql.executeQuery();
							if (rs.next()) {
								SlabNo = rs.getDouble(1);
								efffromdt = rs.getTimestamp(2);
								System.out.println("efffromdt++++" + efffromdt);
								LotNoFrom = rs.getString(3);
							}
							rs.close();
							rs = null;
							pstmtSql.close();
							pstmtSql = null;
						}

						ValidUpTo = utilMethods.RelativeDate((efffromdt), -1);
						/*
						 * Len=LotNoFrom.trim().length(); String s=LotNoFrom.trim();
						 * System.out.println("String s"+s); Ctr=1; OrigStr=""; Set = ""; NewStr = "";
						 * char right1= s.charAt(s.length()-1); // char lotnoform=LotNoFrom
						 * .charAt(Len-1); //LotNoFrom.sub if(Character.isDigit(right1) ) { do {
						 * if(Character.isDigit(right1) ) { left1=s.charAt(0); if(left1=='0') {
						 * if(s.length() > 2) { mid1= s.substring(2); Set=Set + '0'; }
						 * 
						 * } else { OldLen = LotNoFrom.trim().length(); New = OldLen - 1; String
						 * new1=Integer.toString(New); NewLen = new1.trim().length(); if(OldLen !=
						 * NewLen); { int diff2=(int) (OldLen - NewLen); for(int s2=0;s2<diff2;s2++) {
						 * NewStr=NewStr.concat("0"); System.out.println("@@@@@@@@@New String"+NewStr);
						 * } }
						 * 
						 * } } else { OrigStr = Character.toString(left1); Set = Set + OrigStr;
						 * LotNoFrom = (mid1+ 1); } Ctr++; }while(Ctr <= Len); if(New==0) { LotNoFrom =
						 * Set +NewStr.trim(); } else { String a1=Integer.toString(New); LotNoFrom = Set
						 * + NewStr.trim() +a1.trim(); }
						 */
						String s2 = LotNoFrom.trim();
						System.out.println("Lotnumber [" + s2.length()+"]");
						left = 0;
						char right = s2.charAt(s2.length() - 1);
						int diff3 = 0, cntt = 0;
						String original = "", result = "", result2 = "";
						int length = LotNoFrom.trim().length();

						/*
						 * System.out.println("insert in count"); Len=LotNoFrom.trim().length(); Ctr=1;
						 * String s2=LotNoFrom.trim(); //String chekStr=LotNoFrom.trim();
						 */
						System.out.println("s2[" + s2+"]");

						// char right= s2.charAt(s2.length()-1);

						System.out.println("right s2[" + right+"]");
						//Pavan Rane 21aug19[to set allow alphbet at right side]....start
						if(!isNumber(right))
						{
							LotNoFrom ="";
							for ( int i = 0;  i <= s2.length()-2 ; i++ )
							{					
								 LotNoFrom = LotNoFrom + s2.charAt(i); 
								 System.out.println("LotNoFrom......["+LotNoFrom+"]");						
							} 											
							int asciiChar = (int)right;		
							System.out.println("--asciiChar--::["+asciiChar+"]");
							char prevChar = (char) (asciiChar-1);
							System.out.println("--Char--::["+asciiChar+"]");
							LotNoFrom = LotNoFrom + String.valueOf(prevChar);
							System.out.println("929 if char LotNoFrom["+LotNoFrom+"]");
						}else 
						{
						//if (isnumberStr(s2)) { 
						//Pavan Rane 21aug19[to set allow alphbet at right side]....end
							if (isNumber(s2.charAt(s2.length() - 1))) 
							{
								cnt1 = 0;
								for (int i = length - 1; i >= 0; i--) 
								{
									Character character = LotNoFrom.trim().charAt(i);
									System.out.println("Char[" + character+"]");

									boolean flag = isNumber(character);

									if (!flag) {
										result = result + LotNoFrom.trim().charAt(i);
										break;
									}
									System.out.println("result[" + result+"]");
									cnt1++;
									System.out.println("Count is[" + cnt1+"]");
								}
								System.out.println("Result of entered string is: [" + result+"] cnt1["+cnt1+"]");
								//System.out.println("Count is:::" + cnt1);
								int testOrginal = 0;
								long sub = 0;//int sub = 0;
								testOrginal = length - cnt1;

								System.out.println("testOrginal[" + testOrginal + "]");
								String testSub1 = LotNoFrom.trim().substring(0, testOrginal);
								System.out.println("testSub1::-[" + testSub1 + "]");
								int testSub = testSub1.length();
								System.out.println("testSub" + testSub);
								String testSub2 = LotNoFrom.trim().substring(s2.length() - cnt1);
								System.out.println("testSub2::-" + testSub2);
								sub = Long.parseLong(testSub2);//sub = Integer.parseInt(testSub2);
								int u2 = testSub2.length();
								long OrgSub = sub - 1;//int OrgSub = sub - 1;
								String v1 = String.valueOf(OrgSub);
								int v2 = v1.length();
								System.out.println("V2:[" + v2+"]");
								int z = u2 - v2;
								System.out.println("z:[" + z+"]");
								if (z != 0) {
									NewStr = "";
									for (int s3 = 0; s3 < z; s3++) {
										//System.out.println("For loop if ");
										NewStr = NewStr.concat("0");
										System.out.println("@@@@@@@@@New String:::::::::::[" + NewStr.length()+"]");
									}
								}
								int q = NewStr.length();
								String a2 = Long.toString(OrgSub);//String a2 = Integer.toString(OrgSub);
								System.out.println("NewStr[" + NewStr+"]");
								LotNoFrom = testSub1.trim() + NewStr.trim() + a2;
								System.out.println("LotNoFrom1@@@@LotNoFrom@@@@:[" + LotNoFrom+"]");
							}
						}
						if (dbName.equalsIgnoreCase("db2")) 
						{
							validupto = (ValidUpTo);
							if (refNoold != null && refNoold.trim().length() > 0) 
							{
								//sql = "update pricelist set valid_upto = ?,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  =?";
								sql = "update pricelist set valid_upto = ?,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and slab_no = ? and ref_no  =? and lot_no__from < ?";
								pstmtSql = conn.prepareStatement(sql);
								pstmtSql.setTimestamp(1, validupto);
								pstmtSql.setString(2, LotNoFrom);
								pstmtSql.setString(3, plist);
								pstmtSql.setString(4, itemcode);
								pstmtSql.setString(5, unit);
								pstmtSql.setString(6, listtype);
								//pstmtSql.setDouble(7, minqty);
								//pstmtSql.setDouble(8, maxqty);
								pstmtSql.setDouble(7, SlabNo);
								pstmtSql.setString(8, refNoold);
								pstmtSql.setString(9, LotNoFrom);
								int upcnt1 = pstmtSql.executeUpdate();
								System.out.println("No. of rows updated11111111111------>>" + upcnt1);
								if (pstmtSql != null) {
									pstmtSql.close();
									pstmtSql = null;
								}
							} else 
							{
								//sql = "update pricelist set valid_upto = ?,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  is null ";
								sql = "update pricelist set valid_upto = ?,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and slab_no = ? and ref_no  is null and lot_no__from < ?";
								pstmtSql = conn.prepareStatement(sql);
								pstmtSql.setTimestamp(1, ValidUpTo);
								pstmtSql.setString(2, LotNoFrom);
								pstmtSql.setString(3, plist);
								pstmtSql.setString(4, itemcode);
								pstmtSql.setString(5, unit);
								pstmtSql.setString(6, listtype);
								//pstmtSql.setDouble(7, minqty);
								//pstmtSql.setDouble(8, maxqty);
								pstmtSql.setDouble(7, SlabNo);
								pstmtSql.setString(8, LotNoFrom);
								int upcnt1 = pstmtSql.executeUpdate();
								System.out.println("No. of rows updated11111111111------>>" + upcnt1);
								if (pstmtSql != null) {
									pstmtSql.close();
									pstmtSql = null;
								}
								
							}

						} else 
						{
							if (refNoold != null && refNoold.trim().length() > 0) 
							{
								//sql = "update pricelist set valid_upto = ?,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  =?";
								sql = "update pricelist set valid_upto = ?,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and slab_no = ? and ref_no  =? and lot_no__from < ?";
								pstmtSql = conn.prepareStatement(sql);
								pstmtSql.setTimestamp(1, ValidUpTo);
								pstmtSql.setString(2, LotNoFrom);
								pstmtSql.setString(3, plist);
								pstmtSql.setString(4, itemcode);
								pstmtSql.setString(5, unit);
								pstmtSql.setString(6, listtype);
								//pstmtSql.setDouble(7, minqty);
								//pstmtSql.setDouble(8, maxqty);
								pstmtSql.setDouble(7, SlabNo);
								pstmtSql.setString(8, refNoold);
								pstmtSql.setString(9, LotNoFrom);
								int upcnt1 = pstmtSql.executeUpdate();
								System.out.println("No. of rows updated11111111111------>>" + upcnt1);
								if (pstmtSql != null) {
									pstmtSql.close();
									pstmtSql = null;
								}
							} else 
							{
								//sql = "update pricelist set valid_upto = ? ,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  is null ";
								sql = "update pricelist set valid_upto = ? ,lot_no__to = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and slab_no = ? and ref_no  is null and lot_no__from < ?";
								pstmtSql = conn.prepareStatement(sql);
								pstmtSql.setTimestamp(1, ValidUpTo);
								pstmtSql.setString(2, LotNoFrom);
								pstmtSql.setString(3, plist);
								pstmtSql.setString(4, itemcode);
								pstmtSql.setString(5, unit);
								pstmtSql.setString(6, listtype);
								//pstmtSql.setDouble(7, minqty);
								//pstmtSql.setDouble(8, maxqty);
								pstmtSql.setDouble(7, SlabNo);
								pstmtSql.setString(8, LotNoFrom);
								int upcnt1 = pstmtSql.executeUpdate();
								System.out.println("No. of rows updated11111111111------>>" + upcnt1);
								if (pstmtSql != null) {
									pstmtSql.close();
									pstmtSql = null;
								}
							}
							updLotFrom = LotNoFrom;
						}
					}
				}
				// split the batch if the entered batch no is already existing in the master
				// pricelist
				recordfound = false;
				if (listtype.equalsIgnoreCase("B")) 
				{
					LotNoFrom = (String) PList.get("LOT_NO__FROM");
					//System.out.println("@@@@@lot number from2222222222222" + LotNoFrom);
					BaseLogger.log("3", null, null, "Splitting logic LotNoFrom["+LotNoFrom+"]");
					//sql = "select count(1) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?";
					sql = "select count(1) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? ";
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setString(1, plist);
					pstmtSql.setString(2, itemcode);
					pstmtSql.setString(3, unit);
					pstmtSql.setString(4, listtype);
					//pstmtSql.setString(5, LotFrBrow);
					pstmtSql.setString(5, LotNoFrom);
					//pstmtSql.setString(6, LotNoToBrow);
					pstmtSql.setString(6, updLotFrom);
					//pstmtSql.setDouble(7, minqty);
					//pstmtSql.setDouble(8, maxqty);
					rs = pstmtSql.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
						//System.out.println("Batch sql count" + cnt);
					}
					System.out.println("llCount Split logic @@@1171-------->>[" + cnt + "]");
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmtSql != null) {
						pstmtSql.close();
						pstmtSql = null;
					}

					if (cnt > 0) {
						LotNoFrom = (String) PList.get("LOT_NO__FROM");
						LotNoTo = (String) PList.get("LOT_NO__TO");
						nextlotnofr = LotNoFrom;
					  //sql = "select lot_no__from, lot_no__to from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?  order by lot_no__from";
						//sql = "select lot_no__from, lot_no__to, eff_from, valid_upto, min_qty, max_qty, rate,rate_type, price_list__parent, ref_no, ref_no_old, min_rate, max_rate from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ?  and lot_no__to >= ? and min_qty <= ? and max_qty >= ?  order by lot_no__from";
						sql = "select lot_no__from, lot_no__to, eff_from, valid_upto, min_qty, max_qty, rate,rate_type, price_list__parent, ref_no, ref_no_old, min_rate, max_rate from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ?  and lot_no__to >= ? order by lot_no__from";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, plist);
						pstmtSql.setString(2, itemcode);
						pstmtSql.setString(3, unit);
						pstmtSql.setString(4, listtype);
						//pstmtSql.setString(5, lotnofrom);
						pstmtSql.setString(5, LotNoFrom);
						//pstmtSql.setString(6, LotNoToBrow);
						pstmtSql.setString(6, updLotFrom);
						//pstmtSql.setDouble(7, minqty);
						//pstmtSql.setDouble(8, maxqty);
						rs = pstmtSql.executeQuery();
						if (rs.next()) 
						{
							currentlotnofr = rs.getString(1);
							currentlotnoto = rs.getString(2);
							currentlotnofr = rs.getString("lot_no__from");
							currentlotnoto = rs.getString("lot_no__to");
							efffromSplit = rs.getTimestamp("eff_from");
							ValidUpToSplit = rs.getTimestamp("valid_upto");
							minqtySplit = rs.getDouble("min_qty");
							maxqtySplit = rs.getDouble("max_qty");
							rate1Split = rs.getDouble("rate");
							ratetypeSplit = rs.getString("rate_type");
							PriceListParent1Split = rs.getString("price_list__parent");
							refNo1Split = rs.getString("ref_no");
							refNoold1Split = rs.getString("ref_no_old");
							minRate1Split = rs.getDouble("min_rate");
							MaxRate1Split = rs.getDouble("max_rate");
							// }
							recordfound = true;
							System.out.println("Recode Found@@@@@@@@@@@@@" + recordfound);
							currentlotnofrtemp = currentlotnofr;
							currentlotnototemp = currentlotnoto;

/*							Len = currentlotnofr.trim().length();
							Ctr = 1;
							OrigStr = "";
							Set = "";
							NewStr = "";
							New = 0;
							int diff3 = 0, cntt = 0;
							String original = "", result = "", result2 = "";

							String x = LotNoFrom.trim();
							System.out.println("@@@@@x" + x);
							String y = currentlotnofr.trim();
							System.out.println("@@@@@x" + y);
							String s2 = y;
							if (x.compareTo(y) < 0) 
							{
								
								 * char right3= y.charAt(y.length()-1); if(Character.isDigit(right3) ) { do {
								 * if(Character.isDigit(right3) ) { left3=y.charAt(0); if(left3=='0') {
								 * 
								 * if(y.length()>2) {
								 * 
								 * mid3= y.substring(2); Set=Set + '0'; } } else { OldLen =
								 * currentlotnofr.trim().length(); New = OldLen - 1; String
								 * new1=Integer.toString(New); NewLen = new1.trim().length(); if(OldLen !=
								 * NewLen); { int diff1=(int) (OldLen - NewLen); for(int s=0;s<diff1;s++) {
								 * NewStr=NewStr.concat("0"); System.out.println("@@@@@@@@@New String"+NewStr);
								 * } }
								 * 
								 * } } else { OrigStr = Character.toString(left3); Set = Set + OrigStr;
								 * LotNoFrom = (mid3 + 1); } Ctr++; }while(Ctr <= Len); // lotnoto = Set +
								 * trim(ls_NewStr) +string(ll_New) //ls_lot_no__fr = ls_next_lot_no_fr String
								 * a2=Integer.toString(New); lotnoto = Set + NewStr.trim() +a2.trim(); lotnofr =
								 * nextlotnofr;
								 
								int length = currentlotnofr.trim().length();
								System.out.println("s22222222222222" + s2);

								// char right= s2.charAt(s2.length()-1);

								// System.out.println("right s22222222222222"+right);
								//Pavan Rane 21aug19[to set allow alphbet at right side]....start
								char right= s2.charAt(s2.length()-1);
								if(!isNumber(right))
								{
									LotNoFrom ="";
									for ( int i = 0;  i <= s2.length()-2 ; i++ )
									{					
										 LotNoFrom = LotNoFrom + s2.charAt(i); 
										 System.out.println("LotNoFrom......["+LotNoFrom+"]");						
									} 				
									int asciiChar = (int)right;		
									System.out.println("--asciiChar--::["+asciiChar+"]");
									char prevChar = (char) (asciiChar-1);
									System.out.println("--Char--::["+asciiChar+"]");
									LotNoFrom = LotNoFrom + String.valueOf(prevChar);
									System.out.println("1028 if char LotNoFrom["+LotNoFrom+"]");
								}else 															
								//if (isNumber(s2.charAt(s2.length() - 1)))
								//Pavan Rane 21aug19[to set allow alphbet at right side]....end	
								{
									cnt1 = 0;
									for (int i = length - 1; i >= 0; i--) 
									{
										Character character = currentlotnofr.trim().charAt(i);
										System.out.println("Char" + character);
										boolean flag = isNumber(character);
										if (!flag) {
											result = result + currentlotnofr.trim().charAt(i);
											break;
										}
										System.out.println("result" + result);
										cnt1++;
										System.out.println("Count is" + cnt1);
									}
									System.out.println("Result of entered string is: " + result);
									System.out.println("Count is:::" + cnt1);
									int testOrginal = 0;
									int sub = 0;
									testOrginal = length - cnt1;

									System.out.println("testOrginal[" + testOrginal + "]");
									String testSub1 = currentlotnofr.trim().substring(0, testOrginal);
									System.out.println("testSub1::-[" + testSub1 + "]");
									int testSub = testSub1.length();
									System.out.println("testSub" + testSub);
									String testSub2 = currentlotnofr.trim().substring(s2.length() - cnt1);
									System.out.println("testSub2::-" + testSub2);
									sub = Integer.parseInt(testSub2);
									int u2 = testSub2.length();
									int OrgSub = sub - 1;
									String v1 = String.valueOf(OrgSub);
									int v2 = v1.length();
									System.out.println("V2:" + v2);
									int z = u2 - v2;
									System.out.println("z:" + z);
									if (z != 0) {
										for (int s3 = 0; s3 < z; s3++) {
											System.out.println("For loop if ");
											NewStr = NewStr.concat("0");
											System.out.println("@@@@@@@@@New String:::::::::::" + NewStr.length());
										}
									}
									int q = NewStr.length();
									String a2 = Integer.toString(OrgSub);
									System.out.println("NewStr" + NewStr);
									lotnoto = testSub1.trim() + NewStr.trim() + a2;
									lotnofr = nextlotnofr;
									System.out.println("LotNoFrom1@@@@LotNoFrom@@@@:" + lotnoto);
								}

							}
							Len1 = currentlotnoto.trim().length();
							Ctr = 1;
							OrigStr = "";
							Set = "";
							NewStr = "";
							New = 0;

							String p = currentlotnoto.trim();
							String q = LotNoTo.trim();
							s2 = currentlotnoto.trim();
							int length = currentlotnoto.trim().length();
							System.out.println("p--[" + p + "]q--[" + q + "]");
							System.out.println("s22222222222222" + s2);
							if (q.compareTo(p) < 0) 
							{
								
								 * char right4= y.charAt(y.length()-1); if(Character.isDigit(right4) ) { do {
								 * if(Character.isDigit(right4) ) { left4 =y.charAt(0); if(left4=='0') {
								 * if(y.length()>2) { mid4= y.substring(2); Set=Set + '0'; } } else { OldLen =
								 * currentlotnoto.trim().length(); New = OldLen - 1; String
								 * new1=Integer.toString(New); NewLen = new1.trim().length(); if(OldLen !=
								 * NewLen); { int diff=(int) (OldLen - NewLen); for(int s=0;s<diff;s++) {
								 * NewStr=NewStr.concat("0"); System.out.println("@@@@@@@@@New String"+NewStr);
								 * } //NewStr = fill('0',OldLen - NewLen); }
								 * 
								 * } } else { OrigStr = Character.toString(left4); Set = Set + OrigStr;
								 * LotNoFrom = (mid4+ 1); } Ctr++; }while(Ctr <= Len); // lotnoto = Set +
								 * trim(ls_NewStr) +string(ll_New) //ls_lot_no__fr = ls_next_lot_no_fr String
								 * a2=Integer.toString(New); lotnofr = Set + NewStr.trim() +a2.trim(); lotnofr =
								 * nextlotnofr;
								 
								//Pavan Rane 21aug19[to set allow alphbet at right side]....start
								char right= s2.charAt(s2.length()-1);
								if(!isNumber(right))
								{
									LotNoFrom ="";
									for ( int i = 0;  i <= s2.length()-2 ; i++ )
									{					
										 LotNoFrom = LotNoFrom + s2.charAt(i); 
										 System.out.println("LotNoFrom......["+LotNoFrom+"]");						
									} 				
									int asciiChar = (int)right;		
									System.out.println("--asciiChar--::["+asciiChar+"]");
									char prevChar = (char) (asciiChar-1);
									System.out.println("--Char--::["+asciiChar+"]");
									LotNoFrom = LotNoFrom + String.valueOf(prevChar);
									System.out.println("1291 if char LotNoFrom["+LotNoFrom+"]");
								}else 															
								//if (isNumber(s2.charAt(s2.length() - 1)))
								//Pavan Rane 21aug19[to set allow alphbet at right side]....end								
								{
									cnt1 = 0;
									for (int i = length - 1; i >= 0; i--) 
									{
										Character character = currentlotnoto.trim().charAt(i);
										System.out.println("Char" + character);
										boolean flag = isNumber(character);
										if (!flag) {
											result = result + currentlotnoto.trim().charAt(i);
											break;
										}
										System.out.println("result" + result);
										cnt1++;
										System.out.println("Count is" + cnt1);
									}
									System.out.println("Result of entered string is: " + result);
									System.out.println("Count is:::" + cnt1);
									int testOrginal = 0;
									int sub = 0;
									testOrginal = length - cnt1;

									System.out.println("testOrginal[" + testOrginal + "]");
									String testSub1 = currentlotnofr.trim().substring(0, testOrginal);
									System.out.println("testSub1::-[" + testSub1 + "]");
									int testSub = testSub1.length();
									System.out.println("testSub" + testSub);
									String testSub2 = currentlotnoto.trim().substring(s2.length() - cnt1);
									System.out.println("testSub2::-" + testSub2);
									sub = Integer.parseInt(testSub2);
									int u2 = testSub2.length();
									int OrgSub = sub + 1;
									String v1 = String.valueOf(OrgSub);
									int v2 = v1.length();
									System.out.println("V2:" + v2);
									int z = u2 - v2;
									System.out.println("z:" + z);
									if (z != 0) 
									{
										for (int s3 = 0; s3 < z; s3++) 
										{
											System.out.println("For loop if ");
											NewStr = NewStr.concat("0");
											System.out.println("@@@@@@@@@New String:::::::::::" + NewStr.length());
										}
									}
									// int q=NewStr.length();
									String a2 = Integer.toString(OrgSub);
									System.out.println("NewStr" + NewStr);
									nextlotnofr = testSub1.trim() + NewStr.trim() + a2;
									lotnofr = nextlotnofr;
									System.out.println("LotNoFrom1@@@@LotNoFrom@@@@:" + lotnofr);

								}

							}
*/							
							
							String s2 = LotNoTo.trim();
							System.out.println("Lotnumber [" + s2.length()+"]");
							left = 0;
							char right = s2.charAt(s2.length() - 1);
							//int diff3 = 0;
							String original = "", result = "", result2 = "";
							NewStr = "";
							int length = LotNoFrom.trim().length();
							/*
							 * System.out.println("insert in count"); Len=LotNoFrom.trim().length(); Ctr=1;
							 * String s2=LotNoFrom.trim(); //String chekStr=LotNoFrom.trim();
							 */
							System.out.println("s2[" + s2+"]");
							// char right= s2.charAt(s2.length()-1);
							//Pavan Rane 21aug19[to set allow alphbet at right side]....start
							if(!isNumber(right))
							{
								LotNoFrom ="";
								for ( int i = 0;  i <= s2.length()-2 ; i++ )
								{					
									 LotNoFrom = LotNoFrom + s2.charAt(i); 
									 System.out.println("LotNoFrom......["+LotNoFrom+"]");						
								} 																
								int asciiChar = (int)right;		
								System.out.println("--asciiChar--::["+asciiChar+"]");
								char prevChar = (char) (asciiChar-1);
								System.out.println("--Char--::["+asciiChar+"]");
								LotNoFrom = LotNoFrom + String.valueOf(prevChar);
								System.out.println("631 if char LotNoFrom["+LotNoFrom+"]");
							}else
							//if (isNumber(s2.charAt(s2.length() - 1))) 
							{
								cnt1 = 0;
								System.out.println("right s2222[" + right+"]");
								for (int i = length - 1; i >= 0; i--) 
								{
									Character character = LotNoFrom.trim().charAt(i);
									System.out.println("Char [" + character+"]");

									boolean flag = isNumber(character);

									if (!flag) 
									{
										result = result + LotNoFrom.trim().charAt(i);
										break;
									}
									System.out.println("result[" + result+"]");
									cnt1++;
									System.out.println("Count is[" + cnt1+"]");
								}
								System.out.println("Result of entered string is: [" + result+"] cnt1["+cnt1+"]");
								//System.out.println("Count is:::" + cnt1);
								int testOrginal = 0;
								long sub = 0;//int sub = 0;
								testOrginal = length - cnt1;

								System.out.println("testOrginal[" + testOrginal + "]");
								String testSub1 = LotNoFrom.trim().substring(0, testOrginal);
								System.out.println("testSub1::-[" + testSub1 + "]");
								int testSub = testSub1.length();
								System.out.println("testSub[" + testSub+"]");
								//String testSub2 = LotNoFrom.trim().substring(s2.length() - cnt1);
								String testSub2 = LotNoTo.trim().substring(s2.length() - cnt1);
								System.out.println("testSub2::-[" + testSub2+"]");
								sub = Long.parseLong(testSub2);//sub = Integer.parseInt(testSub2);
								int u2 = testSub2.length();
								long OrgSub = sub + 1;//int OrgSub = sub + 1;
								String v1 = String.valueOf(OrgSub);
								int v2 = v1.length();
								System.out.println("V2:[" + v2+"]");
								int z = u2 - v2;
								System.out.println("z:[" + z+"]");
								if (z != 0) 
								{
									NewStr = "";
									for (int s3 = 0; s3 < z; s3++) 
									{
										//System.out.println("For loop if ");
										NewStr = NewStr.concat("0");
										System.out.println("@@@@@@@@@New String:::::::::::[" + NewStr.length()+"]");
									}
								}
								int q = NewStr.length();
								String a2 = Long.toString(OrgSub);//String a2 = Integer.toString(OrgSub);
								System.out.println("NewStr[" + NewStr+"]");
								nextlotnofr = testSub1.trim() + NewStr.trim() + a2;
							}
							
							int temp = 0;
							BaseLogger.log("3", null, null, "Split comparing currentlotnofrtemp["+currentlotnofrtemp+"] LotNoFromsss["+LotNoFrom+"]");
							if (!currentlotnofrtemp.equalsIgnoreCase(LotNoFrom)) {//if (currentlotnofrtemp != LotNoFrom) {
								// temp =Integer.parseInt(LotNoFrom);
								// int temp1=Integer.parseInt(LotNoTo);
								LotNoFroma.add(nextlotnofr);
								//LotNoToa.add(lotnoto);
							}else {
								recordfound = false;
							}
							// int tempnew=0;
							/*
							 * if(currentlotnototemp <= LotNoTo) { tempnew =Integer.parseInt(LotNoFrom); int
							 * temp2=Integer.parseInt(LotNoTo); nextlotnofr=LotNoFroma[temp]+1;
							 * lotnoto=LotNoToa[temp2]+1; }
							 */
						}
						//Added By Pavan Rane 05dec19 start[closed open cursors]
						rs.close();
						rs = null;
						pstmtSql.close();
						pstmtSql =null;
						System.out.println("max(slab) LotNoFroma.size():["+LotNoFroma.size()+"]");
						for (int i = 0; i < LotNoFroma.size(); i++) 
						{
							lotnofr = LotNoFroma.get(i);
							//lotnoto = LotNoToa.get(i);
							sql = "select  max(slab_no)  from pricelist where price_list = ? ";
									//+ " AND item_code = ? ";// removed item code filter by nandkumar gadkari on 16/05/19
							pstmtSql = conn.prepareStatement(sql);
							pstmtSql.setString(1, plist);
						//	pstmtSql.setString(2, itemcode);
							rs = pstmtSql.executeQuery();
							if (rs.next()) {
								LineNo = rs.getInt(1);
								System.out.println("line no@@@@@@@1572# >>[ " + LineNo+"]");
							}
							rs.close();
							rs = null;
							pstmtSql.close();
							pstmtSql = null;
							//if (LineNo == 0) {
							LotNoFromUpd = (String) PList.get("LOT_NO__FROM");
							//LotNoToUpd = (String) PList.get("LOT_NO__TO");
							BaseLogger.log("3", null, null, "Spliting.....["+LotNoFromUpd+"]>priceListOldto["+priceListOldto+"]");
							if(LotNoFromUpd.trim().compareTo(priceListOldto.trim()) < 0) {							
							
								System.out.println("Insert line no");
								LineNo++;
								sql = "insert into pricelist (price_list, item_code,unit,list_type,slab_no,eff_from,valid_upto,lot_no__from,lot_no__to,min_qty,max_qty,rate,rate_type,min_rate,chg_date,chg_user,chg_term,max_rate,order_type,	price_list__parent,	chg_ref_no,	ref_no,ref_no_old  )"
										+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								pstmtInsert = conn.prepareStatement(sql);
								pstmtInsert.setString(1, plist);
								pstmtInsert.setString(2, itemcode);
								pstmtInsert.setString(3, unit);
								pstmtInsert.setString(4, listtype);
								pstmtInsert.setDouble(5, LineNo);
								pstmtInsert.setTimestamp(6, efffromSplit);
								pstmtInsert.setTimestamp(7, ValidUpToSplit);
								pstmtInsert.setString(8, lotnofr);
								pstmtInsert.setString(9, priceListOldto);
								pstmtInsert.setDouble(10, minqtySplit);
								pstmtInsert.setDouble(11, maxqtySplit);
								pstmtInsert.setDouble(12, rate1Split);
								pstmtInsert.setString(13, ratetypeSplit);
								pstmtInsert.setDouble(14, minRate1Split);
								chgDate = new java.sql.Timestamp(System.currentTimeMillis());
								pstmtInsert.setTimestamp(15, chgDate);
								pstmtInsert.setString(16, userId);
								pstmtInsert.setString(17, termId);
								pstmtInsert.setDouble(18, MaxRate1Split);
								pstmtInsert.setString(19, orderType1);
								pstmtInsert.setString(20, PriceListParent1Split);
								pstmtInsert.setString(21, chgref1);
								pstmtInsert.setString(22, refNo1Split);
								pstmtInsert.setString(23, refNoold1Split);							
								UpdCnt = pstmtInsert.executeUpdate();
								pstmtInsert.close();
								pstmtInsert = null;

							}else {
								recordfound = false;
							}

						}
					}
				}
				//To clear the list for each record in list
				LotNoFroma.clear();
				//LotNoToa.clear();
				if (LineNo == 0) {
					sql = "select  max(slab_no)  from pricelist where price_list = ? ";
							//+ " AND item_code = ? ";// removed item code filter by nandkumar gadkari on 16/05/19
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setString(1, plist);
					//pstmtSql.setString(2, itemcode);
					rs = pstmtSql.executeQuery();
					if (rs.next()) {
						LineNo = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmtSql.close();
					pstmtSql = null;
				}
				System.out.println("Record found@@@@@@@@@@@@@" + recordfound);
				today = new Timestamp(System.currentTimeMillis());
				ChgDate = today;
				//20jun19 start
				int newCount = 0;
				//sql = "select count(*) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?  order by lot_no__from";
				sql = "select count(*) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? order by lot_no__from";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, plist);
				pstmtSql.setString(2, itemcode);
				pstmtSql.setString(3, unit);
				pstmtSql.setString(4, listtype);
				pstmtSql.setString(5, lotnofrom);
				pstmtSql.setString(6, lotnoto1);
				//pstmtSql.setDouble(7, minqty);
				//pstmtSql.setDouble(8, maxqty);
				rs = pstmtSql.executeQuery();
				if(rs.next())
				{
					newCount = rs.getInt(1);
					System.out.println("Before insert new Lot Count::["+newCount+"]");
				}
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
				//20jun19 end
				if (recordfound == false || newCount == 0)//if (recordfound == false) 
				{
					LineNo++;
					sql = "insert into pricelist (price_list,item_code,unit,list_type,slab_no,eff_from,valid_upto,lot_no__from,lot_no__to,min_qty,max_qty,rate,rate_type,min_rate,chg_date,chg_user,chg_term,max_rate,order_type,price_list__parent,	chg_ref_no,	ref_no,ref_no_old  )"
							+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstmtInsert = conn.prepareStatement(sql);
					pstmtInsert.setString(1, plist);
					pstmtInsert.setString(2, itemcode);
					pstmtInsert.setString(3, unit);
					pstmtInsert.setString(4, listtype);
					pstmtInsert.setDouble(5, LineNo);
					pstmtInsert.setTimestamp(6, (Timestamp) PList.get("EFF_FROM"));
					pstmtInsert.setTimestamp(7, (Timestamp) PList.get("VALID_UPTO"));
					pstmtInsert.setString(8, lotnofrom);// lotnofr)
					pstmtInsert.setString(9, lotnoto1);// lotnoto)
					pstmtInsert.setDouble(10, minqty);
					pstmtInsert.setDouble(11, maxqty);
					pstmtInsert.setDouble(12, rate1);
					pstmtInsert.setString(13, ratetype);
					//pstmtInsert.setDouble(14, rate1);//Pavan R 23dec19 to update min_rate 
					pstmtInsert.setDouble(14, minrate);
					chgDate = new java.sql.Timestamp(System.currentTimeMillis());
					pstmtInsert.setTimestamp(15, chgDate);
					pstmtInsert.setString(16, userId);
					pstmtInsert.setString(17, termId);
					//pstmtInsert.setDouble(18, rate1);
					pstmtInsert.setDouble(18, maxrate1);//Pavan R 23dec19 to update min_rate
					pstmtInsert.setString(19, orderType1);
					pstmtInsert.setString(20, PriceListParent1);
					pstmtInsert.setString(21, chgref1);
					pstmtInsert.setString(22, refNo1);
					pstmtInsert.setString(23, refNoold1);
					UpdCnt = pstmtInsert.executeUpdate();
					pstmtInsert.close();
					pstmtInsert = null;

				}

				System.out.println("Insert completed");
				chgDate = new java.sql.Timestamp(System.currentTimeMillis());
				String sql1 = "update pricelist set chg_user =?,chg_term = ?,chg_date = ? where price_list =? and  item_code=? and unit = ? and list_type =?";
				pstmtSql = conn.prepareStatement(sql1);
				chgDate = new java.sql.Timestamp(System.currentTimeMillis());
				pstmtSql.setString(1, userId);
				pstmtSql.setString(2, termId);
				pstmtSql.setTimestamp(3, chgDate);
				pstmtSql.setString(4, plist);
				pstmtSql.setString(5, itemcode);
				pstmtSql.setString(6, unit);
				pstmtSql.setString(7, listtype);
				updCnt = pstmtSql.executeUpdate();
				pstmtSql.close();
				pstmtSql = null;

			}//end for PricelistGen			
			// try end
		} catch (Exception e) 
		{	
			//Pavan Rane 20may19 [Exception handling to be proper and added finally]
			System.out.println("Exception :PriceListconf : insertPricelist()::" + e.getMessage() + ":");			
			e.printStackTrace();
			throw new ITMException(e);
		}finally {

			try {
				if(rs != null) {
					rs.close();
					rs = null;
				}
				if(rsInner != null) {
					rsInner.close();
					rsInner = null;
				}
				if(pstmtSql != null) {
					pstmtSql.close(); 
					pstmtSql = null;
				}
				if(pstmtInsert != null) {
					pstmtInsert.close();
					pstmtInsert = null;
				}
				if(pstmtInner != null) {
					pstmtInner.close();
					pstmtInner = null;
				}
			}catch(Exception ex)
			{				
			}
		}
		return retString;
	}

	public String confirmPlist(String tranId, Connection conn, String xtraParams) throws RemoteException, ITMException 
	{
		PreparedStatement pstmtSql = null, pstmtInner = null;
		ResultSet rs = null, rsInner = null;
		String sql = "";
		String dbName = "";
		String retString = "";
		String errCode = "", priceList = "", confirmed = "", empcode = "", itemCode = "", lotNoForm = "";
		String lotNoTo = "", rateType = "", chgref = "", listType = "", PriceListParent = "", PriceList = "";
		String orderType = "", manageType = "", plist1 = "", plist2 = "", plist3 = "", plist4 = "", plist5 = "",
				plist6 = "", plist7 = "", plist8 = "";
		String plist9 = "", plist10 = "", plist11 = "", plist12 = "", unit = "", refNoold = "", refNO = "", data = "",
				calc = "", Str = "";
		String calcmeth = "", plisttar = "";
		java.sql.Timestamp tranDate = null, confDate = null, efffrom = null, validup = null;
		double rate = 0.0;
		double minrate = 0.0;
		double maxrate = 0.0;
		double rate1 = 0.0;
		double rate2 = 0.0;
		double rate3 = 0.0;
		double rate4 = 0.0;
		double rate5 = 0.0;
		double rate6 = 0.0;
		double rate7 = 0.0;
		double rate8 = 0.0;
		double rate9 = 0.0;
		double rate10 = 0.0;
		double rate11 = 0.0;
		double rate12 = 0.0;
		double minqty = 0.0;
		double maxqty = 0.0;
		int lineno = 0;
		String loginEmpCode = "";
		ArrayList PricelistGen = new ArrayList();
		ArrayList PricelistVar = new ArrayList();
		HashMap<String, Object> PList = null;
		double finalRate = 0.0;
		try 
		{
			DistCommon distCommon = new DistCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			ValidatorEJB validatorEJB = null;
			System.out.println("Printing loginEmpCode---1-----" + loginEmpCode + "--");
			dbName = CommonConstants.DB_NAME;
			if (dbName.equalsIgnoreCase("db2")) {
				sql = "select tran_date, price_list,confirmed,conf_date from pricelist_hdr where tran_id =? for update with RS ";

			} else if (dbName.equalsIgnoreCase("mysql")) {
				sql = "select tran_date, price_list,confirmed,conf_date from pricelist_hdr where tran_id =? for update";
			} else if (dbName.equalsIgnoreCase("mssql")) {
				sql = "select tran_date, price_list,confirmed,conf_date from pricelist_hdr where tran_id =?";
			} else {
				sql = "select tran_date, price_list,confirmed,conf_date from pricelist_hdr where tran_id =?  for update nowait";

			}
			try 
			{
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				if (rs.next()) 
				{
					tranDate = rs.getTimestamp(1);
					priceList = rs.getString(2);
					confirmed = rs.getString(3);
					confDate = rs.getTimestamp(4);
					pstmtSql.close();
					pstmtSql = null;
					rs.close();
					rs = null;
				} else 
				{
					pstmtSql.close();
					pstmtSql = null;
					rs.close();
					rs = null;
					retString = itmDBAccessLocal.getErrorString("", "VTLCKERR", "", "", conn);
					return retString;
				}
			} catch (Exception e) 
			{
				e.printStackTrace();//Pavan Rane 20may19 [Exception handling to be proper]
				retString = itmDBAccessLocal.getErrorString("", "VTLCKERR", "", "", conn);
				throw new ITMException(e);
			}
			sql = "select d.item_code,d.lot_no__from,d.lot_no__to,d.rate,d.rate_type,d.min_rate,d.max_rate,d.chg_ref_no,d.eff_from,d.valid_upto,m.price_list,m.list_type ,m.price_list__parent,m.order_type,m.manage_type,d.min_qty ,d.max_qty,d.unit,d.line_no,h.ref_no,ref_no_old,h.calc_basis from pricelist_hdr h, pricelist_det d, pricelist_mst m where h.tran_id = d.tran_id and h.price_list = m.price_list  and h.tran_id = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			while (rs.next()) 
			{
				PList = new HashMap<String, Object>();
				PList.put("ITEM_CODE", itemCode = rs.getString("item_code"));
				PList.put("LOT_NO__FROM", lotNoForm = rs.getString("lot_no__from"));
				PList.put("LOT_NO__TO", lotNoTo = rs.getString("lot_no__to"));
				PList.put("RATE", rate = rs.getDouble("rate"));
				PList.put("RATE_TYPE", rateType = rs.getString("rate_type"));
				PList.put("MIN_RATE", minrate = rs.getDouble("min_rate"));
				PList.put("MAX_RATE", maxrate = rs.getDouble("max_rate"));
				PList.put("CHG_REF_NO", chgref = rs.getString("chg_ref_no"));
				PList.put("EFF_FROM", efffrom = rs.getTimestamp("eff_from"));
				PList.put("VALID_UPTO", validup = rs.getTimestamp("valid_upto"));
				PList.put("PRICE_LIST", PriceList = rs.getString("price_list"));
				PList.put("LIST_TYPE", listType = rs.getString("list_type"));
				PList.put("PRICE_LIST__PARENT", PriceListParent = rs.getString("price_list__parent"));
				PList.put("ORDER_TYPE", orderType = rs.getString("order_type"));
				PList.put("MANAGE_TYPE", manageType = rs.getString("manage_type"));
				PList.put("MIN_QTY", minqty = rs.getDouble("min_qty"));
				PList.put("MAX_QTY", maxqty = rs.getDouble("max_qty"));
				PList.put("UNIT", unit = rs.getString("unit"));
				PList.put("LINE_NO", lineno = rs.getInt("line_no"));
				PList.put("REF_NO", refNO = rs.getString("ref_no"));
				PList.put("REF_NO_OLD", refNoold = rs.getString("ref_no_old"));
				PList.put("CALC_BASIS", calc = rs.getString("calc_basis"));

				/*
				 * PList = new HashMap(); PList.put(PriceList,"PriceList"); PList.put(itemCode,
				 * "item_code"); PList.put(unit,"unit"); PList.put(listType,"list_type");
				 * PList.put(validup,"valid_upto"); PList.put(lotNoForm,"lot_no__from");
				 * PList.put(lotNoTo,"lot_no__to"); PList.put(minqty,"min_qty");
				 * PList.put(maxqty,"max_qty"); PList.put(rate,"rate");
				 * PList.put(rateType,"rate_type"); PList.put(minrate,"min_rate");
				 * PList.put(maxrate,"max_rate"); PList.put(orderType,"order_type");
				 * PList.put(PriceListParent,"price_list__parent");
				 * PList.put(chgref,"chg_ref_no"); PList.put(refNO,"ref_no");
				 * PList.put(refNoold,"ref_no_old"); PList.put(calc,"calc_basis");
				 */
				PricelistGen = null;
				PricelistGen = new ArrayList();
				PricelistGen.add(PList);
				//System.out.println("[PricelistGen----------------][" + PricelistGen + "]");
				BaseLogger.log("9", null, null, "[PricelistGen----------------][" + PricelistGen + "]");
				retString = insertPricelist(tranId,PList,PricelistGen, conn,xtraParams);
				if ( retString != null && retString.trim().length() > 0) 
				{ return
				  retString; 
				}
				 
				sql = "select calc_method, price_list__tar , price_list__parent from pricelist_mst_det where price_list = ? order by line_no ";
				// pstmtSql = conn.prepareStatement(sql);
				// pstmtSql.setString(1, PriceList);
				// rs = pstmtSql.executeQuery();
				pstmtInner = conn.prepareStatement(sql);
				pstmtInner.setString(1, PriceList);
				rsInner = pstmtInner.executeQuery();
				int cnt = 0;
				while (rsInner.next()) 
				{
					calcmeth = rsInner.getString(1);
					plisttar = rsInner.getString(2);
					PriceListParent = rsInner.getString(3);

					// Pavan R 6may19 to add new map in PricelistVar list
					PList = new HashMap<String, Object>();
					PList.put("PRICE_LIST", plisttar);
					PList.put("PRICE_LIST__PARENT", PriceListParent);

					PList.put("ITEM_CODE", itemCode = rs.getString("item_code"));
					PList.put("LOT_NO__FROM", lotNoForm = rs.getString("lot_no__from"));
					PList.put("LOT_NO__TO", lotNoTo = rs.getString("lot_no__to"));
					PList.put("RATE_TYPE", rateType = rs.getString("rate_type"));
					PList.put("MIN_RATE", minrate = rs.getDouble("min_rate"));
					PList.put("MAX_RATE", maxrate = rs.getDouble("max_rate"));
					PList.put("CHG_REF_NO", chgref = rs.getString("chg_ref_no"));
					PList.put("EFF_FROM", efffrom = rs.getTimestamp("eff_from"));
					PList.put("VALID_UPTO", validup = rs.getTimestamp("valid_upto"));
					PList.put("LIST_TYPE", listType = rs.getString("list_type"));
					PList.put("ORDER_TYPE", orderType = rs.getString("order_type"));
					PList.put("MANAGE_TYPE", manageType = rs.getString("manage_type"));
					PList.put("MIN_QTY", minqty = rs.getDouble("min_qty"));
					PList.put("MAX_QTY", maxqty = rs.getDouble("max_qty"));
					PList.put("UNIT", unit = rs.getString("unit"));
					PList.put("LINE_NO", lineno = rs.getInt("line_no"));
					PList.put("REF_NO", refNO = rs.getString("ref_no"));
					PList.put("REF_NO_OLD", refNoold = rs.getString("ref_no_old"));
					PList.put("CALC_BASIS", calc = rs.getString("calc_basis"));
					PList.put("TRAN_ID",tranId);// added by nandkumar gadkari on 13/07/19
					HashMap<String, String> calcRate = null;
					//System.out.println("before calcRate::plisttar["+plisttar+"]calcmeth["+calcmeth+"]PList[ "+PList);
					BaseLogger.log("3", null, null, "before calcRate::plisttar["+plisttar+"]calcmeth["+calcmeth+"]PList[ "+PList);
					calcRate = distCommon.calcRate(plisttar, PList, calcmeth, retString, conn);
					//System.out.println("calcRate::["+calcRate+"]");
					BaseLogger.log("3", null, null, "calcRate::["+calcRate+"]");
					if (calcRate != null) 
					{
						if (calcRate.containsKey("error")) {
							//retString = itmDBAccessLocal.getErrorString("", calcRate.get("error"), "", "", conn);
							retString = getErrorXML(calcRate.get("error"), "calcmeth", "VTRATE2", calcRate.get("error"));
							return retString;
						} else {
							finalRate = Double.parseDouble(calcRate.get("rate"));
							//System.out.println("[finalRate]---[" + finalRate + "]");
							BaseLogger.log("3", null, null, "[finalRate]---[" + finalRate + "]");
						}
					}
					PList.put("RATE", finalRate);

					PricelistVar.add(PList);
					cnt++;
					// Pavan R 6may19 end
					/*
					 * retString= insertPricelist(tranId, PList, PricelistGen, conn, xtraParams);
					 * System.out.println("retString--------------------["+retString+"]"); if (
					 * retString != null && retString.trim().length() > 0) { return retString; }
					 */

				}
				rsInner.close();
				rsInner = null;
				pstmtInner.close();
				pstmtInner = null;
				System.out.println("cnt:"+cnt);
			}
			rs.close();
			rs = null;
			pstmtSql.close();
			pstmtSql = null;
			System.out.println("calling insertPricelist.PricelistVar.size()::"+PricelistVar.size());
			retString = insertPricelist(tranId, PList, PricelistVar, conn, xtraParams);
			System.out.println("retString--------------------[" + retString + "]");
			if (retString != null && retString.trim().length() > 0) 
			{
				return retString;
			}
		} catch (Exception e) 
		{
			//System.out.println(e.getMessage()); //Pavan Rane 20may19 [Exception handling to be proper and added finally]
			//System.out.println("Exception : " + e);
			System.out.println("Exception :PriceListconf : insertPricelist()::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally {

			try {
				if(rs != null) {
					rs.close();
					rs = null;
				}
				if(rsInner != null) {
					rsInner.close();
					rsInner = null;
				}
				if(pstmtSql != null) {
					pstmtSql.close(); 
					pstmtSql = null;
				}
				if(pstmtInner != null) {
					pstmtInner.close();
					pstmtInner = null;
				}
			}catch(Exception ex)
			{				
			}
		}

		return retString;

	}

	private static boolean isNumber(Character character) 
	{
		boolean flag = false;
		try {

			double num = Double.parseDouble("" + character);
			flag = true;
			return flag;
		} catch (Exception e) {
			return flag;
		}

	}

	public boolean isnumberStr(String str) 
	{
		int val;
		try {
			val = Integer.parseInt(str);
		} catch (NumberFormatException nex) 
		{
			return false;
		} catch (Exception ex) 
		{
			return false;
		}
		return true;
	}
	public String getErrorXML(String messageValue, String message, String errorId, String traceInfo)
			throws RemoteException, ITMException {
		System.out.println("getErrorXML..........");
		String errString = "";
		try {
			errString = "";
			StringBuffer valueXmlErrorString = new StringBuffer(
					"<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<Errors>\r\n");

			valueXmlErrorString.append("<error id=\"").append(errorId).append("\" type=\"E\"")
					.append(" column_name=\"description\"").append(">");

			valueXmlErrorString.append("<message><![CDATA[").append(message).append("]]></message>\r\n");
			valueXmlErrorString.append("<description><![CDATA[").append(messageValue).append("]]></description>\r\n");
			valueXmlErrorString.append("<type>E</type>\r\n");
			valueXmlErrorString.append("<option></option>\r\n");
			valueXmlErrorString.append("<time></time>\r\n");
			valueXmlErrorString.append("<alarm></alarm>\r\n");
			valueXmlErrorString.append("<source></source>\r\n");
			valueXmlErrorString.append("<trace><![CDATA[Error : " + traceInfo + " ]]></trace>\r\n");
			valueXmlErrorString.append("<redirect>1</redirect>\r\n");
			valueXmlErrorString.append("</error>\r\n");
			valueXmlErrorString.append("</Errors>\r\n");
			valueXmlErrorString.append("</Header>\r\n");
			valueXmlErrorString.append("</Root>\r\n");
			System.out.println("\n****valueXmlErrorString :" + valueXmlErrorString.toString() + ":********");
			errString = valueXmlErrorString.toString();

			System.out.println("Modified error string" + errString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return errString;
	}


}