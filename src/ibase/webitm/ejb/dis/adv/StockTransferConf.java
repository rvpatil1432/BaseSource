/**
 * PURPOSE : Confirmation of Stock transfer transaction
 * AUTHOR : Gulzar on 13/09/11
 */ 

package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.InvAllocTraceBean;
import ibase.webitm.ejb.dis.StockUpdate;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.InvAcct;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import ibase.webitm.ejb.sys.UtilMethods;
import javax.ejb.Stateless;

@Stateless 
public class StockTransferConf extends ActionHandlerEJB implements StockTransferConfRemote, StockTransferConfLocal
{
	/**
	 * The public function is used to confirm a transaction corresponding to the tran id it takes as an argument
	 * @param : tranId
	 * @param : xtraParams
	 */
	//changes by chitranjan for overriding of confirm method 29/11/11
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String retString = "";
		Connection conn = null;
		boolean isConn= false;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn, isConn);
			System.out.println("retString:::::"+retString);
			//if ( retString != null && retString.length() > 0 )
			//{
			//	throw new Exception("Exception while calling confirm for tran  Id:["+tranID+"]");
			//}
		}
		catch(Exception e)
		{
			System.out.println("Exception in [StockTransferConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}
	//changes by chitranjan in confirm method parameter add connection ,connStatus  
	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus ) throws RemoteException,ITMException
	{		
		String replOrder = "";
		String confirmed = "";
		String retString = "";
		String itemCode = "";
		String siteCode = "";
		String locCodeFr = "";
		String locCodeTo = "";
		String lotNo = "";
		String lotSl = "";
		String lotSlTo = "";
		String acctCodeCr = "";		
		String cctrCodeCr = "";
		//Chnaged by Rohan on 18-07-13 for define variables
		String lockCode = "";
		String lotNoTo = "";

		String sql = "";
		String updateSql = "";

		double quantity = 0.0;
		double noArt = 0.0;

		java.sql.Timestamp expDate = null;
		java.sql.Timestamp mfgDate = null;
		java.sql.Timestamp restestDate = null;
		String packCode = "";
		String siteCodeMfg = "";
		String packInstr = "";
		String suppCodeMfg = "";
		String unitAlt = "";
		String batchNo = "";
		String unit = "";
		String grade = "";
		String remarks = "";
		String dimension = "";
		String acctCodeDr = "";
		String ediOption = "";
		String dataStr = "";
		String userId = "";
		String chgTerm = "",packRef="";

		double grossRate = 0d;
		double convQtyStduom = 0d;
		double batchSize = 0d;
		double stkGrossRate = 0d;
		double stkRate = 0d;
		//Commented and Added by Rohini T on 28/12/2020[Start]
		double stkRateNew = 0d;
		double stkGrossRateNew = 0d;
		//Commented and Added by Rohini T on 28/12/2020[End]
		//Changed by Rohan on 19-08-13 for getting atual rate
		double actualRate = 0d;
		// 16/10/11 manoharan
		double stkQuantity = 0,holdQuantity = 0;
		//Changed by Rohan on 06-08-13 for define variable
		double holdQty = 0;
		PreparedStatement pstmtUpd = null, pstmtHold = null, pstmtRel = null,pstmtQC = null,pstmtStTr=null,pstmt1=null,pstmt3=null;
		ResultSet rsHold = null, rsRel = null, rsQC = null,rsStTr=null,rs1=null,rs3=null;
		String tranIdHold = "", sqlRel  = "", sqlHold = "",nearExpLoc = "", sqlQC = "",sql1="",sql2="";
		String partialUsed = "" ,locCodeInvHold = "" ,lotSlInvHold = "" ,reasonCodeInvHold = "";
		int lineNoHold = 0, qcCount = 0;
		String lineNoSpace="";//added by monika salla
		// end 16/10/11 manoharan
		String partialGrlLoc = ""; //added by Ashish Sonawane on 05/SEP/12 for partial GRL location Inventory Status. 
		String invStatLocCodeTo =""; //added by Ashish Sonawane on 05/SEP/12 for partial GRL location Inventory Status.
		PreparedStatement pstmtInvStat =null;
		ResultSet rsInvStat =null;
		String ledgPostConf= ""; // Added By PRiyankaC 

		int lineNo = 0, updCnt = 0;

		boolean isError = false;

		ArrayList errList = null;
		HashMap updateRowMap = null;
		HashMap strAllocate = null;
		HashMap stockMap = null;//added by kunal on 25/OCT/13

		StockUpdate stkUpdate = null;
		InvAcct invAct=null;
		//Connection conn = null;		
		PreparedStatement pstmt = null;
		PreparedStatement pstmtStock = null;
		ResultSet rs = null;
		ResultSet rsStock = null;

		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();

		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		InvAllocTraceBean invAllocTrace = new InvAllocTraceBean();
		DistCommon dComm = new DistCommon();
		double balQty = 0;
		boolean isConn=false;//added by somanath on 03.08.13

		String sqlStTr="";
		String refSerFor="",refIdFor="",reasCode="",invStat="",lotSlOrg="";  //,partialUsed="";
		double potencyPerc=0,grossWeight=0,tareWeight=0,netWeight=0; //,actualRate=0;
		FinCommon finCommon = new FinCommon();
		DistCommon discommon = new DistCommon();
		String acctCode="",cctrCode="",invacct="",trfacct="",itemSer="",cctrCodeDr="";  //lotNoTo="",;
		double qtyPerArt=0,stknoArt=0,partnoArt=0;//Added by manoj dtd 24/06/2014
		PreparedStatement pstmt2=null;
		ResultSet rs2=null;
		java.sql.Timestamp stockDate = null;
		int cnt = 0;
		String lineNoStr="";
		UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			if ( conn == null )
			{
				isConn=false;//added by somanath on 03.08.13
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
				conn.setAutoCommit(false);
				connStatus = true;
			}

			String dbDateFormat = genericUtility.getDBDateFormat();
			String applDateFormat = genericUtility.getApplDateFormat();

			java.sql.Timestamp currDate = new java.sql.Timestamp( System.currentTimeMillis() );
			java.util.Date currDate1 = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
			String currDateStr = sdf.format(currDate1);
			String empCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginEmpCode" );
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			//Start Added by chandrashekar for employee code empty
			if (empCode == null || empCode.trim().length() == 0)
			{
				sql = "select emp_code from users where code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, userId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					empCode = rs.getString( "emp_code" );
					System.out.println("useres employee code"+empCode);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			//End Added by chandrashekar for employee code empty
			java.sql.Timestamp tranDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString( currDateStr , applDateFormat, dbDateFormat ) + " 00:00:00.0") ;

			updateRowMap = new HashMap();
			stockMap = new HashMap();
			stkUpdate = new StockUpdate();

			sql = "SELECT CONFIRMED, SITE_CODE ,TRAN_DATE FROM STOCK_TRANSFER WHERE TRAN_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed = rs.getString( "CONFIRMED" );
				siteCode = rs.getString( "SITE_CODE" );
				stockDate = rs.getTimestamp( "TRAN_DATE" );
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			if( confirmed.equalsIgnoreCase("Y") )
			{
				System.out.println("The transaction already confirmed");
				retString = itmDBAccess.getErrorString("","VTCONF8","","",conn);//added by somanath on 25.07.2013
				return retString;
			}
			else
			{
				// 24/06/12 manoharan if transfer to near expiry not to transfer the hold quantity
				//nearExpLoc = dComm.getDisparaSTOCK_TRANSFERms("999999","NEAREXP_LOC",conn);
				//partialGrlLoc = dComm.getDisparams("999999","PGRL_INVSTAT",conn);

				//Added by PriyankaC on 10June2019 [start]
				sql = "select (case when LEDG_POST_CONF is null then 'N' else LEDG_POST_CONF end ) as ledgpostconf from transetup where lower(tran_window) = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "w_stock_transfer");
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					ledgPostConf = rs.getString("ledgpostconf");
				}
				System.out.println("Inside ledgPostConf staus....."+ledgPostConf);
				//Added by Rohini T on 24/12/2020[TO Close statment and resultset after execution of query][Start]
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//Added by Rohini T on 24/12/2020[TO Close statment and resultset after execution of query][End]
				if (ledgPostConf == null && ledgPostConf.trim().length() < 0)
				{
					ledgPostConf = "N";
				}

				if (ledgPostConf != null && "Y".equalsIgnoreCase(ledgPostConf.trim()))
				{
					//Commented and Added by Rohini T on 24/12/2020[TO correct update statement][Start]
					System.out.println("Inside ledgPostConf staus :::::@@@@@");
					/*sql =   "UPDATE STOCK_TRANSFER CONF_DATE = ? " +
							"WHERE TRAN_ID = ? ";*/
					sql =   "UPDATE STOCK_TRANSFER SET CONF_DATE = ? " +
							"WHERE TRAN_ID = ? ";
					//Commented and Added by Rohini T on 24/12/2020[TO correct update statement][End]
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, tranDate);
					pstmt.setString(2, tranId);	
					cnt = pstmt.executeUpdate();
					System.out.println("STOCK_TRANSFER UPDATE COUNT..."+cnt);
					if (cnt < 0)
					{
						retString = itmDBAccess.getErrorString("","DS000","","",conn);
						return retString;
					}
					pstmt.close();
					pstmt = null;
				}
				else
				{
					System.out.println("Inside ledgPostConf  : " +stockDate +" todays date" +tranDate);
					tranDate = stockDate;
				}
				/*//Commented by Rohini T on 24/12/2020[TO Close statment and resultset after execution of query][Start]
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				 //Commented by Rohini T on 24/12/2020[TO Close statment and resultset after execution of query][End]*/
				//Added by PriyankaC on 10Jul2019 [END]

				sql = "SELECT LINE_NO, ITEM_CODE, QUANTITY, LOC_CODE__FR, LOC_CODE__TO, LOT_NO__FR, LOT_SL__FR, LOT_SL__TO, ACCT_CODE__CR, CCTR_CODE__CR, NO_ART ,LOT_NO__TO " +
						",acct_code__dr,cctr_code__dr  " +             // added by cpatil on 20/11/13 compare with pb code and adding missing field
						"FROM STOCK_TRANSFER_DET WHERE TRAN_ID = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();

				sqlStTr = " SELECT ref_ser__for, ref_id__for, reas_code  "     // added by cpatil on 20/11/13 compare with pb code and adding missing field 
						+" FROM STOCK_TRANSFER WHERE TRAN_ID = ? ";
				pstmtStTr = conn.prepareStatement(sqlStTr);
				pstmtStTr.setString(1, tranId);
				rsStTr = pstmtStTr.executeQuery();
				if(rsStTr.next())
				{
					refSerFor = rsStTr.getString("ref_ser__for");   
					refIdFor = rsStTr.getString("ref_id__for");
					reasCode = rsStTr.getString("reas_code");
				}
				rsStTr.close();rsStTr = null;
				pstmtStTr.close();pstmtStTr = null;



				// 16/10/11 manoharan quantity, hold_qty added
				sql = "select acct_code__inv, cctr_code__inv, "
						+ "exp_date, pack_code, mfg_date, site_code__mfg, "
						+ "pack_instr, supp_code__mfg, retest_date, "
						+ "gross_rate, rate, conv__qty_stduom, unit__alt, batch_no, batch_size, "
						//Changed by Rohan on 19-08-13 for getting atual rate
						//+ "unit, grade, remarks, dimension, quantity ,hold_qty "
						+ "unit, grade, remarks, dimension, quantity ,hold_qty,actual_rate ,partial_used  "
						+ ",potency_perc, inv_stat, gross_weight, tare_weight, net_weight, lot_sl__org,qty_per_art,no_art,pack_ref 	  "   // added by cpatil on 20/11/13 compare with pb code and adding missing field
						+ "from stock "
						+ "where item_code = ? "
						+ "and site_code = ? "
						+ "and loc_code = ? "
						+ "and lot_no = ? "
						+ "and lot_sl = ? ";
				// end 16/10/11 manoharan hold_qty added
				pstmtStock = conn.prepareStatement(sql);

				while(rs.next())
				{
					lineNo = rs.getInt( "LINE_NO" );//stocktranferdet
					itemCode = rs.getString( "ITEM_CODE" );
					quantity = rs.getDouble( "QUANTITY" );
					locCodeFr = rs.getString( "LOC_CODE__FR" );
					locCodeTo = rs.getString( "LOC_CODE__TO" );
					lotNo = rs.getString( "LOT_NO__FR" );
					lotSl = rs.getString( "LOT_SL__FR" );
					lotSlTo = rs.getString( "LOT_SL__TO" );
					lotNoTo = rs.getString( "LOT_NO__TO" );//added by kunal on 14/oct/13
					noArt = rs.getDouble( "NO_ART" );
					acctCodeCr = rs.getString( "ACCT_CODE__CR" );
					cctrCodeCr = rs.getString( "CCTR_CODE__CR" );

					acctCodeDr = rs.getString( "ACCT_CODE__DR" );         //  cpatil
					cctrCodeDr = rs.getString( "CCTR_CODE__DR" );         //  cpatil

					// 16/10/11 manoharan initialise quantity, hold_qty
					stkQuantity = 0;
					holdQuantity = 0;
					// end 16/10/11 manoharan hold_qty added
					/*Added by Ashish Sonawane on 05-Sep-12 [START]*/
					///Added by jasmina DI89SUN004-08/07/08- Shifted from gbf_update_stock
					//for updating loc_code & lot_sl as null if qc order is found during stock transfer
					// 24/10/13 manoharan merged from PB, eas missing in java code
					if (quantity > 0)
					{
						qcCount = 0;

						sqlQC = "select count(1) from qc_order " 
								+ " where  site_code = ? "
								+ " and loc_code  = ? "
								+ " and item_code = ? "
								+ " and lot_no  = ? "
								+ " and lot_sl = ? "
								+ " and status <> 'C' ";

						pstmtQC = conn.prepareStatement( sqlQC );
						pstmtQC.setString( 1, siteCode );
						pstmtQC.setString( 2, locCodeFr );
						pstmtQC.setString( 3, itemCode );
						pstmtQC.setString( 4, lotNo );
						pstmtQC.setString( 5, lotSl );
						rsQC = pstmtQC.executeQuery();
						if( rsQC.next() )
						{
							qcCount = rsQC.getInt(1);
						}
						rsQC.close();
						rsQC = null;
						pstmtQC.close();
						pstmtQC = null;

						if (qcCount > 0 )
						{
							//below sql tran_id__xfrx added by jasmina 29/07/08-MF89SUN046
							sqlQC = "update qc_order set loc_code = NULL, lot_sl = NULL, tran_id__xfrx = ? " // :as_tran_id
									+ " where  site_code = ? "
									+ " and loc_code  = ? "
									+ " and item_code = ? "
									+ " and lot_no  = ? "
									+ " and lot_sl = ? "
									+ " and status <> 'C' ";

							pstmtQC = conn.prepareStatement( sqlQC );
							pstmtQC.setString( 1, tranId );
							pstmtQC.setString( 2, siteCode );
							pstmtQC.setString( 3, locCodeFr );
							pstmtQC.setString( 4, itemCode );
							pstmtQC.setString( 5, lotNo );
							pstmtQC.setString( 6, lotSl );
							pstmtQC.executeUpdate();
						}
						else
						{
							//If the above count has returned 0 then check for the lot where lot_sl is null
							sqlQC = "select count(1) from qc_order " 
									+ " where  site_code = ? "
									+ " and loc_code  = ? "
									+ " and item_code = ? "
									+ " and lot_no  = ? "
									+ " and lot_sl is null "
									+ " and status <> 'C' ";

							pstmtQC = conn.prepareStatement( sqlQC );
							pstmtQC.setString( 1, siteCode );
							pstmtQC.setString( 2, locCodeFr );
							pstmtQC.setString( 3, itemCode );
							pstmtQC.setString( 4, lotNo );
							rsQC = pstmtQC.executeQuery();
							if( rsQC.next() )
							{
								qcCount = rsQC.getInt(1);
							}
							rsQC.close();
							rsQC = null;
							pstmtQC.close();
							pstmtQC = null;
							if (qcCount > 0 )
							{
								//below sql tran_id__xfrx added by jasmina 29/07/08-MF89SUN046
								sqlQC = "update qc_order set loc_code = NULL, lot_sl = NULL, tran_id__xfrx = ? " // :as_tran_id
										+ " where  site_code = ? "
										+ " and loc_code  = ? "
										+ " and item_code = ? "
										+ " and lot_no  = ? "
										+ " and lot_sl is null "
										+ " and status <> 'C' ";

								pstmtQC = conn.prepareStatement( sqlQC );
								pstmtQC.setString( 1, tranId );
								pstmtQC.setString( 2, siteCode );
								pstmtQC.setString( 3, locCodeFr );
								pstmtQC.setString( 4, itemCode );
								pstmtQC.setString( 5, lotNo );
								pstmtQC.executeUpdate();
							}
						}
					}
					///Added end by jasmina DI89SUN004-08/07/08- Shifted from gbf_update_stock


					String sqlInvStat = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ?";
					pstmtInvStat = conn.prepareStatement( sqlInvStat );
					pstmtInvStat.setString( 1, locCodeTo );
					rsInvStat = pstmtInvStat.executeQuery();
					if( rsInvStat.next() )
					{
						invStatLocCodeTo = checkNull(rsInvStat.getString("INV_STAT"));
					}
					rsInvStat.close();rsInvStat = null;
					pstmtInvStat.close();pstmtInvStat = null;

					/*Added by Ashish Sonawane on 05-Sep-12 [END]*/


					pstmtStock.setString(1,itemCode);
					pstmtStock.setString(2,siteCode);
					pstmtStock.setString(3,locCodeFr);
					pstmtStock.setString(4,lotNo);
					pstmtStock.setString(5,lotSl);
					rsStock = pstmtStock.executeQuery();
					if ( rsStock.next() )
					{
						//acctCodeDr = rsStock.getString("acct_code__inv");
						//cctrCodeDr = rsStock.getString("cctr_code__inv");

						expDate = rsStock.getTimestamp("exp_date");
						packCode = rsStock.getString("pack_code");
						mfgDate = rsStock.getTimestamp("mfg_date");
						siteCodeMfg = rsStock.getString("site_code__mfg");
						packInstr = rsStock.getString("pack_instr");
						suppCodeMfg = rsStock.getString("supp_code__mfg");
						restestDate = rsStock.getTimestamp("retest_date");
						stkGrossRate = rsStock.getDouble("gross_rate");
						stkRate = rsStock.getDouble("rate");
						System.out.println("stkGrossRate"+stkGrossRate+"stkRate"+stkRate);
						convQtyStduom = rsStock.getDouble("conv__qty_stduom");
						unitAlt = rsStock.getString("unit__alt");
						batchNo = rsStock.getString("batch_no");
						batchSize = rsStock.getDouble("batch_size");
						unit = rsStock.getString("unit");
						grade = rsStock.getString("grade");
						remarks = rsStock.getString("remarks");
						dimension = rsStock.getString("dimension");
						// 16/10/11 manoharan hold_qty added
						stkQuantity = rsStock.getDouble("quantity");
						holdQuantity = rsStock.getDouble("hold_qty");
						// end 16/10/11 manoharan hold_qty added
						//Changed by Rohan on 19-08-13 for getting atual rate
						actualRate = rsStock.getDouble("actual_rate");
						partialUsed =  checkNull(rsStock.getString("partial_used"));

						// added by cpatil on 20/11/13 start

						potencyPerc = rsStock.getDouble("potency_perc");    
						grossWeight = rsStock.getDouble("gross_weight");    
						tareWeight = rsStock.getDouble("tare_weight");      
						netWeight = rsStock.getDouble("net_weight");        
						lotSlOrg = rsStock.getString("lot_sl__org");  
						qtyPerArt= rsStock.getDouble("qty_per_art");
						stknoArt=  rsStock.getDouble("no_art");
						packRef = rsStock.getString( "pack_ref" );//added by chandrashekar on 20-12-2014
						// added by cpatil on 20/11/13 end




						/* temporary comment code by kunal on 16/NOV/13

						//added by kunal on 25/oct/13

						stockMap.put("item_code", itemCode);
						stockMap.put("site_code", siteCode);
						stockMap.put("loc_code", locCodeFr);
						stockMap.put("lot_no", lotNo);
						stockMap.put("lot_sl", lotSl);

						stockMap.put("quantity", ""+quantity);

						if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()) && invStatLocCodeTo.trim().equalsIgnoreCase(partialGrlLoc.trim()))
						{
							stockMap.put("no_art", quantity);
						}
						else
						{
							stockMap.put("no_art", noArt);
						}

						stockMap.put("tran_ser", "XFRX");
						stockMap.put("acct_code__cr",acctCodeCr);
						stockMap.put("cctr_code__cr",cctrCodeCr);
						stockMap.put("acct_code_inv",acctCodeCr);
						stockMap.put("cctr_code_inv",cctrCodeCr);
						stockMap.put("rate",Double.toString(stkRate));
						stockMap.put("gross_rate",Double.toString(stkGrossRate));
						System.out.println("stkGrossRate:::"+Double.toString(stkRate)+"stkRate::::"+Double.toString(stkGrossRate));
						stockMap.put("tran_id", tranId );
						stockMap.put("tran_date", tranDate );
						//updateRowMap.put("tran_type","ID"); 

						stockMap.put("qty_stduom", ""+quantity);
						stockMap.put("unit",unit);
						stockMap.put("grade",grade);
						stockMap.put("remarks",remarks);
						stockMap.put("dimension",dimension);
						stockMap.put("tran_type", "I");
						//updateRowMap.put("tran_type", "I");

						stockMap.put("exp_date", expDate);
						stockMap.put("pack_code", packCode);
						stockMap.put("mfg_date", mfgDate);
						stockMap.put("site_code__mfg", siteCodeMfg);
						stockMap.put("pack_instr", packInstr);
						stockMap.put("supp_code__mfg", suppCodeMfg);
						stockMap.put("retest_date", restestDate);
						stockMap.put("conv__qty_stduom", ""+convQtyStduom);
						stockMap.put("unit__alt", unitAlt);
						stockMap.put("batch_no", batchNo);
						stockMap.put("batch_size", ""+batchSize);
						stockMap.put("actual_rate", actualRate);

						errList = allocTransfer(siteCode,itemCode,locCodeFr ,lotNo,lotSl,locCodeTo ,lotNoTo,lotSlTo, quantity,stockMap, xtraParams , conn );
						System.out.println("from alloc transfer=="+errList.toString());
						retString = errList.get(0).toString() ;
						System.out.println("FROM allocTransfer 0 : "+retString);
						if ( retString != null && retString.trim().length() > 0 )
						{
							isError = true;
							break;
						}
						stockMap.clear();
						quantity = Double.parseDouble( errList.get(1).toString() );
						System.out.println("pending quantity FROM allocTransfer "+quantity);
						if(quantity <= 0)
						{
							continue;
						}
						//added by kunal on 25/oct/13 end
						 *temporary comment code by kunal on 16/NOV/13 end
						 */

					}
					rsStock.close(); rsStock = null;
					pstmtStock.clearParameters();
					//Commented and Added by Rohini T on 28/12/2020[Start]
					stkRateNew = Double.parseDouble(UtilMethods.getInstance().getReqDecString(stkRate, 4));
					stkGrossRateNew = Double.parseDouble(UtilMethods.getInstance().getReqDecString(stkGrossRate, 4));
					System.out.println("stkGrossRateNew:::$$"+Double.toString(stkGrossRateNew)+"stkRateNew::::$$"+Double.toString(stkRateNew));
					//Commented and Added by Rohini T on 28/12/2020[End]
					updateRowMap.put("item_code", itemCode);
					updateRowMap.put("site_code", siteCode);
					updateRowMap.put("loc_code", locCodeFr);
					updateRowMap.put("lot_no", lotNo);
					updateRowMap.put("lot_sl", lotSl);
					updateRowMap.put("quantity", ""+quantity);
					//ADDED BY MONIKA SALLA ON 20 APRIL 2021---Stock transfer confirmation there line_no is not set in ref_line in invalloc_trace

					System.out.println("line before["+ lineNo+"]");

					lineNoStr=Integer.toString(lineNo);
					if(lineNoStr != null && lineNoStr.trim().length() > 0)
					{
						lineNoSpace = getLineNewNo(lineNoStr);
					}	
					System.out.println("1820 line after["+ lineNoSpace+"]");

					updateRowMap.put("line_no",lineNoSpace);

					//END

					/* Added by Ashish Sonawane on 07-sep-12 as per Manoharan Sir in case of partial GRL no_art to be set as 0 for issue [START] */
					//Chnaged by Rohan on 29-07-13 for setting no art is 0 in case of location code to is PGRL.
					//if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()))
					/*Commented by Manoj dtd 24/06/2014
					 if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()) && invStatLocCodeTo.trim().equalsIgnoreCase(partialGrlLoc.trim()))
					{
						noArt = 0;
						updateRowMap.put("no_art", noArt);
					}
					else
					{
						updateRowMap.put("no_art", noArt);
					}*/
					if((quantity%qtyPerArt)>0)
					{
						partnoArt=1;	
					}
					else
					{
						partnoArt=0;
					}
					// Modified by Sana S on 15/06/20 [start][not showing divide by zero exception]
					int calcnoArt = 0,partialArt = 0;

					if(qtyPerArt < 0.00 || qtyPerArt > 0.00)
					{
						calcnoArt=(int) (quantity/qtyPerArt);
						// ADDED BY RITESH ON 08/07/14 START
						partialArt=(int) (quantity/qtyPerArt);
						if(partialArt  == 0)
						{
							partialArt = 1;
						}else
						{
							partialArt = 0;
						}
					}
					else
					{
						calcnoArt = (int) noArt;
						partialArt=0;
					}
					//Modified by Sana S on 15/06/20 [start]
					System.out.println("quantity["+quantity+"]--qtyPerArt["+qtyPerArt+"]--calcnoArt["+calcnoArt+"]--partnoArt["+partnoArt+"]");
					System.out.println("partialArt -- "+partialArt);
					updateRowMap.put("no_art", calcnoArt + partialArt);
					// ADDED BY RITESH ON 08/07/14 END

					/* Added by Ashish Sonawane on 07-sep-12 as per Manoharan Sir in case of partial GRL no_art to be set as 0 for issue [END] */

					updateRowMap.put("tran_ser", "XFRX");
					updateRowMap.put("acct_code__cr",acctCodeCr);
					updateRowMap.put("cctr_code__cr",cctrCodeCr);
					updateRowMap.put("acct_code_inv",acctCodeCr);
					updateRowMap.put("cctr_code_inv",cctrCodeCr);
					//Commented and Added by Rohini T on 28/12/2020[Start]
					//updateRowMap.put("rate",Double.toString(stkRate));
					updateRowMap.put("rate",Double.toString(stkRateNew));
					//updateRowMap.put("gross_rate",Double.toString(stkGrossRate));
					updateRowMap.put("gross_rate",Double.toString(stkGrossRateNew));
					System.out.println("stkGrossRateNew:::"+Double.toString(stkGrossRateNew)+"stkRateNew::::"+Double.toString(stkRateNew));
					//Commented and Added by Rohini T on 28/12/2020[End]
					updateRowMap.put("tran_id", tranId );
					updateRowMap.put("tran_date", tranDate );
					updateRowMap.put("tran_type","ID"); 

					updateRowMap.put("qty_stduom", ""+quantity);
					updateRowMap.put("unit",unit);
					updateRowMap.put("grade",grade);
					updateRowMap.put("remarks",remarks);
					updateRowMap.put("dimension",dimension);
					//updateRowMap.put("tran_type", "I");

					//changed by gulzar on 12/24/2011
					updateRowMap.put("exp_date", expDate);
					updateRowMap.put("pack_code", packCode);
					updateRowMap.put("mfg_date", mfgDate);
					updateRowMap.put("site_code__mfg", siteCodeMfg);
					updateRowMap.put("pack_instr", packInstr);
					updateRowMap.put("supp_code__mfg", suppCodeMfg);
					updateRowMap.put("retest_date", restestDate);
					updateRowMap.put("conv__qty_stduom", ""+convQtyStduom);
					updateRowMap.put("unit__alt", unitAlt);
					updateRowMap.put("batch_no", batchNo);
					updateRowMap.put("batch_size", ""+batchSize);
					//End changes by gulzar on 12/24/2011
					//Added by Manoj dtd 29/10/2014 to set hold_lock field
					int countHold=0;
					sql="select count(1) From inv_hold a, inv_hold_det b Where a.tran_id = b.tran_id And b.item_code  = ?	And (b.site_code = ? or b.site_code is null )" +
							"	And (b.loc_code  = ?  or b.loc_code is null )	" +
							"	And (b.lot_no    =? or b.lot_no is null )	" +
							"	And (b.lot_sl    = ? or b.lot_sl is null )		" +
							"And (b.line_no_sl = 0 or b.line_no_sl is null)	" +
							"	And a.confirmed='Y' And b.hold_status ='H'";
					pstmtHold = conn.prepareStatement(sql);
					pstmtHold.setString(1,itemCode);
					pstmtHold.setString(2,siteCode);
					pstmtHold.setString(3,locCodeTo);
					pstmtHold.setString(4,lotNoTo);
					pstmtHold.setString(5,lotSlTo);

					rsHold = pstmtHold.executeQuery();
					if(rsHold.next())
					{
						countHold = rs.getInt(1);
					}
					rsHold.close();
					rsHold = null;
					pstmtHold.close();
					pstmtHold = null;
					if(countHold>0)
					{
						updateRowMap.put("hold_lock", "Y");
					}
					// added by cpatil on 20/11/13  start 
					itemSer = discommon.getItemSer(itemCode, siteCode, tranDate, "","", conn);
					cctrCode = finCommon.getAcctDetrTtype(itemCode, itemSer, "XFRX", " ", conn);
					acctCode = cctrCode.substring(0,cctrCode.indexOf(","));
					cctrCode = cctrCode.substring(cctrCode.indexOf(",")+1);

					if ( cctrCodeDr != null && cctrCodeDr.trim().length() > 0)
					{
						if ( acctCode == null ||acctCode.trim().length() == 0 )
						{	
							invacct = finCommon.getFinparams("999999", "INVENTORY_ACCT", conn);
							trfacct = finCommon.getFinparams("999999", "INV_ACCT_TRF", conn);
							if( "Y".equalsIgnoreCase(invacct) || "Y".equalsIgnoreCase(trfacct ))
							{	
								isError = true;
								//Changes by Dadaso pawar on 12/03/15 [Start]
								//retString = "VTTRFACT";								
								retString = itmDBAccess.getErrorString("","VTTRFACT","","",conn);
								//Changes by Dadaso pawar on 12/03/15 [End]
								break;
								//	errCode = "VTTRFACT";   // ~tSTOCK TRANSFER ACCOUNT NOT FOUND"
								//	return errCode;	
							}
						}
					}
					updateRowMap.put("acct_code__dr",acctCode);   
					if( cctrCode == null || cctrCode.trim().length() == 0 )
					{
						updateRowMap.put("cctr_code__dr",cctrCodeDr); 
					}		
					else
					{
						updateRowMap.put("cctr_code__dr",cctrCode);   
					}

					updateRowMap.put("ref_ser__for",refSerFor);  
					updateRowMap.put("ref_id__for",refIdFor);    
					updateRowMap.put("reas_code",reasCode);    
					updateRowMap.put("potency_perc",potencyPerc);  
					updateRowMap.put("inv_stat",invStat);    
					if( stkQuantity != 0)
					{
						updateRowMap.put("gross_weight", ( grossWeight / stkQuantity * quantity )); 
						updateRowMap.put("tare_weight", ( tareWeight / stkQuantity * quantity ));   
						updateRowMap.put("net_weight", ( netWeight / stkQuantity * quantity ));    
					}
					updateRowMap.put("lot_sl__org",lotSl);    

					// added by cpatil on 20/11/13  end

					stkUpdate = new StockUpdate();
					retString = stkUpdate.updateStock( updateRowMap, xtraParams, conn );
					stkUpdate = null;
					System.out.println("Update stock complter1111");
					if ( retString != null && retString.trim().length() > 0 )
					{
						isError = true;
						break;
					}

					updateRowMap = updateQcOrderLotInfo( updateRowMap , conn);//added by chandrashekar 05-01-2015
					// 	added by cpatil on 20/11/13  start

					sql1 = " select inv_stat from location where loc_code = ?  ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, tranId);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						invStat = rs1.getString("inv_stat");
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;

					// added by cpatil on 20/11/13  end



					updateRowMap.put("loc_code", locCodeTo);
					updateRowMap.put("lot_sl", lotSlTo);
					updateRowMap.put("tran_type", "R");
					/* Added by Ashish Sonawane on 07-sep-12 as per Manoharan Sir in case of partial GRL no_art to be set as quantity at the time of receipt [START] */
					//Chnaged by Rohan on 29-07-13 for setting no art as quantity  in case of location code to is PGRL.start
					/*
					if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()))
					{
						updateRowMap.put("no_art", quantity);
					}
					 */
					/*if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()) && invStatLocCodeTo.trim().equalsIgnoreCase(partialGrlLoc.trim()))
					{
						updateRowMap.put("no_art", quantity);
					}
					else
					{
						updateRowMap.put("no_art", noArt);
					}
					 */
					//Changed By PriyankaC to set noof article from stock transfer detail on 09june2019..[Start]
					//updateRowMap.put("no_art", calcnoArt+partnoArt);
					updateRowMap.put("no_art", noArt);
					//	Changed By PriyankaC to set noof article from stock transfer detail on 09june2019. [END]
					//Chnaged by Rohan on 29-07-13 for setting no art as quantity  in case of location code to is PGRL.end
					/* Added by Ashish Sonawane on 07-sep-12 as per Manoharan Sir in case of partial GRL no_art to be set as quantity at the time of receipt [END] */
					//Chnaged by Rohan on 19-08-13 for updating atual rate in transfered stock.
					updateRowMap.put("actual_rate", actualRate);
					//Added by kunal on 19/NOV/13 ADD partial_used TO MAP as per manoharan sir INSTRUCTION
					System.out.println("partialUsed=="+partialUsed);
					updateRowMap.put("partial_used", partialUsed);


					// 	added by cpatil on 20/11/13  start

					updateRowMap.put("lot_no",lotNoTo);   //  cpatil 
					updateRowMap.put("inv_stat",invStat);
					updateRowMap.put("acct_code__cr",acctCode);  
					if ( cctrCode == null || cctrCode.trim().length() == 0 )
					{
						updateRowMap.put("cctr_code__cr",cctrCodeCr);  		
					}
					else
					{
						updateRowMap.put("cctr_code__cr",cctrCode);   
					}
					updateRowMap.put("acct_code__dr",acctCodeDr);    
					updateRowMap.put("cctr_code__dr",cctrCodeDr);    
					updateRowMap.put("cctr_code_inv",acctCodeDr);    
					updateRowMap.put("cctr_code_inv",cctrCodeDr);    
					updateRowMap.put("ref_ser__for",refSerFor);    
					updateRowMap.put("ref_id__for",refIdFor);    
					updateRowMap.put("pack_ref",packRef); ///added by chandrashekar on20-12-2014
					// added by cpatil on 20/11/13  end
					updateRowMap.put("loc_code_from",locCodeFr); //added by chandrashekar 05-01-2015
					updateRowMap = updateQcOrderLotInfo( updateRowMap , conn);//added by chandrashekar 05-01-2015

					//added by monika salla on 20 april 2021
					/* detail2List = dom2.getElementsByTagName("Detail2");
                    for(int t =0; t < detail2List.getLength(); t++ )
							{
								detailNode = detail2List.item(t);
								childDetilList = detailNode.getChildNodes();
								for(int p =0; p < childDetilList.getLength(); p++ )
								{
									chidDetailNode = childDetilList.item(p);
									//	System.out.println("current child node>>>>>>>>>> " + chidDetailNode.getNodeName() );
									if(chidDetailNode.getNodeName().equalsIgnoreCase("line_no") )
									{
										//System.out.println("line node found >>>>>" + chidDetailNode.getNodeName());
										if(chidDetailNode.getFirstChild() != null )
										{
											lineValue = chidDetailNode.getFirstChild().getNodeValue();
											if(lineValue != null && lineValue.trim().length() > 0)
											{
												lineValueInt = Integer.parseInt(lineValue.trim());
											}
											//System.out.println("current child line value node>>>>>>>>>> "+lineValueInt);
										}
									}
									if(chidDetailNode.getNodeName().equalsIgnoreCase("attribute") )
									{
										//System.out.println("operation node found >>>>>" + chidDetailNode.getNodeName());
										updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
										//	System.out.println("Update flag is......."+updateFlag);
									}

									if(chidDetailNode.getNodeName().equalsIgnoreCase("rcp_amt") )
									{
										//System.out.println("invoiceTrace node found >>>>>" + chidDetailNode.getNodeName());
										if(chidDetailNode.getFirstChild() != null )
										{
											rcpdetamt = chidDetailNode.getFirstChild().getNodeValue();
											if(rcpdetamt != null && rcpdetamt.trim().length() > 0)
											{
												rcpdetAmount = Double.parseDouble(rcpdetamt.trim());
                                            }
                                         System.out.println("1234 tot_amt---["+totAmt +"receipt detail amount---"+rcpdetAmount+"receipt total detail amount"+totAmtdet);

											if(!updateFlag.equalsIgnoreCase("D") && lineNoInt != lineValueInt)
											{
												totAmtdet=totAmtdet+rcpdetAmount;
											}
										}
									}
								}
                            }

                    //end*/

					stkUpdate = new StockUpdate();
					retString = stkUpdate.updateStock( updateRowMap, xtraParams, conn );
					stkUpdate=null;
					if ( retString != null && retString.trim().length() > 0 )
					{
						isError = true;
						break;
					}
					updateRowMap.clear();

					// 16/10/11 manoharan check whether inv hold is there
					// if hold_qty > 0 update the same stock hold_qty for loc_code__to
					// also reduce the hold_qty for old loc_code
					// This utility methods to update old and new location should be in 
					// a separate probably in stockupdate so that we can call the same from various places
					System.out.println(">>>>>>Before StockUpdtHoldItems:");
					StockUpdtHoldItems(reasonCodeInvHold,holdQuantity,quantity,itemCode,siteCode,locCodeFr,lotNo,lotSl,locCodeTo,lotSlTo,conn);
					System.out.println(">>>>>>After StockUpdtHoldItems:");

					// end 16/10/11 manoharan
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				pstmtStock.close();pstmtStock = null;
				double netQty=0.0;
				String msgString="";
				System.out.println(">>>>>>After retString:["+retString);
				if( retString == null || retString.trim().length() == 0 )
				{

					//added by monika salla on 22 april 2021 end
					//  sum(alloc_qty) of invalloc_trace for each line (ref_ser,ref_id and ref_line) the net allocation should be 0 for each line
					System.out.println("inside  retString: null["+retString);

					sql= "select line_no from stock_transfer_det where tran_id=?";
					pstmt= conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs= pstmt.executeQuery();
					while(rs.next())
					{
						lineNo = rs.getInt("line_no");
						lineNoStr=Integer.toString(lineNo);
						System.out.println("9line before["+ lineNo+"]lineNoStr");
						if(lineNoStr != null && lineNoStr.trim().length() > 0)
						{
							lineNoSpace = getLineNewNo(lineNoStr);
						}	
						System.out.println("960 line after["+ lineNoSpace+"]");

						sql2 = "select sum(alloc_qty) from invalloc_trace where ref_ser= ? and ref_id= ? and ref_line= ? ";
						pstmt3 = conn.prepareStatement(sql2);
						pstmt3.setString(1,refSerFor);
						pstmt3.setString(2,tranId);
						pstmt3.setString(3,lineNoSpace);
						rs3 = pstmt3.executeQuery();
						while(rs3.next())
						{
							netQty = rs3.getDouble(1);           
							
						}
						rs3.close();rs3 = null;
						pstmt3.close();pstmt3 = null;
						System.out.println("960 netQty after["+ netQty+"]");
						
						
						if(netQty!=0)
						{
							retString = itmDBAccess.getErrorString("","VTNETAMTZ","","",conn);
							if(retString != null && retString.trim().length() > 0)
							{
								
									System.out.println("lineNoSpace =["+lineNoSpace+"] 45 errString after["+ retString);
									msgString="Line No. is -"+lineNoSpace;
									System.out.println("msgString =["+msgString);

									retString = getModifiedErrorString(retString, msgString);
							}
							System.out.println("961  errString after["+ retString+"]lineNoSpace ["+lineNoSpace);
							return retString;
						}
						
					}
					
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					//added by monika salla on 22 april 2021 end

					updateSql = "UPDATE STOCK_TRANSFER SET CONFIRMED = 'Y', EMP_CODE__APRV = ? " +
							"WHERE TRAN_ID = ? ";
					pstmt = conn.prepareStatement(updateSql);
					pstmt.setString(1, empCode);
					pstmt.setString(2, tranId);				

					updCnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;


					if( updCnt > 0 )
					{					
						System.out.println( updCnt + " rows updated successfully" );
						invAct=new InvAcct();
						System.out.println( updCnt + " rows updated successfully" );
						retString=invAct.retreiveStockTrans(tranId, conn);
						System.out.println("retString@@@@@@@@"+retString);
						System.out.println("retrieve stock transfer called---");
					}if(retString==null || (retString.trim()).length()==0){
						isError = false;
					}else{
						isError=true;
					}
					////////////////////// EDI creation
					/*
					sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_stock_transfer_wiz' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						ediOption = checkNull(rs.getString("EDI_OPTION"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					if ( "1".equals(ediOption.trim()) )
					{
						CreateRCPXML createRCPXML = new CreateRCPXML("w_stockinvStatLocCodeTo_transfer","tran_id");
						dataStr = createRCPXML.getTranXML( tranId, conn );
						System.out.println( "dataStr =[ "+ dataStr + "]" );
						Document ediDataDom = genericUtility.parseString(dataStr);

						E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
						e12CreateBatchLoad.createBatchLoad( ediDataDom, "w_stock_transfer", "0", xtraParams, conn );
						createRCPXML = null;
						e12CreateBatchLoad = null;
					}
					 */
					/////////////////////
				}
				////////////
			}
			if( !isError )
			{
				retString = itmDBAccess.getErrorString("","CONFSUCC","","",conn);
			}
		}
		catch( Exception e )
		{
			try
			{
				conn.rollback();
				isError = true;
				e.printStackTrace();
			}
			catch (Exception e1)
			{
			}
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//Changed by Manish on 1-04-2016 to close prepared statement [START]
				if(pstmtStock != null)
				{
					pstmtStock.close();
					pstmtStock = null;
				}
				if(rsStock != null)
				{
					rsStock.close(); 
					rsStock = null;
				}
				//Changed by Manish on 1-04-2016 to close prepared statement [END]
				//added by chitranjan connStatus if connstatus is true then commit.
				if( !isError && connStatus )
				{
					if (connStatus)
					{
						conn.commit(); 
					}
					//retString = itmDBAccess.getErrorString("","CONFSUCC","","",conn);
				}
				else if( isError && connStatus)
				{
					if (connStatus)
					{
						conn.rollback();
					}
					//retString=itmDBAccess.getErrorString("","VCOINDIFF1","","",conn);
				}
				if ( isConn && isError )
				{
					throw new Exception("Exception while calling confirm for tran  Id:["+tranId+"]");
				}
				if(rs != null )
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmtStock != null )
				{
					pstmtStock.close();
					pstmtStock = null;
				}
				if( conn != null && connStatus )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
	//Changed by Rohan on 17-07-13 for updating INV_HOLD_REL_TRACE
	//private void insertIntoInvHold(String itemCode ,String siteCode, String locCode ,String lotNo, String lotSl, String tranIdHold ,Connection conn) throws ITMException
	//change done by kunal on 31/may/14 reason code add in argumnet

	public void StockUpdtHoldItems(String reasonCodeInvHold,double holdQuantity,double quantity,String itemCode,String siteCode,String locCodeFr,String lotNo,String lotSl,String locCodeTo,String lotSlTo,Connection conn)
	{
		String sqlRel="",tranIdHold="",sqlHold="";
		PreparedStatement pstmtUpd=null,pstmtHold=null;
		int lineNoHold=0;
		ResultSet rsHold=null;
		double balQty=0;
		String lockCode="",locCodeInvHold="",lotSlInvHold="";
		double holdQty=0;
		PreparedStatement pstmt2=null;
		ResultSet rs2=null;
		DistCommon dComm=new DistCommon();
		try
		{
			System.out.println(">>>>In StockUpdtHoldItems");
			String nearExpLoc = dComm.getDisparams("999999","NEAREXP_LOC",conn);
			String partialGrlLoc = dComm.getDisparams("999999","PGRL_INVSTAT",conn);
			if (holdQuantity > 0)
			{
				// update old location with - hold_qty
				sqlRel = "UPDATE STOCK SET HOLD_QTY = HOLD_QTY - ?"
						+ " WHERE ITEM_CODE = ?  "
						+ " AND SITE_CODE = ? "
						+ " AND LOC_CODE = ? "
						+ " AND LOT_NO = ? "
						+ " AND LOT_SL = ? ";
				pstmtUpd = conn.prepareStatement(sqlRel);
				//pstmtUpd.setDouble(1,holdQuantity); //Commented by Ashish Sonawane on 01/SEP/12 as implication on stock due to PGRL location for partial qty putaway
				pstmtUpd.setDouble(1,quantity); //Added by Ashish Sonawane on 01/SEP/12 as implication on stock due to PGRL location for partial qty putaway 
				pstmtUpd.setString(2,itemCode);
				pstmtUpd.setString(3,siteCode);
				pstmtUpd.setString(4,locCodeFr);
				pstmtUpd.setString(5,lotNo);
				pstmtUpd.setString(6,lotSl);
				pstmtUpd.executeUpdate();
				// 24/06/12 manoharan if not neare expiry location hold quantity to be transferred
				if (!nearExpLoc.trim().equalsIgnoreCase(locCodeTo.trim()) )
				{
					// update new location with + hold_qty
					sqlRel = "UPDATE STOCK SET HOLD_QTY = CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END  + ? "
							+ " WHERE ITEM_CODE = ?  "
							+ " AND SITE_CODE = ? "
							+ " AND LOC_CODE = ? "
							+ " AND LOT_NO = ? "
							+ " AND LOT_SL = ? ";
					pstmtUpd = conn.prepareStatement(sqlRel);
					//pstmtUpd.setDouble(1,holdQuantity); //Commented by Ashish Sonawane on 01/SEP/12 as implication on stock due to PGRL location for partial qty putaway
					pstmtUpd.setDouble(1,quantity); //Added by Ashish Sonawane on 01/SEP/12 as implication on stock due to PGRL location for partial qty putaway
					pstmtUpd.setString(2,itemCode);
					pstmtUpd.setString(3,siteCode);
					pstmtUpd.setString(4,locCodeTo);
					pstmtUpd.setString(5,lotNo);
					//pstmtUpd.setString(6,lotSl);//Gulzar on 21/12/11
					pstmtUpd.setString(6,lotSlTo);//Gulzar on 21/12/11
					pstmtUpd.executeUpdate();


					pstmtHold = null;
					rsHold = null;
					tranIdHold = "";
					lineNoHold = 0;


					//Changed by Rohan on 26-07-13 for disinct record
					//sqlHold = "SELECT  D.TRAN_ID AS TRAN_ID, D.LINE_NO AS LINE_NO, "
					/*lHold = "SELECT DISTINCT D.TRAN_ID AS TRAN_ID, D.LINE_NO AS LINE_NO, "
								+ " D.ITEM_CODE AS ITEM_CODE, D.SITE_CODE AS SITE_CODE, "
								+ " D.LOC_CODE AS LOC_CODE, D.LOT_NO AS LOT_NO,  D.LOT_SL AS LOT_SL ,D.REAS_CODE " //ADDED BY KUNAL ON 23/MAY/14 ADD REAS_CODE 
								//Changed By Rohan on 18-07-13 for getting lock_code
								//+ " FROM INV_HOLD_DET D, INV_HOLD H "
								//Changed by Rohan on 06-08-13 to get hold qty
								//+ " ,T.LOCK_CODE FROM INV_HOLD_DET D, INV_HOLD H , INV_HOLD_REL_TRACE T "
								+ " ,T.LOCK_CODE,T.HOLD_QTY FROM INV_HOLD_DET D, INV_HOLD H , INV_HOLD_REL_TRACE T "
								+ " WHERE H.TRAN_ID = D.TRAN_ID "
								//Changed By Rohan on 18-07-13 for getting lock_code
								+ " AND T.REF_NO = D.TRAN_ID "
								//Chnaged by Rohan on 09-08-13 for add join of inv hold det and inv hold rel trace.start
								//change by kunal on 29/may/14 check null value for join
								+" AND (D.ITEM_CODE = T.ITEM_CODE or (d.item_code is null or length(trim(d.item_code)) = 0 ) )"
								+" AND (D.SITE_CODE = T.SITE_CODE or (d.site_code is null or length(trim(d.site_code)) = 0 ) )"  
								+" AND (D.LOC_CODE = T.LOC_CODE  or (d.loc_code is null or length(trim(d.loc_code)) = 0 ) )"
								+" AND (D.LOT_NO = T.LOT_NO  or (d.lot_no is null or length(trim(d.lot_no)) = 0 ) )"
								+" AND (D.LOT_SL = T.LOT_SL or (d.lot_sl is null or length(trim(d.lot_sl)) = 0 ) )" 
								//Chnaged by Rohan on 09-08-13 for add join of inv hold det and inv hold rel trace.end
								//change done by kunal on 30/may/14 add conditiob for INV_HOLD_REL_TRACE table
								+ " AND ((T.ITEM_CODE = ? ) OR (T.ITEM_CODE IS NULL OR LENGTH(TRIM(T.ITEM_CODE)) = 0 ) ) "
								+ " AND ((T.SITE_CODE = ? ) OR (T.SITE_CODE IS NULL OR LENGTH(TRIM(T.SITE_CODE)) = 0 ) ) "
								+ " AND ((T.LOC_CODE = ? ) OR (T.LOC_CODE IS NULL OR LENGTH(TRIM(T.LOC_CODE)) = 0 ) ) "
								+ " AND ((T.LOT_NO = ? ) OR (T.LOT_NO IS NULL OR LENGTH(TRIM(T.LOT_NO)) = 0 ) ) "
								+ " AND ((T.LOT_SL = ? ) OR (T.LOT_SL IS NULL OR LENGTH(TRIM(T.LOT_SL)) = 0 ) ) "
								+ " AND D.HOLD_STATUS = 'H'"
								+ " AND H.CONFIRMED  = 'Y' ";*/
					sqlHold="select sum(case when hold_qty is null then 0 else hold_qty end + case when rel_qty is null then 0 else rel_qty end) as HOLD_QTY,site_code,item_code,lot_no,lock_code,lot_sl,loc_code from inv_hold_rel_trace  where item_code=? and site_code=? and loc_code=? and "
							+ " lot_no=? and lot_sl=? group by site_code,item_code,lot_no,lock_code,lot_sl,loc_code having sum(case when hold_qty is null then 0 else hold_qty end + case when rel_qty is null then 0 else rel_qty end)>0";
					pstmtHold = conn.prepareStatement(sqlHold);
					pstmtHold.setString(1,itemCode);
					pstmtHold.setString(2,siteCode);
					pstmtHold.setString(3,locCodeFr);
					pstmtHold.setString(4,lotNo);
					pstmtHold.setString(5,lotSl);
					rsHold = pstmtHold.executeQuery();
					balQty = holdQuantity;
					while ( rsHold.next() )
					{
						/*//check whether  there is a confirmed release hold for the same 

							sqlRel = "select count(1) from inv_hold_rel  h, inv_hold_rel_det "
								+ " where h.tran_id = d.tran_id "
								+ " and d.tran_id__hold  = ? "
								+ " and d.line_no__hold  = ? "
								+ " and h.confirmed = 'Y'";
							pstmtRel =  conn.prepareStatement(sqlRel);
							pstmtRel.SetString(1,rsHold.getString("TRAN_ID"));
							pstmtRel.setInt(2,rsHold.getInt("LINE_NO"));
							rsRel = pstmtRel.executeQuery();
							if (rsRelel.next())
							{
							}
							rsRel.close();
							rsRel = null;
							pstmtRel.close();
							pstmtRel = null;
						 */
						//tranIdHold = rsHold.getString("TRAN_ID");// added by Ashish Sonawane on 05-sep-12
						//tranIdHold = rsHold.getString("ref_no");
						//Changed By Rohan on 18-07-13 for getting lock_code
						lockCode = checkNull(rsHold.getString("lock_code"));
						//Changed by Rohan on 06-08-13 to get hold qty
						holdQty = rsHold.getDouble("HOLD_QTY");

						locCodeInvHold = checkNull(rsHold.getString("LOC_CODE"));
						lotSlInvHold = checkNull(rsHold.getString("LOT_SL"));
						pstmt2=conn.prepareStatement("SELECT REF_NO FROM INV_HOLD_REL_TRACE WHERE SITE_CODE=? AND ITEM_CODE=? AND LOT_NO=? AND LOT_SL=? AND LOC_CODE=? AND LOCK_CODE=?");
						pstmt2.setString(1,rsHold.getString("site_code"));
						pstmt2.setString(2,rsHold.getString("item_code"));
						pstmt2.setString(3,lotNo);
						pstmt2.setString(4,lotSl);
						pstmt2.setString(5,locCodeInvHold);
						pstmt2.setString(6,lockCode);
						rs2=pstmt2.executeQuery();
						while(rs2.next())
						{
							tranIdHold = rs2.getString("REF_NO");


							//reasonCodeInvHold = checkNull(rsHold.getString("REAS_CODE"));
							//System.out.println("inv hold loc code="+locCodeInvHold+"@"+lotSlInvHold+"@"+reasonCodeInvHold);
							//System.out.println("invStatLocCodeTo"+invStatLocCodeTo+"locCodeTo"+locCodeTo);
							System.out.println("Current Lock Code"+lockCode+"quantity::"+quantity+"holdQty::"+holdQty);
							//Changed by Rohan on 06-08-13 if partial qty move from pso to pso lacation.start
							//if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()))
							//if(invStatLocCodeTo.trim().equalsIgnoreCase(locCodeTo.trim()) || quantity < holdQty )
							//if(quantity < holdQty )
							//{
							System.out.println("-----Insert Block-----");
							//Changed by Rohan on 17-07-13 for updating INV_HOLD_REL_TRACE
							//insertIntoInvHold(itemCode,siteCode,locCodeTo,lotNo,lotSlTo,tranIdHold,conn);
							System.out.println(">>>>>>>>>>Before calling insertIntoInvHold:");
							insertIntoInvHold(itemCode,siteCode,locCodeTo,lotNo,lotSlTo,tranIdHold,locCodeFr,lotSl,quantity,lockCode,reasonCodeInvHold,conn);
							System.out.println("@@@@ End of insertion...");
							//}
							//	else
							//	{
							//		System.out.println("-----Update Block-----");
							// changed by sankara on 06-07-13 fixing bug updated corret lot_sl in inv_hold det.start	
							/*sqlRel = "UPDATE INV_HOLD_DET SET LOC_CODE = ?, STATUS_DATE = ? "
											+ " WHERE TRAN_ID = ?  AND LINE_NO = ?";
										pstmtUpd = conn.prepareStatement(sqlRel);
										pstmtUpd.setString(1,locCodeTo);
										pstmtUpd.setTimestamp(2,new java.sql.Timestamp( System.currentTimeMillis() ));
										pstmtUpd.setString(3,rsHold.getString("TRAN_ID"));
										pstmtUpd.setInt(4,rsHold.getInt("LINE_NO"));
										pstmtUpd.executeUpdate();    */
							//		sqlRel = "UPDATE INV_HOLD_DET SET LOC_CODE = ?, STATUS_DATE = ?, LOT_SL = ? "
							//				+ " WHERE TRAN_ID = ?  AND LINE_NO = ?";
							//		pstmtUpd = conn.prepareStatement(sqlRel);
							//		pstmtUpd.setString(1,locCodeTo);
							//		pstmtUpd.setTimestamp(2,new java.sql.Timestamp( System.currentTimeMillis() ));
							//		pstmtUpd.setString(3,lotSlTo);
							//		pstmtUpd.setString(4,rsHold.getString("TRAN_ID"));
							//		pstmtUpd.setInt(5,rsHold.getInt("LINE_NO"));
							//		pstmtUpd.executeUpdate();    

							// 23/06/12 manoharan
							// changed by sankara on 06-07-13 fixing bug updated corret lot_sl in inv_hold det.end

							// 23/06/12 manoharan
							//tranIdHold = rsHold.getString("TRAN_ID"); //commented by Ashish Sonawane on 05-Sep-12 and moved to line no 421 
							// changed by sankara on 06-07-13 fixing bug updated corret lot_sl in inv_hold_trace.start				
							/*sqlRel = "UPDATE INV_HOLD_REL_TRACE SET LOC_CODE = ? "
												+ " WHERE REF_NO = ? "
												+ " AND ITEM_CODE = ? "
												+ " AND SITE_CODE = ? "
												+ " AND LOC_CODE = ? "
												+ " AND LOT_NO = ? " 
												+ " AND LOT_SL = ? " ;
											pstmtUpd = conn.prepareStatement(sqlRel);
											pstmtUpd.setString(1,locCodeTo);
											pstmtUpd.setString(2,tranIdHold);
											pstmtUpd.setString(3,itemCode);
											pstmtUpd.setString(4,siteCode);
											pstmtUpd.setString(5,locCodeFr);
											pstmtUpd.setString(6,lotNo);
											pstmtUpd.setString(7,lotSl);
											updCnt = pstmtUpd.executeUpdate();
											System.out.println( "[" + updCnt + "] rows updated successfully in INV_HOLD_REL_TRACE" );
											// end 23/06/12 manoharan     */
							//		sqlRel = "UPDATE INV_HOLD_REL_TRACE SET LOC_CODE = ?, LOT_SL = ?"
							//				+ " WHERE REF_NO = ? "
							//				+ " AND ITEM_CODE = ? "
							//				+ " AND SITE_CODE = ? "
							//				+ " AND LOC_CODE = ? "
							//				+ " AND LOT_NO = ? " 
							//				+ " AND LOT_SL = ? " ;
							//		pstmtUpd = conn.prepareStatement(sqlRel);
							//		pstmtUpd.setString(1,locCodeTo);
							//		pstmtUpd.setString(2,lotSlTo);
							//		pstmtUpd.setString(3,tranIdHold);
							//		pstmtUpd.setString(4,itemCode);
							//		pstmtUpd.setString(5,siteCode);
							//		pstmtUpd.setString(6,locCodeFr);
							//		pstmtUpd.setString(7,lotNo);
							//		pstmtUpd.setString(8,lotSl);
							//		updCnt = pstmtUpd.executeUpdate();
							//		System.out.println( "[" + updCnt + "] rows updated successfully in INV_HOLD_REL_TRACE" );
							// end 23/06/12 manoharan     */
							// changed by sankara on 06-07-13 fixing bug updated corret lot_sl in inv_hold_trace det.end	
							// end 23/06/12 manoharan
							//		}
						}
						rs2.close();
						rs2=null;
						pstmt2.close();
						pstmt2=null;
					}
					rsHold.close();
					rsHold = null;
					pstmtHold.close();
					pstmtHold = null;

				} // end 24/06/12 manoharan not near expiry
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void insertIntoInvHold(String itemCode ,String siteCode, String locCode ,String lotNo, String lotSl, String tranIdHold ,String locCodeFr,String lotSlFr,double holdQuantity,String lockCode,String reasonCode ,Connection conn) throws ITMException
	{
		PreparedStatement pstmtInsertInvHold =null;
		PreparedStatement pstmtMaxLine =null;
		ResultSet rsMaxLine =null ,rs = null;
		//Changed by Rohan on 17-07-13 for updating INV_HOLD_REL_TRACE prepared statement and result set.start
		PreparedStatement pstmt = null;
		String sql = "";
		String tranId = "";

		int lineNoIn=0;
		//Changed by Rohan on 17-07-13 for updating INV_HOLD_REL_TRACE prepared statement and result set.end
		int maxLineNo=0,count = 0,count1 = 0;
		double holdQty=0;
		boolean isAddInvHoldDet=false;


		try {
			System.out.println(">>>>In insertIntoInvHold:");
			String sqlMaxLine ="SELECT MAX(LINE_NO) FROM INV_HOLD_DET WHERE TRAN_ID =?";
			pstmtMaxLine = conn.prepareStatement(sqlMaxLine);
			pstmtMaxLine.setString(1,tranIdHold);
			rsMaxLine =pstmtMaxLine.executeQuery();
			if(rsMaxLine.next())
			{
				maxLineNo =(rsMaxLine.getInt(1))+1;
				System.out.println("line no to be inserted :" +maxLineNo);
			}
			else
			{
				maxLineNo =maxLineNo++;
			}

			ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateTimeFormat());
			Date sysDate = new Date();
			String sysDateStr = sdf.format(sysDate);
			boolean isFound=false;
			boolean isitemHold=false;
			boolean isitemLotHold=false;
			boolean isuniqueHold=false;
			String lineNo="";
			sql="select line_no from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? "
					+"  and lot_no is null and lot_sl is null and loc_code is null and case when hold_status is null then 'H' else hold_status end ='H' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranIdHold);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, siteCode);
			//pstmt.setString(4, lotNo);
			rs =pstmt.executeQuery();
			if(rs.next())
			{
				isitemHold=true;
				lineNo=rs.getString(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;

			if(!isitemHold)
			{
				System.out.println(">>>>isitemHold:"+isitemHold);
				sql="select line_no from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? "
						+"  and lot_no = ? and lot_sl is null and loc_code is null and case when hold_status is null then 'H' else hold_status end ='H' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdHold);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, siteCode);
				pstmt.setString(4, lotNo);
				rs =pstmt.executeQuery();
				if(rs.next())
				{
					isitemLotHold=true;
					lineNo=rs.getString(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
			if(!isitemLotHold)
			{
				System.out.println(">>>>isitemLotHold:"+isitemLotHold);
				sql="select line_no from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? "
						+" and loc_code = ? and lot_no = ? and lot_sl=? and case when hold_status is null then 'H' else hold_status end ='H'  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdHold);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, siteCode);
				pstmt.setString(4, locCodeFr);
				pstmt.setString(5, lotNo);
				pstmt.setString(6, lotSlFr); 
				rs =pstmt.executeQuery();
				if(rs.next())
				{
					isuniqueHold=true;
					lineNo=rs.getString(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
			if(isuniqueHold)
			{
				System.out.println(">>>>isuniqueHold:"+isuniqueHold);
				/*sql="select hold_qty from stock where site_code=? and item_code=? and lot_no=? and lot_sl=? and loc_code=? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,lotNo);
				pstmt.setString(4,lotSlFr);
				pstmt.setString(5,locCodeFr);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					holdQty=rs.getDouble(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(holdQty==0)
				{
					sql="UPDATE INV_HOLD_DET SET LOC_CODE=?,LOT_SL=? WHERE  tran_id = ? and item_code = ? and site_code = ? "
							+" and loc_code = ? and lot_no = ?  and lot_sl = ? and line_no=?";	
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, locCode);
					pstmt.setString(2, lotSl);
					pstmt.setString(3,tranIdHold);
					pstmt.setString(4, itemCode);
					pstmt.setString(5, siteCode);
					pstmt.setString(6, locCodeFr);
					pstmt.setString(7, lotNo);
					pstmt.setString(8, lotSlFr);
					pstmt.setString(9, lineNo);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt=null;	
				/
				else
				{*/
				sql="select count(1) from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? "
						+" and loc_code = ? and lot_no = ? and lot_sl=? and case when hold_status is null then 'H' else hold_status end ='H'  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdHold);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, siteCode);
				pstmt.setString(4, locCode);
				pstmt.setString(5, lotNo);
				pstmt.setString(6, lotSl); 
				rs =pstmt.executeQuery();
				if(rs.next())
				{
					count=rs.getInt(1);	
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				System.out.println(">>>>isuniqueHold true condition count:"+ count);
				if(count==0)
				{
					String sqlInsertInvHold = "INSERT INTO INV_HOLD_DET (TRAN_ID, LINE_NO, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, HOLD_STATUS, STATUS_DATE , SCH_REL_DATE , REAS_CODE ) " +
							" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

					pstmtInsertInvHold = conn.prepareStatement(sqlInsertInvHold);

					pstmtInsertInvHold.setString(1, tranIdHold);
					pstmtInsertInvHold.setInt(2, maxLineNo);
					pstmtInsertInvHold.setString(3, itemCode);
					pstmtInsertInvHold.setString(4, siteCode);
					pstmtInsertInvHold.setString(5, locCode);
					pstmtInsertInvHold.setString(6, lotNo);
					pstmtInsertInvHold.setString(7, lotSl);
					pstmtInsertInvHold.setString(8, "H");
					pstmtInsertInvHold.setTimestamp(9, Timestamp.valueOf( sysDateStr));
					pstmtInsertInvHold.setTimestamp(10, Timestamp.valueOf( sysDateStr));
					pstmtInsertInvHold.setString(11, reasonCode);
					int isInserted = pstmtInsertInvHold.executeUpdate();
					pstmtInsertInvHold.close();
					pstmtInsertInvHold=null;
					System.out.println(">>>isInserted :"+ isInserted);
					if(isInserted > 0) //condition added by sagar on 20/08/15
					{
						isAddInvHoldDet= true;
					}

				}
				sql="select hold_qty from stock where site_code=? and item_code=? and lot_no=? and lot_sl=? and loc_code=? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,lotNo);
				pstmt.setString(4,lotSlFr);
				pstmt.setString(5,locCodeFr);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					holdQty=rs.getDouble(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;

				System.out.println("holdQty>>>>>"+holdQty);
				if(holdQty==0)
				{
					sql="UPDATE INV_HOLD_DET SET  hold_status = 'X' WHERE  tran_id = ? and item_code = ? and site_code = ? "
							+" and loc_code = ? and lot_no = ?  and lot_sl = ? and line_no=?  ";	
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranIdHold);
					pstmt.setString(2, itemCode);
					pstmt.setString(3, siteCode);
					pstmt.setString(4, locCodeFr);
					pstmt.setString(5, lotNo);
					pstmt.setString(6, lotSlFr);
					pstmt.setString(7, lineNo);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt=null;	
				}	

				//}
			}



			/*	sql ="select count(*) from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? "
					+" and loc_code = ? and lot_no = ? and lot_sl=? and hold_status<>'R' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranIdHold);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, locCodeFr);
			pstmt.setString(5, lotNo);
			pstmt.setString(6, lotSlFr); 
			rs =pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);				
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null ;
			if(count>0)
			{
				sql="select hold_qty from stock where site_code=? and item_code=? and lot_no=? and lot_sl=? and loc_code=? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,lotNo);
				pstmt.setString(4,lotSlFr);
				pstmt.setString(5,locCodeFr);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					holdQty=rs.getDouble(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(holdQty==0)
				{
					sql="UPDATE INV_HOLD_DET SET LOC_CODE=?,LOT_SL=? WHERE  tran_id = ? and item_code = ? and site_code = ? "
							+" and loc_code = ? and lot_no = ?  and lot_sl = ? ";	
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, locCode);
					pstmt.setString(2, lotSl);
					pstmt.setString(3,tranIdHold);
					pstmt.setString(4, itemCode);
					pstmt.setString(5, siteCode);
					pstmt.setString(6, locCodeFr);
					pstmt.setString(7, lotNo);
					pstmt.setString(8, lotSlFr);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt=null;	
				}
				else
				{
					String sqlInsertInvHold = "INSERT INTO INV_HOLD_DET (TRAN_ID, LINE_NO, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, HOLD_STATUS, STATUS_DATE , SCH_REL_DATE , REAS_CODE ) " +
							" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

					pstmtInsertInvHold = conn.prepareStatement(sqlInsertInvHold);

					pstmtInsertInvHold.setString(1, tranIdHold);
					pstmtInsertInvHold.setInt(2, maxLineNo);
					pstmtInsertInvHold.setString(3, itemCode);
					pstmtInsertInvHold.setString(4, siteCode);
					pstmtInsertInvHold.setString(5, locCode);
					pstmtInsertInvHold.setString(6, lotNo);
					pstmtInsertInvHold.setString(7, lotSl);
					pstmtInsertInvHold.setString(8, "H");
					pstmtInsertInvHold.setTimestamp(9, Timestamp.valueOf( sysDateStr));
					pstmtInsertInvHold.setTimestamp(10, Timestamp.valueOf( sysDateStr));
					pstmtInsertInvHold.setString(11, reasonCode);
					int isInserted = pstmtInsertInvHold.executeUpdate();
					pstmtInsertInvHold.close();
					pstmtInsertInvHold=null;
				}

			}
			else
			{
				String sqlInsertInvHold = "INSERT INTO INV_HOLD_DET (TRAN_ID, LINE_NO, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, HOLD_STATUS, STATUS_DATE , SCH_REL_DATE , REAS_CODE ) " +
						" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				pstmtInsertInvHold = conn.prepareStatement(sqlInsertInvHold);

				pstmtInsertInvHold.setString(1, tranIdHold);
				pstmtInsertInvHold.setInt(2, maxLineNo);
				pstmtInsertInvHold.setString(3, itemCode);
				pstmtInsertInvHold.setString(4, siteCode);
				pstmtInsertInvHold.setString(5, locCode);
				pstmtInsertInvHold.setString(6, lotNo);
				pstmtInsertInvHold.setString(7, lotSl);
				pstmtInsertInvHold.setString(8, "H");
				pstmtInsertInvHold.setTimestamp(9, Timestamp.valueOf( sysDateStr));
				pstmtInsertInvHold.setTimestamp(10, Timestamp.valueOf( sysDateStr));
				pstmtInsertInvHold.setString(11, reasonCode);
				int isInserted = pstmtInsertInvHold.executeUpdate();
				pstmtInsertInvHold.close();
				pstmtInsertInvHold=null;
			}

			count=0;
			sql ="select count(*) from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? "
					+" and loc_code = ? and lot_no = ? and  lot_sl =  ? and hold_status<>'R' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranIdHold);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, locCode);
			pstmt.setString(5, lotNo);
			pstmt.setString(6, lotSl);
			rs =pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);				
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null ;
			//Checking for lotwise in inv_hold_det if record by manoj dtd 08/12/2014 
			if(count==0)
			{
				sql="select count(*) from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? and lot_no=? and hold_status<>'R'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdHold);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, siteCode);
				pstmt.setString(4, lotNo);
				rs =pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);				
				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null ;
			}
			//Checking for item wise in inv_hold_det if record by manoj dtd 08/12/2014
			if(count==0)
			{
				sql="select count(*) from inv_hold_det where tran_id = ? and item_code = ? and site_code = ? and hold_status<>'R' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranIdHold);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, siteCode);
				rs =pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);				
				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null ;	
			}*/

			//if(count > 0)//aaded by kunal on 22/may/14
			//{
			sql ="select count(*) from INV_HOLD_REL_TRACE  where   REF_NO = ?   and ITEM_CODE = ?  and SITE_CODE = ?  "
					+" and  LOC_CODE = ? and LOT_NO = ?  and  LOT_SL = ? and lock_code=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranIdHold);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, locCode);
			pstmt.setString(5, lotNo);
			pstmt.setString(6, lotSl);
			pstmt.setString(7, lockCode);
			rs =pstmt.executeQuery();
			if(rs.next())
			{
				count1 = rs.getInt(1);				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null ;
			System.out.println(">>>inv_hold_rel_trace count1="+count1);
			if(count1 > 0)
			{
				sql = " update  inv_hold_rel_trace set hold_qty = hold_qty + ? where   ref_no = ?   and item_code = ? "
						+"  and site_code = ? and  loc_code = ? and lot_no = ?  and  lot_sl = ? and lock_code=? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble( 1,holdQuantity );
				pstmt.setString( 2, tranIdHold); 
				pstmt.setString( 3, itemCode ); 
				pstmt.setString( 4, siteCode );
				pstmt.setString( 5, locCode );
				pstmt.setString( 6, lotNo);
				pstmt.setString( 7, lotSl );
				pstmt.setString( 8, lockCode );
				System.out.println("record updated::"+pstmt.executeUpdate());

				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			else
			{
				System.out.println(">>>>Check isAddInvHoldDet:"+isAddInvHoldDet);
				System.out.println(">>>>maxLineNo:"+maxLineNo);
				if(isAddInvHoldDet) // condition added by sagar on 24/07/15
				{
					lineNoIn= maxLineNo;
				}
				else
				{
					if(lineNo !=null && lineNo.trim().length() > 0) 
					{
						lineNoIn= Integer.parseInt(lineNo.trim());
					}
				}
				System.out.println(">>>>lineNo for INV_HOLD_REL_TRACE(REF_LINE):"+ lineNoIn);

				tranId = generateTraceId("w_inv_hold_rel_trace",siteCode, conn);
				sql = " INSERT INTO INV_HOLD_REL_TRACE ( TRAN_ID, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, REF_NO, HOLD_QTY, LOCK_CODE, REF_LINE ) " +
						" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ; // REF_LINE added by sagar on 24/07/15 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, tranId); 
				pstmt.setString( 2, itemCode ); 
				pstmt.setString( 3, siteCode );
				pstmt.setString( 4, locCode );
				pstmt.setString( 5, lotNo);
				pstmt.setString( 6, lotSl );
				pstmt.setString( 7, tranIdHold);
				pstmt.setDouble( 8,holdQuantity );
				pstmt.setString( 9, lockCode );
				pstmt.setInt(10, lineNoIn);
				pstmt.executeUpdate();

				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			/*}
			else
			{

				String sqlInsertInvHold = "INSERT INTO INV_HOLD_DET (TRAN_ID, LINE_NO, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, HOLD_STATUS, STATUS_DATE , SCH_REL_DATE , REAS_CODE ) " +
						" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				pstmtInsertInvHold = conn.prepareStatement(sqlInsertInvHold);

				pstmtInsertInvHold.setString(1, tranIdHold);
				pstmtInsertInvHold.setInt(2, maxLineNo);
				pstmtInsertInvHold.setString(3, itemCode);
				pstmtInsertInvHold.setString(4, siteCode);
				pstmtInsertInvHold.setString(5, locCode);
				pstmtInsertInvHold.setString(6, lotNo);
				pstmtInsertInvHold.setString(7, lotSl);
				pstmtInsertInvHold.setString(8, "H");
				pstmtInsertInvHold.setTimestamp(9, Timestamp.valueOf( sysDateStr));
				pstmtInsertInvHold.setTimestamp(10, Timestamp.valueOf( sysDateStr));
				pstmtInsertInvHold.setString(11, reasonCode);
				int isInserted = pstmtInsertInvHold.executeUpdate();

				System.out.println("@@@@ Hey Row inserted !!!!!!!! .....Total :["+isInserted+"]");
				//Changed by Rohan on 17-07-13 for updating INV_HOLD_REL_TRACE.start

				tranId = generateTraceId("w_inv_hold_rel_trace",siteCode, conn);
				sql = " INSERT INTO INV_HOLD_REL_TRACE ( TRAN_ID, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, REF_NO, HOLD_QTY, LOCK_CODE ) " +
						" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, tranId); 
				pstmt.setString( 2, itemCode ); 
				pstmt.setString( 3, siteCode );
				pstmt.setString( 4, locCode );
				pstmt.setString( 5, lotNo);
				pstmt.setString( 6, lotSl );
				pstmt.setString( 7, tranIdHold);
				pstmt.setDouble( 8,holdQuantity );
				pstmt.setString( 9, lockCode ); 
				pstmt.executeUpdate();

				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}*/
			//sql = " UPDATE INV_HOLD_REL_TRACE SET HOLD_QTY = HOLD_QTY - ? WHERE  ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? ";
			sql = " UPDATE INV_HOLD_REL_TRACE SET HOLD_QTY = HOLD_QTY - ? WHERE  ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?  AND LOCK_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble( 1, holdQuantity); 
			pstmt.setString( 2, itemCode ); 
			pstmt.setString( 3, siteCode );
			pstmt.setString( 4, locCodeFr );
			pstmt.setString( 5, lotNo);
			pstmt.setString( 6, lotSlFr );
			pstmt.setString( 7, lockCode ); 
			pstmt.executeUpdate();
			//Changed by Rohan on 17-07-13 for updating INV_HOLD_REL_TRACE.end



		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		//Changed By Rohan on 18-07-13 for closing prepared statement and result set.start
		finally
		{
			try
			{
				if (rsMaxLine != null)
				{
					rsMaxLine.close();
					rsMaxLine = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmtInsertInvHold != null)
				{
					pstmtInsertInvHold.close();
					pstmtInsertInvHold = null;
				}
				if (pstmtMaxLine != null)
				{
					pstmtMaxLine.close();
					pstmtMaxLine = null;
				}
			}

			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		//Changed By Rohan on 18-07-13 for closing prepared statement and result set.start

	}
	//Changed by Rohan on 17-07-13 for generating tran id for INV_HOLD_REL_TRACE.end
	//change done by kunal on 5/jun/14 add site code for id genration
	public String generateTraceId(String windowName ,String siteCode ,Connection conn)throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";	

		java.sql.Date effDate = null;
		java.sql.Timestamp currDate = null;
		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();

		try
		{
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);
			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			//System.out.println("selSql :"+selSql);
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

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";   
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId); 
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

			catch(Exception e)
			{
			}
		}
		return tranId;
	}//generateTranTd()
	//Changed by Rohan on 17-07-13 for generating tran id for INV_HOLD_REL_TRACE.end

	//addede by kunal on 25/oct/13
	private ArrayList allocTransfer(String siteCode,String itemCode,String locCodeFr ,String lotNoFr,String lotSlFr,String locCodeTo ,String lotNoTo,String lotSlTo,double quantitydet,HashMap stockMap,String xtraParams ,Connection conn ) throws ITMException
	{
		String errString = "";
		String sql = "";
		String refSer = "", refId = "" ,tranId = "",workorder = "";
		//double pendingQty = 0 ;
		int refLine = 0,count = 0,k = 0 ,lineNo = 0 , maxLine = 0;
		double quantity = 0 , refQty = 0 ,stockAllocQty = 0 ,invAllocQty = 0,stockQty = 0 ,pendingQty = 0 ;
		double splitQty = 0, rate = 0;
		boolean checkWOIssue = false;
		PreparedStatement pstmt = null ,pstmt1 = null,pstmt2 = null;
		ResultSet rs = null,rs1 = null,rs2 = null;
		InvAllocTraceBean invAllocTrace = null;
		StockUpdate stkUpdate = null;
		ArrayList retValue = new ArrayList();
		HashMap hashMap = null;
		HashMap tempMap = null ;
		String lineNoStr="";
		String lineNoSpace="";

		try
		{
			System.out.println("allocTransfer called........");
			invAllocTrace = new InvAllocTraceBean();
			stkUpdate = new StockUpdate();
			pendingQty = quantitydet ;
			tempMap = stockMap;

			sql = "select  alloc_qty from  stock where site_code = ? and loc_code = ? and item_code = ? "
					+"  and lot_no = ? and lot_sl = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, locCodeFr);
			pstmt.setString(3, itemCode);
			pstmt.setString(4, lotNoFr);
			pstmt.setString(5, lotSlFr);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				stockAllocQty = rs.getDouble("alloc_qty");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;  

			sql = " select sum(a.qty) from "
					+" (select ref_ser,ref_id ,trim(ref_line), sum(alloc_qty) as qty  from invalloc_trace  where site_code = ? and loc_code = ? and item_code = ? "
					+" and lot_no = ? and lo t_sl = ?  group by ref_ser, ref_id, trim(ref_line)  having sum(alloc_qty) > 0 order by  sum(alloc_qty)  ) a  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, locCodeFr);
			pstmt.setString(3, itemCode);
			pstmt.setString(4, lotNoFr);
			pstmt.setString(5, lotSlFr);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				invAllocQty = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("invAllocQty="+invAllocQty+"    stockAllocQty="+stockAllocQty);
			if(invAllocQty < stockAllocQty) //check stock allocate qty and invalloc_trace qty
			{
				checkWOIssue = true;
			}


			sql = "select ref_ser,ref_id ,trim(ref_line) as ref_line, sum(alloc_qty)  from invalloc_trace  where "
					+" site_code = ? and loc_code = ? and item_code = ? and lot_no = ? and lot_sl = ? "
					+"  group by ref_ser, ref_id ,trim( ref_line)  having sum(alloc_qty) > 0 order by  sum(alloc_qty)  desc  ";
			pstmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, locCodeFr);
			pstmt.setString(3, itemCode);
			pstmt.setString(4, lotNoFr);
			pstmt.setString(5, lotSlFr);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				refSer = checkNull(rs.getString( "ref_ser" )) ;
				refId = checkNull(rs.getString( "ref_id" ));
				//refLine = rs.getInt("ref_line");
				refLine = Integer.parseInt( checkNull(rs.getString("ref_line")).trim().length() == 0 ?"0":rs.getString("ref_line").trim()) ;
				refQty = rs.getDouble(4);
				System.out.println("pendingQty="+pendingQty);
				System.out.println("refSer="+refSer);
				System.out.println("refId="+refId);
				System.out.println("refLine="+refLine);
				System.out.println("refQty="+refQty);
				if(pendingQty <= 0)
				{
					break;
				}
				if(pendingQty <= refQty)
				{
					quantity = pendingQty;
					pendingQty = 0;
				}
				else
				{
					quantity = refQty;
					pendingQty = pendingQty - refQty ;
				}
				stockQty = stockQty + quantity;


				hashMap = new HashMap();
				hashMap.put("tran_date",new java.sql.Date(System.currentTimeMillis()));
				hashMap.put("ref_ser",refSer);
				hashMap.put("ref_id",refId);
				//added by monika o 20 april 2021
				//hashMap.put("ref_line",String.valueOf(refLine));
				lineNoStr=Integer.toString(refLine);
				if(lineNoStr != null && lineNoStr.trim().length() > 0)
				{
					lineNoSpace = getLineNewNo(lineNoStr);
				}
				hashMap.put("ref_line",lineNoSpace);	
				//end	
				hashMap.put("site_code",siteCode);
				hashMap.put("item_code",itemCode);
				hashMap.put("loc_code",locCodeFr);
				hashMap.put("lot_no",lotNoFr);
				hashMap.put("lot_sl",lotSlFr);
				hashMap.put("alloc_qty",new Double(-1*quantity)); 
				hashMap.put("chg_user",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
				hashMap.put("chg_term",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId"));
				errString = invAllocTrace.updateInvallocTrace(hashMap, conn);
				System.out.println("updateInvallocTrace  1....."+errString);
				if ( errString != null && errString.trim().length() > 0 )
				{
					retValue.add("Error");
					retValue.add(new Double(quantitydet));
					return retValue;
				}
			}//end rs loop
			System.out.println("Stock Qty="+stockQty+"  pendingQty="+pendingQty);
			if (stockQty > 0 && ( errString == null || errString.trim().length() == 0)) 
			{
				stockMap.put("quantity", ""+stockQty);
				stockMap.put("qty_stduom", ""+stockQty);
				//stock update for tran type = "I"

				stkUpdate = new StockUpdate();
				errString = stkUpdate.updateStock( stockMap, xtraParams, conn ); //updateStock called only once's
				stkUpdate=null;
				System.out.println("stock update FOR I ....."+errString);
				if ( errString != null && errString.trim().length() > 0 )
				{
					retValue.add("Error");
					retValue.add(new Double(quantitydet));
					return retValue;
				}
				else
				{
					stockMap = tempMap;
					stockMap.put("loc_code", locCodeTo);
					stockMap.put("lot_no", lotNoTo);
					stockMap.put("lot_sl", lotSlTo);
					stockMap.put("tran_type", "R");
					//stock update for tran type = "R"
					stkUpdate = new StockUpdate();
					errString = stkUpdate.updateStock( stockMap, xtraParams, conn );
					stkUpdate=null;
					System.out.println("stock update FOR R ....."+errString);
					if ( errString != null && errString.trim().length() > 0 )
					{
						retValue.add("Error");
						retValue.add(new Double(quantitydet));
						return retValue;
					}
				}

				if ( errString == null || errString.trim().length() == 0)
				{
					if(rs != null)
					{
						rs.first();
						System.out.println("iterate rs for deallcation ");
						pendingQty = quantitydet;
						do  //iterate rs for deallcation 
						{
							splitQty = 0;
							refSer = checkNull(rs.getString( "ref_ser" )) ;
							refId = checkNull(rs.getString( "ref_id" ));
							//refLine = rs.getInt("ref_line");
							refLine = Integer.parseInt( checkNull(rs.getString("ref_line")).trim().length() == 0 ?"0":rs.getString("ref_line").trim()) ;
							refQty = rs.getDouble(4);
							System.out.println("quantitydet="+pendingQty);
							System.out.println("refSer="+refSer);
							System.out.println("refId="+refId);
							System.out.println("refLine="+refLine);
							System.out.println("refQty="+refQty);
							if(pendingQty <= 0)
							{
								break;
							}
							if(pendingQty <= refQty)
							{
								quantity = pendingQty;
								pendingQty = 0;
								splitQty =  refQty - quantity;
							}
							else
							{
								quantity = refQty;
								pendingQty = pendingQty - refQty ;
							}
							hashMap = new HashMap();
							hashMap.put("tran_date",new java.sql.Date(System.currentTimeMillis()));
							hashMap.put("ref_ser",refSer);
							hashMap.put("ref_id",refId);
							//added by monika o 20 april 2021
							//hashMap.put("ref_line",String.valueOf(refLine));
							lineNoStr=Integer.toString(refLine);
							if(lineNoStr != null && lineNoStr.trim().length() > 0)
							{
								lineNoSpace = getLineNewNo(lineNoStr);
							}
							hashMap.put("ref_line",lineNoSpace);	
							//end					
							hashMap.put("site_code",siteCode);
							hashMap.put("item_code",itemCode);
							hashMap.put("loc_code",locCodeTo);
							hashMap.put("lot_no",lotNoTo);
							hashMap.put("lot_sl",lotSlTo);
							hashMap.put("alloc_qty",new Double(quantity)); 
							hashMap.put("chg_user",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
							hashMap.put("chg_term",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId"));
							errString = invAllocTrace.updateInvallocTrace(hashMap, conn);
							System.out.println("updateInvallocTrace 2....."+errString);
							if ( errString != null && errString.trim().length() > 0 )
							{
								retValue.add("Error");
								retValue.add(new Double(quantitydet));
								return retValue;
							}
							else  
							{
								System.out.println("DeAlloc updateInvallocTrace(HashMap, Connection) : Sucessuful!");
								System.out.println("refSer:::::"+refSer);
								if("S-ORD".equalsIgnoreCase(refSer.trim())) //sale order
								{
									System.out.println("update for sale order");
									maxLine = 0;

									System.out.println("update...  "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = "update sord_alloc_det set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ? where sale_order = ? and line_no__sord = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setString(5,refId);
										pstmt1.setInt(6,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated sord_alloc_det ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max(line_no) from sord_alloc_det where sale_order = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into sord_alloc_det (tran_id,line_no,sale_order,line_no__sord,item_code,loc_code,lot_no,lot_sl,quantity,"
													+" dealloc_qty,site_code,exp_lev,pending_qty,wave_flag) "
													+" select tran_id,"+maxLine+",sale_order,line_no__sord,item_code,'"+locCodeTo+"','"+lotNoTo+"','"+lotSlTo+"',"+quantity+",dealloc_qty,"
													+" site_code,exp_lev,pending_qty,wave_flag from sord_alloc_det where sale_order = ? and line_no__sord = ? ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted sord_alloc_det ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = "update sord_alloc_det set  loc_code = ? ,lot_no = ? ,lot_sl = ? where sale_order = ? and line_no__sord = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated sord_alloc_det ="+k);
										pstmt1.close();
										pstmt1 = null;
									}

								}
								else if("D-ISS".equalsIgnoreCase(refSer.trim())) //1.Distribution Issue (DISS)
								{

									System.out.println("update for Distribution Issue (DISS)");
									rate = 0;
									maxLine = 0;
									sql = " select rate from distord_issdet where tran_id = ? and line_no = ?  ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, refId);
									pstmt2.setInt(2, refLine);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										rate = rs2.getDouble("rate");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;

									System.out.println("update..  "+rate+"    "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update distord_issdet set  loc_code = ? ,lot_no = ? ,lot_sl = ? , quantity = ?,amount = ? where tran_id = ?  and line_no = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,splitQty*rate);
										pstmt1.setString(6,refId);
										pstmt1.setInt(7,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated distord_issdet ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max(line_no) from distord_issdet where tran_id = ?  ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into distord_issdet (tran_id,line_no,dist_order,line_no_dist_order,item_code,quantity,unit,tax_class,tax_chap,tax_env,loc_code,lot_no,lot_sl,"
													+" pack_code,rate,amount,tax_amt,net_amt,site_code__mfg,mfg_date,exp_date,potency_perc,no_art,gross_weight,tare_weight,net_weight,pack_instr,dimension," 
													+" supp_code__mfg,batch_no,grade,retest_date,rate_clg,rate__clg,discount,disc_amt,remarks,cost_rate,unit__alt,conv__qty__alt,qty_order__alt,pallet_wt,"
													+" rate__alt,conv__rate_alt,return_qty,return_date,returned,batch_size,carton_no,pallet_no) "
													+" select tran_id,"+maxLine+",dist_order,line_no_dist_order,item_code,"+quantity+",unit,tax_class,tax_chap,tax_env,'"+locCodeTo+"','"+lotNoTo+"','"+lotSlTo+"',"
													+" pack_code,rate,15*2,tax_amt,net_amt,site_code__mfg,mfg_date,exp_date,potency_perc,no_art,gross_weight,tare_weight,net_weight,"
													+" pack_instr,dimension,supp_code__mfg,batch_no,grade,retest_date,rate_clg,rate__clg,discount,disc_amt,remarks,cost_rate,unit__alt,"
													+" conv__qty__alt,qty_order__alt,pallet_wt,rate__alt,conv__rate_alt,return_qty,return_date,returned,batch_size,carton_no,pallet_no  from distord_issdet where tran_id = ? and line_no =  ? ";

											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted distord_issdet ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update distord_issdet set  loc_code = ? ,lot_no = ? ,lot_sl = ? where tran_id = ?  and line_no = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated distord_issdet ="+k);
										pstmt1.close();
										pstmt1 = null;
									}


								}
								else if("S-DSP".equalsIgnoreCase(refSer.trim())) //Despatch
								{
									System.out.println("update for Despatch");
									maxLine = 0;
									System.out.println("update... "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update despatchdet set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ?,quantity__ord = ?,quantity__stduom = ?  where desp_id = ? and line_no = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,splitQty);
										pstmt1.setDouble(6,splitQty);
										pstmt1.setString(7,refId);
										pstmt1.setInt(8,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated despatchdet ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max( line_no)   from despatchdet where desp_id = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;
											sql = " insert into despatchdet (desp_id,line_no,sord_no,line_no__sord,exp_lev,item_code__ord,item_code,lot_no,lot_sl,quantity__ord,quantity,loc_code,status,conv__qty_stduom,"
													+" unit__std,unit,quantity__stduom,quantity_real,rate__stduom,invoice_id,quantity_inv,pack_instr,no_art,site_code,pack_qty,gross_weight,tare_weight,nett_weight,mfg_date,exp_date,"
													+" chg_date,chg_user,chg_term,site_code__mfg,rate__clg,dimension,tax_amt,disc_amt,conf_diff_amt,rate__std,cost_rate,frequency,down_payment,down_payment_int,inst_amount,inst_int_amount,"
													+" no_of_inst,line_type,conv__rtuom_stduom,pallet_wt,cust_item__ref,tran_id__invpack,retest_date,part_no,disc_schem_billback_amt,disc_schem_offinv_amt,sscc_18,pack_code,pallet_no) "
													+"  select desp_id,"+maxLine+",sord_no,line_no__sord,exp_lev,item_code__ord,item_code,'"+lotNoTo+"','"+lotSlTo+"',"+quantity+","+quantity+",'"+locCodeTo+"',status,conv__qty_stduom,unit__std,unit,"+quantity+",quantity_real,rate__stduom,"
													+" invoice_id,quantity_inv,pack_instr,no_art,site_code,pack_qty,gross_weight,tare_weight,nett_weight,mfg_date,exp_date,chg_date,chg_user,chg_term,site_code__mfg,rate__clg,dimension,"
													+" tax_amt,disc_amt,conf_diff_amt,rate__std,cost_rate,frequency,down_payment,down_payment_int,inst_amount,inst_int_amount,no_of_inst,line_type,conv__rtuom_stduom,pallet_wt,cust_item__ref,"
													+" tran_id__invpack,retest_date,part_no,disc_schem_billback_amt,disc_schem_offinv_amt,sscc_18,pack_code,pallet_no "
													+" from despatchdet where desp_id = ? and  line_no = ?  ";

											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted despatchdet ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update despatchdet set  loc_code = ? ,lot_no = ? ,lot_sl = ?  where desp_id = ? and line_no = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated despatchdet ="+k);
										pstmt1.close();
										pstmt1 = null;
									}

								}
								else if("XFRX".equalsIgnoreCase(refSer.trim())) // Stock Transfer Multiple
								{
									System.out.println("update for Stock Transfer Multiple");
									maxLine = 0;
									System.out.println("update...  "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update stock_transfer_det set   loc_code__to = ? ,lot_no__to = ? , lot_sl__to  = ? ,quantity = ? where tran_id = ? and line_no = ?   ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setString(5,refId);
										pstmt1.setInt(6,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated stock_transfer_det ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max(line_no) from stock_transfer_det where tran_id = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into stock_transfer_det (tran_id,line_no,item_code,quantity,loc_code__fr,loc_code__to,lot_no__fr,lot_no__to,lot_sl__fr,lot_sl__to,"
													+" remarks,acct_code__cr,acct_code__dr,cctr_code__dr,cctr_code__cr,no_art,loc_code__sys) "
													+" select tran_id,"+maxLine+",item_code,"+quantity+",'"+locCodeTo+"',loc_code__to,'"+lotNoTo+"',lot_no__to,'"+lotSlTo+"',lot_sl__to, "
													+" remarks,acct_code__cr,acct_code__dr,cctr_code__dr,cctr_code__cr,no_art,loc_code__sys from stock_transfer_det where  tran_id = ? and line_no = ?  ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted stock_transfer_det ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update stock_transfer_det set   loc_code__to = ? ,lot_no__to = ? , lot_sl__to  = ?  where tran_id = ? and line_no = ?   ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated stock_transfer_det ="+k);
										pstmt1.close();
										pstmt1 = null;
									}
								}
								else if("C-ISS".equalsIgnoreCase(refSer.trim())) // Consumption Issue(CISS).
								{
									System.out.println("update for Consumption Issue");
									maxLine = 0;
									rate = 0;
									sql = " select rate  from consume_iss_det where cons_issue = ? and line_no = ?  ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, refId);
									pstmt2.setInt(2, refLine);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										rate = rs2.getDouble("rate");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;
									System.out.println("update... "+rate+"    "+quantity+"       "+splitQty);
									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update consume_iss_det set  loc_code = ?,lot_no = ?,lot_sl = ? ,quantity = ? ,quantity__std = ? ,amount = ? where cons_issue = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,splitQty);
										pstmt1.setDouble(6,splitQty*rate);
										pstmt1.setString(7,refId);
										pstmt1.setInt(8,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated consume_iss_det ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max( line_no) from consume_iss_det where cons_issue = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into consume_iss_det (cons_issue,line_no,cons_order,line_no__ord,item_code,quantity,unit,rate,amount,tax_class,tax_chap,tax_env,tax_amt,net_amt,"
													+" acct_code,cctr_code,loc_code,lot_no,lot_sl,quantity__std,unit__std,conv_qty_stduom,qc_reqd,acct_code__inv,cctr_code__inv,no_art,anal_code,carton_no,pallet_no) "
													+" select cons_issue,"+maxLine+",cons_order,line_no__ord,item_code,"+quantity+",unit,rate,"+rate*quantity+",tax_class,tax_chap,tax_env,tax_amt,net_amt,acct_code,"
													+" cctr_code,'"+locCodeTo+"','"+lotNoTo+"','"+lotSlTo+"',"+quantity+",unit__std,conv_qty_stduom,qc_reqd,acct_code__inv,cctr_code__inv,no_art,anal_code,carton_no,pallet_no "
													+" from consume_iss_det where CONS_ISSUE = ? and line_no = ?  ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted consume_iss_det ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update consume_iss_det set  loc_code = ?,lot_no = ?,lot_sl = ? where cons_issue = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated consume_iss_det ="+k);
										pstmt1.close();
										pstmt1 = null;

									}
								}
								else if("ADJISS".equalsIgnoreCase(refSer.trim())) // Adjustment Issue(ADJISS). 
								{
									System.out.println("update for Adjustment Issue");
									rate = 0;
									maxLine = 0;
									sql = " select rate from  adj_issrcpdet where  tran_id = ? and line_no = ?  ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, refId);
									pstmt2.setInt(2, refLine);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										rate = rs2.getDouble("rate");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;

									System.out.println("1420  "+rate+"    "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update adj_issrcpdet set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ? , amount = ? where tran_id = ? and line_no = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,rate * splitQty);
										pstmt1.setString(6,refId);
										pstmt1.setInt(7,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated adj_issrcpdet ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max(line_no) from  adj_issrcpdet where  tran_id = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into  adj_issrcpdet (tran_id,line_no,item_code,unit,loc_code,lot_no,lot_sl,quantity,sundry_type,sundry_code,rate,gross_rate,grade,dimension,no_art, "
													+" amount,gross_weight,tare_weight,net_weight,acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,potency_perc,pack_code,mfg_date,exp_date, "
													+" site_code__mfg,conv__qty_stduom,unit__alt,conv_qty_stduom,unit_alt,theoretical_wt,retest_date,supp_code__mfg,batch_no) "
													+" select tran_id,"+maxLine+",item_code,unit,'"+locCodeTo+"','"+lotNoTo+"','"+lotSlTo+"', "+quantity+", sundry_type,sundry_code,rate,gross_rate,grade,dimension,no_art, "
													+ rate * quantity +",gross_weight,tare_weight,net_weight,acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,potency_perc,pack_code,mfg_date,exp_date, "
													+"  site_code__mfg,conv__qty_stduom,unit__alt,conv_qty_stduom,unit_alt,theoretical_wt,retest_date,supp_code__mfg,batch_no from adj_issrcpdet  "
													+" where tran_id = ? and line_no = ?  ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted adj_issrcpdet ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update adj_issrcpdet set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ? , amount = ? where tran_id = ? and line_no = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setDouble(4,quantity);
										pstmt1.setDouble(5,rate * quantity);
										pstmt1.setString(6,refId);
										pstmt1.setInt(7,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated adj_issrcpdet ="+k);
										pstmt1.close();
										pstmt1 = null;
									}


								}
								else if("P-RET".equalsIgnoreCase(refSer.trim())) // Purchase Order Return(PRETURN). 
								{
									System.out.println("update for PRETURN");
									rate = 0;
									maxLine = 0;
									sql = " select rate  from porcpdet where tran_id = ? and line_no = ?  ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, refId);
									pstmt2.setInt(2, refLine);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										rate = rs2.getDouble("rate");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;

									System.out.println("update..  "+rate+"    "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update porcpdet set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ?, quantity__stduom = ?,net_amt = ? where tran_id = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,splitQty);
										pstmt1.setDouble(6,rate * splitQty);
										pstmt1.setString(7,refId);
										pstmt1.setInt(8,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated porcpdet ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max(line_no)  from porcpdet where tran_id = ?  ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into porcpdet (tran_id,line_no,item_code,purc_order,quantity,unit,rate,discount,tax_amt,net_amt,loc_code,lot_no,lot_sl,line_no__ord,"
													+" canc_bo,vouch_qty,acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,unit__rate,conv__qty_stduom,conv__rtuom_stduom,unit__std,"
													+" quantity__stduom,rate__stduom,pack_code,no_art,pack_instr,batch_no,mfg_date,expiry_date,gross_weight,tare_weight,net_weight,status,"
													+" potency_perc,supp_code__mnfr,site_code__mfg,reas_code,remarks,challan_qty,grade,tax_class,tax_chap,tax_env,specific_instr,special_instr,"
													+" loc_code__excess_short,excess_short_qty,additional_cost,rate__clg,supp_challan_qty,realised_qty,item_code__mfg,spec_ref,std_rate,dept_code,"
													+" effect_stock,physical_status,benefit_type,licence_no,acct_code__prov_dr,cctr_code__prov_dr,acct_code__prov_cr,cctr_code__prov_cr,form_no,"
													+" retest_date,duty_paid,batch_size,damage_qty,sample_qty,shelf_life__type,qc_reqd,sh_qty,rejc_qty,assetinstall_qty,part_qty,partial_yn,anal_code,carton_no,pallet_no) "
													+" select tran_id,"+maxLine+",item_code,purc_order,"+quantity+",unit,rate,discount,tax_amt,"+quantity*rate+",'"+locCodeTo+"','"+lotNoTo+"','"+lotSlTo+"',line_no__ord,"
													+" canc_bo,vouch_qty,acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,unit__rate,conv__qty_stduom,conv__rtuom_stduom,unit__std, "
													+ quantity+",rate__stduom,pack_code,no_art,pack_instr,batch_no,mfg_date,expiry_date,gross_weight,tare_weight,net_weight,status,potency_perc,supp_code__mnfr,"
													+" site_code__mfg,reas_code,remarks,challan_qty,grade,tax_class,tax_chap,tax_env,specific_instr,special_instr,loc_code__excess_short,excess_short_qty,"
													+" additional_cost,rate__clg,supp_challan_qty,realised_qty,item_code__mfg,spec_ref,std_rate,dept_code,effect_stock,physical_status,benefit_type,licence_no,"
													+" acct_code__prov_dr,cctr_code__prov_dr,acct_code__prov_cr,cctr_code__prov_cr,form_no,retest_date,duty_paid,batch_size,damage_qty,sample_qty,shelf_life__type,"
													+" qc_reqd,sh_qty,rejc_qty,assetinstall_qty,part_qty,partial_yn,anal_code,carton_no,pallet_no  from porcpdet where tran_id = ? and line_no = ?  ";
											System.out.println("insert qry="+sql);

											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted porcpdet ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update porcpdet set  loc_code = ? ,lot_no = ? ,lot_sl = ?  where tran_id = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated porcpdet ="+k);
										pstmt1.close();
										pstmt1 = null;

									}

								}
								else if("R-BFS".equalsIgnoreCase(refSer.trim())) // Receipt Back Flush  
								{
									System.out.println("update for Receipt Back Flush");
									rate = 0;
									maxLine = 0;
									sql = " select rate from receipt_backflush_det  where tran_id = ?   ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, refId);
									pstmt2.setInt(2, refLine);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										rate = rs2.getDouble("rate");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;

									System.out.println("update..  "+rate+"    "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update receipt_backflush_det set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ?,amount = ?,net_amt = ? where tran_id = ? and line_no = ?   ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,rate * splitQty);
										pstmt1.setDouble(6,rate * splitQty);
										pstmt1.setString(7,refId);
										pstmt1.setInt(8,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated receipt_backflush_det ="+k);
										pstmt1.close();
										pstmt1 = null;
										if(k > 0)
										{
											sql = " select select max(line_no) from receipt_backflush_det  where tran_id = ?  ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into receipt_backflush_det (tran_id,line_no,site_code,loc_code,lot_no,lot_sl,item_code,"
													+" ref_no,quantity,rate,amount,tax_amt,discount,net_amt,unit,unit__doc,conv_qty_doc,qty_doc,rate_doc,no_art) "
													+" select tran_id,"+maxLine+",site_code,'"+locCodeTo+"','"+lotNoTo+"','"+lotSlTo+"',item_code,ref_no,"+quantity+",rate,"+quantity*rate+",tax_amt,"
													+" discount," +quantity*rate+",unit,unit__doc,conv_qty_doc,qty_doc,rate_doc,no_art from receipt_backflush_det where tran_id = ? and line_no = ? ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted receipt_backflush_det ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update receipt_backflush_det set  loc_code = ? ,lot_no = ? ,lot_sl = ?  where tran_id = ? and line_no = ?   ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated receipt_backflush_det ="+k);
										pstmt1.close();
										pstmt1 = null;
									}
								}
								else if("S-RET".equalsIgnoreCase(refSer.trim())) // (Sales Return)SRETURN.   
								{
									System.out.println("update for SRETURN.");
									rate = 0;
									maxLine = 0;
									sql = " select rate from receipt_backflush_det where tran_id = ? and line_no = ?  ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, refId);
									pstmt2.setInt(2, refLine);
									rs2 = pstmt2.executeQuery();
									if(rs2.next())
									{
										rate = rs2.getDouble("rate");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;


									System.out.println("update...  "+rate+"    "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update sreturndet set  loc_code = ? ,lot_no = ? ,lot_sl = ? ,quantity = ?,quantity__stduom = ?,net_amt = ?  where tran_id = ? and line_no = ?   ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,rate * splitQty);
										pstmt1.setDouble(6,rate * splitQty);
										pstmt1.setString(7,refId);
										pstmt1.setInt(8,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated sreturndet ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{
											sql = " select max(line_no)   from sreturndet  where  tran_id = ? ";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, refId);
											rs2 = pstmt2.executeQuery();
											if(rs2.next())
											{
												maxLine = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											maxLine++;

											sql = " insert into sreturndet (tran_id,line_no,invoice_id,line_no__inv,item_code,quantity,net_amt,status,"
													+" reas_code,loc_code,stk_opt,rate,lot_no,lot_sl,tax_class,tax_chap,tax_env,unit,chg_date,chg_user,chg_term,"
													+" ret_rep_flag,eff_net_amt,conv__qty_stduom,conv__rtuom_stduom,unit__std,quantity__stduom,rate__stduom,exp_date,discount,tax_amt,"
													+" site_code__mfg,mfg_date,unit__rate,pack_code,full_ret,item_ser,reas_code__org,no_art,rate__clg,rate__stk,cost_rate,"
													+" expiry_deduction,rate__std,claim_qty,physical_qty,rate_std,qc_reqd,line_no__invtrace,mrp_value,sale_order,physical_status,line_type,"
													+" invoice_id__club,part_qty,cust_item__ref,gross_weight,tare_weight,net_weight,cust_item__code,sord_line_no,contract_no,line_no__sform) "
													+" select  tran_id,"+maxLine+",invoice_id,line_no__inv,item_code,"+quantity+","+quantity * rate+",status,reas_code,'"+locCodeTo+"',stk_opt,"
													+" rate,'"+lotNoTo+"','"+lotSlTo+"',tax_class,tax_chap,tax_env,unit,chg_date,chg_user,chg_term,ret_rep_flag,eff_net_amt,conv__qty_stduom,"
													+" conv__rtuom_stduom,unit__std,"+quantity+",rate__stduom,exp_date,discount,tax_amt,site_code__mfg,mfg_date,unit__rate,pack_code,full_ret,"
													+" item_ser,reas_code__org,no_art,rate__clg,rate__stk,cost_rate,expiry_deduction,rate__std,claim_qty,physical_qty,rate_std,qc_reqd,"
													+" line_no__invtrace,mrp_value,sale_order,physical_status,line_type,invoice_id__club,part_qty,cust_item__ref,gross_weight,tare_weight,"
													+" net_weight,cust_item__code,sord_line_no,contract_no,line_no__sform from sreturndet where  tran_id = ? and  line_no = ? ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted adj_issrcpdet ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update sreturndet set  loc_code = ? ,lot_no = ? ,lot_sl = ?  where tran_id = ? and line_no = ?   ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated sreturndet ="+k);
										pstmt1.close();
										pstmt1 = null;
									}
								}
								else if("W-ISS".equalsIgnoreCase(refSer.trim())) // Work order ISS .   
								{
									sql = " update workorder_issdet set  loc_code = ? ,lot_no = ? ,lot_sl = ?  where tran_id = ? and line_no = ?   ";
									pstmt1 =conn.prepareStatement(sql);
									pstmt1.setString(1,locCodeTo);
									pstmt1.setString(2,lotNoTo);
									pstmt1.setString(3,lotSlTo);
									pstmt1.setString(4,refId);
									pstmt1.setInt(5,refLine);
									k = pstmt1.executeUpdate();
									System.out.println("No of row updated workorder_issdet ="+k);
									pstmt1.close();
									pstmt1 = null;

								}
								else if("S-ALC".equalsIgnoreCase(refSer.trim())) // Manual Sorder Allocation   
								{
									System.out.println("update for Manual Sorder Allocation");

									System.out.println("update..  "+quantity+"       "+splitQty);

									if(splitQty > 0)//insert new record for split quantity
									{
										sql = " update sordalloc set  loc_code = ? ,lot_no = ?  ,lot_sl = ? , quantity = ?,quantity__stduom = ? where sale_order = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeFr);
										pstmt1.setString(2,lotNoFr);
										pstmt1.setString(3,lotSlFr);
										pstmt1.setDouble(4,splitQty);
										pstmt1.setDouble(5,splitQty);
										pstmt1.setString(6,refId);
										pstmt1.setInt(7,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated sordalloc ="+k);
										pstmt1.close();
										pstmt1 = null;

										if(k > 0)
										{

											sql = " insert into sordalloc (sale_order,line_no,exp_lev,item_code__ord,item_code,lot_no,lot_sl,loc_code,item_ref,quantity,unit,qty_alloc,"
													+" date_alloc,status,item_grade,exp_date,alloc_mode,site_code,conv__qty_stduom,unit__std,quantity__stduom,mfg_date,site_code__mfg,reas_code,ref_id__alloc,ref_line__no,wave_flag) "
													+" select sale_order,line_no,exp_lev,item_code__ord,item_code,'"+lotNoTo+"','"+lotSlTo+"','"+locCodeTo+"',item_ref,"+quantity+",unit,qty_alloc,date_alloc,"
													+" status,item_grade,exp_date,alloc_mode,site_code,conv__qty_stduom,unit__std,"+quantity+",mfg_date,site_code__mfg,reas_code,"
													+" ref_id__alloc,ref_line__no,wave_flag from  sordalloc where  SALE_ORDER = ? and  LINE_NO = ? "
													+" and site_code = ? and loc_code = ? and item_code = ? and lot_no = ? and lot_sl = ?  ";
											System.out.println("insert qry="+sql);
											pstmt1 =conn.prepareStatement(sql);
											pstmt1.setString(1,refId);
											pstmt1.setInt(2,refLine);
											pstmt1.setString(3,siteCode);
											pstmt1.setString(4,locCodeFr);
											pstmt1.setString(5,itemCode);
											pstmt1.setString(6,lotNoFr);
											pstmt1.setString(7,lotSlFr);
											k = pstmt1.executeUpdate();
											System.out.println("No of row inserted adj_issrcpdet ="+k);
											pstmt1.close();
											pstmt1 = null;
										}
									}
									else
									{
										sql = " update sordalloc set  loc_code = ? ,lot_no = ?  ,lot_sl = ?  where sale_order = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated sordalloc ="+k);
										pstmt1.close();
										pstmt1 = null;

									}
								}
							}

						}while(rs.next());
						//end rs loop

					}
				}
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("after invtracealloc check .. pendingQty =="+pendingQty+"  quantitydet="+quantitydet);
			quantitydet = pendingQty;

			if(pendingQty > 0)
			{

				if(checkWOIssue)//if qty alloc in work order
				{
					stockQty = 0;
					System.out.println("qty alloc in work order ");
					sql = " select inv_allocate.work_order ,inv_alloc_det.tran_id, inv_alloc_det.line_no ,inv_alloc_det.alloc_qty from inv_allocate ,  inv_alloc_det where  inv_allocate.tran_id  =  inv_alloc_det.tran_id "
							+" and case when inv_alloc_det.deallocated is null then 'N' else inv_alloc_det.deallocated end != 'Y' "
							+" and inv_alloc_det.site_code = ? and inv_alloc_det.loc_code = ? and inv_alloc_det.item_code = ? "
							+" and inv_alloc_det.lot_no = ? and inv_alloc_det.lot_sl = ?  order by inv_alloc_det.alloc_qty desc  " ;
					pstmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, locCodeFr);
					pstmt.setString(3, itemCode);
					pstmt.setString(4, lotNoFr);
					pstmt.setString(5, lotSlFr);
					rs = pstmt.executeQuery();
					while(rs.next())
					{

						refId = checkNull(rs.getString( "tran_id" ));
						//refLine = rs.getInt("ref_line");
						refLine = Integer.parseInt( checkNull(rs.getString("line_no")).trim().length() == 0 ?"0":rs.getString("line_no").trim()) ;
						refQty = rs.getDouble("alloc_qty");
						workorder = rs.getString("work_order");

						System.out.println("pendingQty="+pendingQty);
						System.out.println("refSer="+refSer);
						System.out.println("refId="+refId);
						System.out.println("refLine="+refLine);
						System.out.println("refQty="+refQty);
						if(pendingQty <= 0)
						{
							break;
						}
						if(pendingQty <= refQty)
						{
							quantity = pendingQty;
							pendingQty = 0;
						}
						else
						{
							quantity = refQty;
							pendingQty = pendingQty - refQty ;
						}
						stockQty = stockQty + quantity;

						sql = " select workorder_issdet.tran_id ,workorder_issdet.line_no  from workorder_iss , workorder_issdet  where workorder_iss.tran_id = workorder_issdet.tran_id "
								+" and workorder_iss.work_order = ?  and workorder_iss.site_code = ? "
								+" and workorder_issdet.loc_code = ? and workorder_issdet.item_code = ?  and workorder_issdet.lot_no = ? and workorder_issdet.lot_sl = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, workorder);
						pstmt1.setString(2, siteCode);
						pstmt1.setString(3, locCodeFr);
						pstmt1.setString(4, itemCode);
						pstmt1.setString(5, lotNoFr);
						pstmt1.setString(6, lotSlFr);
						rs1 = pstmt1.executeQuery();
						while(rs1.next())
						{
							tranId = rs1.getString("tran_id");
							//lineNo = rs1.getInt("line_no");

							sql = "  select count(*) from workorder_issdet_sl  where tran_id   = ? ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, tranId);
							rs2 = pstmt2.executeQuery();
							if(rs2.next())
							{
								count = rs2.getInt(1);
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;
							if(count > 0) //if weighing is done then give error 
							{
								retValue.add("Error");
								retValue.add(new Double(quantitydet));
								return retValue;
							}
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						hashMap = new HashMap();
						hashMap.put("tran_date",new java.sql.Date(System.currentTimeMillis()));
						hashMap.put("ref_ser","W-ISS");
						hashMap.put("ref_id",refId);

						//added by monika o 20 april 2021
						//hashMap.put("ref_line",String.valueOf(refLine));
						lineNoStr=Integer.toString(refLine);

						if(lineNoStr != null && lineNoStr.trim().length() > 0)
						{
							lineNoSpace = getLineNewNo(lineNoStr);
						}
						hashMap.put("ref_line",lineNoSpace);	
						//end						
						hashMap.put("site_code",siteCode);
						hashMap.put("item_code",itemCode);
						hashMap.put("loc_code",locCodeFr);
						hashMap.put("lot_no",lotNoFr);
						hashMap.put("lot_sl",lotSlFr);
						hashMap.put("alloc_qty",new Double(-1*quantity)); 
						hashMap.put("chg_user",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
						hashMap.put("chg_term",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId"));
						errString = invAllocTrace.updateInvallocTrace(hashMap, conn);
						System.out.println("updateInvallocTrace....."+errString);
						if(errString != null && errString.trim().length() > 0)
						{
							retValue.add("Error");
							retValue.add(new Double(quantitydet));
							return retValue;
						}
					}//rs loop end
					System.out.println("Stock Qty="+stockQty+"  pendingQty="+pendingQty);
					if (stockQty > 0 && ( errString == null || errString.trim().length() == 0)) 
					{
						stockMap = tempMap;
						stockMap.put("loc_code", locCodeFr);
						stockMap.put("lot_no", lotNoFr);
						stockMap.put("lot_sl", lotSlFr);
						stockMap.put("tran_type", "I");
						stockMap.put("quantity", ""+stockQty);
						stockMap.put("qty_stduom", ""+stockQty);
						//stock update for tran type = "I"
						stkUpdate = new StockUpdate();
						errString = stkUpdate.updateStock( stockMap, xtraParams, conn ); //updateStock called only once's 
						stkUpdate = null;
						System.out.println("stock update FOR I ....."+errString);
						if ( errString != null && errString.trim().length() > 0 )
						{
							retValue.add("Error");
							retValue.add(new Double(quantitydet));
							return retValue;
						}
						else
						{
							stockMap.put("loc_code", locCodeTo);
							stockMap.put("lot_no", lotNoTo);
							stockMap.put("lot_sl", lotSlTo);
							stockMap.put("tran_type", "R");
							//stock update for tran type = "R"
							stkUpdate = new StockUpdate();
							errString = stkUpdate.updateStock( stockMap, xtraParams, conn ); //updateStock called only once's 
							stkUpdate = null;
							System.out.println("stock update FOR R ....."+errString);
							if ( errString != null && errString.trim().length() > 0 )
							{
								retValue.add("Error");
								retValue.add(new Double(quantitydet));
								return retValue;
							}
						}
						if ( errString == null || errString.trim().length() == 0)
						{
							if(rs != null)
							{
								rs.first();
								pendingQty = quantitydet;
								System.out.println("iterate rs for deallcation ");
								do 
								{

									refId = checkNull(rs.getString( "tran_id" ));
									//refLine = rs.getInt("ref_line");
									refLine = Integer.parseInt( checkNull(rs.getString("line_no")).trim().length() == 0 ?"0":rs.getString("line_no").trim()) ;
									refQty = rs.getDouble("alloc_qty");
									workorder = rs.getString("work_order");

									System.out.println("pendingQty="+pendingQty);
									System.out.println("refSer="+refSer);
									System.out.println("refId="+refId);
									System.out.println("refLine="+refLine);
									System.out.println("refQty="+refQty);
									if(pendingQty <= 0)
									{
										break;
									}
									if(pendingQty <= refQty)
									{
										quantity = pendingQty;
										pendingQty = 0;
									}
									else
									{
										quantity = refQty;
										pendingQty = pendingQty - refQty ;
									}
									hashMap = new HashMap();
									hashMap.put("tran_date",new java.sql.Date(System.currentTimeMillis()));
									hashMap.put("ref_ser",refSer);
									hashMap.put("ref_id",refId);
									//added bymonika salla on 20 april 2021
									lineNoStr=Integer.toString(refLine);

									if(lineNoStr != null && lineNoStr.trim().length() > 0)
									{
										lineNoSpace = getLineNewNo(lineNoStr);
									}
									hashMap.put("ref_line",lineNoSpace);	
									//end	
									//hashMap.put("ref_line",String.valueOf(refLine));					
									hashMap.put("site_code",siteCode);
									hashMap.put("item_code",itemCode);
									hashMap.put("loc_code",locCodeTo);
									hashMap.put("lot_no",lotNoTo);
									hashMap.put("lot_sl",lotSlTo);
									hashMap.put("alloc_qty",new Double(quantity)); 
									hashMap.put("chg_user",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
									hashMap.put("chg_term",new ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId"));
									errString = invAllocTrace.updateInvallocTrace(hashMap, conn);
									System.out.println("updateInvallocTrace 2....."+errString);
									if ( errString != null && errString.trim().length() > 0 )
									{
										retValue.add("Error");
										retValue.add(new Double(quantitydet));
										return retValue;
									}
									else 
									{

										sql = " update inv_alloc_det set  loc_code = ? ,lot_no = ?  ,lot_sl = ?  where tran_id = ? and line_no = ?  ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,refId);
										pstmt1.setInt(5,refLine);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated inv_alloc_det ="+k);
										pstmt1.close();
										pstmt1 = null;

										sql = " update workorder_issdet set   loc_code = ? ,lot_no = ? ,lot_sl = ? where tran_id in  (select tran_id from workorder_iss where work_order = ? and site_code = ? )  "
												+" and loc_code  = ? and item_code = ? and lot_no = ? and lot_sl = ? ";
										pstmt1 =conn.prepareStatement(sql);
										pstmt1.setString(1,locCodeTo);
										pstmt1.setString(2,lotNoTo);
										pstmt1.setString(3,lotSlTo);
										pstmt1.setString(4,workorder);
										pstmt1.setString(5,siteCode);
										pstmt1.setString(6,locCodeFr);
										pstmt1.setString(7,itemCode);
										pstmt1.setString(8,locCodeFr);
										pstmt1.setString(9,lotSlFr);
										k = pstmt1.executeUpdate();
										System.out.println("No of row updated workorder_issdet ="+k);
										pstmt1.close();
										pstmt1 = null;

									}


								}while(rs.next()); //rs 2nd lopp end 
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;



							}


						}

					}
					quantitydet = pendingQty;
				}//check for work order alloc qty
			}//check pending qty 


		} catch (SQLException e) 
		{
			System.out.println("SQLException :allocDeAlloc :SQLException :==>\n"+e.getMessage());
			errString = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
		catch (Exception e) 
		{
			System.out.println("Exception :allocDeAlloc : :Exception :==>\n"+e.getMessage());
			errString = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("Befor Map");
		System.out.println("err string::"+errString);
		System.out.println("pending qty::"+quantitydet);
		System.out.println("Befor Map");
		retValue.add(errString);
		retValue.add(new Double(quantitydet));

		return retValue;
	}//end allocTransfer
	private HashMap updateQcOrderLotInfo(HashMap updateRowMap ,Connection conn) throws ITMException, Exception
	{
		String sql="",errCode="",sqlQC="";
		ResultSet rs=null, rs2=null,rsQC=null;
		PreparedStatement pstmt=null, pstmtUpd=null,pstmtInsert = null,pstmtQC=null;
		String lotNo="",itemCode="",siteCode="",locationCode="",lotSl="",qorderNo="",locationCodeFrom="";
		String unit="",aprv="",rej="",qcLotLocCode="";
		int cnt=0,cnt1=0,updCnt=0,updCnt1=0,cnt22=0;
		double quantity=0,qtySample=0,qcOrdQty=0;
		double  noArt=0 , netWeight=0 , rate=0 , batchSize=0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		int lineNo=0,qcCount=0,qcLotCount=0;
		DistCommon dist = new DistCommon();
		String retString = "",qOrderNo = "", tranType = "";
		String locType="",locLogic ="",site ="",locUdf ="";
		String locCode="",lineNoStr="";
		System.out.println("	..............");
		try
		{
			if(updateRowMap.get("item_code")!= null)
			{
				itemCode = updateRowMap.get("item_code").toString();
			}
			if(updateRowMap.get("site_code")!= null)
			{
				siteCode = updateRowMap.get("site_code").toString();
			}
			if(updateRowMap.get("loc_code")!= null)
			{
				locationCode = updateRowMap.get("loc_code").toString();
			}
			if(updateRowMap.get("loc_code_from")!= null)
			{
				locationCodeFrom = updateRowMap.get("loc_code_from").toString();
			}
			if(updateRowMap.get("lot_no")!= null)
			{
				lotNo = updateRowMap.get("lot_no").toString();
			}
			if(updateRowMap.get("lot_sl")!= null)
			{
				lotSl = updateRowMap.get("lot_sl").toString();
			}
			if(updateRowMap.get("quantity")!= null)
			{
				quantity = Double.parseDouble(updateRowMap.get("quantity").toString());
			}
			if(updateRowMap.get("line_no")!=null)
			{
				//lineNo = Integer.parseInt(updateRowMap.get("line_no").toString().trim());
				//lineNo =updateRowMap.get("line_no").toString();//ADDED BY MONIKA SALLA 22 APRIL 2021
				System.out.println("lineNoStr1234>>>>>>>"+lineNo);
				//lineNoStr=Integer.toString(lineNo);
				lineNoStr = updateRowMap.get("line_no").toString();
				System.out.println("lineNoStr123456>>>>>>>"+lineNoStr);
			}
			if(updateRowMap.get("no_art")!=null)
			{
				noArt = Double.parseDouble(updateRowMap.get("no_art").toString());
			}
			if(updateRowMap.get("net_weight")!=null)
			{
				netWeight = Double.parseDouble(updateRowMap.get("net_weight").toString());
			}
			if(updateRowMap.get("tran_type")!=null)
			{
				tranType = updateRowMap.get("tran_type").toString();
			}
			String apprLocUdfInput="";
			System.out.println("lotNo>>>>>>>"+lotNo);
			System.out.println("locationCode>>>>>>>"+locationCode);
			locLogic = dist.getDisparams ("999999","APR_LOC_LOGIC",conn);
			locUdf  = dist.getDisparams ("999999","APR_LOC_UDF",conn);
			apprLocUdfInput= dist.getDisparams("999999", "APR_LOC_UDF_INPUTS", conn);

			System.out.println ("locLogic is = ===== "+locLogic);
			System.out.println ("locUdf is = ===== "+locUdf);
			//
			if (locLogic.equalsIgnoreCase("NULLFOUND"))
			{
				errCode = "VTLOCLOG"; 
				retString = itmDBAccess.getErrorString("", errCode, "", "", conn);
			}
			if ("U".equalsIgnoreCase (locLogic ))
			{	
				if (locUdf.equalsIgnoreCase("NULLFOUND")|| locUdf.trim().length() == 0)
				{
					System.out.println("inside if locUdf *********");
					errCode = "VTLOCUDF"; 
					retString = itmDBAccess.getErrorString("", errCode, "", "", conn);
				}
			}
			if ("I".equalsIgnoreCase (locLogic ))
			{
				sql = " select LOC_CODE from item "
						+" where item_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locCode = rs.getString(1);
				}
				pstmt.close();
			}
			else if ("S".equalsIgnoreCase(locLogic))
			{
				sql = "select LOC_CODE__APRV from siteitem "
						+" where item_code = ? and site_code = ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setString(2, siteCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locCode = rs.getString(1);
				}
				pstmt.close();
			}	


			if ("U".equalsIgnoreCase(locLogic)) 
			{
				if((locUdf.equalsIgnoreCase("NULLFOUND")|| locUdf==null))
				{
					errCode = "VTLOCUDF"; 
					retString = itmDBAccess.getErrorString("", errCode, "", "", conn);
				}

				//if (!(locUdf.equalsIgnoreCase("NULLFOUND")|| locUdf.trim().length() == 0))
				System.out.println("Site Code####"+siteCode);
				System.out.println("ApprLocationUdfinput#######"+ apprLocUdfInput);
				System.out.println("locUdf#######"+ locUdf);
			}
			if (apprLocUdfInput != null && apprLocUdfInput.trim().length() > 0 && !(apprLocUdfInput.equalsIgnoreCase("NULLFOUND")))
			{
				System.out.println("ApprLocationUdfinput"+ apprLocUdfInput);
				String lotNoIssueOut = "";
				String lotNoIssueOut1 = "";
				String arrStr[] = apprLocUdfInput.split(",");
				int len =arrStr.length;
				for(int i =0;i<len;i++)
				{
					System.out.println("arrStr[i]"+arrStr[i]);
					if(arrStr[i].equals("LOC_CODE__ISS"))
					{
						lotNoIssueOut1=locationCode;
						System.out.println("Orignal Value of lOcation Code is"+locationCode);
						System.out.println("Value of lOcation Code is"+lotNoIssueOut1);
					}
					if(arrStr[i].equals("SITE_CODE"))
					{
						lotNoIssueOut1=siteCode;
						System.out.println("Orignal Value of Site Code is"+siteCode);
						System.out.println("Value of Site Code is"+lotNoIssueOut1);
					}
					if(arrStr[i].equals("ITEM_CODE"))
					{
						lotNoIssueOut1=itemCode;
						System.out.println("Orignal Value of itemCOde Code is"+itemCode);
						System.out.println("Value of itemCOde Code is"+lotNoIssueOut1);
					}
					lotNoIssueOut = lotNoIssueOut + "'"+lotNoIssueOut1+"',";
				}
				lotNoIssueOut = lotNoIssueOut.substring(0,lotNoIssueOut.length()-1);
				System.out.println("Value is--------->"+ lotNoIssueOut);
				sql = "select " + locUdf + "("+lotNoIssueOut + ") from dual";
				pstmt = conn.prepareStatement(sql);
				rs2 = pstmt.executeQuery();
				if (rs2.next())
				{
					locCode = rs2.getString(1);
				}
				else
				{
					errCode = "DS000"; 
					retString = itmDBAccess.getErrorString("", errCode, "", "", conn);
				}
				pstmt.close();
				pstmt = null;
				rs2.close();
				rs2 = null;
				//	}
			}
			else
			{
				if( locUdf != null && locUdf.trim().length() > 0)
				{
					String SiteCode1="";
					String ItemCode1="";
					System.out.println("inside 2nd if locUdf *********");
					sql = "select " + locUdf + "('" + locationCode + "') from dual";//Added single quotes by dhiraj chavan on 8-june-2016
					pstmt = conn.prepareStatement(sql);
					//loc = rs.getString(1);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						locCode = rs.getString(1);
					}
					else
					{
						errCode = "DS000"; 
						retString = itmDBAccess.getErrorString("", errCode, "", "", conn);
					}
					pstmt.close();
					System.out.println("locUdf else "+locCode );
				}
				//else
				//{
				//errCode = "VTLOCUDF"; 
				//retString = itmDBAccess.getErrorString("", errCode, "", "", conn);
				//}
			}
			System.out.println("LOCATION CODE :" +locCode);
			System.out.println("LOCATION CODE :" +locationCode);
			//
			if (quantity > 0)
			{

				System.out.println("inside first loop::::");

				sqlQC = "select count(1),QORDER_NO from qc_order where site_code = ? and item_code = ? and lot_no  = ? and status <> 'C' group by QORDER_NO ";

				pstmtQC = conn.prepareStatement(sqlQC);
				pstmtQC.setString(1, siteCode);
				pstmtQC.setString(2, itemCode);
				pstmtQC.setString(3, lotNo);
				rsQC = pstmtQC.executeQuery();
				if (rsQC.next())
				{
					qcCount = rsQC.getInt(1);
					qOrderNo = rsQC.getString(2);
				}
				rsQC.close();
				rsQC = null;
				pstmtQC.close();
				pstmtQC = null;

				qOrderNo = qOrderNo == null ? "" : qOrderNo.trim();
				System.out.println("qc order no ::: " + qOrderNo);

				if(qcCount > 0){

					System.out.println("inside qc count::: ");

					sql = " select count (*) as cnt,quantity from qc_order_lots where item_code = ? and lot_no = ? and loc_code__issue = ? " +
							" and lot_sl=? and QORDER_NO = ? group by quantity";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, lotNo);
					pstmt.setString(3, locationCode);
					pstmt.setString(4, lotSl);
					pstmt.setString(5, qOrderNo);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt("cnt");
						qcOrdQty = rs.getDouble("quantity");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					System.out.println("Count **** "+ cnt);
					System.out.println("QUantity ****" + qcOrdQty);

					if(tranType.equalsIgnoreCase("ID")){

						System.out.println("inside tran type::: ID");

						if(cnt > 0){

							System.out.println("deleting:::");

							sql = "update qc_order_lots set quantity = ?-?  where item_code = ? and lot_no = ? and loc_code__issue = ? " +
									"and lot_sl=? and qorder_no = ?";//TODO update delete

							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setDouble(1, qcOrdQty);
							pstmtUpd.setDouble(2, quantity);
							pstmtUpd.setString(3, itemCode);
							pstmtUpd.setString(4, lotNo);
							pstmtUpd.setString(5, locationCode);
							pstmtUpd.setString(6, lotSl);
							pstmtUpd.setString(7, qOrderNo);

							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null; 

						}

						/*sql = " select quantity from qc_order_lots where item_code = ? and lot_no = ? and loc_code__issue = ? " +
	 							" and lot_sl=? and QORDER_NO = ?";

	 					pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, lotNo);
						pstmt.setString(3, locationCode);
						pstmt.setString(4, lotSl);
						pstmt.setString(5, qOrderNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{

							qcOrdQty = rs.getDouble("quantity");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(qcOrdQty == 0){

							sql = "delete from qc_order_lots where item_code = ? and lot_no = ? and loc_code__issue = ? " +
		 							" and lot_sl=? and QORDER_NO = ? ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, lotNo);
							pstmt.setString(3, locationCodeFrom);
							pstmt.setString(4, lotSl);
							pstmt.setString(5, qOrderNo);
							pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

						}*/
					}else if(tranType.equalsIgnoreCase("R")){

						System.out.println("tran type R::: ");

						if(cnt > 0){

							System.out.println("adding::::");

							sql = "update qc_order_lots set quantity = ?+?  where item_code = ? and lot_no = ? and loc_code__issue = ? " +
									"and lot_sl=? and qorder_no = ?"; //TODO update add

							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setDouble(1, qcOrdQty);
							pstmtUpd.setDouble(2, quantity);
							pstmtUpd.setString(3, itemCode);
							pstmtUpd.setString(4, lotNo);
							pstmtUpd.setString(5, locationCode);
							pstmtUpd.setString(6, lotSl);
							pstmtUpd.setString(7, qOrderNo);

							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null; 


						}else{

							System.out.println("INSERT BLOCK");
							sqlQC = "select unit,qty_sample from qc_order where  site_code = ? and item_code = ? and lot_no  = ? "
									+ " and status <> 'C' and qorder_no = ?";
							pstmtQC = conn.prepareStatement(sqlQC);
							pstmtQC.setString(1, siteCode);
							pstmtQC.setString(2, itemCode);
							pstmtQC.setString(3, lotNo);
							pstmtQC.setString(4, qOrderNo);

							rsQC = pstmtQC.executeQuery();
							if (rsQC.next())
							{
								unit = rsQC.getString("unit");
								qtySample = rsQC.getDouble("qty_sample");
							}
							rsQC.close();
							rsQC = null;
							pstmtQC.close();
							pstmtQC = null;

							unit = unit == null ? "" : unit.trim();
							System.out.println("unit:::::" + unit);

							sql = "select max(line_no) as line_no from qc_order_lots " +
									" where item_code = ? and lot_no = ? and qorder_no= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, lotNo);
							pstmt.setString(3, qOrderNo);
							rs2 = pstmt.executeQuery();
							if (rs2.next())
							{
								lineNo = Integer.parseInt(rs2.getString("line_no") == null ? "0" : rs2.getString("line_no"));
								//qcLotLocCode = rs2.getString("loc_code");
							}
							pstmt.close();
							pstmt = null;
							rs2.close();
							rs2 = null;

							lineNo++;
							System.out.println("line no to be inserted::: " + lineNo);

							sql = " Insert into qc_order_lots (QC_ORDER,LINE_NO,ITEM_CODE,LOT_NO,LOT_SL,LOC_CODE,QUANTITY," +
									"UNIT,SAMPLE_QTY,LOCTYPE,LOC_CODE__ISSUE,QORDER_NO,NO_ART,NET_WEIGHT) " + 
									" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; //TODO insert
							pstmtInsert = conn.prepareStatement(sql);
							pstmtInsert.setString(1, qOrderNo);
							pstmtInsert.setInt(2, lineNo);
							pstmtInsert.setString(3, itemCode);
							pstmtInsert.setString(4, lotNo);
							pstmtInsert.setString(5, lotSl);
							//pstmtInsert.setString(6, locationCode); 
							//pstmtInsert.setString(6, qcLotLocCode);
							quantity = getUnroundDecimal(quantity, 3);
							pstmtInsert.setString(6, locCode);
							pstmtInsert.setDouble(7, quantity);
							pstmtInsert.setString(8, unit);
							pstmtInsert.setDouble(9, qtySample);
							pstmtInsert.setString(10, "A");
							pstmtInsert.setString(11, locationCode);
							pstmtInsert.setString(12, qOrderNo);
							pstmtInsert.setDouble(13, noArt);
							pstmtInsert.setDouble(14, netWeight);
							//pstmtInsert.setString(15, locCode);
							updCnt1 = pstmtInsert.executeUpdate();
							pstmtInsert.close();
							pstmtInsert = null;
						}
					}
				}

				sql = "delete from qc_order_lots where QORDER_NO = ? and QUANTITY = 0 ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, qOrderNo);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;

			}
			/* sqlQC = "select count(1) from qc_order "
						+ " where  site_code = ? "
						+ " and item_code = ? " 
						+ " and lot_no  = ? "
						+ " and status <> 'C' ";

				pstmtQC = conn.prepareStatement(sqlQC);
				pstmtQC.setString(1, siteCode);
				pstmtQC.setString(2, itemCode);
				pstmtQC.setString(3, lotNo);
				rsQC = pstmtQC.executeQuery();
				if (rsQC.next())
				{
					qcCount = rsQC.getInt(1);
				}
				rsQC.close();
				rsQC = null;
				pstmtQC.close();
				pstmtQC = null;
				if (qcCount > 0)
				{
					sql = " select count (*) as cnt,quantity from qc_order_lots " +
							"where item_code = ? and lot_no = ?  and loc_code__issue = ? and lot_sl=? group by quantity";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, lotNo);
					pstmt.setString(3, locationCode);
					pstmt.setString(4, lotSl);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt("cnt");
						qcOrdQty = rs.getDouble("quantity");
						System.out.println("Count **** "+ cnt);
						System.out.println("QUantity ****" + qcOrdQty);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (cnt > 0)
					{
						if( locationCodeFrom != null && locationCodeFrom.trim().length()>0 )
						{
							sql = "update qc_order_lots set quantity = ?+?  where item_code = ?" +
									" and lot_no = ? and loc_code__issue = ? and lot_sl=?";
							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setDouble(1, qcOrdQty);
							pstmtUpd.setDouble(2, quantity);
							pstmtUpd.setString(3, itemCode);
							pstmtUpd.setString(4, lotNo);
							pstmtUpd.setString(5, locationCode);
							pstmtUpd.setString(6, lotSl);
							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null;

							sql = " select count (*) as cnt,quantity from qc_order_lots " +
									"where item_code = ? and lot_no = ?  and loc_code__issue = ? and lot_sl=? group by quantity";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, lotNo);
							pstmt.setString(3, locationCodeFrom);
							pstmt.setString(4, lotSl);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								//cnt22 = rs.getInt("cnt");
								cnt22 = Integer.parseInt(rs.getString("cnt") == null ? "0" : rs.getString("cnt"));
								qcOrdQty = Double.parseDouble(rs.getString("quantity") == null ? "0" : rs.getString("quantity"));
								System.out.println("Count 22"+ cnt22);
								System.out.println("Quantity 22" + qcOrdQty);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null; V  
							if(cnt22 > 0)
							{
								if (qcOrdQty == 0)
								{
									sql = " delete from qc_order_lots where item_code = ? and lot_no = ? " 
											+ "and loc_code__issue = ? and lot_sl=?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemCode);
									pstmt.setString(2, lotNo);
									pstmt.setString(3, locationCodeFrom);
									pstmt.setString(4, lotSl);
									pstmt.executeUpdate();
									pstmt.close();
									pstmt = null;

								}

							}
						}
						else
						{
							sql = "update qc_order_lots set quantity = ?-?  where item_code = ? " +
									"and lot_no = ? and loc_code__issue = ? and lot_sl=?";
							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setDouble(1, qcOrdQty);
							pstmtUpd.setDouble(2, quantity);
							pstmtUpd.setString(3, itemCode);
							pstmtUpd.setString(4, lotNo);
							pstmtUpd.setString(5, locationCode);
							pstmtUpd.setString(6, lotSl);
							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null;
						}
					} else
					{
						System.out.println("Else part **** count");
						sql = " select quantity from qc_order_lots " 
								+ "where item_code = ? and lot_no = ?  and loc_code__issue=? and lot_sl=?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, lotNo);
						pstmt.setString(3, locationCodeFrom);
						pstmt.setString(4, lotSl);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							qcOrdQty = rs.getDouble("quantity");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@ qcOrdQty To:" + qcOrdQty+"quantity To:"+quantity);
						if(qcOrdQty==0)
						{
							sql = "update qc_order_lots set quantity = ? ,loc_code__issue= ?,loc_code=? ,no_art = ?, lot_no = ?, lot_sl=? " +
								  " where item_code = ? and lot_no = ? and loc_code__issue = ?  and lot_sl=?";
							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setDouble(1, quantity);
							pstmtUpd.setString(2, locationCode);
							pstmtUpd.setString(3, locCode);
							pstmtUpd.setDouble(4, noArt);
							pstmtUpd.setString(5, lotNo);
							pstmtUpd.setString(6, lotSl);
							pstmtUpd.setString(7, itemCode);
							pstmtUpd.setString(8, lotNo);
							pstmtUpd.setString(9, locationCodeFrom);
							pstmtUpd.setString(10, lotSl);
							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null;
						}
						else
						{
							System.out.println("INSERT BLOCK");
							sqlQC = "select qorder_no,unit,qty_sample from qc_order " + " where  site_code = ? "
									+ " and item_code = ? " + " and lot_no  = ? "
									+ " and status <> 'C' ";
							pstmtQC = conn.prepareStatement(sqlQC);
							pstmtQC.setString(1, siteCode);
							pstmtQC.setString(2, itemCode);
							pstmtQC.setString(3, lotNo);
							rsQC = pstmtQC.executeQuery();
							if (rsQC.next())
							{
								qorderNo = rsQC.getString("qorder_no");
								unit = rsQC.getString("unit");
								qtySample = rsQC.getDouble("qty_sample");
							}
							rsQC.close();
							rsQC = null;
							pstmtQC.close();
							pstmtQC = null;

							sql = "select max(line_no) as line_no from qc_order_lots " +
								  " where item_code = ? and lot_no = ? and qorder_no= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, lotNo);
							pstmt.setString(3, qorderNo);
							rs2 = pstmt.executeQuery();
							if (rs2.next())
							{
								lineNo = Integer.parseInt(rs2.getString("line_no") == null ? "0" : rs2.getString("line_no"));
								//qcLotLocCode = rs2.getString("loc_code");
							}
							pstmt.close();
							pstmt = null;
							rs2.close();
							rs2 = null;

							lineNo++;
							sql = " Insert into qc_order_lots (QC_ORDER,LINE_NO,ITEM_CODE,LOT_NO,LOT_SL,LOC_CODE,QUANTITY," +
									"UNIT,SAMPLE_QTY,LOCTYPE,LOC_CODE__ISSUE,QORDER_NO,NO_ART,NET_WEIGHT) " + 
									" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
							pstmtInsert = conn.prepareStatement(sql);
							pstmtInsert.setString(1, qorderNo);
							pstmtInsert.setInt(2, lineNo);
							pstmtInsert.setString(3, itemCode);
							pstmtInsert.setString(4, lotNo);
							pstmtInsert.setString(5, lotSl);
							// pstmtInsert.setString(6, locationCode);
//							pstmtInsert.setString(6, qcLotLocCode);
							pstmtInsert.setString(6, locCode);
							quantity = getUnroundDecimal(quantity, 3);
							pstmtInsert.setDouble(7, quantity);
							pstmtInsert.setString(8, unit);
							pstmtInsert.setDouble(9, qtySample);
							pstmtInsert.setString(10, "A");
							pstmtInsert.setString(11, locationCode);
							pstmtInsert.setString(12, qorderNo);
							pstmtInsert.setDouble(13, noArt);
							pstmtInsert.setDouble(14, netWeight);
							//pstmtInsert.setString(15, locCode);
							updCnt1 = pstmtInsert.executeUpdate();
							pstmtInsert.close();
							pstmtInsert = null;
						}

					}
				}*/

		} catch (Exception e)
		{
			System.out.println("Exception " + e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return updateRowMap;
	}
	public double getUnroundDecimal(double actVal, int prec)
	{

		String fmtStr = "############0";
		String strValue = null;
		double retVal = 0;
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec + 1);
		}
		System.out.println("fmtStr value [" + fmtStr + "]");
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		strValue = decFormat.format(actVal);
		System.out.println(" actVal [" + actVal + "] integer [" + strValue.substring(0,strValue.indexOf(".") +1) + "]");
		System.out.println("decimal [" + strValue.substring(strValue.indexOf(".") + 1, strValue.indexOf(".") + prec + 1) + "]");
		retVal = Double.parseDouble( strValue.substring(0,strValue.indexOf(".") +1) + strValue.substring(strValue.indexOf(".") + 1, strValue.indexOf(".") + prec + 1));
		System.out.println("rounded value [" + retVal + "]");

		return retVal;
	}

	//added by monika salla on 20 april 21 for leading space

	private String getLineNewNo(String lineNo)	
	{
		lineNo = lineNo.trim();
		System.out.println("lineNo"+lineNo);
		String lenStr = "   " + lineNo ;
		System.out.println("lenStr"+lenStr);
		String lineNoNew = lenStr.substring(lenStr.length() - 3, lenStr.length());

		System.out.println("lineNonew["+lineNoNew+"]");

		return lineNoNew;
	}
	
	//added on 11 may 2021 to show line no in message id
	private String getModifiedErrorString(String errString, String modifiedString)
	{
		String xmlStr1 = "";
		String xmlStr2 = "";
		//String xmlStr3 = "";
		try
		{
			if(errString.indexOf("<trace>") != -1)
			{
				xmlStr1 = errString.substring(0,errString.indexOf("<trace>"));
				//xmlStr3 = errString.substring(errString.indexOf("<description>")+"<description>".length(),errString.indexOf("</description>"));
				xmlStr2 = errString.substring(errString.indexOf("</trace>")+"</trace>".length());

				modifiedString = "<trace>" + modifiedString + "</trace>";

				errString = xmlStr1 + modifiedString + xmlStr2;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("getModifiedErrorString >> errString ::"+errString);
		return errString;
	}
	//end

}
