/*
*	PURPOSE : Inv Hold Generation
*	Author: Sandesh
*	Date:	30/08/11
*/
package ibase.webitm.ejb.dis;

import ibase.ejb.*;
import ibase.utility.*;
import ibase.system.config.*;
import ibase.utility.BaseLogger;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.adv.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.utility.ITMException;
import ibase.utility.CommonConstants;

import java.lang.*;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;

import javax.ejb.*;

import java.text.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
 
public class InvHoldGen 
{
	
	public String generateHoldTrans( String lockCode, String refID, String refSer, String siteCode, ArrayList stockList, String xtraParams, Connection conn )throws RemoteException,ITMException
	{
		String sql = "",sqlIn="";
		String confirmed = "";
		String itemCode = "";
		String locCode = "";
		String lotSl = "";
		String lotNo = "";
		String chgUser = "",userId = "";
		String chgTerm = "";
		String retString = "";
		String tranId = "",asnNo = "";
		String holdInsertSql  = "", sqlSelAsn = "", holddetInsertSql = "",lineNoSl ="";//Added by Jagruti Shinde
		java.sql.Timestamp chgDate = null,currDate = null,currDate2 = null;
		int lineNo = 0,stdDays=0;

		boolean isError = false;

		PreparedStatement pstmtHold = null,pstmtIn=null;
		PreparedStatement pstmtHolddet = null;
		PreparedStatement psmtstmtAsn = null;
		ResultSet rsSel = null,rsIn=null,rsAsn = null;
		java.util.Date date = null;
		try
		{
			if ( lockCode == null || lockCode.trim().length() == 0 )
			{
				return "";
			}
			//conn.setAutoCommit(false);
			//addd by Ritesh on 09/05/13 start
			sqlSelAsn = "SELECT ASN_NO FROM PORCP WHERE TRAN_ID = ?";
			psmtstmtAsn = conn.prepareStatement(sqlSelAsn);
			psmtstmtAsn.setString(1, refID);
			rsAsn = psmtstmtAsn.executeQuery();
			if(rsAsn.next())
			{
				asnNo = rsAsn.getString(1);
			}
			psmtstmtAsn.close();
			psmtstmtAsn = null;
			rsAsn.close();
			rsAsn = null;
			holdInsertSql = "INSERT INTO INV_HOLD(TRAN_ID,TRAN_DATE,SITE_CODE,CONFIRMED,REF_ID,REF_SER,CHG_USER,CHG_DATE,CHG_TERM, LOCK_CODE,REF_NO) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

			pstmtHold = conn.prepareStatement(holdInsertSql);//addd by Ritesh on 09/05/13 end

			//Changed by Jagruti Shinde for inserting line_no_sl for qc rejected pallet. Req id:[W16CSUN009]
			//holddetInsertSql = "INSERT INTO INV_HOLD_DET(TRAN_ID,LINE_NO,ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL,HOLD_STATUS,SCH_REL_DATE)	VALUES(?,?,?,?,?,?,?,?,?)";
			holddetInsertSql = "INSERT INTO INV_HOLD_DET(TRAN_ID,LINE_NO,ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL,HOLD_STATUS,SCH_REL_DATE ,LINE_NO_SL )	VALUES(?,?,?,?,?,?,?,?,?,?)";

			pstmtHolddet = conn.prepareStatement(holddetInsertSql);
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();

			chgDate = new java.sql.Timestamp( System.currentTimeMillis() );

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			chgDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");


			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );

			int updCnt = 0;
			
			//Added by Gulzra - 26/11/11
			if (userId == null || userId.trim().length() == 0)
			{
				userId = "SYSTEM";
			}
			if (chgTerm == null || chgTerm.trim().length() == 0)
			{
				chgTerm = "SYSTEM";
			}
			//End changes by gulzar  - 26/11/11
			tranId = generateTranId( "w_inv_hold", siteCode, conn );

			pstmtHold.setString( 1, tranId );
			pstmtHold.setTimestamp( 2, java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0"));
			pstmtHold.setString( 3, siteCode );
			pstmtHold.setString( 4, "N" );
			pstmtHold.setString( 5, refID );
			pstmtHold.setString( 6, refSer );
			pstmtHold.setString( 7, userId );
			pstmtHold.setTimestamp( 8, currDate );
			pstmtHold.setString( 9, chgTerm );
			pstmtHold.setString( 10, lockCode );
			pstmtHold.setString( 11, asnNo );		//added by Ritesh on 09/05/13

			updCnt = pstmtHold.executeUpdate();
			pstmtHold.clearParameters();
			
			if( updCnt > 0 )
			{					
				System.out.println( updCnt + " rows updated successfully" );
			}
			
			HashMap dataMap = new HashMap();

			for ( int ctr = 0; ctr < stockList.size(); ctr++ )
			{
				lineNo++;
				dataMap = (HashMap)stockList.get(ctr);
				
				itemCode = "";
				siteCode = "";
				locCode = "";
				lotNo = "";
				lotSl = "";
				//Changed by Jagruti Shinde for inserting line_no_sl for qc rejected pallet. Req id:[W16CSUN009]
				lineNoSl = "";
				
				if ( dataMap.get("item_code") != null )
				{
					itemCode = (String)dataMap.get("item_code");
				}
				if ( dataMap.get("site_code") != null )
				{
					siteCode = (String)dataMap.get("site_code");
				}
				if ( dataMap.get("loc_code") != null )
				{
					locCode = (String)dataMap.get("loc_code");
				}
				if ( dataMap.get("lot_no") != null )
				{
					lotNo = (String)dataMap.get("lot_no");
				}
				if ( dataMap.get("lot_sl") != null )
				{
					lotSl = (String)dataMap.get("lot_sl");
				}
				//Changed by Jagruti Shinde for inserting line_no_sl for qc rejected pallet. Req id:[W16CSUN009][Start]
				if ( dataMap.get("line_no") != null )
				{
					lineNoSl = (String)dataMap.get("line_no");
				}
				//Changed by Jagruti Shinde for inserting line_no_sl for qc rejected pallet. Req id:[W16CSUN009][End]
				
				//add by cpatil start on 28-07-12 as per manoharan sir
				
				sqlIn="select std_days from inv_lock where lock_code = ?";
				pstmtIn = conn.prepareStatement(sqlIn);
				pstmtIn.setString(1,lockCode);
				
				rsIn = pstmtIn.executeQuery();
				if(rsIn.next())
				{    
					stdDays = rsIn.getInt("std_days");
				}
				rsIn.close();
				rsIn= null;
				pstmtIn.close();
				pstmtIn=null;
				
				 Calendar c = Calendar.getInstance();
				 c.setTime(currDate);
				 c.add(Calendar.DATE, stdDays);
				 currDate2 = new Timestamp(c.getTimeInMillis());
				 
				//add by cpatil on 28-07-12 end
				pstmtHolddet.setString( 1, tranId );
				pstmtHolddet.setInt( 2, lineNo );
				pstmtHolddet.setString( 3, itemCode );
				pstmtHolddet.setString( 4, siteCode );
				pstmtHolddet.setString( 5, locCode );
				pstmtHolddet.setString( 6, lotNo );
				pstmtHolddet.setString( 7, lotSl );
				pstmtHolddet.setString( 8, "H" );
				pstmtHolddet.setTimestamp( 9, currDate2 ); //add by cpatil on 28-07-12 as per manoharan sir
				pstmtHolddet.setString( 10, lineNoSl ); //Added by Jagruti Shinde Req id:[W16CSUN009]
				pstmtHolddet.addBatch();
				pstmtHolddet.clearParameters();
				dataMap.clear(); 
			}
			pstmtHolddet.executeBatch();
			pstmtHolddet.clearBatch();
			InvHoldConf invHoldConf = new InvHoldConf();
			retString = invHoldConf.confirm( tranId, xtraParams, "" , conn ,false);
			if (retString.indexOf("VTCNFSUCC") > 0)
			{
				retString = "";
			}
			else
			{
				isError = true;
			}
		}
		catch(BatchUpdateException buex)
        {
			isError = true;
			retString = "ERROR";
			int [] updateCounts = buex.getUpdateCounts();
			for (int i = 0; i < updateCounts.length; i++)
			{
				System.err.println("  Statement " + i + ":" + updateCounts[i]);
			}
			System.err.println(" Message: " + buex.getMessage());
			System.err.println(" SQLSTATE: " + buex.getSQLState());
			System.err.println(" Error code: " + buex.getErrorCode());
			SQLException ex = buex.getNextException();
			while (ex != null)
			{ 
				System.err.println("SQL exception:");
				System.err.println(" Message: " + ex.getMessage());
				System.err.println(" SQLSTATE: " + ex.getSQLState());
				System.err.println(" Error code: " + ex.getErrorCode());
				ex = ex.getNextException();
			}
			throw new ITMException(buex); //Added By Mukesh Chauhan on 07/08/19
        }
		catch (Exception e)
		{
			isError = true;
			retString = "ERROR";
			System.out.println ( "Exception: InvHoldGen: " + e.getMessage() + ":" );
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if ( !isError )
				{
					//conn.commit();
				}
				if ( rsSel !=null )
				{
					rsSel.close();
					rsSel = null;
				}
				if (rsAsn != null)
				{
					rsAsn.close();
					rsAsn = null;
				}
				if ( pstmtHold!=null )
				{
					pstmtHold.close();
					pstmtHold = null;
				}
				if ( pstmtHolddet!=null )
				{
					pstmtHolddet.close();
					pstmtHolddet = null;
				}
				if (psmtstmtAsn != null)
				{
					psmtstmtAsn.close();
					psmtstmtAsn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception ::==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		return retString;
	}

	private String generateTranId( String windowName, String siteCode, Connection conn )throws ITMException
    {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		String paySiteCode = "";
		String effectiveDate = "";
		java.sql.Timestamp currDate = null;
		java.sql.Date effDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();

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
			catch(Exception e){}
		}
        return tranId;
     }//generateTranTd()		
}
