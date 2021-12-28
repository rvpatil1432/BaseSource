/**
 * Developed by Ritesh On 07/02/14 
 * Purpose: Verify  charge back data (req : DI3FSUN023)
 * */
package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.sql.*;
import java.text.DecimalFormat;
import java.rmi.*;
import java.util.*;

import org.nfunk.jep.JEP; // for ejb3 

import javax.ejb.*;
import javax.annotation.*;

import org.w3c.dom.*;
//import java
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ChargeBackFormVerify extends ActionHandlerEJB implements ChargeBackFormVerifyLocal, ChargeBackFormVerifyRemote 
{
	static
	{
		System.out.println(" ChargeBackFormVerify getch()");
	}
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;
		System.out.println(" actionHandler() called :: ");
		System.out.println(".......tranId......."+tranId);
		System.out.println(".......xtraParams..."+xtraParams);
		System.out.println(".......forcedFlag..."+forcedFlag);
		try
		{
			if(tranId!=null && tranId.trim().length()>0)
			{
				returnString = verifyAll(tranId,xtraParams,forcedFlag);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception ..."+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return returnString;
	}	

	private String verifyAll(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("verifyAll (1) called........["+tranId+"]");
		double qty=0.0,netAmt=0.0,rateSell=0.0;
		double saleqty=0.0,saleretqty=0.0,unconfclaimed=0.0,confclaimed=0.0,rateContr=0.0;
		String itemCode="",lotNo="",porderNo="",porderDate= "",itemSer= "";
		String errString = null,sql="";
		int rowcount = 0,errorCount=0;
		String siteCode="",custCode="",tranDatestr="",tranType="",verifyFlag="",verifyFlagDet="",forceVerifyDet="";
		String  custCodeCredit = "",loginSite;
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pStmt = null,pStmt1=null,pstmtMsg=null;
		ResultSet rs = null,rs1=null,rsMsg=null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String childNodeName = "";
		Node parentNode = null;
		NodeList childNodeList = null;
		NodeList parentNodeList = null;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		Node childNode = null;
		StringBuffer errCode = new StringBuffer("");
		StringBuffer errCodeDet = null;
		StringBuffer errMsgHed = new StringBuffer("");
		StringBuffer errMsgDet = null;
		boolean errorflag = false,flagVerified = false,verifyDetailFlag= false;
		String errCodeString = "";
		ArrayList list = new ArrayList();
		boolean rejectedFlag = false;
		int elements = 0;
		//DecimalFormat df = new DecimalFormat("0.000");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		int cnt=0,cnf=0;
		java.sql.Timestamp tranDateTs = null;
		String prdCode = "",statSal = "";
		try
		{
			System.out.println(" INSIDE TRY { ");
			connDriver = null;
			genericUtility = new ibase.utility.E12GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;            			
			conn.setAutoCommit(false);
			//parentNodeList = dom1.getElementsByTagName("Detail1");
			//parentNodeListLength = parentNodeList.getLength(); 
			loginSite =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			rejectedFlag = false;
			
			
		//	System.out.println("parentNodeListLength------------------->"+parentNodeListLength);
			sql = " select  count(*) from charge_back_form where tran_id = ? and VERIFY_FLAG = 'Y'";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranId.trim());
			rs = pStmt.executeQuery();
			if(rs.next()) 
			{
				cnf  = rs.getInt(1);
			}if(rs != null)
				rs.close();
			rs = null;
			if(pStmt != null)
				pStmt.close();
			pStmt=null;
			if(cnf > 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTVARALR   ","","",conn);
			    return errString;
			}
			/**
			 * VALLABH KADAM 18/JUN/15
			 * While verification
			 * If the Transaction is already 'CANCEL'
			 * that is STATUS='X'
			 * The verification is not allow [VTCANCLALR]
			 * Req Id :- [D15BSUN003]
			 * */
			sql = " select  count(*) from charge_back_form where tran_id = ? and status = 'X'";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranId.trim());
			rs = pStmt.executeQuery();
			if(rs.next()) 
			{
				cnf  = rs.getInt(1);
			}if(rs != null)
				rs.close();
			rs = null;
			if(pStmt != null)
				pStmt.close();
			pStmt=null;
			if(cnf > 0)
			{
				System.out.println("The Transaction is already Cancel @@@@");
				errString = itmDBAccessEJB.getErrorString("","VTCANCLALR","","",conn);
			    return errString;
			}
			
			//delete older error code and error messages from charge_back_form and charge_back_form_det added by sagar on 12/06/14 start..
			System.out.println(">>>>>>>>>>>delete older entries from charge_back_form table");
			sql ="update charge_back_form set error_msg = ?,error_code= ? where tran_id = ? ";
			pStmt =conn.prepareStatement(sql);
			pStmt.setString(1,null);
			pStmt.setString(2,null);
			pStmt.setString(3, tranId.trim());
			pStmt.executeUpdate();
			pStmt.close();
			pStmt = null;
			System.out.println(">>>>>>>>>delete older entries from charge_back_form_det table");
			sql ="update charge_back_form_det set error_code = ? ,error_msg = ? where tran_id = ? " ;
			pStmt =conn.prepareStatement(sql);
			pStmt.setString(1,null);
			pStmt.setString(2,null);
			pStmt.setString(3, tranId.trim());
			pStmt.executeUpdate();
			pStmt.close();
			pStmt = null;
			//delete older error code and error messages from charge_back_form and charge_back_form_det added by sagar on 12/06/14 end..
			
			sql = " select site_code ,cust_code , cust_code__credit,verify_flag ,tran_date,tran_type from charge_back_form where tran_id = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranId);
			rs = pStmt.executeQuery();
			if(rs.next())
			{
				siteCode = rs.getString("site_code");
				custCode = rs.getString("cust_code");
				custCodeCredit = rs.getString("cust_code__credit");
				verifyFlag = rs.getString("verify_flag");
				tranDateTs = rs.getTimestamp("tran_date");
				tranType = rs.getString("tran_type");
			}
			rs.close();
			rs = null;
			pStmt.close();
			pStmt = null;
			if(custCode == null || custCode.trim().length() == 0)
			{
			    errCode.append("VMCUSTNUL,");
			  
			    sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMCUSTNUL' ";
			    pstmtMsg = conn.prepareStatement(sql);
				rsMsg = pstmtMsg.executeQuery();
				if (rsMsg.next()) {
					errMsgHed.append(rsMsg.getString(1)+",");
				}
				else{
					errMsgHed.append("VMCUSTNUL"+",");
				}
				rsMsg.close();
				rsMsg = null;
				pstmtMsg.close();
				pstmtMsg = null;
			}
			
			else
			{
				sql = " SELECT COUNT(*) FROM customer WHERE cust_code = ? ";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, custCode.trim());
				rs = pStmt.executeQuery();
				if (rs.next()) {
					cnt = rs.getInt(1);
				}
				if (cnt == 0) {
					errCode.append("VMINVCUST,");
					
					    sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMINVCUST' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgHed.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgHed.append("VMINVCUST"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
				}
				rs.close();
				rs = null;
				pStmt.close();
				pStmt = null;
			}

//			tranDateTs = Timestamp.valueOf(genericUtility
//					.getValidDateString(tranDatestr,
//							genericUtility.getApplDateFormat(),
//							genericUtility.getDBDateFormat())
//							+ " 00:00:00.0");

			sql = " Select code from period where ? between fr_date and to_date ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setTimestamp(1, tranDateTs);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				prdCode = rs.getString(1);
			}
			rs.close();
			rs = null;
			pStmt.close();
			pStmt = null;
			if (prdCode != null && prdCode.trim().length() > 0)
			{
					sql = " Select count(1) from period_stat where prd_code = ? "
							+ " AND site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, prdCode.trim());
					pStmt.setString(2, loginSite.trim());
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					if (cnt > 0) {
						sql = " Select stat_sal from period_stat where prd_code = ? and site_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, prdCode.trim());
						pStmt.setString(2, loginSite.trim());
						rs = pStmt.executeQuery();
						if (rs.next()) {
							statSal = rs.getString(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						if (statSal != null
								&& statSal.trim().equalsIgnoreCase("N")) {
							errCode.append("VMSTATSNVL,");
							
							sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMSTATSNVL' ";
						    pstmtMsg = conn.prepareStatement(sql);
							rsMsg = pstmtMsg.executeQuery();
							if (rsMsg.next()) {
								errMsgHed.append(rsMsg.getString(1)+",");
							}
							else{
								errMsgHed.append("VMSTATSNVL"+",");
							}
							rsMsg.close();
							rsMsg = null;
							pstmtMsg.close();
							pstmtMsg = null;
						}
					} else {
						errCode.append("VMSTATSND,");
						
						sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMSTATSND' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgHed.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgHed.append("VMSTATSND"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
						
					}
			} else {
				errCode.append("VMPRDNTDF,");
				
				sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMPRDNTDF' ";
			    pstmtMsg = conn.prepareStatement(sql);
				rsMsg = pstmtMsg.executeQuery();
				if (rsMsg.next()) {
					errMsgHed.append(rsMsg.getString(1)+",");
				}
				else{
					errMsgHed.append("VMPRDNTDF"+",");
				}
				rsMsg.close();
				rsMsg = null;
				pstmtMsg.close();
				pstmtMsg = null;
			}
			if(tranType == null || tranType.trim().length() == 0)
			{
				errCode.append("VMTRNTPNUL,");
				
				sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMTRNTPNUL' ";
			    pstmtMsg = conn.prepareStatement(sql);
				rsMsg = pstmtMsg.executeQuery();
				if (rsMsg.next()) {
					errMsgHed.append(rsMsg.getString(1)+",");
				}
				else{
					errMsgHed.append("VMTRNTPNUL"+",");
				}
				rsMsg.close();
				rsMsg = null;
				pstmtMsg.close();
				pstmtMsg = null;
				
			}
			if(custCodeCredit == null || custCodeCredit.trim().length() == 0)
			{
			  errCode.append("VMCUSTNUL,");
			  
				sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMCUSTNUL' ";
			    pstmtMsg = conn.prepareStatement(sql);
				rsMsg = pstmtMsg.executeQuery();
				if (rsMsg.next()) {
					errMsgHed.append(rsMsg.getString(1)+",");
				}
				else{
					errMsgHed.append("VMCUSTNUL"+",");
				}
				rsMsg.close();
				rsMsg = null;
				pstmtMsg.close();
				pstmtMsg = null;
			}
			else
			{
				sql = " SELECT COUNT(*) FROM customer WHERE cust_code = ? ";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, custCodeCredit.trim());
				rs = pStmt.executeQuery();
				if (rs.next()) {
					cnt = rs.getInt(1);
				}
				if (cnt == 0) {
					  errCode.append("VMINVCUST,");
				
					    sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMINVCUST' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgHed.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgHed.append("VMINVCUST"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
				}
				rs.close();
				rs = null;
				pStmt.close();
				pStmt = null;
			}
			if(siteCode == null || siteCode.trim().length() == 0)
			{
			  errCode.append("VMSITENUL,");
			    
			    sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMSITENUL' ";
			    pstmtMsg = conn.prepareStatement(sql);
				rsMsg = pstmtMsg.executeQuery();
				if (rsMsg.next()) {
					errMsgHed.append(rsMsg.getString(1)+",");
				}
				else{
					errMsgHed.append("VMSITENUL"+",");
				}
				rsMsg.close();
				rsMsg = null;
				pstmtMsg.close();
				pstmtMsg = null;
			}
			else
			{
				sql = " SELECT COUNT(*) FROM site WHERE site_code = ? ";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, siteCode.trim());
				rs = pStmt.executeQuery();
				if (rs.next()) {
					cnt = rs.getInt(1);
				}
				if (cnt == 0) {
					 errCode.append("VMINVSITE,");
					 
					    sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VMINVSITE' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgHed.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgHed.append("VMINVSITE"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
				}
				rs.close();
				rs = null;
				pStmt.close();
				pStmt = null;
			}
			//comment by sagar on 09/06/14
			//sql = "select item_code,lot_no,porder_no,porder_date,item_ser,quantity,rate__contr, NET_AMT,RATE__SELL, " +
			//		" sale_qty,sale_ret_qty,unconf_claimed,conf_claimed,line_no  from charge_back_form_det where tran_id = ? ";
			sql="SELECT item_code,lot_no,porder_no,porder_date,item_ser,quantity,rate__contr, NET_AMT,RATE__SELL,sale_qty,sale_ret_qty,unconf_claimed,conf_claimed,line_no," +
					" CASE WHEN verify_flag IS NULL THEN 'N' ELSE verify_flag END verify_flag,CASE WHEN force_verify IS NULL THEN 'N' ELSE force_verify END force_verify " +
					" FROM charge_back_form_det WHERE tran_id = ?";
			pStmt1 = conn.prepareStatement(sql);
			pStmt1.setString(1, tranId.trim());
			rs1 = pStmt1.executeQuery();
			while(rs1.next()) 
			{
				rowcount ++;
				errCodeDet = new StringBuffer("");
				errMsgDet = new StringBuffer("");
				itemCode = rs1.getString("item_code");
				lotNo = rs1.getString("lot_no");
				porderNo = rs1.getString("porder_no");
				porderDate = rs1.getString("porder_date");
				itemSer = rs1.getString("item_ser");
				qty = rs1.getDouble("quantity");
				rateContr = rs1.getDouble("rate__contr");
				saleqty = rs1.getDouble("sale_qty");
				saleretqty = rs1.getDouble("sale_ret_qty");
				unconfclaimed = rs1.getDouble("unconf_claimed");
				confclaimed = rs1.getDouble("conf_claimed");
				netAmt = rs1.getDouble("NET_AMT");
				rateSell = rs1.getDouble("RATE__SELL");
				verifyFlagDet=rs1.getString("verify_flag");
			    forceVerifyDet=rs1.getString("force_verify");
				
				if(netAmt == 0)
				{
					errCodeDet.append("VTNETAMT,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTNETAMT' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTNETAMT"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
					
				}
				if(rateSell == 0)
				{
					errCodeDet.append("VTRATESL,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTRATESL' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTRATESL"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
				}
				
				if(itemCode == null || itemCode.trim().length() == 0)
				{
					errCodeDet.append("VTITMNUL,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTITMNUL' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTITMNUL"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
				}
				else
				{
					sql = "SELECT count(*) from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCode.trim());
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}
					System.out.println(" COUNT =====> [" + cnt + "]");
					if (cnt == 0) {
						errCodeDet.append("VTINVITM,");
						
						sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTINVITM' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgDet.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgDet.append("VTINVITM"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
				}
				if(lotNo == null || lotNo.trim().length() == 0)
				{
					errCodeDet.append("VTLOTNUL,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTLOTNUL' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTLOTNUL"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
				}
				if(porderNo == null || porderNo.trim().length() == 0)
				{
					errCodeDet.append("VTBILNONUL,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTBILNONUL' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTBILNONUL"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
				} 
				else
				{	
					sql = "Select count(1) from charge_back_form A, charge_back_form_det B where"
							+ " A.tran_id = B.tran_id "
							+ " and A.cust_code = ? "
							+ " and B.porder_no = ? "
							+ " and A.tran_id <> '" + tranId + "' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, custCode.trim());
					pStmt.setString(2, porderNo.trim());
					rs = pStmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					if (cnt >= 1) {
						errCodeDet.append("VTBILNOPST,");
						
						sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTBILNOPST' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgDet.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgDet.append("VTBILNOPST"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
						
					}
				}
				if(porderDate == null || porderDate.trim().length() == 0)
				{
					errCodeDet.append("VTBILDTNUL,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTBILDTNUL' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTBILDTNUL"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
					
				}
				if(itemSer == null || itemSer.trim().length() == 0)
				{
					errCodeDet.append("VTITMSRNUL,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTITMSRNUL' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTITMSRNUL"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
					
				}
				
				saleqty = rs1.getDouble("sale_qty");
				saleretqty = rs1.getDouble("sale_ret_qty");
				unconfclaimed = rs1.getDouble("unconf_claimed");
				confclaimed = rs1.getDouble("conf_claimed");
				System.out.println(">>>>>qty:"+qty);
				System.out.println(">>>>>saleqty:"+saleqty);
				System.out.println(">>>>>saleretqty:"+saleretqty);
				System.out.println(">>>>>unconfclaimed:"+unconfclaimed);
				System.out.println(">>>>>confclaimed:"+confclaimed);
				double test= saleqty - saleretqty-unconfclaimed - confclaimed;
				System.out.println(">>>>>test:"+test);
				if(qty > 0.0)
				{
					if (qty > (saleqty - saleretqty
							- unconfclaimed - confclaimed))
					{
						errCodeDet.append("VTINVQUANT,");
						System.out.println(">>>>>Added VTINVQUANT test:"+test);
						sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTINVQUANT' ";
					    pstmtMsg = conn.prepareStatement(sql);
						rsMsg = pstmtMsg.executeQuery();
						if (rsMsg.next()) {
							errMsgDet.append(rsMsg.getString(1)+",");
						}
						else{
							errMsgDet.append("VTINVQUANT"+",");
						}
						rsMsg.close();
						rsMsg = null;
						pstmtMsg.close();
						pstmtMsg = null;
					}
				}
				if(rateContr < 0)
				{
					errCodeDet.append("VTSTKBILRT,");
					
					sql = " SELECT MSG_DESCR  FROM MESSAGES WHERE MSG_NO =  'VTSTKBILRT' ";
				    pstmtMsg = conn.prepareStatement(sql);
					rsMsg = pstmtMsg.executeQuery();
					if (rsMsg.next()) {
						errMsgDet.append(rsMsg.getString(1)+",");
					}
					else{
						errMsgDet.append("VTSTKBILRT"+",");
					}
					rsMsg.close();
					rsMsg = null;
					pstmtMsg.close();
					pstmtMsg = null;
					
				}
				
				errCodeString = errCodeDet.toString();
				/*if(errCodeString != null && errCodeString.trim().length() > 0)
				{
					errorCount ++;
					errorflag = true;
					errCodeString = errCodeString.substring(0,errCodeString.length()-1);
				}else
				{
					verifyDetailFlag = true;
				}*/
				String errMsgDetString =  errMsgDet.toString();
				if(errMsgDetString != null && errMsgDetString.trim().length() > 0)
					errMsgDetString = errMsgDetString.substring(0,errMsgDetString.length()-1);
				if(errMsgDetString.length() > 4000)
					errMsgDetString = errMsgDetString.substring(0,4000);
				
				System.out.println(" errCodeDet String ::"+errCodeString);
				System.out.println(" errMsgDetString ::"+errMsgDetString);
				//Condition added by sagar on 11/06/14 start
				if(errCodeString != null && errCodeString.trim().length() > 0 && "Y".equalsIgnoreCase(forceVerifyDet))
				{
					System.out.println(">>>>>>>>errCodeString is not null and force Y :");
					verifyFlagDet="Y";
					verifyDetailFlag = true;
					errCodeString = errCodeString.substring(0,errCodeString.length()-1);
				}
				else if(errCodeString != null && errCodeString.trim().length() > 0 && "N".equalsIgnoreCase(forceVerifyDet))
				{
					System.out.println(">>>>>>>>errCodeString is not null and force N :");
					verifyFlagDet="N";
					errorflag = true;
					errCodeString = errCodeString.substring(0,errCodeString.length()-1);
				}
				else if(errCodeString.trim().length() <= 0 && "Y".equalsIgnoreCase(forceVerifyDet))
				{
					System.out.println(">>>>>>>>errCodeString is null and force Y :");
					verifyFlagDet="N";
					errorflag = true;
					
				}
				else
				{
					System.out.println(">>>>>>>>errCodeString is null");
					verifyFlagDet="Y";
					verifyDetailFlag = true;
				}
				//Condition added by sagar on 11/06/14 end..
//				if(errCodeString.trim().length() > 0)
//				{
					sql= " update charge_back_form_det set error_code = ?,error_msg = ?,verify_flag= ? where tran_id = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, errCodeString.trim());
					pStmt.setString(2, errMsgDetString.trim());
					pStmt.setString(3, verifyFlagDet);
					pStmt.setString(4, tranId.trim());
					pStmt.setInt(5, Integer.parseInt(rs1.getString("line_no")));
					int detupd = pStmt.executeUpdate();
					pStmt.close();
					pStmt = null;
					System.out.println(">>>>>>>>charge_back_form_det updated ::"+detupd);
//					if(errCodeString.trim().length() > 0)
//					{
//						sql= " update charge_back_form_det set VERIFY_FLAG = 'R' where tran_id = ? and line_no = ? ";
//						pStmt = conn.prepareStatement(sql);
//						pStmt.setString(1, tranId.trim());
//						pStmt.setInt(2, Integer.parseInt(rs1.getString("line_no")));
//						pStmt.executeUpdate();
//						pStmt.close();
//						pStmt = null;
//						conn.commit();
//						System.out.println("detail updated 2::"+detupd);
//					}
				//}
			}
			rs1.close();
			rs1 = null;
			pStmt1.close();
			pStmt1 = null;
			String errCodeHedString = errCode.toString();
			String errMsgHedString = errMsgHed.toString();
			
			if(errCodeHedString != null && errCodeHedString.trim().length() > 0)
				errCodeHedString = errCodeHedString.substring(0,errCodeHedString.length()-1);
			
			if(errMsgHedString != null && errMsgHedString.trim().length() > 0)
				errMsgHedString = errMsgHedString.substring(0,errMsgHedString.length()-1);
			
			if(errCodeHedString.trim().length() > 0)// && (rowcount == errorCount))
			{
				if(errCodeHedString.length() > 4000)
					errCodeHedString = errCodeHedString.substring(0,4000);
				sql= " update charge_back_form set verify_flag = 'R', error_code = ? , error_msg = ? where tran_id = ? ";
				errString = itmDBAccessEJB.getErrorString("","VTREJCHB1 ","","",conn);
			}
			else
			{
				if(errorflag)
					
				{
					System.out.println(" ERROR FOUND IN DETAIL");
					sql= " update charge_back_form set verify_flag = 'P', error_code = ? , error_msg = ? where tran_id = ? ";
					errString = itmDBAccessEJB.getErrorString("","VTPARVFY","","",conn);
				}
				else
				{
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Success All errorflag:"+errorflag);
					sql= " update charge_back_form set verify_flag = 'Y' , error_code = ?, error_msg = ? where tran_id = ? ";
				    //errString= "success";
					errString = itmDBAccessEJB.getErrorString("","VTCHBSUC ","","",conn);

				}
				if(verifyDetailFlag == false)
				{
					sql= " update charge_back_form set verify_flag = 'R', error_code = ? , error_msg = ? where tran_id = ? ";
					errString = itmDBAccessEJB.getErrorString("","VTREJCHB1 ","","",conn);
				}
				if(verifyDetailFlag ==true && errorflag == true)
				{
					System.out.println(" ERROR FOUND IN DETAIL 2");
					sql= " update charge_back_form set verify_flag = 'P', error_code = ? , error_msg = ? where tran_id = ? ";
					errString = itmDBAccessEJB.getErrorString("","VTPARVFY","","",conn);
				}
				
				
			}
			
			System.out.println(">>>>>>>>>>>>>>Before update errCodeHedString:"+errCodeHedString);
			System.out.println(">>>>>>>>>>>>>>Before update errMsgHedString:"+errMsgHedString);
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, errCodeHedString.trim());
			pStmt.setString(2, errMsgHedString.trim());
			pStmt.setString(3, tranId.trim());
			int hedupd = pStmt.executeUpdate();
			pStmt.close();
			pStmt = null;
			System.out.println(" header updated ::"+hedupd);
			
//			if(rejectedFlag)
//			{
//				errString = itmDBAccessEJB.getErrorString("","VTREJCHB","","",conn);	
//				String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
//				String endPart = errString.substring( errString.indexOf("</trace>"));
//				String mainStr = begPart + "Following error has occured\n" ;
//				if(elements > 0 )
//				{ 
//					mainStr	= mainStr + "Transaction not varified: \n";
//				}
//				for(int i = 0; i < elements * 2; i++ )
//				{
//					if( i > 0)
//					{
//						i -= 1;
//					}
//					mainStr = mainStr + 
//							"Tran Id :"+list.get(i++)+ ",cust code :"+ list.get(i++) + "\n" ;
//				}
//				mainStr = mainStr +  endPart;	
//				errString = mainStr;
//				begPart =null;
//				endPart =null;
//				mainStr =null;	
//				//return errString;
//			}
//			if(errString.indexOf("notvarified")>-1)
//			{
//				errString = itmDBAccessEJB.getErrorString("","VTREJCHB ","","",conn);
//			}
//			else if(errString.indexOf("success")>-1)
//			{
//				errString = itmDBAccessEJB.getErrorString("","VTCHBSUC ","","",conn);
//			}
		}
		catch (Exception e)
		{
			errString = e.getMessage();
			System.out.println("Exception..."+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(errString.indexOf("VTCHBSUC")>-1 || errString.indexOf("VTREJCHB")>-1 )
				{
					conn.commit();
				}
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
				if(pStmt != null)
				{
					pStmt.close();
					pStmt = null;					
				}
				if(pStmt1 != null)
				{
					pStmt1.close();
					pStmt1 = null;					
				}	
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
}
