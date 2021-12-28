/**
* PURPOSE : Splitting of Sales Return Form
* AUTHOR : Done  by Rohan on 20/09/11
*/

package ibase.webitm.ejb.dis.adv;

import ibase.utility.EMail;

import ibase.utility.CommonConstants;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;

import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.StockUpdate;
import ibase.webitm.ejb.dis.InvAllocTraceBean;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.ejb.Stateless;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

@Stateless
public class SalesReturnFormSplit extends ActionHandlerEJB implements SalesReturnFormSplitLocal, SalesReturnFormSplitRemote
{
    /**
	 * The public method is used for splitting the sales return form
	 * Returns splitted message on successfull splitting otherwise returns error message
	 * @param tranId is the transaction id to be splitted
	 * @param xtraParams contais additional information such as loginEmpCode,loginCode,chgTerm etc
	 * @param forcedFlag (true or false)
	 */
	String userId = "", termId = "";
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	public String siteCode = "";
	@Override
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String retString = "";
		String sql = "";
		String sql1 = "";
		String status = "";
		String empCode = null;
		String verifyFlag= "";
		String errString = "" ;
		
		try
		{
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		    userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			if (userId == null || userId.trim().length() == 0)
			{
				userId = "SYSTEM";
			}
			if (termId == null || termId.trim().length() == 0)
			{
				termId = "SYSTEM";
			}
			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				empCode = rs.getString("EMP_CODE");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			//sql = "SELECT case when status is null then 'N' else status end as status, SITE_CODE FROM sreturnform WHERE TRAN_ID= ?";
            sql = "SELECT case when status is null then 'N' else status end as status, SITE_CODE,VERIFY_FLAG FROM sreturn_form WHERE TRAN_ID= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				//status = rs.getString("CONFIRMED");//should be status shamim
				status = rs.getString("STATUS");
				siteCode = rs.getString("SITE_CODE");
				verifyFlag = checkNull(rs.getString("VERIFY_FLAG"));
				
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			if( "S".equalsIgnoreCase(status) )									
			{
				//retString = itmDBAccessLocal.getErrorString("","VTCONF1","");
				retString = itmDBAccessLocal.getErrorString("","VTCONFMSP","","",conn);
				return retString;
			}		
															//ADDED BY RITESH ON 05/08/13 START
			if( "C".equalsIgnoreCase(status.trim()) || "X".equalsIgnoreCase(status.trim()) )                  
			{
				retString = itmDBAccessLocal.getErrorString("","VTTRNNTSP","","",conn);
				return retString;
			}												//ADDED BY RITESH ON 05/08/13 END
			/* CHANDNI
			else if(!verifyFlag.equalsIgnoreCase("Y"))				
			{	
			    retString = itmDBAccessLocal.getErrorString("","VTTNTFRCE","");
			    return retString;
			}
			*/
			else
			{
				retString = split(tranId, xtraParams,conn);
				 
				System.out.println("Testing retString----------------"+retString);
				if (retString.contains("Success"))
				{ 
					sql = "UPDATE SRETURN_FORM SET STATUS = ? WHERE TRAN_ID = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,"S");
					pstmt.setString(2,tranId);
	
					pstmt.executeUpdate();
					
					retString = itmDBAccessLocal.getErrorString("","VTSRSPLIT","","",conn);
					
				}
				else
				{
					conn.rollback();
				}
				
			 }
			
		}
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch (Exception e1)
			{
			}
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
	
