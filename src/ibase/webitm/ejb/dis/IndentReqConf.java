package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.*;


@Stateless
public class IndentReqConf extends ActionHandlerEJB implements IndentReqConfLocal, IndentReqConfRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	DistCommon discommon = new DistCommon();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	private boolean isWorkflow = false;

	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentReqConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}
	// Added by AMOL on 18-12-18 [START]
	public String confirm(String tranID,String xtraParams, String forcedFlag,String userInfoStr) throws RemoteException,ITMException
	{
		System.out.println("#### Calling through workflow confirm ...");
		isWorkflow = true;
		String retString = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			String transDB = userInfo.getTransDB();
			System.out.println("#### TransDB connection in : "+transDB);

			if (transDB != null && transDB.trim().length() > 0)
			{
				conn = connDriver.getConnectDB(transDB);
			}
			else
			{
				conn = connDriver.getConnectDB("DriverITM");
			}
			conn.setAutoCommit(false);
			connDriver = null;

			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
			isWorkflow = false;
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentReqConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}
	//// Added by AMOL on 18-12-18 [END]

	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
		System.out.println(" ========= Inside IndentReqConf confirm ============= ");
		System.out.println(" =========  tranId ============= "+tranId);
		System.out.println("xtraParams ::::::::::::: " + xtraParams);

		String errString = "", sql = "", childNodeName = "", userId = "", errorType = "", errCode = "", ld_conf_date = "", ls_runopt = "";
		PreparedStatement pstmt = null, pstmtInsert = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		boolean isError = false;
		int cnt = 0;
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String> errFields = new ArrayList <String>();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");

		String ls_confirm = "", mstatus = "", ls_emp_code__iapr = "", ls_empcodepur = "", ls_sitecode = "", ls_itemcode = "", ls_login_emp = "",
				li_level = "", ls_ecode = "", conf = "", ls_status = "", inddate = "", indtype = "", site = "", sitedlv = "", dept = "", emp = "", 
				work = "", itemdescr = "", newind = "", itemcode = "", userid = "", ldt_chg_date = "", termid = "", ls_ind_seq = "",
				proj = "", ls_siteacct = "", ls_sitebill = "", ls_sitedes = "", ls_ref_no = "",  ls_task_code = "", ls_indno = "", 
				sql1 = "", unit = "", speinst = "", priority = "", apr = "", splinst = "", acct = "", cctr = "", ls_emp = "", ls_supp = "",
				ld_purcrate = "", ls_pack = "", lc_pur_lead = "", ls_pack_instr = "", ls_supp_mnfr = "", lc_qty_std = "", indno = "", 
				ls_str = "", ldt_today = "", 
				ls_unit_std = "", lc_conv = "", ls_emp_pur = "", ls_remarks = "", ld_quantity_atsite = "", ld_aveconsqty_atsite = "",
				ld_reorderqty_atsite = "", ls_emp_code__qcaprv = "", ld_max_rate = "", ls_item_cd_mfg = "", LS_WORK_ORDER = "", ls_anal_code = ""; 
		//Modified by Anjali R. on[24/04/2018][To set quantity in double format][Start]
		/*double lc_qty_stduom = 0;
		int  qty = 0, ll_start = 1, lc_quantity = 0, ll_lineno = 0, lineno = 0;*/
		double lc_qty_stduom = 0 ,quantity = 0.0 ,qty = 0.0;
		//int  qty = 0, ll_start = 1, lc_quantity = 0, ll_lineno = 0, lineno = 0;
		int   ll_start = 1, lc_quantity = 0, ll_lineno = 0, lineno = 0;
		//Modified by Anjali R. on[24/04/2018][To set quantity in double format][End]
		java.sql.Date ldt_reqdate = null, reqdate = null;

		//Modified by Anjali R. on[25/04/2018][To get todays date][Start]
		java.sql.Timestamp today = null;
		java.util.Date date = null;
		//Modified by Anjali R. on[25/04/2018][To get todays date][End]

		String dimension="";   //added by manish mhatre on 16-4-21
		double noArt=0;       //added by manish mhatre on 16-4-21

		try
		{
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));

			if ( conn == null )
			{
				conn = getConnection();
			}

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currDate = new Date();
			indno = tranId;
			ls_ind_seq = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			ls_str = "A";

			sql = "SELECT CONFIRMED,STATUS,IND_DATE,IND_TYPE,SITE_CODE__ORI, SITE_CODE__DEL, DEPT_CODE, EMP_CODE__REQ, WORK_ORDER," +
					" PROJ_CODE, SITE_CODE__ACCT, SITE_CODE__BIL,SITE_CODE__DES, REF_NO ,TASK_CODE FROM  INDENT_HDR WHERE IND_NO = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, indno);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				conf = checkNullAndTrim(rs.getString("CONFIRMED"));
				ls_status = checkNullAndTrim(rs.getString("STATUS"));
				inddate = checkNullAndTrim(rs.getString("IND_DATE"));
				indtype = checkNullAndTrim(rs.getString("IND_TYPE"));
				site = checkNullAndTrim(rs.getString("SITE_CODE__ORI"));
				sitedlv = checkNullAndTrim(rs.getString("SITE_CODE__DEL"));
				dept = checkNullAndTrim(rs.getString("DEPT_CODE"));
				emp = checkNullAndTrim(rs.getString("EMP_CODE__REQ"));
				work = checkNullAndTrim(rs.getString("WORK_ORDER"));
				proj = checkNullAndTrim(rs.getString("PROJ_CODE"));
				ls_siteacct = checkNullAndTrim(rs.getString("SITE_CODE__ACCT"));
				ls_sitebill = checkNullAndTrim(rs.getString("SITE_CODE__BIL"));
				ls_sitedes = checkNullAndTrim(rs.getString("SITE_CODE__DES"));
				ls_ref_no = checkNullAndTrim(rs.getString("REF_NO"));
				ls_task_code = checkNullAndTrim(rs.getString("TASK_CODE"));
			}
			else
			{
				//Changed by wasim on 10-04-2017 for returning if record not found
				//errCode = "VTMCONF20";		
				//errList.add( errCode );
				//errFields.add(childNodeName.toLowerCase());
				errString = itmDBAccess.getErrorString("","VTMCONF20","","",conn);
				return errString;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}				
			if("C".equalsIgnoreCase(ls_status))
			{
				//Changed by wasim on 10-04-2017 for returning if transaction is calcelled
				//errCode = "VTINDCONF1";		
				//errList.add( errCode );
				//errFields.add(childNodeName.toLowerCase());
				errString = itmDBAccess.getErrorString("","VTINDCONF1","","",conn);
				return errString;
			}
			if(conf.length() == 0)
			{
				conf = "N";
			}
			if("Y".equalsIgnoreCase(conf))
			{
				//Changed by wasim on 10-04-2017 for returning if transaction is already confirmed
				//errCode = "VTINDCONF";		
				//errList.add( errCode );
				//errFields.add(childNodeName.toLowerCase());
				errString = itmDBAccess.getErrorString("","VTINDCONF","","",conn);
				return errString;
			}	

			sql = "SELECT COUNT(*) FROM INDENTITEM_DET WHERE IND_NO = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, indno);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}

			if(cnt == 0)
			{
				sql = "SELECT COUNT(1) FROM INDENT_DET WHERE IND_NO = ? AND REQ_DATE IS NULL ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, indno);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}

				if(cnt > 0)
				{
					/*errCode = "VTNOITEM";		
					errList.add( errCode );
					errFields.add(childNodeName.toLowerCase());*/
					errString = itmDBAccess.getErrorString("","VTNOITEM","","",conn);
					return errString;
				}

				cnt = 0;
				//Changed by kunal on 19/04/2018 for removing INDENTITEM part 
				/*sql = "SELECT IND_NO, LINE_NO, ITEM_CODE, QUANTITY, REQ_DATE FROM INDENT_DET WHERE IND_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, indno);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					ls_indno = checkNullAndTrim(rs.getString("IND_NO"));
					ll_lineno = rs.getInt("LINE_NO");
					ls_itemcode = checkNullAndTrim(rs.getString("ITEM_CODE"));
					lc_quantity = rs.getInt("QUANTITY");
					ldt_reqdate = rs.getDate("REQ_DATE");

					sql1 = "INSERT INTO INDENTITEM_DET (IND_NO, ITEM_CODE, LINE_NO, QUANTITY, REQ_DATE) VALUES (?, ?, ?, ?, ?)";
					pstmtInsert = conn.prepareStatement(sql1);
					pstmtInsert.setString(1, ls_indno);
					pstmtInsert.setString(2, ls_itemcode);
					pstmtInsert.setInt(3, ll_lineno);
					pstmtInsert.setInt(4, lc_quantity);
					pstmtInsert.setDate(5, ldt_reqdate);
					pstmtInsert.executeUpdate();
					if(pstmtInsert != null)
					{
						pstmtInsert.close();
						pstmtInsert = null;
					}

				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}*/
			}

			/*cnt = 1;
			sql = "SELECT ITEM_CODE, LINE_NO, QUANTITY, REQ_DATE FROM INDENTITEM_DET WHERE IND_NO = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, indno);
			rs = pstmt.executeQuery();
			 */			
			sql1 = "SELECT LINE_NO,ITEM_CODE,REQ_DATE,UNIT,SPECIFIC_INSTR,PRIORITY,APR_CODE,SPECIAL_INSTR,ACCT_CODE, CCTR_CODE,EMP_CODE__IAPR,SUPP_CODE__PREF," +
					"PURC_RATE,PACK_CODE, PUR_LEAD_TIME,PACK_INSTR,SUPP_CODE__MNFR,QUANTITY__STDUOM, UNIT__STD,CONV__QTY_STDUOM," +
					"EMP_CODE__PUR,REMARKS, QUANTITY_ATSITE, AVECONSQTY_ATSITE, REORDERQTY_ATSITE, QUANTITY,EMP_CODE__QCAPRV, " +
					"MAX_RATE,ITEM_CODE__MFG,WORK_ORDER,ANAL_CODE,DIMENSION, NO_ART  FROM INDENT_DET WHERE IND_NO = ?";   //dimension and no_art added by manish mhatre on 16-4-21
			pstmt = conn.prepareStatement(sql1);
			pstmt.setString(1, indno);
			rs = pstmt.executeQuery();
			while(rs.next())
			{

				/*sql1 = "SELECT LINE_NO,ITEM_CODE,REQ_DATE,UNIT,SPECIFIC_INSTR,PRIORITY,APR_CODE,SPECIAL_INSTR,ACCT_CODE, CCTR_CODE,EMP_CODE__IAPR,SUPP_CODE__PREF," +
						"PURC_RATE,PACK_CODE, PUR_LEAD_TIME,PACK_INSTR,SUPP_CODE__MNFR,QUANTITY__STDUOM, UNIT__STD,CONV__QTY_STDUOM," +
						"EMP_CODE__PUR,REMARKS, QUANTITY_ATSITE, AVECONSQTY_ATSITE, REORDERQTY_ATSITE, QUANTITY,EMP_CODE__QCAPRV, " +
						"MAX_RATE,ITEM_CODE__MFG,WORK_ORDER,ANAL_CODE  FROM INDENT_DET WHERE IND_NO = ? AND LINE_NO= ? AND ITEM_CODE= ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, indno);
				pstmt1.setString(1, lineno);
				pstmt1.setString(1, itemcode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{*/
				lineno = rs.getInt("LINE_NO");
				itemcode = checkNullAndTrim(rs.getString("ITEM_CODE"));
				reqdate = rs.getDate("REQ_DATE");
				unit = checkNullAndTrim(rs.getString("UNIT"));
				speinst	 = checkNullAndTrim(rs.getString("SPECIFIC_INSTR"));
				priority = checkNullAndTrim(rs.getString("PRIORITY"));
				apr = checkNullAndTrim(rs.getString("APR_CODE"));
				splinst = checkNullAndTrim(rs.getString("SPECIAL_INSTR"));
				acct = checkNullAndTrim(rs.getString("ACCT_CODE"));
				cctr = checkNullAndTrim(rs.getString("CCTR_CODE"));
				ls_emp = checkNullAndTrim(rs.getString("EMP_CODE__IAPR"));
				ls_supp = checkNullAndTrim(rs.getString("SUPP_CODE__PREF"));
				ld_purcrate = checkNullAndTrim(rs.getString("PURC_RATE"));
				ls_pack = checkNullAndTrim(rs.getString("PACK_CODE"));
				lc_pur_lead = checkNullAndTrim(rs.getString("PUR_LEAD_TIME"));
				ls_pack_instr = checkNullAndTrim(rs.getString("PACK_INSTR"));
				ls_supp_mnfr = checkNullAndTrim(rs.getString("SUPP_CODE__MNFR")); 
				lc_qty_std = checkNullAndTrim(rs.getString("QUANTITY__STDUOM"));
				ls_unit_std = checkNullAndTrim(rs.getString("UNIT__STD"));
				lc_conv = checkNullAndTrim(rs.getString("CONV__QTY_STDUOM"));
				ls_emp_pur = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
				ls_remarks = checkNullAndTrim(rs.getString("REMARKS"));
				ld_quantity_atsite = checkNullAndTrim(rs.getString("QUANTITY_ATSITE"));
				ld_aveconsqty_atsite = checkNullAndTrim(rs.getString("AVECONSQTY_ATSITE"));
				ld_reorderqty_atsite = checkNullAndTrim(rs.getString("REORDERQTY_ATSITE"));
				//Modified by Anjali R. on[24/04/2018][To get quantity in double format][Start]
				//lc_quantity = rs.getInt("QUANTITY");
				quantity = rs.getDouble("QUANTITY");
				//Modified by Anjali R. on[24/04/2018][To get quantity in double format][End]
				ls_emp_code__qcaprv= checkNullAndTrim(rs.getString("EMP_CODE__QCAPRV"));
				ld_max_rate = checkNullAndTrim(rs.getString("MAX_RATE"));
				ls_item_cd_mfg = checkNullAndTrim(rs.getString("ITEM_CODE__MFG"));
				LS_WORK_ORDER = checkNullAndTrim(rs.getString("WORK_ORDER"));
				ls_anal_code = checkNullAndTrim(rs.getString("ANAL_CODE"));
				dimension= rs.getString("DIMENSION");    //added by manish mhatre on 16-4-21
				noArt=rs.getDouble("NO_ART");            //added by manish mhatre on 16-4-21
				System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);  //added by manish mhatre on 16-4-21

				/*}*/
				/*if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}*/
				//Taking qty_stduom from INDENT_DET 
				//				lc_qty_std = (lc_qty_std.length() == 0) ? "0" : lc_qty_std;
				//				lc_qty_stduom = (qty/lc_quantity) * Double.parseDouble(lc_qty_std);

				sql1 = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemcode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					itemdescr = checkNullAndTrim(rs1.getString("DESCR"));
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}

				if(cnt > 100)
				{
					cnt++;
					if(cnt > 10)
					{
						cnt = 1;
						ll_start = ll_start + 1;

						ls_str = ls_ind_seq.substring(ll_start, 1);						
						//ls_str = mid(ls_ind_seq,ll_start,1)

					}					
					String temp = String.valueOf(cnt);
					temp = temp.substring(temp.length() - 1);
					newind = indno.trim() + ls_str.trim() + temp;
					//newind = righttrim(indno) + trim(ls_str)+ right(string(ll_cnt),1)

				}
				else
				{

					String temp = "0000" + cnt;
					temp = temp.substring(temp.length() - 2);
					newind = indno.trim() + temp;
					//newind= righttrim(indno) + right('0000'+string(i),2);

				}
				System.out.println("---------- newind---------------"+newind);

				//Modified by Anjali R. on[25/04/2018][To get todays date][Start]
				today = new java.sql.Timestamp(System.currentTimeMillis());
				java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
				date = simpleDateFormat.parse(today.toString());
				today = java.sql.Timestamp.valueOf(simpleDateFormat.format(date).toString() + " 00:00:00.0");
				//Modified by Anjali R. on[25/04/2018][To get todays date][End]

				sql1 = "INSERT INTO INDENT(IND_NO,IND_DATE,IND_TYPE,DEPT_CODE,REQ_DATE, ITEM_CODE,ITEM_DESCR,QUANTITY,UNIT__IND," +
						"EMP_CODE__REQ,SITE_CODE, WORK_ORDER,APR_CODE,PROJ_CODE,STATUS,STATUS_DATE,SPECIFIC_INSTR,PRIORITY, SPECIAL_INSTR," +
						"ORD_QTY,SITE_CODE__DLV,ACCT_CODE,CCTR_CODE, SUPP_CODE__PREF,PURC_RATE,SITE_CODE__ACCT,PACK_CODE,PUR_LEAD_TIME, " +
						"PACK_INSTR,SUPP_CODE__MNFR,SITE_CODE__BIL,QUANTITY__STDUOM, UNIT__STD,CONV__QTY_STDUOM,EMP_CODE__PUR,EMP_CODE__IAPR," +
						"QUANTITY_ORI,REMARKS, QUANTITY_ATSITE, AVECONSQTY_ATSITE, REORDERQTY_ATSITE, EMP_CODE__QCAPRV, MAX_RATE,ITEM_CODE__MFG," +
						"SITE_CODE__DES,CHG_USER,CHG_DATE,CHG_TERM, REF_NO,ANAL_CODE,TASK_CODE,DIMENSION,NO_ART)" +   //added dimension and no_art column by manish mhatre on 16-4-21
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";   //added two question marks by manish mhatre on 16-4-21
				pstmtInsert = conn.prepareStatement(sql1);
				pstmtInsert.setString(1, newind);
				//Modified by Anjali R. on[25/04/2018][To set sysdate from Database][Start]
				//pstmtInsert.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
				pstmtInsert.setTimestamp(2, today);
				//Modified by Anjali R. on[25/04/2018][To take sysdate from Database][End]
				pstmtInsert.setString(3, indtype);
				pstmtInsert.setString(4, dept);
				pstmtInsert.setDate(5, reqdate);
				pstmtInsert.setString(6, itemcode);
				pstmtInsert.setString(7, itemdescr);
				//Modified by Anjali R. on[24/04/2018][To set quantity in double format][Start]
				//pstmtInsert.setInt(8, lc_quantity);
				pstmtInsert.setDouble(8, quantity);
				//Modified by Anjali R. on[24/04/2018][To set quantity in double format][End]
				pstmtInsert.setString(9, unit);
				pstmtInsert.setString(10, emp);
				pstmtInsert.setString(11, site);
				pstmtInsert.setString(12, work);
				pstmtInsert.setString(13, apr);
				pstmtInsert.setString(14, proj);
				pstmtInsert.setString(15, "U");
				//Modified by Anjali R. on[25/04/2018][To get todays date][Start]
				//pstmtInsert.setDate(16, new java.sql.Date(new java.util.Date().getTime()));
				pstmtInsert.setTimestamp(16, today);
				//Modified by Anjali R. on[25/04/2018][To get todays date][End]
				pstmtInsert.setString(17, speinst);
				pstmtInsert.setString(18, priority);
				pstmtInsert.setString(19, splinst);
				pstmtInsert.setInt(20, 0);
				pstmtInsert.setString(21, sitedlv);
				pstmtInsert.setString(22, acct);
				pstmtInsert.setString(23, cctr);
				pstmtInsert.setString(24, ls_supp);
				pstmtInsert.setString(25, ld_purcrate);
				pstmtInsert.setString(26, ls_siteacct);
				pstmtInsert.setString(27, ls_pack);
				pstmtInsert.setString(28, lc_pur_lead);
				pstmtInsert.setString(29, ls_pack_instr);
				pstmtInsert.setString(30, ls_supp_mnfr);
				pstmtInsert.setString(31, ls_sitebill);
				//pstmtInsert.setDouble(32, lc_qty_stduom);
				pstmtInsert.setDouble(32, (lc_qty_std.length()>0?Double.parseDouble(lc_qty_std):0));
				pstmtInsert.setString(33, ls_unit_std);
				pstmtInsert.setString(34, lc_conv);
				pstmtInsert.setString(35, ls_emp_pur);
				pstmtInsert.setString(36, ls_emp);
				//Modified by Anjali R. on[24/04/2018][To set quantity in double format][Start]
				//pstmtInsert.setInt(37, lc_quantity);
				pstmtInsert.setDouble(37, quantity);
				//Modified by Anjali R. on[24/04/2018][To set quantity in double format][End]
				pstmtInsert.setString(38, ls_remarks);
				pstmtInsert.setString(39, ld_quantity_atsite);
				pstmtInsert.setString(40, ld_aveconsqty_atsite);
				pstmtInsert.setString(41, ld_reorderqty_atsite);
				pstmtInsert.setString(42, ls_emp_code__qcaprv);
				pstmtInsert.setString(43, ld_max_rate);
				pstmtInsert.setString(44, ls_item_cd_mfg);
				pstmtInsert.setString(45, ls_sitedes);
				pstmtInsert.setString(46, userid);
				pstmtInsert.setString(47, ldt_chg_date);
				pstmtInsert.setString(48, termid);
				pstmtInsert.setString(49, ls_ref_no);
				pstmtInsert.setString(50, ls_anal_code);
				pstmtInsert.setString(51, ls_task_code);
				pstmtInsert.setString(52, dimension);  //added by manish mhatre on 16-4-21
				pstmtInsert.setDouble(53, noArt);     //added by manish mhatre on 16-4-21
				pstmtInsert.executeUpdate();
				if(pstmtInsert != null)
				{
					pstmtInsert.close();
					pstmtInsert = null;
				}
				cnt++;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}

			java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
			sql1 = "UPDATE INDENT_HDR SET CONFIRMED = ? , CONF_DATE = ? WHERE IND_NO = ? ";
			pstmtInsert = conn.prepareStatement(sql1);
			pstmtInsert.setString(1, "Y");
			pstmtInsert.setDate(2, currentDate);
			pstmtInsert.setString(3, indno);
			pstmtInsert.executeUpdate();
			if(pstmtInsert != null)
			{
				pstmtInsert.close();
				pstmtInsert = null;
			}

			if(errCode.length() != 0)
			{
				isError = true;
			}
			else if(errCode.length() == 0)
			{
				isError = false;
				errCode = "CONFSUCESS";
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}

			String ediOption = "";
			String dataStr = "";
			sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_indent_req' ";
			pstmt1 = conn.prepareStatement(sql);
			rs1 = pstmt1.executeQuery();
			if ( rs1.next() )
			{
				ediOption = rs1.getString("EDI_OPTION");
				if(ediOption == null)
				{
					ediOption = "";
				}
			}
			if(pstmt1 != null)
			{
				pstmt1.close();
				pstmt1 = null;
			}
			if(rs1 != null)
			{
				rs1.close();
				rs1 = null;
			}

			if (ediOption == null || ediOption.equalsIgnoreCase("0"))
			{

			}
			else if ("2".equals(ediOption)) 
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_indent_req", "tran_id");
				dataStr = createRCPXML.getTranXML(tranId, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_indent_req", "2", xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from edi 2 batchload = [" + retString + "]");
				}
			}
			else 
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_indent_req","tran_id");
				dataStr = createRCPXML.getTranXML(tranId, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_indent_req", ediOption, xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from batchload = ["+ retString + "]");
				}
			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = itmDBAccess.getErrorString( errFldName, errCode, userId ,"",conn);
					errorType =  errorType( conn, errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;

				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}	
			errString = errStringXml.toString();
		}
		catch(Exception e)
		{
			try
			{
				isError = true;
				System.out.println("Exception "+e.getMessage());
				e.printStackTrace();			
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				//isError = true;
				e1.printStackTrace();
				throw new ITMException(e1);
			}
		}
		finally
		{
			try
			{	
				System.out.println("isError in Finally IndentReqConf="+isError);
				if( !isError  )
				{
					System.out.println("----------commmit-----------");
					conn.commit(); 
				}
				else if ( isError )
				{
					System.out.println("--------------rollback------------");
					conn.rollback();
				}

				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if (conn != null)
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
		if(isWorkflow)//Added by AMOL
		{
			return "1";	
		}
		else
		{
			return errString;
		}

	}

	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input.trim();
	}

	private String checkNullAndTrim( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}

	public String errorType( Connection conn, String errorCode ) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =  ? ";
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
		}		
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}	
}


