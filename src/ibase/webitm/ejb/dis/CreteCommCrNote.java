package ibase.webitm.ejb.dis;

import ibase.planner.utility.ITMException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.fin.FinCommon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CreteCommCrNote 
{

	public String creteCommCrNote (String invoiceId, Connection conn) throws ITMException 
	{



		String   ls_cust_code="", ls_site_code="", ls_finent="", ls_itemser="", ls_acct_code_ar="", ls_cctr_code_ar="", ls_curr_code="", ls_crterm="";
		String keystr="", ls_errcode="", ls_tran_id="", ls_disc_type="", ls_tran_type="", ls_rndstr="", ls_rndoff="", ls_acct_code="", findstring="";
		String ls_cctr_code="";
		double 	lc_amount=0, lc_net_amt=0, lc_tot_amount=0, lc_discount=0, lc_bal_amt=0,	lc_exch_rate=0;
		int 	ll_currow=0 ,ll_hdr_row=0, ll_cnt=0, ll_foundrow=0;
		Timestamp ldt_tran_date = null;

		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String errString = "";
		String sql = ""; String sql1 = ""; String sql2 = "",userId="",termId="",loginEmpCode="";

		StringBuffer xmlBuff = null;

		FinCommon finCommon = new FinCommon();

		try
		{
			Date currentDate = new Date();
			E12GenericUtility genericUtility = new E12GenericUtility();
			Calendar currentDate1 = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate1.getTime());

			//nvo_datastore lds_misc_drcr_hdr, lds_misc_drcr_det, lds_misc_drcr_rcpinv
			//nvo_business_object_misc_drcrrcp nvo_misc_drcr

			/*if itm_structure.fin_comp > 0 then 
				itm_app_server[itm_structure.fin_comp].createinstance(nvo_misc_drcr)
			else	
				nvo_misc_drcr = create nvo_business_object_misc_drcrrcp
			end if	
			 */
			//lds_misc_drcr_hdr = create nvo_datastore    
			//lds_misc_drcr_hdr.dataobject = 'd_misc_drcr_rcp_edit'
			//lds_misc_drcr_hdr.settransobject(sqlca)

			//lds_misc_drcr_det = create nvo_datastore
			//lds_misc_drcr_det.dataobject = 'd_misc_drcr_rdet_edit'
			//lds_misc_drcr_det.settransobject(sqlca)

			//lds_misc_drcr_rcpinv = create nvo_datastore
			//lds_misc_drcr_rcpinv.dataobject = 'd_misc_drcr_rcpinv_brow'
			//lds_misc_drcr_rcpinv.settransobject(sqlca)

			sql = " select site_code, cust_code, tran_date, cust_code, net_amt " +
					//     " into :ls_site_code, :ls_cust_code, :ldt_tran_date, :ls_cust_code, :lc_net_amt " +
					" from invoice where invoice_id = :as_invoice_id";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				ls_site_code= checknull(rs.getString("site_code"));
				ls_cust_code= checknull(rs.getString("cust_code"));
				ldt_tran_date= rs.getTimestamp("tran_date");
				lc_net_amt= rs.getDouble("net_amt");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;



			sql = " select count(1) from inv_disc_hdr a, inv_disc_det b " +
					" where a.tran_id = b.tran_id " +
					" and a.cust_code = ? " +
					" and ? between a.date_from and a.date_to " +
					" and ? >= b.min_inv_amt " +
					" and ? <= b.max_inv_amt ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,ls_cust_code);
			pstmt.setTimestamp(2,ldt_tran_date);
			pstmt.setDouble(3,lc_net_amt);
			pstmt.setDouble(4,lc_net_amt);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				ll_cnt= rs.getInt(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;




			sql = " select fin_entity, item_ser, acct_code__ar, cctr_code__ar, curr_code, exch_rate, cr_term " +
					//	  " into  :ls_finent, :ls_itemser, :ls_acct_code_ar, :ls_cctr_code_ar, :ls_curr_code, :lc_exch_rate, :ls_crterm " +
					" from invoice where invoice_id = :as_invoice_id ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,ls_cust_code);
			pstmt.setTimestamp(2,ldt_tran_date);
			pstmt.setDouble(3,lc_net_amt);
			pstmt.setDouble(4,lc_net_amt);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				ls_finent= checknull(rs.getString("fin_entity"));
				ls_itemser= checknull(rs.getString("item_ser"));
				ls_acct_code_ar= rs.getString("acct_code__ar");
				ls_cctr_code_ar= rs.getString("cctr_code__ar");
				ls_curr_code= checknull(rs.getString("curr_code"));
				lc_exch_rate= rs.getDouble("exch_rate");
				ls_crterm= checknull(rs.getString("cr_term"));
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;


			// set headet data

			xmlBuff = new StringBuffer();
			System.out.println("--XML CREATION !!!!!--");
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("misc_drcr_rcp_cr").append("]]></objName>"); //W_MISC_DRCR_RCP_CR 
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
			/*---Detail1 screen,starts----*/
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"misc_drcr_rcp_cr\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");


			//ll_hdr_row = lds_misc_drcr_hdr.insertrow(0)
			xmlBuff.append("<tran_date><![CDATA["+ ldt_tran_date +"]]></tran_date>");
			xmlBuff.append("<site_code><![CDATA["+ ls_site_code +"]]></site_code>");
			xmlBuff.append("<fin_entity><![CDATA["+ ls_finent +"]]></fin_entity>");
			xmlBuff.append("<sundry_type><![CDATA[C]]></sundry_type>");
			xmlBuff.append("<sundry_code><![CDATA["+ ls_cust_code +"]]></tran_date>");
			xmlBuff.append("<item_ser><![CDATA["+ ls_itemser +"]]></item_ser>");
			xmlBuff.append("<acct_code><![CDATA["+ ls_acct_code_ar +"]]></acct_code>");
			xmlBuff.append("<cctr_code><![CDATA["+ ls_cctr_code_ar +"]]></cctr_code>");
			xmlBuff.append("<eff_date><![CDATA["+ ldt_tran_date +"]]></eff_date>");
			xmlBuff.append("<due_date><![CDATA["+ ldt_tran_date +"]]></due_date>");
			xmlBuff.append("<curr_code><![CDATA["+ ls_curr_code +"]]></curr_code>");
			xmlBuff.append("<exch_rate><![CDATA["+ lc_exch_rate +"]]></exch_rate>");
			xmlBuff.append("<drcr_flag><![CDATA[C]]></drcr_flag>");
			xmlBuff.append("<tran_ser><![CDATA[MDRCRC]]></tran_ser>");
			xmlBuff.append("<remarks><![CDATA[Auto Generated Credit Note from Invoice "+ invoiceId.trim() +"]]></remarks>");
			xmlBuff.append("<chg_date><![CDATA["+ sdf.format(currentDate).toString()  +"]]></chg_date>");
			xmlBuff.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA["+ termId +"]]></chg_term>");
			xmlBuff.append("<emp_code__aprv><![CDATA["+ loginEmpCode +"]]></emp_code__aprv>");
			xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");

			ls_tran_type = checknull(finCommon.getFinparams("999999", "COMM_CRNOTE_TRANTYPE",conn));

			if( "NULLFOUND".equalsIgnoreCase(ls_tran_type) || ls_tran_type == null  || ls_tran_type.trim().length() == 0 ) 
			{
				ls_tran_type = "";
			}
			xmlBuff.append("<tran_type><![CDATA["+ ls_tran_type +"]]></tran_type>");

			ls_rndoff = checknull(finCommon.getFinparams("999999", "MDRCRC-RND",conn));
			if(!"NULLFOUND".equalsIgnoreCase(ls_rndoff) )
			{
				xmlBuff.append("<rnd_off><![CDATA["+ ls_rndoff.trim() +"]]></rnd_off>");
			}

			ls_rndoff = checknull(finCommon.getFinparams("999999", "MDRCRC-RNDTO",conn));
			if(!"NULLFOUND".equalsIgnoreCase(ls_rndoff) )
			{
				xmlBuff.append("<rnd_to><![CDATA["+ ls_rndoff.trim() +"]]></rnd_to>");
			}
			xmlBuff.append("<tran_date><![CDATA["+ ldt_tran_date +"]]></tran_date>");

			xmlBuff.append("<adj_misc_crn><![CDATA[MC]]></adj_misc_crn>");

/*			//Header amount
			lds_misc_drcr_hdr.setitem(ll_hdr_row,"amount",lc_tot_amount)
			lds_misc_drcr_hdr.setitem(ll_hdr_row,"amount__bc",lc_tot_amount*lc_exch_rate)
*/
			xmlBuff.append("<amount><![CDATA["+ lc_tot_amount +"]]></amount>");
			xmlBuff.append("<amount__bc><![CDATA["+(lc_tot_amount*lc_exch_rate)+"]]></amount__bc>");
			
			
			
			
			xmlBuff.append("</Detail1>");
			// end

			// detail data start

			int cnt=0, lineNo=1,lineNo3=1;;
			lc_tot_amount = 0;
			//declare cur_inv_disc cursor for
			sql = " select b.disc_type, b.discount, b.acct_code, b.cctr_code " +
					" from inv_disc_hdr a, inv_disc_det b " +
					" where a.tran_id = b.tran_id " +
					" and a.cust_code = ? " +
					" and ? between a.date_from and a.date_to " +
					" and ? >= b.min_inv_amt " +
					" and ? <= b.max_inv_amt ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,ls_cust_code);
			pstmt.setTimestamp(2,ldt_tran_date);
			pstmt.setDouble(3,lc_net_amt);
			pstmt.setDouble(4,lc_net_amt);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				cnt++;
				ls_disc_type= checknull(rs.getString("disc_type"));
				lc_discount= rs.getDouble("discount");
				ls_acct_code= checknull(rs.getString("acct_code"));
				ls_cctr_code= rs.getString("cctr_code");

				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"misc_drcr_rcp_cr\" objContext=\"2\">");
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"E\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");


				if("F".equalsIgnoreCase(ls_disc_type))
				{		
					lc_amount = lc_discount;
				}
				else  //% - 'P'
				{
					lc_amount = (lc_net_amt * lc_discount / 100);
				}


				lc_tot_amount = lc_tot_amount + lc_amount;

				if( ls_cctr_code == null) 
				{
					ls_cctr_code = " ";
				}


