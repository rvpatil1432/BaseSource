/**
 * PURPOSE : IndentAmendConf component
 * AUTHOR : Manish Mhatre
 * DATE : 12-04-2021
 */

package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.*;
import javax.ejb.Stateless;

import org.w3c.dom.*;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.utility.CommonConstants;
import ibase.webitm.utility.TransIDGenerator;


@Stateless
public class IndentAmendConf extends ActionHandlerEJB implements IndentAmendConfLocal, IndentAmendConfRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	DistCommon discommon = new DistCommon();
	FinCommon finCommon = new FinCommon();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();

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
			System.out.println("Exception in [IndentAmendConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirm(String tranID,String xtraParams, String forcedFlag,String userInfoStr) throws RemoteException,ITMException
	{
		System.out.println("Inside Indent Amendment Confirm ...");
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

			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentAmendConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
		System.out.println(" ========= Inside IndentAmendConf confirm ============= ");
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

		String amdNo="",confirmed="";
		java.sql.Date ldt_reqdate = null, reqdate = null;


		java.sql.Timestamp today = null;
		java.util.Date date = null;

		try
		{
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));

			if ( conn == null )
			{
				conn = getConnection();
				conn.setAutoCommit(false);
				isError = true;
			}

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currDate = new Date();
			amdNo = tranId;

			sql="Select confirmed from indent_amd where amd_no= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed = checkNullAndTrim(rs.getString("CONFIRMED"));
			}
			else
			{
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


			if(!"Y".equalsIgnoreCase(confirmed))
			{
				errString = confirmIndentAmd(amdNo,xtraParams,conn);
			}
			else
			{
				errString = itmDBAccess.getErrorString("","VTIACONF1","","",conn);
				return errString;
			}

			System.out.println("errString[" + errString +"]");
			if (errString != null && errString.trim().length() > 0)
			{
				conn.rollback();
				return errString;
			} 
			else 
			{
				conn.commit();
				errString = itmDBAccess.getErrorString("","CONFSUCESS","","",conn);
				//return errString;
			}

		}
		catch(Exception e)
		{
			if(conn!=null)
			{
				try 
				{
					conn.rollback();
				}
				catch (SQLException ex) 
				{
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		}

		finally
		{
			try
			{
				System.out.println( ">>>>>>>>>>>>>In finaly errString:"+errString);
				if (errString != null && errString.trim().length() > 0 ) 
				{
					if (isError)
					{
						conn.rollback();
						conn.close();
						conn = null;
					}
					System.out.println("Transaction rollback... ");
				}
				else
				{	
					if (isError)
					{
						conn.commit();
						System.out.println("@@@@ Transaction commit... ");
						conn.close();
						conn = null;
					}
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}

	public String confirmIndentAmd(String amdNo, String xtraParams, Connection conn) throws Exception
	{
		System.out.println("----------- Inside confirmIndentAmd ----------------");

		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		String sql="";

		InvDemSuppTraceBean invDemSupTrcBean = null;
		boolean isError = false;
		boolean ordFlag = true;
		int updCnt=0,updCnt1=0;
		String errString = "";
		String empCodeAprv="",empReq="",projCode="",priority="",siteDlv="",acctCode="",cctrCode="";
		String siteCodeAcct="",suppCodePref="",specificInstr="",specialInstr="",conf="",indNo="";
		String ediOption="",item="",site="",empCodePur="",stopBusi="",packInstr="",analCode="",temp="";

		double qty=0, purcrate=0, qtyStduom=0, maxRate=0, poQty=0, quantity0;
		String indType="",ls_type_allow_projbudgt_list="",oldIndNo="",indentNoT="";
		String [] ls_type_allow_projbudgt = null;

		String empCodeReq="",workOrder="",siteCodeDlv="",itemCode="",siteCode="",dimension="";
		double purcRate=0,noArt=0;

		double approxCost=0, indAmount=0, poAmt=0, totPoAmt=0,	currentAmount=0,amtProj=0,exceedAmt=0 ,poAmt1=0 ,poRcpAmt=0 ,poRetAmt=0,totPoAmt11=0,poAmt11=0,poRcpAmt11=0,poRetAmt11=0,poAmt12=0,totAmtProj=0,indentAmt=0;

		Timestamp sysDate = null,amdDate=null,reqDate=null,aprDate=null;

		double quantitylc=0;
		String itemCodels="",loginUsr ="",termId="";
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			Calendar currentDate = Calendar.getInstance();

			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr + "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			loginUsr = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");

			invDemSupTrcBean = new InvDemSuppTraceBean();
			HashMap demandSupplyMap = new HashMap();

			//gbf_update_budget_indent_amd this nvo method do not migrate because it is not update budget in indent approval so as per said SM sir there are no migrated
			/*ls_errcode = lnvo_budget.gbf_update_budget_indent_amd(as_tranid)
			  if len(trim(ls_errcode)) > 0 then
			  if isvalid(lnvo_budget) then destroy lnvo_budget
	          rollback;
	          return ls_errcode
   			  end if
			 */

			sql=  " Select ind_no, amd_date, req_date, quantity, emp_code__aprv, apr_date,	emp_code__req, work_order, "
					+ " proj_code, priority, site_code__dlv, acct_code, cctr_code, site_code__acct, supp_code__pref, purc_rate, "   
					+ " specific_instr, special_instr, quantity__stduom, max_rate, emp_code__pur, item_code, site_code,	pack_instr,anal_code, ind_type ,"
					+ " dimension,no_art "    //added dimension and no_Art field because when change the value then getting value
					+ " from indent_amd where amd_no = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				indNo=rs.getString("ind_no");
				amdDate=rs.getTimestamp("amd_date");
				reqDate=rs.getTimestamp("req_date");
				qty=rs.getDouble("quantity");
				empCodeAprv=rs.getString("emp_code__aprv");
				aprDate=rs.getTimestamp("apr_date");
				empCodeReq=rs.getString("emp_code__req");
				workOrder=rs.getString("work_order");
				projCode=rs.getString("proj_code");
				priority=rs.getString("priority");
				siteCodeDlv=rs.getString("site_code__dlv");
				acctCode=rs.getString("acct_code");
				cctrCode=rs.getString("cctr_code");
				siteCodeAcct=rs.getString("site_code__acct");
				suppCodePref=rs.getString("supp_code__pref");
				purcRate=rs.getDouble("purc_rate");
				specificInstr=rs.getString("specific_instr");
				specialInstr=rs.getString("special_instr");
				qtyStduom=rs.getDouble("quantity__stduom");
				maxRate=rs.getDouble("max_rate");
				empCodePur=rs.getString("emp_code__pur");
				itemCode=rs.getString("item_code");
				siteCode=rs.getString("site_code");
				packInstr=rs.getString("pack_instr");
				analCode=rs.getString("anal_code");
				indType=rs.getString("ind_type");
				dimension=rs.getString("dimension");
				noArt=rs.getDouble("no_art");
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

			ls_type_allow_projbudgt_list = checkNullAndTrim(discommon.getDisparams("999999","TYPE_ALLOW_PROJBUDGET",conn));
			if("NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
			{
				ls_type_allow_projbudgt_list = "";
			}
			System.out.println("ls_type_allow_projbudgt_list ==================>>["+ls_type_allow_projbudgt_list+"]");

			ordFlag = false;
			if(ls_type_allow_projbudgt_list.length() > 0)
			{
				ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");

				for(String str : ls_type_allow_projbudgt)
				{
					if(indType.trim().equalsIgnoreCase(str.trim()))  //15-4-21
					{
						ordFlag = true;
					}
				}
			}

			System.out.println("ordFlag in confirm ==================>>["+ordFlag+"]");

			if(ordFlag)
			{
				sql = "select approx_cost from project where proj_code= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, projCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					approxCost = rs.getDouble("approx_cost");
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

				sql = "select sum((case when quantity is null then 0 else quantity end) * (case when max_rate is null then 0 else max_rate end)) as indent_amt " +
						"from indent where proj_code= ?  and status in ('A','O') and ind_no <> ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, projCode);
				pstmt.setString(2, indNo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					indentAmt = rs.getDouble("indent_amt");
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

				sql = "select ind_no from indent where proj_code= ?  and status in ('L','C') and ord_qty <> 0 ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, projCode);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					indentNoT=rs1.getString("ind_no");


					sql = "select sum(a.tot_amt * b.exch_rate) as po_amt1 from porddet a,porder b " +
							"where (a.purc_order=b.purc_order) " +
							"and b.confirmed='Y' " +
							"and a.proj_code= ? " +
							"and a.ind_no= ? " +
							"and b.status <> 'X' " +
							"and a.status <> 'C' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, projCode);
					pstmt.setString(2, indentNoT);
					rs = pstmt.executeQuery();
					if(rs.next())    //changed on 15-4-21
					{
						poAmt1=rs.getDouble("po_amt1");
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

					sql = "select sum(a.net_amt  * b.exch_rate) as porcp_amt from porcpdet a, porcp b ,porddet c " +
							"where (a.purc_order=c.purc_order) " +
							"and (a.tran_id = b.tran_id ) " +
							"and (a.line_no__ord = c.line_no ) " +
							"and b.confirmed= 'Y' " +
							"and c.proj_code= ? " +
							"and c.ind_no= ? " +
							"and b.status <> 'X' " +
							"and c.status='C' " +
							"and b.tran_ser= 'P-RCP' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, projCode);
					pstmt.setString(2, indentNoT);
					rs = pstmt.executeQuery();
					if(rs.next())    //changed on 15-4-21
					{
						poRcpAmt=rs.getDouble("porcp_amt");
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

					sql = "select sum(a.net_amt  * b.exch_rate) as poret_amt from porcpdet a, porcp b ,porddet c " +
							"where (a.purc_order=c.purc_order) " +
							"and (a.tran_id = b.tran_id ) " +
							"and (a.line_no__ord = c.line_no ) " +
							"and b.confirmed= 'Y' " +
							"and c.proj_code= ? " +
							"and c.ind_no= ? " +
							"and b.status <> 'X' " +
							"and c.status='C' " +
							"and b.tran_ser= 'P-RET' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, projCode);
					pstmt.setString(2, indentNoT);
					rs = pstmt.executeQuery();
					if(rs.next())  //changed on 15-4-21
					{
						poRetAmt=rs.getDouble("poret_amt");
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

					totPoAmt = totPoAmt + poAmt1 + poRcpAmt - poRetAmt;

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

				totPoAmt11=totPoAmt;

				sql = "select sum(a.tot_amt * b.exch_rate) as po_amt from porddet a,porder b " +
						"where (a.purc_order=b.purc_order) " +
						"and b.confirmed='Y' " +
						"and a.proj_code= ? " +
						"and a.ind_no is null " +
						"and b.status <> 'X' " +
						"and a.status <> 'C' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, projCode);
				rs = pstmt.executeQuery();
				if(rs.next())     //changed on 15-4-21
				{
					poAmt12=rs.getDouble("po_amt");
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

				sql = "select sum(a.net_amt  * b.exch_rate) as porcp_amt from porcpdet a, porcp b ,porddet c " +
						"where (a.purc_order=c.purc_order) " +
						"and (a.tran_id = b.tran_id ) " +
						"and (a.line_no__ord = c.line_no ) " +
						"and b.confirmed= 'Y' " +
						"and c.proj_code= ? " +
						"and c.ind_no is null " +
						"and b.status <> 'X' " +
						"and c.status='C' " +
						"and b.tran_ser= 'P-RCP' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, projCode);
				rs = pstmt.executeQuery();
				if(rs.next())   //changed on 15-4-21
				{
					poRcpAmt11=rs.getDouble("porcp_amt");
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

				sql = "select sum(a.net_amt  * b.exch_rate) as poret_amt from porcpdet a, porcp b ,porddet c " +
						"where (a.purc_order=c.purc_order) " +
						"and (a.tran_id = b.tran_id ) " +
						"and (a.line_no__ord = c.line_no ) " +
						"and b.confirmed= 'Y' " +
						"and c.proj_code= ? " +
						"and c.ind_no is null " +
						"and b.status <> 'X' " +
						"and c.status='C' " +
						"and b.tran_ser= 'P-RET' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, projCode);
				rs = pstmt.executeQuery();
				if(rs.next())      //changed on 15-4-21
				{
					poRetAmt11=rs.getDouble("poret_amt");
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

				/*ls_temp = 'lc_tot_poamt11 without null'+string(lc_tot_poamt11)
						select :ls_temp into :ls_temp from dual;*/

				totPoAmt = totPoAmt11 + poAmt12 + poRcpAmt11 - poRetAmt11;

				/*ls_temp = 'lc_tot_poamt total amount'+string(lc_tot_poamt)
					select :ls_temp into :ls_temp from dual;*/


				indentAmt= indentAmt+ totPoAmt;

				sql = "select sum((case when quantity is null then 0 else quantity end) * (case when max_rate is null then 0 else max_rate end)) as current_amount " +
						"from indent_amd " +  
						"where proj_code= ? " +
						"and amd_no= ? " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, projCode);
				pstmt.setString(2,amdNo);
				rs = pstmt.executeQuery();
				if(rs.next())     //changed on 15-4-21
				{
					currentAmount=rs.getDouble("current_amount");
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


				totAmtProj = indentAmt + currentAmount;

				exceedAmt = totAmtProj - approxCost;

				if (totAmtProj > approxCost)
				{ 	
					errString = itmDBAccess.getErrorString("","VTPROJCNF","","",conn);
					return errString;
				}  
			}


			sql = "Select quantity,item_code from indent where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, indNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				quantitylc=rs.getDouble("quantity");
				itemCodels=rs.getString("item_code");
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


			sql = " update indent set amd_no = ?, amd_date = ?, req_date = ?, quantity = ?, emp_code__aprv = ?, emp_code__req = ?, "
					+" work_order = ?, proj_code = ?, priority = ?, site_code__dlv = ?, acct_code= ?, cctr_code = ?, site_code__acct = ?, "	
					+" supp_code__pref = ?, purc_rate = ?, specific_instr = ?, special_instr = ?, quantity__stduom = ?, max_rate = ?, emp_code__pur = ?, "
					+" pack_instr = ?, anal_code = ?, dimension = ?, no_art = ? where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,amdNo);
			pstmt.setTimestamp(2,amdDate);
			pstmt.setTimestamp(3,reqDate);
			pstmt.setDouble(4,qty);
			pstmt.setString(5,empCodeAprv);
			pstmt.setString(6,empCodeReq);
			pstmt.setString(7,workOrder);
			pstmt.setString(8,projCode);
			pstmt.setString(9,priority);
			pstmt.setString(10,siteCodeDlv);
			pstmt.setString(11,acctCode);
			pstmt.setString(12,cctrCode);
			pstmt.setString(13,siteCodeAcct);
			pstmt.setString(14,suppCodePref);
			pstmt.setDouble(15,purcRate);
			pstmt.setString(16,specificInstr);
			pstmt.setString(17,specialInstr);
			pstmt.setDouble(18,qtyStduom);
			pstmt.setDouble(19,maxRate);
			pstmt.setString(20,empCodePur);
			pstmt.setString(21,packInstr);
			pstmt.setString(22,analCode);
			pstmt.setString(23,dimension);   //added dimension when confirm indent amd then update the dimension in indent table
			pstmt.setDouble(24,noArt);       //added no_art when confirm indent amd then update the no_art in indent table
			pstmt.setString(25,indNo);

			updCnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;

			System.out.println("updcnt in confirmindentamd to update in indent table>>"+updCnt);

			if(updCnt>0)
			{
				sql = " update indent_amd set confirmed= 'Y', conf_date = ? where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,sysDate);
				pstmt.setString(2,amdNo);

				updCnt1 = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;

				System.out.println("updcnt1 in confirmindentamd to update in indent_amd table>>"+updCnt1);
			}

			demandSupplyMap.put("item_code", itemCodels);
			demandSupplyMap.put("site_code", siteCodeDlv);		
			demandSupplyMap.put("ref_ser", "IND");
			demandSupplyMap.put("ref_id", indNo);
			demandSupplyMap.put("ref_line", "NA");	
			demandSupplyMap.put("demand_qty", 0.0);
			demandSupplyMap.put("supply_qty", (qty - quantitylc));
			demandSupplyMap.put("change_type", "C");
			demandSupplyMap.put("chg_process", "T");
			demandSupplyMap.put("due_date", reqDate);
			demandSupplyMap.put("chg_user", loginUsr);
			demandSupplyMap.put("chg_term", termId);

			errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
			demandSupplyMap.clear();
			if(errString != null && errString.trim().length() > 0)
			{
				System.out.println("errString["+errString+"]");
				return errString;
			}


			String dataStr = "";
			sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_indentamend' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				ediOption = rs.getString("EDI_OPTION");
				if(ediOption == null)
				{
					ediOption = "";
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
			}

			if (ediOption == null || "0".equalsIgnoreCase(ediOption))
			{

			}
			else if ("2".equalsIgnoreCase(ediOption)) 
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_indentamend", "tran_id");
				dataStr = createRCPXML.getTranXML(amdNo, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_indentamend", "2", xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from edi 2 batchload = [" + retString + "]");
				}
			}
			else 
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_indentamend","tran_id");
				dataStr = createRCPXML.getTranXML(amdNo, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_indentamend", ediOption, xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from batchload = ["+ retString + "]");
				}
			}

			/*if len(ls_errcode) = 0 then
			  if ld_qty = 0 then		
			  ls_errcode = gbf_retrieve_indent_close(ls_indno,'')
			  end if
			  end if*/
			//above pb code as follows as java
			if(qty == 0)
			{
				errString = retrieveIndentClose(indNo,"",xtraParams,conn);
				if(errString != null && errString.trim().length() > 0)
				{
					System.out.println("errString["+errString+"]");
					return errString;
				}
			}

		}

		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);

		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("return errString in confirmindentamd=========>> "+errString);
		return errString;
	}


	public String retrieveIndentClose(String indentNo, String tranId,String xtraParams, Connection conn) throws Exception
	{
		System.out.println("----------- Inside retrieveIndentClose ----------------");

		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		String errString = "",sql="";
		HashMap demandSupplyMap = null;
		InvDemSuppTraceBean invDemSupTrcBean = null;
		boolean lb_ord_flag = false;
		boolean ordFlag = true;
		Timestamp today = null,currDate=null,sysDate=null;
		Date date = null;
		int updCnt1=0,updCnt=0;
		String keyString="",tranIdCol="",userId="",loginSite="",siteCode="",termId="",empCode="",siteCodeDes="";
		String xmlValues = "";
		boolean isError = false;

		double quantity=0,ordQty=0,purcRate=0,purLeadTime=0,quantityStduom=0,convQtyStduom=0,quantityOri=0,quantityAtSite=0,aveconsqtyAtSite=0,reorderqtyAtSite=0,maxRate=0;

		String indDate="",indType="",deptCode="",reqDate="",itemCode="",itemDescr="",unitInd="",unitOrd="",empCodeReq="";
		String siteCodeDlv="",siteCodeAcct="",workOrder="",aprCode="",aprUser="",aprDate="",empCodeAprv="";
		String projCode="",priority="",acctCode="",cctrCode="",empCodePur="",suppCodePref="",amdNo="",amdDate="",packCode="";
		String status="",statusDate="",specificInstr="",specialInstr="",remarks="",chgDate="",chgUser="",chgTerm="",packInstr="";
		String suppCodeMnfr="",mrpRunId="",siteCodeBil="",unitStd="",empCodeIapr="",empCodeQcAprv="",roleCode="",reason="";
		String itemCodeMfg="",planDueDate="",parentTranId="",revTran="",analCode="",refNo="",taskCode="";
		String addDate="",addUser="",addTerm="";

		String updateCloseIndent="";
		String tranDate="",siteCodeIndClose="",indentNoIndClose="",statusIndClose="",empCodeAprvIndClose="",chgDateIndClose="",chgTermIndClose="",chgUserIndClose="";
		String siteCodeOriIndClose="",siteCodeDesIndClose="",siteCodeDlvIndClose="",addDateIndClose="",addUserIndClose="",addTermIndClose="";

		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );

			Calendar currentDate = Calendar.getInstance();
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr+ "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("Now the sysDate is :=>  " + sysDate + "]");

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdf.format(currDate);

			System.out.println("Now the date is currDateStr :=>  " + currDateStr + "]");

			if(tranId==null || tranId.trim().length()==0)
			{
				updateCloseIndent="T";    //Closing indent from indent amendment entry on confirm, for amended qty = 0.
			}
			else
			{
				updateCloseIndent="T";    //Closing indent from close indent entry.
			}

			if("T".equalsIgnoreCase(updateCloseIndent))
			{

				sql = " SELECT (CASE WHEN KEY_STRING IS NULL THEN '' ELSE KEY_STRING END) AS KEY_STRING, "
						+" (CASE WHEN TRAN_ID_COL IS NULL THEN '' ELSE TRAN_ID_COL END) AS TRAN_ID_COL FROM TRANSETUP WHERE TRAN_WINDOW = 'w_indent_close' ";

				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					keyString=checkNullAndTrim(rs.getString("KEY_STRING"));
					tranIdCol=checkNullAndTrim(rs.getString("TRAN_ID_COL"));
				}
				else
				{
					errString = itmDBAccess.getErrorString("","VTTRSET","","",conn);
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

				sql = " Select site_code, site_code__des from indent where ind_no= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,indentNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					siteCode=rs.getString("site_code");
					siteCodeDes=rs.getString("site_code__des");
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

				System.out.println("keyString :" + keyString);
				System.out.println("tranIdCol :" + tranIdCol);

				xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues + "<tran_id></tran_id>";
				xmlValues = xmlValues + "<tran_date>" + currDateStr + "</tran_date>";
				xmlValues = xmlValues + "<site_code>" + loginSite + "</site_code>";
				xmlValues = xmlValues + "<ind_no>" + indentNo + "</ind_no>";
				xmlValues = xmlValues + "<emp_code__aprv>" + empCode + "</emp_code__aprv>";  
				xmlValues = xmlValues + "<status>" + "C" + "</status>";
				xmlValues = xmlValues + "<chg_date>" + currDateStr + "</chg_date>";
				xmlValues = xmlValues + "<chg_term>" + termId + "</chg_term>";
				xmlValues = xmlValues + "<chg_user>" + userId + "</chg_user>";
				xmlValues = xmlValues + "<site_code__ori>" + siteCode + "</site_code__ori>";
				xmlValues = xmlValues + "<site_code__des>" + siteCodeDes + "</site_code__des>";
				xmlValues = xmlValues + "</Detail1></Root>";
				System.out.println("xmlValues :[" + xmlValues + "]");
				TransIDGenerator generatedTranid = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				tranId = generatedTranid.generateTranSeqID("C-IND", tranIdCol, keyString, conn);

				System.out.println("tranId :" + tranId);

				if ("ERROR".equalsIgnoreCase(tranId))
				{
					errString = itmDBAccess.getErrorString("","VTTRANID","","",conn);
					return errString;
				}

			}

			String dbName = (CommonConstants.DB_NAME).trim();
			System.out.println("dbName--["+dbName+"]");

			/*If gs_database = 'db2' Then
					ls_sql = "Select * From indent where ind_no ='" + as_ind_no + "' " + " For Update with RS"
				elseif gs_database = 'mysql' then
					ls_sql = "Select * From indent where ind_no ='" + as_ind_no + "' " + " For Update"	
				elseif gs_database = 'mssql' then
					ls_sql = "Select * From indent (updlock) where ind_no ='" + as_ind_no + "' "
				Else
					ls_sql = "Select * From indent Where ind_no ='" + as_ind_no + "' " + " For Update nowait "
				End If

				Execute Immediate :ls_sql Using SQLCA;
				If get_sqlcode() < 0 Then
					ls_errcode = 'DS000' + string(SQLCA.sqldbcode) 
					GoTo quit
				End If*/

			//above pb code as follows in java
			//sql = "Select * From indent where ind_no = ? ";
			sql= " select ind_no,ind_date,ind_type,dept_code,req_date,item_code,item_descr, "
					+" quantity,unit__ind,ord_qty,unit__ord,emp_code__req,site_code,site_code__dlv, "
					+" site_code__acct,work_order,apr_code,apr_user,apr_date,emp_code__aprv,proj_code, "
					+" priority,acct_code,cctr_code,emp_code__pur,supp_code__pref,purc_rate,amd_no,amd_date, "
					+" pack_code,status,status_date,specific_instr,special_instr,remarks,chg_date,chg_user,chg_term, "
					+" pur_lead_time,pack_instr,supp_code__mnfr,mrp_run_id,site_code__bil,quantity__stduom,unit__std, "
					+" conv__qty_stduom,emp_code__iapr,quantity_ori,quantity_atsite,aveconsqty_atsite,reorderqty_atsite, "
					+" emp_code__qcaprv,max_rate,role_code,reason,item_code__mfg,plan_due_date,site_code__des,parent__tran_id, "
					+" rev__tran,anal_code,ref_no,task_code,add_date,add_user,add_term from indent where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, indentNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				indentNo=rs.getString("ind_no");
				indDate=rs.getString("ind_date");
				indType=rs.getString("ind_type");
				deptCode=rs.getString("dept_code");
				reqDate=rs.getString("req_date");
				itemCode=rs.getString("item_code");
				itemDescr=rs.getString("item_descr");
				quantity=rs.getDouble("quantity");
				unitInd=rs.getString("unit__ind");
				ordQty=rs.getDouble("ord_qty");
				unitOrd=rs.getString("unit__ord");
				empCodeReq=rs.getString("emp_code__req");
				siteCode=rs.getString("site_code");
				siteCodeDlv=rs.getString("site_code__dlv");
				siteCodeAcct=rs.getString("site_code__acct");
				workOrder=rs.getString("work_order");
				aprCode=rs.getString("apr_code");
				aprUser=rs.getString("apr_user");
				aprDate=rs.getString("apr_date");
				empCodeAprv=rs.getString("emp_code__aprv");
				projCode=rs.getString("proj_code");
				priority=rs.getString("priority");
				acctCode=rs.getString("acct_code");
				cctrCode=rs.getString("cctr_code");
				empCodePur=rs.getString("emp_code__pur");
				suppCodePref=rs.getString("supp_code__pref");
				purcRate=rs.getDouble("purc_rate");
				amdNo=rs.getString("amd_no");
				amdDate=rs.getString("amd_date");
				packCode=rs.getString("pack_code");
				status=rs.getString("status");
				statusDate=rs.getString("status_date");
				specificInstr=rs.getString("specific_instr");
				specialInstr=rs.getString("special_instr");
				remarks=rs.getString("remarks");
				chgDate=rs.getString("chg_date");
				chgUser=rs.getString("chg_user");
				chgTerm=rs.getString("chg_term");
				purLeadTime=rs.getDouble("pur_lead_time");
				packInstr=rs.getString("pack_instr");
				suppCodeMnfr=rs.getString("supp_code__mnfr");
				mrpRunId=rs.getString("mrp_run_id");
				siteCodeBil=rs.getString("site_code__bil");
				quantityStduom=rs.getDouble("quantity__stduom");
				unitStd=rs.getString("unit__std");
				convQtyStduom=rs.getDouble("conv__qty_stduom");
				empCodeIapr=rs.getString("emp_code__iapr");
				quantityOri=rs.getDouble("quantity_ori");
				quantityAtSite=rs.getDouble("quantity_atsite");
				aveconsqtyAtSite=rs.getDouble("aveconsqty_atsite");
				reorderqtyAtSite=rs.getDouble("reorderqty_atsite");
				empCodeQcAprv=rs.getString("emp_code__qcaprv");
				maxRate=rs.getDouble("max_rate");
				roleCode=rs.getString("role_code");
				reason=rs.getString("reason");
				itemCodeMfg=rs.getString("item_code__mfg");
				planDueDate=rs.getString("plan_due_date");
				siteCodeDes=rs.getString("site_code__des");
				parentTranId=rs.getString("parent__tran_id");
				revTran=rs.getString("rev__tran");
				analCode=rs.getString("anal_code");
				refNo=rs.getString("ref_no");
				taskCode=rs.getString("task_code");
				addDate=rs.getString("add_date");
				addUser=rs.getString("add_user");
				addTerm=rs.getString("add_term");

			}

			if(rs != null)
			{
				rs.close();
				rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			/*If IsNull(ls_errcode) or len(trim(ls_errcode)) = 0  Then	

					// lock the record in Indent_close
					If gs_database = 'db2' Then
						ls_sql = "Select * From indent_close where tran_id ='" + as_tran_id + "' " + " For Update with RS"
					elseif gs_database = 'mysql' then
						ls_sql = "Select * From indent_close where tran_id ='" + as_tran_id + "' " + " For Update"		
					elseif gs_database = 'mssql' then
						ls_sql = "Select * From indent_close (updlock) where tran_id ='" + as_tran_id + "' "

					Else
						ls_sql = "Select * From indent_close Where tran_id ='" + as_tran_id + "' " + " For Update nowait "
					End If

					Execute Immediate :ls_sql Using SQLCA;
					If get_sqlcode() < 0 Then
						ls_errcode = 'DS000' + string(SQLCA.sqldbcode) 
						GoTo quit
					End If*/	


			//above pb code as follows in java
			//sql="Select * from indent_close where tran_id= ? ";
			sql= " select tran_date,site_code,ind_no,status,emp_code__aprv,chg_date,chg_term,"
					+" chg_user,site_code__ori,site_code__des,site_code__dlv,add_date,add_user,add_term from indent_close where tran_id= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranDate=rs.getString("tran_date");
				siteCodeIndClose=rs.getString("site_code");
				indentNoIndClose=rs.getString("ind_no");
				statusIndClose=rs.getString("status");
				empCodeAprvIndClose=rs.getString("emp_code__aprv");
				chgDateIndClose=rs.getString("chg_date");
				chgTermIndClose=rs.getString("chg_term");
				chgUserIndClose=rs.getString("chg_user");
				siteCodeOriIndClose=rs.getString("site_code__ori");
				siteCodeDesIndClose=rs.getString("site_code__des");
				siteCodeDlvIndClose=rs.getString("site_code__dlv");
				addDateIndClose=rs.getString("add_date");
				addUserIndClose=rs.getString("add_user");
				addTermIndClose=rs.getString("add_term");

			}

			if(rs != null)
			{
				rs.close();
				rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}


			//Calling function to close the Indent.
			/*ls_errcode = gbf_confirm_indent_close(as_ind_no);	
			If get_sqlcode() < 0 Then
				GoTo quit
			End If*/
			errString = confirmIndentClose(indentNo,xtraParams,conn);
			if(errString != null && errString.trim().length() > 0)
			{
				System.out.println("errString["+errString+"]");
				return errString;
			}


			sql = " update indent_close set status= 'C', emp_code__aprv = ?, chg_date = ?, chg_user = ? ,chg_term = ?  where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,empCode);
			pstmt.setTimestamp(2,sysDate);
			pstmt.setString(3,userId);
			pstmt.setString(4,termId);
			pstmt.setString(5,tranId);

			updCnt1 = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;

			System.out.println("update statement count for indent_close table>>"+updCnt1);

			if(errString!=null && errString.trim().length() > 0)
			{
				System.out.println("errString["+errString+"]");
				return errString;
			}



			String ediOption = "";
			String dataStr = "";
			sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_indentamend' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				ediOption = rs.getString("EDI_OPTION");
				if(ediOption == null)
				{
					ediOption = "";
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
			}

			/*nvo_aed_functions_adv nvo_functions_adv
			if itm_structure.adv_comp > 0 then 
				itm_app_server[itm_structure.adv_comp].createinstance(nvo_functions_adv)
			else	
				nvo_functions_adv = create nvo_aed_functions_adv
			end if*/
			//please suggest what will do for above pb code

			if (ediOption == null || "0".equalsIgnoreCase(ediOption))
			{

			}
			else if ("2".equalsIgnoreCase(ediOption)) 
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_indent_close", "tran_id");
				dataStr = createRCPXML.getTranXML(indentNo, conn);
				//dataStr = createRCPXML.getTranXML(tranId, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_indent_close", "2", xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from edi 2 batchload in retrieveIndentClose = [" + retString + "]");
				}
			}
			else 
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_indent_close","tran_id");
				dataStr = createRCPXML.getTranXML(indentNo, conn);
				//dataStr = createRCPXML.getTranXML(tranId, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,"w_indent_close", ediOption, xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from batchload in retrieveIndentClose = ["+ retString + "]");
				}
			}


		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);

		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}


		System.out.println("return errString retrieveindent close =========>> "+errString);
		return errString;
	}

	public String confirmIndentClose(String indentNo,String xtraParams, Connection conn) throws Exception
	{
		System.out.println("----------- Inside confirmIndentClose ----------------");

		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		String errString = "",sql="";
		HashMap demandSupplyMap = null;
		InvDemSuppTraceBean invDemSupTrcBean = null;
		boolean lb_ord_flag = false;
		boolean ordFlag = true;
		//int updCnt=0,updCnt1=0;
		boolean isError = false;
		Timestamp today = null;
		Date date = null;
		int updCnt1=0,updCnt=0;
		String keyString="",tranIdCol="",userId="",loginSite="",siteCode="",termId="",empCode="",siteCodeDes="";
		String xmlValues = "";
		String eCode="",itemCode="",siteCodeDlv="";
		double quantity=0,ordQuantity=0;
		Timestamp reqDate=null,sysDate=null;
		String indentNoEnq="",enquiryNo="",status="",statushdr="";
		int ll_i=0;


		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			invDemSupTrcBean = new InvDemSuppTraceBean();

			Calendar currentDate = Calendar.getInstance();
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr + "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("Now the sysDate is :=>  " + sysDate + "]");

			demandSupplyMap = new HashMap();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );


			/*nvo_business_object_budget lnvo_budget
			 if itm_structure.fin_comp > 0 then 
			 itm_app_server[itm_structure.fin_comp].createinstance(lnvo_budget)
			 else	
			 lnvo_budget = create nvo_business_object_budget
			 end if
			 */
			//please suggest what will do for above pb code

			sql = "select emp_code from users where code =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,userId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				eCode=rs.getString("emp_code");
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

			sql = " update indent set status= 'C', status_date= ?, chg_user = ?,  chg_date = ?  where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,sysDate);
			pstmt.setString(2,userId);
			pstmt.setTimestamp(3,sysDate);
			pstmt.setString(4,indentNo);

			updCnt1 = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;

			System.out.println("update statement count in indent table in confirm indnet close>>"+updCnt1);


			sql = " select item_code,site_code__dlv,quantity,ord_qty,req_date "
					+" from indent where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,indentNo);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				itemCode=rs.getString("item_code");
				siteCodeDlv=rs.getString("site_code__dlv");
				quantity=rs.getDouble("quantity");
				ordQuantity=rs.getDouble("ord_qty");
				reqDate=rs.getTimestamp("req_date");
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

			demandSupplyMap.put("item_code", itemCode);
			demandSupplyMap.put("site_code", siteCodeDlv);		
			demandSupplyMap.put("ref_ser", "IND");
			demandSupplyMap.put("ref_id", indentNo);
			demandSupplyMap.put("ref_line", "NA");	
			demandSupplyMap.put("demand_qty", 0.0);
			demandSupplyMap.put("supply_qty", (ordQuantity - quantity));
			demandSupplyMap.put("change_type", "C");
			demandSupplyMap.put("chg_process", "T");
			demandSupplyMap.put("due_date", reqDate);
			demandSupplyMap.put("chg_user", userId);
			demandSupplyMap.put("chg_term", termId);

			errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
			demandSupplyMap.clear();

			if(errString != null && errString.trim().length() > 0)
			{
				System.out.println("errString["+errString+"]");
				return errString;
			}

			sql = " select enq_no from enq_det where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,indentNo);
			rs = pstmt.executeQuery();
			while ( rs.next() )
			{
				enquiryNo=rs.getString("enq_no");
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

			////Updating status in enq_det
			sql = " select enq_det.ind_no from enq_det where enq_det.enq_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,enquiryNo);
			rs = pstmt.executeQuery();
			while ( rs.next() )
			{
				//ll_i=ll_i+1;
				indentNoEnq=rs.getString("ind_no");


				sql = " select status from indent where ind_no = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,indentNoEnq);
				rs1 = pstmt1.executeQuery();
				if ( rs1.next() )
				{
					status=rs1.getString("status");
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

				if("C".equalsIgnoreCase(status))
				{
					sql = " update enq_det set status='C' where enq_no = ? and ind_no = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,enquiryNo);
					pstmt1.setString(2,indentNoEnq);

					updCnt1 = pstmt.executeUpdate();
					pstmt1.close();
					pstmt1 = null;

					System.out.println("update statement count in enq_det table in confirm indnet close>>"+updCnt1);

					/*if(updCnt>0)
					{
						if mod(ll_i,100) =0 then
									goto EndScript
								end if
					}*/
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
			}


			//Updating status in enq_hdr
			sql = "select status from enq_det where enq_no =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,enquiryNo);
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				statushdr=rs.getString("status");

				if(!"C".equalsIgnoreCase(statushdr))
				{
					ordFlag=false;
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
			}

			//If status for all enq_det records is 'C' then only make enq_hdr status = 'C'.
			if(ordFlag)
			{
				sql = " update enq_hdr set status='C' where enq_no = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,enquiryNo);
				updCnt1 = pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1 = null;

				System.out.println("update statement count in enq_hdr table in confirm indnet close>>"+updCnt1);
			}


			errString = updateIndentClose(indentNo,xtraParams,conn);

			if(errString != null && errString.trim().length() > 0)
			{
				System.out.println("errString["+errString+"]");
				return errString;
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);

		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		System.out.println("return errString confirm indent close=========>> "+errString);
		return errString;
	}


	public String updateIndentClose(String indentNo,String xtraParams, Connection conn) throws Exception
	{
		System.out.println("----------- Inside updateIndentClose ----------------");

		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		String errString = "",sql="";
		HashMap demandSupplyMap = null;
		InvDemSuppTraceBean invDemSupTrcBean = null;
		boolean lb_ord_flag = false;
		boolean ordFlag = true;
		int updCnt=0,updCnt1=0;
		Timestamp today = null,sysDate=null,indDate=null;
		Date date = null;
		String keyString="",tranIdCol="",userId="",loginSite="",siteCode="",termId="",empCode="",siteCodeDes="",tranType="";
		String acctCode="",cctrCode="",siteCodeAcct="",projCode="",deptCode="",analCode="";
		double indentQty=0,indentRate=0;
		String xmlValues = "";
		StringBuffer budgetXml = null;
		double amount=0;
		try
		{

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			Calendar currentDate = Calendar.getInstance();
			tranType="INDAMDC";

			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr + "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");


			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );

			budgetXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>\r\n</header>");

			sql=  " select ind_date,acct_code,cctr_code,site_code__acct,proj_code, " 
					+" (case when quantity is null then 0 else quantity end)- (case when ord_qty is null then 0 else ord_qty end) as ind_qty, " 
					+" (case when purc_rate is null then 0 else purc_rate end ) as ind_rate,dept_code,anal_code "
					+" from indent where ind_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,indentNo);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				indDate=rs.getTimestamp("ind_date");
				acctCode=rs.getString("acct_code");
				cctrCode=rs.getString("cctr_code");
				siteCodeAcct=rs.getString("site_code__acct");
				projCode=rs.getString("proj_code");
				indentQty=rs.getDouble("ind_qty");
				indentRate=rs.getDouble("ind_rate");
				deptCode=rs.getString("dept_code");
				analCode=rs.getString("anal_code");
			}
			else
			{
				errString = itmDBAccess.getErrorString("","VTNOIND","","",conn);
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


			budgetXml.append("<Detail1>");
			budgetXml.append("<site_code><![CDATA["+siteCodeAcct+"]]></site_code>");
			budgetXml.append("<proj_code><![CDATA["+projCode+"]]></proj_code>");
			budgetXml.append("<tran_date><![CDATA["+sdf.format(indDate)+"]]></tran_date>");
			budgetXml.append("<acct_code><![CDATA["+acctCode+"]]></acct_code>");
			budgetXml.append("<cctr_code><![CDATA["+cctrCode+"]]></cctr_code>");
			budgetXml.append("<dept_code><![CDATA["+deptCode+"]]></dept_code>");
			budgetXml.append("<anal_code><![CDATA["+analCode+"]]></anal_code>");
			budgetXml.append("<upd_type><![CDATA[C]]></upd_type>");
			budgetXml.append("<tran_type><![CDATA["+tranType+"]]></tran_type>");

			amount=indentQty * indentRate;

			budgetXml.append("<amount><![CDATA["+amount+"]]></amount>");

			budgetXml.append("</Detail1>");

			budgetXml.append("</Root>");

			System.out.print("Budget Xml String to update Budget :: "+budgetXml.toString());

			System.out.println("errString>>>>>>>>>>>{"+errString+"}" + errString.trim().length());
			if(errString == null || errString.trim().length() == 0)
			{
				System.out.println("errString11>>>>>>>>>>>"+errString);
				errString = finCommon.updateBudget(budgetXml.toString(),"",conn);
				System.out.println("rrString1122222>>>>>>>>>>>"+errString);

				if(errString != null && errString.trim().length() > 0)
				{
					conn.rollback();
					return errString ;
				}
			}		

		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
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

			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		System.out.println("return errString updateindent close=========>> "+errString);
		return errString;
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
			throw new ITMException(ex); 
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


