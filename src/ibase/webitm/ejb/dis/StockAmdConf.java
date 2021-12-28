/**
 * PURPOSE : StockAmdConfcomponent
 * AUTHOR : Sneha Mestry
 * DATE : 12-12-2016
 */

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
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
public class StockAmdConf extends ActionHandlerEJB implements StockAmdConfLocal, StockAmdConfRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
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
			System.out.println("Exception in [StockTransferConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
		System.out.println(" ========= Inside confirm  tranId ============= "+tranId);
		System.out.println("xtraParams ::::::::::::: " + xtraParams);

		String errString = "", sql = "", childNodeName = "", userId = "", errorType = "", errCode = "", ld_conf_date = "";
		boolean isError = false;
		int cnt = 0;
		
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String> errFields = new ArrayList <String>();

		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		
		String ls_confirm = "";

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try
		{
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currDate = new Date();
			ld_conf_date = sdf.format(currDate);
			
			if ( conn == null )
			{
				conn = getConnection();
			}
				
			sql = "SELECT CONFIRMED	FROM STOCK_AMD WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_confirm = checkNullAndTrim(rs.getString("CONFIRMED"));
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
			if(ls_confirm.equalsIgnoreCase("Y"))
			{
				errCode = "VTMCONF1";		
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}
			else
			{
				errCode = retriveStockAmd(tranId, isError, xtraParams, conn);
				
				System.out.println("errCode =========== >> "+ errCode);
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
					errString = itmDBAccess.getErrorString( errFldName, errCode, userId,"",conn );
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
				System.out.println("Exception "+e.getMessage());
				e.printStackTrace();			
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				isError = true;
				e1.printStackTrace();
				throw new ITMException(e1);
			}
		}
		finally
		{
			try
			{				
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
				if (conn != null)
				{
					conn.close();
					conn = null;
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
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
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

	public String errorType( Connection conn, String errorCode )
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " select msg_type from messages where msg_no =  ? ";
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

	public String retriveStockAmd(String tranId, boolean isError, String xtraParams, Connection conn) throws Exception
	{
		System.out.println("--------- Inside retriveStockAmd --------------------- ");
		
		String sql = "";
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs1 = null; 
		String ls_errcode = "";
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			Date currDate = new Date();
			java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
			ls_errcode = confirmStockAmd(tranId, isError, conn);
			System.out.println("ls_errcode Inside retriveStockAmd ===========>>"+ls_errcode);
			
			if(ls_errcode.trim().length() == 0)
			{
				sql = "UPDATE STOCK_AMD SET CONFIRMED = ?, CONF_DATE = ? WHERE TRAN_ID = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "Y");
				pstmt.setDate(2, currentDate);
				pstmt.setString(3, tranId);
	            int cnt = pstmt.executeUpdate();
	            
	            if(cnt > 0)
	            {
	            	String ediOption = "";
	                String dataStr = "";
	                sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_stock_amd' ";
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

	                if ( "1".equals(ediOption.trim()) )
	                {
	                    CreateRCPXML createRCPXML = new CreateRCPXML("w_stock_amd","tran_id");
	                    dataStr = createRCPXML.getTranXML( tranId, conn );
	                    System.out.println( "dataStr =[ "+ dataStr + "]" );
	                    Document ediDataDom = genericUtility.parseString(dataStr);
	                    E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
	                    String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_stock_amd", "0", xtraParams, conn);
	                    createRCPXML = null;
	                    e12CreateBatchLoad = null;
	                    if( retString != null && "SUCCESS".equals(retString) )
	                    {
	                        System.out.println("retString from batchload = 	["+retString+"]");
	                    }
	                }
	                
	           	}
	            if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}

		}
		catch(Exception e)
		{
			System.out.println("Exception============>> "+e);
			isError = true;
		}
		finally
		{

			try
			{
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
    			if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
    			
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		
		}
		return ls_errcode;
	}
	
	public String confirmStockAmd(String tranId, boolean isError, Connection conn) throws Exception
	{
		System.out.println("----------- Inside confirmStockAmd ----------------");
		String sql = "", sql1 = "";
		PreparedStatement pstmt = null,  pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		int cnt = 0;
		
		String ls_item_code = "", ls_site_code = "", ls_loc_code = "", ls_lot_no = "", ls_lot_sl = "", 
				ls_site_code__mfg = "", lc_potency = "", ls_supp_code = "", 
				ls_dim = "", lc_gross_weight = "", lc_tare_weight = "", lc_net_weight = "", lc_conv_qty_stduom = "", 
				ls_considerallocate = "", lc_qtyperart = "", ll_noart = "", ls_pack_code = "", ls_errcode = "", ls_stkopt = "";
		Date ld_mfg_date = null, ld_exp_date = null, ld_retest_date = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			sql = "SELECT ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, MFG_DATE, EXP_DATE, SITE_CODE__MFG, RETEST_DATE, POTENCY_PERC, " +
					"SUPP_CODE__MFG, DIMENSION, GROSS_WEIGHT, TARE_WEIGHT, NET_WEIGHT, CONV__QTY_STDUOM, CONSIDER_ALLOCATE, QTY_PER_ART, " +
					"NO_ART, PACK_CODE FROM	STOCK_AMD WHERE	TRAN_ID = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_item_code = checkNullAndTrim(rs.getString("ITEM_CODE"));
				ls_site_code = checkNullAndTrim(rs.getString("SITE_CODE"));
				ls_loc_code = checkNullAndTrim(rs.getString("LOC_CODE"));
				ls_lot_no = checkNullAndTrim(rs.getString("LOT_NO"));
				ls_lot_sl = checkNullAndTrim(rs.getString("LOT_SL"));
				ld_mfg_date = rs.getDate("MFG_DATE");
				ld_exp_date = rs.getDate("EXP_DATE");
				ls_site_code__mfg = checkNullAndTrim(rs.getString("SITE_CODE__MFG"));
				ld_retest_date = rs.getDate("RETEST_DATE");
				lc_potency = checkNullAndTrim(rs.getString("POTENCY_PERC"));
				ls_supp_code = checkNullAndTrim(rs.getString("SUPP_CODE__MFG"));
				ls_dim = checkNullAndTrim(rs.getString("DIMENSION"));
				lc_gross_weight = checkNullAndTrim(rs.getString("GROSS_WEIGHT"));
				lc_tare_weight = checkNullAndTrim(rs.getString("TARE_WEIGHT"));
				lc_net_weight = checkNullAndTrim(rs.getString("NET_WEIGHT"));
				lc_conv_qty_stduom = checkNullAndTrim(rs.getString("CONV__QTY_STDUOM"));
				ls_considerallocate = checkNullAndTrim(rs.getString("CONSIDER_ALLOCATE"));
				lc_qtyperart = checkNullAndTrim(rs.getString("QTY_PER_ART"));
				ll_noart = checkNullAndTrim(rs.getString("NO_ART"));
				ls_pack_code = checkNullAndTrim(rs.getString("PACK_CODE"));
				

				sql1 = "UPDATE STOCK SET MFG_DATE = ?, EXP_DATE = ?, SITE_CODE__MFG = ?, RETEST_DATE = ?, POTENCY_PERC = ?, SUPP_CODE__MFG = ?, " +
					"DIMENSION = ?, GROSS_WEIGHT = ?, TARE_WEIGHT = ?, NET_WEIGHT = ?, CONV__QTY_STDUOM = ?, CONSIDER_ALLOCATE = ?, " +
					"QTY_PER_ART = ?, NO_ART = ?, GROSS_WT_PER_ART = CASE WHEN ? > 0 THEN (? / ?) ELSE GROSS_WT_PER_ART END, " +
					"TARE_WT_PER_ART = CASE WHEN ? > 0 THEN (? / ?) ELSE TARE_WT_PER_ART END, PACK_CODE = ? " +
					"WHERE	ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO	= ? AND	LOT_SL = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setDate(1, (java.sql.Date) ld_mfg_date);
				pstmt1.setDate(2, (java.sql.Date)  ld_exp_date);
				pstmt1.setString(3, ls_site_code__mfg);
				pstmt1.setDate(4, (java.sql.Date) ld_retest_date);
				pstmt1.setString(5, lc_potency);
				pstmt1.setString(6, ls_supp_code);
				pstmt1.setString(7, ls_dim);
				pstmt1.setString(8, lc_gross_weight);
				pstmt1.setString(9, lc_tare_weight);
				pstmt1.setString(10, lc_net_weight);
				pstmt1.setString(11, lc_conv_qty_stduom);
				pstmt1.setString(12, ls_considerallocate);
				pstmt1.setString(13, lc_qtyperart);
				pstmt1.setString(14, ll_noart);
				pstmt1.setString(15, ll_noart);
				pstmt1.setString(16, lc_gross_weight);
				pstmt1.setString(17, ll_noart);
				pstmt1.setString(18, ll_noart);
				pstmt1.setString(19, lc_tare_weight);
				pstmt1.setString(20, ll_noart);
				pstmt1.setString(21, ls_pack_code);
				pstmt1.setString(22, ls_item_code);
				pstmt1.setString(23, ls_site_code);
				pstmt1.setString(24, ls_loc_code);
				pstmt1.setString(25, ls_lot_no);
				pstmt1.setString(26, ls_lot_sl);
				pstmt1.executeUpdate();
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				
				sql1 = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, ls_item_code);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					ls_stkopt = checkNullAndTrim(rs1.getString("STK_OPT"));
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				
				if(ls_stkopt.equalsIgnoreCase("2"))
				{
					sql1 = "UPDATE STOCK SET MFG_DATE = ?, EXP_DATE = ?, RETEST_DATE = ?, SITE_CODE__MFG = ?, POTENCY_PERC = ?, " +
						"SUPP_CODE__MFG = ?, CONV__QTY_STDUOM = ?, CONSIDER_ALLOCATE = ?, PACK_CODE = ? WHERE ITEM_CODE = ? AND	LOT_NO	= ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setDate(1, (java.sql.Date) ld_mfg_date);
					pstmt1.setDate(2, (java.sql.Date) ld_exp_date);
					pstmt1.setDate(3, (java.sql.Date) ld_retest_date);
					pstmt1.setString(4, ls_site_code__mfg);
					pstmt1.setString(5, lc_potency);
					pstmt1.setString(6, ls_supp_code);
					pstmt1.setString(7, lc_conv_qty_stduom);
					pstmt1.setString(8, ls_considerallocate);
					pstmt1.setString(9, ls_pack_code);
					pstmt1.setString(10, ls_item_code);
					pstmt1.setString(11, ls_lot_no);
					cnt = pstmt1.executeUpdate();
					if(pstmt1 != null)
					{
						pstmt1.close();
						pstmt1 = null;
					}
					
					if(cnt == 0)
					{
						ls_errcode = "VXSTK2";
						isError = true;
						System.out.println("ls_errcode 1111111=======>>"+ls_errcode);
					}
				}

				sql1 = "SELECT COUNT(1) FROM ITEM_LOT_INFO WHERE ITEM_CODE = ? AND LOT_NO = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, ls_item_code);
				pstmt1.setString(2, ls_lot_no);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				
				if(cnt > 0)
				{
					sql1 = "UPDATE ITEM_LOT_INFO SET MFG_DATE = ?, EXP_DATE = ?, RETEST_DATE = ?, SITE_CODE__MFG = ?, SUPP_CODE__MFG = ?, " +
						"POTENCY_PERC = ?, PACK_CODE = ? WHERE ITEM_CODE = ? AND LOT_NO = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setDate(1, (java.sql.Date) ld_mfg_date);
					pstmt1.setDate(2, (java.sql.Date) ld_exp_date);
					pstmt1.setDate(3, (java.sql.Date)  ld_retest_date);
					pstmt1.setString(4, ls_site_code__mfg);
					pstmt1.setString(5, ls_supp_code);
					pstmt1.setString(6, lc_potency);
					pstmt1.setString(7, ls_pack_code);
					pstmt1.setString(8, ls_item_code);
					pstmt1.setString(9, ls_lot_no);
					pstmt1.executeUpdate();
					if(pstmt1 != null)
					{
						pstmt1.close();
						pstmt1 = null;
					}
				}
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
		}
		catch(Exception e)
		{
			System.out.println("Exception============>> "+e);
			isError = true;
		}
		finally
		{

			try
			{				
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
		System.out.println("rturn error code ls_errcode =========>> "+ls_errcode);
		return ls_errcode;
		
		
	}
}


