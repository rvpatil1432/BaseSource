/**
 * Author : Chaitali Parab
 * Date   : 
 * 
 * */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;

import ibase.system.config.*;
import ibase.webitm.ejb.*; 

//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.dis.*;

import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
//Changed by Rohan on 24-07-13 for importing adv packages
import ibase.webitm.ejb.dis.adv.*;
@javax.ejb.Stateless
public class LotStatPos extends ValidatorEJB implements LotStatPosLocal, LotStatPosRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave( String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		String errMsg = "";
		String tranIdRel = "";
		String lineNoRel = "";
		String itemCode = "";
		String lotNo = "";
		String sql = "";
		String invHoldRelSql = "";
		String invHoldRelDetSql = "";
		String siteCode = "";
		String confirmed = "";
		String empCode = "";
		String remark = "";
		String itemCodeDet = "";
		String lotNoDet = "";
		String lineNo = "";
		String tranIdGenerate = "";

		boolean flag = false;
		//Change by Rohan on 25-07-13 for define variable.start
		boolean isHoldHdr = false;
		String invholdSql = "";
		String invholddetSql = "";
		String tranIdHold = "";
		String lockCodeRej  = "";
		String locCode = "";
		String lotSl = "";
		
		int lineNoHoldCtr = 0;
		
		PreparedStatement pstmtHdr = null;
		PreparedStatement pstmtDet = null;
		ResultSet rsHdr = null;
		ResultSet rsDet = null;
		
		DistCommon discommon = new DistCommon();
		//Change by Rohan on 25-07-13 for define variable.end
		int count = 0;
		int updCnt = 0;
		int ctr = 0;
		int lineNoCtr = 0;
		java.sql.Timestamp chgDate = null,currDate = null;
		Date date = null;
		String chgTerm = "";
		String userId = "";
		String lotStatus = "";
		String lockCode = "";
		Document dom = null;

		PreparedStatement pStmt = null;
		PreparedStatement pRelHdr = null;
		PreparedStatement pRelDet = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		HashMap tempMap = null;
		//changed by sankara on 16/09/13 for displaying error message.	
		boolean dataFlag = false, holdFlag = false, relFlag = false;
		
		try
		{
			dom = genericUtility.parseString(domString);

			empCode  = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			if(empCode == null || "null".equals(empCode))
			{
				empCode = "SYSTEM";
			}
			// 10/04/14 manoharan check whether lock exists
			boolean invHoldExists = true;
			int length = dom.getElementsByTagName( "Detail2").getLength();
			for ( int pctr = 0; pctr < length; pctr++ )
			{
				count = 0;
				itemCodeDet = genericUtility.getColumnValueFromNode("item_code", dom.getElementsByTagName("Detail2").item(pctr));
				lotNoDet = genericUtility.getColumnValueFromNode("lot_no", dom.getElementsByTagName("Detail2").item(pctr));
				lockCode = genericUtility.getColumnValueFromNode("lock_code", dom.getElementsByTagName("Detail2").item(pctr));
				sql = "select count(*) from inv_hold h, inv_hold_det d "
					+ " where h.tran_id = d.tran_id and h.confirmed = 'Y' and h.lock_code = ? and d.item_code = ? and d.lot_no = ? and d.hold_status = 'H'";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, lockCode);
				pStmt.setString(2, itemCodeDet);
				pStmt.setString(3, lotNoDet);
				rs = pStmt.executeQuery();
				if( rs.next() )
				{
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pStmt.close();
				pStmt = null;	
				if (count == 0)
				{
					invHoldExists = false;
					break;
				}
				
			}
			if (!invHoldExists)
			{
				errMsg = itmDBAccessLocal.getErrorString("","VTNODATAFD","","",conn);
				return errMsg;
			}
			invHoldRelSql = "INSERT INTO INV_HOLD_REL(TRAN_ID, TRAN_DATE, SITE_CODE, REMARKS, CONFIRMED, EMP_CODE__APRV,CHG_USER ,CHG_DATE, CHG_TERM) VALUES(?,?,?,?,?,?,?,?,?)";
			pRelHdr = conn.prepareStatement(invHoldRelSql);
			invHoldRelDetSql = "INSERT INTO INV_HOLD_REL_DET(TRAN_ID, LINE_NO, TRAN_ID__HOLD, LINE_NO__HOLD, REMARKS) VALUES(?,?,?,?,?)";
			pRelDet = conn.prepareStatement(invHoldRelDetSql);

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			siteCode =  genericUtility.getColumnValueFromNode("site_code", dom.getElementsByTagName("Detail1").item(0));
			confirmed = "N";
			//empCode = "";

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);

			tranIdGenerate = generateTranId( "w_inv_hold_rel", siteCode, currDateStr, conn );

			pRelHdr.setString( 1, tranIdGenerate );
			pRelHdr.setTimestamp( 2, currDate );
			pRelHdr.setString( 3, siteCode );
			pRelHdr.setString( 4, " " );
			pRelHdr.setString( 5, confirmed );
			pRelHdr.setString( 6, "" );
			pRelHdr.setString( 7, userId );
			pRelHdr.setTimestamp( 8, currDate );
			pRelHdr.setString( 9, chgTerm );

			updCnt = pRelHdr.executeUpdate();
			pRelHdr.clearParameters();
			if( updCnt > 0 )
			{					
				System.out.println( updCnt + " rows updated successfully" );
			}
			//Chnaged by Rohan on 25-07-13 for if lot staus 'R' then aquried new lock configured in disparam.start
			invholdSql = "INSERT INTO INV_HOLD(TRAN_ID,TRAN_DATE,SITE_CODE,CONFIRMED,REF_ID,REF_SER,CHG_USER,CHG_DATE,CHG_TERM, LOCK_CODE,REF_NO) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		    pstmtHdr = 	conn.prepareStatement(invholdSql);		
		    
			invholddetSql = "INSERT INTO INV_HOLD_DET(TRAN_ID,LINE_NO,ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL,HOLD_STATUS,SCH_REL_DATE)	VALUES(?,?,?,?,?,?,?,?,?)";
			pstmtDet = 	conn.prepareStatement(invholddetSql);		
			
			lockCodeRej = discommon.getDisparams("999999","REJECTION_LOCKCODE",conn);
			//Chnaged by Rohan on 25-07-13 for if lot staus 'R' then aquried new lock configured in disparam.end
			length = dom.getElementsByTagName( "Detail2").getLength();
			
			for ( int pctr = 0; pctr < length; pctr++ )
			{
				lotStatus = genericUtility.getColumnValueFromNode("lot_status", dom.getElementsByTagName("Detail2").item(pctr));
				
				//Changed by Rohan on 24-07-13 for release lot staus 'R'
				//if(lotStatus.equalsIgnoreCase("A"))
				if((!"".equalsIgnoreCase(lotStatus) && lotStatus != null)) 
				{
					lineNo = genericUtility.getColumnValueFromNode("line_no", dom.getElementsByTagName("Detail2").item(pctr));
					itemCodeDet = genericUtility.getColumnValueFromNode("item_code", dom.getElementsByTagName("Detail2").item(pctr));
					lotNoDet = genericUtility.getColumnValueFromNode("lot_no", dom.getElementsByTagName("Detail2").item(pctr));
					lockCode = genericUtility.getColumnValueFromNode("lock_code", dom.getElementsByTagName("Detail2").item(pctr));
					
					/*
					sql = "select count(*) from asn_det where item_code = ? and lot_no = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCodeDet);
					pStmt.setString(2, lotNoDet);
					rs = pStmt.executeQuery();
					if( rs.next() )
					{
						count = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;					

					if(count != 0)
					{
					*/	
						ctr++;
						//flag = true;
						//Chnaged by Rohan on 25-07-13 for if lot staus 'R' then aquried new lock configured in disparam.start
						if((!"".equalsIgnoreCase(lotStatus) && lotStatus != null) && !lotStatus.equalsIgnoreCase("A") && !isHoldHdr)
						{
							holdFlag = true;
							tranIdHold = generateTranId( "w_inv_hold", siteCode, currDateStr, conn );
							System.out.println("tranIdHold"+tranIdHold);
							pstmtHdr.setString( 1, tranIdHold );
							pstmtHdr.setTimestamp( 2, currDate);
							pstmtHdr.setString( 3, siteCode );
							pstmtHdr.setString( 4, "N" );
							pstmtHdr.setString( 5, tranIdGenerate );
							pstmtHdr.setString( 6, "P-RCP");
							pstmtHdr.setString( 7, userId );
							pstmtHdr.setTimestamp( 8, currDate );
							pstmtHdr.setString( 9, chgTerm );
							pstmtHdr.setString( 10, lockCodeRej );
							pstmtHdr.setString( 11, tranId );

							updCnt = pstmtHdr.executeUpdate();
							pstmtHdr.clearParameters();
							
							if( updCnt > 0 )
							{
								isHoldHdr = true;
								System.out.println( updCnt + " rows updated successfully" );
							}
							
						}
						//Chnaged by Rohan on 25-07-13 for if lot staus 'R' then aquried new lock configured in disparam.end
						//CHanged by Rohan on 26-07-13 for getting loc code and lot sl.
						//sql = "select ih.tran_id, ih.line_no from inv_hold h, inv_hold_det ih"
						sql = "select ih.tran_id, ih.line_no,ih.loc_code,ih.lot_sl from inv_hold h, inv_hold_det ih"
							+" where h.tran_id = ih.tran_id "
							+" and h.lock_code = ?"
							+" and ih.item_code = ?"
							+" and ih.lot_no = ?"
							+" and ih.hold_status = 'H'";
							
							//+" and h.ref_id in ("
							//+" select p.tran_id from porcp p where p.asn_no in ("
							//+" select ah.asn_no from asn_hdr ah where ah.tran_id in ("
							//+" select tran_id from asn_det ad where ad.item_code = ? and ad.lot_no = ? )"
							//+" ) )
							
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, lockCode);
						pStmt.setString(2, itemCodeDet);
						pStmt.setString(3, lotNoDet);
						//pStmt.setString(4, itemCodeDet);
						//pStmt.setString(5, lotNoDet);
						rs = pStmt.executeQuery();
						
						while( rs.next() )//change if to while loop on 22/apr/14 kunal m
						{
							//changed by sankara on 16/09/13 for displaying error message.
							dataFlag = true;
							relFlag = true;
							lineNoCtr++;
							tranIdRel = rs.getString("tran_id");
							lineNoRel = rs.getString("line_no");
							pRelDet.setString( 1, tranIdGenerate );
							pRelDet.setInt( 2,lineNoCtr);
							pRelDet.setString( 3, tranIdRel );
							pRelDet.setString( 4, lineNoRel );
							pRelDet.setString( 5, remark );
							pRelDet.addBatch();
							pRelDet.clearParameters();
							//Chnaged by Rohan on 25-07-13 for if lot staus 'R' then aquried new lock configured in disparam.start
							if((!"".equalsIgnoreCase(lotStatus) && lotStatus != null) && !lotStatus.equalsIgnoreCase("A"))
							{
								holdFlag = true;
								lineNoHoldCtr++; 
								locCode = checkNull(rs.getString("loc_code"));
								lotSl = checkNull(rs.getString("lot_sl"));
								pstmtDet.setString( 1, tranIdHold );
								pstmtDet.setInt( 2, lineNoHoldCtr );
								pstmtDet.setString( 3, itemCodeDet );
								pstmtDet.setString( 4, siteCode );
								pstmtDet.setString( 5, locCode );
								pstmtDet.setString( 6, lotNoDet );
								pstmtDet.setString( 7, lotSl );
								pstmtDet.setString( 8, "H" );
								pstmtDet.setTimestamp( 9, currDate ); 
								pstmtDet.addBatch();
								pstmtDet.clearParameters();
							}	
							//Chnaged by Rohan on 25-07-13 for if lot staus 'R' then aquried new lock configured in disparam.end
					
						}
						//changed by sankara on 16/09/13 for displaying error message start.
						if( dataFlag == false )
						{
							errMsg = itmDBAccessLocal.getErrorString("","VTNODATAFD","","",conn);
							return errMsg;
						}
						//changed by sankara on 16/09/13 for displaying error message end.
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;					

					
						//updCnt = pRelDet.executeUpdate();
						
						if( updCnt > 0 )
						{					
							System.out.println( updCnt + " rows updated successfully" );
						}
					//} // asn_det checking
				}//lotStatus = 'A'
				
			}//for pctr
			
			pRelDet.executeBatch();
			pRelDet.clearBatch();
			//Changed by Rohan on 26-07-13 for execute batch .strat
			pstmtDet.executeBatch();
			pstmtDet.clearBatch();
			if(pstmtDet != null)
			{
				pstmtDet.close();
				pstmtDet = null;
			}
			if(pstmtHdr != null)
			{
				pstmtHdr.close();
				pstmtHdr = null;
			}
			//Changed by Rohan on 26-07-13 for execute batch .end
			//if(flag == true)
			//if(length == ctr)
			//{
				//Chaged by Rohan on 24-07-13 for releasing stock.start
				System.out.println("888888888888 GOing for release old lock 888888888888888888888888");
				InvHoldRelConf InvHoldRelConf = new InvHoldRelConf();
				//confirm(tranIdRel,xtraParams,"F", conn, false);
				errMsg = InvHoldRelConf.confirm( tranIdGenerate,xtraParams,"F",conn,false);
				
				if ( errMsg == null && errMsg.trim().length() <= 0 )
				{
					errMsg = itmDBAccessLocal.getErrorString("","VTPRCERR","","",conn);
					return errMsg;
				}
				
				
				//Changed by Rohan on 24-07-13 for release lot staus 'R' and then put REJECTION_LOCKCODE.start
				//if((!"".equalsIgnoreCase(lotStatus) && lotStatus != null) && !lotStatus.equalsIgnoreCase("A"))
				//{
					System.out.println("888888888888 GOing for aqurie new lock 888888888888888888888888");
					
					//update stock hold qty.start
					//sql = "UPDATE STOCK SET HOLD_QTY = QUANTITY WHERE ITEM_CODE = ? AND SITE_CODE = ? " 
					//	  +" AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL =  ? ";
					//pStmt = conn.prepareStatement(sql);
					//pStmt.setString( 1, itemCodeDet );
					//pStmt.setString( 2, siteCode );
					//pStmt.setString( 3, locCode );
					//pStmt.setString( 4, lotNoDet );
					//pStmt.setString( 5, lotSl );
					//int rowcnt = pStmt.executeUpdate();
					//if(rowcnt == 0)
					//{
					//	errMsg = itmDBAccessLocal.getErrorString("","VTPRCERR","");
					//	return errMsg;
					//}
					//else
					//{
					//	System.out.println("**************stk hold succefuly ****************");
					//}
					//if(pStmt != null)
					//{
					//	pStmt.close();
					//	pStmt = null;
					//}
					//update stock hold qty.end
					if (holdFlag)
					{
						InvHoldConf invHoldConf = new InvHoldConf();
						errMsg = invHoldConf.confirm(tranIdHold,xtraParams, "F" ,conn,false); 
						if ( errMsg == null && errMsg.trim().length() <= 0 )
						{
							errMsg = itmDBAccessLocal.getErrorString("","VTPRCERR","","",conn);
							return errMsg;
						}
					}
				//}
				//Changed by Rohan on 24-07-13 for release lot staus not 'R' and then put REJECTION_LOCKCODE.end
				//Chaged by Rohan on 24-07-13 for releasing stock.end
				sql = "update lot_stat_hdr set confirmed = 'Y', conf_date = ? , EMP_CODE__APRV = ? where tran_id = ?";
				pStmt = conn.prepareStatement(sql);
				pStmt.setTimestamp(1,currDate);
				pStmt.setString(2,empCode);
				pStmt.setString(3,tranId);
				int rowcnt = pStmt.executeUpdate();
				if(rowcnt == 0)
				{
					errMsg = itmDBAccessLocal.getErrorString("","VTPRCERR","","",conn);
					return errMsg;
				}
				
			//}

		}
		catch(BatchUpdateException buex)
        {
			try
			{
				conn.rollback();
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
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}
			throw new ITMException(buex);
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
				
				if(pRelDet != null)
				{
					pRelDet.close();
					pRelDet = null;
				}
				if(pRelHdr != null)
				{
					pRelHdr.close();
					pRelHdr = null;
				}
				if(pStmt != null)
				{
					pStmt.close();
					pStmt = null;
				}
				//Chnaged by Rohan on 24-07-13 for commiting connection.start
				if(pstmtDet != null)
				{
					pstmtDet.close();
					pstmtDet = null;
				}
				if(pstmtHdr != null)
				{
					pstmtHdr.close();
					pstmtHdr = null;
				}
				
				if ( errMsg != null && errMsg.trim().length() > 0 && errMsg.indexOf("success") != -1 )
				{
					conn.commit();
					errMsg = "";
				}
				else
				{
					conn.rollback();
				}
				//Chnaged by Rohan on 24-07-13 for commiting connection.start
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return errMsg;
	}	
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
	private String generateTranId( String windowName, String siteCode, String tranDateStr, Connection conn )throws ITMException
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

		java.sql.Date effDate = null;

		try
		{
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
			xmlValues = xmlValues +		"<tran_id></tran_id>";
			xmlValues = xmlValues +		"<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +		"<tran_date>" + tranDateStr + "</tran_date>"; 
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
}