				conn.commit();
				System.out.println("---COMMITTED==");
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::"+retString);
		return retString;
	}

	public String split(String tranId, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("Enter in Split function Method");
		PreparedStatement pstmt = null, pstmt1 = null,pstmt2 = null;
		PreparedStatement pstmtUpd = null;
		ResultSet rs = null, rs1 = null ,rs2 = null;

		String sql = "",sql1= "",sql2 = "",sql3 = "";
		String errString = "", trnofld = null,transer = "S-RET", keyString = null;

		java.sql.Timestamp tranDate	= null, effDate	= null, lrDate = null;

		String tranType = "", invoiceId = "", custCode = "", itemSer = "", retOpt = "",itemSer1 = "",invoice1="";
		String siteCode = "", projCode = "", analCode = "", remarks = "", fullRet = "";
		String currCode = "", custRef = "", tranCode = "", lrNo = "", lorryNo = "", transMode = "";
		String currCodeBC = "", priceList = "", priceListClg = "";

		double exchRate = 0, netAmount = 0, frtAamount = 0, effNetAmount = 0, bankCharges = 0;

		java.sql.Timestamp today = null;
		java.sql.Timestamp confDate = null;
		java.util.Date date = null;
		HashMap splitCodeWiseMap =  new HashMap(), tempMap = null;
		ArrayList tempList = null;
		String splitCode = "", tempSplitCode ="";

		String itemSerDet = "",itemSerHdr = "", invoiceIdDet = "", itemCode = "", statusDet = "", reasCode = "";
		String locCode = "", stkOpt = "", lotNo = "", lotSl = "", taxClass = "";
		String taxChap = "", taxEnv = "", unit = "", retRepFlag = "", unitStd = "";
		String siteCodeMfg = "", unitRate = "", packCode = "",empCode = "" ;
		String crterm  = "";
		String shiptozip = "";
		String custCd = "";
		String custCodeBill="",custCodeDlv="",siteCodeDlv = "";
        String custCodeBillDet="",custCodeBill1="";//added by Pratiksha A on 26/03/2021
		double lineNoInv = 0, quantity = 0, netAmountDet = 0, rate = 0, effNetAmountDet = 0,claimQuantity = 0,physicalQuantity=0,lineNoInvtrace=0;
		double convQtyStdUom = 0, convRtUomStdUom = 0, quantityStdUom = 0, rateStdUom = 0;
		double discount = 0, taxAmtDet = 0, rateClg = 0, costRate = 0;
		
		java.sql.Timestamp  expDate = null, mfgDate = null;
		//java.sql.Timestamp	currDate = null;
		java.util.Date currDate = new java.util.Date();
		StringBuffer xmlBuff = null;
		String xmlString = null,retString  = null, salesPers = null;
		java.text.SimpleDateFormat sdf = null;
		int lineNo = 0, crCount = 0;
		String retRef="",reasCodeH = "",fullRetHdr="";
		String invoiceRef="",docKey="",srfSplitKey="" , srfSplitKeyListArr[]=null,docValue="" ;// added by nandkumar gadkari on 03/09/19
		String priceListSR="",priceListClgSR="";// added by nandkumar gadkari on 08/06/2020
		try
		{
			DistCommon distCommom = new DistCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		    userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");		
			termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			if (userId == null || userId.trim().length() == 0)
			{
				userId = "SYSTEM";
			}
			if (termId == null || termId.trim().length() == 0)
			{
				termId = "SYSTEM";
			}
			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				empCode = rs.getString("EMP_CODE");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			/*if( dbName.equalsIgnoreCase("db2"))
			{
				sql = "SELECT TRAN_ID,(CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END), (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE		   QC_REQD END), PURC_ORDER, TRAN_TYPE FROM PORCP WHERE TRAN_ID = ? FOR UPDATE ";
			}
			else if ( dbName.equalsIgnoreCase("mssql") )
			{
				sql = "SELECT TRAN_ID,(CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END), (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE		   QC_REQD END), PURC_ORDER , TRAN_TYPE FROM PORCP (UPDLOCK) WHERE TRAN_ID = ?";
			}
			else
			{
				sql = "SELECT TRAN_ID,(CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END), (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE		   QC_REQD END), PURC_ORDER , TRAN_TYPE FROM PORCP WHERE TRAN_ID = ? FOR UPDATE NOWAIT";
			}*/
			////////////////////////////////////////////////////////////////////////////////////
			/*transer = "S-RET";
			trnofld = "tran_id";

			sql = "select key_string FROM transetup WHERE tran_window = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "w_salesreturn");
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				keyString = rs.getString("key_string");
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
			}
			else
			{
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				sql = "select key_string FROM transetup WHERE tran_window = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "GENERAL");
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					keyString = rs.getString("key_string");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
			}*/

			sql = "SELECT tran_date, tran_type, eff_date, invoice_id, "
				+ " cust_code, item_ser, ret_opt, site_code, proj_code, "
				+ " anal_code, remarks, full_ret, curr_code, exch_rate, "
				+ " cust_ref, net_amt, tran_code, lr_no, lr_date, lorry_no, "
				+ " frt_amt, trans_mode, bank_charges, curr_code__bc, price_list, "
				+ " eff_net_amt, tax_amt, price_list__clg,ret_ref,reas_code,site_code__dlv, "
				+ " cust_code__bill,cust_code__dlv  "//added by Pratiksha A on 26-03-21
				+ " FROM sreturn_form "
				+ " WHERE sreturn_form.tran_id = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				tranDate	= rs.getTimestamp("tran_date");
				tranType	= rs.getString("tran_type");
				effDate		= rs.getTimestamp("eff_date");
				invoiceId	= checkNull(rs.getString("invoice_id"));
				custCode	= checkNull(rs.getString("cust_code"));
				itemSerHdr		= checkNull(rs.getString("item_ser"));
				retOpt		= checkNull(rs.getString("ret_opt")); //change done by Kunal Mandhre on 20/06/12
				siteCode	= checkNull(rs.getString("site_code"));
				projCode	= checkNull(rs.getString("proj_code"));
				analCode	= checkNull(rs.getString("anal_code"));
				remarks		= checkNull(rs.getString("remarks"));
				fullRetHdr		= checkNull(rs.getString("full_ret"));//change in variable name by nandkumar gadkari on 03/07/19
				//currCode	= checkNull(rs.getString("curr_code"));
				currCode	= rs.getString("curr_code");
				exchRate	= rs.getDouble("exch_rate");
				custRef		= checkNull(rs.getString("cust_ref"));
				netAmount	= rs.getDouble("net_amt");
				System.out.println("Net Amount"+netAmount);
				tranCode	= checkNull(rs.getString("tran_code"));
				lrNo		= checkNull(rs.getString("lr_no"));
				lrDate		= rs.getTimestamp("lr_date");
				lorryNo		= checkNull(rs.getString("lorry_no"));
				frtAamount	= rs.getDouble("frt_amt");
				transMode	= checkNull(rs.getString("trans_mode"));
				bankCharges	= rs.getDouble("bank_charges");
				currCodeBC	= rs.getString("curr_code__bc");
				priceListSR	= checkNull(rs.getString("price_list"));//Change variable name by Nandkumar Gadkari on 08/06/2020
				effNetAmount= rs.getDouble("eff_net_amt");
				System.out.println("Effective Net Amount"+effNetAmount);
				priceListClgSR= checkNull(rs.getString("price_list__clg"));//Change variable name by Nandkumar Gadkari on 08/06/2020
				retRef=checkNull(rs.getString("ret_ref"));
				reasCodeH=checkNull(rs.getString("reas_code"));
				siteCodeDlv =checkNull(rs.getString("site_code__dlv"));
				custCodeBillDet	= checkNull(rs.getString("cust_code__bill"));//added by pratiksha on 26-03-21 to set cust_code__bill value in sales return
				System.out.println("Cust Code Bill"+custCodeBillDet);//added by pratiksha on 26-03-21
				custCodeDlv	= checkNull(rs.getString("cust_code__dlv"));//added by pratiksha on 26-03-21 to set cust_code__dlv value in sales return
				System.out.println("Cust Code Bill Dlv"+custCodeDlv);//added by pratiksha on 26-03-21
				
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			// 21/11/11 manoharan check whether to consider cr_term, ship_to_zip_code in splitcode
			
			sql = "SELECT count(1) as count "
				+ " FROM sreturn_form_det "
				+ " WHERE sreturn_form_det.tran_id = ? "
				+ " and cr_term is not null "
				+ " and ship_to_zip_code is not null "; 
			 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				crCount = rs.getInt("count");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			// end 21/11/11 manoharan check whether to consider cr_term, ship_to_zip_code in splitcode
			
			sql =" SELECT line_no, invoice_id, line_no__inv,line_no__invtrace,item_code, quantity, net_amt, "
				+ " status, reas_code, loc_code, stk_opt, rate, lot_no, lot_sl, tax_class, "
				+ " tax_chap, tax_env, unit, ret_rep_flag, eff_net_amt, conv__qty_stduom, "
				+ " conv__rtuom_stduom, unit__std, quantity__stduom, rate__stduom, exp_date, "
				+ " discount, tax_amt, site_code__mfg, mfg_date, unit__rate, pack_code, full_ret, "
				+ " item_ser, rate__clg, cost_rate,cr_term,ship_to_zip_code,claim_qty,physical_qty "
				+ " ,invoice_ref,doc_key " // added by nandkumar gadkari on 03/09/19
				+ " FROM sreturn_form_det WHERE tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			
			//INNERMOST LOOP
			while (rs.next() )
			{
				invoiceIdDet	= checkNull(rs.getString("invoice_id"));
				lineNoInv 		= rs.getDouble("line_no__inv");
				//Added by kunal line_no__invtrace  D18CKOY001 
				lineNoInvtrace  = rs.getDouble("line_no__invtrace");
				itemCode 		= checkNull(rs.getString("item_code"));
				quantity		= rs.getDouble("quantity");
				claimQuantity   = rs.getDouble("claim_qty");
				physicalQuantity= rs.getDouble("physical_qty");
				netAmountDet	= rs.getDouble("net_amt");
				System.out.println("Net Amount Det"+netAmountDet);
				statusDet		= checkNull(rs.getString("status"));
				System.out.println("Status det"+statusDet);
				reasCode		= checkNull(rs.getString("reas_code"));
				locCode			= checkNull(rs.getString("loc_code"));
				stkOpt			= checkNull(rs.getString("stk_opt"));
				rate			= rs.getDouble("rate");
				lotNo			= checkNull(rs.getString("lot_no"));
				lotSl			= checkNull(rs.getString("lot_sl"));
				taxClass		= checkNull(rs.getString("tax_class"));
				taxChap			= checkNull(rs.getString("tax_chap"));
				taxEnv			= checkNull(rs.getString("tax_env"));
				//unit			= checkNull(rs.getString("unit"));
				//retRepFlag		= checkNull(rs.getString("ret_rep_flag"));
				effNetAmountDet	= rs.getDouble("eff_net_amt");
				System.out.println("Effective amt det"+effNetAmountDet);
				convQtyStdUom	= rs.getDouble("conv__qty_stduom");
				convRtUomStdUom	= rs.getDouble("conv__rtuom_stduom");
				unitStd			= checkNull(rs.getString("unit__std"));
				quantityStdUom	= rs.getDouble("quantity__stduom");
				rateStdUom		= rs.getDouble("rate__stduom");
				expDate			= rs.getTimestamp("exp_date");
				//expDate			= rs.getDate("exp_date");
				discount		= rs.getDouble("discount");
				taxAmtDet		= rs.getDouble("tax_amt");
				siteCodeMfg		= checkNull(rs.getString("site_code__mfg"));
				mfgDate			= rs.getTimestamp("mfg_date");
				//mfgDate			= rs.getDate("mfg_date");
				//unitRate		= checkNull(rs.getString("unit__rate"));
				packCode		= checkNull(rs.getString("pack_code"));
				fullRet			= checkNull(rs.getString("full_ret"));
				itemSerDet		= checkNull(rs.getString("item_ser"));
				rateClg			= rs.getDouble("rate__clg");
				costRate		= rs.getDouble("cost_rate");
				

				crterm  = checkNull(rs.getString("cr_term"));
				shiptozip = checkNull(rs.getString("ship_to_zip_code"));
				invoiceRef = checkNull(rs.getString("invoice_ref")); // added by nandkumar gadkari on 03/09/19
				docKey = checkNull(rs.getString("doc_key")); // added by nandkumar gadkari on 03/09/19
				// 21/11/11 manoharan check whether to consider cr_term, ship_to_zip_code in splitcode
				if (itemSerDet == null || itemSerDet.trim().length() == 0)
				{
					itemSerDet =  itemSerHdr;
				}
				if (crCount > 0)
				{
					sql1 = "SELECT CUST_CODE FROM CUSTOMER WHERE"+
						   " GROUP_CODE IN(SELECT GROUP_CODE FROM CUSTOMER A,sreturn_form_det B,sreturn_form C"+
						   " WHERE LTRIM(RTRIM(A.PIN))=LTRIM(RTRIM(B.SHIP_TO_ZIP_CODE)) AND"+
						   " A.CUST_CODE=C.CUST_CODE AND B.TRAN_ID=C.TRAN_ID AND B.TRAN_ID= ? AND"+
						   " LTRIM(RTRIM(SHIP_TO_ZIP_CODE))= ?)";
					pstmt2 = conn.prepareStatement(sql1);
					pstmt2.setString(1,tranId);
					pstmt2.setString(2,shiptozip.trim());
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						custCd = rs2.getString("CUST_CODE") == null ? "" : rs2.getString("CUST_CODE");
					}
					pstmt2.close();
					pstmt2 = null;					
					rs2.close();
					rs2 = null;
				}
				else
				{
					custCd = custCode;
				}
				// end 21/11/11 manoharan check whether to consider cr_term, ship_to_zip_code in splitcode
				sql3 ="select price_list, price_list__clg from site_customer where site_code = ? and cust_code = ?";
				pstmt2 = conn.prepareStatement(sql3);
				pstmt2.setString(1,siteCode);
				pstmt2.setString(2,custCd);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					priceList = rs2.getString("price_list");
				}
				pstmt2.close();
				pstmt2 = null;					
				rs2.close();
				rs2 = null;
				
				// 26/11/11 manoharan salesperson to be set
				sql1 = "select curr_code,cust_code__bil,price_list,trans_mode, price_list__clg, sales_pers from customer where cust_code = ?";
				// end 26/11/11 manoharan salesperson to be set
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,custCd);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{ 
					if(currCode == null)
					{
						currCode = rs2.getString("curr_code") == null ? " " : rs2.getString("curr_code");
					}	
					custCodeBill=rs2.getString("cust_code__bil") == null ? " " : rs2.getString("cust_code__bil");
					transMode=rs2.getString("trans_mode") == null ? " " : rs2.getString("trans_mode");
					if(priceList == null || priceList.trim().length()==0)
					{	
						priceList= rs2.getString("price_list") == null ? " ":rs2.getString("price_list") ;
					}
					if(priceListClg == null || priceListClg.trim().length()==0)
					{	
						priceListClg= rs2.getString("price_list__clg") == null ? " ":rs2.getString("price_list__clg") ;
					}
					// 26/11/11 manoharan salesperson to be set
					if(salesPers == null || salesPers.trim().length()==0)
					{	
						salesPers= rs2.getString("sales_pers") == null ? " ":rs2.getString("sales_pers") ;
					}
					// end 26/11/11 manoharan salesperson to be set
				}
				pstmt2.close();
				pstmt2 = null;					
				rs2.close();
				rs2 = null;

				//added by nandkumar gadkari on 08/06/2020-------------------------start-------------------
				if(priceListSR != null && priceListSR.trim().length()>0)
				{	
					priceList= priceListSR ;
				}
				if(priceListClgSR != null && priceListClgSR.trim().length()>0)
				{	
					priceListClg= priceListClgSR ;
				}
				//added by nandkumar gadkari on 08/06/2020-------------------------end-------------------
				/* 11/02/14 manoharan moved below
				sql1 = "select sales_pers from customer_series where cust_code = ? and item_ser = ?";
				// end 26/11/11 manoharan salesperson to be set
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,custCd);
				pstmt2.setString(2,itemSerDet);
				rs2 = pstmt2.executeQuery();
				String salesPersTmp = "";
				if(rs2.next())
				{ 
					salesPersTmp = rs2.getString("sales_pers") == null ? " " : rs2.getString("sales_pers");
				}
				pstmt2.close();
				pstmt2 = null;					
				rs2.close();
				rs2 = null;
				
				if (salesPersTmp != null && salesPersTmp.trim().length() > 0 )
				{
					salesPers = salesPersTmp;
				}
				*/	
				// added by Pratiksha A on 26/03/2021 cust_code__bill to be set  start
				sql2 = "select cust_code__bill,cust_code__dlv from sreturn_form where tran_id = ?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1, tranId);
				rs2 = pstmt2.executeQuery();
				if (rs2.next() )
				{					
					if(custCodeBillDet == null || custCodeBillDet.trim().length()==0)
					{	
						custCodeBillDet= rs2.getString("cust_code__bill") == null ? " ":rs2.getString("cust_code__bill") ;
					}
					System.out.println("Cust Code Bill"+custCodeBillDet);
					System.out.println("Cust Code Bill Dlv"+custCodeDlv);
				}
				pstmt2.close();
				pstmt2 = null;	
				rs2.close();
				rs2 =null;
				// added by Pratiksha A on 26/03/2021 cust_code__bill to be set   end
				
				sql2 ="select unit,unit__rate from item where item_code = ?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1,itemCode);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					
					unit = checkNull(rs2.getString("unit"));
				    unitRate = checkNull(rs2.getString("unit__rate"));
				    System.out.println("Unit"+unit);
				    System.out.println("Unit Rate"+unitRate);					
				}
				pstmt2.close();
				rs2.close();
								
				//added by nandkumar gadkari on 10/12/19-------------start-------------------'				
				tempSplitCode= "";
				srfSplitKey=distCommom.getDisparams("999999", "SRF_SPLIT_KEY", conn);
				if (srfSplitKey != null && srfSplitKey.trim().length() > 0) {
					srfSplitKeyListArr = srfSplitKey.split(",");
					ArrayList<String> srfSplitKeyList = new ArrayList<String>(Arrays.asList(srfSplitKeyListArr));									
					for(String key : srfSplitKeyList)
					{
						if("item_ser".equalsIgnoreCase(key))
						{
							docValue =itemSerDet;
						}
						if("invoice_id".equalsIgnoreCase(key))
						{
							docValue =invoiceIdDet;
						}
						if("cust_code".equalsIgnoreCase(key))
						{
							docValue =custCd;
						}
						if("cr_term".equalsIgnoreCase(key))
						{
							docValue =crterm;
						}
						if("status".equalsIgnoreCase(key))
						{
							docValue =statusDet;
						}
						// added by Pratiksha A on 26/03/2021  Start
						if("cust_code__bill".equalsIgnoreCase(key))
						{
							docValue =custCodeBillDet;
						} 
						// added by Pratiksha A on 26-03-2021 End
						if (tempSplitCode != "" && tempSplitCode.trim().length() > 0)
						{
							tempSplitCode = tempSplitCode + "@" + ( docValue == null || docValue.trim().length() == 0 ? "" : docValue);
						}						
						else
						{
							tempSplitCode = docValue;
						}
					}
					System.out.println("tempSplitCode["+tempSplitCode+"]");
					System.out.println("srfSplitKeyList["+srfSplitKeyList+"]");//added by Pratiksha A on 26/03/2021
				}
				else
				{
					//added by nandkumar gadkari on 10/12/19-------------end-------------------'
					// 21/11/11 manoharan check whether to consider cr_term, ship_to_zip_code in splitcode
					
					
					if (crCount > 0 )
					{
						//tempSplitCode = itemSerDet+"@"+crterm+"@"+custCd; // **************** this need to be changed as per taro requirement
						tempSplitCode = itemSerDet+"@"+invoiceIdDet+"@"+custCd+"@"+crterm+"@"+statusDet+"@"+custCodeBillDet;//add custCodeBillDet by Pratiksha A on 26/03/2021
					}
					else
					{
						//Added by kunal   D18CKOY001
						//tempSplitCode = itemSerDet
						tempSplitCode = itemSerDet+"@"+invoiceIdDet+"@"+statusDet+"@"+custCodeBillDet;//add custCodeBillDet by Pratiksha A on 26/03/2021
					}
					// end 21/11/11 manoharan check whether to consider cr_term, ship_to_zip_code in splitcode
				}
				if(splitCodeWiseMap.containsKey(tempSplitCode))
				{
					tempList = (ArrayList) splitCodeWiseMap.get(tempSplitCode);
				}
				else
				{
					tempList  = new ArrayList();
				}
				// populate the map with the stock keys
				tempMap = new HashMap();
				
				
				
				tempMap.put("invoice_id", invoiceIdDet);

				tempMap.put("line_no__inv", ("" + lineNoInv));
				//Added by kunal line_no__invtrace  D18CKOY001
				tempMap.put("line_no__invtrace", ("" + lineNoInvtrace));
				
				tempMap.put("item_code", itemCode);
				tempMap.put("quantity", getReqDecimal(quantity, 3));
				
				//if(claimQuantity == null || claimQuantity.trim().length()==0))
				if(claimQuantity == 0)
				{
					tempMap.put("claim_qty", getReqDecimal(quantity, 3));
				}
				else
				{
					tempMap.put("claim_qty", getReqDecimal(claimQuantity, 3));
				}
				
				//if(physicalQuantity == null || physicalQuantity.trim().length()==0)
				if(physicalQuantity == 0)
				{	
					tempMap.put("physical_qty", getReqDecimal(claimQuantity, 3));
				}
				else
				{
					tempMap.put("physical_qty", getReqDecimal(physicalQuantity, 3));
				}
				//Changed by rohan 0n 20-10-11 end
				tempMap.put("net_amt", getReqDecimal(netAmountDet, 3));
				tempMap.put("status", statusDet);
				tempMap.put("reas_code", reasCode);
				tempMap.put("loc_code", locCode);
				tempMap.put("stk_opt", stkOpt);
				tempMap.put("rate", getReqDecimal(rate, 4));
				tempMap.put("lot_no", lotNo);
				tempMap.put("lot_sl", lotSl);
				tempMap.put("tax_class", taxClass);
				tempMap.put("tax_chap", taxChap);
				tempMap.put("tax_env", taxEnv);
				tempMap.put("unit", unit);
				//tempMap.put("ret_rep_flag", retRepFlag);
				tempMap.put("ret_rep_flag", "R");
				System.out.println("Ret rep Flag = =R");
				tempMap.put("eff_net_amt", getReqDecimal(effNetAmountDet, 3));
				tempMap.put("conv__qty_stduom", getReqDecimal(convQtyStdUom, 7));
				tempMap.put("conv__rtuom_stduom", getReqDecimal(convRtUomStdUom, 7));
				tempMap.put("unit__std", unitStd);
				tempMap.put("quantity__stduom", getReqDecimal(quantityStdUom, 3));
				tempMap.put("rate__stduom", getReqDecimal(rateStdUom, 4));

				sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				//tempMap.put("exp_date", sdf.parse(expDate.toString()));
				if(expDate != null)
				{	
					tempMap.put("exp_date", sdf.format(expDate).toString());
				}
				/*else
				{
					tempMap.put("exp_date",null);
					
				}*/
				//tempMap.put("mfg_date", sdf.parse(mfgDate.toString()));
				if(mfgDate !=null)
				{	
					tempMap.put("mfg_date", sdf.format(mfgDate).toString());
				}	
				
				tempMap.put("discount", getReqDecimal(discount, 3));
				tempMap.put("tax_amt", getReqDecimal(taxAmtDet, 3));
				tempMap.put("site_code__mfg", siteCodeMfg);
				tempMap.put("unit__rate", unitRate);
				
				tempMap.put("pack_code", packCode);
				tempMap.put("full_ret", fullRet);
				/*tempMap.put("full_ret", "Y");
				System.out.println("Full ret = Y");
				*/tempMap.put("item_ser", itemSerDet);
				if(rateClg != 0)
				{	
					tempMap.put("rate__clg", getReqDecimal(rateClg, 4));
				}
				else
				{
					tempMap.put("rate__clg", getReqDecimal(rate, 4));				
				}
				tempMap.put("cost_rate", getReqDecimal(costRate, 3));
				tempMap.put("invoice_ref", invoiceRef);// added by nandkumar gadkari on 03/09/19
				tempMap.put("doc_key", docKey);// added by nandkumar gadkari on 03/09/19

				tempList.add(tempMap);

				if(splitCodeWiseMap.containsKey(tempSplitCode))
				{
					splitCodeWiseMap.put(tempSplitCode,tempList);
				}
				else
				{
					splitCodeWiseMap.put(tempSplitCode,tempList);
				}

			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			Set setItem = splitCodeWiseMap.entrySet();
			tempList =  null;
			Iterator itrItem = setItem.iterator();
			while(itrItem.hasNext())
			{
				Map.Entry itemMapEntry = (Map.Entry)itrItem.next();
				splitCode = (String)itemMapEntry.getKey();
				System.out.println("splitCode---"+splitCode);
				System.out.println("Length---"+splitCode.split("@").length);
				//String tempstr[] = splitCode.split("$");
				//itemSer = checkNull(tempstr[0]);
				
				if(splitCode.split("@").length == 1)// added by nandkumar gadkari on 12/12/19
				{	
					itemSer1 = checkNull(splitCode.split("@")[0]);
				}	
				if(splitCode.split("@").length > 1)// change condition !=0 to grater than 1 by nandkumar gadkari on 12/12/19
				{	
					itemSer1 = checkNull(splitCode.split("@")[0]);
					invoice1 = checkNull(splitCode.split("@")[1]);
					custCodeBill1 =checkNull(splitCode.split("@")[2]);//added by Pratiksha A on 26/03/2021
				}	
				//crterm = checkNull(tempstr[1]);
				//shiptozip = checkNull(tempstr[2]);
				//itemSer = splitCode; //******************* to be changed to ge other part of key
				tempList = (ArrayList)splitCodeWiseMap.get(splitCode);
				// build the sales return xml here
				/////////////////////////////////////////////////////////////////////////
				xmlBuff = null;
				xmlBuff = new StringBuffer();
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("salesreturn").append("]]></objName>");
				//xmlBuff.append("<objName><![CDATA[").append("salesreturn_split_tr").append("]]></objName>");
				xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
				xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuff.append("<description>").append("Header0 members").append("</description>");

				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"salesreturn\" objContext=\"1\">");
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");

				//currDate = new java.sql.Timestamp(System.currentTimeMillis());
				sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

				//xmlBuff.append("<tran_date><![CDATA["+ sdf.parse(currDate.toString()) +"]]></tran_date>");
				//xmlBuff.append("<cust_code__bill><![CDATA["+ custCodeBill+"]]></cust_code__bill>");//commented  by pratiksha on 26/03/2021
				xmlBuff.append("<cust_code__bill><![CDATA["+ custCodeBill1+"]]></cust_code__bill>");//added by pratiksha on 26/03/2021
				xmlBuff.append("<trans_mode><![CDATA["+ transMode+"]]></trans_mode>");
        		//xmlBuff.append("<cust_code__dlv><![CDATA["+ custCode +"]]></cust_code__dlv>");//commented by pratiksha on 26/03/2021
        		xmlBuff.append("<cust_code__dlv><![CDATA["+ custCodeDlv +"]]></cust_code__dlv>");//added by pratiksha on 26/03/2021
				
			//	xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currDate).toString() +"]]></tran_date>");// current date commented and tran date set  by nandkumar gadkari on 16/01/19
        		xmlBuff.append("<tran_date><![CDATA["+ sdf.format(tranDate).toString() +"]]></tran_date>");
        		xmlBuff.append("<claim_date><![CDATA["+ sdf.format(tranDate).toString() +"]]></claim_date>");
	//claim_date added by nandkumar gadkari  on 16/01/19
        		//xmlBuff.append("<tran_type><![CDATA["+ " " +"]]></tran_type>");
				if( tranType == null )
				{
					System.out.println("Tran Type space");
					tranType = " ";
				}
				xmlBuff.append("<tran_type><![CDATA["+ tranType +"]]></tran_type>");
				//xmlBuff.append("<eff_date><![CDATA["+ sdf.parse(effDate.toString()) +"]]></eff_date>");
				xmlBuff.append("<eff_date><![CDATA["+ sdf.format(effDate).toString() +"]]></eff_date>");
				xmlBuff.append("<invoice_id><![CDATA["+ invoice1 +"]]></invoice_id>");

				xmlBuff.append("<cust_code><![CDATA["+ custCode +"]]></cust_code>");
				//************** to get the item_ser from
				//xmlBuff.append("<item_ser><![CDATA["+ itemSer +"]]></item_ser>");
				xmlBuff.append("<item_ser><![CDATA["+ itemSer1 +"]]></item_ser>");

				xmlBuff.append("<ret_opt><![CDATA["+ retOpt +"]]></ret_opt>"); //change done by Kunal Mandhre on 20/06/12   
				//xmlBuff.append("<ret_opt><![CDATA["+ "C" +"]]></ret_opt>");
				System.out.println("Ret Option = C");
				xmlBuff.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
				xmlBuff.append("<proj_code><![CDATA["+ projCode +"]]></proj_code>");
				xmlBuff.append("<anal_code><![CDATA["+ analCode +"]]></anal_code>");
				xmlBuff.append("<remarks><![CDATA["+ remarks +"]]></remarks>");
				xmlBuff.append("<full_ret><![CDATA["+ fullRetHdr +"]]></full_ret>");//change in variable name by nandkumar gadkari on 03/07/19
				//xmlBuff.append("<full_ret><![CDATA["+ "Y" +"]]></full_ret>");
				
				xmlBuff.append("<curr_code><![CDATA["+ currCode +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+ exchRate +"]]></exch_rate>");
				xmlBuff.append("<cust_ref><![CDATA["+ custRef +"]]></cust_ref>");
				//xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
				//System.out.println("XML 0 append net");
				xmlBuff.append("<tran_code><![CDATA[" + tranCode + "]]></tran_code>");
				xmlBuff.append("<lr_no><![CDATA[" + lrNo + "]]></lr_no>");
				
				System.out.println("going to enter in if stmt");
				if( lrDate!=null)
                {
					System.out.println("Enter to enter in if stmt");
					xmlBuff.append("<lr_date><![CDATA["+ sdf.format(lrDate).toString() +"]]></lr_date>");
					System.out.println("Mayur done to enter in if stmt");
                }
				xmlBuff.append("<lorry_no><![CDATA[" + lorryNo + "]]></lorry_no>");
				xmlBuff.append("<frt_amt><![CDATA[" + frtAamount + "]]></frt_amt>");
				xmlBuff.append("<trans_mode><![CDATA[" + transMode + "]]></trans_mode>");
				xmlBuff.append("<bank_charges><![CDATA[" + bankCharges + "]]></bank_charges>");
				//**************** currency_code__bc from finentity
				//xmlBuff.append("<curr_code__bc><![CDATA[" + currCodeBC + "]]></curr_code__bc>");
				if(currCodeBC == null)
				{
					xmlBuff.append("<curr_code__bc><![CDATA[" + currCode + "]]></curr_code__bc>");
				}
				else
				{
					xmlBuff.append("<curr_code__bc><![CDATA[" + currCodeBC + "]]></curr_code__bc>");
				}
				xmlBuff.append("<price_list><![CDATA[" + priceList + "]]></price_list>");
				//xmlBuff.append("<eff_net_amt><![CDATA[0]]></eff_net_amt>");
				//System.out.println("XML 0 append effec net");
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
				if(currDate !=null)
				{
					System.out.println("Enter to enter in if1 stmt");
				    //xmlBuff.append("<tax_date><![CDATA["+ sdf.parse(currDate.toString()) +"]]></tax_date>");
				    xmlBuff.append("<tax_date><![CDATA["+ sdf.format(currDate).toString() +"]]></tax_date>");
				    System.out.println("Enter to end in if1 stmt");
				}
				//xmlBuff.append("<tax_date><![CDATA["+ currDate == null?null:sdf.format(.toString()) +"]]></tax_date>");
                //SimpleDateFormat form = new SimpleDateFormat();
               // String value1 = form.format(currDate);
				//System.out.println("Rohan currDate date"+ value);
				//xmlBuff.append("<tax_date><![CDATA["+value1+"]]></tax_date>");

				xmlBuff.append("<price_list__clg><![CDATA[" + priceListClg + "]]></price_list__clg>");
				xmlBuff.append("<adj_misc_crn><![CDATA[NA]]></adj_misc_crn>");
				xmlBuff.append("<chg_user><![CDATA[" + userId + "]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA[" + termId + "]]></chg_term>");

				//currDate = new java.sql.Timestamp(System.currentTimeMillis());
				sdf = null;
				sdf = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
				xmlBuff.append("<chg_date><![CDATA["+ sdf.format(currDate) +"]]></chg_date>");
				//xmlBuff.append("<chg_date><![CDATA["+ currDate == null?null:sdf.format(currDate.toString()) +"]]></chg_date>");
				//xmlBuff.append("<chg_date><![CDATA["+value1+"]]></chg_date>");
				// 11/02/14 manoharan sales person to be taken as per the new division, taken from out sode loop
				// 26/11/11 manoharan salesperson to be set
				sql1 = "select sales_pers from customer_series where cust_code = ? and item_ser = ?";
				// end 26/11/11 manoharan salesperson to be set
				pstmt2 = conn.prepareStatement(sql1);
				pstmt2.setString(1,custCode);
				pstmt2.setString(2,itemSer1);
				rs2 = pstmt2.executeQuery();
				String salesPersTmp = "";
				if(rs2.next())
				{ 
					salesPersTmp = rs2.getString("sales_pers") == null ? " " : rs2.getString("sales_pers");
				}
				pstmt2.close();
				pstmt2 = null;					
				rs2.close();
				rs2 = null;
				
				if (salesPersTmp != null && salesPersTmp.trim().length() > 0 )
				{
					salesPers = salesPersTmp;
				}

				xmlBuff.append("<sales_pers><![CDATA[" + salesPers + "]]></sales_pers>");
				// end 26/11/11 manoharan salesperson to be set
				//01112012 manoj sharma  ret_ref field to be set
				xmlBuff.append("<ret_ref><![CDATA[" + retRef + "]]></ret_ref>");
				//end 01112012 manoj sharma  ret_ref field to be set
				//08/07/2013 ritesh tiwari form_no field to be set
				xmlBuff.append("<form_no><![CDATA["+ tranId +"]]></form_no>");
				xmlBuff.append("<reas_code><![CDATA[" + reasCodeH + "]]></reas_code>");
				//end 08/07/2013 ritesh tiwari form_no field to be set
				xmlBuff.append("<wf_status><![CDATA[O]]></wf_status>");//added by Sanjaya/kiran on 05/AUG/13   
				xmlBuff.append("<site_code__dlv><![CDATA["+siteCodeDlv+"]]></site_code__dlv>"); //added by kunal on 30/JUL/13
				xmlBuff.append("</Detail1>");
				lineNo = 0;
				for (int itemCtr = 0; itemCtr < tempList.size(); itemCtr++)
				{
					lineNo++;
					tempMap = (HashMap)tempList.get(itemCtr);

					invoiceIdDet = "";
					itemCode = "";
					statusDet = "";
					reasCode = "";
					locCode = "";
					stkOpt = "";
					lotNo = "";
					lotSl = "";
					taxClass = "";
					taxChap = "";
					taxEnv = "";
					unit = "";
					retRepFlag = "";
					unitStd = "";
					siteCodeMfg = "";
					unitRate = "";
					packCode = "";
					fullRet = "";

					lineNoInv = 0;
					quantity = 0;
					netAmountDet = 0;
					rate = 0;
					effNetAmountDet = 0;
					convQtyStdUom = 0;
					convRtUomStdUom = 0;
					quantityStdUom = 0;
					rateStdUom = 0;
					discount = 0;
					taxAmtDet = 0;
					rateClg = 0;
					costRate = 0;

					expDate = null;
					mfgDate = null;


					xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"salesreturn\" objContext=\"2\">");
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<tran_id/>");
					// ********** if char leading space to be added
					xmlBuff.append("<line_no>" + lineNo + "</line_no>");
					if ( tempMap.get("invoice_id") != null )
					{
						xmlBuff.append("<invoice_id>" + (String)tempMap.get("invoice_id") + "</invoice_id>");
					}
					else
					{
						xmlBuff.append("<invoice_id/>");
					}
					if ( tempMap.get("line_no__inv") != null )
					{
						xmlBuff.append("<line_no__inv>" + (String)tempMap.get("line_no__inv") + "</line_no__inv>");
					}
					else
					{
						xmlBuff.append("<line_no__inv/>");
					}
					//Added by kunal line_no__invtrace  D18CKOY001
					if ( tempMap.get("line_no__invtrace") != null )
					{
						xmlBuff.append("<line_no__invtrace>" + (String)tempMap.get("line_no__invtrace") + "</line_no__invtrace>");
					}
					else
					{
						xmlBuff.append("<line_no__invtrace/>");
					}
					if ( tempMap.get("item_code") != null )
					{
						xmlBuff.append("<item_code>" + (String)tempMap.get("item_code") + "</item_code>");
					}
					else
					{
						xmlBuff.append("<item_code/>");
					}
					if ( tempMap.get("quantity") != null )
					{
						xmlBuff.append("<quantity>" + (String)tempMap.get("quantity") + "</quantity>");
					}
					else
					{
						xmlBuff.append("<quantity><![CDATA[0]]></quantity>");
					}
					//Chnaged by Rohan on 20-10-2011 start
					if ( tempMap.get("claim_qty") != null )
					{
						xmlBuff.append("<claim_qty>" + (String)tempMap.get("claim_qty") + "</claim_qty>");
					}
					else
					{
						xmlBuff.append("<claim_qty><![CDATA[0]]></claim_qty>");
					}
					
					if ( tempMap.get("physical_qty") != null )
					{
						xmlBuff.append("<physical_qty>" + (String)tempMap.get("physical_qty") + "</physical_qty>");
					}
					else
					{
						xmlBuff.append("<physical_qty><![CDATA[0]]></physical_qty>");
					}
					
					//Changed by Rohan on 20-10-2011 end
					if ( tempMap.get("status") != null )
					{
						xmlBuff.append("<status>" + (String)tempMap.get("status") + "</status>");
					}
					else
					{
						xmlBuff.append("<status/>");
					}
					if ( tempMap.get("reas_code") != null )
					{
						xmlBuff.append("<reas_code>" + (String)tempMap.get("reas_code") + "</reas_code>");
					}
					else
					{
						xmlBuff.append("<reas_code/>");
					}
					if ( tempMap.get("loc_code") != null )
					{
						xmlBuff.append("<loc_code>" + (String)tempMap.get("loc_code") + "</loc_code>");
					}
					else
					{
						xmlBuff.append("<loc_code/>");
					}
					if ( tempMap.get("lot_no") != null )
					{
						xmlBuff.append("<lot_no>" + (String)tempMap.get("lot_no") + "</lot_no>");
					}
					else
					{
						xmlBuff.append("<lot_no/>");
					}
					if ( tempMap.get("lot_sl") != null )
					{
						xmlBuff.append("<lot_sl>" + (String)tempMap.get("lot_sl") + "</lot_sl>");
					}
					else
					{
						xmlBuff.append("<lot_sl/>");
					}
					if ( tempMap.get("net_amt") != null )
					{
						xmlBuff.append("<net_amt><![CDATA[" + (String)tempMap.get("net_amt") + "]]></net_amt>");
						System.out.println("temp put net amt"+(String)tempMap.get("net_amt"));
					}
					else
					{
						xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
					}
					if ( tempMap.get("stk_opt") != null )
					{
						xmlBuff.append("<stk_opt><![CDATA[" + (String)tempMap.get("stk_opt") + "]]></stk_opt>");
					}
					else
					{
						xmlBuff.append("<stk_opt/>");
					}
					if ( tempMap.get("rate") != null )
					{
						xmlBuff.append("<rate><![CDATA[" + (String)tempMap.get("rate") + "]]></rate>");
					}
					else
					{
						xmlBuff.append("<rate><![CDATA[0]]></rate>");
					}
					if ( tempMap.get("tax_class") != null )
					{
						xmlBuff.append("<tax_class><![CDATA[" + (String)tempMap.get("tax_class") + "]]></tax_class>");
					}
					else
					{
						xmlBuff.append("<tax_class/>");
					}
					if ( tempMap.get("tax_chap") != null )
					{
						
						xmlBuff.append("<tax_chap><![CDATA[" + (String)tempMap.get("tax_chap") + "]]></tax_chap>");
						
					}
					else
					{
						xmlBuff.append("<tax_chap/>");
					}
					if ( tempMap.get("tax_env") != null )
					{
						xmlBuff.append("<tax_env><![CDATA[" + (String)tempMap.get("tax_env") + "]]></tax_env>");
					}
					else
					{
						xmlBuff.append("<tax_env/>");
					}
					if ( tempMap.get("unit") != null )
					{
						xmlBuff.append("<unit><![CDATA[" + (String)tempMap.get("unit") + "]]></unit>");
					}
					else
					{
						xmlBuff.append("<unit/>");
					}
					if ( tempMap.get("ret_rep_flag") != null )
					{
						xmlBuff.append("<ret_rep_flag><![CDATA[" + (String)tempMap.get("ret_rep_flag") + "]]></ret_rep_flag>");
					}
					else
					{
						xmlBuff.append("<ret_rep_flag/>");
					}
					if ( tempMap.get("eff_net_amt") != null )
					{
						xmlBuff.append("<eff_net_amt><![CDATA[" + (String)tempMap.get("eff_net_amt") + "]]></eff_net_amt>");
						System.out.println("temp put  effective net amt"+(String)tempMap.get("eff_net_amt"));
					}
					else
					{
						xmlBuff.append("<eff_net_amt><![CDATA[0]]></eff_net_amt>");
					}
					if ( tempMap.get("conv__qty_stduom") != null )
					{
						xmlBuff.append("<conv__qty_stduom><![CDATA[" + (String)tempMap.get("conv__qty_stduom") + "]]></conv__qty_stduom>");
					}
					else
					{
						xmlBuff.append("<conv__qty_stduom><![CDATA[1]]></conv__qty_stduom>");
					}
					if ( tempMap.get("conv__rtuom_stduom") != null )
					{
						xmlBuff.append("<conv__rtuom_stduom><![CDATA[" + (String)tempMap.get("conv__rtuom_stduom") + "]]></conv__rtuom_stduom>");
					}
					else
					{
						xmlBuff.append("<conv__rtuom_stduom><![CDATA[1]]></conv__rtuom_stduom>");
					}
					if ( tempMap.get("unit__std") != null )
					{
						xmlBuff.append("<unit__std><![CDATA[" + (String)tempMap.get("unit__std") + "]]></unit__std>");
					}
					else
					{
						xmlBuff.append("<unit__std><![CDATA[" + (String)tempMap.get("unit") + "]]></unit__std>");
					}
					if ( tempMap.get("quantity__stduom") != null )
					{
						xmlBuff.append("<quantity__stduom><![CDATA[" + (String)tempMap.get("quantity__stduom") + "]]></quantity__stduom>");
					}
					else
					{
						xmlBuff.append("<quantity__stduom><![CDATA[0]]></quantity__stduom>");
					}
					if ( tempMap.get("rate__stduom") != null )
					{
						xmlBuff.append("<rate__stduom><![CDATA[" + (String)tempMap.get("rate__stduom") + "]]></rate__stduom>");
					}
					else
					{
						xmlBuff.append("<rate__stduom><![CDATA[0]]></rate__stduom>");
					}
					if ( tempMap.get("exp_date") != null )
					{
						xmlBuff.append("<exp_date><![CDATA[" + (String)tempMap.get("exp_date") + "]]></exp_date>");
					}
					else
					{
						xmlBuff.append("<exp_date/>");
					}
					if ( tempMap.get("discount") != null )
					{
						xmlBuff.append("<discount><![CDATA[" + (String)tempMap.get("discount") + "]]></discount>");
					}
					else
					{
						xmlBuff.append("<discount><![CDATA[0]]></discount>");
					}
					xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
					if ( tempMap.get("site_code__mfg") != null )
					{
						xmlBuff.append("<site_code__mfg><![CDATA[" + (String)tempMap.get("site_code__mfg") + "]]></site_code__mfg>");
					}
					else
					{
						xmlBuff.append("<site_code__mfg/>");
					}
					if ( tempMap.get("mfg_date") != null )
					{
						xmlBuff.append("<mfg_date><![CDATA[" + (String)tempMap.get("mfg_date") + "]]></mfg_date>");
					}
					else
					{
						xmlBuff.append("<mfg_date/>");
					}
					if ( tempMap.get("unit__rate") != null )
					{
						xmlBuff.append("<unit__rate><![CDATA[" + (String)tempMap.get("unit__rate") + "]]></unit__rate>");
					}
					else
					{
						xmlBuff.append("<unit__rate><![CDATA[" + (String)tempMap.get("unit") + "]]></unit__rate>");
					}
					if ( tempMap.get("pack_code") != null )
					{
						xmlBuff.append("<pack_code><![CDATA[" + (String)tempMap.get("pack_code") + "]]></pack_code>");
					}
					else
					{
						xmlBuff.append("<pack_code/>");
					}
					if ( tempMap.get("full_ret") != null )
					{
						xmlBuff.append("<full_ret><![CDATA[" + (String)tempMap.get("full_ret") + "]]></full_ret>");
					}
					else
					{
						xmlBuff.append("<full_ret/>");
					}
					if ( tempMap.get("item_ser") != null )
					{
						xmlBuff.append("<item_ser><![CDATA[" + (String)tempMap.get("item_ser") + "]]></item_ser>");
					}
					else
					{
						xmlBuff.append("<item_ser/>");
					}
					if ( tempMap.get("rate__clg") != null )
					{
						xmlBuff.append("<rate__clg><![CDATA[" + (String)tempMap.get("rate__clg") + "]]></rate__clg>");
					}
					else
					{
						xmlBuff.append("<rate__clg><![CDATA[0]]></rate__clg>");
					}
					if ( tempMap.get("cost_rate") != null )
					{
						xmlBuff.append("<cost_rate><![CDATA[" + (String)tempMap.get("cost_rate") + "]]></cost_rate>");
					}
					else
					{
						xmlBuff.append("<cost_rate><![CDATA[0]]></cost_rate>");
					}
					// added by nandkumar gadkari on 03/09/19---------start-------------
					if ( tempMap.get("invoice_id") == null || (String)tempMap.get("invoice_id") == null || ((String)tempMap.get("invoice_id")).trim().length() == 0 )
					{
						if ( tempMap.get("invoice_ref") != null   )
						{
							xmlBuff.append("<invoice_ref><![CDATA["+ (String)tempMap.get("invoice_ref") +"]]></invoice_ref>");
							
						}
						if ( tempMap.get("doc_key") != null   )
						{
							xmlBuff.append("<doc_key><![CDATA["+ (String)tempMap.get("doc_key") +"]]></doc_key>");
						}
					}
					else
					{
						if ( tempMap.get("doc_key") != null   )
						{
							xmlBuff.append("<doc_key><![CDATA["+ (String)tempMap.get("doc_key") +"]]></doc_key>");
						}
					}
					// added by nandkumar gadkari on 03/09/19---------end-------------
					//sreturn_hdr.setitem(mtot_row, "eff_net_amt", mtot_eff_amt)
					//sreturn_hdr.setitem(mtot_row, "tax_amt", mtot_tax_amt)
					//sreturn_hdr.setitem(mtot_row, "net_amt", mtot_net_amt)

					xmlBuff.append("</Detail2>");

				}

				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");

				xmlString = xmlBuff.toString();

				retString = saveData(siteCode,xmlString,userId,conn);

				if (retString.indexOf("Success") > -1)
				{
					//conn.commit();
					int  d =retString.indexOf("<TranID>");
					int f =	 retString.indexOf("</TranID>");
					String tempTranId = retString.substring(d+8,f);
					System.out.println("Sales Return tran_id [" + tempTranId + "]");
				}
				else
				{
					System.out.println("Sales return not Generated retString [" + retString + "]");
					break;
				}
			} // for each split key
		}
		catch(Exception e)
		{
			try
			{
				System.out.println("--ROLLED----");
				conn.rollback();
			}
			catch (Exception e1)
			{
			}
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
				if(pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
			}
			catch(Exception e)
			{
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result [" + retString + "]");
		return retString;
	}
	private String getReqDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return decFormat.format(actVal);
	}
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		  System.out.println("saving data...........");
		  InitialContext ctx = null;
		  String retString = null;
		  MasterStatefulLocal masterStateful = null; // for ejb3
		  try
		  {
			   AppConnectParm appConnect = new AppConnectParm();
			   ctx = new InitialContext(appConnect.getProperty());
			   masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			   String [] authencate = new String[2];
			   authencate[0] = userId;
			   authencate[1] = "";
			   System.out.println("xmlString to masterstateful [" + xmlString + "]");
			   retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
		 }
		 catch(ITMException itme)
		 {
		   	System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		 }
		 catch(Exception e)
		 {
		  	System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		 }
	  	 return retString;
	}

	//To check Null String
	private String checkNull( String input )
	{
		if( input == null )
		{
			input = "";
		}
		else
		{
			input = input.trim();
		}
		return input;
	}

}