//pending
				//lc_amount = lc_amount + lds_misc_drcr_det.getitemdecimal(ll_foundrow,"amount");
						
				//lds_misc_drcr_det.setitem(ll_foundrow,'amount',lc_amount)
				xmlBuff.append("<amount><![CDATA["+lc_amount+"]]></amount>");
				//lds_misc_drcr_det.setitem(ll_foundrow,'net_amt',lc_amount)
				xmlBuff.append("<net_amt><![CDATA["+lc_amount+"]]></net_amt>");
				xmlBuff.append("</Detail2>");

			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;

			if( cnt == 0)
			{
					
				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"misc_drcr_rcp_cr\" objContext=\"2\">");
				xmlBuff.append("<attribute pkNames=\"\" selected=\"A\" updateFlag=\"E\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				
				xmlBuff.append("<line_no><![CDATA["+lineNo+"]]></line_no>");
				xmlBuff.append("<acct_code><![CDATA["+ls_acct_code+"]]></acct_code>");
				xmlBuff.append("<cctr_code><![CDATA["+ls_cctr_code+"]]></cctr_code>");
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
				xmlBuff.append("<lot_no><![CDATA["+lc_discount+"]]></lot_no>");
				xmlBuff.append("<amount><![CDATA["+lc_amount+"]]></amount>");
				xmlBuff.append("<net_amt><![CDATA["+lc_amount+"]]></net_amt>");
				xmlBuff.append("</Detail2>");
			}

			System.out.println("end of details 2");

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			String xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: retString  :" + xmlString);
//pending
//			errString = saveData(ls_site_code,xmlString,conn);

			System.out.println("Passed xml in  master State full saveData errString["+errString+"]");


			// detail end



			//Amount Adjustment -- Receivables amount has to be adjusted- 
			sql = " select (tot_amt - adj_amt) as lc_bal_amt from receivables " +
				  " where tran_ser = 'S-INV' and ref_no = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lc_bal_amt= rs.getDouble("lc_bal_amt");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
			xmlBuff.append("<Detail3 dbID='' domID=\"1\" objName=\"misc_drcr_rcp_cr\" objContext=\"2\">");
			xmlBuff.append("<attribute pkNames=\"\" selected=\"A\" updateFlag=\"A\" status=\"N\" />");
			xmlBuff.append("<tran_id/>");
			
			xmlBuff.append("<line_no><![CDATA["+lineNo3+"]]></line_no>");
			xmlBuff.append("<ref_ser><![CDATA[S-INV]]></ref_ser>");
			xmlBuff.append("<ref_no><![CDATA["+invoiceId+"]]></ref_no>");
			xmlBuff.append("<adj_amt><![CDATA["+lc_tot_amount+"]]></adj_amt>");
			xmlBuff.append("<ref_bal_amt><![CDATA["+lc_bal_amt+"]]></ref_bal_amt>");
			xmlBuff.append("</Detail3>");
/*			
			
			ll_currow = lds_misc_drcr_rcpinv.insertrow(0)
			lds_misc_drcr_rcpinv.setitem( ll_currow, "tran_id", ls_tran_id)
			lds_misc_drcr_rcpinv.setitem( ll_currow, "line_no", ll_currow)
			lds_misc_drcr_rcpinv.setitem( ll_currow, "ref_ser", 'S-INV')
			lds_misc_drcr_rcpinv.setitem( ll_currow, "ref_no", as_invoice_id)
			lds_misc_drcr_rcpinv.setitem( ll_currow, "adj_amt", lc_tot_amount)
			lds_misc_drcr_rcpinv.setitem( ll_currow, "ref_bal_amt", lc_bal_amt)

			//Header amount
			lds_misc_drcr_hdr.setitem(ll_hdr_row,"amount",lc_tot_amount)
			lds_misc_drcr_hdr.setitem(ll_hdr_row,"amount__bc",lc_tot_amount*lc_exch_rate)

			

			errfound: 
			if isvalid(lds_misc_drcr_hdr) then destroy lds_misc_drcr_hdr
			if isvalid(lds_misc_drcr_det) then destroy lds_misc_drcr_det
			if isvalid(lds_misc_drcr_rcpinv) then destroy lds_misc_drcr_rcpinv
			if isvalid(nvo_misc_drcr) then destroy nvo_misc_drcr

			return ls_errcode		
			
			
			
		*/	
			


		}
		catch( Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{

			try
			{

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

				//conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}

		}
		return errString;

	}


	private String String(double inputStr)
	{
		return ""+inputStr;
	}



	private String checknull(String inputStr)
	{
		if(inputStr==null)
		{
			inputStr="";
		}
		return inputStr;
	}
}
